package com.android.server.wm;

import android.os.SystemClock;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import com.android.server.wm.SurfaceAnimator;
import java.io.PrintWriter;

class LocalAnimationAdapter implements AnimationAdapter {
    private final SurfaceAnimationRunner mAnimator;
    private final AnimationSpec mSpec;

    interface AnimationSpec {
        void apply(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, long j);

        void dump(PrintWriter printWriter, String str);

        long getDuration();

        void writeToProtoInner(ProtoOutputStream protoOutputStream);

        boolean getDetachWallpaper() {
            return false;
        }

        boolean getShowWallpaper() {
            return false;
        }

        int getBackgroundColor() {
            return 0;
        }

        long calculateStatusBarTransitionStartTime() {
            return SystemClock.uptimeMillis();
        }

        boolean canSkipFirstFrame() {
            return false;
        }

        boolean needsEarlyWakeup() {
            return false;
        }

        void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            writeToProtoInner(proto);
            proto.end(token);
        }
    }

    LocalAnimationAdapter(AnimationSpec spec, SurfaceAnimationRunner animator) {
        this.mSpec = spec;
        this.mAnimator = animator;
    }

    public boolean getDetachWallpaper() {
        return this.mSpec.getDetachWallpaper();
    }

    public boolean getShowWallpaper() {
        return this.mSpec.getShowWallpaper();
    }

    public int getBackgroundColor() {
        return this.mSpec.getBackgroundColor();
    }

    public void startAnimation(SurfaceControl animationLeash, SurfaceControl.Transaction t, SurfaceAnimator.OnAnimationFinishedCallback finishCallback) {
        this.mAnimator.startAnimation(this.mSpec, animationLeash, t, new Runnable(finishCallback) {
            private final /* synthetic */ SurfaceAnimator.OnAnimationFinishedCallback f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                this.f$1.onAnimationFinished(LocalAnimationAdapter.this);
            }
        });
    }

    public void onAnimationCancelled(SurfaceControl animationLeash) {
        this.mAnimator.onAnimationCancelled(animationLeash);
    }

    public long getDurationHint() {
        return this.mSpec.getDuration();
    }

    public long getStatusBarTransitionsStartTime() {
        return this.mSpec.calculateStatusBarTransitionStartTime();
    }

    public void dump(PrintWriter pw, String prefix) {
        this.mSpec.dump(pw, prefix);
    }

    public void writeToProto(ProtoOutputStream proto) {
        long token = proto.start(1146756268033L);
        this.mSpec.writeToProto(proto, 1146756268033L);
        proto.end(token);
    }
}
