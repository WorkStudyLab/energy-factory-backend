# CloudFront를 통한 프론트엔드/백엔드 도메인 통합 가이드

## 📋 배경 및 문제점

### 기존 아키텍처
```
프론트엔드: http://energy-factory-frontend-20251001.s3-website.ap-northeast-2.amazonaws.com
백엔드:     http://13.209.24.80:8080
```

### 문제
- **서로 다른 도메인**으로 인한 쿠키 공유 불가
- **Same-Origin Policy** 위반으로 쿠키 기반 인증 실패
- CORS 설정만으로는 해결 불가 (쿠키는 same-site만 전송됨)

### 해결 방안
**CloudFront를 통한 단일 도메인 통합**

---

## 🎯 새로운 아키텍처

```
CloudFront Distribution (단일 도메인)
https://d1o0ytu060swr1.cloudfront.net

├── /            → S3 (프론트엔드)
└── /api/*       → EC2 (백엔드)
```

### 장점
- ✅ 단일 도메인으로 쿠키 자동 전송
- ✅ HTTPS 무료 제공 (AWS Certificate Manager)
- ✅ CDN 캐싱으로 성능 향상
- ✅ CORS 이슈 최소화

---

## 🛠️ 구현 과정

### 1단계: CloudFront Distribution 생성

#### 1-1. 기본 설정
1. AWS 콘솔 → CloudFront → Create distribution
2. 설정:
   - **배포 이름**: `energy-factory-app`
   - **배포 유형**: 단일 웹사이트 또는 앱
   - **사용자 정의 도메인**: 비워둠 (CloudFront 기본 도메인 사용)

#### 1-2. Origin 1 - 프론트엔드 (S3)
```
Origin domain: energy-factory-frontend-20251001.s3-website.ap-northeast-2.amazonaws.com
Protocol: HTTP only (S3 website endpoint는 HTTP만 지원)
Origin path: 비워둠
Name: S3-Frontend
```

#### 1-3. 기본 Behavior 설정 (프론트엔드용)
```
Path pattern: Default (*)
Cache policy: CachingOptimized
Viewer protocol: Redirect HTTP to HTTPS
Allowed HTTP methods: GET, HEAD, OPTIONS
```

#### 1-4. 보안 설정
```
WAF: 비활성화 (개발/테스트용, 나중에 추가 가능)
```

#### 1-5. 추가 설정
```
기본 루트 객체: index.html
Price class: 필요에 따라 선택
```

---

### 2단계: 백엔드 Origin 및 Behavior 추가

Distribution 생성 완료 후:

#### 2-1. Origin 2 - 백엔드 (EC2) 추가
1. CloudFront → Origins 탭 → Create origin
2. 설정:
```
Origin domain: ec2-13-209-24-80.ap-northeast-2.compute.amazonaws.com
Protocol: HTTP only
HTTP port: 8080
HTTPS port: 443
Origin path: 비워둠
Name: Backend-API
```

#### 2-2. Behavior 추가 (/api/* 경로)
1. CloudFront → Behaviors 탭 → Create behavior
2. 설정:
```
Path pattern: /api/*
Origin: Backend-API
Viewer protocol: Redirect HTTP to HTTPS
Allowed HTTP methods: GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE (모든 메서드)
Cache policy: CachingDisabled ⚠️ 중요!
Origin request policy: AllViewer ⚠️ 중요! (쿠키, 헤더, 쿼리스트링 전달)
Response headers policy: None
```

#### 2-3. Behavior 우선순위 확인
Behaviors 탭에서 확인:
```
우선순위 0: /api/*        → Backend-API
우선순위 1: Default (*)   → S3-Frontend
```

순서가 잘못되었다면 `/api/*` 선택 후 "Move up" 클릭

---

### 3단계: Error Pages 설정 (React SPA)

React는 클라이언트 사이드 라우팅을 사용하므로 404/403 에러를 index.html로 리다이렉트

