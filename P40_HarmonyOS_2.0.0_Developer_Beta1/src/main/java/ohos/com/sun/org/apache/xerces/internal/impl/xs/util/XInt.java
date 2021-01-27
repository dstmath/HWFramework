package ohos.com.sun.org.apache.xerces.internal.impl.xs.util;

public final class XInt {
    private int fValue;

    XInt(int i) {
        this.fValue = i;
    }

    public final int intValue() {
        return this.fValue;
    }

    public final short shortValue() {
        return (short) this.fValue;
    }

    public final boolean equals(XInt xInt) {
        return this.fValue == xInt.fValue;
    }

    public String toString() {
        return Integer.toString(this.fValue);
    }
}
