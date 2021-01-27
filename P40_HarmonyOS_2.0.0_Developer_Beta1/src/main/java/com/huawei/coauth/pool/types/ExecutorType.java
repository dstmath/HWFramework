package com.huawei.coauth.pool.types;

public enum ExecutorType {
    TYPE_COLLECTOR(1),
    TYPE_VERIFIER(2),
    TYPE_ALL_IN_ONE(3);
    
    private final int value;

    private ExecutorType(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
