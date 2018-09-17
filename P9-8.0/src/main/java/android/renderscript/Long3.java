package android.renderscript;

public class Long3 {
    public long x;
    public long y;
    public long z;

    public Long3(long i) {
        this.z = i;
        this.y = i;
        this.x = i;
    }

    public Long3(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Long3(Long3 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
    }

    public void add(Long3 a) {
        this.x += a.x;
        this.y += a.y;
        this.z += a.z;
    }

    public static Long3 add(Long3 a, Long3 b) {
        Long3 result = new Long3();
        result.x = a.x + b.x;
        result.y = a.y + b.y;
        result.z = a.z + b.z;
        return result;
    }

    public void add(long value) {
        this.x += value;
        this.y += value;
        this.z += value;
    }

    public static Long3 add(Long3 a, long b) {
        Long3 result = new Long3();
        result.x = a.x + b;
        result.y = a.y + b;
        result.z = a.z + b;
        return result;
    }

    public void sub(Long3 a) {
        this.x -= a.x;
        this.y -= a.y;
        this.z -= a.z;
    }

    public static Long3 sub(Long3 a, Long3 b) {
        Long3 result = new Long3();
        result.x = a.x - b.x;
        result.y = a.y - b.y;
        result.z = a.z - b.z;
        return result;
    }

    public void sub(long value) {
        this.x -= value;
        this.y -= value;
        this.z -= value;
    }

    public static Long3 sub(Long3 a, long b) {
        Long3 result = new Long3();
        result.x = a.x - b;
        result.y = a.y - b;
        result.z = a.z - b;
        return result;
    }

    public void mul(Long3 a) {
        this.x *= a.x;
        this.y *= a.y;
        this.z *= a.z;
    }

    public static Long3 mul(Long3 a, Long3 b) {
        Long3 result = new Long3();
        result.x = a.x * b.x;
        result.y = a.y * b.y;
        result.z = a.z * b.z;
        return result;
    }

    public void mul(long value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
    }

    public static Long3 mul(Long3 a, long b) {
        Long3 result = new Long3();
        result.x = a.x * b;
        result.y = a.y * b;
        result.z = a.z * b;
        return result;
    }

    public void div(Long3 a) {
        this.x /= a.x;
        this.y /= a.y;
        this.z /= a.z;
    }

    public static Long3 div(Long3 a, Long3 b) {
        Long3 result = new Long3();
        result.x = a.x / b.x;
        result.y = a.y / b.y;
        result.z = a.z / b.z;
        return result;
    }

    public void div(long value) {
        this.x /= value;
        this.y /= value;
        this.z /= value;
    }

    public static Long3 div(Long3 a, long b) {
        Long3 result = new Long3();
        result.x = a.x / b;
        result.y = a.y / b;
        result.z = a.z / b;
        return result;
    }

    public void mod(Long3 a) {
        this.x %= a.x;
        this.y %= a.y;
        this.z %= a.z;
    }

    public static Long3 mod(Long3 a, Long3 b) {
        Long3 result = new Long3();
        result.x = a.x % b.x;
        result.y = a.y % b.y;
        result.z = a.z % b.z;
        return result;
    }

    public void mod(long value) {
        this.x %= value;
        this.y %= value;
        this.z %= value;
    }

    public static Long3 mod(Long3 a, long b) {
        Long3 result = new Long3();
        result.x = a.x % b;
        result.y = a.y % b;
        result.z = a.z % b;
        return result;
    }

    public long length() {
        return 3;
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
    }

    public long dotProduct(Long3 a) {
        return ((this.x * a.x) + (this.y * a.y)) + (this.z * a.z);
    }

    public static long dotProduct(Long3 a, Long3 b) {
        return ((b.x * a.x) + (b.y * a.y)) + (b.z * a.z);
    }

    public void addMultiple(Long3 a, long factor) {
        this.x += a.x * factor;
        this.y += a.y * factor;
        this.z += a.z * factor;
    }

    public void set(Long3 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
    }

    public void setValues(long a, long b, long c) {
        this.x = a;
        this.y = b;
        this.z = c;
    }

    public long elementSum() {
        return (this.x + this.y) + this.z;
    }

    public long get(int i) {
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

    public void setAt(int i, long value) {
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

    public void addAt(int i, long value) {
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

    public void copyTo(long[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
    }
}
