package com.huawei.iimagekit.shadow;

import android.content.Context;
import android.graphics.Bitmap;
import com.huawei.iimagekit.common.HwAlgorithmBase;

public class ShadowAlgorithm extends HwAlgorithmBase {
    private int mShadowMethod;

    public ShadowAlgorithm(Context context, int shadowMethod) {
        this.mShadowMethod = shadowMethod;
    }

    public int checkBlurInputParameter(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        if (!IS_IMAGEKIT_SHADOW_PROP) {
            return 6;
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

    public int doShadow(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        int errResult = checkBlurInputParameter(bitmapForBlur, blurredBitmap, radius);
        if (errResult == 0) {
            if (this.mShadowMethod != 2) {
                NoneShadow.doBlur(bitmapForBlur, blurredBitmap, radius);
            } else {
                ShadowBoxBlur.doBlur(bitmapForBlur, blurredBitmap, radius);
            }
        }
        return errResult;
    }
}
