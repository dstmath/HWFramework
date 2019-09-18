package com.android.server.wm;

import android.app.WindowConfiguration;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.os.HwBootFail;
import java.lang.ref.WeakReference;

public class StackWindowController extends WindowContainerController<TaskStack, StackWindowListener> {
    private final H mHandler;
    private final int mStackId;
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
            if (listener != null && msg.what == 0) {
                listener.requestResize((Rect) msg.obj);
            }
        }
    }

    public /* bridge */ /* synthetic */ void onOverrideConfigurationChanged(Configuration configuration) {
        super.onOverrideConfigurationChanged(configuration);
    }

    public StackWindowController(int stackId, StackWindowListener listener, int displayId, boolean onTop, Rect outBounds) {
        this(stackId, listener, displayId, onTop, outBounds, WindowManagerService.getInstance());
    }

    @VisibleForTesting
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
                if (dc != null) {
                    dc.createStack(stackId, onTop, this);
                    getRawBounds(outBounds);
                } else {
                    throw new IllegalArgumentException("Trying to add stackId=" + stackId + " to unknown displayId=" + displayId);
                }
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
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void reparent(int displayId, Rect outStackBounds, boolean onTop) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    DisplayContent targetDc = this.mRoot.getDisplayContent(displayId);
                    if (targetDc != null) {
                        targetDc.moveStackToDisplay((TaskStack) this.mContainer, onTop);
                        getRawBounds(outStackBounds);
                    } else {
                        throw new IllegalArgumentException("Trying to move stackId=" + this.mStackId + " to unknown displayId=" + displayId);
                    }
                } else {
                    throw new IllegalArgumentException("Trying to move unknown stackId=" + this.mStackId + " to displayId=" + displayId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void positionChildAt(TaskWindowContainerController child, int position) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (child.mContainer == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else if (this.mContainer == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else {
                    ((Task) child.mContainer).positionAt(position);
                    ((TaskStack) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void positionChildAtTop(TaskWindowContainerController child, boolean includingParents) {
        if (child != null) {
            synchronized (this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    Task childTask = (Task) child.mContainer;
                    if (childTask == null) {
                        Slog.e("WindowManager", "positionChildAtTop: task=" + child + " not found");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ((TaskStack) this.mContainer).positionChildAt((int) HwBootFail.STAGE_BOOT_SUCCESS, childTask, includingParents);
                    if (this.mService.mAppTransition.isTransitionSet()) {
                        childTask.setSendingToBottom(false);
                    }
                    ((TaskStack) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                    WindowManagerService.resetPriorityAfterLockedSection();
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
        }
    }

    public void positionChildAtBottom(TaskWindowContainerController child, boolean includingParents) {
        if (child != null) {
            synchronized (this.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    Task childTask = (Task) child.mContainer;
                    if (childTask == null) {
                        Slog.e("WindowManager", "positionChildAtBottom: task=" + child + " not found");
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    ((TaskStack) this.mContainer).positionChildAt(Integer.MIN_VALUE, childTask, includingParents);
                    if (this.mService.mAppTransition.isTransitionSet()) {
                        childTask.setSendingToBottom(true);
                    }
                    ((TaskStack) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                    WindowManagerService.resetPriorityAfterLockedSection();
                } catch (Throwable th) {
                    while (true) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
        }
    }

    public void resize(Rect bounds, SparseArray<Rect> taskBounds, SparseArray<Rect> taskTempInsetBounds) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((TaskStack) this.mContainer).prepareFreezingTaskBounds();
                    if (((TaskStack) this.mContainer).setBounds(bounds, taskBounds, taskTempInsetBounds) && ((TaskStack) this.mContainer).isVisible()) {
                        ((TaskStack) this.mContainer).getDisplayContent().setLayoutNeeded();
                        this.mService.mWindowPlacerLocked.performSurfacePlacement();
                    }
                } else {
                    throw new IllegalArgumentException("resizeStack: stack " + this + " not found.");
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onPipAnimationEndResize() {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ((TaskStack) this.mContainer).onPipAnimationEndResize();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void getStackDockedModeBounds(Rect currentTempTaskBounds, Rect outStackBounds, Rect outTempTaskBounds, boolean ignoreVisibility) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((TaskStack) this.mContainer).getStackDockedModeBoundsLocked(currentTempTaskBounds, outStackBounds, outTempTaskBounds, ignoreVisibility);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                outStackBounds.setEmpty();
                outTempTaskBounds.setEmpty();
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void prepareFreezingTaskBounds() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((TaskStack) this.mContainer).prepareFreezingTaskBounds();
                } else {
                    throw new IllegalArgumentException("prepareFreezingTaskBounds: stack " + this + " not found.");
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void getRawBounds(Rect outBounds) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (((TaskStack) this.mContainer).matchParentBounds()) {
                    outBounds.setEmpty();
                } else {
                    ((TaskStack) this.mContainer).getRawBounds(outBounds);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void getBounds(Rect outBounds) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((TaskStack) this.mContainer).getBounds(outBounds);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                outBounds.setEmpty();
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void getBoundsForNewConfiguration(Rect outBounds) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ((TaskStack) this.mContainer).getBoundsForNewConfiguration(outBounds);
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void adjustConfigurationForBounds(Rect bounds, Rect insetBounds, Rect nonDecorBounds, Rect stableBounds, boolean overrideWidth, boolean overrideHeight, float density, Configuration config, Configuration parentConfig, int windowingMode) {
        int height;
        int width;
        Rect parentAppBounds;
        float f = density;
        Configuration configuration = config;
        Configuration configuration2 = parentConfig;
        int i = windowingMode;
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                TaskStack stack = (TaskStack) this.mContainer;
                DisplayContent displayContent = stack.getDisplayContent();
                DisplayInfo di = displayContent.getDisplayInfo();
                DisplayCutout displayCutout = di.displayCutout;
                if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayContent.getDisplayId())) {
                    this.mService.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, displayCutout, this.mTmpStableInsets);
                    this.mService.mPolicy.getNonDecorInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, displayCutout, this.mTmpNonDecorInsets);
                } else {
                    this.mService.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, this.mTmpStableInsets, displayContent.getDisplayId(), displayCutout);
                    this.mService.mPolicy.getNonDecorInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, this.mTmpNonDecorInsets, displayContent.getDisplayId(), displayCutout);
                }
                this.mTmpDisplayBounds.set(0, 0, di.logicalWidth, di.logicalHeight);
                Rect parentAppBounds2 = configuration2.windowConfiguration.getAppBounds();
                Rect rect = bounds;
                configuration.windowConfiguration.setBounds(rect);
                configuration.windowConfiguration.setAppBounds(!bounds.isEmpty() ? rect : null);
                boolean intersectParentBounds = false;
                if (WindowConfiguration.isFloating(windowingMode)) {
                    if (i == 2) {
                        try {
                            if (bounds.width() == this.mTmpDisplayBounds.width() && bounds.height() == this.mTmpDisplayBounds.height()) {
                                stableBounds.inset(this.mTmpStableInsets);
                                nonDecorBounds.inset(this.mTmpNonDecorInsets);
                                configuration.windowConfiguration.getAppBounds().offsetTo(0, 0);
                                intersectParentBounds = true;
                                width = (int) (((float) stableBounds.width()) / f);
                                height = (int) (((float) stableBounds.height()) / f);
                                TaskStack taskStack = stack;
                                parentAppBounds = parentAppBounds2;
                                DisplayInfo displayInfo = di;
                            }
                        } catch (Throwable th) {
                            th = th;
                            Rect rect2 = nonDecorBounds;
                            Rect rect3 = stableBounds;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                    Rect rect4 = nonDecorBounds;
                    Rect rect5 = stableBounds;
                    width = (int) (((float) stableBounds.width()) / f);
                    height = (int) (((float) stableBounds.height()) / f);
                    TaskStack taskStack2 = stack;
                    parentAppBounds = parentAppBounds2;
                    DisplayInfo displayInfo2 = di;
                } else {
                    Rect rect6 = nonDecorBounds;
                    Rect rect7 = stableBounds;
                    TaskStack taskStack3 = stack;
                    parentAppBounds = parentAppBounds2;
                    DisplayInfo displayInfo3 = di;
                    intersectDisplayBoundsExcludeInsets(nonDecorBounds, insetBounds != null ? insetBounds : rect, this.mTmpNonDecorInsets, this.mTmpDisplayBounds, overrideWidth, overrideHeight);
                    intersectDisplayBoundsExcludeInsets(stableBounds, insetBounds != null ? insetBounds : bounds, this.mTmpStableInsets, this.mTmpDisplayBounds, overrideWidth, overrideHeight);
                    width = Math.min((int) (((float) stableBounds.width()) / f), configuration2.screenWidthDp);
                    height = Math.min((int) (((float) stableBounds.height()) / f), configuration2.screenHeightDp);
                    intersectParentBounds = true;
                }
                if (intersectParentBounds && configuration.windowConfiguration.getAppBounds() != null) {
                    configuration.windowConfiguration.getAppBounds().intersect(parentAppBounds);
                }
                configuration.screenWidthDp = width;
                configuration.screenHeightDp = height;
                configuration.smallestScreenWidthDp = getSmallestWidthForTaskBounds(insetBounds != null ? insetBounds : bounds, f, i);
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th2) {
                th = th2;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    private void intersectDisplayBoundsExcludeInsets(Rect inOutBounds, Rect inInsetBounds, Rect stableInsets, Rect displayBounds, boolean overrideWidth, boolean overrideHeight) {
        this.mTmpRect.set(inInsetBounds);
        this.mService.intersectDisplayInsetBounds(displayBounds, stableInsets, this.mTmpRect);
        int leftInset = this.mTmpRect.left - inInsetBounds.left;
        int topInset = this.mTmpRect.top - inInsetBounds.top;
        int bottomInset = 0;
        int rightInset = overrideWidth ? 0 : inInsetBounds.right - this.mTmpRect.right;
        if (!overrideHeight) {
            bottomInset = inInsetBounds.bottom - this.mTmpRect.bottom;
        }
        inOutBounds.inset(leftInset, topInset, rightInset, bottomInset);
    }

    private int getSmallestWidthForTaskBounds(Rect bounds, float density, int windowingMode) {
        DisplayContent displayContent = ((TaskStack) this.mContainer).getDisplayContent();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        if (bounds == null || (bounds.width() == displayInfo.logicalWidth && bounds.height() == displayInfo.logicalHeight)) {
            return displayContent.getConfiguration().smallestScreenWidthDp;
        }
        if (WindowConfiguration.isFloating(windowingMode)) {
            return (int) (((float) Math.min(bounds.width(), bounds.height())) / density);
        }
        return displayContent.getDockedDividerController().getSmallestWidthDpForBounds(bounds);
    }

    /* access modifiers changed from: package-private */
    public void requestResize(Rect bounds) {
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
