package com.huawei.coauth.auth.authmsg;

public enum CoAuthMsgTagType {
    SESSION_ID(0),
    PACKAGE_NAME(1),
    MODULE_NAME(2),
    SELF_DID(3),
    PEER_DID(4),
    GROUP_ID(5),
    RESULT_CODE(6),
    CHALLENGE(7),
    AUTH_TYPE(8),
    SENSOR_DID(9),
    VERIFIER_DID(10),
    MODULE(11),
    PEER_DEVICE_IP(12),
    PEER_DEVICE_PORT(13),
    MSG_LEN(14);
    
    private final int value;

    private CoAuthMsgTagType(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
