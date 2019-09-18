package com.android.server.wm;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.freeform.HwFreeFormUtils;
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
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DockedDividerUtils;
import com.android.server.EventLogTags;
import com.android.server.HwServiceExFactory;
import com.android.server.os.HwBootFail;
import com.android.server.wm.DisplayContent;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.function.Consumer;

public class TaskStack extends AbsTaskStack implements BoundsAnimationTarget {
    private static final float ADJUSTED_STACK_FRACTION_MIN = 0.3f;
    private static final float IME_ADJUST_DIM_AMOUNT = 0.25f;
    private float mAdjustDividerAmount;
    private float mAdjustImeAmount;
    private final Rect mAdjustedBounds = new Rect();
    private boolean mAdjustedForIme;
    private final AnimatingAppWindowTokenRegistry mAnimatingAppWindowTokenRegistry = new AnimatingAppWindowTokenRegistry();
    private WindowStateAnimator mAnimationBackgroundAnimator;
    private SurfaceControl mAnimationBackgroundSurface;
    private boolean mAnimationBackgroundSurfaceIsShown = false;
    private final Rect mBoundsAfterRotation = new Rect();
    private boolean mBoundsAnimating = false;
    private boolean mBoundsAnimatingRequested = false;
    private boolean mBoundsAnimatingToFullscreen = false;
    private Rect mBoundsAnimationSourceHintBounds = new Rect();
    private Rect mBoundsAnimationTarget = new Rect();
    private boolean mCancelCurrentBoundsAnimation = false;
    boolean mDeferRemoval;
    private int mDensity;
    private Dimmer mDimmer = new Dimmer(this);
    private DisplayContent mDisplayContent;
    private final int mDockedStackMinimizeThickness;
    final AppTokenList mExitingAppTokens = new AppTokenList();
    private final Rect mFullyAdjustedImeBounds = new Rect();
    IHwTaskStackEx mHwTSEx = null;
    private boolean mImeGoingAway;
    private WindowState mImeWin;
    private final Point mLastSurfaceSize = new Point();
    private float mMinimizeAmount;
    Rect mPreAnimationBounds = new Rect();
    private int mRotation;
    final int mStackId;
    private final Rect mTmpAdjustedBounds = new Rect();
    final AppTokenList mTmpAppTokens = new AppTokenList();
    final Rect mTmpDimBoundsRect = new Rect();
    private Rect mTmpRect = new Rect();
    private Rect mTmpRect2 = new Rect();
    private Rect mTmpRect3 = new Rect();

