package ohos.sensor.data;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.sensor.bean.CategoryOrientation;

public class CategoryOrientationData extends SensorData<CategoryOrientation> {
    private static final int FOUR_DIMENSIONAL_MATRIX_LENGTH = 16;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218113824, "CategoryOrientationData");
    private static final int NUMBER_DOUBLED = 2;
    private static final int QUATERNION_LENGTH = 4;
    private static final int ROTATION_VECTOR_LENGTH = 3;
    private static final int[] SENSOR_DATA_DIMS = {16, 3, 1, 3, 5, 4, 5};
    private static final int THREE_DIMENSIONAL_MATRIX_LENGTH = 9;

    public CategoryOrientationData(CategoryOrientation categoryOrientation, int i, long j, int i2, float[] fArr) {
        super(categoryOrientation, i, j, i2, fArr);
    }

    public static float[] getQuaternionValues(float[] fArr) {
        float[] fArr2 = new float[4];
        if (fArr.length >= 4) {
            fArr2[0] = fArr[3];
        } else if (fArr.length == 3) {
            fArr2[0] = 1.0f - ((float) ((Math.pow((double) fArr[0], 2.0d) + Math.pow((double) fArr[1], 2.0d)) + Math.pow((double) fArr[2], 2.0d)));
            float f = 0.0f;
            if (fArr2[0] > 0.0f) {
                f = (float) Math.sqrt((double) fArr2[0]);
            }
            fArr2[0] = f;
        } else {
            HiLog.error(LABEL, "getQuaternionValues rotationVector is invalid", new Object[0]);
            return fArr2;
        }
        fArr2[1] = fArr[0];
        fArr2[2] = fArr[1];
        fArr2[3] = fArr[2];
        return fArr2;
    }

    public static void getDeviceRotationMatrix(float[] fArr, float[] fArr2) {
        if (!isParameterValid(fArr, fArr2)) {
            HiLog.error(LABEL, "getDeviceRotationMatrix rotationVector lenght or rotationMatrix length is invalid", new Object[0]);
        } else {
            getRotationMatrixImpl(fArr, getThreeDimensionalRotationMatrix(getQuaternionValues(fArr2)));
        }
    }

    private static boolean isParameterValid(float[] fArr, float[] fArr2) {
        if (fArr2.length < 3) {
            HiLog.error(LABEL, "isParameterValid rotationVector lenght is %{public}d", Integer.valueOf(fArr2.length));
        } else if (fArr.length == 9 || fArr.length == 16) {
            return true;
        } else {
            HiLog.error(LABEL, "isParameterValid rotationMatrix lenght is %{public}d", Integer.valueOf(fArr.length));
        }
        return false;
    }

    private static void getRotationMatrixImpl(float[] fArr, float[] fArr2) {
        int i = fArr.length == 9 ? 3 : 4;
        int i2 = 0;
        while (i2 < i) {
            int i3 = 0;
            while (i3 < i) {
                fArr[(i * i2) + i3] = (i3 == 3 || i2 == 3) ? (i2 == 3 && i3 == 3) ? 1.0f : 0.0f : fArr2[(i2 * 3) + i3];
                i3++;
            }
            i2++;
        }
    }

    private static float[] getThreeDimensionalRotationMatrix(float[] fArr) {
        float[] fArr2 = new float[9];
        if (fArr.length < 4) {
            HiLog.error(LABEL, "getRotationMatrix rotationVector lenght is %{public}d", Integer.valueOf(fArr.length));
        } else {
            float pow = ((float) Math.pow((double) fArr[2], 2.0d)) * 2.0f;
            float pow2 = ((float) Math.pow((double) fArr[3], 2.0d)) * 2.0f;
            fArr2[0] = (1.0f - pow) - pow2;
            float pow3 = 1.0f - (((float) Math.pow((double) fArr[1], 2.0d)) * 2.0f);
            fArr2[4] = pow3 - pow2;
            fArr2[8] = pow3 - pow;
            float f = fArr[0] * 2.0f * fArr[3];
            float f2 = fArr[1] * 2.0f * fArr[2];
            fArr2[1] = f2 - f;
            fArr2[3] = f2 + f;
            float f3 = fArr[0] * 2.0f * fArr[2];
            float f4 = fArr[1] * 2.0f * fArr[3];
            fArr2[2] = f4 + f3;
            fArr2[6] = f4 - f3;
            float f5 = fArr[0] * 2.0f * fArr[1];
            float f6 = fArr[2] * 2.0f * fArr[3];
            fArr2[5] = f6 - f5;
            fArr2[7] = f6 + f5;
        }
        return fArr2;
    }

    public static float[] getDeviceOrientation(float[] fArr, float[] fArr2) {
        if (fArr.length == 9 || fArr.length == 16) {
            int i = 3;
            if (fArr2.length >= 3) {
                if (fArr.length != 9) {
                    i = 4;
                }
                int i2 = (i * 1) + 1;
                fArr2[0] = (float) Math.atan2((double) fArr[1], (double) fArr[i2]);
                int i3 = i * 2;
                fArr2[1] = (float) Math.atan2((double) (-fArr[i3 + 1]), Math.sqrt(Math.pow((double) fArr[1], 2.0d) + Math.pow((double) fArr[i2], 2.0d)));
                fArr2[2] = (float) Math.atan2((double) (-fArr[i3]), (double) fArr[i3 + 2]);
                return fArr2;
            }
        }
        HiLog.error(LABEL, "getDeviceOrientation rotationMatrix or rotationAngle length is invalid", new Object[0]);
        return fArr2;
    }
}
