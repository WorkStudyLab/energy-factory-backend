package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.User;
import com.energyfactory.energy_factory.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);

    Optional<UserAddress> findByUserAndIsDefaultTrue(User user);

    Optional<UserAddress> findByUserAndId(User user, Long id);

    boolean existsByUserAndIsDefaultTrue(User user);

    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = false WHERE ua.user = :user AND ua.isDefault = true")
    void clearDefaultForUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = false WHERE ua.user = :user AND ua.id != :excludeId")
    void clearOtherDefaultsForUser(@Param("user") User user, @Param("excludeId") Long excludeId);

    long countByUser(User user);

    void deleteByUser(User user);
}