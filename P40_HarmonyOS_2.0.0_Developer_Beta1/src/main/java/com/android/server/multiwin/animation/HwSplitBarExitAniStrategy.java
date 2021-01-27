package com.android.server.multiwin.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Slog;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.RelativeLayout;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.HwMultiWindowManager;
import com.android.server.wm.HwMultiWindowSplitUI;
import com.android.server.wm.HwSplitBarConstants;

public abstract class HwSplitBarExitAniStrategy {
    private static final long ALPHA_DURATION = 100;
    static final long COVER_ANIM_DELAY = 300;
    static final long DEFAULT_ANIMATION_TIME = 300;
    static final float FLOAT_NUM_TWO = 2.0f;
    private static final int RULE_FALSE = 0;
    private static final String TAG = "HwSplitBarExitAniStrategy";
    static final String TRANSLATION_X = "translationX";
    static final String TRANSLATION_Y = "translationY";
    ActivityTaskManagerService mAtms;
    float mCurPos;
    int mDisplayId;
    View mDragBar;
    int mDragFullLineColor;
    View mDragFullZone;
    int mDragLineColor;
    float mEndLinePos;
    int mFarLineMargin;
    int mFarLineTrans;
    int mHeight;
    int mHeightColumns;
    boolean mIsCoverAnimDelay;
    View mLeft;
    int mNearLineMargin;
    View mRight;
    int mType;
    int mWidth;
    int mWidthColumns;

    /* access modifiers changed from: package-private */
    public abstract long getAniDuration();

    /* access modifiers changed from: package-private */
    public abstract float getDragBarTransDis();

    /* access modifiers changed from: package-private */
    public abstract void getScaleAnim(float f);

    /* access modifiers changed from: package-private */
    public abstract int getScaleUpStartLen();

    /* access modifiers changed from: package-private */
    public abstract float getTranslateDistance();

    public HwSplitBarExitAniStrategy(ActivityTaskManagerService service, View left, View dragBar, View right, View dragFullZone, Bundle bundle) {
        this.mLeft = left;
        this.mDragBar = dragBar;
        this.mRight = right;
        this.mAtms = service;
        this.mDragFullZone = dragFullZone;
        if (bundle != null) {
            this.mCurPos = bundle.getFloat(HwSplitBarConstants.CURRENT_POSITION);
            this.mWidth = bundle.getInt(HwSplitBarConstants.DISPLAY_WIDTH);
            this.mHeight = bundle.getInt(HwSplitBarConstants.DISPLAY_HEIGHT);
            this.mWidthColumns = bundle.getInt(HwMultiWindowManager.WIDTH_COLUMNS);
            this.mHeightColumns = bundle.getInt(HwMultiWindowManager.HEIGHT_COLUMNS);
            this.mDisplayId = bundle.getInt(HwSplitBarConstants.SPLIT_BAR_DISPLAY_ID);
            this.mType = bundle.getInt(HwSplitBarConstants.SPLIT_RATIO);
            this.mFarLineMargin = bundle.getInt(HwSplitBarConstants.FAR_LINE_MARGIN);
            this.mNearLineMargin = bundle.getInt(HwSplitBarConstants.NEAR_LINE_MARGIN);
            this.mFarLineTrans = bundle.getInt(HwSplitBarConstants.FAR_LINE_TRANS);
            this.mDragLineColor = bundle.getInt(HwSplitBarConstants.DRAG_LINE_COLOR);
            this.mDragFullLineColor = bundle.getInt(HwSplitBarConstants.DRAG_FULL_MODE_LINE_COLOR);
            this.mIsCoverAnimDelay = bundle.getBoolean(HwSplitBarConstants.IS_COVER_ANIM_DELAY, false);
        }
    }

    public static HwSplitBarExitAniStrategy getStrategy(ActivityTaskManagerService service, View left, View dragBar, View right, View dragFullZone, Bundle bundle) {
        if (bundle == null) {
            Slog.i(TAG, " bundle is null, return null of strategy");
            return null;
        }
        int exitRegion = bundle.getInt(HwSplitBarConstants.EXIT_REGION);
        if (exitRegion == 1) {
            return new HwLeftStackExitAnim(service, left, dragBar, right, dragFullZone, bundle);
        }
        if (exitRegion == 2) {
            return new HwRightStackExitAnim(service, left, dragBar, right, dragFullZone, bundle);
        }
        if (exitRegion == 3) {
            return new HwTopStackExitAnim(service, left, dragBar, right, dragFullZone, bundle);
        }
        if (exitRegion != 4) {
            return null;
        }
        return new HwBottomStackExitAnim(service, left, dragBar, right, dragFullZone, bundle);
    }

