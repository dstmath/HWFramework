package android.renderscript;

public class Matrix4f {
    final float[] mMat = new float[16];

    public Matrix4f() {
        loadIdentity();
    }

    public Matrix4f(float[] dataArray) {
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
        float z2;
        float y2;
        float x2;
        this.mMat[3] = 0.0f;
        this.mMat[7] = 0.0f;
        this.mMat[11] = 0.0f;
        this.mMat[12] = 0.0f;
        this.mMat[13] = 0.0f;
        this.mMat[14] = 0.0f;
        this.mMat[15] = 1.0f;
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
        this.mMat[0] = (x2 * x2 * nc) + c;
        this.mMat[4] = (xy * nc) - zs;
        this.mMat[8] = (zx * nc) + ys;
        this.mMat[1] = (xy * nc) + zs;
        this.mMat[5] = (y2 * y2 * nc) + c;
        this.mMat[9] = (yz * nc) - xs;
        this.mMat[2] = (zx * nc) - ys;
        this.mMat[6] = (yz * nc) + xs;
        this.mMat[10] = (z2 * z2 * nc) + c;
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
            float ri3 = 0.0f;
            float ri2 = 0.0f;
            float ri1 = 0.0f;
            float ri0 = 0.0f;
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
        float minor = ((this.mMat[(4 * r0) + c0] * ((this.mMat[(4 * r1) + c1] * this.mMat[(4 * r2) + c2]) - (this.mMat[(4 * r2) + c1] * this.mMat[(4 * r1) + c2]))) - (this.mMat[(4 * r1) + c0] * ((this.mMat[(4 * r0) + c1] * this.mMat[(4 * r2) + c2]) - (this.mMat[(4 * r2) + c1] * this.mMat[(4 * r0) + c2])))) + (this.mMat[(4 * r2) + c0] * ((this.mMat[(4 * r0) + c1] * this.mMat[(4 * r1) + c2]) - (this.mMat[(4 * r1) + c1] * this.mMat[(4 * r0) + c2])));
        return ((i + j) & 1) != 0 ? -minor : minor;
    }

    public boolean inverse() {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.mMat[(4 * i) + j] = computeCofactor(i, j);
            }
        }
        float det = (this.mMat[0] * result.mMat[0]) + (this.mMat[4] * result.mMat[1]) + (this.mMat[8] * result.mMat[2]) + (this.mMat[12] * result.mMat[3]);
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
                result.mMat[(4 * j) + i] = computeCofactor(i, j);
            }
        }
        float det = (this.mMat[0] * result.mMat[0]) + (this.mMat[4] * result.mMat[4]) + (this.mMat[8] * result.mMat[8]) + (this.mMat[12] * result.mMat[12]);
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
                float temp = this.mMat[(i * 4) + j];
                this.mMat[(i * 4) + j] = this.mMat[(j * 4) + i];
                this.mMat[(j * 4) + i] = temp;
            }
        }
    }
}
