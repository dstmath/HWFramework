package com.android.server.security.trustspace;

import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.trustspace.ITrustSpaceManager.Stub;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import com.android.internal.content.PackageMonitor;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.display.Utils;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.huawei.hsm.permission.StubController;
import com.huawei.permission.IHoldService;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.List;

public class TrustSpaceManagerService extends Stub implements IHwSecurityPlugin {
    private static final String ACTION_INTENT_PREVENTED = "huawei.intent.action.TRUSTSPACE_INTENT_PREVENTED";
    private static final String ACTION_PACKAGE_ADDED = "huawei.intent.action.TRUSTSPACE_PACKAGE_ADDED";
    private static final String ACTION_PACKAGE_REMOVED = "huawei.intent.action.TRUSTSPACE_PACKAGE_REMOVED";
    public static final Creator CREATOR = null;
    private static final String HW_APPMARKET_PACKAGENAME = "com.huawei.appmarket";
    private static final boolean HW_DEBUG = false;
    private static final String HW_TRUSTSPACE_LAUNCHER = "com.huawei.trustspace.mainscreen.LoadActivity";
    private static final String HW_TRUSTSPACE_PACKAGENAME = "com.huawei.trustspace";
    private static final String MANAGE_TRUSTSPACE = "com.huawei.permission.MANAGE_TRUSTSPACE";
    private static final int RESULT_CODE_ERROR = -1;
    private static final String SETTINGS_TRUSTSPACE_CONTROL = "trust_space_switch";
    private static final String TAG = "TrustSpaceManagerService";
    private static final int TYPE_RISK = 303;
    private static final int TYPE_VIRUS = 305;
    private Context mContext;
    private boolean mEnableTrustSpace;
    private final MyPackageMonitor mMyPackageMonitor;
    private TrustSpaceSettings mSettings;
    private MySettingsObserver mSettingsObserver;
    private boolean mSupportTrustSpace;
    private final ArraySet<String> mSystemApps;
    private volatile boolean mSystemReady;
    private final ArraySet<Integer> mSystemUids;
    private final ArrayMap<String, Integer> mVirusScanResult;

    /* renamed from: com.android.server.security.trustspace.TrustSpaceManagerService.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ int val$calleeLevel;
        final /* synthetic */ String val$calleePackage;
        final /* synthetic */ int val$callerLevel;
        final /* synthetic */ String val$callerPackage;
        final /* synthetic */ String val$typeString;

        AnonymousClass2(String val$typeString, String val$calleePackage, String val$callerPackage, int val$calleeLevel, int val$callerLevel) {
            this.val$typeString = val$typeString;
            this.val$calleePackage = val$calleePackage;
            this.val$callerPackage = val$callerPackage;
            this.val$calleeLevel = val$calleeLevel;
            this.val$callerLevel = val$callerLevel;
        }

