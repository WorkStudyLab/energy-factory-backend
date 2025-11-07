package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.OAuth2SignupRequestDto;
import com.energyfactory.energy_factory.dto.OAuth2TempInfo;
import com.energyfactory.energy_factory.dto.SignupRequestDto;
import com.energyfactory.energy_factory.dto.SignupResponseDto;
import com.energyfactory.energy_factory.dto.UserAdditionalInfoRequestDto;
import com.energyfactory.energy_factory.dto.UserResponseDto;
import com.energyfactory.energy_factory.entity.User;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.repository.UserRepository;
import com.energyfactory.energy_factory.utils.enums.Provider;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import com.energyfactory.energy_factory.utils.enums.Role;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 사용자 비즈니스 로직 서비스
 * 회원가입, 사용자 정보 조회, 비밀번호 변경, 회원 탈퇴 기능을 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new BusinessException(ResultCode.DUPLICATE_EMAIL);
        }

        // 전화번호 중복 체크
        if (userRepository.existsByPhoneNumber(signupRequestDto.getPhoneNumber())) {
            throw new BusinessException(ResultCode.DUPLICATE_PHONE_NUMBER);
        }

        User user = User.builder()
                .email(signupRequestDto.getEmail())
                .name(signupRequestDto.getName())
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .role(Role.USER)
                .phoneNumber(signupRequestDto.getPhoneNumber())
                .birthDate(signupRequestDto.getBirthDate())
                .address(signupRequestDto.getAddress())
                .provider(Provider.LOCAL)
                .build();

        userRepository.save(user);
        return SignupResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .id(user.getId())
                .build();
    }
    
    /**
     * 사용자 정보 조회
     */
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        return convertToUserResponseDto(user);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.INVALID_PASSWORD);
        }

        // 기존 User 객체의 password만 변경
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }
    
    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }

    /**
     * 이메일로 비밀번호 재설정 (비밀번호 찾기)
     */
    @Transactional
    public void resetPasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 기존 User 객체의 password만 변경
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    /**
     * OAuth2 소셜 로그인 후 회원가입 완료
     * 세션에서 가져온 OAuth2 정보(providerId) + 사용자가 입력/수정한 모든 정보를 DB에 저장
     */
    @Transactional
    public SignupResponseDto signupWithOAuth2(OAuth2TempInfo tempInfo, OAuth2SignupRequestDto requestDto) {
        // 이메일 중복 체크 (사용자가 네이버 정보를 수정했을 수 있음)
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new BusinessException(ResultCode.DUPLICATE_EMAIL);
        }

        // 전화번호 중복 체크
        if (userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new BusinessException(ResultCode.DUPLICATE_PHONE_NUMBER);
        }

        // Provider Enum 변환
        Provider provider = Provider.valueOf(tempInfo.getProvider().toUpperCase());

        // User 생성 및 저장
        // requestDto의 모든 정보 사용 (사용자가 네이버 정보를 수정했을 수 있음)
        User user = User.builder()
                .email(requestDto.getEmail())           // 사용자가 입력/수정한 이메일
                .name(requestDto.getName())             // 사용자가 입력/수정한 이름
                .password(passwordEncoder.encode("OAUTH_USER"))  // OAuth2 사용자는 비밀번호 불필요
                .phoneNumber(requestDto.getPhoneNumber())  // 사용자가 입력/수정한 전화번호
                .birthDate(requestDto.getBirthDate())   // 사용자가 입력한 생년월일
                .address(requestDto.getAddress())       // 사용자가 입력한 주소
                .provider(provider)                     // NAVER
                .providerId(tempInfo.getProviderId())   // 네이버 고유 ID
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return SignupResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    /**
     * 소셜 로그인 후 추가 정보 업데이트
     * 생년월일과 배송지를 업데이트합니다. (전화번호는 네이버에서 필수로 받음)
     */
    @Transactional
    public UserResponseDto updateAdditionalInfo(String email, UserAdditionalInfoRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 생년월일 업데이트
        if (requestDto.getBirthDate() != null) {
            user.setBirthDate(requestDto.getBirthDate());
        }

        // 배송지 주소 업데이트
        if (requestDto.getAddress() != null && !requestDto.getAddress().isEmpty()) {
            user.setAddress(requestDto.getAddress());
        }

        userRepository.save(user);
        return convertToUserResponseDto(user);
    }

    /**
     * 이메일로 사용자 정보 조회 (추가 정보 필요 여부 확인용)
     */
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        return convertToUserResponseDto(user);
    }

    /**
     * 네이버 계정 연동
     * 로그인된 LOCAL 사용자가 네이버 계정을 연동할 때 사용
     */
    @Transactional
    public void linkNaverAccount(Long userId, String providerId, Provider provider) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 2. 이미 소셜 계정이 연동되어 있는지 확인
        if (user.getProviderId() != null) {
            throw new BusinessException(ResultCode.PROVIDER_ALREADY_LINKED);
        }

        // 3. 해당 providerId가 이미 다른 사용자에게 사용 중인지 확인
        if (userRepository.findByProviderId(providerId).isPresent()) {
            throw new BusinessException(ResultCode.PROVIDER_ALREADY_IN_USE);
        }

        // 4. Provider와 ProviderId 업데이트
        user.setProvider(provider);
        user.setProviderId(providerId);

        userRepository.save(user);
    }

    /**
     * User 엔티티를 UserResponseDto로 변환 (마이페이지용)
     */
    private UserResponseDto convertToUserResponseDto(User user) {
        return UserResponseDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .birthDate(user.getBirthDate())
                .authProvider(user.getProvider().name().toLowerCase())
                .memberSince(user.getCreatedAt().toLocalDate())
                .address(user.getAddress() != null ? user.getAddress() : "")
                .role(user.getRole().name())
                .build();
    }
}
