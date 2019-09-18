package com.android.server.gesture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.server.gesture.anim.HwGestureSideLayout;

public class SlideOutContainer extends FrameLayout {
    private static final long EXIT_START_DELAY = 150;
    /* access modifiers changed from: private */
    public boolean mAnimatingOut;
    private final Runnable mHideImmediatelyRunnable;
    private final Runnable mHideRunnable;
    private HwGestureSideLayout mOrb;
    private SlideOutCircleView mOrbLegacy;
    /* access modifiers changed from: private */
    public View mScrim;

    public SlideOutContainer(Context context) {
        this(context, null);
    }

    public SlideOutContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideOutContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mHideImmediatelyRunnable = new Runnable() {
            public void run() {
                SlideOutContainer.this.show(false, false);
            }
        };
        this.mHideRunnable = new Runnable() {
            public void run() {
                SlideOutContainer.this.show(false, true);
            }
        };
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mScrim = findViewById(34603407);
        this.mOrb = (HwGestureSideLayout) findViewById(34603404);
        this.mOrbLegacy = (SlideOutCircleView) findViewById(34603405);
        if (GestureNavConst.USE_ANIM_LEGACY && this.mOrbLegacy != null && this.mOrb != null) {
            this.mOrbLegacy.setVisibility(0);
            this.mOrb.setVisibility(8);
            this.mOrb = null;
        } else if (this.mOrbLegacy != null && this.mOrb != null) {
            this.mOrb.setVisibility(0);
            this.mOrbLegacy.setVisibility(8);
            this.mOrbLegacy = null;
        }
    }

    public void show(boolean show) {
        setVisibility(show ? 0 : 4);
    }

    public void show(boolean show, boolean animate) {
        if (show) {
            if (getVisibility() != 0) {
                setVisibility(0);
                if (animate) {
                    startEnterAnimation();
                } else {
                    reset();
                }
            }
        } else if (animate) {
            startExitAnimation(new Runnable() {
                public void run() {
                    boolean unused = SlideOutContainer.this.mAnimatingOut = false;
                    SlideOutContainer.this.setVisibility(8);
                }
            }, false, false);
        } else {
            setVisibility(8);
        }
    }

    public void hide(boolean animate) {
        if (animate) {
            startAbortAnimation();
        } else {
            setVisibility(4);
        }
    }

    public void reset() {
        this.mAnimatingOut = false;
        if (this.mOrbLegacy != null) {
            this.mOrbLegacy.reset();
        }
        this.mScrim.setAlpha(1.0f);
    }

    public void startEnterAnimation() {
        if (!this.mAnimatingOut) {
            if (this.mOrb != null) {
                this.mOrb.startEnterAnimation();
            }
            if (this.mOrbLegacy != null) {
                this.mOrbLegacy.startEnterAnimation();
            }
            this.mScrim.setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
            post(new Runnable() {
                public void run() {
                    SlideOutContainer.this.mScrim.animate().alpha(1.0f).setDuration(300).setStartDelay(0).setInterpolator(new PathInterpolator(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.2f, 1.0f));
                }
            });
        }
    }

    public void startExitAnimation(boolean isFastSlide, boolean startMaskAnimation) {
        startExitAnimation(this.mHideRunnable, isFastSlide, startMaskAnimation);
    }

    public void startExitAnimation(Runnable endRunnable, boolean isFastSlide, boolean startMaskAnimation) {
        if (this.mAnimatingOut) {
            if (endRunnable != null) {
                endRunnable.run();
            }
            return;
        }
        this.mAnimatingOut = true;
        if (this.mOrbLegacy != null) {
            this.mOrbLegacy.startExitAnimation(this.mHideImmediatelyRunnable, isFastSlide);
        }
        if (this.mOrb != null) {
            this.mOrb.startExitAnimation(this.mHideImmediatelyRunnable, isFastSlide, startMaskAnimation);
        }
        this.mScrim.animate().alpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO).setDuration(250).setStartDelay(EXIT_START_DELAY).setInterpolator(new PathInterpolator(0.4f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.2f, 1.0f));
    }

    public void startAbortAnimation() {
        if (this.mOrbLegacy != null) {
            this.mOrbLegacy.startAbortAnimation(this.mHideImmediatelyRunnable, true);
        }
        if (this.mOrb != null) {
            this.mOrb.startExitAnimation(this.mHideImmediatelyRunnable, false, false);
            setVisibility(8);
        }
        this.mScrim.setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
    }

    public void performOnAnimationFinished(Runnable runnable) {
        if (this.mOrbLegacy != null) {
            this.mOrbLegacy.performOnAnimationFinished(runnable);
        }
        if (this.mOrb != null && runnable != null) {
            runnable.run();
        }
    }

    public boolean isAnimationRunning() {
        if (this.mOrbLegacy != null) {
            return this.mOrbLegacy.isAnimationRunning(true);
        }
        return false;
    }

    public void setSlideOverThreshold(boolean over) {
        if (this.mOrbLegacy != null) {
            this.mOrbLegacy.setDraggedFarEnough(over);
        }
    }

    public void setSlideDistance(float distance, float ratio) {
        if (this.mOrbLegacy != null) {
            this.mOrbLegacy.setDragDistance(distance);
        }
        if (this.mOrb != null) {
            this.mOrb.setSlidingProcess(ratio);
        }
    }

    public void setSlidingSide(boolean onLeft, int appType) {
        if (this.mOrbLegacy != null) {
            this.mOrbLegacy.setSlidingSide(onLeft, appType);
        }
        if (this.mOrb != null) {
            this.mOrb.setSlidingSide(onLeft, appType);
        }
    }

    public boolean isShowing() {
        return getVisibility() == 0 && !this.mAnimatingOut;
    }

    public boolean isVisible() {
        return getVisibility() == 0;
    }

    public ImageView getMaybeSwapLogo() {
        if (this.mOrbLegacy != null) {
            return this.mOrbLegacy.getLogo();
        }
        if (this.mOrb != null) {
            return this.mOrb.getLogo();
        }
        return null;
    }
}
