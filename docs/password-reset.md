# 비밀번호 재설정 기능

## 개요
이메일 기반 비밀번호 재설정(찾기) 기능 구현

## 기술 스택
- **Spring Boot Mail**: 이메일 발송
- **AWS SES SMTP**: 이메일 전송 서비스
- **Redis**: 인증 코드 및 리셋 토큰 임시 저장
- **Spring Validation**: 입력 검증

## 아키텍처

### 3단계 플로우
```
1. 코드 발송 (POST /api/auth/password-reset/send-code)
   ↓
2. 코드 검증 (POST /api/auth/password-reset/verify-code)
   ↓
3. 비밀번호 변경 (POST /api/auth/password-reset)
```

## API 명세

### 1. 인증 코드 전송
**Endpoint:** `POST /api/auth/password-reset/send-code`

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "status": 200,
  "code": "20000000",
  "desc": "성공",
  "data": null
}
```

**특징:**
- 6자리 숫자 인증 코드 생성
- Redis에 5분간 저장
- 1분 재발송 제한 (Rate Limiting)
- AWS SES SMTP로 이메일 발송

---

### 2. 인증 코드 검증
**Endpoint:** `POST /api/auth/password-reset/verify-code`

**Request:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response:**
```json
{
  "status": 200,
  "code": "20000000",
  "desc": "성공",
  "data": {
    "resetToken": "0cdba16d-b559-4b9b-9003-44805a9ce30c",
    "message": "인증이 완료되었습니다. 10분 내에 비밀번호를 재설정해주세요."
  }
}
```

**특징:**
- 코드 검증 성공 시 UUID 리셋 토큰 발급
- 리셋 토큰은 Redis에 10분간 저장
- 검증 완료된 인증 코드는 삭제

---

### 3. 비밀번호 재설정
**Endpoint:** `POST /api/auth/password-reset`

**Request:**
```json
{
  "resetToken": "0cdba16d-b559-4b9b-9003-44805a9ce30c",
  "newPassword": "NewPass123!"
}
```

**Response:**
```json
{
  "status": 200,
  "code": "20000000",
  "desc": "성공",
  "data": null
}
```

**특징:**
- 리셋 토큰 검증
- 비밀번호 규칙: 8자 이상, 대소문자/숫자/특수문자 포함
- BCrypt로 암호화하여 DB 저장
- 사용된 리셋 토큰은 즉시 삭제 (재사용 방지)

## 보안 설계

### 1. 시간 제한
- **인증 코드**: 5분 유효
- **리셋 토큰**: 10분 유효
- **재발송 제한**: 1분 쿨다운

### 2. 토큰 관리
- UUID 기반 리셋 토큰 (추측 불가)
- 일회용 토큰 (사용 후 삭제)
- Redis 자동 만료 (TTL)

### 3. 비밀번호 정책
```
정규식: ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$

