package android.renderscript;

public class Byte3 {
    public byte x;
    public byte y;
    public byte z;

    public Byte3(byte initX, byte initY, byte initZ) {
        this.x = initX;
        this.y = initY;
        this.z = initZ;
    }

    public Byte3(Byte3 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
    }

    public void add(Byte3 a) {
        this.x = (byte) (this.x + a.x);
        this.y = (byte) (this.y + a.y);
        this.z = (byte) (this.z + a.z);
    }

    public static Byte3 add(Byte3 a, Byte3 b) {
        Byte3 result = new Byte3();
        result.x = (byte) (a.x + b.x);
        result.y = (byte) (a.y + b.y);
        result.z = (byte) (a.z + b.z);
        return result;
    }

    public void add(byte value) {
        this.x = (byte) (this.x + value);
        this.y = (byte) (this.y + value);
        this.z = (byte) (this.z + value);
    }

    public static Byte3 add(Byte3 a, byte b) {
        Byte3 result = new Byte3();
        result.x = (byte) (a.x + b);
        result.y = (byte) (a.y + b);
        result.z = (byte) (a.z + b);
        return result;
    }

    public void sub(Byte3 a) {
        this.x = (byte) (this.x - a.x);
        this.y = (byte) (this.y - a.y);
        this.z = (byte) (this.z - a.z);
    }

    public static Byte3 sub(Byte3 a, Byte3 b) {
        Byte3 result = new Byte3();
        result.x = (byte) (a.x - b.x);
        result.y = (byte) (a.y - b.y);
        result.z = (byte) (a.z - b.z);
        return result;
    }

    public void sub(byte value) {
        this.x = (byte) (this.x - value);
        this.y = (byte) (this.y - value);
        this.z = (byte) (this.z - value);
    }

    public static Byte3 sub(Byte3 a, byte b) {
        Byte3 result = new Byte3();
        result.x = (byte) (a.x - b);
        result.y = (byte) (a.y - b);
        result.z = (byte) (a.z - b);
        return result;
    }

    public void mul(Byte3 a) {
        this.x = (byte) (this.x * a.x);
        this.y = (byte) (this.y * a.y);
        this.z = (byte) (this.z * a.z);
    }

    public static Byte3 mul(Byte3 a, Byte3 b) {
        Byte3 result = new Byte3();
        result.x = (byte) (a.x * b.x);
        result.y = (byte) (a.y * b.y);
        result.z = (byte) (a.z * b.z);
        return result;
    }

    public void mul(byte value) {
        this.x = (byte) (this.x * value);
        this.y = (byte) (this.y * value);
        this.z = (byte) (this.z * value);
    }

    public static Byte3 mul(Byte3 a, byte b) {
        Byte3 result = new Byte3();
        result.x = (byte) (a.x * b);
        result.y = (byte) (a.y * b);
        result.z = (byte) (a.z * b);
        return result;
    }

    public void div(Byte3 a) {
        this.x = (byte) (this.x / a.x);
        this.y = (byte) (this.y / a.y);
        this.z = (byte) (this.z / a.z);
    }

    public static Byte3 div(Byte3 a, Byte3 b) {
        Byte3 result = new Byte3();
        result.x = (byte) (a.x / b.x);
        result.y = (byte) (a.y / b.y);
        result.z = (byte) (a.z / b.z);
        return result;
    }

    public void div(byte value) {
        this.x = (byte) (this.x / value);
        this.y = (byte) (this.y / value);
        this.z = (byte) (this.z / value);
    }

    public static Byte3 div(Byte3 a, byte b) {
        Byte3 result = new Byte3();
        result.x = (byte) (a.x / b);
        result.y = (byte) (a.y / b);
        result.z = (byte) (a.z / b);
        return result;
    }

    public byte length() {
        return (byte) 3;
    }

    public void negate() {
        this.x = (byte) (-this.x);
        this.y = (byte) (-this.y);
        this.z = (byte) (-this.z);
    }

    public byte dotProduct(Byte3 a) {
        return (byte) (((byte) (((byte) (this.x * a.x)) + ((byte) (this.y * a.y)))) + ((byte) (this.z * a.z)));
    }

    public static byte dotProduct(Byte3 a, Byte3 b) {
        return (byte) (((byte) (((byte) (b.x * a.x)) + ((byte) (b.y * a.y)))) + ((byte) (b.z * a.z)));
    }

    public void addMultiple(Byte3 a, byte factor) {
        this.x = (byte) (this.x + (a.x * factor));
        this.y = (byte) (this.y + (a.y * factor));
        this.z = (byte) (this.z + (a.z * factor));
    }

    public void set(Byte3 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
    }

    public void setValues(byte a, byte b, byte c) {
        this.x = a;
        this.y = b;
        this.z = c;
    }

    public byte elementSum() {
        return (byte) ((this.x + this.y) + this.z);
    }

    public byte get(int i) {
        switch (i) {
            case 0:
                return this.x;
            case 1:
                return this.y;
            case 2:
                return this.z;
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void setAt(int i, byte value) {
        switch (i) {
            case 0:
                this.x = value;
                return;
            case 1:
                this.y = value;
                return;
            case 2:
                this.z = value;
                return;
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void addAt(int i, byte value) {
        switch (i) {
            case 0:
                this.x = (byte) (this.x + value);
                return;
            case 1:
                this.y = (byte) (this.y + value);
                return;
            case 2:
                this.z = (byte) (this.z + value);
                return;
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void copyTo(byte[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
    }
}
