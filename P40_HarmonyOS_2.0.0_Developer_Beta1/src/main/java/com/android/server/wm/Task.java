package com.android.server.wm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import android.view.animation.PathInterpolator;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ToBooleanFunction;
import com.huawei.android.app.HwActivityManager;
import com.huawei.anim.dynamicanimation.interpolator.SpringInterpolator;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
public class Task extends WindowContainer<AppWindowToken> implements ConfigurationContainerListener {
    private static final long DEFAULT_DURATION = 400;
    private static final long HORIZONTAL_DURATION = 500;
    static final String TAG = "WindowManager";
    private static final long VERTICAL_DURATION = 300;
    private final int CONST_NUM_FOUR = 4;
    private final float PARAMS_DAMPING = ((float) Math.sqrt(3200.0d));
    private final float PARAMS_STIFFNESS = 800.0f;
    private boolean animRunningflag = false;
    private boolean isNarrowAnim = true;
    private boolean mCanAffectSystemUiFlags = true;
    private boolean mDeferRemoval = false;
    private Dimmer mDimmer = new Dimmer(this);
    private int mDragResizeMode;
    private boolean mDragResizing;
    private SurfaceControl mHwFreeFormMoveLeash;
    private SurfaceControl mHwFreeFormScaleLeash;
    private SurfaceControl.Transaction mHwFreeFormTransaction = new SurfaceControl.Transaction();
    private int mLastRotationDisplayId = -1;
    private float mMaxScale = 0.87f;
    private float mMinScale = 0.35f;
    private final Rect mOverrideDisplayedBounds = new Rect();
    final Rect mPreparedFrozenBounds = new Rect();
    final Configuration mPreparedFrozenMergedConfig = new Configuration();
    private boolean mPreserveNonFloatingState = false;
    private int mResizeMode;
    private int mRotation;
    TaskStack mStack;
    private boolean mSupportsPictureInPicture;
    private ActivityManager.TaskDescription mTaskDescription;
    final int mTaskId;
    TaskRecord mTaskRecord;
    private final Rect mTmpDimBoundsRect = new Rect();
    private Rect mTmpRect = new Rect();
    private Rect mTmpRect2 = new Rect();
    private Rect mTmpRect3 = new Rect();
    final int mUserId;
    private ValueAnimator mValueAnimator;
    private final int scaleDuration = 300;
    private ValueAnimator widthAnimator;

