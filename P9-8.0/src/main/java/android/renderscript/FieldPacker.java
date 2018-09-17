package android.renderscript;

import android.util.Log;
import java.util.BitSet;

public class FieldPacker {
    private BitSet mAlignment;
    private byte[] mData;
    private int mLen;
    private int mPos;

    public FieldPacker(int len) {
        this.mPos = 0;
        this.mLen = len;
        this.mData = new byte[len];
        this.mAlignment = new BitSet();
    }

    public FieldPacker(byte[] data) {
        this.mPos = data.length;
        this.mLen = data.length;
        this.mData = data;
        this.mAlignment = new BitSet();
    }

    static FieldPacker createFromArray(Object[] args) {
        FieldPacker fp = new FieldPacker(RenderScript.sPointerSize * 8);
        for (Object arg : args) {
            fp.addSafely(arg);
        }
        fp.resize(fp.mPos);
        return fp;
    }

    public void align(int v) {
        if (v <= 0 || ((v - 1) & v) != 0) {
            throw new RSIllegalArgumentException("argument must be a non-negative non-zero power of 2: " + v);
        }
        while ((this.mPos & (v - 1)) != 0) {
            this.mAlignment.flip(this.mPos);
            byte[] bArr = this.mData;
            int i = this.mPos;
            this.mPos = i + 1;
            bArr[i] = (byte) 0;
        }
    }

    public void subalign(int v) {
        if (((v - 1) & v) != 0) {
            throw new RSIllegalArgumentException("argument must be a non-negative non-zero power of 2: " + v);
        }
        while ((this.mPos & (v - 1)) != 0) {
            this.mPos--;
        }
        if (this.mPos > 0) {
            while (this.mAlignment.get(this.mPos - 1)) {
                this.mPos--;
                this.mAlignment.flip(this.mPos);
            }
        }
    }

    public void reset() {
        this.mPos = 0;
    }

    public void reset(int i) {
        if (i < 0 || i > this.mLen) {
            throw new RSIllegalArgumentException("out of range argument: " + i);
        }
        this.mPos = i;
    }

    public void skip(int i) {
        int res = this.mPos + i;
        if (res < 0 || res > this.mLen) {
            throw new RSIllegalArgumentException("out of range argument: " + i);
        }
        this.mPos = res;
    }

    public void addI8(byte v) {
        byte[] bArr = this.mData;
        int i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = v;
    }

    public byte subI8() {
        subalign(1);
        byte[] bArr = this.mData;
        int i = this.mPos - 1;
        this.mPos = i;
        return bArr[i];
    }

