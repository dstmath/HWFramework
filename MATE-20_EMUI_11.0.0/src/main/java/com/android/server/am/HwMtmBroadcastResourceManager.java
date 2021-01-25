package com.android.server.am;

import android.app.BroadcastOptionsEx;
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
import com.android.server.rms.memrepair.MemRepairAlgorithm;
import com.huawei.android.content.IIntentReceiverEx;
import com.huawei.android.content.IntentFilterExt;
import com.huawei.android.content.pm.ApplicationInfoExt;
import com.huawei.android.content.pm.ResolveInfoEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HwMtmBroadcastResourceManager extends DefaultHwMtmBroadcastResourceManagerImpl {
    public static final int CACHED_APP_MIN_ADJ = 900;
    public static final int FOREGROUND_APP_ADJ = 0;
    private static final int INVALID_CONFIG_POLICY = -1;
    private static final String TAG = "HwMtmBroadcastResourceManager";
    public static final int TOP_APP = 2;
    private AwareBroadcastPolicy mAwareBrPolicy = null;
    private AwareBroadcastRegister mAwareBrRegister = null;
    private AwareBroadcastSend mAwareBrSend = null;
    private AwareJobSchedulerService mAwareJobSchedulerService = null;
    private AwareBroadcastDumpRadar mBrDumpRadar = null;
    private HashMap<ReceiverListEx, String> mBrIdMap = new HashMap<>();
    private final BroadcastQueueEx mQueue;
    private int mSysServicePid = Process.myPid();

    public HwMtmBroadcastResourceManager(BroadcastQueueEx queue) {
        this.mQueue = queue;
        AwareBroadcastPolicy.initBrCache(this.mQueue.getQueueName(), this.mQueue.getService());
    }

    public boolean iawareProcessBroadcast(int type, boolean isParallel, BroadcastRecordEx br, Object target) {
        if (type == 0) {
            boolean enqueue = enqueueAwareProxyBroadcast(isParallel, br, target);
            trackBrFlow(enqueue, isParallel, br, target);
            return enqueue;
        } else if (type == 1) {
            return processBroadcastScheduler(isParallel, br, target);
        } else {
            return false;
        }
    }

    public void iawareCheckCombinedConditon(IntentFilter filter) {
        Iterator<String> actions;
        if (filter == null) {
            AwareLog.e(TAG, "iawareCheckCombinedConditon param error!");
            return;
        }
        if (this.mAwareBrRegister == null) {
            this.mAwareBrRegister = AwareBroadcastRegister.getInstance();
        }
        String matchedId = this.mAwareBrRegister.findMatchedAssembleConditionId(IntentFilterExt.getIdentifier(filter));
        if (matchedId != null && (actions = filter.actionsIterator()) != null) {
            while (actions.hasNext()) {
                String condition = this.mAwareBrRegister.getBrAssembleCondition(matchedId, actions.next());
                if (condition != null) {
                    filter.addCategory(condition);
                    if (AwareBroadcastDebug.getDebugDetail()) {
                        AwareLog.i(TAG, "brreg: add condition: " + condition + " for " + IntentFilterExt.getIdentifier(filter));
                    }
                }
            }
        }
    }

    public void iawareCountDuplicatedReceiver(boolean isRegister, ReceiverListEx receivers, IntentFilter filter) {
        String brId;
        if (receivers == null || receivers.isReceiverListNull() || (isRegister && filter == null)) {
            AwareLog.e(TAG, "iawareCountDuplicatedReceiver param error!");
            return;
        }
        if (this.mAwareBrRegister == null) {
            this.mAwareBrRegister = AwareBroadcastRegister.getInstance();
        }
        if (isRegister) {
            brId = IntentFilterExt.getIdentifier(filter);
            this.mBrIdMap.put(receivers, brId);
        } else {
            brId = this.mBrIdMap.remove(receivers);
        }
        int brCount = this.mAwareBrRegister.countReceiver(isRegister, brId);
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
            if (this.mAwareBrSend == null) {
                this.mAwareBrSend = AwareBroadcastSend.getInstance();
            }
            if (this.mAwareBrSend.setData(action, data)) {
                return this.mAwareBrSend.needSkipBroadcastSend(action);
            }
            return false;
        }
    }

    private boolean processBroadcastScheduler(boolean isParallel, BroadcastRecordEx br, Object target) {
        String packageName;
        if (isParallel || br == null || br.isRecordNull() || br.getIntent() == null || target == null || !(target instanceof ResolveInfo)) {
            AwareLog.e(TAG, "iaware_brjob processBroadcastScheduler param error!");
            return false;
        }
        ResolveInfo info = (ResolveInfo) target;
        ComponentInfo ci = ResolveInfoEx.getComponentInfo(info);
        if (ci == null || ci.applicationInfo == null) {
            packageName = null;
        } else {
            packageName = ci.applicationInfo.packageName;
        }
        String action = br.getIntent().getAction();
        IntentFilter filter = info.filter;
        if (filter == null || IntentFilterExt.countActionFilters(filter) <= 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("iaware_brjob not process: ");
            sb.append(info);
            sb.append(", filter: ");
            Object obj = "null";
            sb.append(filter == null ? obj : filter);
            sb.append(", count: ");
            if (filter != null) {
                obj = Integer.valueOf(IntentFilterExt.countActionFilters(filter));
            }
            sb.append(obj);
            AwareLog.w(TAG, sb.toString());
            StartupByBroadcastRecorder.getInstance().recordStartupTimeByBroadcast(packageName, action, System.currentTimeMillis());
            trackImplicitBr(false, true, packageName, action);
            return false;
        }
        if (br.getIawareCtrlType() == 2) {
            StartupByBroadcastRecorder.getInstance().recordStartupTimeByBroadcast(packageName, action, System.currentTimeMillis());
            trackImplicitBr(true, true, packageName, action);
        } else if (br.queue == null || br.queue.isQueueNull() || !enqueueBroadcastScheduler(isParallel, br, target)) {
            trackImplicitBr(false, true, packageName, action);
        } else {
            trackImplicitBr(true, false, packageName, action);
            AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
            if (policy != null) {
                policy.updateBroadJobCtrlBigData(packageName);
            }
            br.queue.finishReceiverLocked(br, br.getResultCode(), br.getResultData(), br.getResultExtras(), br.getResultAbort(), false);
            br.queue.scheduleBroadcastsLocked();
            br.setState(BroadcastRecordEx.IDLE);
            return true;
        }
        return false;
    }

    private boolean enqueueBroadcastScheduler(boolean isParallel, BroadcastRecordEx br, Object target) {
        if (getAwareJobSchedulerService() == null) {
            return false;
        }
        if (!isSystemApplication(target)) {
            AwareLog.w(TAG, "iaware_brjob not system app");
            return false;
        }
        IIntentReceiverEx resultTo = br.getResultTo();
        if (!br.getResultTo().isReceiverNull()) {
            AwareLog.d(TAG, "reset resultTo null");
            resultTo.resetReceiver();
        }
        List<Object> receiver = new ArrayList<>();
        receiver.add(target);
        return this.mAwareJobSchedulerService.schedule(new HwBroadcastRecord(new BroadcastRecordEx(br.queue, br.getIntent(), br.getCallerApp(), br.getCallerPackage(), br.getCallingPid(), br.getCallingUid(), br.getCallerInstantApp(), br.getResolvedType(), br.getRequiredPermissions(), br.getAppOp(), br.options, receiver, resultTo, br.getResultCode(), br.getResultData(), br.getResultExtras(), br.getOrdered(), br.getSticky(), br.getInitialSticky(), br.getUserId(), br.getAllowBackgroundActivityStarts(), br.getTimeoutExempt())));
    }

    private boolean isCanEnqueue(boolean isParallel, BroadcastRecordEx br, BroadcastFilterEx filter) {
        if (!isParallel || br == null || br.isRecordNull() || filter == null || filter.isFilterNull() || getAwareBrPolicy() == null || br.getIawareCtrlType() == 1 || !this.mAwareBrPolicy.isProxyedAllowedCondition() || br.getCallingPid() != this.mSysServicePid || isThirdOrKeyBroadcast()) {
            return false;
        }
        return true;
    }

    private boolean enqueueAwareProxyBroadcast(boolean isParallel, BroadcastRecordEx br, Object target) {
        String action;
        BroadcastFilterEx filter = BroadcastFilterEx.getBroadcastFilterEx(target);
        if (!isCanEnqueue(isParallel, br, filter)) {
            return false;
        }
        String pkg = filter.getPackageName();
        int pid = getPid(filter);
        int uid = getUid(filter);
        if (isAbnormalValue(pkg, pid, uid) || pid == this.mSysServicePid) {
            return false;
        }
        if (br.getIntent() != null) {
            action = br.getIntent().getAction();
        } else {
            action = null;
        }
        if (this.mAwareBrPolicy.isNotProxySysPkg(pkg, action) || isInstrumentationApp(filter)) {
            return false;
        }
        int curProcState = getCurProcState(filter);
        int curAdj = getProcessCurrentAdj(filter);
        if (-1 != curProcState) {
            if (-10000 != curAdj) {
                if (!this.mAwareBrPolicy.shouldAwareProxyBroadcast(action, br.getCallingPid(), uid, pid, pkg)) {
                    return false;
                }
                List<Object> receiver = new ArrayList<>();
                receiver.add(target);
                BroadcastRecordEx proxyBr = new BroadcastRecordEx(br.queue, br.getIntent(), br.getCallerApp(), br.getCallerPackage(), br.getCallingPid(), br.getCallingUid(), br.getCallerInstantApp(), br.getResolvedType(), br.getRequiredPermissions(), br.getAppOp(), br.options, receiver, br.getResultTo(), br.getResultCode(), br.getResultData(), br.getResultExtras(), br.getOrdered(), br.getSticky(), br.getInitialSticky(), br.getUserId(), br.getAllowBackgroundActivityStarts(), br.getTimeoutExempt());
                proxyBr.setDispatchClockTime(br.getDispatchClockTime());
                proxyBr.setDispatchTime(br.getDispatchTime());
                proxyBr.setIawareCtrlType(1);
                HwBroadcastRecord hwBr = new HwBroadcastRecord(proxyBr);
                hwBr.setReceiverUid(uid);
                hwBr.setReceiverPid(pid);
                hwBr.setReceiverCurAdj(curAdj);
                hwBr.setReceiverPkg(pkg);
                hwBr.setSysApp(isSystemApplication(target));
                hwBr.setReceiverCurProcState(curProcState);
                return this.mAwareBrPolicy.enqueueAwareProxyBroacast(isParallel, hwBr);
            }
        }
        return false;
    }

    public void iawareStartCountBroadcastSpeed(boolean isParallel, BroadcastRecordEx br) {
        if (br != null && !br.isRecordNull() && getAwareBrPolicy() != null && isParallel && br.getIawareCtrlType() != 1) {
            String action = null;
            if (br.getIntent() != null) {
                action = br.getIntent().getAction();
            }
            if (action != null) {
                this.mAwareBrPolicy.iawareStartCountBroadcastSpeed(isParallel, br.getDispatchClockTime(), br.getReceivers().size());
            }
        }
    }

    public void iawareEndCountBroadcastSpeed(BroadcastRecordEx br) {
        if (br != null && !br.isRecordNull() && getAwareBrPolicy() != null && br.getIawareCtrlType() != 1) {
            scheduleTrackBrFlowData();
            this.mAwareBrPolicy.endCheckCount();
        }
    }

    private AwareBroadcastPolicy getAwareBrPolicy() {
        if (this.mAwareBrPolicy == null && MultiTaskManagerService.self() != null) {
            this.mAwareBrPolicy = MultiTaskManagerService.self().getAwareBrPolicy();
        }
        return this.mAwareBrPolicy;
    }

    private AwareJobSchedulerService getAwareJobSchedulerService() {
        if (this.mAwareJobSchedulerService == null && MultiTaskManagerService.self() != null) {
            this.mAwareJobSchedulerService = MultiTaskManagerService.self().getAwareJobSchedulerService();
        }
        return this.mAwareJobSchedulerService;
    }

    public static void insertAwareBroadcast(ArrayList<HwBroadcastRecord> parallelList, String name) {
        if (!(parallelList == null || parallelList.isEmpty())) {
            BroadcastQueueEx queue = parallelList.get(0).getBroacastQueue();
            synchronized (queue.getService()) {
                int listSize = parallelList.size();
                for (int i = 0; i < listSize; i++) {
                    queue.enqueueParallelBroadcasts(i, parallelList.get(i).getBroadcastRecord());
                }
                queue.scheduleBroadcastsLocked();
            }
        }
    }

    private int getPid(BroadcastFilterEx filter) {
        if (filter == null || filter.isFilterNull() || filter.getReceiverList().isReceiverListNull()) {
            return -1;
        }
        int pid = filter.getReceiverList().getPid();
        if (pid > 0 || filter.getReceiverList().getApp().isProcessRecordNull()) {
            return pid;
        }
        return filter.getReceiverList().getApp().getPid();
    }

    private int getUid(BroadcastFilterEx filter) {
        if (filter == null || filter.isFilterNull() || filter.getReceiverList().isReceiverListNull()) {
            return -1;
        }
        int uid = filter.getReceiverList().getUid();
        if (uid > 0 || filter.getReceiverList().getApp().isProcessRecordNull()) {
            return uid;
        }
        return filter.getReceiverList().getApp().getUid();
    }

    private boolean isSystemApplication(Object target) {
        BroadcastFilterEx filter = BroadcastFilterEx.getBroadcastFilterEx(target);
        boolean systemApp = false;
        if (filter == null || filter.isFilterNull()) {
            if (!(target instanceof ResolveInfo)) {
                return false;
            }
            ResolveInfo info = (ResolveInfo) target;
            if (info.activityInfo != null && info.activityInfo.applicationInfo != null) {
                if (ApplicationInfoExt.isSystemApp(info.activityInfo.applicationInfo) || ApplicationInfoExt.isPrivilegedApp(info.activityInfo.applicationInfo) || ApplicationInfoExt.isUpdatedSystemApp(info.activityInfo.applicationInfo)) {
                    systemApp = true;
                }
                return systemApp;
            } else if (!AwareBroadcastDebug.getDebugDetail()) {
                return false;
            } else {
                AwareLog.w(TAG, "isSystemApplication ResolveInfo: info, info.activityInfo, info.activityInfo.applicationInfo is null ");
                return false;
            }
        } else if (!filter.getReceiverList().isReceiverListNull() && !filter.getReceiverList().getApp().isProcessRecordNull()) {
            int flags = filter.getReceiverList().getApp().getInfo().flags;
            int privateFlags = ApplicationInfoExt.getPrivateFlags(filter.getReceiverList().getApp().getInfo());
            if (!((flags & 1) == 0 && (flags & MemRepairAlgorithm.DVALUE_RISE_EXCEED_TWOTHIRD) == 0 && (privateFlags & 8) == 0)) {
                systemApp = true;
            }
            return systemApp;
        } else if (!AwareBroadcastDebug.getDebugDetail()) {
            return false;
        } else {
            AwareLog.d(TAG, "isSystemApplication BroadcastFilter: filter something is null");
            return false;
        }
    }

    private boolean isInstrumentationApp(BroadcastFilterEx filter) {
        boolean instrumentationApp = false;
        if (filter == null || filter.isFilterNull() || filter.getReceiverList().isReceiverListNull() || filter.getReceiverList().getApp().isProcessRecordNull()) {
            return false;
        }
        if (!filter.getReceiverList().getApp().isInstrNull()) {
            instrumentationApp = true;
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "instrumentation app do not proxy!");
            }
        }
        return instrumentationApp;
    }

    private boolean isThirdOrKeyBroadcast() {
        return !MemoryConstant.MEM_REPAIR_CONSTANT_BG.equals(this.mQueue.getQueueName()) && !MemoryConstant.MEM_REPAIR_CONSTANT_FG.equals(this.mQueue.getQueueName());
    }

    private AwareBroadcastDumpRadar getBrDumpRadar() {
        if (this.mBrDumpRadar == null && MultiTaskManagerService.self() != null) {
            this.mBrDumpRadar = MultiTaskManagerService.self().getAwareBrRadar();
        }
        return this.mBrDumpRadar;
    }

    private void trackBrFlow(boolean enqueue, boolean isParallel, BroadcastRecordEx br, Object target) {
        if (getBrDumpRadar() != null && br != null && !br.isRecordNull() && isParallel && target != null && BroadcastFilterEx.getBroadcastFilterEx(target) != null) {
            boolean isProxyed = br.getIawareCtrlType() == 1;
            AwareBroadcastPolicy policy = getAwareBrPolicy();
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

    public static void insertAwareOrderedBroadcast(HwBroadcastRecord hwBr) {
        BroadcastRecordEx br;
        if (hwBr != null && (br = hwBr.getBroadcastRecord()) != null && !br.isRecordNull() && br.queue != null && !br.queue.isQueueNull()) {
            synchronized (br.queue.getService()) {
                br.setIawareCtrlType(2);
                br.queue.enqueueOrderedBroadcastLocked(br);
                br.queue.scheduleBroadcastsLocked();
            }
        }
    }

    private int getProcessCurrentAdj(BroadcastFilterEx filter) {
        if (filter != null && !filter.isFilterNull() && !filter.getReceiverList().isReceiverListNull() && !filter.getReceiverList().getApp().isProcessRecordNull()) {
            return filter.getReceiverList().getApp().getCurAdj();
        }
        if (!AwareBroadcastDebug.getDebugDetail()) {
            return -10000;
        }
        AwareLog.d(TAG, "getProcessCurrentAdj BroadcastFilter: filter something is null");
        return -10000;
    }

    private int getCurProcState(BroadcastFilterEx filter) {
        if (filter != null && !filter.isFilterNull() && !filter.getReceiverList().isReceiverListNull() && !filter.getReceiverList().getApp().isProcessRecordNull()) {
            return filter.getReceiverList().getApp().getCurProcState();
        }
        if (!AwareBroadcastDebug.getDebugDetail()) {
            return -1;
        }
        AwareLog.d(TAG, "getProcessCurrentAdj BroadcastFilter: filter something is null");
        return -1;
    }

    private boolean isAbnormalValue(String pkg, int pid, int uid) {
        return pkg == null || pid == -1 || uid == -1;
    }

    private int filterRegisteredReceiver(Intent intent, BroadcastFilterEx filter, AwareProcessInfo info) {
        if (filter.countActionFilters() > 0 && ((info.procProcInfo.mType == 2 || info.procPid == this.mSysServicePid) && this.mAwareBrPolicy.assemFilterBr(intent, filter.getFilter()))) {
            return AwareBroadcastPolicy.BrCtrlType.DISCARDBR.ordinal();
        }
        if (info.procPid == this.mSysServicePid || info.procProcInfo.mCurAdj < 0) {
            return AwareBroadcastPolicy.BrCtrlType.NONE.ordinal();
        }
        int configListPolicy = getPolicyFromConfigList(intent, filter.getPackageName(), info);
        if (configListPolicy != -1) {
            return configListPolicy;
        }
        int policy = this.mAwareBrPolicy.filterBr(intent, info);
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter : reg policy:" + policy + ", pkgname:" + filter.getPackageName() + ", action:" + intent.getAction() + ", proc state:" + info.getState() + ", proc type:" + info.procProcInfo.mType);
        }
        return policy;
    }

    private int filterResolveInfo(Intent intent, ResolveInfo resolveInfo, AwareProcessInfo info) {
        int configListPolicy = getPolicyFromConfigList(intent, resolveInfo.activityInfo.applicationInfo.packageName, info);
        if (configListPolicy != -1) {
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "iaware_brFilter resolve config policy: " + configListPolicy + ", pkgname:" + resolveInfo.activityInfo.applicationInfo.packageName + ", action:" + intent.getAction());
            }
            return configListPolicy;
        }
        int policy = this.mAwareBrPolicy.filterBr(intent, info);
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter : resolve policy:" + policy + ", pkgname:" + resolveInfo.activityInfo.applicationInfo.packageName + ", action:" + intent.getAction() + ", proc state:" + info.getState() + ", proc type:" + info.procProcInfo.mType);
        }
        return policy;
    }

    public void iawareFilterBroadcast(Intent intent, ProcessRecordEx callerApp, String callerPackage, int callingPid, int callingUid, boolean callerInstantApp, String resolvedType, String[] requiredPermissions, int appOp, BroadcastOptionsEx options, List receivers, List<BroadcastFilterEx> registeredReceivers, IIntentReceiverEx resultTo, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, boolean initialSticky, int userId, boolean allowBackgroundActivityStarts, boolean timeoutExempt) {
        if (isCondtionSatisfy(intent)) {
            this.mAwareBrPolicy.getStateFromSendBr(intent);
            if (ordered && !resultTo.isReceiverNull()) {
                if (AwareBroadcastDebug.getFilterDebug()) {
                    AwareLog.i(TAG, "reset resultTo null");
                }
                resultTo.resetReceiver();
            }
            processRegisteredReceiver(intent, callerApp, callerPackage, callingPid, callingUid, callerInstantApp, resolvedType, requiredPermissions, appOp, options, receivers, registeredReceivers, resultTo, resultCode, resultData, resultExtras, ordered, sticky, initialSticky, userId, allowBackgroundActivityStarts, timeoutExempt);
            processResolveReceiver(intent, callerApp, callerPackage, callingPid, callingUid, callerInstantApp, resolvedType, requiredPermissions, appOp, options, receivers, registeredReceivers, resultTo, resultCode, resultData, resultExtras, ordered, sticky, initialSticky, userId, allowBackgroundActivityStarts, timeoutExempt);
        }
    }

    private void processRegisteredReceiver(Intent intent, ProcessRecordEx callerApp, String callerPackage, int callingPid, int callingUid, boolean callerInstantApp, String resolvedType, String[] requiredPermissions, int appOp, BroadcastOptionsEx options, List receivers, List<BroadcastFilterEx> registeredReceivers, IIntentReceiverEx resultTo, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, boolean initialSticky, int userId, boolean allowBackgroundActivityStarts, boolean timeoutExempt) {
        if (registeredReceivers != null) {
            String action = intent.getAction();
            AwareBroadcastDumpRadar.increaseBrBeforeCount(registeredReceivers.size());
            Iterator<BroadcastFilterEx> regIterator = registeredReceivers.iterator();
            while (regIterator.hasNext()) {
                BroadcastFilterEx filter = regIterator.next();
                AwareProcessInfo info = getProcessInfo(filter);
                if (info == null) {
                    AwareBroadcastDumpRadar.increaseBrNoProcessCount(1);
                } else {
                    int policy = filterRegisteredReceiver(intent, filter, info);
                    if (policy == AwareBroadcastPolicy.BrCtrlType.DISCARDBR.ordinal()) {
                        regIterator.remove();
                        AwareBroadcastDumpRadar.increaseBrAfterCount(1);
                        trackBrFilter(action, info, policy, true, filter);
                    } else if (policy == AwareBroadcastPolicy.BrCtrlType.CACHEBR.ordinal()) {
                        List<Object> cacheReceiver = new ArrayList<>();
                        cacheReceiver.add(filter);
                        HwBroadcastRecord hwBr = new HwBroadcastRecord(new BroadcastRecordEx(this.mQueue, intent, callerApp, callerPackage, callingPid, callingUid, callerInstantApp, resolvedType, requiredPermissions, appOp, options, cacheReceiver, resultTo, resultCode, resultData, resultExtras, ordered, sticky, initialSticky, userId, allowBackgroundActivityStarts, timeoutExempt));
                        hwBr.setCurrentReceiverPid(info.procPid);
                        hwBr.setReceiverPkg(filter.getPackageName());
                        boolean trim = this.mAwareBrPolicy.awareTrimAndEnqueueBr(!ordered, hwBr, false, info.procPid, filter.getPackageName());
                        regIterator.remove();
                        trackBrFilter(action, info, policy, trim, filter);
                    } else if (policy == AwareBroadcastPolicy.BrCtrlType.NONE.ordinal()) {
                        trackBrFilter(action, info, policy, false, filter);
                    }
                }
            }
        }
    }

    private void processResolveReceiver(Intent intent, ProcessRecordEx callerApp, String callerPackage, int callingPid, int callingUid, boolean callerInstantApp, String resolvedType, String[] requiredPermissions, int appOp, BroadcastOptionsEx options, List receivers, List<BroadcastFilterEx> list, IIntentReceiverEx resultTo, int resultCode, String resultData, Bundle resultExtras, boolean ordered, boolean sticky, boolean initialSticky, int userId, boolean allowBackgroundActivityStarts, boolean timeoutExempt) {
        if (receivers != null) {
            String action = intent.getAction();
            AwareBroadcastDumpRadar.increaseBrBeforeCount(receivers.size());
            Iterator<ResolveInfo> iterator = receivers.iterator();
            while (iterator.hasNext()) {
                ResolveInfo resolveInfo = iterator.next();
                AwareProcessInfo info = getProcessInfo(resolveInfo);
                if (info == null) {
                    AwareBroadcastDumpRadar.increaseBrNoProcessCount(1);
                } else {
                    int policy = filterResolveInfo(intent, resolveInfo, info);
                    if (policy == AwareBroadcastPolicy.BrCtrlType.DISCARDBR.ordinal()) {
                        iterator.remove();
                        AwareBroadcastDumpRadar.increaseBrNoProcessCount(1);
                        trackBrFilter(action, info, policy, true, resolveInfo);
                    } else if (policy == AwareBroadcastPolicy.BrCtrlType.CACHEBR.ordinal()) {
                        List<Object> cacheReceiver = new ArrayList<>();
                        cacheReceiver.add(resolveInfo);
                        HwBroadcastRecord hwBr = new HwBroadcastRecord(new BroadcastRecordEx(this.mQueue, intent, callerApp, callerPackage, callingPid, callingUid, callerInstantApp, resolvedType, requiredPermissions, appOp, options, cacheReceiver, resultTo, resultCode, resultData, resultExtras, ordered, sticky, initialSticky, userId, allowBackgroundActivityStarts, timeoutExempt));
                        hwBr.setCurrentReceiverPid(info.procPid);
                        hwBr.setReceiverPkg(resolveInfo.activityInfo.applicationInfo.packageName);
                        boolean trim = this.mAwareBrPolicy.awareTrimAndEnqueueBr(false, hwBr, false, info.procPid, resolveInfo.activityInfo.applicationInfo.packageName);
                        iterator.remove();
                        trackBrFilter(action, info, policy, trim, resolveInfo);
                    } else if (policy == AwareBroadcastPolicy.BrCtrlType.NONE.ordinal()) {
                        trackBrFilter(action, info, policy, false, resolveInfo);
                    }
                }
            }
        }
    }

    private AwareProcessInfo getProcessInfo(Object target) {
        AwareProcessInfo awareProcessInfo = null;
        BroadcastFilterEx filter = BroadcastFilterEx.getBroadcastFilterEx(target);
        if (filter != null && !filter.isFilterNull()) {
            int pid = getPid(filter);
            if (pid == this.mSysServicePid) {
                return new AwareProcessInfo(pid, new ProcessInfo(pid, 1000));
            }
            if (isInstrumentationApp(filter)) {
                return null;
            }
            awareProcessInfo = ProcessInfoCollector.getInstance().getAwareProcessInfo(pid);
            if (awareProcessInfo != null) {
                awareProcessInfo.procProcInfo.mCurAdj = getProcessCurrentAdj(filter);
            }
        } else if (!(target instanceof ResolveInfo)) {
            return null;
        } else {
            ResolveInfo info = (ResolveInfo) target;
            ProcessRecordEx app = this.mQueue.getProcessRecordLocked(info.activityInfo.processName, info.activityInfo.applicationInfo.uid, false);
            if (app != null && !app.isProcessRecordNull()) {
                if (!app.isInstrNull()) {
                    return null;
                }
                awareProcessInfo = ProcessInfoCollector.getInstance().getAwareProcessInfo(app.getPid());
                if (awareProcessInfo != null) {
                    awareProcessInfo.procProcInfo.mCurAdj = app.getCurAdj();
                }
            }
        }
        return awareProcessInfo;
    }

    private int getPolicyFromConfigList(Intent intent, String pkgName, AwareProcessInfo info) {
        if (BroadcastExFeature.isBrFilterWhiteList(pkgName) || BroadcastExFeature.isBrFilterWhiteApp(intent.getAction(), pkgName)) {
            return AwareBroadcastPolicy.BrCtrlType.NONE.ordinal();
        }
        if (info.getState() != 1 || !BroadcastExFeature.isBrFilterBlackApp(intent.getAction(), pkgName)) {
            return -1;
        }
        return AwareBroadcastPolicy.BrCtrlType.CACHEBR.ordinal();
    }

    private boolean isCondtionSatisfy(Intent intent) {
        if (intent == null || intent.getAction() == null || !BroadcastExFeature.isFeatureEnabled(1) || getAwareBrPolicy() == null) {
            return false;
        }
        if (intent.getPackage() == null && intent.getComponent() == null) {
            return true;
        }
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "it is explicitly: " + intent.getAction());
        }
        return false;
    }

    public static void unProxyCachedBr(ArrayList<HwBroadcastRecord> awareParallelBrs, ArrayList<HwBroadcastRecord> awareOrderedBrs) {
        BroadcastQueueEx queue = null;
        if (awareParallelBrs != null && !awareParallelBrs.isEmpty()) {
            queue = awareParallelBrs.get(0).getBroacastQueue();
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "unproxy " + queue.getQueueName() + " Broadcast pkg Parallel Broadcasts (" + awareParallelBrs + ")");
            }
            int count = awareParallelBrs.size();
            for (int i = 0; i < count; i++) {
                queue.enqueueParallelBroadcasts(i, awareParallelBrs.get(i).getBroadcastRecord());
            }
        }
        if (awareOrderedBrs != null && !awareOrderedBrs.isEmpty()) {
            if (queue == null || queue.isQueueNull()) {
                queue = awareOrderedBrs.get(0).getBroacastQueue();
            }
            boolean pending = queue.getPendingBroadcastTimeoutMessage();
            if (pending) {
                movePendingBroadcastToProxyList(queue.getDispatcher().getActiveBroadcastLocked(), awareOrderedBrs, awareOrderedBrs.get(0).getCurrentReceiverPid());
            }
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "unproxy " + queue.getQueueName() + " pending:" + pending + " Broadcast pkg Orded Broadcasts (" + awareOrderedBrs + ")");
            }
            int orderedBroadcastsSize = queue.getDispatcher().getOrderedBroadcastsSize();
            int count2 = awareOrderedBrs.size();
            for (int i2 = 0; i2 < count2; i2++) {
                if (!pending || orderedBroadcastsSize <= 0) {
                    queue.getDispatcher().enqueueOrderedBroadcastLocked(i2, awareOrderedBrs.get(i2).getBroadcastRecord());
                } else {
                    queue.getDispatcher().enqueueOrderedBroadcastLocked(i2 + 1, awareOrderedBrs.get(i2).getBroadcastRecord());
                }
            }
        }
        if (!(queue == null || queue.isQueueNull())) {
            queue.scheduleBroadcastsLocked();
        }
    }

    public static boolean isSameReceiver(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1 == o2) {
            return true;
        }
        BroadcastFilterEx filter1 = BroadcastFilterEx.getBroadcastFilterEx(o1);
        BroadcastFilterEx filter2 = BroadcastFilterEx.getBroadcastFilterEx(o2);
        if (filter1 == null || filter1.isFilterNull() || filter2 == null || filter2.isFilterNull()) {
            if (!(o1 instanceof ResolveInfo) || !(o2 instanceof ResolveInfo)) {
                return false;
            }
            ResolveInfo info1 = (ResolveInfo) o1;
            ResolveInfo info2 = (ResolveInfo) o2;
            if (info1.activityInfo == info2.activityInfo && info1.providerInfo == info2.providerInfo && info1.serviceInfo == info2.serviceInfo) {
                return true;
            }
            return false;
        } else if (!BroadcastFilterEx.isSameReceivers(filter1, filter2)) {
            return false;
        } else {
            return true;
        }
    }

    private static void movePendingBroadcastToProxyList(BroadcastRecordEx br, ArrayList<HwBroadcastRecord> orderedProxyBroadcasts, int pid) {
        int i = pid;
        if (!orderedProxyBroadcasts.isEmpty() && br != null) {
            if (!br.isRecordNull()) {
                List<Object> needMoveReceivers = new ArrayList<>();
                List<Object> receivers = br.getReceivers();
                if (receivers == null) {
                    return;
                }
                if (i > 0) {
                    int recIndex = br.getNextReceiver();
                    int numReceivers = receivers.size();
                    int i2 = recIndex;
                    while (i2 < numReceivers) {
                        Object target = receivers.get(i2);
                        int resolvePid = getResolvePid(br.queue, target);
                        if (resolvePid > 0 && resolvePid == i) {
                            needMoveReceivers.add(target);
                            List<Object> receiver = new ArrayList<>();
                            receiver.add(target);
                            orderedProxyBroadcasts.add(new HwBroadcastRecord(new BroadcastRecordEx(br.queue, br.getIntent(), br.getCallerApp(), br.getCallerPackage(), br.getCallingPid(), br.getCallingUid(), br.getCallerInstantApp(), br.getResolvedType(), br.getRequiredPermissions(), br.getAppOp(), br.options, receiver, (IIntentReceiverEx) null, br.getResultCode(), br.getResultData(), br.getResultExtras(), br.getOrdered(), br.getSticky(), br.getInitialSticky(), br.getUserId(), br.getAllowBackgroundActivityStarts(), br.getTimeoutExempt())));
                        }
                        i2++;
                        i = pid;
                    }
                    if (!needMoveReceivers.isEmpty()) {
                        receivers.removeAll(needMoveReceivers);
                        if (AwareBroadcastDebug.getFilterDebug()) {
                            AwareLog.i(TAG, "unproxy, moving receivers in Ordered Broadcasts (" + br + ") to proxyList, Move receivers: " + needMoveReceivers);
                        }
                    }
                }
            }
        }
    }

    private static int getResolvePid(BroadcastQueueEx queue, Object target) {
        if (!(target instanceof ResolveInfo)) {
            return -1;
        }
        ResolveInfo info = (ResolveInfo) target;
        ProcessRecordEx app = queue.getProcessRecordLocked(info.activityInfo.processName, info.activityInfo.applicationInfo.uid, false);
        if (app == null || app.isProcessRecordNull()) {
            return -1;
        }
        return app.getPid();
    }

    private void trackBrFilter(String action, AwareProcessInfo info, int policy, boolean droped, Object receiver) {
        if (AwareBroadcastDumpRadar.isBetaUser() && getBrDumpRadar() != null) {
            if (!droped) {
                if (info.procPid == this.mSysServicePid) {
                    AwareBroadcastDumpRadar.increaseSsNoDropCount(1);
                } else if (info.procProcInfo.mCurAdj < 0) {
                    AwareBroadcastDumpRadar.increasePerAppNoDropCount(1);
                }
            }
            if (info.procPid == this.mSysServicePid) {
                info.procProcInfo.mProcessName = "system_server";
            }
            String id = "unknow";
            BroadcastFilterEx brFilter = BroadcastFilterEx.getBroadcastFilterEx(receiver);
            if (brFilter != null && !brFilter.isFilterNull()) {
                id = AwareBroadcastRegister.removeBrIdUncommonData(brFilter.getIdentifier());
            }
            String processType = droped ? "drop" : "nodrop";
            this.mBrDumpRadar.addBrFilterDetail(action + ", " + info.procProcInfo.mProcessName + ", " + id + ", " + info.getState() + ", " + policy + ", " + processType);
        }
    }
}
