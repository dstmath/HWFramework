package com.android.server.wm;

import android.graphics.Rect;
import android.util.Flog;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class HwSplitScreenCombination {
    private static final String TAG = "HwSplitScreenCombination";
    int mDisplayId = -1;
    private ActivityStack mHwSplitScreenPrimaryStack = null;
    private ActivityStack mHwSplitScreenSecondaryStack = null;
    int mSplitRatio = 0;

    public boolean isSplitScreenCombined() {
        return (this.mHwSplitScreenPrimaryStack == null || this.mHwSplitScreenSecondaryStack == null) ? false : true;
    }

    public boolean hasHwSplitScreenPrimaryStack() {
        return this.mHwSplitScreenPrimaryStack != null;
    }

    public boolean hasHwSplitScreenSecondaryStack() {
        return this.mHwSplitScreenSecondaryStack != null;
    }

    public boolean hasHwSplitScreenStack(int stackId) {
        ActivityStack activityStack;
        ActivityStack activityStack2 = this.mHwSplitScreenPrimaryStack;
        return (activityStack2 != null && activityStack2.mStackId == stackId) || ((activityStack = this.mHwSplitScreenSecondaryStack) != null && activityStack.mStackId == stackId);
    }

    public boolean hasHwSplitScreenStack(ActivityStack stack) {
        if (stack == null) {
            return false;
        }
        if (this.mHwSplitScreenPrimaryStack == stack || this.mHwSplitScreenSecondaryStack == stack) {
            return true;
        }
        return false;
    }

    public boolean hasStackAndMatchWindowMode(ActivityStack stack) {
        if (stack == null) {
            return false;
        }
        if ((this.mHwSplitScreenPrimaryStack != stack || !stack.inHwSplitScreenPrimaryWindowingMode()) && (this.mHwSplitScreenSecondaryStack != stack || !stack.inHwSplitScreenSecondaryWindowingMode())) {
            return false;
        }
        return true;
    }

    public void addStackReferenceIfNeeded(ActivityStack stack) {
        if (stack != null && stack.inHwMultiStackWindowingMode()) {
            if (stack.inHwSplitScreenPrimaryWindowingMode()) {
                this.mHwSplitScreenPrimaryStack = stack;
                this.mDisplayId = stack.mDisplayId;
            } else if (stack.inHwSplitScreenSecondaryWindowingMode()) {
                this.mHwSplitScreenSecondaryStack = stack;
                this.mDisplayId = stack.mDisplayId;
            }
        }
    }

    public void removeStackReferenceIfNeeded(ActivityStack stack) {
        if (stack != null) {
            if (this.mHwSplitScreenPrimaryStack == stack) {
                this.mHwSplitScreenPrimaryStack = null;
                ActivityStack stackToReparent = this.mHwSplitScreenSecondaryStack;
                this.mHwSplitScreenSecondaryStack = null;
                handleRemoveSplitStack(stack, stackToReparent);
            } else if (this.mHwSplitScreenSecondaryStack == stack) {
                this.mHwSplitScreenSecondaryStack = null;
                ActivityStack stackToReparent2 = this.mHwSplitScreenPrimaryStack;
                this.mHwSplitScreenPrimaryStack = null;
                handleRemoveSplitStack(stack, stackToReparent2);
            }
        }
    }

    private void handleRemoveSplitStack(ActivityStack removedStack, ActivityStack leftStack) {
        if (leftStack != null) {
            if (leftStack.getTaskStack() != null) {
                leftStack.getTaskStack().clearAdjustedBounds();
            }
            if (removedStack.inHwFreeFormWindowingMode()) {
                HwMultiWindowManager.exitHwMultiStack(leftStack, false, false, false, true, false);
            } else {
                HwMultiWindowManager.exitHwMultiStack(leftStack);
            }
            setResumedActivityUncheckLocked(leftStack);
        }
    }

    public Rect getHwSplitScreenStackBounds(int windowingMode) {
        ActivityStack activityStack = this.mHwSplitScreenPrimaryStack;
        if (activityStack != null && activityStack.getWindowingMode() == windowingMode) {
            return this.mHwSplitScreenPrimaryStack.getBounds();
        }
        ActivityStack activityStack2 = this.mHwSplitScreenSecondaryStack;
        if (activityStack2 == null || activityStack2.getWindowingMode() != windowingMode) {
            return null;
        }
        return this.mHwSplitScreenSecondaryStack.getBounds();
    }

    public List<ActivityStack> findCombinedSplitScreenStacks(ActivityStack stack) {
        List<ActivityStack> combinedStacks = new ArrayList<>();
        ActivityStack activityStack = this.mHwSplitScreenPrimaryStack;
        if (activityStack == stack) {
            combinedStacks.add(this.mHwSplitScreenSecondaryStack);
        } else if (this.mHwSplitScreenSecondaryStack == stack) {
            combinedStacks.add(activityStack);
        }
        return combinedStacks;
    }

    public void replaceCombinedSplitScreenStack(ActivityStack stack) {
        if (stack.inHwSplitScreenPrimaryWindowingMode()) {
            ActivityStack stackToReparent = this.mHwSplitScreenPrimaryStack;
            this.mHwSplitScreenPrimaryStack = stack;
            handleReplaceSplitStack(stackToReparent);
        } else if (stack.inHwSplitScreenSecondaryWindowingMode()) {
            ActivityStack stackToReparent2 = this.mHwSplitScreenSecondaryStack;
            this.mHwSplitScreenSecondaryStack = stack;
            handleReplaceSplitStack(stackToReparent2);
        }
    }

    private void handleReplaceSplitStack(ActivityStack stack) {
        if (stack != null) {
            ActivityRecord topActivity = stack.getTopActivity();
            if (topActivity != null) {
                stack.mService.mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(topActivity.appToken, false);
            }
            stack.mService.mHwATMSEx.doReplaceSplitStack(stack);
        }
    }

    private void setResumedActivityUncheckLocked(ActivityStack stack) {
        ActivityRecord top = stack.topRunningActivityLocked();
        if (top != null && top != stack.mService.getLastResumedActivityRecord() && top == stack.mService.mRootActivityContainer.getTopResumedActivity()) {
            stack.mService.setResumedActivityUncheckLocked(top, "onHwSplitScreenModeDismissed");
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("hw split primary: ");
        ActivityStack activityStack = this.mHwSplitScreenPrimaryStack;
        String str = null;
        sb.append(activityStack != null ? activityStack.toShortString() : null);
        sb.append(", hw split secondary: ");
        ActivityStack activityStack2 = this.mHwSplitScreenSecondaryStack;
        if (activityStack2 != null) {
            str = activityStack2.toShortString();
        }
        sb.append(str);
        return sb.toString();
    }

    public boolean isSplitScreenCombinedAndVisible() {
        if (isSplitScreenCombined() && isHwSplitScreenVisible(this.mHwSplitScreenPrimaryStack) && isHwSplitScreenVisible(this.mHwSplitScreenSecondaryStack)) {
            return true;
        }
        return false;
    }

    private boolean isHwSplitScreenVisible(ActivityStack activityStack) {
        return (activityStack.getTaskStack() != null && activityStack.getTaskStack().isVisible()) || activityStack.isTopActivityVisible();
    }

    public boolean isSplitScreenVisible() {
        if (this.mHwSplitScreenPrimaryStack == null && this.mHwSplitScreenSecondaryStack == null) {
            return false;
        }
        ActivityStack activityStack = this.mHwSplitScreenSecondaryStack;
        if (activityStack == null) {
            return isHwSplitScreenVisible(this.mHwSplitScreenPrimaryStack);
        }
        ActivityStack activityStack2 = this.mHwSplitScreenPrimaryStack;
        if (activityStack2 == null) {
            return isHwSplitScreenVisible(activityStack);
        }
        if (!isHwSplitScreenVisible(activityStack2) || !isHwSplitScreenVisible(this.mHwSplitScreenSecondaryStack)) {
            return false;
        }
        return true;
    }

    public void resizeHwSplitStacks(int splitRatio, boolean isEnsureVisible) {
        if (isSplitScreenCombined()) {
            ActivityStack activityStack = this.mHwSplitScreenPrimaryStack;
            if (!(activityStack == null || this.mHwSplitScreenSecondaryStack == null)) {
                DividerBarDragEventReport.bdReport(activityStack.mService.mContext, this.mSplitRatio, splitRatio, this.mHwSplitScreenPrimaryStack.getConfiguration().orientation);
            }
            if (splitRatio == 0 || splitRatio == 1 || splitRatio == 2) {
                this.mSplitRatio = splitRatio;
                Rect primaryOutBounds = new Rect();
                Rect secondaryOutBounds = new Rect();
                HwMultiWindowManager.calcHwSplitStackBounds(this.mHwSplitScreenPrimaryStack.getDisplay(), splitRatio, primaryOutBounds, secondaryOutBounds);
                this.mHwSplitScreenPrimaryStack.resize(primaryOutBounds, (Rect) null, (Rect) null);
                this.mHwSplitScreenSecondaryStack.resize(secondaryOutBounds, (Rect) null, (Rect) null);
                if (isEnsureVisible) {
                    this.mHwSplitScreenSecondaryStack.mService.mStackSupervisor.mRootActivityContainer.ensureActivitiesVisible((ActivityRecord) null, 0, true);
                }
            } else if (splitRatio == 3) {
                this.mSplitRatio = splitRatio;
                handleSplitScreenRatioFull(this.mHwSplitScreenSecondaryStack, this.mHwSplitScreenPrimaryStack);
            } else if (splitRatio == 4) {
                this.mSplitRatio = splitRatio;
                handleSplitScreenRatioFull(this.mHwSplitScreenPrimaryStack, this.mHwSplitScreenSecondaryStack);
            }
        }
    }

    private void handleSplitScreenRatioFull(ActivityStack stackInVisible, ActivityStack stackRemaining) {
        ActivityRecord topActivity = stackInVisible.getTopActivity();
        if (topActivity != null) {
            stackInVisible.mService.mWindowManager.getWindowManagerServiceEx().takeTaskSnapshot(topActivity.appToken, false);
        }
        stackInVisible.getDisplay().positionChildAtBottom(stackInVisible);
        HwMultiWindowManager.exitHwMultiStack(stackInVisible, false, false, false, true, false);
    }

    public ActivityStack getHwSplitScreenPrimaryStack() {
        return this.mHwSplitScreenPrimaryStack;
    }

    public ActivityStack getHwSplitScreenSecondaryStack() {
        return this.mHwSplitScreenSecondaryStack;
    }

    public void reportPkgNameEvent(ActivityTaskManagerService activityTaskManagerService) {
        activityTaskManagerService.mH.post(new Runnable(activityTaskManagerService) {
            /* class com.android.server.wm.$$Lambda$HwSplitScreenCombination$4Sw_1_xoH8oXU5IKsyrokC4punE */
            private final /* synthetic */ ActivityTaskManagerService f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwSplitScreenCombination.this.lambda$reportPkgNameEvent$0$HwSplitScreenCombination(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$reportPkgNameEvent$0$HwSplitScreenCombination(ActivityTaskManagerService activityTaskManagerService) {
        synchronized (activityTaskManagerService.getGlobalLock()) {
            if (isSplitScreenCombined()) {
                ActivityRecord primaryTopActivity = this.mHwSplitScreenPrimaryStack.getTopActivity();
                ActivityRecord secondTopActivity = this.mHwSplitScreenSecondaryStack.getTopActivity();
                if (!(primaryTopActivity == null || secondTopActivity == null)) {
                    try {
                        JSONObject comboPkgNameRecord = new JSONObject();
                        comboPkgNameRecord.put("priPkg", primaryTopActivity.packageName);
                        comboPkgNameRecord.put("secPkg", secondTopActivity.packageName);
                        Flog.bdReport(991311030, comboPkgNameRecord);
                    } catch (JSONException e) {
                        Slog.e(TAG, "create json from split screen combination package names failed.");
                    }
                }
            }
        }
    }
}
