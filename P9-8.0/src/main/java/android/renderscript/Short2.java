package android.renderscript;

public class Short2 {
    public short x;
    public short y;

    public Short2(short i) {
        this.y = i;
        this.x = i;
    }

    public Short2(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public Short2(Short2 source) {
        this.x = source.x;
        this.y = source.y;
    }

    public void add(Short2 a) {
        this.x = (short) (this.x + a.x);
        this.y = (short) (this.y + a.y);
    }

    public static Short2 add(Short2 a, Short2 b) {
        Short2 result = new Short2();
        result.x = (short) (a.x + b.x);
        result.y = (short) (a.y + b.y);
        return result;
    }

    public void add(short value) {
        this.x = (short) (this.x + value);
        this.y = (short) (this.y + value);
    }

    public static Short2 add(Short2 a, short b) {
        Short2 result = new Short2();
        result.x = (short) (a.x + b);
        result.y = (short) (a.y + b);
        return result;
    }

    public void sub(Short2 a) {
        this.x = (short) (this.x - a.x);
        this.y = (short) (this.y - a.y);
    }

    public static Short2 sub(Short2 a, Short2 b) {
        Short2 result = new Short2();
        result.x = (short) (a.x - b.x);
        result.y = (short) (a.y - b.y);
        return result;
    }

    public void sub(short value) {
        this.x = (short) (this.x - value);
        this.y = (short) (this.y - value);
    }

    public static Short2 sub(Short2 a, short b) {
        Short2 result = new Short2();
        result.x = (short) (a.x - b);
        result.y = (short) (a.y - b);
        return result;
    }

    public void mul(Short2 a) {
        this.x = (short) (this.x * a.x);
        this.y = (short) (this.y * a.y);
    }

    public static Short2 mul(Short2 a, Short2 b) {
        Short2 result = new Short2();
        result.x = (short) (a.x * b.x);
        result.y = (short) (a.y * b.y);
        return result;
    }

    public void mul(short value) {
        this.x = (short) (this.x * value);
        this.y = (short) (this.y * value);
    }

    public static Short2 mul(Short2 a, short b) {
        Short2 result = new Short2();
        result.x = (short) (a.x * b);
        result.y = (short) (a.y * b);
        return result;
    }

    public void div(Short2 a) {
        this.x = (short) (this.x / a.x);
        this.y = (short) (this.y / a.y);
    }

    public static Short2 div(Short2 a, Short2 b) {
        Short2 result = new Short2();
        result.x = (short) (a.x / b.x);
        result.y = (short) (a.y / b.y);
        return result;
    }

    public void div(short value) {
        this.x = (short) (this.x / value);
        this.y = (short) (this.y / value);
    }

    public static Short2 div(Short2 a, short b) {
        Short2 result = new Short2();
        result.x = (short) (a.x / b);
        result.y = (short) (a.y / b);
        return result;
    }

    public void mod(Short2 a) {
        this.x = (short) (this.x % a.x);
        this.y = (short) (this.y % a.y);
    }

    public static Short2 mod(Short2 a, Short2 b) {
        Short2 result = new Short2();
        result.x = (short) (a.x % b.x);
        result.y = (short) (a.y % b.y);
        return result;
    }

    public void mod(short value) {
        this.x = (short) (this.x % value);
        this.y = (short) (this.y % value);
    }

    public static Short2 mod(Short2 a, short b) {
        Short2 result = new Short2();
        result.x = (short) (a.x % b);
        result.y = (short) (a.y % b);
        return result;
    }

    public short length() {
        return (short) 2;
    }

    public void negate() {
        this.x = (short) (-this.x);
        this.y = (short) (-this.y);
    }

    public short dotProduct(Short2 a) {
        return (short) ((this.x * a.x) + (this.y * a.y));
    }

    public static short dotProduct(Short2 a, Short2 b) {
        return (short) ((b.x * a.x) + (b.y * a.y));
    }

    public void addMultiple(Short2 a, short factor) {
        this.x = (short) (this.x + (a.x * factor));
        this.y = (short) (this.y + (a.y * factor));
    }

    public void set(Short2 a) {
        this.x = a.x;
        this.y = a.y;
    }

    public void setValues(short a, short b) {
        this.x = a;
        this.y = b;
    }

    public short elementSum() {
        return (short) (this.x + this.y);
    }

    public short get(int i) {
        switch (i) {
            case 0:
                return this.x;
            case 1:
                return this.y;
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void setAt(int i, short value) {
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

    public void addAt(int i, short value) {
        switch (i) {
            case 0:
                this.x = (short) (this.x + value);
                return;
            case 1:
                this.y = (short) (this.y + value);
                return;
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void copyTo(short[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
    }
}
