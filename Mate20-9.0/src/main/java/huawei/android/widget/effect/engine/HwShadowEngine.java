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
    private static final float BRIGHTNESS_MATRIX_A_SCALE = 1.0f;
    private static final int DEFAULT_BLUR_DOWN_FACTOR = 10;
    private static final int DEFAULT_BLUR_RADIUS = 10;
    private static final float DEFAULT_BRIGHTNESS = 1.0f;
    private static final int DEFAULT_OFFSET_PIXEL = 1;
    private static final float DEFAULT_SATURATION = 1.0f;
    private static final float DEFAULT_SCALE_FACTOR = 0.9f;
    private static final float DEFAULT_SHADOW_ALPHA = 1.0f;
    private static final boolean IS_EMUI_LITE = SystemUtil.getSystemProperty("ro.build.hw_emui_lite.enable", IS_EMUI_LITE);
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
    private BlurListener blurListener;
    private ShadowAlgorithm mAlgorithm;
    private Bitmap mBitmapForBlur;
    private Bitmap mBlurBitmap;
    private Canvas mBlurCanvas;
    private int mBlurDownFactor;
    private int mBlurRadius;
    /* access modifiers changed from: private */
    public Bitmap mBlurredBitmap;
    private float mBrightness;
    private int mCanvasSaved;
    private Context mContext;
    private boolean mIsAsyncBlur;
    /* access modifiers changed from: private */
    public boolean mIsBlurredBitmapReady;
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
    /* access modifiers changed from: private */
    public ViewGroup mShadowLayout;
    private RectF mShadowRect;
    private Bitmap mViewBitmap;
    private Canvas mViewCanvas;

    private interface BlurListener {
        void onBlurred(Bitmap bitmap);
    }

    private static class BlurTask extends AsyncTask<Bitmap, Integer, Bitmap> {
        private BlurListener blurListener;
        ShadowAlgorithm mShadowAlgorithm;
        private int radius;

        BlurTask(Context context, int radius2, int method) {
            this.radius = radius2;
            this.mShadowAlgorithm = new ShadowAlgorithm(context, method);
        }

        /* access modifiers changed from: package-private */
        public BlurTask setBlurListener(BlurListener blurListener2) {
            this.blurListener = blurListener2;
            return this;
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(Bitmap... bitmaps) {
            Bitmap blurredBitmap = Bitmap.createBitmap(bitmaps[0].getWidth(), bitmaps[0].getHeight(), Bitmap.Config.ARGB_8888);
            if (this.mShadowAlgorithm.doShadow(bitmaps[0], blurredBitmap, this.radius) == 0) {
                return blurredBitmap;
            }
            Log.w(HwShadowEngine.TAG, "check blur input parameter is error in algorithm");
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            if (this.blurListener == null) {
                Log.w(HwShadowEngine.TAG, "blurListener is null!");
            } else {
                this.blurListener.onBlurred(bitmap);
            }
        }
    }

    public enum ShadowType {
        Small,
        Medium,
        Large,
        XLarge
    }

    public HwShadowEngine(ViewGroup shadowLayout, ShadowType shadowType) {
        this.mMethod = 6;
        this.mOffsetPixel = 1;
        this.mBlurRadius = 10;
        this.mBlurDownFactor = 10;
        this.mScaleFactor = 0.9f;
        this.mShadowAlpha = 1.0f;
        this.mSaturation = 1.0f;
        this.mBrightness = 1.0f;
        this.mIsEffectEnabled = true;
        this.mIsAsyncBlur = IS_EMUI_LITE;
        this.mIsBlurredBitmapReady = IS_EMUI_LITE;
        this.mIsDrawFullView = SystemUtil.getSystemProperty("agp.gfx.shadowType", true);
        this.mOutsideRect = new Rect();
        this.mShadowRect = new RectF();
        this.mPaint = new Paint(1);
        this.mPaint2 = new Paint(2);
        this.blurListener = new BlurListener() {
            public void onBlurred(Bitmap bitmap) {
                if (HwShadowEngine.this.mShadowLayout == null) {
                    Log.w(HwShadowEngine.TAG, "shadowLayout is null");
                    return;
                }
                Bitmap unused = HwShadowEngine.this.mBlurredBitmap = bitmap;
                boolean unused2 = HwShadowEngine.this.mIsBlurredBitmapReady = true;
                HwShadowEngine.this.mShadowLayout.invalidate();
            }
        };
        if (shadowLayout == null || shadowType == null) {
            Log.w(TAG, "shadowLayout or shadowType is null");
            return;
        }
        this.mContext = shadowLayout.getContext();
        this.mShadowLayout = shadowLayout;
        this.mShadowLayout.setWillNotDraw(IS_EMUI_LITE);
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

    public void setEnable(boolean enable) {
        this.mIsEffectEnabled = enable;
    }

    public boolean isEnable() {
        if (!this.mIsEffectEnabled || !isDeviceSupport()) {
            return IS_EMUI_LITE;
        }
        return true;
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
                this.mBitmapForBlur = getBitmapForBlur(targetView);
                this.mBlurredBitmap = Bitmap.createBitmap(this.mBitmapForBlur.getWidth(), this.mBitmapForBlur.getHeight(), Bitmap.Config.ARGB_8888);
                if (this.mAlgorithm.doShadow(this.mBitmapForBlur, this.mBlurredBitmap, radius) != 0) {
                    Log.w(TAG, "check blur input parameter is error in algorithm");
                    return;
                }
            } else if (!this.mIsBlurredBitmapReady) {
                this.mBitmapForBlur = getBitmapForBlur(targetView);
                new BlurTask(this.mContext, radius, this.mMethod).setBlurListener(this.blurListener).execute(new Bitmap[]{this.mBitmapForBlur});
            } else {
                this.mIsBlurredBitmapReady = IS_EMUI_LITE;
            }
            drawBlurredBitmapToCanvas(targetView, this.mBlurredBitmap, canvas);
        }
    }

    private void setShadowType(ShadowType shadowType) {
        if (this.mShadowLayout == null || shadowType == null) {
            Log.w(TAG, "shadowLayout or shadowType is null");
            return;
        }
        this.mIsDrawFullView = shadowType != ShadowType.Large ? true : IS_EMUI_LITE;
        switch (shadowType) {
            case Small:
                this.mBlurRadius = 5;
                this.mOffsetPixel = convertDp2Pixel(1);
                this.mScaleFactor = 0.9f;
                this.mShadowAlpha = SMALL_SHADOW_ALPHA;
                this.mSaturation = 1.0f;
                this.mBrightness = 1.0f;
                break;
            case Medium:
                this.mBlurRadius = MEDIUM_BLUR_RADIUS;
                this.mOffsetPixel = convertDp2Pixel(4);
                this.mScaleFactor = 1.0f;
                this.mShadowAlpha = MEDIUM_SHADOW_ALPHA;
                this.mSaturation = 1.0f;
                this.mBrightness = MEDIUM_BRIGHTNESS;
                break;
            case Large:
                this.mBlurRadius = convertDp2Pixel(LARGE_BLUR_RADIUS_DP);
                this.mOffsetPixel = convertDp2Pixel(2);
                this.mScaleFactor = LARGE_SCALE_FACTOR;
                this.mShadowAlpha = 0.8f;
                this.mSaturation = 2.0f;
                this.mBrightness = 1.0f;
                break;
            case XLarge:
                this.mBlurRadius = 80;
                this.mOffsetPixel = convertDp2Pixel(4);
                this.mScaleFactor = XLARGE_SCALE_FACTOR;
                this.mShadowAlpha = 0.8f;
                this.mSaturation = 2.0f;
                this.mBrightness = 1.0f;
                break;
            default:
                Log.w(TAG, "shadowType is error");
                break;
        }
        setColorMatrixColorFilter();
        this.mShadowLayout.invalidate();
    }

    private int convertDp2Pixel(int dp) {
        if (this.mContext != null) {
            return (int) (this.mContext.getResources().getDisplayMetrics().density * ((float) dp));
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
        brightnessMatrix.setScale(this.mBrightness, this.mBrightness, this.mBrightness, 1.0f);
        ColorMatrix ImageMatrix = new ColorMatrix();
        ImageMatrix.postConcat(saturationMatrix);
        ImageMatrix.postConcat(brightnessMatrix);
        this.mPaint.setAlpha((int) (this.mShadowAlpha * MAX_ALPHA_VALUE));
        this.mPaint.setColorFilter(new ColorMatrixColorFilter(ImageMatrix));
    }

    private Bitmap getBitmapForBlur(View targetView) {
        if (this.mShadowLayout == null || targetView == null) {
            Log.w(TAG, "mShadowLayout is null or current layout has not child view!");
            return null;
        }
        int targetW = targetView.getWidth();
        int targetH = targetView.getHeight();
        float bottomH = this.mIsDrawFullView ? (float) targetH : ((float) (this.mBlurRadius + this.mOffsetPixel)) / this.mScaleFactor;
        float viewBitmapW = (((float) targetW) * this.mScaleFactor) / ((float) this.mBlurDownFactor);
        float viewBitmapH = (this.mScaleFactor * bottomH) / ((float) this.mBlurDownFactor);
        if (this.mViewBitmap == null || this.mViewCanvas == null || this.mViewBitmap.getWidth() != ((int) viewBitmapW) || this.mViewBitmap.getHeight() != ((int) viewBitmapH)) {
            this.mViewBitmap = Bitmap.createBitmap((int) viewBitmapW, (int) viewBitmapH, Bitmap.Config.ARGB_8888);
            this.mViewCanvas = new Canvas(this.mViewBitmap);
        }
        this.mCanvasSaved = this.mViewCanvas.save();
        this.mViewCanvas.scale(this.mScaleFactor / ((float) this.mBlurDownFactor), this.mScaleFactor / ((float) this.mBlurDownFactor));
        this.mViewCanvas.clipRect(MEDIUM_BRIGHTNESS, MEDIUM_BRIGHTNESS, (float) targetW, bottomH);
        this.mViewCanvas.translate(MEDIUM_BRIGHTNESS, bottomH - ((float) targetH));
        targetView.draw(this.mViewCanvas);
        this.mViewCanvas.restoreToCount(this.mCanvasSaved);
        float viewW = ((((float) targetW) * this.mScaleFactor) + ((float) (this.mBlurRadius * 2))) / ((float) this.mBlurDownFactor);
        float viewH = ((this.mScaleFactor * bottomH) + ((float) (2 * this.mBlurRadius))) / ((float) this.mBlurDownFactor);
        float expandW = (float) (this.mBlurRadius / this.mBlurDownFactor);
        if (this.mBlurBitmap == null || this.mBlurCanvas == null || this.mBlurBitmap.getWidth() != ((int) viewBitmapW) || this.mBlurBitmap.getHeight() != ((int) viewBitmapH)) {
            this.mBlurBitmap = Bitmap.createBitmap((int) viewW, (int) viewH, Bitmap.Config.ARGB_8888);
            this.mBlurCanvas = new Canvas(this.mBlurBitmap);
        }
        this.mBlurCanvas.drawBitmap(this.mViewBitmap, null, new RectF(expandW, expandW, expandW + viewBitmapW, expandW + viewBitmapH), this.mPaint);
        return this.mBlurBitmap;
    }

    private void drawBlurredBitmapToCanvas(View targetView, Bitmap blurredBitmap, Canvas canvas) {
        if (this.mShadowLayout == null || targetView == null || blurredBitmap == null || canvas == null) {
            Log.w(TAG, "targetView,blurredBitmap or canvas is null! ");
            return;
        }
        float dWidth = (float) ((blurredBitmap.getWidth() * this.mBlurDownFactor) - canvas.getWidth());
        float dHeightB = (float) (this.mOffsetPixel + this.mBlurRadius);
        float dHeightT = (float) ((2 * this.mBlurRadius) - canvas.getHeight());
        if (this.mIsDrawFullView) {
            dHeightT = (float) ((((blurredBitmap.getHeight() * this.mBlurDownFactor) - this.mOffsetPixel) - this.mBlurRadius) - canvas.getHeight());
        }
        this.mShadowRect.left = ((float) targetView.getLeft()) - (dWidth / 2.0f);
        this.mShadowRect.right = ((float) targetView.getRight()) + (dWidth / 2.0f);
        this.mShadowRect.top = ((float) targetView.getTop()) - dHeightT;
        this.mShadowRect.bottom = ((float) targetView.getBottom()) + dHeightB;
        int insetSize = this.mOffsetPixel + this.mBlurRadius;
        int insetSize2 = insetSize < 0 ? 0 : insetSize;
        canvas.getClipBounds(this.mOutsideRect);
        this.mOutsideRect.inset(-insetSize2, -insetSize2);
        canvas.clipRect(this.mOutsideRect);
        canvas.drawBitmap(blurredBitmap, null, this.mShadowRect, this.mPaint2);
    }
}
