# Energy Factory 인프라 아키텍처 다이어그램

## 전체 인프라 아키텍처

```mermaid
graph TB
    User[사용자<br/>웹 브라우저]

    subgraph "도메인 & DNS"
        Gabia[가비아<br/>Registrar<br/>energy-factory.kr]
        Route53[AWS Route 53<br/>Hosted Zone<br/>DNS 관리]
    end

    subgraph "SSL & CDN"
        ACM[AWS ACM<br/>SSL 인증서<br/>us-east-1]
        CloudFront[AWS CloudFront<br/>d1o0ytu060swr1.cloudfront.net<br/>HTTPS 배포]
    end

    subgraph "프론트엔드"
        S3[AWS S3 Bucket<br/>React 정적 파일<br/>HTML/JS/CSS]
    end

    subgraph "백엔드 - AWS EC2"
        EC2[EC2 Ubuntu<br/>Spring Boot :8080<br/>systemd 서비스]
    end

    subgraph "데이터 저장소 - ap-northeast-2"
        RDS[AWS RDS<br/>MySQL 8.0<br/>에너지 공장 DB]
        Redis[Redis 7.2<br/>RefreshToken 캐시<br/>TTL 7일]
    end

    subgraph "AWS 서비스"
        SES[AWS SES<br/>이메일 발송<br/>noreply@energy-factory.kr]
    end

    subgraph "외부 서비스"
        Toss[토스페이먼츠<br/>결제 승인/취소<br/>API]
        NaverOAuth[네이버 OAuth2<br/>소셜 로그인<br/>계정 연동]
    end

    subgraph "CI/CD"
        GitHub[GitHub Actions<br/>자동 배포<br/>main 브랜치]
    end

    User -->|HTTPS| Gabia
    Gabia -->|네임서버 위임| Route53
    Route53 -->|A Record| CloudFront
    ACM -->|SSL 인증서| CloudFront

    CloudFront -->|/* 정적 파일| S3
    CloudFront -->|/api/* 백엔드| EC2

    EC2 -->|쿼리| RDS
    EC2 -->|RefreshToken 저장/조회| Redis
    EC2 -->|비밀번호 재설정 이메일| SES
    EC2 -->|결제 승인/취소| Toss
    EC2 -->|소셜 로그인| NaverOAuth

    GitHub -->|SSH 배포| EC2

    style User fill:#e1f5ff
    style CloudFront fill:#ff9900
    style S3 fill:#569a31
    style EC2 fill:#ff9900
    style RDS fill:#527fff
    style Redis fill:#dc382d
    style Route53 fill:#8c4fff
    style ACM fill:#dd344c
    style SES fill:#dd344c
    style Toss fill:#0064ff
    style NaverOAuth fill:#03c75a
    style GitHub fill:#24292e
```

## 상세 배포 플로우

```mermaid
sequenceDiagram
    participant Dev as 개발자
    participant GH as GitHub
    participant GA as GitHub Actions
    participant EC2 as EC2 Server
    participant SYS as systemd
    participant APP as Spring Boot

    Dev->>GH: git push origin main
    GH->>GA: Trigger Workflow
    GA->>GA: Gradle Build & Test
    GA->>EC2: SSH 접속
    GA->>EC2: Git Pull
    GA->>EC2: Gradle Build JAR
    GA->>SYS: systemctl restart energy-factory
    SYS->>APP: Start Application
    APP->>APP: Health Check
    APP-->>GA: 배포 성공
    GA-->>Dev: 배포 완료 알림
```

## 사용자 요청 플로우

```mermaid
sequenceDiagram
    participant User as 사용자
    participant CF as CloudFront
    participant S3 as S3 (React)
    participant EC2 as EC2 (Spring Boot)
    participant RDS as MySQL
    participant Redis as Redis

    User->>CF: https://energy-factory.kr
    CF->>S3: GET /
    S3-->>CF: index.html
    CF-->>User: React 앱 로드

    User->>CF: GET /api/products
    CF->>EC2: 프록시 전달
    EC2->>RDS: SELECT * FROM product
    RDS-->>EC2: 상품 목록
    EC2-->>CF: JSON 응답
    CF-->>User: 상품 데이터

    User->>CF: POST /api/auth/login
    CF->>EC2: 로그인 요청
    EC2->>RDS: 사용자 인증
    RDS-->>EC2: 사용자 정보
    EC2->>Redis: RefreshToken 저장
    EC2-->>CF: JWT 쿠키 설정
    CF-->>User: 로그인 성공
```

## 결제 플로우

