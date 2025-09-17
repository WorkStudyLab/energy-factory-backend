package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.dto.UserAddressCreateRequestDto;
import com.energyfactory.energy_factory.dto.UserAddressResponseDto;
import com.energyfactory.energy_factory.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(
        summary = "배송지 목록 조회",
        description = "현재 로그인한 사용자의 모든 배송지를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserAddressResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<List<UserAddressResponseDto>> getAddresses() {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(List.of());
    }

    @PostMapping
    @Operation(
        summary = "배송지 등록",
        description = "새로운 배송지를 등록합니다. 기본 배송지로 설정 시 기존 기본 배송지는 해제됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "배송지 등록 성공",
            content = @Content(schema = @Schema(implementation = UserAddressResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<UserAddressResponseDto> createAddress(
            @Parameter(description = "배송지 생성 정보", required = true)
            @Valid @RequestBody UserAddressCreateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "배송지 상세 조회",
        description = "특정 배송지의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserAddressResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (본인 배송지가 아님)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "배송지를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<UserAddressResponseDto> getAddress(
            @Parameter(description = "배송지 ID", example = "1")
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "배송지 수정",
        description = "기존 배송지 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = @Content(schema = @Schema(implementation = UserAddressResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (본인 배송지가 아님)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "배송지를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<UserAddressResponseDto> updateAddress(
            @Parameter(description = "배송지 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "수정할 배송지 정보", required = true)
            @Valid @RequestBody UserAddressCreateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "배송지 삭제",
        description = "배송지를 삭제합니다. 기본 배송지가 삭제되면 다른 배송지 중 하나가 자동으로 기본 배송지로 설정됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (본인 배송지가 아님)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "배송지를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "삭제할 수 없음 (유일한 배송지)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "배송지 ID", example = "1")
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/default")
    @Operation(
        summary = "기본 배송지 설정",
        description = "특정 배송지를 기본 배송지로 설정합니다. 기존 기본 배송지는 자동으로 해제됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "기본 배송지 설정 성공"),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (본인 배송지가 아님)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "배송지를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<Void> setDefaultAddress(
            @Parameter(description = "배송지 ID", example = "1")
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }
}