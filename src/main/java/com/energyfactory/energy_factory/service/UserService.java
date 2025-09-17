package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.SignupRequestDto;
import com.energyfactory.energy_factory.dto.SignupResponseDto;
import com.energyfactory.energy_factory.entity.User;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.repository.UserRepository;
import com.energyfactory.energy_factory.utils.enums.Provider;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import com.energyfactory.energy_factory.utils.enums.Role;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        if(userRepository.existsByEmail(signupRequestDto.getEmail())){
            throw new BusinessException(ResultCode.DUPLICATE_EMAIL);
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
}
