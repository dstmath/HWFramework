package ohos.agp.render.opengl;

public final class Matrix {
    private static final int MATRIX_MIN_SIZE = 16;
    private static final float[] MATRIX_TEMP = new float[32];
    private static final int VECTOR_MIN_SIZE = 4;

    public static class Result {
        private int offset;
        private float[] resultMatrix;

        public Result(float[] fArr, int i) {
            this.resultMatrix = fArr;
            this.offset = i;
        }

        public void setResultMatrix(float[] fArr) {
            this.resultMatrix = fArr;
        }

        public void setOffset(int i) {
            this.offset = i;
        }

        public float[] getResultMatrix() {
            return this.resultMatrix;
        }

        public int getOffset() {
            return this.offset;
        }
    }

    public static class Distance {
        private float far;
        private float near;

        public Distance(float f, float f2) {
            this.near = f;
            this.far = f2;
        }

        public void setNear(float f) {
            this.near = f;
        }

        public void setFar(float f) {
            this.far = f;
        }

        public float getNear() {
            return this.near;
        }

        public float getFar() {
            return this.far;
        }
    }

    public static class InputMatrix {
        private float[] inputMatrix;
        private int offset;

        public InputMatrix(float[] fArr, int i) {
            this.inputMatrix = fArr;
            this.offset = i;
        }

        public void setInputMatrix(float[] fArr) {
            this.inputMatrix = fArr;
        }

        public void setOffset(int i) {
            this.offset = i;
        }

        public float[] getInputMatrix() {
            return this.inputMatrix;
        }

        public int getOffset() {
            return this.offset;
        }
    }

    public static class Position {
        private float positionX;
        private float positionY;
        private float positionZ;

        public Position(float f, float f2, float f3) {
            this.positionX = f;
            this.positionY = f2;
            this.positionZ = f3;
        }

        public void setPositionX(float f) {
            this.positionX = f;
        }

        public void setPositionY(float f) {
            this.positionY = f;
        }

        public void setPositionZ(float f) {
            this.positionZ = f;
        }

        public float getPositionX() {
            return this.positionX;
        }

        public float getPositionY() {
            return this.positionY;
        }

        public float getPositionZ() {
            return this.positionZ;
        }
    }

    public static class EyeView {
        private float eyeX;
        private float eyeY;
        private float eyeZ;

        public EyeView(float f, float f2, float f3) {
            this.eyeX = f;
            this.eyeY = f2;
            this.eyeZ = f3;
        }

        public void setX(float f) {
            this.eyeX = f;
        }

        public void setY(float f) {
            this.eyeY = f;
        }

        public void setZ(float f) {
            this.eyeZ = f;
        }

        public float getX() {
            return this.eyeX;
        }

        public float getY() {
            return this.eyeY;
        }

        public float getZ() {
            return this.eyeZ;
        }
    }

    public static class CenterView {
        private float centerX;
        private float centerY;
        private float centerZ;

        public CenterView(float f, float f2, float f3) {
            this.centerX = f;
            this.centerY = f2;
            this.centerZ = f3;
        }

        public void setX(float f) {
            this.centerX = f;
        }

        public void setY(float f) {
            this.centerY = f;
        }

        public void setZ(float f) {
            this.centerZ = f;
        }

        public float getX() {
            return this.centerX;
        }

        public float getY() {
            return this.centerY;
        }

        public float getZ() {
            return this.centerZ;
        }
    }

    public static class UpView {
        private float upX;
        private float upY;
        private float upZ;

        public UpView(float f, float f2, float f3) {
            this.upX = f;
            this.upY = f2;
            this.upZ = f3;
        }

        public void setX(float f) {
            this.upX = f;
        }

        public void setY(float f) {
            this.upY = f;
        }

        public void setZ(float f) {
            this.upZ = f;
        }

        public float getX() {
            return this.upX;
        }

        public float getY() {
            return this.upY;
        }

        public float getZ() {
            return this.upZ;
        }
    }

    public static class Translate {
        private float transX;
        private float transY;
        private float transZ;

