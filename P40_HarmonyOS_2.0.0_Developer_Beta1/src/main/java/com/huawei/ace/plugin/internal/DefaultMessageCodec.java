package com.huawei.ace.plugin.internal;

import com.huawei.ace.runtime.ALog;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultMessageCodec {
    private static final byte BYTE_ARRAY = 25;
    private static final byte DOUBLE = 21;
    private static final byte FALSE = 18;
    public static final DefaultMessageCodec INSTANCE = new DefaultMessageCodec();
    private static final byte INT = 19;
    private static final byte INT_ARRAY = 27;
    private static final boolean LITTLE_ENDIAN = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN);
    private static final String LOG_TAG = "DefaultMessageCodec";
    private static final byte LONG = 20;
    private static final byte MAP = 23;
    private static final byte NULL = 16;
    private static final byte SET = 24;
    private static final byte SHORT_ARRAY = 26;
    private static final byte STRING = 22;
    private static final byte TRUE = 17;
    private static final Charset UTF8 = Charset.forName("UTF8");

    /* access modifiers changed from: protected */
    public void writeValue(ByteArrayOutputStream byteArrayOutputStream, Object obj) {
        if (obj == null) {
            byteArrayOutputStream.write(16);
        } else if (obj instanceof Boolean) {
            if (((Boolean) obj).booleanValue()) {
                byteArrayOutputStream.write(17);
            } else {
                byteArrayOutputStream.write(18);
            }
        } else if (obj instanceof Number) {
            if ((obj instanceof Integer) || (obj instanceof Short) || (obj instanceof Byte)) {
                byteArrayOutputStream.write(19);
                writeInt(byteArrayOutputStream, ((Number) obj).intValue());
            } else if (obj instanceof Long) {
                byteArrayOutputStream.write(20);
                writeLong(byteArrayOutputStream, ((Long) obj).longValue());
            } else if ((obj instanceof Float) || (obj instanceof Double)) {
                byteArrayOutputStream.write(21);
                writeDouble(byteArrayOutputStream, ((Number) obj).doubleValue());
            } else {
                ALog.w(LOG_TAG, "Write value failed, unsupported number type");
            }
        } else if (obj instanceof String) {
            byteArrayOutputStream.write(22);
            writeBytes(byteArrayOutputStream, ((String) obj).getBytes(UTF8));
        } else {
            writeComplexTypeValue(byteArrayOutputStream, obj);
        }
    }

    /* access modifiers changed from: protected */
    public void writeComplexTypeValue(ByteArrayOutputStream byteArrayOutputStream, Object obj) {
        if (obj instanceof Map) {
            byteArrayOutputStream.write(23);
            Map map = (Map) obj;
            writeSize(byteArrayOutputStream, map.size());
            for (Map.Entry entry : map.entrySet()) {
                writeValue(byteArrayOutputStream, entry.getKey());
                writeValue(byteArrayOutputStream, entry.getValue());
            }
        } else if (obj instanceof Set) {
            byteArrayOutputStream.write(24);
            Set<Object> set = (Set) obj;
            writeSize(byteArrayOutputStream, set.size());
            for (Object obj2 : set) {
                writeValue(byteArrayOutputStream, obj2);
            }
        } else if (obj instanceof byte[]) {
            byteArrayOutputStream.write(25);
            writeBytes(byteArrayOutputStream, (byte[]) obj);
        } else {
            int i = 0;
            if (obj instanceof short[]) {
                byteArrayOutputStream.write(26);
                short[] sArr = (short[]) obj;
                writeSize(byteArrayOutputStream, sArr.length);
                writeAlignment(byteArrayOutputStream, 2);
                int length = sArr.length;
                while (i < length) {
                    writeShort(byteArrayOutputStream, sArr[i]);
                    i++;
                }
            } else if (obj instanceof int[]) {
                byteArrayOutputStream.write(27);
                int[] iArr = (int[]) obj;
                writeSize(byteArrayOutputStream, iArr.length);
                writeAlignment(byteArrayOutputStream, 4);
                int length2 = iArr.length;
                while (i < length2) {
                    writeInt(byteArrayOutputStream, iArr[i]);
                    i++;
                }
            } else {
                ALog.w(LOG_TAG, "Write value failed, unsupported value type");
            }
        }
    }

    protected static final void writeShort(ByteArrayOutputStream byteArrayOutputStream, short s) {
        if (LITTLE_ENDIAN) {
            byteArrayOutputStream.write(s);
            byteArrayOutputStream.write(s >>> 8);
            return;
        }
        byteArrayOutputStream.write(s >>> 8);
        byteArrayOutputStream.write(s);
    }

    protected static final void writeInt(ByteArrayOutputStream byteArrayOutputStream, int i) {
        if (LITTLE_ENDIAN) {
            byteArrayOutputStream.write(i);
            byteArrayOutputStream.write(i >>> 8);
            byteArrayOutputStream.write(i >>> 16);
            byteArrayOutputStream.write(i >>> 24);
            return;
        }
        byteArrayOutputStream.write(i >>> 24);
        byteArrayOutputStream.write(i >>> 16);
        byteArrayOutputStream.write(i >>> 8);
        byteArrayOutputStream.write(i);
    }

    protected static final void writeLong(ByteArrayOutputStream byteArrayOutputStream, long j) {
        if (LITTLE_ENDIAN) {
            byteArrayOutputStream.write((byte) ((int) j));
            byteArrayOutputStream.write((byte) ((int) (j >>> 8)));
            byteArrayOutputStream.write((byte) ((int) (j >>> 16)));
            byteArrayOutputStream.write((byte) ((int) (j >>> 24)));
            byteArrayOutputStream.write((byte) ((int) (j >>> 32)));
            byteArrayOutputStream.write((byte) ((int) (j >>> 40)));
            byteArrayOutputStream.write((byte) ((int) (j >>> 48)));
            byteArrayOutputStream.write((byte) ((int) (j >>> 56)));
            return;
        }
        byteArrayOutputStream.write((byte) ((int) (j >>> 56)));
        byteArrayOutputStream.write((byte) ((int) (j >>> 48)));
        byteArrayOutputStream.write((byte) ((int) (j >>> 40)));
        byteArrayOutputStream.write((byte) ((int) (j >>> 32)));
        byteArrayOutputStream.write((byte) ((int) (j >>> 24)));
        byteArrayOutputStream.write((byte) ((int) (j >>> 16)));
        byteArrayOutputStream.write((byte) ((int) (j >>> 8)));
        byteArrayOutputStream.write((byte) ((int) j));
    }

    protected static final void writeDouble(ByteArrayOutputStream byteArrayOutputStream, double d) {
        writeLong(byteArrayOutputStream, Double.doubleToLongBits(d));
    }

    protected static final void writeSize(ByteArrayOutputStream byteArrayOutputStream, int i) {
        writeInt(byteArrayOutputStream, i);
    }

    protected static final void writeByteSize(ByteArrayOutputStream byteArrayOutputStream, byte b) {
        byteArrayOutputStream.write(b);
    }

    protected static final void writeBytes(ByteArrayOutputStream byteArrayOutputStream, byte[] bArr) {
        writeSize(byteArrayOutputStream, bArr.length);
        byteArrayOutputStream.write(bArr, 0, bArr.length);
    }

    protected static final void writeAlignment(ByteArrayOutputStream byteArrayOutputStream, int i) {
        int size = byteArrayOutputStream.size() % i;
        if (size != 0) {
            for (int i2 = 0; i2 < i - size; i2++) {
                byteArrayOutputStream.write(0);
            }
        }
    }

    protected static final byte[] readBytes(ByteBuffer byteBuffer) {
        byte[] bArr = new byte[readSize(byteBuffer)];
        byteBuffer.get(bArr);
        return bArr;
    }

    protected static final int readSize(ByteBuffer byteBuffer) {
        if (byteBuffer.hasRemaining()) {
            return byteBuffer.getInt();
        }
        ALog.w(LOG_TAG, "readSize No data remaining in the buffer");
        return 0;
    }

    protected static final byte readByteSize(ByteBuffer byteBuffer) {
        if (byteBuffer.hasRemaining()) {
            return byteBuffer.get();
        }
        ALog.w(LOG_TAG, "readByteSize No data remaining in the buffer");
        return 0;
    }

    /* access modifiers changed from: protected */
    public final Object readValue(ByteBuffer byteBuffer) {
        if (byteBuffer.hasRemaining()) {
            return readValueOfType(byteBuffer.get(), byteBuffer);
        }
        ALog.w(LOG_TAG, "readValue No data remaining in the buffer");
        return 0;
    }

    protected static final void readAlignment(ByteBuffer byteBuffer, int i) {
        int position = byteBuffer.position() % i;
        if (position != 0) {
            byteBuffer.position((byteBuffer.position() + i) - position);
        }
    }

    /* access modifiers changed from: protected */
    public Object readValueOfType(byte b, ByteBuffer byteBuffer) {
        switch (b) {
            case 16:
                return null;
            case 17:
                return true;
            case 18:
                return false;
            case 19:
                return Integer.valueOf(byteBuffer.getInt());
            case 20:
                return Long.valueOf(byteBuffer.getLong());
            case 21:
                return Double.valueOf(byteBuffer.getDouble());
            case 22:
                return new String(readBytes(byteBuffer), UTF8);
            default:
                return readValueOfComplexType(b, byteBuffer);
        }
    }

    /* access modifiers changed from: protected */
    public Object readValueOfComplexType(byte b, ByteBuffer byteBuffer) {
        switch (b) {
            case 23:
                return readMap(byteBuffer);
            case 24:
                return readSet(byteBuffer);
            case 25:
                return readBytes(byteBuffer);
            case 26:
                return readShorts(byteBuffer);
            case 27:
                return readInts(byteBuffer);
            default:
                ALog.e(LOG_TAG, "Wrong type:" + ((int) b));
                return false;
        }
    }

    private Map<Object, Object> readMap(ByteBuffer byteBuffer) {
        int readSize = readSize(byteBuffer);
        HashMap hashMap = new HashMap(readSize, 1.0f);
        for (int i = 0; i < readSize; i++) {
            hashMap.put(readValue(byteBuffer), readValue(byteBuffer));
        }
        return hashMap;
    }

    private HashSet<Object> readSet(ByteBuffer byteBuffer) {
        int readSize = readSize(byteBuffer);
        HashSet<Object> hashSet = new HashSet<>(readSize);
        for (int i = 0; i < readSize; i++) {
            hashSet.add(readValue(byteBuffer));
        }
        return hashSet;
    }

    private short[] readShorts(ByteBuffer byteBuffer) {
        int readSize = readSize(byteBuffer);
        short[] sArr = new short[readSize];
        readAlignment(byteBuffer, 2);
        byteBuffer.asShortBuffer().get(sArr);
        byteBuffer.position(byteBuffer.position() + (readSize * 2));
        return sArr;
    }

    private int[] readInts(ByteBuffer byteBuffer) {
        int readSize = readSize(byteBuffer);
        int[] iArr = new int[readSize];
        readAlignment(byteBuffer, 4);
        byteBuffer.asIntBuffer().get(iArr);
        byteBuffer.position(byteBuffer.position() + (readSize * 4));
        return iArr;
    }

    static final class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
        ExposedByteArrayOutputStream() {
        }

        /* access modifiers changed from: package-private */
        public byte[] buffer() {
            return this.buf;
        }
    }
}
