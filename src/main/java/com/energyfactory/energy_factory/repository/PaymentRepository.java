package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.Order;
import com.energyfactory.energy_factory.entity.Payment;
import com.energyfactory.energy_factory.utils.enums.PaymentMethod;
import com.energyfactory.energy_factory.utils.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 결제 데이터 접근 레포지토리
 * 결제 조회, 검색, 통계 기능을 위한 쿼리 메서드 제공
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 주문별 결제 조회
     */
    Optional<Payment> findByOrder(Order order);

    /**
     * 주문 ID로 결제 조회
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * 거래 ID로 결제 조회
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * 결제 상태별 조회
     */
    Page<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus, Pageable pageable);

    /**
     * 결제 수단별 조회
     */
    Page<Payment> findByPaymentMethodOrderByCreatedAtDesc(PaymentMethod paymentMethod, Pageable pageable);

    /**
     * 결제 상태 + 결제 수단 조합 조회
     */
    Page<Payment> findByPaymentStatusAndPaymentMethodOrderByCreatedAtDesc(
            PaymentStatus paymentStatus, PaymentMethod paymentMethod, Pageable pageable);

    /**
     * 금액 범위별 결제 조회
     */
    Page<Payment> findByAmountBetweenOrderByCreatedAtDesc(
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * 날짜 범위별 결제 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    Page<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate, 
                                  Pageable pageable);

    /**
     * 결제 완료된 항목만 조회
     */
    Page<Payment> findByPaymentStatusAndPaidAtIsNotNullOrderByPaidAtDesc(
            PaymentStatus paymentStatus, Pageable pageable);

    /**
     * 실패한 결제 조회
     */
    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus);

    /**
     * 결제 수단별 통계 (총 금액)
     */
    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p " +
           "WHERE p.paymentStatus = :status GROUP BY p.paymentMethod")
    List<Object[]> findPaymentSummaryByMethod(@Param("status") PaymentStatus status);

    /**
     * 일별 결제 통계
     */
    @Query("SELECT DATE(p.createdAt), COUNT(p), SUM(p.amount) FROM Payment p " +
           "WHERE p.paymentStatus = :status AND p.createdAt >= :fromDate " +
           "GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt) DESC")
    List<Object[]> findDailyPaymentStats(@Param("status") PaymentStatus status, 
                                        @Param("fromDate") LocalDateTime fromDate);

    /**
     * 환불 가능한 결제 조회 (완료 상태)
     */
    List<Payment> findByPaymentStatusAndCreatedAtAfter(PaymentStatus paymentStatus, LocalDateTime date);
}