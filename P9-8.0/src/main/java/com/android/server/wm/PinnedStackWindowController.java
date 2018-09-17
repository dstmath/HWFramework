package com.android.server.wm;

import android.app.RemoteAction;
import android.graphics.Rect;
import android.util.HwPCUtils;
import java.util.List;

public class PinnedStackWindowController extends StackWindowController {
    private static final String TAG = "PinnedStackWindowController";
    private Rect mTmpFromBounds = new Rect();
    private Rect mTmpToBounds = new Rect();

    public PinnedStackWindowController(int stackId, PinnedStackWindowListener listener, int displayId, boolean onTop, Rect outBounds) {
        super(stackId, listener, displayId, onTop, outBounds, WindowManagerService.getInstance());
    }

    public Rect getPictureInPictureBounds(float aspectRatio, Rect stackBounds) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (!this.mService.mSupportsPictureInPicture || this.mContainer == null) {
                } else if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                    HwPCUtils.log(TAG, "ignore getPictureInPictureBounds in pad pc mode");
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return null;
                } else {
                    DisplayContent displayContent = ((TaskStack) this.mContainer).getDisplayContent();
                    if (displayContent == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    }
                    PinnedStackController pinnedStackController = displayContent.getPinnedStackController();
                    if (stackBounds == null) {
                        stackBounds = pinnedStackController.getDefaultBounds();
                    }
                    if (pinnedStackController.isValidPictureInPictureAspectRatio(aspectRatio)) {
                        Rect transformBoundsToAspectRatio = pinnedStackController.transformBoundsToAspectRatio(stackBounds, aspectRatio, true);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return transformBoundsToAspectRatio;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return stackBounds;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return null;
    }

    public void animateResizePinnedStack(Rect toBounds, Rect sourceHintBounds, int animationDuration, boolean fromFullscreen) {
        Throwable th;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    throw new IllegalArgumentException("Pinned stack container not found :(");
                } else if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                    HwPCUtils.log(TAG, "ignore animateResizePinnedStack in pad pc mode");
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else {
                    Rect fromBounds = new Rect();
                    ((TaskStack) this.mContainer).getBounds(fromBounds);
                    int schedulePipModeChangedState = 0;
                    boolean toFullscreen = toBounds == null;
                    if (toFullscreen) {
                        if (fromFullscreen) {
                            throw new IllegalArgumentException("Should not defer scheduling PiP mode change on animation to fullscreen.");
                        }
                        schedulePipModeChangedState = 1;
                        this.mService.getStackBounds(1, this.mTmpToBounds);
                        if (this.mTmpToBounds.isEmpty()) {
                            Rect toBounds2 = new Rect();
                            try {
                                ((TaskStack) this.mContainer).getDisplayContent().getLogicalDisplayRect(toBounds2);
                                toBounds = toBounds2;
                            } catch (Throwable th2) {
                                th = th2;
                                toBounds = toBounds2;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                        toBounds = new Rect(this.mTmpToBounds);
                    } else if (fromFullscreen) {
                        schedulePipModeChangedState = 2;
                    }
                    ((TaskStack) this.mContainer).setAnimationFinalBounds(sourceHintBounds, toBounds, toFullscreen);
                    this.mService.mBoundsAnimationController.getHandler().post(new -$Lambda$Dd9IZYP_DnuZN905KeMl4-pzcAs(fromFullscreen, toFullscreen, animationDuration, schedulePipModeChangedState, this, fromBounds, toBounds));
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } catch (Throwable th3) {
                th = th3;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_PinnedStackWindowController_6221(Rect fromBounds, Rect finalToBounds, int animationDuration, int finalSchedulePipModeChangedState, boolean fromFullscreen, boolean toFullscreen) {
        if (this.mContainer != null) {
            this.mService.mBoundsAnimationController.animateBounds((BoundsAnimationTarget) this.mContainer, fromBounds, finalToBounds, animationDuration, finalSchedulePipModeChangedState, fromFullscreen, toFullscreen);
        }
    }

    /* JADX WARNING: Missing block: B:28:0x0076, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:29:0x0079, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPictureInPictureAspectRatio(float aspectRatio) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (!this.mService.mSupportsPictureInPicture || this.mContainer == null) {
                } else if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                    HwPCUtils.log(TAG, "ignore setPictureInPictureActions in pad pc mode");
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else {
                    PinnedStackController pinnedStackController = ((TaskStack) this.mContainer).getDisplayContent().getPinnedStackController();
                    if (Float.compare(aspectRatio, pinnedStackController.getAspectRatio()) != 0) {
                        ((TaskStack) this.mContainer).getAnimationOrCurrentBounds(this.mTmpFromBounds);
                        this.mTmpToBounds.set(this.mTmpFromBounds);
                        getPictureInPictureBounds(aspectRatio, this.mTmpToBounds);
                        if (!this.mTmpToBounds.equals(this.mTmpFromBounds)) {
                            animateResizePinnedStack(this.mTmpToBounds, null, -1, false);
                        }
                        if (!pinnedStackController.isValidPictureInPictureAspectRatio(aspectRatio)) {
                            aspectRatio = -1.0f;
                        }
                        pinnedStackController.setAspectRatio(aspectRatio);
                    }
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setPictureInPictureActions(List<RemoteAction> actions) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (!this.mService.mSupportsPictureInPicture || this.mContainer == null) {
                } else if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
                    HwPCUtils.log(TAG, "ignore getPictureInPictureActions in pad pc mode");
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else {
                    ((TaskStack) this.mContainer).getDisplayContent().getPinnedStackController().setActions(actions);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean deferScheduleMultiWindowModeChanged() {
        boolean deferScheduleMultiWindowModeChanged;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                deferScheduleMultiWindowModeChanged = ((TaskStack) this.mContainer).deferScheduleMultiWindowModeChanged();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return deferScheduleMultiWindowModeChanged;
    }

    public boolean isAnimatingBoundsToFullscreen() {
        boolean isAnimatingBoundsToFullscreen;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                isAnimatingBoundsToFullscreen = ((TaskStack) this.mContainer).isAnimatingBoundsToFullscreen();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return isAnimatingBoundsToFullscreen;
    }

    public boolean pinnedStackResizeDisallowed() {
        boolean pinnedStackResizeDisallowed;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                pinnedStackResizeDisallowed = ((TaskStack) this.mContainer).pinnedStackResizeDisallowed();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return pinnedStackResizeDisallowed;
    }

    public void updatePictureInPictureModeForPinnedStackAnimation(Rect targetStackBounds) {
        if (this.mListener != null) {
            this.mListener.updatePictureInPictureModeForPinnedStackAnimation(targetStackBounds);
        }
    }
}
