package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.CustomUserDetails;
import com.energyfactory.energy_factory.dto.NaverUserInfo;
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
            // 휴대폰 번호: "010-1234-5678" 그대로 저장 (대쉬 포함)
            phoneNumber = naverUserInfo.getMobile();
        }
        
        // 1. Provider + ProviderId로 기존 사용자 확인
        Optional<User> optionalUser = userRepository.findByProviderAndProviderId(providerEnum, providerId);
        
        User user;
        if (optionalUser.isPresent()) {
            // 기존 OAuth2 사용자 - 정보 업데이트
            user = optionalUser.get();
            user.setName(name);
            user.setEmail(email);

            // 휴대폰 번호가 있으면 업데이트
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                user.setPhoneNumber(phoneNumber);
            }

            userRepository.save(user);
        } else {
            // 2. 같은 이메일로 가입된 사용자가 있는지 확인
            Optional<User> existingUserByEmail = userRepository.findByEmail(email);
            
            if (existingUserByEmail.isPresent()) {
                // 같은 이메일이 이미 존재 (다른 Provider 또는 LOCAL)
                User existingUser = existingUserByEmail.get();
                
                // 기존 계정이 일반 로그인(LOCAL)인 경우
                if (existingUser.getProvider() == Provider.LOCAL) {
                    // OAuth2 정보를 추가하여 계정 연동
                    existingUser.setProvider(providerEnum);
                    existingUser.setProviderId(providerId);
                    userRepository.save(existingUser);
                    user = existingUser;
                } else {
                    // 이미 다른 소셜 로그인으로 가입됨
                    throw new OAuth2AuthenticationException(
                        "이미 " + existingUser.getProvider().getDescription() + "로 가입된 이메일입니다."
                    );
                }
            } else {
                // 3. 완전히 새로운 사용자 - 신규 등록
                user = User.builder()
                    .email(email)
                    .name(name)
                    .password(passwordEncoder.encode("OAUTH_USER"))
                    .phoneNumber(phoneNumber)
                    .provider(providerEnum)
                    .providerId(providerId)
                    .role(Role.USER)
                    .build();

                userRepository.save(user);
            }
        }
        
        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }
}
