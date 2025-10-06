package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.UserProfileService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 프로필 관리 컨트롤러
 * 사용자 프로필 조회, 생성, 수정, 삭제 및 검색 기능을 제공
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "사용자 프로필 관련 API")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping("/{userId}/profile")
    @Operation(summary = "사용자 프로필 생성", description = "새로운 사용자 프로필을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "이미 프로필이 존재함", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserProfileResponseDto>> createProfile(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody UserProfileCreateRequestDto requestDto
    ) {
        UserProfileResponseDto profile = userProfileService.createProfile(userId, requestDto);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profile));
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "사용자 프로필 조회", description = "사용자 ID로 프로필을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserProfileResponseDto>> getProfile(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId
    ) {
        UserProfileResponseDto profile = userProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profile));
    }

    @PutMapping("/{userId}/profile")
    @Operation(summary = "사용자 프로필 수정", description = "사용자 프로필 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserProfileResponseDto>> updateProfile(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto
    ) {
        UserProfileResponseDto profile = userProfileService.updateProfile(userId, requestDto);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profile));
    }

    @DeleteMapping("/{userId}/profile")
    @Operation(summary = "사용자 프로필 삭제", description = "사용자 프로필을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Void>> deleteProfile(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId
    ) {
        userProfileService.deleteProfile(userId);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @PatchMapping("/{userId}/profile/visibility")
    @Operation(summary = "프로필 공개 설정 토글", description = "프로필의 공개/비공개 설정을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공개 설정 변경 성공"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserProfileResponseDto>> toggleVisibility(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId
    ) {
        UserProfileResponseDto profile = userProfileService.toggleVisibility(userId);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profile));
    }

    @PatchMapping("/{userId}/profile/image")
    @Operation(summary = "프로필 이미지 업데이트", description = "프로필 이미지 URL을 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 이미지 업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserProfileResponseDto>> updateProfileImage(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId,
            @RequestParam String profileImageUrl
    ) {
        UserProfileResponseDto profile = userProfileService.updateProfileImage(userId, profileImageUrl);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profile));
    }

    @PatchMapping("/{userId}/profile/bio")
    @Operation(summary = "자기소개 업데이트", description = "프로필의 자기소개를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "자기소개 업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserProfileResponseDto>> updateBio(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId,
            @RequestParam String bio
    ) {
        UserProfileResponseDto profile = userProfileService.updateBio(userId, bio);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profile));
    }

    @GetMapping("/profiles/public")
    @Operation(summary = "공개 프로필 목록 조회", description = "공개된 프로필들을 페이징으로 조회합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Page<UserProfileResponseDto>>> getPublicProfiles(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<UserProfileResponseDto> profiles = userProfileService.getPublicProfiles(pageable);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profiles));
    }

    @GetMapping("/profiles/search/location")
    @Operation(summary = "위치별 프로필 검색", description = "특정 위치의 공개 프로필들을 검색합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Page<UserProfileResponseDto>>> searchByLocation(
            @RequestParam String location,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<UserProfileResponseDto> profiles = userProfileService.searchByLocation(location, pageable);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profiles));
    }

    @GetMapping("/profiles/search/bio")
    @Operation(summary = "자기소개별 프로필 검색", description = "자기소개에 특정 키워드가 포함된 공개 프로필들을 검색합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Page<UserProfileResponseDto>>> searchByBio(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<UserProfileResponseDto> profiles = userProfileService.searchByBio(keyword, pageable);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profiles));
    }

    @GetMapping("/profiles/search/interests")
    @Operation(summary = "관심사별 프로필 검색", description = "관심사에 특정 키워드가 포함된 공개 프로필들을 검색합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Page<UserProfileResponseDto>>> searchByInterests(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<UserProfileResponseDto> profiles = userProfileService.searchByInterests(keyword, pageable);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profiles));
    }

    @GetMapping("/profiles/search/complex")
    @Operation(summary = "복합 검색", description = "위치와 관심사를 조합하여 프로필을 검색합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Page<UserProfileResponseDto>>> searchByLocationAndInterests(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String interests,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<UserProfileResponseDto> profiles = userProfileService.searchByLocationAndInterests(location, interests, pageable);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profiles));
    }

    @GetMapping("/profiles/with-images")
    @Operation(summary = "프로필 이미지가 있는 프로필 조회", description = "프로필 이미지가 등록된 공개 프로필들을 조회합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Page<UserProfileResponseDto>>> getProfilesWithImages(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<UserProfileResponseDto> profiles = userProfileService.getProfilesWithImages(pageable);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profiles));
    }

    @GetMapping("/profiles/with-websites")
    @Operation(summary = "웹사이트가 있는 프로필 조회", description = "웹사이트 URL이 등록된 공개 프로필들을 조회합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Page<UserProfileResponseDto>>> getProfilesWithWebsites(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<UserProfileResponseDto> profiles = userProfileService.getProfilesWithWebsites(pageable);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profiles));
    }

    @GetMapping("/profiles/recent")
    @Operation(summary = "최근 가입한 프로필 조회", description = "최근 N일 내에 생성된 공개 프로필들을 조회합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<List<UserProfileResponseDto>>> getRecentProfiles(
            @RequestParam(defaultValue = "7") int days
    ) {
        List<UserProfileResponseDto> profiles = userProfileService.getRecentProfiles(days);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profiles));
    }

    @GetMapping("/profiles/birth-month/{month}")
    @Operation(summary = "생일 월별 프로필 조회", description = "특정 월에 생일인 공개 프로필들을 조회합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<List<UserProfileResponseDto>>> getProfilesByBirthMonth(
            @Parameter(description = "생일 월 (1-12)", required = true) @PathVariable int month
    ) {
        List<UserProfileResponseDto> profiles = userProfileService.getProfilesByBirthMonth(month);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profiles));
    }

    @GetMapping("/profiles/date-range")
    @Operation(summary = "기간별 프로필 조회", description = "특정 기간 내에 생성된 프로필들을 조회합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<List<UserProfileResponseDto>>> getProfilesByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        List<UserProfileResponseDto> profiles = userProfileService.getProfilesByDateRange(startDate, endDate);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profiles));
    }

    @GetMapping("/profiles/stats/public-count")
    @Operation(summary = "공개 프로필 수 조회", description = "전체 공개 프로필의 개수를 조회합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Long>> getPublicProfilesCount() {
        long count = userProfileService.getPublicProfilesCount();
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, count));
    }

    @GetMapping("/profiles/stats/images-count")
    @Operation(summary = "프로필 이미지가 있는 공개 프로필 수 조회", description = "프로필 이미지가 등록된 공개 프로필의 개수를 조회합니다.")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Long>> getProfilesWithImagesCount() {
        long count = userProfileService.getProfilesWithImagesCount();
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, count));
    }
}