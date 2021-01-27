package com.android.server.gesture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.server.gesture.anim.HwGestureSideLayout;
import com.huawei.utils.HwPartResourceUtils;

public class SlideOutContainer extends FrameLayout {
    private static final long EXIT_START_DELAY = 150;
    private final Runnable mHideImmediatelyRunnable;
    private final Runnable mHideRunnable;
    private boolean mIsAnimatingOut;
    private HwGestureSideLayout mOrb;
    private SlideOutCircleView mOrbLegacy;
    private View mScrim;

    public SlideOutContainer(Context context) {
        this(context, null);
    }

    public SlideOutContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideOutContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mHideImmediatelyRunnable = new Runnable() {
            /* class com.android.server.gesture.SlideOutContainer.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                SlideOutContainer.this.show(false, false);
            }
        };
        this.mHideRunnable = new Runnable() {
            /* class com.android.server.gesture.SlideOutContainer.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                SlideOutContainer.this.show(false, true);
            }
        };
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mScrim = findViewById(HwPartResourceUtils.getResourceId("slide_out_scrim"));
        this.mOrb = (HwGestureSideLayout) findViewById(HwPartResourceUtils.getResourceId("slide_out_circle"));
        this.mOrbLegacy = (SlideOutCircleView) findViewById(HwPartResourceUtils.getResourceId("slide_out_circle_legacy"));
        if (this.mOrbLegacy != null && this.mOrb != null) {
            showOrbView();
        }
    }

    private void showOrbLegacyView() {
        this.mOrbLegacy.setVisibility(0);
        this.mOrb.setVisibility(8);
        this.mOrb = null;
    }

    private void showOrbView() {
        this.mOrb.setVisibility(0);
        this.mOrbLegacy.setVisibility(8);
        this.mOrbLegacy = null;
    }

    public void show(boolean isShow) {
        setVisibility(isShow ? 0 : 4);
    }

    public void show(boolean isShow, boolean isAnimate) {
        if (isShow) {
            if (getVisibility() != 0) {
                setVisibility(0);
                if (isAnimate) {
                    startEnterAnimation();
                } else {
                    reset();
                }
            }
        } else if (isAnimate) {
            startExitAnimation(new Runnable() {
                /* class com.android.server.gesture.SlideOutContainer.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    SlideOutContainer.this.mIsAnimatingOut = false;
                    SlideOutContainer.this.setVisibility(8);
                }
            }, false, false);
        } else {
            setVisibility(8);
        }
    }

    public void hide(boolean isAnimate) {
        if (isAnimate) {
            startAbortAnimation();
        } else {
            setVisibility(4);
        }
    }

    public void reset() {
        this.mIsAnimatingOut = false;
        SlideOutCircleView slideOutCircleView = this.mOrbLegacy;
        if (slideOutCircleView != null) {
            slideOutCircleView.reset();
        }
        this.mScrim.setAlpha(1.0f);
    }

    public void startEnterAnimation() {
        if (!this.mIsAnimatingOut) {
            HwGestureSideLayout hwGestureSideLayout = this.mOrb;
            if (hwGestureSideLayout != null) {
                hwGestureSideLayout.startEnterAnimation();
            }
            SlideOutCircleView slideOutCircleView = this.mOrbLegacy;
            if (slideOutCircleView != null) {
                slideOutCircleView.startEnterAnimation();
            }
            this.mScrim.setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
            post(new Runnable() {
                /* class com.android.server.gesture.SlideOutContainer.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    SlideOutContainer.this.mScrim.animate().alpha(1.0f).setDuration(300).setStartDelay(0).setInterpolator(new PathInterpolator(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.2f, 1.0f));
                }
            });
        }
    }

    public void startExitAnimation(boolean isFastSlide, boolean isStartMaskAnimation) {
        startExitAnimation(this.mHideRunnable, isFastSlide, isStartMaskAnimation);
    }

    public void startExitAnimation(Runnable endRunnable, boolean isFastSlide, boolean isStartMaskAnimation) {
        if (!this.mIsAnimatingOut) {
            this.mIsAnimatingOut = true;
            SlideOutCircleView slideOutCircleView = this.mOrbLegacy;
            if (slideOutCircleView != null) {
                slideOutCircleView.startExitAnimation(this.mHideImmediatelyRunnable, isFastSlide);
            }
            HwGestureSideLayout hwGestureSideLayout = this.mOrb;
            if (hwGestureSideLayout != null) {
                hwGestureSideLayout.startExitAnimation(this.mHideImmediatelyRunnable, isFastSlide, isStartMaskAnimation);
            }
            this.mScrim.animate().alpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO).setDuration(250).setStartDelay(EXIT_START_DELAY).setInterpolator(new PathInterpolator(0.4f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.2f, 1.0f));
        } else if (endRunnable != null) {
            endRunnable.run();
        }
    }

    public void startAbortAnimation() {
        SlideOutCircleView slideOutCircleView = this.mOrbLegacy;
        if (slideOutCircleView != null) {
            slideOutCircleView.startAbortAnimation(this.mHideImmediatelyRunnable, true);
        }
        HwGestureSideLayout hwGestureSideLayout = this.mOrb;
        if (hwGestureSideLayout != null) {
            hwGestureSideLayout.startExitAnimation(this.mHideImmediatelyRunnable, false, false);
            setVisibility(8);
        }
        this.mScrim.setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
    }

    public void performOnAnimationFinished(Runnable runnable) {
        SlideOutCircleView slideOutCircleView = this.mOrbLegacy;
        if (slideOutCircleView != null) {
            slideOutCircleView.performOnAnimationFinished(runnable);
        }
        if (this.mOrb != null && runnable != null) {
            runnable.run();
        }
    }

    public boolean isAnimationRunning() {
        SlideOutCircleView slideOutCircleView = this.mOrbLegacy;
        if (slideOutCircleView != null) {
            return slideOutCircleView.isAnimationRunning(true);
        }
        return false;
    }

    public void setSlideOverThreshold(boolean isOver) {
        SlideOutCircleView slideOutCircleView = this.mOrbLegacy;
        if (slideOutCircleView != null) {
            slideOutCircleView.setDraggedFarEnough(isOver);
        }
    }

    public void setSlideDistance(float distance, float ratio) {
        SlideOutCircleView slideOutCircleView = this.mOrbLegacy;
        if (slideOutCircleView != null) {
            slideOutCircleView.setDragDistance(distance);
        }
        HwGestureSideLayout hwGestureSideLayout = this.mOrb;
        if (hwGestureSideLayout != null) {
            hwGestureSideLayout.setSlidingProcess(ratio);
        }
    }

    public void setSlidingSide(boolean isOnLeft, int appType) {
        SlideOutCircleView slideOutCircleView = this.mOrbLegacy;
        if (slideOutCircleView != null) {
            slideOutCircleView.setSlidingSide(isOnLeft, appType);
        }
        HwGestureSideLayout hwGestureSideLayout = this.mOrb;
        if (hwGestureSideLayout != null) {
            hwGestureSideLayout.setSlidingSide(isOnLeft, appType);
        }
    }

    public boolean isShowing() {
        return getVisibility() == 0 && !this.mIsAnimatingOut;
    }

    public boolean isVisible() {
        return getVisibility() == 0;
    }

    public ImageView getMaybeSwapLogo() {
        SlideOutCircleView slideOutCircleView = this.mOrbLegacy;
        if (slideOutCircleView != null) {
            return slideOutCircleView.getLogo();
        }
        HwGestureSideLayout hwGestureSideLayout = this.mOrb;
        if (hwGestureSideLayout != null) {
            return hwGestureSideLayout.getLogo();
        }
        return null;
    }

    public void setVoiceIcon(boolean isGoogleMode) {
        HwGestureSideLayout hwGestureSideLayout = this.mOrb;
        if (hwGestureSideLayout != null) {
            hwGestureSideLayout.setVoiceIcon(isGoogleMode);
        }
    }
}
