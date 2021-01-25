package com.huawei.security.deviceauth;

public enum GroupOperation {
    CODE_NULL(-1),
    CREATE(0),
    DISBAND(1),
    INVITE(2),
    JOIN(3),
    DELETE(4);
    
    private static final int CREATE_VALUE = 0;
    private static final int DELETE_VALUE = 4;
    private static final int DISBAND_VALUE = 1;
    private static final int INVITE_VALUE = 2;
    private static final int JOIN_VALUE = 3;
    private int mOperation;

    private GroupOperation(int operation) {
        this.mOperation = operation;
    }

    public static GroupOperation valueOf(int operation) {
        if (operation == 0) {
            return CREATE;
        }
        if (operation == 1) {
            return DISBAND;
        }
        if (operation == 2) {
            return INVITE;
        }
        if (operation == 3) {
            return JOIN;
        }
        if (operation == 4) {
            return DELETE;
        }
        LogUtils.e("GroupOperation", "invalid operation value");
        return CODE_NULL;
    }

    public int toInt() {
        return this.mOperation;
    }
}
