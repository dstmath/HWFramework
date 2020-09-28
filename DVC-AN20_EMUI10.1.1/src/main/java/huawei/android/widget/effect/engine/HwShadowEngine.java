package huawei.android.widget.effect.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.iimagekit.blur.util.SystemUtil;
import com.huawei.iimagekit.shadow.ShadowAlgorithm;

public class HwShadowEngine {
    private static final int ALGORITHM_RIGHT_RESULT = 0;
    private static final float BLUR_ATTR_FLOAT = 2.0f;
    private static final int BLUR_ATTR_INT = 2;
    private static final float BRIGHTNESS_MATRIX_A_SCALE = 1.0f;
    private static final int DEFAULT_BLUR_DOWN_FACTOR = 10;
    private static final int DEFAULT_BLUR_RADIUS = 10;
    private static final float DEFAULT_BRIGHTNESS = 1.0f;
    private static final int DEFAULT_OFFSET_PIXEL = 1;
    private static final float DEFAULT_SATURATION = 1.0f;
    private static final float DEFAULT_SCALE_FACTOR = 0.9f;
    private static final float DEFAULT_SHADOW_ALPHA = 1.0f;
    private static final boolean IS_EMUI_LITE = SystemUtil.getSystemProperty("ro.build.hw_emui_lite.enable", false);
    private static final int LARGE_BLUR_RADIUS = 80;
    private static final int LARGE_BLUR_RADIUS_DP = 22;
    private static final float LARGE_BRIGHTNESS = 1.0f;
    private static final int LARGE_OFFSET_DP = 2;
    private static final float LARGE_SATURATION = 2.0f;
    private static final float LARGE_SCALE_FACTOR = 0.85f;
    private static final float LARGE_SHADOW_ALPHA = 0.8f;
    private static final float MAX_ALPHA_VALUE = 255.0f;
    private static final int MEDIUM_BLUR_RADIUS = 30;
    private static final float MEDIUM_BRIGHTNESS = 0.0f;
    private static final int MEDIUM_OFFSET_DP = 4;
    private static final float MEDIUM_SATURATION = 1.0f;
    private static final float MEDIUM_SCALE_FACTOR = 1.0f;
    private static final float MEDIUM_SHADOW_ALPHA = 0.1f;
    private static final int SMALL_BLUR_RADIUS = 5;
    private static final float SMALL_BRIGHTNESS = 1.0f;
    private static final int SMALL_OFFSET_DP = 1;
    private static final float SMALL_SATURATION = 1.0f;
    private static final float SMALL_SCALE_FACTOR = 0.9f;
    private static final float SMALL_SHADOW_ALPHA = 0.3f;
    private static final String TAG = "HwShadowEngine";
    private static final int XLARGE_BLUR_RADIUS = 80;
    private static final float XLARGE_BRIGHTNESS = 1.0f;
    private static final int XLARGE_OFFSET_DP = 4;
    private static final float XLARGE_SATURATION = 2.0f;
    private static final float XLARGE_SCALE_FACTOR = 0.5f;
    private static final float XLARGE_SHADOW_ALPHA = 0.8f;
    private ShadowAlgorithm mAlgorithm;
    private Bitmap mBitmapForBlur;
    private Bitmap mBlurBitmap;
    private Canvas mBlurCanvas;
    private int mBlurDownFactor;
    private BlurListener mBlurListener;
    private int mBlurRadius;
    private Bitmap mBlurredBitmap;
    private float mBrightness;
    private int mCanvasSaved;
    private Context mContext;
    private boolean mIsAsyncBlur;
    private boolean mIsBlurredBitmapReady;
    private boolean mIsDrawFullView;
    private boolean mIsEffectEnabled;
    private int mMethod;
    private int mOffsetPixel;
    private Rect mOutsideRect;
    private Paint mPaint;
    private Paint mPaint2;
    private float mSaturation;
    private float mScaleFactor;
    private float mShadowAlpha;
    private ViewGroup mShadowLayout;
    private RectF mShadowRect;
    private Bitmap mViewBitmap;
    private Canvas mViewCanvas;

    public interface BlurListener {
        void onBlurred(Bitmap bitmap);
    }

    public enum ShadowType {
        Small,
        Medium,
        Large,
        XLarge
    }

