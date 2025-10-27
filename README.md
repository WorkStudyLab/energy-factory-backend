# Energy Factory Backend

건강관리 쇼핑몰의 백엔드 REST API 서버입니다.  
서비스 주소 : https://energy-factory.kr

## 프로젝트 소개

에너지 드링크 및 건강 보조제를 판매하는 전자상거래 플랫폼의 백엔드 시스템입니다.
사용자 인증, 상품 관리, 주문 처리, 결제 등 쇼핑몰의 핵심 기능을 제공합니다.

## 기술 스택

- **Backend**: Spring Boot 3.5.5, Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis 7.2
- **Security**: JWT, OAuth2 (Naver)
- **Documentation**: Swagger UI
- **Deployment**: Docker, GitHub Actions CI/CD → AWS EC2

## 주요 기능

- **인증/인가**: JWT 토큰 기반 인증, 네이버 소셜 로그인
- **사용자 관리**: 회원가입, 프로필 관리, 배송지 관리
- **상품 관리**: 상품 조회, 검색, 필터링, 태그 시스템
- **주문 관리**: 주문 생성/조회/취소, 재고 자동 관리
- **결제 처리**: 결제 상태 관리, 환불 처리

## 빠른 시작

### 사전 요구사항

- Java 17
- Docker & Docker Compose
- Gradle

### 로컬 환경 실행

1. **의존성 서비스 시작** (MySQL, Redis)
```bash
docker-compose up -d
```

2. **환경 변수 설정**
```bash
# .env 파일 생성 (루트 디렉토리)
MYSQL_DATABASE=energy_factory
MYSQL_USER=your_user
MYSQL_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret
```

3. **애플리케이션 실행**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

4. **API 문서 확인**
```
http://localhost:8080/swagger-ui.html
```

## API 엔드포인트

### 인증
- `POST /api/auth/login` - 로그인
- `POST /api/auth/refresh` - 토큰 갱신
- `POST /api/auth/logout` - 로그아웃

### 사용자
- `POST /api/users/signup` - 회원가입
- `GET /api/users/{id}` - 사용자 정보 조회
- `PUT /api/users/{id}` - 사용자 정보 수정

### 상품
- `GET /api/products` - 상품 목록 조회
- `GET /api/products/{id}` - 상품 상세 조회
- `GET /api/products/search` - 상품 검색

### 주문
- `POST /api/orders` - 주문 생성
- `GET /api/orders` - 주문 목록 조회
- `DELETE /api/orders/{id}` - 주문 취소

상세한 API 명세는 Swagger UI에서 확인하실 수 있습니다.

## 프로젝트 구조

```
src/main/java/com/energyfactory/energy_factory/
├── config/          # 설정 (Security, Redis, Swagger 등)
├── controller/      # REST API 엔드포인트
├── service/         # 비즈니스 로직
├── repository/      # 데이터 접근 계층
├── entity/          # JPA 엔티티
├── dto/             # 데이터 전송 객체
├── jwt/             # JWT 인증 처리
└── exception/       # 예외 처리
```

## 환경 설정

- `application-local.yml` - 로컬 개발 환경
- `application-dev.yml` - 개발 환경
- `application-prod.yml` - 프로덕션 환경
- `application-test.yml` - 테스트 환경

## 문서

- `docs/PROJECT_STATUS.md` - 프로젝트 현황
- `docs/erd.txt` - 데이터베이스 ERD
- `docs/workflow-guide.md` - 개발 워크플로우

## 라이선스

본 프로젝트는 개인 프로젝트입니다.
