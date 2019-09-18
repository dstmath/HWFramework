package com.android.server.wm;

import android.app.ActivityManager;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.EventLogTags;
import java.io.PrintWriter;
import java.util.function.Consumer;

class Task extends WindowContainer<AppWindowToken> {
    static final String TAG = "WindowManager";
    private boolean mCanAffectSystemUiFlags = true;
    private boolean mDeferRemoval = false;
    private Dimmer mDimmer = new Dimmer(this);
    private int mDragResizeMode;
    private boolean mDragResizing;
    final Rect mPreparedFrozenBounds = new Rect();
    final Configuration mPreparedFrozenMergedConfig = new Configuration();
    private boolean mPreserveNonFloatingState = false;
    private int mResizeMode;
    private int mRotation;
    TaskStack mStack;
    private boolean mSupportsPictureInPicture;
    private ActivityManager.TaskDescription mTaskDescription;
    final int mTaskId;
    private final Rect mTempInsetBounds = new Rect();
    private final Rect mTmpDimBoundsRect = new Rect();
    private Rect mTmpRect = new Rect();
    private Rect mTmpRect2 = new Rect();
    private Rect mTmpRect3 = new Rect();
    final int mUserId;

    Task(int taskId, TaskStack stack, int userId, WindowManagerService service, int resizeMode, boolean supportsPictureInPicture, ActivityManager.TaskDescription taskDescription, TaskWindowContainerController controller) {
        super(service);
        this.mTaskId = taskId;
        this.mStack = stack;
        this.mUserId = userId;
        this.mResizeMode = resizeMode;
        this.mSupportsPictureInPicture = supportsPictureInPicture;
        setController(controller);
        setBounds(getOverrideBounds());
        this.mTaskDescription = taskDescription;
        setOrientation(-2);
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getDisplayContent() {
        if (this.mStack != null) {
            return this.mStack.getDisplayContent();
        }
        return null;
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

    /* access modifiers changed from: package-private */
    public void addChild(AppWindowToken wtoken, int position) {
        super.addChild(wtoken, getAdjustedAddPosition(position));
        this.mDeferRemoval = false;
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, AppWindowToken child, boolean includingParents) {
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

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldDeferRemoval() {
        return hasWindowsAlive() && this.mStack.isSelfOrChildAnimating();
    }

    /* access modifiers changed from: package-private */
    public void removeIfPossible() {
        if (shouldDeferRemoval()) {
            this.mDeferRemoval = true;
        } else {
            removeImmediately();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeImmediately() {
        EventLog.writeEvent(EventLogTags.WM_TASK_REMOVED, new Object[]{Integer.valueOf(this.mTaskId), "removeTask"});
        this.mDeferRemoval = false;
        super.removeImmediately();
    }

    /* access modifiers changed from: package-private */
    public void reparent(TaskStack stack, int position, boolean moveParents) {
        if (stack != this.mStack) {
            EventLog.writeEvent(EventLogTags.WM_TASK_REMOVED, new Object[]{Integer.valueOf(this.mTaskId), "reParentTask"});
            DisplayContent prevDisplayContent = getDisplayContent();
            if (stack.inPinnedWindowingMode()) {
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
                return;
            }
            return;
        }
        throw new IllegalArgumentException("task=" + this + " already child of stack=" + this.mStack);
    }

    /* access modifiers changed from: package-private */
    public void positionAt(int position) {
        this.mStack.positionChildAt(position, this, false);
    }

    /* access modifiers changed from: package-private */
    public void onParentSet() {
        super.onParentSet();
        updateDisplayInfo(getDisplayContent());
        if (getWindowConfiguration().windowsAreScaleable()) {
            forceWindowsScaleable(true);
        } else {
            forceWindowsScaleable(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeChild(AppWindowToken token) {
        if (!this.mChildren.contains(token)) {
            Slog.e(TAG, "removeChild: token=" + this + " not found.");
            return;
        }
        super.removeChild(token);
        if (this.mChildren.isEmpty()) {
            EventLog.writeEvent(EventLogTags.WM_TASK_REMOVED, new Object[]{Integer.valueOf(this.mTaskId), "removeAppToken: last token"});
            if (this.mDeferRemoval) {
                removeIfPossible();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setSendingToBottom(boolean toBottom) {
        for (int appTokenNdx = 0; appTokenNdx < this.mChildren.size(); appTokenNdx++) {
            ((AppWindowToken) this.mChildren.get(appTokenNdx)).sendingToBottom = toBottom;
        }
    }

    public int setBounds(Rect bounds, boolean forceResize) {
        int boundsChanged = setBounds(bounds);
        if (!forceResize || (boundsChanged & 2) == 2) {
            return boundsChanged;
        }
        onResize();
        return 2 | boundsChanged;
    }

    public int setBounds(Rect bounds) {
        int rotation = 0;
        DisplayContent displayContent = this.mStack.getDisplayContent();
        if (displayContent != null) {
            rotation = displayContent.getDisplayInfo().rotation;
        } else if (bounds == null) {
            return 0;
        }
        if (equivalentOverrideBounds(bounds) && this.mRotation == rotation) {
            return 0;
        }
        int boundsChange = super.setBounds(bounds);
        this.mRotation = rotation;
        return boundsChange;
    }

    /* access modifiers changed from: package-private */
    public void setTempInsetBounds(Rect tempInsetBounds) {
        if (tempInsetBounds != null) {
            this.mTempInsetBounds.set(tempInsetBounds);
        } else {
            this.mTempInsetBounds.setEmpty();
        }
    }

    /* access modifiers changed from: package-private */
    public void getTempInsetBounds(Rect out) {
        out.set(this.mTempInsetBounds);
    }

    /* access modifiers changed from: package-private */
    public void setResizeable(int resizeMode) {
        this.mResizeMode = resizeMode;
    }

    /* access modifiers changed from: package-private */
    public boolean isResizeable() {
        return ActivityInfo.isResizeableMode(this.mResizeMode) || this.mSupportsPictureInPicture || this.mService.mForceResizableTasks;
    }

    /* access modifiers changed from: package-private */
    public boolean preserveOrientationOnResize() {
        return this.mResizeMode == 6 || this.mResizeMode == 5 || this.mResizeMode == 7;
    }

    /* access modifiers changed from: package-private */
    public boolean cropWindowsToStackBounds() {
        return isResizeable();
    }

    /* access modifiers changed from: package-private */
    public void prepareFreezingBounds() {
        this.mPreparedFrozenBounds.set(getBounds());
        this.mPreparedFrozenMergedConfig.setTo(getConfiguration());
    }

    /* access modifiers changed from: package-private */
    public void alignToAdjustedBounds(Rect adjustedBounds, Rect tempInsetBounds, boolean alignBottom) {
        if (isResizeable() && !Configuration.EMPTY.equals(getOverrideConfiguration())) {
            getBounds(this.mTmpRect2);
            if (alignBottom) {
                this.mTmpRect2.offset(0, adjustedBounds.bottom - this.mTmpRect2.bottom);
            } else {
                this.mTmpRect2.offsetTo(adjustedBounds.left, adjustedBounds.top);
            }
            setTempInsetBounds(tempInsetBounds);
            setBounds(this.mTmpRect2, false);
        }
    }

    private boolean useCurrentBounds() {
        DisplayContent displayContent = getDisplayContent();
        return matchParentBounds() || !inSplitScreenSecondaryWindowingMode() || displayContent == null || displayContent.getSplitScreenPrimaryStackIgnoringVisibility() != null;
    }

    public void getBounds(Rect out) {
        if (useCurrentBounds()) {
            super.getBounds(out);
        } else {
            this.mStack.getDisplayContent().getBounds(out);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getMaxVisibleBounds(Rect out) {
        boolean foundTop = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) this.mChildren.get(i);
            if (!token.mIsExiting && !token.isClientHidden() && !token.hiddenRequested) {
                WindowState win = token.findMainWindow();
                if (win != null) {
                    if (!foundTop) {
                        out.set(win.mVisibleFrame);
                        foundTop = true;
                    } else {
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
                    }
                }
            }
        }
        return foundTop;
    }

    public void getDimBounds(Rect out) {
        if (this.mStack != null) {
            DisplayContent displayContent = this.mStack.getDisplayContent();
            boolean dockedResizing = displayContent != null && displayContent.mDividerControllerLocked.isResizing();
            if (!useCurrentBounds()) {
                if (displayContent != null) {
                    displayContent.getBounds(out);
                }
            } else if ((HwPCUtils.isExtDynamicStack(this.mStack.mStackId) && matchParentBounds()) || ((!inFreeformWindowingMode() && !inHwPCFreeformWindowingMode()) || !getMaxVisibleBounds(out))) {
                if (matchParentBounds()) {
                    out.set(getBounds());
                } else if (dockedResizing) {
                    this.mStack.getBounds(out);
                } else {
                    this.mStack.getBounds(this.mTmpRect);
                    this.mTmpRect.intersect(getBounds());
                    out.set(this.mTmpRect);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDragResizing(boolean dragResizing, int dragResizeMode) {
        if (this.mDragResizing == dragResizing) {
            return;
        }
        if (DragResizeMode.isModeAllowedForStack(this.mStack, dragResizeMode)) {
            this.mDragResizing = dragResizing;
            this.mDragResizeMode = dragResizeMode;
            resetDragResizingChangeReported();
            return;
        }
        throw new IllegalArgumentException("Drag resize mode not allow for stack stackId=" + this.mStack.mStackId + " dragResizeMode=" + dragResizeMode + " mode=" + this.mStack.getWindowingMode());
    }

    /* access modifiers changed from: package-private */
    public boolean isDragResizing() {
        return this.mDragResizing;
    }

    /* access modifiers changed from: package-private */
    public int getDragResizeMode() {
        return this.mDragResizeMode;
    }

    /* access modifiers changed from: package-private */
    public void updateDisplayInfo(DisplayContent displayContent) {
        if (displayContent != null) {
            if (matchParentBounds()) {
                setBounds(null);
                return;
            }
            int newRotation = displayContent.getDisplayInfo().rotation;
            if (this.mRotation != newRotation) {
                this.mTmpRect2.set(getBounds());
                if (!getWindowConfiguration().canResizeTask()) {
                    setBounds(this.mTmpRect2);
                    return;
                }
                displayContent.rotateBounds(this.mRotation, newRotation, this.mTmpRect2);
                if (setBounds(this.mTmpRect2) != 0) {
                    TaskWindowContainerController controller = getController();
                    if (controller != null) {
                        controller.requestResize(getBounds(), 1);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelTaskWindowTransition() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((AppWindowToken) this.mChildren.get(i)).cancelAnimation();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSamePackageInTask() {
        int uid = -1;
        boolean ret = true;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken tmpToken = (AppWindowToken) this.mChildren.get(i);
            int i2 = -1;
            if (uid == -1) {
                if (!tmpToken.isEmpty()) {
                    i2 = ((WindowState) tmpToken.mChildren.peekLast()).mOwnerUid;
                }
                uid = i2;
            } else {
                if (!tmpToken.isEmpty()) {
                    i2 = ((WindowState) tmpToken.mChildren.peekLast()).mOwnerUid;
                }
                ret = uid == i2;
            }
            if (!ret) {
                return ret;
            }
        }
        return ret;
    }

    /* access modifiers changed from: package-private */
    public boolean showForAllUsers() {
        int tokensCount = this.mChildren.size();
        return tokensCount != 0 && ((AppWindowToken) this.mChildren.get(tokensCount + -1)).mShowForAllUsers;
    }

    /* access modifiers changed from: package-private */
    public boolean isFloating() {
        return getWindowConfiguration().tasksAreFloating() && !this.mStack.isAnimatingBoundsToFullscreen() && !this.mPreserveNonFloatingState;
    }

    public SurfaceControl getAnimationLeashParent() {
        return getAppAnimationLayer(0);
    }

    /* access modifiers changed from: package-private */
    public boolean isTaskAnimating() {
        RecentsAnimationController recentsAnim = this.mService.getRecentsAnimationController();
        if (recentsAnim == null || !recentsAnim.isAnimatingTask(this)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public WindowState getTopVisibleAppMainWindow() {
        AppWindowToken token = getTopVisibleAppToken();
        if (token != null) {
            return token.findMainWindow();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken getTopFullscreenAppToken() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) this.mChildren.get(i);
            WindowState win = token.findMainWindow();
            if (win != null && win.mAttrs.isFullscreen()) {
                return token;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken getTopVisibleAppToken() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) this.mChildren.get(i);
            if (!token.mIsExiting && !token.isClientHidden() && !token.hiddenRequested) {
                return token;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean isFullscreen() {
        if (useCurrentBounds()) {
            return matchParentBounds();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void forceWindowsScaleable(boolean force) {
        String str;
        this.mService.openSurfaceTransaction();
        try {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                ((AppWindowToken) this.mChildren.get(i)).forceWindowsScaleableInTransaction(force);
            }
        } finally {
            str = "forceWindowsScaleable";
            this.mService.closeSurfaceTransaction(str);
        }
    }

    /* access modifiers changed from: package-private */
    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
        this.mTaskDescription = taskDescription;
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskDescription getTaskDescription() {
        return this.mTaskDescription;
    }

    /* access modifiers changed from: package-private */
    public boolean fillsParent() {
        return matchParentBounds() || !getWindowConfiguration().canResizeTask();
    }

    /* access modifiers changed from: package-private */
    public TaskWindowContainerController getController() {
        return (TaskWindowContainerController) super.getController();
    }

    /* access modifiers changed from: package-private */
    public void forAllTasks(Consumer<Task> callback) {
        callback.accept(this);
    }

    /* access modifiers changed from: package-private */
    public void setCanAffectSystemUiFlags(boolean canAffectSystemUiFlags) {
        this.mCanAffectSystemUiFlags = canAffectSystemUiFlags;
    }

    /* access modifiers changed from: package-private */
    public boolean canAffectSystemUiFlags() {
        return this.mCanAffectSystemUiFlags;
    }

    /* access modifiers changed from: package-private */
    public void dontAnimateDimExit() {
        this.mDimmer.dontAnimateExit();
    }

    public String toString() {
        return "{taskId=" + this.mTaskId + " appTokens=" + this.mChildren + " mdr=" + this.mDeferRemoval + "}";
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return toShortString();
    }

    /* access modifiers changed from: package-private */
    public void clearPreserveNonFloatingState() {
        this.mPreserveNonFloatingState = false;
    }

    /* access modifiers changed from: package-private */
    public Dimmer getDimmer() {
        return this.mDimmer;
    }

    /* access modifiers changed from: package-private */
    public void prepareSurfaces() {
        this.mDimmer.resetDimStates();
        super.prepareSurfaces();
        getDimBounds(this.mTmpDimBoundsRect);
        this.mTmpDimBoundsRect.offsetTo(0, 0);
        if (this.mDimmer.updateDims(getPendingTransaction(), this.mTmpDimBoundsRect)) {
            scheduleAnimation();
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, boolean trim) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, trim);
        proto.write(1120986464258L, this.mTaskId);
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((AppWindowToken) this.mChildren.get(i)).writeToProto(proto, 2246267895811L, trim);
        }
        proto.write(1133871366148L, matchParentBounds());
        getBounds().writeToProto(proto, 1146756268037L);
        this.mTempInsetBounds.writeToProto(proto, 1146756268038L);
        proto.write(1133871366151L, this.mDeferRemoval);
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        AppWindowToken wtoken;
        super.dump(pw, prefix, dumpAll);
        String doublePrefix = prefix + "  ";
        pw.println(prefix + "taskId=" + this.mTaskId);
        pw.println(doublePrefix + "mBounds=" + getBounds().toShortString());
        pw.println(doublePrefix + "mdr=" + this.mDeferRemoval);
        pw.println(doublePrefix + "appTokens=" + this.mChildren);
        pw.println(doublePrefix + "mTempInsetBounds=" + this.mTempInsetBounds.toShortString());
        String triplePrefix = doublePrefix + "  ";
        String quadruplePrefix = triplePrefix + "  ";
        int i = this.mChildren.size() - 1;
        while (i >= 0) {
            pw.println(triplePrefix + "Activity #" + i + " " + wtoken);
            wtoken.dump(pw, quadruplePrefix, dumpAll);
            i += -1;
        }
    }

    /* access modifiers changed from: package-private */
    public String toShortString() {
        return "Task=" + this.mTaskId;
    }
}