    public HwShadowEngine(ViewGroup shadowLayout, ShadowType shadowType) {
        this.mMethod = 2;
        this.mOffsetPixel = 1;
        this.mBlurRadius = 10;
        this.mBlurDownFactor = 10;
        this.mScaleFactor = 0.9f;
        this.mShadowAlpha = 1.0f;
        this.mSaturation = 1.0f;
        this.mBrightness = 1.0f;
        this.mIsEffectEnabled = true;
        this.mIsAsyncBlur = false;
        this.mIsBlurredBitmapReady = false;
        this.mIsDrawFullView = SystemUtil.getSystemProperty("agp.gfx.shadowType", true);
        this.mOutsideRect = new Rect();
        this.mShadowRect = new RectF();
        this.mPaint = new Paint(1);
        this.mPaint2 = new Paint(2);
        this.mBlurListener = new BlurListener() {
            /* class huawei.android.widget.effect.engine.HwShadowEngine.AnonymousClass1 */

            @Override // huawei.android.widget.effect.engine.HwShadowEngine.BlurListener
            public void onBlurred(Bitmap bitmap) {
                if (HwShadowEngine.this.mShadowLayout == null) {
                    Log.w(HwShadowEngine.TAG, "shadowLayout is null");
                    return;
                }
                HwShadowEngine.this.mBlurredBitmap = bitmap;
                HwShadowEngine.this.mIsBlurredBitmapReady = true;
                HwShadowEngine.this.mShadowLayout.invalidate();
            }
        };
        if (shadowLayout == null || shadowType == null) {
            Log.w(TAG, "shadowLayout or shadowType is null");
            return;
        }
        this.mContext = shadowLayout.getContext();
        this.mShadowLayout = shadowLayout;
        this.mShadowLayout.setWillNotDraw(false);
        setShadowType(shadowType);
        this.mAlgorithm = new ShadowAlgorithm(this.mContext, this.mMethod);
        this.mShadowLayout.invalidate();
    }

    public HwShadowEngine(ViewGroup shadowLayout) {
        this(shadowLayout, ShadowType.Medium);
    }

    public static boolean isDeviceSupport() {
        return !IS_EMUI_LITE;
    }

    public void setEnable(boolean isEnable) {
        this.mIsEffectEnabled = isEnable;
    }

    public boolean isEnable() {
        return this.mIsEffectEnabled && isDeviceSupport();
    }

    public void renderShadow(Canvas canvas) {
        if (this.mShadowLayout == null || canvas == null) {
            Log.w(TAG, "input parameter mShadowLayout or canvas is null.");
        } else if (!isEnable()) {
            Log.w(TAG, "ShadowEngine may set enable false or current device does not support!");
        } else {
            int radius = this.mBlurRadius / this.mBlurDownFactor;
            View targetView = this.mShadowLayout.getChildAt(0);
            if (targetView == null) {
                Log.w(TAG, "current layout has not child view!");
                return;
            }
            if (!this.mIsAsyncBlur) {
                Bitmap bitmapForBlur = getBitmapForBlur(targetView);
                if (bitmapForBlur == null) {
                    Log.w(TAG, "getBitmapForBlur() return null!");
                    return;
                }
                this.mBitmapForBlur = bitmapForBlur;
                this.mBlurredBitmap = Bitmap.createBitmap(this.mBitmapForBlur.getWidth(), this.mBitmapForBlur.getHeight(), Bitmap.Config.ARGB_8888);
                if (this.mAlgorithm.doShadow(this.mBitmapForBlur, this.mBlurredBitmap, radius) != 0) {
                    Log.w(TAG, "check blur input parameter is error in algorithm");
                    return;
                }
            } else if (!this.mIsBlurredBitmapReady) {
                Bitmap bitmapForBlur2 = getBitmapForBlur(targetView);
                if (bitmapForBlur2 == null) {
                    Log.w(TAG, "getBitmapForBlur() return null!");
                    return;
                }
                this.mBitmapForBlur = bitmapForBlur2;
                new BlurTask(this.mContext, radius, this.mMethod).setBlurListener(this.mBlurListener).execute(this.mBitmapForBlur);
            } else {
                this.mIsBlurredBitmapReady = false;
            }
            drawBlurredBitmapToCanvas(targetView, this.mBlurredBitmap, canvas);
        }
    }

