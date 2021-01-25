package com.android.server.wm;

import android.content.pm.ActivityInfoEx;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.util.HwMwUtils;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.wm.ActivityStackEx;
import com.huawei.android.app.ActivityOptionsEx;
import com.huawei.android.content.res.ConfigurationAdapter;
import com.huawei.android.util.SlogEx;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.magicwin.HwMagicWinStatistics;
import com.huawei.server.utils.Utils;
import com.huawei.utils.HwPartResourceUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class HwMagicModeSwitcher {
    private static final String CAMERA_PACKAGE = "com.huawei.camera";
    private static final int COMPUTER_RETURN_VALUE_NEGATIVE = -1;
    private static final String INSTALLER_PACKAGE = "com.android.packageinstaller";
    private static final String TAG = "HWMW_HwMagicModeSwitcher";
    private ActivityTaskManagerServiceEx mActivityTaskManager;
    private final ActivityManagerServiceEx mAms;
    private Comparator<ActivityRecordEx> mComputor = new Comparator<ActivityRecordEx>() {
        /* class com.android.server.wm.HwMagicModeSwitcher.AnonymousClass1 */

        public int compare(ActivityRecordEx activity1, ActivityRecordEx activity2) {
            if (activity1.getCreateTime() > activity2.getCreateTime()) {
                return 1;
            }
            return -1;
        }
    };
    private HwMagicWinManager mMwManager;
    private HwMagicWinAmsPolicy mPolicy;
    private Set<Integer> mStackIdsLastInMagicWindow = new HashSet();
    private Set<Integer> mStackIdsMoveToMagicWindow = new HashSet(1);

    public HwMagicModeSwitcher(HwMagicWinAmsPolicy policy, HwMagicWinManager manager, ActivityManagerServiceEx ams) {
        this.mPolicy = policy;
        this.mMwManager = manager;
        this.mAms = ams;
        this.mActivityTaskManager = ams.getActivityTaskManagerEx();
    }

    public void processSpliteScreenForMutilWin(int stackId, boolean isFreeze, int orientation, Bundle result) {
        if (!HwMwUtils.isInSuitableScene(true)) {
            SlogEx.w(TAG, "orientation is portrait or not in suit scene");
            return;
        }
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null && container.isInMagicWinOrientation()) {
            ActivityDisplayEx display = this.mActivityTaskManager.getRootActivityContainer().getDefaultDisplay();
            ActivityStackEx primaryStack = display.getStackEx(stackId);
            if (primaryStack == null) {
                SlogEx.w(TAG, "primaryStack is a null object");
                return;
            }
            ActivityRecordEx topActivity = primaryStack.getTopActivity();
            if (!isStayInOtherWinMode(topActivity)) {
                String pkgName = Utils.getRealPkgName(topActivity);
                if (!container.getHwMagicWinEnabled(pkgName) || !primaryStack.inPinnedWindowingMode()) {
                    if (primaryStack.inSplitScreenPrimaryWindowingMode()) {
                        moveActivityToMagicWindow(display, null, true);
                    }
                    if (container.getHwMagicWinEnabled(pkgName)) {
                        primaryStack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
                        primaryStack.resize((Rect) null, (Rect) null, (Rect) null);
                        setMagicActivityBound(primaryStack, pkgName, null);
                        result.putBoolean("RESULT_SPLITE_SCREEN", primaryStack.getWindowingMode() == 103);
                        if (isFreeze) {
                            this.mAms.getWindowManagerServiceEx().stopFreezingScreen();
                        }
                        this.mPolicy.updateStackVisibility(topActivity, true);
                        return;
                    }
                    return;
                }
                reparentToMagicWindow(primaryStack, pkgName);
                result.putBoolean("RESULT_SPLITE_SCREEN", true);
            }
        }
    }

    private boolean isNeedShowFreeFormAnimation(ActivityStackEx stack, ActivityRecordEx topActivity, boolean isHwFreeFormStack, int orientation) {
        HwMagicContainer container = this.mMwManager.getContainer(topActivity);
        if (container == null || !container.isInMagicWinOrientation()) {
            return false;
        }
        ArrayList<ActivityRecordEx> tempActivityList = this.mPolicy.getAllActivities(stack);
        if (!isHwFreeFormStack) {
            return false;
        }
        if (tempActivityList.size() > 1 || this.mPolicy.isMainActivity(container, topActivity)) {
            return true;
        }
        return false;
    }

    public void processHwMultiStack(int stackId, int orientation, Bundle result) {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null && container.isInMagicWinOrientation()) {
            ActivityStackEx stack = this.mActivityTaskManager.getRootActivityContainer().getDefaultDisplay().getStackEx(stackId);
            if (stack == null) {
                SlogEx.w(TAG, "HwMultiStack is a null object");
            } else if (!this.mPolicy.mMagicWinSplitMng.isNeedProcessCombineStack(stack, true)) {
                ActivityRecordEx topAr = stack.getTopActivity();
                if (!container.isFoldableDevice() || orientation != 2 || topAr == null || !topAr.isFullScreenVideoInLandscape()) {
                    String pkgName = Utils.getRealPkgName(stack.getTopActivity());
                    if (container.getHwMagicWinEnabled(pkgName) && !isStayInOtherWinMode(topAr)) {
                        boolean isHwFreeFormStack = stack.inHwFreeFormWindowingMode();
                        ActivityRecordEx topActivity = stack.getTopActivity();
                        if (isNeedShowFreeFormAnimation(stack, topActivity, isHwFreeFormStack, orientation)) {
                            this.mMwManager.getWmsPolicy().startExitSplitAnimation(topActivity.getAppToken(), (float) this.mActivityTaskManager.getUiContext().getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("hw_multiwindow_freeform_corner_radius")));
                        }
                        this.mMwManager.getUIController().updateSplitBarVisibility(true, container.getDisplayId());
                        if (!container.isFoldableDevice() || !container.isSupportOpenMode(pkgName) || isDragFullModeStack(stack)) {
                            stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
                            stack.resize((Rect) null, (Rect) null, (Rect) null);
                        }
                        setMagicActivityBound(stack, pkgName, null);
                        if (isHwFreeFormStack && topActivity != null && topActivity.inHwMagicWindowingMode()) {
                            this.mPolicy.checkBackgroundForMagicWindow(topActivity);
                        }
                        result.putBoolean("RESULT_HWMULTISTACK", true);
                        showGuideDialog(container, stack, pkgName);
                        if (isHwFreeFormStack) {
                            this.mMwManager.getWmsPolicy().setMoveAnimFromHwFreeform(pkgName);
                            return;
                        }
                        return;
                    }
                    return;
                }
                SlogEx.w(TAG, "processHwMultiStack original window is in FullScreenVideoInLandscape");
                if (container.getHwMagicWinEnabled(Utils.getRealPkgName(topAr))) {
                    this.mStackIdsLastInMagicWindow.remove(Integer.valueOf(stackId));
                    this.mStackIdsMoveToMagicWindow.add(Integer.valueOf(stackId));
                }
            }
        }
    }

    private void showGuideDialog(HwMagicContainer container, ActivityStackEx stack, String pkgName) {
        if (stack.equalsStack(this.mPolicy.getFocusedTopStack(container))) {
            this.mMwManager.getUIController().whetherShowDialog(pkgName);
        }
    }

    public void moveMwToHwMultiStack(int stackId, Rect bounds, Bundle result) {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null) {
            ActivityStackEx stack = this.mActivityTaskManager.getRootActivityContainer().getDefaultDisplay().getStackEx(stackId);
            if (stack == null || bounds == null || bounds.isEmpty()) {
                SlogEx.w(TAG, "moveMwToHwMultiStack exception return");
                return;
            }
            ActivityRecordEx topAr = stack.getTopActivity();
            if (topAr != null) {
                String pkgName = Utils.getRealPkgName(topAr);
                if (this.mPolicy.isSupportMainRelatedMode(container, topAr)) {
                    this.mPolicy.removeRelatedActivity(container, stack, false);
                }
                int stackPos = container.getBoundsPosition(stack.getRequestedOverrideBounds());
                if (stackPos == 1 || stackPos == 2) {
                    this.mPolicy.mMagicWinSplitMng.quitMagicSplitScreenMode(pkgName, stackId, true, topAr.getUserId(), this.mMwManager.getContainer(topAr));
                }
                this.mPolicy.forceFinishInvisibleActivityInFullMode(topAr);
                if (container.isFoldableDevice()) {
                    moveLatestActivityToTop(false, false);
                }
                stack.resize(bounds, (Rect) null, (Rect) null);
                setActivityBoundForStack(stack, bounds);
            }
        }
    }

    private void reparentToMagicWindow(ActivityStackEx sourceStack, String pkgName) {
        ActivityDisplayEx display = this.mActivityTaskManager.getRootActivityContainer().getDefaultDisplay();
        ArrayList<TaskRecordEx> tasks = sourceStack.getAllTaskRecordExs();
        if (!tasks.isEmpty()) {
            ActivityOptionsEx tmpOptions = ActivityOptionsEx.makeBasic();
            tmpOptions.setLaunchWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
            int size = tasks.size();
            int i = 0;
            while (i < size) {
                TaskRecordEx task = tasks.get(i);
                ActivityStackEx toStack = display.getOrCreateStackEx((ActivityRecordEx) null, tmpOptions.getActivityOptions(), task, task.getActivityType(), true);
                boolean isTopTask = i == size + -1;
                if (toStack != null) {
                    task.reparent(toStack, true, TaskRecordEx.REPARENT_MOVE_STACK_TO_FRONT, isTopTask, true, true, "moveTasksToMagicWindowStack - onTop");
                    toStack.resize((Rect) null, (Rect) null, (Rect) null);
                    setMagicActivityBound(toStack, pkgName, null);
                }
                i++;
            }
        }
    }

    private void moveActivityToMagicWindow(ActivityDisplayEx display, IBinder token, boolean isFromSplit) {
        HwMagicContainer container = this.mMwManager.getContainerByDisplayId(display.getDisplayId());
        if (!(container == null || container.isInFoldedStatus())) {
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStackEx stack = display.getChildAt(stackNdx);
                if (!(stack.topTask() == null || stack.topTask().getTopActivity() == null || stack.getActivityType() == 2)) {
                    if (isFromSplit) {
                        moveActivityToMagicWindowForSplit(stack);
                    } else {
                        moveActivityToMagicWindowInner(stack, token);
                    }
                }
            }
            if (display.getTopStackEx() != null && display.getTopStackEx().inHwMagicWindowingMode()) {
                String pkg = Utils.getPackageName(display);
                TaskRecordEx task = token != null ? ActivityRecordEx.forToken(token).getTaskRecordEx() : display.getTopStackEx().topTask();
                HwMagicWinStatistics.getInstance(container.getType()).startTick(container.getConfig(), pkg, this.mMwManager.isDragFullMode(task.getTopActivity()) ? task.getDragFullMode() : -1, "move_to_magic");
            }
        }
    }

    private void moveActivityToMagicWindowForSplit(ActivityStackEx stack) {
        String pkgName = Utils.getRealPkgName(stack.topTask().getTopActivity());
        HwMagicContainer container = this.mMwManager.getContainer(stack.topTask().getTopActivity());
        if (container != null && stack.inSplitScreenSecondaryWindowingMode() && container.getHwMagicWinEnabled(pkgName)) {
            stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, false, false, false, true, false);
            stack.resize((Rect) null, (Rect) null, (Rect) null);
            setMagicActivityBound(stack, pkgName, null);
        }
    }

    public void moveAppToMagicWinWhenFinishingFullscreen(ActivityRecordEx finishActivity) {
        SlogEx.i(TAG, "moveAppToMagicWinWhenFinishingFullscreen");
        if (finishActivity == null || finishActivity.getTaskRecordEx().getStack() == null) {
            SlogEx.i(TAG, "finish activity or stack is null");
            return;
        }
        ActivityStackEx stack = finishActivity.getTaskRecordEx().getStack();
        String pkgName = Utils.getRealPkgName(finishActivity);
        stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, false, false, false, true, false);
        ArrayList<ActivityRecordEx> tempActivityList = this.mPolicy.getAllActivities(stack);
        ActivityRecordEx.remove(tempActivityList, finishActivity);
        this.mPolicy.getMode(finishActivity).setActivityBoundByMode(tempActivityList, pkgName, null);
    }

    /* access modifiers changed from: package-private */
    public void setMagicActivityBound(ActivityStackEx stack, String pkgName, HwMagicContainer toContainer) {
        ArrayList<ActivityRecordEx> tempActivityList = this.mPolicy.getAllActivities(stack);
        if (tempActivityList.size() < 1) {
            SlogEx.d(TAG, "there is not any activity in the list, return");
            return;
        }
        ActivityRecordEx bottomActivity = tempActivityList.get(0);
        if (HwMagicWinAmsPolicy.PERMISSION_ACTIVITY.equals(Utils.getClassName(bottomActivity))) {
            bottomActivity.setBounds((Rect) null);
            ActivityRecordEx.remove(tempActivityList, bottomActivity);
        }
        if (tempActivityList.size() == 1) {
            HwMagicContainer container = toContainer != null ? toContainer : this.mMwManager.getContainer(bottomActivity);
            if (container != null && container.isFoldableDevice() && !this.mMwManager.isDragFullMode(bottomActivity)) {
                stack.setWindowingMode(1);
            }
            this.mPolicy.getBaseMode().setActivityBoundByMode(tempActivityList, pkgName, toContainer);
            return;
        }
        this.mPolicy.getMode(bottomActivity).setActivityBoundByMode(tempActivityList, pkgName, toContainer);
    }

    private void moveActivityToMagicWindowInner(ActivityStackEx stack, IBinder token) {
        ActivityRecordEx topActivity = stack.getTopActivity();
        HwMagicContainer container = this.mMwManager.getContainer(topActivity);
        if (container != null) {
            if (!container.isFoldableDevice() || this.mStackIdsLastInMagicWindow.contains(Integer.valueOf(stack.getStackId())) || this.mStackIdsMoveToMagicWindow.contains(Integer.valueOf(stack.getStackId()))) {
                boolean needMove = false;
                boolean isAppRequest = topActivity.equalsActivityRecord(ActivityRecordEx.forToken(token)) && topActivity.isFullScreenVideoInLandscape();
                if (topActivity.getInfo() != null) {
                    isAppRequest |= ActivityInfoEx.isFixedOrientationLandscape(topActivity.getInfo().screenOrientation);
                }
                if (stack.getWindowingMode() == 1 && !isAppRequest) {
                    needMove = true;
                }
                String pkgName = Utils.getRealPkgName(topActivity);
                if (container.getHwMagicWinEnabled(pkgName) && needMove) {
                    if (!container.isFoldableDevice() || !this.mStackIdsLastInMagicWindow.contains(Integer.valueOf(stack.getStackId()))) {
                        if (!container.isFoldableDevice() || !container.isSupportOpenMode(pkgName)) {
                            stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, false, false, false, true, false);
                        }
                        setMagicActivityBound(stack, pkgName, null);
                        return;
                    }
                    stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, false, false, false, true, false);
                    setActivityBoundAsLast(stack);
                }
            }
        }
    }

    private void setActivityBoundAsLast(ActivityStackEx stack) {
        ArrayList<ActivityRecordEx> tempActivityList = this.mPolicy.getAllActivities(stack);
        if (tempActivityList.size() < 1) {
            SlogEx.d(TAG, "there is not any activity in the list, return");
            return;
        }
        ActivityRecordEx bottomActivity = tempActivityList.get(0);
        if (HwMagicWinAmsPolicy.PERMISSION_ACTIVITY.equals(Utils.getClassName(bottomActivity))) {
            bottomActivity.setBounds((Rect) null);
            ActivityRecordEx.remove(tempActivityList, bottomActivity);
        }
        Iterator<ActivityRecordEx> it = tempActivityList.iterator();
        while (it.hasNext()) {
            ActivityRecordEx ar = it.next();
            if (ar.getLastBound() != null) {
                ar.setBounds(ar.getLastBound());
            }
        }
    }

    public void updateMagicWindowConfiguration(int oldOrientation, int newOrientation, IBinder token) {
        if (!(oldOrientation == newOrientation || oldOrientation == 0 || newOrientation == 0)) {
            updateMagicWindowConfigurationInner(newOrientation, token);
        }
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null) {
            container.getCameraRotation().updateCameraRotation(0);
        }
    }

    private void cleanRelatedActivityIfExist(ActivityDisplayEx display) {
        for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStackEx stack = display.getChildAt(stackNdx);
            HwMagicContainer container = this.mMwManager.getContainer(stack.getTopActivity());
            if (this.mPolicy.isSupportMainRelatedMode(container, stack.getTopActivity())) {
                this.mPolicy.removeRelatedActivity(container, stack, false);
            }
        }
    }

    private void updateMagicWindowConfigurationInner(int newOrientation, IBinder token) {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null) {
            SlogEx.i(TAG, "### Execute -> updateMagicWindowConfiguration");
            ActivityDisplayEx display = this.mActivityTaskManager.getRootActivityContainer().getDefaultDisplay();
            if (newOrientation == 1) {
                if (container.isFoldableDevice()) {
                    moveActivityToMagicWindow(display, token, false);
                    return;
                }
                cleanRelatedActivityIfExist(display);
                if (display.getFocusedStackEx() != null) {
                    setParamsForRotation(container);
                    moveActivityToFullScreen(display);
                    this.mMwManager.getUIController().dismissDialog();
                    this.mMwManager.getUIController().hideMwWallpaperInNeed(container.getDisplayId());
                } else {
                    return;
                }
            }
            if (newOrientation != 2) {
                return;
            }
            if (container.isFoldableDevice()) {
                cleanRelatedActivityIfExist(display);
                moveActivityToFullScreen(display);
                return;
            }
            ActivityRecordEx activityRecord = this.mPolicy.getTopActivity(container);
            if (!isStayInOtherWinMode(activityRecord)) {
                if (container.isPadDevice() && activityRecord != null && this.mPolicy.isMainActivity(container, activityRecord)) {
                    this.mPolicy.startRelateActivityIfNeed(activityRecord, false);
                }
                moveSystemActivityToNewTask(display, token);
                moveActivityToMagicWindow(display, token, false);
                if (this.mPolicy.isShowDragBar(activityRecord)) {
                    this.mMwManager.getUIController().updateSplitBarVisibility(true, container.getDisplayId());
                }
                if (display.getFocusedStackEx() != null) {
                    setParamsForRotation(container);
                    if (display.getTopStackEx() != null && display.getTopStackEx().inHwMagicWindowingMode()) {
                        this.mMwManager.getUIController().whetherShowDialog(Utils.getPackageName(display));
                    }
                }
            }
        }
    }

    private void setParamsForRotation(HwMagicContainer container) {
        ActivityRecordEx top = this.mPolicy.getTopActivity(container);
        if (top != null) {
            container.getAnimation().setParamsForRotation(top.getBounds(), top.getWindowingMode());
        }
    }

    private boolean isStayInOtherWinMode(ActivityRecordEx activityRecord) {
        return this.mPolicy.isPkgInLogoffStatus(activityRecord);
    }

    private void moveActivityToFullScreen(ActivityDisplayEx display) {
        this.mStackIdsLastInMagicWindow.clear();
        this.mStackIdsMoveToMagicWindow.clear();
        for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStackEx stack = display.getChildAt(stackNdx);
            if (stack.inHwMagicWindowingMode()) {
                this.mPolicy.finishInvisibleActivityInFullMode(stack.getTopActivity());
                this.mStackIdsLastInMagicWindow.add(Integer.valueOf(stack.getStackId()));
                setActivityBoundForStack(stack, null);
                if (this.mPolicy.mMagicWinSplitMng.isSpliteModeStack(stack)) {
                    HwMagicWinCombineManagerImpl.getInstance().clearSplitScreenList(Utils.getPackageName(stack.getTopActivity()), this.mPolicy.getStackUserId(stack));
                }
                stack.setWindowingMode(1, false, false, false, true, false);
            }
        }
        if (!(display.getTopStackEx() == null || display.getTopStackEx().inHwMagicWindowingMode())) {
            Optional.ofNullable(this.mMwManager.getContainerByDisplayId(display.getDisplayId())).ifPresent($$Lambda$HwMagicModeSwitcher$04CxX6rBzYUWBOIr4yFVy3KoCn8.INSTANCE);
        }
    }

    private void moveSystemActivityToNewTask(ActivityDisplayEx display, IBinder token) {
        ActivityRecordEx top;
        HwMagicContainer container;
        ActivityStackEx topStack = display.getTopStackEx();
        if (topStack != null && topStack.topTask() != null && topStack.topTask().getTopActivity() != null && (container = this.mMwManager.getContainer((top = topStack.topTask().getTopActivity()))) != null) {
            String realName = Utils.getRealPkgName(top);
            String pkgName = Utils.getPackageName(top);
            if (!container.getHwMagicWinEnabled(realName)) {
                return;
            }
            if (CAMERA_PACKAGE.equals(pkgName) || (INSTALLER_PACKAGE.equals(pkgName) && !HwMagicWinAmsPolicy.PERMISSION_ACTIVITY.equals(Utils.getClassName(top)))) {
                TaskRecordEx newTask = topStack.createTaskRecordEx(this.mActivityTaskManager.getNextTaskIdForUserLockedFromStackSupervisor(top.getUserId()), top.getInfo(), top.getIntent(), true);
                top.reparent(newTask, 0, "magicwin app launch camera portraitly,move camera activity to new task");
                ActivityOptionsEx tmpOptions = ActivityOptionsEx.makeBasic();
                tmpOptions.setLaunchWindowingMode(1);
                newTask.reparent(display.getOrCreateStackEx((ActivityRecordEx) null, tmpOptions.getActivityOptions(), newTask, newTask.getActivityType(), true), true, TaskRecordEx.REPARENT_MOVE_STACK_TO_FRONT, true, true, true, "moveCamera - onTop");
            }
        }
    }

    public void moveLatestActivityToTop(boolean isQuitMagicWindow, boolean isNeedClearBounds) {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null) {
            ActivityDisplayEx display = this.mActivityTaskManager.getRootActivityContainer().getDefaultDisplay();
            synchronized (this.mActivityTaskManager.getGlobalLock()) {
                if (isQuitMagicWindow) {
                    this.mPolicy.mMagicWinSplitMng.updateSpliteStackSequence(display);
                }
                this.mPolicy.finishActivitiesAfterTopActivity(container);
                ArrayList<ActivityStackEx> needProcessStacks = new ArrayList<>();
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStackEx stack = display.getChildAt(stackNdx);
                    String pkgName = Utils.getRealPkgName(stack.getTopActivity());
                    if (container.getHwMagicWinEnabled(pkgName) && (stack.inHwMagicWindowingMode() || stack.inSplitScreenPrimaryWindowingMode() || stack.inHwSplitScreenWindowingMode())) {
                        this.mPolicy.finishInvisibleActivityInFullMode(stack.getTopActivity());
                        ActivityRecordEx.sortActivitiesInStack(stack);
                        if (isQuitMagicWindow && this.mPolicy.mMagicWinSplitMng.isNeedNewTaskStack(stack) && stack.inHwMagicWindowingMode()) {
                            needProcessStacks.add(stack);
                        }
                        ActivityRecordEx topActivity = stack.getTopActivity();
                        ActivityRecordEx masterTop = this.mPolicy.getActivityByPosition(topActivity, 1, 0);
                        if (masterTop != null && masterTop.inHwMagicWindowingMode() && !this.mPolicy.mMagicWinSplitMng.isSpliteModeStack(stack)) {
                            this.mPolicy.setMagicWindowToPause(masterTop);
                        }
                        resetStackMode(stack, getLatestActivity(isQuitMagicWindow, isNeedClearBounds, stack, pkgName, topActivity), isQuitMagicWindow, isNeedClearBounds, container);
                    }
                }
                Iterator<ActivityStackEx> it = needProcessStacks.iterator();
                while (it.hasNext()) {
                    this.mPolicy.mMagicWinSplitMng.isNeedProcessCombineStack(it.next(), false);
                }
            }
        }
    }

    private ActivityRecordEx getLatestActivity(boolean isQuitMagicWindow, boolean isNeedClearBounds, ActivityStackEx stack, String pkgName, ActivityRecordEx topActivity) {
        HwMagicContainer container = this.mMwManager.getContainer(topActivity);
        ActivityRecordEx latestActivity = this.mPolicy.getActivityByPosition(topActivity, 2, 0);
        if (this.mMwManager.isMiddle(topActivity) || isDragFullModeStack(topActivity.getActivityStackEx())) {
            latestActivity = topActivity;
        }
        if (this.mPolicy.isSupportMainRelatedMode(container, topActivity)) {
            if (this.mPolicy.isRelatedActivity(container, latestActivity)) {
                latestActivity = this.mPolicy.getActivityByPosition(topActivity, 1, 0);
            } else if (container.getConfig().isSupportAppTaskSplitScreen(pkgName)) {
                latestActivity = this.mPolicy.mMagicWinSplitMng.getLatestActivityBySplitMode(pkgName, stack, topActivity, latestActivity);
            } else {
                SlogEx.i(TAG, "else the latestActivity is the right top");
            }
            if (isQuitMagicWindow || isNeedClearBounds) {
                this.mPolicy.removeRelatedActivity(container, stack, false);
            }
        }
        return latestActivity;
    }

    private void resetStackMode(ActivityStackEx stack, ActivityRecordEx latestActivity, boolean isQuitMagicWindow, boolean isNeedClearBounds, HwMagicContainer container) {
        if (latestActivity != null) {
            SlogEx.i(TAG, "latest activity is:" + latestActivity);
            stack.getTopActivity().getTaskRecordEx().moveActivityToFrontLocked(latestActivity);
            this.mActivityTaskManager.getRootActivityContainer().resumeFocusedStacksTopActivities();
            this.mPolicy.checkResumeStateForMagicWindow(latestActivity);
            if (isQuitMagicWindow) {
                stack.setWindowingMode(1, false, false, false, true, false);
                setActivityBoundForStack(stack, null);
                if (!container.getConfig().isSupportAppTaskSplitScreen(Utils.getRealPkgName(latestActivity))) {
                    latestActivity.setForceNewConfig((latestActivity.getInfo().configChanges & 3328) != 3328);
                    latestActivity.ensureActivityConfiguration(0, false);
                }
            }
        } else if (isQuitMagicWindow && stack.inHwMagicWindowingMode()) {
            stack.setWindowingMode(1, false, false, false, true, false);
            setActivityBoundForStack(stack, null);
        }
        if (isNeedClearBounds) {
            setActivityBoundForStack(stack, null);
        }
    }

    private void setActivityBoundForStack(ActivityStackEx stack, Rect bounds) {
        ArrayList<ActivityRecordEx> tempActivityList;
        HwMagicContainer container = this.mMwManager.getContainer(stack.getTopActivity());
        if (container != null) {
            if (container.isFoldableDevice()) {
                tempActivityList = this.mPolicy.getAllActivities(stack);
            } else {
                tempActivityList = getActivitiesFocusToRight(stack, container);
            }
            Iterator<ActivityRecordEx> it = tempActivityList.iterator();
            while (it.hasNext()) {
                ActivityRecordEx ar = it.next();
                boolean isSetLastBound = container.isFoldableDevice() || !container.getConfig().isSupportAppTaskSplitScreen(Utils.getPackageName(ar));
                if (bounds == null && isSetLastBound) {
                    ar.setLastBound(ar.getRequestedOverrideBounds());
                }
                ar.setBounds(bounds);
                ar.onRequestedOverrideConfigurationChanged(ConfigurationAdapter.EMPTY);
            }
        }
    }

    private ArrayList<ActivityRecordEx> getActivitiesFocusToRight(ActivityStackEx stack, HwMagicContainer container) {
        ArrayList<ActivityRecordEx> tempActivityList = this.mPolicy.getAllActivities(stack);
        ActivityRecordEx topActivity = stack.getTopActivity();
        if (topActivity != null) {
            ActivityRecordEx masterTop = this.mPolicy.getActivityByPosition(topActivity, 1, 0);
            if (masterTop != null && !container.getConfig().isSupportAppTaskSplitScreen(Utils.getPackageName(masterTop))) {
                this.mPolicy.setMagicWindowToPause(masterTop);
            }
            int topActivityPosition = container.getBoundsPosition(topActivity.getRequestedOverrideBounds());
            ActivityRecordEx bottomActivity = null;
            if (!tempActivityList.isEmpty()) {
                bottomActivity = tempActivityList.get(tempActivityList.size() - 1);
            }
            ActivityRecordEx slaveTop = this.mPolicy.getActivityByPosition(topActivity, 2, 0);
            if (!(masterTop == null || slaveTop == null || (ActivityRecordEx.indexOf(tempActivityList, masterTop) >= ActivityRecordEx.indexOf(tempActivityList, slaveTop) && !this.mMwManager.isSlave(bottomActivity)))) {
                adjustActivitiesOrder(masterTop, tempActivityList);
                if (topActivityPosition == 5) {
                    return tempActivityList;
                }
                this.mActivityTaskManager.getRootActivityContainer().resumeFocusedStacksTopActivities();
                if (!slaveTop.equalsActivityRecord(slaveTop.getActivityStackEx().getResumedActivity()) && slaveTop.isState(ActivityStackEx.ActivityState.RESUMED)) {
                    slaveTop.getActivityStackEx().onActivityStateChanged(slaveTop, ActivityStackEx.ActivityState.RESUMED, "moveActivityToFullScreen");
                }
            }
        }
        return tempActivityList;
    }

    public void adjustActivitiesOrder(ActivityRecordEx left, ArrayList<ActivityRecordEx> activities) {
        if (left != null) {
            synchronized (this.mActivityTaskManager.getGlobalLock()) {
                int leftIndex = ActivityRecordEx.indexOf(left.getTaskRecordEx().getActivityRecordExs(), left);
                int rightIndex = -1;
                ActivityRecordEx right = null;
                Iterator<ActivityRecordEx> it = activities.iterator();
                while (it.hasNext()) {
                    ActivityRecordEx ar = it.next();
                    if (this.mMwManager.isSlave(ar) && ar.getCreateTime() > left.getCreateTime()) {
                        rightIndex = ActivityRecordEx.indexOf(left.getTaskRecordEx().getActivityRecordExs(), ar);
                        right = ar;
                    }
                }
                if (rightIndex >= 0 && leftIndex > rightIndex) {
                    moveActivityToIndex(left, right, rightIndex);
                }
            }
        }
    }

    private void moveActivityToIndex(ActivityRecordEx activity, ActivityRecordEx right, int index) {
        TaskRecordEx taskRecordEx = activity.getTaskRecordEx();
        taskRecordEx.removeActivity(activity);
        taskRecordEx.addActivityAtIndex(index, activity);
        taskRecordEx.getTaskEx().positionChildAt(activity.getAppWindowTokenEx(), index);
        taskRecordEx.updateEffectiveIntent();
        taskRecordEx.setFrontOfTask();
        ActivityStackEx targetStack = activity.getActivityStackEx();
        int lruRightIndex = ActivityRecordEx.indexOf(targetStack.getLRUActivities(), right);
        if (lruRightIndex >= 0 && ActivityRecordEx.indexOf(targetStack.getLRUActivities(), activity) > lruRightIndex) {
            activity.removeFromLRUList();
            activity.addToLRUList(lruRightIndex);
        }
    }

    private void updateActivityModeAndBounds(ActivityRecordEx activityRecord, Rect bounds, int windowMode) {
        ActivityStackEx activityStack = activityRecord.getActivityStackEx();
        if (!(activityStack == null || activityStack.getWindowingMode() == windowMode)) {
            activityStack.setWindowingMode(windowMode, false, false, false, true, false);
        }
        if (!activityRecord.isFinishing()) {
            activityRecord.setWindowingMode(windowMode);
            activityRecord.setBounds(bounds);
        }
    }

    public void updateActivityToFullScreenConfiguration(ActivityRecordEx activityRecord) {
        HwMagicContainer container = this.mMwManager.getContainer(activityRecord);
        if (container != null && !this.mPolicy.isMainActivity(container, activityRecord)) {
            synchronized (this.mActivityTaskManager.getGlobalLock()) {
                if (activityRecord != null) {
                    updateActivityModeAndBounds(activityRecord, container.getBounds(5, false), 1);
                    activityRecord.setForceNewConfig(container.isReLaunchWhenResize(Utils.getPackageName(activityRecord)));
                    activityRecord.ensureActivityConfiguration(0, true);
                }
            }
        }
    }

    public void moveToMagicWinFromFullscreenForTah(ActivityRecordEx focus, ActivityRecordEx next) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (focus != null && next != null && container != null && this.mPolicy.getMode(focus).shouldEnterMagicWinForTah(focus, next)) {
            container.getAnimation().setAnimationNull();
            ActivityRecordEx origActivity = this.mPolicy.getMode(focus).getOrigActivityRecordEx();
            if (origActivity != null && this.mPolicy.isSpecTransActivity(container, focus)) {
                focus = origActivity;
            }
            focus.setIsFromFullscreenToMagicWin(true);
            updateActivityModeAndBounds(focus, container.getBounds(1, Utils.getPackageName(focus)), HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
            focus.setForceNewConfig(container.isReLaunchWhenResize(Utils.getPackageName(focus)));
            focus.ensureActivityConfiguration(0, true);
            updateActivityModeAndBounds(next, container.getBounds(2, Utils.getPackageName(next)), HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
            next.setLastActivityHash(System.identityHashCode(focus.getShadow()));
        }
    }

    public void moveToMagicWinFromFullscreenForMain(ActivityRecordEx focus, ActivityRecordEx next) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (focus != null && next != null && container != null && !this.mPolicy.isPkgInLogoffStatus(next)) {
            updateActivityModeAndBounds(next, container.getBounds(1, Utils.getPackageName(next)), HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
            next.setLastActivityHash(System.identityHashCode(focus.getShadow()));
            next.setForceNewConfig(container.isReLaunchWhenResize(Utils.getPackageName(next)));
            next.ensureActivityConfiguration(0, true);
        }
    }

    private boolean isDragFullModeStack(ActivityStackEx stackEx) {
        TaskRecordEx topTr;
        if (stackEx == null || (topTr = stackEx.topTask()) == null || !this.mMwManager.isDragFullMode(topTr.getDragFullMode())) {
            return false;
        }
        return true;
    }

    public void backToFoldFullDisplay() {
        String packageName;
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null) {
            ActivityDisplayEx display = this.mActivityTaskManager.getRootActivityContainer().getDefaultDisplay();
            synchronized (this.mActivityTaskManager.getGlobalLock()) {
                int i = 1;
                boolean isLand = display.getConfiguration().orientation == 2;
                int stackNdx = display.getChildCount() - 1;
                while (stackNdx >= 0) {
                    ActivityStackEx stack = display.getChildAt(stackNdx);
                    String packageName2 = Utils.getRealPkgName(stack.getTopActivity());
                    if (container.getHwMagicWinEnabled(packageName2)) {
                        if (stack.getWindowingMode() == i) {
                            if (isLand && stack.isFocusedStackOnDisplay()) {
                                this.mStackIdsMoveToMagicWindow.add(Integer.valueOf(stack.getStackId()));
                            } else if (!this.mPolicy.isPkgInLogoffStatus(stack.getTopActivity())) {
                                if (container.isSupportOpenMode(packageName2)) {
                                    if (!isDragFullModeStack(stack)) {
                                        packageName = packageName2;
                                        setMagicActivityBound(stack, packageName, null);
                                    }
                                }
                                packageName = packageName2;
                                stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, false, false, false, true, false);
                                setMagicActivityBound(stack, packageName, null);
                            }
                        }
                    }
                    stackNdx--;
                    i = 1;
                }
                ActivityStackEx activityStack = this.mPolicy.getFocusedTopStack(container);
                if (activityStack != null && activityStack.inHwMagicWindowingMode()) {
                    activityStack.ensureActivitiesVisibleLocked((ActivityRecordEx) null, 0, false);
                    if (this.mMwManager.getAmsPolicy().isShowDragBar(container)) {
                        this.mMwManager.getUIController().updateSplitBarVisibility(true, container.getDisplayId());
                    }
                }
            }
        }
    }

    private void sortActivities(ActivityStackEx stack) {
        for (int taskNdx = stack.getTaskHistory().size() - 1; taskNdx >= 0; taskNdx--) {
            Collections.sort(((TaskRecordEx) stack.getTaskHistory().get(taskNdx)).getActivityRecordExs(), this.mComputor);
        }
    }

    public void clearOverrideBounds(int taskId) {
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            TaskRecordEx task = this.mActivityTaskManager.getRootActivityContainer().anyTaskForId(taskId, RootActivityContainerEx.MATCH_TASK_IN_STACKS_ONLY);
            if (!(task == null || task.getStack() == null || !task.getStack().inHwMagicWindowingMode())) {
                ActivityRecordEx top = task.getStack().getTopActivity();
                HwMagicContainer container = this.mMwManager.getContainer(top);
                if (this.mPolicy.isSupportMainRelatedMode(container, top)) {
                    this.mPolicy.removeRelatedActivity(container, task.getStack(), false);
                }
                setActivityBoundForStack(task.getStack(), null);
            }
        }
    }

    public void clearOverrideBounds(ActivityRecordEx ar) {
        if (ar != null && ar.inHwMagicWindowingMode()) {
            ar.setBounds((Rect) null);
            ar.onRequestedOverrideConfigurationChanged(ConfigurationAdapter.EMPTY);
        }
    }

    public void changeLayoutDirection(boolean isRtl) {
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null) {
            ActivityDisplayEx display = this.mActivityTaskManager.getRootActivityContainer().getDefaultDisplay();
            synchronized (this.mActivityTaskManager.getGlobalLock()) {
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStackEx stack = display.getChildAt(stackNdx);
                    if (stack.inHwMagicWindowingMode()) {
                        String packageName = Utils.getRealPkgName(stack.getTopActivity());
                        if (container.getConfig().isSupportAppTaskSplitScreen(packageName)) {
                            this.mPolicy.forceStopPackage(packageName);
                        } else {
                            Iterator<ActivityRecordEx> it = this.mPolicy.getAllActivities(stack).iterator();
                            while (it.hasNext()) {
                                ActivityRecordEx ar = it.next();
                                if (this.mMwManager.isMaster(ar)) {
                                    this.mPolicy.setWindowBoundsLocked(ar, container.getBounds(2, Utils.getRealPkgName(ar)));
                                } else if (this.mMwManager.isSlave(ar)) {
                                    this.mPolicy.setWindowBoundsLocked(ar, container.getBounds(1, Utils.getRealPkgName(ar)));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private ActivityDisplayEx getVirtualDisplay() {
        return this.mActivityTaskManager.getActivityDisplayEx(this.mMwManager.getVirtualDisplayId());
    }

    public void moveFocusToMagicWinInVirtualContainer() {
        HwMagicContainer container = this.mMwManager.getContainer(1);
        if (container == null) {
            SlogEx.i(TAG, "the device do not support virtual magic window");
            return;
        }
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            ActivityDisplayEx display = getVirtualDisplay();
            if (display == null) {
                SlogEx.w(TAG, "display is null, do nothing");
                return;
            }
            ActivityStackEx stack = display.getFocusedStackEx();
            if (stack != null) {
                if (!stack.isActivityStackNull()) {
                    ActivityRecordEx top = stack.getTopActivity();
                    String packageName = Utils.getRealPkgName(top);
                    int orientation = -1;
                    if (container.getHwMagicWinEnabled(packageName)) {
                        if (!(top == null || top.getAppWindowTokenEx() == null)) {
                            orientation = top.getAppWindowTokenEx().getOrientation();
                        }
                        SlogEx.e(TAG, "enter magicwindow stack=" + stack + ",top= " + top + " oritation " + orientation);
                        if (!ActivityInfoEx.isFixedOrientationLandscape(orientation)) {
                            stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, false, false, false, true, false);
                            setMagicActivityBound(stack, packageName, null);
                            stack.ensureActivitiesVisibleLocked((ActivityRecordEx) null, 0, false);
                            TaskRecordEx task = display.getTopStackEx().topTask();
                            HwMagicWinStatistics.getInstance(1).startTick(container.getConfig(), packageName, this.mMwManager.isDragFullMode(task.getTopActivity()) ? task.getDragFullMode() : -1, "enter_virtual_fullscreen");
                        }
                    } else {
                        if (stack.inHwMagicWindowingMode()) {
                            setActivityBoundForStack(stack, null);
                            stack.setWindowingMode(1, false, false, false, true, false);
                        }
                        SlogEx.i(TAG, "moveFocusToMagicWin pkg do not support mw, packageName=" + packageName);
                    }
                    return;
                }
            }
            SlogEx.w(TAG, "focus stack is null on virtual display");
        }
    }

    public void updateStackForDisplay(int fromStackId, int toDisplayId, int toStackId) {
        ActivityStackEx fromStack = this.mPolicy.getActivityStackEx(fromStackId);
        if (fromStack != null && toDisplayId != -1) {
            if (fromStack.inHwMagicWindowingMode() && this.mMwManager.getContainer(fromStack.getTopActivity()) != null) {
                getActivitiesFocusToRight(fromStack, this.mMwManager.getContainer(fromStack.getTopActivity()));
            }
            String pkgName = Utils.getRealPkgName(fromStack.getTopActivity());
            HwMagicContainer toContainer = this.mMwManager.getContainerByDisplayId(toDisplayId);
            if (toContainer != null && toContainer.isVirtualContainer() && this.mMwManager.getVirtualDisplayId() == -1) {
                return;
            }
            if (toContainer != null && toContainer.getHwMagicWinEnabled(pkgName) && !toContainer.isInFoldedStatus()) {
                ActivityStackEx toStack = this.mPolicy.getActivityStackEx(toStackId);
                if (toStack != null && !toStack.inHwMagicWindowingMode()) {
                    toStack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, false, false, false, true, false);
                }
                setMagicActivityBound(fromStack, pkgName, toContainer);
                if (toContainer.isLocalContainer()) {
                    Iterator<ActivityRecordEx> it = this.mPolicy.getAllActivities(fromStack).iterator();
                    while (it.hasNext()) {
                        this.mMwManager.getAmsPolicy().resize(it.next());
                    }
                }
                fromStack.ensureActivitiesVisibleLocked((ActivityRecordEx) null, 0, false);
            } else if (fromStack.inHwMagicWindowingMode()) {
                this.mPolicy.removeRelatedActivity(this.mMwManager.getContainer(fromStack.getTopActivity()), fromStack, true);
                setActivityBoundForStack(fromStack, null);
                if (toStackId == fromStackId) {
                    fromStack.setWindowingMode(1, false, false, false, true, false);
                }
            }
        }
    }
}
