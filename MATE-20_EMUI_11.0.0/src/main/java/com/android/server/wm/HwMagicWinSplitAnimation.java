package com.android.server.wm;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import com.android.server.multiwin.HwBlur;
import com.huawei.android.graphics.GraphicBufferEx;
import com.huawei.android.hardware.display.DisplayManagerInternalEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.android.view.SurfaceEx;
import com.huawei.android.view.animation.TransformationEx;
import com.huawei.screenrecorder.activities.SurfaceControlEx;
import com.huawei.server.magicwin.HwMagicWinAnimation;
import com.huawei.server.utils.Utils;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

public class HwMagicWinSplitAnimation extends AnimatableEx {
    private static final int ANIMATION_LAYER;
    private static final int ANIMATION_LAYER_BACKGROUND;
    private static final int ANIMATION_LAYER_BASE = (AnimationAdapterEx.WINDOW_FREEZE_LAYER + AnimationAdapterEx.TYPE_LAYER_MULTIPLIER);
    private static final int ANIMATION_LAYER_EXIT;
    private static final int BG_COLOR = -987148;
    private static final int BLUR_DOWNSCALE = 1;
    private static final int BLUR_RADIUS = 10;
    private static final float BLUR_SCALE = 0.05f;
    private static final int COVER_COLOR = Integer.MAX_VALUE;
    private static final int DARK_BG_COLOR = -15066598;
    private static final float SCALE_FULL = 1.0f;
    private static final String TAG = "HWMW_HwMagicWinSplitAnimation";
    protected SurfaceControl mBackgourndSurface;
    protected GraphicBufferEx mBackgroundBuffer;
    protected GraphicBufferEx mGraphicBuffer;
    protected final int mHeigth;
    private final SurfaceAnimatorEx mSurfaceAnimator;
    protected SurfaceControl mSurfaceControl;
    protected WindowContainerEx mTaskStackContainers;
    protected final int mWidth;

    static {
        int i = ANIMATION_LAYER_BASE;
        ANIMATION_LAYER = i - 1;
        ANIMATION_LAYER_EXIT = i - 2;
        ANIMATION_LAYER_BACKGROUND = i - 3;
    }

    public HwMagicWinSplitAnimation(WindowContainerEx container, GraphicBufferEx buffer) {
        this(container, buffer, buffer.getWidth(), buffer.getHeight());
    }

    public HwMagicWinSplitAnimation(WindowContainerEx container, GraphicBufferEx buffer, int width, int height) {
        this.mWidth = width;
        this.mHeigth = height;
        this.mTaskStackContainers = container;
        this.mSurfaceAnimator = new SurfaceAnimatorEx(this, new Runnable() {
            /* class com.android.server.wm.$$Lambda$anjUVzawOhcJamm9PgAXPIHRBI8 */

            @Override // java.lang.Runnable
            public final void run() {
                HwMagicWinSplitAnimation.this.onAnimationFinished();
            }
        }, container.getWmService());
        this.mSurfaceControl = makeSurfaceController(TAG, this.mWidth, this.mHeigth);
        this.mGraphicBuffer = buffer;
    }

    /* access modifiers changed from: package-private */
    public void showAnimationSurface(float cornerRadius) {
        SurfaceControlEx.setWindowCrop(this.mSurfaceControl, new Rect(0, 0, this.mWidth, this.mHeigth));
        SurfaceControlEx.setCornerRadius(this.mSurfaceControl, cornerRadius);
        applyBufferToSurface(this.mSurfaceControl, this.mGraphicBuffer);
        SurfaceControl.Transaction transaction = getPendingTransaction();
        SurfaceEx.show(transaction, this.mSurfaceControl);
        transaction.apply();
    }

    /* access modifiers changed from: package-private */
    public void applyBufferToSurface(SurfaceControl surfaceControl, GraphicBufferEx buffer) {
        attachBufferToSurface(surfaceControl, buffer);
    }

    /* access modifiers changed from: package-private */
    public void showBgBufferSurface(GraphicBufferEx buffer, Point position) {
        this.mBackgroundBuffer = buffer;
        this.mBackgourndSurface = makeSurfaceController(TAG, buffer.getWidth(), buffer.getHeight());
        attachBufferToSurface(this.mBackgourndSurface, this.mBackgroundBuffer);
        showBackgroundSurface(position);
    }

