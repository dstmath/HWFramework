package ohos.agp.render;

import java.util.Arrays;

public class ColorMatrix {
    private static final int CONCAT_INDEX = 4;
    private static final int[] CONCAT_STEPS = {0, 1, 2, 3, 4, 5, 10, 15};
    private static final int KA_ROW = 3;
    private static final int KA_SCALE = 18;
    private static final int KA_TRANS = 19;
    private static final float KB_HUE = 0.072f;
    private static final int KB_ROW = 2;
    private static final int KB_SCALE = 12;
    private static final int KB_TRANS = 14;
    private static final float KG_HUE = 0.715f;
    private static final int KG_ROW = 1;
    private static final int KG_SCALE = 6;
    private static final int KG_TRANS = 9;
    private static final float KR_HUE = 0.213f;
    private static final int KR_ROW = 0;
    private static final int KR_SCALE = 0;
    private static final int KR_TRANS = 4;
    private static final int MATRIX_COLUMN = 5;
    private static final int MATRIX_SIZE = 20;
    private static final float SCALAR = 1.0f;
    private final float[] colorMatrix;

    public ColorMatrix() {
        this.colorMatrix = new float[20];
        reset();
    }

    public ColorMatrix(float[] fArr) {
        this.colorMatrix = new float[20];
        System.arraycopy(fArr, 0, this.colorMatrix, 0, 20);
    }

    public ColorMatrix(ColorMatrix colorMatrix2) {
        this.colorMatrix = new float[20];
        System.arraycopy(colorMatrix2.colorMatrix, 0, this.colorMatrix, 0, 20);
    }

    public final float[] getMatrix() {
        float[] fArr = new float[20];
        System.arraycopy(this.colorMatrix, 0, fArr, 0, 20);
        return fArr;
    }

    public void reset() {
        float[] fArr = this.colorMatrix;
        Arrays.fill(fArr, 0.0f);
        fArr[0] = 1.0f;
        fArr[6] = 1.0f;
        fArr[12] = 1.0f;
        fArr[18] = 1.0f;
    }

    public void setMatrix(float[] fArr) {
        System.arraycopy(fArr, 0, this.colorMatrix, 0, 20);
    }

    public void setScale(float f, float f2, float f3, float f4) {
        float[] fArr = this.colorMatrix;
        Arrays.fill(fArr, 0.0f);
        fArr[0] = f;
        fArr[6] = f2;
        fArr[12] = f3;
        fArr[18] = f4;
    }

    public void postTranslate(float f, float f2, float f3, float f4) {
        float[] fArr = this.colorMatrix;
        fArr[4] = fArr[4] + f;
        fArr[9] = fArr[9] + f2;
        fArr[14] = fArr[14] + f3;
        fArr[19] = fArr[19] + f4;
    }

    public void setSaturation(float f) {
        float f2 = 1.0f - f;
        float f3 = KR_HUE * f2;
        float f4 = KG_HUE * f2;
        float f5 = f2 * KB_HUE;
        float[] fArr = this.colorMatrix;
        Arrays.fill(fArr, 0.0f);
        for (int i = 0; i < 3; i++) {
            int i2 = i * 5;
            fArr[i2 + 0] = f3;
            fArr[i2 + 1] = f4;
            fArr[i2 + 2] = f5;
        }
        fArr[0] = fArr[0] + f;
        fArr[6] = fArr[6] + f;
        fArr[12] = fArr[12] + f;
        fArr[18] = 1.0f;
    }

    public void modify(float[] fArr) {
        System.arraycopy(fArr, 0, this.colorMatrix, 0, 20);
    }

    public void postConcat(ColorMatrix colorMatrix2) {
        setConcat(colorMatrix2, this);
    }

    private void setConcat(ColorMatrix colorMatrix2, ColorMatrix colorMatrix3) {
        float[] fArr = (colorMatrix2 == this || colorMatrix3 == this) ? new float[20] : this.colorMatrix;
        float[] fArr2 = colorMatrix2.colorMatrix;
        float[] fArr3 = colorMatrix3.colorMatrix;
        int i = 0;
        for (int i2 = 0; i2 < 20; i2 += 5) {
            int i3 = i;
            int i4 = 0;
            while (i4 < 4) {
                int[] iArr = CONCAT_STEPS;
                fArr[i3] = (fArr2[iArr[0] + i2] * fArr3[iArr[0] + i4]) + (fArr2[iArr[1] + i2] * fArr3[iArr[5] + i4]) + (fArr2[iArr[2] + i2] * fArr3[iArr[6] + i4]) + (fArr2[iArr[3] + i2] * fArr3[iArr[7] + i4]);
                i4++;
                i3++;
            }
            i = i3 + 1;
            int[] iArr2 = CONCAT_STEPS;
            fArr[i3] = (fArr2[iArr2[0] + i2] * fArr3[4]) + (fArr2[iArr2[1] + i2] * fArr3[9]) + (fArr2[iArr2[2] + i2] * fArr3[14]) + (fArr2[iArr2[3] + i2] * fArr3[19]) + fArr2[iArr2[4] + i2];
        }
        float[] fArr4 = this.colorMatrix;
        if (fArr != fArr4) {
            System.arraycopy(fArr, 0, fArr4, 0, 20);
        }
    }
}
