package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageManagerInternal.ExternalSourcesPolicy;
import android.media.AudioAttributes;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.UserHandle;
import android.os.storage.StorageManagerInternal;
import android.os.storage.StorageManagerInternal.ExternalStorageMountPolicy;
import android.rms.HwSysResource;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IAppOpsService.Stub;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.power.IHwShutdownThread;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class AppOpsService extends Stub {
    static final boolean DEBUG = false;
    static final String TAG = "AppOps";
    static final long WRITE_DELAY = 1800000;
    private HwSysResource mAppOpsResource;
    final SparseArray<SparseArray<Restriction>> mAudioRestrictions = new SparseArray();
    final ArrayMap<IBinder, ClientState> mClients = new ArrayMap();
    Context mContext;
    boolean mFastWriteScheduled;
    final AtomicFile mFile;
    final Handler mHandler;
    final ArrayMap<IBinder, Callback> mModeWatchers = new ArrayMap();
    final SparseArray<ArraySet<Callback>> mOpModeWatchers = new SparseArray();
    private final ArrayMap<IBinder, ClientRestrictionState> mOpUserRestrictions = new ArrayMap();
    final ArrayMap<String, ArraySet<Callback>> mPackageModeWatchers = new ArrayMap();
    private final SparseArray<UidState> mUidStates = new SparseArray();
    final Runnable mWriteRunner = new Runnable() {
        public void run() {
            synchronized (AppOpsService.this) {
                AppOpsService.this.mWriteScheduled = false;
                AppOpsService.this.mFastWriteScheduled = false;
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... params) {
                        AppOpsService.this.writeState();
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            }
        }
    };
    boolean mWriteScheduled;

    public final class Callback implements DeathRecipient {
        final IAppOpsCallback mCallback;

        public Callback(IAppOpsCallback callback) {
            this.mCallback = callback;
            try {
                this.mCallback.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        public void unlinkToDeath() {
            this.mCallback.asBinder().unlinkToDeath(this, 0);
        }

        public void binderDied() {
            AppOpsService.this.stopWatchingMode(this.mCallback);
        }
    }

    static final class ChangeRec {
        final int op;
        final String pkg;
        final int uid;

        ChangeRec(int _op, int _uid, String _pkg) {
            this.op = _op;
            this.uid = _uid;
            this.pkg = _pkg;
        }
    }

    private final class ClientRestrictionState implements DeathRecipient {
        SparseArray<String[]> perUserExcludedPackages;
        SparseArray<boolean[]> perUserRestrictions;
        private final IBinder token;

        public ClientRestrictionState(IBinder token) throws RemoteException {
            token.linkToDeath(this, 0);
            this.token = token;
        }

        public boolean setRestriction(int code, boolean restricted, String[] excludedPackages, int userId) {
            boolean changed = false;
            if (this.perUserRestrictions == null && restricted) {
                this.perUserRestrictions = new SparseArray();
            }
            if (this.perUserRestrictions == null) {
                return false;
            }
            boolean[] userRestrictions = (boolean[]) this.perUserRestrictions.get(userId);
            if (userRestrictions == null && restricted) {
                userRestrictions = new boolean[70];
                this.perUserRestrictions.put(userId, userRestrictions);
            }
            if (!(userRestrictions == null || userRestrictions[code] == restricted)) {
                userRestrictions[code] = restricted;
                if (!restricted && isDefault(userRestrictions)) {
                    this.perUserRestrictions.remove(userId);
                    userRestrictions = null;
                }
                changed = true;
            }
            if (userRestrictions == null) {
                return changed;
            }
            boolean noExcludedPackages = ArrayUtils.isEmpty(excludedPackages);
            if (this.perUserExcludedPackages == null && (noExcludedPackages ^ 1) != 0) {
                this.perUserExcludedPackages = new SparseArray();
            }
            if (this.perUserExcludedPackages == null || (Arrays.equals(excludedPackages, (Object[]) this.perUserExcludedPackages.get(userId)) ^ 1) == 0) {
                return changed;
            }
            if (noExcludedPackages) {
                this.perUserExcludedPackages.remove(userId);
                if (this.perUserExcludedPackages.size() <= 0) {
                    this.perUserExcludedPackages = null;
                }
            } else {
                this.perUserExcludedPackages.put(userId, excludedPackages);
            }
            return true;
        }

        public boolean hasRestriction(int restriction, String packageName, int userId) {
            if (this.perUserRestrictions == null) {
                return false;
            }
            boolean[] restrictions = (boolean[]) this.perUserRestrictions.get(userId);
            if (restrictions == null || !restrictions[restriction]) {
                return false;
            }
            if (this.perUserExcludedPackages == null) {
                return true;
            }
            String[] perUserExclusions = (String[]) this.perUserExcludedPackages.get(userId);
            if (perUserExclusions == null) {
                return true;
            }
            return ArrayUtils.contains(perUserExclusions, packageName) ^ 1;
        }

        public void removeUser(int userId) {
            if (this.perUserExcludedPackages != null) {
                this.perUserExcludedPackages.remove(userId);
                if (this.perUserExcludedPackages.size() <= 0) {
                    this.perUserExcludedPackages = null;
                }
            }
            if (this.perUserRestrictions != null) {
                this.perUserRestrictions.remove(userId);
                if (this.perUserRestrictions.size() <= 0) {
                    this.perUserRestrictions = null;
                }
            }
        }

        public boolean isDefault() {
            return this.perUserRestrictions == null || this.perUserRestrictions.size() <= 0;
        }

        public void binderDied() {
            synchronized (AppOpsService.this) {
                AppOpsService.this.mOpUserRestrictions.remove(this.token);
                if (this.perUserRestrictions == null) {
                    return;
                }
                int userCount = this.perUserRestrictions.size();
                for (int i = 0; i < userCount; i++) {
                    boolean[] restrictions = (boolean[]) this.perUserRestrictions.valueAt(i);
                    int restrictionCount = restrictions.length;
                    for (int j = 0; j < restrictionCount; j++) {
                        if (restrictions[j]) {
                            AppOpsService.this.mHandler.post(new -$Lambda$pxHDwRy_8WGgaOZHQ9e3tJlXPqU(j, this));
                        }
                    }
                }
                destroy();
            }
        }

        /* synthetic */ void lambda$-com_android_server_AppOpsService$ClientRestrictionState_113687(int changedCode) {
            AppOpsService.this.notifyWatchersOfChange(changedCode);
        }

        public void destroy() {
            this.token.unlinkToDeath(this, 0);
        }

        private boolean isDefault(boolean[] array) {
            if (ArrayUtils.isEmpty(array)) {
                return true;
            }
            for (boolean value : array) {
                if (value) {
                    return false;
                }
            }
            return true;
        }
    }

    public final class ClientState extends Binder implements DeathRecipient {
        final IBinder mAppToken;
        final int mPid = Binder.getCallingPid();
        final ArrayList<Op> mStartedOps;

        public ClientState(IBinder appToken) {
            this.mAppToken = appToken;
            if (appToken instanceof Binder) {
                this.mStartedOps = null;
                return;
            }
            this.mStartedOps = new ArrayList();
            try {
                this.mAppToken.linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        public String toString() {
            return "ClientState{mAppToken=" + this.mAppToken + ", " + (this.mStartedOps != null ? "pid=" + this.mPid : "local") + '}';
        }

        public void binderDied() {
            synchronized (AppOpsService.this) {
                for (int i = this.mStartedOps.size() - 1; i >= 0; i--) {
                    AppOpsService.this.finishOperationLocked((Op) this.mStartedOps.get(i));
                }
                AppOpsService.this.mClients.remove(this.mAppToken);
            }
        }
    }

    public static final class Op {
        public int duration;
        public int mode;
        public int nesting;
        public final int op;
        public final String packageName;
        public String proxyPackageName;
        public int proxyUid = -1;
        public long rejectTime;
        public long time;
        public final int uid;

        public Op(int _uid, String _packageName, int _op) {
            this.uid = _uid;
            this.packageName = _packageName;
            this.op = _op;
            this.mode = AppOpsManager.opToDefaultMode(this.op);
        }
    }

    public static final class Ops extends SparseArray<Op> {
        public boolean isPrivileged;
        protected boolean needSyncPriv;
        public final String packageName;
        public final UidState uidState;

        public Ops(String _packageName, UidState _uidState, boolean _isPrivileged, boolean sync) {
            this.packageName = _packageName;
            this.uidState = _uidState;
            this.isPrivileged = _isPrivileged;
            this.needSyncPriv = sync;
        }
    }

    private static final class Restriction {
        private static final ArraySet<String> NO_EXCEPTIONS = new ArraySet();
        ArraySet<String> exceptionPackages;
        int mode;

        /* synthetic */ Restriction(Restriction -this0) {
            this();
        }

        private Restriction() {
            this.exceptionPackages = NO_EXCEPTIONS;
        }
    }

    static class Shell extends ShellCommand {
        final IAppOpsService mInterface;
        final AppOpsService mInternal;
        int mode;
        String modeStr;
        int nonpackageUid;
        int op;
        String opStr;
        String packageName;
        int packageUid;
        int userId = 0;

        Shell(IAppOpsService iface, AppOpsService internal) {
            this.mInterface = iface;
            this.mInternal = internal;
        }

        public int onCommand(String cmd) {
            return AppOpsService.onShellCommand(this, cmd);
        }

        public void onHelp() {
            AppOpsService.dumpCommandHelp(getOutPrintWriter());
        }

        private int strOpToOp(String op, PrintWriter err) {
            try {
                return AppOpsManager.strOpToOp(op);
            } catch (IllegalArgumentException e) {
                try {
                    return Integer.parseInt(op);
                } catch (NumberFormatException e2) {
                    try {
                        return AppOpsManager.strDebugOpToOp(op);
                    } catch (IllegalArgumentException e3) {
                        err.println("Error: " + e3.getMessage());
                        return -1;
                    }
                }
            }
        }

        int strModeToMode(String modeStr, PrintWriter err) {
            if (modeStr.equals("allow")) {
                return 0;
            }
            if (modeStr.equals("deny")) {
                return 2;
            }
            if (modeStr.equals("ignore")) {
                return 1;
            }
            if (modeStr.equals("default")) {
                return 3;
            }
            try {
                return Integer.parseInt(modeStr);
            } catch (NumberFormatException e) {
                err.println("Error: Mode " + modeStr + " is not valid");
                return -1;
            }
        }

        int parseUserOpMode(int defMode, PrintWriter err) throws RemoteException {
            this.userId = -2;
            this.opStr = null;
            this.modeStr = null;
            while (true) {
                String argument = getNextArg();
                if (argument == null) {
                    break;
                } else if ("--user".equals(argument)) {
                    this.userId = UserHandle.parseUserArg(getNextArgRequired());
                } else if (this.opStr == null) {
                    this.opStr = argument;
                } else if (this.modeStr == null) {
                    this.modeStr = argument;
                    break;
                }
            }
            if (this.opStr == null) {
                err.println("Error: Operation not specified.");
                return -1;
            }
            this.op = strOpToOp(this.opStr, err);
            if (this.op < 0) {
                return -1;
            }
            if (this.modeStr != null) {
                int strModeToMode = strModeToMode(this.modeStr, err);
                this.mode = strModeToMode;
                if (strModeToMode < 0) {
                    return -1;
                }
            }
            this.mode = defMode;
            return 0;
        }

        int parseUserPackageOp(boolean reqOp, PrintWriter err) throws RemoteException {
            this.userId = -2;
            this.packageName = null;
            this.opStr = null;
            while (true) {
                String argument = getNextArg();
                if (argument == null) {
                    break;
                } else if ("--user".equals(argument)) {
                    this.userId = UserHandle.parseUserArg(getNextArgRequired());
                } else if (this.packageName == null) {
                    this.packageName = argument;
                } else if (this.opStr == null) {
                    this.opStr = argument;
                    break;
                }
            }
            if (this.packageName == null) {
                err.println("Error: Package name not specified.");
                return -1;
            } else if (this.opStr == null && reqOp) {
                err.println("Error: Operation not specified.");
                return -1;
            } else {
                if (this.opStr != null) {
                    this.op = strOpToOp(this.opStr, err);
                    if (this.op < 0) {
                        return -1;
                    }
                }
                this.op = -1;
                if (this.userId == -2) {
                    this.userId = ActivityManager.getCurrentUser();
                }
                this.nonpackageUid = -1;
                try {
                    this.nonpackageUid = Integer.parseInt(this.packageName);
                } catch (NumberFormatException e) {
                }
                if (this.nonpackageUid == -1 && this.packageName.length() > 1 && this.packageName.charAt(0) == 'u' && this.packageName.indexOf(46) < 0) {
                    int i = 1;
                    while (i < this.packageName.length() && this.packageName.charAt(i) >= '0' && this.packageName.charAt(i) <= '9') {
                        i++;
                    }
                    if (i > 1 && i < this.packageName.length()) {
                        try {
                            int user = Integer.parseInt(this.packageName.substring(1, i));
                            char type = this.packageName.charAt(i);
                            i++;
                            int startTypeVal = i;
                            while (i < this.packageName.length() && this.packageName.charAt(i) >= '0' && this.packageName.charAt(i) <= '9') {
                                i++;
                            }
                            if (i > startTypeVal) {
                                try {
                                    int typeVal = Integer.parseInt(this.packageName.substring(startTypeVal, i));
                                    if (type == 'a') {
                                        this.nonpackageUid = UserHandle.getUid(user, typeVal + 10000);
                                    } else if (type == 's') {
                                        this.nonpackageUid = UserHandle.getUid(user, typeVal);
                                    }
                                } catch (NumberFormatException e2) {
                                }
                            }
                        } catch (NumberFormatException e3) {
                        }
                    }
                }
                if (this.nonpackageUid != -1) {
                    this.packageName = null;
                } else {
                    if ("root".equals(this.packageName)) {
                        this.packageUid = 0;
                    } else {
                        this.packageUid = AppGlobals.getPackageManager().getPackageUid(this.packageName, 8192, this.userId);
                    }
                    if (this.packageUid < 0) {
                        err.println("Error: No UID for " + this.packageName + " in user " + this.userId);
                        return -1;
                    }
                }
                return 0;
            }
        }
    }

    private static final class UidState {
        public SparseIntArray opModes;
        public ArrayMap<String, Ops> pkgOps;
        public final int uid;

        public UidState(int uid) {
            this.uid = uid;
        }

        public void clear() {
            this.pkgOps = null;
            this.opModes = null;
        }

        public boolean isDefault() {
            if (this.pkgOps != null && !this.pkgOps.isEmpty()) {
                return false;
            }
            if (this.opModes == null || this.opModes.size() <= 0) {
                return true;
            }
            return false;
        }
    }

    public AppOpsService(File storagePath, Handler handler) {
        LockGuard.installLock((Object) this, 0);
        this.mFile = new AtomicFile(storagePath);
        this.mHandler = handler;
        readState();
    }

    public void publish(Context context) {
        this.mContext = context;
        ServiceManager.addService("appops", asBinder());
    }

    public void systemReady() {
        synchronized (this) {
            boolean changed = false;
            for (int i = this.mUidStates.size() - 1; i >= 0; i--) {
                UidState uidState = (UidState) this.mUidStates.valueAt(i);
                if (ArrayUtils.isEmpty(getPackagesForUid(uidState.uid))) {
                    uidState.clear();
                    this.mUidStates.removeAt(i);
                    changed = true;
                } else {
                    ArrayMap<String, Ops> pkgs = uidState.pkgOps;
                    if (pkgs != null) {
                        Iterator<Ops> it = pkgs.values().iterator();
                        while (it.hasNext()) {
                            Ops ops = (Ops) it.next();
                            int curUid = -1;
                            try {
                                curUid = AppGlobals.getPackageManager().getPackageUid(ops.packageName, 8192, UserHandle.getUserId(ops.uidState.uid));
                            } catch (RemoteException e) {
                            }
                            if (curUid != ops.uidState.uid) {
                                Slog.i(TAG, "Pruning old package " + ops.packageName + "/" + ops.uidState + ": new uid=" + curUid);
                                it.remove();
                                changed = true;
                            } else if (ops.needSyncPriv) {
                                boolean oldpriv = ops.isPrivileged;
                                syncPrivState(curUid, ops.packageName, ops);
                                if (oldpriv != ops.isPrivileged) {
                                    Slog.i(TAG, "systemReady priv adjusted for package: " + ops.packageName + " uid " + curUid + " oldpriv: " + oldpriv + " npriv:" + ops.isPrivileged);
                                    changed = true;
                                }
                            }
                        }
                        if (uidState.isDefault()) {
                            this.mUidStates.removeAt(i);
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (changed) {
                scheduleFastWriteLocked();
            }
        }
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setExternalSourcesPolicy(new ExternalSourcesPolicy() {
            public int getPackageTrustedToInstallApps(String packageName, int uid) {
                switch (AppOpsService.this.checkOperation(66, uid, packageName)) {
                    case 0:
                        return 0;
                    case 2:
                        return 1;
                    default:
                        return 2;
                }
            }
        });
        ((StorageManagerInternal) LocalServices.getService(StorageManagerInternal.class)).addExternalStoragePolicy(new ExternalStorageMountPolicy() {
            public int getMountMode(int uid, String packageName) {
                if (Process.isIsolated(uid) || AppOpsService.this.noteOperation(59, uid, packageName) != 0) {
                    return 0;
                }
                if (AppOpsService.this.noteOperation(60, uid, packageName) != 0) {
                    return 2;
                }
                return 3;
            }

            public boolean hasExternalStorage(int uid, String packageName) {
                int mountMode = getMountMode(uid, packageName);
                if (mountMode == 2 || mountMode == 3) {
                    return true;
                }
                return false;
            }
        });
    }

    /* JADX WARNING: Missing block: B:22:0x0037, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void packageRemoved(int uid, String packageName) {
        synchronized (this) {
            UidState uidState = (UidState) this.mUidStates.get(uid);
            if (uidState == null) {
                return;
            }
            boolean changed = false;
            if (!(uidState.pkgOps == null || uidState.pkgOps.remove(packageName) == null)) {
                changed = true;
            }
            if (changed && uidState.pkgOps.isEmpty() && getPackagesForUid(uid).length <= 0) {
                this.mUidStates.remove(uid);
            }
            if (changed) {
                scheduleFastWriteLocked();
            }
        }
    }

    public void uidRemoved(int uid) {
        synchronized (this) {
            if (this.mUidStates.indexOfKey(uid) >= 0) {
                this.mUidStates.remove(uid);
                if (this.mAppOpsResource != null) {
                    this.mAppOpsResource.clear(uid, null, 0);
                }
                scheduleFastWriteLocked();
            }
        }
    }

    public void shutdown() {
        Slog.w(TAG, "Writing app ops before shutdown...");
        boolean doWrite = false;
        synchronized (this) {
            if (this.mWriteScheduled) {
                this.mWriteScheduled = false;
                doWrite = true;
            }
        }
        if (doWrite) {
            writeState();
        }
    }

    private ArrayList<OpEntry> collectOps(Ops pkgOps, int[] ops) {
        ArrayList<OpEntry> arrayList = null;
        int j;
        Op curOp;
        if (ops == null) {
            arrayList = new ArrayList();
            for (j = 0; j < pkgOps.size(); j++) {
                curOp = (Op) pkgOps.valueAt(j);
                arrayList.add(new OpEntry(curOp.op, curOp.mode, curOp.time, curOp.rejectTime, curOp.duration, curOp.proxyUid, curOp.proxyPackageName));
            }
        } else {
            for (int i : ops) {
                curOp = (Op) pkgOps.get(i);
                if (curOp != null) {
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    arrayList.add(new OpEntry(curOp.op, curOp.mode, curOp.time, curOp.rejectTime, curOp.duration, curOp.proxyUid, curOp.proxyPackageName));
                }
            }
        }
        return arrayList;
    }

    private ArrayList<OpEntry> collectOps(SparseIntArray uidOps, int[] ops) {
        ArrayList<OpEntry> arrayList = null;
        int j;
        if (ops == null) {
            arrayList = new ArrayList();
            for (j = 0; j < uidOps.size(); j++) {
                arrayList.add(new OpEntry(uidOps.keyAt(j), uidOps.valueAt(j), 0, 0, 0, -1, null));
            }
        } else {
            for (int indexOfKey : ops) {
                int index = uidOps.indexOfKey(indexOfKey);
                if (index >= 0) {
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    arrayList.add(new OpEntry(uidOps.keyAt(index), uidOps.valueAt(index), 0, 0, 0, -1, null));
                }
            }
        }
        return arrayList;
    }

    public List<PackageOps> getPackagesForOps(int[] ops) {
        Throwable th;
        this.mContext.enforcePermission("android.permission.GET_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        List<PackageOps> res = null;
        synchronized (this) {
            try {
                int uidStateCount = this.mUidStates.size();
                for (int i = 0; i < uidStateCount; i++) {
                    UidState uidState = (UidState) this.mUidStates.valueAt(i);
                    if (!(uidState.pkgOps == null || uidState.pkgOps.isEmpty())) {
                        ArrayMap<String, Ops> packages = uidState.pkgOps;
                        int packageCount = packages.size();
                        int j = 0;
                        ArrayList<PackageOps> res2 = res;
                        while (j < packageCount) {
                            ArrayList<PackageOps> res3;
                            try {
                                Ops pkgOps = (Ops) packages.valueAt(j);
                                ArrayList<OpEntry> resOps = collectOps(pkgOps, ops);
                                if (resOps != null) {
                                    if (res2 == null) {
                                        res3 = new ArrayList();
                                    } else {
                                        res3 = res2;
                                    }
                                    res3.add(new PackageOps(pkgOps.packageName, pkgOps.uidState.uid, resOps));
                                } else {
                                    res3 = res2;
                                }
                                j++;
                                res2 = res3;
                            } catch (Throwable th2) {
                                th = th2;
                                res3 = res2;
                                throw th;
                            }
                        }
                        Object res4 = res2;
                    }
                }
                return res4;
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public List<PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) {
        this.mContext.enforcePermission("android.permission.GET_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName == null) {
            return Collections.emptyList();
        }
        synchronized (this) {
            Ops pkgOps = getOpsRawLocked(uid, resolvedPackageName, false);
            if (pkgOps == null) {
                return null;
            }
            ArrayList<OpEntry> resOps = collectOps(pkgOps, ops);
            if (resOps == null) {
                return null;
            }
            ArrayList<PackageOps> res = new ArrayList();
            res.add(new PackageOps(pkgOps.packageName, pkgOps.uidState.uid, resOps));
            return res;
        }
    }

    public List<PackageOps> getUidOps(int uid, int[] ops) {
        this.mContext.enforcePermission("android.permission.GET_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        synchronized (this) {
            UidState uidState = getUidStateLocked(uid, false);
            if (uidState == null) {
                return null;
            }
            ArrayList<OpEntry> resOps = collectOps(uidState.opModes, ops);
            if (resOps == null) {
                return null;
            }
            ArrayList<PackageOps> res = new ArrayList();
            res.add(new PackageOps(null, uidState.uid, resOps));
            return res;
        }
    }

    private void pruneOp(Op op, int uid, String packageName) {
        if (op.time == 0 && op.rejectTime == 0) {
            Ops ops = getOpsRawLocked(uid, packageName, false);
            if (ops != null) {
                ops.remove(op.op);
                if (ops.size() <= 0) {
                    UidState uidState = ops.uidState;
                    ArrayMap<String, Ops> pkgOps = uidState.pkgOps;
                    if (pkgOps != null) {
                        pkgOps.remove(ops.packageName);
                        if (pkgOps.isEmpty()) {
                            uidState.pkgOps = null;
                        }
                        if (uidState.isDefault()) {
                            this.mUidStates.remove(uid);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x007a, code:
            r20 = getPackagesForUid(r29);
            r6 = null;
     */
    /* JADX WARNING: Missing block: B:16:0x007f, code:
            monitor-enter(r27);
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            r8 = (android.util.ArraySet) r27.mOpModeWatchers.get(r28);
     */
    /* JADX WARNING: Missing block: B:19:0x0090, code:
            if (r8 == null) goto L_0x0134;
     */
    /* JADX WARNING: Missing block: B:20:0x0092, code:
            r5 = r8.size();
     */
    /* JADX WARNING: Missing block: B:21:0x0095, code:
            r12 = 0;
            r7 = null;
     */
    /* JADX WARNING: Missing block: B:22:0x0098, code:
            if (r12 >= r5) goto L_0x0133;
     */
    /* JADX WARNING: Missing block: B:24:?, code:
            r4 = (com.android.server.AppOpsService.Callback) r8.valueAt(r12);
            r9 = new android.util.ArraySet();
            java.util.Collections.addAll(r9, r20);
            r6 = new android.util.ArrayMap();
     */
    /* JADX WARNING: Missing block: B:26:?, code:
            r6.put(r4, r9);
     */
    /* JADX WARNING: Missing block: B:27:0x00b2, code:
            r12 = r12 + 1;
            r7 = r6;
     */
    /* JADX WARNING: Missing block: B:51:0x0133, code:
            r6 = r7;
     */
    /* JADX WARNING: Missing block: B:52:0x0134, code:
            r22 = 0;
     */
    /* JADX WARNING: Missing block: B:54:?, code:
            r23 = r20.length;
     */
    /* JADX WARNING: Missing block: B:55:0x013c, code:
            r7 = r6;
     */
    /* JADX WARNING: Missing block: B:56:0x0140, code:
            if (r22 >= r23) goto L_0x0187;
     */
    /* JADX WARNING: Missing block: B:58:?, code:
            r19 = r20[r22];
            r8 = (android.util.ArraySet) r27.mPackageModeWatchers.get(r19);
     */
    /* JADX WARNING: Missing block: B:59:0x0154, code:
            if (r8 == null) goto L_0x0182;
     */
    /* JADX WARNING: Missing block: B:60:0x0156, code:
            if (r7 != null) goto L_0x01fb;
     */
    /* JADX WARNING: Missing block: B:61:0x0158, code:
            r6 = new android.util.ArrayMap();
     */
    /* JADX WARNING: Missing block: B:63:?, code:
            r5 = r8.size();
            r12 = 0;
     */
    /* JADX WARNING: Missing block: B:64:0x0162, code:
            if (r12 >= r5) goto L_0x0183;
     */
    /* JADX WARNING: Missing block: B:65:0x0164, code:
            r4 = (com.android.server.AppOpsService.Callback) r8.valueAt(r12);
            r9 = (android.util.ArraySet) r6.get(r4);
     */
    /* JADX WARNING: Missing block: B:66:0x0170, code:
            if (r9 != null) goto L_0x017a;
     */
    /* JADX WARNING: Missing block: B:67:0x0172, code:
            r9 = new android.util.ArraySet();
            r6.put(r4, r9);
     */
    /* JADX WARNING: Missing block: B:68:0x017a, code:
            r9.add(r19);
     */
    /* JADX WARNING: Missing block: B:69:0x017f, code:
            r12 = r12 + 1;
     */
    /* JADX WARNING: Missing block: B:70:0x0182, code:
            r6 = r7;
     */
    /* JADX WARNING: Missing block: B:71:0x0183, code:
            r22 = r22 + 1;
     */
    /* JADX WARNING: Missing block: B:72:0x0187, code:
            monitor-exit(r27);
     */
    /* JADX WARNING: Missing block: B:73:0x0188, code:
            if (r7 != null) goto L_0x018e;
     */
    /* JADX WARNING: Missing block: B:74:0x018a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:75:0x018b, code:
            r22 = th;
     */
    /* JADX WARNING: Missing block: B:78:0x018e, code:
            r14 = android.os.Binder.clearCallingIdentity();
            r12 = 0;
     */
    /* JADX WARNING: Missing block: B:81:0x0199, code:
            if (r12 >= r7.size()) goto L_0x01f4;
     */
    /* JADX WARNING: Missing block: B:82:0x019b, code:
            r4 = (com.android.server.AppOpsService.Callback) r7.keyAt(r12);
            r18 = (android.util.ArraySet) r7.valueAt(r12);
     */
    /* JADX WARNING: Missing block: B:83:0x01a7, code:
            if (r18 != null) goto L_0x01bd;
     */
    /* JADX WARNING: Missing block: B:85:?, code:
            r4.mCallback.opChanged(r28, r29, null);
     */
    /* JADX WARNING: Missing block: B:87:0x01bd, code:
            r16 = r18.size();
            r13 = 0;
     */
    /* JADX WARNING: Missing block: B:89:0x01c4, code:
            if (r13 >= r16) goto L_0x01ba;
     */
    /* JADX WARNING: Missing block: B:90:0x01c6, code:
            r4.mCallback.opChanged(r28, r29, (java.lang.String) r18.valueAt(r13));
     */
    /* JADX WARNING: Missing block: B:91:0x01dd, code:
            r13 = r13 + 1;
     */
    /* JADX WARNING: Missing block: B:92:0x01e0, code:
            r11 = move-exception;
     */
    /* JADX WARNING: Missing block: B:94:?, code:
            android.util.Log.w(TAG, "Error dispatching op op change", r11);
     */
    /* JADX WARNING: Missing block: B:96:0x01f0, code:
            android.os.Binder.restoreCallingIdentity(r14);
     */
    /* JADX WARNING: Missing block: B:98:0x01f4, code:
            android.os.Binder.restoreCallingIdentity(r14);
     */
    /* JADX WARNING: Missing block: B:99:0x01f7, code:
            return;
     */
    /* JADX WARNING: Missing block: B:100:0x01f8, code:
            r22 = th;
     */
    /* JADX WARNING: Missing block: B:101:0x01f9, code:
            r6 = r7;
     */
    /* JADX WARNING: Missing block: B:102:0x01fb, code:
            r6 = r7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setUidMode(int code, int uid, int mode) {
        if (Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        }
        verifyIncomingOp(code);
        code = AppOpsManager.opToSwitch(code);
        synchronized (this) {
            int defaultMode = AppOpsManager.opToDefaultMode(code);
            UidState uidState = getUidStateLocked(uid, false);
            if (uidState == null) {
                if (mode == defaultMode) {
                    return;
                }
                UidState uidState2 = new UidState(uid);
                uidState2.opModes = new SparseIntArray();
                uidState2.opModes.put(code, mode);
                this.mUidStates.put(uid, uidState2);
                scheduleWriteLocked();
            } else if (uidState.opModes == null) {
                if (mode != defaultMode) {
                    uidState.opModes = new SparseIntArray();
                    uidState.opModes.put(code, mode);
                    scheduleWriteLocked();
                }
            } else if (uidState.opModes.get(code) == mode) {
                return;
            } else {
                if (mode == defaultMode) {
                    uidState.opModes.delete(code);
                    if (uidState.opModes.size() <= 0) {
                        uidState.opModes = null;
                    }
                } else {
                    uidState.opModes.put(code, mode);
                }
                scheduleWriteLocked();
            }
        }
        int i++;
        throw th;
    }

    /* JADX WARNING: Missing block: B:27:0x008e, code:
            if (r10 == null) goto L_?;
     */
    /* JADX WARNING: Missing block: B:28:0x0090, code:
            r8 = android.os.Binder.clearCallingIdentity();
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:31:0x0099, code:
            if (r6 >= r10.size()) goto L_0x00b2;
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            ((com.android.server.AppOpsService.Callback) r10.get(r6)).mCallback.opChanged(r19, r20, r21);
     */
    /* JADX WARNING: Missing block: B:39:0x00b7, code:
            android.os.Binder.restoreCallingIdentity(r8);
     */
    /* JADX WARNING: Missing block: B:51:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setMode(int code, int uid, String packageName, int mode) {
        Throwable th;
        if (Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        }
        verifyIncomingOp(code);
        ArrayList<Callback> repCbs = null;
        code = AppOpsManager.opToSwitch(code);
        synchronized (this) {
            try {
                UidState uidState = getUidStateLocked(uid, false);
                Op op = getOpLocked(code, uid, packageName, true);
                if (!(op == null || op.mode == mode)) {
                    ArrayList<Callback> repCbs2;
                    op.mode = mode;
                    ArraySet<Callback> cbs = (ArraySet) this.mOpModeWatchers.get(code);
                    if (cbs != null) {
                        repCbs2 = new ArrayList();
                        try {
                            repCbs2.addAll(cbs);
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                    repCbs2 = null;
                    cbs = (ArraySet) this.mPackageModeWatchers.get(packageName);
                    if (cbs != null) {
                        if (repCbs2 == null) {
                            repCbs = new ArrayList();
                        } else {
                            repCbs = repCbs2;
                        }
                        repCbs.addAll(cbs);
                    } else {
                        repCbs = repCbs2;
                    }
                    if (mode == AppOpsManager.opToDefaultMode(op.op)) {
                        pruneOp(op, uid, packageName);
                    }
                    scheduleFastWriteLocked();
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        return;
        int i++;
    }

    private static HashMap<Callback, ArrayList<ChangeRec>> addCallbacks(HashMap<Callback, ArrayList<ChangeRec>> callbacks, int op, int uid, String packageName, ArraySet<Callback> cbs) {
        if (cbs == null) {
            return callbacks;
        }
        if (callbacks == null) {
            callbacks = new HashMap();
        }
        boolean duplicate = false;
        int N = cbs.size();
        for (int i = 0; i < N; i++) {
            Callback cb = (Callback) cbs.valueAt(i);
            ArrayList<ChangeRec> reports = (ArrayList) callbacks.get(cb);
            if (reports != null) {
                int reportCount = reports.size();
                for (int j = 0; j < reportCount; j++) {
                    ChangeRec report = (ChangeRec) reports.get(j);
                    if (report.op == op && report.pkg.equals(packageName)) {
                        duplicate = true;
                        break;
                    }
                }
            } else {
                reports = new ArrayList();
                callbacks.put(cb, reports);
            }
            if (!duplicate) {
                reports.add(new ChangeRec(op, uid, packageName));
            }
        }
        return callbacks;
    }

    public void resetAllModes(int reqUserId, String reqPackageName) {
        int i;
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        this.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", callingPid, callingUid, null);
        reqUserId = ActivityManager.handleIncomingUser(callingPid, callingUid, reqUserId, true, true, "resetAllModes", null);
        int reqUid = -1;
        if (reqPackageName != null) {
            try {
                reqUid = AppGlobals.getPackageManager().getPackageUid(reqPackageName, 8192, reqUserId);
            } catch (RemoteException e) {
            }
        }
        HashMap<Callback, ArrayList<ChangeRec>> callbacks = null;
        synchronized (this) {
            boolean changed = false;
            for (i = this.mUidStates.size() - 1; i >= 0; i--) {
                int j;
                String packageName;
                UidState uidState = (UidState) this.mUidStates.valueAt(i);
                SparseIntArray opModes = uidState.opModes;
                if (opModes != null && (uidState.uid == reqUid || reqUid == -1)) {
                    for (j = opModes.size() - 1; j >= 0; j--) {
                        int code = opModes.keyAt(j);
                        if (AppOpsManager.opAllowsReset(code)) {
                            opModes.removeAt(j);
                            if (opModes.size() <= 0) {
                                uidState.opModes = null;
                            }
                            for (String packageName2 : getPackagesForUid(uidState.uid)) {
                                callbacks = addCallbacks(addCallbacks(callbacks, code, uidState.uid, packageName2, (ArraySet) this.mOpModeWatchers.get(code)), code, uidState.uid, packageName2, (ArraySet) this.mPackageModeWatchers.get(packageName2));
                            }
                        }
                    }
                }
                if (uidState.pkgOps != null && (reqUserId == -1 || reqUserId == UserHandle.getUserId(uidState.uid))) {
                    Iterator<Entry<String, Ops>> it = uidState.pkgOps.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<String, Ops> ent = (Entry) it.next();
                        packageName2 = (String) ent.getKey();
                        if (reqPackageName == null || (reqPackageName.equals(packageName2) ^ 1) == 0) {
                            Ops pkgOps = (Ops) ent.getValue();
                            for (j = pkgOps.size() - 1; j >= 0; j--) {
                                Op curOp = (Op) pkgOps.valueAt(j);
                                if (AppOpsManager.opAllowsReset(curOp.op) && curOp.mode != AppOpsManager.opToDefaultMode(curOp.op)) {
                                    curOp.mode = AppOpsManager.opToDefaultMode(curOp.op);
                                    changed = true;
                                    callbacks = addCallbacks(addCallbacks(callbacks, curOp.op, curOp.uid, packageName2, (ArraySet) this.mOpModeWatchers.get(curOp.op)), curOp.op, curOp.uid, packageName2, (ArraySet) this.mPackageModeWatchers.get(packageName2));
                                    if (curOp.time == 0 && curOp.rejectTime == 0) {
                                        pkgOps.removeAt(j);
                                    }
                                }
                            }
                            if (pkgOps.size() == 0) {
                                it.remove();
                            }
                        }
                    }
                    if (uidState.isDefault()) {
                        this.mUidStates.remove(uidState.uid);
                    }
                }
            }
            if (changed) {
                scheduleFastWriteLocked();
            }
        }
        if (callbacks != null) {
            for (Entry<Callback, ArrayList<ChangeRec>> ent2 : callbacks.entrySet()) {
                Callback cb = (Callback) ent2.getKey();
                ArrayList<ChangeRec> reports = (ArrayList) ent2.getValue();
                for (i = 0; i < reports.size(); i++) {
                    ChangeRec rep = (ChangeRec) reports.get(i);
                    try {
                        cb.mCallback.opChanged(rep.op, rep.uid, rep.pkg);
                    } catch (RemoteException e2) {
                    }
                }
            }
        }
    }

    public void startWatchingMode(int op, String packageName, IAppOpsCallback callback) {
        if (callback != null) {
            synchronized (this) {
                ArraySet<Callback> cbs;
                if (op != -1) {
                    op = AppOpsManager.opToSwitch(op);
                }
                Callback cb = (Callback) this.mModeWatchers.get(callback.asBinder());
                if (cb == null) {
                    cb = new Callback(callback);
                    this.mModeWatchers.put(callback.asBinder(), cb);
                }
                if (this.mAppOpsResource == null) {
                    this.mAppOpsResource = HwFrameworkFactory.getHwResource(14);
                }
                if (op != -1) {
                    cbs = (ArraySet) this.mOpModeWatchers.get(op);
                    if (cbs == null) {
                        cbs = new ArraySet();
                        this.mOpModeWatchers.put(op, cbs);
                    }
                    cbs.add(cb);
                }
                if (packageName != null) {
                    cbs = (ArraySet) this.mPackageModeWatchers.get(packageName);
                    if (cbs == null) {
                        cbs = new ArraySet();
                        this.mPackageModeWatchers.put(packageName, cbs);
                    }
                    cbs.add(cb);
                    if (this.mAppOpsResource != null) {
                        this.mAppOpsResource.acquire(Binder.getCallingUid(), packageName, 0, cbs.size());
                    }
                }
            }
        }
    }

    public void stopWatchingMode(IAppOpsCallback callback) {
        if (callback != null) {
            synchronized (this) {
                Callback cb = (Callback) this.mModeWatchers.remove(callback.asBinder());
                if (cb != null) {
                    int i;
                    ArraySet<Callback> cbs;
                    cb.unlinkToDeath();
                    for (i = this.mOpModeWatchers.size() - 1; i >= 0; i--) {
                        cbs = (ArraySet) this.mOpModeWatchers.valueAt(i);
                        cbs.remove(cb);
                        if (cbs.size() <= 0) {
                            this.mOpModeWatchers.removeAt(i);
                        }
                    }
                    for (i = this.mPackageModeWatchers.size() - 1; i >= 0; i--) {
                        cbs = (ArraySet) this.mPackageModeWatchers.valueAt(i);
                        cbs.remove(cb);
                        if (cbs.size() <= 0) {
                            this.mPackageModeWatchers.removeAt(i);
                        }
                    }
                }
            }
        }
    }

    public IBinder getToken(IBinder clientToken) {
        ClientState cs;
        synchronized (this) {
            cs = (ClientState) this.mClients.get(clientToken);
            if (cs == null) {
                cs = new ClientState(clientToken);
                this.mClients.put(clientToken, cs);
            }
        }
        return cs;
    }

    public int checkOperation(int code, int uid, String packageName) {
        verifyIncomingUid(uid);
        verifyIncomingOp(code);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName == null) {
            return 1;
        }
        synchronized (this) {
            if (isOpRestrictedLocked(uid, code, resolvedPackageName)) {
                return 1;
            }
            code = AppOpsManager.opToSwitch(code);
            UidState uidState = getUidStateLocked(uid, false);
            int opToDefaultMode;
            if (uidState == null || uidState.opModes == null || uidState.opModes.indexOfKey(code) < 0) {
                Op op = getOpLocked(code, uid, resolvedPackageName, false);
                if (op == null) {
                    opToDefaultMode = AppOpsManager.opToDefaultMode(code);
                    return opToDefaultMode;
                }
                opToDefaultMode = op.mode;
                return opToDefaultMode;
            }
            opToDefaultMode = uidState.opModes.get(code);
            return opToDefaultMode;
        }
    }

    public int checkAudioOperation(int code, int usage, int uid, String packageName) {
        boolean suspended;
        try {
            suspended = isPackageSuspendedForUser(packageName, uid);
        } catch (IllegalArgumentException e) {
            suspended = false;
        }
        if (suspended) {
            Log.i(TAG, "Audio disabled for suspended package=" + packageName + " for uid=" + uid);
            return 1;
        }
        synchronized (this) {
            int mode = checkRestrictionLocked(code, usage, uid, packageName);
            if (mode != 0) {
                return mode;
            }
            return checkOperation(code, uid, packageName);
        }
    }

    private boolean isPackageSuspendedForUser(String pkg, int uid) {
        try {
            return AppGlobals.getPackageManager().isPackageSuspendedForUser(pkg, UserHandle.getUserId(uid));
        } catch (RemoteException e) {
            throw new SecurityException("Could not talk to package manager service");
        }
    }

    private int checkRestrictionLocked(int code, int usage, int uid, String packageName) {
        SparseArray<Restriction> usageRestrictions = (SparseArray) this.mAudioRestrictions.get(code);
        if (usageRestrictions != null) {
            Restriction r = (Restriction) usageRestrictions.get(usage);
            if (!(r == null || (r.exceptionPackages.contains(packageName) ^ 1) == 0)) {
                return r.mode;
            }
        }
        return 0;
    }

    public void setAudioRestriction(int code, int usage, int uid, int mode, String[] exceptionPackages) {
        verifyIncomingUid(uid);
        verifyIncomingOp(code);
        synchronized (this) {
            SparseArray<Restriction> usageRestrictions = (SparseArray) this.mAudioRestrictions.get(code);
            if (usageRestrictions == null) {
                usageRestrictions = new SparseArray();
                this.mAudioRestrictions.put(code, usageRestrictions);
            }
            usageRestrictions.remove(usage);
            if (mode != 0) {
                Restriction r = new Restriction();
                r.mode = mode;
                if (exceptionPackages != null) {
                    r.exceptionPackages = new ArraySet(N);
                    for (String pkg : exceptionPackages) {
                        if (pkg != null) {
                            r.exceptionPackages.add(pkg.trim());
                        }
                    }
                }
                usageRestrictions.put(usage, r);
            }
        }
        notifyWatchersOfChange(code);
    }

    public int checkPackage(int uid, String packageName) {
        Preconditions.checkNotNull(packageName);
        synchronized (this) {
            if (getOpsRawLocked(uid, packageName, true) != null) {
                return 0;
            }
            return 2;
        }
    }

    public int noteProxyOperation(int code, String proxyPackageName, int proxiedUid, String proxiedPackageName) {
        verifyIncomingOp(code);
        int proxyUid = Binder.getCallingUid();
        String resolveProxyPackageName = resolvePackageName(proxyUid, proxyPackageName);
        if (resolveProxyPackageName == null) {
            return 1;
        }
        int proxyMode = noteOperationUnchecked(code, proxyUid, resolveProxyPackageName, -1, null);
        if (proxyMode != 0 || Binder.getCallingUid() == proxiedUid) {
            return proxyMode;
        }
        String resolveProxiedPackageName = resolvePackageName(proxiedUid, proxiedPackageName);
        if (resolveProxiedPackageName == null) {
            return 1;
        }
        return noteOperationUnchecked(code, proxiedUid, resolveProxiedPackageName, proxyMode, resolveProxyPackageName);
    }

    public int noteOperation(int code, int uid, String packageName) {
        verifyIncomingUid(uid);
        verifyIncomingOp(code);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName == null) {
            return 1;
        }
        return noteOperationUnchecked(code, uid, resolvedPackageName, 0, null);
    }

    private int noteOperationUnchecked(int code, int uid, String packageName, int proxyUid, String proxyPackageName) {
        synchronized (this) {
            Ops ops = getOpsRawLocked(uid, packageName, true);
            if (ops == null) {
                return 2;
            }
            Op op = getOpLocked(ops, code, true);
            if (isOpRestrictedLocked(uid, code, packageName)) {
                return 1;
            }
            if (op.duration == -1) {
                Slog.w(TAG, "Noting op not finished: uid " + uid + " pkg " + packageName + " code " + code + " time=" + op.time + " duration=" + op.duration);
            }
            op.duration = 0;
            int switchCode = AppOpsManager.opToSwitch(code);
            UidState uidState = ops.uidState;
            if (uidState.opModes == null || uidState.opModes.indexOfKey(switchCode) < 0) {
                Op switchOp = switchCode != code ? getOpLocked(ops, switchCode, true) : op;
                if (switchOp.mode != 0) {
                    op.rejectTime = System.currentTimeMillis();
                    int i = switchOp.mode;
                    return i;
                }
            }
            int uidMode = uidState.opModes.get(switchCode);
            if (uidMode != 0) {
                op.rejectTime = System.currentTimeMillis();
                return uidMode;
            }
            op.time = System.currentTimeMillis();
            op.rejectTime = 0;
            op.proxyUid = proxyUid;
            op.proxyPackageName = proxyPackageName;
            return 0;
        }
    }

    /* JADX WARNING: Missing block: B:44:0x007c, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int startOperation(IBinder token, int code, int uid, String packageName) {
        verifyIncomingUid(uid);
        verifyIncomingOp(code);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName == null) {
            return 1;
        }
        ClientState client = (ClientState) token;
        synchronized (this) {
            Ops ops = getOpsRawLocked(uid, resolvedPackageName, true);
            if (ops == null) {
                return 2;
            }
            Op op = getOpLocked(ops, code, true);
            if (isOpRestrictedLocked(uid, code, resolvedPackageName)) {
                return 1;
            }
            int switchCode = AppOpsManager.opToSwitch(code);
            UidState uidState = ops.uidState;
            if (uidState.opModes != null) {
                int uidMode = uidState.opModes.get(switchCode);
                if (uidMode != 0) {
                    op.rejectTime = System.currentTimeMillis();
                    return uidMode;
                }
            }
            Op switchOp = switchCode != code ? getOpLocked(ops, switchCode, true) : op;
            if (switchOp.mode != 0) {
                op.rejectTime = System.currentTimeMillis();
                int i = switchOp.mode;
                return i;
            }
            if (op.nesting == 0) {
                op.time = System.currentTimeMillis();
                op.rejectTime = 0;
                op.duration = -1;
            }
            op.nesting++;
            if (client.mStartedOps != null) {
                client.mStartedOps.add(op);
            }
        }
    }

    public void finishOperation(IBinder token, int code, int uid, String packageName) {
        verifyIncomingUid(uid);
        verifyIncomingOp(code);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName != null && (token instanceof ClientState)) {
            ClientState client = (ClientState) token;
            synchronized (this) {
                Op op = getOpLocked(code, uid, resolvedPackageName, true);
                if (op == null) {
                } else if (client.mStartedOps == null || client.mStartedOps.remove(op)) {
                    finishOperationLocked(op);
                } else {
                    throw new IllegalStateException("Operation not started: uid" + op.uid + " pkg=" + op.packageName + " op=" + op.op);
                }
            }
        }
    }

    public int permissionToOpCode(String permission) {
        if (permission == null) {
            return -1;
        }
        return AppOpsManager.permissionToOpCode(permission);
    }

    void finishOperationLocked(Op op) {
        if (op.nesting <= 1) {
            if (op.nesting == 1) {
                op.duration = (int) (System.currentTimeMillis() - op.time);
                op.time += (long) op.duration;
            } else {
                Slog.w(TAG, "Finishing op nesting under-run: uid " + op.uid + " pkg " + op.packageName + " code " + op.op + " time=" + op.time + " duration=" + op.duration + " nesting=" + op.nesting);
            }
            op.nesting = 0;
            return;
        }
        op.nesting--;
    }

    private void verifyIncomingUid(int uid) {
        if (uid != Binder.getCallingUid() && Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        }
    }

    private void verifyIncomingOp(int op) {
        if (op < 0 || op >= 70) {
            throw new IllegalArgumentException("Bad operation #" + op);
        }
    }

    private UidState getUidStateLocked(int uid, boolean edit) {
        UidState uidState = (UidState) this.mUidStates.get(uid);
        if (uidState == null) {
            if (!edit) {
                return null;
            }
            uidState = new UidState(uid);
            this.mUidStates.put(uid, uidState);
        }
        return uidState;
    }

    private Ops getOpsLocked(int uid, String packageName, boolean edit) {
        if (uid == 0) {
            packageName = "root";
        } else if (uid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
            packageName = "com.android.shell";
        }
        return getOpsRawLocked(uid, packageName, edit);
    }

    private Ops syncPrivState(int uid, String packageName, Ops ops) {
        boolean isPrivileged = false;
        boolean needSyncPriv = false;
        if (ops.needSyncPriv && uid != 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                ApplicationInfo appInfo = ActivityThread.getPackageManager().getApplicationInfo(packageName, 0, UserHandle.getUserId(uid));
                if (appInfo != null) {
                    int pkgUid = appInfo.uid;
                    isPrivileged = (appInfo.privateFlags & 8) != 0;
                } else if ("media".equals(packageName)) {
                    isPrivileged = false;
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Could not contact PackageManager", e);
                needSyncPriv = true;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
            Binder.restoreCallingIdentity(ident);
            ops.needSyncPriv = needSyncPriv;
            ops.isPrivileged = isPrivileged;
        }
        return ops;
    }

    private Ops getOpsRawLocked(int uid, String packageName, boolean edit) {
        UidState uidState = getUidStateLocked(uid, edit);
        if (uidState == null) {
            return null;
        }
        if (uidState.pkgOps == null) {
            if (!edit) {
                return null;
            }
            uidState.pkgOps = new ArrayMap();
        }
        Ops ops = (Ops) uidState.pkgOps.get(packageName);
        if (ops == null) {
            if (!edit) {
                return null;
            }
            boolean isPrivileged = false;
            boolean needSyncPriv = false;
            if (uid != 0) {
                long ident = Binder.clearCallingIdentity();
                int pkgUid = -1;
                try {
                    ApplicationInfo appInfo = ActivityThread.getPackageManager().getApplicationInfo(packageName, 268435456, UserHandle.getUserId(uid));
                    if (appInfo != null) {
                        pkgUid = appInfo.uid;
                        isPrivileged = (appInfo.privateFlags & 8) != 0;
                    } else if ("media".equals(packageName)) {
                        pkgUid = 1013;
                        isPrivileged = false;
                    } else if ("audioserver".equals(packageName)) {
                        pkgUid = 1041;
                        isPrivileged = false;
                    } else if ("cameraserver".equals(packageName)) {
                        pkgUid = 1047;
                        isPrivileged = false;
                    }
                } catch (RemoteException e) {
                    Slog.w(TAG, "Could not contact PackageManager", e);
                    needSyncPriv = true;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
                if (pkgUid != uid) {
                    new RuntimeException("here").fillInStackTrace();
                    Slog.w(TAG, "Bad call: specified package " + packageName + " under uid " + uid + " but it is really " + pkgUid);
                    Binder.restoreCallingIdentity(ident);
                    return null;
                }
                Binder.restoreCallingIdentity(ident);
            }
            ops = new Ops(packageName, uidState, isPrivileged, needSyncPriv);
            uidState.pkgOps.put(packageName, ops);
        } else if (ops.needSyncPriv) {
            syncPrivState(uid, packageName, ops);
        }
        return ops;
    }

    private void scheduleWriteLocked() {
        if (!this.mWriteScheduled) {
            this.mWriteScheduled = true;
            this.mHandler.postDelayed(this.mWriteRunner, 1800000);
        }
    }

    private void scheduleFastWriteLocked() {
        if (!this.mFastWriteScheduled) {
            this.mWriteScheduled = true;
            this.mFastWriteScheduled = true;
            this.mHandler.removeCallbacks(this.mWriteRunner);
            this.mHandler.postDelayed(this.mWriteRunner, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
    }

    private Op getOpLocked(int code, int uid, String packageName, boolean edit) {
        Ops ops = getOpsRawLocked(uid, packageName, edit);
        if (ops == null) {
            return null;
        }
        return getOpLocked(ops, code, edit);
    }

    private Op getOpLocked(Ops ops, int code, boolean edit) {
        Op op = (Op) ops.get(code);
        if (op == null) {
            if (!edit) {
                return null;
            }
            op = new Op(ops.uidState.uid, ops.packageName, code);
            ops.put(code, op);
        }
        if (edit) {
            scheduleWriteLocked();
        }
        return op;
    }

    private boolean isOpRestrictedLocked(int uid, int code, String packageName) {
        int userHandle = UserHandle.getUserId(uid);
        int restrictionSetCount = this.mOpUserRestrictions.size();
        int i = 0;
        while (i < restrictionSetCount) {
            ClientRestrictionState restrictionState = (ClientRestrictionState) this.mOpUserRestrictions.valueAt(i);
            if (restrictionState == null || !restrictionState.hasRestriction(code, packageName, userHandle)) {
                i++;
            } else {
                if (AppOpsManager.opAllowSystemBypassRestriction(code)) {
                    synchronized (this) {
                        Ops ops = getOpsRawLocked(uid, packageName, true);
                        if (ops == null || !ops.isPrivileged) {
                        } else {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    void readState() {
        synchronized (this.mFile) {
            synchronized (this) {
                try {
                    FileInputStream stream = this.mFile.openRead();
                    this.mUidStates.clear();
                    try {
                        int type;
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(stream, StandardCharsets.UTF_8.name());
                        do {
                            type = parser.next();
                            if (type == 2) {
                                break;
                            }
                        } while (type != 1);
                        if (type != 2) {
                            throw new IllegalStateException("no start tag found");
                        }
                        int outerDepth = parser.getDepth();
                        while (true) {
                            type = parser.next();
                            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                                if (!(type == 3 || type == 4)) {
                                    String tagName = parser.getName();
                                    if (tagName.equals(AbsLocationManagerService.DEL_PKG)) {
                                        readPackage(parser);
                                    } else if (tagName.equals("uid")) {
                                        readUidOps(parser);
                                    } else {
                                        Slog.w(TAG, "Unknown element under <app-ops>: " + parser.getName());
                                        XmlUtils.skipCurrentTag(parser);
                                    }
                                }
                            }
                        }
                        if (!true) {
                            this.mUidStates.clear();
                        }
                        try {
                            stream.close();
                        } catch (IOException e) {
                        }
                    } catch (IllegalStateException e2) {
                        Slog.w(TAG, "Failed parsing " + e2);
                        if (null == null) {
                            this.mUidStates.clear();
                        }
                        try {
                            stream.close();
                        } catch (IOException e3) {
                        }
                    } catch (NullPointerException e4) {
                        Slog.w(TAG, "Failed parsing " + e4);
                        if (null == null) {
                            this.mUidStates.clear();
                        }
                        try {
                            stream.close();
                        } catch (IOException e5) {
                        }
                    } catch (NumberFormatException e6) {
                        Slog.w(TAG, "Failed parsing " + e6);
                        if (null == null) {
                            this.mUidStates.clear();
                        }
                        try {
                            stream.close();
                        } catch (IOException e7) {
                        }
                    } catch (XmlPullParserException e8) {
                        Slog.w(TAG, "Failed parsing " + e8);
                        if (null == null) {
                            this.mUidStates.clear();
                        }
                        try {
                            stream.close();
                        } catch (IOException e9) {
                        }
                    } catch (IOException e10) {
                        Slog.w(TAG, "Failed parsing " + e10);
                        if (null == null) {
                            this.mUidStates.clear();
                        }
                        try {
                            stream.close();
                        } catch (IOException e11) {
                        }
                    } catch (IndexOutOfBoundsException e12) {
                        Slog.w(TAG, "Failed parsing " + e12);
                        if (null == null) {
                            this.mUidStates.clear();
                        }
                        try {
                            stream.close();
                        } catch (IOException e13) {
                        }
                    } catch (Throwable th) {
                        if (null == null) {
                            this.mUidStates.clear();
                        }
                        try {
                            stream.close();
                        } catch (IOException e14) {
                        }
                    }
                } catch (FileNotFoundException e15) {
                    Slog.i(TAG, "No existing app ops " + this.mFile.getBaseFile() + "; starting empty");
                    return;
                }
            }
        }
        return;
    }

    void readUidOps(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        int uid = Integer.parseInt(parser.getAttributeValue(null, "n"));
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("op")) {
                    int code = Integer.parseInt(parser.getAttributeValue(null, "n"));
                    int mode = Integer.parseInt(parser.getAttributeValue(null, "m"));
                    UidState uidState = getUidStateLocked(uid, true);
                    if (uidState.opModes == null) {
                        uidState.opModes = new SparseIntArray();
                    }
                    uidState.opModes.put(code, mode);
                } else {
                    Slog.w(TAG, "Unknown element under <uid-ops>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    void readPackage(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        String pkgName = parser.getAttributeValue(null, "n");
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("uid")) {
                    readUid(parser, pkgName);
                } else {
                    Slog.w(TAG, "Unknown element under <pkg>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    void readUid(XmlPullParser parser, String pkgName) throws NumberFormatException, XmlPullParserException, IOException {
        int uid = Integer.parseInt(parser.getAttributeValue(null, "n"));
        String isPrivilegedString = parser.getAttributeValue(null, "p");
        boolean isPrivileged = false;
        boolean needSyncPriv = false;
        if (isPrivilegedString == null) {
            try {
                if (ActivityThread.getPackageManager() != null) {
                    ApplicationInfo appInfo = ActivityThread.getPackageManager().getApplicationInfo(pkgName, 0, UserHandle.getUserId(uid));
                    if (appInfo != null) {
                        isPrivileged = (appInfo.privateFlags & 8) != 0;
                    }
                } else {
                    Slog.i(TAG, "error readUid pkgName:" + pkgName + " uid:" + uid);
                    needSyncPriv = true;
                    isPrivileged = false;
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Could not contact PackageManager", e);
                needSyncPriv = true;
            }
        } else {
            isPrivileged = Boolean.parseBoolean(isPrivilegedString);
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("op")) {
                    Op op = new Op(uid, pkgName, Integer.parseInt(parser.getAttributeValue(null, "n")));
                    String mode = parser.getAttributeValue(null, "m");
                    if (mode != null) {
                        op.mode = Integer.parseInt(mode);
                    }
                    String time = parser.getAttributeValue(null, "t");
                    if (time != null) {
                        op.time = Long.parseLong(time);
                    }
                    time = parser.getAttributeValue(null, "r");
                    if (time != null) {
                        op.rejectTime = Long.parseLong(time);
                    }
                    String dur = parser.getAttributeValue(null, "d");
                    if (dur != null) {
                        op.duration = Integer.parseInt(dur);
                    }
                    String proxyUid = parser.getAttributeValue(null, "pu");
                    if (proxyUid != null) {
                        op.proxyUid = Integer.parseInt(proxyUid);
                    }
                    String proxyPackageName = parser.getAttributeValue(null, "pp");
                    if (proxyPackageName != null) {
                        op.proxyPackageName = proxyPackageName;
                    }
                    UidState uidState = getUidStateLocked(uid, true);
                    if (uidState.pkgOps == null) {
                        uidState.pkgOps = new ArrayMap();
                    }
                    Ops ops = (Ops) uidState.pkgOps.get(pkgName);
                    if (ops == null) {
                        ops = new Ops(pkgName, uidState, isPrivileged, needSyncPriv);
                        uidState.pkgOps.put(pkgName, ops);
                    }
                    ops.put(op.op, op);
                } else {
                    Slog.w(TAG, "Unknown element under <pkg>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    void writeState() {
        synchronized (this.mFile) {
            List<PackageOps> allOps = getPackagesForOps(null);
            try {
                OutputStream stream = this.mFile.startWrite();
                try {
                    int i;
                    int j;
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(stream, StandardCharsets.UTF_8.name());
                    out.startDocument(null, Boolean.valueOf(true));
                    out.startTag(null, "app-ops");
                    int uidStateCount = this.mUidStates.size();
                    for (i = 0; i < uidStateCount; i++) {
                        UidState uidState = (UidState) this.mUidStates.valueAt(i);
                        if (uidState.opModes != null && uidState.opModes.size() > 0) {
                            out.startTag(null, "uid");
                            out.attribute(null, "n", Integer.toString(uidState.uid));
                            SparseIntArray uidOpModes = uidState.opModes;
                            int opCount = uidOpModes.size();
                            for (j = 0; j < opCount; j++) {
                                int op = uidOpModes.keyAt(j);
                                int mode = uidOpModes.valueAt(j);
                                out.startTag(null, "op");
                                out.attribute(null, "n", Integer.toString(op));
                                out.attribute(null, "m", Integer.toString(mode));
                                out.endTag(null, "op");
                            }
                            out.endTag(null, "uid");
                        }
                    }
                    if (allOps != null) {
                        Object lastPkg = null;
                        for (i = 0; i < allOps.size(); i++) {
                            PackageOps pkg = (PackageOps) allOps.get(i);
                            if (!(pkg == null || pkg.getPackageName() == null)) {
                                if (!pkg.getPackageName().equals(lastPkg)) {
                                    if (lastPkg != null) {
                                        out.endTag(null, AbsLocationManagerService.DEL_PKG);
                                    }
                                    lastPkg = pkg.getPackageName();
                                    out.startTag(null, AbsLocationManagerService.DEL_PKG);
                                    out.attribute(null, "n", lastPkg);
                                }
                                out.startTag(null, "uid");
                                out.attribute(null, "n", Integer.toString(pkg.getUid()));
                                synchronized (this) {
                                    Ops ops = getOpsRawLocked(pkg.getUid(), pkg.getPackageName(), false);
                                    if (ops == null) {
                                        out.attribute(null, "p", Boolean.toString(false));
                                    } else if (!ops.needSyncPriv) {
                                        out.attribute(null, "p", Boolean.toString(ops.isPrivileged));
                                    }
                                }
                                List<OpEntry> ops2 = pkg.getOps();
                                for (j = 0; j < ops2.size(); j++) {
                                    OpEntry op2 = (OpEntry) ops2.get(j);
                                    out.startTag(null, "op");
                                    out.attribute(null, "n", Integer.toString(op2.getOp()));
                                    if (op2.getMode() != AppOpsManager.opToDefaultMode(op2.getOp())) {
                                        out.attribute(null, "m", Integer.toString(op2.getMode()));
                                    }
                                    long time = op2.getTime();
                                    if (time != 0) {
                                        out.attribute(null, "t", Long.toString(time));
                                    }
                                    time = op2.getRejectTime();
                                    if (time != 0) {
                                        out.attribute(null, "r", Long.toString(time));
                                    }
                                    int dur = op2.getDuration();
                                    if (dur != 0) {
                                        out.attribute(null, "d", Integer.toString(dur));
                                    }
                                    int proxyUid = op2.getProxyUid();
                                    if (proxyUid != -1) {
                                        out.attribute(null, "pu", Integer.toString(proxyUid));
                                    }
                                    String proxyPackageName = op2.getProxyPackageName();
                                    if (proxyPackageName != null) {
                                        out.attribute(null, "pp", proxyPackageName);
                                    }
                                    out.endTag(null, "op");
                                }
                                out.endTag(null, "uid");
                            }
                        }
                        if (lastPkg != null) {
                            out.endTag(null, AbsLocationManagerService.DEL_PKG);
                        }
                    }
                    out.endTag(null, "app-ops");
                    out.endDocument();
                    this.mFile.finishWrite(stream);
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to write state, restoring backup.", e);
                    this.mFile.failWrite(stream);
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Failed to write state: " + e2);
                return;
            }
        }
        return;
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new Shell(this, this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    static void dumpCommandHelp(PrintWriter pw) {
        pw.println("AppOps service (appops) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  set [--user <USER_ID>] <PACKAGE | UID> <OP> <MODE>");
        pw.println("    Set the mode for a particular application and operation.");
        pw.println("  get [--user <USER_ID>] <PACKAGE | UID> [<OP>]");
        pw.println("    Return the mode for a particular application and optional operation.");
        pw.println("  query-op [--user <USER_ID>] <OP> [<MODE>]");
        pw.println("    Print all packages that currently have the given op in the given mode.");
        pw.println("  reset [--user <USER_ID>] [<PACKAGE>]");
        pw.println("    Reset the given application or all applications to default modes.");
        pw.println("  write-settings");
        pw.println("    Immediately write pending changes to storage.");
        pw.println("  read-settings");
        pw.println("    Read the last written settings, replacing current state in RAM.");
        pw.println("  options:");
        pw.println("    <PACKAGE> an Android package name.");
        pw.println("    <OP>      an AppOps operation.");
        pw.println("    <MODE>    one of allow, ignore, deny, or default");
        pw.println("    <USER_ID> the user id under which the package is installed. If --user is not");
        pw.println("              specified, the current user is assumed.");
    }

    static int onShellCommand(Shell shell, String cmd) {
        long token;
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        PrintWriter err = shell.getErrPrintWriter();
        try {
            int res;
            List<PackageOps> ops;
            int i;
            List<OpEntry> entries;
            int j;
            OpEntry ent;
            if (cmd.equals("set")) {
                res = shell.parseUserPackageOp(true, err);
                if (res < 0) {
                    return res;
                }
                String modeStr = shell.getNextArg();
                if (modeStr == null) {
                    err.println("Error: Mode not specified.");
                    return -1;
                }
                int mode = shell.strModeToMode(modeStr, err);
                if (mode < 0) {
                    return -1;
                }
                if (shell.packageName != null) {
                    shell.mInterface.setMode(shell.op, shell.packageUid, shell.packageName, mode);
                } else {
                    shell.mInterface.setUidMode(shell.op, shell.nonpackageUid, mode);
                }
                return 0;
            } else if (cmd.equals("get")) {
                res = shell.parseUserPackageOp(false, err);
                if (res < 0) {
                    return res;
                }
                if (shell.packageName != null) {
                    ops = shell.mInterface.getOpsForPackage(shell.packageUid, shell.packageName, shell.op != -1 ? new int[]{shell.op} : null);
                } else {
                    ops = shell.mInterface.getUidOps(shell.nonpackageUid, shell.op != -1 ? new int[]{shell.op} : null);
                }
                if (ops == null || ops.size() <= 0) {
                    pw.println("No operations.");
                    return 0;
                }
                long now = System.currentTimeMillis();
                for (i = 0; i < ops.size(); i++) {
                    entries = ((PackageOps) ops.get(i)).getOps();
                    for (j = 0; j < entries.size(); j++) {
                        ent = (OpEntry) entries.get(j);
                        pw.print(AppOpsManager.opToName(ent.getOp()));
                        pw.print(": ");
                        switch (ent.getMode()) {
                            case 0:
                                pw.print("allow");
                                break;
                            case 1:
                                pw.print("ignore");
                                break;
                            case 2:
                                pw.print("deny");
                                break;
                            case 3:
                                pw.print("default");
                                break;
                            default:
                                pw.print("mode=");
                                pw.print(ent.getMode());
                                break;
                        }
                        if (ent.getTime() != 0) {
                            pw.print("; time=");
                            TimeUtils.formatDuration(now - ent.getTime(), pw);
                            pw.print(" ago");
                        }
                        if (ent.getRejectTime() != 0) {
                            pw.print("; rejectTime=");
                            TimeUtils.formatDuration(now - ent.getRejectTime(), pw);
                            pw.print(" ago");
                        }
                        if (ent.getDuration() == -1) {
                            pw.print(" (running)");
                        } else if (ent.getDuration() != 0) {
                            pw.print("; duration=");
                            TimeUtils.formatDuration((long) ent.getDuration(), pw);
                        }
                        pw.println();
                    }
                }
                return 0;
            } else if (cmd.equals("query-op")) {
                res = shell.parseUserOpMode(1, err);
                if (res < 0) {
                    return res;
                }
                ops = shell.mInterface.getPackagesForOps(new int[]{shell.op});
                if (ops == null || ops.size() <= 0) {
                    pw.println("No operations.");
                    return 0;
                }
                for (i = 0; i < ops.size(); i++) {
                    PackageOps pkg = (PackageOps) ops.get(i);
                    boolean hasMatch = false;
                    entries = ((PackageOps) ops.get(i)).getOps();
                    for (j = 0; j < entries.size(); j++) {
                        ent = (OpEntry) entries.get(j);
                        if (ent.getOp() == shell.op && ent.getMode() == shell.mode) {
                            hasMatch = true;
                            break;
                        }
                    }
                    if (hasMatch) {
                        pw.println(pkg.getPackageName());
                    }
                }
                return 0;
            } else if (cmd.equals("reset")) {
                String packageName = null;
                int userId = -2;
                while (true) {
                    String argument = shell.getNextArg();
                    if (argument == null) {
                        if (userId == -2) {
                            userId = ActivityManager.getCurrentUser();
                        }
                        shell.mInterface.resetAllModes(userId, packageName);
                        pw.print("Reset all modes for: ");
                        if (userId == -1) {
                            pw.print("all users");
                        } else {
                            pw.print("user ");
                            pw.print(userId);
                        }
                        pw.print(", ");
                        if (packageName == null) {
                            pw.println("all packages");
                        } else {
                            pw.print("package ");
                            pw.println(packageName);
                        }
                        return 0;
                    } else if ("--user".equals(argument)) {
                        userId = UserHandle.parseUserArg(shell.getNextArgRequired());
                    } else if (packageName == null) {
                        packageName = argument;
                    } else {
                        err.println("Error: Unsupported argument: " + argument);
                        return -1;
                    }
                }
            } else if (cmd.equals("write-settings")) {
                shell.mInternal.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
                token = Binder.clearCallingIdentity();
                try {
                    synchronized (shell.mInternal) {
                        shell.mInternal.mHandler.removeCallbacks(shell.mInternal.mWriteRunner);
                    }
                    shell.mInternal.writeState();
                    pw.println("Current settings written.");
                    return 0;
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else if (!cmd.equals("read-settings")) {
                return shell.handleDefaultCommands(cmd);
            } else {
                shell.mInternal.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
                token = Binder.clearCallingIdentity();
                shell.mInternal.readState();
                pw.println("Last settings read.");
                Binder.restoreCallingIdentity(token);
                return 0;
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void dumpHelp(PrintWriter pw) {
        pw.println("AppOps service (appops) dump options:");
        pw.println("  none");
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, pw)) {
            int i;
            if (args != null) {
                i = 0;
                while (i < args.length) {
                    String arg = args[i];
                    if ("-h".equals(arg)) {
                        dumpHelp(pw);
                        return;
                    } else if ("-a".equals(arg)) {
                        i++;
                    } else if (arg.length() <= 0 || arg.charAt(0) != '-') {
                        pw.println("Unknown command: " + arg);
                        return;
                    } else {
                        pw.println("Unknown option: " + arg);
                        return;
                    }
                }
            }
            synchronized (this) {
                ArraySet<Callback> callbacks;
                int j;
                Op op;
                pw.println("Current AppOps Service state:");
                long now = System.currentTimeMillis();
                boolean needSep = false;
                if (this.mOpModeWatchers.size() > 0) {
                    needSep = true;
                    pw.println("  Op mode watchers:");
                    for (i = 0; i < this.mOpModeWatchers.size(); i++) {
                        pw.print("    Op ");
                        pw.print(AppOpsManager.opToName(this.mOpModeWatchers.keyAt(i)));
                        pw.println(":");
                        callbacks = (ArraySet) this.mOpModeWatchers.valueAt(i);
                        for (j = 0; j < callbacks.size(); j++) {
                            pw.print("      #");
                            pw.print(j);
                            pw.print(": ");
                            pw.println(callbacks.valueAt(j));
                        }
                    }
                }
                if (this.mPackageModeWatchers.size() > 0) {
                    needSep = true;
                    pw.println("  Package mode watchers:");
                    for (i = 0; i < this.mPackageModeWatchers.size(); i++) {
                        pw.print("    Pkg ");
                        pw.print((String) this.mPackageModeWatchers.keyAt(i));
                        pw.println(":");
                        callbacks = (ArraySet) this.mPackageModeWatchers.valueAt(i);
                        for (j = 0; j < callbacks.size(); j++) {
                            pw.print("      #");
                            pw.print(j);
                            pw.print(": ");
                            pw.println(callbacks.valueAt(j));
                        }
                    }
                }
                if (this.mModeWatchers.size() > 0) {
                    needSep = true;
                    pw.println("  All mode watchers:");
                    for (i = 0; i < this.mModeWatchers.size(); i++) {
                        pw.print("    ");
                        pw.print(this.mModeWatchers.keyAt(i));
                        pw.print(" -> ");
                        pw.println(this.mModeWatchers.valueAt(i));
                    }
                }
                if (this.mClients.size() > 0) {
                    needSep = true;
                    pw.println("  Clients:");
                    for (i = 0; i < this.mClients.size(); i++) {
                        pw.print("    ");
                        pw.print(this.mClients.keyAt(i));
                        pw.println(":");
                        ClientState cs = (ClientState) this.mClients.valueAt(i);
                        pw.print("      ");
                        pw.println(cs);
                        if (cs.mStartedOps != null && cs.mStartedOps.size() > 0) {
                            pw.println("      Started ops:");
                            for (j = 0; j < cs.mStartedOps.size(); j++) {
                                op = (Op) cs.mStartedOps.get(j);
                                pw.print("        ");
                                pw.print("uid=");
                                pw.print(op.uid);
                                pw.print(" pkg=");
                                pw.print(op.packageName);
                                pw.print(" op=");
                                pw.println(AppOpsManager.opToName(op.op));
                            }
                        }
                    }
                }
                if (this.mAudioRestrictions.size() > 0) {
                    boolean printedHeader = false;
                    for (int o = 0; o < this.mAudioRestrictions.size(); o++) {
                        String op2 = AppOpsManager.opToName(this.mAudioRestrictions.keyAt(o));
                        SparseArray<Restriction> restrictions = (SparseArray) this.mAudioRestrictions.valueAt(o);
                        for (i = 0; i < restrictions.size(); i++) {
                            if (!printedHeader) {
                                pw.println("  Audio Restrictions:");
                                printedHeader = true;
                                needSep = true;
                            }
                            int usage = restrictions.keyAt(i);
                            pw.print("    ");
                            pw.print(op2);
                            pw.print(" usage=");
                            pw.print(AudioAttributes.usageToString(usage));
                            Restriction r = (Restriction) restrictions.valueAt(i);
                            pw.print(": mode=");
                            pw.println(r.mode);
                            if (!r.exceptionPackages.isEmpty()) {
                                pw.println("      Exceptions:");
                                for (j = 0; j < r.exceptionPackages.size(); j++) {
                                    pw.print("        ");
                                    pw.println((String) r.exceptionPackages.valueAt(j));
                                }
                            }
                        }
                    }
                }
                if (needSep) {
                    pw.println();
                }
                for (i = 0; i < this.mUidStates.size(); i++) {
                    UidState uidState = (UidState) this.mUidStates.valueAt(i);
                    pw.print("  Uid ");
                    UserHandle.formatUid(pw, uidState.uid);
                    pw.println(":");
                    needSep = true;
                    SparseIntArray opModes = uidState.opModes;
                    if (opModes != null) {
                        int opModeCount = opModes.size();
                        for (j = 0; j < opModeCount; j++) {
                            int code = opModes.keyAt(j);
                            int mode = opModes.valueAt(j);
                            pw.print("      ");
                            pw.print(AppOpsManager.opToName(code));
                            pw.print(": mode=");
                            pw.println(mode);
                        }
                    }
                    ArrayMap<String, Ops> pkgOps = uidState.pkgOps;
                    if (pkgOps != null) {
                        for (Ops ops : pkgOps.values()) {
                            pw.print("    Package ");
                            pw.print(ops.packageName);
                            pw.println(" isPrivileged: ");
                            pw.print(ops.isPrivileged);
                            pw.println(" needSyncPriv:");
                            pw.print(ops.needSyncPriv);
                            pw.println(" :");
                            for (j = 0; j < ops.size(); j++) {
                                op = (Op) ops.valueAt(j);
                                pw.print("      ");
                                pw.print(AppOpsManager.opToName(op.op));
                                pw.print(": mode=");
                                pw.print(op.mode);
                                if (op.time != 0) {
                                    pw.print("; time=");
                                    TimeUtils.formatDuration(now - op.time, pw);
                                    pw.print(" ago");
                                }
                                if (op.rejectTime != 0) {
                                    pw.print("; rejectTime=");
                                    TimeUtils.formatDuration(now - op.rejectTime, pw);
                                    pw.print(" ago");
                                }
                                if (op.duration == -1) {
                                    pw.print(" (running)");
                                } else if (op.duration != 0) {
                                    pw.print("; duration=");
                                    TimeUtils.formatDuration((long) op.duration, pw);
                                }
                                pw.println();
                            }
                        }
                    }
                }
                if (needSep) {
                    pw.println();
                }
                int userRestrictionCount = this.mOpUserRestrictions.size();
                for (i = 0; i < userRestrictionCount; i++) {
                    int userId;
                    ClientRestrictionState restrictionState = (ClientRestrictionState) this.mOpUserRestrictions.valueAt(i);
                    pw.println("  User restrictions for token " + ((IBinder) this.mOpUserRestrictions.keyAt(i)) + ":");
                    int restrictionCount = restrictionState.perUserRestrictions != null ? restrictionState.perUserRestrictions.size() : 0;
                    if (restrictionCount > 0) {
                        pw.println("      Restricted ops:");
                        for (j = 0; j < restrictionCount; j++) {
                            userId = restrictionState.perUserRestrictions.keyAt(j);
                            boolean[] restrictedOps = (boolean[]) restrictionState.perUserRestrictions.valueAt(j);
                            if (restrictedOps != null) {
                                StringBuilder restrictedOpsValue = new StringBuilder();
                                restrictedOpsValue.append("[");
                                int restrictedOpCount = restrictedOps.length;
                                for (int k = 0; k < restrictedOpCount; k++) {
                                    if (restrictedOps[k]) {
                                        if (restrictedOpsValue.length() > 1) {
                                            restrictedOpsValue.append(", ");
                                        }
                                        restrictedOpsValue.append(AppOpsManager.opToName(k));
                                    }
                                }
                                restrictedOpsValue.append("]");
                                pw.print("        ");
                                pw.print("user: ");
                                pw.print(userId);
                                pw.print(" restricted ops: ");
                                pw.println(restrictedOpsValue);
                            }
                        }
                    }
                    int excludedPackageCount = restrictionState.perUserExcludedPackages != null ? restrictionState.perUserExcludedPackages.size() : 0;
                    if (excludedPackageCount > 0) {
                        pw.println("      Excluded packages:");
                        for (j = 0; j < excludedPackageCount; j++) {
                            userId = restrictionState.perUserExcludedPackages.keyAt(j);
                            String[] packageNames = (String[]) restrictionState.perUserExcludedPackages.valueAt(j);
                            pw.print("        ");
                            pw.print("user: ");
                            pw.print(userId);
                            pw.print(" packages: ");
                            pw.println(Arrays.toString(packageNames));
                        }
                    }
                }
            }
        }
    }

    public void setUserRestrictions(Bundle restrictions, IBinder token, int userHandle) {
        checkSystemUid("setUserRestrictions");
        Preconditions.checkNotNull(restrictions);
        Preconditions.checkNotNull(token);
        for (int i = 0; i < 70; i++) {
            String restriction = AppOpsManager.opToRestriction(i);
            if (restriction != null) {
                setUserRestrictionNoCheck(i, restrictions.getBoolean(restriction, false), token, userHandle, null);
            }
        }
    }

    public void setUserRestriction(int code, boolean restricted, IBinder token, int userHandle, String[] exceptionPackages) {
        if (Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforcePermission("android.permission.MANAGE_APP_OPS_RESTRICTIONS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        }
        if (userHandle == UserHandle.getCallingUserId() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0 || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS") == 0) {
            verifyIncomingOp(code);
            Preconditions.checkNotNull(token);
            setUserRestrictionNoCheck(code, restricted, token, userHandle, exceptionPackages);
            return;
        }
        throw new SecurityException("Need INTERACT_ACROSS_USERS_FULL or INTERACT_ACROSS_USERS to interact cross user ");
    }

    private void setUserRestrictionNoCheck(int code, boolean restricted, IBinder token, int userHandle, String[] exceptionPackages) {
        boolean notifyChange = false;
        synchronized (this) {
            ClientRestrictionState restrictionState = (ClientRestrictionState) this.mOpUserRestrictions.get(token);
            if (restrictionState == null) {
                try {
                    restrictionState = new ClientRestrictionState(token);
                    this.mOpUserRestrictions.put(token, restrictionState);
                } catch (RemoteException e) {
                    return;
                }
            }
            if (restrictionState.setRestriction(code, restricted, exceptionPackages, userHandle)) {
                notifyChange = true;
            }
            if (restrictionState.isDefault()) {
                this.mOpUserRestrictions.remove(token);
                restrictionState.destroy();
            }
        }
        if (notifyChange) {
            notifyWatchersOfChange(code);
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0013, code:
            r6 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Missing block: B:11:?, code:
            r1 = r3.size();
            r5 = 0;
     */
    /* JADX WARNING: Missing block: B:12:0x001c, code:
            if (r5 >= r1) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            ((com.android.server.AppOpsService.Callback) r3.valueAt(r5)).mCallback.opChanged(r12, -1, null);
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Log.w(TAG, "Error dispatching op op change");
     */
    /* JADX WARNING: Missing block: B:24:0x003d, code:
            android.os.Binder.restoreCallingIdentity(r6);
     */
    /* JADX WARNING: Missing block: B:26:0x0041, code:
            android.os.Binder.restoreCallingIdentity(r6);
     */
    /* JADX WARNING: Missing block: B:27:0x0044, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyWatchersOfChange(int code) {
        synchronized (this) {
            ArraySet<Callback> callbacks = (ArraySet) this.mOpModeWatchers.get(code);
            if (callbacks == null) {
                return;
            }
            ArraySet<Callback> clonedCallbacks = new ArraySet(callbacks);
        }
        int i++;
    }

    public void removeUser(int userHandle) throws RemoteException {
        checkSystemUid("removeUser");
        synchronized (this) {
            for (int i = this.mOpUserRestrictions.size() - 1; i >= 0; i--) {
                ((ClientRestrictionState) this.mOpUserRestrictions.valueAt(i)).removeUser(userHandle);
            }
            removeUidsForUserLocked(userHandle);
        }
    }

    private void removeUidsForUserLocked(int userHandle) {
        for (int i = this.mUidStates.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(this.mUidStates.keyAt(i)) == userHandle) {
                this.mUidStates.removeAt(i);
            }
        }
    }

    private void checkSystemUid(String function) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException(function + " must by called by the system");
        }
    }

    protected void scheduleWriteLockedHook(int code) {
    }

    private static String resolvePackageName(int uid, String packageName) {
        if (uid == 0) {
            return "root";
        }
        if (uid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
            return "com.android.shell";
        }
        if (uid == 1000 && packageName == null) {
            return "android";
        }
        return packageName;
    }

    private static String[] getPackagesForUid(int uid) {
        String[] packageNames = null;
        try {
            packageNames = AppGlobals.getPackageManager().getPackagesForUid(uid);
        } catch (RemoteException e) {
        }
        if (packageNames == null) {
            return EmptyArray.STRING;
        }
        return packageNames;
    }
}
