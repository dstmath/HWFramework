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
        for (int i = 0; i < this.mem.length; i++) {
            this.mem[i] = that.mem[i];
        }
    }

    public double get(int i, int j) {
        if (i >= 0 && i < this.n && j >= 0 && j < this.m) {
            return this.mem[(this.m * i) + j];
        }
        throw new IndexOutOfBoundsException();
    }

    public void put(int i, int j, double v) {
        if (i < 0 || i >= this.n || j < 0 || j >= this.m) {
            throw new IndexOutOfBoundsException();
        }
        this.mem[(this.m * i) + j] = v;
    }

    public Matrix plus(Matrix that) {
        return plus(that, new Matrix(this.n, this.m));
    }

    public Matrix plus(Matrix that, Matrix result) {
        if (this.n == that.n && this.m == that.m && this.n == result.n && this.m == result.m) {
            for (int i = 0; i < this.mem.length; i++) {
                result.mem[i] = this.mem[i] + that.mem[i];
            }
            return result;
        }
        throw new IllegalArgumentException();
    }

    public Matrix minus(Matrix that) {
        return minus(that, new Matrix(this.n, this.m));
    }

    public Matrix minus(Matrix that, Matrix result) {
        if (this.n == that.n && this.m == that.m && this.n == result.n && this.m == result.m) {
            for (int i = 0; i < this.mem.length; i++) {
                result.mem[i] = this.mem[i] - that.mem[i];
            }
            return result;
        }
        throw new IllegalArgumentException();
    }

    public Matrix times(double scalar) {
        return times(scalar, new Matrix(this.n, this.m));
    }

    public Matrix times(double scalar, Matrix result) {
        if (this.n == result.n && this.m == result.m) {
            for (int i = 0; i < this.mem.length; i++) {
                result.mem[i] = this.mem[i] * scalar;
            }
            return result;
        }
        throw new IllegalArgumentException();
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
        return inverse(new Matrix(this.n, this.m), new Matrix(this.n, 2 * this.m));
    }

    public Matrix inverse(Matrix result, Matrix scratch) {
        Matrix matrix = result;
        Matrix matrix2 = scratch;
        if (this.n == this.m && this.n == matrix.n && this.m == matrix.m && this.n == matrix2.n && 2 * this.m == matrix2.m) {
            int i = 0;
            while (i < this.n) {
                int j = 0;
                while (j < this.m) {
                    matrix2.put(i, j, get(i, j));
                    matrix2.put(i, this.m + j, i == j ? 1.0d : 0.0d);
                    j++;
                }
                i++;
            }
            int i2 = 0;
            while (i2 < this.n) {
                int ibest = i2;
                double vbest = Math.abs(matrix2.get(ibest, ibest));
                for (int ii = i2 + 1; ii < this.n; ii++) {
                    double v = Math.abs(matrix2.get(ii, i2));
                    if (v > vbest) {
                        ibest = ii;
                        vbest = v;
                    }
                }
                if (ibest != i2) {
                    for (int j2 = 0; j2 < matrix2.m; j2++) {
                        double t = matrix2.get(i2, j2);
                        matrix2.put(i2, j2, matrix2.get(ibest, j2));
                        matrix2.put(ibest, j2, t);
                    }
                }
                double d = matrix2.get(i2, i2);
                if (d != 0.0d) {
                    for (int j3 = 0; j3 < matrix2.m; j3++) {
                        matrix2.put(i2, j3, matrix2.get(i2, j3) / d);
                    }
                    for (int ii2 = i2 + 1; ii2 < this.n; ii2++) {
                        double d2 = matrix2.get(ii2, i2);
                        for (int j4 = 0; j4 < matrix2.m; j4++) {
                            matrix2.put(ii2, j4, matrix2.get(ii2, j4) - (matrix2.get(i2, j4) * d2));
                        }
                    }
                    i2++;
                } else {
                    throw new ArithmeticException("Singular matrix");
                }
            }
            for (int i3 = this.n - 1; i3 >= 0; i3--) {
                for (int ii3 = 0; ii3 < i3; ii3++) {
                    double d3 = matrix2.get(ii3, i3);
                    for (int j5 = 0; j5 < matrix2.m; j5++) {
                        matrix2.put(ii3, j5, matrix2.get(ii3, j5) - (matrix2.get(i3, j5) * d3));
                    }
                }
            }
            for (int i4 = 0; i4 < matrix.n; i4++) {
                for (int j6 = 0; j6 < matrix.m; j6++) {
                    matrix.put(i4, j6, matrix2.get(i4, this.m + j6));
                }
            }
            return matrix;
        }
        throw new IllegalArgumentException();
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
        for (int i = 0; i < this.mem.length; i++) {
            if (this.mem[i] != other.mem[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int h = (this.n * 101) + this.m;
        for (double hashCode : this.mem) {
            h = (h * 37) + Double.hashCode(hashCode);
        }
        return h;
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
