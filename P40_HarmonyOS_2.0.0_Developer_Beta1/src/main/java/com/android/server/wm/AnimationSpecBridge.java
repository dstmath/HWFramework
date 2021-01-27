package com.android.server.wm;

import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import com.android.server.wm.LocalAnimationAdapter;
import java.io.PrintWriter;

public class AnimationSpecBridge implements LocalAnimationAdapter.AnimationSpec {
    private AnimationSpecEx mAnimationSpecEx;

    public void setAnimationSpecEx(AnimationSpecEx animationSpecEx) {
        this.mAnimationSpecEx = animationSpecEx;
    }

    public long getDuration() {
        AnimationSpecEx animationSpecEx = this.mAnimationSpecEx;
        if (animationSpecEx != null) {
            return animationSpecEx.getDuration();
        }
        return 0;
    }

    public void apply(SurfaceControl.Transaction t, SurfaceControl leash, long currentPlayTime) {
        AnimationSpecEx animationSpecEx = this.mAnimationSpecEx;
        if (animationSpecEx != null) {
            animationSpecEx.apply(t, leash, currentPlayTime);
        }
    }

    public void writeToProtoInner(ProtoOutputStream proto) {
    }

    public void dump(PrintWriter pw, String prefix) {
    }
}