    /* access modifiers changed from: package-private */
    public View getScaleDownView() {
        return this.mLeft;
    }

    /* access modifiers changed from: package-private */
    public View getScaleUpView() {
        return this.mRight;
    }

    /* access modifiers changed from: package-private */
    public int getScaleUpEndLen() {
        return this.mWidth;
    }

    /* access modifiers changed from: package-private */
    public String getTranslateDirect() {
        return TRANSLATION_X;
    }

    public void split2FullAnimation() {
        Interpolator standardCurve = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
        ObjectAnimator scaleDownAni = ObjectAnimator.ofFloat(getScaleDownView(), getTranslateDirect(), getTranslateDistance());
        scaleDownAni.setInterpolator(standardCurve);
        ObjectAnimator dragBarAni = ObjectAnimator.ofFloat(this.mDragBar, getTranslateDirect(), getDragBarTransDis());
        dragBarAni.setInterpolator(standardCurve);
        ValueAnimator scaleUpAni = ValueAnimator.ofInt(getScaleUpStartLen(), getScaleUpEndLen()).setDuration(300L);
        scaleUpAni.setInterpolator(standardCurve);
        View view = this.mLeft;
        view.setPivotX((float) (view.getWidth() / 2));
        View view2 = this.mLeft;
        view2.setPivotY((float) (view2.getHeight() / 2));
        ScaleAnimListener listener = new ScaleAnimListener();
        scaleUpAni.addUpdateListener(listener);
        scaleUpAni.addListener(listener);
        AnimatorSet animatorSets = new AnimatorSet();
        animatorSets.setDuration(300L);
        Interpolator sharpCurve = new PathInterpolator(0.33f, 0.0f, 0.36f, 1.0f);
        ObjectAnimator dragLineTransAnim = getLineTransAnim(standardCurve);
        ObjectAnimator dragLineColorAnim = getLineColorAnim(sharpCurve);
        if (dragLineTransAnim == null || dragLineColorAnim == null) {
            animatorSets.playTogether(scaleDownAni, dragBarAni, scaleUpAni);
        } else {
            animatorSets.playTogether(scaleDownAni, dragBarAni, scaleUpAni, dragLineTransAnim, dragLineColorAnim);
        }
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(getScaleUpView(), View.ALPHA, getScaleUpView().getAlpha(), 0.0f);
        alphaAnim.setDuration(ALPHA_DURATION);
        if (this.mIsCoverAnimDelay) {
            alphaAnim.setStartDelay(300);
        }
        AnimatorSet allSets = new AnimatorSet();
        allSets.playSequentially(animatorSets, alphaAnim);
        allSets.addListener(new AnimListener());
        allSets.start();
    }

    private ObjectAnimator getLineTransAnim(Interpolator interpolator) {
        ObjectAnimator anim;
        View line = ((RelativeLayout) this.mDragFullZone).getChildAt(0);
        int i = this.mType;
        if (i == 6) {
            setZoneViewsParams(true);
            this.mEndLinePos = (-(this.mCurPos - ((float) this.mFarLineMargin))) - ((float) ((this.mDragBar.getWidth() - line.getWidth()) / 2));
            View view = this.mDragFullZone;
            float translationX = view.getTranslationX();
            int i2 = this.mFarLineTrans;
            anim = ObjectAnimator.ofFloat(view, TRANSLATION_X, translationX - ((float) i2), this.mEndLinePos - ((float) i2));
        } else if (i != 5) {
            return null;
        } else {
            setZoneViewsParams(false);
            this.mEndLinePos = (((float) this.mWidth) - (this.mCurPos + ((float) this.mFarLineMargin))) - ((float) ((this.mDragBar.getWidth() + line.getWidth()) / 2));
            View view2 = this.mDragFullZone;
            float translationX2 = view2.getTranslationX();
            int i3 = this.mFarLineTrans;
            anim = ObjectAnimator.ofFloat(view2, TRANSLATION_X, translationX2 + ((float) i3), this.mEndLinePos + ((float) i3));
        }
        ((RelativeLayout) this.mDragBar).getChildAt(0).setVisibility(8);
        if (this.mDragFullZone.getBackground() != null) {
            this.mDragFullZone.getBackground().setAlpha(0);
        }
        this.mDragFullZone.setVisibility(0);
        anim.setInterpolator(interpolator);
        return anim;
    }