        public Translate(float f, float f2, float f3) {
            this.transX = f;
            this.transY = f2;
            this.transZ = f3;
        }

        public void setX(float f) {
            this.transX = f;
        }

        public void setY(float f) {
            this.transY = f;
        }

        public void setZ(float f) {
            this.transZ = f;
        }

        public float getX() {
            return this.transX;
        }

        public float getY() {
            return this.transY;
        }

        public float getZ() {
            return this.transZ;
        }
    }

    public static class Scale {
        private float scaleX;
        private float scaleY;
        private float scaleZ;

        public Scale(float f, float f2, float f3) {
            this.scaleX = f;
            this.scaleY = f2;
            this.scaleZ = f3;
        }

        public void setX(float f) {
            this.scaleX = f;
        }

        public void setY(float f) {
            this.scaleY = f;
        }

        public void setZ(float f) {
            this.scaleZ = f;
        }

        public float getX() {
            return this.scaleX;
        }

        public float getY() {
            return this.scaleY;
        }

        public float getZ() {
            return this.scaleZ;
        }
    }

    public static class RectFloat {
        private float bottom;
        private float left;
        private float right;
        private float top;

        public RectFloat(float f, float f2, float f3, float f4) {
            this.left = f;
            this.right = f2;
            this.top = f3;
            this.bottom = f4;
        }

        public void setLeft(float f) {
            this.left = f;
        }

        public void setRight(float f) {
            this.right = f;
        }

        public void setTop(float f) {
            this.top = f;
        }

        public void setBottom(float f) {
            this.bottom = f;
        }

        public float getLeft() {
            return this.left;
        }

        public float getRight() {
            return this.right;
        }

        public float getTop() {
            return this.top;
        }

        public float getBottom() {
            return this.bottom;
        }
    }

    public static class MatrixException extends RuntimeException {
        private static final long serialVersionUID = -4179802232673652667L;

        public MatrixException(String str) {
            super(str);
        }
    }

    public static void adjustProjectionMatrixByPlanes(Result result, RectFloat rectFloat, Distance distance) throws MatrixException {
        float left = rectFloat.getLeft();
        float right = rectFloat.getRight();
        float top = rectFloat.getTop();
        float bottom = rectFloat.getBottom();
        float near = distance.getNear();
        float far = distance.getFar();
        if (Utils.nearEqual(left, right)) {
            throw new MatrixException("left == right");
        } else if (Utils.nearEqual(top, bottom)) {
            throw new MatrixException("top == bottom");
        } else if (Utils.nearEqual(near, far)) {
            throw new MatrixException("near == far");
        } else if (Utils.lessOrEqual(near, 0.0f)) {
            throw new MatrixException("near <= 0.0f");
        } else if (!Utils.lessOrEqual(far, 0.0f)) {
            float f = 1.0f / (right - left);
            float f2 = near * f * 2.0f;
            float f3 = 1.0f / (top - bottom);
            float f4 = near * f3 * 2.0f;
            float f5 = (right + left) * f;
            float f6 = (top + bottom) * f3;
            float f7 = 1.0f / (near - far);
            float f8 = (far + near) * f7;
            float f9 = far * near * f7 * 2.0f;
            float[] resultMatrix = result.getResultMatrix();
            int offset = result.getOffset();
            if (checkMatrix(resultMatrix, offset, 16)) {
                resultMatrix[offset + 0] = f2;
                resultMatrix[offset + 5] = f4;
                resultMatrix[offset + 8] = f5;
                resultMatrix[offset + 9] = f6;
                resultMatrix[offset + 10] = f8;
                resultMatrix[offset + 14] = f9;
                resultMatrix[offset + 11] = -1.0f;
                resultMatrix[offset + 1] = 0.0f;
                resultMatrix[offset + 2] = 0.0f;
                resultMatrix[offset + 3] = 0.0f;
                resultMatrix[offset + 4] = 0.0f;
                resultMatrix[offset + 6] = 0.0f;
                resultMatrix[offset + 7] = 0.0f;
                resultMatrix[offset + 12] = 0.0f;
                resultMatrix[offset + 13] = 0.0f;
                resultMatrix[offset + 15] = 0.0f;
            }
        } else {
            throw new MatrixException("far <= 0.0f");
        }
    }

