# 네이버 OAuth2 소셜 로그인 구현 가이드

> 작성일: 2025-11-06
> 버전: 2.0.0
> 작성자: Backend Team

## 목차
1. [개요](#개요)
2. [아키텍처](#아키텍처)
3. [구현 상세](#구현-상세)
4. [API 명세](#api-명세)
5. [프론트엔드 연동](#프론트엔드-연동)
6. [보안](#보안)
7. [테스트 시나리오](#테스트-시나리오)
8. [트러블슈팅](#트러블슈팅)

---

## 개요

### 핵심 개념

**세션 기반 회원가입 방식**
- 신규 사용자는 네이버 로그인 후 **세션에 임시 저장**
- 사용자가 추가 정보 입력 후 **회원가입 완료 시점에 DB 저장**
- 중간에 이탈하면 DB에 저장되지 않음 (깔끔한 데이터 관리)

**계정 연동 기능**
- LOCAL 계정 사용자가 네이버 계정을 추가 연동 가능
- 하나의 계정으로 이메일/비밀번호 OR 네이버 둘 다 로그인 가능
- 보안: 로그인된 상태에서만 연동 가능

**자동 로그인**
- 일반 회원가입 시 JWT 자동 발급
- 네이버 회원가입 완료 시 JWT 자동 발급
- 회원가입 후 바로 서비스 이용 가능

### 주요 기능

1. **신규 사용자 네이버 가입**
   - 네이버 → 세션 저장 → 추가 정보 입력 → DB 저장 → JWT 발급

2. **기존 사용자 네이버 로그인**
   - 네이버 → providerId로 사용자 찾기 → JWT 발급

3. **계정 연동**
   - LOCAL 계정 → 네이버 연동 → providerId 추가 → 두 가지 방법으로 로그인 가능

4. **일반 회원가입 자동 로그인**
   - 이메일/비밀번호 회원가입 → JWT 자동 발급 → 네이버 연동 가능

### 기술 스택

- Spring Security OAuth2 Client
- JWT (Access Token + Refresh Token)
- Redis (Refresh Token 저장)
- HttpOnly 쿠키 (보안 강화)
- 세션 기반 임시 데이터 저장

---

## 아키텍처

### 플로우 1: 신규 사용자 네이버 가입

```
[사용자]
   ↓ 네이버 로그인 버튼 클릭
   ↓ GET /oauth2/authorization/naver

[Spring Security OAuth2]
   ↓ 네이버 로그인 페이지로 리다이렉트

[네이버]
   ↓ 사용자 인증 후 콜백
   ↓ GET /login/oauth2/code/naver

[CustomOAuth2UserService]
   ↓ loadUser() 호출
   ↓ 네이버 사용자 정보 조회 (email, name, phoneNumber, providerId)
   ↓ findByProviderId() 조회
   ↓ 없음 → 임시 User 객체 생성 (DB 저장 ❌)

[OAuth2SuccessHandler]
   ↓ userId == null 확인 (신규 사용자)
   ↓ OAuth2TempInfo 생성 (providerId, email, name, phoneNumber)
   ↓ 세션에 저장 (30분 유효)
   ↓ /signup?oauth=pending 리다이렉트

[프론트엔드 /signup]
   ↓ GET /api/auth/oauth-temp-info (세션에서 네이버 정보 조회)
   ↓ 네이버 정보 자동 완성 (email, name, phoneNumber)
   ↓ 사용자가 수정 + 추가 입력 (birthDate, address)
   ↓ POST /api/auth/signup-with-oauth (회원가입 완료)

[UserService.signupWithOAuth2()]
   ↓ 이메일 중복 체크
   ↓ 전화번호 중복 체크
   ↓ User 엔티티 생성 (requestDto의 모든 정보 사용)
   ↓ DB 저장 ✅
   ↓ JWT 토큰 발급 (Access + Refresh)
   ↓ 쿠키에 저장
   ↓ 세션 삭제
   ↓ 201 Created 응답
```

### 플로우 2: 기존 사용자 네이버 로그인

```
[사용자]
   ↓ 네이버 로그인 버튼 클릭

[CustomOAuth2UserService]
   ↓ findByProviderId() 조회
   ↓ 있음 → 기존 User 객체 반환 (DB 변경 ❌)

[OAuth2SuccessHandler]
   ↓ userId != null 확인 (기존 사용자)
   ↓ JWT 토큰 발급 (Access + Refresh)
   ↓ 쿠키에 저장
   ↓ /products 리다이렉트

[프론트엔드 /products]
   ↓ 쿠키의 JWT로 자동 인증
   ↓ 제품 페이지 렌더링
```

### 플로우 3: LOCAL 계정 네이버 연동

```
[사용자]
   ↓ 일반 회원가입 (POST /api/users/signup)
   ↓ JWT 자동 발급 ✅ (쿠키 저장)
   ↓ SignupConnectPage 도착 (이미 로그인된 상태)
   ↓ 네이버 연동 버튼 클릭

[AuthController]
   ↓ GET /api/auth/link/naver
   ↓ @AuthenticationPrincipal로 userId 확인
   ↓ 세션에 linkMode=true, linkUserId 저장
   ↓ /oauth2/authorization/naver 리다이렉트

[CustomOAuth2UserService]
   ↓ findByProviderId() 조회
   ↓ 없음 → 임시 User 객체 생성

[OAuth2SuccessHandler]
   ↓ 세션에서 linkMode=true 확인
   ↓ handleAccountLink() 호출
   ↓ UserService.linkNaverAccount() 호출

[UserService.linkNaverAccount()]
   ↓ 보안 체크:
   │  ├─ 이미 providerId가 있는지 확인 (재연동 방지)
   │  └─ providerId가 다른 사용자에게 사용 중인지 확인
   ↓ User.setProvider(NAVER)
   ↓ User.setProviderId(...)
   ↓ DB 저장 ✅
   ↓ /mypage?link=success 리다이렉트

[프론트엔드 /mypage]
   ↓ "네이버 계정 연동 완료!" 메시지 표시
```

### 컴포넌트 구성도

```
┌──────────────────────────────────────────┐
│          Spring Security OAuth2          │
│     /oauth2/authorization/naver          │
│     /login/oauth2/code/naver             │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│     CustomOAuth2UserService.java         │
│  - loadUser()                            │
│  - findByProviderId()                    │
│  - 임시 User 객체 생성 (DB 저장 ❌)        │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│      OAuth2SuccessHandler.java           │
│  - handleNewUser()      → 세션 저장      │
│  - handleExistingUser() → JWT 발급       │
│  - handleAccountLink()  → 계정 연동      │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│          AuthController.java             │
│  - GET  /api/auth/oauth-temp-info        │
│  - POST /api/auth/signup-with-oauth      │
│  - GET  /api/auth/link/naver             │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│           UserService.java               │
│  - signupWithOAuth2()                    │
│  - linkNaverAccount()                    │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│          UserController.java             │
│  - POST /api/users/signup (JWT 자동 발급) │
└──────────────────────────────────────────┘
```

---

## 구현 상세

### 1. OAuth2TempInfo DTO

**파일**: `src/main/java/com/energyfactory/energy_factory/dto/OAuth2TempInfo.java`

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2TempInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String provider;      // "NAVER"
    private String providerId;    // 네이버 고유 ID
    private String email;         // 네이버 이메일
    private String name;          // 네이버 이름
    private String phoneNumber;   // 네이버 전화번호
}
```

**역할**:
- 신규 사용자의 네이버 정보를 세션에 임시 저장
- DB에 저장하지 않고 사용자가 추가 정보 입력 후 회원가입 완료 시점에 저장
- 30분 세션 타임아웃

### 2. OAuth2SignupRequestDto

**파일**: `src/main/java/com/energyfactory/energy_factory/dto/OAuth2SignupRequestDto.java`

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2 회원가입 완료 요청 DTO")
public class OAuth2SignupRequestDto {

    @NotBlank @Email
    private String email;           // 네이버에서 자동 완성, 수정 가능

    @NotBlank
    private String name;            // 네이버에서 자동 완성, 수정 가능

    @NotBlank
    private String phoneNumber;     // 네이버에서 자동 완성, 수정 가능

    @NotNull
    private LocalDate birthDate;    // 사용자가 입력 필수

    @NotBlank
    private String address;         // 사용자가 입력 필수
}
```

**특징**:
- 네이버 정보 + 사용자 추가 입력 정보 모두 포함
- 사용자가 네이버 정보를 수정할 수 있음 (유연성)

### 3. CustomOAuth2UserService

**파일**: `src/main/java/com/energyfactory/energy_factory/service/CustomOAuth2UserService.java`

**핵심 로직**:

```java
@Override
public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // 1. 네이버 사용자 정보 파싱
    String providerId = oAuth2UserInfo.getProviderId();
    String email = oAuth2UserInfo.getEmail();
    String name = oAuth2UserInfo.getName();
    String phoneNumber = naverUserInfo.getMobile();

    // 2. providerId로 사용자 찾기 (Provider 무관)
    Optional<User> optionalUser = userRepository.findByProviderId(providerId);

    User user;
    if (optionalUser.isPresent()) {
        // 기존 사용자 로그인 (네이버 연동된 사용자 또는 순수 네이버 사용자)
        user = optionalUser.get();
        // 기존 정보 전부 유지 - 네이버 정보로 덮어쓰지 않음
        // DB 저장도 하지 않음 (변경 사항 없음)
    } else {
        // 신규 사용자 - DB에 저장하지 않고 임시 User 객체 생성
        user = User.builder()
            .email(email)
            .name(name)
            .phoneNumber(phoneNumber)
            .provider(providerEnum)
            .providerId(providerId)
            .role(Role.USER)
            .password(passwordEncoder.encode("OAUTH_USER_TEMP"))
            .build();
        // **DB에 저장하지 않음**
    }

    return new CustomUserDetails(user, oAuth2User.getAttributes());
}
```

**주요 변경점**:
- ❌ 기존: 신규 사용자를 바로 DB에 저장
- ✅ 현재: 임시 User 객체만 생성, DB 저장 안 함

### 4. OAuth2SuccessHandler

**파일**: `src/main/java/com/energyfactory/energy_factory/handler/OAuth2SuccessHandler.java`

**주요 메서드**:

#### (1) onAuthenticationSuccess()
```java
@Override
public void onAuthenticationSuccess(...) {
    HttpSession session = request.getSession(false);

    // 1. 계정 연동 모드인지 확인
    if (session != null && Boolean.TRUE.equals(session.getAttribute("linkMode"))) {
        handleAccountLink(request, response, session, customUserDetails);
        return;
    }

    Long userId = customUserDetails.getUser().getId();

    // 2. 신규 사용자 vs 기존 사용자 구분
    if (userId == null) {
        handleNewUser(request, response, customUserDetails);
    } else {
        handleExistingUser(request, response, customUserDetails);
    }
}
```

#### (2) handleNewUser() - 신규 사용자
```java
private void handleNewUser(...) {
    // OAuth2 정보를 세션에 저장
    HttpSession session = request.getSession();
    OAuth2TempInfo tempInfo = OAuth2TempInfo.builder()
        .provider(user.getProvider().name())
        .providerId(user.getProviderId())
        .email(user.getEmail())
        .name(user.getName())
        .phoneNumber(user.getPhoneNumber())
        .build();

    session.setAttribute("oauth2TempInfo", tempInfo);
    session.setMaxInactiveInterval(30 * 60); // 30분 유효

    // signup 페이지로 리다이렉트
    getRedirectStrategy().sendRedirect(request, response, signupUrl + "?oauth=pending");
}
```

#### (3) handleExistingUser() - 기존 사용자
```java
private void handleExistingUser(...) {
    // JWT 토큰 생성
    String accessToken = jwtUtil.createAccessToken(userId, username, role, 10 * 60 * 1000L);
    String refreshToken = jwtUtil.createRefreshToken(userId, username, refreshTokenExpiration);

    // Redis에 저장
    refreshTokenService.saveRefreshToken(username, refreshToken);

    // HttpOnly 쿠키에 저장
    addTokenCookie(response, "accessToken", accessToken, 10 * 60);
    addTokenCookie(response, "refreshToken", refreshToken, refreshTokenExpiration.intValue());

    // 홈으로 리다이렉트
    getRedirectStrategy().sendRedirect(request, response, homeUrl);
}
```

#### (4) handleAccountLink() - 계정 연동
```java
private void handleAccountLink(...) {
    Long linkUserId = (Long) session.getAttribute("linkUserId");
    String providerId = customUserDetails.getUser().getProviderId();
    Provider provider = customUserDetails.getUser().getProvider();

    try {
        // 계정 연동 실행
        userService.linkNaverAccount(linkUserId, providerId, provider);

        // 세션 정리
        session.removeAttribute("linkMode");
        session.removeAttribute("linkUserId");

        // 마이페이지로 리다이렉트 (연동 성공)
        getRedirectStrategy().sendRedirect(request, response, mypageUrl + "?link=success");
    } catch (Exception e) {
        // 에러 처리
        String errorMessage = e.getMessage().contains("이미 소셜 계정이") ? "already_linked" :
                              e.getMessage().contains("이미 다른 사용자가") ? "already_in_use" : "unknown_error";
        getRedirectStrategy().sendRedirect(request, response, mypageUrl + "?link=error&reason=" + errorMessage);
    }
}
```

### 5. UserService

**파일**: `src/main/java/com/energyfactory/energy_factory/service/UserService.java`

#### (1) signupWithOAuth2() - OAuth2 회원가입 완료
```java
@Transactional
public SignupResponseDto signupWithOAuth2(OAuth2TempInfo tempInfo, OAuth2SignupRequestDto requestDto) {
    // 이메일 중복 체크
    if (userRepository.existsByEmail(requestDto.getEmail())) {
        throw new BusinessException(ResultCode.DUPLICATE_EMAIL);
    }

    // 전화번호 중복 체크
    if (userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
        throw new BusinessException(ResultCode.DUPLICATE_PHONE_NUMBER);
    }

    // User 생성 (requestDto의 모든 정보 사용)
    User user = User.builder()
        .email(requestDto.getEmail())           // 사용자가 수정 가능
        .name(requestDto.getName())             // 사용자가 수정 가능
        .phoneNumber(requestDto.getPhoneNumber())  // 사용자가 수정 가능
        .birthDate(requestDto.getBirthDate())   // 사용자가 입력
        .address(requestDto.getAddress())       // 사용자가 입력
        .provider(Provider.valueOf(tempInfo.getProvider()))
        .providerId(tempInfo.getProviderId())
        .role(Role.USER)
        .password(passwordEncoder.encode("OAUTH_USER"))
        .build();

    userRepository.save(user);  // DB 저장

    return SignupResponseDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .build();
}
```

#### (2) linkNaverAccount() - 네이버 계정 연동
```java
@Transactional
public void linkNaverAccount(Long userId, String providerId, Provider provider) {
    // 1. 사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

    // 2. 이미 소셜 계정이 연동되어 있는지 확인
    if (user.getProviderId() != null) {
        throw new BusinessException(ResultCode.PROVIDER_ALREADY_LINKED);
    }

    // 3. 해당 providerId가 이미 다른 사용자에게 사용 중인지 확인
    if (userRepository.findByProviderId(providerId).isPresent()) {
        throw new BusinessException(ResultCode.PROVIDER_ALREADY_IN_USE);
    }

    // 4. Provider와 ProviderId 업데이트
    user.setProvider(provider);
    user.setProviderId(providerId);

    userRepository.save(user);
}
```

### 6. AuthController

**파일**: `src/main/java/com/energyfactory/energy_factory/controller/AuthController.java`

#### (1) getOAuth2TempInfo() - 세션 조회
```java
@GetMapping("/oauth-temp-info")
public ResponseEntity<ApiResponse<OAuth2TempInfo>> getOAuth2TempInfo(HttpServletRequest request) {
    HttpSession session = request.getSession(false);

    if (session == null) {
        throw new BusinessException(ResultCode.OAUTH_SESSION_EXPIRED);
    }

    OAuth2TempInfo tempInfo = (OAuth2TempInfo) session.getAttribute("oauth2TempInfo");

    if (tempInfo == null) {
        throw new BusinessException(ResultCode.OAUTH_SESSION_EXPIRED);
    }

    return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, tempInfo));
}
```

#### (2) signupWithOAuth2() - 회원가입 완료
```java
@PostMapping("/signup-with-oauth")
public ResponseEntity<ApiResponse<SignupResponseDto>> signupWithOAuth2(
        @Valid @RequestBody OAuth2SignupRequestDto requestDto,
        HttpServletRequest request,
        HttpServletResponse response) {

    // 세션에서 OAuth2 임시 정보 가져오기
    HttpSession session = request.getSession(false);
    OAuth2TempInfo tempInfo = (OAuth2TempInfo) session.getAttribute("oauth2TempInfo");

    // 회원가입 처리
    SignupResponseDto signupResponse = userService.signupWithOAuth2(tempInfo, requestDto);

    // JWT 토큰 발급
    String accessToken = jwtUtil.createAccessToken(...);
    String refreshToken = jwtUtil.createRefreshToken(...);

    // 쿠키에 저장
    addTokenCookie(response, "accessToken", accessToken, 10 * 60);
    addTokenCookie(response, "refreshToken", refreshToken, refreshTokenExpiration.intValue());

    // 세션 삭제
    session.removeAttribute("oauth2TempInfo");

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.of(ResultCode.SUCCESS_POST, signupResponse));
}
```

#### (3) linkNaverAccount() - 계정 연동 시작
```java
@GetMapping("/link/naver")
public void linkNaverAccount(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        HttpServletRequest request,
        HttpServletResponse response) throws IOException {

    if (userDetails == null) {
        response.sendRedirect("/api/auth/login?error=not_authenticated");
        return;
    }

    Long userId = userDetails.getUser().getId();

    // 세션에 연동 모드 저장
    HttpSession session = request.getSession();
    session.setAttribute("linkMode", true);
    session.setAttribute("linkUserId", userId);
    session.setMaxInactiveInterval(10 * 60); // 10분 유효

    // 네이버 OAuth2 인증으로 리다이렉트
    response.sendRedirect("/oauth2/authorization/naver");
}
```

### 7. UserController

**파일**: `src/main/java/com/energyfactory/energy_factory/controller/UserController.java`

**일반 회원가입 시 JWT 자동 발급**:

```java
@PostMapping("/signup")
public ResponseEntity<ApiResponse<SignupResponseDto>> signup(
        @Valid @RequestBody SignupRequestDto signupRequestDto,
        HttpServletResponse response) {

    // 회원가입 처리
    SignupResponseDto signupResponse = userService.signup(signupRequestDto);

    // JWT 토큰 발급 (자동 로그인)
    String accessToken = jwtUtil.createAccessToken(...);
    String refreshToken = jwtUtil.createRefreshToken(...);

    // Redis에 저장
    refreshTokenService.saveRefreshToken(signupResponse.getEmail(), refreshToken);

    // HttpOnly 쿠키에 저장
    addTokenCookie(response, "accessToken", accessToken, 10 * 60);
    addTokenCookie(response, "refreshToken", refreshToken, refreshTokenExpiration.intValue());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.of(ResultCode.SUCCESS_POST, signupResponse));
}
```

---

## API 명세

### 1. 네이버 로그인 시작

**엔드포인트**: `GET /oauth2/authorization/naver`

**설명**: 네이버 OAuth2 로그인을 시작합니다.

**요청**:
```bash
GET https://energy-factory.kr/oauth2/authorization/naver
```

**응답**: `302 Found`
```
Location: https://nid.naver.com/oauth2.0/authorize?...
```

**프론트엔드 구현**:
```javascript
const handleNaverLogin = () => {
  window.location.href = '/oauth2/authorization/naver';
};
```

### 2. OAuth2 세션 정보 조회

**엔드포인트**: `GET /api/auth/oauth-temp-info`

**설명**: 세션에 저장된 OAuth2 정보를 조회합니다. (신규 사용자 회원가입 폼 자동 완성용)

**요청**:
```bash
GET https://energy-factory.kr/api/auth/oauth-temp-info
```

**응답**: `200 OK`
```json
{
  "status": 200,
  "code": "20000000",
  "desc": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "provider": "NAVER",
    "providerId": "12345678",
    "email": "hong@naver.com",
    "name": "홍길동",
    "phoneNumber": "010-1234-5678"
  }
}
```

**에러**:
```json
{
  "status": 400,
  "code": "40100007",
  "desc": "OAuth2 세션이 만료되었습니다. 다시 로그인해주세요."
}
```

### 3. OAuth2 회원가입 완료

**엔드포인트**: `POST /api/auth/signup-with-oauth`

**설명**: 네이버 정보 + 사용자 추가 입력 정보로 회원가입을 완료하고 JWT를 발급합니다.

**요청**:
```bash
POST https://energy-factory.kr/api/auth/signup-with-oauth
Content-Type: application/json

{
  "email": "hong@naver.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "birthDate": "1990-01-01",
  "address": "서울특별시 강남구 테헤란로 123"
}
```

**응답**: `201 Created`
```json
{
  "status": 201,
  "code": "20000001",
  "desc": "회원 가입 성공",
  "data": {
    "id": 123,
    "email": "hong@naver.com",
    "name": "홍길동"
  }
}
```

**쿠키**:
```
Set-Cookie: accessToken=eyJhbGci...; HttpOnly; Secure; SameSite=None; Max-Age=600
Set-Cookie: refreshToken=eyJhbGci...; HttpOnly; Secure; SameSite=None; Max-Age=604800
```

### 4. 네이버 계정 연동 시작

**엔드포인트**: `GET /api/auth/link/naver`

**설명**: 로그인된 LOCAL 사용자가 네이버 계정을 연동합니다.

**요청**:
```bash
GET https://energy-factory.kr/api/auth/link/naver
Cookie: accessToken=eyJhbGci...
```

**응답**: `302 Found`
```
Location: /oauth2/authorization/naver
```

**연동 성공 후**:
```
Location: /mypage?link=success
```

**연동 실패 시**:
```
Location: /mypage?link=error&reason=already_linked
Location: /mypage?link=error&reason=already_in_use
```

### 5. 일반 회원가입 (JWT 자동 발급)

**엔드포인트**: `POST /api/users/signup`

**설명**: 이메일/비밀번호로 회원가입하고 JWT를 자동으로 발급합니다.

**요청**:
```bash
POST https://energy-factory.kr/api/users/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Pass1234!",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "birthDate": "1990-01-01",
  "address": "서울시 강남구"
}
```

**응답**: `201 Created`
```json
{
  "status": 201,
  "code": "20000001",
  "desc": "회원 가입 성공",
  "data": {
    "id": 124,
    "email": "user@example.com",
    "name": "홍길동"
  }
}
```

**쿠키**:
```
Set-Cookie: accessToken=eyJhbGci...; HttpOnly; Secure; SameSite=None; Max-Age=600
Set-Cookie: refreshToken=eyJhbGci...; HttpOnly; Secure; SameSite=None; Max-Age=604800
```

---

## 프론트엔드 연동

### 1. 네이버 로그인 버튼

```javascript
// 로그인 페이지
function LoginPage() {
  const handleNaverLogin = () => {
    window.location.href = '/oauth2/authorization/naver';
  };

  return (
    <button onClick={handleNaverLogin} className="naver-login-btn">
      <img src="/naver-logo.svg" alt="네이버" />
      네이버로 시작하기
    </button>
  );
}
```

### 2. 네이버 회원가입 페이지 (/signup?oauth=pending)

```javascript
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

function SignupPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const isOAuthPending = searchParams.get('oauth') === 'pending';

  const [oauthInfo, setOauthInfo] = useState(null);
  const [formData, setFormData] = useState({
    email: '',
    name: '',
    phoneNumber: '',
    birthDate: '',
    address: ''
  });

  useEffect(() => {
    if (isOAuthPending) {
      // 세션에서 네이버 정보 가져오기
      fetch('/api/auth/oauth-temp-info', {
        credentials: 'include'
      })
      .then(res => {
        if (!res.ok) {
          alert('세션이 만료되었습니다. 다시 로그인해주세요.');
          navigate('/login');
          return;
        }
        return res.json();
      })
      .then(data => {
        if (data) {
          setOauthInfo(data.data);
          // 네이버 정보로 폼 자동 완성
          setFormData({
            ...formData,
            email: data.data.email,
            name: data.data.name,
            phoneNumber: data.data.phoneNumber
          });
        }
      })
      .catch(err => {
        console.error('OAuth 정보 조회 실패:', err);
        navigate('/login');
      });
    }
  }, [isOAuthPending]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch('/api/auth/signup-with-oauth', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      if (response.ok) {
        // JWT가 쿠키에 저장됨
        navigate('/products');
      } else {
        const error = await response.json();
        alert(error.desc || '회원가입에 실패했습니다.');
      }
    } catch (err) {
      console.error('회원가입 실패:', err);
      alert('오류가 발생했습니다.');
    }
  };

  return (
    <div className="signup-page">
      <h2>{isOAuthPending ? '네이버 회원가입 완료' : '일반 회원가입'}</h2>

      <form onSubmit={handleSubmit}>
        {/* 네이버 정보 (자동 완성, 수정 가능) */}
        <input
          type="email"
          value={formData.email}
          onChange={(e) => setFormData({...formData, email: e.target.value})}
          placeholder="이메일"
          required
        />

        <input
          type="text"
          value={formData.name}
          onChange={(e) => setFormData({...formData, name: e.target.value})}
          placeholder="이름"
          required
        />

        <input
          type="tel"
          value={formData.phoneNumber}
          onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})}
          placeholder="전화번호"
          required
        />

        {/* 추가 정보 입력 */}
        <input
          type="date"
          value={formData.birthDate}
          onChange={(e) => setFormData({...formData, birthDate: e.target.value})}
          required
        />

        <input
          type="text"
          value={formData.address}
          onChange={(e) => setFormData({...formData, address: e.target.value})}
          placeholder="배송지 주소"
          required
        />

        <button type="submit">회원가입 완료</button>
      </form>
    </div>
  );
}
```

### 3. 계정 연동 페이지 (/signup-connect)

```javascript
import { useNavigate } from 'react-router-dom';

function SignupConnectPage() {
  const navigate = useNavigate();

  const handleNaverLink = () => {
    // JWT 쿠키가 있으므로 인증된 상태
    window.location.href = '/api/auth/link/naver';
  };

  const handleSkip = () => {
    navigate('/products');
  };

  return (
    <div className="connect-page">
      <h2>소셜 계정 연동</h2>
      <p>네이버 계정을 연동하면 더 편리하게 로그인할 수 있습니다.</p>

      <button onClick={handleNaverLink} className="naver-btn">
        <img src="/naver-logo.svg" alt="네이버" />
        네이버 계정 연동하기
      </button>

      <button onClick={handleSkip} className="skip-btn">
        나중에 하기
      </button>
    </div>
  );
}
```

### 4. 마이페이지 - 연동 결과 처리

```javascript
import { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';

function MyPage() {
  const [searchParams] = useSearchParams();

  useEffect(() => {
    const linkStatus = searchParams.get('link');
    const reason = searchParams.get('reason');

    if (linkStatus === 'success') {
      alert('네이버 계정 연동이 완료되었습니다!');
    } else if (linkStatus === 'error') {
      const errorMessages = {
        'already_linked': '이미 소셜 계정이 연동되어 있습니다.',
        'already_in_use': '해당 네이버 계정은 이미 다른 사용자가 사용 중입니다.',
        'session_expired': '세션이 만료되었습니다. 다시 시도해주세요.',
        'unknown_error': '알 수 없는 오류가 발생했습니다.'
      };
      alert(errorMessages[reason] || errorMessages['unknown_error']);
    }
  }, [searchParams]);

  return (
    <div className="mypage">
      <h2>마이페이지</h2>
      {/* 마이페이지 내용 */}
    </div>
  );
}
```

---

## 보안

### 1. 세션 보안

**세션 타임아웃**:
```java
// 신규 가입 세션: 30분
session.setMaxInactiveInterval(30 * 60);

// 계정 연동 세션: 10분
session.setMaxInactiveInterval(10 * 60);
```

**세션 정리**:
- 회원가입 완료 시 세션 삭제
- 계정 연동 완료 시 세션 삭제
- 에러 발생 시에도 세션 삭제

### 2. JWT 토큰 보안

**HttpOnly 쿠키**:
```java
cookie.setHttpOnly(true);   // JavaScript 접근 방지 (XSS 방어)
cookie.setSecure(true);     // HTTPS에서만 전송
cookie.setAttribute("SameSite", "None");  // Cross-site 허용
```

**토큰 만료 시간**:
- Access Token: 10분
- Refresh Token: 7일

### 3. 계정 연동 보안

**보안 체크 항목**:
```java
// 1. 로그인된 사용자만 연동 가능
@GetMapping("/link/naver")
public void linkNaverAccount(@AuthenticationPrincipal CustomUserDetails userDetails, ...) {
    if (userDetails == null) {
        // 미인증 사용자 차단
    }
}

// 2. 이미 연동된 계정 재연동 방지
if (user.getProviderId() != null) {
    throw new BusinessException(ResultCode.PROVIDER_ALREADY_LINKED);
}

// 3. providerId 중복 방지
if (userRepository.findByProviderId(providerId).isPresent()) {
    throw new BusinessException(ResultCode.PROVIDER_ALREADY_IN_USE);
}
```

### 4. IDOR 방지

**JWT에서 userId 추출**:
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

### 5. 중복 데이터 검증

**이메일/전화번호 중복 체크**:
```java
// OAuth2 회원가입 시
if (userRepository.existsByEmail(requestDto.getEmail())) {
    throw new BusinessException(ResultCode.DUPLICATE_EMAIL);
}

if (userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
    throw new BusinessException(ResultCode.DUPLICATE_PHONE_NUMBER);
}
```

---

## 테스트 시나리오

### 시나리오 1: 신규 사용자 네이버 가입

**Given**: 네이버 계정이 있지만 서비스에 처음 가입하는 사용자

**When**:
1. 네이버 로그인 버튼 클릭
2. 네이버 로그인 및 권한 승인

**Then**:
1. `/signup?oauth=pending` 페이지로 리다이렉트
2. 네이버 정보 자동 완성 (email, name, phoneNumber)
3. 사용자가 수정 + 추가 입력 (birthDate, address)
4. 회원가입 완료 버튼 클릭
5. DB에 저장 ✅
6. JWT 쿠키 발급 ✅
7. `/products`로 이동

**검증**:
```sql
SELECT * FROM user WHERE email = 'hong@naver.com';
-- provider: NAVER
-- provider_id: 12345678
-- birth_date: 1990-01-01
-- address: 서울특별시 강남구...
```

```bash
curl -b cookies.txt /api/users/me
# 응답: 사용자 정보 (JWT 인증 성공)
```

### 시나리오 2: 기존 사용자 네이버 로그인

**Given**: 이미 네이버로 가입한 사용자 (providerId 있음)

**When**:
1. 네이버 로그인 버튼 클릭
2. 네이버 로그인

**Then**:
1. `findByProviderId()` → 기존 사용자 찾음
2. JWT 발급
3. `/products`로 바로 리다이렉트
4. 추가 정보 입력 없이 로그인 완료

**검증**:
```bash
curl -b cookies.txt /api/users/me
# 응답: 사용자 정보 (생년월일, 주소 포함)
```

### 시나리오 3: 일반 회원가입 후 네이버 연동

**Given**: 이메일/비밀번호로 회원가입한 사용자 (LOCAL)

**When**:
1. `POST /api/users/signup` → JWT 자동 발급 ✅
2. SignupConnectPage 도착 (이미 로그인된 상태)
3. 네이버 연동 버튼 클릭
4. `GET /api/auth/link/naver`
5. 네이버 로그인

**Then**:
1. 세션에 `linkMode=true, linkUserId={userId}` 저장
2. `CustomOAuth2UserService` → 임시 User 생성 (providerId: naver123)
3. `OAuth2SuccessHandler.handleAccountLink()` 호출
4. `UserService.linkNaverAccount()` 호출
   - 보안 체크 통과 ✅
   - `user.setProvider(NAVER)`
   - `user.setProviderId("naver123")`
   - DB 저장 ✅
5. `/mypage?link=success` 리다이렉트

**검증**:
```sql
SELECT provider, provider_id FROM user WHERE email = 'user@example.com';
-- provider: NAVER (LOCAL → NAVER로 변경)
-- provider_id: naver123 (새로 추가)
```

**이후 로그인**:
- 이메일/비밀번호 로그인 가능 ✅
- 네이버 로그인 가능 ✅

### 시나리오 4: 계정 연동 에러 - 이미 연동된 계정

**Given**:
- User A: LOCAL 계정, 이미 네이버 연동됨 (providerId: naver123)

**When**:
1. User A가 다시 네이버 연동 시도
2. `GET /api/auth/link/naver`

**Then**:
1. `UserService.linkNaverAccount()` 호출
2. `user.getProviderId() != null` 체크
3. `PROVIDER_ALREADY_LINKED` 에러 발생
4. `/mypage?link=error&reason=already_linked` 리다이렉트

**프론트엔드**:
```javascript
alert('이미 소셜 계정이 연동되어 있습니다.');
```

### 시나리오 5: 계정 연동 에러 - 다른 사용자가 사용 중

**Given**:
- User A: 네이버로 가입 (providerId: naver123)
- User B: LOCAL 계정

**When**:
1. User B가 User A와 동일한 네이버 계정으로 연동 시도
2. `GET /api/auth/link/naver`
3. 네이버 로그인 (providerId: naver123)

**Then**:
1. `UserService.linkNaverAccount()` 호출
2. `findByProviderId("naver123")` → User A 존재
3. `PROVIDER_ALREADY_IN_USE` 에러 발생
4. `/mypage?link=error&reason=already_in_use` 리다이렉트

**프론트엔드**:
```javascript
alert('해당 네이버 계정은 이미 다른 사용자가 사용 중입니다.');
```

### 시나리오 6: 세션 만료

**Given**: 신규 사용자가 네이버 로그인 후 30분 이상 경과

**When**:
1. `/signup?oauth=pending` 페이지 도착
2. `GET /api/auth/oauth-temp-info` 호출

**Then**:
1. 세션 만료로 `oauth2TempInfo` 없음
2. `OAUTH_SESSION_EXPIRED` 에러 발생
3. "세션이 만료되었습니다. 다시 로그인해주세요." 알림
4. `/login` 페이지로 이동

---

## 트러블슈팅

### 문제 1: 회원가입 후 쿠키가 전달되지 않음

**증상**: `/signup` 완료 후 다음 페이지에서 401 Unauthorized

**원인**: CORS 또는 쿠키 속성 문제

**해결**:
1. 프론트엔드: `credentials: 'include'` 확인
   ```javascript
   fetch('/api/...', { credentials: 'include' })
   ```

2. 백엔드: `SameSite=None` 및 `Secure=true` 확인
   ```java
   cookie.setSecure(true);
   cookie.setAttribute("SameSite", "None");
   ```

3. CORS 설정 확인
   ```yaml
   app:
     cors:
       allowed-origins: https://energy-factory.kr
   ```

### 문제 2: 계정 연동 시 401 에러

**증상**: `/api/auth/link/naver` 호출 시 401 Unauthorized

**원인**: 로그인되지 않은 상태 (JWT 없음)

**해결**:
1. 일반 회원가입 시 JWT 자동 발급 확인
   ```java
   // UserController.signup()
   addTokenCookie(response, "accessToken", ...);
   ```

2. 브라우저 쿠키 확인
   ```
   Application → Cookies → accessToken 존재 확인
   ```

3. 프론트엔드 fetch 확인
   ```javascript
   fetch('/api/auth/link/naver', {
     credentials: 'include'  // 쿠키 포함 필수
   })
   ```

### 문제 3: 세션 정보가 없음 (OAuth2 회원가입)

**증상**: `/api/auth/oauth-temp-info` 호출 시 세션 만료 에러

**원인**:
1. 30분 세션 타임아웃
2. 브라우저 쿠키 차단
3. 네이버 로그인 없이 직접 페이지 접근

**해결**:
1. 세션 타임아웃 연장 (필요 시)
   ```java
   session.setMaxInactiveInterval(60 * 60); // 1시간
   ```

2. 쿠키 설정 확인 (브라우저 개발자 도구)

3. 프론트엔드 에러 처리 강화
   ```javascript
   if (!res.ok) {
     alert('세션이 만료되었습니다. 다시 로그인해주세요.');
     navigate('/login');
   }
   ```

### 문제 4: Swagger에서 API 테스트 불가

**증상**: 계정 연동 API가 401 에러

**원인**: Swagger UI에서 JWT 쿠키 전송 불가

**해결**:
1. Postman 사용
2. 브라우저 개발자 도구 Network 탭 사용
3. curl 명령어 사용
   ```bash
   # 1. 회원가입
   curl -c cookies.txt -X POST /api/users/signup -d '{...}'

   # 2. 쿠키로 인증된 요청
   curl -b cookies.txt /api/auth/link/naver
   ```

### 문제 5: 네이버 정보가 자동 완성되지 않음

**증상**: `/signup?oauth=pending` 페이지에서 입력 필드가 비어있음

**원인**: 세션 조회 실패 또는 프론트엔드 상태 업데이트 실패

**해결**:
1. 네트워크 탭에서 `/api/auth/oauth-temp-info` 응답 확인
   ```json
   {
     "data": {
       "email": "hong@naver.com",
       "name": "홍길동",
       "phoneNumber": "010-1234-5678"
     }
   }
   ```

2. 프론트엔드 상태 업데이트 확인
   ```javascript
   .then(data => {
     console.log('OAuth Info:', data.data);  // 디버깅
     setFormData({
       ...formData,
       email: data.data.email,
       name: data.data.name,
       phoneNumber: data.data.phoneNumber
     });
   })
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

jwt:
  secret: myVerySecretJWTKeyForEnergyFactoryApplication123456789
  refresh-token-expiration: 604800 # 7일 (초 단위)

app:
  cors:
    allowed-origins: http://localhost:5173,http://localhost:8080
  oauth2:
    signup-url: http://localhost:5173/signup
    home-url: http://localhost:5173/products
    mypage-url: http://localhost:5173/mypage
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
            redirect-uri: "https://energy-factory.kr/login/oauth2/code/{registrationId}"
            authorization-grant-type: authorization_code
            scope: name,email,mobile
            client-name: Naver
        provider:
          naver:
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            token-uri: https://nid.naver.com/oauth2.0/token
            user-info-uri: https://openapi.naver.com/v1/nid/me
            user-name-attribute: response

jwt:
  secret: ${JWT_SECRET_KEY}
  refresh-token-expiration: 604800

app:
  cors:
    allowed-origins: ${ALLOWED_ORIGINS:https://energy-factory.kr,https://www.energy-factory.kr}
  oauth2:
    signup-url: ${FRONTEND_SIGNUP_URL:https://energy-factory.kr/signup}
    home-url: ${FRONTEND_HOME_URL:https://energy-factory.kr/products}
    mypage-url: ${FRONTEND_MYPAGE_URL:https://energy-factory.kr/mypage}
  cookie:
    secure: true  # 프로덕션 환경 HTTPS
```

---

## 참고 자료

### 내부 문서
- [CORS 설정 가이드](./cors-troubleshooting.md)
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
| 2025-11-06 | 2.0.0 | 세션 기반 회원가입 방식 + 계정 연동 기능 구현 | Backend Team |
| 2025-11-05 | 1.0.0 | 네이버 OAuth2 로그인 초기 구현 (자동 회원가입 방식) | Backend Team |

---

## 라이선스

Copyright (c) 2025 Energy Factory. All rights reserved.
