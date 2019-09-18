package com.android.server.am;

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
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hdm.HwDeviceManager;
import android.net.Uri;
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
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.widget.Toast;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.HwBluetoothBigDataService;
import com.android.server.UiThread;
import com.android.server.gesture.GestureNavConst;
import com.android.server.pc.HwPCDataReporter;
import com.android.server.pm.HwPackageManagerServiceEx;
import com.android.server.wm.HwStartWindowRecord;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.com.android.server.fingerprint.FingerViewController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HwActivityStarter extends ActivityStarter {
    private static final int CUST_HOME_SCREEN_OFF = 0;
    private static final int CUST_HOME_SCREEN_ON = 1;
    /* access modifiers changed from: private */
    public static final ComponentName DOCOMOHOME_COMPONENT = new ComponentName("com.nttdocomo.android.dhome", "com.nttdocomo.android.dhome.HomeActivity");
    /* access modifiers changed from: private */
    public static final ComponentName DRAWERHOME_COMPONENT = new ComponentName(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE, "com.huawei.android.launcher.drawer.DrawerLauncher");
    private static final String INTENT_FORWARD_USER_ID = "intent_forward_user_id";
    private static final boolean IS_BOPD = SystemProperties.getBoolean("sys.bopd", false);
    /* access modifiers changed from: private */
    public static final boolean IS_SHOW_CUST_HOME_SCREEN;
    /* access modifiers changed from: private */
    public static final ComponentName NEWSIMPLEHOME_COMPONENT = new ComponentName(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE, "com.huawei.android.launcher.newsimpleui.NewSimpleLauncher");
    /* access modifiers changed from: private */
    public static final ComponentName SIMPLEHOME_COMPONENT = new ComponentName(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE, "com.huawei.android.launcher.simpleui.SimpleUILauncher");
    private static final String TAG = "ActivityStarter";
    /* access modifiers changed from: private */
    public static final ComponentName UNIHOME_COMPONENT = new ComponentName(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE, "com.huawei.android.launcher.unihome.UniHomeLauncher");
    private static final HashSet<ComponentName> sHomecomponent = new HashSet<ComponentName>() {
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
    private static Set<String> sPCPkgName = new HashSet();
    private static Set<String> sSkipCancelResultList = new HashSet();
    private boolean mIsStartupGuideFinished;

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.config.show_cust_homescreen", 0) != 1) {
            z = false;
        }
        IS_SHOW_CUST_HOME_SCREEN = z;
        sSkipCancelResultList.add("com.huawei.systemmanager/.applock.password.AuthLaunchLockedAppActivity");
        sPCPkgName.add("com.huawei.android.hwpay");
        sPCPkgName.add(HwBluetoothBigDataService.BIGDATA_RECEIVER_PACKAGENAME);
        sPCPkgName.add("com.huawei.screenrecorder");
    }

    public HwActivityStarter(ActivityStartController controller, ActivityManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
        super(controller, service, supervisor, interceptor);
    }

    /* access modifiers changed from: package-private */
    public int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, SafeActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, TaskRecord inTask, String reason, boolean allowPendingRemoteAnimationRegistryLookup) {
        Intent intent2 = intent;
        ActivityInfo activityInfo = aInfo;
        ActivityRecord[] activityRecordArr = outActivity;
        if (intent2.getComponent() != null && HwDeviceManager.disallowOp(4, intent2.getComponent().getPackageName())) {
            Flog.i(101, "[" + intent2.getComponent().getPackageName() + "] is disallowed running by MDM apk");
            UiThread.getHandler().post(new Runnable() {
                public void run() {
                    Context context = HwActivityStarter.this.mService.mUiContext;
                    if (context != null) {
                        Toast toast = Toast.makeText(context, context.getString(33686142), 0);
                        toast.getWindowParams().privateFlags |= 16;
                        toast.show();
                    }
                }
            });
            return -96;
        } else if (isApplicationDisabledByMDM(activityInfo, intent2, resolvedType)) {
            Flog.i(101, "Application is disabled by MDM, intent = " + intent2);
            return -92;
        } else {
            int i = 0;
            int userId = activityInfo != null ? UserHandle.getUserId(activityInfo.applicationInfo.uid) : 0;
            if ((!mLauncherStartState.containsKey(Integer.valueOf(userId)) || !((Boolean) mLauncherStartState.get(Integer.valueOf(userId))).booleanValue()) && isFrpRestricted(this.mService.mContext, userId) && userId == 0 && activityInfo != null && isFrpRestrictedApp(this.mService.mContext, intent2, activityInfo, userId)) {
                Intent intentBroadcast = new Intent();
                intentBroadcast.setAction("com.huawei.action.frp_activity_restricted");
                intentBroadcast.putExtra("comp", aInfo.getComponentName());
                this.mService.mContext.sendBroadcast(intentBroadcast, "com.huawei.android.permission.ANTITHEFT");
                Log.w(TAG, "forbid launching Apps becasue of FRP, this App:" + activityInfo);
                return 0;
            }
            if (activityInfo != null) {
                HwStartWindowRecord.getInstance().setStartFromMainAction(Integer.valueOf(activityInfo.applicationInfo.uid), "android.intent.action.MAIN".equals(intent2.getAction()));
            }
            int startResult = HwActivityStarter.super.startActivity(caller, intent, ephemeralIntent, resolvedType, aInfo, rInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, outActivity, inTask, reason, allowPendingRemoteAnimationRegistryLookup);
            try {
                if (ActivityManager.isStartResultSuccessful(startResult) && HwPCUtils.isPcCastModeInServer() && !HwPCUtils.enabledInPad()) {
                    while (true) {
                        if (i >= activityRecordArr.length) {
                            break;
                        }
                        ActivityRecord r = activityRecordArr[i];
                        if (r.getWindowingMode() != 1) {
                            if (r.getActivityType() == 2 && r.fullscreen) {
                                break;
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                    HwPCUtils.getHwPCManager().setScreenPower(true);
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
            ResolveInfo info = this.mSupervisor.resolveIntent(intent, resolvedType, aInfo != null ? UserHandle.getUserId(aInfo.applicationInfo.uid) : 0, 131584, Binder.getCallingUid());
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

    private void changeIntentForDifferentModeIfNeed(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories != null && categories.contains("android.intent.category.HOME")) {
            if (SystemProperties.getBoolean("sys.super_power_save", false)) {
                intent.removeCategory("android.intent.category.HOME");
                intent.addFlags(4194304);
                intent.setClassName(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE, "com.huawei.android.launcher.powersavemode.PowerSaveModeLauncher");
            } else if (SystemProperties.getBoolean("sys.ride_mode", false)) {
                intent.removeCategory("android.intent.category.HOME");
                intent.addFlags(4194304);
                intent.setClassName(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE, "com.huawei.android.launcher.streetmode.StreetModeLauncher");
            } else if (IS_BOPD) {
                intent.removeCategory("android.intent.category.HOME");
                intent.addFlags(4194304);
                intent.setComponent(new ComponentName("com.huawei.KoBackup", "com.huawei.KoBackup.EmergencyBackupActivity"));
                Log.i(TAG, "set activity as hwBackup in the mode of bopd");
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x01f6, code lost:
        r13 = r1;
        r14 = r2;
        r27 = r4;
        r25 = r6;
        r29 = r12;
        r12 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0202, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x0203, code lost:
        r1 = r4;
        r25 = r6;
        r29 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x020b, code lost:
        if (r6 != null) goto L_0x0219;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x020d, code lost:
        if (r16 == false) goto L_0x0210;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0210, code lost:
        r13 = r1;
        r14 = r2;
        r27 = r4;
        r25 = r6;
        r12 = 101;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x021b, code lost:
        if (r11.activityInfo == null) goto L_0x02d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x021e, code lost:
        if (r6 != null) goto L_0x0222;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0220, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:?, code lost:
        r3 = r6.info.packageName;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0226, code lost:
        r13 = r1;
        r14 = r2;
        r27 = r4;
        r4 = r12;
        r25 = r6;
        r12 = 101;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x023a, code lost:
        if (shouldDisplayClonedAppToChoose(r3, r9, r4, r11, r10, r14, r16) == false) goto L_0x02a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x023c, code lost:
        r9.addHwFlags(2);
        r9.setComponent(new android.content.ComponentName(r11.activityInfo.packageName, r11.activityInfo.name));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0250, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:?, code lost:
        standardizeIntentUriForClone(r9, r13);
        r1 = android.content.Intent.createChooser(r9, r15.mService.mContext.getResources().getText(17041361));
        r1.setFlags(r35.getFlags() & -536870913);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x0274, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:?, code lost:
        android.util.Flog.e(101, "startActivityMayWait, fail to create chooser for " + r9, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:?, code lost:
        r9.setHwFlags(r35.getHwFlags() & -3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x02ac, code lost:
        r29 = r36;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x02c3, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x02c9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x02ca, code lost:
        r25 = r6;
        r1 = r4;
        r29 = r36;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x02d1, code lost:
        r13 = r1;
        r14 = r2;
        r27 = r4;
        r25 = r6;
        r12 = 101;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:146:0x02d8, code lost:
        r29 = r36;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x02e6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x02e7, code lost:
        r1 = r27;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x02fd, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x02fe, code lost:
        r1 = r4;
        r25 = r6;
        r29 = r36;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x0308, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x0309, code lost:
        r5 = r12;
        r3 = 64;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x030e, code lost:
        r25 = r6;
        r12 = 101;
        r24 = r9;
        r26 = r10;
        r29 = r36;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x0383, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        android.util.Flog.i(101, "startActivityMayWait, callerApp: " + r6 + ", intent: " + r9.toShortStringWithoutClip(true, true, true) + ", userId = " + r10 + ", callingUid = " + android.os.Binder.getCallingUid());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d1, code lost:
        if (com.android.server.am.HwActivityManagerService.IS_SUPPORT_CLONE_APP == false) goto L_0x030e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00e1, code lost:
        if (r15.mService.mUserController.mInjector.getUserManagerInternal().hasClonedProfile() == false) goto L_0x030e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e3, code lost:
        if (r10 != 0) goto L_0x0120;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00e9, code lost:
        if (r35.getComponent() == null) goto L_0x0120;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00f7, code lost:
        if (com.android.server.pm.HwPackageManagerServiceEx.isSupportCloneAppInCust(r35.getComponent().getPackageName()) != false) goto L_0x0120;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f9, code lost:
        r0 = com.android.server.am.HwActivityStarter.super.startActivityMayWait(r32, r33, r34, r35, r36, r37, r38, r39, r40, r41, r42, r43, r44, r45, r46, r47, r48, r49, r50, r51);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00ff, code lost:
        if (com.android.server.am.ActivityManagerDebugConfig.DEBUG_HW_ACTIVITY == false) goto L_0x0104;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0101, code lost:
        android.os.Trace.traceEnd(64);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0104, code lost:
        android.util.Flog.i(101, "startActivityMayWait cost " + (android.os.SystemClock.uptimeMillis() - r22));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x011f, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0124, code lost:
        r4 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        r2 = new java.util.HashMap<>();
        r1 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0132, code lost:
        if ((r35.getHwFlags() & 1024) == 0) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0134, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0136, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0137, code lost:
        r16 = r0;
        r12 = r36;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0141, code lost:
        r11 = r15.mSupervisor.resolveIntent(r9, r12, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0145, code lost:
        if (r11 != null) goto L_0x020b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:?, code lost:
        r0 = r15.mService.mUserController.mInjector.getUserManagerInternal().getUserInfo(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0155, code lost:
        if (r0 == null) goto L_0x01f6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x015b, code lost:
        if (r0.isClonedProfile() == false) goto L_0x01f6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0161, code lost:
        if (android.os.storage.StorageManager.isUserKeyUnlocked(r48) != false) goto L_0x01b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0169, code lost:
        if (android.os.storage.StorageManager.isUserKeyUnlocked(r0.profileGroupId) == false) goto L_0x01b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0171, code lost:
        if (r15.mService.mUserController.mHaveTryCloneProUserUnlock == false) goto L_0x017b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0173, code lost:
        showErrorDialogToRemoveUser(r15.mService.mContext, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:?, code lost:
        android.util.Slog.i(TAG, "Wait for CloneProfile user unLock, return!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:?, code lost:
        android.os.Binder.restoreCallingIdentity(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0188, code lost:
        if (com.android.server.am.ActivityManagerDebugConfig.DEBUG_HW_ACTIVITY == false) goto L_0x018d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x018a, code lost:
        android.os.Trace.traceEnd(64);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x018d, code lost:
        android.util.Flog.i(101, "startActivityMayWait cost " + (android.os.SystemClock.uptimeMillis() - r22));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01ab, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01ac, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01ad, code lost:
        r29 = r12;
        r3 = 64;
        r5 = 101;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:?, code lost:
        r3 = r0.profileGroupId;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:?, code lost:
        android.util.Flog.i(101, "startActivityMayWait forward intent from clone user " + r0.id + " to parent user " + r3 + " because clone user has non target apps to respond.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01dc, code lost:
        r8 = 101;
        r10 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x01df, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x01e0, code lost:
        r1 = r4;
        r25 = r6;
        r29 = r12;
        r10 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x01e9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x01ea, code lost:
        r10 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x01ec, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x01ed, code lost:
        r1 = r4;
        r25 = r6;
        r29 = r12;
     */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0390  */
    public int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration globalConfig, SafeActivityOptions options, boolean ignoreTargetSecurity, int userId, TaskRecord inTask, String reason, boolean allowPendingRemoteAnimationRegistryLookup) {
        int i;
        long j;
        boolean shouldCheckDual;
        long ident;
        int initialTargetUser;
        Map<String, Integer> mapForwardUserId;
        Intent targetIntent;
        String resolvedType2;
        Intent intent2 = intent;
        int userId2 = userId;
        int i2 = 1;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.ACTIVITYSTARTER_STARTACTIVITYMAYWAIT, new Object[]{intent2, callingPackage});
        changeIntentForDifferentModeIfNeed(intent2);
        long start = SystemClock.uptimeMillis();
        int i3 = 101;
        if (ActivityManagerDebugConfig.DEBUG_HW_ACTIVITY) {
            try {
                Trace.traceBegin(64, "startActivityMayWait");
            } catch (Throwable th) {
                targetUser = th;
                String str = resolvedType;
                i = 101;
                j = 64;
            }
        }
        try {
            if (HwPCUtils.enabledInPad()) {
                if (HwPCUtils.isPcCastModeInServer() && intent2 != null && "com.huawei.android.projectmenu".equals(intent.getPackage())) {
                    HwPCUtils.log("HwActivityStarter", "startActivityMayWait intent: " + intent2);
                    if (ActivityManagerDebugConfig.DEBUG_HW_ACTIVITY) {
                        Trace.traceEnd(64);
                    }
                    Flog.i(101, "startActivityMayWait cost " + (SystemClock.uptimeMillis() - start));
                    return 0;
                }
            }
            synchronized (this.mService) {
                try {
                    ProcessRecord callerApplication = this.mService.getRecordForAppLocked(caller);
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        i = 101;
                        j = 64;
                        ProcessRecord processRecord = callerApplication;
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
            if (targetIntent != null) {
                Intent intent3 = targetIntent;
                try {
                    resolvedType2 = intent3.resolveTypeIfNeeded(this.mService.mContext.getContentResolver());
                    intent2 = intent3;
                } catch (Throwable th5) {
                    th = th5;
                    Intent intent4 = intent3;
                    long ident2 = ident;
                    Binder.restoreCallingIdentity(ident2);
                    throw th;
                }
            } else {
                resolvedType2 = resolvedType;
            }
            String resolvedType3 = resolvedType2;
            if (mapForwardUserId.size() == 1) {
                userId2 = mapForwardUserId.get(INTENT_FORWARD_USER_ID).intValue();
            }
            if (shouldCheckDual) {
                intent2.setHwFlags(intent2.getHwFlags() & -1025);
            }
            if (userId2 != initialTargetUser) {
                intent2.prepareToLeaveUser(initialTargetUser);
                standardizeIntentUriForClone(intent2, initialTargetUser);
            }
            Binder.restoreCallingIdentity(ident);
            Intent intent5 = intent2;
            int userId3 = userId2;
            int i4 = i2;
            try {
                int startActivityMayWait = HwActivityStarter.super.startActivityMayWait(caller, callingUid, callingPackage, intent5, resolvedType3, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, startFlags, profilerInfo, outResult, globalConfig, options, ignoreTargetSecurity, userId3, inTask, reason, allowPendingRemoteAnimationRegistryLookup);
                if (ActivityManagerDebugConfig.DEBUG_HW_ACTIVITY) {
                    Trace.traceEnd(64);
                }
                Flog.i(101, "startActivityMayWait cost " + (SystemClock.uptimeMillis() - start));
                return startActivityMayWait;
            } catch (Throwable th6) {
                targetUser = th6;
                j = 64;
                i = 101;
                Intent intent6 = intent5;
                int i5 = userId3;
                if (ActivityManagerDebugConfig.DEBUG_HW_ACTIVITY) {
                }
                Flog.i(i, "startActivityMayWait cost " + (SystemClock.uptimeMillis() - start));
                throw targetUser;
            }
        } catch (Throwable th7) {
            targetUser = th7;
            i = 101;
            j = 64;
            if (ActivityManagerDebugConfig.DEBUG_HW_ACTIVITY) {
                Trace.traceEnd(j);
            }
            Flog.i(i, "startActivityMayWait cost " + (SystemClock.uptimeMillis() - start));
            throw targetUser;
        }
    }

    private boolean shouldDisplayClonedAppToChoose(String callerPackageName, Intent intent, String resolvedType, ResolveInfo rInfo, int userId, Map<String, Integer> mapForwardUserId, boolean shouldCheckDual) {
        if ((callerPackageName == null && !shouldCheckDual) || GestureNavConst.DEFAULT_LAUNCHER_PACKAGE.equals(callerPackageName) || "android".equals(callerPackageName) || FingerViewController.PKGNAME_OF_KEYGUARD.equals(callerPackageName) || "com.android.settings".equals(callerPackageName) || rInfo.activityInfo.packageName.equals(callerPackageName) || (intent.getHwFlags() & 2) != 0) {
            return false;
        }
        UserInfo clonedProfile = null;
        if ((shouldCheckDual || HwPackageManagerServiceEx.isSupportCloneAppInCust(callerPackageName)) && userId != 0) {
            clonedProfile = this.mService.mUserController.mInjector.getUserManagerInternal().findClonedProfile();
            if (clonedProfile != null && clonedProfile.id == userId) {
                ResolveInfo infoForParent = this.mSupervisor.resolveIntent(intent, resolvedType, clonedProfile.profileGroupId);
                if (infoForParent != null && !infoForParent.activityInfo.getComponentName().equals(rInfo.activityInfo.getComponentName())) {
                    mapForwardUserId.put(INTENT_FORWARD_USER_ID, Integer.valueOf(clonedProfile.profileGroupId));
                    Flog.i(101, "startActivityMayWait forward intent from clone user " + clonedProfile.id + " to parent user " + clonedProfile.profileGroupId + " because clone user just has partial target apps to respond.");
                    return false;
                }
            }
        }
        if (!HwPackageManagerServiceEx.isSupportCloneAppInCust(rInfo.activityInfo.packageName)) {
            return false;
        }
        if (clonedProfile == null) {
            clonedProfile = this.mService.mUserController.mInjector.getUserManagerInternal().findClonedProfile();
        }
        if (clonedProfile == null || ((clonedProfile.id != userId && clonedProfile.profileGroupId != userId) || this.mSupervisor.resolveIntent(intent, resolvedType, clonedProfile.id) == null)) {
            return false;
        }
        if (callerPackageName != null) {
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
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean standardizeHomeIntent(ResolveInfo rInfo, Intent intent) {
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
        if (!IS_SHOW_CUST_HOME_SCREEN) {
            intent.setAction("android.intent.action.MAIN");
        }
        return true;
    }

    private boolean isHomeIntent(Intent intent) {
        return "android.intent.action.MAIN".equals(intent.getAction()) && intent.hasCategory("android.intent.category.HOME") && intent.getCategories().size() == 1 && intent.getData() == null && intent.getComponent() == null && intent.getType() == null;
    }

    public boolean startingCustomActivity(boolean abort, Intent intent, ActivityInfo aInfo) {
        if (this.mService.mCustomController != null) {
            return this.mService.customActivityStarting(intent, aInfo.applicationInfo.packageName);
        }
        return abort;
    }

    /* access modifiers changed from: protected */
    public void setInitialState(ActivityRecord r, ActivityOptions options, TaskRecord inTask, boolean doResume, int startFlags, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        ActivityRecord activityRecord = r;
        ActivityOptions options2 = options;
        if (HwPCUtils.isPcCastModeInServer()) {
            if (!HwPCUtils.enabledInPad() && options2 != null && HwPCUtils.isValidExtDisplayId(options2.getLaunchDisplayId()) && (TextUtils.equals(activityRecord.packageName, "com.android.incallui") || TextUtils.equals(activityRecord.packageName, FingerViewController.PKGNAME_OF_KEYGUARD))) {
                options2.setLaunchDisplayId(0);
            }
            if (HwPCUtils.enabledInPad() && (this.mService instanceof HwActivityManagerService) && !this.mService.isLauncher(activityRecord.packageName) && options2 == null) {
                options2 = ActivityOptions.makeBasic();
            }
        }
        HwActivityStarter.super.setInitialState(activityRecord, options2, inTask, doResume, startFlags, sourceRecord, voiceSession, voiceInteractor);
    }

    /* access modifiers changed from: protected */
    public int getPreferedDisplayId(ActivityRecord sourceRecord, ActivityRecord startingActivity, ActivityOptions options) {
        if (HwPCUtils.isPcCastModeInServer()) {
            if (startingActivity != null && startingActivity.isActivityTypeHome() && "android".equals(startingActivity.launchedFromPackage)) {
                return 0;
            }
            if (HwPCUtils.enabledInPad()) {
                return HwPCUtils.getPCDisplayID();
            }
            if (options != null && HwPCUtils.isValidExtDisplayId(options.getLaunchDisplayId())) {
                return options.getLaunchDisplayId();
            }
            if (startingActivity != null && (this.mService instanceof HwActivityManagerService)) {
                HashMap<String, Integer> maps = this.mService.mHwAMSEx.getPkgDisplayMaps();
                int displayId = 0;
                if (!TextUtils.isEmpty(startingActivity.launchedFromPackage)) {
                    if (maps.containsKey(startingActivity.launchedFromPackage)) {
                        displayId = maps.get(startingActivity.launchedFromPackage).intValue();
                    }
                } else if (!TextUtils.isEmpty(startingActivity.packageName) && maps.containsKey(startingActivity.packageName)) {
                    displayId = maps.get(startingActivity.packageName).intValue();
                }
                if (HwPCUtils.isValidExtDisplayId(displayId)) {
                    return displayId;
                }
            }
        }
        return HwActivityStarter.super.getPreferedDisplayId(sourceRecord, startingActivity, options);
    }

    /* access modifiers changed from: protected */
    public int hasStartedOnOtherDisplay(ActivityRecord startActivity, int sourceDisplayId) {
        ArrayList<ProcessRecord> list;
        if (HwPCUtils.isPcCastModeInServer() && !sPCPkgName.contains(startActivity.packageName)) {
            String activityName = startActivity.realActivity != null ? startActivity.realActivity.getClassName() : "";
            if (packageShouldNotHandle(startActivity.packageName) && !"com.huawei.filemanager.desktopinstruction.EasyProjection".equals(activityName)) {
                return -1;
            }
            if (HwPCUtils.isValidExtDisplayId(sourceDisplayId) && startActivity.isActivityTypeHome()) {
                return 2;
            }
            if ("com.huawei.filemanager.desktopinstruction.EasyProjection".equals(activityName)) {
                list = getPackageProcess("com.huawei.filemanager.desktopinstruction", startActivity.userId);
            } else {
                list = getPackageProcess(startActivity.packageName, startActivity.userId);
            }
            if (list != null) {
                int size = list.size();
                int i = 0;
                while (i < size) {
                    ProcessRecord pr = list.get(i);
                    if (pr == null || pr.mDisplayId == sourceDisplayId || (!pr.foregroundActivities && (!"com.huawei.works".equals(startActivity.packageName) || !isConnectFromWeLink(sourceDisplayId)))) {
                        i++;
                    } else {
                        this.mService.mStackSupervisor.showToast(sourceDisplayId);
                        if (sourceDisplayId == 0) {
                            return 1;
                        }
                        return 0;
                    }
                }
            }
            if (this.mService instanceof HwActivityManagerService) {
                this.mService.mHwAMSEx.getPkgDisplayMaps().put(startActivity.packageName, Integer.valueOf(sourceDisplayId));
            }
        }
        return -1;
    }

    private boolean isConnectFromWeLink(int displayId) {
        Display display = ((DisplayManager) this.mService.mContext.getSystemService("display")).getDisplay(displayId);
        if (display != null && "com.huawei.works".equals(display.getOwnerPackageName())) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean killProcessOnDefaultDisplay(ActivityRecord startActivity) {
        if (HwPCUtils.enabled()) {
            boolean killPackageProcess = false;
            ArrayList<ProcessRecord> list = getPackageProcess(startActivity.packageName, startActivity.userId);
            int N = list.size();
            int i = 0;
            while (true) {
                if (i >= N) {
                    break;
                } else if (HwPCUtils.isValidExtDisplayId(list.get(i).mDisplayId)) {
                    killPackageProcess = true;
                    break;
                } else {
                    i++;
                }
            }
            if (killPackageProcess) {
                this.mService.forceStopPackageLocked(startActivity.packageName, UserHandle.getAppId(startActivity.appInfo.uid), false, false, true, false, false, UserHandle.getUserId(startActivity.appInfo.uid), "relaunch due to in diff display");
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean killProcessOnOtherDisplay(ActivityRecord startActivity, int sourceDisplayId) {
        ActivityRecord activityRecord = startActivity;
        int i = sourceDisplayId;
        if (HwPCUtils.isPcCastModeInServer()) {
            String activityName = activityRecord.realActivity != null ? activityRecord.realActivity.getClassName() : "";
            if (packageShouldNotHandle(activityRecord.packageName) && !"com.huawei.filemanager.desktopinstruction.EasyProjection".equals(activityName)) {
                return false;
            }
            if ("com.huawei.filemanager.desktopinstruction.EasyProjection".equals(activityName)) {
                ArrayList<ProcessRecord> list = getPackageProcess("com.huawei.filemanager.desktopinstruction", activityRecord.userId);
                int N = list.size();
                boolean isRemoved = false;
                for (int i2 = 0; i2 < N; i2++) {
                    if (list.get(i2).mDisplayId != i) {
                        this.mService.removeProcessLocked(list.get(i2), false, true, "killProcessOnOtherDisplay");
                        isRemoved = true;
                    }
                }
                if (isRemoved) {
                    HashSet hashSet = new HashSet();
                    hashSet.add("com.huawei.filemanager.desktopinstruction.EasyProjection");
                    this.mService.mStackSupervisor.finishDisabledPackageActivitiesLocked("com.huawei.desktop.explorer", hashSet, true, false, UserHandle.getUserId(activityRecord.appInfo.uid));
                }
                return isRemoved;
            }
            String processName = "";
            int targetDisplayId = -1;
            boolean killPackageProcess = false;
            ArrayList<ProcessRecord> list2 = getPackageProcess(activityRecord.packageName, activityRecord.userId);
            int N2 = list2.size();
            int i3 = 0;
            while (true) {
                if (i3 >= N2) {
                    break;
                }
                if (list2.get(i3).mDisplayId != i) {
                    if (HwPCUtils.enabledInPad() && "com.android.settings".equals(activityRecord.packageName) && "com.android.phone".equals(list2.get(i3).processName)) {
                        HwPCUtils.log(TAG, "settings in phone process");
                    } else if (TextUtils.isEmpty(activityRecord.packageName) || !activityRecord.packageName.contains("com.tencent.mm") || activityRecord.app == null || activityRecord.app.pid == list2.get(i3).pid) {
                        killPackageProcess = true;
                        processName = list2.get(i3).processName;
                        targetDisplayId = list2.get(i3).mDisplayId;
                    } else {
                        HwPCUtils.log(TAG, "pid is not same so dont kill the process when killing Process on other display");
                    }
                }
                i3++;
            }
            killPackageProcess = true;
            processName = list2.get(i3).processName;
            targetDisplayId = list2.get(i3).mDisplayId;
            if (sPCPkgName.contains(activityRecord.packageName)) {
                killPackageProcess = false;
            }
            if (killPackageProcess) {
                this.mService.forceStopPackageLocked(activityRecord.packageName, UserHandle.getAppId(activityRecord.appInfo.uid), false, false, true, false, false, UserHandle.getUserId(activityRecord.appInfo.uid), "relaunch due to in diff display");
                HwPCDataReporter.getInstance().reportKillProcessEvent(activityRecord.packageName, processName, i, targetDisplayId);
                return true;
            }
        }
        return false;
    }

    private boolean packageShouldNotHandle(String pkgName) {
        boolean z = true;
        if (HwPCUtils.enabledInPad()) {
            if (!"com.huawei.desktop.explorer".equals(pkgName) && !"com.huawei.desktop.systemui".equals(pkgName) && !FingerViewController.PKGNAME_OF_KEYGUARD.equals(pkgName) && !"com.android.incallui".equals(pkgName) && !"com.huawei.android.wfdft".equals(pkgName)) {
                z = false;
            }
            return z;
        }
        if (!"com.huawei.desktop.explorer".equals(pkgName) && !"com.huawei.desktop.systemui".equals(pkgName)) {
            z = false;
        }
        return z;
    }

    private ArrayList<ProcessRecord> getPackageProcess(String pkg, int userId) {
        ArrayList<ProcessRecord> procs = new ArrayList<>();
        int NP = this.mService.mProcessNames.getMap().size();
        for (int ip = 0; ip < NP; ip++) {
            SparseArray<ProcessRecord> apps = (SparseArray) this.mService.mProcessNames.getMap().valueAt(ip);
            int NA = apps.size();
            for (int ia = 0; ia < NA; ia++) {
                ProcessRecord proc = apps.valueAt(ia);
                if (proc.userId == userId && proc != this.mService.mHomeProcess && (proc.pkgList.containsKey(pkg) || ("com.huawei.filemanager.desktopinstruction".equals(pkg) && proc.processName != null && proc.processName.equals(pkg)))) {
                    procs.add(proc);
                }
            }
        }
        return procs;
    }

    private void standardizeIntentUriForClone(Intent intent, int userId) {
        Intent intent2 = intent;
        int i = userId;
        ClipData clipData = intent.getClipData();
        String volume = null;
        String sdcard = Environment.getLegacyExternalStorageDirectory().getAbsolutePath();
        char c = 0;
        if (clipData != null) {
            int itemCount = clipData.getItemCount();
            String volume2 = null;
            int i2 = 0;
            while (i2 < itemCount) {
                ClipData.Item item = clipData.getItemAt(i2);
                Uri uri = item.getUri();
                if (uri != null && "file".equals(uri.getScheme()) && uri.getPath().startsWith(sdcard)) {
                    if (volume2 == null) {
                        this.mService.mContext.getSystemService("storage");
                        StorageVolume[] volumes = StorageManager.getVolumeList(i, 512);
                        if (volumes == null || volumes.length == 0) {
                            break;
                        }
                        volume2 = volumes[c].getPath();
                    }
                    clipData.setItemAt(i2, new ClipData.Item(item.getText(), item.getHtmlText(), item.getIntent(), Uri.parse(uri.toString().replace(sdcard, volume2))));
                }
                i2++;
                c = 0;
            }
            volume = volume2;
        }
        Uri uri2 = intent.getData();
        if (uri2 != null && "file".equals(uri2.getScheme()) && uri2.getPath().startsWith(sdcard)) {
            if (volume == null) {
                this.mService.mContext.getSystemService("storage");
                StorageVolume[] volumes2 = StorageManager.getVolumeList(i, 512);
                if (volumes2 != null && volumes2.length > 0) {
                    volume = volumes2[0].getPath();
                }
            }
            if (volume != null) {
                intent2.setData(Uri.parse(uri2.toString().replace(sdcard, volume)));
            }
        }
        Uri stream = (Uri) intent2.getParcelableExtra("android.intent.extra.STREAM");
        if (stream != null && "file".equals(stream.getScheme()) && stream.getPath().startsWith(sdcard)) {
            if (volume == null) {
                this.mService.mContext.getSystemService("storage");
                StorageVolume[] volumes3 = StorageManager.getVolumeList(i, 512);
                if (volumes3 != null && volumes3.length > 0) {
                    volume = volumes3[0].getPath();
                }
            }
            if (volume != null) {
                intent2.putExtra("android.intent.extra.STREAM", Uri.parse(stream.toString().replace(sdcard, volume)));
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isInSkipCancelResultList(String clsName) {
        return sSkipCancelResultList.contains(clsName);
    }

    private void showErrorDialogToRemoveUser(final Context context, final int userId) {
        UiThread.getHandler().post(new Runnable() {
            public void run() {
                AlertDialog ErrorDialog = new AlertDialog.Builder(context, 33947691).setPositiveButton(33686090, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserManager.get(context).removeUser(userId);
                    }
                }).setNegativeButton(17039360, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setTitle(context.getString(33686095)).setMessage(context.getString(33686091)).create();
                ErrorDialog.getWindow().setType(2003);
                ErrorDialog.getWindow().getAttributes().privateFlags |= 16;
                ErrorDialog.setCanceledOnTouchOutside(false);
                ErrorDialog.show();
                ErrorDialog.getButton(-1).setTextColor(-65536);
            }
        });
    }

    private boolean isFrpRestrictedApp(Context context, Intent intent, ActivityInfo aInfo, int userId) {
        String frpToken = Settings.Secure.getStringForUser(context.getContentResolver(), "hw_frp_token", userId);
        String frpComps = Settings.Secure.getStringForUser(context.getContentResolver(), "hw_frp_comps", userId);
        if (!TextUtils.isEmpty(frpToken) || !TextUtils.isEmpty(frpComps)) {
            String frpCompsTemp = frpComps + ",";
            if (!frpCompsTemp.contains(aInfo.packageName + ",")) {
                if (!frpCompsTemp.contains(aInfo.packageName + "/" + aInfo.name + ",")) {
                    if (frpToken != null) {
                        try {
                            if (frpToken.equals(intent.getStringExtra("hw_frp_token"))) {
                                Slog.i(TAG, aInfo + " gets matched token in intent");
                                return false;
                            }
                        } catch (Exception e) {
                            Slog.e(TAG, "Parse extra failed!");
                            return false;
                        }
                    }
                    return true;
                }
            }
            Slog.i(TAG, aInfo + " is in frp comps");
            return false;
        }
        Slog.i(TAG, "Frp items are Empty");
        return false;
    }
}
