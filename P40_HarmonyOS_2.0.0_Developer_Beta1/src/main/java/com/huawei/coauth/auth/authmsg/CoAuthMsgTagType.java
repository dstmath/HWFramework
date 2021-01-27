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
    MSG_LEN(14),
    EXEC_TYPE(27),
    EXEC_ABILITY(28),
    CO_AUTH_CONTEXT(31),
    PROPERTY_KEY(32),
    PROPERTY_VALUE(33),
    AUTH_PARA(34),
    AUTH_ADDITION(35),
    TEMPLATE_ID(36),
    TITLE_STRING(37),
    SUB_TITLE_STRING(38),
    RETRY_COUNT(39),
    LOCK_GUIDE(52),
    ENABLE_UI(54),
    IDM_GROUP_GID(64),
    IDM_DEVICE_INFO(65),
    IDM_DEVICE_UDID(66),
    IDM_DEVICE_IP(67),
    IDM_DEVICE_LINK_TYPE(68),
    IDM_DEVICE_LINK_MODE(69),
    IDM_DELEGATED_PKG_NAME(70),
    AUTH_TOKEN_SIGN(78),
    AUTH_TOKEN(79),
    AUTH_SIGN(80),
    AUTH_VERSION(81),
    AUTH_METHODS(82),
    AUTH_METHOD(83),
    AUTH_TIMESTAMP(84);
    
    private final int value;

    private CoAuthMsgTagType(int value2) {
        this.value = value2;
    }

    public int getValue() {
        return this.value;
    }
}
