package com.android.server.wm;

import android.util.Slog;
import android.util.TimeUtils;
import android.view.Choreographer;
import android.view.SurfaceControl;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import com.android.server.am.ProcessList;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.job.controllers.JobStatus;
import java.io.PrintWriter;
import java.util.ArrayList;

public class AppWindowAnimator {
    static final int PROLONG_ANIMATION_AT_END = 1;
    static final int PROLONG_ANIMATION_AT_START = 2;
    private static final int PROLONG_ANIMATION_DISABLED = 0;
    static final String TAG = null;
    static final Animation sDummyAnimation = null;
    boolean allDrawn;
    int animLayerAdjustment;
    boolean animating;
    Animation animation;
    boolean deferFinalFrameCleanup;
    boolean deferThumbnailDestruction;
    boolean freezingScreen;
    boolean hasTransformation;
    int lastFreezeDuration;
    ArrayList<WindowStateAnimator> mAllAppWinAnimators;
    final WindowAnimator mAnimator;
    final AppWindowToken mAppToken;
    private boolean mClearProlongedAnimation;
    private int mProlongAnimation;
    final WindowManagerService mService;
    private boolean mSkipFirstFrame;
    private int mStackClip;
    SurfaceControl thumbnail;
    Animation thumbnailAnimation;
    int thumbnailForceAboveLayer;
    int thumbnailLayer;
    int thumbnailTransactionSeq;
    final Transformation thumbnailTransformation;
    final Transformation transformation;
    boolean usingTransferredAnimation;
    boolean wasAnimating;

    static final class DummyAnimation extends Animation {
        DummyAnimation() {
        }

        public boolean getTransformation(long currentTime, Transformation outTransformation) {
            return false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.AppWindowAnimator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.AppWindowAnimator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.AppWindowAnimator.<clinit>():void");
    }

    public AppWindowAnimator(AppWindowToken atoken) {
        this.transformation = new Transformation();
        this.thumbnailTransformation = new Transformation();
        this.mAllAppWinAnimators = new ArrayList();
        this.usingTransferredAnimation = false;
        this.mSkipFirstFrame = false;
        this.mStackClip = PROLONG_ANIMATION_AT_END;
        this.mAppToken = atoken;
        this.mService = atoken.service;
        this.mAnimator = this.mService.mAnimator;
    }

