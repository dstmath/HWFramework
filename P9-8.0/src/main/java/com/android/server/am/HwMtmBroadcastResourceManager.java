package com.android.server.am;

import android.content.IIntentReceiver;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.brjob.StartupByBroadcastRecorder;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDebug;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.ArrayList;
import java.util.List;

public class HwMtmBroadcastResourceManager implements AbsHwMtmBroadcastResourceManager {
    private static final String TAG = "HwMtmBroadcastResourceManager";
    private AwareJobSchedulerService mAwareJobSchedulerService = null;
    private AwareBroadcastDumpRadar mBrDumpRadar = null;
    private AwareBroadcastPolicy mIawareBrPolicy = null;
    private final BroadcastQueue mQueue;

    public HwMtmBroadcastResourceManager(BroadcastQueue queue) {
        this.mQueue = queue;
    }

    public boolean iawareProcessBroadcast(int type, boolean isParallel, BroadcastRecord r, Object target) {
        if (type == 0) {
            boolean enqueue = enqueueIawareProxyBroacast(isParallel, r, target);
            trackBrFlow(enqueue, isParallel, r, target);
            return enqueue;
        } else if (type == 1) {
            return processBroadcastScheduler(isParallel, r, target);
        } else {
            return false;
        }
    }

    private boolean processBroadcastScheduler(boolean isParallel, BroadcastRecord r, Object target) {
        if (isParallel || r == null || target == null || ((target instanceof ResolveInfo) ^ 1) != 0) {
            AwareLog.e(TAG, "iaware_brjob processBroadcastScheduler param error!");
            return false;
        }
        ResolveInfo info = (ResolveInfo) target;
        String packageName = null;
        ComponentInfo ci = info.getComponentInfo();
        if (!(ci == null || ci.applicationInfo == null)) {
            packageName = ci.applicationInfo.packageName;
        }
        String action = r.intent.getAction();
        IntentFilter filter = info.filter;
        if (filter == null || filter.countActionFilters() <= 0) {
            Object obj;
            String str = TAG;
            StringBuilder append = new StringBuilder().append("iaware_brjob not process: ").append(info).append(", filter: ");
            if (filter == null) {
                obj = "null";
            } else {
                IntentFilter obj2 = filter;
            }
            AwareLog.w(str, append.append(obj2).append(", count: ").append(filter == null ? "null" : Integer.valueOf(filter.countActionFilters())).toString());
            StartupByBroadcastRecorder.getInstance().recordStartupTimeByBroadcast(packageName, action, System.currentTimeMillis());
            trackImplicitBr(false, true, packageName, action);
        } else if (r.iawareCtrlType == 2) {
            StartupByBroadcastRecorder.getInstance().recordStartupTimeByBroadcast(packageName, action, System.currentTimeMillis());
            trackImplicitBr(true, true, packageName, action);
        } else if (r.queue == null || !enqueueBroadcastScheduler(isParallel, r, target)) {
            trackImplicitBr(false, true, packageName, action);
        } else {
            trackImplicitBr(true, false, packageName, action);
            AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
            if (policy != null) {
                policy.updateBroadJobCtrlBigData(packageName);
            }
            r.queue.finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false);
            r.queue.scheduleBroadcastsLocked();
            r.state = 0;
            return true;
        }
        return false;
    }

    private boolean enqueueBroadcastScheduler(boolean isParallel, BroadcastRecord r, Object target) {
        if (getAwareJobSchedulerService() == null) {
            return false;
        }
        if (isSystemApplication(target)) {
            IIntentReceiver resultTo = r.resultTo;
            if (r.resultTo != null) {
                AwareLog.d(TAG, "reset resultTo null");
                resultTo = null;
            }
            List<Object> receiver = new ArrayList();
            receiver.add(target);
            return this.mAwareJobSchedulerService.schedule(new HwBroadcastRecord(new BroadcastRecord(r.queue, r.intent, r.callerApp, r.callerPackage, r.callingPid, r.callingUid, r.callerInstantApp, r.resolvedType, r.requiredPermissions, r.appOp, r.options, receiver, resultTo, r.resultCode, r.resultData, r.resultExtras, r.ordered, r.sticky, r.initialSticky, r.userId)));
        }
        AwareLog.w(TAG, "iaware_brjob not system app");
        return false;
    }

    private boolean enqueueIawareProxyBroacast(boolean isParallel, BroadcastRecord r, Object target) {
        if (isAbnormalParameters(isParallel, r, target)) {
            return false;
        }
        if (getIawareBrPolicy() == null) {
            return false;
        }
        if (r.iawareCtrlType == 1) {
            return false;
        }
        if (!this.mIawareBrPolicy.isProxyedAllowedCondition()) {
            return false;
        }
        if (isThirdOrKeyBroadcast()) {
            return false;
        }
        String pkg = getPkg(target);
        int pid = getPid(target);
        int uid = getUid(target);
        if (pkg == null || pid == -1 || uid == -1) {
            return false;
        }
        String action = null;
        if (r.intent != null) {
            action = r.intent.getAction();
        }
        if ((isSystemApplication(target) || isInstrumentationApp(target)) && !this.mIawareBrPolicy.isProxySysPkg(pkg)) {
            return false;
        }
        if (!this.mIawareBrPolicy.shouldIawareProxyBroadcast(action, r.callingPid, uid, pid, pkg)) {
            return false;
        }
        List<Object> receiver = new ArrayList();
        receiver.add(target);
        BroadcastRecord proxyBR = new BroadcastRecord(r.queue, r.intent, r.callerApp, r.callerPackage, r.callingPid, r.callingUid, r.callerInstantApp, r.resolvedType, r.requiredPermissions, r.appOp, r.options, receiver, r.resultTo, r.resultCode, r.resultData, r.resultExtras, r.ordered, r.sticky, r.initialSticky, r.userId);
        proxyBR.dispatchClockTime = r.dispatchClockTime;
        proxyBR.dispatchTime = r.dispatchTime;
        proxyBR.iawareCtrlType = 1;
        HwBroadcastRecord hwBroadcastRecord = new HwBroadcastRecord(proxyBR);
        hwBroadcastRecord.setReceiverUid(uid);
        return this.mIawareBrPolicy.enqueueIawareProxyBroacast(isParallel, hwBroadcastRecord);
    }

    public void iawareStartCountBroadcastSpeed(boolean isParallel, BroadcastRecord r) {
        if (r != null && getIawareBrPolicy() != null && isParallel && r.iawareCtrlType != 1) {
            String action = null;
            if (r.intent != null) {
                action = r.intent.getAction();
            }
            if (action != null) {
                this.mIawareBrPolicy.iawareStartCountBroadcastSpeed(isParallel, r.dispatchClockTime, r.receivers.size());
            }
        }
    }

    public void iawareEndCountBroadcastSpeed(BroadcastRecord r) {
        if (r != null && getIawareBrPolicy() != null && r.iawareCtrlType != 1) {
            scheduleTrackBrFlowData();
            this.mIawareBrPolicy.endCheckCount();
        }
    }

    private boolean isAbnormalParameters(boolean isParallel, BroadcastRecord r, Object target) {
        if (!isParallel || r == null || target == null) {
            return true;
        }
        return false;
    }

    private AwareBroadcastPolicy getIawareBrPolicy() {
        if (this.mIawareBrPolicy == null && MultiTaskManagerService.self() != null) {
            this.mIawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
        }
        return this.mIawareBrPolicy;
    }

    private AwareJobSchedulerService getAwareJobSchedulerService() {
        if (this.mAwareJobSchedulerService == null && MultiTaskManagerService.self() != null) {
            this.mAwareJobSchedulerService = MultiTaskManagerService.self().getAwareJobSchedulerService();
        }
        return this.mAwareJobSchedulerService;
    }

    public static void insertIawareBroadcast(ArrayList<HwBroadcastRecord> parallelList, String name) {
        if (parallelList != null && parallelList.size() != 0) {
            BroadcastQueue queue = ((HwBroadcastRecord) parallelList.get(0)).getBroacastQueue();
            synchronized (queue.mService) {
                int listSize = parallelList.size();
                for (int i = 0; i < listSize; i++) {
                    queue.mParallelBroadcasts.add(i, ((HwBroadcastRecord) parallelList.get(i)).getBroadcastRecord());
                }
                queue.scheduleBroadcastsLocked();
            }
        }
    }

    private String getPkg(Object target) {
        if (target instanceof BroadcastFilter) {
            return ((BroadcastFilter) target).packageName;
        }
        return null;
    }

    private int getPid(Object target) {
        if (!(target instanceof BroadcastFilter)) {
            return -1;
        }
        BroadcastFilter filter = (BroadcastFilter) target;
        if (filter.receiverList == null) {
            return -1;
        }
        int pid = filter.receiverList.pid;
        if (pid > 0 || filter.receiverList.app == null) {
            return pid;
        }
        return filter.receiverList.app.pid;
    }

    private int getUid(Object target) {
        if (!(target instanceof BroadcastFilter)) {
            return -1;
        }
        BroadcastFilter filter = (BroadcastFilter) target;
        if (filter.receiverList == null) {
            return -1;
        }
        int uid = filter.receiverList.uid;
        if (uid > 0 || filter.receiverList.app == null) {
            return uid;
        }
        return filter.receiverList.app.uid;
    }

    private boolean isSystemApplication(Object target) {
        if (target instanceof BroadcastFilter) {
            BroadcastFilter filter = (BroadcastFilter) target;
            if (filter.receiverList != null && filter.receiverList.app != null) {
                int flags = filter.receiverList.app.info.flags;
                int privateFlags = filter.receiverList.app.info.privateFlags;
                if ((flags & 1) == 0 && (flags & 128) == 0) {
                    return (privateFlags & 8) != 0;
                } else {
                    return true;
                }
            } else if (!AwareBroadcastDebug.getDebugDetail()) {
                return false;
            } else {
                AwareLog.d(TAG, "isSystemApplication BroadcastFilter: filter something is null");
                return false;
            }
        } else if (!(target instanceof ResolveInfo)) {
            return false;
        } else {
            ResolveInfo info = (ResolveInfo) target;
            if (info.activityInfo == null || info.activityInfo.applicationInfo == null) {
                if (!AwareBroadcastDebug.getDebugDetail()) {
                    return false;
                }
                AwareLog.w(TAG, "isSystemApplication ResolveInfo: info, info.activityInfo, info.activityInfo.applicationInfo is null ");
                return false;
            } else if (info.activityInfo.applicationInfo.isSystemApp() || info.activityInfo.applicationInfo.isPrivilegedApp()) {
                return true;
            } else {
                return info.activityInfo.applicationInfo.isUpdatedSystemApp();
            }
        }
    }

    private boolean isInstrumentationApp(Object target) {
        boolean instrumentationApp = false;
        if (target instanceof BroadcastFilter) {
            BroadcastFilter filter = (BroadcastFilter) target;
            if (!(filter.receiverList == null || filter.receiverList.app == null || filter.receiverList.app.instr == null)) {
                instrumentationApp = true;
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(TAG, "instrumentation app do not proxy!");
                }
            }
        }
        return instrumentationApp;
    }

    private boolean isThirdOrKeyBroadcast() {
        return (!MemoryConstant.MEM_REPAIR_CONSTANT_BG.equals(this.mQueue.mQueueName) ? MemoryConstant.MEM_REPAIR_CONSTANT_FG.equals(this.mQueue.mQueueName) : 1) ^ 1;
    }

    private AwareBroadcastDumpRadar getBrDumpRadar() {
        if (this.mBrDumpRadar == null && MultiTaskManagerService.self() != null) {
            this.mBrDumpRadar = MultiTaskManagerService.self().getIawareBrRadar();
        }
        return this.mBrDumpRadar;
    }

    private void trackBrFlow(boolean enqueue, boolean isParallel, BroadcastRecord r, Object target) {
        if (getBrDumpRadar() != null && r != null && isParallel && target != null && ((target instanceof BroadcastFilter) ^ 1) == 0) {
            String packageName = ((BroadcastFilter) target).packageName;
            String action = r.intent == null ? null : r.intent.getAction();
            boolean isProxyed = r.iawareCtrlType == 1;
            AwareBroadcastPolicy policy = getIawareBrPolicy();
            this.mBrDumpRadar.trackBrFlowDetail(enqueue, isProxyed, policy == null ? false : policy.getStartProxy(), packageName, action);
            this.mBrDumpRadar.trackBrFlowSpeed(enqueue, isProxyed);
        }
    }

    private void scheduleTrackBrFlowData() {
        if (getBrDumpRadar() != null) {
            this.mBrDumpRadar.scheduleTrackBrFlowData();
        }
    }

    private void trackImplicitBr(boolean inControl, boolean startup, String packageName, String action) {
        if (getBrDumpRadar() != null && packageName != null && action != null) {
            this.mBrDumpRadar.trackImplicitBrDetail(inControl, startup, packageName, action);
            this.mBrDumpRadar.scheduleTrackBrFlowData();
        }
    }

    public static void insertIawareOrderedBroadcast(HwBroadcastRecord hwBr) {
        if (hwBr != null) {
            BroadcastRecord r = hwBr.getBroadcastRecord();
            if (r != null && r.queue != null) {
                synchronized (r.queue.mService) {
                    r.iawareCtrlType = 2;
                    r.queue.enqueueOrderedBroadcastLocked(r);
                    r.queue.scheduleBroadcastsLocked();
                }
            }
        }
    }
}
