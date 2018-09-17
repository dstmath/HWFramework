package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
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
import android.content.Intent.FilterComparison;
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
import android.os.RemoteCallback;
import android.os.RemoteCallback.OnResultListener;
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
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.webkit.WebViewZygote;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.FastPrintWriter;
import com.android.server.LocalServices;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pg.PGManagerInternal;
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
    static final int CHECK_INTERVAL = ((int) (ActivityManagerService.SCALE_ANR * 10000.0f));
    private static final boolean DEBUG_DELAYED_SERVICE = ActivityManagerDebugConfig.DEBUG_SERVICE;
    private static final boolean DEBUG_DELAYED_STARTS = DEBUG_DELAYED_SERVICE;
    private static final String HW_PARENT_CONTROL = "com.huawei.parentcontrol";
    static final boolean IS_FPGA = boardname.contains("fpga");
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    private static final boolean LOG_SERVICE_START_STOP = true;
    static final int SERVICE_BACKGROUND_TIMEOUT = (SERVICE_TIMEOUT * 10);
    private static final boolean SERVICE_RESCHEDULE = SystemProperties.getBoolean("ro.am.reschedule_service", false);
    static final int SERVICE_START_FOREGROUND_TIMEOUT = 5000;
    static final int SERVICE_TIMEOUT = (IS_FPGA ? (int) (ActivityManagerService.SCALE_ANR * 2.0E7f) : (int) (ActivityManagerService.SCALE_ANR * 20000.0f));
    static final int SERVICE_WAIT_TIMEOUT = 3000;
    private static final String TAG = "ActivityManager";
    private static final String TAG_MU = (TAG + "_MU");
    private static final String TAG_SERVICE = (TAG + ActivityManagerDebugConfig.POSTFIX_SERVICE);
    private static final String TAG_SERVICE_EXECUTING = (TAG + ActivityManagerDebugConfig.POSTFIX_SERVICE_EXECUTING);
    static String boardname = SystemProperties.get("ro.board.boardname", "0");
    private static final boolean enableANRWarning = SystemProperties.getBoolean("ro.anr.warning.enable", false);
    final ActivityManagerService mAm;
    final ArrayList<ServiceRecord> mDestroyingServices = new ArrayList();
    String mLastAnrDump;
    final Runnable mLastAnrDumpClearer = new Runnable() {
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
    final int mMaxStartingBackground;
    final ArrayList<ServiceRecord> mPendingServices = new ArrayList();
    final ArrayList<ServiceRecord> mRestartingServices = new ArrayList();
    boolean mScreenOn = true;
    final ArrayMap<IBinder, ArrayList<ConnectionRecord>> mServiceConnections = new ArrayMap();
    final SparseArray<ServiceMap> mServiceMap = new SparseArray();
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

    final class ServiceDumper {
        private final String[] args;
        private final boolean dumpAll;
        private final String dumpPackage;
        private final FileDescriptor fd;
        private final ItemMatcher matcher;
        private boolean needSep = false;
        private final long nowReal = SystemClock.elapsedRealtime();
        private boolean printed = false;
        private boolean printedAnything = false;
        private final PrintWriter pw;
        private final ArrayList<ServiceRecord> services = new ArrayList();

        ServiceDumper(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
            this.fd = fd;
            this.pw = pw;
            this.args = args;
            this.dumpAll = dumpAll;
            this.dumpPackage = dumpPackage;
            this.matcher = new ItemMatcher();
            this.matcher.build(args, opti);
            for (int user : ActiveServices.this.mAm.mUserController.getUsers()) {
                ServiceMap smap = ActiveServices.this.getServiceMapLocked(user);
                if (smap.mServicesByName.size() > 0) {
                    for (int si = 0; si < smap.mServicesByName.size(); si++) {
                        ServiceRecord r = (ServiceRecord) smap.mServicesByName.valueAt(si);
                        if (this.matcher.match(r, r.name)) {
                            if (dumpPackage != null) {
                                if ((dumpPackage.equals(r.appInfo.packageName) ^ 1) != 0) {
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
                for (int user : ActiveServices.this.mAm.mUserController.getUsers()) {
                    int serviceIdx = 0;
                    while (serviceIdx < this.services.size() && ((ServiceRecord) this.services.get(serviceIdx)).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = false;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = false;
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
                for (int user : ActiveServices.this.mAm.mUserController.getUsers()) {
                    int serviceIdx = 0;
                    while (serviceIdx < this.services.size() && ((ServiceRecord) this.services.get(serviceIdx)).userId != user) {
                        serviceIdx++;
                    }
                    this.printed = false;
                    if (serviceIdx < this.services.size()) {
                        this.needSep = false;
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
            TimeUtils.formatDuration(r.createTime, this.nowReal, this.pw);
            this.pw.print(" started=");
            this.pw.print(r.startRequested);
            this.pw.print(" connections=");
            this.pw.println(r.connections.size());
            if (r.connections.size() > 0) {
                this.pw.println("    Connections:");
                for (int conni = 0; conni < r.connections.size(); conni++) {
                    ArrayList<ConnectionRecord> clist = (ArrayList) r.connections.valueAt(conni);
                    for (int i = 0; i < clist.size(); i++) {
                        String toShortString;
                        ConnectionRecord conn = (ConnectionRecord) clist.get(i);
                        this.pw.print("      ");
                        this.pw.print(conn.binding.intent.intent.getIntent().toShortString(false, false, false, false));
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
                        thread.dumpService(tp.getWriteFd(), r, this.args);
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
                    this.needSep = true;
                }
            }
        }

        private void dumpUserRemainsLocked(int user) {
            int si;
            ServiceRecord r;
            ServiceMap smap = ActiveServices.this.getServiceMapLocked(user);
            this.printed = false;
            int SN = smap.mDelayedStartList.size();
            for (si = 0; si < SN; si++) {
                r = (ServiceRecord) smap.mDelayedStartList.get(si);
                if (this.matcher.match(r, r.name) && (this.dumpPackage == null || (this.dumpPackage.equals(r.appInfo.packageName) ^ 1) == 0)) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        this.pw.println("  User " + user + " delayed start services:");
                        this.printed = true;
                    }
                    this.printedAnything = true;
                    this.pw.print("  * Delayed start ");
                    this.pw.println(r);
                }
            }
            this.printed = false;
            SN = smap.mStartingBackground.size();
            for (si = 0; si < SN; si++) {
                r = (ServiceRecord) smap.mStartingBackground.get(si);
                if (this.matcher.match(r, r.name) && (this.dumpPackage == null || (this.dumpPackage.equals(r.appInfo.packageName) ^ 1) == 0)) {
                    if (!this.printed) {
                        if (this.printedAnything) {
                            this.pw.println();
                        }
                        this.pw.println("  User " + user + " starting in background:");
                        this.printed = true;
                    }
                    this.printedAnything = true;
                    this.pw.print("  * Starting bg ");
                    this.pw.println(r);
                }
            }
        }

        private void dumpRemainsLocked() {
            int i;
            ServiceRecord r;
            if (ActiveServices.this.mPendingServices.size() > 0) {
                this.printed = false;
                for (i = 0; i < ActiveServices.this.mPendingServices.size(); i++) {
                    r = (ServiceRecord) ActiveServices.this.mPendingServices.get(i);
                    if (this.matcher.match(r, r.name) && (this.dumpPackage == null || (this.dumpPackage.equals(r.appInfo.packageName) ^ 1) == 0)) {
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
            if (ActiveServices.this.mRestartingServices.size() > 0) {
                this.printed = false;
                for (i = 0; i < ActiveServices.this.mRestartingServices.size(); i++) {
                    r = (ServiceRecord) ActiveServices.this.mRestartingServices.get(i);
                    if (this.matcher.match(r, r.name) && (this.dumpPackage == null || (this.dumpPackage.equals(r.appInfo.packageName) ^ 1) == 0)) {
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
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (ActiveServices.this.mDestroyingServices.size() > 0) {
                this.printed = false;
                for (i = 0; i < ActiveServices.this.mDestroyingServices.size(); i++) {
                    r = (ServiceRecord) ActiveServices.this.mDestroyingServices.get(i);
                    if (this.matcher.match(r, r.name) && (this.dumpPackage == null || (this.dumpPackage.equals(r.appInfo.packageName) ^ 1) == 0)) {
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
                        this.pw.println(r);
                        r.dump(this.pw, "    ");
                    }
                }
                this.needSep = true;
            }
            if (this.dumpAll) {
                this.printed = false;
                for (int ic = 0; ic < ActiveServices.this.mServiceConnections.size(); ic++) {
                    ArrayList<ConnectionRecord> r2 = (ArrayList) ActiveServices.this.mServiceConnections.valueAt(ic);
                    for (i = 0; i < r2.size(); i++) {
                        ConnectionRecord cr = (ConnectionRecord) r2.get(i);
                        if (this.matcher.match(cr.binding.service, cr.binding.service.name) && (this.dumpPackage == null || (cr.binding.client != null && (this.dumpPackage.equals(cr.binding.client.info.packageName) ^ 1) == 0))) {
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
                for (int user : ActiveServices.this.mAm.mUserController.getUsers()) {
                    boolean printedUser = false;
                    ServiceMap smap = (ServiceMap) ActiveServices.this.mServiceMap.get(user);
                    if (smap != null) {
                        for (i = smap.mActiveForegroundApps.size() - 1; i >= 0; i--) {
                            ActiveForegroundApp aa = (ActiveForegroundApp) smap.mActiveForegroundApps.valueAt(i);
                            if (this.dumpPackage == null || (this.dumpPackage.equals(aa.mPackageName) ^ 1) == 0) {
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
                                this.pw.print(i);
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
                        if (smap.hasMessagesOrCallbacks()) {
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
        final ArrayMap<String, ActiveForegroundApp> mActiveForegroundApps = new ArrayMap();
        boolean mActiveForegroundAppsChanged;
        final ArrayList<ServiceRecord> mDelayedStartList = new ArrayList();
        final ArrayMap<FilterComparison, ServiceRecord> mServicesByIntent = new ArrayMap();
        final ArrayMap<ComponentName, ServiceRecord> mServicesByName = new ArrayMap();
        final ArrayList<ServiceRecord> mStartingBackground = new ArrayList();
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
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case 2:
                    ActiveServices.this.updateForegroundApps(this);
                    return;
                default:
                    return;
            }
        }

        void ensureNotStartingBackgroundLocked(ServiceRecord r) {
            if (this.mStartingBackground.remove(r)) {
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    Slog.v(ActiveServices.TAG_SERVICE, "No longer background starting: " + r);
                }
                rescheduleDelayedStartsLocked();
            }
            if (this.mDelayedStartList.remove(r) && ActiveServices.DEBUG_DELAYED_STARTS) {
                Slog.v(ActiveServices.TAG_SERVICE, "No longer delaying start: " + r);
            }
        }

        void rescheduleDelayedStartsLocked() {
            ServiceRecord r;
            removeMessages(1);
            long now = SystemClock.uptimeMillis();
            int i = 0;
            int N = this.mStartingBackground.size();
            while (i < N) {
                r = (ServiceRecord) this.mStartingBackground.get(i);
                if (r.startingBgTimeout <= now) {
                    Slog.i(ActiveServices.TAG, "Waited long enough for: " + r);
                    this.mStartingBackground.remove(i);
                    N--;
                    i--;
                }
                i++;
            }
            while (this.mDelayedStartList.size() > 0 && this.mStartingBackground.size() < ActiveServices.this.mMaxStartingBackground) {
                r = (ServiceRecord) this.mDelayedStartList.remove(0);
                if (ActiveServices.DEBUG_DELAYED_STARTS) {
                    Slog.v(ActiveServices.TAG_SERVICE, "REM FR DELAY LIST (exec next): " + r);
                }
                if (r.pendingStarts.size() <= 0) {
                    Slog.w(ActiveServices.TAG, "**** NO PENDING STARTS! " + r + " startReq=" + r.startRequested + " delayedStop=" + r.delayedStop);
                } else {
                    if (ActiveServices.DEBUG_DELAYED_SERVICE && this.mDelayedStartList.size() > 0) {
                        Slog.v(ActiveServices.TAG_SERVICE, "Remaining delayed list:");
                        for (i = 0; i < this.mDelayedStartList.size(); i++) {
                            Slog.v(ActiveServices.TAG_SERVICE, "  #" + i + ": " + this.mDelayedStartList.get(i));
                        }
                    }
                    r.delayed = false;
                    try {
                        ProcessRecord servicePR = ActiveServices.this.mAm.getProcessRecordLocked(r.processName, r.appInfo.uid, false);
                        if (!(r.appInfo.uid < 10000 || servicePR == null || servicePR.thread == null)) {
                            LogPower.push(148, "serviceboot", r.packageName, Integer.toString(servicePR.pid));
                        }
                        ActiveServices.this.startServiceInnerLocked(this, ((StartItem) r.pendingStarts.get(0)).intent, r, false, true);
                    } catch (TransactionTooLargeException e) {
                    }
                }
            }
            if (this.mStartingBackground.size() > 0) {
                ServiceRecord next = (ServiceRecord) this.mStartingBackground.get(0);
                long when = next.startingBgTimeout > now ? next.startingBgTimeout : now;
                if (ActiveServices.DEBUG_DELAYED_SERVICE) {
                    Slog.v(ActiveServices.TAG_SERVICE, "Top bg start is " + next + ", can delay others up to " + when);
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

        /* synthetic */ ServiceRestarter(ActiveServices this$0, ServiceRestarter -this1) {
            this();
        }

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

    public ActiveServices(ActivityManagerService service) {
        this.mAm = service;
        int maxBg = 0;
        try {
            maxBg = Integer.parseInt(SystemProperties.get("ro.config.max_starting_bg", "0"));
        } catch (RuntimeException e) {
        }
        if (maxBg <= 0) {
            maxBg = ActivityManager.isLowRamDeviceStatic() ? 1 : 8;
        }
        this.mMaxStartingBackground = maxBg;
    }

    ServiceRecord getServiceByNameLocked(ComponentName name, int callingUser) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "getServiceByNameLocked(" + name + "), callingUser = " + callingUser);
        }
        return (ServiceRecord) getServiceMapLocked(callingUser).mServicesByName.get(name);
    }

    boolean hasBackgroundServicesLocked(int callingUser) {
        ServiceMap smap = (ServiceMap) this.mServiceMap.get(callingUser);
        if (smap == null || smap.mStartingBackground.size() < this.mMaxStartingBackground) {
            return false;
        }
        return true;
    }

    private ServiceMap getServiceMapLocked(int callingUser) {
        ServiceMap smap = (ServiceMap) this.mServiceMap.get(callingUser);
        if (smap != null) {
            return smap;
        }
        smap = new ServiceMap(this.mAm.mHandler.getLooper(), callingUser);
        this.mServiceMap.put(callingUser, smap);
        return smap;
    }

    ArrayMap<ComponentName, ServiceRecord> getServicesLocked(int callingUser) {
        return getServiceMapLocked(callingUser).mServicesByName;
    }

    ComponentName startServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int callingPid, int callingUid, boolean fgRequired, String callingPackage, int userId) throws TransactionTooLargeException {
        boolean callerFg;
        if (DEBUG_DELAYED_STARTS) {
            Slog.v(TAG_SERVICE, "startService: " + service + " type=" + resolvedType + " args=" + service.getExtras());
        }
        if (caller != null) {
            ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
            if (callerApp == null) {
                throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting service " + service);
            }
            callerFg = callerApp.setSchedGroup != 0;
        } else {
            callerFg = true;
        }
        this.mAm.setServiceFlagLocked(1);
        ServiceLookupResult res = retrieveServiceLocked(service, resolvedType, callingPackage, callingPid, callingUid, userId, true, callerFg, false);
        this.mAm.setServiceFlagLocked(0);
        if (res == null) {
            return null;
        }
        if (res.record == null) {
            return new ComponentName("!", res.permission != null ? res.permission : "private to package");
        }
        ServiceRecord r = res.record;
        if (this.mAm.mUserController.exists(r.userId)) {
            if (!(r.startRequested || (fgRequired ^ 1) == 0)) {
                int allowed = this.mAm.getAppStartModeLocked(r.appInfo.uid, r.packageName, r.appInfo.targetSdkVersion, callingPid, false, false);
                if (allowed != 0) {
                    Slog.w(TAG, "Background start not allowed: service " + service + " to " + r.name.flattenToShortString() + " from pid=" + callingPid + " uid=" + callingUid + " pkg=" + callingPackage);
                    if (allowed == 1) {
                        return null;
                    }
                    return new ComponentName("?", "app is in background uid " + ((UidRecord) this.mAm.mActiveUids.get(r.appInfo.uid)));
                }
            }
            PGManagerInternal pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
            if (!(pgm == null || callingPackage == null)) {
                if ((callingPackage.equals(r.name.getPackageName()) ^ 1) != 0) {
                    if (pgm.isServiceProxy(r.name, null)) {
                        Slog.i(TAG, "start service is proxy: " + r.name);
                        return null;
                    }
                }
            }
            NeededUriGrants neededGrants = this.mAm.checkGrantUriPermissionFromIntentLocked(callingUid, r.packageName, service, service.getFlags(), null, r.userId);
            if (this.mAm.mPermissionReviewRequired && !requestStartTargetPermissionsReviewIfNeededLocked(r, callingPackage, callingUid, service, callerFg, userId)) {
                return null;
            }
            if (unscheduleServiceRestartLocked(r, callingUid, false) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "START SERVICE WHILE RESTART PENDING: " + r);
            }
            r.lastActivity = SystemClock.uptimeMillis();
            r.startRequested = true;
            r.delayedStop = false;
            r.fgRequired = fgRequired;
            r.pendingStarts.add(new StartItem(r, false, r.makeNextStartId(), service, neededGrants, callingUid));
            ServiceMap smap = getServiceMapLocked(r.userId);
            boolean addToStarting = false;
            if (!callerFg && (fgRequired ^ 1) != 0 && r.app == null && this.mAm.mUserController.hasStartedUserState(r.userId)) {
                ProcessRecord proc = this.mAm.getProcessRecordLocked(r.processName, r.appInfo.uid, false);
                if (proc == null || proc.curProcState > 12) {
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
                        r.delayed = true;
                        return r.name;
                    } else {
                        if (DEBUG_DELAYED_STARTS) {
                            Slog.v(TAG_SERVICE, "Not delaying: " + r);
                        }
                        addToStarting = true;
                    }
                } else if (proc.curProcState >= 11) {
                    addToStarting = true;
                    if (DEBUG_DELAYED_STARTS) {
                        Slog.v(TAG_SERVICE, "Not delaying, but counting as bg: " + r);
                    }
                } else if (DEBUG_DELAYED_STARTS) {
                    StringBuilder stringBuilder = new StringBuilder(128);
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
                if (callerFg || fgRequired) {
                    Slog.v(TAG_SERVICE, "Not potential delay (callerFg=" + callerFg + " uid=" + callingUid + " pid=" + callingPid + " fgRequired=" + fgRequired + "): " + r);
                } else if (r.app != null) {
                    Slog.v(TAG_SERVICE, "Not potential delay (cur app=" + r.app + "): " + r);
                } else {
                    Slog.v(TAG_SERVICE, "Not potential delay (user " + r.userId + " not started): " + r);
                }
            }
            if (r.appInfo.uid >= 10000 && callingPackage != null) {
                if ((callingPackage.equals(r.packageName) ^ 1) != 0) {
                    LogPower.push(148, "serviceboot", r.packageName, Integer.toString(0), new String[]{callingPackage});
                }
            }
            if (HwPCUtils.isPcCastModeInServer() && this.mAm.mHwAMSEx != null) {
                ProcessRecord pr = this.mAm.getRecordForAppLocked(caller);
                if (pr != null) {
                    this.mAm.mHwAMSEx.storeDisplayIdForPCMode(r.packageName, pr.mDisplayId, this.mAm);
                }
            }
            return startServiceInnerLocked(smap, service, r, callerFg, addToStarting);
        }
        Slog.w(TAG, "Trying to start service with non-existent user! " + r.userId);
        return null;
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(ServiceRecord r, String callingPackage, int callingUid, Intent service, boolean callerFg, int userId) {
        if (!this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(r.packageName, r.userId)) {
            return true;
        }
        if (callerFg) {
            ActivityManagerService activityManagerService = this.mAm;
            Intent[] intentArr = new Intent[]{service};
            String[] strArr = new String[1];
            strArr[0] = service.resolveType(this.mAm.mContext.getContentResolver());
            IIntentSender target = activityManagerService.getIntentSenderLocked(4, callingPackage, callingUid, userId, null, null, 0, intentArr, strArr, 1409286144, null);
            final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent.addFlags(276824064);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", r.packageName);
            intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                Slog.i(TAG, "u" + r.userId + " Launching permission review for package " + r.packageName);
            }
            final int i = userId;
            this.mAm.mHandler.post(new Runnable() {
                public void run() {
                    ActiveServices.this.mAm.mContext.startActivityAsUser(intent, new UserHandle(i));
                }
            });
            return false;
        }
        Slog.w(TAG, "u" + r.userId + " Starting a service in package" + r.packageName + " requires a permissions review");
        return false;
    }

    ComponentName startServiceInnerLocked(ServiceMap smap, Intent service, ServiceRecord r, boolean callerFg, boolean addToStarting) throws TransactionTooLargeException {
        ServiceState stracker = r.getTracker();
        if (stracker != null) {
            stracker.setStarted(true, this.mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
        }
        r.callStart = false;
        synchronized (r.stats.getBatteryStats()) {
            r.stats.startRunningLocked();
        }
        String error = bringUpServiceLocked(r, service.getFlags(), callerFg, false, false);
        if (error != null) {
            return new ComponentName("!!", error);
        }
        if (r.startRequested && addToStarting) {
            boolean first = smap.mStartingBackground.size() == 0;
            smap.mStartingBackground.add(r);
            r.startingBgTimeout = SystemClock.uptimeMillis() + this.mAm.mConstants.BG_START_TIMEOUT;
            if (DEBUG_DELAYED_SERVICE) {
                RuntimeException here = new RuntimeException("here");
                here.fillInStackTrace();
                Slog.v(TAG_SERVICE, "Starting background (first=" + first + "): " + r, here);
            } else if (DEBUG_DELAYED_STARTS) {
                Slog.v(TAG_SERVICE, "Starting background (first=" + first + "): " + r);
            }
            if (first) {
                smap.rescheduleDelayedStartsLocked();
            }
        } else if (callerFg || r.fgRequired) {
            smap.ensureNotStartingBackgroundLocked(r);
        }
        return r.name;
    }

    private void stopServiceLocked(ServiceRecord service) {
        if (service.delayed) {
            if (DEBUG_DELAYED_STARTS) {
                Slog.v(TAG_SERVICE, "Delaying stop of pending: " + service);
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

    int stopServiceLocked(IApplicationThread caller, Intent service, String resolvedType, int userId) {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "stopService: " + service + " type=" + resolvedType);
        }
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
        if (caller == null || callerApp != null) {
            ServiceLookupResult r = retrieveServiceLocked(service, resolvedType, null, Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, false);
            if (r == null) {
                return 0;
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
                if (service != null && service.appInfo.uid == uid && service.startRequested && this.mAm.getAppStartModeLocked(service.appInfo.uid, service.packageName, service.appInfo.targetSdkVersion, -1, false, false) != 0) {
                    if (stopping == null) {
                        stopping = new ArrayList();
                    }
                    String compName = service.name.flattenToShortString();
                    EventLogTags.writeAmStopIdleService(service.appInfo.uid, compName);
                    StringBuilder sb = new StringBuilder(64);
                    sb.append("Stopping service due to app idle: ");
                    UserHandle.formatUid(sb, service.appInfo.uid);
                    sb.append(" ");
                    TimeUtils.formatDuration(service.createTime - SystemClock.elapsedRealtime(), sb);
                    sb.append(" ");
                    sb.append(compName);
                    Slog.w(TAG, sb.toString());
                    stopping.add(service);
                }
            }
            if (stopping != null) {
                for (i = stopping.size() - 1; i >= 0; i--) {
                    service = (ServiceRecord) stopping.get(i);
                    service.delayed = false;
                    services.ensureNotStartingBackgroundLocked(service);
                    stopServiceLocked(service);
                }
            }
        }
    }

    IBinder peekServiceLocked(Intent service, String resolvedType, String callingPackage) {
        ServiceLookupResult r = retrieveServiceLocked(service, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), UserHandle.getCallingUserId(), false, false, false);
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
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "stopServiceToken: " + className + " " + token + " startId=" + startId);
        }
        ServiceRecord r = findServiceLocked(className, token, UserHandle.getCallingUserId());
        if (r == null) {
            return false;
        }
        if (startId >= 0) {
            StartItem si = r.findDeliveredStart(startId, false);
            if (si != null) {
                while (r.deliveredStarts.size() > 0) {
                    StartItem cur = (StartItem) r.deliveredStarts.remove(0);
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
                Slog.w(TAG, "stopServiceToken startId " + startId + " is last, but have " + r.deliveredStarts.size() + " remaining args");
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
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    boolean foregroundAppShownEnoughLocked(ActiveForegroundApp aa, long nowElapsed) {
        if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
            Slog.d(TAG, "Shown enough: pkg=" + aa.mPackageName + ", uid=" + aa.mUid);
        }
        aa.mHideTime = JobStatus.NO_LATEST_RUNTIME;
        long minTime;
        if (aa.mShownWhileTop) {
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return true;
            }
            Slog.d(TAG, "YES - shown while on top");
            return true;
        } else if (this.mScreenOn || aa.mShownWhileScreenOn) {
            long j;
            long j2 = aa.mStartVisibleTime;
            if (aa.mStartTime != aa.mStartVisibleTime) {
                j = this.mAm.mConstants.FGSERVICE_SCREEN_ON_AFTER_TIME;
            } else {
                j = this.mAm.mConstants.FGSERVICE_MIN_SHOWN_TIME;
            }
            minTime = j2 + j;
            if (nowElapsed >= minTime) {
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(TAG, "YES - shown long enough with screen on");
                }
                return true;
            }
            long reportTime = nowElapsed + this.mAm.mConstants.FGSERVICE_MIN_REPORT_TIME;
            if (reportTime <= minTime) {
                reportTime = minTime;
            }
            aa.mHideTime = reportTime;
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return false;
            }
            Slog.d(TAG, "NO -- wait " + (aa.mHideTime - nowElapsed) + " with screen on");
            return false;
        } else {
            minTime = aa.mEndTime + this.mAm.mConstants.FGSERVICE_SCREEN_ON_BEFORE_TIME;
            if (nowElapsed >= minTime) {
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(TAG, "YES - gone long enough with screen off");
                }
                return true;
            }
            aa.mHideTime = minTime;
            if (!ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                return false;
            }
            Slog.d(TAG, "NO -- wait " + (aa.mHideTime - nowElapsed) + " with screen off");
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:53:0x011a, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
            r22 = (android.app.NotificationManager) r36.mAm.mContext.getSystemService("notification");
            r4 = r36.mAm.mContext;
     */
    /* JADX WARNING: Missing block: B:54:0x0132, code:
            if (r11 == null) goto L_0x02de;
     */
    /* JADX WARNING: Missing block: B:55:0x0134, code:
            r16 = 0;
     */
    /* JADX WARNING: Missing block: B:57:0x013c, code:
            if (r16 >= r11.size()) goto L_0x016e;
     */
    /* JADX WARNING: Missing block: B:58:0x013e, code:
            r10 = (com.android.server.am.ActiveServices.ActiveForegroundApp) r11.get(r16);
     */
    /* JADX WARNING: Missing block: B:59:0x0148, code:
            if (r10.mLabel != null) goto L_0x0165;
     */
    /* JADX WARNING: Missing block: B:60:0x014a, code:
            r31 = r4.getPackageManager();
     */
    /* JADX WARNING: Missing block: B:62:?, code:
            r10.mLabel = r31.getApplicationInfoAsUser(r10.mPackageName, 4202496, r37.mUserId).loadLabel(r31);
     */
    /* JADX WARNING: Missing block: B:65:0x0169, code:
            r10.mLabel = r10.mPackageName;
     */
    /* JADX WARNING: Missing block: B:66:0x016e, code:
            r26 = android.os.SystemClock.elapsedRealtime();
            r28 = r26;
     */
    /* JADX WARNING: Missing block: B:67:0x0179, code:
            if (r11.size() != 1) goto L_0x0251;
     */
    /* JADX WARNING: Missing block: B:68:0x017b, code:
            r6 = new android.content.Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            r6.setData(android.net.Uri.fromParts(com.android.server.am.HwBroadcastRadarUtil.KEY_PACKAGE, ((com.android.server.am.ActiveServices.ActiveForegroundApp) r11.get(0)).mPackageName, null));
            r32 = r4.getString(17040044, new java.lang.Object[]{((com.android.server.am.ActiveServices.ActiveForegroundApp) r11.get(0)).mLabel});
            r18 = r4.getString(17040047);
            r30 = new java.lang.String[]{((com.android.server.am.ActiveServices.ActiveForegroundApp) r11.get(0)).mPackageName};
            r28 = ((com.android.server.am.ActiveServices.ActiveForegroundApp) r11.get(0)).mStartTime;
     */
    /* JADX WARNING: Missing block: B:69:0x01d0, code:
            r23 = new android.os.Bundle();
            r23.putStringArray("android.foregroundApps", r30);
            r7 = new android.app.Notification.Builder(r4, com.android.internal.notification.SystemNotificationChannels.FOREGROUND_SERVICE).addExtras(r23).setSmallIcon(17303403).setOngoing(true);
     */
    /* JADX WARNING: Missing block: B:70:0x01fa, code:
            if (r28 >= r26) goto L_0x02db;
     */
    /* JADX WARNING: Missing block: B:71:0x01fc, code:
            r5 = true;
     */
    /* JADX WARNING: Missing block: B:72:0x01fd, code:
            r0 = r22;
            r0.notifyAsUser(null, 40, r7.setShowWhen(r5).setWhen(java.lang.System.currentTimeMillis() - (r26 - r28)).setColor(r4.getColor(17170769)).setContentTitle(r32).setContentText(r18).setContentIntent(android.app.PendingIntent.getActivityAsUser(r4, 0, r6, 134217728, null, new android.os.UserHandle(r37.mUserId))).build(), new android.os.UserHandle(r37.mUserId));
     */
    /* JADX WARNING: Missing block: B:73:0x0250, code:
            return;
     */
    /* JADX WARNING: Missing block: B:74:0x0251, code:
            r6 = new android.content.Intent("android.settings.FOREGROUND_SERVICES_SETTINGS");
            r30 = new java.lang.String[r11.size()];
            r16 = 0;
     */
    /* JADX WARNING: Missing block: B:76:0x0269, code:
            if (r16 >= r11.size()) goto L_0x028a;
     */
    /* JADX WARNING: Missing block: B:77:0x026b, code:
            r30[r16] = ((com.android.server.am.ActiveServices.ActiveForegroundApp) r11.get(r16)).mPackageName;
            r28 = java.lang.Math.min(r28, ((com.android.server.am.ActiveServices.ActiveForegroundApp) r11.get(r16)).mStartTime);
            r16 = r16 + 1;
     */
    /* JADX WARNING: Missing block: B:78:0x028a, code:
            r6.putExtra("packages", r30);
            r32 = r4.getString(17040045, new java.lang.Object[]{java.lang.Integer.valueOf(r11.size())});
            r18 = ((com.android.server.am.ActiveServices.ActiveForegroundApp) r11.get(0)).mLabel.toString();
            r16 = 1;
     */
    /* JADX WARNING: Missing block: B:80:0x02bc, code:
            if (r16 >= r11.size()) goto L_0x01d0;
     */
    /* JADX WARNING: Missing block: B:81:0x02be, code:
            r18 = r4.getString(17040046, new java.lang.Object[]{r18, ((com.android.server.am.ActiveServices.ActiveForegroundApp) r11.get(r16)).mLabel});
            r16 = r16 + 1;
     */
    /* JADX WARNING: Missing block: B:82:0x02db, code:
            r5 = false;
     */
    /* JADX WARNING: Missing block: B:83:0x02de, code:
            r22.cancelAsUser(null, 40, new android.os.UserHandle(r37.mUserId));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void updateForegroundApps(ServiceMap smap) {
        int i;
        Throwable th;
        ArrayList active = null;
        synchronized (this.mAm) {
            ActivityManagerService.boostPriorityForLockedSection();
            long now = SystemClock.elapsedRealtime();
            long nextUpdateTime = JobStatus.NO_LATEST_RUNTIME;
            if (smap != null) {
                ArrayList<ActiveForegroundApp> active2;
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(TAG, "Updating foreground apps for user " + smap.mUserId);
                }
                i = smap.mActiveForegroundApps.size() - 1;
                ArrayList<ActiveForegroundApp> active3 = null;
                while (i >= 0) {
                    try {
                        ActiveForegroundApp aa = (ActiveForegroundApp) smap.mActiveForegroundApps.valueAt(i);
                        if (aa.mEndTime != 0) {
                            if (foregroundAppShownEnoughLocked(aa, now)) {
                                smap.mActiveForegroundApps.removeAt(i);
                                smap.mActiveForegroundAppsChanged = true;
                                active2 = active3;
                                i--;
                                active3 = active2;
                            } else if (aa.mHideTime < nextUpdateTime) {
                                nextUpdateTime = aa.mHideTime;
                            }
                        }
                        if (aa.mAppOnTop) {
                            active2 = active3;
                        } else {
                            if (active3 == null) {
                                active2 = new ArrayList();
                            } else {
                                active2 = active3;
                            }
                            try {
                                if (ActivityManagerDebugConfig.HWFLOW) {
                                    Slog.i(TAG, " updateForegroundApps Adding active: pkg= " + aa.mPackageName + ", uid=" + aa.mUid);
                                }
                                active2.add(aa);
                            } catch (Throwable th2) {
                                th = th2;
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                        i--;
                        active3 = active2;
                    } catch (Throwable th3) {
                        th = th3;
                        active2 = active3;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                smap.removeMessages(2);
                if (nextUpdateTime < JobStatus.NO_LATEST_RUNTIME) {
                    if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                        Slog.d(TAG, "Next update time in: " + (nextUpdateTime - now));
                    }
                    smap.sendMessageAtTime(smap.obtainMessage(2), (SystemClock.uptimeMillis() + nextUpdateTime) - SystemClock.elapsedRealtime());
                    active = active3;
                } else {
                    active2 = active3;
                }
            }
            if (smap.mActiveForegroundAppsChanged) {
                smap.mActiveForegroundAppsChanged = false;
            } else {
                ActivityManagerService.resetPriorityAfterLockedSection();
                return;
            }
        }
        i++;
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
        ActiveForegroundApp active = (ActiveForegroundApp) smap.mActiveForegroundApps.get(r.packageName);
        if (active != null) {
            active.mNumActive--;
            if (active.mNumActive <= 0) {
                active.mEndTime = SystemClock.elapsedRealtime();
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(TAG, "Ended running of service");
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

    void updateScreenStateLocked(boolean screenOn) {
        if (this.mScreenOn != screenOn) {
            this.mScreenOn = screenOn;
            if (screenOn) {
                long nowElapsed = SystemClock.elapsedRealtime();
                if (ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE) {
                    Slog.d(TAG, "Screen turned on");
                }
                for (int i = this.mServiceMap.size() - 1; i >= 0; i--) {
                    ServiceMap smap = (ServiceMap) this.mServiceMap.valueAt(i);
                    long nextUpdateTime = JobStatus.NO_LATEST_RUNTIME;
                    boolean changed = false;
                    for (int j = smap.mActiveForegroundApps.size() - 1; j >= 0; j--) {
                        ActiveForegroundApp active = (ActiveForegroundApp) smap.mActiveForegroundApps.valueAt(j);
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

    void foregroundServiceProcStateChangedLocked(UidRecord uidRec) {
        ServiceMap smap = (ServiceMap) this.mServiceMap.get(UserHandle.getUserId(uidRec.uid));
        if (smap != null) {
            boolean changed = false;
            for (int j = smap.mActiveForegroundApps.size() - 1; j >= 0; j--) {
                ActiveForegroundApp active = (ActiveForegroundApp) smap.mActiveForegroundApps.valueAt(j);
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

    private void setServiceForegroundInnerLocked(ServiceRecord r, int id, Notification notification, int flags) {
        boolean z = false;
        ServiceMap smap;
        if (id == 0) {
            if (r.isForeground) {
                smap = getServiceMapLocked(r.userId);
                if (smap != null) {
                    decActiveForegroundAppLocked(smap, r);
                }
                r.isForeground = false;
                if (r.app != null) {
                    this.mAm.updateLruProcessLocked(r.app, false, null);
                    updateServiceForegroundLocked(r.app, true);
                }
            }
            if ((flags & 1) != 0) {
                cancelForegroundNotificationLocked(r);
                r.foregroundId = 0;
                r.foregroundNoti = null;
            } else if (r.appInfo.targetSdkVersion >= 21) {
                r.stripForegroundServiceFlagFromNotification();
                if ((flags & 2) != 0) {
                    r.foregroundId = 0;
                    r.foregroundNoti = null;
                }
            }
        } else if (notification == null) {
            throw new IllegalArgumentException("null notification");
        } else {
            if (r.appInfo.isInstantApp()) {
                switch (this.mAm.mAppOpsService.checkOperation(68, r.appInfo.uid, r.appInfo.packageName)) {
                    case 0:
                        break;
                    case 1:
                        Slog.w(TAG, "Instant app " + r.appInfo.packageName + " does not have permission to create foreground services" + ", ignoring.");
                        return;
                    case 2:
                        throw new SecurityException("Instant app " + r.appInfo.packageName + " does not have permission to create foreground services");
                    default:
                        try {
                            if (AppGlobals.getPackageManager().checkPermission("android.permission.INSTANT_APP_FOREGROUND_SERVICE", r.appInfo.packageName, UserHandle.getUserId(r.appInfo.uid)) != 0) {
                                throw new SecurityException("Instant app " + r.appInfo.packageName + " does not have permission to create foreground" + "services");
                            }
                        } catch (RemoteException e) {
                            throw new SecurityException("Failed to check instant app permission.", e);
                        }
                        break;
                }
            }
            if (r.fgRequired) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE || ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                    Slog.i(TAG, "Service called startForeground() as required: " + r);
                }
                r.fgRequired = false;
                r.fgWaiting = false;
                this.mAm.mHandler.removeMessages(66, r);
            }
            if (r.foregroundId != id) {
                cancelForegroundNotificationLocked(r);
                r.foregroundId = id;
            }
            notification.flags |= 64;
            r.foregroundNoti = notification;
            if (!r.isForeground) {
                smap = getServiceMapLocked(r.userId);
                if (smap != null) {
                    ActiveForegroundApp active = (ActiveForegroundApp) smap.mActiveForegroundApps.get(r.packageName);
                    if (active == null) {
                        active = new ActiveForegroundApp();
                        active.mPackageName = r.packageName;
                        active.mUid = r.appInfo.uid;
                        active.mShownWhileScreenOn = this.mScreenOn;
                        if (!(r.app == null || r.app.uidRecord == null)) {
                            if (r.app.uidRecord.curProcState <= 2) {
                                z = true;
                            }
                            active.mShownWhileTop = z;
                            active.mAppOnTop = z;
                        }
                        long elapsedRealtime = SystemClock.elapsedRealtime();
                        active.mStartVisibleTime = elapsedRealtime;
                        active.mStartTime = elapsedRealtime;
                        smap.mActiveForegroundApps.put(r.packageName, active);
                        requestUpdateActiveForegroundAppsLocked(smap, 0);
                    }
                    active.mNumActive++;
                }
                r.isForeground = true;
            }
            r.postNotification();
            if (r.app != null) {
                updateServiceForegroundLocked(r.app, true);
            }
            getServiceMapLocked(r.userId).ensureNotStartingBackgroundLocked(r);
            this.mAm.notifyPackageUse(r.serviceInfo.packageName, 2);
        }
    }

    private void cancelForegroundNotificationLocked(ServiceRecord r) {
        if (!(r == null || r.foregroundId == 0)) {
            ServiceMap sm = getServiceMapLocked(r.userId);
            if (sm != null) {
                int i = sm.mServicesByName.size() - 1;
                while (i >= 0) {
                    ServiceRecord other = (ServiceRecord) sm.mServicesByName.valueAt(i);
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
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            ServiceRecord sr = (ServiceRecord) proc.services.valueAt(i);
            if (sr.isForeground || sr.fgRequired) {
                anyForeground = true;
                break;
            }
        }
        this.mAm.updateProcessForegroundLocked(proc, anyForeground, oomAdj);
    }

    private void updateWhitelistManagerLocked(ProcessRecord proc) {
        proc.whitelistManager = false;
        for (int i = proc.services.size() - 1; i >= 0; i--) {
            if (((ServiceRecord) proc.services.valueAt(i)).whitelistManager) {
                proc.whitelistManager = true;
                return;
            }
        }
    }

    public void updateServiceConnectionActivitiesLocked(ProcessRecord clientProc) {
        if (clientProc != null && clientProc.connections != null) {
            ArraySet updatedProcesses = null;
            for (int i = 0; i < clientProc.connections.size(); i++) {
                ProcessRecord proc = ((ConnectionRecord) clientProc.connections.valueAt(i)).binding.service.app;
                if (!(proc == null || proc == clientProc)) {
                    if (updatedProcesses == null) {
                        updatedProcesses = new ArraySet();
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
        for (int i = proc.services.size() - 1; i >= 0 && (anyClientActivities ^ 1) != 0; i--) {
            ServiceRecord sr = (ServiceRecord) proc.services.valueAt(i);
            for (int conni = sr.connections.size() - 1; conni >= 0 && (anyClientActivities ^ 1) != 0; conni--) {
                ArrayList<ConnectionRecord> clist = (ArrayList) sr.connections.valueAt(conni);
                for (int cri = clist.size() - 1; cri >= 0; cri--) {
                    ConnectionRecord cr = (ConnectionRecord) clist.get(cri);
                    if (cr.binding.client != null && cr.binding.client != proc && cr.binding.client.activities.size() > 0) {
                        anyClientActivities = true;
                        break;
                    }
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

    int bindServiceLocked(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws TransactionTooLargeException {
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "bindService: " + service + " type=" + resolvedType + " conn=" + connection.asBinder() + " flags=0x" + Integer.toHexString(flags));
        }
        ProcessRecord callerApp = this.mAm.getRecordForAppLocked(caller);
        if (callerApp == null) {
            throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when binding service " + service);
        }
        ActivityRecord activity = null;
        if (token != null) {
            activity = ActivityRecord.isInStackLocked(token);
            if (activity == null) {
                Slog.w(TAG, "Binding with unknown activity: " + token);
                return 0;
            }
        }
        int clientLabel = 0;
        PendingIntent clientIntent = null;
        boolean isCallerSystem = callerApp.info.uid == 1000;
        if (isCallerSystem) {
            service.setDefusable(true);
            clientIntent = (PendingIntent) service.getParcelableExtra("android.intent.extra.client_intent");
            if (clientIntent != null) {
                clientLabel = service.getIntExtra("android.intent.extra.client_label", 0);
                if (clientLabel != 0) {
                    service = service.cloneFilter();
                }
            }
        }
        if ((134217728 & flags) != 0) {
            this.mAm.enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "BIND_TREAT_LIKE_ACTIVITY");
        }
        if ((16777216 & flags) == 0 || (isCallerSystem ^ 1) == 0) {
            boolean callerFg = callerApp.setSchedGroup != 0;
            boolean isBindExternal = (Integer.MIN_VALUE & flags) != 0;
            this.mAm.setServiceFlagLocked(2);
            ServiceLookupResult res = retrieveServiceLocked(service, resolvedType, callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), userId, true, callerFg, isBindExternal);
            this.mAm.setServiceFlagLocked(0);
            if (res == null) {
                return 0;
            }
            if (res.record == null) {
                return -1;
            }
            final ServiceRecord s = res.record;
            PGManagerInternal pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
            if (!(pgm == null || callingPackage == null)) {
                if ((callingPackage.equals(s.name.getPackageName()) ^ 1) != 0) {
                    if (pgm.isServiceProxy(s.name, null)) {
                        Slog.i(TAG, "bind service is proxy: " + s.name);
                        return 0;
                    }
                }
            }
            boolean permissionsReviewRequired = false;
            if (this.mAm.mPermissionReviewRequired && this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(s.packageName, s.userId)) {
                permissionsReviewRequired = true;
                if (callerFg) {
                    ServiceRecord serviceRecord = s;
                    final Intent serviceIntent = service;
                    final boolean z = callerFg;
                    final IServiceConnection iServiceConnection = connection;
                    RemoteCallback remoteCallback = new RemoteCallback(new OnResultListener() {
                        public void onResult(Bundle result) {
                            synchronized (ActiveServices.this.mAm) {
                                long identity;
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    identity = Binder.clearCallingIdentity();
                                    if (ActiveServices.this.mPendingServices.contains(s)) {
                                        if (ActiveServices.this.mAm.getPackageManagerInternalLocked().isPermissionsReviewRequired(s.packageName, s.userId)) {
                                            ActiveServices.this.unbindServiceLocked(iServiceConnection);
                                        } else {
                                            try {
                                                ActiveServices.this.bringUpServiceLocked(s, serviceIntent.getFlags(), z, false, false);
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
                    });
                    Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                    intent.addFlags(276824064);
                    intent.putExtra("android.intent.extra.PACKAGE_NAME", s.packageName);
                    intent.putExtra("android.intent.extra.REMOTE_CALLBACK", remoteCallback);
                    if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                        Slog.i(TAG, "u" + s.userId + " Launching permission review for package " + s.packageName);
                    }
                    final Intent intent2 = intent;
                    final int i = userId;
                    this.mAm.mHandler.post(new Runnable() {
                        public void run() {
                            ActiveServices.this.mAm.mContext.startActivityAsUser(intent2, new UserHandle(i));
                        }
                    });
                } else {
                    Slog.w(TAG, "u" + s.userId + " Binding to a service in package" + s.packageName + " requires a permissions review");
                    return 0;
                }
            }
            long origId = Binder.clearCallingIdentity();
            if (unscheduleServiceRestartLocked(s, callerApp.info.uid, false) && ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "BIND SERVICE WHILE RESTART PENDING: " + s);
            }
            if ((flags & 1) != 0) {
                s.lastActivity = SystemClock.uptimeMillis();
                if (!s.hasAutoCreateConnections()) {
                    ServiceState stracker = s.getTracker();
                    if (stracker != null) {
                        stracker.setBound(true, this.mAm.mProcessStats.getMemFactorLocked(), s.lastActivity);
                    }
                }
            }
            this.mAm.startAssociationLocked(callerApp.uid, callerApp.processName, callerApp.curProcState, s.appInfo.uid, s.name, s.processName);
            this.mAm.grantEphemeralAccessLocked(callerApp.userId, service, s.appInfo.uid, UserHandle.getAppId(callerApp.uid));
            this.mAm.reportServiceRelationIAware(1, s, callerApp);
            AppBindRecord b = s.retrieveAppBindingLocked(service, callerApp);
            ConnectionRecord c = new ConnectionRecord(b, activity, connection, flags, clientLabel, clientIntent);
            IBinder binder = connection.asBinder();
            ArrayList<ConnectionRecord> clist = (ArrayList) s.connections.get(binder);
            if (clist == null) {
                clist = new ArrayList();
                s.connections.put(binder, clist);
            }
            clist.add(c);
            b.connections.add(c);
            if (activity != null) {
                if (activity.connections == null) {
                    activity.connections = new HashSet();
                }
                activity.connections.add(c);
            }
            b.client.connections.add(c);
            this.mAm.smartTrimAddProcessRelation_HwSysM(b, c.binding);
            if ((c.flags & 8) != 0) {
                b.client.hasAboveClient = true;
            }
            if ((c.flags & 16777216) != 0) {
                s.whitelistManager = true;
            }
            if (s.app != null) {
                updateServiceClientActivitiesLocked(s.app, c, true);
            }
            clist = (ArrayList) this.mServiceConnections.get(binder);
            if (clist == null) {
                clist = new ArrayList();
                this.mServiceConnections.put(binder, clist);
            }
            clist.add(c);
            if ((flags & 1) != 0) {
                s.lastActivity = SystemClock.uptimeMillis();
                if (bringUpServiceLocked(s, service.getFlags(), callerFg, false, permissionsReviewRequired) != null) {
                    Binder.restoreCallingIdentity(origId);
                    return 0;
                }
            }
            try {
                if (s.app != null) {
                    boolean z2;
                    if ((134217728 & flags) != 0) {
                        s.app.treatLikeActivity = true;
                    }
                    if (s.whitelistManager) {
                        s.app.whitelistManager = true;
                    }
                    ActivityManagerService activityManagerService = this.mAm;
                    ProcessRecord processRecord = s.app;
                    if (s.app.hasClientActivities) {
                        z2 = true;
                    } else {
                        z2 = s.app.treatLikeActivity;
                    }
                    activityManagerService.updateLruProcessLocked(processRecord, z2, b.client);
                    this.mAm.updateOomAdjLocked(s.app, true);
                }
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Bind " + s + " with " + b + ": received=" + b.intent.received + " apps=" + b.intent.apps.size() + " doRebind=" + b.intent.doRebind);
                }
                if (s.app != null && b.intent.received) {
                    c.conn.connected(s.name, b.intent.binder, false);
                    if (b.intent.apps.size() == 1 && b.intent.doRebind) {
                        requestServiceBindingLocked(s, b.intent, callerFg, true);
                    }
                } else if (!b.intent.requested) {
                    requestServiceBindingLocked(s, b.intent, callerFg, false);
                }
            } catch (Throwable e) {
                Slog.w(TAG, "Failure sending service " + s.shortName + " to connection " + c.conn.asBinder() + " (in " + c.binding.client.processName + ")", e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
            getServiceMapLocked(s.userId).ensureNotStartingBackgroundLocked(s);
            Binder.restoreCallingIdentity(origId);
            ProcessRecord app = s.app;
            if (app == null && s.processName != null) {
                app = this.mAm.getProcessRecordLocked(s.processName, s.appInfo.uid, true);
            }
            if (!(app == null || app.uid < 10000 || callerApp.pid == app.pid || callerApp.info == null || app.info == null || callerApp.info.packageName == null || (callerApp.info.packageName.equals(app.info.packageName) ^ 1) == 0)) {
                LogPower.push(148, "bindservice", s.packageName, Integer.toString(app.pid), new String[]{callingPackage});
                LogPower.push(166, s.processName, Integer.toString(callerApp.pid), Integer.toString(app.pid), new String[]{"service"});
            }
            return 1;
        }
        throw new SecurityException("Non-system caller " + caller + " (pid=" + Binder.getCallingPid() + ") set BIND_ALLOW_WHITELIST_MANAGEMENT when binding service " + service);
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
                if (!(b == null || (b.received ^ 1) == 0)) {
                    b.binder = service;
                    b.requested = true;
                    b.received = true;
                    for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
                        ArrayList<ConnectionRecord> clist = (ArrayList) r.connections.valueAt(conni);
                        for (int i = 0; i < clist.size(); i++) {
                            c = (ConnectionRecord) clist.get(i);
                            if (filter.equals(c.binding.intent.intent)) {
                                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                    Slog.v(TAG_SERVICE, "Publishing to: " + c);
                                }
                                c.conn.connected(r.name, service, false);
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
                serviceDoneExecutingLocked(r, this.mDestroyingServices.contains(r), false);
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
            return false;
        }
        long origId = Binder.clearCallingIdentity();
        while (clist.size() > 0) {
            try {
                ConnectionRecord r = (ConnectionRecord) clist.get(0);
                removeConnectionLocked(r, null, null);
                if (clist.size() > 0 && clist.get(0) == r) {
                    Slog.wtf(TAG, "Connection " + r + " not removed for binder " + binder);
                    clist.remove(0);
                }
                if (r.binding.service.app != null) {
                    if (r.binding.service.app.whitelistManager) {
                        updateWhitelistManagerLocked(r.binding.service.app);
                    }
                    if ((r.flags & 134217728) != 0) {
                        boolean z;
                        r.binding.service.app.treatLikeActivity = true;
                        ActivityManagerService activityManagerService = this.mAm;
                        ProcessRecord processRecord = r.binding.service.app;
                        if (r.binding.service.app.hasClientActivities) {
                            z = true;
                        } else {
                            z = r.binding.service.app.treatLikeActivity;
                        }
                        activityManagerService.updateLruProcessLocked(processRecord, z, null);
                    }
                    this.mAm.updateOomAdjLocked(r.binding.service.app, false);
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
        this.mAm.updateOomAdjLocked();
        return true;
    }

    void unbindFinishedLocked(ServiceRecord r, Intent intent, boolean doRebind) {
        long origId = Binder.clearCallingIdentity();
        if (r != null) {
            try {
                IntentBindRecord b = (IntentBindRecord) r.bindings.get(new FilterComparison(intent));
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "unbindFinished in " + r + " at " + b + ": apps=" + (b != null ? b.apps.size() : 0));
                }
                boolean inDestroying = this.mDestroyingServices.contains(r);
                if (b != null) {
                    if (b.apps.size() <= 0 || (inDestroying ^ 1) == 0) {
                        b.doRebind = true;
                    } else {
                        boolean inFg = false;
                        for (int i = b.apps.size() - 1; i >= 0; i--) {
                            ProcessRecord client = ((AppBindRecord) b.apps.valueAt(i)).client;
                            if (client != null && client.setSchedGroup != 0) {
                                inFg = true;
                                break;
                            }
                        }
                        try {
                            requestServiceBindingLocked(r, b, inFg, true);
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
                serviceDoneExecutingLocked(r, inDestroying, false);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    private final ServiceRecord findServiceLocked(ComponentName name, IBinder token, int userId) {
        IBinder r = getServiceByNameLocked(name, userId);
        return r == token ? r : null;
    }

    private ServiceLookupResult retrieveServiceLocked(Intent service, String resolvedType, String callingPackage, int callingPid, int callingUid, int userId, boolean createIfNeeded, boolean callingFromFg, boolean isBindExternal) {
        ProcessRecord callerApp;
        ServiceRecord r;
        ServiceRecord r2;
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.v(TAG_SERVICE, "retrieveServiceLocked: " + service + " type=" + resolvedType + " callingUid=" + callingUid);
        }
        userId = this.mAm.mUserController.handleIncomingUser(callingPid, callingUid, userId, false, 1, "service", null);
        synchronized (this.mAm.mPidsSelfLocked) {
            callerApp = (ProcessRecord) this.mAm.mPidsSelfLocked.get(callingPid);
        }
        ServiceMap smap = getServiceMapLocked(userId);
        ComponentName comp = service.getComponent();
        if (comp != null) {
            r = (ServiceRecord) smap.mServicesByName.get(comp);
            if (ActivityManagerDebugConfig.DEBUG_SERVICE && r != null) {
                Slog.v(TAG_SERVICE, "Retrieved by component: " + r);
            }
        } else {
            r = null;
        }
        if (r == null && (isBindExternal ^ 1) != 0) {
            r = (ServiceRecord) smap.mServicesByIntent.get(new FilterComparison(service));
            if (ActivityManagerDebugConfig.DEBUG_SERVICE && r != null) {
                Slog.v(TAG_SERVICE, "Retrieved by intent: " + r);
            }
        }
        if (r == null || (r.serviceInfo.flags & 4) == 0) {
            r2 = r;
        } else {
            if ((callingPackage.equals(r.packageName) ^ 1) != 0) {
                r = null;
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Whoops, can't use existing external service");
                    r2 = null;
                }
            }
            r2 = r;
        }
        if (r2 == null) {
            try {
                ResolveInfo rInfo = this.mAm.getPackageManagerInternalLocked().resolveService(service, resolvedType, 268436480, userId, callingUid);
                ServiceInfo sInfo = rInfo != null ? rInfo.serviceInfo : null;
                if (sInfo != null && this.mAm.shouldPreventStartService(sInfo, callingPid, callingUid, callerApp, false, service)) {
                    return null;
                }
                if (sInfo == null) {
                    Slog.w(TAG_SERVICE, "Unable to start service " + service + " U=" + userId + ": not found");
                    return null;
                }
                ServiceInfo sInfo2;
                ComponentName name = new ComponentName(sInfo.applicationInfo.packageName, sInfo.name);
                if ((sInfo.flags & 4) != 0) {
                    if (!isBindExternal) {
                        throw new SecurityException("BIND_EXTERNAL_SERVICE required for " + name);
                    } else if (!sInfo.exported) {
                        throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not exported");
                    } else if ((sInfo.flags & 2) == 0) {
                        throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an isolatedProcess");
                    } else {
                        ApplicationInfo aInfo = AppGlobals.getPackageManager().getApplicationInfo(callingPackage, 1024, userId);
                        if (aInfo == null) {
                            throw new SecurityException("BIND_EXTERNAL_SERVICE failed, could not resolve client package " + callingPackage);
                        }
                        ServiceInfo serviceInfo = new ServiceInfo(sInfo);
                        serviceInfo.applicationInfo = new ApplicationInfo(serviceInfo.applicationInfo);
                        serviceInfo.applicationInfo.packageName = aInfo.packageName;
                        serviceInfo.applicationInfo.uid = aInfo.uid;
                        ComponentName componentName = new ComponentName(aInfo.packageName, name.getClassName());
                        service.setComponent(componentName);
                        name = componentName;
                    }
                } else if (isBindExternal) {
                    throw new SecurityException("BIND_EXTERNAL_SERVICE failed, " + name + " is not an externalService");
                } else {
                    sInfo2 = sInfo;
                }
                if (userId > 0) {
                    if (this.mAm.isSingleton(sInfo2.processName, sInfo2.applicationInfo, sInfo2.name, sInfo2.flags)) {
                        if (this.mAm.isValidSingletonCall(callingUid, sInfo2.applicationInfo.uid)) {
                            userId = 0;
                            smap = getServiceMapLocked(0);
                        }
                    }
                    sInfo = new ServiceInfo(sInfo2);
                    sInfo.applicationInfo = this.mAm.getAppInfoForUser(sInfo.applicationInfo, userId);
                } else {
                    sInfo = sInfo2;
                }
                r2 = (ServiceRecord) smap.mServicesByName.get(name);
                if (ActivityManagerDebugConfig.DEBUG_SERVICE && r2 != null) {
                    Slog.v(TAG_SERVICE, "Retrieved via pm by intent: " + r2);
                }
                if (r2 == null) {
                    if (createIfNeeded) {
                        Serv ss;
                        FilterComparison filter = new FilterComparison(service.cloneFilter());
                        ServiceRestarter res = new ServiceRestarter(this, null);
                        BatteryStatsImpl stats = this.mAm.mBatteryStatsService.getActiveStatistics();
                        synchronized (stats) {
                            ss = stats.getServiceStatsLocked(sInfo.applicationInfo.uid, sInfo.packageName, sInfo.name);
                        }
                        r = new ServiceRecord(this.mAm, ss, name, filter, sInfo, callingFromFg, res);
                        try {
                            res.setService(r);
                            smap.mServicesByName.put(name, r);
                            smap.mServicesByIntent.put(filter, r);
                            for (int i = this.mPendingServices.size() - 1; i >= 0; i--) {
                                ServiceRecord pr = (ServiceRecord) this.mPendingServices.get(i);
                                if (pr.serviceInfo.applicationInfo.uid == sInfo.applicationInfo.uid && pr.name.equals(name)) {
                                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                        Slog.v(TAG_SERVICE, "Remove pending: " + pr);
                                    }
                                    this.mPendingServices.remove(i);
                                }
                            }
                            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                                Slog.v(TAG_SERVICE, "Retrieve created new service: " + r);
                            }
                        } catch (RemoteException e) {
                        }
                    }
                    r = r2;
                } else {
                    r = r2;
                }
            } catch (RemoteException e2) {
                r = r2;
            }
        } else if (!createIfNeeded || r2.serviceInfo == null) {
            r = r2;
        } else {
            if (this.mAm.shouldPreventStartService(r2.serviceInfo, callingPid, callingUid, callerApp, true, service)) {
                return null;
            }
            r = r2;
        }
        if (r != null) {
            if (this.mAm.checkComponentPermission(r.permission, callingPid, callingUid, r.appInfo.uid, r.exported) == 0) {
                if (!(r.permission == null || callingPackage == null)) {
                    int opCode = AppOpsManager.permissionToOpCode(r.permission);
                    if (!(opCode == -1 || this.mAm.mAppOpsService.noteOperation(opCode, callingUid, callingPackage) == 0)) {
                        Slog.w(TAG, "Appop Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " requires appop " + AppOpsManager.opToName(opCode));
                        return null;
                    }
                }
                if (!this.mAm.mIntentFirewall.checkService(r.name, service, callingUid, callingPid, resolvedType, r.appInfo)) {
                    Flog.w(102, "prevent by firewall Unable to start service " + service + " U=" + userId + ": force null");
                    return null;
                } else if (!this.mAm.shouldPreventStartService(r.serviceInfo, callingUid, callingPid, callingPackage, userId)) {
                    return new ServiceLookupResult(r, null);
                } else {
                    Flog.w(102, "prevent by trustspace Unable to start service " + service + " U=" + userId + ": force null");
                    return null;
                }
            } else if (r.exported) {
                Slog.w(TAG, "Permission Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " requires " + r.permission);
                return new ServiceLookupResult(null, r.permission);
            } else {
                Slog.w(TAG, "Permission Denial: Accessing service " + r.name + " from pid=" + callingPid + ", uid=" + callingUid + " that is not exported from uid " + r.appInfo.uid);
                return new ServiceLookupResult(null, "not exported from uid " + r.appInfo.uid);
            }
        }
        Flog.i(102, "retrieve service " + service + " U=" + userId + ": ret null");
        return null;
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
                stracker.setExecuting(true, this.mAm.mProcessStats.getMemFactorLocked(), now);
            }
            if (r.app != null) {
                r.app.executingServices.add(r);
                ProcessRecord processRecord = r.app;
                processRecord.execServicesFg |= fg;
                if (r.app.executingServices.size() == 1) {
                    scheduleServiceTimeoutLocked(r.app);
                }
            }
        } else if (!(r.app == null || !fg || (r.app.execServicesFg ^ 1) == 0)) {
            r.app.execServicesFg = true;
            scheduleServiceTimeoutLocked(r.app);
        }
        r.executeFg |= fg;
        r.executeNesting++;
        r.executingStart = now;
    }

    private final boolean requestServiceBindingLocked(ServiceRecord r, IntentBindRecord i, boolean execInFg, boolean rebind) throws TransactionTooLargeException {
        boolean inDestroying;
        if (r.app == null || r.app.thread == null) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            Slog.d(TAG_SERVICE, "requestBind " + i + ": requested=" + i.requested + " rebind=" + rebind);
        }
        if ((!i.requested || rebind) && i.apps.size() > 0) {
            try {
                bumpServiceExecutingLocked(r, execInFg, "bind");
                r.app.forceProcessStateUpTo(11);
                r.app.thread.scheduleBindService(r, i.intent.getIntent(), rebind, r.app.repProcState);
                if (!rebind) {
                    i.requested = true;
                }
                i.hasBound = true;
                i.doRebind = false;
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
                return false;
            }
        }
        return true;
    }

    private final boolean scheduleServiceRestartLocked(ServiceRecord r, boolean allowCancel) {
        boolean canceled = false;
        if (this.mAm.isShuttingDownLocked()) {
            Slog.w(TAG, "Not scheduling restart of crashed service " + r.shortName + " - system is shutting down");
            return false;
        }
        ServiceMap smap = getServiceMapLocked(r.userId);
        if (smap.mServicesByName.get(r.name) != r) {
            Slog.wtf(TAG, "Attempting to schedule restart of " + r + " when found in map: " + ((ServiceRecord) smap.mServicesByName.get(r.name)));
            return false;
        }
        int i;
        ServiceRecord r2;
        long now = SystemClock.uptimeMillis();
        if ((r.serviceInfo.applicationInfo.flags & 8) == 0) {
            long minDuration = this.mAm.mConstants.SERVICE_RESTART_DURATION;
            long resetTime = this.mAm.mConstants.SERVICE_RESET_RUN_DURATION;
            int N = r.deliveredStarts.size();
            if (N > 0) {
                for (i = N - 1; i >= 0; i--) {
                    StartItem si = (StartItem) r.deliveredStarts.get(i);
                    si.removeUriPermissionsLocked();
                    if (si.intent != null) {
                        if (!allowCancel || (si.deliveryCount < 3 && si.doneExecutingCount < 6)) {
                            r.pendingStarts.add(0, si);
                            long dur = (SystemClock.uptimeMillis() - si.deliveredTime) * 2;
                            if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
                                Slog.w(TAG, "Can add more delay !!! si.deliveredTime " + si.deliveredTime + " dur " + dur + " si.deliveryCount " + si.deliveryCount + " si.doneExecutingCount " + si.doneExecutingCount + " allowCancel " + allowCancel);
                            }
                            if (minDuration < dur) {
                                minDuration = dur;
                            }
                            if (resetTime < dur) {
                                resetTime = dur;
                            }
                        } else {
                            Slog.w(TAG, "Canceling start item " + si.intent + " in service " + r.name);
                            canceled = true;
                        }
                    }
                }
                r.deliveredStarts.clear();
            }
            r.totalRestartCount++;
            if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
                Slog.w(TAG, "r.name " + r.name + " N " + N + " minDuration " + minDuration + " resetTime " + resetTime + " now " + now + " r.restartDelay " + r.restartDelay + " r.restartTime+resetTime " + (r.restartTime + resetTime) + " allowCancel " + allowCancel);
            }
            if (r.restartDelay == 0) {
                r.restartCount++;
                r.restartDelay = minDuration;
            } else if (now > r.restartTime + resetTime) {
                r.restartCount = 1;
                r.restartDelay = minDuration;
            } else {
                r.restartDelay *= (long) this.mAm.mConstants.SERVICE_RESTART_DURATION_FACTOR;
                if (r.restartDelay < minDuration) {
                    r.restartDelay = minDuration;
                }
            }
            r.nextRestartTime = r.restartDelay + now;
            if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
                Slog.w(TAG, "r.name " + r.name + " N " + N + " minDuration " + minDuration + " resetTime " + resetTime + " now " + now + " r.restartDelay " + r.restartDelay + " r.restartTime+resetTime " + (r.restartTime + resetTime) + " r.nextRestartTime " + r.nextRestartTime + " allowCancel " + allowCancel);
            }
            boolean repeat;
            do {
                repeat = false;
                long restartTimeBetween = this.mAm.mConstants.SERVICE_MIN_RESTART_TIME_BETWEEN;
                for (i = this.mRestartingServices.size() - 1; i >= 0; i--) {
                    r2 = (ServiceRecord) this.mRestartingServices.get(i);
                    if (r2 != r && r.nextRestartTime >= r2.nextRestartTime - restartTimeBetween && r.nextRestartTime < r2.nextRestartTime + restartTimeBetween) {
                        r.nextRestartTime = r2.nextRestartTime + restartTimeBetween;
                        r.restartDelay = r.nextRestartTime - now;
                        repeat = true;
                        continue;
                        break;
                    }
                }
            } while (repeat);
        } else {
            r.totalRestartCount++;
            r.restartCount = 0;
            r.restartDelay = 0;
            r.nextRestartTime = now;
        }
        if (!this.mRestartingServices.contains(r)) {
            r.createdFromFg = false;
            this.mRestartingServices.add(r);
            r.makeRestarting(this.mAm.mProcessStats.getMemFactorLocked(), now);
        }
        cancelForegroundNotificationLocked(r);
        this.mAm.mHandler.removeCallbacks(r.restarter);
        this.mAm.mHandler.postAtTime(r.restarter, r.nextRestartTime);
        r.nextRestartTime = SystemClock.uptimeMillis() + r.restartDelay;
        Slog.w(TAG, "Scheduling restart of crashed service " + r.shortName + " in " + r.restartDelay + "ms");
        if (SERVICE_RESCHEDULE && DEBUG_DELAYED_SERVICE) {
            for (i = this.mRestartingServices.size() - 1; i >= 0; i--) {
                r2 = (ServiceRecord) this.mRestartingServices.get(i);
                Slog.w(TAG, "Restarting list - i " + i + " r2.nextRestartTime " + r2.nextRestartTime + " r2.name " + r2.name);
            }
        }
        EventLog.writeEvent(EventLogTags.AM_SCHEDULE_SERVICE_RESTART, new Object[]{Integer.valueOf(r.userId), r.shortName, Long.valueOf(r.restartDelay)});
        return canceled;
    }

    final void performServiceRestartLocked(ServiceRecord r) {
        if (!this.mRestartingServices.contains(r)) {
            Flog.i(102, "no need to performServiceRestart for r:" + r);
        } else if (isServiceNeededLocked(r, false, false)) {
            try {
                if (SERVICE_RESCHEDULE) {
                    boolean shouldDelay = false;
                    ActivityRecord top_rc = null;
                    ActivityStack stack = this.mAm.getFocusedStack();
                    if (stack != null) {
                        top_rc = stack.topRunningActivityLocked();
                    }
                    if (!(top_rc == null || top_rc.nowVisible || (r.shortName.contains(top_rc.packageName) ^ 1) == 0)) {
                        shouldDelay = true;
                    }
                    if (shouldDelay) {
                        if (DEBUG_DELAYED_SERVICE) {
                            Slog.v(TAG, "Reschedule service restart due to app launch r.shortName " + r.shortName + " r.app = " + r.app);
                        }
                        r.resetRestartCounter();
                        scheduleServiceRestartLocked(r, true);
                    } else {
                        bringUpServiceLocked(r, r.intent.getIntent().getFlags(), r.createdFromFg, true, false);
                    }
                } else {
                    bringUpServiceLocked(r, r.intent.getIntent().getFlags(), r.createdFromFg, true, false);
                }
            } catch (TransactionTooLargeException e) {
                Flog.w(102, "performServiceRestart TransactionTooLarge e:", e);
            }
        } else {
            Slog.wtf(TAG, "Restarting service that is not needed: " + r);
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
            for (int i = this.mRestartingServices.size() - 1; i >= 0; i--) {
                if (((ServiceRecord) this.mRestartingServices.get(i)).restartTracker == r.restartTracker) {
                    stillTracking = true;
                    break;
                }
            }
            if (!stillTracking) {
                r.restartTracker.setRestarting(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                r.restartTracker = null;
            }
        }
    }

    private String bringUpServiceLocked(ServiceRecord r, int intentFlags, boolean execInFg, boolean whileRestarting, boolean permissionsReviewRequired) throws TransactionTooLargeException {
        if (r.app != null && r.app.thread != null) {
            sendServiceArgsLocked(r, execInFg, false);
            return null;
        } else if (whileRestarting || !this.mRestartingServices.contains(r)) {
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "Bringing up " + r + " " + r.intent + " fg=" + r.fgRequired);
            }
            if (this.mRestartingServices.remove(r)) {
                if (HW_PARENT_CONTROL.equals(r.packageName)) {
                    Slog.i(TAG, "parentcontrol reset restart counter");
                    r.resetRestartCounter();
                }
                clearRestartingIfNeededLocked(r);
            }
            if (r.delayed) {
                if (DEBUG_DELAYED_STARTS) {
                    Slog.v(TAG_SERVICE, "REM FR DELAY LIST (bring up): " + r);
                }
                getServiceMapLocked(r.userId).mDelayedStartList.remove(r);
                r.delayed = false;
            }
            String msg;
            if (this.mAm.mUserController.hasStartedUserState(r.userId)) {
                ProcessRecord app;
                try {
                    AppGlobals.getPackageManager().setPackageStoppedState(r.packageName, false, r.userId);
                } catch (RemoteException e) {
                } catch (IllegalArgumentException e2) {
                    Slog.w(TAG, "Failed trying to unstop package " + r.packageName + ": " + e2);
                }
                boolean isolated = (r.serviceInfo.flags & 2) != 0;
                String procName = r.processName;
                String hostingType = "service";
                if (isolated) {
                    app = r.isolatedProc;
                    if (WebViewZygote.isMultiprocessEnabled() && r.serviceInfo.packageName.equals(WebViewZygote.getPackageName())) {
                        hostingType = "webview_service";
                    }
                } else {
                    app = this.mAm.getProcessRecordLocked(procName, r.appInfo.uid, false);
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
                        } catch (Throwable e4) {
                            Slog.w(TAG, "Exception when starting service " + r.shortName, e4);
                        }
                    }
                }
                if (app == null && (permissionsReviewRequired ^ 1) != 0) {
                    if (whileRestarting) {
                        this.mAm.shouldPreventRestartService(r.serviceInfo, true);
                    }
                    String[] entryPointArgs = null;
                    if (HwPCUtils.isPcCastModeInServer() && this.mAm.mHwAMSEx != null) {
                        entryPointArgs = this.mAm.mHwAMSEx.initPCEntryArgsForService(r, this.mAm);
                        if (entryPointArgs.length == 0) {
                            entryPointArgs = null;
                        }
                    }
                    if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && TextUtils.equals(r.appInfo.packageName, "com.android.incallui")) {
                        entryPointArgs = new String[]{String.valueOf(HwPCUtils.getPCDisplayID())};
                    }
                    app = this.mAm.startProcessLocked(procName, r.appInfo, true, intentFlags, hostingType, r.name, false, isolated, 0, false, null, null, entryPointArgs, null);
                    if (app == null) {
                        msg = "Unable to launch app " + r.appInfo.packageName + "/" + r.appInfo.uid + " for service " + r.intent.getIntent() + ": process is bad";
                        Slog.w(TAG, msg);
                        try {
                            bringDownServiceLocked(r);
                        } catch (Throwable e5) {
                            Slog.w(TAG, "Exception when bring down Service " + r.shortName, e5);
                        }
                        return msg;
                    } else if (isolated) {
                        r.isolatedProc = app;
                    }
                }
                if (!this.mPendingServices.contains(r)) {
                    this.mPendingServices.add(r);
                }
                if (r.delayedStop) {
                    r.delayedStop = false;
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
            Flog.i(102, "do nothing when waiting for a restart and Bringing up " + r + " " + r.intent);
            return null;
        }
    }

    private final void requestServiceBindingsLocked(ServiceRecord r, boolean execInFg) throws TransactionTooLargeException {
        int i = r.bindings.size() - 1;
        while (i >= 0 && requestServiceBindingLocked(r, (IntentBindRecord) r.bindings.valueAt(i), execInFg, false)) {
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
        this.mAm.updateLruProcessLocked(app, false, null);
        updateServiceForegroundLocked(r.app, false);
        this.mAm.updateOomAdjLocked();
        boolean inDestroying;
        try {
            int lastPeriod = r.shortName.lastIndexOf(46);
            EventLogTags.writeAmCreateService(r.userId, System.identityHashCode(r), lastPeriod >= 0 ? r.shortName.substring(lastPeriod) : r.shortName, r.app.uid, r.app.pid);
            synchronized (r.stats.getBatteryStats()) {
                r.stats.startLaunchedLocked();
            }
            this.mAm.notifyPackageUse(r.serviceInfo.packageName, 1);
            app.forceProcessStateUpTo(11);
            app.thread.scheduleCreateService(r, r.serviceInfo, this.mAm.compatibilityInfoForPackageLocked(r.serviceInfo.applicationInfo), app.repProcState);
            r.postNotification();
            if (!true) {
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
                    Flog.w(102, "Destroying no retry when creating service " + r);
                } else {
                    scheduleServiceRestartLocked(r, false);
                }
            }
            if (r.whitelistManager) {
                app.whitelistManager = true;
            }
            requestServiceBindingsLocked(r, execInFg);
            updateServiceClientActivitiesLocked(app, null, true);
            if (r.startRequested && r.callStart && r.pendingStarts.size() == 0) {
                r.pendingStarts.add(new StartItem(r, false, r.makeNextStartId(), null, null, 0));
            }
            sendServiceArgsLocked(r, execInFg, true);
            if (r.delayed) {
                if (DEBUG_DELAYED_STARTS) {
                    Slog.v(TAG_SERVICE, "REM FR DELAY LIST (new proc): " + r);
                }
                getServiceMapLocked(r.userId).mDelayedStartList.remove(r);
                r.delayed = false;
            }
            if (r.delayedStop) {
                r.delayedStop = false;
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
                if (!false) {
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
                        Flog.w(102, "Destroying no retry when creating service " + r);
                    } else {
                        scheduleServiceRestartLocked(r, false);
                    }
                }
            }
        }
    }

    private final void sendServiceArgsLocked(ServiceRecord r, boolean execInFg, boolean oomAdjusted) throws TransactionTooLargeException {
        int N = r.pendingStarts.size();
        if (N != 0) {
            ArrayList<ServiceStartArgs> args = new ArrayList();
            while (r.pendingStarts.size() > 0) {
                StartItem si = (StartItem) r.pendingStarts.remove(0);
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
                    this.mAm.grantEphemeralAccessLocked(r.userId, si.intent, r.appInfo.uid, UserHandle.getAppId(si.callingId));
                    bumpServiceExecutingLocked(r, execInFg, "start");
                    if (!oomAdjusted) {
                        oomAdjusted = true;
                        this.mAm.updateOomAdjLocked(r.app, true);
                    }
                    if (r.fgRequired && (r.fgWaiting ^ 1) != 0) {
                        if (r.isForeground) {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(TAG, "Service already foreground; no new timeout: " + r);
                            }
                            r.fgRequired = false;
                        } else {
                            if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                                Slog.i(TAG, "Launched service must call startForeground() within timeout: " + r);
                            }
                            scheduleServiceForegroundTransitionTimeoutLocked(r);
                        }
                    }
                    int flags = 0;
                    if (si.deliveryCount > 1) {
                        flags = 2;
                    }
                    if (si.doneExecutingCount > 0) {
                        flags |= 1;
                    }
                    args.add(new ServiceStartArgs(si.taskRemoved, si.id, flags, si.intent));
                }
            }
            ParceledListSlice<ServiceStartArgs> slice = new ParceledListSlice(args);
            slice.setInlineCountLimit(4);
            Exception caughtException = null;
            try {
                r.app.thread.scheduleServiceArgs(r, slice);
            } catch (Exception e) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    String str = TAG_SERVICE;
                    String str2 = str;
                    Slog.v(str2, "Transaction too large for " + args.size() + " args, first: " + ((ServiceStartArgs) args.get(0)).args);
                }
                Slog.w(TAG, "Failed delivering service starts", e);
                caughtException = e;
            } catch (Exception e2) {
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "Crashed while sending args: " + r);
                }
                Slog.w(TAG, "Failed delivering service starts", e2);
                caughtException = e2;
            } catch (Exception e3) {
                Slog.w(TAG, "Unexpected exception", e3);
                caughtException = e3;
            }
            if (caughtException != null) {
                boolean inDestroying = this.mDestroyingServices.contains(r);
                for (int i = 0; i < args.size(); i++) {
                    serviceDoneExecutingLocked(r, inDestroying, inDestroying);
                }
                if (caughtException instanceof TransactionTooLargeException) {
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
                Slog.w(TAG, "Exception when r is illegal!");
            }
        }
    }

    private final void bringDownServiceLocked(ServiceRecord r) {
        int i;
        for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> c = (ArrayList) r.connections.valueAt(conni);
            for (i = 0; i < c.size(); i++) {
                ConnectionRecord cr = (ConnectionRecord) c.get(i);
                cr.serviceDead = true;
                try {
                    cr.conn.connected(r.name, null, true);
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
                        bumpServiceExecutingLocked(r, false, "bring down unbind");
                        this.mAm.updateOomAdjLocked(r.app, true);
                        ibr.hasBound = false;
                        ibr.requested = false;
                        r.app.thread.scheduleUnbindService(r, ibr.intent.getIntent());
                    } catch (Exception e2) {
                        Slog.w(TAG, "Exception when unbinding service " + r.shortName, e2);
                        serviceProcessGoneLocked(r);
                    }
                }
            }
        }
        if (r.fgRequired) {
            Slog.w(TAG_SERVICE, "Bringing down service while still waiting for start foreground: " + r);
            r.fgRequired = false;
            r.fgWaiting = false;
            this.mAm.mHandler.removeMessages(66, r);
            if (r.app != null) {
                Message msg = this.mAm.mHandler.obtainMessage(69);
                msg.obj = r.app;
                this.mAm.mHandler.sendMessage(msg);
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
            RuntimeException here = new RuntimeException();
            here.fillInStackTrace();
            Slog.v(TAG_SERVICE, "Bringing down " + r + " " + r.intent, here);
        }
        r.destroyTime = SystemClock.uptimeMillis();
        EventLogTags.writeAmDestroyService(r.userId, System.identityHashCode(r), r.app != null ? r.app.pid : -1);
        ServiceMap smap = getServiceMapLocked(r.userId);
        ServiceRecord found = (ServiceRecord) smap.mServicesByName.remove(r.name);
        if (found == null || found == r) {
            smap.mServicesByIntent.remove(r.intent);
            r.totalRestartCount = 0;
            unscheduleServiceRestartLocked(r, 0, true);
            for (i = this.mPendingServices.size() - 1; i >= 0; i--) {
                if (this.mPendingServices.get(i) == r) {
                    this.mPendingServices.remove(i);
                    if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                        Slog.v(TAG_SERVICE, "Removed pending: " + r);
                    }
                }
            }
            cancelForegroundNotificationLocked(r);
            if (r.isForeground) {
                decActiveForegroundAppLocked(smap, r);
            }
            r.isForeground = false;
            r.foregroundId = 0;
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
                    updateServiceForegroundLocked(r.app, false);
                    try {
                        bumpServiceExecutingLocked(r, false, "destroy");
                        this.mDestroyingServices.add(r);
                        r.destroying = true;
                        this.mAm.updateOomAdjLocked(r.app, true);
                        r.app.thread.scheduleStopService(r);
                    } catch (Exception e22) {
                        Slog.w(TAG, "Exception when destroying service " + r.shortName, e22);
                        serviceProcessGoneLocked(r);
                    }
                } else {
                    Flog.i(102, "Removed service that has no process: " + r);
                }
            } else {
                Flog.i(102, "Removed service that is not running: " + r);
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
                r.tracker.setStarted(false, memFactor, now);
                r.tracker.setBound(false, memFactor, now);
                if (r.executeNesting == 0) {
                    r.tracker.clearCurrentOwner(r, false);
                    r.tracker = null;
                }
            }
            smap.ensureNotStartingBackgroundLocked(r);
            return;
        }
        smap.mServicesByName.put(r.name, found);
        throw new IllegalStateException("Bringing down " + r + " but actually running " + found);
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
            if (!(s.app == null || s.app.uid < 10000 || pr.pid == s.app.pid || s.app.info == null || pr.info == null || s.app.info.packageName == null || (s.app.info.packageName.equals(pr.info.packageName) ^ 1) == 0)) {
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
                updateServiceClientActivitiesLocked(s.app, c, true);
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
                    bumpServiceExecutingLocked(s, false, "unbind");
                    if (b.client != s.app && (c.flags & 32) == 0 && s.app.setProcState <= 12) {
                        this.mAm.updateLruProcessLocked(s.app, false, null);
                    }
                    this.mAm.updateOomAdjLocked(s.app, true);
                    b.intent.hasBound = false;
                    b.intent.doRebind = false;
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
                    s.tracker.setBound(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                }
                bringDownServiceIfNeededLocked(s, true, hasAutoCreate);
            }
        }
    }

    void serviceDoneExecutingLocked(ServiceRecord r, int type, int startId, int res) {
        boolean inDestroying = this.mDestroyingServices.contains(r);
        if (r != null) {
            if (type == 1) {
                r.callStart = true;
                switch (res) {
                    case 0:
                    case 1:
                        r.findDeliveredStart(startId, true);
                        r.stopIfKilled = false;
                        break;
                    case 2:
                        r.findDeliveredStart(startId, true);
                        if (r.getLastStartId() == startId) {
                            r.stopIfKilled = true;
                            break;
                        }
                        break;
                    case 3:
                        StartItem si = r.findDeliveredStart(startId, false);
                        if (si != null) {
                            si.deliveryCount = 0;
                            si.doneExecutingCount++;
                            r.stopIfKilled = true;
                            break;
                        }
                        break;
                    case 1000:
                        r.findDeliveredStart(startId, true);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown service start result: " + res);
                }
                if (res == 0) {
                    r.callStart = false;
                }
            } else if (type == 2) {
                if (inDestroying) {
                    if (r.executeNesting != 1) {
                        Flog.w(102, "Service done with onDestroy, but executeNesting=" + r.executeNesting + ": " + r);
                        Slog.w(TAG, "Service done with onDestroy, but executeNesting=" + r.executeNesting + ": " + r);
                        r.executeNesting = 1;
                    }
                } else if (r.app != null) {
                    Flog.w(102, "Service done with onDestroy, but not inDestroying:" + r + ", app=" + r.app);
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
            r.tracker.setExecuting(false, memFactor, now);
            r.tracker.setBound(false, memFactor, now);
            r.tracker.setStarted(false, memFactor, now);
        }
        serviceDoneExecutingLocked(r, true, true);
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
                r.app.execServicesFg = false;
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
                            r.app.execServicesFg = true;
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
                if (!(r.app == null || (r.app.persistent ^ 1) == 0)) {
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
        boolean didSomething = false;
        if (this.mPendingServices.size() > 0) {
            sr = null;
            i = 0;
            while (i < this.mPendingServices.size()) {
                try {
                    sr = (ServiceRecord) this.mPendingServices.get(i);
                    if (proc == sr.isolatedProc || (proc.uid == sr.appInfo.uid && (processName.equals(sr.processName) ^ 1) == 0)) {
                        this.mPendingServices.remove(i);
                        i--;
                        proc.addPackage(sr.appInfo.packageName, sr.appInfo.versionCode, this.mAm.mProcessStats);
                        realStartServiceLocked(sr, proc, sr.createdFromFg);
                        didSomething = true;
                        if (!isServiceNeededLocked(sr, false, false)) {
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
            for (i = 0; i < this.mRestartingServices.size(); i++) {
                sr = (ServiceRecord) this.mRestartingServices.get(i);
                if (proc == sr.isolatedProc || (proc.uid == sr.appInfo.uid && (processName.equals(sr.processName) ^ 1) == 0)) {
                    this.mAm.mHandler.removeCallbacks(sr.restarter);
                    this.mAm.mHandler.post(sr.restarter);
                }
            }
        }
        return didSomething;
    }

    void processStartTimedOutLocked(ProcessRecord proc) {
        int i = 0;
        while (i < this.mPendingServices.size()) {
            ServiceRecord sr = (ServiceRecord) this.mPendingServices.get(i);
            if ((proc.uid == sr.appInfo.uid && proc.processName.equals(sr.processName)) || sr.isolatedProc == proc) {
                Slog.w(TAG, "Forcing bringing down service: " + sr);
                sr.isolatedProc = null;
                this.mPendingServices.remove(i);
                i--;
                bringDownServiceLocked(sr);
            }
            i++;
        }
    }

    private boolean collectPackageServicesLocked(String packageName, Set<String> filterByClasses, boolean evenPersistent, boolean doit, boolean killProcess, ArrayMap<ComponentName, ServiceRecord> services) {
        boolean didSomething = false;
        for (int i = services.size() - 1; i >= 0; i--) {
            ServiceRecord service = (ServiceRecord) services.valueAt(i);
            if (service != null) {
                boolean sameComponent;
                if (packageName == null) {
                    sameComponent = true;
                } else if (!service.packageName.equals(packageName)) {
                    sameComponent = false;
                } else if (filterByClasses != null) {
                    sameComponent = filterByClasses.contains(service.name.getClassName());
                } else {
                    sameComponent = true;
                }
                if (sameComponent && (service.app == null || evenPersistent || (service.app.persistent ^ 1) != 0)) {
                    if (!doit) {
                        return true;
                    }
                    didSomething = true;
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
        }
        return didSomething;
    }

    boolean bringDownDisabledPackageServicesLocked(String packageName, Set<String> filterByClasses, int userId, boolean evenPersistent, boolean killProcess, boolean doit) {
        int i;
        boolean didSomething = false;
        if (this.mTmpCollectionResults != null) {
            this.mTmpCollectionResults.clear();
        }
        if (userId == -1) {
            for (i = this.mServiceMap.size() - 1; i >= 0; i--) {
                didSomething |= collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, killProcess, ((ServiceMap) this.mServiceMap.valueAt(i)).mServicesByName);
                if (!doit && didSomething) {
                    return true;
                }
                if (doit && filterByClasses == null) {
                    forceStopPackageLocked(packageName, ((ServiceMap) this.mServiceMap.valueAt(i)).mUserId);
                }
            }
        } else {
            ServiceMap smap = (ServiceMap) this.mServiceMap.get(userId);
            if (smap != null) {
                didSomething = collectPackageServicesLocked(packageName, filterByClasses, evenPersistent, doit, killProcess, smap.mServicesByName);
            }
            if (doit && filterByClasses == null) {
                forceStopPackageLocked(packageName, userId);
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

    void forceStopPackageLocked(String packageName, int userId) {
        ServiceMap smap = (ServiceMap) this.mServiceMap.get(userId);
        if (smap != null && smap.mActiveForegroundApps.size() > 0) {
            for (int i = smap.mActiveForegroundApps.size() - 1; i >= 0; i--) {
                if (((ActiveForegroundApp) smap.mActiveForegroundApps.valueAt(i)).mPackageName.equals(packageName)) {
                    smap.mActiveForegroundApps.removeAt(i);
                    smap.mActiveForegroundAppsChanged = true;
                }
            }
            if (smap.mActiveForegroundAppsChanged) {
                requestUpdateActiveForegroundAppsLocked(smap, 0);
            }
        }
    }

    void cleanUpRemovedTaskLocked(TaskRecord tr, ComponentName component, Intent baseIntent) {
        int i;
        ServiceRecord sr;
        ArrayList<ServiceRecord> services = new ArrayList();
        ArrayMap<ComponentName, ServiceRecord> alls = getServicesLocked(tr.userId);
        for (i = alls.size() - 1; i >= 0; i--) {
            sr = (ServiceRecord) alls.valueAt(i);
            if (sr != null && sr.packageName.equals(component.getPackageName())) {
                services.add(sr);
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
                    sr.pendingStarts.add(new StartItem(sr, true, sr.makeNextStartId(), baseIntent, null, 0));
                    if (!(sr.app == null || sr.app.thread == null)) {
                        try {
                            sendServiceArgsLocked(sr, true, false);
                        } catch (TransactionTooLargeException e) {
                        }
                    }
                }
            }
        }
    }

    final void killServicesLocked(ProcessRecord app, boolean allowRestart) {
        int i;
        ServiceRecord sr;
        for (i = app.connections.size() - 1; i >= 0; i--) {
            removeConnectionLocked((ConnectionRecord) app.connections.valueAt(i), app, null);
        }
        updateServiceConnectionActivitiesLocked(app);
        app.connections.clear();
        app.whitelistManager = false;
        for (i = app.services.size() - 1; i >= 0; i--) {
            sr = (ServiceRecord) app.services.valueAt(i);
            synchronized (sr.stats.getBatteryStats()) {
                sr.stats.stopLaunchedLocked();
            }
            if (!(sr.app == app || sr.app == null || (sr.app.persistent ^ 1) == 0)) {
                sr.app.services.remove(sr);
            }
            sr.app = null;
            sr.isolatedProc = null;
            sr.executeNesting = 0;
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
                b.hasBound = false;
                b.received = false;
                b.requested = false;
                for (int appi = b.apps.size() - 1; appi >= 0; appi--) {
                    ProcessRecord proc = (ProcessRecord) b.apps.keyAt(appi);
                    if (!proc.killedByAm && proc.thread != null) {
                        AppBindRecord abind = (AppBindRecord) b.apps.valueAt(appi);
                        for (int conni = abind.connections.size() - 1; conni >= 0; conni--) {
                            if ((((ConnectionRecord) abind.connections.valueAt(conni)).flags & 49) == 1) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        ServiceMap smap = getServiceMapLocked(app.userId);
        boolean getPreventResult = false;
        boolean preventRestart = false;
        for (i = app.services.size() - 1; i >= 0; i--) {
            sr = (ServiceRecord) app.services.valueAt(i);
            if (!app.persistent) {
                app.services.removeAt(i);
            }
            ServiceRecord curRec = (ServiceRecord) smap.mServicesByName.get(sr.name);
            if (curRec == sr) {
                if (allowRestart) {
                    allowRestart = this.mAm.isAcquireAppServiceResourceLocked(sr, app);
                    if (!allowRestart) {
                        Flog.i(102, "Service " + sr + " in process " + app + " prevent restart by RMS srname: " + sr.name);
                    }
                }
                if (allowRestart && sr.crashCount >= 2 && (sr.serviceInfo.applicationInfo.flags & 8) == 0) {
                    Slog.w(TAG, "Service crashed " + sr.crashCount + " times, stopping: " + sr);
                    EventLog.writeEvent(EventLogTags.AM_SERVICE_CRASHED_TOO_MUCH, new Object[]{Integer.valueOf(sr.userId), Integer.valueOf(sr.crashCount), sr.shortName, Integer.valueOf(app.pid)});
                    bringDownServiceLocked(sr);
                } else if (allowRestart && (this.mAm.mUserController.isUserRunningLocked(sr.userId, 0) ^ 1) == 0) {
                    boolean canceled = scheduleServiceRestartLocked(sr, true);
                    boolean bringDown = false;
                    if (sr.startRequested && ((sr.stopIfKilled || canceled) && sr.pendingStarts.size() == 0)) {
                        sr.startRequested = false;
                        if (sr.tracker != null) {
                            sr.tracker.setStarted(false, this.mAm.mProcessStats.getMemFactorLocked(), SystemClock.uptimeMillis());
                        }
                        if (!sr.hasAutoCreateConnections()) {
                            bringDownServiceLocked(sr);
                            bringDown = true;
                        }
                    }
                    if (!bringDown) {
                        if (!getPreventResult) {
                            preventRestart = this.mAm.shouldPreventRestartService(sr.serviceInfo, false);
                            getPreventResult = true;
                        }
                        allowRestart = preventRestart ^ 1;
                        if (!allowRestart) {
                            bringDownServiceLocked(sr);
                        }
                    }
                } else {
                    bringDownServiceLocked(sr);
                }
            } else if (curRec != null) {
                Slog.wtf(TAG, "Service " + sr + " in process " + app + " not same as in map: " + curRec);
            }
        }
        if (!allowRestart) {
            ServiceRecord r;
            app.services.clear();
            for (i = this.mRestartingServices.size() - 1; i >= 0; i--) {
                r = (ServiceRecord) this.mRestartingServices.get(i);
                if (r.processName.equals(app.processName) && r.serviceInfo.applicationInfo.uid == app.info.uid) {
                    this.mRestartingServices.remove(i);
                    clearRestartingIfNeededLocked(r);
                }
            }
            for (i = this.mPendingServices.size() - 1; i >= 0; i--) {
                r = (ServiceRecord) this.mPendingServices.get(i);
                if (r.processName.equals(app.processName) && r.serviceInfo.applicationInfo.uid == app.info.uid) {
                    this.mPendingServices.remove(i);
                }
            }
        }
        i = this.mDestroyingServices.size();
        while (i > 0) {
            i--;
            sr = (ServiceRecord) this.mDestroyingServices.get(i);
            if (sr.app == app) {
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
            info.flags |= 4;
        }
        if (r.app != null && r.app.persistent) {
            info.flags |= 8;
        }
        for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
            ArrayList<ConnectionRecord> connl = (ArrayList) r.connections.valueAt(conni);
            for (int i = 0; i < connl.size(); i++) {
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

    List<RunningServiceInfo> getRunningServiceInfoLocked(int maxNum, int flags, int callingUid, boolean allowed, boolean canInteractAcrossUsers) {
        ArrayList<RunningServiceInfo> res = new ArrayList();
        long ident = Binder.clearCallingIdentity();
        ArrayMap<ComponentName, ServiceRecord> alls;
        int i;
        ServiceRecord sr;
        ServiceRecord r;
        RunningServiceInfo info;
        if (canInteractAcrossUsers) {
            try {
                int[] users = this.mAm.mUserController.getUsers();
                for (int ui = 0; ui < users.length && res.size() < maxNum; ui++) {
                    alls = getServicesLocked(users[ui]);
                    for (i = 0; i < alls.size() && res.size() < maxNum; i++) {
                        sr = (ServiceRecord) alls.valueAt(i);
                        if (sr != null) {
                            res.add(makeRunningServiceInfoLocked(sr));
                        }
                    }
                }
                for (i = 0; i < this.mRestartingServices.size() && res.size() < maxNum; i++) {
                    r = (ServiceRecord) this.mRestartingServices.get(i);
                    if (r != null) {
                        info = makeRunningServiceInfoLocked(r);
                        info.restarting = r.nextRestartTime;
                        res.add(info);
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            int userId = UserHandle.getUserId(callingUid);
            alls = getServicesLocked(userId);
            for (i = 0; i < alls.size() && res.size() < maxNum; i++) {
                sr = (ServiceRecord) alls.valueAt(i);
                if ((allowed || !(sr == null || sr.app == null || sr.app.uid != callingUid)) && sr != null) {
                    res.add(makeRunningServiceInfoLocked(sr));
                }
            }
            for (i = 0; i < this.mRestartingServices.size() && res.size() < maxNum; i++) {
                r = (ServiceRecord) this.mRestartingServices.get(i);
                if (r.userId == userId && ((allowed || !(r == null || r.app == null || r.app.uid != callingUid)) && r != null)) {
                    info = makeRunningServiceInfoLocked(r);
                    info.restarting = r.nextRestartTime;
                    res.add(info);
                }
            }
        }
        Binder.restoreCallingIdentity(ident);
        return res;
    }

    public PendingIntent getRunningServiceControlPanelLocked(ComponentName name) {
        ServiceRecord r = getServiceByNameLocked(name, UserHandle.getUserId(Binder.getCallingUid()));
        if (r != null) {
            for (int conni = r.connections.size() - 1; conni >= 0; conni--) {
                ArrayList<ConnectionRecord> conn = (ArrayList) r.connections.valueAt(conni);
                for (int i = 0; i < conn.size(); i++) {
                    if (((ConnectionRecord) conn.get(i)).clientIntent != null) {
                        return ((ConnectionRecord) conn.get(i)).clientIntent;
                    }
                }
            }
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:30:0x0133, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:31:0x0136, code:
            if (r7 == null) goto L_0x0146;
     */
    /* JADX WARNING: Missing block: B:32:0x0138, code:
            r26.mAm.mAppErrors.appNotResponding(r27, null, null, false, r7);
     */
    /* JADX WARNING: Missing block: B:33:0x0146, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void serviceTimeout(ProcessRecord proc) {
        String anrMessage = null;
        Boolean frozenAnr = Boolean.valueOf(false);
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (proc.executingServices.size() == 0 || proc.thread == null) {
                } else {
                    long now = SystemClock.uptimeMillis();
                    long maxTime = now - ((long) (proc.execServicesFg ? SERVICE_TIMEOUT : SERVICE_BACKGROUND_TIMEOUT));
                    ServiceRecord timeout = null;
                    long nextTime = 0;
                    int i = proc.executingServices.size() - 1;
                    while (i >= 0) {
                        ServiceRecord sr = (ServiceRecord) proc.executingServices.valueAt(i);
                        if (proc.execServicesFg && sr.executingStart < now - ((long) CHECK_INTERVAL) && this.mAm.isTopProcessLocked(sr.app)) {
                            timeout = sr;
                            frozenAnr = Boolean.valueOf(true);
                            break;
                        } else if (sr.executingStart < maxTime) {
                            timeout = sr;
                            frozenAnr = Boolean.valueOf(false);
                            break;
                        } else {
                            if (sr.executingStart > nextTime) {
                                nextTime = sr.executingStart;
                            }
                            i--;
                        }
                    }
                    if (timeout == null || !this.mAm.mLruProcesses.contains(proc)) {
                        Message msg = this.mAm.mHandler.obtainMessage(12);
                        msg.obj = proc;
                        if (nextTime == 0) {
                            Slog.e(TAG, "nextTime invaild, remove the message");
                            this.mAm.mHandler.removeMessages(12, proc);
                        } else if (((long) CHECK_INTERVAL) + nextTime < now) {
                            this.mAm.mHandler.sendMessageAtTime(msg, ((long) (proc.execServicesFg ? SERVICE_TIMEOUT : SERVICE_BACKGROUND_TIMEOUT)) + nextTime);
                        } else {
                            this.mAm.mHandler.sendMessageAtTime(msg, ((long) (proc.execServicesFg ? CHECK_INTERVAL : SERVICE_BACKGROUND_TIMEOUT)) + nextTime);
                        }
                    } else {
                        if (frozenAnr.booleanValue()) {
                            Slog.w(TAG, "ANR has been triggered " + ((((long) SERVICE_TIMEOUT) + timeout.executingStart) - now) + "ms earlier because it caused frozen problem.");
                        }
                        Slog.w(TAG, "Timeout executing service: " + timeout);
                        Writer sw = new StringWriter();
                        PrintWriter pw = new FastPrintWriter(sw, false, 1024);
                        pw.println(timeout);
                        timeout.dump(pw, "    ");
                        pw.close();
                        this.mLastAnrDump = sw.toString();
                        this.mAm.mHandler.removeCallbacks(this.mLastAnrDumpClearer);
                        this.mAm.mHandler.postDelayed(this.mLastAnrDumpClearer, 7200000);
                        anrMessage = "executing service " + timeout.shortName;
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Missing block: B:15:0x003b, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:16:0x003e, code:
            if (r1 == null) goto L_0x004b;
     */
    /* JADX WARNING: Missing block: B:17:0x0040, code:
            r7.mAm.mAppErrors.appNotResponding(r1, null, null, false, "Context.startForegroundService() did not then call Service.startForeground()");
     */
    /* JADX WARNING: Missing block: B:18:0x004b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void serviceForegroundTimeout(ServiceRecord r) {
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (!r.fgRequired || r.destroying) {
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK) {
                        Slog.i(TAG, "Service foreground-required timeout for " + r);
                    }
                    ProcessRecord app = r.app;
                    r.fgWaiting = false;
                    stopServiceLocked(r);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void serviceForegroundCrash(ProcessRecord app) {
        this.mAm.crashApplication(app.uid, app.pid, app.info.packageName, app.userId, "Context.startForegroundService() did not then call Service.startForeground()");
    }

    void scheduleServiceTimeoutLocked(ProcessRecord proc) {
        if (proc.executingServices.size() == 0 || proc.thread == null) {
            Flog.i(102, "no need to schedule service timeout");
            return;
        }
        Message msg = this.mAm.mHandler.obtainMessage(12);
        msg.obj = proc;
        this.mAm.mHandler.sendMessageDelayed(msg, (long) (proc.execServicesFg ? CHECK_INTERVAL : SERVICE_BACKGROUND_TIMEOUT));
        msg = this.mAm.mHandler.obtainMessage(99);
        msg.obj = proc;
        this.mAm.mHandler.sendMessageDelayed(msg, 3000);
    }

    void scheduleServiceForegroundTransitionTimeoutLocked(ServiceRecord r) {
        if (r.app.executingServices.size() != 0 && r.app.thread != null) {
            Message msg = this.mAm.mHandler.obtainMessage(66);
            msg.obj = r;
            r.fgWaiting = true;
            this.mAm.mHandler.sendMessageDelayed(msg, 5000);
        }
    }

    ServiceDumper newServiceDumperLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        return new ServiceDumper(fd, pw, args, opti, dumpAll, dumpPackage);
    }

    protected boolean dumpService(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        int i;
        ArrayList<ServiceRecord> services = new ArrayList();
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                int[] users = this.mAm.mUserController.getUsers();
                ServiceMap smap;
                ArrayMap<ComponentName, ServiceRecord> alls;
                if ("all".equals(name)) {
                    for (int user : users) {
                        smap = (ServiceMap) this.mServiceMap.get(user);
                        if (smap != null) {
                            alls = smap.mServicesByName;
                            for (i = 0; i < alls.size(); i++) {
                                services.add((ServiceRecord) alls.valueAt(i));
                            }
                        }
                    }
                } else {
                    Object componentName = name != null ? ComponentName.unflattenFromString(name) : null;
                    int objectId = 0;
                    if (componentName == null) {
                        objectId = Integer.parseInt(name, 16);
                        name = null;
                        componentName = null;
                    }
                    for (int user2 : users) {
                        smap = (ServiceMap) this.mServiceMap.get(user2);
                        if (smap != null) {
                            alls = smap.mServicesByName;
                            for (i = 0; i < alls.size(); i++) {
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
            return false;
        }
        boolean needSep = false;
        for (i = 0; i < services.size(); i++) {
            if (needSep) {
                pw.println();
            }
            needSep = true;
            dumpService("", fd, pw, (ServiceRecord) services.get(i), args, dumpAll);
        }
        return true;
    }

    private void dumpService(String prefix, FileDescriptor fd, PrintWriter pw, ServiceRecord r, String[] args, boolean dumpAll) {
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
            TransferPipe tp;
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
            } catch (Throwable th) {
                tp.kill();
            }
        }
    }
}
