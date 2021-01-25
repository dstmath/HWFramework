package android.renderscript;

public class Byte4 {
    public byte w;
    public byte x;
    public byte y;
    public byte z;

    public Byte4() {
    }

    public Byte4(byte initX, byte initY, byte initZ, byte initW) {
        this.x = initX;
        this.y = initY;
        this.z = initZ;
        this.w = initW;
    }

    public Byte4(Byte4 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
        this.w = source.w;
    }

    public void add(Byte4 a) {
        this.x = (byte) (this.x + a.x);
        this.y = (byte) (this.y + a.y);
        this.z = (byte) (this.z + a.z);
        this.w = (byte) (this.w + a.w);
    }

    public static Byte4 add(Byte4 a, Byte4 b) {
        Byte4 result = new Byte4();
        result.x = (byte) (a.x + b.x);
        result.y = (byte) (a.y + b.y);
        result.z = (byte) (a.z + b.z);
        result.w = (byte) (a.w + b.w);
        return result;
    }

    public void add(byte value) {
        this.x = (byte) (this.x + value);
        this.y = (byte) (this.y + value);
        this.z = (byte) (this.z + value);
        this.w = (byte) (this.w + value);
    }

    public static Byte4 add(Byte4 a, byte b) {
        Byte4 result = new Byte4();
        result.x = (byte) (a.x + b);
        result.y = (byte) (a.y + b);
        result.z = (byte) (a.z + b);
        result.w = (byte) (a.w + b);
        return result;
    }

    public void sub(Byte4 a) {
        this.x = (byte) (this.x - a.x);
        this.y = (byte) (this.y - a.y);
        this.z = (byte) (this.z - a.z);
        this.w = (byte) (this.w - a.w);
    }

    public static Byte4 sub(Byte4 a, Byte4 b) {
        Byte4 result = new Byte4();
        result.x = (byte) (a.x - b.x);
        result.y = (byte) (a.y - b.y);
        result.z = (byte) (a.z - b.z);
        result.w = (byte) (a.w - b.w);
        return result;
    }

    public void sub(byte value) {
        this.x = (byte) (this.x - value);
        this.y = (byte) (this.y - value);
        this.z = (byte) (this.z - value);
        this.w = (byte) (this.w - value);
    }

    public static Byte4 sub(Byte4 a, byte b) {
        Byte4 result = new Byte4();
        result.x = (byte) (a.x - b);
        result.y = (byte) (a.y - b);
        result.z = (byte) (a.z - b);
        result.w = (byte) (a.w - b);
        return result;
    }

    public void mul(Byte4 a) {
        this.x = (byte) (this.x * a.x);
        this.y = (byte) (this.y * a.y);
        this.z = (byte) (this.z * a.z);
        this.w = (byte) (this.w * a.w);
    }

    public static Byte4 mul(Byte4 a, Byte4 b) {
        Byte4 result = new Byte4();
        result.x = (byte) (a.x * b.x);
        result.y = (byte) (a.y * b.y);
        result.z = (byte) (a.z * b.z);
        result.w = (byte) (a.w * b.w);
        return result;
    }

    public void mul(byte value) {
        this.x = (byte) (this.x * value);
        this.y = (byte) (this.y * value);
        this.z = (byte) (this.z * value);
        this.w = (byte) (this.w * value);
    }

    public static Byte4 mul(Byte4 a, byte b) {
        Byte4 result = new Byte4();
        result.x = (byte) (a.x * b);
        result.y = (byte) (a.y * b);
        result.z = (byte) (a.z * b);
        result.w = (byte) (a.w * b);
        return result;
    }

    public void div(Byte4 a) {
        this.x = (byte) (this.x / a.x);
        this.y = (byte) (this.y / a.y);
        this.z = (byte) (this.z / a.z);
        this.w = (byte) (this.w / a.w);
    }

    public static Byte4 div(Byte4 a, Byte4 b) {
        Byte4 result = new Byte4();
        result.x = (byte) (a.x / b.x);
        result.y = (byte) (a.y / b.y);
        result.z = (byte) (a.z / b.z);
        result.w = (byte) (a.w / b.w);
        return result;
    }

    public void div(byte value) {
        this.x = (byte) (this.x / value);
        this.y = (byte) (this.y / value);
        this.z = (byte) (this.z / value);
        this.w = (byte) (this.w / value);
    }

    public static Byte4 div(Byte4 a, byte b) {
        Byte4 result = new Byte4();
        result.x = (byte) (a.x / b);
        result.y = (byte) (a.y / b);
        result.z = (byte) (a.z / b);
        result.w = (byte) (a.w / b);
        return result;
    }

    public byte length() {
        return 4;
    }

    public void negate() {
        this.x = (byte) (-this.x);
        this.y = (byte) (-this.y);
        this.z = (byte) (-this.z);
        this.w = (byte) (-this.w);
    }

    public byte dotProduct(Byte4 a) {
        return (byte) ((this.x * a.x) + (this.y * a.y) + (this.z * a.z) + (this.w * a.w));
    }

    public static byte dotProduct(Byte4 a, Byte4 b) {
        return (byte) ((b.x * a.x) + (b.y * a.y) + (b.z * a.z) + (b.w * a.w));
    }

    public void addMultiple(Byte4 a, byte factor) {
        this.x = (byte) (this.x + (a.x * factor));
        this.y = (byte) (this.y + (a.y * factor));
        this.z = (byte) (this.z + (a.z * factor));
        this.w = (byte) (this.w + (a.w * factor));
    }

    public void set(Byte4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.w = a.w;
    }

    public void setValues(byte a, byte b, byte c, byte d) {
        this.x = a;
        this.y = b;
        this.z = c;
        this.w = d;
    }

    public byte elementSum() {
        return (byte) (this.x + this.y + this.z + this.w);
    }

    public byte get(int i) {
        if (i == 0) {
            return this.x;
        }
        if (i == 1) {
            return this.y;
        }
        if (i == 2) {
            return this.z;
        }
        if (i == 3) {
            return this.w;
        }
        throw new IndexOutOfBoundsException("Index: i");
    }

    public void setAt(int i, byte value) {
        if (i == 0) {
            this.x = value;
        } else if (i == 1) {
            this.y = value;
        } else if (i == 2) {
            this.z = value;
        } else if (i == 3) {
            this.w = value;
        } else {
            throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void addAt(int i, byte value) {
        if (i == 0) {
            this.x = (byte) (this.x + value);
        } else if (i == 1) {
            this.y = (byte) (this.y + value);
        } else if (i == 2) {
            this.z = (byte) (this.z + value);
        } else if (i == 3) {
            this.w = (byte) (this.w + value);
        } else {
            throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void copyTo(byte[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
        data[offset + 3] = this.w;
    }
}
