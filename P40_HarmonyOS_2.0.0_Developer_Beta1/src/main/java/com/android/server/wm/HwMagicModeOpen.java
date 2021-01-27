package com.android.server.wm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import com.huawei.android.util.SlogEx;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class HwMagicModeOpen extends HwMagicModeBase {
    private static final int LEFT_BACK_LOOP_MAX_COUNT = 10;
    private static final String TAG = "HWMW_HwMagicModeOpen";

    public HwMagicModeOpen(HwMagicWinManager manager, HwMagicWinAmsPolicy policy, Context context) {
        super(manager, policy, context);
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void setPairForFinish(ActivityRecordEx finishActivity) {
        ActivityRecordEx finishPair = getPairNextActivity(finishActivity);
        if (finishPair != null) {
            finishPair.setLastActivityHash(finishActivity.getLastActivityHash());
        }
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void finishRightAfterFinishingLeft(ActivityRecordEx finishActivity) {
        if (this.mPolicy.getActivityByPosition(finishActivity, 1, 1) == null) {
            super.finishRightAfterFinishingLeft(finishActivity);
            return;
        }
        ActivityRecordEx needFinishActivity = getPairNextActivity(finishActivity);
        HwMagicContainer container = this.mMwManager.getContainer(needFinishActivity);
        SlogEx.i(TAG, "finishRightAfterFinishingLeft needFinishActivity=" + needFinishActivity);
        int exitCount = 0;
        while (needFinishActivity != null && exitCount < 10) {
            ActivityRecordEx next = this.mPolicy.getActivityByPosition(finishActivity, 2, 0);
            if (next != null) {
                if (!needFinishActivity.equalsActivityRecord(next)) {
                    exitCount++;
                    if (!this.mPolicy.isRelatedInSlave(container, next)) {
                        ActivityStackEx activityStackEx = finishActivity.getActivityStackEx();
                        HwMagicWinAmsPolicy hwMagicWinAmsPolicy = this.mPolicy;
                        activityStackEx.finishActivityLocked(next, 0, (Intent) null, HwMagicWinAmsPolicy.MAGIC_WINDOW_FINISH_EVENT, true, false);
                    }
                } else if (!this.mPolicy.isRelatedInSlave(container, needFinishActivity)) {
                    ActivityStackEx activityStackEx2 = finishActivity.getActivityStackEx();
                    HwMagicWinAmsPolicy hwMagicWinAmsPolicy2 = this.mPolicy;
                    activityStackEx2.finishActivityLocked(needFinishActivity, 0, (Intent) null, HwMagicWinAmsPolicy.MAGIC_WINDOW_FINISH_EVENT, true, false);
                    return;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    private ActivityRecordEx getPairNextActivity(ActivityRecordEx pairPreActivity) {
        HwMagicContainer container = this.mMwManager.getContainer(pairPreActivity);
        if (container == null) {
            return null;
        }
        int hashValue = System.identityHashCode(pairPreActivity.getShadow());
        List<TaskRecordEx> taskHistory = pairPreActivity.getActivityStackEx().getAllTaskRecordExs();
        for (int taskIndex = taskHistory.size() - 1; taskIndex >= 0; taskIndex--) {
            List<ActivityRecordEx> activityRecords = taskHistory.get(taskIndex).getActivityRecordExs();
            for (int activityIndex = activityRecords.size() - 1; activityIndex >= 0; activityIndex--) {
                ActivityRecordEx activity = activityRecords.get(activityIndex);
                if (activity != null && !activity.isFinishing() && container.getBoundsPosition(activity.getRequestedOverrideBounds()) == 2 && activity.getLastActivityHash() == hashValue) {
                    return activity;
                }
            }
        }
        return null;
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public boolean shouldEnterMagicWinForTah(ActivityRecordEx focus, ActivityRecordEx next) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        super.setOrigActivityToken(container, focus);
        ActivityRecordEx origActivity = ActivityRecordEx.forToken(this.mOrigActivityToken);
        if (this.mPolicy.isDefaultFullscreenActivity(container, next) || origActivity == null || origActivity.getStackId() != next.getStackId() || !isSpecPairActivities(origActivity, next, null) || this.mPolicy.isSpecTransActivityPreDefined(container, next)) {
            return false;
        }
        return true;
    }

    private boolean isSpecPairActivities(ActivityRecordEx focusRecord, ActivityRecordEx targetRecord, HwMagicContainer toContainer) {
        String focusPkg = Utils.getPackageName(focusRecord);
        String focusCls = Utils.getClassName(focusRecord);
        String targetName = Utils.getClassName(targetRecord);
        if (focusPkg != null && !focusPkg.equals(Utils.getPackageName(targetRecord))) {
            return false;
        }
        HwMagicContainer container = toContainer != null ? toContainer : this.mMwManager.getContainer(focusRecord);
        if (container == null || !container.getConfig().isSpecPairActivities(focusPkg, focusCls, targetName)) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void overrideIntent(ActivityRecordEx focus, ActivityRecordEx next, boolean isNewTask) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (container != null) {
            ActivityRecordEx origActivity = focus;
            super.setOrigActivityToken(container, focus);
            if (focus.inHwMagicWindowingMode()) {
                if (!this.mPolicy.isSpecTransActivityPreDefined(container, next) || this.mPolicy.mMagicWinSplitMng.isPkgSpliteScreenMode(next, true)) {
                    if (this.mOrigActivityToken != null) {
                        origActivity = ActivityRecordEx.forToken(this.mOrigActivityToken);
                        if (origActivity == null) {
                            SlogEx.w(TAG, "overrideIntent origActivity is null");
                            super.overrideIntent(focus, next, isNewTask);
                            return;
                        }
                        next.setLastActivityHash(System.identityHashCode(origActivity.getShadow()));
                    }
                } else if (!this.mMwManager.isDragFullMode(next)) {
                    next.setBounds(container.getBounds(2, Utils.getRealPkgName(next)));
                    return;
                } else {
                    return;
                }
            }
            if (origActivity.getStackId() != next.getStackId()) {
                origActivity = focus;
            }
            super.overrideIntent(origActivity, next, isNewTask);
        }
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public int getTargetWindowPosition(ActivityRecordEx focus, ActivityRecordEx next) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (this.mPolicy.isRelatedActivity(container, next)) {
            return 2;
        }
        if (this.mPolicy.isMainActivity(container, next)) {
            return 1;
        }
        if (this.mMwManager.getAmsPolicy().isHomeActivity(container, next)) {
            return 3;
        }
        if (!this.mMwManager.isMiddle(focus)) {
            return super.getTargetWindowPosition(focus, next);
        }
        if (this.mPolicy.isDefaultFullscreenActivity(container, next)) {
            return 5;
        }
        if (isSpecPairActivities(focus, next, null)) {
            return 2;
        }
        return 3;
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public boolean isMoveActivityToMaster(ActivityRecordEx focus, ActivityRecordEx next, int targetPosition) {
        if (isMoveLockToMaster(focus)) {
            return true;
        }
        boolean lockMaster = isLockMaster(focus);
        if (isSkippingMoveToMaster(focus, next) || targetPosition != 2 || lockMaster) {
            return false;
        }
        if ((this.mMwManager.isMiddle(focus) || this.mMwManager.isSlave(focus)) && isSpecPairActivities(focus, next, null)) {
            return true;
        }
        return false;
    }

    private boolean isMoveLockToMaster(ActivityRecordEx focus) {
        return !this.mPolicy.isDefaultFullscreenActivity(this.mMwManager.getContainer(focus), focus) && this.mMwManager.isSlave(focus) && isLockMasterActivity(focus);
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void adjustWindowForFinish(ActivityRecordEx activity, String finishReason) {
        if (!this.mMwManager.isSlave(activity) || this.mPolicy.getActivityByPosition(activity, 2, 1) != null || !setActivityBoundAfterFinishing(activity)) {
            adjustWindowForDoubleWindows(activity, finishReason);
        }
    }

    private boolean setActivityBoundAfterFinishing(ActivityRecordEx activity) {
        ArrayList<ActivityRecordEx> tempActivityList = this.mPolicy.getAllActivities(activity.getActivityStackEx());
        ActivityRecordEx.remove(tempActivityList, activity);
        return setActivityBoundForOpenEx(tempActivityList, Utils.getRealPkgName(activity), true, null);
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void setActivityBoundByMode(ArrayList<ActivityRecordEx> activities, String packageName, HwMagicContainer toContainer) {
        ActivityRecordEx topActivity;
        if (activities != null && activities.size() != 0) {
            setActivityBoundMainRelatedIfNeed(activities, packageName, toContainer);
            boolean z = true;
            if (activities.size() < 1) {
                SlogEx.d(TAG, "there is not any activity in the list, return");
                return;
            }
            HwMagicContainer container = toContainer != null ? toContainer : this.mMwManager.getContainer(activities.get(0));
            if (container != null) {
                if (this.mMwManager.isDragFullMode(activities.get(0))) {
                    super.setActivityBoundByMode(activities, packageName, toContainer);
                    return;
                }
                int index = getLockPageIndex(activities);
                boolean isBackToMw = setActivityBoundForOpenEx(activities, packageName, false, toContainer);
                if (isBackToMw) {
                    if (index != -1) {
                        enterLockMaster(activities, packageName, container, index);
                    }
                    if (container.isFoldableDevice() && isBackToMw && (topActivity = activities.get(0)) != null && !topActivity.isFullScreenVideoInLandscape()) {
                        if ((topActivity.getInfo().configChanges & 3328) == 3328) {
                            z = false;
                        }
                        topActivity.setForceNewConfig(z);
                        topActivity.ensureActivityConfiguration(0, false);
                    }
                }
            }
        }
    }

    private void enterLockMaster(ArrayList<ActivityRecordEx> activities, String packageName, HwMagicContainer toContainer, int index) {
        boolean hasFullscreenActivity = false;
        int i = activities.size() - 1;
        while (i >= 0) {
            ActivityRecordEx ar = activities.get(i);
            ar.setBounds(toContainer.getBounds(i < index ? 2 : 1, packageName));
            hasFullscreenActivity = setDefaultFullscreenBounds(ar, hasFullscreenActivity);
            i--;
        }
    }

    private boolean setActivityBoundForOpenEx(ArrayList<ActivityRecordEx> activities, String packageName, boolean isFromFinish, HwMagicContainer toContainer) {
        if (activities.size() < 1) {
            return false;
        }
        HwMagicContainer container = toContainer != null ? toContainer : this.mMwManager.getContainer(activities.get(0));
        if (container == null) {
            return false;
        }
        int middleIndex = 0;
        int rightIndex = 0;
        int leftIndex = 0;
        for (int activityIndex = 0; activityIndex < activities.size() - 1; activityIndex++) {
            ActivityRecordEx prevActivity = activities.get(activityIndex + 1);
            ActivityRecordEx current = activities.get(activityIndex);
            if (!current.isFinishing() && !prevActivity.isFinishing()) {
                if (this.mPolicy.isDefaultFullscreenActivity(container, current)) {
                    middleIndex = activityIndex + 1;
                    leftIndex = 0;
                    rightIndex = 0;
                } else if (isSpecPairActivities(prevActivity, current, toContainer) && leftIndex == 0) {
                    leftIndex = activityIndex + 1;
                    rightIndex = activityIndex;
                }
            }
        }
        if (rightIndex == 0 && leftIndex == 0) {
            SlogEx.i(TAG, "setActivityBoundForOpenEx Didn't find the pair activity");
            if (!this.mMwManager.isDragFullMode(activities.get(0))) {
                if (container.isFoldableDevice() && !isFromFinish) {
                    activities.get(0).getActivityStackEx().setWindowingMode(1);
                }
                if (!container.isPadDevice()) {
                    return false;
                }
            }
            if (middleIndex == 0) {
                super.setActivityBoundByMode(activities, packageName, toContainer);
                return true;
            }
        }
        setActivityBoundForOpenExInner(activities, leftIndex, middleIndex, isFromFinish, toContainer);
        return true;
    }

    private void setActivityBoundForOpenExInner(ArrayList<ActivityRecordEx> activities, int leftIndex, int middleIndex, boolean isFromFinish, HwMagicContainer toContainer) {
        Rect fullBounds;
        int i;
        HwMagicContainer container = toContainer != null ? toContainer : this.mMwManager.getContainer(activities.get(0));
        if (container != null) {
            String packageName = Utils.getRealPkgName(activities.get(0));
            Rect middleBounds = container.getBounds(3, packageName);
            Rect fullBounds2 = container.getBounds(5, packageName);
            ActivityStackEx leftActivityStack = activities.get(leftIndex).getActivityStackEx();
            if (isFromFinish || leftActivityStack.getWindowingMode() != 1) {
                fullBounds = fullBounds2;
                i = 1;
                leftActivityStack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
            } else {
                fullBounds = fullBounds2;
                i = 1;
                leftActivityStack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, false, false, false, true, false);
            }
            leftActivityStack.resize((Rect) null, (Rect) null, (Rect) null);
            Rect masterBounds = container.getBounds(i, packageName);
            Rect slaveBounds = container.getBounds(2, packageName);
            for (int index = 0; index < activities.size(); index++) {
                ActivityRecordEx activity = activities.get(index);
                if (!activity.isFinishing()) {
                    if (index < middleIndex) {
                        activity.setBounds(container.isFoldableDevice() ? middleBounds : fullBounds);
                    } else if (index < leftIndex) {
                        activity.setBounds(slaveBounds);
                    } else if (index == leftIndex) {
                        activity.setBounds(masterBounds);
                    } else if (index != leftIndex + 1 || !isSpecPairActivities(activity, activities.get(leftIndex), toContainer)) {
                        if (container.isPadDevice()) {
                            activity.setBounds(middleBounds);
                        }
                        SlogEx.i(TAG, "setActivityBoundForOpenEx keep bound");
                    } else {
                        activity.setBounds(masterBounds);
                    }
                }
            }
        }
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public boolean isExitSliding(ActivityRecordEx finishActivity, ActivityRecordEx secondSlaveActivity, String finishReason) {
        if (secondSlaveActivity == null || HwMagicWinAmsPolicy.MAGIC_WINDOW_FINISH_EVENT.equals(finishReason) || !this.mMwManager.isSlave(finishActivity)) {
            return false;
        }
        if (!this.mPolicy.isRelatedActivity(this.mMwManager.getContainer(finishActivity), secondSlaveActivity)) {
            return isPrevActivity(finishActivity, secondSlaveActivity);
        }
        if (this.mPolicy.getActivityByPosition(finishActivity, 1, 1) == null) {
            return true;
        }
        return false;
    }

    private boolean isPrevActivity(ActivityRecordEx sourceActivity, ActivityRecordEx targetActivity) {
        return sourceActivity.getLastActivityHash() == System.identityHashCode(targetActivity.getShadow());
    }
}
