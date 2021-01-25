package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.hsm.HwSystemManager;
import android.net.INetd;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.util.EventLog;
import android.util.Flog;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.server.display.color.DisplayTransformManager;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class BroadcastQueue extends AbsBroadcastQueue {
    static final int BROADCAST_CHECKTIMEOUT_MSG = 203;
    private static final int BROADCAST_ENQUEUED_TIMEOUT = 2000;
    static final int BROADCAST_INTENT_MSG = 200;
    static final int BROADCAST_TIMEOUT_MSG = 201;
    static final int MAX_BROADCAST_HISTORY = ((ActivityManager.isLowRamDeviceStatic() || SystemProperties.getBoolean("ro.config.hw_low_ram", false)) ? 10 : 50);
    static final int MAX_BROADCAST_SUMMARY_HISTORY = ((ActivityManager.isLowRamDeviceStatic() || SystemProperties.getBoolean("ro.config.hw_low_ram", false)) ? 25 : DisplayTransformManager.LEVEL_COLOR_MATRIX_INVERT_COLOR);
    static final int MAYBE_BROADCAST_BG_TIMEOUT = 5000;
    static final int MAYBE_BROADCAST_FG_TIMEOUT = 2000;
    private static final String TAG = "BroadcastQueue";
    private static final String TAG_BROADCAST = "BroadcastQueue";
    private static final String TAG_MU = "BroadcastQueue_MU";
    final BroadcastRecord[] mBroadcastHistory = new BroadcastRecord[MAX_BROADCAST_HISTORY];
    final Intent[] mBroadcastSummaryHistory;
    boolean mBroadcastsScheduled;
    final BroadcastConstants mConstants;
    final boolean mDelayBehindServices;
    final BroadcastDispatcher mDispatcher;
    final BroadcastHandler mHandler;
    int mHistoryNext = 0;
    boolean mLogLatencyMetrics;
    private int mNextToken = 0;
    final ArrayList<BroadcastRecord> mParallelBroadcasts = new ArrayList<>();
    BroadcastRecord mPendingBroadcast;
    int mPendingBroadcastRecvIndex;
    boolean mPendingBroadcastTimeoutMessage;
    final String mQueueName;
    final ActivityManagerService mService;
    final SparseIntArray mSplitRefcounts = new SparseIntArray();
    final long[] mSummaryHistoryDispatchTime;
    final long[] mSummaryHistoryEnqueueTime;
    final long[] mSummaryHistoryFinishTime;
    int mSummaryHistoryNext;

    /* access modifiers changed from: private */
    public final class BroadcastHandler extends Handler {
        public BroadcastHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 200) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v("BroadcastQueue", "Received BROADCAST_INTENT_MSG [" + BroadcastQueue.this.mQueueName + "]");
                }
                BroadcastQueue.this.processNextBroadcast(true);
            } else if (i == BroadcastQueue.BROADCAST_TIMEOUT_MSG) {
                synchronized (BroadcastQueue.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        BroadcastQueue.this.broadcastTimeoutLocked(true);
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == BroadcastQueue.BROADCAST_CHECKTIMEOUT_MSG) {
                synchronized (BroadcastQueue.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        BroadcastQueue.this.handleMaybeTimeoutBr();
                        BroadcastQueue.this.uploadRadarMessage(2803, null);
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class AppNotResponding implements Runnable {
        private final String mAnnotation;
        private final ProcessRecord mApp;

        public AppNotResponding(ProcessRecord app, String annotation) {
            this.mApp = app;
            this.mAnnotation = annotation;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mApp.appNotResponding(null, null, null, null, false, this.mAnnotation);
        }
    }

    BroadcastQueue(ActivityManagerService service, Handler handler, String name, BroadcastConstants constants, boolean allowDelayBehindServices) {
        int i = MAX_BROADCAST_SUMMARY_HISTORY;
        this.mBroadcastSummaryHistory = new Intent[i];
        this.mSummaryHistoryNext = 0;
        this.mSummaryHistoryEnqueueTime = new long[i];
        this.mSummaryHistoryDispatchTime = new long[i];
        this.mSummaryHistoryFinishTime = new long[i];
        this.mBroadcastsScheduled = false;
        this.mPendingBroadcast = null;
        this.mLogLatencyMetrics = true;
        this.mService = service;
        this.mHandler = new BroadcastHandler(handler.getLooper());
        this.mQueueName = name;
        this.mDelayBehindServices = allowDelayBehindServices;
        this.mConstants = constants;
        this.mDispatcher = new BroadcastDispatcher(this, this.mConstants, this.mHandler, this.mService);
    }

    /* access modifiers changed from: package-private */
    public void start(ContentResolver resolver) {
        this.mDispatcher.start();
        this.mConstants.startObserving(this.mHandler, resolver);
    }

    public String toString() {
        return this.mQueueName;
    }

    public boolean isPendingBroadcastProcessLocked(int pid) {
        BroadcastRecord broadcastRecord = this.mPendingBroadcast;
        return broadcastRecord != null && broadcastRecord.curApp.pid == pid;
    }

    public void enqueueParallelBroadcastLocked(BroadcastRecord r) {
        this.mParallelBroadcasts.add(r);
        enqueueBroadcastHelper(r);
    }

    public void enqueueOrderedBroadcastLocked(BroadcastRecord r) {
        this.mDispatcher.enqueueOrderedBroadcastLocked(r);
        enqueueBroadcastHelper(r);
    }

    private void enqueueBroadcastHelper(BroadcastRecord r) {
        r.enqueueClockTime = System.currentTimeMillis();
        if (Trace.isTagEnabled(64)) {
            Trace.asyncTraceBegin(64, createBroadcastTraceTitle(r, 0), System.identityHashCode(r));
        }
    }

    public final BroadcastRecord replaceParallelBroadcastLocked(BroadcastRecord r) {
        return replaceBroadcastLocked(this.mParallelBroadcasts, r, "PARALLEL");
    }

    public final BroadcastRecord replaceOrderedBroadcastLocked(BroadcastRecord r) {
        return this.mDispatcher.replaceBroadcastLocked(r, "ORDERED");
    }

    private BroadcastRecord replaceBroadcastLocked(ArrayList<BroadcastRecord> queue, BroadcastRecord r, String typeForLogging) {
        Intent intent = r.intent;
        for (int i = queue.size() - 1; i > 0; i--) {
            BroadcastRecord old = queue.get(i);
            if (old.userId == r.userId && intent.filterEquals(old.intent)) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v("BroadcastQueue", "***** DROPPING " + typeForLogging + " [" + this.mQueueName + "]: " + intent);
                }
                queue.set(i, r);
                return old;
            }
        }
        return null;
    }

    /* JADX WARN: Type inference failed for: r4v1, types: [com.android.server.am.ProcessRecord, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r4v4, types: [com.android.server.am.ProcessRecord, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private final void processCurBroadcastLocked(BroadcastRecord r, ProcessRecord app, boolean skipOomAdj) throws RemoteException {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(app.uid, app.pid, IHwBehaviorCollectManager.BehaviorId.BROADCASTQUEUE_PROCESSCURBROADCASTLOCKED, new Object[]{r.intent});
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v("BroadcastQueue", "Process cur broadcast " + r + " for app " + app);
        }
        if (app.thread == null) {
            throw new RemoteException();
        } else if (app.inFullBackup) {
            skipReceiverLocked(r);
        } else {
            r.receiver = app.thread.asBinder();
            r.curApp = app;
            app.curReceivers.add(r);
            app.forceProcessStateUpTo(12);
            this.mService.mProcessList.updateLruProcessLocked(app, false, null);
            if (!skipOomAdj) {
                this.mService.updateOomAdjLocked("updateOomAdj_meh");
            }
            r.intent.setComponent(r.curComponent);
            boolean started = false;
            try {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.v("BroadcastQueue", "Delivering to component " + r.curComponent + ": " + r);
                }
                this.mService.notifyPackageUse(r.intent.getComponent().getPackageName(), 3);
                app.thread.scheduleReceiver(new Intent(r.intent), r.curReceiver, this.mService.compatibilityInfoForPackage(r.curReceiver.applicationInfo), r.resultCode, r.resultData, r.resultExtras, r.ordered, r.userId, app.getReportedProcState());
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v("BroadcastQueue", "Process cur broadcast " + r + " DELIVERED for app " + app);
                }
                started = true;
            } finally {
                if (!started) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v("BroadcastQueue", "Process cur broadcast " + r + ": NOT STARTED!");
                    }
                    ?? r4 = 0;
                    r.receiver = r4;
                    r.curApp = r4;
                    app.curReceivers.remove(r);
                }
            }
        }
    }

    public boolean sendPendingBroadcastsLocked(ProcessRecord app) {
        BroadcastRecord br = this.mPendingBroadcast;
        if (br == null || br.curApp.pid <= 0 || br.curApp.pid != app.pid) {
            return false;
        }
        if (br.curApp != app) {
            Slog.e("BroadcastQueue", "App mismatch when sending pending broadcast to " + app.processName + ", intended target is " + br.curApp.processName);
            return false;
        }
        try {
            this.mPendingBroadcast = null;
            processCurBroadcastLocked(br, app, false);
            return true;
        } catch (Exception e) {
            Slog.w("BroadcastQueue", "Exception in new application when starting receiver " + br.curComponent.flattenToShortString(), e);
            logBroadcastReceiverDiscardLocked(br);
            finishReceiverLocked(br, br.resultCode, br.resultData, br.resultExtras, br.resultAbort, false);
            scheduleBroadcastsLocked();
            br.state = 0;
            throw new RuntimeException(e.getMessage());
        }
    }

    public void skipPendingBroadcastLocked(int pid) {
        BroadcastRecord br = this.mPendingBroadcast;
        if (br != null && br.curApp.pid == pid) {
            br.state = 0;
            br.nextReceiver = this.mPendingBroadcastRecvIndex;
            this.mPendingBroadcast = null;
            scheduleBroadcastsLocked();
        }
    }

    public void skipCurrentReceiverLocked(ProcessRecord app) {
        BroadcastRecord broadcastRecord;
        BroadcastRecord r = null;
        BroadcastRecord curActive = this.mDispatcher.getActiveBroadcastLocked();
        if (curActive != null && curActive.curApp == app) {
            r = curActive;
        }
        if (r == null && (broadcastRecord = this.mPendingBroadcast) != null && broadcastRecord.curApp == app) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v("BroadcastQueue", "[" + this.mQueueName + "] skip & discard pending app " + r);
            }
            r = this.mPendingBroadcast;
        }
        if (r != null) {
            skipReceiverLocked(r);
        }
    }

    private void skipReceiverLocked(BroadcastRecord r) {
        logBroadcastReceiverDiscardLocked(r);
        finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
        scheduleBroadcastsLocked();
    }

    public void scheduleBroadcastsLocked() {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v("BroadcastQueue", "Schedule broadcasts [" + this.mQueueName + "]: current=" + this.mBroadcastsScheduled);
        }
        if (!this.mBroadcastsScheduled) {
            BroadcastHandler broadcastHandler = this.mHandler;
            this.mBroadcastsScheduled = broadcastHandler.sendMessage(broadcastHandler.obtainMessage(200, this));
            if (!this.mBroadcastsScheduled) {
                Slog.e("BroadcastQueue", "Schedule broadcasts fail. Queue name is " + this.mQueueName);
            }
        }
    }

    public BroadcastRecord getMatchingOrderedReceiver(IBinder receiver) {
        BroadcastRecord br = this.mDispatcher.getActiveBroadcastLocked();
        if (br == null || br.receiver != receiver) {
            return null;
        }
        return br;
    }

    private int nextSplitTokenLocked() {
        int next = this.mNextToken + 1;
        if (next <= 0) {
            next = 1;
        }
        this.mNextToken = next;
        return next;
    }

    private void postActivityStartTokenRemoval(ProcessRecord app, BroadcastRecord r) {
        String msgToken = (app.toShortString() + r.toString()).intern();
        this.mHandler.removeCallbacksAndMessages(msgToken);
        this.mHandler.postAtTime(new Runnable(app, r) {
            /* class com.android.server.am.$$Lambda$BroadcastQueue$Rc4kAs41vmqWweLcJR0YLxZ0dM */
            private final /* synthetic */ ProcessRecord f$1;
            private final /* synthetic */ BroadcastRecord f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                BroadcastQueue.this.lambda$postActivityStartTokenRemoval$0$BroadcastQueue(this.f$1, this.f$2);
            }
        }, msgToken, r.receiverTime + this.mConstants.ALLOW_BG_ACTIVITY_START_TIMEOUT);
    }

    public /* synthetic */ void lambda$postActivityStartTokenRemoval$0$BroadcastQueue(ProcessRecord app, BroadcastRecord r) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                app.removeAllowBackgroundActivityStartsToken(r);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean finishReceiverLocked(BroadcastRecord r, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort, boolean waitForServices) {
        ActivityInfo nextReceiver;
        boolean z = false;
        if (r.curApp != null) {
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(r.curApp.uid, r.curApp.pid, IHwBehaviorCollectManager.BehaviorId.BROADCASTQUEUE_FINISHRECEIVERLOCKED, new Object[]{r.intent});
        }
        int state = r.state;
        ActivityInfo receiver = r.curReceiver;
        long elapsed = SystemClock.uptimeMillis() - r.receiverTime;
        r.state = 0;
        if (state == 0) {
            Slog.w("BroadcastQueue", "finishReceiver [" + this.mQueueName + "] called but state is IDLE");
        }
        if (r.allowBackgroundActivityStarts && r.curApp != null) {
            if (elapsed > this.mConstants.ALLOW_BG_ACTIVITY_START_TIMEOUT) {
                r.curApp.removeAllowBackgroundActivityStartsToken(r);
            } else {
                postActivityStartTokenRemoval(r.curApp, r);
            }
        }
        if (r.nextReceiver > 0) {
            r.duration[r.nextReceiver - 1] = elapsed;
        }
        if (!r.timeoutExempt) {
            if (this.mConstants.SLOW_TIME > 0 && elapsed > this.mConstants.SLOW_TIME) {
                if (r.curApp != null && !UserHandle.isCore(r.curApp.uid)) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                        Slog.i("BroadcastQueue", "Broadcast receiver " + (r.nextReceiver - 1) + " was slow: " + receiver + " br=" + r);
                    }
                    this.mDispatcher.startDeferring(r.curApp.uid);
                } else if (r.curApp == null) {
                    Slog.d("BroadcastQueue", "finish receiver curApp is null? " + r);
                } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                    Slog.i("BroadcastQueue", "Core uid " + r.curApp.uid + " receiver was slow but not deferring: " + receiver + " br=" + r);
                }
            }
        } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
            Slog.i("BroadcastQueue", "Finished broadcast " + r.intent.getAction() + " is exempt from deferral policy");
        }
        r.receiver = null;
        r.intent.setComponent(null);
        if (r.curApp != null && r.curApp.curReceivers.contains(r)) {
            r.curApp.curReceivers.remove(r);
        }
        if (r.curFilter != null) {
            r.curFilter.receiverList.curBroadcast = null;
        }
        r.curFilter = null;
        r.curReceiver = null;
        r.curApp = null;
        this.mPendingBroadcast = null;
        r.resultCode = resultCode;
        r.resultData = resultData;
        r.resultExtras = resultExtras;
        if (!resultAbort || (r.intent.getFlags() & DumpState.DUMP_HWFEATURES) != 0) {
            r.resultAbort = false;
        } else {
            r.resultAbort = resultAbort;
        }
        if (waitForServices && r.curComponent != null && r.queue.mDelayBehindServices && r.queue.mDispatcher.getActiveBroadcastLocked() == r) {
            if (r.nextReceiver < r.receivers.size()) {
                Object obj = r.receivers.get(r.nextReceiver);
                nextReceiver = obj instanceof ActivityInfo ? (ActivityInfo) obj : null;
            } else {
                nextReceiver = null;
            }
            if (receiver != null && nextReceiver != null && receiver.applicationInfo.uid == nextReceiver.applicationInfo.uid && receiver.processName.equals(nextReceiver.processName)) {
                z = false;
            } else if (this.mService.mServices.hasBackgroundServicesLocked(r.userId)) {
                Slog.i("BroadcastQueue", "Delay finish: " + r.curComponent.flattenToShortString());
                r.state = 4;
                return false;
            } else {
                z = false;
            }
        }
        r.curComponent = null;
        if (state == 1 || state == 3) {
            return true;
        }
        return z;
    }

    public void backgroundServicesFinishedLocked(int userId) {
        BroadcastRecord br = this.mDispatcher.getActiveBroadcastLocked();
        if (br != null && br.userId == userId && br.state == 4) {
            Slog.i("BroadcastQueue", "Resuming delayed broadcast");
            br.curComponent = null;
            br.state = 0;
            processNextBroadcast(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver, Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
        if (app != null) {
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(app.uid, app.pid, IHwBehaviorCollectManager.BehaviorId.BROADCASTQUEUE_PERFORMRECEIVELOCKED, new Object[]{intent});
        }
        if (app == null) {
            receiver.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
        } else if (app.thread != null) {
            try {
                app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode, data, extras, ordered, sticky, sendingUser, app.getReportedProcState());
            } catch (RemoteException ex) {
                synchronized (this.mService) {
                    ActivityManagerService.boostPriorityForLockedSection();
                    Slog.w("BroadcastQueue", "Can't deliver broadcast to " + app.processName + " (pid " + app.pid + "). Crashing it.");
                    app.scheduleCrash("can't deliver broadcast");
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw ex;
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        } else {
            throw new RemoteException("app.thread must not be null");
        }
    }

    /* JADX WARN: Type inference failed for: r15v1, types: [com.android.server.am.BroadcastRecord, com.android.server.am.BroadcastFilter, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r15v2 */
    /* JADX WARN: Type inference failed for: r15v3 */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x04d8  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x05d6  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x05ea  */
    /* JADX WARNING: Removed duplicated region for block: B:151:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x039b  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x03b4  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0409  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x040b  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x04d3  */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void deliverToRegisteredReceiverLocked(BroadcastRecord r, BroadcastFilter filter, boolean ordered, int index) {
        boolean skip;
        boolean skip2;
        String str;
        ?? r15;
        RemoteException e;
        boolean skip3;
        boolean skip4 = false;
        if (!this.mService.validateAssociationAllowedLocked(r.callerPackage, r.callingUid, filter.packageName, filter.owningUid)) {
            Slog.w("BroadcastQueue", "Association not allowed: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") to " + filter.packageName + " through " + filter);
            skip4 = true;
        }
        if (!skip4 && !this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, filter.receiverList.uid)) {
            Slog.w("BroadcastQueue", "Firewall blocked: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") to " + filter.packageName + " through " + filter);
            skip4 = true;
        }
        boolean z = true;
        if (filter.requiredPermission != null) {
            ActivityManagerService activityManagerService = this.mService;
            if (ActivityManagerService.checkComponentPermission(filter.requiredPermission, r.callingPid, r.callingUid, -1, true) != 0) {
                Slog.w("BroadcastQueue", "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") requires " + filter.requiredPermission + " due to registered receiver " + filter);
                skip4 = true;
            } else {
                int opCode = AppOpsManager.permissionToOpCode(filter.requiredPermission);
                if (!(opCode == -1 || this.mService.mAppOpsService.noteOperation(opCode, r.callingUid, r.callerPackage) == 0)) {
                    Slog.w("BroadcastQueue", "Appop Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") requires appop " + AppOpsManager.permissionToOp(filter.requiredPermission) + " due to registered receiver " + filter);
                    skip4 = true;
                }
            }
        }
        if (skip4 || r.requiredPermissions == null || r.requiredPermissions.length <= 0) {
            skip4 = skip4;
        } else {
            int i = 0;
            while (true) {
                if (i >= r.requiredPermissions.length) {
                    break;
                }
                String requiredPermission = r.requiredPermissions[i];
                ActivityManagerService activityManagerService2 = this.mService;
                if (ActivityManagerService.checkComponentPermission(requiredPermission, filter.receiverList.pid, filter.receiverList.uid, -1, z) != 0) {
                    Slog.w("BroadcastQueue", "Permission Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires " + requiredPermission + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                    skip4 = true;
                    break;
                }
                int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                if (appOp != -1 && appOp != r.appOp) {
                    if (this.mService.mAppOpsService.noteOperation(appOp, filter.receiverList.uid, filter.packageName) != 0) {
                        Slog.w("BroadcastQueue", "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                        skip4 = true;
                        break;
                    }
                }
                i++;
                skip4 = skip4;
                z = true;
            }
            if (r.intent != null && "android.provider.Telephony.SMS_RECEIVED".equals(r.intent.getAction())) {
                HwSystemManager.insertSendBroadcastRecord(filter.packageName, r.intent.getAction(), filter.receiverList.uid);
            }
        }
        if (skip4) {
            skip3 = skip4;
        } else if (r.requiredPermissions == null || r.requiredPermissions.length == 0) {
            ActivityManagerService activityManagerService3 = this.mService;
            skip3 = skip4;
            if (ActivityManagerService.checkComponentPermission(null, filter.receiverList.pid, filter.receiverList.uid, -1, true) != 0) {
                Slog.w("BroadcastQueue", "Permission Denial: security check failed when receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                skip = true;
                if (!(skip || r.appOp == -1 || this.mService.mAppOpsService.noteOperation(r.appOp, filter.receiverList.uid, filter.packageName) == 0)) {
                    Slog.w("BroadcastQueue", "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                    skip = true;
                }
                if (skip) {
                    skip2 = skip;
                    str = " to ";
                    if (this.mService.mHwAMSEx.shouldPreventSendBroadcast(r, null, null, filter.packageName, false)) {
                        skip2 = true;
                    }
                } else {
                    skip2 = skip;
                    str = " to ";
                }
                if (!skip2 && (filter.receiverList.app == null || filter.receiverList.app.killed || filter.receiverList.app.isCrashing())) {
                    Slog.w("BroadcastQueue", "Skipping deliver [" + this.mQueueName + "] " + r + str + filter.receiverList + ": process gone or crashing");
                    skip2 = true;
                }
                boolean visibleToInstantApps = (r.intent.getFlags() & DumpState.DUMP_COMPILER_STATS) == 0;
                if (!skip2 && !visibleToInstantApps && filter.instantApp && filter.receiverList.uid != r.callingUid) {
                    Slog.w("BroadcastQueue", "Instant App Denial: receiving " + r.intent.toString() + str + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") due to sender " + r.callerPackage + " (uid " + r.callingUid + ") not specifying FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS");
                    skip2 = true;
                }
                if (!skip2 && !filter.visibleToInstantApp && r.callerInstantApp && filter.receiverList.uid != r.callingUid) {
                    Slog.w("BroadcastQueue", "Instant App Denial: receiving " + r.intent.toString() + str + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires receiver be visible to instant apps due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                    skip2 = true;
                }
                if (!skip2) {
                    r.delivery[index] = 2;
                    return;
                } else if (!requestStartTargetPermissionsReviewIfNeededLocked(r, filter.packageName, filter.owningUserId)) {
                    r.delivery[index] = 2;
                    return;
                } else if (!getMtmBRManagerEnabled(10) || !getMtmBRManager().iawareProcessBroadcast(0, !ordered, new BroadcastRecordEx(r), filter)) {
                    r.delivery[index] = 1;
                    if (ordered) {
                        r.receiver = filter.receiverList.receiver.asBinder();
                        r.curFilter = filter;
                        filter.receiverList.curBroadcast = r;
                        r.state = 2;
                        if (filter.receiverList.app != null) {
                            r.curApp = filter.receiverList.app;
                            filter.receiverList.app.curReceivers.add(r);
                            this.mService.updateOomAdjLocked(r.curApp, true, "updateOomAdj_startReceiver");
                        }
                    }
                    try {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                            Slog.i("BroadcastQueue", "Delivering to " + filter + " : " + r);
                        }
                        if (filter.receiverList.app == null || !filter.receiverList.app.inFullBackup) {
                            r.receiverTime = SystemClock.uptimeMillis();
                            maybeAddAllowBackgroundActivityStartsToken(filter.receiverList.app, r);
                            r15 = 0;
                            try {
                                performReceiveLocked(filter.receiverList.app, filter.receiverList.receiver, new Intent(r.intent), r.resultCode, r.resultData, r.resultExtras, r.ordered, r.initialSticky, r.userId);
                                if (r.allowBackgroundActivityStarts && !r.ordered) {
                                    postActivityStartTokenRemoval(filter.receiverList.app, r);
                                }
                            } catch (RemoteException e2) {
                                e = e2;
                                Slog.w("BroadcastQueue", "Failure sending broadcast " + r.intent, e);
                                if (filter.receiverList.app != null) {
                                    filter.receiverList.app.removeAllowBackgroundActivityStartsToken(r);
                                    if (ordered) {
                                        filter.receiverList.app.curReceivers.remove(r);
                                    }
                                }
                                if (!ordered) {
                                    r.receiver = r15;
                                    r.curFilter = r15;
                                    filter.receiverList.curBroadcast = r15;
                                    return;
                                }
                                return;
                            }
                        } else if (ordered) {
                            skipReceiverLocked(r);
                        }
                        if (ordered) {
                            r.state = 3;
                            return;
                        }
                        return;
                    } catch (RemoteException e3) {
                        e = e3;
                        r15 = 0;
                        Slog.w("BroadcastQueue", "Failure sending broadcast " + r.intent, e);
                        if (filter.receiverList.app != null) {
                        }
                        if (!ordered) {
                        }
                    }
                } else {
                    return;
                }
            }
        } else {
            skip3 = skip4;
        }
        skip = skip3;
        Slog.w("BroadcastQueue", "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
        skip = true;
        if (skip) {
        }
        Slog.w("BroadcastQueue", "Skipping deliver [" + this.mQueueName + "] " + r + str + filter.receiverList + ": process gone or crashing");
        skip2 = true;
        if ((r.intent.getFlags() & DumpState.DUMP_COMPILER_STATS) == 0) {
        }
        Slog.w("BroadcastQueue", "Instant App Denial: receiving " + r.intent.toString() + str + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") due to sender " + r.callerPackage + " (uid " + r.callingUid + ") not specifying FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS");
        skip2 = true;
        Slog.w("BroadcastQueue", "Instant App Denial: receiving " + r.intent.toString() + str + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ") requires receiver be visible to instant apps due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
        skip2 = true;
        if (!skip2) {
        }
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(BroadcastRecord receiverRecord, String receivingPackageName, final int receivingUserId) {
        boolean callerForeground;
        if (!this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(receivingPackageName, receivingUserId)) {
            return true;
        }
        if (receiverRecord.callerApp != null) {
            callerForeground = receiverRecord.callerApp.setSchedGroup != 0;
        } else {
            callerForeground = true;
        }
        if (!callerForeground || receiverRecord.intent.getComponent() == null) {
            Slog.w("BroadcastQueue", "u" + receivingUserId + " Receiving a broadcast in package" + receivingPackageName + " requires a permissions review");
        } else {
            IIntentSender target = this.mService.mPendingIntentController.getIntentSender(1, receiverRecord.callerPackage, receiverRecord.callingUid, receiverRecord.userId, null, null, 0, new Intent[]{receiverRecord.intent}, new String[]{receiverRecord.intent.resolveType(this.mService.mContext.getContentResolver())}, 1409286144, null);
            final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent.addFlags(411041792);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", receivingPackageName);
            intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                Slog.i("BroadcastQueue", "u" + receivingUserId + " Launching permission review for package " + receivingPackageName);
            }
            this.mHandler.post(new Runnable() {
                /* class com.android.server.am.BroadcastQueue.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    BroadcastQueue.this.mService.mContext.startActivityAsUser(intent, new UserHandle(receivingUserId));
                }
            });
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final void scheduleTempWhitelistLocked(int uid, long duration, BroadcastRecord r) {
        if (duration > 2147483647L) {
            duration = 2147483647L;
        }
        StringBuilder b = new StringBuilder();
        b.append("broadcast:");
        UserHandle.formatUid(b, r.callingUid);
        b.append(":");
        if (r.intent.getAction() != null) {
            b.append(r.intent.getAction());
        } else if (r.intent.getComponent() != null) {
            r.intent.getComponent().appendShortString(b);
        } else if (r.intent.getData() != null) {
            b.append(r.intent.getData());
        }
        this.mService.tempWhitelistUidLocked(uid, duration, b.toString());
    }

    /* access modifiers changed from: package-private */
    public final boolean isSignaturePerm(String[] perms) {
        if (perms == null) {
            return false;
        }
        IPackageManager pm = AppGlobals.getPackageManager();
        for (int i = perms.length - 1; i >= 0; i--) {
            try {
                PermissionInfo pi = pm.getPermissionInfo(perms[i], PackageManagerService.PLATFORM_PACKAGE_NAME, 0);
                if (pi == null || (pi.protectionLevel & 31) != 2) {
                    return false;
                }
            } catch (RemoteException e) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public final void processNextBroadcast(boolean fromMsg) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                processNextBroadcastLocked(fromMsg, false);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:451:0x110c, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:455:0x1155, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:456:0x1156, code lost:
        r37 = r1;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0612 A[SYNTHETIC, Splitter:B:177:0x0612] */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x06a8  */
    /* JADX WARNING: Removed duplicated region for block: B:196:0x06b7  */
    /* JADX WARNING: Removed duplicated region for block: B:407:0x0fd5  */
    /* JADX WARNING: Removed duplicated region for block: B:410:0x0ffd  */
    /* JADX WARNING: Removed duplicated region for block: B:413:0x1009  */
    /* JADX WARNING: Removed duplicated region for block: B:418:0x1048  */
    /* JADX WARNING: Removed duplicated region for block: B:451:0x110c A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:438:0x10e6] */
    /* JADX WARNING: Removed duplicated region for block: B:461:0x1181  */
    /* JADX WARNING: Removed duplicated region for block: B:473:0x1213  */
    /* JADX WARNING: Removed duplicated region for block: B:476:0x1219  */
    /* JADX WARNING: Removed duplicated region for block: B:477:0x1244  */
    /* JADX WARNING: Removed duplicated region for block: B:480:0x124e  */
    /* JADX WARNING: Removed duplicated region for block: B:483:0x1260  */
    /* JADX WARNING: Removed duplicated region for block: B:486:0x1285  */
    /* JADX WARNING: Removed duplicated region for block: B:487:0x1288  */
    /* JADX WARNING: Removed duplicated region for block: B:490:0x129e  */
    /* JADX WARNING: Removed duplicated region for block: B:492:0x12f2  */
    /* JADX WARNING: Removed duplicated region for block: B:494:0x12fc A[LOOP:2: B:69:0x022a->B:494:0x12fc, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:504:0x0716 A[SYNTHETIC] */
    public final void processNextBroadcastLocked(boolean fromMsg, boolean skipOomAdj) {
        int i;
        int numReceivers;
        BroadcastRecord r;
        int i2;
        IIntentReceiver iIntentReceiver;
        boolean skip;
        boolean isSingleton;
        ProcessRecord app;
        boolean skip2;
        boolean skip3;
        String targetProcess;
        boolean skip4;
        ComponentName component;
        int perm;
        ResolveInfo info;
        ResolveInfo info2;
        String targetProcess2;
        boolean z;
        ProcessRecord startProcessLocked;
        RemoteException e;
        boolean skip5;
        int opCode;
        boolean sendResult;
        RemoteException e2;
        BroadcastRecord defer;
        ProcessRecord proc;
        ProcessRecord processRecord;
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v("BroadcastQueue", "processNextBroadcast [" + this.mQueueName + "]: " + this.mParallelBroadcasts.size() + " parallel broadcasts; " + this.mDispatcher.describeStateLocked());
        }
        this.mService.updateCpuStats();
        if (fromMsg) {
            this.mBroadcastsScheduled = false;
        }
        while (true) {
            i = 1;
            if (this.mParallelBroadcasts.size() <= 0) {
                break;
            }
            BroadcastRecord r2 = this.mParallelBroadcasts.remove(0);
            r2.dispatchTime = SystemClock.uptimeMillis();
            r2.dispatchClockTime = System.currentTimeMillis();
            if (Trace.isTagEnabled(64)) {
                Trace.asyncTraceEnd(64, createBroadcastTraceTitle(r2, 0), System.identityHashCode(r2));
                Trace.asyncTraceBegin(64, createBroadcastTraceTitle(r2, 1), System.identityHashCode(r2));
            }
            int N = r2.receivers.size();
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                Slog.v("BroadcastQueue", "Processing parallel broadcast [" + this.mQueueName + "] " + r2);
            }
            if (getMtmBRManagerEnabled(10)) {
                getMtmBRManager().iawareStartCountBroadcastSpeed(true, new BroadcastRecordEx(r2));
            }
            for (int i3 = 0; i3 < N; i3++) {
                Object target = r2.receivers.get(i3);
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v("BroadcastQueue", "Delivering non-ordered on [" + this.mQueueName + "] to registered " + target + ": " + r2);
                }
                if (!enqueueProxyBroadcast(true, r2, target)) {
                    deliverToRegisteredReceiverLocked(r2, (BroadcastFilter) target, false, i3);
                } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v("BroadcastQueue", "parallel " + this.mQueueName + " broadcast:(" + r2 + ") should be proxyed, target:(" + target + ")");
                }
            }
            if (getMtmBRManagerEnabled(10)) {
                getMtmBRManager().iawareEndCountBroadcastSpeed(new BroadcastRecordEx(r2));
            }
            addBroadcastToHistoryLocked(r2);
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                Slog.v("BroadcastQueue", "Done with parallel broadcast [" + this.mQueueName + "] " + r2);
            }
        }
        IIntentReceiver iIntentReceiver2 = null;
        if (this.mPendingBroadcast != null) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                Slog.v("BroadcastQueue", "processNextBroadcast [" + this.mQueueName + "]: waiting for " + this.mPendingBroadcast.curApp);
            }
            if (this.mPendingBroadcast.curApp.pid > 0) {
                synchronized (this.mService.mPidsSelfLocked) {
                    ProcessRecord proc2 = this.mService.mPidsSelfLocked.get(this.mPendingBroadcast.curApp.pid);
                    if (proc2 != null) {
                        if (!proc2.isCrashing()) {
                            processRecord = null;
                            proc = processRecord;
                        }
                    }
                    processRecord = 1;
                    proc = processRecord;
                }
            } else {
                ProcessRecord proc3 = (ProcessRecord) this.mService.mProcessList.mProcessNames.get(this.mPendingBroadcast.curApp.processName, this.mPendingBroadcast.curApp.uid);
                proc = (proc3 == null || !proc3.pendingStart) ? 1 : null;
            }
            if (proc != null) {
                Slog.w("BroadcastQueue", "pending app  [" + this.mQueueName + "]" + this.mPendingBroadcast.curApp + " died before responding to broadcast");
                BroadcastRecord broadcastRecord = this.mPendingBroadcast;
                broadcastRecord.state = 0;
                broadcastRecord.nextReceiver = this.mPendingBroadcastRecvIndex;
                this.mPendingBroadcast = null;
            } else {
                return;
            }
        }
        boolean looped = false;
        while (true) {
            long now = SystemClock.uptimeMillis();
            BroadcastRecord r3 = this.mDispatcher.getNextBroadcastLocked(now);
            if (r3 == null) {
                this.mDispatcher.scheduleDeferralCheckLocked(false);
                this.mService.scheduleAppGcsLocked();
                if (looped) {
                    this.mService.updateOomAdjLocked("updateOomAdj_startReceiver");
                }
                if (this.mService.mUserController.mBootCompleted && this.mLogLatencyMetrics) {
                    this.mLogLatencyMetrics = false;
                    return;
                }
                return;
            }
            boolean forceReceive = false;
            int numReceivers2 = r3.receivers != null ? r3.receivers.size() : 0;
            if (!this.mService.mProcessesReady || r3.timeoutExempt || r3.dispatchTime <= 0) {
                numReceivers = numReceivers2;
                r = r3;
            } else {
                if (r3.anrCount > 0) {
                    Slog.w("BroadcastQueue", "intentAction=" + r3.intent.getAction() + "dispatchTime=" + r3.dispatchTime + " numReceivers=" + numReceivers2 + " nextReceiver=" + r3.nextReceiver + " now=" + now + " state=" + r3.state + " curReceiver= " + r3.curReceiver);
                }
                if (numReceivers2 > 0 && now > r3.dispatchTime + (this.mConstants.TIMEOUT * 2 * ((long) numReceivers2))) {
                    Slog.w("BroadcastQueue", "Hung broadcast [" + this.mQueueName + "] discarded after timeout failure: now=" + now + " dispatchTime=" + r3.dispatchTime + " startTime=" + r3.receiverTime + " intent=" + r3.intent + " numReceivers=" + numReceivers2 + " nextReceiver=" + r3.nextReceiver + " state=" + r3.state);
                    broadcastTimeoutLocked(false);
                    forceReceive = true;
                    r3.state = 0;
                }
                if (r3.anrCount <= 0 || numReceivers2 != 0 || now <= r3.dispatchTime || r3.curReceiver == null) {
                    numReceivers = numReceivers2;
                    r = r3;
                } else if (this == r3.queue) {
                    Slog.w("BroadcastQueue", " dispatchTime=" + r3.dispatchTime + " numReceivers=" + numReceivers2 + " nextReceiver=" + r3.nextReceiver + " now=" + now + " state=" + r3.state + " curReceiver= " + r3.curReceiver + " finish curReceiver");
                    logBroadcastReceiverDiscardLocked(r3);
                    finishReceiverLocked(r3, r3.resultCode, r3.resultData, r3.resultExtras, r3.resultAbort, false);
                    scheduleBroadcastsLocked();
                    r3.state = 0;
                    return;
                } else {
                    numReceivers = numReceivers2;
                    r = r3;
                }
            }
            if (r.state == 0) {
                if (r.receivers == null || r.nextReceiver >= numReceivers || r.resultAbort || forceReceive) {
                    if (r.resultTo != null) {
                        if (r.splitToken != 0) {
                            int newCount = this.mSplitRefcounts.get(r.splitToken) - i;
                            if (newCount == 0) {
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                    Slog.i("BroadcastQueue", "Sending broadcast completion for split token " + r.splitToken + " : " + r.intent.getAction());
                                }
                                this.mSplitRefcounts.delete(r.splitToken);
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                    Slog.i("BroadcastQueue", "Result refcount now " + newCount + " for split token " + r.splitToken + " : " + r.intent.getAction() + " - not sending completion yet");
                                }
                                this.mSplitRefcounts.put(r.splitToken, newCount);
                                sendResult = false;
                                if (!sendResult) {
                                    try {
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            try {
                                                Slog.i("BroadcastQueue", "Finishing broadcast [" + this.mQueueName + "] " + r.intent.getAction() + " app=" + r.callerApp);
                                            } catch (RemoteException e3) {
                                                e2 = e3;
                                                i2 = 2;
                                                iIntentReceiver = iIntentReceiver2;
                                            }
                                        }
                                        i2 = 2;
                                        iIntentReceiver = iIntentReceiver2;
                                        try {
                                            performReceiveLocked(r.callerApp, r.resultTo, new Intent(r.intent), r.resultCode, r.resultData, r.resultExtras, false, false, r.userId);
                                            r.resultTo = iIntentReceiver;
                                        } catch (RemoteException e4) {
                                            e2 = e4;
                                        }
                                    } catch (RemoteException e5) {
                                        e2 = e5;
                                        i2 = 2;
                                        iIntentReceiver = iIntentReceiver2;
                                        r.resultTo = iIntentReceiver;
                                        Slog.w("BroadcastQueue", "Failure [" + this.mQueueName + "] sending broadcast result of " + r.intent, e2);
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        }
                                        cancelBroadcastTimeoutLocked();
                                        Slog.v("BroadcastQueue", "Finished with ordered broadcast " + r);
                                        addBroadcastToHistoryLocked(r);
                                        this.mService.addBroadcastStatLocked(r.intent.getAction(), r.callerPackage, r.manifestCount, r.manifestSkipCount, r.finishTime - r.dispatchTime);
                                        this.mDispatcher.retireBroadcastLocked(r);
                                        r = null;
                                        looped = true;
                                        if (r == null) {
                                        }
                                    }
                                } else {
                                    i2 = 2;
                                    iIntentReceiver = iIntentReceiver2;
                                }
                            }
                        }
                        sendResult = true;
                        if (!sendResult) {
                        }
                    } else {
                        i2 = 2;
                        iIntentReceiver = iIntentReceiver2;
                    }
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v("BroadcastQueue", "Cancelling BROADCAST_TIMEOUT_MSG");
                    }
                    cancelBroadcastTimeoutLocked();
                    Slog.v("BroadcastQueue", "Finished with ordered broadcast " + r);
                    addBroadcastToHistoryLocked(r);
                    if (r.intent.getComponent() == null && r.intent.getPackage() == null && (r.intent.getFlags() & 1073741824) == 0) {
                        this.mService.addBroadcastStatLocked(r.intent.getAction(), r.callerPackage, r.manifestCount, r.manifestSkipCount, r.finishTime - r.dispatchTime);
                    }
                    this.mDispatcher.retireBroadcastLocked(r);
                    r = null;
                    looped = true;
                } else {
                    if (!r.deferred) {
                        int receiverUid = r.getReceiverUid(r.receivers.get(r.nextReceiver));
                        if (this.mDispatcher.isDeferringLocked(receiverUid)) {
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                Slog.i("BroadcastQueue", "Next receiver in " + r + " uid " + receiverUid + " at " + r.nextReceiver + " is under deferral");
                            }
                            if (r.nextReceiver + i == numReceivers) {
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                    Slog.i("BroadcastQueue", "Sole receiver of " + r + " is under deferral; setting aside and proceeding");
                                }
                                defer = r;
                                this.mDispatcher.retireBroadcastLocked(r);
                            } else {
                                defer = r.splitRecipientsLocked(receiverUid, r.nextReceiver);
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                    Slog.i("BroadcastQueue", "Post split:");
                                    Slog.i("BroadcastQueue", "Original broadcast receivers:");
                                    for (int i4 = 0; i4 < r.receivers.size(); i4++) {
                                        Slog.i("BroadcastQueue", "  " + r.receivers.get(i4));
                                    }
                                    Slog.i("BroadcastQueue", "Split receivers:");
                                    for (int i5 = 0; i5 < defer.receivers.size(); i5++) {
                                        Slog.i("BroadcastQueue", "  " + defer.receivers.get(i5));
                                    }
                                }
                                if (r.resultTo != null) {
                                    int token = r.splitToken;
                                    if (token == 0) {
                                        int nextSplitTokenLocked = nextSplitTokenLocked();
                                        defer.splitToken = nextSplitTokenLocked;
                                        r.splitToken = nextSplitTokenLocked;
                                        this.mSplitRefcounts.put(r.splitToken, 2);
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                            Slog.i("BroadcastQueue", "Broadcast needs split refcount; using new token " + r.splitToken);
                                        }
                                    } else {
                                        int curCount = this.mSplitRefcounts.get(token);
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL && curCount == 0) {
                                            Slog.wtf("BroadcastQueue", "Split refcount is zero with token for " + r);
                                        }
                                        this.mSplitRefcounts.put(token, curCount + 1);
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                                            Slog.i("BroadcastQueue", "New split count for token " + token + " is " + (curCount + 1));
                                        }
                                    }
                                }
                            }
                            this.mDispatcher.addDeferredBroadcast(receiverUid, defer);
                            r = null;
                            looped = true;
                            i2 = 2;
                            iIntentReceiver = iIntentReceiver2;
                        }
                    }
                    i2 = 2;
                    iIntentReceiver = iIntentReceiver2;
                }
                if (r == null) {
                    int recIdx = r.nextReceiver;
                    r.nextReceiver = recIdx + 1;
                    Object target2 = r.receivers.get(recIdx);
                    if (enqueueProxyBroadcast(false, r, target2)) {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Flog.i(104, "orderd " + this.mQueueName + " broadcast:(" + r + ") should be proxyed, target:(" + target2 + ")");
                        }
                        scheduleBroadcastsLocked();
                        return;
                    }
                    reportMediaButtonToAware(r, target2);
                    r.receiverTime = SystemClock.uptimeMillis();
                    if (recIdx == 0) {
                        r.dispatchTime = r.receiverTime;
                        r.dispatchClockTime = System.currentTimeMillis();
                        long enqueuedMs = r.dispatchClockTime - r.enqueueClockTime;
                        if (enqueuedMs > 2000) {
                            Flog.i(104, "dispatch order bd [" + this.mQueueName + "] " + r + " eq:" + enqueuedMs + " rx:" + r.receivers.size());
                        }
                        if (this.mLogLatencyMetrics) {
                            StatsLog.write(142, r.dispatchClockTime - r.enqueueClockTime);
                        }
                        if (Trace.isTagEnabled(64)) {
                            Trace.asyncTraceEnd(64, createBroadcastTraceTitle(r, 0), System.identityHashCode(r));
                            Trace.asyncTraceBegin(64, createBroadcastTraceTitle(r, 1), System.identityHashCode(r));
                        }
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                            Slog.v("BroadcastQueue", "Processing ordered broadcast [" + this.mQueueName + "] " + r);
                        }
                    }
                    if (!this.mPendingBroadcastTimeoutMessage) {
                        long timeoutTime = r.receiverTime + this.mConstants.TIMEOUT;
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v("BroadcastQueue", "Submitting BROADCAST_TIMEOUT_MSG [" + this.mQueueName + "] for " + r + " at " + timeoutTime);
                        }
                        setBroadcastTimeoutLocked(timeoutTime);
                    }
                    BroadcastOptions brOptions = r.options;
                    Object nextReceiver = r.receivers.get(recIdx);
                    if (nextReceiver instanceof BroadcastFilter) {
                        BroadcastFilter filter = (BroadcastFilter) nextReceiver;
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v("BroadcastQueue", "Delivering ordered [" + this.mQueueName + "] to registered " + filter + ": " + r);
                        }
                        deliverToRegisteredReceiverLocked(r, filter, r.ordered, recIdx);
                        if (r.receiver == null || !r.ordered) {
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.v("BroadcastQueue", "Quick finishing [" + this.mQueueName + "]: ordered=" + r.ordered + " receiver=" + r.receiver);
                            }
                            r.state = 0;
                            scheduleBroadcastsLocked();
                            return;
                        }
                        if (filter.receiverList != null) {
                            maybeAddAllowBackgroundActivityStartsToken(filter.receiverList.app, r);
                        }
                        if (brOptions != null && brOptions.getTemporaryAppWhitelistDuration() > 0) {
                            scheduleTempWhitelistLocked(filter.owningUid, brOptions.getTemporaryAppWhitelistDuration(), r);
                            return;
                        }
                        return;
                    }
                    ResolveInfo info3 = (ResolveInfo) nextReceiver;
                    ComponentName component2 = new ComponentName(info3.activityInfo.applicationInfo.packageName, info3.activityInfo.name);
                    boolean skip6 = false;
                    if (brOptions != null && (info3.activityInfo.applicationInfo.targetSdkVersion < brOptions.getMinManifestReceiverApiLevel() || info3.activityInfo.applicationInfo.targetSdkVersion > brOptions.getMaxManifestReceiverApiLevel())) {
                        skip6 = true;
                    }
                    if (!skip6 && !this.mService.validateAssociationAllowedLocked(r.callerPackage, r.callingUid, component2.getPackageName(), info3.activityInfo.applicationInfo.uid)) {
                        Slog.w("BroadcastQueue", "Association not allowed: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") to " + component2.flattenToShortString());
                        skip6 = true;
                    }
                    if (!skip6 && (!this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, info3.activityInfo.applicationInfo.uid))) {
                        Slog.w("BroadcastQueue", "Firewall blocked: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") to " + component2.flattenToShortString());
                    }
                    ActivityManagerService activityManagerService = this.mService;
                    int i6 = ActivityManagerService.checkComponentPermission(info3.activityInfo.permission, r.callingPid, r.callingUid, info3.activityInfo.applicationInfo.uid, info3.activityInfo.exported);
                    int i7 = -1;
                    if (!skip6 && i6 != 0) {
                        if (!info3.activityInfo.exported) {
                            Slog.w("BroadcastQueue", "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") is not exported from uid " + info3.activityInfo.applicationInfo.uid + " due to receiver " + component2.flattenToShortString());
                        } else {
                            Slog.w("BroadcastQueue", "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") requires " + info3.activityInfo.permission + " due to receiver " + component2.flattenToShortString());
                        }
                        skip = true;
                    } else if (skip6 || info3.activityInfo.permission == null || (opCode = AppOpsManager.permissionToOpCode(info3.activityInfo.permission)) == -1 || this.mService.mAppOpsService.noteOperation(opCode, r.callingUid, r.callerPackage) == 0) {
                        skip = skip6;
                    } else {
                        Slog.w("BroadcastQueue", "Appop Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ") requires appop " + AppOpsManager.permissionToOp(info3.activityInfo.permission) + " due to registered receiver " + component2.flattenToShortString());
                        skip = true;
                    }
                    if (!skip && info3.activityInfo.applicationInfo.uid != 1000 && r.requiredPermissions != null && r.requiredPermissions.length > 0) {
                        int perm2 = i6;
                        int i8 = 0;
                        while (true) {
                            if (i8 >= r.requiredPermissions.length) {
                                i6 = perm2;
                                break;
                            }
                            String requiredPermission = r.requiredPermissions[i8];
                            try {
                                perm2 = AppGlobals.getPackageManager().checkPermission(requiredPermission, info3.activityInfo.applicationInfo.packageName, UserHandle.getUserId(info3.activityInfo.applicationInfo.uid));
                            } catch (RemoteException e6) {
                                perm2 = -1;
                            }
                            if (perm2 != 0) {
                                Slog.w("BroadcastQueue", "Permission Denial: receiving " + r.intent + " to " + component2.flattenToShortString() + " requires " + requiredPermission + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                skip = true;
                                i6 = perm2;
                                break;
                            }
                            int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                            if (!(appOp == i7 || appOp == r.appOp || this.mService.mAppOpsService.noteOperation(appOp, info3.activityInfo.applicationInfo.uid, info3.activityInfo.packageName) == 0)) {
                                Slog.w("BroadcastQueue", "Appop Denial: receiving " + r.intent + " to " + component2.flattenToShortString() + " requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                skip = true;
                                i6 = perm2;
                                break;
                            }
                            i8++;
                            i7 = -1;
                        }
                        if (r.intent != null && "android.provider.Telephony.SMS_RECEIVED".equals(r.intent.getAction())) {
                            HwSystemManager.insertSendBroadcastRecord(info3.activityInfo.applicationInfo.packageName, r.intent.getAction(), info3.activityInfo.applicationInfo.uid);
                        }
                    }
                    if (!(skip || r.appOp == -1 || this.mService.mAppOpsService.noteOperation(r.appOp, info3.activityInfo.applicationInfo.uid, info3.activityInfo.packageName) == 0)) {
                        Slog.w("BroadcastQueue", "Appop Denial: receiving " + r.intent + " to " + component2.flattenToShortString() + " requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                        skip = true;
                    }
                    try {
                        isSingleton = this.mService.isSingleton(info3.activityInfo.processName, info3.activityInfo.applicationInfo, info3.activityInfo.name, info3.activityInfo.flags);
                    } catch (SecurityException e7) {
                        Slog.w("BroadcastQueue", e7.getMessage());
                        skip = true;
                        isSingleton = false;
                    }
                    if (!((info3.activityInfo.flags & 1073741824) == 0 || ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS", info3.activityInfo.applicationInfo.uid) == 0)) {
                        Slog.w("BroadcastQueue", "Permission Denial: Receiver " + component2.flattenToShortString() + " requests FLAG_SINGLE_USER, but app does not hold android.permission.INTERACT_ACROSS_USERS");
                        skip = true;
                    }
                    if (!skip && info3.activityInfo.applicationInfo.isInstantApp() && r.callingUid != info3.activityInfo.applicationInfo.uid) {
                        Slog.w("BroadcastQueue", "Instant App Denial: receiving " + r.intent + " to " + component2.flattenToShortString() + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ") Instant Apps do not support manifest receivers");
                        skip = true;
                    }
                    if (!skip && r.callerInstantApp && (info3.activityInfo.flags & DumpState.DUMP_DEXOPT) == 0 && r.callingUid != info3.activityInfo.applicationInfo.uid) {
                        Slog.w("BroadcastQueue", "Instant App Denial: receiving " + r.intent + " to " + component2.flattenToShortString() + " requires receiver have visibleToInstantApps set due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                        skip = true;
                    }
                    if (r.curApp != null && r.curApp.isCrashing()) {
                        Slog.w("BroadcastQueue", "Skipping deliver ordered [" + this.mQueueName + "] " + r + " to " + r.curApp + ": process crashing");
                        skip = true;
                    }
                    if (!skip) {
                        boolean isAvailable = false;
                        try {
                            isAvailable = AppGlobals.getPackageManager().isPackageAvailable(info3.activityInfo.packageName, UserHandle.getUserId(info3.activityInfo.applicationInfo.uid));
                        } catch (Exception e8) {
                            Slog.w("BroadcastQueue", "Exception getting recipient info for " + info3.activityInfo.packageName, e8);
                        }
                        if (!isAvailable) {
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.v("BroadcastQueue", "Skipping delivery to " + info3.activityInfo.packageName + " / " + info3.activityInfo.applicationInfo.uid + " : package no longer available");
                            }
                            skip = true;
                        }
                    }
                    if (!skip && !requestStartTargetPermissionsReviewIfNeededLocked(r, info3.activityInfo.packageName, UserHandle.getUserId(info3.activityInfo.applicationInfo.uid))) {
                        skip = true;
                    }
                    int receiverUid2 = info3.activityInfo.applicationInfo.uid;
                    if (r.callingUid != 1000 && isSingleton && this.mService.isValidSingletonCall(r.callingUid, receiverUid2)) {
                        info3.activityInfo = this.mService.getActivityInfoForUser(info3.activityInfo, 0);
                    }
                    String targetProcess3 = info3.activityInfo.processName;
                    ProcessRecord app2 = this.mService.getProcessRecordLocked(targetProcess3, info3.activityInfo.applicationInfo.uid, false);
                    if (!skip) {
                        app = app2;
                        skip5 = skip;
                        int allowed = this.mService.getAppStartModeLocked(info3.activityInfo.applicationInfo.uid, info3.activityInfo.packageName, info3.activityInfo.applicationInfo.targetSdkVersion, -1, true, false, false);
                        if (allowed != 0) {
                            if (allowed == 3) {
                                Slog.w("BroadcastQueue", "Background execution disabled: receiving " + r.intent + " to " + component2.flattenToShortString());
                                skip2 = true;
                            } else if ((r.intent.getFlags() & DumpState.DUMP_VOLUMES) != 0 || (r.intent.getComponent() == null && r.intent.getPackage() == null && (r.intent.getFlags() & DumpState.DUMP_SERVICE_PERMISSIONS) == 0 && !this.mService.isExcludedInBGCheck(component2.getPackageName(), null) && !isSignaturePerm(r.requiredPermissions))) {
                                this.mService.addBackgroundCheckViolationLocked(r.intent.getAction(), component2.getPackageName());
                                Slog.w("BroadcastQueue", "Background execution not allowed: receiving " + r.intent + " to " + component2.flattenToShortString());
                                skip2 = true;
                            }
                            if (!skip2 || "android.intent.action.ACTION_SHUTDOWN".equals(r.intent.getAction()) || this.mService.mUserController.isUserRunning(UserHandle.getUserId(info3.activityInfo.applicationInfo.uid), 0)) {
                                skip3 = skip2;
                            } else {
                                Slog.w("BroadcastQueue", "Skipping delivery to " + info3.activityInfo.packageName + " / " + info3.activityInfo.applicationInfo.uid + " : user is not running");
                                skip3 = true;
                            }
                            if (skip3) {
                                targetProcess = targetProcess3;
                                perm = receiverUid2;
                                component = component2;
                                if (this.mService.mHwAMSEx.shouldPreventSendBroadcast(r, info3, app, info3.activityInfo.packageName, true)) {
                                    skip4 = true;
                                    if (skip4) {
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v("BroadcastQueue", "Skipping delivery of ordered [" + this.mQueueName + "] " + r + " for reason described above");
                                        }
                                        r.delivery[recIdx] = i2;
                                        r.receiver = null;
                                        r.curFilter = null;
                                        r.state = 0;
                                        r.manifestSkipCount++;
                                        scheduleBroadcastsLocked();
                                        return;
                                    }
                                    r.manifestCount++;
                                    r.delivery[recIdx] = 1;
                                    r.state = 1;
                                    r.curComponent = component;
                                    r.curReceiver = info3.activityInfo;
                                    if (ActivityManagerDebugConfig.DEBUG_MU && r.callingUid > 100000) {
                                        Slog.v(TAG_MU, "Updated broadcast record activity info for secondary user, " + info3.activityInfo + ", callingUid = " + r.callingUid + ", uid = " + perm);
                                    }
                                    if (brOptions != null && brOptions.getTemporaryAppWhitelistDuration() > 0) {
                                        scheduleTempWhitelistLocked(perm, brOptions.getTemporaryAppWhitelistDuration(), r);
                                    }
                                    try {
                                        AppGlobals.getPackageManager().setPackageStoppedState(r.curComponent.getPackageName(), false, r.userId);
                                    } catch (RemoteException e9) {
                                    } catch (IllegalArgumentException e10) {
                                        Slog.w("BroadcastQueue", "Failed trying to unstop package " + r.curComponent.getPackageName() + ": " + e10);
                                    }
                                    if (app == null || app.thread == null || app.killed) {
                                        info = info3;
                                    } else {
                                        try {
                                            app.addPackage(info3.activityInfo.packageName, info3.activityInfo.applicationInfo.longVersionCode, this.mService.mProcessStats);
                                            maybeAddAllowBackgroundActivityStartsToken(app, r);
                                            try {
                                                processCurBroadcastLocked(r, app, skipOomAdj);
                                                return;
                                            } catch (RemoteException e11) {
                                                e = e11;
                                                info = info3;
                                                Slog.w("BroadcastQueue", "Exception when sending broadcast to " + r.curComponent, e);
                                                if (this.mService.mUserController.getCurrentUserId() != 0) {
                                                }
                                                if (!ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                                }
                                                if (!getMtmBRManagerEnabled(11)) {
                                                }
                                                startProcessLocked = this.mService.startProcessLocked(targetProcess2, info2.activityInfo.applicationInfo, true, r.intent.getFlags() | 4, new HostingRecord(INetd.IF_FLAG_BROADCAST, r.curComponent), (r.intent.getFlags() & DumpState.DUMP_APEX) == 0 ? z : false, false, false);
                                                r.curApp = startProcessLocked;
                                                if (startProcessLocked != null) {
                                                }
                                            } catch (RuntimeException e12) {
                                                RuntimeException e13 = e12;
                                                Slog.wtf("BroadcastQueue", "Failed sending broadcast to " + r.curComponent + " with " + r.intent, e13);
                                                logBroadcastReceiverDiscardLocked(r);
                                                finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                                scheduleBroadcastsLocked();
                                                r.state = 0;
                                                return;
                                            }
                                        } catch (RemoteException e14) {
                                            e = e14;
                                            info = info3;
                                            Slog.w("BroadcastQueue", "Exception when sending broadcast to " + r.curComponent, e);
                                            if (this.mService.mUserController.getCurrentUserId() != 0) {
                                            }
                                            if (!ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            }
                                            if (!getMtmBRManagerEnabled(11)) {
                                            }
                                            startProcessLocked = this.mService.startProcessLocked(targetProcess2, info2.activityInfo.applicationInfo, true, r.intent.getFlags() | 4, new HostingRecord(INetd.IF_FLAG_BROADCAST, r.curComponent), (r.intent.getFlags() & DumpState.DUMP_APEX) == 0 ? z : false, false, false);
                                            r.curApp = startProcessLocked;
                                            if (startProcessLocked != null) {
                                            }
                                        } catch (RuntimeException e15) {
                                        }
                                    }
                                    if (this.mService.mUserController.getCurrentUserId() != 0) {
                                        info2 = info;
                                        try {
                                            String packageName = info2.activityInfo.applicationInfo.packageName;
                                            String sourceDir = info2.activityInfo.applicationInfo.sourceDir;
                                            String publicSourceDir = info2.activityInfo.applicationInfo.publicSourceDir;
                                            ApplicationInfo applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, UserHandle.getUserId(info2.activityInfo.applicationInfo.uid));
                                            if (applicationInfo == null) {
                                                return;
                                            }
                                            if (!applicationInfo.sourceDir.equals(sourceDir) || !applicationInfo.publicSourceDir.equals(publicSourceDir)) {
                                                Slog.e("BroadcastQueue", packageName + " is replaced, sourceDir is changed from " + sourceDir + " to " + applicationInfo.sourceDir + ", publicSourceDir is changed from " + publicSourceDir + " to " + applicationInfo.publicSourceDir);
                                                info2.activityInfo.applicationInfo = applicationInfo;
                                            }
                                        } catch (RemoteException e16) {
                                            Slog.e("BroadcastQueue", "error get appInfo for " + info2.activityInfo.applicationInfo.packageName);
                                        }
                                    } else {
                                        info2 = info;
                                    }
                                    if (!ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("Need to start app [");
                                        sb.append(this.mQueueName);
                                        sb.append("] ");
                                        targetProcess2 = targetProcess;
                                        sb.append(targetProcess2);
                                        sb.append(" for broadcast ");
                                        sb.append(r);
                                        Slog.v("BroadcastQueue", sb.toString());
                                    } else {
                                        targetProcess2 = targetProcess;
                                    }
                                    if (!getMtmBRManagerEnabled(11)) {
                                        z = true;
                                        if (getMtmBRManager().iawareProcessBroadcast(1, false, new BroadcastRecordEx(r), target2)) {
                                            return;
                                        }
                                    } else {
                                        z = true;
                                    }
                                    startProcessLocked = this.mService.startProcessLocked(targetProcess2, info2.activityInfo.applicationInfo, true, r.intent.getFlags() | 4, new HostingRecord(INetd.IF_FLAG_BROADCAST, r.curComponent), (r.intent.getFlags() & DumpState.DUMP_APEX) == 0 ? z : false, false, false);
                                    r.curApp = startProcessLocked;
                                    if (startProcessLocked != null) {
                                        Slog.w("BroadcastQueue", "Unable to launch app " + info2.activityInfo.applicationInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + perm + " for broadcast " + r.intent + ": process is bad");
                                        logBroadcastReceiverDiscardLocked(r);
                                        finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                        scheduleBroadcastsLocked();
                                        r.state = 0;
                                        return;
                                    }
                                    maybeAddAllowBackgroundActivityStartsToken(r.curApp, r);
                                    this.mPendingBroadcast = r;
                                    this.mPendingBroadcastRecvIndex = recIdx;
                                    return;
                                }
                            } else {
                                targetProcess = targetProcess3;
                                perm = receiverUid2;
                                component = component2;
                            }
                            skip4 = skip3;
                            if (skip4) {
                            }
                        }
                    } else {
                        app = app2;
                        skip5 = skip;
                    }
                    skip2 = skip5;
                    if (!skip2) {
                    }
                    skip3 = skip2;
                    if (skip3) {
                    }
                    skip4 = skip3;
                    if (skip4) {
                    }
                } else {
                    iIntentReceiver2 = iIntentReceiver;
                    i = 1;
                }
            } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.d("BroadcastQueue", "processNextBroadcast(" + this.mQueueName + ") called when not idle (state=" + r.state + ")");
                return;
            } else {
                return;
            }
        }
    }

    private void maybeAddAllowBackgroundActivityStartsToken(ProcessRecord proc, BroadcastRecord r) {
        if (r != null && proc != null && r.allowBackgroundActivityStarts) {
            this.mHandler.removeCallbacksAndMessages((proc.toShortString() + r.toString()).intern());
            proc.addAllowBackgroundActivityStartsToken(r);
        }
    }

    /* access modifiers changed from: package-private */
    public final void setBroadcastTimeoutLocked(long timeoutTime) {
        if (!this.mPendingBroadcastTimeoutMessage) {
            this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(BROADCAST_TIMEOUT_MSG, this), timeoutTime);
            Message msg = this.mHandler.obtainMessage(BROADCAST_CHECKTIMEOUT_MSG, this);
            if ("background".equals(this.mQueueName) || "bgthirdapp".equals(this.mQueueName) || "bgkeyapp".equals(this.mQueueName)) {
                this.mHandler.sendMessageDelayed(msg, 5000);
            } else {
                this.mHandler.sendMessageDelayed(msg, 2000);
            }
            this.mPendingBroadcastTimeoutMessage = true;
        }
    }

    /* access modifiers changed from: package-private */
    public final void cancelBroadcastTimeoutLocked() {
        if (this.mPendingBroadcastTimeoutMessage) {
            this.mHandler.removeMessages(BROADCAST_TIMEOUT_MSG, this);
            this.mHandler.removeMessages(BROADCAST_CHECKTIMEOUT_MSG, this);
            this.mPendingBroadcastTimeoutMessage = false;
        }
    }

    /* access modifiers changed from: package-private */
    public final void broadcastTimeoutLocked(boolean fromMsg) {
        Object curReceiver;
        ProcessRecord app;
        String anrMessage;
        boolean debugging = false;
        if (fromMsg) {
            this.mPendingBroadcastTimeoutMessage = false;
        }
        if (!this.mDispatcher.isEmpty() && this.mDispatcher.getActiveBroadcastLocked() != null) {
            long now = SystemClock.uptimeMillis();
            BroadcastRecord r = this.mDispatcher.getActiveBroadcastLocked();
            if (fromMsg) {
                if (this.mService.mProcessesReady) {
                    if (!r.timeoutExempt) {
                        long timeoutTime = r.receiverTime + this.mConstants.TIMEOUT;
                        if (timeoutTime > now) {
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.v("BroadcastQueue", "Premature timeout [" + this.mQueueName + "] @ " + now + ": resetting BROADCAST_TIMEOUT_MSG for " + timeoutTime);
                            }
                            setBroadcastTimeoutLocked(timeoutTime);
                            return;
                        }
                    } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.i("BroadcastQueue", "Broadcast timeout but it's exempt: " + r.intent.getAction());
                        return;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            if (r.state == 4) {
                StringBuilder sb = new StringBuilder();
                sb.append("Waited long enough for: ");
                sb.append(r.curComponent != null ? r.curComponent.flattenToShortString() : "(null)");
                Slog.i("BroadcastQueue", sb.toString());
                r.curComponent = null;
                r.state = 0;
                processNextBroadcast(false);
                return;
            }
            if (r.curApp != null && r.curApp.isDebugging()) {
                debugging = true;
            }
            Slog.w("BroadcastQueue", "Timeout of broadcast " + r + " - receiver=" + r.receiver + ", started " + (now - r.receiverTime) + "ms ago");
            r.receiverTime = now;
            if (!debugging) {
                r.anrCount++;
            }
            ProcessRecord app2 = null;
            if (r.nextReceiver > 0) {
                Object curReceiver2 = r.receivers.get(r.nextReceiver - 1);
                r.delivery[r.nextReceiver - 1] = 3;
                curReceiver = curReceiver2;
            } else {
                curReceiver = r.curReceiver;
            }
            Slog.w("BroadcastQueue", "Receiver during timeout of " + r + " : " + curReceiver);
            logBroadcastReceiverDiscardLocked(r);
            if (curReceiver == null || !(curReceiver instanceof BroadcastFilter)) {
                app = r.curApp;
            } else {
                BroadcastFilter bf = (BroadcastFilter) curReceiver;
                if (!(bf.receiverList.pid == 0 || bf.receiverList.pid == ActivityManagerService.MY_PID)) {
                    synchronized (this.mService.mPidsSelfLocked) {
                        app2 = this.mService.mPidsSelfLocked.get(bf.receiverList.pid);
                    }
                }
                app = app2;
            }
            if (app != null) {
                anrMessage = "Broadcast of " + r.intent.toString();
            } else {
                anrMessage = null;
            }
            if (this.mPendingBroadcast == r) {
                this.mPendingBroadcast = null;
            }
            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
            scheduleBroadcastsLocked();
            if (!debugging && anrMessage != null) {
                this.mHandler.post(new AppNotResponding(app, anrMessage));
            }
        }
    }

    private final int ringAdvance(int x, int increment, int ringSize) {
        int x2 = x + increment;
        if (x2 < 0) {
            return ringSize - 1;
        }
        if (x2 >= ringSize) {
            return 0;
        }
        return x2;
    }

    private final void addBroadcastToHistoryLocked(BroadcastRecord original) {
        if (original.callingUid >= 0) {
            original.finishTime = SystemClock.uptimeMillis();
            if (Trace.isTagEnabled(64)) {
                Trace.asyncTraceEnd(64, createBroadcastTraceTitle(original, 1), System.identityHashCode(original));
            }
            BroadcastRecord historyRecord = original.maybeStripForHistory();
            BroadcastRecord[] broadcastRecordArr = this.mBroadcastHistory;
            int i = this.mHistoryNext;
            broadcastRecordArr[i] = historyRecord;
            this.mHistoryNext = ringAdvance(i, 1, MAX_BROADCAST_HISTORY);
            this.mBroadcastSummaryHistory[this.mSummaryHistoryNext] = historyRecord.intent;
            this.mSummaryHistoryEnqueueTime[this.mSummaryHistoryNext] = historyRecord.enqueueClockTime;
            this.mSummaryHistoryDispatchTime[this.mSummaryHistoryNext] = historyRecord.dispatchClockTime;
            this.mSummaryHistoryFinishTime[this.mSummaryHistoryNext] = System.currentTimeMillis();
            this.mSummaryHistoryNext = ringAdvance(this.mSummaryHistoryNext, 1, MAX_BROADCAST_SUMMARY_HISTORY);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean cleanupDisabledPackageReceiversLocked(String packageName, Set<String> filterByClasses, int userId, boolean doit) {
        boolean didSomething = false;
        for (int i = this.mParallelBroadcasts.size() - 1; i >= 0; i--) {
            didSomething |= this.mParallelBroadcasts.get(i).cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
            if (!doit && didSomething) {
                return true;
            }
        }
        return didSomething | this.mDispatcher.cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
    }

    /* access modifiers changed from: package-private */
    public final void logBroadcastReceiverDiscardLocked(BroadcastRecord r) {
        int logIndex = r.nextReceiver - 1;
        if (logIndex < 0 || logIndex >= r.receivers.size()) {
            if (logIndex < 0) {
                Slog.w("BroadcastQueue", "Discarding broadcast before first receiver is invoked: " + r);
            }
            EventLog.writeEvent((int) EventLogTags.AM_BROADCAST_DISCARD_APP, -1, Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(r.nextReceiver), "NONE");
            return;
        }
        Object curReceiver = r.receivers.get(logIndex);
        if (curReceiver instanceof BroadcastFilter) {
            BroadcastFilter bf = (BroadcastFilter) curReceiver;
            EventLog.writeEvent((int) EventLogTags.AM_BROADCAST_DISCARD_FILTER, Integer.valueOf(bf.owningUserId), Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(logIndex), Integer.valueOf(System.identityHashCode(bf)));
            return;
        }
        ResolveInfo ri = (ResolveInfo) curReceiver;
        EventLog.writeEvent((int) EventLogTags.AM_BROADCAST_DISCARD_APP, Integer.valueOf(UserHandle.getUserId(ri.activityInfo.applicationInfo.uid)), Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(logIndex), ri.toString());
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000c: APUT  (r0v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r1v0 java.lang.String) */
    private String createBroadcastTraceTitle(BroadcastRecord record, int state) {
        Object[] objArr = new Object[4];
        objArr[0] = state == 0 ? "in queue" : "dispatched";
        String str = "";
        objArr[1] = record.callerPackage == null ? str : record.callerPackage;
        objArr[2] = record.callerApp == null ? "process unknown" : record.callerApp.toShortString();
        if (record.intent != null) {
            str = record.intent.getAction();
        }
        objArr[3] = str;
        return String.format("Broadcast %s from %s (%s) %s", objArr);
    }

    /* access modifiers changed from: package-private */
    public boolean isIdle() {
        return this.mParallelBroadcasts.isEmpty() && this.mDispatcher.isEmpty() && this.mPendingBroadcast == null;
    }

    /* access modifiers changed from: package-private */
    public void cancelDeferrals() {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mDispatcher.cancelDeferralsLocked();
                scheduleBroadcastsLocked();
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String describeState() {
        String str;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                str = this.mParallelBroadcasts.size() + " parallel; " + this.mDispatcher.describeStateLocked();
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        int i;
        int lastIndex;
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.mQueueName);
        for (int i2 = this.mParallelBroadcasts.size() - 1; i2 >= 0; i2--) {
            this.mParallelBroadcasts.get(i2).writeToProto(proto, 2246267895810L);
        }
        this.mDispatcher.writeToProto(proto, 2246267895811L);
        BroadcastRecord broadcastRecord = this.mPendingBroadcast;
        if (broadcastRecord != null) {
            broadcastRecord.writeToProto(proto, 1146756268036L);
        }
        int lastIndex2 = this.mHistoryNext;
        int ringIndex = lastIndex2;
        do {
            i = -1;
            ringIndex = ringAdvance(ringIndex, -1, MAX_BROADCAST_HISTORY);
            BroadcastRecord r = this.mBroadcastHistory[ringIndex];
            if (r != null) {
                r.writeToProto(proto, 2246267895813L);
                continue;
            }
        } while (ringIndex != lastIndex2);
        int i3 = this.mSummaryHistoryNext;
        int ringIndex2 = i3;
        int lastIndex3 = i3;
        while (true) {
            int ringIndex3 = ringAdvance(ringIndex2, i, MAX_BROADCAST_SUMMARY_HISTORY);
            Intent intent = this.mBroadcastSummaryHistory[ringIndex3];
            if (intent == null) {
                lastIndex = lastIndex3;
            } else {
                long summaryToken = proto.start(2246267895814L);
                lastIndex = lastIndex3;
                intent.writeToProto(proto, 1146756268033L, false, true, true, false);
                proto.write(1112396529666L, this.mSummaryHistoryEnqueueTime[ringIndex3]);
                proto.write(1112396529667L, this.mSummaryHistoryDispatchTime[ringIndex3]);
                proto.write(1112396529668L, this.mSummaryHistoryFinishTime[ringIndex3]);
                proto.end(summaryToken);
            }
            if (ringIndex3 == lastIndex) {
                proto.end(token);
                return;
            }
            lastIndex3 = lastIndex;
            ringIndex2 = ringIndex3;
            i = -1;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage, boolean needSep) {
        boolean needSep2;
        String str;
        int lastIndex;
        boolean printed;
        BroadcastRecord broadcastRecord;
        String str2 = dumpPackage;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String str3 = ":";
        if (!this.mParallelBroadcasts.isEmpty() || !this.mDispatcher.isEmpty() || this.mPendingBroadcast != null) {
            boolean printed2 = false;
            boolean needSep3 = needSep;
            for (int i = this.mParallelBroadcasts.size() - 1; i >= 0; i--) {
                BroadcastRecord br = this.mParallelBroadcasts.get(i);
                if (str2 == null || str2.equals(br.callerPackage)) {
                    if (!printed2) {
                        if (needSep3) {
                            pw.println();
                        }
                        needSep3 = true;
                        printed2 = true;
                        pw.println("  Active broadcasts [" + this.mQueueName + "]:");
                    }
                    pw.println("  Active Broadcast " + this.mQueueName + " #" + i + str3);
                    br.dump(pw, "    ", sdf);
                }
            }
            this.mDispatcher.dumpLocked(pw, str2, this.mQueueName, sdf);
            if (str2 == null || ((broadcastRecord = this.mPendingBroadcast) != null && str2.equals(broadcastRecord.callerPackage))) {
                pw.println();
                pw.println("  Pending broadcast [" + this.mQueueName + "]:");
                BroadcastRecord broadcastRecord2 = this.mPendingBroadcast;
                if (broadcastRecord2 != null) {
                    broadcastRecord2.dump(pw, "    ", sdf);
                } else {
                    pw.println("    (null)");
                }
                needSep2 = true;
            } else {
                needSep2 = needSep3;
            }
        } else {
            needSep2 = needSep;
        }
        this.mConstants.dump(pw);
        boolean printed3 = false;
        int i2 = -1;
        int lastIndex2 = this.mHistoryNext;
        int ringIndex = lastIndex2;
        while (true) {
            int ringIndex2 = ringAdvance(ringIndex, -1, MAX_BROADCAST_HISTORY);
            BroadcastRecord r = this.mBroadcastHistory[ringIndex2];
            if (r == null) {
                str = str3;
            } else {
                i2++;
                if (str2 == null || str2.equals(r.callerPackage)) {
                    if (!printed3) {
                        if (needSep2) {
                            pw.println();
                        }
                        needSep2 = true;
                        pw.println("  Historical broadcasts [" + this.mQueueName + "]:");
                        printed = true;
                    } else {
                        printed = printed3;
                    }
                    if (dumpAll) {
                        pw.print("  Historical Broadcast " + this.mQueueName + " #");
                        pw.print(i2);
                        pw.println(str3);
                        r.dump(pw, "    ", sdf);
                        str = str3;
                    } else {
                        pw.print("  #");
                        pw.print(i2);
                        pw.print(": ");
                        pw.println(r);
                        pw.print("    ");
                        str = str3;
                        pw.println(r.intent.toShortString(true, true, true, false));
                        if (!(r.targetComp == null || r.targetComp == r.intent.getComponent())) {
                            pw.print("    targetComp: ");
                            pw.println(r.targetComp.toShortString());
                        }
                        Bundle bundle = r.intent.getExtras();
                        if (bundle != null && !isFromEmailMdm(r.intent)) {
                            pw.print("    extras: ");
                            pw.println(bundle.toString());
                        }
                    }
                    printed3 = printed;
                } else {
                    str = str3;
                }
            }
            ringIndex = ringIndex2;
            if (ringIndex == lastIndex2) {
                break;
            }
            str2 = dumpPackage;
            lastIndex2 = lastIndex2;
            str3 = str;
        }
        if (str2 == null) {
            int lastIndex3 = this.mSummaryHistoryNext;
            int ringIndex3 = lastIndex3;
            if (dumpAll) {
                printed3 = false;
                i2 = -1;
            } else {
                int j = i2;
                while (j > 0 && ringIndex3 != lastIndex3) {
                    ringIndex3 = ringAdvance(ringIndex3, -1, MAX_BROADCAST_SUMMARY_HISTORY);
                    if (this.mBroadcastHistory[ringIndex3] != null) {
                        j--;
                    }
                }
            }
            while (true) {
                ringIndex3 = ringAdvance(ringIndex3, -1, MAX_BROADCAST_SUMMARY_HISTORY);
                Intent intent = this.mBroadcastSummaryHistory[ringIndex3];
                if (intent != null) {
                    if (!printed3) {
                        if (needSep2) {
                            pw.println();
                        }
                        pw.println("  Historical broadcasts summary [" + this.mQueueName + "]:");
                        printed3 = true;
                        needSep2 = true;
                    }
                    if (!dumpAll && i2 >= 50) {
                        pw.println("  ...");
                        break;
                    }
                    i2++;
                    pw.print("  #");
                    pw.print(i2);
                    pw.print(": ");
                    pw.println(intent.toShortString(false, true, true, false));
                    pw.print("    ");
                    lastIndex = lastIndex3;
                    TimeUtils.formatDuration(this.mSummaryHistoryDispatchTime[ringIndex3] - this.mSummaryHistoryEnqueueTime[ringIndex3], pw);
                    pw.print(" dispatch ");
                    TimeUtils.formatDuration(this.mSummaryHistoryFinishTime[ringIndex3] - this.mSummaryHistoryDispatchTime[ringIndex3], pw);
                    pw.println(" finish");
                    pw.print("    enq=");
                    pw.print(sdf.format(new Date(this.mSummaryHistoryEnqueueTime[ringIndex3])));
                    pw.print(" disp=");
                    pw.print(sdf.format(new Date(this.mSummaryHistoryDispatchTime[ringIndex3])));
                    pw.print(" fin=");
                    pw.println(sdf.format(new Date(this.mSummaryHistoryFinishTime[ringIndex3])));
                    Bundle bundle2 = intent.getExtras();
                    if (bundle2 != null && !"android.intent.action.PHONE_STATE".equals(intent.getAction()) && !"android.intent.action.NEW_OUTGOING_CALL".equals(intent.getAction()) && !isFromEmailMdm(intent)) {
                        pw.print("    extras: ");
                        pw.println(bundle2.toString());
                    }
                    printed3 = printed3;
                } else {
                    lastIndex = lastIndex3;
                }
                if (ringIndex3 == lastIndex) {
                    break;
                }
                lastIndex3 = lastIndex;
            }
        }
        return needSep2;
    }

    private boolean isFromEmailMdm(Intent intent) {
        if (intent == null) {
            return false;
        }
        return "com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction());
    }
}
