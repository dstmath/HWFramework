package android.renderscript;

public class Byte2 {
    public byte x;
    public byte y;

    public Byte2(byte initX, byte initY) {
        this.x = initX;
        this.y = initY;
    }

    public Byte2(Byte2 source) {
        this.x = source.x;
        this.y = source.y;
    }

    public void add(Byte2 a) {
        this.x = (byte) (this.x + a.x);
        this.y = (byte) (this.y + a.y);
    }

    public static Byte2 add(Byte2 a, Byte2 b) {
        Byte2 result = new Byte2();
        result.x = (byte) (a.x + b.x);
        result.y = (byte) (a.y + b.y);
        return result;
    }

    public void add(byte value) {
        this.x = (byte) (this.x + value);
        this.y = (byte) (this.y + value);
    }

    public static Byte2 add(Byte2 a, byte b) {
        Byte2 result = new Byte2();
        result.x = (byte) (a.x + b);
        result.y = (byte) (a.y + b);
        return result;
    }

    public void sub(Byte2 a) {
        this.x = (byte) (this.x - a.x);
        this.y = (byte) (this.y - a.y);
    }

    public static Byte2 sub(Byte2 a, Byte2 b) {
        Byte2 result = new Byte2();
        result.x = (byte) (a.x - b.x);
        result.y = (byte) (a.y - b.y);
        return result;
    }

    public void sub(byte value) {
        this.x = (byte) (this.x - value);
        this.y = (byte) (this.y - value);
    }

    public static Byte2 sub(Byte2 a, byte b) {
        Byte2 result = new Byte2();
        result.x = (byte) (a.x - b);
        result.y = (byte) (a.y - b);
        return result;
    }

    public void mul(Byte2 a) {
        this.x = (byte) (this.x * a.x);
        this.y = (byte) (this.y * a.y);
    }

    public static Byte2 mul(Byte2 a, Byte2 b) {
        Byte2 result = new Byte2();
        result.x = (byte) (a.x * b.x);
        result.y = (byte) (a.y * b.y);
        return result;
    }

    public void mul(byte value) {
        this.x = (byte) (this.x * value);
        this.y = (byte) (this.y * value);
    }

    public static Byte2 mul(Byte2 a, byte b) {
        Byte2 result = new Byte2();
        result.x = (byte) (a.x * b);
        result.y = (byte) (a.y * b);
        return result;
    }

    public void div(Byte2 a) {
        this.x = (byte) (this.x / a.x);
        this.y = (byte) (this.y / a.y);
    }

    public static Byte2 div(Byte2 a, Byte2 b) {
        Byte2 result = new Byte2();
        result.x = (byte) (a.x / b.x);
        result.y = (byte) (a.y / b.y);
        return result;
    }

    public void div(byte value) {
        this.x = (byte) (this.x / value);
        this.y = (byte) (this.y / value);
    }

    public static Byte2 div(Byte2 a, byte b) {
        Byte2 result = new Byte2();
        result.x = (byte) (a.x / b);
        result.y = (byte) (a.y / b);
        return result;
    }

    public byte length() {
        return (byte) 2;
    }

    public void negate() {
        this.x = (byte) (-this.x);
        this.y = (byte) (-this.y);
    }

    public byte dotProduct(Byte2 a) {
        return (byte) ((this.x * a.x) + (this.y * a.y));
    }

    public static byte dotProduct(Byte2 a, Byte2 b) {
        return (byte) ((b.x * a.x) + (b.y * a.y));
    }

    public void addMultiple(Byte2 a, byte factor) {
        this.x = (byte) (this.x + (a.x * factor));
        this.y = (byte) (this.y + (a.y * factor));
    }

    public void set(Byte2 a) {
        this.x = a.x;
        this.y = a.y;
    }

    public void setValues(byte a, byte b) {
        this.x = a;
        this.y = b;
    }

    public byte elementSum() {
        return (byte) (this.x + this.y);
    }

    public byte get(int i) {
        switch (i) {
            case 0:
                return this.x;
            case 1:
                return this.y;
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
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void copyTo(byte[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
    }
}