```mermaid
  sequenceDiagram
      participant User as 사용자
      participant FE as 프론트엔드
      participant API as Spring Boot
      participant DB as MySQL
      participant Toss as 토스페이먼츠

      User->>FE: 상품 주문
      FE->>API: POST /api/orders
      API->>DB: 재고 확인 (availableStock)
      DB-->>API: 재고 충분
      API->>DB: reservedStock++
      API-->>FE: orderId, orderNumber, amount

      FE->>Toss: 결제창 호출
      Toss->>User: 결제 수단 선택
      User->>Toss: 결제 진행
      Toss->>FE: 결제 승인 요청

      FE->>API: POST /api/payments/toss/confirm
      API->>Toss: 결제 승인 API
      Toss-->>API: status: DONE
      API->>DB: stock--, reservedStock--
      API->>DB: paymentStatus = COMPLETED
      API->>DB: 장바구니에서 주문 상품 삭제
      API-->>FE: 결제 완료
      FE-->>User: 주문 완료 화면

```

## OAuth2 소셜 로그인 플로우

```mermaid
sequenceDiagram
    participant User as 사용자
    participant FE as 프론트엔드
    participant EC2 as Spring Boot
    participant Naver as 네이버 OAuth2
    participant RDS as MySQL
    participant Redis as Redis

    User->>FE: 네이버 로그인 클릭
    FE->>EC2: GET /oauth2/authorization/naver
    EC2-->>User: 네이버 로그인 페이지로 리다이렉트

    User->>Naver: 로그인 & 동의
    Naver-->>EC2: Authorization Code
    EC2->>Naver: Access Token 요청
    Naver-->>EC2: Access Token
    EC2->>Naver: 사용자 프로필 요청
    Naver-->>EC2: 이름, 이메일, 전화번호

    EC2->>RDS: 기존 사용자 확인
    alt 신규 사용자
        EC2->>EC2: 세션에 임시 저장
        EC2-->>FE: 회원가입 페이지로 리다이렉트
        User->>FE: 추가 정보 입력
        FE->>EC2: POST /api/auth/signup-with-oauth
        EC2->>RDS: 신규 사용자 생성
    else 기존 사용자
        EC2->>RDS: 사용자 정보 조회
    end

    EC2->>Redis: RefreshToken 저장
    EC2-->>FE: JWT 쿠키 설정
    FE-->>User: 로그인 완료
```

## 재고 예약 시스템

```mermaid
stateDiagram-v2
    [*] --> Available: 상품 재고 초기화
    Available --> Reserved: 주문 생성 (reservedStock++)
    Reserved --> Sold: 결제 완료 (stock--, reservedStock--)
    Reserved --> Available: 결제 취소 (reservedStock--)
    Reserved --> Available: 15분 타임아웃 (스케줄러)
    Sold --> [*]

    note right of Reserved
        PENDING 상태
        15분 이내 결제 필요
    end note

    note right of Sold
        PAID 상태
        재고 차감 완료
    end note
```

## 데이터베이스 ERD 요약

```mermaid
erDiagram
    USER ||--o{ USER_ADDRESS : has
    USER ||--o{ ORDERS : places
    USER ||--o{ CART_ITEMS : has

    PRODUCT ||--o{ PRODUCT_VARIANT : has
    PRODUCT ||--o{ PRODUCT_NUTRIENTS : has
    PRODUCT ||--o{ PRODUCT_TAGS : tagged
    TAGS ||--o{ PRODUCT_TAGS : tagged

    PRODUCT_VARIANT ||--o{ ORDER_ITEMS : contains
    PRODUCT_VARIANT ||--o{ CART_ITEMS : contains

    ORDERS ||--o{ ORDER_ITEMS : contains
    ORDERS ||--|| PAYMENTS : has

    USER {
        bigint id PK
        string email UK
        string password
        string name
        string phone
        enum role
        enum provider
        datetime created_at
    }

    PRODUCT {
        bigint id PK
        string name
        string description
        int base_price
        string image_url
        datetime created_at
    }

    PRODUCT_VARIANT {
        bigint id PK
        bigint product_id FK
        string size
        int stock
        int reserved_stock
        int price
    }

    ORDERS {
        bigint id PK
        bigint user_id FK
        string order_number UK
        enum status
        int total_amount
        datetime created_at
    }

    PAYMENTS {
        bigint id PK
        bigint order_id FK
        string payment_key UK
        string method
        int amount
        enum status
        datetime approved_at
    }
```

## 네트워크 구성

