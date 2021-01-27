package com.android.server.multiwin.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.HwMultiWindowSplitUI;
import com.android.server.wm.HwSplitBarConstants;

public class HwDragFullAnimStrategy {
    private static final long DEFAULT_ANIMATION_TIME = 250;
    private static final long SCALE_ANIMATION_TIME = 100;
    private static final String TAG = "HwDragFullAnimStrategy";
    private static final String TRANSLATION_X = "translationX";
    private ActivityTaskManagerService mAtms;
    private int mDisplayId;
    private int mDragFullLineColor;
    private View mDragLine;
    private int mDragLineColor;
    private View mFarLine;
    private View mNearLine;
    private int mNearLineTrans = 0;
    private int mType;
    private int mWidth;
    private View mZone;

    private HwDragFullAnimStrategy(ActivityTaskManagerService service, View zone, View farLine, View nearLine, View dragLine, Bundle bundle) {
        this.mAtms = service;
        this.mZone = zone;
        this.mFarLine = farLine;
        this.mNearLine = nearLine;
        this.mDragLine = dragLine;
        if (bundle != null) {
            this.mDisplayId = bundle.getInt(HwSplitBarConstants.SPLIT_BAR_DISPLAY_ID);
            this.mNearLineTrans = bundle.getInt(HwSplitBarConstants.NEAR_LINE_TRANS);
            this.mDragLineColor = bundle.getInt(HwSplitBarConstants.DRAG_LINE_COLOR);
            this.mDragFullLineColor = bundle.getInt(HwSplitBarConstants.DRAG_FULL_MODE_LINE_COLOR);
            this.mWidth = bundle.getInt(HwSplitBarConstants.DISPLAY_WIDTH);
            this.mType = bundle.getInt(HwSplitBarConstants.SPLIT_RATIO);
        }
    }

    public static HwDragFullAnimStrategy getStrategy(ActivityTaskManagerService service, View zone, View farLine, View nearLine, View dragLine, Bundle bundle) {
        return new HwDragFullAnimStrategy(service, zone, farLine, nearLine, dragLine, bundle);
    }

