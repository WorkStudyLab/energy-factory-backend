package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.Product;
import com.energyfactory.energy_factory.entity.ProductTag;
import com.energyfactory.energy_factory.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {

    boolean existsByProductAndTag(Product product, Tag tag);

    Optional<ProductTag> findByProductAndTag(Product product, Tag tag);

    List<ProductTag> findByProduct(Product product);

    List<ProductTag> findByTag(Tag tag);

    @Query("SELECT pt FROM ProductTag pt JOIN FETCH pt.product WHERE pt.tag = :tag")
    Page<ProductTag> findByTagWithProduct(@Param("tag") Tag tag, Pageable pageable);

    @Query("SELECT pt FROM ProductTag pt JOIN FETCH pt.tag WHERE pt.product = :product")
    List<ProductTag> findByProductWithTag(@Param("product") Product product);

    @Query("SELECT pt.product FROM ProductTag pt WHERE pt.tag.id IN :tagIds GROUP BY pt.product HAVING COUNT(DISTINCT pt.tag.id) = :tagCount")
    Page<Product> findProductsByAllTags(@Param("tagIds") List<Long> tagIds, @Param("tagCount") long tagCount, Pageable pageable);

    @Query("SELECT pt.product FROM ProductTag pt WHERE pt.tag.id IN :tagIds GROUP BY pt.product")
    Page<Product> findProductsByAnyTags(@Param("tagIds") List<Long> tagIds, Pageable pageable);

    void deleteByProduct(Product product);

    void deleteByTag(Tag tag);
}