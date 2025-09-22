package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.Order;
import com.energyfactory.energy_factory.entity.User;
import com.energyfactory.energy_factory.utils.enums.OrderStatus;
import com.energyfactory.energy_factory.utils.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 주문번호로 주문 조회
    Optional<Order> findByOrderNumber(Long orderNumber);

    // 사용자의 주문 목록 조회 (최신순)
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 사용자의 특정 상태 주문 조회
    Page<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status, Pageable pageable);

    // 사용자의 특정 결제상태 주문 조회
    Page<Order> findByUserAndPaymentStatusOrderByCreatedAtDesc(User user, PaymentStatus paymentStatus, Pageable pageable);

    // 사용자의 상태별, 결제상태별 주문 조회
    Page<Order> findByUserAndStatusAndPaymentStatusOrderByCreatedAtDesc(
            User user, OrderStatus status, PaymentStatus paymentStatus, Pageable pageable);

    // 기간별 주문 조회
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    Page<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate, 
                                Pageable pageable);

    // 사용자의 기간별 주문 조회
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    Page<Order> findByUserAndDateRange(@Param("user") User user,
                                       @Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate, 
                                       Pageable pageable);

    // 주문 상태별 통계
    long countByStatus(OrderStatus status);

    // 사용자의 주문 수 조회
    long countByUser(User user);

    // 사용자의 특정 상태 주문 수
    long countByUserAndStatus(User user, OrderStatus status);

    // 특정 사용자의 모든 주문 삭제 (탈퇴 시 사용)
    void deleteByUser(User user);
}