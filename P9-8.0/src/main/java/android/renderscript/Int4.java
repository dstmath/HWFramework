package android.renderscript;

public class Int4 {
    public int w;
    public int x;
    public int y;
    public int z;

    public Int4(int i) {
        this.w = i;
        this.z = i;
        this.y = i;
        this.x = i;
    }

    public Int4(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Int4(Int4 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
        this.w = source.w;
    }

    public void add(Int4 a) {
        this.x += a.x;
        this.y += a.y;
        this.z += a.z;
        this.w += a.w;
    }

    public static Int4 add(Int4 a, Int4 b) {
        Int4 result = new Int4();
        result.x = a.x + b.x;
        result.y = a.y + b.y;
        result.z = a.z + b.z;
        result.w = a.w + b.w;
        return result;
    }

    public void add(int value) {
        this.x += value;
        this.y += value;
        this.z += value;
        this.w += value;
    }

    public static Int4 add(Int4 a, int b) {
        Int4 result = new Int4();
        result.x = a.x + b;
        result.y = a.y + b;
        result.z = a.z + b;
        result.w = a.w + b;
        return result;
    }

    public void sub(Int4 a) {
        this.x -= a.x;
        this.y -= a.y;
        this.z -= a.z;
        this.w -= a.w;
    }

    public static Int4 sub(Int4 a, Int4 b) {
        Int4 result = new Int4();
        result.x = a.x - b.x;
        result.y = a.y - b.y;
        result.z = a.z - b.z;
        result.w = a.w - b.w;
        return result;
    }

    public void sub(int value) {
        this.x -= value;
        this.y -= value;
        this.z -= value;
        this.w -= value;
    }

    public static Int4 sub(Int4 a, int b) {
        Int4 result = new Int4();
        result.x = a.x - b;
        result.y = a.y - b;
        result.z = a.z - b;
        result.w = a.w - b;
        return result;
    }

    public void mul(Int4 a) {
        this.x *= a.x;
        this.y *= a.y;
        this.z *= a.z;
        this.w *= a.w;
    }

    public static Int4 mul(Int4 a, Int4 b) {
        Int4 result = new Int4();
        result.x = a.x * b.x;
        result.y = a.y * b.y;
        result.z = a.z * b.z;
        result.w = a.w * b.w;
        return result;
    }

    public void mul(int value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
        this.w *= value;
    }

    public static Int4 mul(Int4 a, int b) {
        Int4 result = new Int4();
        result.x = a.x * b;
        result.y = a.y * b;
        result.z = a.z * b;
        result.w = a.w * b;
        return result;
    }

    public void div(Int4 a) {
        this.x /= a.x;
        this.y /= a.y;
        this.z /= a.z;
        this.w /= a.w;
    }

    public static Int4 div(Int4 a, Int4 b) {
        Int4 result = new Int4();
        result.x = a.x / b.x;
        result.y = a.y / b.y;
        result.z = a.z / b.z;
        result.w = a.w / b.w;
        return result;
    }

    public void div(int value) {
        this.x /= value;
        this.y /= value;
        this.z /= value;
        this.w /= value;
    }

    public static Int4 div(Int4 a, int b) {
        Int4 result = new Int4();
        result.x = a.x / b;
        result.y = a.y / b;
        result.z = a.z / b;
        result.w = a.w / b;
        return result;
    }

    public void mod(Int4 a) {
        this.x %= a.x;
        this.y %= a.y;
        this.z %= a.z;
        this.w %= a.w;
    }

    public static Int4 mod(Int4 a, Int4 b) {
        Int4 result = new Int4();
        result.x = a.x % b.x;
        result.y = a.y % b.y;
        result.z = a.z % b.z;
        result.w = a.w % b.w;
        return result;
    }

    public void mod(int value) {
        this.x %= value;
        this.y %= value;
        this.z %= value;
        this.w %= value;
    }

    public static Int4 mod(Int4 a, int b) {
        Int4 result = new Int4();
        result.x = a.x % b;
        result.y = a.y % b;
        result.z = a.z % b;
        result.w = a.w % b;
        return result;
    }

    public int length() {
        return 4;
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
    }

    public int dotProduct(Int4 a) {
        return (((this.x * a.x) + (this.y * a.y)) + (this.z * a.z)) + (this.w * a.w);
    }

    public static int dotProduct(Int4 a, Int4 b) {
        return (((b.x * a.x) + (b.y * a.y)) + (b.z * a.z)) + (b.w * a.w);
    }

    public void addMultiple(Int4 a, int factor) {
        this.x += a.x * factor;
        this.y += a.y * factor;
        this.z += a.z * factor;
        this.w += a.w * factor;
    }

    public void set(Int4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.w = a.w;
    }

    public void setValues(int a, int b, int c, int d) {
        this.x = a;
        this.y = b;
        this.z = c;
        this.w = d;
    }

    public int elementSum() {
        return ((this.x + this.y) + this.z) + this.w;
    }

    public int get(int i) {
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

    public void setAt(int i, int value) {
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

    public void addAt(int i, int value) {
        switch (i) {
            case 0:
                this.x += value;
                return;
            case 1:
                this.y += value;
                return;
            case 2:
                this.z += value;
                return;
            case 3:
                this.w += value;
                return;
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void copyTo(int[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
        data[offset + 3] = this.w;
    }
}
