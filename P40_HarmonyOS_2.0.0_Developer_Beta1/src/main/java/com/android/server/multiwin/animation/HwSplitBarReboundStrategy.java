package com.android.server.multiwin.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Slog;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.RelativeLayout;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.HwMultiWindowSplitUI;
import com.android.server.wm.HwSplitBarConstants;
import com.huawei.anim.dynamicanimation.interpolator.SpringInterpolator;

public abstract class HwSplitBarReboundStrategy {
    static final int CONST_NUM_FOUR = 4;
    static final long COVER_ANIM_DELAY = 300;
    private static final int FLAG_END_OF_ALL_ANIMATIOR = 0;
    private static final int FLAG_END_OF_REBOUND_ANIMATOR = 1;
    static final float FLOAT_NUM_TWO = 2.0f;
    static final float PARAMS_DAMPING = ((float) Math.sqrt(3200.0d));
    static final float PARAMS_STIFFNESS = 800.0f;
    static final long REBACK_ANIM_DURATION = 200;
    static final String TAG = "HwSplitBarReboundStrategy";
    ActivityTaskManagerService mAtms;
    float mCurPosition;
    int mDisplayId;
    View mDragBar;
    float mEndPosition;
    int mHeight;
    boolean mIsCoverAnimDelay;
    View mLeft;
    View mRight;
    int mSplitRatio;
    int mWidth;

    /* access modifiers changed from: package-private */
    public abstract ValueAnimator getCoverScaleAnim();

    /* access modifiers changed from: package-private */
    public abstract ObjectAnimator getDragLineTransAnim(Interpolator interpolator);

    public HwSplitBarReboundStrategy(ActivityTaskManagerService service, View left, View dragBar, View right, Bundle bundle) {
        this.mLeft = left;
        this.mDragBar = dragBar;
        this.mRight = right;
        this.mAtms = service;
        if (bundle != null) {
            this.mCurPosition = bundle.getFloat(HwSplitBarConstants.CURRENT_POSITION);
            this.mEndPosition = bundle.getFloat(HwSplitBarConstants.END_POSITION);
            this.mWidth = bundle.getInt(HwSplitBarConstants.DISPLAY_WIDTH);
            this.mHeight = bundle.getInt(HwSplitBarConstants.DISPLAY_HEIGHT);
            this.mSplitRatio = bundle.getInt(HwSplitBarConstants.SPLIT_RATIO);
            this.mDisplayId = bundle.getInt(HwSplitBarConstants.SPLIT_BAR_DISPLAY_ID);
            this.mIsCoverAnimDelay = bundle.getBoolean(HwSplitBarConstants.IS_COVER_ANIM_DELAY, false);
        }
    }

    public static HwSplitBarReboundStrategy getStrategy(ActivityTaskManagerService service, View left, View dragBar, View right, Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        if (bundle.getBoolean(HwSplitBarConstants.SPLIT_ORIENTATION)) {
            return new HwSplitBarHorizontalRebound(service, left, dragBar, right, bundle);
        }
        return new HwSplitBarVerticalRebound(service, left, dragBar, right, bundle);
    }

    public void startReboundAnim() {
        Slog.i(TAG, " startReboundAnim endPosition = " + this.mEndPosition + " curPos = " + this.mCurPosition + " mLeft.getWidth() = " + this.mLeft.getWidth());
        float endPos = this.mEndPosition - this.mCurPosition;
        ObjectAnimator reboundAnim = ObjectAnimator.ofFloat(this.mDragBar, getTranslation(), getDragBarTranslate(), endPos);
        SpringInterpolator interpolator = new SpringInterpolator(800.0f, PARAMS_DAMPING, endPos);
        interpolator.setValueThreshold(2.0f);
        long duration = (long) interpolator.getDuration();
        Slog.i(TAG, " duration = " + duration + " endPos = " + endPos);
        ValueAnimator scaleAnim = getCoverScaleAnim();
        AnimatorSet sets = new AnimatorSet();
        sets.playTogether(reboundAnim, scaleAnim);
        sets.setDuration(duration);
        sets.setInterpolator(interpolator);
        sets.addListener(new AnimListener(1));
        sets.start();
    }

