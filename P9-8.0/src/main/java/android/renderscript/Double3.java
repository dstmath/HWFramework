package android.renderscript;

public class Double3 {
    public double x;
    public double y;
    public double z;

    public Double3(Double3 data) {
        this.x = data.x;
        this.y = data.y;
        this.z = data.z;
    }

    public Double3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Double3 add(Double3 a, Double3 b) {
        Double3 res = new Double3();
        res.x = a.x + b.x;
        res.y = a.y + b.y;
        res.z = a.z + b.z;
        return res;
    }

    public void add(Double3 value) {
        this.x += value.x;
        this.y += value.y;
        this.z += value.z;
    }

    public void add(double value) {
        this.x += value;
        this.y += value;
        this.z += value;
    }

    public static Double3 add(Double3 a, double b) {
        Double3 res = new Double3();
        res.x = a.x + b;
        res.y = a.y + b;
        res.z = a.z + b;
        return res;
    }

    public void sub(Double3 value) {
        this.x -= value.x;
        this.y -= value.y;
        this.z -= value.z;
    }

    public static Double3 sub(Double3 a, Double3 b) {
        Double3 res = new Double3();
        res.x = a.x - b.x;
        res.y = a.y - b.y;
        res.z = a.z - b.z;
        return res;
    }

    public void sub(double value) {
        this.x -= value;
        this.y -= value;
        this.z -= value;
    }

    public static Double3 sub(Double3 a, double b) {
        Double3 res = new Double3();
        res.x = a.x - b;
        res.y = a.y - b;
        res.z = a.z - b;
        return res;
    }

    public void mul(Double3 value) {
        this.x *= value.x;
        this.y *= value.y;
        this.z *= value.z;
    }

    public static Double3 mul(Double3 a, Double3 b) {
        Double3 res = new Double3();
        res.x = a.x * b.x;
        res.y = a.y * b.y;
        res.z = a.z * b.z;
        return res;
    }

    public void mul(double value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
    }

    public static Double3 mul(Double3 a, double b) {
        Double3 res = new Double3();
        res.x = a.x * b;
        res.y = a.y * b;
        res.z = a.z * b;
        return res;
    }

    public void div(Double3 value) {
        this.x /= value.x;
        this.y /= value.y;
        this.z /= value.z;
    }

    public static Double3 div(Double3 a, Double3 b) {
        Double3 res = new Double3();
        res.x = a.x / b.x;
        res.y = a.y / b.y;
        res.z = a.z / b.z;
        return res;
    }

    public void div(double value) {
        this.x /= value;
        this.y /= value;
        this.z /= value;
    }

    public static Double3 div(Double3 a, double b) {
        Double3 res = new Double3();
        res.x = a.x / b;
        res.y = a.y / b;
        res.z = a.z / b;
        return res;
    }

    public double dotProduct(Double3 a) {
        return ((this.x * a.x) + (this.y * a.y)) + (this.z * a.z);
    }

    public static double dotProduct(Double3 a, Double3 b) {
        return ((b.x * a.x) + (b.y * a.y)) + (b.z * a.z);
    }

    public void addMultiple(Double3 a, double factor) {
        this.x += a.x * factor;
        this.y += a.y * factor;
        this.z += a.z * factor;
    }

    public void set(Double3 a) {
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

    public double elementSum() {
        return (this.x + this.y) + this.z;
    }

    public double get(int i) {
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
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void setValues(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void copyTo(double[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
        data[offset + 2] = this.z;
    }
}
