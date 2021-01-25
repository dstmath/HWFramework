package ohos.agp.render.render3d.math;

import java.util.Arrays;

public class Matrix4x4 {
    private static final float EPSILON = 1.0E-6f;
    private static final float[] IDENTITY_VALUES = {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    private static final int M00 = 0;
    private static final int M01 = 4;
    private static final int M02 = 8;
    private static final int M03 = 12;
    private static final int M10 = 1;
    private static final int M11 = 5;
    private static final int M12 = 9;
    private static final int M13 = 13;
    private static final int M20 = 2;
    private static final int M21 = 6;
    private static final int M22 = 10;
    private static final int M23 = 14;
    private static final int M30 = 3;
    private static final int M31 = 7;
    private static final int M32 = 11;
    private static final int M33 = 15;
    private static final int MATRIX_COLUMN_COUNT = 4;
    private static final int MATRIX_COMPONENT_COUNT = 16;
    private static final int MATRIX_ROW_COUNT = 4;
    private static final int MATRIX_SCALE_COMPONENT_X = 0;
    private static final int MATRIX_SCALE_COMPONENT_Y = 5;
    private static final int MATRIX_SCALE_COMPONENT_Z = 10;
    private static final int MATRIX_TRANSLATION_COMPONENT_W = 15;
    private static final int MATRIX_TRANSLATION_COMPONENT_X = 12;
    private static final int MATRIX_TRANSLATION_COMPONENT_Y = 13;
    private static final int MATRIX_TRANSLATION_COMPONENT_Z = 14;
    private static final float QUAT_CONVERSION_MULTIPLIER = 2.0f;
    private float[] matValues = new float[16];

    public static Matrix4x4 identity() {
        Matrix4x4 matrix4x4 = new Matrix4x4();
        matrix4x4.set(IDENTITY_VALUES, 0);
        return matrix4x4;
    }

    public float get(int i) {
        if (i >= 0 && i <= 15) {
            return this.matValues[i];
        }
        throw new IllegalArgumentException("The index where to obtain the matrix value must be in the range of 0~15.");
    }

    public float get(int i, int i2) {
        if (i >= 0 && i < 4 && i2 >= 0 && i2 < 4) {
            return this.matValues[(i * 4) + i2];
        }
        throw new IllegalArgumentException("The row and column of the value to obtain must be in the range of 0~3.");
    }

    public void set(int i, float f) {
        if (i < 0 || i > 15) {
            throw new IllegalArgumentException("The index where to obtain the matrix value must be in the range of 0~15.");
        }
        this.matValues[i] = f;
    }

    public void set(int i, int i2, float f) {
        if (i < 0 || i >= 4 || i2 < 0 || i2 >= 4) {
            throw new IllegalArgumentException("The row and column of the value to obtain must be in the range of 0~3.");
        }
        this.matValues[(i * 4) + i2] = f;
    }

    public void set(float[] fArr, int i) {
        float[] fArr2 = this.matValues;
        if (fArr2 == null) {
            throw new IllegalArgumentException();
        } else if (fArr2.length - i >= 16) {
            System.arraycopy(fArr, i, fArr2, 0, 16);
        } else {
            throw new IllegalArgumentException("Matrix values must have at least 16 components starting from offset");
        }
    }

    public float[] getData() {
        return Arrays.copyOf(this.matValues, 16);
    }

    public void makeTranslation(Vector3 vector3) {
        set(IDENTITY_VALUES, 0);
        this.matValues[12] = vector3.getX();
        this.matValues[13] = vector3.getY();
        this.matValues[14] = vector3.getZ();
    }

    public void makeRotation(Quaternion quaternion) {
        set(IDENTITY_VALUES, 0);
        Quaternion normalized = quaternion.getNormalized();
        float y = normalized.getY() * normalized.getY();
        float z = normalized.getZ() * normalized.getZ();
        this.matValues[0] = 1.0f - ((y + z) * 2.0f);
        float x = normalized.getX() * normalized.getY();
        float z2 = normalized.getZ() * normalized.getW();
        this.matValues[4] = (x - z2) * 2.0f;
        float x2 = normalized.getX() * normalized.getZ();
        float y2 = normalized.getY() * normalized.getW();
        float[] fArr = this.matValues;
        fArr[8] = (x2 + y2) * 2.0f;
        fArr[1] = (x + z2) * 2.0f;
        float x3 = normalized.getX() * normalized.getX();
        this.matValues[5] = 1.0f - ((z + x3) * 2.0f);
        float y3 = normalized.getY() * normalized.getZ();
        float x4 = normalized.getX() * normalized.getW();
        float[] fArr2 = this.matValues;
        fArr2[9] = (y3 - x4) * 2.0f;
        fArr2[2] = (x2 - y2) * 2.0f;
        fArr2[6] = (y3 + x4) * 2.0f;
        fArr2[10] = 1.0f - ((x3 + y) * 2.0f);
    }

    public void makeScale(Vector3 vector3) {
        set(IDENTITY_VALUES, 0);
        this.matValues[0] = vector3.getX();
        this.matValues[5] = vector3.getY();
        this.matValues[10] = vector3.getZ();
    }

    public Vector3 extractTranslation() {
        float[] fArr = this.matValues;
        return new Vector3(fArr[12], fArr[13], fArr[14]);
    }

    public Vector3 extractScale() {
        float[] fArr = this.matValues;
        float length = Vector3.length(fArr[0], fArr[1], fArr[2]);
        float[] fArr2 = this.matValues;
        float length2 = Vector3.length(fArr2[4], fArr2[5], fArr2[6]);
        float[] fArr3 = this.matValues;
        return new Vector3(length, length2, Vector3.length(fArr3[8], fArr3[9], fArr3[10]));
    }

    public void extractRotation(Matrix4x4 matrix4x4) {
        Vector3 extractScale = extractScale();
        float x = extractScale.getX();
        if (Math.abs(x) > 1.0E-6f) {
            for (int i = 0; i < 3; i++) {
                matrix4x4.matValues[i] = this.matValues[i] / x;
            }
        }
        matrix4x4.matValues[3] = 0.0f;
        float y = extractScale.getY();
        if (Math.abs(y) > 1.0E-6f) {
            for (int i2 = 4; i2 < 7; i2++) {
                matrix4x4.matValues[i2] = this.matValues[i2] / y;
            }
        }
        matrix4x4.matValues[7] = 0.0f;
        float z = extractScale.getZ();
        if (Math.abs(z) > 1.0E-6f) {
            for (int i3 = 8; i3 < 11; i3++) {
                matrix4x4.matValues[i3] = this.matValues[i3] / z;
            }
        }
        float[] fArr = matrix4x4.matValues;
        fArr[11] = 0.0f;
        fArr[12] = 0.0f;
        fArr[13] = 0.0f;
        fArr[14] = 0.0f;
        fArr[15] = 0.0f;
    }

    public Quaternion extractQuaternion() {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        float[] fArr = this.matValues;
        float f8 = fArr[0] + fArr[5] + fArr[10];
        if (f8 > 0.0f) {
            float sqrt = (float) (Math.sqrt(((double) f8) + 1.0d) * 2.0d);
            float[] fArr2 = this.matValues;
            f3 = (fArr2[6] - fArr2[9]) / sqrt;
            f2 = (fArr2[8] - fArr2[2]) / sqrt;
            f = (fArr2[1] - fArr2[4]) / sqrt;
            f4 = sqrt * 0.25f;
        } else {
            if (fArr[0] <= fArr[5] || fArr[0] <= fArr[10]) {
                float[] fArr3 = this.matValues;
                if (fArr3[5] > fArr3[10]) {
                    f6 = (float) (Math.sqrt((double) (((fArr3[5] + 1.0f) - fArr3[0]) - fArr3[10])) * 2.0d);
                    float[] fArr4 = this.matValues;
                    f3 = (fArr4[4] + fArr4[1]) / f6;
                    f2 = f6 * 0.25f;
                    f = (fArr4[9] + fArr4[6]) / f6;
                    f5 = fArr4[8];
                    f7 = fArr4[2];
                } else {
                    f6 = (float) (Math.sqrt((double) (((fArr3[10] + 1.0f) - fArr3[0]) - fArr3[5])) * 2.0d);
                    float[] fArr5 = this.matValues;
                    f3 = (fArr5[8] + fArr5[2]) / f6;
                    f2 = (fArr5[9] + fArr5[6]) / f6;
                    f = f6 * 0.25f;
                    f5 = fArr5[1];
                    f7 = fArr5[4];
                }
            } else {
                f6 = (float) (Math.sqrt((double) (((fArr[0] + 1.0f) - fArr[5]) - fArr[10])) * 2.0d);
                f3 = f6 * 0.25f;
                float[] fArr6 = this.matValues;
                f2 = (fArr6[4] + fArr6[1]) / f6;
                f = (fArr6[8] + fArr6[2]) / f6;
                f5 = fArr6[6];
                f7 = fArr6[9];
            }
            f4 = (f5 - f7) / f6;
        }
        return Quaternion.normalize(f3, f2, f, f4);
    }

    public static void multiply(Matrix4x4 matrix4x4, Matrix4x4 matrix4x42, Matrix4x4 matrix4x43) {
        float[] fArr = new float[16];
        for (int i = 0; i < 4; i++) {
            int i2 = 0;
            int i3 = 0;
            while (i2 < 4) {
                int i4 = i3;
                for (int i5 = 0; i5 < 4; i5++) {
                    fArr[i4] = fArr[i4] + (matrix4x4.matValues[(i * 4) + i5] * matrix4x42.matValues[(i2 * 4) + i]);
                    i4++;
                }
                i2++;
                i3 = i4;
            }
        }
        matrix4x43.set(fArr, 0);
    }

    public static boolean invert(Matrix4x4 matrix4x4, Matrix4x4 matrix4x42) {
        float[] fArr = matrix4x4.matValues;
        float[] fArr2 = matrix4x42.matValues;
        int i = 0;
        fArr2[0] = ((((((fArr[5] * fArr[10]) * fArr[15]) - ((fArr[5] * fArr[11]) * fArr[14])) - ((fArr[9] * fArr[6]) * fArr[15])) + ((fArr[9] * fArr[7]) * fArr[14])) + ((fArr[13] * fArr[6]) * fArr[11])) - ((fArr[13] * fArr[7]) * fArr[10]);
        fArr2[4] = (((((((-fArr[4]) * fArr[10]) * fArr[15]) + ((fArr[4] * fArr[11]) * fArr[14])) + ((fArr[8] * fArr[6]) * fArr[15])) - ((fArr[8] * fArr[7]) * fArr[14])) - ((fArr[12] * fArr[6]) * fArr[11])) + (fArr[12] * fArr[7] * fArr[10]);
        fArr2[8] = ((((((fArr[4] * fArr[9]) * fArr[15]) - ((fArr[4] * fArr[11]) * fArr[13])) - ((fArr[8] * fArr[5]) * fArr[15])) + ((fArr[8] * fArr[7]) * fArr[13])) + ((fArr[12] * fArr[5]) * fArr[11])) - ((fArr[12] * fArr[7]) * fArr[9]);
        fArr2[12] = (((((((-fArr[4]) * fArr[9]) * fArr[14]) + ((fArr[4] * fArr[10]) * fArr[13])) + ((fArr[8] * fArr[5]) * fArr[14])) - ((fArr[8] * fArr[6]) * fArr[13])) - ((fArr[12] * fArr[5]) * fArr[10])) + (fArr[12] * fArr[6] * fArr[9]);
        fArr2[1] = (((((((-fArr[1]) * fArr[10]) * fArr[15]) + ((fArr[1] * fArr[11]) * fArr[14])) + ((fArr[9] * fArr[2]) * fArr[15])) - ((fArr[9] * fArr[3]) * fArr[14])) - ((fArr[13] * fArr[2]) * fArr[11])) + (fArr[13] * fArr[3] * fArr[10]);
        fArr2[5] = ((((((fArr[0] * fArr[10]) * fArr[15]) - ((fArr[0] * fArr[11]) * fArr[14])) - ((fArr[8] * fArr[2]) * fArr[15])) + ((fArr[8] * fArr[3]) * fArr[14])) + ((fArr[12] * fArr[2]) * fArr[11])) - ((fArr[12] * fArr[3]) * fArr[10]);
        fArr2[9] = (((((((-fArr[0]) * fArr[9]) * fArr[15]) + ((fArr[0] * fArr[11]) * fArr[13])) + ((fArr[8] * fArr[1]) * fArr[15])) - ((fArr[8] * fArr[3]) * fArr[13])) - ((fArr[12] * fArr[1]) * fArr[11])) + (fArr[12] * fArr[3] * fArr[9]);
        fArr2[13] = ((((((fArr[0] * fArr[9]) * fArr[14]) - ((fArr[0] * fArr[10]) * fArr[13])) - ((fArr[8] * fArr[1]) * fArr[14])) + ((fArr[8] * fArr[2]) * fArr[13])) + ((fArr[12] * fArr[1]) * fArr[10])) - ((fArr[12] * fArr[2]) * fArr[9]);
        fArr2[2] = ((((((fArr[1] * fArr[6]) * fArr[15]) - ((fArr[1] * fArr[7]) * fArr[14])) - ((fArr[5] * fArr[2]) * fArr[15])) + ((fArr[5] * fArr[3]) * fArr[14])) + ((fArr[13] * fArr[2]) * fArr[7])) - ((fArr[13] * fArr[3]) * fArr[6]);
        fArr2[6] = (((((((-fArr[0]) * fArr[6]) * fArr[15]) + ((fArr[0] * fArr[7]) * fArr[14])) + ((fArr[4] * fArr[2]) * fArr[15])) - ((fArr[4] * fArr[3]) * fArr[14])) - ((fArr[12] * fArr[2]) * fArr[7])) + (fArr[12] * fArr[3] * fArr[6]);
        fArr2[10] = ((((((fArr[0] * fArr[5]) * fArr[15]) - ((fArr[0] * fArr[7]) * fArr[13])) - ((fArr[4] * fArr[1]) * fArr[15])) + ((fArr[4] * fArr[3]) * fArr[13])) + ((fArr[12] * fArr[1]) * fArr[7])) - ((fArr[12] * fArr[3]) * fArr[5]);
        fArr2[14] = (((((((-fArr[0]) * fArr[5]) * fArr[14]) + ((fArr[0] * fArr[6]) * fArr[13])) + ((fArr[4] * fArr[1]) * fArr[14])) - ((fArr[4] * fArr[2]) * fArr[13])) - ((fArr[12] * fArr[1]) * fArr[6])) + (fArr[12] * fArr[2] * fArr[5]);
        fArr2[3] = (((((((-fArr[1]) * fArr[6]) * fArr[11]) + ((fArr[1] * fArr[7]) * fArr[10])) + ((fArr[5] * fArr[2]) * fArr[11])) - ((fArr[5] * fArr[3]) * fArr[10])) - ((fArr[9] * fArr[2]) * fArr[7])) + (fArr[9] * fArr[3] * fArr[6]);
        fArr2[7] = ((((((fArr[0] * fArr[6]) * fArr[11]) - ((fArr[0] * fArr[7]) * fArr[10])) - ((fArr[4] * fArr[2]) * fArr[11])) + ((fArr[4] * fArr[3]) * fArr[10])) + ((fArr[8] * fArr[2]) * fArr[7])) - ((fArr[8] * fArr[3]) * fArr[6]);
        fArr2[11] = (((((((-fArr[0]) * fArr[5]) * fArr[11]) + ((fArr[0] * fArr[7]) * fArr[9])) + ((fArr[4] * fArr[1]) * fArr[11])) - ((fArr[4] * fArr[3]) * fArr[9])) - ((fArr[8] * fArr[1]) * fArr[7])) + (fArr[8] * fArr[3] * fArr[5]);
        fArr2[15] = ((((((fArr[0] * fArr[5]) * fArr[10]) - ((fArr[0] * fArr[6]) * fArr[9])) - ((fArr[4] * fArr[1]) * fArr[10])) + ((fArr[4] * fArr[2]) * fArr[9])) + ((fArr[8] * fArr[1]) * fArr[6])) - ((fArr[8] * fArr[2]) * fArr[5]);
        float f = (fArr[0] * fArr2[0]) + (fArr[1] * fArr2[4]) + (fArr[2] * fArr2[8]) + (fArr[3] * fArr2[12]);
        if (Math.abs(f) < 1.0E-6f) {
            return false;
        }
        float f2 = 1.0f / f;
        while (true) {
            float[] fArr3 = matrix4x42.matValues;
            if (i >= fArr3.length) {
                return true;
            }
            fArr3[i] = fArr3[i] * f2;
            i++;
        }
    }

    public Vector3 transformPoint(Vector3 vector3) {
        float x = vector3.getX();
        float y = vector3.getY();
        float z = vector3.getZ();
        float[] fArr = this.matValues;
        return new Vector3((fArr[0] * x) + (fArr[4] * y) + (fArr[8] * z) + fArr[12], (fArr[1] * x) + (fArr[5] * y) + (fArr[9] * z) + fArr[13], (fArr[2] * x) + (fArr[6] * y) + (fArr[10] * z) + fArr[14]);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Matrix4x4) {
            return Arrays.equals(this.matValues, ((Matrix4x4) obj).matValues);
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.matValues);
    }

    public String toString() {
        return Arrays.toString(this.matValues);
    }
}
