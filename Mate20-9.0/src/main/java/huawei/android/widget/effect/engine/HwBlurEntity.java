package huawei.android.widget.effect.engine;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.Log;
import android.view.View;
import huawei.android.widget.effect.engine.HwBlurEngine;

public class HwBlurEntity {
    private static final int ALPHA_85_PERCENT = 216;
    private static final int ALPHA_90_PERCENT = 229;
    private static final float BLUR_BRIGHTNESS = 1.03f;
    private static final float BLUR_CONTRAST = 1.0f;
    private static final float BLUR_SATURATION = 1.2f;
    private static final float DARK_BLUR_BRIGHTNESS = 1.0f;
    private static final float DARK_BLUR_CONTRAST = 1.0f;
    private static final float DARK_BLUR_SATURATION = 2.0f;
    private static final float GRAY_BLUR_BRIGHTNESS = 1.5f;
    private static final float GRAY_BLUR_CONTRAST = 2.0f;
    private static final float GRAY_BLUR_SATURATION = 4.0f;
    private static final float LIGHT_BLUR_BRIGHTNESS = 1.5f;
    private static final float LIGHT_BLUR_CONTRAST = 2.0f;
    private static final float LIGHT_BLUR_SATURATION = 4.0f;
    private static final float SCALE = 1.0f;
    private static final String TAG = HwBlurEntity.class.getSimpleName();
    private static final float TRANSLATE_RATIO = 128.0f;
    private float mBrightness = 1.0f;
    private float mContrast = 1.0f;
    private int mCornerRadius = 0;
    private Paint mDrawBlurredBitmapPaint = new Paint(2);
    private Paint mDrawColorBalancePaint = new Paint();
    private Paint mDrawOverLayColorPaint = new Paint();
    private boolean mIsBlurEnabled = false;
    private int mOverlayAlpha;
    private int mOverlayColor = 0;
    private float mSaturation = 1.0f;
    private Rect targetViewRect = new Rect();

    HwBlurEntity(View targetView, HwBlurEngine.BlurType blurType) {
        this.mOverlayAlpha = getAlphaOfTargetView(targetView);
        setBlurType(blurType);
        targetView.setWillNotDraw(false);
    }

    public boolean isEnabled() {
        return this.mIsBlurEnabled;
    }

    public void setBlurEnable(boolean isBlurEnable) {
        if (this.mIsBlurEnabled != isBlurEnable) {
            this.mIsBlurEnabled = isBlurEnable;
        }
    }

    public void setCornerRadius(int cornerRadius) {
        if (this.mCornerRadius != cornerRadius) {
            this.mCornerRadius = cornerRadius;
        }
    }

    public void setOverlayColor(int overlayColor) {
        if (this.mOverlayColor != overlayColor) {
            this.mOverlayColor = overlayColor;
            this.mDrawOverLayColorPaint.setColor(this.mOverlayColor);
        }
    }

    public void setTargetViewRect(Rect targetViewRect2) {
        this.targetViewRect = targetViewRect2;
    }

    public void drawBitmapForBlur(Canvas canvas, Bitmap bitmapForBlur, Rect unionRect, int downFactor) {
        Rect rect = new Rect();
        rect.left = (this.targetViewRect.left - unionRect.left) / downFactor;
        rect.top = (this.targetViewRect.top - unionRect.top) / downFactor;
        rect.right = (this.targetViewRect.right - unionRect.left) / downFactor;
        rect.bottom = (this.targetViewRect.bottom - unionRect.top) / downFactor;
        canvas.drawBitmap(bitmapForBlur, rect, rect, this.mDrawColorBalancePaint);
    }

    public void drawBlurredBitmap(Canvas canvas, Bitmap blurredBitmap, Rect unionRect, int downFactor) {
        this.mDrawBlurredBitmapPaint.setShader(getBitmapShader(blurredBitmap, unionRect, downFactor));
        if (this.mCornerRadius > 0) {
            canvas.drawRoundRect(0.0f, 0.0f, (float) this.targetViewRect.width(), (float) this.targetViewRect.height(), (float) this.mCornerRadius, (float) this.mCornerRadius, this.mDrawBlurredBitmapPaint);
            canvas.drawRoundRect(0.0f, 0.0f, (float) this.targetViewRect.width(), (float) this.targetViewRect.height(), (float) this.mCornerRadius, (float) this.mCornerRadius, this.mDrawOverLayColorPaint);
            return;
        }
        canvas.drawRect(0.0f, 0.0f, (float) this.targetViewRect.width(), (float) this.targetViewRect.height(), this.mDrawBlurredBitmapPaint);
        canvas.drawRect(0.0f, 0.0f, (float) this.targetViewRect.width(), (float) this.targetViewRect.height(), this.mDrawOverLayColorPaint);
    }

