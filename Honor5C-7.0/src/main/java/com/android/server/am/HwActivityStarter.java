package com.android.server.am;

import android.app.AppGlobals;
import android.app.IActivityContainer;
import android.app.IActivityManager.WaitResult;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.rms.HwSysResManager;
import android.service.voice.IVoiceInteractionSession;
import android.util.Flog;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.BatteryStatsImpl;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HwActivityStarter extends ActivityStarter {
    private static final ComponentName DRAWERHOME_COMPONENT;
    private static final ComponentName SIMPLEHOME_COMPONENT;
    private static final ComponentName UNIHOME_COMPONENT;
    private static final HashSet<ComponentName> sHomecomponent;

    static {
        UNIHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.unihome.UniHomeLauncher");
        DRAWERHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.drawer.DrawerLauncher");
        SIMPLEHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.simpleui.SimpleUILauncher");
        sHomecomponent = new HashSet<ComponentName>() {
            {
                add(HwActivityStarter.UNIHOME_COMPONENT);
                add(HwActivityStarter.DRAWERHOME_COMPONENT);
                add(HwActivityStarter.SIMPLEHOME_COMPONENT);
            }
        };
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
                Flog.e(WifiProCommonDefs.TYEP_HAS_INTERNET, "isFakeForegroundAppStartActivity, fail to get resolve information for " + intent, e);
            }
            ActivityInfo activityInfo = rInfo != null ? rInfo.activityInfo : null;
            if (!(activityInfo == null || activityInfo.applicationInfo == null || callingPackage == null)) {
                if (callingPackage.equals(activityInfo.applicationInfo.packageName)) {
                    String fakeForegroundString = callingPackage + "/" + activityInfo.name;
                    long identityToken2 = Binder.clearCallingIdentity();
                    boolean isFakeForegroundAppActivity = HwSysResManager.getInstance().isFakeForegroundProcess(fakeForegroundString);
                    Binder.restoreCallingIdentity(identityToken2);
                    Flog.d(WifiProCommonDefs.TYEP_HAS_INTERNET, fakeForegroundString + " can't start when screen off: " + isFakeForegroundAppActivity);
                    if (isFakeForegroundAppActivity) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration config, Bundle bOptions, boolean ignoreTargetSecurity, int userId, IActivityContainer iContainer, TaskRecord inTask) {
        if (isFakeForegroundAppStartActivity(callingPackage, intent, resolvedType, userId)) {
            return 0;
        }
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP) {
            ProcessRecord callerApplication;
            synchronized (this.mService) {
                callerApplication = this.mService.getRecordForAppLocked(caller);
            }
            Flog.i(WifiProCommonDefs.TYEP_HAS_INTERNET, "startActivityMayWait, callerApp: " + callerApplication + ", intent: " + intent);
            if ((intent.getHwFlags() & 1) != 0) {
                startFlags |= 4194304;
            }
            ResolveInfo rInfo = null;
            try {
                rInfo = AppGlobals.getPackageManager().resolveIntent(intent, resolvedType, 66560, userId);
            } catch (RemoteException e) {
            }
            if (!(rInfo == null || rInfo.activityInfo == null || callerApplication == null)) {
                if (!this.mService.isPackageCloned(rInfo.activityInfo.packageName, userId)) {
                    intent.setHwFlags(intent.getHwFlags() & -2);
                    startFlags &= -4194305;
                } else if (!rInfo.activityInfo.packageName.equals(callerApplication.info.packageName)) {
                    boolean dual = (intent.getHwFlags() & 2) != 0;
                    if (dual) {
                        intent.setHwFlags(intent.getHwFlags() & -3);
                    }
                    if (shouldDisplayClonedAppToChoose(callerApplication.info.packageName) && !dual && (intent.getHwFlags() & 32) == 0) {
                        intent.addHwFlags(2);
                        intent.setComponent(new ComponentName(rInfo.activityInfo.packageName, rInfo.activityInfo.name));
                        Intent intent2 = null;
                        try {
                            intent2 = Intent.createChooser(intent, this.mService.mContext.getResources().getText(17040242));
                            intent2.setFlags(intent.getFlags() & -536870913);
                        } catch (Exception e2) {
                            Flog.e(WifiProCommonDefs.TYEP_HAS_INTERNET, "startActivityMayWait, fail to create chooser for " + intent, e2);
                        }
                        if (intent2 != null) {
                            intent = intent2;
                            intent.setHwFlags(intent.getHwFlags() & -2);
                            startFlags &= -4194305;
                            resolvedType = intent.resolveTypeIfNeeded(this.mService.mContext.getContentResolver());
                        }
                    }
                } else if (callerApplication.info.euid != 0) {
                    intent.addHwFlags(1);
                    startFlags |= 4194304;
                } else {
                    intent.setHwFlags(intent.getHwFlags() & -2);
                    startFlags &= -4194305;
                }
            }
        } else {
            intent.setHwFlags(intent.getHwFlags() & -2);
            startFlags &= -4194305;
        }
        return super.startActivityMayWait(caller, callingUid, callingPackage, intent, resolvedType, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, startFlags, profilerInfo, outResult, config, bOptions, ignoreTargetSecurity, userId, iContainer, inTask);
    }

    private boolean shouldDisplayClonedAppToChoose(String callerPackageName) {
        if (callerPackageName == null || "android".equals(callerPackageName) || "com.android.systemui".equals(callerPackageName)) {
            return false;
        }
        if (WifiProCommonUtils.HUAWEI_SETTINGS.equals(callerPackageName)) {
            return true;
        }
        List<ResolveInfo> homeResolveInfos = new ArrayList();
        try {
            AppGlobals.getPackageManager().getHomeActivities(homeResolveInfos);
        } catch (Exception e) {
            Flog.e(WifiProCommonDefs.TYEP_HAS_INTERNET, "Failed to getHomeActivities from PackageManager.", e);
        }
        for (ResolveInfo ri : homeResolveInfos) {
            if (callerPackageName.equals(ri.activityInfo.packageName)) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean standardizeHomeIntent(ResolveInfo rInfo, Intent intent) {
        if (rInfo == null || rInfo.activityInfo == null || intent == null || !sHomecomponent.contains(new ComponentName(rInfo.activityInfo.applicationInfo.packageName, rInfo.activityInfo.name)) || isHomeIntent(intent)) {
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
        intent.setAction("android.intent.action.MAIN");
        return true;
    }

    private boolean isHomeIntent(Intent intent) {
        if ("android.intent.action.MAIN".equals(intent.getAction()) && intent.hasCategory("android.intent.category.HOME") && intent.getCategories().size() == 1 && intent.getData() == null) {
            return intent.getType() == null;
        } else {
            return false;
        }
    }
}
