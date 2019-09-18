package com.android.server.security.trustspace;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.trustspace.ITrustSpaceManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.widget.Toast;
import com.android.internal.content.PackageMonitor;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.securityprofile.ScreenshotProtectorCallback;
import com.android.server.security.securityprofile.SecurityProfileInternal;
import java.util.ArrayList;
import java.util.List;

public class TrustSpaceManagerService extends ITrustSpaceManager.Stub implements IHwSecurityPlugin {
    private static final String ACTION_INTENT_PREVENTED = "huawei.intent.action.TRUSTSPACE_INTENT_PREVENTED";
    private static final String ACTION_PACKAGE_ADDED = "huawei.intent.action.TRUSTSPACE_PACKAGE_ADDED";
    private static final String ACTION_PACKAGE_REMOVED = "huawei.intent.action.TRUSTSPACE_PACKAGE_REMOVED";
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            if (TrustSpaceManagerService.HW_DEBUG) {
                Slog.d(TrustSpaceManagerService.TAG, "createPlugin");
            }
            return new TrustSpaceManagerService(context);
        }

        public String getPluginPermission() {
            return TrustSpaceManagerService.MANAGE_TRUSTSPACE;
        }
    };
    private static final String HW_APPMARKET_PACKAGENAME = "com.huawei.appmarket";
    /* access modifiers changed from: private */
    public static final boolean HW_DEBUG = Log.HWINFO;
    private static final String HW_TRUSTSPACE_LAUNCHER = "com.huawei.trustspace.mainscreen.LoadActivity";
    private static final String HW_TRUSTSPACE_PACKAGENAME = "com.huawei.trustspace";
    private static final int INVALID_CLONED_PROFILE = -1000;
    private static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    private static final String MANAGE_TRUSTSPACE = "com.huawei.permission.MANAGE_TRUSTSPACE";
    private static final int RESULT_CODE_ERROR = -1;
    private static final String SETTINGS_TRUSTSPACE_CONTROL = "trust_space_switch";
    private static final String SETTINGS_TRUSTSPACE_SWITCH = "is_trustspace_enabled";
    private static final String TAG = "TrustSpaceManagerService";
    private static final int TYPE_RISK = 303;
    private static final int TYPE_VIRUS = 305;
    private int mClonedProfileUserId;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public boolean mEnableTrustSpace = false;
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    private TrustSpaceSettings mSettings;
    private final MySettingsObserver mSettingsObserver = new MySettingsObserver();
    /* access modifiers changed from: private */
    public boolean mSupportTrustSpace = false;
    private final ArraySet<String> mSystemApps = new ArraySet<>();
    private volatile boolean mSystemReady = false;
    private final ArraySet<Integer> mSystemUids = new ArraySet<>();
    private final ArrayMap<String, Integer> mVirusScanResult = new ArrayMap<>();
    private SecurityProfileInternal securityProfileInternal = null;

    private final class LocalServiceImpl implements TrustSpaceManagerInternal {
        private LocalServiceImpl() {
        }

        public boolean checkIntent(int type, String calleePackage, int callerUid, int callerPid, String callingPackage, int userId) {
            return TrustSpaceManagerService.this.checkIntent(type, calleePackage, callerUid, callerPid, callingPackage, userId);
        }

        public void initTrustSpace() {
            TrustSpaceManagerService.this.initTrustSpace();
        }

        public boolean isIntentProtectedApp(String pkg) {
            return TrustSpaceManagerService.this.isIntentProtectedAppInner(pkg);
        }

        public int getProtectionLevel(String packageName) {
            return TrustSpaceManagerService.this.getProtectionLevel(packageName);
        }
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        MyBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_ADDED".equals(intent.getAction())) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                Slog.d(TrustSpaceManagerService.TAG, "action add user" + userId);
                if (!(userId == -10000 || userId == 0)) {
                    TrustSpaceManagerService.this.enableTrustSpaceApp(false, false, userId);
                }
                if (TrustSpaceManagerService.this.isClonedProfile(userId)) {
                    TrustSpaceManagerService.this.updateClonedProfileUserId(userId);
                }
            }
        }
    }

    private final class MyPackageMonitor extends PackageMonitor {
        private MyPackageMonitor() {
        }

        public void onPackageAdded(String packageName, int uid) {
            TrustSpaceManagerService.this.handlePackageAppeared(packageName, 3, uid);
        }

        public void onPackageRemoved(String packageName, int uid) {
            TrustSpaceManagerService.this.handlePackageDisappeared(packageName, 3, uid);
        }

        public void onPackageUpdateStarted(String packageName, int uid) {
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            TrustSpaceManagerService.this.handlePackageAppeared(packageName, 1, uid);
        }
    }

    private class MySettingsObserver extends ContentObserver {
        private final Uri TRUSTSPACE_SWITCH_URI = Settings.Secure.getUriFor(TrustSpaceManagerService.SETTINGS_TRUSTSPACE_SWITCH);

        public MySettingsObserver() {
            super(new Handler());
        }

        public void registerContentObserver() {
            TrustSpaceManagerService.this.mContext.getContentResolver().registerContentObserver(this.TRUSTSPACE_SWITCH_URI, false, this, 0);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.TRUSTSPACE_SWITCH_URI.equals(uri)) {
                int i = 0;
                boolean enable = Settings.Secure.getIntForUser(TrustSpaceManagerService.this.mContext.getContentResolver(), TrustSpaceManagerService.SETTINGS_TRUSTSPACE_SWITCH, 1, 0) == 1;
                Slog.i(TrustSpaceManagerService.TAG, "TrustSpace Enabled = " + enable);
                boolean unused = TrustSpaceManagerService.this.mEnableTrustSpace = enable;
                if (TrustSpaceManagerService.this.mSupportTrustSpace) {
                    TrustSpaceManagerService.this.enableTrustSpaceApp(true, enable, 0);
                    ContentResolver contentResolver = TrustSpaceManagerService.this.mContext.getContentResolver();
                    if (enable) {
                        i = 1;
                    }
                    Settings.System.putInt(contentResolver, TrustSpaceManagerService.SETTINGS_TRUSTSPACE_CONTROL, i);
                }
            }
        }
    }

    private class ScreenshotProtector extends ScreenshotProtectorCallback {
        private ScreenshotProtector() {
        }

        public boolean isProtectedApp(String packageName) {
            if (ActivityManager.getCurrentUser() == 0 && TrustSpaceManagerService.this.getProtectionLevel(packageName) == 2) {
                return true;
            }
            return false;
        }

        public void notifyInfo(String projectionPack) {
            String text = TrustSpaceManagerService.this.mContext.getResources().getString(33686248);
            if (text != null) {
                String packageLabel = projectionPack;
                PackageManager pm = TrustSpaceManagerService.this.mContext.getPackageManager();
                if (pm != null) {
                    try {
                        ApplicationInfo info = pm.getApplicationInfoAsUser(projectionPack, 0, 0);
                        if (info != null) {
                            CharSequence seq = pm.getApplicationLabel(info);
                            packageLabel = seq == null ? null : seq.toString();
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Slog.e(TrustSpaceManagerService.TAG, "can not find " + projectionPack);
                    }
                }
                if (packageLabel != null) {
                    text = text.replace("%s", packageLabel);
                }
                showToast(text);
            }
        }

        private void showToast(final String text) {
            UiThread.getHandler().post(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(TrustSpaceManagerService.this.mContext, text, 1);
                    toast.getWindowParams().privateFlags |= 16;
                    toast.show();
                }
            });
        }
    }

    public TrustSpaceManagerService(Context context) {
        this.mContext = context;
        this.mSettings = new TrustSpaceSettings();
    }

    public void onStart() {
        Slog.d(TAG, "TrustSpaceManagerService Start");
        LocalServices.addService(TrustSpaceManagerInternal.class, new LocalServiceImpl());
        this.securityProfileInternal = (SecurityProfileInternal) LocalServices.getService(SecurityProfileInternal.class);
        if (this.securityProfileInternal != null) {
            this.securityProfileInternal.registerScreenshotProtector(new ScreenshotProtector());
        }
    }

    public void onStop() {
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [android.os.IBinder, com.android.server.security.trustspace.TrustSpaceManagerService] */
    public IBinder asBinder() {
        return this;
    }

    /* access modifiers changed from: private */
    public void handlePackageAppeared(String packageName, int reason, int uid) {
        if (isUseTrustSpace()) {
            int userId = UserHandle.getUserId(uid);
            if (userId == 0 || isClonedProfile(userId)) {
                if (HW_DEBUG) {
                    Slog.d(TAG, "onPackageAppeared:" + packageName + " reason=" + reason + " uid=" + uid);
                }
                if (userId == 0) {
                    removeVirusScanResult(packageName);
                }
                Intent intent = new Intent(ACTION_PACKAGE_ADDED);
                intent.putExtra("packageName", packageName);
                intent.putExtra("reason", reason);
                intent.putExtra("userId", userId);
                intent.setPackage(HW_TRUSTSPACE_PACKAGENAME);
                this.mContext.startService(intent);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handlePackageDisappeared(String packageName, int reason, int uid) {
        if (isUseTrustSpace()) {
            int userId = UserHandle.getUserId(uid);
            if (userId == 0 || isClonedProfile(userId)) {
                if (HW_DEBUG) {
                    Slog.d(TAG, "onPackageDisappeared:" + packageName + " reason=" + reason + " uid=" + uid);
                }
                if (userId == 0) {
                    removeVirusScanResult(packageName);
                    synchronized (this) {
                        boolean configChange = false;
                        boolean isProtectedApp = this.mSettings.isIntentProtectedApp(packageName);
                        boolean isTrustApp = this.mSettings.isTrustApp(packageName);
                        if (isProtectedApp && reason == 3) {
                            this.mSettings.removeIntentProtectedApp(packageName);
                            configChange = true;
                        }
                        if (isTrustApp && reason == 3) {
                            this.mSettings.removeTrustApp(packageName);
                            configChange = true;
                        }
                        if (configChange) {
                            this.mSettings.writePackages();
                        }
                    }
                }
                Intent intent = new Intent(ACTION_PACKAGE_REMOVED);
                intent.putExtra("packageName", packageName);
                intent.putExtra("reason", reason);
                intent.putExtra("userId", userId);
                intent.setPackage(HW_TRUSTSPACE_PACKAGENAME);
                this.mContext.startService(intent);
            }
        }
    }

    private boolean isTrustSpaceEnable() {
        return Settings.System.getIntForUser(this.mContext.getContentResolver(), SETTINGS_TRUSTSPACE_CONTROL, 1, 0) == 1;
    }

    private boolean isUseTrustSpace() {
        return this.mSupportTrustSpace && this.mEnableTrustSpace;
    }

    private boolean calledFromValidUser() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (uid == 1000 || userId == 0) {
            return true;
        }
        return false;
    }

    private void removeVirusScanResult(String packageName) {
        synchronized (this.mVirusScanResult) {
            this.mVirusScanResult.remove(packageName);
        }
    }

    private void addVirusScanResult(String packageName, int type) {
        synchronized (this.mVirusScanResult) {
            this.mVirusScanResult.put(packageName, Integer.valueOf(type));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0040, code lost:
        r1 = android.os.SystemClock.uptimeMillis();
        r6 = com.huawei.hsm.permission.StubController.getHoldService();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0048, code lost:
        if (r6 != null) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004c, code lost:
        if (HW_DEBUG == false) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004e, code lost:
        android.util.Slog.e(TAG, "isMaliciousApp, service is null!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0055, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0056, code lost:
        r7 = new android.os.Bundle();
        r7.putString("packageName", r15);
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0068, code lost:
        r8 = r6.callHsmService("isVirusApk", r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006a, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006b, code lost:
        android.util.Slog.e(TAG, "callHsmService fail: " + r9.getMessage());
     */
    private boolean isMaliciousApp(String packageName) {
        long start;
        Bundle res;
        if (packageName == null) {
            return false;
        }
        synchronized (this.mVirusScanResult) {
            if (this.mVirusScanResult.containsKey(packageName)) {
                int type = this.mVirusScanResult.get(packageName).intValue();
                if (type != 303) {
                    if (type != 305) {
                        return false;
                    }
                }
                Slog.i(TAG, "find Malicious App:" + packageName);
                return true;
            }
        }
        long costTime = SystemClock.uptimeMillis() - start;
        if (HW_DEBUG) {
            Slog.d(TAG, "isMaliciousApp cost: " + costTime);
        }
        if (res == null) {
            Slog.i(TAG, "isVirusApk, res is null");
            return false;
        }
        int resultCode = res.getInt("result_code", -1);
        if (resultCode != -1) {
            addVirusScanResult(packageName, resultCode);
        }
        if (resultCode != 303 && resultCode != 305) {
            return false;
        }
        Slog.i(TAG, "find Malicious App:" + packageName);
        return true;
    }

    private boolean isSystemPackageInstalled(String packageName, int flag) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, flag, 0);
        } catch (RemoteException e) {
            Log.d(TAG, "Can't find package:" + packageName);
        }
        if (appInfo == null || !appInfo.isSystemApp()) {
            return false;
        }
        return true;
    }

    private boolean isSupportTrustSpaceInner() {
        if (isSystemPackageInstalled(HW_APPMARKET_PACKAGENAME, 0)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void enableTrustSpaceApp(boolean enablePackage, boolean enableLauncher, int userId) {
        IPackageManager pm = AppGlobals.getPackageManager();
        ComponentName cName = new ComponentName(HW_TRUSTSPACE_PACKAGENAME, HW_TRUSTSPACE_LAUNCHER);
        int i = 2;
        int newPackageState = enablePackage ? 0 : 2;
        if (enableLauncher) {
            i = 0;
        }
        int newLauncherState = i;
        try {
            pm.setApplicationEnabledSetting(HW_TRUSTSPACE_PACKAGENAME, newPackageState, 0, userId, null);
            if (enablePackage) {
                pm.setComponentEnabledSetting(cName, newLauncherState, 1, userId);
            }
        } catch (Exception e) {
            Slog.w(TAG, "enableTrustSpaceApp fail: " + e.getMessage());
        }
    }

    private void loadSystemPackages() {
        ArraySet<Integer> tempUidSet = new ArraySet<>();
        ArraySet<String> tempPkgSet = new ArraySet<>();
        List<ApplicationInfo> allApp = null;
        try {
            allApp = AppGlobals.getPackageManager().getInstalledApplications(8192, 0).getList();
        } catch (RemoteException e) {
            Slog.e(TAG, "Get Installed Applications fail");
        }
        if (allApp != null) {
            for (ApplicationInfo app : allApp) {
                if (app.isSystemApp() && (app.hwFlags & 33554432) == 0) {
                    tempUidSet.add(Integer.valueOf(app.uid));
                    tempPkgSet.add(app.packageName);
                }
            }
            this.mSystemUids.clear();
            this.mSystemUids.addAll(tempUidSet);
            this.mSystemApps.clear();
            this.mSystemApps.addAll(tempPkgSet);
        }
    }

    private int getClonedProfileId() {
        if (!IS_SUPPORT_CLONE_APP) {
            return -1000;
        }
        try {
            for (UserInfo info : ServiceManager.getService("user").getProfiles(0, false)) {
                if (info.isClonedProfile()) {
                    return info.id;
                }
            }
            Slog.d(TAG, "Cloned Profile is not exist");
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to getProfiles");
        }
        return -1000;
    }

    /* access modifiers changed from: private */
    public boolean isClonedProfile(int userId) {
        boolean z = false;
        if (!IS_SUPPORT_CLONE_APP) {
            return false;
        }
        try {
            UserInfo userInfo = ServiceManager.getService("user").getUserInfo(userId);
            if (userInfo != null && userInfo.isClonedProfile()) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to getUserInfo");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void updateClonedProfileUserId(int userId) {
        if (IS_SUPPORT_CLONE_APP && userId != -1000) {
            this.mClonedProfileUserId = userId;
            Slog.i(TAG, "Cloned profile user" + userId);
        }
    }

    private boolean checkClonedProfile(int userId) {
        boolean z = false;
        if (!IS_SUPPORT_CLONE_APP || userId == -1000) {
            return false;
        }
        if (userId == this.mClonedProfileUserId) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void initTrustSpace() {
        if (!this.mSystemReady) {
            Slog.i(TAG, "TrustSpaceManagerService init begin");
            if (!isSystemPackageInstalled(HW_TRUSTSPACE_PACKAGENAME, 0)) {
                Slog.e(TAG, "TrustSpace application is not exist");
                return;
            }
            this.mSupportTrustSpace = isSupportTrustSpaceInner();
            if (this.mSupportTrustSpace) {
                synchronized (this) {
                    this.mSettings.readPackages();
                }
                loadSystemPackages();
                this.mMyPackageMonitor.register(this.mContext, null, UserHandle.ALL, false);
                this.mSettingsObserver.registerContentObserver();
                updateClonedProfileUserId(getClonedProfileId());
                this.mEnableTrustSpace = isTrustSpaceEnable();
            }
            IntentFilter broadcastFilter = new IntentFilter();
            broadcastFilter.addAction("android.intent.action.USER_ADDED");
            this.mContext.registerReceiver(new MyBroadcastReceiver(), broadcastFilter);
            Slog.i(TAG, "Enable TrustSpace App: " + this.mSupportTrustSpace);
            if (this.mSupportTrustSpace) {
                enableTrustSpaceApp(true, this.mEnableTrustSpace, 0);
            } else {
                disableTrustSpaceForAllUsers();
            }
            this.mSystemReady = true;
            Slog.i(TAG, "TrustSpaceManagerService init end");
        }
    }

    private void disableTrustSpaceForAllUsers() {
        try {
            for (UserInfo user : ServiceManager.getService("user").getUsers(false)) {
                enableTrustSpaceApp(false, false, user.id);
            }
        } catch (RemoteException e) {
            Slog.d(TAG, "disableTrustSpaceForAllUsers, failed to getUserInfo");
        }
    }

    private boolean isValidUser(int type, int userId) {
        boolean z = false;
        boolean isValidUser = userId == 0;
        if (type == 1) {
            if (userId == -1) {
                z = true;
            }
            isValidUser |= z;
        }
        return checkClonedProfile(userId) | isValidUser;
    }

    private boolean isSpecialCaller(String packageName, int uid) {
        if (packageName != null) {
            return this.mSystemApps.contains(packageName);
        }
        if (uid < 10000 || this.mSystemUids.contains(Integer.valueOf(uid))) {
            return true;
        }
        return false;
    }

    private boolean isSelfCall(String calleePackage, String callingPackage) {
        return calleePackage != null && calleePackage.equals(callingPackage);
    }

    private boolean isSpecialCallee(String packageName) {
        return this.mSystemApps.contains(packageName);
    }

    private boolean isSpecialPackage(String callingPackage, int callerUid, String calleePackage) {
        boolean isCallerIntentProtected;
        boolean isCalleeIntentProtected;
        boolean isSpecialCaller = isSpecialCaller(callingPackage, callerUid);
        boolean isSpecialCallee = isSpecialCallee(calleePackage);
        synchronized (this) {
            isCallerIntentProtected = this.mSettings.isIntentProtectedApp(callingPackage);
            isCalleeIntentProtected = this.mSettings.isIntentProtectedApp(calleePackage);
        }
        return (isSpecialCaller && !isCallerIntentProtected) || (isSpecialCallee && !isCalleeIntentProtected);
    }

    private boolean shouldNotify(int type, String target) {
        if ((type == 0 || type == 2 || type == 3) && !isMaliciousApp(target)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0059, code lost:
        if (r7 != 0) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005b, code lost:
        if (r6 != 0) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005d, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0060, code lost:
        if (HW_DEBUG == false) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0062, code lost:
        android.util.Slog.d(TAG, "check Intent, type: " + r15 + " calleePackage: " + r10 + " calleelevel=" + r7 + " callerPid:" + r12 + " callerUid:" + r11 + " callerPackage:" + r13 + " callerLevel=" + r6 + " userId:" + r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b0, code lost:
        r0 = false;
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b3, code lost:
        if (r7 != 1) goto L_0x00c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b5, code lost:
        if (r6 != 0) goto L_0x00c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b7, code lost:
        if (r17 == false) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00be, code lost:
        if (isMaliciousApp(r13) == false) goto L_0x00ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00c0, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c2, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00c3, code lost:
        if (r6 != 1) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c5, code lost:
        if (r7 != 0) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c7, code lost:
        if (r16 == false) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ce, code lost:
        if (isMaliciousApp(r10) == false) goto L_0x00ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00d0, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00d2, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d4, code lost:
        if (r7 != 2) goto L_0x00e5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d6, code lost:
        if (r6 != 0) goto L_0x00e4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00d8, code lost:
        if (r17 == false) goto L_0x00db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00db, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e0, code lost:
        if (shouldNotify(r9, r13) == false) goto L_0x00ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00e2, code lost:
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00e4, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00e5, code lost:
        if (r6 != 2) goto L_0x00ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00eb, code lost:
        if (isMaliciousApp(r10) != false) goto L_0x00ee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ed, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00ee, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00ef, code lost:
        r18 = r0;
        r0 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00f6, code lost:
        if (isMaliciousApp(r13) != false) goto L_0x0100;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00fc, code lost:
        if (isMaliciousApp(r10) == false) goto L_0x0101;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0100, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0101, code lost:
        r5 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0102, code lost:
        if (r5 == false) goto L_0x0119;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0104, code lost:
        if (r9 == 1) goto L_0x0119;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0106, code lost:
        r20 = r5;
        r21 = r6;
        r22 = r7;
        notifyIntentPrevented(r15, r10, r7, r13, r6, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0119, code lost:
        r20 = r5;
        r21 = r6;
        r22 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x011f, code lost:
        if (r0 == false) goto L_0x012d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0121, code lost:
        notifyIntentPrevented(r15, r10, r22, r13, r21, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x012d, code lost:
        if (r18 == false) goto L_0x018d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x012f, code lost:
        android.util.Slog.i(TAG, "prevent Intent, type: " + r15 + " calleePackage: " + r10 + " calleelevel=" + r22 + " callerPid:" + r12 + " callerUid:" + r11 + " callerPackage:" + r13 + " callerLevel=" + r21 + " isMalicious=" + r20 + " userId:" + r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x018d, code lost:
        r5 = r20;
        r4 = r21;
        r3 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0193, code lost:
        return r18;
     */
    public boolean checkIntent(int type, String calleePackage, int callerUid, int callerPid, String callingPackage, int userId) {
        boolean isTrustcaller;
        int calleeLevel;
        int callerLevel;
        int i = type;
        String str = calleePackage;
        int i2 = callerUid;
        int i3 = callerPid;
        String str2 = callingPackage;
        int i4 = userId;
        boolean z = false;
        if (!isUseTrustSpace() || !isValidUser(i, i4) || isSelfCall(str, str2) || isSpecialPackage(str2, i2, str)) {
            return false;
        }
        String typeString = TrustSpaceSettings.componentTypeToString(type);
        if (str == null || str2 == null) {
            Slog.w(TAG, "unknown Intent, type: " + typeString + " calleePackage: " + str + " callerPid:" + i3 + " callerUid:" + i2 + " callerPackage:" + str2 + " userId:" + i4);
            return false;
        }
        synchronized (this) {
            try {
                boolean isTrustcallee = this.mSettings.isTrustApp(str);
                try {
                    isTrustcaller = this.mSettings.isTrustApp(str2);
                } catch (Throwable th) {
                    th = th;
                    boolean z2 = isTrustcallee;
                    throw th;
                }
                try {
                    calleeLevel = this.mSettings.getProtectionLevel(str) & 255;
                    try {
                        callerLevel = this.mSettings.getProtectionLevel(str2) & 255;
                    } catch (Throwable th2) {
                        th = th2;
                        int i5 = calleeLevel;
                        boolean z3 = isTrustcallee;
                        boolean z4 = isTrustcaller;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    boolean z5 = isTrustcallee;
                    boolean z6 = isTrustcaller;
                    throw th;
                }
                try {
                } catch (Throwable th4) {
                    th = th4;
                    int i6 = callerLevel;
                    boolean z7 = isTrustcallee;
                    int i7 = calleeLevel;
                    boolean z8 = isTrustcaller;
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                throw th;
            }
        }
    }

    private void notifyIntentPrevented(String typeString, String calleePackage, int calleeLevel, String callerPackage, int callerLevel, boolean isMalicious) {
        Handler handler = UiThread.getHandler();
        final String str = typeString;
        final String str2 = calleePackage;
        final String str3 = callerPackage;
        final int i = calleeLevel;
        final int i2 = callerLevel;
        final boolean z = isMalicious;
        AnonymousClass2 r1 = new Runnable() {
            public void run() {
                Intent intent = new Intent(TrustSpaceManagerService.ACTION_INTENT_PREVENTED);
                intent.putExtra("component", str);
                intent.putExtra("callee", str2);
                intent.putExtra("caller", str3);
                intent.putExtra("calleeLevel", i);
                intent.putExtra("callerLevel", i2);
                intent.putExtra("isMalicious", z);
                intent.setPackage(TrustSpaceManagerService.HW_TRUSTSPACE_PACKAGENAME);
                TrustSpaceManagerService.this.mContext.startService(intent);
                Slog.d(TrustSpaceManagerService.TAG, "Notify intent prevented.");
            }
        };
        handler.post(r1);
    }

    /* access modifiers changed from: private */
    public boolean isIntentProtectedAppInner(String packageName) {
        boolean isIntentProtectedApp;
        if (!calledFromValidUser() || packageName == null) {
            return false;
        }
        synchronized (this) {
            isIntentProtectedApp = this.mSettings.isIntentProtectedApp(packageName);
        }
        return isIntentProtectedApp;
    }

    public boolean addIntentProtectedApps(List<String> packages, int flags) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this) {
            for (String packageName : packages) {
                this.mSettings.addIntentProtectedApp(packageName, flags);
                if (HW_DEBUG) {
                    Slog.d(TAG, "add " + packageName + " to intent protected list, flags=" + flags);
                }
            }
            this.mSettings.writePackages();
        }
        return true;
    }

    public boolean removeIntentProtectedApp(String packageName) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser() || packageName == null) {
            return false;
        }
        synchronized (this) {
            this.mSettings.removeIntentProtectedApp(packageName);
            this.mSettings.writePackages();
            if (HW_DEBUG) {
                Slog.d(TAG, "remove " + packageName + " from intent protected list");
            }
        }
        return true;
    }

    public List<String> getIntentProtectedApps(int flags) {
        List<String> intentProtectedApps;
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser()) {
            return new ArrayList(0);
        }
        synchronized (this) {
            intentProtectedApps = this.mSettings.getIntentProtectedApps(flags);
        }
        return intentProtectedApps;
    }

    public boolean removeIntentProtectedApps(List<String> packages, int flags) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this) {
            this.mSettings.removeIntentProtectedApps(packages, flags);
            this.mSettings.writePackages();
            if (HW_DEBUG) {
                Slog.d(TAG, "remove apps in intent protected list, flag=" + flags);
            }
        }
        return true;
    }

    public boolean updateTrustApps(List<String> packages, int flag) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser() || packages == null) {
            return false;
        }
        synchronized (this) {
            this.mSettings.updateTrustApps(packages, flag);
            this.mSettings.writePackages();
            if (HW_DEBUG) {
                Slog.d(TAG, "update trust apps, flag=" + flag);
            }
        }
        return true;
    }

    public boolean isIntentProtectedApp(String packageName) {
        boolean isIntentProtectedApp;
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser() || packageName == null) {
            return false;
        }
        synchronized (this) {
            isIntentProtectedApp = this.mSettings.isIntentProtectedApp(packageName);
        }
        return isIntentProtectedApp;
    }

    public int getProtectionLevel(String packageName) {
        int protectionLevel;
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser() || packageName == null) {
            return 0;
        }
        synchronized (this) {
            protectionLevel = this.mSettings.getProtectionLevel(packageName);
        }
        return protectionLevel;
    }

    public boolean isSupportTrustSpace() {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser()) {
            return false;
        }
        return this.mSupportTrustSpace;
    }

    public boolean isHwTrustSpace(int userId) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        boolean z = false;
        if (!calledFromValidUser()) {
            return false;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = ServiceManager.getService("user").getUserInfo(userId);
            if (userInfo != null && userInfo.isHwTrustSpace()) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Slog.d(TAG, "failed to getUserInfo");
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
}
