package com.huawei.coauth.auth;

public enum CoAuthRetCode {
    CO_AUTH_RET_SUCCESS(0),
    CO_AUTH_RET_BAD_PARAM(1),
    CO_AUTH_RET_COPY_ERROR(2),
    CO_AUTH_RET_NO_MEMORY(3),
    CO_AUTH_RET_NO_SPCACE(4),
    CO_AUTH_RET_BAD_ACCESS(5),
    CO_AUTH_RET_READ_ERROR(6),
    CO_AUTH_RET_NULL_PTR(7),
    CO_AUTH_RET_FAIL(8);
    
    private final int value;

    private CoAuthRetCode(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
