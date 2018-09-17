package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.view.DisplayInfo;
import com.android.server.os.HwBootFail;
import java.lang.ref.WeakReference;

public class StackWindowController extends WindowContainerController<TaskStack, StackWindowListener> {
    private final H mHandler;
    final int mStackId;
    private final Rect mTmpDisplayBounds;
    private final Rect mTmpNonDecorInsets;
    private final Rect mTmpRect;
    private final Rect mTmpStableInsets;

    private static final class H extends Handler {
        static final int REQUEST_RESIZE = 0;
        private final WeakReference<StackWindowController> mController;

        H(WeakReference<StackWindowController> controller, Looper looper) {
            super(looper);
            this.mController = controller;
        }

        public void handleMessage(Message msg) {
            StackWindowController controller = (StackWindowController) this.mController.get();
            StackWindowListener listener = controller != null ? (StackWindowListener) controller.mListener : null;
            if (listener != null) {
                switch (msg.what) {
                    case 0:
                        listener.requestResize((Rect) msg.obj);
                        break;
                }
            }
        }
    }

    public StackWindowController(int stackId, StackWindowListener listener, int displayId, boolean onTop, Rect outBounds) {
        this(stackId, listener, displayId, onTop, outBounds, WindowManagerService.getInstance());
    }

    public StackWindowController(int stackId, StackWindowListener listener, int displayId, boolean onTop, Rect outBounds, WindowManagerService service) {
        super(listener, service);
        this.mTmpRect = new Rect();
        this.mTmpStableInsets = new Rect();
        this.mTmpNonDecorInsets = new Rect();
        this.mTmpDisplayBounds = new Rect();
        this.mStackId = stackId;
        this.mHandler = new H(new WeakReference(this), service.mH.getLooper());
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                DisplayContent dc = this.mRoot.getDisplayContent(displayId);
                if (dc == null) {
                    throw new IllegalArgumentException("Trying to add stackId=" + stackId + " to unknown displayId=" + displayId);
                }
                dc.addStackToDisplay(stackId, onTop).setController(this);
                getRawBounds(outBounds);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void removeContainer() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((TaskStack) this.mContainer).removeIfPossible();
                    super.removeContainer();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isVisible() {
        boolean isVisible;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                isVisible = this.mContainer != null ? ((TaskStack) this.mContainer).isVisible() : false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return isVisible;
    }

    public void reparent(int displayId, Rect outStackBounds, boolean onTop) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    throw new IllegalArgumentException("Trying to move unknown stackId=" + this.mStackId + " to displayId=" + displayId);
                }
                DisplayContent targetDc = this.mRoot.getDisplayContent(displayId);
                if (targetDc == null) {
                    throw new IllegalArgumentException("Trying to move stackId=" + this.mStackId + " to unknown displayId=" + displayId);
                }
                targetDc.moveStackToDisplay((TaskStack) this.mContainer, onTop);
                getRawBounds(outStackBounds);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void positionChildAt(TaskWindowContainerController child, int position, Rect bounds, Configuration overrideConfig) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (child.mContainer == null) {
                } else if (this.mContainer == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else {
                    ((Task) child.mContainer).positionAt(position, bounds, overrideConfig);
                    ((TaskStack) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void positionChildAtTop(TaskWindowContainerController child, boolean includingParents) {
        if (child != null) {
            synchronized (this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    Task childTask = child.mContainer;
                    if (childTask == null) {
                        Slog.e("WindowManager", "positionChildAtTop: task=" + child + " not found");
                    } else {
                        ((TaskStack) this.mContainer).positionChildAt((int) HwBootFail.STAGE_BOOT_SUCCESS, childTask, includingParents);
                        if (this.mService.mAppTransition.isTransitionSet()) {
                            childTask.setSendingToBottom(false);
                        }
                        ((TaskStack) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public void positionChildAtBottom(TaskWindowContainerController child) {
        if (child != null) {
            synchronized (this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    Task childTask = child.mContainer;
                    if (childTask == null) {
                        Slog.e("WindowManager", "positionChildAtBottom: task=" + child + " not found");
                    } else {
                        ((TaskStack) this.mContainer).positionChildAt(Integer.MIN_VALUE, childTask, false);
                        if (this.mService.mAppTransition.isTransitionSet()) {
                            childTask.setSendingToBottom(true);
                        }
                        ((TaskStack) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public boolean resize(Rect bounds, SparseArray<Configuration> configs, SparseArray<Rect> taskBounds, SparseArray<Rect> taskTempInsetBounds) {
        boolean rawFullscreen;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    throw new IllegalArgumentException("resizeStack: stack " + this + " not found.");
                }
                ((TaskStack) this.mContainer).prepareFreezingTaskBounds();
                if (((TaskStack) this.mContainer).setBounds(bounds, configs, taskBounds, taskTempInsetBounds) && ((TaskStack) this.mContainer).isVisible()) {
                    ((TaskStack) this.mContainer).getDisplayContent().setLayoutNeeded();
                    this.mService.mWindowPlacerLocked.performSurfacePlacement();
                }
                rawFullscreen = ((TaskStack) this.mContainer).getRawFullscreen();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return rawFullscreen;
    }

    public void getStackDockedModeBounds(Rect currentTempTaskBounds, Rect outStackBounds, Rect outTempTaskBounds, boolean ignoreVisibility) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((TaskStack) this.mContainer).getStackDockedModeBoundsLocked(currentTempTaskBounds, outStackBounds, outTempTaskBounds, ignoreVisibility);
                } else {
                    outStackBounds.setEmpty();
                    outTempTaskBounds.setEmpty();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void prepareFreezingTaskBounds() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    throw new IllegalArgumentException("prepareFreezingTaskBounds: stack " + this + " not found.");
                }
                ((TaskStack) this.mContainer).prepareFreezingTaskBounds();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void getRawBounds(Rect outBounds) {
        if (((TaskStack) this.mContainer).getRawFullscreen()) {
            outBounds.setEmpty();
        } else {
            ((TaskStack) this.mContainer).getRawBounds(outBounds);
        }
    }

    public void getBounds(Rect outBounds) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((TaskStack) this.mContainer).getBounds(outBounds);
                } else {
                    outBounds.setEmpty();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void getBoundsForNewConfiguration(Rect outBounds) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ((TaskStack) this.mContainer).getBoundsForNewConfiguration(outBounds);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void adjustConfigurationForBounds(Rect bounds, Rect insetBounds, Rect nonDecorBounds, Rect stableBounds, boolean overrideWidth, boolean overrideHeight, float density, Configuration config, Configuration parentConfig) {
        synchronized (this.mWindowMap) {
            int width;
            int height;
            WindowManagerService.boostPriorityForLockedSection();
            DisplayContent displayContent = this.mContainer.getDisplayContent();
            DisplayInfo di = displayContent.getDisplayInfo();
            if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayContent.getDisplayId())) {
                this.mService.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, this.mTmpStableInsets, displayContent.getDisplayId());
                this.mService.mPolicy.getNonDecorInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, this.mTmpNonDecorInsets, displayContent.getDisplayId());
            } else {
                try {
                    this.mService.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, this.mTmpStableInsets);
                    this.mService.mPolicy.getNonDecorInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, this.mTmpNonDecorInsets);
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            this.mTmpDisplayBounds.set(0, 0, di.logicalWidth, di.logicalHeight);
            Rect parentAppBounds = parentConfig.appBounds;
            config.setAppBounds(!bounds.isEmpty() ? bounds : null);
            boolean intersectParentBounds = false;
            if (StackId.tasksAreFloating(this.mStackId)) {
                if (this.mStackId == 4 && bounds.width() == this.mTmpDisplayBounds.width() && bounds.height() == this.mTmpDisplayBounds.height()) {
                    stableBounds.inset(this.mTmpStableInsets);
                    nonDecorBounds.inset(this.mTmpNonDecorInsets);
                    config.appBounds.offsetTo(0, 0);
                    intersectParentBounds = true;
                }
                width = (int) (((float) stableBounds.width()) / density);
                height = (int) (((float) stableBounds.height()) / density);
            } else {
                Rect rect;
                if (insetBounds != null) {
                    rect = insetBounds;
                } else {
                    rect = bounds;
                }
                intersectDisplayBoundsExcludeInsets(nonDecorBounds, rect, this.mTmpNonDecorInsets, this.mTmpDisplayBounds, overrideWidth, overrideHeight);
                if (insetBounds != null) {
                    rect = insetBounds;
                } else {
                    rect = bounds;
                }
                intersectDisplayBoundsExcludeInsets(stableBounds, rect, this.mTmpStableInsets, this.mTmpDisplayBounds, overrideWidth, overrideHeight);
                width = Math.min((int) (((float) stableBounds.width()) / density), parentConfig.screenWidthDp);
                height = Math.min((int) (((float) stableBounds.height()) / density), parentConfig.screenHeightDp);
                intersectParentBounds = true;
            }
            if (intersectParentBounds && config.appBounds != null) {
                config.appBounds.intersect(parentAppBounds);
            }
            config.screenWidthDp = width;
            config.screenHeightDp = height;
            if (insetBounds == null) {
                insetBounds = bounds;
            }
            config.smallestScreenWidthDp = getSmallestWidthForTaskBounds(insetBounds, density);
        }
    }

    private void intersectDisplayBoundsExcludeInsets(Rect inOutBounds, Rect inInsetBounds, Rect stableInsets, Rect displayBounds, boolean overrideWidth, boolean overrideHeight) {
        this.mTmpRect.set(inInsetBounds);
        this.mService.intersectDisplayInsetBounds(displayBounds, stableInsets, this.mTmpRect);
        inOutBounds.inset(this.mTmpRect.left - inInsetBounds.left, this.mTmpRect.top - inInsetBounds.top, overrideWidth ? 0 : inInsetBounds.right - this.mTmpRect.right, overrideHeight ? 0 : inInsetBounds.bottom - this.mTmpRect.bottom);
    }

    private int getSmallestWidthForTaskBounds(Rect bounds, float density) {
        DisplayContent displayContent = ((TaskStack) this.mContainer).getDisplayContent();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        if (bounds == null || (bounds.width() == displayInfo.logicalWidth && bounds.height() == displayInfo.logicalHeight)) {
            return displayContent.getConfiguration().smallestScreenWidthDp;
        }
        if (StackId.tasksAreFloating(this.mStackId)) {
            return (int) (((float) Math.min(bounds.width(), bounds.height())) / density);
        }
        return displayContent.getDockedDividerController().getSmallestWidthDpForBounds(bounds);
    }

    void requestResize(Rect bounds) {
        this.mHandler.obtainMessage(0, bounds).sendToTarget();
    }

    public String toString() {
        return "{StackWindowController stackId=" + this.mStackId + "}";
    }

    public void resetBounds() {
        if (this.mContainer != null) {
            ((TaskStack) this.mContainer).setBounds(null);
        }
    }

    public void clearTempInsetBounds() {
        if (this.mContainer != null) {
            ((TaskStack) this.mContainer).clearTempInsetBounds();
        }
    }
}
