package com.energyfactory.energy_factory.utils.enums;

public enum PaymentStatus {
    PENDING("결제대기"),
    COMPLETED("결제완료"),
    FAILED("결제실패"),
    CANCELLED("결제취소"),
    REFUNDED("환불완료");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}