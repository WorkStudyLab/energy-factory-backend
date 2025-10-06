package com.energyfactory.energy_factory.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileCreateRequestDto {

    @Size(max = 500, message = "프로필 이미지 URL은 500자를 초과할 수 없습니다")
    private String profileImageUrl;

    @Size(max = 1000, message = "자기소개는 1000자를 초과할 수 없습니다")
    private String bio;

    private String interests;

    private String preferences;

    private Boolean isPublic;

    @Size(max = 500, message = "웹사이트 URL은 500자를 초과할 수 없습니다")
    private String websiteUrl;

    @Size(max = 100, message = "거주지는 100자를 초과할 수 없습니다")
    private String location;

    private LocalDate birthDate;
}