    public static void multiplyMatrixToMatrix(Result result, InputMatrix inputMatrix, InputMatrix inputMatrix2) throws MatrixException {
        float[] resultMatrix = result.getResultMatrix();
        int offset = result.getOffset();
        float[] inputMatrix3 = inputMatrix.getInputMatrix();
        int offset2 = inputMatrix.getOffset();
        float[] inputMatrix4 = inputMatrix2.getInputMatrix();
        int offset3 = inputMatrix2.getOffset();
        if (checkMatrix(resultMatrix, offset, 16) && checkMatrix(inputMatrix3, offset2, 16) && checkMatrix(inputMatrix4, offset3, 16)) {
            for (int i = 0; i < 4; i++) {
                int i2 = i * 4;
                int i3 = offset3 + i2;
                float f = inputMatrix4[i3];
                float f2 = inputMatrix3[offset2 + 0] * f;
                float f3 = inputMatrix3[offset2 + 1] * f;
                float f4 = inputMatrix3[offset2 + 2] * f;
                float f5 = inputMatrix3[offset2 + 3] * f;
                for (int i4 = 1; i4 < 4; i4++) {
                    float f6 = inputMatrix4[i3 + i4];
                    int i5 = offset2 + (i4 * 4);
                    f2 += inputMatrix3[i5] * f6;
                    f3 += inputMatrix3[i5 + 1] * f6;
                    f4 += inputMatrix3[i5 + 2] * f6;
                    f5 += inputMatrix3[i5 + 3] * f6;
                }
                int i6 = i2 + offset;
                resultMatrix[i6] = f2;
                resultMatrix[i6 + 1] = f3;
                resultMatrix[i6 + 2] = f4;
                resultMatrix[i6 + 3] = f5;
            }
        }
    }

    public static void adjustOrthographicMatrix(Result result, RectFloat rectFloat, Distance distance) throws MatrixException {
        float left = rectFloat.getLeft();
        float right = rectFloat.getRight();
        float top = rectFloat.getTop();
        float bottom = rectFloat.getBottom();
        float near = distance.getNear();
        float far = distance.getFar();
        if (Utils.nearEqual(left, right)) {
            throw new MatrixException("left == right");
        } else if (Utils.nearEqual(bottom, top)) {
            throw new MatrixException("bottom == top");
        } else if (!Utils.nearEqual(near, far)) {
            float f = 1.0f / (right - left);
            float f2 = f * 2.0f;
            float f3 = 1.0f / (top - bottom);
            float f4 = 2.0f * f3;
            float f5 = 1.0f / (far - near);
            float f6 = -2.0f * f5;
            float f7 = (-(right + left)) * f;
            float f8 = (-(top + bottom)) * f3;
            float f9 = (-(far + near)) * f5;
            float[] resultMatrix = result.getResultMatrix();
            int offset = result.getOffset();
            if (checkMatrix(resultMatrix, offset, 16)) {
                resultMatrix[offset + 0] = f2;
                resultMatrix[offset + 5] = f4;
                resultMatrix[offset + 10] = f6;
                resultMatrix[offset + 12] = f7;
                resultMatrix[offset + 13] = f8;
                resultMatrix[offset + 14] = f9;
                resultMatrix[offset + 15] = 1.0f;
                resultMatrix[offset + 1] = 0.0f;
                resultMatrix[offset + 2] = 0.0f;
                resultMatrix[offset + 3] = 0.0f;
                resultMatrix[offset + 4] = 0.0f;
                resultMatrix[offset + 6] = 0.0f;
                resultMatrix[offset + 7] = 0.0f;
                resultMatrix[offset + 8] = 0.0f;
                resultMatrix[offset + 9] = 0.0f;
                resultMatrix[offset + 11] = 0.0f;
            }
        } else {
            throw new MatrixException("near == far");
        }
    }

