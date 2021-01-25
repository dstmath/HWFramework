package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.freeform.HwFreeFormUtils;
import android.hdm.HwDeviceManager;
import android.iawareperf.UniPerf;
import android.os.Binder;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.widget.Toast;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.HwBluetoothBigDataService;
import com.android.server.UiThread;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.pgmng.log.LogPower;
import com.huawei.server.HwPCFactory;
import com.huawei.server.wm.IHwActivityStarterEx;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HwActivityStarterEx implements IHwActivityStarterEx {
    private static final int APPLOCK_CLOSE_STATE = 0;
    private static final int APPLOCK_OPEN_STATE = 1;
    private static final String APP_LOCK_FUNC_STATUS = "app_lock_func_status";
    private static final String APP_LOCK_LIST = "app_lock_list";
    private static final int COMPONENT_NAME_LENGTH = 2;
    private static final int DEFAULT_UID = -1;
    private static final String INSTANTSHARE_PACKAGE_NAME = "com.huawei.android.instantshare";
    private static final int INVALID_DISPLAY_ID = -1;
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_PRELOADAPP_EN = SystemProperties.getBoolean("persist.sys.appstart.preload.enable", false);
    private static final boolean IS_SUPPORT_GAME_ASSIST;
    private static final String PKG_ICONNECT = "com.huawei.iconnect";
    private static final String PKG_PARENT_CONTROL = "com.huawei.parentcontrol";
    private static final int SCREEN_OFF = 0;
    private static final int SCREEN_ON = 1;
    private static final String SEMICOLON_STR = ";";
    private static final String SKIP_REUSE = "skipReuse";
    private static final String SKIP_START = "skipStart";
    private static final String START_RESULT = "startResult";
    private static final String TAG = "HwActivityStarterEx";
    private static final int WAIT_TIME = 10;
    private static Set<String> sPCPkgName = new HashSet();
    private boolean isAppLocked = false;
    private volatile boolean mIsNeedWait = true;
    final ActivityTaskManagerService mService;
    private Toast mToast = null;

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.config.gameassist", 0) != 1) {
            z = false;
        }
        IS_SUPPORT_GAME_ASSIST = z;
        sPCPkgName.add("com.huawei.android.hwpay");
        sPCPkgName.add(HwBluetoothBigDataService.BIGDATA_RECEIVER_PACKAGENAME);
        sPCPkgName.add("com.huawei.screenrecorder");
        sPCPkgName.add("com.huawei.pcassistant");
        sPCPkgName.add("com.huawei.dmsdp");
        sPCPkgName.add("com.huawei.associateassistant");
        sPCPkgName.add(INSTANTSHARE_PACKAGE_NAME);
    }

    public HwActivityStarterEx(ActivityTaskManagerService service) {
        this.mService = service;
    }

    private String buildMsg(int canBoost, boolean isScreenOn) {
        return "canBoost=" + canBoost + "|screenOn=" + (isScreenOn ? 1 : 0);
    }

    public void effectiveIawareToLaunchApp(Intent targetIntent, ActivityInfo targetActivityInfo, String curActivityPkName) {
        if (targetIntent != null && targetActivityInfo != null) {
            String strPkg = "";
            String shortComponentName = "";
            String lastShortComponentName = null;
            if (targetIntent.getComponent() != null) {
                strPkg = targetIntent.getComponent().getPackageName();
                shortComponentName = targetIntent.getComponent().flattenToShortString();
            }
            if (!isAppHotStart(targetIntent, targetActivityInfo, this.mService.getRecentTasks().getRawTasks())) {
                this.mService.mAtmDAProxy.notifyAppEventToIaware(3, strPkg);
            }
            boolean isScreenOn = false;
            Object obj = this.mService.mContext.getSystemService("power");
            if (obj instanceof PowerManager) {
                isScreenOn = ((PowerManager) obj).isScreenOn();
            }
            int canBoost = this.mService.mHwATMSEx.canAppBoost(targetActivityInfo, isScreenOn);
            String strMsg = buildMsg(canBoost, isScreenOn);
            ActivityRecord lastResumeActivity = this.mService.getLastResumedActivityRecord();
            if (lastResumeActivity != null) {
                lastShortComponentName = lastResumeActivity.shortComponentName;
            }
            if (curActivityPkName == null || !curActivityPkName.equals(strPkg)) {
                effectiveIawareToLaunchAppWhenNotCurrent(targetActivityInfo, strPkg, canBoost, strMsg);
                return;
            }
            StringBuilder appEventStr = new StringBuilder(strPkg);
            sendUniperfEvent(4098, strMsg, isScreenOn && (shortComponentName.equals(lastShortComponentName) ^ true), appEventStr);
            if (canBoost > 0) {
                this.mService.mAtmDAProxy.notifyAppEventToIaware(2, appEventStr.toString());
            }
        }
    }

    private void effectiveIawareToLaunchAppWhenNotCurrent(ActivityInfo targetActivityInfo, String strPkg, int canBoost, String strMsg) {
        boolean hasActivityInStack = false;
        if (this.mService.mWarmColdSwitch) {
            hasActivityInStack = this.mService.mStackSupervisor.hasActivityInStackLocked(targetActivityInfo);
        }
        if (hasActivityInStack) {
            if (!"com.huawei.android.launcher".equals(strPkg)) {
                UniPerf.getInstance().uniPerfEvent(4399, strMsg, new int[0]);
            }
        } else if ("com.android.systemui".equals(strPkg)) {
            UniPerf.getInstance().uniPerfEvent(4098, strMsg, new int[0]);
        } else {
            UniPerf.getInstance().uniPerfEvent(4099, strMsg, new int[0]);
        }
        if (canBoost > 0) {
            this.mService.mAtmDAProxy.notifyAppEventToIaware(1, strPkg);
        }
        LogPower.push(139, strPkg);
    }

    private void sendUniperfEvent(int uniperfCmdId, String extra, boolean isNeedOnOff, StringBuilder appEventStr) {
        if (isNeedOnOff) {
            appEventStr.append(":on");
            UniPerf.getInstance().uniPerfEvent(uniperfCmdId, extra, new int[]{0});
            return;
        }
        UniPerf.getInstance().uniPerfEvent(uniperfCmdId, extra, new int[0]);
    }

    private boolean isAppHotStart(Intent targetIntent, ActivityInfo targetActivityInfo, ArrayList<TaskRecord> recentTasks) {
        if (targetIntent == null || targetActivityInfo == null || recentTasks == null) {
            return false;
        }
        if (!"android.intent.action.MAIN".equals(targetIntent.getAction()) || targetIntent.getCategories() == null || !targetIntent.getCategories().contains("android.intent.category.LAUNCHER")) {
            return true;
        }
        Optional<Boolean> ret = isAppHotStartWithNonMainActivity(targetIntent, targetActivityInfo, recentTasks);
        if (ret.isPresent()) {
            return ret.get().booleanValue();
        }
        return false;
    }

    private Optional<Boolean> isAppHotStartWithNonMainActivity(Intent targetIntent, ActivityInfo targetActivityInfo, ArrayList<TaskRecord> recentTasks) {
        ComponentName cls = targetIntent.getComponent();
        int taskSize = recentTasks.size();
        for (int i = 0; i < taskSize; i++) {
            TaskRecord task = recentTasks.get(i);
            Intent taskIntent = task.intent;
            Intent affinityIntent = task.affinityIntent;
            if ((task.rootAffinity != null && task.rootAffinity.equals(targetActivityInfo.taskAffinity)) || ((taskIntent != null && taskIntent.getComponent() != null && taskIntent.getComponent().compareTo(cls) == 0) || (affinityIntent != null && affinityIntent.getComponent() != null && affinityIntent.getComponent().compareTo(cls) == 0))) {
                return Optional.of(Boolean.valueOf(task.mActivities.size() > 0));
            }
        }
        return Optional.empty();
    }

    public boolean isAbleToLaunchInVr(Context context, Intent intent, String callingPackage, ActivityInfo launchActivityInfo) {
        return this.mService.mVrMananger.isAbleToLaunchInVr(context, intent, callingPackage, launchActivityInfo);
    }

    public boolean isAbleToLaunchVideoActivity(Context context, Intent intent) {
        if (!IS_SUPPORT_GAME_ASSIST || intent == null || intent.getComponent() == null || !HwActivityTaskManager.isGameDndOn() || !HwSnsVideoManager.isInterceptActivity(intent.getComponent().flattenToShortString())) {
            return true;
        }
        return false;
    }

    public boolean isAbleToLaunchInPCCastMode(String shortComponentName, int displayId, ActivityRecord reusedActivity) {
        if (((displayId == 0 || displayId == -1) && (reusedActivity == null || reusedActivity.getDisplayId() == 0 || reusedActivity.getDisplayId() == -1)) || shortComponentName == null || !HwPCUtils.isPcCastModeInServer()) {
            return true;
        }
        String[] tmp = shortComponentName.split("/");
        if (tmp.length != 2) {
            return true;
        }
        if (!HwPCUtils.sPackagesCanStartedInPCMode.contains(tmp[0]) && !this.isAppLocked) {
            return true;
        }
        if (this.isAppLocked && reusedActivity != null) {
            try {
                forceStopPackageSync(reusedActivity);
            } catch (Exception e) {
                HwPCUtils.log(TAG, "Failed to kill app lock");
            }
        }
        showToast(displayId, false, this.isAppLocked);
        return false;
    }

    private boolean isAppInLockList(ActivityRecord activityRecord) {
        if (activityRecord == null || this.mService == null || activityRecord.packageName == null || Settings.Secure.getInt(this.mService.mContext.getContentResolver(), APP_LOCK_FUNC_STATUS, 0) != 1) {
            return false;
        }
        if ((";" + Settings.Secure.getStringForUser(this.mService.mContext.getContentResolver(), APP_LOCK_LIST, activityRecord.mUserId) + ";").contains(";" + activityRecord.packageName + ";")) {
            return true;
        }
        return false;
    }

    private boolean packageShouldNotHandle(String pkgName) {
        return HwPCUtils.enabledInPad() ? "com.huawei.desktop.explorer".equals(pkgName) || "com.huawei.desktop.systemui".equals(pkgName) || "com.android.systemui".equals(pkgName) || "com.android.incallui".equals(pkgName) || "com.huawei.android.wfdft".equals(pkgName) || PKG_PARENT_CONTROL.equals(pkgName) || PKG_ICONNECT.equals(pkgName) : HwPCUtils.isHiCarCastMode() ? "com.huawei.desktop.explorer".equals(pkgName) || "com.huawei.desktop.systemui".equals(pkgName) || "com.huawei.hicar".equals(pkgName) : "com.huawei.desktop.explorer".equals(pkgName) || "com.huawei.desktop.systemui".equals(pkgName);
    }

    private ArrayList<WindowProcessController> getProcessOnOtherDisplay(String pkg, int userId, int sourceDisplayId) {
        ArrayList<WindowProcessController> procs = new ArrayList<>();
        if (!HwPCUtils.isValidExtDisplayId(sourceDisplayId) || !HwPCUtils.isHiCarCastMode() || !hasTopActivityOnHiCarDisplay(pkg)) {
            int processSize = this.mService.mProcessNames.getMap().size();
            for (int ip = 0; ip < processSize; ip++) {
                SparseArray<WindowProcessController> apps = (SparseArray) this.mService.mProcessNames.getMap().valueAt(ip);
                int appSize = apps.size();
                for (int ia = 0; ia < appSize; ia++) {
                    WindowProcessController proc = apps.valueAt(ia);
                    if (proc.mUserId == userId && proc != this.mService.mHomeProcess && ((proc.mPkgList.contains(pkg) || ("com.huawei.filemanager.desktopinstruction".equals(pkg) && proc.mName != null && proc.mName.equals(pkg))) && ((HwPCUtils.isValidExtDisplayId(sourceDisplayId) || HwPCUtils.isValidExtDisplayId(proc.mDisplayId)) && proc.mDisplayId != sourceDisplayId))) {
                        procs.add(proc);
                    }
                }
            }
            return procs;
        }
        Log.d(TAG, "Do not kill top activity on hicar display");
        return procs;
    }

    private void showToast(final int displayId, final boolean isAdaptInPcScreen, final boolean isAppLock) {
        if (HwPCUtils.isPcCastModeInServer()) {
            final Context context = HwPCUtils.isValidExtDisplayId(displayId) ? HwPCUtils.getDisplayContext(this.mService.mContext, displayId) : this.mService.mContext;
            if (context != null) {
                UiThread.getHandler().post(new Runnable() {
                    /* class com.android.server.wm.HwActivityStarterEx.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        if (HwActivityStarterEx.this.mToast != null) {
                            HwActivityStarterEx.this.mToast.cancel();
                        }
                        if (HwPCUtils.isValidExtDisplayId(displayId)) {
                            if (isAppLock) {
                                HwActivityStarterEx hwActivityStarterEx = HwActivityStarterEx.this;
                                Context context = context;
                                hwActivityStarterEx.mToast = Toast.makeText(context, context.getResources().getString(33686095), 0);
                            } else if (!isAdaptInPcScreen) {
                                HwActivityStarterEx hwActivityStarterEx2 = HwActivityStarterEx.this;
                                Context context2 = context;
                                hwActivityStarterEx2.mToast = Toast.makeText(context2, context2.getResources().getString(33686015), 0);
                            } else {
                                HwPCUtils.log(HwActivityStarterEx.TAG, "nothing to toast");
                            }
                        }
                        if (HwActivityStarterEx.this.mToast != null) {
                            HwActivityStarterEx.this.mToast.show();
                        }
                    }
                });
            }
        }
    }

    private int hasStartedOnOtherDisplay(ActivityRecord startActivity, int sourceDisplayId) {
        int ret;
        if (HwPCUtils.isPcCastModeInServer() && !sPCPkgName.contains(startActivity.packageName)) {
            String activityName = startActivity.mActivityComponent != null ? startActivity.mActivityComponent.getClassName() : "";
            if (packageShouldNotHandle(startActivity.packageName) && !"com.huawei.filemanager.desktopinstruction.EasyProjection".equals(activityName)) {
                return -1;
            }
            if (HwPCUtils.isValidExtDisplayId(sourceDisplayId) && startActivity.isActivityTypeHome()) {
                return 2;
            }
            if ((!HwPCUtils.isValidExtDisplayId(sourceDisplayId) || !HwPCUtils.isHiCarCastMode()) && hasTopActivityOnHiCarDisplay(startActivity.packageName) && (ret = hasStartedOnOtherDisplayWithResumedActivity(startActivity, sourceDisplayId, activityName)) != -1) {
                return ret;
            }
            if (HwPCUtils.isHiCarCastMode() && HwPCUtils.isValidExtDisplayId(sourceDisplayId)) {
                startActivity.mShowWhenLocked = true;
            }
            this.mService.mHwATMSEx.getPkgDisplayMaps().put(startActivity.packageName, Integer.valueOf(sourceDisplayId));
        }
        return -1;
    }

    private int hasStartedOnOtherDisplayWithResumedActivity(ActivityRecord startActivity, int sourceDisplayId, String activityName) {
        ArrayList<WindowProcessController> list;
        if ("com.huawei.filemanager.desktopinstruction.EasyProjection".equals(activityName)) {
            list = getProcessOnOtherDisplay("com.huawei.filemanager.desktopinstruction", startActivity.mUserId, sourceDisplayId);
        } else {
            list = getProcessOnOtherDisplay(startActivity.packageName, startActivity.mUserId, sourceDisplayId);
        }
        if (list == null) {
            return -1;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).hasForegroundActivities() && !HwPCUtils.enabledInPad() && !ActivityStartInterceptorBridge.isAppLockActivity(startActivity.shortComponentName) && (!"com.huawei.works".equals(startActivity.packageName) || !HwPCUtils.isValidExtDisplayId(sourceDisplayId))) {
                HwPCUtils.showDialogForSwitchDisplay(sourceDisplayId, startActivity.packageName);
                if (sourceDisplayId == 0) {
                    return 1;
                }
                return 0;
            }
        }
        return -1;
    }

    private boolean hasTopActivityOnHiCarDisplay(String pkgName) {
        if (!HwPCUtils.isHiCarCastMode()) {
            return true;
        }
        Optional<String> topPkgName = Optional.ofNullable(this.mService.getRootActivityContainer().getActivityDisplay(HwPCUtils.getPCDisplayID())).map($$Lambda$HwActivityStarterEx$acOL8KCZ6yIs_CbIVhBwdx9zM6M.INSTANCE).map($$Lambda$HwActivityStarterEx$eDBLZR9SLmQ0NA7ufbP9KcllCPY.INSTANCE).map($$Lambda$HwActivityStarterEx$HkPfId3HfP6HrH7s_t3B0IBWFQs.INSTANCE);
        if (topPkgName == null || !topPkgName.isPresent() || !topPkgName.get().equals(pkgName)) {
            return false;
        }
        HwPCUtils.log(TAG, "Top activity on PC display is matched. ");
        return true;
    }

    private boolean isRemovedForKillProcessInPcMode(ActivityRecord startActivity, int sourceDisplayId) {
        ArrayList<WindowProcessController> list = getProcessOnOtherDisplay("com.huawei.filemanager.desktopinstruction", startActivity.mUserId, sourceDisplayId);
        int size = list.size();
        boolean isRemoved = false;
        for (int i = 0; i < size; i++) {
            Process.killProcess(list.get(i).mPid);
            isRemoved = true;
        }
        if (isRemoved) {
            Set<String> disabledClasses = new HashSet<>();
            disabledClasses.add("com.huawei.filemanager.desktopinstruction.EasyProjection");
            this.mService.mRootActivityContainer.finishDisabledPackageActivities("com.huawei.desktop.explorer", disabledClasses, true, false, UserHandle.getUserId(startActivity.appInfo.uid));
        }
        return isRemoved;
    }

    private boolean isKillProcessOnOtherDisplay(ActivityRecord startActivity, int sourceDisplayId) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return false;
        }
        String activityName = startActivity.mActivityComponent != null ? startActivity.mActivityComponent.getClassName() : "";
        if (packageShouldNotHandle(startActivity.packageName) && !"com.huawei.filemanager.desktopinstruction.EasyProjection".equals(activityName)) {
            return false;
        }
        if (!(!HwPCUtils.isHiCarCastMode() || startActivity.intent == null || startActivity.intent.getComponent() == null)) {
            try {
                List<String> records = HwPCUtils.getHwPCManager().getCarAppList();
                if (records != null && !records.isEmpty()) {
                    for (int i = 0; i < records.size(); i++) {
                        if (startActivity.intent.getComponent().flattenToString().equals(records.get(i))) {
                            HwPCUtils.log(TAG, "dont kill this activity:" + records.get(i));
                            return false;
                        }
                    }
                }
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "remote exception while getCarAppList");
            }
        }
        if ("com.huawei.filemanager.desktopinstruction.EasyProjection".equals(activityName)) {
            return isRemovedForKillProcessInPcMode(startActivity, sourceDisplayId);
        }
        ArrayList<WindowProcessController> list = getProcessOnOtherDisplay(startActivity.packageName, startActivity.mUserId, sourceDisplayId);
        int size = list.size();
        int i2 = 0;
        while (true) {
            if (i2 >= size) {
                break;
            }
            if (HwPCUtils.enabledInPad() && WifiProCommonUtils.WIFI_SETTINGS_PHONE.equals(startActivity.packageName) && "com.android.phone".equals(list.get(i2).mName)) {
                HwPCUtils.log(TAG, "settings in phone process");
            } else if (TextUtils.isEmpty(startActivity.packageName) || !startActivity.packageName.contains("com.tencent.mm") || startActivity.app == null || startActivity.app.mPid == list.get(i2).mPid) {
                break;
            } else {
                HwPCUtils.log(TAG, "pid is not same so dont kill the process when killing Process on other display");
            }
            i2++;
        }
        String processName = list.get(i2).mName;
        int targetDisplayId = list.get(i2).mDisplayId;
        if (!sPCPkgName.contains(startActivity.packageName)) {
            if ("com.huawei.works".equals(startActivity.packageName)) {
                killOrMoveProcess(sourceDisplayId, startActivity);
                return true;
            }
            forceStopPackageSync(startActivity);
            HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwPCDataReporter().reportKillProcessEvent(startActivity.packageName, processName, sourceDisplayId, targetDisplayId);
            return true;
        }
        return false;
    }

    private void killOrMoveProcess(int sourceDisplayId, ActivityRecord startActivity) {
        HwPCUtils.log(TAG, "killOrMoveProcess sourceDisplayId:" + sourceDisplayId);
        ArrayList<WindowProcessController> list = getProcessOnOtherDisplay(startActivity.packageName, startActivity.mUserId, sourceDisplayId);
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            WindowProcessController controller = list.get(i);
            if (controller.mActivities.size() > 0) {
                ActivityRecord record = (ActivityRecord) controller.mActivities.get(0);
                if (record != null) {
                    record.getActivityStack().finishAllActivitiesLocked(true);
                    Process.killProcess(controller.getPid());
                }
            } else if ("com.huawei.works".equals(controller.mName)) {
                Process.killProcess(controller.getPid());
            }
            controller.mDisplayId = sourceDisplayId;
        }
    }

    private boolean killProcessOnDefaultDisplay(ActivityRecord startActivity) {
        if (!HwPCUtils.enabled()) {
            return false;
        }
        boolean isKillPackageProcess = false;
        ArrayList<WindowProcessController> list = getProcessOnOtherDisplay(startActivity.packageName, startActivity.mUserId, 0);
        int size = list.size();
        int i = 0;
        while (true) {
            if (i >= size) {
                break;
            } else if (HwPCUtils.isValidExtDisplayId(list.get(i).mDisplayId)) {
                isKillPackageProcess = true;
                break;
            } else {
                i++;
            }
        }
        if (!isKillPackageProcess) {
            return false;
        }
        if ("com.huawei.works".equals(startActivity.packageName)) {
            killOrMoveProcess(0, startActivity);
            return true;
        }
        forceStopPackageSync(startActivity);
        return true;
    }

    public Bundle checkActivityStartedOnDisplay(ActivityRecord startActivity, int preferredDisplayId, ActivityOptions options, ActivityRecord reusedActivity) {
        if (startActivity == null) {
            Slog.i(TAG, "startActivityUnchecked reusedActivity :" + reusedActivity);
            return null;
        } else if (HwPCUtils.isHiCarCastMode() && isSupportMultiDisplayMetaData(startActivity.packageName)) {
            Slog.i(TAG, "App support multiDisplay, do not check");
            return null;
        } else if (HwPCUtils.isPcCastModeInServer()) {
            return checkWithPcCastModeInServer(startActivity, preferredDisplayId, options, reusedActivity);
        } else {
            if (!killProcessOnDefaultDisplay(startActivity)) {
                return null;
            }
            Bundle ret = new Bundle();
            ret.putBoolean(SKIP_REUSE, true);
            return ret;
        }
    }

    private boolean isSupportMultiDisplayMetaData(String packageName) {
        boolean isSupportMultiDisplay = false;
        try {
            ApplicationInfo appInfo = this.mService.mContext.getPackageManager().getApplicationInfo(packageName, 128);
            if (!(appInfo == null || appInfo.metaData == null)) {
                isSupportMultiDisplay = appInfo.metaData.getBoolean("com.huawei.multidisplay.support.multidisplay", false);
            }
            Slog.i(TAG, "isSupportMultiDisplay:" + isSupportMultiDisplay);
            return isSupportMultiDisplay;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "isSupportMultiDisplay: package name not found !");
            return false;
        }
    }

    private Bundle checkWithPcCastModeInServer(ActivityRecord startActivity, int preferredDisplayId, ActivityOptions options, ActivityRecord inReusedActivity) {
        Bundle ret = new Bundle();
        ret.putInt(START_RESULT, 0);
        ret.putBoolean(SKIP_REUSE, false);
        ret.putBoolean(SKIP_START, false);
        ActivityRecord reusedActivity = inReusedActivity;
        checkAppIsLocked(startActivity);
        if (!isAbleToLaunchInPCCastMode(startActivity.shortComponentName, preferredDisplayId, reusedActivity)) {
            ret.putBoolean(SKIP_START, true);
            ret.putInt(START_RESULT, 102);
            return ret;
        }
        int startedResult = hasStartedOnOtherDisplay(startActivity, preferredDisplayId);
        if (startedResult != -1) {
            ActivityOptions.abort(options);
            return getSkipStartBundle(startActivity, ret, startedResult);
        }
        if (isKillProcessOnOtherDisplay(startActivity, preferredDisplayId)) {
            reusedActivity = null;
            ret.putBoolean(SKIP_REUSE, true);
        }
        if (HwPCUtils.enabledInPad() && reusedActivity != null && HwPCUtils.isValidExtDisplayId(preferredDisplayId) && reusedActivity.getDisplayId() != preferredDisplayId) {
            reusedActivity = null;
            ret.putBoolean(SKIP_REUSE, true);
            Slog.i(TAG, "reusedActivity is not in PCdisplay");
        }
        if (reusedActivity != null && !HwPCUtils.isValidExtDisplayId(reusedActivity.getDisplayId()) && ((HwPCUtils.enabledInPad() && ("com.android.systemui/.settings.BrightnessDialog".equals(reusedActivity.shortComponentName) || "com.android.incallui".equals(reusedActivity.packageName) || "com.huawei.android.wfdft".equals(reusedActivity.packageName))) || (HwPCUtils.isHiCarCastMode() && "com.android.incallui".equals(reusedActivity.packageName)))) {
            if (Log.HWINFO) {
                Slog.i(TAG, "startActivityUnchecked reusedActivity :" + reusedActivity);
            }
            ret.putBoolean(SKIP_REUSE, true);
        }
        return ret;
    }

    private void checkAppIsLocked(ActivityRecord startActivity) {
        if (startActivity != null) {
            this.isAppLocked = ActivityStartInterceptorBridge.isAppLockActivity(startActivity.shortComponentName) || isAppInLockList(startActivity);
        }
    }

    private Bundle getSkipStartBundle(ActivityRecord startActivity, Bundle ret, int startedResult) {
        ActivityStack sourceStack = startActivity.resultTo != null ? startActivity.resultTo.getActivityStack() : null;
        if (sourceStack != null) {
            sourceStack.sendActivityResultLocked(-1, startActivity.resultTo, startActivity.resultWho, startActivity.requestCode, 0, (Intent) null);
        }
        if (startedResult == 1) {
            ret.putInt(START_RESULT, 99);
            ret.putBoolean(SKIP_START, true);
        } else if (startedResult == 0) {
            ret.putInt(START_RESULT, 98);
            ret.putBoolean(SKIP_START, true);
        } else {
            ret.putBoolean(SKIP_START, true);
        }
        return ret;
    }

    public boolean checkActivityStartForPCMode(ActivityOptions options, ActivityRecord startActivity, ActivityStack targetStack) {
        if (HwPCUtils.isPcCastModeInServer()) {
            if (startActivity == null || targetStack == null) {
                HwPCUtils.log(TAG, "null params, return true for checkActivityStartForPCMode");
                return true;
            } else if (HwPCUtils.isHiCarCastMode() && isSupportMultiDisplayMetaData(startActivity.packageName)) {
                Slog.i(TAG, "App support multiDisplay, do not check");
                return true;
            } else if (hasStartedOnOtherDisplay(startActivity, targetStack.mDisplayId) != -1) {
                ActivityOptions.abort(options);
                ActivityStack sourceStack = startActivity.resultTo != null ? startActivity.resultTo.getActivityStack() : null;
                if (sourceStack != null) {
                    sourceStack.sendActivityResultLocked(-1, startActivity.resultTo, startActivity.resultWho, startActivity.requestCode, 0, (Intent) null);
                }
                if (!Log.HWINFO) {
                    return false;
                }
                HwPCUtils.log(TAG, "cancel activity start, act:" + startActivity + " targetStack:" + targetStack);
                return false;
            }
        }
        return true;
    }

    private void forceStopPackageSync(final ActivityRecord record) {
        HwPCUtils.log(TAG, "forceStopPackageSync, enter.");
        this.mIsNeedWait = true;
        new Thread(new Runnable() {
            /* class com.android.server.wm.HwActivityStarterEx.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                Process.setThreadPriority(-2);
                try {
                    HwPCUtils.log(HwActivityStarterEx.TAG, "AMS forceStopPackage, start.");
                    if (record == null || record.appInfo == null) {
                        HwPCUtils.log(HwActivityStarterEx.TAG, "AMS forceStopPackage break, activity record is null");
                    } else {
                        ActivityManager.getService().forceStopPackage(record.packageName, UserHandle.getUserId(record.appInfo.uid));
                    }
                    HwPCUtils.log(HwActivityStarterEx.TAG, "AMS forceStopPackage, end.");
                } catch (RemoteException e) {
                    if (Log.HWINFO) {
                        HwPCUtils.log(HwActivityStarterEx.TAG, "Failed to kill aps package of " + record.packageName);
                    }
                }
                HwActivityStarterEx.this.mIsNeedWait = false;
                synchronized (HwActivityStarterEx.this.mService.getGlobalLock()) {
                    HwActivityStarterEx.this.mService.getGlobalLock().notifyAll();
                }
            }
        }, "StopPkgForMultiDisplay").start();
        while (this.mIsNeedWait) {
            try {
                this.mService.getGlobalLock().wait(10);
            } catch (InterruptedException e) {
                HwPCUtils.log(TAG, "forceStopPackageSync, Failed to mGlobalLock.wait.");
            }
        }
        HwPCUtils.log(TAG, "forceStopPackageSync, exit.");
    }

    public void handleFreeFormStackIfNeed(ActivityRecord startActivity) {
        ActivityStack freeFormStack;
        if (HwFreeFormUtils.isFreeFormEnable() && (freeFormStack = this.mService.getRootActivityContainer().getStack(5, 1)) != null) {
            ActivityRecord topActivity = freeFormStack.topRunningActivityLocked();
            if (topActivity != null && startActivity.launchedFromPackage != null && startActivity.launchedFromPackage.equals(topActivity.packageName)) {
                HwFreeFormUtils.log(TAG, "start activity:" + startActivity + " from:" + startActivity.launchedFromPackage + " in pkg:" + startActivity.packageName + " freeform topActivity.pkg:" + topActivity.packageName);
                if (freeFormStack.getCurrentPkgUnderFreeForm().equals(startActivity.packageName)) {
                    HwFreeFormUtils.log(TAG, "launch under-freeform app from the same app as freeform-app in fullscreen");
                } else if (!topActivity.packageName.equals(startActivity.packageName)) {
                    freeFormStack.setCurrentPkgUnderFreeForm("");
                    freeFormStack.setFreeFormStackVisible(false);
                    ActivityDisplay defaultDisplay = this.mService.mRootActivityContainer.getDefaultDisplay();
                    ActivityStack toStack = defaultDisplay != null ? defaultDisplay.getTopStackInWindowingMode(1) : null;
                    if (toStack == null) {
                        HwFreeFormUtils.log(TAG, "toStack is null, interrupt move freeform");
                        return;
                    }
                    HwFreeFormUtils.log(TAG, "move freeform to fullscreen for launch other app from freeform stack");
                    topActivity.getTaskRecord().reparent(toStack, true, 1, true, false, "exitFreeformMode");
                }
            } else if (topActivity != null && topActivity.packageName.equals(startActivity.packageName) && startActivity.getWindowingMode() == 1) {
                HwFreeFormUtils.log(TAG, "keep freeform for launch the same app as freeform-app in fullscreen stack");
            } else if (!TextUtils.isEmpty(freeFormStack.getCurrentPkgUnderFreeForm()) && freeFormStack.getCurrentPkgUnderFreeForm() != null && !freeFormStack.getCurrentPkgUnderFreeForm().equals(startActivity.packageName) && !startActivity.inFreeformWindowingMode()) {
                freeFormStack.setFreeFormStackVisible(false);
                if (startActivity.getActivityType() != 3) {
                    freeFormStack.setCurrentPkgUnderFreeForm("");
                    freeFormStack.finishAllActivitiesLocked(true);
                }
                HwFreeFormUtils.log(TAG, "remove freeform for launch app from no-freeform stack");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0063 A[Catch:{ RemoteException -> 0x0074 }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x006d A[Catch:{ RemoteException -> 0x0074 }] */
    public void moveFreeFormToFullScreenStackIfNeed(ActivityRecord startActivity, boolean isInFreeformWindowingMode) {
        ActivityStack freeFormStack;
        boolean isNull;
        if (HwFreeFormUtils.isFreeFormEnable() && (freeFormStack = this.mService.mRootActivityContainer.getStack(5, 1)) != null && freeFormStack.getFreeFormStackVisible()) {
            String activityTitle = startActivity.toString();
            boolean isNeedExit = false;
            for (String str : HwFreeFormUtils.sExitFreeformActivity) {
                if (activityTitle.contains(str)) {
                    isNeedExit = true;
                }
            }
            ActivityRecord topActivity = freeFormStack.topRunningActivityLocked();
            if (isInFreeformWindowingMode && isNeedExit) {
                if (topActivity != null) {
                    try {
                        if (topActivity.task != null) {
                            ActivityRecord rootActivity = topActivity.task.getRootActivity();
                            if (!(rootActivity == null || rootActivity.app == null)) {
                                if (rootActivity.app.mThread != null) {
                                    isNull = false;
                                    if (isNull) {
                                        rootActivity.app.mThread.scheduleRestoreFreeFormConfig(rootActivity.appToken);
                                    } else {
                                        HwFreeFormUtils.log(TAG, "restoreFreeFormConfig failed : no rootActivity");
                                    }
                                }
                            }
                            isNull = true;
                            if (isNull) {
                            }
                        }
                    } catch (RemoteException e) {
                        HwFreeFormUtils.log(TAG, "scheduleRestoreFreeFormConfig error!");
                    }
                }
                freeFormStack.setCurrentPkgUnderFreeForm("");
                freeFormStack.setFreeFormStackVisible(false);
                HwFreeFormUtils.log(TAG, "launch some activity from freeform but move to fullscreen");
                this.mService.mHwATMSEx.toggleFreeformWindowingModeEx(topActivity);
                freeFormStack.setWindowingMode(1);
            }
        }
    }

    public void preloadApplication(ApplicationInfo appInfo, String callingPackage) {
        if (IS_PRELOADAPP_EN && appInfo != null && callingPackage != null && !callingPackage.equals(appInfo.packageName) && "com.huawei.android.launcher".equals(callingPackage) && this.mService.getProcessController(appInfo.processName, appInfo.uid) == null) {
            this.mService.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$rk4xJMbKRM1lLgjaIamkhpdvq98.INSTANCE, this.mService.mAmInternal, appInfo));
        }
    }

    public boolean isAppDisabledByMdmNoComponent(ActivityInfo activityInfo, Intent intent, String resolvedType, ActivityStackSupervisor supervisor) {
        boolean isComponentExist = false;
        if (intent == null || supervisor == null || intent.getComponent() != null) {
            return false;
        }
        boolean isMdmDisable = false;
        ResolveInfo infoTemp = supervisor.resolveIntent(intent, resolvedType, activityInfo != null && activityInfo.applicationInfo != null ? UserHandle.getUserId(activityInfo.applicationInfo.uid) : 0, 131584, Binder.getCallingUid());
        if (!(!((infoTemp == null || infoTemp.activityInfo == null) ? false : true) || infoTemp.activityInfo.packageName == null || infoTemp.activityInfo.name == null)) {
            isComponentExist = true;
        }
        if (isComponentExist) {
            isMdmDisable = HwDeviceManager.mdmDisallowOp(21, new Intent().setComponent(new ComponentName(infoTemp.activityInfo.packageName, infoTemp.activityInfo.name)));
        }
        if (isMdmDisable) {
            UiThread.getHandler().post(new Runnable() {
                /* class com.android.server.wm.$$Lambda$HwActivityStarterEx$1jam1QK55C3XWv6UHzke5WpxMGo */

                @Override // java.lang.Runnable
                public final void run() {
                    HwActivityStarterEx.this.lambda$isAppDisabledByMdmNoComponent$3$HwActivityStarterEx();
                }
            });
            Log.i(TAG, "Application is disabled by MDM, intent component is null.");
        }
        return isMdmDisable;
    }

    public /* synthetic */ void lambda$isAppDisabledByMdmNoComponent$3$HwActivityStarterEx() {
        Toast.makeText(this.mService.mContext, this.mService.mContext.getResources().getString(33685904), 0).show();
    }
}
