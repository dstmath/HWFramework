package com.android.server.om;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.IApplicationThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IIntentReceiver;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.FgThread;
import com.android.server.IoThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.om.OverlayManagerService;
import com.android.server.om.OverlayManagerServiceImpl;
import com.android.server.pm.DumpState;
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
import java.util.concurrent.atomic.AtomicBoolean;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParserException;

public final class OverlayManagerService extends SystemService {
    static final boolean DEBUG = false;
    private static final String DEFAULT_OVERLAYS_PROP = "ro.boot.vendor.overlay.theme";
    static final String TAG = "OverlayManager";
    private final OverlayManagerServiceImpl mImpl;
    private final Object mLock = new Object();
    private final PackageManagerHelper mPackageManager;
    private final AtomicBoolean mPersistSettingsScheduled = new AtomicBoolean(false);
    private final IBinder mService = new IOverlayManager.Stub() {
        /* class com.android.server.om.OverlayManagerService.AnonymousClass1 */

        public Map<String, List<OverlayInfo>> getAllOverlays(int userId) throws RemoteException {
            Map<String, List<OverlayInfo>> overlaysForUser;
            try {
                Trace.traceBegin(67108864, "OMS#getAllOverlays " + userId);
                int userId2 = handleIncomingUser(userId, "getAllOverlays");
                synchronized (OverlayManagerService.this.mLock) {
                    overlaysForUser = OverlayManagerService.this.mImpl.getOverlaysForUser(userId2);
                }
                return overlaysForUser;
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        public List<OverlayInfo> getOverlayInfosForTarget(String targetPackageName, int userId) throws RemoteException {
            List<OverlayInfo> overlayInfosForTarget;
            try {
                Trace.traceBegin(67108864, "OMS#getOverlayInfosForTarget " + targetPackageName);
                int userId2 = handleIncomingUser(userId, "getOverlayInfosForTarget");
                if (targetPackageName == null) {
                    return Collections.emptyList();
                }
                synchronized (OverlayManagerService.this.mLock) {
                    overlayInfosForTarget = OverlayManagerService.this.mImpl.getOverlayInfosForTarget(targetPackageName, userId2);
                }
                Trace.traceEnd(67108864);
                return overlayInfosForTarget;
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        public OverlayInfo getOverlayInfo(String packageName, int userId) throws RemoteException {
            OverlayInfo overlayInfo;
            try {
                Trace.traceBegin(67108864, "OMS#getOverlayInfo " + packageName);
                int userId2 = handleIncomingUser(userId, "getOverlayInfo");
                if (packageName == null) {
                    return null;
                }
                synchronized (OverlayManagerService.this.mLock) {
                    overlayInfo = OverlayManagerService.this.mImpl.getOverlayInfo(packageName, userId2);
                }
                Trace.traceEnd(67108864);
                return overlayInfo;
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        /* JADX INFO: finally extract failed */
        public boolean setEnabled(String packageName, boolean enable, int userId) throws RemoteException {
            boolean enabled;
            try {
                Trace.traceBegin(67108864, "OMS#setEnabled " + packageName + " " + enable);
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
                    Trace.traceEnd(67108864);
                    return enabled;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        /* JADX INFO: finally extract failed */
        public boolean setEnabledExclusive(String packageName, boolean enable, int userId) throws RemoteException {
            boolean enabledExclusive;
            try {
                Trace.traceBegin(67108864, "OMS#setEnabledExclusive " + packageName + " " + enable);
                enforceChangeOverlayPackagesPermission("setEnabledExclusive");
                int userId2 = handleIncomingUser(userId, "setEnabledExclusive");
                if (packageName != null) {
                    if (enable) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            synchronized (OverlayManagerService.this.mLock) {
                                enabledExclusive = OverlayManagerService.this.mImpl.setEnabledExclusive(packageName, false, userId2);
                            }
                            Binder.restoreCallingIdentity(ident);
                            Trace.traceEnd(67108864);
                            return enabledExclusive;
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                    }
                }
                return false;
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        /* JADX INFO: finally extract failed */
        public boolean setEnabledExclusiveInCategory(String packageName, int userId) throws RemoteException {
            boolean enabledExclusive;
            try {
                Trace.traceBegin(67108864, "OMS#setEnabledExclusiveInCategory " + packageName);
                enforceChangeOverlayPackagesPermission("setEnabledExclusiveInCategory");
                int userId2 = handleIncomingUser(userId, "setEnabledExclusiveInCategory");
                if (packageName == null) {
                    return false;
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (OverlayManagerService.this.mLock) {
                        enabledExclusive = OverlayManagerService.this.mImpl.setEnabledExclusive(packageName, true, userId2);
                    }
                    Binder.restoreCallingIdentity(ident);
                    Trace.traceEnd(67108864);
                    return enabledExclusive;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        /* JADX INFO: finally extract failed */
        public boolean setPriority(String packageName, String parentPackageName, int userId) throws RemoteException {
            boolean priority;
            try {
                Trace.traceBegin(67108864, "OMS#setPriority " + packageName + " " + parentPackageName);
                enforceChangeOverlayPackagesPermission("setPriority");
                int userId2 = handleIncomingUser(userId, "setPriority");
                if (packageName != null) {
                    if (parentPackageName != null) {
                        long ident = Binder.clearCallingIdentity();
                        try {
                            synchronized (OverlayManagerService.this.mLock) {
                                priority = OverlayManagerService.this.mImpl.setPriority(packageName, parentPackageName, userId2);
                            }
                            Binder.restoreCallingIdentity(ident);
                            Trace.traceEnd(67108864);
                            return priority;
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                    }
                }
                return false;
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        /* JADX INFO: finally extract failed */
        public boolean setHighestPriority(String packageName, int userId) throws RemoteException {
            boolean highestPriority;
            try {
                Trace.traceBegin(67108864, "OMS#setHighestPriority " + packageName);
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
                    Trace.traceEnd(67108864);
                    return highestPriority;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        /* JADX INFO: finally extract failed */
        public boolean setLowestPriority(String packageName, int userId) throws RemoteException {
            boolean lowestPriority;
            try {
                Trace.traceBegin(67108864, "OMS#setLowestPriority " + packageName);
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
                    Trace.traceEnd(67108864);
                    return lowestPriority;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        /* JADX INFO: finally extract failed */
        public String[] getDefaultOverlayPackages() throws RemoteException {
            String[] defaultOverlayPackages;
            try {
                Trace.traceBegin(67108864, "OMS#getDefaultOverlayPackages");
                OverlayManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.MODIFY_THEME_OVERLAY", null);
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (OverlayManagerService.this.mLock) {
                        defaultOverlayPackages = OverlayManagerService.this.mImpl.getDefaultOverlayPackages();
                    }
                    Binder.restoreCallingIdentity(ident);
                    return defaultOverlayPackages;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.om.OverlayManagerService$1 */
        /* JADX WARN: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new OverlayManagerShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00d6, code lost:
            if (r2.equals("packagename") != false) goto L_0x0133;
         */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            char c;
            String opt;
            DumpState dumpState = new DumpState();
            dumpState.setUserId(UserHandle.getUserId(Binder.getCallingUid()));
            int opti = 0;
            while (true) {
                c = 0;
                if (opti >= args.length || (opt = args[opti]) == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                    break;
                }
                opti++;
                if ("-h".equals(opt)) {
                    pw.println("dump [-h] [--verbose] [--user USER_ID] [[FIELD] PACKAGE]");
                    pw.println("  Print debugging information about the overlay manager.");
                    pw.println("  With optional parameter PACKAGE, limit output to the specified");
                    pw.println("  package. With optional parameter FIELD, limit output to");
                    pw.println("  the value of that SettingsItem field. Field names are");
                    pw.println("  case insensitive and out.println the m prefix can be omitted,");
                    pw.println("  so the following are equivalent: mState, mstate, State, state.");
                    return;
                } else if ("--user".equals(opt)) {
                    opti++;
                    if (opti >= args.length) {
                        pw.println("Error: user missing argument");
                        return;
                    }
                    try {
                        dumpState.setUserId(Integer.parseInt(args[opti]));
                    } catch (NumberFormatException e) {
                        pw.println("Error: user argument is not a number: " + args[opti]);
                        return;
                    }
                } else if ("--verbose".equals(opt)) {
                    dumpState.setVerbose(true);
                } else {
                    pw.println("Unknown argument: " + opt + "; use -h for help");
                }
            }
            if (opti < args.length) {
                String arg = args[opti];
                opti++;
                switch (arg.hashCode()) {
                    case -1750736508:
                        if (arg.equals("targetoverlayablename")) {
                            c = 3;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1248283232:
                        if (arg.equals("targetpackagename")) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1165461084:
                        if (arg.equals("priority")) {
                            c = '\b';
                            break;
                        }
                        c = 65535;
                        break;
                    case -836029914:
                        if (arg.equals("userid")) {
                            c = 1;
                            break;
                        }
                        c = 65535;
                        break;
                    case 50511102:
                        if (arg.equals("category")) {
                            c = '\t';
                            break;
                        }
                        c = 65535;
                        break;
                    case 109757585:
                        if (arg.equals("state")) {
                            c = 5;
                            break;
                        }
                        c = 65535;
                        break;
                    case 440941271:
                        if (arg.equals("isenabled")) {
                            c = 6;
                            break;
                        }
                        c = 65535;
                        break;
                    case 697685016:
                        if (arg.equals("isstatic")) {
                            c = 7;
                            break;
                        }
                        c = 65535;
                        break;
                    case 909712337:
                        break;
                    case 1693907299:
                        if (arg.equals("basecodepath")) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case '\b':
                    case '\t':
                        dumpState.setField(arg);
                        break;
                    default:
                        dumpState.setPackageName(arg);
                        break;
                }
            }
            if (dumpState.getPackageName() == null && opti < args.length) {
                dumpState.setPackageName(args[opti]);
                int opti2 = opti + 1;
            }
            enforceDumpPermission("dump");
            synchronized (OverlayManagerService.this.mLock) {
                OverlayManagerService.this.mImpl.dump(pw, dumpState);
                if (dumpState.getPackageName() == null) {
                    OverlayManagerService.this.mPackageManager.dump(pw, dumpState);
                }
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
    private final AtomicFile mSettingsFile;
    private final UserManagerService mUserManager;

    /* JADX WARN: Type inference failed for: r0v2, types: [com.android.server.om.OverlayManagerService$1, android.os.IBinder] */
    public OverlayManagerService(Context context, Installer installer) {
        super(context);
        Throwable th;
        try {
            Trace.traceBegin(67108864, "OMS#OverlayManagerService");
            this.mSettingsFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), "overlays.xml"), "overlays");
            this.mPackageManager = new PackageManagerHelper();
            this.mUserManager = UserManagerService.getInstance();
            try {
                IdmapManager im = new IdmapManager(installer, this.mPackageManager);
                this.mSettings = new OverlayManagerSettings();
                this.mImpl = new OverlayManagerServiceImpl(this.mPackageManager, im, this.mSettings, getDefaultOverlayPackages(), new OverlayChangeListener());
                IntentFilter packageFilter = new IntentFilter();
                packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
                packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
                packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
                packageFilter.addDataScheme("package");
                getContext().registerReceiverAsUser(new PackageReceiver(), UserHandle.ALL, packageFilter, null, null);
                IntentFilter userFilter = new IntentFilter();
                userFilter.addAction("android.intent.action.USER_ADDED");
                userFilter.addAction("android.intent.action.USER_REMOVED");
                getContext().registerReceiverAsUser(new UserReceiver(), UserHandle.ALL, userFilter, null, null);
                restoreSettings();
                initIfNeeded();
                onSwitchUser(0);
                publishBinderService("overlay", this.mService);
                publishLocalService(OverlayManagerService.class, this);
                Trace.traceEnd(67108864);
            } catch (Throwable th2) {
                th = th2;
                Trace.traceEnd(67108864);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            Trace.traceEnd(67108864);
            throw th;
        }
    }

    @Override // com.android.server.SystemService
    public void onStart() {
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

    @Override // com.android.server.SystemService
    public void onSwitchUser(int newUserId) {
        try {
            Trace.traceBegin(67108864, "OMS#onSwitchUser " + newUserId);
            synchronized (this.mLock) {
                updateAssets(newUserId, this.mImpl.updateOverlaysForUser(newUserId));
            }
            schedulePersistSettings();
        } finally {
            Trace.traceEnd(67108864);
        }
    }

    private static String[] getDefaultOverlayPackages() {
        String str = SystemProperties.get(DEFAULT_OVERLAYS_PROP);
        if (TextUtils.isEmpty(str)) {
            return EmptyArray.STRING;
        }
        ArraySet<String> defaultPackages = new ArraySet<>();
        String[] split = str.split(";");
        for (String packageName : split) {
            if (!TextUtils.isEmpty(packageName)) {
                defaultPackages.add(packageName);
            }
        }
        return (String[]) defaultPackages.toArray(new String[defaultPackages.size()]);
    }

    private final class PackageReceiver extends BroadcastReceiver {
        private PackageReceiver() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:19:0x005f, code lost:
            if (r0.equals("android.intent.action.PACKAGE_ADDED") == false) goto L_0x0076;
         */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0079  */
        /* JADX WARNING: Removed duplicated region for block: B:34:0x008c  */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                Slog.e(OverlayManagerService.TAG, "Cannot handle package broadcast with null action");
                return;
            }
            Uri data = intent.getData();
            if (data == null) {
                Slog.e(OverlayManagerService.TAG, "Cannot handle package broadcast with null data");
                return;
            }
            String packageName = data.getSchemeSpecificPart();
            boolean z = false;
            boolean replacing = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
            int extraUid = intent.getIntExtra("android.intent.extra.UID", -10000);
            int[] userIds = extraUid == -10000 ? OverlayManagerService.this.mUserManager.getUserIds() : new int[]{UserHandle.getUserId(extraUid)};
            int hashCode = action.hashCode();
            if (hashCode != 172491798) {
                if (hashCode != 525384130) {
                    if (hashCode == 1544582882) {
                    }
                } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    z = true;
                    if (z) {
                        if (z) {
                            onPackageChanged(packageName, userIds);
                            return;
                        } else if (z) {
                            if (replacing) {
                                onPackageReplacing(packageName, userIds);
                                return;
                            } else {
                                onPackageRemoved(packageName, userIds);
                                return;
                            }
                        } else {
                            return;
                        }
                    } else if (replacing) {
                        onPackageReplaced(packageName, userIds);
                        return;
                    } else {
                        onPackageAdded(packageName, userIds);
                        return;
                    }
                }
            } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                z = true;
                if (z) {
                }
            }
            z = true;
            if (z) {
            }
        }

        private void onPackageAdded(String packageName, int[] userIds) {
            try {
                Trace.traceBegin(67108864, "OMS#onPackageAdded " + packageName);
                int length = userIds.length;
                for (int i = 0; i < length; i++) {
                    int userId = userIds[i];
                    synchronized (OverlayManagerService.this.mLock) {
                        PackageInfo pi = OverlayManagerService.this.mPackageManager.getPackageInfo(packageName, userId, false);
                        if (pi != null && !pi.applicationInfo.isInstantApp()) {
                            OverlayManagerService.this.mPackageManager.cachePackageInfo(packageName, userId, pi);
                            if (pi.isOverlayPackage()) {
                                OverlayManagerService.this.mImpl.onOverlayPackageAdded(packageName, userId);
                            } else {
                                OverlayManagerService.this.mImpl.onTargetPackageAdded(packageName, userId);
                            }
                        }
                    }
                }
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        private void onPackageChanged(String packageName, int[] userIds) {
            try {
                Trace.traceBegin(67108864, "OMS#onPackageChanged " + packageName);
                int length = userIds.length;
                for (int i = 0; i < length; i++) {
                    int userId = userIds[i];
                    synchronized (OverlayManagerService.this.mLock) {
                        PackageInfo pi = OverlayManagerService.this.mPackageManager.getPackageInfo(packageName, userId, false);
                        if (pi != null && pi.applicationInfo.isInstantApp()) {
                            OverlayManagerService.this.mPackageManager.cachePackageInfo(packageName, userId, pi);
                            if (pi.isOverlayPackage()) {
                                OverlayManagerService.this.mImpl.onOverlayPackageChanged(packageName, userId);
                            } else {
                                OverlayManagerService.this.mImpl.onTargetPackageChanged(packageName, userId);
                            }
                        }
                    }
                }
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        private void onPackageReplacing(String packageName, int[] userIds) {
            try {
                Trace.traceBegin(67108864, "OMS#onPackageReplacing " + packageName);
                for (int userId : userIds) {
                    synchronized (OverlayManagerService.this.mLock) {
                        OverlayManagerService.this.mPackageManager.forgetPackageInfo(packageName, userId);
                        if (OverlayManagerService.this.mImpl.getOverlayInfo(packageName, userId) != null) {
                            OverlayManagerService.this.mImpl.onOverlayPackageReplacing(packageName, userId);
                        }
                    }
                }
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        private void onPackageReplaced(String packageName, int[] userIds) {
            try {
                Trace.traceBegin(67108864, "OMS#onPackageReplaced " + packageName);
                int length = userIds.length;
                for (int i = 0; i < length; i++) {
                    int userId = userIds[i];
                    synchronized (OverlayManagerService.this.mLock) {
                        PackageInfo pi = OverlayManagerService.this.mPackageManager.getPackageInfo(packageName, userId, false);
                        if (pi != null && !pi.applicationInfo.isInstantApp()) {
                            OverlayManagerService.this.mPackageManager.cachePackageInfo(packageName, userId, pi);
                            if (pi.isOverlayPackage()) {
                                OverlayManagerService.this.mImpl.onOverlayPackageReplaced(packageName, userId);
                            } else {
                                OverlayManagerService.this.mImpl.onTargetPackageReplaced(packageName, userId);
                            }
                        }
                    }
                }
            } finally {
                Trace.traceEnd(67108864);
            }
        }

        private void onPackageRemoved(String packageName, int[] userIds) {
            try {
                Trace.traceBegin(67108864, "OMS#onPackageRemoved " + packageName);
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
            } finally {
                Trace.traceEnd(67108864);
            }
        }
    }

    private final class UserReceiver extends BroadcastReceiver {
        private UserReceiver() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:13:0x0036  */
        /* JADX WARNING: Removed duplicated region for block: B:29:0x0067  */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            char c;
            ArrayList<String> targets;
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode != -2061058799) {
                if (hashCode == 1121780209 && action.equals("android.intent.action.USER_ADDED")) {
                    c = 0;
                    if (c != 0) {
                        if (c == 1 && userId != -10000) {
                            try {
                                Trace.traceBegin(67108864, "OMS ACTION_USER_REMOVED");
                                synchronized (OverlayManagerService.this.mLock) {
                                    OverlayManagerService.this.mImpl.onUserRemoved(userId);
                                    OverlayManagerService.this.mPackageManager.forgetAllPackageInfos(userId);
                                }
                                return;
                            } finally {
                                Trace.traceEnd(67108864);
                            }
                        } else {
                            return;
                        }
                    } else if (userId != -10000) {
                        try {
                            Trace.traceBegin(67108864, "OMS ACTION_USER_ADDED");
                            synchronized (OverlayManagerService.this.mLock) {
                                targets = OverlayManagerService.this.mImpl.updateOverlaysForUser(userId);
                            }
                            OverlayManagerService.this.updateOverlayPaths(userId, targets);
                            return;
                        } finally {
                            Trace.traceEnd(67108864);
                        }
                    } else {
                        return;
                    }
                }
            } else if (action.equals("android.intent.action.USER_REMOVED")) {
                c = 1;
                if (c != 0) {
                }
            }
            c = 65535;
            if (c != 0) {
            }
        }
    }

    /* access modifiers changed from: private */
    public final class OverlayChangeListener implements OverlayManagerServiceImpl.OverlayChangeListener {
        private OverlayChangeListener() {
        }

        @Override // com.android.server.om.OverlayManagerServiceImpl.OverlayChangeListener
        public void onOverlaysChanged(String targetPackageName, int userId) {
            OverlayManagerService.this.schedulePersistSettings();
            FgThread.getHandler().post(new Runnable(userId, targetPackageName) {
                /* class com.android.server.om.$$Lambda$OverlayManagerService$OverlayChangeListener$u9oeN2C0PDMo0pYiLqfMBkwuMNA */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    OverlayManagerService.OverlayChangeListener.this.lambda$onOverlaysChanged$0$OverlayManagerService$OverlayChangeListener(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onOverlaysChanged$0$OverlayManagerService$OverlayChangeListener(int userId, String targetPackageName) {
            OverlayManagerService.this.updateAssets(userId, targetPackageName);
            Intent intent = new Intent("android.intent.action.OVERLAY_CHANGED", Uri.fromParts("package", targetPackageName, null));
            intent.setFlags(DumpState.DUMP_HANDLE);
            try {
                try {
                    ActivityManager.getService().broadcastIntent((IApplicationThread) null, intent, (String) null, (IIntentReceiver) null, 0, (String) null, (Bundle) null, (String[]) null, -1, (Bundle) null, false, false, userId);
                } catch (RemoteException e) {
                }
            } catch (RemoteException e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateOverlayPaths(int userId, List<String> targetPackageNames) {
        Throwable th;
        List<String> targetPackageNames2 = targetPackageNames;
        try {
            Trace.traceBegin(67108864, "OMS#updateOverlayPaths " + targetPackageNames2);
            PackageManagerInternal pm = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
            boolean updateFrameworkRes = targetPackageNames2.contains(PackageManagerService.PLATFORM_PACKAGE_NAME);
            boolean updateFrameworkReshwext = targetPackageNames2.contains("androidhwext");
            if (updateFrameworkRes || updateFrameworkReshwext) {
                targetPackageNames2 = pm.getTargetPackageNames(userId);
            }
            try {
                Map<String, List<String>> pendingChanges = new ArrayMap<>(targetPackageNames2.size());
                List<String> frameworkhwextOverlays = this.mImpl.getEnabledOverlayPackageNames("androidhwext", userId);
                synchronized (this.mLock) {
                    List<String> frameworkOverlays = this.mImpl.getEnabledOverlayPackageNames(PackageManagerService.PLATFORM_PACKAGE_NAME, userId);
                    int n = targetPackageNames2.size();
                    for (int i = 0; i < n; i++) {
                        String targetPackageName = targetPackageNames2.get(i);
                        List<String> list = new ArrayList<>();
                        if (!PackageManagerService.PLATFORM_PACKAGE_NAME.equals(targetPackageName)) {
                            list.addAll(frameworkOverlays);
                        }
                        boolean isInDataSkinDir = isInDataSkinDir(targetPackageName);
                        if (!"androidhwext".equals(targetPackageName) && !isInDataSkinDir) {
                            list.addAll(frameworkhwextOverlays);
                        }
                        list.addAll(this.mImpl.getEnabledOverlayPackageNames(targetPackageName, userId));
                        pendingChanges.put(targetPackageName, list);
                    }
                }
                int n2 = targetPackageNames2.size();
                for (int i2 = 0; i2 < n2; i2++) {
                    String targetPackageName2 = targetPackageNames2.get(i2);
                    if (!pm.setEnabledOverlayPackages(userId, targetPackageName2, pendingChanges.get(targetPackageName2))) {
                        Slog.e(TAG, String.format("Failed to change enabled overlays for %s user %d", targetPackageName2, Integer.valueOf(userId)));
                    }
                }
                Trace.traceEnd(67108864);
            } catch (Throwable th2) {
                th = th2;
                Trace.traceEnd(67108864);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            Trace.traceEnd(67108864);
            throw th;
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
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void schedulePersistSettings() {
        if (!this.mPersistSettingsScheduled.getAndSet(true)) {
            IoThread.getHandler().post(new Runnable() {
                /* class com.android.server.om.$$Lambda$OverlayManagerService$_WGEV7N0qhntbqqDW3A1OTVv5o */

                @Override // java.lang.Runnable
                public final void run() {
                    OverlayManagerService.this.lambda$schedulePersistSettings$0$OverlayManagerService();
                }
            });
        }
    }

    public /* synthetic */ void lambda$schedulePersistSettings$0$OverlayManagerService() {
        this.mPersistSettingsScheduled.set(false);
        synchronized (this.mLock) {
            FileOutputStream stream = null;
            try {
                stream = this.mSettingsFile.startWrite();
                this.mSettings.persist(stream);
                this.mSettingsFile.finishWrite(stream);
            } catch (IOException | XmlPullParserException e) {
                this.mSettingsFile.failWrite(stream);
                Slog.e(TAG, "failed to persist overlay state", e);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0073, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0074, code lost:
        if (r3 != null) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x007a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x007b, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x007e, code lost:
        throw r5;
     */
    private void restoreSettings() {
        try {
            Trace.traceBegin(67108864, "OMS#restoreSettings");
            synchronized (this.mLock) {
                if (this.mSettingsFile.getBaseFile().exists()) {
                    FileInputStream stream = this.mSettingsFile.openRead();
                    this.mSettings.restore(stream);
                    List<UserInfo> liveUsers = this.mUserManager.getUsers(true);
                    int[] liveUserIds = new int[liveUsers.size()];
                    for (int i = 0; i < liveUsers.size(); i++) {
                        liveUserIds[i] = liveUsers.get(i).getUserHandle().getIdentifier();
                    }
                    Arrays.sort(liveUserIds);
                    int[] users = this.mSettings.getUsers();
                    for (int userId : users) {
                        if (Arrays.binarySearch(liveUserIds, userId) < 0) {
                            this.mSettings.removeUser(userId);
                        }
                    }
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException | XmlPullParserException e) {
                            Slog.e(TAG, "failed to restore overlay state", e);
                        }
                    }
                    Trace.traceEnd(67108864);
                }
            }
        } finally {
            Trace.traceEnd(67108864);
        }
    }

    /* access modifiers changed from: private */
    public static final class PackageManagerHelper implements OverlayManagerServiceImpl.PackageManagerHelper {
        private static final String TAB1 = "    ";
        private static final String TAB2 = "        ";
        private final SparseArray<HashMap<String, PackageInfo>> mCache = new SparseArray<>();
        private final IPackageManager mPackageManager = AppGlobals.getPackageManager();
        private final PackageManagerInternal mPackageManagerInternal = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class));

        PackageManagerHelper() {
        }

        public PackageInfo getPackageInfo(String packageName, int userId, boolean useCache) {
            PackageInfo cachedPi;
            if (useCache && (cachedPi = getCachedPackageInfo(packageName, userId)) != null) {
                return cachedPi;
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

        @Override // com.android.server.om.OverlayManagerServiceImpl.PackageManagerHelper
        public PackageInfo getPackageInfo(String packageName, int userId) {
            return getPackageInfo(packageName, userId, true);
        }

        @Override // com.android.server.om.OverlayManagerServiceImpl.PackageManagerHelper
        public boolean signaturesMatching(String packageName1, String packageName2, int userId) {
            try {
                return this.mPackageManager.checkSignatures(packageName1, packageName2) == 0;
            } catch (RemoteException e) {
                return false;
            }
        }

        @Override // com.android.server.om.OverlayManagerServiceImpl.PackageManagerHelper
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

        public void dump(PrintWriter pw, DumpState dumpState) {
            pw.println("PackageInfo cache");
            if (!dumpState.isVerbose()) {
                int count = 0;
                int n = this.mCache.size();
                for (int i = 0; i < n; i++) {
                    count += this.mCache.get(this.mCache.keyAt(i)).size();
                }
                pw.println(TAB1 + count + " package(s)");
            } else if (this.mCache.size() == 0) {
                pw.println("    <empty>");
            } else {
                int n2 = this.mCache.size();
                for (int i2 = 0; i2 < n2; i2++) {
                    int userId = this.mCache.keyAt(i2);
                    pw.println("    User " + userId);
                    for (Map.Entry<String, PackageInfo> entry : this.mCache.get(userId).entrySet()) {
                        pw.println(TAB2 + entry.getKey() + ": " + entry.getValue());
                    }
                }
            }
        }
    }
}
