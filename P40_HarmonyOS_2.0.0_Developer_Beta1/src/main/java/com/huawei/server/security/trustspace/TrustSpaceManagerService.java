package com.huawei.server.security.trustspace;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.trustspace.ITrustSpaceManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.ApplicationInfoEx;
import com.huawei.android.content.pm.IPackageManagerEx;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.internal.os.BackgroundThreadEx;
import com.huawei.server.UiThreadEx;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.server.security.permissionmanager.util.PermConst;
import com.huawei.util.LogEx;
import java.util.ArrayList;
import java.util.List;

public class TrustSpaceManagerService extends ITrustSpaceManager.Stub implements IHwSecurityPlugin {
    private static final String ACTION_INTENT_PREVENTED = "huawei.intent.action.TRUSTSPACE_INTENT_PREVENTED";
    private static final String ACTION_PACKAGE_ADDED = "huawei.intent.action.TRUSTSPACE_PACKAGE_ADDED";
    private static final String ACTION_PACKAGE_REMOVED = "huawei.intent.action.TRUSTSPACE_PACKAGE_REMOVED";
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.trustspace.TrustSpaceManagerService.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            if (TrustSpaceManagerService.IS_HW_DEBUG) {
                Log.d(TrustSpaceManagerService.TAG, "createPlugin");
            }
            return new TrustSpaceManagerService(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return TrustSpaceManagerService.MANAGE_TRUSTSPACE;
        }
    };
    private static final int DEFAULT_FLAG = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int FLAG_MATCH_ALL_BADGE_APPS = 8;
    private static final String HW_APP_MARKET_PACKAGE_NAME = "com.huawei.appmarket";
    private static final String HW_TRUSTSPACE_LAUNCHER = "com.huawei.trustspace.mainscreen.LoadActivity";
    private static final String HW_TRUSTSPACE_PACKAGE_NAME = "com.huawei.trustspace";
    private static final int INVALID_CLONED_PROFILE = -1000;
    private static final boolean IS_HW_DEBUG = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final boolean IS_SUPPORT_CLONE_APP = SystemPropertiesEx.getBoolean("ro.config.hw_support_clone_app", false);
    private static final String KEY_TRUST_SPACE_BADGE_SWITCH = "trust_secure_hint_enable";
    private static final String MANAGE_TRUSTSPACE = "com.huawei.permission.MANAGE_TRUSTSPACE";
    private static final int PACKAGE_PERMANENT_CHANGE = 3;
    private static final int PACKAGE_TEMPORARY_CHANGE = 2;
    private static final int PACKAGE_UPDATING = 1;
    private static final String SETTINGS_TRUSTSPACE_ENABLED = "is_trustspace_enabled";
    private static final String SETTINGS_TRUSTSPACE_SWITCH = "trust_space_switch";
    private static final String TAG = "TrustSpaceManagerService";
    private static final Uri TRUSTSPACE_BADGE_SWITCH_URI = Settings.Secure.getUriFor(KEY_TRUST_SPACE_BADGE_SWITCH);
    private static final int TRUSTSPACE_DISABLED = 0;
    private static final int TRUSTSPACE_ENABLED = 1;
    private static final Uri TRUSTSPACE_ENABLED_URI = Settings.Secure.getUriFor(SETTINGS_TRUSTSPACE_ENABLED);
    private static final int TRUST_SPACE_BADGE_STATUS_ENABLE = 1;
    private int mBadgeSwitchStatus;
    private int mClonedProfileUserId;
    private Context mContext;
    private volatile boolean mIsSystemReady = false;
    private boolean mIsTrustSpaceEnabled = false;
    private boolean mIsTrustSpaceSupported = false;
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    private TrustSpaceSettings mSettings;
    private final MySettingsObserver mSettingsObserver = new MySettingsObserver();
    private final ArraySet<String> mSystemApps = new ArraySet<>(10);
    private final ArraySet<Integer> mSystemUids = new ArraySet<>(10);
    private final ArrayMap<String, Integer> mVirusScanResults = new ArrayMap<>(10);

    public TrustSpaceManagerService(Context context) {
        this.mContext = context;
        this.mSettings = new TrustSpaceSettings();
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        if (IS_HW_DEBUG) {
            Log.d(TAG, "TrustSpaceManagerService Start");
        }
        LocalServicesExt.addService(TrustSpaceManagerInternal.class, new LocalServiceImpl());
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.trustspace.TrustSpaceManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    private final class LocalServiceImpl implements TrustSpaceManagerInternal {
        private LocalServiceImpl() {
        }

        @Override // com.huawei.server.security.trustspace.TrustSpaceManagerInternal
        public boolean checkIntent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
            return TrustSpaceManagerService.this.checkIntent(type, calleePackage, callerUid, callerPid, callerPackage, userId);
        }

        @Override // com.huawei.server.security.trustspace.TrustSpaceManagerInternal
        public void initTrustSpace() {
            TrustSpaceManagerService.this.initTrustSpace();
        }

        @Override // com.huawei.server.security.trustspace.TrustSpaceManagerInternal
        public boolean isIntentProtectedApp(String packageName) {
            return TrustSpaceManagerService.this.isIntentProtectedAppInner(packageName);
        }

        @Override // com.huawei.server.security.trustspace.TrustSpaceManagerInternal
        public int getProtectionLevel(String packageName) {
            return TrustSpaceManagerService.this.getProtectionLevel(packageName);
        }
    }

    /* access modifiers changed from: private */
    public final class MyPackageMonitor extends BroadcastReceiver {
        IntentFilter mPackageFilter = new IntentFilter();
        Context mRegisteredContext;
        Handler mRegisteredHandler;

        MyPackageMonitor() {
            this.mPackageFilter.addAction("android.intent.action.PACKAGE_ADDED");
            this.mPackageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            this.mPackageFilter.addDataScheme("package");
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("android.intent.extra.user_handle", -10000) == -10000) {
                Log.w("PackageMonitor", "Intent broadcast does not contain user handle: " + intent);
                return;
            }
            String action = intent.getAction();
            Uri uri = intent.getData();
            String pkg = uri != null ? uri.getSchemeSpecificPart() : null;
            if (pkg != null) {
                int uid = intent.getIntExtra("android.intent.extra.UID", 0);
                if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                        TrustSpaceManagerService.this.handlePackageAppeared(pkg, 1, uid);
                    } else {
                        TrustSpaceManagerService.this.handlePackageAppeared(pkg, 3, uid);
                    }
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    TrustSpaceManagerService.this.handlePackageDisappeared(pkg, 3, uid);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void register(Context context, UserHandle user, Handler handler) {
            if (this.mRegisteredContext == null) {
                this.mRegisteredContext = context;
                if (handler != null) {
                    this.mRegisteredHandler = handler;
                    if (user != null) {
                        ContextEx.registerReceiverAsUser(context, this, user, this.mPackageFilter, (String) null, this.mRegisteredHandler);
                    } else {
                        context.registerReceiver(this, this.mPackageFilter, null, this.mRegisteredHandler);
                    }
                } else {
                    throw new NullPointerException();
                }
            } else {
                throw new IllegalStateException("Already registered");
            }
        }
    }

    /* access modifiers changed from: private */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        private MyBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_ADDED".equals(intent.getAction())) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if (TrustSpaceManagerService.IS_HW_DEBUG) {
                    Log.d(TrustSpaceManagerService.TAG, "action add user:" + userId);
                }
                if (!(userId == -10000 || userId == 0)) {
                    TrustSpaceManagerService.this.enableTrustSpaceApp(false, false, userId);
                }
                if (TrustSpaceManagerService.this.isClonedProfile(userId)) {
                    TrustSpaceManagerService.this.updateClonedProfileUserId(userId);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class MySettingsObserver extends ContentObserver {
        MySettingsObserver() {
            super(new Handler());
        }

        /* access modifiers changed from: package-private */
        public void registerContentObserver() {
            ContentResolver resolver = TrustSpaceManagerService.this.mContext.getContentResolver();
            ContentResolverExt.registerContentObserver(resolver, TrustSpaceManagerService.TRUSTSPACE_ENABLED_URI, false, this, 0);
            ContentResolverExt.registerContentObserver(resolver, TrustSpaceManagerService.TRUSTSPACE_BADGE_SWITCH_URI, false, this, 0);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            int i = 0;
            if (TrustSpaceManagerService.TRUSTSPACE_ENABLED_URI.equals(uri)) {
                boolean isEnabled = SettingsEx.Secure.getIntForUser(TrustSpaceManagerService.this.mContext.getContentResolver(), TrustSpaceManagerService.SETTINGS_TRUSTSPACE_ENABLED, 1, 0) == 1;
                if (TrustSpaceManagerService.IS_HW_DEBUG) {
                    Log.i(TrustSpaceManagerService.TAG, "TrustSpace Enabled = " + isEnabled);
                }
                TrustSpaceManagerService.this.mIsTrustSpaceEnabled = isEnabled;
                if (TrustSpaceManagerService.this.mIsTrustSpaceSupported) {
                    TrustSpaceManagerService.this.enableTrustSpaceApp(true, isEnabled, 0);
                    ContentResolver contentResolver = TrustSpaceManagerService.this.mContext.getContentResolver();
                    if (isEnabled) {
                        i = 1;
                    }
                    Settings.System.putInt(contentResolver, TrustSpaceManagerService.SETTINGS_TRUSTSPACE_SWITCH, i);
                }
            } else if (TrustSpaceManagerService.TRUSTSPACE_BADGE_SWITCH_URI.equals(uri)) {
                TrustSpaceManagerService trustSpaceManagerService = TrustSpaceManagerService.this;
                trustSpaceManagerService.mBadgeSwitchStatus = SettingsEx.Secure.getIntForUser(trustSpaceManagerService.mContext.getContentResolver(), TrustSpaceManagerService.KEY_TRUST_SPACE_BADGE_SWITCH, 1, 0);
            } else {
                Log.w(TrustSpaceManagerService.TAG, "illegal uri");
            }
        }
    }

    /* access modifiers changed from: private */
    public class ComponentInfo {
        private boolean mIsHarmful;
        private boolean mIsTrust;
        private String mPackageName;
        private int mProtectionLevel;

        private ComponentInfo(String packageName, int protectionLevel, boolean isTrust, boolean isHarmful) {
            this.mPackageName = packageName;
            this.mProtectionLevel = protectionLevel;
            this.mIsTrust = isTrust;
            this.mIsHarmful = isHarmful;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getPackageName() {
            return this.mPackageName;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getProtectionLevel() {
            return this.mProtectionLevel;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean getIsTrust() {
            return this.mIsTrust;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean getIsHarmful() {
            return this.mIsHarmful;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePackageAppeared(String packageName, int reason, int uid) {
        if (isUseTrustSpace()) {
            int userId = UserHandleEx.getUserId(uid);
            if (userId == 0 || isClonedProfile(userId)) {
                if (IS_HW_DEBUG) {
                    Log.d(TAG, "onPackageAppeared:" + packageName + " reason=" + reason + " uid=" + uid);
                }
                if (userId == 0) {
                    removeVirusScanResult(packageName);
                }
                Intent intent = new Intent(ACTION_PACKAGE_ADDED);
                intent.putExtra(PermConst.PACKAGE_NAME, packageName);
                intent.putExtra("reason", reason);
                intent.putExtra(PermConst.USER_ID, userId);
                intent.setPackage(HW_TRUSTSPACE_PACKAGE_NAME);
                this.mContext.startService(intent);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePackageDisappeared(String packageName, int reason, int uid) {
        if (isUseTrustSpace()) {
            int userId = UserHandleEx.getUserId(uid);
            if (userId == 0 || isClonedProfile(userId)) {
                if (IS_HW_DEBUG) {
                    Log.d(TAG, "handlePackageDisappeared:" + packageName + " reason=" + reason + " uid=" + uid);
                }
                if (userId == 0) {
                    removeVirusScanResult(packageName);
                    synchronized (this) {
                        boolean isConfigChanged = false;
                        boolean isProtectedApp = this.mSettings.isIntentProtectedApp(packageName);
                        boolean isTrustApp = this.mSettings.isTrustApp(packageName);
                        if (isProtectedApp && reason == 3) {
                            this.mSettings.removeIntentProtectedApp(packageName);
                            isConfigChanged = true;
                        }
                        if (isTrustApp && reason == 3) {
                            this.mSettings.removeTrustApp(packageName);
                            isConfigChanged = true;
                        }
                        if (isConfigChanged) {
                            this.mSettings.writePackages();
                        }
                    }
                }
                Intent intent = new Intent(ACTION_PACKAGE_REMOVED);
                intent.putExtra(PermConst.PACKAGE_NAME, packageName);
                intent.putExtra("reason", reason);
                intent.putExtra(PermConst.USER_ID, userId);
                intent.setPackage(HW_TRUSTSPACE_PACKAGE_NAME);
                this.mContext.startService(intent);
            }
        }
    }

    private boolean isTrustSpaceEnable() {
        return SettingsEx.System.getIntForUser(this.mContext.getContentResolver(), SETTINGS_TRUSTSPACE_SWITCH, 1, 0) == 1;
    }

    private boolean isUseTrustSpace() {
        return this.mIsTrustSpaceSupported && this.mIsTrustSpaceEnabled;
    }

    private boolean calledFromValidUser() {
        int uid = Binder.getCallingUid();
        int userId = UserHandleEx.getUserId(uid);
        if (uid == 1000 || userId == 0) {
            return true;
        }
        return false;
    }

    private void removeVirusScanResult(String packageName) {
        synchronized (this.mVirusScanResults) {
            this.mVirusScanResults.remove(packageName);
        }
    }

    private boolean isSystemPackageInstalled(String packageName, int flag) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = IPackageManagerEx.getApplicationInfo(packageName, flag, 0);
        } catch (RemoteException e) {
            Log.d(TAG, "Can't find package:" + packageName);
        }
        if (appInfo == null || !isSystemApp(appInfo)) {
            return false;
        }
        return true;
    }

    private boolean isSystemApp(ApplicationInfo info) {
        return (info.flags & 1) != 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableTrustSpaceApp(boolean isPackageEnabled, boolean isLauncherEnabled, int userId) {
        ComponentName componentName = new ComponentName(HW_TRUSTSPACE_PACKAGE_NAME, HW_TRUSTSPACE_LAUNCHER);
        int newLauncherState = 2;
        int newPackageState = isPackageEnabled ? 0 : 2;
        if (isLauncherEnabled) {
            newLauncherState = 0;
        }
        try {
            IPackageManagerEx.setApplicationEnabledSetting(HW_TRUSTSPACE_PACKAGE_NAME, newPackageState, 0, userId, (String) null);
            if (isPackageEnabled) {
                IPackageManagerEx.setComponentEnabledSetting(componentName, newLauncherState, 1, userId);
            }
        } catch (Exception e) {
            Log.e(TAG, "enableTrustSpaceApp fail");
        }
    }

    private void loadSystemPackages() {
        ArraySet<Integer> tempUidSet = new ArraySet<>();
        ArraySet<String> tempPkgSet = new ArraySet<>();
        List<ApplicationInfo> allApps = PackageManagerExt.getInstalledApplicationsAsUser(this.mContext.getPackageManager(), 8192, 0);
        if (allApps != null) {
            int size = allApps.size();
            for (int i = 0; i < size; i++) {
                ApplicationInfo app = allApps.get(i);
                ApplicationInfoEx infoEx = new ApplicationInfoEx(app);
                if (isSystemApp(app) && (infoEx.getHwFlags() & 33554432) == 0) {
                    tempUidSet.add(Integer.valueOf(app.uid));
                    tempPkgSet.add(app.packageName);
                }
            }
            this.mSystemUids.clear();
            this.mSystemUids.addAll((ArraySet<? extends Integer>) tempUidSet);
            this.mSystemApps.clear();
            this.mSystemApps.addAll((ArraySet<? extends String>) tempPkgSet);
        }
    }

    private int getClonedProfileId() {
        if (!IS_SUPPORT_CLONE_APP) {
            return INVALID_CLONED_PROFILE;
        }
        for (UserInfoExt info : UserManagerExt.getProfiles(UserManagerExt.get(this.mContext), 0)) {
            if (info.isClonedProfile()) {
                return info.getUserId();
            }
        }
        Log.d(TAG, "Cloned Profile is not exist");
        return INVALID_CLONED_PROFILE;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isClonedProfile(int userId) {
        UserInfoExt userInfo;
        if (IS_SUPPORT_CLONE_APP && (userInfo = UserManagerExt.getUserInfoEx((UserManager) this.mContext.getSystemService("user"), userId)) != null && userInfo.isClonedProfile()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateClonedProfileUserId(int userId) {
        if (IS_SUPPORT_CLONE_APP && userId != INVALID_CLONED_PROFILE) {
            this.mClonedProfileUserId = userId;
            if (IS_HW_DEBUG) {
                Log.i(TAG, "Cloned profile user:" + userId);
            }
        }
    }

    private boolean checkClonedProfile(int userId) {
        if (!IS_SUPPORT_CLONE_APP || userId == INVALID_CLONED_PROFILE || userId != this.mClonedProfileUserId) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initTrustSpace() {
        if (!this.mIsSystemReady) {
            if (IS_HW_DEBUG) {
                Log.i(TAG, "TrustSpaceManagerService init begin");
            }
            if (!isSystemPackageInstalled(HW_TRUSTSPACE_PACKAGE_NAME, 0)) {
                Log.e(TAG, "TrustSpace application does not exist");
                return;
            }
            this.mIsTrustSpaceSupported = isSystemPackageInstalled(HW_APP_MARKET_PACKAGE_NAME, 0);
            if (this.mIsTrustSpaceSupported) {
                synchronized (this) {
                    this.mSettings.readPackages();
                }
                loadSystemPackages();
                this.mMyPackageMonitor.register(this.mContext, UserHandleEx.ALL, BackgroundThreadEx.getHandler());
                this.mSettingsObserver.registerContentObserver();
                updateClonedProfileUserId(getClonedProfileId());
                this.mIsTrustSpaceEnabled = isTrustSpaceEnable();
            }
            IntentFilter broadcastFilter = new IntentFilter();
            broadcastFilter.addAction("android.intent.action.USER_ADDED");
            this.mContext.registerReceiver(new MyBroadcastReceiver(), broadcastFilter);
            if (IS_HW_DEBUG) {
                Log.i(TAG, "Enable TrustSpace App: " + this.mIsTrustSpaceSupported);
            }
            if (this.mIsTrustSpaceSupported) {
                enableTrustSpaceApp(true, this.mIsTrustSpaceEnabled, 0);
            } else {
                disableTrustSpaceForAllUsers();
            }
            this.mIsSystemReady = true;
            this.mBadgeSwitchStatus = SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), KEY_TRUST_SPACE_BADGE_SWITCH, 1, 0);
            if (IS_HW_DEBUG) {
                Log.i(TAG, "TrustSpaceManagerService init end");
            }
        }
    }

    private void disableTrustSpaceForAllUsers() {
        for (UserInfoExt user : UserManagerExt.getUsers(UserManagerExt.get(this.mContext), false)) {
            enableTrustSpaceApp(false, false, user.getUserId());
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

    private boolean isSelfCall(String calleePackage, String callerPackage) {
        return calleePackage != null && calleePackage.equals(callerPackage);
    }

    private boolean isSpecialCallee(String packageName) {
        return this.mSystemApps.contains(packageName);
    }

    private boolean isSpecialPackage(String callerPackage, int callerUid, String calleePackage) {
        boolean isCallerIntentProtected;
        boolean isCalleeIntentProtected;
        boolean isSpecialCaller = isSpecialCaller(callerPackage, callerUid);
        boolean isSpecialCallee = isSpecialCallee(calleePackage);
        synchronized (this) {
            isCallerIntentProtected = this.mSettings.isIntentProtectedApp(callerPackage);
            isCalleeIntentProtected = this.mSettings.isIntentProtectedApp(calleePackage);
        }
        return (isSpecialCaller && !isCallerIntentProtected) || (isSpecialCallee && !isCalleeIntentProtected);
    }

    private boolean shouldNotify(int type, boolean isHarmful) {
        if ((type == 0 || type == 2 || type == 3) && !isHarmful) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkIntent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        Throwable th;
        boolean isTrustCallee;
        boolean isTrustCaller;
        int calleeLevel;
        int callerLevel;
        boolean isCalleeHarmful;
        boolean isCallerHarmful;
        boolean z = false;
        if (isUseTrustSpace() && isValidUser(type, userId) && !isSelfCall(calleePackage, callerPackage)) {
            if (!isSpecialPackage(callerPackage, callerUid, calleePackage)) {
                String typeString = TrustSpaceSettings.componentTypeToString(type);
                if (calleePackage != null) {
                    if (callerPackage != null) {
                        synchronized (this) {
                            try {
                                isTrustCallee = this.mSettings.isTrustApp(calleePackage);
                                try {
                                    isTrustCaller = this.mSettings.isTrustApp(callerPackage);
                                    try {
                                        calleeLevel = this.mSettings.getProtectionLevel(calleePackage) & 255;
                                    } catch (Throwable th2) {
                                        th = th2;
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
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                                try {
                                    callerLevel = this.mSettings.getProtectionLevel(callerPackage) & 255;
                                    try {
                                        isCalleeHarmful = this.mSettings.isHarmfulApp(calleePackage);
                                    } catch (Throwable th5) {
                                        th = th5;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th6) {
                                    th = th6;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                                try {
                                    isCallerHarmful = this.mSettings.isHarmfulApp(callerPackage);
                                } catch (Throwable th7) {
                                    th = th7;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th8) {
                                th = th8;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        if (calleeLevel == 0 && callerLevel == 0) {
                            return false;
                        }
                        if (IS_HW_DEBUG) {
                            Log.d(TAG, "check Intent, type: " + typeString + " calleePackage: " + calleePackage + " calleeLevel=" + calleeLevel + " callerPid:" + callerPid + " callerUid:" + callerUid + " callerPackage:" + callerPackage + " callerLevel=" + callerLevel + " userId:" + userId);
                        }
                        if (isCalleeHarmful && IS_HW_DEBUG) {
                            Log.i(TAG, "find calleePackage Malicious App:" + calleePackage);
                        }
                        if (isCallerHarmful && IS_HW_DEBUG) {
                            Log.i(TAG, "find callerPackage Malicious App:" + callerPackage);
                        }
                        boolean shouldPrevent = checkComponentLevel(new ComponentInfo(callerPackage, callerLevel, isTrustCaller, isCallerHarmful), new ComponentInfo(calleePackage, calleeLevel, isTrustCallee, isCalleeHarmful), type);
                        if (shouldPrevent) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("prevent Intent, type: ");
                            sb.append(typeString);
                            sb.append(" calleePackage: ");
                            sb.append(calleePackage);
                            sb.append(" calleeLevel=");
                            sb.append(calleeLevel);
                            sb.append(" callerPid:");
                            sb.append(callerPid);
                            sb.append(" callerUid:");
                            sb.append(callerUid);
                            sb.append(" callerPackage:");
                            sb.append(callerPackage);
                            sb.append(" callerLevel=");
                            sb.append(callerLevel);
                            sb.append(" isMalicious=");
                            if (isCalleeHarmful || isCallerHarmful) {
                                z = true;
                            }
                            sb.append(z);
                            sb.append(" userId:");
                            sb.append(userId);
                            Log.i(TAG, sb.toString());
                        }
                        return shouldPrevent;
                    }
                }
                Log.e(TAG, "checkIntent calleePackage or callerPackage is null!");
                return false;
            }
        }
        return false;
    }

    private boolean checkComponentLevel(ComponentInfo caller, ComponentInfo callee, int type) {
        int calleeLevel = callee.getProtectionLevel();
        int callerLevel = caller.getProtectionLevel();
        boolean isTrustCallee = callee.getIsTrust();
        boolean isTrustCaller = caller.getIsTrust();
        boolean isCalleeHarmful = callee.getIsHarmful();
        boolean isCallerHarmful = caller.getIsHarmful();
        boolean shouldPrevent = false;
        boolean shouldNotify = false;
        if (calleeLevel == 1) {
            if (callerLevel != 0 || isTrustCaller) {
                return false;
            }
            shouldPrevent = isCallerHarmful;
        } else if (callerLevel == 1) {
            if (calleeLevel != 0 || isTrustCallee) {
                return false;
            }
            shouldPrevent = isCalleeHarmful;
        } else if (calleeLevel == 2) {
            if (callerLevel != 0 || isTrustCaller) {
                return false;
            }
            shouldPrevent = isCallerHarmful;
            if (shouldNotify(type, isCallerHarmful)) {
                shouldNotify = true;
            }
        } else if (callerLevel != 2) {
            Log.w(TAG, "incorrect protection level!");
        } else if (!isCalleeHarmful) {
            return false;
        } else {
            shouldPrevent = true;
        }
        String typeString = TrustSpaceSettings.componentTypeToString(type);
        if ((isCalleeHarmful || isCallerHarmful) && type != 1) {
            notifyIntentPrevented(typeString, caller, callee, true);
        } else if (shouldNotify) {
            notifyIntentPrevented(typeString, caller, callee, false);
        } else {
            Log.w(TAG, "not notify intent prevented!");
        }
        return shouldPrevent;
    }

    private void notifyIntentPrevented(final String typeString, final ComponentInfo caller, final ComponentInfo callee, final boolean isMalicious) {
        UiThreadEx.getHandler().post(new Runnable() {
            /* class com.huawei.server.security.trustspace.TrustSpaceManagerService.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                Intent intent = new Intent(TrustSpaceManagerService.ACTION_INTENT_PREVENTED);
                intent.putExtra("component", typeString);
                intent.putExtra("callee", callee.getPackageName());
                intent.putExtra("caller", caller.getPackageName());
                intent.putExtra("calleeLevel", callee.getProtectionLevel());
                intent.putExtra("callerLevel", caller.getProtectionLevel());
                intent.putExtra("isMalicious", isMalicious);
                intent.setPackage(TrustSpaceManagerService.HW_TRUSTSPACE_PACKAGE_NAME);
                TrustSpaceManagerService.this.mContext.startService(intent);
                if (TrustSpaceManagerService.IS_HW_DEBUG) {
                    Log.d(TrustSpaceManagerService.TAG, "Notify intent prevented.");
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isIntentProtectedAppInner(String packageName) {
        boolean isIntentProtectedApp;
        if (!calledFromValidUser() || packageName == null) {
            return false;
        }
        synchronized (this) {
            isIntentProtectedApp = this.mSettings.isIntentProtectedApp(packageName);
        }
        return isIntentProtectedApp;
    }

    public boolean addIntentProtectedApps(List<String> packageNames, int flags) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this) {
            int size = packageNames.size();
            for (int i = 0; i < size; i++) {
                String packageName = packageNames.get(i);
                this.mSettings.addIntentProtectedApp(packageName, flags);
                if (IS_HW_DEBUG) {
                    Log.d(TAG, "add " + packageName + " to intent protected list, flags=" + flags);
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
            if (IS_HW_DEBUG) {
                Log.d(TAG, "remove " + packageName + " from intent protected list");
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
        if (hasNoBadgeApps(flags)) {
            return new ArrayList(0);
        }
        synchronized (this) {
            intentProtectedApps = this.mSettings.getIntentProtectedApps(flags);
        }
        return intentProtectedApps;
    }

    private boolean hasNoBadgeApps(int flags) {
        if ((flags & 8) == 0 || this.mBadgeSwitchStatus == 1) {
            return false;
        }
        return true;
    }

    public boolean removeIntentProtectedApps(List<String> packageNames, int flags) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser()) {
            return false;
        }
        synchronized (this) {
            this.mSettings.removeIntentProtectedApps(packageNames, flags);
            this.mSettings.writePackages();
            if (IS_HW_DEBUG) {
                Log.d(TAG, "remove apps in intent protected list, flag=" + flags);
            }
        }
        return true;
    }

    public boolean updateTrustApps(List<String> packageNames, int flag) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTSPACE, null);
        if (!calledFromValidUser() || packageNames == null) {
            return false;
        }
        synchronized (this) {
            this.mSettings.updateTrustApps(packageNames, flag);
            this.mSettings.writePackages();
            if (IS_HW_DEBUG) {
                Log.d(TAG, "update trust apps, flag=" + flag);
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
        return this.mIsTrustSpaceSupported;
    }

    public boolean isHwTrustSpace(int userId) {
        return false;
    }
}
