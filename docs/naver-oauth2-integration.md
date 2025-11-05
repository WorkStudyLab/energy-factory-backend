# 네이버 OAuth2 소셜 로그인 구현 가이드

> 작성일: 2025-11-05
> 작성자: Backend Team

## 목차
1. [개요](#개요)
2. [아키텍처](#아키텍처)
3. [구현 상세](#구현-상세)
4. [API 명세](#api-명세)
5. [프론트엔드 연동](#프론트엔드-연동)
6. [보안](#보안)
7. [테스트 시나리오](#테스트-시나리오)

---

## 개요

### 목적
네이버 계정을 통한 간편 로그인 기능을 제공하여 사용자 경험을 개선하고 회원가입 전환율을 높입니다.

### 주요 기능
- 네이버 계정으로 간편 회원가입
- 기존 사용자 자동 로그인
- LOCAL 계정과 네이버 계정 자동 연동
- 사용자 정보 완성도 기반 리다이렉트

### 기술 스택
- Spring Security OAuth2 Client
- JWT (Access Token + Refresh Token)
- Redis (Refresh Token 저장)
- HttpOnly 쿠키 (보안 강화)

---

## 아키텍처

### 전체 플로우

```
[프론트엔드]
   ↓ GET /api/oauth2/naver

[OAuth2Controller]
   ↓ Redirect → /oauth2/authorization/naver

[Spring Security OAuth2]
   ↓ Redirect → 네이버 로그인 페이지

[네이버]
   ↓ 사용자 인증 후 콜백
   ↓ /login/oauth2/code/naver

[CustomOAuth2UserService]
   ↓ loadUser() 호출
   ↓ 네이버 사용자 정보 조회
   ↓ DB 처리:
   │  ├─ 신규 사용자 → 자동 회원가입
   │  ├─ 기존 OAuth2 사용자 → 정보 업데이트
   │  └─ LOCAL 사용자 → 네이버 계정 연동

[OAuth2SuccessHandler]
   ↓ JWT 토큰 생성 (Access + Refresh)
   ↓ Redis에 Refresh Token 저장
   ↓ HttpOnly 쿠키로 토큰 전달
   ↓ 사용자 정보 확인:
   │  ├─ 생년월일 ❌ OR 주소 ❌ → /signup
   │  └─ 생년월일 ✅ AND 주소 ✅ → /products

[프론트엔드]
   ↓ 리다이렉트된 페이지 로드
   ↓ GET /api/users/profile (쿠키의 JWT로 자동 인증)
   ↓ 네이버 정보 렌더링
   ↓ (필요 시) PUT /api/users/additional-info
   ↓ 회원가입/로그인 완료
```

### 컴포넌트 구성

```
┌─────────────────────────────────────────┐
│         OAuth2Controller.java           │  ← Swagger 문서화 및 엔드포인트
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│      Spring Security OAuth2 Client      │  ← OAuth2 자동 처리
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│    CustomOAuth2UserService.java         │  ← 사용자 정보 처리
│    - NaverUserInfo.java                 │  ← 네이버 응답 파싱
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│     OAuth2SuccessHandler.java           │  ← 인증 성공 처리
│     - JWT 토큰 생성                      │
│     - 리다이렉트 URL 결정                │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         UserController.java             │  ← 기존 사용자 API
│    - GET /api/users/profile             │
│    - PUT /api/users/additional-info     │
└─────────────────────────────────────────┘
```

---

## 구현 상세

### 1. OAuth2Controller

**파일**: `src/main/java/com/energyfactory/energy_factory/controller/OAuth2Controller.java`

```java
@RestController
@RequestMapping("/api/oauth2")
@Tag(name = "OAuth2 소셜 로그인")
public class OAuth2Controller {

    @GetMapping("/naver")
    public void naverLogin(HttpServletResponse response) throws IOException {
        // Spring Security OAuth2가 실제 처리
        // 이 메서드는 Swagger 문서화 목적
        response.sendRedirect("/oauth2/authorization/naver");
    }
}
```

**역할**:
- 프론트엔드가 호출할 명확한 엔드포인트 제공
- Swagger UI에 API 문서 표시
- `/oauth2/authorization/naver`로 리다이렉트

### 2. CustomOAuth2UserService

**파일**: `src/main/java/com/energyfactory/energy_factory/service/CustomOAuth2UserService.java`

**주요 로직**:

#### (1) 네이버 사용자 정보 파싱
```java
if (registrationId.equals("naver")) {
    Map<String, Object> response = oAuth2User.getAttribute("response");
    oAuth2UserInfo = new NaverUserInfo(response);
}
```

#### (2) 사용자 처리 분기

**Case 1: 기존 OAuth2 사용자**
```java
Optional<User> optionalUser = userRepository.findByProviderAndProviderId(providerEnum, providerId);
if (optionalUser.isPresent()) {
    // 정보 업데이트 (이름, 이메일, 전화번호)
    user = optionalUser.get();
    user.setName(name);
    user.setEmail(email);
    user.setPhoneNumber(phoneNumber);
    userRepository.save(user);
}
```

**Case 2: LOCAL 계정이 있는 경우**
```java
Optional<User> existingUserByEmail = userRepository.findByEmail(email);
if (existingUserByEmail.isPresent() && existingUser.getProvider() == Provider.LOCAL) {
    // OAuth2 정보 추가하여 계정 연동
    existingUser.setProvider(providerEnum);
    existingUser.setProviderId(providerId);
    userRepository.save(existingUser);
}
```

**Case 3: 완전히 새로운 사용자**
```java
user = User.builder()
    .email(email)
    .name(name)
    .password(passwordEncoder.encode("OAUTH_USER"))
    .phoneNumber(phoneNumber)
    .provider(providerEnum)
    .providerId(providerId)
    .role(Role.USER)
    .build();
userRepository.save(user);
```

### 3. OAuth2SuccessHandler

**파일**: `src/main/java/com/energyfactory/energy_factory/handler/OAuth2SuccessHandler.java`

**주요 개선 사항**:

#### (1) JWT 토큰 생성
```java
// Access Token (10분)
String accessToken = jwtUtil.createAccessToken(userId, username, role, 10 * 60 * 1000L);

// Refresh Token (7일)
String refreshToken = jwtUtil.createRefreshToken(userId, username, refreshTokenExpiration);

// Redis에 저장
refreshTokenService.saveRefreshToken(username, refreshToken);
```

#### (2) HttpOnly 쿠키 저장
```java
private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);   // XSS 방어
    cookie.setSecure(true);     // HTTPS only
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    cookie.setAttribute("SameSite", "None");
    response.addCookie(cookie);
}
```

#### (3) 리다이렉트 URL 결정 (신규 기능!)
```java
private String determineRedirectUrl(CustomUserDetails customUserDetails) {
    boolean hasBirthDate = customUserDetails.getUser().getBirthDate() != null;
    boolean hasAddress = customUserDetails.getUser().getAddress() != null
                      && !customUserDetails.getUser().getAddress().trim().isEmpty();

    if (hasBirthDate && hasAddress) {
        // 기존 회원: 홈으로 이동
        return homeUrl;  // https://energy-factory.kr/products
    } else {
        // 신규 회원: 추가 정보 입력 화면으로
        return signupUrl;  // https://energy-factory.kr/signup
    }
}
```

### 4. NaverUserInfo DTO

**파일**: `src/main/java/com/energyfactory/energy_factory/dto/NaverUserInfo.java`

```java
public class NaverUserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    public String getMobile() {
        return (String) attributes.get("mobile");
    }
}
```

**네이버 응답 형식**:
```json
{
  "response": {
    "id": "12345678",
    "email": "hong@naver.com",
    "name": "홍길동",
    "mobile": "010-1234-5678"
  }
}
```

---

## API 명세

### 1. 네이버 로그인 시작

**엔드포인트**: `GET /api/oauth2/naver`

**설명**: 네이버 OAuth2 로그인을 시작합니다.

**요청**:
```bash
GET https://energy-factory.kr/api/oauth2/naver
```

**응답**: `302 Found`
```
Location: https://nid.naver.com/oauth2.0/authorize?...
```

**프론트엔드 구현**:
```javascript
const handleNaverLogin = () => {
  window.location.href = 'https://energy-factory.kr/api/oauth2/naver';
};
```

### 2. 사용자 정보 조회

**엔드포인트**: `GET /api/users/profile`

**설명**: 현재 로그인한 사용자의 정보를 조회합니다. JWT는 쿠키에서 자동으로 추출됩니다.

**요청**:
```bash
GET https://energy-factory.kr/api/users/profile
Cookie: accessToken=eyJhbGci...
```

**응답**: `200 OK`
```json
{
  "status": 200,
  "code": "20000000",
  "desc": "성공",
  "data": {
    "name": "홍길동",
    "email": "hong@naver.com",
    "phone": "010-1234-5678",
    "birthDate": null,
    "authProvider": "naver",
    "memberSince": "2025-11-05",
    "address": null
  }
}
```

**프론트엔드 구현**:
```javascript
useEffect(() => {
  fetch('https://energy-factory.kr/api/users/profile', {
    credentials: 'include' // 쿠키 자동 포함
  })
  .then(res => res.json())
  .then(data => {
    setUserData(data.data);
  });
}, []);
```

### 3. 추가 정보 입력

**엔드포인트**: `PUT /api/users/additional-info`

**설명**: 소셜 로그인 후 부족한 정보(생년월일, 주소)를 추가합니다.

**요청**:
```bash
PUT https://energy-factory.kr/api/users/additional-info
Cookie: accessToken=eyJhbGci...
Content-Type: application/json

{
  "birthday": "1990-01-01",
  "address": "서울특별시 강남구 테헤란로 123",
  "detailAddress": "ABC빌딩 5층"
}
```

**응답**: `200 OK`
```json
{
  "status": 200,
  "code": "20000000",
  "desc": "성공",
  "data": {
    "name": "홍길동",
    "email": "hong@naver.com",
    "phone": "010-1234-5678",
    "birthDate": "1990-01-01",
    "authProvider": "naver",
    "memberSince": "2025-11-05",
    "address": "서울특별시 강남구 테헤란로 123"
  }
}
```

**프론트엔드 구현**:
```javascript
const handleSubmit = async (e) => {
  e.preventDefault();

  await fetch('https://energy-factory.kr/api/users/additional-info', {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      birthday: birthday,
      address: address,
      detailAddress: detailAddress
    })
  });

  navigate('/products');
};
```

---

## 프론트엔드 연동

### 1. 네이버 로그인 버튼

```javascript
// 로그인 페이지 또는 메인 페이지
import { useNavigate } from 'react-router-dom';

function LoginPage() {
  const handleNaverLogin = () => {
    window.location.href = 'https://energy-factory.kr/api/oauth2/naver';
  };

  return (
    <div>
      <button onClick={handleNaverLogin}>
        <img src="/naver-logo.png" alt="네이버" />
        네이버로 시작하기
      </button>
    </div>
  );
}
```

### 2. 추가 정보 입력 페이지 (/signup)

```javascript
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function SignupPage() {
  const navigate = useNavigate();
  const [userData, setUserData] = useState(null);
  const [birthday, setBirthday] = useState('');
  const [address, setAddress] = useState('');
  const [detailAddress, setDetailAddress] = useState('');
  const [loading, setLoading] = useState(true);

  // 페이지 로드 시 사용자 정보 가져오기
  useEffect(() => {
    fetch('https://energy-factory.kr/api/users/profile', {
      credentials: 'include'
    })
    .then(res => {
      if (!res.ok) {
        // 인증 실패 시 로그인 페이지로
        navigate('/login');
        return;
      }
      return res.json();
    })
    .then(data => {
      if (data) {
        setUserData(data.data);
        setLoading(false);
      }
    })
    .catch(err => {
      console.error('사용자 정보 조회 실패:', err);
      navigate('/login');
    });
  }, [navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch('https://energy-factory.kr/api/users/additional-info', {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          birthday: birthday,
          address: address,
          detailAddress: detailAddress
        })
      });

      if (response.ok) {
        // 성공 시 제품 페이지로 이동
        navigate('/products');
      } else {
        alert('추가 정보 입력에 실패했습니다.');
      }
    } catch (err) {
      console.error('추가 정보 입력 실패:', err);
      alert('오류가 발생했습니다.');
    }
  };

  if (loading) {
    return <div>로딩 중...</div>;
  }

  return (
    <div className="signup-page">
      <h2>추가 정보 입력</h2>
      <p>회원가입을 완료하기 위해 추가 정보를 입력해주세요.</p>

      <form onSubmit={handleSubmit}>
        {/* 네이버에서 가져온 정보 (읽기 전용) */}
        <div className="form-group">
          <label>이름</label>
          <input
            type="text"
            value={userData?.name || ''}
            disabled
            className="disabled-input"
          />
        </div>

        <div className="form-group">
          <label>이메일</label>
          <input
            type="email"
            value={userData?.email || ''}
            disabled
            className="disabled-input"
          />
        </div>

        <div className="form-group">
          <label>전화번호</label>
          <input
            type="tel"
            value={userData?.phone || ''}
            disabled
            className="disabled-input"
          />
        </div>

        <hr />

        {/* 사용자가 입력할 추가 정보 */}
        <div className="form-group">
          <label htmlFor="birthday">생년월일 *</label>
          <input
            id="birthday"
            type="date"
            value={birthday}
            onChange={(e) => setBirthday(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="address">주소 *</label>
          <input
            id="address"
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="서울특별시 강남구 테헤란로 123"
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="detailAddress">상세 주소</label>
          <input
            id="detailAddress"
            type="text"
            value={detailAddress}
            onChange={(e) => setDetailAddress(e.target.value)}
            placeholder="ABC빌딩 5층"
          />
        </div>

        <button type="submit" className="btn-primary">
          회원가입 완료
        </button>
      </form>
    </div>
  );
}

export default SignupPage;
```

### 3. 제품 페이지 (/products)

```javascript
// 기존 회원은 이 페이지로 바로 리다이렉트됩니다.
// 쿠키에 JWT가 있으므로 인증된 상태입니다.

function ProductsPage() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    // 사용자 정보 확인 (선택사항)
    fetch('https://energy-factory.kr/api/users/profile', {
      credentials: 'include'
    })
    .then(res => res.json())
    .then(data => {
      setUser(data.data);
    });
  }, []);

  return (
    <div>
      <h1>제품 목록</h1>
      {user && <p>환영합니다, {user.name}님!</p>}
      {/* 제품 목록... */}
    </div>
  );
}
```

---

## 보안

### 1. JWT 토큰 보안

**HttpOnly 쿠키 사용**:
```java
cookie.setHttpOnly(true);   // JavaScript 접근 방지 (XSS 방어)
cookie.setSecure(true);     // HTTPS에서만 전송
cookie.setAttribute("SameSite", "None");  // Cross-site 허용
```

**장점**:
- XSS 공격 방지: JavaScript로 토큰 탈취 불가
- CSRF 토큰과 함께 사용하면 안전
- 브라우저가 자동으로 쿠키 관리

### 2. IDOR 방지

JWT에 `userId`를 포함하여 사용자 ID를 URL에서 받지 않음:

```java
// ❌ 취약: URL에서 userId를 받음
@GetMapping("/users/{userId}")
public User getUser(@PathVariable Long userId) { ... }

// ✅ 안전: JWT에서 userId 추출
@GetMapping("/users/me")
public User getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
    Long userId = userDetails.getUser().getId();
    ...
}
```

### 3. 계정 보호

**중복 이메일 처리**:
```java
// 이미 다른 소셜 로그인으로 가입된 이메일인 경우
if (existingUser.getProvider() != Provider.LOCAL) {
    throw new OAuth2AuthenticationException(
        "이미 " + existingUser.getProvider().getDescription() + "로 가입된 이메일입니다."
    );
}
```

**LOCAL 계정과 자동 연동**:
```java
// LOCAL 계정이 있는 경우 네이버 계정과 연동
if (existingUser.getProvider() == Provider.LOCAL) {
    existingUser.setProvider(providerEnum);
    existingUser.setProviderId(providerId);
    userRepository.save(existingUser);
}
```

### 4. CORS 설정

**application-prod.yml**:
```yaml
app:
  cors:
    allowed-origins: https://energy-factory.kr,https://www.energy-factory.kr,https://d1o0ytu060swr1.cloudfront.net
```

**SecurityConfig.java**:
```java
http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

---

## 테스트 시나리오

### 시나리오 1: 신규 사용자 회원가입

**Given**: 네이버 계정이 있지만 서비스에 처음 가입하는 사용자

**When**:
1. 네이버 로그인 버튼 클릭
2. 네이버 로그인 및 권한 승인

**Then**:
1. `/signup` 페이지로 리다이렉트
2. 네이버 정보 표시 (이름, 이메일, 전화번호)
3. 추가 정보 입력 (생년월일, 주소)
4. 회원가입 완료 후 `/products`로 이동

**검증**:
```sql
SELECT * FROM user WHERE email = 'hong@naver.com';
-- provider: NAVER
-- provider_id: 12345678
-- birth_date: 1990-01-01
-- address: 서울특별시 강남구...
```

### 시나리오 2: 기존 사용자 로그인

**Given**: 이미 네이버로 가입한 사용자

**When**:
1. 네이버 로그인 버튼 클릭
2. 네이버 로그인 및 권한 승인

**Then**:
1. `/products` 페이지로 바로 리다이렉트
2. 추가 정보 입력 없이 바로 로그인 완료

**검증**:
```bash
curl -b cookies.txt https://energy-factory.kr/api/users/profile
# 응답: 사용자 정보 (생년월일, 주소 포함)
```

### 시나리오 3: LOCAL 계정 연동

**Given**:
- 이메일/비밀번호로 가입한 사용자 (LOCAL)
- 같은 이메일의 네이버 계정

**When**:
1. 네이버 로그인 버튼 클릭
2. 네이버 로그인 (이메일: hong@naver.com)

**Then**:
1. 기존 LOCAL 계정이 네이버 계정으로 업그레이드
2. 생년월일과 주소가 있으면 `/products`로
3. 생년월일과 주소가 없으면 `/signup`로

**검증**:
```sql
SELECT * FROM user WHERE email = 'hong@naver.com';
-- provider: NAVER (LOCAL → NAVER로 변경됨)
-- provider_id: 12345678 (새로 추가됨)
```

### 시나리오 4: 중복 소셜 계정

**Given**:
- 이미 카카오로 가입한 이메일 (hong@naver.com)
- 같은 이메일의 네이버 계정

**When**:
1. 네이버 로그인 버튼 클릭
2. 네이버 로그인 (이메일: hong@naver.com)

**Then**:
1. 에러 메시지: "이미 KAKAO로 가입된 이메일입니다."
2. 로그인 실패

**검증**:
```bash
# 카카오 계정 유지
SELECT provider FROM user WHERE email = 'hong@naver.com';
-- provider: KAKAO (변경되지 않음)
```

### 시나리오 5: JWT 쿠키 검증

**Given**: 네이버 로그인 완료

**When**:
1. 브라우저 개발자 도구 열기
2. Application → Cookies 확인

**Then**:
```
Name: accessToken
Value: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
HttpOnly: ✅ true
Secure: ✅ true
SameSite: None
Max-Age: 600 (10분)

Name: refreshToken
Value: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
HttpOnly: ✅ true
Secure: ✅ true
SameSite: None
Max-Age: 604800 (7일)
```

### 시나리오 6: 프론트엔드 연동 테스트

**Step 1**: 네이버 로그인
```javascript
window.location.href = 'https://energy-factory.kr/api/oauth2/naver';
```

**Step 2**: `/signup` 페이지에서 사용자 정보 조회
```javascript
const response = await fetch('https://energy-factory.kr/api/users/profile', {
  credentials: 'include'
});
const data = await response.json();

console.log(data.data.name);  // "홍길동"
console.log(data.data.email); // "hong@naver.com"
console.log(data.data.phone); // "010-1234-5678"
```

**Step 3**: 추가 정보 입력
```javascript
const response = await fetch('https://energy-factory.kr/api/users/additional-info', {
  method: 'PUT',
  credentials: 'include',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    birthday: '1990-01-01',
    address: '서울특별시 강남구 테헤란로 123',
    detailAddress: 'ABC빌딩 5층'
  })
});

console.log(response.ok); // true
```

---

## 설정 파일

### 로컬 환경 (application-local.yml)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          naver:
            client-id: bYUxnsY44vphTM7xjIIg
            client-secret: ZSPcMkuT_z
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            authorization-grant-type: authorization_code
            scope: name,email,mobile
            client-name: Naver
        provider:
          naver:
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            token-uri: https://nid.naver.com/oauth2.0/token
            user-info-uri: https://openapi.naver.com/v1/nid/me
            user-name-attribute: response

app:
  oauth2:
    signup-url: http://localhost:5173/signup
    home-url: http://localhost:5173/products
  cookie:
    secure: false  # 로컬 환경 HTTP
```

### 프로덕션 환경 (application-prod.yml)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          naver:
            client-id: ${NAVER_CLIENT_ID}
            client-secret: ${NAVER_CLIENT_SECRET}
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            authorization-grant-type: authorization_code
            scope: name,email,mobile
            client-name: Naver
        provider:
          naver:
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            token-uri: https://nid.naver.com/oauth2.0/token
            user-info-uri: https://openapi.naver.com/v1/nid/me
            user-name-attribute: response

app:
  oauth2:
    signup-url: ${FRONTEND_SIGNUP_URL:https://energy-factory.kr/signup}
    home-url: ${FRONTEND_HOME_URL:https://energy-factory.kr/products}
  cookie:
    secure: true  # 프로덕션 환경 HTTPS
```

---

## 트러블슈팅

### 문제 1: 리다이렉트 후 쿠키가 전달되지 않음

**증상**: `/signup` 페이지에서 API 호출 시 401 Unauthorized

**원인**: CORS 설정 또는 쿠키 속성 문제

**해결**:
1. 프론트엔드: `credentials: 'include'` 확인
2. 백엔드: `SameSite=None` 및 `Secure=true` 확인
3. CORS 허용 오리진 확인

### 문제 2: 네이버 로그인 후 홈으로 이동해야 하는데 `/signup`로 이동

**증상**: 기존 회원인데 추가 정보 입력 화면으로 이동

**원인**: 생년월일 또는 주소가 DB에 없음

**해결**:
```sql
-- 사용자 정보 확인
SELECT birth_date, address FROM user WHERE email = 'hong@naver.com';

-- 필요 시 수동 업데이트
UPDATE user SET
  birth_date = '1990-01-01',
  address = '서울시 강남구'
WHERE email = 'hong@naver.com';
```

### 문제 3: "이미 다른 소셜 로그인으로 가입된 이메일입니다" 에러

**증상**: 같은 이메일로 여러 소셜 로그인 시도

**원인**: 의도된 동작 (보안)

**해결**: 사용자에게 기존 소셜 로그인 방법 사용 안내

### 문제 4: Swagger에서 네이버 로그인 API가 안 보임

**증상**: `/api/oauth2/naver` 엔드포인트가 Swagger UI에 표시되지 않음

**원인**:
1. OAuth2Controller가 없음
2. SecurityConfig에서 `/api/oauth2/**` 허용 안 됨

**해결**:
```java
// SecurityConfig.java
.requestMatchers("/api/oauth2/**").permitAll()
```

---

## 참고 자료

### 내부 문서
- [CORS 설정 가이드](./cors-troubleshooting.md)
- [비밀번호 재설정 구현](./password-reset.md)
- [CloudFront 통합](./cloudfront-integration.md)

### 외부 문서
- [네이버 로그인 API 문서](https://developers.naver.com/docs/login/api/)
- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [JWT 토큰 가이드](https://jwt.io/introduction)

### API 문서
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI 스펙: `http://localhost:8080/v3/api-docs`

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 2025-11-05 | 1.0.0 | 네이버 OAuth2 로그인 구현 및 문서 작성 | Backend Team |

---

## 라이선스

Copyright (c) 2025 Energy Factory. All rights reserved.
