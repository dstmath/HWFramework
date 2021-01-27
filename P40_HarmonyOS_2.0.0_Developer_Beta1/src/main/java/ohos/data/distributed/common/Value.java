package ohos.data.distributed.common;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class Value {
    private static final int LENGTH_EIGHT = 8;
    private static final int LENGTH_FOUR = 4;
    private final byte type;
    private final byte[] value;

    private Value(byte b, byte[] bArr) {
        this.type = b;
        this.value = bArr;
    }

    public static Value get(String str) {
        if (str != null) {
            try {
                return new Value(ValueType.STRING.getType(), str.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new KvStoreException(KvStoreErrorCode.UTF_8_NOT_SUPPORT, e.getMessage());
            }
        } else {
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "value is null");
        }
    }

    public static Value get(int i) {
        ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.putInt(i);
        return new Value(ValueType.INT.getType(), allocate.array());
    }

    public static Value get(float f) {
        ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.putFloat(f);
        return new Value(ValueType.FLOAT.getType(), allocate.array());
    }

    public static Value get(double d) {
        ByteBuffer allocate = ByteBuffer.allocate(8);
        allocate.putDouble(d);
        return new Value(ValueType.DOUBLE.getType(), allocate.array());
    }

    public static Value get(byte[] bArr) {
        return new Value(ValueType.BYTE_ARRAY.getType(), bArr);
    }

    public double getDouble() {
        assertValueType(ValueType.DOUBLE);
        assertValueLength(8);
        return ByteBuffer.wrap(this.value).getDouble();
    }

    public String getString() {
        assertValueType(ValueType.STRING);
        try {
            return new String(this.value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new KvStoreException(KvStoreErrorCode.UTF_8_NOT_SUPPORT, e.getMessage());
        }
    }

    private void assertValueType(ValueType valueType) {
        if (ValueType.get(this.type) != valueType) {
            throw new KvStoreException(KvStoreErrorCode.INVALID_VALUE_TYPE, "invalid value type.");
        }
    }

    private void assertValueLength(int i) {
        byte[] bArr = this.value;
        if (bArr == null || i != bArr.length) {
            StringBuilder sb = new StringBuilder();
            sb.append("value size expect ");
            sb.append(i);
            sb.append(", real ");
            byte[] bArr2 = this.value;
            sb.append(bArr2 == null ? 0 : bArr2.length);
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, sb.toString());
        }
    }

    public int getInt() {
        assertValueType(ValueType.INT);
        assertValueLength(4);
        return ByteBuffer.wrap(this.value).getInt();
    }

    public float getFloat() {
        assertValueType(ValueType.FLOAT);
        assertValueLength(4);
        return ByteBuffer.wrap(this.value).getFloat();
    }

    public byte[] getByteArray() {
        assertValueType(ValueType.BYTE_ARRAY);
        return this.value;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof Value)) {
            return false;
        }
        Value value2 = (Value) obj;
        return this.type == value2.type && Arrays.equals(this.value, value2.value);
    }

    public int hashCode() {
        return (Objects.hash(Byte.valueOf(this.type)) * 31) + Arrays.hashCode(this.value);
    }
}
