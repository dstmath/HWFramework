package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.app.ActivityManager.TaskDescription;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.DisplayInfo;
import com.android.server.EventLogTags;
import java.io.PrintWriter;
import java.util.function.Consumer;

class Task extends WindowContainer<AppWindowToken> implements DimLayerUser {
    private static final int BOUNDS_CHANGE_NONE = 0;
    private static final int BOUNDS_CHANGE_POSITION = 1;
    private static final int BOUNDS_CHANGE_SIZE = 2;
    static final String TAG = "WindowManager";
    private Rect mBounds = new Rect();
    private boolean mDeferRemoval = false;
    private int mDragResizeMode;
    private boolean mDragResizing;
    private boolean mFillsParent = true;
    private boolean mHomeTask;
    final Rect mPreparedFrozenBounds = new Rect();
    final Configuration mPreparedFrozenMergedConfig = new Configuration();
    private boolean mPreserveNonFloatingState = false;
    private int mResizeMode;
    private int mRotation;
    final WindowManagerService mService;
    TaskStack mStack;
    private boolean mSupportsPictureInPicture;
    private TaskDescription mTaskDescription;
    final int mTaskId;
    private final Rect mTempInsetBounds = new Rect();
    private Rect mTmpRect = new Rect();
    private Rect mTmpRect2 = new Rect();
    final int mUserId;

    Task(int taskId, TaskStack stack, int userId, WindowManagerService service, Rect bounds, Configuration overrideConfig, int resizeMode, boolean supportsPictureInPicture, boolean homeTask, TaskDescription taskDescription, TaskWindowContainerController controller) {
        this.mTaskId = taskId;
        this.mStack = stack;
        this.mUserId = userId;
        this.mService = service;
        this.mResizeMode = resizeMode;
        this.mSupportsPictureInPicture = supportsPictureInPicture;
        this.mHomeTask = homeTask;
        setController(controller);
        setBounds(bounds, overrideConfig);
        this.mTaskDescription = taskDescription;
        setOrientation(-2);
    }

    DisplayContent getDisplayContent() {
        return this.mStack != null ? this.mStack.getDisplayContent() : null;
    }

    private int getAdjustedAddPosition(int suggestedPosition) {
        int size = this.mChildren.size();
        if (suggestedPosition >= size) {
            return Math.min(size, suggestedPosition);
        }
        int pos = 0;
        while (pos < size && pos < suggestedPosition) {
            if (((AppWindowToken) this.mChildren.get(pos)).removed) {
                suggestedPosition++;
            }
            pos++;
        }
        return Math.min(size, suggestedPosition);
    }

    void addChild(AppWindowToken wtoken, int position) {
        super.addChild((WindowContainer) wtoken, getAdjustedAddPosition(position));
        this.mDeferRemoval = false;
    }

    void positionChildAt(int position, AppWindowToken child, boolean includingParents) {
        super.positionChildAt(getAdjustedAddPosition(position), child, includingParents);
        this.mDeferRemoval = false;
    }

