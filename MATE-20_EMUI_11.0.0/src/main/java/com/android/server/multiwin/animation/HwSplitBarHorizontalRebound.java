package com.android.server.multiwin.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Slog;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.HwMultiWindowSplitUI;

public class HwSplitBarHorizontalRebound extends HwSplitBarReboundStrategy {
    HwSplitBarHorizontalRebound(ActivityTaskManagerService service, View left, View dragView, View right, Bundle bundle) {
        super(service, left, dragView, right, bundle);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarReboundStrategy
    public ValueAnimator getCoverScaleAnim() {
        this.mLeft.setPivotX(0.0f);
        HwMultiWindowSplitUI split = HwMultiWindowSplitUI.getInstance(this.mAtms.getUiContext(), this.mAtms, this.mDisplayId);
        int offSet = split.primaryBounds.left;
        int statusBarH = split.getNotchSizeOnRight();
        ValueAnimator scaleUp = ValueAnimator.ofInt(this.mLeft.getWidth() + offSet, (int) this.mEndPosition);
        scaleUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(statusBarH, split, offSet) {
            /* class com.android.server.multiwin.animation.$$Lambda$HwSplitBarHorizontalRebound$jbKMNjX68FzxvXfesEak00zf3Ho */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ HwMultiWindowSplitUI f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                HwSplitBarHorizontalRebound.this.lambda$getCoverScaleAnim$0$HwSplitBarHorizontalRebound(this.f$1, this.f$2, this.f$3, valueAnimator);
            }
        });
        return scaleUp;
    }

    public /* synthetic */ void lambda$getCoverScaleAnim$0$HwSplitBarHorizontalRebound(int statusBarH, HwMultiWindowSplitUI split, int offSet, ValueAnimator animation) {
        float value = 1.0f;
        if (animation.getAnimatedValue() instanceof Integer) {
            value = ((float) ((Integer) animation.getAnimatedValue()).intValue()) * 1.0f;
        }
        if (this.mLeft != null && (this.mLeft instanceof RelativeLayout) && this.mRight != null && (this.mRight instanceof RelativeLayout)) {
            View leftIcon = ((RelativeLayout) this.mLeft).getChildAt(0);
            View rightIcon = ((RelativeLayout) this.mRight).getChildAt(0);
            try {
                float rightScaledWidth = (((((float) this.mWidth) - value) - ((float) this.mDragBar.getWidth())) - ((float) statusBarH)) - ((float) split.getNavBarRight());
                float leftScale = (value - ((float) offSet)) / ((float) this.mLeft.getWidth());
                float rightScale = (rightScaledWidth * 1.0f) / ((float) this.mRight.getWidth());
                this.mLeft.setScaleX(leftScale);
                this.mRight.setScaleX(rightScale);
                this.mRight.setTranslationX((((float) this.mRight.getWidth()) - rightScaledWidth) / 2.0f);
                if (leftIcon != null) {
                    leftIcon.setScaleX(1.0f / leftScale);
                }
                if (rightIcon != null) {
                    rightIcon.setScaleX(1.0f / rightScale);
                }
            } catch (IllegalArgumentException e) {
                Slog.i("HwSplitBarReboundStrategy", "IllegalArgumentException happened when getScaleAnim ");
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarReboundStrategy
    public ObjectAnimator getDragLineTransAnim(Interpolator interpolator) {
        ObjectAnimator dragTransAnim = ObjectAnimator.ofFloat(this.mDragBar, "translationY", this.mDragBar.getTranslationY(), 0.0f);
        dragTransAnim.setInterpolator(interpolator);
        return dragTransAnim;
    }
}
