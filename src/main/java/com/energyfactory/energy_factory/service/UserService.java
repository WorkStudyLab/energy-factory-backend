package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.SignupRequestDto;
import com.energyfactory.energy_factory.dto.SignupResponseDto;
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
        
        User updatedUser = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .password(passwordEncoder.encode(newPassword))
                .phoneNumber(user.getPhoneNumber())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
                
        userRepository.save(updatedUser);
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
     * User 엔티티를 UserResponseDto로 변환
     */
    private UserResponseDto convertToUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .provider(user.getProvider().name())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
