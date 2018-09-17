package android.renderscript;

public class Int3 {
    public int x;
    public int y;
    public int z;

    public Int3(int i) {
        this.z = i;
        this.y = i;
        this.x = i;
    }

    public Int3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Int3(Int3 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
    }

    public void add(Int3 a) {
        this.x += a.x;
        this.y += a.y;
        this.z += a.z;
    }

    public static Int3 add(Int3 a, Int3 b) {
        Int3 result = new Int3();
        result.x = a.x + b.x;
        result.y = a.y + b.y;
        result.z = a.z + b.z;
        return result;
    }

    public void add(int value) {
        this.x += value;
        this.y += value;
        this.z += value;
    }

    public static Int3 add(Int3 a, int b) {
        Int3 result = new Int3();
        result.x = a.x + b;
        result.y = a.y + b;
        result.z = a.z + b;
        return result;
    }

    public void sub(Int3 a) {
        this.x -= a.x;
        this.y -= a.y;
        this.z -= a.z;
    }

    public static Int3 sub(Int3 a, Int3 b) {
        Int3 result = new Int3();
        result.x = a.x - b.x;
        result.y = a.y - b.y;
        result.z = a.z - b.z;
        return result;
    }

    public void sub(int value) {
        this.x -= value;
        this.y -= value;
        this.z -= value;
    }

    public static Int3 sub(Int3 a, int b) {
        Int3 result = new Int3();
        result.x = a.x - b;
        result.y = a.y - b;
        result.z = a.z - b;
        return result;
    }

    public void mul(Int3 a) {
        this.x *= a.x;
        this.y *= a.y;
        this.z *= a.z;
    }

    public static Int3 mul(Int3 a, Int3 b) {
        Int3 result = new Int3();
        result.x = a.x * b.x;
        result.y = a.y * b.y;
        result.z = a.z * b.z;
        return result;
    }

    public void mul(int value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
    }

    public static Int3 mul(Int3 a, int b) {
        Int3 result = new Int3();
        result.x = a.x * b;
        result.y = a.y * b;
        result.z = a.z * b;
        return result;
    }

    public void div(Int3 a) {
        this.x /= a.x;
        this.y /= a.y;
        this.z /= a.z;
    }

    public static Int3 div(Int3 a, Int3 b) {
        Int3 result = new Int3();
        result.x = a.x / b.x;
        result.y = a.y / b.y;
        result.z = a.z / b.z;
        return result;
    }

    public void div(int value) {
        this.x /= value;
        this.y /= value;
        this.z /= value;
    }

    public static Int3 div(Int3 a, int b) {
        Int3 result = new Int3();
        result.x = a.x / b;
        result.y = a.y / b;
        result.z = a.z / b;
        return result;
    }

    public void mod(Int3 a) {
        this.x %= a.x;
        this.y %= a.y;
        this.z %= a.z;
    }

    public static Int3 mod(Int3 a, Int3 b) {
        Int3 result = new Int3();
        result.x = a.x % b.x;
        result.y = a.y % b.y;
        result.z = a.z % b.z;
        return result;
    }

    public void mod(int value) {
        this.x %= value;
        this.y %= value;
        this.z %= value;
    }

    public static Int3 mod(Int3 a, int b) {
        Int3 result = new Int3();
        result.x = a.x % b;
        result.y = a.y % b;
        result.z = a.z % b;
        return result;
    }

    public int length() {
        return 3;
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
    }

    public int dotProduct(Int3 a) {
        return ((this.x * a.x) + (this.y * a.y)) + (this.z * a.z);
    }

    public static int dotProduct(Int3 a, Int3 b) {
        return ((b.x * a.x) + (b.y * a.y)) + (b.z * a.z);
    }

    public void addMultiple(Int3 a, int factor) {
        this.x += a.x * factor;
        this.y += a.y * factor;
        this.z += a.z * factor;
    }

    public void set(Int3 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
    }

    public void setValues(int a, int b, int c) {
        this.x = a;
        this.y = b;
        this.z = c;
    }

    public int elementSum() {
        return (this.x + this.y) + this.z;
    }

    public int get(int i) {
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
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void copyTo(int[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
    }
}
