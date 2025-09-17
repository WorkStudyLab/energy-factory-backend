package com.energyfactory.energy_factory.dto;

import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private final int status;
    private final String code;
    private final String desc;
    private final T data;

    public ApiResponse(ResultCode resultCode, T data) {
        this.status = resultCode.getStatus().value();
        this.code = resultCode.getCode();
        this.desc = resultCode.getDesc();
        this.data = data;
    }

    public static<T> ApiResponse<T> of(ResultCode resultCode, T data) {
        return new ApiResponse<T>(resultCode, data);
    }
}
