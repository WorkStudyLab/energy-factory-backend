package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.CustomUserDetails;
import com.energyfactory.energy_factory.dto.NaverUserInfo;
import com.energyfactory.energy_factory.dto.OAuth2TempInfo;
import com.energyfactory.energy_factory.dto.OAuth2UserInfo;
import com.energyfactory.energy_factory.entity.User;
import com.energyfactory.energy_factory.repository.UserRepository;
import com.energyfactory.energy_factory.utils.enums.Provider;
import com.energyfactory.energy_factory.utils.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = null;

        if (registrationId.equals("naver")) {
            Map<String, Object> response = oAuth2User.getAttribute("response");
            oAuth2UserInfo = new NaverUserInfo(response);
        }

        if (oAuth2UserInfo == null) {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다.");
        }

        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();
        String provider = oAuth2UserInfo.getProvider();
        Provider providerEnum = Provider.valueOf(provider.toUpperCase());

        // 네이버에서 휴대폰 번호 가져오기
        String phoneNumber = null;
        if (oAuth2UserInfo instanceof NaverUserInfo) {
            NaverUserInfo naverUserInfo = (NaverUserInfo) oAuth2UserInfo;
            phoneNumber = naverUserInfo.getMobile();
        }

        // 1. ProviderId로 기존 사용자 찾기 (Provider 무관)
        // LOCAL 사용자가 네이버 연동한 경우도 찾을 수 있음
        Optional<User> optionalUser = userRepository.findByProviderId(providerId);

        User user;
        if (optionalUser.isPresent()) {
            // 기존 사용자 로그인 (네이버 연동된 사용자 또는 순수 네이버 사용자)
            user = optionalUser.get();

            // **기존 정보 전부 유지** - 네이버 정보로 덮어쓰지 않음
            // DB 저장도 하지 않음 (변경 사항 없음)
        } else {
            // 2. 신규 사용자 - DB에 저장하지 않고 임시 User 객체 생성
            // OAuth2SuccessHandler에서 세션에 네이버 정보를 저장하고,
            // 사용자가 추가 정보 입력 후 회원가입 완료
            user = User.builder()
                .email(email)               // 네이버에서 가져온 이메일 (자동 완성용)
                .name(name)                 // 네이버에서 가져온 이름 (자동 완성용)
                .password(passwordEncoder.encode("OAUTH_USER_TEMP"))  // 임시 비밀번호
                .phoneNumber(phoneNumber)   // 네이버에서 가져온 전화번호 (자동 완성용)
                .provider(providerEnum)
                .providerId(providerId)
                .role(Role.USER)
                .build();

            // **DB에 저장하지 않음** - 임시 객체로만 사용
        }

        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }
}
