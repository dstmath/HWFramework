package com.android.server.wm;

import android.graphics.Rect;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import com.android.server.wm.LocalAnimationAdapter;
import java.io.PrintWriter;

public class TvWindowChangeAnimationSpec implements LocalAnimationAdapter.AnimationSpec {
    private static final float DURATION_RATIO = 0.99f;
    private static final float HALF_RATIO = 0.5f;
    private static final String TAG = "TvWindowChangeAnimationSpec";
    private static final int TV_ANIMATION_DURATION = AppTransition.DEFAULT_APP_TRANSITION_DURATION;
    private static final int TV_FLOAT_ARRAY_SIZE = 9;
    private static final int TV_VECTOR_ARRAY_SIZE = 4;
    private float mCornerRadius = 0.0f;
    private boolean mIsGrowing;
    private final boolean mIsTvAppAnimation;
    private final boolean mIsTvThumbnail;
    private Animation mTvAnimation;
    private final Rect mTvEndBounds;
    private final Rect mTvStartBounds;
    private final ThreadLocal<TvTmpValues> mTvThreadLocalTmps = ThreadLocal.withInitial($$Lambda$TvWindowChangeAnimationSpec$qD_uKOPPy4CA3wWbXQJk7xRL0Q.INSTANCE);

    static /* synthetic */ TvTmpValues lambda$new$0() {
        return new TvTmpValues();
    }

    public TvWindowChangeAnimationSpec(Rect startBounds, Rect endBounds, DisplayInfo displayInfo, float durationScale, boolean isAppAnimation, boolean isThumbnail) {
        this.mTvStartBounds = new Rect(startBounds);
        this.mTvEndBounds = new Rect(endBounds);
        this.mIsTvAppAnimation = isAppAnimation;
        this.mIsTvThumbnail = isThumbnail;
        createTvBoundsInterpolator((long) ((int) (((float) TV_ANIMATION_DURATION) * durationScale)), displayInfo);
    }

    public boolean getShowWallpaper() {
        return false;
    }

    public int getBackgroundColor() {
        return 0;
    }

    public long getDuration() {
        return this.mTvAnimation.getDuration();
    }

    private void createTvBoundsInterpolator(long duration, DisplayInfo displayInfo) {
        AnimationSet animationSet;
        this.mIsGrowing = ((this.mTvEndBounds.width() - this.mTvStartBounds.width()) + this.mTvEndBounds.height()) - this.mTvStartBounds.height() >= 0;
        Slog.d(TAG, "createTvBoundsInterpolator, mTvStartBounds = " + this.mTvStartBounds + ", mTvEndBounds = " + this.mTvEndBounds);
        if (this.mIsTvThumbnail) {
            animationSet = createTvThumbnailAnimation(duration);
        } else {
            animationSet = createTvAppAnimation(duration);
        }
        if (animationSet != null) {
            this.mTvAnimation = animationSet;
            this.mTvAnimation.initialize(this.mTvStartBounds.width(), this.mTvStartBounds.height(), displayInfo.appWidth, displayInfo.appHeight);
        }
    }

    private AnimationSet createTvThumbnailAnimation(long duration) {
        Animation translateAnimation;
        AnimationSet animationSet = new AnimationSet(true);
        if (this.mTvEndBounds.height() == 0 || this.mTvEndBounds.width() == 0 || this.mTvStartBounds.height() == 0 || this.mTvStartBounds.width() == 0) {
            Slog.d(TAG, "createTvThumbnailAnimation: variable bounds is zero");
            return animationSet;
        }
        animationSet.setInterpolator(HwWmConstants.FRICTION_CURVE_INTERPOLATOR);
        float startScaleX = ((float) this.mTvStartBounds.width()) / (((float) this.mTvEndBounds.height()) * (((float) this.mTvStartBounds.width()) / ((float) this.mTvStartBounds.height())));
        float startScaleY = ((float) this.mTvStartBounds.height()) / ((float) this.mTvEndBounds.height());
        Animation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration((long) (((float) duration) * 0.5f));
        animationSet.addAnimation(animation);
        float endScaleY = 1.0f / startScaleY;
        Animation animation2 = new ScaleAnimation(1.0f, 1.0f / startScaleX, 1.0f, endScaleY);
        animation2.setDuration(duration);
        animationSet.addAnimation(animation2);
        if (this.mIsGrowing) {
            translateAnimation = new TranslateAnimation((float) this.mTvStartBounds.left, (((float) this.mTvEndBounds.width()) - (((float) this.mTvStartBounds.width()) * endScaleY)) * 0.5f, (float) this.mTvStartBounds.top, (float) this.mTvEndBounds.top);
        } else {
            translateAnimation = new TranslateAnimation((float) this.mTvStartBounds.left, ((float) this.mTvEndBounds.left) - (((((float) this.mTvStartBounds.width()) / startScaleX) * 0.5f) - (((float) this.mTvEndBounds.width()) * 0.5f)), (float) this.mTvStartBounds.top, (float) this.mTvEndBounds.top);
        }
        translateAnimation.setDuration(duration);
        animationSet.addAnimation(translateAnimation);
        return animationSet;
    }

