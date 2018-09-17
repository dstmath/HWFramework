package com.android.server.wm;

import android.util.Slog;
import com.android.server.power.IHwShutdownThread;
import com.android.server.wm.-$Lambda$rQWNC8oUFFqNI368PlDEO7_YsgQ.AnonymousClass1;
import java.util.ArrayDeque;
import java.util.function.Consumer;

public class WindowLayersController {
    private boolean mAboveImeTarget;
    private ArrayDeque<WindowState> mAboveImeTargetAppWindows = new ArrayDeque();
    private boolean mAnyLayerChanged;
    private final Consumer<WindowState> mAssignWindowLayersConsumer = new AnonymousClass1(this);
    private ArrayDeque<WindowState> mAssistantWindows = new ArrayDeque();
    private int mCurBaseLayer;
    private int mCurLayer;
    private WindowState mDockDivider = null;
    private ArrayDeque<WindowState> mDockedWindows = new ArrayDeque();
    private int mHighestApplicationLayer = 0;
    private int mHighestLayerInImeTargetBaseLayer;
    private WindowState mImeTarget;
    private ArrayDeque<WindowState> mInputMethodWindows = new ArrayDeque();
    private ArrayDeque<WindowState> mPinnedWindows = new ArrayDeque();
    private ArrayDeque<WindowState> mReplacingWindows = new ArrayDeque();
    private final WindowManagerService mService;

    WindowLayersController(WindowManagerService service) {
        this.mService = service;
    }

    /* synthetic */ void lambda$-com_android_server_wm_WindowLayersController_3717(WindowState w) {
        boolean layerChanged = false;
        int oldLayer = w.mLayer;
        if (w.mBaseLayer == this.mCurBaseLayer) {
            this.mCurLayer += 5;
        } else {
            int i = w.mBaseLayer;
            this.mCurLayer = i;
            this.mCurBaseLayer = i;
        }
        assignAnimLayer(w, this.mCurLayer);
        if (!(w.mLayer == oldLayer && w.mWinAnimator.mAnimLayer == oldLayer)) {
            layerChanged = true;
            this.mAnyLayerChanged = true;
        }
        if (w.mAppToken != null) {
            this.mHighestApplicationLayer = Math.max(this.mHighestApplicationLayer, w.mWinAnimator.mAnimLayer);
        }
        if (this.mImeTarget != null && w.mBaseLayer == this.mImeTarget.mBaseLayer) {
            this.mHighestLayerInImeTargetBaseLayer = Math.max(this.mHighestLayerInImeTargetBaseLayer, w.mWinAnimator.mAnimLayer);
        }
        collectSpecialWindows(w);
        if (layerChanged) {
            w.scheduleAnimationIfDimming();
        }
    }

    final void assignWindowLayers(DisplayContent dc) {
        reset();
        dc.forAllWindows(this.mAssignWindowLayersConsumer, false);
        adjustSpecialWindows();
        if (this.mService.mAccessibilityController != null && this.mAnyLayerChanged && dc.getDisplayId() == 0) {
            this.mService.mAccessibilityController.onWindowLayersChangedLocked();
        }
    }

    private void logDebugLayers(DisplayContent dc) {
        dc.forAllWindows((Consumer) new -$Lambda$rQWNC8oUFFqNI368PlDEO7_YsgQ(), false);
    }

    static /* synthetic */ void lambda$-com_android_server_wm_WindowLayersController_5667(WindowState w) {
        Slog.v("WindowManager", "Assign layer " + w + ": " + "mBase=" + w.mBaseLayer + " mLayer=" + w.mLayer + (w.mAppToken == null ? "" : " mAppLayer=" + w.mAppToken.getAnimLayerAdjustment()) + " =mAnimLayer=" + w.mWinAnimator.mAnimLayer);
    }

    private void reset() {
        int i;
        this.mHighestApplicationLayer = 0;
        this.mPinnedWindows.clear();
        this.mInputMethodWindows.clear();
        this.mDockedWindows.clear();
        this.mAssistantWindows.clear();
        this.mReplacingWindows.clear();
        this.mDockDivider = null;
        this.mCurBaseLayer = 0;
        this.mCurLayer = 0;
        this.mAnyLayerChanged = false;
        this.mImeTarget = this.mService.mInputMethodTarget;
        if (this.mImeTarget != null) {
            i = this.mImeTarget.mBaseLayer;
        } else {
            i = 0;
        }
        this.mHighestLayerInImeTargetBaseLayer = i;
        this.mAboveImeTarget = false;
        this.mAboveImeTargetAppWindows.clear();
    }

