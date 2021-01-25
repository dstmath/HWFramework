package com.huawei.coauth.pool.types;

import android.util.SparseArray;
import com.huawei.security.HwKeystoreManager;

public enum AuthAttributeType {
    AUTH_RESULT_CODE(HwKeystoreManager.AUTH_TEMPLATE_ID_BIND_REPEAT),
    AUTH_RESULT_INFO(HwKeystoreManager.AUTH_TEMPLATE_ID_BIND_OVERFLOW),
    AUTH_CHALLENGE_NUM(100003),
    AUTH_SIGNATURE(100004),
    AUTH_IDENTIFY_MODE(100005),
    AUTH_TEMPLATE_ID(100006),
    AUTH_TEMPLATE_ID_LIST(100007),
    AUTH_ERROR_COUNT(100008),
    AUTH_REMAIN_COUNT(100009),
    AUTH_REMAIN_TIME(100010),
    AUTH_TIMEOUT_TIME(100011),
    AUTH_ROOT_STATUS(100012),
    AUTH_RESULT_STATUS(100013),
    AUTH_SESSION_ID(100014),
    AUTH_PACKAGE_NAME(100015),
    PIN_PASSWORD_TYPE(100101),
    PIN_PAKE_SESSION_KEY(100102),
    FACE_AUTH_SCORE(100201),
    FACE_MASK_WEARING_STATUS(100202),
    FACE_EYE_CLOSING_STATUS(100203),
    FACE_VOICE_GENDER(100301),
    WEARABLE_WEAR_STATUS(100401),
    WEARABLE_WEAR_DISTANCE(100402),
    AUTH_EXPECT_ATTRS(200001),
    AUTH_CMD_ATTRS(200002),
    AUTH_RESULT_ATTRS(200003),
    ATTRIBUTE_INVALID_VALUE(Integer.MAX_VALUE);
    
    private static final SparseArray<AuthAttributeType> ATTR_TYPE_MAP = new SparseArray<>();
    private final int value;

    static {
        AuthAttributeType[] values = values();
        for (AuthAttributeType attrType : values) {
            ATTR_TYPE_MAP.put(attrType.value, attrType);
        }
    }

    private AuthAttributeType(int value2) {
        this.value = value2;
    }

    public final int getValue() {
        return this.value;
    }

    public static AuthAttributeType of(int type) {
        AuthAttributeType attrType = ATTR_TYPE_MAP.get(type);
        if (attrType == null) {
            return ATTRIBUTE_INVALID_VALUE;
        }
        return attrType;
    }
}
