# Energy Factory Backend - SSoT (Single Source of Truth) 참조 가이드

## 개요
이 문서는 프로젝트 내에서 정보의 진실의 원천(SSoT)을 명확히 정의하여 데이터 중복과 불일치를 방지합니다.

## SSoT 매핑 테이블

| 정보 유형 | 진실의 원천 (SSoT) | 참조/파생 위치 | 업데이트 순서 |
|-----------|-------------------|----------------|---------------|
| **데이터 모델** | `docs/erd.txt` | JPA Entity, Repository | ERD → Entity → Repository |
| **API 스펙** | Controller 클래스 | DTO, Service 메서드 | Controller → DTO → Service |
| **비즈니스 로직** | Service 클래스 | Controller, Test | Service → Controller → Test |
| **설정값** | `application.yml` | @Value, @ConfigurationProperties | application.yml → 코드 |
| **상수값** | Enum/Constants 클래스 | 비즈니스 로직 전반 | Constants → 사용 코드 |
| **프로젝트 상태** | `task-history.md` | README.md, 커밋 메시지 | task-history → README |
| **API 문서** | Swagger 어노테이션 | 외부 API 문서 | Controller → Swagger UI |

## 세부 SSoT 규칙

### 1. 데이터베이스 스키마
```
진실의 원천: docs/erd.txt
파생 위치: 
- src/main/java/.../entity/*.java
- src/main/java/.../repository/*.java
- 마이그레이션 스크립트

업데이트 절차:
1. ERD 수정
2. Entity 클래스 업데이트
3. Repository 인터페이스 업데이트
4. 마이그레이션 스크립트 생성
```

### 2. REST API 명세
```
진실의 원천: Controller 클래스
파생 위치:
- DTO 클래스
- Service 메서드 시그니처
- Swagger 문서
- 프론트엔드 API 호출 코드

업데이트 절차:
1. Controller 메서드 수정
2. 관련 DTO 업데이트
3. Service 메서드 시그니처 변경
4. Swagger 어노테이션 업데이트
```

### 3. 비즈니스 규칙
```
진실의 원천: Service 클래스
파생 위치:
- Controller 로직
- Validation 규칙
- 테스트 케이스
- 문서화

업데이트 절차:
1. Service 메서드 구현
2. Controller에서 Service 호출
3. Validation 규칙 적용
4. 테스트 케이스 작성
```

### 4. 설정 관리
```
진실의 원천: application.yml
파생 위치:
- @Value 어노테이션
- @ConfigurationProperties 클래스
- 환경별 설정 파일

업데이트 절차:
1. application.yml 수정
2. 관련 @Value 또는 @ConfigurationProperties 업데이트
3. 환경별 설정 파일 동기화
```

## SSoT 위반 시나리오 및 해결방안

### ❌ 잘못된 예시들

#### 1. 데이터 모델 중복 정의
```java
// 잘못됨: ERD와 다른 필드 구조
@Entity
public class User {
    private String email;        // ERD에는 username으로 정의됨
    private String displayName;  // ERD에 없는 필드
}
```

#### 2. API 스펙 불일치
```java
// Controller
public ResponseEntity<UserDto> getUser(@PathVariable Long id)

// Service (잘못됨: 다른 반환 타입)
public User findUserById(Long id)  // UserDto가 아닌 User 반환
```

#### 3. 하드코딩된 상수
```java
// 잘못됨: 매직 넘버 사용
if (user.getAge() >= 19) {  // 성인 나이 기준이 하드코딩됨
    // ...
}
```

### ✅ 올바른 예시들

#### 1. ERD 기반 엔티티 설계
```java
// ERD 확인 후 정확한 필드 구조로 구현
@Entity
public class User {
    private String username;     // ERD와 일치
    private String email;        // ERD와 일치
    // ERD에 정의된 필드만 사용
}
```

#### 2. Controller 기반 API 일관성
```java
// Controller
public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id)

// Service (일치: 같은 반환 타입)
public UserDto findUserById(Long id)  // Controller와 일치하는 반환 타입
```

#### 3. 설정 기반 상수 관리
```java
// application.yml
business:
  adult-age: 19

// 코드
@Value("${business.adult-age}")
private int adultAge;

if (user.getAge() >= adultAge) {
    // ...
}
```

## SSoT 체크리스트

### 작업 시작 전 확인사항
- [ ] 관련 ERD 확인 (데이터 모델 작업 시)
- [ ] 해당 Controller 확인 (API 관련 작업 시)
- [ ] application.yml 확인 (설정 관련 작업 시)
- [ ] 기존 Service 로직 확인 (비즈니스 로직 작업 시)

### 작업 완료 후 확인사항
- [ ] SSoT와 모든 파생 위치 일치 확인
- [ ] 중복 정의된 정보 제거
- [ ] 관련 테스트 케이스 업데이트
- [ ] 문서 업데이트 (필요 시)

### 코드 리뷰 시 확인사항
- [ ] 새로운 정보의 SSoT 명확히 정의됨
- [ ] 기존 SSoT 위반하지 않음
- [ ] 하드코딩된 값 없음
- [ ] 중복 로직 없음

## 자주 발생하는 SSoT 위반 패턴

### 1. "복사-붙여넣기" 중복
```java
// 잘못됨: 같은 검증 로직을 여러 곳에 복사
public class UserController {
    public void validateUser(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}

public class UserService {
    public void validateUser(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}

// 올바름: 공통 로직을 한 곳에서 관리
@Component
public class UserValidator {
    public void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}
```

### 2. 설정값 분산
```java
// 잘못됨: 같은 설정값을 여러 곳에 하드코딩
public class OrderService {
    private static final int MAX_ORDER_ITEMS = 10;
}

public class CartService {
    private static final int MAX_CART_ITEMS = 10;  // 중복!
}

// 올바름: 설정 파일에서 중앙 관리
// application.yml
business:
  max-items-per-order: 10

// 코드에서 참조
@Value("${business.max-items-per-order}")
private int maxItemsPerOrder;
```

## 마무리

SSoT 원칙을 준수하면:
- **데이터 일관성 보장**
- **유지보수 비용 절감**
- **버그 발생률 감소**
- **코드 품질 향상**

모든 작업 시 "이 정보의 진실의 원천은 무엇인가?"를 먼저 확인하는 습관을 가져야 합니다.