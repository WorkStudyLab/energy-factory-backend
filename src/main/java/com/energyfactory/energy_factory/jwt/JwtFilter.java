package com.energyfactory.energy_factory.jwt;

import com.energyfactory.energy_factory.dto.CustomUserDetails;
import com.energyfactory.energy_factory.entity.User;
import com.energyfactory.energy_factory.utils.enums.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출 시도
        String token = null;
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7); // "Bearer " 제거
            System.out.println("Token from header");
        }

        // 2. 헤더에 없으면 쿠키에서 토큰 추출 시도
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        System.out.println("Token from cookie");
                        break;
                    }
                }
            }
        }

        // 3. 토큰이 없으면 필터 체인 계속 진행
        if (token == null) {
            System.out.println("token null");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 4. 토큰 소멸 시간 검증
            if (jwtUtil.isExpired(token)) {
                System.out.println("token expired");
                filterChain.doFilter(request, response);
                return;
            }

            // 5. 토큰에서 username과 role 획득
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            // role이 null이거나 빈 문자열인 경우 처리
            if (role == null || role.trim().isEmpty()) {
                System.out.println("role is null or empty");
                filterChain.doFilter(request, response);
                return;
            }

            // 6. User 객체 생성 및 인증 정보 설정
            User user = new User();
            user.setEmail(username);
            user.setPassword("temppassword");
            user.setRole(Role.valueOf(role.trim()));

            // 7. UserDetails에 회원 정보 객체 담기
            CustomUserDetails customUserDetails = new CustomUserDetails(user);

            // 8. 스프링 시큐리티 인증 토큰 생성
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
            );
            
            // 9. 세션에 사용자 등록
            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("authentication success: " + username);

        } catch (Exception e) {
            System.out.println("JWT processing error: " + e.getMessage());
            // 토큰이 유효하지 않은 경우 인증 없이 진행
        }

        filterChain.doFilter(request, response);
    }
}
