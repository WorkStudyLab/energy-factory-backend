# Swagger API 문서 작성 가이드

Energy Factory 프로젝트의 Swagger API 문서 작성 표준 가이드입니다.

## 🛠️ 기본 설정

### Dependencies
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0'
```

### SwaggerConfig
- JWT 인증 스키마 포함
- 프로젝트 기본 정보 설정
- Security 요구사항 정의

## 📝 Controller 작성 규칙

### 1. Controller 레벨 어노테이션

```java
@RestController
@RequestMapping("/api/도메인명")
@Tag(name = "도메인명", description = "도메인 설명")
public class DomainController {
    // ...
}
```

**규칙:**
- `@Tag` name: 영문 단수형 (Product, User, Order)
- 관리자 API: `@RequestMapping("/api/admin/도메인명")`, `@Tag(name = "Admin 도메인명")`
- description: 한글로 간단히 설명

### 2. Method 레벨 어노테이션

```java
@GetMapping("/{id}")
@Operation(
    summary = "리소스 조회",
    description = "상세한 기능 설명을 작성합니다."
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ResponseDto.class))
    ),
    @ApiResponse(
        responseCode = "404",
        description = "리소스를 찾을 수 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    ),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
})
public ResponseEntity<ResponseDto> getResource(
    @Parameter(description = "리소스 ID", example = "1")
    @PathVariable Long id
) {
    // 구현
}
```

### 3. Parameter 어노테이션

```java
// Path Variable
@Parameter(description = "리소스 ID", example = "1")
@PathVariable Long id

// Request Parameter (필수)
@Parameter(description = "검색 키워드", example = "검색어", required = true)
@RequestParam String keyword

// Request Parameter (선택)
@Parameter(description = "페이지 크기", example = "20")
@RequestParam(defaultValue = "20") Integer size

// Request Body
@Parameter(description = "생성 요청 데이터", required = true)
@Valid @RequestBody CreateRequestDto request

// Pageable
@Parameter(description = "페이징 정보")
@PageableDefault(size = 20) Pageable pageable
```

## 📋 응답 코드 표준

### 일반 사용자 API
- `200`: 조회/수정 성공
- `201`: 생성 성공
- `204`: 삭제 성공
- `400`: 잘못된 요청
- `404`: 리소스 없음
- `500`: 서버 오류

### 관리자 API (추가)
- `401`: 인증 실패
- `403`: 권한 없음
- `409`: 비즈니스 로직 충돌 (예: 삭제 불가)

### 에러 응답 스키마
모든 4xx, 5xx 응답에는 `ErrorResponseDto` 스키마 적용:

```java
@ApiResponse(
    responseCode = "400",
    description = "잘못된 요청",
    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
)
```

## 🏗️ DTO 작성 규칙

### 1. DTO 클래스 레벨

```java
@Getter
@Builder // 또는 @Setter
@Schema(description = "리소스 응답 DTO")
public class ResourceResponseDto {
    // 필드들
}
```

### 2. 필드 레벨 어노테이션

```java
@Schema(description = "필드 설명", example = "예제값")
private String field;

// 필수 필드 (Request DTO)
@Schema(description = "필수 필드", example = "예제값", required = true)
private String requiredField;

// 날짜/시간
@Schema(description = "생성일", example = "2024-01-01T10:00:00")
private LocalDateTime createdAt;

// 숫자
@Schema(description = "가격", example = "29900.00")
private BigDecimal price;

// 배열/리스트
@Schema(description = "태그 목록")
private List<TagDto> tags;
```

### 3. 중첩 클래스

```java
@Getter
@Builder
@Schema(description = "중첩 객체")
public static class NestedDto {
    @Schema(description = "중첩 필드", example = "값")
    private String nestedField;
}
```

## 🔐 인증이 필요한 API

관리자 API의 경우 인증 관련 응답 코드를 반드시 포함:

```java
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공"),
    @ApiResponse(
        responseCode = "401", 
        description = "인증 실패",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    ),
    @ApiResponse(
        responseCode = "403", 
        description = "권한 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
    )
})
```

## 📁 파일 구조

```
src/main/java/com/energyfactory/energy_factory/
├── config/
│   └── SwaggerConfig.java
├── controller/
│   ├── ProductController.java          # 일반 사용자 API
│   ├── AdminProductController.java     # 관리자 API
│   └── UserController.java
└── dto/
    ├── ProductResponseDto.java         # 응답 DTO
    ├── ProductCreateRequestDto.java    # 요청 DTO
    └── ErrorResponseDto.java          # 에러 응답 DTO
```

## ✅ 체크리스트

새 도메인 API 작성 시 확인 사항:

- [ ] Controller에 `@Tag` 어노테이션 추가
- [ ] 모든 endpoint에 `@Operation` 추가
- [ ] 모든 endpoint에 `@ApiResponses` 추가
- [ ] 모든 parameter에 `@Parameter` 추가
- [ ] Response DTO에 `@Schema` 어노테이션 추가
- [ ] Request DTO에 validation과 함께 `@Schema` 추가
- [ ] 에러 응답에 `ErrorResponseDto` 스키마 적용
- [ ] 관리자 API에 인증 관련 응답 코드 포함

## 🎯 예제 참고

- **완성된 예제**: `ProductController.java`, `AdminProductController.java`
- **DTO 예제**: `ProductResponseDto.java`, `ProductCreateRequestDto.java`
- **설정 예제**: `SwaggerConfig.java`

이 가이드를 참고하여 다른 도메인의 API 문서를 일관성 있게 작성하세요.