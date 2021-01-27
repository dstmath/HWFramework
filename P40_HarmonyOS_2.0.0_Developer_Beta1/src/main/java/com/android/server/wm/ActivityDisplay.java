package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.WindowConfiguration;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.graphics.Point;
import android.os.IBinder;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.IntArray;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.am.EventLogTags;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.android.server.wm.RootActivityContainer;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class ActivityDisplay extends ConfigurationContainer<ActivityStack> implements WindowContainerListener {
    static final int POSITION_BOTTOM = Integer.MIN_VALUE;
    static final int POSITION_TOP = Integer.MAX_VALUE;
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_KEYGUARD = "ActivityTaskManager_keyguard";
    private static final String TAG_STACK = "ActivityTaskManager";
    private static int sNextFreeStackId = 0;
    final ArrayList<ActivityTaskManagerInternal.SleepToken> mAllSleepTokens = new ArrayList<>();
    private ActivityStack mCoordinationPrimaryStack = null;
    Display mDisplay;
    private IntArray mDisplayAccessUIDs = new IntArray();
    DisplayContent mDisplayContent;
    int mDisplayId;
    private ActivityStack mHomeStack = null;
    private IHwActivityDisplayEx mHwActivityDisplayEx;
    private ActivityRecord mLastCompatModeActivity;
    private ActivityStack mLastFocusedStack;
    ActivityTaskManagerInternal.SleepToken mOffToken;
    private ActivityStack mPinnedStack = null;
    private ActivityStack mPreferredTopFocusableStack;
    private ActivityStack mRealLastFocusedStack;
    private ActivityStack mRecentsStack = null;
    private boolean mRemoved;
    private RootActivityContainer mRootActivityContainer;
    ActivityTaskManagerService mService;
    private boolean mSingleTaskInstance;
    private boolean mSleeping;
    private ActivityStack mSplitScreenPrimaryStack = null;
    private ArrayList<OnStackOrderChangedListener> mStackOrderChangedCallbacks = new ArrayList<>();
    final ArrayList<ActivityStack> mStacks = new ArrayList<>();
    private Point mTmpDisplaySize = new Point();
    private final RootActivityContainer.FindTaskResult mTmpFindTaskResult = new RootActivityContainer.FindTaskResult();

    /* access modifiers changed from: package-private */
    public interface OnStackOrderChangedListener {
        void onStackOrderChanged(ActivityStack activityStack);
    }

    ActivityDisplay(RootActivityContainer root, Display display) {
        this.mRootActivityContainer = root;
        this.mService = root.mService;
        this.mDisplayId = display.getDisplayId();
        this.mDisplay = display;
        this.mDisplayContent = createDisplayContent();
        updateBounds();
        this.mHwActivityDisplayEx = HwServiceExFactory.getHwActivityDisplayEx();
    }

    /* access modifiers changed from: protected */
    public DisplayContent createDisplayContent() {
        return this.mService.mWindowManager.mRoot.createDisplayContent(this.mDisplay, this);
    }

    private void updateBounds() {
        this.mDisplay.getRealSize(this.mTmpDisplaySize);
        setBounds(0, 0, this.mTmpDisplaySize.x, this.mTmpDisplaySize.y);
    }

    /* access modifiers changed from: package-private */
    public void onDisplayChanged() {
        ActivityTaskManagerInternal.SleepToken sleepToken;
        int displayId = this.mDisplay.getDisplayId();
        if (displayId != 0) {
            int displayState = this.mDisplay.getState();
            if (displayState == 1 && this.mOffToken == null) {
                this.mOffToken = this.mService.acquireSleepToken("Display-off", displayId);
            } else if (displayState == 2 && (sleepToken = this.mOffToken) != null) {
                sleepToken.release();
                this.mOffToken = null;
            }
        }
        updateBounds();
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent != null) {
            displayContent.updateDisplayInfo();
            this.mService.mWindowManager.requestTraversal();
        }
    }

    @Override // com.android.server.wm.WindowContainerListener
    public void onInitializeOverrideConfiguration(Configuration config) {
        getRequestedOverrideConfiguration().updateFrom(config);
    }

    /* access modifiers changed from: package-private */
    public void addChild(ActivityStack stack, int position) {
        if (position == POSITION_BOTTOM) {
            position = 0;
        } else if (position == POSITION_TOP) {
            position = this.mStacks.size();
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            Slog.v("ActivityTaskManager", "addChild: attaching " + stack + " to displayId=" + this.mDisplayId + " position=" + position);
        } else {
            Slog.v("ActivityTaskManager", "addChild: attaching stackId=" + stack.mStackId + " to displayId=" + this.mDisplayId + " position=" + position);
        }
        addStackReferenceIfNeeded(stack);
        positionChildAt(stack, position);
        this.mService.updateSleepIfNeededLocked();
    }

    /* access modifiers changed from: package-private */
    public void removeChild(ActivityStack stack) {
        if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            Slog.v("ActivityTaskManager", "removeChild: detaching " + stack + " from displayId=" + this.mDisplayId);
        } else {
            Slog.v("ActivityTaskManager", "removeChild: detaching stackId=" + stack.mStackId + " from displayId=" + this.mDisplayId);
        }
        this.mStacks.remove(stack);
        if (this.mPreferredTopFocusableStack == stack) {
            this.mPreferredTopFocusableStack = null;
        }
        removeStackReferenceIfNeeded(stack);
        releaseSelfIfNeeded();
        this.mService.updateSleepIfNeededLocked();
        onStackOrderChanged(stack);
        if (this.mStacks.isEmpty()) {
            this.mService.mHwATMSEx.notifyDisplayStacksEmpty(this.mDisplayId);
        }
    }

    /* access modifiers changed from: package-private */
    public void positionChildAtTop(ActivityStack stack, boolean includingParents) {
        positionChildAtTop(stack, includingParents, null);
    }

    /* access modifiers changed from: package-private */
    public void positionChildAtTop(ActivityStack stack, boolean includingParents, String updateLastFocusedStackReason) {
        positionChildAt(stack, this.mStacks.size(), includingParents, updateLastFocusedStackReason);
    }

    /* access modifiers changed from: package-private */
    public void positionChildAtBottom(ActivityStack stack) {
        positionChildAtBottom(stack, null);
    }

    /* access modifiers changed from: package-private */
    public void positionChildAtBottom(ActivityStack stack, String updateLastFocusedStackReason) {
        positionChildAt(stack, 0, false, updateLastFocusedStackReason);
    }

    private void positionChildAt(ActivityStack stack, int position) {
        positionChildAt(stack, position, false, null);
    }

    private void positionChildAt(ActivityStack stack, int position, boolean includingParents, String updateLastFocusedStackReason) {
        DisplayContent displayContent;
        ActivityStack currentFocusedStack;
        ActivityStack prevFocusedStack = updateLastFocusedStackReason != null ? getFocusedStack() : null;
        boolean wasContained = this.mStacks.remove(stack);
        if (!this.mSingleTaskInstance || getChildCount() <= 0) {
            int insertPosition = getTopInsertPosition(stack, position);
            this.mStacks.add(insertPosition, stack);
            if (insertPosition == this.mStacks.size() - 1) {
                Slog.v("ActivityTaskManager", "positionChild stackId=" + stack.mStackId + " to top.");
                insertPosition = POSITION_TOP;
            }
            if (wasContained && position >= this.mStacks.size() - 1 && stack.isFocusableAndVisible()) {
                this.mPreferredTopFocusableStack = stack;
            } else if (this.mPreferredTopFocusableStack == stack) {
                this.mPreferredTopFocusableStack = null;
                if ("moveTaskToBackLocked".equals(updateLastFocusedStackReason) && this.mStacks.size() > 0) {
                    this.mPreferredTopFocusableStack = getFocusedStack();
                }
            }
            if (!(updateLastFocusedStackReason == null || (currentFocusedStack = getFocusedStack()) == prevFocusedStack)) {
                this.mLastFocusedStack = prevFocusedStack;
                this.mRealLastFocusedStack = prevFocusedStack;
                if (HwFreeFormUtils.isFreeFormEnable() && currentFocusedStack != null) {
                    ActivityStack freeFormStack = getStack(5, 1);
                    if (currentFocusedStack.isActivityTypeHome() && freeFormStack != null) {
                        freeFormStack.setFreeFormStackVisible(false);
                    }
                    if (currentFocusedStack.inFreeformWindowingMode()) {
                        currentFocusedStack.setFreeFormStackVisible(true);
                    }
                }
                this.mService.mHwATMSEx.focusStackChange(this.mRootActivityContainer.mCurrentUser, this.mDisplayId, currentFocusedStack, this.mLastFocusedStack);
                int i = this.mRootActivityContainer.mCurrentUser;
                int i2 = this.mDisplayId;
                int i3 = -1;
                int stackId = currentFocusedStack == null ? -1 : currentFocusedStack.getStackId();
                ActivityStack activityStack = this.mLastFocusedStack;
                if (activityStack != null) {
                    i3 = activityStack.getStackId();
                }
                EventLogTags.writeAmFocusedStack(i, i2, stackId, i3, updateLastFocusedStackReason);
            }
            if (!(stack.getTaskStack() == null || (displayContent = this.mDisplayContent) == null)) {
                displayContent.positionStackAt(insertPosition, stack.getTaskStack(), includingParents);
            }
            if (!wasContained) {
                stack.setParent(this);
            }
            onStackOrderChanged(stack);
            return;
        }
        throw new IllegalStateException("positionChildAt: Can only have one child on display=" + this);
    }

    public void switchFocusedStack(ActivityStack stack, String updateLastFocusedStackReason) {
        if (stack != null && updateLastFocusedStackReason != null) {
            ActivityStack prevFocusedStack = getFocusedStack();
            if (stack.isFocusableAndVisible()) {
                this.mPreferredTopFocusableStack = stack;
            }
            ActivityStack currentFocusedStack = getFocusedStack();
            if (currentFocusedStack != prevFocusedStack) {
                this.mLastFocusedStack = prevFocusedStack;
                this.mRealLastFocusedStack = prevFocusedStack;
                this.mService.mHwATMSEx.focusStackChange(this.mRootActivityContainer.mCurrentUser, this.mDisplayId, currentFocusedStack, this.mLastFocusedStack);
                int i = this.mRootActivityContainer.mCurrentUser;
                int i2 = this.mDisplayId;
                int i3 = -1;
                int stackId = currentFocusedStack == null ? -1 : currentFocusedStack.getStackId();
                ActivityStack activityStack = this.mLastFocusedStack;
                if (activityStack != null) {
                    i3 = activityStack.getStackId();
                }
                EventLogTags.writeAmFocusedStack(i, i2, stackId, i3, updateLastFocusedStackReason);
            }
            ActivityRecord activityRecord = stack.topRunningActivityLocked();
            if (activityRecord != null) {
                this.mService.setResumedActivityUncheckLocked(activityRecord, updateLastFocusedStackReason);
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
        }
    }

    private int getTopInsertPosition(ActivityStack stack, int candidatePosition) {
        int position = this.mStacks.size();
        if (stack.inPinnedWindowingMode()) {
            return Math.min(position, candidatePosition);
        }
        while (position > 0) {
            ActivityStack targetStack = this.mStacks.get(position - 1);
            if ((!targetStack.isAlwaysOnTop() && !isFreeformStackOnTop(targetStack)) || (stack.isAlwaysOnTop() && !targetStack.inPinnedWindowingMode())) {
                break;
            }
            position--;
        }
        return Math.min(position, candidatePosition);
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getStack(int stackId) {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            T t = (T) this.mStacks.get(i);
            if (t.mStackId == stackId) {
                return t;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getStack(int windowingMode, int activityType) {
        if (activityType == 2) {
            return (T) this.mHomeStack;
        }
        if (activityType == 3) {
            return (T) this.mRecentsStack;
        }
        if (windowingMode == 2) {
            return (T) this.mPinnedStack;
        }
        if (windowingMode == 3) {
            return (T) this.mSplitScreenPrimaryStack;
        }
        if (windowingMode == 11) {
            return (T) this.mCoordinationPrimaryStack;
        }
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            T t = (T) this.mStacks.get(i);
            if (t.isCompatible(windowingMode, activityType)) {
                return t;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean isFreeformStackOnTop(ActivityStack targetStack) {
        boolean isFreeformOnTop = false;
        if (targetStack == null) {
            return false;
        }
        boolean isFreeformOnTop2 = targetStack.inFreeformWindowingMode() && targetStack.mIsFreeFormStackVisible;
        if (!isFreeformOnTop2) {
            return isFreeformOnTop2;
        }
        ActivityRecord topActivity = targetStack.topRunningActivityLocked();
        if (topActivity != null && this.mService.isInFreeformWhiteList(topActivity.packageName)) {
            isFreeformOnTop = true;
        }
        return isFreeformOnTop;
    }

    private boolean alwaysCreateStack(int windowingMode, int activityType) {
        return activityType == 1 && (windowingMode == 1 || windowingMode == 5 || windowingMode == 4 || windowingMode == 10 || windowingMode == 12 || windowingMode == 103 || WindowConfiguration.isHwMultiStackWindowingMode(windowingMode));
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getOrCreateStack(int windowingMode, int activityType, boolean onTop) {
        T stack;
        return (alwaysCreateStack(windowingMode, activityType) || (stack = (T) getStack(windowingMode, activityType)) == null) ? (T) createStack(windowingMode, activityType, onTop) : stack;
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getOrCreateStack(ActivityRecord r, ActivityOptions options, TaskRecord candidateTask, int activityType, boolean onTop) {
        T stack = (T) getOrCreateStack(validateWindowingMode(options != null ? options.getLaunchWindowingMode() : 0, r, candidateTask, activityType), activityType, onTop);
        this.mService.mHwATMSEx.stackCreated(stack, r);
        return stack;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getNextStackId() {
        int i = sNextFreeStackId;
        sNextFreeStackId = i + 1;
        return i;
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T createStack(int windowingMode, int activityType, boolean onTop) {
        ActivityStack stack;
        if (this.mSingleTaskInstance && getChildCount() > 0) {
            return (T) this.mRootActivityContainer.getDefaultDisplay().createStack(windowingMode, activityType, onTop);
        }
        if (activityType == 0) {
            activityType = 1;
        }
        if (activityType != 1 && (stack = getStack(0, activityType)) != null) {
            throw new IllegalArgumentException("Stack=" + stack + " of activityType=" + activityType + " already on display=" + this + ". Can't have multiple.");
        } else if (isWindowingModeSupported(windowingMode, this.mService.mSupportsMultiWindow, this.mService.mSupportsSplitScreenMultiWindow, this.mService.mSupportsFreeformWindowManagement, this.mService.mSupportsPictureInPicture, activityType)) {
            return (T) createStackUnchecked(windowingMode, activityType, getNextStackId(), onTop);
        } else {
            throw new IllegalArgumentException("Can't create stack for unsupported windowingMode=" + windowingMode);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public <T extends ActivityStack> T createStackUnchecked(int windowingMode, int activityType, int stackId, boolean onTop) {
        if (windowingMode != 2 || activityType == 1) {
            return (T) HwServiceFactory.createActivityStack(this, stackId, this.mRootActivityContainer.mStackSupervisor, windowingMode, activityType, onTop);
        }
        throw new IllegalArgumentException("Stack with windowing mode cannot with non standard activity type.");
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getFocusedStack() {
        ActivityStack activityStack = this.mPreferredTopFocusableStack;
        if (activityStack != null) {
            return activityStack;
        }
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ActivityStack stack = this.mStacks.get(i);
            if (stack.isFocusableAndVisible()) {
                return stack;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getNextFocusableStack() {
        return getNextFocusableStack(null, false);
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getNextFocusableStack(ActivityStack currentFocus, boolean ignoreCurrent) {
        int currentWindowingMode = currentFocus != null ? currentFocus.getWindowingMode() : 0;
        ActivityStack candidate = null;
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ActivityStack stack = this.mStacks.get(i);
            if ((!ignoreCurrent || stack != currentFocus) && stack.isFocusableAndVisible()) {
                if (currentWindowingMode == 4 && candidate == null && stack.inSplitScreenPrimaryWindowingMode()) {
                    candidate = stack;
                } else if (candidate == null || !stack.inSplitScreenSecondaryWindowingMode()) {
                    return stack;
                } else {
                    return candidate;
                }
            }
        }
        return candidate;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getResumedActivity() {
        ActivityStack focusedStack = getFocusedStack();
        if (focusedStack == null) {
            return null;
        }
        ActivityRecord resumedActivity = focusedStack.getResumedActivity();
        if (resumedActivity != null && resumedActivity.app != null) {
            return resumedActivity;
        }
        ActivityRecord resumedActivity2 = focusedStack.mPausingActivity;
        if (resumedActivity2 == null || resumedActivity2.app == null) {
            return focusedStack.topRunningActivityLocked(true);
        }
        return resumedActivity2;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getLastFocusedStack() {
        return this.mLastFocusedStack;
    }

    /* access modifiers changed from: package-private */
    public boolean allResumedActivitiesComplete() {
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ActivityRecord r = this.mStacks.get(stackNdx).getResumedActivity();
            if (!(r == null || r.isState(ActivityStack.ActivityState.RESUMED))) {
                return false;
            }
        }
        ActivityStack currentFocusedStack = getFocusedStack();
        if (ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            Slog.d("ActivityTaskManager", "allResumedActivitiesComplete: mLastFocusedStack changing from=" + this.mLastFocusedStack + " to=" + currentFocusedStack);
        }
        this.mLastFocusedStack = currentFocusedStack;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean pauseBackStacks(boolean userLeaving, ActivityRecord resuming, boolean dontWait, ActivityRecord prev) {
        boolean someActivityPaused = false;
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStack stack = this.mStacks.get(stackNdx);
            ActivityRecord resumedActivity = stack.getResumedActivity();
            if (resumedActivity != null && (stack.getVisibility(resuming) != 0 || !stack.isFocusable())) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.d("ActivityTaskManager", "pauseBackStacks: stack=" + stack + " mResumedActivity=" + resumedActivity);
                }
                if (!this.mHwActivityDisplayEx.keepStackResumed(stack) && !shouldKeepResumedIfFreeFormExist(stack)) {
                    boolean z = false;
                    if (stack.startPausingLocked(userLeaving, false, resuming, dontWait) && !pauseWithoutAffectPrev(stack, prev)) {
                        z = true;
                    }
                    someActivityPaused |= z;
                }
            }
        }
        return someActivityPaused;
    }

    private boolean pauseWithoutAffectPrev(ActivityStack pausingStack, ActivityRecord prev) {
        return pausingStack != null && pausingStack.inHwFreeFormWindowingMode() && prev != null && DisplayPolicy.LAUNCHER_PACKAGE_NAME.equals(prev.launchedFromPackage);
    }

    private boolean shouldKeepResumedIfFreeFormExist(ActivityStack stack) {
        boolean isStackInFreeForm = false;
        if (HwFreeFormUtils.isFreeFormEnable() && stack != null) {
            ActivityStack freeFormStack = this.mService.mStackSupervisor.mRootActivityContainer.getStack(5, 1);
            if (freeFormStack != null && freeFormStack.getFreeFormStackVisible() && !stack.isHomeOrRecentsStack()) {
                if (!stack.inFreeformWindowingMode() && stack.mResumedActivity != null && freeFormStack.topRunningActivityLocked() != null && !freeFormStack.topRunningActivityLocked().packageName.equals(stack.mResumedActivity.packageName)) {
                    isStackInFreeForm = true;
                }
                if (isStackInFreeForm) {
                    freeFormStack.setCurrentPkgUnderFreeForm(stack.mResumedActivity.packageName);
                    HwFreeFormUtils.log("ams", "Stack:" + stack + " is keep resumed");
                }
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void findTaskLocked(ActivityRecord r, boolean isPreferredDisplay, RootActivityContainer.FindTaskResult result) {
        this.mTmpFindTaskResult.clear();
        for (int stackNdx = getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStack stack = getChildAt(stackNdx);
            if (r.hasCompatibleActivityType(stack)) {
                stack.findTaskLocked(r, this.mTmpFindTaskResult);
                if (this.mTmpFindTaskResult.mRecord == null) {
                    continue;
                } else if (this.mTmpFindTaskResult.mIdealMatch) {
                    result.setTo(this.mTmpFindTaskResult);
                    return;
                } else if (isPreferredDisplay) {
                    result.setTo(this.mTmpFindTaskResult);
                }
            } else if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d("ActivityTaskManager", "Skipping stack: (mismatch activity/stack) " + stack);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeStacksInWindowingModes(int... windowingModes) {
        if (!(windowingModes == null || windowingModes.length == 0)) {
            ArrayList<ActivityStack> stacks = new ArrayList<>();
            for (int j = windowingModes.length - 1; j >= 0; j--) {
                int windowingMode = windowingModes[j];
                for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                    ActivityStack stack = this.mStacks.get(i);
                    if (stack.isActivityTypeStandardOrUndefined() && stack.getWindowingMode() == windowingMode) {
                        stacks.add(stack);
                        if (HwFreeFormUtils.isFreeFormEnable() && stack.getWindowingMode() == 5) {
                            stack.setFreeFormStackVisible(false);
                            stack.setCurrentPkgUnderFreeForm("");
                        }
                    }
                }
            }
            for (int i2 = stacks.size() - 1; i2 >= 0; i2--) {
                this.mRootActivityContainer.mStackSupervisor.removeStack(stacks.get(i2));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeStacksWithActivityTypes(int... activityTypes) {
        if (!(activityTypes == null || activityTypes.length == 0)) {
            ArrayList<ActivityStack> stacks = new ArrayList<>();
            for (int j = activityTypes.length - 1; j >= 0; j--) {
                int activityType = activityTypes[j];
                for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                    ActivityStack stack = this.mStacks.get(i);
                    if (stack.getActivityType() == activityType) {
                        stacks.add(stack);
                    }
                }
            }
            for (int i2 = stacks.size() - 1; i2 >= 0; i2--) {
                this.mRootActivityContainer.mStackSupervisor.removeStack(stacks.get(i2));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onStackWindowingModeChanged(ActivityStack stack) {
        removeStackReferenceIfNeeded(stack);
        addStackReferenceIfNeeded(stack);
    }

    private void addStackReferenceIfNeeded(ActivityStack stack) {
        int activityType = stack.getActivityType();
        int windowingMode = stack.getWindowingMode();
        if (activityType == 2) {
            ActivityStack activityStack = this.mHomeStack;
            if (activityStack == null || activityStack == stack) {
                this.mHomeStack = stack;
            } else {
                throw new IllegalArgumentException("addStackReferenceIfNeeded: home stack=" + this.mHomeStack + " already exist on display=" + this + " stack=" + stack);
            }
        } else if (activityType == 3) {
            ActivityStack activityStack2 = this.mRecentsStack;
            if (activityStack2 == null || activityStack2 == stack) {
                this.mRecentsStack = stack;
            } else {
                throw new IllegalArgumentException("addStackReferenceIfNeeded: recents stack=" + this.mRecentsStack + " already exist on display=" + this + " stack=" + stack);
            }
        }
        if (windowingMode == 2) {
            ActivityStack activityStack3 = this.mPinnedStack;
            if (activityStack3 == null || activityStack3 == stack) {
                this.mPinnedStack = stack;
            } else {
                throw new IllegalArgumentException("addStackReferenceIfNeeded: pinned stack=" + this.mPinnedStack + " already exist on display=" + this + " stack=" + stack);
            }
        } else if (windowingMode == 3) {
            ActivityStack activityStack4 = this.mSplitScreenPrimaryStack;
            if (activityStack4 == null || activityStack4 == stack) {
                this.mSplitScreenPrimaryStack = stack;
                onSplitScreenModeActivated();
            } else {
                throw new IllegalArgumentException("addStackReferenceIfNeeded: split-screen-primary stack=" + this.mSplitScreenPrimaryStack + " already exist on display=" + this + " stack=" + stack);
            }
        } else if (windowingMode == 11) {
            ActivityStack activityStack5 = this.mCoordinationPrimaryStack;
            if (activityStack5 == null || activityStack5 == stack) {
                this.mCoordinationPrimaryStack = stack;
                onCoordinationModeActivated();
            } else {
                throw new IllegalArgumentException("addStackReferenceIfNeeded: coordination_primary stack=" + this.mCoordinationPrimaryStack + " already exist on display=" + this + " stack=" + stack);
            }
        }
        this.mService.mHwATMSEx.addStackReferenceIfNeeded(stack);
    }

    private void removeStackReferenceIfNeeded(ActivityStack stack) {
        if (stack == this.mHomeStack) {
            this.mHomeStack = null;
        } else if (stack == this.mRecentsStack) {
            this.mRecentsStack = null;
        } else if (stack == this.mPinnedStack) {
            this.mPinnedStack = null;
        } else if (stack == this.mSplitScreenPrimaryStack) {
            this.mSplitScreenPrimaryStack = null;
            onSplitScreenModeDismissed();
        } else if (stack == this.mCoordinationPrimaryStack) {
            this.mCoordinationPrimaryStack = null;
            onCoordinationModeDismissed();
        }
        this.mService.mHwATMSEx.removeStackReferenceIfNeeded(stack);
    }

    private void onSplitScreenModeDismissed() {
        ActivityRecord focusStackTopActivity;
        this.mRootActivityContainer.mWindowManager.deferSurfaceLayout();
        boolean isShouldLaunchSplitActivity = false;
        ActivityStack splitScreenStack = null;
        try {
            for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                ActivityStack otherStack = this.mStacks.get(i);
                ActivityRecord otherStackTopActivity = otherStack.getTopActivity();
                if (otherStackTopActivity != null && otherStackTopActivity.toString().contains("splitscreen.SplitScreenAppActivity")) {
                    splitScreenStack = otherStack;
                } else if (otherStack.inSplitScreenSecondaryWindowingMode() && !this.mService.mHwATMSEx.isSwitchToMagicWin(otherStack.getStackId(), false, getConfiguration().orientation)) {
                    otherStack.setWindowingMode(0, false, false, false, true, false);
                }
            }
            ActivityStack focusStack = getFocusedStack();
            boolean isChangeFocusFromSplitScreen = false;
            if (!(focusStack == null || (focusStackTopActivity = focusStack.getTopActivity()) == null || !focusStackTopActivity.toString().contains("splitscreen.SplitScreenAppActivity"))) {
                isChangeFocusFromSplitScreen = true;
            }
            if (splitScreenStack != null && isChangeFocusFromSplitScreen) {
                Slog.w("ActivityTaskManager", "dismiss split mode and enter home");
                isShouldLaunchSplitActivity = true;
            }
            try {
                ActivityStack topFullscreenStack = getTopStackInWindowingMode(1);
                if (HwMwUtils.ENABLED) {
                    ActivityStack topStack = getTopStack();
                    if (this.mHwActivityDisplayEx.launchMagicOnSplitScreenDismissed(topStack)) {
                        topFullscreenStack = topStack;
                    }
                }
                if (!(topFullscreenStack == null || this.mHomeStack == null || isTopStack(this.mHomeStack))) {
                    if (isShouldLaunchSplitActivity) {
                        positionChildAtTop(this.mHomeStack, false);
                    } else {
                        this.mHomeStack.moveToFront("onSplitScreenModeDismissed");
                        topFullscreenStack.moveToFront("onSplitScreenModeDismissed");
                        ActivityRecord top = topFullscreenStack.topRunningActivityLocked();
                        if (top != null && top == this.mRootActivityContainer.getTopResumedActivity()) {
                            this.mService.setResumedActivityUncheckLocked(top, "onSplitScreenModeDismissed");
                        }
                    }
                    this.mRootActivityContainer.mStackSupervisor.mNoAnimActivities.remove(this.mHomeStack.getTopActivity());
                }
            } finally {
                this.mRootActivityContainer.mWindowManager.continueSurfaceLayout();
            }
        } catch (Throwable th) {
            ActivityStack topFullscreenStack2 = getTopStackInWindowingMode(1);
            if (HwMwUtils.ENABLED) {
                ActivityStack topStack2 = getTopStack();
                if (this.mHwActivityDisplayEx.launchMagicOnSplitScreenDismissed(topStack2)) {
                    topFullscreenStack2 = topStack2;
                }
            }
            if (!(topFullscreenStack2 == null || this.mHomeStack == null || isTopStack(this.mHomeStack))) {
                if (0 == 0) {
                    this.mHomeStack.moveToFront("onSplitScreenModeDismissed");
                    topFullscreenStack2.moveToFront("onSplitScreenModeDismissed");
                    ActivityRecord top2 = topFullscreenStack2.topRunningActivityLocked();
                    if (top2 != null && top2 == this.mRootActivityContainer.getTopResumedActivity()) {
                        this.mService.setResumedActivityUncheckLocked(top2, "onSplitScreenModeDismissed");
                    }
                } else {
                    positionChildAtTop(this.mHomeStack, false);
                }
                this.mRootActivityContainer.mStackSupervisor.mNoAnimActivities.remove(this.mHomeStack.getTopActivity());
            }
            throw th;
        } finally {
            this.mRootActivityContainer.mWindowManager.continueSurfaceLayout();
        }
    }

    private void onSplitScreenModeActivated() {
        this.mRootActivityContainer.mWindowManager.deferSurfaceLayout();
        try {
            for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                ActivityStack otherStack = this.mStacks.get(i);
                if (otherStack != this.mSplitScreenPrimaryStack) {
                    if (otherStack.affectedBySplitScreenResize()) {
                        otherStack.setWindowingMode(4, false, false, true, true, false);
                    }
                }
            }
        } finally {
            this.mRootActivityContainer.mWindowManager.continueSurfaceLayout();
        }
    }

    private boolean isWindowingModeSupported(int windowingMode, boolean supportsMultiWindow, boolean supportsSplitScreen, boolean supportsFreeform, boolean supportsPip, int activityType) {
        if (windowingMode == 11 || windowingMode == 12 || windowingMode == 0 || windowingMode == 1) {
            return true;
        }
        if (!supportsMultiWindow) {
            return false;
        }
        int displayWindowingMode = getWindowingMode();
        if (windowingMode == 3 || windowingMode == 4 || WindowConfiguration.isHwSplitScreenWindowingMode(windowingMode)) {
            if (!supportsSplitScreen || !WindowConfiguration.supportSplitScreenWindowingMode(activityType) || displayWindowingMode == 5) {
                return false;
            }
            return true;
        } else if (!supportsFreeform && windowingMode == 5) {
            return false;
        } else {
            if (windowingMode == 10 && !HwPCUtils.isPcCastModeInServer()) {
                return false;
            }
            if (supportsPip || windowingMode != 2) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public int resolveWindowingMode(ActivityRecord r, ActivityOptions options, TaskRecord task, int activityType) {
        int windowingMode = options != null ? options.getLaunchWindowingMode() : 0;
        if (windowingMode == 0) {
            if (task != null) {
                windowingMode = task.getWindowingMode();
            }
            if (windowingMode == 0 && r != null) {
                windowingMode = r.getWindowingMode();
            }
            if (windowingMode == 0) {
                windowingMode = getWindowingMode();
            }
        }
        int windowingMode2 = validateWindowingMode(windowingMode, r, task, activityType);
        if (windowingMode2 != 0) {
            return windowingMode2;
        }
        return 1;
    }

    /* access modifiers changed from: package-private */
    public int validateWindowingMode(int windowingMode, ActivityRecord r, TaskRecord task, int activityType) {
        boolean supportsPip;
        boolean supportsFreeform;
        boolean supportsSplitScreen;
        boolean inSplitScreenMode;
        int windowingMode2;
        boolean supportsMultiWindow = this.mService.mSupportsMultiWindow;
        boolean supportsSplitScreen2 = this.mService.mSupportsSplitScreenMultiWindow;
        boolean supportsFreeform2 = this.mService.mSupportsFreeformWindowManagement;
        boolean supportsPip2 = this.mService.mSupportsPictureInPicture;
        if (supportsMultiWindow) {
            if (task != null) {
                supportsMultiWindow = task.isResizeable();
                supportsSplitScreen = task.supportsSplitScreenWindowingMode();
                supportsFreeform = supportsFreeform2;
                supportsPip = supportsPip2;
            } else if (r != null) {
                supportsMultiWindow = r.isResizeable();
                supportsSplitScreen = r.supportsSplitScreenWindowingMode();
                supportsFreeform = r.supportsFreeform();
                supportsPip = r.supportsPictureInPicture();
            }
            boolean supportsMultiWindow2 = !supportsMultiWindow || windowingMode == 103;
            inSplitScreenMode = hasSplitScreenPrimaryStack();
            if (!inSplitScreenMode || windowingMode != 4) {
                if (inSplitScreenMode || (!(windowingMode == 1 || windowingMode == 0) || !supportsSplitScreen)) {
                    windowingMode2 = windowingMode;
                } else {
                    windowingMode2 = 4;
                }
            } else if (!HwMwUtils.ENABLED || r == null || !r.inHwMagicWindowingMode()) {
                windowingMode2 = 0;
            } else {
                windowingMode2 = 103;
            }
            if (windowingMode2 != 0 || !isWindowingModeSupported(windowingMode2, supportsMultiWindow2, supportsSplitScreen, supportsFreeform, supportsPip, activityType)) {
                return 0;
            }
            return windowingMode2;
        }
        supportsSplitScreen = supportsSplitScreen2;
        supportsFreeform = supportsFreeform2;
        supportsPip = supportsPip2;
        if (!supportsMultiWindow) {
        }
        inSplitScreenMode = hasSplitScreenPrimaryStack();
        if (!inSplitScreenMode) {
        }
        if (inSplitScreenMode) {
        }
        windowingMode2 = windowingMode;
        if (windowingMode2 != 0) {
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getTopStack() {
        if (this.mStacks.isEmpty()) {
            return null;
        }
        ArrayList<ActivityStack> arrayList = this.mStacks;
        return arrayList.get(arrayList.size() - 1);
    }

    /* access modifiers changed from: package-private */
    public boolean isTopStack(ActivityStack stack) {
        return stack == getTopStack();
    }

    /* access modifiers changed from: package-private */
    public boolean isTopNotPinnedStack(ActivityStack stack) {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ActivityStack current = this.mStacks.get(i);
            if (!(current.inPinnedWindowingMode() || current.inHwPCMultiStackWindowingMode())) {
                return current == stack;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getTopStackInWindowingMode(int windowingMode) {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ActivityStack current = this.mStacks.get(i);
            if (windowingMode == current.getWindowingMode()) {
                return current;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningActivity() {
        return topRunningActivity(false);
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord topRunningActivity(boolean considerKeyguardState) {
        ActivityRecord topRunning = null;
        ActivityStack focusedStack = getFocusedStack();
        if (focusedStack != null) {
            topRunning = focusedStack.topRunningActivityLocked();
        }
        if (topRunning == null) {
            for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                ActivityStack stack = this.mStacks.get(i);
                if (!(stack == focusedStack || !stack.isFocusable() || (topRunning = stack.topRunningActivityLocked()) == null)) {
                    break;
                }
            }
        }
        if (topRunning == null || !considerKeyguardState || !this.mRootActivityContainer.mStackSupervisor.getKeyguardController().isKeyguardLocked() || topRunning.canShowWhenLocked()) {
            return topRunning;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getIndexOf(ActivityStack stack) {
        return this.mStacks.indexOf(stack);
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void onRequestedOverrideConfigurationChanged(Configuration overrideConfiguration) {
        DisplayContent displayContent;
        int currRotation = getRequestedOverrideConfiguration().windowConfiguration.getRotation();
        if (!(currRotation == -1 || currRotation == overrideConfiguration.windowConfiguration.getRotation() || (displayContent = this.mDisplayContent) == null)) {
            displayContent.applyRotationLocked(currRotation, overrideConfiguration.windowConfiguration.getRotation());
        }
        super.onRequestedOverrideConfigurationChanged(overrideConfiguration);
        if (this.mDisplayContent != null) {
            this.mService.mWindowManager.setNewDisplayOverrideConfiguration(overrideConfiguration, this.mDisplayContent);
        }
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent != null) {
            displayContent.preOnConfigurationChanged();
            this.mService.mHwATMSEx.onDisplayConfigurationChanged(this.mDisplayId);
        }
        super.onConfigurationChanged(newParentConfig);
    }

    /* access modifiers changed from: package-private */
    public void onLockTaskPackagesUpdated() {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            this.mStacks.get(i).onLockTaskPackagesUpdated();
        }
    }

    /* access modifiers changed from: package-private */
    public void onExitingSplitScreenMode() {
        this.mSplitScreenPrimaryStack = null;
    }

    /* access modifiers changed from: package-private */
    public void handleActivitySizeCompatModeIfNeeded(ActivityRecord r) {
        if (r.isState(ActivityStack.ActivityState.RESUMED) && r.getWindowingMode() == 1) {
            if (!r.inSizeCompatMode()) {
                if (this.mLastCompatModeActivity != null) {
                    this.mService.getTaskChangeNotificationController().notifySizeCompatModeActivityChanged(this.mDisplayId, null);
                }
                this.mLastCompatModeActivity = null;
            } else if (this.mLastCompatModeActivity != r) {
                this.mLastCompatModeActivity = r;
                this.mService.getTaskChangeNotificationController().notifySizeCompatModeActivityChanged(this.mDisplayId, r.appToken);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getSplitScreenPrimaryStack() {
        return this.mSplitScreenPrimaryStack;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSplitScreenPrimaryStack() {
        return this.mSplitScreenPrimaryStack != null;
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getPinnedStack() {
        return this.mPinnedStack;
    }

    /* access modifiers changed from: package-private */
    public boolean hasPinnedStack() {
        return this.mPinnedStack != null;
    }

    public String toString() {
        return "ActivityDisplay={" + this.mDisplayId + " numStacks=" + this.mStacks.size() + "}";
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public int getChildCount() {
        return this.mStacks.size();
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public ActivityStack getChildAt(int index) {
        return this.mStacks.get(index);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public ConfigurationContainer getParent() {
        return this.mRootActivityContainer;
    }

    /* access modifiers changed from: package-private */
    public boolean isPrivate() {
        return (this.mDisplay.getFlags() & 4) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isUidPresent(int uid) {
        Iterator<ActivityStack> it = this.mStacks.iterator();
        while (it.hasNext()) {
            if (it.next().isUidPresent(uid)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isRemoved() {
        return this.mRemoved;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void remove() {
        int windowingMode;
        boolean destroyContentOnRemoval = shouldDestroyContentOnRemove();
        ActivityStack lastReparentedStack = null;
        this.mPreferredTopFocusableStack = null;
        ActivityDisplay toDisplay = this.mRootActivityContainer.getDefaultDisplay();
        this.mRootActivityContainer.mStackSupervisor.beginDeferResume();
        try {
            int numStacks = this.mStacks.size();
            int stackNdx = 0;
            while (stackNdx < numStacks) {
                ActivityStack stack = this.mStacks.get(stackNdx);
                if (!destroyContentOnRemoval) {
                    if (stack.isActivityTypeStandardOrUndefined()) {
                        if (toDisplay.hasSplitScreenPrimaryStack()) {
                            windowingMode = 4;
                        } else {
                            windowingMode = 0;
                        }
                        stack.reparent(toDisplay, true, true);
                        if (!stack.inHwMagicWindowingMode()) {
                            stack.setWindowingMode(windowingMode);
                        }
                        lastReparentedStack = stack;
                        int stackNdx2 = stackNdx - (numStacks - this.mStacks.size());
                        numStacks = this.mStacks.size();
                        stackNdx = stackNdx2 + 1;
                    }
                }
                stack.finishAllActivitiesLocked(true);
                int stackNdx22 = stackNdx - (numStacks - this.mStacks.size());
                numStacks = this.mStacks.size();
                stackNdx = stackNdx22 + 1;
            }
            this.mRootActivityContainer.mStackSupervisor.endDeferResume();
            this.mRemoved = true;
            if (lastReparentedStack != null) {
                lastReparentedStack.postReparent();
            }
            releaseSelfIfNeeded();
            if (!this.mAllSleepTokens.isEmpty()) {
                this.mRootActivityContainer.mSleepTokens.removeAll(this.mAllSleepTokens);
                this.mAllSleepTokens.clear();
                this.mService.updateSleepIfNeededLocked();
            }
        } catch (Throwable th) {
            this.mRootActivityContainer.mStackSupervisor.endDeferResume();
            throw th;
        }
    }

    private void releaseSelfIfNeeded() {
        if (this.mRemoved && this.mDisplayContent != null) {
            ActivityStack stack = this.mStacks.size() == 1 ? this.mStacks.get(0) : null;
            if (stack != null && stack.isActivityTypeHome() && stack.getAllTasks().isEmpty()) {
                stack.remove();
            } else if (this.mStacks.isEmpty()) {
                this.mDisplayContent.removeIfPossible();
                this.mDisplayContent = null;
                this.mRootActivityContainer.removeChild(this);
                this.mRootActivityContainer.mStackSupervisor.getKeyguardController().onDisplayRemoved(this.mDisplayId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public IntArray getPresentUIDs() {
        this.mDisplayAccessUIDs.clear();
        Iterator<ActivityStack> it = this.mStacks.iterator();
        while (it.hasNext()) {
            it.next().getPresentUIDs(this.mDisplayAccessUIDs);
        }
        return this.mDisplayAccessUIDs;
    }

    /* access modifiers changed from: package-private */
    public boolean supportsSystemDecorations() {
        return this.mDisplayContent.supportsSystemDecorations();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldDestroyContentOnRemove() {
        return this.mDisplay.getRemoveMode() == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldSleep() {
        return (this.mStacks.isEmpty() || !this.mAllSleepTokens.isEmpty()) && this.mService.mRunningVoice == null;
    }

    /* access modifiers changed from: package-private */
    public void setFocusedApp(ActivityRecord r, boolean moveFocusNow) {
        AppWindowToken newFocus;
        if (this.mDisplayContent != null) {
            IBinder token = r.appToken;
            if (token == null) {
                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                    Slog.v("WindowManager", "Clearing focused app, displayId=" + this.mDisplayId);
                }
                newFocus = null;
            } else {
                AppWindowToken newFocus2 = this.mService.mWindowManager.mRoot.getAppWindowToken(token);
                if (newFocus2 == null) {
                    Slog.w("WindowManager", "Attempted to set focus to non-existing app token: " + token + ", displayId=" + this.mDisplayId);
                }
                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                    Slog.v("WindowManager", "Set focused app to: " + newFocus2 + " moveFocusNow=" + moveFocusNow + " displayId=" + this.mDisplayId);
                }
                newFocus = newFocus2;
            }
            boolean changed = this.mDisplayContent.setFocusedApp(newFocus);
            if (moveFocusNow && changed) {
                this.mService.mWindowManager.updateFocusedWindowLocked(0, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getStackAbove(ActivityStack stack) {
        int stackIndex = this.mStacks.indexOf(stack) + 1;
        if (stackIndex < this.mStacks.size()) {
            return this.mStacks.get(stackIndex);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void moveStackBehindBottomMostVisibleStack(ActivityStack stack) {
        if (!stack.shouldBeVisible(null)) {
            positionChildAtBottom(stack);
            int numStacks = this.mStacks.size();
            for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                ActivityStack s = this.mStacks.get(stackNdx);
                if (s != stack) {
                    int winMode = s.getWindowingMode();
                    boolean isValidWindowingMode = true;
                    if (!(winMode == 1 || winMode == 4 || winMode == 103)) {
                        isValidWindowingMode = false;
                    }
                    if (s.shouldBeVisible(null) && isValidWindowingMode) {
                        positionChildAt(stack, Math.max(0, stackNdx - 1));
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void moveStackBehindStack(ActivityStack stack, ActivityStack behindStack) {
        if (behindStack != null && behindStack != stack) {
            int stackIndex = this.mStacks.indexOf(stack);
            int behindStackIndex = this.mStacks.indexOf(behindStack);
            positionChildAt(stack, Math.max(0, stackIndex <= behindStackIndex ? behindStackIndex - 1 : behindStackIndex));
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureActivitiesVisible(ActivityRecord starting, int configChanges, boolean preserveWindows, boolean notifyClients) {
        for (int stackNdx = getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            getChildAt(stackNdx).ensureActivitiesVisibleLocked(starting, configChanges, preserveWindows, notifyClients);
        }
    }

    /* access modifiers changed from: package-private */
    public void moveHomeStackToFront(String reason) {
        ActivityStack activityStack = this.mHomeStack;
        if (activityStack != null) {
            activityStack.moveToFront(reason);
        }
    }

    /* access modifiers changed from: package-private */
    public void moveHomeActivityToTop(String reason) {
        ActivityRecord top = getHomeActivity();
        if (top == null) {
            moveHomeStackToFront(reason);
        } else {
            top.moveFocusableActivityToTop(reason);
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getHomeStack() {
        return this.mHomeStack;
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getHomeActivity() {
        return getHomeActivityForUser(this.mRootActivityContainer.mCurrentUser);
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getHomeActivityForUser(int userId) {
        ActivityStack activityStack = this.mHomeStack;
        if (activityStack == null) {
            return null;
        }
        ArrayList<TaskRecord> tasks = activityStack.getAllTasks();
        for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = tasks.get(taskNdx);
            if (task.isActivityTypeHome()) {
                ArrayList<ActivityRecord> activities = task.mActivities;
                for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                    ActivityRecord r = activities.get(activityNdx);
                    if (r.isActivityTypeHome() && (userId == -1 || r.mUserId == userId)) {
                        return r;
                    }
                }
                continue;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean isSleeping() {
        return this.mSleeping;
    }

    /* access modifiers changed from: package-private */
    public void setIsSleeping(boolean asleep) {
        if (ActivityTaskManagerDebugConfig.DEBUG_KEYGUARD) {
            Slog.v(TAG_KEYGUARD, "set asleep:" + asleep, new Exception());
        }
        this.mSleeping = asleep;
    }

    /* access modifiers changed from: package-private */
    public void registerStackOrderChangedListener(OnStackOrderChangedListener listener) {
        if (!this.mStackOrderChangedCallbacks.contains(listener)) {
            this.mStackOrderChangedCallbacks.add(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterStackOrderChangedListener(OnStackOrderChangedListener listener) {
        this.mStackOrderChangedCallbacks.remove(listener);
    }

    private void onStackOrderChanged(ActivityStack stack) {
        for (int i = this.mStackOrderChangedCallbacks.size() - 1; i >= 0; i--) {
            this.mStackOrderChangedCallbacks.get(i).onStackOrderChanged(stack);
        }
    }

    public void deferUpdateImeTarget() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent != null) {
            displayContent.deferUpdateImeTarget();
        }
    }

    public void continueUpdateImeTarget() {
        DisplayContent displayContent = this.mDisplayContent;
        if (displayContent != null) {
            displayContent.continueUpdateImeTarget();
        }
    }

    /* access modifiers changed from: package-private */
    public void setDisplayToSingleTaskInstance() {
        int childCount = getChildCount();
        if (childCount <= 1) {
            if (childCount > 0) {
                ActivityStack stack = getChildAt(0);
                if (stack.getChildCount() > 1) {
                    throw new IllegalArgumentException("Display stack already has multiple tasks. display=" + this + " stack=" + stack);
                }
            }
            this.mSingleTaskInstance = true;
            return;
        }
        throw new IllegalArgumentException("Display already has multiple stacks. display=" + this);
    }

    /* access modifiers changed from: package-private */
    public boolean isSingleTaskInstance() {
        return this.mSingleTaskInstance;
    }

    public void dump(PrintWriter pw, String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("displayId=");
        sb.append(this.mDisplayId);
        sb.append(" stacks=");
        sb.append(this.mStacks.size());
        sb.append(this.mSingleTaskInstance ? " mSingleTaskInstance" : "");
        pw.println(sb.toString());
        String myPrefix = prefix + " ";
        if (this.mHomeStack != null) {
            pw.println(myPrefix + "mHomeStack=" + this.mHomeStack);
        }
        if (this.mRecentsStack != null) {
            pw.println(myPrefix + "mRecentsStack=" + this.mRecentsStack);
        }
        if (this.mPinnedStack != null) {
            pw.println(myPrefix + "mPinnedStack=" + this.mPinnedStack);
        }
        if (this.mSplitScreenPrimaryStack != null) {
            pw.println(myPrefix + "mSplitScreenPrimaryStack=" + this.mSplitScreenPrimaryStack);
        }
        if (this.mPreferredTopFocusableStack != null) {
            pw.println(myPrefix + "mPreferredTopFocusableStack=" + this.mPreferredTopFocusableStack);
        }
        if (this.mLastFocusedStack != null) {
            pw.println(myPrefix + "mLastFocusedStack=" + this.mLastFocusedStack);
        }
    }

    public void dumpStacks(PrintWriter pw) {
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            pw.print(this.mStacks.get(i).mStackId);
            if (i > 0) {
                pw.print(",");
            }
        }
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, logLevel);
        proto.write(1120986464258L, this.mDisplayId);
        proto.write(1133871366150L, this.mSingleTaskInstance);
        ActivityStack focusedStack = getFocusedStack();
        if (focusedStack != null) {
            proto.write(1120986464260L, focusedStack.mStackId);
            ActivityRecord focusedActivity = focusedStack.getDisplay().getResumedActivity();
            if (focusedActivity != null) {
                focusedActivity.writeIdentifierToProto(proto, 1146756268037L);
            }
        } else {
            proto.write(1120986464260L, -1);
        }
        for (int stackNdx = this.mStacks.size() - 1; stackNdx >= 0; stackNdx--) {
            this.mStacks.get(stackNdx).writeToProto(proto, 2246267895811L, logLevel);
        }
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getRealLastFocusedStack() {
        return this.mRealLastFocusedStack;
    }

    /* access modifiers changed from: package-private */
    public void onCoordinationModeDismissed() {
        this.mService.mWindowManager.deferSurfaceLayout();
        try {
            for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                ActivityStack otherStack = this.mStacks.get(i);
                if (otherStack.inCoordinationSecondaryWindowingMode()) {
                    otherStack.setWindowingMode(1, false, false, false, true, false);
                }
            }
        } finally {
            this.mService.mWindowManager.continueSurfaceLayout();
        }
    }

    private void onCoordinationModeActivated() {
        this.mService.mWindowManager.deferSurfaceLayout();
        try {
            for (int i = this.mStacks.size() - 1; i >= 0; i--) {
                ActivityStack otherStack = this.mStacks.get(i);
                if (otherStack != this.mCoordinationPrimaryStack && !otherStack.inHwFreeFormWindowingMode()) {
                    if (!otherStack.inPinnedWindowingMode()) {
                        otherStack.setWindowingMode(12, false, false, false, true, false);
                    }
                }
            }
        } finally {
            this.mService.mWindowManager.continueSurfaceLayout();
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack getCoordinationPrimaryStack() {
        return this.mCoordinationPrimaryStack;
    }

    /* access modifiers changed from: package-private */
    public boolean hasCoordinationPrimaryStack() {
        return this.mCoordinationPrimaryStack != null;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<ActivityStack> getAllStacksInWindowingMode(int windowingMode) {
        ArrayList<ActivityStack> stacks = new ArrayList<>();
        for (int i = this.mStacks.size() - 1; i >= 0; i--) {
            ActivityStack otherStack = this.mStacks.get(i);
            if (otherStack.getWindowingMode() == windowingMode) {
                stacks.add(otherStack);
            }
        }
        return stacks;
    }
}
