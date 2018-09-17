package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.hsm.HwSystemManager;
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
import android.util.TimeUtils;
import com.android.server.HwServiceFactory;
import com.android.server.display.DisplayTransformManager;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.wm.WindowState;
import com.huawei.pgmng.common.Utils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class BroadcastQueue extends AbsBroadcastQueue {
    static final int BROADCAST_CHECKTIMEOUT_MSG = 203;
    static final int BROADCAST_INTENT_MSG = 200;
    static final int BROADCAST_TIMEOUT_MSG = 201;
    static final int CHECK_INTERVAL = ((int) (ActivityManagerService.SCALE_ANR * 10000.0f));
    static final int MAX_BROADCAST_HISTORY;
    static final int MAX_BROADCAST_SUMMARY_HISTORY;
    static final int MAYBE_BROADCAST_BG_TIMEOUT = 5000;
    static final int MAYBE_BROADCAST_FG_TIMEOUT = 2000;
    static final int SCHEDULE_TEMP_WHITELIST_MSG = 202;
    private static final String TAG = "BroadcastQueue";
    private static final String TAG_BROADCAST = (TAG + ActivityManagerDebugConfig.POSTFIX_BROADCAST);
    private static final String TAG_MU = "BroadcastQueue_MU";
    private static final boolean mIsBetaUser;
    final BroadcastRecord[] mBroadcastHistory = new BroadcastRecord[MAX_BROADCAST_HISTORY];
    final Intent[] mBroadcastSummaryHistory = new Intent[MAX_BROADCAST_SUMMARY_HISTORY];
    boolean mBroadcastsScheduled = false;
    final boolean mDelayBehindServices;
    final BroadcastHandler mHandler;
    int mHistoryNext = 0;
    final ArrayList<BroadcastRecord> mOrderedBroadcasts = new ArrayList();
    final ArrayList<BroadcastRecord> mParallelBroadcasts = new ArrayList();
    BroadcastRecord mPendingBroadcast = null;
    int mPendingBroadcastRecvIndex;
    boolean mPendingBroadcastTimeoutMessage;
    final String mQueueName;
    final ActivityManagerService mService;
    final long[] mSummaryHistoryDispatchTime = new long[MAX_BROADCAST_SUMMARY_HISTORY];
    final long[] mSummaryHistoryEnqueueTime = new long[MAX_BROADCAST_SUMMARY_HISTORY];
    final long[] mSummaryHistoryFinishTime = new long[MAX_BROADCAST_SUMMARY_HISTORY];
    int mSummaryHistoryNext = 0;
    final long mTimeoutPeriod;

    private final class AppNotResponding implements Runnable {
        private static final String GET_FOCUSED_WINDOW_METHOD_NAME = "getFocusedWindow";
        private static final String WINDOW_MANAGER_SERVICE_CLASS_NAME = "com.android.server.wm.WindowManagerService";
        private final String mAnnotation;
        private final ProcessRecord mApp;

        public AppNotResponding(ProcessRecord app, String annotation) {
            this.mApp = app;
            this.mAnnotation = annotation;
        }

        public void run() {
            BroadcastQueue.this.mService.mAppErrors.appNotResponding(this.mApp, null, null, isAboveSystem(), this.mAnnotation);
        }

        private boolean isAboveSystem() {
            WindowState focusedWindow = getCurFocusedWindow();
            if (focusedWindow != null && windowTypeToLayerLw(focusedWindow.mAttrs.type, focusedWindow.canAddInternalSystemWindow()) < windowTypeToLayerLw(2003, true)) {
                return false;
            }
            return true;
        }

        private int windowTypeToLayerLw(int type, boolean canAddInternalSystemWindow) {
            return HwPolicyFactory.getHwPhoneWindowManager().getWindowLayerFromTypeLw(type, canAddInternalSystemWindow);
        }

        private WindowState getCurFocusedWindow() {
            try {
                Class<?> cls = Class.forName(WINDOW_MANAGER_SERVICE_CLASS_NAME);
                if (cls != null) {
                    Method method = cls.getDeclaredMethod(GET_FOCUSED_WINDOW_METHOD_NAME, new Class[0]);
                    if (method != null) {
                        method.setAccessible(true);
                        return (WindowState) method.invoke(BroadcastQueue.this.mService.mWindowManager, new Object[0]);
                    }
                }
            } catch (Exception e) {
                Slog.e(BroadcastQueue.TAG, "BroadcastQueue AppNotResponding getCurFocusedWindow failed", e);
            }
            return null;
        }
    }

    private final class BroadcastHandler extends Handler {
        public BroadcastHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(BroadcastQueue.TAG_BROADCAST, "Received BROADCAST_INTENT_MSG");
                    }
                    BroadcastQueue.this.processNextBroadcast(true);
                    return;
                case BroadcastQueue.BROADCAST_TIMEOUT_MSG /*201*/:
                    synchronized (BroadcastQueue.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            BroadcastQueue.this.broadcastTimeoutLocked(true);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                case BroadcastQueue.BROADCAST_CHECKTIMEOUT_MSG /*203*/:
                    synchronized (BroadcastQueue.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            BroadcastQueue.this.handleMaybeTimeoutBC();
                            BroadcastQueue.this.uploadRadarMessage(2803, null);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    static {
        boolean z;
        int i = (ActivityManager.isLowRamDeviceStatic() || SystemProperties.getBoolean("ro.config.hw_low_ram", false)) ? 10 : 50;
        MAX_BROADCAST_HISTORY = i;
        i = (ActivityManager.isLowRamDeviceStatic() || SystemProperties.getBoolean("ro.config.hw_low_ram", false)) ? 25 : DisplayTransformManager.LEVEL_COLOR_MATRIX_INVERT_COLOR;
        MAX_BROADCAST_SUMMARY_HISTORY = i;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3) {
            z = true;
        } else {
            z = false;
        }
        mIsBetaUser = z;
    }

    private void handleMaybeTimeoutBC() {
        if (this.mOrderedBroadcasts.size() == 0) {
            Slog.w(TAG, "handleMaybeTimeoutBC, but mOrderedBroadcasts is null");
            return;
        }
        BroadcastRecord r = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
        if (r.nextReceiver <= 0) {
            Slog.w(TAG, "handleMaybeTimeoutBC Timeout on receiver with nextReceiver <= 0");
            return;
        }
        String pkg = null;
        String pid = null;
        String target = null;
        BroadcastFilter curReceiver = r.receivers.get(r.nextReceiver - 1);
        if (curReceiver instanceof BroadcastFilter) {
            BroadcastFilter bf = curReceiver;
            pkg = bf.packageName;
            if (bf.receiverList != null) {
                int iPid = bf.receiverList.pid;
                if (iPid <= 0 && bf.receiverList.app != null) {
                    iPid = bf.receiverList.app.pid;
                }
                pid = String.valueOf(iPid);
            }
            target = "PackageName:" + pkg;
        } else if (curReceiver instanceof ResolveInfo) {
            ResolveInfo info = (ResolveInfo) curReceiver;
            if (info.activityInfo != null) {
                pkg = info.activityInfo.applicationInfo.packageName;
                StringBuilder sb = new StringBuilder(128);
                sb.append("ReceiverName:");
                ComponentName.appendShortString(sb, pkg, info.activityInfo.name);
                target = sb.toString();
            }
        }
        Utils.handleTimeOut("broadcast", pkg, pid);
        this.mService.checkOrderedBroadcastTimeoutLocked(target + "+ActionName:" + r.intent.getAction(), 0, false);
    }

    BroadcastQueue(ActivityManagerService service, Handler handler, String name, long timeoutPeriod, boolean allowDelayBehindServices) {
        this.mService = service;
        this.mHandler = new BroadcastHandler(handler.getLooper());
        this.mQueueName = name;
        this.mTimeoutPeriod = timeoutPeriod;
        this.mDelayBehindServices = allowDelayBehindServices;
    }

    public String toString() {
        return this.mQueueName;
    }

    public boolean isPendingBroadcastProcessLocked(int pid) {
        return this.mPendingBroadcast != null && this.mPendingBroadcast.curApp.pid == pid;
    }

    public void enqueueParallelBroadcastLocked(BroadcastRecord r) {
        this.mParallelBroadcasts.add(r);
        enqueueBroadcastHelper(r);
    }

    public void enqueueOrderedBroadcastLocked(BroadcastRecord r) {
        this.mOrderedBroadcasts.add(r);
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
        return replaceBroadcastLocked(this.mOrderedBroadcasts, r, "ORDERED");
    }

    private BroadcastRecord replaceBroadcastLocked(ArrayList<BroadcastRecord> queue, BroadcastRecord r, String typeForLogging) {
        Intent intent = r.intent;
        for (int i = queue.size() - 1; i > 0; i--) {
            BroadcastRecord old = (BroadcastRecord) queue.get(i);
            if (old.userId == r.userId && intent.filterEquals(old.intent)) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG_BROADCAST, "***** DROPPING " + typeForLogging + " [" + this.mQueueName + "]: " + intent);
                }
                queue.set(i, r);
                return old;
            }
        }
        return null;
    }

    private final void processCurBroadcastLocked(BroadcastRecord r, ProcessRecord app) throws RemoteException {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG_BROADCAST, "Process cur broadcast " + r + " for app " + app);
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
            this.mService.updateLruProcessLocked(app, false, null);
            this.mService.updateOomAdjLocked();
            r.intent.setComponent(r.curComponent);
            boolean started = false;
            try {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.v(TAG_BROADCAST, "Delivering to component " + r.curComponent + ": " + r);
                }
                this.mService.notifyPackageUse(r.intent.getComponent().getPackageName(), 3);
                app.thread.scheduleReceiver(new Intent(r.intent), r.curReceiver, this.mService.compatibilityInfoForPackageLocked(r.curReceiver.applicationInfo), r.resultCode, r.resultData, r.resultExtras, r.ordered, r.userId, app.repProcState);
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG_BROADCAST, "Process cur broadcast " + r + " DELIVERED for app " + app);
                }
                started = true;
            } finally {
                if (!started) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG_BROADCAST, "Process cur broadcast " + r + ": NOT STARTED!");
                    }
                    r.receiver = null;
                    r.curApp = null;
                    app.curReceivers.remove(r);
                }
            }
        }
    }

    public boolean sendPendingBroadcastsLocked(ProcessRecord app) {
        boolean didSomething = false;
        BroadcastRecord br = this.mPendingBroadcast;
        if (br != null && br.curApp.pid == app.pid) {
            if (br.curApp != app) {
                Slog.e(TAG, "App mismatch when sending pending broadcast to " + app.processName + ", intended target is " + br.curApp.processName);
                return false;
            }
            try {
                this.mPendingBroadcast = null;
                processCurBroadcastLocked(br, app);
                didSomething = true;
            } catch (Exception e) {
                Slog.w(TAG, "Exception in new application when starting receiver " + br.curComponent.flattenToShortString(), e);
                logBroadcastReceiverDiscardLocked(br);
                finishReceiverLocked(br, br.resultCode, br.resultData, br.resultExtras, br.resultAbort, false);
                scheduleBroadcastsLocked();
                br.state = 0;
                throw new RuntimeException(e.getMessage());
            }
        }
        return didSomething;
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
        BroadcastRecord r = null;
        if (this.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord br = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
            if (br.curApp == app) {
                r = br;
            }
        }
        if (r == null && this.mPendingBroadcast != null && this.mPendingBroadcast.curApp == app) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG_BROADCAST, "[" + this.mQueueName + "] skip & discard pending app " + r);
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
            Slog.v(TAG_BROADCAST, "Schedule broadcasts [" + this.mQueueName + "]: current=" + this.mBroadcastsScheduled);
        }
        if (!this.mBroadcastsScheduled) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(200, this));
            this.mBroadcastsScheduled = true;
        }
    }

    public BroadcastRecord getMatchingOrderedReceiver(IBinder receiver) {
        if (this.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord r = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
            if (r == null || r.receiver != receiver) {
                return null;
            }
            return r;
        }
        return null;
    }

    public boolean finishReceiverLocked(BroadcastRecord r, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort, boolean waitForServices) {
        int state = r.state;
        ActivityInfo receiver = r.curReceiver;
        r.state = 0;
        if (state == 0) {
            Slog.w(TAG, "finishReceiver [" + this.mQueueName + "] called but state is IDLE");
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
        if (resultAbort && (r.intent.getFlags() & 134217728) == 0) {
            r.resultAbort = resultAbort;
        } else {
            r.resultAbort = false;
        }
        if (waitForServices && r.curComponent != null && r.queue.mDelayBehindServices && r.queue.mOrderedBroadcasts.size() > 0 && r.queue.mOrderedBroadcasts.get(0) == r) {
            ActivityInfo nextReceiver;
            if (r.nextReceiver < r.receivers.size()) {
                Object obj = r.receivers.get(r.nextReceiver);
                nextReceiver = obj instanceof ActivityInfo ? (ActivityInfo) obj : null;
            } else {
                nextReceiver = null;
            }
            if ((receiver == null || nextReceiver == null || receiver.applicationInfo.uid != nextReceiver.applicationInfo.uid || (receiver.processName.equals(nextReceiver.processName) ^ 1) != 0) && this.mService.mServices.hasBackgroundServicesLocked(r.userId)) {
                Slog.i(TAG, "Delay finish: " + r.curComponent.flattenToShortString());
                r.state = 4;
                return false;
            }
        }
        r.curComponent = null;
        boolean z = state != 1 ? state == 3 : true;
        return z;
    }

    public void backgroundServicesFinishedLocked(int userId) {
        if (this.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord br = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
            if (br.userId == userId && br.state == 4) {
                Slog.i(TAG, "Resuming delayed broadcast");
                br.curComponent = null;
                br.state = 0;
                processNextBroadcast(false);
            }
        }
    }

    void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver, Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
        if (app == null) {
            receiver.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
        } else if (app.thread != null) {
            try {
                app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode, data, extras, ordered, sticky, sendingUser, app.repProcState);
            } catch (RemoteException ex) {
                synchronized (this.mService) {
                    ActivityManagerService.boostPriorityForLockedSection();
                    Slog.w(TAG, "Can't deliver broadcast to " + app.processName + " (pid " + app.pid + "). Crashing it.");
                    app.scheduleCrash("can't deliver broadcast");
                    throw ex;
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        } else {
            throw new RemoteException("app.thread must not be null");
        }
    }

    private void deliverToRegisteredReceiverLocked(BroadcastRecord r, BroadcastFilter filter, boolean ordered, int index) {
        boolean skip = false;
        if (filter.requiredPermission != null) {
            if (this.mService.checkComponentPermission(filter.requiredPermission, r.callingPid, r.callingUid, -1, true) != 0) {
                Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires " + filter.requiredPermission + " due to registered receiver " + filter);
                skip = true;
            } else {
                int opCode = AppOpsManager.permissionToOpCode(filter.requiredPermission);
                if (opCode != -1) {
                    if (this.mService.mAppOpsService.noteOperation(opCode, r.callingUid, r.callerPackage) != 0) {
                        Slog.w(TAG, "Appop Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires appop " + AppOpsManager.permissionToOp(filter.requiredPermission) + " due to registered receiver " + filter);
                        skip = true;
                    }
                }
            }
        }
        if (!(skip || r.requiredPermissions == null || r.requiredPermissions.length <= 0)) {
            int i = 0;
            while (i < r.requiredPermissions.length) {
                String requiredPermission = r.requiredPermissions[i];
                if (this.mService.checkComponentPermission(requiredPermission, filter.receiverList.pid, filter.receiverList.uid, -1, true) == 0) {
                    int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                    if (appOp != -1 && appOp != r.appOp && this.mService.mAppOpsService.noteOperation(appOp, filter.receiverList.uid, filter.packageName) != 0) {
                        Slog.w(TAG, "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                        skip = true;
                        break;
                    }
                    i++;
                } else {
                    Slog.w(TAG, "Permission Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " requires " + requiredPermission + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                    skip = true;
                    break;
                }
            }
            if (r.intent != null && "android.provider.Telephony.SMS_RECEIVED".equals(r.intent.getAction())) {
                HwSystemManager.insertSendBroadcastRecord(filter.packageName, r.intent.getAction(), filter.receiverList.uid);
            }
        }
        if (!skip && ((r.requiredPermissions == null || r.requiredPermissions.length == 0) && this.mService.checkComponentPermission(null, filter.receiverList.pid, filter.receiverList.uid, -1, true) != 0)) {
            Slog.w(TAG, "Permission Denial: security check failed when receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
            skip = true;
        }
        if (!(skip || r.appOp == -1 || this.mService.mAppOpsService.noteOperation(r.appOp, filter.receiverList.uid, filter.packageName) == 0)) {
            Slog.w(TAG, "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
            skip = true;
        }
        if (!this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, filter.receiverList.uid)) {
            skip = true;
        }
        if (!skip && this.mService.shouldPreventSendBroadcast(r.intent, filter.packageName, r.callingUid, r.callingPid, r.callerPackage, r.userId)) {
            skip = true;
        }
        if (!skip && (filter.receiverList.app == null || filter.receiverList.app.killed || filter.receiverList.app.crashing)) {
            Slog.w(TAG, "Skipping deliver [" + this.mQueueName + "] " + r + " to " + filter.receiverList + ": process gone or crashing");
            skip = true;
        }
        boolean visibleToInstantApps = (r.intent.getFlags() & DumpState.DUMP_COMPILER_STATS) != 0;
        if (!(skip || (visibleToInstantApps ^ 1) == 0 || !filter.instantApp || filter.receiverList.uid == r.callingUid)) {
            Slog.w(TAG, "Instant App Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")" + " not specifying FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS");
            skip = true;
        }
        if (!(skip || (filter.visibleToInstantApp ^ 1) == 0 || !r.callerInstantApp || filter.receiverList.uid == r.callingUid)) {
            Slog.w(TAG, "Instant App Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " requires receiver be visible to instant apps" + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
            skip = true;
        }
        if (skip) {
            r.delivery[index] = 2;
            return;
        }
        if (this.mService.mPermissionReviewRequired) {
            if (!requestStartTargetPermissionsReviewIfNeededLocked(r, filter.packageName, filter.owningUserId)) {
                r.delivery[index] = 2;
                return;
            }
        }
        if (!getMtmBRManagerEnabled(10) || !getMtmBRManager().iawareProcessBroadcast(0, ordered ^ 1, r, filter)) {
            r.delivery[index] = 1;
            if (ordered) {
                r.receiver = filter.receiverList.receiver.asBinder();
                r.curFilter = filter;
                filter.receiverList.curBroadcast = r;
                r.state = 2;
                if (filter.receiverList.app != null) {
                    r.curApp = filter.receiverList.app;
                    filter.receiverList.app.curReceivers.add(r);
                    this.mService.updateOomAdjLocked(r.curApp, true);
                }
            }
            try {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.i(TAG_BROADCAST, "Delivering to " + filter + " : " + r);
                }
                if (filter.receiverList.app == null || !filter.receiverList.app.inFullBackup) {
                    performReceiveLocked(filter.receiverList.app, filter.receiverList.receiver, new Intent(r.intent), r.resultCode, r.resultData, r.resultExtras, r.ordered, r.initialSticky, r.userId);
                } else if (ordered) {
                    skipReceiverLocked(r);
                }
                if (ordered) {
                    r.state = 3;
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Failure sending broadcast " + r.intent, e);
                if (ordered) {
                    r.receiver = null;
                    r.curFilter = null;
                    filter.receiverList.curBroadcast = null;
                    if (filter.receiverList.app != null) {
                        filter.receiverList.app.curReceivers.remove(r);
                    }
                }
            }
        }
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(BroadcastRecord receiverRecord, String receivingPackageName, int receivingUserId) {
        if (!this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(receivingPackageName, receivingUserId)) {
            return true;
        }
        boolean callerForeground = receiverRecord.callerApp != null ? receiverRecord.callerApp.setSchedGroup != 0 : true;
        if (!callerForeground || receiverRecord.intent.getComponent() == null) {
            Slog.w(TAG, "u" + receivingUserId + " Receiving a broadcast in package" + receivingPackageName + " requires a permissions review");
        } else {
            IIntentSender target = this.mService.getIntentSenderLocked(1, receiverRecord.callerPackage, receiverRecord.callingUid, receiverRecord.userId, null, null, 0, new Intent[]{receiverRecord.intent}, new String[]{receiverRecord.intent.resolveType(this.mService.mContext.getContentResolver())}, 1409286144, null);
            final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent.addFlags(276824064);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", receivingPackageName);
            intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                Slog.i(TAG, "u" + receivingUserId + " Launching permission review for package " + receivingPackageName);
            }
            final int i = receivingUserId;
            this.mHandler.post(new Runnable() {
                public void run() {
                    BroadcastQueue.this.mService.mContext.startActivityAsUser(intent, new UserHandle(i));
                }
            });
        }
        return false;
    }

    final void scheduleTempWhitelistLocked(int uid, long duration, BroadcastRecord r) {
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

    final boolean isSignaturePerm(String[] perms) {
        if (perms == null) {
            return false;
        }
        IPackageManager pm = AppGlobals.getPackageManager();
        int i = perms.length - 1;
        while (i >= 0) {
            try {
                if ((pm.getPermissionInfo(perms[i], "android", 0).protectionLevel & 31) != 2) {
                    return false;
                }
                i--;
            } catch (RemoteException e) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:446:0x05b8 A:{SYNTHETIC} */
    /* JADX WARNING: Missing block: B:86:0x02c9, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:87:0x02cc, code:
            return;
     */
    /* JADX WARNING: Missing block: B:124:0x04dd, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:125:0x04e0, code:
            return;
     */
    /* JADX WARNING: Missing block: B:191:0x080e, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:192:0x0811, code:
            return;
     */
    /* JADX WARNING: Missing block: B:355:0x0fe1, code:
            if ((isSignaturePerm(r5.requiredPermissions) ^ 1) != 0) goto L_0x0fe3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void processNextBroadcast(boolean fromMsg) {
        synchronized (this.mService) {
            BroadcastRecord r;
            int i;
            Object target;
            ActivityManagerService.boostPriorityForLockedSection();
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG_BROADCAST, "processNextBroadcast [" + this.mQueueName + "]: " + this.mParallelBroadcasts.size() + " parallel broadcasts, " + this.mOrderedBroadcasts.size() + " ordered broadcasts");
            }
            this.mService.updateCpuStats();
            if (fromMsg) {
                this.mBroadcastsScheduled = false;
            }
            while (this.mParallelBroadcasts.size() > 0) {
                synchronized (this.mParallelBroadcasts) {
                    r = (BroadcastRecord) this.mParallelBroadcasts.remove(0);
                }
                if (r == null) {
                    Flog.e(104, "mParallelBroadcasts.get(0) is null");
                } else {
                    r.dispatchTime = SystemClock.uptimeMillis();
                    r.dispatchClockTime = System.currentTimeMillis();
                    if (Trace.isTagEnabled(64)) {
                        Trace.asyncTraceEnd(64, createBroadcastTraceTitle(r, 0), System.identityHashCode(r));
                        Trace.asyncTraceBegin(64, createBroadcastTraceTitle(r, 1), System.identityHashCode(r));
                    }
                    int N = r.receivers.size();
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                        Slog.v(TAG_BROADCAST, "Processing parallel broadcast [" + this.mQueueName + "] " + r);
                    }
                    if (getMtmBRManagerEnabled(10)) {
                        getMtmBRManager().iawareStartCountBroadcastSpeed(true, r);
                    }
                    for (i = 0; i < N; i++) {
                        target = r.receivers.get(i);
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG_BROADCAST, "Delivering non-ordered on [" + this.mQueueName + "] to registered " + target + ": " + r);
                        }
                        if (!enqueueProxyBroadcast(true, r, target)) {
                            deliverToRegisteredReceiverLocked(r, (BroadcastFilter) target, false, i);
                        } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG, "parallel " + this.mQueueName + " broadcast:(" + r + ") should be proxyed, target:(" + target + ")");
                        }
                    }
                    if (getMtmBRManagerEnabled(10)) {
                        getMtmBRManager().iawareEndCountBroadcastSpeed(r);
                    }
                    addBroadcastToHistoryLocked(r);
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                        Slog.v(TAG_BROADCAST, "Done with parallel broadcast [" + this.mQueueName + "] " + r);
                    }
                }
            }
            if (this.mPendingBroadcast != null) {
                ProcessRecord proc;
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.v(TAG_BROADCAST, "processNextBroadcast [" + this.mQueueName + "]: waiting for " + this.mPendingBroadcast.curApp);
                }
                synchronized (this.mService.mPidsSelfLocked) {
                    proc = (ProcessRecord) this.mService.mPidsSelfLocked.get(this.mPendingBroadcast.curApp.pid);
                    try {
                    } catch (Throwable th) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
                if (proc != null ? proc.crashing : true) {
                    Slog.w(TAG, "pending app  [" + this.mQueueName + "]" + this.mPendingBroadcast.curApp + " died before responding to broadcast");
                    this.mPendingBroadcast.state = 0;
                    this.mPendingBroadcast.nextReceiver = this.mPendingBroadcastRecvIndex;
                    this.mPendingBroadcast = null;
                } else {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
            }
            boolean looped = false;
            while (this.mOrderedBroadcasts.size() != 0) {
                r = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
                boolean forceReceive = false;
                int numReceivers = r.receivers != null ? r.receivers.size() : 0;
                if (this.mService.mProcessesReady && r.dispatchTime > 0) {
                    long now = SystemClock.uptimeMillis();
                    if (r.anrCount > 0) {
                        Slog.w(TAG_BROADCAST, "intentAction=" + r.intent.getAction() + "dispatchTime=" + r.dispatchTime + " numReceivers=" + numReceivers + " nextReceiver=" + r.nextReceiver + " now=" + now + " state=" + r.state + " curReceiver  = " + r.curReceiver);
                    }
                    if (numReceivers > 0 && now > r.dispatchTime + ((this.mTimeoutPeriod * 2) * ((long) numReceivers))) {
                        Slog.w(TAG, "Hung broadcast [" + this.mQueueName + "] discarded after timeout failure:" + " now=" + now + " dispatchTime=" + r.dispatchTime + " startTime=" + r.receiverTime + " intent=" + r.intent + " numReceivers=" + numReceivers + " nextReceiver=" + r.nextReceiver + " state=" + r.state);
                        broadcastTimeoutLocked(false);
                        forceReceive = true;
                        r.state = 0;
                    }
                    if (r.anrCount > 0 && numReceivers == 0 && now > r.dispatchTime && r.curReceiver != null && this == r.queue) {
                        Slog.w(TAG_BROADCAST, " dispatchTime=" + r.dispatchTime + " numReceivers=" + numReceivers + " nextReceiver=" + r.nextReceiver + " now=" + now + " state=" + r.state + " curReceiver  = " + r.curReceiver + "finish curReceiver");
                        logBroadcastReceiverDiscardLocked(r);
                        finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                        scheduleBroadcastsLocked();
                        r.state = 0;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                }
                if (r.state == 0) {
                    if (r.receivers != null && r.nextReceiver < numReceivers) {
                        if (!(r.resultAbort || forceReceive)) {
                            if (r != null) {
                                int recIdx = r.nextReceiver;
                                r.nextReceiver = recIdx + 1;
                                target = r.receivers.get(recIdx);
                                if (enqueueProxyBroadcast(false, r, target)) {
                                    Flog.i(104, "orderd " + this.mQueueName + " broadcast:(" + r + ") should be proxyed, target:(" + target + ")");
                                    scheduleBroadcastsLocked();
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return;
                                }
                                reportMediaButtonToAware(r, target);
                                r.receiverTime = SystemClock.uptimeMillis();
                                if (recIdx == 0) {
                                    r.dispatchTime = r.receiverTime;
                                    r.dispatchClockTime = System.currentTimeMillis();
                                    Flog.i(104, "dispatch ordered broadcast [" + this.mQueueName + "] " + r + ", has " + r.receivers.size() + " receivers");
                                    updateSRMSStatisticsData(r);
                                    if (Trace.isTagEnabled(64)) {
                                        Trace.asyncTraceEnd(64, createBroadcastTraceTitle(r, 0), System.identityHashCode(r));
                                        Trace.asyncTraceBegin(64, createBroadcastTraceTitle(r, 1), System.identityHashCode(r));
                                    }
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                                        Slog.v(TAG_BROADCAST, "Processing ordered broadcast [" + this.mQueueName + "] " + r);
                                    }
                                }
                                if (!this.mPendingBroadcastTimeoutMessage) {
                                    long timeoutTime = r.receiverTime + this.mTimeoutPeriod;
                                    long checkTime = r.receiverTime + ((long) CHECK_INTERVAL);
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Submitting BROADCAST_TIMEOUT_MSG [" + this.mQueueName + "] for " + r + " at " + timeoutTime + ". Next frozen check time is " + checkTime);
                                    }
                                    setBroadcastTimeoutLocked(checkTime);
                                }
                                BroadcastOptions brOptions = r.options;
                                Object nextReceiver = r.receivers.get(recIdx);
                                if (nextReceiver instanceof BroadcastFilter) {
                                    BroadcastFilter filter = (BroadcastFilter) nextReceiver;
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Delivering ordered [" + this.mQueueName + "] to registered " + filter + ": " + r);
                                    }
                                    deliverToRegisteredReceiverLocked(r, filter, r.ordered, recIdx);
                                    if (r.receiver == null || (r.ordered ^ 1) != 0) {
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v(TAG_BROADCAST, "Quick finishing [" + this.mQueueName + "]: ordered=" + r.ordered + " receiver=" + r.receiver);
                                        }
                                        r.state = 0;
                                        scheduleBroadcastsLocked();
                                    } else if (brOptions != null) {
                                        if (brOptions.getTemporaryAppWhitelistDuration() > 0) {
                                            scheduleTempWhitelistLocked(filter.owningUid, brOptions.getTemporaryAppWhitelistDuration(), r);
                                        }
                                    }
                                } else {
                                    int skip;
                                    ResolveInfo info = (ResolveInfo) nextReceiver;
                                    ComponentName componentName = new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
                                    boolean skip2 = false;
                                    if (brOptions != null && (info.activityInfo.applicationInfo.targetSdkVersion < brOptions.getMinManifestReceiverApiLevel() || info.activityInfo.applicationInfo.targetSdkVersion > brOptions.getMaxManifestReceiverApiLevel())) {
                                        skip2 = true;
                                    }
                                    int perm = this.mService.checkComponentPermission(info.activityInfo.permission, r.callingPid, r.callingUid, info.activityInfo.applicationInfo.uid, info.activityInfo.exported);
                                    if (!skip2 && perm != 0) {
                                        if (info.activityInfo.exported) {
                                            Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires " + info.activityInfo.permission + " due to receiver " + componentName.flattenToShortString());
                                        } else {
                                            Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " is not exported from uid " + info.activityInfo.applicationInfo.uid + " due to receiver " + componentName.flattenToShortString());
                                        }
                                        skip = true;
                                    } else if (!(skip2 || info.activityInfo.permission == null)) {
                                        int opCode = AppOpsManager.permissionToOpCode(info.activityInfo.permission);
                                        if (opCode != -1) {
                                            if (this.mService.mAppOpsService.noteOperation(opCode, r.callingUid, r.callerPackage) != 0) {
                                                Slog.w(TAG, "Appop Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires appop " + AppOpsManager.permissionToOp(info.activityInfo.permission) + " due to registered receiver " + componentName.flattenToShortString());
                                                skip = true;
                                            }
                                        }
                                    }
                                    if (!(skip == true || info.activityInfo.applicationInfo.uid == 1000 || r.requiredPermissions == null || r.requiredPermissions.length <= 0)) {
                                        for (String requiredPermission : r.requiredPermissions) {
                                            try {
                                                perm = AppGlobals.getPackageManager().checkPermission(requiredPermission, info.activityInfo.applicationInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                                            } catch (RemoteException e) {
                                                perm = -1;
                                            }
                                            if (perm != 0) {
                                                Slog.w(TAG, "Permission Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires " + requiredPermission + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                                skip = true;
                                                break;
                                            }
                                            int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                                            if (!(appOp == -1 || appOp == r.appOp)) {
                                                if (this.mService.mAppOpsService.noteOperation(appOp, info.activityInfo.applicationInfo.uid, info.activityInfo.packageName) != 0) {
                                                    Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                                    skip = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (r.intent != null && "android.provider.Telephony.SMS_RECEIVED".equals(r.intent.getAction())) {
                                            HwSystemManager.insertSendBroadcastRecord(info.activityInfo.applicationInfo.packageName, r.intent.getAction(), info.activityInfo.applicationInfo.uid);
                                        }
                                    }
                                    if (!(skip == true || r.appOp == -1 || this.mService.mAppOpsService.noteOperation(r.appOp, info.activityInfo.applicationInfo.uid, info.activityInfo.packageName) == 0)) {
                                        Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                        skip = true;
                                    }
                                    if (!skip == true) {
                                        skip = this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, info.activityInfo.applicationInfo.uid) ^ 1;
                                    }
                                    if (!skip == true && this.mService.shouldPreventSendBroadcast(r.intent, info.activityInfo.packageName, r.callingUid, r.callingPid, r.callerPackage, r.userId)) {
                                        skip = true;
                                    }
                                    boolean isSingleton = false;
                                    try {
                                        isSingleton = this.mService.isSingleton(info.activityInfo.processName, info.activityInfo.applicationInfo, info.activityInfo.name, info.activityInfo.flags);
                                    } catch (SecurityException e2) {
                                        Slog.w(TAG, e2.getMessage());
                                        skip = true;
                                    }
                                    if (!((info.activityInfo.flags & 1073741824) == 0 || ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS", info.activityInfo.applicationInfo.uid) == 0)) {
                                        Slog.w(TAG, "Permission Denial: Receiver " + componentName.flattenToShortString() + " requests FLAG_SINGLE_USER, but app does not hold " + "android.permission.INTERACT_ACROSS_USERS");
                                        skip = true;
                                    }
                                    if (!(skip == true || !info.activityInfo.applicationInfo.isInstantApp() || r.callingUid == info.activityInfo.applicationInfo.uid)) {
                                        Slog.w(TAG, "Instant App Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")" + " Instant Apps do not support manifest receivers");
                                        skip = true;
                                    }
                                    if (!skip == true && r.callerInstantApp && (info.activityInfo.flags & DumpState.DUMP_DEXOPT) == 0 && r.callingUid != info.activityInfo.applicationInfo.uid) {
                                        Slog.w(TAG, "Instant App Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires receiver have visibleToInstantApps set" + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                        skip = true;
                                    }
                                    if (skip == true) {
                                        r.manifestSkipCount++;
                                    } else {
                                        r.manifestCount++;
                                    }
                                    if (r.curApp != null && r.curApp.crashing) {
                                        Slog.w(TAG, "Skipping deliver ordered [" + this.mQueueName + "] " + r + " to " + r.curApp + ": process crashing");
                                        skip = true;
                                    }
                                    if (!skip == true) {
                                        boolean isAvailable = false;
                                        try {
                                            isAvailable = AppGlobals.getPackageManager().isPackageAvailable(info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                                        } catch (Throwable e3) {
                                            Slog.w(TAG, "Exception getting recipient info for " + info.activityInfo.packageName, e3);
                                        }
                                        if (!isAvailable) {
                                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                                Slog.v(TAG_BROADCAST, "Skipping delivery to " + info.activityInfo.packageName + " / " + info.activityInfo.applicationInfo.uid + " : package no longer available");
                                            }
                                            skip = true;
                                        }
                                    }
                                    if (HwServiceFactory.getHwNLPManager().shouldSkipGoogleNlp(r.intent, info.activityInfo.processName)) {
                                        skip = 1;
                                    }
                                    if (this.mService.mPermissionReviewRequired && (skip ^ 1) != 0) {
                                        if (!requestStartTargetPermissionsReviewIfNeededLocked(r, info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid))) {
                                            skip = 1;
                                        }
                                    }
                                    int receiverUid = info.activityInfo.applicationInfo.uid;
                                    if (r.callingUid != 1000 && isSingleton && this.mService.isValidSingletonCall(r.callingUid, receiverUid)) {
                                        info.activityInfo = this.mService.getActivityInfoForUser(info.activityInfo, 0);
                                    }
                                    String targetProcess = info.activityInfo.processName;
                                    ProcessRecord app = this.mService.getProcessRecordLocked(targetProcess, info.activityInfo.applicationInfo.uid, false);
                                    if (skip == 0) {
                                        int allowed = this.mService.getAppStartModeLocked(info.activityInfo.applicationInfo.uid, info.activityInfo.packageName, info.activityInfo.applicationInfo.targetSdkVersion, -1, true, false);
                                        if (allowed != 0) {
                                            if (allowed == 3) {
                                                Slog.w(TAG, "Background execution disabled: receiving " + r.intent + " to " + componentName.flattenToShortString());
                                                skip = 1;
                                            } else {
                                                if ((r.intent.getFlags() & 8388608) == 0) {
                                                    if (r.intent.getComponent() == null && r.intent.getPackage() == null && (r.intent.getFlags() & 16777216) == 0) {
                                                    }
                                                }
                                                this.mService.addBackgroundCheckViolationLocked(r.intent.getAction(), componentName.getPackageName());
                                                Slog.w(TAG, "Background execution not allowed: receiving " + r.intent + " to " + componentName.flattenToShortString());
                                                skip = 1;
                                            }
                                        }
                                    }
                                    if (skip == 0) {
                                        if (this.mService.shouldPreventSendReceiver(r.intent, info, r.callingPid, r.callingUid, app, r.callerApp)) {
                                            skip = 1;
                                        }
                                    }
                                    if (skip != 0) {
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v(TAG_BROADCAST, "Skipping delivery of ordered [" + this.mQueueName + "] " + r + " for whatever reason");
                                        }
                                        r.delivery[recIdx] = 2;
                                        r.receiver = null;
                                        r.curFilter = null;
                                        r.state = 0;
                                        scheduleBroadcastsLocked();
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        return;
                                    }
                                    r.delivery[recIdx] = 1;
                                    r.state = 1;
                                    r.curComponent = componentName;
                                    r.curReceiver = info.activityInfo;
                                    if (ActivityManagerDebugConfig.DEBUG_MU && r.callingUid > 100000) {
                                        Slog.v(TAG_MU, "Updated broadcast record activity info for secondary user, " + info.activityInfo + ", callingUid = " + r.callingUid + ", uid = " + info.activityInfo.applicationInfo.uid);
                                    }
                                    if (brOptions != null && brOptions.getTemporaryAppWhitelistDuration() > 0) {
                                        scheduleTempWhitelistLocked(receiverUid, brOptions.getTemporaryAppWhitelistDuration(), r);
                                    }
                                    try {
                                        AppGlobals.getPackageManager().setPackageStoppedState(r.curComponent.getPackageName(), false, UserHandle.getUserId(r.callingUid));
                                    } catch (RemoteException e4) {
                                    } catch (IllegalArgumentException e5) {
                                        Slog.w(TAG, "Failed trying to unstop package " + r.curComponent.getPackageName() + ": " + e5);
                                    }
                                    if (app != null) {
                                        if (!(app.thread == null || (app.killed ^ 1) == 0)) {
                                            try {
                                                app.addPackage(info.activityInfo.packageName, info.activityInfo.applicationInfo.versionCode, this.mService.mProcessStats);
                                                processCurBroadcastLocked(r, app);
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                return;
                                            } catch (Throwable e6) {
                                                Slog.w(TAG, "Exception when sending broadcast to " + r.curComponent, e6);
                                            } catch (Throwable e7) {
                                                Slog.wtf(TAG, "Failed sending broadcast to " + r.curComponent + " with " + r.intent, e7);
                                                logBroadcastReceiverDiscardLocked(r);
                                                finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                                scheduleBroadcastsLocked();
                                                r.state = 0;
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                return;
                                            }
                                        }
                                    }
                                    if (this.mService.mUserController.getCurrentUserIdLocked() == 0) {
                                        try {
                                            String packageName = info.activityInfo.applicationInfo.packageName;
                                            String sourceDir = info.activityInfo.applicationInfo.sourceDir;
                                            String publicSourceDir = info.activityInfo.applicationInfo.publicSourceDir;
                                            ApplicationInfo applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                                            if (applicationInfo == null) {
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                return;
                                            } else if (!(applicationInfo.sourceDir.equals(sourceDir) && (applicationInfo.publicSourceDir.equals(publicSourceDir) ^ 1) == 0)) {
                                                Slog.e(TAG, packageName + " is replaced, sourceDir is changed from " + sourceDir + " to " + applicationInfo.sourceDir + ", publicSourceDir is changed from " + publicSourceDir + " to " + applicationInfo.publicSourceDir);
                                                info.activityInfo.applicationInfo = applicationInfo;
                                            }
                                        } catch (RemoteException e8) {
                                        }
                                    }
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Need to start app [" + this.mQueueName + "] " + targetProcess + " for broadcast " + r);
                                    }
                                    if (getMtmBRManagerEnabled(11) && getMtmBRManager().iawareProcessBroadcast(1, false, r, target)) {
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        return;
                                    }
                                    ProcessRecord startProcessLocked = this.mService.startProcessLocked(targetProcess, info.activityInfo.applicationInfo, true, r.intent.getFlags() | 4, "broadcast", r.curComponent, (r.intent.getFlags() & 33554432) != 0, false, false);
                                    r.curApp = startProcessLocked;
                                    if (startProcessLocked == null) {
                                        Slog.w(TAG, "Unable to launch app " + info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.applicationInfo.uid + " for broadcast " + r.intent + ": process is bad");
                                        logBroadcastReceiverDiscardLocked(r);
                                        finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                        scheduleBroadcastsLocked();
                                        r.state = 0;
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        return;
                                    }
                                    this.mPendingBroadcast = r;
                                    this.mPendingBroadcastRecvIndex = recIdx;
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return;
                                }
                            }
                        }
                    }
                    if (r.resultTo != null) {
                        try {
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.i(TAG_BROADCAST, "Finishing broadcast [" + this.mQueueName + "] " + r.intent.getAction() + " app=" + r.callerApp);
                            }
                            performReceiveLocked(r.callerApp, r.resultTo, new Intent(r.intent), r.resultCode, r.resultData, r.resultExtras, false, false, r.userId);
                            r.resultTo = null;
                        } catch (Throwable e62) {
                            r.resultTo = null;
                            Slog.w(TAG, "Failure [" + this.mQueueName + "] sending broadcast result of " + r.intent, e62);
                        }
                    }
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG_BROADCAST, "Cancelling BROADCAST_TIMEOUT_MSG");
                    }
                    cancelBroadcastTimeoutLocked();
                    Slog.v(TAG_BROADCAST, "Finished with ordered broadcast " + r);
                    addBroadcastToHistoryLocked(r);
                    if (r.intent.getComponent() == null && r.intent.getPackage() == null && (r.intent.getFlags() & 1073741824) == 0) {
                        this.mService.addBroadcastStatLocked(r.intent.getAction(), r.callerPackage, r.manifestCount, r.manifestSkipCount, r.finishTime - r.dispatchTime);
                    }
                    this.mOrderedBroadcasts.remove(0);
                    r = null;
                    looped = true;
                    continue;
                    if (r != null) {
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.d(TAG_BROADCAST, "processNextBroadcast(" + this.mQueueName + ") called when not idle (state=" + r.state + ")");
                }
            }
            this.mService.scheduleAppGcsLocked();
            if (looped) {
                this.mService.updateOomAdjLocked();
            }
        }
    }

    final void setBroadcastTimeoutLocked(long timeoutTime) {
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

    final void cancelBroadcastTimeoutLocked() {
        if (this.mPendingBroadcastTimeoutMessage) {
            this.mHandler.removeMessages(BROADCAST_TIMEOUT_MSG, this);
            this.mHandler.removeMessages(BROADCAST_CHECKTIMEOUT_MSG, this);
            this.mPendingBroadcastTimeoutMessage = false;
        }
    }

    final void broadcastTimeoutLocked(boolean fromMsg) {
        if (fromMsg) {
            this.mPendingBroadcastTimeoutMessage = false;
        }
        if (this.mOrderedBroadcasts.size() != 0) {
            long now = SystemClock.uptimeMillis();
            BroadcastRecord r = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
            if (fromMsg) {
                if (this.mService.mDidDexOpt) {
                    this.mService.mDidDexOpt = false;
                    setBroadcastTimeoutLocked(SystemClock.uptimeMillis() + this.mTimeoutPeriod);
                    return;
                } else if (this.mService.mProcessesReady) {
                    long timeoutTime = r.receiverTime + this.mTimeoutPeriod;
                    long checkTime = r.receiverTime + ((long) CHECK_INTERVAL);
                    if (checkTime > now) {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG_BROADCAST, "Premature timeout [" + this.mQueueName + "] @ " + now + ": resetting BROADCAST_TIMEOUT_MSG for " + timeoutTime + ". Next frozen check time is " + checkTime);
                        }
                        setBroadcastTimeoutLocked(checkTime);
                        return;
                    } else if (timeoutTime > now) {
                        if (this.mService.isTopProcessLocked(((BroadcastRecord) this.mOrderedBroadcasts.get(0)).curApp)) {
                            Slog.w(TAG, "ANR has been triggered " + (timeoutTime - now) + "ms earlier because it caused frozen problem.");
                        } else {
                            setBroadcastTimeoutLocked(now + ((long) CHECK_INTERVAL));
                            return;
                        }
                    }
                } else {
                    return;
                }
            }
            BroadcastRecord br = (BroadcastRecord) this.mOrderedBroadcasts.get(0);
            if (br.state == 4) {
                Slog.i(TAG, "Waited long enough for: " + (br.curComponent != null ? br.curComponent.flattenToShortString() : "(null)"));
                br.curComponent = null;
                br.state = 0;
                processNextBroadcast(false);
                return;
            }
            Slog.w(TAG, "Timeout of broadcast " + r + " - receiver=" + r.receiver + ", started " + (now - r.receiverTime) + "ms ago");
            r.receiverTime = now;
            r.anrCount++;
            if (r.nextReceiver <= 0) {
                Slog.w(TAG, "Timeout on receiver with nextReceiver <= 0");
                return;
            }
            ProcessRecord app = null;
            String anrMessage = null;
            BroadcastFilter curReceiver = r.receivers.get(r.nextReceiver - 1);
            r.delivery[r.nextReceiver - 1] = 3;
            Slog.w(TAG, "Receiver during timeout: " + curReceiver);
            logBroadcastReceiverDiscardLocked(r);
            if (curReceiver instanceof BroadcastFilter) {
                BroadcastFilter bf = curReceiver;
                if (!(bf.receiverList.pid == 0 || bf.receiverList.pid == ActivityManagerService.MY_PID)) {
                    synchronized (this.mService.mPidsSelfLocked) {
                        app = (ProcessRecord) this.mService.mPidsSelfLocked.get(bf.receiverList.pid);
                    }
                }
            } else {
                app = r.curApp;
            }
            if (app != null) {
                anrMessage = "Broadcast of " + r.intent.toString();
            }
            if (this.mPendingBroadcast == r) {
                this.mPendingBroadcast = null;
            }
            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
            scheduleBroadcastsLocked();
            if (anrMessage == null || !"android.intent.action.PRE_BOOT_COMPLETED".equals(r.intent.getAction())) {
                if (anrMessage != null) {
                    this.mHandler.post(new AppNotResponding(app, anrMessage));
                }
                return;
            }
            Slog.w(TAG, "Skip anr of PRE_BOOT_COMPLETED for app :" + app);
        }
    }

    private final int ringAdvance(int x, int increment, int ringSize) {
        x += increment;
        if (x < 0) {
            return ringSize - 1;
        }
        if (x >= ringSize) {
            return 0;
        }
        return x;
    }

    private final void addBroadcastToHistoryLocked(BroadcastRecord r) {
        if (r.callingUid >= 0) {
            r.finishTime = SystemClock.uptimeMillis();
            if (Trace.isTagEnabled(64)) {
                Trace.asyncTraceEnd(64, createBroadcastTraceTitle(r, 1), System.identityHashCode(r));
            }
            this.mBroadcastHistory[this.mHistoryNext] = r;
            this.mHistoryNext = ringAdvance(this.mHistoryNext, 1, MAX_BROADCAST_HISTORY);
            this.mBroadcastSummaryHistory[this.mSummaryHistoryNext] = r.intent;
            this.mSummaryHistoryEnqueueTime[this.mSummaryHistoryNext] = r.enqueueClockTime;
            this.mSummaryHistoryDispatchTime[this.mSummaryHistoryNext] = r.dispatchClockTime;
            this.mSummaryHistoryFinishTime[this.mSummaryHistoryNext] = System.currentTimeMillis();
            this.mSummaryHistoryNext = ringAdvance(this.mSummaryHistoryNext, 1, MAX_BROADCAST_SUMMARY_HISTORY);
        }
    }

    boolean cleanupDisabledPackageReceiversLocked(String packageName, Set<String> filterByClasses, int userId, boolean doit) {
        int i;
        boolean didSomething = false;
        for (i = this.mParallelBroadcasts.size() - 1; i >= 0; i--) {
            didSomething |= ((BroadcastRecord) this.mParallelBroadcasts.get(i)).cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
            if (!doit && didSomething) {
                return true;
            }
        }
        for (i = this.mOrderedBroadcasts.size() - 1; i >= 0; i--) {
            didSomething |= ((BroadcastRecord) this.mOrderedBroadcasts.get(i)).cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
            if (!doit && didSomething) {
                return true;
            }
        }
        return didSomething;
    }

    final void logBroadcastReceiverDiscardLocked(BroadcastRecord r) {
        int logIndex = r.nextReceiver - 1;
        if (logIndex < 0 || logIndex >= r.receivers.size()) {
            if (logIndex < 0) {
                Slog.w(TAG, "Discarding broadcast before first receiver is invoked: " + r);
            }
            EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_APP, new Object[]{Integer.valueOf(-1), Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(r.nextReceiver), "NONE"});
            return;
        }
        BroadcastFilter curReceiver = r.receivers.get(logIndex);
        if (curReceiver instanceof BroadcastFilter) {
            BroadcastFilter bf = curReceiver;
            EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_FILTER, new Object[]{Integer.valueOf(bf.owningUserId), Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(logIndex), Integer.valueOf(System.identityHashCode(bf))});
            return;
        }
        ResolveInfo ri = (ResolveInfo) curReceiver;
        EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_APP, new Object[]{Integer.valueOf(UserHandle.getUserId(ri.activityInfo.applicationInfo.uid)), Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(logIndex), ri.toString()});
    }

    private String createBroadcastTraceTitle(BroadcastRecord record, int state) {
        String str = "Broadcast %s from %s (%s) %s";
        Object[] objArr = new Object[4];
        objArr[0] = state == 0 ? "in queue" : "dispatched";
        objArr[1] = record.callerPackage == null ? "" : record.callerPackage;
        objArr[2] = record.callerApp == null ? "process unknown" : record.callerApp.toShortString();
        objArr[3] = record.intent == null ? "" : record.intent.getAction();
        return String.format(str, objArr);
    }

    final boolean isIdle() {
        if (this.mParallelBroadcasts.isEmpty() && this.mOrderedBroadcasts.isEmpty() && this.mPendingBroadcast == null) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:47:0x019a, code:
            if (r24.equals(r18.mPendingBroadcast.callerPackage) != false) goto L_0x019c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage, boolean needSep) {
        boolean printed;
        int i;
        Bundle bundle;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (this.mParallelBroadcasts.size() > 0 || this.mOrderedBroadcasts.size() > 0 || this.mPendingBroadcast != null) {
            BroadcastRecord br;
            printed = false;
            for (i = this.mParallelBroadcasts.size() - 1; i >= 0; i--) {
                br = (BroadcastRecord) this.mParallelBroadcasts.get(i);
                if (dumpPackage != null) {
                    if ((dumpPackage.equals(br.callerPackage) ^ 1) != 0) {
                    }
                }
                if (!printed) {
                    if (needSep) {
                        pw.println();
                    }
                    needSep = true;
                    printed = true;
                    pw.println("  Active broadcasts [" + this.mQueueName + "]:");
                }
                pw.println("  Active Broadcast " + this.mQueueName + " #" + i + ":");
                br.dump(pw, "    ", sdf);
            }
            printed = false;
            needSep = true;
            for (i = this.mOrderedBroadcasts.size() - 1; i >= 0; i--) {
                br = (BroadcastRecord) this.mOrderedBroadcasts.get(i);
                if (dumpPackage != null) {
                    if ((dumpPackage.equals(br.callerPackage) ^ 1) != 0) {
                    }
                }
                if (!printed) {
                    if (needSep) {
                        pw.println();
                    }
                    needSep = true;
                    printed = true;
                    pw.println("  Active ordered broadcasts [" + this.mQueueName + "]:");
                }
                pw.println("  Active Ordered Broadcast " + this.mQueueName + " #" + i + ":");
                ((BroadcastRecord) this.mOrderedBroadcasts.get(i)).dump(pw, "    ", sdf);
            }
            if (dumpPackage != null) {
                if (this.mPendingBroadcast != null) {
                }
            }
            if (needSep) {
                pw.println();
            }
            pw.println("  Pending broadcast [" + this.mQueueName + "]:");
            if (this.mPendingBroadcast != null) {
                this.mPendingBroadcast.dump(pw, "    ", sdf);
            } else {
                pw.println("    (null)");
            }
            needSep = true;
        }
        printed = false;
        i = -1;
        int lastIndex = this.mHistoryNext;
        int ringIndex = lastIndex;
        do {
            ringIndex = ringAdvance(ringIndex, -1, MAX_BROADCAST_HISTORY);
            BroadcastRecord r = this.mBroadcastHistory[ringIndex];
            if (r != null) {
                i++;
                if (dumpPackage != null) {
                    if ((dumpPackage.equals(r.callerPackage) ^ 1) != 0) {
                        continue;
                    }
                }
                if (!printed) {
                    if (needSep) {
                        pw.println();
                    }
                    needSep = true;
                    pw.println("  Historical broadcasts [" + this.mQueueName + "]:");
                    printed = true;
                }
                if (dumpAll) {
                    pw.print("  Historical Broadcast " + this.mQueueName + " #");
                    pw.print(i);
                    pw.println(":");
                    r.dump(pw, "    ", sdf);
                    continue;
                } else {
                    pw.print("  #");
                    pw.print(i);
                    pw.print(": ");
                    pw.println(r);
                    pw.print("    ");
                    pw.println(r.intent.toShortString(true, true, true, false));
                    if (!(r.targetComp == null || r.targetComp == r.intent.getComponent())) {
                        pw.print("    targetComp: ");
                        pw.println(r.targetComp.toShortString());
                    }
                    bundle = r.intent.getExtras();
                    if (bundle != null) {
                        if ((isFromEmailMDM(r.intent) ^ 1) != 0) {
                            pw.print("    extras: ");
                            pw.println(bundle.toString());
                            continue;
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            }
        } while (ringIndex != lastIndex);
        if (dumpPackage == null) {
            int ringIndex2;
            ringIndex = this.mSummaryHistoryNext;
            lastIndex = ringIndex;
            if (dumpAll) {
                printed = false;
                i = -1;
                ringIndex2 = ringIndex;
            } else {
                int j = i;
                ringIndex2 = ringIndex;
                while (j > 0 && ringIndex2 != ringIndex) {
                    ringIndex2 = ringAdvance(ringIndex2, -1, MAX_BROADCAST_SUMMARY_HISTORY);
                    if (this.mBroadcastHistory[ringIndex2] != null) {
                        j--;
                    }
                }
            }
            do {
                ringIndex2 = ringAdvance(ringIndex2, -1, MAX_BROADCAST_SUMMARY_HISTORY);
                Intent intent = this.mBroadcastSummaryHistory[ringIndex2];
                if (intent != null) {
                    if (!printed) {
                        if (needSep) {
                            pw.println();
                        }
                        needSep = true;
                        pw.println("  Historical broadcasts summary [" + this.mQueueName + "]:");
                        printed = true;
                    }
                    if (!dumpAll && i >= 50) {
                        pw.println("  ...");
                        ringIndex = ringIndex2;
                        break;
                    }
                    i++;
                    pw.print("  #");
                    pw.print(i);
                    pw.print(": ");
                    pw.println(intent.toShortString(false, true, true, false));
                    pw.print("    ");
                    TimeUtils.formatDuration(this.mSummaryHistoryDispatchTime[ringIndex2] - this.mSummaryHistoryEnqueueTime[ringIndex2], pw);
                    pw.print(" dispatch ");
                    TimeUtils.formatDuration(this.mSummaryHistoryFinishTime[ringIndex2] - this.mSummaryHistoryDispatchTime[ringIndex2], pw);
                    pw.println(" finish");
                    pw.print("    enq=");
                    pw.print(sdf.format(new Date(this.mSummaryHistoryEnqueueTime[ringIndex2])));
                    pw.print(" disp=");
                    pw.print(sdf.format(new Date(this.mSummaryHistoryDispatchTime[ringIndex2])));
                    pw.print(" fin=");
                    pw.println(sdf.format(new Date(this.mSummaryHistoryFinishTime[ringIndex2])));
                    bundle = intent.getExtras();
                    if (!(bundle == null || "android.intent.action.PHONE_STATE".equals(intent.getAction()) || ("android.intent.action.NEW_OUTGOING_CALL".equals(intent.getAction()) ^ 1) == 0 || (isFromEmailMDM(intent) ^ 1) == 0)) {
                        pw.print("    extras: ");
                        pw.println(bundle.toString());
                        continue;
                    }
                }
            } while (ringIndex2 != ringIndex);
            ringIndex = ringIndex2;
        }
        return needSep;
    }

    private boolean isFromEmailMDM(Intent intent) {
        if (intent == null) {
            return false;
        }
        return "com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction());
    }

    public void cleanupBroadcastLocked(ProcessRecord app) {
    }

    private void updateSRMSStatisticsData(BroadcastRecord r) {
        if (!mIsBetaUser || !this.mService.getIawareResourceFeature(1)) {
            return;
        }
        if ("bgkeyapp".equals(this.mQueueName) || "fgkeyapp".equals(this.mQueueName)) {
            long elapsedTime = r.dispatchClockTime - r.enqueueClockTime;
            if (elapsedTime >= 0 && elapsedTime <= 20) {
                this.mService.updateSRMSStatisticsData(10);
            } else if (elapsedTime > 20 && elapsedTime <= 60) {
                this.mService.updateSRMSStatisticsData(11);
            } else if (elapsedTime > 60 && elapsedTime <= 100) {
                this.mService.updateSRMSStatisticsData(12);
            } else if (elapsedTime > 100) {
                this.mService.updateSRMSStatisticsData(13);
            } else {
                Slog.w(TAG_BROADCAST, "elapsedTime error [" + this.mQueueName + "] for " + r);
            }
        }
    }
}
