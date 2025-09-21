package com.energyfactory.energy_factory.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class JwtToken {
    private String grantType; // Bearer
    private String accessToken;
    private String refreshToken;
}
