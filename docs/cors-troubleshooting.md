# CORS 문제 해결 과정

## 문제 상황

### 증상
- localhost에서 `https://energy-factory.kr/api/payments/toss/confirm`로 결제 요청 시 CORS 에러 발생
- 브라우저 에러: `No 'Access-Control-Allow-Origin' header is present on the requested resource`
- 서버는 200 OK 응답하지만 브라우저가 차단
- 다른 API(`/api/cart` 등)는 정상 작동

### 초기 가설
- SecurityConfig의 CORS 설정 문제
- localhost가 허용 목록에 없음

---

## 원인 분석

### 1차 조사: CORS 설정 확인

**SecurityConfig.java**
```java
// 하드코딩된 CORS 설정 (환경변수 미사용)
configuration.setAllowedOrigins(Arrays.asList(
    "https://energy-factory.kr",
    "http://localhost:5173"  // ← 이미 포함되어 있음
));
```

**발견:** localhost는 이미 허용 목록에 있었음 → CORS 설정 자체는 문제 없음

### 2차 조사: OPTIONS vs POST 요청 비교

```bash
# OPTIONS (preflight) - 정상
curl -X OPTIONS https://energy-factory.kr/api/payments/toss/confirm
< access-control-allow-origin: http://localhost:5173  ✅

# POST (실제 요청) - 실패
curl -X POST https://energy-factory.kr/api/payments/toss/confirm
(CORS 헤더 없음)  ❌
```

**발견:** Preflight는 성공하지만 실제 요청에서 CORS 헤더가 사라짐

### 3차 조사: 다른 API와 비교

```bash
# /api/cart - 정상
< access-control-allow-origin: http://localhost:5173  ✅

# /api/payments/toss/confirm - 실패
< server: AmazonS3  ← 이상한 응답
< x-cache: Error from cloudfront
< age: 62452  ← 17시간 전 캐시
```

**핵심 발견:** 결제 API만 S3에서 응답이 오고 있음!

---

## 근본 원인

### 원인 1: WebConfig와 SecurityConfig의 CORS 설정 충돌

**WebConfig.java**
```java
@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
private String[] allowedOrigins;  // ❌ String[] 타입
```

**문제점:**
- `application-prod.yml`의 환경변수는 String 타입
- WebConfig는 String[]로 받으려 해서 실패
- Spring MVC CORS는 preflight만 처리

**SecurityConfig.java**
```java
@Value("${app.cors.allowed-origins}")
private String allowedOrigins;  // ✅ String 타입
```

**결과:**
- SecurityConfig는 정상 동작
- 하지만 WebConfig와 충돌하여 일부 요청에만 CORS 적용 안 됨

### 원인 2: CloudFront Error Pages 설정 (주 원인)

**CloudFront 설정:**
- 403, 404 에러 발생 시 → S3의 에러 페이지 반환
- Error Caching Minimum TTL: 300초

**문제 발생 시나리오:**
1. 초기 결제 테스트 → EC2에서 404 에러 발생 (테스트 orderId 없음)
2. CloudFront가 Error Pages 설정에 따라 **S3의 에러 페이지 반환**
3. **S3 응답에는 CORS 헤더 없음** ❌
4. 이 응답이 **17시간 동안 캐시됨** (age: 62452)
5. 이후 모든 결제 요청에 캐시된 S3 응답 반환 → CORS 에러

**왜 다른 API는 괜찮았나?**
- `/api/cart` 등은 에러가 발생하지 않아 정상적으로 EC2에서 응답
- `/api/payments/toss/confirm`만 초기에 404 발생 → S3 캐시 생성

---

## 해결 방법

### 1단계: WebConfig 제거

```bash
# EC2와 로컬 모두에서
rm src/main/java/com/energyfactory/energy_factory/config/WebConfig.java
```

**이유:**
- SecurityConfig만으로 CORS 처리 충분
- 두 개의 CORS 설정이 충돌하지 않도록 함

### 2단계: SecurityConfig 환경변수 사용

```java
@Value("${app.cors.allowed-origins}")
private String allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    String[] origins = allowedOrigins.split(",");
    configuration.setAllowedOrigins(Arrays.asList(origins));
    // ...
}
```

### 3단계: .env 파일 수정 (EC2)

```bash
# 공백 제거
ALLOWED_ORIGINS=https://energy-factory.kr,https://www.energy-factory.kr,https://d1o0ytu060swr1.cloudfront.net,http://localhost:5173,http://localhost:3000
```

### 4단계: CloudFront Error Pages 삭제

**AWS Console → CloudFront → Error Pages 탭**
- 설정된 모든 Error Page 규칙 삭제
- 이제 에러 발생 시 Origin의 에러 응답을 그대로 전달

### 5단계: CloudFront 캐시 무효화

**Invalidations 탭:**
- 객체 경로: `/*`
- 생성 → 완료 대기

### 6단계: 빌드 및 재시작

```bash
# EC2
./gradlew clean build -x test
cp build/libs/energy-factory-0.0.1-SNAPSHOT.jar ./
sudo systemctl restart energy-factory
```

---

## 결과 확인

### 테스트

```bash
curl -X POST https://energy-factory.kr/api/payments/toss/confirm \
  -H "Origin: http://localhost:5173" \
  -H "Content-Type: application/json" \
  -d '{"paymentKey":"test","orderId":"123","amount":1000}' \
  -v 2>&1 | grep -i "access-control"
```

**성공:**
```
< access-control-allow-origin: http://localhost:5173  ✅
< access-control-allow-credentials: true  ✅
```

### 브라우저 테스트
- CORS 에러 사라짐 ✅
- 결제 요청 정상 전달 ✅
- 404 에러는 비즈니스 로직 문제 (테스트 데이터 없음)

---

## 교훈

### 1. CORS는 여러 레이어에서 설정 가능
- Spring MVC (WebConfig)
- Spring Security (SecurityConfig)
- **충돌 방지를 위해 하나만 사용 권장**

### 2. CloudFront Error Pages 주의
- Error Pages는 Origin의 CORS 설정을 우회함
- S3 fallback 시 CORS 헤더 누락 가능
- 불필요한 Error Pages 설정 제거 권장

### 3. 캐시 문제 진단
- `x-cache` 헤더 확인
- `age` 헤더로 캐시 시간 확인
- `server` 헤더로 실제 응답 출처 확인

### 4. 디버깅 순서
1. 백엔드 직접 테스트 (curl localhost:8080)
2. EC2 공인 IP 테스트 (curl EC2-IP:8080)
3. CloudFront 통한 테스트 (curl https://domain)
4. 각 단계별 응답 헤더 비교

---

## 관련 파일

### 수정된 파일
- `src/main/java/com/energyfactory/energy_factory/config/SecurityConfig.java`
  - 환경변수 기반 CORS 설정

### 삭제된 파일
- `src/main/java/com/energyfactory/energy_factory/config/WebConfig.java`
  - CORS 충돌 제거

### 환경 설정
- `/home/ubuntu/energy_factory/.env` (EC2)
  - `ALLOWED_ORIGINS` 환경변수

### CloudFront 설정
- Error Pages: 모두 삭제
- Cache Policy: CachingDisabled (API 경로)
- Origin Request Policy: AllViewer
- Response Headers Policy: SimpleCORS

---

## 날짜
- 문제 발생: 2025-11-04
- 해결 완료: 2025-11-05
