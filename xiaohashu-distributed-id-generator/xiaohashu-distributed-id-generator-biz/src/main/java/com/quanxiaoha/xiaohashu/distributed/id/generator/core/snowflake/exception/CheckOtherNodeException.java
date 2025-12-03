package com.quanxiaoha.xiaohashu.distributed.id.generator.core.snowflake.exception;

public class CheckOtherNodeException extends RuntimeException {
    public CheckOtherNodeException(String message) {
        super(message);
    }
}
