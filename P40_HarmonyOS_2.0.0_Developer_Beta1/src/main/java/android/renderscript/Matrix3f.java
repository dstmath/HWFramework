package android.renderscript;

public class Matrix3f {
    final float[] mMat;

    public Matrix3f() {
        this.mMat = new float[9];
        loadIdentity();
    }

    public Matrix3f(float[] dataArray) {
        this.mMat = new float[9];
        float[] fArr = this.mMat;
        System.arraycopy(dataArray, 0, fArr, 0, fArr.length);
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
        float[] fArr = this.mMat;
        fArr[0] = 1.0f;
        fArr[1] = 0.0f;
        fArr[2] = 0.0f;
        fArr[3] = 0.0f;
        fArr[4] = 1.0f;
        fArr[5] = 0.0f;
        fArr[6] = 0.0f;
        fArr[7] = 0.0f;
        fArr[8] = 1.0f;
    }

    public void load(Matrix3f src) {
        float[] array = src.getArray();
        float[] fArr = this.mMat;
        System.arraycopy(array, 0, fArr, 0, fArr.length);
    }

    public void loadRotate(float rot, float x, float y, float z) {
        float z2;
        float y2;
        float x2;
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
        float[] fArr = this.mMat;
        fArr[0] = (x2 * x2 * nc) + c;
        fArr[3] = (xy * nc) - zs;
        fArr[6] = (zx * nc) + ys;
        fArr[1] = (xy * nc) + zs;
        fArr[4] = (y2 * y2 * nc) + c;
        fArr[7] = (yz * nc) - xs;
        fArr[2] = (zx * nc) - ys;
        fArr[5] = (yz * nc) + xs;
        fArr[8] = (z2 * z2 * nc) + c;
    }

    public void loadRotate(float rot) {
        loadIdentity();
        float rot2 = rot * 0.017453292f;
        float c = (float) Math.cos((double) rot2);
        float s = (float) Math.sin((double) rot2);
        float[] fArr = this.mMat;
        fArr[0] = c;
        fArr[1] = -s;
        fArr[3] = s;
        fArr[4] = c;
    }

    public void loadScale(float x, float y) {
        loadIdentity();
        float[] fArr = this.mMat;
        fArr[0] = x;
        fArr[4] = y;
    }

    public void loadScale(float x, float y, float z) {
        loadIdentity();
        float[] fArr = this.mMat;
        fArr[0] = x;
        fArr[4] = y;
        fArr[8] = z;
    }

    public void loadTranslate(float x, float y) {
        loadIdentity();
        float[] fArr = this.mMat;
        fArr[6] = x;
        fArr[7] = y;
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
                float[] fArr = this.mMat;
                float temp = fArr[(i * 3) + j];
                fArr[(i * 3) + j] = fArr[(j * 3) + i];
                fArr[(j * 3) + i] = temp;
            }
        }
    }
}
