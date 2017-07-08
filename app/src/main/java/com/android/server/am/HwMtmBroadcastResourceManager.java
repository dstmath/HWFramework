package com.android.server.am;

import android.rms.iaware.AwareLog;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDebug;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.util.ArrayList;
import java.util.List;

public class HwMtmBroadcastResourceManager implements AbsHwMtmBroadcastResourceManager {
    private static final String TAG = "HwMtmBroadcastResourceManager";
    private AwareBroadcastPolicy mIawareBrPolicy;
    private final BroadcastQueue mQueue;

    public HwMtmBroadcastResourceManager(BroadcastQueue queue) {
        this.mIawareBrPolicy = null;
        this.mQueue = queue;
    }

    public boolean iawareProcessBroadcast(int type, boolean isParallel, BroadcastRecord r, Object target) {
        if (type == 0) {
            return enqueueIawareProxyBroacast(isParallel, r, target);
        }
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
        BroadcastRecord proxyBR = new BroadcastRecord(r.queue, r.intent, r.callerApp, r.callerPackage, r.callingPid, r.callingUid, r.resolvedType, r.requiredPermissions, r.appOp, r.options, receiver, r.resultTo, r.resultCode, r.resultData, r.resultExtras, r.ordered, r.sticky, r.initialSticky, r.userId);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void insertIawareBroadcast(ArrayList<HwBroadcastRecord> parallelList, String name) {
        if (parallelList != null && parallelList.size() != 0) {
            BroadcastQueue queue = ((HwBroadcastRecord) parallelList.get(0)).getBroacastQueue();
            synchronized (queue.mService) {
                int i = 0;
                while (true) {
                    if (i < parallelList.size()) {
                        queue.mParallelBroadcasts.add(i, ((HwBroadcastRecord) parallelList.get(i)).getBroadcastRecord());
                        i++;
                    } else {
                        queue.scheduleBroadcastsLocked();
                    }
                }
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
        if (!(target instanceof BroadcastFilter)) {
            return false;
        }
        BroadcastFilter filter = (BroadcastFilter) target;
        if (filter.receiverList != null && filter.receiverList.app != null) {
            int flags = filter.receiverList.app.info.flags;
            int privateFlags = filter.receiverList.app.info.privateFlags;
            if ((flags & 1) == 0 && (flags & HwSecDiagnoseConstant.BIT_VERIFYBOOT) == 0) {
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
    }

    private boolean isInstrumentationApp(Object target) {
        boolean instrumentationApp = false;
        if (target instanceof BroadcastFilter) {
            BroadcastFilter filter = (BroadcastFilter) target;
            if (!(filter.receiverList == null || filter.receiverList.app == null || filter.receiverList.app.instrumentationClass == null)) {
                instrumentationApp = true;
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(TAG, "instrumentation app do not proxy!");
                }
            }
        }
        return instrumentationApp;
    }

    private boolean isThirdOrKeyBroadcast() {
        return ("background".equals(this.mQueue.mQueueName) || "foreground".equals(this.mQueue.mQueueName)) ? false : true;
    }
}
