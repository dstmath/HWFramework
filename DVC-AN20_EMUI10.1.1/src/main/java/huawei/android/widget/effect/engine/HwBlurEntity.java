package huawei.android.widget.effect.engine;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import huawei.android.widget.effect.engine.HwBlurEngine;

public class HwBlurEntity {
    private static final int ALPHA_00_PERCENT = 0;
    private static final int ALPHA_55_PERCENT = 140;
    private static final int ALPHA_85_PERCENT = 216;
    private static final int ALPHA_90_PERCENT = 229;
    private static final int BLACK_COLOR = -16777216;
    private static final float BLUR_BRIGHTNESS = 1.03f;
    private static final float BLUR_CONTRAST = 1.0f;
    private static final float BLUR_SATURATION = 1.2f;
    private static final int COLOR_MASK = 16777215;
    private static final int COLOR_SHIFT = 24;
    private static final float DARK_BLUR_BRIGHTNESS = 1.0f;
    private static final float DARK_BLUR_CONTRAST = 1.0f;
    private static final float DARK_BLUR_SATURATION = 2.0f;
    private static final boolean DEBUG;
    private static final int DOMESTIC_BETA = 3;
    private static final float GRAY_BLUR_BRIGHTNESS = 1.5f;
    private static final float GRAY_BLUR_CONTRAST = 2.0f;
    private static final float GRAY_BLUR_SATURATION = 4.0f;
    private static final int GRAY_COLOR = -855310;
    private static final float LIGHT_BLUR_BRIGHTNESS = 1.5f;
    private static final float LIGHT_BLUR_CONTRAST = 2.0f;
    private static final float LIGHT_BLUR_SATURATION = 4.0f;
    private static final float SCALE = 1.0f;
    private static final String TAG = HwBlurEntity.class.getSimpleName();
    private static final float TRANSLATE_RATIO = 128.0f;
    private static final int WHITE_COLOR = -1;
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
    private Rect mTargetViewRect = new Rect();

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        DEBUG = z;
    }

    HwBlurEntity(View targetView, HwBlurEngine.BlurType blurType) {
        this.mOverlayAlpha = getAlphaOfTargetView(targetView);
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "HwBlurEntity: mOverlayAlpha " + this.mOverlayAlpha);
        }
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

    public void setTargetViewRect(Rect targetViewRect) {
        this.mTargetViewRect = targetViewRect;
    }

    public void drawBitmapForBlur(Canvas canvas, Bitmap bitmapForBlur, Rect unionRect, int downFactor) {
        Rect rect = new Rect();
        rect.left = (this.mTargetViewRect.left - unionRect.left) / downFactor;
        rect.top = (this.mTargetViewRect.top - unionRect.top) / downFactor;
        rect.right = (this.mTargetViewRect.right - unionRect.left) / downFactor;
        rect.bottom = (this.mTargetViewRect.bottom - unionRect.top) / downFactor;
        canvas.drawBitmap(bitmapForBlur, rect, rect, this.mDrawColorBalancePaint);
    }

    public void drawBlurredBitmap(Canvas canvas, Bitmap blurredBitmap, Rect unionRect, int downFactor) {
        this.mDrawBlurredBitmapPaint.setShader(getBitmapShader(blurredBitmap, unionRect, downFactor));
        if (this.mCornerRadius > 0) {
            float width = (float) this.mTargetViewRect.width();
            float height = (float) this.mTargetViewRect.height();
            int i = this.mCornerRadius;
            canvas.drawRoundRect(0.0f, 0.0f, width, height, (float) i, (float) i, this.mDrawBlurredBitmapPaint);
            int i2 = this.mCornerRadius;
            canvas.drawRoundRect(0.0f, 0.0f, (float) this.mTargetViewRect.width(), (float) this.mTargetViewRect.height(), (float) i2, (float) i2, this.mDrawOverLayColorPaint);
            return;
        }
        canvas.drawRect(0.0f, 0.0f, (float) this.mTargetViewRect.width(), (float) this.mTargetViewRect.height(), this.mDrawBlurredBitmapPaint);
        canvas.drawRect(0.0f, 0.0f, (float) this.mTargetViewRect.width(), (float) this.mTargetViewRect.height(), this.mDrawOverLayColorPaint);
    }

    private void setBlurType(HwBlurEngine.BlurType blurType) {
        if (blurType == null) {
            Log.w(TAG, "blurType type cannot be null.");
            return;
        }
        int i = AnonymousClass1.$SwitchMap$huawei$android$widget$effect$engine$HwBlurEngine$BlurType[blurType.ordinal()];
        if (i == 1) {
            this.mOverlayColor = (0 << COLOR_SHIFT) | 0;
            this.mSaturation = BLUR_SATURATION;
            this.mBrightness = BLUR_BRIGHTNESS;
            this.mContrast = 1.0f;
        } else if (i == 2) {
            this.mOverlayColor = (this.mOverlayAlpha << COLOR_SHIFT) | COLOR_MASK;
            this.mSaturation = 4.0f;
            this.mBrightness = 1.5f;
            this.mContrast = 2.0f;
        } else if (i == 3) {
            this.mOverlayColor = (this.mOverlayAlpha << COLOR_SHIFT) | 15921906;
            this.mSaturation = 4.0f;
            this.mBrightness = 1.5f;
            this.mContrast = 2.0f;
        } else if (i != 4) {
            Log.w(TAG, "blurType is incorrect.");
        } else {
            this.mOverlayColor = (ALPHA_55_PERCENT << COLOR_SHIFT) | 0;
            this.mSaturation = 2.0f;
            this.mBrightness = 1.0f;
            this.mContrast = 1.0f;
        }
        this.mDrawOverLayColorPaint.setColor(this.mOverlayColor);
        this.mDrawColorBalancePaint.setColorFilter(getColorMatrixColorFilter());
    }

    /* access modifiers changed from: package-private */
    /* renamed from: huawei.android.widget.effect.engine.HwBlurEntity$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$huawei$android$widget$effect$engine$HwBlurEngine$BlurType = new int[HwBlurEngine.BlurType.values().length];

        static {
            try {
                $SwitchMap$huawei$android$widget$effect$engine$HwBlurEngine$BlurType[HwBlurEngine.BlurType.Blur.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$huawei$android$widget$effect$engine$HwBlurEngine$BlurType[HwBlurEngine.BlurType.LightBlur.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$huawei$android$widget$effect$engine$HwBlurEngine$BlurType[HwBlurEngine.BlurType.LightBlurWithGray.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$huawei$android$widget$effect$engine$HwBlurEngine$BlurType[HwBlurEngine.BlurType.DarkBlur.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private ColorMatrixColorFilter getColorMatrixColorFilter() {
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(this.mSaturation);
        ColorMatrix brightnessMatrix = new ColorMatrix();
        float f = this.mBrightness;
        brightnessMatrix.setScale(f, f, f, 1.0f);
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
        matrix.postTranslate((float) (unionRect.left - this.mTargetViewRect.left), (float) (unionRect.top - this.mTargetViewRect.top));
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
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "getAlphaOfTargetView: packageName " + packageName);
            }
            char c = 65535;
            switch (packageName.hashCode()) {
                case -1605005728:
                    if (packageName.equals("com.android.documentsui")) {
                        c = 6;
                        break;
                    }
                    break;
                case -969203187:
                    if (packageName.equals("com.huawei.contacts")) {
                        c = 1;
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
                        c = 2;
                        break;
                    }
                    break;
                case -480218078:
                    if (packageName.equals("com.huawei.email")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 217702641:
                    if (packageName.equals("com.example.android.notepad")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1156888975:
                    if (packageName.equals("com.android.settings")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1534272944:
                    if (packageName.equals("com.android.email")) {
                        c = 7;
                        break;
                    }
                    break;
                case 1839186535:
                    if (packageName.equals("com.huawei.notepad")) {
                        c = 5;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                    return ALPHA_85_PERCENT;
                case 2:
                    return ALPHA_85_PERCENT;
                case 3:
                    return ALPHA_85_PERCENT;
                case 4:
                case 5:
                    return ALPHA_85_PERCENT;
                case 6:
                    return ALPHA_85_PERCENT;
                case 7:
                case '\b':
                    return ALPHA_85_PERCENT;
                default:
                    return ALPHA_90_PERCENT;
            }
        }
    }
}
