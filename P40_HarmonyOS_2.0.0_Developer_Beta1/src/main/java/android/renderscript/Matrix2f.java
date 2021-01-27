package android.renderscript;

public class Matrix2f {
    final float[] mMat;

    public Matrix2f() {
        this.mMat = new float[4];
        loadIdentity();
    }

    public Matrix2f(float[] dataArray) {
        this.mMat = new float[4];
        float[] fArr = this.mMat;
        System.arraycopy(dataArray, 0, fArr, 0, fArr.length);
    }

    public float[] getArray() {
        return this.mMat;
    }

    public float get(int x, int y) {
        return this.mMat[(x * 2) + y];
    }

    public void set(int x, int y, float v) {
        this.mMat[(x * 2) + y] = v;
    }

    public void loadIdentity() {
        float[] fArr = this.mMat;
        fArr[0] = 1.0f;
        fArr[1] = 0.0f;
        fArr[2] = 0.0f;
        fArr[3] = 1.0f;
    }

    public void load(Matrix2f src) {
        float[] array = src.getArray();
        float[] fArr = this.mMat;
        System.arraycopy(array, 0, fArr, 0, fArr.length);
    }

    public void loadRotate(float rot) {
        float rot2 = rot * 0.017453292f;
        float c = (float) Math.cos((double) rot2);
        float s = (float) Math.sin((double) rot2);
        float[] fArr = this.mMat;
        fArr[0] = c;
        fArr[1] = -s;
        fArr[2] = s;
        fArr[3] = c;
    }

    public void loadScale(float x, float y) {
        loadIdentity();
        float[] fArr = this.mMat;
        fArr[0] = x;
        fArr[3] = y;
    }

    public void loadMultiply(Matrix2f lhs, Matrix2f rhs) {
        for (int i = 0; i < 2; i++) {
            float ri0 = 0.0f;
            float ri1 = 0.0f;
            for (int j = 0; j < 2; j++) {
                float rhs_ij = rhs.get(i, j);
                ri0 += lhs.get(j, 0) * rhs_ij;
                ri1 += lhs.get(j, 1) * rhs_ij;
            }
            set(i, 0, ri0);
            set(i, 1, ri1);
        }
    }

    public void multiply(Matrix2f rhs) {
        Matrix2f tmp = new Matrix2f();
        tmp.loadMultiply(this, rhs);
        load(tmp);
    }

    public void rotate(float rot) {
        Matrix2f tmp = new Matrix2f();
        tmp.loadRotate(rot);
        multiply(tmp);
    }

    public void scale(float x, float y) {
        Matrix2f tmp = new Matrix2f();
        tmp.loadScale(x, y);
        multiply(tmp);
    }

    public void transpose() {
        float[] fArr = this.mMat;
        float temp = fArr[1];
        fArr[1] = fArr[2];
        fArr[2] = temp;
    }
}
