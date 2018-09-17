package com.android.server.om;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager.Stub;
import android.content.om.OverlayInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.ConcurrentUtils;
import com.android.server.FgThread;
import com.android.server.IoThread;
import com.android.server.LocalServices;
import com.android.server.SystemServerInitThreadPool;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.om.-$Lambda$Whs3NIaASrs6bpQxTTs9leTDPyo.AnonymousClass2;
import com.android.server.om.-$Lambda$Whs3NIaASrs6bpQxTTs9leTDPyo.AnonymousClass3;
import com.android.server.pm.Installer;
import com.android.server.pm.UserManagerService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public final class OverlayManagerService extends SystemService {
    static final boolean DEBUG = false;
    private static final String DEFAULT_OVERLAYS_PROP = "ro.boot.vendor.overlay.theme";
    private static final String FWK_DARK_TAG = "com.android.frameworkhwext.dark";
    static final String TAG = "OverlayManager";
    private final OverlayManagerServiceImpl mImpl;
    private Future<?> mInitCompleteSignal;
    private final Object mLock = new Object();
    private final PackageManagerHelper mPackageManager = new PackageManagerHelper();
    private final AtomicBoolean mPersistSettingsScheduled = new AtomicBoolean(false);
    private final IBinder mService = new Stub() {
        public Map<String, List<OverlayInfo>> getAllOverlays(int userId) throws RemoteException {
            Map<String, List<OverlayInfo>> overlaysForUser;
            userId = handleIncomingUser(userId, "getAllOverlays");
            synchronized (OverlayManagerService.this.mLock) {
                overlaysForUser = OverlayManagerService.this.mImpl.getOverlaysForUser(userId);
            }
            return overlaysForUser;
        }

        public List<OverlayInfo> getOverlayInfosForTarget(String targetPackageName, int userId) throws RemoteException {
            userId = handleIncomingUser(userId, "getOverlayInfosForTarget");
            if (targetPackageName == null) {
                return Collections.emptyList();
            }
            List<OverlayInfo> overlayInfosForTarget;
            synchronized (OverlayManagerService.this.mLock) {
                overlayInfosForTarget = OverlayManagerService.this.mImpl.getOverlayInfosForTarget(targetPackageName, userId);
            }
            return overlayInfosForTarget;
        }

        public OverlayInfo getOverlayInfo(String packageName, int userId) throws RemoteException {
            userId = handleIncomingUser(userId, "getOverlayInfo");
            if (packageName == null) {
                return null;
            }
            OverlayInfo overlayInfo;
            synchronized (OverlayManagerService.this.mLock) {
                overlayInfo = OverlayManagerService.this.mImpl.getOverlayInfo(packageName, userId);
            }
            return overlayInfo;
        }

        public boolean setEnabled(String packageName, boolean enable, int userId) throws RemoteException {
            enforceChangeOverlayPackagesPermission("setEnabled");
            userId = handleIncomingUser(userId, "setEnabled");
            if (packageName == null) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                boolean enabled;
                synchronized (OverlayManagerService.this.mLock) {
                    enabled = OverlayManagerService.this.mImpl.setEnabled(packageName, enable, userId);
                }
                return enabled;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setEnabledExclusive(String packageName, boolean enable, int userId) throws RemoteException {
            enforceChangeOverlayPackagesPermission("setEnabled");
            userId = handleIncomingUser(userId, "setEnabled");
            if (packageName == null || (enable ^ 1) != 0) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                boolean enabledExclusive;
                synchronized (OverlayManagerService.this.mLock) {
                    enabledExclusive = OverlayManagerService.this.mImpl.setEnabledExclusive(packageName, userId);
                }
                return enabledExclusive;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setPriority(String packageName, String parentPackageName, int userId) throws RemoteException {
            enforceChangeOverlayPackagesPermission("setPriority");
            userId = handleIncomingUser(userId, "setPriority");
            if (packageName == null || parentPackageName == null) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                boolean priority;
                synchronized (OverlayManagerService.this.mLock) {
                    priority = OverlayManagerService.this.mImpl.setPriority(packageName, parentPackageName, userId);
                }
                return priority;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setHighestPriority(String packageName, int userId) throws RemoteException {
            enforceChangeOverlayPackagesPermission("setHighestPriority");
            userId = handleIncomingUser(userId, "setHighestPriority");
            if (packageName == null) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                boolean highestPriority;
                synchronized (OverlayManagerService.this.mLock) {
                    highestPriority = OverlayManagerService.this.mImpl.setHighestPriority(packageName, userId);
                }
                return highestPriority;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setLowestPriority(String packageName, int userId) throws RemoteException {
            enforceChangeOverlayPackagesPermission("setLowestPriority");
            userId = handleIncomingUser(userId, "setLowestPriority");
            if (packageName == null) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                boolean lowestPriority;
                synchronized (OverlayManagerService.this.mLock) {
                    lowestPriority = OverlayManagerService.this.mImpl.setLowestPriority(packageName, userId);
                }
                return lowestPriority;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new OverlayManagerShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] argv) {
            enforceDumpPermission("dump");
            boolean verbose = argv.length > 0 ? "--verbose".equals(argv[0]) : false;
            synchronized (OverlayManagerService.this.mLock) {
                OverlayManagerService.this.mImpl.onDump(pw);
                OverlayManagerService.this.mPackageManager.dump(pw, verbose);
            }
        }

        private int handleIncomingUser(int userId, String message) {
            return ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, message, null);
        }

        private void enforceChangeOverlayPackagesPermission(String message) {
            OverlayManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.CHANGE_OVERLAY_PACKAGES", message);
        }

        private void enforceDumpPermission(String message) {
            OverlayManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.DUMP", message);
        }
    };
    private final OverlayManagerSettings mSettings;
    private final AtomicFile mSettingsFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), "overlays.xml"));
    private final UserManagerService mUserManager = UserManagerService.getInstance();

    private final class OverlayChangeListener implements OverlayChangeListener {
        /* synthetic */ OverlayChangeListener(OverlayManagerService this$0, OverlayChangeListener -this1) {
            this();
        }

        private OverlayChangeListener() {
        }

        public void onOverlaysChanged(String targetPackageName, int userId) {
            OverlayManagerService.this.schedulePersistSettings();
            FgThread.getHandler().post(new AnonymousClass2(userId, this, targetPackageName));
        }

        /* synthetic */ void lambda$-com_android_server_om_OverlayManagerService$OverlayChangeListener_30962(int userId, String targetPackageName) {
            OverlayManagerService.this.updateAssets(userId, targetPackageName);
            Intent intent = new Intent("android.intent.action.OVERLAY_CHANGED", Uri.fromParts(HwBroadcastRadarUtil.KEY_PACKAGE, targetPackageName, null));
            intent.setFlags(67108864);
            try {
                ActivityManager.getService().broadcastIntent(null, intent, null, null, 0, null, null, null, -1, null, false, false, userId);
            } catch (RemoteException e) {
            }
        }

        public void onPackageUpgraded(String packageName, int userId) {
            OverlayManagerService.this.schedulePersistSettings();
            FgThread.getHandler().post(new AnonymousClass3(userId, this, packageName));
        }

        /* synthetic */ void lambda$-com_android_server_om_OverlayManagerService$OverlayChangeListener_32021(int userId, String packageName) {
            OverlayManagerService.this.updateApplicationAssets(userId, packageName);
        }
    }

    private static final class PackageManagerHelper implements PackageManagerHelper {
        private static final String TAB1 = "    ";
        private static final String TAB2 = "        ";
        private final SparseArray<HashMap<String, PackageInfo>> mCache = new SparseArray();
        private final IPackageManager mPackageManager = AppGlobals.getPackageManager();
        private final PackageManagerInternal mPackageManagerInternal = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class));

        PackageManagerHelper() {
        }

        public PackageInfo getPackageInfo(String packageName, int userId, boolean useCache) {
            if (useCache) {
                PackageInfo cachedPi = getCachedPackageInfo(packageName, userId);
                if (cachedPi != null) {
                    return cachedPi;
                }
            }
            try {
                PackageInfo pi = this.mPackageManager.getPackageInfo(packageName, 0, userId);
                if (useCache && pi != null) {
                    cachePackageInfo(packageName, userId, pi);
                }
                return pi;
            } catch (RemoteException e) {
                return null;
            }
        }

        public PackageInfo getPackageInfo(String packageName, int userId) {
            return getPackageInfo(packageName, userId, true);
        }

        public boolean signaturesMatching(String packageName1, String packageName2, int userId) {
            boolean z = false;
            try {
                if (this.mPackageManager.checkSignatures(packageName1, packageName2) == 0) {
                    z = true;
                }
                return z;
            } catch (RemoteException e) {
                return false;
            }
        }

        public List<PackageInfo> getOverlayPackages(int userId) {
            return this.mPackageManagerInternal.getOverlayPackages(userId);
        }

        public PackageInfo getCachedPackageInfo(String packageName, int userId) {
            HashMap<String, PackageInfo> map = (HashMap) this.mCache.get(userId);
            if (map == null) {
                return null;
            }
            return (PackageInfo) map.get(packageName);
        }

        public void cachePackageInfo(String packageName, int userId, PackageInfo pi) {
            HashMap<String, PackageInfo> map = (HashMap) this.mCache.get(userId);
            if (map == null) {
                map = new HashMap();
                this.mCache.put(userId, map);
            }
            map.put(packageName, pi);
        }

        public void forgetPackageInfo(String packageName, int userId) {
            HashMap<String, PackageInfo> map = (HashMap) this.mCache.get(userId);
            if (map != null) {
                map.remove(packageName);
                if (map.isEmpty()) {
                    this.mCache.delete(userId);
                }
            }
        }

        public void forgetAllPackageInfos(int userId) {
            this.mCache.delete(userId);
        }

        public void dump(PrintWriter pw, boolean verbose) {
            pw.println("PackageInfo cache");
            int N;
            int i;
            if (!verbose) {
                int count = 0;
                N = this.mCache.size();
                for (i = 0; i < N; i++) {
                    count += ((HashMap) this.mCache.get(this.mCache.keyAt(i))).size();
                }
                pw.println(TAB1 + count + " package(s)");
            } else if (this.mCache.size() == 0) {
                pw.println("    <empty>");
            } else {
                N = this.mCache.size();
                for (i = 0; i < N; i++) {
                    int userId = this.mCache.keyAt(i);
                    pw.println("    User " + userId);
                    for (Entry<String, PackageInfo> entry : ((HashMap) this.mCache.get(userId)).entrySet()) {
                        pw.println(TAB2 + ((String) entry.getKey()) + ": " + entry.getValue());
                    }
                }
            }
        }
    }

    private final class PackageReceiver extends BroadcastReceiver {
        /* synthetic */ PackageReceiver(OverlayManagerService this$0, PackageReceiver -this1) {
            this();
        }

        private PackageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Uri data = intent.getData();
            if (data == null) {
                Slog.e(OverlayManagerService.TAG, "Cannot handle package broadcast with null data");
                return;
            }
            String packageName = data.getSchemeSpecificPart();
            boolean replacing = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
            int[] userIds = intent.getIntExtra("android.intent.extra.UID", -10000) == -10000 ? OverlayManagerService.this.mUserManager.getUserIds() : new int[]{UserHandle.getUserId(intent.getIntExtra("android.intent.extra.UID", -10000))};
            String action = intent.getAction();
            if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                if (replacing) {
                    onPackageUpgraded(packageName, userIds);
                } else {
                    onPackageAdded(packageName, userIds);
                }
            } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                onPackageChanged(packageName, userIds);
            } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                if (replacing) {
                    onPackageUpgrading(packageName, userIds);
                } else {
                    onPackageRemoved(packageName, userIds);
                }
            }
        }

        private void onPackageAdded(String packageName, int[] userIds) {
            for (int userId : userIds) {
                synchronized (OverlayManagerService.this.mLock) {
                    PackageInfo pi = OverlayManagerService.this.mPackageManager.getPackageInfo(packageName, userId, false);
                    if (pi != null) {
                        OverlayManagerService.this.mPackageManager.cachePackageInfo(packageName, userId, pi);
                        if (OverlayManagerService.this.isOverlayPackage(pi)) {
                            OverlayManagerService.this.mImpl.onOverlayPackageAdded(packageName, userId);
                        } else {
                            OverlayManagerService.this.mImpl.onTargetPackageAdded(packageName, userId);
                        }
                    }
                }
            }
        }

        private void onPackageChanged(String packageName, int[] userIds) {
            for (int userId : userIds) {
                synchronized (OverlayManagerService.this.mLock) {
                    PackageInfo pi = OverlayManagerService.this.mPackageManager.getPackageInfo(packageName, userId, false);
                    if (pi != null) {
                        OverlayManagerService.this.mPackageManager.cachePackageInfo(packageName, userId, pi);
                        if (OverlayManagerService.this.isOverlayPackage(pi)) {
                            OverlayManagerService.this.mImpl.onOverlayPackageChanged(packageName, userId);
                        } else {
                            OverlayManagerService.this.mImpl.onTargetPackageChanged(packageName, userId);
                        }
                    }
                }
            }
        }

        private void onPackageUpgrading(String packageName, int[] userIds) {
            for (int userId : userIds) {
                synchronized (OverlayManagerService.this.mLock) {
                    OverlayManagerService.this.mPackageManager.forgetPackageInfo(packageName, userId);
                    if (OverlayManagerService.this.mImpl.getOverlayInfo(packageName, userId) == null) {
                        OverlayManagerService.this.mImpl.onTargetPackageUpgrading(packageName, userId);
                    } else {
                        OverlayManagerService.this.mImpl.onOverlayPackageUpgrading(packageName, userId);
                    }
                }
            }
        }

        private void onPackageUpgraded(String packageName, int[] userIds) {
            for (int userId : userIds) {
                synchronized (OverlayManagerService.this.mLock) {
                    PackageInfo pi = OverlayManagerService.this.mPackageManager.getPackageInfo(packageName, userId, false);
                    if (pi != null) {
                        OverlayManagerService.this.mPackageManager.cachePackageInfo(packageName, userId, pi);
                        if (OverlayManagerService.this.isOverlayPackage(pi)) {
                            OverlayManagerService.this.mImpl.onOverlayPackageUpgraded(packageName, userId);
                        } else {
                            OverlayManagerService.this.mImpl.onTargetPackageUpgraded(packageName, userId);
                        }
                    }
                }
            }
        }

        private void onPackageRemoved(String packageName, int[] userIds) {
            for (int userId : userIds) {
                synchronized (OverlayManagerService.this.mLock) {
                    OverlayManagerService.this.mPackageManager.forgetPackageInfo(packageName, userId);
                    if (OverlayManagerService.this.mImpl.getOverlayInfo(packageName, userId) == null) {
                        OverlayManagerService.this.mImpl.onTargetPackageRemoved(packageName, userId);
                    } else {
                        OverlayManagerService.this.mImpl.onOverlayPackageRemoved(packageName, userId);
                    }
                }
            }
        }
    }

    private final class UserReceiver extends BroadcastReceiver {
        /* synthetic */ UserReceiver(OverlayManagerService this$0, UserReceiver -this1) {
            this();
        }

        private UserReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            String action = intent.getAction();
            if (action.equals("android.intent.action.USER_ADDED")) {
                if (userId != -10000) {
                    ArrayList<String> targets;
                    synchronized (OverlayManagerService.this.mLock) {
                        targets = OverlayManagerService.this.mImpl.updateOverlaysForUser(userId);
                    }
                    OverlayManagerService.this.updateOverlayPaths(userId, targets);
                }
            } else if (action.equals("android.intent.action.USER_REMOVED") && userId != -10000) {
                synchronized (OverlayManagerService.this.mLock) {
                    OverlayManagerService.this.mImpl.onUserRemoved(userId);
                    OverlayManagerService.this.mPackageManager.forgetAllPackageInfos(userId);
                }
            }
        }
    }

    public OverlayManagerService(Context context, Installer installer) {
        super(context);
        IdmapManager im = new IdmapManager(installer);
        this.mSettings = new OverlayManagerSettings();
        this.mImpl = new OverlayManagerServiceImpl(this.mPackageManager, im, this.mSettings, getDefaultOverlayPackages(), new OverlayChangeListener(this, null));
        this.mInitCompleteSignal = SystemServerInitThreadPool.get().submit(new -$Lambda$Whs3NIaASrs6bpQxTTs9leTDPyo(this), "Init OverlayManagerService");
    }

    /* synthetic */ void lambda$-com_android_server_om_OverlayManagerService_10254() {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        getContext().registerReceiverAsUser(new PackageReceiver(this, null), UserHandle.ALL, packageFilter, null, null);
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_ADDED");
        userFilter.addAction("android.intent.action.USER_REMOVED");
        getContext().registerReceiverAsUser(new UserReceiver(this, null), UserHandle.ALL, userFilter, null, null);
        restoreSettings();
        initIfNeeded();
        onSwitchUser(0);
        publishBinderService("overlay", this.mService);
        publishLocalService(OverlayManagerService.class, this);
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            ConcurrentUtils.waitForFutureNoInterrupt(this.mInitCompleteSignal, "Wait for OverlayManagerService init");
            this.mInitCompleteSignal = null;
        }
    }

    private void initIfNeeded() {
        List<UserInfo> users = ((UserManager) getContext().getSystemService(UserManager.class)).getUsers(true);
        synchronized (this.mLock) {
            int userCount = users.size();
            for (int i = 0; i < userCount; i++) {
                UserInfo userInfo = (UserInfo) users.get(i);
                if (!(userInfo.supportsSwitchTo() || userInfo.id == 0)) {
                    updateOverlayPaths(((UserInfo) users.get(i)).id, this.mImpl.updateOverlaysForUser(((UserInfo) users.get(i)).id));
                }
            }
        }
    }

    public void onSwitchUser(int newUserId) {
        synchronized (this.mLock) {
            updatePrimaryUserPackagesEnable(newUserId);
            updateAssets(newUserId, this.mImpl.updateOverlaysForUser(newUserId));
        }
        schedulePersistSettings();
    }

    private void updatePrimaryUserPackagesEnable(int newUserId) {
        List<OverlayInfo> fwkOverlayInfos = this.mImpl.getOverlayInfosForTarget("androidhwext", newUserId);
        int overlaysSize = fwkOverlayInfos == null ? 0 : fwkOverlayInfos.size();
        for (int i = 0; i < overlaysSize; i++) {
            String packageName = ((OverlayInfo) fwkOverlayInfos.get(i)).packageName;
            if ("com.android.frameworkhwext.dark".equals(packageName)) {
                OverlayInfo overlayInfo = this.mImpl.getOverlayInfo(packageName, 0);
                if (overlayInfo != null) {
                    String newStatus = SystemProperties.get("persist.deep.theme_" + newUserId, "");
                    if (newUserId != 0) {
                        if (TextUtils.isEmpty(newStatus) && overlayInfo.isEnabled() && (this.mImpl.setEnabled(packageName, false, 0) ^ 1) != 0) {
                            Slog.w(TAG, String.format("Failed to set false for %s user %d", new Object[]{packageName, Integer.valueOf(0)}));
                        }
                        if (!(!"dark".equals(newStatus) || (overlayInfo.isEnabled() ^ 1) == 0 || (this.mImpl.setEnabled(packageName, true, 0) ^ 1) == 0)) {
                            Slog.w(TAG, String.format("Failed to set true for %s user %d", new Object[]{packageName, Integer.valueOf(0)}));
                        }
                    }
                    if (newUserId == 0) {
                        boolean isPrimaryDark = "dark".equals(SystemProperties.get("persist.deep.theme_0", ""));
                        if (!(isPrimaryDark == overlayInfo.isEnabled() || (this.mImpl.setEnabled(packageName, isPrimaryDark, 0) ^ 1) == 0)) {
                            Slog.w(TAG, String.format("Failed to restore " + String.valueOf(isPrimaryDark) + " for %s user %d", new Object[]{packageName, Integer.valueOf(0)}));
                        }
                    }
                }
            }
        }
    }

    private static Set<String> getDefaultOverlayPackages() {
        String str = SystemProperties.get(DEFAULT_OVERLAYS_PROP);
        if (TextUtils.isEmpty(str)) {
            return Collections.emptySet();
        }
        ArraySet<String> defaultPackages = new ArraySet();
        for (String packageName : str.split(";")) {
            if (!TextUtils.isEmpty(packageName)) {
                defaultPackages.add(packageName);
            }
        }
        return defaultPackages;
    }

    private boolean isOverlayPackage(PackageInfo pi) {
        if (pi == null || pi.overlayTarget == null || (pi.overlayFlags & 4) == 0) {
            return false;
        }
        return true;
    }

    private void updateOverlayPaths(int userId, List<String> targetPackageNames) {
        int N;
        int i;
        String targetPackageName;
        PackageManagerInternal pm = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        boolean updateFrameworkRes = targetPackageNames.contains("android");
        boolean updateFrameworkReshwext = targetPackageNames.contains("androidhwext");
        if (updateFrameworkRes || updateFrameworkReshwext) {
            targetPackageNames = pm.getTargetPackageNames(userId);
        }
        Map<String, List<String>> pendingChanges = new ArrayMap(targetPackageNames.size());
        synchronized (this.mLock) {
            List<String> frameworkOverlays = this.mImpl.getEnabledOverlayPackageNames("android", userId);
            List<String> frameworkhwextOverlays = this.mImpl.getEnabledOverlayPackageNames("androidhwext", userId);
            N = targetPackageNames.size();
            for (i = 0; i < N; i++) {
                targetPackageName = (String) targetPackageNames.get(i);
                List<String> list = new ArrayList();
                if (!"android".equals(targetPackageName)) {
                    list.addAll(frameworkOverlays);
                }
                if (!"androidhwext".equals(targetPackageName)) {
                    list.addAll(frameworkhwextOverlays);
                }
                list.addAll(this.mImpl.getEnabledOverlayPackageNames(targetPackageName, userId));
                pendingChanges.put(targetPackageName, list);
            }
        }
        N = targetPackageNames.size();
        for (i = 0; i < N; i++) {
            targetPackageName = (String) targetPackageNames.get(i);
            if (!pm.setEnabledOverlayPackages(userId, targetPackageName, (List) pendingChanges.get(targetPackageName))) {
                Slog.e(TAG, String.format("Failed to change enabled overlays for %s user %d", new Object[]{targetPackageName, Integer.valueOf(userId)}));
            }
        }
    }

    private void updateAssets(int userId, String targetPackageName) {
        updateAssets(userId, Collections.singletonList(targetPackageName));
    }

    private void updateAssets(int userId, List<String> targetPackageNames) {
        updateOverlayPaths(userId, targetPackageNames);
        try {
            ActivityManager.getService().scheduleApplicationInfoChanged(targetPackageNames, userId);
        } catch (RemoteException e) {
        }
    }

    private void updateApplicationAssets(int userId, String packageName) {
        PackageManagerInternal pm = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        List<String> list = new ArrayList();
        synchronized (this.mLock) {
            List<String> frameworkOverlays = this.mImpl.getEnabledOverlayPackageNames("android", userId);
            List<String> frameworkhwextOverlays = this.mImpl.getEnabledOverlayPackageNames("androidhwext", userId);
            list.addAll(frameworkOverlays);
            list.addAll(frameworkhwextOverlays);
            list.addAll(this.mImpl.getEnabledOverlayPackageNames(packageName, userId));
        }
        if (!pm.setEnabledOverlayPackages(userId, packageName, list)) {
            Slog.e(TAG, String.format("Failed to change enabled overlays for %s user %d", new Object[]{packageName, Integer.valueOf(userId)}));
        }
    }

    private void schedulePersistSettings() {
        if (!this.mPersistSettingsScheduled.getAndSet(true)) {
            IoThread.getHandler().post(new com.android.server.om.-$Lambda$Whs3NIaASrs6bpQxTTs9leTDPyo.AnonymousClass1(this));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x001c A:{Splitter: B:3:0x000a, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception), PHI: r1 } */
    /* JADX WARNING: Missing block: B:7:0x001c, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            r5.mSettingsFile.failWrite(r1);
            android.util.Slog.e(TAG, "failed to persist overlay state", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    /* synthetic */ void lambda$-com_android_server_om_OverlayManagerService_36834() {
        this.mPersistSettingsScheduled.set(false);
        synchronized (this.mLock) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = this.mSettingsFile.startWrite();
                this.mSettings.persist(fileOutputStream);
                this.mSettingsFile.finishWrite(fileOutputStream);
            } catch (Exception e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x006b A:{Splitter: B:23:0x006a, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:25:0x006b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            android.util.Slog.e(TAG, "failed to restore overlay state", r0);
     */
    /* JADX WARNING: Missing block: B:29:0x0076, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void restoreSettings() {
        FileInputStream fileInputStream;
        Throwable th;
        Throwable th2 = null;
        synchronized (this.mLock) {
            if (this.mSettingsFile.getBaseFile().exists()) {
                fileInputStream = null;
                try {
                    fileInputStream = this.mSettingsFile.openRead();
                    this.mSettings.restore(fileInputStream);
                    List<UserInfo> liveUsers = this.mUserManager.getUsers(true);
                    int[] liveUserIds = new int[liveUsers.size()];
                    for (int i = 0; i < liveUsers.size(); i++) {
                        liveUserIds[i] = ((UserInfo) liveUsers.get(i)).getUserHandle().getIdentifier();
                    }
                    Arrays.sort(liveUserIds);
                    for (int userId : this.mSettings.getUsers()) {
                        if (Arrays.binarySearch(liveUserIds, userId) < 0) {
                            this.mSettings.removeUser(userId);
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (Exception e) {
                        }
                    }
                } catch (Throwable th22) {
                    Throwable th4 = th22;
                    th22 = th;
                    th = th4;
                }
            } else {
                return;
            }
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        } else {
            throw th;
        }
    }
}
