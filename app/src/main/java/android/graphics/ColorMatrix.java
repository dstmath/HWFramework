package android.graphics;

import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import java.util.Arrays;

public class ColorMatrix {
    private final float[] mArray;

    public ColorMatrix() {
        this.mArray = new float[20];
        reset();
    }

    public ColorMatrix(float[] src) {
        this.mArray = new float[20];
        System.arraycopy(src, 0, this.mArray, 0, 20);
    }

    public ColorMatrix(ColorMatrix src) {
        this.mArray = new float[20];
        System.arraycopy(src.mArray, 0, this.mArray, 0, 20);
    }

    public final float[] getArray() {
        return this.mArray;
    }

    public void reset() {
        float[] a = this.mArray;
        Arrays.fill(a, 0.0f);
        a[18] = Engine.DEFAULT_VOLUME;
        a[12] = Engine.DEFAULT_VOLUME;
        a[6] = Engine.DEFAULT_VOLUME;
        a[0] = Engine.DEFAULT_VOLUME;
    }

    public void set(ColorMatrix src) {
        System.arraycopy(src.mArray, 0, this.mArray, 0, 20);
    }

    public void set(float[] src) {
        System.arraycopy(src, 0, this.mArray, 0, 20);
    }

    public void setScale(float rScale, float gScale, float bScale, float aScale) {
        float[] a = this.mArray;
        for (int i = 19; i > 0; i--) {
            a[i] = 0.0f;
        }
        a[0] = rScale;
        a[6] = gScale;
        a[12] = bScale;
        a[18] = aScale;
    }

    public void setRotate(int axis, float degrees) {
        reset();
        double radians = (((double) degrees) * 3.141592653589793d) / 180.0d;
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);
        float[] fArr;
        switch (axis) {
            case TextToSpeech.SUCCESS /*0*/:
                fArr = this.mArray;
                this.mArray[12] = cosine;
                fArr[6] = cosine;
                this.mArray[7] = sine;
                this.mArray[11] = -sine;
            case AudioState.ROUTE_EARPIECE /*1*/:
                fArr = this.mArray;
                this.mArray[12] = cosine;
                fArr[0] = cosine;
                this.mArray[2] = -sine;
                this.mArray[10] = sine;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                fArr = this.mArray;
                this.mArray[6] = cosine;
                fArr[0] = cosine;
                this.mArray[1] = sine;
                this.mArray[5] = -sine;
            default:
                throw new RuntimeException();
        }
    }

    public void setConcat(ColorMatrix matA, ColorMatrix matB) {
        float[] tmp;
        if (matA == this || matB == this) {
            tmp = new float[20];
        } else {
            tmp = this.mArray;
        }
        float[] a = matA.mArray;
        float[] b = matB.mArray;
        int index = 0;
        for (int j = 0; j < 20; j += 5) {
            int i = 0;
            int index2 = index;
            while (i < 4) {
                index = index2 + 1;
                tmp[index2] = (((a[j + 0] * b[i + 0]) + (a[j + 1] * b[i + 5])) + (a[j + 2] * b[i + 10])) + (a[j + 3] * b[i + 15]);
                i++;
                index2 = index;
            }
            index = index2 + 1;
            tmp[index2] = ((((a[j + 0] * b[4]) + (a[j + 1] * b[9])) + (a[j + 2] * b[14])) + (a[j + 3] * b[19])) + a[j + 4];
        }
        if (tmp != this.mArray) {
            System.arraycopy(tmp, 0, this.mArray, 0, 20);
        }
    }

    public void preConcat(ColorMatrix prematrix) {
        setConcat(this, prematrix);
    }

    public void postConcat(ColorMatrix postmatrix) {
        setConcat(postmatrix, this);
    }

    public void setSaturation(float sat) {
        reset();
        float[] m = this.mArray;
        float invSat = Engine.DEFAULT_VOLUME - sat;
        float R = 0.213f * invSat;
        float G = 0.715f * invSat;
        float B = 0.072f * invSat;
        m[0] = R + sat;
        m[1] = G;
        m[2] = B;
        m[5] = R;
        m[6] = G + sat;
        m[7] = B;
        m[10] = R;
        m[11] = G;
        m[12] = B + sat;
    }

    public void setRGB2YUV() {
        reset();
        float[] m = this.mArray;
        m[0] = 0.299f;
        m[1] = 0.587f;
        m[2] = 0.114f;
        m[5] = -0.16874f;
        m[6] = -0.33126f;
        m[7] = NetworkHistoryUtils.RECOVERY_PERCENTAGE;
        m[10] = NetworkHistoryUtils.RECOVERY_PERCENTAGE;
        m[11] = -0.41869f;
        m[12] = -0.08131f;
    }

    public void setYUV2RGB() {
        reset();
        float[] m = this.mArray;
        m[2] = 1.402f;
        m[5] = Engine.DEFAULT_VOLUME;
        m[6] = -0.34414f;
        m[7] = -0.71414f;
        m[10] = Engine.DEFAULT_VOLUME;
        m[11] = 1.772f;
        m[12] = 0.0f;
    }
}
