package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.UserAddressCreateRequestDto;
import com.energyfactory.energy_factory.dto.UserAddressResponseDto;
import com.energyfactory.energy_factory.service.UserAddressService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 배송지 관리 컨트롤러
 * 배송지 등록, 조회, 수정, 삭제, 기본 배송지 설정 기능을 제공
 */
@RestController
@RequestMapping("/api/users/{userId}/addresses")
@Tag(name = "User Address", description = "사용자 배송지 관련 API")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    @Operation(summary = "사용자 배송지 목록 조회")
    public ResponseEntity<ApiResponse<List<UserAddressResponseDto>>> getUserAddresses(
            @PathVariable Long userId
    ) {
        List<UserAddressResponseDto> addresses = userAddressService.getUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, addresses));
    }

    @PostMapping
    @Operation(summary = "배송지 등록")
    public ResponseEntity<ApiResponse<UserAddressResponseDto>> createAddress(
            @PathVariable Long userId,
            @Valid @RequestBody UserAddressCreateRequestDto request
    ) {
        UserAddressResponseDto address = userAddressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResultCode.SUCCESS_POST, address));
    }

    @GetMapping("/{addressId}")
    @Operation(summary = "배송지 상세 조회")
    public ResponseEntity<ApiResponse<UserAddressResponseDto>> getAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        UserAddressResponseDto address = userAddressService.getAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, address));
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "배송지 수정")
    public ResponseEntity<ApiResponse<UserAddressResponseDto>> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody UserAddressCreateRequestDto request
    ) {
        UserAddressResponseDto address = userAddressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, address));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "배송지 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        userAddressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @PatchMapping("/{addressId}/default")
    @Operation(summary = "기본 배송지 설정")
    public ResponseEntity<ApiResponse<UserAddressResponseDto>> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        UserAddressResponseDto address = userAddressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, address));
    }

    @GetMapping("/default")
    @Operation(summary = "기본 배송지 조회")
    public ResponseEntity<ApiResponse<UserAddressResponseDto>> getDefaultAddress(
            @PathVariable Long userId
    ) {
        UserAddressResponseDto address = userAddressService.getDefaultAddress(userId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, address));
    }
}