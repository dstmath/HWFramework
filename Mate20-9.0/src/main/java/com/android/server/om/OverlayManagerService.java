package com.android.server.om;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
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
import com.android.server.om.OverlayManagerService;
import com.android.server.om.OverlayManagerServiceImpl;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserManagerService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParserException;

public final class OverlayManagerService extends SystemService {
    static final boolean DEBUG = false;
    private static final String DEFAULT_OVERLAYS_PROP = "ro.boot.vendor.overlay.theme";
    private static final String FWK_DARK_TAG = "com.android.frameworkhwext.dark";
    static final String TAG = "OverlayManager";
    /* access modifiers changed from: private */
    public final OverlayManagerServiceImpl mImpl;
    private Future<?> mInitCompleteSignal;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public final PackageManagerHelper mPackageManager = new PackageManagerHelper();
    private final AtomicBoolean mPersistSettingsScheduled = new AtomicBoolean(false);
    private final IBinder mService = new IOverlayManager.Stub() {
        public Map<String, List<OverlayInfo>> getAllOverlays(int userId) throws RemoteException {
            Map<String, List<OverlayInfo>> overlaysForUser;
            int userId2 = handleIncomingUser(userId, "getAllOverlays");
            synchronized (OverlayManagerService.this.mLock) {
                overlaysForUser = OverlayManagerService.this.mImpl.getOverlaysForUser(userId2);
            }
            return overlaysForUser;
        }

        public List<OverlayInfo> getOverlayInfosForTarget(String targetPackageName, int userId) throws RemoteException {
            List<OverlayInfo> overlayInfosForTarget;
            int userId2 = handleIncomingUser(userId, "getOverlayInfosForTarget");
            if (targetPackageName == null) {
                return Collections.emptyList();
            }
            synchronized (OverlayManagerService.this.mLock) {
                overlayInfosForTarget = OverlayManagerService.this.mImpl.getOverlayInfosForTarget(targetPackageName, userId2);
            }
            return overlayInfosForTarget;
        }

        public OverlayInfo getOverlayInfo(String packageName, int userId) throws RemoteException {
            OverlayInfo overlayInfo;
            int userId2 = handleIncomingUser(userId, "getOverlayInfo");
            if (packageName == null) {
                return null;
            }
            synchronized (OverlayManagerService.this.mLock) {
                overlayInfo = OverlayManagerService.this.mImpl.getOverlayInfo(packageName, userId2);
            }
            return overlayInfo;
        }

        public boolean setEnabled(String packageName, boolean enable, int userId) throws RemoteException {
            boolean enabled;
            enforceChangeOverlayPackagesPermission("setEnabled");
            int userId2 = handleIncomingUser(userId, "setEnabled");
            if (packageName == null) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (OverlayManagerService.this.mLock) {
                    enabled = OverlayManagerService.this.mImpl.setEnabled(packageName, enable, userId2);
                }
                Binder.restoreCallingIdentity(ident);
                return enabled;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        public boolean setEnabledExclusive(String packageName, boolean enable, int userId) throws RemoteException {
            boolean enabledExclusive;
            enforceChangeOverlayPackagesPermission("setEnabled");
            int userId2 = handleIncomingUser(userId, "setEnabled");
            if (packageName == null || !enable) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (OverlayManagerService.this.mLock) {
                    enabledExclusive = OverlayManagerService.this.mImpl.setEnabledExclusive(packageName, false, userId2);
                }
                Binder.restoreCallingIdentity(ident);
                return enabledExclusive;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        public boolean setEnabledExclusiveInCategory(String packageName, int userId) throws RemoteException {
            boolean enabledExclusive;
            enforceChangeOverlayPackagesPermission("setEnabled");
            int userId2 = handleIncomingUser(userId, "setEnabled");
            if (packageName == null) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (OverlayManagerService.this.mLock) {
                    enabledExclusive = OverlayManagerService.this.mImpl.setEnabledExclusive(packageName, true, userId2);
                }
                Binder.restoreCallingIdentity(ident);
                return enabledExclusive;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        public boolean setPriority(String packageName, String parentPackageName, int userId) throws RemoteException {
            boolean priority;
            enforceChangeOverlayPackagesPermission("setPriority");
            int userId2 = handleIncomingUser(userId, "setPriority");
            if (packageName == null || parentPackageName == null) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (OverlayManagerService.this.mLock) {
                    priority = OverlayManagerService.this.mImpl.setPriority(packageName, parentPackageName, userId2);
                }
                Binder.restoreCallingIdentity(ident);
                return priority;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        public boolean setHighestPriority(String packageName, int userId) throws RemoteException {
            boolean highestPriority;
            enforceChangeOverlayPackagesPermission("setHighestPriority");
            int userId2 = handleIncomingUser(userId, "setHighestPriority");
            if (packageName == null) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (OverlayManagerService.this.mLock) {
                    highestPriority = OverlayManagerService.this.mImpl.setHighestPriority(packageName, userId2);
                }
                Binder.restoreCallingIdentity(ident);
                return highestPriority;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        public boolean setLowestPriority(String packageName, int userId) throws RemoteException {
            boolean lowestPriority;
            enforceChangeOverlayPackagesPermission("setLowestPriority");
            int userId2 = handleIncomingUser(userId, "setLowestPriority");
            if (packageName == null) {
                return false;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (OverlayManagerService.this.mLock) {
                    lowestPriority = OverlayManagerService.this.mImpl.setLowestPriority(packageName, userId2);
                }
                Binder.restoreCallingIdentity(ident);
                return lowestPriority;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new OverlayManagerShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] argv) {
            enforceDumpPermission("dump");
            boolean z = false;
            if (argv.length > 0 && "--verbose".equals(argv[0])) {
                z = true;
            }
            boolean verbose = z;
            synchronized (OverlayManagerService.this.mLock) {
                OverlayManagerService.this.mImpl.onDump(pw);
                OverlayManagerService.this.mPackageManager.dump(pw, verbose);
            }
        }

        private int handleIncomingUser(int userId, String message) {
            return ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, message, null);
        }

        private void enforceChangeOverlayPackagesPermission(String message) {
            OverlayManagerService.this.getContext().enforceCallingPermission("android.permission.CHANGE_OVERLAY_PACKAGES", message);
        }

        private void enforceDumpPermission(String message) {
            OverlayManagerService.this.getContext().enforceCallingPermission("android.permission.DUMP", message);
        }
    };
    private final OverlayManagerSettings mSettings;
    private final AtomicFile mSettingsFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), "overlays.xml"), "overlays");
    /* access modifiers changed from: private */
    public final UserManagerService mUserManager = UserManagerService.getInstance();

    private final class OverlayChangeListener implements OverlayManagerServiceImpl.OverlayChangeListener {
        private OverlayChangeListener() {
        }

        public void onOverlaysChanged(String targetPackageName, int userId) {
            OverlayManagerService.this.schedulePersistSettings();
            FgThread.getHandler().post(new Runnable(userId, targetPackageName) {
                private final /* synthetic */ int f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    OverlayManagerService.OverlayChangeListener.lambda$onOverlaysChanged$0(OverlayManagerService.OverlayChangeListener.this, this.f$1, this.f$2);
                }
            });
        }

        public static /* synthetic */ void lambda$onOverlaysChanged$0(OverlayChangeListener overlayChangeListener, int userId, String targetPackageName) {
            String str = targetPackageName;
            OverlayManagerService.this.updateAssets(userId, str);
            Intent intent = new Intent("android.intent.action.OVERLAY_CHANGED", Uri.fromParts("package", str, null));
            intent.setFlags(67108864);
            try {
                Intent intent2 = intent;
                try {
                    ActivityManager.getService().broadcastIntent(null, intent, null, null, 0, null, null, null, -1, null, false, false, userId);
                } catch (RemoteException e) {
                }
            } catch (RemoteException e2) {
                Intent intent3 = intent;
            }
        }
    }

