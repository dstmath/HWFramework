package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.pm.ApplicationInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.util.HwMwUtils;
import com.android.server.am.ActivityManagerServiceEx;
import com.huawei.android.app.ActivityOptionsEx;
import com.huawei.android.app.WindowConfigurationEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.utils.Utils;
import java.util.Set;

public class HwMagicActivityStarter {
    private static final int PARAM_INDEX_FOUR = 4;
    private static final int PARAM_INDEX_ONE = 1;
    private static final int PARAM_INDEX_THREE = 3;
    private static final int PARAM_INDEX_TWO = 2;
    private static final int PARAM_INDEX_ZERO = 0;
    private static final int PARAM_NUM_PROCESS_ARGS = 4;
    private static final String TAG = "HWMW_HwMagicActivityStarter";
    private ActivityTaskManagerServiceEx mActivityTaskManager;
    private ActivityManagerServiceEx mAms;
    private HwMagicWinAmsPolicy mAmsPolicy;
    private HwMagicWinManager mMwManager;

    public HwMagicActivityStarter(ActivityManagerServiceEx ams, HwMagicWinManager manager, HwMagicWinAmsPolicy policy) {
        this.mAms = ams;
        this.mMwManager = manager;
        this.mAmsPolicy = policy;
        this.mActivityTaskManager = ams.getActivityTaskManagerEx();
    }

