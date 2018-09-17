package com.android.server.wm;

import android.util.Slog;
import android.util.TimeUtils;
import android.view.Choreographer;
import android.view.SurfaceControl;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.job.controllers.JobStatus;
import java.io.PrintWriter;
import java.util.ArrayList;

public class AppWindowAnimator {
    static final int PROLONG_ANIMATION_AT_END = 1;
    static final int PROLONG_ANIMATION_AT_START = 2;
    private static final int PROLONG_ANIMATION_DISABLED = 0;
    static final String TAG = "WindowManager";
    static final Animation sDummyAnimation = new DummyAnimation();
    boolean allDrawn;
    int animLayerAdjustment;
    boolean animating;
    Animation animation;
    boolean deferFinalFrameCleanup;
    boolean deferThumbnailDestruction;
    boolean freezingScreen;
    boolean hasTransformation;
    int lastFreezeDuration;
    ArrayList<WindowStateAnimator> mAllAppWinAnimators = new ArrayList();
    final WindowAnimator mAnimator;
    final AppWindowToken mAppToken;
    private boolean mClearProlongedAnimation;
    private int mProlongAnimation;
    final WindowManagerService mService;
    private boolean mSkipFirstFrame = false;
    private int mStackClip = 1;
    private int mTransit;
    private int mTransitFlags;
    SurfaceControl thumbnail;
    Animation thumbnailAnimation;
    int thumbnailForceAboveLayer;
    int thumbnailLayer;
    int thumbnailTransactionSeq;
    final Transformation thumbnailTransformation = new Transformation();
    final Transformation transformation = new Transformation();
    boolean usingTransferredAnimation = false;
    boolean wasAnimating;

    static final class DummyAnimation extends Animation {
        DummyAnimation() {
        }

        public boolean getTransformation(long currentTime, Transformation outTransformation) {
            return false;
        }
    }

    public AppWindowAnimator(AppWindowToken atoken, WindowManagerService service) {
        this.mAppToken = atoken;
        this.mService = service;
        this.mAnimator = this.mService.mAnimator;
    }

