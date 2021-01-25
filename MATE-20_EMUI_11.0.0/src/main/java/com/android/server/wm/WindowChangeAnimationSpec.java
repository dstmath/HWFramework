package com.android.server.wm;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ClipRectAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import com.android.server.wm.LocalAnimationAdapter;
import java.io.PrintWriter;

public class WindowChangeAnimationSpec implements LocalAnimationAdapter.AnimationSpec {
    static final int ANIMATION_DURATION = 200;
    private Animation mAnimation;
    private final Rect mEndBounds;
    private final boolean mIsAppAnimation;
    private final boolean mIsThumbnail;
    private final Rect mStartBounds;
    private final ThreadLocal<TmpValues> mThreadLocalTmps = ThreadLocal.withInitial($$Lambda$WindowChangeAnimationSpec$J5jIvng4nctFR8T6L2f_W3o1KU.INSTANCE);
    private final Rect mTmpRect = new Rect();

    static /* synthetic */ TmpValues lambda$new$0() {
        return new TmpValues();
    }

    public WindowChangeAnimationSpec(Rect startBounds, Rect endBounds, DisplayInfo displayInfo, float durationScale, boolean isAppAnimation, boolean isThumbnail) {
        this.mStartBounds = new Rect(startBounds);
        this.mEndBounds = new Rect(endBounds);
        this.mIsAppAnimation = isAppAnimation;
        this.mIsThumbnail = isThumbnail;
        createBoundsInterpolator((long) ((int) (200.0f * durationScale)), displayInfo);
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public boolean getShowWallpaper() {
        return false;
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public int getBackgroundColor() {
        return 0;
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public long getDuration() {
        return this.mAnimation.getDuration();
    }

    private void createBoundsInterpolator(long duration, DisplayInfo displayInfo) {
        boolean growing = ((this.mEndBounds.width() - this.mStartBounds.width()) + this.mEndBounds.height()) - this.mStartBounds.height() >= 0;
        long scalePeriod = (long) (((float) duration) * 0.7f);
        float startScaleX = ((((float) this.mStartBounds.width()) * 0.7f) / ((float) this.mEndBounds.width())) + (1.0f - 0.7f);
        float startScaleY = ((((float) this.mStartBounds.height()) * 0.7f) / ((float) this.mEndBounds.height())) + (1.0f - 0.7f);
        if (this.mIsThumbnail) {
            AnimationSet animSet = new AnimationSet(true);
            Animation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(scalePeriod);
            if (!growing) {
                anim.setStartOffset(duration - scalePeriod);
            }
            animSet.addAnimation(anim);
            float endScaleX = 1.0f / startScaleX;
            float endScaleY = 1.0f / startScaleY;
            Animation anim2 = new ScaleAnimation(endScaleX, endScaleX, endScaleY, endScaleY);
            anim2.setDuration(duration);
            animSet.addAnimation(anim2);
            this.mAnimation = animSet;
            this.mAnimation.initialize(this.mStartBounds.width(), this.mStartBounds.height(), this.mEndBounds.width(), this.mEndBounds.height());
            return;
        }
        AnimationSet animSet2 = new AnimationSet(true);
        Animation scaleAnim = new ScaleAnimation(startScaleX, 1.0f, startScaleY, 1.0f);
        scaleAnim.setDuration(scalePeriod);
        if (!growing) {
            scaleAnim.setStartOffset(duration - scalePeriod);
        }
        animSet2.addAnimation(scaleAnim);
        Animation translateAnim = new TranslateAnimation((float) this.mStartBounds.left, (float) this.mEndBounds.left, (float) this.mStartBounds.top, (float) this.mEndBounds.top);
        translateAnim.setDuration(duration);
        animSet2.addAnimation(translateAnim);
        Rect startClip = new Rect(this.mStartBounds);
        Rect endClip = new Rect(this.mEndBounds);
        startClip.offsetTo(0, 0);
        endClip.offsetTo(0, 0);
        Animation clipAnim = new ClipRectAnimation(startClip, endClip);
        clipAnim.setDuration(duration);
        animSet2.addAnimation(clipAnim);
        this.mAnimation = animSet2;
        this.mAnimation.initialize(this.mStartBounds.width(), this.mStartBounds.height(), displayInfo.appWidth, displayInfo.appHeight);
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public void apply(SurfaceControl.Transaction t, SurfaceControl leash, long currentPlayTime) {
        TmpValues tmp = this.mThreadLocalTmps.get();
        if (this.mIsThumbnail) {
            this.mAnimation.getTransformation(currentPlayTime, tmp.mTransformation);
            t.setMatrix(leash, tmp.mTransformation.getMatrix(), tmp.mFloats);
            t.setAlpha(leash, tmp.mTransformation.getAlpha());
            return;
        }
        this.mAnimation.getTransformation(currentPlayTime, tmp.mTransformation);
        Matrix matrix = tmp.mTransformation.getMatrix();
        t.setMatrix(leash, matrix, tmp.mFloats);
        float[] fArr = tmp.mVecs;
        tmp.mVecs[2] = 0.0f;
        fArr[1] = 0.0f;
        float[] fArr2 = tmp.mVecs;
        tmp.mVecs[3] = 1.0f;
        fArr2[0] = 1.0f;
        matrix.mapVectors(tmp.mVecs);
        tmp.mVecs[0] = 1.0f / tmp.mVecs[0];
        tmp.mVecs[3] = 1.0f / tmp.mVecs[3];
        Rect clipRect = tmp.mTransformation.getClipRect();
        this.mTmpRect.left = (int) ((((float) clipRect.left) * tmp.mVecs[0]) + 0.5f);
        this.mTmpRect.right = (int) ((((float) clipRect.right) * tmp.mVecs[0]) + 0.5f);
        this.mTmpRect.top = (int) ((((float) clipRect.top) * tmp.mVecs[3]) + 0.5f);
        this.mTmpRect.bottom = (int) ((((float) clipRect.bottom) * tmp.mVecs[3]) + 0.5f);
        t.setWindowCrop(leash, this.mTmpRect);
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public long calculateStatusBarTransitionStartTime() {
        long uptime = SystemClock.uptimeMillis();
        return Math.max(uptime, (((long) (((float) this.mAnimation.getDuration()) * 0.99f)) + uptime) - 120);
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public boolean canSkipFirstFrame() {
        return false;
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public boolean needsEarlyWakeup() {
        return this.mIsAppAnimation;
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println(this.mAnimation.getDuration());
    }

    @Override // com.android.server.wm.LocalAnimationAdapter.AnimationSpec
    public void writeToProtoInner(ProtoOutputStream proto) {
        long token = proto.start(1146756268033L);
        proto.write(1138166333441L, this.mAnimation.toString());
        proto.end(token);
    }

    /* access modifiers changed from: private */
    public static class TmpValues {
        final float[] mFloats;
        final Transformation mTransformation;
        final float[] mVecs;

        private TmpValues() {
            this.mTransformation = new Transformation();
            this.mFloats = new float[9];
            this.mVecs = new float[4];
        }
    }
}
