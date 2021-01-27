package com.huawei.coauth.pool.types;

public enum ExecutorSecureLevel {
    EXECUTOR_SECURE_LEVEL_0(0),
    EXECUTOR_SECURE_LEVEL_1(1),
    EXECUTOR_SECURE_LEVEL_2(2),
    EXECUTOR_SECURE_LEVEL_3(3);
    
    private final int value;

    private ExecutorSecureLevel(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