```mermaid
graph LR
    subgraph "Public Internet"
        Users[사용자들]
    end

    subgraph "AWS Global"
        CF[CloudFront<br/>Edge Locations<br/>전 세계]
        ACM[ACM<br/>us-east-1]
    end

    subgraph "AWS ap-northeast-2 Region"
        subgraph "VPC"
            subgraph "Public Subnet"
                EC2[EC2<br/>Spring Boot<br/>:8080]
            end

            subgraph "Private Subnet"
                RDS[(RDS MySQL<br/>:3306)]
                Redis[(Redis<br/>:6379)]
            end
        end

        S3[S3 Bucket<br/>정적 호스팅]
        SES[SES<br/>SMTP]
    end

    subgraph "External Services"
        Toss[토스페이먼츠 API]
        Naver[네이버 OAuth2]
    end

    Users -->|HTTPS| CF
    CF -->|Origin| S3
    CF -->|Origin| EC2
    ACM -.SSL.- CF

    EC2 --> RDS
    EC2 --> Redis
    EC2 --> SES
    EC2 -->|HTTPS| Toss
    EC2 -->|HTTPS| Naver

    style CF fill:#ff9900
    style S3 fill:#569a31
    style EC2 fill:#ff9900
    style RDS fill:#527fff
    style Redis fill:#dc382d
```

## 보안 구성

```mermaid
graph TB
    subgraph "보안 계층"
        HTTPS[HTTPS Only<br/>TLS 1.2+]
        CORS[CORS 정책<br/>허용 도메인만]
        JWT[JWT 인증<br/>HttpOnly Cookie]
    end

    subgraph "인증 방식"
        Local[일반 회원가입<br/>이메일/비밀번호]
        OAuth[네이버 OAuth2<br/>소셜 로그인]
        Link[계정 연동<br/>LOCAL + NAVER]
    end

    subgraph "토큰 관리"
        Access[Access Token<br/>10분<br/>HttpOnly]
        Refresh[Refresh Token<br/>7일<br/>Redis 저장]
    end

    HTTPS --> CORS
    CORS --> JWT

    Local --> Access
    OAuth --> Access
    Link --> Access

    Access --> Refresh

    style HTTPS fill:#28a745
    style CORS fill:#ffc107
    style JWT fill:#17a2b8
    style Access fill:#007bff
    style Refresh fill:#6610f2
```

## 비용 구조 (월간 예상)

```mermaid
pie title 월간 AWS 비용 구성
    "EC2 t3.small" : 15.0
    "RDS MySQL t3.micro" : 18.0
    "Route 53 Hosted Zone" : 0.5
    "CloudFront 데이터 전송" : 5.0
    "S3 스토리지 + 요청" : 1.0
    "SES 이메일 발송" : 0.5
    "기타 (Route 53 쿼리 등)" : 1.0
```

## 모니터링 포인트 (개선 필요)

```mermaid
mindmap
  root((모니터링))
    애플리케이션
      에러 트래킹
        Sentry 미구현
      APM
        New Relic 미구현
      로그 수집
        systemd journald만 사용
    인프라
      서버 메트릭
        CPU/메모리 모니터링 필요
      데이터베이스
        슬로우 쿼리 로그
        커넥션 풀 모니터링
      네트워크
        CloudFront 트래픽
        API 레이턴시
    비즈니스
      주문 성공률
      결제 실패율
      재고 부족 알림
```

---

## 다이어그램 사용 가이드

### Mermaid 다이어그램 렌더링

이 문서의 다이어그램은 Mermaid 형식으로 작성되었습니다. 다음 환경에서 자동으로 렌더링됩니다:

1. **GitHub** - 마크다운 파일을 GitHub에서 열면 자동 렌더링
2. **VS Code** - Mermaid Preview 확장 설치
3. **IntelliJ IDEA** - 기본 마크다운 뷰어에서 지원
4. **온라인 에디터** - https://mermaid.live 에서 편집 가능

### 다이어그램 내보내기

1. **PNG/SVG 생성**: https://mermaid.live 에서 코드 복사 후 Export
2. **PDF 생성**: VS Code에서 "Markdown PDF" 확장 사용
3. **Draw.io 변환**: Mermaid 코드를 Draw.io로 import 가능

---

## 업데이트 이력

- 2025-11-11: 초기 인프라 다이어그램 작성
  - 전체 아키텍처 다이어그램
  - CI/CD 플로우
  - 사용자 요청 플로우
  - 결제 플로우
  - OAuth2 로그인 플로우
  - 재고 예약 시스템
  - ERD 요약
  - 네트워크 구성
  - 보안 구성
  - 비용 구조

---

## 참고 자료

- [Mermaid 공식 문서](https://mermaid.js.org/)
- [AWS 아키텍처 아이콘](https://aws.amazon.com/architecture/icons/)
- [CloudFront 커스텀 도메인 설정](./cloudfront-custom-domain.md)
- [CI/CD 구축 가이드](./ci-cd/README.md)
