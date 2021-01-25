package com.huawei.coauth.fusion;

import android.util.SparseArray;

public enum FusionAuthType {
    TYPE_IGNORE(-1),
    DEFAULT(0),
    PIN(1),
    FACE(2),
    FINGERPRINT(3),
    VOICE(4),
    THINGS(5),
    BEHAVIOR(6);
    
    private static final SparseArray<FusionAuthType> AUTH_TYPE_MAP = new SparseArray<>();
    private final int value;

    static {
        FusionAuthType[] values = values();
        for (FusionAuthType fusionAuthType : values) {
            AUTH_TYPE_MAP.put(fusionAuthType.value, fusionAuthType);
        }
    }

    static FusionAuthType valueOf(int type) {
        FusionAuthType coAuthType = AUTH_TYPE_MAP.get(type);
        if (coAuthType == null) {
            return TYPE_IGNORE;
        }
        return coAuthType;
    }

    private FusionAuthType(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