1. CloudFront → Error pages 탭 → Create custom error response

#### 404 에러 설정
```
HTTP error code: 404
Customize error response: Yes
Response page path: /index.html
HTTP response code: 200
```

#### 403 에러 설정
```
HTTP error code: 403
Customize error response: Yes
Response page path: /index.html
HTTP response code: 200
```

---

### 4단계: 백엔드 코드 수정

#### 4-1. SecurityConfig.java
**파일 경로**: `src/main/java/com/energyfactory/energy_factory/config/SecurityConfig.java`

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
            "https://d1o0ytu060swr1.cloudfront.net",  // CloudFront 도메인
            "http://localhost:3000",
            "http://localhost:5173"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

#### 4-2. LoginFilter.java
**파일 경로**: `src/main/java/com/energyfactory/energy_factory/jwt/LoginFilter.java`

```java
private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);  // JavaScript 접근 방지 (XSS 방어)
    cookie.setSecure(true);    // HTTPS에서만 쿠키 전송 (프로덕션 환경)
    cookie.setPath("/");       // 모든 경로에서 접근 가능
    cookie.setMaxAge(maxAge);  // 쿠키 만료 시간 (초)
    cookie.setAttribute("SameSite", "None");  // Cross-site 쿠키 허용 (Secure=true 필요)

    response.addCookie(cookie);
}
```

#### 4-3. OAuth2SuccessHandler.java
**파일 경로**: `src/main/java/com/energyfactory/energy_factory/handler/OAuth2SuccessHandler.java`

```java
private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);  // JavaScript 접근 방지 (XSS 방어)
    cookie.setSecure(true);    // HTTPS에서만 쿠키 전송 (프로덕션 환경)
    cookie.setPath("/");       // 모든 경로에서 접근 가능
    cookie.setMaxAge(maxAge);  // 쿠키 만료 시간 (초)
    cookie.setAttribute("SameSite", "None");  // Cross-site 쿠키 허용 (Secure=true 필요)

    response.addCookie(cookie);
}
```

#### 4-4. application-prod.yml
**파일 경로**: `src/main/resources/application-prod.yml`

```yaml
# OAuth2 리다이렉트 URL 설정
app:
  oauth2:
    redirect-url: ${FRONTEND_URL:https://d1o0ytu060swr1.cloudfront.net}
  cookie:
    secure: true  # 프로덕션 환경 HTTPS
  cors:
    allowed-origins: ${ALLOWED_ORIGINS:https://d1o0ytu060swr1.cloudfront.net}
```

#### 4-5. 불필요한 프로파일 정리
```bash
# application-dev.yml 삭제 (local/prod만 사용)
rm src/main/resources/application-dev.yml

# 프로파일 구성:
# - local: 로컬 개발 (localhost, HTTP, Secure=false)
# - prod: EC2 프로덕션 (CloudFront, HTTPS, Secure=true)
```

---

### 5단계: EC2 서버 설정

#### 5-1. systemd 서비스 파일 수정
```bash
# EC2 접속
ssh -i energyFactory.pem ubuntu@13.209.24.80

# 서비스 파일 수정
sudo nano /etc/systemd/system/energy-factory.service
```

**수정 내용**:
```ini
# 변경 전
ExecStart=/usr/bin/java -jar energy-factory-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# 변경 후
ExecStart=/usr/bin/java -jar energy-factory-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### 5-2. 서비스 재시작
```bash
# systemd 설정 리로드
sudo systemctl daemon-reload

# 서비스 재시작
sudo systemctl restart energy-factory

# 상태 확인
sudo systemctl status energy-factory

