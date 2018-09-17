package android.renderscript;

public class Matrix3f {
    final float[] mMat;

    public Matrix3f() {
        this.mMat = new float[9];
        loadIdentity();
    }

    public Matrix3f(float[] dataArray) {
        this.mMat = new float[9];
        System.arraycopy(dataArray, 0, this.mMat, 0, this.mMat.length);
    }

    public float[] getArray() {
        return this.mMat;
    }

    public float get(int x, int y) {
        return this.mMat[(x * 3) + y];
    }

    public void set(int x, int y, float v) {
        this.mMat[(x * 3) + y] = v;
    }

    public void loadIdentity() {
        this.mMat[0] = 1.0f;
        this.mMat[1] = 0.0f;
        this.mMat[2] = 0.0f;
        this.mMat[3] = 0.0f;
        this.mMat[4] = 1.0f;
        this.mMat[5] = 0.0f;
        this.mMat[6] = 0.0f;
        this.mMat[7] = 0.0f;
        this.mMat[8] = 1.0f;
    }

    public void load(Matrix3f src) {
        System.arraycopy(src.getArray(), 0, this.mMat, 0, this.mMat.length);
    }

    public void loadRotate(float rot, float x, float y, float z) {
        rot *= 0.017453292f;
        float c = (float) Math.cos((double) rot);
        float s = (float) Math.sin((double) rot);
        float len = (float) Math.sqrt((double) (((x * x) + (y * y)) + (z * z)));
        if ((len != 1.0f ? 1 : null) == null) {
            float recipLen = 1.0f / len;
            x *= recipLen;
            y *= recipLen;
            z *= recipLen;
        }
        float nc = 1.0f - c;
        float xy = x * y;
        float yz = y * z;
        float zx = z * x;
        float xs = x * s;
        float ys = y * s;
        float zs = z * s;
        this.mMat[0] = ((x * x) * nc) + c;
        this.mMat[3] = (xy * nc) - zs;
        this.mMat[6] = (zx * nc) + ys;
        this.mMat[1] = (xy * nc) + zs;
        this.mMat[4] = ((y * y) * nc) + c;
        this.mMat[7] = (yz * nc) - xs;
        this.mMat[2] = (zx * nc) - ys;
        this.mMat[5] = (yz * nc) + xs;
        this.mMat[8] = ((z * z) * nc) + c;
    }

    public void loadRotate(float rot) {
        loadIdentity();
        rot *= 0.017453292f;
        float c = (float) Math.cos((double) rot);
        float s = (float) Math.sin((double) rot);
        this.mMat[0] = c;
        this.mMat[1] = -s;
        this.mMat[3] = s;
        this.mMat[4] = c;
    }

    public void loadScale(float x, float y) {
        loadIdentity();
        this.mMat[0] = x;
        this.mMat[4] = y;
    }

    public void loadScale(float x, float y, float z) {
        loadIdentity();
        this.mMat[0] = x;
        this.mMat[4] = y;
        this.mMat[8] = z;
    }

    public void loadTranslate(float x, float y) {
        loadIdentity();
        this.mMat[6] = x;
        this.mMat[7] = y;
    }

    public void loadMultiply(Matrix3f lhs, Matrix3f rhs) {
        for (int i = 0; i < 3; i++) {
            float ri0 = 0.0f;
            float ri1 = 0.0f;
            float ri2 = 0.0f;
            for (int j = 0; j < 3; j++) {
                float rhs_ij = rhs.get(i, j);
                ri0 += lhs.get(j, 0) * rhs_ij;
                ri1 += lhs.get(j, 1) * rhs_ij;
                ri2 += lhs.get(j, 2) * rhs_ij;
            }
            set(i, 0, ri0);
            set(i, 1, ri1);
            set(i, 2, ri2);
        }
    }

    public void multiply(Matrix3f rhs) {
        Matrix3f tmp = new Matrix3f();
        tmp.loadMultiply(this, rhs);
        load(tmp);
    }

    public void rotate(float rot, float x, float y, float z) {
        Matrix3f tmp = new Matrix3f();
        tmp.loadRotate(rot, x, y, z);
        multiply(tmp);
    }

    public void rotate(float rot) {
        Matrix3f tmp = new Matrix3f();
        tmp.loadRotate(rot);
        multiply(tmp);
    }

    public void scale(float x, float y) {
        Matrix3f tmp = new Matrix3f();
        tmp.loadScale(x, y);
        multiply(tmp);
    }

    public void scale(float x, float y, float z) {
        Matrix3f tmp = new Matrix3f();
        tmp.loadScale(x, y, z);
        multiply(tmp);
    }

    public void translate(float x, float y) {
        Matrix3f tmp = new Matrix3f();
        tmp.loadTranslate(x, y);
        multiply(tmp);
    }

    public void transpose() {
        for (int i = 0; i < 2; i++) {
            for (int j = i + 1; j < 3; j++) {
                float temp = this.mMat[(i * 3) + j];
                this.mMat[(i * 3) + j] = this.mMat[(j * 3) + i];
                this.mMat[(j * 3) + i] = temp;
            }
        }
    }
}
