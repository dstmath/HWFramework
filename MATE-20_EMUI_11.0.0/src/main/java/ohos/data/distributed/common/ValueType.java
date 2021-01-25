package ohos.data.distributed.common;

public enum ValueType {
    STRING((byte) 0),
    INT((byte) 1),
    FLOAT((byte) 2),
    BYTE_ARRAY((byte) 3),
    BOOLEAN((byte) 4),
    DOUBLE((byte) 5);
    
    private byte type;

    private ValueType(byte b) {
        this.type = b;
    }

    public static ValueType get(byte b) {
        if (b == 0) {
            return STRING;
        }
        if (b == 1) {
            return INT;
        }
        if (b == 2) {
            return FLOAT;
        }
        if (b == 3) {
            return BYTE_ARRAY;
        }
        if (b == 4) {
            return BOOLEAN;
        }
        if (b == 5) {
            return DOUBLE;
        }
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT);
    }

    public byte getType() {
        return this.type;
    }
}
