package com.energyfactory.energy_factory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * OAuth2 소셜 로그인 컨트롤러
 * 네이버 등 소셜 로그인 엔드포인트를 문서화하고 제공합니다.
 */
@RestController
@RequestMapping("/api/oauth2")
@Tag(name = "OAuth2 소셜 로그인", description = "네이버 등 소셜 로그인 관련 API")
public class OAuth2Controller {

    @GetMapping("/naver")
    @Operation(
        summary = "네이버 소셜 로그인",
        description = """
            네이버 OAuth2 로그인을 시작합니다.

            ## 전체 플로우
            1. 이 API를 호출하면 네이버 로그인 페이지로 자동 리다이렉트됩니다.
            2. 사용자가 네이버에서 로그인하고 권한을 승인합니다.
            3. 백엔드가 자동으로 다음을 처리합니다:
               - 새로운 사용자: 자동 회원가입
               - 기존 사용자: 정보 업데이트 및 로그인
               - LOCAL 계정이 있는 경우: 네이버 계정과 연동
            4. JWT Access Token과 Refresh Token이 HttpOnly 쿠키로 저장됩니다.
            5. 설정된 프론트엔드 URL로 리다이렉트됩니다.

            ## 인증 후 사용 가능한 API
            - `GET /api/users/me` - 현재 사용자 정보 조회
            - `GET /api/users/profile` - 프로필 조회
            - `PUT /api/users/additional-info` - 추가 정보 입력 (생일, 주소 등)

            ## 참고사항
            - Swagger UI에서 "Try it out"으로 테스트하면 브라우저가 리다이렉트됩니다.
            - 로그인 완료 후 JWT 토큰은 자동으로 쿠키에 저장됩니다.
            - 쿠키는 HttpOnly, Secure, SameSite=None 속성으로 보호됩니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "302",
            description = "네이버 로그인 페이지로 리다이렉트",
            content = @Content(
                mediaType = "text/html",
                examples = @ExampleObject(
                    value = "Redirecting to Naver OAuth2..."
                )
            )
        )
    })
    public void naverLogin(HttpServletResponse response) throws IOException {
        // Spring Security OAuth2가 실제 처리를 담당
        // 이 메서드는 문서화 목적으로만 사용
        response.sendRedirect("/oauth2/authorization/naver");
    }
}