    private void collectSpecialWindows(WindowState w) {
        if (w.mAttrs.type == 2034) {
            this.mDockDivider = w;
            return;
        }
        if (w.mWillReplaceWindow) {
            this.mReplacingWindows.add(w);
        }
        if (w.mIsImWindow) {
            this.mInputMethodWindows.add(w);
            return;
        }
        if (this.mImeTarget != null) {
            if (w.getParentWindow() == this.mImeTarget && w.mSubLayer > 0) {
                this.mAboveImeTargetAppWindows.add(w);
            } else if (this.mAboveImeTarget && w.mAppToken != null) {
                this.mAboveImeTargetAppWindows.add(w);
            }
            if (w == this.mImeTarget) {
                this.mAboveImeTarget = true;
            }
        }
        Task task = w.getTask();
        if (task != null) {
            TaskStack stack = task.mStack;
            if (stack != null) {
                if (stack.mStackId == 4) {
                    this.mPinnedWindows.add(w);
                } else if (stack.mStackId == 3) {
                    this.mDockedWindows.add(w);
                } else if (stack.mStackId == 6) {
                    this.mAssistantWindows.add(w);
                }
            }
        }
    }

    private void adjustSpecialWindows() {
        int layer = this.mHighestApplicationLayer + 5;
        while (!this.mDockedWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mDockedWindows.remove(), layer);
        }
        layer = assignAndIncreaseLayerIfNeeded(this.mDockDivider, layer);
        while (!this.mReplacingWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mReplacingWindows.remove(), layer);
        }
        while (!this.mAssistantWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mAssistantWindows.remove(), layer);
        }
        while (!this.mPinnedWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mPinnedWindows.remove(), layer);
        }
        if (this.mImeTarget != null) {
            if (this.mImeTarget.mAppToken == null) {
                layer = this.mHighestLayerInImeTargetBaseLayer + 5;
            }
            while (!this.mInputMethodWindows.isEmpty()) {
                layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mInputMethodWindows.remove(), layer);
            }
            while (!this.mAboveImeTargetAppWindows.isEmpty()) {
                layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mAboveImeTargetAppWindows.remove(), layer);
            }
        }
    }

    private int assignAndIncreaseLayerIfNeeded(WindowState win, int layer) {
        if (win == null) {
            return layer;
        }
        int adjustLayer = (win.getAttrs().type != 2034 || (win.getAttrs().flags & 536870912) == 0) ? layer : layer + 1;
        assignAnimLayer(win, adjustLayer);
        return layer + 5;
    }

    private void assignAnimLayer(WindowState w, int layer) {
        w.mLayer = layer;
        w.mWinAnimator.mAnimLayer = w.getAnimLayerAdjustment() + w.getSpecialWindowAnimLayerAdjustment();
        if (w.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && (this.mService.isCoverOpen() ^ 1) != 0) {
            w.mWinAnimator.mAnimLayer = AbsWindowManagerService.TOP_LAYER;
        }
        if (w.mAppToken != null && w.mAppToken.mAppAnimator.thumbnailForceAboveLayer > 0) {
            if (w.mWinAnimator.mAnimLayer > w.mAppToken.mAppAnimator.thumbnailForceAboveLayer) {
                w.mAppToken.mAppAnimator.thumbnailForceAboveLayer = w.mWinAnimator.mAnimLayer;
            }
            int highestLayer = w.mAppToken.getHighestAnimLayer();
            if (highestLayer > 0 && w.mAppToken.mAppAnimator.thumbnail != null && w.mAppToken.mAppAnimator.thumbnailForceAboveLayer != highestLayer) {
                w.mAppToken.mAppAnimator.thumbnailForceAboveLayer = highestLayer;
                w.mAppToken.mAppAnimator.thumbnail.setLayer(highestLayer + 1);
            }
        }
    }
}
