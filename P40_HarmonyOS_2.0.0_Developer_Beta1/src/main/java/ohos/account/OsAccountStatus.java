package ohos.account;

import ohos.rpc.MessageParcel;

public enum OsAccountStatus {
    INVALID(-1),
    BOOTING(0),
    RUNNING_LOCKED(1),
    RUNNING_UNLOCKING(2),
    RUNNING_UNLOCKED(3),
    STOPPING(4),
    STOPPED(5);
    
    private final int statusValue;

    private OsAccountStatus(int i) {
        this.statusValue = i;
    }

    public int getValue() {
        return this.statusValue;
    }

    public static boolean marshallingEnum(MessageParcel messageParcel, OsAccountStatus osAccountStatus) {
        return messageParcel != null && messageParcel.writeInt(osAccountStatus.getValue());
    }

    public static OsAccountStatus unmarshallingEnum(MessageParcel messageParcel) {
        if (messageParcel == null) {
            return INVALID;
        }
        return getStatus(messageParcel.readInt());
    }

    public static OsAccountStatus getStatus(int i) {
        OsAccountStatus[] values = values();
        for (OsAccountStatus osAccountStatus : values) {
            if (osAccountStatus.getValue() == i) {
                return osAccountStatus;
            }
        }
        return INVALID;
    }
}
