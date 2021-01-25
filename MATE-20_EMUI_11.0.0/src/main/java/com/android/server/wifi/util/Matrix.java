package com.android.server.wifi.util;

public class Matrix {
    public final int m;
    public final double[] mem;
    public final int n;

    public Matrix(int rows, int cols) {
        this.n = rows;
        this.m = cols;
        this.mem = new double[(rows * cols)];
    }

    public Matrix(int stride, double[] values) {
        this.n = ((values.length + stride) - 1) / stride;
        this.m = stride;
        this.mem = values;
        if (this.mem.length != this.n * this.m) {
            throw new IllegalArgumentException();
        }
    }

    public Matrix(Matrix that) {
        this.n = that.n;
        this.m = that.m;
        this.mem = new double[that.mem.length];
        int i = 0;
        while (true) {
            double[] dArr = this.mem;
            if (i < dArr.length) {
                dArr[i] = that.mem[i];
                i++;
            } else {
                return;
            }
        }
    }

    public double get(int i, int j) {
        int i2;
        if (i >= 0 && i < this.n && j >= 0 && j < (i2 = this.m)) {
            return this.mem[(i2 * i) + j];
        }
        throw new IndexOutOfBoundsException();
    }

    public void put(int i, int j, double v) {
        int i2;
        if (i < 0 || i >= this.n || j < 0 || j >= (i2 = this.m)) {
            throw new IndexOutOfBoundsException();
        }
        this.mem[(i2 * i) + j] = v;
    }

    public Matrix plus(Matrix that) {
        return plus(that, new Matrix(this.n, this.m));
    }

