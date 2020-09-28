package com.huawei.coauth.auth;

public enum CoAuthType {
    REMOTEPIN(1),
    FACE(2);
    
    private final int value;

    private CoAuthType(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
