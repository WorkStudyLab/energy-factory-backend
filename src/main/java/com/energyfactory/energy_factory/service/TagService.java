package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.TagCreateRequestDto;
import com.energyfactory.energy_factory.dto.TagListResponseDto;
import com.energyfactory.energy_factory.dto.TagResponseDto;
import com.energyfactory.energy_factory.dto.TagUpdateRequestDto;
import com.energyfactory.energy_factory.entity.Tag;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.repository.TagRepository;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 태그 비즈니스 로직 서비스
 * 태그 생성, 조회, 수정, 삭제 기능을 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    /**
     * 태그 생성
     */
    @Transactional
    public TagResponseDto createTag(TagCreateRequestDto createRequestDto) {
        // 태그명 중복 체크
        if (tagRepository.existsByName(createRequestDto.getName())) {
            throw new BusinessException(ResultCode.DUPLICATE_TAG_NAME);
        }

        Tag tag = Tag.builder()
                .name(createRequestDto.getName())
                .build();

        tagRepository.save(tag);
        return convertToTagResponseDto(tag);
    }

    /**
     * 태그 상세 조회
     */
    public TagResponseDto getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.TAG_NOT_FOUND));

        return convertToTagResponseDto(tag);
    }

    /**
     * 태그명으로 태그 조회
     */
    public TagResponseDto getTagByName(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ResultCode.TAG_NOT_FOUND));

        return convertToTagResponseDto(tag);
    }

    /**
     * 태그 목록 조회 (전체, 페이징)
     */
    public TagListResponseDto getAllTags(Pageable pageable) {
        Page<Tag> tagPage = tagRepository.findAllByOrderByNameAsc(pageable);
        return buildTagListResponse(tagPage);
    }

    /**
     * 태그 검색 (이름 기준 부분 일치)
     */
    public TagListResponseDto searchTags(String keyword, Pageable pageable) {
        Page<Tag> tagPage = tagRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return buildTagListResponse(tagPage);
    }

    /**
     * 인기 태그 조회 (상품 연결 수 기준)
     */
    public TagListResponseDto getPopularTags(Pageable pageable) {
        Page<Tag> tagPage = tagRepository.findPopularTags(pageable);
        return buildTagListResponse(tagPage);
    }

    /**
     * 태그 수정
     */
    @Transactional
    public TagResponseDto updateTag(Long id, TagUpdateRequestDto updateRequestDto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.TAG_NOT_FOUND));

        // 다른 태그가 동일한 이름을 사용하는지 체크
        if (!tag.getName().equals(updateRequestDto.getName()) &&
            tagRepository.existsByName(updateRequestDto.getName())) {
            throw new BusinessException(ResultCode.DUPLICATE_TAG_NAME);
        }

        Tag updatedTag = Tag.builder()
                .id(tag.getId())
                .name(updateRequestDto.getName())
                .createdAt(tag.getCreatedAt())
                .productTags(tag.getProductTags())
                .build();

        tagRepository.save(updatedTag);
        return convertToTagResponseDto(updatedTag);
    }

    /**
     * 태그 삭제
     */
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.TAG_NOT_FOUND));

        tagRepository.delete(tag);
    }

    /**
     * 태그 목록 응답 DTO 구성
     */
    private TagListResponseDto buildTagListResponse(Page<Tag> tagPage) {
        List<TagListResponseDto.TagSummaryDto> tags = tagPage.getContent().stream()
                .map(this::convertToTagSummaryDto)
                .collect(Collectors.toList());

        TagListResponseDto.PageInfoDto pageInfo = TagListResponseDto.PageInfoDto.builder()
                .currentPage(tagPage.getNumber())
                .pageSize(tagPage.getSize())
                .totalElements(tagPage.getTotalElements())
                .totalPages(tagPage.getTotalPages())
                .first(tagPage.isFirst())
                .last(tagPage.isLast())
                .build();

        return TagListResponseDto.builder()
                .tags(tags)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * Tag 엔티티를 TagResponseDto로 변환
     */
    private TagResponseDto convertToTagResponseDto(Tag tag) {
        return TagResponseDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .productCount((long) tag.getProductTags().size())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }

    /**
     * Tag 엔티티를 TagSummaryDto로 변환 (목록용)
     */
    private TagListResponseDto.TagSummaryDto convertToTagSummaryDto(Tag tag) {
        return TagListResponseDto.TagSummaryDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .productCount((long) tag.getProductTags().size())
                .build();
    }
}