    public TaskStack(WindowManagerService service, int stackId, StackWindowController controller) {
        super(service);
        this.mStackId = stackId;
        setController(controller);
        this.mDockedStackMinimizeThickness = service.mContext.getResources().getDimensionPixelSize(17105036);
        EventLog.writeEvent(EventLogTags.WM_STACK_CREATED, stackId);
        this.mHwTSEx = HwServiceExFactory.getHwTaskStackEx(this, service);
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getDisplayContent() {
        return this.mDisplayContent;
    }

    /* access modifiers changed from: package-private */
    public Task findHomeTask() {
        if (!isActivityTypeHome() || this.mChildren.isEmpty()) {
            return null;
        }
        return (Task) this.mChildren.get(this.mChildren.size() - 1);
    }

    /* access modifiers changed from: package-private */
    public boolean setBounds(Rect stackBounds, SparseArray<Rect> taskBounds, SparseArray<Rect> taskTempInsetBounds) {
        setBounds(stackBounds);
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            Task task = (Task) this.mChildren.get(taskNdx);
            task.setBounds(taskBounds.get(task.mTaskId), false);
            task.setTempInsetBounds(taskTempInsetBounds != null ? taskTempInsetBounds.get(task.mTaskId) : null);
        }
        return true;
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
            int taskNdx = this.mChildren.size() - 1;
            while (true) {
                int taskNdx2 = taskNdx;
                if (taskNdx2 >= 0) {
                    ((Task) this.mChildren.get(taskNdx2)).alignToAdjustedBounds(adjustedBounds, tempInsetBounds, alignBottom);
                    taskNdx = taskNdx2 - 1;
                } else {
                    return;
                }
            }
        }
    }

    private void updateAnimationBackgroundBounds() {
        if (this.mAnimationBackgroundSurface != null) {
            getRawBounds(this.mTmpRect);
            Rect stackBounds = getBounds();
            getPendingTransaction().setSize(this.mAnimationBackgroundSurface, this.mTmpRect.width(), this.mTmpRect.height()).setPosition(this.mAnimationBackgroundSurface, (float) (this.mTmpRect.left - stackBounds.left), (float) (this.mTmpRect.top - stackBounds.top));
            scheduleAnimation();
        }
    }

    private void hideAnimationSurface() {
        if (this.mAnimationBackgroundSurface != null) {
            getPendingTransaction().hide(this.mAnimationBackgroundSurface);
            this.mAnimationBackgroundSurfaceIsShown = false;
            scheduleAnimation();
        }
    }

    private void showAnimationSurface(float alpha) {
        if (this.mAnimationBackgroundSurface != null) {
            getPendingTransaction().setLayer(this.mAnimationBackgroundSurface, Integer.MIN_VALUE).setAlpha(this.mAnimationBackgroundSurface, alpha).show(this.mAnimationBackgroundSurface);
            this.mAnimationBackgroundSurfaceIsShown = true;
            scheduleAnimation();
        }
    }

    public int setBounds(Rect bounds) {
        return setBounds(getOverrideBounds(), bounds);
    }

    private int setBounds(Rect existing, Rect bounds) {
        int rotation = 0;
        int density = 0;
        if (this.mDisplayContent != null) {
            this.mDisplayContent.getBounds(this.mTmpRect);
            rotation = this.mDisplayContent.getDisplayInfo().rotation;
            density = this.mDisplayContent.getDisplayInfo().logicalDensityDpi;
        }
        if (equivalentBounds(existing, bounds) && this.mRotation == rotation) {
            return 0;
        }
        int result = super.setBounds(bounds);
        if (this.mDisplayContent != null) {
            updateAnimationBackgroundBounds();
        }
        this.mRotation = rotation;
        this.mDensity = density;
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

    private boolean useCurrentBounds() {
        if (matchParentBounds() || !inSplitScreenSecondaryWindowingMode() || this.mDisplayContent == null || this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility() != null) {
            return true;
        }
        return false;
    }

    public void getBounds(Rect bounds) {
        bounds.set(getBounds());
    }

    public Rect getBounds() {
        if (!useCurrentBounds()) {
            return this.mDisplayContent.getBounds();
        }
        if (!this.mAdjustedBounds.isEmpty()) {
            return this.mAdjustedBounds;
        }
        return super.getBounds();
    }

    /* access modifiers changed from: package-private */
    public void setAnimationFinalBounds(Rect sourceHintBounds, Rect destBounds, boolean toFullscreen) {
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
    public void updateDisplayInfo(Rect bounds) {
        if (this.mDisplayContent != null) {
            for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
                ((Task) this.mChildren.get(taskNdx)).updateDisplayInfo(this.mDisplayContent);
            }
            if (bounds != null) {
                setBounds(bounds);
            } else if (matchParentBounds()) {
                setBounds(null);
            } else {
                this.mTmpRect2.set(getRawBounds());
                int newRotation = this.mDisplayContent.getDisplayInfo().rotation;
                int newDensity = this.mDisplayContent.getDisplayInfo().logicalDensityDpi;
                if (this.mRotation == newRotation && this.mDensity == newDensity) {
                    setBounds(this.mTmpRect2);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateBoundsAfterConfigChange() {
        int i = 0;
        if (this.mDisplayContent == null) {
            return false;
        }
        if (inPinnedWindowingMode()) {
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
        if (this.mRotation == newRotation && this.mDensity == newDensity && !inSplitScreenPrimaryWindowingMode()) {
            return false;
        }
        if (matchParentBounds()) {
            setBounds(null);
            return false;
        }
        this.mTmpRect2.set(getRawBounds());
        this.mDisplayContent.rotateBounds(this.mRotation, newRotation, this.mTmpRect2);
        if (inSplitScreenPrimaryWindowingMode()) {
            repositionPrimarySplitScreenStackAfterRotation(this.mTmpRect2);
            snapDockedStackAfterRotation(this.mTmpRect2);
            int newDockSide = getDockSide(this.mTmpRect2);
            WindowManagerService windowManagerService = this.mService;
            if (!(newDockSide == 1 || newDockSide == 2)) {
                i = 1;
            }
            windowManagerService.setDockedStackCreateStateLocked(i, null);
            this.mDisplayContent.getDockedDividerController().notifyDockSideChanged(newDockSide);
        }
        this.mBoundsAfterRotation.set(this.mTmpRect2);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void getBoundsForNewConfiguration(Rect outBounds) {
        outBounds.set(this.mBoundsAfterRotation);
        this.mBoundsAfterRotation.setEmpty();
    }

    private void repositionPrimarySplitScreenStackAfterRotation(Rect inOutBounds) {
        int dockSide = getDockSide(inOutBounds);
        if (!this.mDisplayContent.getDockedDividerController().canPrimaryStackDockTo(dockSide)) {
            this.mDisplayContent.getBounds(this.mTmpRect);
            switch (DockedDividerUtils.invertDockSide(dockSide)) {
                case 1:
                    int movement = inOutBounds.left;
                    inOutBounds.left -= movement;
                    inOutBounds.right -= movement;
                    break;
                case 2:
                    int movement2 = inOutBounds.top;
                    inOutBounds.top -= movement2;
                    inOutBounds.bottom -= movement2;
                    break;
                case 3:
                    int movement3 = this.mTmpRect.right - inOutBounds.right;
                    inOutBounds.left += movement3;
                    inOutBounds.right += movement3;
                    break;
                case 4:
                    int movement4 = this.mTmpRect.bottom - inOutBounds.bottom;
                    inOutBounds.top += movement4;
                    inOutBounds.bottom += movement4;
                    break;
            }
        }
    }

    private void snapDockedStackAfterRotation(Rect outBounds) {
        int dockSide;
        int dockSide2;
        DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
        int dividerSize = this.mDisplayContent.getDockedDividerController().getContentWidth();
        int dockSide3 = getDockSide(outBounds);
        Rect rect = outBounds;
        int dividerPosition = DockedDividerUtils.calculatePositionForBounds(rect, dockSide3, dividerSize);
        int displayWidth = displayInfo.logicalWidth;
        int displayHeight = displayInfo.logicalHeight;
        int rotation = displayInfo.rotation;
        int orientation = this.mDisplayContent.getConfiguration().orientation;
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mDisplayContent.getDisplayId())) {
            dockSide = dockSide3;
            dockSide2 = orientation;
            this.mService.mPolicy.getStableInsetsLw(rotation, displayWidth, displayHeight, displayInfo.displayCutout, rect);
        } else {
            dockSide = dockSide3;
            dockSide2 = orientation;
            this.mService.mPolicy.getStableInsetsLw(rotation, displayWidth, displayHeight, rect, this.mDisplayContent.getDisplayId(), displayInfo.displayCutout);
        }
        Resources resources = this.mService.mContext.getResources();
        boolean z = true;
        if (dockSide2 != 1) {
            z = false;
        }
        boolean z2 = z;
        int i = rotation;
        int rotation2 = getDockSide();
        int i2 = displayHeight;
        DividerSnapAlgorithm algorithm = new DividerSnapAlgorithm(resources, displayWidth, displayHeight, dividerSize, z2, rect, rotation2, isMinimizedDockAndHomeStackResizable());
        DockedDividerUtils.calculateBoundsForPosition(algorithm.calculateNonDismissingSnapTarget(dividerPosition).position, dockSide, rect, displayInfo.logicalWidth, displayInfo.logicalHeight, dividerSize);
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
            addChild(task, (Comparator) null);
            positionChildAt(position, task, moveParents, showForAllUsers);
            return;
        }
        throw new IllegalStateException("Trying to add taskId=" + task.mTaskId + " to stackId=" + this.mStackId + ", but it is already attached to stackId=" + task.mStack.mStackId);
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, Task child, boolean includingParents) {
        positionChildAt(position, child, includingParents, child.showForAllUsers());
    }

    private void positionChildAt(int position, Task child, boolean includingParents, boolean showForAllUsers) {
        int targetPosition = findPositionForTask(child, position, showForAllUsers, false);
        super.positionChildAt(targetPosition, child, includingParents);
        EventLog.writeEvent(EventLogTags.WM_TASK_MOVED, new Object[]{Integer.valueOf(child.mTaskId), Integer.valueOf(targetPosition == this.mChildren.size() - 1 ? 1 : 0), Integer.valueOf(targetPosition)});
    }

    private int findPositionForTask(Task task, int targetPosition, boolean showForAllUsers, boolean addingNew) {
        boolean canShowTask = showForAllUsers || this.mService.isCurrentProfileLocked(task.mUserId);
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
                return HwBootFail.STAGE_BOOT_SUCCESS;
            }
        }
        return Math.min(Math.max(targetPosition, minPosition), maxPosition);
    }

    private int computeMinPosition(int minPosition, int size) {
        while (minPosition < size) {
            Task tmpTask = (Task) this.mChildren.get(minPosition);
            if (tmpTask.showForAllUsers() || this.mService.isCurrentProfileLocked(tmpTask.mUserId)) {
                break;
            }
            minPosition++;
        }
        return minPosition;
    }

    private int computeMaxPosition(int maxPosition) {
        while (maxPosition > 0) {
            Task tmpTask = (Task) this.mChildren.get(maxPosition);
            if (!(tmpTask.showForAllUsers() || this.mService.isCurrentProfileLocked(tmpTask.mUserId))) {
                break;
            }
            maxPosition--;
        }
        return maxPosition;
    }

    /* access modifiers changed from: package-private */
    public void removeChild(Task task) {
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

    public void onConfigurationChanged(Configuration newParentConfig) {
        int prevWindowingMode = getWindowingMode();
        super.onConfigurationChanged(newParentConfig);
        updateSurfaceSize(getPendingTransaction());
        int windowingMode = getWindowingMode();
        if (this.mDisplayContent != null && prevWindowingMode != windowingMode) {
            this.mDisplayContent.onStackWindowingModeChanged(this);
            updateBoundsForWindowModeChange();
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
        WindowManagerService windowManagerService = this.mService;
        return (int) Math.ceil((double) (WindowManagerService.dipToPixel(5, displayMetrics) * 2));
    }

    /* access modifiers changed from: protected */
    public void updateSurfaceSize(SurfaceControl.Transaction transaction) {
        if (this.mSurfaceControl != null) {
            Rect stackBounds = getBounds();
            int width = stackBounds.width();
            int height = stackBounds.height();
            int outset = getStackOutset();
            int width2 = width + (2 * outset);
            int height2 = height + (2 * outset);
            if (this.mService.getLazyMode() != 0 && inMultiWindowMode() && !HwPCUtils.isPcDynamicStack(this.mStackId)) {
                width2 = (int) (((float) width2) * 0.75f);
                height2 = (int) (((float) height2) * 0.75f);
            }
            if (HwFoldScreenState.isFoldScreenDevice() && this.mService.isInSubFoldScaleMode() && inMultiWindowMode()) {
                width2 = (int) (((float) width2) * this.mService.mSubFoldModeScale);
                height2 = (int) (((float) height2) * this.mService.mSubFoldModeScale);
            }
            if (width2 != this.mLastSurfaceSize.x || height2 != this.mLastSurfaceSize.y) {
                transaction.setSize(this.mSurfaceControl, width2, height2);
                this.mLastSurfaceSize.set(width2, height2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDisplayChanged(DisplayContent dc) {
        if (this.mDisplayContent == null) {
            this.mDisplayContent = dc;
            updateBoundsForWindowModeChange();
            SurfaceControl.Builder colorLayer = makeChildSurface(null).setColorLayer(true);
            this.mAnimationBackgroundSurface = colorLayer.setName("animation background stackId=" + this.mStackId).build();
            super.onDisplayChanged(dc);
            return;
        }
        throw new IllegalStateException("onDisplayChanged: Already attached");
    }

    private void updateBoundsForWindowModeChange() {
        Rect bounds = calculateBoundsForWindowModeChange();
        if (inSplitScreenSecondaryWindowingMode()) {
            forAllWindows((Consumer<WindowState>) $$Lambda$TaskStack$0Cm5zc_NsRa5nGarFvrp2KYfUYU.INSTANCE, true);
        }
        updateDisplayInfo(bounds);
        updateSurfaceBounds();
    }

    private Rect calculateBoundsForWindowModeChange() {
        boolean inSplitScreenPrimary = inSplitScreenPrimaryWindowingMode();
        TaskStack splitScreenStack = this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility();
        boolean isFoldableDevice = CoordinationModeUtils.isFoldable();
        boolean inCoordinationPrimary = isFoldableDevice && inCoordinationPrimaryWindowingMode();
        TaskStack coordinationPrimaryStack = isFoldableDevice ? this.mDisplayContent.getCoordinationPrimaryStackIgnoringVisibility() : null;
        if (inSplitScreenPrimary || (splitScreenStack != null && inSplitScreenSecondaryWindowingMode() && !splitScreenStack.fillsParent())) {
            Rect bounds = new Rect();
            this.mDisplayContent.getBounds(this.mTmpRect);
            this.mTmpRect2.setEmpty();
            if (splitScreenStack != null) {
                if (!inSplitScreenSecondaryWindowingMode() || !this.mDisplayContent.mDividerControllerLocked.isMinimizedDock() || splitScreenStack.getTopChild() == null) {
                    splitScreenStack.getRawBounds(this.mTmpRect2);
                } else {
                    ((Task) splitScreenStack.getTopChild()).getBounds(this.mTmpRect2);
                }
            }
            getStackDockedModeBounds(this.mTmpRect, bounds, this.mTmpRect2, this.mDisplayContent.mDividerControllerLocked.getContentWidth(), this.mService.mDockedStackCreateMode == 0);
            return bounds;
        }
        if (inPinnedWindowingMode()) {
            getAnimationOrCurrentBounds(this.mTmpRect2);
            if (this.mDisplayContent.mPinnedStackControllerLocked.onTaskStackBoundsChanged(this.mTmpRect2, this.mTmpRect3)) {
                return new Rect(this.mTmpRect3);
            }
        } else if (inCoordinationPrimary || (coordinationPrimaryStack != null && inCoordinationSecondaryWindowingMode())) {
            Rect bounds2 = new Rect();
            this.mDisplayContent.getBounds(this.mTmpRect);
            this.mTmpRect2.setEmpty();
            if (coordinationPrimaryStack != null) {
                coordinationPrimaryStack.getRawBounds(this.mTmpRect2);
            }
            getStackCoordinationModeBounds(bounds2);
            return bounds2;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void getStackDockedModeBoundsLocked(Rect currentTempTaskBounds, Rect outStackBounds, Rect outTempTaskBounds, boolean ignoreVisibility) {
        outTempTaskBounds.setEmpty();
        boolean dockedOnTopOrLeft = true;
        if (isActivityTypeHome()) {
            Task homeTask = findHomeTask();
            if (homeTask == null || !homeTask.isResizeable()) {
                outStackBounds.setEmpty();
            } else if (!CoordinationModeUtils.isFoldable() || !inCoordinationSecondaryWindowingMode()) {
                getDisplayContent().mDividerControllerLocked.getHomeStackBoundsInDockedMode(outStackBounds);
            } else {
                CoordinationModeUtils.getInstance(this.mService.mContext).getStackCoordinationModeBounds(true, getDisplayContent().mDisplayInfo.rotation, outStackBounds);
            }
            outTempTaskBounds.set(outStackBounds);
        } else if (CoordinationModeUtils.isFoldable() && inCoordinationSecondaryWindowingMode()) {
            getStackCoordinationModeBounds(outStackBounds);
            outTempTaskBounds.set(outStackBounds);
        } else if (isMinimizedDockAndHomeStackResizable() && currentTempTaskBounds != null) {
            outStackBounds.set(currentTempTaskBounds);
        } else if (!inSplitScreenWindowingMode() || this.mDisplayContent == null) {
            outStackBounds.set(getRawBounds());
        } else {
            TaskStack dockedStack = this.mDisplayContent.getSplitScreenPrimaryStackIgnoringVisibility();
            if (dockedStack == null) {
                throw new IllegalStateException("Calling getStackDockedModeBoundsLocked() when there is no docked stack.");
            } else if (ignoreVisibility || dockedStack.isVisible()) {
                int dockedSide = dockedStack.getDockSide();
                if (dockedSide == -1) {
                    Slog.e("WindowManager", "Failed to get valid docked side for docked stack=" + dockedStack);
                    outStackBounds.set(getRawBounds());
                    return;
                }
                this.mDisplayContent.getBounds(this.mTmpRect);
                dockedStack.getRawBounds(this.mTmpRect2);
                if (!(dockedSide == 2 || dockedSide == 1)) {
                    dockedOnTopOrLeft = false;
                }
                Rect rect = outStackBounds;
                getStackDockedModeBounds(this.mTmpRect, rect, this.mTmpRect2, this.mDisplayContent.mDividerControllerLocked.getContentWidth(), dockedOnTopOrLeft);
            } else {
                this.mDisplayContent.getBounds(outStackBounds);
            }
        }
    }

    private void getStackDockedModeBounds(Rect displayRect, Rect outBounds, Rect dockedBounds, int dockDividerWidth, boolean dockOnTopOrLeft) {
        Rect rect = outBounds;
        Rect rect2 = dockedBounds;
        boolean dockedStack = inSplitScreenPrimaryWindowingMode();
        boolean splitHorizontally = displayRect.width() > displayRect.height();
        boolean shouldHorizontalSplit = false;
        if (this.mService.mHwWMSEx.isInFoldFullDisplayMode()) {
            shouldHorizontalSplit = true;
        }
        boolean shouldHorizontalSplit2 = shouldHorizontalSplit;
        rect.set(displayRect);
        if (!dockedStack) {
            if (!dockOnTopOrLeft) {
                if (splitHorizontally || shouldHorizontalSplit2) {
                    rect.right = rect2.left - dockDividerWidth;
                } else {
                    rect.bottom = rect2.top - dockDividerWidth;
                }
            } else if (splitHorizontally || shouldHorizontalSplit2) {
                rect.left = rect2.right + dockDividerWidth;
            } else {
                rect.top = rect2.bottom + dockDividerWidth;
            }
            DockedDividerUtils.sanitizeStackBounds(rect, !dockOnTopOrLeft);
        } else if (this.mService.mDockedStackCreateBounds != null) {
            rect.set(this.mService.mDockedStackCreateBounds);
        } else {
            DisplayInfo di = this.mDisplayContent.getDisplayInfo();
            if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mDisplayContent.getDisplayId())) {
                this.mService.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, di.displayCutout, this.mTmpRect2);
            } else {
                this.mService.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, this.mTmpRect2, this.mDisplayContent.getDisplayId(), di.displayCutout);
            }
            Resources resources = this.mService.mContext.getResources();
            int i = di.logicalWidth;
            int i2 = di.logicalHeight;
            boolean z = this.mDisplayContent.getConfiguration().orientation == 1;
            DisplayInfo di2 = this.mTmpRect2;
            DividerSnapAlgorithm dividerSnapAlgorithm = r3;
            DisplayInfo displayInfo = di;
            DividerSnapAlgorithm dividerSnapAlgorithm2 = new DividerSnapAlgorithm(resources, i, i2, dockDividerWidth, z, di2);
            int position = dividerSnapAlgorithm.getMiddleTarget().position;
            if (dockOnTopOrLeft) {
                if (splitHorizontally || shouldHorizontalSplit2) {
                    rect.right = position;
                } else {
                    rect.bottom = position;
                }
            } else if (splitHorizontally || shouldHorizontalSplit2) {
                rect.left = position + dockDividerWidth;
            } else {
                rect.top = position + dockDividerWidth;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resetDockedStackToMiddle() {
        if (!inSplitScreenPrimaryWindowingMode()) {
            this.mService.mDockedStackCreateBounds = null;
            Rect bounds = new Rect();
            getStackDockedModeBoundsLocked(null, bounds, new Rect(), true);
            getController().requestResize(bounds);
            return;
        }
        throw new IllegalStateException("Not a docked stack=" + this);
    }

    /* access modifiers changed from: package-private */
    public StackWindowController getController() {
        return (StackWindowController) super.getController();
    }

    /* access modifiers changed from: package-private */
    public void removeIfPossible() {
        if (isSelfOrChildAnimating()) {
            this.mDeferRemoval = true;
        } else {
            removeImmediately();
        }
    }

    /* access modifiers changed from: package-private */
    public void onParentSet() {
        super.onParentSet();
        if (getParent() == null && this.mDisplayContent != null) {
            EventLog.writeEvent(EventLogTags.WM_STACK_REMOVED, this.mStackId);
            if (this.mAnimationBackgroundSurface != null) {
                this.mAnimationBackgroundSurface.destroy();
                this.mAnimationBackgroundSurface = null;
            }
            this.mDisplayContent = null;
            this.mService.mWindowPlacerLocked.requestTraversal();
        }
    }

    /* access modifiers changed from: package-private */
    public void resetAnimationBackgroundAnimator() {
        this.mAnimationBackgroundAnimator = null;
        hideAnimationSurface();
    }

    /* access modifiers changed from: protected */
    public void setAnimationBackground(WindowStateAnimator winAnimator, int color) {
        int animLayer = winAnimator.mAnimLayer;
        if (this.mAnimationBackgroundAnimator == null || animLayer < this.mAnimationBackgroundAnimator.mAnimLayer) {
            this.mAnimationBackgroundAnimator = winAnimator;
            int animLayer2 = this.mDisplayContent.getLayerForAnimationBackground(winAnimator);
            showAnimationSurface(((float) ((color >> 24) & 255)) / 255.0f);
        }
    }

    /* access modifiers changed from: package-private */
    public void switchUser() {
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

    /* access modifiers changed from: package-private */
    public void setAdjustedForIme(WindowState imeWin, boolean forceUpdate) {
        this.mImeWin = imeWin;
        this.mImeGoingAway = false;
        if (!this.mAdjustedForIme || forceUpdate) {
            this.mAdjustedForIme = true;
            this.mAdjustImeAmount = 0.0f;
            this.mAdjustDividerAmount = 0.0f;
            updateAdjustForIme(0.0f, 0.0f, true);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAdjustedForIme() {
        return this.mAdjustedForIme;
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimatingForIme() {
        return this.mImeWin != null && this.mImeWin.isAnimatingLw();
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
        if (adjustBoundsNow) {
            this.mImeWin = null;
            this.mImeGoingAway = false;
            this.mAdjustImeAmount = 0.0f;
            this.mAdjustDividerAmount = 0.0f;
            if (this.mAdjustedForIme) {
                this.mAdjustedForIme = false;
                updateAdjustedBounds();
                this.mService.setResizeDimLayer(false, getWindowingMode(), 1.0f);
            }
        } else {
            this.mImeGoingAway |= this.mAdjustedForIme;
        }
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
        int dockedSide = getDockSide();
        boolean dockedTopOrBottom = dockedSide == 2 || dockedSide == 4;
        if (imeWin == null) {
        } else if (!dockedTopOrBottom) {
            int i = dockedSide;
        } else {
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
                this.mTmpAdjustedBounds.bottom = (int) ((this.mAdjustImeAmount * ((float) bottom)) + ((1.0f - this.mAdjustImeAmount) * ((float) getRawBounds().bottom)));
                this.mFullyAdjustedImeBounds.set(getRawBounds());
                int i2 = dockedSide;
                Rect rect = displayStableRect;
            } else {
                int topBeforeImeAdjust = (getRawBounds().top - dividerWidth) + dividerWidthInactive;
                int top = Math.max(getRawBounds().top - yOffset, getMinTopStackBottom(displayStableRect, getRawBounds().top - dividerWidth) + dividerWidthInactive);
                this.mTmpAdjustedBounds.set(getRawBounds());
                int i3 = dockedSide;
                Rect rect2 = displayStableRect;
                int i4 = topBeforeImeAdjust;
                this.mTmpAdjustedBounds.top = getRawBounds().top + ((int) ((this.mAdjustImeAmount * ((float) (top - topBeforeImeAdjust))) + (this.mAdjustDividerAmount * ((float) (dividerWidthInactive - dividerWidth)))));
                this.mFullyAdjustedImeBounds.set(getRawBounds());
                this.mFullyAdjustedImeBounds.top = top;
                this.mFullyAdjustedImeBounds.bottom = getRawBounds().height() + top;
            }
            return true;
        }
        return false;
    }

    private boolean adjustForMinimizedDockedStack(float minimizeAmount) {
        int dockSide = getDockSide();
        if (dockSide == -1 && !this.mTmpAdjustedBounds.isEmpty()) {
            return false;
        }
        if (dockSide == 2) {
            this.mService.getStableInsetsLocked(0, this.mTmpRect);
            int topInset = this.mTmpRect.top;
            this.mTmpAdjustedBounds.set(getRawBounds());
            this.mTmpAdjustedBounds.bottom = (int) ((((float) topInset) * minimizeAmount) + ((1.0f - minimizeAmount) * ((float) getRawBounds().bottom)));
        } else if (dockSide == 1) {
            this.mTmpAdjustedBounds.set(getRawBounds());
            int width = getRawBounds().width();
            this.mTmpAdjustedBounds.right = (int) ((((float) this.mDockedStackMinimizeThickness) * minimizeAmount) + ((1.0f - minimizeAmount) * ((float) getRawBounds().right)));
            this.mTmpAdjustedBounds.left = this.mTmpAdjustedBounds.right - width;
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
            this.mService.getStableInsetsLocked(0, this.mTmpRect);
            return getRawBounds().bottom - this.mTmpRect.top;
        } else if (dockSide == 1 || dockSide == 3) {
            return getRawBounds().width() - this.mDockedStackMinimizeThickness;
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
        if (this.mAdjustedForIme && adjust && !isImeTarget) {
            this.mService.setResizeDimLayer(true, getWindowingMode(), Math.max(this.mAdjustImeAmount, this.mAdjustDividerAmount) * IME_ADJUST_DIM_AMOUNT);
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

    public void writeToProto(ProtoOutputStream proto, long fieldId, boolean trim) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, trim);
        proto.write(1120986464258L, this.mStackId);
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            ((Task) this.mChildren.get(taskNdx)).writeToProto(proto, 2246267895811L, trim);
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

    /* access modifiers changed from: package-private */
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
        if (this.mAnimationBackgroundSurfaceIsShown != 0) {
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
    public boolean fillsParent() {
        if (useCurrentBounds()) {
            return matchParentBounds();
        }
        return true;
    }

    public String toString() {
        return "{stackId=" + this.mStackId + " tasks=" + this.mChildren + "}";
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return toShortString();
    }

    public String toShortString() {
        return "Stack=" + this.mStackId;
    }

    /* access modifiers changed from: package-private */
    public int getDockSide() {
        return getDockSide(getRawBounds());
    }

    /* access modifiers changed from: package-private */
    public int getDockSideForDisplay(DisplayContent dc) {
        return getDockSide(dc, getRawBounds());
    }

    private int getDockSide(Rect bounds) {
        if (this.mDisplayContent == null) {
            return -1;
        }
        return getDockSide(this.mDisplayContent, bounds);
    }

    private int getDockSide(DisplayContent dc, Rect bounds) {
        if (!inSplitScreenWindowingMode()) {
            return -1;
        }
        dc.getBounds(this.mTmpRect);
        return dc.getDockedDividerController().getDockSide(bounds, this.mTmpRect, dc.getConfiguration().orientation);
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
    public int taskIdFromPoint(int x, int y) {
        getBounds(this.mTmpRect);
        if (!this.mTmpRect.contains(x, y) || isAdjustedForMinimizedDockedStack()) {
            return -1;
        }
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            Task task = (Task) this.mChildren.get(taskNdx);
            if (task.getTopVisibleAppMainWindow() != null || (HwPCUtils.isPcCastModeInServer() && task.isVisible())) {
                task.getDimBounds(this.mTmpRect);
                if (this.mTmpRect.contains(x, y)) {
                    return task.mTaskId;
                }
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void findTaskForResizePoint(int x, int y, int delta, DisplayContent.TaskForResizePointSearchResult results) {
        if (!getWindowConfiguration().canResizeTask() && (!HwFreeFormUtils.isFreeFormEnable() || !this.mDisplayContent.isStackVisible(5))) {
            results.searchDone = true;
        } else if (!findTaskInFreeform(x, y, delta, results)) {
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
        }
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
                boolean isFreeformed = task.inFreeformWindowingMode() || task.inHwPCFreeformWindowingMode();
                if (task == focusedTask) {
                    if (!HwFreeFormUtils.isFreeFormEnable() || isFreeformed) {
                        postExclude.set(this.mTmpRect);
                    } else {
                        touchExcludeRegion.op(this.mTmpRect, Region.Op.DIFFERENCE);
                    }
                }
                if (task != focusedTask || isFreeformed) {
                    if (isFreeformed) {
                        this.mTmpRect.inset(-delta, -delta);
                        this.mTmpRect.intersect(contentRect);
                    }
                    touchExcludeRegion.op(this.mTmpRect, Region.Op.DIFFERENCE);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0013, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r2.mService.mActivityManager.resizePinnedStack(r3, r4);
     */
    public boolean setPinnedStackSize(Rect stackBounds, Rect tempTaskBounds) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mCancelCurrentBoundsAnimation) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onAllWindowsDrawn() {
        if (this.mBoundsAnimating || this.mBoundsAnimatingRequested) {
            this.mService.mBoundsAnimationController.onAllWindowsDrawn();
        }
    }

    public void onAnimationStart(boolean schedulePipModeChangedCallback, boolean forceUpdate) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mBoundsAnimatingRequested = false;
                this.mBoundsAnimating = true;
                this.mCancelCurrentBoundsAnimation = false;
                if (schedulePipModeChangedCallback) {
                    forAllWindows((Consumer<WindowState>) $$Lambda$TaskStack$n0sDe5GcitIQBOrca4W45Hcc98.INSTANCE, false);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        if (inPinnedWindowingMode()) {
            try {
                this.mService.mActivityManager.notifyPinnedStackAnimationStarted();
            } catch (RemoteException e) {
            }
            PinnedStackWindowController controller = (PinnedStackWindowController) getController();
            if (schedulePipModeChangedCallback && controller != null) {
                controller.updatePictureInPictureModeForPinnedStackAnimation(null, forceUpdate);
            }
        }
    }

    public void onAnimationEnd(boolean schedulePipModeChangedCallback, Rect finalStackSize, boolean moveToFullscreen) {
        if (inPinnedWindowingMode()) {
            PinnedStackWindowController controller = (PinnedStackWindowController) getController();
            if (schedulePipModeChangedCallback && controller != null) {
                controller.updatePictureInPictureModeForPinnedStackAnimation(this.mBoundsAnimationTarget, false);
            }
            if (finalStackSize != null) {
                setPinnedStackSize(finalStackSize, null);
            } else {
                onPipAnimationEndResize();
            }
            try {
                this.mService.mActivityManager.notifyPinnedStackAnimationEnded();
                if (moveToFullscreen) {
                    this.mService.mActivityManager.moveTasksToFullscreenStack(this.mStackId, true);
                }
            } catch (RemoteException e) {
            }
        } else {
            onPipAnimationEndResize();
        }
    }

    public void onPipAnimationEndResize() {
        this.mBoundsAnimating = false;
        for (int i = 0; i < this.mChildren.size(); i++) {
            ((Task) this.mChildren.get(i)).clearPreserveNonFloatingState();
        }
        this.mService.requestTraversal();
    }

    public boolean shouldDeferStartOnMoveToFullscreen() {
        TaskStack homeStack = this.mDisplayContent.getHomeStack();
        if (homeStack == null) {
            return true;
        }
        Task homeTask = (Task) homeStack.getTopChild();
        if (homeTask == null) {
            return true;
        }
        AppWindowToken homeApp = homeTask.getTopVisibleAppToken();
        if (!homeTask.isVisible() || homeApp == null) {
            return true;
        }
        return true ^ homeApp.allDrawn;
    }

    public boolean deferScheduleMultiWindowModeChanged() {
        boolean z = false;
        if (!inPinnedWindowingMode()) {
            return false;
        }
        if (this.mBoundsAnimatingRequested || this.mBoundsAnimating) {
            z = true;
        }
        return z;
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

    public DisplayInfo getDisplayInfo() {
        if (this.mDisplayContent != null) {
            return this.mDisplayContent.getDisplayInfo();
        }
        return new DisplayInfo();
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
    public void getRelativePosition(Point outPos) {
        super.getRelativePosition(outPos);
        int outset = getStackOutset();
        outPos.x -= outset;
        outPos.y -= outset;
    }

    /* access modifiers changed from: package-private */
    public AnimatingAppWindowTokenRegistry getAnimatingAppWindowTokenRegistry() {
        return this.mAnimatingAppWindowTokenRegistry;
    }

    /* access modifiers changed from: package-private */
    public void clearTempInsetBounds() {
        for (int taskNdx = this.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
            ((Task) this.mChildren.get(taskNdx)).setTempInsetBounds(null);
        }
    }

    private void getStackCoordinationModeBounds(Rect outBounds) {
        boolean inCoordinationPrimary = inCoordinationPrimaryWindowingMode();
        boolean isTopsecondaryCoordinationStack = false;
        TaskStack secondaryCoordinationStack = this.mDisplayContent.getTopStackInWindowingMode(12);
        if (secondaryCoordinationStack != null && secondaryCoordinationStack.mStackId == this.mStackId) {
            isTopsecondaryCoordinationStack = true;
        }
        CoordinationModeUtils.getInstance(this.mService.mContext).getStackCoordinationModeBounds(inCoordinationPrimary || !isTopsecondaryCoordinationStack, getDisplayContent().mDisplayInfo.rotation, outBounds);
    }

    private boolean findTaskInFreeform(int x, int y, int delta, DisplayContent.TaskForResizePointSearchResult results) {
        return this.mHwTSEx.findTaskInFreeform(x, y, delta, results);
    }
}
