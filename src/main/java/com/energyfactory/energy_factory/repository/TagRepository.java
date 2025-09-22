package com.energyfactory.energy_factory.repository;

import com.energyfactory.energy_factory.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 태그 데이터 접근 레포지토리
 * 태그 조회, 검색, 중복 체크 기능을 위한 쿼리 메서드 제공
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 태그명 중복 체크
     */
    boolean existsByName(String name);
    
    /**
     * 태그명으로 태그 조회
     */
    Optional<Tag> findByName(String name);
    
    /**
     * 태그명 검색 (부분 일치)
     */
    Page<Tag> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    
    /**
     * 인기 태그 조회 (ProductTag 개수 기준)
     */
    @Query("SELECT t FROM Tag t LEFT JOIN t.productTags pt " +
           "GROUP BY t.id ORDER BY COUNT(pt) DESC")
    Page<Tag> findPopularTags(Pageable pageable);
    
    /**
     * 태그명 기준 정렬된 전체 태그 조회
     */
    Page<Tag> findAllByOrderByNameAsc(Pageable pageable);
}