    private void setShadowType(ShadowType shadowType) {
        if (this.mShadowLayout == null || shadowType == null) {
            Log.w(TAG, "shadowLayout or shadowType is null");
            return;
        }
        this.mIsDrawFullView = shadowType != ShadowType.Large;
        int i = AnonymousClass2.$SwitchMap$huawei$android$widget$effect$engine$HwShadowEngine$ShadowType[shadowType.ordinal()];
        if (i == 1) {
            this.mBlurRadius = 5;
            this.mOffsetPixel = convertDp2Pixel(1);
            this.mScaleFactor = 0.9f;
            this.mShadowAlpha = SMALL_SHADOW_ALPHA;
            this.mSaturation = 1.0f;
            this.mBrightness = 1.0f;
        } else if (i == 2) {
            this.mBlurRadius = MEDIUM_BLUR_RADIUS;
            this.mOffsetPixel = convertDp2Pixel(4);
            this.mScaleFactor = 1.0f;
            this.mShadowAlpha = 0.1f;
            this.mSaturation = 1.0f;
            this.mBrightness = MEDIUM_BRIGHTNESS;
        } else if (i == 3) {
            this.mBlurRadius = convertDp2Pixel(LARGE_BLUR_RADIUS_DP);
            this.mOffsetPixel = convertDp2Pixel(2);
            this.mScaleFactor = LARGE_SCALE_FACTOR;
            this.mShadowAlpha = 0.8f;
            this.mSaturation = 2.0f;
            this.mBrightness = 1.0f;
        } else if (i != 4) {
            Log.w(TAG, "shadowType is error");
        } else {
            this.mBlurRadius = 80;
            this.mOffsetPixel = convertDp2Pixel(4);
            this.mScaleFactor = 0.5f;
            this.mShadowAlpha = 0.8f;
            this.mSaturation = 2.0f;
            this.mBrightness = 1.0f;
        }
        setColorMatrixColorFilter();
        this.mShadowLayout.invalidate();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: huawei.android.widget.effect.engine.HwShadowEngine$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$huawei$android$widget$effect$engine$HwShadowEngine$ShadowType = new int[ShadowType.values().length];

        static {
            try {
                $SwitchMap$huawei$android$widget$effect$engine$HwShadowEngine$ShadowType[ShadowType.Small.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$huawei$android$widget$effect$engine$HwShadowEngine$ShadowType[ShadowType.Medium.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$huawei$android$widget$effect$engine$HwShadowEngine$ShadowType[ShadowType.Large.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$huawei$android$widget$effect$engine$HwShadowEngine$ShadowType[ShadowType.XLarge.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private static class BlurTask extends AsyncTask<Bitmap, Integer, Bitmap> {
        private BlurListener mBlurListener;
        private int mRadius;
        private ShadowAlgorithm mShadowAlgorithm;

        BlurTask(Context context, int radius, int method) {
            this.mRadius = radius;
            this.mShadowAlgorithm = new ShadowAlgorithm(context, method);
        }

        /* access modifiers changed from: package-private */
        public BlurTask setBlurListener(BlurListener blurListener) {
            this.mBlurListener = blurListener;
            return this;
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(Bitmap... bitmaps) {
            Bitmap blurredBitmap = Bitmap.createBitmap(bitmaps[0].getWidth(), bitmaps[0].getHeight(), Bitmap.Config.ARGB_8888);
            if (this.mShadowAlgorithm.doShadow(bitmaps[0], blurredBitmap, this.mRadius) == 0) {
                return blurredBitmap;
            }
            Log.w(HwShadowEngine.TAG, "check blur input parameter is error in algorithm");
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            BlurListener blurListener = this.mBlurListener;
            if (blurListener == null) {
                Log.w(HwShadowEngine.TAG, "mBlurListener is null!");
            } else {
                blurListener.onBlurred(bitmap);
            }
        }
    }

    private int convertDp2Pixel(int dp) {
        Context context = this.mContext;
        if (context != null) {
            return (int) (context.getResources().getDisplayMetrics().density * ((float) dp));
        }
        Log.w(TAG, "mContext is null!");
        return 1;
    }

    private void setColorMatrixColorFilter() {
        if (this.mShadowLayout == null) {
            Log.w(TAG, "mShadowLayout is null");
            return;
        }
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(this.mSaturation);
        ColorMatrix brightnessMatrix = new ColorMatrix();
        float f = this.mBrightness;
        brightnessMatrix.setScale(f, f, f, 1.0f);
        ColorMatrix imageMatrix = new ColorMatrix();
        imageMatrix.postConcat(saturationMatrix);
        imageMatrix.postConcat(brightnessMatrix);
        this.mPaint.setAlpha((int) (this.mShadowAlpha * MAX_ALPHA_VALUE));
        this.mPaint.setColorFilter(new ColorMatrixColorFilter(imageMatrix));
    }

    private Bitmap getBitmapForBlur(View targetView) {
        if (this.mShadowLayout == null || targetView == null) {
            Log.w(TAG, "mShadowLayout is null or current layout has not child view!");
            return null;
        }
        int targetW = targetView.getWidth();
        int targetH = targetView.getHeight();
        float bottomH = this.mIsDrawFullView ? (float) targetH : ((float) (this.mBlurRadius + this.mOffsetPixel)) / this.mScaleFactor;
        float f = this.mScaleFactor;
        int i = this.mBlurDownFactor;
        float viewBitmapW = (((float) targetW) * f) / ((float) i);
        float viewBitmapH = (f * bottomH) / ((float) i);
        Bitmap bitmap = this.mViewBitmap;
        if (bitmap == null || this.mViewCanvas == null || bitmap.getWidth() != ((int) viewBitmapW) || this.mViewBitmap.getHeight() != ((int) viewBitmapH)) {
            if (viewBitmapW <= MEDIUM_BRIGHTNESS || viewBitmapH <= MEDIUM_BRIGHTNESS) {
                Log.w(TAG, "viewBitmapW or viewBitmapH -le 0");
                return null;
            }
            this.mViewBitmap = Bitmap.createBitmap((int) viewBitmapW, (int) viewBitmapH, Bitmap.Config.ARGB_8888);
            this.mViewCanvas = new Canvas(this.mViewBitmap);
        }
        this.mCanvasSaved = this.mViewCanvas.save();
        Canvas canvas = this.mViewCanvas;
        float f2 = this.mScaleFactor;
        int i2 = this.mBlurDownFactor;
        canvas.scale(f2 / ((float) i2), f2 / ((float) i2));
        this.mViewCanvas.clipRect(MEDIUM_BRIGHTNESS, MEDIUM_BRIGHTNESS, (float) targetW, bottomH);
        this.mViewCanvas.translate(MEDIUM_BRIGHTNESS, bottomH - ((float) targetH));
        targetView.draw(this.mViewCanvas);
        this.mViewCanvas.restoreToCount(this.mCanvasSaved);
        float f3 = this.mScaleFactor;
        int i3 = this.mBlurRadius;
        int i4 = this.mBlurDownFactor;
        float viewW = ((((float) targetW) * f3) + ((float) (i3 * 2))) / ((float) i4);
        float viewH = ((f3 * bottomH) + ((float) (i3 * 2))) / ((float) i4);
        float expandW = (float) (i3 / i4);
        Bitmap bitmap2 = this.mBlurBitmap;
        if (bitmap2 == null || this.mBlurCanvas == null || bitmap2.getWidth() != ((int) viewBitmapW) || this.mBlurBitmap.getHeight() != ((int) viewBitmapH)) {
            if (viewW <= MEDIUM_BRIGHTNESS || viewH <= MEDIUM_BRIGHTNESS) {
                Log.w(TAG, "viewW or viewH -le 0");
                return null;
            }
            this.mBlurBitmap = Bitmap.createBitmap((int) viewW, (int) viewH, Bitmap.Config.ARGB_8888);
            this.mBlurCanvas = new Canvas(this.mBlurBitmap);
        }
        this.mBlurCanvas.drawBitmap(this.mViewBitmap, (Rect) null, new RectF(expandW, expandW, expandW + viewBitmapW, expandW + viewBitmapH), this.mPaint);
        return this.mBlurBitmap;
    }

    private void drawBlurredBitmapToCanvas(View targetView, Bitmap blurredBitmap, Canvas canvas) {
        if (this.mShadowLayout == null || targetView == null || blurredBitmap == null || canvas == null) {
            Log.w(TAG, "targetView,blurredBitmap or canvas is null! ");
            return;
        }
        float width = (float) ((blurredBitmap.getWidth() * this.mBlurDownFactor) - canvas.getWidth());
        float totalHeight = (float) ((this.mBlurRadius * 2) - canvas.getHeight());
        if (this.mIsDrawFullView) {
            totalHeight = (float) ((((blurredBitmap.getHeight() * this.mBlurDownFactor) - this.mOffsetPixel) - this.mBlurRadius) - canvas.getHeight());
        }
        this.mShadowRect.left = ((float) targetView.getLeft()) - (width / 2.0f);
        this.mShadowRect.right = ((float) targetView.getRight()) + (width / 2.0f);
        this.mShadowRect.bottom = ((float) targetView.getBottom()) + ((float) (this.mOffsetPixel + this.mBlurRadius));
        this.mShadowRect.top = ((float) targetView.getTop()) - totalHeight;
        int insetSize = this.mOffsetPixel + this.mBlurRadius;
        int insetSize2 = insetSize < 0 ? 0 : insetSize;
        canvas.getClipBounds(this.mOutsideRect);
        this.mOutsideRect.inset(-insetSize2, -insetSize2);
        canvas.clipRect(this.mOutsideRect);
        canvas.drawBitmap(blurredBitmap, (Rect) null, this.mShadowRect, this.mPaint2);
    }
}
