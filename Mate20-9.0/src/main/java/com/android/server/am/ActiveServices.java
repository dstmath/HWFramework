package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ServiceStartArgs;
import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.webkit.WebViewZygote;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.server.AppStateTracker;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ServiceRecord;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pg.PGManagerInternal;
import com.android.server.pm.DumpState;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.usage.AppStandbyController;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ActiveServices {
    static final int CHECK_INTERVAL = ((int) (10000.0f * ActivityManagerService.SCALE_ANR));
    /* access modifiers changed from: private */
    public static final boolean DEBUG_DELAYED_SERVICE = ActivityManagerDebugConfig.DEBUG_SERVICE;
    /* access modifiers changed from: private */
    public static final boolean DEBUG_DELAYED_STARTS = DEBUG_DELAYED_SERVICE;
    private static final String HW_PARENT_CONTROL = "com.huawei.parentcontrol";
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    private static final boolean LOG_SERVICE_START_STOP = true;
    static final int SERVICE_BACKGROUND_TIMEOUT = (SERVICE_TIMEOUT * 10);
    private static final long SERVICE_CONNECTIONS_THRESHOLD = 100;
    static final int SERVICE_START_FOREGROUND_TIMEOUT = 10000;
    static final int SERVICE_TIMEOUT = ((int) (20000.0f * ActivityManagerService.SCALE_ANR));
    static final int SERVICE_WAIT_PRECHECK = 3000;
    private static final boolean SHOW_DUNGEON_NOTIFICATION = false;
    private static final String TAG = "ActivityManager";
    private static final String TAG_MU = "ActivityManager_MU";
    private static final String TAG_SERVICE = "ActivityManager";
    private static final String TAG_SERVICE_EXECUTING = "ActivityManager";
    private static final long TOOK_THRESHOLD_MS = 1000;
    final ActivityManagerService mAm;
    final ArrayList<ServiceRecord> mDestroyingServices = new ArrayList<>();
    String mLastAnrDump;
    final Runnable mLastAnrDumpClearer;
    final int mMaxStartingBackground;
    final ArrayList<ServiceRecord> mPendingServices = new ArrayList<>();
    final ArrayList<ServiceRecord> mRestartingServices = new ArrayList<>();
    boolean mScreenOn;
    final ArrayMap<IBinder, ArrayList<ConnectionRecord>> mServiceConnections = new ArrayMap<>();
    final SparseArray<ServiceMap> mServiceMap = new SparseArray<>();
    private ArrayList<ServiceRecord> mTmpCollectionResults = null;

    static final class ActiveForegroundApp {
        boolean mAppOnTop;
        long mEndTime;
        long mHideTime;
        CharSequence mLabel;
        int mNumActive;
        String mPackageName;
        boolean mShownWhileScreenOn;
        boolean mShownWhileTop;
        long mStartTime;
        long mStartVisibleTime;
        int mUid;

        ActiveForegroundApp() {
        }
    }

    class ForcedStandbyListener extends AppStateTracker.Listener {
        ForcedStandbyListener() {
        }

        public void stopForegroundServicesForUidPackage(int uid, String packageName) {
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ServiceMap smap = ActiveServices.this.getServiceMapLocked(UserHandle.getUserId(uid));
                    int N = smap.mServicesByName.size();
                    ArrayList<ServiceRecord> toStop = new ArrayList<>(N);
                    for (int i = 0; i < N; i++) {
                        ServiceRecord r = smap.mServicesByName.valueAt(i);
                        if ((uid == r.serviceInfo.applicationInfo.uid || packageName.equals(r.serviceInfo.packageName)) && r.isForeground) {
                            toStop.add(r);
                        }
                    }
                    int i2 = toStop.size();
                    if (i2 > 0 && ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                        Slog.i(ActivityManagerService.TAG, "Package " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + uid + " entering FAS with foreground services");
                    }
                    for (int i3 = 0; i3 < i2; i3++) {
                        ServiceRecord r2 = toStop.get(i3);
                        if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                            Slog.i(ActivityManagerService.TAG, "  Stopping fg for service " + r2);
                        }
                        ActiveServices.this.setServiceForegroundInnerLocked(r2, 0, null, 0);
                    }
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }
    }

    final class ServiceDumper {
        private final String[] args;
        private final boolean dumpAll;
        private final String dumpPackage;
        private final FileDescriptor fd;
        private final ActivityManagerService.ItemMatcher matcher;
        private boolean needSep;
        private final long nowReal = SystemClock.elapsedRealtime();
        private boolean printed;
        private boolean printedAnything;
        private final PrintWriter pw;
        private final ArrayList<ServiceRecord> services = new ArrayList<>();
        final /* synthetic */ ActiveServices this$0;

        ServiceDumper(ActiveServices this$02, FileDescriptor fd2, PrintWriter pw2, String[] args2, int opti, boolean dumpAll2, String dumpPackage2) {
            ActiveServices activeServices = this$02;
            String[] strArr = args2;
            String str = dumpPackage2;
            this.this$0 = activeServices;
            int i = 0;
            this.needSep = false;
            this.printedAnything = false;
            this.printed = false;
            this.fd = fd2;
            this.pw = pw2;
            this.args = strArr;
            this.dumpAll = dumpAll2;
            this.dumpPackage = str;
            this.matcher = new ActivityManagerService.ItemMatcher();
            this.matcher.build(strArr, opti);
            int[] users = activeServices.mAm.mUserController.getUsers();
            int length = users.length;
            int i2 = 0;
            while (i2 < length) {
                ServiceMap smap = activeServices.getServiceMapLocked(users[i2]);
                if (smap.mServicesByName.size() > 0) {
                    int si = i;
                    while (si < smap.mServicesByName.size()) {
                        ServiceRecord r = smap.mServicesByName.valueAt(si);
                        if (this.matcher.match(r, r.name) && (str == null || str.equals(r.appInfo.packageName))) {
                            this.services.add(r);
                        }
                        si++;
                        ActiveServices activeServices2 = this$02;
                    }
                }
                i2++;
                activeServices = this$02;
                i = 0;
            }
        }

        private void dumpHeaderLocked() {
            this.pw.println("ACTIVITY MANAGER SERVICES (dumpsys activity services)");
            if (this.this$0.mLastAnrDump != null) {
                this.pw.println("  Last ANR service:");
                this.pw.print(this.this$0.mLastAnrDump);
                this.pw.println();
            }
        }

        /* access modifiers changed from: package-private */
        public void dumpLocked() {
            dumpHeaderLocked();
            try {
                int[] users = this.this$0.mAm.mUserController.getUsers();
                int length = users.length;
                for (int i = 0; i < length; i++) {
                    int user = users[i];
                    int serviceIdx = 0;
                    while (serviceIdx < this.services.size() && this.services.get(serviceIdx).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = false;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = false;
                        while (true) {
                            if (serviceIdx >= this.services.size()) {
                                break;
                            }
                            ServiceRecord r = this.services.get(serviceIdx);
                            serviceIdx++;
                            if (r.userId != user) {
                                break;
                            }
                            dumpServiceLocalLocked(r);
                        }
                        this.needSep |= this.printed;
                    }
                    dumpUserRemainsLocked(user);
                }
            } catch (Exception e) {
                Slog.w(ActivityManagerService.TAG, "Exception in dumpServicesLocked", e);
            }
            dumpRemainsLocked();
        }

        /* access modifiers changed from: package-private */
        public void dumpWithClient() {
            synchronized (this.this$0.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    dumpHeaderLocked();
                } finally {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
            try {
                int[] users = this.this$0.mAm.mUserController.getUsers();
                int length = users.length;
                for (int i = 0; i < length; i++) {
                    int user = users[i];
                    int serviceIdx = 0;
                    while (serviceIdx < this.services.size() && this.services.get(serviceIdx).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = false;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = false;
                        while (true) {
                            if (serviceIdx >= this.services.size()) {
                                break;
                            }
                            ServiceRecord r = this.services.get(serviceIdx);
                            serviceIdx++;
                            if (r.userId != user) {
                                break;
                            }
                            synchronized (this.this$0.mAm) {
                                ActivityManagerService.boostPriorityForLockedSection();
                                dumpServiceLocalLocked(r);
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            dumpServiceClient(r);
                        }
                        this.needSep |= this.printed;
                    }
                    synchronized (this.this$0.mAm) {
                        ActivityManagerService.boostPriorityForLockedSection();
                        dumpUserRemainsLocked(user);
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            } catch (Exception e) {
                Slog.w(ActivityManagerService.TAG, "Exception in dumpServicesLocked", e);
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            synchronized (this.this$0.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    dumpRemainsLocked();
                } catch (Throwable th2) {
                    while (true) {
                        throw th2;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        private void dumpUserHeaderLocked(int user) {
            if (!this.printed) {
                if (this.printedAnything) {
                    this.pw.println();
                }
                PrintWriter printWriter = this.pw;
                printWriter.println("  User " + user + " active services:");
                this.printed = true;
            }
            this.printedAnything = true;
            if (this.needSep) {
                this.pw.println();
            }
        }

        private void dumpServiceLocalLocked(ServiceRecord r) {
            dumpUserHeaderLocked(r.userId);
            this.pw.print("  * ");
            this.pw.println(r);
            if (this.dumpAll) {
                r.dump(this.pw, "    ");
                this.needSep = true;
                return;
            }
            this.pw.print("    app=");
            this.pw.println(r.app);
            this.pw.print("    created=");
            TimeUtils.formatDuration(r.createRealTime, this.nowReal, this.pw);
            this.pw.print(" started=");
            this.pw.print(r.startRequested);
            this.pw.print(" connections=");
            this.pw.println(r.connections.size());
            if (r.connections.size() > 0) {
                this.pw.println("    Connections:");
                for (int conni = 0; conni < r.connections.size(); conni++) {
                    ArrayList<ConnectionRecord> clist = r.connections.valueAt(conni);
                    for (int i = 0; i < clist.size(); i++) {
                        ConnectionRecord conn = clist.get(i);
                        this.pw.print("      ");
                        this.pw.print(conn.binding.intent.intent.getIntent().toShortString(false, false, false, false));
                        this.pw.print(" -> ");
                        ProcessRecord proc = conn.binding.client;
                        this.pw.println(proc != null ? proc.toShortString() : "null");
                    }
                }
            }
        }

        private void dumpServiceClient(ServiceRecord r) {
            TransferPipe tp;
            ProcessRecord proc = r.app;
            if (proc != null) {
                IApplicationThread thread = proc.thread;
                if (thread != null) {
                    this.pw.println("    Client:");
                    this.pw.flush();
                    try {
                        tp = new TransferPipe();
                        thread.dumpService(tp.getWriteFd(), r, this.args);
                        tp.setBufferPrefix("      ");
                        tp.go(this.fd, 2000);
                        tp.kill();
                    } catch (IOException e) {
                        PrintWriter printWriter = this.pw;
                        printWriter.println("      Failure while dumping the service: " + e);
                    } catch (RemoteException e2) {
                        this.pw.println("      Got a RemoteException while dumping the service");
                    } catch (Throwable th) {
                        tp.kill();
                        throw th;
                    }
                    this.needSep = true;
                }
            }
        }

        private void dumpUserRemainsLocked(int user) {
            ServiceMap smap = this.this$0.getServiceMapLocked(user);
            this.printed = false;
            int SN = smap.mDelayedStartList.size();
            for (int si = 0; si < SN; si++) {
                ServiceRecord r = smap.mDelayedStartList.get(si);
                if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        PrintWriter printWriter = this.pw;
                        printWriter.println("  User " + user + " delayed start services:");
                        this.printed = true;
                    }
                    this.printedAnything = true;
                    this.pw.print("  * Delayed start ");
                    this.pw.println(r);
                }
            }
            this.printed = false;
            int SN2 = smap.mStartingBackground.size();
            for (int si2 = 0; si2 < SN2; si2++) {
                ServiceRecord r2 = smap.mStartingBackground.get(si2);
                if (this.matcher.match(r2, r2.name) && (this.dumpPackage == null || this.dumpPackage.equals(r2.appInfo.packageName))) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        PrintWriter printWriter2 = this.pw;
                        printWriter2.println("  User " + user + " starting in background:");
                        this.printed = true;
                    }
                    this.printedAnything = true;
                    this.pw.print("  * Starting bg ");
                    this.pw.println(r2);
                }
            }
        }

        private void dumpRemainsLocked() {
            if (this.this$0.mPendingServices.size() > 0) {
                this.printed = false;
                for (int i = 0; i < this.this$0.mPendingServices.size(); i++) {
                    ServiceRecord r = this.this$0.mPendingServices.get(i);
                    if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Pending services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Pending ");
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.this$0.mRestartingServices.size() > 0) {
                this.printed = false;
                for (int i2 = 0; i2 < this.this$0.mRestartingServices.size(); i2++) {
                    ServiceRecord r2 = this.this$0.mRestartingServices.get(i2);
                    if (this.matcher.match(r2, r2.name) && (this.dumpPackage == null || this.dumpPackage.equals(r2.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Restarting services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Restarting ");
                        this.pw.println(r2);
                        r2.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.this$0.mDestroyingServices.size() > 0) {
                this.printed = false;
                for (int i3 = 0; i3 < this.this$0.mDestroyingServices.size(); i3++) {
                    ServiceRecord r3 = this.this$0.mDestroyingServices.get(i3);
                    if (this.matcher.match(r3, r3.name) && (this.dumpPackage == null || this.dumpPackage.equals(r3.appInfo.packageName))) {
                        this.printedAnything = true;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = true;
                            this.pw.println("  Destroying services:");
                            this.printed = true;
                        }
                        this.pw.print("  * Destroy ");
                        this.pw.println(r3);
                        r3.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.dumpAll) {
                this.printed = false;
                for (int ic = 0; ic < this.this$0.mServiceConnections.size(); ic++) {
                    ArrayList<ConnectionRecord> r4 = this.this$0.mServiceConnections.valueAt(ic);
                    for (int i4 = 0; i4 < r4.size(); i4++) {
                        ConnectionRecord cr = r4.get(i4);
                        if (this.matcher.match(cr.binding.service, cr.binding.service.name) && (this.dumpPackage == null || (cr.binding.client != null && this.dumpPackage.equals(cr.binding.client.info.packageName)))) {
                            this.printedAnything = true;
                            if (!this.printed) {
                                if (this.needSep) {
                                    this.pw.println();
                                }
                                this.needSep = true;
                                this.pw.println("  Connection bindings to services:");
                                this.printed = true;
                            }
                            this.pw.print("  * ");
                            this.pw.println(cr);
                            cr.dump(this.pw, "    ");
                        }
                    }
                }
            }
            if (this.matcher.all) {
                long nowElapsed = SystemClock.elapsedRealtime();
                for (int user : this.this$0.mAm.mUserController.getUsers()) {
                    boolean printedUser = false;
                    ServiceMap smap = this.this$0.mServiceMap.get(user);
                    if (smap != null) {
                        for (int i5 = smap.mActiveForegroundApps.size() - 1; i5 >= 0; i5--) {
                            ActiveForegroundApp aa = smap.mActiveForegroundApps.valueAt(i5);
                            if (this.dumpPackage == null || this.dumpPackage.equals(aa.mPackageName)) {
                                if (!printedUser) {
                                    printedUser = true;
                                    this.printedAnything = true;
                                    if (this.needSep) {
                                        this.pw.println();
                                    }
                                    this.needSep = true;
                                    this.pw.print("Active foreground apps - user ");
                                    this.pw.print(user);
                                    this.pw.println(":");
                                }
                                this.pw.print("  #");
                                this.pw.print(i5);
                                this.pw.print(": ");
                                this.pw.println(aa.mPackageName);
                                if (aa.mLabel != null) {
                                    this.pw.print("    mLabel=");
                                    this.pw.println(aa.mLabel);
                                }
                                this.pw.print("    mNumActive=");
                                this.pw.print(aa.mNumActive);
                                this.pw.print(" mAppOnTop=");
                                this.pw.print(aa.mAppOnTop);
                                this.pw.print(" mShownWhileTop=");
                                this.pw.print(aa.mShownWhileTop);
                                this.pw.print(" mShownWhileScreenOn=");
                                this.pw.println(aa.mShownWhileScreenOn);
                                this.pw.print("    mStartTime=");
                                TimeUtils.formatDuration(aa.mStartTime - nowElapsed, this.pw);
                                this.pw.print(" mStartVisibleTime=");
                                TimeUtils.formatDuration(aa.mStartVisibleTime - nowElapsed, this.pw);
                                this.pw.println();
                                if (aa.mEndTime != 0) {
                                    this.pw.print("    mEndTime=");
                                    TimeUtils.formatDuration(aa.mEndTime - nowElapsed, this.pw);
                                    this.pw.println();
                                }
                            }
                        }
                        if (smap.hasMessagesOrCallbacks() != 0) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.printedAnything = true;
                            this.needSep = true;
                            this.pw.print("  Handler - user ");
                            this.pw.print(user);
                            this.pw.println(":");
                            smap.dumpMine(new PrintWriterPrinter(this.pw), "    ");
                        }
                    }
                }
            }
            if (!this.printedAnything) {
                this.pw.println("  (nothing)");
            }
        }
    }

    private final class ServiceLookupResult {
        final String permission;
        final ServiceRecord record;

        ServiceLookupResult(ServiceRecord _record, String _permission) {
            this.record = _record;
            this.permission = _permission;
        }
    }

    final class ServiceMap extends Handler {
        static final int MSG_BG_START_TIMEOUT = 1;
        static final int MSG_UPDATE_FOREGROUND_APPS = 2;
        final ArrayMap<String, ActiveForegroundApp> mActiveForegroundApps = new ArrayMap<>();
        boolean mActiveForegroundAppsChanged;
        final ArrayList<ServiceRecord> mDelayedStartList = new ArrayList<>();
        final ArrayMap<Intent.FilterComparison, ServiceRecord> mServicesByIntent = new ArrayMap<>();
        final ArrayMap<ComponentName, ServiceRecord> mServicesByName = new ArrayMap<>();
        final ArrayList<ServiceRecord> mStartingBackground = new ArrayList<>();
        final int mUserId;

        ServiceMap(Looper looper, int userId) {
            super(looper);
            this.mUserId = userId;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (ActiveServices.this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            rescheduleDelayedStartsLocked();
                        } catch (Throwable th) {
                            while (true) {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                                break;
                            }
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                case 2:
                    ActiveServices.this.updateForegroundApps(this);
                    return;
                default:
                    return;
            }
        }

        /* access modifiers changed from: package-private */
        public void ensureNotStartingBackgroundLocked(ServiceRecord r) {
            if (this.mStartingBackground.remove(r)) {
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    Slog.v(ActivityManagerService.TAG, "No longer background starting: " + r);
                }
                rescheduleDelayedStartsLocked();
            }
            if (this.mDelayedStartList.remove(r) && ActiveServices.DEBUG_DELAYED_STARTS) {
                Slog.v(ActivityManagerService.TAG, "No longer delaying start: " + r);
            }
        }

        /* access modifiers changed from: package-private */
        public void rescheduleDelayedStartsLocked() {
            removeMessages(1);
            long now = SystemClock.uptimeMillis();
            int i = 0;
            int N = this.mStartingBackground.size();
            while (i < N) {
                if (this.mStartingBackground.get(i).startingBgTimeout <= now) {
                    Slog.i(ActivityManagerService.TAG, "Waited long enough for: " + r);
                    this.mStartingBackground.remove(i);
                    N += -1;
                    i += -1;
                }
                i++;
            }
            while (this.mDelayedStartList.size() > 0 && this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                ServiceRecord r = this.mDelayedStartList.remove(0);
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    Slog.v(ActivityManagerService.TAG, "REM FR DELAY LIST (exec next): " + r);
                }
                if (r.pendingStarts.size() <= 0) {
                    Slog.w(ActivityManagerService.TAG, "**** NO PENDING STARTS! " + r + " startReq=" + r.startRequested + " delayedStop=" + r.delayedStop);
                } else {
                    if (ActiveServices.DEBUG_DELAYED_SERVICE && this.mDelayedStartList.size() > 0) {
                        Slog.v(ActivityManagerService.TAG, "Remaining delayed list:");
                        for (int i2 = 0; i2 < this.mDelayedStartList.size(); i2++) {
                            Slog.v(ActivityManagerService.TAG, "  #" + i2 + ": " + this.mDelayedStartList.get(i2));
                        }
                    }
                    r.delayed = false;
                    try {
                        ProcessRecord servicePR = ActiveServices.this.mAm.getProcessRecordLocked(r.processName, r.appInfo.uid, false);
                        if (!(r.appInfo.uid < 10000 || servicePR == null || servicePR.thread == null)) {
                            LogPower.push(148, "serviceboot", r.packageName, Integer.toString(servicePR.pid));
                        }
                        ActiveServices.this.startServiceInnerLocked(this, r.pendingStarts.get(0).intent, r, false, true);
                    } catch (TransactionTooLargeException e) {
                    }
                }
            }
            if (this.mStartingBackground.size() > 0) {
                ServiceRecord next = this.mStartingBackground.get(0);
                long when = next.startingBgTimeout > now ? next.startingBgTimeout : now;
                if (ActiveServices.DEBUG_DELAYED_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Top bg start is " + next + ", can delay others up to " + when);
                }
                sendMessageAtTime(obtainMessage(1), when);
            }
            if (this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                ActiveServices.this.mAm.backgroundServicesFinishedLocked(this.mUserId);
            }
        }
    }

    private class ServiceRestarter implements Runnable {
        private ServiceRecord mService;

        private ServiceRestarter() {
        }

        /* access modifiers changed from: package-private */
        public void setService(ServiceRecord service) {
            this.mService = service;
        }

        public void run() {
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActiveServices.this.performServiceRestartLocked(this.mService);
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }
    }

    public ActiveServices(ActivityManagerService service) {
        int i = 1;
        this.mScreenOn = true;
        this.mLastAnrDumpClearer = new Runnable() {
            public void run() {
                synchronized (ActiveServices.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        ActiveServices.this.mLastAnrDump = null;
                    } catch (Throwable th) {
                        while (true) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        };
        this.mAm = service;
        int maxBg = 0;
        try {
            maxBg = Integer.parseInt(SystemProperties.get("ro.config.max_starting_bg", "0"));
        } catch (RuntimeException e) {
        }
        if (maxBg > 0) {
            i = maxBg;
        } else if (!ActivityManager.isLowRamDeviceStatic()) {
            i = 8;
        }
        this.mMaxStartingBackground = i;
    }

    /* access modifiers changed from: package-private */
    public void systemServicesReady() {
        ((AppStateTracker) LocalServices.getService(AppStateTracker.class)).addListener(new ForcedStandbyListener());
    }

    /* access modifiers changed from: package-private */
    public ServiceRecord getServiceByNameLocked(ComponentName name, int callingUser) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "getServiceByNameLocked(" + name + "), callingUser = " + callingUser);
        }
        return getServiceMapLocked(callingUser).mServicesByName.get(name);
    }

    /* access modifiers changed from: package-private */
    public boolean hasBackgroundServicesLocked(int callingUser) {
        ServiceMap smap = this.mServiceMap.get(callingUser);
        return smap != null && smap.mStartingBackground.size() >= this.mMaxStartingBackground;
    }

    /* access modifiers changed from: private */
    public ServiceMap getServiceMapLocked(int callingUser) {
        ServiceMap smap = this.mServiceMap.get(callingUser);
        if (smap != null) {
            return smap;
        }
        ServiceMap smap2 = new ServiceMap(this.mAm.mHandler.getLooper(), callingUser);
        this.mServiceMap.put(callingUser, smap2);
        return smap2;
    }

    /* access modifiers changed from: package-private */
    public ArrayMap<ComponentName, ServiceRecord> getServicesLocked(int callingUser) {
        return getServiceMapLocked(callingUser).mServicesByName;
    }

    private boolean appRestrictedAnyInBackground(int uid, String packageName) {
        return this.mAm.mAppOpsService.checkOperation(70, uid, packageName) != 0;
    }

    /* access modifiers changed from: package-private */
    public ComponentName startServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int callingPid, int callingUid, boolean fgRequired, String callingPackage, int userId) throws TransactionTooLargeException {
        String str;
        boolean callerFg;
        boolean callerFg2;
        boolean forceSilentAbort;
        boolean fgRequired2;
        boolean callerFg3;
        ServiceRecord r;
        IApplicationThread iApplicationThread = caller;
        Intent intent = service;
        int i = callingPid;
        int i2 = callingUid;
        String str2 = callingPackage;
        if (DEBUG_DELAYED_STARTS) {
            StringBuilder sb = new StringBuilder();
            sb.append("startService: ");
            sb.append(intent);
            sb.append(" type=");
            str = resolvedType;
            sb.append(str);
            sb.append(" args=");
            sb.append(service.getExtras());
            Slog.v(ActivityManagerService.TAG, sb.toString());
        } else {
            str = resolvedType;
        }
        if (iApplicationThread != null) {
            ProcessRecord callerApp = this.mAm.getRecordForAppLocked(iApplicationThread);
            if (callerApp != null) {
                callerFg = callerApp.setSchedGroup != 0;
            } else {
                throw new SecurityException("Unable to find app for caller " + iApplicationThread + " (pid=" + i + ") when starting service " + intent);
            }
        } else {
            callerFg = true;
        }
        this.mAm.setServiceFlagLocked(1);
        boolean callerFg4 = callerFg;
        String str3 = str2;
        ServiceLookupResult res = retrieveServiceLocked(intent, str, str2, i, i2, userId, true, callerFg4, false, false);
        this.mAm.setServiceFlagLocked(0);
        if (res == null) {
            return null;
        }
        if (res.record == null) {
            return new ComponentName("!", res.permission != null ? res.permission : "private to package");
        }
        ServiceRecord r2 = res.record;
        if (!this.mAm.mUserController.exists(r2.userId)) {
            Slog.w(ActivityManagerService.TAG, "Trying to start service with non-existent user! " + r2.userId);
            return null;
        }
        boolean bgLaunch = !this.mAm.isUidActiveLocked(r2.appInfo.uid);
        boolean forcedStandby = false;
        if (!bgLaunch || !appRestrictedAnyInBackground(r2.appInfo.uid, r2.packageName)) {
            callerFg2 = callerFg4;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Forcing bg-only service start only for ");
                sb2.append(r2.shortName);
                sb2.append(" : bgLaunch=");
                sb2.append(bgLaunch);
                sb2.append(" callerFg=");
                callerFg2 = callerFg4;
                sb2.append(callerFg2);
                Slog.d(ActivityManagerService.TAG, sb2.toString());
            } else {
                callerFg2 = callerFg4;
            }
            forcedStandby = true;
        }
        boolean forcedStandby2 = forcedStandby;
        if (fgRequired) {
            int mode = this.mAm.mAppOpsService.checkOperation(76, r2.appInfo.uid, r2.packageName);
            if (mode != 3) {
                switch (mode) {
                    case 0:
                        break;
                    case 1:
                        Slog.w(ActivityManagerService.TAG, "startForegroundService not allowed due to app op: service " + intent + " to " + r2.name.flattenToShortString() + " from pid=" + i + " uid=" + i2 + " pkg=" + str3);
                        forceSilentAbort = true;
                        fgRequired2 = false;
                        break;
                    default:
                        return new ComponentName("!!", "foreground not allowed as per app op");
                }
            }
        }
        fgRequired2 = fgRequired;
        forceSilentAbort = false;
        if (forcedStandby2 || (!r2.startRequested && !fgRequired2)) {
            callerFg3 = callerFg2;
            boolean z = bgLaunch;
            r = r2;
            int allowed = this.mAm.getAppStartModeLocked(r2.appInfo.uid, r2.packageName, r2.appInfo.targetSdkVersion, i, false, false, forcedStandby2);
            if (allowed != 0) {
                Slog.w(ActivityManagerService.TAG, "Background start not allowed: service " + intent + " to " + r.name.flattenToShortString() + " from pid=" + i + " uid=" + i2 + " pkg=" + str3 + " startFg?=" + fgRequired2);
                if (allowed == 1 || forceSilentAbort) {
                    return null;
                }
                if (!forcedStandby2 || !fgRequired2) {
                    return new ComponentName("?", "app is in background uid " + this.mAm.mActiveUids.get(r.appInfo.uid));
                }
                if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                    Slog.v(ActivityManagerService.TAG, "Silently dropping foreground service launch due to FAS");
                }
                return null;
            }
        } else {
            callerFg3 = callerFg2;
            boolean z2 = bgLaunch;
            r = r2;
        }
        PGManagerInternal pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        if (pgm == null || str3 == null || this.mAm.getUidStateLocked(i2) == 2 || str3.equals(r.name.getPackageName()) || !pgm.isServiceProxy(r.name, null)) {
            if (r.appInfo.targetSdkVersion < 26 && fgRequired2) {
                if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK || ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.i(ActivityManagerService.TAG, "startForegroundService() but host targets " + r.appInfo.targetSdkVersion + " - not requiring startForeground()");
                }
                fgRequired2 = false;
            }
            ActivityManagerService.NeededUriGrants neededGrants = this.mAm.checkGrantUriPermissionFromIntentLocked(i2, r.packageName, intent, service.getFlags(), null, r.userId);
            if (this.mAm.mPermissionReviewRequired && !requestStartTargetPermissionsReviewIfNeededLocked(r, str3, i2, intent, callerFg3, userId)) {
                return null;
            }
            if (unscheduleServiceRestartLocked(r, i2, false) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(ActivityManagerService.TAG, "START SERVICE WHILE RESTART PENDING: " + r);
            }
            r.lastActivity = SystemClock.uptimeMillis();
            r.startRequested = true;
            r.delayedStop = false;
            r.fgRequired = fgRequired2;
            ServiceRecord.StartItem startItem = r0;
            PGManagerInternal pGManagerInternal = pgm;
            ServiceRecord.StartItem startItem2 = new ServiceRecord.StartItem(r, false, r.makeNextStartId(), intent, neededGrants, i2);
            r.pendingStarts.add(startItem);
            if (fgRequired2) {
                this.mAm.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, r.appInfo.uid, r.packageName, true);
            }
            ServiceMap smap = getServiceMapLocked(r.userId);
            boolean addToStarting = false;
            boolean callerFg5 = callerFg3;
            if (!callerFg5 && !fgRequired2 && r.app == null && this.mAm.mUserController.hasStartedUserState(r.userId)) {
                ProcessRecord proc = this.mAm.getProcessRecordLocked(r.processName, r.appInfo.uid, false);
                if (proc == null || proc.curProcState > 10) {
                    if (DEBUG_DELAYED_SERVICE) {
                        Slog.v(ActivityManagerService.TAG, "Potential start delay of " + r + " in " + proc);
                    }
                    if (r.delayed) {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(ActivityManagerService.TAG, "Continuing to delay: " + r);
                        }
                        return r.name;
                    } else if (smap.mStartingBackground.size() >= this.mMaxStartingBackground) {
                        Slog.i(ActivityManagerService.TAG, "Delaying start of: " + r);
                        smap.mDelayedStartList.add(r);
                        r.delayed = true;
                        return r.name;
                    } else {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(ActivityManagerService.TAG, "Not delaying: " + r);
                        }
                        addToStarting = true;
                    }
                } else if (proc.curProcState >= 9) {
                    addToStarting = true;
                    if (DEBUG_DELAYED_STARTS) {
                        Slog.v(ActivityManagerService.TAG, "Not delaying, but counting as bg: " + r);
                    }
                } else if (DEBUG_DELAYED_STARTS) {
                    StringBuilder sb3 = new StringBuilder(128);
                    sb3.append("Not potential delay (state=");
                    sb3.append(proc.curProcState);
                    sb3.append(' ');
                    sb3.append(proc.adjType);
                    String reason = proc.makeAdjReason();
                    if (reason != null) {
                        sb3.append(' ');
                        sb3.append(reason);
                    }
                    sb3.append("): ");
                    sb3.append(r.toString());
                    Slog.v(ActivityManagerService.TAG, sb3.toString());
                }
            } else if (DEBUG_DELAYED_STARTS) {
                if (callerFg5 || fgRequired2) {
                    Slog.v(ActivityManagerService.TAG, "Not potential delay (callerFg=" + callerFg5 + " uid=" + i2 + " pid=" + i + " fgRequired=" + fgRequired2 + "): " + r);
                } else if (r.app != null) {
                    Slog.v(ActivityManagerService.TAG, "Not potential delay (cur app=" + r.app + "): " + r);
                } else {
                    Slog.v(ActivityManagerService.TAG, "Not potential delay (user " + r.userId + " not started): " + r);
                }
            }
            boolean addToStarting2 = addToStarting;
            if (r.appInfo.uid >= 10000 && str3 != null && !str3.equals(r.packageName)) {
                LogPower.push(148, "serviceboot", r.packageName, Integer.toString(0), new String[]{str3});
            }
            return startServiceInnerLocked(smap, intent, r, callerFg5, addToStarting2);
        }
        Slog.i(ActivityManagerService.TAG, "start service is proxy: " + r.name);
        return null;
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(ServiceRecord r, String callingPackage, int callingUid, Intent service, boolean callerFg, int userId) {
        ServiceRecord serviceRecord = r;
        Intent intent = service;
        if (!this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(serviceRecord.packageName, serviceRecord.userId)) {
            int i = userId;
            return true;
        } else if (!callerFg) {
            Slog.w(ActivityManagerService.TAG, "u" + serviceRecord.userId + " Starting a service in package" + serviceRecord.packageName + " requires a permissions review");
            return false;
        } else {
            IIntentSender target = this.mAm.getIntentSenderLocked(4, callingPackage, callingUid, userId, null, null, 0, new Intent[]{intent}, new String[]{intent.resolveType(this.mAm.mContext.getContentResolver())}, 1409286144, null);
            final Intent intent2 = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent2.addFlags(276824064);
            intent2.putExtra("android.intent.extra.PACKAGE_NAME", serviceRecord.packageName);
            intent2.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                Slog.i(ActivityManagerService.TAG, "u" + serviceRecord.userId + " Launching permission review for package " + serviceRecord.packageName);
            }
            final int i2 = userId;
            this.mAm.mHandler.post(new Runnable() {
                public void run() {
                    ActiveServices.this.mAm.mContext.startActivityAsUser(intent2, new UserHandle(i2));
                }
            });
            return false;
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00d2, code lost:
        r0 = th;
     */
    public ComponentName startServiceInnerLocked(ServiceMap smap, Intent service, ServiceRecord r, boolean callerFg, boolean addToStarting) throws TransactionTooLargeException {
        ServiceMap serviceMap = smap;
        ServiceRecord serviceRecord = r;
        ServiceState stracker = r.getTracker();
        if (stracker != null) {
            stracker.setStarted(true, this.mAm.mProcessStats.getMemFactorLocked(), serviceRecord.lastActivity);
        }
        boolean z = false;
        serviceRecord.callStart = false;
        synchronized (serviceRecord.stats.getBatteryStats()) {
            try {
                serviceRecord.stats.startRunningLocked();
            } catch (Throwable th) {
                th = th;
                Intent intent = service;
                while (true) {
                    throw th;
                }
            }
        }
        Intent intent2 = service;
        this.mAm.mHwAMSEx.setHbsMiniAppUid(serviceRecord.appInfo, intent2);
        String error = bringUpServiceLocked(serviceRecord, intent2.getFlags(), callerFg, false, false);
        if (error != null) {
            return new ComponentName("!!", error);
        }
        if (serviceRecord.startRequested && addToStarting) {
            if (serviceMap.mStartingBackground.size() == 0) {
                z = true;
            }
            boolean first = z;
            serviceMap.mStartingBackground.add(serviceRecord);
            serviceRecord.startingBgTimeout = SystemClock.uptimeMillis() + this.mAm.mConstants.BG_START_TIMEOUT;
            if (DEBUG_DELAYED_SERVICE) {
                RuntimeException here = new RuntimeException("here");
                here.fillInStackTrace();
                Slog.v(ActivityManagerService.TAG, "Starting background (first=" + first + "): " + serviceRecord, here);
            } else if (DEBUG_DELAYED_STARTS) {
                Slog.v(ActivityManagerService.TAG, "Starting background (first=" + first + "): " + serviceRecord);
            }
            if (first) {
                serviceMap.rescheduleDelayedStartsLocked();
            }
        } else if (callerFg || serviceRecord.fgRequired) {
            serviceMap.ensureNotStartingBackgroundLocked(serviceRecord);
        }
        return serviceRecord.name;
    }

    private void stopServiceLocked(ServiceRecord service) {
        if (service.delayed) {
            if (DEBUG_DELAYED_STARTS) {
                Slog.v(ActivityManagerService.TAG, "Delaying stop of pending: " + service);
            }
            service.delayedStop = true;
            return;
        }
        synchronized (service.stats.getBatteryStats()) {
            service.stats.stopRunningLocked();
        }
        service.startRequested = false;
        if (service.tracker != null) {
            service.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
        }
        service.callStart = false;
        bringDownServiceIfNeededLocked(service, false, false);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public int stopServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int userId) {
        String str;
        IApplicationThread iApplicationThread = caller;
        Intent intent = service;
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            StringBuilder sb = new StringBuilder();
            sb.append("stopService: ");
            sb.append(intent);
            sb.append(" type=");
            str = resolvedType;
            sb.append(str);
            Slog.v(ActivityManagerService.TAG, sb.toString());
        } else {
            str = resolvedType;
        }
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(iApplicationThread);
        if (iApplicationThread == null || callerApp != null) {
            ServiceLookupResult r = retrieveServiceLocked(intent, str, null, Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, false, false);
            if (r == null) {
                return 0;
            }
            if (r.record == null) {
                return -1;
            }
            long origId = Binder.clearCallingIdentity();
            try {
                stopServiceLocked(r.record);
                Binder.restoreCallingIdentity(origId);
                return 1;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        } else {
            throw new SecurityException("Unable to find app for caller " + iApplicationThread + " (pid=" + Binder.getCallingPid() + ") when stopping service " + intent);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopInBackgroundLocked(int uid) {
        ServiceMap services = this.mServiceMap.get(UserHandle.getUserId(uid));
        ArrayList<ServiceRecord> stopping = null;
        if (services != null) {
            for (int i = services.mServicesByName.size() - 1; i >= 0; i--) {
                ServiceRecord service = services.mServicesByName.valueAt(i);
                if (service != null && service.appInfo.uid == uid && service.startRequested && this.mAm.getAppStartModeLocked(service.appInfo.uid, service.packageName, service.appInfo.targetSdkVersion, -1, false, false, false) != 0) {
                    if (stopping == null) {
                        stopping = new ArrayList<>();
                    }
                    String compName = service.name.flattenToShortString();
                    EventLogTags.writeAmStopIdleService(service.appInfo.uid, compName);
                    StringBuilder sb = new StringBuilder(64);
                    sb.append("Stopping service due to app idle: ");
                    UserHandle.formatUid(sb, service.appInfo.uid);
                    sb.append(" ");
                    TimeUtils.formatDuration(service.createRealTime - SystemClock.elapsedRealtime(), sb);
                    sb.append(" ");
                    sb.append(compName);
                    Slog.w(ActivityManagerService.TAG, sb.toString());
                    stopping.add(service);
                }
            }
            if (stopping != null) {
                for (int i2 = stopping.size() - 1; i2 >= 0; i2--) {
                    ServiceRecord service2 = stopping.get(i2);
                    service2.delayed = false;
                    services.ensureNotStartingBackgroundLocked(service2);
                    stopServiceLocked(service2);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public IBinder peekServiceLocked(Intent service, String resolvedType, String callingPackage) {
        ServiceLookupResult r = retrieveServiceLocked(service, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), UserHandle.getCallingUserId(), false, false, false, false);
        if (r == null) {
            return null;
        }
        if (r.record != null) {
            IntentBindRecord ib = r.record.bindings.get(r.record.intent);
            if (ib != null) {
                return ib.binder;
            }
            return null;
        }
        throw new SecurityException("Permission Denial: Accessing service from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + r.permission);
    }

    /* access modifiers changed from: package-private */
    public boolean stopServiceTokenLocked(ComponentName className, IBinder token, int startId) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "stopServiceToken: " + className + " " + token + " startId=" + startId);
        }
        ServiceRecord r = findServiceLocked(className, token, UserHandle.getCallingUserId());
        if (r == null) {
            return false;
        }
        if (startId >= 0) {
            ServiceRecord.StartItem si = r.findDeliveredStart(startId, false, false);
            if (si != null) {
                while (r.deliveredStarts.size() > 0) {
                    ServiceRecord.StartItem cur = r.deliveredStarts.remove(0);
                    cur.removeUriPermissionsLocked();
                    if (cur == si) {
                        break;
                    }
                }
            }
            if (r.getLastStartId() != startId) {
                return false;
            }
            if (r.deliveredStarts.size() > 0) {
                Slog.w(ActivityManagerService.TAG, "stopServiceToken startId " + startId + " is last, but have " + r.deliveredStarts.size() + " remaining args");
            }
        }
        synchronized (r.stats.getBatteryStats()) {
            r.stats.stopRunningLocked();
        }
        r.startRequested = false;
        if (r.tracker != null) {
            r.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
        }
        r.callStart = false;
        long origId = Binder.clearCallingIdentity();
        bringDownServiceIfNeededLocked(r, false, false);
        Binder.restoreCallingIdentity(origId);
        return true;
    }

    public void setServiceForegroundLocked(ComponentName className, IBinder token, int id, Notification notification, int flags) {
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        try {
            ServiceRecord r = findServiceLocked(className, token, userId);
            if (r != null) {
                setServiceForegroundInnerLocked(r, id, notification, flags);
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean foregroundAppShownEnoughLocked(ActiveForegroundApp aa, long nowElapsed) {
        long j;
        if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
            Slog.d(ActivityManagerService.TAG, "Shown enough: pkg=" + aa.mPackageName + ", uid=" + aa.mUid);
        }
        aa.mHideTime = JobStatus.NO_LATEST_RUNTIME;
        if (aa.mShownWhileTop) {
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return true;
            }
            Slog.d(ActivityManagerService.TAG, "YES - shown while on top");
            return true;
        } else if (this.mScreenOn || aa.mShownWhileScreenOn) {
            long j2 = aa.mStartVisibleTime;
            if (aa.mStartTime != aa.mStartVisibleTime) {
                j = this.mAm.mConstants.FGSERVICE_SCREEN_ON_AFTER_TIME;
            } else {
                j = this.mAm.mConstants.FGSERVICE_MIN_SHOWN_TIME;
            }
            long minTime = j2 + j;
            if (nowElapsed >= minTime) {
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(ActivityManagerService.TAG, "YES - shown long enough with screen on");
                }
                return true;
            }
            long reportTime = this.mAm.mConstants.FGSERVICE_MIN_REPORT_TIME + nowElapsed;
            aa.mHideTime = reportTime > minTime ? reportTime : minTime;
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return false;
            }
            Slog.d(ActivityManagerService.TAG, "NO -- wait " + (aa.mHideTime - nowElapsed) + " with screen on");
            return false;
        } else {
            long minTime2 = aa.mEndTime + this.mAm.mConstants.FGSERVICE_SCREEN_ON_BEFORE_TIME;
            if (nowElapsed >= minTime2) {
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(ActivityManagerService.TAG, "YES - gone long enough with screen off");
                }
                return true;
            }
            aa.mHideTime = minTime2;
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return false;
            }
            Slog.d(ActivityManagerService.TAG, "NO -- wait " + (aa.mHideTime - nowElapsed) + " with screen off");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateForegroundApps(ServiceMap smap) {
        ArrayList<ActiveForegroundApp> active = null;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                long now = SystemClock.elapsedRealtime();
                long nextUpdateTime = JobStatus.NO_LATEST_RUNTIME;
                if (smap != null) {
                    if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                        Slog.d(ActivityManagerService.TAG, "Updating foreground apps for user " + smap.mUserId);
                    }
                    for (int i = smap.mActiveForegroundApps.size() - 1; i >= 0; i--) {
                        ActiveForegroundApp aa = smap.mActiveForegroundApps.valueAt(i);
                        if (aa.mEndTime != 0) {
                            if (foregroundAppShownEnoughLocked(aa, now)) {
                                smap.mActiveForegroundApps.removeAt(i);
                                smap.mActiveForegroundAppsChanged = true;
                            } else if (aa.mHideTime < nextUpdateTime) {
                                nextUpdateTime = aa.mHideTime;
                            }
                        }
                        if (!aa.mAppOnTop) {
                            if (active == null) {
                                active = new ArrayList<>();
                            }
                            if (ActivityManagerDebugConfig.HWFLOW) {
                                Slog.i(ActivityManagerService.TAG, " updateForegroundApps Adding active: pkg= " + aa.mPackageName + ", uid=" + aa.mUid);
                            }
                            active.add(aa);
                        }
                    }
                    smap.removeMessages(2);
                    if (nextUpdateTime < JobStatus.NO_LATEST_RUNTIME) {
                        if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                            Slog.d(ActivityManagerService.TAG, "Next update time in: " + (nextUpdateTime - now));
                        }
                        smap.sendMessageAtTime(smap.obtainMessage(2), (SystemClock.uptimeMillis() + nextUpdateTime) - SystemClock.elapsedRealtime());
                    }
                }
                if (!smap.mActiveForegroundAppsChanged) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                smap.mActiveForegroundAppsChanged = false;
                ActivityManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    private void requestUpdateActiveForegroundAppsLocked(ServiceMap smap, long timeElapsed) {
        Message msg = smap.obtainMessage(2);
        if (timeElapsed != 0) {
            smap.sendMessageAtTime(msg, (SystemClock.uptimeMillis() + timeElapsed) - SystemClock.elapsedRealtime());
            return;
        }
        smap.mActiveForegroundAppsChanged = true;
        smap.sendMessage(msg);
    }

    private void decActiveForegroundAppLocked(ServiceMap smap, ServiceRecord r) {
        ActiveForegroundApp active = smap.mActiveForegroundApps.get(r.packageName);
        if (active != null) {
            active.mNumActive--;
            if (active.mNumActive <= 0) {
                active.mEndTime = SystemClock.elapsedRealtime();
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(ActivityManagerService.TAG, "Ended running of service");
                }
                if (foregroundAppShownEnoughLocked(active, active.mEndTime)) {
                    smap.mActiveForegroundApps.remove(r.packageName);
                    smap.mActiveForegroundAppsChanged = true;
                    requestUpdateActiveForegroundAppsLocked(smap, 0);
                } else if (active.mHideTime < JobStatus.NO_LATEST_RUNTIME) {
                    requestUpdateActiveForegroundAppsLocked(smap, active.mHideTime);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateScreenStateLocked(boolean screenOn) {
        if (this.mScreenOn != screenOn) {
            this.mScreenOn = screenOn;
            if (screenOn) {
                long nowElapsed = SystemClock.elapsedRealtime();
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(ActivityManagerService.TAG, "Screen turned on");
                }
                for (int i = this.mServiceMap.size() - 1; i >= 0; i--) {
                    ServiceMap smap = this.mServiceMap.valueAt(i);
                    long nextUpdateTime = JobStatus.NO_LATEST_RUNTIME;
                    boolean changed = false;
                    for (int j = smap.mActiveForegroundApps.size() - 1; j >= 0; j--) {
                        ActiveForegroundApp active = smap.mActiveForegroundApps.valueAt(j);
                        if (active.mEndTime != 0) {
                            if (!active.mShownWhileScreenOn && active.mStartVisibleTime == active.mStartTime) {
                                active.mStartVisibleTime = nowElapsed;
                                active.mEndTime = nowElapsed;
                            }
                            if (foregroundAppShownEnoughLocked(active, nowElapsed)) {
                                smap.mActiveForegroundApps.remove(active.mPackageName);
                                smap.mActiveForegroundAppsChanged = true;
                                changed = true;
                            } else if (active.mHideTime < nextUpdateTime) {
                                nextUpdateTime = active.mHideTime;
                            }
                        } else if (!active.mShownWhileScreenOn) {
                            active.mShownWhileScreenOn = true;
                            active.mStartVisibleTime = nowElapsed;
                        }
                    }
                    if (changed) {
                        requestUpdateActiveForegroundAppsLocked(smap, 0);
                    } else if (nextUpdateTime < JobStatus.NO_LATEST_RUNTIME) {
                        requestUpdateActiveForegroundAppsLocked(smap, nextUpdateTime);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void foregroundServiceProcStateChangedLocked(UidRecord uidRec) {
        ServiceMap smap = this.mServiceMap.get(UserHandle.getUserId(uidRec.uid));
        if (smap != null) {
            boolean changed = false;
            for (int j = smap.mActiveForegroundApps.size() - 1; j >= 0; j--) {
                ActiveForegroundApp active = smap.mActiveForegroundApps.valueAt(j);
                if (active.mUid == uidRec.uid) {
                    if (uidRec.curProcState <= 2) {
                        if (!active.mAppOnTop) {
                            active.mAppOnTop = true;
                            changed = true;
                        }
                        active.mShownWhileTop = true;
                    } else if (active.mAppOnTop) {
                        active.mAppOnTop = false;
                        changed = true;
                    }
                }
            }
            if (changed) {
                requestUpdateActiveForegroundAppsLocked(smap, 0);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setServiceForegroundInnerLocked(ServiceRecord r, int id, Notification notification, int flags) {
        ServiceRecord serviceRecord = r;
        int i = id;
        Notification notification2 = notification;
        boolean z = false;
        if (i == 0) {
            if (serviceRecord.isForeground) {
                ServiceMap smap = getServiceMapLocked(serviceRecord.userId);
                if (smap != null) {
                    decActiveForegroundAppLocked(smap, serviceRecord);
                }
                serviceRecord.isForeground = false;
                this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, serviceRecord.appInfo.uid, serviceRecord.packageName);
                StatsLog.write(60, serviceRecord.appInfo.uid, serviceRecord.shortName, 2);
                if (serviceRecord.app != null) {
                    this.mAm.updateLruProcessLocked(serviceRecord.app, false, null);
                    updateServiceForegroundLocked(serviceRecord.app, true);
                }
            }
            if ((flags & 1) != 0) {
                cancelForegroundNotificationLocked(r);
                serviceRecord.foregroundId = 0;
                serviceRecord.foregroundNoti = null;
            } else if (serviceRecord.appInfo.targetSdkVersion >= 21) {
                r.stripForegroundServiceFlagFromNotification();
                if ((flags & 2) != 0) {
                    serviceRecord.foregroundId = 0;
                    serviceRecord.foregroundNoti = null;
                }
            }
        } else if (notification2 != null) {
            if (serviceRecord.appInfo.isInstantApp()) {
                switch (this.mAm.mAppOpsService.checkOperation(68, serviceRecord.appInfo.uid, serviceRecord.appInfo.packageName)) {
                    case 0:
                        break;
                    case 1:
                        Slog.w(ActivityManagerService.TAG, "Instant app " + serviceRecord.appInfo.packageName + " does not have permission to create foreground services, ignoring.");
                        return;
                    case 2:
                        throw new SecurityException("Instant app " + serviceRecord.appInfo.packageName + " does not have permission to create foreground services");
                    default:
                        this.mAm.enforcePermission("android.permission.INSTANT_APP_FOREGROUND_SERVICE", serviceRecord.app.pid, serviceRecord.appInfo.uid, "startForeground");
                        break;
                }
            } else if (serviceRecord.appInfo.targetSdkVersion >= 28) {
                this.mAm.enforcePermission("android.permission.FOREGROUND_SERVICE", serviceRecord.app.pid, serviceRecord.appInfo.uid, "startForeground");
            }
            boolean alreadyStartedOp = false;
            if (serviceRecord.fgRequired) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE || ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                    Slog.i(ActivityManagerService.TAG, "Service called startForeground() as required: " + serviceRecord);
                }
                serviceRecord.fgRequired = false;
                serviceRecord.fgWaiting = false;
                alreadyStartedOp = true;
                this.mAm.mHandler.removeMessages(66, serviceRecord);
            }
            boolean ignoreForeground = false;
            try {
                int mode = this.mAm.mAppOpsService.checkOperation(76, serviceRecord.appInfo.uid, serviceRecord.packageName);
                if (mode != 3) {
                    switch (mode) {
                        case 0:
                            break;
                        case 1:
                            Slog.w(ActivityManagerService.TAG, "Service.startForeground() not allowed due to app op: service " + serviceRecord.shortName);
                            ignoreForeground = true;
                            break;
                        default:
                            throw new SecurityException("Foreground not allowed as per app op");
                    }
                }
                if (!ignoreForeground && appRestrictedAnyInBackground(serviceRecord.appInfo.uid, serviceRecord.packageName)) {
                    Slog.w(ActivityManagerService.TAG, "Service.startForeground() not allowed due to bg restriction: service " + serviceRecord.shortName);
                    updateServiceForegroundLocked(serviceRecord.app, false);
                    ignoreForeground = true;
                }
                if (!ignoreForeground) {
                    if (serviceRecord.foregroundId != i) {
                        cancelForegroundNotificationLocked(r);
                        serviceRecord.foregroundId = i;
                    }
                    notification2.flags |= 64;
                    serviceRecord.foregroundNoti = notification2;
                    if (!serviceRecord.isForeground) {
                        ServiceMap smap2 = getServiceMapLocked(serviceRecord.userId);
                        if (smap2 != null) {
                            ActiveForegroundApp active = smap2.mActiveForegroundApps.get(serviceRecord.packageName);
                            if (active == null) {
                                active = new ActiveForegroundApp();
                                active.mPackageName = serviceRecord.packageName;
                                active.mUid = serviceRecord.appInfo.uid;
                                active.mShownWhileScreenOn = this.mScreenOn;
                                if (!(serviceRecord.app == null || serviceRecord.app.uidRecord == null)) {
                                    if (serviceRecord.app.uidRecord.curProcState <= 2) {
                                        z = true;
                                    }
                                    active.mShownWhileTop = z;
                                    active.mAppOnTop = z;
                                }
                                long elapsedRealtime = SystemClock.elapsedRealtime();
                                active.mStartVisibleTime = elapsedRealtime;
                                active.mStartTime = elapsedRealtime;
                                smap2.mActiveForegroundApps.put(serviceRecord.packageName, active);
                                requestUpdateActiveForegroundAppsLocked(smap2, 0);
                            }
                            active.mNumActive++;
                        }
                        serviceRecord.isForeground = true;
                        this.mAm.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, serviceRecord.appInfo.uid, serviceRecord.packageName, true);
                        StatsLog.write(60, serviceRecord.appInfo.uid, serviceRecord.shortName, 1);
                    }
                    r.postNotification();
                    if (serviceRecord.app != null) {
                        updateServiceForegroundLocked(serviceRecord.app, true);
                    }
                    getServiceMapLocked(serviceRecord.userId).ensureNotStartingBackgroundLocked(serviceRecord);
                    this.mAm.notifyPackageUse(serviceRecord.serviceInfo.packageName, 2);
                } else if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(ActivityManagerService.TAG, "Suppressing startForeground() for FAS " + serviceRecord);
                }
            } finally {
                if (alreadyStartedOp) {
                    this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, serviceRecord.appInfo.uid, serviceRecord.packageName);
                }
            }
        } else {
            throw new IllegalArgumentException("null notification");
        }
    }

    private void cancelForegroundNotificationLocked(ServiceRecord r) {
        if (!(r == null || r.foregroundId == 0)) {
            ServiceMap sm = getServiceMapLocked(r.userId);
            if (sm != null) {
                int i = sm.mServicesByName.size() - 1;
                while (i >= 0) {
                    ServiceRecord other = sm.mServicesByName.valueAt(i);
                    if (other == null || other == r || other.foregroundId != r.foregroundId || !other.packageName.equals(r.packageName)) {
                        i--;
                    } else {
                        return;
                    }
                }
            }
            r.cancelNotification();
        }
    }

    private void updateServiceForegroundLocked(ProcessRecord proc, boolean oomAdj) {
        boolean anyForeground = false;
        int i = proc.services.size() - 1;
        while (true) {
            if (i < 0) {
                break;
            }
            ServiceRecord sr = proc.services.valueAt(i);
            if (sr.isForeground || sr.fgRequired) {
                anyForeground = true;
            } else {
                i--;
            }
        }
        anyForeground = true;
        this.mAm.updateProcessForegroundLocked(proc, anyForeground, oomAdj);
    }

    private void updateWhitelistManagerLocked(ProcessRecord proc) {
        proc.whitelistManager = false;
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            if (proc.services.valueAt(i).whitelistManager) {
                proc.whitelistManager = true;
                return;
            }
        }
    }

    public void updateServiceConnectionActivitiesLocked(ProcessRecord clientProc) {
        if (clientProc != null && clientProc.connections != null) {
            ArraySet<ProcessRecord> updatedProcesses = null;
            for (int i = 0; i < clientProc.connections.size(); i++) {
                ProcessRecord proc = clientProc.connections.valueAt(i).binding.service.app;
                if (!(proc == null || proc == clientProc)) {
                    if (updatedProcesses == null) {
                        updatedProcesses = new ArraySet<>();
                    } else if (updatedProcesses.contains(proc)) {
                    }
                    updatedProcesses.add(proc);
                    updateServiceClientActivitiesLocked(proc, null, false);
                }
            }
        }
    }

    private boolean updateServiceClientActivitiesLocked(ProcessRecord proc, ConnectionRecord modCr, boolean updateLru) {
        if (modCr != null && modCr.binding.client != null && modCr.binding.client.activities.size() <= 0) {
            return false;
        }
        boolean anyClientActivities = false;
        for (int i = proc.services.size() - 1; i >= 0 && !anyClientActivities; i--) {
            ServiceRecord sr = proc.services.valueAt(i);
            for (int conni = sr.connections.size() - 1; conni >= 0 && !anyClientActivities; conni--) {
                ArrayList<ConnectionRecord> clist = sr.connections.valueAt(conni);
                int cri = clist.size() - 1;
                while (true) {
                    if (cri < 0) {
                        break;
                    }
                    ConnectionRecord cr = clist.get(cri);
                    if (cr.binding.client != null && cr.binding.client != proc && cr.binding.client.activities.size() > 0) {
                        anyClientActivities = true;
                        break;
                    }
                    cri--;
                }
            }
        }
        if (anyClientActivities == proc.hasClientActivities) {
            return false;
        }
        proc.hasClientActivities = anyClientActivities;
        if (updateLru) {
            this.mAm.updateLruProcessLocked(proc, anyClientActivities, null);
        }
        return true;
    }

    /* JADX WARNING: type inference failed for: r3v39, types: [android.os.Parcelable] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x034e A[SYNTHETIC, Splitter:B:113:0x034e] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x0371 A[SYNTHETIC, Splitter:B:122:0x0371] */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x03cd A[SYNTHETIC, Splitter:B:140:0x03cd] */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x03d8  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x03df A[SYNTHETIC, Splitter:B:152:0x03df] */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x03f0 A[SYNTHETIC, Splitter:B:158:0x03f0] */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x0403 A[SYNTHETIC, Splitter:B:165:0x0403] */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x044f  */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x0460  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x049b  */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x04e5 A[SYNTHETIC, Splitter:B:208:0x04e5] */
    /* JADX WARNING: Removed duplicated region for block: B:232:0x055c A[SYNTHETIC, Splitter:B:232:0x055c] */
    /* JADX WARNING: Removed duplicated region for block: B:244:0x058c  */
    public int bindServiceLocked(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws TransactionTooLargeException {
        String str;
        String str2;
        ProcessRecord callerApp;
        boolean callerFg;
        boolean permissionsReviewRequired;
        String str3;
        ProcessRecord callerApp2;
        long origId;
        long origId2;
        Intent service2;
        AppBindRecord b;
        long origId3;
        Intent service3;
        ConnectionRecord c;
        ArrayList<ConnectionRecord> clist;
        ActivityRecord activity;
        int connectionSize;
        ArrayList<ConnectionRecord> clist2;
        ConnectionRecord c2;
        boolean callerFg2;
        ProcessRecord app;
        boolean z;
        IApplicationThread iApplicationThread = caller;
        IBinder iBinder = token;
        Intent intent = service;
        String str4 = callingPackage;
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            StringBuilder sb = new StringBuilder();
            sb.append("bindService: ");
            sb.append(intent);
            sb.append(" type=");
            str = resolvedType;
            sb.append(str);
            sb.append(" conn=");
            sb.append(connection.asBinder());
            sb.append(" flags=0x");
            sb.append(Integer.toHexString(flags));
            Slog.v(ActivityManagerService.TAG, sb.toString());
        } else {
            str = resolvedType;
        }
        ProcessRecord callerApp3 = this.mAm.getRecordForAppLocked(iApplicationThread);
        if (callerApp3 != null) {
            ActivityRecord activity2 = null;
            if (iBinder != null) {
                activity2 = ActivityRecord.isInStackLocked(token);
                if (activity2 == null) {
                    Slog.w(ActivityManagerService.TAG, "Binding with unknown activity: " + iBinder);
                    return 0;
                }
            }
            ActivityRecord activity3 = activity2;
            int clientLabel = 0;
            PendingIntent clientIntent = null;
            boolean isCallerSystem = callerApp3.info.uid == 1000;
            if (isCallerSystem) {
                intent.setDefusable(true);
                clientIntent = intent.getParcelableExtra("android.intent.extra.client_intent");
                if (clientIntent != null) {
                    clientLabel = intent.getIntExtra("android.intent.extra.client_label", 0);
                    if (clientLabel != 0) {
                        intent = service.cloneFilter();
                    }
                }
            }
            Intent service4 = intent;
            int clientLabel2 = clientLabel;
            PendingIntent clientIntent2 = clientIntent;
            if ((flags & 134217728) != 0) {
                this.mAm.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "BIND_TREAT_LIKE_ACTIVITY");
            }
            if ((flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 && !isCallerSystem) {
                throw new SecurityException("Non-system caller " + iApplicationThread + " (pid=" + Binder.getCallingPid() + ") set BIND_ALLOW_WHITELIST_MANAGEMENT when binding service " + service4);
            } else if ((flags & DumpState.DUMP_CHANGES) == 0 || isCallerSystem) {
                boolean callerFg3 = callerApp3.setSchedGroup != 0;
                boolean isBindExternal = (flags & Integer.MIN_VALUE) != 0;
                boolean allowInstant = (flags & DumpState.DUMP_CHANGES) != 0;
                this.mAm.setServiceFlagLocked(2);
                boolean callerFg4 = callerFg3;
                Intent service5 = service4;
                ActivityRecord activity4 = activity3;
                ProcessRecord callerApp4 = callerApp3;
                ServiceLookupResult res = retrieveServiceLocked(service4, str, str4, Binder.getCallingPid(), Binder.getCallingUid(), userId, true, callerFg4, isBindExternal, allowInstant);
                this.mAm.setServiceFlagLocked(0);
                if (res == null) {
                    return 0;
                }
                if (res.record == null) {
                    return -1;
                }
                ServiceRecord s = res.record;
                PGManagerInternal pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
                if (pgm != null) {
                    str2 = callingPackage;
                    if (str2 != null) {
                        callerApp = callerApp4;
                        if (this.mAm.getUidStateLocked(callerApp.info.uid) != 2 && !str2.equals(s.name.getPackageName()) && pgm.isServiceProxy(s.name, null)) {
                            Slog.i(ActivityManagerService.TAG, "bind service is proxy: " + s.name);
                            return 0;
                        }
                    } else {
                        callerApp = callerApp4;
                    }
                } else {
                    callerApp = callerApp4;
                    str2 = callingPackage;
                }
                if (!this.mAm.mPermissionReviewRequired || !this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(s.packageName, s.userId)) {
                    int i = userId;
                    ServiceLookupResult serviceLookupResult = res;
                    PGManagerInternal pGManagerInternal = pgm;
                    callerFg = callerFg4;
                    callerApp2 = callerApp;
                    str3 = str2;
                    permissionsReviewRequired = false;
                } else {
                    boolean callerFg5 = callerFg4;
                    if (!callerFg5) {
                        Slog.w(ActivityManagerService.TAG, "u" + s.userId + " Binding to a service in package" + s.packageName + " requires a permissions review");
                        return 0;
                    }
                    final ServiceRecord serviceRecord = s;
                    final Intent serviceIntent = service5;
                    permissionsReviewRequired = true;
                    callerFg = callerFg5;
                    ServiceLookupResult serviceLookupResult2 = res;
                    callerApp2 = callerApp;
                    final boolean z2 = callerFg;
                    PGManagerInternal pGManagerInternal2 = pgm;
                    str3 = str2;
                    final IServiceConnection iServiceConnection = connection;
                    AnonymousClass3 r1 = new RemoteCallback.OnResultListener() {
                        public void onResult(Bundle result) {
                            long identity;
                            synchronized (ActiveServices.this.mAm) {
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    identity = Binder.clearCallingIdentity();
                                    if (!ActiveServices.this.mPendingServices.contains(serviceRecord)) {
                                        Binder.restoreCallingIdentity(identity);
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        return;
                                    }
                                    if (!ActiveServices.this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(serviceRecord.packageName, serviceRecord.userId)) {
                                        try {
                                            ActiveServices.this.mAm.mHwAMSEx.setHbsMiniAppUid(serviceRecord.appInfo, serviceIntent);
                                            String unused = ActiveServices.this.bringUpServiceLocked(serviceRecord, serviceIntent.getFlags(), z2, false, false);
                                        } catch (RemoteException e) {
                                        }
                                    } else {
                                        ActiveServices.this.unbindServiceLocked(iServiceConnection);
                                    }
                                    Binder.restoreCallingIdentity(identity);
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                } catch (Throwable th) {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                        }
                    };
                    RemoteCallback callback = new RemoteCallback(r1);
                    final Intent intent2 = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                    intent2.addFlags(276824064);
                    intent2.putExtra("android.intent.extra.PACKAGE_NAME", s.packageName);
                    intent2.putExtra("android.intent.extra.REMOTE_CALLBACK", callback);
                    if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                        Slog.i(ActivityManagerService.TAG, "u" + s.userId + " Launching permission review for package " + s.packageName);
                    }
                    final int i2 = userId;
                    this.mAm.mHandler.post(new Runnable() {
                        public void run() {
                            ActiveServices.this.mAm.mContext.startActivityAsUser(intent2, new UserHandle(i2));
                        }
                    });
                }
                long origId4 = Binder.clearCallingIdentity();
                try {
                    if (unscheduleServiceRestartLocked(s, callerApp2.info.uid, false)) {
                        try {
                            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                Slog.v(ActivityManagerService.TAG, "BIND SERVICE WHILE RESTART PENDING: " + s);
                            }
                        } catch (Throwable th) {
                            th = th;
                            origId = origId4;
                            boolean z3 = isBindExternal;
                            boolean z4 = allowInstant;
                            Intent intent3 = service5;
                            ActivityRecord activityRecord = activity4;
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    }
                    if ((flags & 1) != 0) {
                        s.lastActivity = SystemClock.uptimeMillis();
                        if (!s.hasAutoCreateConnections()) {
                            ServiceState stracker = s.getTracker();
                            if (stracker != null) {
                                stracker.setBound(true, this.mAm.mProcessStats.getMemFactorLocked(), s.lastActivity);
                                origId2 = origId4;
                                this.mAm.startAssociationLocked(callerApp2.uid, callerApp2.processName, callerApp2.curProcState, s.appInfo.uid, s.name, s.processName);
                                service2 = service5;
                                try {
                                    this.mAm.grantEphemeralAccessLocked(callerApp2.userId, service2, s.appInfo.uid, UserHandle.getAppId(callerApp2.uid));
                                    this.mAm.mHwAMSEx.reportServiceRelationIAware(1, s, callerApp2);
                                    AppBindRecord b2 = s.retrieveAppBindingLocked(service2, callerApp2);
                                    r0 = r0;
                                    boolean z5 = isBindExternal;
                                    b = b2;
                                    origId3 = origId2;
                                    boolean z6 = allowInstant;
                                    service3 = service2;
                                    try {
                                        ConnectionRecord connectionRecord = new ConnectionRecord(b2, activity4, connection, flags, clientLabel2, clientIntent2);
                                        c = connectionRecord;
                                        IBinder binder = connection.asBinder();
                                        clist = s.connections.get(binder);
                                        if (clist == null) {
                                            try {
                                                clist = new ArrayList<>();
                                                s.connections.put(binder, clist);
                                            } catch (Throwable th2) {
                                                th = th2;
                                                Intent intent4 = service3;
                                                ActivityRecord activityRecord2 = activity4;
                                            }
                                        }
                                        clist.add(c);
                                        b.connections.add(c);
                                        activity = activity4;
                                        if (activity != null) {
                                            try {
                                                if (activity.connections == null) {
                                                    activity.connections = new HashSet<>();
                                                }
                                                activity.connections.add(c);
                                            } catch (Throwable th3) {
                                                th = th3;
                                                ActivityRecord activityRecord3 = activity;
                                                Intent intent5 = service3;
                                                origId = origId3;
                                                Binder.restoreCallingIdentity(origId);
                                                throw th;
                                            }
                                        }
                                        b.client.connections.add(c);
                                        connectionSize = s.connections.size();
                                        if (ActivityManagerDebugConfig.HWFLOW && ((long) connectionSize) > SERVICE_CONNECTIONS_THRESHOLD) {
                                            Flog.i(102, "bindServiceLocked " + s + ",connection size= " + connectionSize + ",callingPackage= " + str3);
                                        }
                                        if ((c.flags & 8) != 0) {
                                            b.client.hasAboveClient = true;
                                        }
                                        if ((c.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0) {
                                            s.whitelistManager = true;
                                        }
                                        if (s.app != null) {
                                            updateServiceClientActivitiesLocked(s.app, c, true);
                                        }
                                        clist2 = this.mServiceConnections.get(binder);
                                        if (clist2 == null) {
                                            clist2 = new ArrayList<>();
                                            this.mServiceConnections.put(binder, clist2);
                                        }
                                        ArrayList<ConnectionRecord> clist3 = clist2;
                                        clist3.add(c);
                                        if ((flags & 1) == 0) {
                                            try {
                                                this.mAm.mHwAMSEx.setHbsMiniAppUid(s.appInfo, service3);
                                                s.lastActivity = SystemClock.uptimeMillis();
                                                ArrayList<ConnectionRecord> arrayList = clist3;
                                                ServiceRecord serviceRecord2 = s;
                                                int i3 = connectionSize;
                                                int connectionSize2 = service3.getFlags();
                                                ActivityRecord activityRecord4 = activity;
                                                IBinder iBinder2 = binder;
                                                Intent intent6 = service3;
                                                c2 = c;
                                                try {
                                                    if (bringUpServiceLocked(serviceRecord2, connectionSize2, callerFg, false, permissionsReviewRequired) != null) {
                                                        Binder.restoreCallingIdentity(origId3);
                                                        return 0;
                                                    }
                                                    origId = origId3;
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    origId = origId3;
                                                    boolean z7 = callerFg;
                                                    Binder.restoreCallingIdentity(origId);
                                                    throw th;
                                                }
                                            } catch (Throwable th5) {
                                                th = th5;
                                                ActivityRecord activityRecord5 = activity;
                                                Intent intent7 = service3;
                                                origId = origId3;
                                                boolean z8 = callerFg;
                                                Binder.restoreCallingIdentity(origId);
                                                throw th;
                                            }
                                        } else {
                                            ArrayList<ConnectionRecord> arrayList2 = clist3;
                                            int i4 = connectionSize;
                                            ActivityRecord activityRecord6 = activity;
                                            IBinder iBinder3 = binder;
                                            Intent intent8 = service3;
                                            origId = origId3;
                                            c2 = c;
                                        }
                                        try {
                                            if (s.app != null) {
                                                if ((flags & 134217728) != 0) {
                                                    try {
                                                        s.app.treatLikeActivity = true;
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                        Binder.restoreCallingIdentity(origId);
                                                        throw th;
                                                    }
                                                }
                                                if (s.whitelistManager) {
                                                    s.app.whitelistManager = true;
                                                }
                                                ActivityManagerService activityManagerService = this.mAm;
                                                ProcessRecord processRecord = s.app;
                                                if (!s.app.hasClientActivities) {
                                                    if (!s.app.treatLikeActivity) {
                                                        z = false;
                                                        activityManagerService.updateLruProcessLocked(processRecord, z, b.client);
                                                        this.mAm.updateOomAdjLocked(s.app, true);
                                                    }
                                                }
                                                z = true;
                                                activityManagerService.updateLruProcessLocked(processRecord, z, b.client);
                                                this.mAm.updateOomAdjLocked(s.app, true);
                                            }
                                            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                                Slog.v(ActivityManagerService.TAG, "Bind " + s + " with " + b + ": received=" + b.intent.received + " apps=" + b.intent.apps.size() + " doRebind=" + b.intent.doRebind);
                                            }
                                            if (s.app != null) {
                                                try {
                                                    if (b.intent.received) {
                                                        c2.conn.connected(s.name, b.intent.binder, false);
                                                        if (b.intent.apps.size() != 1 || !b.intent.doRebind) {
                                                            callerFg2 = callerFg;
                                                            getServiceMapLocked(s.userId).ensureNotStartingBackgroundLocked(s);
                                                            Binder.restoreCallingIdentity(origId);
                                                            app = s.app;
                                                            if (app == null && s.processName != null) {
                                                                app = this.mAm.getProcessRecordLocked(s.processName, s.appInfo.uid, true);
                                                            }
                                                            if (app != null || app.uid < 10000 || callerApp2.pid == app.pid || callerApp2.info == null || app.info == null || callerApp2.info.packageName == null || callerApp2.info.packageName.equals(app.info.packageName)) {
                                                            } else {
                                                                boolean z9 = callerFg2;
                                                                LogPower.push(148, "bindservice", s.packageName, Integer.toString(app.pid), new String[]{str3});
                                                                LogPower.push(166, s.processName, Integer.toString(callerApp2.pid), Integer.toString(app.pid), new String[]{"service"});
                                                            }
                                                            return 1;
                                                        }
                                                        callerFg2 = callerFg;
                                                        try {
                                                            requestServiceBindingLocked(s, b.intent, callerFg2, true);
                                                            getServiceMapLocked(s.userId).ensureNotStartingBackgroundLocked(s);
                                                            Binder.restoreCallingIdentity(origId);
                                                            app = s.app;
                                                            app = this.mAm.getProcessRecordLocked(s.processName, s.appInfo.uid, true);
                                                            if (app != null) {
                                                            }
                                                            return 1;
                                                        } catch (Throwable th7) {
                                                            th = th7;
                                                            boolean z10 = callerFg2;
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    Slog.w(ActivityManagerService.TAG, "Failure sending service " + s.shortName + " to connection " + c2.conn.asBinder() + " (in " + c2.binding.client.processName + ")", e);
                                                } catch (Throwable th8) {
                                                    th = th8;
                                                    boolean z11 = callerFg;
                                                    Binder.restoreCallingIdentity(origId);
                                                    throw th;
                                                }
                                            }
                                            callerFg2 = callerFg;
                                        } catch (Throwable th9) {
                                            th = th9;
                                            boolean z12 = callerFg;
                                            Binder.restoreCallingIdentity(origId);
                                            throw th;
                                        }
                                    } catch (Throwable th10) {
                                        th = th10;
                                        Intent intent9 = service3;
                                        ActivityRecord activityRecord7 = activity4;
                                        boolean z13 = callerFg;
                                        origId = origId3;
                                        Binder.restoreCallingIdentity(origId);
                                        throw th;
                                    }
                                    try {
                                        if (!b.intent.requested) {
                                            requestServiceBindingLocked(s, b.intent, callerFg2, false);
                                        }
                                        getServiceMapLocked(s.userId).ensureNotStartingBackgroundLocked(s);
                                        Binder.restoreCallingIdentity(origId);
                                        app = s.app;
                                        app = this.mAm.getProcessRecordLocked(s.processName, s.appInfo.uid, true);
                                        if (app != null) {
                                        }
                                        return 1;
                                    } catch (Throwable th11) {
                                        th = th11;
                                        boolean z14 = callerFg2;
                                        Binder.restoreCallingIdentity(origId);
                                        throw th;
                                    }
                                } catch (Throwable th12) {
                                    th = th12;
                                    Intent intent10 = service2;
                                    boolean z15 = isBindExternal;
                                    boolean z16 = allowInstant;
                                    ActivityRecord activityRecord8 = activity4;
                                    boolean z17 = callerFg;
                                    origId = origId2;
                                    Binder.restoreCallingIdentity(origId);
                                    throw th;
                                }
                            }
                        }
                    }
                    origId2 = origId4;
                    try {
                        this.mAm.startAssociationLocked(callerApp2.uid, callerApp2.processName, callerApp2.curProcState, s.appInfo.uid, s.name, s.processName);
                        service2 = service5;
                        this.mAm.grantEphemeralAccessLocked(callerApp2.userId, service2, s.appInfo.uid, UserHandle.getAppId(callerApp2.uid));
                        this.mAm.mHwAMSEx.reportServiceRelationIAware(1, s, callerApp2);
                        AppBindRecord b22 = s.retrieveAppBindingLocked(service2, callerApp2);
                        connectionRecord = connectionRecord;
                        boolean z52 = isBindExternal;
                        b = b22;
                        origId3 = origId2;
                        boolean z62 = allowInstant;
                        service3 = service2;
                        ConnectionRecord connectionRecord2 = new ConnectionRecord(b22, activity4, connection, flags, clientLabel2, clientIntent2);
                        c = connectionRecord2;
                        IBinder binder2 = connection.asBinder();
                        clist = s.connections.get(binder2);
                        if (clist == null) {
                        }
                        clist.add(c);
                        b.connections.add(c);
                        activity = activity4;
                        if (activity != null) {
                        }
                    } catch (Throwable th13) {
                        th = th13;
                        boolean z18 = isBindExternal;
                        boolean z19 = allowInstant;
                        Intent intent11 = service5;
                        ActivityRecord activityRecord9 = activity4;
                        boolean z20 = callerFg;
                        origId = origId2;
                        Binder.restoreCallingIdentity(origId);
                        throw th;
                    }
                    try {
                        b.client.connections.add(c);
                        connectionSize = s.connections.size();
                        Flog.i(102, "bindServiceLocked " + s + ",connection size= " + connectionSize + ",callingPackage= " + str3);
                        if ((c.flags & 8) != 0) {
                        }
                        if ((c.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0) {
                        }
                        if (s.app != null) {
                        }
                        clist2 = this.mServiceConnections.get(binder2);
                        if (clist2 == null) {
                        }
                        ArrayList<ConnectionRecord> clist32 = clist2;
                        clist32.add(c);
                        if ((flags & 1) == 0) {
                        }
                        if (s.app != null) {
                        }
                        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        }
                        if (s.app != null) {
                        }
                        callerFg2 = callerFg;
                        if (!b.intent.requested) {
                        }
                        getServiceMapLocked(s.userId).ensureNotStartingBackgroundLocked(s);
                        Binder.restoreCallingIdentity(origId);
                        app = s.app;
                        app = this.mAm.getProcessRecordLocked(s.processName, s.appInfo.uid, true);
                        if (app != null) {
                        }
                        return 1;
                    } catch (Throwable th14) {
                        th = th14;
                        ActivityRecord activityRecord10 = activity;
                        Intent intent12 = service3;
                        boolean z21 = callerFg;
                        origId = origId3;
                        Binder.restoreCallingIdentity(origId);
                        throw th;
                    }
                } catch (Throwable th15) {
                    th = th15;
                    origId = origId4;
                    boolean z22 = isBindExternal;
                    boolean z23 = allowInstant;
                    Intent intent13 = service5;
                    ActivityRecord activityRecord11 = activity4;
                    boolean z24 = callerFg;
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
            } else {
                throw new SecurityException("Non-system caller " + iApplicationThread + " (pid=" + Binder.getCallingPid() + ") set BIND_ALLOW_INSTANT when binding service " + service4);
            }
        } else {
            int i5 = userId;
            throw new SecurityException("Unable to find app for caller " + iApplicationThread + " (pid=" + Binder.getCallingPid() + ") when binding service " + intent);
        }
    }

    /* access modifiers changed from: package-private */
    public void publishServiceLocked(ServiceRecord r, Intent intent, IBinder service) {
        ConnectionRecord c;
        IntentBindRecord b;
        Intent.FilterComparison filter;
        ServiceRecord serviceRecord = r;
        Intent intent2 = intent;
        IBinder iBinder = service;
        long origId = Binder.clearCallingIdentity();
        try {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(ActivityManagerService.TAG, "PUBLISHING " + serviceRecord + " " + intent2 + ": " + iBinder);
            }
            if (serviceRecord != null) {
                Intent.FilterComparison filter2 = new Intent.FilterComparison(intent2);
                IntentBindRecord b2 = serviceRecord.bindings.get(filter2);
                if (b2 == null || b2.received) {
                    IntentBindRecord intentBindRecord = b2;
                } else {
                    b2.binder = iBinder;
                    b2.requested = true;
                    b2.received = true;
                    int connectionSize = serviceRecord.connections.size();
                    if (ActivityManagerDebugConfig.HWFLOW && ((long) connectionSize) > SERVICE_CONNECTIONS_THRESHOLD) {
                        Flog.i(102, "publishServiceLocked " + serviceRecord + ",connection size= " + connectionSize);
                    }
                    long start = SystemClock.uptimeMillis();
                    int conni = connectionSize - 1;
                    while (true) {
                        int conni2 = conni;
                        if (conni2 < 0) {
                            break;
                        }
                        ArrayList valueAt = serviceRecord.connections.valueAt(conni2);
                        int i = 0;
                        while (true) {
                            int i2 = i;
                            if (i2 >= valueAt.size()) {
                                break;
                            }
                            c = (ConnectionRecord) valueAt.get(i2);
                            if (!filter2.equals(c.binding.intent.intent)) {
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    filter = filter2;
                                    StringBuilder sb = new StringBuilder();
                                    b = b2;
                                    sb.append("Not publishing to: ");
                                    sb.append(c);
                                    Slog.v(ActivityManagerService.TAG, sb.toString());
                                } else {
                                    filter = filter2;
                                    b = b2;
                                }
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(ActivityManagerService.TAG, "Bound intent: " + c.binding.intent.intent);
                                }
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(ActivityManagerService.TAG, "Published intent: " + intent2);
                                }
                            } else {
                                filter = filter2;
                                b = b2;
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(ActivityManagerService.TAG, "Publishing to: " + c);
                                }
                                c.conn.connected(serviceRecord.name, iBinder, false);
                            }
                            i = i2 + 1;
                            filter2 = filter;
                            b2 = b;
                            intent2 = intent;
                        }
                        IntentBindRecord intentBindRecord2 = b2;
                        conni = conni2 - 1;
                        intent2 = intent;
                    }
                    IntentBindRecord intentBindRecord3 = b2;
                    long end = SystemClock.uptimeMillis();
                    if (ActivityManagerDebugConfig.HWFLOW && end - start > 1000) {
                        Flog.i(102, "publishServiceLocked " + serviceRecord + ",took " + (end - start) + "ms");
                    }
                }
                serviceDoneExecutingLocked(serviceRecord, this.mDestroyingServices.contains(serviceRecord), false);
            }
            Binder.restoreCallingIdentity(origId);
        } catch (Exception e) {
            Slog.w(ActivityManagerService.TAG, "Failure sending service " + serviceRecord.name + " to connection " + c.conn.asBinder() + " (in " + c.binding.client.processName + ")", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean unbindServiceLocked(IServiceConnection connection) {
        IBinder binder = connection.asBinder();
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "unbindService: conn=" + binder);
        }
        ArrayList<ConnectionRecord> clist = this.mServiceConnections.get(binder);
        if (clist == null) {
            Slog.w(ActivityManagerService.TAG, "Unbind failed: could not find connection for " + connection.asBinder());
            return false;
        }
        long origId = Binder.clearCallingIdentity();
        while (true) {
            try {
                boolean z = true;
                if (clist.size() > 0) {
                    ConnectionRecord r = clist.get(0);
                    removeConnectionLocked(r, null, null);
                    if (clist.size() > 0 && clist.get(0) == r) {
                        Slog.wtf(ActivityManagerService.TAG, "Connection " + r + " not removed for binder " + binder);
                        clist.remove(0);
                    }
                    if (r.binding.service.app != null) {
                        if (r.binding.service.app.whitelistManager) {
                            updateWhitelistManagerLocked(r.binding.service.app);
                        }
                        if ((r.flags & 134217728) != 0) {
                            r.binding.service.app.treatLikeActivity = true;
                            ActivityManagerService activityManagerService = this.mAm;
                            ProcessRecord processRecord = r.binding.service.app;
                            if (!r.binding.service.app.hasClientActivities) {
                                if (!r.binding.service.app.treatLikeActivity) {
                                    z = false;
                                }
                            }
                            activityManagerService.updateLruProcessLocked(processRecord, z, null);
                        }
                        this.mAm.updateOomAdjLocked(r.binding.service.app, false);
                    }
                } else {
                    this.mAm.updateOomAdjLocked();
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unbindFinishedLocked(ServiceRecord r, Intent intent, boolean doRebind) {
        long origId = Binder.clearCallingIdentity();
        if (r != null) {
            try {
                IntentBindRecord b = r.bindings.get(new Intent.FilterComparison(intent));
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("unbindFinished in ");
                    sb.append(r);
                    sb.append(" at ");
                    sb.append(b);
                    sb.append(": apps=");
                    sb.append(b != null ? b.apps.size() : 0);
                    Slog.v(ActivityManagerService.TAG, sb.toString());
                }
                boolean inDestroying = this.mDestroyingServices.contains(r);
                if (b != null) {
                    if (b.apps.size() <= 0 || inDestroying) {
                        b.doRebind = true;
                    } else {
                        boolean inFg = false;
                        int i = b.apps.size() - 1;
                        while (true) {
                            if (i >= 0) {
                                ProcessRecord client = b.apps.valueAt(i).client;
                                if (client != null && client.setSchedGroup != 0) {
                                    inFg = true;
                                    break;
                                }
                                i--;
                            }
                        }
                        try {
                            requestServiceBindingLocked(r, b, inFg, true);
                            break;
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
                serviceDoneExecutingLocked(r, inDestroying, false);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    private final ServiceRecord findServiceLocked(ComponentName name, IBinder token, int userId) {
        ServiceRecord r = getServiceByNameLocked(name, userId);
        if (r == token) {
            return r;
        }
        return null;
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:114:?, code lost:
        r19 = new com.android.server.am.ServiceRecord(r1.mAm, r21, r8, r11, r9, r37, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x02be, code lost:
        r2 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:?, code lost:
        r13.setService(r2);
        r7.mServicesByName.put(r8, r2);
        r7.mServicesByIntent.put(r11, r2);
        r0 = r1.mPendingServices.size() - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x02d4, code lost:
        if (r0 < 0) goto L_0x0318;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x02d6, code lost:
        r3 = r1.mPendingServices.get(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x02e8, code lost:
        if (r3.serviceInfo.applicationInfo.uid != r9.applicationInfo.uid) goto L_0x0311;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x02f0, code lost:
        if (r3.name.equals(r8) == false) goto L_0x0311;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x02f4, code lost:
        if (com.android.server.am.ActivityManagerDebugConfig.DEBUG_SERVICE == false) goto L_0x030c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x02f6, code lost:
        android.util.Slog.v(com.android.server.am.ActivityManagerService.TAG, "Remove pending: " + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x030c, code lost:
        r1.mPendingServices.remove(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x0311, code lost:
        r0 = r0 - 1;
        r4 = r32;
        r5 = r34;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x031a, code lost:
        if (com.android.server.am.ActivityManagerDebugConfig.DEBUG_SERVICE == 0) goto L_0x0332;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x031c, code lost:
        android.util.Slog.v(com.android.server.am.ActivityManagerService.TAG, "Retrieve created new service: " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x0332, code lost:
        r6 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x0335, code lost:
        r8 = r7;
        r7 = r10;
        r10 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:0x054b, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x03ba  */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x051d  */
    private ServiceLookupResult retrieveServiceLocked(Intent service, String resolvedType, String callingPackage, int callingPid, int callingUid, int userId, boolean createIfNeeded, boolean callingFromFg, boolean isBindExternal, boolean allowInstant) {
        ProcessRecord callerApp;
        ServiceRecord r;
        int userId2;
        int i;
        String str;
        int i2;
        int userId3;
        ServiceInfo serviceInfo;
        ServiceMap smap;
        ServiceLookupResult serviceLookupResult;
        ServiceRecord r2;
        int i3;
        String str2;
        int userId4;
        ServiceInfo sInfo;
        ServiceMap smap2;
        int userId5;
        Intent intent = service;
        String str3 = callingPackage;
        int i4 = callingPid;
        int i5 = callingUid;
        ServiceRecord r3 = null;
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "retrieveServiceLocked: " + intent + " type=" + resolvedType + " callingUid=" + i5);
        } else {
            String str4 = resolvedType;
        }
        int userId6 = this.mAm.mUserController.handleIncomingUser(i4, i5, userId, false, 1, "service", null);
        synchronized (this.mAm.mPidsSelfLocked) {
            try {
                callerApp = this.mAm.mPidsSelfLocked.get(i4);
            } catch (Throwable th) {
                th = th;
                String str5 = str3;
                int i6 = userId6;
                int userId7 = i4;
                int i7 = i5;
                String str6 = str5;
                while (true) {
                    throw th;
                }
            }
        }
        ServiceMap smap3 = getServiceMapLocked(userId6);
        ComponentName comp = service.getComponent();
        if (comp != null) {
            r3 = smap3.mServicesByName.get(comp);
            if (ActivityManagerDebugConfig.DEBUG_SERVICE && r3 != null) {
                Slog.v(ActivityManagerService.TAG, "Retrieved by component: " + r3);
            }
        }
        if (r3 == null && !isBindExternal) {
            r3 = smap3.mServicesByIntent.get(new Intent.FilterComparison(intent));
            if (ActivityManagerDebugConfig.DEBUG_SERVICE && r3 != null) {
                Slog.v(ActivityManagerService.TAG, "Retrieved by intent: " + r3);
            }
        }
        if (!(r3 == null || (r3.serviceInfo.flags & 4) == 0 || str3.equals(r3.packageName))) {
            r3 = null;
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(ActivityManagerService.TAG, "Whoops, can't use existing external service");
            }
        }
        if (r == null) {
            int flags = 268436480;
            if (allowInstant) {
                flags = 268436480 | DumpState.DUMP_VOLUMES;
            }
            try {
                ComponentName componentName = comp;
                try {
                    ResolveInfo rInfo = this.mAm.getPackageManagerInternalLocked().resolveService(intent, resolvedType, flags, userId6, i5);
                    if (rInfo != null) {
                        try {
                            serviceInfo = rInfo.serviceInfo;
                        } catch (RemoteException e) {
                            userId3 = userId6;
                        }
                    } else {
                        serviceInfo = null;
                    }
                    ServiceInfo sInfo2 = serviceInfo;
                    if (sInfo2 != null) {
                        smap = smap3;
                        userId3 = userId6;
                        r2 = r;
                        i3 = i5;
                        int i8 = i4;
                        str2 = str3;
                        try {
                            if (this.mAm.shouldPreventStartService(sInfo2, i4, i5, callerApp, false, intent)) {
                                return null;
                            }
                            serviceLookupResult = null;
                        } catch (RemoteException e2) {
                            r = r2;
                            smap3 = smap;
                            userId2 = userId3;
                            ServiceMap serviceMap = smap3;
                            i2 = callingPid;
                            str = callingPackage;
                            i = callingUid;
                            if (r == null) {
                            }
                        }
                    } else {
                        smap = smap3;
                        userId3 = userId6;
                        r2 = r;
                        i3 = i5;
                        int i9 = i4;
                        str2 = str3;
                        serviceLookupResult = null;
                    }
                    if (sInfo2 == null) {
                        Slog.w(ActivityManagerService.TAG, "Unable to start service " + intent + " U=" + userId3 + ": not found");
                        return serviceLookupResult;
                    }
                    ComponentName name = new ComponentName(sInfo2.applicationInfo.packageName, sInfo2.name);
                    if ((sInfo2.flags & 4) != 0) {
                        if (!isBindExternal) {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE required for " + name);
                        } else if (!sInfo2.exported) {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not exported");
                        } else if ((sInfo2.flags & 2) != 0) {
                            ApplicationInfo aInfo = AppGlobals.getPackageManager().getApplicationInfo(str2, 1024, userId3);
                            if (aInfo != null) {
                                sInfo2 = new ServiceInfo(sInfo2);
                                sInfo2.applicationInfo = new ApplicationInfo(sInfo2.applicationInfo);
                                sInfo2.applicationInfo.packageName = aInfo.packageName;
                                sInfo2.applicationInfo.uid = aInfo.uid;
                                name = new ComponentName(aInfo.packageName, name.getClassName());
                                intent.setComponent(name);
                            } else {
                                throw new SecurityException("BIND_EXTERNAL_SERVICE failed, could not resolve client package " + str2);
                            }
                        } else {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an isolatedProcess");
                        }
                    } else if (isBindExternal) {
                        ResolveInfo resolveInfo = rInfo;
                        throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an externalService");
                    }
                    if (userId3 > 0) {
                        if (!this.mAm.isSingleton(sInfo2.processName, sInfo2.applicationInfo, sInfo2.name, sInfo2.flags) || !this.mAm.isValidSingletonCall(i3, sInfo2.applicationInfo.uid)) {
                            userId5 = userId3;
                        } else {
                            userId5 = 0;
                            try {
                                smap = getServiceMapLocked(0);
                            } catch (RemoteException e3) {
                                r = r2;
                                userId3 = 0;
                                smap3 = smap;
                                userId2 = userId3;
                                ServiceMap serviceMap2 = smap3;
                                i2 = callingPid;
                                str = callingPackage;
                                i = callingUid;
                                if (r == null) {
                                }
                            }
                        }
                        ServiceInfo sInfo3 = new ServiceInfo(sInfo2);
                        sInfo3.applicationInfo = this.mAm.getAppInfoForUser(sInfo3.applicationInfo, userId5);
                        userId4 = userId5;
                        smap2 = smap;
                        sInfo = sInfo3;
                    } else {
                        sInfo = sInfo2;
                        userId4 = userId3;
                        smap2 = smap;
                    }
                    try {
                        r2 = smap2.mServicesByName.get(name);
                        if (ActivityManagerDebugConfig.DEBUG_SERVICE && r2 != null) {
                            Slog.v(ActivityManagerService.TAG, "Retrieved via pm by intent: " + r2);
                        }
                        if (r2 == null && createIfNeeded) {
                            Intent.FilterComparison filter = new Intent.FilterComparison(service.cloneFilter());
                            ServiceRestarter res = new ServiceRestarter();
                            BatteryStatsImpl stats = this.mAm.mBatteryStatsService.getActiveStatistics();
                            synchronized (stats) {
                                try {
                                    ResolveInfo resolveInfo2 = rInfo;
                                    try {
                                        BatteryStatsImpl.Uid.Pkg.Serv ss = stats.getServiceStatsLocked(sInfo.applicationInfo.uid, sInfo.packageName, sInfo.name);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    ResolveInfo resolveInfo3 = rInfo;
                                    throw th;
                                }
                            }
                        }
                        ServiceMap serviceMap3 = smap2;
                        userId2 = userId4;
                        i2 = callingPid;
                        str = callingPackage;
                        i = callingUid;
                        r = r2;
                    } catch (RemoteException e4) {
                        smap3 = smap2;
                        userId3 = userId4;
                        r = r2;
                    }
                } catch (RemoteException e5) {
                    ServiceMap serviceMap4 = smap3;
                    userId3 = userId6;
                    ServiceRecord serviceRecord = r;
                    userId2 = userId3;
                    ServiceMap serviceMap22 = smap3;
                    i2 = callingPid;
                    str = callingPackage;
                    i = callingUid;
                    if (r == null) {
                    }
                }
            } catch (RemoteException e6) {
                ComponentName componentName2 = comp;
                ServiceMap serviceMap5 = smap3;
                userId3 = userId6;
                ServiceRecord serviceRecord2 = r;
                userId2 = userId3;
                ServiceMap serviceMap222 = smap3;
                i2 = callingPid;
                str = callingPackage;
                i = callingUid;
                if (r == null) {
                }
            }
        } else {
            ServiceMap serviceMap6 = smap3;
            int userId8 = userId6;
            ServiceRecord r4 = r;
            if (!createIfNeeded || r4.serviceInfo == null) {
                r = r4;
                userId2 = userId8;
                i2 = callingPid;
                str = callingPackage;
                i = callingUid;
            } else {
                i2 = callingPid;
                str = callingPackage;
                i = callingUid;
                r = r4;
                userId2 = userId8;
                if (this.mAm.shouldPreventStartService(r4.serviceInfo, i2, i, callerApp, true, intent)) {
                    return null;
                }
            }
        }
        if (r == null) {
            if (this.mAm.checkComponentPermission(r.permission, i2, i, r.appInfo.uid, r.exported) == 0) {
                if (!(r.permission == null || str == null)) {
                    int opCode = AppOpsManager.permissionToOpCode(r.permission);
                    if (!(opCode == -1 || this.mAm.mAppOpsService.noteOperation(opCode, i, str) == 0)) {
                        Slog.w(ActivityManagerService.TAG, "Appop Denial: Accessing service " + r.name + " from pid=" + i2 + ", uid=" + i + " requires appop " + AppOpsManager.opToName(opCode));
                        return null;
                    }
                }
                if (!this.mAm.mIntentFirewall.checkService(r.name, intent, i, i2, resolvedType, r.appInfo)) {
                    Flog.w(102, "prevent by firewall Unable to start service " + intent + " U=" + userId2 + ": force null");
                    return null;
                } else if (!this.mAm.mHwAMSEx.shouldPreventStartService(r.serviceInfo, i, i2, str, userId2)) {
                    return new ServiceLookupResult(r, null);
                } else {
                    Flog.w(102, "prevent by trustspace Unable to start service " + intent + " U=" + userId2 + ": force null");
                    return null;
                }
            } else if (!r.exported) {
                Slog.w(ActivityManagerService.TAG, "Permission Denial: Accessing service " + r.name + " from pid=" + i2 + ", uid=" + i + " that is not exported from uid " + r.appInfo.uid);
                return new ServiceLookupResult(null, "not exported from uid " + r.appInfo.uid);
            } else {
                Slog.w(ActivityManagerService.TAG, "Permission Denial: Accessing service " + r.name + " from pid=" + i2 + ", uid=" + i + " requires " + r.permission);
                return new ServiceLookupResult(null, r.permission);
            }
        } else {
            Flog.i(102, "retrieve service " + intent + " U=" + userId2 + ": ret null");
            return null;
        }
    }

    private final void bumpServiceExecutingLocked(ServiceRecord r, boolean fg, String why) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            StringBuilder sb = new StringBuilder();
            sb.append(">>> EXECUTING ");
            sb.append(why);
            sb.append(" of ");
            sb.append(r);
            sb.append(" in app ");
            sb.append(r.app);
            sb.append(", r.executeNesting: ");
            sb.append(r.executeNesting);
            sb.append(", fg: ");
            sb.append(fg);
            sb.append(", r.app.execServicesFg: ");
            int i = 0;
            sb.append(r.app == null ? false : r.app.execServicesFg);
            sb.append(", r.app.executingServices.size: ");
            if (r.app != null) {
                i = r.app.executingServices.size();
            }
            sb.append(i);
            Slog.v(ActivityManagerService.TAG, sb.toString());
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
            Slog.v(ActivityManagerService.TAG, ">>> EXECUTING " + why + " of " + r.shortName);
        }
        boolean timeoutNeeded = true;
        if (this.mAm.mBootPhase < 600 && r.app != null && r.app.pid == Process.myPid()) {
            Slog.w(ActivityManagerService.TAG, "Too early to start/bind service in system_server: Phase=" + this.mAm.mBootPhase + " " + r.getComponentName());
            timeoutNeeded = false;
        }
        long now = SystemClock.uptimeMillis();
        if (r.executeNesting == 0) {
            r.executeFg = fg;
            ServiceState stracker = r.getTracker();
            if (stracker != null) {
                stracker.setExecuting(true, this.mAm.mProcessStats.getMemFactorLocked(), now);
            }
            if (r.app != null) {
                r.app.executingServices.add(r);
                r.app.execServicesFg |= fg;
                if (timeoutNeeded && r.app.executingServices.size() == 1) {
                    scheduleServiceTimeoutLocked(r.app);
                }
            }
        } else if (r.app != null && fg && !r.app.execServicesFg) {
            r.app.execServicesFg = true;
            if (timeoutNeeded) {
                scheduleServiceTimeoutLocked(r.app);
            }
        }
        r.executeFg |= fg;
        r.executeNesting++;
        r.executingStart = now;
    }

    private final boolean requestServiceBindingLocked(ServiceRecord r, IntentBindRecord i, boolean execInFg, boolean rebind) throws TransactionTooLargeException {
        if (r.app == null || r.app.thread == null) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.d(ActivityManagerService.TAG, "requestBind " + i + ": requested=" + i.requested + " rebind=" + rebind);
        }
        if ((!i.requested || rebind) && i.apps.size() > 0) {
            try {
                bumpServiceExecutingLocked(r, execInFg, "bind");
                r.app.forceProcessStateUpTo(9);
                r.app.thread.scheduleBindService(r, i.intent.getIntent(), rebind, r.app.repProcState);
                if (!rebind) {
                    i.requested = true;
                }
                i.hasBound = true;
                i.doRebind = false;
            } catch (TransactionTooLargeException e) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Crashed while binding " + r, e);
                }
                boolean inDestroying = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                throw e;
            } catch (RemoteException e2) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Crashed while binding " + r);
                }
                boolean inDestroying2 = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying2, inDestroying2);
                return false;
            }
        }
        return true;
    }

    private final boolean scheduleServiceRestartLocked(ServiceRecord r, boolean allowCancel) {
        boolean z;
        boolean canceled;
        long minDuration;
        ServiceMap smap;
        boolean canceled2;
        ServiceRecord serviceRecord = r;
        boolean canceled3 = false;
        if (this.mAm.isShuttingDownLocked()) {
            Slog.w(ActivityManagerService.TAG, "Not scheduling restart of crashed service " + serviceRecord.shortName + " - system is shutting down");
            return false;
        }
        ServiceMap smap2 = getServiceMapLocked(serviceRecord.userId);
        if (smap2.mServicesByName.get(serviceRecord.name) != serviceRecord) {
            Slog.wtf(ActivityManagerService.TAG, "Attempting to schedule restart of " + serviceRecord + " when found in map: " + smap2.mServicesByName.get(serviceRecord.name));
            return false;
        }
        long now = SystemClock.uptimeMillis();
        int i = 3;
        if ((serviceRecord.serviceInfo.applicationInfo.flags & 8) == 0) {
            long minDuration2 = this.mAm.mConstants.SERVICE_RESTART_DURATION;
            long resetTime = this.mAm.mConstants.SERVICE_RESET_RUN_DURATION;
            int N = serviceRecord.deliveredStarts.size();
            if (N > 0) {
                int i2 = N - 1;
                while (true) {
                    int i3 = i2;
                    if (i3 < 0) {
                        break;
                    }
                    ServiceRecord.StartItem si = serviceRecord.deliveredStarts.get(i3);
                    si.removeUriPermissionsLocked();
                    if (si.intent != null) {
                        if (!allowCancel) {
                            canceled2 = canceled3;
                        } else if (si.deliveryCount >= i || si.doneExecutingCount >= 6) {
                            StringBuilder sb = new StringBuilder();
                            boolean z2 = canceled3;
                            sb.append("Canceling start item ");
                            sb.append(si.intent);
                            sb.append(" in service ");
                            sb.append(serviceRecord.name);
                            Slog.w(ActivityManagerService.TAG, sb.toString());
                            canceled3 = true;
                        } else {
                            canceled2 = canceled3;
                        }
                        serviceRecord.pendingStarts.add(0, si);
                        smap = smap2;
                        long dur = (SystemClock.uptimeMillis() - si.deliveredTime) * 2;
                        if (minDuration2 < dur) {
                            minDuration2 = dur;
                        }
                        if (resetTime < dur) {
                            resetTime = dur;
                        }
                        canceled3 = canceled2;
                        i2 = i3 - 1;
                        smap2 = smap;
                        i = 3;
                    }
                    smap = smap2;
                    i2 = i3 - 1;
                    smap2 = smap;
                    i = 3;
                }
                canceled = canceled3;
                ServiceMap serviceMap = smap2;
                serviceRecord.deliveredStarts.clear();
            } else {
                canceled = false;
            }
            serviceRecord.totalRestartCount++;
            if (serviceRecord.restartDelay == 0) {
                serviceRecord.restartCount++;
                serviceRecord.restartDelay = minDuration2;
            } else if (serviceRecord.crashCount > 1) {
                serviceRecord.restartDelay = this.mAm.mConstants.BOUND_SERVICE_CRASH_RESTART_DURATION * ((long) (serviceRecord.crashCount - 1));
            } else if (now > serviceRecord.restartTime + resetTime) {
                serviceRecord.restartCount = 1;
                serviceRecord.restartDelay = minDuration2;
            } else {
                serviceRecord.restartDelay *= (long) this.mAm.mConstants.SERVICE_RESTART_DURATION_FACTOR;
                if (serviceRecord.restartDelay < minDuration2) {
                    serviceRecord.restartDelay = minDuration2;
                }
            }
            serviceRecord.nextRestartTime = serviceRecord.restartDelay + now;
            while (true) {
                boolean repeat = false;
                long restartTimeBetween = this.mAm.mConstants.SERVICE_MIN_RESTART_TIME_BETWEEN;
                int i4 = this.mRestartingServices.size() - 1;
                while (true) {
                    if (i4 < 0) {
                        minDuration = minDuration2;
                        break;
                    }
                    ServiceRecord r2 = this.mRestartingServices.get(i4);
                    if (r2 != serviceRecord) {
                        minDuration = minDuration2;
                        if (serviceRecord.nextRestartTime >= r2.nextRestartTime - restartTimeBetween && serviceRecord.nextRestartTime < r2.nextRestartTime + restartTimeBetween) {
                            serviceRecord.nextRestartTime = r2.nextRestartTime + restartTimeBetween;
                            serviceRecord.restartDelay = serviceRecord.nextRestartTime - now;
                            repeat = true;
                            break;
                        }
                    } else {
                        minDuration = minDuration2;
                    }
                    i4--;
                    minDuration2 = minDuration;
                }
                if (!repeat) {
                    break;
                }
                minDuration2 = minDuration;
            }
            canceled3 = canceled;
            z = false;
        } else {
            serviceRecord.totalRestartCount++;
            z = false;
            serviceRecord.restartCount = 0;
            serviceRecord.restartDelay = 0;
            serviceRecord.nextRestartTime = now;
        }
        if (!this.mRestartingServices.contains(serviceRecord)) {
            serviceRecord.createdFromFg = z;
            this.mRestartingServices.add(serviceRecord);
            serviceRecord.makeRestarting(this.mAm.mProcessStats.getMemFactorLocked(), now);
        }
        cancelForegroundNotificationLocked(r);
        this.mAm.mHandler.removeCallbacks(serviceRecord.restarter);
        this.mAm.mHandler.postAtTime(serviceRecord.restarter, serviceRecord.nextRestartTime);
        serviceRecord.nextRestartTime = SystemClock.uptimeMillis() + serviceRecord.restartDelay;
        Slog.w(ActivityManagerService.TAG, "Scheduling restart of crashed service " + serviceRecord.shortName + " in " + serviceRecord.restartDelay + "ms");
        EventLog.writeEvent(EventLogTags.AM_SCHEDULE_SERVICE_RESTART, new Object[]{Integer.valueOf(serviceRecord.userId), serviceRecord.shortName, Long.valueOf(serviceRecord.restartDelay)});
        return canceled3;
    }

    /* access modifiers changed from: package-private */
    public final void performServiceRestartLocked(ServiceRecord r) {
        if (!this.mRestartingServices.contains(r)) {
            Flog.i(102, "no need to performServiceRestart for r:" + r);
        } else if (!isServiceNeededLocked(r, false, false)) {
            Slog.wtf(ActivityManagerService.TAG, "Restarting service that is not needed: " + r);
        } else {
            try {
                bringUpServiceLocked(r, r.intent.getIntent().getFlags(), r.createdFromFg, true, false);
            } catch (TransactionTooLargeException e) {
                Flog.w(102, "performServiceRestart TransactionTooLarge e:", e);
            }
        }
    }

    private final boolean unscheduleServiceRestartLocked(ServiceRecord r, int callingUid, boolean force) {
        if (!force && r.restartDelay == 0) {
            return false;
        }
        boolean removed = this.mRestartingServices.remove(r);
        if (removed || callingUid != r.appInfo.uid) {
            r.resetRestartCounter();
        }
        if (removed) {
            clearRestartingIfNeededLocked(r);
        }
        this.mAm.mHandler.removeCallbacks(r.restarter);
        return true;
    }

    private void clearRestartingIfNeededLocked(ServiceRecord r) {
        if (r.restartTracker != null) {
            boolean stillTracking = false;
            int i = this.mRestartingServices.size() - 1;
            while (true) {
                if (i < 0) {
                    break;
                } else if (this.mRestartingServices.get(i).restartTracker == r.restartTracker) {
                    stillTracking = true;
                    break;
                } else {
                    i--;
                }
            }
            if (!stillTracking) {
                r.restartTracker.setRestarting(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                r.restartTracker = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0204  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0293  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x029a  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x02d6  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x02df  */
    public String bringUpServiceLocked(ServiceRecord r, int intentFlags, boolean execInFg, boolean whileRestarting, boolean permissionsReviewRequired) throws TransactionTooLargeException {
        String hostingType;
        ProcessRecord app;
        ProcessRecord app2;
        ServiceRecord serviceRecord = r;
        boolean z = execInFg;
        if (serviceRecord.app == null || serviceRecord.app.thread == null) {
            if (whileRestarting || !this.mRestartingServices.contains(serviceRecord)) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Bringing up " + serviceRecord + " " + serviceRecord.intent + " fg=" + serviceRecord.fgRequired);
                }
                if (this.mRestartingServices.remove(serviceRecord)) {
                    if (HW_PARENT_CONTROL.equals(serviceRecord.packageName)) {
                        Slog.i(ActivityManagerService.TAG, "parentcontrol reset restart counter");
                        r.resetRestartCounter();
                    }
                    clearRestartingIfNeededLocked(r);
                }
                if (serviceRecord.delayed) {
                    if (DEBUG_DELAYED_STARTS) {
                        Slog.v(ActivityManagerService.TAG, "REM FR DELAY LIST (bring up): " + serviceRecord);
                    }
                    getServiceMapLocked(serviceRecord.userId).mDelayedStartList.remove(serviceRecord);
                    serviceRecord.delayed = false;
                }
                if (!this.mAm.mUserController.hasStartedUserState(serviceRecord.userId)) {
                    String msg = "Unable to launch app " + serviceRecord.appInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + serviceRecord.appInfo.uid + " for service " + serviceRecord.intent.getIntent() + ": user " + serviceRecord.userId + " is stopped";
                    Slog.w(ActivityManagerService.TAG, msg);
                    bringDownServiceLocked(r);
                    return msg;
                }
                try {
                    AppGlobals.getPackageManager().setPackageStoppedState(serviceRecord.packageName, false, serviceRecord.userId);
                } catch (RemoteException e) {
                } catch (IllegalArgumentException e2) {
                    Slog.w(ActivityManagerService.TAG, "Failed trying to unstop package " + serviceRecord.packageName + ": " + e2);
                }
                boolean isolated = (serviceRecord.serviceInfo.flags & 2) != 0;
                String procName = serviceRecord.processName;
                if (!isolated) {
                    app = this.mAm.getProcessRecordLocked(procName, serviceRecord.appInfo.uid, false);
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("bringUpServiceLocked: appInfo.uid=");
                        sb.append(serviceRecord.appInfo.uid);
                        sb.append(" app=");
                        sb.append(app);
                        sb.append(" app.thread=");
                        sb.append(app != null ? app.thread : null);
                        Slog.v(TAG_MU, sb.toString());
                    }
                    if (!(app == null || app.thread == null)) {
                        try {
                            app.addPackage(serviceRecord.appInfo.packageName, serviceRecord.appInfo.longVersionCode, this.mAm.mProcessStats);
                            realStartServiceLocked(serviceRecord, app, z);
                            return null;
                        } catch (TransactionTooLargeException e3) {
                            throw e3;
                        } catch (RemoteException e4) {
                            Slog.w(ActivityManagerService.TAG, "Exception when starting service " + serviceRecord.shortName, e4);
                        }
                    }
                } else {
                    app = serviceRecord.isolatedProc;
                    if (WebViewZygote.isMultiprocessEnabled() && serviceRecord.serviceInfo.packageName.equals(WebViewZygote.getPackageName())) {
                        hostingType = "webview_service";
                        app2 = app;
                        if (app2 == null && !this.mAm.mHwAMSEx.needCheckProcDied(app2)) {
                            String str = procName;
                        } else if (permissionsReviewRequired) {
                            if (whileRestarting) {
                                this.mAm.shouldPreventRestartService(serviceRecord.serviceInfo, true);
                            }
                            String str2 = procName;
                            ProcessRecord startProcessLocked = this.mAm.startProcessLocked(procName, serviceRecord.appInfo, true, intentFlags, hostingType, serviceRecord.name, false, isolated, 0, false, null, null, initPCEntryArgs(r), null);
                            ProcessRecord app3 = startProcessLocked;
                            if (startProcessLocked == null) {
                                String msg2 = "Unable to launch app " + serviceRecord.appInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + serviceRecord.appInfo.uid + " for service " + serviceRecord.intent.getIntent() + ": process is bad";
                                Slog.w(ActivityManagerService.TAG, msg2);
                                try {
                                    bringDownServiceLocked(r);
                                } catch (IllegalStateException e5) {
                                    IllegalStateException illegalStateException = e5;
                                    Slog.w(ActivityManagerService.TAG, "Exception when bring down Service " + serviceRecord.shortName, e5);
                                }
                                return msg2;
                            }
                            if (isolated) {
                                serviceRecord.isolatedProc = app3;
                            }
                            if (serviceRecord.fgRequired) {
                                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                                    Slog.v(ActivityManagerService.TAG, "Whitelisting " + UserHandle.formatUid(serviceRecord.appInfo.uid) + " for fg-service launch");
                                }
                                this.mAm.tempWhitelistUidLocked(serviceRecord.appInfo.uid, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY, "fg-service-launch");
                            }
                            if (!this.mPendingServices.contains(serviceRecord)) {
                                this.mPendingServices.add(serviceRecord);
                            }
                            if (serviceRecord.delayedStop) {
                                serviceRecord.delayedStop = false;
                                if (serviceRecord.startRequested) {
                                    if (DEBUG_DELAYED_STARTS) {
                                        Slog.v(ActivityManagerService.TAG, "Applying delayed stop (in bring up): " + serviceRecord);
                                    }
                                    stopServiceLocked(r);
                                }
                            }
                            return null;
                        }
                        ProcessRecord processRecord = app2;
                        if (serviceRecord.fgRequired) {
                        }
                        if (!this.mPendingServices.contains(serviceRecord)) {
                        }
                        if (serviceRecord.delayedStop) {
                        }
                        return null;
                    }
                }
                hostingType = "service";
                app2 = app;
                if (app2 == null) {
                }
                if (permissionsReviewRequired) {
                }
            } else {
                Flog.i(102, "do nothing when waiting for a restart and Bringing up " + serviceRecord + " " + serviceRecord.intent);
                return null;
            }
        } else if (this.mAm.mHwAMSEx.needCheckProcDied(serviceRecord.app)) {
            return null;
        } else {
            sendServiceArgsLocked(serviceRecord, z, false);
            return null;
        }
    }

    private final void requestServiceBindingsLocked(ServiceRecord r, boolean execInFg) throws TransactionTooLargeException {
        int i = r.bindings.size() - 1;
        while (i >= 0 && requestServiceBindingLocked(r, r.bindings.valueAt(i), execInFg, false)) {
            i--;
        }
    }

    private final void realStartServiceLocked(ServiceRecord r, ProcessRecord app, boolean execInFg) throws RemoteException {
        boolean z;
        ServiceRecord serviceRecord = r;
        ProcessRecord processRecord = app;
        boolean z2 = execInFg;
        if (processRecord.thread != null) {
            if (ActivityManagerDebugConfig.DEBUG_MU) {
                Slog.v(TAG_MU, "realStartServiceLocked, ServiceRecord.uid = " + serviceRecord.appInfo.uid + ", ProcessRecord.uid = " + processRecord.uid);
            }
            serviceRecord.app = processRecord;
            long uptimeMillis = SystemClock.uptimeMillis();
            serviceRecord.lastActivity = uptimeMillis;
            serviceRecord.restartTime = uptimeMillis;
            boolean newService = processRecord.services.add(serviceRecord);
            bumpServiceExecutingLocked(serviceRecord, z2, "create");
            this.mAm.updateLruProcessLocked(processRecord, false, null);
            updateServiceForegroundLocked(serviceRecord.app, false);
            this.mAm.updateOomAdjLocked();
            try {
                int lastPeriod = serviceRecord.shortName.lastIndexOf(46);
                EventLogTags.writeAmCreateService(serviceRecord.userId, System.identityHashCode(r), lastPeriod >= 0 ? serviceRecord.shortName.substring(lastPeriod) : serviceRecord.shortName, serviceRecord.app.uid, serviceRecord.app.pid);
                synchronized (serviceRecord.stats.getBatteryStats()) {
                    serviceRecord.stats.startLaunchedLocked();
                }
                this.mAm.notifyPackageUse(serviceRecord.serviceInfo.packageName, 1);
                processRecord.forceProcessStateUpTo(9);
                processRecord.thread.scheduleCreateService(serviceRecord, serviceRecord.serviceInfo, this.mAm.compatibilityInfoForPackageLocked(serviceRecord.serviceInfo.applicationInfo), processRecord.repProcState);
                r.postNotification();
                if (1 == 0) {
                    boolean inDestroying = this.mDestroyingServices.contains(serviceRecord);
                    serviceDoneExecutingLocked(serviceRecord, inDestroying, inDestroying);
                    if (newService) {
                        processRecord.services.remove(serviceRecord);
                        serviceRecord.app = null;
                    }
                    if (!inDestroying) {
                        scheduleServiceRestartLocked(serviceRecord, false);
                    } else {
                        Flog.w(102, "Destroying no retry when creating service " + serviceRecord);
                    }
                }
                if (serviceRecord.whitelistManager) {
                    processRecord.whitelistManager = true;
                }
                requestServiceBindingsLocked(serviceRecord, z2);
                updateServiceClientActivitiesLocked(processRecord, null, true);
                if (serviceRecord.startRequested && serviceRecord.callStart && serviceRecord.pendingStarts.size() == 0) {
                    ArrayList<ServiceRecord.StartItem> arrayList = serviceRecord.pendingStarts;
                    ServiceRecord.StartItem startItem = r2;
                    ServiceRecord.StartItem startItem2 = new ServiceRecord.StartItem(serviceRecord, false, r.makeNextStartId(), null, null, 0);
                    arrayList.add(startItem);
                }
                sendServiceArgsLocked(serviceRecord, z2, true);
                if (serviceRecord.delayed) {
                    if (DEBUG_DELAYED_STARTS) {
                        Slog.v(ActivityManagerService.TAG, "REM FR DELAY LIST (new proc): " + serviceRecord);
                    }
                    getServiceMapLocked(serviceRecord.userId).mDelayedStartList.remove(serviceRecord);
                    z = false;
                    serviceRecord.delayed = false;
                } else {
                    z = false;
                }
                if (serviceRecord.delayedStop) {
                    serviceRecord.delayedStop = z;
                    if (serviceRecord.startRequested) {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(ActivityManagerService.TAG, "Applying delayed stop (from start): " + serviceRecord);
                        }
                        stopServiceLocked(r);
                    }
                }
            } catch (DeadObjectException e) {
                try {
                    Slog.w(ActivityManagerService.TAG, "Application dead when creating service " + serviceRecord);
                    this.mAm.appDiedLocked(processRecord);
                    throw e;
                } catch (Throwable th) {
                    if (0 == 0) {
                        boolean inDestroying2 = this.mDestroyingServices.contains(serviceRecord);
                        serviceDoneExecutingLocked(serviceRecord, inDestroying2, inDestroying2);
                        if (newService) {
                            processRecord.services.remove(serviceRecord);
                            serviceRecord.app = null;
                        }
                        if (!inDestroying2) {
                            scheduleServiceRestartLocked(serviceRecord, false);
                        } else {
                            Flog.w(102, "Destroying no retry when creating service " + serviceRecord);
                        }
                    }
                    throw th;
                }
            }
        } else {
            throw new RemoteException();
        }
    }

    private final void sendServiceArgsLocked(ServiceRecord r, boolean execInFg, boolean oomAdjusted) throws TransactionTooLargeException {
        int i;
        int N = r.pendingStarts.size();
        if (N != 0) {
            ArrayList<ServiceStartArgs> args = new ArrayList<>();
            while (true) {
                if (r.pendingStarts.size() <= 0) {
                    break;
                }
                ServiceRecord.StartItem si = r.pendingStarts.remove(0);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Sending arguments to: " + r + " " + r.intent + " args=" + si.intent);
                }
                if (si.intent != null || N <= 1) {
                    si.deliveredTime = SystemClock.uptimeMillis();
                    r.deliveredStarts.add(si);
                    si.deliveryCount++;
                    if (si.neededGrants != null) {
                        this.mAm.grantUriPermissionUncheckedFromIntentLocked(si.neededGrants, si.getUriPermissionsLocked());
                    }
                    this.mAm.grantEphemeralAccessLocked(r.userId, si.intent, r.appInfo.uid, UserHandle.getAppId(si.callingId));
                    bumpServiceExecutingLocked(r, execInFg, "start");
                    if (!oomAdjusted) {
                        oomAdjusted = true;
                        this.mAm.updateOomAdjLocked(r.app, true);
                    }
                    if (r.fgRequired && !r.fgWaiting) {
                        if (!r.isForeground) {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(ActivityManagerService.TAG, "Launched service must call startForeground() within timeout: " + r);
                            }
                            scheduleServiceForegroundTransitionTimeoutLocked(r);
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(ActivityManagerService.TAG, "Service already foreground; no new timeout: " + r);
                            }
                            r.fgRequired = false;
                        }
                    }
                    int flags = 0;
                    if (si.deliveryCount > 1) {
                        flags = 0 | 2;
                    }
                    if (si.doneExecutingCount > 0) {
                        flags |= 1;
                    }
                    args.add(new ServiceStartArgs(si.taskRemoved, si.id, flags, si.intent));
                }
            }
            ParceledListSlice<ServiceStartArgs> slice = new ParceledListSlice<>(args);
            slice.setInlineCountLimit(4);
            Exception caughtException = null;
            try {
                r.app.thread.scheduleServiceArgs(r, slice);
            } catch (TransactionTooLargeException e) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Transaction too large for " + args.size() + " args, first: " + args.get(0).args);
                }
                Slog.w(ActivityManagerService.TAG, "Failed delivering service starts", e);
                caughtException = e;
            } catch (RemoteException e2) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Crashed while sending args: " + r);
                }
                Slog.w(ActivityManagerService.TAG, "Failed delivering service starts", e2);
                caughtException = e2;
            } catch (Exception e3) {
                Slog.w(ActivityManagerService.TAG, "Unexpected exception", e3);
                caughtException = e3;
            }
            if (caughtException != null) {
                boolean inDestroying = this.mDestroyingServices.contains(r);
                for (i = 0; i < args.size(); i++) {
                    serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                }
                if ((caughtException instanceof TransactionTooLargeException) != 0) {
                    throw ((TransactionTooLargeException) caughtException);
                }
            }
        }
    }

    private final boolean isServiceNeededLocked(ServiceRecord r, boolean knowConn, boolean hasConn) {
        if (r.startRequested) {
            return true;
        }
        if (!knowConn) {
            hasConn = r.hasAutoCreateConnections();
        }
        if (hasConn) {
            return true;
        }
        return false;
    }

    private final void bringDownServiceIfNeededLocked(ServiceRecord r, boolean knowConn, boolean hasConn) {
        if (isServiceNeededLocked(r, knowConn, hasConn)) {
            Flog.i(102, "ServiceNeeded not bring down service:" + r);
        } else if (this.mPendingServices.contains(r)) {
            Flog.i(102, "still in launching not bring down service:" + r);
        } else {
            try {
                bringDownServiceLocked(r);
            } catch (IllegalStateException e) {
                Slog.w(ActivityManagerService.TAG, "Exception when r is illegal!");
            }
        }
    }

    private final void bringDownServiceLocked(ServiceRecord r) {
        int i;
        ServiceRecord serviceRecord = r;
        int connectionSize = serviceRecord.connections.size();
        if (ActivityManagerDebugConfig.HWFLOW && ((long) connectionSize) > SERVICE_CONNECTIONS_THRESHOLD) {
            Flog.i(102, "bringDownServiceLocked " + serviceRecord + ",connection size= " + connectionSize);
        }
        long start = SystemClock.uptimeMillis();
        int conni = connectionSize - 1;
        while (true) {
            int conni2 = conni;
            if (conni2 < 0) {
                break;
            }
            ArrayList<ConnectionRecord> c = serviceRecord.connections.valueAt(conni2);
            for (int i2 = 0; i2 < c.size(); i2++) {
                ConnectionRecord cr = c.get(i2);
                cr.serviceDead = true;
                if (this.mAm.mHwAMSEx.needCheckProcDied(cr.binding == null ? null : cr.binding.client)) {
                    Slog.d(ActivityManagerService.TAG, "client is dead, cr:" + cr);
                } else {
                    try {
                        cr.conn.connected(serviceRecord.name, null, true);
                    } catch (Exception e) {
                        Slog.w(ActivityManagerService.TAG, "Failure disconnecting service " + serviceRecord.name + " to connection " + c.get(i2).conn.asBinder() + " (in " + c.get(i2).binding.client.processName + ")", e);
                    }
                }
            }
            conni = conni2 - 1;
        }
        long diff = SystemClock.uptimeMillis() - start;
        if (diff > 1000) {
            if (ActivityManagerDebugConfig.HWFLOW) {
                Flog.i(102, "bringDownServiceLocked " + serviceRecord + ",took " + diff + "ms");
            }
            Jlog.d(377, r.toString(), (int) diff, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        }
        if (serviceRecord.app != null && serviceRecord.app.thread != null && !this.mAm.mHwAMSEx.needCheckProcDied(serviceRecord.app)) {
            int i3 = serviceRecord.bindings.size() - 1;
            while (true) {
                int i4 = i3;
                if (i4 < 0) {
                    break;
                }
                IntentBindRecord ibr = serviceRecord.bindings.valueAt(i4);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Bringing down binding " + ibr + ": hasBound=" + ibr.hasBound);
                }
                if (ibr.hasBound) {
                    try {
                        bumpServiceExecutingLocked(serviceRecord, false, "bring down unbind");
                        this.mAm.updateOomAdjLocked(serviceRecord.app, true);
                        ibr.hasBound = false;
                        ibr.requested = false;
                        serviceRecord.app.thread.scheduleUnbindService(serviceRecord, ibr.intent.getIntent());
                    } catch (Exception e2) {
                        Slog.w(ActivityManagerService.TAG, "Exception when unbinding service " + serviceRecord.shortName, e2);
                        serviceProcessGoneLocked(r);
                    }
                }
                i3 = i4 - 1;
            }
        }
        if (serviceRecord.fgRequired != 0) {
            Slog.w(ActivityManagerService.TAG, "Bringing down service while still waiting for start foreground: " + serviceRecord);
            serviceRecord.fgRequired = false;
            serviceRecord.fgWaiting = false;
            this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, serviceRecord.appInfo.uid, serviceRecord.packageName);
            this.mAm.mHandler.removeMessages(66, serviceRecord);
            if (serviceRecord.app != null) {
                Message msg = this.mAm.mHandler.obtainMessage(69);
                msg.obj = serviceRecord.app;
                msg.getData().putCharSequence("servicerecord", r.toString());
                this.mAm.mHandler.sendMessage(msg);
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            RuntimeException here = new RuntimeException();
            here.fillInStackTrace();
            Slog.v(ActivityManagerService.TAG, "Bringing down " + serviceRecord + " " + serviceRecord.intent, here);
        }
        serviceRecord.destroyTime = SystemClock.uptimeMillis();
        int i5 = serviceRecord.userId;
        int identityHashCode = System.identityHashCode(r);
        if (serviceRecord.app != null) {
            i = serviceRecord.app.pid;
        } else {
            i = -1;
        }
        EventLogTags.writeAmDestroyService(i5, identityHashCode, i);
        ServiceMap smap = getServiceMapLocked(serviceRecord.userId);
        ServiceRecord found = smap.mServicesByName.remove(serviceRecord.name);
        if (found == null || found == serviceRecord) {
            smap.mServicesByIntent.remove(serviceRecord.intent);
            serviceRecord.totalRestartCount = 0;
            unscheduleServiceRestartLocked(serviceRecord, 0, true);
            for (int i6 = this.mPendingServices.size() - 1; i6 >= 0; i6--) {
                if (this.mPendingServices.get(i6) == serviceRecord) {
                    this.mPendingServices.remove(i6);
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(ActivityManagerService.TAG, "Removed pending: " + serviceRecord);
                    }
                }
            }
            cancelForegroundNotificationLocked(r);
            if (serviceRecord.isForeground) {
                decActiveForegroundAppLocked(smap, serviceRecord);
                this.mAm.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAm.mAppOpsService), 76, serviceRecord.appInfo.uid, serviceRecord.packageName);
                StatsLog.write(60, serviceRecord.appInfo.uid, serviceRecord.shortName, 2);
            }
            serviceRecord.isForeground = false;
            serviceRecord.foregroundId = 0;
            serviceRecord.foregroundNoti = null;
            r.clearDeliveredStartsLocked();
            serviceRecord.pendingStarts.clear();
            if (serviceRecord.app != null) {
                synchronized (serviceRecord.stats.getBatteryStats()) {
                    serviceRecord.stats.stopLaunchedLocked();
                }
                serviceRecord.app.services.remove(serviceRecord);
                if (serviceRecord.whitelistManager) {
                    updateWhitelistManagerLocked(serviceRecord.app);
                }
                if (serviceRecord.app.thread != null) {
                    updateServiceForegroundLocked(serviceRecord.app, false);
                    try {
                        bumpServiceExecutingLocked(serviceRecord, false, "destroy");
                        this.mDestroyingServices.add(serviceRecord);
                        serviceRecord.destroying = true;
                        this.mAm.updateOomAdjLocked(serviceRecord.app, true);
                        serviceRecord.app.thread.scheduleStopService(serviceRecord);
                    } catch (Exception e3) {
                        Slog.w(ActivityManagerService.TAG, "Exception when destroying service " + serviceRecord.shortName, e3);
                        serviceProcessGoneLocked(r);
                    }
                } else {
                    Flog.i(102, "Removed service that has no process: " + serviceRecord);
                }
            } else {
                Flog.i(102, "Removed service that is not running: " + serviceRecord);
            }
            if (serviceRecord.bindings.size() > 0) {
                serviceRecord.bindings.clear();
            }
            if (serviceRecord.restarter instanceof ServiceRestarter) {
                ((ServiceRestarter) serviceRecord.restarter).setService(null);
            }
            int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
            long now = SystemClock.uptimeMillis();
            if (serviceRecord.tracker != null) {
                serviceRecord.tracker.setStarted(false, memFactor, now);
                serviceRecord.tracker.setBound(false, memFactor, now);
                if (serviceRecord.executeNesting == 0) {
                    serviceRecord.tracker.clearCurrentOwner(serviceRecord, false);
                    serviceRecord.tracker = null;
                }
            }
            smap.ensureNotStartingBackgroundLocked(serviceRecord);
            return;
        }
        smap.mServicesByName.put(serviceRecord.name, found);
        throw new IllegalStateException("Bringing down " + serviceRecord + " but actually running " + found);
    }

    /* access modifiers changed from: package-private */
    public void removeConnectionLocked(ConnectionRecord c, ProcessRecord skipApp, ActivityRecord skipAct) {
        IBinder binder = c.conn.asBinder();
        AppBindRecord b = c.binding;
        ServiceRecord s = b.service;
        ArrayList<ConnectionRecord> clist = s.connections.get(binder);
        if (clist != null) {
            clist.remove(c);
            if (clist.size() == 0) {
                s.connections.remove(binder);
            }
        }
        b.connections.remove(c);
        if (!(c.activity == null || c.activity == skipAct || c.activity.connections == null)) {
            c.activity.connections.remove(c);
        }
        if (b.client != skipApp) {
            b.client.connections.remove(c);
            ProcessRecord pr = b.client;
            if (!(s.app == null || s.app.uid < 10000 || pr.pid == s.app.pid || s.app.info == null || pr.info == null || s.app.info.packageName == null || s.app.info.packageName.equals(pr.info.packageName))) {
                LogPower.push(167, s.processName, Integer.toString(pr.pid), Integer.toString(s.app.pid), new String[]{"service"});
            }
            if ((c.flags & 8) != 0) {
                b.client.updateHasAboveClientLocked();
            }
            if ((c.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0) {
                s.updateWhitelistManager();
                if (!s.whitelistManager && s.app != null) {
                    updateWhitelistManagerLocked(s.app);
                }
            }
            if (s.app != null) {
                updateServiceClientActivitiesLocked(s.app, c, true);
            }
        }
        ArrayList<ConnectionRecord> clist2 = this.mServiceConnections.get(binder);
        if (clist2 != null) {
            clist2.remove(c);
            if (clist2.size() == 0) {
                this.mServiceConnections.remove(binder);
            }
        }
        this.mAm.stopAssociationLocked(b.client.uid, b.client.processName, s.appInfo.uid, s.name);
        this.mAm.mHwAMSEx.reportServiceRelationIAware(3, s, b.client);
        if (b.connections.size() == 0) {
            b.intent.apps.remove(b.client);
        }
        if (!c.serviceDead) {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(ActivityManagerService.TAG, "Disconnecting binding " + b.intent + ": shouldUnbind=" + b.intent.hasBound);
            }
            if (s.app != null && s.app.thread != null && b.intent.apps.size() == 0 && b.intent.hasBound && !this.mAm.mHwAMSEx.needCheckProcDied(s.app)) {
                try {
                    bumpServiceExecutingLocked(s, false, "unbind");
                    if (b.client != s.app && (c.flags & 32) == 0 && s.app.setProcState <= 12) {
                        this.mAm.updateLruProcessLocked(s.app, false, null);
                    }
                    this.mAm.updateOomAdjLocked(s.app, true);
                    b.intent.hasBound = false;
                    b.intent.doRebind = false;
                    s.app.thread.scheduleUnbindService(s, b.intent.intent.getIntent());
                } catch (Exception e) {
                    Slog.w(ActivityManagerService.TAG, "Exception when unbinding service " + s.shortName, e);
                    if (!s.app.killedByAm) {
                        serviceProcessGoneLocked(s);
                    }
                }
            }
            this.mPendingServices.remove(s);
            if ((c.flags & 1) != 0) {
                boolean hasAutoCreate = s.hasAutoCreateConnections();
                if (!hasAutoCreate && s.tracker != null) {
                    s.tracker.setBound(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                }
                bringDownServiceIfNeededLocked(s, true, hasAutoCreate);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void serviceDoneExecutingLocked(ServiceRecord r, int type, int startId, int res) {
        boolean inDestroying = this.mDestroyingServices.contains(r);
        if (r != null) {
            if (type == 1) {
                r.callStart = true;
                if (res != 1000) {
                    switch (res) {
                        case 0:
                        case 1:
                            r.findDeliveredStart(startId, false, true);
                            r.stopIfKilled = false;
                            break;
                        case 2:
                            r.findDeliveredStart(startId, false, true);
                            if (r.getLastStartId() == startId) {
                                r.stopIfKilled = true;
                                break;
                            }
                            break;
                        case 3:
                            ServiceRecord.StartItem si = r.findDeliveredStart(startId, false, false);
                            if (si != null) {
                                si.deliveryCount = 0;
                                si.doneExecutingCount++;
                                r.stopIfKilled = true;
                                break;
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown service start result: " + res);
                    }
                } else {
                    r.findDeliveredStart(startId, true, true);
                }
                if (res == 0) {
                    r.callStart = false;
                }
            } else if (type == 2) {
                if (!inDestroying) {
                    if (r.app != null) {
                        Flog.w(102, "Service done with onDestroy, but not inDestroying:" + r + ", app=" + r.app);
                    }
                } else if (r.executeNesting != 1) {
                    Flog.w(102, "Service done with onDestroy, but executeNesting=" + r.executeNesting + ": " + r);
                    Slog.w(ActivityManagerService.TAG, "Service done with onDestroy, but executeNesting=" + r.executeNesting + ": " + r);
                    r.executeNesting = 1;
                }
            }
            long origId = Binder.clearCallingIdentity();
            serviceDoneExecutingLocked(r, inDestroying, inDestroying);
            Binder.restoreCallingIdentity(origId);
            return;
        }
        Slog.w(ActivityManagerService.TAG, "Done executing unknown service from pid " + Binder.getCallingPid());
    }

    private void serviceProcessGoneLocked(ServiceRecord r) {
        if (r.tracker != null) {
            int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
            long now = SystemClock.uptimeMillis();
            r.tracker.setExecuting(false, memFactor, now);
            r.tracker.setBound(false, memFactor, now);
            r.tracker.setStarted(false, memFactor, now);
        }
        serviceDoneExecutingLocked(r, true, true);
    }

    private void serviceDoneExecutingLocked(ServiceRecord r, boolean inDestroying, boolean finishing) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(ActivityManagerService.TAG, "<<< DONE EXECUTING " + r + ": nesting=" + r.executeNesting + ", inDestroying=" + inDestroying + ", app=" + r.app);
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
            Slog.v(ActivityManagerService.TAG, "<<< DONE EXECUTING " + r.shortName);
        }
        r.executeNesting--;
        if (r.executeNesting <= 0) {
            if (r.app != null) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Nesting at 0 of " + r.shortName);
                }
                r.app.execServicesFg = false;
                r.app.executingServices.remove(r);
                if (r.app.executingServices.size() == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE || ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
                        Slog.v(ActivityManagerService.TAG, "No more executingServices of " + r.shortName);
                    }
                    this.mAm.mHandler.removeMessages(12, r.app);
                    this.mAm.mHandler.removeMessages(96, r.app);
                    this.mAm.mHandler.removeMessages(99, r.app);
                } else if (r.executeFg) {
                    int i = r.app.executingServices.size() - 1;
                    while (true) {
                        if (i < 0) {
                            break;
                        } else if (r.app.executingServices.valueAt(i).executeFg) {
                            r.app.execServicesFg = true;
                            break;
                        } else {
                            i--;
                        }
                    }
                }
                if (inDestroying) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(ActivityManagerService.TAG, "doneExecuting remove destroying " + r);
                    }
                    this.mDestroyingServices.remove(r);
                    r.bindings.clear();
                }
                this.mAm.updateOomAdjLocked(r.app, true);
            }
            r.executeFg = false;
            if (r.tracker != null) {
                r.tracker.setExecuting(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                if (finishing) {
                    r.tracker.clearCurrentOwner(r, false);
                    r.tracker = null;
                }
            }
            if (finishing) {
                if (r.app != null && !r.app.persistent) {
                    r.app.services.remove(r);
                    if (r.whitelistManager) {
                        updateWhitelistManagerLocked(r.app);
                    }
                }
                r.app = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean attachApplicationLocked(ProcessRecord proc, String processName) throws RemoteException {
        boolean didSomething = false;
        int i = 0;
        if (this.mPendingServices.size() > 0) {
            ServiceRecord sr = null;
            boolean didSomething2 = false;
            int i2 = 0;
            while (i2 < this.mPendingServices.size()) {
                try {
                    sr = this.mPendingServices.get(i2);
                    if (proc != sr.isolatedProc) {
                        if (proc.uid == sr.appInfo.uid) {
                            if (!processName.equals(sr.processName)) {
                            }
                        }
                        i2++;
                    }
                    this.mPendingServices.remove(i2);
                    i2--;
                    proc.addPackage(sr.appInfo.packageName, sr.appInfo.longVersionCode, this.mAm.mProcessStats);
                    realStartServiceLocked(sr, proc, sr.createdFromFg);
                    didSomething2 = true;
                    if (!isServiceNeededLocked(sr, false, false)) {
                        bringDownServiceLocked(sr);
                    }
                    i2++;
                } catch (RemoteException e) {
                    Slog.w(ActivityManagerService.TAG, "Exception in new application when starting service " + sr.shortName, e);
                    throw e;
                }
            }
            didSomething = didSomething2;
        }
        if (this.mRestartingServices.size() > 0) {
            while (true) {
                int i3 = i;
                if (i3 >= this.mRestartingServices.size()) {
                    break;
                }
                ServiceRecord sr2 = this.mRestartingServices.get(i3);
                if (proc == sr2.isolatedProc || (proc.uid == sr2.appInfo.uid && processName.equals(sr2.processName))) {
                    this.mAm.mHandler.removeCallbacks(sr2.restarter);
                    this.mAm.mHandler.post(sr2.restarter);
                }
                i = i3 + 1;
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public void processStartTimedOutLocked(ProcessRecord proc) {
        int i = 0;
        while (i < this.mPendingServices.size()) {
            ServiceRecord sr = this.mPendingServices.get(i);
            if ((proc.uid == sr.appInfo.uid && proc.processName.equals(sr.processName)) || sr.isolatedProc == proc) {
                Slog.w(ActivityManagerService.TAG, "Forcing bringing down service: " + sr);
                sr.isolatedProc = null;
                this.mPendingServices.remove(i);
                i += -1;
                bringDownServiceLocked(sr);
            }
            i++;
        }
    }

    private boolean collectPackageServicesLocked(String packageName, Set<String> filterByClasses, boolean evenPersistent, boolean doit, boolean killProcess, ArrayMap<ComponentName, ServiceRecord> services) {
        boolean didSomething = false;
        for (int i = services.size() - 1; i >= 0; i--) {
            ServiceRecord service = services.valueAt(i);
            if (service != null) {
                if ((packageName == null || (service.packageName.equals(packageName) && (filterByClasses == null || filterByClasses.contains(service.name.getClassName())))) && (service.app == null || evenPersistent || !service.app.persistent)) {
                    if (!doit) {
                        return true;
                    }
                    didSomething = true;
                    Slog.i(ActivityManagerService.TAG, "  Force stopping service " + service);
                    if (service.app != null) {
                        service.app.removed = killProcess;
                        if (!service.app.persistent) {
                            service.app.services.remove(service);
                            if (service.whitelistManager) {
                                updateWhitelistManagerLocked(service.app);
                            }
                        }
                        if (service.app.executingServices.size() == 1 && service.app.executingServices.contains(service)) {
                            Slog.w(ActivityManagerService.TAG, "Remove timeout message for service: " + service);
                            service.app.execServicesFg = false;
                            this.mAm.mHandler.removeMessages(12, service.app);
                            this.mAm.mHandler.removeMessages(96, service.app);
                            this.mAm.mHandler.removeMessages(99, service.app);
                        }
                        service.app.executingServices.remove(service);
                    }
                    service.app = null;
                    service.isolatedProc = null;
                    if (this.mTmpCollectionResults == null) {
                        this.mTmpCollectionResults = new ArrayList<>();
                    }
                    this.mTmpCollectionResults.add(service);
                }
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public boolean bringDownDisabledPackageServicesLocked(String packageName, Set<String> filterByClasses, int userId, boolean evenPersistent, boolean killProcess, boolean doit) {
        String str = packageName;
        int i = userId;
        boolean didSomething = false;
        if (this.mTmpCollectionResults != null) {
            this.mTmpCollectionResults.clear();
        }
        if (i == -1) {
            int i2 = this.mServiceMap.size() - 1;
            while (true) {
                int i3 = i2;
                if (i3 < 0) {
                    break;
                }
                didSomething |= collectPackageServicesLocked(str, filterByClasses, evenPersistent, doit, killProcess, this.mServiceMap.valueAt(i3).mServicesByName);
                if (!doit && didSomething) {
                    return true;
                }
                if (doit && filterByClasses == null) {
                    forceStopPackageLocked(str, this.mServiceMap.valueAt(i3).mUserId);
                }
                i2 = i3 - 1;
            }
        } else {
            ServiceMap smap = this.mServiceMap.get(i);
            if (smap != null) {
                didSomething = collectPackageServicesLocked(str, filterByClasses, evenPersistent, doit, killProcess, smap.mServicesByName);
            }
            if (doit && filterByClasses == null) {
                forceStopPackageLocked(str, i);
            }
        }
        if (this.mTmpCollectionResults != null) {
            for (int i4 = this.mTmpCollectionResults.size() - 1; i4 >= 0; i4--) {
                bringDownServiceLocked(this.mTmpCollectionResults.get(i4));
            }
            this.mTmpCollectionResults.clear();
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public void forceStopPackageLocked(String packageName, int userId) {
        ServiceMap smap = this.mServiceMap.get(userId);
        if (smap != null && smap.mActiveForegroundApps.size() > 0) {
            for (int i = smap.mActiveForegroundApps.size() - 1; i >= 0; i--) {
                if (smap.mActiveForegroundApps.valueAt(i).mPackageName.equals(packageName)) {
                    smap.mActiveForegroundApps.removeAt(i);
                    smap.mActiveForegroundAppsChanged = true;
                }
            }
            if (smap.mActiveForegroundAppsChanged != 0) {
                requestUpdateActiveForegroundAppsLocked(smap, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void cleanUpRemovedTaskLocked(TaskRecord tr, ComponentName component, Intent baseIntent) {
        ArrayList arrayList = new ArrayList();
        ArrayMap<ComponentName, ServiceRecord> alls = getServicesLocked(tr.userId);
        for (int i = alls.size() - 1; i >= 0; i--) {
            ServiceRecord sr = alls.valueAt(i);
            if (sr != null && sr.packageName.equals(component.getPackageName())) {
                arrayList.add(sr);
            }
        }
        if (arrayList.size() > 0) {
            LogPower.push(148, "cleanUpservice", component.getPackageName());
        }
        int i2 = arrayList.size() - 1;
        while (true) {
            int i3 = i2;
            if (i3 >= 0) {
                ServiceRecord sr2 = (ServiceRecord) arrayList.get(i3);
                if (sr2.startRequested) {
                    if ((sr2.serviceInfo.flags & 1) != 0) {
                        Slog.i(ActivityManagerService.TAG, "Stopping service " + sr2.shortName + ": remove task");
                        stopServiceLocked(sr2);
                    } else {
                        ArrayList<ServiceRecord.StartItem> arrayList2 = sr2.pendingStarts;
                        ServiceRecord.StartItem startItem = new ServiceRecord.StartItem(sr2, true, sr2.getLastStartId(), baseIntent, null, 0);
                        arrayList2.add(startItem);
                        if (!(sr2.app == null || sr2.app.thread == null)) {
                            try {
                                sendServiceArgsLocked(sr2, true, false);
                            } catch (TransactionTooLargeException e) {
                            }
                        }
                    }
                }
                i2 = i3 - 1;
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: type inference failed for: r3v0 */
    /* JADX WARNING: type inference failed for: r3v1, types: [boolean] */
    /* JADX WARNING: type inference failed for: r3v14 */
    /* JADX WARNING: type inference failed for: r4v23, types: [android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v25 */
    /* JADX WARNING: type inference failed for: r4v29 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x0281 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0265  */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final void killServicesLocked(ProcessRecord app, boolean allowRestart) {
        ProcessRecord processRecord;
        ServiceMap smap;
        ServiceMap smap2;
        ProcessRecord processRecord2 = app;
        ? r3 = 1;
        int i = processRecord2.connections.size() - 1;
        while (true) {
            processRecord = null;
            if (i < 0) {
                break;
            }
            removeConnectionLocked(processRecord2.connections.valueAt(i), processRecord2, null);
            i--;
        }
        updateServiceConnectionActivitiesLocked(app);
        processRecord2.connections.clear();
        processRecord2.whitelistManager = false;
        int i2 = processRecord2.services.size() - 1;
        while (i2 >= 0) {
            ServiceRecord sr = processRecord2.services.valueAt(i2);
            synchronized (sr.stats.getBatteryStats()) {
                sr.stats.stopLaunchedLocked();
            }
            if (!(sr.app == processRecord2 || sr.app == null || sr.app.persistent)) {
                sr.app.services.remove(sr);
            }
            sr.app = processRecord;
            sr.isolatedProc = processRecord;
            sr.executeNesting = 0;
            sr.forceClearTracker();
            if (this.mDestroyingServices.remove(sr) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(ActivityManagerService.TAG, "killServices remove destroying " + sr);
            }
            int bindingi = sr.bindings.size() - 1;
            ? r4 = processRecord;
            while (bindingi >= 0) {
                IntentBindRecord b = sr.bindings.valueAt(bindingi);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "Killing binding " + b + ": shouldUnbind=" + b.hasBound);
                }
                b.binder = r4;
                b.hasBound = false;
                b.received = false;
                b.requested = false;
                for (int appi = b.apps.size() - 1; appi >= 0; appi--) {
                    ProcessRecord proc = b.apps.keyAt(appi);
                    if (!proc.killedByAm && proc.thread != null) {
                        AppBindRecord abind = b.apps.valueAt(appi);
                        boolean hasCreate = false;
                        int conni = abind.connections.size() - 1;
                        while (true) {
                            if (conni < 0) {
                                break;
                            } else if ((abind.connections.valueAt(conni).flags & 49) == 1) {
                                hasCreate = true;
                                break;
                            } else {
                                conni--;
                            }
                        }
                        if (!hasCreate) {
                        }
                    }
                }
                bindingi--;
                r4 = 0;
            }
            i2--;
            processRecord = null;
        }
        ServiceMap smap3 = getServiceMapLocked(processRecord2.userId);
        int i3 = processRecord2.services.size() - 1;
        boolean preventRestart = false;
        boolean getPreventResult = false;
        boolean allowRestart2 = allowRestart;
        while (i3 >= 0) {
            ServiceRecord sr2 = processRecord2.services.valueAt(i3);
            if (!processRecord2.persistent) {
                processRecord2.services.removeAt(i3);
            }
            ServiceRecord curRec = smap3.mServicesByName.get(sr2.name);
            if (curRec == sr2) {
                if (allowRestart2) {
                    allowRestart2 = this.mAm.isAcquireAppServiceResourceLocked(sr2, processRecord2);
                    if (!allowRestart2) {
                        Flog.i(102, "Service " + sr2 + " in process " + processRecord2 + " prevent restart by RMS srname: " + sr2.name);
                    }
                }
                if (!allowRestart2 || ((long) sr2.crashCount) < this.mAm.mConstants.BOUND_SERVICE_MAX_CRASH_RETRY || (sr2.serviceInfo.applicationInfo.flags & 8) != 0) {
                    if (!allowRestart2) {
                        smap2 = smap3;
                    } else if (!this.mAm.mUserController.isUserRunning(sr2.userId, 0)) {
                        smap2 = smap3;
                    } else {
                        boolean canceled = scheduleServiceRestartLocked(sr2, r3);
                        boolean bringDown = false;
                        if (sr2.startRequested) {
                            if (!sr2.stopIfKilled && !canceled) {
                                smap = smap3;
                                if (!bringDown) {
                                }
                                i3--;
                                smap3 = smap;
                                r3 = 1;
                            } else if (sr2.pendingStarts.size() == 0) {
                                sr2.startRequested = false;
                                if (sr2.tracker != null) {
                                    smap = smap3;
                                    sr2.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                                } else {
                                    smap = smap3;
                                }
                                if (!sr2.hasAutoCreateConnections()) {
                                    bringDownServiceLocked(sr2);
                                    bringDown = true;
                                }
                                if (!bringDown) {
                                    if (!getPreventResult) {
                                        preventRestart = this.mAm.shouldPreventRestartService(sr2.serviceInfo, false);
                                        getPreventResult = true;
                                    }
                                    boolean allowRestart3 = !preventRestart;
                                    if (!allowRestart3) {
                                        bringDownServiceLocked(sr2);
                                    }
                                    allowRestart2 = allowRestart3;
                                }
                                i3--;
                                smap3 = smap;
                                r3 = 1;
                            }
                        }
                        smap = smap3;
                        if (!bringDown) {
                        }
                        i3--;
                        smap3 = smap;
                        r3 = 1;
                    }
                    bringDownServiceLocked(sr2);
                    i3--;
                    smap3 = smap;
                    r3 = 1;
                } else {
                    Slog.w(ActivityManagerService.TAG, "Service crashed " + sr2.crashCount + " times, stopping: " + sr2);
                    Object[] objArr = new Object[4];
                    objArr[0] = Integer.valueOf(sr2.userId);
                    objArr[r3] = Integer.valueOf(sr2.crashCount);
                    objArr[2] = sr2.shortName;
                    objArr[3] = Integer.valueOf(processRecord2.pid);
                    EventLog.writeEvent(EventLogTags.AM_SERVICE_CRASHED_TOO_MUCH, objArr);
                    bringDownServiceLocked(sr2);
                }
            } else if (curRec != null) {
                Slog.wtf(ActivityManagerService.TAG, "Service " + sr2 + " in process " + processRecord2 + " not same as in map: " + curRec);
            }
            smap = smap3;
            i3--;
            smap3 = smap;
            r3 = 1;
        }
        if (!allowRestart2) {
            processRecord2.services.clear();
            for (int i4 = this.mRestartingServices.size() - 1; i4 >= 0; i4--) {
                ServiceRecord r = this.mRestartingServices.get(i4);
                if (r.processName.equals(processRecord2.processName) && r.serviceInfo.applicationInfo.uid == processRecord2.info.uid) {
                    this.mRestartingServices.remove(i4);
                    clearRestartingIfNeededLocked(r);
                }
            }
            for (int i5 = this.mPendingServices.size() - 1; i5 >= 0; i5--) {
                ServiceRecord r2 = this.mPendingServices.get(i5);
                if (r2.processName.equals(processRecord2.processName) && r2.serviceInfo.applicationInfo.uid == processRecord2.info.uid) {
                    this.mPendingServices.remove(i5);
                }
            }
        }
        int i6 = this.mDestroyingServices.size();
        while (i6 > 0) {
            i6--;
            ServiceRecord sr3 = this.mDestroyingServices.get(i6);
            if (sr3.app == processRecord2) {
                sr3.forceClearTracker();
                this.mDestroyingServices.remove(i6);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(ActivityManagerService.TAG, "killServices remove destroying " + sr3);
                }
            }
        }
        processRecord2.executingServices.clear();
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.RunningServiceInfo makeRunningServiceInfoLocked(ServiceRecord r) {
        ActivityManager.RunningServiceInfo info = new ActivityManager.RunningServiceInfo();
        info.service = r.name;
        if (r.app != null) {
            info.pid = r.app.pid;
        }
        info.uid = r.appInfo.uid;
        info.process = r.processName;
        info.foreground = r.isForeground;
        info.activeSince = r.createRealTime;
        info.started = r.startRequested;
        info.clientCount = r.connections.size();
        info.crashCount = r.crashCount;
        info.lastActivityTime = r.lastActivity;
        if (r.isForeground) {
            info.flags |= 2;
        }
        if (r.startRequested) {
            info.flags |= 1;
        }
        if (r.app != null && r.app.pid == ActivityManagerService.MY_PID) {
            info.flags |= 4;
        }
        if (r.app != null && r.app.persistent) {
            info.flags |= 8;
        }
        for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> connl = r.connections.valueAt(conni);
            for (int i = 0; i < connl.size(); i++) {
                ConnectionRecord conn = connl.get(i);
                if (conn.clientLabel != 0) {
                    info.clientPackage = conn.binding.client.info.packageName;
                    info.clientLabel = conn.clientLabel;
                    return info;
                }
            }
        }
        return info;
    }

    /* access modifiers changed from: package-private */
    public List<ActivityManager.RunningServiceInfo> getRunningServiceInfoLocked(int maxNum, int flags, int callingUid, boolean allowed, boolean canInteractAcrossUsers) {
        ArrayList<ActivityManager.RunningServiceInfo> res = new ArrayList<>();
        long ident = Binder.clearCallingIdentity();
        int i = 0;
        if (canInteractAcrossUsers) {
            try {
                int[] users = this.mAm.mUserController.getUsers();
                for (int ui = 0; ui < users.length && res.size() < maxNum; ui++) {
                    ArrayMap<ComponentName, ServiceRecord> alls = getServicesLocked(users[ui]);
                    for (int i2 = 0; i2 < alls.size() && res.size() < maxNum; i2++) {
                        ServiceRecord sr = alls.valueAt(i2);
                        if (sr != null) {
                            res.add(makeRunningServiceInfoLocked(sr));
                        }
                    }
                }
                while (i < this.mRestartingServices.size() && res.size() < maxNum) {
                    ServiceRecord r = this.mRestartingServices.get(i);
                    if (r != null) {
                        ActivityManager.RunningServiceInfo info = makeRunningServiceInfoLocked(r);
                        info.restarting = r.nextRestartTime;
                        res.add(info);
                    }
                    i++;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            int userId = UserHandle.getUserId(callingUid);
            ArrayMap<ComponentName, ServiceRecord> alls2 = getServicesLocked(userId);
            for (int i3 = 0; i3 < alls2.size() && res.size() < maxNum; i3++) {
                ServiceRecord sr2 = alls2.valueAt(i3);
                if ((allowed || !(sr2 == null || sr2.app == null || sr2.app.uid != callingUid)) && sr2 != null) {
                    res.add(makeRunningServiceInfoLocked(sr2));
                }
            }
            while (i < this.mRestartingServices.size() && res.size() < maxNum) {
                ServiceRecord r2 = this.mRestartingServices.get(i);
                if (r2.userId == userId && ((allowed || !(r2 == null || r2.app == null || r2.app.uid != callingUid)) && r2 != null)) {
                    ActivityManager.RunningServiceInfo info2 = makeRunningServiceInfoLocked(r2);
                    info2.restarting = r2.nextRestartTime;
                    res.add(info2);
                }
                i++;
            }
        }
        return res;
    }

    public PendingIntent getRunningServiceControlPanelLocked(ComponentName name) {
        ServiceRecord r = getServiceByNameLocked(name, UserHandle.getUserId(Binder.getCallingUid()));
        if (r != null) {
            for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
                ArrayList<ConnectionRecord> conn = r.connections.valueAt(conni);
                for (int i = 0; i < conn.size(); i++) {
                    if (conn.get(i).clientIntent != null) {
                        return conn.get(i).clientIntent;
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0197, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x019a, code lost:
        if (r16 == null) goto L_0x01a9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x019c, code lost:
        r1.mAm.mAppErrors.appNotResponding(r8, null, null, false, r16);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x01a9, code lost:
        return;
     */
    public void serviceTimeout(ProcessRecord proc) {
        Boolean frozenAnr;
        String anrMessage;
        int i;
        Boolean frozenAnr2;
        boolean frozenAnr3;
        ServiceRecord sr;
        boolean z;
        ProcessRecord processRecord = proc;
        String anrMessage2 = null;
        boolean frozenAnr4 = false;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (processRecord.executingServices.size() == 0) {
                    anrMessage = null;
                    frozenAnr = false;
                } else if (processRecord.thread == null) {
                    anrMessage = null;
                    frozenAnr = false;
                } else {
                    long now = SystemClock.uptimeMillis();
                    if (processRecord.execServicesFg) {
                        try {
                            i = SERVICE_TIMEOUT;
                        } catch (Throwable th) {
                            th = th;
                        }
                    } else {
                        i = SERVICE_BACKGROUND_TIMEOUT;
                    }
                    long maxTime = now - ((long) i);
                    ServiceRecord timeout = null;
                    long nextTime = 0;
                    boolean z2 = true;
                    int i2 = processRecord.executingServices.size() - 1;
                    while (i2 >= 0) {
                        ServiceRecord sr2 = processRecord.executingServices.valueAt(i2);
                        if (processRecord.execServicesFg) {
                            anrMessage = anrMessage2;
                            frozenAnr = frozenAnr4;
                            try {
                                ServiceRecord sr3 = sr2;
                                if (sr2.executingStart < now - ((long) CHECK_INTERVAL)) {
                                    sr = sr3;
                                    if (this.mAm.isTopProcessLocked(sr.app)) {
                                        timeout = sr;
                                        frozenAnr3 = true;
                                        frozenAnr2 = frozenAnr3;
                                        break;
                                    }
                                    z = true;
                                } else {
                                    sr = sr3;
                                    z = true;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                String str = anrMessage;
                                Boolean bool = frozenAnr;
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } else {
                            anrMessage = anrMessage2;
                            frozenAnr = frozenAnr4;
                            z = z2;
                            sr = sr2;
                        }
                        if (sr.executingStart < maxTime) {
                            timeout = sr;
                            frozenAnr3 = false;
                            frozenAnr2 = frozenAnr3;
                            break;
                        }
                        if (sr.executingStart > nextTime) {
                            nextTime = sr.executingStart;
                        }
                        i2--;
                        z2 = z;
                        anrMessage2 = anrMessage;
                        frozenAnr4 = frozenAnr;
                    }
                    anrMessage = anrMessage2;
                    frozenAnr2 = frozenAnr4;
                    if (timeout != null) {
                        try {
                            if (this.mAm.mLruProcesses.contains(processRecord)) {
                                if (frozenAnr2.booleanValue()) {
                                    Slog.w(ActivityManagerService.TAG, "ANR has been triggered " + ((((long) SERVICE_TIMEOUT) + timeout.executingStart) - now) + "ms earlier because it caused frozen problem.");
                                }
                                Slog.w(ActivityManagerService.TAG, "Timeout executing service: " + timeout);
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new FastPrintWriter(sw, false, 1024);
                                pw.println(timeout);
                                timeout.dump(pw, "    ");
                                pw.close();
                                this.mLastAnrDump = sw.toString();
                                this.mAm.mHandler.removeCallbacks(this.mLastAnrDumpClearer);
                                long j = maxTime;
                                this.mAm.mHandler.postDelayed(this.mLastAnrDumpClearer, AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT);
                                anrMessage = "executing service " + timeout.shortName;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            Boolean bool2 = frozenAnr2;
                            String str2 = anrMessage;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                    long j2 = maxTime;
                    Message msg = this.mAm.mHandler.obtainMessage(12);
                    msg.obj = processRecord;
                    if (nextTime == 0) {
                        Slog.e(ActivityManagerService.TAG, "nextTime invaild, remove the message");
                        this.mAm.mHandler.removeMessages(12, processRecord);
                    } else if (((long) CHECK_INTERVAL) + nextTime < now) {
                        this.mAm.mHandler.sendMessageAtTime(msg, ((long) (processRecord.execServicesFg ? SERVICE_TIMEOUT : SERVICE_BACKGROUND_TIMEOUT)) + nextTime);
                    } else {
                        this.mAm.mHandler.sendMessageAtTime(msg, ((long) (processRecord.execServicesFg ? CHECK_INTERVAL : SERVICE_BACKGROUND_TIMEOUT)) + nextTime);
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th4) {
                th = th4;
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003d, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
        if (r0 == null) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0043, code lost:
        r3 = r9.mAm.mAppErrors;
        r3.appNotResponding(r0, null, null, false, "Context.startForegroundService() did not then call Service.startForeground(): " + r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005f, code lost:
        return;
     */
    public void serviceForegroundTimeout(ServiceRecord r) {
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (r.fgRequired) {
                    if (!r.destroying) {
                        ProcessRecord app = r.app;
                        if (app == null || !app.debugging) {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(ActivityManagerService.TAG, "Service foreground-required timeout for " + r);
                            }
                            r.fgWaiting = false;
                            stopServiceLocked(r);
                        } else {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void updateServiceApplicationInfoLocked(ApplicationInfo applicationInfo) {
        ServiceMap serviceMap = this.mServiceMap.get(UserHandle.getUserId(applicationInfo.uid));
        if (serviceMap != null) {
            ArrayMap<ComponentName, ServiceRecord> servicesByName = serviceMap.mServicesByName;
            for (int j = servicesByName.size() - 1; j >= 0; j--) {
                ServiceRecord serviceRecord = servicesByName.valueAt(j);
                if (applicationInfo.packageName.equals(serviceRecord.appInfo.packageName)) {
                    serviceRecord.appInfo = applicationInfo;
                    serviceRecord.serviceInfo.applicationInfo = applicationInfo;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void serviceForegroundCrash(ProcessRecord app, CharSequence serviceRecord) {
        ActivityManagerService activityManagerService = this.mAm;
        int i = app.uid;
        int i2 = app.pid;
        String str = app.info.packageName;
        int i3 = app.userId;
        activityManagerService.crashApplication(i, i2, str, i3, "Context.startForegroundService() did not then call Service.startForeground(): " + serviceRecord);
    }

    /* access modifiers changed from: package-private */
    public void scheduleServiceTimeoutLocked(ProcessRecord proc) {
        if (proc.executingServices.size() == 0 || proc.thread == null) {
            Flog.i(102, "no need to schedule service timeout");
            return;
        }
        Message msg = this.mAm.mHandler.obtainMessage(12);
        msg.obj = proc;
        this.mAm.mHandler.sendMessageDelayed(msg, (long) (proc.execServicesFg ? CHECK_INTERVAL : SERVICE_BACKGROUND_TIMEOUT));
        Message msg2 = this.mAm.mHandler.obtainMessage(96);
        msg2.obj = proc;
        this.mAm.mHandler.sendMessageDelayed(msg2, 3000);
    }

    /* access modifiers changed from: package-private */
    public void scheduleServiceForegroundTransitionTimeoutLocked(ServiceRecord r) {
        if (r.app.executingServices.size() != 0 && r.app.thread != null) {
            Message msg = this.mAm.mHandler.obtainMessage(66);
            msg.obj = r;
            r.fgWaiting = true;
            this.mAm.mHandler.sendMessageDelayed(msg, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
    }

    /* access modifiers changed from: package-private */
    public ServiceDumper newServiceDumperLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        ServiceDumper serviceDumper = new ServiceDumper(this, fd, pw, args, opti, dumpAll, dumpPackage);
        return serviceDumper;
    }

    /* access modifiers changed from: protected */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        int i;
        ProtoOutputStream protoOutputStream = proto;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                long outterToken = proto.start(fieldId);
                int[] users = this.mAm.mUserController.getUsers();
                int length = users.length;
                int i2 = 0;
                while (i2 < length) {
                    int user = users[i2];
                    ServiceMap smap = this.mServiceMap.get(user);
                    if (smap == null) {
                        i = i2;
                    } else {
                        long token = protoOutputStream.start(2246267895809L);
                        protoOutputStream.write(1120986464257L, user);
                        ArrayMap<ComponentName, ServiceRecord> alls = smap.mServicesByName;
                        int i3 = 0;
                        while (i3 < alls.size()) {
                            alls.valueAt(i3).writeToProto(protoOutputStream, 2246267895810L);
                            i3++;
                            i2 = i2;
                        }
                        i = i2;
                        protoOutputStream.end(token);
                    }
                    i2 = i + 1;
                }
                protoOutputStream.end(outterToken);
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: protected */
    public boolean dumpService(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        int i;
        ArrayList arrayList = new ArrayList();
        Predicate<ServiceRecord> filter = DumpUtils.filterRecord(name);
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                i = 0;
                for (int user : this.mAm.mUserController.getUsers()) {
                    ServiceMap smap = this.mServiceMap.get(user);
                    if (smap != null) {
                        ArrayMap<ComponentName, ServiceRecord> alls = smap.mServicesByName;
                        for (int i2 = 0; i2 < alls.size(); i2++) {
                            ServiceRecord r1 = alls.valueAt(i2);
                            if (filter.test(r1)) {
                                arrayList.add(r1);
                            }
                        }
                    }
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (arrayList.size() <= 0) {
            return false;
        }
        arrayList.sort(Comparator.comparing($$Lambda$Y_KRxxoOXfyYceuDG7WHd46Y_I.INSTANCE));
        boolean needSep = false;
        while (true) {
            int i3 = i;
            if (i3 >= arrayList.size()) {
                return true;
            }
            if (needSep) {
                pw.println();
            }
            needSep = true;
            dumpService(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, fd, pw, (ServiceRecord) arrayList.get(i3), args, dumpAll);
            i = i3 + 1;
        }
    }

    private void dumpService(String prefix, FileDescriptor fd, PrintWriter pw, ServiceRecord r, String[] args, boolean dumpAll) {
        TransferPipe tp;
        String innerPrefix = prefix + "  ";
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                pw.print(prefix);
                pw.print("SERVICE ");
                pw.print(r.shortName);
                pw.print(" ");
                pw.print(Integer.toHexString(System.identityHashCode(r)));
                pw.print(" pid=");
                if (r.app != null) {
                    pw.println(r.app.pid);
                } else {
                    pw.println("(not running)");
                }
                if (dumpAll) {
                    r.dump(pw, innerPrefix);
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (r.app != null && r.app.thread != null) {
            pw.print(prefix);
            pw.println("  Client:");
            pw.flush();
            try {
                tp = new TransferPipe();
                r.app.thread.dumpService(tp.getWriteFd(), r, args);
                tp.setBufferPrefix(prefix + "    ");
                tp.go(fd);
                tp.kill();
            } catch (IOException e) {
                pw.println(prefix + "    Failure while dumping the service: " + e);
            } catch (RemoteException e2) {
                pw.println(prefix + "    Got a RemoteException while dumping the service");
            } catch (Throwable th2) {
                tp.kill();
                throw th2;
            }
        }
    }

    private String[] initPCEntryArgs(ServiceRecord r) {
        if (!HwPCUtils.isPcCastModeInServer()) {
            return null;
        }
        String[] entryPointArgs = null;
        if (TextUtils.equals(r.intent.getIntent().getAction(), "android.view.InputMethod")) {
            WindowManagerInternal wmi = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            if (wmi != null && HwPCUtils.enabledInPad()) {
                entryPointArgs = new String[]{String.valueOf(HwPCUtils.getPCDisplayID())};
            } else if (wmi != null && wmi.isHardKeyboardAvailable()) {
                entryPointArgs = new String[]{String.valueOf(this.mAm.mWindowManager.getFocusedDisplayId())};
            } else if (HwPCUtils.mTouchDeviceID != -1) {
                entryPointArgs = new String[]{String.valueOf(this.mAm.mWindowManager.getFocusedDisplayId()), String.valueOf(HwPCUtils.mTouchDeviceID)};
            } else {
                entryPointArgs = new String[]{String.valueOf(0)};
            }
        }
        if (TextUtils.equals(r.appInfo.packageName, "com.huawei.desktop.systemui") || TextUtils.equals(r.appInfo.packageName, "com.huawei.desktop.explorer")) {
            entryPointArgs = new String[]{String.valueOf(-HwPCUtils.getPCDisplayID())};
        }
        return entryPointArgs;
    }
}
