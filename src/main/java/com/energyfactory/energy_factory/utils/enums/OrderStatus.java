package com.energyfactory.energy_factory.utils.enums;

public enum OrderStatus {
    PENDING("주문접수"),
    CONFIRMED("주문확인"), 
    SHIPPED("배송중"),
    DELIVERED("배송완료"),
    CANCELLED("주문취소");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}