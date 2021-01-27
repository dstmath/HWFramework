package com.android.server.wm;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Slog;
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
    private static final String TAG = "WindowAnimationSpec";
    private long mAnimaitonStartDelay;
    private Animation mAnimation;
    private long mAnimationTime;
    private final boolean mCanSkipFirstFrame;
    private Interpolator mCornerRadiusInterpolator;
    private SurfaceControl.CornerCurveParams mDynamicCornerCurveParams;
    private float mEndRadius;
    private final boolean mIsAppAnimation;
    private boolean mIsDynamicCornerRadius;
    private final Point mPosition;
    private float mScaleSetting;
    private final Rect mStackBounds;
    private int mStackClipMode;
    private float mStartRadius;
    private final ThreadLocal<TmpValues> mThreadLocalTmps;
    private final Rect mTmpRect;
    private final float mWindowCornerRadius;

    static /* synthetic */ TmpValues lambda$new$0() {
        return new TmpValues();
    }

    public WindowAnimationSpec(Animation animation, Point position, boolean canSkipFirstFrame, float windowCornerRadius) {
        this(animation, position, null, canSkipFirstFrame, 2, false, windowCornerRadius);
    }

    public WindowAnimationSpec(Animation animation, Point position, Rect stackBounds, boolean canSkipFirstFrame, int stackClipMode, boolean isAppAnimation, float windowCornerRadius) {
        this.mPosition = new Point();
        this.mThreadLocalTmps = ThreadLocal.withInitial($$Lambda$WindowAnimationSpec$jKE7Phq2DESkeBondpaNPBLn6Cs.INSTANCE);
        this.mStackBounds = new Rect();
        this.mTmpRect = new Rect();
        this.mStartRadius = 0.0f;
        this.mEndRadius = 0.0f;
        this.mScaleSetting = 0.0f;
        this.mAnimationTime = 0;
        this.mAnimaitonStartDelay = 0;
        this.mIsDynamicCornerRadius = false;
        this.mDynamicCornerCurveParams = null;
        this.mAnimation = animation;
        if (position != null) {
            this.mPosition.set(position.x, position.y);
        }
        this.mWindowCornerRadius = windowCornerRadius;
        this.mCanSkipFirstFrame = canSkipFirstFrame;
        this.mIsAppAnimation = isAppAnimation;
        this.mStackClipMode = stackClipMode;
        if (stackBounds != null) {
            this.mStackBounds.set(stackBounds);
        }
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public boolean getShowWallpaper() {
        return this.mAnimation.getShowWallpaper();
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public int getBackgroundColor() {
        return this.mAnimation.getBackgroundColor();
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public long getDuration() {
        return this.mAnimation.computeDurationHint();
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public void apply(SurfaceControl.Transaction t, SurfaceControl leash, long currentPlayTime) {
        TmpValues tmp = this.mThreadLocalTmps.get();
        tmp.transformation.clear();
        this.mAnimation.getTransformation(currentPlayTime, tmp.transformation);
        tmp.transformation.getMatrix().postTranslate((float) this.mPosition.x, (float) this.mPosition.y);
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.d(TAG, "Surface is " + leash + ", and position is (" + tmp.floats[2] + ", " + tmp.floats[5] + ")");
        }
        t.setMatrix(leash, tmp.transformation.getMatrix(), tmp.floats);
        t.setAlpha(leash, tmp.transformation.getAlpha());
        boolean cropSet = false;
        if (this.mStackClipMode != 2) {
            this.mTmpRect.set(this.mStackBounds);
            if (tmp.transformation.hasClipRect()) {
                this.mTmpRect.intersect(tmp.transformation.getClipRect());
            }
            t.setWindowCrop(leash, this.mTmpRect);
            cropSet = true;
        } else if (tmp.transformation.hasClipRect()) {
            t.setWindowCrop(leash, tmp.transformation.getClipRect());
            cropSet = true;
        }
        if (cropSet && this.mAnimation.hasRoundedCorners()) {
            float f = this.mWindowCornerRadius;
            if (f > 0.0f) {
                t.setCornerRadius(leash, f);
            }
        }
        applyDynamicCornerRadius(t, leash, currentPlayTime);
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public long calculateStatusBarTransitionStartTime() {
        TranslateAnimation openTranslateAnimation = findTranslateAnimation(this.mAnimation);
        if (openTranslateAnimation == null) {
            return SystemClock.uptimeMillis();
        }
        return ((SystemClock.uptimeMillis() + openTranslateAnimation.getStartOffset()) + ((long) (((float) openTranslateAnimation.getDuration()) * findAlmostThereFraction(openTranslateAnimation.getInterpolator())))) - 120;
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public boolean canSkipFirstFrame() {
        return this.mCanSkipFirstFrame;
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public boolean needsEarlyWakeup() {
        return this.mIsAppAnimation;
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println(this.mAnimation);
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public void writeToProtoInner(ProtoOutputStream proto) {
        long token = proto.start(1146756268033L);
        proto.write(1138166333441L, this.mAnimation.toString());
        proto.end(token);
    }

    private static TranslateAnimation findTranslateAnimation(Animation animation) {
        if (animation instanceof TranslateAnimation) {
            return (TranslateAnimation) animation;
        }
        if (!(animation instanceof AnimationSet)) {
            return null;
        }
        AnimationSet set = (AnimationSet) animation;
        for (int i = 0; i < set.getAnimations().size(); i++) {
            Animation a = set.getAnimations().get(i);
            if (a instanceof TranslateAnimation) {
                return (TranslateAnimation) a;
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

    /* access modifiers changed from: private */
    public static class TmpValues {
        final float[] floats;
        final Transformation transformation;

        private TmpValues() {
            this.transformation = new Transformation();
            this.floats = new float[9];
        }
    }

    public void setDynamicCornerRadiusInfo(Interpolator interpolator, float startRadius, float endRadius, float scaleSetting, long animationTime, long startDelay) {
        if (interpolator != null) {
            this.mCornerRadiusInterpolator = interpolator;
        }
        this.mIsDynamicCornerRadius = true;
        this.mStartRadius = startRadius;
        this.mEndRadius = endRadius;
        this.mScaleSetting = scaleSetting;
        this.mAnimationTime = animationTime;
        this.mAnimaitonStartDelay = startDelay;
        this.mDynamicCornerCurveParams = null;
    }

    public void setDynamicCornerCurveParams(SurfaceControl.CornerCurveParams curveParams) {
        this.mDynamicCornerCurveParams = curveParams;
    }

    private void applyDynamicCornerRadius(SurfaceControl.Transaction t, SurfaceControl leash, long currentPlayTime) {
        if (this.mCornerRadiusInterpolator != null && this.mIsDynamicCornerRadius && Float.compare(this.mScaleSetting, 0.0f) != 0 && Float.compare((float) this.mAnimationTime, 0.0f) != 0 && Float.compare((float) currentPlayTime, ((float) this.mAnimaitonStartDelay) * this.mScaleSetting) >= 0) {
            Interpolator interpolator = this.mCornerRadiusInterpolator;
            float f = this.mScaleSetting;
            float progress = interpolator.getInterpolation((((float) currentPlayTime) - (((float) this.mAnimaitonStartDelay) * f)) / (f * ((float) this.mAnimationTime)));
            SurfaceControl.CornerCurveParams cornerCurveParams = this.mDynamicCornerCurveParams;
            if (cornerCurveParams != null) {
                t.setCornerCurveParams(leash, cornerCurveParams);
            }
            float f2 = this.mStartRadius;
            t.setCornerRadius(leash, f2 + ((this.mEndRadius - f2) * progress));
        }
    }
}
