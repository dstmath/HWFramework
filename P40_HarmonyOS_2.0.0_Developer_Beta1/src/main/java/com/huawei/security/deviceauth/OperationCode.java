package com.huawei.security.deviceauth;

public enum OperationCode {
    CODE_NULL(0),
    BIND(1),
    AUTHENTICATE(2),
    ADD_AUTH_INFO(3),
    REMOVE_AUTH_INFO(4),
    UNBIND(5),
    AUTH_KEY_AGREEMENT(6),
    REGISTER(7),
    SECURE_CLONE(8);
    
    private static final int ADD_AUTH_INFO_VALUE = 3;
    private static final int AUTHENTICATE_VALUE = 2;
    private static final int AUTH_KEY_AGREEMENT_VALUE = 6;
    private static final int BIND_VALUE = 1;
    private static final int REGISTER_VALUE = 7;
    private static final int REMOVE_AUTH_INFO_VALUE = 4;
    private static final int SECURE_CLONE_VALUE = 8;
    private static final int UNBIND_VALUE = 5;
    private int mOperation;

    private OperationCode(int operation) {
        this.mOperation = operation;
    }

    public static OperationCode valueOf(int operation) {
        switch (operation) {
            case 1:
                return BIND;
            case 2:
                return AUTHENTICATE;
            case 3:
                return ADD_AUTH_INFO;
            case 4:
                return REMOVE_AUTH_INFO;
            case 5:
                return UNBIND;
            case 6:
                return AUTH_KEY_AGREEMENT;
            case 7:
                return REGISTER;
            case 8:
                return SECURE_CLONE;
            default:
                return CODE_NULL;
        }
    }

    public int toInt() {
        return this.mOperation;
    }
}
