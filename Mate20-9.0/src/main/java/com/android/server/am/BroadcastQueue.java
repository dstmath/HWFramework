package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
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
import android.util.proto.ProtoOutputStream;
import com.android.server.HwServiceFactory;
import com.android.server.display.DisplayTransformManager;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.wm.WindowState;
import com.huawei.pgmng.common.Utils;
import huawei.android.security.IHwBehaviorCollectManager;
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
    static final int MAX_BROADCAST_HISTORY = ((ActivityManager.isLowRamDeviceStatic() || SystemProperties.getBoolean("ro.config.hw_low_ram", false)) ? 10 : 50);
    static final int MAX_BROADCAST_SUMMARY_HISTORY = ((ActivityManager.isLowRamDeviceStatic() || SystemProperties.getBoolean("ro.config.hw_low_ram", false)) ? 25 : DisplayTransformManager.LEVEL_COLOR_MATRIX_INVERT_COLOR);
    static final int MAYBE_BROADCAST_BG_TIMEOUT = 5000;
    static final int MAYBE_BROADCAST_FG_TIMEOUT = 2000;
    static final int SCHEDULE_TEMP_WHITELIST_MSG = 202;
    private static final String TAG = "BroadcastQueue";
    private static final String TAG_BROADCAST = "BroadcastQueue";
    private static final String TAG_MU = "BroadcastQueue_MU";
    private static final boolean mIsBetaUser;
    final BroadcastRecord[] mBroadcastHistory = new BroadcastRecord[MAX_BROADCAST_HISTORY];
    final Intent[] mBroadcastSummaryHistory = new Intent[MAX_BROADCAST_SUMMARY_HISTORY];
    boolean mBroadcastsScheduled = false;
    final boolean mDelayBehindServices;
    final BroadcastHandler mHandler;
    int mHistoryNext = 0;
    final ArrayList<BroadcastRecord> mOrderedBroadcasts = new ArrayList<>();
    final ArrayList<BroadcastRecord> mParallelBroadcasts = new ArrayList<>();
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
            return focusedWindow == null || windowTypeToLayerLw(focusedWindow.mAttrs.type, focusedWindow.canAddInternalSystemWindow()) >= windowTypeToLayerLw(2003, true);
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
                Slog.e("BroadcastQueue", "BroadcastQueue AppNotResponding getCurFocusedWindow failed", e);
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
                        Slog.v("BroadcastQueue", "Received BROADCAST_INTENT_MSG");
                    }
                    BroadcastQueue.this.processNextBroadcast(true);
                    return;
                case BroadcastQueue.BROADCAST_TIMEOUT_MSG /*201*/:
                    synchronized (BroadcastQueue.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            BroadcastQueue.this.broadcastTimeoutLocked(true);
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
                case BroadcastQueue.BROADCAST_CHECKTIMEOUT_MSG /*203*/:
                    synchronized (BroadcastQueue.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            BroadcastQueue.this.handleMaybeTimeoutBC();
                            BroadcastQueue.this.uploadRadarMessage(2803, null);
                        } catch (Throwable th2) {
                            while (true) {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th2;
                                break;
                            }
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                default:
                    return;
            }
        }
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3) {
            z = true;
        }
        mIsBetaUser = z;
    }

    /* access modifiers changed from: private */
    public void handleMaybeTimeoutBC() {
        if (this.mOrderedBroadcasts.size() == 0) {
            Slog.w("BroadcastQueue", "handleMaybeTimeoutBC, but mOrderedBroadcasts is null");
            return;
        }
        BroadcastRecord r = this.mOrderedBroadcasts.get(0);
        if (r.nextReceiver <= 0) {
            Slog.w("BroadcastQueue", "handleMaybeTimeoutBC Timeout on receiver with nextReceiver <= 0");
            return;
        }
        String pkg = null;
        String pid = null;
        String target = null;
        Object curReceiver = r.receivers.get(r.nextReceiver - 1);
        if (curReceiver instanceof BroadcastFilter) {
            BroadcastFilter bf = (BroadcastFilter) curReceiver;
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
        int i = queue.size() - 1;
        while (i > 0) {
            BroadcastRecord old = queue.get(i);
            if (old.userId != r.userId || !intent.filterEquals(old.intent)) {
                i--;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v("BroadcastQueue", "***** DROPPING " + typeForLogging + " [" + this.mQueueName + "]: " + intent);
                }
                queue.set(i, r);
                return old;
            }
        }
        return null;
    }

    private final void processCurBroadcastLocked(BroadcastRecord r, ProcessRecord app, boolean skipOomAdj) throws RemoteException {
        BroadcastRecord broadcastRecord = r;
        ProcessRecord processRecord = app;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(processRecord.uid, processRecord.pid, IHwBehaviorCollectManager.BehaviorId.BROADCASTQUEUE_PROCESSCURBROADCASTLOCKED, new Object[]{broadcastRecord.intent});
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v("BroadcastQueue", "Process cur broadcast " + broadcastRecord + " for app " + processRecord);
        }
        if (processRecord.thread == null) {
            throw new RemoteException();
        } else if (processRecord.inFullBackup) {
            skipReceiverLocked(r);
        } else {
            broadcastRecord.receiver = processRecord.thread.asBinder();
            broadcastRecord.curApp = processRecord;
            processRecord.curReceivers.add(broadcastRecord);
            processRecord.forceProcessStateUpTo(10);
            this.mService.updateLruProcessLocked(processRecord, false, null);
            if (!skipOomAdj) {
                this.mService.updateOomAdjLocked();
            }
            broadcastRecord.intent.setComponent(broadcastRecord.curComponent);
            try {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.v("BroadcastQueue", "Delivering to component " + broadcastRecord.curComponent + ": " + broadcastRecord);
                }
                this.mService.notifyPackageUse(broadcastRecord.intent.getComponent().getPackageName(), 3);
                processRecord.thread.scheduleReceiver(new Intent(broadcastRecord.intent), broadcastRecord.curReceiver, this.mService.compatibilityInfoForPackageLocked(broadcastRecord.curReceiver.applicationInfo), broadcastRecord.resultCode, broadcastRecord.resultData, broadcastRecord.resultExtras, broadcastRecord.ordered, broadcastRecord.userId, processRecord.repProcState);
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v("BroadcastQueue", "Process cur broadcast " + broadcastRecord + " DELIVERED for app " + processRecord);
                }
                if (1 == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v("BroadcastQueue", "Process cur broadcast " + broadcastRecord + ": NOT STARTED!");
                    }
                    broadcastRecord.receiver = null;
                    broadcastRecord.curApp = null;
                    processRecord.curReceivers.remove(broadcastRecord);
                }
            } catch (Throwable th) {
                if (0 == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v("BroadcastQueue", "Process cur broadcast " + broadcastRecord + ": NOT STARTED!");
                    }
                    broadcastRecord.receiver = null;
                    broadcastRecord.curApp = null;
                    processRecord.curReceivers.remove(broadcastRecord);
                }
                throw th;
            }
        }
    }

    public boolean sendPendingBroadcastsLocked(ProcessRecord app) {
        boolean didSomething = false;
        BroadcastRecord br = this.mPendingBroadcast;
        if (br != null && br.curApp.pid > 0 && br.curApp.pid == app.pid) {
            if (br.curApp != app) {
                Slog.e("BroadcastQueue", "App mismatch when sending pending broadcast to " + app.processName + ", intended target is " + br.curApp.processName);
                return false;
            }
            try {
                this.mPendingBroadcast = null;
                processCurBroadcastLocked(br, app, false);
                didSomething = true;
            } catch (Exception e) {
                Exception e2 = e;
                Slog.w("BroadcastQueue", "Exception in new application when starting receiver " + br.curComponent.flattenToShortString(), e2);
                logBroadcastReceiverDiscardLocked(br);
                finishReceiverLocked(br, br.resultCode, br.resultData, br.resultExtras, br.resultAbort, false);
                scheduleBroadcastsLocked();
                br.state = 0;
                throw new RuntimeException(e2.getMessage());
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
            BroadcastRecord br = this.mOrderedBroadcasts.get(0);
            if (br.curApp == app) {
                r = br;
            }
        }
        if (r == null && this.mPendingBroadcast != null && this.mPendingBroadcast.curApp == app) {
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
            this.mHandler.sendMessage(this.mHandler.obtainMessage(200, this));
            this.mBroadcastsScheduled = true;
        }
    }

    public BroadcastRecord getMatchingOrderedReceiver(IBinder receiver) {
        if (this.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord r = this.mOrderedBroadcasts.get(0);
            if (r != null && r.receiver == receiver) {
                return r;
            }
        }
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v16, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v16, resolved type: android.content.pm.ActivityInfo} */
    /* JADX WARNING: Multi-variable type inference failed */
    public boolean finishReceiverLocked(BroadcastRecord r, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort, boolean waitForServices) {
        ActivityInfo nextReceiver;
        boolean z = true;
        if (r.curApp != null) {
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(r.curApp.uid, r.curApp.pid, IHwBehaviorCollectManager.BehaviorId.BROADCASTQUEUE_FINISHRECEIVERLOCKED, new Object[]{r.intent});
        }
        int state = r.state;
        ActivityInfo receiver = r.curReceiver;
        r.state = 0;
        if (state == 0) {
            Slog.w("BroadcastQueue", "finishReceiver [" + this.mQueueName + "] called but state is IDLE");
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
        if (!resultAbort || (r.intent.getFlags() & 134217728) != 0) {
            r.resultAbort = false;
        } else {
            r.resultAbort = resultAbort;
        }
        if (waitForServices && r.curComponent != null && r.queue.mDelayBehindServices && r.queue.mOrderedBroadcasts.size() > 0 && r.queue.mOrderedBroadcasts.get(0) == r) {
            if (r.nextReceiver < r.receivers.size()) {
                Object obj = r.receivers.get(r.nextReceiver);
                nextReceiver = obj instanceof ActivityInfo ? obj : null;
            } else {
                nextReceiver = null;
            }
            if ((receiver == null || nextReceiver == null || receiver.applicationInfo.uid != nextReceiver.applicationInfo.uid || !receiver.processName.equals(nextReceiver.processName)) && this.mService.mServices.hasBackgroundServicesLocked(r.userId)) {
                Slog.i("BroadcastQueue", "Delay finish: " + r.curComponent.flattenToShortString());
                r.state = 4;
                return false;
            }
        }
        r.curComponent = null;
        if (!(state == 1 || state == 3)) {
            z = false;
        }
        return z;
    }

    public void backgroundServicesFinishedLocked(int userId) {
        if (this.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord br = this.mOrderedBroadcasts.get(0);
            if (br.userId == userId && br.state == 4) {
                Slog.i("BroadcastQueue", "Resuming delayed broadcast");
                br.curComponent = null;
                br.state = 0;
                processNextBroadcast(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver, Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
        ProcessRecord processRecord = app;
        if (processRecord != null) {
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(processRecord.uid, processRecord.pid, IHwBehaviorCollectManager.BehaviorId.BROADCASTQUEUE_PERFORMRECEIVELOCKED, new Object[]{intent});
        }
        if (processRecord == null) {
            receiver.performReceive(intent, resultCode, data, extras, ordered, sticky, sendingUser);
        } else if (processRecord.thread != null) {
            try {
                processRecord.thread.scheduleRegisteredReceiver(receiver, intent, resultCode, data, extras, ordered, sticky, sendingUser, processRecord.repProcState);
            } catch (RemoteException e) {
                RemoteException ex = e;
                synchronized (this.mService) {
                    ActivityManagerService.boostPriorityForLockedSection();
                    Slog.w("BroadcastQueue", "Can't deliver broadcast to " + processRecord.processName + " (pid " + processRecord.pid + "). Crashing it.");
                    processRecord.scheduleCrash("can't deliver broadcast");
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw ex;
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } else {
            throw new RemoteException("app.thread must not be null");
        }
    }

    private void deliverToRegisteredReceiverLocked(BroadcastRecord r, BroadcastFilter filter, boolean ordered, int index) {
        BroadcastRecord broadcastRecord = r;
        BroadcastFilter broadcastFilter = filter;
        boolean skip = false;
        if (broadcastFilter.requiredPermission != null) {
            if (this.mService.checkComponentPermission(broadcastFilter.requiredPermission, broadcastRecord.callingPid, broadcastRecord.callingUid, -1, true) != 0) {
                Slog.w("BroadcastQueue", "Permission Denial: broadcasting " + broadcastRecord.intent.toString() + " from " + broadcastRecord.callerPackage + " (pid=" + broadcastRecord.callingPid + ", uid=" + broadcastRecord.callingUid + ") requires " + broadcastFilter.requiredPermission + " due to registered receiver " + broadcastFilter);
                skip = true;
            } else {
                int opCode = AppOpsManager.permissionToOpCode(broadcastFilter.requiredPermission);
                if (!(opCode == -1 || this.mService.mAppOpsService.noteOperation(opCode, broadcastRecord.callingUid, broadcastRecord.callerPackage) == 0)) {
                    Slog.w("BroadcastQueue", "Appop Denial: broadcasting " + broadcastRecord.intent.toString() + " from " + broadcastRecord.callerPackage + " (pid=" + broadcastRecord.callingPid + ", uid=" + broadcastRecord.callingUid + ") requires appop " + AppOpsManager.permissionToOp(broadcastFilter.requiredPermission) + " due to registered receiver " + broadcastFilter);
                    skip = true;
                }
            }
        }
        if (!skip && broadcastRecord.requiredPermissions != null && broadcastRecord.requiredPermissions.length > 0) {
            int i = 0;
            while (true) {
                if (i >= broadcastRecord.requiredPermissions.length) {
                    break;
                }
                String requiredPermission = broadcastRecord.requiredPermissions[i];
                if (this.mService.checkComponentPermission(requiredPermission, broadcastFilter.receiverList.pid, broadcastFilter.receiverList.uid, -1, true) == 0) {
                    int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                    if (appOp != -1 && appOp != broadcastRecord.appOp && this.mService.mAppOpsService.noteOperation(appOp, broadcastFilter.receiverList.uid, broadcastFilter.packageName) != 0) {
                        Slog.w("BroadcastQueue", "Appop Denial: receiving " + broadcastRecord.intent.toString() + " to " + broadcastFilter.receiverList.app + " (pid=" + broadcastFilter.receiverList.pid + ", uid=" + broadcastFilter.receiverList.uid + ") requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + broadcastRecord.callerPackage + " (uid " + broadcastRecord.callingUid + ")");
                        skip = true;
                        break;
                    }
                    i++;
                } else {
                    Slog.w("BroadcastQueue", "Permission Denial: receiving " + broadcastRecord.intent.toString() + " to " + broadcastFilter.receiverList.app + " (pid=" + broadcastFilter.receiverList.pid + ", uid=" + broadcastFilter.receiverList.uid + ") requires " + requiredPermission + " due to sender " + broadcastRecord.callerPackage + " (uid " + broadcastRecord.callingUid + ")");
                    skip = true;
                    break;
                }
            }
            if (broadcastRecord.intent != null && "android.provider.Telephony.SMS_RECEIVED".equals(broadcastRecord.intent.getAction())) {
                HwSystemManager.insertSendBroadcastRecord(broadcastFilter.packageName, broadcastRecord.intent.getAction(), broadcastFilter.receiverList.uid);
            }
        }
        if (!skip && ((broadcastRecord.requiredPermissions == null || broadcastRecord.requiredPermissions.length == 0) && this.mService.checkComponentPermission(null, broadcastFilter.receiverList.pid, broadcastFilter.receiverList.uid, -1, true) != 0)) {
            Slog.w("BroadcastQueue", "Permission Denial: security check failed when receiving " + broadcastRecord.intent.toString() + " to " + broadcastFilter.receiverList.app + " (pid=" + broadcastFilter.receiverList.pid + ", uid=" + broadcastFilter.receiverList.uid + ") due to sender " + broadcastRecord.callerPackage + " (uid " + broadcastRecord.callingUid + ")");
            skip = true;
        }
        if (!(skip || broadcastRecord.appOp == -1 || this.mService.mAppOpsService.noteOperation(broadcastRecord.appOp, broadcastFilter.receiverList.uid, broadcastFilter.packageName) == 0)) {
            Slog.w("BroadcastQueue", "Appop Denial: receiving " + broadcastRecord.intent.toString() + " to " + broadcastFilter.receiverList.app + " (pid=" + broadcastFilter.receiverList.pid + ", uid=" + broadcastFilter.receiverList.uid + ") requires appop " + AppOpsManager.opToName(broadcastRecord.appOp) + " due to sender " + broadcastRecord.callerPackage + " (uid " + broadcastRecord.callingUid + ")");
            skip = true;
        }
        if (!this.mService.mIntentFirewall.checkBroadcast(broadcastRecord.intent, broadcastRecord.callingUid, broadcastRecord.callingPid, broadcastRecord.resolvedType, broadcastFilter.receiverList.uid)) {
            skip = true;
        }
        if (!skip && this.mService.mHwAMSEx.shouldPreventSendBroadcast(broadcastRecord.intent, broadcastFilter.packageName, broadcastRecord.callingUid, broadcastRecord.callingPid, broadcastRecord.callerPackage, broadcastRecord.userId)) {
            skip = true;
        }
        if (!skip && (broadcastFilter.receiverList.app == null || broadcastFilter.receiverList.app.killed || broadcastFilter.receiverList.app.crashing)) {
            Slog.w("BroadcastQueue", "Skipping deliver [" + this.mQueueName + "] " + broadcastRecord + " to " + broadcastFilter.receiverList + ": process gone or crashing");
            skip = true;
        }
        boolean visibleToInstantApps = (broadcastRecord.intent.getFlags() & DumpState.DUMP_COMPILER_STATS) != 0;
        if (!skip && !visibleToInstantApps && broadcastFilter.instantApp && broadcastFilter.receiverList.uid != broadcastRecord.callingUid) {
            Slog.w("BroadcastQueue", "Instant App Denial: receiving " + broadcastRecord.intent.toString() + " to " + broadcastFilter.receiverList.app + " (pid=" + broadcastFilter.receiverList.pid + ", uid=" + broadcastFilter.receiverList.uid + ") due to sender " + broadcastRecord.callerPackage + " (uid " + broadcastRecord.callingUid + ") not specifying FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS");
            skip = true;
        }
        if (!skip && !broadcastFilter.visibleToInstantApp && broadcastRecord.callerInstantApp && broadcastFilter.receiverList.uid != broadcastRecord.callingUid) {
            Slog.w("BroadcastQueue", "Instant App Denial: receiving " + broadcastRecord.intent.toString() + " to " + broadcastFilter.receiverList.app + " (pid=" + broadcastFilter.receiverList.pid + ", uid=" + broadcastFilter.receiverList.uid + ") requires receiver be visible to instant apps due to sender " + broadcastRecord.callerPackage + " (uid " + broadcastRecord.callingUid + ")");
            skip = true;
        }
        if (skip) {
            broadcastRecord.delivery[index] = 2;
        } else if (this.mService.mPermissionReviewRequired && !requestStartTargetPermissionsReviewIfNeededLocked(broadcastRecord, broadcastFilter.packageName, broadcastFilter.owningUserId)) {
            broadcastRecord.delivery[index] = 2;
        } else if (!getMtmBRManagerEnabled(10) || !getMtmBRManager().iawareProcessBroadcast(0, !ordered, broadcastRecord, broadcastFilter)) {
            broadcastRecord.delivery[index] = 1;
            if (ordered) {
                broadcastRecord.receiver = broadcastFilter.receiverList.receiver.asBinder();
                broadcastRecord.curFilter = broadcastFilter;
                broadcastFilter.receiverList.curBroadcast = broadcastRecord;
                broadcastRecord.state = 2;
                if (broadcastFilter.receiverList.app != null) {
                    broadcastRecord.curApp = broadcastFilter.receiverList.app;
                    broadcastFilter.receiverList.app.curReceivers.add(broadcastRecord);
                    this.mService.updateOomAdjLocked(broadcastRecord.curApp, true);
                }
            }
            try {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.i("BroadcastQueue", "Delivering to " + broadcastFilter + " : " + broadcastRecord);
                }
                if (broadcastFilter.receiverList.app == null || !broadcastFilter.receiverList.app.inFullBackup) {
                    performReceiveLocked(broadcastFilter.receiverList.app, broadcastFilter.receiverList.receiver, new Intent(broadcastRecord.intent), broadcastRecord.resultCode, broadcastRecord.resultData, broadcastRecord.resultExtras, broadcastRecord.ordered, broadcastRecord.initialSticky, broadcastRecord.userId);
                } else if (ordered) {
                    skipReceiverLocked(r);
                }
                if (ordered) {
                    broadcastRecord.state = 3;
                }
            } catch (RemoteException e) {
                Slog.w("BroadcastQueue", "Failure sending broadcast " + broadcastRecord.intent, e);
                if (ordered) {
                    broadcastRecord.receiver = null;
                    broadcastRecord.curFilter = null;
                    broadcastFilter.receiverList.curBroadcast = null;
                    if (broadcastFilter.receiverList.app != null) {
                        broadcastFilter.receiverList.app.curReceivers.remove(broadcastRecord);
                    }
                }
            }
        }
    }

    private boolean requestStartTargetPermissionsReviewIfNeededLocked(BroadcastRecord receiverRecord, String receivingPackageName, int receivingUserId) {
        BroadcastRecord broadcastRecord = receiverRecord;
        String str = receivingPackageName;
        final int i = receivingUserId;
        if (!this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(str, i)) {
            return true;
        }
        if (!(broadcastRecord.callerApp == null || broadcastRecord.callerApp.setSchedGroup != 0) || broadcastRecord.intent.getComponent() == null) {
            Slog.w("BroadcastQueue", "u" + i + " Receiving a broadcast in package" + str + " requires a permissions review");
        } else {
            IIntentSender target = this.mService.getIntentSenderLocked(1, broadcastRecord.callerPackage, broadcastRecord.callingUid, broadcastRecord.userId, null, null, 0, new Intent[]{broadcastRecord.intent}, new String[]{broadcastRecord.intent.resolveType(this.mService.mContext.getContentResolver())}, 1409286144, null);
            final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent.addFlags(276824064);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", str);
            intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                Slog.i("BroadcastQueue", "u" + i + " Launching permission review for package " + str);
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    BroadcastQueue.this.mService.mContext.startActivityAsUser(intent, new UserHandle(i));
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
        int i = perms.length - 1;
        while (i >= 0) {
            try {
                PermissionInfo pi = pm.getPermissionInfo(perms[i], PackageManagerService.PLATFORM_PACKAGE_NAME, 0);
                if (pi == null || (pi.protectionLevel & 31) != 2) {
                    return false;
                }
                i--;
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
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:382:0x0e79, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:383:0x0e7a, code lost:
        r29 = r3;
        r30 = r5;
        r12 = r6;
        r13 = r7;
        r18 = r40;
        r44 = r41;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:384:0x0e86, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:385:0x0e87, code lost:
        r12 = r48;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x049e  */
    /* JADX WARNING: Removed duplicated region for block: B:382:0x0e79 A[ExcHandler: RemoteException (e android.os.RemoteException), Splitter:B:375:0x0e61] */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e86 A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:373:0x0e5f] */
    /* JADX WARNING: Removed duplicated region for block: B:394:0x0f14 A[SYNTHETIC, Splitter:B:394:0x0f14] */
    /* JADX WARNING: Removed duplicated region for block: B:406:0x0f8b  */
    /* JADX WARNING: Removed duplicated region for block: B:407:0x0fb6  */
    /* JADX WARNING: Removed duplicated region for block: B:410:0x0fc0  */
    /* JADX WARNING: Removed duplicated region for block: B:413:0x0fcd  */
    /* JADX WARNING: Removed duplicated region for block: B:416:0x0fed  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x0ff0  */
    /* JADX WARNING: Removed duplicated region for block: B:420:0x1006  */
    /* JADX WARNING: Removed duplicated region for block: B:422:0x1054  */
    /* JADX WARNING: Removed duplicated region for block: B:424:0x105b A[LOOP:2: B:72:0x023a->B:424:0x105b, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:437:0x04fe A[SYNTHETIC] */
    public final void processNextBroadcastLocked(boolean fromMsg, boolean skipOomAdj) {
        IIntentReceiver iIntentReceiver;
        boolean skip;
        Object nextReceiver;
        String targetProcess;
        String targetProcess2;
        int receiverUid;
        ResolveInfo info;
        String targetProcess3;
        boolean z;
        ProcessRecord startProcessLocked;
        int i;
        int perm;
        BroadcastRecord r;
        ProcessRecord proc;
        ProcessRecord processRecord;
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v("BroadcastQueue", "processNextBroadcast [" + this.mQueueName + "]: " + this.mParallelBroadcasts.size() + " parallel broadcasts, " + this.mOrderedBroadcasts.size() + " ordered broadcasts");
        }
        this.mService.updateCpuStats();
        if (fromMsg) {
            if (!this.mBroadcastsScheduled) {
                Slog.e("BroadcastQueue", "processNextBroadcast before mBroadcastsScheduled is set true", new RuntimeException("here").fillInStackTrace());
            }
            this.mBroadcastsScheduled = false;
        }
        while (this.mParallelBroadcasts.size() > 0) {
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
                getMtmBRManager().iawareStartCountBroadcastSpeed(true, r2);
            }
            for (int i2 = 0; i2 < N; i2++) {
                Object target = r2.receivers.get(i2);
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v("BroadcastQueue", "Delivering non-ordered on [" + this.mQueueName + "] to registered " + target + ": " + r2);
                }
                if (!enqueueProxyBroadcast(true, r2, target)) {
                    deliverToRegisteredReceiverLocked(r2, (BroadcastFilter) target, false, i2);
                } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v("BroadcastQueue", "parallel " + this.mQueueName + " broadcast:(" + r2 + ") should be proxyed, target:(" + target + ")");
                }
            }
            if (getMtmBRManagerEnabled(10)) {
                getMtmBRManager().iawareEndCountBroadcastSpeed(r2);
            }
            addBroadcastToHistoryLocked(r2);
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                Slog.v("BroadcastQueue", "Done with parallel broadcast [" + this.mQueueName + "] " + r2);
            }
        }
        if (this.mPendingBroadcast != null) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                Slog.v("BroadcastQueue", "processNextBroadcast [" + this.mQueueName + "]: waiting for " + this.mPendingBroadcast.curApp);
            }
            if (this.mPendingBroadcast.curApp.pid > 0) {
                synchronized (this.mService.mPidsSelfLocked) {
                    ProcessRecord proc2 = this.mService.mPidsSelfLocked.get(this.mPendingBroadcast.curApp.pid);
                    if (proc2 != null) {
                        if (!proc2.crashing) {
                            processRecord = null;
                            proc = processRecord;
                        }
                    }
                    processRecord = 1;
                    proc = processRecord;
                }
            } else {
                ProcessRecord proc3 = (ProcessRecord) this.mService.mProcessNames.get(this.mPendingBroadcast.curApp.processName, this.mPendingBroadcast.curApp.uid);
                proc = (proc3 == null || !proc3.pendingStart) ? 1 : null;
            }
            if (proc != null) {
                Slog.w("BroadcastQueue", "pending app  [" + this.mQueueName + "]" + this.mPendingBroadcast.curApp + " died before responding to broadcast");
                this.mPendingBroadcast.state = 0;
                this.mPendingBroadcast.nextReceiver = this.mPendingBroadcastRecvIndex;
                this.mPendingBroadcast = null;
            } else {
                return;
            }
        }
        boolean looped = false;
        while (true) {
            boolean looped2 = looped;
            if (this.mOrderedBroadcasts.size() == 0) {
                this.mService.scheduleAppGcsLocked();
                if (looped2) {
                    this.mService.updateOomAdjLocked();
                }
                return;
            }
            BroadcastRecord r3 = this.mOrderedBroadcasts.get(0);
            boolean forceReceive = false;
            int numReceivers = r3.receivers != null ? r3.receivers.size() : 0;
            if (this.mService.mProcessesReady && r3.dispatchTime > 0) {
                long now = SystemClock.uptimeMillis();
                if (r3.anrCount > 0) {
                    Slog.w("BroadcastQueue", "intentAction=" + r3.intent.getAction() + "dispatchTime=" + r3.dispatchTime + " numReceivers=" + numReceivers + " nextReceiver=" + r3.nextReceiver + " now=" + now + " state=" + r3.state + " curReceiver  = " + r3.curReceiver);
                }
                if (numReceivers > 0 && now > r3.dispatchTime + (2 * this.mTimeoutPeriod * ((long) numReceivers))) {
                    Slog.w("BroadcastQueue", "Hung broadcast [" + this.mQueueName + "] discarded after timeout failure: now=" + now + " dispatchTime=" + r3.dispatchTime + " startTime=" + r3.receiverTime + " intent=" + r3.intent + " numReceivers=" + numReceivers + " nextReceiver=" + r3.nextReceiver + " state=" + r3.state);
                    broadcastTimeoutLocked(false);
                    forceReceive = true;
                    r3.state = 0;
                }
                if (r3.anrCount > 0 && numReceivers == 0 && now > r3.dispatchTime && r3.curReceiver != null && this == r3.queue) {
                    Slog.w("BroadcastQueue", " dispatchTime=" + r3.dispatchTime + " numReceivers=" + numReceivers + " nextReceiver=" + r3.nextReceiver + " now=" + now + " state=" + r3.state + " curReceiver  = " + r3.curReceiver + "finish curReceiver");
                    logBroadcastReceiverDiscardLocked(r3);
                    long j = now;
                    int i3 = numReceivers;
                    finishReceiverLocked(r3, r3.resultCode, r3.resultData, r3.resultExtras, r3.resultAbort, false);
                    scheduleBroadcastsLocked();
                    r3.state = 0;
                    return;
                }
            }
            int numReceivers2 = numReceivers;
            boolean forceReceive2 = forceReceive;
            if (r3.state != 0) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.d("BroadcastQueue", "processNextBroadcast(" + this.mQueueName + ") called when not idle (state=" + r3.state + ")");
                }
                return;
            }
            if (r3.receivers == null || r3.nextReceiver >= numReceivers2 || r3.resultAbort || forceReceive2) {
                if (r3.resultTo != null) {
                    try {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            try {
                                Slog.i("BroadcastQueue", "Finishing broadcast [" + this.mQueueName + "] " + r3.intent.getAction() + " app=" + r3.callerApp);
                            } catch (RemoteException e) {
                                e = e;
                                r = r3;
                                int i4 = numReceivers2;
                                iIntentReceiver = null;
                            }
                        }
                        r = r3;
                        iIntentReceiver = null;
                        int i5 = numReceivers2;
                        try {
                            performReceiveLocked(r3.callerApp, r3.resultTo, new Intent(r3.intent), r3.resultCode, r3.resultData, r3.resultExtras, false, false, r3.userId);
                            r.resultTo = null;
                        } catch (RemoteException e2) {
                            e = e2;
                        }
                    } catch (RemoteException e3) {
                        e = e3;
                        r = r3;
                        int i6 = numReceivers2;
                        iIntentReceiver = null;
                        r.resultTo = iIntentReceiver;
                        Slog.w("BroadcastQueue", "Failure [" + this.mQueueName + "] sending broadcast result of " + r.intent, e);
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        }
                        cancelBroadcastTimeoutLocked();
                        Slog.v("BroadcastQueue", "Finished with ordered broadcast " + r);
                        addBroadcastToHistoryLocked(r);
                        this.mService.addBroadcastStatLocked(r.intent.getAction(), r.callerPackage, r.manifestCount, r.manifestSkipCount, r.finishTime - r.dispatchTime);
                        this.mOrderedBroadcasts.remove(0);
                        r3 = null;
                        looped2 = true;
                        if (r3 == null) {
                        }
                    }
                } else {
                    r = r3;
                    int i7 = numReceivers2;
                    iIntentReceiver = null;
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
                this.mOrderedBroadcasts.remove(0);
                r3 = null;
                looped2 = true;
            } else {
                iIntentReceiver = null;
            }
            if (r3 == null) {
                int i8 = r3.nextReceiver;
                r3.nextReceiver = i8 + 1;
                int recIdx = i8;
                Object target2 = r3.receivers.get(recIdx);
                if (enqueueProxyBroadcast(false, r3, target2)) {
                    Flog.i(104, "orderd " + this.mQueueName + " broadcast:(" + r3 + ") should be proxyed, target:(" + target2 + ")");
                    scheduleBroadcastsLocked();
                    return;
                }
                reportMediaButtonToAware(r3, target2);
                r3.receiverTime = SystemClock.uptimeMillis();
                if (recIdx == 0) {
                    r3.dispatchTime = r3.receiverTime;
                    r3.dispatchClockTime = System.currentTimeMillis();
                    Flog.i(104, "dispatch ordered broadcast [" + this.mQueueName + "] " + r3 + " enqueued " + (r3.dispatchClockTime - r3.enqueueClockTime) + " ms ago, has " + r3.receivers.size() + " receivers");
                    updateSRMSStatisticsData(r3);
                    if (Trace.isTagEnabled(64)) {
                        Trace.asyncTraceEnd(64, createBroadcastTraceTitle(r3, 0), System.identityHashCode(r3));
                        Trace.asyncTraceBegin(64, createBroadcastTraceTitle(r3, 1), System.identityHashCode(r3));
                    }
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                        Slog.v("BroadcastQueue", "Processing ordered broadcast [" + this.mQueueName + "] " + r3);
                    }
                }
                if (!this.mPendingBroadcastTimeoutMessage) {
                    long timeoutTime = r3.receiverTime + this.mTimeoutPeriod;
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v("BroadcastQueue", "Submitting BROADCAST_TIMEOUT_MSG [" + this.mQueueName + "] for " + r3 + " at " + timeoutTime);
                    }
                    setBroadcastTimeoutLocked(timeoutTime);
                }
                BroadcastOptions brOptions = r3.options;
                Object nextReceiver2 = r3.receivers.get(recIdx);
                if (nextReceiver2 instanceof BroadcastFilter) {
                    BroadcastFilter filter = (BroadcastFilter) nextReceiver2;
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v("BroadcastQueue", "Delivering ordered [" + this.mQueueName + "] to registered " + filter + ": " + r3);
                    }
                    deliverToRegisteredReceiverLocked(r3, filter, r3.ordered, recIdx);
                    if (r3.receiver == null || !r3.ordered) {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v("BroadcastQueue", "Quick finishing [" + this.mQueueName + "]: ordered=" + r3.ordered + " receiver=" + r3.receiver);
                        }
                        r3.state = 0;
                        scheduleBroadcastsLocked();
                    } else if (brOptions != null && brOptions.getTemporaryAppWhitelistDuration() > 0) {
                        scheduleTempWhitelistLocked(filter.owningUid, brOptions.getTemporaryAppWhitelistDuration(), r3);
                    }
                    return;
                }
                ResolveInfo info2 = (ResolveInfo) nextReceiver2;
                ComponentName component = new ComponentName(info2.activityInfo.applicationInfo.packageName, info2.activityInfo.name);
                boolean skip2 = false;
                if (brOptions != null && (info2.activityInfo.applicationInfo.targetSdkVersion < brOptions.getMinManifestReceiverApiLevel() || info2.activityInfo.applicationInfo.targetSdkVersion > brOptions.getMaxManifestReceiverApiLevel())) {
                    skip2 = true;
                }
                int perm2 = this.mService.checkComponentPermission(info2.activityInfo.permission, r3.callingPid, r3.callingUid, info2.activityInfo.applicationInfo.uid, info2.activityInfo.exported);
                if (!skip2 && perm2 != 0) {
                    if (!info2.activityInfo.exported) {
                        Slog.w("BroadcastQueue", "Permission Denial: broadcasting " + r3.intent.toString() + " from " + r3.callerPackage + " (pid=" + r3.callingPid + ", uid=" + r3.callingUid + ") is not exported from uid " + info2.activityInfo.applicationInfo.uid + " due to receiver " + component.flattenToShortString());
                    } else {
                        Slog.w("BroadcastQueue", "Permission Denial: broadcasting " + r3.intent.toString() + " from " + r3.callerPackage + " (pid=" + r3.callingPid + ", uid=" + r3.callingUid + ") requires " + info2.activityInfo.permission + " due to receiver " + component.flattenToShortString());
                    }
                    skip2 = true;
                } else if (!skip2 && info2.activityInfo.permission != null) {
                    int opCode = AppOpsManager.permissionToOpCode(info2.activityInfo.permission);
                    if (!(opCode == -1 || this.mService.mAppOpsService.noteOperation(opCode, r3.callingUid, r3.callerPackage) == 0)) {
                        Slog.w("BroadcastQueue", "Appop Denial: broadcasting " + r3.intent.toString() + " from " + r3.callerPackage + " (pid=" + r3.callingPid + ", uid=" + r3.callingUid + ") requires appop " + AppOpsManager.permissionToOp(info2.activityInfo.permission) + " due to registered receiver " + component.flattenToShortString());
                        skip2 = true;
                    }
                }
                boolean skip3 = skip2;
                if (!skip3 && info2.activityInfo.applicationInfo.uid != 1000 && r3.requiredPermissions != null && r3.requiredPermissions.length > 0) {
                    int i9 = 0;
                    while (true) {
                        int i10 = i9;
                        if (i10 >= r3.requiredPermissions.length) {
                            break;
                        }
                        String requiredPermission = r3.requiredPermissions[i10];
                        try {
                            i = AppGlobals.getPackageManager().checkPermission(requiredPermission, info2.activityInfo.applicationInfo.packageName, UserHandle.getUserId(info2.activityInfo.applicationInfo.uid));
                        } catch (RemoteException e4) {
                            i = -1;
                        }
                        perm2 = i;
                        if (perm2 != 0) {
                            Slog.w("BroadcastQueue", "Permission Denial: receiving " + r3.intent + " to " + component.flattenToShortString() + " requires " + requiredPermission + " due to sender " + r3.callerPackage + " (uid " + r3.callingUid + ")");
                            skip3 = true;
                            break;
                        }
                        int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                        if (appOp == -1 || appOp == r3.appOp) {
                            perm = perm2;
                        } else {
                            perm = perm2;
                            if (this.mService.mAppOpsService.noteOperation(appOp, info2.activityInfo.applicationInfo.uid, info2.activityInfo.packageName) != 0) {
                                Slog.w("BroadcastQueue", "Appop Denial: receiving " + r3.intent + " to " + component.flattenToShortString() + " requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r3.callerPackage + " (uid " + r3.callingUid + ")");
                                skip3 = true;
                                perm2 = perm;
                                break;
                            }
                        }
                        i9 = i10 + 1;
                        perm2 = perm;
                    }
                    if (r3.intent != null && "android.provider.Telephony.SMS_RECEIVED".equals(r3.intent.getAction())) {
                        HwSystemManager.insertSendBroadcastRecord(info2.activityInfo.applicationInfo.packageName, r3.intent.getAction(), info2.activityInfo.applicationInfo.uid);
                    }
                }
                int perm3 = perm2;
                if (!(skip3 || r3.appOp == -1 || this.mService.mAppOpsService.noteOperation(r3.appOp, info2.activityInfo.applicationInfo.uid, info2.activityInfo.packageName) == 0)) {
                    Slog.w("BroadcastQueue", "Appop Denial: receiving " + r3.intent + " to " + component.flattenToShortString() + " requires appop " + AppOpsManager.opToName(r3.appOp) + " due to sender " + r3.callerPackage + " (uid " + r3.callingUid + ")");
                    skip3 = true;
                }
                if (!skip3) {
                    boolean z2 = skip3;
                    skip = !this.mService.mIntentFirewall.checkBroadcast(r3.intent, r3.callingUid, r3.callingPid, r3.resolvedType, info2.activityInfo.applicationInfo.uid);
                } else {
                    skip = skip3;
                }
                if (!skip) {
                    nextReceiver = nextReceiver2;
                    if (this.mService.mHwAMSEx.shouldPreventSendBroadcast(r3.intent, info2.activityInfo.packageName, r3.callingUid, r3.callingPid, r3.callerPackage, r3.userId)) {
                        skip = true;
                    }
                } else {
                    nextReceiver = nextReceiver2;
                }
                boolean isSingleton = false;
                try {
                    isSingleton = this.mService.isSingleton(info2.activityInfo.processName, info2.activityInfo.applicationInfo, info2.activityInfo.name, info2.activityInfo.flags);
                } catch (SecurityException e5) {
                    Slog.w("BroadcastQueue", e5.getMessage());
                    skip = true;
                }
                boolean isSingleton2 = isSingleton;
                if (!((info2.activityInfo.flags & 1073741824) == 0 || ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS", info2.activityInfo.applicationInfo.uid) == 0)) {
                    Slog.w("BroadcastQueue", "Permission Denial: Receiver " + component.flattenToShortString() + " requests FLAG_SINGLE_USER, but app does not hold " + "android.permission.INTERACT_ACROSS_USERS");
                    skip = true;
                }
                if (!skip && info2.activityInfo.applicationInfo.isInstantApp() && r3.callingUid != info2.activityInfo.applicationInfo.uid) {
                    Slog.w("BroadcastQueue", "Instant App Denial: receiving " + r3.intent + " to " + component.flattenToShortString() + " due to sender " + r3.callerPackage + " (uid " + r3.callingUid + ") Instant Apps do not support manifest receivers");
                    skip = true;
                }
                if (!skip && r3.callerInstantApp && (info2.activityInfo.flags & DumpState.DUMP_DEXOPT) == 0 && r3.callingUid != info2.activityInfo.applicationInfo.uid) {
                    Slog.w("BroadcastQueue", "Instant App Denial: receiving " + r3.intent + " to " + component.flattenToShortString() + " requires receiver have visibleToInstantApps set due to sender " + r3.callerPackage + " (uid " + r3.callingUid + ")");
                    skip = true;
                }
                if (r3.curApp != null && r3.curApp.crashing) {
                    Slog.w("BroadcastQueue", "Skipping deliver ordered [" + this.mQueueName + "] " + r3 + " to " + r3.curApp + ": process crashing");
                    skip = true;
                }
                if (!skip) {
                    boolean isAvailable = false;
                    try {
                        isAvailable = AppGlobals.getPackageManager().isPackageAvailable(info2.activityInfo.packageName, UserHandle.getUserId(info2.activityInfo.applicationInfo.uid));
                    } catch (Exception e6) {
                        Slog.w("BroadcastQueue", "Exception getting recipient info for " + info2.activityInfo.packageName, e6);
                    }
                    if (!isAvailable) {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v("BroadcastQueue", "Skipping delivery to " + info2.activityInfo.packageName + " / " + info2.activityInfo.applicationInfo.uid + " : package no longer available");
                        }
                        skip = true;
                    }
                }
                if (HwServiceFactory.getHwNLPManager().shouldSkipGoogleNlp(r3.intent, info2.activityInfo.processName)) {
                    skip = true;
                }
                if (this.mService.mPermissionReviewRequired && !skip && !requestStartTargetPermissionsReviewIfNeededLocked(r3, info2.activityInfo.packageName, UserHandle.getUserId(info2.activityInfo.applicationInfo.uid))) {
                    skip = true;
                }
                int receiverUid2 = info2.activityInfo.applicationInfo.uid;
                if (r3.callingUid != 1000 && isSingleton2 && this.mService.isValidSingletonCall(r3.callingUid, receiverUid2)) {
                    info2.activityInfo = this.mService.getActivityInfoForUser(info2.activityInfo, 0);
                }
                String targetProcess4 = info2.activityInfo.processName;
                ProcessRecord app = this.mService.getProcessRecordLocked(targetProcess4, info2.activityInfo.applicationInfo.uid, false);
                if (!skip) {
                    targetProcess = targetProcess4;
                    int allowed = this.mService.getAppStartModeLocked(info2.activityInfo.applicationInfo.uid, info2.activityInfo.packageName, info2.activityInfo.applicationInfo.targetSdkVersion, -1, true, false, false);
                    if (allowed != 0) {
                        if (allowed == 3) {
                            Slog.w("BroadcastQueue", "Background execution disabled: receiving " + r3.intent + " to " + component.flattenToShortString());
                            skip = true;
                        } else if ((r3.intent.getFlags() & DumpState.DUMP_VOLUMES) != 0 || (r3.intent.getComponent() == null && r3.intent.getPackage() == null && (r3.intent.getFlags() & DumpState.DUMP_SERVICE_PERMISSIONS) == 0 && !this.mService.isExcludedInBGCheck(component.getPackageName(), null) && !isSignaturePerm(r3.requiredPermissions))) {
                            this.mService.addBackgroundCheckViolationLocked(r3.intent.getAction(), component.getPackageName());
                            Slog.w("BroadcastQueue", "Background execution not allowed: receiving " + r3.intent + " to " + component.flattenToShortString());
                            skip = true;
                        }
                    }
                } else {
                    targetProcess = targetProcess4;
                }
                if (!skip && !"android.intent.action.ACTION_SHUTDOWN".equals(r3.intent.getAction()) && !this.mService.mUserController.isUserRunning(UserHandle.getUserId(info2.activityInfo.applicationInfo.uid), 0)) {
                    skip = true;
                    Slog.w("BroadcastQueue", "Skipping delivery to " + info2.activityInfo.packageName + " / " + info2.activityInfo.applicationInfo.uid + " : user is not running");
                }
                if (!skip) {
                    int i11 = perm3;
                    if (this.mService.shouldPreventSendReceiver(r3.intent, info2, r3.callingPid, r3.callingUid, app, r3.callerApp)) {
                        skip = true;
                    }
                }
                if (skip) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v("BroadcastQueue", "Skipping delivery of ordered [" + this.mQueueName + "] " + r3 + " for whatever reason");
                    }
                    r3.delivery[recIdx] = 2;
                    r3.receiver = null;
                    r3.curFilter = null;
                    r3.state = 0;
                    r3.manifestSkipCount++;
                    scheduleBroadcastsLocked();
                    return;
                }
                r3.manifestCount++;
                r3.delivery[recIdx] = 1;
                r3.state = 1;
                r3.curComponent = component;
                r3.curReceiver = info2.activityInfo;
                if (ActivityManagerDebugConfig.DEBUG_MU && r3.callingUid > 100000) {
                    Slog.v(TAG_MU, "Updated broadcast record activity info for secondary user, " + info2.activityInfo + ", callingUid = " + r3.callingUid + ", uid = " + receiverUid2);
                }
                if (brOptions != null && brOptions.getTemporaryAppWhitelistDuration() > 0) {
                    scheduleTempWhitelistLocked(receiverUid2, brOptions.getTemporaryAppWhitelistDuration(), r3);
                }
                try {
                    AppGlobals.getPackageManager().setPackageStoppedState(r3.curComponent.getPackageName(), false, UserHandle.getUserId(r3.callingUid));
                } catch (RemoteException e7) {
                } catch (IllegalArgumentException e8) {
                    Slog.w("BroadcastQueue", "Failed trying to unstop package " + r3.curComponent.getPackageName() + ": " + e8);
                }
                if (app == null || app.thread == null || app.killed) {
                    ComponentName componentName = component;
                    info = info2;
                    receiverUid = receiverUid2;
                    Object obj = nextReceiver;
                    targetProcess2 = targetProcess;
                } else {
                    try {
                        try {
                            app.addPackage(info2.activityInfo.packageName, (long) info2.activityInfo.applicationInfo.versionCode, this.mService.mProcessStats);
                            processCurBroadcastLocked(r3, app, skipOomAdj);
                            return;
                        } catch (RemoteException e9) {
                        } catch (RuntimeException e10) {
                            e = e10;
                            Slog.wtf("BroadcastQueue", "Failed sending broadcast to " + r3.curComponent + " with " + r3.intent, e);
                            logBroadcastReceiverDiscardLocked(r3);
                            RuntimeException runtimeException = e;
                            ProcessRecord processRecord2 = app;
                            String str = targetProcess;
                            ComponentName componentName2 = component;
                            ResolveInfo resolveInfo = info2;
                            int i12 = receiverUid2;
                            Object obj2 = nextReceiver;
                            finishReceiverLocked(r3, r3.resultCode, r3.resultData, r3.resultExtras, r3.resultAbort, false);
                            scheduleBroadcastsLocked();
                            r3.state = 0;
                            return;
                        }
                    } catch (RemoteException e11) {
                        e = e11;
                        ProcessRecord processRecord3 = app;
                        ComponentName componentName3 = component;
                        info = info2;
                        receiverUid = receiverUid2;
                        Object obj3 = nextReceiver;
                        targetProcess2 = targetProcess;
                        Slog.w("BroadcastQueue", "Exception when sending broadcast to " + r3.curComponent, e);
                        if (this.mService.mUserController.getCurrentUserId() == 0) {
                        }
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        }
                        if (getMtmBRManagerEnabled(11)) {
                        }
                        startProcessLocked = this.mService.startProcessLocked(targetProcess3, info.activityInfo.applicationInfo, true, r3.intent.getFlags() | 4, "broadcast", r3.curComponent, (r3.intent.getFlags() & DumpState.DUMP_HANDLE) != 0 ? z : false, false, false);
                        r3.curApp = startProcessLocked;
                        if (startProcessLocked == null) {
                        }
                    } catch (RuntimeException e12) {
                    }
                }
                if (this.mService.mUserController.getCurrentUserId() == 0) {
                    try {
                        String packageName = info.activityInfo.applicationInfo.packageName;
                        String sourceDir = info.activityInfo.applicationInfo.sourceDir;
                        String publicSourceDir = info.activityInfo.applicationInfo.publicSourceDir;
                        ApplicationInfo applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                        if (applicationInfo != null) {
                            if (!applicationInfo.sourceDir.equals(sourceDir) || !applicationInfo.publicSourceDir.equals(publicSourceDir)) {
                                Slog.e("BroadcastQueue", packageName + " is replaced, sourceDir is changed from " + sourceDir + " to " + applicationInfo.sourceDir + ", publicSourceDir is changed from " + publicSourceDir + " to " + applicationInfo.publicSourceDir);
                                info.activityInfo.applicationInfo = applicationInfo;
                            }
                        } else {
                            return;
                        }
                    } catch (RemoteException e13) {
                    }
                }
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Need to start app [");
                    sb.append(this.mQueueName);
                    sb.append("] ");
                    targetProcess3 = targetProcess2;
                    sb.append(targetProcess3);
                    sb.append(" for broadcast ");
                    sb.append(r3);
                    Slog.v("BroadcastQueue", sb.toString());
                } else {
                    targetProcess3 = targetProcess2;
                }
                if (getMtmBRManagerEnabled(11)) {
                    z = true;
                    if (getMtmBRManager().iawareProcessBroadcast(1, false, r3, target2)) {
                        return;
                    }
                } else {
                    z = true;
                }
                startProcessLocked = this.mService.startProcessLocked(targetProcess3, info.activityInfo.applicationInfo, true, r3.intent.getFlags() | 4, "broadcast", r3.curComponent, (r3.intent.getFlags() & DumpState.DUMP_HANDLE) != 0 ? z : false, false, false);
                r3.curApp = startProcessLocked;
                if (startProcessLocked == null) {
                    Slog.w("BroadcastQueue", "Unable to launch app " + info.activityInfo.applicationInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + receiverUid + " for broadcast " + r3.intent + ": process is bad");
                    logBroadcastReceiverDiscardLocked(r3);
                    String str2 = targetProcess3;
                    finishReceiverLocked(r3, r3.resultCode, r3.resultData, r3.resultExtras, r3.resultAbort, false);
                    scheduleBroadcastsLocked();
                    r3.state = 0;
                    return;
                }
                this.mPendingBroadcast = r3;
                this.mPendingBroadcastRecvIndex = recIdx;
                return;
            }
            IIntentReceiver iIntentReceiver2 = iIntentReceiver;
            looped = looped2;
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
        boolean z = false;
        if (fromMsg) {
            this.mPendingBroadcastTimeoutMessage = false;
        }
        if (this.mOrderedBroadcasts.size() != 0) {
            long now = SystemClock.uptimeMillis();
            BroadcastRecord r = this.mOrderedBroadcasts.get(0);
            if (fromMsg) {
                if (this.mService.mProcessesReady) {
                    long timeoutTime = r.receiverTime + this.mTimeoutPeriod;
                    if (timeoutTime > now) {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v("BroadcastQueue", "Premature timeout [" + this.mQueueName + "] @ " + now + ": resetting BROADCAST_TIMEOUT_MSG for " + timeoutTime);
                        }
                        setBroadcastTimeoutLocked(timeoutTime);
                        return;
                    }
                } else {
                    return;
                }
            }
            BroadcastRecord br = this.mOrderedBroadcasts.get(0);
            if (br.state == 4) {
                StringBuilder sb = new StringBuilder();
                sb.append("Waited long enough for: ");
                sb.append(br.curComponent != null ? br.curComponent.flattenToShortString() : "(null)");
                Slog.i("BroadcastQueue", sb.toString());
                br.curComponent = null;
                br.state = 0;
                processNextBroadcast(false);
                return;
            }
            if (r.curApp != null && r.curApp.debugging) {
                z = true;
            }
            boolean debugging = z;
            Slog.w("BroadcastQueue", "Timeout of broadcast " + r + " - receiver=" + r.receiver + ", started " + (now - r.receiverTime) + "ms ago");
            r.receiverTime = now;
            if (!debugging) {
                r.anrCount++;
            }
            ProcessRecord app = null;
            String anrMessage = null;
            if (r.nextReceiver > 0) {
                curReceiver = r.receivers.get(r.nextReceiver - 1);
                r.delivery[r.nextReceiver - 1] = 3;
            } else {
                curReceiver = r.curReceiver;
            }
            Object curReceiver2 = curReceiver;
            Slog.w("BroadcastQueue", "Receiver during timeout of " + r + " : " + curReceiver2);
            logBroadcastReceiverDiscardLocked(r);
            if (curReceiver2 == null || !(curReceiver2 instanceof BroadcastFilter)) {
                app = r.curApp;
            } else {
                BroadcastFilter bf = (BroadcastFilter) curReceiver2;
                if (!(bf.receiverList.pid == 0 || bf.receiverList.pid == ActivityManagerService.MY_PID)) {
                    synchronized (this.mService.mPidsSelfLocked) {
                        app = this.mService.mPidsSelfLocked.get(bf.receiverList.pid);
                    }
                }
            }
            ProcessRecord app2 = app;
            if (app2 != null) {
                anrMessage = "Broadcast of " + r.intent.toString();
            }
            String anrMessage2 = anrMessage;
            if (this.mPendingBroadcast == r) {
                this.mPendingBroadcast = null;
            }
            String anrMessage3 = anrMessage2;
            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
            scheduleBroadcastsLocked();
            if (anrMessage3 == null || !"android.intent.action.PRE_BOOT_COMPLETED".equals(r.intent.getAction())) {
                if (!debugging && anrMessage3 != null) {
                    this.mHandler.post(new AppNotResponding(app2, anrMessage3));
                }
                return;
            }
            Slog.w("BroadcastQueue", "Skip anr of PRE_BOOT_COMPLETED for app :" + app2);
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
            this.mBroadcastHistory[this.mHistoryNext] = historyRecord;
            this.mHistoryNext = ringAdvance(this.mHistoryNext, 1, MAX_BROADCAST_HISTORY);
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
        for (int i2 = this.mOrderedBroadcasts.size() - 1; i2 >= 0; i2--) {
            didSomething |= this.mOrderedBroadcasts.get(i2).cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
            if (!doit && didSomething) {
                return true;
            }
        }
        return didSomething;
    }

    /* access modifiers changed from: package-private */
    public final void logBroadcastReceiverDiscardLocked(BroadcastRecord r) {
        int logIndex = r.nextReceiver - 1;
        if (logIndex < 0 || logIndex >= r.receivers.size()) {
            if (logIndex < 0) {
                Slog.w("BroadcastQueue", "Discarding broadcast before first receiver is invoked: " + r);
            }
            EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_APP, new Object[]{-1, Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(r.nextReceiver), "NONE"});
            return;
        }
        Object curReceiver = r.receivers.get(logIndex);
        if (curReceiver instanceof BroadcastFilter) {
            BroadcastFilter bf = (BroadcastFilter) curReceiver;
            EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_FILTER, new Object[]{Integer.valueOf(bf.owningUserId), Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(logIndex), Integer.valueOf(System.identityHashCode(bf))});
            return;
        }
        ResolveInfo ri = (ResolveInfo) curReceiver;
        EventLog.writeEvent(EventLogTags.AM_BROADCAST_DISCARD_APP, new Object[]{Integer.valueOf(UserHandle.getUserId(ri.activityInfo.applicationInfo.uid)), Integer.valueOf(System.identityHashCode(r)), r.intent.getAction(), Integer.valueOf(logIndex), ri.toString()});
    }

    private String createBroadcastTraceTitle(BroadcastRecord record, int state) {
        Object[] objArr = new Object[4];
        objArr[0] = state == 0 ? "in queue" : "dispatched";
        objArr[1] = record.callerPackage == null ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : record.callerPackage;
        objArr[2] = record.callerApp == null ? "process unknown" : record.callerApp.toShortString();
        objArr[3] = record.intent == null ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : record.intent.getAction();
        return String.format("Broadcast %s from %s (%s) %s", objArr);
    }

    /* access modifiers changed from: package-private */
    public final boolean isIdle() {
        return this.mParallelBroadcasts.isEmpty() && this.mOrderedBroadcasts.isEmpty() && this.mPendingBroadcast == null;
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        int i;
        int lastIndex;
        ProtoOutputStream protoOutputStream = proto;
        long token = proto.start(fieldId);
        protoOutputStream.write(1138166333441L, this.mQueueName);
        for (int i2 = this.mParallelBroadcasts.size() - 1; i2 >= 0; i2--) {
            this.mParallelBroadcasts.get(i2).writeToProto(protoOutputStream, 2246267895810L);
        }
        for (int i3 = this.mOrderedBroadcasts.size() - 1; i3 >= 0; i3--) {
            this.mOrderedBroadcasts.get(i3).writeToProto(protoOutputStream, 2246267895811L);
        }
        if (this.mPendingBroadcast != null) {
            this.mPendingBroadcast.writeToProto(protoOutputStream, 1146756268036L);
        }
        int lastIndex2 = this.mHistoryNext;
        int ringIndex = lastIndex2;
        do {
            i = -1;
            ringIndex = ringAdvance(ringIndex, -1, MAX_BROADCAST_HISTORY);
            BroadcastRecord r = this.mBroadcastHistory[ringIndex];
            if (r != null) {
                r.writeToProto(protoOutputStream, 2246267895813L);
                continue;
            }
        } while (ringIndex != lastIndex2);
        int i4 = this.mSummaryHistoryNext;
        int ringIndex2 = i4;
        int lastIndex3 = i4;
        while (true) {
            int ringIndex3 = ringAdvance(ringIndex2, i, MAX_BROADCAST_SUMMARY_HISTORY);
            Intent intent = this.mBroadcastSummaryHistory[ringIndex3];
            if (intent == null) {
                lastIndex = lastIndex3;
            } else {
                lastIndex = lastIndex3;
                intent.writeToProto(protoOutputStream, 1146756268033L, false, true, true, false);
                protoOutputStream.write(1112396529666L, this.mSummaryHistoryEnqueueTime[ringIndex3]);
                protoOutputStream.write(1112396529667L, this.mSummaryHistoryDispatchTime[ringIndex3]);
                protoOutputStream.write(1112396529668L, this.mSummaryHistoryFinishTime[ringIndex3]);
                protoOutputStream.end(protoOutputStream.start(2246267895814L));
            }
            int lastIndex4 = lastIndex;
            if (ringIndex3 == lastIndex4) {
                protoOutputStream.end(token);
                return;
            }
            lastIndex3 = lastIndex4;
            ringIndex2 = ringIndex3;
            i = -1;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage, boolean needSep) {
        boolean needSep2;
        int ringIndex;
        PrintWriter printWriter = pw;
        String str = dumpPackage;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean z = true;
        if (this.mParallelBroadcasts.size() > 0 || this.mOrderedBroadcasts.size() > 0 || this.mPendingBroadcast != null) {
            boolean printed = false;
            boolean needSep3 = needSep;
            for (int i = this.mParallelBroadcasts.size() - 1; i >= 0; i--) {
                BroadcastRecord br = this.mParallelBroadcasts.get(i);
                if (str == null || str.equals(br.callerPackage)) {
                    if (!printed) {
                        if (needSep3) {
                            pw.println();
                        }
                        needSep3 = true;
                        printed = true;
                        printWriter.println("  Active broadcasts [" + this.mQueueName + "]:");
                    }
                    printWriter.println("  Active Broadcast " + this.mQueueName + " #" + i + ":");
                    br.dump(printWriter, "    ", sdf);
                }
            }
            boolean printed2 = false;
            needSep2 = true;
            for (int i2 = this.mOrderedBroadcasts.size() - 1; i2 >= 0; i2--) {
                BroadcastRecord br2 = this.mOrderedBroadcasts.get(i2);
                if (str == null || str.equals(br2.callerPackage)) {
                    if (!printed2) {
                        if (needSep2) {
                            pw.println();
                        }
                        needSep2 = true;
                        printed2 = true;
                        printWriter.println("  Active ordered broadcasts [" + this.mQueueName + "]:");
                    }
                    printWriter.println("  Active Ordered Broadcast " + this.mQueueName + " #" + i2 + ":");
                    this.mOrderedBroadcasts.get(i2).dump(printWriter, "    ", sdf);
                }
            }
            if (str == null || (this.mPendingBroadcast != null && str.equals(this.mPendingBroadcast.callerPackage))) {
                if (needSep2) {
                    pw.println();
                }
                printWriter.println("  Pending broadcast [" + this.mQueueName + "]:");
                if (this.mPendingBroadcast != null) {
                    this.mPendingBroadcast.dump(printWriter, "    ", sdf);
                } else {
                    printWriter.println("    (null)");
                }
                needSep2 = true;
            }
        } else {
            needSep2 = needSep;
        }
        int i3 = -1;
        int lastIndex = this.mHistoryNext;
        boolean needSep4 = needSep2;
        boolean printed3 = false;
        int ringIndex2 = lastIndex;
        do {
            ringIndex2 = ringAdvance(ringIndex2, -1, MAX_BROADCAST_HISTORY);
            BroadcastRecord r = this.mBroadcastHistory[ringIndex2];
            if (r != null) {
                i3++;
                if (str == null || str.equals(r.callerPackage)) {
                    if (!printed3) {
                        if (needSep4) {
                            pw.println();
                        }
                        needSep4 = true;
                        printWriter.println("  Historical broadcasts [" + this.mQueueName + "]:");
                        printed3 = true;
                    }
                    if (dumpAll) {
                        printWriter.print("  Historical Broadcast " + this.mQueueName + " #");
                        printWriter.print(i3);
                        printWriter.println(":");
                        r.dump(printWriter, "    ", sdf);
                        continue;
                    } else {
                        printWriter.print("  #");
                        printWriter.print(i3);
                        printWriter.print(": ");
                        printWriter.println(r);
                        printWriter.print("    ");
                        printWriter.println(r.intent.toShortString(true, true, true, false));
                        if (!(r.targetComp == null || r.targetComp == r.intent.getComponent())) {
                            printWriter.print("    targetComp: ");
                            printWriter.println(r.targetComp.toShortString());
                        }
                        Bundle bundle = r.intent.getExtras();
                        if (bundle != null && !isFromEmailMDM(r.intent)) {
                            printWriter.print("    extras: ");
                            printWriter.println(bundle.toString());
                            continue;
                        }
                    }
                }
            }
        } while (ringIndex2 != lastIndex);
        if (str == null) {
            int lastIndex2 = this.mSummaryHistoryNext;
            int ringIndex3 = lastIndex2;
            if (dumpAll) {
                printed3 = false;
                i3 = -1;
                ringIndex = ringIndex3;
            } else {
                ringIndex = ringIndex3;
                int j = i3;
                while (j > 0 && ringIndex != lastIndex2) {
                    ringIndex = ringAdvance(ringIndex, -1, MAX_BROADCAST_SUMMARY_HISTORY);
                    if (this.mBroadcastHistory[ringIndex] != null) {
                        j--;
                    }
                }
            }
            while (true) {
                ringIndex = ringAdvance(ringIndex, -1, MAX_BROADCAST_SUMMARY_HISTORY);
                Intent intent = this.mBroadcastSummaryHistory[ringIndex];
                if (intent != null) {
                    if (!printed3) {
                        if (needSep4) {
                            pw.println();
                        }
                        needSep4 = true;
                        printWriter.println("  Historical broadcasts summary [" + this.mQueueName + "]:");
                        printed3 = true;
                    }
                    if (!dumpAll && i3 >= 50) {
                        printWriter.println("  ...");
                        break;
                    }
                    i3++;
                    printWriter.print("  #");
                    printWriter.print(i3);
                    printWriter.print(": ");
                    printWriter.println(intent.toShortString(false, z, z, false));
                    printWriter.print("    ");
                    TimeUtils.formatDuration(this.mSummaryHistoryDispatchTime[ringIndex] - this.mSummaryHistoryEnqueueTime[ringIndex], printWriter);
                    printWriter.print(" dispatch ");
                    TimeUtils.formatDuration(this.mSummaryHistoryFinishTime[ringIndex] - this.mSummaryHistoryDispatchTime[ringIndex], printWriter);
                    printWriter.println(" finish");
                    printWriter.print("    enq=");
                    printWriter.print(sdf.format(new Date(this.mSummaryHistoryEnqueueTime[ringIndex])));
                    printWriter.print(" disp=");
                    printWriter.print(sdf.format(new Date(this.mSummaryHistoryDispatchTime[ringIndex])));
                    printWriter.print(" fin=");
                    printWriter.println(sdf.format(new Date(this.mSummaryHistoryFinishTime[ringIndex])));
                    Bundle bundle2 = intent.getExtras();
                    if (bundle2 != null && !"android.intent.action.PHONE_STATE".equals(intent.getAction()) && !"android.intent.action.NEW_OUTGOING_CALL".equals(intent.getAction()) && !isFromEmailMDM(intent)) {
                        printWriter.print("    extras: ");
                        printWriter.println(bundle2.toString());
                    }
                }
                if (ringIndex == lastIndex2) {
                    break;
                }
                z = true;
            }
        } else {
            int lastIndex3 = ringIndex2;
        }
        return needSep4;
    }

    private boolean isFromEmailMDM(Intent intent) {
        if (intent == null) {
            return false;
        }
        return "com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intent.getAction());
    }

    private void updateSRMSStatisticsData(BroadcastRecord r) {
        if (mIsBetaUser && this.mService.getIawareResourceFeature(1)) {
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
                    Slog.w("BroadcastQueue", "elapsedTime error [" + this.mQueueName + "] for " + r);
                }
            }
        }
    }
}
