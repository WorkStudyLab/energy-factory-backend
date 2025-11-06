# 네이버 OAuth2 시퀀스 다이어그램

> 작성일: 2025-11-06
> 버전: 1.0.0
> 관련 문서: [naver-oauth2-integration.md](./naver-oauth2-integration.md)

## 목차
1. [신규 사용자 네이버 가입](#1-신규-사용자-네이버-가입)
2. [기존 사용자 네이버 로그인](#2-기존-사용자-네이버-로그인)
3. [LOCAL 계정 네이버 연동](#3-local-계정-네이버-연동)

---

## 1. 신규 사용자 네이버 가입

**시나리오**: 처음으로 네이버 계정으로 서비스에 가입하는 사용자

**핵심 포인트**:
- 네이버 정보를 세션에 임시 저장 (DB 저장 ❌)
- 사용자가 추가 정보 입력 후 회원가입 완료 시점에 DB 저장
- 회원가입 완료 후 JWT 자동 발급

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Frontend as 프론트엔드
    participant Spring as Spring Security
    participant Naver as 네이버
    participant OAuth2Service as CustomOAuth2UserService
    participant Handler as OAuth2SuccessHandler
    participant Session as 세션
    participant AuthController as AuthController
    participant UserService as UserService
    participant DB as 데이터베이스

    User->>Frontend: 네이버 로그인 버튼 클릭
    Frontend->>Spring: GET /oauth2/authorization/naver
    Spring->>Naver: 302 리다이렉트 (네이버 로그인 페이지)

    User->>Naver: 로그인 및 권한 승인
    Naver->>Spring: GET /login/oauth2/code/naver?code=...

    Spring->>OAuth2Service: loadUser(userRequest)
    OAuth2Service->>Naver: 사용자 정보 요청
    Naver-->>OAuth2Service: {id, email, name, mobile}

    OAuth2Service->>DB: findByProviderId(providerId)
    DB-->>OAuth2Service: 없음 (신규 사용자)

    OAuth2Service->>OAuth2Service: 임시 User 객체 생성 (DB 저장 ❌)
    OAuth2Service-->>Spring: CustomUserDetails(임시 User)

    Spring->>Handler: onAuthenticationSuccess()
    Handler->>Handler: userId == null? (신규 사용자 확인)

    Handler->>Session: OAuth2TempInfo 저장
    Note over Session: provider, providerId,<br/>email, name, phoneNumber
    Session-->>Handler: 저장 완료 (30분 유효)

    Handler->>Frontend: 302 /signup?oauth=pending

    Frontend->>AuthController: GET /api/auth/oauth-temp-info
    AuthController->>Session: getAttribute("oauth2TempInfo")
    Session-->>AuthController: OAuth2TempInfo
    AuthController-->>Frontend: 200 OK {email, name, phoneNumber}

    Frontend->>User: 네이버 정보 자동 완성 표시
    User->>Frontend: 정보 수정 + 추가 입력<br/>(birthDate, address)

    Frontend->>AuthController: POST /api/auth/signup-with-oauth<br/>{email, name, phoneNumber, birthDate, address}
    AuthController->>Session: getAttribute("oauth2TempInfo")
    Session-->>AuthController: OAuth2TempInfo

    AuthController->>UserService: signupWithOAuth2(tempInfo, requestDto)
    UserService->>DB: existsByEmail() / existsByPhoneNumber()
    DB-->>UserService: 중복 없음

    UserService->>DB: save(User)
    DB-->>UserService: User(id=123)
    UserService-->>AuthController: SignupResponseDto

    AuthController->>AuthController: JWT 토큰 생성<br/>(Access + Refresh)
    AuthController->>Session: removeAttribute("oauth2TempInfo")
    AuthController-->>Frontend: 201 Created<br/>Set-Cookie: accessToken, refreshToken

    Frontend->>User: /products로 이동 (로그인 완료)
```

**주요 단계 설명**:

| 단계 | 설명 | 비고 |
|------|------|------|
| 1-3 | 네이버 로그인 시작 | Spring Security OAuth2가 자동 처리 |
| 4-6 | 네이버 사용자 정보 조회 | CustomOAuth2UserService.loadUser() |
| 7-8 | 신규 사용자 확인 | findByProviderId() → 없음 |
| 9-10 | 임시 User 객체 생성 | **DB 저장하지 않음** |
| 11-13 | 세션에 임시 저장 | OAuth2TempInfo (30분 유효) |
| 14 | 회원가입 페이지로 이동 | /signup?oauth=pending |
| 15-17 | 네이버 정보 조회 | GET /api/auth/oauth-temp-info |
| 18-19 | 사용자 입력 | 네이버 정보 수정 + 추가 정보 입력 |
| 20-25 | 회원가입 완료 | **이 시점에 DB 저장** |
| 26-28 | JWT 발급 및 리다이렉트 | 자동 로그인 완료 |

---

## 2. 기존 사용자 네이버 로그인

**시나리오**: 이미 네이버로 가입한 사용자가 다시 로그인

**핵심 포인트**:
- providerId로 기존 사용자 찾기
- 기존 정보 유지 (업데이트 ❌)
- JWT 발급 후 홈으로 바로 이동

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Frontend as 프론트엔드
    participant Spring as Spring Security
    participant Naver as 네이버
    participant OAuth2Service as CustomOAuth2UserService
    participant Handler as OAuth2SuccessHandler
    participant DB as 데이터베이스
    participant Redis as Redis

    User->>Frontend: 네이버 로그인 버튼 클릭
    Frontend->>Spring: GET /oauth2/authorization/naver
    Spring->>Naver: 302 리다이렉트

    User->>Naver: 로그인 및 권한 승인
    Naver->>Spring: GET /login/oauth2/code/naver?code=...

    Spring->>OAuth2Service: loadUser(userRequest)
    OAuth2Service->>Naver: 사용자 정보 요청
    Naver-->>OAuth2Service: {id, email, name, mobile}

    OAuth2Service->>DB: findByProviderId(providerId)
    DB-->>OAuth2Service: User(id=123) ✅ 기존 사용자

    Note over OAuth2Service: 기존 정보 유지<br/>(DB 업데이트 ❌)
    OAuth2Service-->>Spring: CustomUserDetails(기존 User)

    Spring->>Handler: onAuthenticationSuccess()
    Handler->>Handler: userId == 123? (기존 사용자 확인)

    Handler->>Handler: JWT 토큰 생성<br/>(Access + Refresh)
    Handler->>Redis: saveRefreshToken(email, refreshToken)
    Redis-->>Handler: 저장 완료

    Handler-->>Frontend: 302 /products<br/>Set-Cookie: accessToken, refreshToken

    Frontend->>User: 제품 페이지 표시 (로그인 완료)
```

**주요 단계 설명**:

| 단계 | 설명 | 비고 |
|------|------|------|
| 1-6 | 네이버 로그인 | 신규 가입과 동일 |
| 7-8 | 기존 사용자 확인 | findByProviderId() → **있음** |
| 9-10 | 기존 User 반환 | DB 업데이트하지 않음 |
| 11-14 | JWT 발급 | Access + Refresh Token |
| 15-16 | Redis에 저장 | Refresh Token 저장 |
| 17-18 | 홈으로 리다이렉트 | 추가 정보 입력 없이 바로 로그인 |

---

## 3. LOCAL 계정 네이버 연동

**시나리오**: 이메일/비밀번호로 가입한 사용자가 네이버 계정을 추가 연동

**핵심 포인트**:
- 일반 회원가입 시 JWT 자동 발급 (자동 로그인)
- 로그인된 상태에서 네이버 연동
- 보안 체크: providerId 중복 방지, 재연동 방지
- 연동 후 두 가지 방법으로 로그인 가능

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Frontend as 프론트엔드
    participant UserController as UserController
    participant AuthController as AuthController
    participant Session as 세션
    participant Spring as Spring Security
    participant Naver as 네이버
    participant OAuth2Service as CustomOAuth2UserService
    participant Handler as OAuth2SuccessHandler
    participant UserService as UserService
    participant DB as 데이터베이스

    User->>Frontend: 일반 회원가입<br/>(email/password)
    Frontend->>UserController: POST /api/users/signup
    UserController->>DB: save(User)
    DB-->>UserController: User(id=100, provider=LOCAL)

    UserController->>UserController: JWT 자동 발급 ✅
    UserController-->>Frontend: 201 Created<br/>Set-Cookie: accessToken, refreshToken

    Frontend->>User: SignupConnectPage 도착<br/>(이미 로그인된 상태)

    User->>Frontend: 네이버 연동 버튼 클릭
    Frontend->>AuthController: GET /api/auth/link/naver<br/>Cookie: accessToken

    AuthController->>AuthController: @AuthenticationPrincipal<br/>userId 확인 (100)
    AuthController->>Session: setAttribute("linkMode", true)<br/>setAttribute("linkUserId", 100)
    Session-->>AuthController: 저장 완료 (10분 유효)

    AuthController-->>Frontend: 302 /oauth2/authorization/naver
    Frontend->>Spring: GET /oauth2/authorization/naver
    Spring->>Naver: 302 리다이렉트

    User->>Naver: 네이버 로그인
    Naver->>Spring: GET /login/oauth2/code/naver?code=...

    Spring->>OAuth2Service: loadUser(userRequest)
    OAuth2Service->>Naver: 사용자 정보 요청
    Naver-->>OAuth2Service: {id: "naver123", email, name, mobile}

    OAuth2Service->>DB: findByProviderId("naver123")
    DB-->>OAuth2Service: 없음

    OAuth2Service->>OAuth2Service: 임시 User 객체 생성
    OAuth2Service-->>Spring: CustomUserDetails(임시 User)

    Spring->>Handler: onAuthenticationSuccess()
    Handler->>Session: getAttribute("linkMode")
    Session-->>Handler: true ✅ 연동 모드

    Handler->>Session: getAttribute("linkUserId")
    Session-->>Handler: 100

    Handler->>UserService: linkNaverAccount(100, "naver123", NAVER)

    UserService->>DB: findById(100)
    DB-->>UserService: User(id=100, providerId=null)

    UserService->>UserService: 보안 체크<br/>1. providerId == null? ✅<br/>2. "naver123" 다른 사용자 사용? ✅

    UserService->>DB: update User<br/>SET provider=NAVER, providerId="naver123"
    DB-->>UserService: 업데이트 완료

    UserService-->>Handler: 성공

    Handler->>Session: removeAttribute("linkMode")<br/>removeAttribute("linkUserId")
    Handler-->>Frontend: 302 /mypage?link=success

    Frontend->>User: "네이버 계정 연동 완료!" 메시지 표시

    Note over User,DB: 이제 이메일/비밀번호 OR<br/>네이버 로그인 둘 다 가능 ✅
```

**주요 단계 설명**:

| 단계 | 설명 | 비고 |
|------|------|------|
| 1-4 | 일반 회원가입 | POST /api/users/signup |
| 5-6 | JWT 자동 발급 | **회원가입 후 자동 로그인** |
| 7-8 | SignupConnectPage 도착 | 이미 로그인된 상태 |
| 9-12 | 네이버 연동 시작 | GET /api/auth/link/naver |
| 13-15 | 연동 모드 저장 | linkMode=true, linkUserId=100 |
| 16-23 | 네이버 OAuth2 플로우 | 일반 로그인과 동일 |
| 24-27 | 연동 모드 확인 | linkMode == true? |
| 28-32 | 계정 연동 실행 | linkNaverAccount() |
| 33-35 | 보안 체크 | providerId 중복 방지 |
| 36-37 | DB 업데이트 | provider=NAVER, providerId 추가 |
| 38-40 | 연동 완료 | 세션 정리 및 리다이렉트 |
| 41 | 성공 메시지 | /mypage?link=success |

---

## 에러 시나리오

### 에러 1: 세션 만료 (신규 가입)

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Frontend as 프론트엔드
    participant AuthController as AuthController
    participant Session as 세션

    Note over User,Session: 네이버 로그인 후 30분 이상 경과

    User->>Frontend: /signup?oauth=pending 페이지 도착
    Frontend->>AuthController: GET /api/auth/oauth-temp-info

    AuthController->>Session: getAttribute("oauth2TempInfo")
    Session-->>AuthController: null (세션 만료)

    AuthController-->>Frontend: 400 Bad Request<br/>OAUTH_SESSION_EXPIRED

    Frontend->>User: "세션이 만료되었습니다.<br/>다시 로그인해주세요."
    Frontend->>Frontend: navigate('/login')
```

### 에러 2: 이미 연동된 계정 (재연동 시도)

```mermaid
sequenceDiagram
    actor User as 사용자 (providerId: naver123)
    participant Frontend as 프론트엔드
    participant AuthController as AuthController
    participant Handler as OAuth2SuccessHandler
    participant UserService as UserService
    participant DB as 데이터베이스

    User->>Frontend: 네이버 재연동 시도
    Frontend->>AuthController: GET /api/auth/link/naver

    Note over AuthController,Handler: 네이버 OAuth2 플로우...

    Handler->>UserService: linkNaverAccount(userId, "naver123", NAVER)

    UserService->>DB: findById(userId)
    DB-->>UserService: User(providerId="naver123")

    UserService->>UserService: providerId != null?
    Note over UserService: 이미 연동되어 있음

    UserService-->>Handler: BusinessException<br/>PROVIDER_ALREADY_LINKED

    Handler-->>Frontend: 302 /mypage?link=error&reason=already_linked

    Frontend->>User: "이미 소셜 계정이<br/>연동되어 있습니다."
```

### 에러 3: 다른 사용자가 사용 중인 providerId

```mermaid
sequenceDiagram
    actor UserA as User A (providerId: naver123)
    actor UserB as User B (providerId: null)
    participant Frontend as 프론트엔드
    participant AuthController as AuthController
    participant Handler as OAuth2SuccessHandler
    participant UserService as UserService
    participant DB as 데이터베이스

    Note over UserA,DB: User A가 이미 naver123으로 가입됨

    UserB->>Frontend: 같은 네이버 계정으로 연동 시도
    Frontend->>AuthController: GET /api/auth/link/naver

    Note over AuthController,Handler: 네이버 OAuth2 플로우...

    Handler->>UserService: linkNaverAccount(UserB.id, "naver123", NAVER)

    UserService->>DB: findById(UserB.id)
    DB-->>UserService: User B (providerId=null)

    UserService->>DB: findByProviderId("naver123")
    DB-->>UserService: User A ✅ 이미 사용 중

    UserService-->>Handler: BusinessException<br/>PROVIDER_ALREADY_IN_USE

    Handler-->>Frontend: 302 /mypage?link=error&reason=already_in_use

    Frontend->>UserB: "해당 네이버 계정은<br/>이미 다른 사용자가<br/>사용 중입니다."
```

---

## 데이터베이스 상태 변화

### 신규 가입 플로우

```sql
-- 1. 네이버 로그인 (세션에만 저장)
-- DB 변화 없음

-- 2. 회원가입 완료 (POST /api/auth/signup-with-oauth)
INSERT INTO user (
    email, name, password, phone_number, birth_date, address,
    provider, provider_id, role, created_at
) VALUES (
    'hong@naver.com', '홍길동', '$2a$10$...', '010-1234-5678',
    '1990-01-01', '서울시 강남구',
    'NAVER', '12345678', 'USER', NOW()
);
-- Result: id=123
```

### 기존 사용자 로그인 플로우

```sql
-- 1. 네이버 로그인
SELECT * FROM user WHERE provider_id = '12345678';
-- Result: User(id=123, email='hong@naver.com', ...)

-- 2. DB 업데이트 없음 (기존 정보 유지)
```

### 계정 연동 플로우

```sql
-- 1. 일반 회원가입
INSERT INTO user (
    email, name, password, phone_number, birth_date, address,
    provider, provider_id, role, created_at
) VALUES (
    'user@example.com', '김철수', '$2a$10$...', '010-9999-8888',
    '1995-05-05', '서울시 종로구',
    'LOCAL', NULL, 'USER', NOW()
);
-- Result: id=100

-- 2. 네이버 연동 (PUT)
UPDATE user
SET provider = 'NAVER',
    provider_id = 'naver123',
    updated_at = NOW()
WHERE id = 100;
-- Result: 1 row affected

-- 3. 이후 두 가지 방법으로 로그인 가능
-- 방법 1: 이메일/비밀번호 (기존 password 유지)
-- 방법 2: 네이버 (providerId로 찾기)
```

---

## 참고 사항

### Mermaid 렌더링

이 문서의 시퀀스 다이어그램은 Mermaid 형식으로 작성되었습니다.

**지원 플랫폼**:
- GitHub (자동 렌더링)
- GitLab (자동 렌더링)
- VS Code (Mermaid Preview 확장 설치)
- IntelliJ IDEA (Mermaid 플러그인 설치)

**온라인 에디터**:
- [Mermaid Live Editor](https://mermaid.live/)

### 관련 문서

- [네이버 OAuth2 통합 가이드](./naver-oauth2-integration.md) - 상세 구현 설명
- [API 명세서](./naver-oauth2-integration.md#api-명세)
- [테스트 시나리오](./naver-oauth2-integration.md#테스트-시나리오)

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 2025-11-06 | 1.0.0 | 시퀀스 다이어그램 초안 작성 | Backend Team |

---

## 라이선스

Copyright (c) 2025 Energy Factory. All rights reserved.
