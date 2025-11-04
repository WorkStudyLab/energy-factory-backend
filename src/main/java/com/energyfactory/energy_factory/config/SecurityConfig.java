package com.energyfactory.energy_factory.config;

import com.energyfactory.energy_factory.exception.CustomAuthenticationEntryPoint;
import com.energyfactory.energy_factory.handler.OAuth2SuccessHandler;
import com.energyfactory.energy_factory.jwt.JwtFilter;
import com.energyfactory.energy_factory.jwt.JwtUtil;
import com.energyfactory.energy_factory.jwt.LoginFilter;
import com.energyfactory.energy_factory.repository.UserRepository;
import com.energyfactory.energy_factory.service.CustomOAuth2UserService;
import com.energyfactory.energy_factory.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final UserRepository userRepository;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JwtUtil jwtUtil,
                         RefreshTokenService refreshTokenService, CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                         CustomOAuth2UserService customOAuth2UserService, OAuth2SuccessHandler oAuth2SuccessHandler,
                         UserRepository userRepository) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.userRepository = userRepository;
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 불필요 (공개)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/signup").permitAll()
                        .requestMatchers("/api/products/**").permitAll()
                        .requestMatchers("/api/tags/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()

                        // 토스페이먼츠 관련 (인증 불필요)
                        .requestMatchers("/api/payments/toss/**").permitAll()

                        // 테스트용 주문 생성 (인증 불필요)
                        .requestMatchers("/api/orders/test/**").permitAll()

                        // 정적 리소스 (HTML 테스트 페이지)
                        .requestMatchers("/*.html", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()

                        // Swagger 관련
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()

                        // 인증 필요
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/payments/**").authenticated()
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/user-profile/**").authenticated()
                        .requestMatchers("/api/user-address/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin.disable())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );
        http
                .httpBasic((auth) -> auth.disable());

        http
                .addFilterBefore(new JwtFilter(jwtUtil, userRepository), LoginFilter.class)
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshTokenService), UsernamePasswordAuthenticationFilter.class);

        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 환경변수에서 CORS 허용 오리진 목록 가져오기
        String[] origins = allowedOrigins.split(",");
        configuration.setAllowedOrigins(Arrays.asList(origins));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
