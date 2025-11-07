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
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 주문번호로 주문 조회
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.payments WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumber(@Param("orderNumber") Long orderNumber);

    // 사용자의 특정 상태 주문 조회
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.payments WHERE o.user = :user AND o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByUserAndStatusOrderByCreatedAtDesc(@Param("user") User user, @Param("status") OrderStatus status, Pageable pageable);

    // 사용자의 특정 결제상태 주문 조회
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.payments WHERE o.user = :user AND o.paymentStatus = :paymentStatus ORDER BY o.createdAt DESC")
    Page<Order> findByUserAndPaymentStatusOrderByCreatedAtDesc(@Param("user") User user, @Param("paymentStatus") PaymentStatus paymentStatus, Pageable pageable);

    // 사용자의 특정 결제상태가 아닌 주문 조회
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.payments WHERE o.user = :user AND o.paymentStatus <> :paymentStatus ORDER BY o.createdAt DESC")
    Page<Order> findByUserAndPaymentStatusNotOrderByCreatedAtDesc(@Param("user") User user, @Param("paymentStatus") PaymentStatus paymentStatus, Pageable pageable);

    // 사용자의 상태별, 결제상태별 주문 조회
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.payments WHERE o.user = :user AND o.status = :status AND o.paymentStatus = :paymentStatus ORDER BY o.createdAt DESC")
    Page<Order> findByUserAndStatusAndPaymentStatusOrderByCreatedAtDesc(
            @Param("user") User user, @Param("status") OrderStatus status, @Param("paymentStatus") PaymentStatus paymentStatus, Pageable pageable);

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

    // 타임아웃된 PENDING 주문 조회 (재고 예약 해제용)
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = :paymentStatus AND o.createdAt < :cutoffTime")
    List<Order> findTimeoutOrders(@Param("paymentStatus") PaymentStatus paymentStatus,
                                   @Param("cutoffTime") LocalDateTime cutoffTime);
}