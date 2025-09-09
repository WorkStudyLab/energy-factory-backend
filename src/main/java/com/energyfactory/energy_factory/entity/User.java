package com.energyfactory.energy_factory.entity;

import com.energyfactory.energy_factory.utils.enums.Role;
import com.energyfactory.energy_factory.utils.enums.Provider;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "varchar(255) COMMENT '이메일'")
    private String email;

    @Column(nullable = false, columnDefinition = "varchar(255) COMMENT '비밀번호'")
    private String password;

    @Column(nullable = false, columnDefinition = "varchar(100) COMMENT '이름'")
    private String name;

    @Column(name = "phone_number", nullable = false, unique = true, columnDefinition = "varchar(20) COMMENT '전화번호'")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50) COMMENT '소셜 로그인 제공자'")
    private Provider provider;

    @Column(name = "provider_id", unique = true, columnDefinition = "varchar(255) COMMENT '소셜 계정 식별자'")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50) COMMENT '권한'")
    private Role role;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP NOT NULL COMMENT '생성일'")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserAddress> addresses = new ArrayList<>();
}