    public static void rotateMatrix(Result result, float f, Position position) throws MatrixException {
        float[] resultMatrix = result.getResultMatrix();
        int offset = result.getOffset();
        if (checkMatrix(resultMatrix, offset, 16)) {
            synchronized (MATRIX_TEMP) {
                adjustRotateMatrix(new Result(MATRIX_TEMP, 0), f, position);
                multiplyMatrixToMatrix(new Result(MATRIX_TEMP, 16), new InputMatrix(resultMatrix, offset), new InputMatrix(MATRIX_TEMP, 0));
                System.arraycopy(MATRIX_TEMP, 16, resultMatrix, offset, 16);
            }
        }
    }

    public static void adjustViewMatrixByAngleOfCamera(Result result, EyeView eyeView, CenterView centerView, UpView upView) throws MatrixException {
        float x = eyeView.getX();
        float y = eyeView.getY();
        float z = eyeView.getZ();
        float x2 = centerView.getX();
        float y2 = centerView.getY();
        float z2 = centerView.getZ();
        float x3 = upView.getX();
        float y3 = upView.getY();
        float z3 = upView.getZ();
        float f = x2 - x;
        float f2 = y2 - y;
        float f3 = z2 - z;
        if (!Utils.nearZero(f) || !Utils.nearZero(f2) || !Utils.nearZero(f3)) {
            float length = 1.0f / length(new Position(f, f2, f3));
            float f4 = f * length;
            float f5 = f2 * length;
            float f6 = f3 * length;
            float f7 = (f5 * z3) - (f6 * y3);
            float f8 = (f6 * x3) - (f4 * z3);
            float f9 = (f4 * y3) - (f5 * x3);
            if (!Utils.nearZero(x3) || !Utils.nearZero(y3) || !Utils.nearZero(z3)) {
                float length2 = 1.0f / length(new Position(f7, f8, f9));
                float f10 = f7 * length2;
                float f11 = f8 * length2;
                float f12 = f9 * length2;
                float f13 = (f11 * f6) - (f12 * f5);
                float f14 = (f12 * f4) - (f10 * f6);
                float f15 = (f10 * f5) - (f11 * f4);
                float[] resultMatrix = result.getResultMatrix();
                int offset = result.getOffset();
                if (checkMatrix(resultMatrix, offset, 16)) {
                    resultMatrix[offset + 0] = f10;
                    resultMatrix[offset + 1] = f13;
                    resultMatrix[offset + 2] = -f4;
                    resultMatrix[offset + 3] = 0.0f;
                    resultMatrix[offset + 4] = f11;
                    resultMatrix[offset + 5] = f14;
                    resultMatrix[offset + 6] = -f5;
                    resultMatrix[offset + 7] = 0.0f;
                    resultMatrix[offset + 8] = f12;
                    resultMatrix[offset + 9] = f15;
                    resultMatrix[offset + 10] = -f6;
                    resultMatrix[offset + 11] = 0.0f;
                    resultMatrix[offset + 12] = 0.0f;
                    resultMatrix[offset + 13] = 0.0f;
                    resultMatrix[offset + 14] = 0.0f;
                    resultMatrix[offset + 15] = 1.0f;
                    translateMatrix(new Result(resultMatrix, offset), new Translate(-x, -y, -z));
                    return;
                }
                return;
            }
            throw new MatrixException("upX = 0.0f && upY = 0.0f && upZ = 0.0f");
        }
        throw new MatrixException("centerX = eyeX && centerY = eyeY && centerZ = eyeZ");
    }