    public void addI16(short v) {
        align(2);
        byte[] bArr = this.mData;
        int i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) (v & 255);
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) (v >> 8);
    }

    public short subI16() {
        subalign(2);
        byte[] bArr = this.mData;
        int i = this.mPos - 1;
        this.mPos = i;
        short v = (short) ((bArr[i] & 255) << 8);
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        return (short) (((short) (bArr[i] & 255)) | v);
    }

    public void addI32(int v) {
        align(4);
        byte[] bArr = this.mData;
        int i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) (v & 255);
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((v >> 8) & 255);
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((v >> 16) & 255);
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((v >> 24) & 255);
    }

    public int subI32() {
        subalign(4);
        byte[] bArr = this.mData;
        int i = this.mPos - 1;
        this.mPos = i;
        int v = (bArr[i] & 255) << 24;
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        v |= (bArr[i] & 255) << 16;
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        v |= (bArr[i] & 255) << 8;
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        return v | (bArr[i] & 255);
    }

    public void addI64(long v) {
        align(8);
        byte[] bArr = this.mData;
        int i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) (v & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 8) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 16) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 24) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 32) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 40) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 48) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 56) & 255));
    }

    public long subI64() {
        subalign(8);
        byte[] bArr = this.mData;
        int i = this.mPos - 1;
        this.mPos = i;
        long v = 0 | ((((long) bArr[i]) & 255) << 56);
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        v |= (((long) bArr[i]) & 255) << 48;
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        v |= (((long) bArr[i]) & 255) << 40;
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        v |= (((long) bArr[i]) & 255) << 32;
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        v |= (((long) bArr[i]) & 255) << 24;
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        v |= (((long) bArr[i]) & 255) << 16;
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        v |= (((long) bArr[i]) & 255) << 8;
        bArr = this.mData;
        i = this.mPos - 1;
        this.mPos = i;
        return v | (((long) bArr[i]) & 255);
    }

    public void addU8(short v) {
        if (v < (short) 0 || v > (short) 255) {
            Log.e("rs", "FieldPacker.addU8( " + v + " )");
            throw new IllegalArgumentException("Saving value out of range for type");
        }
        byte[] bArr = this.mData;
        int i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) v;
    }

    public void addU16(int v) {
        if (v < 0 || v > 65535) {
            Log.e("rs", "FieldPacker.addU16( " + v + " )");
            throw new IllegalArgumentException("Saving value out of range for type");
        }
        align(2);
        byte[] bArr = this.mData;
        int i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) (v & 255);
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) (v >> 8);
    }

    public void addU32(long v) {
        if (v < 0 || v > 4294967295L) {
            Log.e("rs", "FieldPacker.addU32( " + v + " )");
            throw new IllegalArgumentException("Saving value out of range for type");
        }
        align(4);
        byte[] bArr = this.mData;
        int i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) (v & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 8) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 16) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 24) & 255));
    }

    public void addU64(long v) {
        if (v < 0) {
            Log.e("rs", "FieldPacker.addU64( " + v + " )");
            throw new IllegalArgumentException("Saving value out of range for type");
        }
        align(8);
        byte[] bArr = this.mData;
        int i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) (v & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 8) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 16) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 24) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 32) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 40) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 48) & 255));
        bArr = this.mData;
        i = this.mPos;
        this.mPos = i + 1;
        bArr[i] = (byte) ((int) ((v >> 56) & 255));
    }

    public void addF32(float v) {
        addI32(Float.floatToRawIntBits(v));
    }

    public float subF32() {
        return Float.intBitsToFloat(subI32());
    }

    public void addF64(double v) {
        addI64(Double.doubleToRawLongBits(v));
    }

    public double subF64() {
        return Double.longBitsToDouble(subI64());
    }

    public void addObj(BaseObj obj) {
        if (obj != null) {
            if (RenderScript.sPointerSize == 8) {
                addI64(obj.getID(null));
                addI64(0);
                addI64(0);
                addI64(0);
                return;
            }
            addI32((int) obj.getID(null));
        } else if (RenderScript.sPointerSize == 8) {
            addI64(0);
            addI64(0);
            addI64(0);
            addI64(0);
        } else {
            addI32(0);
        }
    }

    public void addF32(Float2 v) {
        addF32(v.x);
        addF32(v.y);
    }

    public void addF32(Float3 v) {
        addF32(v.x);
        addF32(v.y);
        addF32(v.z);
    }

    public void addF32(Float4 v) {
        addF32(v.x);
        addF32(v.y);
        addF32(v.z);
        addF32(v.w);
    }

    public void addF64(Double2 v) {
        addF64(v.x);
        addF64(v.y);
    }

    public void addF64(Double3 v) {
        addF64(v.x);
        addF64(v.y);
        addF64(v.z);
    }

    public void addF64(Double4 v) {
        addF64(v.x);
        addF64(v.y);
        addF64(v.z);
        addF64(v.w);
    }

    public void addI8(Byte2 v) {
        addI8(v.x);
        addI8(v.y);
    }

    public void addI8(Byte3 v) {
        addI8(v.x);
        addI8(v.y);
        addI8(v.z);
    }

    public void addI8(Byte4 v) {
        addI8(v.x);
        addI8(v.y);
        addI8(v.z);
        addI8(v.w);
    }

    public void addU8(Short2 v) {
        addU8(v.x);
        addU8(v.y);
    }

    public void addU8(Short3 v) {
        addU8(v.x);
        addU8(v.y);
        addU8(v.z);
    }

    public void addU8(Short4 v) {
        addU8(v.x);
        addU8(v.y);
        addU8(v.z);
        addU8(v.w);
    }

    public void addI16(Short2 v) {
        addI16(v.x);
        addI16(v.y);
    }

    public void addI16(Short3 v) {
        addI16(v.x);
        addI16(v.y);
        addI16(v.z);
    }

    public void addI16(Short4 v) {
        addI16(v.x);
        addI16(v.y);
        addI16(v.z);
        addI16(v.w);
    }

    public void addU16(Int2 v) {
        addU16(v.x);
        addU16(v.y);
    }

    public void addU16(Int3 v) {
        addU16(v.x);
        addU16(v.y);
        addU16(v.z);
    }

    public void addU16(Int4 v) {
        addU16(v.x);
        addU16(v.y);
        addU16(v.z);
        addU16(v.w);
    }

    public void addI32(Int2 v) {
        addI32(v.x);
        addI32(v.y);
    }

    public void addI32(Int3 v) {
        addI32(v.x);
        addI32(v.y);
        addI32(v.z);
    }

    public void addI32(Int4 v) {
        addI32(v.x);
        addI32(v.y);
        addI32(v.z);
        addI32(v.w);
    }

    public void addU32(Long2 v) {
        addU32(v.x);
        addU32(v.y);
    }

    public void addU32(Long3 v) {
        addU32(v.x);
        addU32(v.y);
        addU32(v.z);
    }

    public void addU32(Long4 v) {
        addU32(v.x);
        addU32(v.y);
        addU32(v.z);
        addU32(v.w);
    }

    public void addI64(Long2 v) {
        addI64(v.x);
        addI64(v.y);
    }

    public void addI64(Long3 v) {
        addI64(v.x);
        addI64(v.y);
        addI64(v.z);
    }

    public void addI64(Long4 v) {
        addI64(v.x);
        addI64(v.y);
        addI64(v.z);
        addI64(v.w);
    }

    public void addU64(Long2 v) {
        addU64(v.x);
        addU64(v.y);
    }

    public void addU64(Long3 v) {
        addU64(v.x);
        addU64(v.y);
        addU64(v.z);
    }

    public void addU64(Long4 v) {
        addU64(v.x);
        addU64(v.y);
        addU64(v.z);
        addU64(v.w);
    }

    public Float2 subFloat2() {
        Float2 v = new Float2();
        v.y = subF32();
        v.x = subF32();
        return v;
    }

    public Float3 subFloat3() {
        Float3 v = new Float3();
        v.z = subF32();
        v.y = subF32();
        v.x = subF32();
        return v;
    }

    public Float4 subFloat4() {
        Float4 v = new Float4();
        v.w = subF32();
        v.z = subF32();
        v.y = subF32();
        v.x = subF32();
        return v;
    }

    public Double2 subDouble2() {
        Double2 v = new Double2();
        v.y = subF64();
        v.x = subF64();
        return v;
    }

    public Double3 subDouble3() {
        Double3 v = new Double3();
        v.z = subF64();
        v.y = subF64();
        v.x = subF64();
        return v;
    }

    public Double4 subDouble4() {
        Double4 v = new Double4();
        v.w = subF64();
        v.z = subF64();
        v.y = subF64();
        v.x = subF64();
        return v;
    }

    public Byte2 subByte2() {
        Byte2 v = new Byte2();
        v.y = subI8();
        v.x = subI8();
        return v;
    }

    public Byte3 subByte3() {
        Byte3 v = new Byte3();
        v.z = subI8();
        v.y = subI8();
        v.x = subI8();
        return v;
    }

    public Byte4 subByte4() {
        Byte4 v = new Byte4();
        v.w = subI8();
        v.z = subI8();
        v.y = subI8();
        v.x = subI8();
        return v;
    }

    public Short2 subShort2() {
        Short2 v = new Short2();
        v.y = subI16();
        v.x = subI16();
        return v;
    }

    public Short3 subShort3() {
        Short3 v = new Short3();
        v.z = subI16();
        v.y = subI16();
        v.x = subI16();
        return v;
    }

    public Short4 subShort4() {
        Short4 v = new Short4();
        v.w = subI16();
        v.z = subI16();
        v.y = subI16();
        v.x = subI16();
        return v;
    }

    public Int2 subInt2() {
        Int2 v = new Int2();
        v.y = subI32();
        v.x = subI32();
        return v;
    }

    public Int3 subInt3() {
        Int3 v = new Int3();
        v.z = subI32();
        v.y = subI32();
        v.x = subI32();
        return v;
    }

    public Int4 subInt4() {
        Int4 v = new Int4();
        v.w = subI32();
        v.z = subI32();
        v.y = subI32();
        v.x = subI32();
        return v;
    }

    public Long2 subLong2() {
        Long2 v = new Long2();
        v.y = subI64();
        v.x = subI64();
        return v;
    }

    public Long3 subLong3() {
        Long3 v = new Long3();
        v.z = subI64();
        v.y = subI64();
        v.x = subI64();
        return v;
    }

    public Long4 subLong4() {
        Long4 v = new Long4();
        v.w = subI64();
        v.z = subI64();
        v.y = subI64();
        v.x = subI64();
        return v;
    }

    public void addMatrix(Matrix4f v) {
        for (float addF32 : v.mMat) {
            addF32(addF32);
        }
    }

    public Matrix4f subMatrix4f() {
        Matrix4f v = new Matrix4f();
        for (int i = v.mMat.length - 1; i >= 0; i--) {
            v.mMat[i] = subF32();
        }
        return v;
    }

    public void addMatrix(Matrix3f v) {
        for (float addF32 : v.mMat) {
            addF32(addF32);
        }
    }

    public Matrix3f subMatrix3f() {
        Matrix3f v = new Matrix3f();
        for (int i = v.mMat.length - 1; i >= 0; i--) {
            v.mMat[i] = subF32();
        }
        return v;
    }

    public void addMatrix(Matrix2f v) {
        for (float addF32 : v.mMat) {
            addF32(addF32);
        }
    }

    public Matrix2f subMatrix2f() {
        Matrix2f v = new Matrix2f();
        for (int i = v.mMat.length - 1; i >= 0; i--) {
            v.mMat[i] = subF32();
        }
        return v;
    }

    public void addBoolean(boolean v) {
        addI8((byte) (v ? 1 : 0));
    }

    public boolean subBoolean() {
        if (subI8() == (byte) 1) {
            return true;
        }
        return false;
    }

    public final byte[] getData() {
        return this.mData;
    }

    public int getPos() {
        return this.mPos;
    }

    private void add(Object obj) {
        if (obj instanceof Boolean) {
            addBoolean(((Boolean) obj).booleanValue());
        } else if (obj instanceof Byte) {
            addI8(((Byte) obj).byteValue());
        } else if (obj instanceof Short) {
            addI16(((Short) obj).shortValue());
        } else if (obj instanceof Integer) {
            addI32(((Integer) obj).intValue());
        } else if (obj instanceof Long) {
            addI64(((Long) obj).longValue());
        } else if (obj instanceof Float) {
            addF32(((Float) obj).floatValue());
        } else if (obj instanceof Double) {
            addF64(((Double) obj).doubleValue());
        } else if (obj instanceof Byte2) {
            addI8((Byte2) obj);
        } else if (obj instanceof Byte3) {
            addI8((Byte3) obj);
        } else if (obj instanceof Byte4) {
            addI8((Byte4) obj);
        } else if (obj instanceof Short2) {
            addI16((Short2) obj);
        } else if (obj instanceof Short3) {
            addI16((Short3) obj);
        } else if (obj instanceof Short4) {
            addI16((Short4) obj);
        } else if (obj instanceof Int2) {
            addI32((Int2) obj);
        } else if (obj instanceof Int3) {
            addI32((Int3) obj);
        } else if (obj instanceof Int4) {
            addI32((Int4) obj);
        } else if (obj instanceof Long2) {
            addI64((Long2) obj);
        } else if (obj instanceof Long3) {
            addI64((Long3) obj);
        } else if (obj instanceof Long4) {
            addI64((Long4) obj);
        } else if (obj instanceof Float2) {
            addF32((Float2) obj);
        } else if (obj instanceof Float3) {
            addF32((Float3) obj);
        } else if (obj instanceof Float4) {
            addF32((Float4) obj);
        } else if (obj instanceof Double2) {
            addF64((Double2) obj);
        } else if (obj instanceof Double3) {
            addF64((Double3) obj);
        } else if (obj instanceof Double4) {
            addF64((Double4) obj);
        } else if (obj instanceof Matrix2f) {
            addMatrix((Matrix2f) obj);
        } else if (obj instanceof Matrix3f) {
            addMatrix((Matrix3f) obj);
        } else if (obj instanceof Matrix4f) {
            addMatrix((Matrix4f) obj);
        } else if (obj instanceof BaseObj) {
            addObj((BaseObj) obj);
        }
    }

    private boolean resize(int newSize) {
        if (newSize == this.mLen) {
            return false;
        }
        byte[] newData = new byte[newSize];
        System.arraycopy(this.mData, 0, newData, 0, this.mPos);
        this.mData = newData;
        this.mLen = newSize;
        return true;
    }

    private void addSafely(Object obj) {
        int oldPos = this.mPos;
        boolean retry;
        do {
            retry = false;
            try {
                add(obj);
                continue;
            } catch (ArrayIndexOutOfBoundsException e) {
                this.mPos = oldPos;
                resize(this.mLen * 2);
                retry = true;
                continue;
            }
        } while (retry);
    }
}
