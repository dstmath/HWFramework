package com.android.server.gesture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.huawei.utils.HwPartResourceUtils;

public class SlideOutCircleView extends FrameLayout {
    public static final Interpolator ALPHA_OUT = new PathInterpolator(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.8f, 1.0f);
    private static final int EXIT_FADE_OUT_ANIMATION_TIME = 300;
    private static final int START_DELAY_TIME = 50;
    private final Paint mBackgroundPaint;
    private final int mBaseMargin;
    private float mCircleAnimationEndValue;
    private ValueAnimator mCircleAnimator;
    private int mCircleCenterStartPos;
    private final int mCircleMaxSize;
    private final int mCircleMinSize;
    private final Rect mCircleRect;
    private final int mCircleSideMargin;
    private float mCircleSize;
    private ValueAnimator.AnimatorUpdateListener mCircleUpdateListener;
    private AnimatorListenerAdapter mClearAnimatorListener;
    private final Interpolator mDisappearInterpolator;
    private ValueAnimator mFadeOutAnimator;
    private final Interpolator mFastOutSlowInInterpolator;
    private boolean mIsCircleHidden;
    private boolean mIsClipToOutline;
    private boolean mIsDraggedFarEnough;
    private boolean mIsOffsetAnimatingIn;
    private boolean mIsSlidingOnLeft;
    private ImageView mLeftLogo;
    private final int mMaxElevation;
    private float mOffset;
    private ValueAnimator mOffsetAnimator;
    private ValueAnimator.AnimatorUpdateListener mOffsetUpdateListener;
    private float mOutlineAlpha;
    private ImageView mRightLogo;
    private final int mStaticOffset;
    private final Rect mStaticRect;

    public SlideOutCircleView(Context context) {
        this(context, null);
    }

