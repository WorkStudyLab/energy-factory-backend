package com.energyfactory.energy_factory.service;

import com.energyfactory.energy_factory.dto.UserProfileCreateRequestDto;
import com.energyfactory.energy_factory.dto.UserProfileResponseDto;
import com.energyfactory.energy_factory.dto.UserProfileUpdateRequestDto;
import com.energyfactory.energy_factory.entity.User;
import com.energyfactory.energy_factory.entity.UserProfile;
import com.energyfactory.energy_factory.exception.BusinessException;
import com.energyfactory.energy_factory.repository.UserProfileRepository;
import com.energyfactory.energy_factory.repository.UserRepository;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserProfileResponseDto createProfile(Long userId, UserProfileCreateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        if (userProfileRepository.existsByUserId(userId)) {
            throw new BusinessException(ResultCode.DUPLICATE_REQUEST);
        }

        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .profileImageUrl(requestDto.getProfileImageUrl())
                .bio(requestDto.getBio())
                .interests(requestDto.getInterests())
                .preferences(requestDto.getPreferences())
                .isPublic(requestDto.getIsPublic() != null ? requestDto.getIsPublic() : true)
                .websiteUrl(requestDto.getWebsiteUrl())
                .location(requestDto.getLocation())
                .birthDate(requestDto.getBirthDate())
                .build();

        userProfileRepository.save(userProfile);
        return convertToResponseDto(userProfile);
    }

    public UserProfileResponseDto getProfileByUserId(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.RESOURCE_NOT_FOUND));

        return convertToResponseDto(userProfile);
    }

    public UserProfileResponseDto getProfileById(Long profileId) {
        UserProfile userProfile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException(ResultCode.RESOURCE_NOT_FOUND));

        return convertToResponseDto(userProfile);
    }

    @Transactional
    public UserProfileResponseDto updateProfile(Long userId, UserProfileUpdateRequestDto requestDto) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.RESOURCE_NOT_FOUND));

        userProfile.updateProfile(
                requestDto.getProfileImageUrl(),
                requestDto.getBio(),
                requestDto.getInterests(),
                requestDto.getPreferences(),
                requestDto.getIsPublic(),
                requestDto.getWebsiteUrl(),
                requestDto.getLocation(),
                requestDto.getBirthDate()
        );

        userProfileRepository.save(userProfile);
        return convertToResponseDto(userProfile);
    }

    @Transactional
    public void deleteProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.RESOURCE_NOT_FOUND));

        userProfileRepository.delete(userProfile);
    }

    private UserProfileResponseDto convertToResponseDto(UserProfile userProfile) {
        return UserProfileResponseDto.builder()
                .id(userProfile.getId())
                .userId(userProfile.getUser().getId())
                .userEmail(userProfile.getUser().getEmail())
                .userName(userProfile.getUser().getName())
                .profileImageUrl(userProfile.getProfileImageUrl())
                .bio(userProfile.getBio())
                .interests(userProfile.getInterests())
                .preferences(userProfile.getPreferences())
                .isPublic(userProfile.getIsPublic())
                .websiteUrl(userProfile.getWebsiteUrl())
                .location(userProfile.getLocation())
                .birthDate(userProfile.getBirthDate())
                .createdAt(userProfile.getCreatedAt())
                .updatedAt(userProfile.getUpdatedAt())
                .build();
    }
}