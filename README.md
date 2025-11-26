# Energy Factory Backend

건강관리 제품을 판매하는 쇼핑몰 서비스의 백엔드 REST API 서버입니다.

| 서비스 | URL |
|--------|-----|
| 웹사이트 | https://energy-factory.kr |
| API 문서 | http://13.209.24.80:8080/swagger-ui/index.html |
| 상세 구현 문서 | https://meteor-vein-e56.notion.site/Energy-Factory-2b7c4ab994e58069b7e1eac49e81699a |

## 서비스 화면

| 홈/상품 목록 | 상품 상세 | 영양정보 |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/45e56fe4-d4c1-41ee-9e36-55a5ec0b84ab" width="280" /> | <img src="https://github.com/user-attachments/assets/569c2771-22c2-4a7b-9c86-36cec60ca955" width="280" /> | <img src="https://github.com/user-attachments/assets/21a0fe45-f06b-4877-913d-6694af0ee3d3" width="280" /> |

| 장바구니 | 주문내역 | 마이페이지 |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/43a091e7-a661-4e20-9199-04c2c821c9e7" width="280" /> | <img src="https://github.com/user-attachments/assets/1f17946f-1b1e-43c3-b9c7-b4ca336d4f45" width="280" /> | <img src="https://github.com/user-attachments/assets/e449b12f-758d-4db1-9d66-e7999e342c96" width="280" /> |



## 기술 스택

| 분류 | 기술 |
|------|------|
| Backend | Spring Boot 3.5.5, Java 17 |
| Database | MySQL 8.0 (RDS), Redis 7.2 |
| Auth | JWT, OAuth2 (Naver) |
| Payment | Toss Payments |
| Infra | AWS EC2, S3, CloudFront, SES, Route 53 |
| CI/CD | GitHub Actions, systemd |

## 핵심 기술 구현

### 1. 재고 동시성 제어
- **문제**: 동시 주문 시 재고 정합성 문제, 미결제 주문의 재고 점유
- **해결**: `stock`, `reservedStock`, `availableStock` 3-tier 재고 관리 + 15분 타임아웃 스케줄러

### 2. SSE 실시간 알림
- **문제**: 주문 상태 변경을 사용자에게 실시간 전달
- **해결**: Server-Sent Events + ConcurrentHashMap 기반 연결 관리 (30분 타임아웃)

### 3. 소셜 로그인 통합
- **문제**: 신규/기존 사용자 분기, 계정 연동, JWT 통합
- **해결**: OAuth2 세션 기반 신규 사용자 처리 + linkMode 플래그 기반 계정 연동

### 4. 비밀번호 재설정
- **문제**: 안전한 비밀번호 재설정 프로세스
- **해결**: 3단계 인증 (코드 발송 → 검증 → 재설정) + Redis TTL + Rate Limiting

### 5. CI/CD 파이프라인
- **문제**: 수동 배포의 번거로움, 테스트 누락
- **해결**: GitHub Actions (Test → Deploy) + JAR 자동 백업 + systemd 서비스 관리

## 시스템 아키텍처

<img width="1311" height="650" alt="image" src="https://github.com/user-attachments/assets/b78d4086-2615-4d0e-af45-ed7c538ddf8f" />

## ERD

<img width="1047" height="735" alt="image" src="https://github.com/user-attachments/assets/279b3c49-d4c8-49f1-9dfe-aa19ec483f10" />


## 프로젝트 구조

```
src/main/java/com/energyfactory/energy_factory/
├── config/        # Security, Redis, Swagger 설정
├── controller/    # REST API 엔드포인트
├── service/       # 비즈니스 로직
├── repository/    # 데이터 접근 계층
├── entity/        # JPA 엔티티
├── dto/           # 데이터 전송 객체
├── jwt/           # JWT 인증 처리
├── scheduler/     # 스케줄러 (재고 타임아웃 등)
└── exception/     # 예외 처리
```

## 개발 정보

- **개발 기간**: 2025.09 ~ 2025.11

- **주요 완료 항목**
    - 사용자 인증 및 권한 관리
    - 상품 및 주문 프로세스
    - 결제 시스템
    - 실시간 알림
    - CI/CD 파이프라인
 
## Contributors

| <img src="https://github.com/yundoun.png" width="80" /> | <img src="https://github.com/seongheonjeong.png" width="80" /> |
|:---:|:---:|
| [윤도운](https://github.com/yundoun) | [정성헌](https://github.com/seongheonjeong) |
  | **Project Lead** | Backend |
  | Backend, Infra, CI/CD | 초기 개발 참여 |