    private static final class PackageManagerHelper implements OverlayManagerServiceImpl.PackageManagerHelper {
        private static final String TAB1 = "    ";
        private static final String TAB2 = "        ";
        private final SparseArray<HashMap<String, PackageInfo>> mCache = new SparseArray<>();
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
            HashMap<String, PackageInfo> map = this.mCache.get(userId);
            if (map == null) {
                return null;
            }
            return map.get(packageName);
        }

        public void cachePackageInfo(String packageName, int userId, PackageInfo pi) {
            HashMap<String, PackageInfo> map = this.mCache.get(userId);
            if (map == null) {
                map = new HashMap<>();
                this.mCache.put(userId, map);
            }
            map.put(packageName, pi);
        }

        public void forgetPackageInfo(String packageName, int userId) {
            HashMap<String, PackageInfo> map = this.mCache.get(userId);
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
            int i = 0;
            if (!verbose) {
                int count = 0;
                int N = this.mCache.size();
                while (i < N) {
                    count += this.mCache.get(this.mCache.keyAt(i)).size();
                    i++;
                }
                pw.println(TAB1 + count + " package(s)");
            } else if (this.mCache.size() == 0) {
                pw.println("    <empty>");
            } else {
                int N2 = this.mCache.size();
                while (i < N2) {
                    int userId = this.mCache.keyAt(i);
                    pw.println("    User " + userId);
                    for (Map.Entry<String, PackageInfo> entry : this.mCache.get(userId).entrySet()) {
                        pw.println(TAB2 + entry.getKey() + ": " + entry.getValue());
                    }
                    i++;
                }
            }
        }
    }

    private final class PackageReceiver extends BroadcastReceiver {
        private PackageReceiver() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0056, code lost:
            if (r7.equals("android.intent.action.PACKAGE_ADDED") == false) goto L_0x006d;
         */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0072  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x007c  */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0080  */
        public void onReceive(Context context, Intent intent) {
            Uri data = intent.getData();
            if (data == null) {
                Slog.e(OverlayManagerService.TAG, "Cannot handle package broadcast with null data");
                return;
            }
            String packageName = data.getSchemeSpecificPart();
            char c = 0;
            boolean replacing = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
            int extraUid = intent.getIntExtra("android.intent.extra.UID", -10000);
            int[] userIds = extraUid == -10000 ? OverlayManagerService.this.mUserManager.getUserIds() : new int[]{UserHandle.getUserId(extraUid)};
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode != 172491798) {
                if (hashCode != 525384130) {
                    if (hashCode == 1544582882) {
                    }
                } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    c = 2;
                    switch (c) {
                        case 0:
                            if (!replacing) {
                                onPackageAdded(packageName, userIds);
                                break;
                            } else {
                                onPackageUpgraded(packageName, userIds);
                                break;
                            }
                        case 1:
                            onPackageChanged(packageName, userIds);
                            break;
                        case 2:
                            if (!replacing) {
                                onPackageRemoved(packageName, userIds);
                                break;
                            } else {
                                onPackageUpgrading(packageName, userIds);
                                break;
                            }
                    }
                }
            } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        }

        private void onPackageAdded(String packageName, int[] userIds) {
            for (int userId : userIds) {
                synchronized (OverlayManagerService.this.mLock) {
                    PackageInfo pi = OverlayManagerService.this.mPackageManager.getPackageInfo(packageName, userId, false);
                    if (pi != null) {
                        OverlayManagerService.this.mPackageManager.cachePackageInfo(packageName, userId, pi);
                        if (pi.isOverlayPackage()) {
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
                        if (pi.isOverlayPackage()) {
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
                    if (OverlayManagerService.this.mImpl.getOverlayInfo(packageName, userId) != null) {
                        OverlayManagerService.this.mImpl.onOverlayPackageUpgrading(packageName, userId);
                    } else {
                        OverlayManagerService.this.mImpl.onTargetPackageUpgrading(packageName, userId);
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
                        if (pi.isOverlayPackage()) {
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
                    if (OverlayManagerService.this.mImpl.getOverlayInfo(packageName, userId) != null) {
                        OverlayManagerService.this.mImpl.onOverlayPackageRemoved(packageName, userId);
                    } else {
                        OverlayManagerService.this.mImpl.onTargetPackageRemoved(packageName, userId);
                    }
                }
            }
        }
    }

    private final class UserReceiver extends BroadcastReceiver {
        private UserReceiver() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:12:0x0034  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x0054  */
        /* JADX WARNING: Removed duplicated region for block: B:40:? A[RETURN, SYNTHETIC] */
        public void onReceive(Context context, Intent intent) {
            char c;
            ArrayList<String> targets;
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode == -2061058799) {
                if (action.equals("android.intent.action.USER_REMOVED")) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                    }
                }
            } else if (hashCode == 1121780209 && action.equals("android.intent.action.USER_ADDED")) {
                c = 0;
                switch (c) {
                    case 0:
                        if (userId != -10000) {
                            synchronized (OverlayManagerService.this.mLock) {
                                targets = OverlayManagerService.this.mImpl.updateOverlaysForUser(userId);
                            }
                            OverlayManagerService.this.updateOverlayPaths(userId, targets);
                            return;
                        }
                        return;
                    case 1:
                        if (userId != -10000) {
                            synchronized (OverlayManagerService.this.mLock) {
                                OverlayManagerService.this.mImpl.onUserRemoved(userId);
                                OverlayManagerService.this.mPackageManager.forgetAllPackageInfos(userId);
                            }
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
        }
    }

    /* JADX WARNING: type inference failed for: r0v2, types: [com.android.server.om.OverlayManagerService$1, android.os.IBinder] */
    public OverlayManagerService(Context context, Installer installer) {
        super(context);
        IdmapManager im = new IdmapManager(installer);
        this.mSettings = new OverlayManagerSettings();
        OverlayManagerServiceImpl overlayManagerServiceImpl = new OverlayManagerServiceImpl(this.mPackageManager, im, this.mSettings, getDefaultOverlayPackages(), new OverlayChangeListener());
        this.mImpl = overlayManagerServiceImpl;
        this.mInitCompleteSignal = SystemServerInitThreadPool.get().submit(new Runnable() {
            public final void run() {
                OverlayManagerService.lambda$new$0(OverlayManagerService.this);
            }
        }, "Init OverlayManagerService");
    }

    public static /* synthetic */ void lambda$new$0(OverlayManagerService overlayManagerService) {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        overlayManagerService.getContext().registerReceiverAsUser(new PackageReceiver(), UserHandle.ALL, packageFilter, null, null);
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_ADDED");
        userFilter.addAction("android.intent.action.USER_REMOVED");
        overlayManagerService.getContext().registerReceiverAsUser(new UserReceiver(), UserHandle.ALL, userFilter, null, null);
        overlayManagerService.restoreSettings();
        overlayManagerService.initIfNeeded();
        overlayManagerService.onSwitchUser(0);
        overlayManagerService.publishBinderService("overlay", overlayManagerService.mService);
        overlayManagerService.publishLocalService(OverlayManagerService.class, overlayManagerService);
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
                UserInfo userInfo = users.get(i);
                if (!userInfo.supportsSwitchTo() && userInfo.id != 0) {
                    updateOverlayPaths(users.get(i).id, this.mImpl.updateOverlaysForUser(users.get(i).id));
                }
            }
        }
    }

    public void onSwitchUser(int newUserId) {
        synchronized (this.mLock) {
            updatePrimaryUserPackagesEnable(newUserId);
            List<String> targets = this.mImpl.updateOverlaysForUser(newUserId);
            if (targets != null && (targets.contains(PackageManagerService.PLATFORM_PACKAGE_NAME) || targets.contains("androidhwext"))) {
                Slog.i(TAG, "targets contains android or androidhwext");
            }
            updateAssets(newUserId, targets);
        }
        schedulePersistSettings();
    }

    private void updatePrimaryUserPackagesEnable(int newUserId) {
        List<OverlayInfo> fwkOverlayInfos = this.mImpl.getOverlayInfosForTarget("androidhwext", newUserId);
        int overlaysSize = fwkOverlayInfos == null ? 0 : fwkOverlayInfos.size();
        for (int i = 0; i < overlaysSize; i++) {
            String packageName = fwkOverlayInfos.get(i).packageName;
            if ("com.android.frameworkhwext.dark".equals(packageName)) {
                OverlayInfo overlayInfo = this.mImpl.getOverlayInfo(packageName, 0);
                if (overlayInfo != null) {
                    String newStatus = SystemProperties.get("persist.deep.theme_" + newUserId, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    if (newUserId != 0) {
                        if (TextUtils.isEmpty(newStatus) && overlayInfo.isEnabled() && !this.mImpl.setEnabled(packageName, false, 0)) {
                            Slog.w(TAG, String.format("Failed to set false for %s user %d", new Object[]{packageName, 0}));
                        }
                        if ("dark".equals(newStatus) && !overlayInfo.isEnabled() && !this.mImpl.setEnabled(packageName, true, 0)) {
                            Slog.w(TAG, String.format("Failed to set true for %s user %d", new Object[]{packageName, 0}));
                        }
                    }
                    if (newUserId == 0) {
                        boolean isPrimaryDark = "dark".equals(SystemProperties.get("persist.deep.theme_0", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
                        if (isPrimaryDark != overlayInfo.isEnabled() && !this.mImpl.setEnabled(packageName, isPrimaryDark, 0)) {
                            Slog.w(TAG, String.format("Failed to restore " + String.valueOf(isPrimaryDark) + " for %s user %d", new Object[]{packageName, 0}));
                        }
                    }
                }
            }
        }
    }

    private static String[] getDefaultOverlayPackages() {
        String str = SystemProperties.get(DEFAULT_OVERLAYS_PROP);
        if (TextUtils.isEmpty(str)) {
            return EmptyArray.STRING;
        }
        ArraySet<String> defaultPackages = new ArraySet<>();
        for (String packageName : str.split(";")) {
            if (!TextUtils.isEmpty(packageName)) {
                defaultPackages.add(packageName);
            }
        }
        return (String[]) defaultPackages.toArray(new String[defaultPackages.size()]);
    }

    /* access modifiers changed from: private */
    public void updateOverlayPaths(int userId, List<String> targetPackageNames) {
        int i = userId;
        List<String> list = targetPackageNames;
        PackageManagerInternal pm = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        boolean updateFrameworkRes = list.contains(PackageManagerService.PLATFORM_PACKAGE_NAME);
        boolean updateFrameworkReshwext = list.contains("androidhwext");
        if (updateFrameworkRes || updateFrameworkReshwext) {
            list = pm.getTargetPackageNames(i);
        }
        List<String> targetPackageNames2 = list;
        Map<String, List<String>> pendingChanges = new ArrayMap<>(targetPackageNames2.size());
        synchronized (this.mLock) {
            List<String> frameworkOverlays = this.mImpl.getEnabledOverlayPackageNames(PackageManagerService.PLATFORM_PACKAGE_NAME, i);
            ArrayList arrayList = new ArrayList(frameworkOverlays.size());
            arrayList.addAll(frameworkOverlays);
            arrayList.remove(OverlayManagerSettings.FWK_DARK_OVERLAY_TAG);
            List<String> frameworkhwextOverlays = this.mImpl.getEnabledOverlayPackageNames("androidhwext", i);
            int N = targetPackageNames2.size();
            int i2 = 0;
            while (i2 < N) {
                String targetPackageName = targetPackageNames2.get(i2);
                boolean isInDataSkinDir = isInDataSkinDir(targetPackageName);
                List<String> list2 = new ArrayList<>();
                List<String> frameworkOverlays2 = frameworkOverlays;
                if (!PackageManagerService.PLATFORM_PACKAGE_NAME.equals(targetPackageName)) {
                    list2.addAll(isInDataSkinDir ? arrayList : frameworkOverlays2);
                }
                if (!"androidhwext".equals(targetPackageName) && !isInDataSkinDir) {
                    list2.addAll(frameworkhwextOverlays);
                }
                list2.addAll(this.mImpl.getEnabledOverlayPackageNames(targetPackageName, i));
                pendingChanges.put(targetPackageName, list2);
                i2++;
                frameworkOverlays = frameworkOverlays2;
            }
        }
        int N2 = targetPackageNames2.size();
        for (int i3 = 0; i3 < N2; i3++) {
            String targetPackageName2 = targetPackageNames2.get(i3);
            if (!pm.setEnabledOverlayPackages(i, targetPackageName2, pendingChanges.get(targetPackageName2))) {
                Slog.e(TAG, String.format("Failed to change enabled overlays for %s user %d", new Object[]{targetPackageName2, Integer.valueOf(userId)}));
            }
        }
    }

    private boolean isInDataSkinDir(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        File root = new File(Environment.getDataDirectory() + "/themes/" + UserHandle.myUserId());
        if (!root.exists()) {
            return false;
        }
        File[] files = root.listFiles();
        int size = files == null ? 0 : files.length;
        for (int i = 0; i < size; i++) {
            File file = files[i];
            if (file.isFile() && packageName.equals(file.getName())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void updateAssets(int userId, String targetPackageName) {
        updateAssets(userId, (List<String>) Collections.singletonList(targetPackageName));
    }

    private void updateAssets(int userId, List<String> targetPackageNames) {
        updateOverlayPaths(userId, targetPackageNames);
        try {
            ActivityManager.getService().scheduleApplicationInfoChanged(targetPackageNames, userId);
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: private */
    public void schedulePersistSettings() {
        if (!this.mPersistSettingsScheduled.getAndSet(true)) {
            IoThread.getHandler().post(new Runnable() {
                public final void run() {
                    OverlayManagerService.lambda$schedulePersistSettings$1(OverlayManagerService.this);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$schedulePersistSettings$1(OverlayManagerService overlayManagerService) {
        overlayManagerService.mPersistSettingsScheduled.set(false);
        synchronized (overlayManagerService.mLock) {
            FileOutputStream stream = null;
            try {
                stream = overlayManagerService.mSettingsFile.startWrite();
                overlayManagerService.mSettings.persist(stream);
                overlayManagerService.mSettingsFile.finishWrite(stream);
            } catch (IOException | XmlPullParserException e) {
                overlayManagerService.mSettingsFile.failWrite(stream);
                Slog.e(TAG, "failed to persist overlay state", e);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0073, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x007c, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        android.util.Slog.e(TAG, "failed to restore overlay state", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0085, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007c A[ExcHandler: IOException | XmlPullParserException (r1v4 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:7:0x0011] */
    private void restoreSettings() {
        FileInputStream stream;
        synchronized (this.mLock) {
            if (this.mSettingsFile.getBaseFile().exists()) {
                try {
                    stream = this.mSettingsFile.openRead();
                    this.mSettings.restore(stream);
                    List<UserInfo> liveUsers = this.mUserManager.getUsers(true);
                    int[] liveUserIds = new int[liveUsers.size()];
                    for (int i = 0; i < liveUsers.size(); i++) {
                        liveUserIds[i] = liveUsers.get(i).getUserHandle().getIdentifier();
                    }
                    Arrays.sort(liveUserIds);
                    for (int userId : this.mSettings.getUsers()) {
                        if (Arrays.binarySearch(liveUserIds, userId) < 0) {
                            this.mSettings.removeUser(userId);
                        }
                    }
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException | XmlPullParserException e) {
                } catch (Throwable th) {
                    if (stream != null) {
                        if (r2 != null) {
                            stream.close();
                        } else {
                            stream.close();
                        }
                    }
                    throw th;
                }
            }
        }
    }
}
