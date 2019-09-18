package com.huawei.iimagekit.blur;

import android.content.Context;
import android.graphics.Bitmap;
import com.huawei.iimagekit.common.HwAlgorithmBase;
import com.huawei.iimagekit.shadow.ShadowBoxBlur;

public class BlurAlgorithm extends HwAlgorithmBase {
    private int mBlurMethod;

    public BlurAlgorithm(Context context, int blurMethod, boolean use_4channel) {
        if (use_4channel) {
            this.mBlurMethod = 6;
        } else {
            this.mBlurMethod = blurMethod;
        }
        if (this.mBlurMethod != 6) {
            System.loadLibrary("iimagekit_jni");
        }
    }

    public BlurAlgorithm(Context context, int blurMethod) {
        this(context, blurMethod, false);
    }

    /* access modifiers changed from: protected */
    public int checkBlurInputParameter(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        if (!IMAGEKIT_BLUR_PROP) {
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
        int err_result = checkBlurInputParameter(bitmapForBlur, blurredBitmap, radius);
        if (err_result == 0) {
            int i = this.mBlurMethod;
            if (i == 6) {
                ShadowBoxBlur.doBlur(bitmapForBlur, blurredBitmap, radius);
            } else if (i != 11) {
                CPUBoxBlur.getInstance().doBlur(bitmapForBlur, blurredBitmap, radius);
            } else {
                CPUFastBlur.getInstance().doBlur(bitmapForBlur, blurredBitmap, radius);
            }
        } else {
            localLog(4, "ErrorCode:" + err_result);
        }
        return err_result;
    }
}
