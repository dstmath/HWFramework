package android.renderscript;

public class Long4 {
    public long w;
    public long x;
    public long y;
    public long z;

    public Long4(long i) {
        this.w = i;
        this.z = i;
        this.y = i;
        this.x = i;
    }

    public Long4(long x, long y, long z, long w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Long4(Long4 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
        this.w = source.w;
    }

    public void add(Long4 a) {
        this.x += a.x;
        this.y += a.y;
        this.z += a.z;
        this.w += a.w;
    }

    public static Long4 add(Long4 a, Long4 b) {
        Long4 result = new Long4();
        result.x = a.x + b.x;
        result.y = a.y + b.y;
        result.z = a.z + b.z;
        result.w = a.w + b.w;
        return result;
    }

    public void add(long value) {
        this.x += value;
        this.y += value;
        this.z += value;
        this.w += value;
    }

    public static Long4 add(Long4 a, long b) {
        Long4 result = new Long4();
        result.x = a.x + b;
        result.y = a.y + b;
        result.z = a.z + b;
        result.w = a.w + b;
        return result;
    }

    public void sub(Long4 a) {
        this.x -= a.x;
        this.y -= a.y;
        this.z -= a.z;
        this.w -= a.w;
    }

    public static Long4 sub(Long4 a, Long4 b) {
        Long4 result = new Long4();
        result.x = a.x - b.x;
        result.y = a.y - b.y;
        result.z = a.z - b.z;
        result.w = a.w - b.w;
        return result;
    }

    public void sub(long value) {
        this.x -= value;
        this.y -= value;
        this.z -= value;
        this.w -= value;
    }

    public static Long4 sub(Long4 a, long b) {
        Long4 result = new Long4();
        result.x = a.x - b;
        result.y = a.y - b;
        result.z = a.z - b;
        result.w = a.w - b;
        return result;
    }

    public void mul(Long4 a) {
        this.x *= a.x;
        this.y *= a.y;
        this.z *= a.z;
        this.w *= a.w;
    }

    public static Long4 mul(Long4 a, Long4 b) {
        Long4 result = new Long4();
        result.x = a.x * b.x;
        result.y = a.y * b.y;
        result.z = a.z * b.z;
        result.w = a.w * b.w;
        return result;
    }

    public void mul(long value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
        this.w *= value;
    }

    public static Long4 mul(Long4 a, long b) {
        Long4 result = new Long4();
        result.x = a.x * b;
        result.y = a.y * b;
        result.z = a.z * b;
        result.w = a.w * b;
        return result;
    }

    public void div(Long4 a) {
        this.x /= a.x;
        this.y /= a.y;
        this.z /= a.z;
        this.w /= a.w;
    }

    public static Long4 div(Long4 a, Long4 b) {
        Long4 result = new Long4();
        result.x = a.x / b.x;
        result.y = a.y / b.y;
        result.z = a.z / b.z;
        result.w = a.w / b.w;
        return result;
    }

    public void div(long value) {
        this.x /= value;
        this.y /= value;
        this.z /= value;
        this.w /= value;
    }

    public static Long4 div(Long4 a, long b) {
        Long4 result = new Long4();
        result.x = a.x / b;
        result.y = a.y / b;
        result.z = a.z / b;
        result.w = a.w / b;
        return result;
    }

    public void mod(Long4 a) {
        this.x %= a.x;
        this.y %= a.y;
        this.z %= a.z;
        this.w %= a.w;
    }

    public static Long4 mod(Long4 a, Long4 b) {
        Long4 result = new Long4();
        result.x = a.x % b.x;
        result.y = a.y % b.y;
        result.z = a.z % b.z;
        result.w = a.w % b.w;
        return result;
    }

    public void mod(long value) {
        this.x %= value;
        this.y %= value;
        this.z %= value;
        this.w %= value;
    }

    public static Long4 mod(Long4 a, long b) {
        Long4 result = new Long4();
        result.x = a.x % b;
        result.y = a.y % b;
        result.z = a.z % b;
        result.w = a.w % b;
        return result;
    }

    public long length() {
        return 4;
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
    }

    public long dotProduct(Long4 a) {
        return (((this.x * a.x) + (this.y * a.y)) + (this.z * a.z)) + (this.w * a.w);
    }

    public static long dotProduct(Long4 a, Long4 b) {
        return (((b.x * a.x) + (b.y * a.y)) + (b.z * a.z)) + (b.w * a.w);
    }

    public void addMultiple(Long4 a, long factor) {
        this.x += a.x * factor;
        this.y += a.y * factor;
        this.z += a.z * factor;
        this.w += a.w * factor;
    }

    public void set(Long4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.w = a.w;
    }

    public void setValues(long a, long b, long c, long d) {
        this.x = a;
        this.y = b;
        this.z = c;
        this.w = d;
    }

    public long elementSum() {
        return ((this.x + this.y) + this.z) + this.w;
    }

    public long get(int i) {
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
            case 3:
                this.w = value;
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
            case 3:
                this.w += value;
                return;
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void copyTo(long[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
        data[offset + 3] = this.w;
    }
}
