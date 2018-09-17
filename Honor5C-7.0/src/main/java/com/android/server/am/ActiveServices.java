package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallback;
import android.os.RemoteCallback.OnResultListener;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Flog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.FastPrintWriter;
import com.android.server.job.controllers.JobStatus;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActiveServices {
    static final int BG_START_TIMEOUT = 0;
    private static final boolean DEBUG_DELAYED_SERVICE = false;
    private static final boolean DEBUG_DELAYED_STARTS = false;
    static final boolean IS_FPGA = false;
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    private static final boolean LOG_SERVICE_START_STOP = true;
    static final int MAX_SERVICE_INACTIVITY = 1800000;
    static final int SERVICE_BACKGROUND_TIMEOUT = 0;
    static final int SERVICE_MIN_RESTART_TIME_BETWEEN = 10000;
    private static final boolean SERVICE_RESCHEDULE = false;
    static final int SERVICE_RESET_RUN_DURATION = 60000;
    static final int SERVICE_RESTART_DURATION = 1000;
    static final int SERVICE_RESTART_DURATION_FACTOR = 4;
    static final int SERVICE_TIMEOUT = 0;
    static final int SERVICE_WAIT_TIMEOUT = 3000;
    private static final String TAG = null;
    private static final String TAG_MU = null;
    private static final String TAG_SERVICE = null;
    private static final String TAG_SERVICE_EXECUTING = null;
    static String boardname;
    private static final boolean enableANRWarning = false;
    final ActivityManagerService mAm;
    final ArrayList<ServiceRecord> mDestroyingServices;
    String mLastAnrDump;
    final Runnable mLastAnrDumpClearer;
    final int mMaxStartingBackground;
    final ArrayList<ServiceRecord> mPendingServices;
    final ArrayList<ServiceRecord> mRestartingServices;
    final ArrayMap<IBinder, ArrayList<ConnectionRecord>> mServiceConnections;
    final SparseArray<ServiceMap> mServiceMap;
    ArrayList<ServiceRecord> mTmpCollectionResults;

    /* renamed from: com.android.server.am.ActiveServices.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ Intent val$intent;
        final /* synthetic */ int val$userId;

        AnonymousClass2(Intent val$intent, int val$userId) {
            this.val$intent = val$intent;
            this.val$userId = val$userId;
        }

        public void run() {
            ActiveServices.this.mAm.mContext.startActivityAsUser(this.val$intent, new UserHandle(this.val$userId));
        }
    }

    /* renamed from: com.android.server.am.ActiveServices.3 */
    class AnonymousClass3 implements OnResultListener {
        final /* synthetic */ boolean val$callerFg;
        final /* synthetic */ IServiceConnection val$connection;
        final /* synthetic */ Intent val$serviceIntent;
        final /* synthetic */ ServiceRecord val$serviceRecord;

        AnonymousClass3(ServiceRecord val$serviceRecord, Intent val$serviceIntent, boolean val$callerFg, IServiceConnection val$connection) {
            this.val$serviceRecord = val$serviceRecord;
            this.val$serviceIntent = val$serviceIntent;
            this.val$callerFg = val$callerFg;
            this.val$connection = val$connection;
        }

        public void onResult(Bundle result) {
            synchronized (ActiveServices.this.mAm) {
                long identity;
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    identity = Binder.clearCallingIdentity();
                    if (ActiveServices.this.mPendingServices.contains(this.val$serviceRecord)) {
                        if (ActiveServices.this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(this.val$serviceRecord.packageName, this.val$serviceRecord.userId)) {
                            ActiveServices.this.unbindServiceLocked(this.val$connection);
                        } else {
                            try {
                                ActiveServices.this.bringUpServiceLocked(this.val$serviceRecord, this.val$serviceIntent.getFlags(), this.val$callerFg, ActiveServices.SERVICE_RESCHEDULE, ActiveServices.SERVICE_RESCHEDULE);
                            } catch (RemoteException e) {
                            }
                        }
                        Binder.restoreCallingIdentity(identity);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    Binder.restoreCallingIdentity(identity);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    /* renamed from: com.android.server.am.ActiveServices.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ Intent val$intent;
        final /* synthetic */ int val$userId;

        AnonymousClass4(Intent val$intent, int val$userId) {
            this.val$intent = val$intent;
            this.val$userId = val$userId;
        }

        public void run() {
            ActiveServices.this.mAm.mContext.startActivityAsUser(this.val$intent, new UserHandle(this.val$userId));
        }
    }

    final class ServiceDumper {
        private final String[] args;
        private final boolean dumpAll;
        private final String dumpPackage;
        private final FileDescriptor fd;
        private final ItemMatcher matcher;
        private boolean needSep;
        private final long nowReal;
        private final int opti;
        private boolean printed;
        private boolean printedAnything;
        private final PrintWriter pw;
        private final ArrayList<ServiceRecord> services;

        ServiceDumper(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
            this.services = new ArrayList();
            this.nowReal = SystemClock.elapsedRealtime();
            this.needSep = ActiveServices.SERVICE_RESCHEDULE;
            this.printedAnything = ActiveServices.SERVICE_RESCHEDULE;
            this.printed = ActiveServices.SERVICE_RESCHEDULE;
            this.fd = fd;
            this.pw = pw;
            this.args = args;
            this.opti = opti;
            this.dumpAll = dumpAll;
            this.dumpPackage = dumpPackage;
            this.matcher = new ItemMatcher();
            this.matcher.build(args, opti);
            int[] users = ActiveServices.this.mAm.mUserController.getUsers();
            int length = users.length;
            for (int i = ActiveServices.SERVICE_TIMEOUT; i < length; i++) {
                ServiceMap smap = ActiveServices.this.getServiceMap(users[i]);
                if (smap.mServicesByName.size() > 0) {
                    for (int si = ActiveServices.SERVICE_TIMEOUT; si < smap.mServicesByName.size(); si++) {
                        ServiceRecord r = (ServiceRecord) smap.mServicesByName.valueAt(si);
                        if (this.matcher.match(r, r.name)) {
                            if (dumpPackage != null) {
                                if (!dumpPackage.equals(r.appInfo.packageName)) {
                                }
                            }
                            this.services.add(r);
                        }
                    }
                }
            }
        }

        private void dumpHeaderLocked() {
            this.pw.println("ACTIVITY MANAGER SERVICES (dumpsys activity services)");
            if (ActiveServices.this.mLastAnrDump != null) {
                this.pw.println("  Last ANR service:");
                this.pw.print(ActiveServices.this.mLastAnrDump);
                this.pw.println();
            }
        }

        void dumpLocked() {
            dumpHeaderLocked();
            try {
                int[] users = ActiveServices.this.mAm.mUserController.getUsers();
                int length = users.length;
                for (int i = ActiveServices.SERVICE_TIMEOUT; i < length; i++) {
                    int user = users[i];
                    int serviceIdx = ActiveServices.SERVICE_TIMEOUT;
                    while (serviceIdx < this.services.size() && ((ServiceRecord) this.services.get(serviceIdx)).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = ActiveServices.SERVICE_RESCHEDULE;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = ActiveServices.SERVICE_RESCHEDULE;
                        while (serviceIdx < this.services.size()) {
                            ServiceRecord r = (ServiceRecord) this.services.get(serviceIdx);
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
                Slog.w(ActiveServices.TAG, "Exception in dumpServicesLocked", e);
            }
            dumpRemainsLocked();
        }

        void dumpWithClient() {
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    dumpHeaderLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            try {
                int[] users = ActiveServices.this.mAm.mUserController.getUsers();
                int length = users.length;
                for (int i = ActiveServices.SERVICE_TIMEOUT; i < length; i++) {
                    int user = users[i];
                    int serviceIdx = ActiveServices.SERVICE_TIMEOUT;
                    while (serviceIdx < this.services.size() && ((ServiceRecord) this.services.get(serviceIdx)).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = ActiveServices.SERVICE_RESCHEDULE;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = ActiveServices.SERVICE_RESCHEDULE;
                        while (serviceIdx < this.services.size()) {
                            ServiceRecord r = (ServiceRecord) this.services.get(serviceIdx);
                            serviceIdx++;
                            if (r.userId != user) {
                                break;
                            }
                            synchronized (ActiveServices.this.mAm) {
                                ActivityManagerService.boostPriorityForLockedSection();
                                dumpServiceLocalLocked(r);
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            dumpServiceClient(r);
                        }
                        this.needSep |= this.printed;
                    }
                    synchronized (ActiveServices.this.mAm) {
                        ActivityManagerService.boostPriorityForLockedSection();
                        dumpUserRemainsLocked(user);
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            } catch (Exception e) {
                Slog.w(ActiveServices.TAG, "Exception in dumpServicesLocked", e);
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    dumpRemainsLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        private void dumpUserHeaderLocked(int user) {
            if (!this.printed) {
                if (this.printedAnything) {
                    this.pw.println();
                }
                this.pw.println("  User " + user + " active services:");
                this.printed = ActiveServices.LOG_SERVICE_START_STOP;
            }
            this.printedAnything = ActiveServices.LOG_SERVICE_START_STOP;
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
                this.needSep = ActiveServices.LOG_SERVICE_START_STOP;
                return;
            }
            this.pw.print("    app=");
            this.pw.println(r.app);
            this.pw.print("    created=");
            TimeUtils.formatDuration(r.createTime, this.nowReal, this.pw);
            this.pw.print(" started=");
            this.pw.print(r.startRequested);
            this.pw.print(" connections=");
            this.pw.println(r.connections.size());
            if (r.connections.size() > 0) {
                this.pw.println("    Connections:");
                for (int conni = ActiveServices.SERVICE_TIMEOUT; conni < r.connections.size(); conni++) {
                    ArrayList<ConnectionRecord> clist = (ArrayList) r.connections.valueAt(conni);
                    for (int i = ActiveServices.SERVICE_TIMEOUT; i < clist.size(); i++) {
                        String toShortString;
                        ConnectionRecord conn = (ConnectionRecord) clist.get(i);
                        this.pw.print("      ");
                        this.pw.print(conn.binding.intent.intent.getIntent().toShortString(ActiveServices.SERVICE_RESCHEDULE, ActiveServices.SERVICE_RESCHEDULE, ActiveServices.SERVICE_RESCHEDULE, ActiveServices.SERVICE_RESCHEDULE));
                        this.pw.print(" -> ");
                        ProcessRecord proc = conn.binding.client;
                        PrintWriter printWriter = this.pw;
                        if (proc != null) {
                            toShortString = proc.toShortString();
                        } else {
                            toShortString = "null";
                        }
                        printWriter.println(toShortString);
                    }
                }
            }
        }

        private void dumpServiceClient(ServiceRecord r) {
            ProcessRecord proc = r.app;
            if (proc != null) {
                IApplicationThread thread = proc.thread;
                if (thread != null) {
                    this.pw.println("    Client:");
                    this.pw.flush();
                    TransferPipe tp;
                    try {
                        tp = new TransferPipe();
                        thread.dumpService(tp.getWriteFd().getFileDescriptor(), r, this.args);
                        tp.setBufferPrefix("      ");
                        tp.go(this.fd, 2000);
                        tp.kill();
                    } catch (IOException e) {
                        this.pw.println("      Failure while dumping the service: " + e);
                    } catch (RemoteException e2) {
                        this.pw.println("      Got a RemoteException while dumping the service");
                    } catch (Throwable th) {
                        tp.kill();
                    }
                    this.needSep = ActiveServices.LOG_SERVICE_START_STOP;
                }
            }
        }

        private void dumpUserRemainsLocked(int user) {
            int si;
            ServiceMap smap = ActiveServices.this.getServiceMap(user);
            this.printed = ActiveServices.SERVICE_RESCHEDULE;
            int SN = smap.mDelayedStartList.size();
            for (si = ActiveServices.SERVICE_TIMEOUT; si < SN; si++) {
                ServiceRecord r = (ServiceRecord) smap.mDelayedStartList.get(si);
                if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        this.pw.println("  User " + user + " delayed start services:");
                        this.printed = ActiveServices.LOG_SERVICE_START_STOP;
                    }
                    this.printedAnything = ActiveServices.LOG_SERVICE_START_STOP;
                    this.pw.print("  * Delayed start ");
                    this.pw.println(r);
                }
            }
            this.printed = ActiveServices.SERVICE_RESCHEDULE;
            SN = smap.mStartingBackground.size();
            for (si = ActiveServices.SERVICE_TIMEOUT; si < SN; si++) {
                r = (ServiceRecord) smap.mStartingBackground.get(si);
                if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        this.pw.println("  User " + user + " starting in background:");
                        this.printed = ActiveServices.LOG_SERVICE_START_STOP;
                    }
                    this.printedAnything = ActiveServices.LOG_SERVICE_START_STOP;
                    this.pw.print("  * Starting bg ");
                    this.pw.println(r);
                }
            }
        }

        private void dumpRemainsLocked() {
            int i;
            ServiceRecord r;
            if (ActiveServices.this.mPendingServices.size() > 0) {
                this.printed = ActiveServices.SERVICE_RESCHEDULE;
                for (i = ActiveServices.SERVICE_TIMEOUT; i < ActiveServices.this.mPendingServices.size(); i++) {
                    r = (ServiceRecord) ActiveServices.this.mPendingServices.get(i);
                    if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                        this.printedAnything = ActiveServices.LOG_SERVICE_START_STOP;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = ActiveServices.LOG_SERVICE_START_STOP;
                            this.pw.println("  Pending services:");
                            this.printed = ActiveServices.LOG_SERVICE_START_STOP;
                        }
                        this.pw.print("  * Pending ");
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = ActiveServices.LOG_SERVICE_START_STOP;
            }
            if (ActiveServices.this.mRestartingServices.size() > 0) {
                this.printed = ActiveServices.SERVICE_RESCHEDULE;
                for (i = ActiveServices.SERVICE_TIMEOUT; i < ActiveServices.this.mRestartingServices.size(); i++) {
                    r = (ServiceRecord) ActiveServices.this.mRestartingServices.get(i);
                    if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                        this.printedAnything = ActiveServices.LOG_SERVICE_START_STOP;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = ActiveServices.LOG_SERVICE_START_STOP;
                            this.pw.println("  Restarting services:");
                            this.printed = ActiveServices.LOG_SERVICE_START_STOP;
                        }
                        this.pw.print("  * Restarting ");
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = ActiveServices.LOG_SERVICE_START_STOP;
            }
            if (ActiveServices.this.mDestroyingServices.size() > 0) {
                this.printed = ActiveServices.SERVICE_RESCHEDULE;
                for (i = ActiveServices.SERVICE_TIMEOUT; i < ActiveServices.this.mDestroyingServices.size(); i++) {
                    r = (ServiceRecord) ActiveServices.this.mDestroyingServices.get(i);
                    if (this.matcher.match(r, r.name) && (this.dumpPackage == null || this.dumpPackage.equals(r.appInfo.packageName))) {
                        this.printedAnything = ActiveServices.LOG_SERVICE_START_STOP;
                        if (!this.printed) {
                            if (this.needSep) {
                                this.pw.println();
                            }
                            this.needSep = ActiveServices.LOG_SERVICE_START_STOP;
                            this.pw.println("  Destroying services:");
                            this.printed = ActiveServices.LOG_SERVICE_START_STOP;
                        }
                        this.pw.print("  * Destroy ");
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = ActiveServices.LOG_SERVICE_START_STOP;
            }
            if (this.dumpAll) {
                this.printed = ActiveServices.SERVICE_RESCHEDULE;
                for (int ic = ActiveServices.SERVICE_TIMEOUT; ic < ActiveServices.this.mServiceConnections.size(); ic++) {
                    ArrayList<ConnectionRecord> r2 = (ArrayList) ActiveServices.this.mServiceConnections.valueAt(ic);
                    for (i = ActiveServices.SERVICE_TIMEOUT; i < r2.size(); i++) {
                        ConnectionRecord cr = (ConnectionRecord) r2.get(i);
                        if (this.matcher.match(cr.binding.service, cr.binding.service.name) && (this.dumpPackage == null || (cr.binding.client != null && this.dumpPackage.equals(cr.binding.client.info.packageName)))) {
                            this.printedAnything = ActiveServices.LOG_SERVICE_START_STOP;
                            if (!this.printed) {
                                if (this.needSep) {
                                    this.pw.println();
                                }
                                this.needSep = ActiveServices.LOG_SERVICE_START_STOP;
                                this.pw.println("  Connection bindings to services:");
                                this.printed = ActiveServices.LOG_SERVICE_START_STOP;
                            }
                            this.pw.print("  * ");
                            this.pw.println(cr);
                            cr.dump(this.pw, "    ");
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

    class ServiceMap extends Handler {
        static final int MSG_BG_START_TIMEOUT = 1;
        final ArrayList<ServiceRecord> mDelayedStartList;
        final ArrayMap<FilterComparison, ServiceRecord> mServicesByIntent;
        final ArrayMap<ComponentName, ServiceRecord> mServicesByName;
        final ArrayList<ServiceRecord> mStartingBackground;
        final int mUserId;

        ServiceMap(Looper looper, int userId) {
            super(looper);
            this.mServicesByName = new ArrayMap();
            this.mServicesByIntent = new ArrayMap();
            this.mDelayedStartList = new ArrayList();
            this.mStartingBackground = new ArrayList();
            this.mUserId = userId;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BG_START_TIMEOUT /*1*/:
                    synchronized (ActiveServices.this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            rescheduleDelayedStarts();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                default:
            }
        }

        void ensureNotStartingBackground(ServiceRecord r) {
            if (this.mStartingBackground.remove(r)) {
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    Slog.v(ActiveServices.TAG_SERVICE, "No longer background starting: " + r);
                }
                rescheduleDelayedStarts();
            }
            if (this.mDelayedStartList.remove(r) && ActiveServices.DEBUG_DELAYED_STARTS) {
                Slog.v(ActiveServices.TAG_SERVICE, "No longer delaying start: " + r);
            }
        }

        void rescheduleDelayedStarts() {
            removeMessages(MSG_BG_START_TIMEOUT);
            long now = SystemClock.uptimeMillis();
            int i = ActiveServices.SERVICE_TIMEOUT;
            int N = this.mStartingBackground.size();
            while (i < N) {
                ServiceRecord r = (ServiceRecord) this.mStartingBackground.get(i);
                if (r.startingBgTimeout <= now) {
                    Slog.i(ActiveServices.TAG, "Waited long enough for: " + r);
                    this.mStartingBackground.remove(i);
                    N--;
                    i--;
                }
                i += MSG_BG_START_TIMEOUT;
            }
            while (this.mDelayedStartList.size() > 0 && this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                r = (ServiceRecord) this.mDelayedStartList.remove(ActiveServices.SERVICE_TIMEOUT);
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    Slog.v(ActiveServices.TAG_SERVICE, "REM FR DELAY LIST (exec next): " + r);
                }
                if (r.pendingStarts.size() <= 0) {
                    Slog.w(ActiveServices.TAG, "**** NO PENDING STARTS! " + r + " startReq=" + r.startRequested + " delayedStop=" + r.delayedStop);
                } else {
                    if (ActiveServices.DEBUG_DELAYED_SERVICE && this.mDelayedStartList.size() > 0) {
                        Slog.v(ActiveServices.TAG_SERVICE, "Remaining delayed list:");
                        for (i = ActiveServices.SERVICE_TIMEOUT; i < this.mDelayedStartList.size(); i += MSG_BG_START_TIMEOUT) {
                            Slog.v(ActiveServices.TAG_SERVICE, "  #" + i + ": " + this.mDelayedStartList.get(i));
                        }
                    }
                    r.delayed = ActiveServices.SERVICE_RESCHEDULE;
                    try {
                        ProcessRecord servicePR = ActiveServices.this.mAm.getProcessRecordLocked(r.processName, r.appInfo.uid + r.appInfo.euid, ActiveServices.SERVICE_RESCHEDULE);
                        if (!(r.appInfo.uid < ActiveServices.SERVICE_MIN_RESTART_TIME_BETWEEN || servicePR == null || servicePR.thread == null)) {
                            LogPower.push(148, "serviceboot", r.packageName, Integer.toString(servicePR.pid));
                        }
                        ActiveServices.this.startServiceInnerLocked(this, ((StartItem) r.pendingStarts.get(ActiveServices.SERVICE_TIMEOUT)).intent, r, ActiveServices.SERVICE_RESCHEDULE, ActiveServices.LOG_SERVICE_START_STOP);
                    } catch (TransactionTooLargeException e) {
                    }
                }
            }
            if (this.mStartingBackground.size() > 0) {
                ServiceRecord next = (ServiceRecord) this.mStartingBackground.get(ActiveServices.SERVICE_TIMEOUT);
                long when = next.startingBgTimeout > now ? next.startingBgTimeout : now;
                if (ActiveServices.DEBUG_DELAYED_SERVICE) {
                    Slog.v(ActiveServices.TAG_SERVICE, "Top bg start is " + next + ", can delay others up to " + when);
                }
                sendMessageAtTime(obtainMessage(MSG_BG_START_TIMEOUT), when);
            }
            if (this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                int i2;
                ActivityManagerService activityManagerService = ActiveServices.this.mAm;
                if (this.mUserId == 2147383647) {
                    i2 = ActiveServices.SERVICE_TIMEOUT;
                } else {
                    i2 = this.mUserId;
                }
                activityManagerService.backgroundServicesFinishedLocked(i2);
            }
        }
    }

    private class ServiceRestarter implements Runnable {
        private ServiceRecord mService;

        private ServiceRestarter() {
        }

        void setService(ServiceRecord service) {
            this.mService = service;
        }

        public void run() {
            synchronized (ActiveServices.this.mAm) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActiveServices.this.performServiceRestartLocked(this.mService);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.ActiveServices.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.ActiveServices.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActiveServices.<clinit>():void");
    }

    public ActiveServices(ActivityManagerService service) {
        this.mServiceMap = new SparseArray();
        this.mServiceConnections = new ArrayMap();
        this.mPendingServices = new ArrayList();
        this.mRestartingServices = new ArrayList();
        this.mDestroyingServices = new ArrayList();
        this.mTmpCollectionResults = null;
        this.mLastAnrDumpClearer = new Runnable() {
            public void run() {
                synchronized (ActiveServices.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        ActiveServices.this.mLastAnrDump = null;
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        };
        this.mAm = service;
        int maxBg = SERVICE_TIMEOUT;
        try {
            maxBg = Integer.parseInt(SystemProperties.get("ro.config.max_starting_bg", "0"));
        } catch (RuntimeException e) {
        }
        if (maxBg <= 0) {
            maxBg = ActivityManager.isLowRamDeviceStatic() ? 1 : 8;
        }
        this.mMaxStartingBackground = maxBg;
    }

    ServiceRecord getServiceByName(ComponentName name, int callingUser) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "getServiceByName(" + name + "), callingUser = " + callingUser);
        }
        return (ServiceRecord) getServiceMap(callingUser).mServicesByName.get(name);
    }

    boolean hasBackgroundServices(int callingUser) {
        ServiceMap smap = (ServiceMap) this.mServiceMap.get(callingUser);
        if (smap == null || smap.mStartingBackground.size() < this.mMaxStartingBackground) {
            return SERVICE_RESCHEDULE;
        }
        return LOG_SERVICE_START_STOP;
    }

    private ServiceMap getServiceMap(int callingUser) {
        ServiceMap smap = (ServiceMap) this.mServiceMap.get(callingUser);
        if (smap != null) {
            return smap;
        }
        smap = new ServiceMap(this.mAm.mHandler.getLooper(), callingUser);
        this.mServiceMap.put(callingUser, smap);
        return smap;
    }

    ArrayMap<ComponentName, ServiceRecord> getServices(int callingUser) {
        return getServiceMap(callingUser).mServicesByName;
    }

    ComponentName startServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int callingPid, int callingUid, String callingPackage, int userId) throws TransactionTooLargeException {
        boolean callerFg;
        if (DEBUG_DELAYED_STARTS) {
            Slog.v(TAG_SERVICE, "startService: " + service + " type=" + resolvedType + " args=" + service.getExtras());
        }
        if (caller != null) {
            ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
            if (callerApp == null) {
                throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when starting service " + service);
            }
            callerFg = callerApp.setSchedGroup != 0 ? LOG_SERVICE_START_STOP : SERVICE_RESCHEDULE;
        } else {
            callerFg = LOG_SERVICE_START_STOP;
        }
        ServiceLookupResult res = retrieveServiceLocked(service, resolvedType, callingPackage, callingPid, callingUid, userId, LOG_SERVICE_START_STOP, callerFg, SERVICE_RESCHEDULE);
        if (res == null) {
            return null;
        }
        if (res.record == null) {
            return new ComponentName("!", res.permission != null ? res.permission : "private to package");
        }
        ServiceRecord r = res.record;
        if (this.mAm.mUserController.exists(r.userId)) {
            if (!r.startRequested) {
                long token = Binder.clearCallingIdentity();
                try {
                    if (this.mAm.checkAllowBackgroundLocked(r.appInfo.uid, r.packageName, callingPid, LOG_SERVICE_START_STOP) != 0) {
                        Slog.w(TAG, "Background start not allowed: service " + service + " to " + r.name.flattenToShortString() + " from pid=" + callingPid + " uid=" + callingUid + " pkg=" + callingPackage);
                        return null;
                    }
                    Binder.restoreCallingIdentity(token);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
            NeededUriGrants neededGrants = this.mAm.checkGrantUriPermissionFromIntentLocked(callingUid, r.packageName, service, service.getFlags(), null, r.userId);
            if (Build.PERMISSIONS_REVIEW_REQUIRED && !requestStartTargetPermissionsReviewIfNeededLocked(r, callingPackage, callingUid, service, callerFg, userId)) {
                return null;
            }
            int i;
            if (unscheduleServiceRestartLocked(r, callingUid, SERVICE_RESCHEDULE) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "START SERVICE WHILE RESTART PENDING: " + r);
            }
            r.lastActivity = SystemClock.uptimeMillis();
            r.startRequested = LOG_SERVICE_START_STOP;
            r.delayedStop = SERVICE_RESCHEDULE;
            r.pendingStarts.add(new StartItem(r, SERVICE_RESCHEDULE, r.makeNextStartId(), service, neededGrants));
            if (r.appInfo.euid == 0) {
                i = r.userId;
            } else {
                i = r.appInfo.euid;
            }
            ServiceMap smap = getServiceMap(i);
            boolean addToStarting = SERVICE_RESCHEDULE;
            if (!callerFg && r.app == null && this.mAm.mUserController.hasStartedUserState(r.userId)) {
                ProcessRecord proc = this.mAm.getProcessRecordLocked(r.processName, r.appInfo.uid + r.appInfo.euid, SERVICE_RESCHEDULE);
                if (proc == null || proc.curProcState > 11) {
                    if (DEBUG_DELAYED_SERVICE) {
                        Slog.v(TAG_SERVICE, "Potential start delay of " + r + " in " + proc);
                    }
                    if (r.delayed) {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(TAG_SERVICE, "Continuing to delay: " + r);
                        }
                        return r.name;
                    } else if (smap.mStartingBackground.size() >= this.mMaxStartingBackground) {
                        Slog.i(TAG_SERVICE, "Delaying start of: " + r);
                        smap.mDelayedStartList.add(r);
                        r.delayed = LOG_SERVICE_START_STOP;
                        return r.name;
                    } else {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(TAG_SERVICE, "Not delaying: " + r);
                        }
                        addToStarting = LOG_SERVICE_START_STOP;
                    }
                } else if (proc.curProcState >= 10) {
                    addToStarting = LOG_SERVICE_START_STOP;
                    if (DEBUG_DELAYED_STARTS) {
                        Slog.v(TAG_SERVICE, "Not delaying, but counting as bg: " + r);
                    }
                } else if (DEBUG_DELAYED_STARTS) {
                    StringBuilder stringBuilder = new StringBuilder(DumpState.DUMP_PACKAGES);
                    stringBuilder.append("Not potential delay (state=").append(proc.curProcState).append(' ').append(proc.adjType);
                    String reason = proc.makeAdjReason();
                    if (reason != null) {
                        stringBuilder.append(' ');
                        stringBuilder.append(reason);
                    }
                    stringBuilder.append("): ");
                    stringBuilder.append(r.toString());
                    Slog.v(TAG_SERVICE, stringBuilder.toString());
                }
            } else if (DEBUG_DELAYED_STARTS) {
                if (callerFg) {
                    Slog.v(TAG_SERVICE, "Not potential delay (callerFg=" + callerFg + " uid=" + callingUid + " pid=" + callingPid + "): " + r);
                } else if (r.app != null) {
                    Slog.v(TAG_SERVICE, "Not potential delay (cur app=" + r.app + "): " + r);
                } else {
                    Slog.v(TAG_SERVICE, "Not potential delay (user " + r.userId + " not started): " + r);
                }
            }
            if (r.appInfo.uid >= SERVICE_MIN_RESTART_TIME_BETWEEN && callingPackage != null) {
                if (!callingPackage.equals(r.packageName)) {
                    LogPower.push(148, "serviceboot", r.packageName, Integer.toString(SERVICE_TIMEOUT), new String[]{callingPackage});
                }
            }
            return startServiceInnerLocked(smap, service, r, callerFg, addToStarting);
        }
        Slog.w(TAG, "Trying to start service with non-existent user! " + r.userId);
        return null;
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(ServiceRecord r, String callingPackage, int callingUid, Intent service, boolean callerFg, int userId) {
        if (!this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(r.packageName, r.userId)) {
            return LOG_SERVICE_START_STOP;
        }
        if (callerFg) {
            ActivityManagerService activityManagerService = this.mAm;
            Intent[] intentArr = new Intent[]{service};
            String[] strArr = new String[1];
            strArr[SERVICE_TIMEOUT] = service.resolveType(this.mAm.mContext.getContentResolver());
            IIntentSender target = activityManagerService.getIntentSenderLocked(SERVICE_RESTART_DURATION_FACTOR, callingPackage, callingUid, userId, null, null, SERVICE_TIMEOUT, intentArr, strArr, 1409286144, null);
            Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent.addFlags(276824064);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", r.packageName);
            intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                Slog.i(TAG, "u" + r.userId + " Launching permission review for package " + r.packageName);
            }
            this.mAm.mHandler.post(new AnonymousClass2(intent, userId));
            return SERVICE_RESCHEDULE;
        }
        Slog.w(TAG, "u" + r.userId + " Starting a service in package" + r.packageName + " requires a permissions review");
        return SERVICE_RESCHEDULE;
    }

    ComponentName startServiceInnerLocked(ServiceMap smap, Intent service, ServiceRecord r, boolean callerFg, boolean addToStarting) throws TransactionTooLargeException {
        ServiceState stracker = r.getTracker();
        if (stracker != null) {
            stracker.setStarted(LOG_SERVICE_START_STOP, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
        }
        r.callStart = SERVICE_RESCHEDULE;
        synchronized (r.stats.getBatteryStats()) {
            r.stats.startRunningLocked();
        }
        String error = bringUpServiceLocked(r, service.getFlags(), callerFg, SERVICE_RESCHEDULE, SERVICE_RESCHEDULE);
        if (error != null) {
            return new ComponentName("!!", error);
        }
        if (r.startRequested && addToStarting) {
            boolean first = smap.mStartingBackground.size() == 0 ? LOG_SERVICE_START_STOP : SERVICE_RESCHEDULE;
            smap.mStartingBackground.add(r);
            r.startingBgTimeout = SystemClock.uptimeMillis() + ((long) BG_START_TIMEOUT);
            if (DEBUG_DELAYED_SERVICE) {
                RuntimeException here = new RuntimeException("here");
                here.fillInStackTrace();
                Slog.v(TAG_SERVICE, "Starting background (first=" + first + "): " + r, here);
            } else if (DEBUG_DELAYED_STARTS) {
                Slog.v(TAG_SERVICE, "Starting background (first=" + first + "): " + r);
            }
            if (first) {
                smap.rescheduleDelayedStarts();
            }
        } else if (callerFg) {
            smap.ensureNotStartingBackground(r);
        }
        return r.name;
    }

    private void stopServiceLocked(ServiceRecord service) {
        if (service.delayed) {
            if (DEBUG_DELAYED_STARTS) {
                Slog.v(TAG_SERVICE, "Delaying stop of pending: " + service);
            }
            service.delayedStop = LOG_SERVICE_START_STOP;
            return;
        }
        synchronized (service.stats.getBatteryStats()) {
            service.stats.stopRunningLocked();
        }
        service.startRequested = SERVICE_RESCHEDULE;
        if (service.tracker != null) {
            service.tracker.setStarted(SERVICE_RESCHEDULE, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
        }
        service.callStart = SERVICE_RESCHEDULE;
        bringDownServiceIfNeededLocked(service, SERVICE_RESCHEDULE, SERVICE_RESCHEDULE);
    }

    int stopServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int userId) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "stopService: " + service + " type=" + resolvedType);
        }
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
        if (caller == null || callerApp != null) {
            ServiceLookupResult r = retrieveServiceLocked(service, resolvedType, null, Binder.getCallingPid(), Binder.getCallingUid(), userId, SERVICE_RESCHEDULE, SERVICE_RESCHEDULE, SERVICE_RESCHEDULE);
            if (r == null) {
                return SERVICE_TIMEOUT;
            }
            if (r.record == null) {
                return -1;
            }
            long origId = Binder.clearCallingIdentity();
            try {
                stopServiceLocked(r.record);
                return 1;
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when stopping service " + service);
        }
    }

    void stopInBackgroundLocked(int uid) {
        ServiceMap services = (ServiceMap) this.mServiceMap.get(UserHandle.getUserId(uid));
        ArrayList stopping = null;
        if (services != null) {
            int i;
            ServiceRecord service;
            for (i = services.mServicesByName.size() - 1; i >= 0; i--) {
                service = (ServiceRecord) services.mServicesByName.valueAt(i);
                if (service.appInfo.uid == uid && service.startRequested && this.mAm.mAppOpsService.noteOperation(63, uid, service.packageName) != 0 && stopping == null) {
                    stopping = new ArrayList();
                    stopping.add(service);
                }
            }
            if (stopping != null) {
                for (i = stopping.size() - 1; i >= 0; i--) {
                    service = (ServiceRecord) stopping.get(i);
                    service.delayed = SERVICE_RESCHEDULE;
                    services.ensureNotStartingBackground(service);
                    stopServiceLocked(service);
                }
            }
        }
    }

    IBinder peekServiceLocked(Intent service, String resolvedType, String callingPackage) {
        ServiceLookupResult r = retrieveServiceLocked(service, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), UserHandle.getCallingUserId(), SERVICE_RESCHEDULE, SERVICE_RESCHEDULE, SERVICE_RESCHEDULE);
        if (r == null) {
            return null;
        }
        if (r.record == null) {
            throw new SecurityException("Permission Denial: Accessing service from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + r.permission);
        }
        IntentBindRecord ib = (IntentBindRecord) r.record.bindings.get(r.record.intent);
        if (ib != null) {
            return ib.binder;
        }
        return null;
    }

    boolean stopServiceTokenLocked(ComponentName className, IBinder token, int startId) {
        ProcessRecord callerApp = (ProcessRecord) this.mAm.mPidsSelfLocked.get(Binder.getCallingPid());
        int euid = callerApp != null ? callerApp.info.euid : SERVICE_TIMEOUT;
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "stopServiceToken: " + className + " " + token + " startId=" + startId + ", callingPid: " + Binder.getCallingPid() + ", euid: " + euid);
        }
        if (euid == 0) {
            euid = UserHandle.getCallingUserId();
        }
        ServiceRecord r = findServiceLocked(className, token, euid);
        if (r == null) {
            return SERVICE_RESCHEDULE;
        }
        if (startId >= 0) {
            StartItem si = r.findDeliveredStart(startId, SERVICE_RESCHEDULE);
            if (si != null) {
                while (r.deliveredStarts.size() > 0) {
                    StartItem cur = (StartItem) r.deliveredStarts.remove(SERVICE_TIMEOUT);
                    cur.removeUriPermissionsLocked();
                    if (cur == si) {
                        break;
                    }
                }
            }
            if (r.getLastStartId() != startId) {
                return SERVICE_RESCHEDULE;
            }
            if (r.deliveredStarts.size() > 0) {
                Slog.w(TAG, "stopServiceToken startId " + startId + " is last, but have " + r.deliveredStarts.size() + " remaining args");
            }
        }
        synchronized (r.stats.getBatteryStats()) {
            r.stats.stopRunningLocked();
        }
        r.startRequested = SERVICE_RESCHEDULE;
        if (r.tracker != null) {
            r.tracker.setStarted(SERVICE_RESCHEDULE, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
        }
        r.callStart = SERVICE_RESCHEDULE;
        long origId = Binder.clearCallingIdentity();
        bringDownServiceIfNeededLocked(r, SERVICE_RESCHEDULE, SERVICE_RESCHEDULE);
        Binder.restoreCallingIdentity(origId);
        return LOG_SERVICE_START_STOP;
    }

    public void setServiceForegroundLocked(ComponentName className, IBinder token, int id, Notification notification, int flags) {
        int userId = UserHandle.getCallingUserId();
        long origId = Binder.clearCallingIdentity();
        try {
            ServiceRecord r = findServiceLocked(className, token, userId);
            if (r != null) {
                if (id == 0) {
                    if (r.isForeground) {
                        r.isForeground = SERVICE_RESCHEDULE;
                        if (r.app != null) {
                            this.mAm.updateLruProcessLocked(r.app, SERVICE_RESCHEDULE, null);
                            updateServiceForegroundLocked(r.app, LOG_SERVICE_START_STOP);
                        }
                    }
                    if ((flags & 1) != 0) {
                        r.cancelNotification();
                        r.foregroundId = SERVICE_TIMEOUT;
                        r.foregroundNoti = null;
                    } else if (r.appInfo.targetSdkVersion >= 21) {
                        r.stripForegroundServiceFlagFromNotification();
                        if ((flags & 2) != 0) {
                            r.foregroundId = SERVICE_TIMEOUT;
                            r.foregroundNoti = null;
                        }
                    }
                } else if (notification == null) {
                    throw new IllegalArgumentException("null notification");
                } else {
                    if (r.foregroundId != id) {
                        r.cancelNotification();
                        r.foregroundId = id;
                    }
                    notification.flags |= 64;
                    r.foregroundNoti = notification;
                    r.isForeground = LOG_SERVICE_START_STOP;
                    r.postNotification();
                    if (r.app != null) {
                        updateServiceForegroundLocked(r.app, LOG_SERVICE_START_STOP);
                    }
                    getServiceMap(r.appInfo.euid == 0 ? r.userId : r.appInfo.euid).ensureNotStartingBackground(r);
                    this.mAm.notifyPackageUse(r.serviceInfo.packageName, 2);
                }
            }
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private void updateServiceForegroundLocked(ProcessRecord proc, boolean oomAdj) {
        boolean anyForeground = SERVICE_RESCHEDULE;
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            if (((ServiceRecord) proc.services.valueAt(i)).isForeground) {
                anyForeground = LOG_SERVICE_START_STOP;
                break;
            }
        }
        this.mAm.updateProcessForegroundLocked(proc, anyForeground, oomAdj);
    }

    private void updateWhitelistManagerLocked(ProcessRecord proc) {
        proc.whitelistManager = SERVICE_RESCHEDULE;
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            if (((ServiceRecord) proc.services.valueAt(i)).whitelistManager) {
                proc.whitelistManager = LOG_SERVICE_START_STOP;
                return;
            }
        }
    }

    public void updateServiceConnectionActivitiesLocked(ProcessRecord clientProc) {
        if (clientProc != null && clientProc.connections != null) {
            ArraySet updatedProcesses = null;
            for (int i = SERVICE_TIMEOUT; i < clientProc.connections.size(); i++) {
                ProcessRecord proc = ((ConnectionRecord) clientProc.connections.valueAt(i)).binding.service.app;
                if (!(proc == null || proc == clientProc)) {
                    if (updatedProcesses == null) {
                        updatedProcesses = new ArraySet();
                    } else if (updatedProcesses.contains(proc)) {
                    }
                    updatedProcesses.add(proc);
                    updateServiceClientActivitiesLocked(proc, null, SERVICE_RESCHEDULE);
                }
            }
        }
    }

    private boolean updateServiceClientActivitiesLocked(ProcessRecord proc, ConnectionRecord modCr, boolean updateLru) {
        if (modCr != null && modCr.binding.client != null && modCr.binding.client.activities.size() <= 0) {
            return SERVICE_RESCHEDULE;
        }
        boolean anyClientActivities = SERVICE_RESCHEDULE;
        for (int i = proc.services.size() - 1; i >= 0 && !anyClientActivities; i--) {
            ServiceRecord sr = (ServiceRecord) proc.services.valueAt(i);
            for (int conni = sr.connections.size() - 1; conni >= 0 && !anyClientActivities; conni--) {
                ArrayList<ConnectionRecord> clist = (ArrayList) sr.connections.valueAt(conni);
                for (int cri = clist.size() - 1; cri >= 0; cri--) {
                    ConnectionRecord cr = (ConnectionRecord) clist.get(cri);
                    if (cr.binding.client != null && cr.binding.client != proc && cr.binding.client.activities.size() > 0) {
                        anyClientActivities = LOG_SERVICE_START_STOP;
                        break;
                    }
                }
            }
        }
        if (anyClientActivities == proc.hasClientActivities) {
            return SERVICE_RESCHEDULE;
        }
        proc.hasClientActivities = anyClientActivities;
        if (updateLru) {
            this.mAm.updateLruProcessLocked(proc, anyClientActivities, null);
        }
        return LOG_SERVICE_START_STOP;
    }

    int bindServiceLocked(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws TransactionTooLargeException {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "bindService: " + service + " type=" + resolvedType + " conn=" + connection.asBinder() + " flags=0x" + Integer.toHexString(flags));
        }
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
        if (callerApp == null) {
            throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when binding service " + service);
        }
        ActivityRecord activityRecord = null;
        if (token != null) {
            activityRecord = ActivityRecord.isInStackLocked(token);
            if (activityRecord == null) {
                Slog.w(TAG, "Binding with unknown activity: " + token);
                return SERVICE_TIMEOUT;
            }
        }
        int clientLabel = SERVICE_TIMEOUT;
        PendingIntent pendingIntent = null;
        boolean isCallerSystem = callerApp.info.uid == SERVICE_RESTART_DURATION ? LOG_SERVICE_START_STOP : SERVICE_RESCHEDULE;
        if (isCallerSystem) {
            service.setDefusable(LOG_SERVICE_START_STOP);
            pendingIntent = (PendingIntent) service.getParcelableExtra("android.intent.extra.client_intent");
            if (pendingIntent != null) {
                clientLabel = service.getIntExtra("android.intent.extra.client_label", SERVICE_TIMEOUT);
                if (clientLabel != 0) {
                    service = service.cloneFilter();
                }
            }
        }
        if ((134217728 & flags) != 0) {
            this.mAm.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "BIND_TREAT_LIKE_ACTIVITY");
        }
        if ((16777216 & flags) == 0 || isCallerSystem) {
            boolean callerFg = callerApp.setSchedGroup != 0 ? LOG_SERVICE_START_STOP : SERVICE_RESCHEDULE;
            ServiceLookupResult res = retrieveServiceLocked(service, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), userId, LOG_SERVICE_START_STOP, callerFg, (UsbAudioDevice.kAudioDeviceMeta_Alsa & flags) != 0 ? LOG_SERVICE_START_STOP : SERVICE_RESCHEDULE);
            if (res == null) {
                return SERVICE_TIMEOUT;
            }
            if (res.record == null) {
                return -1;
            }
            int i;
            ServiceRecord s = res.record;
            boolean permissionsReviewRequired = SERVICE_RESCHEDULE;
            if (Build.PERMISSIONS_REVIEW_REQUIRED && this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(s.packageName, s.userId)) {
                permissionsReviewRequired = LOG_SERVICE_START_STOP;
                if (callerFg) {
                    ServiceRecord serviceRecord = s;
                    RemoteCallback remoteCallback = new RemoteCallback(new AnonymousClass3(s, service, callerFg, connection));
                    Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                    intent.addFlags(276824064);
                    intent.putExtra("android.intent.extra.PACKAGE_NAME", s.packageName);
                    intent.putExtra("android.intent.extra.REMOTE_CALLBACK", remoteCallback);
                    if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                        Slog.i(TAG, "u" + s.userId + " Launching permission review for package " + s.packageName);
                    }
                    this.mAm.mHandler.post(new AnonymousClass4(intent, userId));
                } else {
                    Slog.w(TAG, "u" + s.userId + " Binding to a service in package" + s.packageName + " requires a permissions review");
                    return SERVICE_TIMEOUT;
                }
            }
            long origId = Binder.clearCallingIdentity();
            if (unscheduleServiceRestartLocked(s, callerApp.info.uid, SERVICE_RESCHEDULE) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "BIND SERVICE WHILE RESTART PENDING: " + s);
            }
            if ((flags & 1) != 0) {
                s.lastActivity = SystemClock.uptimeMillis();
                if (!s.hasAutoCreateConnections()) {
                    ServiceState stracker = s.getTracker();
                    if (stracker != null) {
                        stracker.setBound(LOG_SERVICE_START_STOP, this.mAm.mProcessStats.getMemFactorLocked(), s.lastActivity);
                    }
                }
            }
            this.mAm.startAssociationLocked(callerApp.uid, callerApp.processName, callerApp.curProcState, s.appInfo.uid, s.name, s.processName);
            this.mAm.reportServiceRelationIAware(1, s, callerApp);
            AppBindRecord b = s.retrieveAppBindingLocked(service, callerApp);
            ConnectionRecord c = new ConnectionRecord(b, activityRecord, connection, flags, clientLabel, pendingIntent);
            IBinder binder = connection.asBinder();
            ArrayList<ConnectionRecord> clist = (ArrayList) s.connections.get(binder);
            if (clist == null) {
                clist = new ArrayList();
                s.connections.put(binder, clist);
            }
            clist.add(c);
            b.connections.add(c);
            if (activityRecord != null) {
                if (activityRecord.connections == null) {
                    activityRecord.connections = new HashSet();
                }
                activityRecord.connections.add(c);
            }
            b.client.connections.add(c);
            this.mAm.smartTrimAddProcessRelation_HwSysM(b, c.binding);
            if ((c.flags & 8) != 0) {
                b.client.hasAboveClient = LOG_SERVICE_START_STOP;
            }
            if ((c.flags & 16777216) != 0) {
                s.whitelistManager = LOG_SERVICE_START_STOP;
            }
            if (s.app != null) {
                updateServiceClientActivitiesLocked(s.app, c, LOG_SERVICE_START_STOP);
            }
            clist = (ArrayList) this.mServiceConnections.get(binder);
            if (clist == null) {
                clist = new ArrayList();
                this.mServiceConnections.put(binder, clist);
            }
            clist.add(c);
            if ((flags & 1) != 0) {
                s.lastActivity = SystemClock.uptimeMillis();
                if (bringUpServiceLocked(s, service.getFlags(), callerFg, SERVICE_RESCHEDULE, permissionsReviewRequired) != null) {
                    Binder.restoreCallingIdentity(origId);
                    return SERVICE_TIMEOUT;
                }
            }
            try {
                if (s.app != null) {
                    boolean z;
                    if ((134217728 & flags) != 0) {
                        s.app.treatLikeActivity = LOG_SERVICE_START_STOP;
                    }
                    if (s.whitelistManager) {
                        s.app.whitelistManager = LOG_SERVICE_START_STOP;
                    }
                    ActivityManagerService activityManagerService = this.mAm;
                    ProcessRecord processRecord = s.app;
                    if (s.app.hasClientActivities) {
                        z = LOG_SERVICE_START_STOP;
                    } else {
                        z = s.app.treatLikeActivity;
                    }
                    activityManagerService.updateLruProcessLocked(processRecord, z, b.client);
                    this.mAm.updateOomAdjLocked(s.app);
                }
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Bind " + s + " with " + b + ": received=" + b.intent.received + " apps=" + b.intent.apps.size() + " doRebind=" + b.intent.doRebind);
                }
                if (s.app != null && b.intent.received) {
                    c.conn.connected(s.name, b.intent.binder);
                    if (b.intent.apps.size() == 1 && b.intent.doRebind) {
                        requestServiceBindingLocked(s, b.intent, callerFg, LOG_SERVICE_START_STOP);
                    }
                } else if (!b.intent.requested) {
                    requestServiceBindingLocked(s, b.intent, callerFg, SERVICE_RESCHEDULE);
                }
            } catch (Throwable e) {
                Slog.w(TAG, "Failure sending service " + s.shortName + " to connection " + c.conn.asBinder() + " (in " + c.binding.client.processName + ")", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
            if (s.appInfo.euid == 0) {
                i = s.userId;
            } else {
                i = s.appInfo.euid;
            }
            getServiceMap(i).ensureNotStartingBackground(s);
            Binder.restoreCallingIdentity(origId);
            ProcessRecord app = s.app;
            if (app == null && s.processName != null) {
                app = this.mAm.getProcessRecordLocked(s.processName, s.appInfo.uid + s.appInfo.euid, LOG_SERVICE_START_STOP);
            }
            if (!(app == null || app.uid < SERVICE_MIN_RESTART_TIME_BETWEEN || callerApp.pid == app.pid || callerApp.info == null || app.info == null || callerApp.info.packageName == null || callerApp.info.packageName.equals(app.info.packageName))) {
                LogPower.push(148, "bindservice", s.packageName, Integer.toString(app.pid));
                LogPower.push(166, s.processName, Integer.toString(callerApp.pid), Integer.toString(app.pid), new String[]{"service"});
            }
            return 1;
        }
        throw new SecurityException("Non-system caller " + caller + " (pid=" + Binder.getCallingPid() + ") set BIND_ALLOW_WHITELIST_MANAGEMENT when binding service " + service);
    }

    private void foo() {
    }

    void publishServiceLocked(ServiceRecord r, Intent intent, IBinder service) {
        long origId = Binder.clearCallingIdentity();
        ConnectionRecord c;
        try {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "PUBLISHING " + r + " " + intent + ": " + service);
            }
            if (r != null) {
                FilterComparison filter = new FilterComparison(intent);
                IntentBindRecord b = (IntentBindRecord) r.bindings.get(filter);
                if (!(b == null || b.received)) {
                    b.binder = service;
                    b.requested = LOG_SERVICE_START_STOP;
                    b.received = LOG_SERVICE_START_STOP;
                    for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
                        ArrayList<ConnectionRecord> clist = (ArrayList) r.connections.valueAt(conni);
                        for (int i = SERVICE_TIMEOUT; i < clist.size(); i++) {
                            c = (ConnectionRecord) clist.get(i);
                            if (filter.equals(c.binding.intent.intent)) {
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(TAG_SERVICE, "Publishing to: " + c);
                                }
                                c.conn.connected(r.name, service);
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(TAG_SERVICE, "Not publishing to: " + c);
                                }
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(TAG_SERVICE, "Bound intent: " + c.binding.intent.intent);
                                }
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(TAG_SERVICE, "Published intent: " + intent);
                                }
                            }
                        }
                    }
                }
                serviceDoneExecutingLocked(r, this.mDestroyingServices.contains(r), SERVICE_RESCHEDULE);
            }
            Binder.restoreCallingIdentity(origId);
        } catch (Exception e) {
            Slog.w(TAG, "Failure sending service " + r.name + " to connection " + c.conn.asBinder() + " (in " + c.binding.client.processName + ")", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    boolean unbindServiceLocked(IServiceConnection connection) {
        IBinder binder = connection.asBinder();
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "unbindService: conn=" + binder);
        }
        ArrayList<ConnectionRecord> clist = (ArrayList) this.mServiceConnections.get(binder);
        if (clist == null) {
            Slog.w(TAG, "Unbind failed: could not find connection for " + connection.asBinder());
            return SERVICE_RESCHEDULE;
        }
        long origId = Binder.clearCallingIdentity();
        while (clist.size() > 0) {
            try {
                ConnectionRecord r = (ConnectionRecord) clist.get(SERVICE_TIMEOUT);
                removeConnectionLocked(r, null, null);
                if (clist.size() > 0 && clist.get(SERVICE_TIMEOUT) == r) {
                    Slog.wtf(TAG, "Connection " + r + " not removed for binder " + binder);
                    clist.remove(SERVICE_TIMEOUT);
                }
                if (r.binding.service.app != null) {
                    if (r.binding.service.app.whitelistManager) {
                        updateWhitelistManagerLocked(r.binding.service.app);
                    }
                    if ((r.flags & 134217728) != 0) {
                        boolean z;
                        r.binding.service.app.treatLikeActivity = LOG_SERVICE_START_STOP;
                        ActivityManagerService activityManagerService = this.mAm;
                        ProcessRecord processRecord = r.binding.service.app;
                        if (r.binding.service.app.hasClientActivities) {
                            z = LOG_SERVICE_START_STOP;
                        } else {
                            z = r.binding.service.app.treatLikeActivity;
                        }
                        activityManagerService.updateLruProcessLocked(processRecord, z, null);
                    }
                    this.mAm.updateOomAdjLocked(r.binding.service.app);
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
        return LOG_SERVICE_START_STOP;
    }

    void unbindFinishedLocked(ServiceRecord r, Intent intent, boolean doRebind) {
        long origId = Binder.clearCallingIdentity();
        if (r != null) {
            try {
                IntentBindRecord b = (IntentBindRecord) r.bindings.get(new FilterComparison(intent));
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "unbindFinished in " + r + " at " + b + ": apps=" + (b != null ? b.apps.size() : SERVICE_TIMEOUT));
                }
                boolean inDestroying = this.mDestroyingServices.contains(r);
                if (b != null) {
                    if (b.apps.size() <= 0 || inDestroying) {
                        b.doRebind = LOG_SERVICE_START_STOP;
                    } else {
                        boolean inFg = SERVICE_RESCHEDULE;
                        for (int i = b.apps.size() - 1; i >= 0; i--) {
                            ProcessRecord client = ((AppBindRecord) b.apps.valueAt(i)).client;
                            if (client != null && client.setSchedGroup != 0) {
                                inFg = LOG_SERVICE_START_STOP;
                                break;
                            }
                        }
                        try {
                            requestServiceBindingLocked(r, b, inFg, LOG_SERVICE_START_STOP);
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
                serviceDoneExecutingLocked(r, inDestroying, SERVICE_RESCHEDULE);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    private final ServiceRecord findServiceLocked(ComponentName name, IBinder token, int userId) {
        IBinder r = getServiceByName(name, userId);
        return r == token ? r : null;
    }

    private ServiceLookupResult retrieveServiceLocked(Intent service, String resolvedType, String callingPackage, int callingPid, int callingUid, int userId, boolean createIfNeeded, boolean callingFromFg, boolean isBindExternal) {
        ComponentName comp;
        ServiceRecord r;
        int flags;
        ServiceInfo sInfo;
        ServiceInfo sInfo2;
        ComponentName name;
        ApplicationInfo aInfo;
        ServiceRecord r2;
        BatteryStatsImpl stats;
        Serv ss;
        int i;
        int opCode;
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "retrieveServiceLocked: " + service + " type=" + resolvedType + " callingUid=" + callingUid);
        }
        userId = this.mAm.mUserController.handleIncomingUser(callingPid, callingUid, userId, SERVICE_RESCHEDULE, 1, "service", null);
        ProcessRecord callerApp = (ProcessRecord) this.mAm.mPidsSelfLocked.get(callingPid);
        ServiceMap smap = getServiceMap(userId);
        boolean isServiceToBeClone = SERVICE_RESCHEDULE;
        if (callerApp == null || callerApp.info.euid == 0) {
            if ((service.getHwFlags() & 1) != 0) {
            }
            comp = service.getComponent();
            if (comp == null) {
                r = (ServiceRecord) smap.mServicesByName.get(comp);
            } else {
                r = null;
            }
            if (r == null && !isBindExternal) {
                r = (ServiceRecord) smap.mServicesByIntent.get(new FilterComparison(service));
            }
            if (!(r == null || (r.serviceInfo.flags & SERVICE_RESTART_DURATION_FACTOR) == 0)) {
                if (!callingPackage.equals(r.packageName)) {
                    r = null;
                }
            }
            if (r == null) {
                flags = 268436480;
                if (isServiceToBeClone) {
                    flags = 272630784;
                }
                try {
                    Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "retrieveServiceLocked, callerApp: " + callerApp + ", flags: " + Integer.toHexString(flags));
                    ResolveInfo rInfo = AppGlobals.getPackageManager().resolveService(service, resolvedType, flags, userId);
                    sInfo = rInfo == null ? rInfo.serviceInfo : null;
                    if (sInfo != null) {
                        sInfo2 = sInfo;
                    } else if (this.mAm.shouldPreventStartService(sInfo, callingPid, callingUid)) {
                        sInfo2 = sInfo;
                    } else {
                        Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "prevent by hsm Will not start service " + service + " U=" + userId + ": force null");
                        sInfo2 = null;
                    }
                    if (sInfo2 != null) {
                        Slog.w(TAG_SERVICE, "Unable to start service " + service + " U=" + userId + ": not found");
                        return null;
                    }
                    name = new ComponentName(sInfo2.applicationInfo.packageName, sInfo2.name);
                    if ((sInfo2.flags & SERVICE_RESTART_DURATION_FACTOR) == 0) {
                        if (isBindExternal) {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE required for " + name);
                        } else if (!sInfo2.exported) {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not exported");
                        } else if ((sInfo2.flags & 2) != 0) {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an isolatedProcess");
                        } else {
                            aInfo = AppGlobals.getPackageManager().getApplicationInfo(callingPackage, DumpState.DUMP_PROVIDERS, userId);
                            if (aInfo != null) {
                                throw new SecurityException("BIND_EXTERNAL_SERVICE failed, could not resolve client package " + callingPackage);
                            }
                            sInfo = new ServiceInfo(sInfo2);
                            sInfo.applicationInfo = new ApplicationInfo(sInfo.applicationInfo);
                            sInfo.applicationInfo.packageName = aInfo.packageName;
                            sInfo.applicationInfo.uid = aInfo.uid;
                            ComponentName componentName = new ComponentName(aInfo.packageName, name.getClassName());
                            service.setComponent(componentName);
                            name = componentName;
                            sInfo2 = sInfo;
                        }
                    } else if (isBindExternal) {
                        throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an externalService");
                    }
                    if (userId <= 0) {
                        if (this.mAm.isSingleton(sInfo2.processName, sInfo2.applicationInfo, sInfo2.name, sInfo2.flags)) {
                            if (this.mAm.isValidSingletonCall(callingUid, sInfo2.applicationInfo.uid)) {
                                userId = SERVICE_TIMEOUT;
                                smap = getServiceMap(SERVICE_TIMEOUT);
                            }
                        }
                        sInfo = new ServiceInfo(sInfo2);
                        sInfo.applicationInfo = this.mAm.getAppInfoForUser(sInfo.applicationInfo, userId);
                    } else {
                        sInfo = sInfo2;
                    }
                    r2 = (ServiceRecord) smap.mServicesByName.get(name);
                    if (r2 == null) {
                        r = r2;
                    } else if (createIfNeeded) {
                        r = r2;
                    } else {
                        try {
                            FilterComparison filter = new FilterComparison(service.cloneFilter());
                            ActiveServices activeServices = this;
                            ServiceRestarter res = new ServiceRestarter();
                            stats = this.mAm.mBatteryStatsService.getActiveStatistics();
                            synchronized (stats) {
                                ss = stats.getServiceStatsLocked(sInfo.applicationInfo.uid, sInfo.packageName, sInfo.name);
                            }
                            r = new ServiceRecord(this.mAm, ss, name, filter, sInfo, callingFromFg, res);
                            res.setService(r);
                            smap.mServicesByName.put(name, r);
                            smap.mServicesByIntent.put(filter, r);
                            for (i = this.mPendingServices.size() - 1; i >= 0; i--) {
                                ServiceRecord pr = (ServiceRecord) this.mPendingServices.get(i);
                                if (pr.serviceInfo.applicationInfo.uid == sInfo.applicationInfo.uid && pr.serviceInfo.applicationInfo.euid == sInfo.applicationInfo.euid && pr.name.equals(name)) {
                                    this.mPendingServices.remove(i);
                                }
                            }
                        } catch (RemoteException e) {
                            r = r2;
                        }
                    }
                } catch (RemoteException e2) {
                }
            }
            if (r == null) {
                if (this.mAm.checkComponentPermission(r.permission, callingPid, callingUid, r.appInfo.uid, r.exported) != 0) {
                    if (!(r.permission == null || callingPackage == null)) {
                        opCode = AppOpsManager.permissionToOpCode(r.permission);
                        if (!(opCode == -1 || this.mAm.mAppOpsService.noteOperation(opCode, callingUid, callingPackage) == 0)) {
                            Slog.w(TAG, "Appop Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " requires appop " + AppOpsManager.opToName(opCode));
                            return null;
                        }
                    }
                    if (!this.mAm.mIntentFirewall.checkService(r.name, service, callingUid, callingPid, resolvedType, r.appInfo)) {
                        Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "prevent by firewall Unable to start service " + service + " U=" + userId + ": force null");
                        return null;
                    } else if (!this.mAm.shouldPreventStartService(r.serviceInfo, callingUid, callingPid, callingPackage, userId)) {
                        return new ServiceLookupResult(r, null);
                    } else {
                        Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "prevent by trustspace Unable to start service " + service + " U=" + userId + ": force null");
                        return null;
                    }
                } else if (r.exported) {
                    Slog.w(TAG, "Permission Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " that is not exported from uid " + r.appInfo.uid);
                    return new ServiceLookupResult(null, "not exported from uid " + r.appInfo.uid);
                } else {
                    Slog.w(TAG, "Permission Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " requires " + r.permission);
                    return new ServiceLookupResult(null, r.permission);
                }
            }
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "retrieve service " + service + " U=" + userId + ": ret null");
            return null;
        }
        try {
            rInfo = AppGlobals.getPackageManager().resolveService(service, resolvedType, SERVICE_TIMEOUT, userId);
            if (rInfo != null && this.mAm.isPackageCloned(rInfo.serviceInfo.packageName, userId)) {
                smap = getServiceMap(2147383647);
                isServiceToBeClone = LOG_SERVICE_START_STOP;
            }
        } catch (Throwable e3) {
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "resolve service error", e3);
        }
        comp = service.getComponent();
        if (comp == null) {
            r = null;
        } else {
            r = (ServiceRecord) smap.mServicesByName.get(comp);
        }
        r = (ServiceRecord) smap.mServicesByIntent.get(new FilterComparison(service));
        if (callingPackage.equals(r.packageName)) {
            r = null;
        }
        if (r == null) {
            flags = 268436480;
            if (isServiceToBeClone) {
                flags = 272630784;
            }
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "retrieveServiceLocked, callerApp: " + callerApp + ", flags: " + Integer.toHexString(flags));
            ResolveInfo rInfo2 = AppGlobals.getPackageManager().resolveService(service, resolvedType, flags, userId);
            if (rInfo2 == null) {
            }
            if (sInfo != null) {
                sInfo2 = sInfo;
            } else if (this.mAm.shouldPreventStartService(sInfo, callingPid, callingUid)) {
                sInfo2 = sInfo;
            } else {
                Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "prevent by hsm Will not start service " + service + " U=" + userId + ": force null");
                sInfo2 = null;
            }
            if (sInfo2 != null) {
                name = new ComponentName(sInfo2.applicationInfo.packageName, sInfo2.name);
                if ((sInfo2.flags & SERVICE_RESTART_DURATION_FACTOR) == 0) {
                    if (isBindExternal) {
                        throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an externalService");
                    }
                } else if (isBindExternal) {
                    throw new SecurityException("BIND_EXTERNAL_SERVICE required for " + name);
                } else if (!sInfo2.exported) {
                    throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not exported");
                } else if ((sInfo2.flags & 2) != 0) {
                    aInfo = AppGlobals.getPackageManager().getApplicationInfo(callingPackage, DumpState.DUMP_PROVIDERS, userId);
                    if (aInfo != null) {
                        sInfo = new ServiceInfo(sInfo2);
                        sInfo.applicationInfo = new ApplicationInfo(sInfo.applicationInfo);
                        sInfo.applicationInfo.packageName = aInfo.packageName;
                        sInfo.applicationInfo.uid = aInfo.uid;
                        ComponentName componentName2 = new ComponentName(aInfo.packageName, name.getClassName());
                        service.setComponent(componentName2);
                        name = componentName2;
                        sInfo2 = sInfo;
                    } else {
                        throw new SecurityException("BIND_EXTERNAL_SERVICE failed, could not resolve client package " + callingPackage);
                    }
                } else {
                    throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an isolatedProcess");
                }
                if (userId <= 0) {
                    sInfo = sInfo2;
                } else {
                    if (this.mAm.isSingleton(sInfo2.processName, sInfo2.applicationInfo, sInfo2.name, sInfo2.flags)) {
                        if (this.mAm.isValidSingletonCall(callingUid, sInfo2.applicationInfo.uid)) {
                            userId = SERVICE_TIMEOUT;
                            smap = getServiceMap(SERVICE_TIMEOUT);
                        }
                    }
                    sInfo = new ServiceInfo(sInfo2);
                    sInfo.applicationInfo = this.mAm.getAppInfoForUser(sInfo.applicationInfo, userId);
                }
                r2 = (ServiceRecord) smap.mServicesByName.get(name);
                if (r2 == null) {
                    r = r2;
                } else if (createIfNeeded) {
                    r = r2;
                } else {
                    FilterComparison filter2 = new FilterComparison(service.cloneFilter());
                    ActiveServices activeServices2 = this;
                    ServiceRestarter res2 = new ServiceRestarter();
                    stats = this.mAm.mBatteryStatsService.getActiveStatistics();
                    synchronized (stats) {
                        ss = stats.getServiceStatsLocked(sInfo.applicationInfo.uid, sInfo.packageName, sInfo.name);
                    }
                    r = new ServiceRecord(this.mAm, ss, name, filter2, sInfo, callingFromFg, res2);
                    res2.setService(r);
                    smap.mServicesByName.put(name, r);
                    smap.mServicesByIntent.put(filter2, r);
                    for (i = this.mPendingServices.size() - 1; i >= 0; i--) {
                        ServiceRecord pr2 = (ServiceRecord) this.mPendingServices.get(i);
                        this.mPendingServices.remove(i);
                    }
                }
            } else {
                Slog.w(TAG_SERVICE, "Unable to start service " + service + " U=" + userId + ": not found");
                return null;
            }
        }
        if (r == null) {
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "retrieve service " + service + " U=" + userId + ": ret null");
            return null;
        }
        if (this.mAm.checkComponentPermission(r.permission, callingPid, callingUid, r.appInfo.uid, r.exported) != 0) {
            opCode = AppOpsManager.permissionToOpCode(r.permission);
            Slog.w(TAG, "Appop Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " requires appop " + AppOpsManager.opToName(opCode));
            return null;
        } else if (r.exported) {
            Slog.w(TAG, "Permission Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " requires " + r.permission);
            return new ServiceLookupResult(null, r.permission);
        } else {
            Slog.w(TAG, "Permission Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " that is not exported from uid " + r.appInfo.uid);
            return new ServiceLookupResult(null, "not exported from uid " + r.appInfo.uid);
        }
    }

    private final void bumpServiceExecutingLocked(ServiceRecord r, boolean fg, String why) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, ">>> EXECUTING " + why + " of " + r + " in app " + r.app);
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
            Slog.v(TAG_SERVICE_EXECUTING, ">>> EXECUTING " + why + " of " + r.shortName);
        }
        long now = SystemClock.uptimeMillis();
        if (r.executeNesting == 0) {
            r.executeFg = fg;
            ServiceState stracker = r.getTracker();
            if (stracker != null) {
                stracker.setExecuting(LOG_SERVICE_START_STOP, this.mAm.mProcessStats.getMemFactorLocked(), now);
            }
            if (r.app != null) {
                r.app.executingServices.add(r);
                ProcessRecord processRecord = r.app;
                processRecord.execServicesFg |= fg;
                if (r.app.executingServices.size() == 1) {
                    scheduleServiceTimeoutLocked(r.app);
                }
            }
        } else if (!(r.app == null || !fg || r.app.execServicesFg)) {
            r.app.execServicesFg = LOG_SERVICE_START_STOP;
            scheduleServiceTimeoutLocked(r.app);
        }
        r.executeFg |= fg;
        r.executeNesting++;
        r.executingStart = now;
    }

    private final boolean requestServiceBindingLocked(ServiceRecord r, IntentBindRecord i, boolean execInFg, boolean rebind) throws TransactionTooLargeException {
        boolean inDestroying;
        if (r.app == null || r.app.thread == null) {
            return SERVICE_RESCHEDULE;
        }
        if ((!i.requested || rebind) && i.apps.size() > 0) {
            try {
                bumpServiceExecutingLocked(r, execInFg, "bind");
                r.app.forceProcessStateUpTo(10);
                r.app.thread.scheduleBindService(r, i.intent.getIntent(), rebind, r.app.repProcState);
                if (!rebind) {
                    i.requested = LOG_SERVICE_START_STOP;
                }
                i.hasBound = LOG_SERVICE_START_STOP;
                i.doRebind = SERVICE_RESCHEDULE;
            } catch (TransactionTooLargeException e) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Crashed while binding " + r, e);
                }
                inDestroying = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                throw e;
            } catch (RemoteException e2) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Crashed while binding " + r);
                }
                inDestroying = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                return SERVICE_RESCHEDULE;
            }
        }
        return LOG_SERVICE_START_STOP;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final boolean scheduleServiceRestartLocked(ServiceRecord r, boolean allowCancel) {
        boolean canceled = SERVICE_RESCHEDULE;
        if (this.mAm.isShuttingDownLocked()) {
            Slog.w(TAG, "Not scheduling restart of crashed service " + r.shortName + " - system is shutting down");
            return SERVICE_RESCHEDULE;
        }
        int i;
        ServiceMap smap;
        long now;
        long minDuration;
        long resetTime;
        int N;
        int i2;
        StartItem si;
        long dur;
        StringBuilder append;
        long j;
        boolean repeat;
        ServiceRecord r2;
        if (r.appInfo != null) {
            if (r.appInfo.euid != 0) {
                if (this.mAm.isPackageCloned(r.packageName, r.userId)) {
                    i = r.appInfo.euid;
                    smap = getServiceMap(i);
                    if (smap.mServicesByName.get(r.name) == r) {
                        ServiceRecord cur = (ServiceRecord) smap.mServicesByName.get(r.name);
                        Slog.wtf(TAG, "Attempting to schedule restart of " + r + " when found in map: " + cur);
                        return SERVICE_RESCHEDULE;
                    }
                    now = SystemClock.uptimeMillis();
                    if ((r.serviceInfo.applicationInfo.flags & 8) != 0) {
                        minDuration = 1000;
                        resetTime = 60000;
                        N = r.deliveredStarts.size();
                        if (N > 0) {
                            for (i2 = N - 1; i2 >= 0; i2--) {
                                si = (StartItem) r.deliveredStarts.get(i2);
                                si.removeUriPermissionsLocked();
                                if (si.intent != null) {
                                    if (allowCancel) {
                                        i = si.deliveryCount;
                                        if (r0 < 3) {
                                            i = si.doneExecutingCount;
                                        }
                                        Intent intent = si.intent;
                                        Slog.w(TAG, "Canceling start item " + r0 + " in service " + r.name);
                                        canceled = LOG_SERVICE_START_STOP;
                                    }
                                    r.pendingStarts.add(SERVICE_TIMEOUT, si);
                                    dur = (SystemClock.uptimeMillis() - si.deliveredTime) * 2;
                                    if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
                                        append = new StringBuilder().append("Can add more delay !!! si.deliveredTime ").append(si.deliveredTime).append(" dur ");
                                        int i3 = si.deliveryCount;
                                        Slog.w(TAG, r21.append(dur).append(" si.deliveryCount ").append(r0).append(" si.doneExecutingCount ").append(si.doneExecutingCount).append(" allowCancel ").append(allowCancel).toString());
                                    }
                                    if (minDuration < dur) {
                                        minDuration = dur;
                                    }
                                    if (resetTime < dur) {
                                        resetTime = dur;
                                    }
                                }
                            }
                            r.deliveredStarts.clear();
                        }
                        r.totalRestartCount++;
                        if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
                            append = new StringBuilder().append("r.name ").append(r.name).append(" N ").append(N).append(" minDuration ").append(minDuration).append(" resetTime ").append(resetTime).append(" now ");
                            Slog.w(TAG, r21.append(now).append(" r.restartDelay ").append(r.restartDelay).append(" r.restartTime+resetTime ").append(r.restartTime + resetTime).append(" allowCancel ").append(allowCancel).toString());
                        }
                        if (r.restartDelay != 0) {
                            r.restartCount++;
                            r.restartDelay = minDuration;
                        } else {
                            if (now <= r.restartTime + resetTime) {
                                r.restartCount = 1;
                                r.restartDelay = minDuration;
                            } else {
                                r.restartDelay *= 4;
                                if (r.restartDelay < minDuration) {
                                    r.restartDelay = minDuration;
                                }
                            }
                        }
                        r.nextRestartTime = r.restartDelay + now;
                        if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
                            append = new StringBuilder().append("r.name ").append(r.name).append(" N ").append(N).append(" minDuration ").append(minDuration).append(" resetTime ").append(resetTime).append(" now ");
                            j = r.restartDelay;
                            j = r.restartTime;
                            Slog.w(TAG, r21.append(now).append(" r.restartDelay ").append(r0).append(" r.restartTime+resetTime ").append(r0 + resetTime).append(" r.nextRestartTime ").append(r.nextRestartTime).append(" allowCancel ").append(allowCancel).toString());
                        }
                        do {
                            repeat = SERVICE_RESCHEDULE;
                            for (i2 = this.mRestartingServices.size() - 1; i2 >= 0; i2--) {
                                r2 = (ServiceRecord) this.mRestartingServices.get(i2);
                                if (r2 != r) {
                                    if (r.nextRestartTime < r2.nextRestartTime - JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY) {
                                        if (r.nextRestartTime < r2.nextRestartTime + JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY) {
                                            r.nextRestartTime = r2.nextRestartTime + JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
                                            r.restartDelay = r.nextRestartTime - now;
                                            repeat = LOG_SERVICE_START_STOP;
                                            break;
                                            continue;
                                            break;
                                            continue;
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                            }
                        } while (repeat);
                    } else {
                        r.totalRestartCount++;
                        r.restartCount = SERVICE_TIMEOUT;
                        r.restartDelay = 0;
                        r.nextRestartTime = now;
                    }
                    if (!this.mRestartingServices.contains(r)) {
                        r.createdFromFg = SERVICE_RESCHEDULE;
                        this.mRestartingServices.add(r);
                        r.makeRestarting(this.mAm.mProcessStats.getMemFactorLocked(), now);
                    }
                    r.cancelNotification();
                    this.mAm.mHandler.removeCallbacks(r.restarter);
                    this.mAm.mHandler.postAtTime(r.restarter, r.nextRestartTime);
                    r.nextRestartTime = SystemClock.uptimeMillis() + r.restartDelay;
                    String str = r.shortName;
                    Slog.w(TAG, "Scheduling restart of crashed service " + r0 + " in " + r.restartDelay + "ms");
                    if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
                        for (i2 = this.mRestartingServices.size() - 1; i2 >= 0; i2--) {
                            r2 = (ServiceRecord) this.mRestartingServices.get(i2);
                            append = new StringBuilder().append("Restarting list - i ");
                            j = r2.nextRestartTime;
                            Slog.w(TAG, r21.append(i2).append(" r2.nextRestartTime ").append(r0).append(" r2.name ").append(r2.name).toString());
                        }
                    }
                    Integer[] numArr = new Object[3];
                    numArr[SERVICE_TIMEOUT] = Integer.valueOf(r.userId);
                    numArr[1] = r.shortName;
                    numArr[2] = Long.valueOf(r.restartDelay);
                    EventLog.writeEvent(EventLogTags.AM_SCHEDULE_SERVICE_RESTART, numArr);
                    return canceled;
                }
            }
        }
        i = r.userId;
        smap = getServiceMap(i);
        if (smap.mServicesByName.get(r.name) == r) {
            now = SystemClock.uptimeMillis();
            if ((r.serviceInfo.applicationInfo.flags & 8) != 0) {
                r.totalRestartCount++;
                r.restartCount = SERVICE_TIMEOUT;
                r.restartDelay = 0;
                r.nextRestartTime = now;
            } else {
                minDuration = 1000;
                resetTime = 60000;
                N = r.deliveredStarts.size();
                if (N > 0) {
                    for (i2 = N - 1; i2 >= 0; i2--) {
                        si = (StartItem) r.deliveredStarts.get(i2);
                        si.removeUriPermissionsLocked();
                        if (si.intent != null) {
                            if (allowCancel) {
                                i = si.deliveryCount;
                                if (r0 < 3) {
                                    i = si.doneExecutingCount;
                                }
                                Intent intent2 = si.intent;
                                Slog.w(TAG, "Canceling start item " + r0 + " in service " + r.name);
                                canceled = LOG_SERVICE_START_STOP;
                            }
                            r.pendingStarts.add(SERVICE_TIMEOUT, si);
                            dur = (SystemClock.uptimeMillis() - si.deliveredTime) * 2;
                            append = new StringBuilder().append("Can add more delay !!! si.deliveredTime ").append(si.deliveredTime).append(" dur ");
                            int i32 = si.deliveryCount;
                            Slog.w(TAG, r21.append(dur).append(" si.deliveryCount ").append(r0).append(" si.doneExecutingCount ").append(si.doneExecutingCount).append(" allowCancel ").append(allowCancel).toString());
                            if (minDuration < dur) {
                                minDuration = dur;
                            }
                            if (resetTime < dur) {
                                resetTime = dur;
                            }
                        }
                    }
                    r.deliveredStarts.clear();
                }
                r.totalRestartCount++;
                append = new StringBuilder().append("r.name ").append(r.name).append(" N ").append(N).append(" minDuration ").append(minDuration).append(" resetTime ").append(resetTime).append(" now ");
                Slog.w(TAG, r21.append(now).append(" r.restartDelay ").append(r.restartDelay).append(" r.restartTime+resetTime ").append(r.restartTime + resetTime).append(" allowCancel ").append(allowCancel).toString());
                if (r.restartDelay != 0) {
                    if (now <= r.restartTime + resetTime) {
                        r.restartDelay *= 4;
                        if (r.restartDelay < minDuration) {
                            r.restartDelay = minDuration;
                        }
                    } else {
                        r.restartCount = 1;
                        r.restartDelay = minDuration;
                    }
                } else {
                    r.restartCount++;
                    r.restartDelay = minDuration;
                }
                r.nextRestartTime = r.restartDelay + now;
                append = new StringBuilder().append("r.name ").append(r.name).append(" N ").append(N).append(" minDuration ").append(minDuration).append(" resetTime ").append(resetTime).append(" now ");
                j = r.restartDelay;
                j = r.restartTime;
                Slog.w(TAG, r21.append(now).append(" r.restartDelay ").append(r0).append(" r.restartTime+resetTime ").append(r0 + resetTime).append(" r.nextRestartTime ").append(r.nextRestartTime).append(" allowCancel ").append(allowCancel).toString());
                do {
                    repeat = SERVICE_RESCHEDULE;
                    for (i2 = this.mRestartingServices.size() - 1; i2 >= 0; i2--) {
                        r2 = (ServiceRecord) this.mRestartingServices.get(i2);
                        if (r2 != r) {
                            if (r.nextRestartTime < r2.nextRestartTime - JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY) {
                                continue;
                            } else {
                                if (r.nextRestartTime < r2.nextRestartTime + JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY) {
                                    r.nextRestartTime = r2.nextRestartTime + JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
                                    r.restartDelay = r.nextRestartTime - now;
                                    repeat = LOG_SERVICE_START_STOP;
                                    break;
                                    continue;
                                    break;
                                    continue;
                                }
                            }
                        }
                    }
                } while (repeat);
            }
            if (this.mRestartingServices.contains(r)) {
                r.createdFromFg = SERVICE_RESCHEDULE;
                this.mRestartingServices.add(r);
                r.makeRestarting(this.mAm.mProcessStats.getMemFactorLocked(), now);
            }
            r.cancelNotification();
            this.mAm.mHandler.removeCallbacks(r.restarter);
            this.mAm.mHandler.postAtTime(r.restarter, r.nextRestartTime);
            r.nextRestartTime = SystemClock.uptimeMillis() + r.restartDelay;
            String str2 = r.shortName;
            Slog.w(TAG, "Scheduling restart of crashed service " + r0 + " in " + r.restartDelay + "ms");
            for (i2 = this.mRestartingServices.size() - 1; i2 >= 0; i2--) {
                r2 = (ServiceRecord) this.mRestartingServices.get(i2);
                append = new StringBuilder().append("Restarting list - i ");
                j = r2.nextRestartTime;
                Slog.w(TAG, r21.append(i2).append(" r2.nextRestartTime ").append(r0).append(" r2.name ").append(r2.name).toString());
            }
            Integer[] numArr2 = new Object[3];
            numArr2[SERVICE_TIMEOUT] = Integer.valueOf(r.userId);
            numArr2[1] = r.shortName;
            numArr2[2] = Long.valueOf(r.restartDelay);
            EventLog.writeEvent(EventLogTags.AM_SCHEDULE_SERVICE_RESTART, numArr2);
            return canceled;
        }
        ServiceRecord cur2 = (ServiceRecord) smap.mServicesByName.get(r.name);
        Slog.wtf(TAG, "Attempting to schedule restart of " + r + " when found in map: " + cur2);
        return SERVICE_RESCHEDULE;
    }

    final void performServiceRestartLocked(ServiceRecord r) {
        if (!this.mRestartingServices.contains(r)) {
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "no need to performServiceRestart for r:" + r);
        } else if (isServiceNeeded(r, SERVICE_RESCHEDULE, SERVICE_RESCHEDULE)) {
            try {
                if (SERVICE_RESCHEDULE) {
                    boolean shouldDelay = SERVICE_RESCHEDULE;
                    ActivityRecord top_rc = null;
                    ActivityStack stack = this.mAm.getFocusedStack();
                    if (stack != null) {
                        top_rc = stack.topRunningActivityLocked();
                    }
                    if (!(top_rc == null || top_rc.nowVisible || r.shortName.contains(top_rc.packageName))) {
                        shouldDelay = LOG_SERVICE_START_STOP;
                    }
                    if (shouldDelay) {
                        if (DEBUG_DELAYED_SERVICE) {
                            Slog.v(TAG, "Reschedule service restart due to app launch r.shortName " + r.shortName + " r.app = " + r.app);
                        }
                        r.resetRestartCounter();
                        scheduleServiceRestartLocked(r, LOG_SERVICE_START_STOP);
                    } else {
                        bringUpServiceLocked(r, r.intent.getIntent().getFlags(), r.createdFromFg, LOG_SERVICE_START_STOP, SERVICE_RESCHEDULE);
                    }
                } else {
                    bringUpServiceLocked(r, r.intent.getIntent().getFlags(), r.createdFromFg, LOG_SERVICE_START_STOP, SERVICE_RESCHEDULE);
                }
            } catch (TransactionTooLargeException e) {
                Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "performServiceRestart TransactionTooLarge e:", e);
            }
        } else {
            Slog.wtf(TAG, "Restarting service that is not needed: " + r);
        }
    }

    private final boolean unscheduleServiceRestartLocked(ServiceRecord r, int callingUid, boolean force) {
        if (!force && r.restartDelay == 0) {
            return SERVICE_RESCHEDULE;
        }
        boolean removed = this.mRestartingServices.remove(r);
        if (removed || callingUid != r.appInfo.uid) {
            r.resetRestartCounter();
        }
        if (removed) {
            clearRestartingIfNeededLocked(r);
        }
        this.mAm.mHandler.removeCallbacks(r.restarter);
        return LOG_SERVICE_START_STOP;
    }

    private void clearRestartingIfNeededLocked(ServiceRecord r) {
        if (r.restartTracker != null) {
            boolean stillTracking = SERVICE_RESCHEDULE;
            for (int i = this.mRestartingServices.size() - 1; i >= 0; i--) {
                if (((ServiceRecord) this.mRestartingServices.get(i)).restartTracker == r.restartTracker) {
                    stillTracking = LOG_SERVICE_START_STOP;
                    break;
                }
            }
            if (!stillTracking) {
                r.restartTracker.setRestarting(SERVICE_RESCHEDULE, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                r.restartTracker = null;
            }
        }
    }

    private String bringUpServiceLocked(ServiceRecord r, int intentFlags, boolean execInFg, boolean whileRestarting, boolean permissionsReviewRequired) throws TransactionTooLargeException {
        if (r.app != null && r.app.thread != null) {
            sendServiceArgsLocked(r, execInFg, SERVICE_RESCHEDULE);
            return null;
        } else if (whileRestarting || r.restartDelay <= 0) {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "Bringing up " + r + " " + r.intent);
            }
            if (this.mRestartingServices.remove(r)) {
                r.resetRestartCounter();
                clearRestartingIfNeededLocked(r);
            }
            if (r.delayed) {
                int i;
                if (DEBUG_DELAYED_STARTS) {
                    Slog.v(TAG_SERVICE, "REM FR DELAY LIST (bring up): " + r);
                }
                if (r.appInfo.euid == 0) {
                    i = r.userId;
                } else {
                    i = r.appInfo.euid;
                }
                getServiceMap(i).mDelayedStartList.remove(r);
                r.delayed = SERVICE_RESCHEDULE;
            }
            String msg;
            if (this.mAm.mUserController.hasStartedUserState(r.userId)) {
                ProcessRecord app;
                try {
                    AppGlobals.getPackageManager().setPackageStoppedState(r.packageName, SERVICE_RESCHEDULE, r.userId);
                } catch (RemoteException e) {
                } catch (IllegalArgumentException e2) {
                    Slog.w(TAG, "Failed trying to unstop package " + r.packageName + ": " + e2);
                }
                boolean isolated = (r.serviceInfo.flags & 2) != 0 ? LOG_SERVICE_START_STOP : SERVICE_RESCHEDULE;
                String procName = r.processName;
                if (isolated) {
                    app = r.isolatedProc;
                } else {
                    Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "bindServiceLocked, r: " + r + ", euid: " + r.appInfo.euid);
                    app = this.mAm.getProcessRecordLocked(procName, r.appInfo.uid + r.appInfo.euid, SERVICE_RESCHEDULE);
                    if (ActivityManagerDebugConfig.DEBUG_MU) {
                        Slog.v(TAG_MU, "bringUpServiceLocked: appInfo.uid=" + r.appInfo.uid + " app=" + app);
                    }
                    if (!(app == null || app.thread == null)) {
                        try {
                            app.addPackage(r.appInfo.packageName, r.appInfo.versionCode, this.mAm.mProcessStats);
                            realStartServiceLocked(r, app, execInFg);
                            return null;
                        } catch (TransactionTooLargeException e3) {
                            throw e3;
                        } catch (RemoteException e4) {
                            Slog.w(TAG, "Exception when starting service " + r.shortName, e4);
                        }
                    }
                }
                if (app == null && !permissionsReviewRequired) {
                    Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "mAm.startProcessLocked for service: " + r + ", appinfo euid: " + r.appInfo.euid);
                    app = this.mAm.startProcessLocked(procName, r.appInfo, LOG_SERVICE_START_STOP, intentFlags, "service", r.name, SERVICE_RESCHEDULE, isolated, SERVICE_RESCHEDULE);
                    if (app == null) {
                        msg = "Unable to launch app " + r.appInfo.packageName + "/" + r.appInfo.uid + " for service " + r.intent.getIntent() + ": process is bad";
                        Slog.w(TAG, msg);
                        bringDownServiceLocked(r);
                        return msg;
                    } else if (isolated) {
                        r.isolatedProc = app;
                    }
                }
                if (!this.mPendingServices.contains(r)) {
                    this.mPendingServices.add(r);
                }
                if (r.delayedStop) {
                    r.delayedStop = SERVICE_RESCHEDULE;
                    if (r.startRequested) {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(TAG_SERVICE, "Applying delayed stop (in bring up): " + r);
                        }
                        stopServiceLocked(r);
                    }
                }
                return null;
            }
            msg = "Unable to launch app " + r.appInfo.packageName + "/" + r.appInfo.uid + " for service " + r.intent.getIntent() + ": user " + r.userId + " is stopped";
            Slog.w(TAG, msg);
            bringDownServiceLocked(r);
            return msg;
        } else {
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "do nothing when waiting for a restart and Bringing up " + r + " " + r.intent);
            return null;
        }
    }

    private final void requestServiceBindingsLocked(ServiceRecord r, boolean execInFg) throws TransactionTooLargeException {
        int i = r.bindings.size() - 1;
        while (i >= 0 && requestServiceBindingLocked(r, (IntentBindRecord) r.bindings.valueAt(i), execInFg, SERVICE_RESCHEDULE)) {
            i--;
        }
    }

    private final void realStartServiceLocked(ServiceRecord r, ProcessRecord app, boolean execInFg) throws RemoteException {
        if (app.thread == null) {
            throw new RemoteException();
        }
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "realStartServiceLocked, ServiceRecord.uid = " + r.appInfo.uid + ", ProcessRecord.uid = " + app.uid);
        }
        r.app = app;
        long uptimeMillis = SystemClock.uptimeMillis();
        r.lastActivity = uptimeMillis;
        r.restartTime = uptimeMillis;
        boolean newService = app.services.add(r);
        bumpServiceExecutingLocked(r, execInFg, "create");
        this.mAm.updateLruProcessLocked(app, SERVICE_RESCHEDULE, null);
        this.mAm.updateOomAdjLocked();
        boolean inDestroying;
        try {
            int lastPeriod = r.shortName.lastIndexOf(46);
            EventLogTags.writeAmCreateService(r.userId, System.identityHashCode(r), lastPeriod >= 0 ? r.shortName.substring(lastPeriod) : r.shortName, r.app.uid + r.app.info.euid, r.app.pid);
            synchronized (r.stats.getBatteryStats()) {
                r.stats.startLaunchedLocked();
            }
            this.mAm.notifyPackageUse(r.serviceInfo.packageName, 1);
            app.forceProcessStateUpTo(10);
            app.thread.scheduleCreateService(r, r.serviceInfo, this.mAm.compatibilityInfoForPackageLocked(r.serviceInfo.applicationInfo), app.repProcState);
            r.postNotification();
            if (!LOG_SERVICE_START_STOP) {
                inDestroying = this.mDestroyingServices.contains(r);
                serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                if (newService) {
                    app.services.remove(r);
                    r.app = null;
                    if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
                        Slog.w(TAG, " Failed to create Service !!!! .This will introduce huge delay...  " + r.shortName + " in " + r.restartDelay + "ms");
                    }
                }
                if (inDestroying) {
                    Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Destroying no retry when creating service " + r);
                } else {
                    scheduleServiceRestartLocked(r, SERVICE_RESCHEDULE);
                }
            }
            if (r.whitelistManager) {
                app.whitelistManager = LOG_SERVICE_START_STOP;
            }
            requestServiceBindingsLocked(r, execInFg);
            updateServiceClientActivitiesLocked(app, null, LOG_SERVICE_START_STOP);
            if (r.startRequested && r.callStart && r.pendingStarts.size() == 0) {
                r.pendingStarts.add(new StartItem(r, SERVICE_RESCHEDULE, r.makeNextStartId(), null, null));
            }
            sendServiceArgsLocked(r, execInFg, LOG_SERVICE_START_STOP);
            if (r.delayed) {
                if (DEBUG_DELAYED_STARTS) {
                    Slog.v(TAG_SERVICE, "REM FR DELAY LIST (new proc): " + r);
                }
                getServiceMap(r.appInfo.euid == 0 ? r.userId : r.appInfo.euid).mDelayedStartList.remove(r);
                r.delayed = SERVICE_RESCHEDULE;
            }
            if (r.delayedStop) {
                r.delayedStop = SERVICE_RESCHEDULE;
                if (r.startRequested) {
                    if (DEBUG_DELAYED_STARTS) {
                        Slog.v(TAG_SERVICE, "Applying delayed stop (from start): " + r);
                    }
                    stopServiceLocked(r);
                }
            }
        } catch (DeadObjectException e) {
            try {
                Slog.w(TAG, "Application dead when creating service " + r);
                this.mAm.appDiedLocked(app);
                throw e;
            } catch (Throwable th) {
                if (!SERVICE_RESCHEDULE) {
                    inDestroying = this.mDestroyingServices.contains(r);
                    serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                    if (newService) {
                        app.services.remove(r);
                        r.app = null;
                        if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
                            Slog.w(TAG, " Failed to create Service !!!! .This will introduce huge delay...  " + r.shortName + " in " + r.restartDelay + "ms");
                        }
                    }
                    if (inDestroying) {
                        Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Destroying no retry when creating service " + r);
                    } else {
                        scheduleServiceRestartLocked(r, SERVICE_RESCHEDULE);
                    }
                }
            }
        }
    }

    private final void sendServiceArgsLocked(ServiceRecord r, boolean execInFg, boolean oomAdjusted) throws TransactionTooLargeException {
        int N = r.pendingStarts.size();
        if (N != 0) {
            while (r.pendingStarts.size() > 0) {
                Exception caughtException = null;
                try {
                    StartItem si = (StartItem) r.pendingStarts.remove(SERVICE_TIMEOUT);
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "Sending arguments to: " + r + " " + r.intent + " args=" + si.intent);
                    }
                    if (si.intent != null || N <= 1) {
                        si.deliveredTime = SystemClock.uptimeMillis();
                        r.deliveredStarts.add(si);
                        si.deliveryCount++;
                        if (si.neededGrants != null) {
                            this.mAm.grantUriPermissionUncheckedFromIntentLocked(si.neededGrants, si.getUriPermissionsLocked());
                        }
                        bumpServiceExecutingLocked(r, execInFg, "start");
                        if (!oomAdjusted) {
                            oomAdjusted = LOG_SERVICE_START_STOP;
                            this.mAm.updateOomAdjLocked(r.app);
                        }
                        int flags = SERVICE_TIMEOUT;
                        if (si.deliveryCount > 1) {
                            flags = 2;
                        }
                        if (si.doneExecutingCount > 0) {
                            flags |= 1;
                        }
                        r.app.thread.scheduleServiceArgs(r, si.taskRemoved, si.id, flags, si.intent);
                        if (caughtException != null) {
                            boolean inDestroying = this.mDestroyingServices.contains(r);
                            serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                            if (caughtException instanceof TransactionTooLargeException) {
                                throw ((TransactionTooLargeException) caughtException);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "Transaction too large");
                    }
                    caughtException = e;
                } catch (Exception e2) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "Crashed while sending args: " + r);
                    }
                    caughtException = e2;
                } catch (Exception e3) {
                    Slog.w(TAG, "Unexpected exception", e3);
                    caughtException = e3;
                }
            }
        }
    }

    private final boolean isServiceNeeded(ServiceRecord r, boolean knowConn, boolean hasConn) {
        if (r.startRequested) {
            return LOG_SERVICE_START_STOP;
        }
        if (!knowConn) {
            hasConn = r.hasAutoCreateConnections();
        }
        if (hasConn) {
            return LOG_SERVICE_START_STOP;
        }
        return SERVICE_RESCHEDULE;
    }

    private final void bringDownServiceIfNeededLocked(ServiceRecord r, boolean knowConn, boolean hasConn) {
        if (isServiceNeeded(r, knowConn, hasConn)) {
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "ServiceNeeded not bring down service:" + r);
        } else if (this.mPendingServices.contains(r)) {
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "still in launching not bring down service:" + r);
        } else {
            bringDownServiceLocked(r);
        }
    }

    final void bringDownServiceLocked(ServiceRecord r) {
        int i;
        int i2;
        for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> c = (ArrayList) r.connections.valueAt(conni);
            for (i = SERVICE_TIMEOUT; i < c.size(); i++) {
                ConnectionRecord cr = (ConnectionRecord) c.get(i);
                cr.serviceDead = LOG_SERVICE_START_STOP;
                try {
                    cr.conn.connected(r.name, null);
                } catch (Exception e) {
                    Slog.w(TAG, "Failure disconnecting service " + r.name + " to connection " + ((ConnectionRecord) c.get(i)).conn.asBinder() + " (in " + ((ConnectionRecord) c.get(i)).binding.client.processName + ")", e);
                }
            }
        }
        if (!(r.app == null || r.app.thread == null)) {
            for (i = r.bindings.size() - 1; i >= 0; i--) {
                IntentBindRecord ibr = (IntentBindRecord) r.bindings.valueAt(i);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Bringing down binding " + ibr + ": hasBound=" + ibr.hasBound);
                }
                if (ibr.hasBound) {
                    try {
                        bumpServiceExecutingLocked(r, SERVICE_RESCHEDULE, "bring down unbind");
                        this.mAm.updateOomAdjLocked(r.app);
                        ibr.hasBound = SERVICE_RESCHEDULE;
                        r.app.thread.scheduleUnbindService(r, ibr.intent.getIntent());
                    } catch (Exception e2) {
                        Slog.w(TAG, "Exception when unbinding service " + r.shortName, e2);
                        serviceProcessGoneLocked(r);
                    }
                }
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "Bringing down " + r + " " + r.intent);
        }
        r.destroyTime = SystemClock.uptimeMillis();
        EventLogTags.writeAmDestroyService(r.userId, System.identityHashCode(r), r.app != null ? r.app.pid : -1);
        if (r.appInfo.euid == 0) {
            i2 = r.userId;
        } else {
            i2 = r.appInfo.euid;
        }
        ServiceMap smap = getServiceMap(i2);
        smap.mServicesByName.remove(r.name);
        smap.mServicesByIntent.remove(r.intent);
        r.totalRestartCount = SERVICE_TIMEOUT;
        unscheduleServiceRestartLocked(r, SERVICE_TIMEOUT, LOG_SERVICE_START_STOP);
        for (i = this.mPendingServices.size() - 1; i >= 0; i--) {
            if (this.mPendingServices.get(i) == r) {
                this.mPendingServices.remove(i);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Removed pending: " + r);
                }
            }
        }
        r.cancelNotification();
        r.isForeground = SERVICE_RESCHEDULE;
        r.foregroundId = SERVICE_TIMEOUT;
        r.foregroundNoti = null;
        r.clearDeliveredStartsLocked();
        r.pendingStarts.clear();
        if (r.app != null) {
            synchronized (r.stats.getBatteryStats()) {
                r.stats.stopLaunchedLocked();
            }
            r.app.services.remove(r);
            if (r.whitelistManager) {
                updateWhitelistManagerLocked(r.app);
            }
            if (r.app.thread != null) {
                updateServiceForegroundLocked(r.app, SERVICE_RESCHEDULE);
                try {
                    bumpServiceExecutingLocked(r, SERVICE_RESCHEDULE, "destroy");
                    this.mDestroyingServices.add(r);
                    r.destroying = LOG_SERVICE_START_STOP;
                    this.mAm.updateOomAdjLocked(r.app);
                    r.app.thread.scheduleStopService(r);
                } catch (Exception e22) {
                    Slog.w(TAG, "Exception when destroying service " + r.shortName, e22);
                    serviceProcessGoneLocked(r);
                }
            } else {
                Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Removed service that has no process: " + r);
            }
        } else {
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Removed service that is not running: " + r);
        }
        if (r.bindings.size() > 0) {
            r.bindings.clear();
        }
        if (r.restarter instanceof ServiceRestarter) {
            ((ServiceRestarter) r.restarter).setService(null);
        }
        int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
        long now = SystemClock.uptimeMillis();
        if (r.tracker != null) {
            r.tracker.setStarted(SERVICE_RESCHEDULE, memFactor, now);
            r.tracker.setBound(SERVICE_RESCHEDULE, memFactor, now);
            if (r.executeNesting == 0) {
                r.tracker.clearCurrentOwner(r, SERVICE_RESCHEDULE);
                r.tracker = null;
            }
        }
        smap.ensureNotStartingBackground(r);
    }

    void removeConnectionLocked(ConnectionRecord c, ProcessRecord skipApp, ActivityRecord skipAct) {
        IBinder binder = c.conn.asBinder();
        AppBindRecord b = c.binding;
        ServiceRecord s = b.service;
        ArrayList<ConnectionRecord> clist = (ArrayList) s.connections.get(binder);
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
            if (!(s.app == null || s.app.uid < SERVICE_MIN_RESTART_TIME_BETWEEN || pr.pid == s.app.pid || s.app.info == null || pr.info == null || s.app.info.packageName == null || s.app.info.packageName.equals(pr.info.packageName))) {
                LogPower.push(167, s.processName, Integer.toString(pr.pid), Integer.toString(s.app.pid), new String[]{"service"});
            }
            if ((c.flags & 8) != 0) {
                b.client.updateHasAboveClientLocked();
            }
            if ((c.flags & 16777216) != 0) {
                s.updateWhitelistManager();
                if (!(s.whitelistManager || s.app == null)) {
                    updateWhitelistManagerLocked(s.app);
                }
            }
            if (s.app != null) {
                updateServiceClientActivitiesLocked(s.app, c, LOG_SERVICE_START_STOP);
            }
        }
        clist = (ArrayList) this.mServiceConnections.get(binder);
        if (clist != null) {
            clist.remove(c);
            if (clist.size() == 0) {
                this.mServiceConnections.remove(binder);
            }
        }
        this.mAm.stopAssociationLocked(b.client.uid, b.client.processName, s.appInfo.uid, s.name);
        this.mAm.reportServiceRelationIAware(3, s, b.client);
        if (b.connections.size() == 0) {
            b.intent.apps.remove(b.client);
        }
        if (!c.serviceDead) {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "Disconnecting binding " + b.intent + ": shouldUnbind=" + b.intent.hasBound);
            }
            if (s.app != null && s.app.thread != null && b.intent.apps.size() == 0 && b.intent.hasBound) {
                try {
                    bumpServiceExecutingLocked(s, SERVICE_RESCHEDULE, "unbind");
                    if (b.client != s.app && (c.flags & 32) == 0 && s.app.setProcState <= 11) {
                        this.mAm.updateLruProcessLocked(s.app, SERVICE_RESCHEDULE, null);
                    }
                    this.mAm.updateOomAdjLocked(s.app);
                    b.intent.hasBound = SERVICE_RESCHEDULE;
                    b.intent.doRebind = SERVICE_RESCHEDULE;
                    s.app.thread.scheduleUnbindService(s, b.intent.intent.getIntent());
                } catch (Exception e) {
                    Slog.w(TAG, "Exception when unbinding service " + s.shortName, e);
                    if (!s.app.killedByAm) {
                        serviceProcessGoneLocked(s);
                    }
                }
            }
            this.mPendingServices.remove(s);
            if ((c.flags & 1) != 0) {
                boolean hasAutoCreate = s.hasAutoCreateConnections();
                if (!(hasAutoCreate || s.tracker == null)) {
                    s.tracker.setBound(SERVICE_RESCHEDULE, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                }
                bringDownServiceIfNeededLocked(s, LOG_SERVICE_START_STOP, hasAutoCreate);
            }
        }
    }

    void serviceDoneExecutingLocked(ServiceRecord r, int type, int startId, int res) {
        boolean inDestroying = this.mDestroyingServices.contains(r);
        if (r != null) {
            if (type == 1) {
                r.callStart = LOG_SERVICE_START_STOP;
                switch (res) {
                    case SERVICE_TIMEOUT /*0*/:
                    case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                        r.findDeliveredStart(startId, LOG_SERVICE_START_STOP);
                        r.stopIfKilled = SERVICE_RESCHEDULE;
                        break;
                    case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                        r.findDeliveredStart(startId, LOG_SERVICE_START_STOP);
                        if (r.getLastStartId() == startId) {
                            r.stopIfKilled = LOG_SERVICE_START_STOP;
                            break;
                        }
                        break;
                    case H.REPORT_LOSING_FOCUS /*3*/:
                        StartItem si = r.findDeliveredStart(startId, SERVICE_RESCHEDULE);
                        if (si != null) {
                            si.deliveryCount = SERVICE_TIMEOUT;
                            si.doneExecutingCount++;
                            r.stopIfKilled = LOG_SERVICE_START_STOP;
                            break;
                        }
                        break;
                    case SERVICE_RESTART_DURATION /*1000*/:
                        r.findDeliveredStart(startId, LOG_SERVICE_START_STOP);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown service start result: " + res);
                }
                if (res == 0) {
                    r.callStart = SERVICE_RESCHEDULE;
                }
            } else if (type == 2) {
                if (inDestroying) {
                    if (r.executeNesting != 1) {
                        Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Service done with onDestroy, but executeNesting=" + r.executeNesting + ": " + r);
                        Slog.w(TAG, "Service done with onDestroy, but executeNesting=" + r.executeNesting + ": " + r);
                        r.executeNesting = 1;
                    }
                } else if (r.app != null) {
                    Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Service done with onDestroy, but not inDestroying:" + r + ", app=" + r.app);
                }
            }
            long origId = Binder.clearCallingIdentity();
            serviceDoneExecutingLocked(r, inDestroying, inDestroying);
            Binder.restoreCallingIdentity(origId);
            return;
        }
        Slog.w(TAG, "Done executing unknown service from pid " + Binder.getCallingPid());
    }

    private void serviceProcessGoneLocked(ServiceRecord r) {
        if (r.tracker != null) {
            int memFactor = this.mAm.mProcessStats.getMemFactorLocked();
            long now = SystemClock.uptimeMillis();
            r.tracker.setExecuting(SERVICE_RESCHEDULE, memFactor, now);
            r.tracker.setBound(SERVICE_RESCHEDULE, memFactor, now);
            r.tracker.setStarted(SERVICE_RESCHEDULE, memFactor, now);
        }
        serviceDoneExecutingLocked(r, LOG_SERVICE_START_STOP, LOG_SERVICE_START_STOP);
    }

    private void serviceDoneExecutingLocked(ServiceRecord r, boolean inDestroying, boolean finishing) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "<<< DONE EXECUTING " + r + ": nesting=" + r.executeNesting + ", inDestroying=" + inDestroying + ", app=" + r.app);
        } else if (ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
            Slog.v(TAG_SERVICE_EXECUTING, "<<< DONE EXECUTING " + r.shortName);
        }
        r.executeNesting--;
        if (r.executeNesting <= 0) {
            if (r.app != null) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Nesting at 0 of " + r.shortName);
                }
                r.app.execServicesFg = SERVICE_RESCHEDULE;
                r.app.executingServices.remove(r);
                if (r.app.executingServices.size() == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE || ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING) {
                        Slog.v(TAG_SERVICE_EXECUTING, "No more executingServices of " + r.shortName);
                    }
                    this.mAm.mHandler.removeMessages(12, r.app);
                    this.mAm.mHandler.removeMessages(99, r.app);
                } else if (r.executeFg) {
                    for (int i = r.app.executingServices.size() - 1; i >= 0; i--) {
                        if (((ServiceRecord) r.app.executingServices.valueAt(i)).executeFg) {
                            r.app.execServicesFg = LOG_SERVICE_START_STOP;
                            break;
                        }
                    }
                }
                if (inDestroying) {
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "doneExecuting remove destroying " + r);
                    }
                    this.mDestroyingServices.remove(r);
                    r.bindings.clear();
                }
                this.mAm.updateOomAdjLocked(r.app);
            }
            r.executeFg = SERVICE_RESCHEDULE;
            if (r.tracker != null) {
                r.tracker.setExecuting(SERVICE_RESCHEDULE, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                if (finishing) {
                    r.tracker.clearCurrentOwner(r, SERVICE_RESCHEDULE);
                    r.tracker = null;
                }
            }
            if (finishing) {
                if (!(r.app == null || r.app.persistent)) {
                    r.app.services.remove(r);
                    if (r.whitelistManager) {
                        updateWhitelistManagerLocked(r.app);
                    }
                }
                r.app = null;
            }
        }
    }

    boolean attachApplicationLocked(ProcessRecord proc, String processName) throws RemoteException {
        ServiceRecord sr;
        int i;
        boolean didSomething = SERVICE_RESCHEDULE;
        if (this.mPendingServices.size() > 0) {
            sr = null;
            i = SERVICE_TIMEOUT;
            while (i < this.mPendingServices.size()) {
                try {
                    sr = (ServiceRecord) this.mPendingServices.get(i);
                    if (proc == sr.isolatedProc || (proc.uid == sr.appInfo.uid && proc.info.euid == sr.appInfo.euid && processName.equals(sr.processName))) {
                        this.mPendingServices.remove(i);
                        i--;
                        proc.addPackage(sr.appInfo.packageName, sr.appInfo.versionCode, this.mAm.mProcessStats);
                        realStartServiceLocked(sr, proc, sr.createdFromFg);
                        didSomething = LOG_SERVICE_START_STOP;
                        if (!isServiceNeeded(sr, SERVICE_RESCHEDULE, SERVICE_RESCHEDULE)) {
                            bringDownServiceLocked(sr);
                        }
                    }
                    i++;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception in new application when starting service " + sr.shortName, e);
                    throw e;
                }
            }
        }
        if (this.mRestartingServices.size() > 0) {
            for (i = SERVICE_TIMEOUT; i < this.mRestartingServices.size(); i++) {
                sr = (ServiceRecord) this.mRestartingServices.get(i);
                if (proc == sr.isolatedProc || (proc.uid == sr.appInfo.uid && processName.equals(sr.processName) && proc.info.euid == sr.appInfo.euid)) {
                    this.mAm.mHandler.removeCallbacks(sr.restarter);
                    this.mAm.mHandler.post(sr.restarter);
                }
            }
        }
        return didSomething;
    }

    void processStartTimedOutLocked(ProcessRecord proc) {
        int i = SERVICE_TIMEOUT;
        while (i < this.mPendingServices.size()) {
            ServiceRecord sr = (ServiceRecord) this.mPendingServices.get(i);
            if (!(proc.uid == sr.appInfo.uid && proc.processName.equals(sr.processName) && proc.info.euid == sr.appInfo.euid)) {
                if (sr.isolatedProc != proc) {
                    i++;
                }
            }
            Slog.w(TAG, "Forcing bringing down service: " + sr);
            sr.isolatedProc = null;
            this.mPendingServices.remove(i);
            i--;
            bringDownServiceLocked(sr);
            i++;
        }
    }

    boolean collectPackageServicesLocked(String packageName, Set<String> filterByClasses, boolean evenPersistent, boolean doit, boolean killProcess, ArrayMap<ComponentName, ServiceRecord> services) {
        boolean didSomething = SERVICE_RESCHEDULE;
        for (int i = services.size() - 1; i >= 0; i--) {
            boolean sameComponent;
            ServiceRecord service = (ServiceRecord) services.valueAt(i);
            if (packageName == null) {
                sameComponent = LOG_SERVICE_START_STOP;
            } else if (!service.packageName.equals(packageName)) {
                sameComponent = SERVICE_RESCHEDULE;
            } else if (filterByClasses != null) {
                sameComponent = filterByClasses.contains(service.name.getClassName());
            } else {
                sameComponent = LOG_SERVICE_START_STOP;
            }
            if (sameComponent && (service.app == null || evenPersistent || !service.app.persistent)) {
                if (!doit) {
                    return LOG_SERVICE_START_STOP;
                }
                didSomething = LOG_SERVICE_START_STOP;
                Slog.i(TAG, "  Force stopping service " + service);
                if (service.app != null) {
                    service.app.removed = killProcess;
                    if (!service.app.persistent) {
                        service.app.services.remove(service);
                        if (service.whitelistManager) {
                            updateWhitelistManagerLocked(service.app);
                        }
                    }
                }
                service.app = null;
                service.isolatedProc = null;
                if (this.mTmpCollectionResults == null) {
                    this.mTmpCollectionResults = new ArrayList();
                }
                this.mTmpCollectionResults.add(service);
            }
        }
        return didSomething;
    }

    boolean bringDownDisabledPackageServicesLocked(String packageName, Set<String> filterByClasses, int userId, boolean evenPersistent, boolean killProcess, boolean doit) {
        int i;
        boolean didSomething = SERVICE_RESCHEDULE;
        if (this.mTmpCollectionResults != null) {
            this.mTmpCollectionResults.clear();
        }
        if (userId == -1) {
            for (i = this.mServiceMap.size() - 1; i >= 0; i--) {
                didSomething |= collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, killProcess, ((ServiceMap) this.mServiceMap.valueAt(i)).mServicesByName);
                if (!doit && didSomething) {
                    return LOG_SERVICE_START_STOP;
                }
            }
        } else {
            ServiceMap smap = (ServiceMap) this.mServiceMap.get(userId);
            if (smap != null) {
                didSomething = collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, killProcess, smap.mServicesByName);
            }
        }
        if (this.mTmpCollectionResults != null) {
            for (i = this.mTmpCollectionResults.size() - 1; i >= 0; i--) {
                bringDownServiceLocked((ServiceRecord) this.mTmpCollectionResults.get(i));
            }
            this.mTmpCollectionResults.clear();
        }
        return didSomething;
    }

    void cleanUpRemovedTaskLocked(TaskRecord tr, ComponentName component, Intent baseIntent) {
        ArrayList<ServiceRecord> services = new ArrayList();
        List<ArrayMap<ComponentName, ServiceRecord>> list = new ArrayList();
        list.add(getServices(tr.userId));
        if (tr.userId == 0) {
            list.add(getServices(2147383647));
        }
        for (ArrayMap<ComponentName, ServiceRecord> alls : list) {
            int i;
            for (i = alls.size() - 1; i >= 0; i--) {
                ServiceRecord sr = (ServiceRecord) alls.valueAt(i);
                if (sr.packageName.equals(component.getPackageName())) {
                    services.add(sr);
                }
            }
        }
        if (services.size() > 0) {
            LogPower.push(148, "cleanUpservice", component.getPackageName());
        }
        for (i = services.size() - 1; i >= 0; i--) {
            sr = (ServiceRecord) services.get(i);
            if (sr.startRequested) {
                if ((sr.serviceInfo.flags & 1) != 0) {
                    Slog.i(TAG, "Stopping service " + sr.shortName + ": remove task");
                    stopServiceLocked(sr);
                } else {
                    sr.pendingStarts.add(new StartItem(sr, LOG_SERVICE_START_STOP, sr.makeNextStartId(), baseIntent, null));
                    if (!(sr.app == null || sr.app.thread == null)) {
                        try {
                            sendServiceArgsLocked(sr, LOG_SERVICE_START_STOP, SERVICE_RESCHEDULE);
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
            }
        }
    }

    final void killServicesLocked(ProcessRecord app, boolean allowRestart) {
        int i;
        int i2;
        for (i = app.connections.size() - 1; i >= 0; i--) {
            removeConnectionLocked((ConnectionRecord) app.connections.valueAt(i), app, null);
        }
        updateServiceConnectionActivitiesLocked(app);
        app.connections.clear();
        app.whitelistManager = SERVICE_RESCHEDULE;
        for (i = app.services.size() - 1; i >= 0; i--) {
            ServiceRecord sr = (ServiceRecord) app.services.valueAt(i);
            synchronized (sr.stats.getBatteryStats()) {
                sr.stats.stopLaunchedLocked();
            }
            ProcessRecord processRecord = sr.app;
            if (!(r0 == app || sr.app == null)) {
                if (!sr.app.persistent) {
                    sr.app.services.remove(sr);
                }
            }
            sr.app = null;
            sr.isolatedProc = null;
            sr.executeNesting = SERVICE_TIMEOUT;
            sr.forceClearTracker();
            if (this.mDestroyingServices.remove(sr) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "killServices remove destroying " + sr);
            }
            for (int bindingi = sr.bindings.size() - 1; bindingi >= 0; bindingi--) {
                IntentBindRecord b = (IntentBindRecord) sr.bindings.valueAt(bindingi);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Killing binding " + b + ": shouldUnbind=" + b.hasBound);
                }
                b.binder = null;
                b.hasBound = SERVICE_RESCHEDULE;
                b.received = SERVICE_RESCHEDULE;
                b.requested = SERVICE_RESCHEDULE;
                for (int appi = b.apps.size() - 1; appi >= 0; appi--) {
                    ProcessRecord proc = (ProcessRecord) b.apps.keyAt(appi);
                    if (!(proc.killedByAm || proc.thread == null)) {
                        AppBindRecord abind = (AppBindRecord) b.apps.valueAt(appi);
                        boolean hasCreate = SERVICE_RESCHEDULE;
                        for (int conni = abind.connections.size() - 1; conni >= 0; conni--) {
                            if ((((ConnectionRecord) abind.connections.valueAt(conni)).flags & 49) == 1) {
                                hasCreate = LOG_SERVICE_START_STOP;
                                break;
                            }
                        }
                        if (hasCreate) {
                        }
                    }
                }
            }
        }
        if (app.info.euid == 0) {
            i2 = app.userId;
        } else {
            i2 = app.info.euid;
        }
        ServiceMap smap = getServiceMap(i2);
        for (i = app.services.size() - 1; i >= 0; i--) {
            sr = (ServiceRecord) app.services.valueAt(i);
            if (!app.persistent) {
                app.services.removeAt(i);
            }
            ServiceRecord curRec = (ServiceRecord) smap.mServicesByName.get(sr.name);
            if (curRec == sr) {
                if (allowRestart) {
                    boolean preventRestart = this.mAm.shouldPreventRestartService(sr.name.getPackageName());
                    if (preventRestart) {
                        Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Service " + sr + " in process " + app + " prevent restart by hsm srname: " + sr.name);
                    }
                    allowRestart = preventRestart ? SERVICE_RESCHEDULE : LOG_SERVICE_START_STOP;
                }
                if (allowRestart) {
                    allowRestart = this.mAm.isAcquireAppServiceResourceLocked(sr, app);
                    if (!allowRestart) {
                        Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Service " + sr + " in process " + app + " prevent restart by RMS srname: " + sr.name);
                    }
                }
                if (allowRestart) {
                    i2 = sr.crashCount;
                    if (r0 >= 2) {
                        if ((sr.serviceInfo.applicationInfo.flags & 8) == 0) {
                            Slog.w(TAG, "Service crashed " + sr.crashCount + " times, stopping: " + sr);
                            Integer[] numArr = new Object[SERVICE_RESTART_DURATION_FACTOR];
                            numArr[SERVICE_TIMEOUT] = Integer.valueOf(sr.userId);
                            numArr[1] = Integer.valueOf(sr.crashCount);
                            numArr[2] = sr.shortName;
                            numArr[3] = Integer.valueOf(app.pid);
                            EventLog.writeEvent(EventLogTags.AM_SERVICE_CRASHED_TOO_MUCH, numArr);
                            bringDownServiceLocked(sr);
                        }
                    }
                }
                if (allowRestart) {
                    if (this.mAm.mUserController.isUserRunningLocked(sr.userId, SERVICE_TIMEOUT)) {
                        boolean canceled = scheduleServiceRestartLocked(sr, LOG_SERVICE_START_STOP);
                        if (sr.startRequested && (sr.stopIfKilled || canceled)) {
                            if (sr.pendingStarts.size() == 0) {
                                sr.startRequested = SERVICE_RESCHEDULE;
                                if (sr.tracker != null) {
                                    sr.tracker.setStarted(SERVICE_RESCHEDULE, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                                }
                                if (!sr.hasAutoCreateConnections()) {
                                    bringDownServiceLocked(sr);
                                }
                            }
                        }
                    }
                }
                bringDownServiceLocked(sr);
            } else if (curRec != null) {
                Slog.wtf(TAG, "Service " + sr + " in process " + app + " not same as in map: " + curRec);
            }
        }
        if (!allowRestart) {
            ServiceRecord r;
            app.services.clear();
            for (i = this.mRestartingServices.size() - 1; i >= 0; i--) {
                r = (ServiceRecord) this.mRestartingServices.get(i);
                if (r.processName.equals(app.processName) && r.serviceInfo.applicationInfo.uid == app.info.uid && r.serviceInfo.applicationInfo.euid == app.info.euid) {
                    this.mRestartingServices.remove(i);
                    clearRestartingIfNeededLocked(r);
                }
            }
            for (i = this.mPendingServices.size() - 1; i >= 0; i--) {
                r = (ServiceRecord) this.mPendingServices.get(i);
                if (r.processName.equals(app.processName) && r.serviceInfo.applicationInfo.uid == app.info.uid && r.serviceInfo.applicationInfo.euid == app.info.euid) {
                    this.mPendingServices.remove(i);
                }
            }
        }
        i = this.mDestroyingServices.size();
        while (i > 0) {
            i--;
            sr = (ServiceRecord) this.mDestroyingServices.get(i);
            processRecord = sr.app;
            if (r0 == app) {
                sr.forceClearTracker();
                this.mDestroyingServices.remove(i);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "killServices remove destroying " + sr);
                }
            }
        }
        app.executingServices.clear();
    }

    RunningServiceInfo makeRunningServiceInfoLocked(ServiceRecord r) {
        RunningServiceInfo info = new RunningServiceInfo();
        info.service = r.name;
        if (r.app != null) {
            info.pid = r.app.pid;
        }
        info.uid = r.appInfo.uid;
        info.process = r.processName;
        info.foreground = r.isForeground;
        info.activeSince = r.createTime;
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
            info.flags |= SERVICE_RESTART_DURATION_FACTOR;
        }
        if (r.app != null && r.app.persistent) {
            info.flags |= 8;
        }
        for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> connl = (ArrayList) r.connections.valueAt(conni);
            for (int i = SERVICE_TIMEOUT; i < connl.size(); i++) {
                ConnectionRecord conn = (ConnectionRecord) connl.get(i);
                if (conn.clientLabel != 0) {
                    info.clientPackage = conn.binding.client.info.packageName;
                    info.clientLabel = conn.clientLabel;
                    return info;
                }
            }
        }
        return info;
    }

    List<RunningServiceInfo> getRunningServiceInfoLocked(int maxNum, int flags) {
        ArrayList<RunningServiceInfo> res = new ArrayList();
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            int[] users;
            ArrayMap<ComponentName, ServiceRecord> alls;
            int i;
            ServiceRecord r;
            RunningServiceInfo info;
            if (ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS_FULL", uid) == 0) {
                int[] usersFromAms = this.mAm.mUserController.getUsers();
                users = new int[(usersFromAms.length + 1)];
                System.arraycopy(usersFromAms, SERVICE_TIMEOUT, users, SERVICE_TIMEOUT, usersFromAms.length);
                users[usersFromAms.length] = 2147383647;
                for (int ui = SERVICE_TIMEOUT; ui < users.length && res.size() < maxNum; ui++) {
                    alls = getServices(users[ui]);
                    for (i = SERVICE_TIMEOUT; i < alls.size() && res.size() < maxNum; i++) {
                        res.add(makeRunningServiceInfoLocked((ServiceRecord) alls.valueAt(i)));
                    }
                }
                for (i = SERVICE_TIMEOUT; i < this.mRestartingServices.size() && res.size() < maxNum; i++) {
                    r = (ServiceRecord) this.mRestartingServices.get(i);
                    info = makeRunningServiceInfoLocked(r);
                    info.restarting = r.nextRestartTime;
                    res.add(info);
                }
            } else {
                int userId = UserHandle.getUserId(uid);
                users = userId == 0 ? new int[]{userId, 2147383647} : new int[]{userId};
                int length = users.length;
                for (int i2 = SERVICE_TIMEOUT; i2 < length; i2++) {
                    alls = getServices(users[i2]);
                    for (i = SERVICE_TIMEOUT; i < alls.size() && res.size() < maxNum; i++) {
                        res.add(makeRunningServiceInfoLocked((ServiceRecord) alls.valueAt(i)));
                    }
                }
                for (i = SERVICE_TIMEOUT; i < this.mRestartingServices.size() && res.size() < maxNum; i++) {
                    r = (ServiceRecord) this.mRestartingServices.get(i);
                    if (r.userId == userId) {
                        info = makeRunningServiceInfoLocked(r);
                        info.restarting = r.nextRestartTime;
                        res.add(info);
                    }
                }
            }
            Binder.restoreCallingIdentity(ident);
            return res;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public PendingIntent getRunningServiceControlPanelLocked(ComponentName name) {
        ServiceRecord r = getServiceByName(name, UserHandle.getUserId(Binder.getCallingUid()));
        if (r != null) {
            for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
                ArrayList<ConnectionRecord> conn = (ArrayList) r.connections.valueAt(conni);
                for (int i = SERVICE_TIMEOUT; i < conn.size(); i++) {
                    if (((ConnectionRecord) conn.get(i)).clientIntent != null) {
                        return ((ConnectionRecord) conn.get(i)).clientIntent;
                    }
                }
            }
        }
        return null;
    }

    void serviceTimeout(ProcessRecord proc) {
        String anrMessage = null;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (proc.executingServices.size() == 0 || proc.thread == null) {
                    return;
                }
                long maxTime = SystemClock.uptimeMillis() - ((long) (proc.execServicesFg ? SERVICE_TIMEOUT : SERVICE_BACKGROUND_TIMEOUT));
                ServiceRecord timeout = null;
                long nextTime = 0;
                for (int i = proc.executingServices.size() - 1; i >= 0; i--) {
                    ServiceRecord sr = (ServiceRecord) proc.executingServices.valueAt(i);
                    if (sr.executingStart < maxTime) {
                        timeout = sr;
                        break;
                    }
                    if (sr.executingStart > nextTime) {
                        nextTime = sr.executingStart;
                    }
                }
                if (timeout == null || !this.mAm.mLruProcesses.contains(proc)) {
                    Message msg = this.mAm.mHandler.obtainMessage(12);
                    msg.obj = proc;
                    if (nextTime == 0) {
                        Slog.e(TAG, "nextTime invaild, remove the message");
                        this.mAm.mHandler.removeMessages(12, proc);
                    } else {
                        this.mAm.mHandler.sendMessageAtTime(msg, ((long) (proc.execServicesFg ? SERVICE_TIMEOUT : SERVICE_BACKGROUND_TIMEOUT)) + nextTime);
                    }
                } else {
                    Slog.w(TAG, "Timeout executing service: " + timeout);
                    Writer sw = new StringWriter();
                    PrintWriter fastPrintWriter = new FastPrintWriter(sw, SERVICE_RESCHEDULE, DumpState.DUMP_PROVIDERS);
                    fastPrintWriter.println(timeout);
                    timeout.dump(fastPrintWriter, "    ");
                    fastPrintWriter.close();
                    this.mLastAnrDump = sw.toString();
                    this.mAm.mHandler.removeCallbacks(this.mLastAnrDumpClearer);
                    this.mAm.mHandler.postDelayed(this.mLastAnrDumpClearer, 7200000);
                    anrMessage = "executing service " + timeout.shortName;
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
                if (anrMessage != null) {
                    this.mAm.mAppErrors.appNotResponding(proc, null, null, SERVICE_RESCHEDULE, anrMessage);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void scheduleServiceTimeoutLocked(ProcessRecord proc) {
        if (proc.executingServices.size() == 0 || proc.thread == null) {
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "no need to schedule service timeout");
            return;
        }
        long now = SystemClock.uptimeMillis();
        Message msg = this.mAm.mHandler.obtainMessage(12);
        msg.obj = proc;
        this.mAm.mHandler.sendMessageAtTime(msg, ((long) (proc.execServicesFg ? SERVICE_TIMEOUT : SERVICE_BACKGROUND_TIMEOUT)) + now);
        msg = this.mAm.mHandler.obtainMessage(99);
        msg.obj = proc;
        this.mAm.mHandler.sendMessageDelayed(msg, 3000);
    }

    List<ServiceRecord> collectServicesToDumpLocked(ItemMatcher matcher, String dumpPackage) {
        int i = SERVICE_TIMEOUT;
        ArrayList<ServiceRecord> services = new ArrayList();
        int[] usersFromAms = this.mAm.mUserController.getUsers();
        int[] users = new int[(usersFromAms.length + 1)];
        System.arraycopy(usersFromAms, SERVICE_TIMEOUT, users, SERVICE_TIMEOUT, usersFromAms.length);
        users[usersFromAms.length] = 2147383647;
        int length = users.length;
        while (i < length) {
            ServiceMap smap = getServiceMap(users[i]);
            if (smap.mServicesByName.size() > 0) {
                for (int si = SERVICE_TIMEOUT; si < smap.mServicesByName.size(); si++) {
                    ServiceRecord r = (ServiceRecord) smap.mServicesByName.valueAt(si);
                    if (matcher.match(r, r.name) && (dumpPackage == null || dumpPackage.equals(r.appInfo.packageName))) {
                        services.add(r);
                    }
                }
            }
            i++;
        }
        return services;
    }

    ServiceDumper newServiceDumperLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        return new ServiceDumper(fd, pw, args, opti, dumpAll, dumpPackage);
    }

    protected boolean dumpService(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        ArrayList<ServiceRecord> services = new ArrayList();
        synchronized (this.mAm) {
            int i;
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                int[] usersFromAms = this.mAm.mUserController.getUsers();
                int[] users = new int[(usersFromAms.length + 1)];
                System.arraycopy(usersFromAms, SERVICE_TIMEOUT, users, SERVICE_TIMEOUT, usersFromAms.length);
                users[usersFromAms.length] = 2147383647;
                int length;
                int i2;
                ServiceMap smap;
                ArrayMap<ComponentName, ServiceRecord> alls;
                if ("all".equals(name)) {
                    length = users.length;
                    for (i2 = SERVICE_TIMEOUT; i2 < length; i2++) {
                        smap = (ServiceMap) this.mServiceMap.get(users[i2]);
                        if (smap != null) {
                            alls = smap.mServicesByName;
                            for (i = SERVICE_TIMEOUT; i < alls.size(); i++) {
                                services.add((ServiceRecord) alls.valueAt(i));
                            }
                        }
                    }
                } else {
                    Object componentName = name != null ? ComponentName.unflattenFromString(name) : null;
                    int objectId = SERVICE_TIMEOUT;
                    if (componentName == null) {
                        objectId = Integer.parseInt(name, 16);
                        name = null;
                        componentName = null;
                    }
                    length = users.length;
                    for (i2 = SERVICE_TIMEOUT; i2 < length; i2++) {
                        smap = (ServiceMap) this.mServiceMap.get(users[i2]);
                        if (smap != null) {
                            alls = smap.mServicesByName;
                            for (i = SERVICE_TIMEOUT; i < alls.size(); i++) {
                                ServiceRecord r1 = (ServiceRecord) alls.valueAt(i);
                                if (componentName != null) {
                                    if (r1.name.equals(componentName)) {
                                        services.add(r1);
                                    }
                                } else if (name != null) {
                                    if (r1.name.flattenToString().contains(name)) {
                                        services.add(r1);
                                    }
                                } else if (System.identityHashCode(r1) == objectId) {
                                    services.add(r1);
                                }
                            }
                            continue;
                        }
                    }
                }
            } catch (RuntimeException e) {
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (services.size() <= 0) {
            return SERVICE_RESCHEDULE;
        }
        boolean needSep = SERVICE_RESCHEDULE;
        for (i = SERVICE_TIMEOUT; i < services.size(); i++) {
            if (needSep) {
                pw.println();
            }
            needSep = LOG_SERVICE_START_STOP;
            dumpService("", fd, pw, (ServiceRecord) services.get(i), args, dumpAll);
        }
        return LOG_SERVICE_START_STOP;
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
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (r.app != null && r.app.thread != null) {
            pw.print(prefix);
            pw.println("  Client:");
            pw.flush();
            try {
                tp = new TransferPipe();
                r.app.thread.dumpService(tp.getWriteFd().getFileDescriptor(), r, args);
                tp.setBufferPrefix(prefix + "    ");
                tp.go(fd);
                tp.kill();
            } catch (IOException e) {
                pw.println(prefix + "    Failure while dumping the service: " + e);
            } catch (RemoteException e2) {
                pw.println(prefix + "    Got a RemoteException while dumping the service");
            } catch (Throwable th) {
                tp.kill();
            }
        }
    }
}