    public void overrideIntentFlagForHwMagicWin(ActivityRecordEx focus, ActivityRecordEx next, ActivityOptions options) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (next != null && container != null) {
            SlogEx.i(TAG, "### Execute overrideIntentFlagForHwMagicWin");
            HwMagicModeBase appMode = this.mAmsPolicy.getMode(focus);
            if (focus == null) {
                SlogEx.e(TAG, "overrideIntentFlagForHwMagicWin focus is null");
            } else if (!isOtherMultiWinOptions(options, next, focus)) {
                if (focus.isActivityTypeHome() && next.getIntent() != null) {
                    Set<String> categories = next.getIntent().getCategories();
                    next.setIsStartFromLauncher(categories != null && categories.contains("android.intent.category.LAUNCHER"));
                }
                if (focus.inHwMagicWindowingMode()) {
                    appMode.addNewTaskFlag(focus, next);
                    this.mAmsPolicy.finishInvisibleActivityInFullMode(focus);
                    ActivityRecordEx slavetop = this.mAmsPolicy.getActivityByPosition(focus, 2, 0);
                    if (slavetop != null && this.mMwManager.isMaster(focus)) {
                        slavetop.setMagicWindowPageType(1);
                    }
                }
                if (focus.isActivityTypeHome() && container.getHwMagicWinEnabled(Utils.getPackageName(next)) && !container.isFoldableDevice()) {
                    container.getAnimation().setOpenAppAnimation();
                }
                if (this.mAmsPolicy.getOrientationPolicy().isDefaultLandOrientation(next.getInfo().screenOrientation)) {
                    next.setIsFullScreenVideoInLandscape(true);
                }
                container.updateActivityOptions(focus, next, options);
            } else if (options != null) {
                this.mAmsPolicy.mMagicWinSplitMng.multWindowModeProcess(focus, ActivityOptionsEx.getLaunchWindowingMode(options));
            }
        }
    }

    public void overrideIntentForHwMagicWin(IBinder focusToken, ActivityRecordEx next, IBinder reusedToken, boolean isNewTask, ActivityOptions options) {
        TaskRecordEx task;
        ActivityRecordEx focus = ActivityRecordEx.forToken(focusToken);
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (container != null) {
            setExtrasWhenStartingWindow(next, focus);
            HwMagicModeBase appMode = this.mAmsPolicy.getMode(focus);
            SlogEx.i(TAG, "### Execute overrideIntentForHwMagicWin appMode=" + appMode);
            if (appMode.checkStatus(focus, next) && !isOtherMultiWinOptions(options, next, focus)) {
                if (focus == null || (!focus.inHwMultiStackWindowingMode() && !focus.inFreeformWindowingMode())) {
                    if (container.isFoldableDevice() && next.isStartFromLauncher()) {
                        next.setIsStartFromLauncher(false);
                        if (!this.mAmsPolicy.isMainActivity(container, next)) {
                            return;
                        }
                    }
                    ActivityRecordEx reusedActivity = ActivityRecordEx.forToken(reusedToken);
                    if (!isNewTask && reusedActivity != null && (task = reusedActivity.getTaskRecordEx()) != null && task.getTopActivity() == null && !task.equalsTaskRecord(focus.getTaskRecordEx())) {
                        isNewTask = true;
                    }
                    if (this.mAmsPolicy.isMainActivity(container, next)) {
                        focus.setStartingWindowState(ActivityRecordEx.STARTING_WINDOW_NOT_SHOWN);
                    }
                    if (isStartToMagicWindow(focus, next, isNewTask)) {
                        if (next.isActivityTypeHome() || isNewTask) {
                            this.mMwManager.getUIController().updateSplitBarVisibility(false, container.getDisplayId());
                        }
                        if (next.inHwMagicWindowingMode() && this.mAmsPolicy.isMainActivity(container, next)) {
                            this.mAmsPolicy.startRelateActivityIfNeed(next, false);
                        }
                        if (next.inHwMagicWindowingMode() && this.mMwManager.isMiddle(next) && this.mAmsPolicy.isPkgInLogoffStatus(next)) {
                            SlogEx.d(TAG, "overrideIntentForHwMagicWin not login set to full");
                            next.setBounds((Rect) null);
                            next.setWindowingMode(1);
                        }
                        if (next.getIntent() != null) {
                            next.getIntent().removeFlags(65536);
                        }
                    }
                }
            }
        }
    }

    public void overrideArgsForHwMagicWin(ApplicationInfo info, String[] args) {
        if (info != null && HwMwUtils.isInSuitableScene(true)) {
            if (args == null || args.length < 4) {
                SlogEx.w(TAG, "overrideArgsForHwMagicWin args is not valid");
                return;
            }
            HwMagicContainer container = this.mMwManager.getLocalContainer();
            ActivityStackEx focusStack = this.mAmsPolicy.getFocusedTopStack(container);
            boolean isMagicMode = focusStack != null ? focusStack.inHwMagicWindowingMode() : false;
            if (container != null && container.getHwMagicWinEnabled(info.packageName)) {
                SlogEx.w(TAG, "overrideArgsForHwMagicWin packageName : " + info.packageName);
                args[0] = String.valueOf(true);
                Rect bound = container.getBounds(3, info.packageName);
                args[1] = String.valueOf(String.valueOf(bound.width()));
                args[2] = String.valueOf(String.valueOf(bound.height()));
                if (isMagicMode) {
                    args[3] = String.valueOf((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
                } else {
                    args[3] = String.valueOf(0);
                }
                args[4] = String.valueOf(container.getConfig().isDragable(info.packageName));
            }
        }
    }

    public void clearTask(IBinder token, int flag, IBinder rToken, Bundle result) {
        ActivityRecordEx next = ActivityRecordEx.forToken(token);
        ActivityRecordEx current = ActivityRecordEx.forToken(rToken);
        SlogEx.i(TAG, "### Execute -> clearTask finish activity=" + current);
        if (next == null || current == null) {
            result.putBoolean("RESULT_CLEAR_TASK", false);
            return;
        }
        boolean isClearTaskInMagicWindowMode = (67108864 & flag) != 0 || next.getLaunchMode() == 2;
        HwMagicContainer container = this.mMwManager.getContainer(current);
        if (!isClearTaskInMagicWindowMode || ((!this.mAmsPolicy.isHomeActivity(container, current) && current.getCreateTime() >= next.getCreateTime()) || this.mAmsPolicy.isMainActivity(container, next))) {
            result.putBoolean("RESULT_CLEAR_TASK", false);
        } else {
            result.putBoolean("RESULT_CLEAR_TASK", true);
        }
    }

    public void setExtrasWhenStartingWindow(ActivityRecordEx next, ActivityRecordEx focus) {
        if (next != null && focus != null && next.getTaskRecordEx() != null && next.getIntent() != null && focus.getActivityStackEx() != null && next.getActivityStackEx() != null && !focus.getActivityStackEx().equalsStack(next.getActivityStackEx())) {
            String nextPkg = Utils.getRealPkgName(next);
            String focusPkg = Utils.getRealPkgName(focus);
            if (nextPkg != null && !nextPkg.equals(focusPkg)) {
                Bundle bundle = next.getTaskRecordEx().getMagicWindowExtras();
                if ("android.intent.action.MAIN".equals(next.getIntent().getAction())) {
                    bundle.putBoolean(Utils.KEY_LAUNCH_FROM_MAIN, true);
                } else {
                    bundle.putBoolean(Utils.KEY_LAUNCH_FROM_MAIN, false);
                }
                next.getTaskRecordEx().setMagicWindowExtras(bundle);
            }
        }
    }

    private boolean isStartToMagicWindow(ActivityRecordEx focus, ActivityRecordEx next, boolean isNewTask) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        HwMagicModeBase appMode = this.mAmsPolicy.getMode(focus);
        String nextPkg = Utils.getPackageName(next);
        String focusPkg = Utils.getPackageName(focus);
        if (!focus.inHwMagicWindowingMode() || isEnterDoubleWindowForFold(container, focus)) {
            if (this.mAmsPolicy.isMainActivity(container, next)) {
                this.mAmsPolicy.mModeSwitcher.moveToMagicWinFromFullscreenForMain(focus, next);
                return true;
            } else if (!container.isFoldableDevice() && (!container.isPadDevice() || !container.getConfig().isSupportAppTaskSplitScreen(nextPkg))) {
                appMode.overrideIntent(focus, next, isNewTask);
                return true;
            } else if (isNewTask || appMode.isLaunchFromMain(next)) {
                SlogEx.d(TAG, "isStartToMagicWindow focus is not MW and next new task");
                return false;
            } else {
                this.mAmsPolicy.mModeSwitcher.moveToMagicWinFromFullscreenForTah(focus, next);
                return true;
            }
        } else if (!container.isFoldableDevice() || !isNewTask || focusPkg == null || focusPkg.equals(nextPkg)) {
            appMode.setOrigActivityToken(container, focus);
            appMode.overrideIntent(focus, next, isNewTask);
            return true;
        } else {
            SlogEx.d(TAG, "isStartToMagicWindow start another app");
            return false;
        }
    }

    private boolean isOtherMultiWinOptions(ActivityOptions options, ActivityRecordEx next, ActivityRecordEx focus) {
        int windowMode;
        if (options == null || ((windowMode = ActivityOptionsEx.getLaunchWindowingMode(options)) == 102 && Utils.getPackageName(next) != null && Utils.getPackageName(next).equals(Utils.getRealPkgName(focus)) && focus.inHwMagicWindowingMode() && focus.getUserId() == next.getUserId())) {
            return false;
        }
        if (WindowConfigurationEx.isHwMultiStackWindowingMode(windowMode) || windowMode == 5) {
            return true;
        }
        return false;
    }

    private boolean isEnterDoubleWindowForFold(HwMagicContainer container, ActivityRecordEx focus) {
        if (!(container.isFoldableDevice() && focus.inHwMagicWindowingMode() && this.mMwManager.isFull(focus) && this.mAmsPolicy.isEnterDoubleWindowIgnoreHome(container, Utils.getRealPkgName(focus)))) {
            return false;
        }
        for (ActivityRecordEx activity : this.mAmsPolicy.getAllActivities(focus.getActivityStackEx())) {
            if (this.mAmsPolicy.isDefaultFullscreenActivity(container, activity)) {
                return false;
            }
        }
        return true;
    }
}
