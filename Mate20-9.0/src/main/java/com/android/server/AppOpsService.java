package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AppOpsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
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
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManagerInternal;
import android.provider.Settings;
import android.rms.HwSysResource;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.KeyValueListParser;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IAppOpsActiveCallback;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.job.controllers.JobStatus;
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
    private static final int[] PROCESS_STATE_TO_UID_STATE = {0, 0, 1, 2, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5};
    static final String TAG = "AppOps";
    private static final int UID_ANY = -2;
    static final String[] UID_STATE_NAMES = {"pers ", "top  ", "fgsvc", "fg   ", "bg   ", "cch  "};
    static final String[] UID_STATE_REJECT_ATTRS = {"rp", "rt", "rfs", "rf", "rb", "rc"};
    static final String[] UID_STATE_TIME_ATTRS = {"tp", "tt", "tfs", "tf", "tb", "tc"};
    static final long WRITE_DELAY = 1800000;
    final ArrayMap<IBinder, SparseArray<ActiveCallback>> mActiveWatchers = new ArrayMap<>();
    private final AppOpsManagerInternalImpl mAppOpsManagerInternal = new AppOpsManagerInternalImpl();
    private HwSysResource mAppOpsResource;
    final SparseArray<SparseArray<Restriction>> mAudioRestrictions = new SparseArray<>();
    final ArrayMap<IBinder, ClientState> mClients = new ArrayMap<>();
    private final Constants mConstants;
    Context mContext;
    boolean mFastWriteScheduled;
    final AtomicFile mFile;
    final Handler mHandler;
    long mLastUptime;
    final ArrayMap<IBinder, ModeCallback> mModeWatchers = new ArrayMap<>();
    final SparseArray<ArraySet<ModeCallback>> mOpModeWatchers = new SparseArray<>();
    /* access modifiers changed from: private */
    public final ArrayMap<IBinder, ClientRestrictionState> mOpUserRestrictions = new ArrayMap<>();
    final ArrayMap<String, ArraySet<ModeCallback>> mPackageModeWatchers = new ArrayMap<>();
    SparseIntArray mProfileOwners;
    @VisibleForTesting
    final SparseArray<UidState> mUidStates = new SparseArray<>();
    final Runnable mWriteRunner = new Runnable() {
        public void run() {
            synchronized (AppOpsService.this) {
                AppOpsService.this.mWriteScheduled = false;
                AppOpsService.this.mFastWriteScheduled = false;
                new AsyncTask<Void, Void, Void>() {
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

    final class ActiveCallback implements IBinder.DeathRecipient {
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

        public void binderDied() {
            AppOpsService.this.stopWatchingActive(this.mCallback);
        }
    }

    private final class AppOpsManagerInternalImpl extends AppOpsManagerInternal {
        private AppOpsManagerInternalImpl() {
        }

        public void setDeviceAndProfileOwners(SparseIntArray owners) {
            synchronized (AppOpsService.this) {
                AppOpsService.this.mProfileOwners = owners;
            }
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

    private final class ClientRestrictionState implements IBinder.DeathRecipient {
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
            int[] users2 = users;
            if (this.perUserRestrictions != null) {
                for (int thisUserId : users2) {
                    boolean[] userRestrictions = this.perUserRestrictions.get(thisUserId);
                    if (userRestrictions == null && restricted) {
                        userRestrictions = new boolean[78];
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
                        if (this.perUserExcludedPackages != null && !Arrays.equals(excludedPackages, (Object[]) this.perUserExcludedPackages.get(thisUserId))) {
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
            if (this.perUserRestrictions == null) {
                return false;
            }
            boolean[] restrictions = this.perUserRestrictions.get(userId);
            if (restrictions == null || !restrictions[restriction]) {
                return false;
            }
            if (this.perUserExcludedPackages == null) {
                return true;
            }
            String[] perUserExclusions = this.perUserExcludedPackages.get(userId);
            if (perUserExclusions == null) {
                return true;
            }
            return true ^ ArrayUtils.contains(perUserExclusions, packageName);
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
                if (this.perUserRestrictions != null) {
                    int userCount = this.perUserRestrictions.size();
                    for (int i = 0; i < userCount; i++) {
                        boolean[] restrictions = this.perUserRestrictions.valueAt(i);
                        int restrictionCount = restrictions.length;
                        for (int j = 0; j < restrictionCount; j++) {
                            if (restrictions[j]) {
                                AppOpsService.this.mHandler.post(new Runnable(j) {
                                    private final /* synthetic */ int f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    public final void run() {
                                        AppOpsService.this.notifyWatchersOfChange(this.f$1, -2);
                                    }
                                });
                            }
                        }
                    }
                    destroy();
                }
            }
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

    final class ClientState extends Binder implements IBinder.DeathRecipient {
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

        public String toString() {
            return "ClientState{mAppToken=" + this.mAppToken + ", pid=" + this.mPid + '}';
        }

        public void binderDied() {
            synchronized (AppOpsService.this) {
                for (int i = this.mStartedOps.size() - 1; i >= 0; i--) {
                    AppOpsService.this.finishOperationLocked(this.mStartedOps.get(i), true);
                }
                AppOpsService.this.mClients.remove(this.mAppToken);
            }
        }
    }

    private final class Constants extends ContentObserver {
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

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            String value;
            if (this.mResolver != null) {
                value = Settings.Global.getString(this.mResolver, "app_ops_constants");
            } else {
                value = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            synchronized (AppOpsService.this) {
                try {
                    this.mParser.setString(value);
                } catch (IllegalArgumentException e) {
                    Slog.e(AppOpsService.TAG, "Bad app ops settings", e);
                }
                this.TOP_STATE_SETTLE_TIME = this.mParser.getDurationMillis(KEY_TOP_STATE_SETTLE_TIME, 30000);
                this.FG_SERVICE_STATE_SETTLE_TIME = this.mParser.getDurationMillis(KEY_FG_SERVICE_STATE_SETTLE_TIME, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
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

    final class ModeCallback implements IBinder.DeathRecipient {
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
            return uid == -2 || this.mWatchingUid < 0 || this.mWatchingUid == uid;
        }

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

        public void binderDied() {
            AppOpsService.this.stopWatchingMode(this.mCallback);
        }
    }

    static final class Op {
        int duration;
        int mode;
        final int op;
        final String packageName;
        String proxyPackageName;
        int proxyUid = -1;
        long[] rejectTime = new long[6];
        int startNesting;
        long startRealtime;
        long[] time = new long[6];
        final int uid;
        final UidState uidState;

        Op(UidState _uidState, String _packageName, int _op) {
            this.uidState = _uidState;
            this.uid = _uidState.uid;
            this.packageName = _packageName;
            this.op = _op;
            this.mode = AppOpsManager.opToDefaultMode(this.op);
        }

        /* access modifiers changed from: package-private */
        public boolean hasAnyTime() {
            for (int i = 0; i < 6; i++) {
                if (this.time[i] != 0 || this.rejectTime[i] != 0) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public int getMode() {
            return this.uidState.evalMode(this.mode);
        }
    }

    static final class Ops extends SparseArray<Op> {
        final boolean isPrivileged;
        final String packageName;
        final UidState uidState;

        Ops(String _packageName, UidState _uidState, boolean _isPrivileged) {
            this.packageName = _packageName;
            this.uidState = _uidState;
            this.isPrivileged = _isPrivileged;
        }
    }

    private static final class Restriction {
        private static final ArraySet<String> NO_EXCEPTIONS = new ArraySet<>();
        ArraySet<String> exceptionPackages;
        int mode;

        private Restriction() {
            this.exceptionPackages = NO_EXCEPTIONS;
        }
    }

    static class Shell extends ShellCommand {
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
                String nextArg = getNextArg();
                String argument = nextArg;
                if (nextArg == null) {
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
            } else {
                this.mode = defMode;
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public int parseUserPackageOp(boolean reqOp, PrintWriter err) throws RemoteException {
            this.userId = -2;
            this.packageName = null;
            this.opStr = null;
            while (true) {
                String nextArg = getNextArg();
                String argument = nextArg;
                if (nextArg == null) {
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
            } else if (this.opStr != null || !reqOp) {
                if (this.opStr != null) {
                    this.op = strOpToOp(this.opStr, err);
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
                            int startTypeVal = i + 1;
                            int i2 = startTypeVal;
                            while (i2 < this.packageName.length() && this.packageName.charAt(i2) >= '0' && this.packageName.charAt(i2) <= '9') {
                                try {
                                    i2++;
                                } catch (NumberFormatException e2) {
                                }
                            }
                            if (i2 > startTypeVal) {
                                try {
                                    int typeVal = Integer.parseInt(this.packageName.substring(startTypeVal, i2));
                                    if (type == 'a') {
                                        this.nonpackageUid = UserHandle.getUid(user, typeVal + 10000);
                                    } else if (type == 's') {
                                        this.nonpackageUid = UserHandle.getUid(user, typeVal);
                                    }
                                } catch (NumberFormatException e3) {
                                }
                            }
                        } catch (NumberFormatException e4) {
                            int i3 = i;
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

    @VisibleForTesting
    static final class UidState {
        public SparseBooleanArray foregroundOps;
        public boolean hasForegroundWatchers;
        public SparseIntArray opModes;
        public int pendingState = 5;
        public long pendingStateCommitTime;
        public ArrayMap<String, Ops> pkgOps;
        public int startNesting;
        public int state = 5;
        public final int uid;

        public UidState(int uid2) {
            this.uid = uid2;
        }

        public void clear() {
            this.pkgOps = null;
            this.opModes = null;
        }

        public boolean isDefault() {
            return (this.pkgOps == null || this.pkgOps.isEmpty()) && (this.opModes == null || this.opModes.size() <= 0);
        }

        /* access modifiers changed from: package-private */
        public int evalMode(int mode) {
            if (mode != 4) {
                return mode;
            }
            return this.state <= 2 ? 0 : 1;
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
            if (this.opModes != null) {
                for (int i = this.opModes.size() - 1; i >= 0; i--) {
                    if (this.opModes.valueAt(i) == 4) {
                        if (which == null) {
                            which = new SparseBooleanArray();
                        }
                        evalForegroundWatchers(this.opModes.keyAt(i), watchers, which);
                    }
                }
            }
            if (this.pkgOps != null) {
                for (int i2 = this.pkgOps.size() - 1; i2 >= 0; i2--) {
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

    public AppOpsService(File storagePath, Handler handler) {
        LockGuard.installLock((Object) this, 0);
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
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setExternalSourcesPolicy(new PackageManagerInternal.ExternalSourcesPolicy() {
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
        ((StorageManagerInternal) LocalServices.getService(StorageManagerInternal.class)).addExternalStoragePolicy(new StorageManagerInternal.ExternalStorageMountPolicy() {
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

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a2, code lost:
        return;
     */
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
                            if (uid == op.uid && packageName.equals(op.packageName)) {
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
                        if (op2.duration == -1) {
                            scheduleOpActiveChangedIfNeededLocked(op2.op, op2.uid, op2.packageName, false);
                        }
                    }
                }
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

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0049  */
    public void updateUidProcState(int uid, int procState) {
        long settleTime;
        synchronized (this) {
            UidState uidState = getUidStateLocked(uid, true);
            int newState = PROCESS_STATE_TO_UID_STATE[procState];
            if (!(uidState == null || uidState.pendingState == newState)) {
                int oldPendingState = uidState.pendingState;
                uidState.pendingState = newState;
                if (newState >= uidState.state) {
                    if (newState > 2) {
                        if (uidState.pendingStateCommitTime == 0) {
                            if (uidState.state <= 1) {
                                settleTime = this.mConstants.TOP_STATE_SETTLE_TIME;
                            } else if (uidState.state <= 2) {
                                settleTime = this.mConstants.FG_SERVICE_STATE_SETTLE_TIME;
                            } else {
                                settleTime = this.mConstants.BG_STATE_SETTLE_TIME;
                                uidState.pendingStateCommitTime = SystemClock.uptimeMillis() + settleTime;
                            }
                            uidState.pendingStateCommitTime = SystemClock.uptimeMillis() + settleTime;
                        }
                        if (uidState.startNesting != 0) {
                            long now = System.currentTimeMillis();
                            for (int i = uidState.pkgOps.size() - 1; i >= 0; i--) {
                                Ops ops = uidState.pkgOps.valueAt(i);
                                for (int j = ops.size() - 1; j >= 0; j--) {
                                    Op op = (Op) ops.valueAt(j);
                                    if (op.startNesting > 0) {
                                        op.time[oldPendingState] = now;
                                        op.time[newState] = now;
                                    }
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
        long j;
        long j2;
        Ops ops2 = pkgOps;
        int[] iArr = ops;
        long elapsedNow = SystemClock.elapsedRealtime();
        int i = -1;
        if (iArr == null) {
            ArrayList<AppOpsManager.OpEntry> resOps = new ArrayList<>();
            int j3 = 0;
            while (j3 < pkgOps.size()) {
                Op curOp = (Op) ops2.valueAt(j3);
                boolean running = curOp.duration == i;
                if (running) {
                    j2 = elapsedNow - curOp.startRealtime;
                } else {
                    j2 = (long) curOp.duration;
                }
                long duration = j2;
                long elapsedNow2 = elapsedNow;
                int i2 = curOp.proxyUid;
                String str = curOp.proxyPackageName;
                Op op = curOp;
                AppOpsManager.OpEntry opEntry = r11;
                long j4 = duration;
                AppOpsManager.OpEntry opEntry2 = new AppOpsManager.OpEntry(curOp.op, curOp.mode, curOp.time, curOp.rejectTime, (int) duration, running, i2, str);
                resOps.add(opEntry);
                j3++;
                elapsedNow = elapsedNow2;
                i = -1;
            }
            return resOps;
        }
        long elapsedNow3 = elapsedNow;
        ArrayList<AppOpsManager.OpEntry> resOps2 = null;
        int j5 = 0;
        while (j5 < iArr.length) {
            Op curOp2 = (Op) ops2.get(iArr[j5]);
            if (curOp2 != null) {
                if (resOps2 == null) {
                    resOps2 = new ArrayList<>();
                }
                boolean running2 = curOp2.duration == -1;
                if (running2) {
                    j = elapsedNow3 - curOp2.startRealtime;
                } else {
                    j = (long) curOp2.duration;
                }
                long duration2 = j;
                AppOpsManager.OpEntry opEntry3 = r7;
                long j6 = duration2;
                AppOpsManager.OpEntry opEntry4 = new AppOpsManager.OpEntry(curOp2.op, curOp2.mode, curOp2.time, curOp2.rejectTime, (int) duration2, running2, curOp2.proxyUid, curOp2.proxyPackageName);
                resOps2.add(opEntry3);
            }
            j5++;
            ops2 = pkgOps;
        }
        return resOps2;
    }

    private ArrayList<AppOpsManager.OpEntry> collectOps(SparseIntArray uidOps, int[] ops) {
        SparseIntArray sparseIntArray = uidOps;
        int[] iArr = ops;
        ArrayList<AppOpsManager.OpEntry> resOps = null;
        int j = 0;
        if (iArr == null) {
            resOps = new ArrayList<>();
            while (j < uidOps.size()) {
                AppOpsManager.OpEntry opEntry = new AppOpsManager.OpEntry(sparseIntArray.keyAt(j), sparseIntArray.valueAt(j), 0, 0, 0, -1, null);
                resOps.add(opEntry);
                j++;
            }
        } else {
            while (j < iArr.length) {
                int index = sparseIntArray.indexOfKey(iArr[j]);
                if (index >= 0) {
                    if (resOps == null) {
                        resOps = new ArrayList<>();
                    }
                    AppOpsManager.OpEntry opEntry2 = new AppOpsManager.OpEntry(sparseIntArray.keyAt(index), sparseIntArray.valueAt(index), 0, 0, 0, -1, null);
                    resOps.add(opEntry2);
                }
                j++;
            }
        }
        return resOps;
    }

    public List<AppOpsManager.PackageOps> getPackagesForOps(int[] ops) {
        this.mContext.enforcePermission("android.permission.GET_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        synchronized (this) {
            try {
                int uidStateCount = this.mUidStates.size();
                ArrayList<AppOpsManager.PackageOps> res = null;
                int i = 0;
                while (i < uidStateCount) {
                    try {
                        UidState uidState = this.mUidStates.valueAt(i);
                        if (uidState.pkgOps != null) {
                            if (!uidState.pkgOps.isEmpty()) {
                                ArrayMap<String, Ops> packages = uidState.pkgOps;
                                int packageCount = packages.size();
                                ArrayList<AppOpsManager.PackageOps> res2 = res;
                                int j = 0;
                                while (j < packageCount) {
                                    try {
                                        Ops pkgOps = packages.valueAt(j);
                                        ArrayList<AppOpsManager.OpEntry> resOps = collectOps(pkgOps, ops);
                                        if (resOps != null) {
                                            if (res2 == null) {
                                                res2 = new ArrayList<>();
                                            }
                                            res2.add(new AppOpsManager.PackageOps(pkgOps.packageName, pkgOps.uidState.uid, resOps));
                                        }
                                        j++;
                                    } catch (Throwable th) {
                                        th = th;
                                        throw th;
                                    }
                                }
                                res = res2;
                            }
                        }
                        i++;
                    } catch (Throwable th2) {
                        th = th2;
                        ArrayList<AppOpsManager.PackageOps> arrayList = res;
                        throw th;
                    }
                }
                return res;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
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
            res.add(new AppOpsManager.PackageOps(null, uidState.uid, resOps));
            return res;
        }
    }

    private void pruneOp(Op op, int uid, String packageName) {
        if (!op.hasAnyTime()) {
            Ops ops = getOpsRawLocked(uid, packageName, false, false);
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0024, code lost:
        r6.mContext.enforcePermission("android.permission.MANAGE_APP_OPS_MODES", android.os.Binder.getCallingPid(), android.os.Binder.getCallingUid(), null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0034, code lost:
        return;
     */
    public void enforceManageAppOpsModes(int callingPid, int callingUid, int targetUid) {
        if (callingPid != Process.myPid()) {
            int callingUser = UserHandle.getUserId(callingUid);
            synchronized (this) {
                if (this.mProfileOwners != null && this.mProfileOwners.get(callingUser, -1) == callingUid && targetUid >= 0 && callingUser == UserHandle.getUserId(targetUid)) {
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007f, code lost:
        r13 = getPackagesForUid(r21);
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0084, code lost:
        monitor-enter(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r0 = r7.mOpModeWatchers.get(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008d, code lost:
        if (r0 == null) goto L_0x00b8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x008f, code lost:
        r2 = r0.size();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0093, code lost:
        r3 = null;
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0095, code lost:
        if (r1 >= r2) goto L_0x00b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r4 = r0.valueAt(r1);
        r5 = new android.util.ArraySet<>();
        java.util.Collections.addAll(r5, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a5, code lost:
        if (r3 != null) goto L_0x00ad;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a7, code lost:
        r3 = new android.util.ArrayMap<>();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ad, code lost:
        r3.put(r4, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00b0, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00b3, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00b4, code lost:
        r1 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b7, code lost:
        r1 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r2 = r13.length;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00b9, code lost:
        r14 = r1;
        r1 = r0;
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00bc, code lost:
        if (r0 >= r2) goto L_0x0100;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        r3 = r13[r0];
        r1 = r7.mPackageModeWatchers.get(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c9, code lost:
        if (r1 == null) goto L_0x00f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00cb, code lost:
        if (r14 != null) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00cd, code lost:
        r14 = new android.util.ArrayMap<>();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d3, code lost:
        r4 = r1.size();
        r5 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00d8, code lost:
        if (r5 >= r4) goto L_0x00f8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00da, code lost:
        r6 = r1.valueAt(r5);
        r15 = r14.get(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e6, code lost:
        if (r15 != null) goto L_0x00f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00e8, code lost:
        r15 = new android.util.ArraySet<>();
        r14.put(r6, r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f1, code lost:
        r15.add(r3);
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00f8, code lost:
        r0 = r0 + 1;
        r11 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00fc, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00fd, code lost:
        r1 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0100, code lost:
        monitor-exit(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0101, code lost:
        if (r14 != null) goto L_0x0104;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0103, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0104, code lost:
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0109, code lost:
        if (r0 >= r14.size()) goto L_0x016f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x010b, code lost:
        r11 = r14.keyAt(r0);
        r15 = r14.valueAt(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0119, code lost:
        if (r15 != null) goto L_0x0138;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x011b, code lost:
        r7.mHandler.sendMessage(com.android.internal.util.function.pooled.PooledLambda.obtainMessage(com.android.server.$$Lambda$AppOpsService$lxgFmOnGguOiLyfUZbyOpNBfTVw.INSTANCE, r7, r11, java.lang.Integer.valueOf(r10), java.lang.Integer.valueOf(r21), r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0138, code lost:
        r12 = r15.size();
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x013d, code lost:
        r6 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x013e, code lost:
        if (r6 >= r12) goto L_0x0169;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0140, code lost:
        r7.mHandler.sendMessage(com.android.internal.util.function.pooled.PooledLambda.obtainMessage(com.android.server.$$Lambda$AppOpsService$lxgFmOnGguOiLyfUZbyOpNBfTVw.INSTANCE, r7, r11, java.lang.Integer.valueOf(r10), java.lang.Integer.valueOf(r21), r15.valueAt(r6)));
        r1 = r6 + 1;
        r8 = r21;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0169, code lost:
        r0 = r0 + 1;
        r8 = r21;
        r12 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x016f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0170, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:?, code lost:
        monitor-exit(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0172, code lost:
        throw r0;
     */
    public void setUidMode(int code, int uid, int mode) {
        int i = uid;
        int i2 = mode;
        enforceManageAppOpsModes(Binder.getCallingPid(), Binder.getCallingUid(), i);
        verifyIncomingOp(code);
        int code2 = AppOpsManager.opToSwitch(code);
        synchronized (this) {
            int defaultMode = AppOpsManager.opToDefaultMode(code2);
            int i3 = 0;
            UidState uidState = getUidStateLocked(i, false);
            String str = null;
            if (uidState == null) {
                if (i2 != defaultMode) {
                    UidState uidState2 = new UidState(i);
                    uidState2.opModes = new SparseIntArray();
                    uidState2.opModes.put(code2, i2);
                    this.mUidStates.put(i, uidState2);
                    scheduleWriteLocked();
                }
            } else if (uidState.opModes == null) {
                if (i2 != defaultMode) {
                    uidState.opModes = new SparseIntArray();
                    uidState.opModes.put(code2, i2);
                    scheduleWriteLocked();
                }
            } else if (uidState.opModes.get(code2) != i2) {
                if (i2 == defaultMode) {
                    uidState.opModes.delete(code2);
                    if (uidState.opModes.size() <= 0) {
                        uidState.opModes = null;
                    }
                } else {
                    uidState.opModes.put(code2, i2);
                }
                scheduleWriteLocked();
            }
        }
    }

    public void setMode(int code, int uid, String packageName, int mode) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.APPOPS_SETMODE);
        enforceManageAppOpsModes(Binder.getCallingPid(), Binder.getCallingUid(), uid);
        verifyIncomingOp(code);
        ArraySet<ModeCallback> repCbs = null;
        int code2 = AppOpsManager.opToSwitch(code);
        synchronized (this) {
            UidState uidState = getUidStateLocked(uid, false);
            Op op = getOpLocked(code2, uid, packageName, true);
            if (!(op == null || op.mode == mode)) {
                op.mode = mode;
                if (uidState != null) {
                    uidState.evalForegroundOps(this.mOpModeWatchers);
                }
                ArraySet<ModeCallback> cbs = this.mOpModeWatchers.get(code2);
                if (cbs != null) {
                    if (0 == 0) {
                        repCbs = new ArraySet<>();
                    }
                    repCbs.addAll(cbs);
                }
                ArraySet<ModeCallback> cbs2 = this.mPackageModeWatchers.get(packageName);
                if (cbs2 != null) {
                    if (repCbs == null) {
                        repCbs = new ArraySet<>();
                    }
                    repCbs.addAll(cbs2);
                }
                if (mode == AppOpsManager.opToDefaultMode(op.op)) {
                    pruneOp(op, uid, packageName);
                }
                scheduleFastWriteLocked();
            }
        }
        if (repCbs != null) {
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$1lQKm3WHEUQsD7KzYyJ5stQSc04.INSTANCE, this, repCbs, Integer.valueOf(code2), Integer.valueOf(uid), packageName));
        }
    }

    /* access modifiers changed from: private */
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
        int N = cbs.size();
        boolean duplicate = false;
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

    /* JADX WARNING: Code restructure failed: missing block: B:113:0x01f8, code lost:
        if (r11 == null) goto L_0x0262;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x01fa, code lost:
        r0 = r11.entrySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x0206, code lost:
        if (r0.hasNext() == false) goto L_0x0262;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0208, code lost:
        r12 = r0.next();
        r13 = r12.getKey();
        r14 = r12.getValue();
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x021e, code lost:
        r15 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0223, code lost:
        if (r15 >= r14.size()) goto L_0x025b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x0225, code lost:
        r6 = r14.get(r15);
        r5 = r7.mHandler;
        r1 = com.android.server.$$Lambda$AppOpsService$lxgFmOnGguOiLyfUZbyOpNBfTVw.INSTANCE;
        r4 = java.lang.Integer.valueOf(r6.op);
        r28 = r0;
        r0 = r5;
        r19 = r22;
        r5 = java.lang.Integer.valueOf(r6.uid);
        r20 = r6;
        r16 = r23;
        r0.sendMessage(com.android.internal.util.function.pooled.PooledLambda.obtainMessage(r1, r7, r13, r4, r5, r6.pkg));
        r1 = r15 + 1;
        r0 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x025b, code lost:
        r28 = r0;
        r19 = r22;
        r16 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x0262, code lost:
        r19 = r22;
        r16 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x0266, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00f5 A[Catch:{ all -> 0x01dd, all -> 0x01ef }] */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00f7 A[Catch:{ all -> 0x01dd, all -> 0x01ef }] */
    public void resetAllModes(int reqUserId, String reqPackageName) {
        int callingPid;
        int callingUid;
        Map.Entry<String, Ops> ent;
        Map<String, Ops> packages;
        boolean changed;
        HashMap<ModeCallback, ArrayList<ChangeRec>> callbacks;
        int callingPid2;
        int callingUid2;
        SparseIntArray opModes;
        int i;
        String packageName;
        String str = reqPackageName;
        int callingPid3 = Binder.getCallingPid();
        int callingUid3 = Binder.getCallingUid();
        int reqUserId2 = ActivityManager.handleIncomingUser(callingPid3, callingUid3, reqUserId, true, true, "resetAllModes", null);
        int reqUid = -1;
        if (str != null) {
            try {
                reqUid = AppGlobals.getPackageManager().getPackageUid(str, 8192, reqUserId2);
            } catch (RemoteException e) {
            }
        }
        int reqUid2 = reqUid;
        enforceManageAppOpsModes(callingPid3, callingUid3, reqUid2);
        synchronized (this) {
            boolean changed2 = false;
            try {
                int i2 = this.mUidStates.size() - 1;
                HashMap<ModeCallback, ArrayList<ChangeRec>> callbacks2 = null;
                while (true) {
                    int i3 = i2;
                    if (i3 < 0) {
                        break;
                    }
                    try {
                        UidState uidState = this.mUidStates.valueAt(i3);
                        SparseIntArray opModes2 = uidState.opModes;
                        if (opModes2 != null) {
                            if (uidState.uid != reqUid2) {
                                if (reqUid2 != -1) {
                                    SparseIntArray sparseIntArray = opModes2;
                                    callingUid = callingUid3;
                                    callingPid = callingPid3;
                                    if (uidState.pkgOps == null) {
                                        if (reqUserId2 == -1 || reqUserId2 == UserHandle.getUserId(uidState.uid)) {
                                            Map<String, Ops> packages2 = uidState.pkgOps;
                                            Iterator<Map.Entry<String, Ops>> it = packages2.entrySet().iterator();
                                            boolean uidChanged = false;
                                            while (it.hasNext()) {
                                                Map.Entry<String, Ops> ent2 = it.next();
                                                String packageName2 = ent2.getKey();
                                                if (str == null || str.equals(packageName2)) {
                                                    Ops pkgOps = ent2.getValue();
                                                    int j = pkgOps.size() - 1;
                                                    while (j >= 0) {
                                                        Op curOp = (Op) pkgOps.valueAt(j);
                                                        if (AppOpsManager.opAllowsReset(curOp.op)) {
                                                            changed = changed2;
                                                            if (curOp.mode != AppOpsManager.opToDefaultMode(curOp.op)) {
                                                                curOp.mode = AppOpsManager.opToDefaultMode(curOp.op);
                                                                uidChanged = true;
                                                                packages = packages2;
                                                                ent = ent2;
                                                                callbacks = addCallbacks(callbacks2, curOp.op, curOp.uid, packageName2, this.mOpModeWatchers.get(curOp.op));
                                                                HashMap<ModeCallback, ArrayList<ChangeRec>> callbacks3 = addCallbacks(callbacks, curOp.op, curOp.uid, packageName2, this.mPackageModeWatchers.get(packageName2));
                                                                if (!curOp.hasAnyTime()) {
                                                                    pkgOps.removeAt(j);
                                                                }
                                                                callbacks2 = callbacks3;
                                                                changed2 = true;
                                                                j--;
                                                                packages2 = packages;
                                                                ent2 = ent;
                                                            } else {
                                                                packages = packages2;
                                                                ent = ent2;
                                                            }
                                                        } else {
                                                            changed = changed2;
                                                            packages = packages2;
                                                            ent = ent2;
                                                        }
                                                        changed2 = changed;
                                                        j--;
                                                        packages2 = packages;
                                                        ent2 = ent;
                                                    }
                                                    boolean changed3 = changed2;
                                                    Map<String, Ops> packages3 = packages2;
                                                    Map.Entry<String, Ops> entry = ent2;
                                                    if (pkgOps.size() == 0) {
                                                        it.remove();
                                                    }
                                                    changed2 = changed3;
                                                    packages2 = packages3;
                                                }
                                            }
                                            if (uidState.isDefault()) {
                                                this.mUidStates.remove(uidState.uid);
                                            }
                                            if (uidChanged) {
                                                uidState.evalForegroundOps(this.mOpModeWatchers);
                                            }
                                        }
                                    }
                                    i2 = i3 - 1;
                                    callingUid3 = callingUid;
                                    callingPid3 = callingPid;
                                }
                            }
                            int j2 = opModes2.size() - 1;
                            while (j2 >= 0) {
                                int code = opModes2.keyAt(j2);
                                if (AppOpsManager.opAllowsReset(code)) {
                                    opModes2.removeAt(j2);
                                    if (opModes2.size() <= 0) {
                                        try {
                                            uidState.opModes = null;
                                        } catch (Throwable th) {
                                            th = th;
                                            int i4 = callingUid3;
                                            int i5 = callingPid3;
                                            HashMap<ModeCallback, ArrayList<ChangeRec>> hashMap = callbacks2;
                                        }
                                    }
                                    String[] packagesForUid = getPackagesForUid(uidState.uid);
                                    int length = packagesForUid.length;
                                    opModes = opModes2;
                                    callbacks = callbacks2;
                                    int i6 = 0;
                                    while (i6 < length) {
                                        try {
                                            i = length;
                                            callingUid = callingUid3;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            HashMap<ModeCallback, ArrayList<ChangeRec>> hashMap2 = callbacks;
                                            int i7 = callingUid3;
                                            int i8 = callingPid3;
                                            while (true) {
                                                try {
                                                    break;
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                }
                                            }
                                            throw th;
                                        }
                                        try {
                                            callingPid = callingPid3;
                                            packageName = packagesForUid[i6];
                                        } catch (Throwable th4) {
                                            th = th4;
                                            HashMap<ModeCallback, ArrayList<ChangeRec>> hashMap3 = callbacks;
                                            int i9 = callingPid3;
                                            int i10 = callingUid;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                        try {
                                            callbacks = addCallbacks(addCallbacks(callbacks, code, uidState.uid, packageName, this.mOpModeWatchers.get(code)), code, uidState.uid, packageName, this.mPackageModeWatchers.get(packageName));
                                            i6++;
                                            length = i;
                                            callingUid3 = callingUid;
                                            callingPid3 = callingPid;
                                        } catch (Throwable th5) {
                                            th = th5;
                                            HashMap<ModeCallback, ArrayList<ChangeRec>> hashMap4 = callbacks;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    }
                                    callingUid2 = callingUid3;
                                    callingPid2 = callingPid3;
                                    callbacks2 = callbacks;
                                } else {
                                    opModes = opModes2;
                                    callingUid2 = callingUid3;
                                    callingPid2 = callingPid3;
                                }
                                j2--;
                                opModes2 = opModes;
                                callingUid3 = callingUid2;
                                callingPid3 = callingPid2;
                            }
                        }
                        callingUid = callingUid3;
                        callingPid = callingPid3;
                        if (uidState.pkgOps == null) {
                        }
                        i2 = i3 - 1;
                        callingUid3 = callingUid;
                        callingPid3 = callingPid;
                    } catch (Throwable th6) {
                        th = th6;
                        HashMap<ModeCallback, ArrayList<ChangeRec>> hashMap5 = callbacks2;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
                int callingUid4 = callingUid3;
                int callingPid4 = callingPid3;
                if (changed2) {
                    scheduleFastWriteLocked();
                }
                try {
                } catch (Throwable th7) {
                    th = th7;
                    int i11 = callingUid4;
                    int i12 = callingPid4;
                    HashMap<ModeCallback, ArrayList<ChangeRec>> hashMap6 = callbacks2;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            } catch (Throwable th8) {
                th = th8;
                int i13 = callingUid3;
                int i14 = callingPid3;
                while (true) {
                    break;
                }
                throw th;
            }
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
        int i;
        int i2 = op;
        String str = packageName;
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        Preconditions.checkArgumentInRange(i2, -1, 77, "Invalid op code: " + i2);
        if (callback != null) {
            synchronized (this) {
                if (i2 != -1) {
                    try {
                        i = AppOpsManager.opToSwitch(op);
                    } catch (Throwable th) {
                        th = th;
                        int i3 = i2;
                        throw th;
                    }
                } else {
                    i = i2;
                }
                int op2 = i;
                try {
                    ModeCallback cb = this.mModeWatchers.get(callback.asBinder());
                    if (cb == null) {
                        ModeCallback modeCallback = new ModeCallback(callback, -1, flags, callingUid, callingPid);
                        cb = modeCallback;
                        this.mModeWatchers.put(callback.asBinder(), cb);
                    }
                    if (this.mAppOpsResource == null) {
                        this.mAppOpsResource = HwFrameworkFactory.getHwResource(14);
                    }
                    if (op2 != -1) {
                        ArraySet<ModeCallback> cbs = this.mOpModeWatchers.get(op2);
                        if (cbs == null) {
                            cbs = new ArraySet<>();
                            this.mOpModeWatchers.put(op2, cbs);
                        }
                        cbs.add(cb);
                    }
                    if (str != null) {
                        ArraySet<ModeCallback> cbs2 = this.mPackageModeWatchers.get(str);
                        if (cbs2 == null) {
                            cbs2 = new ArraySet<>();
                            this.mPackageModeWatchers.put(str, cbs2);
                        }
                        cbs2.add(cb);
                        if (this.mAppOpsResource != null) {
                            this.mAppOpsResource.acquire(Binder.getCallingUid(), str, 0, cbs2.size());
                        }
                    }
                    evalAllForegroundOpsLocked();
                } catch (Throwable th2) {
                    th = th2;
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
            int code2 = AppOpsManager.opToSwitch(code);
            UidState uidState = getUidStateLocked(uid, false);
            if (uidState == null || uidState.opModes == null || uidState.opModes.indexOfKey(code2) < 0) {
                Op op = getOpLocked(code2, uid, resolvedPackageName, false);
                if (op == null) {
                    int opToDefaultMode = AppOpsManager.opToDefaultMode(code2);
                    return opToDefaultMode;
                }
                int i = op.mode;
                return i;
            }
            int i2 = uidState.opModes.get(code2);
            return i2;
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
        try {
            return AppGlobals.getPackageManager().isPackageSuspendedForUser(pkg, UserHandle.getUserId(uid));
        } catch (RemoteException e) {
            throw new SecurityException("Could not talk to package manager service");
        }
    }

    private int checkRestrictionLocked(int code, int usage, int uid, String packageName) {
        SparseArray<Restriction> usageRestrictions = this.mAudioRestrictions.get(code);
        if (usageRestrictions != null) {
            Restriction r = usageRestrictions.get(usage);
            if (r != null && !r.exceptionPackages.contains(packageName)) {
                return r.mode;
            }
        }
        return 0;
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
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$UKMH8n9xZqCOX59uFPylskhjBgo.INSTANCE, this, Integer.valueOf(code), -2));
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
            Ops ops = getOpsRawLocked(uid, packageName, true, false);
            if (ops == null) {
                return 2;
            }
            Op op = getOpLocked(ops, code, true);
            if (isOpRestrictedLocked(uid, code, packageName)) {
                return 1;
            }
            UidState uidState = ops.uidState;
            if (op.duration == -1) {
                Slog.w(TAG, "Noting op not finished: uid " + uid + " pkg " + packageName + " code " + code + " time=" + op.time[uidState.state] + " duration=" + op.duration);
            }
            op.duration = 0;
            int switchCode = AppOpsManager.opToSwitch(code);
            if (uidState.opModes == null || uidState.opModes.indexOfKey(switchCode) < 0) {
                int mode = (switchCode != code ? getOpLocked(ops, switchCode, true) : op).getMode();
                if (mode != 0) {
                    op.rejectTime[uidState.state] = System.currentTimeMillis();
                    return mode;
                }
            } else {
                int uidMode = uidState.evalMode(uidState.opModes.get(switchCode));
                if (uidMode != 0) {
                    op.rejectTime[uidState.state] = System.currentTimeMillis();
                    return uidMode;
                }
            }
            op.time[uidState.state] = System.currentTimeMillis();
            op.rejectTime[uidState.state] = 0;
            op.proxyUid = proxyUid;
            op.proxyPackageName = proxyPackageName;
            return 0;
        }
    }

    public void startWatchingActive(int[] ops, IAppOpsActiveCallback callback) {
        int watchedUid = -1;
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WATCH_APPOPS") != 0) {
            watchedUid = callingUid;
        }
        if (ops != null) {
            Preconditions.checkArrayElementsInRange(ops, 0, 77, "Invalid op code in: " + Arrays.toString(ops));
        }
        if (callback != null) {
            synchronized (this) {
                SparseArray<ActiveCallback> callbacks = this.mActiveWatchers.get(callback.asBinder());
                if (callbacks == null) {
                    callbacks = new SparseArray<>();
                    this.mActiveWatchers.put(callback.asBinder(), callbacks);
                }
                SparseArray<ActiveCallback> callbacks2 = callbacks;
                ActiveCallback activeCallback = new ActiveCallback(callback, watchedUid, callingUid, callingPid);
                ActiveCallback activeCallback2 = activeCallback;
                for (int op : ops) {
                    callbacks2.put(op, activeCallback2);
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
                        if (i == 0) {
                            activeCallbacks.valueAt(i).destroy();
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00b8, code lost:
        return 0;
     */
    public int startOperation(IBinder token, int code, int uid, String packageName, boolean startIfModeDefault) {
        int i = code;
        int i2 = uid;
        verifyIncomingUid(i2);
        verifyIncomingOp(i);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName == null) {
            return 1;
        }
        ClientState client = (ClientState) token;
        synchronized (this) {
            try {
                Ops ops = getOpsRawLocked(i2, resolvedPackageName, true, false);
                if (ops == null) {
                    return 2;
                }
                Op op = getOpLocked(ops, i, true);
                if (isOpRestrictedLocked(i2, i, resolvedPackageName)) {
                    return 1;
                }
                int switchCode = AppOpsManager.opToSwitch(code);
                UidState uidState = ops.uidState;
                if (uidState.opModes == null || uidState.opModes.indexOfKey(switchCode) < 0) {
                    int mode = (switchCode != i ? getOpLocked(ops, switchCode, true) : op).getMode();
                    if (mode != 0 && (!startIfModeDefault || mode != 3)) {
                        op.rejectTime[uidState.state] = System.currentTimeMillis();
                        return mode;
                    }
                } else {
                    int uidMode = uidState.evalMode(uidState.opModes.get(switchCode));
                    if (uidMode != 0 && (!startIfModeDefault || uidMode != 3)) {
                        op.rejectTime[uidState.state] = System.currentTimeMillis();
                        return uidMode;
                    }
                }
                if (op.startNesting == 0) {
                    op.startRealtime = SystemClock.elapsedRealtime();
                    op.time[uidState.state] = System.currentTimeMillis();
                    op.rejectTime[uidState.state] = 0;
                    op.duration = -1;
                    scheduleOpActiveChangedIfNeededLocked(i, i2, packageName, true);
                } else {
                    String str = packageName;
                }
                op.startNesting++;
                uidState.startNesting++;
                if (client.mStartedOps != null) {
                    client.mStartedOps.add(op);
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00b3, code lost:
        return;
     */
    public void finishOperation(IBinder token, int code, int uid, String packageName) {
        verifyIncomingUid(uid);
        verifyIncomingOp(code);
        String resolvedPackageName = resolvePackageName(uid, packageName);
        if (resolvedPackageName != null && (token instanceof ClientState)) {
            ClientState client = (ClientState) token;
            synchronized (this) {
                Op op = getOpLocked(code, uid, resolvedPackageName, true);
                if (op != null) {
                    if (!client.mStartedOps.remove(op)) {
                        long identity = Binder.clearCallingIdentity();
                        try {
                            if (((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getPackageUid(resolvedPackageName, 0, UserHandle.getUserId(uid)) < 0) {
                                Slog.i(TAG, "Finishing op=" + AppOpsManager.opToName(code) + " for non-existing package=" + resolvedPackageName + " in uid=" + uid);
                                return;
                            }
                            Binder.restoreCallingIdentity(identity);
                            Slog.wtf(TAG, "Operation not started: uid=" + op.uid + " pkg=" + op.packageName + " op=" + AppOpsManager.opToName(op.op));
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
            this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$NC5g1JY4YR6y4VePru4TO7AKp8M.INSTANCE, this, dispatchedCallbacks, Integer.valueOf(code), Integer.valueOf(uid), packageName, Boolean.valueOf(active)));
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

    public int permissionToOpCode(String permission) {
        if (permission == null) {
            return -1;
        }
        return AppOpsManager.permissionToOpCode(permission);
    }

    /* access modifiers changed from: package-private */
    public void finishOperationLocked(Op op, boolean finishNested) {
        if (op.startNesting <= 1 || finishNested) {
            if (op.startNesting == 1 || finishNested) {
                op.duration = (int) (SystemClock.elapsedRealtime() - op.startRealtime);
                op.time[op.uidState.state] = System.currentTimeMillis();
            } else {
                Slog.w(TAG, "Finishing op nesting under-run: uid " + op.uid + " pkg " + op.packageName + " code " + op.op + " time=" + op.time + " duration=" + op.duration + " nesting=" + op.startNesting);
            }
            if (op.startNesting >= 1) {
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
        if (op < 0 || op >= 78) {
            throw new IllegalArgumentException("Bad operation #" + op);
        }
    }

    private UidState getUidStateLocked(int uid, boolean edit) {
        UidState uidState = this.mUidStates.get(uid);
        if (uidState == null) {
            if (!edit) {
                return null;
            }
            uidState = new UidState(uid);
            this.mUidStates.put(uid, uidState);
        } else if (uidState.pendingStateCommitTime != 0) {
            if (uidState.pendingStateCommitTime < this.mLastUptime) {
                commitUidPendingStateLocked(uidState);
            } else {
                this.mLastUptime = SystemClock.uptimeMillis();
                if (uidState.pendingStateCommitTime < this.mLastUptime) {
                    commitUidPendingStateLocked(uidState);
                }
            }
        }
        return uidState;
    }

    private void commitUidPendingStateLocked(UidState uidState) {
        int pkgi;
        ModeCallback callback;
        int i;
        UidState uidState2 = uidState;
        boolean z = true;
        boolean lastForeground = uidState2.state <= 2;
        boolean nowForeground = uidState2.pendingState <= 2;
        uidState2.state = uidState2.pendingState;
        uidState2.pendingStateCommitTime = 0;
        if (uidState2.hasForegroundWatchers && lastForeground != nowForeground) {
            int cbi = uidState2.foregroundOps.size() - 1;
            while (true) {
                int fgi = cbi;
                if (fgi >= 0) {
                    if (uidState2.foregroundOps.valueAt(fgi)) {
                        int code = uidState2.foregroundOps.keyAt(fgi);
                        ArraySet<ModeCallback> callbacks = this.mOpModeWatchers.get(code);
                        if (callbacks != null) {
                            int pkgi2 = callbacks.size() - z;
                            while (true) {
                                int cbi2 = pkgi2;
                                if (cbi2 < 0) {
                                    break;
                                }
                                ModeCallback callback2 = callbacks.valueAt(cbi2);
                                if ((callback2.mFlags != false && z) && callback2.isWatchingUid(uidState2.uid)) {
                                    int i2 = 4;
                                    boolean doAllPackages = (uidState2.opModes == null || uidState2.opModes.get(code) != 4) ? false : z;
                                    if (uidState2.pkgOps != null) {
                                        int pkgi3 = uidState2.pkgOps.size() - z;
                                        while (true) {
                                            int pkgi4 = pkgi3;
                                            if (pkgi4 < 0) {
                                                break;
                                            }
                                            Op op = (Op) uidState2.pkgOps.valueAt(pkgi4).get(code);
                                            if (doAllPackages || (op != null && op.mode == i2)) {
                                                Op op2 = op;
                                                pkgi = pkgi4;
                                                i = 4;
                                                callback = callback2;
                                                this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$lxgFmOnGguOiLyfUZbyOpNBfTVw.INSTANCE, this, callback2, Integer.valueOf(code), Integer.valueOf(uidState2.uid), uidState2.pkgOps.keyAt(pkgi4)));
                                            } else {
                                                pkgi = pkgi4;
                                                i = i2;
                                                callback = callback2;
                                            }
                                            pkgi3 = pkgi - 1;
                                            i2 = i;
                                            callback2 = callback;
                                        }
                                    }
                                }
                                pkgi2 = cbi2 - 1;
                                z = true;
                            }
                        }
                    }
                    cbi = fgi - 1;
                    z = true;
                } else {
                    return;
                }
            }
        }
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
        if (ops == null) {
            if (!edit) {
                return null;
            }
            boolean isPrivileged = false;
            if (uid != 0) {
                long ident = Binder.clearCallingIdentity();
                int pkgUid = -1;
                try {
                    ApplicationInfo appInfo = ActivityThread.getPackageManager().getApplicationInfo(packageName, 268435456, UserHandle.getUserId(uid));
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
                        new RuntimeException("here").fillInStackTrace();
                        Slog.w(TAG, "Bad call: specified package " + packageName + " under uid " + uid + " but it is really " + pkgUid);
                    }
                    Binder.restoreCallingIdentity(ident);
                    return null;
                }
                Binder.restoreCallingIdentity(ident);
            }
            ops = new Ops(packageName, uidState, isPrivileged);
            uidState.pkgOps.put(packageName, ops);
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
        Ops ops = getOpsRawLocked(uid, packageName, edit, false);
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

    private boolean isOpRestrictedLocked(int uid, int code, String packageName) {
        int userHandle = UserHandle.getUserId(uid);
        int restrictionSetCount = this.mOpUserRestrictions.size();
        int i = 0;
        while (i < restrictionSetCount) {
            ClientRestrictionState restrictionState = this.mOpUserRestrictions.valueAt(i);
            if (restrictionState == null || !restrictionState.hasRestriction(code, packageName, userHandle)) {
                i++;
            } else {
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
    /* JADX WARNING: Removed duplicated region for block: B:16:0x002e A[Catch:{ IllegalStateException -> 0x015b, NullPointerException -> 0x0138, NumberFormatException -> 0x0115, XmlPullParserException -> 0x00f3, IOException -> 0x00d1, IndexOutOfBoundsException -> 0x00af }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a3 A[SYNTHETIC, Splitter:B:44:0x00a3] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:73:0x0111=Splitter:B:73:0x0111, B:82:0x0133=Splitter:B:82:0x0133, B:91:0x0156=Splitter:B:91:0x0156, B:100:0x0179=Splitter:B:100:0x0179, B:41:0x009b=Splitter:B:41:0x009b, B:55:0x00cd=Splitter:B:55:0x00cd, B:64:0x00ef=Splitter:B:64:0x00ef} */
    public void readState() {
        int type;
        int oldVersion = -1;
        synchronized (this.mFile) {
            synchronized (this) {
                try {
                    FileInputStream stream = this.mFile.openRead();
                    try {
                        this.mUidStates.clear();
                        try {
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setInput(stream, StandardCharsets.UTF_8.name());
                            while (true) {
                                int next = parser.next();
                                type = next;
                                if (next == 2 || type == 1) {
                                    if (type != 2) {
                                        String versionString = parser.getAttributeValue(null, "v");
                                        if (versionString != null) {
                                            oldVersion = Integer.parseInt(versionString);
                                        }
                                        int outerDepth = parser.getDepth();
                                        while (true) {
                                            int next2 = parser.next();
                                            int type2 = next2;
                                            if (next2 != 1 && (type2 != 3 || parser.getDepth() > outerDepth)) {
                                                if (type2 != 3) {
                                                    if (type2 != 4) {
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
                                        }
                                        if (1 == 0) {
                                            this.mUidStates.clear();
                                        }
                                        try {
                                            stream.close();
                                        } catch (IOException e) {
                                        }
                                    } else {
                                        throw new IllegalStateException("no start tag found");
                                    }
                                }
                            }
                            if (type != 2) {
                            }
                        } catch (IllegalStateException e2) {
                            Slog.w(TAG, "Failed parsing " + e2);
                            if (0 == 0) {
                                this.mUidStates.clear();
                            }
                            stream.close();
                        } catch (NullPointerException e3) {
                            Slog.w(TAG, "Failed parsing " + e3);
                            if (0 == 0) {
                                this.mUidStates.clear();
                            }
                            stream.close();
                        } catch (NumberFormatException e4) {
                            Slog.w(TAG, "Failed parsing " + e4);
                            if (0 == 0) {
                                this.mUidStates.clear();
                            }
                            stream.close();
                        } catch (XmlPullParserException e5) {
                            Slog.w(TAG, "Failed parsing " + e5);
                            if (0 == 0) {
                                this.mUidStates.clear();
                            }
                            stream.close();
                        } catch (IOException e6) {
                            Slog.w(TAG, "Failed parsing " + e6);
                            if (0 == 0) {
                                this.mUidStates.clear();
                            }
                            stream.close();
                        } catch (IndexOutOfBoundsException e7) {
                            Slog.w(TAG, "Failed parsing " + e7);
                            if (0 == 0) {
                                this.mUidStates.clear();
                            }
                            stream.close();
                        }
                    } catch (Throwable th) {
                        while (true) {
                            throw th;
                        }
                    }
                } catch (FileNotFoundException e8) {
                    Slog.i(TAG, "No existing app ops " + this.mFile.getBaseFile() + "; starting empty");
                    return;
                }
            }
        }
        synchronized (this) {
            upgradeLocked(oldVersion);
        }
    }

    private void upgradeRunAnyInBackgroundLocked() {
        for (int i = 0; i < this.mUidStates.size(); i++) {
            UidState uidState = this.mUidStates.valueAt(i);
            if (uidState != null) {
                if (uidState.opModes != null) {
                    int idx = uidState.opModes.indexOfKey(63);
                    if (idx >= 0) {
                        uidState.opModes.put(70, uidState.opModes.valueAt(idx));
                    }
                }
                if (uidState.pkgOps != null) {
                    boolean changed = false;
                    for (int j = 0; j < uidState.pkgOps.size(); j++) {
                        Ops ops = uidState.pkgOps.valueAt(j);
                        if (ops != null) {
                            Op op = (Op) ops.get(63);
                            if (!(op == null || op.mode == AppOpsManager.opToDefaultMode(op.op))) {
                                Op copy = new Op(op.uidState, op.packageName, 70);
                                copy.mode = op.mode;
                                ops.put(70, copy);
                                changed = true;
                            }
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
            Slog.d(TAG, "Upgrading app-ops xml from version " + oldVersion + " to " + 1);
            if (oldVersion == -1) {
                upgradeRunAnyInBackgroundLocked();
            }
            scheduleFastWriteLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void readUidOps(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        int uid = Integer.parseInt(parser.getAttributeValue(null, "n"));
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
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

    /* access modifiers changed from: package-private */
    public void readPackage(XmlPullParser parser) throws NumberFormatException, XmlPullParserException, IOException {
        String pkgName = parser.getAttributeValue(null, "n");
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x020a, code lost:
        r24 = r0;
        r17 = 3;
        r20 = 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0280, code lost:
        r24 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x0282, code lost:
        r9 = r9 - 1;
        r11 = r17;
        r13 = r20;
        r0 = r24;
        r10 = 1;
     */
    public void readUid(XmlPullParser parser, String pkgName) throws NumberFormatException, XmlPullParserException, IOException {
        int outerDepth;
        char c;
        int outerDepth2;
        char c2;
        char c3;
        XmlPullParser xmlPullParser = parser;
        String str = pkgName;
        String str2 = null;
        int uid = Integer.parseInt(xmlPullParser.getAttributeValue(null, "n"));
        String isPrivilegedString = xmlPullParser.getAttributeValue(null, "p");
        boolean isPrivileged = false;
        boolean z = true;
        if (isPrivilegedString == null) {
            try {
                if (ActivityThread.getPackageManager() != null) {
                    ApplicationInfo appInfo = ActivityThread.getPackageManager().getApplicationInfo(str, 0, UserHandle.getUserId(uid));
                    if (appInfo != null) {
                        isPrivileged = (appInfo.privateFlags & 8) != 0;
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
        int outerDepth3 = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == z) {
            } else if (type != 3 || parser.getDepth() > outerDepth3) {
                if (type == 3) {
                    outerDepth = outerDepth3;
                } else if (type == 4) {
                    outerDepth = outerDepth3;
                } else if (parser.getName().equals("op")) {
                    UidState uidState = getUidStateLocked(uid, z);
                    if (uidState.pkgOps == null) {
                        uidState.pkgOps = new ArrayMap<>();
                    }
                    Op op = new Op(uidState, str, Integer.parseInt(xmlPullParser.getAttributeValue(str2, "n")));
                    int i = parser.getAttributeCount() - (z ? 1 : 0);
                    char c4 = z;
                    while (i >= 0) {
                        String name = xmlPullParser.getAttributeName(i);
                        String value = xmlPullParser.getAttributeValue(i);
                        switch (name.hashCode()) {
                            case 100:
                                if (name.equals("d")) {
                                    c = c4;
                                    break;
                                }
                            case 109:
                                if (name.equals("m")) {
                                    c = 0;
                                    break;
                                }
                            case 114:
                                if (name.equals("r")) {
                                    c = 17;
                                    break;
                                }
                            case HdmiCecKeycode.CEC_KEYCODE_F4_YELLOW /*116*/:
                                if (name.equals("t")) {
                                    c = 16;
                                    break;
                                }
                            case 3584:
                                if (name.equals("pp")) {
                                    c = 3;
                                    break;
                                }
                            case 3589:
                                if (name.equals("pu")) {
                                    c = 2;
                                    break;
                                }
                            case 3632:
                                if (name.equals("rb")) {
                                    c = 14;
                                    break;
                                }
                            case 3633:
                                if (name.equals("rc")) {
                                    c = 15;
                                    break;
                                }
                            case 3636:
                                if (name.equals("rf")) {
                                    c = 13;
                                    break;
                                }
                            case 3646:
                                if (name.equals("rp")) {
                                    c = 10;
                                    break;
                                }
                            case 3650:
                                if (name.equals("rt")) {
                                    c = 11;
                                    break;
                                }
                            case 3694:
                                if (name.equals("tb")) {
                                    c = 8;
                                    break;
                                }
                            case 3695:
                                if (name.equals("tc")) {
                                    c = 9;
                                    break;
                                }
                            case 3698:
                                if (name.equals("tf")) {
                                    c = 7;
                                    break;
                                }
                            case 3708:
                                if (name.equals("tp")) {
                                    c = 4;
                                    break;
                                }
                            case 3712:
                                if (name.equals("tt")) {
                                    c = 5;
                                    break;
                                }
                            case 112831:
                                if (name.equals("rfs")) {
                                    c = 12;
                                    break;
                                }
                            case 114753:
                                if (name.equals("tfs")) {
                                    c = 6;
                                    break;
                                }
                            default:
                                c = 65535;
                                break;
                        }
                        switch (c) {
                            case 0:
                                c3 = 3;
                                c2 = 4;
                                op.mode = Integer.parseInt(value);
                                break;
                            case 1:
                                c3 = 3;
                                c2 = 4;
                                op.duration = Integer.parseInt(value);
                                break;
                            case 2:
                                c3 = 3;
                                c2 = 4;
                                op.proxyUid = Integer.parseInt(value);
                                break;
                            case 3:
                                c3 = 3;
                                c2 = 4;
                                op.proxyPackageName = value;
                                break;
                            case 4:
                                c3 = 3;
                                c2 = 4;
                                op.time[0] = Long.parseLong(value);
                                break;
                            case 5:
                                c3 = 3;
                                c2 = 4;
                                op.time[c4] = Long.parseLong(value);
                                break;
                            case 6:
                                c3 = 3;
                                c2 = 4;
                                op.time[2] = Long.parseLong(value);
                                break;
                            case 7:
                                c2 = 4;
                                c3 = 3;
                                op.time[3] = Long.parseLong(value);
                                break;
                            case 8:
                                c2 = 4;
                                op.time[4] = Long.parseLong(value);
                                outerDepth2 = outerDepth3;
                                c3 = 3;
                                break;
                            case 9:
                                op.time[5] = Long.parseLong(value);
                                break;
                            case 10:
                                op.rejectTime[0] = Long.parseLong(value);
                                break;
                            case 11:
                                op.rejectTime[c4] = Long.parseLong(value);
                                break;
                            case 12:
                                op.rejectTime[2] = Long.parseLong(value);
                                break;
                            case 13:
                                op.rejectTime[3] = Long.parseLong(value);
                                break;
                            case 14:
                                op.rejectTime[4] = Long.parseLong(value);
                                break;
                            case 15:
                                op.rejectTime[5] = Long.parseLong(value);
                                break;
                            case 16:
                                op.time[c4] = Long.parseLong(value);
                                break;
                            case 17:
                                op.rejectTime[c4] = Long.parseLong(value);
                                break;
                            default:
                                c3 = 3;
                                c2 = 4;
                                StringBuilder sb = new StringBuilder();
                                outerDepth2 = outerDepth3;
                                sb.append("Unknown attribute in 'op' tag: ");
                                sb.append(name);
                                Slog.w(TAG, sb.toString());
                                break;
                        }
                    }
                    outerDepth = outerDepth3;
                    Ops ops = uidState.pkgOps.get(str);
                    if (ops == null) {
                        ops = new Ops(str, uidState, isPrivileged);
                        uidState.pkgOps.put(str, ops);
                    }
                    ops.put(op.op, op);
                } else {
                    outerDepth = outerDepth3;
                    Slog.w(TAG, "Unknown element under <pkg>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
                outerDepth3 = outerDepth;
                str2 = null;
                z = true;
            } else {
                int i2 = outerDepth3;
            }
        }
        UidState uidState2 = getUidStateLocked(uid, false);
        if (uidState2 != null) {
            uidState2.evalForegroundOps(this.mOpModeWatchers);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x026a, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
        r10 = r9.getOps();
        r11 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0140, code lost:
        if (r11 >= r10.size()) goto L_0x021d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0142, code lost:
        r13 = r10.get(r11);
        r5.startTag(r0, "op");
        r5.attribute(r0, "n", java.lang.Integer.toString(r13.getOp()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0168, code lost:
        if (r13.getMode() == android.app.AppOpsManager.opToDefaultMode(r13.getOp())) goto L_0x0178;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:?, code lost:
        r5.attribute(r0, "m", java.lang.Integer.toString(r13.getMode()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0178, code lost:
        r14 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x017a, code lost:
        if (r14 >= 6) goto L_0x01d0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
        r19 = r13;
        r12 = r13.getLastTimeFor(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x018a, code lost:
        if (r12 == 0) goto L_0x019b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0194, code lost:
        r20 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:?, code lost:
        r5.attribute(null, UID_STATE_TIME_ATTRS[r14], java.lang.Long.toString(r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x019b, code lost:
        r20 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x019d, code lost:
        r0 = r19;
        r24 = r8;
        r23 = r9;
        r8 = r0.getLastRejectTimeFor(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01af, code lost:
        if (r8 == 0) goto L_0x01c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01b1, code lost:
        r25 = r6;
        r5.attribute(null, UID_STATE_REJECT_ATTRS[r14], java.lang.Long.toString(r8));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01c0, code lost:
        r25 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01c2, code lost:
        r14 = r14 + 1;
        r13 = r0;
        r4 = r20;
        r9 = r23;
        r8 = r24;
        r6 = r25;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01d0, code lost:
        r20 = r4;
        r25 = r6;
        r24 = r8;
        r23 = r9;
        r0 = r13;
        r4 = r0.getDuration();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01dd, code lost:
        if (r4 == 0) goto L_0x01e9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01df, code lost:
        r5.attribute(null, "d", java.lang.Integer.toString(r4));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01e9, code lost:
        r6 = r0.getProxyUid();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x01ee, code lost:
        if (r6 == -1) goto L_0x01fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01f0, code lost:
        r5.attribute(null, "pu", java.lang.Integer.toString(r6));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x01fb, code lost:
        r8 = r0.getProxyPackageName();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01ff, code lost:
        if (r8 == null) goto L_0x0208;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0201, code lost:
        r5.attribute(null, "pp", r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0208, code lost:
        r5.endTag(null, "op");
        r11 = r11 + 1;
        r4 = r20;
        r9 = r23;
        r8 = r24;
        r6 = r25;
        r0 = null;
        r12 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x021d, code lost:
        r20 = r4;
        r25 = r6;
        r24 = r8;
        r23 = r9;
        r5.endTag(null, "uid");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x022c, code lost:
        r8 = r24;
     */
    public void writeState() {
        int uidStateCount;
        List<AppOpsManager.PackageOps> allOps;
        int i;
        synchronized (this.mFile) {
            try {
                FileOutputStream stream = this.mFile.startWrite();
                String str = null;
                List<AppOpsManager.PackageOps> allOps2 = getPackagesForOps(null);
                try {
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(stream, StandardCharsets.UTF_8.name());
                    out.startDocument(null, true);
                    out.startTag(null, "app-ops");
                    out.attribute(null, "v", String.valueOf(1));
                    int uidStateCount2 = this.mUidStates.size();
                    int i2 = 0;
                    while (i2 < uidStateCount2) {
                        try {
                            UidState uidState = this.mUidStates.valueAt(i2);
                            if (!(uidState == null || uidState.opModes == null || uidState.opModes.size() <= 0)) {
                                out.startTag(null, "uid");
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
                                out.endTag(null, "uid");
                            }
                            i2++;
                        } catch (IOException e) {
                            e = e;
                            List<AppOpsManager.PackageOps> list = allOps2;
                            Slog.w(TAG, "Failed to write state, restoring backup.", e);
                            this.mFile.failWrite(stream);
                        }
                    }
                    if (allOps2 != null) {
                        String lastPkg = null;
                        int i3 = 0;
                        while (i3 < allOps2.size()) {
                            AppOpsManager.PackageOps pkg = allOps2.get(i3);
                            if (pkg == null) {
                                allOps = allOps2;
                                uidStateCount = uidStateCount2;
                            } else if (pkg.getPackageName() == null) {
                                allOps = allOps2;
                                uidStateCount = uidStateCount2;
                            } else {
                                if (!pkg.getPackageName().equals(lastPkg)) {
                                    if (lastPkg != null) {
                                        out.endTag(str, AbsLocationManagerService.DEL_PKG);
                                    }
                                    lastPkg = pkg.getPackageName();
                                    out.startTag(str, AbsLocationManagerService.DEL_PKG);
                                    out.attribute(str, "n", lastPkg);
                                }
                                out.startTag(str, "uid");
                                out.attribute(str, "n", Integer.toString(pkg.getUid()));
                                synchronized (this) {
                                    try {
                                        Ops ops = getOpsRawLocked(pkg.getUid(), pkg.getPackageName(), false, false);
                                        if (ops != null) {
                                            try {
                                                out.attribute(str, "p", Boolean.toString(ops.isPrivileged));
                                                i = 0;
                                            } catch (Throwable th) {
                                                th = th;
                                                List<AppOpsManager.PackageOps> list2 = allOps2;
                                                int i4 = uidStateCount2;
                                                String str2 = lastPkg;
                                                AppOpsManager.PackageOps packageOps = pkg;
                                                while (true) {
                                                    try {
                                                        break;
                                                    } catch (Throwable th2) {
                                                        th = th2;
                                                    }
                                                }
                                                throw th;
                                            }
                                        } else {
                                            i = 0;
                                            out.attribute(str, "p", Boolean.toString(false));
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        List<AppOpsManager.PackageOps> list3 = allOps2;
                                        int i5 = uidStateCount2;
                                        String str3 = lastPkg;
                                        AppOpsManager.PackageOps packageOps2 = pkg;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                            }
                            i3++;
                            allOps2 = allOps;
                            uidStateCount2 = uidStateCount;
                            str = null;
                        }
                        int i6 = uidStateCount2;
                        if (lastPkg != null) {
                            out.endTag(null, AbsLocationManagerService.DEL_PKG);
                        }
                    } else {
                        int i7 = uidStateCount2;
                    }
                    out.endTag(null, "app-ops");
                    out.endDocument();
                    this.mFile.finishWrite(stream);
                } catch (IOException e2) {
                    e = e2;
                    List<AppOpsManager.PackageOps> list4 = allOps2;
                    Slog.w(TAG, "Failed to write state, restoring backup.", e);
                    this.mFile.failWrite(stream);
                }
            } catch (IOException e3) {
                Slog.w(TAG, "Failed to write state: " + e3);
            } catch (Throwable th4) {
                throw th4;
            }
        }
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    static int onShellCommand(Shell shell, String cmd) {
        char c;
        List<AppOpsManager.PackageOps> ops;
        long token;
        Shell shell2 = shell;
        String str = cmd;
        if (str == null) {
            return shell.handleDefaultCommands(cmd);
        }
        PrintWriter pw = shell.getOutPrintWriter();
        PrintWriter err = shell.getErrPrintWriter();
        try {
            switch (cmd.hashCode()) {
                case -1703718319:
                    if (str.equals("write-settings")) {
                        c = 4;
                        break;
                    }
                case -1166702330:
                    if (str.equals("query-op")) {
                        c = 2;
                        break;
                    }
                case 102230:
                    if (str.equals("get")) {
                        c = 1;
                        break;
                    }
                case 113762:
                    if (str.equals("set")) {
                        c = 0;
                        break;
                    }
                case 3540994:
                    if (str.equals("stop")) {
                        c = 7;
                        break;
                    }
                case 108404047:
                    if (str.equals("reset")) {
                        c = 3;
                        break;
                    }
                case 109757538:
                    if (str.equals("start")) {
                        c = 6;
                        break;
                    }
                case 2085703290:
                    if (str.equals("read-settings")) {
                        c = 5;
                        break;
                    }
            }
            c = 65535;
            switch (c) {
                case 0:
                    int res = shell2.parseUserPackageOp(true, err);
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
                    if (shell2.packageName != null) {
                        shell2.mInterface.setMode(shell2.op, shell2.packageUid, shell2.packageName, mode);
                    } else {
                        shell2.mInterface.setUidMode(shell2.op, shell2.nonpackageUid, mode);
                    }
                    return 0;
                case 1:
                    int res2 = shell2.parseUserPackageOp(false, err);
                    if (res2 < 0) {
                        return res2;
                    }
                    int[] iArr = null;
                    if (shell2.packageName != null) {
                        IAppOpsService iAppOpsService = shell2.mInterface;
                        int i = shell2.packageUid;
                        String str2 = shell2.packageName;
                        if (shell2.op != -1) {
                            iArr = new int[]{shell2.op};
                        }
                        ops = iAppOpsService.getOpsForPackage(i, str2, iArr);
                    } else {
                        IAppOpsService iAppOpsService2 = shell2.mInterface;
                        int i2 = shell2.nonpackageUid;
                        if (shell2.op != -1) {
                            iArr = new int[]{shell2.op};
                        }
                        ops = iAppOpsService2.getUidOps(i2, iArr);
                    }
                    if (ops != null) {
                        if (ops.size() > 0) {
                            long now = System.currentTimeMillis();
                            for (int i3 = 0; i3 < ops.size(); i3++) {
                                List<AppOpsManager.OpEntry> entries = ops.get(i3).getOps();
                                for (int j = 0; j < entries.size(); j++) {
                                    AppOpsManager.OpEntry ent = entries.get(j);
                                    pw.print(AppOpsManager.opToName(ent.getOp()));
                                    pw.print(": ");
                                    pw.print(AppOpsManager.modeToName(ent.getMode()));
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
                        }
                    }
                    pw.println("No operations.");
                    if (shell2.op > -1 && shell2.op < 78) {
                        pw.println("Default mode: " + AppOpsManager.modeToName(AppOpsManager.opToDefaultMode(shell2.op)));
                    }
                    return 0;
                case 2:
                    int res3 = shell2.parseUserOpMode(1, err);
                    if (res3 < 0) {
                        return res3;
                    }
                    List<AppOpsManager.PackageOps> ops2 = shell2.mInterface.getPackagesForOps(new int[]{shell2.op});
                    if (ops2 != null) {
                        if (ops2.size() > 0) {
                            for (int i4 = 0; i4 < ops2.size(); i4++) {
                                AppOpsManager.PackageOps pkg = ops2.get(i4);
                                boolean hasMatch = false;
                                List<AppOpsManager.OpEntry> entries2 = ops2.get(i4).getOps();
                                int j2 = 0;
                                while (true) {
                                    if (j2 < entries2.size()) {
                                        AppOpsManager.OpEntry ent2 = entries2.get(j2);
                                        if (ent2.getOp() == shell2.op && ent2.getMode() == shell2.mode) {
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
                        }
                    }
                    pw.println("No operations.");
                    return 0;
                case 3:
                    String packageName = null;
                    int userId = -2;
                    while (true) {
                        String nextArg = shell.getNextArg();
                        String argument = nextArg;
                        if (nextArg == null) {
                            if (userId == -2) {
                                userId = ActivityManager.getCurrentUser();
                            }
                            shell2.mInterface.resetAllModes(userId, packageName);
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
                    shell2.mInternal.enforceManageAppOpsModes(Binder.getCallingPid(), Binder.getCallingUid(), -1);
                    long token2 = Binder.clearCallingIdentity();
                    try {
                        synchronized (shell2.mInternal) {
                            shell2.mInternal.mHandler.removeCallbacks(shell2.mInternal.mWriteRunner);
                        }
                        shell2.mInternal.writeState();
                        pw.println("Current settings written.");
                        Binder.restoreCallingIdentity(token2);
                        return 0;
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(token2);
                        throw th;
                    }
                case 5:
                    shell2.mInternal.enforceManageAppOpsModes(Binder.getCallingPid(), Binder.getCallingUid(), -1);
                    token = Binder.clearCallingIdentity();
                    shell2.mInternal.readState();
                    pw.println("Last settings read.");
                    Binder.restoreCallingIdentity(token);
                    return 0;
                case 6:
                    int res4 = shell2.parseUserPackageOp(true, err);
                    if (res4 < 0) {
                        return res4;
                    }
                    if (shell2.packageName == null) {
                        return -1;
                    }
                    shell2.mInterface.startOperation(shell2.mToken, shell2.op, shell2.packageUid, shell2.packageName, true);
                    return 0;
                case 7:
                    int res5 = shell2.parseUserPackageOp(true, err);
                    if (res5 < 0) {
                        return res5;
                    }
                    if (shell2.packageName == null) {
                        return -1;
                    }
                    shell2.mInterface.finishOperation(shell2.mToken, shell2.op, shell2.packageUid, shell2.packageName);
                    return 0;
                default:
                    return shell.handleDefaultCommands(cmd);
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        } catch (Throwable th2) {
            Binder.restoreCallingIdentity(token);
            throw th2;
        }
        pw.println("Remote exception: " + e);
        return -1;
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
    }

    private void dumpTimesLocked(PrintWriter pw, String firstPrefix, String prefix, long[] times, long now, SimpleDateFormat sdf, Date date) {
        PrintWriter printWriter = pw;
        boolean hasTime = false;
        int i = 0;
        while (true) {
            if (i >= 6) {
                break;
            } else if (times[i] != 0) {
                hasTime = true;
                break;
            } else {
                i++;
            }
        }
        if (hasTime) {
            boolean first = true;
            for (int i2 = 0; i2 < 6; i2++) {
                if (times[i2] != 0) {
                    printWriter.print(first ? firstPrefix : prefix);
                    first = false;
                    printWriter.print(UID_STATE_NAMES[i2]);
                    printWriter.print(" = ");
                    date.setTime(times[i2]);
                    printWriter.print(sdf.format(date));
                    printWriter.print(" (");
                    TimeUtils.formatDuration(times[i2] - now, printWriter);
                    printWriter.println(")");
                } else {
                    Date date2 = date;
                }
            }
            Date date3 = date;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:282:0x0582 A[Catch:{ all -> 0x0538 }] */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x0585 A[Catch:{ all -> 0x0538 }] */
    /* JADX WARNING: Removed duplicated region for block: B:285:0x0588 A[Catch:{ all -> 0x0538 }] */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x058b A[Catch:{ all -> 0x0538 }] */
    /* JADX WARNING: Removed duplicated region for block: B:302:0x05c1 A[Catch:{ all -> 0x0538 }] */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x063c  */
    /* JADX WARNING: Removed duplicated region for block: B:344:0x0650  */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int dumpOp;
        int dumpMode;
        String dumpPackage;
        int dumpUid;
        boolean needSep;
        long now;
        boolean z;
        int dumpUid2;
        long now2;
        IBinder token;
        int userRestrictionCount;
        long now3;
        IBinder token2;
        int dumpOp2;
        int dumpMode2;
        String dumpPackage2;
        int dumpUid3;
        long nowUptime;
        long now4;
        AppOpsService appOpsService;
        PrintWriter printWriter;
        int i;
        SimpleDateFormat sdf;
        int i2;
        boolean needSep2;
        long nowUptime2;
        String dumpPackage3;
        int dumpMode3;
        int dumpOp3;
        PrintWriter printWriter2;
        AppOpsService appOpsService2;
        long now5;
        int dumpUid4;
        int i3;
        SimpleDateFormat sdf2;
        int dumpOp4;
        boolean printedPackage;
        int j;
        ArrayMap<String, Ops> pkgOps;
        int dumpMode4;
        String dumpPackage4;
        int dumpUid5;
        int pkgi;
        long nowUptime3;
        UidState uidState;
        SimpleDateFormat sdf3;
        long now6;
        Ops ops;
        AppOpsService appOpsService3;
        PrintWriter printWriter3;
        int i4;
        SparseIntArray opModes;
        long nowUptime4;
        int mode;
        boolean hasOp;
        boolean hasPackage;
        boolean hasMode;
        boolean hasPackage2;
        boolean hasOp2;
        boolean needSep3;
        SimpleDateFormat sdf4;
        boolean needSep4;
        SparseArray<Restriction> restrictions;
        boolean needSep5;
        boolean needSep6;
        int i5;
        long now7;
        boolean needSep7;
        ActiveCallback cb;
        boolean needSep8;
        boolean needSep9;
        boolean needSep10;
        boolean printedHeader;
        boolean needSep11;
        ArraySet<ModeCallback> callbacks;
        AppOpsService appOpsService4 = this;
        PrintWriter printWriter4 = pw;
        String[] strArr = args;
        if (DumpUtils.checkDumpAndUsageStatsPermission(appOpsService4.mContext, TAG, printWriter4)) {
            String dumpPackage5 = null;
            if (strArr != null) {
                int dumpMode5 = -1;
                dumpUid = -1;
                int dumpOp5 = -1;
                int i6 = 0;
                while (i6 < strArr.length) {
                    String arg = strArr[i6];
                    if ("-h".equals(arg)) {
                        appOpsService4.dumpHelp(printWriter4);
                        return;
                    }
                    if (!"-a".equals(arg)) {
                        if ("--op".equals(arg)) {
                            i6++;
                            if (i6 >= strArr.length) {
                                printWriter4.println("No argument for --op option");
                                return;
                            }
                            dumpOp5 = Shell.strOpToOp(strArr[i6], printWriter4);
                            if (dumpOp5 < 0) {
                                return;
                            }
                        } else if ("--package".equals(arg)) {
                            int i7 = i6 + 1;
                            if (i7 >= strArr.length) {
                                printWriter4.println("No argument for --package option");
                                return;
                            }
                            dumpPackage5 = strArr[i7];
                            try {
                                dumpUid = AppGlobals.getPackageManager().getPackageUid(dumpPackage5, 12591104, 0);
                            } catch (RemoteException e) {
                            }
                            if (dumpUid < 0) {
                                printWriter4.println("Unknown package: " + dumpPackage5);
                                return;
                            }
                            dumpUid = UserHandle.getAppId(dumpUid);
                            i6 = i7;
                        } else if ("--mode".equals(arg)) {
                            i6++;
                            if (i6 >= strArr.length) {
                                printWriter4.println("No argument for --mode option");
                                return;
                            }
                            dumpMode5 = Shell.strModeToMode(strArr[i6], printWriter4);
                            if (dumpMode5 < 0) {
                                return;
                            }
                        } else if (arg.length() <= 0 || arg.charAt(0) != '-') {
                            printWriter4.println("Unknown command: " + arg);
                            return;
                        } else {
                            printWriter4.println("Unknown option: " + arg);
                            return;
                        }
                    }
                    i6++;
                }
                dumpOp = dumpOp5;
                dumpMode = dumpMode5;
                dumpPackage = dumpPackage5;
            } else {
                dumpOp = -1;
                dumpPackage = null;
                dumpMode = -1;
                dumpUid = -1;
            }
            synchronized (this) {
                printWriter4.println("Current AppOps Service state:");
                appOpsService4.mConstants.dump(printWriter4);
                pw.println();
                long now8 = System.currentTimeMillis();
                long nowElapsed = SystemClock.elapsedRealtime();
                long nowUptime5 = SystemClock.uptimeMillis();
                SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date date = new Date();
                boolean needSep12 = false;
                if (dumpOp < 0 && dumpMode < 0 && dumpPackage == null) {
                    if (appOpsService4.mProfileOwners != null) {
                        printWriter4.println("  Profile owners:");
                        for (int poi = 0; poi < appOpsService4.mProfileOwners.size(); poi++) {
                            printWriter4.print("    User #");
                            printWriter4.print(appOpsService4.mProfileOwners.keyAt(poi));
                            printWriter4.print(": ");
                            UserHandle.formatUid(printWriter4, appOpsService4.mProfileOwners.valueAt(poi));
                            pw.println();
                        }
                        pw.println();
                    }
                }
                try {
                    if (appOpsService4.mOpModeWatchers.size() > 0) {
                        boolean printedHeader2 = false;
                        boolean needSep13 = false;
                        int i8 = 0;
                        while (true) {
                            needSep11 = needSep13;
                            if (i8 >= appOpsService4.mOpModeWatchers.size()) {
                                break;
                            }
                            if (dumpOp < 0 || dumpOp == appOpsService4.mOpModeWatchers.keyAt(i8)) {
                                boolean printedOpHeader = false;
                                ArraySet<ModeCallback> callbacks2 = appOpsService4.mOpModeWatchers.valueAt(i8);
                                boolean printedHeader3 = printedHeader2;
                                int j2 = 0;
                                while (j2 < callbacks2.size()) {
                                    ModeCallback cb2 = callbacks2.valueAt(j2);
                                    if (dumpPackage != null) {
                                        callbacks = callbacks2;
                                        if (cb2.mWatchingUid >= 0 && dumpUid != UserHandle.getAppId(cb2.mWatchingUid)) {
                                            j2++;
                                            callbacks2 = callbacks;
                                            String[] strArr2 = args;
                                        }
                                    } else {
                                        callbacks = callbacks2;
                                    }
                                    needSep11 = true;
                                    if (!printedHeader3) {
                                        printWriter4.println("  Op mode watchers:");
                                        printedHeader3 = true;
                                    }
                                    if (!printedOpHeader) {
                                        printWriter4.print("    Op ");
                                        printWriter4.print(AppOpsManager.opToName(appOpsService4.mOpModeWatchers.keyAt(i8)));
                                        printWriter4.println(":");
                                        printedOpHeader = true;
                                    }
                                    printWriter4.print("      #");
                                    printWriter4.print(j2);
                                    printWriter4.print(": ");
                                    printWriter4.println(cb2);
                                    j2++;
                                    callbacks2 = callbacks;
                                    String[] strArr22 = args;
                                }
                                printedHeader2 = printedHeader3;
                            }
                            needSep13 = needSep11;
                            i8++;
                            String[] strArr3 = args;
                        }
                        needSep12 = needSep11;
                    }
                    if (appOpsService4.mPackageModeWatchers.size() <= 0 || dumpOp >= 0) {
                        needSep = needSep12;
                    } else {
                        boolean printedHeader4 = false;
                        needSep = needSep12;
                        int i9 = 0;
                        while (i9 < appOpsService4.mPackageModeWatchers.size()) {
                            try {
                                if (dumpPackage == null || dumpPackage.equals(appOpsService4.mPackageModeWatchers.keyAt(i9))) {
                                    boolean needSep14 = true;
                                    if (!printedHeader4) {
                                        printWriter4.println("  Package mode watchers:");
                                        printedHeader4 = true;
                                    }
                                    printWriter4.print("    Pkg ");
                                    printWriter4.print(appOpsService4.mPackageModeWatchers.keyAt(i9));
                                    printWriter4.println(":");
                                    ArraySet<ModeCallback> callbacks3 = appOpsService4.mPackageModeWatchers.valueAt(i9);
                                    int j3 = 0;
                                    while (true) {
                                        needSep10 = needSep14;
                                        printedHeader = printedHeader4;
                                        int j4 = j3;
                                        if (j4 >= callbacks3.size()) {
                                            break;
                                        }
                                        printWriter4.print("      #");
                                        printWriter4.print(j4);
                                        printWriter4.print(": ");
                                        printWriter4.println(callbacks3.valueAt(j4));
                                        j3 = j4 + 1;
                                        needSep14 = needSep10;
                                        printedHeader4 = printedHeader;
                                    }
                                    needSep = needSep10;
                                    printedHeader4 = printedHeader;
                                }
                                i9++;
                            } catch (Throwable th) {
                                th = th;
                                int i10 = dumpUid;
                                String str = dumpPackage;
                                int i11 = dumpMode;
                                int i12 = dumpOp;
                                PrintWriter printWriter5 = printWriter4;
                                AppOpsService appOpsService5 = appOpsService4;
                                throw th;
                            }
                        }
                    }
                    if (appOpsService4.mModeWatchers.size() > 0 && dumpOp < 0) {
                        boolean printedHeader5 = false;
                        for (int i13 = 0; i13 < appOpsService4.mModeWatchers.size(); i13++) {
                            ModeCallback cb3 = appOpsService4.mModeWatchers.valueAt(i13);
                            if (dumpPackage != null) {
                                boolean needSep15 = needSep;
                                if (cb3.mWatchingUid >= 0 && dumpUid != UserHandle.getAppId(cb3.mWatchingUid)) {
                                    needSep9 = needSep15;
                                }
                            }
                            if (!printedHeader5) {
                                needSep8 = true;
                                printWriter4.println("  All op mode watchers:");
                                printedHeader5 = true;
                            } else {
                                needSep8 = true;
                            }
                            printWriter4.print("    ");
                            printWriter4.print(Integer.toHexString(System.identityHashCode(appOpsService4.mModeWatchers.keyAt(i13))));
                            printWriter4.print(": ");
                            printWriter4.println(cb3);
                            needSep9 = needSep8;
                        }
                        boolean z2 = needSep;
                    }
                    if (appOpsService4.mActiveWatchers.size() <= 0 || dumpMode >= 0) {
                        now = now8;
                        z = true;
                    } else {
                        boolean needSep16 = true;
                        boolean printedHeader6 = false;
                        int i14 = 0;
                        while (i5 < appOpsService4.mActiveWatchers.size()) {
                            SparseArray<ActiveCallback> activeWatchers = appOpsService4.mActiveWatchers.valueAt(i5);
                            if (activeWatchers.size() <= 0) {
                                now7 = now8;
                                needSep7 = needSep;
                            } else {
                                needSep7 = needSep;
                                ActiveCallback cb4 = activeWatchers.valueAt(0);
                                if (dumpOp < 0 || activeWatchers.indexOfKey(dumpOp) >= 0) {
                                    if (dumpPackage != null) {
                                        now7 = now8;
                                        cb = cb4;
                                        if (cb.mWatchingUid >= 0 && dumpUid != UserHandle.getAppId(cb.mWatchingUid)) {
                                        }
                                    } else {
                                        now7 = now8;
                                        cb = cb4;
                                    }
                                    if (!printedHeader6) {
                                        printWriter4.println("  All op active watchers:");
                                        printedHeader6 = true;
                                    }
                                    printWriter4.print("    ");
                                    printWriter4.print(Integer.toHexString(System.identityHashCode(appOpsService4.mActiveWatchers.keyAt(i5))));
                                    printWriter4.println(" ->");
                                    printWriter4.print("        [");
                                    int opCount = activeWatchers.size();
                                    i5 = 0;
                                    while (i5 < opCount) {
                                        if (i5 > 0) {
                                            printWriter4.print(' ');
                                        }
                                        printWriter4.print(AppOpsManager.opToName(activeWatchers.keyAt(i5)));
                                        if (i5 < opCount - 1) {
                                            printWriter4.print(',');
                                        }
                                        i5++;
                                    }
                                    printWriter4.println("]");
                                    printWriter4.print("        ");
                                    printWriter4.println(cb);
                                } else {
                                    now7 = now8;
                                }
                            }
                            i14 = i5 + 1;
                            needSep16 = needSep7;
                            now8 = now7;
                        }
                        now = now8;
                        boolean z3 = needSep;
                        z = true;
                    }
                    if (appOpsService4.mClients.size() <= 0 || dumpMode >= 0) {
                        dumpUid2 = dumpUid;
                    } else {
                        boolean needSep17 = true;
                        boolean printedHeader7 = false;
                        int i15 = 0;
                        while (i15 < appOpsService4.mClients.size()) {
                            try {
                                ClientState cs = appOpsService4.mClients.valueAt(i15);
                                if (cs.mStartedOps.size() > 0) {
                                    boolean printedStarted = false;
                                    boolean printedClient = false;
                                    boolean printedHeader8 = printedHeader7;
                                    int j5 = 0;
                                    while (true) {
                                        dumpUid2 = dumpUid;
                                        try {
                                            if (j5 >= cs.mStartedOps.size()) {
                                                break;
                                            }
                                            Op op = cs.mStartedOps.get(j5);
                                            if (dumpOp >= 0) {
                                                needSep6 = needSep;
                                                if (op.op != dumpOp) {
                                                    j5++;
                                                    dumpUid = dumpUid2;
                                                    needSep = needSep6;
                                                }
                                            } else {
                                                needSep6 = needSep;
                                            }
                                            if (dumpPackage == null || dumpPackage.equals(op.packageName)) {
                                                if (!printedHeader8) {
                                                    printWriter4.println("  Clients:");
                                                    printedHeader8 = true;
                                                }
                                                if (!printedClient) {
                                                    printWriter4.print("    ");
                                                    printWriter4.print(appOpsService4.mClients.keyAt(i15));
                                                    printWriter4.println(":");
                                                    printWriter4.print("      ");
                                                    printWriter4.println(cs);
                                                    printedClient = true;
                                                }
                                                if (!printedStarted) {
                                                    printWriter4.println("      Started ops:");
                                                    printedStarted = true;
                                                }
                                                printWriter4.print("        ");
                                                printWriter4.print("uid=");
                                                printWriter4.print(op.uid);
                                                printWriter4.print(" pkg=");
                                                printWriter4.print(op.packageName);
                                                printWriter4.print(" op=");
                                                printWriter4.println(AppOpsManager.opToName(op.op));
                                                j5++;
                                                dumpUid = dumpUid2;
                                                needSep = needSep6;
                                            } else {
                                                j5++;
                                                dumpUid = dumpUid2;
                                                needSep = needSep6;
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            String str2 = dumpPackage;
                                            int i16 = dumpMode;
                                            int i17 = dumpOp;
                                            PrintWriter printWriter6 = printWriter4;
                                            AppOpsService appOpsService6 = appOpsService4;
                                            int i18 = dumpUid2;
                                            throw th;
                                        }
                                    }
                                    needSep5 = needSep;
                                    printedHeader7 = printedHeader8;
                                } else {
                                    dumpUid2 = dumpUid;
                                    needSep5 = needSep;
                                }
                                i15++;
                                dumpUid = dumpUid2;
                                needSep17 = needSep5;
                            } catch (Throwable th3) {
                                th = th3;
                                int i19 = dumpUid;
                                String str3 = dumpPackage;
                                int i20 = dumpMode;
                                int i21 = dumpOp;
                                PrintWriter printWriter7 = printWriter4;
                                AppOpsService appOpsService7 = appOpsService4;
                                throw th;
                            }
                        }
                        dumpUid2 = dumpUid;
                        boolean z4 = needSep;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    int i22 = dumpUid;
                    String str4 = dumpPackage;
                    int i23 = dumpMode;
                    int i24 = dumpOp;
                    PrintWriter printWriter8 = printWriter4;
                    AppOpsService appOpsService8 = appOpsService4;
                    throw th;
                }
                try {
                    if (appOpsService4.mAudioRestrictions.size() > 0 && dumpOp < 0 && dumpPackage != null && dumpMode < 0) {
                        boolean printedHeader9 = false;
                        int o = 0;
                        while (o < appOpsService4.mAudioRestrictions.size()) {
                            String op2 = AppOpsManager.opToName(appOpsService4.mAudioRestrictions.keyAt(o));
                            SparseArray<Restriction> restrictions2 = appOpsService4.mAudioRestrictions.valueAt(o);
                            boolean printedHeader10 = printedHeader9;
                            int i25 = 0;
                            while (i25 < restrictions2.size()) {
                                if (!printedHeader10) {
                                    printWriter4.println("  Audio Restrictions:");
                                    printedHeader10 = true;
                                    needSep = true;
                                }
                                int usage = restrictions2.keyAt(i25);
                                boolean needSep18 = needSep;
                                printWriter4.print("    ");
                                printWriter4.print(op2);
                                printWriter4.print(" usage=");
                                printWriter4.print(AudioAttributes.usageToString(usage));
                                Restriction r = restrictions2.valueAt(i25);
                                String op3 = op2;
                                printWriter4.print(": mode=");
                                printWriter4.println(AppOpsManager.modeToName(r.mode));
                                if (!r.exceptionPackages.isEmpty()) {
                                    printWriter4.println("      Exceptions:");
                                    int j6 = 0;
                                    while (true) {
                                        restrictions = restrictions2;
                                        if (j6 >= r.exceptionPackages.size()) {
                                            break;
                                        }
                                        printWriter4.print("        ");
                                        printWriter4.println(r.exceptionPackages.valueAt(j6));
                                        j6++;
                                        restrictions2 = restrictions;
                                    }
                                } else {
                                    restrictions = restrictions2;
                                }
                                i25++;
                                needSep = needSep18;
                                op2 = op3;
                                restrictions2 = restrictions;
                            }
                            o++;
                            printedHeader9 = printedHeader10;
                        }
                    }
                    if (needSep) {
                        pw.println();
                    }
                    int i26 = 0;
                    while (true) {
                        int i27 = i26;
                        if (i27 >= appOpsService4.mUidStates.size()) {
                            break;
                        }
                        UidState uidState2 = appOpsService4.mUidStates.valueAt(i27);
                        SparseIntArray opModes2 = uidState2.opModes;
                        ArrayMap<String, Ops> pkgOps2 = uidState2.pkgOps;
                        if (dumpOp >= 0 || dumpPackage != null || dumpMode >= 0) {
                            if (dumpOp >= 0) {
                                if (uidState2.opModes == null || uidState2.opModes.indexOfKey(dumpOp) < 0) {
                                    hasOp = false;
                                    hasPackage = dumpPackage != null ? z : false;
                                    hasMode = dumpMode >= 0 ? z : false;
                                    if (!hasMode || opModes2 == null) {
                                        hasOp2 = hasOp;
                                        hasPackage2 = hasPackage;
                                    } else {
                                        boolean hasMode2 = hasMode;
                                        int opi = 0;
                                        while (true) {
                                            int opi2 = opi;
                                            if (hasMode2) {
                                                hasOp2 = hasOp;
                                                hasPackage2 = hasPackage;
                                                break;
                                            }
                                            hasOp2 = hasOp;
                                            hasPackage2 = hasPackage;
                                            int opi3 = opi2;
                                            if (opi3 >= opModes2.size()) {
                                                break;
                                            }
                                            if (opModes2.valueAt(opi3) == dumpMode) {
                                                hasMode2 = true;
                                            }
                                            opi = opi3 + 1;
                                            hasOp = hasOp2;
                                            hasPackage = hasPackage2;
                                        }
                                        hasMode = hasMode2;
                                    }
                                    if (pkgOps2 == null) {
                                        boolean hasPackage3 = hasPackage2;
                                        int pkgi2 = 0;
                                        while (true) {
                                            if (hasOp2 && hasPackage3) {
                                                if (hasMode) {
                                                    sdf = sdf5;
                                                    needSep3 = needSep;
                                                    i2 = i27;
                                                    break;
                                                }
                                            }
                                            i2 = i27;
                                            if (pkgi2 >= pkgOps2.size()) {
                                                sdf = sdf5;
                                                needSep3 = needSep;
                                                break;
                                            }
                                            Ops ops2 = pkgOps2.valueAt(pkgi2);
                                            if (!hasOp2 && ops2 != null && ops2.indexOfKey(dumpOp) >= 0) {
                                                hasOp2 = true;
                                            }
                                            if (!hasMode) {
                                                boolean hasMode3 = hasMode;
                                                int opi4 = 0;
                                                while (true) {
                                                    int opi5 = opi4;
                                                    if (hasMode3) {
                                                        sdf4 = sdf5;
                                                        needSep4 = needSep;
                                                        break;
                                                    }
                                                    needSep4 = needSep;
                                                    sdf4 = sdf5;
                                                    int opi6 = opi5;
                                                    if (opi6 >= ops2.size()) {
                                                        break;
                                                    }
                                                    if (((Op) ops2.valueAt(opi6)).mode == dumpMode) {
                                                        hasMode3 = true;
                                                    }
                                                    opi4 = opi6 + 1;
                                                    needSep = needSep4;
                                                    sdf5 = sdf4;
                                                }
                                                hasMode = hasMode3;
                                            } else {
                                                sdf4 = sdf5;
                                                needSep4 = needSep;
                                            }
                                            if (!hasPackage3 && dumpPackage.equals(ops2.packageName)) {
                                                hasPackage3 = true;
                                            }
                                            pkgi2++;
                                            i27 = i2;
                                            needSep = needSep4;
                                            sdf5 = sdf4;
                                        }
                                        hasPackage2 = hasPackage3;
                                    } else {
                                        sdf = sdf5;
                                        needSep3 = needSep;
                                        i2 = i27;
                                    }
                                    if (uidState2.foregroundOps != null && !hasOp2) {
                                        if (uidState2.foregroundOps.indexOfKey(dumpOp) > 0) {
                                            hasOp2 = true;
                                        }
                                    }
                                    if (hasOp2 || !hasPackage2) {
                                        nowUptime = nowUptime5;
                                        dumpPackage2 = dumpPackage;
                                        dumpMode2 = dumpMode;
                                        dumpOp2 = dumpOp;
                                        printWriter = printWriter4;
                                        appOpsService = appOpsService4;
                                        now4 = now;
                                        dumpUid3 = dumpUid2;
                                        i = i2;
                                        sdf5 = sdf;
                                    } else if (!hasMode) {
                                        nowUptime = nowUptime5;
                                        dumpPackage2 = dumpPackage;
                                        dumpMode2 = dumpMode;
                                        dumpOp2 = dumpOp;
                                        printWriter = printWriter4;
                                        appOpsService = appOpsService4;
                                        now4 = now;
                                        dumpUid3 = dumpUid2;
                                        i = i2;
                                        sdf5 = sdf;
                                    }
                                    needSep2 = needSep3;
                                    printWriter4 = printWriter;
                                    appOpsService4 = appOpsService;
                                    now = now4;
                                    dumpUid2 = dumpUid3;
                                    dumpPackage = dumpPackage2;
                                    dumpMode = dumpMode2;
                                    dumpOp = dumpOp2;
                                    z = true;
                                    i26 = i + 1;
                                    nowUptime5 = nowUptime;
                                }
                            }
                            hasOp = z;
                            if (dumpPackage != null) {
                            }
                            if (dumpMode >= 0) {
                            }
                            if (!hasMode) {
                            }
                            hasOp2 = hasOp;
                            hasPackage2 = hasPackage;
                            if (pkgOps2 == null) {
                            }
                            if (uidState2.foregroundOps.indexOfKey(dumpOp) > 0) {
                            }
                            if (hasOp2) {
                            }
                            nowUptime = nowUptime5;
                            dumpPackage2 = dumpPackage;
                            dumpMode2 = dumpMode;
                            dumpOp2 = dumpOp;
                            printWriter = printWriter4;
                            appOpsService = appOpsService4;
                            now4 = now;
                            dumpUid3 = dumpUid2;
                            i = i2;
                            sdf5 = sdf;
                            needSep2 = needSep3;
                            printWriter4 = printWriter;
                            appOpsService4 = appOpsService;
                            now = now4;
                            dumpUid2 = dumpUid3;
                            dumpPackage = dumpPackage2;
                            dumpMode = dumpMode2;
                            dumpOp = dumpOp2;
                            z = true;
                            i26 = i + 1;
                            nowUptime5 = nowUptime;
                        } else {
                            sdf = sdf5;
                            boolean z5 = needSep;
                            i2 = i27;
                        }
                        printWriter4.print("  Uid ");
                        UserHandle.formatUid(printWriter4, uidState2.uid);
                        printWriter4.println(":");
                        printWriter4.print("    state=");
                        printWriter4.println(UID_STATE_NAMES[uidState2.state]);
                        if (uidState2.state != uidState2.pendingState) {
                            printWriter4.print("    pendingState=");
                            printWriter4.println(UID_STATE_NAMES[uidState2.pendingState]);
                        }
                        if (uidState2.pendingStateCommitTime != 0) {
                            printWriter4.print("    pendingStateCommitTime=");
                            TimeUtils.formatDuration(uidState2.pendingStateCommitTime, nowUptime5, printWriter4);
                            pw.println();
                        }
                        if (uidState2.startNesting != 0) {
                            printWriter4.print("    startNesting=");
                            printWriter4.println(uidState2.startNesting);
                        }
                        if (uidState2.foregroundOps != null && (dumpMode < 0 || dumpMode == 4)) {
                            printWriter4.println("    foregroundOps:");
                            for (int j7 = 0; j7 < uidState2.foregroundOps.size(); j7++) {
                                if (dumpOp < 0 || dumpOp == uidState2.foregroundOps.keyAt(j7)) {
                                    printWriter4.print("      ");
                                    printWriter4.print(AppOpsManager.opToName(uidState2.foregroundOps.keyAt(j7)));
                                    printWriter4.print(": ");
                                    printWriter4.println(uidState2.foregroundOps.valueAt(j7) ? "WATCHER" : "SILENT");
                                }
                            }
                            printWriter4.print("    hasForegroundWatchers=");
                            printWriter4.println(uidState2.hasForegroundWatchers);
                        }
                        if (opModes2 != null) {
                            int opModeCount = opModes2.size();
                            for (int j8 = 0; j8 < opModeCount; j8++) {
                                int code = opModes2.keyAt(j8);
                                int mode2 = opModes2.valueAt(j8);
                                if (dumpOp < 0 || dumpOp == code) {
                                    if (dumpMode < 0 || dumpMode == mode2) {
                                        printWriter4.print("      ");
                                        printWriter4.print(AppOpsManager.opToName(code));
                                        printWriter4.print(": mode=");
                                        printWriter4.println(AppOpsManager.modeToName(mode2));
                                    }
                                }
                            }
                        }
                        if (pkgOps2 == null) {
                            nowUptime2 = nowUptime5;
                            dumpPackage3 = dumpPackage;
                            dumpMode3 = dumpMode;
                            dumpOp3 = dumpOp;
                            printWriter2 = printWriter4;
                            appOpsService2 = appOpsService4;
                            now5 = now;
                            dumpUid4 = dumpUid2;
                            i3 = i2;
                            sdf2 = sdf;
                        } else {
                            int pkgi3 = 0;
                            while (true) {
                                int pkgi4 = pkgi3;
                                if (pkgi4 >= pkgOps2.size()) {
                                    break;
                                }
                                Ops ops3 = pkgOps2.valueAt(pkgi4);
                                if (dumpPackage != null) {
                                    if (!dumpPackage.equals(ops3.packageName)) {
                                        i2 = i2;
                                        printWriter4 = printWriter4;
                                        appOpsService4 = appOpsService4;
                                        pkgi3 = pkgi4 + 1;
                                        now = now;
                                        sdf = sdf;
                                        uidState2 = uidState2;
                                        dumpUid2 = dumpUid2;
                                        dumpPackage = dumpPackage;
                                        dumpMode = dumpMode;
                                        pkgOps2 = pkgOps2;
                                        dumpOp = dumpOp;
                                        z = true;
                                        opModes2 = opModes2;
                                        nowUptime5 = nowUptime5;
                                    }
                                }
                                boolean printedPackage2 = false;
                                int j9 = 0;
                                while (true) {
                                    int j10 = j9;
                                    if (j10 >= ops3.size()) {
                                        break;
                                    }
                                    Op op4 = (Op) ops3.valueAt(j10);
                                    if (dumpOp >= 0) {
                                        nowUptime4 = nowUptime5;
                                        if (dumpOp != op4.op) {
                                            printedPackage = printedPackage2;
                                            dumpPackage4 = dumpPackage;
                                            dumpMode4 = dumpMode;
                                            opModes = opModes2;
                                            pkgOps = pkgOps2;
                                            j = j10;
                                            dumpOp4 = dumpOp;
                                            ops = ops3;
                                            uidState = uidState2;
                                            pkgi = pkgi4;
                                            printWriter3 = printWriter4;
                                            appOpsService3 = appOpsService4;
                                            now6 = now;
                                            dumpUid5 = dumpUid2;
                                            i4 = i2;
                                            sdf3 = sdf;
                                            nowUptime3 = nowUptime4;
                                            i2 = i4;
                                            printWriter4 = printWriter3;
                                            appOpsService4 = appOpsService3;
                                            j9 = j + 1;
                                            ops3 = ops;
                                            now = now6;
                                            sdf = sdf3;
                                            uidState2 = uidState;
                                            pkgi4 = pkgi;
                                            dumpUid2 = dumpUid5;
                                            dumpPackage = dumpPackage4;
                                            dumpMode = dumpMode4;
                                            pkgOps2 = pkgOps;
                                            printedPackage2 = printedPackage;
                                            dumpOp = dumpOp4;
                                            z = true;
                                            opModes2 = opModes;
                                            nowUptime5 = nowUptime3;
                                        }
                                    } else {
                                        nowUptime4 = nowUptime5;
                                    }
                                    if (dumpMode < 0 || dumpMode == op4.mode) {
                                        if (!printedPackage2) {
                                            printWriter4.print("    Package ");
                                            printWriter4.print(ops3.packageName);
                                            printWriter4.println(":");
                                            printedPackage2 = true;
                                        }
                                        boolean printedPackage3 = printedPackage2;
                                        printWriter4.print("      ");
                                        printWriter4.print(AppOpsManager.opToName(op4.op));
                                        printWriter4.print(" (");
                                        printWriter4.print(AppOpsManager.modeToName(op4.mode));
                                        int switchOp = AppOpsManager.opToSwitch(op4.op);
                                        if (switchOp != op4.op) {
                                            printWriter4.print(" / switch ");
                                            printWriter4.print(AppOpsManager.opToName(switchOp));
                                            Op switchObj = (Op) ops3.get(switchOp);
                                            if (switchObj != null) {
                                                printedPackage = printedPackage3;
                                                mode = switchObj.mode;
                                            } else {
                                                printedPackage = printedPackage3;
                                                mode = AppOpsManager.opToDefaultMode(switchOp);
                                            }
                                            Op op5 = switchObj;
                                            printWriter4.print("=");
                                            printWriter4.print(AppOpsManager.modeToName(mode));
                                        } else {
                                            printedPackage = printedPackage3;
                                        }
                                        printWriter4.println("): ");
                                        nowUptime3 = nowUptime4;
                                        int pkgi5 = pkgi4;
                                        Op op6 = op4;
                                        int i28 = switchOp;
                                        dumpUid5 = dumpUid2;
                                        dumpPackage4 = dumpPackage;
                                        dumpMode4 = dumpMode;
                                        opModes = opModes2;
                                        pkgOps = pkgOps2;
                                        j = j10;
                                        now6 = now;
                                        sdf3 = sdf;
                                        try {
                                            appOpsService4.dumpTimesLocked(printWriter4, "          Access: ", "                  ", op4.time, now6, sdf3, date);
                                            dumpOp4 = dumpOp;
                                            ops = ops3;
                                            int dumpOp6 = z;
                                            uidState = uidState2;
                                            Op op7 = op6;
                                            i4 = i2;
                                            pkgi = pkgi5;
                                            printWriter3 = printWriter4;
                                            appOpsService3 = appOpsService4;
                                            appOpsService4.dumpTimesLocked(printWriter4, "          Reject: ", "                  ", op6.rejectTime, now6, sdf3, date);
                                            if (op7.duration == -1) {
                                                printWriter3.print("          Running start at: ");
                                                TimeUtils.formatDuration(nowElapsed - op7.startRealtime, printWriter3);
                                                pw.println();
                                            } else if (op7.duration != 0) {
                                                printWriter3.print("          duration=");
                                                TimeUtils.formatDuration((long) op7.duration, printWriter3);
                                                pw.println();
                                            }
                                            if (op7.startNesting != 0) {
                                                printWriter3.print("          startNesting=");
                                                printWriter3.println(op7.startNesting);
                                            }
                                            i2 = i4;
                                            printWriter4 = printWriter3;
                                            appOpsService4 = appOpsService3;
                                            j9 = j + 1;
                                            ops3 = ops;
                                            now = now6;
                                            sdf = sdf3;
                                            uidState2 = uidState;
                                            pkgi4 = pkgi;
                                            dumpUid2 = dumpUid5;
                                            dumpPackage = dumpPackage4;
                                            dumpMode = dumpMode4;
                                            pkgOps2 = pkgOps;
                                            printedPackage2 = printedPackage;
                                            dumpOp = dumpOp4;
                                            z = true;
                                            opModes2 = opModes;
                                            nowUptime5 = nowUptime3;
                                        } catch (Throwable th5) {
                                            th = th5;
                                            throw th;
                                        }
                                    }
                                    printedPackage = printedPackage2;
                                    dumpPackage4 = dumpPackage;
                                    dumpMode4 = dumpMode;
                                    opModes = opModes2;
                                    pkgOps = pkgOps2;
                                    j = j10;
                                    dumpOp4 = dumpOp;
                                    ops = ops3;
                                    uidState = uidState2;
                                    pkgi = pkgi4;
                                    printWriter3 = printWriter4;
                                    appOpsService3 = appOpsService4;
                                    now6 = now;
                                    dumpUid5 = dumpUid2;
                                    i4 = i2;
                                    sdf3 = sdf;
                                    nowUptime3 = nowUptime4;
                                    i2 = i4;
                                    printWriter4 = printWriter3;
                                    appOpsService4 = appOpsService3;
                                    j9 = j + 1;
                                    ops3 = ops;
                                    now = now6;
                                    sdf = sdf3;
                                    uidState2 = uidState;
                                    pkgi4 = pkgi;
                                    dumpUid2 = dumpUid5;
                                    dumpPackage = dumpPackage4;
                                    dumpMode = dumpMode4;
                                    pkgOps2 = pkgOps;
                                    printedPackage2 = printedPackage;
                                    dumpOp = dumpOp4;
                                    z = true;
                                    opModes2 = opModes;
                                    nowUptime5 = nowUptime3;
                                }
                                i2 = i2;
                                printWriter4 = printWriter4;
                                appOpsService4 = appOpsService4;
                                pkgi3 = pkgi4 + 1;
                                now = now;
                                sdf = sdf;
                                uidState2 = uidState2;
                                dumpUid2 = dumpUid2;
                                dumpPackage = dumpPackage;
                                dumpMode = dumpMode;
                                pkgOps2 = pkgOps2;
                                dumpOp = dumpOp;
                                z = true;
                                opModes2 = opModes2;
                                nowUptime5 = nowUptime5;
                            }
                            nowUptime2 = nowUptime5;
                            dumpPackage3 = dumpPackage;
                            dumpMode3 = dumpMode;
                            dumpOp3 = dumpOp;
                            printWriter2 = printWriter4;
                            appOpsService2 = appOpsService4;
                            now5 = now;
                            dumpUid4 = dumpUid2;
                            i3 = i2;
                            sdf2 = sdf;
                        }
                        needSep2 = true;
                        printWriter4 = printWriter;
                        appOpsService4 = appOpsService;
                        now = now4;
                        dumpUid2 = dumpUid3;
                        dumpPackage = dumpPackage2;
                        dumpMode = dumpMode2;
                        dumpOp = dumpOp2;
                        z = true;
                        i26 = i + 1;
                        nowUptime5 = nowUptime;
                    }
                    String str5 = dumpPackage;
                    int i29 = dumpMode;
                    int i30 = dumpOp;
                    PrintWriter printWriter9 = printWriter4;
                    AppOpsService appOpsService9 = appOpsService4;
                    long now9 = now;
                    int i31 = dumpUid2;
                    if (needSep) {
                        pw.println();
                    }
                    int k = appOpsService9.mOpUserRestrictions.size();
                    int i32 = 0;
                    while (i32 < k) {
                        IBinder token3 = appOpsService9.mOpUserRestrictions.keyAt(i32);
                        ClientRestrictionState restrictionState = appOpsService9.mOpUserRestrictions.valueAt(i32);
                        printWriter9.println("  User restrictions for token " + token3 + ":");
                        int restrictionCount = restrictionState.perUserRestrictions != null ? restrictionState.perUserRestrictions.size() : 0;
                        if (restrictionCount > 0) {
                            printWriter9.println("      Restricted ops:");
                            int j11 = 0;
                            while (j11 < restrictionCount) {
                                int userId = restrictionState.perUserRestrictions.keyAt(j11);
                                boolean[] restrictedOps = restrictionState.perUserRestrictions.valueAt(j11);
                                if (restrictedOps == null) {
                                    userRestrictionCount = k;
                                    token = token3;
                                    now2 = now9;
                                } else {
                                    StringBuilder restrictedOpsValue = new StringBuilder();
                                    restrictedOpsValue.append("[");
                                    int restrictedOpCount = restrictedOps.length;
                                    int k2 = 0;
                                    while (true) {
                                        userRestrictionCount = k;
                                        int userRestrictionCount2 = k2;
                                        if (userRestrictionCount2 >= restrictedOpCount) {
                                            break;
                                        }
                                        if (restrictedOps[userRestrictionCount2]) {
                                            token2 = token3;
                                            now3 = now9;
                                            if (restrictedOpsValue.length() > 1) {
                                                restrictedOpsValue.append(", ");
                                            }
                                            restrictedOpsValue.append(AppOpsManager.opToName(userRestrictionCount2));
                                        } else {
                                            token2 = token3;
                                            now3 = now9;
                                        }
                                        k2 = userRestrictionCount2 + 1;
                                        k = userRestrictionCount;
                                        token3 = token2;
                                        now9 = now3;
                                    }
                                    token = token3;
                                    now2 = now9;
                                    restrictedOpsValue.append("]");
                                    printWriter9.print("        ");
                                    printWriter9.print("user: ");
                                    printWriter9.print(userId);
                                    printWriter9.print(" restricted ops: ");
                                    printWriter9.println(restrictedOpsValue);
                                }
                                j11++;
                                k = userRestrictionCount;
                                token3 = token;
                                now9 = now2;
                            }
                        }
                        int userRestrictionCount3 = k;
                        IBinder iBinder = token3;
                        long now10 = now9;
                        int excludedPackageCount = restrictionState.perUserExcludedPackages != null ? restrictionState.perUserExcludedPackages.size() : 0;
                        if (excludedPackageCount > 0) {
                            printWriter9.println("      Excluded packages:");
                            for (int j12 = 0; j12 < excludedPackageCount; j12++) {
                                int userId2 = restrictionState.perUserExcludedPackages.keyAt(j12);
                                printWriter9.print("        ");
                                printWriter9.print("user: ");
                                printWriter9.print(userId2);
                                printWriter9.print(" packages: ");
                                printWriter9.println(Arrays.toString(restrictionState.perUserExcludedPackages.valueAt(j12)));
                            }
                        }
                        i32++;
                        k = userRestrictionCount3;
                        now9 = now10;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    String str6 = dumpPackage;
                    int i33 = dumpMode;
                    int i34 = dumpOp;
                    PrintWriter printWriter10 = printWriter4;
                    AppOpsService appOpsService10 = appOpsService4;
                    int i35 = dumpUid2;
                    throw th;
                }
            }
        }
    }

    public void setUserRestrictions(Bundle restrictions, IBinder token, int userHandle) {
        checkSystemUid("setUserRestrictions");
        Preconditions.checkNotNull(restrictions);
        Preconditions.checkNotNull(token);
        for (int i = 0; i < 78; i++) {
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
                this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$AppOpsService$UKMH8n9xZqCOX59uFPylskhjBgo.INSTANCE, this, Integer.valueOf(code), -2));
            }
            if (restrictionState.isDefault()) {
                this.mOpUserRestrictions.remove(token);
                restrictionState.destroy();
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyWatchersOfChange(int code, int uid) {
        synchronized (this) {
            ArraySet<ModeCallback> callbacks = this.mOpModeWatchers.get(code);
            if (callbacks != null) {
                ArraySet arraySet = new ArraySet(callbacks);
                notifyOpChanged((ArraySet<ModeCallback>) arraySet, code, uid, (String) null);
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
        if (Binder.getCallingUid() != uid && this.mContext.checkCallingOrSelfPermission("android.permission.WATCH_APPOPS") != 0) {
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
                    if (op.op == code && op.uid == uid) {
                        return true;
                    }
                }
            }
            return false;
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

    /* access modifiers changed from: protected */
    public void scheduleWriteLockedHook(int code) {
    }

    private static String resolvePackageName(int uid, String packageName) {
        if (uid == 0) {
            return "root";
        }
        if (uid == 2000) {
            return "com.android.shell";
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
    /* JADX WARNING: Can't fix incorrect switch cases order */
    public static int resolveUid(String packageName) {
        char c;
        if (packageName == null) {
            return -1;
        }
        switch (packageName.hashCode()) {
            case -31178072:
                if (packageName.equals("cameraserver")) {
                    c = 4;
                    break;
                }
            case 3506402:
                if (packageName.equals("root")) {
                    c = 0;
                    break;
                }
            case 103772132:
                if (packageName.equals("media")) {
                    c = 2;
                    break;
                }
            case 109403696:
                if (packageName.equals("shell")) {
                    c = 1;
                    break;
                }
            case 1344606873:
                if (packageName.equals("audioserver")) {
                    c = 3;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 0;
            case 1:
                return IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME;
            case 2:
                return 1013;
            case 3:
                return 1041;
            case 4:
                return 1047;
            default:
                return -1;
        }
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
