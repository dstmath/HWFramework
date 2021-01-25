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

public class HwSplitBarVerticalRebound extends HwSplitBarReboundStrategy {
    HwSplitBarVerticalRebound(ActivityTaskManagerService service, View left, View dragView, View right, Bundle bundle) {
        super(service, left, dragView, right, bundle);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarReboundStrategy
    public String getTranslation() {
        return "translationY";
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarReboundStrategy
    public float getDragBarTranslate() {
        return this.mDragBar.getTranslationY();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarReboundStrategy
    public ValueAnimator getCoverScaleAnim() {
        this.mLeft.setPivotY(0.0f);
        HwMultiWindowSplitUI split = HwMultiWindowSplitUI.getInstance(this.mAtms.getUiContext(), this.mAtms, this.mDisplayId);
        split.isNavBarMini();
        int offSet = split.primaryBounds.top;
        ValueAnimator scaleUp = ValueAnimator.ofInt(this.mLeft.getHeight() + offSet, (int) this.mEndPosition);
        scaleUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(split, offSet) {
            /* class com.android.server.multiwin.animation.$$Lambda$HwSplitBarVerticalRebound$dhlAFPylLqGRYJZsGqvgP5bE22g */
            private final /* synthetic */ HwMultiWindowSplitUI f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                HwSplitBarVerticalRebound.this.lambda$getCoverScaleAnim$0$HwSplitBarVerticalRebound(this.f$1, this.f$2, valueAnimator);
            }
        });
        return scaleUp;
    }

    public /* synthetic */ void lambda$getCoverScaleAnim$0$HwSplitBarVerticalRebound(HwMultiWindowSplitUI split, int offSet, ValueAnimator animation) {
        float value = 1.0f;
        if (animation.getAnimatedValue() instanceof Integer) {
            value = ((float) ((Integer) animation.getAnimatedValue()).intValue()) * 1.0f;
        }
        if (this.mLeft != null && (this.mLeft instanceof RelativeLayout) && this.mRight != null && (this.mRight instanceof RelativeLayout)) {
            View leftIcon = ((RelativeLayout) this.mLeft).getChildAt(0);
            View rightIcon = ((RelativeLayout) this.mRight).getChildAt(0);
            try {
                float rightScaledHeight = ((((float) this.mHeight) - value) - ((float) this.mDragBar.getHeight())) - ((float) split.getNavBarBottomUpDown());
                float leftScale = (value - ((float) offSet)) / ((float) this.mLeft.getHeight());
                float rightScale = rightScaledHeight / ((float) this.mRight.getHeight());
                this.mLeft.setScaleY(leftScale);
                this.mRight.setScaleY(rightScale);
                this.mRight.setTranslationY((((float) this.mRight.getHeight()) - rightScaledHeight) / 2.0f);
                if (leftIcon != null) {
                    leftIcon.setScaleY(1.0f / leftScale);
                }
                if (rightIcon != null) {
                    rightIcon.setScaleY(1.0f / rightScale);
                }
            } catch (IllegalArgumentException e) {
                Slog.i("HwSplitBarReboundStrategy", "IllegalArgumentException happened when getScaleAnim ");
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarReboundStrategy
    public ObjectAnimator getDragLineTransAnim(Interpolator interpolator) {
        ObjectAnimator dragTransAnim = ObjectAnimator.ofFloat(this.mDragBar, "translationX", this.mDragBar.getTranslationX(), 0.0f);
        dragTransAnim.setInterpolator(interpolator);
        return dragTransAnim;
    }
}