        public void run() {
            Intent intent = new Intent(TrustSpaceManagerService.ACTION_INTENT_PREVENTED);
            intent.putExtra("component", this.val$typeString);
            intent.putExtra("callee", this.val$calleePackage);
            intent.putExtra(PreciseIgnore.SERVICE_CLAZZ_CALLER_ELEMENT_KEY, this.val$callerPackage);
            intent.putExtra("calleeLevel", this.val$calleeLevel);
            intent.putExtra("callerLevel", this.val$callerLevel);
            intent.setPackage(TrustSpaceManagerService.HW_TRUSTSPACE_PACKAGENAME);
            TrustSpaceManagerService.this.mContext.startService(intent);
            Slog.d(TrustSpaceManagerService.TAG, "Notify intent prevented.");
        }
    }

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
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        MyBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_ADDED".equals(intent.getAction())) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                Slog.d(TrustSpaceManagerService.TAG, "action add user" + userId);
                if (!(userId == -10000 || userId == 0)) {
                    TrustSpaceManagerService.this.enableTrustSpaceApp(TrustSpaceManagerService.HW_DEBUG, TrustSpaceManagerService.HW_DEBUG, userId);
                }
            }
        }
    }

    private final class MyPackageMonitor extends PackageMonitor {
        private MyPackageMonitor() {
        }

        public void onPackageAppeared(String packageName, int reason) {
            if (TrustSpaceManagerService.this.isUseTrustSpace()) {
                Slog.d(TrustSpaceManagerService.TAG, "onPackageAppeared:" + packageName + " reason=" + reason);
                TrustSpaceManagerService.this.removeVirusScanResult(packageName);
                Intent intent = new Intent(TrustSpaceManagerService.ACTION_PACKAGE_ADDED);
                intent.putExtra(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, packageName);
                intent.putExtra("reason", reason);
                intent.setPackage(TrustSpaceManagerService.HW_TRUSTSPACE_PACKAGENAME);
                TrustSpaceManagerService.this.mContext.startService(intent);
            }
        }

        public void onPackageDisappeared(String packageName, int reason) {
            if (TrustSpaceManagerService.this.isUseTrustSpace()) {
                Slog.d(TrustSpaceManagerService.TAG, "onPackageDisappeared:" + packageName + " reason=" + reason);
                TrustSpaceManagerService.this.removeVirusScanResult(packageName);
                boolean configChange = TrustSpaceManagerService.HW_DEBUG;
                synchronized (TrustSpaceManagerService.this) {
                    boolean isProtectedApp = TrustSpaceManagerService.this.mSettings.isIntentProtectedApp(packageName);
                    boolean isTrustApp = TrustSpaceManagerService.this.mSettings.isTrustApp(packageName);
                    if (isProtectedApp && reason == 3) {
                        TrustSpaceManagerService.this.mSettings.removeIntentProtectedApp(packageName);
                        configChange = true;
                    }
                    if (isTrustApp && reason == 3) {
                        TrustSpaceManagerService.this.mSettings.removeTrustApp(packageName);
                        configChange = true;
                    }
                    if (configChange) {
                        TrustSpaceManagerService.this.mSettings.writePackages();
                    }
                }
                Intent intent = new Intent(TrustSpaceManagerService.ACTION_PACKAGE_REMOVED);
                intent.putExtra(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, packageName);
                intent.putExtra("reason", reason);
                intent.setPackage(TrustSpaceManagerService.HW_TRUSTSPACE_PACKAGENAME);
                TrustSpaceManagerService.this.mContext.startService(intent);
            }
        }
    }

    private class MySettingsObserver extends ContentObserver {
        private final Uri TRUSTSPACE_CONTROL_URI;

        public MySettingsObserver() {
            super(new Handler());
            this.TRUSTSPACE_CONTROL_URI = System.getUriFor(TrustSpaceManagerService.SETTINGS_TRUSTSPACE_CONTROL);
        }

        public void registerContentObserver() {
            TrustSpaceManagerService.this.mContext.getContentResolver().registerContentObserver(this.TRUSTSPACE_CONTROL_URI, TrustSpaceManagerService.HW_DEBUG, this, 0);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.TRUSTSPACE_CONTROL_URI.equals(uri)) {
                TrustSpaceManagerService.this.mEnableTrustSpace = TrustSpaceManagerService.this.isTrustSpaceEnable();
                Slog.i(TrustSpaceManagerService.TAG, "TrustSpace Enabled = " + TrustSpaceManagerService.this.mEnableTrustSpace);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustspace.TrustSpaceManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustspace.TrustSpaceManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustspace.TrustSpaceManagerService.<clinit>():void");
    }

    public TrustSpaceManagerService(Context context) {
        this.mSystemReady = HW_DEBUG;
        this.mSupportTrustSpace = HW_DEBUG;
        this.mEnableTrustSpace = HW_DEBUG;
        this.mSystemUids = new ArraySet();
        this.mSystemApps = new ArraySet();
        this.mVirusScanResult = new ArrayMap();
        this.mMyPackageMonitor = new MyPackageMonitor();
        this.mSettingsObserver = new MySettingsObserver();
        this.mContext = context;
        this.mSettings = new TrustSpaceSettings();
    }

    public void onStart() {
        Slog.d(TAG, "TrustSpaceManagerService Start");
        LocalServices.addService(TrustSpaceManagerInternal.class, new LocalServiceImpl());
    }

    public void onStop() {
    }

    public IBinder asBinder() {
        return this;
    }

    private boolean isTrustSpaceEnable() {
        return System.getIntForUser(this.mContext.getContentResolver(), SETTINGS_TRUSTSPACE_CONTROL, 1, 0) == 1 ? true : HW_DEBUG;
    }

    private boolean isUseTrustSpace() {
        return this.mSupportTrustSpace ? this.mEnableTrustSpace : HW_DEBUG;
    }

    private boolean calledFromValidUser() {
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (uid == IOTController.TYPE_MASTER || userId == 0) {
            return true;
        }
        if (HW_DEBUG) {
            Slog.d(TAG, "IPC called from valid User. Calling uid = " + uid + " calling userId = " + userId);
        }
        return HW_DEBUG;
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

    private boolean isMaliciousApp(String packageName) {
        if (packageName == null) {
            return HW_DEBUG;
        }
        synchronized (this.mVirusScanResult) {
            if (this.mVirusScanResult.containsKey(packageName)) {
                int type = ((Integer) this.mVirusScanResult.get(packageName)).intValue();
                if (type == TYPE_RISK || type == TYPE_VIRUS) {
                    Slog.i(TAG, "find Malicious App:" + packageName);
                    return true;
                }
                return HW_DEBUG;
            }
            long start = SystemClock.uptimeMillis();
            IHoldService service = StubController.getHoldService();
            if (service == null) {
                if (HW_DEBUG) {
                    Slog.e(TAG, "isMaliciousApp, service is null!");
                }
                return HW_DEBUG;
            }
            Bundle bundle = new Bundle();
            bundle.putString(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, packageName);
            Bundle res = null;
            try {
                res = service.callHsmService("isVirusApk", bundle);
            } catch (Exception e) {
                Slog.e(TAG, "callHsmService fail: " + e.getMessage());
            }
            long costTime = SystemClock.uptimeMillis() - start;
            if (HW_DEBUG) {
                Slog.d(TAG, "isMaliciousApp cost: " + costTime);
            }
            if (res == null) {
                Slog.i(TAG, "isVirusApk, res is null");
                return HW_DEBUG;
            }
            int resultCode = res.getInt("result_code", RESULT_CODE_ERROR);
            if (resultCode != RESULT_CODE_ERROR) {
                addVirusScanResult(packageName, resultCode);
            }
            if (resultCode != TYPE_RISK && resultCode != TYPE_VIRUS) {
                return HW_DEBUG;
            }
            Slog.i(TAG, "find Malicious App:" + packageName);
            return true;
        }
    }

    private boolean isSystemPackageInstalled(String packageName, int flag) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, flag, 0);
        } catch (RemoteException e) {
            Log.d(TAG, "Can't find package:" + packageName);
        }
        if (appInfo != null) {
            return appInfo.isSystemApp();
        }
        return HW_DEBUG;
    }

    private boolean isSupportTrustSpaceInner() {
        if (isSystemPackageInstalled(HW_APPMARKET_PACKAGENAME, 0)) {
            return true;
        }
        return HW_DEBUG;
    }

    private void enableTrustSpaceApp(boolean enablePackage, boolean enableLauncher, int userId) {
        int newPackageState;
        int newLauncherState;
        IPackageManager pm = AppGlobals.getPackageManager();
        ComponentName cName = new ComponentName(HW_TRUSTSPACE_PACKAGENAME, HW_TRUSTSPACE_LAUNCHER);
        if (enablePackage) {
            newPackageState = 0;
        } else {
            newPackageState = 2;
        }
        if (enableLauncher) {
            newLauncherState = 0;
        } else {
            newLauncherState = 2;
        }
        try {
            pm.setApplicationEnabledSetting(HW_TRUSTSPACE_PACKAGENAME, newPackageState, 0, userId, null);
            pm.setComponentEnabledSetting(cName, newLauncherState, 0, userId);
        } catch (Exception e) {
            Slog.w(TAG, "enableTrustSpaceApp fail: " + e.getMessage());
        }
    }

    private void loadSystemPackages() {
        ArraySet<Integer> tempUidSet = new ArraySet();
        ArraySet<String> tempPkgSet = new ArraySet();
        List<ApplicationInfo> allApp = null;
        try {
            allApp = AppGlobals.getPackageManager().getInstalledApplications(8192, 0).getList();
        } catch (RemoteException e) {
            Slog.e(TAG, "Get Installed Applications fail");
        }
        if (allApp != null) {
            for (ApplicationInfo app : allApp) {
                if (app.isSystemApp() && (app.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0) {
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

    private void initTrustSpace() {
        if (!this.mSystemReady) {
            Slog.i(TAG, "TrustSpaceManagerService init begin");
            if (isSystemPackageInstalled(HW_TRUSTSPACE_PACKAGENAME, 0)) {
                synchronized (this) {
                    this.mSettings.readPackages();
                }
                this.mSupportTrustSpace = isSupportTrustSpaceInner();
                if (this.mSupportTrustSpace) {
                    loadSystemPackages();
                    this.mMyPackageMonitor.register(this.mContext, null, UserHandle.SYSTEM, true);
                    IntentFilter broadcastFilter = new IntentFilter();
                    broadcastFilter.addAction("android.intent.action.USER_ADDED");
                    this.mContext.registerReceiver(new MyBroadcastReceiver(), broadcastFilter);
                    this.mSettingsObserver = new MySettingsObserver();
                    this.mSettingsObserver.registerContentObserver();
                    this.mEnableTrustSpace = isTrustSpaceEnable();
                }
                Slog.i(TAG, "Enable TrustSpace App: " + this.mSupportTrustSpace);
                enableTrustSpaceApp(this.mSupportTrustSpace, this.mEnableTrustSpace, 0);
                this.mSystemReady = true;
                Slog.i(TAG, "TrustSpaceManagerService init end");
                return;
            }
            Slog.e(TAG, "TrustSpace application is not exist");
        }
    }

    private boolean isValidUser(int type, int userId) {
        int i = 1;
        boolean isValidUser = userId == 0 ? true : HW_DEBUG;
        if (type != 1) {
            return isValidUser;
        }
        if (userId != RESULT_CODE_ERROR) {
            i = 0;
        }
        return isValidUser | i;
    }

    private boolean isSpecialCaller(String packageName, int uid) {
        if (packageName != null) {
            return this.mSystemApps.contains(packageName);
        }
        if (uid < LifeCycleStateMachine.TIME_OUT_TIME || this.mSystemUids.contains(Integer.valueOf(uid))) {
            return true;
        }
        return HW_DEBUG;
    }

    private boolean isSelfCall(String calleePackage, String callingPackage) {
        return calleePackage != null ? calleePackage.equals(callingPackage) : HW_DEBUG;
    }

    private boolean isSpecialCallee(String packageName) {
        return this.mSystemApps.contains(packageName);
    }

    private boolean shouldNotify(int type, String target) {
        if ((type == 0 || type == 2 || type == 3) && !isMaliciousApp(target)) {
            return true;
        }
        return HW_DEBUG;
    }

    private boolean checkIntent(int type, String calleePackage, int callerUid, int callerPid, String callingPackage, int userId) {
        if (!isUseTrustSpace() || !isValidUser(type, userId) || isSpecialCaller(callingPackage, callerUid) || isSelfCall(calleePackage, callingPackage) || isSpecialCallee(calleePackage)) {
            return HW_DEBUG;
        }
        String typeString = TrustSpaceSettings.componentTypeToString(type);
        if (calleePackage == null || callingPackage == null) {
            Slog.w(TAG, "unknown Intent, type: " + typeString + " calleePackage: " + calleePackage + " callerPid:" + callerPid + " callerUid:" + callerUid + " callerPackage:" + callingPackage + " userId:" + userId);
            return HW_DEBUG;
        }
        synchronized (this) {
            boolean isTrustcallee = this.mSettings.isTrustApp(calleePackage);
            boolean isTrustcaller = this.mSettings.isTrustApp(callingPackage);
            int calleeLevel = this.mSettings.getProtectionLevel(calleePackage) & Utils.MAXINUM_TEMPERATURE;
            int callerLevel = this.mSettings.getProtectionLevel(callingPackage) & Utils.MAXINUM_TEMPERATURE;
        }
        if (calleeLevel == 0 && callerLevel == 0) {
            return HW_DEBUG;
        }
        if (HW_DEBUG) {
            Slog.d(TAG, "check Intent, type: " + typeString + " calleePackage: " + calleePackage + " calleelevel=" + calleeLevel + " callerPid:" + callerPid + " callerUid:" + callerUid + " callerPackage:" + callingPackage + " callerLevel=" + callerLevel + " userId:" + userId);
        }
        boolean needPrevent = HW_DEBUG;
        if (calleeLevel == 1) {
            if (callerLevel != 0 || isTrustcaller) {
                return HW_DEBUG;
            }
            if (isMaliciousApp(callingPackage)) {
                needPrevent = true;
            }
        } else if (callerLevel == 1) {
            if (calleeLevel != 0 || isTrustcallee) {
                return HW_DEBUG;
            }
            if (isMaliciousApp(calleePackage)) {
                needPrevent = true;
            }
        } else if (calleeLevel == 2) {
            if (callerLevel != 0 || isTrustcaller) {
                return HW_DEBUG;
            }
            needPrevent = true;
            if (shouldNotify(type, callingPackage)) {
                notifyIntentPrevented(typeString, calleePackage, calleeLevel, callingPackage, callerLevel);
            }
        } else if (callerLevel == 2) {
            if (calleeLevel != 0 || isTrustcallee) {
                return HW_DEBUG;
            }
            needPrevent = true;
            if (shouldNotify(type, calleePackage)) {
                notifyIntentPrevented(typeString, calleePackage, calleeLevel, callingPackage, callerLevel);
            }
        }
        if (needPrevent) {
            Slog.i(TAG, "prevent Intent, type: " + typeString + " calleePackage: " + calleePackage + " calleelevel=" + calleeLevel + " callerPid:" + callerPid + " callerUid:" + callerUid + " callerPackage:" + callingPackage + " callerLevel=" + callerLevel + " userId:" + userId);
        }
        return needPrevent;
    }

    private void notifyIntentPrevented(String typeString, String calleePackage, int calleeLevel, String callerPackage, int callerLevel) {
        UiThread.getHandler().post(new AnonymousClass2(typeString, calleePackage, callerPackage, calleeLevel, callerLevel));
    }

    private boolean isIntentProtectedAppInner(String packageName) {
        if (!calledFromValidUser() || packageName == null) {
            return HW_DEBUG;
        }
        boolean isIntentProtectedApp;
        synchronized (this) {
            isIntentProtectedApp = this.mSettings.isIntentProtectedApp(packageName);
        }
        return isIntentProtectedApp;
    }

    public boolean addIntentProtectedApps(List<String> packages, int flags) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser()) {
            return HW_DEBUG;
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
            return HW_DEBUG;
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
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser()) {
            return new ArrayList(0);
        }
        List<String> intentProtectedApps;
        synchronized (this) {
            intentProtectedApps = this.mSettings.getIntentProtectedApps(flags);
        }
        return intentProtectedApps;
    }

    public boolean removeIntentProtectedApps(List<String> packages, int flags) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser()) {
            return HW_DEBUG;
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
            return HW_DEBUG;
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
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser() || packageName == null) {
            return HW_DEBUG;
        }
        boolean isIntentProtectedApp;
        synchronized (this) {
            isIntentProtectedApp = this.mSettings.isIntentProtectedApp(packageName);
        }
        return isIntentProtectedApp;
    }

    public int getProtectionLevel(String packageName) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser() || packageName == null) {
            return 0;
        }
        int protectionLevel;
        synchronized (this) {
            protectionLevel = this.mSettings.getProtectionLevel(packageName);
        }
        return protectionLevel;
    }

    public boolean isSupportTrustSpace() {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (calledFromValidUser()) {
            return this.mSupportTrustSpace;
        }
        return HW_DEBUG;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isHwTrustSpace(int userId) {
        boolean z = HW_DEBUG;
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser()) {
            return HW_DEBUG;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = ((IUserManager) ServiceManager.getService("user")).getUserInfo(userId);
            if (userInfo != null) {
                z = userInfo.isHwTrustSpace();
            }
            Binder.restoreCallingIdentity(ident);
            return z;
        } catch (RemoteException e) {
            Slog.d(TAG, "failed to getUserInfo");
            return HW_DEBUG;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }
}