    private AnimationSet createTvAppAnimation(long duration) {
        AnimationSet animationSet = new AnimationSet(true);
        if (this.mTvEndBounds.height() == 0 || this.mTvEndBounds.width() == 0 || this.mTvStartBounds.height() == 0 || this.mTvStartBounds.width() == 0) {
            Slog.d(TAG, "createTvAppAnimation: variable bounds is zero");
            return animationSet;
        }
        animationSet.setInterpolator(HwWmConstants.FRICTION_CURVE_INTERPOLATOR);
        if (this.mIsGrowing) {
            float startScaleX = ((float) this.mTvStartBounds.width()) / ((((float) this.mTvStartBounds.width()) / ((float) this.mTvStartBounds.height())) * ((float) this.mTvEndBounds.height()));
            Animation scaleAnimation = new ScaleAnimation(startScaleX, 1.0f, ((float) this.mTvStartBounds.height()) / ((float) this.mTvEndBounds.height()), 1.0f);
            scaleAnimation.setDuration(duration);
            animationSet.addAnimation(scaleAnimation);
            Animation translateAnimation = new TranslateAnimation((float) ((int) (((float) this.mTvStartBounds.left) - (((((float) this.mTvEndBounds.width()) * startScaleX) * 0.5f) - (((float) this.mTvStartBounds.width()) * 0.5f)))), (float) this.mTvEndBounds.left, (float) this.mTvStartBounds.top, (float) this.mTvEndBounds.top);
            translateAnimation.setDuration(duration);
            animationSet.addAnimation(translateAnimation);
        } else {
            float contentWidth = (float) ((int) ((((float) this.mTvEndBounds.width()) / ((float) this.mTvEndBounds.height())) * ((float) this.mTvStartBounds.height())));
            Animation scaleAnimation2 = new ScaleAnimation(contentWidth / ((float) this.mTvEndBounds.width()), 1.0f, ((float) this.mTvStartBounds.height()) / ((float) this.mTvEndBounds.height()), 1.0f);
            scaleAnimation2.setDuration(duration);
            animationSet.addAnimation(scaleAnimation2);
            int translateX = (int) ((((float) this.mTvStartBounds.width()) - contentWidth) * 0.5f);
            Animation translateAnimation2 = new TranslateAnimation((float) translateX, (float) this.mTvEndBounds.left, (float) this.mTvStartBounds.top, (float) this.mTvEndBounds.top);
            Slog.d(TAG, "createTvAppAnimation: app length = " + (translateX - this.mTvEndBounds.left));
            translateAnimation2.setDuration(duration);
            animationSet.addAnimation(translateAnimation2);
        }
        return animationSet;
    }

    public long calculateStatusBarTransitionStartTime() {
        if (this.mTvAnimation == null) {
            Slog.d(TAG, "calculateStatusBarTransitionStartTime mTvAnimation is not init");
            return 0;
        }
        long uptime = SystemClock.uptimeMillis();
        return Math.max(uptime, (((long) (((float) this.mTvAnimation.getDuration()) * DURATION_RATIO)) + uptime) - 120);
    }

    public boolean canSkipFirstFrame() {
        return false;
    }

    public boolean needsEarlyWakeup() {
        return this.mIsTvAppAnimation;
    }

    public void dump(PrintWriter printWriter, String prefixTv) {
        if (printWriter == null) {
            Slog.e(TAG, "dump: printWriter is null");
            return;
        }
        printWriter.print(prefixTv);
        printWriter.println(this.mTvAnimation.getDuration());
    }

    public void writeToProtoInner(ProtoOutputStream protoOutputStream) {
        if (protoOutputStream == null || this.mTvAnimation == null) {
            Slog.e(TAG, "writeToProtoInner: protoOutputStream or mTvAnimation is null");
            return;
        }
        long tvToken = protoOutputStream.start(1146756268033L);
        protoOutputStream.write(1138166333441L, this.mTvAnimation.toString());
        protoOutputStream.end(tvToken);
    }

    public void apply(SurfaceControl.Transaction transaction, SurfaceControl leash, long currentPlayTime) {
        if (transaction == null || leash == null || this.mTvAnimation == null) {
            Slog.d(TAG, "tv apply: transaction | leash | mTvAnimation is null");
            return;
        }
        TvTmpValues tmp = this.mTvThreadLocalTmps.get();
        if (this.mIsTvThumbnail) {
            Trace.traceBegin(1, "thumbnail draw: " + tmp.mTvTransformation.getMatrix());
            this.mTvAnimation.getTransformation(currentPlayTime, tmp.mTvTransformation);
            transaction.setMatrix(leash, tmp.mTvTransformation.getMatrix(), tmp.mTvFloats);
            transaction.setAlpha(leash, tmp.mTvTransformation.getAlpha());
            if (this.mIsGrowing) {
                transaction.setCornerRadius(leash, this.mCornerRadius);
            }
            Trace.traceEnd(1);
            return;
        }
        Trace.traceBegin(1, "app draw: " + tmp.mTvTransformation.getMatrix());
        this.mTvAnimation.getTransformation(currentPlayTime, tmp.mTvTransformation);
        transaction.setMatrix(leash, tmp.mTvTransformation.getMatrix(), tmp.mTvFloats);
        transaction.setAlpha(leash, tmp.mTvTransformation.getAlpha());
        Trace.traceEnd(1);
    }

    /* access modifiers changed from: private */
    public static class TvTmpValues {
        final float[] mTvFloats;
        final Transformation mTvTransformation;
        final float[] mVectors;

        private TvTmpValues() {
            this.mTvTransformation = new Transformation();
            this.mTvFloats = new float[9];
            this.mVectors = new float[4];
        }
    }

    public void setWindowCornerRadius(float radius) {
        this.mCornerRadius = radius;
    }
}
