package com.energyfactory.energy_factory.utils.enums;

/**
 * 결제 수단 enum
 * 다양한 결제 방식을 타입 안전하게 관리
 */
public enum PaymentMethod {
    CREDIT_CARD("신용카드"),
    DEBIT_CARD("체크카드"),
    BANK_TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE_PAYMENT("모바일결제"),
    KAKAO_PAY("카카오페이"),
    NAVER_PAY("네이버페이"),
    TOSS_PAY("토스페이"),
    PAYCO("페이코"),
    POINT("포인트"),
    COUPON("쿠폰");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}