    public static void adjustRotateMatrix(Result result, float f, Position position) throws MatrixException {
        float[] resultMatrix = result.getResultMatrix();
        int offset = result.getOffset();
        if (checkMatrix(resultMatrix, offset, 16)) {
            resultMatrix[offset + 3] = 0.0f;
            resultMatrix[offset + 7] = 0.0f;
            resultMatrix[offset + 11] = 0.0f;
            resultMatrix[offset + 12] = 0.0f;
            resultMatrix[offset + 13] = 0.0f;
            resultMatrix[offset + 14] = 0.0f;
            resultMatrix[offset + 15] = 1.0f;
            double d = (double) (f * 0.017453292f);
            float sin = (float) Math.sin(d);
            float cos = (float) Math.cos(d);
            float positionX = position.getPositionX();
            float positionY = position.getPositionY();
            float positionZ = position.getPositionZ();
            if (positionX == 1.0f && positionY == 0.0f && positionZ == 0.0f) {
                resultMatrix[offset + 5] = cos;
                resultMatrix[offset + 10] = cos;
                resultMatrix[offset + 6] = sin;
                resultMatrix[offset + 9] = -sin;
                resultMatrix[offset + 1] = 0.0f;
                resultMatrix[offset + 2] = 0.0f;
                resultMatrix[offset + 4] = 0.0f;
                resultMatrix[offset + 8] = 0.0f;
                resultMatrix[offset + 0] = 1.0f;
                return;
            }
            int i = (positionX > 0.0f ? 1 : (positionX == 0.0f ? 0 : -1));
            if (i == 0 && positionY == 1.0f && positionZ == 0.0f) {
                resultMatrix[offset + 0] = cos;
                resultMatrix[offset + 10] = cos;
                resultMatrix[offset + 8] = sin;
                resultMatrix[offset + 2] = -sin;
                resultMatrix[offset + 1] = 0.0f;
                resultMatrix[offset + 4] = 0.0f;
                resultMatrix[offset + 6] = 0.0f;
                resultMatrix[offset + 9] = 0.0f;
                resultMatrix[offset + 5] = 1.0f;
            } else if (i == 0 && positionY == 0.0f && positionZ == 1.0f) {
                resultMatrix[offset + 0] = cos;
                resultMatrix[offset + 5] = cos;
                resultMatrix[offset + 1] = sin;
                resultMatrix[offset + 4] = -sin;
                resultMatrix[offset + 2] = 0.0f;
                resultMatrix[offset + 6] = 0.0f;
                resultMatrix[offset + 8] = 0.0f;
                resultMatrix[offset + 9] = 0.0f;
                resultMatrix[offset + 10] = 1.0f;
            } else {
                float length = length(new Position(positionX, positionY, positionZ));
                if (length != 1.0f) {
                    float f2 = 1.0f / length;
                    positionX *= f2;
                    positionY *= f2;
                    positionZ *= f2;
                }
                float f3 = 1.0f - cos;
                float f4 = positionX * sin;
                float f5 = positionY * sin;
                float f6 = sin * positionZ;
                resultMatrix[offset + 0] = (positionX * positionX * f3) + cos;
                float f7 = positionX * positionY * f3;
                resultMatrix[offset + 4] = f7 - f6;
                float f8 = positionZ * positionX * f3;
                resultMatrix[offset + 8] = f8 + f5;
                resultMatrix[offset + 1] = f7 + f6;
                resultMatrix[offset + 5] = (positionY * positionY * f3) + cos;
                float f9 = positionY * positionZ * f3;
                resultMatrix[offset + 9] = f9 - f4;
                resultMatrix[offset + 2] = f8 - f5;
                resultMatrix[offset + 6] = f9 + f4;
                resultMatrix[offset + 10] = (positionZ * positionZ * f3) + cos;
            }
        }
    }

    public static void translateMatrix(Result result, Translate translate) throws MatrixException {
        float[] resultMatrix = result.getResultMatrix();
        int offset = result.getOffset();
        if (checkMatrix(resultMatrix, offset, 16)) {
            float x = translate.getX();
            float y = translate.getY();
            float z = translate.getZ();
            for (int i = 0; i < 4; i++) {
                int i2 = offset + i;
                int i3 = i2 + 12;
                resultMatrix[i3] = resultMatrix[i3] + (resultMatrix[i2] * x) + (resultMatrix[i2 + 4] * y) + (resultMatrix[i2 + 8] * z);
            }
        }
    }