    private ObjectAnimator getLineColorAnim(Interpolator sharpCurve) {
        if (!HwMultiWindowManager.getInstance(this.mAtms).isDragFullModeByType(this.mType)) {
            return null;
        }
        View mFarLine = ((RelativeLayout) this.mDragFullZone).getChildAt(0);
        View mNearLine = ((RelativeLayout) this.mDragFullZone).getChildAt(1);
        mFarLine.setVisibility(0);
        mNearLine.setVisibility(4);
        mFarLine.setBackgroundColor(this.mDragLineColor);
        ObjectAnimator anim = ObjectAnimator.ofObject(mFarLine, "backgroundColor", new ArgbEvaluator(), Integer.valueOf(this.mDragLineColor), Integer.valueOf(this.mDragFullLineColor));
        anim.setInterpolator(sharpCurve);
        return anim;
    }

    private void setZoneViewsParams(boolean isLeft) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) this.mDragFullZone.getLayoutParams();
        lp.addRule(1, isLeft ? this.mDragBar.getId() : 0);
        lp.addRule(0, isLeft ? 0 : this.mDragBar.getId());
        this.mDragFullZone.setLayoutParams(lp);
        HwMultiWindowSplitUI.getInstance(this.mAtms.getUiContext(), this.mAtms, this.mDisplayId).setLinesParamsInDragFull(isLeft);
    }

    private class ScaleAnimListener implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private ScaleAnimListener() {
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            if (animation.getAnimatedValue() != null) {
                float value = 1.0f;
                if (animation.getAnimatedValue() instanceof Integer) {
                    value = (float) ((Integer) animation.getAnimatedValue()).intValue();
                }
                HwSplitBarExitAniStrategy.this.getScaleAnim(value);
            }
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            Slog.i(HwSplitBarExitAniStrategy.TAG, " set layout background color");
            HwMultiWindowSplitUI.getInstance(HwSplitBarExitAniStrategy.this.mAtms.getUiContext(), HwSplitBarExitAniStrategy.this.mAtms, HwSplitBarExitAniStrategy.this.mDisplayId).setLayoutBackground();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
            HwMultiWindowSplitUI.getInstance(HwSplitBarExitAniStrategy.this.mAtms.getUiContext(), HwSplitBarExitAniStrategy.this.mAtms, HwSplitBarExitAniStrategy.this.mDisplayId).setLayoutBackground();
        }
    }

    private class AnimListener implements Animator.AnimatorListener {
        private AnimListener() {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            HwMultiWindowManager multiWindowManager = HwMultiWindowManager.getInstance(HwSplitBarExitAniStrategy.this.mAtms);
            if (multiWindowManager.isDragFullModeByType(HwSplitBarExitAniStrategy.this.mType)) {
                HwSplitBarExitAniStrategy hwSplitBarExitAniStrategy = HwSplitBarExitAniStrategy.this;
                hwSplitBarExitAniStrategy.onAnimEnd(hwSplitBarExitAniStrategy.mLeft);
                HwSplitBarExitAniStrategy hwSplitBarExitAniStrategy2 = HwSplitBarExitAniStrategy.this;
                hwSplitBarExitAniStrategy2.onAnimEnd(hwSplitBarExitAniStrategy2.mRight);
                HwSplitBarExitAniStrategy.this.mDragBar.setTranslationY(0.0f);
                HwSplitBarExitAniStrategy.this.mDragBar.setTranslationX(0.0f);
                HwSplitBarExitAniStrategy.this.mDragFullZone.setTranslationX(0.0f);
                HwSplitBarExitAniStrategy.this.mDragFullZone.setTranslationY(0.0f);
                ((RelativeLayout) HwSplitBarExitAniStrategy.this.mDragBar).getChildAt(0).setVisibility(0);
                HwMultiWindowSplitUI.getInstance(HwSplitBarExitAniStrategy.this.mAtms.getUiContext(), HwSplitBarExitAniStrategy.this.mAtms, HwSplitBarExitAniStrategy.this.mDisplayId).setSplitBarDragFullMode(HwSplitBarExitAniStrategy.this.mType);
            } else {
                HwSplitBarExitAniStrategy.this.mDragBar.setVisibility(8);
                Slog.i(HwSplitBarExitAniStrategy.TAG, " multiWindowManager.removeSplitScreenDividerBar");
                multiWindowManager.removeSplitScreenDividerBar(100, true, HwSplitBarExitAniStrategy.this.mDisplayId);
            }
            Slog.i(HwSplitBarExitAniStrategy.TAG, "on animation end");
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
            HwMultiWindowManager.getInstance(HwSplitBarExitAniStrategy.this.mAtms).removeSplitScreenDividerBar(100, true, HwSplitBarExitAniStrategy.this.mDisplayId);
            Slog.i(HwSplitBarExitAniStrategy.TAG, "on animation cancel");
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }
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

    private View getIcon(View view) {
        if (view instanceof RelativeLayout) {
            return ((RelativeLayout) view).getChildAt(0);
        }
        return null;
    }
}
