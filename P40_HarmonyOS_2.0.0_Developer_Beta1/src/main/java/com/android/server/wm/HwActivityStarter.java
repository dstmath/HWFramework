package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.app.WaitResult;
import android.common.HwFrameworkFactory;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.hdm.HwDeviceManager;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.widget.Toast;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.HwBluetoothBigDataService;
import com.android.server.UiThread;
import com.android.server.am.PendingIntentRecord;
import com.android.server.cust.am.HwCustFwkActivityStarter;
import com.android.server.cust.utils.HwCustPkgNameConstant;
import com.android.server.multiwin.HwMultiWinConstants;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.aod.AodThemeConst;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HwActivityStarter extends ActivityStarter {
    private static final int CUST_HOME_SCREEN_OFF = 0;
    private static final int CUST_HOME_SCREEN_ON = 1;
    private static final ComponentName DOCOMOHOME_COMPONENT = new ComponentName("com.nttdocomo.android.dhome", "com.nttdocomo.android.dhome.HomeActivity");
    private static final ComponentName DRAWERHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.drawer.DrawerLauncher");
    private static final Set<String> EXPECT_PACKAGE_NAMES = new HashSet<String>() {
        /* class com.android.server.wm.HwActivityStarter.AnonymousClass2 */

        {
            add("com.huawei.android.launcher");
            add(PackageManagerServiceEx.PLATFORM_PACKAGE_NAME);
            add(HwCustPkgNameConstant.HW_SYSTEMUI_PACKAGE);
            add(WifiProCommonUtils.WIFI_SETTINGS_PHONE);
            add(HwMultiWinConstants.DOCKBAR_PACKAGE_NAME);
        }
    };
    private static final String FILE_STR = "file";
    private static final Set<ComponentName> HOME_COMPONENTS = new HashSet<ComponentName>() {
        /* class com.android.server.wm.HwActivityStarter.AnonymousClass1 */

        {
            if (HwActivityStarter.IS_SHOW_CUST_HOME_SCREEN) {
                add(HwActivityStarter.DOCOMOHOME_COMPONENT);
                return;
            }
            add(HwActivityStarter.UNIHOME_COMPONENT);
            add(HwActivityStarter.DRAWERHOME_COMPONENT);
            add(HwActivityStarter.SIMPLEHOME_COMPONENT);
            add(HwActivityStarter.NEWSIMPLEHOME_COMPONENT);
        }
    };
    private static final String HW_LAUNCHER_PKG_NAME = "com.huawei.android.launcher";
    private static final String INTENT_FORWARD_USER_ID = "intent_forward_user_id";
    private static final boolean IS_BOPD = SystemProperties.getBoolean("sys.bopd", false);
    private static final boolean IS_SHOW_CUST_HOME_SCREEN;
    private static final ComponentName NEWSIMPLEHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.newsimpleui.NewSimpleLauncher");
    private static final int PROVISIONED_OFF = 0;
    private static final ComponentName SIMPLEHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.simpleui.SimpleUILauncher");
    private static final String TAG = "ActivityStarter";
    private static final ComponentName UNIHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.unihome.UniHomeLauncher");
    private static Map<Integer, Boolean> sLauncherStartStates = new HashMap();
    private static Set<String> sPcPkgNames = new HashSet();
    private static Set<String> sSkipCancelResults = new HashSet();
    private HwCustFwkActivityStarter mHwCustFwkActivityStarter = new HwCustFwkActivityStarter();

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.config.show_cust_homescreen", 0) != 1) {
            z = false;
        }
        IS_SHOW_CUST_HOME_SCREEN = z;
        sSkipCancelResults.add(ActivityStartInterceptorBridge.getAppLock());
        sSkipCancelResults.add(ActivityStartInterceptorBridge.getAppOpaqueLock());
        sPcPkgNames.add("com.huawei.android.hwpay");
        sPcPkgNames.add(HwBluetoothBigDataService.BIGDATA_RECEIVER_PACKAGENAME);
        sPcPkgNames.add("com.huawei.screenrecorder");
    }

    public HwActivityStarter(ActivityStartController controller, ActivityTaskManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
        super(controller, service, supervisor, interceptor);
    }

    private void forceValidateHomeButton(int userId) {
        if (Settings.Secure.getIntForUser(this.mService.mContext.getContentResolver(), "user_setup_complete", 0, userId) == 0 || Settings.Global.getInt(this.mService.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
            Settings.Global.putInt(this.mService.mContext.getContentResolver(), "device_provisioned", 1);
            Settings.Secure.putIntForUser(this.mService.mContext.getContentResolver(), "user_setup_complete", 1, userId);
            Log.w(TAG, "DEVICE_PROVISIONED or USER_SETUP_COMPLETE set 0 to 1!");
        }
    }

    private boolean isStartLauncherActivity(Intent intent, int userId) {
        if (intent == null) {
            Log.w(TAG, "intent is null, not start launcher!");
            return false;
        }
        PackageManager pm = this.mService.mContext.getPackageManager();
        Intent mainIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
        ComponentName cmp = intent.getComponent();
        if (pm != null && intent.hasCategory("android.intent.category.HOME")) {
            long origId = Binder.clearCallingIdentity();
            try {
                ResolveInfo info = pm.resolveActivityAsUser(mainIntent, 0, userId);
                if (info == null || info.priority != 0 || cmp == null || info.activityInfo == null || !cmp.getPackageName().equals(info.activityInfo.packageName)) {
                    Binder.restoreCallingIdentity(origId);
                } else {
                    Log.d(TAG, "info priority is 0, cmp: " + cmp + ", userId: " + userId);
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo activityInfo, ResolveInfo resolveInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, SafeActivityOptions options, boolean isIgnoreTargetSecurity, boolean isComponentSpecified, ActivityRecord[] outActivity, TaskRecord inTask, String reason, boolean isAllowPendingRemoteAnimationRegistryLookup, PendingIntentRecord originatingPendingIntent, boolean isAllowBackgroundActivityStart) {
        if (intent.getComponent() != null && HwDeviceManager.disallowOp(4, intent.getComponent().getPackageName())) {
            return showToastDisallowedByMdmApk(intent);
        }
        if (isMultiWindowDisabled()) {
            if (isHwMultiWindowMode(options, intent, activityInfo, caller)) {
                return showToastDisallowedByMdmApk(intent);
            }
        }
        int custStartResult = this.mHwCustFwkActivityStarter.getPreventStartStatus(intent);
        if (custStartResult != 0) {
            Log.i(TAG, "prevent activity start by HwCustFwkActivityStarter");
            return custStartResult;
        } else if (isApplicationDisabledByMdm(activityInfo, intent, resolvedType)) {
            Flog.i(101, "Application is disabled by MDM, intent = " + intent);
            return -92;
        } else {
            int userId = activityInfo != null ? UserHandle.getUserId(activityInfo.applicationInfo.uid) : 0;
            if ((!sLauncherStartStates.containsKey(Integer.valueOf(userId)) || !sLauncherStartStates.get(Integer.valueOf(userId)).booleanValue()) && isFrpRestricted(this.mService.mContext, userId) && userId == 0 && activityInfo != null && isFrpRestrictedApp(this.mService.mContext, intent, activityInfo, userId)) {
                return sendFrpRestrictedBroadcast(activityInfo);
            }
            if (activityInfo != null) {
                HwStartWindowRecord.getInstance().setStartFromMainAction(Integer.valueOf(activityInfo.applicationInfo.uid), "android.intent.action.MAIN".equals(intent.getAction()));
            }
            if ((!sLauncherStartStates.containsKey(Integer.valueOf(userId)) || !sLauncherStartStates.get(Integer.valueOf(userId)).booleanValue()) && isStartLauncherActivity(intent, userId)) {
                Slog.w(TAG, "check the USER_SETUP_COMPLETE is set 1 in first start launcher!");
                forceValidateHomeButton(userId);
                sLauncherStartStates.put(Integer.valueOf(userId), true);
            }
            int startResult = HwActivityStarter.super.startActivity(caller, intent, ephemeralIntent, resolvedType, activityInfo, resolveInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, isIgnoreTargetSecurity, isComponentSpecified, outActivity, inTask, reason, isAllowPendingRemoteAnimationRegistryLookup, originatingPendingIntent, isAllowBackgroundActivityStart);
            pcSetScreenPower(startResult, outActivity);
            return startResult;
        }
    }

    private int sendFrpRestrictedBroadcast(ActivityInfo activityInfo) {
        Intent intentBroadcast = new Intent();
        intentBroadcast.setAction("com.huawei.action.frp_activity_restricted");
        intentBroadcast.putExtra("comp", activityInfo.getComponentName());
        this.mService.mH.post(new Runnable(intentBroadcast) {
            /* class com.android.server.wm.$$Lambda$HwActivityStarter$e6aEtGx3YirQhqD047ZnS3yuEFE */
            private final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwActivityStarter.this.lambda$sendFrpRestrictedBroadcast$0$HwActivityStarter(this.f$1);
            }
        });
        Log.w(TAG, "forbid launching Apps becasue of FRP, this App:" + activityInfo);
        return 0;
    }

    public /* synthetic */ void lambda$sendFrpRestrictedBroadcast$0$HwActivityStarter(Intent intentBroadcast) {
        this.mService.mContext.sendBroadcast(intentBroadcast, "com.huawei.android.permission.ANTITHEFT");
    }

    private void pcSetScreenPower(int startResult, ActivityRecord[] outActivity) {
        try {
            if (!ActivityManager.isStartResultSuccessful(startResult)) {
                return;
            }
            if ((HwPCUtils.isPcCastModeInServer() || HwPCUtils.isInWindowsCastMode() || HwPCUtils.isDisallowLockScreenForHwMultiDisplay()) && !HwPCUtils.enabledInPad()) {
                for (ActivityRecord r : outActivity) {
                    if (r != null) {
                        if (!HwPCUtils.isInWindowsCastMode() || "com.android.incallui".equals(r.packageName) || ActivityStartInterceptorBridge.isAppLockActivity(r.shortComponentName)) {
                            if (r.getWindowingMode() != 1) {
                                if (r.getActivityType() == 2 && r.fullscreen) {
                                }
                            }
                            HwPCUtils.getHwPCManager().setScreenPower(true);
                            return;
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            HwPCUtils.log(TAG, "startActivityLocked error");
        }
    }

    private int showToastDisallowedByMdmApk(Intent intent) {
        Flog.i(101, "[" + intent.getComponent().getPackageName() + "] is disallowed running by MDM apk");
        UiThread.getHandler().post(new Runnable() {
            /* class com.android.server.wm.HwActivityStarter.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                Context context = HwActivityStarter.this.mService.mUiContext;
                if (context != null) {
                    Toast toast = Toast.makeText(context, context.getString(33686043), 0);
                    toast.getWindowParams().privateFlags |= 16;
                    toast.show();
                }
            }
        });
        return -96;
    }

    private boolean isApplicationDisabledByMdm(ActivityInfo activityInfo, Intent intent, String resolvedType) {
        boolean isMdmDisabnled = false;
        if (intent.getComponent() == null) {
            Log.i(TAG, "isApplicationDisabledByMdm intent component is null");
        } else {
            isMdmDisabnled = HwDeviceManager.mdmDisallowOp(21, intent);
        }
        if (isMdmDisabnled) {
            UiThread.getHandler().post(new Runnable() {
                /* class com.android.server.wm.HwActivityStarter.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    Toast.makeText(HwActivityStarter.this.mService.mContext, HwActivityStarter.this.mService.mContext.getResources().getString(33685904), 0).show();
                }
            });
        }
        return isMdmDisabnled;
    }

    private void changeIntentForDifferentModeIfNeed(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories != null && categories.contains("android.intent.category.HOME")) {
            if (SystemProperties.getBoolean("sys.super_power_save", false)) {
                intent.removeCategory("android.intent.category.HOME");
                intent.addFlags(HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE);
                intent.setClassName("com.huawei.android.launcher", "com.huawei.android.launcher.powersavemode.PowerSaveModeLauncher");
            } else if (SystemProperties.getBoolean("sys.ride_mode", false)) {
                intent.removeCategory("android.intent.category.HOME");
                intent.addFlags(HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE);
                intent.setClassName("com.huawei.android.launcher", "com.huawei.android.launcher.streetmode.StreetModeLauncher");
            } else if (!IS_BOPD) {
            } else {
                if (Settings.Global.getInt(this.mService.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
                    Log.i(TAG, "failed to set activity as EmergencyBackupActivity for bopd due to oobe not finished.");
                    return;
                }
                intent.removeCategory("android.intent.category.HOME");
                intent.addFlags(HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE);
                intent.setComponent(new ComponentName(SystemProperties.get("sys.bopd.package.name", "com.huawei.KoBackup"), SystemProperties.get("sys.bopd.activity.name", "com.huawei.KoBackup.EmergencyBackupActivity")));
                Log.i(TAG, "set activity as EmergencyBackupActivity in the mode of bopd successfully.");
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:52:0x017b */
    /* JADX WARN: Type inference failed for: r8v4 */
    /* JADX WARN: Type inference failed for: r8v6, types: [com.android.server.wm.HwActivityStarter] */
    /* JADX WARN: Type inference failed for: r8v8 */
    /* JADX WARN: Type inference failed for: r8v22 */
    /* JADX WARN: Type inference failed for: r8v24 */
    /* JADX WARN: Type inference failed for: r8v27 */
    /* JADX WARN: Type inference failed for: r8v28 */
    /* JADX WARN: Type inference failed for: r8v29 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x025b, code lost:
        r18 = r32;
        r19 = r7;
        r10 = r8;
        r8 = r15;
        r15 = r31;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x026b, code lost:
        if (r0.activityInfo == null) goto L_0x0383;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x026d, code lost:
        if (r29 == null) goto L_0x0283;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0273, code lost:
        r2 = r29.mInfo.packageName;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x0275, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0283, code lost:
        r2 = r38;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0285, code lost:
        r18 = r32;
        r15 = r31;
        r10 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x029d, code lost:
        if (shouldDisplayClonedAppToChoose(r2, r31, r32, r0, r7, r0, r0) == false) goto L_0x0349;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x029f, code lost:
        r15.addHwFlags(2);
        r15.setComponent(new android.content.ComponentName(r0.activityInfo.packageName, r0.activityInfo.name));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x02b4, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:?, code lost:
        r10.standardizeIntentUriForClone(r15, r28, (android.os.storage.StorageManager) r10.mService.mContext.getSystemService("storage"));
        r3 = android.content.Intent.createChooser(r15, r10.mService.mContext.getResources().getText(17041505));
        r3.setFlags(r15.getFlags() & -536870913);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x02e6, code lost:
        if (r52 == null) goto L_0x0313;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:?, code lost:
        r0 = r52.getOptions(r10.mSupervisor);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x02ee, code lost:
        if (r0 == null) goto L_0x0313;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x02f8, code lost:
        if (android.app.WindowConfiguration.isHwMultiStackWindowingMode(r0.getLaunchWindowingMode()) == false) goto L_0x0313;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x02fa, code lost:
        r15.putExtra("android.activity.windowingMode", r0.getLaunchWindowingMode());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x030a, code lost:
        if (r0.getLaunchWindowingMode() != 102) goto L_0x0313;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x030c, code lost:
        r0.setLaunchWindowingMode(1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x0311, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x0313, code lost:
        r8 = 'e';
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x0316, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x032b, code lost:
        r8 = 'e';
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:?, code lost:
        android.util.Flog.e(101, "startActivityMayWait, fail to create chooser for " + r15, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x0330, code lost:
        if (r3 == null) goto L_0x0341;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:?, code lost:
        r0 = r3.resolveTypeIfNeeded(r10.mService.mContext.getContentResolver());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x033f, code lost:
        r15 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x0341, code lost:
        r0 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x0343, code lost:
        r18 = r0;
        r4 = r15;
        r8 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x0347, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x0349, code lost:
        r8 = 'e';
        r15.setHwFlags(r15.getHwFlags() & -3);
        r4 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x035e, code lost:
        if (r0.size() != 1) goto L_0x0370;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x0360, code lost:
        r5 = r0.get(com.android.server.wm.HwActivityStarter.INTENT_FORWARD_USER_ID).intValue();
        r2 = r18;
        r8 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x0370, code lost:
        r2 = r18;
        r5 = r7;
        r8 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x0375, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x0379, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x037c, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x0383, code lost:
        r18 = r32;
        r19 = r7;
        r10 = r8;
        r8 = r15;
        r15 = r31;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x038f, code lost:
        r4 = r15;
        r2 = r18;
        r5 = r19;
        r8 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x0394, code lost:
        if (r0 != false) goto L_0x0396;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:?, code lost:
        r4.setHwFlags(r4.getHwFlags() & -1025);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x03a0, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x03a7, code lost:
        if (r5 != r28) goto L_0x03a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x03a9, code lost:
        r4.prepareToLeaveUser(r28);
        r10.standardizeIntentUriForClone(r4, r28, (android.os.storage.StorageManager) r10.mService.mContext.getSystemService("storage"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:?, code lost:
        android.os.Binder.restoreCallingIdentity(r0);
        r29 = r2;
        r0 = r4;
        r30 = r5;
        r8 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x03c6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x03d3, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x03e0, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x03e5, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x03f3, code lost:
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:0x03f6, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x03f7, code lost:
        r0 = th;
        r8 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x03f9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x03fe, code lost:
        r3 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x0401, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:184:0x0402, code lost:
        r33 = 64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:185:0x0406, code lost:
        r3 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:186:0x0409, code lost:
        r33 = 64;
        r0 = r41;
        r29 = r42;
        r30 = r54;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x0419, code lost:
        r1 = com.android.server.wm.HwActivityStarter.super.startActivityMayWait(r36, r37, r38, r39, r40, r0, r29, r43, r44, r45, r46, r47, r48, r49, r50, r51, r52, r53, r30, r55, r56, r57, r58, r59);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:0x0451, code lost:
        if (com.android.server.wm.ActivityTaskManagerDebugConfig.DEBUG_HW_ACTIVITY != false) goto L_0x0453;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x0453, code lost:
        android.os.Trace.traceEnd(r33);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x0456, code lost:
        android.util.Flog.i(101, "startActivityMayWait cost " + (android.os.SystemClock.uptimeMillis() - r26));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x0473, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x0474, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:193:0x0475, code lost:
        r3 = 101;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x0493, code lost:
        android.os.Trace.traceEnd(r33);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0096, code lost:
        android.util.Flog.i(101, "startActivityMayWait, callerApp: " + r0 + ", intent: " + r41.toShortStringWithoutClip(true, true, true) + ", userId = " + r54 + ", callingUid = " + android.os.Binder.getCallingUid());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00d3, code lost:
        if (com.android.server.am.HwActivityManagerService.IS_SUPPORT_CLONE_APP == false) goto L_0x0409;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00dd, code lost:
        if (r35.mService.mUserManagerInternal.hasClonedProfile() == false) goto L_0x0409;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00df, code lost:
        if (r54 != 0) goto L_0x016b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00e5, code lost:
        if (r41.getComponent() == null) goto L_0x016b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00f3, code lost:
        if (com.android.server.pm.HwPackageManagerService.isSupportCloneAppInCust(r41.getComponent().getPackageName()) != false) goto L_0x015a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00f5, code lost:
        r33 = 64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r0 = com.android.server.wm.HwActivityStarter.super.startActivityMayWait(r36, r37, r38, r39, r40, r41, r42, r43, r44, r45, r46, r47, r48, r49, r50, r51, r52, r53, r54, r55, r56, r57, r58, r59);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0137, code lost:
        if (com.android.server.wm.ActivityTaskManagerDebugConfig.DEBUG_HW_ACTIVITY == false) goto L_0x013c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0139, code lost:
        android.os.Trace.traceEnd(64);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x013c, code lost:
        android.util.Flog.i(101, "startActivityMayWait cost " + (android.os.SystemClock.uptimeMillis() - r26));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0159, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x015a, code lost:
        r32 = r42;
        r31 = r41;
        r28 = r54;
        r29 = r0;
        r15 = 101;
        r33 = 64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0166, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0167, code lost:
        r33 = 64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x016b, code lost:
        r32 = r42;
        r31 = r41;
        r28 = r54;
        r29 = r0;
        r15 = 101;
        r33 = 64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:?, code lost:
        r0 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        r0 = new java.util.HashMap<>();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0189, code lost:
        if ((r31.getHwFlags() & 1024) == 0) goto L_0x018d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x018b, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x018d, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x018e, code lost:
        r8 = r35;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0194, code lost:
        r7 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        r0 = ((com.android.server.wm.HwActivityStarter) r8).mSupervisor.resolveIntent(r31, r32, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x019f, code lost:
        if (r0 != null) goto L_0x0252;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:?, code lost:
        r0 = ((com.android.server.wm.HwActivityStarter) r8).mService.mUserManagerInternal.getUserInfo(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x01a9, code lost:
        if (r0 == null) goto L_0x0238;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x01af, code lost:
        if (r0.isClonedProfile() == false) goto L_0x0238;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01b5, code lost:
        if (android.os.storage.StorageManager.isUserKeyUnlocked(r7) != false) goto L_0x0202;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01bd, code lost:
        if (android.os.storage.StorageManager.isUserKeyUnlocked(r0.profileGroupId) == false) goto L_0x0202;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x01c7, code lost:
        if (((com.android.server.wm.HwActivityStarter) r8).mService.mAmInternal.getHaveTryCloneProUserUnlock() == false) goto L_0x01d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x01c9, code lost:
        r8.showErrorDialogToRemoveUser(((com.android.server.wm.HwActivityStarter) r8).mService.mContext, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01d1, code lost:
        android.util.Slog.i(com.android.server.wm.HwActivityStarter.TAG, "Wait for CloneProfile user unLock, return!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:?, code lost:
        android.os.Binder.restoreCallingIdentity(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01de, code lost:
        if (com.android.server.wm.ActivityTaskManagerDebugConfig.DEBUG_HW_ACTIVITY == false) goto L_0x01e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01e0, code lost:
        android.os.Trace.traceEnd(r33);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01e3, code lost:
        android.util.Flog.i(r15, "startActivityMayWait cost " + (android.os.SystemClock.uptimeMillis() - r26));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01fe, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01ff, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0202, code lost:
        r1 = r0.profileGroupId;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:?, code lost:
        android.util.Flog.i(r15, "startActivityMayWait forward intent from clone user " + r0.id + " to parent user " + r1 + " because clone user has non target apps to respond.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0228, code lost:
        r7 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x022a, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0238, code lost:
        r2 = r32;
        r4 = r31;
        r5 = r7;
        r10 = r8;
        r8 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0244, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0254, code lost:
        if (r29 != null) goto L_0x0269;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0256, code lost:
        if (r38 != null) goto L_0x0269;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0258, code lost:
        if (r0 == false) goto L_0x025b;
     */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x0332  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x0341  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x0360  */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x0370  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0396 A[SYNTHETIC, Splitter:B:160:0x0396] */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x03a9 A[Catch:{ all -> 0x03a0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0453  */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x0493  */
    /* JADX WARNING: Unknown variable types count: 1 */
    public int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, int requestRealCallingPid, int requestRealCallingUid, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration globalConfig, SafeActivityOptions options, boolean isIgnoreTargetSecurity, int userId, TaskRecord inTask, String reason, boolean isAllowPendingRemoteAnimationRegistryLookup, PendingIntentRecord originatingPendingIntent, boolean isAllowBackgroundActivityStart) {
        long j;
        int i;
        UserInfo targetUser;
        Throwable th;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.ACTIVITYSTARTER_STARTACTIVITYMAYWAIT, new Object[]{intent, callingPackage});
        changeIntentForDifferentModeIfNeed(intent);
        long start = SystemClock.uptimeMillis();
        char c = 'e';
        r8 = 101;
        ?? r8 = 101;
        int i2 = 101;
        try {
            if (ActivityTaskManagerDebugConfig.DEBUG_HW_ACTIVITY) {
                Trace.traceBegin(64, "startActivityMayWait");
            }
            if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || !"com.huawei.android.projectmenu".equals(intent.getPackage())) {
                synchronized (this.mService.getGlobalLock()) {
                    try {
                        WindowProcessController callerApplication = this.mService.getProcessController(caller);
                        try {
                        } catch (Throwable th2) {
                            th = th2;
                            i = 101;
                            j = 64;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        i = 101;
                        j = 64;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
                try {
                    throw th;
                } catch (Throwable th5) {
                    targetUser = th5;
                }
            } else {
                if (Log.HWINFO) {
                    HwPCUtils.log("HwActivityStarter", "startActivityMayWait intent: " + intent);
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_HW_ACTIVITY) {
                    Trace.traceEnd(64);
                }
                Flog.i(101, "startActivityMayWait cost " + (SystemClock.uptimeMillis() - start));
                return 0;
            }
        } catch (Throwable th6) {
            targetUser = th6;
            i = 101;
            j = 64;
            if (ActivityTaskManagerDebugConfig.DEBUG_HW_ACTIVITY) {
            }
            Flog.i(i, "startActivityMayWait cost " + (SystemClock.uptimeMillis() - start));
            throw targetUser;
        }
    }

    private boolean shouldDisplayClonedAppToChoose(String callerPackageName, Intent intent, String resolvedType, ResolveInfo resolveInfo, int userId, Map<String, Integer> mapForwardUserId, boolean isShouldCheckDual) {
        UserInfo clonedProfile;
        if ((callerPackageName == null && !isShouldCheckDual) || startFromInExpectApps(callerPackageName) || resolveInfo.activityInfo.packageName.equals(callerPackageName) || (intent.getHwFlags() & 2) != 0) {
            return false;
        }
        UserInfo clonedProfile2 = null;
        if ((isShouldCheckDual || HwPackageManagerService.isSupportCloneAppInCust(callerPackageName)) && userId != 0) {
            clonedProfile2 = this.mService.mUserManagerInternal.findClonedProfile();
            if (clonePartialNeedRespond(clonedProfile2, userId, intent, resolvedType, resolveInfo, mapForwardUserId)) {
                return false;
            }
        }
        if (!HwPackageManagerService.isSupportCloneAppInCust(resolveInfo.activityInfo.packageName)) {
            return false;
        }
        if (clonedProfile2 == null) {
            clonedProfile = this.mService.mUserManagerInternal.findClonedProfile();
        } else {
            clonedProfile = clonedProfile2;
        }
        if (clonedProfile != null) {
            if (clonedProfile.id == userId || clonedProfile.profileGroupId == userId) {
                if (this.mSupervisor.resolveIntent(intent, resolvedType, clonedProfile.id) == null) {
                    return false;
                }
                if (callerPackageName == null) {
                    return true;
                }
                List<ResolveInfo> homeResolveInfos = new ArrayList<>();
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
                return true;
            }
        }
        return false;
    }

    private boolean clonePartialNeedRespond(UserInfo clonedProfile, int userId, Intent intent, String resolvedType, ResolveInfo resolveInfo, Map<String, Integer> mapForwardUserId) {
        ResolveInfo infoForParent;
        if (clonedProfile == null || clonedProfile.id != userId || (infoForParent = this.mSupervisor.resolveIntent(intent, resolvedType, clonedProfile.profileGroupId)) == null || infoForParent.activityInfo.getComponentName().equals(resolveInfo.activityInfo.getComponentName())) {
            return false;
        }
        mapForwardUserId.put(INTENT_FORWARD_USER_ID, Integer.valueOf(clonedProfile.profileGroupId));
        Flog.i(101, "startActivityMayWait forward intent from clone user " + clonedProfile.id + " to parent user " + clonedProfile.profileGroupId + " because clone user just has partial target apps to respond.");
        return true;
    }

    private boolean startFromInExpectApps(String callerPackageName) {
        return EXPECT_PACKAGE_NAMES.contains(callerPackageName);
    }

    /* access modifiers changed from: protected */
    public boolean standardizeHomeIntent(ResolveInfo resolveInfo, Intent intent) {
        if (resolveInfo == null || resolveInfo.activityInfo == null || intent == null || !HOME_COMPONENTS.contains(new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName, resolveInfo.activityInfo.name)) || isHomeIntent(intent)) {
            return false;
        }
        ComponentName cn = intent.getComponent();
        String packageName = cn != null ? cn.getPackageName() : intent.getPackage();
        intent.setComponent(null);
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        Set<String> tempCategories = intent.getCategories();
        if (tempCategories != null) {
            tempCategories.clear();
        }
        intent.addCategory("android.intent.category.HOME");
        if (IS_SHOW_CUST_HOME_SCREEN) {
            return true;
        }
        intent.setAction("android.intent.action.MAIN");
        return true;
    }

    private boolean isHomeIntent(Intent intent) {
        return (("android.intent.action.MAIN".equals(intent.getAction()) && intent.hasCategory("android.intent.category.HOME") && intent.getCategories().size() == 1) && intent.getData() == null && intent.getComponent() == null) && intent.getType() == null;
    }

    private void standardizeIntentUriForClone(Intent intent, int userId, StorageManager storageManager) {
        StorageVolume[] volumes;
        StorageVolume[] volumes2;
        ClipData clipData = intent.getClipData();
        String volume = null;
        String sdcard = Environment.getLegacyExternalStorageDirectory().getAbsolutePath();
        char c = 0;
        if (clipData != null) {
            int itemCount = clipData.getItemCount();
            int i = 0;
            while (i < itemCount) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                if (checkUriForClone(uri, sdcard)) {
                    if (volume == null) {
                        StorageVolume[] volumes3 = StorageManager.getVolumeList(userId, 512);
                        if (volumes3 == null || volumes3.length == 0) {
                            break;
                        }
                        volume = volumes3[c].getPath();
                    }
                    clipData.setItemAt(i, new ClipData.Item(item.getText(), item.getHtmlText(), item.getIntent(), Uri.parse(uri.toString().replace(sdcard, volume))));
                }
                i++;
                c = 0;
            }
        }
        Uri uri2 = intent.getData();
        if (checkUriForClone(uri2, sdcard)) {
            if (volume == null && (volumes2 = StorageManager.getVolumeList(userId, 512)) != null && volumes2.length > 0) {
                volume = volumes2[0].getPath();
            }
            if (volume != null) {
                intent.setData(Uri.parse(uri2.toString().replace(sdcard, volume)));
            }
        }
        Uri stream = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
        if (checkUriForClone(stream, sdcard)) {
            if (volume == null && (volumes = StorageManager.getVolumeList(userId, 512)) != null && volumes.length > 0) {
                volume = volumes[0].getPath();
            }
            if (volume != null) {
                intent.putExtra("android.intent.extra.STREAM", Uri.parse(stream.toString().replace(sdcard, volume)));
            }
        }
    }

    private boolean checkUriForClone(Uri uri, String sdcard) {
        String path;
        if (uri == null || !FILE_STR.equals(uri.getScheme()) || (path = uri.getPath()) == null || !path.startsWith(sdcard)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isInSkipCancelResultList(String clsName) {
        return sSkipCancelResults.contains(clsName);
    }

    private void showErrorDialogToRemoveUser(final Context context, final int userId) {
        UiThread.getHandler().post(new Runnable() {
            /* class com.android.server.wm.HwActivityStarter.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                AlertDialog errorDialog = new AlertDialog.Builder(context, 33947691).setPositiveButton(33685653, new DialogInterface.OnClickListener() {
                    /* class com.android.server.wm.HwActivityStarter.AnonymousClass5.AnonymousClass2 */

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        UserManager.get(context).removeUser(userId);
                    }
                }).setNegativeButton(17039360, new DialogInterface.OnClickListener() {
                    /* class com.android.server.wm.HwActivityStarter.AnonymousClass5.AnonymousClass1 */

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setTitle(context.getString(33685655)).setMessage(context.getString(33685654)).create();
                errorDialog.getWindow().setType(2003);
                errorDialog.getWindow().getAttributes().privateFlags |= 16;
                errorDialog.setCanceledOnTouchOutside(false);
                errorDialog.show();
                errorDialog.getButton(-1).setTextColor(-65536);
            }
        });
    }

    private boolean isFrpRestrictedApp(Context context, Intent intent, ActivityInfo activityInfo, int userId) {
        String frpToken = Settings.Secure.getStringForUser(context.getContentResolver(), "hw_frp_token", userId);
        String frpComps = Settings.Secure.getStringForUser(context.getContentResolver(), "hw_frp_comps", userId);
        if (!TextUtils.isEmpty(frpToken) || !TextUtils.isEmpty(frpComps)) {
            String frpCompsTemp = "," + frpComps + ",";
            if (!frpCompsTemp.contains("," + activityInfo.packageName + ",")) {
                if (!frpCompsTemp.contains("," + activityInfo.packageName + AodThemeConst.SPLASH + activityInfo.name + ",")) {
                    if (frpToken == null) {
                        return true;
                    }
                    try {
                        if (!frpToken.equals(intent.getStringExtra("hw_frp_token"))) {
                            return true;
                        }
                        Slog.i(TAG, activityInfo + " gets matched token in intent");
                        return false;
                    } catch (BadParcelableException e) {
                        Slog.e(TAG, "Parse extra failed!");
                        return false;
                    }
                }
            }
            Slog.i(TAG, activityInfo + " is in frp comps");
            return false;
        }
        Slog.i(TAG, "Frp items are Empty");
        return false;
    }

    private boolean isMultiWindowDisabled() {
        return this.mService.mHwATMSEx.getMultiWindowDisabled();
    }

    private boolean isHwMultiWindowMode(SafeActivityOptions options, Intent intent, ActivityInfo activityInfo, IApplicationThread caller) {
        ActivityOptions activityOptions;
        if (options == null || intent == null || activityInfo == null || caller == null || (activityOptions = options.getOptions(intent, activityInfo, this.mService.getProcessController(caller), this.mSupervisor)) == null) {
            return false;
        }
        int activityMode = activityOptions.getLaunchWindowingMode();
        if (activityMode != 100 && activityMode != 101 && activityMode != 102 && activityMode != 105 && activityMode != 103) {
            return false;
        }
        Slog.i(TAG, "isHwMultiWindowMode true activityMode: " + activityMode);
        return true;
    }

    /* access modifiers changed from: protected */
    public void setInitialState(ActivityRecord r, ActivityOptions options, TaskRecord inTask, boolean doResume, int startFlags, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean restrictedBgActivity) {
        if (HwMwUtils.ENABLED) {
            HwMwUtils.performPolicy(1, new Object[]{r, options, sourceRecord, this.mService.mLastResumedActivity});
        }
        HwActivityStarter.super.setInitialState(r, options, inTask, doResume, startFlags, sourceRecord, voiceSession, voiceInteractor, restrictedBgActivity);
    }
}
