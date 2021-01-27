package com.android.server.wm;

import android.app.RemoteAction;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.HwFoldScreenState;
import android.os.RemoteException;
import android.util.CoordinationModeUtils;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DockedDividerUtils;
import com.android.server.wm.BoundsAnimationController;
import com.android.server.wm.DisplayContent;
import com.huawei.android.app.HwActivityManager;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class TaskStack extends WindowContainer<Task> implements BoundsAnimationTarget, ConfigurationContainerListener {
    private static final float ADJUSTED_STACK_FRACTION_MIN = 0.3f;
    private static final float IME_ADJUST_DIM_AMOUNT = 0.25f;
    private static final int INVALID_TASK_ID = -1;
    private static final float RING_ANIMATION_DIM_AMOUNT = 1.0f;
    ActivityStack mActivityStack;
    private float mAdjustDividerAmount;
    private float mAdjustImeAmount;
    private final Rect mAdjustedBounds = new Rect();
    private boolean mAdjustedForIme;
    private final AnimatingAppWindowTokenRegistry mAnimatingAppWindowTokenRegistry = new AnimatingAppWindowTokenRegistry();
    private WindowStateAnimator mAnimationBackgroundAnimator;
    private SurfaceControl mAnimationBackgroundSurface;
    private boolean mAnimationBackgroundSurfaceIsShown = false;
    @BoundsAnimationController.AnimationType
    private int mAnimationType;
    private boolean mBoundsAnimating = false;
    private boolean mBoundsAnimatingRequested = false;
    private boolean mBoundsAnimatingToFullscreen = false;
    private Rect mBoundsAnimationSourceHintBounds = new Rect();
    private Rect mBoundsAnimationTarget = new Rect();
    private boolean mCancelCurrentBoundsAnimation = false;
    boolean mDeferRemoval;
    private Dimmer mDimmer = new Dimmer(this);
    private final int mDockedStackMinimizeThickness;
    final AppTokenList mExitingAppTokens = new AppTokenList();
    private final Rect mFullyAdjustedImeBounds = new Rect();
    float mHwStackScale = 1.0f;
    private boolean mImeGoingAway;
    private WindowState mImeWin;
    private boolean mInHwFreeFormMoveBackOrCloseState = false;
    private boolean mIsTvSplitExitTop = false;
    private final Point mLastSurfaceSize = new Point();
    private float mMinimizeAmount;
    Rect mPreAnimationBounds = new Rect();
    final int mStackId;
    private final Rect mTmpAdjustedBounds = new Rect();
    final AppTokenList mTmpAppTokens = new AppTokenList();
    final Rect mTmpDimBoundsRect = new Rect();
    private Rect mTmpFromBounds = new Rect();
    private Rect mTmpRect = new Rect();
    private Rect mTmpRect2 = new Rect();
    private Rect mTmpRect3 = new Rect();
    private Rect mTmpToBounds = new Rect();

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public /* bridge */ /* synthetic */ void commitPendingTransaction() {
        super.commitPendingTransaction();
    }

    @Override // com.android.server.wm.WindowContainer
    public /* bridge */ /* synthetic */ int compareTo(WindowContainer windowContainer) {
        return super.compareTo(windowContainer);
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public /* bridge */ /* synthetic */ SurfaceControl getAnimationLeashParent() {
        return super.getAnimationLeashParent();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public /* bridge */ /* synthetic */ SurfaceControl getParentSurfaceControl() {
        return super.getParentSurfaceControl();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public /* bridge */ /* synthetic */ SurfaceControl.Transaction getPendingTransaction() {
        return super.getPendingTransaction();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public /* bridge */ /* synthetic */ SurfaceControl getSurfaceControl() {
        return super.getSurfaceControl();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public /* bridge */ /* synthetic */ int getSurfaceHeight() {
        return super.getSurfaceHeight();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public /* bridge */ /* synthetic */ int getSurfaceWidth() {
        return super.getSurfaceWidth();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public /* bridge */ /* synthetic */ SurfaceControl.Builder makeAnimationLeash() {
        return super.makeAnimationLeash();
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public /* bridge */ /* synthetic */ void onAnimationLeashCreated(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl) {
        super.onAnimationLeashCreated(transaction, surfaceControl);
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.SurfaceAnimator.Animatable
    public /* bridge */ /* synthetic */ void onAnimationLeashLost(SurfaceControl.Transaction transaction) {
        super.onAnimationLeashLost(transaction);
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public /* bridge */ /* synthetic */ void onRequestedOverrideConfigurationChanged(Configuration configuration) {
        super.onRequestedOverrideConfigurationChanged(configuration);
    }

    public TaskStack(WindowManagerService service, int stackId, ActivityStack activityStack) {
        super(service);
        this.mStackId = stackId;
        this.mActivityStack = activityStack;
        activityStack.registerConfigurationChangeListener(this);
        this.mDockedStackMinimizeThickness = service.mContext.getResources().getDimensionPixelSize(17105146);
        EventLog.writeEvent(31004, stackId);
    }

    /* access modifiers changed from: package-private */
    public Task findHomeTask() {
        if (!isActivityTypeHome() || this.mChildren.isEmpty()) {
            return null;
        }
        return (Task) this.mChildren.get(this.mChildren.size() - 1);
    }

    /* access modifiers changed from: package-private */
    public void prepareFreezingTaskBounds() {
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            ((Task) this.mChildren.get(taskNdx)).prepareFreezingBounds();
        }
    }

    private void setAdjustedBounds(Rect bounds) {
        if (!this.mAdjustedBounds.equals(bounds) || isAnimatingForIme()) {
            this.mAdjustedBounds.set(bounds);
            boolean adjusted = !this.mAdjustedBounds.isEmpty();
            Rect insetBounds = null;
            if (adjusted && isAdjustedForMinimizedDockedStack()) {
                insetBounds = getRawBounds();
            } else if (adjusted && this.mAdjustedForIme) {
                insetBounds = this.mImeGoingAway ? getRawBounds() : this.mFullyAdjustedImeBounds;
            }
            alignTasksToAdjustedBounds(adjusted ? this.mAdjustedBounds : getRawBounds(), insetBounds);
            this.mDisplayContent.setLayoutNeeded();
            updateSurfaceBounds();
        }
    }

    private void alignTasksToAdjustedBounds(Rect adjustedBounds, Rect tempInsetBounds) {
        if (!matchParentBounds()) {
            boolean alignBottom = this.mAdjustedForIme && getDockSide() == 2;
            for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
                ((Task) this.mChildren.get(taskNdx)).alignToAdjustedBounds(adjustedBounds, tempInsetBounds, alignBottom);
            }
        }
    }

    private void updateAnimationBackgroundBounds() {
        if (this.mAnimationBackgroundSurface != null) {
            getRawBounds(this.mTmpRect);
            Rect stackBounds = getBounds();
            getPendingTransaction().setWindowCrop(this.mAnimationBackgroundSurface, this.mTmpRect.width(), this.mTmpRect.height()).setPosition(this.mAnimationBackgroundSurface, (float) (this.mTmpRect.left - stackBounds.left), (float) (this.mTmpRect.top - stackBounds.top));
            scheduleAnimation();
        }
    }

    private void hideAnimationSurface() {
        if (this.mAnimationBackgroundSurface != null && this.mAnimationBackgroundSurfaceIsShown) {
            getPendingTransaction().hide(this.mAnimationBackgroundSurface);
            this.mAnimationBackgroundSurfaceIsShown = false;
            scheduleAnimation();
        }
    }

    private void showAnimationSurface(float alpha) {
        if (this.mAnimationBackgroundSurface != null) {
            if (!inHwFreeFormWindowingMode()) {
                getPendingTransaction().setLayer(this.mAnimationBackgroundSurface, Integer.MIN_VALUE).setAlpha(this.mAnimationBackgroundSurface, alpha).show(this.mAnimationBackgroundSurface);
            }
            this.mAnimationBackgroundSurfaceIsShown = true;
            scheduleAnimation();
        }
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public int setBounds(Rect bounds) {
        return setBounds(getRequestedOverrideBounds(), bounds);
    }

    private int setBounds(Rect existing, Rect bounds) {
        if (equivalentBounds(existing, bounds)) {
            return 0;
        }
        int result = super.setBounds(bounds);
        if (getParent() != null) {
            updateAnimationBackgroundBounds();
        }
        updateAdjustedBounds();
        updateSurfaceBounds();
        return result;
    }

    /* access modifiers changed from: package-private */
    public void getRawBounds(Rect out) {
        out.set(getRawBounds());
    }

    /* access modifiers changed from: package-private */
    public Rect getRawBounds() {
        return super.getBounds();
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void getBounds(Rect bounds) {
        bounds.set(getBounds());
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public Rect getBounds() {
        if (!this.mAdjustedBounds.isEmpty()) {
            return this.mAdjustedBounds;
        }
        return super.getBounds();
    }

    private void setAnimationFinalBounds(Rect sourceHintBounds, Rect destBounds, boolean toFullscreen) {
        this.mBoundsAnimatingRequested = true;
        this.mBoundsAnimatingToFullscreen = toFullscreen;
        if (destBounds != null) {
            this.mBoundsAnimationTarget.set(destBounds);
        } else {
            this.mBoundsAnimationTarget.setEmpty();
        }
        if (sourceHintBounds != null) {
            this.mBoundsAnimationSourceHintBounds.set(sourceHintBounds);
        } else if (!this.mBoundsAnimating) {
            this.mBoundsAnimationSourceHintBounds.setEmpty();
        }
        this.mPreAnimationBounds.set(getRawBounds());
    }

    /* access modifiers changed from: package-private */
    public void getFinalAnimationBounds(Rect outBounds) {
        outBounds.set(this.mBoundsAnimationTarget);
    }

    /* access modifiers changed from: package-private */
    public void getFinalAnimationSourceHintBounds(Rect outBounds) {
        outBounds.set(this.mBoundsAnimationSourceHintBounds);
    }

    /* access modifiers changed from: package-private */
    public void getAnimationOrCurrentBounds(Rect outBounds) {
        if ((this.mBoundsAnimatingRequested || this.mBoundsAnimating) && !this.mBoundsAnimationTarget.isEmpty()) {
            getFinalAnimationBounds(outBounds);
        } else {
            getBounds(outBounds);
        }
    }

    public void getDimBounds(Rect out) {
        getBounds(out);
    }

    /* access modifiers changed from: package-private */
    public boolean calculatePinnedBoundsForConfigChange(Rect inOutBounds) {
        boolean animating = false;
        if ((this.mBoundsAnimatingRequested || this.mBoundsAnimating) && !this.mBoundsAnimationTarget.isEmpty()) {
            animating = true;
            getFinalAnimationBounds(this.mTmpRect2);
        } else {
            this.mTmpRect2.set(inOutBounds);
        }
        boolean updated = this.mDisplayContent.mPinnedStackControllerLocked.onTaskStackBoundsChanged(this.mTmpRect2, this.mTmpRect3);
        if (updated) {
            inOutBounds.set(this.mTmpRect3);
            if (animating && !inOutBounds.equals(this.mBoundsAnimationTarget)) {
                DisplayContent displayContent = getDisplayContent();
                displayContent.mBoundsAnimationController.getHandler().post(new Runnable(displayContent) {
                    /* class com.android.server.wm.$$Lambda$TaskStack$LbFVWgYTv7giS6WqQc5168AJCDQ */
                    private final /* synthetic */ DisplayContent f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        TaskStack.this.lambda$calculatePinnedBoundsForConfigChange$0$TaskStack(this.f$1);
                    }
                });
            }
            this.mBoundsAnimationTarget.setEmpty();
            this.mBoundsAnimationSourceHintBounds.setEmpty();
            this.mCancelCurrentBoundsAnimation = true;
        }
        return updated;
    }

    public /* synthetic */ void lambda$calculatePinnedBoundsForConfigChange$0$TaskStack(DisplayContent displayContent) {
        displayContent.mBoundsAnimationController.cancel(this);
    }

    /* access modifiers changed from: package-private */
    public void calculateDockedBoundsForConfigChange(Configuration parentConfig, Rect inOutBounds) {
        int i = 0;
        boolean primary = getRequestedOverrideWindowingMode() == 3;
        repositionSplitScreenStackAfterRotation(parentConfig, primary, inOutBounds);
        snapDockedStackAfterRotation(parentConfig, this.mDisplayContent.getDisplayInfo().displayCutout, inOutBounds);
        if (primary) {
            int newDockSide = getDockSide(parentConfig, inOutBounds);
            WindowManagerService windowManagerService = this.mWmService;
            if (!(newDockSide == 1 || newDockSide == 2)) {
                i = 1;
            }
            windowManagerService.setDockedStackCreateStateLocked(i, null);
            this.mDisplayContent.getDockedDividerController().notifyDockSideChanged(newDockSide);
        }
    }

    /* access modifiers changed from: package-private */
    public void repositionSplitScreenStackAfterRotation(Configuration parentConfig, boolean primary, Rect inOutBounds) {
        int dockSide = getDockSide(this.mDisplayContent, parentConfig, inOutBounds);
        int otherDockSide = DockedDividerUtils.invertDockSide(dockSide);
        if (!this.mDisplayContent.getDockedDividerController().canPrimaryStackDockTo(primary ? dockSide : otherDockSide, parentConfig.windowConfiguration.getBounds(), parentConfig.windowConfiguration.getRotation())) {
            Rect parentBounds = parentConfig.windowConfiguration.getBounds();
            if (otherDockSide == 1) {
                int movement = inOutBounds.left;
                inOutBounds.left -= movement;
                inOutBounds.right -= movement;
            } else if (otherDockSide == 2) {
                int movement2 = inOutBounds.top;
                inOutBounds.top -= movement2;
                inOutBounds.bottom -= movement2;
            } else if (otherDockSide == 3) {
                int movement3 = parentBounds.right - inOutBounds.right;
                inOutBounds.left += movement3;
                inOutBounds.right += movement3;
            } else if (otherDockSide == 4) {
                int movement4 = parentBounds.bottom - inOutBounds.bottom;
                inOutBounds.top += movement4;
                inOutBounds.bottom += movement4;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void snapDockedStackAfterRotation(Configuration parentConfig, DisplayCutout displayCutout, Rect outBounds) {
        int dividerSize = this.mDisplayContent.getDockedDividerController().getContentWidth();
        int dockSide = getDockSide(parentConfig, outBounds);
        int dividerPosition = DockedDividerUtils.calculatePositionForBounds(outBounds, dockSide, dividerSize);
        int displayWidth = parentConfig.windowConfiguration.getBounds().width();
        int displayHeight = parentConfig.windowConfiguration.getBounds().height();
        int rotation = parentConfig.windowConfiguration.getRotation();
        int orientation = parentConfig.orientation;
        this.mDisplayContent.getDisplayPolicy().getStableInsetsLw(rotation, displayWidth, displayHeight, displayCutout, outBounds);
        Resources resources = this.mWmService.mContext.getResources();
        boolean z = true;
        if (orientation != 1) {
            z = false;
        }
        DockedDividerUtils.calculateBoundsForPosition(new DividerSnapAlgorithm(resources, displayWidth, displayHeight, dividerSize, z, outBounds, getDockSide(), isMinimizedDockAndHomeStackResizable()).calculateNonDismissingSnapTarget(dividerPosition).position, dockSide, outBounds, displayWidth, displayHeight, dividerSize);
    }

    /* access modifiers changed from: package-private */
    public void addTask(Task task, int position) {
        addTask(task, position, task.showForAllUsers(), true);
    }

    /* access modifiers changed from: package-private */
    public void addTask(Task task, int position, boolean showForAllUsers, boolean moveParents) {
        TaskStack currentStack = task.mStack;
        if (currentStack == null || currentStack.mStackId == this.mStackId) {
            task.mStack = this;
            addChild((TaskStack) task, (Comparator<TaskStack>) null);
            positionChildAt(position, task, moveParents, showForAllUsers);
            return;
        }
        throw new IllegalStateException("Trying to add taskId=" + task.mTaskId + " to stackId=" + this.mStackId + ", but it is already attached to stackId=" + task.mStack.mStackId);
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(Task child, int position) {
        if (child != null) {
            child.positionAt(position);
            getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void positionChildAtTop(Task child, boolean includingParents) {
        if (child != null) {
            positionChildAt(Integer.MAX_VALUE, child, includingParents);
            DisplayContent displayContent = getDisplayContent();
            if (displayContent.mAppTransition.isTransitionSet()) {
                child.setSendingToBottom(false);
            }
            displayContent.layoutAndAssignWindowLayersIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void positionChildAtBottom(Task child, boolean includingParents) {
        if (child != null) {
            positionChildAt(Integer.MIN_VALUE, child, includingParents);
            if (getDisplayContent().mAppTransition.isTransitionSet()) {
                child.setSendingToBottom(true);
            }
            getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, Task child, boolean includingParents) {
        positionChildAt(position, child, includingParents, child.showForAllUsers());
    }

    private void positionChildAt(int position, Task child, boolean includingParents, boolean showForAllUsers) {
        int targetPosition = findPositionForTask(child, position, showForAllUsers, false);
        super.positionChildAt(targetPosition, (int) child, includingParents);
        EventLog.writeEvent(31002, Integer.valueOf(child.mTaskId), Integer.valueOf(targetPosition == this.mChildren.size() - 1 ? 1 : 0), Integer.valueOf(targetPosition));
    }

    /* access modifiers changed from: package-private */
    public void reparent(int displayId, Rect outStackBounds, boolean onTop) {
        DisplayContent targetDc = this.mWmService.mRoot.getDisplayContent(displayId);
        if (targetDc != null) {
            targetDc.moveStackToDisplay(this, onTop);
            if (matchParentBounds()) {
                outStackBounds.setEmpty();
            } else {
                getRawBounds(outStackBounds);
            }
        } else {
            throw new IllegalArgumentException("Trying to move stackId=" + this.mStackId + " to unknown displayId=" + displayId);
        }
    }

    private int findPositionForTask(Task task, int targetPosition, boolean showForAllUsers, boolean addingNew) {
        boolean canShowTask = showForAllUsers || this.mWmService.isCurrentProfileLocked(task.mUserId);
        int stackSize = this.mChildren.size();
        int minPosition = 0;
        int maxPosition = addingNew ? stackSize : stackSize - 1;
        if (canShowTask) {
            minPosition = computeMinPosition(0, stackSize);
        } else {
            maxPosition = computeMaxPosition(maxPosition);
        }
        if (targetPosition == Integer.MIN_VALUE && minPosition == 0) {
            return Integer.MIN_VALUE;
        }
        if (targetPosition == Integer.MAX_VALUE) {
            if (maxPosition == (addingNew ? stackSize : stackSize - 1)) {
                return Integer.MAX_VALUE;
            }
        }
        return Math.min(Math.max(targetPosition, minPosition), maxPosition);
    }

    private int computeMinPosition(int minPosition, int size) {
        while (minPosition < size) {
            Task tmpTask = (Task) this.mChildren.get(minPosition);
            if (tmpTask.showForAllUsers() || this.mWmService.isCurrentProfileLocked(tmpTask.mUserId)) {
                break;
            }
            minPosition++;
        }
        return minPosition;
    }

    private int computeMaxPosition(int maxPosition) {
        while (maxPosition > 0) {
            Task tmpTask = (Task) this.mChildren.get(maxPosition);
            if (!(tmpTask.showForAllUsers() || this.mWmService.isCurrentProfileLocked(tmpTask.mUserId))) {
                break;
            }
            maxPosition--;
        }
        return maxPosition;
    }

    /* access modifiers changed from: package-private */
    public void removeChild(Task task) {
        super.removeChild((TaskStack) task);
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

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        int prevWindowingMode = getWindowingMode();
        super.onConfigurationChanged(newParentConfig);
        updateSurfaceSize(getPendingTransaction());
        int windowingMode = getWindowingMode();
        isAlwaysOnTop();
        if (this.mDisplayContent != null && prevWindowingMode != windowingMode) {
            this.mDisplayContent.onStackWindowingModeChanged(this);
            if (inSplitScreenSecondaryWindowingMode()) {
                forAllWindows((Consumer<WindowState>) $$Lambda$TaskStack$PVMhxGhbT6eBbe3ARm5uodEqxDE.INSTANCE, true);
            }
        }
    }

    private void updateSurfaceBounds() {
        updateSurfaceSize(getPendingTransaction());
        updateSurfacePosition();
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    public int getStackOutset() {
        DisplayContent displayContent = getDisplayContent();
        if (!inPinnedWindowingMode() || displayContent == null) {
            return 0;
        }
        DisplayMetrics displayMetrics = displayContent.getDisplayMetrics();
        WindowManagerService windowManagerService = this.mWmService;
        return (int) Math.ceil((double) (WindowManagerService.dipToPixel(5, displayMetrics) * 2));
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void getRelativeDisplayedPosition(Point outPos) {
        super.getRelativeDisplayedPosition(outPos);
        int outset = getStackOutset();
        outPos.x -= outset;
        outPos.y -= outset;
    }

    /* access modifiers changed from: protected */
    public void updateSurfaceSize(SurfaceControl.Transaction transaction) {
        if (this.mSurfaceControl != null) {
            Rect stackBounds = getDisplayedBounds();
            int width = stackBounds.width();
            int height = stackBounds.height();
            int outset = getStackOutset();
            int width2 = width + (outset * 2);
            int height2 = height + (outset * 2);
            if (HwFoldScreenState.isFoldScreenDevice() && this.mWmService.isInSubFoldScaleMode() && inMultiWindowMode() && !HwPCUtils.isPcDynamicStack(this.mStackId)) {
                width2 = (int) (((float) width2) * this.mWmService.mSubFoldModeScale);
                height2 = (int) (((float) height2) * this.mWmService.mSubFoldModeScale);
            }
            if (width2 != this.mLastSurfaceSize.x || height2 != this.mLastSurfaceSize.y) {
                if (getWindowConfiguration().tasksAreFloating()) {
                    transaction.setWindowCrop(this.mSurfaceControl, -1, -1);
                } else {
                    transaction.setWindowCrop(this.mSurfaceControl, width2, height2);
                }
                this.mLastSurfaceSize.set(width2, height2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Point getLastSurfaceSize() {
        return this.mLastSurfaceSize;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onDisplayChanged(DisplayContent dc) {
        if (this.mDisplayContent == null || this.mDisplayContent == dc) {
            super.onDisplayChanged(dc);
            updateSurfaceBounds();
            if (this.mAnimationBackgroundSurface == null) {
                SurfaceControl.Builder colorLayer = makeChildSurface(null).setColorLayer();
                this.mAnimationBackgroundSurface = colorLayer.setName("animation background stackId=" + this.mStackId).build();
                return;
            }
            return;
        }
        throw new IllegalStateException("onDisplayChanged: Already attached");
    }

    /* access modifiers changed from: package-private */
    public void getStackDockedModeBoundsLocked(Configuration parentConfig, Rect dockedBounds, Rect currentTempTaskBounds, Rect outStackBounds, Rect outTempTaskBounds) {
        outTempTaskBounds.setEmpty();
        if (!CoordinationModeUtils.isFoldable() || !inCoordinationSecondaryWindowingMode()) {
            if (dockedBounds != null) {
                if (!dockedBounds.isEmpty()) {
                    int dockedSide = getDockSide(parentConfig, dockedBounds);
                    if (isActivityTypeHome()) {
                        Task homeTask = findHomeTask();
                        if (homeTask == null || !homeTask.isResizeable()) {
                            outStackBounds.setEmpty();
                        } else {
                            getDisplayContent().mDividerControllerLocked.getHomeStackBoundsInDockedMode(parentConfig, dockedSide, outStackBounds);
                        }
                        outTempTaskBounds.set(outStackBounds);
                        return;
                    } else if (isMinimizedDockAndHomeStackResizable() && currentTempTaskBounds != null) {
                        outStackBounds.set(currentTempTaskBounds);
                        return;
                    } else if (dockedSide == -1) {
                        Slog.e("WindowManager", "Failed to get valid docked side for docked stack");
                        outStackBounds.set(getRawBounds());
                        return;
                    } else {
                        getStackDockedModeBounds(parentConfig, false, outStackBounds, dockedBounds, this.mDisplayContent.mDividerControllerLocked.getContentWidth(), dockedSide == 2 || dockedSide == 1);
                        return;
                    }
                }
            }
            getStackDockedModeBounds(parentConfig, true, outStackBounds, dockedBounds, this.mDisplayContent.mDividerControllerLocked.getContentWidth(), this.mWmService.mDockedStackCreateMode == 0);
            return;
        }
        if (isActivityTypeHome()) {
            CoordinationModeUtils.getInstance(this.mWmService.mContext).getStackCoordinationModeBounds(true, getDisplayContent().mDisplayInfo.rotation, outStackBounds);
        } else {
            getStackCoordinationModeBounds(outStackBounds);
        }
        outTempTaskBounds.set(outStackBounds);
    }

    private void getStackDockedModeBounds(Configuration parentConfig, boolean primary, Rect outBounds, Rect dockedBounds, int dockDividerWidth, boolean dockOnTopOrLeft) {
        Rect displayRect = parentConfig.windowConfiguration.getBounds();
        boolean splitHorizontally = displayRect.width() > displayRect.height();
        outBounds.set(displayRect);
        if (!primary) {
            if (!dockOnTopOrLeft) {
                if (splitHorizontally) {
                    outBounds.right = dockedBounds.left - dockDividerWidth;
                } else {
                    outBounds.bottom = dockedBounds.top - dockDividerWidth;
                }
            } else if (splitHorizontally) {
                outBounds.left = dockedBounds.right + dockDividerWidth;
            } else {
                outBounds.top = dockedBounds.bottom + dockDividerWidth;
            }
            DockedDividerUtils.sanitizeStackBounds(outBounds, !dockOnTopOrLeft);
        } else if (this.mWmService.mDockedStackCreateBounds != null) {
            outBounds.set(this.mWmService.mDockedStackCreateBounds);
        } else {
            this.mDisplayContent.getDisplayPolicy().getStableInsetsLw(parentConfig.windowConfiguration.getRotation(), displayRect.width(), displayRect.height(), this.mDisplayContent.getDisplayInfo().displayCutout, this.mTmpRect2);
            int position = new DividerSnapAlgorithm(this.mWmService.mContext.getResources(), displayRect.width(), displayRect.height(), dockDividerWidth, parentConfig.orientation == 1, this.mTmpRect2).getMiddleTarget().position;
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

    /* access modifiers changed from: package-private */
    public void resetDockedStackToMiddle() {
        if (!inSplitScreenPrimaryWindowingMode()) {
            Rect dockedBounds = null;
            this.mWmService.mDockedStackCreateBounds = null;
            Rect bounds = new Rect();
            Rect tempBounds = new Rect();
            TaskStack dockedStack = this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility();
            if (!(dockedStack == null || dockedStack == this)) {
                dockedBounds = dockedStack.getRawBounds();
            }
            getStackDockedModeBoundsLocked(this.mDisplayContent.getConfiguration(), dockedBounds, null, bounds, tempBounds);
            this.mActivityStack.requestResize(bounds);
            return;
        }
        throw new IllegalStateException("Not a docked stack=" + this);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeIfPossible() {
        if (isSelfOrChildAnimating()) {
            this.mDeferRemoval = true;
        } else {
            removeImmediately();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeImmediately() {
        ActivityStack activityStack = this.mActivityStack;
        if (activityStack != null) {
            activityStack.unregisterConfigurationChangeListener(this);
        }
        super.removeImmediately();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onParentChanged() {
        super.onParentChanged();
        if (getParent() == null && this.mDisplayContent != null) {
            EventLog.writeEvent(31006, this.mStackId);
            SurfaceControl surfaceControl = this.mAnimationBackgroundSurface;
            if (surfaceControl != null) {
                surfaceControl.remove();
                this.mAnimationBackgroundSurface = null;
            }
            this.mDisplayContent = null;
            this.mWmService.mWindowPlacerLocked.requestTraversal();
        }
    }

    /* access modifiers changed from: package-private */
    public void resetAnimationBackgroundAnimator() {
        this.mAnimationBackgroundAnimator = null;
        hideAnimationSurface();
    }

    /* access modifiers changed from: package-private */
    public void setAnimationBackground(WindowStateAnimator winAnimator, int color) {
        if (this.mAnimationBackgroundAnimator == null) {
            this.mAnimationBackgroundAnimator = winAnimator;
            showAnimationSurface(((float) ((color >> 24) & 255)) / 255.0f);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void switchUser() {
        super.switchUser();
        int top = this.mChildren.size();
        for (int taskNdx = 0; taskNdx < top; taskNdx++) {
            Task task = (Task) this.mChildren.get(taskNdx);
            if (this.mWmService.isCurrentProfileLocked(task.mUserId) || task.showForAllUsers()) {
                this.mChildren.remove(taskNdx);
                this.mChildren.add(task);
                top--;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setAdjustedForIme(WindowState imeWin, boolean keepLastAmount) {
        this.mImeWin = imeWin;
        this.mImeGoingAway = false;
        if (!this.mAdjustedForIme || keepLastAmount) {
            this.mAdjustedForIme = true;
            DockedStackDividerController controller = getDisplayContent().mDividerControllerLocked;
            float adjustDividerAmount = 0.0f;
            float adjustImeAmount = keepLastAmount ? controller.mLastAnimationProgress : 0.0f;
            if (keepLastAmount) {
                adjustDividerAmount = controller.mLastDividerProgress;
            }
            updateAdjustForIme(adjustImeAmount, adjustDividerAmount, true);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAdjustedForIme() {
        return this.mAdjustedForIme;
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimatingForIme() {
        WindowState windowState = this.mImeWin;
        return windowState != null && windowState.isAnimatingLw();
    }

    /* access modifiers changed from: package-private */
    public boolean updateAdjustForIme(float adjustAmount, float adjustDividerAmount, boolean force) {
        if (adjustAmount == this.mAdjustImeAmount && adjustDividerAmount == this.mAdjustDividerAmount && !force) {
            return false;
        }
        this.mAdjustImeAmount = adjustAmount;
        this.mAdjustDividerAmount = adjustDividerAmount;
        updateAdjustedBounds();
        return isVisible();
    }

    /* access modifiers changed from: package-private */
    public void resetAdjustedForIme(boolean adjustBoundsNow) {
        AppWindowToken topApp;
        if (adjustBoundsNow) {
            this.mImeWin = null;
            int displayId = 0;
            this.mImeGoingAway = false;
            this.mAdjustImeAmount = 0.0f;
            this.mAdjustDividerAmount = 0.0f;
            if (this.mAdjustedForIme) {
                this.mAdjustedForIme = false;
                updateAdjustedBounds();
                this.mWmService.setResizeDimLayer(false, getWindowingMode(), 1.0f);
                if (inHwSplitScreenWindowingMode()) {
                    Task topTask = (Task) getTopChild();
                    if (!(topTask == null || (topApp = topTask.getTopVisibleAppToken()) == null || topApp.findMainWindow() == null)) {
                        topApp.findMainWindow().mLayoutNeeded = true;
                    }
                    if (this.mDisplayContent != null) {
                        displayId = this.mDisplayContent.mDisplayId;
                    }
                    this.mActivityStack.mService.applyUpdateSplitBarPos(1, displayId);
                    return;
                }
                return;
            }
            return;
        }
        this.mImeGoingAway |= this.mAdjustedForIme;
    }

    /* access modifiers changed from: package-private */
    public boolean setAdjustedForMinimizedDock(float minimizeAmount) {
        if (minimizeAmount == this.mMinimizeAmount) {
            return false;
        }
        this.mMinimizeAmount = minimizeAmount;
        updateAdjustedBounds();
        return isVisible();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldIgnoreInput() {
        return isAdjustedForMinimizedDockedStack() || (inSplitScreenPrimaryWindowingMode() && isMinimizedDockAndHomeStackResizable());
    }

    /* access modifiers changed from: package-private */
    public void beginImeAdjustAnimation() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            Task task = (Task) this.mChildren.get(j);
            if (task.hasContentToDisplay()) {
                task.setDragResizing(true, 1);
                task.setWaitingForDrawnIfResizingChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void endImeAdjustAnimation() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            ((Task) this.mChildren.get(j)).setDragResizing(false, 1);
        }
    }

    /* access modifiers changed from: package-private */
    public int getMinTopStackBottom(Rect displayContentRect, int originalStackBottom) {
        return displayContentRect.top + ((int) (((float) (originalStackBottom - displayContentRect.top)) * ADJUSTED_STACK_FRACTION_MIN));
    }

    private boolean adjustForIME(WindowState imeWin) {
        if (getDisplayContent().mAppTransition.isRunning()) {
            return false;
        }
        int dockedSide = inHwSplitScreenWindowingMode() ? getHwDockSide() : getDockSide();
        boolean dockedTopOrBottom = dockedSide == 2 || dockedSide == 4;
        if (imeWin != null) {
            if (dockedTopOrBottom) {
                Rect displayStableRect = this.mTmpRect;
                Rect contentBounds = this.mTmpRect2;
                getDisplayContent().getStableRect(displayStableRect);
                contentBounds.set(displayStableRect);
                int imeTop = Math.max(imeWin.getFrameLw().top, contentBounds.top) + imeWin.getGivenContentInsetsLw().top;
                if (contentBounds.bottom > imeTop) {
                    contentBounds.bottom = imeTop;
                }
                int yOffset = displayStableRect.bottom - contentBounds.bottom;
                int dividerWidth = getDisplayContent().mDividerControllerLocked.getContentWidth();
                int dividerWidthInactive = getDisplayContent().mDividerControllerLocked.getContentWidthInactive();
                if (dockedSide == 2) {
                    int bottom = Math.max(((getRawBounds().bottom - yOffset) + dividerWidth) - dividerWidthInactive, getMinTopStackBottom(displayStableRect, getRawBounds().bottom));
                    this.mTmpAdjustedBounds.set(getRawBounds());
                    Rect rect = this.mTmpAdjustedBounds;
                    float f = this.mAdjustImeAmount;
                    rect.bottom = (int) ((((float) bottom) * f) + ((1.0f - f) * ((float) getRawBounds().bottom)));
                    this.mFullyAdjustedImeBounds.set(getRawBounds());
                    return true;
                }
                int top = Math.max(getRawBounds().top - yOffset, getMinTopStackBottom(displayStableRect, getRawBounds().top - dividerWidth) + dividerWidthInactive);
                this.mTmpAdjustedBounds.set(getRawBounds());
                this.mTmpAdjustedBounds.top = getRawBounds().top + ((int) ((this.mAdjustImeAmount * ((float) (top - ((getRawBounds().top - dividerWidth) + dividerWidthInactive)))) + (this.mAdjustDividerAmount * ((float) (dividerWidthInactive - dividerWidth)))));
                this.mFullyAdjustedImeBounds.set(getRawBounds());
                Rect rect2 = this.mFullyAdjustedImeBounds;
                rect2.top = top;
                rect2.bottom = getRawBounds().height() + top;
                return true;
            }
        }
        return false;
    }

    private boolean adjustForMinimizedDockedStack(float minimizeAmount) {
        int dockSide = getDockSide();
        if (dockSide == -1 && !this.mTmpAdjustedBounds.isEmpty()) {
            return false;
        }
        if (dockSide == 2) {
            this.mWmService.getStableInsetsLocked(0, this.mTmpRect);
            int topInset = this.mTmpRect.top;
            this.mTmpAdjustedBounds.set(getRawBounds());
            this.mTmpAdjustedBounds.bottom = (int) ((((float) topInset) * minimizeAmount) + ((1.0f - minimizeAmount) * ((float) getRawBounds().bottom)));
        } else if (dockSide == 1) {
            this.mTmpAdjustedBounds.set(getRawBounds());
            int width = getRawBounds().width();
            this.mTmpAdjustedBounds.right = (int) ((((float) this.mDockedStackMinimizeThickness) * minimizeAmount) + ((1.0f - minimizeAmount) * ((float) getRawBounds().right)));
            Rect rect = this.mTmpAdjustedBounds;
            rect.left = rect.right - width;
        } else if (dockSide == 3) {
            this.mTmpAdjustedBounds.set(getRawBounds());
            this.mTmpAdjustedBounds.left = (int) ((((float) (getRawBounds().right - this.mDockedStackMinimizeThickness)) * minimizeAmount) + ((1.0f - minimizeAmount) * ((float) getRawBounds().left)));
        }
        return true;
    }

    private boolean isMinimizedDockAndHomeStackResizable() {
        return this.mDisplayContent.mDividerControllerLocked.isMinimizedDock() && this.mDisplayContent.mDividerControllerLocked.isHomeStackResizable();
    }

    /* access modifiers changed from: package-private */
    public int getMinimizeDistance() {
        int dockSide = getDockSide();
        if (dockSide == -1) {
            return 0;
        }
        if (dockSide == 2) {
            this.mWmService.getStableInsetsLocked(0, this.mTmpRect);
            return getRawBounds().bottom - this.mTmpRect.top;
        } else if (dockSide == 1 || dockSide == 3) {
            return getRawBounds().width() - this.mDockedStackMinimizeThickness;
        } else {
            return 0;
        }
    }

    private void updateAdjustedBounds() {
        boolean adjust = false;
        float f = this.mMinimizeAmount;
        if (f != 0.0f) {
            adjust = adjustForMinimizedDockedStack(f);
        } else if (this.mAdjustedForIme) {
            adjust = adjustForIME(this.mImeWin);
        }
        if (!adjust) {
            this.mTmpAdjustedBounds.setEmpty();
        }
        setAdjustedBounds(this.mTmpAdjustedBounds);
        int displayId = 0;
        boolean isImeTarget = this.mWmService.getImeFocusStackLocked() == this;
        if (this.mAdjustedForIme && adjust) {
            if (!isImeTarget) {
                this.mWmService.setResizeDimLayer(true, getWindowingMode(), Math.max(this.mAdjustImeAmount, this.mAdjustDividerAmount) * IME_ADJUST_DIM_AMOUNT);
                return;
            }
            if (this.mDisplayContent != null) {
                displayId = this.mDisplayContent.mDisplayId;
            }
            this.mActivityStack.mService.applyUpdateSplitBarPos(-1, displayId);
        }
    }

    /* access modifiers changed from: package-private */
    public void applyAdjustForImeIfNeeded(Task task) {
        if (this.mMinimizeAmount == 0.0f && this.mAdjustedForIme && !this.mAdjustedBounds.isEmpty()) {
            task.alignToAdjustedBounds(this.mAdjustedBounds, this.mImeGoingAway ? getRawBounds() : this.mFullyAdjustedImeBounds, getDockSide() == 2);
            this.mDisplayContent.setLayoutNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAdjustedForMinimizedDockedStack() {
        return this.mMinimizeAmount != 0.0f;
    }

    /* access modifiers changed from: package-private */
    public boolean isTaskAnimating() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if (((Task) this.mChildren.get(j)).isTaskAnimating()) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        if (logLevel != 2 || isVisible()) {
            long token = proto.start(fieldId);
            super.writeToProto(proto, 1146756268033L, logLevel);
            proto.write(1120986464258L, this.mStackId);
            for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
                ((Task) this.mChildren.get(taskNdx)).writeToProto(proto, 2246267895811L, logLevel);
            }
            proto.write(1133871366148L, matchParentBounds());
            getRawBounds().writeToProto(proto, 1146756268037L);
            proto.write(1133871366150L, this.mAnimationBackgroundSurfaceIsShown);
            proto.write(1133871366151L, this.mDeferRemoval);
            proto.write(1108101562376L, this.mMinimizeAmount);
            proto.write(1133871366153L, this.mAdjustedForIme);
            proto.write(1108101562378L, this.mAdjustImeAmount);
            proto.write(1108101562379L, this.mAdjustDividerAmount);
            this.mAdjustedBounds.writeToProto(proto, 1146756268044L);
            proto.write(1133871366157L, this.mBoundsAnimating);
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        pw.println(prefix + "mStackId=" + this.mStackId);
        pw.println(prefix + "mDeferRemoval=" + this.mDeferRemoval);
        pw.println(prefix + "mBounds=" + getRawBounds().toShortString());
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
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx += -1) {
            ((Task) this.mChildren.get(taskNdx)).dump(pw, prefix + "  ", dumpAll);
        }
        if (this.mAnimationBackgroundSurfaceIsShown) {
            pw.println(prefix + "mWindowAnimationBackgroundSurface is shown");
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
                token.dump(pw, "    ", dumpAll);
            }
        }
        this.mAnimatingAppWindowTokenRegistry.dump(pw, "AnimatingApps:", prefix);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean fillsParent() {
        return matchParentBounds();
    }

    @Override // java.lang.Object
    public String toString() {
        return "{stackId=" + this.mStackId + " tasks=" + this.mChildren + "}";
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public String getName() {
        return toShortString();
    }

    public String toShortString() {
        return "Stack=" + this.mStackId;
    }

    /* access modifiers changed from: package-private */
    public int getDockSide() {
        return getDockSide(this.mDisplayContent.getConfiguration(), getRawBounds());
    }

    /* access modifiers changed from: package-private */
    public int getHwDockSide() {
        Rect displayRect = this.mDisplayContent.getConfiguration().windowConfiguration.getBounds();
        Rect bounds = getRawBounds();
        int diff = (displayRect.bottom - bounds.bottom) - (bounds.top - displayRect.top);
        if (diff > 0) {
            return 2;
        }
        if (diff < 0) {
            return 4;
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int getDockSideForDisplay(DisplayContent dc) {
        return getDockSide(dc, dc.getConfiguration(), getRawBounds());
    }

    /* access modifiers changed from: package-private */
    public int getDockSide(Configuration parentConfig, Rect bounds) {
        if (this.mDisplayContent == null) {
            return -1;
        }
        return getDockSide(this.mDisplayContent, parentConfig, bounds);
    }

    private int getDockSide(DisplayContent dc, Configuration parentConfig, Rect bounds) {
        return dc.getDockedDividerController().getDockSide(bounds, parentConfig.windowConfiguration.getBounds(), parentConfig.orientation, parentConfig.windowConfiguration.getRotation());
    }

    /* access modifiers changed from: package-private */
    public boolean hasTaskForUser(int userId) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((Task) this.mChildren.get(i)).mUserId == userId) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void findTaskForResizePoint(int x, int y, int delta, DisplayContent.TaskForResizePointSearchResult results) {
        boolean z = true;
        if ((getWindowConfiguration().canResizeTask() || getWindowConfiguration().inHwFreeFormWindowingMode()) && !this.mWmService.mAtmService.mHwATMSEx.isPadCastStack(this.mActivityStack) && !inHwPCMultiStackWindowingMode()) {
            int i = this.mChildren.size() - 1;
            while (i >= 0) {
                Task task = (Task) this.mChildren.get(i);
                if (task.getWindowingMode() == z) {
                    results.searchDone = z;
                    return;
                }
                task.getDimBounds(this.mTmpRect);
                if (task.inHwFreeFormWindowingMode()) {
                    int left = this.mTmpRect.left;
                    int top = this.mTmpRect.top;
                    this.mTmpRect.scale(this.mHwStackScale);
                    this.mTmpRect.offsetTo(left, top);
                    Rect newTmpRect = new Rect(this.mTmpRect);
                    int inSizeWidth = getDisplayContent().getInSizeOrOutWidth(0);
                    Rect appBounds = new Rect(this.mTmpRect);
                    appBounds.inset(inSizeWidth, inSizeWidth);
                    WindowManagerService windowManagerService = this.mWmService;
                    int hotAreaHeight = (this.mTmpRect.bottom - this.mTmpRect.top) - (((int) (((float) WindowManagerService.dipToPixel(36, getDisplayContent().getDisplayMetrics())) * this.mHwStackScale)) * 2);
                    int hotAreaHeight2 = hotAreaHeight < 0 ? 0 : hotAreaHeight;
                    if (newTmpRect.height() > hotAreaHeight2) {
                        newTmpRect.set(this.mTmpRect.left, this.mTmpRect.bottom - hotAreaHeight2, this.mTmpRect.right, this.mTmpRect.bottom);
                    }
                    newTmpRect.inset(-delta, 0, -delta, -delta);
                    if (newTmpRect.contains(x, y)) {
                        results.searchDone = true;
                        if (!appBounds.contains(x, y)) {
                            results.taskForResize = task;
                            return;
                        }
                        return;
                    }
                } else {
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
                i--;
                z = true;
            }
            return;
        }
        results.searchDone = true;
    }

    /* access modifiers changed from: package-private */
    public void setTouchExcludeRegion(Task focusedTask, int delta, Region touchExcludeRegion, Rect contentRect, Rect postExclude) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            Task task = (Task) this.mChildren.get(i);
            AppWindowToken token = task.getTopVisibleAppToken();
            if (token != null && token.hasContentToDisplay()) {
                if (!task.isActivityTypeHome() || !isMinimizedDockAndHomeStackResizable()) {
                    task.getDimBounds(this.mTmpRect);
                } else {
                    this.mDisplayContent.getBounds(this.mTmpRect);
                }
                if (task.inHwFreeFormWindowingMode()) {
                    int left = this.mTmpRect.left;
                    int top = this.mTmpRect.top;
                    this.mTmpRect.scale(this.mHwStackScale);
                    this.mTmpRect.offsetTo(left, top);
                }
                if (task == focusedTask) {
                    if (task.inHwFreeFormWindowingMode()) {
                        int inSizeWidth = getDisplayContent().getInSizeOrOutWidth(0);
                        this.mTmpRect.inset(inSizeWidth, inSizeWidth);
                    }
                    postExclude.set(this.mTmpRect);
                }
                boolean isFreeformed = task.inFreeformWindowingMode();
                if (task != focusedTask || isFreeformed || task.inHwFreeFormWindowingMode()) {
                    if (isFreeformed || task.inHwFreeFormWindowingMode()) {
                        this.mTmpRect.inset(-delta, -delta);
                        this.mTmpRect.intersect(contentRect);
                    }
                    touchExcludeRegion.op(this.mTmpRect, Region.Op.DIFFERENCE);
                }
            }
        }
    }

    @Override // com.android.server.wm.BoundsAnimationTarget
    public boolean setPinnedStackSize(Rect stackBounds, Rect tempTaskBounds) {
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mCancelCurrentBoundsAnimation) {
                    return false;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                try {
                    this.mWmService.mActivityTaskManager.resizePinnedStack(stackBounds, tempTaskBounds);
                    return true;
                } catch (RemoteException e) {
                    return true;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onAllWindowsDrawn() {
        if (this.mBoundsAnimating || this.mBoundsAnimatingRequested) {
            getDisplayContent().mBoundsAnimationController.onAllWindowsDrawn();
        }
    }

    @Override // com.android.server.wm.BoundsAnimationTarget
    public boolean onAnimationStart(boolean schedulePipModeChangedCallback, boolean forceUpdate, @BoundsAnimationController.AnimationType int animationType) {
        ActivityStack activityStack;
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (!isAttached()) {
                    return false;
                }
                this.mBoundsAnimatingRequested = false;
                this.mBoundsAnimating = true;
                this.mAnimationType = animationType;
                if (schedulePipModeChangedCallback) {
                    forAllWindows((Consumer<WindowState>) $$Lambda$TaskStack$NPerlV3pAikqmRCCx3JO0qCLTyw.INSTANCE, false);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        if (inPinnedWindowingMode()) {
            try {
                this.mWmService.mActivityTaskManager.notifyPinnedStackAnimationStarted();
            } catch (RemoteException e) {
            }
            if ((schedulePipModeChangedCallback || animationType == 1) && (activityStack = this.mActivityStack) != null) {
                activityStack.updatePictureInPictureModeForPinnedStackAnimation(null, forceUpdate);
            }
        }
        return true;
    }

    @Override // com.android.server.wm.BoundsAnimationTarget
    public void onAnimationEnd(boolean schedulePipModeChangedCallback, Rect finalStackSize, boolean moveToFullscreen) {
        if (inPinnedWindowingMode()) {
            if (schedulePipModeChangedCallback) {
                this.mActivityStack.updatePictureInPictureModeForPinnedStackAnimation(this.mBoundsAnimationTarget, false);
            }
            if (this.mAnimationType == 1) {
                setPinnedStackAlpha(1.0f);
                this.mActivityStack.mService.notifyPinnedStackAnimationEnded();
                return;
            }
            if (finalStackSize == null || this.mCancelCurrentBoundsAnimation) {
                onPipAnimationEndResize();
            } else {
                setPinnedStackSize(finalStackSize, null);
            }
            this.mActivityStack.mService.notifyPinnedStackAnimationEnded();
            if (moveToFullscreen) {
                this.mActivityStack.mService.moveTasksToFullscreenStack(this.mStackId, true);
                return;
            }
            return;
        }
        onPipAnimationEndResize();
    }

    /* access modifiers changed from: package-private */
    public Rect getPictureInPictureBounds(float aspectRatio, Rect stackBounds) {
        DisplayContent displayContent;
        if (!this.mWmService.mSupportsPictureInPicture || (displayContent = getDisplayContent()) == null || !inPinnedWindowingMode()) {
            return null;
        }
        PinnedStackController pinnedStackController = displayContent.getPinnedStackController();
        if (stackBounds == null) {
            stackBounds = pinnedStackController.getDefaultOrLastSavedBounds();
        }
        if (pinnedStackController.isValidPictureInPictureAspectRatio(aspectRatio)) {
            return pinnedStackController.transformBoundsToAspectRatio(stackBounds, aspectRatio, true);
        }
        return stackBounds;
    }

    /* access modifiers changed from: package-private */
    public void animateResizePinnedStack(Rect toBounds, Rect sourceHintBounds, int animationDuration, boolean fromFullscreen) {
        int schedulePipModeChangedState;
        Rect toBounds2;
        int intendedAnimationType;
        if (inPinnedWindowingMode()) {
            Rect fromBounds = new Rect();
            getBounds(fromBounds);
            boolean toFullscreen = toBounds == null;
            if (toFullscreen) {
                if (!fromFullscreen) {
                    this.mWmService.getStackBounds(1, 1, this.mTmpToBounds);
                    if (!this.mTmpToBounds.isEmpty()) {
                        schedulePipModeChangedState = 1;
                        toBounds2 = new Rect(this.mTmpToBounds);
                    } else {
                        Rect toBounds3 = new Rect();
                        getDisplayContent().getBounds(toBounds3);
                        schedulePipModeChangedState = 1;
                        toBounds2 = toBounds3;
                    }
                } else {
                    throw new IllegalArgumentException("Should not defer scheduling PiP mode change on animation to fullscreen.");
                }
            } else if (fromFullscreen) {
                toBounds2 = toBounds;
                schedulePipModeChangedState = 2;
            } else {
                toBounds2 = toBounds;
                schedulePipModeChangedState = 0;
            }
            setAnimationFinalBounds(sourceHintBounds, toBounds2, toFullscreen);
            DisplayContent displayContent = getDisplayContent();
            int intendedAnimationType2 = displayContent.mBoundsAnimationController.getAnimationType();
            if (intendedAnimationType2 == 1) {
                if (fromFullscreen) {
                    setPinnedStackAlpha(0.0f);
                }
                if (toBounds2.width() == fromBounds.width() && toBounds2.height() == fromBounds.height()) {
                    intendedAnimationType2 = 0;
                }
                if (fromFullscreen || toFullscreen) {
                    intendedAnimationType = intendedAnimationType2;
                } else {
                    intendedAnimationType = 0;
                }
            } else {
                intendedAnimationType = intendedAnimationType2;
            }
            this.mCancelCurrentBoundsAnimation = false;
            displayContent.mBoundsAnimationController.getHandler().post(new Runnable(displayContent, fromBounds, toBounds2, animationDuration, schedulePipModeChangedState, fromFullscreen, toFullscreen, intendedAnimationType) {
                /* class com.android.server.wm.$$Lambda$TaskStack$Vzix6ElfYqr96C0Kgjxo_MdVpAg */
                private final /* synthetic */ DisplayContent f$1;
                private final /* synthetic */ Rect f$2;
                private final /* synthetic */ Rect f$3;
                private final /* synthetic */ int f$4;
                private final /* synthetic */ int f$5;
                private final /* synthetic */ boolean f$6;
                private final /* synthetic */ boolean f$7;
                private final /* synthetic */ int f$8;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                    this.f$7 = r8;
                    this.f$8 = r9;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    TaskStack.this.lambda$animateResizePinnedStack$3$TaskStack(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
                }
            });
        }
    }

    public /* synthetic */ void lambda$animateResizePinnedStack$3$TaskStack(DisplayContent displayContent, Rect fromBounds, Rect finalToBounds, int animationDuration, int finalSchedulePipModeChangedState, boolean fromFullscreen, boolean toFullscreen, int animationType) {
        displayContent.mBoundsAnimationController.animateBounds(this, fromBounds, finalToBounds, animationDuration, finalSchedulePipModeChangedState, fromFullscreen, toFullscreen, animationType);
    }

    /* access modifiers changed from: package-private */
    public void setPictureInPictureAspectRatio(float aspectRatio) {
        if (this.mWmService.mSupportsPictureInPicture && inPinnedWindowingMode()) {
            PinnedStackController pinnedStackController = getDisplayContent().getPinnedStackController();
            if (Float.compare(aspectRatio, pinnedStackController.getAspectRatio()) != 0) {
                getAnimationOrCurrentBounds(this.mTmpFromBounds);
                this.mTmpToBounds.set(this.mTmpFromBounds);
                getPictureInPictureBounds(aspectRatio, this.mTmpToBounds);
                if (!this.mTmpToBounds.equals(this.mTmpFromBounds)) {
                    animateResizePinnedStack(this.mTmpToBounds, null, -1, false);
                }
                pinnedStackController.setAspectRatio(pinnedStackController.isValidPictureInPictureAspectRatio(aspectRatio) ? aspectRatio : -1.0f);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPictureInPictureActions(List<RemoteAction> actions) {
        if (this.mWmService.mSupportsPictureInPicture && inPinnedWindowingMode()) {
            getDisplayContent().getPinnedStackController().setActions(actions);
        }
    }

    @Override // com.android.server.wm.BoundsAnimationTarget
    public boolean isAttached() {
        boolean z;
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                z = this.mDisplayContent != null;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public void onPipAnimationEndResize() {
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mBoundsAnimating = false;
                for (int i = 0; i < this.mChildren.size(); i++) {
                    ((Task) this.mChildren.get(i)).clearPreserveNonFloatingState();
                }
                this.mWmService.requestTraversal();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    @Override // com.android.server.wm.BoundsAnimationTarget
    public boolean shouldDeferStartOnMoveToFullscreen() {
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                boolean z = false;
                if (!isAttached()) {
                    return false;
                }
                TaskStack homeStack = this.mDisplayContent.getHomeStack();
                if (homeStack == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return true;
                }
                Task homeTask = (Task) homeStack.getTopChild();
                if (homeTask == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return true;
                }
                AppWindowToken homeApp = homeTask.getTopVisibleAppToken();
                if (!homeTask.isVisible() || homeApp == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return true;
                }
                if (!homeApp.allDrawn) {
                    z = true;
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return z;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean deferScheduleMultiWindowModeChanged() {
        if (!inPinnedWindowingMode()) {
            return false;
        }
        if (this.mBoundsAnimatingRequested || this.mBoundsAnimating) {
            return true;
        }
        return false;
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
        return isAnimatingBounds() && lastAnimatingBoundsWasToFullscreen();
    }

    public boolean pinnedStackResizeDisallowed() {
        if (!this.mBoundsAnimating || !this.mCancelCurrentBoundsAnimation) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean checkCompleteDeferredRemoval() {
        if (isSelfOrChildAnimating()) {
            return true;
        }
        if (this.mDeferRemoval) {
            removeImmediately();
        }
        return super.checkCompleteDeferredRemoval();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public int getOrientation() {
        if (canSpecifyOrientation()) {
            return super.getOrientation();
        }
        return -2;
    }

    private boolean canSpecifyOrientation() {
        int windowingMode = getWindowingMode();
        int activityType = getActivityType();
        return windowingMode == 1 || windowingMode == 11 || windowingMode == 12 || activityType == 2 || activityType == 3 || activityType == 4;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public Dimmer getDimmer() {
        return this.mDimmer;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void prepareSurfaces() {
        this.mDimmer.resetDimStates();
        super.prepareSurfaces();
        getDimBounds(this.mTmpDimBoundsRect);
        this.mTmpDimBoundsRect.offsetTo(0, 0);
        if (this.mDimmer.updateDims(getPendingTransaction(), this.mTmpDimBoundsRect)) {
            if (this.mActivityStack.inHwFreeFormWindowingMode() && !this.mActivityStack.inHwPCMultiStackWindowingMode()) {
                this.mDimmer.updateDimsCornerRadius(getPendingTransaction(), (float) this.mActivityStack.mService.mContext.getResources().getDimensionPixelSize(34472614));
            }
            if (this.mActivityStack.inHwSplitScreenWindowingMode()) {
                this.mDimmer.updateDimsCornerRadius(getPendingTransaction(), HwActivityManager.IS_PHONE ? 0.0f : (float) this.mActivityStack.mService.mContext.getResources().getDimensionPixelSize(34472617));
            }
            scheduleAnimation();
        }
    }

    @Override // com.android.server.wm.BoundsAnimationTarget
    public boolean setPinnedStackAlpha(float alpha) {
        synchronized (this.mWmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                SurfaceControl sc = getSurfaceControl();
                boolean z = false;
                if (sc != null) {
                    if (sc.isValid()) {
                        getPendingTransaction().setAlpha(sc, this.mCancelCurrentBoundsAnimation ? 1.0f : alpha);
                        scheduleAnimation();
                        if (!this.mCancelCurrentBoundsAnimation) {
                            z = true;
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return z;
                    }
                }
                return false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public DisplayInfo getDisplayInfo() {
        return this.mDisplayContent.getDisplayInfo();
    }

    /* access modifiers changed from: package-private */
    public void dim(float alpha) {
        this.mDimmer.dimAbove(getPendingTransaction(), alpha);
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    public void stopDimming() {
        this.mDimmer.stopDim(getPendingTransaction());
        scheduleAnimation();
    }

    /* access modifiers changed from: package-private */
    public AnimatingAppWindowTokenRegistry getAnimatingAppWindowTokenRegistry() {
        return this.mAnimatingAppWindowTokenRegistry;
    }

    /* access modifiers changed from: package-private */
    public int taskIdFromPoint(int x, int y) {
        getBounds(this.mTmpRect);
        if (!this.mTmpRect.contains(x, y) || isAdjustedForMinimizedDockedStack()) {
            return -1;
        }
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            Task task = (Task) this.mChildren.get(taskNdx);
            if (task.getTopVisibleAppMainWindow() != null) {
                task.getDimBounds(this.mTmpRect);
                if (this.mTmpRect.contains(x, y)) {
                    return task.mTaskId;
                }
            }
        }
        return -1;
    }

    public void clearAdjustedBounds() {
        this.mTmpAdjustedBounds.setEmpty();
        this.mAdjustedBounds.setEmpty();
    }

    private void getStackCoordinationModeBounds(Rect outBounds) {
        boolean inCoordinationPrimary = inCoordinationPrimaryWindowingMode();
        boolean isTopsecondaryCoordinationStack = false;
        TaskStack secondaryCoordinationStack = this.mDisplayContent.getTopStackInWindowingMode(12);
        if (secondaryCoordinationStack != null && secondaryCoordinationStack.mStackId == this.mStackId) {
            isTopsecondaryCoordinationStack = true;
        }
        CoordinationModeUtils.getInstance(this.mWmService.mContext).getStackCoordinationModeBounds(inCoordinationPrimary || !isTopsecondaryCoordinationStack, getDisplayContent().mDisplayInfo.rotation, outBounds);
    }

    /* access modifiers changed from: package-private */
    public void setInHwFreeFormMoveBackOrCloseState(boolean state) {
        this.mInHwFreeFormMoveBackOrCloseState = state;
    }

    /* access modifiers changed from: package-private */
    public boolean inHwFreeFormMoveBackOrCloseState() {
        return this.mInHwFreeFormMoveBackOrCloseState;
    }

    /* access modifiers changed from: package-private */
    public void setAlpha(SurfaceControl.Transaction t, float alpha) {
        if (t == null || this.mSurfaceControl == null) {
            Slog.w("WindowManager", "setAlpha return, t or mSurfaceControl is null");
            return;
        }
        t.setAlpha(this.mSurfaceControl, alpha);
        t.apply();
    }

    /* access modifiers changed from: package-private */
    public void setTvSplitExitTop(boolean isTop) {
        this.mIsTvSplitExitTop = isTop;
        Slog.w("WindowManager", "setTvSplitExitTop " + isTop);
    }

    /* access modifiers changed from: package-private */
    public boolean isTvSplitExitTop() {
        return this.mIsTvSplitExitTop;
    }
}