    public SlideOutCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideOutCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SlideOutCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mBackgroundPaint = new Paint();
        this.mCircleRect = new Rect();
        this.mStaticRect = new Rect();
        this.mIsSlidingOnLeft = true;
        this.mCircleUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.gesture.SlideOutCircleView.AnonymousClass1 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                SlideOutCircleView.this.applyCircleSize(((Float) animation.getAnimatedValue()).floatValue());
                SlideOutCircleView.this.updateElevation();
            }
        };
        this.mClearAnimatorListener = new AnimatorListenerAdapter() {
            /* class com.android.server.gesture.SlideOutCircleView.AnonymousClass2 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                SlideOutCircleView.this.mCircleAnimator = null;
            }
        };
        this.mOffsetUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.gesture.SlideOutCircleView.AnonymousClass3 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                SlideOutCircleView.this.setOffset(((Float) animation.getAnimatedValue()).floatValue());
            }
        };
        setOutlineProvider(new ViewOutlineProvider() {
            /* class com.android.server.gesture.SlideOutCircleView.AnonymousClass4 */

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                if (SlideOutCircleView.this.mCircleSize > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    outline.setOval(SlideOutCircleView.this.mCircleRect);
                } else {
                    outline.setEmpty();
                }
                outline.setAlpha(SlideOutCircleView.this.mOutlineAlpha);
            }
        });
        setWillNotDraw(false);
        this.mCircleSideMargin = context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("slide_out_circle_logo_side_margin"));
        this.mCircleMinSize = context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("slide_out_circle_diameter_min_size"));
        this.mCircleMaxSize = context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("slide_out_circle_diameter_max_size"));
        this.mCircleCenterStartPos = context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("slide_out_circle_center_start_pos"));
        this.mBaseMargin = context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("slide_out_circle_base_margin"));
        this.mStaticOffset = context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("slide_out_circle_travel_distance"));
        this.mMaxElevation = context.getResources().getDimensionPixelSize(34472146);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(getContext(), 17563661);
        this.mDisappearInterpolator = AnimationUtils.loadInterpolator(getContext(), 17563663);
        this.mBackgroundPaint.setAntiAlias(true);
        this.mBackgroundPaint.setColor(getResources().getColor(33882315));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawCircle((float) this.mCircleRect.centerX(), (float) this.mCircleRect.centerY(), this.mCircleSize / 2.0f, this.mBackgroundPaint);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLeftLogo = (ImageView) findViewById(HwPartResourceUtils.getResourceId("left_circle_logo"));
        this.mRightLogo = (ImageView) findViewById(HwPartResourceUtils.getResourceId("right_circle_logo"));
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int l, int t, int r, int b) {
        super.onLayout(isChanged, l, t, r, b);
        ImageView imageView = this.mLeftLogo;
        imageView.layout(0, 0, imageView.getMeasuredWidth(), this.mLeftLogo.getMeasuredHeight());
        ImageView imageView2 = this.mRightLogo;
        imageView2.layout(0, 0, imageView2.getMeasuredWidth(), this.mRightLogo.getMeasuredHeight());
        if (isChanged) {
            updateCircleRect(this.mStaticRect, (float) this.mStaticOffset, true);
        }
    }

    public void setSlidingSide(boolean isOnLeft, int appType) {
        this.mIsSlidingOnLeft = isOnLeft;
        if (this.mIsSlidingOnLeft) {
            this.mLeftLogo.setVisibility(0);
            this.mRightLogo.setVisibility(4);
        } else {
            this.mLeftLogo.setVisibility(4);
            this.mRightLogo.setVisibility(0);
        }
        updateCircleRect(this.mStaticRect, (float) this.mStaticOffset, true);
    }

    public ImageView getLogo() {
        return this.mIsSlidingOnLeft ? this.mLeftLogo : this.mRightLogo;
    }

    public void setCircleSize(float circleSize) {
        setCircleSize(circleSize, false, null, 0, null);
    }

    /* access modifiers changed from: private */
    public static class MyAnimatorListenerAdapter extends AnimatorListenerAdapter {
        Runnable mRunnable;

        MyAnimatorListenerAdapter(Runnable runnable) {
            this.mRunnable = runnable;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            Runnable runnable = this.mRunnable;
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public void setCircleSize(float circleSize, boolean isAnimated, Runnable endRunnable, int startDelay, Interpolator interpolator) {
        boolean isAnimating = this.mCircleAnimator != null;
        boolean isAnimationPending = isAnimating && !this.mCircleAnimator.isRunning();
        boolean isAnimatingOut = isAnimating && this.mCircleAnimationEndValue == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (isAnimated || isAnimationPending || isAnimatingOut) {
            if (isAnimating) {
                this.mCircleAnimator.cancel();
            }
            this.mCircleAnimator = ValueAnimator.ofFloat(this.mCircleSize, circleSize);
            this.mCircleAnimator.addUpdateListener(this.mCircleUpdateListener);
            this.mCircleAnimator.addListener(this.mClearAnimatorListener);
            this.mCircleAnimator.addListener(new MyAnimatorListenerAdapter(endRunnable));
            this.mCircleAnimator.setInterpolator(interpolator != null ? interpolator : this.mDisappearInterpolator);
            this.mCircleAnimator.setDuration(300L);
            this.mCircleAnimator.setStartDelay((long) startDelay);
            this.mCircleAnimator.start();
            this.mCircleAnimationEndValue = circleSize;
        } else if (isAnimating) {
            this.mCircleAnimator.getValues()[0].setFloatValues(circleSize - this.mCircleAnimationEndValue, circleSize);
            ValueAnimator valueAnimator = this.mCircleAnimator;
            valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
            this.mCircleAnimationEndValue = circleSize;
        } else {
            applyCircleSize(circleSize);
            updateElevation();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyCircleSize(float circleSize) {
        this.mCircleSize = circleSize;
        updateLayout();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateElevation() {
        int i = this.mStaticOffset;
        float ratio = (((float) i) - this.mOffset) / ((float) i);
        float ratio2 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (1.0f - ratio > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            ratio2 = ratio;
        }
        setElevation(((float) this.mMaxElevation) * ratio2);
    }

    public void setOffset(float offset) {
        setOffset(offset, false, 0, null, null);
    }

    private void setOffset(float offset, boolean isAnimate, int startDelay, Interpolator interpolator, final Runnable endRunnable) {
        if (!isAnimate) {
            this.mOffset = offset;
            updateLayout();
            if (endRunnable != null) {
                endRunnable.run();
                return;
            }
            return;
        }
        ValueAnimator valueAnimator = this.mOffsetAnimator;
        if (valueAnimator != null) {
            valueAnimator.removeAllListeners();
            this.mOffsetAnimator.cancel();
        }
        boolean z = true;
        this.mOffsetAnimator = ValueAnimator.ofFloat(this.mOffset, offset);
        this.mOffsetAnimator.addUpdateListener(this.mOffsetUpdateListener);
        this.mOffsetAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.server.gesture.SlideOutCircleView.AnonymousClass5 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                SlideOutCircleView.this.mOffsetAnimator = null;
                Runnable runnable = endRunnable;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        this.mOffsetAnimator.setInterpolator(interpolator != null ? interpolator : this.mDisappearInterpolator);
        this.mOffsetAnimator.setStartDelay((long) startDelay);
        this.mOffsetAnimator.setDuration(300L);
        this.mOffsetAnimator.start();
        if (offset == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            z = false;
        }
        this.mIsOffsetAnimatingIn = z;
    }

    private void updateLayout() {
        updateCircleRect();
        updateLogo();
        invalidateOutline();
        invalidate();
        updateClipping();
    }

    private void updateClipping() {
        boolean isClip = this.mCircleSize < ((float) this.mCircleMinSize);
        if (isClip != this.mIsClipToOutline) {
            setClipToOutline(isClip);
            this.mIsClipToOutline = isClip;
        }
    }

    private void updateLogo() {
        float translationY;
        ImageView logo = getLogo();
        boolean isExitAnimationRunning = this.mFadeOutAnimator != null;
        Rect rect = isExitAnimationRunning ? this.mCircleRect : this.mStaticRect;
        float translationX = (((float) (rect.left + rect.right)) / 2.0f) - (((float) logo.getWidth()) / 2.0f);
        float translationY2 = (((float) (rect.top + rect.bottom)) / 2.0f) - (((float) logo.getHeight()) / 2.0f);
        int i = this.mStaticOffset;
        float f = this.mOffset;
        float ratio = (((float) i) - f) / ((float) i);
        if (!isExitAnimationRunning) {
            translationY = translationY2 + (((float) i) * ratio * 0.3f);
            float alphaTmp = ((1.0f - ratio) - 0.5f) * 2.0f;
            float alpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            if (alphaTmp > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                alpha = alphaTmp;
            }
            logo.setAlpha(alpha);
        } else {
            translationY = translationY2 + ((f - ((float) i)) / 2.0f);
        }
        logo.setTranslationX(translationX);
        logo.setTranslationY(translationY);
    }

    private void updateCircleRect() {
        updateCircleRect(this.mCircleRect, this.mOffset, false);
    }

    private void updateCircleRect(Rect rect, float offset, boolean isUseStaticSize) {
        int top;
        int left;
        float circleSize = isUseStaticSize ? (float) this.mCircleMinSize : this.mCircleSize;
        if (this.mIsSlidingOnLeft) {
            left = (int) (((float) (this.mCircleSideMargin + this.mCircleCenterStartPos)) - (circleSize / 2.0f));
            top = (int) (((((float) getHeight()) - (circleSize / 2.0f)) - ((float) this.mBaseMargin)) - offset);
        } else {
            left = (int) (((float) ((getWidth() - this.mCircleSideMargin) - this.mCircleCenterStartPos)) - (circleSize / 2.0f));
            top = (int) (((((float) getHeight()) - (circleSize / 2.0f)) - ((float) this.mBaseMargin)) - offset);
        }
        rect.set(left, top, (int) (((float) left) + circleSize), (int) (((float) top) + circleSize));
    }

    public void setDragDistance(float distance) {
        if (!this.mIsCircleHidden || this.mIsDraggedFarEnough) {
            float circleSize = ((float) this.mCircleMinSize) + rubberband(distance);
            int i = this.mCircleMaxSize;
            if (circleSize > ((float) i)) {
                circleSize = (float) i;
            }
            setCircleSize(circleSize);
        }
    }

    private float rubberband(float diff) {
        return (float) Math.pow((double) Math.abs(diff), 0.6000000238418579d);
    }

    public void startAbortAnimation(Runnable endRunnable, boolean isAnimAlpha) {
        setCircleSize(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, true, null, 0, null);
        setOffset(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, true, 0, null, endRunnable);
        if (isAnimAlpha) {
            animate().alpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO).setDuration(300).setStartDelay(0).setInterpolator(ALPHA_OUT).start();
        } else {
            setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        }
        this.mIsCircleHidden = true;
    }

    public void startEnterAnimation() {
        setAlpha(1.0f);
        applyCircleSize(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        setOffset(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        setCircleSize((float) this.mCircleMinSize, true, null, START_DELAY_TIME, null);
        setOffset((float) this.mStaticOffset, true, START_DELAY_TIME, null, null);
        this.mIsCircleHidden = false;
    }

    public void startExitAnimation(Runnable endRunnable, boolean isFastSlide) {
        setOffset((((float) getHeight()) / 2.0f) - ((float) this.mBaseMargin), true, START_DELAY_TIME, this.mFastOutSlowInInterpolator, null);
        setCircleSize((float) Math.ceil(Math.hypot((double) (((float) getWidth()) / 2.0f), (double) (((float) getHeight()) / 2.0f)) * 2.0d), true, null, START_DELAY_TIME, this.mFastOutSlowInInterpolator);
        performExitFadeOutAnimation(START_DELAY_TIME, EXIT_FADE_OUT_ANIMATION_TIME, endRunnable);
    }

    private void performExitFadeOutAnimation(int startDelay, int duration, final Runnable endRunnable) {
        this.mFadeOutAnimator = ValueAnimator.ofFloat(((float) this.mBackgroundPaint.getAlpha()) / 255.0f, 0.0f);
        this.mFadeOutAnimator.setInterpolator(new LinearInterpolator());
        this.mFadeOutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.gesture.SlideOutCircleView.AnonymousClass6 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                float backgroundValue;
                float animatedFraction = animation.getAnimatedFraction();
                float logoValue = SlideOutCircleView.ALPHA_OUT.getInterpolation(1.0f - (animatedFraction > 0.5f ? 1.0f : animatedFraction / 0.5f));
                if (animatedFraction < 0.2f) {
                    backgroundValue = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                } else {
                    backgroundValue = SlideOutCircleView.ALPHA_OUT.getInterpolation((animatedFraction - 0.2f) / 0.8f);
                }
                float backgroundValue2 = 1.0f - backgroundValue;
                SlideOutCircleView.this.mBackgroundPaint.setAlpha((int) (255.0f * backgroundValue2));
                SlideOutCircleView.this.mOutlineAlpha = backgroundValue2;
                SlideOutCircleView.this.getLogo().setAlpha(logoValue);
                SlideOutCircleView.this.invalidateOutline();
                SlideOutCircleView.this.invalidate();
            }
        });
        this.mFadeOutAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.server.gesture.SlideOutCircleView.AnonymousClass7 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                Runnable runnable = endRunnable;
                if (runnable != null) {
                    runnable.run();
                }
                SlideOutCircleView.this.getLogo().setAlpha(1.0f);
                SlideOutCircleView.this.mBackgroundPaint.setAlpha(GestureNavConst.VIBRATION_AMPLITUDE);
                SlideOutCircleView.this.mOutlineAlpha = 1.0f;
                SlideOutCircleView.this.mFadeOutAnimator = null;
            }
        });
        this.mFadeOutAnimator.setStartDelay((long) startDelay);
        this.mFadeOutAnimator.setDuration((long) duration);
        this.mFadeOutAnimator.start();
    }

    public void setDraggedFarEnough(boolean isFarEnough) {
        if (isFarEnough != this.mIsDraggedFarEnough) {
            if (!isFarEnough) {
                startAbortAnimation(null, false);
            } else if (this.mIsCircleHidden) {
                startEnterAnimation();
            }
            this.mIsDraggedFarEnough = isFarEnough;
        }
    }

    public void reset() {
        this.mIsDraggedFarEnough = false;
        this.mIsCircleHidden = true;
        this.mIsClipToOutline = false;
        ValueAnimator valueAnimator = this.mFadeOutAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mBackgroundPaint.setAlpha(GestureNavConst.VIBRATION_AMPLITUDE);
        this.mOutlineAlpha = 1.0f;
    }

    public boolean isAnimationRunning(boolean isEnterAnimation) {
        return this.mOffsetAnimator != null && isEnterAnimation == this.mIsOffsetAnimatingIn;
    }

    public void performOnAnimationFinished(Runnable runnable) {
        ValueAnimator valueAnimator = this.mOffsetAnimator;
        if (valueAnimator != null) {
            valueAnimator.addListener(new MyAnimatorListenerAdapter(runnable));
        } else if (runnable != null) {
            runnable.run();
        }
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }
}