    public void afterRebound() {
        Interpolator standardCurve = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
        Interpolator sharpCurve = new PathInterpolator(0.33f, 0.0f, 0.36f, 1.0f);
        ObjectAnimator leftAlphaAnim = ObjectAnimator.ofFloat(this.mLeft, View.ALPHA, this.mLeft.getAlpha(), 0.0f);
        ObjectAnimator rightAlphaAnim = ObjectAnimator.ofFloat(this.mRight, View.ALPHA, this.mRight.getAlpha(), 0.0f);
        leftAlphaAnim.setInterpolator(sharpCurve);
        rightAlphaAnim.setInterpolator(sharpCurve);
        View leftIcon = getIcon(this.mLeft);
        View rightIcon = getIcon(this.mRight);
        AnimatorSet leftScaleAnims = getIconScaleAnim(leftIcon, standardCurve);
        AnimatorSet rightScaleAnims = getIconScaleAnim(rightIcon, standardCurve);
        AnimatorSet afterReboundAnimSets = new AnimatorSet();
        afterReboundAnimSets.playTogether(leftAlphaAnim, rightAlphaAnim, getDragLineTransAnim(sharpCurve), leftScaleAnims, rightScaleAnims);
        afterReboundAnimSets.setDuration(200L);
        afterReboundAnimSets.addListener(new AnimListener(0));
        if (this.mIsCoverAnimDelay) {
            afterReboundAnimSets.setStartDelay(COVER_ANIM_DELAY);
        }
        afterReboundAnimSets.start();
    }

    /* access modifiers changed from: package-private */
    public class AnimListener implements Animator.AnimatorListener {
        int mFlag;

        AnimListener(int flag) {
            this.mFlag = flag;
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            int i = this.mFlag;
            if (i == 0) {
                HwSplitBarReboundStrategy hwSplitBarReboundStrategy = HwSplitBarReboundStrategy.this;
                hwSplitBarReboundStrategy.onAnimEnd(hwSplitBarReboundStrategy.mLeft);
                HwSplitBarReboundStrategy hwSplitBarReboundStrategy2 = HwSplitBarReboundStrategy.this;
                hwSplitBarReboundStrategy2.onAnimEnd(hwSplitBarReboundStrategy2.mRight);
                HwSplitBarReboundStrategy.this.mDragBar.setTranslationY(0.0f);
                HwSplitBarReboundStrategy.this.mDragBar.setTranslationX(0.0f);
                HwMultiWindowSplitUI.getInstance(HwSplitBarReboundStrategy.this.mAtms.getUiContext(), HwSplitBarReboundStrategy.this.mAtms, HwSplitBarReboundStrategy.this.mDisplayId).updateSplitBarPosition(HwSplitBarReboundStrategy.this.mSplitRatio, HwSplitBarReboundStrategy.this.mCurPosition);
                Slog.i(HwSplitBarReboundStrategy.TAG, " end of all rebound animator ");
            } else if (i == 1) {
                if (HwSplitBarReboundStrategy.this.mEndPosition == 0.0f) {
                    HwSplitBarReboundStrategy.this.mDragBar.setVisibility(8);
                }
                HwMultiWindowSplitUI.getInstance(HwSplitBarReboundStrategy.this.mAtms.getUiContext(), HwSplitBarReboundStrategy.this.mAtms, HwSplitBarReboundStrategy.this.mDisplayId).setLayoutBackground();
                HwSplitBarReboundStrategy.this.afterRebound();
            }
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }
    }

    private View getIcon(View view) {
        if (view == null || !(view instanceof RelativeLayout)) {
            return null;
        }
        return ((RelativeLayout) view).getChildAt(0);
    }

    private AnimatorSet getIconScaleAnim(View view, Interpolator standardCurve) {
        AnimatorSet animatorSets = new AnimatorSet();
        if (view == null) {
            return animatorSets;
        }
        animatorSets.playTogether(ObjectAnimator.ofFloat(view, View.SCALE_X, view.getScaleX(), view.getScaleX() * 0.85f), ObjectAnimator.ofFloat(view, View.SCALE_Y, view.getScaleY(), view.getScaleY() * 0.85f));
        animatorSets.setDuration(200L);
        animatorSets.setInterpolator(standardCurve);
        return animatorSets;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAnimEnd(View view) {
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
        view.setTranslationX(0.0f);
        view.setTranslationY(0.0f);
        view.setAlpha(1.0f);
        View icon = getIcon(view);
        if (icon != null) {
            icon.setScaleX(1.0f);
            icon.setScaleY(1.0f);
            icon.setTranslationY(0.0f);
            icon.setTranslationX(0.0f);
        }
    }

    /* access modifiers changed from: package-private */
    public String getTranslation() {
        return "translationX";
    }

    /* access modifiers changed from: package-private */
    public float getDragBarTranslate() {
        return this.mDragBar.getTranslationX();
    }
}
