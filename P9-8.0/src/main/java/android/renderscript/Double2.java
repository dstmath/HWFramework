package android.renderscript;

public class Double2 {
    public double x;
    public double y;

    public Double2(Double2 data) {
        this.x = data.x;
        this.y = data.y;
    }

    public Double2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Double2 add(Double2 a, Double2 b) {
        Double2 res = new Double2();
        res.x = a.x + b.x;
        res.y = a.y + b.y;
        return res;
    }

    public void add(Double2 value) {
        this.x += value.x;
        this.y += value.y;
    }

    public void add(double value) {
        this.x += value;
        this.y += value;
    }

    public static Double2 add(Double2 a, double b) {
        Double2 res = new Double2();
        res.x = a.x + b;
        res.y = a.y + b;
        return res;
    }

    public void sub(Double2 value) {
        this.x -= value.x;
        this.y -= value.y;
    }

    public static Double2 sub(Double2 a, Double2 b) {
        Double2 res = new Double2();
        res.x = a.x - b.x;
        res.y = a.y - b.y;
        return res;
    }

    public void sub(double value) {
        this.x -= value;
        this.y -= value;
    }

    public static Double2 sub(Double2 a, double b) {
        Double2 res = new Double2();
        res.x = a.x - b;
        res.y = a.y - b;
        return res;
    }

    public void mul(Double2 value) {
        this.x *= value.x;
        this.y *= value.y;
    }

    public static Double2 mul(Double2 a, Double2 b) {
        Double2 res = new Double2();
        res.x = a.x * b.x;
        res.y = a.y * b.y;
        return res;
    }

    public void mul(double value) {
        this.x *= value;
        this.y *= value;
    }

    public static Double2 mul(Double2 a, double b) {
        Double2 res = new Double2();
        res.x = a.x * b;
        res.y = a.y * b;
        return res;
    }

    public void div(Double2 value) {
        this.x /= value.x;
        this.y /= value.y;
    }

    public static Double2 div(Double2 a, Double2 b) {
        Double2 res = new Double2();
        res.x = a.x / b.x;
        res.y = a.y / b.y;
        return res;
    }

    public void div(double value) {
        this.x /= value;
        this.y /= value;
    }

    public static Double2 div(Double2 a, double b) {
        Double2 res = new Double2();
        res.x = a.x / b;
        res.y = a.y / b;
        return res;
    }

    public double dotProduct(Double2 a) {
        return (this.x * a.x) + (this.y * a.y);
    }

    public static Double dotProduct(Double2 a, Double2 b) {
        return Double.valueOf((b.x * a.x) + (b.y * a.y));
    }

    public void addMultiple(Double2 a, double factor) {
        this.x += a.x * factor;
        this.y += a.y * factor;
    }

    public void set(Double2 a) {
        this.x = a.x;
        this.y = a.y;
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
    }

    public int length() {
        return 2;
    }

    public double elementSum() {
        return this.x + this.y;
    }

    public double get(int i) {
        switch (i) {
            case 0:
                return this.x;
            case 1:
                return this.y;
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
            default:
                throw new IndexOutOfBoundsException("Index: i");
        }
    }

    public void setValues(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void copyTo(double[] data, int offset) {
        data[offset] = this.x;
        data[offset + 1] = this.y;
    }
}