    /* access modifiers changed from: package-private */
    public void showBgColoredSurface(int color, Rect bounds) {
        this.mBackgourndSurface = makeSurfaceController(TAG, bounds.width(), bounds.height());
        Surface surface = new Surface(this.mBackgourndSurface);
        Canvas canvas = null;
        try {
            canvas = surface.lockCanvas(bounds);
        } catch (Surface.OutOfResourcesException | IllegalArgumentException e) {
            SlogEx.i(TAG, "lockCanvas exception");
        }
        if (canvas != null) {
            canvas.drawColor(color);
            surface.unlockCanvasAndPost(canvas);
            surface.release();
            showBackgroundSurface(new Point(bounds.left, bounds.top));
        }
    }

    private void destroyGraphicBuffer(GraphicBufferEx buffer) {
        if (buffer != null && !buffer.isDestroyed()) {
            buffer.destroy();
        }
    }

    private void destroySurfaceControl(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl) {
        if (surfaceControl != null) {
            SurfaceEx.remove(transaction, surfaceControl);
        }
    }

    private SurfaceControl makeSurfaceController(String surfaceName, int width, int height) {
        return this.mTaskStackContainers.makeChildSurface((WindowContainerEx) null).setParent(this.mTaskStackContainers.getSurfaceControl()).setName(surfaceName).setFormat(-3).setBufferSize(width, height).build();
    }

    private void attachBufferToSurface(SurfaceControl sc, GraphicBufferEx buffer) {
        Surface surface = new Surface(sc);
        SurfaceEx.attachAndQueueBuffer(surface, buffer);
        surface.release();
    }

    private void showBackgroundSurface(Point position) {
        SurfaceControl.Transaction transaction = getPendingTransaction();
        SurfaceEx.setPosition(transaction, this.mBackgourndSurface, (float) position.x, (float) position.y);
        transaction.setLayer(this.mBackgourndSurface, ANIMATION_LAYER_BACKGROUND);
        SurfaceEx.show(transaction, this.mBackgourndSurface);
        transaction.apply();
    }

    private void destroyAnimation(SurfaceControl.Transaction transaction) {
        destroyGraphicBuffer(this.mGraphicBuffer);
        this.mGraphicBuffer = null;
        destroySurfaceControl(transaction, this.mSurfaceControl);
        this.mSurfaceControl = null;
        destroyGraphicBuffer(this.mBackgroundBuffer);
        this.mBackgroundBuffer = null;
        destroySurfaceControl(transaction, this.mBackgourndSurface);
        this.mBackgourndSurface = null;
    }

    public SurfaceControl.Transaction getPendingTransaction() {
        return this.mTaskStackContainers.getPendingTransaction();
    }

    public void commitPendingTransaction() {
        this.mTaskStackContainers.commitPendingTransaction();
    }

    public void onAnimationLeashCreated(SurfaceControl.Transaction transaction, SurfaceControl leash) {
        transaction.setLayer(leash, ANIMATION_LAYER);
    }

