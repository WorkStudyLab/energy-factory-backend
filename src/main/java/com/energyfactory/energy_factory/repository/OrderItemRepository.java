package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.Order;
import com.energyfactory.energy_factory.entity.OrderItem;
import com.energyfactory.energy_factory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 특정 주문의 주문 항목들 조회
    List<OrderItem> findByOrder(Order order);

    // 특정 상품이 포함된 주문 항목들 조회
    List<OrderItem> findByProduct(Product product);

    // 특정 주문의 주문 항목 수 조회
    long countByOrder(Order order);

    // 특정 상품의 총 주문 수량 조회
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product = :product")
    Long getTotalQuantityByProduct(@Param("product") Product product);

    // 특정 주문에서 특정 상품의 주문 항목 조회
    OrderItem findByOrderAndProduct(Order order, Product product);

    // 주문 삭제 시 관련 주문 항목들 삭제
    void deleteByOrder(Order order);
}