package com.android.server.multiwin.animation;

import android.os.Bundle;
import android.util.Slog;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.server.wm.ActivityTaskManagerService;

public class HwTopStackExitAnim extends HwSplitBarExitAniStrategy {
    private static final String TAG = "HwTopStackExitAnim";

    HwTopStackExitAnim(ActivityTaskManagerService service, View left, View dragBar, View right, View dragFullZone, Bundle bundle) {
        super(service, left, dragBar, right, dragFullZone, bundle);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public long getAniDuration() {
        float duration = (this.mCurPos * 300.0f) / ((float) (this.mHeight / this.mHeightColumns));
        Slog.i(TAG, " animation duration = " + duration);
        return (long) duration;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public void getScaleAnim(float value) {
        if (this.mRight != null && (this.mRight instanceof RelativeLayout)) {
            View img = ((RelativeLayout) this.mRight).getChildAt(0);
            try {
                this.mRight.setScaleY((1.0f * value) / ((float) this.mRight.getHeight()));
                this.mRight.setTranslationY((((float) this.mRight.getHeight()) - value) / 2.0f);
                if (img != null) {
                    img.setScaleY(((float) this.mRight.getHeight()) / value);
                }
            } catch (IllegalArgumentException e) {
                Slog.i(TAG, "IllegalArgumentException happened when getScaleAnim ");
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public int getScaleUpEndLen() {
        return this.mHeight;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public int getScaleUpStartLen() {
        return this.mRight.getHeight();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public float getDragBarTransDis() {
        return -(this.mCurPos + ((float) this.mDragBar.getHeight()));
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public float getTranslateDistance() {
        return -this.mCurPos;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public String getTranslateDirect() {
        return "translationY";
    }
}