    public static boolean invertMatrix(Result result, InputMatrix inputMatrix) throws MatrixException {
        float[] inputMatrix2 = inputMatrix.getInputMatrix();
        int offset = inputMatrix.getOffset();
        float[] resultMatrix = result.getResultMatrix();
        int offset2 = result.getOffset();
        if (!checkMatrix(inputMatrix2, offset, 16) || !checkMatrix(resultMatrix, offset2, 16)) {
            return true;
        }
        float f = inputMatrix2[offset];
        float f2 = inputMatrix2[offset + 1];
        float f3 = inputMatrix2[offset + 2];
        float f4 = inputMatrix2[offset + 3];
        float f5 = inputMatrix2[offset + 4];
        float f6 = inputMatrix2[offset + 5];
        float f7 = inputMatrix2[offset + 6];
        float f8 = inputMatrix2[offset + 7];
        float f9 = inputMatrix2[offset + 8];
        float f10 = inputMatrix2[offset + 9];
        float f11 = inputMatrix2[offset + 10];
        float f12 = inputMatrix2[offset + 11];
        float f13 = inputMatrix2[offset + 12];
        float f14 = inputMatrix2[offset + 13];
        float f15 = inputMatrix2[offset + 14];
        float f16 = inputMatrix2[offset + 15];
        float f17 = f11 * f16;
        float f18 = f15 * f12;
        float f19 = f7 * f16;
        float f20 = f15 * f8;
        float f21 = f7 * f12;
        float f22 = f11 * f8;
        float f23 = f3 * f16;
        float f24 = f15 * f4;
        float f25 = f3 * f12;
        float f26 = f11 * f4;
        float f27 = f3 * f8;
        float f28 = f7 * f4;
        float f29 = (((f17 * f6) + (f20 * f10)) + (f21 * f14)) - (((f18 * f6) + (f19 * f10)) + (f22 * f14));
        float f30 = (((f18 * f2) + (f23 * f10)) + (f26 * f14)) - (((f17 * f2) + (f24 * f10)) + (f25 * f14));
        float f31 = (((f19 * f2) + (f24 * f6)) + (f27 * f14)) - (((f20 * f2) + (f23 * f6)) + (f28 * f14));
        float f32 = (((f22 * f2) + (f25 * f6)) + (f28 * f10)) - (((f21 * f2) + (f26 * f6)) + (f27 * f10));
        float f33 = (((f18 * f5) + (f19 * f9)) + (f22 * f13)) - (((f17 * f5) + (f20 * f9)) + (f21 * f13));
        float f34 = (((f17 * f) + (f24 * f9)) + (f25 * f13)) - (((f18 * f) + (f23 * f9)) + (f26 * f13));
        float f35 = (((f20 * f) + (f23 * f5)) + (f28 * f13)) - (((f19 * f) + (f24 * f5)) + (f27 * f13));
        float f36 = (((f21 * f) + (f26 * f5)) + (f27 * f9)) - (((f22 * f) + (f25 * f5)) + (f28 * f9));
        float f37 = f9 * f14;
        float f38 = f13 * f10;
        float f39 = f5 * f14;
        float f40 = f13 * f6;
        float f41 = f5 * f10;
        float f42 = f9 * f6;
        float f43 = f14 * f;
        float f44 = f13 * f2;
        float f45 = f10 * f;
        float f46 = f9 * f2;
        float f47 = f6 * f;
        float f48 = f2 * f5;
        float f49 = (((f37 * f8) + (f40 * f12)) + (f41 * f16)) - (((f38 * f8) + (f39 * f12)) + (f42 * f16));
        float f50 = (((f38 * f4) + (f43 * f12)) + (f46 * f16)) - (((f37 * f4) + (f44 * f12)) + (f45 * f16));
        float f51 = (((f39 * f4) + (f44 * f8)) + (f47 * f16)) - (((f40 * f4) + (f43 * f8)) + (f16 * f48));
        float f52 = (((f42 * f4) + (f45 * f8)) + (f48 * f12)) - (((f4 * f41) + (f8 * f46)) + (f12 * f47));
        float f53 = (((f39 * f11) + (f42 * f15)) + (f38 * f7)) - (((f41 * f15) + (f37 * f7)) + (f40 * f11));
        float f54 = (((f45 * f15) + (f37 * f3)) + (f44 * f11)) - (((f43 * f11) + (f46 * f15)) + (f38 * f3));
        float f55 = (((f43 * f7) + (f48 * f15)) + (f40 * f3)) - (((f15 * f47) + (f39 * f3)) + (f44 * f7));
        float f56 = (((f47 * f11) + (f41 * f3)) + (f46 * f7)) - (((f45 * f7) + (f48 * f11)) + (f42 * f3));
        float f57 = (f * f29) + (f5 * f30) + (f9 * f31) + (f13 * f32);
        if (f57 == 0.0f) {
            return false;
        }
        float f58 = 1.0f / f57;
        resultMatrix[offset2] = f29 * f58;
        resultMatrix[offset2 + 1] = f30 * f58;
        resultMatrix[offset2 + 2] = f31 * f58;
        resultMatrix[offset2 + 3] = f32 * f58;
        resultMatrix[offset2 + 4] = f33 * f58;
        resultMatrix[offset2 + 5] = f34 * f58;
        resultMatrix[offset2 + 6] = f35 * f58;
        resultMatrix[offset2 + 7] = f36 * f58;
        resultMatrix[offset2 + 8] = f49 * f58;
        resultMatrix[offset2 + 9] = f50 * f58;
        resultMatrix[offset2 + 10] = f51 * f58;
        resultMatrix[offset2 + 11] = f52 * f58;
        resultMatrix[offset2 + 12] = f53 * f58;
        resultMatrix[offset2 + 13] = f54 * f58;
        resultMatrix[offset2 + 14] = f55 * f58;
        resultMatrix[offset2 + 15] = f56 * f58;
        return true;
    }

