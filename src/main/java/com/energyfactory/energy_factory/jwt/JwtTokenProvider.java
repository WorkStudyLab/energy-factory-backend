package com.energyfactory.energy_factory.jwt;

import org.springframework.beans.factory.annotation.Value;

import java.security.Key;

public class JwtTokenProvider {

    @Value("${JWT_SECRET_KEY}")
    private final Key key;

    public JwtTokenProvider(Key key) {
        this.key = key;
    }
}
