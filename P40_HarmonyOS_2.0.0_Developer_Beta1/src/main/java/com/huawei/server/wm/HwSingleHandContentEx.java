package com.huawei.server.wm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.provider.Settings;
import android.view.SurfaceControl;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import com.android.server.wm.TransactionEx;
import com.android.server.wm.WindowManagerServiceEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.DisplayInfoEx;

public class HwSingleHandContentEx extends HwSingleHandContentExBridgeEx implements IsingleHandInner {
    private static final int ANIMATION_TIME = 200;
    private static final int APP_ANIMATION_TIME = 50;
    private static final float DEFAULT_SCALE = 1.0f;
    private static final int FREEZE_ROTATION = -1;
    private static final float PATH_INTERPOLATOR_X = 0.4f;
    private static final String TAG = "HwSingleHandContentEx";
    private float mCurrentScale = 1.0f;
    private int mEnterAnimationTime = ANIMATION_TIME;
    private ValueAnimator mEnteringValueAnimator;
    private ValueAnimator mExitingValueAnimator;
    private boolean mIsEnteringLazyMode = false;
    private boolean mIsExitingLazyMode = false;
    private boolean mIsQuickMode = false;
    private boolean mIsQuickQuitMode = false;
    private int mLastLazyMode = WindowManagerServiceEx.LAZY_MODE_NOMAL;
    private SurfaceControl mOverLayerSurfaceControl;
    private TransactionEx mSingleHandTraction = new TransactionEx(this.mTransaction);
    private SurfaceControl mSurfaceControl;
    private SurfaceControl.Transaction mTransaction = new SurfaceControl.Transaction();
    private WindowManagerServiceEx mWmService = getServiceEx();

    public HwSingleHandContentEx(WindowManagerServiceEx serviceEx) {
        super(serviceEx);
    }