    private void setBlurType(HwBlurEngine.BlurType blurType) {
        if (blurType == null) {
            Log.w(TAG, "blurType type cannot be null.");
            return;
        }
        switch (blurType) {
            case Blur:
                this.mOverlayColor = Color.argb(0, 0, 0, 0);
                this.mSaturation = BLUR_SATURATION;
                this.mBrightness = BLUR_BRIGHTNESS;
                this.mContrast = 1.0f;
                break;
            case LightBlur:
                this.mOverlayColor = Color.argb(this.mOverlayAlpha, 255, 255, 255);
                this.mSaturation = 4.0f;
                this.mBrightness = 1.5f;
                this.mContrast = 2.0f;
                break;
            case LightBlurWithGray:
                this.mOverlayColor = Color.argb(this.mOverlayAlpha, 242, 242, 242);
                this.mSaturation = 4.0f;
                this.mBrightness = 1.5f;
                this.mContrast = 2.0f;
                break;
            case DarkBlur:
                this.mOverlayColor = Color.argb(140, 0, 0, 0);
                this.mSaturation = 2.0f;
                this.mBrightness = 1.0f;
                this.mContrast = 1.0f;
                break;
            default:
                Log.w(TAG, "blurType is incorrect.");
                break;
        }
        this.mDrawOverLayColorPaint.setColor(this.mOverlayColor);
        this.mDrawColorBalancePaint.setColorFilter(getColorMatrixColorFilter());
    }

    private ColorMatrixColorFilter getColorMatrixColorFilter() {
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(this.mSaturation);
        ColorMatrix brightnessMatrix = new ColorMatrix();
        brightnessMatrix.setScale(this.mBrightness, this.mBrightness, this.mBrightness, 1.0f);
        float scale = this.mContrast;
        float translate = (1.0f - scale) * TRANSLATE_RATIO;
        ColorMatrix contrastMatrix = new ColorMatrix(new float[]{scale, 0.0f, 0.0f, 0.0f, translate, 0.0f, scale, 0.0f, 0.0f, translate, 0.0f, 0.0f, scale, 0.0f, translate, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.postConcat(contrastMatrix);
        colorMatrix.postConcat(saturationMatrix);
        colorMatrix.postConcat(brightnessMatrix);
        return new ColorMatrixColorFilter(colorMatrix);
    }

    private BitmapShader getBitmapShader(Bitmap blurredBitmap, Rect unionRect, int downFactor) {
        Matrix matrix = new Matrix();
        matrix.postScale((float) downFactor, (float) downFactor);
        matrix.postTranslate((float) (unionRect.left - this.targetViewRect.left), (float) (unionRect.top - this.targetViewRect.top));
        Shader.TileMode tileMode = Shader.TileMode.CLAMP;
        BitmapShader shader = new BitmapShader(blurredBitmap, tileMode, tileMode);
        shader.setLocalMatrix(matrix);
        return shader;
    }

    private int getAlphaOfTargetView(View targetView) {
        if (targetView == null) {
            Log.w(TAG, "targetView cannot be null.");
            return ALPHA_90_PERCENT;
        } else if (targetView.getContext() == null) {
            Log.w(TAG, "context of targetView cannot be null.");
            return ALPHA_90_PERCENT;
        } else {
            String packageName = targetView.getContext().getPackageName();
            char c = 65535;
            switch (packageName.hashCode()) {
                case -1605005728:
                    if (packageName.equals("com.android.documentsui")) {
                        c = 4;
                        break;
                    }
                    break;
                case -845193793:
                    if (packageName.equals("com.android.contacts")) {
                        c = 0;
                        break;
                    }
                    break;
                case -695601689:
                    if (packageName.equals("com.android.mms")) {
                        c = 1;
                        break;
                    }
                    break;
                case 217702641:
                    if (packageName.equals("com.example.android.notepad")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1156888975:
                    if (packageName.equals("com.android.settings")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1534272944:
                    if (packageName.equals("com.android.email")) {
                        c = 5;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    return ALPHA_85_PERCENT;
                case 1:
                    return ALPHA_85_PERCENT;
                case 2:
                    return ALPHA_85_PERCENT;
                case 3:
                    return ALPHA_85_PERCENT;
                case 4:
                    return ALPHA_85_PERCENT;
                case 5:
                    return ALPHA_85_PERCENT;
                default:
                    return ALPHA_90_PERCENT;
            }
        }
    }
}
