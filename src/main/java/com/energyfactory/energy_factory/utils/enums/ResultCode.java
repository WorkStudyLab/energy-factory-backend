package com.energyfactory.energy_factory.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(HttpStatus.OK, "20000000", "요청이 성공적으로 처리되었습니다."),
    SUCCESS_POST(HttpStatus.CREATED,"20000001","회원 가입 성공"),
    LOGIN_SUCCESS(HttpStatus.OK, "20000002", "로그인이 성공적으로 처리되었습니다."),
    TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "20000003", "토큰이 성공적으로 갱신되었습니다."),
    
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "40000001", "인증 정보가 유효하지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "40000002", "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "40000009", "이미 사용 중인 이메일입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "40100000", "로그인에 실패했습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "40100003", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "40100004", "만료된 리프레시 토큰입니다."),
    INVALID_TOKEN_TYPE(HttpStatus.BAD_REQUEST, "40100005", "유효하지 않은 토큰 타입입니다."),
    REFRESH_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "40100006", "리프레시 토큰이 필요합니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT, "40000010", "이미 사용 중인 전화번호입니다."),

    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "40100001", "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "40100002", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "40100003", "현재 비밀번호가 올바르지 않습니다."),

    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "40200001", "태그를 찾을 수 없습니다."),
    DUPLICATE_TAG_NAME(HttpStatus.CONFLICT, "40200002", "이미 사용 중인 태그명입니다."),

    // Order 도메인 에러 코드
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "40300001", "상품의 재고가 부족합니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "40300002", "상품 가격이 일치하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "40300003", "해당 주문에 접근할 권한이 없습니다."),
    CANNOT_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "40300004", "취소할 수 없는 주문 상태입니다."),

    // Payment 도메인 에러 코드
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "40400001", "잘못된 요청입니다."),

    // UserProfile 도메인 에러 코드
    DUPLICATE_REQUEST(HttpStatus.CONFLICT, "40500001", "이미 프로필이 존재합니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "40500002", "프로필을 찾을 수 없습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "50000000", "서버에 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String desc;

}
