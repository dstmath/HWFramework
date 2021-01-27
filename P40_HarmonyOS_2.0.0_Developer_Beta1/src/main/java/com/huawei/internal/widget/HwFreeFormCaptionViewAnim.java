package com.huawei.internal.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.provider.Settings;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.anim.dynamicanimation.interpolator.SpringInterpolator;

public class HwFreeFormCaptionViewAnim {
    private static final String ALPHA = "alpha";
    private static final float BAR_SCALE_BIG = 1.75f;
    private static final float BAR_SCALE_SMALL = 0.85f;
    private static final int CONSTANT_NUM_TWO = 2;
    private static final int CONST_NUM_FOUR = 4;
    private static final long DURATION_ALPHA_SCALE = 150;
    private static final long DURATION_TRANSLATE = 300;
    private static final float FRICTION_CX1 = 0.2f;
    private static final float FRICTION_CX2 = 0.2f;
    private static final float FRICTION_CY1 = 0.0f;
    private static final float FRICTION_CY2 = 1.0f;
    private static final float HALF_SCALE = 0.5f;
    private static final float PARAMS_DAMPING = ((float) Math.sqrt(3200.0d));
    private static final float PARAMS_STIFFNESS = 800.0f;
    private static final String SCALE_X = "scaleX";
    private static final String SCALE_Y = "scaleY";
    private static final float SHARP_CX1 = 0.33f;
    private static final float SHARP_CX2 = 0.67f;
    private static final float SHARP_CY1 = 0.0f;
    private static final float SHARP_CY2 = 1.0f;
    private static final String TAG = "HwFreeFormCaptionViewAnim";
    private static final String TRANSLATE_X = "translationX";
    private Animator executingAnim;
    boolean inAnimating;
    boolean inFade;
    boolean inShow;
    private HwFreeFormCaptionView mCaptionView;

    public HwFreeFormCaptionViewAnim(HwFreeFormCaptionView captionView) {
        this.mCaptionView = captionView;
    }

    /* access modifiers changed from: package-private */
    public void startFadingAnim() {
        if (this.mCaptionView.elementViewsDisable()) {
            Log.i(TAG, "startFadingAnim return.");
            return;
        }
        this.inFade = true;
        AnimatorSet translateScale = getTranslateAndScaleAnim(false);
        AnimatorSet alpha = getAlphaAnim(false);
        ObjectAnimator barScaleFirst = getBarScaleFirst(false);
        AnimatorSet alphaAndScale = new AnimatorSet();
        alphaAndScale.playTogether(alpha, barScaleFirst);
        alphaAndScale.setStartDelay(DURATION_ALPHA_SCALE);
        translateScale.start();
        alphaAndScale.start();
    }

    public void startShowingAnim() {
        if (this.mCaptionView.elementViewsDisable()) {
            Log.i(TAG, "startShowingAnim return.");
            return;
        }
        this.inShow = true;
        ObjectAnimator barScaleFirst = getBarScaleFirst(true);
        barScaleFirst.addListener(new AnimShowing());
        barScaleFirst.start();
    }

    /* access modifiers changed from: package-private */
    public void tryCancel() {
        Animator animator = this.executingAnim;
        if (animator != null) {
            animator.cancel();
        }
    }

    abstract class AnimElem implements Animator.AnimatorListener {
        private static final String TAG = "AnimElem";
        protected boolean isCancelled;
        protected String step;

        AnimElem() {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
            Log.d(TAG, "onAnimationStart, Step " + this.step);
            HwFreeFormCaptionViewAnim.this.executingAnim = animation;
            HwFreeFormCaptionViewAnim.this.inAnimating = true;
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            HwFreeFormCaptionViewAnim.this.inAnimating = false;
            Log.d(TAG, "onAnimationEnd, Step " + this.step);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
            this.isCancelled = true;
            Log.i(TAG, "onAnimationCancel, Step " + this.step);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }
    }

    /* access modifiers changed from: package-private */
    public class AnimFading extends AnimElem {
        AnimFading() {
            super();
            this.step = "Fading";
        }

        @Override // com.huawei.internal.widget.HwFreeFormCaptionViewAnim.AnimElem, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            HwFreeFormCaptionViewAnim.this.hideButtons();
            if (!this.isCancelled) {
                HwFreeFormCaptionViewAnim.this.getBarScaleSecond(false).start();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class AnimFaded extends AnimElem {
        AnimFaded() {
            super();
            this.step = "Faded";
        }

        @Override // com.huawei.internal.widget.HwFreeFormCaptionViewAnim.AnimElem, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            HwFreeFormCaptionViewAnim.this.firstTimePopupWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public class AnimShowing extends AnimElem {
        AnimShowing() {
            super();
            this.step = "Showing";
        }

        @Override // com.huawei.internal.widget.HwFreeFormCaptionViewAnim.AnimElem, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (this.isCancelled) {
                HwFreeFormCaptionViewAnim.this.setButtonsVisible();
                HwFreeFormCaptionViewAnim.this.showButtons();
                return;
            }
            runShowedAnim();
        }

