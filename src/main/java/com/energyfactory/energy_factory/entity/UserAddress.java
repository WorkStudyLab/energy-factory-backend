package com.energyfactory.energy_factory.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_address")
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT NOT NULL COMMENT '배송지 아이디'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BIGINT NOT NULL COMMENT '사용자 아이디'")
    private User user;

    @Column(name = "recipient_name", nullable = false, columnDefinition = "VARCHAR(100) NOT NULL COMMENT '수령인'")
    private String recipientName;

    @Column(name = "phone", columnDefinition = "VARCHAR(20) COMMENT '수령인 연락처'")
    private String phone;

    @Column(name = "postal_code", nullable = false, columnDefinition = "VARCHAR(10) NOT NULL COMMENT '우편번호'")
    private String postalCode;

    @Column(name = "address_line1", nullable = false, columnDefinition = "TEXT NOT NULL COMMENT '기본주소'")
    private String addressLine1;

    @Column(name = "is_default", columnDefinition = "BOOLEAN COMMENT '기본 배송지 여부'")
    private Boolean isDefault;

    @Column(name = "address_line2", columnDefinition = "TEXT COMMENT '상세주소'")
    private String addressLine2;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '생성일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;

}
