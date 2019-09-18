package com.android.server.wm;

import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import com.android.server.wm.SurfaceAnimator;
import java.io.PrintWriter;

interface AnimationAdapter {
    public static final long STATUS_BAR_TRANSITION_DURATION = 120;

    void dump(PrintWriter printWriter, String str);

    int getBackgroundColor();

    boolean getDetachWallpaper();

    long getDurationHint();

    boolean getShowWallpaper();

    long getStatusBarTransitionsStartTime();

    void onAnimationCancelled(SurfaceControl surfaceControl);

    void startAnimation(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction, SurfaceAnimator.OnAnimationFinishedCallback onAnimationFinishedCallback);

    void writeToProto(ProtoOutputStream protoOutputStream);

    void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        writeToProto(proto);
        proto.end(token);
    }
}
