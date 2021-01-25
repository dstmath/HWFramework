package com.android.server.mtm.taskstatus;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.app.ProcessMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.ProcessRecordEx;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.appmng.AwareAppMngDfx;
import com.android.server.rms.iaware.appmng.AwareAppUseDataManager;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.android.server.rms.iaware.memory.utils.BigMemoryConstant;
import com.android.server.rms.memrepair.ProcStateStatisData;
import com.huawei.android.app.ActivityManagerExt;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.INotificationManagerEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.ServiceThreadExt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessCleaner {
    private static final int CLEAN_PID_ACTIVITY = 4;
    private static final int CLEAN_PID_NOTIFICATION = 2;
    private static final int CLEAN_UID_NOTIFICATION = 1;
    private static final Object LOCK = new Object();
    private static final int PROTECTED_APP_NUM_FROM_MDM = 10;
    private static final int SEND_MSG_DELAYED_TIME_MS = 200;
    private static final String TAG = "ProcessCleaner";
    private static ProcessCleaner sProcessCleaner = null;
    private ActivityManager mActivityManager;
    private CleanHandler mCleanHandler;
    private HandlerThread mCleanThread;
    protected AtomicBoolean mCleaning;
    private Handler mHandler;
    private final HwActivityManagerService mHwAms;
    private final ArrayList<String> mMdmProtectedList;
    private final ProcessMap<ProcessFastKillInfo> mProcCleanMap;
    private ProcessInfoCollector mProcInfoCollector;

    private ProcessCleaner(Context context) {
        this.mCleaning = new AtomicBoolean(false);
        this.mHwAms = HwActivityManagerService.self();
        this.mMdmProtectedList = new ArrayList<>();
        this.mProcCleanMap = new ProcessMap<>();
        this.mProcInfoCollector = null;
        this.mActivityManager = null;
        this.mCleanHandler = null;
        this.mCleanThread = null;
        this.mHandler = new Handler(Looper.getMainLooper()) {
            /* class com.android.server.mtm.taskstatus.ProcessCleaner.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i != 1) {
                    if (i == 2 && (msg.obj instanceof List)) {
                        ProcessCleaner.this.cleanNotificationWithPid((List) msg.obj, msg.arg1, msg.arg2);
                    }
                } else if (msg.obj instanceof List) {
                    ProcessCleaner.this.cleanPackageNotifications((List) msg.obj, msg.arg1);
                }
            }
        };
        this.mProcInfoCollector = ProcessInfoCollector.getInstance();
        if (this.mHwAms == null) {
            SlogEx.e(TAG, "init failed to get HwAMS handler");
        }
        this.mActivityManager = (ActivityManager) context.getSystemService(BigMemoryConstant.BIG_MEM_INFO_ITEM_TAG);
        this.mCleanThread = new ServiceThreadExt("iaware.clean", -2, false).getHandlerThread();
        this.mCleanThread.start();
        Looper loop = this.mCleanThread.getLooper();
        if (loop != null) {
            this.mCleanHandler = new CleanHandler(loop);
        }
    }

    public static ProcessCleaner getInstance(Context context) {
        ProcessCleaner processCleaner;
        synchronized (LOCK) {
            if (sProcessCleaner == null) {
                sProcessCleaner = new ProcessCleaner(context);
            }
            processCleaner = sProcessCleaner;
        }
        return processCleaner;
    }

    public static ProcessCleaner getInstance() {
        ProcessCleaner processCleaner;
        synchronized (LOCK) {
            processCleaner = sProcessCleaner;
        }
        return processCleaner;
    }

    public enum CleanType {
        NONE("do-nothing"),
        COMPACT("compact"),
        REMOVE_TASK("remove-task"),
        KILL_ALLOW_START("kill-allow-start"),
        KILL_FORBID_START("kill-forbid-start"),
        KILL_DELAY_START("kill-delay-start"),
        FORCE_STOP("force-stop"),
        FORCE_STOP_REMOVE_TASK("force-stop-remove-task"),
        FREEZE_NOMAL("freeze-nomal"),
        FREEZE_UP_DOWNLOAD("freeze-up-download"),
        IO_LIMIT("io-limit"),
        FORCE_STOP_ALARM("force-stop-alarm"),
        CPU_LIMIT("cpulimit");
        
        private String mDescription;

        private CleanType(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    /* access modifiers changed from: private */
    public final class CleanHandler extends Handler {
        private CleanHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            ProcessFastKillInfo procHold;
            ProcessRecordEx proc;
            if (msg.what == 4 && msg.obj != null && (msg.obj instanceof ProcessFastKillInfo) && (proc = (procHold = (ProcessFastKillInfo) msg.obj).mApp) != null && !proc.isProcessRecordNull()) {
                IBinder thread = (procHold.mAppThread == null || procHold.mAppThread.isApplicationThreadNull()) ? null : procHold.mAppThread.getBinder();
                boolean isNative = true;
                if (msg.arg2 != 1) {
                    isNative = false;
                }
                HwActivityManager.cleanProcessResourceFast(proc.getProcessName(), procHold.mPid, thread, procHold.mAllowRestart, isNative);
                ProcessCleaner.this.removeProcessFastKillLocked(proc.getProcessName(), procHold.mUid);
                AwareLog.d(ProcessCleaner.TAG, "fast kill clean proc: " + proc + ", pid: " + procHold.mPid);
            }
        }
    }

    public int uniformClean(AwareProcessBlockInfo procGroup, Bundle extras, String reason) {
        int killedCount = 0;
        if (procGroup == null || procGroup.procProcessList == null || procGroup.procProcessList.isEmpty()) {
            return 0;
        }
        switch (procGroup.procCleanType) {
            case KILL_ALLOW_START:
            case KILL_FORBID_START:
                for (AwareProcessInfo awareProc : procGroup.procProcessList) {
                    if (awareProc != null) {
                        if (killProcess(awareProc.procPid, procGroup.procCleanType == CleanType.KILL_ALLOW_START, reason)) {
                            killedCount++;
                        }
                    }
                }
                return killedCount;
            case REMOVE_TASK:
                int result = removeTask(procGroup);
                if (result > 0) {
                    return 0 + result;
                }
                return 0;
            case FORCE_STOP_REMOVE_TASK:
                if (forceStopAppsAsUser(procGroup.procProcessList.get(0), reason)) {
                    killedCount = 0 + procGroup.procProcessList.size();
                }
                removeTask(procGroup);
                return killedCount;
            case FORCE_STOP:
                if (forceStopAppsAsUser(procGroup.procProcessList.get(0), reason)) {
                    return 0 + procGroup.procProcessList.size();
                }
                return 0;
            case FORCE_STOP_ALARM:
                List<Integer> killedPid = killProcessesSameUidExt(procGroup, (AtomicBoolean) null, reason, new boolean[]{false, false, true});
                if (killedPid != null) {
                    return 0 + killedPid.size();
                }
                return 0;
            default:
                return 0;
        }
    }

    private Map<String, List<String>> getAlarmTags(int uid, List<String> packageList) {
        if (packageList == null || packageList.isEmpty()) {
            return null;
        }
        Map<String, List<String>> tags = new ArrayMap<>();
        boolean clearAll = true;
        for (String pkg : packageList) {
            List<String> list = AwareIntelligentRecg.getInstance().getAllInvalidAlarmTags(uid, pkg);
            if (list != null) {
                clearAll = false;
                tags.put(pkg, list);
            }
        }
        if (clearAll) {
            return null;
        }
        return tags;
    }

    public boolean killProcess(int pid, boolean restartService) {
        return killProcess(pid, restartService, "null");
    }

    public boolean killProcess(int pid, boolean restartService, String reason) {
        long start = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.isOpenInfoLog) {
            SlogEx.d(TAG, "process cleaner kill process: pid is " + pid + ", restart service :" + restartService);
        }
        ProcessInfo procInfo = this.mProcInfoCollector.getProcessInfo(pid);
        if (procInfo == null) {
            SlogEx.e(TAG, "process cleaner kill process: process info is null ");
            return false;
        } else if (this.mHwAms == null) {
            SlogEx.e(TAG, "process cleaner kill process: mHwAms is null ");
            return false;
        } else if (!HwActivityManager.killProcessRecordFromMTM(procInfo, restartService, reason)) {
            SlogEx.e(TAG, "process cleaner kill process: failed to kill ");
            return false;
        } else {
            this.mProcInfoCollector.recordKilledProcess(procInfo);
            long end = SystemClock.elapsedRealtime();
            if (!this.mProcInfoCollector.isOpenInfoLog) {
                return true;
            }
            SlogEx.d(TAG, "process cleaner kill process: pid is " + pid + ",last time: " + (end - start));
            return true;
        }
    }

    public int removeTask(AwareProcessBlockInfo procGroup) {
        if (procGroup == null) {
            return 0;
        }
        if (this.mHwAms == null) {
            AwareLog.e(TAG, "process cleaner kill process: mHwAms is null ");
            return 0;
        } else if (procGroup.procProcessList == null) {
            return 0;
        } else {
            HashSet<Integer> taskIdSet = new HashSet<>();
            boolean success = false;
            for (AwareProcessInfo awareProc : procGroup.procProcessList) {
                if (awareProc != null) {
                    taskIdSet.add(Integer.valueOf(awareProc.procTaskId));
                }
            }
            Iterator<Integer> it = taskIdSet.iterator();
            while (it.hasNext()) {
                Integer taskId = it.next();
                if (taskId.intValue() != -1) {
                    if (HwActivityTaskManager.removeTask(taskId.intValue(), (IBinder) null, (String) null, true, "iAware")) {
                        success = true;
                    } else {
                        AwareLog.e(TAG, "fail to removeTask: " + taskId);
                    }
                }
            }
            if (success) {
                return procGroup.procProcessList.size();
            }
            return 0;
        }
    }

    public List<Integer> killProcessesSameUidExt(AwareProcessBlockInfo procGroup, boolean quickKillAction, String reason, boolean needCheckAdj) {
        return killProcessesSameUidExt(procGroup, (AtomicBoolean) null, reason, new boolean[]{true, quickKillAction, needCheckAdj});
    }

    private String getReason(String reason, boolean resCleanAllow, String pkgName) {
        long lastUseTime = AwareAppUseDataManager.getInstance().getLastUseTime(pkgName);
        if (resCleanAllow) {
            return "iAwareF[" + reason + "] " + lastUseTime + "ms";
        }
        return "iAwareK[" + reason + "] " + lastUseTime + "ms";
    }

    private boolean isParamValidLogged(AwareProcessBlockInfo procGroup, AwareAppMngSort appMngSort, List<AwareProcessInfo> procInfoAllStopList, boolean[] params) {
        if (procGroup == null || params.length != 3 || procGroup.procUid == 0 || procInfoAllStopList == null) {
            return false;
        }
        if (this.mHwAms == null) {
            SlogEx.e(TAG, "[aware_mem] Why mHwAms is null!!");
            return false;
        } else if (checkPkgInProtectedListFromMdm(procGroup.procPackageName)) {
            SlogEx.d(TAG, "[aware_mem] " + procGroup.procPackageName + " protected by MDM");
            return false;
        } else if (!procGroup.procIsNativeForceStop && appMngSort != null && appMngSort.isProcessBlockPidChanged(procGroup)) {
            if (this.mProcInfoCollector.isOpenInfoLog) {
                SlogEx.d(TAG, "[aware_mem] new process has started in block, uid: " + procGroup.procUid);
            }
            return false;
        } else if (!this.mProcInfoCollector.isOpenInfoLog) {
            return true;
        } else {
            SlogEx.d(TAG, "[aware_mem] start process cleaner kill process start");
            return true;
        }
    }

    private ArraySet<Integer> getCantStopPids(AwareProcessBlockInfo procGroup, AwareAppMngSort appMngSort, List<AwareProcessInfo> procInfoAllStopList, boolean needCheckAdj) {
        ArraySet<Integer> pidCantStop = new ArraySet<>();
        if (needCheckAdj && !procGroup.procIsNativeForceStop) {
            Iterator<AwareProcessInfo> it = procInfoAllStopList.iterator();
            while (it.hasNext()) {
                AwareProcessInfo info = it.next();
                if ((appMngSort == null || info == null || info.procProcInfo == null || !appMngSort.isGroupBeHigher(info.procPid, info.procProcInfo.mUid, info.procProcInfo.mProcessName, info.procProcInfo.mPackageName, info.procMemGroup)) ? false : true) {
                    pidCantStop.add(Integer.valueOf(info.procPid));
                }
            }
        }
        return pidCantStop;
    }

    private void resetStoppedState(boolean[] status, AwareProcessBlockInfo procGroup, List<String> packageList, Map<String, List<String>> alarmTagMap, boolean needCheckAlarm) {
        boolean isAlarmFlag;
        boolean isCleanAllRes = status[0];
        boolean isCleanUidActivity = status[1];
        boolean hasPerceptAlarm = status[2];
        boolean resCleanAllow = procGroup.procResCleanAllow;
        int targetUid = procGroup.procUid;
        if (isCleanAllRes) {
            if (needCheckAlarm) {
                isAlarmFlag = this.mHwAms.isPkgHasAlarm(packageList, targetUid);
            } else {
                isAlarmFlag = false;
            }
            if (isAlarmFlag) {
                SlogEx.d(TAG, "[aware_mem] is alarm " + packageList);
                this.mHwAms.setPackageStoppedState(packageList, false, targetUid);
            } else {
                SlogEx.d(TAG, "[aware_mem] start process cleaner cleanPackageRes, clnAlarm:" + procGroup.procCleanAlarm + ", hasPerceptAlarm:" + hasPerceptAlarm + ", isNative:" + procGroup.procIsNativeForceStop + ", cleanResult: " + HwActivityManager.cleanPackageRes(packageList, alarmTagMap, targetUid, procGroup.procCleanAlarm, procGroup.procIsNativeForceStop, hasPerceptAlarm));
                if (!procGroup.procIsNativeForceStop && hasPerceptAlarm) {
                    this.mHwAms.setPackageStoppedState(packageList, false, targetUid);
                }
            }
        } else if (resCleanAllow) {
            SlogEx.d(TAG, "[aware_mem] start process cleaner reset PackageStoppedState");
            this.mHwAms.setPackageStoppedState(packageList, false, targetUid);
        }
        if (isCleanUidActivity) {
            SlogEx.d(TAG, "[aware_mem] clean uid activity:" + targetUid);
            this.mHwAms.cleanActivityByUid(packageList, targetUid);
        }
    }

    private void printKillInfo(AwareProcessInfo info, AwareProcessBlockInfo procGroup, boolean killResult) {
        if (info.procProcInfo != null) {
            String killHint = killResult ? "success " : "fail ";
            StringBuilder sb = new StringBuilder();
            sb.append("[aware_mem] process cleaner ");
            sb.append(killHint);
            sb.append("pid:");
            sb.append(info.procPid);
            sb.append(", uid:");
            sb.append(info.procProcInfo.mUid);
            sb.append(", ");
            sb.append(info.procProcInfo.mProcessName);
            sb.append(", ");
            sb.append(info.procProcInfo.mPackageName);
            sb.append(", mHasShownUi:");
            sb.append(info.procHasShownUi);
            sb.append(", ");
            sb.append(procGroup.procSubTypeStr);
            sb.append(", class:");
            sb.append(procGroup.procClassRate);
            sb.append(", ");
            sb.append(procGroup.procSubClassRate);
            sb.append(", ");
            sb.append(info.procClassRate);
            sb.append(", ");
            sb.append(info.procSubClassRate);
            sb.append(", adj:");
            sb.append(info.procProcInfo.mCurAdj);
            sb.append(killResult ? " is killed" : "");
            SlogEx.d(TAG, sb.toString());
        }
    }

    private boolean shouldCleanUidActivity(boolean resCleanAllow, AwareProcessInfo info, List<AwareProcessInfo> pidsWithNotification, List<String> packageList, int targetUid) {
        if (!resCleanAllow && info.procProcInfo != null && pidsWithNotification.contains(info) && !info.procRestartFlag) {
            Message msg = this.mHandler.obtainMessage(2);
            msg.obj = packageList;
            msg.arg1 = targetUid;
            msg.arg2 = info.procPid;
            this.mHandler.sendMessageDelayed(msg, 200);
            SlogEx.d(TAG, "[aware_mem] clean notification " + info.procProcInfo.mProcessName);
        }
        if (resCleanAllow || !info.procHasShownUi || this.mHwAms.numOfPidWithActivity(targetUid) != 0) {
            return false;
        }
        return true;
    }

    private boolean getKillResultLocked(AwareProcessInfo info, AwareProcessBlockInfo procGroup, String reason, boolean[] params, ArraySet<Integer> pidCantStop) {
        if (procGroup.procIsNativeForceStop) {
            killProcessSameUid(info.procPid, info.getRestartFlag(), reason, params, true);
            return true;
        } else if (!pidCantStop.contains(Integer.valueOf(info.procPid))) {
            return killProcessSameUid(info.procPid, info.getRestartFlag(), reason, params, false);
        } else {
            return false;
        }
    }

    private void notifyCleanResultWithHint(boolean isCleanAllRes, List<String> packageList, int targetUid, List<Integer> list) {
        if (isCleanAllRes) {
            Message msg = this.mHandler.obtainMessage(1);
            msg.obj = packageList;
            msg.arg1 = targetUid;
            this.mHandler.sendMessageDelayed(msg, 200);
        }
    }

    private void printKillList(List<Integer> killList) {
        if (this.mProcInfoCollector.isOpenInfoLog) {
            SlogEx.d(TAG, "[aware_mem] process cleaner kill pids: " + killList.toString());
        }
    }

    private void trackKillInfoForBeta(List<AwareProcessInfo> dfxDataList, boolean isCleanAllRes, boolean quickKillAction) {
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            AwareAppMngDfx.getInstance().trackeKillInfo(dfxDataList, isCleanAllRes, quickKillAction);
        }
    }

    private void setHwAmsPackageStoppedStateLocked(AwareProcessBlockInfo procGroup, List<String> packageList) {
        if (procGroup.procResCleanAllow) {
            SlogEx.d(TAG, "[aware_mem] start process cleaner setPackageStoppedState.");
            this.mHwAms.setPackageStoppedState(packageList, true, procGroup.procUid);
        }
    }

    public List<Integer> killProcessesSameUidExt(AwareProcessBlockInfo procGroup, AtomicBoolean interrupt, String reason, boolean[] params) {
        Map<String, List<String>> alarmTagMap;
        HwActivityManagerService hwActivityManagerService;
        Throwable th;
        int i;
        List<AwareProcessInfo> dfxDataList;
        List<Integer> killList;
        List<String> packageList;
        boolean isCleanAllRes;
        AwareProcessInfo info;
        boolean killResult;
        List<String> packageList2;
        AwareAppMngSort appMngSort;
        List<AwareProcessInfo> procInfoAllStopList;
        List<Integer> killList2;
        boolean isCleanUidActivity;
        AwareAppMngSort appMngSort2 = AwareAppMngSort.getInstance();
        List<AwareProcessInfo> procInfoAllStopList2 = procGroup == null ? null : procGroup.getProcessList();
        if (!isParamValidLogged(procGroup, appMngSort2, procInfoAllStopList2, params)) {
            return null;
        }
        String reason2 = getReason(reason, procGroup.procResCleanAllow, procGroup.procPackageName);
        List<String> packageList3 = getPackageList(procInfoAllStopList2);
        List<AwareProcessInfo> notifiedPid = getPidsWithNotification(procInfoAllStopList2);
        boolean isCleanAllRes2 = procGroup.procResCleanAllow;
        List<Integer> killList3 = new ArrayList<>();
        boolean needCheckAlarm = appMngSort2 != null ? appMngSort2.needCheckAlarm(procGroup) : true;
        if (procGroup.procIsNativeForceStop) {
            alarmTagMap = null;
        } else {
            alarmTagMap = getAlarmTags(procGroup.procUid, packageList3);
        }
        boolean hasPerceptAlarm = AwareIntelligentRecg.getInstance().hasPerceptAlarm(procGroup.procUid, packageList3);
        HwActivityManagerService hwActivityManagerService2 = this.mHwAms;
        synchronized (hwActivityManagerService2) {
            try {
                setHwAmsPackageStoppedStateLocked(procGroup, packageList3);
                ArraySet<Integer> pidCantStop = getCantStopPids(procGroup, appMngSort2, procInfoAllStopList2, params[2]);
                sortProcListWithStable(procInfoAllStopList2);
                Iterator<AwareProcessInfo> it = procInfoAllStopList2.iterator();
                boolean isCleanAllRes3 = isCleanAllRes2;
                List<AwareProcessInfo> dfxDataList2 = null;
                boolean isCleanUidActivity2 = false;
                while (true) {
                    try {
                        if (!it.hasNext()) {
                            i = 3;
                            dfxDataList = dfxDataList2;
                            hwActivityManagerService = hwActivityManagerService2;
                            killList = killList3;
                            packageList = packageList3;
                            isCleanAllRes = isCleanAllRes3;
                            break;
                        }
                        try {
                            info = it.next();
                            if (interrupt != null) {
                                try {
                                    if (interrupt.get()) {
                                        isCleanAllRes = false;
                                        i = 3;
                                        dfxDataList = dfxDataList2;
                                        hwActivityManagerService = hwActivityManagerService2;
                                        killList = killList3;
                                        packageList = packageList3;
                                        break;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    hwActivityManagerService = hwActivityManagerService2;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th3) {
                                            th = th3;
                                        }
                                    }
                                    throw th;
                                }
                            }
                            killResult = getKillResultLocked(info, procGroup, reason2, params, pidCantStop);
                            if (killResult) {
                                killList3.add(Integer.valueOf(info.procPid));
                                List<AwareProcessInfo> dfxDataList3 = dfxDataList2;
                                hwActivityManagerService = hwActivityManagerService2;
                                killList2 = killList3;
                                packageList2 = packageList3;
                                procInfoAllStopList = procInfoAllStopList2;
                                appMngSort = appMngSort2;
                                try {
                                    isCleanUidActivity = shouldCleanUidActivity(procGroup.procResCleanAllow, info, notifiedPid, packageList2, procGroup.procUid) ? true : isCleanUidActivity2;
                                } catch (Throwable th4) {
                                    th = th4;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                                try {
                                    if (AwareConstant.CURRENT_USER_TYPE == 3) {
                                        if (dfxDataList3 == null) {
                                            dfxDataList3 = new ArrayList<>();
                                        }
                                        dfxDataList3.add(info);
                                        isCleanUidActivity2 = isCleanUidActivity;
                                        dfxDataList2 = dfxDataList3;
                                    } else {
                                        isCleanUidActivity2 = isCleanUidActivity;
                                        dfxDataList2 = dfxDataList3;
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } else {
                                hwActivityManagerService = hwActivityManagerService2;
                                killList2 = killList3;
                                packageList2 = packageList3;
                                procInfoAllStopList = procInfoAllStopList2;
                                appMngSort = appMngSort2;
                                isCleanAllRes3 = false;
                                dfxDataList2 = dfxDataList2;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            hwActivityManagerService = hwActivityManagerService2;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                        try {
                            printKillInfo(info, procGroup, killResult);
                            packageList3 = packageList2;
                            killList3 = killList2;
                            hwActivityManagerService2 = hwActivityManagerService;
                            procInfoAllStopList2 = procInfoAllStopList;
                            appMngSort2 = appMngSort;
                        } catch (Throwable th7) {
                            th = th7;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        hwActivityManagerService = hwActivityManagerService2;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
                try {
                    boolean[] status = new boolean[i];
                    status[0] = isCleanAllRes;
                    status[1] = isCleanUidActivity2;
                    status[2] = hasPerceptAlarm;
                    try {
                        resetStoppedState(status, procGroup, packageList, alarmTagMap, needCheckAlarm);
                        notifyCleanResultWithHint(isCleanAllRes, packageList, procGroup.procUid, killList);
                        printKillList(killList);
                        trackKillInfoForBeta(dfxDataList, isCleanAllRes, params[1]);
                        if (killList.isEmpty()) {
                            return null;
                        }
                        return killList;
                    } catch (Throwable th9) {
                        th = th9;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } catch (Throwable th10) {
                    th = th10;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            } catch (Throwable th11) {
                th = th11;
                hwActivityManagerService = hwActivityManagerService2;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    public void beginKillFast() {
        this.mCleaning.set(true);
    }

    public void endKillFast() {
        this.mCleaning.set(false);
    }

    private boolean isFastKilledParamValid(AwareProcessBlockInfo procGroup, AwareAppMngSort appMngSort, List<AwareProcessInfo> procInfoAllStopList, boolean[] params) {
        if (procGroup == null || params.length != 3 || procGroup.procUid == 0 || procInfoAllStopList == null) {
            return false;
        }
        if (this.mHwAms == null) {
            AwareLog.e(TAG, "[aware_mem] Why mHwAms is null!");
            return false;
        } else if (checkPkgInProtectedListFromMdm(procGroup.procPackageName)) {
            AwareLog.d(TAG, "[aware_mem] " + procGroup.procPackageName + " protected by MDM");
            return false;
        } else if (procGroup.procIsNativeForceStop || appMngSort == null || !appMngSort.isProcessBlockPidChanged(procGroup)) {
            return true;
        } else {
            AwareLog.d(TAG, "[aware_mem] new process has started in block, uid: " + procGroup.procUid);
            return false;
        }
    }

    private boolean getFastKillResult(AwareProcessInfo info, AwareProcessBlockInfo procGroup, String reason, boolean[] params, ArraySet<Integer> pidCantStop) {
        if (procGroup.procIsNativeForceStop) {
            return killProcessFast(info.procPid, info.getRestartFlag(), reason, params, true);
        }
        if (!pidCantStop.contains(Integer.valueOf(info.procPid))) {
            return killProcessFast(info.procPid, info.getRestartFlag(), reason, params, false);
        }
        return false;
    }

    private void cleanNotification(boolean isCleanAllRes, List<String> packageList, AwareProcessBlockInfo procGroup) {
        int targetUid = procGroup.procUid;
        if (isCleanAllRes) {
            Message msg = this.mHandler.obtainMessage(1);
            msg.obj = packageList;
            msg.arg1 = targetUid;
            this.mHandler.sendMessageDelayed(msg, 200);
            AwareLog.d(TAG, "[aware_mem] clean uid notification:" + targetUid + ", pkg: " + procGroup.procPackageName);
        }
    }

    private void printFastKillInfo(AwareProcessInfo info, AwareProcessBlockInfo procGroup, boolean killResult) {
        if (info.procProcInfo != null) {
            String killHint = killResult ? "success " : "fail ";
            StringBuilder sb = new StringBuilder();
            sb.append("[aware_mem] fast kill ");
            sb.append(killHint);
            sb.append("pid:");
            sb.append(info.procPid);
            sb.append(",uid:");
            sb.append(info.procProcInfo.mUid);
            sb.append(",");
            sb.append(info.procProcInfo.mProcessName);
            sb.append(",");
            sb.append(info.procProcInfo.mPackageName);
            sb.append(",mHasShownUi:");
            sb.append(info.procHasShownUi);
            sb.append(",");
            sb.append(procGroup.procSubTypeStr);
            sb.append(",class:");
            sb.append(procGroup.procClassRate);
            sb.append(",");
            sb.append(procGroup.procSubClassRate);
            sb.append(",");
            sb.append(info.procClassRate);
            sb.append(",");
            sb.append(info.procSubClassRate);
            sb.append(",adj:");
            sb.append(info.procProcInfo.mCurAdj);
            sb.append(killResult ? " is killed" : "");
            AwareLog.d(TAG, sb.toString());
        }
    }

    private void addProcessFastKillWithNotification(AwareProcessInfo info, AwareProcessBlockInfo procGroup, List<String> packageList, ProcessRecordEx app, List<AwareProcessInfo> notifiedPid) {
        ProcessFastKillInfo procHold = new ProcessFastKillInfo(app, app.getUid(), info.procPid, app.getThread(), info.getRestartFlag());
        addProcessFastKillLocked(procHold, app.getProcessName(), app.getUid());
        if (!procGroup.procResCleanAllow && info.procProcInfo != null && notifiedPid.contains(info) && !info.procRestartFlag) {
            Message msg = this.mHandler.obtainMessage(2);
            msg.obj = packageList;
            msg.arg1 = procGroup.procUid;
            msg.arg2 = info.procPid;
            this.mHandler.sendMessageDelayed(msg, 200);
            AwareLog.d(TAG, "[aware_mem] clean notification " + info.procProcInfo.mProcessName);
        }
        Message msg2 = this.mCleanHandler.obtainMessage(4);
        msg2.obj = procHold;
        msg2.arg1 = procGroup.procUid;
        if (!info.procHasShownUi || info.procStableValue > 1) {
            msg2.arg2 = 0;
            this.mCleanHandler.sendMessage(msg2);
            return;
        }
        msg2.arg2 = procGroup.procIsNativeForceStop ? 1 : 0;
        this.mCleanHandler.sendMessageAtFrontOfQueue(msg2);
    }

    /* JADX INFO: Multiple debug info for r11v6 'killResult'  boolean: [D('killResult' boolean), D('procInfoAllStopList' java.util.List<com.android.server.mtm.iaware.appmng.AwareProcessInfo>)] */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0113, code lost:
        r0 = r17;
     */
    public List<Integer> killProcessesSameUidFast(AwareProcessBlockInfo procGroup, AtomicBoolean interrupt, String reason, boolean[] params) {
        List<AwareProcessInfo> dfxDataList;
        boolean isCleanAllRes;
        List<AwareProcessInfo> procInfoAllStopList;
        boolean killResult;
        AwareAppMngSort appMngSort = AwareAppMngSort.getInstance();
        List<AwareProcessInfo> procInfoAllStopList2 = procGroup == null ? null : procGroup.getProcessList();
        if (!isFastKilledParamValid(procGroup, appMngSort, procInfoAllStopList2, params)) {
            return null;
        }
        List<Integer> killList = new ArrayList<>();
        List<String> packageList = getPackageList(procInfoAllStopList2);
        List<AwareProcessInfo> notifiedPid = getPidsWithNotification(procInfoAllStopList2);
        boolean isCleanAllRes2 = procGroup.procResCleanAllow;
        ArraySet<Integer> pidCantStop = getCantStopPids(procGroup, appMngSort, procInfoAllStopList2, params[2]);
        sortProcListWithStable(procInfoAllStopList2);
        Iterator<AwareProcessInfo> it = procInfoAllStopList2.iterator();
        List<AwareProcessInfo> dfxDataList2 = null;
        boolean isCleanAllRes3 = isCleanAllRes2;
        while (true) {
            if (!it.hasNext()) {
                dfxDataList = dfxDataList2;
                break;
            }
            AwareProcessInfo info = it.next();
            if (info != null) {
                if (interrupt != null && interrupt.get()) {
                    isCleanAllRes = false;
                    dfxDataList = dfxDataList2;
                    break;
                }
                ProcessRecordEx app = this.mHwAms.getProcessRecordLockedEx(info.procPid);
                if (app != null) {
                    if (!app.isProcessRecordNull()) {
                        boolean killResult2 = getFastKillResult(info, procGroup, reason, params, pidCantStop);
                        if (killResult2) {
                            killList.add(Integer.valueOf(info.procPid));
                            procInfoAllStopList = procInfoAllStopList2;
                            killResult = killResult2;
                            addProcessFastKillWithNotification(info, procGroup, packageList, app, notifiedPid);
                            if (AwareConstant.CURRENT_USER_TYPE == 3) {
                                if (dfxDataList2 == null) {
                                    dfxDataList2 = new ArrayList<>();
                                } else {
                                    dfxDataList2 = dfxDataList2;
                                }
                                dfxDataList2.add(info);
                            } else {
                                dfxDataList2 = dfxDataList2;
                            }
                        } else {
                            procInfoAllStopList = procInfoAllStopList2;
                            killResult = killResult2;
                            AwareLog.w(TAG, "not clean res, killResult:" + killResult + ", app:" + app + ", pid:" + info.procPid);
                            isCleanAllRes3 = false;
                            dfxDataList2 = dfxDataList2;
                        }
                        printFastKillInfo(info, procGroup, killResult);
                        appMngSort = appMngSort;
                        procInfoAllStopList2 = procInfoAllStopList;
                    }
                }
                dfxDataList2 = dfxDataList2;
                appMngSort = appMngSort;
                procInfoAllStopList2 = procInfoAllStopList2;
            } else {
                dfxDataList = dfxDataList2;
                break;
            }
        }
        cleanNotification(isCleanAllRes, packageList, procGroup);
        trackKillInfoForBeta(dfxDataList, isCleanAllRes, params[1]);
        if (killList.isEmpty()) {
            return null;
        }
        return killList;
    }

    public boolean forceStopAppsAsUser(AwareProcessInfo awareProc, String reason) {
        if (awareProc == null) {
            return false;
        }
        ProcessInfo awareProcInfo = awareProc.procProcInfo;
        if (awareProcInfo == null) {
            AwareLog.e(TAG, "forceStopAppsAsUser kill package: package info is null ");
            return false;
        }
        String pkgName = (String) awareProcInfo.mPackageName.get(0);
        if (pkgName == null || pkgName.trim().isEmpty()) {
            AwareLog.e(TAG, "forceStopAppsAsUser kill package: pkgName == null");
            return false;
        }
        int userId = UserHandleEx.getUserId(awareProcInfo.mUid);
        if (this.mHwAms != null) {
            long lastUseTime = AwareAppUseDataManager.getInstance().getLastUseTime(pkgName);
            HwActivityManagerService hwActivityManagerService = this.mHwAms;
            hwActivityManagerService.setLocalThreadReason("iAwareF[" + reason + "] " + lastUseTime + "ms");
            this.mHwAms.forceStopPackage(pkgName, userId);
            return true;
        }
        AwareLog.e(TAG, "forceStopAppsAsUser process: mActivityManager is null ");
        return false;
    }

    public boolean forceStopApps(int pid) {
        long start = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.isOpenInfoLog) {
            SlogEx.d(TAG, "forceStopApps kill process: pid is " + pid);
        }
        ProcessInfo procInfo = this.mProcInfoCollector.getProcessInfo(pid);
        if (procInfo == null) {
            SlogEx.e(TAG, "forceStopApps kill process: process info is null.");
            return false;
        } else if (procInfo.mCurSchedGroup != 0) {
            SlogEx.e(TAG, "forceStopApps kill process: process " + procInfo.mProcessName + " is not in BG");
            return false;
        } else {
            String pkgName = (String) procInfo.mPackageName.get(0);
            if (pkgName == null || pkgName.trim().isEmpty()) {
                SlogEx.e(TAG, "forceStopApps kill process: pkgName == null");
                return false;
            }
            ActivityManager activityManager = this.mActivityManager;
            if (activityManager != null) {
                ActivityManagerExt.forceStopPackage(activityManager, pkgName);
                this.mProcInfoCollector.recordKilledProcess(procInfo);
                long end = SystemClock.elapsedRealtime();
                if (!this.mProcInfoCollector.isOpenInfoLog) {
                    return true;
                }
                SlogEx.d(TAG, "forceStopApps kill process: pid is " + pid + ", last time: " + (end - start));
                return true;
            }
            SlogEx.e(TAG, "forceStopApps process: mActivityManager is null ");
            return false;
        }
    }

    private List<String> getPackageList(List<AwareProcessInfo> procInfoAllStopList) {
        List<String> packageList = new ArrayList<>();
        for (AwareProcessInfo curInfo : procInfoAllStopList) {
            if (!(curInfo.procProcInfo == null || curInfo.procProcInfo.mPackageName == null)) {
                Iterator it = curInfo.procProcInfo.mPackageName.iterator();
                while (it.hasNext()) {
                    String packageName = (String) it.next();
                    if (!packageList.contains(packageName)) {
                        packageList.add(packageName);
                    }
                }
            }
        }
        return packageList;
    }

    private List<AwareProcessInfo> getPidsWithNotification(List<AwareProcessInfo> procInfoAllStopList) {
        List<AwareProcessInfo> pidsWithNotification = new ArrayList<>();
        for (AwareProcessInfo info : procInfoAllStopList) {
            if (hasNotification(info.procPid)) {
                pidsWithNotification.add(info);
            }
        }
        return pidsWithNotification;
    }

    public boolean killProcessSameUid(int pid, boolean restartService, String reason, boolean[] params, boolean isNative) {
        long start = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.isOpenInfoLog) {
            SlogEx.d(TAG, "[aware_mem] process cleaner kill process: pid is " + pid + ", restart service: " + restartService);
        }
        ProcessInfo procInfo = this.mProcInfoCollector.getProcessInfo(pid);
        if (procInfo == null) {
            SlogEx.e(TAG, "[aware_mem] process cleaner kill process: process info is null ");
            return false;
        }
        boolean isAsynchronous = params[0];
        boolean needCheckAdj = params[2];
        if (isNative) {
            if (!HwActivityManager.killProcessRecordFromIAwareNative(procInfo, restartService, isAsynchronous, reason)) {
                return false;
            }
        } else if (!HwActivityManager.killProcessRecordFromIAware(procInfo, restartService, isAsynchronous, reason, needCheckAdj)) {
            return false;
        }
        this.mProcInfoCollector.recordKilledProcess(procInfo);
        long end = SystemClock.elapsedRealtime();
        if (!this.mProcInfoCollector.isOpenInfoLog) {
            return true;
        }
        SlogEx.d(TAG, "[aware_mem] process cleaner kill process: pid is " + pid + ",last time :" + (end - start));
        return true;
    }

    private boolean killProcessFast(int pid, boolean restartService, String reason, boolean[] params, boolean isNative) {
        ProcessInfo proc = this.mProcInfoCollector.getProcessInfo(pid);
        if (proc == null) {
            AwareLog.e(TAG, "[aware_mem] fast kill process: process info is null ");
            return false;
        }
        long lastUseTime = -1;
        if (proc.mPackageName != null && !proc.mPackageName.isEmpty()) {
            lastUseTime = AwareAppUseDataManager.getInstance().getLastUseTime((String) proc.mPackageName.get(0));
        }
        String newReason = reason + CpuCustBaseConfig.CPUCONFIG_INVALID_STR + lastUseTime;
        boolean needCheckAdj = params[2];
        boolean isAsync = params[0];
        if (isNative) {
            if (!HwActivityManager.killNativeProcessRecordFast(proc.mProcessName, proc.mPid, proc.mUid, restartService, isAsync, newReason)) {
                return false;
            }
        } else if (!HwActivityManager.killProcessRecordFast(proc.mProcessName, proc.mPid, proc.mUid, restartService, isAsync, newReason, needCheckAdj)) {
            return false;
        }
        this.mProcInfoCollector.recordKilledProcess(proc);
        AwareLog.d(TAG, "[aware_mem] fast kill proc: " + proc.mProcessName + ", pid: " + pid + ", restart: " + restartService);
        return true;
    }

    public void setProtectedListFromMdm(List<String> protectedList) {
        if (protectedList == null) {
            SlogEx.e(TAG, "[aware_mem] Set MDM protected list error");
            return;
        }
        ArrayList<String> tempList = new ArrayList<>();
        if (protectedList.size() < 10) {
            tempList.addAll(protectedList);
        } else {
            for (int i = 0; i < 10; i++) {
                tempList.add(protectedList.get(i));
            }
            SlogEx.d(TAG, "[aware_mem] Only 10 apps will be protected from MDM." + tempList.toString());
        }
        synchronized (this.mMdmProtectedList) {
            this.mMdmProtectedList.clear();
            this.mMdmProtectedList.addAll(tempList);
        }
    }

    public void removeProtectedListFromMdm() {
        synchronized (this.mMdmProtectedList) {
            this.mMdmProtectedList.clear();
        }
        SlogEx.d(TAG, "[aware_mem] Remove MDM protected list");
    }

    public boolean checkPkgInProtectedListFromMdm(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        synchronized (this.mMdmProtectedList) {
            if (this.mMdmProtectedList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanPackageNotifications(List<String> packageList, int targetUid) {
        if (packageList != null) {
            try {
                SlogEx.v(TAG, "cleanupPackageNotifications, userId=" + UserHandleEx.getUserId(targetUid) + ProcStateStatisData.SEPERATOR_CHAR + packageList);
                INotificationManagerEx.cancelAllNotifications(packageList, targetUid);
            } catch (RemoteException e) {
                SlogEx.e(TAG, "Unable to talk to notification manager. Woe!");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanNotificationWithPid(List<String> packageList, int targetUid, int pid) {
        if (packageList != null) {
            try {
                INotificationManagerEx.cleanNotificationWithPid(packageList, targetUid, pid);
            } catch (RemoteException e) {
                SlogEx.e(TAG, "Unable to talk to notification manager. Woe!");
            }
        }
    }

    private boolean hasNotification(int pid) {
        if (pid < 0) {
            return false;
        }
        return INotificationManagerEx.hasNotification(pid);
    }

    public boolean isProcessFastKillLocked(String procName, int uid) {
        boolean z;
        synchronized (this.mProcCleanMap) {
            z = ((ProcessFastKillInfo) this.mProcCleanMap.get(procName, uid)) != null;
        }
        return z;
    }

    private void addProcessFastKillLocked(ProcessFastKillInfo app, String procName, int uid) {
        if (app != null) {
            synchronized (this.mProcCleanMap) {
                this.mProcCleanMap.put(procName, uid, app);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeProcessFastKillLocked(String procName, int uid) {
        synchronized (this.mProcCleanMap) {
            this.mProcCleanMap.remove(procName, uid);
        }
    }

    private void sortProcListWithStable(List<AwareProcessInfo> procList) {
        AwareAppAssociate.getInstance();
        if (AwareAppAssociate.isEnabled() && procList.size() >= 2) {
            sortProcListWithStableEx(procList);
        }
    }

    private void sortProcListWithStableEx(List<AwareProcessInfo> procList) {
        int linkIdx;
        List<AwareProcessInfo> arrayList = new ArrayList<>();
        List<AwareProcessInfo> arrayList2 = new ArrayList<>();
        List<AwareProcessInfo> arrayList3 = new ArrayList<>();
        List<List<AwareProcessInfo>> objectList = new ArrayList<>(3);
        objectList.add(arrayList);
        objectList.add(arrayList2);
        objectList.add(arrayList3);
        Iterator<AwareProcessInfo> listIt = procList.iterator();
        while (listIt.hasNext()) {
            AwareProcessInfo procInfo = listIt.next();
            if (procInfo != null && (linkIdx = this.mHwAms.getStableProviderProcStatus(procInfo.procPid)) > 0 && linkIdx <= 3) {
                procInfo.procStableValue = linkIdx;
                objectList.get(linkIdx - 1).add(procInfo);
                listIt.remove();
            }
        }
        for (List<AwareProcessInfo> tempList : objectList) {
            if (tempList.size() > 0) {
                procList.addAll(tempList);
            }
        }
    }
}