    public static void multiplyMatrixToVector(Result result, InputMatrix inputMatrix, InputMatrix inputMatrix2) throws MatrixException {
        float[] resultMatrix = result.getResultMatrix();
        int offset = result.getOffset();
        float[] inputMatrix3 = inputMatrix.getInputMatrix();
        int offset2 = inputMatrix.getOffset();
        float[] inputMatrix4 = inputMatrix2.getInputMatrix();
        int offset3 = inputMatrix2.getOffset();
        if (checkMatrix(resultMatrix, offset, 4) && checkMatrix(inputMatrix3, offset2, 16) && checkMatrix(inputMatrix4, offset3, 4)) {
            float f = inputMatrix4[offset3 + 0];
            float f2 = inputMatrix4[offset3 + 1];
            float f3 = inputMatrix4[offset3 + 2];
            float f4 = inputMatrix4[offset3 + 3];
            int i = offset2 + 0;
            resultMatrix[offset + 0] = (inputMatrix3[i + 0] * f) + (inputMatrix3[i + 4] * f2) + (inputMatrix3[i + 8] * f3) + (inputMatrix3[i + 12] * f4);
            int i2 = offset2 + 1;
            resultMatrix[offset + 1] = (inputMatrix3[i2 + 0] * f) + (inputMatrix3[i2 + 4] * f2) + (inputMatrix3[i2 + 8] * f3) + (inputMatrix3[i2 + 12] * f4);
            int i3 = offset2 + 2;
            resultMatrix[offset + 2] = (inputMatrix3[i3 + 0] * f) + (inputMatrix3[i3 + 4] * f2) + (inputMatrix3[i3 + 8] * f3) + (inputMatrix3[i3 + 12] * f4);
            int i4 = offset2 + 3;
            resultMatrix[offset + 3] = (inputMatrix3[i4 + 0] * f) + (inputMatrix3[i4 + 4] * f2) + (inputMatrix3[i4 + 8] * f3) + (inputMatrix3[i4 + 12] * f4);
        }
    }

