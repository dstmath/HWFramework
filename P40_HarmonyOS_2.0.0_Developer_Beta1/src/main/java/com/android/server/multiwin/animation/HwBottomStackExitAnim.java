package com.android.server.multiwin.animation;

import android.os.Bundle;
import android.util.Slog;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.server.wm.ActivityTaskManagerService;

public class HwBottomStackExitAnim extends HwSplitBarExitAniStrategy {
    private static final String TAG = "HwBottomStackExitAnim";

    HwBottomStackExitAnim(ActivityTaskManagerService service, View left, View dragBar, View right, View dragFullZone, Bundle bundle) {
        super(service, left, dragBar, right, dragFullZone, bundle);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public long getAniDuration() {
        float duration = ((((float) this.mHeight) - this.mCurPos) * 300.0f) / ((float) (this.mHeight / this.mHeightColumns));
        Slog.i(TAG, " animation duration = " + duration);
        return (long) duration;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public int getScaleUpEndLen() {
        return this.mHeight;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public int getScaleUpStartLen() {
        return this.mLeft.getHeight();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public float getDragBarTransDis() {
        return ((float) this.mHeight) - this.mCurPos;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public void getScaleAnim(float value) {
        if (this.mLeft != null && (this.mLeft instanceof RelativeLayout)) {
            View img = ((RelativeLayout) this.mLeft).getChildAt(0);
            try {
                this.mLeft.setScaleY((1.0f * value) / ((float) this.mLeft.getHeight()));
                this.mLeft.setTranslationY((value - ((float) this.mLeft.getHeight())) / 2.0f);
                if (img != null) {
                    img.setScaleY(((float) this.mLeft.getHeight()) / value);
                }
            } catch (IllegalArgumentException e) {
                Slog.i(TAG, "IllegalArgumentException happened when getScaleAnim ");
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public float getTranslateDistance() {
        return ((float) this.mHeight) - this.mCurPos;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public String getTranslateDirect() {
        return "translationY";
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public View getScaleDownView() {
        return this.mRight;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public View getScaleUpView() {
        return this.mLeft;
    }
}