요구사항:
- 최소 8자
- 소문자 1개 이상
- 대문자 1개 이상
- 숫자 1개 이상
- 특수문자 1개 이상 (@$!%*?&)
```

## 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| 40100001 | 401 | 사용자를 찾을 수 없음 |
| 40700001 | 400 | 인증 코드 만료 |
| 40700002 | 400 | 인증 코드 불일치 |
| 40700003 | 400 | 리셋 토큰 무효 또는 만료 |
| 42900001 | 429 | 요청 과다 (1분 내 재요청) |
| 50000001 | 500 | 이메일 발송 실패 |

## 구현 파일

### Controller
- `PasswordResetController.java`: 3개 엔드포인트 제공

### Service
- `VerificationCodeService.java`: 인증 코드 생성/검증, 리셋 토큰 관리
- `EmailService.java`: AWS SES SMTP 이메일 발송
- `UserService.java`: 비밀번호 재설정 로직

### DTO
- `SendCodeRequestDto.java`: 코드 발송 요청
- `VerifyCodeRequestDto.java`: 코드 검증 요청
- `VerifyCodeResponseDto.java`: 코드 검증 응답 (리셋 토큰 포함)
- `ResetPasswordRequestDto.java`: 비밀번호 재설정 요청

### Configuration
- `application-local.yml`: 로컬 환경 설정
- `application-prod.yml`: 프로덕션 환경 설정

## 환경 설정

### 1. Redis
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### 2. AWS SES SMTP
```yaml
spring:
  mail:
    host: email-smtp.ap-northeast-2.amazonaws.com
    port: 587
    username: ${AWS_SES_SMTP_USERNAME}
    password: ${AWS_SES_SMTP_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

app:
  mail:
    from: ${MAIL_FROM_ADDRESS}
    from-name: Energy Factory
```

### 3. 환경 변수
```bash
export AWS_SES_SMTP_USERNAME="your-smtp-username"
export AWS_SES_SMTP_PASSWORD="your-smtp-password"
export MAIL_FROM_ADDRESS="noreply@energy-factory.kr"
```

## AWS SES 설정

### 현재 상태 (Sandbox Mode)
- **제한사항**: Verified된 이메일 주소로만 발송 가능
- **일일 한도**: 200통/24시간
- **발송 속도**: 1통/초

### 프로덕션 액세스 필요
```
모든 이메일 주소로 발송하려면:
1. 도메인 구매 (예: energy-factory.kr)
2. SES에서 도메인 자격 증명 생성
3. DNS 레코드 추가 (DKIM, SPF, MX)
4. 프로덕션 액세스 요청
5. AWS 승인 대기 (보통 24시간 이내)
```

## 테스트 방법

### 1. 로컬 환경 설정
```bash
# Redis 설치 및 실행
brew install redis
brew services start redis

# 환경 변수 설정
export AWS_SES_SMTP_USERNAME="..."
export AWS_SES_SMTP_PASSWORD="..."
export MAIL_FROM_ADDRESS="ehdns1133@gmail.com"

# 서버 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 2. API 테스트
```bash
# 1. 인증 코드 발송
curl -X POST "http://localhost:8080/api/auth/password-reset/send-code" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'

# 2. 이메일에서 코드 확인 후 검증
curl -X POST "http://localhost:8080/api/auth/password-reset/verify-code" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","code":"123456"}'

# 3. 비밀번호 재설정
curl -X POST "http://localhost:8080/api/auth/password-reset" \
  -H "Content-Type: application/json" \
  -d '{"resetToken":"토큰값","newPassword":"NewPass123!"}'

# 4. 새 비밀번호로 로그인 확인
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"NewPass123!"}'
```

## 주의사항

### 1. 이메일 발송
- AWS SES Sandbox 모드에서는 verified 이메일만 수신 가능
- 스팸 폴더 확인 필요
- 발신자 이메일(FROM)도 verified 되어야 함

### 2. Redis 의존성
- Redis가 실행되지 않으면 인증 코드 저장/조회 실패
- 로컬: `brew services start redis`
- EC2: `sudo systemctl start redis`

### 3. 비밀번호 정책
- 프론트엔드에서도 동일한 정규식 검증 권장
- 백엔드 validation 실패 시 명확한 에러 메시지 반환

## 향후 개선 사항

### 1. 보안 강화
- [ ] IP 기반 Rate Limiting 추가
- [ ] 실패 횟수 제한 (5회 실패 시 일시 차단)
- [ ] 이메일 발송 로그 기록

### 2. 기능 확장
- [ ] SMS 인증 옵션 추가
- [ ] 2단계 인증(2FA) 연동
- [ ] 최근 사용한 비밀번호 재사용 방지

### 3. 모니터링
- [ ] 이메일 발송 성공률 추적
- [ ] 인증 실패율 모니터링
- [ ] AWS SES Bounce/Complaint 처리

## 참고 자료
- [AWS SES Documentation](https://docs.aws.amazon.com/ses/)
- [Spring Mail Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)
- [Redis TTL Documentation](https://redis.io/commands/ttl/)