# 로그 확인
sudo journalctl -u energy-factory -f
```

#### 5-3. 빠른 수정 (한 줄 명령어)
```bash
sudo systemctl stop energy-factory && \
sudo sed -i 's/--spring.profiles.active=dev/--spring.profiles.active=prod/g' /etc/systemd/system/energy-factory.service && \
sudo systemctl daemon-reload && \
sudo systemctl start energy-factory && \
sudo systemctl status energy-factory
```

---

### 6단계: 프론트엔드 코드 수정

#### 6-1. API baseURL 변경

**방법 1: 상대 경로 사용 (권장)**
```javascript
// .env.production 또는 설정 파일
REACT_APP_API_BASE_URL=/api

// API 호출 예시
const response = await fetch('/api/products');
```

**방법 2: 절대 경로 사용**
```javascript
// .env.production
REACT_APP_API_BASE_URL=https://d1o0ytu060swr1.cloudfront.net/api

// API 호출 예시
const response = await fetch(`${process.env.REACT_APP_API_BASE_URL}/products`);
```

#### 6-2. Axios 사용 시
```javascript
// src/utils/axios.js 또는 src/config/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: '/api',  // 상대 경로 사용
  withCredentials: true,  // 쿠키 전송 활성화 (중요!)
});

export default api;
```

#### 6-3. S3 재배포
```bash
# 프론트엔드 빌드
npm run build

# S3 업로드
aws s3 sync build/ s3://energy-factory-frontend-20251001/ --delete

# CloudFront 캐시 무효화
aws cloudfront create-invalidation \
  --distribution-id E21IZFYCYIBGRW \
  --paths "/*"
```

---

## 📊 최종 결과

### 변경 전
```
프론트엔드: http://s3-bucket.s3-website.ap-northeast-2.amazonaws.com
백엔드:     http://13.209.24.80:8080/api/*
문제:       쿠키 전송 불가 (다른 도메인)
```

### 변경 후
```
통합 도메인: https://d1o0ytu060swr1.cloudfront.net
프론트엔드: https://d1o0ytu060swr1.cloudfront.net/
백엔드:     https://d1o0ytu060swr1.cloudfront.net/api/*
결과:       쿠키 자동 전송 (같은 도메인) ✅
```

---

## ✅ 테스트 체크리스트

- [ ] CloudFront 도메인 접속: `https://d1o0ytu060swr1.cloudfront.net`
- [ ] 프론트엔드 정상 로드 확인
- [ ] API 호출 테스트: `https://d1o0ytu060swr1.cloudfront.net/api/products`
- [ ] 로그인 기능 테스트
- [ ] 쿠키 전송 확인 (브라우저 개발자 도구 → Application → Cookies)
- [ ] 소셜 로그인 테스트 (Naver OAuth)
- [ ] CORS 에러 없음 확인

---

## 🔧 트러블슈팅

### 504 Gateway Timeout

**원인**: CloudFront가 백엔드 EC2에 연결하지 못함

**해결 방법**:
1. EC2 서버 상태 확인
```bash
sudo systemctl status energy-factory
```

2. EC2 보안 그룹 확인
   - 인바운드 규칙에 8080 포트 열림 확인
   - 소스: 0.0.0.0/0 또는 CloudFront IP 범위

3. CloudFront Origin 설정 확인
   - Origin domain이 올바른지 확인
   - Protocol과 Port 확인 (HTTP, 8080)

4. 백엔드 로그 확인
```bash
sudo journalctl -u energy-factory -f
tail -f /home/ubuntu/energy_factory/app.log
```

---

### 쿠키가 전송되지 않음

**원인**: 쿠키 보안 설정 문제

**해결 방법**:
1. `cookie.setSecure(true)` 확인 (HTTPS 필수)
2. `cookie.setAttribute("SameSite", "None")` 확인
3. 브라우저 개발자 도구에서 쿠키 확인
   - Application → Cookies → `https://d1o0ytu060swr1.cloudfront.net`
   - `accessToken`, `refreshToken` 쿠키 존재 확인
   - Secure, HttpOnly, SameSite 속성 확인

---

### CORS 에러

**원인**: SecurityConfig의 allowedOrigins 설정 누락