    public void split2DragFullAnim() {
        this.mZone.setVisibility(0);
        if (this.mZone.getBackground() != null) {
            this.mZone.getBackground().setAlpha(0);
        }
        this.mNearLine.setVisibility(0);
        View view = this.mNearLine;
        ObjectAnimator nearLineTransAnim = ObjectAnimator.ofFloat(view, TRANSLATION_X, view.getTranslationX() - ((float) this.mNearLineTrans), 0.0f);
        ObjectAnimator nearLineAlphaAnim = ObjectAnimator.ofFloat(this.mNearLine, View.ALPHA, 0.0f, 1.0f);
        ValueAnimator zoneAlphaAnim = ValueAnimator.ofInt(0, 255);
        zoneAlphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.multiwin.animation.HwDragFullAnimStrategy.AnonymousClass1 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                if (HwDragFullAnimStrategy.this.mZone.getBackground() != null) {
                    HwDragFullAnimStrategy.this.mZone.getBackground().setAlpha(((Integer) animation.getAnimatedValue()).intValue());
                }
            }
        });
        Interpolator standardCurve = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
        Interpolator sharpCurve = new PathInterpolator(0.33f, 0.0f, 0.36f, 1.0f);
        zoneAlphaAnim.setInterpolator(sharpCurve);
        nearLineTransAnim.setInterpolator(standardCurve);
        nearLineAlphaAnim.setInterpolator(sharpCurve);
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(DEFAULT_ANIMATION_TIME);
        animSet.addListener(new Split2DragListener());
        animSet.playTogether(zoneAlphaAnim, nearLineTransAnim, nearLineAlphaAnim);
        animSet.start();
    }

    public void dragFullMode2SplitAnim(ValueAnimator dragAnimator) {
        View view = this.mNearLine;
        ObjectAnimator nearLineTransAnim = ObjectAnimator.ofFloat(view, TRANSLATION_X, view.getTranslationX(), this.mFarLine.getX() - this.mNearLine.getX());
        ObjectAnimator nearLineAlphaAnim = ObjectAnimator.ofFloat(this.mNearLine, View.ALPHA, 1.0f, 0.0f);
        ObjectAnimator farLineColorAnim = ObjectAnimator.ofObject(this.mFarLine, "backgroundColor", new ArgbEvaluator(), Integer.valueOf(this.mDragFullLineColor), Integer.valueOf(this.mDragLineColor));
        ValueAnimator zoneAnim = getZoneAlphaAnim();
        Interpolator standardCurve = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
        Interpolator sharpCurve = new PathInterpolator(0.33f, 0.0f, 0.36f, 1.0f);
        farLineColorAnim.setInterpolator(sharpCurve);
        nearLineTransAnim.setInterpolator(standardCurve);
        nearLineAlphaAnim.setInterpolator(sharpCurve);
        zoneAnim.setInterpolator(sharpCurve);
        zoneAnim.setInterpolator(sharpCurve);
        AnimatorSet sets = new AnimatorSet();
        sets.playTogether(zoneAnim, dragAnimator, nearLineTransAnim, nearLineAlphaAnim, farLineColorAnim);
        sets.setDuration(DEFAULT_ANIMATION_TIME);
        sets.addListener(new Drag2SplitListener());
        sets.start();
    }

    private ValueAnimator getZoneAlphaAnim() {
        RelativeLayout.LayoutParams zoneLp = (RelativeLayout.LayoutParams) this.mZone.getLayoutParams();
        zoneLp.removeRule(1);
        zoneLp.removeRule(0);
        if (this.mType == 5) {
            zoneLp.setMargins(this.mWidth - this.mZone.getWidth(), 0, 0, 0);
        }
        this.mZone.setLayoutParams(zoneLp);
        ValueAnimator zoneAnim = ValueAnimator.ofInt(255, 0);
        zoneAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.multiwin.animation.HwDragFullAnimStrategy.AnonymousClass2 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                if (HwDragFullAnimStrategy.this.mZone.getBackground() != null) {
                    HwDragFullAnimStrategy.this.mZone.getBackground().setAlpha(((Integer) animation.getAnimatedValue()).intValue());
                }
            }
        });
        return zoneAnim;
    }

    public void splitBarDarkModeAnim() {
        Interpolator sharpCurve = new PathInterpolator(0.33f, 0.0f, 0.36f, 1.0f);
        ObjectAnimator zoneAlpha = ObjectAnimator.ofFloat(this.mZone, View.ALPHA, this.mZone.getAlpha(), 0.2f);
        zoneAlpha.setDuration(750L);
        zoneAlpha.setInterpolator(sharpCurve);
        zoneAlpha.start();
    }

    public void dragFullZoneScaleDownAnim() {
        Animation animation = new ScaleAnimation(1.0f, 1.06f, 1.0f, 1.06f, 1, this.mType == 6 ? 0.0f : 1.0f, 1, 0.5f);
        animation.setDuration(SCALE_ANIMATION_TIME);
        animation.setFillAfter(true);
        this.mZone.startAnimation(animation);
    }

    public void dragFullZoneScaleGoneAnim() {
        Animation animation = new ScaleAnimation(1.06f, 1.0f, 1.06f, 1.0f, 1, 0.0f, 1, 0.5f);
        animation.setDuration(SCALE_ANIMATION_TIME);
        animation.setFillAfter(true);
        animation.setAnimationListener(new DragFullZoneScaleDownListener());
        this.mZone.startAnimation(animation);
    }

    /* access modifiers changed from: private */
    public class Split2DragListener implements Animator.AnimatorListener {
        private Split2DragListener() {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            HwDragFullAnimStrategy.this.mFarLine.setAlpha(1.0f);
            HwDragFullAnimStrategy.this.mNearLine.setTranslationX(0.0f);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
            HwDragFullAnimStrategy.this.mNearLine.setAlpha(1.0f);
            HwDragFullAnimStrategy.this.mNearLine.setTranslationX(0.0f);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }
    }

    /* access modifiers changed from: private */
    public class Drag2SplitListener implements Animator.AnimatorListener {
        private Drag2SplitListener() {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
            HwDragFullAnimStrategy.this.mDragLine.setVisibility(4);
            HwDragFullAnimStrategy.this.mZone.setScaleX(1.0f);
            HwDragFullAnimStrategy.this.mZone.setScaleY(1.0f);
            HwDragFullAnimStrategy.this.mZone.clearAnimation();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            HwDragFullAnimStrategy.this.drag2SplitAnimEnd();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
            HwDragFullAnimStrategy.this.drag2SplitAnimEnd();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }
    }

    /* access modifiers changed from: private */
    public class DragFullZoneScaleDownListener implements Animation.AnimationListener {
        private DragFullZoneScaleDownListener() {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
            HwDragFullAnimStrategy.this.mZone.setScaleX(1.0f);
            HwDragFullAnimStrategy.this.mZone.setScaleY(1.0f);
            HwDragFullAnimStrategy.this.mZone.clearAnimation();
            HwMultiWindowSplitUI.getInstance(HwDragFullAnimStrategy.this.mAtms.getUiContext(), HwDragFullAnimStrategy.this.mAtms, HwDragFullAnimStrategy.this.mDisplayId).resetDragFullZoneAfterScale(HwDragFullAnimStrategy.this.mType);
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void drag2SplitAnimEnd() {
        this.mDragLine.setVisibility(0);
        this.mNearLine.setTranslationX(0.0f);
        this.mNearLine.setTranslationY(0.0f);
        this.mNearLine.setAlpha(1.0f);
        this.mZone.setTranslationX(0.0f);
        this.mZone.setTranslationY(0.0f);
        this.mZone.setScaleX(1.0f);
        this.mZone.setScaleY(1.0f);
        this.mZone.setAlpha(0.7f);
        if (this.mType == 5) {
            RelativeLayout.LayoutParams zoneLp = (RelativeLayout.LayoutParams) this.mZone.getLayoutParams();
            zoneLp.setMargins(0, 0, 0, 0);
            this.mZone.setLayoutParams(zoneLp);
        }
        this.mZone.setVisibility(8);
        this.mNearLine.setVisibility(8);
        this.mFarLine.setVisibility(8);
        HwMultiWindowSplitUI.getInstance(this.mAtms.getUiContext(), this.mAtms, this.mDisplayId).resetSplitBarFromDragFullMode();
    }
}