    public Matrix plus(Matrix that, Matrix result) {
        int i;
        int i2 = this.n;
        if (i2 == that.n && (i = this.m) == that.m && i2 == result.n && i == result.m) {
            int i3 = 0;
            while (true) {
                double[] dArr = this.mem;
                if (i3 >= dArr.length) {
                    return result;
                }
                result.mem[i3] = dArr[i3] + that.mem[i3];
                i3++;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Matrix minus(Matrix that) {
        return minus(that, new Matrix(this.n, this.m));
    }

    public Matrix minus(Matrix that, Matrix result) {
        int i;
        int i2 = this.n;
        if (i2 == that.n && (i = this.m) == that.m && i2 == result.n && i == result.m) {
            int i3 = 0;
            while (true) {
                double[] dArr = this.mem;
                if (i3 >= dArr.length) {
                    return result;
                }
                result.mem[i3] = dArr[i3] - that.mem[i3];
                i3++;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Matrix times(double scalar) {
        return times(scalar, new Matrix(this.n, this.m));
    }

    public Matrix times(double scalar, Matrix result) {
        if (this.n == result.n && this.m == result.m) {
            int i = 0;
            while (true) {
                double[] dArr = this.mem;
                if (i >= dArr.length) {
                    return result;
                }
                result.mem[i] = dArr[i] * scalar;
                i++;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Matrix dot(Matrix that) {
        return dot(that, new Matrix(this.n, that.m));
    }

    public Matrix dot(Matrix that, Matrix result) {
        if (this.n == result.n && this.m == that.n && that.m == result.m) {
            for (int i = 0; i < this.n; i++) {
                for (int j = 0; j < that.m; j++) {
                    double s = 0.0d;
                    for (int k = 0; k < this.m; k++) {
                        s += get(i, k) * that.get(k, j);
                    }
                    result.put(i, j, s);
                }
            }
            return result;
        }
        throw new IllegalArgumentException();
    }

    public Matrix transpose() {
        return transpose(new Matrix(this.m, this.n));
    }

    public Matrix transpose(Matrix result) {
        if (this.n == result.m && this.m == result.n) {
            for (int i = 0; i < this.n; i++) {
                for (int j = 0; j < this.m; j++) {
                    result.put(j, i, get(i, j));
                }
            }
            return result;
        }
        throw new IllegalArgumentException();
    }

    public Matrix inverse() {
        return inverse(new Matrix(this.n, this.m), new Matrix(this.n, this.m * 2));
    }

    public Matrix inverse(Matrix result, Matrix scratch) {
        int i = this.n;
        int i2 = this.m;
        if (i == i2 && i == result.n && i2 == result.m && i == scratch.n && i2 * 2 == scratch.m) {
            int i3 = 0;
            while (i3 < this.n) {
                int j = 0;
                while (j < this.m) {
                    scratch.put(i3, j, get(i3, j));
                    scratch.put(i3, this.m + j, i3 == j ? 1.0d : 0.0d);
                    j++;
                }
                i3++;
            }
            int i4 = 0;
            while (true) {
                int ibest = this.n;
                if (i4 < ibest) {
                    int ibest2 = i4;
                    double vbest = Math.abs(scratch.get(ibest2, ibest2));
                    for (int ii = i4 + 1; ii < this.n; ii++) {
                        double v = Math.abs(scratch.get(ii, i4));
                        if (v > vbest) {
                            ibest2 = ii;
                            vbest = v;
                        }
                    }
                    if (ibest2 != i4) {
                        for (int j2 = 0; j2 < scratch.m; j2++) {
                            double t = scratch.get(i4, j2);
                            scratch.put(i4, j2, scratch.get(ibest2, j2));
                            scratch.put(ibest2, j2, t);
                        }
                    }
                    double d = scratch.get(i4, i4);
                    if (d != 0.0d) {
                        for (int j3 = 0; j3 < scratch.m; j3++) {
                            scratch.put(i4, j3, scratch.get(i4, j3) / d);
                        }
                        for (int ii2 = i4 + 1; ii2 < this.n; ii2++) {
                            double d2 = scratch.get(ii2, i4);
                            for (int j4 = 0; j4 < scratch.m; j4++) {
                                scratch.put(ii2, j4, scratch.get(ii2, j4) - (scratch.get(i4, j4) * d2));
                            }
                        }
                        i4++;
                    } else {
                        throw new ArithmeticException("Singular matrix");
                    }
                } else {
                    for (int i5 = ibest - 1; i5 >= 0; i5--) {
                        for (int ii3 = 0; ii3 < i5; ii3++) {
                            double d3 = scratch.get(ii3, i5);
                            for (int j5 = 0; j5 < scratch.m; j5++) {
                                scratch.put(ii3, j5, scratch.get(ii3, j5) - (scratch.get(i5, j5) * d3));
                            }
                        }
                    }
                    for (int i6 = 0; i6 < result.n; i6++) {
                        for (int j6 = 0; j6 < result.m; j6++) {
                            result.put(i6, j6, scratch.get(i6, this.m + j6));
                        }
                    }
                    return result;
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Matrix dotTranspose(Matrix that) {
        return dotTranspose(that, new Matrix(this.n, that.n));
    }

    public Matrix dotTranspose(Matrix that, Matrix result) {
        if (this.n == result.n && this.m == that.m && that.n == result.m) {
            for (int i = 0; i < this.n; i++) {
                for (int j = 0; j < that.n; j++) {
                    double s = 0.0d;
                    for (int k = 0; k < this.m; k++) {
                        s += get(i, k) * that.get(j, k);
                    }
                    result.put(i, j, s);
                }
            }
            return result;
        }
        throw new IllegalArgumentException();
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof Matrix)) {
            return false;
        }
        Matrix other = (Matrix) that;
        if (this.n != other.n || this.m != other.m) {
            return false;
        }
        int i = 0;
        while (true) {
            double[] dArr = this.mem;
            if (i >= dArr.length) {
                return true;
            }
            if (dArr[i] != other.mem[i]) {
                return false;
            }
            i++;
        }
    }

    public int hashCode() {
        int h = (this.n * 101) + this.m;
        int i = 0;
        while (true) {
            double[] dArr = this.mem;
            if (i >= dArr.length) {
                return h;
            }
            h = (h * 37) + Double.hashCode(dArr[i]);
            i++;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.n * this.m * 8);
        sb.append("[");
        for (int i = 0; i < this.mem.length; i++) {
            if (i > 0) {
                sb.append(i % this.m == 0 ? "; " : ", ");
            }
            sb.append(this.mem[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
