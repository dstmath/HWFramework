package com.android.server.multiwin.animation;

import android.os.Bundle;
import android.util.Slog;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.server.wm.ActivityTaskManagerService;

public class HwLeftStackExitAnim extends HwSplitBarExitAniStrategy {
    private static final String TAG = "HwLeftStackExitAnim";

    HwLeftStackExitAnim(ActivityTaskManagerService service, View left, View dragView, View right, View dragFullZone, Bundle bundle) {
        super(service, left, dragView, right, dragFullZone, bundle);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public long getAniDuration() {
        float duration = (this.mCurPos * 300.0f) / ((float) (this.mWidth / this.mWidthColumns));
        Slog.i(TAG, " animation duration = " + duration);
        return (long) duration;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public int getScaleUpStartLen() {
        return this.mRight.getWidth();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public float getDragBarTransDis() {
        return -(this.mCurPos + ((float) this.mDragBar.getWidth()));
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public float getTranslateDistance() {
        return -this.mCurPos;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.animation.HwSplitBarExitAniStrategy
    public void getScaleAnim(float value) {
        if (this.mRight != null && (this.mRight instanceof RelativeLayout)) {
            View img = ((RelativeLayout) this.mRight).getChildAt(0);
            try {
                this.mRight.setScaleX((1.0f * value) / ((float) this.mRight.getWidth()));
                this.mRight.setTranslationX((((float) this.mRight.getWidth()) - value) / 2.0f);
                if (img != null) {
                    img.setScaleX(((float) this.mRight.getWidth()) / value);
                }
            } catch (IllegalArgumentException e) {
                Slog.i(TAG, "IllegalArgumentException happened when getScaleAnim ");
            }
        }
    }
}
