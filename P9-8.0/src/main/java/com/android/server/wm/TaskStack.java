package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.os.RemoteException;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.view.DisplayInfo;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DockedDividerUtils;
import com.android.server.EventLogTags;
import java.io.PrintWriter;
import java.util.function.Consumer;

public class TaskStack extends AbsTaskStack implements DimLayerUser, BoundsAnimationTarget {
    private static final float ADJUSTED_STACK_FRACTION_MIN = 0.3f;
    private static final float IME_ADJUST_DIM_AMOUNT = 0.25f;
    private float mAdjustDividerAmount;
    private float mAdjustImeAmount;
    private final Rect mAdjustedBounds = new Rect();
    private boolean mAdjustedForIme;
    private WindowStateAnimator mAnimationBackgroundAnimator;
    private DimLayer mAnimationBackgroundSurface;
    private Rect mBounds = new Rect();
    private final Rect mBoundsAfterRotation = new Rect();
    private boolean mBoundsAnimating = false;
    private boolean mBoundsAnimatingRequested = false;
    private boolean mBoundsAnimatingToFullscreen = false;
    private Rect mBoundsAnimationSourceHintBounds = new Rect();
    private Rect mBoundsAnimationTarget = new Rect();
    private boolean mCancelCurrentBoundsAnimation = false;
    boolean mDeferRemoval;
    private int mDensity;
    private DisplayContent mDisplayContent;
    private final int mDockedStackMinimizeThickness;
    final AppTokenList mExitingAppTokens = new AppTokenList();
    private boolean mFillsParent = true;
    private final Rect mFullyAdjustedImeBounds = new Rect();
    private boolean mImeGoingAway;
    private WindowState mImeWin;
    private float mMinimizeAmount;
    Rect mPreAnimationBounds = new Rect();
    private int mRotation;
    private final WindowManagerService mService;
    final int mStackId;
    private final Rect mTmpAdjustedBounds = new Rect();
    final AppTokenList mTmpAppTokens = new AppTokenList();
    private Rect mTmpRect = new Rect();
    private Rect mTmpRect2 = new Rect();
    private Rect mTmpRect3 = new Rect();

    public TaskStack(WindowManagerService service, int stackId) {
        this.mService = service;
        this.mStackId = stackId;
        this.mDockedStackMinimizeThickness = service.mContext.getResources().getDimensionPixelSize(17105015);
        EventLog.writeEvent(EventLogTags.WM_STACK_CREATED, stackId);
    }

    DisplayContent getDisplayContent() {
        return this.mDisplayContent;
    }

