package com.energyfactory.energy_factory.utils.enums;

public enum Provider {
    LOCAL("일반 로그인"),
    GOOGLE("구글"),
    KAKAO("카카오"),
    NAVER("네이버");

    private final String description;

    Provider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}