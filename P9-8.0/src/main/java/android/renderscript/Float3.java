package android.renderscript;

public class Float3 {
    public float x;
    public float y;
    public float z;

    public Float3(Float3 data) {
        this.x = data.x;
        this.y = data.y;
        this.z = data.z;
    }

    public Float3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Float3 add(Float3 a, Float3 b) {
        Float3 res = new Float3();
        res.x = a.x + b.x;
        res.y = a.y + b.y;
        res.z = a.z + b.z;
        return res;
    }

    public void add(Float3 value) {
        this.x += value.x;
        this.y += value.y;
        this.z += value.z;
    }

    public void add(float value) {
        this.x += value;
        this.y += value;
        this.z += value;
    }

    public static Float3 add(Float3 a, float b) {
        Float3 res = new Float3();
        res.x = a.x + b;
        res.y = a.y + b;
        res.z = a.z + b;
        return res;
    }

    public void sub(Float3 value) {
        this.x -= value.x;
        this.y -= value.y;
        this.z -= value.z;
    }

    public static Float3 sub(Float3 a, Float3 b) {
        Float3 res = new Float3();
        res.x = a.x - b.x;
        res.y = a.y - b.y;
        res.z = a.z - b.z;
        return res;
    }

    public void sub(float value) {
        this.x -= value;
        this.y -= value;
        this.z -= value;
    }

    public static Float3 sub(Float3 a, float b) {
        Float3 res = new Float3();
        res.x = a.x - b;
        res.y = a.y - b;
        res.z = a.z - b;
        return res;
    }

    public void mul(Float3 value) {
        this.x *= value.x;
        this.y *= value.y;
        this.z *= value.z;
    }

    public static Float3 mul(Float3 a, Float3 b) {
        Float3 res = new Float3();
        res.x = a.x * b.x;
        res.y = a.y * b.y;
        res.z = a.z * b.z;
        return res;
    }

    public void mul(float value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
    }

    public static Float3 mul(Float3 a, float b) {
        Float3 res = new Float3();
        res.x = a.x * b;
        res.y = a.y * b;
        res.z = a.z * b;
        return res;
    }

    public void div(Float3 value) {
        this.x /= value.x;
        this.y /= value.y;
        this.z /= value.z;
    }

    public static Float3 div(Float3 a, Float3 b) {
        Float3 res = new Float3();
        res.x = a.x / b.x;
        res.y = a.y / b.y;
        res.z = a.z / b.z;
        return res;
    }

    public void div(float value) {
        this.x /= value;
        this.y /= value;
        this.z /= value;
    }

    public static Float3 div(Float3 a, float b) {
        Float3 res = new Float3();
        res.x = a.x / b;
        res.y = a.y / b;
        res.z = a.z / b;
        return res;
    }

    public Float dotProduct(Float3 a) {
        return new Float(((this.x * a.x) + (this.y * a.y)) + (this.z * a.z));
    }

    public static Float dotProduct(Float3 a, Float3 b) {
        return new Float(((b.x * a.x) + (b.y * a.y)) + (b.z * a.z));
    }

    public void addMultiple(Float3 a, float factor) {
        this.x += a.x * factor;
        this.y += a.y * factor;
        this.z += a.z * factor;
    }

    public void set(Float3 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
    }

    public int length() {
        return 3;
    }

    public Float elementSum() {
        return new Float((this.x + this.y) + this.z);
    }

    public float get(int i) {
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
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void setValues(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void copyTo(float[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
    }
}
