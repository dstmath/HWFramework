package android.renderscript;

public class Double4 {
    public double w;
    public double x;
    public double y;
    public double z;

    public Double4(Double4 data) {
        this.x = data.x;
        this.y = data.y;
        this.z = data.z;
        this.w = data.w;
    }

    public Double4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public static Double4 add(Double4 a, Double4 b) {
        Double4 res = new Double4();
        res.x = a.x + b.x;
        res.y = a.y + b.y;
        res.z = a.z + b.z;
        res.w = a.w + b.w;
        return res;
    }

    public void add(Double4 value) {
        this.x += value.x;
        this.y += value.y;
        this.z += value.z;
        this.w += value.w;
    }

    public void add(double value) {
        this.x += value;
        this.y += value;
        this.z += value;
        this.w += value;
    }

    public static Double4 add(Double4 a, double b) {
        Double4 res = new Double4();
        res.x = a.x + b;
        res.y = a.y + b;
        res.z = a.z + b;
        res.w = a.w + b;
        return res;
    }

    public void sub(Double4 value) {
        this.x -= value.x;
        this.y -= value.y;
        this.z -= value.z;
        this.w -= value.w;
    }

    public void sub(double value) {
        this.x -= value;
        this.y -= value;
        this.z -= value;
        this.w -= value;
    }

    public static Double4 sub(Double4 a, double b) {
        Double4 res = new Double4();
        res.x = a.x - b;
        res.y = a.y - b;
        res.z = a.z - b;
        res.w = a.w - b;
        return res;
    }

    public static Double4 sub(Double4 a, Double4 b) {
        Double4 res = new Double4();
        res.x = a.x - b.x;
        res.y = a.y - b.y;
        res.z = a.z - b.z;
        res.w = a.w - b.w;
        return res;
    }

    public void mul(Double4 value) {
        this.x *= value.x;
        this.y *= value.y;
        this.z *= value.z;
        this.w *= value.w;
    }

    public void mul(double value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
        this.w *= value;
    }

    public static Double4 mul(Double4 a, Double4 b) {
        Double4 res = new Double4();
        res.x = a.x * b.x;
        res.y = a.y * b.y;
        res.z = a.z * b.z;
        res.w = a.w * b.w;
        return res;
    }

    public static Double4 mul(Double4 a, double b) {
        Double4 res = new Double4();
        res.x = a.x * b;
        res.y = a.y * b;
        res.z = a.z * b;
        res.w = a.w * b;
        return res;
    }

    public void div(Double4 value) {
        this.x /= value.x;
        this.y /= value.y;
        this.z /= value.z;
        this.w /= value.w;
    }

    public void div(double value) {
        this.x /= value;
        this.y /= value;
        this.z /= value;
        this.w /= value;
    }

    public static Double4 div(Double4 a, double b) {
        Double4 res = new Double4();
        res.x = a.x / b;
        res.y = a.y / b;
        res.z = a.z / b;
        res.w = a.w / b;
        return res;
    }

    public static Double4 div(Double4 a, Double4 b) {
        Double4 res = new Double4();
        res.x = a.x / b.x;
        res.y = a.y / b.y;
        res.z = a.z / b.z;
        res.w = a.w / b.w;
        return res;
    }

    public double dotProduct(Double4 a) {
        return (((this.x * a.x) + (this.y * a.y)) + (this.z * a.z)) + (this.w * a.w);
    }

    public static double dotProduct(Double4 a, Double4 b) {
        return (((b.x * a.x) + (b.y * a.y)) + (b.z * a.z)) + (b.w * a.w);
    }

    public void addMultiple(Double4 a, double factor) {
        this.x += a.x * factor;
        this.y += a.y * factor;
        this.z += a.z * factor;
        this.w += a.w * factor;
    }

    public void set(Double4 a) {
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

    public double elementSum() {
        return ((this.x + this.y) + this.z) + this.w;
    }

    public double get(int i) {
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

    public void setAt(int i, double value) {
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

    public void addAt(int i, double value) {
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

    public void setValues(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public void copyTo(double[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
        data[offset + 3] = this.w;
    }
}
