package ohos.com.sun.org.apache.bcel.internal.generic;

import ohos.com.sun.org.apache.bcel.internal.Constants;

public final class BasicType extends Type {
    BasicType(byte b) {
        super(b, Constants.SHORT_TYPE_NAMES[b]);
        if (b < 4 || b > 12) {
            throw new ClassGenException("Invalid type: " + ((int) b));
        }
    }

    public static final BasicType getType(byte b) {
        switch (b) {
            case 4:
                return BOOLEAN;
            case 5:
                return CHAR;
            case 6:
                return FLOAT;
            case 7:
                return DOUBLE;
            case 8:
                return BYTE;
            case 9:
                return SHORT;
            case 10:
                return INT;
            case 11:
                return LONG;
            case 12:
                return VOID;
            default:
                throw new ClassGenException("Invalid type: " + ((int) b));
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof BasicType) || ((BasicType) obj).type != this.type) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.type;
    }
}
