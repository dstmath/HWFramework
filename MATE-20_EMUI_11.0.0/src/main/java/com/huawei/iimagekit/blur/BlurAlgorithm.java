package com.huawei.iimagekit.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import com.huawei.iimagekit.common.HwAlgorithmBase;
import com.huawei.iimagekit.shadow.ShadowBoxBlur;

public class BlurAlgorithm extends HwAlgorithmBase {
    private int mBlurMethod;
    private String mMaskColor;
    private int mRadius;
    private float mSaturation;
    private int mScale;

    public BlurAlgorithm(Context context, int blurMethod, boolean isFourChannel) {
        this.mBlurMethod = 3;
        this.mSaturation = 1.0f;
        this.mMaskColor = "#00000000";
        this.mScale = 1;
        this.mRadius = 0;
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

    public BlurAlgorithm() {
        this(null, 3, false);
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
        if (radius < 3 || radius > 25) {
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

    public int styleBlur(Context context, Bitmap bitmapForBlur, Bitmap blurredBitmap, int style) {
        if (context == null) {
            localLog(HwAlgorithmBase.LogLevel.LOG_ERROR, "Failed styleBlur, ErrorCode:8");
            return 8;
        } else if (bitmapForBlur == null) {
            localLog(HwAlgorithmBase.LogLevel.LOG_ERROR, "Failed styleBlur, ErrorCode:1");
            return 1;
        } else if (blurredBitmap == null) {
            localLog(HwAlgorithmBase.LogLevel.LOG_ERROR, "Failed styleBlur, ErrorCode:2");
            return 2;
        } else {
            if (bitmapForBlur.getHeight() == blurredBitmap.getHeight()) {
                if (bitmapForBlur.getWidth() == blurredBitmap.getWidth()) {
                    setBlurType(style);
                    if (this.mScale == 0) {
                        localLog(HwAlgorithmBase.LogLevel.LOG_ERROR, "Error Scale, ErrorCode:3");
                        return 3;
                    }
                    int scaledWidth = Math.max(1, bitmapForBlur.getWidth() / this.mScale);
                    int scaledHeight = Math.max(1, bitmapForBlur.getHeight() / this.mScale);
                    Bitmap scaledBitmapForBlur = Bitmap.createScaledBitmap(bitmapForBlur, scaledWidth, scaledHeight, false);
                    Bitmap scaledBlurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                    int errResult = blur(scaledBitmapForBlur, scaledBlurredBitmap, Math.max((int) ((((float) this.mRadius) * context.getResources().getDisplayMetrics().density) / ((float) this.mScale)), 3));
                    if (errResult != 0) {
                        return errResult;
                    }
                    ColorMatrix colorMatrix = new ColorMatrix();
                    colorMatrix.setSaturation(this.mSaturation);
                    Paint paint = new Paint();
                    paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
                    Canvas canvas = new Canvas(scaledBlurredBitmap);
                    canvas.drawBitmap(scaledBlurredBitmap, 0.0f, 0.0f, paint);
                    canvas.drawColor(Color.parseColor(this.mMaskColor));
                    new Canvas(blurredBitmap).drawBitmap(Bitmap.createScaledBitmap(scaledBlurredBitmap, blurredBitmap.getWidth(), blurredBitmap.getHeight(), true), 0.0f, 0.0f, new Paint());
                    return errResult;
                }
            }
            localLog(HwAlgorithmBase.LogLevel.LOG_ERROR, "Failed styleBlur, ErrorCode:3");
            return 3;
        }
    }

    private void setBlurType(int style) {
        if (style == 0) {
            localLog(HwAlgorithmBase.LogLevel.LOG_INFO, "BlurAlgorithm, Blur Style: BG_SMALL_LIGHT.");
            this.mSaturation = 1.2f;
            this.mScale = 24;
            this.mRadius = 22;
            this.mMaskColor = "#4CFFFFFF";
        } else if (style == 1) {
            localLog(HwAlgorithmBase.LogLevel.LOG_INFO, "BlurAlgorithm, Blur Style: BG_MEDIUM_LIGHT.");
            this.mSaturation = 1.5f;
            this.mScale = 30;
            this.mRadius = 75;
            this.mMaskColor = "#4CFFFFFF";
        } else if (style == 2) {
            localLog(HwAlgorithmBase.LogLevel.LOG_INFO, "BlurAlgorithm, Blur Style: BG_SMALL_DARK.");
            this.mSaturation = 1.1f;
            this.mScale = 24;
            this.mRadius = 22;
            this.mMaskColor = "#400D0D0D";
        } else if (style == 3) {
            localLog(HwAlgorithmBase.LogLevel.LOG_INFO, "BlurAlgorithm, Blur Style: BG_MEDIUM_DARK.");
            this.mSaturation = 1.15f;
            this.mScale = 30;
            this.mRadius = 75;
            this.mMaskColor = "#400D0D0D";
        } else if (style != 4) {
            localLog(HwAlgorithmBase.LogLevel.LOG_INFO, "BlurAlgorithm, default Blur Style.");
        } else {
            localLog(HwAlgorithmBase.LogLevel.LOG_INFO, "BlurAlgorithm, Blur Style: BACKGROUND_XLARGE.");
            this.mSaturation = 1.5f;
            this.mScale = 50;
            this.mRadius = 242;
            this.mMaskColor = "#00000000";
        }
    }
}
