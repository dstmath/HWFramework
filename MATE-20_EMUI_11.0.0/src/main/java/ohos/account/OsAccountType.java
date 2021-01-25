package ohos.account;

import ohos.rpc.MessageParcel;

public enum OsAccountType {
    INVALID(-1),
    ADMIN(0),
    NORMAL(1),
    HIDDEN_SPACE(2),
    REPAIR_MODE(3),
    GUEST(4);
    
    private final int typeValue;

    private OsAccountType(int i) {
        this.typeValue = i;
    }

    public int getValue() {
        return this.typeValue;
    }

    public static boolean marshallingEnum(MessageParcel messageParcel, OsAccountType osAccountType) {
        return messageParcel != null && messageParcel.writeInt(osAccountType.getValue());
    }

    public static OsAccountType unmarshallingEnum(MessageParcel messageParcel) {
        if (messageParcel == null) {
            return INVALID;
        }
        return getType(messageParcel.readInt());
    }

    public static OsAccountType getType(int i) {
        OsAccountType[] values = values();
        for (OsAccountType osAccountType : values) {
            if (osAccountType.getValue() == i) {
                return osAccountType;
            }
        }
        return INVALID;
    }
}
