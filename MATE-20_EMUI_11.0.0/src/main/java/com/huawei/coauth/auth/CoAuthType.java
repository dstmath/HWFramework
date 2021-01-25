package com.huawei.coauth.auth;

import android.util.SparseArray;

public enum CoAuthType {
    TYPE_IGNORE(0),
    REMOTEPIN(1),
    FACE(2);
    
    private static final SparseArray<CoAuthType> AUTH_TYPE_MAP = new SparseArray<>();
    private final int value;

    static {
        CoAuthType[] values = values();
        for (CoAuthType coAuthType : values) {
            AUTH_TYPE_MAP.put(coAuthType.value, coAuthType);
        }
    }

    public static CoAuthType valueOf(int type) {
        CoAuthType coAuthType = AUTH_TYPE_MAP.get(type);
        if (coAuthType == null) {
            return TYPE_IGNORE;
        }
        return coAuthType;
    }

    private CoAuthType(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
