package android.renderscript;

import android.annotation.UnsupportedAppUsage;

public class Matrix4f {
    @UnsupportedAppUsage
    final float[] mMat;

    public Matrix4f() {
        this.mMat = new float[16];
        loadIdentity();
    }

    public Matrix4f(float[] dataArray) {
        this.mMat = new float[16];
        float[] fArr = this.mMat;
        System.arraycopy(dataArray, 0, fArr, 0, fArr.length);
    }

    public float[] getArray() {
        return this.mMat;
    }

    public float get(int x, int y) {
        return this.mMat[(x * 4) + y];
    }

    public void set(int x, int y, float v) {
        this.mMat[(x * 4) + y] = v;
    }

    public void loadIdentity() {
        float[] fArr = this.mMat;
        fArr[0] = 1.0f;
        fArr[1] = 0.0f;
        fArr[2] = 0.0f;
        fArr[3] = 0.0f;
        fArr[4] = 0.0f;
        fArr[5] = 1.0f;
        fArr[6] = 0.0f;
        fArr[7] = 0.0f;
        fArr[8] = 0.0f;
        fArr[9] = 0.0f;
        fArr[10] = 1.0f;
        fArr[11] = 0.0f;
        fArr[12] = 0.0f;
        fArr[13] = 0.0f;
        fArr[14] = 0.0f;
        fArr[15] = 1.0f;
    }

    public void load(Matrix4f src) {
        float[] array = src.getArray();
        float[] fArr = this.mMat;
        System.arraycopy(array, 0, fArr, 0, fArr.length);
    }

    public void load(Matrix3f src) {
        this.mMat[0] = src.mMat[0];
        this.mMat[1] = src.mMat[1];
        this.mMat[2] = src.mMat[2];
        float[] fArr = this.mMat;
        fArr[3] = 0.0f;
        fArr[4] = src.mMat[3];
        this.mMat[5] = src.mMat[4];
        this.mMat[6] = src.mMat[5];
        float[] fArr2 = this.mMat;
        fArr2[7] = 0.0f;
        fArr2[8] = src.mMat[6];
        this.mMat[9] = src.mMat[7];
        this.mMat[10] = src.mMat[8];
        float[] fArr3 = this.mMat;
        fArr3[11] = 0.0f;
        fArr3[12] = 0.0f;
        fArr3[13] = 0.0f;
        fArr3[14] = 0.0f;
        fArr3[15] = 1.0f;
    }

    public void loadRotate(float rot, float x, float y, float z) {
        float z2;
        float y2;
        float x2;
        float[] fArr = this.mMat;
        fArr[3] = 0.0f;
        fArr[7] = 0.0f;
        fArr[11] = 0.0f;
        fArr[12] = 0.0f;
        fArr[13] = 0.0f;
        fArr[14] = 0.0f;
        fArr[15] = 1.0f;
        float rot2 = 0.017453292f * rot;
        float c = (float) Math.cos((double) rot2);
        float s = (float) Math.sin((double) rot2);
        float len = (float) Math.sqrt((double) ((x * x) + (y * y) + (z * z)));
        if (len == 1.0f) {
            float recipLen = 1.0f / len;
            x2 = x * recipLen;
            y2 = y * recipLen;
            z2 = z * recipLen;
        } else {
            x2 = x;
            y2 = y;
            z2 = z;
        }
        float nc = 1.0f - c;
        float xy = x2 * y2;
        float yz = y2 * z2;
        float zx = z2 * x2;
        float xs = x2 * s;
        float ys = y2 * s;
        float zs = z2 * s;
        float[] fArr2 = this.mMat;
        fArr2[0] = (x2 * x2 * nc) + c;
        fArr2[4] = (xy * nc) - zs;
        fArr2[8] = (zx * nc) + ys;
        fArr2[1] = (xy * nc) + zs;
        fArr2[5] = (y2 * y2 * nc) + c;
        fArr2[9] = (yz * nc) - xs;
        fArr2[2] = (zx * nc) - ys;
        fArr2[6] = (yz * nc) + xs;
        fArr2[10] = (z2 * z2 * nc) + c;
    }

    public void loadScale(float x, float y, float z) {
        loadIdentity();
        float[] fArr = this.mMat;
        fArr[0] = x;
        fArr[5] = y;
        fArr[10] = z;
    }

    public void loadTranslate(float x, float y, float z) {
        loadIdentity();
        float[] fArr = this.mMat;
        fArr[12] = x;
        fArr[13] = y;
        fArr[14] = z;
    }

    public void loadMultiply(Matrix4f lhs, Matrix4f rhs) {
        for (int i = 0; i < 4; i++) {
            float ri0 = 0.0f;
            float ri1 = 0.0f;
            float ri2 = 0.0f;
            float ri3 = 0.0f;
            for (int j = 0; j < 4; j++) {
                float rhs_ij = rhs.get(i, j);
                ri0 += lhs.get(j, 0) * rhs_ij;
                ri1 += lhs.get(j, 1) * rhs_ij;
                ri2 += lhs.get(j, 2) * rhs_ij;
                ri3 += lhs.get(j, 3) * rhs_ij;
            }
            set(i, 0, ri0);
            set(i, 1, ri1);
            set(i, 2, ri2);
            set(i, 3, ri3);
        }
    }

    public void loadOrtho(float l, float r, float b, float t, float n, float f) {
        loadIdentity();
        float[] fArr = this.mMat;
        fArr[0] = 2.0f / (r - l);
        fArr[5] = 2.0f / (t - b);
        fArr[10] = -2.0f / (f - n);
        fArr[12] = (-(r + l)) / (r - l);
        fArr[13] = (-(t + b)) / (t - b);
        fArr[14] = (-(f + n)) / (f - n);
    }

