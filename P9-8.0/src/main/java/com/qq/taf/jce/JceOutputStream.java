package com.qq.taf.jce;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JceOutputStream {
    private ByteBuffer mU;
    protected String mV;

    public JceOutputStream(ByteBuffer bs) {
        this.mV = "GBK";
        this.mU = bs;
    }

    public JceOutputStream(int capacity) {
        this.mV = "GBK";
        this.mU = ByteBuffer.allocate(capacity);
    }

    public JceOutputStream() {
        this(128);
    }

    public ByteBuffer getByteBuffer() {
        return this.mU;
    }

    public byte[] toByteArray() {
        byte[] newBytes = new byte[this.mU.position()];
        System.arraycopy(this.mU.array(), 0, newBytes, 0, this.mU.position());
        return newBytes;
    }

    public void reserve(int len) {
        if (this.mU.remaining() < len) {
            ByteBuffer bs2 = ByteBuffer.allocate((this.mU.capacity() + len) * 2);
            bs2.put(this.mU.array(), 0, this.mU.position());
            this.mU = bs2;
        }
    }

    public void writeHead(byte type, int tag) {
        if (tag < 15) {
            this.mU.put((byte) ((tag << 4) | type));
        } else if (tag >= 256) {
            throw new c("tag is too large: " + tag);
        } else {
            this.mU.put((byte) (type | 240));
            this.mU.put((byte) tag);
        }
    }

    public void write(boolean b, int tag) {
        int i = 0;
        if (b) {
            i = 1;
        }
        write((byte) i, tag);
    }

    public void write(byte b, int tag) {
        reserve(3);
        if (b != (byte) 0) {
            writeHead((byte) 0, tag);
            this.mU.put(b);
            return;
        }
        writeHead(JceStruct.ZERO_TAG, tag);
    }

    public void write(short n, int tag) {
        reserve(4);
        if (n >= (short) -128 && n <= (short) 127) {
            write((byte) n, tag);
            return;
        }
        writeHead((byte) 1, tag);
        this.mU.putShort(n);
    }

    public void write(int n, int tag) {
        reserve(6);
        if (n >= -32768 && n <= 32767) {
            write((short) n, tag);
            return;
        }
        writeHead((byte) 2, tag);
        this.mU.putInt(n);
    }

    public void write(long n, int tag) {
        Object obj = 1;
        reserve(10);
        if ((n < -2147483648L ? 1 : null) == null) {
            if (n <= 2147483647L) {
                obj = null;
            }
            if (obj == null) {
                write((int) n, tag);
                return;
            }
        }
        writeHead((byte) 3, tag);
        this.mU.putLong(n);
    }

    public void write(float n, int tag) {
        reserve(6);
        writeHead((byte) 4, tag);
        this.mU.putFloat(n);
    }

    public void write(double n, int tag) {
        reserve(10);
        writeHead((byte) 5, tag);
        this.mU.putDouble(n);
    }

    public void writeStringByte(String s, int tag) {
        byte[] by = a.E(s);
        reserve(by.length + 10);
        if (by.length <= 255) {
            writeHead((byte) 6, tag);
            this.mU.put((byte) by.length);
            this.mU.put(by);
            return;
        }
        writeHead((byte) 7, tag);
        this.mU.putInt(by.length);
        this.mU.put(by);
    }

    public void writeByteString(String s, int tag) {
        reserve(s.length() + 10);
        byte[] by = a.E(s);
        if (by.length <= 255) {
            writeHead((byte) 6, tag);
            this.mU.put((byte) by.length);
            this.mU.put(by);
            return;
        }
        writeHead((byte) 7, tag);
        this.mU.putInt(by.length);
        this.mU.put(by);
    }

    public void write(String s, int tag) {
        byte[] by;
        try {
            by = s.getBytes(this.mV);
        } catch (UnsupportedEncodingException e) {
            by = s.getBytes();
        }
        reserve(by.length + 10);
        if (by.length <= 255) {
            writeHead((byte) 6, tag);
            this.mU.put((byte) by.length);
            this.mU.put(by);
            return;
        }
        writeHead((byte) 7, tag);
        this.mU.putInt(by.length);
        this.mU.put(by);
    }

    public <K, V> void write(Map<K, V> m, int tag) {
        reserve(8);
        writeHead((byte) 8, tag);
        write(m != null ? m.size() : 0, 0);
        if (m != null) {
            for (Entry<K, V> en : m.entrySet()) {
                write(en.getKey(), 0);
                write(en.getValue(), 1);
            }
        }
    }

    public void write(boolean[] l, int tag) {
        reserve(8);
        writeHead((byte) 9, tag);
        write(l.length, 0);
        boolean[] zArr = l;
        for (boolean e : l) {
            write(e, 0);
        }
    }

    public void write(byte[] l, int tag) {
        reserve(l.length + 8);
        writeHead(JceStruct.SIMPLE_LIST, tag);
        writeHead((byte) 0, 0);
        write(l.length, 0);
        this.mU.put(l);
    }

    public void write(short[] l, int tag) {
        reserve(8);
        writeHead((byte) 9, tag);
        write(l.length, 0);
        short[] sArr = l;
        for (short e : l) {
            write(e, 0);
        }
    }

    public void write(int[] l, int tag) {
        reserve(8);
        writeHead((byte) 9, tag);
        write(l.length, 0);
        int[] iArr = l;
        for (int e : l) {
            write(e, 0);
        }
    }

    public void write(long[] l, int tag) {
        reserve(8);
        writeHead((byte) 9, tag);
        write(l.length, 0);
        long[] jArr = l;
        for (long e : l) {
            write(e, 0);
        }
    }

    public void write(float[] l, int tag) {
        reserve(8);
        writeHead((byte) 9, tag);
        write(l.length, 0);
        float[] fArr = l;
        for (float e : l) {
            write(e, 0);
        }
    }

    public void write(double[] l, int tag) {
        reserve(8);
        writeHead((byte) 9, tag);
        write(l.length, 0);
        double[] dArr = l;
        for (double e : l) {
            write(e, 0);
        }
    }

    public <T> void write(T[] l, int tag) {
        a(l, tag);
    }

    private void a(Object[] l, int tag) {
        reserve(8);
        writeHead((byte) 9, tag);
        write(l.length, 0);
        Object[] objArr = l;
        for (Object e : l) {
            write(e, 0);
        }
    }

    public <T> void write(Collection<T> l, int tag) {
        reserve(8);
        writeHead((byte) 9, tag);
        write(l != null ? l.size() : 0, 0);
        if (l != null) {
            for (T e : l) {
                write((Object) e, 0);
            }
        }
    }

    public void write(JceStruct o, int tag) {
        reserve(2);
        writeHead((byte) 10, tag);
        o.writeTo(this);
        reserve(2);
        writeHead(JceStruct.STRUCT_END, 0);
    }

    public void write(Byte o, int tag) {
        write(o.byteValue(), tag);
    }

    public void write(Boolean o, int tag) {
        write(o.booleanValue(), tag);
    }

    public void write(Short o, int tag) {
        write(o.shortValue(), tag);
    }

    public void write(Integer o, int tag) {
        write(o.intValue(), tag);
    }

    public void write(Long o, int tag) {
        write(o.longValue(), tag);
    }

    public void write(Float o, int tag) {
        write(o.floatValue(), tag);
    }

    public void write(Double o, int tag) {
        write(o.doubleValue(), tag);
    }

    public void write(Object o, int tag) {
        if (o instanceof Byte) {
            write(((Byte) o).byteValue(), tag);
        } else if (o instanceof Boolean) {
            write(((Boolean) o).booleanValue(), tag);
        } else if (o instanceof Short) {
            write(((Short) o).shortValue(), tag);
        } else if (o instanceof Integer) {
            write(((Integer) o).intValue(), tag);
        } else if (o instanceof Long) {
            write(((Long) o).longValue(), tag);
        } else if (o instanceof Float) {
            write(((Float) o).floatValue(), tag);
        } else if (o instanceof Double) {
            write(((Double) o).doubleValue(), tag);
        } else if (o instanceof String) {
            write((String) o, tag);
        } else if (o instanceof Map) {
            write((Map) o, tag);
        } else if (o instanceof List) {
            write((List) o, tag);
        } else if (o instanceof JceStruct) {
            write((JceStruct) o, tag);
        } else if (o instanceof byte[]) {
            write((byte[]) o, tag);
        } else if (o instanceof boolean[]) {
            write((boolean[]) o, tag);
        } else if (o instanceof short[]) {
            write((short[]) o, tag);
        } else if (o instanceof int[]) {
            write((int[]) o, tag);
        } else if (o instanceof long[]) {
            write((long[]) o, tag);
        } else if (o instanceof float[]) {
            write((float[]) o, tag);
        } else if (o instanceof double[]) {
            write((double[]) o, tag);
        } else if (o.getClass().isArray()) {
            a((Object[]) o, tag);
        } else if (o instanceof Collection) {
            write((Collection) o, tag);
        } else {
            throw new c("write object error: unsupport type. " + o.getClass());
        }
    }

    public int setServerEncoding(String se) {
        this.mV = se;
        return 0;
    }

    public static void main(String[] args) {
        JceOutputStream os = new JceOutputStream();
        os.write(1311768467283714885L, 0);
        System.out.println(a.c(os.getByteBuffer().array()));
        System.out.println(Arrays.toString(os.toByteArray()));
    }
}
