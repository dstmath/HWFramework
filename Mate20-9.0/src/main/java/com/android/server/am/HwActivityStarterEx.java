package com.android.server.am;

import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.freeform.HwFreeFormUtils;
import android.iawareperf.UniPerf;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.HwPCUtils;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.Toast;
import com.android.server.UiThread;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.huawei.pgmng.log.LogPower;
import com.huawei.server.am.IHwActivityStarterEx;
import huawei.com.android.server.fingerprint.FingerViewController;
import java.util.ArrayList;

public class HwActivityStarterEx implements IHwActivityStarterEx {
    public static final String TAG = "HwActivityStarterEx";
    private static final boolean mIsSupportGameAssist;
    final ActivityManagerService mService;
    /* access modifiers changed from: private */
    public Toast mToast = null;
    private IVRSystemServiceManager mVrMananger;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.config.gameassist", 0) == 1) {
            z = true;
        }
        mIsSupportGameAssist = z;
    }

    public HwActivityStarterEx(ActivityManagerService service) {
        this.mService = service;
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
    }

    private String buildMsg(int canBoost, boolean isScreenOn) {
        return "canBoost=" + canBoost + "|screenOn=" + ((int) isScreenOn);
    }

    public void effectiveIawareToLaunchApp(Intent targetIntent, ActivityInfo targetAInfo, String curActivityPkName) {
        Intent intent = targetIntent;
        ActivityInfo activityInfo = targetAInfo;
        String str = curActivityPkName;
        if (intent != null && activityInfo != null) {
            String strPkg = "";
            String shortComponentName = "";
            String lastShortComponentName = null;
            if (targetIntent.getComponent() != null) {
                strPkg = targetIntent.getComponent().getPackageName();
                shortComponentName = targetIntent.getComponent().flattenToShortString();
            }
            if (!isAppHotStart(intent, activityInfo, this.mService.getRecentTasks().getRawTasks())) {
                this.mService.mDAProxy.notifyAppEventToIaware(3, strPkg);
            }
            boolean isScreenOn = false;
            BatteryStatsService mBatteryStatsService = this.mService.mBatteryStatsService;
            if (mBatteryStatsService != null) {
                isScreenOn = mBatteryStatsService.getActiveStatistics().isScreenOn();
            }
            int canBoost = this.mService.mHwAMSEx.canAppBoost(activityInfo, isScreenOn);
            String strMsg = buildMsg(canBoost, isScreenOn);
            ActivityRecord lastResumeActivity = this.mService.getLastResumedActivity();
            if (lastResumeActivity != null) {
                lastShortComponentName = lastResumeActivity.shortComponentName;
            }
            StringBuilder appEventStr = new StringBuilder();
            appEventStr.append(strPkg);
            boolean z = false;
            if (str == null || !str.equals(strPkg)) {
                boolean hasActivityInStack = false;
                if (this.mService.mWarmColdSwitch) {
                    hasActivityInStack = this.mService.mStackSupervisor.hasActivityInStackLocked(activityInfo);
                }
                if (hasActivityInStack) {
                    boolean z2 = hasActivityInStack;
                    UniPerf.getInstance().uniPerfEvent(4399, strMsg, new int[0]);
                } else {
                    if (FingerViewController.PKGNAME_OF_KEYGUARD.equals(strPkg)) {
                        UniPerf.getInstance().uniPerfEvent(4098, strMsg, new int[0]);
                    } else {
                        UniPerf.getInstance().uniPerfEvent(4099, strMsg, new int[0]);
                    }
                }
                if (canBoost > 0) {
                    this.mService.mDAProxy.notifyAppEventToIaware(1, appEventStr.toString());
                }
                LogPower.push(CPUFeature.MSG_SET_FG_CGROUP, strPkg);
                return;
            }
            boolean diffComponent = !shortComponentName.equals(lastShortComponentName);
            if (isScreenOn && diffComponent) {
                z = true;
            }
            sendUniperfEvent(4098, strMsg, z, appEventStr);
            if (canBoost > 0) {
                this.mService.mDAProxy.notifyAppEventToIaware(2, appEventStr.toString());
            }
        }
    }

    private void sendUniperfEvent(int uniperfCmdId, String extra, boolean needOnOff, StringBuilder appEventStr) {
        if (needOnOff) {
            appEventStr.append(":on");
            UniPerf.getInstance().uniPerfEvent(uniperfCmdId, extra, new int[]{0});
            return;
        }
        UniPerf.getInstance().uniPerfEvent(uniperfCmdId, extra, new int[0]);
    }

    private boolean isAppHotStart(Intent targetIntent, ActivityInfo targetAInfo, ArrayList<TaskRecord> recentTasks) {
        if (!(targetIntent == null || targetAInfo == null || recentTasks == null)) {
            if (!"android.intent.action.MAIN".equals(targetIntent.getAction()) || targetIntent.getCategories() == null || !targetIntent.getCategories().contains("android.intent.category.LAUNCHER")) {
                return true;
            }
            ComponentName cls = targetIntent.getComponent();
            int taskSize = recentTasks.size();
            int i = 0;
            while (i < taskSize) {
                TaskRecord task = recentTasks.get(i);
                Intent taskIntent = task.intent;
                Intent affinityIntent = task.affinityIntent;
                if ((task.rootAffinity == null || !task.rootAffinity.equals(targetAInfo.taskAffinity)) && ((taskIntent == null || taskIntent.getComponent() == null || taskIntent.getComponent().compareTo(cls) != 0) && (affinityIntent == null || affinityIntent.getComponent() == null || affinityIntent.getComponent().compareTo(cls) != 0))) {
                    i++;
                } else if (task.mActivities.size() > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean isAbleToLaunchInVR(Context context, String packageName) {
        if (packageName == null || context == null || this.mVrMananger == null || !this.mVrMananger.isVRMode()) {
            return true;
        }
        if (!this.mVrMananger.isVRApplication(context, packageName) && !this.mVrMananger.isVirtualScreenMode()) {
            return false;
        }
        if (this.mVrMananger.isVRApplication(context, packageName)) {
            this.mVrMananger.addVRLowPowerAppList(packageName);
        }
        IVRSystemServiceManager iVRSystemServiceManager = this.mVrMananger;
        if ("com.huawei.vrvirtualscreen".equals(packageName)) {
            this.mVrMananger.setVirtualScreenMode(true);
        } else {
            IVRSystemServiceManager iVRSystemServiceManager2 = this.mVrMananger;
            if ("com.huawei.vrlauncherx".equals(packageName)) {
                this.mVrMananger.setVirtualScreenMode(false);
            }
        }
        return true;
    }

    public boolean isAbleToLaunchVideoActivity(Context context, Intent intent) {
        if (!mIsSupportGameAssist) {
            return true;
        }
        if (!HwSnsVideoManager.getInstance(context).getReadyToShowActivity(intent)) {
            return false;
        }
        HwSnsVideoManager.getInstance(this.mService.mContext).setReadyToShowActivity(false);
        return true;
    }

    public void handleFreeFormStackIfNeed(ActivityRecord startActivity) {
        if (HwFreeFormUtils.isFreeFormEnable()) {
            ActivityStack freeFormStack = this.mService.mStackSupervisor.getStack(5, 1);
            if (freeFormStack != null) {
                ActivityRecord topActivity = freeFormStack.topRunningActivityLocked();
                if (topActivity != null && startActivity.launchedFromPackage != null && startActivity.launchedFromPackage.equals(topActivity.packageName)) {
                    HwFreeFormUtils.log("ams", "start activity:" + startActivity + " from:" + startActivity.launchedFromPackage + " in pkg:" + startActivity.packageName + " freeform topActivity.pkg:" + topActivity.packageName);
                    if (freeFormStack.getCurrentPkgUnderFreeForm().equals(startActivity.packageName)) {
                        HwFreeFormUtils.log("ams", "keep freeform for launch the under-freeform app from the same app as freeform-app in fullscreen stack");
                    } else if (!topActivity.packageName.equals(startActivity.packageName)) {
                        freeFormStack.setCurrentPkgUnderFreeForm("");
                        freeFormStack.setFreeFormStackVisible(false);
                        ActivityStack toStack = this.mService.mStackSupervisor.getDefaultDisplay().getTopStackInWindowingMode(1);
                        HwFreeFormUtils.log("ams", "move freeform to fullscreen for launch other app from freeform stack");
                        topActivity.getTask().reparent(toStack, true, 1, true, false, "exitFreeformMode");
                    }
                } else if (topActivity != null && topActivity.packageName.equals(startActivity.packageName) && startActivity.getWindowingMode() == 1) {
                    HwFreeFormUtils.log("ams", "keep freeform for launch the same app as freeform-app in fullscreen stack");
                } else if (!"".equals(freeFormStack.getCurrentPkgUnderFreeForm()) && freeFormStack.getCurrentPkgUnderFreeForm() != null && !freeFormStack.getCurrentPkgUnderFreeForm().equals(startActivity.packageName) && !startActivity.inFreeformWindowingMode()) {
                    freeFormStack.setFreeFormStackVisible(false);
                    if (startActivity.getActivityType() != 3) {
                        freeFormStack.setCurrentPkgUnderFreeForm("");
                        freeFormStack.finishAllActivitiesLocked(true);
                    }
                    HwFreeFormUtils.log("ams", "remove freeform for launch app from no-freeform stack");
                }
            }
        }
    }

    public void moveFreeFormToFullScreenStackIfNeed(ActivityRecord startActivity, boolean isInFreeformWindowingMode) {
        if (HwFreeFormUtils.isFreeFormEnable()) {
            ActivityStack freeFormStack = this.mService.mStackSupervisor.getStack(5, 1);
            if (freeFormStack != null && freeFormStack.getFreeFormStackVisible()) {
                String activityTitle = startActivity.toString();
                boolean isNeedExit = false;
                for (String str : HwFreeFormUtils.sExitFreeformActivity) {
                    if (activityTitle.contains(str)) {
                        isNeedExit = true;
                    }
                }
                ActivityRecord topActivity = freeFormStack.topRunningActivityLocked();
                if ((!isInFreeformWindowingMode && startActivity.packageName != null && topActivity != null && startActivity.packageName.equals(topActivity.packageName)) || (isInFreeformWindowingMode && isNeedExit)) {
                    if (topActivity != null) {
                        try {
                            if (topActivity.task != null) {
                                ActivityRecord rootActivity = topActivity.task.getRootActivity();
                                if (rootActivity == null || rootActivity.app == null || rootActivity.app.thread == null) {
                                    HwFreeFormUtils.log("ams", "restoreFreeFormConfig failed : no rootActivity");
                                } else {
                                    rootActivity.app.thread.scheduleRestoreFreeFormConfig(rootActivity.appToken);
                                }
                            }
                        } catch (RemoteException e) {
                            HwFreeFormUtils.log("ams", "scheduleRestoreFreeFormConfig error!");
                        }
                    }
                    freeFormStack.setCurrentPkgUnderFreeForm("");
                    freeFormStack.setFreeFormStackVisible(false);
                    HwFreeFormUtils.log("ams", "moveFreeFormToFullScreenStack for launch some app from freeform stack but in different stack");
                    freeFormStack.setWindowingMode(1);
                }
            }
        }
    }

    public boolean isAbleToLaunchInPCCastMode(String shortComponentName, int displayId, final ActivityRecord reusedActivity) {
        if (((displayId == 0 || displayId == -1) && (reusedActivity == null || reusedActivity.getDisplayId() == 0 || reusedActivity.getDisplayId() == -1)) || shortComponentName == null || !HwPCUtils.isPcCastModeInServer()) {
            return true;
        }
        String[] tmp = shortComponentName.split("/");
        if (tmp.length != 2) {
            return true;
        }
        String packageName = tmp[0];
        final boolean isAppLocked = "com.huawei.systemmanager/.applock.password.AuthLaunchLockedAppActivity".equals(shortComponentName);
        if (!HwPCUtils.sPackagesCanStartedInPCMode.contains(packageName) && !isAppLocked) {
            return true;
        }
        HwPCUtils.log(TAG, "about to launch app which cannot be started in PC mode, abort.");
        final Context context = HwPCUtils.getDisplayContext(this.mService.mContext, HwPCUtils.getPCDisplayID());
        if (context != null) {
            UiThread.getHandler().post(new Runnable() {
                public void run() {
                    String toShow;
                    if (HwActivityStarterEx.this.mToast != null) {
                        HwActivityStarterEx.this.mToast.cancel();
                    }
                    if (isAppLocked) {
                        toShow = context.getResources().getString(33686165);
                        if (reusedActivity != null) {
                            HwActivityStarterEx.this.mService.forceStopPackageLocked(reusedActivity.packageName, UserHandle.getAppId(reusedActivity.appInfo.uid), false, false, true, false, false, UserHandle.getUserId(reusedActivity.appInfo.uid), "launch locked app in external display when unlocked screen");
                        }
                    } else {
                        toShow = context.getResources().getString(33686015);
                    }
                    Toast unused = HwActivityStarterEx.this.mToast = Toast.makeText(context, toShow, 0);
                    HwActivityStarterEx.this.mToast.show();
                }
            });
        }
        return false;
    }
}
