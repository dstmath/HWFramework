package com.android.server.wm;

import android.app.RemoteAction;
import android.graphics.Rect;
import android.os.Handler;
import android.util.HwPCUtils;
import java.util.List;

public class PinnedStackWindowController extends StackWindowController {
    private static final String TAG = "PinnedStackWindowController";
    private Rect mTmpFromBounds = new Rect();
    private Rect mTmpToBounds = new Rect();

    public PinnedStackWindowController(int stackId, PinnedStackWindowListener listener, int displayId, boolean onTop, Rect outBounds, WindowManagerService service) {
        super(stackId, listener, displayId, onTop, outBounds, service);
    }

    public Rect getPictureInPictureBounds(float aspectRatio, Rect stackBounds) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mService.mSupportsPictureInPicture) {
                    if (this.mContainer != null) {
                        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) {
                            DisplayContent displayContent = ((TaskStack) this.mContainer).getDisplayContent();
                            if (displayContent == null) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return null;
                            }
                            PinnedStackController pinnedStackController = displayContent.getPinnedStackController();
                            if (stackBounds == null) {
                                stackBounds = pinnedStackController.getDefaultOrLastSavedBounds();
                            }
                            if (pinnedStackController.isValidPictureInPictureAspectRatio(aspectRatio)) {
                                Rect transformBoundsToAspectRatio = pinnedStackController.transformBoundsToAspectRatio(stackBounds, aspectRatio, true);
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return transformBoundsToAspectRatio;
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return stackBounds;
                        }
                        HwPCUtils.log(TAG, "ignore getPictureInPictureBounds in pad pc mode");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return null;
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return null;
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void animateResizePinnedStack(Rect toBounds, Rect sourceHintBounds, int animationDuration, boolean fromFullscreen) {
        int schedulePipModeChangedState;
        Rect toBounds2;
        Rect toBounds3;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    if (HwPCUtils.enabledInPad()) {
                        try {
                            if (HwPCUtils.isPcCastModeInServer()) {
                                HwPCUtils.log(TAG, "ignore animateResizePinnedStack in pad pc mode");
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                        } catch (Throwable th) {
                            th = th;
                            Rect rect = toBounds;
                            Rect rect2 = sourceHintBounds;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                    Rect fromBounds = new Rect();
                    ((TaskStack) this.mContainer).getBounds(fromBounds);
                    int schedulePipModeChangedState2 = 0;
                    boolean toFullscreen = toBounds == null;
                    if (!toFullscreen) {
                        if (fromFullscreen) {
                            schedulePipModeChangedState2 = 2;
                        }
                        toBounds2 = toBounds;
                        schedulePipModeChangedState = schedulePipModeChangedState2;
                    } else if (!fromFullscreen) {
                        this.mService.getStackBounds(1, 1, this.mTmpToBounds);
                        if (!this.mTmpToBounds.isEmpty()) {
                            toBounds3 = new Rect(this.mTmpToBounds);
                        } else {
                            toBounds3 = new Rect();
                            try {
                                ((TaskStack) this.mContainer).getDisplayContent().getBounds(toBounds3);
                            } catch (Throwable th3) {
                                th = th3;
                                Rect rect3 = sourceHintBounds;
                                Rect rect4 = toBounds3;
                            }
                        }
                        schedulePipModeChangedState = 1;
                        toBounds2 = toBounds3;
                    } else {
                        throw new IllegalArgumentException("Should not defer scheduling PiP mode change on animation to fullscreen.");
                    }
                    try {
                        try {
                            ((TaskStack) this.mContainer).setAnimationFinalBounds(sourceHintBounds, toBounds2, toFullscreen);
                            Rect rect5 = fromBounds;
                            $$Lambda$PinnedStackWindowController$x7R9b0MaS9BJmenirckXpBNyg r0 = r1;
                            Handler handler = this.mService.mBoundsAnimationController.getHandler();
                            $$Lambda$PinnedStackWindowController$x7R9b0MaS9BJmenirckXpBNyg r1 = new Runnable(fromBounds, toBounds2, animationDuration, schedulePipModeChangedState, fromFullscreen, toFullscreen) {
                                private final /* synthetic */ Rect f$1;
                                private final /* synthetic */ Rect f$2;
                                private final /* synthetic */ int f$3;
                                private final /* synthetic */ int f$4;
                                private final /* synthetic */ boolean f$5;
                                private final /* synthetic */ boolean f$6;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                    this.f$3 = r4;
                                    this.f$4 = r5;
                                    this.f$5 = r6;
                                    this.f$6 = r7;
                                }

                                public final void run() {
                                    PinnedStackWindowController.lambda$animateResizePinnedStack$0(PinnedStackWindowController.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
                                }
                            };
                            handler.post(r0);
                            WindowManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th4) {
                            th = th4;
                            while (true) {
                                break;
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        Rect rect6 = sourceHintBounds;
                        while (true) {
                            break;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                } else {
                    Rect rect7 = sourceHintBounds;
                    try {
                        throw new IllegalArgumentException("Pinned stack container not found :(");
                    } catch (Throwable th6) {
                        th = th6;
                        while (true) {
                            break;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } catch (Throwable th7) {
                th = th7;
                Rect rect8 = sourceHintBounds;
                while (true) {
                    break;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    public static /* synthetic */ void lambda$animateResizePinnedStack$0(PinnedStackWindowController pinnedStackWindowController, Rect fromBounds, Rect finalToBounds, int animationDuration, int finalSchedulePipModeChangedState, boolean fromFullscreen, boolean toFullscreen) {
        if (pinnedStackWindowController.mContainer != null) {
            pinnedStackWindowController.mService.mBoundsAnimationController.animateBounds((BoundsAnimationTarget) pinnedStackWindowController.mContainer, fromBounds, finalToBounds, animationDuration, finalSchedulePipModeChangedState, fromFullscreen, toFullscreen);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0076, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0079, code lost:
        return;
     */
    public void setPictureInPictureAspectRatio(float aspectRatio) {
        float f;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mService.mSupportsPictureInPicture) {
                    if (this.mContainer != null) {
                        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) {
                            PinnedStackController pinnedStackController = ((TaskStack) this.mContainer).getDisplayContent().getPinnedStackController();
                            if (Float.compare(aspectRatio, pinnedStackController.getAspectRatio()) != 0) {
                                ((TaskStack) this.mContainer).getAnimationOrCurrentBounds(this.mTmpFromBounds);
                                this.mTmpToBounds.set(this.mTmpFromBounds);
                                getPictureInPictureBounds(aspectRatio, this.mTmpToBounds);
                                if (!this.mTmpToBounds.equals(this.mTmpFromBounds)) {
                                    animateResizePinnedStack(this.mTmpToBounds, null, -1, false);
                                }
                                if (pinnedStackController.isValidPictureInPictureAspectRatio(aspectRatio)) {
                                    f = aspectRatio;
                                } else {
                                    f = -1.0f;
                                }
                                pinnedStackController.setAspectRatio(f);
                            }
                        } else {
                            HwPCUtils.log(TAG, "ignore setPictureInPictureActions in pad pc mode");
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void setPictureInPictureActions(List<RemoteAction> actions) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mService.mSupportsPictureInPicture) {
                    if (this.mContainer != null) {
                        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) {
                            ((TaskStack) this.mContainer).getDisplayContent().getPinnedStackController().setActions(actions);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                        HwPCUtils.log(TAG, "ignore getPictureInPictureActions in pad pc mode");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public boolean deferScheduleMultiWindowModeChanged() {
        boolean deferScheduleMultiWindowModeChanged;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                deferScheduleMultiWindowModeChanged = ((TaskStack) this.mContainer).deferScheduleMultiWindowModeChanged();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return deferScheduleMultiWindowModeChanged;
    }

    public boolean isAnimatingBoundsToFullscreen() {
        boolean isAnimatingBoundsToFullscreen;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                isAnimatingBoundsToFullscreen = ((TaskStack) this.mContainer).isAnimatingBoundsToFullscreen();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return isAnimatingBoundsToFullscreen;
    }

    public boolean pinnedStackResizeDisallowed() {
        boolean pinnedStackResizeDisallowed;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                pinnedStackResizeDisallowed = ((TaskStack) this.mContainer).pinnedStackResizeDisallowed();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return pinnedStackResizeDisallowed;
    }

    public void updatePictureInPictureModeForPinnedStackAnimation(Rect targetStackBounds, boolean forceUpdate) {
        if (this.mListener != null) {
            ((PinnedStackWindowListener) this.mListener).updatePictureInPictureModeForPinnedStackAnimation(targetStackBounds, forceUpdate);
        }
    }
}