**해결 방법**:
1. SecurityConfig.java 확인
```java
configuration.setAllowedOrigins(Arrays.asList(
    "https://d1o0ytu060swr1.cloudfront.net",  // 반드시 포함
    "http://localhost:3000"
));
```

2. `allowCredentials(true)` 설정 확인
3. 백엔드 재시작 후 테스트

---

### Failed to configure a DataSource

**원인**: EC2에서 잘못된 프로파일(dev) 실행 중

**해결 방법**:
1. systemd 서비스 파일 확인
```bash
cat /etc/systemd/system/energy-factory.service | grep ExecStart
```

2. `--spring.profiles.active=prod`로 변경
```bash
sudo nano /etc/systemd/system/energy-factory.service
sudo systemctl daemon-reload
sudo systemctl restart energy-factory
```

3. `.env` 파일에 DB 환경변수 확인
```bash
cat /home/ubuntu/energy_factory/.env
```

---

## 📝 Git Commit 예시

```bash
git add -A
git commit -m "feat: CloudFront 도메인 통합 및 쿠키 기반 인증 설정

- CloudFront 배포를 통한 프론트엔드/백엔드 도메인 통일
- CORS 설정을 CloudFront 도메인으로 변경 (https://d1o0ytu060swr1.cloudfront.net)
- 쿠키 보안 설정 강화 (Secure=true, SameSite=None)
- application-dev.yml 삭제 (local/prod 프로파일로 통일)

변경사항:
- SecurityConfig.java: CORS allowed origins에 CloudFront 도메인 추가
- LoginFilter.java: 쿠키 Secure=true, SameSite=None 설정
- OAuth2SuccessHandler.java: 쿠키 Secure=true, SameSite=None 설정
- application-prod.yml: CloudFront 도메인 및 HTTPS 설정
- application-dev.yml: 삭제 (불필요한 프로파일 제거)

프로파일 구성:
- local: 로컬 개발 환경 (localhost, HTTP)
- prod: EC2 프로덕션 환경 (CloudFront, HTTPS)"

git push
```

---

## 🎯 핵심 포인트

### CloudFront 설정
1. **단일 도메인 통합**: 프론트엔드와 백엔드를 같은 도메인으로 제공
2. **Behavior 우선순위**: `/api/*`가 Default보다 먼저 매칭되어야 함
3. **캐시 정책**:
   - API (Backend): `CachingDisabled` (동적 콘텐츠)
   - Frontend (S3): `CachingOptimized` (정적 콘텐츠)
4. **Origin Request Policy**: `AllViewer` (쿠키, 헤더, 쿼리스트링 모두 전달)

### 쿠키 설정
1. **Secure=true**: HTTPS에서만 쿠키 전송
2. **HttpOnly=true**: JavaScript 접근 차단 (XSS 방어)
3. **SameSite=None**: Cross-site 요청에서도 쿠키 전송 (Secure=true 필수)
4. **Path=/**: 모든 경로에서 쿠키 접근 가능

### 프로파일 관리
1. **local**: 로컬 개발 환경 (localhost:3000, HTTP, Secure=false)
2. **prod**: EC2 프로덕션 환경 (CloudFront, HTTPS, Secure=true)
3. **dev 프로파일 삭제**: 혼란 방지 및 관리 간소화

---

## 📌 참고 정보

**CloudFront 도메인**: `https://d1o0ytu060swr1.cloudfront.net`
**Distribution ID**: `E21IZFYCYIBGRW`
**프론트엔드 S3 버킷**: `energy-factory-frontend-20251001`
**백엔드 EC2 IP**: `13.209.24.80:8080`

**배포 완료 시간**: 2025-10-21
**작성자**: Energy Factory Team

---

## 🔗 관련 문서

- [AWS CloudFront 공식 문서](https://docs.aws.amazon.com/cloudfront/)
- [Cookie SameSite 속성 가이드](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie/SameSite)
- [CORS 설정 가이드](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
