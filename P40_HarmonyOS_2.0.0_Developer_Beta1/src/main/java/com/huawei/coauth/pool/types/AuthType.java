package com.huawei.coauth.pool.types;

public enum AuthType {
    TYPE_IGNORE(0),
    PIN(1),
    FACE(2),
    VOICE(4),
    BEHAVIOR(8),
    THINGS(16);
    
    private final int value;

    private AuthType(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
