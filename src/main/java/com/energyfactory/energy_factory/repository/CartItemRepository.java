package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.CartItem;
import com.energyfactory.energy_factory.entity.User;
import com.energyfactory.energy_factory.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 사용자의 모든 장바구니 아이템 조회 (최신순)
    List<CartItem> findByUserOrderByCreatedAtDesc(User user);

    // 사용자의 특정 variant 장바구니 아이템 조회 (중복 체크용)
    Optional<CartItem> findByUserAndProductVariant(User user, ProductVariant productVariant);

    // 사용자의 장바구니 전체 삭제
    void deleteByUser(User user);

    // 사용자의 특정 아이템들 삭제 (선택 삭제용)
    void deleteByUserAndIdIn(User user, List<Long> ids);

    // 사용자와 ID로 조회 (권한 체크용)
    Optional<CartItem> findByUserAndId(User user, Long id);

}
