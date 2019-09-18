package com.android.server.wm;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import com.android.server.wm.LocalAnimationAdapter;
import java.io.PrintWriter;

public class WindowAnimationSpec implements LocalAnimationAdapter.AnimationSpec {
    private Animation mAnimation;
    private final boolean mCanSkipFirstFrame;
    private final boolean mIsAppAnimation;
    private final Point mPosition;
    private final Rect mStackBounds;
    private int mStackClipMode;
    private final ThreadLocal<TmpValues> mThreadLocalTmps;
    private final Rect mTmpRect;

    private static class TmpValues {
        final float[] floats;
        final Transformation transformation;

        private TmpValues() {
            this.transformation = new Transformation();
            this.floats = new float[9];
        }
    }

    static /* synthetic */ TmpValues lambda$new$0() {
        return new TmpValues();
    }

    public WindowAnimationSpec(Animation animation, Point position, boolean canSkipFirstFrame) {
        this(animation, position, null, canSkipFirstFrame, 2, false);
    }

    public WindowAnimationSpec(Animation animation, Point position, Rect stackBounds, boolean canSkipFirstFrame, int stackClipMode, boolean isAppAnimation) {
        this.mPosition = new Point();
        this.mThreadLocalTmps = ThreadLocal.withInitial($$Lambda$WindowAnimationSpec$jKE7Phq2DESkeBondpaNPBLn6Cs.INSTANCE);
        this.mStackBounds = new Rect();
        this.mTmpRect = new Rect();
        this.mAnimation = animation;
        if (position != null) {
            this.mPosition.set(position.x, position.y);
        }
        this.mCanSkipFirstFrame = canSkipFirstFrame;
        this.mIsAppAnimation = isAppAnimation;
        this.mStackClipMode = stackClipMode;
        if (stackBounds != null) {
            this.mStackBounds.set(stackBounds);
        }
    }

    public boolean getDetachWallpaper() {
        return this.mAnimation.getDetachWallpaper();
    }

    public boolean getShowWallpaper() {
        return this.mAnimation.getShowWallpaper();
    }

    public int getBackgroundColor() {
        return this.mAnimation.getBackgroundColor();
    }

    public long getDuration() {
        return this.mAnimation.computeDurationHint();
    }

    public void apply(SurfaceControl.Transaction t, SurfaceControl leash, long currentPlayTime) {
        TmpValues tmp = this.mThreadLocalTmps.get();
        tmp.transformation.clear();
        this.mAnimation.getTransformation(currentPlayTime, tmp.transformation);
        tmp.transformation.getMatrix().postTranslate((float) this.mPosition.x, (float) this.mPosition.y);
        t.setMatrix(leash, tmp.transformation.getMatrix(), tmp.floats);
        t.setAlpha(leash, tmp.transformation.getAlpha());
        if (this.mStackClipMode == 2) {
            t.setWindowCrop(leash, tmp.transformation.getClipRect());
        } else if (this.mStackClipMode == 0) {
            this.mTmpRect.set(this.mStackBounds);
            this.mTmpRect.offsetTo(this.mPosition.x, this.mPosition.y);
            t.setFinalCrop(leash, this.mTmpRect);
            t.setWindowCrop(leash, tmp.transformation.getClipRect());
        } else {
            this.mTmpRect.set(this.mStackBounds);
            this.mTmpRect.intersect(tmp.transformation.getClipRect());
            t.setWindowCrop(leash, this.mTmpRect);
        }
    }

    public long calculateStatusBarTransitionStartTime() {
        TranslateAnimation openTranslateAnimation = findTranslateAnimation(this.mAnimation);
        if (openTranslateAnimation == null) {
            return SystemClock.uptimeMillis();
        }
        return ((SystemClock.uptimeMillis() + openTranslateAnimation.getStartOffset()) + ((long) (((float) openTranslateAnimation.getDuration()) * findAlmostThereFraction(openTranslateAnimation.getInterpolator())))) - 120;
    }

    public boolean canSkipFirstFrame() {
        return this.mCanSkipFirstFrame;
    }

    public boolean needsEarlyWakeup() {
        return this.mIsAppAnimation;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println(this.mAnimation);
    }

    public void writeToProtoInner(ProtoOutputStream proto) {
        long token = proto.start(1146756268033L);
        proto.write(1138166333441L, this.mAnimation.toString());
        proto.end(token);
    }

    private static TranslateAnimation findTranslateAnimation(Animation animation) {
        if (animation instanceof TranslateAnimation) {
            return (TranslateAnimation) animation;
        }
        if (animation instanceof AnimationSet) {
            AnimationSet set = (AnimationSet) animation;
            for (int i = 0; i < set.getAnimations().size(); i++) {
                Animation a = set.getAnimations().get(i);
                if (a instanceof TranslateAnimation) {
                    return (TranslateAnimation) a;
                }
            }
        }
        return null;
    }

    private static float findAlmostThereFraction(Interpolator interpolator) {
        float val = 0.5f;
        for (float adj = 0.25f; adj >= 0.01f; adj /= 2.0f) {
            if (interpolator.getInterpolation(val) < 0.99f) {
                val += adj;
            } else {
                val -= adj;
            }
        }
        return val;
    }
}
