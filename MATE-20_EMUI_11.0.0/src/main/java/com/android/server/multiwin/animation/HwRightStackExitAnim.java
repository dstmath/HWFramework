package com.android.server.multiwin.animation;

import android.os.Bundle;
import android.util.Slog;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.server.wm.ActivityTaskManagerService;

public class HwRightStackExitAnim extends HwSplitBarExitAniStrategy {
    private static final String TAG = "HwRightStackExitAnim";

    HwRightStackExitAnim(ActivityTaskManagerService service, View left, View dragView, View right, View dragFullZone, Bundle bundle) {
        super(service, left, dragView, right, dragFullZone, bundle);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public long getAniDuration() {
        float duration = ((((float) this.mWidth) - this.mCurPos) * 300.0f) / ((float) (this.mWidth / this.mWidthColumns));
        Slog.i(TAG, " animation duration = " + duration);
        return (long) duration;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public int getScaleUpStartLen() {
        return this.mLeft.getWidth();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public float getDragBarTransDis() {
        return ((float) this.mWidth) - this.mCurPos;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public float getTranslateDistance() {
        return ((float) this.mWidth) - this.mCurPos;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public void getScaleAnim(float value) {
        if (this.mLeft != null && (this.mLeft instanceof RelativeLayout)) {
            View img = ((RelativeLayout) this.mLeft).getChildAt(0);
            try {
                this.mLeft.setScaleX((1.0f * value) / ((float) this.mLeft.getWidth()));
                this.mLeft.setTranslationX((value - ((float) this.mLeft.getWidth())) / 2.0f);
                if (img != null) {
                    img.setScaleX(((float) this.mLeft.getWidth()) / value);
                }
            } catch (IllegalArgumentException e) {
                Slog.i(TAG, "IllegalArgumentException happened when getScaleAnim ");
            }
        }
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
