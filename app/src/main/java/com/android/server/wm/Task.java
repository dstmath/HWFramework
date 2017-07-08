package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.EventLog;
import android.view.DisplayInfo;
import com.android.server.EventLogTags;
import java.io.PrintWriter;
import java.util.ArrayList;

class Task implements DimLayerUser {
    static final int BOUNDS_CHANGE_NONE = 0;
    static final int BOUNDS_CHANGE_POSITION = 1;
    static final int BOUNDS_CHANGE_SIZE = 2;
    static final String TAG = null;
    final AppTokenList mAppTokens;
    private Rect mBounds;
    boolean mDeferRemoval;
    private int mDragResizeMode;
    private boolean mDragResizing;
    private boolean mFullscreen;
    private boolean mHomeTask;
    Configuration mOverrideConfig;
    private Rect mPreScrollBounds;
    final Rect mPreparedFrozenBounds;
    final Configuration mPreparedFrozenMergedConfig;
    private int mResizeMode;
    int mRotation;
    private boolean mScrollValid;
    final WindowManagerService mService;
    TaskStack mStack;
    final int mTaskId;
    private final Rect mTempInsetBounds;
    private Rect mTmpRect;
    private Rect mTmpRect2;
    final int mUserId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.Task.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.Task.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.Task.<clinit>():void");
    }

    Task(int taskId, TaskStack stack, int userId, WindowManagerService service, Rect bounds, Configuration config) {
        this.mAppTokens = new AppTokenList();
        this.mDeferRemoval = false;
        this.mBounds = new Rect();
        this.mPreparedFrozenBounds = new Rect();
        this.mPreparedFrozenMergedConfig = new Configuration();
        this.mPreScrollBounds = new Rect();
        this.mTempInsetBounds = new Rect();
        this.mFullscreen = true;
        this.mOverrideConfig = Configuration.EMPTY;
        this.mTmpRect = new Rect();
        this.mTmpRect2 = new Rect();
        this.mTaskId = taskId;
        this.mStack = stack;
        this.mUserId = userId;
        this.mService = service;
        setBounds(bounds, config);
    }

    DisplayContent getDisplayContent() {
        return this.mStack.getDisplayContent();
    }

    void addAppToken(int addPos, AppWindowToken wtoken, int resizeMode, boolean homeTask) {
        int lastPos = this.mAppTokens.size();
        if (addPos >= lastPos) {
            addPos = lastPos;
        } else {
            int pos = BOUNDS_CHANGE_NONE;
            while (pos < lastPos && pos < addPos) {
                if (((AppWindowToken) this.mAppTokens.get(pos)).removed) {
                    addPos += BOUNDS_CHANGE_POSITION;
                }
                pos += BOUNDS_CHANGE_POSITION;
            }
        }
        this.mAppTokens.add(addPos, wtoken);
        wtoken.mTask = this;
        this.mDeferRemoval = false;
        this.mResizeMode = resizeMode;
        this.mHomeTask = homeTask;
    }

    private boolean hasWindowsAlive() {
        for (int i = this.mAppTokens.size() - 1; i >= 0; i--) {
            if (((AppWindowToken) this.mAppTokens.get(i)).hasWindowsAlive()) {
                return true;
            }
        }
        return false;
    }

    void removeLocked() {
        if (hasWindowsAlive() && this.mStack.isAnimating()) {
            this.mDeferRemoval = true;
            return;
        }
        Object[] objArr = new Object[BOUNDS_CHANGE_SIZE];
        objArr[BOUNDS_CHANGE_NONE] = Integer.valueOf(this.mTaskId);
        objArr[BOUNDS_CHANGE_POSITION] = "removeTask";
        EventLog.writeEvent(EventLogTags.WM_TASK_REMOVED, objArr);
        this.mDeferRemoval = false;
        DisplayContent content = getDisplayContent();
        if (content != null) {
            content.mDimLayerController.removeDimLayerUser(this);
        }
        this.mStack.removeTask(this);
        this.mService.mTaskIdToTask.delete(this.mTaskId);
    }

    void moveTaskToStack(TaskStack stack, boolean toTop) {
        if (stack != this.mStack) {
            Object[] objArr = new Object[BOUNDS_CHANGE_SIZE];
            objArr[BOUNDS_CHANGE_NONE] = Integer.valueOf(this.mTaskId);
            objArr[BOUNDS_CHANGE_POSITION] = "moveTask";
            EventLog.writeEvent(EventLogTags.WM_TASK_REMOVED, objArr);
            if (this.mStack != null) {
                this.mStack.removeTask(this);
            }
            stack.addTask(this, toTop);
        }
    }

    void positionTaskInStack(TaskStack stack, int position, Rect bounds, Configuration config) {
        if (!(this.mStack == null || stack == this.mStack)) {
            Object[] objArr = new Object[BOUNDS_CHANGE_SIZE];
            objArr[BOUNDS_CHANGE_NONE] = Integer.valueOf(this.mTaskId);
            objArr[BOUNDS_CHANGE_POSITION] = "moveTask";
            EventLog.writeEvent(EventLogTags.WM_TASK_REMOVED, objArr);
            this.mStack.removeTask(this);
        }
        stack.positionTask(this, position, showForAllUsers());
        resizeLocked(bounds, config, false);
        for (int activityNdx = this.mAppTokens.size() - 1; activityNdx >= 0; activityNdx--) {
            ArrayList<WindowState> windows = ((AppWindowToken) this.mAppTokens.get(activityNdx)).allAppWindows;
            for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                ((WindowState) windows.get(winNdx)).notifyMovedInStack();
            }
        }
    }

    boolean removeAppToken(AppWindowToken wtoken) {
        boolean removed = this.mAppTokens.remove(wtoken);
        if (this.mAppTokens.size() == 0) {
            Object[] objArr = new Object[BOUNDS_CHANGE_SIZE];
            objArr[BOUNDS_CHANGE_NONE] = Integer.valueOf(this.mTaskId);
            objArr[BOUNDS_CHANGE_POSITION] = "removeAppToken: last token";
            EventLog.writeEvent(EventLogTags.WM_TASK_REMOVED, objArr);
            if (this.mDeferRemoval) {
                removeLocked();
            }
        }
        wtoken.mTask = null;
        return removed;
    }

    void setSendingToBottom(boolean toBottom) {
        for (int appTokenNdx = BOUNDS_CHANGE_NONE; appTokenNdx < this.mAppTokens.size(); appTokenNdx += BOUNDS_CHANGE_POSITION) {
            ((AppWindowToken) this.mAppTokens.get(appTokenNdx)).sendingToBottom = toBottom;
        }
    }

    private int setBounds(Rect bounds, Configuration config) {
        if (config == null) {
            config = Configuration.EMPTY;
        }
        if (bounds == null && !Configuration.EMPTY.equals(config)) {
            throw new IllegalArgumentException("null bounds but non empty configuration: " + config);
        } else if (bounds == null || !Configuration.EMPTY.equals(config)) {
            boolean oldFullscreen = this.mFullscreen;
            int rotation = BOUNDS_CHANGE_NONE;
            DisplayContent displayContent = this.mStack.getDisplayContent();
            if (displayContent != null) {
                displayContent.getLogicalDisplayRect(this.mTmpRect);
                rotation = displayContent.getDisplayInfo().rotation;
                this.mFullscreen = bounds == null;
                if (this.mFullscreen) {
                    bounds = this.mTmpRect;
                }
            }
            if (bounds == null) {
                return BOUNDS_CHANGE_NONE;
            }
            if (this.mPreScrollBounds.equals(bounds) && oldFullscreen == this.mFullscreen && this.mRotation == rotation) {
                return BOUNDS_CHANGE_NONE;
            }
            int boundsChange = BOUNDS_CHANGE_NONE;
            if (!(this.mPreScrollBounds.left == bounds.left && this.mPreScrollBounds.top == bounds.top)) {
                boundsChange = BOUNDS_CHANGE_POSITION;
            }
            if (!(this.mPreScrollBounds.width() == bounds.width() && this.mPreScrollBounds.height() == bounds.height())) {
                boundsChange |= BOUNDS_CHANGE_SIZE;
            }
            this.mPreScrollBounds.set(bounds);
            resetScrollLocked();
            this.mRotation = rotation;
            if (displayContent != null) {
                displayContent.mDimLayerController.updateDimLayer(this);
            }
            if (this.mFullscreen) {
                config = Configuration.EMPTY;
            }
            this.mOverrideConfig = config;
            return boundsChange;
        } else {
            throw new IllegalArgumentException("non null bounds, but empty configuration");
        }
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
        if (this.mHomeTask) {
            return false;
        }
        return !ActivityInfo.isResizeableMode(this.mResizeMode) ? this.mService.mForceResizableTasks : true;
    }

    boolean cropWindowsToStackBounds() {
        return !this.mHomeTask && (isResizeable() || this.mResizeMode == BOUNDS_CHANGE_POSITION);
    }

    boolean isHomeTask() {
        return this.mHomeTask;
    }

    private boolean inCropWindowsResizeMode() {
        return (this.mHomeTask || isResizeable() || this.mResizeMode != BOUNDS_CHANGE_POSITION) ? false : true;
    }

    boolean resizeLocked(Rect bounds, Configuration configuration, boolean forced) {
        int boundsChanged = setBounds(bounds, configuration);
        if (forced) {
            boundsChanged |= BOUNDS_CHANGE_SIZE;
        }
        if (boundsChanged == 0) {
            return false;
        }
        if ((boundsChanged & BOUNDS_CHANGE_SIZE) == BOUNDS_CHANGE_SIZE) {
            resizeWindows();
        } else {
            moveWindows();
        }
        return true;
    }

    void prepareFreezingBounds() {
        this.mPreparedFrozenBounds.set(this.mBounds);
        this.mPreparedFrozenMergedConfig.setTo(this.mService.mCurConfiguration);
        this.mPreparedFrozenMergedConfig.updateFrom(this.mOverrideConfig);
    }

    void alignToAdjustedBounds(Rect adjustedBounds, Rect tempInsetBounds, boolean alignBottom) {
        if (isResizeable() && this.mOverrideConfig != Configuration.EMPTY) {
            getBounds(this.mTmpRect2);
            if (alignBottom) {
                this.mTmpRect2.offset(BOUNDS_CHANGE_NONE, adjustedBounds.bottom - this.mTmpRect2.bottom);
            } else {
                this.mTmpRect2.offsetTo(adjustedBounds.left, adjustedBounds.top);
            }
            setTempInsetBounds(tempInsetBounds);
            resizeLocked(this.mTmpRect2, this.mOverrideConfig, false);
        }
    }

    void resetScrollLocked() {
        if (this.mScrollValid) {
            this.mScrollValid = false;
            applyScrollToAllWindows(BOUNDS_CHANGE_NONE, BOUNDS_CHANGE_NONE);
        }
        this.mBounds.set(this.mPreScrollBounds);
    }

    void applyScrollToAllWindows(int xOffset, int yOffset) {
        for (int activityNdx = this.mAppTokens.size() - 1; activityNdx >= 0; activityNdx--) {
            ArrayList<WindowState> windows = ((AppWindowToken) this.mAppTokens.get(activityNdx)).allAppWindows;
            for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                WindowState win = (WindowState) windows.get(winNdx);
                win.mXOffset = xOffset;
                win.mYOffset = yOffset;
            }
        }
    }

    void applyScrollToWindowIfNeeded(WindowState win) {
        if (this.mScrollValid) {
            win.mXOffset = this.mBounds.left;
            win.mYOffset = this.mBounds.top;
        }
    }

    boolean scrollLocked(Rect bounds) {
        this.mStack.getDimBounds(this.mTmpRect);
        if (this.mService.mCurConfiguration.orientation == BOUNDS_CHANGE_SIZE) {
            if (bounds.left > this.mTmpRect.left) {
                bounds.left = this.mTmpRect.left;
                bounds.right = this.mTmpRect.left + this.mBounds.width();
            } else if (bounds.right < this.mTmpRect.right) {
                bounds.left = this.mTmpRect.right - this.mBounds.width();
                bounds.right = this.mTmpRect.right;
            }
        } else if (bounds.top > this.mTmpRect.top) {
            bounds.top = this.mTmpRect.top;
            bounds.bottom = this.mTmpRect.top + this.mBounds.height();
        } else if (bounds.bottom < this.mTmpRect.bottom) {
            bounds.top = this.mTmpRect.bottom - this.mBounds.height();
            bounds.bottom = this.mTmpRect.bottom;
        }
        if (this.mScrollValid && bounds.equals(this.mBounds)) {
            return false;
        }
        this.mBounds.set(bounds);
        this.mScrollValid = true;
        applyScrollToAllWindows(bounds.left, bounds.top);
        return true;
    }

    private boolean useCurrentBounds() {
        DisplayContent displayContent = this.mStack.getDisplayContent();
        if (this.mFullscreen || !StackId.isTaskResizeableByDockedStack(this.mStack.mStackId) || displayContent == null || displayContent.getDockedStackVisibleForUserLocked() != null) {
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
        for (int i = this.mAppTokens.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) this.mAppTokens.get(i);
            if (!(token.mIsExiting || token.clientHidden || token.hiddenRequested)) {
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
        DisplayContent displayContent = this.mStack.getDisplayContent();
        boolean isResizing = displayContent != null ? displayContent.mDividerControllerLocked.isResizing() : false;
        if (!useCurrentBounds()) {
            displayContent.getLogicalDisplayRect(out);
        } else if (!inFreeformWorkspace() || !getMaxVisibleBounds(out)) {
            if (this.mFullscreen) {
                out.set(this.mBounds);
            } else {
                if (isResizing) {
                    this.mStack.getBounds(out);
                } else {
                    this.mStack.getBounds(this.mTmpRect);
                    this.mTmpRect.intersect(this.mBounds);
                }
                out.set(this.mTmpRect);
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

    void resetDragResizingChangeReported() {
        for (int activityNdx = this.mAppTokens.size() - 1; activityNdx >= 0; activityNdx--) {
            ArrayList<WindowState> windows = ((AppWindowToken) this.mAppTokens.get(activityNdx)).allAppWindows;
            for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                ((WindowState) windows.get(winNdx)).resetDragResizingChangeReported();
            }
        }
    }

    boolean isDragResizing() {
        if (this.mDragResizing) {
            return true;
        }
        return this.mStack != null ? this.mStack.isDragResizing() : false;
    }

    int getDragResizeMode() {
        return this.mDragResizeMode;
    }

    void addWindowsWaitingForDrawnIfResizingChanged() {
        for (int activityNdx = this.mAppTokens.size() - 1; activityNdx >= 0; activityNdx--) {
            ArrayList<WindowState> windows = ((AppWindowToken) this.mAppTokens.get(activityNdx)).allAppWindows;
            for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                WindowState win = (WindowState) windows.get(winNdx);
                if (win.isDragResizeChanged()) {
                    this.mService.mWaitingForDrawn.add(win);
                }
            }
        }
    }

    void updateDisplayInfo(DisplayContent displayContent) {
        if (displayContent != null) {
            if (this.mFullscreen) {
                setBounds(null, Configuration.EMPTY);
                return;
            }
            int newRotation = displayContent.getDisplayInfo().rotation;
            if (this.mRotation != newRotation) {
                this.mTmpRect2.set(this.mPreScrollBounds);
                if (StackId.isTaskResizeAllowed(this.mStack.mStackId)) {
                    displayContent.rotateBounds(this.mRotation, newRotation, this.mTmpRect2);
                    if (setBounds(this.mTmpRect2, this.mOverrideConfig) != 0) {
                        this.mService.mH.obtainMessage(43, this.mTaskId, BOUNDS_CHANGE_POSITION, this.mPreScrollBounds).sendToTarget();
                    }
                    return;
                }
                setBounds(this.mTmpRect2, this.mOverrideConfig);
            }
        }
    }

    void resizeWindows() {
        ArrayList<WindowState> resizingWindows = this.mService.mResizingWindows;
        for (int activityNdx = this.mAppTokens.size() - 1; activityNdx >= 0; activityNdx--) {
            AppWindowToken atoken = (AppWindowToken) this.mAppTokens.get(activityNdx);
            atoken.destroySavedSurfaces();
            ArrayList<WindowState> windows = atoken.allAppWindows;
            for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                WindowState win = (WindowState) windows.get(winNdx);
                if (win.mHasSurface && !resizingWindows.contains(win)) {
                    resizingWindows.add(win);
                    if (!(win.computeDragResizing() || win.mAttrs.type != BOUNDS_CHANGE_POSITION || this.mStack.getBoundsAnimating() || win.isGoneForLayoutLw() || inPinnedWorkspace())) {
                        win.setResizedWhileNotDragResizing(true);
                    }
                }
                if (win.isGoneForLayoutLw()) {
                    win.mResizedWhileGone = true;
                }
            }
        }
    }

    void moveWindows() {
        for (int activityNdx = this.mAppTokens.size() - 1; activityNdx >= 0; activityNdx--) {
            ArrayList<WindowState> windows = ((AppWindowToken) this.mAppTokens.get(activityNdx)).allAppWindows;
            for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                ((WindowState) windows.get(winNdx)).mMovedByResize = true;
            }
        }
    }

    void cancelTaskWindowTransition() {
        for (int activityNdx = this.mAppTokens.size() - 1; activityNdx >= 0; activityNdx--) {
            ((AppWindowToken) this.mAppTokens.get(activityNdx)).mAppAnimator.clearAnimation();
        }
    }

    void cancelTaskThumbnailTransition() {
        for (int activityNdx = this.mAppTokens.size() - 1; activityNdx >= 0; activityNdx--) {
            ((AppWindowToken) this.mAppTokens.get(activityNdx)).mAppAnimator.clearThumbnail();
        }
    }

    boolean showForAllUsers() {
        int tokensCount = this.mAppTokens.size();
        if (tokensCount != 0) {
            return ((AppWindowToken) this.mAppTokens.get(tokensCount - 1)).showForAllUsers;
        }
        return false;
    }

    boolean isVisibleForUser() {
        for (int i = this.mAppTokens.size() - 1; i >= 0; i--) {
            AppWindowToken appToken = (AppWindowToken) this.mAppTokens.get(i);
            for (int j = appToken.allAppWindows.size() - 1; j >= 0; j--) {
                if (!((WindowState) appToken.allAppWindows.get(j)).isHiddenFromUserLocked()) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isVisible() {
        for (int i = this.mAppTokens.size() - 1; i >= 0; i--) {
            if (((AppWindowToken) this.mAppTokens.get(i)).isVisible()) {
                return true;
            }
        }
        return false;
    }

    boolean inHomeStack() {
        return this.mStack != null && this.mStack.mStackId == 0;
    }

    boolean inFreeformWorkspace() {
        return this.mStack != null && this.mStack.mStackId == BOUNDS_CHANGE_SIZE;
    }

    boolean inDockedWorkspace() {
        return this.mStack != null && this.mStack.mStackId == 3;
    }

    boolean inPinnedWorkspace() {
        return this.mStack != null && this.mStack.mStackId == 4;
    }

    boolean isResizeableByDockedStack() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null || displayContent.getDockedStackLocked() == null || this.mStack == null) {
            return false;
        }
        return StackId.isTaskResizeableByDockedStack(this.mStack.mStackId);
    }

    boolean isFloating() {
        return StackId.tasksAreFloating(this.mStack.mStackId);
    }

    boolean isDockedInEffect() {
        return !inDockedWorkspace() ? isResizeableByDockedStack() : true;
    }

    boolean isTwoFingerScrollMode() {
        return inCropWindowsResizeMode() ? isDockedInEffect() : false;
    }

    WindowState getTopVisibleAppMainWindow() {
        AppWindowToken token = getTopVisibleAppToken();
        if (token != null) {
            return token.findMainWindow();
        }
        return null;
    }

    AppWindowToken getTopVisibleAppToken() {
        for (int i = this.mAppTokens.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) this.mAppTokens.get(i);
            if (!token.mIsExiting && !token.clientHidden && !token.hiddenRequested) {
                return token;
            }
        }
        return null;
    }

    AppWindowToken getTopAppToken() {
        return this.mAppTokens.size() > 0 ? (AppWindowToken) this.mAppTokens.get(this.mAppTokens.size() - 1) : null;
    }

    public boolean dimFullscreen() {
        return !isHomeTask() ? isFullscreen() : true;
    }

    boolean isFullscreen() {
        if (useCurrentBounds()) {
            return this.mFullscreen;
        }
        return true;
    }

    public DisplayInfo getDisplayInfo() {
        return this.mStack.getDisplayContent().getDisplayInfo();
    }

    public String toString() {
        return "{taskId=" + this.mTaskId + " appTokens=" + this.mAppTokens + " mdr=" + this.mDeferRemoval + "}";
    }

    public String toShortString() {
        return "Task=" + this.mTaskId;
    }

    public void dump(String prefix, PrintWriter pw) {
        String doublePrefix = prefix + "  ";
        pw.println(prefix + "taskId=" + this.mTaskId);
        pw.println(doublePrefix + "mFullscreen=" + this.mFullscreen);
        pw.println(doublePrefix + "mBounds=" + this.mBounds.toShortString());
        pw.println(doublePrefix + "mdr=" + this.mDeferRemoval);
        pw.println(doublePrefix + "appTokens=" + this.mAppTokens);
        pw.println(doublePrefix + "mTempInsetBounds=" + this.mTempInsetBounds.toShortString());
        String triplePrefix = doublePrefix + "  ";
        for (int i = this.mAppTokens.size() - 1; i >= 0; i--) {
            AppWindowToken wtoken = (AppWindowToken) this.mAppTokens.get(i);
            pw.println(triplePrefix + "Activity #" + i + " " + wtoken);
            wtoken.dump(pw, triplePrefix);
        }
    }
}
