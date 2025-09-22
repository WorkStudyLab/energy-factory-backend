package com.energyfactory.energy_factory.exception;

import com.energyfactory.energy_factory.utils.enums.ResultCode;

public class AuthException extends BusinessException {
    public AuthException(ResultCode resultCode) {
        super(resultCode);
    }
}