    public void loadOrthoWindow(int w, int h) {
        loadOrtho(0.0f, (float) w, (float) h, 0.0f, -1.0f, 1.0f);
    }

    public void loadFrustum(float l, float r, float b, float t, float n, float f) {
        loadIdentity();
        float[] fArr = this.mMat;
        fArr[0] = (n * 2.0f) / (r - l);
        fArr[5] = (2.0f * n) / (t - b);
        fArr[8] = (r + l) / (r - l);
        fArr[9] = (t + b) / (t - b);
        fArr[10] = (-(f + n)) / (f - n);
        fArr[11] = -1.0f;
        fArr[14] = ((-2.0f * f) * n) / (f - n);
        fArr[15] = 0.0f;
    }

    public void loadPerspective(float fovy, float aspect, float near, float far) {
        float top = ((float) Math.tan((double) ((float) ((((double) fovy) * 3.141592653589793d) / 360.0d)))) * near;
        float bottom = -top;
        loadFrustum(bottom * aspect, top * aspect, bottom, top, near, far);
    }

    public void loadProjectionNormalized(int w, int h) {
        Matrix4f m1 = new Matrix4f();
        Matrix4f m2 = new Matrix4f();
        if (w > h) {
            float aspect = ((float) w) / ((float) h);
            m1.loadFrustum(-aspect, aspect, -1.0f, 1.0f, 1.0f, 100.0f);
        } else {
            float aspect2 = ((float) h) / ((float) w);
            m1.loadFrustum(-1.0f, 1.0f, -aspect2, aspect2, 1.0f, 100.0f);
        }
        m2.loadRotate(180.0f, 0.0f, 1.0f, 0.0f);
        m1.loadMultiply(m1, m2);
        m2.loadScale(-2.0f, 2.0f, 1.0f);
        m1.loadMultiply(m1, m2);
        m2.loadTranslate(0.0f, 0.0f, 2.0f);
        m1.loadMultiply(m1, m2);
        load(m1);
    }

    public void multiply(Matrix4f rhs) {
        Matrix4f tmp = new Matrix4f();
        tmp.loadMultiply(this, rhs);
        load(tmp);
    }

    public void rotate(float rot, float x, float y, float z) {
        Matrix4f tmp = new Matrix4f();
        tmp.loadRotate(rot, x, y, z);
        multiply(tmp);
    }

    public void scale(float x, float y, float z) {
        Matrix4f tmp = new Matrix4f();
        tmp.loadScale(x, y, z);
        multiply(tmp);
    }

    public void translate(float x, float y, float z) {
        Matrix4f tmp = new Matrix4f();
        tmp.loadTranslate(x, y, z);
        multiply(tmp);
    }

    private float computeCofactor(int i, int j) {
        int c0 = (i + 1) % 4;
        int c1 = (i + 2) % 4;
        int c2 = (i + 3) % 4;
        int r0 = (j + 1) % 4;
        int r1 = (j + 2) % 4;
        int r2 = (j + 3) % 4;
        float[] fArr = this.mMat;
        float minor = ((fArr[(r0 * 4) + c0] * ((fArr[(r1 * 4) + c1] * fArr[(r2 * 4) + c2]) - (fArr[(r2 * 4) + c1] * fArr[(r1 * 4) + c2]))) - (fArr[(r1 * 4) + c0] * ((fArr[(r0 * 4) + c1] * fArr[(r2 * 4) + c2]) - (fArr[(r2 * 4) + c1] * fArr[(r0 * 4) + c2])))) + (fArr[(r2 * 4) + c0] * ((fArr[(r0 * 4) + c1] * fArr[(r1 * 4) + c2]) - (fArr[(r1 * 4) + c1] * fArr[(r0 * 4) + c2])));
        if (((i + j) & 1) != 0) {
            return -minor;
        }
        return minor;
    }

    public boolean inverse() {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.mMat[(i * 4) + j] = computeCofactor(i, j);
            }
        }
        float[] fArr = this.mMat;
        float f = fArr[0];
        float[] fArr2 = result.mMat;
        float det = (f * fArr2[0]) + (fArr[4] * fArr2[1]) + (fArr[8] * fArr2[2]) + (fArr[12] * fArr2[3]);
        if (((double) Math.abs(det)) < 1.0E-6d) {
            return false;
        }
        float det2 = 1.0f / det;
        for (int i2 = 0; i2 < 16; i2++) {
            this.mMat[i2] = result.mMat[i2] * det2;
        }
        return true;
    }

    public boolean inverseTranspose() {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.mMat[(j * 4) + i] = computeCofactor(i, j);
            }
        }
        float[] fArr = this.mMat;
        float f = fArr[0];
        float[] fArr2 = result.mMat;
        float det = (f * fArr2[0]) + (fArr[4] * fArr2[4]) + (fArr[8] * fArr2[8]) + (fArr[12] * fArr2[12]);
        if (((double) Math.abs(det)) < 1.0E-6d) {
            return false;
        }
        float det2 = 1.0f / det;
        for (int i2 = 0; i2 < 16; i2++) {
            this.mMat[i2] = result.mMat[i2] * det2;
        }
        return true;
    }

    public void transpose() {
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 4; j++) {
                float[] fArr = this.mMat;
                float temp = fArr[(i * 4) + j];
                fArr[(i * 4) + j] = fArr[(j * 4) + i];
                fArr[(j * 4) + i] = temp;
            }
        }
    }
}