    public void setAnimation(Animation anim, int width, int height, int parentWidth, int parentHeight, boolean skipFirstFrame, int stackClip, int transit, int transitFlags) {
        this.animation = anim;
        this.animating = false;
        if (!anim.isInitialized()) {
            anim.initialize(width, height, parentWidth, parentHeight);
        }
        anim.restrictDuration(JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        anim.scaleCurrentDuration(this.mService.getTransitionAnimationScaleLocked());
        int zorder = anim.getZAdjustment();
        int adj = 0;
        if (zorder == 1) {
            adj = 1000;
        } else if (zorder == -1) {
            adj = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
        }
        if (this.animLayerAdjustment != adj) {
            this.animLayerAdjustment = adj;
            updateLayers();
        }
        this.transformation.clear();
        this.transformation.setAlpha((float) (this.mAppToken.hasContentToDisplay() ? 1 : 0));
        this.hasTransformation = true;
        this.mStackClip = stackClip;
        this.mSkipFirstFrame = skipFirstFrame;
        this.mTransit = transit;
        this.mTransitFlags = transitFlags;
        if (!this.mAppToken.fillsParent()) {
            anim.setBackgroundColor(0);
        }
        if (this.mClearProlongedAnimation) {
            this.mProlongAnimation = 0;
        } else {
            this.mClearProlongedAnimation = true;
        }
    }

    public void setDummyAnimation() {
        int i = 1;
        this.animation = sDummyAnimation;
        this.hasTransformation = true;
        this.transformation.clear();
        Transformation transformation = this.transformation;
        if (!this.mAppToken.hasContentToDisplay()) {
            i = 0;
        }
        transformation.setAlpha((float) i);
    }

    void setNullAnimation() {
        this.animation = null;
        this.usingTransferredAnimation = false;
    }

    public void clearAnimation() {
        if (this.animation != null) {
            this.animating = true;
        }
        clearThumbnail();
        setNullAnimation();
        if (this.mAppToken.deferClearAllDrawn) {
            this.mAppToken.clearAllDrawn();
        }
        this.mStackClip = 1;
        this.mTransit = -1;
        this.mTransitFlags = 0;
    }

    public boolean isAnimating() {
        return this.animation == null ? this.mAppToken.inPendingTransaction : true;
    }

    public int getTransit() {
        return this.mTransit;
    }

    int getTransitFlags() {
        return this.mTransitFlags;
    }

    public void clearThumbnail() {
        if (this.thumbnail != null) {
            this.thumbnail.hide();
            this.mService.mWindowPlacerLocked.destroyAfterTransaction(this.thumbnail);
            this.thumbnail = null;
        }
        this.deferThumbnailDestruction = false;
    }

    int getStackClip() {
        return this.mStackClip;
    }

    void transferCurrentAnimation(AppWindowAnimator toAppAnimator, WindowStateAnimator transferWinAnimator) {
        if (this.animation != null) {
            toAppAnimator.animation = this.animation;
            toAppAnimator.animating = this.animating;
            toAppAnimator.animLayerAdjustment = this.animLayerAdjustment;
            setNullAnimation();
            this.animLayerAdjustment = 0;
            toAppAnimator.updateLayers();
            updateLayers();
            toAppAnimator.usingTransferredAnimation = true;
            toAppAnimator.mTransit = this.mTransit;
        }
        if (transferWinAnimator != null) {
            this.mAllAppWinAnimators.remove(transferWinAnimator);
            toAppAnimator.mAllAppWinAnimators.add(transferWinAnimator);
            toAppAnimator.hasTransformation = transferWinAnimator.mAppAnimator.hasTransformation;
            if (toAppAnimator.hasTransformation) {
                toAppAnimator.transformation.set(transferWinAnimator.mAppAnimator.transformation);
            } else {
                toAppAnimator.transformation.clear();
            }
            transferWinAnimator.mAppAnimator = toAppAnimator;
        }
    }

    private void updateLayers() {
        this.mAppToken.getDisplayContent().assignWindowLayers(false);
        this.thumbnailLayer = this.mAppToken.getHighestAnimLayer();
    }

    private void stepThumbnailAnimation(long currentTime) {
        boolean screenAnimation;
        this.thumbnailTransformation.clear();
        this.thumbnailAnimation.getTransformation(getAnimationFrameTime(this.thumbnailAnimation, currentTime), this.thumbnailTransformation);
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(0);
        if (screenRotationAnimation != null) {
            screenAnimation = screenRotationAnimation.isAnimating();
        } else {
            screenAnimation = false;
        }
        if (screenAnimation) {
            this.thumbnailTransformation.postCompose(screenRotationAnimation.getEnterTransformation());
        }
        float[] tmpFloats = this.mService.mTmpFloats;
        this.thumbnailTransformation.getMatrix().getValues(tmpFloats);
        this.thumbnail.setPosition(tmpFloats[2], tmpFloats[5]);
        this.thumbnail.setAlpha(this.thumbnailTransformation.getAlpha());
        if (this.thumbnailForceAboveLayer > 0) {
            this.thumbnail.setLayer(this.thumbnailForceAboveLayer + 1);
        } else {
            this.thumbnail.setLayer((this.thumbnailLayer + 5) - 4);
        }
        this.thumbnail.setMatrix(tmpFloats[0], tmpFloats[3], tmpFloats[1], tmpFloats[4]);
        this.thumbnail.setWindowCrop(this.thumbnailTransformation.getClipRect());
    }

    private long getAnimationFrameTime(Animation animation, long currentTime) {
        if (this.mProlongAnimation != 2) {
            return currentTime;
        }
        animation.setStartTime(currentTime);
        return 1 + currentTime;
    }

    private boolean stepAnimation(long currentTime) {
        if (this.animation == null) {
            return false;
        }
        this.transformation.clear();
        boolean hasMoreFrames = this.animation.getTransformation(getAnimationFrameTime(this.animation, currentTime), this.transformation);
        if (!hasMoreFrames) {
            if (!this.deferThumbnailDestruction || (this.deferFinalFrameCleanup ^ 1) == 0) {
                this.deferFinalFrameCleanup = false;
                if (this.mProlongAnimation == 1) {
                    hasMoreFrames = true;
                } else {
                    setNullAnimation();
                    clearThumbnail();
                }
            } else {
                this.deferFinalFrameCleanup = true;
                hasMoreFrames = true;
            }
        }
        this.hasTransformation = hasMoreFrames;
        return hasMoreFrames;
    }

    private long getStartTimeCorrection() {
        if (!this.mSkipFirstFrame) {
            return 0;
        }
        try {
            return (-Choreographer.getInstance().getFrameIntervalNanos()) / 1000000;
        } catch (IllegalStateException e) {
            Slog.v(TAG, "Can't get Looper return 0");
            return 0;
        }
    }

    boolean stepAnimationLocked(long currentTime) {
        if (this.mService.okToDisplay()) {
            if (this.animation == sDummyAnimation) {
                return false;
            }
            if ((this.mAppToken.allDrawn || this.animating || this.mAppToken.startingDisplayed) && this.animation != null) {
                if (!this.animating) {
                    long correction = getStartTimeCorrection();
                    this.animation.setStartTime(currentTime + correction);
                    this.animating = true;
                    if (this.thumbnail != null) {
                        this.thumbnail.show();
                        this.thumbnailAnimation.setStartTime(currentTime + correction);
                    }
                    this.mSkipFirstFrame = false;
                }
                if (stepAnimation(currentTime)) {
                    if (this.thumbnail != null) {
                        stepThumbnailAnimation(currentTime);
                    }
                    return true;
                }
            }
        } else if (this.animation != null) {
            this.animating = true;
            this.animation = null;
        }
        this.hasTransformation = false;
        if (!this.animating && this.animation == null) {
            return false;
        }
        this.mAppToken.setAppLayoutChanges(8, "AppWindowToken");
        clearAnimation();
        this.animating = false;
        if (this.animLayerAdjustment != 0) {
            this.animLayerAdjustment = 0;
            updateLayers();
        }
        if (this.mService.mInputMethodTarget != null && this.mService.mInputMethodTarget.mAppToken == this.mAppToken) {
            this.mAppToken.getDisplayContent().computeImeTarget(true);
        }
        this.transformation.clear();
        int numAllAppWinAnimators = this.mAllAppWinAnimators.size();
        for (int i = 0; i < numAllAppWinAnimators; i++) {
            ((WindowStateAnimator) this.mAllAppWinAnimators.get(i)).mWin.onExitAnimationDone();
        }
        this.mService.mAppTransition.notifyAppTransitionFinishedLocked(this.mAppToken.token);
        return false;
    }

    boolean showAllWindowsLocked() {
        boolean isAnimating = false;
        int NW = this.mAllAppWinAnimators.size();
        for (int i = 0; i < NW; i++) {
            WindowStateAnimator winAnimator = (WindowStateAnimator) this.mAllAppWinAnimators.get(i);
            winAnimator.mWin.performShowLocked();
            isAnimating |= winAnimator.isAnimationSet();
        }
        return isAnimating;
    }

    void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        pw.print(prefix);
        pw.print("mAppToken=");
        pw.println(this.mAppToken);
        pw.print(prefix);
        pw.print("mAnimator=");
        pw.println(this.mAnimator);
        pw.print(prefix);
        pw.print("freezingScreen=");
        pw.print(this.freezingScreen);
        pw.print(" allDrawn=");
        pw.print(this.allDrawn);
        pw.print(" animLayerAdjustment=");
        pw.println(this.animLayerAdjustment);
        if (this.lastFreezeDuration != 0) {
            pw.print(prefix);
            pw.print("lastFreezeDuration=");
            TimeUtils.formatDuration((long) this.lastFreezeDuration, pw);
            pw.println();
        }
        if (this.animating || this.animation != null) {
            pw.print(prefix);
            pw.print("animating=");
            pw.println(this.animating);
            pw.print(prefix);
            pw.print("animation=");
            pw.println(this.animation);
            pw.print(prefix);
            pw.print("mTransit=");
            pw.println(this.mTransit);
            pw.print(prefix);
            pw.print("mTransitFlags=");
            pw.println(this.mTransitFlags);
        }
        if (this.hasTransformation) {
            pw.print(prefix);
            pw.print("XForm: ");
            this.transformation.printShortString(pw);
            pw.println();
        }
        if (this.thumbnail != null) {
            pw.print(prefix);
            pw.print("thumbnail=");
            pw.print(this.thumbnail);
            pw.print(" layer=");
            pw.println(this.thumbnailLayer);
            pw.print(prefix);
            pw.print("thumbnailAnimation=");
            pw.println(this.thumbnailAnimation);
            pw.print(prefix);
            pw.print("thumbnailTransformation=");
            pw.println(this.thumbnailTransformation.toShortString());
        }
        for (int i = 0; i < this.mAllAppWinAnimators.size(); i++) {
            WindowStateAnimator wanim = (WindowStateAnimator) this.mAllAppWinAnimators.get(i);
            pw.print(prefix);
            pw.print("App Win Anim #");
            pw.print(i);
            pw.print(": ");
            pw.println(wanim);
        }
    }

    void startProlongAnimation(int prolongType) {
        this.mProlongAnimation = prolongType;
        this.mClearProlongedAnimation = false;
    }

    void endProlongedAnimation() {
        this.mProlongAnimation = 0;
    }
}
