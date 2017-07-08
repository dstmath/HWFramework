package tmsdkobf;

import java.io.Serializable;

public abstract class fs implements Serializable {
    public static final byte BYTE = (byte) 0;
    public static final byte DOUBLE = (byte) 5;
    public static final byte FLOAT = (byte) 4;
    public static final byte INT = (byte) 2;
    public static final int JCE_MAX_STRING_LENGTH = 104857600;
    public static final byte LIST = (byte) 9;
    public static final byte LONG = (byte) 3;
    public static final byte MAP = (byte) 8;
    public static final byte SHORT = (byte) 1;
    public static final byte SIMPLE_LIST = (byte) 13;
    public static final byte STRING1 = (byte) 6;
    public static final byte STRING4 = (byte) 7;
    public static final byte STRUCT_BEGIN = (byte) 10;
    public static final byte STRUCT_END = (byte) 11;
    public static final byte ZERO_TAG = (byte) 12;

    public abstract void readFrom(fq fqVar);

    public abstract void writeTo(fr frVar);

    public void display(StringBuilder stringBuilder, int i) {
    }

    public void displaySimple(StringBuilder stringBuilder, int i) {
    }

    public fs newInit() {
        return null;
    }

    public void recyle() {
    }

    public boolean containField(String str) {
        return false;
    }

    public Object getFieldByName(String str) {
        return null;
    }

    public void setFieldByName(String str, Object obj) {
    }

    public byte[] toByteArray() {
        fr frVar = new fr();
        writeTo(frVar);
        return frVar.toByteArray();
    }

    public byte[] toByteArray(String str) {
        fr frVar = new fr();
        frVar.ae(str);
        writeTo(frVar);
        return frVar.toByteArray();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        display(stringBuilder, 0);
        return stringBuilder.toString();
    }
}
