package com.huawei.agpengine.math;

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
    private final float[] values = new float[16];

    public static Matrix4x4 identity() {
        Matrix4x4 mat = new Matrix4x4();
        mat.set(IDENTITY_VALUES, 0);
        return mat;
    }

    public float get(int index) {
        return this.values[index];
    }

    public float get(int row, int column) {
        return this.values[(row * 4) + column];
    }

    public void set(int index, float value) {
        this.values[index] = value;
    }

    public void set(int row, int column, float value) {
        this.values[(row * 4) + column] = value;
    }

    public void set(float[] dataIn, int offset) {
        if (dataIn == null) {
            throw new NullPointerException("dataIn must no be null.");
        } else if (dataIn.length - offset >= 16) {
            System.arraycopy(dataIn, offset, this.values, 0, 16);
        } else {
            throw new IllegalArgumentException("Matrix values must have at least 16 components starting from offset");
        }
    }

    public float[] getData() {
        return this.values;
    }

    public void makeTranslation(Vector3 translation) {
        set(IDENTITY_VALUES, 0);
        this.values[12] = translation.getX();
        this.values[13] = translation.getY();
        this.values[14] = translation.getZ();
    }

    public void makeRotation(Quaternion rotation) {
        set(IDENTITY_VALUES, 0);
        Quaternion rotationNorm = rotation.getNormalized();
        float yy = rotationNorm.getY() * rotationNorm.getY();
        float zz = rotationNorm.getZ() * rotationNorm.getZ();
        this.values[0] = 1.0f - ((yy + zz) * QUAT_CONVERSION_MULTIPLIER);
        float xy = rotationNorm.getX() * rotationNorm.getY();
        float zw = rotationNorm.getZ() * rotationNorm.getW();
        this.values[4] = (xy - zw) * QUAT_CONVERSION_MULTIPLIER;
        float xz = rotationNorm.getX() * rotationNorm.getZ();
        float yw = rotationNorm.getY() * rotationNorm.getW();
        float[] fArr = this.values;
        fArr[8] = (xz + yw) * QUAT_CONVERSION_MULTIPLIER;
        fArr[1] = (xy + zw) * QUAT_CONVERSION_MULTIPLIER;
        float xx = rotationNorm.getX() * rotationNorm.getX();
        this.values[5] = 1.0f - ((xx + zz) * QUAT_CONVERSION_MULTIPLIER);
        float yz = rotationNorm.getY() * rotationNorm.getZ();
        float xw = rotationNorm.getX() * rotationNorm.getW();
        float[] fArr2 = this.values;
        fArr2[M12] = (yz - xw) * QUAT_CONVERSION_MULTIPLIER;
        fArr2[2] = (xz - yw) * QUAT_CONVERSION_MULTIPLIER;
        fArr2[M21] = (yz + xw) * QUAT_CONVERSION_MULTIPLIER;
        fArr2[10] = 1.0f - ((xx + yy) * QUAT_CONVERSION_MULTIPLIER);
    }

    public void makeScale(Vector3 scale) {
        set(IDENTITY_VALUES, 0);
        this.values[0] = scale.getX();
        this.values[5] = scale.getY();
        this.values[10] = scale.getZ();
    }

    public Vector3 extractTranslation() {
        float[] fArr = this.values;
        return new Vector3(fArr[12], fArr[13], fArr[14]);
    }

    public Vector3 extractScale() {
        float[] fArr = this.values;
        float length = Vector3.length(fArr[0], fArr[1], fArr[2]);
        float[] fArr2 = this.values;
        float length2 = Vector3.length(fArr2[4], fArr2[5], fArr2[M21]);
        float[] fArr3 = this.values;
        return new Vector3(length, length2, Vector3.length(fArr3[8], fArr3[M12], fArr3[10]));
    }

    public void extractRotation(Matrix4x4 rotationOut) {
        Vector3 scale = extractScale();
        float scaleX = scale.getX();
        if (Math.abs(scaleX) > EPSILON) {
            for (int i = 0; i < 3; i++) {
                rotationOut.values[i] = this.values[i] / scaleX;
            }
        }
        rotationOut.values[3] = 0.0f;
        float scaleY = scale.getY();
        if (Math.abs(scaleY) > EPSILON) {
            for (int i2 = 4; i2 < M31; i2++) {
                rotationOut.values[i2] = this.values[i2] / scaleY;
            }
        }
        rotationOut.values[M31] = 0.0f;
        float scaleZ = scale.getZ();
        if (Math.abs(scaleZ) > EPSILON) {
            for (int i3 = 8; i3 < M32; i3++) {
                rotationOut.values[i3] = this.values[i3] / scaleZ;
            }
        }
        float[] fArr = rotationOut.values;
        fArr[M32] = 0.0f;
        fArr[12] = 0.0f;
        fArr[13] = 0.0f;
        fArr[14] = 0.0f;
        fArr[15] = 0.0f;
    }

    public Quaternion extractQuaternion() {
        float rotationW;
        float rotationZ;
        float rotationY;
        float rotationX;
        float[] fArr = this.values;
        float tr = fArr[0] + fArr[5] + fArr[10];
        if (tr > 0.0f) {
            float scalar = (float) (Math.sqrt(((double) tr) + 1.0d) * 2.0d);
            float[] fArr2 = this.values;
            rotationX = (fArr2[M21] - fArr2[M12]) / scalar;
            rotationY = (fArr2[8] - fArr2[2]) / scalar;
            rotationZ = (fArr2[1] - fArr2[4]) / scalar;
            rotationW = 0.25f * scalar;
        } else if (fArr[0] <= fArr[5] || fArr[0] <= fArr[10]) {
            float[] fArr3 = this.values;
            if (fArr3[5] > fArr3[10]) {
                float scalar2 = (float) (Math.sqrt((double) (((fArr3[5] + 1.0f) - fArr3[0]) - fArr3[10])) * 2.0d);
                float[] fArr4 = this.values;
                rotationX = (fArr4[4] + fArr4[1]) / scalar2;
                rotationY = scalar2 * 0.25f;
                rotationZ = (fArr4[M12] + fArr4[M21]) / scalar2;
                rotationW = (fArr4[8] - fArr4[2]) / scalar2;
            } else {
                float scalar3 = (float) (Math.sqrt((double) (((fArr3[10] + 1.0f) - fArr3[0]) - fArr3[5])) * 2.0d);
                float[] fArr5 = this.values;
                rotationX = (fArr5[8] + fArr5[2]) / scalar3;
                rotationY = (fArr5[M12] + fArr5[M21]) / scalar3;
                rotationZ = scalar3 * 0.25f;
                rotationW = (fArr5[1] - fArr5[4]) / scalar3;
            }
        } else {
            float scalar4 = (float) (Math.sqrt((double) (((fArr[0] + 1.0f) - fArr[5]) - fArr[10])) * 2.0d);
            rotationX = scalar4 * 0.25f;
            float[] fArr6 = this.values;
            rotationY = (fArr6[4] + fArr6[1]) / scalar4;
            rotationZ = (fArr6[8] + fArr6[2]) / scalar4;
            rotationW = (fArr6[M21] - fArr6[M12]) / scalar4;
        }
        return Quaternion.normalize(rotationX, rotationY, rotationZ, rotationW);
    }

    public static void multiply(Matrix4x4 lhs, Matrix4x4 rhs, Matrix4x4 product) {
        float[] outArray = new float[16];
        for (int row = 0; row < 4; row++) {
            int index = 0;
            for (int column = 0; column < 4; column++) {
                for (int column2 = 0; column2 < 4; column2++) {
                    outArray[index] = outArray[index] + (lhs.values[(row * 4) + column2] * rhs.values[(column * 4) + row]);
                    index++;
                }
            }
        }
        product.set(outArray, 0);
    }

    public static boolean invert(Matrix4x4 inputMatrix, Matrix4x4 out) {
        float[] vs = inputMatrix.values;
        float[] fArr = out.values;
        fArr[0] = ((((((vs[5] * vs[10]) * vs[15]) - ((vs[5] * vs[M32]) * vs[14])) - ((vs[M12] * vs[M21]) * vs[15])) + ((vs[M12] * vs[M31]) * vs[14])) + ((vs[13] * vs[M21]) * vs[M32])) - ((vs[13] * vs[M31]) * vs[10]);
        fArr[4] = (((((((-vs[4]) * vs[10]) * vs[15]) + ((vs[4] * vs[M32]) * vs[14])) + ((vs[8] * vs[M21]) * vs[15])) - ((vs[8] * vs[M31]) * vs[14])) - ((vs[12] * vs[M21]) * vs[M32])) + (vs[12] * vs[M31] * vs[10]);
        fArr[8] = ((((((vs[4] * vs[M12]) * vs[15]) - ((vs[4] * vs[M32]) * vs[13])) - ((vs[8] * vs[5]) * vs[15])) + ((vs[8] * vs[M31]) * vs[13])) + ((vs[12] * vs[5]) * vs[M32])) - ((vs[12] * vs[M31]) * vs[M12]);
        fArr[12] = (((((((-vs[4]) * vs[M12]) * vs[14]) + ((vs[4] * vs[10]) * vs[13])) + ((vs[8] * vs[5]) * vs[14])) - ((vs[8] * vs[M21]) * vs[13])) - ((vs[12] * vs[5]) * vs[10])) + (vs[12] * vs[M21] * vs[M12]);
        fArr[1] = (((((((-vs[1]) * vs[10]) * vs[15]) + ((vs[1] * vs[M32]) * vs[14])) + ((vs[M12] * vs[2]) * vs[15])) - ((vs[M12] * vs[3]) * vs[14])) - ((vs[13] * vs[2]) * vs[M32])) + (vs[13] * vs[3] * vs[10]);
        fArr[5] = ((((((vs[0] * vs[10]) * vs[15]) - ((vs[0] * vs[M32]) * vs[14])) - ((vs[8] * vs[2]) * vs[15])) + ((vs[8] * vs[3]) * vs[14])) + ((vs[12] * vs[2]) * vs[M32])) - ((vs[12] * vs[3]) * vs[10]);
        fArr[M12] = (((((((-vs[0]) * vs[M12]) * vs[15]) + ((vs[0] * vs[M32]) * vs[13])) + ((vs[8] * vs[1]) * vs[15])) - ((vs[8] * vs[3]) * vs[13])) - ((vs[12] * vs[1]) * vs[M32])) + (vs[12] * vs[3] * vs[M12]);
        fArr[13] = ((((((vs[0] * vs[M12]) * vs[14]) - ((vs[0] * vs[10]) * vs[13])) - ((vs[8] * vs[1]) * vs[14])) + ((vs[8] * vs[2]) * vs[13])) + ((vs[12] * vs[1]) * vs[10])) - ((vs[12] * vs[2]) * vs[M12]);
        fArr[2] = ((((((vs[1] * vs[M21]) * vs[15]) - ((vs[1] * vs[M31]) * vs[14])) - ((vs[5] * vs[2]) * vs[15])) + ((vs[5] * vs[3]) * vs[14])) + ((vs[13] * vs[2]) * vs[M31])) - ((vs[13] * vs[3]) * vs[M21]);
        fArr[M21] = (((((((-vs[0]) * vs[M21]) * vs[15]) + ((vs[0] * vs[M31]) * vs[14])) + ((vs[4] * vs[2]) * vs[15])) - ((vs[4] * vs[3]) * vs[14])) - ((vs[12] * vs[2]) * vs[M31])) + (vs[12] * vs[3] * vs[M21]);
        fArr[10] = ((((((vs[0] * vs[5]) * vs[15]) - ((vs[0] * vs[M31]) * vs[13])) - ((vs[4] * vs[1]) * vs[15])) + ((vs[4] * vs[3]) * vs[13])) + ((vs[12] * vs[1]) * vs[M31])) - ((vs[12] * vs[3]) * vs[5]);
        fArr[14] = (((((((-vs[0]) * vs[5]) * vs[14]) + ((vs[0] * vs[M21]) * vs[13])) + ((vs[4] * vs[1]) * vs[14])) - ((vs[4] * vs[2]) * vs[13])) - ((vs[12] * vs[1]) * vs[M21])) + (vs[12] * vs[2] * vs[5]);
        fArr[3] = (((((((-vs[1]) * vs[M21]) * vs[M32]) + ((vs[1] * vs[M31]) * vs[10])) + ((vs[5] * vs[2]) * vs[M32])) - ((vs[5] * vs[3]) * vs[10])) - ((vs[M12] * vs[2]) * vs[M31])) + (vs[M12] * vs[3] * vs[M21]);
        fArr[M31] = ((((((vs[0] * vs[M21]) * vs[M32]) - ((vs[0] * vs[M31]) * vs[10])) - ((vs[4] * vs[2]) * vs[M32])) + ((vs[4] * vs[3]) * vs[10])) + ((vs[8] * vs[2]) * vs[M31])) - ((vs[8] * vs[3]) * vs[M21]);
        fArr[M32] = (((((((-vs[0]) * vs[5]) * vs[M32]) + ((vs[0] * vs[M31]) * vs[M12])) + ((vs[4] * vs[1]) * vs[M32])) - ((vs[4] * vs[3]) * vs[M12])) - ((vs[8] * vs[1]) * vs[M31])) + (vs[8] * vs[3] * vs[5]);
        fArr[15] = ((((((vs[0] * vs[5]) * vs[10]) - ((vs[0] * vs[M21]) * vs[M12])) - ((vs[4] * vs[1]) * vs[10])) + ((vs[4] * vs[2]) * vs[M12])) + ((vs[8] * vs[1]) * vs[M21])) - ((vs[8] * vs[2]) * vs[5]);
        float det = (vs[0] * fArr[0]) + (vs[1] * fArr[4]) + (vs[2] * fArr[8]) + (vs[3] * fArr[12]);
        if (Math.abs(det) < EPSILON) {
            return false;
        }
        float det2 = 1.0f / det;
        int i = 0;
        while (true) {
            float[] fArr2 = out.values;
            if (i >= fArr2.length) {
                return true;
            }
            fArr2[i] = fArr2[i] * det2;
            i++;
        }
    }

    public Vector3 transformPoint(Vector3 point) {
        float pointX = point.getX();
        float pointY = point.getY();
        float pointZ = point.getZ();
        float[] fArr = this.values;
        return new Vector3((fArr[0] * pointX) + (fArr[4] * pointY) + (fArr[8] * pointZ) + fArr[12], (fArr[1] * pointX) + (fArr[5] * pointY) + (fArr[M12] * pointZ) + fArr[13], (fArr[2] * pointX) + (fArr[M21] * pointY) + (fArr[10] * pointZ) + fArr[14]);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Matrix4x4) {
            return Arrays.equals(this.values, ((Matrix4x4) other).values);
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    public String toString() {
        return Arrays.toString(this.values);
    }
}
