package android.renderscript;

public class Short4 {
    public short w;
    public short x;
    public short y;
    public short z;

    public Short4(short i) {
        this.w = i;
        this.z = i;
        this.y = i;
        this.x = i;
    }

    public Short4(short x, short y, short z, short w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Short4(Short4 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
        this.w = source.w;
    }

    public void add(Short4 a) {
        this.x = (short) (this.x + a.x);
        this.y = (short) (this.y + a.y);
        this.z = (short) (this.z + a.z);
        this.w = (short) (this.w + a.w);
    }

    public static Short4 add(Short4 a, Short4 b) {
        Short4 result = new Short4();
        result.x = (short) (a.x + b.x);
        result.y = (short) (a.y + b.y);
        result.z = (short) (a.z + b.z);
        result.w = (short) (a.w + b.w);
        return result;
    }

    public void add(short value) {
        this.x = (short) (this.x + value);
        this.y = (short) (this.y + value);
        this.z = (short) (this.z + value);
        this.w = (short) (this.w + value);
    }

    public static Short4 add(Short4 a, short b) {
        Short4 result = new Short4();
        result.x = (short) (a.x + b);
        result.y = (short) (a.y + b);
        result.z = (short) (a.z + b);
        result.w = (short) (a.w + b);
        return result;
    }

    public void sub(Short4 a) {
        this.x = (short) (this.x - a.x);
        this.y = (short) (this.y - a.y);
        this.z = (short) (this.z - a.z);
        this.w = (short) (this.w - a.w);
    }

    public static Short4 sub(Short4 a, Short4 b) {
        Short4 result = new Short4();
        result.x = (short) (a.x - b.x);
        result.y = (short) (a.y - b.y);
        result.z = (short) (a.z - b.z);
        result.w = (short) (a.w - b.w);
        return result;
    }

    public void sub(short value) {
        this.x = (short) (this.x - value);
        this.y = (short) (this.y - value);
        this.z = (short) (this.z - value);
        this.w = (short) (this.w - value);
    }

    public static Short4 sub(Short4 a, short b) {
        Short4 result = new Short4();
        result.x = (short) (a.x - b);
        result.y = (short) (a.y - b);
        result.z = (short) (a.z - b);
        result.w = (short) (a.w - b);
        return result;
    }

    public void mul(Short4 a) {
        this.x = (short) (this.x * a.x);
        this.y = (short) (this.y * a.y);
        this.z = (short) (this.z * a.z);
        this.w = (short) (this.w * a.w);
    }

    public static Short4 mul(Short4 a, Short4 b) {
        Short4 result = new Short4();
        result.x = (short) (a.x * b.x);
        result.y = (short) (a.y * b.y);
        result.z = (short) (a.z * b.z);
        result.w = (short) (a.w * b.w);
        return result;
    }

    public void mul(short value) {
        this.x = (short) (this.x * value);
        this.y = (short) (this.y * value);
        this.z = (short) (this.z * value);
        this.w = (short) (this.w * value);
    }

    public static Short4 mul(Short4 a, short b) {
        Short4 result = new Short4();
        result.x = (short) (a.x * b);
        result.y = (short) (a.y * b);
        result.z = (short) (a.z * b);
        result.w = (short) (a.w * b);
        return result;
    }

    public void div(Short4 a) {
        this.x = (short) (this.x / a.x);
        this.y = (short) (this.y / a.y);
        this.z = (short) (this.z / a.z);
        this.w = (short) (this.w / a.w);
    }

    public static Short4 div(Short4 a, Short4 b) {
        Short4 result = new Short4();
        result.x = (short) (a.x / b.x);
        result.y = (short) (a.y / b.y);
        result.z = (short) (a.z / b.z);
        result.w = (short) (a.w / b.w);
        return result;
    }

    public void div(short value) {
        this.x = (short) (this.x / value);
        this.y = (short) (this.y / value);
        this.z = (short) (this.z / value);
        this.w = (short) (this.w / value);
    }

    public static Short4 div(Short4 a, short b) {
        Short4 result = new Short4();
        result.x = (short) (a.x / b);
        result.y = (short) (a.y / b);
        result.z = (short) (a.z / b);
        result.w = (short) (a.w / b);
        return result;
    }

    public void mod(Short4 a) {
        this.x = (short) (this.x % a.x);
        this.y = (short) (this.y % a.y);
        this.z = (short) (this.z % a.z);
        this.w = (short) (this.w % a.w);
    }

    public static Short4 mod(Short4 a, Short4 b) {
        Short4 result = new Short4();
        result.x = (short) (a.x % b.x);
        result.y = (short) (a.y % b.y);
        result.z = (short) (a.z % b.z);
        result.w = (short) (a.w % b.w);
        return result;
    }

    public void mod(short value) {
        this.x = (short) (this.x % value);
        this.y = (short) (this.y % value);
        this.z = (short) (this.z % value);
        this.w = (short) (this.w % value);
    }

    public static Short4 mod(Short4 a, short b) {
        Short4 result = new Short4();
        result.x = (short) (a.x % b);
        result.y = (short) (a.y % b);
        result.z = (short) (a.z % b);
        result.w = (short) (a.w % b);
        return result;
    }

    public short length() {
        return (short) 4;
    }

    public void negate() {
        this.x = (short) (-this.x);
        this.y = (short) (-this.y);
        this.z = (short) (-this.z);
        this.w = (short) (-this.w);
    }

    public short dotProduct(Short4 a) {
        return (short) ((((this.x * a.x) + (this.y * a.y)) + (this.z * a.z)) + (this.w * a.w));
    }

    public static short dotProduct(Short4 a, Short4 b) {
        return (short) ((((b.x * a.x) + (b.y * a.y)) + (b.z * a.z)) + (b.w * a.w));
    }

    public void addMultiple(Short4 a, short factor) {
        this.x = (short) (this.x + (a.x * factor));
        this.y = (short) (this.y + (a.y * factor));
        this.z = (short) (this.z + (a.z * factor));
        this.w = (short) (this.w + (a.w * factor));
    }

    public void set(Short4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.w = a.w;
    }

    public void setValues(short a, short b, short c, short d) {
        this.x = a;
        this.y = b;
        this.z = c;
        this.w = d;
    }

    public short elementSum() {
        return (short) (((this.x + this.y) + this.z) + this.w);
    }

    public short get(int i) {
        switch (i) {
            case 0:
                return this.x;
            case 1:
                return this.y;
            case 2:
                return this.z;
            case 3:
                return this.w;
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
            case 2:
                this.z = value;
                return;
            case 3:
                this.w = value;
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
            case 2:
                this.z = (short) (this.z + value);
                return;
            case 3:
                this.w = (short) (this.w + value);
                return;
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void copyTo(short[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
        data[offset + 3] = this.w;
    }
}