    Task(int taskId, TaskStack stack, int userId, WindowManagerService service, int resizeMode, boolean supportsPictureInPicture, ActivityManager.TaskDescription taskDescription, TaskRecord taskRecord) {
        super(service);
        this.mTaskId = taskId;
        this.mStack = stack;
        this.mUserId = userId;
        this.mResizeMode = resizeMode;
        this.mSupportsPictureInPicture = supportsPictureInPicture;
        this.mTaskRecord = taskRecord;
        TaskRecord taskRecord2 = this.mTaskRecord;
        if (taskRecord2 != null) {
            taskRecord2.registerConfigurationChangeListener(this);
        }
        setBounds(getRequestedOverrideBounds());
        this.mTaskDescription = taskDescription;
        setOrientation(-2);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public DisplayContent getDisplayContent() {
        TaskStack taskStack = this.mStack;
        if (taskStack != null) {
            return taskStack.getDisplayContent();
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
        super.addChild((Task) wtoken, getAdjustedAddPosition(position));
        this.mDeferRemoval = false;
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, AppWindowToken child, boolean includingParents) {
        super.positionChildAt(getAdjustedAddPosition(position), (int) child, includingParents);
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
    @Override // com.android.server.wm.WindowContainer
    public void removeIfPossible() {
        if (shouldDeferRemoval()) {
            this.mDeferRemoval = true;
        } else {
            removeImmediately();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeImmediately() {
        EventLog.writeEvent(31003, Integer.valueOf(this.mTaskId), "removeTask");
        this.mDeferRemoval = false;
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord != null) {
            taskRecord.unregisterConfigurationChangeListener(this);
        }
        super.removeImmediately();
    }

    /* access modifiers changed from: package-private */
    public void reparent(TaskStack stack, int position, boolean moveParents) {
        if (stack == this.mStack) {
            throw new IllegalArgumentException("task=" + this + " already child of stack=" + this.mStack);
        } else if (stack != null) {
            EventLog.writeEvent(31003, Integer.valueOf(this.mTaskId), "reParentTask");
            DisplayContent prevDisplayContent = getDisplayContent();
            if (stack.inPinnedWindowingMode()) {
                this.mPreserveNonFloatingState = true;
            } else {
                this.mPreserveNonFloatingState = false;
            }
            try {
                getParent().removeChild(this);
                stack.addTask(this, position, showForAllUsers(), moveParents);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "removeChild: container=" + getName() + " is not a child of container=" + getParent().getName());
            } catch (IllegalStateException e2) {
                Slog.e(TAG, "Trying to add taskId=" + this.mTaskId + " to stackId=" + stack.mStackId + ", but it is already attached to stackId=" + this.mStack.mStackId);
            } catch (Exception e3) {
                Slog.e(TAG, "Exception:", e3);
                throw e3;
            }
            Slog.i(TAG, "reParentTask: removing taskId=" + this.mTaskId + " from stack=" + this.mStack + " to stack=" + stack);
            DisplayContent displayContent = stack.getDisplayContent();
            displayContent.setLayoutNeeded();
            if (prevDisplayContent != displayContent) {
                onDisplayChanged(displayContent);
                prevDisplayContent.setLayoutNeeded();
            }
            getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
        } else {
            throw new IllegalArgumentException("reparent: could not find stack.");
        }
    }

    /* access modifiers changed from: package-private */
    public void positionAt(int position) {
        this.mStack.positionChildAt(position, this, false);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onParentChanged() {
        super.onParentChanged();
        adjustBoundsForDisplayChangeIfNeeded(getDisplayContent());
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
        super.removeChild((Task) token);
        if (this.mChildren.isEmpty()) {
            EventLog.writeEvent(31003, Integer.valueOf(this.mTaskId), "removeAppToken: last token");
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
        return boundsChanged | 2;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public int setBounds(Rect bounds) {
        int rotation = 0;
        DisplayContent displayContent = this.mStack.getDisplayContent();
        if (displayContent != null) {
            rotation = displayContent.getDisplayInfo().rotation;
        } else if (bounds == null) {
            return super.setBounds(bounds);
        }
        if (equivalentRequestedOverrideBounds(bounds) && this.mRotation == rotation) {
            return super.setBounds(bounds);
        }
        int boundsChange = super.setBounds(bounds);
        this.mRotation = rotation;
        updateSurfacePosition();
        return boundsChange;
    }

    @Override // com.android.server.wm.WindowContainer
    public boolean onDescendantOrientationChanged(IBinder freezeDisplayToken, ConfigurationContainer requestingContainer) {
        if (super.onDescendantOrientationChanged(freezeDisplayToken, requestingContainer)) {
            return true;
        }
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null || taskRecord.getParent() == null) {
            return false;
        }
        TaskRecord taskRecord2 = this.mTaskRecord;
        taskRecord2.onConfigurationChanged(taskRecord2.getParent().getConfiguration());
        return true;
    }

    /* access modifiers changed from: package-private */
    public void resize(boolean relayout, boolean forced) {
        if (setBounds(getRequestedOverrideBounds(), forced) != 0 && relayout) {
            getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onDisplayChanged(DisplayContent dc) {
        adjustBoundsForDisplayChangeIfNeeded(dc);
        super.onDisplayChanged(dc);
        this.mWmService.mAtmService.getTaskChangeNotificationController().notifyTaskDisplayChanged(this.mTaskId, dc != null ? dc.getDisplayId() : -1);
    }

    /* access modifiers changed from: package-private */
    public void setOverrideDisplayedBounds(Rect overrideDisplayedBounds) {
        if (overrideDisplayedBounds != null) {
            this.mOverrideDisplayedBounds.set(overrideDisplayedBounds);
        } else {
            this.mOverrideDisplayedBounds.setEmpty();
        }
        updateSurfacePosition();
    }

    /* access modifiers changed from: package-private */
    public Rect getOverrideDisplayedBounds() {
        return this.mOverrideDisplayedBounds;
    }

    /* access modifiers changed from: package-private */
    public void setResizeable(int resizeMode) {
        this.mResizeMode = resizeMode;
    }

    /* access modifiers changed from: package-private */
    public boolean isResizeable() {
        String str;
        IHwActivityTaskManagerServiceEx iHwActivityTaskManagerServiceEx = this.mWmService.mAtmService.mHwATMSEx;
        TaskRecord taskRecord = this.mTaskRecord;
        if (taskRecord == null || taskRecord.realActivity == null) {
            str = null;
        } else {
            str = this.mTaskRecord.realActivity.getPackageName();
        }
        return iHwActivityTaskManagerServiceEx.isResizableApp(str, this.mResizeMode) || this.mSupportsPictureInPicture || this.mWmService.mForceResizableTasks;
    }

    /* access modifiers changed from: package-private */
    public boolean preserveOrientationOnResize() {
        int i = this.mResizeMode;
        return i == 6 || i == 5 || i == 7;
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
        if (isResizeable() && !Configuration.EMPTY.equals(getRequestedOverrideConfiguration())) {
            getBounds(this.mTmpRect2);
            if (alignBottom) {
                this.mTmpRect2.offset(0, adjustedBounds.bottom - this.mTmpRect2.bottom);
            } else {
                this.mTmpRect2.offsetTo(adjustedBounds.left, adjustedBounds.top);
            }
            if (tempInsetBounds == null || tempInsetBounds.isEmpty()) {
                setOverrideDisplayedBounds(null);
                setBounds(this.mTmpRect2);
                return;
            }
            setOverrideDisplayedBounds(this.mTmpRect2);
            setBounds(tempInsetBounds);
        }
    }

    @Override // com.android.server.wm.WindowContainer
    public Rect getDisplayedBounds() {
        if (this.mOverrideDisplayedBounds.isEmpty()) {
            return super.getDisplayedBounds();
        }
        return this.mOverrideDisplayedBounds;
    }

    private boolean getMaxVisibleBounds(Rect out) {
        WindowState win;
        boolean foundTop = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) this.mChildren.get(i);
            if (!token.mIsExiting && !token.isClientHidden() && !token.hiddenRequested && (win = token.findMainWindow()) != null) {
                if (!foundTop) {
                    foundTop = true;
                    out.setEmpty();
                }
                win.getMaxVisibleBounds(out);
            }
        }
        return foundTop;
    }

    public void getDimBounds(Rect out) {
        TaskStack taskStack = this.mStack;
        if (taskStack != null) {
            DisplayContent displayContent = taskStack.getDisplayContent();
            boolean dockedResizing = displayContent != null && displayContent.mDividerControllerLocked.isResizing();
            if ((HwPCUtils.isExtDynamicStack(this.mStack.mStackId) && matchParentBounds()) || ((!inFreeformWindowingMode() && !inHwPCFreeformWindowingMode() && getWindowingMode() != 102) || !getMaxVisibleBounds(out))) {
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
        if (!dragResizing || DragResizeMode.isModeAllowedForStack(this.mStack, dragResizeMode)) {
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

    public void setTaskDockedResizing(boolean resizing) {
        setDragResizing(resizing, 1);
    }

    private void adjustBoundsForDisplayChangeIfNeeded(DisplayContent displayContent) {
        TaskRecord taskRecord;
        if (displayContent != null) {
            if (matchParentBounds()) {
                setBounds(null);
                return;
            }
            int displayId = displayContent.getDisplayId();
            int newRotation = displayContent.getDisplayInfo().rotation;
            if (displayId != this.mLastRotationDisplayId) {
                this.mLastRotationDisplayId = displayId;
                this.mRotation = newRotation;
            } else if (this.mRotation != newRotation) {
                this.mTmpRect2.set(getBounds());
                if (!getWindowConfiguration().canResizeTask()) {
                    setBounds(this.mTmpRect2);
                    return;
                }
                displayContent.rotateBounds(this.mRotation, newRotation, this.mTmpRect2);
                if (HwFreeFormUtils.isFreeFormEnable() && inFreeformWindowingMode() && this.mTmpRect2.top < 0) {
                    Rect rect = this.mTmpRect2;
                    rect.offsetTo(rect.left, this.mWmService.mContext.getResources().getDimensionPixelSize(17105445));
                    HwFreeFormUtils.log(TAG, "avoid bounds conflict freeform offset to " + this.mTmpRect2);
                }
                if (setBounds(this.mTmpRect2) != 0 && (taskRecord = this.mTaskRecord) != null) {
                    taskRecord.requestResize(getBounds(), 1);
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
        WindowState windowState;
        int uid = -1;
        boolean ret = true;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken tmpToken = (AppWindowToken) this.mChildren.get(i);
            if (!tmpToken.isEmpty() && (windowState = (WindowState) tmpToken.mChildren.peekLast()) != null) {
                if (uid == -1) {
                    uid = windowState.mOwnerUid;
                } else {
                    ret = uid == windowState.mOwnerUid;
                }
                if (!ret) {
                    return ret;
                }
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

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public SurfaceControl getAnimationLeashParent() {
        return getAppAnimationLayer(2);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public SurfaceControl.Builder makeSurface() {
        return super.makeSurface().setMetadata(3, this.mTaskId);
    }

    /* access modifiers changed from: package-private */
    public boolean isTaskAnimating() {
        RecentsAnimationController recentsAnim = this.mWmService.getRecentsAnimationController();
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
            if (!(token.mIsExiting || token.isClientHidden() || token.hiddenRequested)) {
                return token;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void positionChildAtTop(AppWindowToken aToken) {
        positionChildAt(aToken, Integer.MAX_VALUE);
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(AppWindowToken aToken, int position) {
        if (aToken == null) {
            Slog.w(TAG, "Attempted to position of non-existing app");
        } else {
            positionChildAt(position, aToken, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void forceWindowsScaleable(boolean force) {
        this.mWmService.openSurfaceTransaction();
        try {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                ((AppWindowToken) this.mChildren.get(i)).forceWindowsScaleableInTransaction(force);
            }
        } finally {
            this.mWmService.closeSurfaceTransaction("forceWindowsScaleable");
        }
    }

    /* access modifiers changed from: package-private */
    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
        this.mTaskDescription = taskDescription;
    }

    /* access modifiers changed from: package-private */
    public void onSnapshotChanged(ActivityManager.TaskSnapshot snapshot) {
        this.mTaskRecord.onSnapshotChanged(snapshot);
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskDescription getTaskDescription() {
        return this.mTaskDescription;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean fillsParent() {
        return matchParentBounds() || !getWindowConfiguration().canResizeTask();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
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

    @Override // java.lang.Object
    public String toString() {
        return "{taskId=" + this.mTaskId + " appTokens=" + this.mChildren + " mdr=" + this.mDeferRemoval + "}";
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public String getName() {
        return toShortString();
    }

    /* access modifiers changed from: package-private */
    public void clearPreserveNonFloatingState() {
        this.mPreserveNonFloatingState = false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public Dimmer getDimmer() {
        return this.mDimmer;
    }

    private void fixDimBoundsFreeform() {
        boolean isChildrenExist = true;
        if (!((getDisplayContent() == null || getDisplayContent().mInputMethodTarget == null || !hasChild(getDisplayContent().mInputMethodTarget.mAppToken)) ? false : true)) {
            this.mTmpDimBoundsRect.offsetTo(0, 0);
            return;
        }
        AppWindowToken appWindowToken = getTopVisibleAppToken();
        if (appWindowToken == null || appWindowToken.getTopChild() == null) {
            isChildrenExist = false;
        }
        if (!isChildrenExist) {
            this.mTmpDimBoundsRect.offsetTo(0, 0);
            return;
        }
        Rect outerBound = new Rect(getBounds());
        Iterator it = appWindowToken.mChildren.iterator();
        while (it.hasNext()) {
            Rect targetFrame = ((WindowState) it.next()).mWindowFrames.mFrame;
            outerBound.left = Math.min(outerBound.left, targetFrame.left);
            outerBound.top = Math.min(outerBound.top, targetFrame.top);
        }
        this.mTmpDimBoundsRect.offsetTo(outerBound.left - getBounds().left, outerBound.top - getBounds().top);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void prepareSurfaces() {
        this.mDimmer.resetDimStates();
        super.prepareSurfaces();
        getDimBounds(this.mTmpDimBoundsRect);
        if (inHwFreeFormWindowingMode()) {
            fixDimBoundsFreeform();
        } else {
            this.mTmpDimBoundsRect.offsetTo(0, 0);
        }
        if (this.mDimmer.updateDims(getPendingTransaction(), this.mTmpDimBoundsRect)) {
            if (this.mStack.mActivityStack.inHwFreeFormWindowingMode() && !this.mStack.mActivityStack.inHwPCMultiStackWindowingMode()) {
                this.mDimmer.updateDimsCornerRadius(getPendingTransaction(), (float) this.mStack.mActivityStack.mService.mContext.getResources().getDimensionPixelSize(34472614));
            }
            if (this.mStack.mActivityStack.inHwSplitScreenWindowingMode()) {
                this.mDimmer.updateDimsCornerRadius(getPendingTransaction(), HwActivityManager.IS_PHONE ? 0.0f : (float) this.mStack.mActivityStack.mService.mContext.getResources().getDimensionPixelSize(34472617));
            }
            scheduleAnimation();
        }
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        if (logLevel != 2 || isVisible()) {
            long token = proto.start(fieldId);
            super.writeToProto(proto, 1146756268033L, logLevel);
            proto.write(1120986464258L, this.mTaskId);
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                ((AppWindowToken) this.mChildren.get(i)).writeToProto(proto, 2246267895811L, logLevel);
            }
            proto.write(1133871366148L, matchParentBounds());
            getBounds().writeToProto(proto, 1146756268037L);
            this.mOverrideDisplayedBounds.writeToProto(proto, 1146756268038L);
            proto.write(1133871366151L, this.mDeferRemoval);
            proto.write(1120986464264L, this.mSurfaceControl.getWidth());
            proto.write(1120986464265L, this.mSurfaceControl.getHeight());
            proto.end(token);
        }
    }

    @Override // com.android.server.wm.WindowContainer
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        super.dump(pw, prefix, dumpAll);
        String doublePrefix = prefix + "  ";
        pw.println(prefix + "taskId=" + this.mTaskId);
        pw.println(doublePrefix + "mBounds=" + getBounds().toShortString());
        pw.println(doublePrefix + "mdr=" + this.mDeferRemoval);
        pw.println(doublePrefix + "appTokens=" + this.mChildren);
        pw.println(doublePrefix + "mDisplayedBounds=" + this.mOverrideDisplayedBounds.toShortString());
        String triplePrefix = doublePrefix + "  ";
        String quadruplePrefix = triplePrefix + "  ";
        for (int i = this.mChildren.size() - 1; i >= 0; i += -1) {
            AppWindowToken wtoken = (AppWindowToken) this.mChildren.get(i);
            pw.println(triplePrefix + "Activity #" + i + " " + wtoken);
            wtoken.dump(pw, quadruplePrefix, dumpAll);
        }
    }

    /* access modifiers changed from: package-private */
    public String toShortString() {
        return "Task=" + this.mTaskId;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.WindowContainer
    public void onAnimationFinished() {
        super.onAnimationFinished();
        Slog.i(TAG, "onAnimationFinished task=" + this.mTaskId);
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                    new ArrayList<>(((AppWindowToken) this.mChildren.get(i)).mChildren).forEach($$Lambda$01bPtngJg5AqEoOWfW3rWfV7MH4.INSTANCE);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void initHwFreeFormScaleLeash(Rect startBounds) {
        Slog.i(TAG, "initHwFreeFormScaleLeash startBounds: " + startBounds);
        SurfaceControl.Builder parent = makeAnimationLeash().setParent(getAppAnimationLayer(0));
        this.mHwFreeFormScaleLeash = parent.setName(getSurfaceControl() + " - hwfreeform-scale-leash").build();
        this.mHwFreeFormScaleLeash.setRelativeLayer(getParentSurfaceControl(), 0);
        this.mHwFreeFormTransaction.setPosition(this.mHwFreeFormScaleLeash, (float) startBounds.left, (float) startBounds.top);
        this.mHwFreeFormTransaction.show(this.mHwFreeFormScaleLeash);
        this.mHwFreeFormTransaction.reparent(getSurfaceControl(), this.mHwFreeFormScaleLeash);
        getDisplayContent().assignStackOrdering(this.mHwFreeFormTransaction);
        this.mHwFreeFormTransaction.apply();
    }

    /* access modifiers changed from: package-private */
    public void updateHwFreeFormScaleLeash(int pointX, int pointY, float scale) {
        this.mHwFreeFormTransaction.setPosition(this.mHwFreeFormScaleLeash, (float) pointX, (float) pointY);
        this.mHwFreeFormTransaction.setMatrix(this.mHwFreeFormScaleLeash, scale, 0.0f, 0.0f, scale);
        this.mHwFreeFormTransaction.apply();
    }

    /* access modifiers changed from: package-private */
    public void destroyHwFreeFormScaleLeash(float scale) {
        if (this.mHwFreeFormScaleLeash != null && this.mStack != null) {
            Slog.i(TAG, "destroyHwFreeFormScaleLeash scale: " + scale);
            this.mStack.mHwStackScale = scale;
            SurfaceControl sc = getSurfaceControl();
            if (!(getParentSurfaceControl() == null || sc == null)) {
                this.mHwFreeFormTransaction.reparent(sc, getParentSurfaceControl());
            }
            this.mHwFreeFormTransaction.hide(this.mHwFreeFormScaleLeash);
            this.mHwFreeFormTransaction.remove(this.mHwFreeFormScaleLeash);
            updateHwFreeFormTaskDims(this.mHwFreeFormTransaction);
            adjustHwFreeFormWindowScale();
            this.mHwFreeFormTransaction.apply();
            this.mHwFreeFormScaleLeash = null;
            getDisplayContent().updateTouchExcludeRegion();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isHwFreeFormScaleAnimating() {
        return this.mHwFreeFormScaleLeash != null;
    }

    /* access modifiers changed from: package-private */
    public void adjustHwFreeFormWindowScale() {
        forAllWindows((ToBooleanFunction<WindowState>) new ToBooleanFunction() {
            /* class com.android.server.wm.$$Lambda$Task$UKFx5utA2apnGLAcL5lBp8QWX3w */

            public final boolean apply(Object obj) {
                return Task.this.lambda$adjustHwFreeFormWindowScale$0$Task((WindowState) obj);
            }
        }, true);
    }

    public /* synthetic */ boolean lambda$adjustHwFreeFormWindowScale$0$Task(WindowState win) {
        if ((win != null && win.isVisibleNow() && win.isInputMethodWindow()) || win.mWinAnimator == null || win.mWinAnimator.mSurfaceController == null || this.mStack == null) {
            return false;
        }
        win.updateSurfacePosition(this.mHwFreeFormTransaction);
        win.mWinAnimator.mSurfaceController.setMatrix(this.mHwFreeFormTransaction, this.mStack.mHwStackScale * win.mWinAnimator.mDsDx * win.mHScale * win.mWinAnimator.mExtraHScale, 0.0f, 0.0f, this.mStack.mHwStackScale * win.mWinAnimator.mDsDx * win.mHScale * win.mWinAnimator.mExtraHScale, false);
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateHwFreeFormTaskDims(SurfaceControl.Transaction t) {
        getDimBounds(this.mTmpDimBoundsRect);
        fixDimBoundsFreeform();
        this.mDimmer.updateDims(t, this.mTmpDimBoundsRect);
    }

    /* access modifiers changed from: package-private */
    public WindowState getTopVisibleNonPermissionAppMainWindow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken token = (AppWindowToken) this.mChildren.get(i);
            if (!(token.mIsExiting || token.isClientHidden() || token.hiddenRequested || token.isPermissionApp())) {
                return token.findMainWindow();
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void startReboundScaleAnimation(final Rect startBounds, final Rect stopBounds, final int ctrlType, float[] mScale, final boolean reboundIsOutOfScreen) {
        ValueAnimator valueAnimator = this.widthAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            Slog.i(TAG, "startReboundScaleAnimation, animator is running.");
        } else if (this.mStack == null) {
            Slog.i(TAG, "stack is null.");
        } else {
            final float mCurScale = mScale[0];
            final float mInitialScale = mScale[1];
            float[] scale = this.mWmService.mAtmService.mHwATMSEx.getScaleRange(this.mStack.mActivityStack);
            if (scale[0] > 1.0E-6f) {
                this.mMinScale = scale[0];
                this.mMaxScale = scale[1];
            }
            reboundModeSelect(mCurScale, mInitialScale, startBounds);
            this.widthAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.widthAnimator.setInterpolator(new SpringInterpolator(800.0f, this.PARAMS_DAMPING));
            this.widthAnimator.setDuration(VERTICAL_DURATION);
            this.widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.server.wm.Task.AnonymousClass1 */

                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator animation) {
                    synchronized (Task.this.mWmService.getGlobalLock()) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            Task.this.animRunningflag = true;
                            float animTempScale = ((Float) animation.getAnimatedValue()).floatValue();
                            if (Task.this.mHwFreeFormScaleLeash == null) {
                                Task.this.widthAnimator.cancel();
                                return;
                            }
                            Task.this.setReboundPosition(ctrlType, reboundIsOutOfScreen, startBounds, stopBounds, animTempScale);
                            Task.this.setReboundMatrix(mCurScale, mInitialScale, Task.this.mMaxScale, Task.this.mMinScale, animTempScale);
                            Task.this.mHwFreeFormTransaction.apply();
                            WindowManagerService.resetPriorityAfterLockedSection();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            });
            this.widthAnimator.addListener(new AnimatorListenerAdapter() {
                /* class com.android.server.wm.Task.AnonymousClass2 */

                /* JADX INFO: finally extract failed */
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    synchronized (Task.this.mWmService.getGlobalLock()) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            Task.this.destroyHwFreeFormScaleLeash(mCurScale, mInitialScale);
                            Task.this.animRunningflag = false;
                        } catch (Throwable th) {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    Task.this.mWmService.mAtmService.mHwATMSEx.updateFreeFormOutLine(-2);
                }
            });
            Handler handler = this.mWmService.mAnimationHandler;
            ValueAnimator valueAnimator2 = this.widthAnimator;
            Objects.requireNonNull(valueAnimator2);
            handler.post(new Runnable(valueAnimator2) {
                /* class com.android.server.wm.$$Lambda$ormGphh9OtfOhs5ymFeF5N5rzg */
                private final /* synthetic */ ValueAnimator f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.start();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void setReboundPosition(int ctrlType, boolean reboundIsOutOfScreen, Rect startBounds, Rect stopBounds, float animTempScale) {
        int deltaWidth = (stopBounds.right - stopBounds.left) - (startBounds.right - startBounds.left);
        if (ctrlType != 1) {
            if (ctrlType != 2) {
                switch (ctrlType) {
                    case 8:
                        if (reboundIsOutOfScreen) {
                            this.mHwFreeFormTransaction.setPosition(this.mHwFreeFormScaleLeash, (float) (startBounds.left + ((int) (((float) (stopBounds.left - startBounds.left)) * animTempScale))), (float) startBounds.top);
                            return;
                        } else {
                            this.mHwFreeFormTransaction.setPosition(this.mHwFreeFormScaleLeash, (float) (startBounds.left - ((int) ((((float) deltaWidth) * animTempScale) / 2.0f))), (float) startBounds.top);
                            return;
                        }
                    case 9:
                        break;
                    case 10:
                        break;
                    default:
                        this.mHwFreeFormTransaction.setPosition(this.mHwFreeFormScaleLeash, (float) (startBounds.left + ((int) (((float) (stopBounds.left - startBounds.left)) * animTempScale))), (float) startBounds.top);
                        return;
                }
            }
            if (reboundIsOutOfScreen) {
                this.mHwFreeFormTransaction.setPosition(this.mHwFreeFormScaleLeash, (float) (startBounds.left + ((int) (((float) (stopBounds.left - startBounds.left)) * animTempScale))), (float) startBounds.top);
                return;
            } else {
                this.mHwFreeFormTransaction.setPosition(this.mHwFreeFormScaleLeash, (float) startBounds.left, (float) startBounds.top);
                return;
            }
        }
        if (reboundIsOutOfScreen) {
            this.mHwFreeFormTransaction.setPosition(this.mHwFreeFormScaleLeash, (float) (startBounds.left + ((int) (((float) (stopBounds.left - startBounds.left)) * animTempScale))), (float) startBounds.top);
        } else {
            this.mHwFreeFormTransaction.setPosition(this.mHwFreeFormScaleLeash, (float) (startBounds.left - ((int) (((float) deltaWidth) * animTempScale))), (float) startBounds.top);
        }
    }

    /* access modifiers changed from: package-private */
    public void reboundModeSelect(float mCurScale, float mInitialScale, Rect startBounds) {
        if (mCurScale * mInitialScale < this.mMinScale) {
            this.isNarrowAnim = true;
        } else {
            this.isNarrowAnim = false;
        }
        if (mCurScale == 1.0f && this.mHwFreeFormScaleLeash == null) {
            initHwFreeFormScaleLeash(startBounds);
            this.mStack.mHwStackScale = mInitialScale;
        }
    }

    /* access modifiers changed from: package-private */
    public void setReboundMatrix(float curScale, float initialScale, float maxScale, float minScale, float animScale) {
        if (curScale == 1.0f) {
            return;
        }
        if (curScale * initialScale >= maxScale) {
            this.mHwFreeFormTransaction.setMatrix(this.mHwFreeFormScaleLeash, curScale - ((curScale - (maxScale / initialScale)) * animScale), 0.0f, 0.0f, curScale - ((curScale - (maxScale / initialScale)) * animScale));
        } else if (curScale * initialScale <= minScale) {
            this.mHwFreeFormTransaction.setMatrix(this.mHwFreeFormScaleLeash, curScale + (((minScale / initialScale) - curScale) * animScale), 0.0f, 0.0f, curScale + (((minScale / initialScale) - curScale) * animScale));
        }
    }

    /* access modifiers changed from: package-private */
    public void destroyHwFreeFormScaleLeash(float mCurScale, float mInitialScale) {
        if (mCurScale * mInitialScale <= this.mMaxScale && mCurScale * mInitialScale >= this.mMinScale) {
            destroyHwFreeFormScaleLeash(mCurScale * mInitialScale);
        } else if (this.isNarrowAnim) {
            destroyHwFreeFormScaleLeash(this.mMinScale);
        } else {
            destroyHwFreeFormScaleLeash(this.mMaxScale);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getAnimRunningFlag() {
        return this.animRunningflag;
    }

    /* access modifiers changed from: package-private */
    public boolean startMoveAnimation(final int taskId, final int windowingMode, final Rect endBounds, final float scale) {
        if (this.mHwFreeFormMoveLeash != null) {
            Slog.i(TAG, "startMoveAnimation, animator is running.");
            return false;
        }
        Slog.i(TAG, "startMoveAnimation task:" + this.mTaskId);
        SurfaceControl.Builder parent = makeAnimationLeash().setParent(getAppAnimationLayer(11));
        this.mHwFreeFormMoveLeash = parent.setName(getSurfaceControl() + " - hwfreeform-move-leash").build();
        Rect startBounds = getBounds();
        final int startLeft = startBounds.left;
        final int startTop = startBounds.top;
        this.mHwFreeFormTransaction.setPosition(this.mHwFreeFormMoveLeash, (float) startLeft, (float) startTop);
        this.mHwFreeFormTransaction.show(this.mHwFreeFormMoveLeash);
        this.mHwFreeFormTransaction.reparent(getSurfaceControl(), this.mHwFreeFormMoveLeash);
        this.mHwFreeFormTransaction.apply();
        this.mStack.mActivityStack.resize(endBounds, null, null);
        this.mValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.wm.Task.AnonymousClass3 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                int endLeft = endBounds.left;
                int endTop = endBounds.top;
                synchronized (Task.this.mWmService.getGlobalLock()) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        Task.this.mHwFreeFormTransaction.setPosition(Task.this.mHwFreeFormMoveLeash, ((float) startLeft) + (((float) (endLeft - startLeft)) * ((Float) animation.getAnimatedValue()).floatValue()), ((float) startTop) + (((float) (endTop - startTop)) * ((Float) animation.getAnimatedValue()).floatValue()));
                        Task.this.mHwFreeFormTransaction.apply();
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        });
        this.mValueAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.server.wm.Task.AnonymousClass4 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                Slog.i(Task.TAG, "startMoveAnimation onAnimationEnd");
                super.onAnimationEnd(animation);
                synchronized (Task.this.mWmService.getGlobalLock()) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        SurfaceControl sc = Task.this.getSurfaceControl();
                        if (!(Task.this.getParentSurfaceControl() == null || sc == null)) {
                            Task.this.mHwFreeFormTransaction.reparent(sc, Task.this.getParentSurfaceControl());
                        }
                        Task.this.mHwFreeFormTransaction.hide(Task.this.mHwFreeFormMoveLeash);
                        Task.this.mHwFreeFormTransaction.remove(Task.this.mHwFreeFormMoveLeash);
                        Task.this.mHwFreeFormTransaction.apply();
                        Task.this.mHwFreeFormMoveLeash = null;
                        Task.this.mWmService.mAtmService.mHwATMSEx.notifyWindowStateChange("first", "onAnimationFinished", windowingMode, endBounds, scale, taskId, "", 0, null, 0.0f, -1);
                        Slog.i(Task.TAG, "Notify onAnimationEnd");
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        });
        this.mValueAnimator.setInterpolator(new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f));
        this.mValueAnimator.setDuration(calDuration(startBounds, endBounds));
        Handler handler = this.mWmService.mAnimationHandler;
        ValueAnimator valueAnimator = this.mValueAnimator;
        Objects.requireNonNull(valueAnimator);
        handler.post(new Runnable(valueAnimator) {
            /* class com.android.server.wm.$$Lambda$ormGphh9OtfOhs5ymFeF5N5rzg */
            private final /* synthetic */ ValueAnimator f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.start();
            }
        });
        return true;
    }

    private long calDuration(Rect startBounds, Rect endBounds) {
        int startLeft = startBounds.left;
        int startTop = startBounds.top;
        int endLeft = endBounds.left;
        int endTop = endBounds.top;
        long duration = DEFAULT_DURATION;
        if (startLeft == endLeft) {
            duration = VERTICAL_DURATION;
        } else if (startTop == endTop) {
            duration = HORIZONTAL_DURATION;
        }
        Slog.i(TAG, "calDuration: " + duration);
        return duration;
    }
}
