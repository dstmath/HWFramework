package huawei.android.widget.effect.engine;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import com.huawei.iimagekit.blur.BlurAlgorithm;
import com.huawei.iimagekit.blur.util.SystemUtil;
import com.huawei.uikit.effect.BuildConfig;
import huawei.android.widget.loader.ResLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HwBlurEngine {
    private static final String ADVANCED_VISUAL_EFFECT_KEY = "advanced_visual_effect";
    private static final int ADVANCED_VISUAL_EFFECT_OFF = 0;
    private static final int ADVANCED_VISUAL_EFFECT_ON = 1;
    private static final int BLUR_RADIUS = 120;
    private static final int COUNT = 4;
    private static final boolean DEBUG = (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3);
    private static final int DOMESTIC_BETA = 3;
    private static final int DOWN_FACTOR = 15;
    private static final int HANDLER_MASSAGE_BLUR = 1;
    private static final boolean IS_BLUR_EFFECT_ENABLED = SystemUtil.getSystemProperty("ro.config.hw_blur_effect", true);
    private static final boolean IS_EMUI_LITE = SystemUtil.getSystemProperty("ro.build.hw_emui_lite.enable", false);
    private static final int MAX_FRAME_INTERVAL = 16666666;
    private static final int MIN_DIMEN = 1;
    private static final String TAG = HwBlurEngine.class.getSimpleName();
    private static final int TARGET_VIEWS_INITIAL_CAPACITY = 6;
    private static HwBlurEngine mBlurEngine = new HwBlurEngine();
    private static BlurAlgorithm mStaticAlgorithm = new BlurAlgorithm((Context) null, 2, true);
    private BlurAlgorithm mAlgorithm;
    private Bitmap mBalanceBitmap;
    private Canvas mBalanceCanvas;
    private Bitmap mBitmapForBlur;
    private Bitmap mBitmapForDraw;
    private Canvas mBlurCanvas;
    private Rect mBlurUnionRect = new Rect();
    private Bitmap mBlurredBitmap;
    private View mDecorView;
    private Rect mDecorViewRect = new Rect();
    private boolean mIsBitmapCopying = false;
    private boolean mIsBlurEffectEnabled = true;
    private boolean mIsDecorViewChanged = false;
    private boolean mIsDrawingViewSelf = false;
    private boolean mIsLastFrameTimeout = false;
    private boolean mIsSettingFetched = false;
    private boolean mIsSettingOpened = false;
    private boolean mIsSkipDrawFrame = false;
    private boolean mIsThemeAttrFetched = false;
    private boolean mIsThemeSupported = false;
    private boolean mIsUnionAreaChanged = true;
    private long mLastFrameTime = System.nanoTime();
    private ViewTreeObserver.OnGlobalLayoutListener mLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        /* class huawei.android.widget.effect.engine.$$Lambda$HwBlurEngine$mT50yEF0clUy9ydDQRLag1Qem98 */

        public final void onGlobalLayout() {
            HwBlurEngine.this.updateUnionArea();
        }
    };
    private int mMethod = 3;
    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        /* class huawei.android.widget.effect.engine.$$Lambda$HwBlurEngine$xUbPOu5PkLUPGbsHOyrUY88qw */

        public final boolean onPreDraw() {
            return HwBlurEngine.this.onPreDraw();
        }
    };
    private SubThread mSubThread;
    private SubThreadHandler mSubThreadHandler;
    private ConcurrentHashMap<View, HwBlurEntity> mTargetViews = new ConcurrentHashMap<>(6);

    public enum BlurType {
        Blur(1),
        DarkBlur(2),
        LightBlur(3),
        LightBlurWithGray(4);
        
        int mValue;

        private BlurType(int value) {
            this.mValue = value;
        }

        public static BlurType fromTypeValue(int typeValue) {
            BlurType type = null;
            int size = values().length;
            for (int i = 0; i < size; i++) {
                type = values()[i];
                if (type.getValue() == typeValue) {
                    return type;
                }
            }
            return type;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    private HwBlurEngine() {
        if (DEBUG) {
            Log.d(TAG, "create blur engine instance: version 1.8");
        }
    }

    @Deprecated
    public HwBlurEngine(View targetView, BlurType blurType) {
        Log.e(TAG, "This constructor is deprecated and will remove later.");
    }

    public static HwBlurEngine getInstance() {
        return mBlurEngine;
    }

    @Deprecated
    public static HwBlurEngine getInstance(View targetView, BlurType blurType) {
        if (DEBUG) {
            Log.w(TAG, "getInstance: this function deprecated!!!");
        }
        mBlurEngine.addBlurTargetView(targetView, blurType);
        return mBlurEngine;
    }

    public void addBlurTargetView(View targetView, BlurType blurType) {
        if (targetView == null || targetView.getContext() == null) {
            Log.e(TAG, "addBlurTargetView: targetView can not be null");
        } else if (blurType == null) {
            Log.e(TAG, "addBlurTargetView: blurType can not be null");
        } else if (!isShowBlur(targetView.getContext())) {
            if (DEBUG) {
                Log.w(TAG, "addBlurTargetView: isShowBlur = false");
            }
        } else if (!this.mTargetViews.containsKey(targetView)) {
            if (this.mAlgorithm == null) {
                this.mAlgorithm = new BlurAlgorithm(targetView.getContext(), this.mMethod);
            }
            this.mTargetViews.put(targetView, new HwBlurEntity(targetView, blurType));
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "addBlurTargetView: [" + this.mTargetViews.size() + "], view: " + targetView);
            }
        }
    }

    public void removeBlurTargetView(View targetView) {
        if (targetView == null) {
            Log.e(TAG, "removeTargetView: targetView can not be null");
            return;
        }
        this.mTargetViews.remove(targetView);
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "removeTargetView: [" + this.mTargetViews.size() + "], view = " + targetView);
        }
        if (this.mTargetViews.size() == 0) {
            this.mIsThemeAttrFetched = false;
            this.mIsSettingFetched = false;
            destroyDecorView();
        }
    }

    public boolean isDrawingViewSelf() {
        return this.mIsDrawingViewSelf;
    }

    public void setBlurEnable(boolean isBlurEnable) {
        this.mIsBlurEffectEnabled = isBlurEnable;
    }

    public boolean isBlurEnable() {
        return this.mIsBlurEffectEnabled;
    }

    public boolean isShowHwBlur() {
        return isBlurEnable() && isDeviceSupport() && isConfigurationOpen();
    }

    public boolean isShowHwBlur(View targetView) {
        if (targetView == null || targetView.getContext() == null) {
            Log.e(TAG, "isShowHwBlur: targetView can not be null");
            return false;
        } else if (!isThemeSupport(targetView.getContext()) || !isSettingOpened(targetView.getContext()) || !this.mTargetViews.containsKey(targetView) || !isShowHwBlur() || !this.mTargetViews.get(targetView).isEnabled()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isShowBlur(Context context) {
        return isShowHwBlur() && isThemeSupport(context) && isSettingOpened(context);
    }

    public boolean isThemeSupportedBlurEffect(Context context) {
        if (context == null) {
            Log.e(TAG, "isThemeSupportedBlurEffect: context is null.");
            return false;
        }
        int blurIdentifier = ResLoader.getInstance().getIdentifier(context, "attr", "hwBlurEffectEnable");
        if (blurIdentifier == 0) {
            return false;
        }
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{blurIdentifier});
        boolean isHwBlurEffectEnable = typedArray.getBoolean(0, false);
        typedArray.recycle();
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "isThemeSupportedBlurEffect: fetec theme property success, " + isHwBlurEffectEnable);
        }
        return isHwBlurEffectEnable;
    }

    public boolean isSettingEnabledBlurEffect(Context context) {
        if (context == null || context.getContentResolver() == null) {
            Log.e(TAG, "isSettingEnabledBlurEffect: context of targetView is null.");
            return false;
        }
        int result = Settings.System.getInt(context.getContentResolver(), ADVANCED_VISUAL_EFFECT_KEY, 0);
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "isSettingEnabledBlurEffect: fetch setting data success, " + result);
        }
        if (result == 1) {
            return true;
        }
        return false;
    }

    public void setTargetViewBlurEnable(View targetView, boolean isBlurEnable) {
        if (targetView == null) {
            Log.e(TAG, "setBlurEnable: targetView can not be null");
        } else if (this.mTargetViews.containsKey(targetView)) {
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "setBlurEnable: [" + isBlurEnable + "], view = " + targetView);
            }
            this.mTargetViews.get(targetView).setBlurEnable(isBlurEnable);
            if (isBlurEnable) {
                updateDecorView(targetView);
            }
            updateUnionArea();
        } else if (DEBUG) {
            String str2 = TAG;
            Log.w(str2, "setBlurEnable: target view is not contained in blur engine. view: " + targetView + ", enable: " + isBlurEnable);
        }
    }

    public void setTargetViewCornerRadius(View targetView, int cornerRadius) {
        if (targetView == null) {
            Log.e(TAG, "setCornerRadius: targetView can not be null");
        } else if (cornerRadius < 0) {
            Log.e(TAG, "setCornerRadius: corner radius must >= 0");
        } else if (this.mTargetViews.containsKey(targetView)) {
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "setCornerRadius: view = " + targetView + ", radius = " + cornerRadius);
            }
            this.mTargetViews.get(targetView).setCornerRadius(cornerRadius);
            targetView.invalidate();
        } else if (DEBUG) {
            Log.w(TAG, "setCornerRadius: target view is not contained in blur engine.");
        }
    }

    public void setTargetViewOverlayColor(View targetView, int overlayColor) {
        if (targetView == null) {
            Log.e(TAG, "setOverlayColor: targetView can not be null");
        } else if (this.mTargetViews.containsKey(targetView)) {
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "setOverlayColor: view = " + targetView + ", color = " + overlayColor);
            }
            this.mTargetViews.get(targetView).setOverlayColor(overlayColor);
            targetView.invalidate();
        } else if (DEBUG) {
            Log.w(TAG, "setOverlayColor: target view is not contained in blur engine.");
        }
    }

    public void onWindowVisibilityChanged(View targetView, boolean isViewVisible) {
        if (!isViewVisible) {
            removeBlurTargetView(targetView);
        }
    }

    @Deprecated
    public void onWindowVisibilityChanged(View targetView, boolean isViewVisible, boolean isBlurEnabled) {
        onWindowVisibilityChanged(targetView, isViewVisible);
        setTargetViewBlurEnable(targetView, isBlurEnabled);
    }

    public void draw(Canvas canvas, View targetView) {
        if (canvas == null || targetView == null || !targetView.isShown()) {
            Log.e(TAG, "draw: canvas is null or target view is null or view is not shown");
        } else if (!isShowHwBlur()) {
            if (DEBUG) {
                Log.w(TAG, "draw: isShowHwBlur = false");
            }
        } else if (this.mIsDrawingViewSelf) {
            throw new RuntimeException(BuildConfig.FLAVOR);
        } else if (this.mTargetViews.containsKey(targetView) && this.mTargetViews.get(targetView).isEnabled() && this.mBitmapForDraw != null) {
            this.mTargetViews.get(targetView).drawBlurredBitmap(canvas, this.mBitmapForDraw, this.mBlurUnionRect, 15);
        }
    }

    @Deprecated
    public void draw(Canvas canvas) {
        Log.e(TAG, "draw(Canvas canvas) is deprecated pls use draw(Canvas, View) instead.");
    }

    private static boolean isDeviceSupport() {
        return !IS_EMUI_LITE;
    }

    private static boolean isConfigurationOpen() {
        return IS_BLUR_EFFECT_ENABLED;
    }

    private boolean isThemeSupport(Context context) {
        if (!this.mIsThemeAttrFetched) {
            this.mIsThemeSupported = isThemeSupportedBlurEffect(context);
            this.mIsThemeAttrFetched = true;
        }
        return this.mIsThemeSupported;
    }

    private boolean isSettingOpened(Context context) {
        if (!this.mIsSettingFetched) {
            this.mIsSettingOpened = isSettingEnabledBlurEffect(context);
            this.mIsSettingFetched = true;
        }
        return this.mIsSettingOpened;
    }

    /* access modifiers changed from: private */
    public boolean onPreDraw() {
        View view;
        this.mIsSkipDrawFrame = isDrawFrameTimeout() && !this.mIsUnionAreaChanged;
        if (this.mBlurUnionRect.width() == 0 || this.mBlurUnionRect.height() == 0 || !isShowHwBlur() || (view = this.mDecorView) == null) {
            return true;
        }
        if (this.mIsUnionAreaChanged) {
            getBitmapForBlur();
            prepareBlurredBitmap();
            this.mIsUnionAreaChanged = false;
        } else {
            view.post(new Runnable() {
                /* class huawei.android.widget.effect.engine.$$Lambda$HwBlurEngine$4jYJ818cTpPpo1oCogbANZGF_dc */

                public final void run() {
                    HwBlurEngine.this.lambda$onPreDraw$0$HwBlurEngine();
                }
            });
        }
        this.mIsBitmapCopying = true;
        Bitmap bitmap = this.mBlurredBitmap;
        if (!(bitmap == null || this.mBitmapForDraw == null)) {
            this.mBitmapForDraw = bitmap.copy(bitmap.getConfig(), this.mBlurredBitmap.isMutable());
        }
        this.mIsBitmapCopying = false;
        return true;
    }

    public /* synthetic */ void lambda$onPreDraw$0$HwBlurEngine() {
        SubThreadHandler subThreadHandler;
        getBitmapForBlur();
        if (this.mSubThread != null && (subThreadHandler = this.mSubThreadHandler) != null) {
            subThreadHandler.execute(1);
        }
    }

    private boolean isDrawFrameTimeout() {
        long currentTime = System.nanoTime();
        this.mLastFrameTime = currentTime;
        this.mIsLastFrameTimeout = currentTime - this.mLastFrameTime > 16666666 && !this.mIsLastFrameTimeout;
        return this.mIsLastFrameTimeout;
    }

    /* access modifiers changed from: private */
    public class SubThread extends Thread {
        private Looper mLooper;

        private SubThread() {
        }

        public void run() {
            super.run();
            Looper.prepare();
            this.mLooper = Looper.myLooper();
            HwBlurEngine.this.mSubThreadHandler = new SubThreadHandler();
            Looper.loop();
        }

        /* access modifiers changed from: package-private */
        public void quit() {
            Looper looper = this.mLooper;
            if (looper != null) {
                looper.quitSafely();
                this.mLooper = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SubThreadHandler extends Handler {
        private SubThreadHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                HwBlurEngine.getInstance().prepareBlurredBitmap();
            }
        }

        /* access modifiers changed from: package-private */
        public void execute(int messageType) {
            if (messageType == 1) {
                removeCallbacksAndMessages(null);
                sendEmptyMessage(1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void prepareBlurredBitmap() {
        Bitmap bitmap;
        Bitmap bitmap2;
        Bitmap bitmap3;
        Canvas canvas;
        Bitmap bitmap4;
        Bitmap bitmap5;
        if (this.mIsSkipDrawFrame) {
            invalidateTargetViewsInSubThread();
            return;
        }
        Canvas canvas2 = this.mBalanceCanvas;
        if (!(canvas2 == null || (bitmap5 = this.mBitmapForBlur) == null)) {
            canvas2.drawBitmap(bitmap5, 0.0f, 0.0f, (Paint) null);
        }
        for (Map.Entry<View, HwBlurEntity> entityEntry : this.mTargetViews.entrySet()) {
            HwBlurEntity blurEntity = entityEntry.getValue();
            if (entityEntry.getKey().isShown() && blurEntity.isEnabled() && (canvas = this.mBalanceCanvas) != null && (bitmap4 = this.mBitmapForBlur) != null) {
                blurEntity.drawBitmapForBlur(canvas, bitmap4, this.mBlurUnionRect, 15);
            }
        }
        if (!(this.mIsBitmapCopying || (bitmap = this.mBalanceBitmap) == null || (bitmap2 = this.mBlurredBitmap) == null)) {
            int result = this.mAlgorithm.blur(bitmap, bitmap2, 8);
            if (result != 0 && DEBUG) {
                String str = TAG;
                Log.w(str, " mAlgorithm.blur occurred some error, error code= " + result);
            }
            Bitmap bitmap6 = this.mBlurredBitmap;
            if (!(bitmap6 == null || (bitmap3 = this.mBitmapForDraw) == null || !bitmap6.sameAs(bitmap3))) {
                return;
            }
        }
        if (this.mIsDecorViewChanged) {
            this.mIsDecorViewChanged = false;
        }
        invalidateTargetViewsInSubThread();
    }

    private void invalidateTargetViewsInSubThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            for (Map.Entry<View, HwBlurEntity> entityEntry : this.mTargetViews.entrySet()) {
                if (entityEntry.getKey().isShown() && entityEntry.getValue().isEnabled()) {
                    entityEntry.getKey().postInvalidate();
                }
            }
        }
    }

    private void getBitmapForBlur() {
        if (this.mIsSkipDrawFrame || this.mDecorView == null) {
            return;
        }
        if (this.mBlurCanvas != null || this.mTargetViews.size() != 0) {
            this.mDecorView.getGlobalVisibleRect(this.mDecorViewRect);
            if (this.mBlurCanvas == null) {
                initBitmapAndCanvas();
            }
            int saveCanvas = this.mBlurCanvas.save();
            this.mBlurCanvas.scale(0.06666667f, 0.06666667f);
            this.mBlurCanvas.translate((float) (this.mDecorViewRect.left - this.mBlurUnionRect.left), (float) (this.mDecorViewRect.top - this.mBlurUnionRect.top));
            this.mIsDrawingViewSelf = true;
            try {
                this.mDecorView.draw(this.mBlurCanvas);
            } catch (Exception e) {
            }
            this.mIsDrawingViewSelf = false;
            this.mBlurCanvas.restoreToCount(saveCanvas);
        }
    }

    private static Bitmap getBitmapForBlur(View view, int downscale) {
        Bitmap bitmapForBlur = Bitmap.createBitmap(Math.max(1, view.getWidth() / downscale), Math.max(1, view.getHeight() / downscale), Bitmap.Config.ARGB_4444);
        Canvas blurCanvas = new Canvas(bitmapForBlur);
        blurCanvas.scale(1.0f / ((float) downscale), 1.0f / ((float) downscale));
        view.draw(blurCanvas);
        return bitmapForBlur;
    }

    private static Bitmap getBitmapForBlur(Bitmap input, int downscale) {
        int scaledW = Math.max(1, input.getWidth() / downscale);
        int scaledH = Math.max(1, input.getHeight() / downscale);
        Bitmap bitmapForBlur = Bitmap.createBitmap(scaledW, scaledH, Bitmap.Config.ARGB_4444);
        Canvas blurCanvas = new Canvas(bitmapForBlur);
        RectF rectF = new RectF(0.0f, 0.0f, (float) scaledW, (float) scaledH);
        Paint paint = new Paint();
        paint.setShader(getBitmapScaleShader(input, bitmapForBlur));
        blurCanvas.drawRect(rectF, paint);
        return bitmapForBlur;
    }

    private void updateDecorView(View targetView) {
        View decorView;
        Context mContext = targetView.getContext();
        for (int i = 0; i < 4 && !(mContext instanceof Activity) && (mContext instanceof ContextWrapper); i++) {
            mContext = ((ContextWrapper) mContext).getBaseContext();
        }
        if ((mContext instanceof Activity) && (decorView = ((Activity) mContext).getWindow().getDecorView()) != this.mDecorView) {
            if (this.mSubThread == null) {
                this.mSubThread = new SubThread();
                this.mSubThread.start();
                if (DEBUG) {
                    String str = TAG;
                    Log.d(str, "updateDecorView: create new sub-thread " + this.mSubThread.getId());
                }
            }
            View view = this.mDecorView;
            if (view != null) {
                view.getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
                this.mDecorView.getViewTreeObserver().removeOnGlobalLayoutListener(this.mLayoutListener);
            }
            this.mDecorView = decorView;
            this.mDecorView.getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
            this.mDecorView.getViewTreeObserver().addOnGlobalLayoutListener(this.mLayoutListener);
            this.mIsDecorViewChanged = true;
            if (DEBUG) {
                String str2 = TAG;
                Log.d(str2, "updateDecorView: " + this.mDecorView);
            }
        }
    }

    private void destroyResources() {
        Bitmap bitmap = this.mBitmapForBlur;
        if (bitmap != null) {
            this.mBlurCanvas = null;
            bitmap.recycle();
            this.mBitmapForBlur = null;
            if (DEBUG) {
                Log.d(TAG, "destroyDecorView: destroyed mBlurCanvas and mBitmapForBlur");
            }
        }
        Bitmap bitmap2 = this.mBalanceBitmap;
        if (bitmap2 != null) {
            this.mBalanceCanvas = null;
            bitmap2.recycle();
            this.mBalanceBitmap = null;
            if (DEBUG) {
                Log.d(TAG, "destroyDecorView: destroyed mBalanceCanvas and mBalanceBitmap");
            }
        }
        Bitmap bitmap3 = this.mBlurredBitmap;
        if (bitmap3 != null) {
            bitmap3.recycle();
            this.mBlurredBitmap = null;
            if (DEBUG) {
                Log.d(TAG, "destroyDecorView: destroyed mBlurredBitmap");
            }
        }
        Bitmap bitmap4 = this.mBitmapForDraw;
        if (bitmap4 != null) {
            bitmap4.recycle();
            this.mBitmapForDraw = null;
            if (DEBUG) {
                Log.d(TAG, "destroyDecorView: destroyed mBitmapForDraw");
            }
        }
        SubThread subThread = this.mSubThread;
        if (subThread != null) {
            subThread.quit();
            this.mSubThread = null;
            if (DEBUG) {
                Log.d(TAG, "destroyResources: sub-thread Looper quited safely");
            }
        }
    }

    private void destroyDecorView() {
        View view = this.mDecorView;
        if (view != null) {
            view.getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
            this.mDecorView.getViewTreeObserver().removeOnGlobalLayoutListener(this.mLayoutListener);
            this.mDecorView = null;
            if (DEBUG) {
                Log.d(TAG, "destroyDecorView: removed listeners and destroy decor view");
            }
        }
        SubThreadHandler subThreadHandler = this.mSubThreadHandler;
        if (subThreadHandler != null) {
            subThreadHandler.removeCallbacksAndMessages(null);
            this.mSubThreadHandler = null;
            this.mSubThread.quit();
            this.mSubThread = null;
            if (DEBUG) {
                Log.d(TAG, "destroyDecorView: quit sub-thread and sub-thread handler");
            }
        }
        this.mBlurCanvas = null;
        this.mBalanceCanvas = null;
        this.mBitmapForBlur = null;
        this.mBalanceBitmap = null;
        this.mBlurredBitmap = null;
        this.mBitmapForDraw = null;
        if (DEBUG) {
            Log.d(TAG, "destroyDecorView: all bitmaps and canvas were be set to null");
        }
    }

    /* access modifiers changed from: private */
    public void updateUnionArea() {
        this.mBlurUnionRect.setEmpty();
        for (Map.Entry<View, HwBlurEntity> entityEntry : this.mTargetViews.entrySet()) {
            View targetView = entityEntry.getKey();
            HwBlurEntity blurEntity = entityEntry.getValue();
            if (targetView.isShown() && blurEntity.isEnabled()) {
                Rect targetViewRect = new Rect();
                targetView.getGlobalVisibleRect(targetViewRect);
                blurEntity.setTargetViewRect(targetViewRect);
                this.mBlurUnionRect.union(targetViewRect);
            }
            targetView.invalidate();
        }
        initBitmapAndCanvas();
    }

    private void initBitmapAndCanvas() {
        int targetWidth = this.mBlurUnionRect.width() / 15;
        int targetHeight = this.mBlurUnionRect.height() / 15;
        int scaledW = targetWidth <= 1 ? 1 : targetWidth;
        int scaledH = targetHeight <= 1 ? 1 : targetHeight;
        boolean isBitmapChanged = false;
        boolean isBitmapNull = this.mBlurredBitmap == null || this.mBalanceBitmap == null || this.mBitmapForDraw == null;
        boolean isCanvasNull = this.mBlurCanvas == null || this.mBalanceCanvas == null;
        Bitmap bitmap = this.mBitmapForBlur;
        if (!(bitmap != null && bitmap.getWidth() == scaledW && this.mBitmapForBlur.getHeight() == scaledH)) {
            isBitmapChanged = true;
        }
        if (isCanvasNull || isBitmapNull || isBitmapChanged) {
            this.mBitmapForBlur = Bitmap.createBitmap(scaledW, scaledH, Bitmap.Config.ARGB_4444);
            this.mBalanceBitmap = Bitmap.createBitmap(scaledW, scaledH, Bitmap.Config.ARGB_4444);
            this.mBlurredBitmap = Bitmap.createBitmap(scaledW, scaledH, Bitmap.Config.ARGB_4444);
            this.mBitmapForDraw = Bitmap.createBitmap(scaledW, scaledH, Bitmap.Config.ARGB_4444);
            this.mBlurCanvas = new Canvas(this.mBitmapForBlur);
            this.mBalanceCanvas = new Canvas(this.mBalanceBitmap);
            this.mIsUnionAreaChanged = true;
            if (DEBUG) {
                Log.d(TAG, "initBitmapAndCanvas: bitmap initialed. [" + scaledW + ", " + scaledH + "]");
            }
        }
    }

    public static Bitmap blur(Bitmap input, int radius, int downscale) {
        if (input == null || input.getWidth() == 0 || input.getHeight() == 0) {
            Log.e(TAG, "blur: input bitmap is null or bitmap size is 0");
            return null;
        } else if (radius <= 0 || downscale <= 0) {
            Log.e(TAG, "blur: blur radius and downscale must > 0");
            return null;
        } else {
            int blurRadius = (int) (((float) radius) / ((float) downscale));
            if (blurRadius != 0) {
                return getBlurredBitmap(getBitmapForBlur(input, downscale), blurRadius);
            }
            Log.e(TAG, "blur: blur radius downscale must < radius");
            return null;
        }
    }

    public static Bitmap blur(View view, int radius, int downscale) {
        if (view == null || view.getWidth() == 0 || view.getHeight() == 0) {
            Log.e(TAG, "blur: input view is null or view size is 0");
            return null;
        } else if (radius <= 0 || downscale <= 0) {
            Log.e(TAG, "blur: blur radius and downscale must > 0");
            return null;
        } else {
            int blurRadius = (int) (((float) radius) / ((float) downscale));
            if (blurRadius != 0) {
                return getBlurredBitmap(getBitmapForBlur(view, downscale), blurRadius);
            }
            Log.e(TAG, "blur: blur radius downscale must < radius");
            return null;
        }
    }

    private static BitmapShader getBitmapScaleShader(Bitmap in, Bitmap out) {
        float scaleRateX = ((float) out.getWidth()) / ((float) in.getWidth());
        float scaleRateY = ((float) out.getHeight()) / ((float) in.getHeight());
        Matrix matrix = new Matrix();
        matrix.postScale(scaleRateX, scaleRateY);
        Shader.TileMode tileMode = Shader.TileMode.CLAMP;
        BitmapShader shader = new BitmapShader(in, tileMode, tileMode);
        shader.setLocalMatrix(matrix);
        return shader;
    }

    private static Bitmap getBlurredBitmap(Bitmap bitmapForBlur, int radius) {
        Bitmap blurredBitmap = Bitmap.createBitmap(bitmapForBlur.getWidth(), bitmapForBlur.getHeight(), Bitmap.Config.ARGB_4444);
        BlurAlgorithm blurAlgorithm = mStaticAlgorithm;
        if (blurAlgorithm != null) {
            blurAlgorithm.blur(bitmapForBlur, blurredBitmap, radius);
        }
        return blurredBitmap;
    }

    @Deprecated
    public static boolean isEnable() {
        Log.e(TAG, "isEnable() is deprecated and will remove later.");
        return false;
    }

    @Deprecated
    public static void setGlobalEnable(boolean isEnable) {
        Log.e(TAG, "setGlobalEnable() is deprecated and will remove later.");
    }

    @Deprecated
    public void setEnable(boolean isEnable) {
        Log.e(TAG, "setEnable() is deprecated and will remove later.");
        this.mIsBlurEffectEnabled = isDeviceSupport() && isEnable;
    }

    @Deprecated
    public void onAttachedToWindow() {
        Log.e(TAG, "onAttachedToWindow() will remove later and call this method will take no effect");
    }

    @Deprecated
    public void onDetachedFromWindow() {
        Log.e(TAG, "onDetachedFromWindow() will remove later and call this method will take no effect");
    }

    @Deprecated
    public void onWindowFocusChanged(View targetView, boolean isHasWindowFocus, boolean isTargetViewEnableBlur) {
        onWindowVisibilityChanged(targetView, isHasWindowFocus);
        setTargetViewBlurEnable(targetView, isTargetViewEnableBlur);
    }

    @Deprecated
    public void onWindowFocusChanged(View targetView, boolean isHasWindowFocus) {
        onWindowVisibilityChanged(targetView, isHasWindowFocus);
        setTargetViewBlurEnable(targetView, true);
    }
}
