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
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.EventLog;
import android.util.Flog;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.HwServiceFactory;
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
    static final int MAX_BROADCAST_HISTORY = 0;
    static final int MAX_BROADCAST_SUMMARY_HISTORY = 0;
    static final int MAYBE_BROADCAST_BG_TIMEOUT = 5000;
    static final int MAYBE_BROADCAST_FG_TIMEOUT = 2000;
    static final int SCHEDULE_TEMP_WHITELIST_MSG = 202;
    private static final String TAG = "BroadcastQueue";
    private static final String TAG_BROADCAST = null;
    private static final String TAG_MU = "BroadcastQueue_MU";
    private static final boolean mIsBetaUser = false;
    final BroadcastRecord[] mBroadcastHistory;
    final Intent[] mBroadcastSummaryHistory;
    boolean mBroadcastsScheduled;
    final boolean mDelayBehindServices;
    final BroadcastHandler mHandler;
    int mHistoryNext;
    final ArrayList<BroadcastRecord> mOrderedBroadcasts;
    final ArrayList<BroadcastRecord> mParallelBroadcasts;
    BroadcastRecord mPendingBroadcast;
    int mPendingBroadcastRecvIndex;
    boolean mPendingBroadcastTimeoutMessage;
    final String mQueueName;
    final ActivityManagerService mService;
    final long[] mSummaryHistoryDispatchTime;
    final long[] mSummaryHistoryEnqueueTime;
    final long[] mSummaryHistoryFinishTime;
    int mSummaryHistoryNext;
    final long mTimeoutPeriod;

    /* renamed from: com.android.server.am.BroadcastQueue.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ Intent val$intent;
        final /* synthetic */ int val$receivingUserId;

        AnonymousClass1(Intent val$intent, int val$receivingUserId) {
            this.val$intent = val$intent;
            this.val$receivingUserId = val$receivingUserId;
        }

        public void run() {
            BroadcastQueue.this.mService.mContext.startActivityAsUser(this.val$intent, new UserHandle(this.val$receivingUserId));
        }
    }

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
            if (focusedWindow != null && windowTypeToLayerLw(focusedWindow.mAttrs.type) < windowTypeToLayerLw(2003)) {
                return false;
            }
            return true;
        }

        private int windowTypeToLayerLw(int type) {
            return HwPolicyFactory.getHwPhoneWindowManager().windowTypeToLayerLw(type);
        }

        private WindowState getCurFocusedWindow() {
            try {
                Class<?> cls = Class.forName(WINDOW_MANAGER_SERVICE_CLASS_NAME);
                if (cls != null) {
                    Method method = cls.getDeclaredMethod(GET_FOCUSED_WINDOW_METHOD_NAME, new Class[BroadcastQueue.MAX_BROADCAST_SUMMARY_HISTORY]);
                    if (method != null) {
                        method.setAccessible(true);
                        return (WindowState) method.invoke(BroadcastQueue.this.mService.mWindowManager, new Object[BroadcastQueue.MAX_BROADCAST_SUMMARY_HISTORY]);
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
                case BroadcastQueue.BROADCAST_INTENT_MSG /*200*/:
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(BroadcastQueue.TAG_BROADCAST, "Received BROADCAST_INTENT_MSG");
                    }
                    BroadcastQueue.this.processNextBroadcast(true);
                case BroadcastQueue.BROADCAST_TIMEOUT_MSG /*201*/:
                    synchronized (BroadcastQueue.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            BroadcastQueue.this.broadcastTimeoutLocked(true);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                case BroadcastQueue.SCHEDULE_TEMP_WHITELIST_MSG /*202*/:
                    LocalService dic = BroadcastQueue.this.mService.mLocalDeviceIdleController;
                    if (dic != null) {
                        dic.addPowerSaveTempWhitelistAppDirect(UserHandle.getAppId(msg.arg1), (long) msg.arg2, true, (String) msg.obj);
                    }
                case BroadcastQueue.BROADCAST_CHECKTIMEOUT_MSG /*203*/:
                    synchronized (BroadcastQueue.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            BroadcastQueue.this.handleMaybeTimeoutBC();
                            BroadcastQueue.this.uploadRadarMessage(HwBroadcastRadarUtil.SCENE_DEF_RECEIVER_TIMEOUT, null);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.BroadcastQueue.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.BroadcastQueue.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.BroadcastQueue.<clinit>():void");
    }

    private void handleMaybeTimeoutBC() {
        if (this.mOrderedBroadcasts.size() == 0) {
            Slog.w(TAG, "handleMaybeTimeoutBC, but mOrderedBroadcasts is null");
            return;
        }
        BroadcastRecord r = (BroadcastRecord) this.mOrderedBroadcasts.get(MAX_BROADCAST_SUMMARY_HISTORY);
        if (r.nextReceiver <= 0) {
            Slog.w(TAG, "handleMaybeTimeoutBC Timeout on receiver with nextReceiver <= 0");
            return;
        }
        String pkg = null;
        String str = null;
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
                str = String.valueOf(iPid);
            }
            target = "PackageName:" + pkg;
        } else if (curReceiver instanceof ResolveInfo) {
            ResolveInfo info = (ResolveInfo) curReceiver;
            if (info.activityInfo != null) {
                pkg = info.activityInfo.applicationInfo.packageName;
                StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
                sb.append("ReceiverName:");
                ComponentName.appendShortString(sb, pkg, info.activityInfo.name);
                target = sb.toString();
            }
        }
        Utils.handleTimeOut("broadcast", pkg, str);
        this.mService.checkOrderedBroadcastTimeoutLocked(target + "+ActionName:" + r.intent.getAction(), MAX_BROADCAST_SUMMARY_HISTORY, false);
    }

    BroadcastQueue(ActivityManagerService service, Handler handler, String name, long timeoutPeriod, boolean allowDelayBehindServices) {
        this.mParallelBroadcasts = new ArrayList();
        this.mOrderedBroadcasts = new ArrayList();
        this.mBroadcastHistory = new BroadcastRecord[MAX_BROADCAST_HISTORY];
        this.mHistoryNext = MAX_BROADCAST_SUMMARY_HISTORY;
        this.mBroadcastSummaryHistory = new Intent[MAX_BROADCAST_SUMMARY_HISTORY];
        this.mSummaryHistoryNext = MAX_BROADCAST_SUMMARY_HISTORY;
        this.mSummaryHistoryEnqueueTime = new long[MAX_BROADCAST_SUMMARY_HISTORY];
        this.mSummaryHistoryDispatchTime = new long[MAX_BROADCAST_SUMMARY_HISTORY];
        this.mSummaryHistoryFinishTime = new long[MAX_BROADCAST_SUMMARY_HISTORY];
        this.mBroadcastsScheduled = false;
        this.mPendingBroadcast = null;
        this.mService = service;
        this.mHandler = new BroadcastHandler(handler.getLooper());
        this.mQueueName = name;
        this.mTimeoutPeriod = timeoutPeriod;
        this.mDelayBehindServices = allowDelayBehindServices;
    }

    public boolean isPendingBroadcastProcessLocked(int pid) {
        return this.mPendingBroadcast != null && this.mPendingBroadcast.curApp.pid == pid;
    }

    public void enqueueParallelBroadcastLocked(BroadcastRecord r) {
        this.mParallelBroadcasts.add(r);
        if (r != null) {
            r.enqueueClockTime = System.currentTimeMillis();
        }
    }

    public void enqueueOrderedBroadcastLocked(BroadcastRecord r) {
        this.mOrderedBroadcasts.add(r);
        if (r != null) {
            r.enqueueClockTime = System.currentTimeMillis();
        }
    }

    public final boolean replaceParallelBroadcastLocked(BroadcastRecord r) {
        synchronized (this.mParallelBroadcasts) {
            int i = this.mParallelBroadcasts.size() - 1;
            while (i >= 0) {
                if (r == null || r.intent == null || this.mParallelBroadcasts.get(i) == null || !r.intent.filterEquals(((BroadcastRecord) this.mParallelBroadcasts.get(i)).intent)) {
                    i--;
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG_BROADCAST, "***** DROPPING PARALLEL [" + this.mQueueName + "]: " + r.intent);
                    }
                    this.mParallelBroadcasts.set(i, r);
                    return true;
                }
            }
            return false;
        }
    }

    public final boolean replaceOrderedBroadcastLocked(BroadcastRecord r) {
        for (int i = this.mOrderedBroadcasts.size() - 1; i > 0; i--) {
            if (r.intent.filterEquals(((BroadcastRecord) this.mOrderedBroadcasts.get(i)).intent)) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG_BROADCAST, "***** DROPPING ORDERED [" + this.mQueueName + "]: " + r.intent);
                }
                this.mOrderedBroadcasts.set(i, r);
                return true;
            }
        }
        return false;
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
            app.curReceiver = r;
            app.forceProcessStateUpTo(11);
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
                    Slog.v(TAG_BROADCAST, "Process cur broadcast " + r + " DELIVERED for app " + app + ", callerApp euid: " + (r.callerApp != null ? r.callerApp.info.euid : MAX_BROADCAST_SUMMARY_HISTORY));
                }
                started = true;
            } finally {
                if (!started) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG_BROADCAST, "Process cur broadcast " + r + ": NOT STARTED!");
                    }
                    r.receiver = null;
                    r.curApp = null;
                    app.curReceiver = null;
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
                br.state = MAX_BROADCAST_SUMMARY_HISTORY;
                throw new RuntimeException(e.getMessage());
            }
        }
        return didSomething;
    }

    public void skipPendingBroadcastLocked(int pid) {
        BroadcastRecord br = this.mPendingBroadcast;
        if (br != null && br.curApp.pid == pid) {
            br.state = MAX_BROADCAST_SUMMARY_HISTORY;
            br.nextReceiver = this.mPendingBroadcastRecvIndex;
            this.mPendingBroadcast = null;
            scheduleBroadcastsLocked();
        }
    }

    public void skipCurrentReceiverLocked(ProcessRecord app) {
        BroadcastRecord r = null;
        if (this.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord br = (BroadcastRecord) this.mOrderedBroadcasts.get(MAX_BROADCAST_SUMMARY_HISTORY);
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
            this.mHandler.sendMessage(this.mHandler.obtainMessage(BROADCAST_INTENT_MSG, this));
            this.mBroadcastsScheduled = true;
        }
    }

    public BroadcastRecord getMatchingOrderedReceiver(IBinder receiver) {
        if (this.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord r = (BroadcastRecord) this.mOrderedBroadcasts.get(MAX_BROADCAST_SUMMARY_HISTORY);
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
        r.state = MAX_BROADCAST_SUMMARY_HISTORY;
        if (state == 0) {
            Slog.w(TAG, "finishReceiver [" + this.mQueueName + "] called but state is IDLE");
        }
        r.receiver = null;
        r.intent.setComponent(null);
        if (r.curApp != null && r.curApp.curReceiver == r) {
            r.curApp.curReceiver = null;
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
        if (waitForServices && r.curComponent != null && r.queue.mDelayBehindServices && r.queue.mOrderedBroadcasts.size() > 0 && r.queue.mOrderedBroadcasts.get(MAX_BROADCAST_SUMMARY_HISTORY) == r) {
            ActivityInfo activityInfo;
            if (r.nextReceiver < r.receivers.size()) {
                Object obj = r.receivers.get(r.nextReceiver);
                activityInfo = obj instanceof ActivityInfo ? (ActivityInfo) obj : null;
            } else {
                activityInfo = null;
            }
            if (!(receiver != null && r0 != null && receiver.applicationInfo.uid == r0.applicationInfo.uid && receiver.applicationInfo.euid == r0.applicationInfo.euid && receiver.processName.equals(r0.processName)) && this.mService.mServices.hasBackgroundServices(r.userId)) {
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
            BroadcastRecord br = (BroadcastRecord) this.mOrderedBroadcasts.get(MAX_BROADCAST_SUMMARY_HISTORY);
            if (br.userId == userId && br.state == 4) {
                Slog.i(TAG, "Resuming delayed broadcast");
                br.curComponent = null;
                br.state = MAX_BROADCAST_SUMMARY_HISTORY;
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
                }
                ActivityManagerService.boostPriorityForLockedSection();
                Slog.w(TAG, "Can't deliver broadcast to " + app.processName + " (pid " + app.pid + "). Crashing it.");
                app.scheduleCrash("can't deliver broadcast");
                throw ex;
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
            for (int i = MAX_BROADCAST_SUMMARY_HISTORY; i < r.requiredPermissions.length; i++) {
                String requiredPermission = r.requiredPermissions[i];
                if (this.mService.checkComponentPermission(requiredPermission, filter.receiverList.pid, filter.receiverList.uid, -1, true) != 0) {
                    Slog.w(TAG, "Permission Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " requires " + requiredPermission + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                    skip = true;
                    break;
                }
                int appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                if (!(appOp == -1 || appOp == r.appOp)) {
                    if (this.mService.mAppOpsService.noteOperation(appOp, filter.receiverList.uid, filter.packageName) != 0) {
                        Slog.w(TAG, "Appop Denial: receiving " + r.intent.toString() + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")" + " requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                        skip = true;
                        break;
                    }
                }
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
        if (!skip && this.mService.checkAllowBackgroundLocked(filter.receiverList.uid, filter.packageName, -1, true) == 2) {
            Slog.w(TAG, "Background execution not allowed: receiving " + r.intent + " to " + filter.receiverList.app + " (pid=" + filter.receiverList.pid + ", uid=" + filter.receiverList.uid + ")");
            skip = true;
        }
        if (!this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, filter.receiverList.uid)) {
            skip = true;
        }
        if (!skip && this.mService.shouldPreventSendBroadcast(r.intent, filter.packageName, r.callingUid, r.callingPid, r.callerPackage, r.userId)) {
            skip = true;
        }
        if (!skip && (filter.receiverList.app == null || filter.receiverList.app.crashing)) {
            Slog.w(TAG, "Skipping deliver [" + this.mQueueName + "] " + r + " to " + filter.receiverList + ": process crashing");
            skip = true;
        }
        if (skip) {
            r.delivery[index] = 2;
            return;
        }
        if (Build.PERMISSIONS_REVIEW_REQUIRED) {
            if (!requestStartTargetPermissionsReviewIfNeededLocked(r, filter.packageName, filter.owningUserId)) {
                r.delivery[index] = 2;
                return;
            }
        }
        if (getMtmBRManagerEnabled(10)) {
            AbsHwMtmBroadcastResourceManager mtmBRManager = getMtmBRManager();
            if (mtmBRManager != null) {
                if (mtmBRManager.iawareProcessBroadcast(MAX_BROADCAST_SUMMARY_HISTORY, !ordered, r, filter)) {
                    return;
                }
            }
        }
        r.delivery[index] = 1;
        if (ordered) {
            r.receiver = filter.receiverList.receiver.asBinder();
            r.curFilter = filter;
            filter.receiverList.curBroadcast = r;
            r.state = 2;
            if (filter.receiverList.app != null) {
                r.curApp = filter.receiverList.app;
                filter.receiverList.app.curReceiver = r;
                this.mService.updateOomAdjLocked(r.curApp);
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
        } catch (Throwable e) {
            Slog.w(TAG, "Failure sending broadcast " + r.intent, e);
            if (ordered) {
                r.receiver = null;
                r.curFilter = null;
                filter.receiverList.curBroadcast = null;
                if (filter.receiverList.app != null) {
                    filter.receiverList.app.curReceiver = null;
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
            IIntentSender target = this.mService.getIntentSenderLocked(1, receiverRecord.callerPackage, receiverRecord.callingUid, receiverRecord.userId, null, null, MAX_BROADCAST_SUMMARY_HISTORY, new Intent[]{receiverRecord.intent}, new String[]{receiverRecord.intent.resolveType(this.mService.mContext.getContentResolver())}, 1409286144, null);
            Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent.addFlags(276824064);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", receivingPackageName);
            intent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                Slog.i(TAG, "u" + receivingUserId + " Launching permission review for package " + receivingPackageName);
            }
            this.mHandler.post(new AnonymousClass1(intent, receivingUserId));
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
            b.append(r.intent.getComponent().flattenToShortString());
        } else if (r.intent.getData() != null) {
            b.append(r.intent.getData());
        }
        this.mHandler.obtainMessage(SCHEDULE_TEMP_WHITELIST_MSG, uid, (int) duration, b.toString()).sendToTarget();
    }

    final void processNextBroadcast(boolean fromMsg) {
        synchronized (this.mService) {
            int i;
            Object target;
            ActivityManagerService.boostPriorityForLockedSection();
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG_BROADCAST, "processNextBroadcast [" + this.mQueueName + "]: " + this.mParallelBroadcasts.size() + " broadcasts, " + this.mOrderedBroadcasts.size() + " ordered broadcasts");
            }
            this.mService.updateCpuStats();
            if (fromMsg) {
                this.mBroadcastsScheduled = false;
            }
            while (this.mParallelBroadcasts.size() > 0) {
                synchronized (this.mParallelBroadcasts) {
                    BroadcastRecord r = (BroadcastRecord) this.mParallelBroadcasts.remove(MAX_BROADCAST_SUMMARY_HISTORY);
                    try {
                    } catch (Throwable th) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
                if (r == null) {
                    Flog.e(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, "mParallelBroadcasts.get(0) is null");
                } else {
                    AbsHwMtmBroadcastResourceManager mtmBRManager;
                    r.dispatchTime = SystemClock.uptimeMillis();
                    r.dispatchClockTime = System.currentTimeMillis();
                    int N = r.receivers.size();
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                        Slog.v(TAG_BROADCAST, "Processing parallel broadcast [" + this.mQueueName + "] " + r);
                    }
                    if (getMtmBRManagerEnabled(10)) {
                        mtmBRManager = getMtmBRManager();
                        if (mtmBRManager != null) {
                            mtmBRManager.iawareStartCountBroadcastSpeed(true, r);
                        }
                    }
                    for (i = MAX_BROADCAST_SUMMARY_HISTORY; i < N; i++) {
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
                        mtmBRManager = getMtmBRManager();
                        if (mtmBRManager != null) {
                            mtmBRManager.iawareEndCountBroadcastSpeed(r);
                        }
                    }
                    addBroadcastToHistoryLocked(r);
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                        Slog.v(TAG_BROADCAST, "Done with parallel broadcast [" + this.mQueueName + "] " + r);
                    }
                }
            }
            if (this.mPendingBroadcast != null) {
                boolean z;
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.v(TAG_BROADCAST, "processNextBroadcast [" + this.mQueueName + "]: waiting for " + this.mPendingBroadcast.curApp);
                }
                synchronized (this.mService.mPidsSelfLocked) {
                    ProcessRecord proc = (ProcessRecord) this.mService.mPidsSelfLocked.get(this.mPendingBroadcast.curApp.pid);
                    z = proc != null ? proc.crashing : true;
                }
                if (z) {
                    Slog.w(TAG, "pending app  [" + this.mQueueName + "]" + this.mPendingBroadcast.curApp + " died before responding to broadcast");
                    this.mPendingBroadcast.state = MAX_BROADCAST_SUMMARY_HISTORY;
                    this.mPendingBroadcast.nextReceiver = this.mPendingBroadcastRecvIndex;
                    this.mPendingBroadcast = null;
                } else {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
            }
            boolean looped = false;
            while (this.mOrderedBroadcasts.size() != 0) {
                r = (BroadcastRecord) this.mOrderedBroadcasts.get(MAX_BROADCAST_SUMMARY_HISTORY);
                boolean forceReceive = false;
                int numReceivers = r.receivers != null ? r.receivers.size() : MAX_BROADCAST_SUMMARY_HISTORY;
                if (this.mService.mProcessesReady && r.dispatchTime > 0) {
                    long now = SystemClock.uptimeMillis();
                    if (r.anrCount > 0) {
                        Slog.w(TAG_BROADCAST, "intentAction=" + r.intent.getAction() + "dispatchTime=" + r.dispatchTime + " numReceivers=" + numReceivers + " nextReceiver=" + r.nextReceiver + " now=" + now + " state=" + r.state + " curReceiver  = " + r.curReceiver);
                    }
                    if (numReceivers > 0 && now > r.dispatchTime + ((this.mTimeoutPeriod * 2) * ((long) numReceivers))) {
                        Slog.w(TAG, "Hung broadcast [" + this.mQueueName + "] discarded after timeout failure:" + " now=" + now + " dispatchTime=" + r.dispatchTime + " startTime=" + r.receiverTime + " intent=" + r.intent + " numReceivers=" + numReceivers + " nextReceiver=" + r.nextReceiver + " state=" + r.state);
                        broadcastTimeoutLocked(false);
                        forceReceive = true;
                        r.state = MAX_BROADCAST_SUMMARY_HISTORY;
                    }
                    if (r.anrCount > 0 && numReceivers == 0 && now > r.dispatchTime && r.curReceiver != null && this == r.queue) {
                        Slog.w(TAG_BROADCAST, " dispatchTime=" + r.dispatchTime + " numReceivers=" + numReceivers + " nextReceiver=" + r.nextReceiver + " now=" + now + " state=" + r.state + " curReceiver  = " + r.curReceiver + "finish curReceiver");
                        logBroadcastReceiverDiscardLocked(r);
                        finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                        scheduleBroadcastsLocked();
                        r.state = MAX_BROADCAST_SUMMARY_HISTORY;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                }
                if (r.state != 0) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.d(TAG_BROADCAST, "processNextBroadcast(" + this.mQueueName + ") called when not idle (state=" + r.state + ")");
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                int recIdx;
                long timeoutTime;
                BroadcastOptions brOptions;
                Object nextReceiver;
                ResolveInfo info;
                ComponentName componentName;
                boolean skip;
                int perm;
                int opCode;
                String requiredPermission;
                int appOp;
                boolean isSingleton;
                boolean isAvailable;
                int receiverUid;
                String targetProcess;
                ProcessRecord app;
                int allowed;
                String packageName;
                String sourceDir;
                String publicSourceDir;
                ApplicationInfo applicationInfo;
                ProcessRecord startProcessLocked;
                BroadcastFilter filter;
                if (r.receivers != null && r.nextReceiver < numReceivers) {
                    if (!r.resultAbort) {
                        if (!forceReceive) {
                            continue;
                            if (r != null) {
                                recIdx = r.nextReceiver;
                                r.nextReceiver = recIdx + 1;
                                target = r.receivers.get(recIdx);
                                if (enqueueProxyBroadcast(false, r, target)) {
                                    r.receiverTime = SystemClock.uptimeMillis();
                                    if (recIdx == 0) {
                                        r.dispatchTime = r.receiverTime;
                                        r.dispatchClockTime = System.currentTimeMillis();
                                        Flog.i(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, "dispatch ordered broadcast [" + this.mQueueName + "] " + r + ", has " + r.receivers.size() + " receivers");
                                        updateSRMSStatisticsData(r);
                                    }
                                    if (!this.mPendingBroadcastTimeoutMessage) {
                                        timeoutTime = r.receiverTime + this.mTimeoutPeriod;
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v(TAG_BROADCAST, "Submitting BROADCAST_TIMEOUT_MSG [" + this.mQueueName + "] for " + r + " at " + timeoutTime);
                                        }
                                        setBroadcastTimeoutLocked(timeoutTime);
                                    }
                                    brOptions = r.options;
                                    nextReceiver = r.receivers.get(recIdx);
                                    if (nextReceiver instanceof BroadcastFilter) {
                                        info = (ResolveInfo) nextReceiver;
                                        componentName = new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
                                        skip = false;
                                        if (brOptions != null && (info.activityInfo.applicationInfo.targetSdkVersion < brOptions.getMinManifestReceiverApiLevel() || info.activityInfo.applicationInfo.targetSdkVersion > brOptions.getMaxManifestReceiverApiLevel())) {
                                            skip = true;
                                        }
                                        perm = this.mService.checkComponentPermission(info.activityInfo.permission, r.callingPid, r.callingUid, info.activityInfo.applicationInfo.uid, info.activityInfo.exported);
                                        if (skip && perm != 0) {
                                            if (info.activityInfo.exported) {
                                                Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires " + info.activityInfo.permission + " due to receiver " + componentName.flattenToShortString());
                                            } else {
                                                Slog.w(TAG, "Permission Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " is not exported from uid " + info.activityInfo.applicationInfo.uid + " due to receiver " + componentName.flattenToShortString());
                                            }
                                            skip = true;
                                        } else if (!(skip || info.activityInfo.permission == null)) {
                                            opCode = AppOpsManager.permissionToOpCode(info.activityInfo.permission);
                                            if (opCode != -1) {
                                                if (this.mService.mAppOpsService.noteOperation(opCode, r.callingUid, r.callerPackage) != 0) {
                                                    Slog.w(TAG, "Appop Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires appop " + AppOpsManager.permissionToOp(info.activityInfo.permission) + " due to registered receiver " + componentName.flattenToShortString());
                                                    skip = true;
                                                }
                                            }
                                        }
                                        if (!(skip || info.activityInfo.applicationInfo.uid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || r.requiredPermissions == null || r.requiredPermissions.length <= 0)) {
                                            for (i = MAX_BROADCAST_SUMMARY_HISTORY; i < r.requiredPermissions.length; i++) {
                                                requiredPermission = r.requiredPermissions[i];
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
                                                appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                                                if (!(appOp == -1 || appOp == r.appOp)) {
                                                    if (this.mService.mAppOpsService.noteOperation(appOp, info.activityInfo.applicationInfo.uid, info.activityInfo.packageName) != 0) {
                                                        Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                                        skip = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (!(skip || r.appOp == -1 || this.mService.mAppOpsService.noteOperation(r.appOp, info.activityInfo.applicationInfo.uid, info.activityInfo.packageName) == 0)) {
                                            Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                            skip = true;
                                        }
                                        if (!skip) {
                                            skip = this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, info.activityInfo.applicationInfo.uid);
                                        }
                                        if (!skip && this.mService.shouldPreventSendBroadcast(r.intent, info.activityInfo.packageName, r.callingUid, r.callingPid, r.callerPackage, r.userId)) {
                                            skip = true;
                                        }
                                        isSingleton = false;
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
                                        if (skip) {
                                            r.manifestSkipCount++;
                                        } else {
                                            r.manifestCount++;
                                        }
                                        if (r.curApp != null && r.curApp.crashing) {
                                            Slog.w(TAG, "Skipping deliver ordered [" + this.mQueueName + "] " + r + " to " + r.curApp + ": process crashing");
                                            skip = true;
                                        }
                                        if (!skip) {
                                            isAvailable = false;
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
                                            skip = true;
                                        }
                                        if (!(!Build.PERMISSIONS_REVIEW_REQUIRED || skip || requestStartTargetPermissionsReviewIfNeededLocked(r, info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid)))) {
                                            skip = true;
                                        }
                                        receiverUid = info.activityInfo.applicationInfo.uid;
                                        if (r.callingUid != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE && r34 && this.mService.isValidSingletonCall(r.callingUid, receiverUid)) {
                                            info.activityInfo = this.mService.getActivityInfoForUser(info.activityInfo, MAX_BROADCAST_SUMMARY_HISTORY);
                                        }
                                        targetProcess = info.activityInfo.processName;
                                        app = this.mService.getProcessRecordLocked(targetProcess, info.activityInfo.applicationInfo.uid + info.activityInfo.applicationInfo.euid, false);
                                        if (!skip) {
                                            allowed = this.mService.checkAllowBackgroundLocked(info.activityInfo.applicationInfo.uid, info.activityInfo.packageName, -1, false);
                                            if (allowed != 0) {
                                                if (allowed == 2) {
                                                    Slog.w(TAG, "Background execution disabled: receiving " + r.intent + " to " + componentName.flattenToShortString());
                                                    skip = true;
                                                } else if ((r.intent.getFlags() & 8388608) != 0 || (r.intent.getComponent() == null && r.intent.getPackage() == null && (r.intent.getFlags() & 16777216) == 0)) {
                                                    Slog.w(TAG, "Background execution not allowed: receiving " + r.intent + " to " + componentName.flattenToShortString());
                                                    skip = true;
                                                }
                                            }
                                        }
                                        if (skip) {
                                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                                Slog.v(TAG_BROADCAST, "Skipping delivery of ordered [" + this.mQueueName + "] " + r + " for whatever reason");
                                            }
                                            r.delivery[recIdx] = 2;
                                            r.receiver = null;
                                            r.curFilter = null;
                                            r.state = MAX_BROADCAST_SUMMARY_HISTORY;
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
                                            if (app.thread != null) {
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
                                                    r.state = MAX_BROADCAST_SUMMARY_HISTORY;
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    return;
                                                }
                                            }
                                        }
                                        if (this.mService.mUserController.getCurrentUserIdLocked() == 0) {
                                            try {
                                                packageName = info.activityInfo.applicationInfo.packageName;
                                                sourceDir = info.activityInfo.applicationInfo.sourceDir;
                                                publicSourceDir = info.activityInfo.applicationInfo.publicSourceDir;
                                                applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, MAX_BROADCAST_SUMMARY_HISTORY, UserHandle.getUserId(r.callingUid));
                                                if (applicationInfo == null) {
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    return;
                                                } else if (!(applicationInfo.sourceDir.equals(sourceDir) && applicationInfo.publicSourceDir.equals(publicSourceDir))) {
                                                    Slog.e(TAG, packageName + " is replaced, sourceDir is changed from " + sourceDir + " to " + applicationInfo.sourceDir + ", publicSourceDir is changed from " + publicSourceDir + " to " + applicationInfo.publicSourceDir);
                                                    info.activityInfo.applicationInfo = applicationInfo;
                                                }
                                            } catch (RemoteException e8) {
                                            }
                                        }
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v(TAG_BROADCAST, "Need to start app [" + this.mQueueName + "] " + targetProcess + " for broadcast " + r);
                                        }
                                        startProcessLocked = this.mService.startProcessLocked(targetProcess, info.activityInfo.applicationInfo, true, r.intent.getFlags() | 4, "broadcast", r.curComponent, (r.intent.getFlags() & 33554432) != 0, false, false);
                                        r.curApp = startProcessLocked;
                                        if (startProcessLocked == null) {
                                            Slog.w(TAG, "Unable to launch app " + info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.applicationInfo.uid + " for broadcast " + r.intent + ": process is bad");
                                            logBroadcastReceiverDiscardLocked(r);
                                            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                            scheduleBroadcastsLocked();
                                            r.state = MAX_BROADCAST_SUMMARY_HISTORY;
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        }
                                        this.mPendingBroadcast = r;
                                        this.mPendingBroadcastRecvIndex = recIdx;
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        return;
                                    }
                                    filter = (BroadcastFilter) nextReceiver;
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Delivering ordered [" + this.mQueueName + "] to registered " + filter + ": " + r);
                                    }
                                    deliverToRegisteredReceiverLocked(r, filter, r.ordered, recIdx);
                                    if (r.receiver != null || !r.ordered) {
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v(TAG_BROADCAST, "Quick finishing [" + this.mQueueName + "]: ordered=" + r.ordered + " receiver=" + r.receiver);
                                        }
                                        r.state = MAX_BROADCAST_SUMMARY_HISTORY;
                                        scheduleBroadcastsLocked();
                                    } else if (brOptions != null && brOptions.getTemporaryAppWhitelistDuration() > 0) {
                                        scheduleTempWhitelistLocked(filter.owningUid, brOptions.getTemporaryAppWhitelistDuration(), r);
                                    }
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return;
                                }
                                Flog.i(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, "orderd " + this.mQueueName + " broadcast:(" + r + ") should be proxyed, target:(" + target + ")");
                                scheduleBroadcastsLocked();
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
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
                    Slog.v(TAG_BROADCAST, "Finished with ordered broadcast " + r);
                }
                addBroadcastToHistoryLocked(r);
                if (r.intent.getComponent() == null && r.intent.getPackage() == null && (r.intent.getFlags() & 1073741824) == 0) {
                    this.mService.addBroadcastStatLocked(r.intent.getAction(), r.callerPackage, r.manifestCount, r.manifestSkipCount, r.finishTime - r.dispatchTime);
                }
                this.mOrderedBroadcasts.remove(MAX_BROADCAST_SUMMARY_HISTORY);
                r = null;
                looped = true;
                continue;
                if (r != null) {
                    recIdx = r.nextReceiver;
                    r.nextReceiver = recIdx + 1;
                    target = r.receivers.get(recIdx);
                    if (enqueueProxyBroadcast(false, r, target)) {
                        r.receiverTime = SystemClock.uptimeMillis();
                        if (recIdx == 0) {
                            r.dispatchTime = r.receiverTime;
                            r.dispatchClockTime = System.currentTimeMillis();
                            Flog.i(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, "dispatch ordered broadcast [" + this.mQueueName + "] " + r + ", has " + r.receivers.size() + " receivers");
                            updateSRMSStatisticsData(r);
                        }
                        if (this.mPendingBroadcastTimeoutMessage) {
                            timeoutTime = r.receiverTime + this.mTimeoutPeriod;
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.v(TAG_BROADCAST, "Submitting BROADCAST_TIMEOUT_MSG [" + this.mQueueName + "] for " + r + " at " + timeoutTime);
                            }
                            setBroadcastTimeoutLocked(timeoutTime);
                        }
                        brOptions = r.options;
                        nextReceiver = r.receivers.get(recIdx);
                        if (nextReceiver instanceof BroadcastFilter) {
                            info = (ResolveInfo) nextReceiver;
                            componentName = new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
                            skip = false;
                            skip = true;
                            perm = this.mService.checkComponentPermission(info.activityInfo.permission, r.callingPid, r.callingUid, info.activityInfo.applicationInfo.uid, info.activityInfo.exported);
                            if (skip) {
                            }
                            opCode = AppOpsManager.permissionToOpCode(info.activityInfo.permission);
                            if (opCode != -1) {
                                if (this.mService.mAppOpsService.noteOperation(opCode, r.callingUid, r.callerPackage) != 0) {
                                    Slog.w(TAG, "Appop Denial: broadcasting " + r.intent.toString() + " from " + r.callerPackage + " (pid=" + r.callingPid + ", uid=" + r.callingUid + ")" + " requires appop " + AppOpsManager.permissionToOp(info.activityInfo.permission) + " due to registered receiver " + componentName.flattenToShortString());
                                    skip = true;
                                }
                            }
                            while (i < r.requiredPermissions.length) {
                                requiredPermission = r.requiredPermissions[i];
                                perm = AppGlobals.getPackageManager().checkPermission(requiredPermission, info.activityInfo.applicationInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                                if (perm != 0) {
                                    Slog.w(TAG, "Permission Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires " + requiredPermission + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                    skip = true;
                                    break;
                                }
                                appOp = AppOpsManager.permissionToOpCode(requiredPermission);
                                if (this.mService.mAppOpsService.noteOperation(appOp, info.activityInfo.applicationInfo.uid, info.activityInfo.packageName) != 0) {
                                    Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires appop " + AppOpsManager.permissionToOp(requiredPermission) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                    skip = true;
                                    break;
                                }
                                Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                                skip = true;
                                if (skip) {
                                    if (this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, info.activityInfo.applicationInfo.uid)) {
                                    }
                                }
                                skip = true;
                                isSingleton = false;
                                isSingleton = this.mService.isSingleton(info.activityInfo.processName, info.activityInfo.applicationInfo, info.activityInfo.name, info.activityInfo.flags);
                                Slog.w(TAG, "Permission Denial: Receiver " + componentName.flattenToShortString() + " requests FLAG_SINGLE_USER, but app does not hold " + "android.permission.INTERACT_ACROSS_USERS");
                                skip = true;
                                if (skip) {
                                    r.manifestSkipCount++;
                                } else {
                                    r.manifestCount++;
                                }
                                Slog.w(TAG, "Skipping deliver ordered [" + this.mQueueName + "] " + r + " to " + r.curApp + ": process crashing");
                                skip = true;
                                if (skip) {
                                    isAvailable = false;
                                    isAvailable = AppGlobals.getPackageManager().isPackageAvailable(info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                                    if (isAvailable) {
                                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                            Slog.v(TAG_BROADCAST, "Skipping delivery to " + info.activityInfo.packageName + " / " + info.activityInfo.applicationInfo.uid + " : package no longer available");
                                        }
                                        skip = true;
                                    }
                                }
                                if (HwServiceFactory.getHwNLPManager().shouldSkipGoogleNlp(r.intent, info.activityInfo.processName)) {
                                    skip = true;
                                }
                                skip = true;
                                receiverUid = info.activityInfo.applicationInfo.uid;
                                info.activityInfo = this.mService.getActivityInfoForUser(info.activityInfo, MAX_BROADCAST_SUMMARY_HISTORY);
                                targetProcess = info.activityInfo.processName;
                                app = this.mService.getProcessRecordLocked(targetProcess, info.activityInfo.applicationInfo.uid + info.activityInfo.applicationInfo.euid, false);
                                if (skip) {
                                    allowed = this.mService.checkAllowBackgroundLocked(info.activityInfo.applicationInfo.uid, info.activityInfo.packageName, -1, false);
                                    if (allowed != 0) {
                                        if (allowed == 2) {
                                            Slog.w(TAG, "Background execution not allowed: receiving " + r.intent + " to " + componentName.flattenToShortString());
                                            skip = true;
                                        } else {
                                            Slog.w(TAG, "Background execution disabled: receiving " + r.intent + " to " + componentName.flattenToShortString());
                                            skip = true;
                                        }
                                    }
                                }
                                if (skip) {
                                    r.delivery[recIdx] = 1;
                                    r.state = 1;
                                    r.curComponent = componentName;
                                    r.curReceiver = info.activityInfo;
                                    Slog.v(TAG_MU, "Updated broadcast record activity info for secondary user, " + info.activityInfo + ", callingUid = " + r.callingUid + ", uid = " + info.activityInfo.applicationInfo.uid);
                                    scheduleTempWhitelistLocked(receiverUid, brOptions.getTemporaryAppWhitelistDuration(), r);
                                    AppGlobals.getPackageManager().setPackageStoppedState(r.curComponent.getPackageName(), false, UserHandle.getUserId(r.callingUid));
                                    if (app != null) {
                                        if (app.thread != null) {
                                            app.addPackage(info.activityInfo.packageName, info.activityInfo.applicationInfo.versionCode, this.mService.mProcessStats);
                                            processCurBroadcastLocked(r, app);
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        }
                                    }
                                    if (this.mService.mUserController.getCurrentUserIdLocked() == 0) {
                                        packageName = info.activityInfo.applicationInfo.packageName;
                                        sourceDir = info.activityInfo.applicationInfo.sourceDir;
                                        publicSourceDir = info.activityInfo.applicationInfo.publicSourceDir;
                                        applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, MAX_BROADCAST_SUMMARY_HISTORY, UserHandle.getUserId(r.callingUid));
                                        if (applicationInfo == null) {
                                            Slog.e(TAG, packageName + " is replaced, sourceDir is changed from " + sourceDir + " to " + applicationInfo.sourceDir + ", publicSourceDir is changed from " + publicSourceDir + " to " + applicationInfo.publicSourceDir);
                                            info.activityInfo.applicationInfo = applicationInfo;
                                        } else {
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return;
                                        }
                                    }
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Need to start app [" + this.mQueueName + "] " + targetProcess + " for broadcast " + r);
                                    }
                                    if ((r.intent.getFlags() & 33554432) != 0) {
                                    }
                                    startProcessLocked = this.mService.startProcessLocked(targetProcess, info.activityInfo.applicationInfo, true, r.intent.getFlags() | 4, "broadcast", r.curComponent, (r.intent.getFlags() & 33554432) != 0, false, false);
                                    r.curApp = startProcessLocked;
                                    if (startProcessLocked == null) {
                                        this.mPendingBroadcast = r;
                                        this.mPendingBroadcastRecvIndex = recIdx;
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        return;
                                    }
                                    Slog.w(TAG, "Unable to launch app " + info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.applicationInfo.uid + " for broadcast " + r.intent + ": process is bad");
                                    logBroadcastReceiverDiscardLocked(r);
                                    finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                    scheduleBroadcastsLocked();
                                    r.state = MAX_BROADCAST_SUMMARY_HISTORY;
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return;
                                }
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                    Slog.v(TAG_BROADCAST, "Skipping delivery of ordered [" + this.mQueueName + "] " + r + " for whatever reason");
                                }
                                r.delivery[recIdx] = 2;
                                r.receiver = null;
                                r.curFilter = null;
                                r.state = MAX_BROADCAST_SUMMARY_HISTORY;
                                scheduleBroadcastsLocked();
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            Slog.w(TAG, "Appop Denial: receiving " + r.intent + " to " + componentName.flattenToShortString() + " requires appop " + AppOpsManager.opToName(r.appOp) + " due to sender " + r.callerPackage + " (uid " + r.callingUid + ")");
                            skip = true;
                            if (skip) {
                                if (this.mService.mIntentFirewall.checkBroadcast(r.intent, r.callingUid, r.callingPid, r.resolvedType, info.activityInfo.applicationInfo.uid)) {
                                }
                            }
                            skip = true;
                            isSingleton = false;
                            isSingleton = this.mService.isSingleton(info.activityInfo.processName, info.activityInfo.applicationInfo, info.activityInfo.name, info.activityInfo.flags);
                            Slog.w(TAG, "Permission Denial: Receiver " + componentName.flattenToShortString() + " requests FLAG_SINGLE_USER, but app does not hold " + "android.permission.INTERACT_ACROSS_USERS");
                            skip = true;
                            if (skip) {
                                r.manifestCount++;
                            } else {
                                r.manifestSkipCount++;
                            }
                            Slog.w(TAG, "Skipping deliver ordered [" + this.mQueueName + "] " + r + " to " + r.curApp + ": process crashing");
                            skip = true;
                            if (skip) {
                                isAvailable = false;
                                isAvailable = AppGlobals.getPackageManager().isPackageAvailable(info.activityInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid));
                                if (isAvailable) {
                                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                        Slog.v(TAG_BROADCAST, "Skipping delivery to " + info.activityInfo.packageName + " / " + info.activityInfo.applicationInfo.uid + " : package no longer available");
                                    }
                                    skip = true;
                                }
                            }
                            if (HwServiceFactory.getHwNLPManager().shouldSkipGoogleNlp(r.intent, info.activityInfo.processName)) {
                                skip = true;
                            }
                            skip = true;
                            receiverUid = info.activityInfo.applicationInfo.uid;
                            info.activityInfo = this.mService.getActivityInfoForUser(info.activityInfo, MAX_BROADCAST_SUMMARY_HISTORY);
                            targetProcess = info.activityInfo.processName;
                            app = this.mService.getProcessRecordLocked(targetProcess, info.activityInfo.applicationInfo.uid + info.activityInfo.applicationInfo.euid, false);
                            if (skip) {
                                allowed = this.mService.checkAllowBackgroundLocked(info.activityInfo.applicationInfo.uid, info.activityInfo.packageName, -1, false);
                                if (allowed != 0) {
                                    if (allowed == 2) {
                                        Slog.w(TAG, "Background execution disabled: receiving " + r.intent + " to " + componentName.flattenToShortString());
                                        skip = true;
                                    } else {
                                        Slog.w(TAG, "Background execution not allowed: receiving " + r.intent + " to " + componentName.flattenToShortString());
                                        skip = true;
                                    }
                                }
                            }
                            if (skip) {
                                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                    Slog.v(TAG_BROADCAST, "Skipping delivery of ordered [" + this.mQueueName + "] " + r + " for whatever reason");
                                }
                                r.delivery[recIdx] = 2;
                                r.receiver = null;
                                r.curFilter = null;
                                r.state = MAX_BROADCAST_SUMMARY_HISTORY;
                                scheduleBroadcastsLocked();
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            r.delivery[recIdx] = 1;
                            r.state = 1;
                            r.curComponent = componentName;
                            r.curReceiver = info.activityInfo;
                            Slog.v(TAG_MU, "Updated broadcast record activity info for secondary user, " + info.activityInfo + ", callingUid = " + r.callingUid + ", uid = " + info.activityInfo.applicationInfo.uid);
                            scheduleTempWhitelistLocked(receiverUid, brOptions.getTemporaryAppWhitelistDuration(), r);
                            AppGlobals.getPackageManager().setPackageStoppedState(r.curComponent.getPackageName(), false, UserHandle.getUserId(r.callingUid));
                            if (app != null) {
                                if (app.thread != null) {
                                    app.addPackage(info.activityInfo.packageName, info.activityInfo.applicationInfo.versionCode, this.mService.mProcessStats);
                                    processCurBroadcastLocked(r, app);
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return;
                                }
                            }
                            if (this.mService.mUserController.getCurrentUserIdLocked() == 0) {
                                packageName = info.activityInfo.applicationInfo.packageName;
                                sourceDir = info.activityInfo.applicationInfo.sourceDir;
                                publicSourceDir = info.activityInfo.applicationInfo.publicSourceDir;
                                applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, MAX_BROADCAST_SUMMARY_HISTORY, UserHandle.getUserId(r.callingUid));
                                if (applicationInfo == null) {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return;
                                }
                                Slog.e(TAG, packageName + " is replaced, sourceDir is changed from " + sourceDir + " to " + applicationInfo.sourceDir + ", publicSourceDir is changed from " + publicSourceDir + " to " + applicationInfo.publicSourceDir);
                                info.activityInfo.applicationInfo = applicationInfo;
                            }
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.v(TAG_BROADCAST, "Need to start app [" + this.mQueueName + "] " + targetProcess + " for broadcast " + r);
                            }
                            if ((r.intent.getFlags() & 33554432) != 0) {
                            }
                            startProcessLocked = this.mService.startProcessLocked(targetProcess, info.activityInfo.applicationInfo, true, r.intent.getFlags() | 4, "broadcast", r.curComponent, (r.intent.getFlags() & 33554432) != 0, false, false);
                            r.curApp = startProcessLocked;
                            if (startProcessLocked == null) {
                                Slog.w(TAG, "Unable to launch app " + info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.applicationInfo.uid + " for broadcast " + r.intent + ": process is bad");
                                logBroadcastReceiverDiscardLocked(r);
                                finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
                                scheduleBroadcastsLocked();
                                r.state = MAX_BROADCAST_SUMMARY_HISTORY;
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            this.mPendingBroadcast = r;
                            this.mPendingBroadcastRecvIndex = recIdx;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return;
                        }
                        filter = (BroadcastFilter) nextReceiver;
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG_BROADCAST, "Delivering ordered [" + this.mQueueName + "] to registered " + filter + ": " + r);
                        }
                        deliverToRegisteredReceiverLocked(r, filter, r.ordered, recIdx);
                        if (r.receiver != null) {
                        }
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG_BROADCAST, "Quick finishing [" + this.mQueueName + "]: ordered=" + r.ordered + " receiver=" + r.receiver);
                        }
                        r.state = MAX_BROADCAST_SUMMARY_HISTORY;
                        scheduleBroadcastsLocked();
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    Flog.i(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, "orderd " + this.mQueueName + " broadcast:(" + r + ") should be proxyed, target:(" + target + ")");
                    scheduleBroadcastsLocked();
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return;
                }
            }
            this.mService.scheduleAppGcsLocked();
            if (looped) {
                this.mService.updateOomAdjLocked();
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
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
            BroadcastRecord r = (BroadcastRecord) this.mOrderedBroadcasts.get(MAX_BROADCAST_SUMMARY_HISTORY);
            if (fromMsg) {
                if (this.mService.mDidDexOpt) {
                    this.mService.mDidDexOpt = false;
                    setBroadcastTimeoutLocked(SystemClock.uptimeMillis() + this.mTimeoutPeriod);
                    return;
                } else if (this.mService.mProcessesReady) {
                    long timeoutTime = r.receiverTime + this.mTimeoutPeriod;
                    if (timeoutTime > now) {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG_BROADCAST, "Premature timeout [" + this.mQueueName + "] @ " + now + ": resetting BROADCAST_TIMEOUT_MSG for " + timeoutTime);
                        }
                        setBroadcastTimeoutLocked(timeoutTime);
                        return;
                    }
                } else {
                    return;
                }
            }
            BroadcastRecord br = (BroadcastRecord) this.mOrderedBroadcasts.get(MAX_BROADCAST_SUMMARY_HISTORY);
            if (br.state == 4) {
                Slog.i(TAG, "Waited long enough for: " + (br.curComponent != null ? br.curComponent.flattenToShortString() : "(null)"));
                br.curComponent = null;
                br.state = MAX_BROADCAST_SUMMARY_HISTORY;
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
            ProcessRecord processRecord = null;
            String anrMessage = null;
            BroadcastFilter curReceiver = r.receivers.get(r.nextReceiver - 1);
            r.delivery[r.nextReceiver - 1] = 3;
            Slog.w(TAG, "Receiver during timeout: " + curReceiver);
            logBroadcastReceiverDiscardLocked(r);
            if (curReceiver instanceof BroadcastFilter) {
                BroadcastFilter bf = curReceiver;
                if (!(bf.receiverList.pid == 0 || bf.receiverList.pid == ActivityManagerService.MY_PID)) {
                    synchronized (this.mService.mPidsSelfLocked) {
                        processRecord = (ProcessRecord) this.mService.mPidsSelfLocked.get(bf.receiverList.pid);
                    }
                }
            } else {
                processRecord = r.curApp;
            }
            if (processRecord != null) {
                anrMessage = "Broadcast of " + r.intent.toString();
            }
            if (this.mPendingBroadcast == r) {
                this.mPendingBroadcast = null;
            }
            finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
            scheduleBroadcastsLocked();
            if (anrMessage != null) {
                this.mHandler.post(new AppNotResponding(processRecord, anrMessage));
            }
        }
    }

    private final int ringAdvance(int x, int increment, int ringSize) {
        x += increment;
        if (x < 0) {
            return ringSize - 1;
        }
        if (x >= ringSize) {
            return MAX_BROADCAST_SUMMARY_HISTORY;
        }
        return x;
    }

    private final void addBroadcastToHistoryLocked(BroadcastRecord r) {
        if (r.callingUid >= 0) {
            r.finishTime = SystemClock.uptimeMillis();
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

    /* JADX WARNING: inconsistent code. */
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
                    if (!dumpPackage.equals(br.callerPackage)) {
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
                    if (!dumpPackage.equals(br.callerPackage)) {
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
                    if (!dumpPackage.equals(r.callerPackage)) {
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
                        pw.print("    extras: ");
                        pw.println(bundle.toString());
                        continue;
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
                    if (!(bundle == null || "android.intent.action.PHONE_STATE".equals(intent.getAction()) || "android.intent.action.NEW_OUTGOING_CALL".equals(intent.getAction()))) {
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
