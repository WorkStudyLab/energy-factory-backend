package com.energyfactory.energy_factory.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDto {
    private Long id;
    private String email;
    private String name;
}