    public void onAnimationLeashLost(SurfaceControl.Transaction transaction) {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            SurfaceEx.hide(transaction, surfaceControl);
        }
        SurfaceControl surfaceControl2 = this.mBackgourndSurface;
        if (surfaceControl2 != null) {
            SurfaceEx.hide(transaction, surfaceControl2);
        }
    }

    public SurfaceControl.Builder makeAnimationLeash() {
        return this.mTaskStackContainers.makeAnimationLeash();
    }

    public SurfaceControl getAnimationLeashParent() {
        return this.mTaskStackContainers.getSurfaceControl();
    }

    public SurfaceControl getSurfaceControl() {
        return this.mSurfaceControl;
    }

    public SurfaceControl getParentSurfaceControl() {
        return this.mTaskStackContainers.getSurfaceControl();
    }

    public int getSurfaceWidth() {
        return this.mWidth;
    }

    public int getSurfaceHeight() {
        return this.mHeigth;
    }

    /* access modifiers changed from: package-private */
    public void onAnimationFinished() {
        destroyAnimation(getPendingTransaction());
    }

    /* access modifiers changed from: package-private */
    public void stopAnimation() {
        this.mSurfaceAnimator.cancelAnimation();
        destroyAnimation(getPendingTransaction());
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(AnimationAdapterEx anim, boolean isHidden) {
        this.mSurfaceAnimator.startAnimation(getPendingTransaction(), anim, isHidden);
    }

    public static class BlurWindowAnimation extends HwMagicWinSplitAnimation {
        BlurWindowAnimation(WindowContainerEx container, GraphicBufferEx buffer, Rect surfaceBounds) {
            super(container, buffer, surfaceBounds.width(), surfaceBounds.height());
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.HwMagicWinSplitAnimation
        public void applyBufferToSurface(SurfaceControl sc, GraphicBufferEx buffer) {
            Surface surface = new Surface(sc);
            Rect bounds = new Rect(0, 0, this.mWidth, this.mHeigth);
            Canvas canvas = null;
            try {
                canvas = surface.lockCanvas(bounds);
            } catch (Surface.OutOfResourcesException | IllegalArgumentException e) {
                SlogEx.i(HwMagicWinSplitAnimation.TAG, "lockCanvas exception");
            }
            if (canvas != null) {
                Bitmap blurBitmap = null;
                Bitmap hwBitmap = null;
                try {
                    canvas.drawColor(-1);
                    hwBitmap = GraphicBufferEx.wrapHardwareBuffer(buffer, (ColorSpace) null);
                    blurBitmap = HwBlur.blur(hwBitmap, 10, 1, false);
                    canvas.drawBitmap(blurBitmap, (Rect) null, bounds, new Paint(5));
                    canvas.drawColor(HwMagicWinSplitAnimation.COVER_COLOR);
                    surface.unlockCanvasAndPost(canvas);
                    surface.release();
                } finally {
                    if (blurBitmap != null && !blurBitmap.isRecycled()) {
                        blurBitmap.recycle();
                    }
                    if (hwBitmap != null && !hwBitmap.isRecycled()) {
                        hwBitmap.recycle();
                    }
                    if (!buffer.isDestroyed()) {
                        buffer.destroy();
                    }
                }
            }
        }
    }

    public static class SplitScreenAnimation {
        private final Rect mTmpRect = new Rect();

        SplitScreenAnimation() {
        }

        private GraphicBufferEx captureLayers(TaskEx captrueTask, float scale) {
            captrueTask.getBounds(this.mTmpRect);
            this.mTmpRect.offsetTo(0, 0);
            return SurfaceEx.captureLayers(captrueTask.getSurfaceControl(), this.mTmpRect, scale);
        }

        private HwMagicWinSplitAnimation createEnterAnimation(TaskEx enterTask, boolean isDrawGaussBlur) {
            DisplayContentEx displayContent = enterTask.getDisplayContentEx();
            if (isDrawGaussBlur) {
                return new BlurWindowAnimation(displayContent.getTaskStackContainers(), captureLayers(enterTask, HwMagicWinSplitAnimation.BLUR_SCALE), enterTask.getBounds());
            }
            return new HwMagicWinSplitAnimation(displayContent.getTaskStackContainers(), captureLayers(enterTask, HwMagicWinSplitAnimation.SCALE_FULL));
        }

        private HwMagicWinSplitAnimation createExitAnimation(TaskEx exitTask) {
            return new ExitTaskAnimation(exitTask.getDisplayContentEx().getTaskStackContainers(), captureLayers(exitTask, HwMagicWinSplitAnimation.SCALE_FULL));
        }

        /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
            com.huawei.android.util.SlogEx.i(com.android.server.wm.HwMagicWinSplitAnimation.TAG, "create screen shot fail!");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
            return;
         */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x00cd A[ExcHandler: ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException (e java.lang.Throwable), Splitter:B:10:0x002f] */
        private void showBackgroundSurface(HwMagicWinSplitAnimation splitAnimation, int displayId, DisplayContentEx displayContent, boolean isScreenshot) {
            Bitmap screenShot = null;
            DisplayInfoEx displayInfo = displayContent.getDisplayInfoEx();
            Rect screenBounds = new Rect(0, 0, displayInfo.getLogicalWidth(), displayInfo.getLogicalHeight());
            if (!isScreenshot) {
                splitAnimation.showBgColoredSurface(Utils.isNightMode(splitAnimation.mTaskStackContainers.getWmService().getContext()) ? HwMagicWinSplitAnimation.DARK_BG_COLOR : HwMagicWinSplitAnimation.BG_COLOR, screenBounds);
            } else if (displayId == 0 || displayId == HwMagicWinManager.getInstance().getVirtualDisplayId()) {
                if (displayId == 0) {
                    try {
                        screenShot = SurfaceEx.screenshot(screenBounds, screenBounds.width(), screenBounds.height(), displayContent.getRotation());
                    } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
                    } catch (Throwable th) {
                        if (0 != 0 && !screenShot.isRecycled()) {
                            screenShot.recycle();
                        }
                        throw th;
                    }
                } else {
                    screenShot = (Bitmap) Class.forName("android.view.SurfaceControl").getDeclaredMethod("screenshot", IBinder.class, Integer.TYPE, Integer.TYPE).invoke(null, DisplayManagerInternalEx.getInstance().getDisplayToken(displayId), Integer.valueOf(screenBounds.width()), Integer.valueOf(screenBounds.height()));
                }
                if (screenShot != null) {
                    splitAnimation.showBgBufferSurface(GraphicBufferEx.createGraphicBufferHandle(screenShot), new Point(screenBounds.left, screenBounds.top));
                } else if (screenShot != null && !screenShot.isRecycled()) {
                    screenShot.recycle();
                    return;
                } else {
                    return;
                }
            } else if (0 != 0 && !screenShot.isRecycled()) {
                screenShot.recycle();
                return;
            } else {
                return;
            }
            if (screenShot == null || screenShot.isRecycled()) {
                return;
            }
            screenShot.recycle();
        }

        public void startSplitScreenAnimation(AppWindowTokenExt appToken, HwMagicWinAnimation.AnimationParams animParams, boolean isScreenshot, float cornerRadius, int displayId) {
            if (appToken != null && appToken.getTaskEx() != null) {
                HwMagicWinSplitAnimation splitScreenAnimation = createEnterAnimation(appToken.getTaskEx(), true);
                if (!isScreenshot) {
                    splitScreenAnimation.showAnimationSurface(cornerRadius);
                }
                showBackgroundSurface(splitScreenAnimation, displayId, appToken.getDisplayContentEx(), isScreenshot);
                if (isScreenshot) {
                    splitScreenAnimation.showAnimationSurface(cornerRadius);
                }
                splitScreenAnimation.startAnimation(AnimationAdapterEx.getAnimationAdapterEx(new SplitScreenAnimationSpec(animParams.getAnimation(), null, splitScreenAnimation.mBackgourndSurface, animParams.getHideThreshold()), appToken), false);
            }
        }

        public void startMultiTaskAnimation(TaskEx enterTask, TaskEx exitTask, int displayId, HwMagicWinAnimation.AnimationParams enterAnimParams, HwMagicWinAnimation.AnimationParams exitAnimParams, boolean isDrawGaussBlur) {
            if (enterTask != null && exitTask != null) {
                HwMagicWinSplitAnimation enterTaskAnim = createEnterAnimation(enterTask, isDrawGaussBlur);
                HwMagicWinSplitAnimation exitTaskAnimation = createExitAnimation(exitTask);
                enterTaskAnim.showAnimationSurface(HwMagicWinAnimation.INVALID_THRESHOLD);
                exitTaskAnimation.showAnimationSurface(HwMagicWinAnimation.INVALID_THRESHOLD);
                showBackgroundSurface(enterTaskAnim, displayId, enterTask.getDisplayContentEx(), false);
                enterTaskAnim.startAnimation(AnimationAdapterEx.getAnimationAdapterEx(new SplitScreenAnimationSpec(enterAnimParams.getAnimation(), null, enterTaskAnim.mBackgourndSurface, enterAnimParams.getHideThreshold()), enterTask), false);
                exitTaskAnimation.startAnimation(AnimationAdapterEx.getAnimationAdapterEx(new SplitScreenAnimationSpec(exitAnimParams.getAnimation(), new Point(exitTask.getBounds().left, exitTask.getBounds().top)), exitTask), false);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class ExitTaskAnimation extends HwMagicWinSplitAnimation {
        ExitTaskAnimation(WindowContainerEx container, GraphicBufferEx buffer) {
            super(container, buffer);
        }

        @Override // com.android.server.wm.HwMagicWinSplitAnimation
        public void onAnimationLeashCreated(SurfaceControl.Transaction transaction, SurfaceControl leash) {
            transaction.setLayer(leash, HwMagicWinSplitAnimation.ANIMATION_LAYER_EXIT);
        }
    }

    /* access modifiers changed from: private */
    public static class TmpValues {
        private static final int MATRIX_LENGTH = 9;
        final float[] floats;
        final Transformation transformation;

        private TmpValues() {
            this.transformation = new Transformation();
            this.floats = new float[9];
        }
    }

    /* access modifiers changed from: private */
    public static class SplitScreenAnimationSpec extends AnimationSpecEx {
        private long mAnimDuration;
        private final Point mAnimPosition;
        private long mAnimStartOffset;
        private long mAnimStartTime;
        private final Animation mAnimation;
        private final WeakReference<SurfaceControl> mHideSurface;
        private float mHideThreshold;
        private boolean mIsHasHidedSurface;
        private final ThreadLocal<TmpValues> mThreadLocalTmps;

        static /* synthetic */ TmpValues lambda$new$0() {
            return new TmpValues();
        }

        SplitScreenAnimationSpec(Animation animation) {
            this(animation, null, null, HwMagicWinAnimation.INVALID_THRESHOLD);
        }

        SplitScreenAnimationSpec(Animation animation, Point animPos) {
            this(animation, animPos, null, HwMagicWinAnimation.INVALID_THRESHOLD);
        }

        SplitScreenAnimationSpec(Animation animation, Point animPos, SurfaceControl hideSurface, float hideThreshold) {
            this.mAnimStartTime = 0;
            this.mAnimStartOffset = 0;
            this.mAnimDuration = 0;
            this.mAnimPosition = new Point();
            this.mIsHasHidedSurface = false;
            this.mThreadLocalTmps = ThreadLocal.withInitial($$Lambda$HwMagicWinSplitAnimation$SplitScreenAnimationSpec$CZkToanMKGprdMH6KQBXhW4Ufzw.INSTANCE);
            this.mHideThreshold = HwMagicWinAnimation.INVALID_THRESHOLD;
            this.mAnimation = animation;
            if (animPos != null) {
                this.mAnimPosition.set(animPos.x, animPos.y);
            }
            this.mHideSurface = new WeakReference<>(hideSurface);
            if (hideSurface != null) {
                this.mAnimStartTime = animation.getStartTime();
                this.mAnimStartOffset = animation.getStartOffset();
                this.mAnimDuration = animation.computeDurationHint();
                this.mHideThreshold = hideThreshold;
            }
        }

        public long getDuration() {
            return this.mAnimation.computeDurationHint();
        }

        public void apply(SurfaceControl.Transaction transaction, SurfaceControl leash, long currentPlayTime) {
            TmpValues tmp = this.mThreadLocalTmps.get();
            tmp.transformation.clear();
            this.mAnimation.getTransformation(currentPlayTime, tmp.transformation);
            tmp.transformation.getMatrix().postTranslate((float) this.mAnimPosition.x, (float) this.mAnimPosition.y);
            SurfaceEx.setMatrix(transaction, leash, tmp.transformation.getMatrix(), tmp.floats);
            transaction.setAlpha(leash, tmp.transformation.getAlpha());
            if (TransformationEx.hasClipRect(tmp.transformation)) {
                SurfaceEx.setWindowCrop(transaction, leash, TransformationEx.getClipRect(tmp.transformation));
            }
            hideSurface(transaction, currentPlayTime);
        }

        private void hideSurface(SurfaceControl.Transaction transaction, long currentPlayTime) {
            if (this.mHideSurface.get() != null && !this.mIsHasHidedSurface) {
                long j = this.mAnimDuration;
                if (j > 0) {
                    float f = this.mHideThreshold;
                    if (f + HwMagicWinSplitAnimation.SCALE_FULL > HwMagicWinSplitAnimation.SCALE_FULL && (((float) (currentPlayTime - (this.mAnimStartTime + this.mAnimStartOffset))) * HwMagicWinSplitAnimation.SCALE_FULL) / ((float) j) >= f) {
                        SurfaceEx.hide(transaction, this.mHideSurface.get());
                        this.mIsHasHidedSurface = true;
                    }
                }
            }
        }
    }
}