    private boolean hasWindowsAlive() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((AppWindowToken) this.mChildren.get(i)).hasWindowsAlive()) {
                return true;
            }
        }
        return false;
    }

    boolean shouldDeferRemoval() {
        return hasWindowsAlive() ? this.mStack.isAnimating() : false;
    }

    void removeIfPossible() {
        if (shouldDeferRemoval()) {
            this.mDeferRemoval = true;
        } else {
            removeImmediately();
        }
    }

    void removeImmediately() {
        EventLog.writeEvent(EventLogTags.WM_TASK_REMOVED, new Object[]{Integer.valueOf(this.mTaskId), "removeTask"});
        this.mDeferRemoval = false;
        DisplayContent content = getDisplayContent();
        if (content != null) {
            content.mDimLayerController.removeDimLayerUser(this);
        }
        super.removeImmediately();
    }

    void reparent(TaskStack stack, int position, boolean moveParents) {
        if (stack == this.mStack) {
            throw new IllegalArgumentException("task=" + this + " already child of stack=" + this.mStack);
        }
        EventLog.writeEvent(EventLogTags.WM_TASK_REMOVED, new Object[]{Integer.valueOf(this.mTaskId), "reParentTask"});
        DisplayContent prevDisplayContent = getDisplayContent();
        if (stack.mStackId == 4) {
            this.mPreserveNonFloatingState = true;
        } else {
            this.mPreserveNonFloatingState = false;
        }
        getParent().removeChild(this);
        stack.addTask(this, position, showForAllUsers(), moveParents);
        DisplayContent displayContent = stack.getDisplayContent();
        displayContent.setLayoutNeeded();
        if (prevDisplayContent != displayContent) {
            onDisplayChanged(displayContent);
            prevDisplayContent.setLayoutNeeded();
        }
    }

    void positionAt(int position, Rect bounds, Configuration overrideConfig) {
        this.mStack.positionChildAt(position, this, false);
        resizeLocked(bounds, overrideConfig, false);
    }

    void onParentSet() {
        updateDisplayInfo(getDisplayContent());
        if (StackId.windowsAreScaleable(this.mStack.mStackId)) {
            forceWindowsScaleable(true);
        } else {
            forceWindowsScaleable(false);
        }
    }

    void removeChild(AppWindowToken token) {
        if (this.mChildren.contains(token)) {
            super.removeChild(token);
            if (this.mChildren.isEmpty()) {
                EventLog.writeEvent(EventLogTags.WM_TASK_REMOVED, new Object[]{Integer.valueOf(this.mTaskId), "removeAppToken: last token"});
                if (this.mDeferRemoval) {
                    removeIfPossible();
                }
            }
            return;
        }
        Slog.e(TAG, "removeChild: token=" + this + " not found.");
    }

    void setSendingToBottom(boolean toBottom) {
        for (int appTokenNdx = 0; appTokenNdx < this.mChildren.size(); appTokenNdx++) {
            ((AppWindowToken) this.mChildren.get(appTokenNdx)).sendingToBottom = toBottom;
        }
    }

    private int setBounds(Rect bounds, Configuration overrideConfig) {
        if (overrideConfig == null) {
            overrideConfig = Configuration.EMPTY;
        }
        if (bounds == null && (Configuration.EMPTY.equals(overrideConfig) ^ 1) != 0) {
            Configuration cfgTemp = new Configuration(overrideConfig);
            cfgTemp.extraConfig.setToDefaults();
            if (!Configuration.EMPTY.equals(cfgTemp)) {
                throw new IllegalArgumentException("null bounds but non empty configuration: " + overrideConfig);
            }
        }
        if (bounds == null || !Configuration.EMPTY.equals(overrideConfig)) {
            boolean oldFullscreen = this.mFillsParent;
            int rotation = 0;
            DisplayContent displayContent = this.mStack.getDisplayContent();
            if (displayContent != null) {
                displayContent.getLogicalDisplayRect(this.mTmpRect);
                rotation = displayContent.getDisplayInfo().rotation;
                this.mFillsParent = bounds == null;
                if (this.mFillsParent) {
                    bounds = this.mTmpRect;
                }
            }
            if (bounds == null) {
                return 0;
            }
            if (this.mBounds.equals(bounds) && oldFullscreen == this.mFillsParent && this.mRotation == rotation) {
                return 0;
            }
            int boundsChange = 0;
            if (!(this.mBounds.left == bounds.left && this.mBounds.top == bounds.top)) {
                boundsChange = 1;
            }
            if (!(this.mBounds.width() == bounds.width() && this.mBounds.height() == bounds.height())) {
                boundsChange |= 2;
            }
            this.mBounds.set(bounds);
            this.mRotation = rotation;
            if (displayContent != null) {
                displayContent.mDimLayerController.updateDimLayer(this);
            }
            if (this.mFillsParent) {
                overrideConfig = Configuration.EMPTY;
            }
            onOverrideConfigurationChanged(overrideConfig);
            return boundsChange;
        }
        throw new IllegalArgumentException("non null bounds, but empty configuration");
    }

    void setTempInsetBounds(Rect tempInsetBounds) {
        if (tempInsetBounds != null) {
            this.mTempInsetBounds.set(tempInsetBounds);
        } else {
            this.mTempInsetBounds.setEmpty();
        }
    }

    void getTempInsetBounds(Rect out) {
        out.set(this.mTempInsetBounds);
    }

    void setResizeable(int resizeMode) {
        this.mResizeMode = resizeMode;
    }

    boolean isResizeable() {
        if (ActivityInfo.isResizeableMode(this.mResizeMode) || this.mSupportsPictureInPicture) {
            return true;
        }
        return this.mService.mForceResizableTasks;
    }

    boolean preserveOrientationOnResize() {
        if (this.mResizeMode == 6 || this.mResizeMode == 5 || this.mResizeMode == 7) {
            return true;
        }
        return false;
    }

    boolean cropWindowsToStackBounds() {
        return isResizeable();
    }

    boolean isHomeTask() {
        return this.mHomeTask;
    }

    boolean resizeLocked(Rect bounds, Configuration overrideConfig, boolean forced) {
        int boundsChanged = setBounds(bounds, overrideConfig);
        if (forced) {
            boundsChanged |= 2;
        }
        if (boundsChanged == 0) {
            return false;
        }
        if ((boundsChanged & 2) == 2) {
            onResize();
        } else {
            onMovedByResize();
        }
        return true;
    }

    void prepareFreezingBounds() {
        this.mPreparedFrozenBounds.set(this.mBounds);
        this.mPreparedFrozenMergedConfig.setTo(getConfiguration());
    }

    void alignToAdjustedBounds(Rect adjustedBounds, Rect tempInsetBounds, boolean alignBottom) {
        if (isResizeable() && !Configuration.EMPTY.equals(getOverrideConfiguration())) {
            getBounds(this.mTmpRect2);
            if (alignBottom) {
                this.mTmpRect2.offset(0, adjustedBounds.bottom - this.mTmpRect2.bottom);
            } else {
                this.mTmpRect2.offsetTo(adjustedBounds.left, adjustedBounds.top);
            }
            setTempInsetBounds(tempInsetBounds);
            resizeLocked(this.mTmpRect2, getOverrideConfiguration(), false);
        }
    }

    private boolean useCurrentBounds() {
        DisplayContent displayContent = this.mStack.getDisplayContent();
        if (this.mFillsParent || (StackId.isTaskResizeableByDockedStack(this.mStack.mStackId) ^ 1) != 0 || displayContent == null || displayContent.getDockedStackIgnoringVisibility() != null) {
            return true;
        }
        return false;
    }

    void getBounds(Rect out) {
        if (useCurrentBounds()) {
            out.set(this.mBounds);
        } else {
            this.mStack.getDisplayContent().getLogicalDisplayRect(out);
        }
    }

    boolean getMaxVisibleBounds(Rect out) {
        boolean foundTop = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) this.mChildren.get(i);
            if (!(token.mIsExiting || token.isClientHidden() || token.hiddenRequested)) {
                WindowState win = token.findMainWindow();
                if (win != null) {
                    if (foundTop) {
                        if (win.mVisibleFrame.left < out.left) {
                            out.left = win.mVisibleFrame.left;
                        }
                        if (win.mVisibleFrame.top < out.top) {
                            out.top = win.mVisibleFrame.top;
                        }
                        if (win.mVisibleFrame.right > out.right) {
                            out.right = win.mVisibleFrame.right;
                        }
                        if (win.mVisibleFrame.bottom > out.bottom) {
                            out.bottom = win.mVisibleFrame.bottom;
                        }
                    } else {
                        out.set(win.mVisibleFrame);
                        foundTop = true;
                    }
                }
            }
        }
        return foundTop;
    }

    public void getDimBounds(Rect out) {
        if (this.mStack != null) {
            boolean dockedResizing;
            DisplayContent displayContent = this.mStack.getDisplayContent();
            if (displayContent != null) {
                dockedResizing = displayContent.mDividerControllerLocked.isResizing();
            } else {
                dockedResizing = false;
            }
            if (!useCurrentBounds()) {
                if (displayContent != null) {
                    displayContent.getLogicalDisplayRect(out);
                }
            } else if ((HwPCUtils.isPcDynamicStack(this.mStack.mStackId) && this.mFillsParent) || !inFreeformWorkspace() || !getMaxVisibleBounds(out)) {
                if (this.mFillsParent) {
                    out.set(this.mBounds);
                } else {
                    if (dockedResizing) {
                        this.mStack.getBounds(out);
                    } else {
                        this.mStack.getBounds(this.mTmpRect);
                        this.mTmpRect.intersect(this.mBounds);
                    }
                    out.set(this.mTmpRect);
                }
            }
        }
    }

    void setDragResizing(boolean dragResizing, int dragResizeMode) {
        if (this.mDragResizing == dragResizing) {
            return;
        }
        if (DragResizeMode.isModeAllowedForStack(this.mStack.mStackId, dragResizeMode)) {
            this.mDragResizing = dragResizing;
            this.mDragResizeMode = dragResizeMode;
            resetDragResizingChangeReported();
            return;
        }
        throw new IllegalArgumentException("Drag resize mode not allow for stack stackId=" + this.mStack.mStackId + " dragResizeMode=" + dragResizeMode);
    }

    boolean isDragResizing() {
        return this.mDragResizing;
    }

    int getDragResizeMode() {
        return this.mDragResizeMode;
    }

    void updateDisplayInfo(DisplayContent displayContent) {
        if (displayContent != null) {
            if (this.mFillsParent) {
                setBounds(null, Configuration.EMPTY);
                return;
            }
            int newRotation = displayContent.getDisplayInfo().rotation;
            if (this.mRotation != newRotation) {
                this.mTmpRect2.set(this.mBounds);
                if (StackId.isTaskResizeAllowed(this.mStack.mStackId)) {
                    displayContent.rotateBounds(this.mRotation, newRotation, this.mTmpRect2);
                    if (setBounds(this.mTmpRect2, getOverrideConfiguration()) != 0) {
                        TaskWindowContainerController controller = getController();
                        if (controller != null) {
                            controller.requestResize(this.mBounds, 1);
                        }
                    }
                    return;
                }
                setBounds(this.mTmpRect2, getOverrideConfiguration());
            }
        }
    }

    void cancelTaskWindowTransition() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((AppWindowToken) this.mChildren.get(i)).mAppAnimator.clearAnimation();
        }
    }

    void cancelTaskThumbnailTransition() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((AppWindowToken) this.mChildren.get(i)).mAppAnimator.clearThumbnail();
        }
    }

    boolean showForAllUsers() {
        int tokensCount = this.mChildren.size();
        if (tokensCount != 0) {
            return ((AppWindowToken) this.mChildren.get(tokensCount - 1)).mShowForAllUsers;
        }
        return false;
    }

    boolean inFreeformWorkspace() {
        boolean z = true;
        if (this.mStack != null && HwPCUtils.isPcDynamicStack(this.mStack.mStackId)) {
            return true;
        }
        if (this.mStack == null || this.mStack.mStackId != 2) {
            z = false;
        }
        return z;
    }

    boolean inPinnedWorkspace() {
        return this.mStack != null && this.mStack.mStackId == 4;
    }

    boolean isFloating() {
        if (!StackId.tasksAreFloating(this.mStack.mStackId) || (this.mStack.isAnimatingBoundsToFullscreen() ^ 1) == 0) {
            return false;
        }
        return this.mPreserveNonFloatingState ^ 1;
    }

    WindowState getTopVisibleAppMainWindow() {
        AppWindowToken token = getTopVisibleAppToken();
        if (token != null) {
            return token.findMainWindow();
        }
        return null;
    }

    AppWindowToken getTopFullscreenAppToken() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) this.mChildren.get(i);
            WindowState win = token.findMainWindow();
            if (win != null && win.mAttrs.isFullscreen()) {
                return token;
            }
        }
        return null;
    }

    AppWindowToken getTopVisibleAppToken() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) this.mChildren.get(i);
            if (!token.mIsExiting && (token.isClientHidden() ^ 1) != 0 && (token.hiddenRequested ^ 1) != 0) {
                return token;
            }
        }
        return null;
    }

    public boolean dimFullscreen() {
        return isFullscreen();
    }

    boolean isFullscreen() {
        if (useCurrentBounds()) {
            return this.mFillsParent;
        }
        return true;
    }

    public DisplayInfo getDisplayInfo() {
        return getDisplayContent().getDisplayInfo();
    }

    public boolean isAttachedToDisplay() {
        return getDisplayContent() != null;
    }

    void forceWindowsScaleable(boolean force) {
        this.mService.openSurfaceTransaction();
        try {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                ((AppWindowToken) this.mChildren.get(i)).forceWindowsScaleableInTransaction(force);
            }
        } finally {
            this.mService.closeSurfaceTransaction();
        }
    }

    void setTaskDescription(TaskDescription taskDescription) {
        this.mTaskDescription = taskDescription;
    }

    TaskDescription getTaskDescription() {
        return this.mTaskDescription;
    }

    boolean fillsParent() {
        return !this.mFillsParent ? StackId.isTaskResizeAllowed(this.mStack.mStackId) ^ 1 : true;
    }

    TaskWindowContainerController getController() {
        return (TaskWindowContainerController) super.getController();
    }

    void forAllTasks(Consumer<Task> callback) {
        callback.accept(this);
    }

    public String toString() {
        return "{taskId=" + this.mTaskId + " appTokens=" + this.mChildren + " mdr=" + this.mDeferRemoval + "}";
    }

    String getName() {
        return toShortString();
    }

    void clearPreserveNonFloatingState() {
        this.mPreserveNonFloatingState = false;
    }

    public String toShortString() {
        return "Task=" + this.mTaskId;
    }

    public void dump(String prefix, PrintWriter pw) {
        String doublePrefix = prefix + "  ";
        pw.println(prefix + "taskId=" + this.mTaskId);
        pw.println(doublePrefix + "mFillsParent=" + this.mFillsParent);
        pw.println(doublePrefix + "mBounds=" + this.mBounds.toShortString());
        pw.println(doublePrefix + "mdr=" + this.mDeferRemoval);
        pw.println(doublePrefix + "appTokens=" + this.mChildren);
        pw.println(doublePrefix + "mTempInsetBounds=" + this.mTempInsetBounds.toShortString());
        String triplePrefix = doublePrefix + "  ";
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken wtoken = (AppWindowToken) this.mChildren.get(i);
            pw.println(triplePrefix + "Activity #" + i + " " + wtoken);
            wtoken.dump(pw, triplePrefix);
        }
    }
}
