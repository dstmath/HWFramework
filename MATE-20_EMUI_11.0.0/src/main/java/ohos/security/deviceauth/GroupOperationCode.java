package ohos.security.deviceauth;

public enum GroupOperationCode {
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

    private GroupOperationCode(int i) {
        this.mOperation = i;
    }

    public static GroupOperationCode valueOf(int i) {
        if (i == 0) {
            return CREATE;
        }
        if (i == 1) {
            return DISBAND;
        }
        if (i == 2) {
            return INVITE;
        }
        if (i == 3) {
            return JOIN;
        }
        if (i != 4) {
            return CODE_NULL;
        }
        return DELETE;
    }

    public int toInt() {
        return this.mOperation;
    }
}
