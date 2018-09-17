package java.lang;

import java.io.Serializable;

public abstract class Number implements Serializable {
    private static final long serialVersionUID = -8742448824652078965L;

    public abstract double doubleValue();

    public abstract float floatValue();

    public abstract int intValue();

    public abstract long longValue();

    public byte byteValue() {
        return (byte) intValue();
    }

    public short shortValue() {
        return (short) intValue();
    }
}