    public static void adjustPerspectiveProjectionMatrix(Result result, float f, float f2, Distance distance) throws MatrixException {
        float near = distance.getNear();
        float far = distance.getFar();
        float tan = 1.0f / ((float) Math.tan(((double) f) * 0.008726646259971648d));
        float f3 = 1.0f / (near - far);
        float[] resultMatrix = result.getResultMatrix();
        int offset = result.getOffset();
        if (checkMatrix(resultMatrix, offset, 16)) {
            resultMatrix[offset + 0] = tan / f2;
            resultMatrix[offset + 1] = 0.0f;
            resultMatrix[offset + 2] = 0.0f;
            resultMatrix[offset + 3] = 0.0f;
            resultMatrix[offset + 4] = 0.0f;
            resultMatrix[offset + 5] = tan;
            resultMatrix[offset + 6] = 0.0f;
            resultMatrix[offset + 7] = 0.0f;
            resultMatrix[offset + 8] = 0.0f;
            resultMatrix[offset + 9] = 0.0f;
            resultMatrix[offset + 10] = (far + near) * f3;
            resultMatrix[offset + 11] = -1.0f;
            resultMatrix[offset + 12] = 0.0f;
            resultMatrix[offset + 13] = 0.0f;
            resultMatrix[offset + 14] = far * 2.0f * near * f3;
            resultMatrix[offset + 15] = 0.0f;
        }
    }

    public static void scaleMatrix(InputMatrix inputMatrix, Scale scale) throws MatrixException {
        float[] inputMatrix2 = inputMatrix.getInputMatrix();
        int offset = inputMatrix.getOffset();
        if (checkMatrix(inputMatrix2, offset, 16)) {
            float x = scale.getX();
            float y = scale.getY();
            float z = scale.getZ();
            for (int i = 0; i < 4; i++) {
                int i2 = offset + i;
                inputMatrix2[i2] = inputMatrix2[i2] * x;
                int i3 = i2 + 4;
                inputMatrix2[i3] = inputMatrix2[i3] * y;
                int i4 = i2 + 8;
                inputMatrix2[i4] = inputMatrix2[i4] * z;
            }
        }
    }

    public static void adjustIdentityMatrix(Result result) throws MatrixException {
        float[] resultMatrix = result.getResultMatrix();
        int offset = result.getOffset();
        if (checkMatrix(resultMatrix, offset, 16)) {
            for (int i = 0; i < 16; i++) {
                resultMatrix[offset + i] = 0.0f;
            }
            for (int i2 = 0; i2 < 16; i2 += 5) {
                resultMatrix[offset + i2] = 1.0f;
            }
        }
    }

    public static void transposeMatrix(Result result, InputMatrix inputMatrix) throws MatrixException {
        float[] resultMatrix = result.getResultMatrix();
        int offset = result.getOffset();
        float[] inputMatrix2 = inputMatrix.getInputMatrix();
        int offset2 = inputMatrix.getOffset();
        if (checkMatrix(resultMatrix, offset, 16) && checkMatrix(inputMatrix2, offset2, 16)) {
            for (int i = 0; i < 4; i++) {
                int i2 = (i * 4) + offset2;
                int i3 = offset + i;
                resultMatrix[i3] = inputMatrix2[i2];
                resultMatrix[i3 + 4] = inputMatrix2[i2 + 1];
                resultMatrix[i3 + 8] = inputMatrix2[i2 + 2];
                resultMatrix[i3 + 12] = inputMatrix2[i2 + 3];
            }
        }
    }

    private static float length(Position position) {
        float positionX = position.getPositionX();
        float positionY = position.getPositionY();
        float positionZ = position.getPositionZ();
        return (float) Math.sqrt((double) ((positionX * positionX) + (positionY * positionY) + (positionZ * positionZ)));
    }

    private static boolean checkMatrix(float[] fArr, int i, int i2) throws MatrixException {
        if (fArr == null) {
            throw new MatrixException("matrix is null");
        } else if (i < 0) {
            throw new MatrixException("offset is less than 0");
        } else if (fArr.length - i >= i2) {
            return true;
        } else {
            throw new MatrixException("actual length of the matrix is less than the minimum length");
        }
    }
}
