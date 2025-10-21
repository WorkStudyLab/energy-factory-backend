package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.CartService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 장바구니 관리 컨트롤러
 * 장바구니 조회, 추가, 수량 변경, 삭제 등의 기능을 제공
 * 모든 API는 JWT 인증이 필요합니다
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "장바구니 관련 API (인증 필수)")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "장바구니 조회", description = "로그인한 사용자의 장바구니 목록과 총 금액을 조회합니다")
    public ResponseEntity<ApiResponse<CartListResponseDto>> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        CartListResponseDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, cart));
    }

    @PostMapping
    @Operation(summary = "장바구니 추가", description = "상품을 장바구니에 추가합니다. 이미 담긴 상품이면 수량이 증가합니다")
    public ResponseEntity<ApiResponse<CartItemResponseDto>> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartItemAddRequestDto request
    ) {
        Long userId = userDetails.getUser().getId();
        CartItemResponseDto cartItem = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResultCode.SUCCESS_POST, cartItem));
    }

    @PatchMapping("/{cartItemId}")
    @Operation(summary = "장바구니 수량 변경", description = "장바구니 아이템의 수량을 변경합니다")
    public ResponseEntity<ApiResponse<CartItemResponseDto>> updateCartItemQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequestDto request
    ) {
        Long userId = userDetails.getUser().getId();
        CartItemResponseDto cartItem = cartService.updateCartItemQuantity(userId, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, cartItem));
    }

    @DeleteMapping("/{cartItemId}")
    @Operation(summary = "장바구니 아이템 삭제", description = "장바구니에서 특정 아이템을 삭제합니다")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId
    ) {
        Long userId = userDetails.getUser().getId();
        cartService.deleteCartItem(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @DeleteMapping
    @Operation(summary = "장바구니 전체 삭제", description = "장바구니의 모든 아이템을 삭제합니다")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @DeleteMapping("/selected")
    @Operation(summary = "장바구니 선택 삭제", description = "선택한 장바구니 아이템들을 삭제합니다")
    public ResponseEntity<ApiResponse<Void>> deleteSelectedCartItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<Long> cartItemIds
    ) {
        Long userId = userDetails.getUser().getId();
        cartService.deleteSelectedCartItems(userId, cartItemIds);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }
}
