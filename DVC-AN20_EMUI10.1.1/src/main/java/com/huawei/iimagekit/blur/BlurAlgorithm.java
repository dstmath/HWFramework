package com.huawei.iimagekit.blur;

import android.content.Context;
import android.graphics.Bitmap;
import com.huawei.iimagekit.common.HwAlgorithmBase;
import com.huawei.iimagekit.shadow.ShadowBoxBlur;

public class BlurAlgorithm extends HwAlgorithmBase {
    private int mBlurMethod;

    public BlurAlgorithm(Context context, int blurMethod, boolean isFourChannel) {
        if (isFourChannel) {
            this.mBlurMethod = 2;
        } else {
            this.mBlurMethod = blurMethod;
        }
        if (this.mBlurMethod != 2) {
            System.loadLibrary("iimagekit_jni");
        }
    }

    public BlurAlgorithm(Context context, int blurMethod) {
        this(context, blurMethod, false);
    }

    /* access modifiers changed from: protected */
    public int checkBlurInputParameter(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        if (!IS_IMAGEKIT_BLUR_PROP) {
            return 5;
        }
        if (bitmapForBlur == null) {
            return 1;
        }
        if (blurredBitmap == null) {
            return 2;
        }
        if (bitmapForBlur.getHeight() != blurredBitmap.getHeight() || bitmapForBlur.getWidth() != blurredBitmap.getWidth()) {
            return 3;
        }
        if (radius <= 2 || radius > 25) {
            return 4;
        }
        if (bitmapForBlur.getHeight() < 3 || bitmapForBlur.getWidth() < 3) {
            return 7;
        }
        return 0;
    }

    public int blur(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        HwAlgorithmBase.LogLevel logLevel = HwAlgorithmBase.LogLevel.LOG_INFO;
        localLog(logLevel, "BlurAlgorithm, blur ......, blurMethod:" + this.mBlurMethod + ", radius:" + radius);
        int errResult = checkBlurInputParameter(bitmapForBlur, blurredBitmap, radius);
        if (errResult == 0) {
            int i = this.mBlurMethod;
            if (i == 2) {
                HwAlgorithmBase.LogLevel logLevel2 = HwAlgorithmBase.LogLevel.LOG_INFO;
                localLog(logLevel2, "BlurAlgorithm, blur calling ShadowBoxBlur......, radius:" + radius);
                ShadowBoxBlur.doBlur(bitmapForBlur, blurredBitmap, radius);
            } else if (i == 4) {
                HwAlgorithmBase.LogLevel logLevel3 = HwAlgorithmBase.LogLevel.LOG_INFO;
                localLog(logLevel3, "BlurAlgorithm, blur calling CPUFastBlur......, radius:" + radius);
                CpuFastBlur.getInstance().doBlur(bitmapForBlur, blurredBitmap, radius);
            } else if (i != 5) {
                HwAlgorithmBase.LogLevel logLevel4 = HwAlgorithmBase.LogLevel.LOG_INFO;
                localLog(logLevel4, "BlurAlgorithm, blur calling CPUBoxBlur......, radius:" + radius);
                CpuBoxBlur.getInstance().doBlur(bitmapForBlur, blurredBitmap, radius);
            } else {
                HwAlgorithmBase.LogLevel logLevel5 = HwAlgorithmBase.LogLevel.LOG_INFO;
                localLog(logLevel5, "BlurAlgorithm, blur calling CPUGaussianBlur......, radius:" + radius);
                CpuGaussianBlur.getInstance().doBlur(bitmapForBlur, blurredBitmap, radius);
            }
        } else {
            HwAlgorithmBase.LogLevel logLevel6 = HwAlgorithmBase.LogLevel.LOG_ERROR;
            localLog(logLevel6, "ErrorCode:" + errResult);
        }
        return errResult;
    }
}
