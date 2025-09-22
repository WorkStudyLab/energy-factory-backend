package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.dto.UserAddressCreateRequestDto;
import com.energyfactory.energy_factory.dto.UserAddressResponseDto;
import com.energyfactory.energy_factory.service.UserService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 배송지 관리 컨트롤러
 * 배송지 등록, 조회, 수정, 삭제, 기본 배송지 설정 기능을 제공
 */
@RestController
@RequestMapping("/api/users/addresses")
@Tag(name = "User Address", description = "사용자 배송지 관련 API")
public class UserAddressController {

    private final UserService userService;

    @Autowired
    public UserAddressController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "배송지 목록 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<List<UserAddressResponseDto>>> getAddresses() {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, List.of()));
    }

    @PostMapping
    @Operation(summary = "배송지 등록")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserAddressResponseDto>> createAddress(
            @Valid @RequestBody UserAddressCreateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ResultCode.SUCCESS_POST, null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "배송지 상세 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserAddressResponseDto>> getAddress(
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "배송지 수정")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserAddressResponseDto>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody UserAddressCreateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "배송지 삭제")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Void>> deleteAddress(
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @PatchMapping("/{id}/default")
    @Operation(summary = "기본 배송지 설정")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Void>> setDefaultAddress(
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }
}