package tmsdkobf;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import tmsdk.bg.module.wifidetect.WifiDetectManager;
import tmsdk.common.module.aresengine.SystemCallLogFilterConsts;

public class fr {
    private ByteBuffer mx;
    protected String my;

    public fr(int i) {
        this.my = "GBK";
        this.mx = ByteBuffer.allocate(i);
    }

    public fr() {
        this(SystemCallLogFilterConsts.NOTIFY_SHORT_CALL);
    }

    public ByteBuffer t() {
        return this.mx;
    }

    public byte[] toByteArray() {
        Object obj = new byte[this.mx.position()];
        System.arraycopy(this.mx.array(), 0, obj, 0, this.mx.position());
        return obj;
    }

    public void ag(int i) {
        if (this.mx.remaining() < i) {
            ByteBuffer allocate = ByteBuffer.allocate((this.mx.capacity() + i) * 2);
            allocate.put(this.mx.array(), 0, this.mx.position());
            this.mx = allocate;
        }
    }

    public void a(byte b, int i) {
        if (i < 15) {
            this.mx.put((byte) ((i << 4) | b));
        } else if (i >= WifiDetectManager.SECURITY_NONE) {
            throw new fp("tag is too large: " + i);
        } else {
            this.mx.put((byte) (b | 240));
            this.mx.put((byte) i);
        }
    }

    public void a(boolean z, int i) {
        int i2 = 0;
        if (z) {
            i2 = 1;
        }
        b((byte) i2, i);
    }

    public void b(byte b, int i) {
        ag(3);
        if (b != null) {
            a((byte) 0, i);
            this.mx.put(b);
            return;
        }
        a((byte) fs.ZERO_TAG, i);
    }

    public void a(short s, int i) {
        ag(4);
        if (s >= (short) -128 && s <= (short) 127) {
            b((byte) s, i);
            return;
        }
        a((byte) 1, i);
        this.mx.putShort(s);
    }

    public void write(int i, int i2) {
        ag(6);
        if (i >= -32768 && i <= 32767) {
            a((short) i, i2);
            return;
        }
        a((byte) 2, i2);
        this.mx.putInt(i);
    }

    public void b(long j, int i) {
        Object obj = 1;
        ag(10);
        if ((j < -2147483648L ? 1 : null) == null) {
            if (j <= 2147483647L) {
                obj = null;
            }
            if (obj == null) {
                write((int) j, i);
                return;
            }
        }
        a((byte) 3, i);
        this.mx.putLong(j);
    }

    public void a(float f, int i) {
        ag(6);
        a((byte) 4, i);
        this.mx.putFloat(f);
    }

    public void a(double d, int i) {
        ag(10);
        a((byte) 5, i);
        this.mx.putDouble(d);
    }

    public void a(String str, int i) {
        byte[] bytes;
        try {
            bytes = str.getBytes(this.my);
        } catch (UnsupportedEncodingException e) {
            bytes = str.getBytes();
        }
        ag(bytes.length + 10);
        if (bytes.length <= 255) {
            a((byte) 6, i);
            this.mx.put((byte) bytes.length);
            this.mx.put(bytes);
            return;
        }
        a((byte) 7, i);
        this.mx.putInt(bytes.length);
        this.mx.put(bytes);
    }

    public <K, V> void a(Map<K, V> map, int i) {
        ag(8);
        a((byte) 8, i);
        write(map != null ? map.size() : 0, 0);
        if (map != null) {
            for (Entry entry : map.entrySet()) {
                a(entry.getKey(), 0);
                a(entry.getValue(), 1);
            }
        }
    }

    public void a(boolean[] zArr, int i) {
        ag(8);
        a((byte) 9, i);
        write(zArr.length, 0);
        for (boolean a : zArr) {
            a(a, 0);
        }
    }

    public void a(byte[] bArr, int i) {
        ag(bArr.length + 8);
        a((byte) fs.SIMPLE_LIST, i);
        a((byte) 0, 0);
        write(bArr.length, 0);
        this.mx.put(bArr);
    }

    public void a(short[] sArr, int i) {
        ag(8);
        a((byte) 9, i);
        write(sArr.length, 0);
        for (short a : sArr) {
            a(a, 0);
        }
    }

    public void a(int[] iArr, int i) {
        ag(8);
        a((byte) 9, i);
        write(iArr.length, 0);
        for (int write : iArr) {
            write(write, 0);
        }
    }

    public void a(long[] jArr, int i) {
        ag(8);
        a((byte) 9, i);
        write(jArr.length, 0);
        for (long b : jArr) {
            b(b, 0);
        }
    }

    public void a(float[] fArr, int i) {
        ag(8);
        a((byte) 9, i);
        write(fArr.length, 0);
        for (float a : fArr) {
            a(a, 0);
        }
    }

    public void a(double[] dArr, int i) {
        ag(8);
        a((byte) 9, i);
        write(dArr.length, 0);
        for (double a : dArr) {
            a(a, 0);
        }
    }

    private void a(Object[] objArr, int i) {
        ag(8);
        a((byte) 9, i);
        write(objArr.length, 0);
        for (Object a : objArr) {
            a(a, 0);
        }
    }

    public <T> void a(Collection<T> collection, int i) {
        ag(8);
        a((byte) 9, i);
        write(collection != null ? collection.size() : 0, 0);
        if (collection != null) {
            for (T a : collection) {
                a((Object) a, 0);
            }
        }
    }

    public void a(fs fsVar, int i) {
        ag(2);
        a((byte) 10, i);
        fsVar.writeTo(this);
        ag(2);
        a((byte) fs.STRUCT_END, 0);
    }

    public void a(Object obj, int i) {
        if (obj instanceof Byte) {
            b(((Byte) obj).byteValue(), i);
        } else if (obj instanceof Boolean) {
            a(((Boolean) obj).booleanValue(), i);
        } else if (obj instanceof Short) {
            a(((Short) obj).shortValue(), i);
        } else if (obj instanceof Integer) {
            write(((Integer) obj).intValue(), i);
        } else if (obj instanceof Long) {
            b(((Long) obj).longValue(), i);
        } else if (obj instanceof Float) {
            a(((Float) obj).floatValue(), i);
        } else if (obj instanceof Double) {
            a(((Double) obj).doubleValue(), i);
        } else if (obj instanceof String) {
            a((String) obj, i);
        } else if (obj instanceof Map) {
            a((Map) obj, i);
        } else if (obj instanceof List) {
            a((List) obj, i);
        } else if (obj instanceof fs) {
            a((fs) obj, i);
        } else if (obj instanceof byte[]) {
            a((byte[]) obj, i);
        } else if (obj instanceof boolean[]) {
            a((boolean[]) obj, i);
        } else if (obj instanceof short[]) {
            a((short[]) obj, i);
        } else if (obj instanceof int[]) {
            a((int[]) obj, i);
        } else if (obj instanceof long[]) {
            a((long[]) obj, i);
        } else if (obj instanceof float[]) {
            a((float[]) obj, i);
        } else if (obj instanceof double[]) {
            a((double[]) obj, i);
        } else if (obj.getClass().isArray()) {
            a((Object[]) obj, i);
        } else if (obj instanceof Collection) {
            a((Collection) obj, i);
        } else {
            throw new fp("write object error: unsupport type. " + obj.getClass());
        }
    }

    public int ae(String str) {
        this.my = str;
        return 0;
    }
}
