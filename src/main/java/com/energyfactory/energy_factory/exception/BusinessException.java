package com.energyfactory.energy_factory.exception;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import lombok.Getter;


@Getter
public class BusinessException  extends RuntimeException {
    protected ResultCode resultCode;

    public BusinessException (ResultCode resultCode){
        super(resultCode.getDesc());
        this.resultCode = resultCode;
    }
}



