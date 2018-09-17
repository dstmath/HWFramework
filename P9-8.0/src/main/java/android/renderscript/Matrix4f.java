package android.renderscript;

public class Matrix4f {
    final float[] mMat;

    public Matrix4f() {
        this.mMat = new float[16];
        loadIdentity();
    }

    public Matrix4f(float[] dataArray) {
        this.mMat = new float[16];
        System.arraycopy(dataArray, 0, this.mMat, 0, this.mMat.length);
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
        this.mMat[0] = 1.0f;
        this.mMat[1] = 0.0f;
        this.mMat[2] = 0.0f;
        this.mMat[3] = 0.0f;
        this.mMat[4] = 0.0f;
        this.mMat[5] = 1.0f;
        this.mMat[6] = 0.0f;
        this.mMat[7] = 0.0f;
        this.mMat[8] = 0.0f;
        this.mMat[9] = 0.0f;
        this.mMat[10] = 1.0f;
        this.mMat[11] = 0.0f;
        this.mMat[12] = 0.0f;
        this.mMat[13] = 0.0f;
        this.mMat[14] = 0.0f;
        this.mMat[15] = 1.0f;
    }

    public void load(Matrix4f src) {
        System.arraycopy(src.getArray(), 0, this.mMat, 0, this.mMat.length);
    }

    public void load(Matrix3f src) {
        this.mMat[0] = src.mMat[0];
        this.mMat[1] = src.mMat[1];
        this.mMat[2] = src.mMat[2];
        this.mMat[3] = 0.0f;
        this.mMat[4] = src.mMat[3];
        this.mMat[5] = src.mMat[4];
        this.mMat[6] = src.mMat[5];
        this.mMat[7] = 0.0f;
        this.mMat[8] = src.mMat[6];
        this.mMat[9] = src.mMat[7];
        this.mMat[10] = src.mMat[8];
        this.mMat[11] = 0.0f;
        this.mMat[12] = 0.0f;
        this.mMat[13] = 0.0f;
        this.mMat[14] = 0.0f;
        this.mMat[15] = 1.0f;
    }

    public void loadRotate(float rot, float x, float y, float z) {
        this.mMat[3] = 0.0f;
        this.mMat[7] = 0.0f;
        this.mMat[11] = 0.0f;
        this.mMat[12] = 0.0f;
        this.mMat[13] = 0.0f;
        this.mMat[14] = 0.0f;
        this.mMat[15] = 1.0f;
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
        this.mMat[4] = (xy * nc) - zs;
        this.mMat[8] = (zx * nc) + ys;
        this.mMat[1] = (xy * nc) + zs;
        this.mMat[5] = ((y * y) * nc) + c;
        this.mMat[9] = (yz * nc) - xs;
        this.mMat[2] = (zx * nc) - ys;
        this.mMat[6] = (yz * nc) + xs;
        this.mMat[10] = ((z * z) * nc) + c;
    }

    public void loadScale(float x, float y, float z) {
        loadIdentity();
        this.mMat[0] = x;
        this.mMat[5] = y;
        this.mMat[10] = z;
    }

    public void loadTranslate(float x, float y, float z) {
        loadIdentity();
        this.mMat[12] = x;
        this.mMat[13] = y;
        this.mMat[14] = z;
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
        this.mMat[0] = 2.0f / (r - l);
        this.mMat[5] = 2.0f / (t - b);
        this.mMat[10] = -2.0f / (f - n);
        this.mMat[12] = (-(r + l)) / (r - l);
        this.mMat[13] = (-(t + b)) / (t - b);
        this.mMat[14] = (-(f + n)) / (f - n);
    }

    public void loadOrthoWindow(int w, int h) {
        loadOrtho(0.0f, (float) w, (float) h, 0.0f, -1.0f, 1.0f);
    }

    public void loadFrustum(float l, float r, float b, float t, float n, float f) {
        loadIdentity();
        this.mMat[0] = (2.0f * n) / (r - l);
        this.mMat[5] = (2.0f * n) / (t - b);
        this.mMat[8] = (r + l) / (r - l);
        this.mMat[9] = (t + b) / (t - b);
        this.mMat[10] = (-(f + n)) / (f - n);
        this.mMat[11] = -1.0f;
        this.mMat[14] = ((-2.0f * f) * n) / (f - n);
        this.mMat[15] = 0.0f;
    }

    public void loadPerspective(float fovy, float aspect, float near, float far) {
        float top = near * ((float) Math.tan((double) ((float) ((((double) fovy) * 3.141592653589793d) / 360.0d))));
        float bottom = -top;
        loadFrustum(bottom * aspect, top * aspect, bottom, top, near, far);
    }

    public void loadProjectionNormalized(int w, int h) {
        Matrix4f m1 = new Matrix4f();
        Matrix4f m2 = new Matrix4f();
        float aspect;
        if (w > h) {
            aspect = ((float) w) / ((float) h);
            m1.loadFrustum(-aspect, aspect, -1.0f, 1.0f, 1.0f, 100.0f);
        } else {
            aspect = ((float) h) / ((float) w);
            m1.loadFrustum(-1.0f, 1.0f, -aspect, aspect, 1.0f, 100.0f);
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
        float minor = ((this.mMat[(r0 * 4) + c0] * ((this.mMat[(r1 * 4) + c1] * this.mMat[(r2 * 4) + c2]) - (this.mMat[(r2 * 4) + c1] * this.mMat[(r1 * 4) + c2]))) - (this.mMat[(r1 * 4) + c0] * ((this.mMat[(r0 * 4) + c1] * this.mMat[(r2 * 4) + c2]) - (this.mMat[(r2 * 4) + c1] * this.mMat[(r0 * 4) + c2])))) + (this.mMat[(r2 * 4) + c0] * ((this.mMat[(r0 * 4) + c1] * this.mMat[(r1 * 4) + c2]) - (this.mMat[(r1 * 4) + c1] * this.mMat[(r0 * 4) + c2])));
        return ((i + j) & 1) != 0 ? -minor : minor;
    }

    public boolean inverse() {
        int i;
        Matrix4f result = new Matrix4f();
        for (i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.mMat[(i * 4) + j] = computeCofactor(i, j);
            }
        }
        float det = (((this.mMat[0] * result.mMat[0]) + (this.mMat[4] * result.mMat[1])) + (this.mMat[8] * result.mMat[2])) + (this.mMat[12] * result.mMat[3]);
        if (((double) Math.abs(det)) < 1.0E-6d) {
            return false;
        }
        det = 1.0f / det;
        for (i = 0; i < 16; i++) {
            this.mMat[i] = result.mMat[i] * det;
        }
        return true;
    }

    public boolean inverseTranspose() {
        int i;
        Matrix4f result = new Matrix4f();
        for (i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.mMat[(j * 4) + i] = computeCofactor(i, j);
            }
        }
        float det = (((this.mMat[0] * result.mMat[0]) + (this.mMat[4] * result.mMat[4])) + (this.mMat[8] * result.mMat[8])) + (this.mMat[12] * result.mMat[12]);
        if (((double) Math.abs(det)) < 1.0E-6d) {
            return false;
        }
        det = 1.0f / det;
        for (i = 0; i < 16; i++) {
            this.mMat[i] = result.mMat[i] * det;
        }
        return true;
    }

    public void transpose() {
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 4; j++) {
                float temp = this.mMat[(i * 4) + j];
                this.mMat[(i * 4) + j] = this.mMat[(j * 4) + i];
                this.mMat[(j * 4) + i] = temp;
            }
        }
    }
}
