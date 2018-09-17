package com.qq.taf.jce;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class JceInputStream {
    private ByteBuffer mU;
    protected String mV = "GBK";

    public static class a {
        public byte mW;
        public int tag;
    }

    public JceInputStream(ByteBuffer bs) {
        this.mU = bs;
    }

    public JceInputStream(byte[] bs) {
        this.mU = ByteBuffer.wrap(bs);
    }

    public JceInputStream(byte[] bs, int pos) {
        this.mU = ByteBuffer.wrap(bs);
        this.mU.position(pos);
    }

    public void warp(byte[] bs) {
        wrap(bs);
    }

    public void wrap(byte[] bs) {
        this.mU = ByteBuffer.wrap(bs);
    }

    public static int readHead(a hd, ByteBuffer bb) {
        byte b = bb.get();
        hd.mW = (byte) ((byte) (b & 15));
        hd.tag = (b & 240) >> 4;
        if (hd.tag != 15) {
            return 1;
        }
        hd.tag = bb.get() & 255;
        return 2;
    }

    public void readHead(a hd) {
        readHead(hd, this.mU);
    }

    private int a(a hd) {
        return readHead(hd, this.mU.duplicate());
    }

    private void skip(int len) {
        this.mU.position(this.mU.position() + len);
    }

    public boolean skipToTag(int tag) {
        try {
            a hd = new a();
            while (true) {
                int len = a(hd);
                if (tag > hd.tag && hd.mW != JceStruct.STRUCT_END) {
                    skip(len);
                    a(hd.mW);
                }
            }
            if (tag != hd.tag) {
                return false;
            }
            return true;
        } catch (b e) {
            return false;
        } catch (BufferUnderflowException e2) {
            return false;
        }
    }

    public void skipToStructEnd() {
        a hd = new a();
        while (this.mU.remaining() != 0) {
            readHead(hd);
            a(hd.mW);
            if (hd.mW == JceStruct.STRUCT_END) {
                return;
            }
        }
    }

    private void q() {
        a hd = new a();
        readHead(hd);
        a(hd.mW);
    }

    private void a(byte type) {
        int size;
        int i;
        switch (type) {
            case (byte) 0:
                skip(1);
                return;
            case (byte) 1:
                skip(2);
                return;
            case (byte) 2:
                skip(4);
                return;
            case (byte) 3:
                skip(8);
                return;
            case (byte) 4:
                skip(4);
                return;
            case (byte) 5:
                skip(8);
                return;
            case (byte) 6:
                int len = this.mU.get();
                if (len < 0) {
                    len += 256;
                }
                skip(len);
                return;
            case (byte) 7:
                skip(this.mU.getInt());
                return;
            case (byte) 8:
                size = read(0, 0, true);
                for (i = 0; i < size * 2; i++) {
                    q();
                }
                return;
            case (byte) 9:
                size = read(0, 0, true);
                for (i = 0; i < size; i++) {
                    q();
                }
                return;
            case (byte) 10:
                skipToStructEnd();
                return;
            case (byte) 11:
            case (byte) 12:
                return;
            case (byte) 13:
                a hd = new a();
                readHead(hd);
                if (hd.mW == (byte) 0) {
                    skip(read(0, 0, true));
                    return;
                }
                throw new b("skipField with invalid type, type value: " + type + ", " + hd.mW);
            default:
                throw new b("invalid type.");
        }
    }

    public boolean read(boolean b, int tag, boolean isRequire) {
        if (read((byte) 0, tag, isRequire) == (byte) 0) {
            return false;
        }
        return true;
    }

    public byte read(byte c, int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 0:
                    return this.mU.get();
                case (byte) 11:
                    return c;
                case (byte) 12:
                    return (byte) 0;
                default:
                    throw new b("type mismatch.");
            }
        } else if (!isRequire) {
            return c;
        } else {
            throw new b("require field not exist.");
        }
    }

    public short read(short n, int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 0:
                    return (short) this.mU.get();
                case (byte) 1:
                    return this.mU.getShort();
                case (byte) 11:
                    return n;
                case (byte) 12:
                    return (short) 0;
                default:
                    throw new b("type mismatch.");
            }
        } else if (!isRequire) {
            return n;
        } else {
            throw new b("require field not exist.");
        }
    }

    public int read(int n, int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 0:
                    return this.mU.get();
                case (byte) 1:
                    return this.mU.getShort();
                case (byte) 2:
                    return this.mU.getInt();
                case (byte) 11:
                    return n;
                case (byte) 12:
                    return 0;
                default:
                    throw new b("type mismatch.");
            }
        } else if (!isRequire) {
            return n;
        } else {
            throw new b("require field not exist.");
        }
    }

    public long read(long n, int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 0:
                    return (long) this.mU.get();
                case (byte) 1:
                    return (long) this.mU.getShort();
                case (byte) 2:
                    return (long) this.mU.getInt();
                case (byte) 3:
                    return this.mU.getLong();
                case (byte) 12:
                    return 0;
                default:
                    throw new b("type mismatch.");
            }
        } else if (!isRequire) {
            return n;
        } else {
            throw new b("require field not exist.");
        }
    }

    public float read(float n, int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 4:
                    return this.mU.getFloat();
                case (byte) 11:
                    return n;
                case (byte) 12:
                    return 0.0f;
                default:
                    throw new b("type mismatch.");
            }
        } else if (!isRequire) {
            return n;
        } else {
            throw new b("require field not exist.");
        }
    }

    public double read(double n, int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 4:
                    return (double) this.mU.getFloat();
                case (byte) 5:
                    return this.mU.getDouble();
                case (byte) 11:
                    return n;
                case (byte) 12:
                    return 0.0d;
                default:
                    throw new b("type mismatch.");
            }
        } else if (!isRequire) {
            return n;
        } else {
            throw new b("require field not exist.");
        }
    }

    public String readByteString(String s, int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            int len;
            byte[] ss;
            switch (hd.mW) {
                case (byte) 6:
                    len = this.mU.get();
                    if (len < 0) {
                        len += 256;
                    }
                    ss = new byte[len];
                    this.mU.get(ss);
                    return a.c(ss);
                case (byte) 7:
                    len = this.mU.getInt();
                    if (len <= JceStruct.JCE_MAX_STRING_LENGTH && len >= 0) {
                        ss = new byte[len];
                        this.mU.get(ss);
                        return a.c(ss);
                    }
                    throw new b("String too long: " + len);
                case (byte) 11:
                    return s;
                default:
                    throw new b("type mismatch.");
            }
        } else if (!isRequire) {
            return s;
        } else {
            throw new b("require field not exist.");
        }
    }

    public String read(String s, int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            int len;
            byte[] ss;
            switch (hd.mW) {
                case (byte) 6:
                    len = this.mU.get();
                    if (len < 0) {
                        len += 256;
                    }
                    ss = new byte[len];
                    this.mU.get(ss);
                    try {
                        return new String(ss, this.mV);
                    } catch (UnsupportedEncodingException e) {
                        return new String(ss);
                    }
                case (byte) 7:
                    len = this.mU.getInt();
                    if (len <= JceStruct.JCE_MAX_STRING_LENGTH && len >= 0) {
                        ss = new byte[len];
                        this.mU.get(ss);
                        try {
                            return new String(ss, this.mV);
                        } catch (UnsupportedEncodingException e2) {
                            return new String(ss);
                        }
                    }
                    throw new b("String too long: " + len);
                case (byte) 11:
                    return s;
                default:
                    throw new b("type mismatch.");
            }
        } else if (!isRequire) {
            return s;
        } else {
            throw new b("require field not exist.");
        }
    }

    public String readString(int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            int len;
            byte[] ss;
            switch (hd.mW) {
                case (byte) 6:
                    len = this.mU.get();
                    if (len < 0) {
                        len += 256;
                    }
                    ss = new byte[len];
                    this.mU.get(ss);
                    try {
                        return new String(ss, this.mV);
                    } catch (UnsupportedEncodingException e) {
                        return new String(ss);
                    }
                case (byte) 7:
                    len = this.mU.getInt();
                    if (len <= JceStruct.JCE_MAX_STRING_LENGTH && len >= 0) {
                        ss = new byte[len];
                        this.mU.get(ss);
                        try {
                            return new String(ss, this.mV);
                        } catch (UnsupportedEncodingException e2) {
                            return new String(ss);
                        }
                    }
                    throw new b("String too long: " + len);
                case (byte) 11:
                    return null;
                default:
                    throw new b("type mismatch.");
            }
        } else if (!isRequire) {
            return null;
        } else {
            throw new b("require field not exist.");
        }
    }

    public String[] read(String[] s, int tag, boolean isRequire) {
        return (String[]) readArray((Object[]) s, tag, isRequire);
    }

    public Map<String, String> readStringMap(int tag, boolean isRequire) {
        HashMap<String, String> mr = new HashMap();
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 8:
                    int size = read(0, 0, true);
                    if (size >= 0) {
                        for (int i = 0; i < size; i++) {
                            mr.put(readString(0, true), readString(1, true));
                        }
                        break;
                    }
                    throw new b("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new b("type mismatch.");
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return mr;
    }

    public <K, V> HashMap<K, V> readMap(Map<K, V> m, int tag, boolean isRequire) {
        return (HashMap) a(new HashMap(), m, tag, isRequire);
    }

    private <K, V> Map<K, V> a(Map<K, V> mr, Map<K, V> m, int tag, boolean isRequire) {
        if (m == null || m.isEmpty()) {
            return new HashMap();
        }
        Entry<K, V> en = (Entry) m.entrySet().iterator().next();
        Object mk = en.getKey();
        Object mv = en.getValue();
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 8:
                    int size = read(0, 0, true);
                    if (size >= 0) {
                        for (int i = 0; i < size; i++) {
                            mr.put(read(mk, 0, true), read(mv, 1, true));
                        }
                        break;
                    }
                    throw new b("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new b("type mismatch.");
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return mr;
    }

    public List readList(int tag, boolean isRequire) {
        List lr = new ArrayList();
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 9:
                    int size = read(0, 0, true);
                    if (size >= 0) {
                        for (int i = 0; i < size; i++) {
                            a subH = new a();
                            readHead(subH);
                            switch (subH.mW) {
                                case (byte) 0:
                                    skip(1);
                                    break;
                                case (byte) 1:
                                    skip(2);
                                    break;
                                case (byte) 2:
                                    skip(4);
                                    break;
                                case (byte) 3:
                                    skip(8);
                                    break;
                                case (byte) 4:
                                    skip(4);
                                    break;
                                case (byte) 5:
                                    skip(8);
                                    break;
                                case (byte) 6:
                                    int len = this.mU.get();
                                    if (len < 0) {
                                        len += 256;
                                    }
                                    skip(len);
                                    break;
                                case (byte) 7:
                                    skip(this.mU.getInt());
                                    break;
                                case (byte) 8:
                                case (byte) 9:
                                case (byte) 11:
                                    break;
                                case (byte) 10:
                                    try {
                                        JceStruct struct = (JceStruct) Class.forName(JceStruct.class.getName()).getConstructor(new Class[0]).newInstance(new Object[0]);
                                        struct.readFrom(this);
                                        skipToStructEnd();
                                        lr.add(struct);
                                        break;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        throw new b("type mismatch." + e);
                                    }
                                case (byte) 12:
                                    lr.add(new Integer(0));
                                    break;
                                default:
                                    throw new b("type mismatch.");
                            }
                        }
                        break;
                    }
                    throw new b("size invalid: " + size);
                default:
                    throw new b("type mismatch.");
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return lr;
    }

    public boolean[] read(boolean[] l, int tag, boolean isRequire) {
        boolean[] lr = null;
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 9:
                    int size = read(0, 0, true);
                    if (size >= 0) {
                        lr = new boolean[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = read(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new b("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new b("type mismatch.");
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return lr;
    }

    public byte[] read(byte[] l, int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            int size;
            byte[] lr;
            switch (hd.mW) {
                case (byte) 9:
                    size = read(0, 0, true);
                    if (size >= 0) {
                        lr = new byte[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = (byte) read(lr[0], 0, true);
                        }
                        return lr;
                    }
                    throw new b("size invalid: " + size);
                case (byte) 11:
                    return null;
                case (byte) 13:
                    a hh = new a();
                    readHead(hh);
                    if (hh.mW == (byte) 0) {
                        size = read(0, 0, true);
                        if (size >= 0) {
                            lr = new byte[size];
                            this.mU.get(lr);
                            return lr;
                        }
                        throw new b("invalid size, tag: " + tag + ", type: " + hd.mW + ", " + hh.mW + ", size: " + size);
                    }
                    throw new b("type mismatch, tag: " + tag + ", type: " + hd.mW + ", " + hh.mW);
                default:
                    throw new b("type mismatch.");
            }
        } else if (!isRequire) {
            return null;
        } else {
            throw new b("require field not exist.");
        }
    }

    public short[] read(short[] l, int tag, boolean isRequire) {
        short[] lr = null;
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 9:
                    int size = read(0, 0, true);
                    if (size >= 0) {
                        lr = new short[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = (short) read(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new b("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new b("type mismatch.");
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return lr;
    }

    public int[] read(int[] l, int tag, boolean isRequire) {
        int[] lr = null;
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 9:
                    int size = read(0, 0, true);
                    if (size >= 0) {
                        lr = new int[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = read(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new b("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new b("type mismatch.");
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return lr;
    }

    public long[] read(long[] l, int tag, boolean isRequire) {
        long[] lr = null;
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 9:
                    int size = read(0, 0, true);
                    if (size >= 0) {
                        lr = new long[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = read(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new b("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new b("type mismatch.");
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return lr;
    }

    public float[] read(float[] l, int tag, boolean isRequire) {
        float[] lr = null;
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 9:
                    int size = read(0, 0, true);
                    if (size >= 0) {
                        lr = new float[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = read(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new b("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new b("type mismatch.");
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return lr;
    }

    public double[] read(double[] l, int tag, boolean isRequire) {
        double[] lr = null;
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 9:
                    int size = read(0, 0, true);
                    if (size >= 0) {
                        lr = new double[size];
                        for (int i = 0; i < size; i++) {
                            lr[i] = read(lr[0], 0, true);
                        }
                        break;
                    }
                    throw new b("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new b("type mismatch.");
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return lr;
    }

    public <T> T[] readArray(T[] l, int tag, boolean isRequire) {
        if (l != null && l.length != 0) {
            return a(l[0], tag, isRequire);
        }
        throw new b("unable to get type of key and value.");
    }

    public <T> List<T> readArray(List<T> l, int tag, boolean isRequire) {
        if (l == null || l.isEmpty()) {
            return new ArrayList();
        }
        Object[] tt = a(l.get(0), tag, isRequire);
        if (tt == null) {
            return null;
        }
        ArrayList<T> ll = new ArrayList();
        for (Object add : tt) {
            ll.add(add);
        }
        return ll;
    }

    private <T> T[] a(T mt, int tag, boolean isRequire) {
        if (skipToTag(tag)) {
            a hd = new a();
            readHead(hd);
            switch (hd.mW) {
                case (byte) 9:
                    int size = read(0, 0, true);
                    if (size >= 0) {
                        Object[] lr = (Object[]) Array.newInstance(mt.getClass(), size);
                        for (int i = 0; i < size; i++) {
                            lr[i] = read((Object) mt, 0, true);
                        }
                        return lr;
                    }
                    throw new b("size invalid: " + size);
                case (byte) 11:
                    break;
                default:
                    throw new b("type mismatch.");
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return null;
    }

    public JceStruct directRead(JceStruct o, int tag, boolean isRequire) {
        JceStruct ref = null;
        if (skipToTag(tag)) {
            try {
                ref = o.newInit();
                a hd = new a();
                readHead(hd);
                if (hd.mW == (byte) 10) {
                    ref.readFrom(this);
                    skipToStructEnd();
                } else {
                    throw new b("type mismatch.");
                }
            } catch (Exception e) {
                throw new b(e.getMessage());
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return ref;
    }

    public JceStruct read(JceStruct o, int tag, boolean isRequire) {
        JceStruct ref = null;
        if (skipToTag(tag)) {
            try {
                ref = (JceStruct) o.getClass().newInstance();
                a hd = new a();
                readHead(hd);
                if (hd.mW == (byte) 10) {
                    ref.readFrom(this);
                    skipToStructEnd();
                } else {
                    throw new b("type mismatch.");
                }
            } catch (Exception e) {
                throw new b(e.getMessage());
            }
        } else if (isRequire) {
            throw new b("require field not exist.");
        }
        return ref;
    }

    public JceStruct[] read(JceStruct[] o, int tag, boolean isRequire) {
        return (JceStruct[]) readArray((Object[]) o, tag, isRequire);
    }

    public <T> Object read(T o, int tag, boolean isRequire) {
        if (o instanceof Byte) {
            return Byte.valueOf(read((byte) 0, tag, isRequire));
        }
        if (o instanceof Boolean) {
            return Boolean.valueOf(read(false, tag, isRequire));
        }
        if (o instanceof Short) {
            return Short.valueOf(read((short) 0, tag, isRequire));
        }
        if (o instanceof Integer) {
            return Integer.valueOf(read(0, tag, isRequire));
        }
        if (o instanceof Long) {
            return Long.valueOf(read(0, tag, isRequire));
        }
        if (o instanceof Float) {
            return Float.valueOf(read(0.0f, tag, isRequire));
        }
        if (o instanceof Double) {
            return Double.valueOf(read(0.0d, tag, isRequire));
        }
        if (o instanceof String) {
            return readString(tag, isRequire);
        }
        if (o instanceof Map) {
            return readMap((Map) o, tag, isRequire);
        }
        if (o instanceof List) {
            return readArray((List) o, tag, isRequire);
        }
        if (o instanceof JceStruct) {
            return read((JceStruct) o, tag, isRequire);
        }
        if (!o.getClass().isArray()) {
            throw new b("read object error: unsupport type.");
        } else if ((o instanceof byte[]) || (o instanceof Byte[])) {
            return read(null, tag, isRequire);
        } else {
            if (o instanceof boolean[]) {
                return read(null, tag, isRequire);
            }
            if (o instanceof short[]) {
                return read(null, tag, isRequire);
            }
            if (o instanceof int[]) {
                return read(null, tag, isRequire);
            }
            if (o instanceof long[]) {
                return read(null, tag, isRequire);
            }
            if (o instanceof float[]) {
                return read(null, tag, isRequire);
            }
            if (o instanceof double[]) {
                return read(null, tag, isRequire);
            }
            return readArray((Object[]) o, tag, isRequire);
        }
    }

    public int setServerEncoding(String se) {
        this.mV = se;
        return 0;
    }

    public static void main(String[] args) {
    }

    public ByteBuffer getBs() {
        return this.mU;
    }
}
