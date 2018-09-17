package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.app.IActivityContainer;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.app.WaitResult;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.hdm.HwDeviceManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.rms.HwSysResManager;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.BatteryStatsImpl;
import com.android.server.HwBluetoothBigDataService;
import com.android.server.UiThread;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import huawei.android.os.HwAntiTheftManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HwActivityStarter extends ActivityStarter {
    private static final Intent BROWSE_PROBE = new Intent().setAction("android.intent.action.VIEW").addCategory("android.intent.category.BROWSABLE").setData(Uri.parse("http:"));
    private static final ComponentName DOCOMOHOME_COMPONENT = new ComponentName("com.nttdocomo.android.dhome", "com.nttdocomo.android.dhome.HomeActivity");
    private static final ComponentName DRAWERHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.drawer.DrawerLauncher");
    private static final String INTENT_FORWARD_USER_ID = "intent_forward_user_id";
    private static final boolean IS_SHOW_DCMUI = SystemProperties.getBoolean("ro.config.hw_show_dcmui", false);
    private static final ComponentName SIMPLEHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.simpleui.SimpleUILauncher");
    private static final String TAG = "ActivityStarter";
    private static final ComponentName UNIHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.unihome.UniHomeLauncher");
    private static final HashSet<ComponentName> sHomecomponent = new HashSet<ComponentName>() {
        {
            if (SystemProperties.getBoolean("ro.config.hw_show_dcmui", false)) {
                add(HwActivityStarter.DOCOMOHOME_COMPONENT);
                return;
            }
            add(HwActivityStarter.UNIHOME_COMPONENT);
            add(HwActivityStarter.DRAWERHOME_COMPONENT);
            add(HwActivityStarter.SIMPLEHOME_COMPONENT);
        }
    };
    private static Set<String> sPCPkgName = new HashSet();
    private static Set<String> sSkipCancelResultList = new HashSet();
    private boolean mIsStartupGuideFinished;

    static {
        sSkipCancelResultList.add("com.huawei.systemmanager/.applock.password.AuthLaunchLockedAppActivity");
        sPCPkgName.add("com.huawei.android.hwpay");
        sPCPkgName.add(HwBluetoothBigDataService.BIGDATA_RECEIVER_PACKAGENAME);
        sPCPkgName.add("com.huawei.screenrecorder");
    }

    public HwActivityStarter(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        super(service, supervisor);
    }

    private boolean isFakeForegroundAppStartActivity(String callingPackage, Intent intent, String resolvedType, int userId) {
        BatteryStatsImpl stats = this.mService.mBatteryStatsService.getActiveStatistics();
        long identityToken1 = Binder.clearCallingIdentity();
        if (stats.isScreenOn() || !HwSysResManager.getInstance().isEnableFakeForegroundControl()) {
            Binder.restoreCallingIdentity(identityToken1);
        } else {
            Binder.restoreCallingIdentity(identityToken1);
            ResolveInfo rInfo = null;
            try {
                rInfo = AppGlobals.getPackageManager().resolveIntent(intent, resolvedType, 66560, userId);
            } catch (RemoteException e) {
                Flog.e(101, "isFakeForegroundAppStartActivity, fail to get resolve information for " + intent, e);
            }
            ActivityInfo aInfo = rInfo != null ? rInfo.activityInfo : null;
            if (!(aInfo == null || aInfo.applicationInfo == null || callingPackage == null)) {
                if (callingPackage.equals(aInfo.applicationInfo.packageName)) {
                    String fakeForegroundString = callingPackage + "/" + aInfo.name;
                    long identityToken2 = Binder.clearCallingIdentity();
                    boolean isFakeForegroundAppActivity = HwSysResManager.getInstance().isFakeForegroundProcess(fakeForegroundString);
                    Binder.restoreCallingIdentity(identityToken2);
                    Flog.d(101, fakeForegroundString + " can't start when screen off: " + isFakeForegroundAppActivity);
                    if (isFakeForegroundAppActivity) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    final int startActivityLocked(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, ActivityContainer container, TaskRecord inTask, String reason) {
        if (intent.getComponent() != null && HwDeviceManager.disallowOp(4, intent.getComponent().getPackageName())) {
            Flog.i(101, "[" + intent.getComponent().getPackageName() + "] is disallowed running by MDM apk");
            UiThread.getHandler().post(new Runnable() {
                public void run() {
                    Context context = HwActivityStarter.this.mService.mUiContext;
                    if (context != null) {
                        Toast.makeText(context, context.getString(33685976), 0).show();
                    }
                }
            });
            return -96;
        } else if (isApplicationDisabledByMDM(aInfo, intent, resolvedType)) {
            Flog.i(101, "Application is disabled by MDM, intent = " + intent);
            return -92;
        } else {
            int userId = aInfo != null ? UserHandle.getUserId(aInfo.applicationInfo.uid) : 0;
            if (!(this.mLauncherStartState.containsKey(Integer.valueOf(userId)) ? ((Boolean) this.mLauncherStartState.get(Integer.valueOf(userId))).booleanValue() : false) && isFrpRestricted(this.mService.mContext, userId) && isStartBrowserApps(aInfo, userId)) {
                Log.w(TAG, "forbid launching browser because frp is restricted");
                return 0;
            }
            if (!this.mIsStartupGuideFinished) {
                Intent startUpIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT").setPackage("com.huawei.hwstartupguide");
                boolean isAntiTheftSupported = HwAntiTheftManager.getInstance().isAntiTheftSupported();
                boolean hasStartupGuide = this.mSupervisor.resolveIntent(startUpIntent, startUpIntent.resolveType(this.mService.mContext), userId) != null;
                if (isAntiTheftSupported) {
                    this.mIsStartupGuideFinished = (hasStartupGuide ? HwAntiTheftManager.getInstance().getAntiTheftEnabled() : 0) ^ 1;
                }
                if (!hasStartupGuide) {
                    this.mIsStartupGuideFinished = true;
                }
                if (isAntiTheftSupported && (this.mIsStartupGuideFinished ^ 1) != 0 && isStartBrowserApps(aInfo, userId)) {
                    Log.w(TAG, "forbid launching browser because startupguide is not finished");
                    return 0;
                }
            }
            int startResult = super.startActivityLocked(caller, intent, ephemeralIntent, resolvedType, aInfo, rInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, outActivity, container, inTask, reason);
            try {
                if (ActivityManager.isStartResultSuccessful(startResult) && HwPCUtils.isPcCastModeInServer() && (HwPCUtils.enabledInPad() ^ 1) != 0) {
                    for (ActivityRecord r : outActivity) {
                        if (r.getStackId() == 1 || (r.getStackId() == 0 && r.fullscreen)) {
                            HwPCUtils.getHwPCManager().setScreenPower(true);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                HwPCUtils.log(TAG, "startActivityLocked " + e);
            }
            return startResult;
        }
    }

    private boolean isApplicationDisabledByMDM(ActivityInfo aInfo, Intent intent, String resolvedType) {
        boolean mdmDisabnled = false;
        if (intent.getComponent() == null) {
            ResolveInfo info = this.mSupervisor.resolveIntent(intent, resolvedType, aInfo != null ? UserHandle.getUserId(aInfo.applicationInfo.uid) : 0, 131584);
            if (!(info == null || info.activityInfo == null)) {
                mdmDisabnled = HwDeviceManager.mdmDisallowOp(21, new Intent().setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name)));
            }
        } else {
            mdmDisabnled = HwDeviceManager.mdmDisallowOp(21, intent);
        }
        if (mdmDisabnled) {
            UiThread.getHandler().post(new Runnable() {
                public void run() {
                    Toast.makeText(HwActivityStarter.this.mService.mContext, HwActivityStarter.this.mService.mContext.getResources().getString(33685904), 0).show();
                }
            });
        }
        return mdmDisabnled;
    }

    int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration globalConfig, Bundle bOptions, boolean ignoreTargetSecurity, int userId, IActivityContainer iContainer, TaskRecord inTask, String reason) {
        if (isFakeForegroundAppStartActivity(callingPackage, intent, resolvedType, userId)) {
            return 0;
        }
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && intent != null && "com.huawei.android.projectmenu".equals(intent.getPackage())) {
            HwPCUtils.log("HwActivityStarter", "startActivityMayWait intent: " + intent);
            return 0;
        }
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP) {
            long ident = Binder.clearCallingIdentity();
            try {
                ProcessRecord callerApplication;
                Map<String, Integer> mapForwardUserId = new HashMap();
                int initialTargetUser = userId;
                synchronized (this.mService) {
                    callerApplication = this.mService.getRecordForAppLocked(caller);
                }
                Flog.i(101, "startActivityMayWait, callerApp: " + callerApplication + ", intent: " + intent + ", userId = " + userId + ", callingUid = " + Binder.getCallingUid());
                boolean shouldCheckDual = (intent.getHwFlags() & 1024) != 0;
                ResolveInfo rInfo = this.mSupervisor.resolveIntent(intent, resolvedType, userId);
                if (rInfo == null) {
                    UserInfo targetUser = this.mService.mUserController.mInjector.getUserManagerInternal().getUserInfo(userId);
                    if (targetUser != null && targetUser.isClonedProfile()) {
                        if (StorageManager.isUserKeyUnlocked(userId) || !StorageManager.isUserKeyUnlocked(targetUser.profileGroupId)) {
                            userId = targetUser.profileGroupId;
                            Flog.i(101, "startActivityMayWait forward intent from clone user " + targetUser.id + " to parent user " + userId + " because clone user has non target apps to respond.");
                        } else {
                            showErrorDialogToRemoveUser(this.mService.mContext, userId);
                            return 0;
                        }
                    }
                } else if (callerApplication != null || shouldCheckDual) {
                    if (rInfo.activityInfo != null) {
                        if (shouldDisplayClonedAppToChoose(callerApplication == null ? null : callerApplication.info.packageName, intent, resolvedType, rInfo, userId, mapForwardUserId, shouldCheckDual)) {
                            intent.addHwFlags(2);
                            intent.setComponent(new ComponentName(rInfo.activityInfo.packageName, rInfo.activityInfo.name));
                            Intent intent2 = null;
                            try {
                                standardizeIntentUriForClone(intent, userId);
                                intent2 = Intent.createChooser(intent, this.mService.mContext.getResources().getText(17041218));
                                intent2.setFlags(intent.getFlags() & -536870913);
                            } catch (Exception e) {
                                Flog.e(101, "startActivityMayWait, fail to create chooser for " + intent, e);
                            }
                            if (intent2 != null) {
                                intent = intent2;
                                resolvedType = intent.resolveTypeIfNeeded(this.mService.mContext.getContentResolver());
                            }
                        } else {
                            intent.setHwFlags(intent.getHwFlags() & -3);
                        }
                        if (mapForwardUserId.size() == 1) {
                            userId = ((Integer) mapForwardUserId.get(INTENT_FORWARD_USER_ID)).intValue();
                        }
                    }
                }
                if (shouldCheckDual) {
                    intent.setHwFlags(intent.getHwFlags() & -1025);
                }
                if (userId != initialTargetUser) {
                    intent.prepareToLeaveUser(initialTargetUser);
                    standardizeIntentUriForClone(intent, initialTargetUser);
                }
                Binder.restoreCallingIdentity(ident);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return super.startActivityMayWait(caller, callingUid, callingPackage, intent, resolvedType, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, startFlags, profilerInfo, outResult, globalConfig, bOptions, ignoreTargetSecurity, userId, iContainer, inTask, reason);
    }

    private boolean shouldDisplayClonedAppToChoose(String callerPackageName, Intent intent, String resolvedType, ResolveInfo rInfo, int userId, Map<String, Integer> mapForwardUserId, boolean shouldCheckDual) {
        if (callerPackageName == null && (shouldCheckDual ^ 1) != 0) {
            return false;
        }
        if ("com.huawei.android.launcher".equals(callerPackageName) || "android".equals(callerPackageName) || "com.android.systemui".equals(callerPackageName) || "com.android.settings".equals(callerPackageName)) {
            return false;
        }
        if (rInfo.activityInfo.packageName.equals(callerPackageName)) {
            return false;
        }
        if ((intent.getHwFlags() & 2) != 0) {
            return false;
        }
        UserInfo clonedProfile = null;
        if ((shouldCheckDual || HwPackageManagerService.isSupportCloneAppInCust(callerPackageName)) && userId != 0) {
            clonedProfile = this.mService.mUserController.mInjector.getUserManagerInternal().findClonedProfile();
            if (clonedProfile != null && clonedProfile.id == userId) {
                ResolveInfo infoForParent = this.mSupervisor.resolveIntent(intent, resolvedType, clonedProfile.profileGroupId);
                if (!(infoForParent == null || (infoForParent.activityInfo.getComponentName().equals(rInfo.activityInfo.getComponentName()) ^ 1) == 0)) {
                    mapForwardUserId.put(INTENT_FORWARD_USER_ID, Integer.valueOf(clonedProfile.profileGroupId));
                    Flog.i(101, "startActivityMayWait forward intent from clone user " + clonedProfile.id + " to parent user " + clonedProfile.profileGroupId + " because clone user just has partial target apps to respond.");
                    return false;
                }
            }
        }
        if (!HwPackageManagerService.isSupportCloneAppInCust(rInfo.activityInfo.packageName)) {
            return false;
        }
        if (clonedProfile == null) {
            clonedProfile = this.mService.mUserController.mInjector.getUserManagerInternal().findClonedProfile();
        }
        if (clonedProfile == null || (clonedProfile.id != userId && clonedProfile.profileGroupId != userId)) {
            return false;
        }
        if (this.mSupervisor.resolveIntent(intent, resolvedType, clonedProfile.id) == null) {
            return false;
        }
        if (callerPackageName != null) {
            List<ResolveInfo> homeResolveInfos = new ArrayList();
            try {
                AppGlobals.getPackageManager().getHomeActivities(homeResolveInfos);
            } catch (RemoteException e) {
                Flog.e(101, "Failed to getHomeActivities from PackageManager.", e);
            }
            for (ResolveInfo ri : homeResolveInfos) {
                if (callerPackageName.equals(ri.activityInfo.packageName)) {
                    return false;
                }
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:4:0x0008, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean standardizeHomeIntent(ResolveInfo rInfo, Intent intent) {
        if (rInfo == null || rInfo.activityInfo == null || intent == null || !sHomecomponent.contains(new ComponentName(rInfo.activityInfo.applicationInfo.packageName, rInfo.activityInfo.name)) || (isHomeIntent(intent) ^ 1) == 0) {
            return false;
        }
        ComponentName cn = intent.getComponent();
        String packageName = cn != null ? cn.getPackageName() : intent.getPackage();
        intent.setComponent(null);
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        Set<String> s = intent.getCategories();
        if (s != null) {
            s.clear();
        }
        intent.addCategory("android.intent.category.HOME");
        if (!IS_SHOW_DCMUI) {
            intent.setAction("android.intent.action.MAIN");
        }
        return true;
    }

    private boolean isHomeIntent(Intent intent) {
        if ("android.intent.action.MAIN".equals(intent.getAction()) && intent.hasCategory("android.intent.category.HOME") && intent.getCategories().size() == 1 && intent.getData() == null) {
            return intent.getType() == null;
        } else {
            return false;
        }
    }

    public boolean startingCustomActivity(boolean abort, Intent intent, ActivityInfo aInfo) {
        if (((HwActivityManagerService) this.mService).mCustomController != null) {
            return ((HwActivityManagerService) this.mService).customActivityStarting(intent, aInfo.applicationInfo.packageName);
        }
        return abort;
    }

    protected void setInitialState(ActivityRecord r, ActivityOptions options, TaskRecord inTask, boolean doResume, int startFlags, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        if (HwPCUtils.isPcCastModeInServer() && (HwPCUtils.enabledInPad() ^ 1) != 0 && options != null && HwPCUtils.isValidExtDisplayId(options.getLaunchDisplayId()) && (TextUtils.equals(r.packageName, "com.android.incallui") || TextUtils.equals(r.packageName, "com.android.systemui"))) {
            options.setLaunchDisplayId(0);
        }
        ActivityOptions activityOptions = options;
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && (this.mService instanceof HwActivityManagerService) && (((HwActivityManagerService) this.mService).isLauncher(r.packageName) ^ 1) != 0) {
            if (options == null) {
                activityOptions = ActivityOptions.makeBasic();
            }
            if (!HwPCUtils.isPcDynamicStack(activityOptions.getLaunchStackId()) && (activityOptions.getLaunchDisplayId() == -1 || activityOptions.getLaunchDisplayId() == 0)) {
                HwPCUtils.log("HwActivityStarter", "setInitialState r: " + r + ",ori displayId:" + activityOptions.getLaunchDisplayId() + ",ori stackId:" + activityOptions.getLaunchStackId());
                activityOptions.setLaunchDisplayId(HwPCUtils.getPCDisplayID());
                activityOptions.setLaunchStackId(-1);
                HwPCUtils.log("HwActivityStarter", "setInitialState r: " + r + ",set displayId:" + HwPCUtils.getPCDisplayID() + ",set stackId:" + -1);
            }
        }
        super.setInitialState(r, activityOptions, inTask, doResume, startFlags, sourceRecord, voiceSession, voiceInteractor);
    }

    protected int getSourceDisplayId(ActivityRecord sourceRecord, ActivityRecord startingActivity) {
        if (HwPCUtils.isPcCastModeInServer()) {
            if (this.mOptions != null && HwPCUtils.isValidExtDisplayId(this.mOptions.getLaunchDisplayId())) {
                return this.mOptions.getLaunchDisplayId();
            }
            if (startingActivity != null && (this.mService instanceof HwActivityManagerService)) {
                HashMap<String, Integer> maps = ((HwActivityManagerService) this.mService).mPkgDisplayMaps;
                int displayId = 0;
                if (TextUtils.isEmpty(startingActivity.launchedFromPackage)) {
                    if (!TextUtils.isEmpty(startingActivity.packageName) && maps.containsKey(startingActivity.packageName)) {
                        displayId = ((Integer) maps.get(startingActivity.packageName)).intValue();
                    }
                } else if (maps.containsKey(startingActivity.launchedFromPackage)) {
                    displayId = ((Integer) maps.get(startingActivity.launchedFromPackage)).intValue();
                }
                if (HwPCUtils.isValidExtDisplayId(displayId)) {
                    return displayId;
                }
            }
        }
        return super.getSourceDisplayId(sourceRecord, startingActivity);
    }

    public void killAllPCProcessesLocked() {
        ArrayList<ProcessRecord> procs = new ArrayList();
        int NP = this.mService.mProcessNames.getMap().size();
        for (int ip = 0; ip < NP; ip++) {
            SparseArray<ProcessRecord> apps = (SparseArray) this.mService.mProcessNames.getMap().valueAt(ip);
            int NA = apps.size();
            for (int ia = 0; ia < NA; ia++) {
                ProcessRecord proc = (ProcessRecord) apps.valueAt(ia);
                if (!(proc == this.mService.mHomeProcess || !HwPCUtils.isValidExtDisplayId(proc.mDisplayId) || "com.huawei.android.instantshare".equals(proc.processName))) {
                    procs.add(proc);
                }
            }
        }
        int N = procs.size();
        for (int i = 0; i < N; i++) {
            ((ProcessRecord) procs.get(i)).kill("HwPCUtils#DisplayRemoved", true);
        }
    }

    protected boolean hasStartedOnOtherDisplay(ActivityRecord startActivity, int sourceDisplayId) {
        if (!HwPCUtils.isPcCastModeInServer() || (sPCPkgName.contains(startActivity.packageName) ^ 1) == 0 || packageShouldNotHandle(startActivity.packageName)) {
            return false;
        }
        if (HwPCUtils.isValidExtDisplayId(sourceDisplayId) && startActivity.isHomeActivity()) {
            return true;
        }
        ArrayList<ProcessRecord> list = getPackageProcess(startActivity.packageName, startActivity.userId);
        if (list != null) {
            for (ProcessRecord pr : list) {
                if (pr != null && pr.mDisplayId != sourceDisplayId && pr.foregroundActivities) {
                    this.mService.mStackSupervisor.showToast(sourceDisplayId);
                    return true;
                }
            }
        }
        if (this.mService instanceof HwActivityManagerService) {
            ((HwActivityManagerService) this.mService).mPkgDisplayMaps.put(startActivity.packageName, Integer.valueOf(sourceDisplayId));
        }
        return false;
    }

    protected boolean killProcessOnDefaultDisplay(ActivityRecord startActivity) {
        if (HwPCUtils.enabled()) {
            boolean killPackageProcess = false;
            ArrayList<ProcessRecord> list = getPackageProcess(startActivity.packageName, startActivity.userId);
            int N = list.size();
            for (int i = 0; i < N; i++) {
                if (HwPCUtils.isValidExtDisplayId(((ProcessRecord) list.get(i)).mDisplayId)) {
                    killPackageProcess = true;
                    break;
                }
            }
            if ("com.huawei.android.instantshare".equals(startActivity.packageName)) {
                killPackageProcess = false;
            }
            if (killPackageProcess) {
                this.mService.forceStopPackageLocked(startActivity.packageName, UserHandle.getAppId(startActivity.appInfo.uid), false, false, true, false, false, UserHandle.getUserId(startActivity.appInfo.uid), "relaunch due to in diff display");
                return true;
            }
        }
        return false;
    }

    protected boolean killProcessOnOtherDisplay(ActivityRecord startActivity, int sourceDisplayId) {
        if (HwPCUtils.isPcCastModeInServer()) {
            if (packageShouldNotHandle(startActivity.packageName)) {
                return false;
            }
            boolean killPackageProcess = false;
            ArrayList<ProcessRecord> list = getPackageProcess(startActivity.packageName, startActivity.userId);
            int N = list.size();
            int i = 0;
            while (i < N) {
                if (((ProcessRecord) list.get(i)).mDisplayId != sourceDisplayId) {
                    if (HwPCUtils.enabledInPad() && "com.android.settings".equals(startActivity.packageName) && "com.android.phone".equals(((ProcessRecord) list.get(i)).processName)) {
                        HwPCUtils.log(TAG, "settings in phone process");
                    } else if (TextUtils.isEmpty(startActivity.packageName) || !startActivity.packageName.contains("com.tencent.mm") || startActivity.app == null || startActivity.app.pid == ((ProcessRecord) list.get(i)).pid) {
                        killPackageProcess = true;
                        break;
                    } else {
                        HwPCUtils.log(TAG, "pid is not same so dont kill the process when killing Process on other display");
                    }
                }
                i++;
            }
            if (sPCPkgName.contains(startActivity.packageName)) {
                killPackageProcess = false;
            }
            if (killPackageProcess) {
                this.mService.forceStopPackageLocked(startActivity.packageName, UserHandle.getAppId(startActivity.appInfo.uid), false, false, true, false, false, UserHandle.getUserId(startActivity.appInfo.uid), "relaunch due to in diff display");
                return true;
            }
        }
        return false;
    }

    private boolean packageShouldNotHandle(String pkgName) {
        boolean z = true;
        if (HwPCUtils.enabledInPad()) {
            if (!("com.huawei.desktop.explorer".equals(pkgName) || "com.huawei.desktop.systemui".equals(pkgName) || "com.android.systemui".equals(pkgName) || "com.android.incallui".equals(pkgName) || "com.huawei.android.wfdft".equals(pkgName))) {
                z = "com.huawei.android.instantshare".equals(pkgName);
            }
            return z;
        }
        if (!"com.huawei.desktop.explorer".equals(pkgName)) {
            z = "com.huawei.desktop.systemui".equals(pkgName);
        }
        return z;
    }

    private ArrayList<ProcessRecord> getPackageProcess(String pkg, int userId) {
        ArrayList<ProcessRecord> procs = new ArrayList();
        int NP = this.mService.mProcessNames.getMap().size();
        for (int ip = 0; ip < NP; ip++) {
            SparseArray<ProcessRecord> apps = (SparseArray) this.mService.mProcessNames.getMap().valueAt(ip);
            int NA = apps.size();
            for (int ia = 0; ia < NA; ia++) {
                ProcessRecord proc = (ProcessRecord) apps.valueAt(ia);
                if (proc.userId == userId && proc != this.mService.mHomeProcess && proc.pkgList.containsKey(pkg)) {
                    procs.add(proc);
                }
            }
        }
        return procs;
    }

    private void standardizeIntentUriForClone(Intent intent, int userId) {
        Uri uri;
        StorageManager storageManager;
        StorageVolume[] volumes;
        ClipData clipData = intent.getClipData();
        CharSequence charSequence = null;
        String sdcard = Environment.getLegacyExternalStorageDirectory().getAbsolutePath();
        if (clipData != null) {
            int itemCount = clipData.getItemCount();
            for (int i = 0; i < itemCount; i++) {
                Item item = clipData.getItemAt(i);
                uri = item.getUri();
                if (uri != null && MemoryConstant.MEM_PREREAD_FILE.equals(uri.getScheme()) && uri.getPath().startsWith(sdcard)) {
                    if (charSequence == null) {
                        storageManager = (StorageManager) this.mService.mContext.getSystemService("storage");
                        volumes = StorageManager.getVolumeList(userId, 512);
                        if (volumes == null || volumes.length == 0) {
                            break;
                        }
                        charSequence = volumes[0].getPath();
                    }
                    clipData.setItemAt(i, new Item(item.getText(), item.getHtmlText(), item.getIntent(), Uri.parse(uri.toString().replace(sdcard, charSequence))));
                }
            }
        }
        uri = intent.getData();
        if (uri != null && MemoryConstant.MEM_PREREAD_FILE.equals(uri.getScheme()) && uri.getPath().startsWith(sdcard)) {
            if (charSequence == null) {
                storageManager = (StorageManager) this.mService.mContext.getSystemService("storage");
                volumes = StorageManager.getVolumeList(userId, 512);
                if (volumes != null && volumes.length > 0) {
                    charSequence = volumes[0].getPath();
                }
            }
            if (charSequence != null) {
                intent.setData(Uri.parse(uri.toString().replace(sdcard, charSequence)));
            }
        }
        Uri stream = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
        if (stream != null && MemoryConstant.MEM_PREREAD_FILE.equals(stream.getScheme()) && stream.getPath().startsWith(sdcard)) {
            if (charSequence == null) {
                storageManager = (StorageManager) this.mService.mContext.getSystemService("storage");
                volumes = StorageManager.getVolumeList(userId, 512);
                if (volumes != null && volumes.length > 0) {
                    charSequence = volumes[0].getPath();
                }
            }
            if (charSequence != null) {
                intent.putExtra("android.intent.extra.STREAM", Uri.parse(stream.toString().replace(sdcard, charSequence)));
            }
        }
    }

    protected boolean isInSkipCancelResultList(String clsName) {
        return sSkipCancelResultList.contains(clsName);
    }

    private boolean isStartBrowserApps(ActivityInfo aInfo, int userId) {
        if (aInfo == null || aInfo.applicationInfo == null) {
            return false;
        }
        Set<String> specPkg = new HashSet();
        specPkg.add("com.google.android.setupwizard");
        specPkg.add("com.huawei.hwstartupguide");
        specPkg.add("com.android.settings");
        PackageManager pm = this.mService.mContext.getPackageManager();
        long origId = Binder.clearCallingIdentity();
        try {
            List<ResolveInfo> list = pm.queryIntentActivitiesAsUser(BROWSE_PROBE, 786432, userId);
            int count = list.size();
            for (int i = 0; i < count; i++) {
                ResolveInfo info = (ResolveInfo) list.get(i);
                if (!(info.activityInfo == null || info.activityInfo.getComponentName() == null || info.activityInfo.packageName == null || (info.handleAllWebDataURI ^ 1) != 0)) {
                    if (specPkg.contains(aInfo.packageName) && info.activityInfo.getComponentName().equals(aInfo.getComponentName())) {
                        Log.w(TAG, "skip launch aInfo:" + aInfo + " because browser info:" + info);
                        Binder.restoreCallingIdentity(origId);
                        return true;
                    } else if (!specPkg.contains(aInfo.packageName) && info.activityInfo.packageName.equals(aInfo.packageName)) {
                        Log.w(TAG, "skip launch aInfo:" + aInfo + " because browser app:" + aInfo.packageName);
                        return true;
                    }
                }
            }
            Binder.restoreCallingIdentity(origId);
            return false;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private void showErrorDialogToRemoveUser(final Context context, final int userId) {
        UiThread.getHandler().post(new Runnable() {
            public void run() {
                Builder builder = new Builder(context, 33947691);
                final Context context = context;
                final int i = userId;
                AlertDialog ErrorDialog = builder.setPositiveButton(33685943, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserManager.get(context).removeUser(i);
                    }
                }).setNegativeButton(17039360, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setTitle(context.getString(33685948)).setMessage(context.getString(33685947)).create();
                ErrorDialog.getWindow().setType(DeviceStatusConstant.MSDP_DEVICE_STATUS_MOVEMENT);
                LayoutParams attributes = ErrorDialog.getWindow().getAttributes();
                attributes.privateFlags |= 16;
                ErrorDialog.setCanceledOnTouchOutside(false);
                ErrorDialog.show();
                ErrorDialog.getButton(-1).setTextColor(-65536);
            }
        });
    }
}