    Task findHomeTask() {
        if (this.mStackId != 0) {
            return null;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((Task) this.mChildren.get(i)).isHomeTask()) {
                return (Task) this.mChildren.get(i);
            }
        }
        return null;
    }

    boolean hasMultipleTaskWithHomeTaskNotTop() {
        return this.mChildren.size() > 1 ? ((Task) this.mChildren.get(this.mChildren.size() - 1)).isHomeTask() ^ 1 : false;
    }

    boolean setBounds(Rect stackBounds, SparseArray<Configuration> configs, SparseArray<Rect> taskBounds, SparseArray<Rect> taskTempInsetBounds) {
        setBounds(stackBounds);
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            Task task = (Task) this.mChildren.get(taskNdx);
            Configuration config = (Configuration) configs.get(task.mTaskId);
            if (config != null) {
                Rect rect;
                task.resizeLocked((Rect) taskBounds.get(task.mTaskId), config, false);
                if (taskTempInsetBounds != null) {
                    rect = (Rect) taskTempInsetBounds.get(task.mTaskId);
                } else {
                    rect = null;
                }
                task.setTempInsetBounds(rect);
            } else {
                Slog.wtf("WindowManager", "No config for task: " + task + ", is there a mismatch with AM?");
            }
        }
        return true;
    }

    void prepareFreezingTaskBounds() {
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            ((Task) this.mChildren.get(taskNdx)).prepareFreezingBounds();
        }
    }

    private void setAdjustedBounds(Rect bounds) {
        if (!this.mAdjustedBounds.equals(bounds) || (isAnimatingForIme() ^ 1) == 0) {
            this.mAdjustedBounds.set(bounds);
            boolean adjusted = this.mAdjustedBounds.isEmpty() ^ 1;
            Rect insetBounds = null;
            if (adjusted && isAdjustedForMinimizedDockedStack()) {
                insetBounds = this.mBounds;
            } else if (adjusted && this.mAdjustedForIme) {
                insetBounds = this.mImeGoingAway ? this.mBounds : this.mFullyAdjustedImeBounds;
            }
            alignTasksToAdjustedBounds(adjusted ? this.mAdjustedBounds : this.mBounds, insetBounds);
            this.mDisplayContent.setLayoutNeeded();
        }
    }

    private void alignTasksToAdjustedBounds(Rect adjustedBounds, Rect tempInsetBounds) {
        if (!this.mFillsParent) {
            boolean alignBottom = this.mAdjustedForIme && getDockSide() == 2;
            for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
                ((Task) this.mChildren.get(taskNdx)).alignToAdjustedBounds(adjustedBounds, tempInsetBounds, alignBottom);
            }
        }
    }

    boolean setBounds(Rect bounds) {
        boolean oldFullscreen = this.mFillsParent;
        int rotation = 0;
        int density = 0;
        if (this.mDisplayContent != null) {
            this.mDisplayContent.getLogicalDisplayRect(this.mTmpRect);
            rotation = this.mDisplayContent.getDisplayInfo().rotation;
            density = this.mDisplayContent.getDisplayInfo().logicalDensityDpi;
            this.mFillsParent = bounds == null;
            if (this.mFillsParent) {
                bounds = this.mTmpRect;
            }
        }
        if (bounds == null) {
            return false;
        }
        if (this.mBounds.equals(bounds) && oldFullscreen == this.mFillsParent && this.mRotation == rotation) {
            return false;
        }
        if (this.mDisplayContent != null) {
            this.mDisplayContent.mDimLayerController.updateDimLayer(this);
            this.mAnimationBackgroundSurface.setBounds(bounds);
        }
        this.mBounds.set(bounds);
        this.mRotation = rotation;
        this.mDensity = density;
        updateAdjustedBounds();
        return true;
    }

    void getRawBounds(Rect out) {
        out.set(this.mBounds);
    }

    private boolean useCurrentBounds() {
        if (this.mFillsParent || (StackId.isResizeableByDockedStack(this.mStackId) ^ 1) != 0 || this.mDisplayContent == null || this.mDisplayContent.getDockedStackLocked() != null) {
            return true;
        }
        return false;
    }

    public void getBounds(Rect out) {
        if (useCurrentBounds()) {
            if (this.mAdjustedBounds.isEmpty()) {
                out.set(this.mBounds);
            } else {
                out.set(this.mAdjustedBounds);
            }
            return;
        }
        this.mDisplayContent.getLogicalDisplayRect(out);
    }

    void setAnimationFinalBounds(Rect sourceHintBounds, Rect destBounds, boolean toFullscreen) {
        this.mBoundsAnimatingRequested = true;
        this.mBoundsAnimatingToFullscreen = toFullscreen;
        if (destBounds != null) {
            this.mBoundsAnimationTarget.set(destBounds);
        } else {
            this.mBoundsAnimationTarget.setEmpty();
        }
        if (sourceHintBounds != null) {
            this.mBoundsAnimationSourceHintBounds.set(sourceHintBounds);
        } else {
            this.mBoundsAnimationSourceHintBounds.setEmpty();
        }
        this.mPreAnimationBounds.set(this.mBounds);
    }

    void getFinalAnimationBounds(Rect outBounds) {
        outBounds.set(this.mBoundsAnimationTarget);
    }

    void getFinalAnimationSourceHintBounds(Rect outBounds) {
        outBounds.set(this.mBoundsAnimationSourceHintBounds);
    }

    void getAnimationOrCurrentBounds(Rect outBounds) {
        if ((this.mBoundsAnimatingRequested || this.mBoundsAnimating) && (this.mBoundsAnimationTarget.isEmpty() ^ 1) != 0) {
            getFinalAnimationBounds(outBounds);
        } else {
            getBounds(outBounds);
        }
    }

    public void getDimBounds(Rect out) {
        getBounds(out);
    }

    void updateDisplayInfo(Rect bounds) {
        if (this.mDisplayContent != null) {
            for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
                ((Task) this.mChildren.get(taskNdx)).updateDisplayInfo(this.mDisplayContent);
            }
            if (bounds != null) {
                setBounds(bounds);
            } else if (this.mFillsParent) {
                setBounds(null);
            } else {
                this.mTmpRect2.set(this.mBounds);
                int newRotation = this.mDisplayContent.getDisplayInfo().rotation;
                int newDensity = this.mDisplayContent.getDisplayInfo().logicalDensityDpi;
                if (this.mRotation == newRotation && this.mDensity == newDensity) {
                    setBounds(this.mTmpRect2);
                }
            }
        }
    }

    boolean updateBoundsAfterConfigChange() {
        int i = 0;
        if (this.mDisplayContent == null) {
            return false;
        }
        if (this.mStackId == 4) {
            getAnimationOrCurrentBounds(this.mTmpRect2);
            if (this.mDisplayContent.mPinnedStackControllerLocked.onTaskStackBoundsChanged(this.mTmpRect2, this.mTmpRect3)) {
                this.mBoundsAfterRotation.set(this.mTmpRect3);
                this.mBoundsAnimationTarget.setEmpty();
                this.mBoundsAnimationSourceHintBounds.setEmpty();
                this.mCancelCurrentBoundsAnimation = true;
                return true;
            }
        }
        int newRotation = getDisplayInfo().rotation;
        int newDensity = getDisplayInfo().logicalDensityDpi;
        if (this.mRotation == newRotation && this.mDensity == newDensity) {
            return false;
        }
        if (this.mFillsParent) {
            setBounds(null);
            return false;
        }
        this.mTmpRect2.set(this.mBounds);
        this.mDisplayContent.rotateBounds(this.mRotation, newRotation, this.mTmpRect2);
        switch (this.mStackId) {
            case 3:
                repositionDockedStackAfterRotation(this.mTmpRect2);
                snapDockedStackAfterRotation(this.mTmpRect2);
                int newDockSide = getDockSide(this.mTmpRect2);
                WindowManagerService windowManagerService = this.mService;
                if (!(newDockSide == 1 || newDockSide == 2)) {
                    i = 1;
                }
                windowManagerService.setDockedStackCreateStateLocked(i, null);
                this.mDisplayContent.getDockedDividerController().notifyDockSideChanged(newDockSide);
                break;
        }
        this.mBoundsAfterRotation.set(this.mTmpRect2);
        return true;
    }

    void getBoundsForNewConfiguration(Rect outBounds) {
        outBounds.set(this.mBoundsAfterRotation);
        this.mBoundsAfterRotation.setEmpty();
    }

    private void repositionDockedStackAfterRotation(Rect inOutBounds) {
        int dockSide = getDockSide(inOutBounds);
        if (!this.mService.mPolicy.isDockSideAllowed(dockSide)) {
            this.mDisplayContent.getLogicalDisplayRect(this.mTmpRect);
            int movement;
            switch (DockedDividerUtils.invertDockSide(dockSide)) {
                case 1:
                    movement = inOutBounds.left;
                    inOutBounds.left -= movement;
                    inOutBounds.right -= movement;
                    break;
                case 2:
                    movement = inOutBounds.top;
                    inOutBounds.top -= movement;
                    inOutBounds.bottom -= movement;
                    break;
                case 3:
                    movement = this.mTmpRect.right - inOutBounds.right;
                    inOutBounds.left += movement;
                    inOutBounds.right += movement;
                    break;
                case 4:
                    movement = this.mTmpRect.bottom - inOutBounds.bottom;
                    inOutBounds.top += movement;
                    inOutBounds.bottom += movement;
                    break;
            }
        }
    }

    private void snapDockedStackAfterRotation(Rect outBounds) {
        DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
        int dividerSize = this.mDisplayContent.getDockedDividerController().getContentWidth();
        int dockSide = getDockSide(outBounds);
        int dividerPosition = DockedDividerUtils.calculatePositionForBounds(outBounds, dockSide, dividerSize);
        int displayWidth = this.mDisplayContent.getDisplayInfo().logicalWidth;
        int displayHeight = this.mDisplayContent.getDisplayInfo().logicalHeight;
        int rotation = displayInfo.rotation;
        int orientation = this.mDisplayContent.getConfiguration().orientation;
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(this.mDisplayContent.getDisplayId())) {
            this.mService.mPolicy.getStableInsetsLw(rotation, displayWidth, displayHeight, outBounds, this.mDisplayContent.getDisplayId());
        } else {
            this.mService.mPolicy.getStableInsetsLw(rotation, displayWidth, displayHeight, outBounds);
        }
        DockedDividerUtils.calculateBoundsForPosition(new DividerSnapAlgorithm(this.mService.mContext.getResources(), displayWidth, displayHeight, dividerSize, orientation == 1, outBounds, isMinimizedDockAndHomeStackResizable()).calculateNonDismissingSnapTarget(dividerPosition).position, dockSide, outBounds, displayInfo.logicalWidth, displayInfo.logicalHeight, dividerSize);
    }

    void addTask(Task task, int position) {
        addTask(task, position, task.showForAllUsers(), true);
    }

    void addTask(Task task, int position, boolean showForAllUsers, boolean moveParents) {
        TaskStack currentStack = task.mStack;
        if (currentStack == null || currentStack.mStackId == this.mStackId) {
            task.mStack = this;
            addChild((WindowContainer) task, null);
            positionChildAt(position, task, moveParents, showForAllUsers);
            return;
        }
        throw new IllegalStateException("Trying to add taskId=" + task.mTaskId + " to stackId=" + this.mStackId + ", but it is already attached to stackId=" + task.mStack.mStackId);
    }

    void positionChildAt(int position, Task child, boolean includingParents) {
        positionChildAt(position, child, includingParents, child.showForAllUsers());
    }

    private void positionChildAt(int position, Task child, boolean includingParents, boolean showForAllUsers) {
        int targetPosition = findPositionForTask(child, position, showForAllUsers, false);
        super.positionChildAt(targetPosition, child, includingParents);
        int toTop = targetPosition == this.mChildren.size() + -1 ? 1 : 0;
        EventLog.writeEvent(EventLogTags.WM_TASK_MOVED, new Object[]{Integer.valueOf(child.mTaskId), Integer.valueOf(toTop), Integer.valueOf(targetPosition)});
    }

    private int findPositionForTask(Task task, int targetPosition, boolean showForAllUsers, boolean addingNew) {
        boolean canShowTask = !showForAllUsers ? this.mService.isCurrentProfileLocked(task.mUserId) : true;
        int stackSize = this.mChildren.size();
        int minPosition = 0;
        int maxPosition = addingNew ? stackSize : stackSize - 1;
        if (canShowTask) {
            minPosition = computeMinPosition(0, stackSize);
        } else {
            maxPosition = computeMaxPosition(maxPosition);
        }
        return Math.min(Math.max(targetPosition, minPosition), maxPosition);
    }

    private int computeMinPosition(int minPosition, int size) {
        while (minPosition < size) {
            boolean canShowTmpTask;
            Task tmpTask = (Task) this.mChildren.get(minPosition);
            if (tmpTask.showForAllUsers()) {
                canShowTmpTask = true;
            } else {
                canShowTmpTask = this.mService.isCurrentProfileLocked(tmpTask.mUserId);
            }
            if (canShowTmpTask) {
                break;
            }
            minPosition++;
        }
        return minPosition;
    }

    private int computeMaxPosition(int maxPosition) {
        while (maxPosition > 0) {
            boolean canShowTmpTask;
            Task tmpTask = (Task) this.mChildren.get(maxPosition);
            if (tmpTask.showForAllUsers()) {
                canShowTmpTask = true;
            } else {
                canShowTmpTask = this.mService.isCurrentProfileLocked(tmpTask.mUserId);
            }
            if (!canShowTmpTask) {
                break;
            }
            maxPosition--;
        }
        return maxPosition;
    }

    void removeChild(Task task) {
        super.removeChild(task);
        task.mStack = null;
        if (this.mDisplayContent != null) {
            if (this.mChildren.isEmpty()) {
                getParent().positionChildAt(Integer.MIN_VALUE, this, false);
            }
            this.mDisplayContent.setLayoutNeeded();
        }
        for (int appNdx = this.mExitingAppTokens.size() - 1; appNdx >= 0; appNdx--) {
            AppWindowToken wtoken = (AppWindowToken) this.mExitingAppTokens.get(appNdx);
            if (wtoken.getTask() == task) {
                wtoken.mIsExiting = false;
                this.mExitingAppTokens.remove(appNdx);
            }
        }
    }

    void onDisplayChanged(DisplayContent dc) {
        if (this.mDisplayContent != null) {
            throw new IllegalStateException("onDisplayChanged: Already attached");
        }
        this.mDisplayContent = dc;
        this.mAnimationBackgroundSurface = new DimLayer(this.mService, this, this.mDisplayContent.getDisplayId(), "animation background stackId=" + this.mStackId);
        Rect bounds = null;
        TaskStack dockedStack = dc.getDockedStackIgnoringVisibility();
        if (this.mStackId == 3 || !(dockedStack == null || !StackId.isResizeableByDockedStack(this.mStackId) || (dockedStack.fillsParent() ^ 1) == 0)) {
            bounds = new Rect();
            dc.getLogicalDisplayRect(this.mTmpRect);
            this.mTmpRect2.setEmpty();
            if (dockedStack != null) {
                dockedStack.getRawBounds(this.mTmpRect2);
            }
            getStackDockedModeBounds(this.mTmpRect, bounds, this.mStackId, this.mTmpRect2, this.mDisplayContent.mDividerControllerLocked.getContentWidth(), this.mService.mDockedStackCreateMode == 0);
        } else if (this.mStackId == 4) {
            getAnimationOrCurrentBounds(this.mTmpRect2);
            if (this.mDisplayContent.mPinnedStackControllerLocked.onTaskStackBoundsChanged(this.mTmpRect2, this.mTmpRect3)) {
                bounds = new Rect(this.mTmpRect3);
            }
        }
        updateDisplayInfo(bounds);
        super.onDisplayChanged(dc);
    }

    void getStackDockedModeBoundsLocked(Rect currentTempTaskBounds, Rect outStackBounds, Rect outTempTaskBounds, boolean ignoreVisibility) {
        outTempTaskBounds.setEmpty();
        if (this.mStackId == 0) {
            Task homeTask = findHomeTask();
            if (homeTask == null || !homeTask.isResizeable()) {
                outStackBounds.setEmpty();
            } else {
                getDisplayContent().mDividerControllerLocked.getHomeStackBoundsInDockedMode(outStackBounds);
            }
            outTempTaskBounds.set(outStackBounds);
        } else if (isMinimizedDockAndHomeStackResizable() && currentTempTaskBounds != null) {
            outStackBounds.set(currentTempTaskBounds);
        } else if ((this.mStackId == 3 || (StackId.isResizeableByDockedStack(this.mStackId) ^ 1) == 0) && this.mDisplayContent != null) {
            TaskStack dockedStack = this.mDisplayContent.getDockedStackIgnoringVisibility();
            if (dockedStack == null) {
                throw new IllegalStateException("Calling getStackDockedModeBoundsLocked() when there is no docked stack.");
            } else if (ignoreVisibility || (dockedStack.isVisible() ^ 1) == 0) {
                int dockedSide = dockedStack.getDockSide();
                if (dockedSide == -1) {
                    Slog.e("WindowManager", "Failed to get valid docked side for docked stack=" + dockedStack);
                    outStackBounds.set(this.mBounds);
                    return;
                }
                this.mDisplayContent.getLogicalDisplayRect(this.mTmpRect);
                dockedStack.getRawBounds(this.mTmpRect2);
                boolean dockedOnTopOrLeft = dockedSide == 2 || dockedSide == 1;
                getStackDockedModeBounds(this.mTmpRect, outStackBounds, this.mStackId, this.mTmpRect2, this.mDisplayContent.mDividerControllerLocked.getContentWidth(), dockedOnTopOrLeft);
            } else {
                this.mDisplayContent.getLogicalDisplayRect(outStackBounds);
            }
        } else {
            outStackBounds.set(this.mBounds);
        }
    }

    private void getStackDockedModeBounds(Rect displayRect, Rect outBounds, int stackId, Rect dockedBounds, int dockDividerWidth, boolean dockOnTopOrLeft) {
        boolean dockedStack = stackId == 3;
        boolean splitHorizontally = displayRect.width() > displayRect.height();
        outBounds.set(displayRect);
        if (!dockedStack) {
            if (dockOnTopOrLeft) {
                if (splitHorizontally) {
                    outBounds.left = dockedBounds.right + dockDividerWidth;
                } else {
                    outBounds.top = dockedBounds.bottom + dockDividerWidth;
                }
            } else if (splitHorizontally) {
                outBounds.right = dockedBounds.left - dockDividerWidth;
            } else {
                outBounds.bottom = dockedBounds.top - dockDividerWidth;
            }
            DockedDividerUtils.sanitizeStackBounds(outBounds, dockOnTopOrLeft ^ 1);
        } else if (this.mService.mDockedStackCreateBounds != null) {
            outBounds.set(this.mService.mDockedStackCreateBounds);
        } else {
            DisplayInfo di = this.mDisplayContent.getDisplayInfo();
            if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(this.mDisplayContent.getDisplayId())) {
                this.mService.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, this.mTmpRect2, this.mDisplayContent.getDisplayId());
            } else {
                this.mService.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, this.mTmpRect2);
            }
            int position = new DividerSnapAlgorithm(this.mService.mContext.getResources(), di.logicalWidth, di.logicalHeight, dockDividerWidth, this.mDisplayContent.getConfiguration().orientation == 1, this.mTmpRect2).getMiddleTarget().position;
            if (dockOnTopOrLeft) {
                if (splitHorizontally) {
                    outBounds.right = position;
                } else {
                    outBounds.bottom = position;
                }
            } else if (splitHorizontally) {
                outBounds.left = position + dockDividerWidth;
            } else {
                outBounds.top = position + dockDividerWidth;
            }
        }
    }

    void resetDockedStackToMiddle() {
        if (this.mStackId != 3) {
            throw new IllegalStateException("Not a docked stack=" + this);
        }
        this.mService.mDockedStackCreateBounds = null;
        Rect bounds = new Rect();
        getStackDockedModeBoundsLocked(null, bounds, new Rect(), true);
        getController().requestResize(bounds);
    }

    StackWindowController getController() {
        return (StackWindowController) super.getController();
    }

    void removeIfPossible() {
        if (isAnimating()) {
            this.mDeferRemoval = true;
        } else {
            removeImmediately();
        }
    }

    void removeImmediately() {
        super.removeImmediately();
        onRemovedFromDisplay();
    }

    void onRemovedFromDisplay() {
        this.mDisplayContent.mDimLayerController.removeDimLayerUser(this);
        EventLog.writeEvent(EventLogTags.WM_STACK_REMOVED, this.mStackId);
        if (this.mAnimationBackgroundSurface != null) {
            this.mAnimationBackgroundSurface.destroySurface();
            this.mAnimationBackgroundSurface = null;
        }
        if (this.mStackId == 3) {
            this.mDisplayContent.mDividerControllerLocked.notifyDockedStackExistsChanged(false);
        }
        this.mDisplayContent = null;
        this.mService.mWindowPlacerLocked.requestTraversal();
    }

    void resetAnimationBackgroundAnimator() {
        this.mAnimationBackgroundAnimator = null;
        if (this.mAnimationBackgroundSurface != null) {
            this.mAnimationBackgroundSurface.hide();
        }
    }

    protected void setAnimationBackground(WindowStateAnimator winAnimator, int color) {
        int animLayer = winAnimator.mAnimLayer;
        if (this.mAnimationBackgroundAnimator == null || animLayer < this.mAnimationBackgroundAnimator.mAnimLayer) {
            this.mAnimationBackgroundAnimator = winAnimator;
            this.mAnimationBackgroundSurface.show(this.mDisplayContent.getLayerForAnimationBackground(winAnimator) - 1, ((float) ((color >> 24) & 255)) / 255.0f, 0);
        }
    }

    void switchUser() {
        super.switchUser();
        int top = this.mChildren.size();
        for (int taskNdx = 0; taskNdx < top; taskNdx++) {
            Task task = (Task) this.mChildren.get(taskNdx);
            if (this.mService.isCurrentProfileLocked(task.mUserId) || task.showForAllUsers()) {
                this.mChildren.remove(taskNdx);
                this.mChildren.add(task);
                top--;
            }
        }
    }

    void setAdjustedForIme(WindowState imeWin, boolean forceUpdate) {
        this.mImeWin = imeWin;
        this.mImeGoingAway = false;
        if (!this.mAdjustedForIme || forceUpdate) {
            this.mAdjustedForIme = true;
            this.mAdjustImeAmount = 0.0f;
            this.mAdjustDividerAmount = 0.0f;
            updateAdjustForIme(0.0f, 0.0f, true);
        }
    }

    boolean isAdjustedForIme() {
        return this.mAdjustedForIme;
    }

    boolean isAnimatingForIme() {
        return this.mImeWin != null ? this.mImeWin.isAnimatingLw() : false;
    }

    boolean updateAdjustForIme(float adjustAmount, float adjustDividerAmount, boolean force) {
        if (adjustAmount == this.mAdjustImeAmount && adjustDividerAmount == this.mAdjustDividerAmount && !force) {
            return false;
        }
        this.mAdjustImeAmount = adjustAmount;
        this.mAdjustDividerAmount = adjustDividerAmount;
        updateAdjustedBounds();
        return isVisible();
    }

    void resetAdjustedForIme(boolean adjustBoundsNow) {
        if (adjustBoundsNow) {
            this.mImeWin = null;
            this.mAdjustedForIme = false;
            this.mImeGoingAway = false;
            this.mAdjustImeAmount = 0.0f;
            this.mAdjustDividerAmount = 0.0f;
            updateAdjustedBounds();
            this.mService.setResizeDimLayer(false, this.mStackId, 1.0f);
            return;
        }
        this.mImeGoingAway |= this.mAdjustedForIme;
    }

    boolean setAdjustedForMinimizedDock(float minimizeAmount) {
        if (minimizeAmount == this.mMinimizeAmount) {
            return false;
        }
        this.mMinimizeAmount = minimizeAmount;
        updateAdjustedBounds();
        return isVisible();
    }

    boolean shouldIgnoreInput() {
        if (isAdjustedForMinimizedDockedStack()) {
            return true;
        }
        if (this.mStackId == 3) {
            return isMinimizedDockAndHomeStackResizable();
        }
        return false;
    }

    void beginImeAdjustAnimation() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            Task task = (Task) this.mChildren.get(j);
            if (task.hasContentToDisplay()) {
                task.setDragResizing(true, 1);
                task.setWaitingForDrawnIfResizingChanged();
            }
        }
    }

    void endImeAdjustAnimation() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            ((Task) this.mChildren.get(j)).setDragResizing(false, 1);
        }
    }

    int getMinTopStackBottom(Rect displayContentRect, int originalStackBottom) {
        return displayContentRect.top + ((int) (((float) (originalStackBottom - displayContentRect.top)) * ADJUSTED_STACK_FRACTION_MIN));
    }

    private boolean adjustForIME(WindowState imeWin) {
        int dockedSide = getDockSide();
        boolean dockedTopOrBottom = dockedSide == 2 || dockedSide == 4;
        if (imeWin == null || (dockedTopOrBottom ^ 1) != 0) {
            return false;
        }
        Rect displayContentRect = this.mTmpRect;
        Rect contentBounds = this.mTmpRect2;
        getDisplayContent().getContentRect(displayContentRect);
        contentBounds.set(displayContentRect);
        int imeTop = Math.max(imeWin.getFrameLw().top, contentBounds.top) + imeWin.getGivenContentInsetsLw().top;
        if (contentBounds.bottom > imeTop) {
            contentBounds.bottom = imeTop;
        }
        int yOffset = displayContentRect.bottom - contentBounds.bottom;
        int dividerWidth = getDisplayContent().mDividerControllerLocked.getContentWidth();
        int dividerWidthInactive = getDisplayContent().mDividerControllerLocked.getContentWidthInactive();
        if (dockedSide == 2) {
            int bottom = Math.max(((this.mBounds.bottom - yOffset) + dividerWidth) - dividerWidthInactive, getMinTopStackBottom(displayContentRect, this.mBounds.bottom));
            this.mTmpAdjustedBounds.set(this.mBounds);
            this.mTmpAdjustedBounds.bottom = (int) ((this.mAdjustImeAmount * ((float) bottom)) + ((1.0f - this.mAdjustImeAmount) * ((float) this.mBounds.bottom)));
            this.mFullyAdjustedImeBounds.set(this.mBounds);
        } else {
            int dividerWidthDelta = dividerWidthInactive - dividerWidth;
            int topBeforeImeAdjust = (this.mBounds.top - dividerWidth) + dividerWidthInactive;
            int top = Math.max(this.mBounds.top - yOffset, getMinTopStackBottom(displayContentRect, this.mBounds.top - dividerWidth) + dividerWidthInactive);
            this.mTmpAdjustedBounds.set(this.mBounds);
            this.mTmpAdjustedBounds.top = this.mBounds.top + ((int) ((this.mAdjustImeAmount * ((float) (top - topBeforeImeAdjust))) + (this.mAdjustDividerAmount * ((float) dividerWidthDelta))));
            this.mFullyAdjustedImeBounds.set(this.mBounds);
            this.mFullyAdjustedImeBounds.top = top;
            this.mFullyAdjustedImeBounds.bottom = this.mBounds.height() + top;
        }
        return true;
    }

    private boolean adjustForMinimizedDockedStack(float minimizeAmount) {
        int dockSide = getDockSide();
        if (dockSide == -1 && (this.mTmpAdjustedBounds.isEmpty() ^ 1) != 0) {
            return false;
        }
        if (dockSide == 2) {
            this.mService.getStableInsetsLocked(0, this.mTmpRect);
            int topInset = this.mTmpRect.top;
            this.mTmpAdjustedBounds.set(this.mBounds);
            this.mTmpAdjustedBounds.bottom = (int) ((((float) topInset) * minimizeAmount) + ((1.0f - minimizeAmount) * ((float) this.mBounds.bottom)));
        } else if (dockSide == 1) {
            this.mTmpAdjustedBounds.set(this.mBounds);
            int width = this.mBounds.width();
            this.mTmpAdjustedBounds.right = (int) ((((float) this.mDockedStackMinimizeThickness) * minimizeAmount) + ((1.0f - minimizeAmount) * ((float) this.mBounds.right)));
            this.mTmpAdjustedBounds.left = this.mTmpAdjustedBounds.right - width;
        } else if (dockSide == 3) {
            this.mTmpAdjustedBounds.set(this.mBounds);
            this.mTmpAdjustedBounds.left = (int) ((((float) (this.mBounds.right - this.mDockedStackMinimizeThickness)) * minimizeAmount) + ((1.0f - minimizeAmount) * ((float) this.mBounds.left)));
        }
        return true;
    }

    private boolean isMinimizedDockAndHomeStackResizable() {
        if (this.mDisplayContent.mDividerControllerLocked.isMinimizedDock()) {
            return this.mDisplayContent.mDividerControllerLocked.isHomeStackResizable();
        }
        return false;
    }

    int getMinimizeDistance() {
        int dockSide = getDockSide();
        if (dockSide == -1) {
            return 0;
        }
        if (dockSide == 2) {
            this.mService.getStableInsetsLocked(0, this.mTmpRect);
            return this.mBounds.bottom - this.mTmpRect.top;
        } else if (dockSide == 1 || dockSide == 3) {
            return this.mBounds.width() - this.mDockedStackMinimizeThickness;
        } else {
            return 0;
        }
    }

    private void updateAdjustedBounds() {
        boolean adjust = false;
        if (this.mMinimizeAmount != 0.0f) {
            adjust = adjustForMinimizedDockedStack(this.mMinimizeAmount);
        } else if (this.mAdjustedForIme) {
            adjust = adjustForIME(this.mImeWin);
        }
        if (!adjust) {
            this.mTmpAdjustedBounds.setEmpty();
        }
        setAdjustedBounds(this.mTmpAdjustedBounds);
        boolean isImeTarget = this.mService.getImeFocusStackLocked() == this;
        if (this.mAdjustedForIme && adjust && (isImeTarget ^ 1) != 0) {
            this.mService.setResizeDimLayer(true, this.mStackId, Math.max(this.mAdjustImeAmount, this.mAdjustDividerAmount) * IME_ADJUST_DIM_AMOUNT);
        }
    }

    void applyAdjustForImeIfNeeded(Task task) {
        if (this.mMinimizeAmount == 0.0f && (this.mAdjustedForIme ^ 1) == 0 && !this.mAdjustedBounds.isEmpty()) {
            task.alignToAdjustedBounds(this.mAdjustedBounds, this.mImeGoingAway ? this.mBounds : this.mFullyAdjustedImeBounds, getDockSide() == 2);
            this.mDisplayContent.setLayoutNeeded();
        }
    }

    boolean isAdjustedForMinimizedDockedStack() {
        return this.mMinimizeAmount != 0.0f;
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + "mStackId=" + this.mStackId);
        pw.println(prefix + "mDeferRemoval=" + this.mDeferRemoval);
        pw.println(prefix + "mFillsParent=" + this.mFillsParent);
        pw.println(prefix + "mBounds=" + this.mBounds.toShortString());
        if (this.mMinimizeAmount != 0.0f) {
            pw.println(prefix + "mMinimizeAmount=" + this.mMinimizeAmount);
        }
        if (this.mAdjustedForIme) {
            pw.println(prefix + "mAdjustedForIme=true");
            pw.println(prefix + "mAdjustImeAmount=" + this.mAdjustImeAmount);
            pw.println(prefix + "mAdjustDividerAmount=" + this.mAdjustDividerAmount);
        }
        if (!this.mAdjustedBounds.isEmpty()) {
            pw.println(prefix + "mAdjustedBounds=" + this.mAdjustedBounds.toShortString());
        }
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            ((Task) this.mChildren.get(taskNdx)).dump(prefix + "  ", pw);
        }
        if (this.mAnimationBackgroundSurface.isDimming()) {
            pw.println(prefix + "mWindowAnimationBackgroundSurface:");
            this.mAnimationBackgroundSurface.printTo(prefix + "  ", pw);
        }
        if (!this.mExitingAppTokens.isEmpty()) {
            pw.println();
            pw.println("  Exiting application tokens:");
            for (int i = this.mExitingAppTokens.size() - 1; i >= 0; i--) {
                WindowToken token = (WindowToken) this.mExitingAppTokens.get(i);
                pw.print("  Exiting App #");
                pw.print(i);
                pw.print(' ');
                pw.print(token);
                pw.println(':');
                token.dump(pw, "    ");
            }
        }
    }

    boolean getRawFullscreen() {
        return this.mFillsParent;
    }

    public boolean dimFullscreen() {
        return !StackId.isHomeOrRecentsStack(this.mStackId) ? fillsParent() : true;
    }

    boolean fillsParent() {
        if (useCurrentBounds()) {
            return this.mFillsParent;
        }
        return true;
    }

    public DisplayInfo getDisplayInfo() {
        return this.mDisplayContent.getDisplayInfo();
    }

    public boolean isAttachedToDisplay() {
        return this.mDisplayContent != null;
    }

    public String toString() {
        return "{stackId=" + this.mStackId + " tasks=" + this.mChildren + "}";
    }

    String getName() {
        return toShortString();
    }

    public String toShortString() {
        return "Stack=" + this.mStackId;
    }

    int getDockSide() {
        return getDockSide(this.mBounds);
    }

    int getDockSide(Rect bounds) {
        if ((this.mStackId != 3 && (StackId.isResizeableByDockedStack(this.mStackId) ^ 1) != 0) || this.mDisplayContent == null) {
            return -1;
        }
        this.mDisplayContent.getLogicalDisplayRect(this.mTmpRect);
        return getDockSideUnchecked(bounds, this.mTmpRect, this.mDisplayContent.getConfiguration().orientation);
    }

    static int getDockSideUnchecked(Rect bounds, Rect displayRect, int orientation) {
        if (orientation == 1) {
            if (bounds.top - displayRect.top <= displayRect.bottom - bounds.bottom) {
                return 2;
            }
            return 4;
        } else if (orientation != 2) {
            return -1;
        } else {
            if (bounds.left - displayRect.left <= displayRect.right - bounds.right) {
                return 1;
            }
            return 3;
        }
    }

    boolean hasTaskForUser(int userId) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((Task) this.mChildren.get(i)).mUserId == userId) {
                return true;
            }
        }
        return false;
    }

    int taskIdFromPoint(int x, int y) {
        getBounds(this.mTmpRect);
        if (!this.mTmpRect.contains(x, y) || isAdjustedForMinimizedDockedStack()) {
            return -1;
        }
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            Task task = (Task) this.mChildren.get(taskNdx);
            if (task.getTopVisibleAppMainWindow() == null) {
                if (!(HwPCUtils.isPcCastModeInServer() ? task.isVisible() : false)) {
                    continue;
                }
            }
            task.getDimBounds(this.mTmpRect);
            if (this.mTmpRect.contains(x, y)) {
                return task.mTaskId;
            }
        }
        return -1;
    }

    protected void findTaskForResizePoint(int x, int y, int delta, TaskForResizePointSearchResult results) {
        if (StackId.isTaskResizeAllowed(this.mStackId)) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                Task task = (Task) this.mChildren.get(i);
                if (task.isFullscreen()) {
                    results.searchDone = true;
                    return;
                }
                task.getDimBounds(this.mTmpRect);
                this.mTmpRect.inset(-delta, -delta);
                if (this.mTmpRect.contains(x, y)) {
                    this.mTmpRect.inset(delta, delta);
                    results.searchDone = true;
                    if (!this.mTmpRect.contains(x, y)) {
                        results.taskForResize = task;
                        return;
                    }
                    return;
                }
            }
            return;
        }
        results.searchDone = true;
    }

    void setTouchExcludeRegion(Task focusedTask, int delta, Region touchExcludeRegion, Rect contentRect, Rect postExclude) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            Task task = (Task) this.mChildren.get(i);
            AppWindowToken token = task.getTopVisibleAppToken();
            if (token != null && (token.hasContentToDisplay() ^ 1) == 0) {
                if (task.isHomeTask() && isMinimizedDockAndHomeStackResizable()) {
                    this.mDisplayContent.getLogicalDisplayRect(this.mTmpRect);
                } else {
                    task.getDimBounds(this.mTmpRect);
                }
                if (task == focusedTask) {
                    postExclude.set(this.mTmpRect);
                }
                boolean isFreeformed = task.inFreeformWorkspace();
                if (task != focusedTask || isFreeformed) {
                    if (isFreeformed) {
                        this.mTmpRect.inset(-delta, -delta);
                        this.mTmpRect.intersect(contentRect);
                    }
                    touchExcludeRegion.op(this.mTmpRect, Op.DIFFERENCE);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0013, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:11:?, code:
            r3.mService.mActivityManager.resizePinnedStack(r4, r5);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setPinnedStackSize(Rect stackBounds, Rect tempTaskBounds) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mCancelCurrentBoundsAnimation) {
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return false;
        return true;
    }

    void onAllWindowsDrawn() {
        if (this.mBoundsAnimating || (this.mBoundsAnimatingRequested ^ 1) == 0) {
            this.mService.mBoundsAnimationController.onAllWindowsDrawn();
        }
    }

    public void onAnimationStart(boolean schedulePipModeChangedCallback) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mBoundsAnimatingRequested = false;
                this.mBoundsAnimating = true;
                this.mCancelCurrentBoundsAnimation = false;
                if (schedulePipModeChangedCallback) {
                    forAllWindows((Consumer) new -$Lambda$j4SSrYTFDOq_WO6634VEaBSX2Wc(), false);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (this.mStackId == 4) {
            try {
                this.mService.mActivityManager.notifyPinnedStackAnimationStarted();
            } catch (RemoteException e) {
            }
            PinnedStackWindowController controller = (PinnedStackWindowController) getController();
            if (schedulePipModeChangedCallback && controller != null) {
                controller.updatePictureInPictureModeForPinnedStackAnimation(null);
            }
        }
    }

    public void onAnimationEnd(boolean schedulePipModeChangedCallback, Rect finalStackSize, boolean moveToFullscreen) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mBoundsAnimating = false;
                for (int i = 0; i < this.mChildren.size(); i++) {
                    ((Task) this.mChildren.get(i)).clearPreserveNonFloatingState();
                }
                this.mService.requestTraversal();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (this.mStackId == 4) {
            PinnedStackWindowController controller = (PinnedStackWindowController) getController();
            if (schedulePipModeChangedCallback && controller != null) {
                controller.updatePictureInPictureModeForPinnedStackAnimation(this.mBoundsAnimationTarget);
            }
            if (finalStackSize != null) {
                setPinnedStackSize(finalStackSize, null);
            }
            try {
                this.mService.mActivityManager.notifyPinnedStackAnimationEnded();
                if (moveToFullscreen) {
                    this.mService.mActivityManager.moveTasksToFullscreenStack(this.mStackId, true);
                }
            } catch (RemoteException e) {
            }
        }
    }

    public boolean deferScheduleMultiWindowModeChanged() {
        if (this.mStackId != 4) {
            return false;
        }
        return !this.mBoundsAnimatingRequested ? this.mBoundsAnimating : true;
    }

    public boolean hasMovementAnimations() {
        return StackId.hasMovementAnimations(this.mStackId);
    }

    public boolean isForceScaled() {
        return this.mBoundsAnimating;
    }

    public boolean isAnimatingBounds() {
        return this.mBoundsAnimating;
    }

    public boolean lastAnimatingBoundsWasToFullscreen() {
        return this.mBoundsAnimatingToFullscreen;
    }

    public boolean isAnimatingBoundsToFullscreen() {
        return isAnimatingBounds() ? lastAnimatingBoundsWasToFullscreen() : false;
    }

    public boolean pinnedStackResizeDisallowed() {
        if (this.mBoundsAnimating && this.mCancelCurrentBoundsAnimation) {
            return true;
        }
        return false;
    }

    boolean checkCompleteDeferredRemoval() {
        if (isAnimating()) {
            return true;
        }
        if (this.mDeferRemoval) {
            removeImmediately();
        }
        return super.checkCompleteDeferredRemoval();
    }

    void stepAppWindowsAnimation(long currentTime) {
        super.stepAppWindowsAnimation(currentTime);
        this.mTmpAppTokens.clear();
        this.mTmpAppTokens.addAll(this.mExitingAppTokens);
        for (int i = 0; i < this.mTmpAppTokens.size(); i++) {
            AppWindowAnimator appAnimator = ((AppWindowToken) this.mTmpAppTokens.get(i)).mAppAnimator;
            appAnimator.wasAnimating = appAnimator.animating;
            if (appAnimator.stepAnimationLocked(currentTime)) {
                this.mService.mAnimator.setAnimating(true);
                this.mService.mAnimator.mAppWindowAnimating = true;
            } else if (appAnimator.wasAnimating) {
                appAnimator.mAppToken.setAppLayoutChanges(4, "exiting appToken " + appAnimator.mAppToken + " done");
            }
        }
        this.mTmpAppTokens.clear();
    }

    int getOrientation() {
        return StackId.canSpecifyOrientation(this.mStackId) ? super.getOrientation() : -2;
    }

    void clearTempInsetBounds() {
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            ((Task) this.mChildren.get(taskNdx)).setTempInsetBounds(null);
        }
    }
}
