package com.android.server.appop;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AppOpsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageManagerInternal;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.KeyValueListParser;
import android.util.LongSparseArray;
import android.util.LongSparseLongArray;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IAppOpsActiveCallback;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsNotedCallback;
import com.android.internal.app.IAppOpsService;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.util.function.QuadFunction;
import com.android.internal.util.function.TriFunction;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.LocalServices;
import com.android.server.LockGuard;
import com.android.server.SystemService;
import com.android.server.appop.AppOpsService;
import com.android.server.display.color.DisplayTransformManager;
import com.android.server.job.controllers.JobStatus;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import com.android.server.notification.NotificationShellCmd;
import com.android.server.pm.PackageManagerService;
import com.android.server.power.IHwShutdownThread;
import com.android.server.slice.SliceClientPermissions;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class AppOpsService extends IAppOpsService.Stub {
    private static final int CURRENT_VERSION = 1;
    static final boolean DEBUG = false;
    private static final int NO_VERSION = -1;
    private static final int[] OPS_RESTRICTED_ON_SUSPEND = {28, 27, 26};
    private static final int[] PROCESS_STATE_TO_UID_STATE = {100, 100, 200, DisplayTransformManager.LEVEL_COLOR_MATRIX_INVERT_COLOR, SystemService.PHASE_SYSTEM_SERVICES_READY, 400, SystemService.PHASE_SYSTEM_SERVICES_READY, SystemService.PHASE_SYSTEM_SERVICES_READY, SystemService.PHASE_THIRD_PARTY_APPS_CAN_START, SystemService.PHASE_THIRD_PARTY_APPS_CAN_START, SystemService.PHASE_THIRD_PARTY_APPS_CAN_START, SystemService.PHASE_THIRD_PARTY_APPS_CAN_START, SystemService.PHASE_THIRD_PARTY_APPS_CAN_START, 700, 700, 700, 700, 700, 700, 700, 700, 700};
    static final String TAG = "AppOps";
    private static final int UID_ANY = -2;
    static final long WRITE_DELAY = 1800000;
    final ArrayMap<IBinder, SparseArray<ActiveCallback>> mActiveWatchers = new ArrayMap<>();
    private final AppOpsManagerInternalImpl mAppOpsManagerInternal = new AppOpsManagerInternalImpl();
    final SparseArray<SparseArray<Restriction>> mAudioRestrictions = new SparseArray<>();
    @GuardedBy({"this"})
    private AppOpsManagerInternal.CheckOpsDelegate mCheckOpsDelegate;
    final ArrayMap<IBinder, ClientState> mClients = new ArrayMap<>();
    @VisibleForTesting
    final Constants mConstants;
    Context mContext;
    boolean mFastWriteScheduled;
    final AtomicFile mFile;
    final Handler mHandler;
    final HistoricalRegistry mHistoricalRegistry = new HistoricalRegistry(this);
    long mLastRealtime;
    final ArrayMap<IBinder, ModeCallback> mModeWatchers = new ArrayMap<>();
    final ArrayMap<IBinder, SparseArray<NotedCallback>> mNotedWatchers = new ArrayMap<>();
    final SparseArray<ArraySet<ModeCallback>> mOpModeWatchers = new SparseArray<>();
    private final ArrayMap<IBinder, ClientRestrictionState> mOpUserRestrictions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<ModeCallback>> mPackageModeWatchers = new ArrayMap<>();
    SparseIntArray mProfileOwners;
    @VisibleForTesting
    final SparseArray<UidState> mUidStates = new SparseArray<>();
    final Runnable mWriteRunner = new Runnable() {
        /* class com.android.server.appop.AppOpsService.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (AppOpsService.this) {
                AppOpsService.this.mWriteScheduled = false;
                AppOpsService.this.mFastWriteScheduled = false;
                new AsyncTask<Void, Void, Void>() {
                    /* class com.android.server.appop.AppOpsService.AnonymousClass1.AnonymousClass1 */

                    /* access modifiers changed from: protected */
                    public Void doInBackground(Void... params) {
                        AppOpsService.this.writeState();
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            }
        }
    };
    boolean mWriteScheduled;

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public final class Constants extends ContentObserver {
        private static final String KEY_BG_STATE_SETTLE_TIME = "bg_state_settle_time";
        private static final String KEY_FG_SERVICE_STATE_SETTLE_TIME = "fg_service_state_settle_time";
        private static final String KEY_TOP_STATE_SETTLE_TIME = "top_state_settle_time";
        public long BG_STATE_SETTLE_TIME;
        public long FG_SERVICE_STATE_SETTLE_TIME;
        public long TOP_STATE_SETTLE_TIME;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
            updateConstants();
        }

        public void startMonitoring(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("app_ops_constants"), false, this);
            updateConstants();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            String value;
            ContentResolver contentResolver = this.mResolver;
            if (contentResolver != null) {
                value = Settings.Global.getString(contentResolver, "app_ops_constants");
            } else {
                value = "";
            }
            synchronized (AppOpsService.this) {
                try {
                    this.mParser.setString(value);
                } catch (IllegalArgumentException e) {
                    Slog.e(AppOpsService.TAG, "Bad app ops settings", e);
                }
                this.TOP_STATE_SETTLE_TIME = this.mParser.getDurationMillis(KEY_TOP_STATE_SETTLE_TIME, 30000);
                this.FG_SERVICE_STATE_SETTLE_TIME = this.mParser.getDurationMillis(KEY_FG_SERVICE_STATE_SETTLE_TIME, (long) JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                this.BG_STATE_SETTLE_TIME = this.mParser.getDurationMillis(KEY_BG_STATE_SETTLE_TIME, 1000);
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw) {
            pw.println("  Settings:");
            pw.print("    ");
            pw.print(KEY_TOP_STATE_SETTLE_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.TOP_STATE_SETTLE_TIME, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_FG_SERVICE_STATE_SETTLE_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.FG_SERVICE_STATE_SETTLE_TIME, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_BG_STATE_SETTLE_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.BG_STATE_SETTLE_TIME, pw);
            pw.println();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static final class UidState {
        public SparseBooleanArray foregroundOps;
        public boolean hasForegroundWatchers;
        public SparseIntArray opModes;
        public int pendingState = 700;
        public long pendingStateCommitTime;
        public ArrayMap<String, Ops> pkgOps;
        public int startNesting;
        public int state = 700;
        public final int uid;

        public UidState(int uid2) {
            this.uid = uid2;
        }

        public void clear() {
            this.pkgOps = null;
            this.opModes = null;
        }

        public boolean isDefault() {
            SparseIntArray sparseIntArray;
            ArrayMap<String, Ops> arrayMap = this.pkgOps;
            return (arrayMap == null || arrayMap.isEmpty()) && ((sparseIntArray = this.opModes) == null || sparseIntArray.size() <= 0) && this.state == 700 && this.pendingState == 700;
        }

        /* access modifiers changed from: package-private */
        public int evalMode(int op, int mode) {
            if (mode == 4) {
                return this.state <= AppOpsManager.resolveFirstUnrestrictedUidState(op) ? 0 : 1;
            }
            return mode;
        }

        private void evalForegroundWatchers(int op, SparseArray<ArraySet<ModeCallback>> watchers, SparseBooleanArray which) {
            boolean curValue = which.get(op, false);
            ArraySet<ModeCallback> callbacks = watchers.get(op);
            if (callbacks != null) {
                int cbi = callbacks.size() - 1;
                while (!curValue && cbi >= 0) {
                    if ((callbacks.valueAt(cbi).mFlags & 1) != 0) {
                        this.hasForegroundWatchers = true;
                        curValue = true;
                    }
                    cbi--;
                }
            }
            which.put(op, curValue);
        }

        public void evalForegroundOps(SparseArray<ArraySet<ModeCallback>> watchers) {
            SparseBooleanArray which = null;
            this.hasForegroundWatchers = false;
            SparseIntArray sparseIntArray = this.opModes;
            if (sparseIntArray != null) {
                for (int i = sparseIntArray.size() - 1; i >= 0; i--) {
                    if (this.opModes.valueAt(i) == 4) {
                        if (which == null) {
                            which = new SparseBooleanArray();
                        }
                        evalForegroundWatchers(this.opModes.keyAt(i), watchers, which);
                    }
                }
            }
            ArrayMap<String, Ops> arrayMap = this.pkgOps;
            if (arrayMap != null) {
                for (int i2 = arrayMap.size() - 1; i2 >= 0; i2--) {
                    Ops ops = this.pkgOps.valueAt(i2);
                    for (int j = ops.size() - 1; j >= 0; j--) {
                        if (((Op) ops.valueAt(j)).mode == 4) {
                            if (which == null) {
                                which = new SparseBooleanArray();
                            }
                            evalForegroundWatchers(ops.keyAt(j), watchers, which);
                        }
                    }
                }
            }
            this.foregroundOps = which;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class Ops extends SparseArray<Op> {
        final boolean isPrivileged;
        final String packageName;
        final UidState uidState;

        Ops(String _packageName, UidState _uidState, boolean _isPrivileged) {
            this.packageName = _packageName;
            this.uidState = _uidState;
            this.isPrivileged = _isPrivileged;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class Op {
        private LongSparseLongArray mAccessTimes;
        private LongSparseLongArray mDurations;
        private LongSparseArray<String> mProxyPackageNames;
        private LongSparseLongArray mProxyUids;
        private LongSparseLongArray mRejectTimes;
        private int mode;
        int op;
        final String packageName;
        boolean running;
        int startNesting;
        long startRealtime;
        final UidState uidState;

        Op(UidState uidState2, String packageName2, int op2) {
            this.op = op2;
            this.uidState = uidState2;
            this.packageName = packageName2;
            this.mode = AppOpsManager.opToDefaultMode(op2);
        }

        /* access modifiers changed from: package-private */
        public int getMode() {
            return this.mode;
        }

        /* access modifiers changed from: package-private */
        public int evalMode() {
            return this.uidState.evalMode(this.op, this.mode);
        }

        public void accessed(long time, int proxyUid, String proxyPackageName, int uidState2, int flags) {
            long key = AppOpsManager.makeKey(uidState2, flags);
            if (this.mAccessTimes == null) {
                this.mAccessTimes = new LongSparseLongArray();
            }
            this.mAccessTimes.put(key, time);
            updateProxyState(key, proxyUid, proxyPackageName);
            LongSparseLongArray longSparseLongArray = this.mDurations;
            if (longSparseLongArray != null) {
                longSparseLongArray.delete(key);
            }
        }

        public void rejected(long time, int proxyUid, String proxyPackageName, int uidState2, int flags) {
            long key = AppOpsManager.makeKey(uidState2, flags);
            if (this.mRejectTimes == null) {
                this.mRejectTimes = new LongSparseLongArray();
            }
            this.mRejectTimes.put(key, time);
            updateProxyState(key, proxyUid, proxyPackageName);
            LongSparseLongArray longSparseLongArray = this.mDurations;
            if (longSparseLongArray != null) {
                longSparseLongArray.delete(key);
            }
        }

        public void started(long time, int uidState2, int flags) {
            updateAccessTimeAndDuration(time, -1, uidState2, flags);
            this.running = true;
        }

        public void finished(long time, long duration, int uidState2, int flags) {
            updateAccessTimeAndDuration(time, duration, uidState2, flags);
            this.running = false;
        }

        public void running(long time, long duration, int uidState2, int flags) {
            updateAccessTimeAndDuration(time, duration, uidState2, flags);
        }

        public void continuing(long duration, int uidState2, int flags) {
            long key = AppOpsManager.makeKey(uidState2, flags);
            if (this.mDurations == null) {
                this.mDurations = new LongSparseLongArray();
            }
            this.mDurations.put(key, duration);
        }

        private void updateAccessTimeAndDuration(long time, long duration, int uidState2, int flags) {
            long key = AppOpsManager.makeKey(uidState2, flags);
            if (this.mAccessTimes == null) {
                this.mAccessTimes = new LongSparseLongArray();
            }
            this.mAccessTimes.put(key, time);
            if (this.mDurations == null) {
                this.mDurations = new LongSparseLongArray();
            }
            this.mDurations.put(key, duration);
        }

        private void updateProxyState(long key, int proxyUid, String proxyPackageName) {
            if (this.mProxyUids == null) {
                this.mProxyUids = new LongSparseLongArray();
            }
            this.mProxyUids.put(key, (long) proxyUid);
            if (this.mProxyPackageNames == null) {
                this.mProxyPackageNames = new LongSparseArray<>();
            }
            this.mProxyPackageNames.put(key, proxyPackageName);
        }

        /* access modifiers changed from: package-private */
        public boolean hasAnyTime() {
            LongSparseLongArray longSparseLongArray;
            LongSparseLongArray longSparseLongArray2 = this.mAccessTimes;
            return (longSparseLongArray2 != null && longSparseLongArray2.size() > 0) || ((longSparseLongArray = this.mRejectTimes) != null && longSparseLongArray.size() > 0);
        }
    }

    /* access modifiers changed from: package-private */
    public final class ModeCallback implements IBinder.DeathRecipient {
        final IAppOpsCallback mCallback;
        final int mCallingPid;
        final int mCallingUid;
        final int mFlags;
        final int mWatchingUid;

        ModeCallback(IAppOpsCallback callback, int watchingUid, int flags, int callingUid, int callingPid) {
            this.mCallback = callback;
            this.mWatchingUid = watchingUid;
            this.mFlags = flags;
            this.mCallingUid = callingUid;
            this.mCallingPid = callingPid;
            try {
                this.mCallback.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        public boolean isWatchingUid(int uid) {
            int i;
            return uid == -2 || (i = this.mWatchingUid) < 0 || i == uid;
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ModeCallback{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" watchinguid=");
            UserHandle.formatUid(sb, this.mWatchingUid);
            sb.append(" flags=0x");
            sb.append(Integer.toHexString(this.mFlags));
            sb.append(" from uid=");
            UserHandle.formatUid(sb, this.mCallingUid);
            sb.append(" pid=");
            sb.append(this.mCallingPid);
            sb.append('}');
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public void unlinkToDeath() {
            this.mCallback.asBinder().unlinkToDeath(this, 0);
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            AppOpsService.this.stopWatchingMode(this.mCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public final class ActiveCallback implements IBinder.DeathRecipient {
        final IAppOpsActiveCallback mCallback;
        final int mCallingPid;
        final int mCallingUid;
        final int mWatchingUid;

        ActiveCallback(IAppOpsActiveCallback callback, int watchingUid, int callingUid, int callingPid) {
            this.mCallback = callback;
            this.mWatchingUid = watchingUid;
            this.mCallingUid = callingUid;
            this.mCallingPid = callingPid;
            try {
                this.mCallback.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ActiveCallback{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" watchinguid=");
            UserHandle.formatUid(sb, this.mWatchingUid);
            sb.append(" from uid=");
            UserHandle.formatUid(sb, this.mCallingUid);
            sb.append(" pid=");
            sb.append(this.mCallingPid);
            sb.append('}');
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public void destroy() {
            this.mCallback.asBinder().unlinkToDeath(this, 0);
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            AppOpsService.this.stopWatchingActive(this.mCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public final class NotedCallback implements IBinder.DeathRecipient {
        final IAppOpsNotedCallback mCallback;
        final int mCallingPid;
        final int mCallingUid;
        final int mWatchingUid;

        NotedCallback(IAppOpsNotedCallback callback, int watchingUid, int callingUid, int callingPid) {
            this.mCallback = callback;
            this.mWatchingUid = watchingUid;
            this.mCallingUid = callingUid;
            this.mCallingPid = callingPid;
            try {
                this.mCallback.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("NotedCallback{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" watchinguid=");
            UserHandle.formatUid(sb, this.mWatchingUid);
            sb.append(" from uid=");
            UserHandle.formatUid(sb, this.mCallingUid);
            sb.append(" pid=");
            sb.append(this.mCallingPid);
            sb.append('}');
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public void destroy() {
            this.mCallback.asBinder().unlinkToDeath(this, 0);
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            AppOpsService.this.stopWatchingNoted(this.mCallback);
        }
    }

    /* access modifiers changed from: package-private */
    public final class ClientState extends Binder implements IBinder.DeathRecipient {
        final IBinder mAppToken;
        final int mPid;
        final ArrayList<Op> mStartedOps = new ArrayList<>();

        ClientState(IBinder appToken) {
            this.mAppToken = appToken;
            this.mPid = Binder.getCallingPid();
            if (!(appToken instanceof Binder)) {
                try {
                    this.mAppToken.linkToDeath(this, 0);
                } catch (RemoteException e) {
                }
            }
        }

        @Override // java.lang.Object
        public String toString() {
            return "ClientState{mAppToken=" + this.mAppToken + ", pid=" + this.mPid + '}';
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (AppOpsService.this) {
                for (int i = this.mStartedOps.size() - 1; i >= 0; i--) {
                    AppOpsService.this.finishOperationLocked(this.mStartedOps.get(i), true);
                }
                AppOpsService.this.mClients.remove(this.mAppToken);
            }
        }
    }

    public AppOpsService(File storagePath, Handler handler) {
        LockGuard.installLock(this, 0);
        this.mFile = new AtomicFile(storagePath, "appops");
        this.mHandler = handler;
        this.mConstants = new Constants(this.mHandler);
        readState();
    }

    public void publish(Context context) {
        this.mContext = context;
        ServiceManager.addService("appops", asBinder());
        LocalServices.addService(AppOpsManagerInternal.class, this.mAppOpsManagerInternal);
    }

    public void systemReady() {
        this.mConstants.startMonitoring(this.mContext.getContentResolver());
        this.mHistoricalRegistry.systemReady(this.mContext.getContentResolver());
        synchronized (this) {
            boolean changed = false;
            for (int i = this.mUidStates.size() - 1; i >= 0; i--) {
                UidState uidState = this.mUidStates.valueAt(i);
                if (ArrayUtils.isEmpty(getPackagesForUid(uidState.uid))) {
                    uidState.clear();
                    this.mUidStates.removeAt(i);
                    changed = true;
                } else {
                    ArrayMap<String, Ops> pkgs = uidState.pkgOps;
                    if (pkgs != null) {
                        Iterator<Ops> it = pkgs.values().iterator();
                        while (it.hasNext()) {
                            Ops ops = it.next();
                            int curUid = -1;
                            try {
                                curUid = AppGlobals.getPackageManager().getPackageUid(ops.packageName, 8192, UserHandle.getUserId(ops.uidState.uid));
                            } catch (RemoteException e) {
                            }
                            if (curUid != ops.uidState.uid) {
                                Slog.i(TAG, "Pruning old package " + ops.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + ops.uidState + ": new uid=" + curUid);
                                it.remove();
                                changed = true;
                            }
                        }
                        if (uidState.isDefault()) {
                            this.mUidStates.removeAt(i);
                        }
                    }
                }
            }
            if (changed) {
                scheduleFastWriteLocked();
            }
        }
        IntentFilter packageSuspendFilter = new IntentFilter();
        packageSuspendFilter.addAction("android.intent.action.PACKAGES_UNSUSPENDED");
        packageSuspendFilter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.appop.AppOpsService.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int[] changedUids = intent.getIntArrayExtra("android.intent.extra.changed_uid_list");
                String[] changedPkgs = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                int[] iArr = AppOpsService.OPS_RESTRICTED_ON_SUSPEND;
                for (int code : iArr) {
                    synchronized (AppOpsService.this) {
                        ArraySet<ModeCallback> callbacks = AppOpsService.this.mOpModeWatchers.get(code);
                        if (callbacks != null) {
                            ArraySet<ModeCallback> callbacks2 = new ArraySet<>(callbacks);
                            for (int i = 0; i < changedUids.length; i++) {
                                AppOpsService.this.notifyOpChanged(callbacks2, code, changedUids[i], changedPkgs[i]);
                            }
                        }
                    }
                }
            }
        }, packageSuspendFilter);
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setExternalSourcesPolicy(new PackageManagerInternal.ExternalSourcesPolicy() {
            /* class com.android.server.appop.AppOpsService.AnonymousClass3 */

            public int getPackageTrustedToInstallApps(String packageName, int uid) {
                int appOpMode = AppOpsService.this.checkOperation(66, uid, packageName);
                if (appOpMode == 0) {
                    return 0;
                }
                if (appOpMode != 2) {
                    return 2;
                }
                return 1;
            }
        });
        if (!StorageManager.hasIsolatedStorage()) {
            ((StorageManagerInternal) LocalServices.getService(StorageManagerInternal.class)).addExternalStoragePolicy(new StorageManagerInternal.ExternalStorageMountPolicy() {
                /* class com.android.server.appop.AppOpsService.AnonymousClass4 */

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
                    return mountMode == 2 || mountMode == 3;
                }
            });
        }
    }

    public void packageRemoved(int uid, String packageName) {
        synchronized (this) {
            UidState uidState = this.mUidStates.get(uid);
            if (uidState != null) {
                Ops ops = null;
                if (uidState.pkgOps != null) {
                    ops = uidState.pkgOps.remove(packageName);
                }
                if (ops != null && uidState.pkgOps.isEmpty() && getPackagesForUid(uid).length <= 0) {
                    this.mUidStates.remove(uid);
                }
                int clientCount = this.mClients.size();
                for (int i = 0; i < clientCount; i++) {
                    ClientState client = this.mClients.valueAt(i);
                    if (client.mStartedOps != null) {
                        for (int j = client.mStartedOps.size() - 1; j >= 0; j--) {
                            Op op = client.mStartedOps.get(j);
                            if (uid == op.uidState.uid && packageName.equals(op.packageName)) {
                                finishOperationLocked(op, true);
                                client.mStartedOps.remove(j);
                                if (op.startNesting <= 0) {
                                    scheduleOpActiveChangedIfNeededLocked(op.op, uid, packageName, false);
                                }
                            }
                        }
                    }
                }
                if (ops != null) {
                    scheduleFastWriteLocked();
                    int opCount = ops.size();
                    for (int i2 = 0; i2 < opCount; i2++) {
                        Op op2 = (Op) ops.valueAt(i2);
                        if (op2.running) {
                            scheduleOpActiveChangedIfNeededLocked(op2.op, op2.uidState.uid, op2.packageName, false);
                        }
                    }
                }
                this.mHistoricalRegistry.clearHistory(uid, packageName);
            }
        }
    }

    public void uidRemoved(int uid) {
        synchronized (this) {
            if (this.mUidStates.indexOfKey(uid) >= 0) {
                this.mUidStates.remove(uid);
                scheduleFastWriteLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void updatePendingState(long currentTime, int uid) {
        synchronized (this) {
            this.mLastRealtime = Long.max(currentTime, this.mLastRealtime);
            updatePendingStateIfNeededLocked(this.mUidStates.get(uid));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x006c  */
    public void updateUidProcState(int uid, int procState) {
        int j;
        Ops ops;
        long now;
        long settleTime;
        synchronized (this) {
            UidState uidState = getUidStateLocked(uid, true);
            int newState = PROCESS_STATE_TO_UID_STATE[procState];
            if (!(uidState == null || uidState.pendingState == newState)) {
                int oldPendingState = uidState.pendingState;
                uidState.pendingState = newState;
                if (newState >= uidState.state) {
                    if (newState > 400 || uidState.state <= 400) {
                        if (uidState.pendingStateCommitTime == 0) {
                            if (uidState.state <= 200) {
                                settleTime = this.mConstants.TOP_STATE_SETTLE_TIME;
                            } else if (uidState.state <= 400) {
                                settleTime = this.mConstants.FG_SERVICE_STATE_SETTLE_TIME;
                            } else {
                                settleTime = this.mConstants.BG_STATE_SETTLE_TIME;
                            }
                            long commitTime = SystemClock.elapsedRealtime() + settleTime;
                            uidState.pendingStateCommitTime = commitTime;
                            this.mHandler.sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$AppOpsService$CVMSlLMRyZYA1tmqvyuOloKBu0.INSTANCE, this, Long.valueOf(commitTime + 1), Integer.valueOf(uid)), 1 + settleTime);
                        }
                        if (uidState.startNesting != 0) {
                            long now2 = System.currentTimeMillis();
                            for (int i = uidState.pkgOps.size() - 1; i >= 0; i--) {
                                Ops ops2 = uidState.pkgOps.valueAt(i);
                                int j2 = ops2.size() - 1;
                                while (j2 >= 0) {
                                    Op op = (Op) ops2.valueAt(j2);
                                    if (op.startNesting > 0) {
                                        long duration = SystemClock.elapsedRealtime() - op.startRealtime;
                                        ops = ops2;
                                        j = j2;
                                        this.mHistoricalRegistry.increaseOpAccessDuration(op.op, op.uidState.uid, op.packageName, oldPendingState, 1, duration);
                                        now = now2;
                                        op.finished(now2, duration, oldPendingState, 1);
                                        op.startRealtime = now;
                                        op.started(now, newState, 1);
                                    } else {
                                        ops = ops2;
                                        j = j2;
                                        now = now2;
                                    }
                                    j2 = j - 1;
                                    now2 = now;
                                    ops2 = ops;
                                }
                            }
                        }
                    }
                }
                commitUidPendingStateLocked(uidState);
                if (uidState.startNesting != 0) {
                }
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

    private ArrayList<AppOpsManager.OpEntry> collectOps(Ops pkgOps, int[] ops) {
        ArrayList<AppOpsManager.OpEntry> resOps = null;
        long elapsedNow = SystemClock.elapsedRealtime();
        if (ops == null) {
            resOps = new ArrayList<>();
            for (int j = 0; j < pkgOps.size(); j++) {
                resOps.add(getOpEntryForResult((Op) pkgOps.valueAt(j), elapsedNow));
            }
        } else {
            for (int i : ops) {
                Op curOp = (Op) pkgOps.get(i);
                if (curOp != null) {
                    if (resOps == null) {
                        resOps = new ArrayList<>();
                    }
                    resOps.add(getOpEntryForResult(curOp, elapsedNow));
                }
            }
        }
        return resOps;
    }

    private ArrayList<AppOpsManager.OpEntry> collectOps(SparseIntArray uidOps, int[] ops) {
        if (uidOps == null) {
            return null;
        }
        ArrayList<AppOpsManager.OpEntry> resOps = null;
        if (ops == null) {
            resOps = new ArrayList<>();
            for (int j = 0; j < uidOps.size(); j++) {
                resOps.add(new AppOpsManager.OpEntry(uidOps.keyAt(j), uidOps.valueAt(j)));
            }
        } else {
            for (int i : ops) {
                int index = uidOps.indexOfKey(i);
                if (index >= 0) {
                    if (resOps == null) {
                        resOps = new ArrayList<>();
                    }
                    resOps.add(new AppOpsManager.OpEntry(uidOps.keyAt(index), uidOps.valueAt(index)));
                }
            }
        }
        return resOps;
    }

    private static AppOpsManager.OpEntry getOpEntryForResult(Op op, long elapsedNow) {
        if (op.running) {
            op.continuing(elapsedNow - op.startRealtime, op.uidState.state, 1);
        }
        return new AppOpsManager.OpEntry(op.op, op.running, op.mode, op.mAccessTimes != null ? op.mAccessTimes.clone() : null, op.mRejectTimes != null ? op.mRejectTimes.clone() : null, op.mDurations != null ? op.mDurations.clone() : null, op.mProxyUids != null ? op.mProxyUids.clone() : null, op.mProxyPackageNames != null ? op.mProxyPackageNames.clone() : null);
    }

    public List<AppOpsManager.PackageOps> getPackagesForOps(int[] ops) {
        this.mContext.enforcePermission("android.permission.GET_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        ArrayList<AppOpsManager.PackageOps> res = null;
        synchronized (this) {
            int uidStateCount = this.mUidStates.size();
            for (int i = 0; i < uidStateCount; i++) {
                UidState uidState = this.mUidStates.valueAt(i);
                if (uidState.pkgOps != null) {
                    if (!uidState.pkgOps.isEmpty()) {
                        ArrayMap<String, Ops> packages = uidState.pkgOps;
                        int packageCount = packages.size();
                        for (int j = 0; j < packageCount; j++) {
                            Ops pkgOps = packages.valueAt(j);
                            ArrayList<AppOpsManager.OpEntry> resOps = collectOps(pkgOps, ops);
                            if (resOps != null) {
                                if (res == null) {
                                    res = new ArrayList<>();
                                }
                                res.add(new AppOpsManager.PackageOps(pkgOps.packageName, pkgOps.uidState.uid, resOps));
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    public List<AppOpsManager.PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) {
        this.mContext.enforcePermission("android.permission.GET_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName == null) {
            return Collections.emptyList();
        }
        synchronized (this) {
            Ops pkgOps = getOpsRawLocked(uid, resolvedPackageName, false, false);
            if (pkgOps == null) {
                return null;
            }
            ArrayList<AppOpsManager.OpEntry> resOps = collectOps(pkgOps, ops);
            if (resOps == null) {
                return null;
            }
            ArrayList<AppOpsManager.PackageOps> res = new ArrayList<>();
            res.add(new AppOpsManager.PackageOps(pkgOps.packageName, pkgOps.uidState.uid, resOps));
            return res;
        }
    }

    public void getHistoricalOps(int uid, String packageName, List<String> opNames, long beginTimeMillis, long endTimeMillis, int flags, RemoteCallback callback) {
        new AppOpsManager.HistoricalOpsRequest.Builder(beginTimeMillis, endTimeMillis).setUid(uid).setPackageName(packageName).setOpNames(opNames).setFlags(flags).build();
        Preconditions.checkNotNull(callback, "callback cannot be null");
        this.mContext.enforcePermission("android.permission.GET_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), "getHistoricalOps");
        this.mHistoricalRegistry.getHistoricalOps(uid, packageName, opNames != null ? (String[]) opNames.toArray(new String[opNames.size()]) : null, beginTimeMillis, endTimeMillis, flags, callback);
    }

    public void getHistoricalOpsFromDiskRaw(int uid, String packageName, List<String> opNames, long beginTimeMillis, long endTimeMillis, int flags, RemoteCallback callback) {
        new AppOpsManager.HistoricalOpsRequest.Builder(beginTimeMillis, endTimeMillis).setUid(uid).setPackageName(packageName).setOpNames(opNames).setFlags(flags).build();
        Preconditions.checkNotNull(callback, "callback cannot be null");
        this.mContext.enforcePermission("android.permission.MANAGE_APPOPS", Binder.getCallingPid(), Binder.getCallingUid(), "getHistoricalOps");
        this.mHistoricalRegistry.getHistoricalOpsFromDiskRaw(uid, packageName, opNames != null ? (String[]) opNames.toArray(new String[opNames.size()]) : null, beginTimeMillis, endTimeMillis, flags, callback);
    }

    public void reloadNonHistoricalState() {
        this.mContext.enforcePermission("android.permission.MANAGE_APPOPS", Binder.getCallingPid(), Binder.getCallingUid(), "reloadNonHistoricalState");
        writeState();
        readState();
    }

    public List<AppOpsManager.PackageOps> getUidOps(int uid, int[] ops) {
        this.mContext.enforcePermission("android.permission.GET_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        synchronized (this) {
            UidState uidState = getUidStateLocked(uid, false);
            if (uidState == null) {
                return null;
            }
            ArrayList<AppOpsManager.OpEntry> resOps = collectOps(uidState.opModes, ops);
            if (resOps == null) {
                return null;
            }
            ArrayList<AppOpsManager.PackageOps> res = new ArrayList<>();
            res.add(new AppOpsManager.PackageOps((String) null, uidState.uid, resOps));
            return res;
        }
    }

    private void pruneOp(Op op, int uid, String packageName) {
        Ops ops;
        UidState uidState;
        ArrayMap<String, Ops> pkgOps;
        if (!op.hasAnyTime() && (ops = getOpsRawLocked(uid, packageName, false, false)) != null) {
            ops.remove(op.op);
            if (ops.size() <= 0 && (pkgOps = (uidState = ops.uidState).pkgOps) != null) {
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

    private void enforceManageAppOpsModes(int callingPid, int callingUid, int targetUid) {
        if (callingPid != Process.myPid()) {
            int callingUser = UserHandle.getUserId(callingUid);
            synchronized (this) {
                if (this.mProfileOwners == null || this.mProfileOwners.get(callingUser, -1) != callingUid || targetUid < 0 || callingUser != UserHandle.getUserId(targetUid)) {
                    this.mContext.enforcePermission("android.permission.MANAGE_APP_OPS_MODES", Binder.getCallingPid(), Binder.getCallingUid(), null);
                }
            }
        }
    }

    public void setUidMode(int code, int uid, int mode) {
        enforceManageAppOpsModes(Binder.getCallingPid(), Binder.getCallingUid(), uid);
        verifyIncomingOp(code);
        int code2 = AppOpsManager.opToSwitch(code);
        synchronized (this) {
            int defaultMode = AppOpsManager.opToDefaultMode(code2);
            UidState uidState = getUidStateLocked(uid, false);
            if (uidState == null) {
                if (mode != defaultMode) {
                    uidState = new UidState(uid);
                    uidState.opModes = new SparseIntArray();
                    uidState.opModes.put(code2, mode);
                    this.mUidStates.put(uid, uidState);
                    scheduleWriteLocked();
                } else {
                    return;
                }
            } else if (uidState.opModes == null) {
                if (mode != defaultMode) {
                    uidState.opModes = new SparseIntArray();
                    uidState.opModes.put(code2, mode);
                    scheduleWriteLocked();
                }
            } else if (uidState.opModes.indexOfKey(code2) < 0 || uidState.opModes.get(code2) != mode) {
                if (mode == defaultMode) {
                    uidState.opModes.delete(code2);
                    if (uidState.opModes.size() <= 0) {
                        uidState.opModes = null;
                    }
                } else {
                    uidState.opModes.put(code2, mode);
                }
                scheduleWriteLocked();
            } else {
                return;
            }
            uidState.evalForegroundOps(this.mOpModeWatchers);
            notifyOpChangedForAllPkgsInUid(code2, uid, false);
            notifyOpChangedSync(code2, uid, null, mode);
        }
    }

    /* access modifiers changed from: private */
    public void notifyOpChangedForAllPkgsInUid(int code, int uid, boolean onlyForeground) {
        Throwable th;
        ArrayMap<ModeCallback, ArraySet<String>> callbackSpecs;
        ArrayMap<ModeCallback, ArraySet<String>> callbackSpecs2;
        String[] uidPackageNames = getPackagesForUid(uid);
        ArrayMap<ModeCallback, ArraySet<String>> callbackSpecs3 = null;
        synchronized (this) {
            try {
                try {
                    ArraySet<ModeCallback> callbacks = this.mOpModeWatchers.get(code);
                    if (callbacks != null) {
                        try {
                            int callbackCount = callbacks.size();
                            for (int i = 0; i < callbackCount; i++) {
                                ModeCallback callback = callbacks.valueAt(i);
                                if (!onlyForeground || (callback.mFlags & 1) != 0) {
                                    ArraySet<String> changedPackages = new ArraySet<>();
                                    Collections.addAll(changedPackages, uidPackageNames);
                                    if (callbackSpecs3 == null) {
                                        callbackSpecs3 = new ArrayMap<>();
                                    }
                                    callbackSpecs3.put(callback, changedPackages);
                                }
                            }
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
                    }
                    callbackSpecs = callbackSpecs3;
                    for (String uidPackageName : uidPackageNames) {
                        try {
                            ArraySet<ModeCallback> callbacks2 = this.mPackageModeWatchers.get(uidPackageName);
                            if (callbacks2 != null) {
                                if (callbackSpecs == null) {
                                    callbackSpecs2 = new ArrayMap<>();
                                } else {
                                    callbackSpecs2 = callbackSpecs;
                                }
                                try {
                                    int callbackCount2 = callbacks2.size();
                                    for (int i2 = 0; i2 < callbackCount2; i2++) {
                                        ModeCallback callback2 = callbacks2.valueAt(i2);
                                        if (!onlyForeground || (callback2.mFlags & 1) != 0) {
                                            ArraySet<String> changedPackages2 = callbackSpecs2.get(callback2);
                                            if (changedPackages2 == null) {
                                                changedPackages2 = new ArraySet<>();
                                                callbackSpecs2.put(callback2, changedPackages2);
                                            }
                                            changedPackages2.add(uidPackageName);
                                        }
                                    }
                                    callbackSpecs = callbackSpecs2;
                                } catch (Throwable th4) {
                                    th = th4;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    try {
                    } catch (Throwable th6) {
                        th = th6;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
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
        if (callbackSpecs != null) {
            int i3 = 0;
            while (i3 < callbackSpecs.size()) {
                ModeCallback callback3 = callbackSpecs.keyAt(i3);
                ArraySet<String> reportedPackageNames = callbackSpecs.valueAt(i3);
                if (reportedPackageNames == null) {
                    this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$FYLTtxqrHmv8Y5UdZ9ybXKsSJhs.INSTANCE, this, callback3, Integer.valueOf(code), Integer.valueOf(uid), (Object) null));
                } else {
                    int reportedPackageCount = reportedPackageNames.size();
                    int j = 0;
                    while (j < reportedPackageCount) {
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$FYLTtxqrHmv8Y5UdZ9ybXKsSJhs.INSTANCE, this, callback3, Integer.valueOf(code), Integer.valueOf(uid), reportedPackageNames.valueAt(j)));
                        j++;
                        uidPackageNames = uidPackageNames;
                    }
                }
                i3++;
                uidPackageNames = uidPackageNames;
            }
        }
    }

    private void notifyOpChangedSync(int code, int uid, String packageName, int mode) {
        StorageManagerInternal storageManagerInternal = (StorageManagerInternal) LocalServices.getService(StorageManagerInternal.class);
        if (storageManagerInternal != null) {
            storageManagerInternal.onAppOpsChanged(code, uid, packageName, mode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAllPkgModesToDefault(int code, int uid) {
        synchronized (this) {
            UidState uidState = getUidStateLocked(uid, false);
            if (uidState != null) {
                ArrayMap<String, Ops> pkgOps = uidState.pkgOps;
                if (pkgOps != null) {
                    boolean scheduleWrite = false;
                    int numPkgs = pkgOps.size();
                    for (int pkgNum = 0; pkgNum < numPkgs; pkgNum++) {
                        Op op = (Op) pkgOps.valueAt(pkgNum).get(code);
                        if (op != null) {
                            int defaultMode = AppOpsManager.opToDefaultMode(code);
                            if (op.mode != defaultMode) {
                                op.mode = defaultMode;
                                scheduleWrite = true;
                            }
                        }
                    }
                    if (scheduleWrite) {
                        scheduleWriteLocked();
                    }
                }
            }
        }
    }

    public void setMode(int code, int uid, String packageName, int mode) {
        setMode(code, uid, packageName, mode, true, false);
    }

    private void setMode(int code, int uid, String packageName, int mode, boolean verifyUid, boolean isPrivileged) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.APPOPS_SETMODE);
        enforceManageAppOpsModes(Binder.getCallingPid(), Binder.getCallingUid(), uid);
        verifyIncomingOp(code);
        ArraySet<ModeCallback> repCbs = null;
        int code2 = AppOpsManager.opToSwitch(code);
        synchronized (this) {
            UidState uidState = getUidStateLocked(uid, false);
            Op op = getOpLocked(code2, uid, packageName, true, verifyUid, isPrivileged);
            if (!(op == null || op.mode == mode)) {
                op.mode = mode;
                if (uidState != null) {
                    uidState.evalForegroundOps(this.mOpModeWatchers);
                }
                ArraySet<? extends ModeCallback> arraySet = this.mOpModeWatchers.get(code2);
                if (arraySet != null) {
                    if (0 == 0) {
                        repCbs = new ArraySet<>();
                    }
                    repCbs.addAll(arraySet);
                }
                ArraySet<? extends ModeCallback> arraySet2 = this.mPackageModeWatchers.get(packageName);
                if (arraySet2 != null) {
                    if (repCbs == null) {
                        repCbs = new ArraySet<>();
                    }
                    repCbs.addAll(arraySet2);
                }
                if (mode == AppOpsManager.opToDefaultMode(op.op)) {
                    pruneOp(op, uid, packageName);
                }
                scheduleFastWriteLocked();
            }
        }
        if (repCbs != null) {
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$NDUi03ZZuuR42RDEIQ0UELKycc.INSTANCE, this, repCbs, Integer.valueOf(code2), Integer.valueOf(uid), packageName));
        }
        notifyOpChangedSync(code2, uid, packageName, mode);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void notifyOpChanged(ArraySet<ModeCallback> callbacks, int code, int uid, String packageName) {
        for (int i = 0; i < callbacks.size(); i++) {
            notifyOpChanged(callbacks.valueAt(i), code, uid, packageName);
        }
    }

    /* access modifiers changed from: private */
    public void notifyOpChanged(ModeCallback callback, int code, int uid, String packageName) {
        if (uid == -2 || callback.mWatchingUid < 0 || callback.mWatchingUid == uid) {
            long identity = Binder.clearCallingIdentity();
            try {
                callback.mCallback.opChanged(code, uid, packageName);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    private static HashMap<ModeCallback, ArrayList<ChangeRec>> addCallbacks(HashMap<ModeCallback, ArrayList<ChangeRec>> callbacks, int op, int uid, String packageName, ArraySet<ModeCallback> cbs) {
        if (cbs == null) {
            return callbacks;
        }
        if (callbacks == null) {
            callbacks = new HashMap<>();
        }
        boolean duplicate = false;
        int N = cbs.size();
        for (int i = 0; i < N; i++) {
            ModeCallback cb = cbs.valueAt(i);
            ArrayList<ChangeRec> reports = callbacks.get(cb);
            if (reports != null) {
                int reportCount = reports.size();
                int j = 0;
                while (true) {
                    if (j >= reportCount) {
                        break;
                    }
                    ChangeRec report = reports.get(j);
                    if (report.op == op && report.pkg.equals(packageName)) {
                        duplicate = true;
                        break;
                    }
                    j++;
                }
            } else {
                reports = new ArrayList<>();
                callbacks.put(cb, reports);
            }
            if (!duplicate) {
                reports.add(new ChangeRec(op, uid, packageName));
            }
        }
        return callbacks;
    }

    /* access modifiers changed from: package-private */
    public static final class ChangeRec {
        final int op;
        final String pkg;
        final int uid;

        ChangeRec(int _op, int _uid, String _pkg) {
            this.op = _op;
            this.uid = _uid;
            this.pkg = _pkg;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0031  */
    public void resetAllModes(int reqUserId, String reqPackageName) {
        int reqUid;
        Throwable th;
        HashMap<ModeCallback, ArrayList<ChangeRec>> callbacks;
        UidState uidState;
        int callingPid;
        int callingUid;
        Map.Entry<String, Ops> ent;
        Map<String, Ops> packages;
        int callingPid2;
        int callingUid2;
        SparseIntArray opModes;
        String packageName;
        int callingPid3 = Binder.getCallingPid();
        int callingUid3 = Binder.getCallingUid();
        int reqUserId2 = ActivityManager.handleIncomingUser(callingPid3, callingUid3, reqUserId, true, true, "resetAllModes", null);
        if (reqPackageName != null) {
            try {
                reqUid = AppGlobals.getPackageManager().getPackageUid(reqPackageName, 8192, reqUserId2);
            } catch (RemoteException e) {
            }
            enforceManageAppOpsModes(callingPid3, callingUid3, reqUid);
            synchronized (this) {
                boolean changed = false;
                try {
                    int i = this.mUidStates.size() - 1;
                    callbacks = null;
                    while (i >= 0) {
                        try {
                            uidState = this.mUidStates.valueAt(i);
                            SparseIntArray opModes2 = uidState.opModes;
                            if (opModes2 != null) {
                                if (uidState.uid != reqUid) {
                                    if (reqUid != -1) {
                                        callingUid = callingUid3;
                                        callingPid = callingPid3;
                                    }
                                }
                                int j = opModes2.size() - 1;
                                while (j >= 0) {
                                    int code = opModes2.keyAt(j);
                                    if (AppOpsManager.opAllowsReset(code)) {
                                        opModes2.removeAt(j);
                                        if (opModes2.size() <= 0) {
                                            try {
                                                uidState.opModes = null;
                                            } catch (Throwable th2) {
                                                th = th2;
                                            }
                                        }
                                        String[] packagesForUid = getPackagesForUid(uidState.uid);
                                        int length = packagesForUid.length;
                                        opModes = opModes2;
                                        HashMap<ModeCallback, ArrayList<ChangeRec>> callbacks2 = callbacks;
                                        int i2 = 0;
                                        while (i2 < length) {
                                            try {
                                                packageName = packagesForUid[i2];
                                            } catch (Throwable th3) {
                                                th = th3;
                                                while (true) {
                                                    try {
                                                        break;
                                                    } catch (Throwable th4) {
                                                        th = th4;
                                                    }
                                                }
                                                throw th;
                                            }
                                            try {
                                            } catch (Throwable th5) {
                                                th = th5;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                            try {
                                                callbacks2 = addCallbacks(callbacks2, code, uidState.uid, packageName, this.mOpModeWatchers.get(code));
                                                callbacks2 = addCallbacks(callbacks2, code, uidState.uid, packageName, this.mPackageModeWatchers.get(packageName));
                                                i2++;
                                                packagesForUid = packagesForUid;
                                                callingUid3 = callingUid3;
                                                callingPid3 = callingPid3;
                                            } catch (Throwable th6) {
                                                th = th6;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        }
                                        callingUid2 = callingUid3;
                                        callingPid2 = callingPid3;
                                        callbacks = callbacks2;
                                    } else {
                                        opModes = opModes2;
                                        callingUid2 = callingUid3;
                                        callingPid2 = callingPid3;
                                    }
                                    j--;
                                    opModes2 = opModes;
                                    callingUid3 = callingUid2;
                                    callingPid3 = callingPid2;
                                }
                                callingUid = callingUid3;
                                callingPid = callingPid3;
                            } else {
                                callingUid = callingUid3;
                                callingPid = callingPid3;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                        try {
                            if (uidState.pkgOps != null) {
                                if (reqUserId2 == -1 || reqUserId2 == UserHandle.getUserId(uidState.uid)) {
                                    Map<String, Ops> packages2 = uidState.pkgOps;
                                    Iterator<Map.Entry<String, Ops>> it = packages2.entrySet().iterator();
                                    HashMap<ModeCallback, ArrayList<ChangeRec>> callbacks3 = null;
                                    while (it.hasNext()) {
                                        Map.Entry<String, Ops> ent2 = it.next();
                                        String packageName2 = ent2.getKey();
                                        if (reqPackageName == null || reqPackageName.equals(packageName2)) {
                                            Ops pkgOps = ent2.getValue();
                                            int j2 = pkgOps.size() - 1;
                                            while (j2 >= 0) {
                                                Op curOp = (Op) pkgOps.valueAt(j2);
                                                if (AppOpsManager.opAllowsReset(curOp.op)) {
                                                    packages = packages2;
                                                    if (curOp.mode != AppOpsManager.opToDefaultMode(curOp.op)) {
                                                        curOp.mode = AppOpsManager.opToDefaultMode(curOp.op);
                                                        int uid = curOp.uidState.uid;
                                                        ent = ent2;
                                                        try {
                                                            HashMap<ModeCallback, ArrayList<ChangeRec>> callbacks4 = addCallbacks(addCallbacks(callbacks, curOp.op, uid, packageName2, this.mOpModeWatchers.get(curOp.op)), curOp.op, uid, packageName2, this.mPackageModeWatchers.get(packageName2));
                                                            if (!curOp.hasAnyTime()) {
                                                                pkgOps.removeAt(j2);
                                                            }
                                                            changed = true;
                                                            callbacks = callbacks4;
                                                            callbacks3 = 1;
                                                            j2--;
                                                            packages2 = packages;
                                                            ent2 = ent;
                                                        } catch (Throwable th8) {
                                                            th = th8;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else {
                                                        ent = ent2;
                                                    }
                                                } else {
                                                    packages = packages2;
                                                    ent = ent2;
                                                }
                                                changed = changed;
                                                j2--;
                                                packages2 = packages;
                                                ent2 = ent;
                                            }
                                            if (pkgOps.size() == 0) {
                                                it.remove();
                                            }
                                            changed = changed;
                                            packages2 = packages2;
                                        }
                                    }
                                    if (uidState.isDefault()) {
                                        this.mUidStates.remove(uidState.uid);
                                    }
                                    if (callbacks3 != null) {
                                        uidState.evalForegroundOps(this.mOpModeWatchers);
                                    }
                                }
                            }
                            i--;
                            callingUid3 = callingUid;
                            callingPid3 = callingPid;
                        } catch (Throwable th9) {
                            th = th9;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    if (changed) {
                        scheduleFastWriteLocked();
                    }
                    try {
                    } catch (Throwable th10) {
                        th = th10;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } catch (Throwable th11) {
                    th = th11;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
            if (callbacks != null) {
                Iterator<Map.Entry<ModeCallback, ArrayList<ChangeRec>>> it2 = callbacks.entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry<ModeCallback, ArrayList<ChangeRec>> ent3 = it2.next();
                    ModeCallback cb = ent3.getKey();
                    ArrayList<ChangeRec> reports = ent3.getValue();
                    int i3 = 0;
                    while (i3 < reports.size()) {
                        ChangeRec rep = reports.get(i3);
                        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$FYLTtxqrHmv8Y5UdZ9ybXKsSJhs.INSTANCE, this, cb, Integer.valueOf(rep.op), Integer.valueOf(rep.uid), rep.pkg));
                        i3++;
                        it2 = it2;
                    }
                }
                return;
            }
            return;
        }
        reqUid = -1;
        enforceManageAppOpsModes(callingPid3, callingUid3, reqUid);
        synchronized (this) {
        }
    }

    private void evalAllForegroundOpsLocked() {
        for (int uidi = this.mUidStates.size() - 1; uidi >= 0; uidi--) {
            UidState uidState = this.mUidStates.valueAt(uidi);
            if (uidState.foregroundOps != null) {
                uidState.evalForegroundOps(this.mOpModeWatchers);
            }
        }
    }

    public void startWatchingMode(int op, String packageName, IAppOpsCallback callback) {
        startWatchingModeWithFlags(op, packageName, 0, callback);
    }

    public void startWatchingModeWithFlags(int op, String packageName, int flags, IAppOpsCallback callback) {
        Throwable th;
        int op2;
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        Preconditions.checkArgumentInRange(op, -1, 90, "Invalid op code: " + op);
        if (callback != null) {
            synchronized (this) {
                if (op != -1) {
                    try {
                        op2 = AppOpsManager.opToSwitch(op);
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } else {
                    op2 = op;
                }
                try {
                    ModeCallback cb = this.mModeWatchers.get(callback.asBinder());
                    if (cb == null) {
                        cb = new ModeCallback(callback, -1, flags, callingUid, callingPid);
                        this.mModeWatchers.put(callback.asBinder(), cb);
                    }
                    if (op2 != -1) {
                        ArraySet<ModeCallback> cbs = this.mOpModeWatchers.get(op2);
                        if (cbs == null) {
                            cbs = new ArraySet<>();
                            this.mOpModeWatchers.put(op2, cbs);
                        }
                        cbs.add(cb);
                    }
                    if (packageName != null) {
                        ArraySet<ModeCallback> cbs2 = this.mPackageModeWatchers.get(packageName);
                        if (cbs2 == null) {
                            cbs2 = new ArraySet<>();
                            this.mPackageModeWatchers.put(packageName, cbs2);
                        }
                        cbs2.add(cb);
                    }
                    evalAllForegroundOpsLocked();
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    public void stopWatchingMode(IAppOpsCallback callback) {
        if (callback != null) {
            synchronized (this) {
                ModeCallback cb = this.mModeWatchers.remove(callback.asBinder());
                if (cb != null) {
                    cb.unlinkToDeath();
                    for (int i = this.mOpModeWatchers.size() - 1; i >= 0; i--) {
                        ArraySet<ModeCallback> cbs = this.mOpModeWatchers.valueAt(i);
                        cbs.remove(cb);
                        if (cbs.size() <= 0) {
                            this.mOpModeWatchers.removeAt(i);
                        }
                    }
                    for (int i2 = this.mPackageModeWatchers.size() - 1; i2 >= 0; i2--) {
                        ArraySet<ModeCallback> cbs2 = this.mPackageModeWatchers.valueAt(i2);
                        cbs2.remove(cb);
                        if (cbs2.size() <= 0) {
                            this.mPackageModeWatchers.removeAt(i2);
                        }
                    }
                }
                evalAllForegroundOpsLocked();
            }
        }
    }

    public IBinder getToken(IBinder clientToken) {
        ClientState cs;
        synchronized (this) {
            cs = this.mClients.get(clientToken);
            if (cs == null) {
                cs = new ClientState(clientToken);
                this.mClients.put(clientToken, cs);
            }
        }
        return cs;
    }

    public AppOpsManagerInternal.CheckOpsDelegate getAppOpsServiceDelegate() {
        AppOpsManagerInternal.CheckOpsDelegate checkOpsDelegate;
        synchronized (this) {
            checkOpsDelegate = this.mCheckOpsDelegate;
        }
        return checkOpsDelegate;
    }

    public void setAppOpsServiceDelegate(AppOpsManagerInternal.CheckOpsDelegate delegate) {
        synchronized (this) {
            this.mCheckOpsDelegate = delegate;
        }
    }

    public int checkOperationRaw(int code, int uid, String packageName) {
        return checkOperationInternal(code, uid, packageName, true);
    }

    public int checkOperation(int code, int uid, String packageName) {
        return checkOperationInternal(code, uid, packageName, false);
    }

    private int checkOperationInternal(int code, int uid, String packageName, boolean raw) {
        AppOpsManagerInternal.CheckOpsDelegate delegate;
        synchronized (this) {
            delegate = this.mCheckOpsDelegate;
        }
        if (delegate == null) {
            return checkOperationImpl(code, uid, packageName, raw);
        }
        return delegate.checkOperation(code, uid, packageName, raw, new QuadFunction() {
            /* class com.android.server.appop.$$Lambda$AppOpsService$gQy7GOuCV6GbjQtdNhNG6xld8I4 */

            public final Object apply(Object obj, Object obj2, Object obj3, Object obj4) {
                return Integer.valueOf(AppOpsService.this.checkOperationImpl(((Integer) obj).intValue(), ((Integer) obj2).intValue(), (String) obj3, ((Boolean) obj4).booleanValue()));
            }
        });
    }

    /* access modifiers changed from: private */
    public int checkOperationImpl(int code, int uid, String packageName, boolean raw) {
        verifyIncomingUid(uid);
        verifyIncomingOp(code);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName == null) {
            return 1;
        }
        return checkOperationUnchecked(code, uid, resolvedPackageName, raw);
    }

    private int checkOperationUnchecked(int code, int uid, String packageName, boolean raw) {
        return checkOperationUnchecked(code, uid, packageName, raw, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int checkOperationUnchecked(int code, int uid, String packageName, boolean raw, boolean verify) {
        if (isOpRestrictedDueToSuspend(code, packageName, uid)) {
            return 1;
        }
        synchronized (this) {
            if (verify) {
                checkPackage(uid, packageName);
            }
            if (isOpRestrictedLocked(uid, code, packageName)) {
                return 1;
            }
            int code2 = AppOpsManager.opToSwitch(code);
            UidState uidState = getUidStateLocked(uid, false);
            if (uidState == null || uidState.opModes == null || uidState.opModes.indexOfKey(code2) < 0) {
                Op op = getOpLocked(code2, uid, packageName, false, verify, false);
                if (op == null) {
                    return AppOpsManager.opToDefaultMode(code2);
                }
                return raw ? op.mode : op.evalMode();
            }
            int rawMode = uidState.opModes.get(code2);
            return raw ? rawMode : uidState.evalMode(code2, rawMode);
        }
    }

    public int checkAudioOperation(int code, int usage, int uid, String packageName) {
        AppOpsManagerInternal.CheckOpsDelegate delegate;
        synchronized (this) {
            delegate = this.mCheckOpsDelegate;
        }
        if (delegate == null) {
            return checkAudioOperationImpl(code, usage, uid, packageName);
        }
        return delegate.checkAudioOperation(code, usage, uid, packageName, new QuadFunction() {
            /* class com.android.server.appop.$$Lambda$AppOpsService$mfUWTdGevxEoIUv1cEPEFG0qAaI */

            public final Object apply(Object obj, Object obj2, Object obj3, Object obj4) {
                return Integer.valueOf(AppOpsService.this.checkAudioOperationImpl(((Integer) obj).intValue(), ((Integer) obj2).intValue(), ((Integer) obj3).intValue(), (String) obj4));
            }
        });
    }

    /* access modifiers changed from: private */
    public int checkAudioOperationImpl(int code, int usage, int uid, String packageName) {
        boolean suspended;
        try {
            suspended = isPackageSuspendedForUser(packageName, uid);
        } catch (IllegalArgumentException e) {
            suspended = false;
        }
        if (suspended) {
            Slog.i(TAG, "Audio disabled for suspended package=" + packageName + " for uid=" + uid);
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
        long identity = Binder.clearCallingIdentity();
        try {
            boolean isPackageSuspendedForUser = AppGlobals.getPackageManager().isPackageSuspendedForUser(pkg, UserHandle.getUserId(uid));
            Binder.restoreCallingIdentity(identity);
            return isPackageSuspendedForUser;
        } catch (RemoteException e) {
            throw new SecurityException("Could not talk to package manager service");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private int checkRestrictionLocked(int code, int usage, int uid, String packageName) {
        Restriction r;
        SparseArray<Restriction> usageRestrictions = this.mAudioRestrictions.get(code);
        if (usageRestrictions == null || (r = usageRestrictions.get(usage)) == null || r.exceptionPackages.contains(packageName)) {
            return 0;
        }
        return r.mode;
    }

    public void setAudioRestriction(int code, int usage, int uid, int mode, String[] exceptionPackages) {
        enforceManageAppOpsModes(Binder.getCallingPid(), Binder.getCallingUid(), uid);
        verifyIncomingUid(uid);
        verifyIncomingOp(code);
        synchronized (this) {
            SparseArray<Restriction> usageRestrictions = this.mAudioRestrictions.get(code);
            if (usageRestrictions == null) {
                usageRestrictions = new SparseArray<>();
                this.mAudioRestrictions.put(code, usageRestrictions);
            }
            usageRestrictions.remove(usage);
            if (mode != 0) {
                Restriction r = new Restriction();
                r.mode = mode;
                if (exceptionPackages != null) {
                    int N = exceptionPackages.length;
                    r.exceptionPackages = new ArraySet<>(N);
                    for (String pkg : exceptionPackages) {
                        if (pkg != null) {
                            r.exceptionPackages.add(pkg.trim());
                        }
                    }
                }
                usageRestrictions.put(usage, r);
            }
        }
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$GUeKjlbzT65s86vaxy5gvOajuhw.INSTANCE, this, Integer.valueOf(code), -2));
    }

    public int checkPackage(int uid, String packageName) {
        Preconditions.checkNotNull(packageName);
        synchronized (this) {
            if (getOpsRawLocked(uid, packageName, true, true) != null) {
                return 0;
            }
            return 2;
        }
    }

    public int noteProxyOperation(int code, int proxyUid, String proxyPackageName, int proxiedUid, String proxiedPackageName) {
        int proxyFlags;
        int proxiedFlags;
        verifyIncomingUid(proxyUid);
        verifyIncomingOp(code);
        String resolveProxyPackageName = resolvePackageName(proxyUid, proxyPackageName);
        if (resolveProxyPackageName == null) {
            return 1;
        }
        boolean isProxyTrusted = this.mContext.checkPermission("android.permission.UPDATE_APP_OPS_STATS", -1, proxyUid) == 0;
        if (isProxyTrusted) {
            proxyFlags = 2;
        } else {
            proxyFlags = 4;
        }
        int proxyMode = noteOperationUnchecked(code, proxyUid, resolveProxyPackageName, -1, null, proxyFlags);
        if (proxyMode == 0) {
            if (Binder.getCallingUid() != proxiedUid) {
                String resolveProxiedPackageName = resolvePackageName(proxiedUid, proxiedPackageName);
                if (resolveProxiedPackageName == null) {
                    return 1;
                }
                if (isProxyTrusted) {
                    proxiedFlags = 8;
                } else {
                    proxiedFlags = 16;
                }
                return noteOperationUnchecked(code, proxiedUid, resolveProxiedPackageName, proxyUid, resolveProxyPackageName, proxiedFlags);
            }
        }
        return proxyMode;
    }

    public int noteOperation(int code, int uid, String packageName) {
        AppOpsManagerInternal.CheckOpsDelegate delegate;
        synchronized (this) {
            delegate = this.mCheckOpsDelegate;
        }
        if (delegate == null) {
            return noteOperationImpl(code, uid, packageName);
        }
        return delegate.noteOperation(code, uid, packageName, new TriFunction() {
            /* class com.android.server.appop.$$Lambda$AppOpsService$hqd76gFlOJ1gAuDYDPVUaSkXjTc */

            public final Object apply(Object obj, Object obj2, Object obj3) {
                return Integer.valueOf(AppOpsService.this.noteOperationImpl(((Integer) obj).intValue(), ((Integer) obj2).intValue(), (String) obj3));
            }
        });
    }

    /* access modifiers changed from: private */
    public int noteOperationImpl(int code, int uid, String packageName) {
        verifyIncomingUid(uid);
        verifyIncomingOp(code);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName == null) {
            return 1;
        }
        return noteOperationUnchecked(code, uid, resolvedPackageName, -1, null, 1);
    }

    private int noteOperationUnchecked(int code, int uid, String packageName, int proxyUid, String proxyPackageName, int flags) {
        UidState uidState;
        Op op;
        synchronized (this) {
            Ops ops = getOpsRawLocked(uid, packageName, true, false);
            if (ops == null) {
                scheduleOpNotedIfNeededLocked(code, uid, packageName, 1);
                return 2;
            }
            Op op2 = getOpLocked(ops, code, true);
            if (isOpRestrictedLocked(uid, code, packageName)) {
                scheduleOpNotedIfNeededLocked(code, uid, packageName, 1);
                return 1;
            }
            UidState uidState2 = ops.uidState;
            if (op2.running) {
                AppOpsManager.OpEntry entry = new AppOpsManager.OpEntry(op2.op, op2.running, op2.mode, op2.mAccessTimes, op2.mRejectTimes, op2.mDurations, op2.mProxyUids, op2.mProxyPackageNames);
                Slog.w(TAG, "Noting op not finished: uid " + uid + " pkg " + packageName + " code " + code + " time=" + entry.getLastAccessTime(uidState2.state, uidState2.state, flags) + " duration=" + entry.getLastDuration(uidState2.state, uidState2.state, flags));
            }
            int switchCode = AppOpsManager.opToSwitch(code);
            if (uidState2.opModes == null || uidState2.opModes.indexOfKey(switchCode) < 0) {
                Op switchOp = switchCode != code ? getOpLocked(ops, switchCode, true) : op2;
                int mode = switchOp.evalMode();
                if (switchOp.mode != 0) {
                    op2.rejected(System.currentTimeMillis(), proxyUid, proxyPackageName, uidState2.state, flags);
                    this.mHistoricalRegistry.incrementOpRejected(code, uid, packageName, uidState2.state, flags);
                    scheduleOpNotedIfNeededLocked(code, uid, packageName, mode);
                    return mode;
                }
                uidState = uidState2;
                op = op2;
            } else {
                int uidMode = uidState2.evalMode(code, uidState2.opModes.get(switchCode));
                if (uidMode != 0) {
                    op2.rejected(System.currentTimeMillis(), proxyUid, proxyPackageName, uidState2.state, flags);
                    this.mHistoricalRegistry.incrementOpRejected(code, uid, packageName, uidState2.state, flags);
                    scheduleOpNotedIfNeededLocked(code, uid, packageName, uidMode);
                    return uidMode;
                }
                uidState = uidState2;
                op = op2;
            }
            op.accessed(System.currentTimeMillis(), proxyUid, proxyPackageName, uidState.state, flags);
            this.mHistoricalRegistry.incrementOpAccessedCount(op.op, uid, packageName, uidState.state, flags);
            scheduleOpNotedIfNeededLocked(code, uid, packageName, 0);
            return 0;
        }
    }

    public void startWatchingActive(int[] ops, IAppOpsActiveCallback callback) {
        SparseArray<ActiveCallback> callbacks;
        int watchedUid = -1;
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WATCH_APPOPS") != 0) {
            watchedUid = callingUid;
        }
        if (ops != null) {
            Preconditions.checkArrayElementsInRange(ops, 0, 90, "Invalid op code in: " + Arrays.toString(ops));
        }
        if (callback != null) {
            synchronized (this) {
                SparseArray<ActiveCallback> callbacks2 = this.mActiveWatchers.get(callback.asBinder());
                if (callbacks2 == null) {
                    SparseArray<ActiveCallback> callbacks3 = new SparseArray<>();
                    this.mActiveWatchers.put(callback.asBinder(), callbacks3);
                    callbacks = callbacks3;
                } else {
                    callbacks = callbacks2;
                }
                ActiveCallback activeCallback = new ActiveCallback(callback, watchedUid, callingUid, callingPid);
                for (int op : ops) {
                    callbacks.put(op, activeCallback);
                }
            }
        }
    }

    public void stopWatchingActive(IAppOpsActiveCallback callback) {
        if (callback != null) {
            synchronized (this) {
                SparseArray<ActiveCallback> activeCallbacks = this.mActiveWatchers.remove(callback.asBinder());
                if (activeCallbacks != null) {
                    int callbackCount = activeCallbacks.size();
                    for (int i = 0; i < callbackCount; i++) {
                        activeCallbacks.valueAt(i).destroy();
                    }
                }
            }
        }
    }

    public void startWatchingNoted(int[] ops, IAppOpsNotedCallback callback) {
        SparseArray<NotedCallback> callbacks;
        int watchedUid = -1;
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WATCH_APPOPS") != 0) {
            watchedUid = callingUid;
        }
        Preconditions.checkArgument(!ArrayUtils.isEmpty(ops), "Ops cannot be null or empty");
        Preconditions.checkArrayElementsInRange(ops, 0, 90, "Invalid op code in: " + Arrays.toString(ops));
        Preconditions.checkNotNull(callback, "Callback cannot be null");
        synchronized (this) {
            SparseArray<NotedCallback> callbacks2 = this.mNotedWatchers.get(callback.asBinder());
            if (callbacks2 == null) {
                SparseArray<NotedCallback> callbacks3 = new SparseArray<>();
                this.mNotedWatchers.put(callback.asBinder(), callbacks3);
                callbacks = callbacks3;
            } else {
                callbacks = callbacks2;
            }
            NotedCallback notedCallback = new NotedCallback(callback, watchedUid, callingUid, callingPid);
            for (int op : ops) {
                callbacks.put(op, notedCallback);
            }
        }
    }

    public void stopWatchingNoted(IAppOpsNotedCallback callback) {
        Preconditions.checkNotNull(callback, "Callback cannot be null");
        synchronized (this) {
            SparseArray<NotedCallback> notedCallbacks = this.mNotedWatchers.remove(callback.asBinder());
            if (notedCallbacks != null) {
                int callbackCount = notedCallbacks.size();
                for (int i = 0; i < callbackCount; i++) {
                    notedCallbacks.valueAt(i).destroy();
                }
            }
        }
    }

    public int startOperation(IBinder token, int code, int uid, String packageName, boolean startIfModeDefault) {
        UidState uidState;
        UidState uidState2;
        Op op;
        verifyIncomingUid(uid);
        verifyIncomingOp(code);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName == null) {
            return 1;
        }
        ClientState client = (ClientState) token;
        synchronized (this) {
            try {
                Ops ops = getOpsRawLocked(uid, resolvedPackageName, true, false);
                if (ops == null) {
                    return 2;
                }
                Op op2 = getOpLocked(ops, code, true);
                if (isOpRestrictedLocked(uid, code, resolvedPackageName)) {
                    return 1;
                }
                int switchCode = AppOpsManager.opToSwitch(code);
                UidState uidState3 = ops.uidState;
                int opCode = op2.op;
                if (uidState3.opModes == null || uidState3.opModes.indexOfKey(switchCode) < 0) {
                    int mode = (switchCode != code ? getOpLocked(ops, switchCode, true) : op2).evalMode();
                    if (mode != 0) {
                        if (startIfModeDefault) {
                            if (mode == 3) {
                                uidState = uidState3;
                            }
                        }
                        op2.rejected(System.currentTimeMillis(), -1, null, uidState3.state, 1);
                        this.mHistoricalRegistry.incrementOpRejected(opCode, uid, packageName, uidState3.state, 1);
                        return mode;
                    }
                    uidState = uidState3;
                } else {
                    int uidMode = uidState3.evalMode(code, uidState3.opModes.get(switchCode));
                    if (uidMode != 0) {
                        if (!startIfModeDefault || uidMode != 3) {
                            op2.rejected(System.currentTimeMillis(), -1, null, uidState3.state, 1);
                            this.mHistoricalRegistry.incrementOpRejected(opCode, uid, packageName, uidState3.state, 1);
                            return uidMode;
                        }
                    }
                    uidState = uidState3;
                }
                if (op2.startNesting == 0) {
                    op2.startRealtime = SystemClock.elapsedRealtime();
                    uidState2 = uidState;
                    op2.started(System.currentTimeMillis(), uidState2.state, 1);
                    op = op2;
                    this.mHistoricalRegistry.incrementOpAccessedCount(opCode, uid, packageName, uidState2.state, 1);
                    scheduleOpActiveChangedIfNeededLocked(code, uid, packageName, true);
                } else {
                    op = op2;
                    uidState2 = uidState;
                }
                op.startNesting++;
                uidState2.startNesting++;
                if (client.mStartedOps != null) {
                    client.mStartedOps.add(op);
                }
                return 0;
            } catch (Throwable th) {
                th = th;
                throw th;
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
                Op op = getOpLocked(code, uid, resolvedPackageName, true, true, false);
                if (op != null) {
                    if (!client.mStartedOps.remove(op)) {
                        long identity = Binder.clearCallingIdentity();
                        try {
                            if (((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getPackageUid(resolvedPackageName, 0, UserHandle.getUserId(uid)) < 0) {
                                Slog.i(TAG, "Finishing op=" + AppOpsManager.opToName(code) + " for non-existing package=" + resolvedPackageName + " in uid=" + uid);
                                return;
                            }
                            Binder.restoreCallingIdentity(identity);
                            Slog.wtf(TAG, "Operation not started: uid=" + op.uidState.uid + " pkg=" + op.packageName + " op=" + AppOpsManager.opToName(op.op));
                        } finally {
                            Binder.restoreCallingIdentity(identity);
                        }
                    } else {
                        finishOperationLocked(op, false);
                        if (op.startNesting <= 0) {
                            scheduleOpActiveChangedIfNeededLocked(code, uid, packageName, false);
                        }
                    }
                }
            }
        }
    }

    private void scheduleOpActiveChangedIfNeededLocked(int code, int uid, String packageName, boolean active) {
        ArraySet<ActiveCallback> dispatchedCallbacks = null;
        int callbackListCount = this.mActiveWatchers.size();
        for (int i = 0; i < callbackListCount; i++) {
            ActiveCallback callback = this.mActiveWatchers.valueAt(i).get(code);
            if (callback != null && (callback.mWatchingUid < 0 || callback.mWatchingUid == uid)) {
                if (dispatchedCallbacks == null) {
                    dispatchedCallbacks = new ArraySet<>();
                }
                dispatchedCallbacks.add(callback);
            }
        }
        if (dispatchedCallbacks != null) {
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$ac4Ra3Yhj0OQzvkaL2dLbsuLAmQ.INSTANCE, this, dispatchedCallbacks, Integer.valueOf(code), Integer.valueOf(uid), packageName, Boolean.valueOf(active)));
        }
    }

    /* access modifiers changed from: private */
    public void notifyOpActiveChanged(ArraySet<ActiveCallback> callbacks, int code, int uid, String packageName, boolean active) {
        long identity = Binder.clearCallingIdentity();
        try {
            int callbackCount = callbacks.size();
            for (int i = 0; i < callbackCount; i++) {
                try {
                    callbacks.valueAt(i).mCallback.opActiveChanged(code, uid, packageName, active);
                } catch (RemoteException e) {
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void scheduleOpNotedIfNeededLocked(int code, int uid, String packageName, int result) {
        ArraySet<NotedCallback> dispatchedCallbacks = null;
        int callbackListCount = this.mNotedWatchers.size();
        for (int i = 0; i < callbackListCount; i++) {
            NotedCallback callback = this.mNotedWatchers.valueAt(i).get(code);
            if (callback != null && (callback.mWatchingUid < 0 || callback.mWatchingUid == uid)) {
                if (dispatchedCallbacks == null) {
                    dispatchedCallbacks = new ArraySet<>();
                }
                dispatchedCallbacks.add(callback);
            }
        }
        if (dispatchedCallbacks != null) {
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$AfBLuTvVESlqN91IyRX84hMV5nE.INSTANCE, this, dispatchedCallbacks, Integer.valueOf(code), Integer.valueOf(uid), packageName, Integer.valueOf(result)));
        }
    }

    /* access modifiers changed from: private */
    public void notifyOpChecked(ArraySet<NotedCallback> callbacks, int code, int uid, String packageName, int result) {
        long identity = Binder.clearCallingIdentity();
        try {
            int callbackCount = callbacks.size();
            for (int i = 0; i < callbackCount; i++) {
                try {
                    callbacks.valueAt(i).mCallback.opNoted(code, uid, packageName, result);
                } catch (RemoteException e) {
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int permissionToOpCode(String permission) {
        if (permission == null) {
            return -1;
        }
        return AppOpsManager.permissionToOpCode(permission);
    }

    /* access modifiers changed from: package-private */
    public void finishOperationLocked(Op op, boolean finishNested) {
        int i;
        int opCode = op.op;
        int uid = op.uidState.uid;
        if (op.startNesting <= 1 || finishNested) {
            if (op.startNesting == 1 || finishNested) {
                long duration = SystemClock.elapsedRealtime() - op.startRealtime;
                op.finished(System.currentTimeMillis(), duration, op.uidState.state, 1);
                i = 1;
                this.mHistoricalRegistry.increaseOpAccessDuration(opCode, uid, op.packageName, op.uidState.state, 1, duration);
            } else {
                AppOpsManager.OpEntry entry = new AppOpsManager.OpEntry(op.op, op.running, op.mode, op.mAccessTimes, op.mRejectTimes, op.mDurations, op.mProxyUids, op.mProxyPackageNames);
                Slog.w(TAG, "Finishing op nesting under-run: uid " + uid + " pkg " + op.packageName + " code " + opCode + " time=" + entry.getLastAccessTime(31) + " duration=" + entry.getLastDuration(100, 700, 31) + " nesting=" + op.startNesting);
                i = 1;
            }
            if (op.startNesting >= i) {
                op.uidState.startNesting -= op.startNesting;
            }
            op.startNesting = 0;
            return;
        }
        op.startNesting--;
        op.uidState.startNesting--;
    }

    private void verifyIncomingUid(int uid) {
        if (uid != Binder.getCallingUid() && Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        }
    }

    private void verifyIncomingOp(int op) {
        if (op < 0 || op >= 91) {
            throw new IllegalArgumentException("Bad operation #" + op);
        }
    }

    private UidState getUidStateLocked(int uid, boolean edit) {
        UidState uidState = this.mUidStates.get(uid);
        if (uidState != null) {
            updatePendingStateIfNeededLocked(uidState);
            return uidState;
        } else if (!edit) {
            return null;
        } else {
            UidState uidState2 = new UidState(uid);
            this.mUidStates.put(uid, uidState2);
            return uidState2;
        }
    }

    private void updatePendingStateIfNeededLocked(UidState uidState) {
        if (uidState != null && uidState.pendingStateCommitTime != 0) {
            if (uidState.pendingStateCommitTime < this.mLastRealtime) {
                commitUidPendingStateLocked(uidState);
                return;
            }
            this.mLastRealtime = SystemClock.elapsedRealtime();
            if (uidState.pendingStateCommitTime < this.mLastRealtime) {
                commitUidPendingStateLocked(uidState);
            }
        }
    }

    private void commitUidPendingStateLocked(UidState uidState) {
        ModeCallback callback;
        int pkgi;
        int cbi;
        ArraySet<ModeCallback> callbacks;
        AppOpsService appOpsService = this;
        if (uidState.hasForegroundWatchers) {
            boolean z = true;
            int fgi = uidState.foregroundOps.size() - 1;
            while (fgi >= 0) {
                if (uidState.foregroundOps.valueAt(fgi)) {
                    int code = uidState.foregroundOps.keyAt(fgi);
                    long firstUnrestrictedUidState = (long) AppOpsManager.resolveFirstUnrestrictedUidState(code);
                    boolean resolvedNowFg = false;
                    boolean resolvedLastFg = ((long) uidState.state) <= firstUnrestrictedUidState ? z : false;
                    if (((long) uidState.pendingState) <= firstUnrestrictedUidState) {
                        boolean z2 = z ? 1 : 0;
                        Object[] objArr = z ? 1 : 0;
                        Object[] objArr2 = z ? 1 : 0;
                        Object[] objArr3 = z ? 1 : 0;
                        resolvedNowFg = z2;
                    }
                    if (resolvedLastFg != resolvedNowFg) {
                        int i = 4;
                        if (uidState.opModes == null || uidState.opModes.indexOfKey(code) < 0 || uidState.opModes.get(code) != 4) {
                            ArraySet<ModeCallback> callbacks2 = appOpsService.mOpModeWatchers.get(code);
                            if (callbacks2 != null) {
                                int size = callbacks2.size();
                                int i2 = z ? 1 : 0;
                                int i3 = z ? 1 : 0;
                                int i4 = z ? 1 : 0;
                                int cbi2 = size - i2;
                                while (cbi2 >= 0) {
                                    ModeCallback callback2 = callbacks2.valueAt(cbi2);
                                    int i5 = callback2.mFlags;
                                    int i6 = z ? 1 : 0;
                                    char c = z ? 1 : 0;
                                    char c2 = z ? 1 : 0;
                                    char c3 = z ? 1 : 0;
                                    if ((i5 & i6) != 0) {
                                        if (callback2.isWatchingUid(uidState.uid)) {
                                            int size2 = uidState.pkgOps.size();
                                            int i7 = z ? 1 : 0;
                                            int i8 = z ? 1 : 0;
                                            int pkgi2 = size2 - i7;
                                            while (pkgi2 >= 0) {
                                                Op op = (Op) uidState.pkgOps.valueAt(pkgi2).get(code);
                                                if (op == null) {
                                                    pkgi = pkgi2;
                                                    callback = callback2;
                                                    cbi = cbi2;
                                                    callbacks = callbacks2;
                                                } else if (op.mode == i) {
                                                    pkgi = pkgi2;
                                                    callback = callback2;
                                                    cbi = cbi2;
                                                    callbacks = callbacks2;
                                                    appOpsService.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$FYLTtxqrHmv8Y5UdZ9ybXKsSJhs.INSTANCE, this, callback2, Integer.valueOf(code), Integer.valueOf(uidState.uid), uidState.pkgOps.keyAt(pkgi2)));
                                                } else {
                                                    pkgi = pkgi2;
                                                    callback = callback2;
                                                    cbi = cbi2;
                                                    callbacks = callbacks2;
                                                }
                                                pkgi2 = pkgi - 1;
                                                i = 4;
                                                appOpsService = this;
                                                callbacks2 = callbacks;
                                                cbi2 = cbi;
                                                callback2 = callback;
                                            }
                                        }
                                    }
                                    cbi2--;
                                    i = 4;
                                    appOpsService = this;
                                    callbacks2 = callbacks2;
                                    z = true;
                                }
                            }
                        } else {
                            appOpsService.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$u9c0eEYUUm25QC1meV06FHffZE0.INSTANCE, appOpsService, Integer.valueOf(code), Integer.valueOf(uidState.uid), Boolean.valueOf(z)));
                        }
                    }
                }
                fgi--;
                z = true;
                appOpsService = this;
            }
        }
        uidState.state = uidState.pendingState;
        uidState.pendingStateCommitTime = 0;
    }

    private Ops getOpsRawLocked(int uid, String packageName, boolean edit, boolean uidMismatchExpected) {
        UidState uidState = getUidStateLocked(uid, edit);
        if (uidState == null) {
            return null;
        }
        if (uidState.pkgOps == null) {
            if (!edit) {
                return null;
            }
            uidState.pkgOps = new ArrayMap<>();
        }
        Ops ops = uidState.pkgOps.get(packageName);
        if (ops != null) {
            return ops;
        }
        if (!edit) {
            return null;
        }
        boolean isPrivileged = false;
        if (uid != 0) {
            long ident = Binder.clearCallingIdentity();
            int pkgUid = -1;
            try {
                ApplicationInfo appInfo = ActivityThread.getPackageManager().getApplicationInfo(packageName, 786432, UserHandle.getUserId(uid));
                if (appInfo != null) {
                    pkgUid = appInfo.uid;
                    isPrivileged = (appInfo.privateFlags & 8) != 0;
                } else {
                    pkgUid = resolveUid(packageName);
                    if (pkgUid >= 0) {
                        isPrivileged = false;
                    }
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Could not contact PackageManager", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
            if (pkgUid != uid) {
                if (!uidMismatchExpected) {
                    RuntimeException ex = new RuntimeException("here");
                    ex.fillInStackTrace();
                    Slog.w(TAG, "Bad call: specified package " + packageName + " under uid " + uid + " but it is really " + pkgUid, ex);
                }
                Binder.restoreCallingIdentity(ident);
                return null;
            }
            Binder.restoreCallingIdentity(ident);
        }
        Ops ops2 = new Ops(packageName, uidState, isPrivileged);
        uidState.pkgOps.put(packageName, ops2);
        return ops2;
    }

    private Ops getOpsRawNoVerifyLocked(int uid, String packageName, boolean edit, boolean isPrivileged) {
        UidState uidState = getUidStateLocked(uid, edit);
        if (uidState == null) {
            return null;
        }
        if (uidState.pkgOps == null) {
            if (!edit) {
                return null;
            }
            uidState.pkgOps = new ArrayMap<>();
        }
        Ops ops = uidState.pkgOps.get(packageName);
        if (ops != null) {
            return ops;
        }
        if (!edit) {
            return null;
        }
        Ops ops2 = new Ops(packageName, uidState, isPrivileged);
        uidState.pkgOps.put(packageName, ops2);
        return ops2;
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

    private Op getOpLocked(int code, int uid, String packageName, boolean edit, boolean verifyUid, boolean isPrivileged) {
        Ops ops;
        if (verifyUid) {
            ops = getOpsRawLocked(uid, packageName, edit, false);
        } else {
            ops = getOpsRawNoVerifyLocked(uid, packageName, edit, isPrivileged);
        }
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
            op = new Op(ops.uidState, ops.packageName, code);
            ops.put(code, op);
        }
        if (edit) {
            scheduleWriteLocked();
        }
        return op;
    }

    private boolean isOpRestrictedDueToSuspend(int code, String packageName, int uid) {
        if (!ArrayUtils.contains(OPS_RESTRICTED_ON_SUSPEND, code)) {
            return false;
        }
        return ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).isPackageSuspended(packageName, UserHandle.getUserId(uid));
    }

    private boolean isOpRestrictedLocked(int uid, int code, String packageName) {
        int userHandle = UserHandle.getUserId(uid);
        int restrictionSetCount = this.mOpUserRestrictions.size();
        for (int i = 0; i < restrictionSetCount; i++) {
            if (this.mOpUserRestrictions.valueAt(i).hasRestriction(code, packageName, userHandle)) {
                if (AppOpsManager.opAllowSystemBypassRestriction(code)) {
                    synchronized (this) {
                        Ops ops = getOpsRawLocked(uid, packageName, true, false);
                        if (ops != null && ops.isPrivileged) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void readState() {
        FileInputStream stream;
        XmlPullParser parser;
        int type;
        int oldVersion = -1;
        synchronized (this.mFile) {
            synchronized (this) {
                try {
                    stream = this.mFile.openRead();
                    this.mUidStates.clear();
                    try {
                        parser = Xml.newPullParser();
                        parser.setInput(stream, StandardCharsets.UTF_8.name());
                    } catch (IllegalStateException e) {
                        Slog.w(TAG, "Failed parsing " + e);
                        if (0 == 0) {
                            this.mUidStates.clear();
                        }
                        stream.close();
                    } catch (NullPointerException e2) {
                        Slog.w(TAG, "Failed parsing " + e2);
                        if (0 == 0) {
                            this.mUidStates.clear();
                        }
                        stream.close();
                    } catch (NumberFormatException e3) {
                        Slog.w(TAG, "Failed parsing " + e3);
                        if (0 == 0) {
                            this.mUidStates.clear();
                        }
                        stream.close();
                    } catch (XmlPullParserException e4) {
                        Slog.w(TAG, "Failed parsing " + e4);
                        if (0 == 0) {
                            this.mUidStates.clear();
                        }
                        stream.close();
                    } catch (IOException e5) {
                        Slog.w(TAG, "Failed parsing " + e5);
                        if (0 == 0) {
                            this.mUidStates.clear();
                        }
                        stream.close();
                    } catch (IndexOutOfBoundsException e6) {
                        Slog.w(TAG, "Failed parsing " + e6);
                        if (0 == 0) {
                            this.mUidStates.clear();
                        }
                        stream.close();
                    } catch (Throwable th) {
                        if (0 == 0) {
                            this.mUidStates.clear();
                        }
                        try {
                            stream.close();
                        } catch (IOException e7) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e8) {
                    Slog.i(TAG, "No existing app ops " + this.mFile.getBaseFile() + "; starting empty");
                    return;
                }
                while (true) {
                    type = parser.next();
                    if (type == 2 || type == 1) {
                        break;
                    }
                }
                if (type == 2) {
                    String versionString = parser.getAttributeValue(null, "v");
                    if (versionString != null) {
                        oldVersion = Integer.parseInt(versionString);
                    }
                    int outerDepth = parser.getDepth();
                    while (true) {
                        int type2 = parser.next();
                        if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                            break;
                        } else if (type2 != 3) {
                            if (type2 != 4) {
                                String tagName = parser.getName();
                                if (tagName.equals("pkg")) {
                                    readPackage(parser);
                                } else if (tagName.equals(WatchlistLoggingHandler.WatchlistEventKeys.UID)) {
                                    readUidOps(parser);
                                } else {
                                    Slog.w(TAG, "Unknown element under <app-ops>: " + parser.getName());
                                    XmlUtils.skipCurrentTag(parser);
                                }
                            }
                        }
                    }
                    if (1 == 0) {
                        this.mUidStates.clear();
                    }
                    try {
                        stream.close();
                    } catch (IOException e9) {
                    }
                } else {
                    throw new IllegalStateException("no start tag found");
                }
            }
        }
        synchronized (this) {
            upgradeLocked(oldVersion);
        }
    }

    private void upgradeRunAnyInBackgroundLocked() {
        Op op;
        int idx;
        for (int i = 0; i < this.mUidStates.size(); i++) {
            UidState uidState = this.mUidStates.valueAt(i);
            if (uidState != null) {
                if (uidState.opModes != null && (idx = uidState.opModes.indexOfKey(63)) >= 0) {
                    uidState.opModes.put(70, uidState.opModes.valueAt(idx));
                }
                if (uidState.pkgOps != null) {
                    boolean changed = false;
                    for (int j = 0; j < uidState.pkgOps.size(); j++) {
                        Ops ops = uidState.pkgOps.valueAt(j);
                        if (!(ops == null || (op = (Op) ops.get(63)) == null || op.mode == AppOpsManager.opToDefaultMode(op.op))) {
                            Op copy = new Op(op.uidState, op.packageName, 70);
                            copy.mode = op.mode;
                            ops.put(70, copy);
                            changed = true;
                        }
                    }
                    if (changed) {
                        uidState.evalForegroundOps(this.mOpModeWatchers);
                    }
                }
            }
        }
    }

    private void upgradeLocked(int oldVersion) {
        if (oldVersion < 1) {
            Slog.d(TAG, "Upgrading app-ops xml from version " + oldVersion + " to 1");
            if (oldVersion == -1) {
                upgradeRunAnyInBackgroundLocked();
            }
            scheduleFastWriteLocked();
        }
    }

    private void readUidOps(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
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

    private void readPackage(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
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
                if (parser.getName().equals(WatchlistLoggingHandler.WatchlistEventKeys.UID)) {
                    readUid(parser, pkgName);
                } else {
                    Slog.w(TAG, "Unknown element under <pkg>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    private void readUid(XmlPullParser parser, String pkgName) throws NumberFormatException, XmlPullParserException, IOException {
        int uid = Integer.parseInt(parser.getAttributeValue(null, "n"));
        UidState uidState = getUidStateLocked(uid, true);
        String isPrivilegedString = parser.getAttributeValue(null, "p");
        boolean isPrivileged = false;
        if (isPrivilegedString == null) {
            try {
                if (ActivityThread.getPackageManager() != null) {
                    boolean z = false;
                    ApplicationInfo appInfo = ActivityThread.getPackageManager().getApplicationInfo(pkgName, 0, UserHandle.getUserId(uid));
                    if (appInfo != null) {
                        if ((appInfo.privateFlags & 8) != 0) {
                            z = true;
                        }
                        isPrivileged = z;
                    }
                } else {
                    return;
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Could not contact PackageManager", e);
            }
        } else {
            isPrivileged = Boolean.parseBoolean(isPrivilegedString);
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                break;
            } else if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("op")) {
                    readOp(parser, uidState, pkgName, isPrivileged);
                } else {
                    Slog.w(TAG, "Unknown element under <pkg>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        uidState.evalForegroundOps(this.mOpModeWatchers);
    }

    private void readOp(XmlPullParser parser, UidState uidState, String pkgName, boolean isPrivileged) throws NumberFormatException, XmlPullParserException, IOException {
        long j;
        Op op = new Op(uidState, pkgName, Integer.parseInt(parser.getAttributeValue(null, "n")));
        op.mode = XmlUtils.readIntAttribute(parser, "m", AppOpsManager.opToDefaultMode(op.op));
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                break;
            } else if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("st")) {
                    long key = XmlUtils.readLongAttribute(parser, "n");
                    int flags = AppOpsManager.extractFlagsFromKey(key);
                    int state = AppOpsManager.extractUidStateFromKey(key);
                    long accessTime = XmlUtils.readLongAttribute(parser, "t", 0);
                    long rejectTime = XmlUtils.readLongAttribute(parser, "r", 0);
                    long accessDuration = XmlUtils.readLongAttribute(parser, "d", 0);
                    String proxyPkg = XmlUtils.readStringAttribute(parser, "pp");
                    int proxyUid = XmlUtils.readIntAttribute(parser, "pu", 0);
                    if (accessTime > 0) {
                        j = 0;
                        op.accessed(accessTime, proxyUid, proxyPkg, state, flags);
                    } else {
                        j = 0;
                    }
                    if (rejectTime > j) {
                        op.rejected(rejectTime, proxyUid, proxyPkg, state, flags);
                    }
                    if (accessDuration > j) {
                        op.running(accessTime, accessDuration, state, flags);
                    }
                } else {
                    Slog.w(TAG, "Unknown element under <op>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        if (uidState.pkgOps == null) {
            uidState.pkgOps = new ArrayMap<>();
        }
        Ops ops = uidState.pkgOps.get(pkgName);
        if (ops == null) {
            ops = new Ops(pkgName, uidState, isPrivileged);
            uidState.pkgOps.put(pkgName, ops);
        }
        ops.put(op.op, op);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r0v38 'proxyPkg'  java.lang.String: [D('proxyPkg' java.lang.String), D('flags' int)] */
    /* JADX WARN: Type inference failed for: r11v3 */
    /* JADX WARN: Type inference failed for: r11v4, types: [int] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x02e4 A[SYNTHETIC, Splitter:B:136:0x02e4] */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x030c A[SYNTHETIC, Splitter:B:146:0x030c] */
    public void writeState() {
        Throwable th;
        String str;
        String str2;
        IOException e;
        IOException e2;
        List<AppOpsManager.PackageOps> allOps;
        Throwable th2;
        String lastPkg;
        List<AppOpsManager.PackageOps> allOps2;
        String lastPkg2;
        List<AppOpsManager.PackageOps> allOps3;
        String proxyPkg;
        synchronized (this.mFile) {
            try {
                FileOutputStream stream = this.mFile.startWrite();
                String str3 = null;
                List<AppOpsManager.PackageOps> allOps4 = getPackagesForOps(null);
                try {
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(stream, StandardCharsets.UTF_8.name());
                    out.startDocument(null, true);
                    out.startTag(null, "app-ops");
                    out.attribute(null, "v", String.valueOf(1));
                    synchronized (this) {
                        try {
                            int uidStateCount = this.mUidStates.size();
                            for (int i = 0; i < uidStateCount; i++) {
                                try {
                                    UidState uidState = this.mUidStates.valueAt(i);
                                    if (uidState.opModes != null && uidState.opModes.size() > 0) {
                                        out.startTag(null, WatchlistLoggingHandler.WatchlistEventKeys.UID);
                                        out.attribute(null, "n", Integer.toString(uidState.uid));
                                        SparseIntArray uidOpModes = uidState.opModes;
                                        int opCount = uidOpModes.size();
                                        for (int j = 0; j < opCount; j++) {
                                            int op = uidOpModes.keyAt(j);
                                            int mode = uidOpModes.valueAt(j);
                                            out.startTag(null, "op");
                                            out.attribute(null, "n", Integer.toString(op));
                                            out.attribute(null, "m", Integer.toString(mode));
                                            out.endTag(null, "op");
                                        }
                                        out.endTag(null, WatchlistLoggingHandler.WatchlistEventKeys.UID);
                                    }
                                } catch (Throwable th3) {
                                    e2 = th3;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th4) {
                                            e2 = th4;
                                        }
                                    }
                                    throw e2;
                                }
                            }
                        } catch (Throwable th5) {
                            e2 = th5;
                            while (true) {
                                break;
                            }
                            throw e2;
                        }
                    }
                    if (allOps4 != null) {
                        String lastPkg3 = null;
                        boolean z = false;
                        int i2 = 0;
                        while (i2 < allOps4.size()) {
                            AppOpsManager.PackageOps pkg = allOps4.get(i2);
                            if (pkg.getPackageName() == null) {
                                allOps = allOps4;
                            } else {
                                if (!pkg.getPackageName().equals(lastPkg3)) {
                                    if (lastPkg3 != null) {
                                        try {
                                            out.endTag(str3, "pkg");
                                        } catch (IOException e3) {
                                            e = e3;
                                            try {
                                                Slog.w(TAG, "Failed to write state, restoring backup.", e);
                                                this.mFile.failWrite(stream);
                                                if (stream != null) {
                                                }
                                            } catch (Throwable th6) {
                                                th = th6;
                                                if (stream != null) {
                                                    try {
                                                        stream.close();
                                                    } catch (IOException e4) {
                                                        Slog.w(TAG, "Failed to close stream: " + e4.getMessage());
                                                    }
                                                }
                                                throw th;
                                            }
                                        } catch (Throwable th7) {
                                            th = th7;
                                            if (stream != null) {
                                            }
                                            throw th;
                                        }
                                    }
                                    lastPkg3 = pkg.getPackageName();
                                    out.startTag(str3, "pkg");
                                    out.attribute(str3, "n", lastPkg3);
                                }
                                out.startTag(str3, WatchlistLoggingHandler.WatchlistEventKeys.UID);
                                out.attribute(str3, "n", Integer.toString(pkg.getUid()));
                                synchronized (this) {
                                    try {
                                        Ops ops = getOpsRawLocked(pkg.getUid(), pkg.getPackageName(), z, z);
                                        if (ops != null) {
                                            try {
                                                out.attribute(str3, "p", Boolean.toString(ops.isPrivileged));
                                            } catch (Throwable th8) {
                                                th2 = th8;
                                            }
                                        } else {
                                            out.attribute(str3, "p", Boolean.toString(z));
                                        }
                                    } catch (Throwable th9) {
                                        th2 = th9;
                                        while (true) {
                                            try {
                                                break;
                                            } catch (Throwable th10) {
                                                th2 = th10;
                                            }
                                        }
                                        throw th2;
                                    }
                                }
                                List<AppOpsManager.OpEntry> ops2 = pkg.getOps();
                                int j2 = z;
                                while (j2 < ops2.size()) {
                                    AppOpsManager.OpEntry op2 = ops2.get(j2 == true ? 1 : 0);
                                    out.startTag(str3, "op");
                                    out.attribute(str3, "n", Integer.toString(op2.getOp()));
                                    if (op2.getMode() != AppOpsManager.opToDefaultMode(op2.getOp())) {
                                        out.attribute(str3, "m", Integer.toString(op2.getMode()));
                                    }
                                    LongSparseArray keys = op2.collectKeys();
                                    if (keys == null) {
                                        allOps2 = allOps4;
                                        lastPkg = lastPkg3;
                                    } else if (keys.size() <= 0) {
                                        allOps2 = allOps4;
                                        lastPkg = lastPkg3;
                                    } else {
                                        int keyCount = keys.size();
                                        int k = 0;
                                        while (k < keyCount) {
                                            long key = keys.keyAt(k);
                                            int uidState2 = AppOpsManager.extractUidStateFromKey(key);
                                            int flags = AppOpsManager.extractFlagsFromKey(key);
                                            long accessTime = op2.getLastAccessTime(uidState2, uidState2, flags);
                                            long rejectTime = op2.getLastRejectTime(uidState2, uidState2, flags);
                                            long accessDuration = op2.getLastDuration(uidState2, uidState2, flags);
                                            String proxyPkg2 = op2.getProxyPackageName(uidState2, flags);
                                            int proxyUid = op2.getProxyUid(uidState2, flags);
                                            if (accessTime > 0 || rejectTime > 0 || accessDuration > 0) {
                                                proxyPkg = proxyPkg2;
                                            } else {
                                                proxyPkg = proxyPkg2;
                                                if (proxyPkg == null && proxyUid < 0) {
                                                    allOps3 = allOps4;
                                                    lastPkg2 = lastPkg3;
                                                    k++;
                                                    allOps4 = allOps3;
                                                    lastPkg3 = lastPkg2;
                                                }
                                            }
                                            allOps3 = allOps4;
                                            lastPkg2 = lastPkg3;
                                            try {
                                                out.startTag(null, "st");
                                                out.attribute(null, "n", Long.toString(key));
                                                if (accessTime > 0) {
                                                    out.attribute(null, "t", Long.toString(accessTime));
                                                }
                                                if (rejectTime > 0) {
                                                    out.attribute(null, "r", Long.toString(rejectTime));
                                                }
                                                if (accessDuration > 0) {
                                                    out.attribute(null, "d", Long.toString(accessDuration));
                                                }
                                                if (proxyPkg != null) {
                                                    out.attribute(null, "pp", proxyPkg);
                                                }
                                                if (proxyUid >= 0) {
                                                    out.attribute(null, "pu", Integer.toString(proxyUid));
                                                }
                                                out.endTag(null, "st");
                                                k++;
                                                allOps4 = allOps3;
                                                lastPkg3 = lastPkg2;
                                            } catch (IOException e5) {
                                                e = e5;
                                                Slog.w(TAG, "Failed to write state, restoring backup.", e);
                                                this.mFile.failWrite(stream);
                                                if (stream != null) {
                                                }
                                            }
                                        }
                                        allOps2 = allOps4;
                                        lastPkg = lastPkg3;
                                        out.endTag(null, "op");
                                        allOps4 = allOps2;
                                        lastPkg3 = lastPkg;
                                        str3 = null;
                                        j2++;
                                    }
                                    out.endTag(null, "op");
                                    allOps4 = allOps2;
                                    lastPkg3 = lastPkg;
                                    str3 = null;
                                    j2++;
                                }
                                allOps = allOps4;
                                out.endTag(null, WatchlistLoggingHandler.WatchlistEventKeys.UID);
                                lastPkg3 = lastPkg3;
                            }
                            i2++;
                            allOps4 = allOps;
                            str3 = null;
                            z = false;
                        }
                        if (lastPkg3 != null) {
                            out.endTag(null, "pkg");
                        }
                    }
                    out.endTag(null, "app-ops");
                    out.endDocument();
                    this.mFile.finishWrite(stream);
                    out.flush();
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e6) {
                            str2 = TAG;
                            str = "Failed to close stream: " + e6.getMessage();
                        }
                    }
                } catch (IOException e7) {
                    e = e7;
                    Slog.w(TAG, "Failed to write state, restoring backup.", e);
                    this.mFile.failWrite(stream);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e8) {
                            str2 = TAG;
                            str = "Failed to close stream: " + e8.getMessage();
                        }
                    }
                } catch (Throwable th11) {
                    th = th11;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (IOException e9) {
                Slog.w(TAG, "Failed to write state: " + e9);
                return;
            }
        }
        Slog.w(str2, str);
    }

    /* access modifiers changed from: package-private */
    public static class Shell extends ShellCommand {
        static final Binder sBinder = new Binder();
        final IAppOpsService mInterface;
        final AppOpsService mInternal;
        IBinder mToken;
        int mode;
        String modeStr;
        int nonpackageUid;
        int op;
        String opStr;
        String packageName;
        int packageUid;
        boolean targetsUid;
        int userId = 0;

        Shell(IAppOpsService iface, AppOpsService internal) {
            this.mInterface = iface;
            this.mInternal = internal;
            try {
                this.mToken = this.mInterface.getToken(sBinder);
            } catch (RemoteException e) {
            }
        }

        public int onCommand(String cmd) {
            return AppOpsService.onShellCommand(this, cmd);
        }

        public void onHelp() {
            AppOpsService.dumpCommandHelp(getOutPrintWriter());
        }

        /* access modifiers changed from: private */
        public static int strOpToOp(String op2, PrintWriter err) {
            try {
                return AppOpsManager.strOpToOp(op2);
            } catch (IllegalArgumentException e) {
                try {
                    return Integer.parseInt(op2);
                } catch (NumberFormatException e2) {
                    try {
                        return AppOpsManager.strDebugOpToOp(op2);
                    } catch (IllegalArgumentException e3) {
                        err.println("Error: " + e3.getMessage());
                        return -1;
                    }
                }
            }
        }

        static int strModeToMode(String modeStr2, PrintWriter err) {
            for (int i = AppOpsManager.MODE_NAMES.length - 1; i >= 0; i--) {
                if (AppOpsManager.MODE_NAMES[i].equals(modeStr2)) {
                    return i;
                }
            }
            try {
                return Integer.parseInt(modeStr2);
            } catch (NumberFormatException e) {
                err.println("Error: Mode " + modeStr2 + " is not valid");
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public int parseUserOpMode(int defMode, PrintWriter err) throws RemoteException {
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
            String str = this.opStr;
            if (str == null) {
                err.println("Error: Operation not specified.");
                return -1;
            }
            this.op = strOpToOp(str, err);
            if (this.op < 0) {
                return -1;
            }
            String str2 = this.modeStr;
            if (str2 != null) {
                int strModeToMode = strModeToMode(str2, err);
                this.mode = strModeToMode;
                if (strModeToMode < 0) {
                    return -1;
                }
                return 0;
            }
            this.mode = defMode;
            return 0;
        }

        /* access modifiers changed from: package-private */
        public int parseUserPackageOp(boolean reqOp, PrintWriter err) throws RemoteException {
            this.userId = -2;
            this.packageName = null;
            this.opStr = null;
            while (true) {
                String argument = getNextArg();
                if (argument == null) {
                    break;
                } else if ("--user".equals(argument)) {
                    this.userId = UserHandle.parseUserArg(getNextArgRequired());
                } else if ("--uid".equals(argument)) {
                    this.targetsUid = true;
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
            } else if (this.opStr != null || !reqOp) {
                String str = this.opStr;
                if (str != null) {
                    this.op = strOpToOp(str, err);
                    if (this.op < 0) {
                        return -1;
                    }
                } else {
                    this.op = -1;
                }
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
                            int i2 = i + 1;
                            while (i2 < this.packageName.length() && this.packageName.charAt(i2) >= '0' && this.packageName.charAt(i2) <= '9') {
                                i2++;
                            }
                            if (i2 > i2) {
                                try {
                                    int typeVal = Integer.parseInt(this.packageName.substring(i2, i2));
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
                    this.packageUid = AppOpsService.resolveUid(this.packageName);
                    if (this.packageUid < 0) {
                        this.packageUid = AppGlobals.getPackageManager().getPackageUid(this.packageName, 8192, this.userId);
                    }
                    if (this.packageUid < 0) {
                        err.println("Error: No UID for " + this.packageName + " in user " + this.userId);
                        return -1;
                    }
                }
                return 0;
            } else {
                err.println("Error: Operation not specified.");
                return -1;
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.appop.AppOpsService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new Shell(this, this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    static void dumpCommandHelp(PrintWriter pw) {
        pw.println("AppOps service (appops) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  start [--user <USER_ID>] <PACKAGE | UID> <OP> ");
        pw.println("    Starts a given operation for a particular application.");
        pw.println("  stop [--user <USER_ID>] <PACKAGE | UID> <OP> ");
        pw.println("    Stops a given operation for a particular application.");
        pw.println("  set [--user <USER_ID>] <[--uid] PACKAGE | UID> <OP> <MODE>");
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
        pw.println("    <PACKAGE> an Android package name or its UID if prefixed by --uid");
        pw.println("    <OP>      an AppOps operation.");
        pw.println("    <MODE>    one of allow, ignore, deny, or default");
        pw.println("    <USER_ID> the user id under which the package is installed. If --user is not");
        pw.println("              specified, the current user is assumed.");
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    static int onShellCommand(Shell shell, String cmd) {
        char c;
        List<AppOpsManager.PackageOps> ops;
        if (cmd == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        PrintWriter err = shell.getErrPrintWriter();
        try {
            int i = 0;
            switch (cmd.hashCode()) {
                case -1703718319:
                    if (cmd.equals("write-settings")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -1166702330:
                    if (cmd.equals("query-op")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 102230:
                    if (cmd.equals("get")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 113762:
                    if (cmd.equals("set")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 3540994:
                    if (cmd.equals("stop")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 108404047:
                    if (cmd.equals("reset")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 109757538:
                    if (cmd.equals("start")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 2085703290:
                    if (cmd.equals("read-settings")) {
                        c = 5;
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
                    int res = shell.parseUserPackageOp(true, err);
                    if (res < 0) {
                        return res;
                    }
                    String modeStr = shell.getNextArg();
                    if (modeStr == null) {
                        err.println("Error: Mode not specified.");
                        return -1;
                    }
                    int mode = Shell.strModeToMode(modeStr, err);
                    if (mode < 0) {
                        return -1;
                    }
                    if (!shell.targetsUid && shell.packageName != null) {
                        shell.mInterface.setMode(shell.op, shell.packageUid, shell.packageName, mode);
                        return 0;
                    } else if (!shell.targetsUid || shell.packageName == null) {
                        shell.mInterface.setUidMode(shell.op, shell.nonpackageUid, mode);
                        return 0;
                    } else {
                        try {
                            shell.mInterface.setUidMode(shell.op, shell.mInternal.mContext.getPackageManager().getPackageUid(shell.packageName, shell.userId), mode);
                            return 0;
                        } catch (PackageManager.NameNotFoundException e) {
                            return -1;
                        }
                    }
                case 1:
                    int res2 = shell.parseUserPackageOp(false, err);
                    if (res2 < 0) {
                        return res2;
                    }
                    List<AppOpsManager.PackageOps> ops2 = new ArrayList<>();
                    int[] iArr = null;
                    if (shell.packageName != null) {
                        List<AppOpsManager.PackageOps> r = shell.mInterface.getUidOps(shell.packageUid, shell.op != -1 ? new int[]{shell.op} : null);
                        if (r != null) {
                            ops2.addAll(r);
                        }
                        IAppOpsService iAppOpsService = shell.mInterface;
                        int i2 = shell.packageUid;
                        String str = shell.packageName;
                        if (shell.op != -1) {
                            iArr = new int[]{shell.op};
                        }
                        List<AppOpsManager.PackageOps> r2 = iAppOpsService.getOpsForPackage(i2, str, iArr);
                        if (r2 != null) {
                            ops2.addAll(r2);
                        }
                    } else {
                        IAppOpsService iAppOpsService2 = shell.mInterface;
                        int i3 = shell.nonpackageUid;
                        if (shell.op != -1) {
                            iArr = new int[]{shell.op};
                        }
                        ops2 = iAppOpsService2.getUidOps(i3, iArr);
                    }
                    if (ops2 != null) {
                        if (ops2.size() > 0) {
                            long now = System.currentTimeMillis();
                            int i4 = 0;
                            while (i4 < ops2.size()) {
                                AppOpsManager.PackageOps packageOps = ops2.get(i4);
                                if (packageOps.getPackageName() == null) {
                                    pw.print("Uid mode: ");
                                }
                                List<AppOpsManager.OpEntry> entries = packageOps.getOps();
                                int j = i;
                                while (j < entries.size()) {
                                    AppOpsManager.OpEntry ent = entries.get(j);
                                    pw.print(AppOpsManager.opToName(ent.getOp()));
                                    pw.print(": ");
                                    pw.print(AppOpsManager.modeToName(ent.getMode()));
                                    if (ent.getTime() != 0) {
                                        pw.print("; time=");
                                        ops = ops2;
                                        TimeUtils.formatDuration(now - ent.getTime(), pw);
                                        pw.print(" ago");
                                    } else {
                                        ops = ops2;
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
                                        TimeUtils.formatDuration(ent.getDuration(), pw);
                                    }
                                    pw.println();
                                    j++;
                                    ops2 = ops;
                                }
                                i4++;
                                i = 0;
                            }
                            return 0;
                        }
                    }
                    pw.println("No operations.");
                    if (shell.op <= -1 || shell.op >= 91) {
                        return 0;
                    }
                    pw.println("Default mode: " + AppOpsManager.modeToName(AppOpsManager.opToDefaultMode(shell.op)));
                    return 0;
                case 2:
                    int res3 = shell.parseUserOpMode(1, err);
                    if (res3 < 0) {
                        return res3;
                    }
                    List<AppOpsManager.PackageOps> ops3 = shell.mInterface.getPackagesForOps(new int[]{shell.op});
                    if (ops3 == null || ops3.size() <= 0) {
                        pw.println("No operations.");
                        return 0;
                    }
                    for (int i5 = 0; i5 < ops3.size(); i5++) {
                        AppOpsManager.PackageOps pkg = ops3.get(i5);
                        boolean hasMatch = false;
                        List<AppOpsManager.OpEntry> entries2 = ops3.get(i5).getOps();
                        int j2 = 0;
                        while (true) {
                            if (j2 < entries2.size()) {
                                AppOpsManager.OpEntry ent2 = entries2.get(j2);
                                if (ent2.getOp() == shell.op && ent2.getMode() == shell.mode) {
                                    hasMatch = true;
                                } else {
                                    j2++;
                                }
                            }
                        }
                        if (hasMatch) {
                            pw.println(pkg.getPackageName());
                        }
                    }
                    return 0;
                case 3:
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
                case 4:
                    shell.mInternal.enforceManageAppOpsModes(Binder.getCallingPid(), Binder.getCallingUid(), -1);
                    long token = Binder.clearCallingIdentity();
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
                case 5:
                    shell.mInternal.enforceManageAppOpsModes(Binder.getCallingPid(), Binder.getCallingUid(), -1);
                    long token2 = Binder.clearCallingIdentity();
                    try {
                        shell.mInternal.readState();
                        pw.println("Last settings read.");
                        return 0;
                    } finally {
                        Binder.restoreCallingIdentity(token2);
                    }
                case 6:
                    int res4 = shell.parseUserPackageOp(true, err);
                    if (res4 < 0) {
                        return res4;
                    }
                    if (shell.packageName == null) {
                        return -1;
                    }
                    shell.mInterface.startOperation(shell.mToken, shell.op, shell.packageUid, shell.packageName, true);
                    return 0;
                case 7:
                    int res5 = shell.parseUserPackageOp(true, err);
                    if (res5 < 0) {
                        return res5;
                    }
                    if (shell.packageName == null) {
                        return -1;
                    }
                    shell.mInterface.finishOperation(shell.mToken, shell.op, shell.packageUid, shell.packageName);
                    return 0;
                default:
                    return shell.handleDefaultCommands(cmd);
            }
        } catch (RemoteException e2) {
            pw.println("Remote exception: " + e2);
            return -1;
        }
    }

    private void dumpHelp(PrintWriter pw) {
        pw.println("AppOps service (appops) dump options:");
        pw.println("  -h");
        pw.println("    Print this help text.");
        pw.println("  --op [OP]");
        pw.println("    Limit output to data associated with the given app op code.");
        pw.println("  --mode [MODE]");
        pw.println("    Limit output to data associated with the given app op mode.");
        pw.println("  --package [PACKAGE]");
        pw.println("    Limit output to data associated with the given package name.");
        pw.println("  --watchers");
        pw.println("    Only output the watcher sections.");
    }

    private void dumpStatesLocked(PrintWriter pw, Op op, long now, SimpleDateFormat sdf, Date date, String prefix) {
        String str;
        String str2;
        String str3 = prefix;
        AppOpsManager.OpEntry entry = new AppOpsManager.OpEntry(op.op, op.running, op.mode, op.mAccessTimes, op.mRejectTimes, op.mDurations, op.mProxyUids, op.mProxyPackageNames);
        LongSparseArray keys = entry.collectKeys();
        if (keys == null) {
            return;
        }
        if (keys.size() > 0) {
            int keyCount = keys.size();
            int proxyUid = 0;
            while (proxyUid < keyCount) {
                long key = keys.keyAt(proxyUid);
                int uidState = AppOpsManager.extractUidStateFromKey(key);
                int flags = AppOpsManager.extractFlagsFromKey(key);
                long accessTime = entry.getLastAccessTime(uidState, uidState, flags);
                long rejectTime = entry.getLastRejectTime(uidState, uidState, flags);
                long accessDuration = entry.getLastDuration(uidState, uidState, flags);
                String proxyPkg = entry.getProxyPackageName(uidState, flags);
                int proxyUid2 = entry.getProxyUid(uidState, flags);
                if (accessTime > 0) {
                    pw.print(str3);
                    pw.print("Access: ");
                    pw.print(AppOpsManager.keyToString(key));
                    pw.print(" ");
                    date.setTime(accessTime);
                    pw.print(sdf.format(date));
                    pw.print(" (");
                    str = " (";
                    TimeUtils.formatDuration(accessTime - now, pw);
                    pw.print(")");
                    if (accessDuration > 0) {
                        pw.print(" duration=");
                        TimeUtils.formatDuration(accessDuration, pw);
                    }
                    if (proxyUid2 >= 0) {
                        pw.print(" proxy[");
                        pw.print("uid=");
                        pw.print(proxyUid2);
                        pw.print(", pkg=");
                        pw.print(proxyPkg);
                        str2 = "]";
                        pw.print(str2);
                    } else {
                        str2 = "]";
                    }
                    pw.println();
                } else {
                    str = " (";
                    str2 = "]";
                }
                if (rejectTime > 0) {
                    pw.print(prefix);
                    pw.print("Reject: ");
                    pw.print(AppOpsManager.keyToString(key));
                    date.setTime(rejectTime);
                    pw.print(sdf.format(date));
                    pw.print(str);
                    TimeUtils.formatDuration(rejectTime - now, pw);
                    pw.print(")");
                    if (proxyUid2 >= 0) {
                        pw.print(" proxy[");
                        pw.print("uid=");
                        pw.print(proxyUid2);
                        pw.print(", pkg=");
                        pw.print(proxyPkg);
                        pw.print(str2);
                    }
                    pw.println();
                }
                proxyUid++;
                str3 = prefix;
                keys = keys;
                keyCount = keyCount;
                entry = entry;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r4v35 int: [D('opCode' int), D('pkgOps' android.util.ArrayMap<java.lang.String, com.android.server.appop.AppOpsService$Ops>)] */
    /* JADX INFO: Multiple debug info for r6v32 'sdf'  java.text.SimpleDateFormat: [D('sdf' java.text.SimpleDateFormat), D('switchOp' int)] */
    /* JADX INFO: Multiple debug info for r4v39 'now'  long: [D('opCode' int), D('now' long)] */
    /* JADX INFO: Multiple debug info for r26v16 'now'  long: [D('now' long), D('nowElapsed' long)] */
    /* JADX INFO: Multiple debug info for r5v20 'mode'  int: [D('mode' int), D('nowElapsed' long)] */
    /* JADX INFO: Multiple debug info for r1v56 'opModeCount'  int: [D('opModeCount' int), D('code' int)] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x035e, code lost:
        if (r1 != android.os.UserHandle.getAppId(r24.mWatchingUid)) goto L_0x0361;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:438:0x08c6, code lost:
        if (r3 != r2.mode) goto L_0x08c9;
     */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        boolean dumpWatchers;
        int dumpOp;
        String dumpPackage;
        int dumpUid;
        Throwable th;
        boolean needSep;
        long now;
        int dumpUid2;
        SimpleDateFormat sdf;
        Date date;
        long now2;
        int userRestrictionCount;
        ClientRestrictionState restrictionState;
        int excludedPackageCount;
        boolean hasPackage;
        SimpleDateFormat sdf2;
        Date date2;
        int restrictedOpCount;
        boolean[] restrictedOps;
        int dumpMode;
        boolean dumpWatchers2;
        long now3;
        boolean dumpHistory;
        int dumpOp2;
        String dumpPackage2;
        long now4;
        boolean needSep2;
        SimpleDateFormat sdf3;
        long nowElapsed;
        long nowElapsed2;
        int dumpMode2;
        UidState uidState;
        long now5;
        ArrayMap<String, Ops> pkgOps;
        int dumpOp3;
        String dumpPackage3;
        SimpleDateFormat sdf4;
        long now6;
        int dumpMode3;
        UidState uidState2;
        long nowElapsed3;
        int j;
        Ops ops;
        int dumpOp4;
        String dumpPackage4;
        SimpleDateFormat sdf5;
        long now7;
        int j2;
        boolean printedPackage;
        int mode;
        long nowElapsed4;
        int opModeCount;
        int code;
        int mode2;
        boolean hasPackage2;
        boolean hasOp;
        boolean hasOp2;
        boolean needSep3;
        boolean hasOp3;
        SparseArray<Restriction> restrictions;
        boolean needSep4;
        int dumpUid3;
        boolean needSep5;
        boolean printedHeader;
        long now8;
        boolean needSep6;
        boolean needSep7;
        ArraySet<ModeCallback> callbacks;
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, pw)) {
            String dumpPackage5 = null;
            int dumpUid4 = -1;
            boolean dumpOp5 = false;
            if (args != null) {
                int i = 0;
                boolean dumpWatchers3 = false;
                int dumpMode4 = -1;
                int dumpUid5 = -1;
                int dumpOp6 = -1;
                while (i < args.length) {
                    String arg = args[i];
                    if ("-h".equals(arg)) {
                        dumpHelp(pw);
                        return;
                    }
                    if (!"-a".equals(arg)) {
                        if ("--op".equals(arg)) {
                            i++;
                            if (i >= args.length) {
                                pw.println("No argument for --op option");
                                return;
                            }
                            int dumpOp7 = Shell.strOpToOp(args[i], pw);
                            if (dumpOp7 >= 0) {
                                dumpOp6 = dumpOp7;
                            } else {
                                return;
                            }
                        } else if ("--package".equals(arg)) {
                            i++;
                            if (i >= args.length) {
                                pw.println("No argument for --package option");
                                return;
                            }
                            dumpPackage5 = args[i];
                            try {
                                dumpUid5 = AppGlobals.getPackageManager().getPackageUid(dumpPackage5, 12591104, 0);
                            } catch (RemoteException e) {
                            }
                            if (dumpUid5 < 0) {
                                pw.println("Unknown package: " + dumpPackage5);
                                return;
                            }
                            dumpUid5 = UserHandle.getAppId(dumpUid5);
                        } else if ("--mode".equals(arg)) {
                            i++;
                            if (i >= args.length) {
                                pw.println("No argument for --mode option");
                                return;
                            }
                            int dumpMode5 = Shell.strModeToMode(args[i], pw);
                            if (dumpMode5 >= 0) {
                                dumpMode4 = dumpMode5;
                            } else {
                                return;
                            }
                        } else if ("--watchers".equals(arg)) {
                            dumpWatchers3 = true;
                        } else if (arg.length() <= 0 || arg.charAt(0) != '-') {
                            pw.println("Unknown command: " + arg);
                            return;
                        } else {
                            pw.println("Unknown option: " + arg);
                            return;
                        }
                    }
                    i++;
                }
                dumpOp = dumpOp6;
                dumpWatchers = dumpWatchers3;
                dumpPackage = dumpPackage5;
                dumpUid = dumpUid5;
                dumpUid4 = dumpMode4;
            } else {
                dumpOp = -1;
                dumpWatchers = false;
                dumpPackage = null;
                dumpUid = -1;
            }
            synchronized (this) {
                try {
                    pw.println("Current AppOps Service state:");
                    if (0 == 0 && !dumpWatchers) {
                        try {
                            this.mConstants.dump(pw);
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                    pw.println();
                    long now9 = System.currentTimeMillis();
                    long nowElapsed5 = SystemClock.elapsedRealtime();
                    SystemClock.uptimeMillis();
                    long nowElapsed6 = nowElapsed5;
                    SimpleDateFormat sdf6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    Date date3 = new Date();
                    if (dumpOp < 0 && dumpUid4 < 0 && dumpPackage == null && this.mProfileOwners != null && !dumpWatchers && 0 == 0) {
                        pw.println("  Profile owners:");
                        for (int poi = 0; poi < this.mProfileOwners.size(); poi++) {
                            pw.print("    User #");
                            pw.print(this.mProfileOwners.keyAt(poi));
                            pw.print(": ");
                            UserHandle.formatUid(pw, this.mProfileOwners.valueAt(poi));
                            pw.println();
                        }
                        pw.println();
                    }
                    if (this.mOpModeWatchers.size() <= 0 || 0 != 0) {
                        needSep = false;
                    } else {
                        boolean j3 = false;
                        needSep = false;
                        for (int i2 = 0; i2 < this.mOpModeWatchers.size(); i2++) {
                            if (dumpOp < 0 || dumpOp == this.mOpModeWatchers.keyAt(i2)) {
                                boolean printedOpHeader = false;
                                ArraySet<ModeCallback> callbacks2 = this.mOpModeWatchers.valueAt(i2);
                                boolean printedHeader2 = j3;
                                int j4 = 0;
                                while (true) {
                                    needSep7 = needSep;
                                    if (j4 >= callbacks2.size()) {
                                        break;
                                    }
                                    ModeCallback cb = callbacks2.valueAt(j4);
                                    if (dumpPackage != null) {
                                        callbacks = callbacks2;
                                        if (dumpUid != UserHandle.getAppId(cb.mWatchingUid)) {
                                            needSep = needSep7;
                                            j4++;
                                            callbacks2 = callbacks;
                                        }
                                    } else {
                                        callbacks = callbacks2;
                                    }
                                    if (!printedHeader2) {
                                        needSep7 = true;
                                        pw.println("  Op mode watchers:");
                                        printedHeader2 = true;
                                    } else {
                                        needSep7 = true;
                                    }
                                    if (!printedOpHeader) {
                                        pw.print("    Op ");
                                        pw.print(AppOpsManager.opToName(this.mOpModeWatchers.keyAt(i2)));
                                        pw.println(":");
                                        printedOpHeader = true;
                                    }
                                    pw.print("      #");
                                    pw.print(j4);
                                    pw.print(": ");
                                    pw.println(cb);
                                    needSep = needSep7;
                                    j4++;
                                    callbacks2 = callbacks;
                                }
                                j3 = printedHeader2;
                                needSep = needSep7;
                            }
                        }
                    }
                    if (this.mPackageModeWatchers.size() > 0 && dumpOp < 0 && 0 == 0) {
                        boolean printedHeader3 = false;
                        for (int i3 = 0; i3 < this.mPackageModeWatchers.size(); i3++) {
                            if (dumpPackage == null || dumpPackage.equals(this.mPackageModeWatchers.keyAt(i3))) {
                                boolean needSep8 = true;
                                if (!printedHeader3) {
                                    pw.println("  Package mode watchers:");
                                    printedHeader3 = true;
                                }
                                pw.print("    Pkg ");
                                pw.print(this.mPackageModeWatchers.keyAt(i3));
                                pw.println(":");
                                ArraySet<ModeCallback> callbacks3 = this.mPackageModeWatchers.valueAt(i3);
                                int j5 = 0;
                                while (j5 < callbacks3.size()) {
                                    pw.print("      #");
                                    pw.print(j5);
                                    pw.print(": ");
                                    pw.println(callbacks3.valueAt(j5));
                                    j5++;
                                    needSep8 = needSep8;
                                }
                                needSep = needSep8;
                                printedHeader3 = printedHeader3;
                            }
                        }
                    }
                    if (this.mModeWatchers.size() > 0 && dumpOp < 0 && 0 == 0) {
                        boolean printedHeader4 = false;
                        for (int i4 = 0; i4 < this.mModeWatchers.size(); i4++) {
                            ModeCallback cb2 = this.mModeWatchers.valueAt(i4);
                            if (dumpPackage != null) {
                                if (dumpUid != UserHandle.getAppId(cb2.mWatchingUid)) {
                                    needSep = needSep;
                                }
                            }
                            needSep = true;
                            if (!printedHeader4) {
                                pw.println("  All op mode watchers:");
                                printedHeader4 = true;
                            }
                            pw.print("    ");
                            pw.print(Integer.toHexString(System.identityHashCode(this.mModeWatchers.keyAt(i4))));
                            pw.print(": ");
                            pw.println(cb2);
                            printedHeader4 = printedHeader4;
                        }
                    }
                    if (this.mActiveWatchers.size() <= 0 || dumpUid4 >= 0) {
                        now = now9;
                    } else {
                        needSep = true;
                        boolean printedHeader5 = false;
                        int watcherNum = 0;
                        while (watcherNum < this.mActiveWatchers.size()) {
                            SparseArray<ActiveCallback> activeWatchers = this.mActiveWatchers.valueAt(watcherNum);
                            if (activeWatchers.size() <= 0) {
                                needSep6 = needSep;
                            } else {
                                ActiveCallback cb3 = activeWatchers.valueAt(0);
                                if (dumpOp < 0 || activeWatchers.indexOfKey(dumpOp) >= 0) {
                                    if (dumpPackage != null) {
                                        needSep6 = needSep;
                                    } else {
                                        needSep6 = needSep;
                                    }
                                    if (!printedHeader5) {
                                        pw.println("  All op active watchers:");
                                        printedHeader5 = true;
                                    }
                                    pw.print("    ");
                                    pw.print(Integer.toHexString(System.identityHashCode(this.mActiveWatchers.keyAt(watcherNum))));
                                    pw.println(" ->");
                                    pw.print("        [");
                                    int opCount = activeWatchers.size();
                                    now8 = now9;
                                    for (int opNum = 0; opNum < opCount; opNum++) {
                                        if (opNum > 0) {
                                            pw.print(' ');
                                        }
                                        pw.print(AppOpsManager.opToName(activeWatchers.keyAt(opNum)));
                                        if (opNum < opCount - 1) {
                                            pw.print(',');
                                        }
                                    }
                                    pw.println("]");
                                    pw.print("        ");
                                    pw.println(cb3);
                                    watcherNum++;
                                    needSep = needSep6;
                                    now9 = now8;
                                } else {
                                    needSep6 = needSep;
                                }
                            }
                            now8 = now9;
                            watcherNum++;
                            needSep = needSep6;
                            now9 = now8;
                        }
                        now = now9;
                    }
                    if (this.mNotedWatchers.size() > 0 && dumpUid4 < 0) {
                        needSep = true;
                        boolean printedHeader6 = false;
                        int i5 = 0;
                        while (i5 < this.mNotedWatchers.size()) {
                            SparseArray<NotedCallback> notedWatchers = this.mNotedWatchers.valueAt(i5);
                            if (notedWatchers.size() > 0) {
                                NotedCallback cb4 = notedWatchers.valueAt(0);
                                if ((dumpOp < 0 || notedWatchers.indexOfKey(dumpOp) >= 0) && (dumpPackage == null || dumpUid == UserHandle.getAppId(cb4.mWatchingUid))) {
                                    if (!printedHeader6) {
                                        pw.println("  All op noted watchers:");
                                        printedHeader6 = true;
                                    }
                                    pw.print("    ");
                                    pw.print(Integer.toHexString(System.identityHashCode(this.mNotedWatchers.keyAt(i5))));
                                    pw.println(" ->");
                                    pw.print("        [");
                                    int opCount2 = notedWatchers.size();
                                    i5 = 0;
                                    while (i5 < opCount2) {
                                        if (i5 > 0) {
                                            printedHeader = printedHeader6;
                                            pw.print(' ');
                                        } else {
                                            printedHeader = printedHeader6;
                                        }
                                        pw.print(AppOpsManager.opToName(notedWatchers.keyAt(i5)));
                                        if (i5 < opCount2 - 1) {
                                            pw.print(',');
                                        }
                                        i5++;
                                        printedHeader6 = printedHeader;
                                    }
                                    pw.println("]");
                                    pw.print("        ");
                                    pw.println(cb4);
                                    printedHeader6 = printedHeader6;
                                }
                            }
                            i5++;
                        }
                    }
                    if (this.mClients.size() <= 0 || dumpUid4 >= 0 || dumpWatchers || 0 != 0) {
                        dumpUid2 = dumpUid;
                    } else {
                        needSep = true;
                        boolean j6 = false;
                        int i6 = 0;
                        while (i6 < this.mClients.size()) {
                            try {
                                boolean printedClient = false;
                                ClientState cs = this.mClients.valueAt(i6);
                                if (cs.mStartedOps.size() > 0) {
                                    boolean printedStarted = false;
                                    boolean printedHeader7 = j6;
                                    int j7 = 0;
                                    while (true) {
                                        dumpUid3 = dumpUid;
                                        try {
                                            if (j7 >= cs.mStartedOps.size()) {
                                                break;
                                            }
                                            Op op = cs.mStartedOps.get(j7);
                                            if (dumpOp >= 0) {
                                                needSep5 = needSep;
                                                if (op.op != dumpOp) {
                                                    j7++;
                                                    dumpUid = dumpUid3;
                                                    needSep = needSep5;
                                                }
                                            } else {
                                                needSep5 = needSep;
                                            }
                                            if (dumpPackage == null || dumpPackage.equals(op.packageName)) {
                                                if (!printedHeader7) {
                                                    pw.println("  Clients:");
                                                    printedHeader7 = true;
                                                }
                                                if (!printedClient) {
                                                    pw.print("    ");
                                                    pw.print(this.mClients.keyAt(i6));
                                                    pw.println(":");
                                                    pw.print("      ");
                                                    pw.println(cs);
                                                    printedClient = true;
                                                }
                                                if (!printedStarted) {
                                                    pw.println("      Started ops:");
                                                    printedStarted = true;
                                                }
                                                pw.print("        ");
                                                pw.print("uid=");
                                                pw.print(op.uidState.uid);
                                                pw.print(" pkg=");
                                                pw.print(op.packageName);
                                                pw.print(" op=");
                                                pw.println(AppOpsManager.opToName(op.op));
                                                j7++;
                                                dumpUid = dumpUid3;
                                                needSep = needSep5;
                                            } else {
                                                j7++;
                                                dumpUid = dumpUid3;
                                                needSep = needSep5;
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                            throw th;
                                        }
                                    }
                                    needSep4 = needSep;
                                    j6 = printedHeader7;
                                } else {
                                    dumpUid3 = dumpUid;
                                    needSep4 = needSep;
                                }
                                i6++;
                                dumpUid = dumpUid3;
                                needSep = needSep4;
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        }
                        dumpUid2 = dumpUid;
                    }
                    try {
                        if (this.mAudioRestrictions.size() > 0 && dumpOp < 0 && dumpPackage != null && dumpUid4 < 0 && !dumpWatchers && !dumpWatchers) {
                            boolean printedHeader8 = false;
                            for (int o = 0; o < this.mAudioRestrictions.size(); o++) {
                                String op2 = AppOpsManager.opToName(this.mAudioRestrictions.keyAt(o));
                                SparseArray<Restriction> restrictions2 = this.mAudioRestrictions.valueAt(o);
                                int i7 = 0;
                                while (i7 < restrictions2.size()) {
                                    if (!printedHeader8) {
                                        pw.println("  Audio Restrictions:");
                                        printedHeader8 = true;
                                        needSep = true;
                                    }
                                    int usage = restrictions2.keyAt(i7);
                                    pw.print("    ");
                                    pw.print(op2);
                                    pw.print(" usage=");
                                    pw.print(AudioAttributes.usageToString(usage));
                                    Restriction r = restrictions2.valueAt(i7);
                                    pw.print(": mode=");
                                    pw.println(AppOpsManager.modeToName(r.mode));
                                    if (!r.exceptionPackages.isEmpty()) {
                                        pw.println("      Exceptions:");
                                        int j8 = 0;
                                        while (true) {
                                            restrictions = restrictions2;
                                            if (j8 >= r.exceptionPackages.size()) {
                                                break;
                                            }
                                            pw.print("        ");
                                            pw.println(r.exceptionPackages.valueAt(j8));
                                            j8++;
                                            restrictions2 = restrictions;
                                        }
                                    } else {
                                        restrictions = restrictions2;
                                    }
                                    i7++;
                                    printedHeader8 = printedHeader8;
                                    op2 = op2;
                                    restrictions2 = restrictions;
                                }
                            }
                        }
                        if (needSep) {
                            pw.println();
                        }
                        int i8 = 0;
                        while (i8 < this.mUidStates.size()) {
                            UidState uidState3 = this.mUidStates.valueAt(i8);
                            SparseIntArray opModes = uidState3.opModes;
                            ArrayMap<String, Ops> pkgOps2 = uidState3.pkgOps;
                            if (dumpWatchers) {
                                dumpMode = dumpUid4;
                                dumpHistory = dumpOp5;
                                needSep2 = needSep;
                                dumpWatchers2 = dumpWatchers;
                                now4 = now;
                                dumpPackage2 = dumpPackage;
                                dumpOp2 = dumpOp;
                                now3 = nowElapsed6;
                            } else if (dumpOp5) {
                                dumpPackage2 = dumpPackage;
                                dumpMode = dumpUid4;
                                dumpHistory = dumpOp5;
                                needSep2 = needSep;
                                dumpWatchers2 = dumpWatchers;
                                now4 = now;
                                dumpOp2 = dumpOp;
                                now3 = nowElapsed6;
                            } else {
                                if (dumpOp >= 0 || dumpPackage != null || dumpUid4 >= 0) {
                                    boolean hasOp4 = dumpOp < 0 || (uidState3.opModes != null && uidState3.opModes.indexOfKey(dumpOp) >= 0);
                                    boolean hasPackage3 = dumpPackage == null;
                                    boolean hasMode = dumpUid4 < 0;
                                    if (hasMode || opModes == null) {
                                        hasOp = hasOp4;
                                        hasPackage2 = hasPackage3;
                                    } else {
                                        hasOp = hasOp4;
                                        int opi = 0;
                                        while (true) {
                                            if (hasMode) {
                                                hasPackage2 = hasPackage3;
                                                break;
                                            }
                                            hasPackage2 = hasPackage3;
                                            if (opi >= opModes.size()) {
                                                break;
                                            }
                                            if (opModes.valueAt(opi) == dumpUid4) {
                                                hasMode = true;
                                            }
                                            opi++;
                                            hasPackage3 = hasPackage2;
                                        }
                                    }
                                    if (pkgOps2 != null) {
                                        dumpHistory = dumpOp5;
                                        boolean hasPackage4 = hasPackage2;
                                        int pkgi = 0;
                                        hasOp2 = hasOp;
                                        while (true) {
                                            if (hasOp2 && hasPackage4 && hasMode) {
                                                needSep2 = needSep;
                                                dumpWatchers2 = dumpWatchers;
                                                break;
                                            }
                                            dumpWatchers2 = dumpWatchers;
                                            try {
                                                if (pkgi >= pkgOps2.size()) {
                                                    needSep2 = needSep;
                                                    break;
                                                }
                                                Ops ops2 = pkgOps2.valueAt(pkgi);
                                                if (!hasOp2 && ops2 != null && ops2.indexOfKey(dumpOp) >= 0) {
                                                    hasOp2 = true;
                                                }
                                                if (!hasMode) {
                                                    hasOp3 = hasOp2;
                                                    int opi2 = 0;
                                                    while (true) {
                                                        if (hasMode) {
                                                            needSep3 = needSep;
                                                            break;
                                                        }
                                                        needSep3 = needSep;
                                                        if (opi2 >= ops2.size()) {
                                                            break;
                                                        }
                                                        if (((Op) ops2.valueAt(opi2)).mode == dumpUid4) {
                                                            hasMode = true;
                                                        }
                                                        opi2++;
                                                        needSep = needSep3;
                                                    }
                                                } else {
                                                    hasOp3 = hasOp2;
                                                    needSep3 = needSep;
                                                }
                                                if (!hasPackage4 && dumpPackage.equals(ops2.packageName)) {
                                                    hasPackage4 = true;
                                                }
                                                pkgi++;
                                                hasOp2 = hasOp3;
                                                dumpWatchers = dumpWatchers2;
                                                needSep = needSep3;
                                            } catch (Throwable th5) {
                                                th = th5;
                                                throw th;
                                            }
                                        }
                                        hasPackage2 = hasPackage4;
                                    } else {
                                        dumpHistory = dumpOp5;
                                        needSep2 = needSep;
                                        dumpWatchers2 = dumpWatchers;
                                        hasOp2 = hasOp;
                                    }
                                    try {
                                        if (uidState3.foregroundOps != null && !hasOp2 && uidState3.foregroundOps.indexOfKey(dumpOp) > 0) {
                                            hasOp2 = true;
                                        }
                                        if (!hasOp2 || !hasPackage2) {
                                            dumpMode = dumpUid4;
                                            dumpOp2 = dumpOp;
                                            now4 = now;
                                            dumpPackage2 = dumpPackage;
                                            now3 = nowElapsed6;
                                        } else if (!hasMode) {
                                            dumpPackage2 = dumpPackage;
                                            dumpMode = dumpUid4;
                                            dumpOp2 = dumpOp;
                                            now4 = now;
                                            now3 = nowElapsed6;
                                        }
                                    } catch (Throwable th6) {
                                        th = th6;
                                        throw th;
                                    }
                                } else {
                                    dumpHistory = dumpOp5;
                                    dumpWatchers2 = dumpWatchers;
                                }
                                pw.print("  Uid ");
                                UserHandle.formatUid(pw, uidState3.uid);
                                pw.println(":");
                                pw.print("    state=");
                                pw.println(AppOpsManager.getUidStateName(uidState3.state));
                                if (uidState3.state != uidState3.pendingState) {
                                    pw.print("    pendingState=");
                                    pw.println(AppOpsManager.getUidStateName(uidState3.pendingState));
                                }
                                if (uidState3.pendingStateCommitTime != 0) {
                                    pw.print("    pendingStateCommitTime=");
                                    sdf3 = sdf6;
                                    nowElapsed = nowElapsed6;
                                    TimeUtils.formatDuration(uidState3.pendingStateCommitTime, nowElapsed, pw);
                                    pw.println();
                                } else {
                                    sdf3 = sdf6;
                                    nowElapsed = nowElapsed6;
                                }
                                if (uidState3.startNesting != 0) {
                                    pw.print("    startNesting=");
                                    pw.println(uidState3.startNesting);
                                }
                                if (uidState3.foregroundOps != null && (dumpUid4 < 0 || dumpUid4 == 4)) {
                                    pw.println("    foregroundOps:");
                                    for (int j9 = 0; j9 < uidState3.foregroundOps.size(); j9++) {
                                        if (dumpOp < 0 || dumpOp == uidState3.foregroundOps.keyAt(j9)) {
                                            pw.print("      ");
                                            pw.print(AppOpsManager.opToName(uidState3.foregroundOps.keyAt(j9)));
                                            pw.print(": ");
                                            pw.println(uidState3.foregroundOps.valueAt(j9) ? "WATCHER" : "SILENT");
                                        }
                                    }
                                    pw.print("    hasForegroundWatchers=");
                                    pw.println(uidState3.hasForegroundWatchers);
                                }
                                needSep = true;
                                if (opModes != null) {
                                    int opModeCount2 = opModes.size();
                                    int j10 = 0;
                                    while (j10 < opModeCount2) {
                                        int code2 = opModes.keyAt(j10);
                                        int mode3 = opModes.valueAt(j10);
                                        if (dumpOp >= 0) {
                                            opModeCount = opModeCount2;
                                            code = code2;
                                            if (dumpOp != code) {
                                                nowElapsed4 = nowElapsed;
                                                j10++;
                                                opModeCount2 = opModeCount;
                                                nowElapsed = nowElapsed4;
                                            }
                                        } else {
                                            opModeCount = opModeCount2;
                                            code = code2;
                                        }
                                        if (dumpUid4 >= 0) {
                                            nowElapsed4 = nowElapsed;
                                            mode2 = mode3;
                                            if (dumpUid4 != mode2) {
                                                j10++;
                                                opModeCount2 = opModeCount;
                                                nowElapsed = nowElapsed4;
                                            }
                                        } else {
                                            nowElapsed4 = nowElapsed;
                                            mode2 = mode3;
                                        }
                                        pw.print("      ");
                                        pw.print(AppOpsManager.opToName(code));
                                        pw.print(": mode=");
                                        pw.println(AppOpsManager.modeToName(mode2));
                                        j10++;
                                        opModeCount2 = opModeCount;
                                        nowElapsed = nowElapsed4;
                                    }
                                    nowElapsed2 = nowElapsed;
                                } else {
                                    nowElapsed2 = nowElapsed;
                                }
                                if (pkgOps2 == null) {
                                    dumpPackage2 = dumpPackage;
                                    dumpMode = dumpUid4;
                                    sdf6 = sdf3;
                                    now4 = now;
                                    dumpOp2 = dumpOp;
                                    now3 = nowElapsed2;
                                } else {
                                    int pkgi2 = 0;
                                    while (pkgi2 < pkgOps2.size()) {
                                        Ops ops3 = pkgOps2.valueAt(pkgi2);
                                        if (dumpPackage == null || dumpPackage.equals(ops3.packageName)) {
                                            boolean op3 = false;
                                            int j11 = 0;
                                            while (j11 < ops3.size()) {
                                                try {
                                                    Op op4 = (Op) ops3.valueAt(j11);
                                                    int opCode = op4.op;
                                                    if (dumpOp < 0 || dumpOp == opCode) {
                                                        if (dumpUid4 >= 0) {
                                                            j2 = j11;
                                                            try {
                                                            } catch (Throwable th7) {
                                                                th = th7;
                                                                throw th;
                                                            }
                                                        } else {
                                                            j2 = j11;
                                                        }
                                                        if (!op3) {
                                                            pw.print("    Package ");
                                                            pw.print(ops3.packageName);
                                                            pw.println(":");
                                                            printedPackage = true;
                                                        } else {
                                                            printedPackage = op3;
                                                        }
                                                        pw.print("      ");
                                                        pw.print(AppOpsManager.opToName(opCode));
                                                        pw.print(" (");
                                                        pw.print(AppOpsManager.modeToName(op4.mode));
                                                        int switchOp = AppOpsManager.opToSwitch(opCode);
                                                        if (switchOp != opCode) {
                                                            pw.print(" / switch ");
                                                            pw.print(AppOpsManager.opToName(switchOp));
                                                            Op switchObj = (Op) ops3.get(switchOp);
                                                            if (switchObj != null) {
                                                                mode = switchObj.mode;
                                                            } else {
                                                                mode = AppOpsManager.opToDefaultMode(switchOp);
                                                            }
                                                            pw.print("=");
                                                            pw.print(AppOpsManager.modeToName(mode));
                                                        }
                                                        pw.println("): ");
                                                        uidState2 = uidState3;
                                                        dumpPackage4 = dumpPackage;
                                                        dumpMode3 = dumpUid4;
                                                        dumpOp4 = dumpOp;
                                                        sdf5 = sdf3;
                                                        j = j2;
                                                        ops = ops3;
                                                        now7 = now;
                                                        nowElapsed3 = nowElapsed2;
                                                        dumpStatesLocked(pw, op4, now7, sdf5, date3, "          ");
                                                        if (op4.running) {
                                                            pw.print("          Running start at: ");
                                                            TimeUtils.formatDuration(nowElapsed3 - op4.startRealtime, pw);
                                                            pw.println();
                                                        }
                                                        if (op4.startNesting != 0) {
                                                            pw.print("          startNesting=");
                                                            pw.println(op4.startNesting);
                                                        }
                                                        op3 = printedPackage;
                                                        int i9 = j + 1;
                                                        dumpOp = dumpOp4;
                                                        nowElapsed2 = nowElapsed3;
                                                        dumpUid4 = dumpMode3;
                                                        now = now7;
                                                        sdf3 = sdf5;
                                                        pkgOps2 = pkgOps2;
                                                        ops3 = ops;
                                                        j11 = i9;
                                                        dumpPackage = dumpPackage4;
                                                        uidState3 = uidState2;
                                                    } else {
                                                        j2 = j11;
                                                    }
                                                    dumpMode3 = dumpUid4;
                                                    uidState2 = uidState3;
                                                    sdf5 = sdf3;
                                                    dumpPackage4 = dumpPackage;
                                                    dumpOp4 = dumpOp;
                                                    ops = ops3;
                                                    now7 = now;
                                                    nowElapsed3 = nowElapsed2;
                                                    j = j2;
                                                    int i92 = j + 1;
                                                    dumpOp = dumpOp4;
                                                    nowElapsed2 = nowElapsed3;
                                                    dumpUid4 = dumpMode3;
                                                    now = now7;
                                                    sdf3 = sdf5;
                                                    pkgOps2 = pkgOps2;
                                                    ops3 = ops;
                                                    j11 = i92;
                                                    dumpPackage = dumpPackage4;
                                                    uidState3 = uidState2;
                                                } catch (Throwable th8) {
                                                    th = th8;
                                                    throw th;
                                                }
                                            }
                                            dumpMode2 = dumpUid4;
                                            pkgOps = pkgOps2;
                                            uidState = uidState3;
                                            dumpPackage3 = dumpPackage;
                                            now6 = now;
                                            now5 = nowElapsed2;
                                            sdf4 = sdf3;
                                            dumpOp3 = dumpOp;
                                        } else {
                                            dumpMode2 = dumpUid4;
                                            pkgOps = pkgOps2;
                                            uidState = uidState3;
                                            sdf4 = sdf3;
                                            now6 = now;
                                            dumpPackage3 = dumpPackage;
                                            dumpOp3 = dumpOp;
                                            now5 = nowElapsed2;
                                        }
                                        pkgi2++;
                                        dumpPackage = dumpPackage3;
                                        dumpOp = dumpOp3;
                                        nowElapsed2 = now5;
                                        uidState3 = uidState;
                                        dumpUid4 = dumpMode2;
                                        now = now6;
                                        sdf3 = sdf4;
                                        pkgOps2 = pkgOps;
                                    }
                                    dumpMode = dumpUid4;
                                    sdf6 = sdf3;
                                    now4 = now;
                                    dumpPackage2 = dumpPackage;
                                    dumpOp2 = dumpOp;
                                    now3 = nowElapsed2;
                                }
                                i8++;
                                dumpPackage = dumpPackage2;
                                dumpOp = dumpOp2;
                                dumpOp5 = dumpHistory;
                                nowElapsed6 = now3;
                                dumpWatchers = dumpWatchers2;
                                dumpUid4 = dumpMode;
                                now = now4;
                            }
                            needSep = needSep2;
                            i8++;
                            dumpPackage = dumpPackage2;
                            dumpOp = dumpOp2;
                            dumpOp5 = dumpHistory;
                            nowElapsed6 = now3;
                            dumpWatchers = dumpWatchers2;
                            dumpUid4 = dumpMode;
                            now = now4;
                        }
                        long now10 = now;
                        if (needSep) {
                            pw.println();
                        }
                        int userRestrictionCount2 = this.mOpUserRestrictions.size();
                        int i10 = 0;
                        while (i10 < userRestrictionCount2) {
                            IBinder token = this.mOpUserRestrictions.keyAt(i10);
                            ClientRestrictionState restrictionState2 = this.mOpUserRestrictions.valueAt(i10);
                            boolean printedTokenHeader = false;
                            if (dumpUid4 >= 0 || dumpWatchers) {
                                userRestrictionCount = userRestrictionCount2;
                                now2 = now10;
                                sdf = sdf6;
                                date = date3;
                            } else if (dumpOp5) {
                                userRestrictionCount = userRestrictionCount2;
                                now2 = now10;
                                sdf = sdf6;
                                date = date3;
                            } else {
                                int restrictionCount = restrictionState2.perUserRestrictions != null ? restrictionState2.perUserRestrictions.size() : 0;
                                if (restrictionCount <= 0 || dumpPackage != null) {
                                    userRestrictionCount = userRestrictionCount2;
                                    now2 = now10;
                                    sdf = sdf6;
                                    date = date3;
                                } else {
                                    boolean printedOpsHeader = false;
                                    int j12 = 0;
                                    while (j12 < restrictionCount) {
                                        int userId = restrictionState2.perUserRestrictions.keyAt(j12);
                                        boolean[] restrictedOps2 = restrictionState2.perUserRestrictions.valueAt(j12);
                                        if (restrictedOps2 != null) {
                                            if (dumpOp < 0 || (dumpOp < restrictedOps2.length && restrictedOps2[dumpOp])) {
                                                if (!printedTokenHeader) {
                                                    StringBuilder sb = new StringBuilder();
                                                    sdf2 = sdf6;
                                                    sb.append("  User restrictions for token ");
                                                    sb.append(token);
                                                    sb.append(":");
                                                    pw.println(sb.toString());
                                                    printedTokenHeader = true;
                                                } else {
                                                    sdf2 = sdf6;
                                                }
                                                if (!printedOpsHeader) {
                                                    pw.println("      Restricted ops:");
                                                    printedOpsHeader = true;
                                                }
                                                StringBuilder restrictedOpsValue = new StringBuilder();
                                                restrictedOpsValue.append("[");
                                                int restrictedOpCount2 = restrictedOps2.length;
                                                date2 = date3;
                                                int k = 0;
                                                while (k < restrictedOpCount2) {
                                                    if (restrictedOps2[k]) {
                                                        restrictedOps = restrictedOps2;
                                                        restrictedOpCount = restrictedOpCount2;
                                                        if (restrictedOpsValue.length() > 1) {
                                                            restrictedOpsValue.append(", ");
                                                        }
                                                        restrictedOpsValue.append(AppOpsManager.opToName(k));
                                                    } else {
                                                        restrictedOps = restrictedOps2;
                                                        restrictedOpCount = restrictedOpCount2;
                                                    }
                                                    k++;
                                                    restrictedOps2 = restrictedOps;
                                                    restrictedOpCount2 = restrictedOpCount;
                                                }
                                                restrictedOpsValue.append("]");
                                                pw.print("        ");
                                                pw.print("user: ");
                                                pw.print(userId);
                                                pw.print(" restricted ops: ");
                                                pw.println(restrictedOpsValue);
                                                j12++;
                                                userRestrictionCount2 = userRestrictionCount2;
                                                now10 = now10;
                                                date3 = date2;
                                                sdf6 = sdf2;
                                            }
                                        }
                                        sdf2 = sdf6;
                                        date2 = date3;
                                        j12++;
                                        userRestrictionCount2 = userRestrictionCount2;
                                        now10 = now10;
                                        date3 = date2;
                                        sdf6 = sdf2;
                                    }
                                    userRestrictionCount = userRestrictionCount2;
                                    now2 = now10;
                                    sdf = sdf6;
                                    date = date3;
                                }
                                int excludedPackageCount2 = restrictionState2.perUserExcludedPackages != null ? restrictionState2.perUserExcludedPackages.size() : 0;
                                if (excludedPackageCount2 > 0 && dumpOp < 0) {
                                    boolean printedPackagesHeader = false;
                                    int j13 = 0;
                                    while (j13 < excludedPackageCount2) {
                                        int userId2 = restrictionState2.perUserExcludedPackages.keyAt(j13);
                                        String[] packageNames = restrictionState2.perUserExcludedPackages.valueAt(j13);
                                        if (packageNames == null) {
                                            excludedPackageCount = excludedPackageCount2;
                                            restrictionState = restrictionState2;
                                        } else {
                                            if (dumpPackage != null) {
                                                hasPackage = false;
                                                int length = packageNames.length;
                                                excludedPackageCount = excludedPackageCount2;
                                                int excludedPackageCount3 = 0;
                                                while (true) {
                                                    if (excludedPackageCount3 >= length) {
                                                        restrictionState = restrictionState2;
                                                        break;
                                                    }
                                                    restrictionState = restrictionState2;
                                                    if (dumpPackage.equals(packageNames[excludedPackageCount3])) {
                                                        hasPackage = true;
                                                        break;
                                                    } else {
                                                        excludedPackageCount3++;
                                                        restrictionState2 = restrictionState;
                                                    }
                                                }
                                            } else {
                                                excludedPackageCount = excludedPackageCount2;
                                                restrictionState = restrictionState2;
                                                hasPackage = true;
                                            }
                                            if (hasPackage) {
                                                if (!printedTokenHeader) {
                                                    pw.println("  User restrictions for token " + token + ":");
                                                    printedTokenHeader = true;
                                                }
                                                if (!printedPackagesHeader) {
                                                    pw.println("      Excluded packages:");
                                                    printedPackagesHeader = true;
                                                }
                                                pw.print("        ");
                                                pw.print("user: ");
                                                pw.print(userId2);
                                                pw.print(" packages: ");
                                                pw.println(Arrays.toString(packageNames));
                                            }
                                        }
                                        j13++;
                                        excludedPackageCount2 = excludedPackageCount;
                                        restrictionState2 = restrictionState;
                                    }
                                }
                            }
                            i10++;
                            userRestrictionCount2 = userRestrictionCount;
                            now10 = now2;
                            date3 = date;
                            sdf6 = sdf;
                        }
                    } catch (Throwable th9) {
                        th = th9;
                        throw th;
                    }
                } catch (Throwable th10) {
                    th = th10;
                    throw th;
                }
            }
            if (dumpOp5 && !dumpWatchers) {
                this.mHistoricalRegistry.dump("  ", pw, dumpUid2, dumpPackage, dumpOp);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class Restriction {
        private static final ArraySet<String> NO_EXCEPTIONS = new ArraySet<>();
        ArraySet<String> exceptionPackages;
        int mode;

        private Restriction() {
            this.exceptionPackages = NO_EXCEPTIONS;
        }
    }

    public void setUserRestrictions(Bundle restrictions, IBinder token, int userHandle) {
        checkSystemUid("setUserRestrictions");
        Preconditions.checkNotNull(restrictions);
        Preconditions.checkNotNull(token);
        for (int i = 0; i < 91; i++) {
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
        synchronized (this) {
            ClientRestrictionState restrictionState = this.mOpUserRestrictions.get(token);
            if (restrictionState == null) {
                try {
                    restrictionState = new ClientRestrictionState(token);
                    this.mOpUserRestrictions.put(token, restrictionState);
                } catch (RemoteException e) {
                    return;
                }
            }
            if (restrictionState.setRestriction(code, restricted, exceptionPackages, userHandle)) {
                this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$GUeKjlbzT65s86vaxy5gvOajuhw.INSTANCE, this, Integer.valueOf(code), -2));
            }
            if (restrictionState.isDefault()) {
                this.mOpUserRestrictions.remove(token);
                restrictionState.destroy();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void notifyWatchersOfChange(int code, int uid) {
        synchronized (this) {
            ArraySet<ModeCallback> callbacks = this.mOpModeWatchers.get(code);
            if (callbacks != null) {
                notifyOpChanged(new ArraySet<>(callbacks), code, uid, (String) null);
            }
        }
    }

    public void removeUser(int userHandle) throws RemoteException {
        checkSystemUid("removeUser");
        synchronized (this) {
            for (int i = this.mOpUserRestrictions.size() - 1; i >= 0; i--) {
                this.mOpUserRestrictions.valueAt(i).removeUser(userHandle);
            }
            removeUidsForUserLocked(userHandle);
        }
    }

    public boolean isOperationActive(int code, int uid, String packageName) {
        if (!(Binder.getCallingUid() == uid || this.mContext.checkCallingOrSelfPermission("android.permission.WATCH_APPOPS") == 0)) {
            return false;
        }
        verifyIncomingOp(code);
        if (resolvePackageName(uid, packageName) == null) {
            return false;
        }
        synchronized (this) {
            for (int i = this.mClients.size() - 1; i >= 0; i--) {
                ClientState client = this.mClients.valueAt(i);
                for (int j = client.mStartedOps.size() - 1; j >= 0; j--) {
                    Op op = client.mStartedOps.get(j);
                    if (op.op == code && op.uidState.uid == uid) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public void setHistoryParameters(int mode, long baseSnapshotInterval, int compressionStep) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_APPOPS", "setHistoryParameters");
        this.mHistoricalRegistry.setHistoryParameters(mode, baseSnapshotInterval, (long) compressionStep);
    }

    public void offsetHistory(long offsetMillis) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_APPOPS", "offsetHistory");
        this.mHistoricalRegistry.offsetHistory(offsetMillis);
    }

    public void addHistoricalOps(AppOpsManager.HistoricalOps ops) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_APPOPS", "addHistoricalOps");
        this.mHistoricalRegistry.addHistoricalOps(ops);
    }

    public void resetHistoryParameters() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_APPOPS", "resetHistoryParameters");
        this.mHistoricalRegistry.resetHistoryParameters();
    }

    public void clearHistory() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_APPOPS", "clearHistory");
        this.mHistoricalRegistry.clearHistory();
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

    private static String resolvePackageName(int uid, String packageName) {
        if (uid == 0) {
            return "root";
        }
        if (uid == 2000) {
            return NotificationShellCmd.NOTIFICATION_PACKAGE;
        }
        if (uid == 1013) {
            return "media";
        }
        if (uid == 1041) {
            return "audioserver";
        }
        if (uid == 1047) {
            return "cameraserver";
        }
        if (uid == 1000 && packageName == null) {
            return PackageManagerService.PLATFORM_PACKAGE_NAME;
        }
        return packageName;
    }

    /* access modifiers changed from: private */
    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static int resolveUid(String packageName) {
        boolean z;
        if (packageName == null) {
            return -1;
        }
        switch (packageName.hashCode()) {
            case -31178072:
                if (packageName.equals("cameraserver")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 3506402:
                if (packageName.equals("root")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case 103772132:
                if (packageName.equals("media")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 109403696:
                if (packageName.equals("shell")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1344606873:
                if (packageName.equals("audioserver")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            return 0;
        }
        if (z) {
            return IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME;
        }
        if (z) {
            return 1013;
        }
        if (z) {
            return 1041;
        }
        if (!z) {
            return -1;
        }
        return 1047;
    }

    private static String[] getPackagesForUid(int uid) {
        String[] packageNames = null;
        if (AppGlobals.getPackageManager() != null) {
            try {
                packageNames = AppGlobals.getPackageManager().getPackagesForUid(uid);
            } catch (RemoteException e) {
            }
        }
        if (packageNames == null) {
            return EmptyArray.STRING;
        }
        return packageNames;
    }

    /* access modifiers changed from: private */
    public final class ClientRestrictionState implements IBinder.DeathRecipient {
        SparseArray<String[]> perUserExcludedPackages;
        SparseArray<boolean[]> perUserRestrictions;
        private final IBinder token;

        public ClientRestrictionState(IBinder token2) throws RemoteException {
            token2.linkToDeath(this, 0);
            this.token = token2;
        }

        public boolean setRestriction(int code, boolean restricted, String[] excludedPackages, int userId) {
            int[] users;
            boolean changed = false;
            if (this.perUserRestrictions == null && restricted) {
                this.perUserRestrictions = new SparseArray<>();
            }
            if (userId == -1) {
                List<UserInfo> liveUsers = UserManager.get(AppOpsService.this.mContext).getUsers(false);
                users = new int[liveUsers.size()];
                for (int i = 0; i < liveUsers.size(); i++) {
                    users[i] = liveUsers.get(i).id;
                }
            } else {
                users = new int[]{userId};
            }
            if (this.perUserRestrictions != null) {
                for (int thisUserId : users) {
                    boolean[] userRestrictions = this.perUserRestrictions.get(thisUserId);
                    if (userRestrictions == null && restricted) {
                        userRestrictions = new boolean[91];
                        this.perUserRestrictions.put(thisUserId, userRestrictions);
                    }
                    if (!(userRestrictions == null || userRestrictions[code] == restricted)) {
                        userRestrictions[code] = restricted;
                        if (!restricted && isDefault(userRestrictions)) {
                            this.perUserRestrictions.remove(thisUserId);
                            userRestrictions = null;
                        }
                        changed = true;
                    }
                    if (userRestrictions != null) {
                        boolean noExcludedPackages = ArrayUtils.isEmpty(excludedPackages);
                        if (this.perUserExcludedPackages == null && !noExcludedPackages) {
                            this.perUserExcludedPackages = new SparseArray<>();
                        }
                        SparseArray<String[]> sparseArray = this.perUserExcludedPackages;
                        if (sparseArray != null && !Arrays.equals(excludedPackages, sparseArray.get(thisUserId))) {
                            if (noExcludedPackages) {
                                this.perUserExcludedPackages.remove(thisUserId);
                                if (this.perUserExcludedPackages.size() <= 0) {
                                    this.perUserExcludedPackages = null;
                                }
                            } else {
                                this.perUserExcludedPackages.put(thisUserId, excludedPackages);
                            }
                            changed = true;
                        }
                    }
                }
            }
            return changed;
        }

        public boolean hasRestriction(int restriction, String packageName, int userId) {
            boolean[] restrictions;
            String[] perUserExclusions;
            SparseArray<boolean[]> sparseArray = this.perUserRestrictions;
            if (sparseArray == null || (restrictions = sparseArray.get(userId)) == null || !restrictions[restriction]) {
                return false;
            }
            SparseArray<String[]> sparseArray2 = this.perUserExcludedPackages;
            if (sparseArray2 == null || (perUserExclusions = sparseArray2.get(userId)) == null) {
                return true;
            }
            return true ^ ArrayUtils.contains(perUserExclusions, packageName);
        }

        public void removeUser(int userId) {
            SparseArray<String[]> sparseArray = this.perUserExcludedPackages;
            if (sparseArray != null) {
                sparseArray.remove(userId);
                if (this.perUserExcludedPackages.size() <= 0) {
                    this.perUserExcludedPackages = null;
                }
            }
            SparseArray<boolean[]> sparseArray2 = this.perUserRestrictions;
            if (sparseArray2 != null) {
                sparseArray2.remove(userId);
                if (this.perUserRestrictions.size() <= 0) {
                    this.perUserRestrictions = null;
                }
            }
        }

        public boolean isDefault() {
            SparseArray<boolean[]> sparseArray = this.perUserRestrictions;
            return sparseArray == null || sparseArray.size() <= 0;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (AppOpsService.this) {
                AppOpsService.this.mOpUserRestrictions.remove(this.token);
                if (this.perUserRestrictions != null) {
                    int userCount = this.perUserRestrictions.size();
                    for (int i = 0; i < userCount; i++) {
                        boolean[] restrictions = this.perUserRestrictions.valueAt(i);
                        int restrictionCount = restrictions.length;
                        for (int j = 0; j < restrictionCount; j++) {
                            if (restrictions[j]) {
                                AppOpsService.this.mHandler.post(new Runnable(j) {
                                    /* class com.android.server.appop.$$Lambda$AppOpsService$ClientRestrictionState$uMVYManZlOG3nljcsmHU5SaC48k */
                                    private final /* synthetic */ int f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    @Override // java.lang.Runnable
                                    public final void run() {
                                        AppOpsService.ClientRestrictionState.this.lambda$binderDied$0$AppOpsService$ClientRestrictionState(this.f$1);
                                    }
                                });
                            }
                        }
                    }
                    destroy();
                }
            }
        }

        public /* synthetic */ void lambda$binderDied$0$AppOpsService$ClientRestrictionState(int changedCode) {
            AppOpsService.this.notifyWatchersOfChange(changedCode, -2);
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

    /* access modifiers changed from: private */
    public final class AppOpsManagerInternalImpl extends AppOpsManagerInternal {
        private AppOpsManagerInternalImpl() {
        }

        public void setDeviceAndProfileOwners(SparseIntArray owners) {
            synchronized (AppOpsService.this) {
                AppOpsService.this.mProfileOwners = owners;
            }
        }

        public void setUidMode(int code, int uid, int mode) {
            AppOpsService.this.setUidMode(code, uid, mode);
        }

        public void setAllPkgModesToDefault(int code, int uid) {
            AppOpsService.this.setAllPkgModesToDefault(code, uid);
        }

        public int checkOperationUnchecked(int code, int uid, String packageName) {
            return AppOpsService.this.checkOperationUnchecked(code, uid, packageName, true, false);
        }
    }
}
