package com.android.server.wm;

import android.view.SurfaceControl;
import com.android.server.wm.LocalAnimationAdapter;

public class AnimationSpecEx {
    private AnimationSpecBridge mBridge = new AnimationSpecBridge();

    public AnimationSpecEx() {
        this.mBridge.setAnimationSpecEx(this);
    }

    public long getDuration() {
        return 0;
    }

    public void apply(SurfaceControl.Transaction t, SurfaceControl leash, long currentPlayTime) {
    }

    public LocalAnimationAdapter.AnimationSpec getAnimationSpec() {
        return this.mBridge;
    }
}
