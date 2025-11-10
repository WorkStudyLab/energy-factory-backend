# Admin CMS 구현 문서

## 개요
관리자 전용 상품 관리 시스템(CMS) 구현 및 인증/권한 버그 수정

**작성일**: 2025-11-10
**작성자**: Development Team

---

## 1. 시스템 구조

### 1.1 API 엔드포인트
```
Base URL: /api/admin/products
권한 요구사항: ROLE_ADMIN
인증 방식: HttpOnly Cookie (JWT)
```

### 1.2 주요 엔드포인트

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/api/admin/products` | 상품 목록 조회 (페이지네이션, 필터링) | ADMIN |
| GET | `/api/admin/products/{id}` | 상품 상세 조회 | ADMIN |
| POST | `/api/admin/products` | 신규 상품 생성 | ADMIN |
| PUT | `/api/admin/products/{id}` | 상품 정보 수정 | ADMIN |
| DELETE | `/api/admin/products/{id}` | 상품 삭제 | ADMIN |
| GET | `/api/admin/products/categories` | 카테고리 목록 조회 | ADMIN |
| POST | `/api/admin/products/images` | 상품 이미지 업로드 (S3) | ADMIN |
| DELETE | `/api/admin/products/images` | 상품 이미지 삭제 (S3) | ADMIN |

---

## 2. 구현된 기능

### 2.1 상품 CRUD
- 상품 생성, 조회, 수정, 삭제 기능
- 태그 관리 (ProductTag 연관 관계)
- 변형 상품(Variant) 지원
- 페이지네이션 및 필터링 (카테고리, 가격, 상태, 키워드)

### 2.2 이미지 관리
- AWS S3 연동 (프로덕션 환경에만 활성화)
- 이미지 업로드/삭제 기능
- 파일 검증: 크기(5MB 이하), 확장자(jpg, jpeg, png, gif, webp)

### 2.3 보안
- Spring Security 기반 권한 체크
- HttpOnly Cookie를 통한 JWT 인증
- 환경별 Cookie Secure 플래그 설정

---

## 3. 버그 수정 내역

### 3.1 Cookie Secure 플래그 문제

**증상**:
```
- 로컬 환경(HTTP)에서 로그인 성공 후 관리자 API 호출 시 401 Unauthorized
- 프론트엔드에서 "인증 정보가 유효하지 않습니다" 오류 발생
```

**원인**:
```java
// LoginFilter.java - 기존 코드
private void addTokenCookie(...) {
    cookie.setSecure(true);  // 하드코딩: HTTPS에서만 쿠키 전송
}
```
- 로컬 환경은 HTTP를 사용하므로 `Secure=true` 쿠키가 전송되지 않음
- 인증 쿠키가 브라우저에서 서버로 전송되지 않아 인증 실패

**해결**:

1. **application-local.yml** - 환경별 설정 추가
```yaml
app:
  cookie:
    secure: false  # 로컬 환경: HTTP 허용
```

2. **application-prod.yml**
```yaml
app:
  cookie:
    secure: true  # 프로덕션: HTTPS만 허용
```

3. **SecurityConfig.java** - 설정값 주입
```java
@Value("${app.cookie.secure}")
private boolean cookieSecure;

// LoginFilter 생성 시 전달
new LoginFilter(authenticationManager, jwtUtil, refreshTokenService, cookieSecure)
```

4. **LoginFilter.java** - 파라미터 수정
```java
private final boolean cookieSecure;

public LoginFilter(..., boolean cookieSecure) {
    this.cookieSecure = cookieSecure;
}