        /* access modifiers changed from: package-private */
        public void runShowedAnim() {
            AnimatorSet translateScale = HwFreeFormCaptionViewAnim.this.getTranslateAndScaleAnim(true);
            AnimatorSet alpha = HwFreeFormCaptionViewAnim.this.getAlphaAnim(true);
            ObjectAnimator barScaleSecond = HwFreeFormCaptionViewAnim.this.getBarScaleSecond(true);
            AnimatorSet transAndBarScale = new AnimatorSet();
            transAndBarScale.playTogether(translateScale, barScaleSecond);
            transAndBarScale.start();
            alpha.start();
        }
    }

    /* access modifiers changed from: package-private */
    public class AnimShowed extends AnimElem {
        AnimShowed() {
            super();
            this.step = "Showed";
        }

        @Override // com.huawei.internal.widget.HwFreeFormCaptionViewAnim.AnimElem, android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            HwFreeFormCaptionViewAnim.this.setButtonsVisible();
        }

        @Override // com.huawei.internal.widget.HwFreeFormCaptionViewAnim.AnimElem, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            HwFreeFormCaptionViewAnim.this.showButtons();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideButtons() {
        this.mCaptionView.mDragBarRect.set(0, 0, this.mCaptionView.mCloseRect.right, this.mCaptionView.mCloseRect.bottom);
        this.mCaptionView.mMaximize.setVisibility(8);
        this.mCaptionView.mMinimize.setVisibility(8);
        this.mCaptionView.mClose.setVisibility(8);
        this.mCaptionView.mMaximizeRect.setEmpty();
        this.mCaptionView.mMinimizeRect.setEmpty();
        this.mCaptionView.mCloseRect.setEmpty();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showButtons() {
        this.mCaptionView.mMaximize.setTranslationX(0.0f);
        this.mCaptionView.mMinimize.setTranslationX(0.0f);
        this.mCaptionView.mClose.setTranslationX(0.0f);
        this.mCaptionView.mMaximize.getHitRect(this.mCaptionView.mMaximizeRect);
        this.mCaptionView.mMinimize.getHitRect(this.mCaptionView.mMinimizeRect);
        this.mCaptionView.mClose.getHitRect(this.mCaptionView.mCloseRect);
        this.mCaptionView.mDragBar.getHitRect(this.mCaptionView.mDragBarRect);
        this.mCaptionView.checkIfNeedHide();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setButtonsVisible() {
        this.mCaptionView.mMaximize.setVisibility(0);
        this.mCaptionView.mMinimize.setVisibility(0);
        this.mCaptionView.mClose.setVisibility(0);
        this.mCaptionView.mMaximize.getHitRect(this.mCaptionView.mMaximizeRect);
        this.mCaptionView.mMinimize.getHitRect(this.mCaptionView.mMinimizeRect);
        this.mCaptionView.mClose.getHitRect(this.mCaptionView.mCloseRect);
        this.mCaptionView.mDragBar.getHitRect(this.mCaptionView.mDragBarRect);
        this.mCaptionView.checkIfNeedHide();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void firstTimePopupWindow() {
        HwFreeFormCaptionView hwFreeFormCaptionView = this.mCaptionView;
        if (hwFreeFormCaptionView != null && hwFreeFormCaptionView.mContext != null) {
            int firstAppearState = Settings.Secure.getInt(this.mCaptionView.mContext.getContentResolver(), "drag_bar_popup_first_appear", 0);
            if (this.mCaptionView.mDragBarPopupWindow != null && firstAppearState == 0 && !"com.huawei.hwdockbar".equals(this.mCaptionView.mContext.getPackageName())) {
                this.mCaptionView.mDragBarPopupWindow.show();
                this.mCaptionView.mDragBarPopupWindow.delayDismiss();
                HwActivityTaskManager.saveMultiWindowTipState("drag_bar_popup_first_appear", 1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AnimatorSet getTranslateAndScaleAnim(boolean isShow) {
        float maxTransDistance = 0.0f;
        float minTransDistance = 0.0f;
        float closeTransDistance = 0.0f;
        if (!isShow) {
            float scale = HwActivityTaskManager.getStackScale(-100);
            int[] location = new int[2];
            this.mCaptionView.mMaximize.getLocationOnScreen(location);
            this.mCaptionView.mMinimize.getLocationOnScreen(location);
            this.mCaptionView.mClose.getLocationOnScreen(location);
            float closeStartPos = ((float) location[0]) - ((((float) this.mCaptionView.mClose.getWidth()) * scale) / 2.0f);
            this.mCaptionView.mDragBar.getLocationOnScreen(location);
            float endPos = ((float) location[0]) + ((((float) this.mCaptionView.mDragBar.getWidth()) * scale) / 2.0f);
            maxTransDistance = (endPos - ((float) location[0])) + this.mCaptionView.mMaximize.getTranslationX();
            minTransDistance = (endPos - ((float) location[0])) + this.mCaptionView.mMinimize.getTranslationX();
            closeTransDistance = (endPos - closeStartPos) + this.mCaptionView.mClose.getTranslationX();
        }
        float toScale = 0.5f;
        float fromScale = isShow ? 0.5f : 1.0f;
        if (isShow) {
            toScale = 1.0f;
        }
        ObjectAnimator maxAnimator = ObjectAnimator.ofFloat(this.mCaptionView.mMaximize, TRANSLATE_X, maxTransDistance);
        ObjectAnimator minAnimator = ObjectAnimator.ofFloat(this.mCaptionView.mMinimize, TRANSLATE_X, minTransDistance);
        ObjectAnimator closeAnimator = ObjectAnimator.ofFloat(this.mCaptionView.mClose, TRANSLATE_X, closeTransDistance);
        ObjectAnimator maxScaleX = ObjectAnimator.ofFloat(this.mCaptionView.mMaximize, SCALE_X, fromScale, toScale);
        ObjectAnimator maxScaleY = ObjectAnimator.ofFloat(this.mCaptionView.mMaximize, SCALE_Y, fromScale, toScale);
        ObjectAnimator minScaleX = ObjectAnimator.ofFloat(this.mCaptionView.mMinimize, SCALE_X, fromScale, toScale);
        ObjectAnimator minScaleY = ObjectAnimator.ofFloat(this.mCaptionView.mMinimize, SCALE_Y, fromScale, toScale);
        ObjectAnimator closeScaleX = ObjectAnimator.ofFloat(this.mCaptionView.mClose, SCALE_X, fromScale, toScale);
        ObjectAnimator closeScaleY = ObjectAnimator.ofFloat(this.mCaptionView.mClose, SCALE_Y, fromScale, toScale);
        Interpolator frictionCurve = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(maxAnimator, minAnimator, closeAnimator, maxScaleX, maxScaleY, minScaleX, minScaleY, closeScaleX, closeScaleY);
        animatorSet.setDuration(DURATION_TRANSLATE);
        animatorSet.setInterpolator(frictionCurve);
        if (isShow) {
            animatorSet.addListener(new AnimShowed());
        } else {
            animatorSet.addListener(new AnimFading());
        }
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AnimatorSet getAlphaAnim(boolean isShow) {
        float fromAlpha = isShow ? 0.0f : 1.0f;
        float toAlpha = isShow ? 1.0f : 0.0f;
        ObjectAnimator maxAlphaAnim = ObjectAnimator.ofFloat(this.mCaptionView.mMaximize, ALPHA, fromAlpha, toAlpha);
        ObjectAnimator minAlphaAnim = ObjectAnimator.ofFloat(this.mCaptionView.mMinimize, ALPHA, fromAlpha, toAlpha);
        ObjectAnimator closeAlphaAnim = ObjectAnimator.ofFloat(this.mCaptionView.mClose, ALPHA, fromAlpha, toAlpha);
        Interpolator sharpCurve = new PathInterpolator(SHARP_CX1, 0.0f, SHARP_CX2, 1.0f);
        AnimatorSet alphaSet = new AnimatorSet();
        alphaSet.playTogether(maxAlphaAnim, minAlphaAnim, closeAlphaAnim);
        alphaSet.setInterpolator(sharpCurve);
        alphaSet.setDuration(DURATION_ALPHA_SCALE);
        return alphaSet;
    }

    private ObjectAnimator getBarScaleFirst(boolean isShow) {
        float toScale = isShow ? BAR_SCALE_SMALL : BAR_SCALE_BIG;
        Interpolator frictionCurve = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
        ObjectAnimator barScaleAnim = ObjectAnimator.ofFloat(this.mCaptionView.mDragBar, SCALE_X, 1.0f, toScale);
        barScaleAnim.setInterpolator(frictionCurve);
        barScaleAnim.setDuration(DURATION_ALPHA_SCALE);
        return barScaleAnim;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ObjectAnimator getBarScaleSecond(boolean isShow) {
        ObjectAnimator barScaleAnim = ObjectAnimator.ofFloat(this.mCaptionView.mDragBar, SCALE_X, this.mCaptionView.mDragBar.getScaleX(), isShow ? BAR_SCALE_BIG : BAR_SCALE_SMALL, 1.0f);
        SpringInterpolator interpolator = new SpringInterpolator(800.0f, PARAMS_DAMPING);
        barScaleAnim.setInterpolator(interpolator);
        barScaleAnim.setDuration((long) interpolator.getDuration());
        if (!isShow) {
            barScaleAnim.addListener(new AnimFaded());
        }
        return barScaleAnim;
    }
}
