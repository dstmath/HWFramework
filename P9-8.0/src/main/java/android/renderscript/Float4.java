package android.renderscript;

public class Float4 {
    public float w;
    public float x;
    public float y;
    public float z;

    public Float4(Float4 data) {
        this.x = data.x;
        this.y = data.y;
        this.z = data.z;
        this.w = data.w;
    }

    public Float4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public static Float4 add(Float4 a, Float4 b) {
        Float4 res = new Float4();
        res.x = a.x + b.x;
        res.y = a.y + b.y;
        res.z = a.z + b.z;
        res.w = a.w + b.w;
        return res;
    }

    public void add(Float4 value) {
        this.x += value.x;
        this.y += value.y;
        this.z += value.z;
        this.w += value.w;
    }

    public void add(float value) {
        this.x += value;
        this.y += value;
        this.z += value;
        this.w += value;
    }

    public static Float4 add(Float4 a, float b) {
        Float4 res = new Float4();
        res.x = a.x + b;
        res.y = a.y + b;
        res.z = a.z + b;
        res.w = a.w + b;
        return res;
    }

    public void sub(Float4 value) {
        this.x -= value.x;
        this.y -= value.y;
        this.z -= value.z;
        this.w -= value.w;
    }

    public void sub(float value) {
        this.x -= value;
        this.y -= value;
        this.z -= value;
        this.w -= value;
    }

    public static Float4 sub(Float4 a, float b) {
        Float4 res = new Float4();
        res.x = a.x - b;
        res.y = a.y - b;
        res.z = a.z - b;
        res.w = a.w - b;
        return res;
    }

    public static Float4 sub(Float4 a, Float4 b) {
        Float4 res = new Float4();
        res.x = a.x - b.x;
        res.y = a.y - b.y;
        res.z = a.z - b.z;
        res.w = a.w - b.w;
        return res;
    }

    public void mul(Float4 value) {
        this.x *= value.x;
        this.y *= value.y;
        this.z *= value.z;
        this.w *= value.w;
    }

    public void mul(float value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
        this.w *= value;
    }

    public static Float4 mul(Float4 a, Float4 b) {
        Float4 res = new Float4();
        res.x = a.x * b.x;
        res.y = a.y * b.y;
        res.z = a.z * b.z;
        res.w = a.w * b.w;
        return res;
    }

    public static Float4 mul(Float4 a, float b) {
        Float4 res = new Float4();
        res.x = a.x * b;
        res.y = a.y * b;
        res.z = a.z * b;
        res.w = a.w * b;
        return res;
    }

    public void div(Float4 value) {
        this.x /= value.x;
        this.y /= value.y;
        this.z /= value.z;
        this.w /= value.w;
    }

    public void div(float value) {
        this.x /= value;
        this.y /= value;
        this.z /= value;
        this.w /= value;
    }

    public static Float4 div(Float4 a, float b) {
        Float4 res = new Float4();
        res.x = a.x / b;
        res.y = a.y / b;
        res.z = a.z / b;
        res.w = a.w / b;
        return res;
    }

    public static Float4 div(Float4 a, Float4 b) {
        Float4 res = new Float4();
        res.x = a.x / b.x;
        res.y = a.y / b.y;
        res.z = a.z / b.z;
        res.w = a.w / b.w;
        return res;
    }

    public float dotProduct(Float4 a) {
        return (((this.x * a.x) + (this.y * a.y)) + (this.z * a.z)) + (this.w * a.w);
    }

    public static float dotProduct(Float4 a, Float4 b) {
        return (((b.x * a.x) + (b.y * a.y)) + (b.z * a.z)) + (b.w * a.w);
    }

    public void addMultiple(Float4 a, float factor) {
        this.x += a.x * factor;
        this.y += a.y * factor;
        this.z += a.z * factor;
        this.w += a.w * factor;
    }

    public void set(Float4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.w = a.w;
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
    }

    public int length() {
        return 4;
    }

    public float elementSum() {
        return ((this.x + this.y) + this.z) + this.w;
    }

    public float get(int i) {
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

    public void setAt(int i, float value) {
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

    public void addAt(int i, float value) {
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

    public void setValues(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public void copyTo(float[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
        data[offset + 3] = this.w;
    }
}
