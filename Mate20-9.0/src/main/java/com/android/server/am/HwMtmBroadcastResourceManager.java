package com.android.server.am;

import android.app.BroadcastOptions;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Process;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.brjob.StartupByBroadcastRecorder;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDebug;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.mtm.iaware.srms.AwareBroadcastRegister;
import com.android.server.mtm.iaware.srms.AwareBroadcastSend;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.srms.BroadcastExFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HwMtmBroadcastResourceManager implements AbsHwMtmBroadcastResourceManager {
    public static final int CACHED_APP_MIN_ADJ = 900;
    public static final int FOREGROUND_APP_ADJ = 0;
    private static final int INVALID_CONFIG_POLICY = -1;
    private static final String TAG = "HwMtmBroadcastResourceManager";
    public static final int TOP_APP = 2;
    private AwareBroadcastRegister mAwareBRRegister = null;
    private AwareBroadcastSend mAwareBRSend = null;
    private AwareJobSchedulerService mAwareJobSchedulerService = null;
    private HashMap<ReceiverList, String> mBRIdMap = new HashMap<>();
    private AwareBroadcastDumpRadar mBrDumpRadar = null;
    private AwareBroadcastPolicy mIawareBrPolicy = null;
    private final BroadcastQueue mQueue;
    private int mSysServicePid = Process.myPid();

    public HwMtmBroadcastResourceManager(BroadcastQueue queue) {
        this.mQueue = queue;
        AwareBroadcastPolicy.initBrCache(this.mQueue.mQueueName, this.mQueue.mService);
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

    public void iawareCheckCombinedConditon(IntentFilter filter) {
        if (filter == null) {
            AwareLog.e(TAG, "iawareCheckCombinedConditon param error!");
            return;
        }
        if (this.mAwareBRRegister == null) {
            this.mAwareBRRegister = AwareBroadcastRegister.getInstance();
        }
        String acId = this.mAwareBRRegister.findMatchedAssembleConditionId(filter.getIdentifier());
        if (acId != null) {
            Iterator<String> actions = filter.actionsIterator();
            if (actions != null) {
                while (actions.hasNext()) {
                    String condition = this.mAwareBRRegister.getBRAssembleCondition(acId, actions.next());
                    if (condition != null) {
                        filter.addCategory(condition);
                        if (AwareBroadcastDebug.getDebugDetail()) {
                            AwareLog.i(TAG, "brreg: add condition: " + condition + " for " + filter.getIdentifier());
                        }
                    }
                }
            }
        }
    }

    public void iawareCountDuplicatedReceiver(boolean isRegister, ReceiverList rl, IntentFilter filter) {
        String brId;
        if (rl == null || (isRegister && filter == null)) {
            AwareLog.e(TAG, "iawareCountDuplicatedReceiver param error!");
            return;
        }
        if (this.mAwareBRRegister == null) {
            this.mAwareBRRegister = AwareBroadcastRegister.getInstance();
        }
        if (isRegister) {
            brId = filter.getIdentifier();
            this.mBRIdMap.put(rl, brId);
        } else {
            brId = this.mBRIdMap.remove(rl);
        }
        int brCount = this.mAwareBRRegister.countReceiverRegister(isRegister, brId);
        if (AwareBroadcastDebug.getDebugDetail()) {
            StringBuilder sb = new StringBuilder();
            sb.append("brreg: regCounter, ");
            sb.append(isRegister ? "register" : "unregister");
            sb.append(" brId: ");
            sb.append(brId);
            sb.append(" count:");
            sb.append(brCount);
            AwareLog.i(TAG, sb.toString());
        }
    }

    public boolean iawareNeedSkipBroadcastSend(String action, Object[] data) {
        if (action == null || data == null) {
            AwareLog.e(TAG, "iawareNeedSkipBroadcastSend param error!");
            return false;
        } else if (!BroadcastExFeature.isFeatureEnabled(2)) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "BrSend feature not enabled!");
            }
            return false;
        } else {
            if (this.mAwareBRSend == null) {
                this.mAwareBRSend = AwareBroadcastSend.getInstance();
            }
            if (this.mAwareBRSend.setData(action, data)) {
                return this.mAwareBRSend.needSkipBroadcastSend(action);
            }
            return false;
        }
    }

    private boolean processBroadcastScheduler(boolean isParallel, BroadcastRecord r, Object target) {
        IntentFilter filter;
        BroadcastRecord broadcastRecord = r;
        Object obj = target;
        if (isParallel || broadcastRecord == null || obj == null || !(obj instanceof ResolveInfo)) {
            AwareLog.e(TAG, "iaware_brjob processBroadcastScheduler param error!");
            return false;
        }
        ResolveInfo info = (ResolveInfo) obj;
        String packageName = null;
        ComponentInfo ci = info.getComponentInfo();
        if (!(ci == null || ci.applicationInfo == null)) {
            packageName = ci.applicationInfo.packageName;
        }
        String packageName2 = packageName;
        String action = broadcastRecord.intent.getAction();
        IntentFilter filter2 = info.filter;
        if (filter2 == null || filter2.countActionFilters() <= 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("iaware_brjob not process: ");
            sb.append(info);
            sb.append(", filter: ");
            IntentFilter filter3 = filter2;
            sb.append(filter3 == null ? "null" : filter3);
            sb.append(", count: ");
            sb.append(filter3 == null ? "null" : Integer.valueOf(filter3.countActionFilters()));
            AwareLog.w(TAG, sb.toString());
            StartupByBroadcastRecorder.getInstance().recordStartupTimeByBroadcast(packageName2, action, System.currentTimeMillis());
            trackImplicitBr(false, true, packageName2, action);
        } else {
            if (broadcastRecord.iawareCtrlType == 2) {
                filter = filter2;
                StartupByBroadcastRecorder.getInstance().recordStartupTimeByBroadcast(packageName2, action, System.currentTimeMillis());
                trackImplicitBr(true, true, packageName2, action);
            } else if (broadcastRecord.queue == null || !enqueueBroadcastScheduler(isParallel, r, target)) {
                filter = filter2;
                trackImplicitBr(false, true, packageName2, action);
            } else {
                trackImplicitBr(true, false, packageName2, action);
                AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
                if (policy != null) {
                    policy.updateBroadJobCtrlBigData(packageName2);
                }
                AwareAppStartupPolicy awareAppStartupPolicy = policy;
                IntentFilter intentFilter = filter2;
                broadcastRecord.queue.finishReceiverLocked(broadcastRecord, broadcastRecord.resultCode, broadcastRecord.resultData, broadcastRecord.resultExtras, broadcastRecord.resultAbort, false);
                broadcastRecord.queue.scheduleBroadcastsLocked();
                broadcastRecord.state = 0;
                return true;
            }
        }
        return false;
    }

    private boolean enqueueBroadcastScheduler(boolean isParallel, BroadcastRecord r, Object target) {
        BroadcastRecord broadcastRecord = r;
        Object obj = target;
        if (getAwareJobSchedulerService() == null) {
            return false;
        }
        if (!isSystemApplication(obj)) {
            AwareLog.w(TAG, "iaware_brjob not system app");
            return false;
        }
        IIntentReceiver resultTo = broadcastRecord.resultTo;
        if (broadcastRecord.resultTo != null) {
            AwareLog.d(TAG, "reset resultTo null");
            resultTo = null;
        }
        List<Object> receiver = new ArrayList<>();
        receiver.add(obj);
        List<Object> receiver2 = receiver;
        BroadcastRecord jobCtrlBr = new BroadcastRecord(broadcastRecord.queue, broadcastRecord.intent, broadcastRecord.callerApp, broadcastRecord.callerPackage, broadcastRecord.callingPid, broadcastRecord.callingUid, broadcastRecord.callerInstantApp, broadcastRecord.resolvedType, broadcastRecord.requiredPermissions, broadcastRecord.appOp, broadcastRecord.options, receiver2, resultTo, broadcastRecord.resultCode, broadcastRecord.resultData, broadcastRecord.resultExtras, broadcastRecord.ordered, broadcastRecord.sticky, broadcastRecord.initialSticky, broadcastRecord.userId);
        return this.mAwareJobSchedulerService.schedule(new HwBroadcastRecord(jobCtrlBr));
    }

    private boolean enqueueIawareProxyBroacast(boolean isParallel, BroadcastRecord r, Object target) {
        BroadcastRecord broadcastRecord = r;
        Object obj = target;
        if (isAbnormalParameters(isParallel, r, target) || getIawareBrPolicy() == null || broadcastRecord.iawareCtrlType == 1 || !this.mIawareBrPolicy.isProxyedAllowedCondition() || broadcastRecord.callingPid != this.mSysServicePid || isThirdOrKeyBroadcast()) {
            return false;
        }
        String pkg = getPkg(obj);
        int pid = getPid(obj);
        int uid = getUid(obj);
        if (isAbnormalValue(pkg, pid, uid) || pid == this.mSysServicePid) {
            return false;
        }
        String action = null;
        if (broadcastRecord.intent != null) {
            action = broadcastRecord.intent.getAction();
        }
        String action2 = action;
        if (this.mIawareBrPolicy.isNotProxySysPkg(pkg, action2)) {
            String str = pkg;
            int i = pid;
            int i2 = uid;
            String str2 = action2;
            int pid2 = isParallel;
        } else if (isInstrumentationApp(obj)) {
            String str3 = pkg;
            int i3 = pid;
            int i4 = uid;
            String str4 = action2;
            int pid3 = isParallel;
        } else {
            boolean isSystemApp = isSystemApplication(obj);
            int curProcState = getcurProcState(obj);
            int curAdj = getProcessCurrentAdj(obj);
            if (-1 == curProcState) {
                int i5 = curAdj;
                int i6 = curProcState;
                int i7 = pid;
                int i8 = uid;
                String str5 = action2;
                boolean z = isSystemApp;
                int pid4 = isParallel;
            } else if (-10000 == curAdj) {
                String str6 = pkg;
                int i9 = curAdj;
                int i10 = curProcState;
                int i11 = pid;
                int i12 = uid;
                String str7 = action2;
                boolean z2 = isSystemApp;
                int pid5 = isParallel;
            } else {
                int curAdj2 = curAdj;
                int curProcState2 = curProcState;
                if (!this.mIawareBrPolicy.shouldIawareProxyBroadcast(action2, broadcastRecord.callingPid, uid, pid, pkg)) {
                    return false;
                }
                List<Object> receiver = new ArrayList<>();
                receiver.add(obj);
                String str8 = action2;
                boolean isSystemApp2 = isSystemApp;
                String pkg2 = pkg;
                int pid6 = pid;
                BroadcastRecord broadcastRecord2 = new BroadcastRecord(broadcastRecord.queue, broadcastRecord.intent, broadcastRecord.callerApp, broadcastRecord.callerPackage, broadcastRecord.callingPid, broadcastRecord.callingUid, broadcastRecord.callerInstantApp, broadcastRecord.resolvedType, broadcastRecord.requiredPermissions, broadcastRecord.appOp, broadcastRecord.options, receiver, broadcastRecord.resultTo, broadcastRecord.resultCode, broadcastRecord.resultData, broadcastRecord.resultExtras, broadcastRecord.ordered, broadcastRecord.sticky, broadcastRecord.initialSticky, broadcastRecord.userId);
                BroadcastRecord proxyBR = broadcastRecord2;
                proxyBR.dispatchClockTime = broadcastRecord.dispatchClockTime;
                proxyBR.dispatchTime = broadcastRecord.dispatchTime;
                proxyBR.iawareCtrlType = 1;
                HwBroadcastRecord hwBr = new HwBroadcastRecord(proxyBR);
                hwBr.setReceiverUid(uid);
                hwBr.setReceiverPid(pid6);
                hwBr.setReceiverCurAdj(curAdj2);
                hwBr.setReceiverPkg(pkg2);
                hwBr.setSysApp(isSystemApp2);
                hwBr.setReceiverCurProcState(curProcState2);
                return this.mIawareBrPolicy.enqueueIawareProxyBroacast(isParallel, hwBr);
            }
            return false;
        }
        return false;
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
            BroadcastQueue queue = parallelList.get(0).getBroacastQueue();
            synchronized (queue.mService) {
                int listSize = parallelList.size();
                for (int i = 0; i < listSize; i++) {
                    queue.mParallelBroadcasts.add(i, parallelList.get(i).getBroadcastRecord());
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
        boolean systemApp = false;
        if (target instanceof BroadcastFilter) {
            BroadcastFilter filter = (BroadcastFilter) target;
            if (filter.receiverList != null && filter.receiverList.app != null) {
                int flags = filter.receiverList.app.info.flags;
                int privateFlags = filter.receiverList.app.info.privateFlags;
                if (!((flags & 1) == 0 && (flags & 128) == 0 && (privateFlags & 8) == 0)) {
                    systemApp = true;
                }
                return systemApp;
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
            if (info.activityInfo != null && info.activityInfo.applicationInfo != null) {
                if (info.activityInfo.applicationInfo.isSystemApp() || info.activityInfo.applicationInfo.isPrivilegedApp() || info.activityInfo.applicationInfo.isUpdatedSystemApp()) {
                    systemApp = true;
                }
                return systemApp;
            } else if (!AwareBroadcastDebug.getDebugDetail()) {
                return false;
            } else {
                AwareLog.w(TAG, "isSystemApplication ResolveInfo: info, info.activityInfo, info.activityInfo.applicationInfo is null ");
                return false;
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
        return !MemoryConstant.MEM_REPAIR_CONSTANT_BG.equals(this.mQueue.mQueueName) && !MemoryConstant.MEM_REPAIR_CONSTANT_FG.equals(this.mQueue.mQueueName);
    }

    private AwareBroadcastDumpRadar getBrDumpRadar() {
        if (this.mBrDumpRadar == null && MultiTaskManagerService.self() != null) {
            this.mBrDumpRadar = MultiTaskManagerService.self().getIawareBrRadar();
        }
        return this.mBrDumpRadar;
    }

    private void trackBrFlow(boolean enqueue, boolean isParallel, BroadcastRecord r, Object target) {
        if (getBrDumpRadar() != null && r != null && isParallel && target != null && (target instanceof BroadcastFilter)) {
            boolean isProxyed = r.iawareCtrlType == 1;
            AwareBroadcastPolicy policy = getIawareBrPolicy();
            if (policy != null) {
                this.mBrDumpRadar.trackBrFlowSpeed(enqueue, isProxyed, !policy.isSpeedNoCtrol(), true ^ policy.isScreenOff());
            }
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

    private int getProcessCurrentAdj(Object target) {
        if (!(target instanceof BroadcastFilter)) {
            return -10000;
        }
        BroadcastFilter filter = (BroadcastFilter) target;
        if (filter.receiverList != null && filter.receiverList.app != null) {
            return filter.receiverList.app.curAdj;
        }
        if (!AwareBroadcastDebug.getDebugDetail()) {
            return -10000;
        }
        AwareLog.d(TAG, "getProcessCurrentAdj BroadcastFilter: filter something is null");
        return -10000;
    }

    private int getcurProcState(Object target) {
        if (!(target instanceof BroadcastFilter)) {
            return -1;
        }
        BroadcastFilter filter = (BroadcastFilter) target;
        if (filter.receiverList != null && filter.receiverList.app != null) {
            return filter.receiverList.app.curProcState;
        }
        if (!AwareBroadcastDebug.getDebugDetail()) {
            return -1;
        }
        AwareLog.d(TAG, "getProcessCurrentAdj BroadcastFilter: filter something is null");
        return -1;
    }

    private boolean isAbnormalValue(String pkg, int pid, int uid) {
        if (pkg == null || pid == -1 || uid == -1) {
            return true;
        }
        return false;
    }

    private int filterRegisteredReceiver(Intent intent, BroadcastFilter filter, AwareProcessInfo pInfo) {
        if (filter.countActionFilters() > 0 && ((pInfo.mProcInfo.mType == 2 || pInfo.mPid == this.mSysServicePid) && this.mIawareBrPolicy.assemFilterBr(intent, filter))) {
            return AwareBroadcastPolicy.BrCtrlType.DISCARDBR.ordinal();
        }
        if (pInfo.mPid == this.mSysServicePid || pInfo.mProcInfo.mCurAdj < 0) {
            return AwareBroadcastPolicy.BrCtrlType.NONE.ordinal();
        }
        int configListPolicy = getPolicyFromConfigList(intent, filter.packageName, pInfo);
        if (configListPolicy != -1) {
            return configListPolicy;
        }
        int policy = this.mIawareBrPolicy.filterBr(intent, pInfo);
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter : reg policy:" + policy + ", pkgname:" + filter.packageName + ", action:" + intent.getAction() + ", proc state:" + pInfo.getState() + ", proc type:" + pInfo.mProcInfo.mType);
        }
        return policy;
    }

    private int filterResolveInfo(Intent intent, ResolveInfo resolveInfo, AwareProcessInfo pInfo) {
        int configListPolicy = getPolicyFromConfigList(intent, resolveInfo.activityInfo.applicationInfo.packageName, pInfo);
        if (configListPolicy != -1) {
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "iaware_brFilter resolve config policy: " + configListPolicy + ", pkgname:" + resolveInfo.activityInfo.applicationInfo.packageName + ", action:" + intent.getAction());
            }
            return configListPolicy;
        }
        int policy = this.mIawareBrPolicy.filterBr(intent, pInfo);
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter : resolve policy:" + policy + ", pkgname:" + resolveInfo.activityInfo.applicationInfo.packageName + ", action:" + intent.getAction() + ", proc state:" + pInfo.getState() + ", proc type:" + pInfo.mProcInfo.mType);
        }
        return policy;
    }

    public void iawareFilterBroadcast(Intent intent, ProcessRecord callerApp, String callerPackage, int callingPid, int callingUid, boolean callerInstantApp, String resolvedType, String[] requiredPermissions, int appOp, BroadcastOptions options, List receivers, List<BroadcastFilter> registeredReceivers, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, boolean initialSticky, int userId) {
        if (isCondtionSatisfy(intent)) {
            Intent intent2 = intent;
            this.mIawareBrPolicy.getStateFromSendBr(intent2);
            IIntentReceiver proxyResultTo = resultTo;
            if (ordered && resultTo != null) {
                if (AwareBroadcastDebug.getFilterDebug()) {
                    AwareLog.i(TAG, "reset resultTo null");
                }
                proxyResultTo = null;
            }
            IIntentReceiver proxyResultTo2 = proxyResultTo;
            Intent intent3 = intent2;
            ProcessRecord processRecord = callerApp;
            String str = callerPackage;
            int i = callingPid;
            int i2 = callingUid;
            boolean z = callerInstantApp;
            String str2 = resolvedType;
            String[] strArr = requiredPermissions;
            int i3 = appOp;
            BroadcastOptions broadcastOptions = options;
            List list = receivers;
            List<BroadcastFilter> list2 = registeredReceivers;
            IIntentReceiver iIntentReceiver = proxyResultTo2;
            int i4 = resultCode;
            String str3 = resultData;
            Bundle bundle = resultExtras;
            boolean z2 = ordered;
            boolean z3 = sticky;
            boolean z4 = initialSticky;
            int i5 = userId;
            processRegisteredReceiver(intent3, processRecord, str, i, i2, z, str2, strArr, i3, broadcastOptions, list, list2, iIntentReceiver, i4, str3, bundle, z2, z3, z4, i5);
            processResolveReceiver(intent, processRecord, str, i, i2, z, str2, strArr, i3, broadcastOptions, list, list2, iIntentReceiver, i4, str3, bundle, z2, z3, z4, i5);
        }
    }

    private void processRegisteredReceiver(Intent intent, ProcessRecord callerApp, String callerPackage, int callingPid, int callingUid, boolean callerInstantApp, String resolvedType, String[] requiredPermissions, int appOp, BroadcastOptions options, List receivers, List<BroadcastFilter> registeredReceivers, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, boolean initialSticky, int userId) {
        if (registeredReceivers != null) {
            String action = intent.getAction();
            AwareBroadcastDumpRadar.increatBrBeforeCount(registeredReceivers.size());
            Iterator<BroadcastFilter> regIterator = registeredReceivers.iterator();
            while (true) {
                Iterator<BroadcastFilter> regIterator2 = regIterator;
                if (regIterator2.hasNext()) {
                    BroadcastFilter filter = regIterator2.next();
                    AwareProcessInfo pInfo = getProcessInfo(filter);
                    if (pInfo == null) {
                        AwareBroadcastDumpRadar.increatBrNoProcessCount(1);
                    } else {
                        Intent intent2 = intent;
                        int policy = filterRegisteredReceiver(intent2, filter, pInfo);
                        if (policy == AwareBroadcastPolicy.BrCtrlType.DISCARDBR.ordinal()) {
                            regIterator2.remove();
                            AwareBroadcastDumpRadar.increatBrAfterCount(1);
                            trackBrFilter(action, pInfo, policy, true, filter);
                        } else if (policy == AwareBroadcastPolicy.BrCtrlType.CACHEBR.ordinal()) {
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(filter);
                            int policy2 = policy;
                            BroadcastRecord cacheBr = new BroadcastRecord(this.mQueue, intent2, callerApp, callerPackage, callingPid, callingUid, callerInstantApp, resolvedType, requiredPermissions, appOp, options, arrayList, resultTo, resultCode, resultData, resultExtras, ordered, sticky, initialSticky, userId);
                            HwBroadcastRecord hwBr = new HwBroadcastRecord(cacheBr);
                            hwBr.setCurrentReceiverPid(pInfo.mPid);
                            hwBr.setReceiverPkg(filter.packageName);
                            boolean trim = this.mIawareBrPolicy.awareTrimAndEnqueueBr(!ordered, hwBr, false, pInfo.mPid, filter.packageName);
                            regIterator2.remove();
                            HwBroadcastRecord hwBroadcastRecord = hwBr;
                            int i = policy2;
                            ArrayList arrayList2 = arrayList;
                            trackBrFilter(action, pInfo, policy2, trim, filter);
                        } else {
                            int policy3 = policy;
                            if (policy3 == AwareBroadcastPolicy.BrCtrlType.NONE.ordinal()) {
                                trackBrFilter(action, pInfo, policy3, false, filter);
                            }
                        }
                    }
                    regIterator = regIterator2;
                } else {
                    return;
                }
            }
        }
    }

    private void processResolveReceiver(Intent intent, ProcessRecord callerApp, String callerPackage, int callingPid, int callingUid, boolean callerInstantApp, String resolvedType, String[] requiredPermissions, int appOp, BroadcastOptions options, List receivers, List<BroadcastFilter> list, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, boolean initialSticky, int userId) {
        if (receivers != null) {
            String action = intent.getAction();
            AwareBroadcastDumpRadar.increatBrBeforeCount(receivers.size());
            Iterator<ResolveInfo> iterator = receivers.iterator();
            while (true) {
                Iterator<ResolveInfo> iterator2 = iterator;
                if (iterator2.hasNext()) {
                    ResolveInfo resolveInfo = iterator2.next();
                    AwareProcessInfo pInfo = getProcessInfo(resolveInfo);
                    if (pInfo == null) {
                        AwareBroadcastDumpRadar.increatBrNoProcessCount(1);
                    } else {
                        Intent intent2 = intent;
                        int policy = filterResolveInfo(intent2, resolveInfo, pInfo);
                        if (policy == AwareBroadcastPolicy.BrCtrlType.DISCARDBR.ordinal()) {
                            iterator2.remove();
                            AwareBroadcastDumpRadar.increatBrAfterCount(1);
                            trackBrFilter(action, pInfo, policy, true, resolveInfo);
                        } else if (policy == AwareBroadcastPolicy.BrCtrlType.CACHEBR.ordinal()) {
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(resolveInfo);
                            int policy2 = policy;
                            BroadcastRecord cacheBr = new BroadcastRecord(this.mQueue, intent2, callerApp, callerPackage, callingPid, callingUid, callerInstantApp, resolvedType, requiredPermissions, appOp, options, arrayList, resultTo, resultCode, resultData, resultExtras, ordered, sticky, initialSticky, userId);
                            HwBroadcastRecord hwBr = new HwBroadcastRecord(cacheBr);
                            hwBr.setCurrentReceiverPid(pInfo.mPid);
                            hwBr.setReceiverPkg(resolveInfo.activityInfo.applicationInfo.packageName);
                            boolean trim = this.mIawareBrPolicy.awareTrimAndEnqueueBr(false, hwBr, false, pInfo.mPid, resolveInfo.activityInfo.applicationInfo.packageName);
                            iterator2.remove();
                            HwBroadcastRecord hwBroadcastRecord = hwBr;
                            int i = policy2;
                            ArrayList arrayList2 = arrayList;
                            trackBrFilter(action, pInfo, policy2, trim, resolveInfo);
                        } else {
                            int policy3 = policy;
                            if (policy3 == AwareBroadcastPolicy.BrCtrlType.NONE.ordinal()) {
                                trackBrFilter(action, pInfo, policy3, false, resolveInfo);
                            }
                        }
                    }
                    iterator = iterator2;
                } else {
                    return;
                }
            }
        }
    }

    private AwareProcessInfo getProcessInfo(Object target) {
        AwareProcessInfo awareProcessInfo = null;
        if (target instanceof BroadcastFilter) {
            int pid = getPid(target);
            if (pid == this.mSysServicePid) {
                return new AwareProcessInfo(pid, new ProcessInfo(pid, 1000));
            }
            if (isInstrumentationApp(target)) {
                return null;
            }
            awareProcessInfo = ProcessInfoCollector.getInstance().getAwareProcessInfo(pid);
            if (awareProcessInfo != null) {
                awareProcessInfo.mProcInfo.mCurAdj = getProcessCurrentAdj(target);
            }
        } else if (target instanceof ResolveInfo) {
            ResolveInfo info = (ResolveInfo) target;
            ProcessRecord app = this.mQueue.mService.getProcessRecordLocked(info.activityInfo.processName, info.activityInfo.applicationInfo.uid, false);
            if (app != null) {
                if (app.instr != null) {
                    return null;
                }
                awareProcessInfo = ProcessInfoCollector.getInstance().getAwareProcessInfo(app.pid);
                if (awareProcessInfo != null) {
                    awareProcessInfo.mProcInfo.mCurAdj = app.curAdj;
                }
            }
        }
        return awareProcessInfo;
    }

    private int getPolicyFromConfigList(Intent intent, String pkgName, AwareProcessInfo pInfo) {
        if (BroadcastExFeature.isBrFilterMPApp(intent.getAction(), pkgName)) {
            return AwareBroadcastPolicy.BrCtrlType.DISCARDBR.ordinal();
        }
        if (BroadcastExFeature.isBrFilterWhiteList(pkgName) || BroadcastExFeature.isBrFilterWhiteApp(intent.getAction(), pkgName)) {
            return AwareBroadcastPolicy.BrCtrlType.NONE.ordinal();
        }
        if (pInfo.getState() != 1 || !BroadcastExFeature.isBrFilterBlackApp(intent.getAction(), pkgName)) {
            return -1;
        }
        return AwareBroadcastPolicy.BrCtrlType.CACHEBR.ordinal();
    }

    private boolean isCondtionSatisfy(Intent intent) {
        if (intent == null || intent.getAction() == null || !BroadcastExFeature.isFeatureEnabled(1) || getIawareBrPolicy() == null) {
            return false;
        }
        if (intent.getPackage() == null && intent.getComponent() == null) {
            return true;
        }
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "it is explicitly : " + intent.getAction());
        }
        return false;
    }

    public static void unProxyCachedBr(ArrayList<HwBroadcastRecord> awareParallelBrs, ArrayList<HwBroadcastRecord> awareOrderedBrs) {
        BroadcastQueue queue = null;
        if (awareParallelBrs != null && awareParallelBrs.size() > 0) {
            queue = awareParallelBrs.get(0).getBroacastQueue();
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "unproxy " + queue.mQueueName + " Broadcast pkg Parallel Broadcasts (" + awareParallelBrs + ")");
            }
            int count = awareParallelBrs.size();
            for (int i = 0; i < count; i++) {
                queue.mParallelBroadcasts.add(i, awareParallelBrs.get(i).getBroadcastRecord());
            }
        }
        if (awareOrderedBrs != null && awareOrderedBrs.size() > 0) {
            if (queue == null) {
                queue = awareOrderedBrs.get(0).getBroacastQueue();
            }
            boolean pending = queue.mPendingBroadcastTimeoutMessage;
            if (pending) {
                movePendingBroadcastToProxyList(queue.mOrderedBroadcasts, awareOrderedBrs, awareOrderedBrs.get(0).getCurrentReceiverPid());
            }
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "unproxy " + queue.mQueueName + " pending:" + pending + " Broadcast pkg Orded Broadcasts (" + awareOrderedBrs + ")");
            }
            int count2 = awareOrderedBrs.size();
            for (int i2 = 0; i2 < count2; i2++) {
                if (pending) {
                    queue.mOrderedBroadcasts.add(i2 + 1, awareOrderedBrs.get(i2).getBroadcastRecord());
                } else {
                    queue.mOrderedBroadcasts.add(i2, awareOrderedBrs.get(i2).getBroadcastRecord());
                }
            }
        }
        if (queue != null) {
            queue.scheduleBroadcastsLocked();
        }
    }

    public static boolean isSameReceiver(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1 != o2) {
            if (!(o1 instanceof BroadcastFilter) || !(o2 instanceof BroadcastFilter)) {
                if (!(o1 instanceof ResolveInfo) || !(o2 instanceof ResolveInfo)) {
                    return false;
                }
                ResolveInfo info1 = (ResolveInfo) o1;
                ResolveInfo info2 = (ResolveInfo) o2;
                if (!(info1.activityInfo == info2.activityInfo && info1.providerInfo == info2.providerInfo && info1.serviceInfo == info2.serviceInfo)) {
                    return false;
                }
            } else if (((BroadcastFilter) o1).receiverList != ((BroadcastFilter) o2).receiverList) {
                return false;
            }
        }
        return true;
    }

    private static void movePendingBroadcastToProxyList(ArrayList<BroadcastRecord> orderedBroadcasts, ArrayList<HwBroadcastRecord> orderedProxyBroadcasts, int pid) {
        int i;
        List<Object> needMoveReceivers;
        List<Object> receivers;
        int numReceivers;
        int recIdx;
        int i2 = pid;
        if (orderedProxyBroadcasts.size() == 0 || orderedBroadcasts.size() == 0) {
            ArrayList<HwBroadcastRecord> arrayList = orderedProxyBroadcasts;
            return;
        }
        BroadcastRecord r = orderedBroadcasts.get(0);
        List<Object> needMoveReceivers2 = new ArrayList<>();
        List<Object> receivers2 = r.receivers;
        if (receivers2 == null) {
            List<Object> list = receivers2;
            List<Object> needMoveReceivers3 = orderedProxyBroadcasts;
        } else if (i2 <= 0) {
            ArrayList arrayList2 = needMoveReceivers2;
            List<Object> list2 = receivers2;
            List<Object> needMoveReceivers4 = orderedProxyBroadcasts;
        } else {
            int recIdx2 = r.nextReceiver;
            int numReceivers2 = receivers2.size();
            int i3 = recIdx2;
            while (i3 < numReceivers2) {
                Object target = receivers2.get(i3);
                int resolvePid = getResolvePid(r.queue, target);
                if (resolvePid <= 0 || resolvePid != i2) {
                    needMoveReceivers = needMoveReceivers2;
                    receivers = receivers2;
                    recIdx = recIdx2;
                    numReceivers = numReceivers2;
                    i = i3;
                    List<Object> needMoveReceivers5 = orderedProxyBroadcasts;
                } else {
                    needMoveReceivers2.add(target);
                    List<Object> receiver = new ArrayList<>();
                    receiver.add(target);
                    recIdx = recIdx2;
                    numReceivers = numReceivers2;
                    Object obj = target;
                    int i4 = resolvePid;
                    receivers = receivers2;
                    needMoveReceivers = needMoveReceivers2;
                    i = i3;
                    BroadcastRecord r1 = new BroadcastRecord(r.queue, r.intent, r.callerApp, r.callerPackage, r.callingPid, r.callingUid, r.callerInstantApp, r.resolvedType, r.requiredPermissions, r.appOp, r.options, receiver, null, r.resultCode, r.resultData, r.resultExtras, r.ordered, r.sticky, r.initialSticky, r.userId);
                    orderedProxyBroadcasts.add(new HwBroadcastRecord(r1));
                }
                i3 = i + 1;
                recIdx2 = recIdx;
                numReceivers2 = numReceivers;
                receivers2 = receivers;
                needMoveReceivers2 = needMoveReceivers;
                i2 = pid;
                ArrayList<BroadcastRecord> arrayList3 = orderedBroadcasts;
            }
            List<Object> needMoveReceivers6 = needMoveReceivers2;
            List<Object> receivers3 = receivers2;
            int i5 = recIdx2;
            int i6 = numReceivers2;
            List<Object> needMoveReceivers7 = orderedProxyBroadcasts;
            List<Object> needMoveReceivers8 = needMoveReceivers6;
            if (needMoveReceivers8.size() > 0) {
                receivers3.removeAll(needMoveReceivers8);
                if (AwareBroadcastDebug.getFilterDebug()) {
                    AwareLog.i(TAG, "unproxy, moving receivers in Ordered Broadcasts (" + r + ") to proxyList, Move receivers : " + needMoveReceivers8);
                }
            }
        }
    }

    private static int getResolvePid(BroadcastQueue queue, Object target) {
        if (target instanceof ResolveInfo) {
            ResolveInfo info = (ResolveInfo) target;
            ProcessRecord app = queue.mService.getProcessRecordLocked(info.activityInfo.processName, info.activityInfo.applicationInfo.uid, false);
            if (app != null) {
                return app.pid;
            }
        }
        return -1;
    }

    private void trackBrFilter(String action, AwareProcessInfo pInfo, int policy, boolean droped, Object receiver) {
        String processType;
        if (AwareBroadcastDumpRadar.isBetaUser() && getBrDumpRadar() != null) {
            if (!droped) {
                if (pInfo.mPid == this.mSysServicePid) {
                    AwareBroadcastDumpRadar.increatSsNoDropCount(1);
                } else if (pInfo.mProcInfo.mCurAdj < 0) {
                    AwareBroadcastDumpRadar.increatPerAppNoDropCount(1);
                }
            }
            if (pInfo.mPid == this.mSysServicePid) {
                pInfo.mProcInfo.mProcessName = "system_server";
            }
            String id = "unknow";
            if (receiver instanceof BroadcastFilter) {
                id = AwareBroadcastRegister.removeBRIdUncommonData(((BroadcastFilter) receiver).getIdentifier());
            }
            if (droped) {
                processType = "drop";
            } else {
                processType = "nodrop";
            }
            this.mBrDumpRadar.addBrFilterDetail(action + "," + pInfo.mProcInfo.mProcessName + "," + id + "," + pInfo.getState() + "," + policy + "," + processType);
        }
    }
}
