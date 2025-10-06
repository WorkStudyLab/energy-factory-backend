package com.energyfactory.energy_factory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {

    private Long id;

    private Long userId;

    private String userEmail;

    private String userName;

    private String profileImageUrl;

    private String bio;

    private String interests;

    private String preferences;

    private Boolean isPublic;

    private String websiteUrl;

    private String location;

    private LocalDate birthDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}