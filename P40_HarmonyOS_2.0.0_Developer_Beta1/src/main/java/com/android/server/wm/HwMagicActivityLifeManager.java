package com.android.server.wm;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.wm.ActivityStackEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.magicwin.HwMagicWinStatistics;
import com.huawei.server.utils.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public class HwMagicActivityLifeManager {
    private static final int PARAM_INDEX_ONE = 1;
    private static final int SIZE_SHOULD_CALL_IDLE = 2;
    private static final String TAG = "HWMW_HwMagicActivityLifeManager";
    private ActivityTaskManagerServiceEx mActivityTaskManager;
    private ActivityManagerServiceEx mAms;
    private HwMagicWinAmsPolicy mAmsPolicy;
    private HwMagicWinSplitManager mMagicWinSplitMng;
    private HwMagicWinManager mMwManager;

    public HwMagicActivityLifeManager(ActivityManagerServiceEx ams, HwMagicWinManager manager, HwMagicWinSplitManager magicWinSplitMng, HwMagicWinAmsPolicy policy) {
        this.mAms = ams;
        this.mMwManager = manager;
        this.mAmsPolicy = policy;
        this.mActivityTaskManager = ams.getActivityTaskManagerEx();
        this.mMagicWinSplitMng = magicWinSplitMng;
    }

    public void resumeActivityForHwMagicWin(ActivityRecordEx resumeActivity) {
        int mode;
        ActivityRecordEx topActivity;
        HwMagicContainer container = this.mMwManager.getContainer(resumeActivity);
        if (container != null) {
            SlogEx.i(TAG, "### Execute -> resumeActivityForHwMagicWin ActivityRecord resumeActivity = " + resumeActivity);
            String pkg = Utils.getRealPkgName(resumeActivity);
            if (this.mMwManager.isDragFullMode(resumeActivity)) {
                mode = resumeActivity.getTaskRecordEx().getDragFullMode();
                resumeActivity.setIsFromFullscreenToMagicWin(true);
            } else {
                mode = resumeActivity.inHwMagicWindowingMode() ? -1 : -2;
            }
            HwMagicWinStatistics.getInstance(container.getType()).startTick(container.getConfig(), pkg, mode, "resume");
            container.getCameraRotation().updateCameraRotation(1);
            requestRotation(resumeActivity);
            if (container.getConfig().isSupportAppTaskSplitScreen(pkg)) {
                this.mMagicWinSplitMng.addOrUpdateMainActivityStat(container, resumeActivity);
            }
            startRightOnResume(resumeActivity, pkg);
            if (resumeActivity.inHwMagicWindowingMode()) {
                if (resumeActivity.isTopRunningActivity()) {
                    if (this.mMwManager.isFull(resumeActivity)) {
                        SlogEx.i(TAG, "resumeActivityForHwMagicWin isFull, change to false");
                        this.mMwManager.getUIController().updateMwWallpaperVisibility(false, container.getDisplayId(), false);
                    } else if (this.mMwManager.isMiddle(resumeActivity) || isTaskSpliteAppInMwBound(pkg, container, resumeActivity)) {
                        SlogEx.i(TAG, "resumeActivityForHwMagicWin is magic mode, change to true");
                        this.mMwManager.getUIController().updateMwWallpaperVisibility(true, container.getDisplayId(), false);
                    }
                }
                this.mMagicWinSplitMng.resizeStackWhileResumeSplitAppIfNeed(pkg, resumeActivity);
                if (this.mMagicWinSplitMng.isPkgSpliteScreenMode(resumeActivity, true)) {
                    this.mMagicWinSplitMng.resizeStackIfNeedOnresume(resumeActivity);
                }
                if (this.mAmsPolicy.isMainActivity(container, resumeActivity) && this.mMwManager.isMaster(resumeActivity) && !container.getConfig().isSupportAppTaskSplitScreen(pkg)) {
                    this.mAmsPolicy.startRelateActivityIfNeed(resumeActivity, false);
                }
                TaskRecordEx taskRecord = resumeActivity.getTaskRecordEx();
                if (taskRecord != null && taskRecord.getChildCount() == 2 && (topActivity = taskRecord.getChildAt(taskRecord.getChildCount() - 1)) != null && topActivity.isFinishing() && this.mMwManager.isSlave(topActivity)) {
                    SlogEx.d(TAG, "resumeActivityForHwMagicWin call scheduleIdleLocked");
                    this.mAmsPolicy.scheduleIdleLocked();
                }
                adjustWindowForResume(resumeActivity);
                this.mAmsPolicy.checkResumeStateForMagicWindow(resumeActivity);
                this.mAmsPolicy.checkBackgroundForMagicWindow(resumeActivity);
                boolean canShowWhileOccluded = this.mAms.getWindowManagerServiceEx().getWindowManagerPolicyEx().isKeyguardOccluded();
                boolean isKeyguardLocked = this.mActivityTaskManager.getKeyguardControllerFromStackSupervisor().isKeyguardLocked();
                boolean isTopActivity = resumeActivity.equalsActivityRecord(this.mAmsPolicy.getTopActivity(container));
                if (canShowWhileOccluded && isTopActivity && resumeActivity.isNowVisible() && !this.mMwManager.isFull(resumeActivity)) {
                    if (isKeyguardLocked) {
                        resumeActivity.setBounds(container.getBounds(3, Utils.getPackageName(resumeActivity)));
                    } else {
                        adjustWindowForMiddle(resumeActivity);
                    }
                }
                if (resumeActivity.isFinishAllRightBottom()) {
                    this.mAmsPolicy.finishMagicWindow(resumeActivity, false);
                }
                resumeActivity.setIsFinishAllRightBottom(false);
                if (this.mMwManager.isMaster(resumeActivity)) {
                    resumeUnusualActivity(resumeActivity, 2);
                }
                if (this.mMwManager.isSlave(resumeActivity)) {
                    resumeUnusualActivity(resumeActivity, 1);
                }
                this.mMwManager.getUIController().updateBgColor(container.getDisplayId());
            } else if (resumeActivity.getWindowingMode() == 1) {
                this.mMwManager.getUIController().updateSplitBarVisibility(false, container.getDisplayId());
            }
        }
    }

    private void adjustWindowForResume(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container != null) {
            HwMagicWinAmsPolicy hwMagicWinAmsPolicy = this.mAmsPolicy;
            if (!HwMagicWinAmsPolicy.TRANSITION_ACTIVITIES.contains(Utils.getClassName(activity)) && !this.mMagicWinSplitMng.isPkgSpliteScreenMode(activity, true)) {
                if (this.mMwManager.isMaster(activity) && !activity.isFinishing() && this.mAmsPolicy.getActivityByPosition(activity, 2, 0) == null) {
                    if (this.mAmsPolicy.getActivityByPosition(activity, 1, 1) == null || this.mAmsPolicy.isMainActivity(container, activity)) {
                        this.mAmsPolicy.moveWindow(activity, 3);
                    } else {
                        this.mAmsPolicy.moveWindow(activity, 2);
                        return;
                    }
                }
                if (this.mMwManager.isSlave(activity) && this.mAmsPolicy.getActivityByPosition(activity, 1, 0) == null) {
                    ActivityRecordEx activityRecord = this.mAmsPolicy.getActivityByPosition(activity, 3, 0);
                    if (activityRecord != null) {
                        this.mAmsPolicy.moveWindow(activityRecord, 1);
                        return;
                    }
                    this.mAmsPolicy.moveWindow(activity, 3);
                }
                if ((this.mMwManager.isMaster(activity) || this.mMwManager.isSlave(activity) || this.mAmsPolicy.isShowDragBar(activity)) && container.getConfig().isDragable(Utils.getRealPkgName(activity))) {
                    this.mMwManager.getUIController().updateSplitBarVisibility(true, container.getDisplayId());
                } else {
                    this.mMwManager.getUIController().updateSplitBarVisibility(false, container.getDisplayId());
                }
            }
        }
    }

    private void adjustWindowForMiddle(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container == null || !this.mMwManager.isMiddle(activity) || container.getConfig().isDefaultFullscreenActivity(Utils.getRealPkgName(activity), Utils.getClassName(activity))) {
            return;
        }
        if (this.mAmsPolicy.getActivityByPosition(activity, 1, 0) != null) {
            this.mAmsPolicy.moveWindow(activity, 2);
        } else if (this.mAmsPolicy.getActivityByPosition(activity, 2, 0) != null) {
            this.mAmsPolicy.moveWindow(activity, 1);
        }
    }

    private void resumeUnusualActivity(ActivityRecordEx resumeActivity, int windowPosition) {
        ActivityRecordEx activity = this.mAmsPolicy.getActivityByPosition(resumeActivity, windowPosition, 0);
        if (activity != null && !this.mAmsPolicy.isKeyguardLockedAndOccluded()) {
            if (activity.getAppWindowTokenEx() != null && activity.getAppWindowTokenEx().findMainWindow() == null) {
                moveToFrontForResumeUnusual(activity);
            } else if (this.mMwManager.isSupportMultiResume(Utils.getPackageName(activity)) && !activity.isState(ActivityStackEx.ActivityState.RESUMED)) {
                if (windowPosition != 1 || this.mAmsPolicy.isSupportLeftResume(this.mMwManager.getContainer(activity), activity) || (!activity.isState(ActivityStackEx.ActivityState.PAUSED, ActivityStackEx.ActivityState.PAUSING, ActivityStackEx.ActivityState.RESUMED) && !activity.isState(ActivityStackEx.ActivityState.STOPPED) && !activity.isState(ActivityStackEx.ActivityState.STOPPING))) {
                    moveToFrontForResumeUnusual(activity);
                }
            }
        }
    }

    private void moveToFrontForResumeUnusual(ActivityRecordEx activity) {
        SlogEx.i(TAG, "moveToFrontForResumeUnusual unusualActivity = " + activity);
        activity.getTaskRecordEx().moveActivityToFrontLocked(activity);
        this.mActivityTaskManager.getRootActivityContainer().resumeFocusedStacksTopActivities();
    }

    private void startRightOnResume(ActivityRecordEx resumeAr, String pkg) {
        if (this.mAmsPolicy.isPkgInLoginStatus(resumeAr) && this.mAmsPolicy.isNeedStartOrMoveRight(resumeAr, pkg)) {
            HwMagicContainer container = this.mMwManager.getContainer(resumeAr);
            if (this.mAmsPolicy.isMainActivity(container, resumeAr) && resumeAr.getTaskRecordEx().getStack().equalsStack(this.mAmsPolicy.getFocusedTopStack(container))) {
                SlogEx.w(TAG, "resumeAr on the top");
                startRelatedAndSetMainMode(resumeAr, pkg);
            } else if (this.mAmsPolicy.isRelatedActivity(container, resumeAr)) {
                ActivityRecordEx preAr = this.mAmsPolicy.getActivityByPosition(resumeAr, 0, 1);
                if (preAr != null && preAr.getTaskRecordEx() != null && this.mMwManager.isMiddle(preAr) && this.mMwManager.isSlave(resumeAr)) {
                    resumeAr.getTaskRecordEx().moveActivityToFrontLocked(preAr);
                    SlogEx.i(TAG, "start right resume move the middle to top");
                }
            } else {
                SlogEx.d(TAG, "start right other activity");
            }
        }
    }

    private void startRelatedAndSetMainMode(ActivityRecordEx mainAr, String pkgName) {
        HwMagicContainer container = this.mMwManager.getContainer(mainAr);
        if (container != null) {
            ArrayList<ActivityRecordEx> arList = this.mAmsPolicy.getAllActivities(mainAr.getActivityStackEx());
            ArrayList<ActivityRecordEx> otherArList = new ArrayList<>();
            Iterator<ActivityRecordEx> it = arList.iterator();
            while (it.hasNext()) {
                ActivityRecordEx ar = it.next();
                if (ar != null && !this.mAmsPolicy.isMainActivity(container, ar)) {
                    otherArList.add(ar);
                }
            }
            if (!this.mMwManager.isMaster(mainAr)) {
                this.mMagicWinSplitMng.addOrUpdateMainActivityStat(container, mainAr);
                updateActivityModeAndBounds(mainAr, container.getBounds(1, pkgName), HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
            }
            SlogEx.i(TAG, "start other activity size : " + otherArList.size());
            if (otherArList.size() == 0) {
                String relateActName = container.getConfig().getRelateActivity(pkgName);
                if (!relateActName.isEmpty()) {
                    this.mAmsPolicy.startRelateActivity(pkgName, relateActName, mainAr);
                    return;
                }
                return;
            }
            Iterator<ActivityRecordEx> it2 = otherArList.iterator();
            while (it2.hasNext()) {
                ActivityRecordEx otherAr = it2.next();
                SlogEx.i(TAG, "setLoginStatus startRelatedAndSetMode start otherAr = " + otherAr + " pkgName = " + pkgName);
                if (otherAr != null && !this.mMwManager.isSlave(otherAr) && !this.mMwManager.isMaster(otherAr)) {
                    HwMagicWinAmsPolicy hwMagicWinAmsPolicy = this.mAmsPolicy;
                    if (!HwMagicWinAmsPolicy.PERMISSION_ACTIVITY.equals(Utils.getClassName(otherAr))) {
                        updateActivityModeAndBounds(otherAr, container.getBounds(2, pkgName), HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
                    }
                }
            }
        }
    }

    private void updateActivityModeAndBounds(ActivityRecordEx activityRecord, Rect bounds, int windowMode) {
        if (activityRecord != null) {
            ActivityStackEx activityStack = activityRecord.getTaskRecordEx().getStack();
            if (!(activityStack == null || activityStack.getWindowingMode() == windowMode)) {
                activityStack.setWindowingMode(windowMode);
            }
            SlogEx.i(TAG, "updateActivityModeAndBounds activityRecord = " + activityRecord + " bounds = " + bounds);
            activityRecord.setWindowingMode(windowMode);
            activityRecord.setBounds(bounds);
        }
    }

    private boolean isTaskSpliteAppInMwBound(String pkg, HwMagicContainer container, ActivityRecordEx ar) {
        return container.getConfig().isSupportAppTaskSplitScreen(pkg) && (this.mMwManager.isMaster(ar) || this.mMwManager.isSlave(ar));
    }

    @SuppressLint({"SourceLockedOrientationActivity"})
    private void requestRotation(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container != null && container.isFoldableDevice() && activity.getActivityStackEx().getWindowingMode() == 1 && !activity.isFullScreenVideoInLandscape()) {
            if ((container.getOrientation() == 2) && container.getHwMagicWinEnabled(Utils.getRealPkgName(activity))) {
                activity.setRequestedOrientation(1);
            }
        }
    }
}
