package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
@Tag(name = "OAuth2 테스트", description = "OAuth2 로그인 테스트용 API")
public class OAuth2TestController {
    
    @GetMapping("/user")
    @Operation(summary = "현재 로그인된 사용자 정보 조회", description = "OAuth2 또는 일반 로그인한 사용자의 정보를 반환합니다.")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        
        if (userDetails != null) {
            response.put("email", userDetails.getUsername());
            response.put("name", userDetails.getName());
            response.put("phoneNumber", userDetails.getUser().getPhoneNumber());
            response.put("provider", userDetails.getUser().getProvider());
            response.put("role", userDetails.getUser().getRole());
        } else {
            response.put("message", "로그인되지 않은 사용자입니다.");
        }
        
        return response;
    }
}
