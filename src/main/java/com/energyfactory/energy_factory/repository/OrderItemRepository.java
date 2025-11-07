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

    // 특정 상품의 총 주문 수량 조회
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product = :product")
    Long getTotalQuantityByProduct(@Param("product") Product product);

}