    public void setAnimation(Animation anim, int width, int height, boolean skipFirstFrame, int stackClip) {
        int i;
        this.animation = anim;
        this.animating = false;
        if (!anim.isInitialized()) {
            anim.initialize(width, height, width, height);
        }
        anim.restrictDuration(JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        anim.scaleCurrentDuration(this.mService.getTransitionAnimationScaleLocked());
        int zorder = anim.getZAdjustment();
        int adj = 0;
        if (zorder == PROLONG_ANIMATION_AT_END) {
            adj = ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE;
        } else if (zorder == -1) {
            adj = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
        }
        if (this.animLayerAdjustment != adj) {
            this.animLayerAdjustment = adj;
            updateLayers();
        }
        this.transformation.clear();
        Transformation transformation = this.transformation;
        if (this.mAppToken.isVisible()) {
            i = PROLONG_ANIMATION_AT_END;
        } else {
            i = 0;
        }
        transformation.setAlpha((float) i);
        this.hasTransformation = true;
        this.mStackClip = stackClip;
        this.mSkipFirstFrame = skipFirstFrame;
        if (!this.mAppToken.appFullscreen) {
            anim.setBackgroundColor(0);
        }
        if (this.mClearProlongedAnimation) {
            this.mProlongAnimation = 0;
        } else {
            this.mClearProlongedAnimation = true;
        }
        for (int i2 = this.mAppToken.allAppWindows.size() - 1; i2 >= 0; i2--) {
            ((WindowState) this.mAppToken.allAppWindows.get(i2)).resetJustMovedInStack();
        }
    }

    public void setDummyAnimation() {
        int i = PROLONG_ANIMATION_AT_END;
        this.animation = sDummyAnimation;
        this.hasTransformation = true;
        this.transformation.clear();
        Transformation transformation = this.transformation;
        if (!this.mAppToken.isVisible()) {
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
        this.mStackClip = PROLONG_ANIMATION_AT_END;
    }

    public boolean isAnimating() {
        return this.animation == null ? this.mAppToken.inPendingTransaction : true;
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

    void updateLayers() {
        int windowCount = this.mAppToken.allAppWindows.size();
        int adj = this.animLayerAdjustment;
        this.thumbnailLayer = -1;
        WallpaperController wallpaperController = this.mService.mWallpaperControllerLocked;
        for (int i = 0; i < windowCount; i += PROLONG_ANIMATION_AT_END) {
            WindowState w = (WindowState) this.mAppToken.allAppWindows.get(i);
            WindowStateAnimator winAnimator = w.mWinAnimator;
            winAnimator.mAnimLayer = w.mLayer + adj;
            if (winAnimator.mAnimLayer > this.thumbnailLayer) {
                this.thumbnailLayer = winAnimator.mAnimLayer;
            }
            if (w == this.mService.mInputMethodTarget && !this.mService.mInputMethodTargetWaitingAnim) {
                this.mService.mLayersController.setInputMethodAnimLayerAdjustment(adj);
            }
            wallpaperController.setAnimLayerAdjustment(w, adj);
        }
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
        this.thumbnail.setPosition(tmpFloats[PROLONG_ANIMATION_AT_START], tmpFloats[5]);
        this.thumbnail.setAlpha(this.thumbnailTransformation.getAlpha());
        if (this.thumbnailForceAboveLayer > 0) {
            this.thumbnail.setLayer(this.thumbnailForceAboveLayer + PROLONG_ANIMATION_AT_END);
        } else {
            this.thumbnail.setLayer((this.thumbnailLayer + 5) - 4);
        }
        this.thumbnail.setMatrix(tmpFloats[0], tmpFloats[3], tmpFloats[PROLONG_ANIMATION_AT_END], tmpFloats[4]);
        this.thumbnail.setWindowCrop(this.thumbnailTransformation.getClipRect());
    }

    private long getAnimationFrameTime(Animation animation, long currentTime) {
        if (this.mProlongAnimation != PROLONG_ANIMATION_AT_START) {
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
            if (!this.deferThumbnailDestruction || this.deferFinalFrameCleanup) {
                this.deferFinalFrameCleanup = false;
                if (this.mProlongAnimation == PROLONG_ANIMATION_AT_END) {
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

    boolean stepAnimationLocked(long currentTime, int displayId) {
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
        this.mAnimator.setAppLayoutChanges(this, 8, "AppWindowToken", displayId);
        clearAnimation();
        this.animating = false;
        if (this.animLayerAdjustment != 0) {
            this.animLayerAdjustment = 0;
            updateLayers();
        }
        if (this.mService.mInputMethodTarget != null && this.mService.mInputMethodTarget.mAppToken == this.mAppToken) {
            this.mService.moveInputMethodWindowsIfNeededLocked(true);
        }
        this.transformation.clear();
        int numAllAppWinAnimators = this.mAllAppWinAnimators.size();
        for (int i = 0; i < numAllAppWinAnimators; i += PROLONG_ANIMATION_AT_END) {
            ((WindowStateAnimator) this.mAllAppWinAnimators.get(i)).finishExit();
        }
        this.mService.mAppTransition.notifyAppTransitionFinishedLocked(this.mAppToken.token);
        return false;
    }

    boolean showAllWindowsLocked() {
        boolean isAnimating = false;
        int NW = this.mAllAppWinAnimators.size();
        for (int i = 0; i < NW; i += PROLONG_ANIMATION_AT_END) {
            WindowStateAnimator winAnimator = (WindowStateAnimator) this.mAllAppWinAnimators.get(i);
            winAnimator.performShowLocked();
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
        for (int i = 0; i < this.mAllAppWinAnimators.size(); i += PROLONG_ANIMATION_AT_END) {
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