    /* access modifiers changed from: package-private */
    public Point getCurrentPoint(boolean isEntering, float scale) {
        if (!isEntering) {
            return this.mWmService.getOriginPointForLazyMode(scale, this.mLastLazyMode);
        }
        WindowManagerServiceEx windowManagerServiceEx = this.mWmService;
        return windowManagerServiceEx.getOriginPointForLazyMode(scale, windowManagerServiceEx.getLazyMode());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelCropCornerLocked() {
        this.mSingleHandTraction.setWindowCrop(this.mSurfaceControl, new Rect(0, 0, 0, 0));
        this.mSingleHandTraction.setCornerRadius(this.mSurfaceControl, 0.0f);
    }

    private void cropCorner() {
        float radius = SingleHandUtils.getDeviceRoundRadiusSize(this.mWmService.getContext().getResources().getDisplayMetrics()) * 1.0f;
        synchronized (this.mWmService.getGlobalLock()) {
            this.mSingleHandTraction.setWindowCrop(this.mSurfaceControl, getCurrentScreenRegionLocked());
            this.mSingleHandTraction.setCornerRadius(this.mSurfaceControl, radius);
            this.mSingleHandTraction.getTransaction().apply();
        }
    }

    private Rect getCurrentScreenRegionLocked() {
        DisplayInfoEx defaultDisplayInfo = this.mWmService.getDefaultDisplayContentLocked().getDisplayInfoEx();
        return new Rect(0, 0, defaultDisplayInfo.getLogicalWidth(), defaultDisplayInfo.getLogicalHeight());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void translationMatrix(boolean isEntering) {
        Point point = getCurrentPoint(isEntering, this.mCurrentScale);
        SlogEx.i(TAG, "mLastLazyMode:" + this.mLastLazyMode + " handleLazyMode point: " + point + "  scale is " + this.mCurrentScale + " isEntering: " + isEntering);
        synchronized (this.mWmService.getGlobalLock()) {
            this.mSingleHandTraction.setMatrix(this.mSurfaceControl, this.mCurrentScale, 0.0f, 0.0f, this.mCurrentScale).setPosition(this.mSurfaceControl, (float) point.x, (float) point.y);
            this.mSingleHandTraction.getTransaction().apply();
        }
        Settings.Global.putFloat(this.mWmService.getContext().getContentResolver(), SingleHandUtils.KEY_SINGLE_HAND_SCREEN_SCALE, this.mCurrentScale);
    }

    /* access modifiers changed from: package-private */
    public boolean startEnteringAnimation() {
        ValueAnimator valueAnimator = this.mEnteringValueAnimator;
        if (valueAnimator == null || !valueAnimator.isRunning()) {
            this.mWmService.setAnimatorLazyMode(true);
            if (!this.mIsQuickMode) {
                synchronized (this.mWmService.getGlobalLock()) {
                    this.mSingleHandTraction.getTransaction().setAlpha(this.mOverLayerSurfaceControl, 0.0f);
                    this.mSingleHandTraction.getTransaction().apply();
                }
            }
            this.mEnteringValueAnimator = ValueAnimator.ofFloat(1.0f, WindowManagerServiceEx.LAZY_MODE_SCALE);
            this.mEnteringValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.huawei.server.wm.HwSingleHandContentEx.AnonymousClass1 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    HwSingleHandContentEx.this.mCurrentScale = ((Float) animation.getAnimatedValue()).floatValue();
                    HwSingleHandContentEx.this.translationMatrix(true);
                }
            });
            this.mEnteringValueAnimator.addListener(new AnimatorListenerAdapter() {
                /* class com.huawei.server.wm.HwSingleHandContentEx.AnonymousClass2 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    SlogEx.i(HwSingleHandContentEx.TAG, "enter onAnimationStart");
                    HwSingleHandContentEx.this.mIsEnteringLazyMode = true;
                    synchronized (HwSingleHandContentEx.this.mWmService.getGlobalLock()) {
                        HwSingleHandContentEx.this.mWmService.getDefaultDisplayContentLocked().pauseRotationLocked();
                    }
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    SlogEx.i(HwSingleHandContentEx.TAG, "enter onAnimationEnd");
                    super.onAnimationEnd(animation);
                    synchronized (HwSingleHandContentEx.this.mWmService.getGlobalLock()) {
                        if (!HwSingleHandContentEx.this.mIsQuickMode) {
                            HwSingleHandContentEx.this.mSingleHandTraction.getTransaction().setAlpha(HwSingleHandContentEx.this.mOverLayerSurfaceControl, 1.0f);
                        }
                        HwSingleHandContentEx.this.mWmService.getDefaultDisplayContentLocked().resumeRotationLocked();
                        HwSingleHandContentEx.this.cancelCropCornerLocked();
                        HwSingleHandContentEx.this.mSingleHandTraction.getTransaction().apply();
                        HwSingleHandContentEx.this.mWmService.setAnimatorLazyMode(false);
                    }
                    HwSingleHandContentEx.this.mCurrentScale = WindowManagerServiceEx.LAZY_MODE_SCALE;
                    HwSingleHandContentEx hwSingleHandContentEx = HwSingleHandContentEx.this;
                    hwSingleHandContentEx.mLastLazyMode = hwSingleHandContentEx.mWmService.getLazyMode();
                    HwSingleHandContentEx.this.mIsEnteringLazyMode = false;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animation) {
                    SlogEx.i(HwSingleHandContentEx.TAG, "enter onAnimationCancel");
                    synchronized (HwSingleHandContentEx.this.mWmService.getGlobalLock()) {
                        if (!HwSingleHandContentEx.this.mIsQuickMode) {
                            HwSingleHandContentEx.this.mSingleHandTraction.getTransaction().setAlpha(HwSingleHandContentEx.this.mOverLayerSurfaceControl, 1.0f);
                        }
                        HwSingleHandContentEx.this.cancelCropCornerLocked();
                        HwSingleHandContentEx.this.mSingleHandTraction.getTransaction().apply();
                        HwSingleHandContentEx.this.mIsEnteringLazyMode = false;
                        HwSingleHandContentEx.this.mLastLazyMode = HwSingleHandContentEx.this.mWmService.getLazyMode();
                    }
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animation) {
                }
            });
            this.mEnteringValueAnimator.setInterpolator(new PathInterpolator(PATH_INTERPOLATOR_X, 0.0f, PATH_INTERPOLATOR_X, 1.0f));
            if (this.mIsQuickMode) {
                this.mEnterAnimationTime = APP_ANIMATION_TIME;
            } else {
                this.mEnterAnimationTime = ANIMATION_TIME;
            }
            this.mEnteringValueAnimator.setDuration((long) this.mEnterAnimationTime);
            cropCorner();
            if (this.mWmService.getAnimationHandler() != null) {
                this.mWmService.getAnimationHandler().post(new Runnable() {
                    /* class com.huawei.server.wm.HwSingleHandContentEx.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        HwSingleHandContentEx.this.mEnteringValueAnimator.start();
                    }
                });
            }
            return true;
        }
        SlogEx.i(TAG, "startEnteringAnimation animator is running");
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean startExitingAnimation() {
        ValueAnimator valueAnimator = this.mExitingValueAnimator;
        if (valueAnimator == null || !valueAnimator.isRunning()) {
            this.mExitingValueAnimator = ValueAnimator.ofFloat(WindowManagerServiceEx.LAZY_MODE_SCALE, 1.0f);
            this.mExitingValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.huawei.server.wm.HwSingleHandContentEx.AnonymousClass4 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    HwSingleHandContentEx.this.mCurrentScale = ((Float) animation.getAnimatedValue()).floatValue();
                    HwSingleHandContentEx.this.translationMatrix(false);
                }
            });
            this.mExitingValueAnimator.addListener(new AnimatorListenerAdapter() {
                /* class com.huawei.server.wm.HwSingleHandContentEx.AnonymousClass5 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    SlogEx.i(HwSingleHandContentEx.TAG, "exit onAnimationStart");
                    HwSingleHandContentEx.this.mIsExitingLazyMode = true;
                    HwSingleHandContentEx.this.mCurrentScale = WindowManagerServiceEx.LAZY_MODE_SCALE;
                    HwSingleHandContentEx.this.translationMatrix(false);
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    SlogEx.i(HwSingleHandContentEx.TAG, "exit onAnimationEnd");
                    super.onAnimationEnd(animation);
                    synchronized (HwSingleHandContentEx.this.mWmService.getGlobalLock()) {
                        HwSingleHandContentEx.this.mSingleHandTraction.getTransaction().setAlpha(HwSingleHandContentEx.this.mOverLayerSurfaceControl, 1.0f);
                        HwSingleHandContentEx.this.cancelCropCornerLocked();
                        HwSingleHandContentEx.this.mSingleHandTraction.getTransaction().apply();
                    }
                    HwSingleHandContentEx.this.mCurrentScale = 1.0f;
                    HwSingleHandContentEx.this.mIsExitingLazyMode = false;
                    HwSingleHandContentEx hwSingleHandContentEx = HwSingleHandContentEx.this;
                    hwSingleHandContentEx.mLastLazyMode = hwSingleHandContentEx.mWmService.getLazyMode();
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animation) {
                    SlogEx.i(HwSingleHandContentEx.TAG, "exit onAnimationCancel");
                    synchronized (HwSingleHandContentEx.this.mWmService.getGlobalLock()) {
                        HwSingleHandContentEx.this.cancelCropCornerLocked();
                        HwSingleHandContentEx.this.mSingleHandTraction.getTransaction().setAlpha(HwSingleHandContentEx.this.mOverLayerSurfaceControl, 1.0f);
                        HwSingleHandContentEx.this.mSingleHandTraction.getTransaction().apply();
                        HwSingleHandContentEx.this.mIsExitingLazyMode = false;
                        HwSingleHandContentEx.this.mLastLazyMode = HwSingleHandContentEx.this.mWmService.getLazyMode();
                    }
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animation) {
                }
            });
            this.mExitingValueAnimator.setInterpolator(new LinearInterpolator());
            this.mExitingValueAnimator.setDuration(200L);
            this.mSingleHandTraction.getTransaction().setAlpha(this.mOverLayerSurfaceControl, 0.0f);
            cropCorner();
            if (this.mWmService.getAnimationHandler() != null) {
                this.mWmService.getAnimationHandler().post(new Runnable() {
                    /* class com.huawei.server.wm.HwSingleHandContentEx.AnonymousClass6 */

                    @Override // java.lang.Runnable
                    public void run() {
                        HwSingleHandContentEx.this.mExitingValueAnimator.start();
                    }
                });
            }
            return true;
        }
        SlogEx.i(TAG, "startExitingAnimation animator is running");
        return false;
    }

    public void handleSingleHandMode(TransactionEx transaction, SurfaceControl winLayer, SurfaceControl overLayer) {
        this.mSurfaceControl = winLayer;
        this.mOverLayerSurfaceControl = overLayer;
    }

    @Override // com.huawei.server.wm.IsingleHandInner
    public boolean isDoAnimation() {
        if (!this.mIsEnteringLazyMode && !this.mIsExitingLazyMode) {
            return false;
        }
        SlogEx.i(TAG, "mEnteringLazyMode: " + this.mIsEnteringLazyMode + " mExitingLazyMode: " + this.mIsExitingLazyMode);
        return true;
    }

    private void doExitAnimation() {
        ValueAnimator valueAnimator;
        if (this.mIsEnteringLazyMode && (valueAnimator = this.mEnteringValueAnimator) != null && valueAnimator.isRunning()) {
            this.mEnteringValueAnimator.cancel();
        }
        this.mIsExitingLazyMode = true;
        if (!startExitingAnimation()) {
            SlogEx.d(TAG, "mIsExitingLazyMode false, maybe repeat");
        }
    }

    private void doEnterAnimation() {
        ValueAnimator valueAnimator;
        if (this.mIsExitingLazyMode && (valueAnimator = this.mExitingValueAnimator) != null && valueAnimator.isRunning()) {
            this.mExitingValueAnimator.cancel();
        }
        this.mIsEnteringLazyMode = true;
        if (!startEnteringAnimation()) {
            SlogEx.i(TAG, "mIsEnteringLazyMode false, maybe repeat");
        }
    }

    @Override // com.huawei.server.wm.IsingleHandInner
    public void doAnimation(boolean isQuickMode, boolean isEntering) {
        this.mIsQuickMode = isQuickMode;
        if (this.mIsQuickQuitMode) {
            SlogEx.i(TAG, "In Quick Quit Mode, cancel animation");
        } else if (isEntering) {
            doEnterAnimation();
        } else {
            doExitAnimation();
        }
    }

    @Override // com.huawei.server.wm.IsingleHandInner
    public void relayoutMatrix() {
        this.mCurrentScale = WindowManagerServiceEx.LAZY_MODE_SCALE;
        Point point = getCurrentPoint(true, this.mCurrentScale);
        synchronized (this.mWmService.getGlobalLock()) {
            this.mSingleHandTraction.setMatrix(this.mSurfaceControl, this.mCurrentScale, 0.0f, 0.0f, this.mCurrentScale).setPosition(this.mSurfaceControl, (float) point.x, (float) point.y);
            this.mSingleHandTraction.getTransaction().apply();
        }
    }

    @Override // com.huawei.server.wm.IsingleHandInner
    public WindowManagerServiceEx getWindowManagerServiceEx() {
        return getServiceEx();
    }

    @Override // com.huawei.server.wm.IsingleHandInner
    public void setQuickQuitMode(boolean isQuick) {
        this.mIsQuickQuitMode = isQuick;
        if (!isQuick) {
            synchronized (this.mWmService.getGlobalLock()) {
                this.mSingleHandTraction.getTransaction().setAlpha(this.mOverLayerSurfaceControl, 1.0f);
                this.mSingleHandTraction.getTransaction().apply();
            }
        }
    }

    @Override // com.huawei.server.wm.IsingleHandInner
    public void doQuickQuitLazyMode() {
        this.mCurrentScale = 1.0f;
        Point point = getCurrentPoint(false, this.mCurrentScale);
        synchronized (this.mWmService.getGlobalLock()) {
            this.mSingleHandTraction.getTransaction().setAlpha(this.mOverLayerSurfaceControl, 0.0f);
            this.mSingleHandTraction.setMatrix(this.mSurfaceControl, this.mCurrentScale, 0.0f, 0.0f, this.mCurrentScale).setPosition(this.mSurfaceControl, (float) point.x, (float) point.y);
            this.mSingleHandTraction.getTransaction().apply();
            this.mWmService.freezeOrThawRotation((int) FREEZE_ROTATION);
        }
        this.mLastLazyMode = this.mWmService.getLazyMode();
    }
}