private void addTokenCookie(...) {
    cookie.setSecure(cookieSecure);  // 환경별 설정 사용
}
```

**결과**:
- 로컬 환경에서 HTTP Cookie 인증 정상 작동 ✅
- 프로덕션 환경에서는 HTTPS 강제로 보안 유지 ✅

---

### 3.2 Spring Security 권한 접두사 누락 문제

**증상**:
```
- JWT 토큰 검증 성공, SecurityContext에 인증 정보 등록 완료
- 하지만 /api/admin/** 엔드포인트 접근 시 여전히 401 Unauthorized
- 백엔드 로그: "authentication success: userId=72, email=admin@naver.com"
```

**원인**:
```java
// CustomUserDetails.java - 기존 코드
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    collection.add(new GrantedAuthority() {
        public String getAuthority() {
            return user.getRole().toString();  // "ADMIN" 반환
        }
    });
}

// SecurityConfig.java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
// hasRole("ADMIN")은 내부적으로 "ROLE_ADMIN"을 기대
```

Spring Security의 `.hasRole("ADMIN")` 메서드는 자동으로 "ROLE_" 접두사를 추가하여 "ROLE_ADMIN"과 비교합니다.
하지만 `getAuthority()`가 "ADMIN"만 반환하여 권한 체크 실패.

**해결**:
```java
// CustomUserDetails.java - 수정 후
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    collection.add(new GrantedAuthority() {
        public String getAuthority() {
            return "ROLE_" + user.getRole().toString();  // "ROLE_ADMIN" 반환
        }
    });
}
```

**결과**:
- `/api/admin/**` 엔드포인트 ADMIN 권한 체크 정상 작동 ✅
- 로그인 응답에서도 `"role": "ROLE_ADMIN"` 반환 확인 ✅

---

## 4. 파일 구조

```
src/main/java/com/energyfactory/energy_factory/
├── controller/
│   └── AdminProductController.java       # 관리자 상품 관리 API
├── service/
│   ├── AdminProductService.java          # 상품 CRUD 비즈니스 로직
│   ├── ProductService.java               # 공통 상품 조회 로직
│   └── S3Service.java                    # AWS S3 이미지 업로드/삭제
├── dto/
│   ├── CustomUserDetails.java            # UserDetails 구현 (권한 정보 포함)
│   ├── ProductCreateRequestDto.java
│   ├── ProductUpdateRequestDto.java
│   └── ProductResponseDto.java
├── jwt/
│   ├── JwtFilter.java                    # JWT 토큰 검증 필터
│   └── LoginFilter.java                  # 로그인 처리 필터 (쿠키 생성)
├── config/
│   ├── SecurityConfig.java               # Spring Security 설정
│   └── S3Config.java                     # AWS S3 클라이언트 설정
└── repository/
    └── ProductRepository.java

src/main/resources/
├── application-local.yml                 # 로컬 환경 설정
└── application-prod.yml                  # 프로덕션 환경 설정
```

---

## 5. 테스트 결과

### 5.1 로그인 테스트
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@naver.com","password":"Dkdhtl@qkf1"}' \
  -c cookies.txt

# 응답
{
  "status": 200,
  "data": {
    "userId": 72,
    "role": "ROLE_ADMIN",
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
}
```

### 5.2 관리자 API 테스트
```bash
# 상품 목록 조회
curl -X GET "http://localhost:8080/api/admin/products" -b cookies.txt
# 응답: 200 OK ✅

# 카테고리 목록 조회
curl -X GET "http://localhost:8080/api/admin/products/categories" -b cookies.txt
# 응답: 200 OK ✅
```

---

## 6. 환경 설정

### 6.1 로컬 환경 (application-local.yml)
```yaml
spring:
  profiles:
    active: local

app:
  cookie:
    secure: false  # HTTP 허용
  cors:
    allowed-origins: http://localhost:3000,http://localhost:5173
```

### 6.2 프로덕션 환경 (application-prod.yml)
```yaml
spring:
  profiles:
    active: prod

app:
  cookie:
    secure: true  # HTTPS 필수
  cors:
    allowed-origins: https://your-domain.com

cloud:
  aws:
    s3:
      bucket: energy-factory-products
    region:
      static: ap-northeast-2
    credentials:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
```

---

## 7. 주요 개념 정리

### 7.1 HttpOnly Cookie
- JavaScript에서 접근 불가 (XSS 공격 방어)
- HTTP 요청 시 자동으로 전송
- `Secure` 플래그: HTTPS에서만 전송
- `SameSite=None`: Cross-origin 요청 허용 (CORS)

### 7.2 Spring Security 권한 체크
```java
// 메서드별 차이점
.hasRole("ADMIN")         // "ROLE_ADMIN"과 비교 (접두사 자동 추가)
.hasAuthority("ADMIN")    // "ADMIN"과 정확히 비교
.hasAuthority("ROLE_ADMIN") // "ROLE_ADMIN"과 정확히 비교
```

### 7.3 JWT 토큰 구조
```json
{
  "userId": 72,
  "username": "admin@naver.com",
  "role": "ROLE_ADMIN",
  "tokenType": "access",
  "iat": 1762736479,
  "exp": 1762738279
}
```

---

## 8. 향후 개선 사항

### 8.1 기능 개선
- [ ] 상품 일괄 업로드 기능 (CSV/Excel)
- [ ] 상품 복사 기능
- [ ] 재고 히스토리 관리
- [ ] 통계 대시보드

### 8.2 보안 강화
- [ ] Rate Limiting (API 호출 제한)
- [ ] IP 화이트리스트 (관리자 접근 제한)
- [ ] 관리자 활동 로그 기록
- [ ] 2FA (Two-Factor Authentication)

### 8.3 성능 최적화
- [ ] 이미지 썸네일 자동 생성
- [ ] Redis 캐싱 (상품 목록)
- [ ] Lazy Loading (상품 변형, 태그)

---

## 9. 트러블슈팅

### 9.1 401 Unauthorized 에러
**체크리스트**:
1. 로그인 성공 여부 확인
2. Cookie가 브라우저에 저장되었는지 확인 (개발자 도구 > Application > Cookies)
3. Cookie의 Secure 플래그 확인 (HTTP 환경에서 true이면 전송 안됨)
4. JWT 토큰 만료 시간 확인 (Access Token: 30분)
5. 사용자 권한 확인 (`role: "ROLE_ADMIN"`)
6. Backend 로그 확인: `Token from cookie`, `authentication success`

### 9.2 CORS 에러
```yaml
# application.yml
app:
  cors:
    allowed-origins: http://localhost:3000  # 프론트엔드 URL 추가
```

### 9.3 S3 업로드 실패
- AWS 자격 증명 확인
- S3 버킷 권한 확인 (PutObject, DeleteObject)
- 로컬 환경에서는 S3Service가 로드되지 않음 (`@Profile("prod")`)

---

## 10. 참고 자료

### 10.1 관련 파일
- `SecurityConfig.java:91` - ADMIN 권한 설정
- `LoginFilter.java:203` - Cookie Secure 플래그 설정
- `CustomUserDetails.java:41` - ROLE 접두사 추가
- `AdminProductController.java` - CMS API 엔드포인트

### 10.2 커밋 히스토리
```
fix: HttpOnly Cookie 인증 및 관리자 권한 체크 버그 수정
- LoginFilter에 cookieSecure 파라미터 추가
- CustomUserDetails에 ROLE_ 접두사 추가
```

---

## 문의사항
개발 관련 문의사항은 개발팀으로 연락 부탁드립니다.
