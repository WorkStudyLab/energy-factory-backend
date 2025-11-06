package com.energyfactory.energy_factory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * OAuth2 인증 후 임시로 세션에 저장되는 정보
 * 사용자가 추가 정보를 입력하기 전까지 DB에 저장되지 않음
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2TempInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * OAuth2 제공자 (예: NAVER)
     */
    private String provider;

    /**
     * OAuth2 제공자의 고유 사용자 ID
     */
    private String providerId;

    /**
     * 이메일 주소
     */
    private String email;

    /**
     * 사용자 이름
     */
    private String name;

    /**
     * 전화번호 (네이버에서 제공)
     */
    private String phoneNumber;
}
