package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "태그 목록 응답 DTO")
public class TagListResponseDto {

    @Schema(description = "태그 목록")
    private List<TagSummaryDto> tags;

    @Schema(description = "페이징 정보")
    private PageInfoDto pageInfo;

    @Getter
    @Builder
    @Schema(description = "태그 요약 정보 (목록용)")
    public static class TagSummaryDto {
        @Schema(description = "태그 ID", example = "1")
        private Long id;

        @Schema(description = "태그명", example = "고단백")
        private String name;

        @Schema(description = "이 태그가 적용된 상품 수", example = "15")
        private Long productCount;
    }

    @Getter
    @Builder
    @Schema(description = "페이징 정보")
    public static class PageInfoDto {
        @Schema(description = "현재 페이지", example = "1")
        private int currentPage;

        @Schema(description = "페이지 크기", example = "20")
        private int pageSize;

        @Schema(description = "전체 요소 수", example = "150")
        private long totalElements;

        @Schema(description = "전체 페이지 수", example = "8")
        private int totalPages;

        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private boolean first;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private boolean last;
    }
}