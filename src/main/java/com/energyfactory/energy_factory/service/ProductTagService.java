package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.ProductListResponseDto;
import com.energyfactory.energy_factory.dto.TagResponseDto;
import com.energyfactory.energy_factory.entity.Product;
import com.energyfactory.energy_factory.entity.ProductTag;
import com.energyfactory.energy_factory.entity.Tag;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.repository.ProductRepository;
import com.energyfactory.energy_factory.repository.ProductTagRepository;
import com.energyfactory.energy_factory.repository.TagRepository;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTagService {

    private final ProductTagRepository productTagRepository;
    private final ProductRepository productRepository;
    private final TagRepository tagRepository;

    @Transactional
    public void addTagToProduct(Long productId, Long tagId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ResultCode.TAG_NOT_FOUND));

        if (productTagRepository.existsByProductAndTag(product, tag)) {
            throw new BusinessException(ResultCode.DUPLICATE_TAG_NAME);
        }

        ProductTag productTag = ProductTag.builder()
                .product(product)
                .tag(tag)
                .build();

        productTagRepository.save(productTag);
    }

    @Transactional
    public void addTagsToProduct(Long productId, List<Long> tagIds) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }

        List<ProductTag> existingProductTags = productTagRepository.findByProduct(product);
        List<Long> existingTagIds = existingProductTags.stream()
                .map(pt -> pt.getTag().getId())
                .collect(Collectors.toList());

        List<ProductTag> newProductTags = tags.stream()
                .filter(tag -> !existingTagIds.contains(tag.getId()))
                .map(tag -> ProductTag.builder()
                        .product(product)
                        .tag(tag)
                        .build())
                .collect(Collectors.toList());

        productTagRepository.saveAll(newProductTags);
    }

    @Transactional
    public void removeTagFromProduct(Long productId, Long tagId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ResultCode.TAG_NOT_FOUND));

        ProductTag productTag = productTagRepository.findByProductAndTag(product, tag)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        productTagRepository.delete(productTag);
    }

    @Transactional
    public void removeAllTagsFromProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        productTagRepository.deleteByProduct(product);
    }

    public List<TagResponseDto> getProductTags(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        List<ProductTag> productTags = productTagRepository.findByProductWithTag(product);
        
        return productTags.stream()
                .map(pt -> convertToTagResponseDto(pt.getTag()))
                .collect(Collectors.toList());
    }

    public ProductListResponseDto getProductsByTag(Long tagId, Pageable pageable) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ResultCode.TAG_NOT_FOUND));

        Page<ProductTag> productTagPage = productTagRepository.findByTagWithProduct(tag, pageable);
        
        List<ProductListResponseDto.ProductSummaryDto> products = productTagPage.getContent().stream()
                .map(pt -> convertToProductSummaryDto(pt.getProduct()))
                .collect(Collectors.toList());

        ProductListResponseDto.PageInfoDto pageInfo = ProductListResponseDto.PageInfoDto.builder()
                .currentPage(productTagPage.getNumber())
                .pageSize(productTagPage.getSize())
                .totalElements(productTagPage.getTotalElements())
                .totalPages(productTagPage.getTotalPages())
                .first(productTagPage.isFirst())
                .last(productTagPage.isLast())
                .build();

        return ProductListResponseDto.builder()
                .products(products)
                .pageInfo(pageInfo)
                .build();
    }

    public ProductListResponseDto getProductsByAllTags(List<Long> tagIds, Pageable pageable) {
        if (tagIds.isEmpty()) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }

        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }

        Page<Product> productPage = productTagRepository.findProductsByAllTags(tagIds, tagIds.size(), pageable);
        
        List<ProductListResponseDto.ProductSummaryDto> products = productPage.getContent().stream()
                .map(this::convertToProductSummaryDto)
                .collect(Collectors.toList());

        ProductListResponseDto.PageInfoDto pageInfo = ProductListResponseDto.PageInfoDto.builder()
                .currentPage(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();

        return ProductListResponseDto.builder()
                .products(products)
                .pageInfo(pageInfo)
                .build();
    }

    public ProductListResponseDto getProductsByAnyTags(List<Long> tagIds, Pageable pageable) {
        if (tagIds.isEmpty()) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }

        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new BusinessException(ResultCode.TAG_NOT_FOUND);
        }

        Page<Product> productPage = productTagRepository.findProductsByAnyTags(tagIds, pageable);
        
        List<ProductListResponseDto.ProductSummaryDto> products = productPage.getContent().stream()
                .map(this::convertToProductSummaryDto)
                .collect(Collectors.toList());

        ProductListResponseDto.PageInfoDto pageInfo = ProductListResponseDto.PageInfoDto.builder()
                .currentPage(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();

        return ProductListResponseDto.builder()
                .products(products)
                .pageInfo(pageInfo)
                .build();
    }

    private TagResponseDto convertToTagResponseDto(Tag tag) {
        return TagResponseDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .productCount((long) tag.getProductTags().size())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }

    private ProductListResponseDto.ProductSummaryDto convertToProductSummaryDto(Product product) {
        return ProductListResponseDto.ProductSummaryDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .category(product.getCategory())
                .status(product.getStatus())
                .build();
    }
}