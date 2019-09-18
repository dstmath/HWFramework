package com.android.server.mtm.taskstatus;

import android.app.ActivityManager;
import android.app.IApplicationThread;
import android.app.INotificationManager;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.app.ProcessMap;
import com.android.server.ServiceThread;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.ProcessRecord;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.huawei.android.app.HwActivityManager;
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
    private static final int PROTECTED_APP_NUM_FROM_MDM = 3;
    private static final String TAG = "ProcessCleaner";
    private static ProcessCleaner mProcessCleaner = null;
    private ActivityManager mActivityManager;
    private CleanHandler mCleanHandler;
    private HandlerThread mCleanThread;
    protected AtomicBoolean mCleaning;
    Handler mHandler;
    private HwActivityManagerService mHwAMS;
    private ArrayList<String> mMDMProtectedList;
    private final ProcessMap<ProcessFastKillInfo> mProcCleanMap;
    private ProcessInfoCollector mProcInfoCollector;

    private final class CleanHandler extends Handler {
        private CleanHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 4) {
                ProcessFastKillInfo procHold = (ProcessFastKillInfo) msg.obj;
                if (procHold != null) {
                    ProcessRecord proc = procHold.mApp;
                    if (proc != null) {
                        IBinder thread = procHold.mAppThread != null ? procHold.mAppThread.asBinder() : null;
                        boolean isNative = true;
                        if (msg.arg2 != 1) {
                            isNative = false;
                        }
                        HwActivityManager.cleanProcessResourceFast(proc.processName, procHold.mPid, thread, procHold.mAllowRestart, isNative);
                        ProcessCleaner.this.removeProcessFastKillLocked(proc.processName, procHold.mUid);
                        AwareLog.d(ProcessCleaner.TAG, "fast kill clean proc: " + proc + ", pid: " + procHold.mPid);
                    }
                }
            }
        }
    }

    public enum CleanType {
        NONE("do-nothing"),
        COMPACT("compact"),
        REMOVETASK("removetask"),
        KILL_ALLOW_START("kill-allow-start"),
        KILL_FORBID_START("kill-forbid-start"),
        KILL_DELAY_START("kill-delay-start"),
        FORCESTOP("force-stop"),
        FORCESTOP_REMOVETASK("force-stop-removetask"),
        FREEZE_NOMAL("freeze-nomal"),
        FREEZE_UP_DOWNLOAD("freeze-up-download"),
        IOLIMIT("iolimit"),
        FORCESTOP_ALARM("force-stop-alarm"),
        CPULIMIT("cpulimit");
        
        String mDescription;

        private CleanType(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private ProcessCleaner(Context context) {
        this.mHwAMS = null;
        this.mProcInfoCollector = null;
        this.mActivityManager = null;
        this.mMDMProtectedList = new ArrayList<>();
        this.mProcCleanMap = new ProcessMap<>();
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ProcessCleaner.this.cleanPackageNotifications((List) msg.obj, msg.arg1);
                        return;
                    case 2:
                        ProcessCleaner.this.cleanNotificationWithPid((List) msg.obj, msg.arg1, msg.arg2);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mCleanHandler = null;
        this.mCleanThread = null;
        this.mCleaning = new AtomicBoolean(false);
        this.mProcInfoCollector = ProcessInfoCollector.getInstance();
        this.mHwAMS = HwActivityManagerService.self();
        if (this.mHwAMS == null) {
            Slog.e(TAG, "init failed to get HwAMS handler");
        }
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        this.mCleanThread = new ServiceThread("iaware.clean", -2, false);
        this.mCleanThread.start();
        Looper loop = this.mCleanThread.getLooper();
        if (loop != null) {
            this.mCleanHandler = new CleanHandler(loop);
        }
    }

    public static synchronized ProcessCleaner getInstance(Context context) {
        ProcessCleaner processCleaner;
        synchronized (ProcessCleaner.class) {
            if (mProcessCleaner == null) {
                mProcessCleaner = new ProcessCleaner(context);
            }
            processCleaner = mProcessCleaner;
        }
        return processCleaner;
    }

    public static synchronized ProcessCleaner getInstance() {
        ProcessCleaner processCleaner;
        synchronized (ProcessCleaner.class) {
            processCleaner = mProcessCleaner;
        }
        return processCleaner;
    }

    public int uniformClean(AwareProcessBlockInfo procGroup, Bundle extras, String reason) {
        int killedCount = 0;
        if (procGroup == null) {
            return 0;
        }
        if (procGroup.mProcessList != null && !procGroup.mProcessList.isEmpty()) {
            switch (procGroup.mCleanType) {
                case KILL_ALLOW_START:
                case KILL_FORBID_START:
                    for (AwareProcessInfo awareProc : procGroup.mProcessList) {
                        if (killProcess(awareProc.mPid, procGroup.mCleanType == CleanType.KILL_ALLOW_START, reason)) {
                            killedCount++;
                        }
                    }
                    break;
                case REMOVETASK:
                    int result = removetask(procGroup);
                    if (result > 0) {
                        killedCount = 0 + result;
                        break;
                    }
                    break;
                case FORCESTOP_REMOVETASK:
                    if (forcestopAppsAsUser(procGroup.mProcessList.get(0), reason)) {
                        killedCount = 0 + procGroup.mProcessList.size();
                    }
                    removetask(procGroup);
                    break;
                case FORCESTOP:
                    if (forcestopAppsAsUser(procGroup.mProcessList.get(0), reason)) {
                        killedCount = 0 + procGroup.mProcessList.size();
                        break;
                    }
                    break;
                case FORCESTOP_ALARM:
                    List<Integer> killedPid = killProcessesSameUidExt(procGroup, null, false, false, reason, true);
                    if (killedPid != null) {
                        killedCount = 0 + killedPid.size();
                        break;
                    }
                    break;
            }
        }
        return killedCount;
    }

    private Map<String, List<String>> getAlarmTags(int uid, List<String> packageList) {
        Map<String, List<String>> map = null;
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
        if (!clearAll) {
            map = tags;
        }
        return map;
    }

    public boolean killProcess(int pid, boolean restartservice) {
        return killProcess(pid, restartservice, "null");
    }

    public boolean killProcess(int pid, boolean restartservice, String reason) {
        long start = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "process cleaner kill process: pid is " + pid + ", restart service :" + restartservice);
        }
        ProcessInfo temp = this.mProcInfoCollector.getProcessInfo(pid);
        if (temp == null) {
            Slog.e(TAG, "process cleaner kill process: process info is null ");
            return false;
        } else if (this.mHwAMS == null) {
            Slog.e(TAG, "process cleaner kill process: mHwAMS is null ");
            return false;
        } else if (!HwActivityManager.killProcessRecordFromMTM(temp, restartservice, reason)) {
            Slog.e(TAG, "process cleaner kill process: failed to kill ");
            return false;
        } else {
            this.mProcInfoCollector.recordKilledProcess(temp);
            long end = SystemClock.elapsedRealtime();
            if (this.mProcInfoCollector.INFO) {
                Slog.d(TAG, "process cleaner kill process: pid is " + pid + ",last time :" + (end - start));
            }
            return true;
        }
    }

    public int removetask(AwareProcessBlockInfo procGroup) {
        if (procGroup == null) {
            return 0;
        }
        if (this.mHwAMS == null) {
            AwareLog.e(TAG, "process cleaner kill process: mHwAMS is null ");
            return 0;
        } else if (procGroup.mProcessList == null) {
            return 0;
        } else {
            HashSet<Integer> taskIdSet = new HashSet<>();
            boolean success = false;
            for (AwareProcessInfo awareProc : procGroup.mProcessList) {
                if (awareProc != null) {
                    taskIdSet.add(Integer.valueOf(awareProc.mTaskId));
                }
            }
            Iterator<Integer> it = taskIdSet.iterator();
            while (it.hasNext()) {
                Integer taskId = it.next();
                if (taskId.intValue() != -1) {
                    if (this.mHwAMS.removeTask(taskId.intValue())) {
                        success = true;
                    } else {
                        AwareLog.e(TAG, "fail to removeTask: " + taskId);
                    }
                }
            }
            if (success) {
                return procGroup.mProcessList.size();
            }
            return 0;
        }
    }

    public List<Integer> killProcessesSameUidExt(AwareProcessBlockInfo procGroup, boolean quickKillAction, String reason, boolean needCheckAdj) {
        return killProcessesSameUidExt(procGroup, null, true, quickKillAction, reason, needCheckAdj);
    }

    /* JADX WARNING: Removed duplicated region for block: B:174:0x0392  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x043c  */
    public List<Integer> killProcessesSameUidExt(AwareProcessBlockInfo procGroup, AtomicBoolean interrupt, boolean isAsynchronous, boolean quickKillAction, String reason, boolean needCheckAdj) {
        String reason2;
        HwActivityManagerService hwActivityManagerService;
        List<AwareProcessInfo> dfxDataList;
        List<String> packageList;
        boolean hasPerceptAlarm;
        ArraySet<Integer> pidCantStop;
        HwActivityManagerService hwActivityManagerService2;
        boolean hasPerceptAlarm2;
        List<String> packageList2;
        boolean z;
        List<AwareProcessInfo> dfxDataList2;
        boolean isCleanAllRes;
        boolean hasPerceptAlarm3;
        boolean isCleanAllRes2;
        ArraySet<Integer> pidCantStop2;
        List<AwareProcessInfo> procInfoAllStopList;
        Iterator<AwareProcessInfo> it;
        AwareAppMngSort appMngSort;
        List<String> packageList3;
        List<AwareProcessInfo> pidsWithNotification;
        List<AwareProcessInfo> dfxDataList3;
        AwareProcessInfo info;
        String killHint;
        List<AwareProcessInfo> list;
        AwareProcessInfo info2;
        Map<String, List<String>> alarmTagMap;
        boolean isCleanUidActivity;
        boolean isCleanAllRes3;
        Iterator<AwareProcessInfo> it2;
        AwareProcessBlockInfo awareProcessBlockInfo = procGroup;
        String str = reason;
        if (awareProcessBlockInfo == null) {
            return null;
        }
        int targetUid = awareProcessBlockInfo.mUid;
        boolean resCleanAllow = awareProcessBlockInfo.mResCleanAllow;
        if (resCleanAllow) {
            reason2 = "iAwareF[" + str + "]";
        } else {
            reason2 = "iAwareK[" + str + "]";
        }
        String reason3 = reason2;
        List<AwareProcessInfo> procInfoAllStopList2 = procGroup.getProcessList();
        if (targetUid == 0) {
            boolean z2 = quickKillAction;
            List<AwareProcessInfo> list2 = procInfoAllStopList2;
        } else if (procInfoAllStopList2 == null) {
            boolean z3 = quickKillAction;
            List<AwareProcessInfo> list3 = procInfoAllStopList2;
        } else if (this.mHwAMS == null) {
            Slog.e(TAG, "[aware_mem] Why mHwAMS is null!!");
            return null;
        } else if (checkPkgInProtectedListFromMDM(awareProcessBlockInfo.mPackageName)) {
            Slog.d(TAG, "[aware_mem] " + awareProcessBlockInfo.mPackageName + " protected by MDM");
            return null;
        } else {
            List<Integer> killList = new ArrayList<>();
            List<String> packageList4 = getPackageList(procInfoAllStopList2);
            List<AwareProcessInfo> pidsWithNotification2 = getPidsWithNotification(procInfoAllStopList2);
            if (this.mProcInfoCollector.INFO) {
                Slog.d(TAG, "[aware_mem] start process cleaner kill process start");
            }
            AwareAppMngSort appMngSort2 = AwareAppMngSort.getInstance();
            if (awareProcessBlockInfo.mIsNativeForceStop || appMngSort2 == null || !appMngSort2.isProcessBlockPidChanged(awareProcessBlockInfo)) {
                boolean isCleanAllRes4 = resCleanAllow;
                boolean isCleanUidActivity2 = false;
                boolean needCheckAlarm = appMngSort2 != null ? appMngSort2.needCheckAlarm(awareProcessBlockInfo) : true;
                Map<String, List<String>> alarmTagMap2 = null;
                if (!awareProcessBlockInfo.mIsNativeForceStop) {
                    alarmTagMap2 = getAlarmTags(targetUid, packageList4);
                }
                Map<String, List<String>> alarmTagMap3 = alarmTagMap2;
                boolean hasPerceptAlarm4 = AwareIntelligentRecg.getInstance().hasPerceptAlarm(targetUid, packageList4);
                List<AwareProcessInfo> pidsWithNotification3 = pidsWithNotification2;
                HwActivityManagerService hwActivityManagerService3 = this.mHwAMS;
                synchronized (hwActivityManagerService3) {
                    if (resCleanAllow) {
                        dfxDataList = null;
                        try {
                            Slog.d(TAG, "[aware_mem] start process cleaner setPackageStoppedState");
                            this.mHwAMS.setPackageStoppedState(packageList4, true, targetUid);
                        } catch (Throwable th) {
                            th = th;
                            boolean z4 = isCleanAllRes4;
                            boolean isCleanUidActivity3 = hasPerceptAlarm4;
                            AwareAppMngSort awareAppMngSort = appMngSort2;
                            hwActivityManagerService = hwActivityManagerService3;
                            Map<String, List<String>> map = alarmTagMap3;
                            List<AwareProcessInfo> list4 = procInfoAllStopList2;
                            List<AwareProcessInfo> list5 = pidsWithNotification3;
                            boolean isCleanAllRes5 = quickKillAction;
                            List<String> list6 = packageList4;
                        }
                    } else {
                        dfxDataList = null;
                    }
                    try {
                        ArraySet<Integer> pidCantStop3 = new ArraySet<>();
                        if (needCheckAdj) {
                            try {
                                if (!awareProcessBlockInfo.mIsNativeForceStop) {
                                    Iterator<AwareProcessInfo> it3 = procInfoAllStopList2.iterator();
                                    while (it3.hasNext()) {
                                        AwareProcessInfo info3 = it3.next();
                                        if (appMngSort2 != null) {
                                            it2 = it3;
                                            AwareProcessInfo info4 = info3;
                                            if (info4 != null) {
                                                isCleanAllRes3 = isCleanAllRes4;
                                                try {
                                                    if (info4.mProcInfo != null) {
                                                        isCleanUidActivity = isCleanUidActivity2;
                                                        try {
                                                            hasPerceptAlarm = hasPerceptAlarm4;
                                                        } catch (Throwable th2) {
                                                            th = th2;
                                                            Map<String, List<String>> map2 = alarmTagMap3;
                                                            boolean z5 = quickKillAction;
                                                            boolean z6 = hasPerceptAlarm4;
                                                            AwareAppMngSort awareAppMngSort2 = appMngSort2;
                                                            hwActivityManagerService = hwActivityManagerService3;
                                                            List<AwareProcessInfo> list7 = procInfoAllStopList2;
                                                            List<AwareProcessInfo> list8 = pidsWithNotification3;
                                                            ArrayList arrayList = dfxDataList;
                                                            List<String> list9 = packageList4;
                                                            while (true) {
                                                                try {
                                                                    break;
                                                                } catch (Throwable th3) {
                                                                    th = th3;
                                                                }
                                                            }
                                                            throw th;
                                                        }
                                                        try {
                                                            packageList = packageList4;
                                                            try {
                                                                alarmTagMap = alarmTagMap3;
                                                                try {
                                                                    if (appMngSort2.isGroupBeHigher(info4.mPid, info4.mProcInfo.mUid, info4.mProcInfo.mProcessName, info4.mProcInfo.mPackageName, info4.mMemGroup)) {
                                                                        pidCantStop3.add(Integer.valueOf(info4.mPid));
                                                                    }
                                                                } catch (Throwable th4) {
                                                                    th = th4;
                                                                    boolean z7 = quickKillAction;
                                                                    AwareAppMngSort awareAppMngSort3 = appMngSort2;
                                                                    hwActivityManagerService = hwActivityManagerService3;
                                                                    List<AwareProcessInfo> list10 = procInfoAllStopList2;
                                                                    List<AwareProcessInfo> list11 = pidsWithNotification3;
                                                                    ArrayList arrayList2 = dfxDataList;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    throw th;
                                                                }
                                                            } catch (Throwable th5) {
                                                                th = th5;
                                                                Map<String, List<String>> map3 = alarmTagMap3;
                                                                boolean z8 = quickKillAction;
                                                                AwareAppMngSort awareAppMngSort4 = appMngSort2;
                                                                hwActivityManagerService = hwActivityManagerService3;
                                                                List<AwareProcessInfo> list12 = procInfoAllStopList2;
                                                                List<AwareProcessInfo> list13 = pidsWithNotification3;
                                                                ArrayList arrayList3 = dfxDataList;
                                                                boolean z9 = hasPerceptAlarm;
                                                                List<String> list14 = packageList;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                throw th;
                                                            }
                                                        } catch (Throwable th6) {
                                                            th = th6;
                                                            Map<String, List<String>> map4 = alarmTagMap3;
                                                            boolean z10 = quickKillAction;
                                                            AwareAppMngSort awareAppMngSort5 = appMngSort2;
                                                            hwActivityManagerService = hwActivityManagerService3;
                                                            List<AwareProcessInfo> list15 = procInfoAllStopList2;
                                                            List<AwareProcessInfo> list16 = pidsWithNotification3;
                                                            ArrayList arrayList4 = dfxDataList;
                                                            boolean z11 = hasPerceptAlarm;
                                                            List<String> list17 = packageList4;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } else {
                                                        isCleanUidActivity = isCleanUidActivity2;
                                                        hasPerceptAlarm = hasPerceptAlarm4;
                                                        packageList = packageList4;
                                                        alarmTagMap = alarmTagMap3;
                                                    }
                                                    it3 = it2;
                                                    isCleanAllRes4 = isCleanAllRes3;
                                                    isCleanUidActivity2 = isCleanUidActivity;
                                                    hasPerceptAlarm4 = hasPerceptAlarm;
                                                    packageList4 = packageList;
                                                    alarmTagMap3 = alarmTagMap;
                                                } catch (Throwable th7) {
                                                    th = th7;
                                                    boolean z12 = isCleanUidActivity2;
                                                    Map<String, List<String>> map5 = alarmTagMap3;
                                                    boolean z13 = quickKillAction;
                                                    boolean isCleanUidActivity4 = hasPerceptAlarm4;
                                                    AwareAppMngSort awareAppMngSort6 = appMngSort2;
                                                    hwActivityManagerService = hwActivityManagerService3;
                                                    List<AwareProcessInfo> list18 = procInfoAllStopList2;
                                                    List<AwareProcessInfo> list19 = pidsWithNotification3;
                                                    ArrayList arrayList5 = dfxDataList;
                                                    List<String> list20 = packageList4;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            }
                                        } else {
                                            it2 = it3;
                                        }
                                        isCleanAllRes3 = isCleanAllRes4;
                                        isCleanUidActivity = isCleanUidActivity2;
                                        hasPerceptAlarm = hasPerceptAlarm4;
                                        packageList = packageList4;
                                        alarmTagMap = alarmTagMap3;
                                        it3 = it2;
                                        isCleanAllRes4 = isCleanAllRes3;
                                        isCleanUidActivity2 = isCleanUidActivity;
                                        hasPerceptAlarm4 = hasPerceptAlarm;
                                        packageList4 = packageList;
                                        alarmTagMap3 = alarmTagMap;
                                    }
                                }
                            } catch (Throwable th8) {
                                th = th8;
                                boolean z14 = isCleanAllRes4;
                                Map<String, List<String>> map6 = alarmTagMap3;
                                boolean isCleanAllRes6 = quickKillAction;
                                boolean isCleanUidActivity5 = hasPerceptAlarm4;
                                AwareAppMngSort awareAppMngSort7 = appMngSort2;
                                hwActivityManagerService = hwActivityManagerService3;
                                List<AwareProcessInfo> list21 = procInfoAllStopList2;
                                List<AwareProcessInfo> list22 = pidsWithNotification3;
                                ArrayList arrayList6 = dfxDataList;
                                List<String> list23 = packageList4;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        boolean isCleanAllRes7 = isCleanAllRes4;
                        boolean isCleanUidActivity6 = isCleanUidActivity2;
                        hasPerceptAlarm = hasPerceptAlarm4;
                        packageList = packageList4;
                        Map<String, List<String>> alarmTagMap4 = alarmTagMap3;
                        try {
                            Iterator<AwareProcessInfo> it4 = procInfoAllStopList2.iterator();
                            List<AwareProcessInfo> dfxDataList4 = dfxDataList;
                            while (true) {
                                try {
                                    if (!it4.hasNext()) {
                                        pidCantStop = pidCantStop3;
                                        AwareAppMngSort awareAppMngSort8 = appMngSort2;
                                        hwActivityManagerService2 = hwActivityManagerService3;
                                        List<AwareProcessInfo> list24 = procInfoAllStopList2;
                                        List<AwareProcessInfo> list25 = pidsWithNotification3;
                                        hasPerceptAlarm2 = hasPerceptAlarm;
                                        packageList2 = packageList;
                                        z = true;
                                        dfxDataList2 = dfxDataList4;
                                        isCleanAllRes = isCleanAllRes7;
                                        break;
                                    }
                                    try {
                                        AwareProcessInfo info5 = it4.next();
                                        if (interrupt != null) {
                                            try {
                                                if (interrupt.get()) {
                                                    pidCantStop = pidCantStop3;
                                                    AwareAppMngSort awareAppMngSort9 = appMngSort2;
                                                    hwActivityManagerService2 = hwActivityManagerService3;
                                                    isCleanAllRes = false;
                                                    List<AwareProcessInfo> list26 = procInfoAllStopList2;
                                                    List<AwareProcessInfo> list27 = pidsWithNotification3;
                                                    hasPerceptAlarm2 = hasPerceptAlarm;
                                                    packageList2 = packageList;
                                                    z = true;
                                                    dfxDataList2 = dfxDataList4;
                                                    break;
                                                }
                                            } catch (Throwable th9) {
                                                th = th9;
                                                boolean z15 = quickKillAction;
                                                AwareAppMngSort awareAppMngSort10 = appMngSort2;
                                                hwActivityManagerService = hwActivityManagerService3;
                                                ArrayList arrayList7 = dfxDataList4;
                                                List<AwareProcessInfo> list28 = procInfoAllStopList2;
                                                List<AwareProcessInfo> list29 = pidsWithNotification3;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        }
                                        boolean killResult = false;
                                        if (awareProcessBlockInfo.mIsNativeForceStop) {
                                            try {
                                                info2 = info5;
                                                hasPerceptAlarm3 = hasPerceptAlarm;
                                                appMngSort = appMngSort2;
                                                hwActivityManagerService = hwActivityManagerService3;
                                                it = it4;
                                                pidsWithNotification = pidsWithNotification3;
                                                dfxDataList3 = dfxDataList4;
                                                procInfoAllStopList = procInfoAllStopList2;
                                                packageList3 = packageList;
                                            } catch (Throwable th10) {
                                                th = th10;
                                                AwareAppMngSort awareAppMngSort11 = appMngSort2;
                                                hwActivityManagerService = hwActivityManagerService3;
                                                List<AwareProcessInfo> list30 = procInfoAllStopList2;
                                                List<AwareProcessInfo> list31 = pidsWithNotification3;
                                                List<String> list32 = packageList;
                                                boolean z16 = quickKillAction;
                                                List<AwareProcessInfo> list33 = dfxDataList4;
                                                boolean z17 = hasPerceptAlarm;
                                                AwareProcessBlockInfo awareProcessBlockInfo2 = procGroup;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                            try {
                                                killProcessSameUid(info5.mPid, info5.getRestartFlag(), isAsynchronous, reason3, true, needCheckAdj);
                                                killResult = true;
                                                pidCantStop2 = pidCantStop3;
                                                info = info2;
                                            } catch (Throwable th11) {
                                                th = th11;
                                                boolean z18 = quickKillAction;
                                                AwareProcessBlockInfo awareProcessBlockInfo3 = procGroup;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        } else {
                                            appMngSort = appMngSort2;
                                            hwActivityManagerService = hwActivityManagerService3;
                                            dfxDataList3 = dfxDataList4;
                                            it = it4;
                                            procInfoAllStopList = procInfoAllStopList2;
                                            pidsWithNotification = pidsWithNotification3;
                                            hasPerceptAlarm3 = hasPerceptAlarm;
                                            packageList3 = packageList;
                                            AwareProcessInfo info6 = info5;
                                            try {
                                                if (!pidCantStop3.contains(Integer.valueOf(info6.mPid))) {
                                                    pidCantStop2 = pidCantStop3;
                                                    info = info6;
                                                    killResult = killProcessSameUid(info6.mPid, info6.getRestartFlag(), isAsynchronous, reason3, false, needCheckAdj);
                                                } else {
                                                    pidCantStop2 = pidCantStop3;
                                                    info = info6;
                                                }
                                            } catch (Throwable th12) {
                                                th = th12;
                                                List<AwareProcessInfo> list34 = dfxDataList3;
                                                AwareProcessBlockInfo awareProcessBlockInfo4 = procGroup;
                                                boolean z19 = quickKillAction;
                                                List<AwareProcessInfo> list35 = list34;
                                                boolean z20 = hasPerceptAlarm3;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        }
                                        if (killResult) {
                                            killList.add(Integer.valueOf(info.mPid));
                                            if (!resCleanAllow && info.mProcInfo != null && pidsWithNotification.contains(info) && !info.mRestartFlag) {
                                                Message msg = this.mHandler.obtainMessage(2);
                                                msg.obj = packageList3;
                                                msg.arg1 = targetUid;
                                                msg.arg2 = info.mPid;
                                                this.mHandler.sendMessageDelayed(msg, 200);
                                                Slog.d(TAG, "[aware_mem] clean notification " + info.mProcInfo.mProcessName);
                                            }
                                            boolean isCleanUidActivity7 = (resCleanAllow || !info.mHasShownUi || this.mHwAMS.numOfPidWithActivity(targetUid) != 0) ? isCleanUidActivity6 : true;
                                            try {
                                                if (AwareConstant.CURRENT_USER_TYPE == 3) {
                                                    if (dfxDataList3 == null) {
                                                        list = new ArrayList<>();
                                                    } else {
                                                        list = dfxDataList3;
                                                    }
                                                    try {
                                                        list.add(info);
                                                        dfxDataList4 = list;
                                                        isCleanUidActivity6 = isCleanUidActivity7;
                                                        if (info.mProcInfo == null) {
                                                            if (killResult) {
                                                                killHint = "success ";
                                                            } else {
                                                                killHint = "fail ";
                                                            }
                                                            StringBuilder sb = new StringBuilder();
                                                            sb.append("[aware_mem] process cleaner ");
                                                            sb.append(killHint);
                                                            sb.append("pid:");
                                                            sb.append(info.mPid);
                                                            sb.append(",uid:");
                                                            sb.append(info.mProcInfo.mUid);
                                                            sb.append(",");
                                                            sb.append(info.mProcInfo.mProcessName);
                                                            sb.append(",");
                                                            sb.append(info.mProcInfo.mPackageName);
                                                            sb.append(",mHasShownUi:");
                                                            sb.append(info.mHasShownUi);
                                                            sb.append(",");
                                                            awareProcessBlockInfo = procGroup;
                                                            try {
                                                                sb.append(awareProcessBlockInfo.mSubTypeStr);
                                                                sb.append(",class:");
                                                                sb.append(awareProcessBlockInfo.mClassRate);
                                                                sb.append(",");
                                                                sb.append(awareProcessBlockInfo.mSubClassRate);
                                                                sb.append(",");
                                                                sb.append(info.mClassRate);
                                                                sb.append(",");
                                                                sb.append(info.mSubClassRate);
                                                                sb.append(",adj:");
                                                                sb.append(info.mProcInfo.mCurAdj);
                                                                sb.append(killResult ? " is killed" : "");
                                                                Slog.d(TAG, sb.toString());
                                                            } catch (Throwable th13) {
                                                                th = th13;
                                                                boolean z21 = quickKillAction;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                throw th;
                                                            }
                                                        } else {
                                                            awareProcessBlockInfo = procGroup;
                                                        }
                                                        pidsWithNotification3 = pidsWithNotification;
                                                        packageList = packageList3;
                                                        appMngSort2 = appMngSort;
                                                        hwActivityManagerService3 = hwActivityManagerService;
                                                        hasPerceptAlarm = hasPerceptAlarm3;
                                                        it4 = it;
                                                        procInfoAllStopList2 = procInfoAllStopList;
                                                        pidCantStop3 = pidCantStop2;
                                                    } catch (Throwable th14) {
                                                        th = th14;
                                                        boolean z22 = quickKillAction;
                                                        boolean z23 = isCleanUidActivity7;
                                                        AwareProcessBlockInfo awareProcessBlockInfo32 = procGroup;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                } else {
                                                    isCleanUidActivity6 = isCleanUidActivity7;
                                                }
                                            } catch (Throwable th15) {
                                                th = th15;
                                                boolean z24 = quickKillAction;
                                                boolean z25 = isCleanUidActivity7;
                                                AwareProcessBlockInfo awareProcessBlockInfo322 = procGroup;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        } else {
                                            isCleanAllRes7 = false;
                                        }
                                        dfxDataList4 = dfxDataList3;
                                    } catch (Throwable th16) {
                                        th = th16;
                                        AwareAppMngSort awareAppMngSort12 = appMngSort2;
                                        hwActivityManagerService = hwActivityManagerService3;
                                        List<AwareProcessInfo> list36 = procInfoAllStopList2;
                                        List<AwareProcessInfo> list37 = pidsWithNotification3;
                                        List<String> list38 = packageList;
                                        boolean z26 = quickKillAction;
                                        List<AwareProcessInfo> list39 = dfxDataList4;
                                        boolean z27 = hasPerceptAlarm;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                    try {
                                        if (info.mProcInfo == null) {
                                        }
                                        pidsWithNotification3 = pidsWithNotification;
                                        packageList = packageList3;
                                        appMngSort2 = appMngSort;
                                        hwActivityManagerService3 = hwActivityManagerService;
                                        hasPerceptAlarm = hasPerceptAlarm3;
                                        it4 = it;
                                        procInfoAllStopList2 = procInfoAllStopList;
                                        pidCantStop3 = pidCantStop2;
                                    } catch (Throwable th17) {
                                        th = th17;
                                        AwareProcessBlockInfo awareProcessBlockInfo5 = procGroup;
                                        boolean z212 = quickKillAction;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th18) {
                                    th = th18;
                                    boolean z28 = quickKillAction;
                                    AwareAppMngSort awareAppMngSort13 = appMngSort2;
                                    hwActivityManagerService = hwActivityManagerService3;
                                    List<AwareProcessInfo> list40 = procInfoAllStopList2;
                                    List<AwareProcessInfo> list41 = pidsWithNotification3;
                                    boolean z29 = hasPerceptAlarm;
                                    List<String> list42 = packageList;
                                    List<AwareProcessInfo> list43 = dfxDataList4;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            }
                            if (isCleanAllRes) {
                                boolean isAlarmFlag = false;
                                if (needCheckAlarm) {
                                    try {
                                        isAlarmFlag = this.mHwAMS.isPkgHasAlarm(packageList2, targetUid);
                                    } catch (Throwable th19) {
                                        th = th19;
                                        boolean z30 = quickKillAction;
                                        boolean z31 = isCleanAllRes;
                                        List<AwareProcessInfo> list44 = dfxDataList2;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                                if (isAlarmFlag) {
                                    Slog.d(TAG, "[aware_mem] is alarm " + packageList2);
                                    this.mHwAMS.setPackageStoppedState(packageList2, false, targetUid);
                                    isCleanAllRes2 = isCleanAllRes;
                                    dfxDataList4 = dfxDataList2;
                                    boolean isCleanAllRes8 = hasPerceptAlarm3;
                                    ArraySet<Integer> arraySet = pidCantStop;
                                } else {
                                    try {
                                        boolean z32 = z;
                                        ArraySet<Integer> arraySet2 = pidCantStop;
                                        isCleanAllRes2 = isCleanAllRes;
                                        dfxDataList4 = dfxDataList2;
                                    } catch (Throwable th20) {
                                        th = th20;
                                        boolean z33 = isCleanAllRes;
                                        boolean isCleanAllRes9 = hasPerceptAlarm3;
                                        boolean z34 = quickKillAction;
                                        List<AwareProcessInfo> list45 = dfxDataList2;
                                        boolean z35 = z33;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                    try {
                                        boolean cleanResult = HwActivityManager.cleanPackageRes(packageList2, alarmTagMap4, targetUid, awareProcessBlockInfo.mCleanAlarm, awareProcessBlockInfo.mIsNativeForceStop, hasPerceptAlarm3);
                                        StringBuilder sb2 = new StringBuilder();
                                        sb2.append("[aware_mem] start process cleaner cleanPackageRes, clnAlarm:");
                                        sb2.append(awareProcessBlockInfo.mCleanAlarm);
                                        sb2.append(", hasPerceptAlarm:");
                                        boolean hasPerceptAlarm5 = hasPerceptAlarm3;
                                        sb2.append(hasPerceptAlarm5);
                                        sb2.append(", isNative:");
                                        sb2.append(awareProcessBlockInfo.mIsNativeForceStop);
                                        sb2.append(", cleanResult: ");
                                        sb2.append(cleanResult);
                                        Slog.d(TAG, sb2.toString());
                                        if (!awareProcessBlockInfo.mIsNativeForceStop && hasPerceptAlarm5) {
                                            this.mHwAMS.setPackageStoppedState(packageList2, false, targetUid);
                                        }
                                    } catch (Throwable th21) {
                                        th = th21;
                                        boolean z36 = quickKillAction;
                                        List<AwareProcessInfo> list46 = dfxDataList4;
                                        boolean z37 = isCleanAllRes2;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                            } else {
                                isCleanAllRes2 = isCleanAllRes;
                                dfxDataList4 = dfxDataList2;
                                boolean isCleanAllRes10 = hasPerceptAlarm3;
                                ArraySet<Integer> arraySet3 = pidCantStop;
                                if (resCleanAllow) {
                                    Slog.d(TAG, "[aware_mem] start process cleaner reset PackageStoppedState");
                                    this.mHwAMS.setPackageStoppedState(packageList2, false, targetUid);
                                }
                            }
                            if (isCleanUidActivity6) {
                                Slog.d(TAG, "[aware_mem] clean uid activity:" + targetUid);
                                this.mHwAMS.cleanActivityByUid(packageList2, targetUid);
                            }
                            try {
                                boolean isCleanAllRes11 = isCleanAllRes2;
                                if (isCleanAllRes11) {
                                    Message msg2 = this.mHandler.obtainMessage(1);
                                    msg2.obj = packageList2;
                                    msg2.arg1 = targetUid;
                                    this.mHandler.sendMessageDelayed(msg2, 200);
                                }
                                if (this.mProcInfoCollector.INFO) {
                                    Slog.d(TAG, "[aware_mem] process cleaner kill pids:" + killList.toString());
                                }
                                if (AwareConstant.CURRENT_USER_TYPE == 3) {
                                    AwareAppMngDFX.getInstance().trackeKillInfo(dfxDataList4, isCleanAllRes11, quickKillAction);
                                } else {
                                    boolean z38 = quickKillAction;
                                }
                                return killList.size() > 0 ? killList : null;
                            } catch (Throwable th22) {
                                th = th22;
                                boolean z39 = quickKillAction;
                                boolean z40 = isCleanAllRes2;
                                List<AwareProcessInfo> list47 = dfxDataList4;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } catch (Throwable th23) {
                            th = th23;
                            boolean z41 = quickKillAction;
                            AwareAppMngSort awareAppMngSort14 = appMngSort2;
                            hwActivityManagerService = hwActivityManagerService3;
                            List<AwareProcessInfo> list48 = procInfoAllStopList2;
                            List<AwareProcessInfo> list49 = pidsWithNotification3;
                            boolean z42 = hasPerceptAlarm;
                            List<String> list50 = packageList;
                            List<AwareProcessInfo> list51 = dfxDataList;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th24) {
                        th = th24;
                        boolean z43 = isCleanAllRes4;
                        boolean isCleanUidActivity8 = hasPerceptAlarm4;
                        AwareAppMngSort awareAppMngSort15 = appMngSort2;
                        hwActivityManagerService = hwActivityManagerService3;
                        Map<String, List<String>> map7 = alarmTagMap3;
                        List<AwareProcessInfo> list52 = procInfoAllStopList2;
                        List<AwareProcessInfo> list53 = pidsWithNotification3;
                        boolean isCleanAllRes12 = quickKillAction;
                        List<String> list54 = packageList4;
                        List<AwareProcessInfo> list55 = dfxDataList;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } else {
                if (this.mProcInfoCollector.INFO) {
                    Slog.d(TAG, "[aware_mem] new process has started in block, uid: " + targetUid);
                }
                return null;
            }
        }
        return null;
    }

    public void beginKillFast() {
        this.mCleaning.set(true);
    }

    public void endKillFast() {
        this.mCleaning.set(false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0351, code lost:
        r1 = r16;
     */
    public List<Integer> killProcessesSameUidFast(AwareProcessBlockInfo procGroup, AtomicBoolean interrupt, boolean isAsynchronous, boolean quickKillAction, String reason, boolean needCheckAdj) {
        int i;
        List<AwareProcessInfo> dfxDataList;
        int targetUid;
        List<String> packageList;
        long j;
        boolean isCleanAllRes;
        int appUid;
        Iterator<AwareProcessInfo> it;
        List<AwareProcessInfo> procInfoAllStopList;
        List<AwareProcessInfo> dfxDataList2;
        String appProcName;
        List<AwareProcessInfo> procInfoAllStopList2;
        AwareProcessInfo info;
        ArraySet<Integer> pidCantStop;
        int targetUid2;
        List<String> packageList2;
        List<AwareProcessInfo> list;
        AwareAppMngSort appMngSort;
        List<String> packageList3;
        Iterator<AwareProcessInfo> it2;
        int targetUid3;
        List<AwareProcessInfo> dfxDataList3;
        ArraySet<Integer> pidCantStop2;
        AwareProcessBlockInfo awareProcessBlockInfo = procGroup;
        if (awareProcessBlockInfo == null) {
            return null;
        }
        int targetUid4 = awareProcessBlockInfo.mUid;
        boolean resCleanAllow = awareProcessBlockInfo.mResCleanAllow;
        List<AwareProcessInfo> procInfoAllStopList3 = procGroup.getProcessList();
        if (targetUid4 == 0) {
            boolean z = quickKillAction;
            int i2 = targetUid4;
            List<AwareProcessInfo> list2 = procInfoAllStopList3;
        } else if (procInfoAllStopList3 == null) {
            boolean z2 = quickKillAction;
            int i3 = targetUid4;
            List<AwareProcessInfo> list3 = procInfoAllStopList3;
        } else if (this.mHwAMS == null) {
            AwareLog.e(TAG, "[aware_mem] Why mHwAMS is null!");
            return null;
        } else if (checkPkgInProtectedListFromMDM(awareProcessBlockInfo.mPackageName)) {
            AwareLog.d(TAG, "[aware_mem] " + awareProcessBlockInfo.mPackageName + " protected by MDM");
            return null;
        } else {
            List<Integer> killList = new ArrayList<>();
            List<AwareProcessInfo> dfxDataList4 = null;
            List<String> packageList4 = getPackageList(procInfoAllStopList3);
            List<AwareProcessInfo> pidsWithNotification = getPidsWithNotification(procInfoAllStopList3);
            AwareAppMngSort appMngSort2 = AwareAppMngSort.getInstance();
            if (awareProcessBlockInfo.mIsNativeForceStop || appMngSort2 == null || !appMngSort2.isProcessBlockPidChanged(awareProcessBlockInfo)) {
                boolean isCleanAllRes2 = resCleanAllow;
                ArraySet<Integer> pidCantStop3 = new ArraySet<>();
                if (needCheckAdj && !awareProcessBlockInfo.mIsNativeForceStop) {
                    Iterator<AwareProcessInfo> it3 = procInfoAllStopList3.iterator();
                    while (it3.hasNext()) {
                        AwareProcessInfo info2 = it3.next();
                        if (appMngSort2 == null || info2 == null || info2.mProcInfo == null) {
                            dfxDataList3 = dfxDataList4;
                            it2 = it3;
                            pidCantStop2 = pidCantStop3;
                            appMngSort = appMngSort2;
                            targetUid3 = targetUid4;
                            packageList3 = packageList4;
                        } else {
                            dfxDataList3 = dfxDataList4;
                            targetUid3 = targetUid4;
                            packageList3 = packageList4;
                            AwareProcessInfo info3 = info2;
                            it2 = it3;
                            pidCantStop2 = pidCantStop3;
                            appMngSort = appMngSort2;
                            if (appMngSort2.isGroupBeHigher(info2.mPid, info2.mProcInfo.mUid, info2.mProcInfo.mProcessName, info2.mProcInfo.mPackageName, info2.mMemGroup)) {
                                pidCantStop2.add(Integer.valueOf(info3.mPid));
                            }
                        }
                        pidCantStop3 = pidCantStop2;
                        dfxDataList4 = dfxDataList3;
                        targetUid4 = targetUid3;
                        it3 = it2;
                        packageList4 = packageList3;
                        appMngSort2 = appMngSort;
                    }
                }
                ArraySet<Integer> pidCantStop4 = pidCantStop3;
                AwareAppMngSort awareAppMngSort = appMngSort2;
                int targetUid5 = targetUid4;
                List<String> packageList5 = packageList4;
                Iterator<AwareProcessInfo> it4 = procInfoAllStopList3.iterator();
                List<AwareProcessInfo> list4 = dfxDataList4;
                while (true) {
                    if (!it4.hasNext()) {
                        i = 3;
                        ArraySet<Integer> arraySet = pidCantStop4;
                        List<AwareProcessInfo> list5 = procInfoAllStopList3;
                        dfxDataList = list4;
                        targetUid = targetUid5;
                        packageList = packageList5;
                        j = 200;
                        break;
                    }
                    AwareProcessInfo info4 = it4.next();
                    if (info4 != null) {
                        if (interrupt != null && interrupt.get()) {
                            i = 3;
                            ArraySet<Integer> arraySet2 = pidCantStop4;
                            List<AwareProcessInfo> list6 = procInfoAllStopList3;
                            dfxDataList = list4;
                            isCleanAllRes = false;
                            targetUid = targetUid5;
                            packageList = packageList5;
                            j = 200;
                            break;
                        }
                        List<AwareProcessInfo> app = this.mHwAMS.getProcessRecordLocked(info4.mPid);
                        if (app != null) {
                            boolean killResult = false;
                            int appUid2 = app.uid;
                            String appProcName2 = app.processName;
                            if (awareProcessBlockInfo.mIsNativeForceStop) {
                                it = it4;
                                appProcName = appProcName2;
                                appUid = appUid2;
                                procInfoAllStopList = procInfoAllStopList3;
                                procInfoAllStopList2 = app;
                                dfxDataList2 = list4;
                                info = info4;
                                killResult = killProcessFast(info4.mPid, info4.getRestartFlag(), isAsynchronous, reason, true, needCheckAdj);
                            } else {
                                appUid = appUid2;
                                it = it4;
                                procInfoAllStopList = procInfoAllStopList3;
                                dfxDataList2 = list4;
                                appProcName = appProcName2;
                                procInfoAllStopList2 = app;
                                info = info4;
                                if (!pidCantStop4.contains(Integer.valueOf(info.mPid))) {
                                    killResult = killProcessFast(info.mPid, info.getRestartFlag(), isAsynchronous, reason, false, needCheckAdj);
                                }
                            }
                            boolean killResult2 = killResult;
                            if (killResult2) {
                                IApplicationThread thread = procInfoAllStopList2.thread;
                                ProcessFastKillInfo processFastKillInfo = new ProcessFastKillInfo(procInfoAllStopList2, appUid, info.mPid, thread, info.getRestartFlag());
                                addProcessFastKillLocked(processFastKillInfo, appProcName, appUid);
                                killList.add(Integer.valueOf(info.mPid));
                                if (resCleanAllow || info.mProcInfo == null || !pidsWithNotification.contains(info) || info.mRestartFlag) {
                                    pidCantStop = pidCantStop4;
                                    String str = appProcName;
                                    targetUid2 = targetUid5;
                                    packageList2 = packageList5;
                                } else {
                                    Message msg = this.mHandler.obtainMessage(2);
                                    packageList2 = packageList5;
                                    msg.obj = packageList2;
                                    targetUid2 = targetUid5;
                                    msg.arg1 = targetUid2;
                                    IApplicationThread iApplicationThread = thread;
                                    msg.arg2 = info.mPid;
                                    pidCantStop = pidCantStop4;
                                    String str2 = appProcName;
                                    this.mHandler.sendMessageDelayed(msg, 200);
                                    AwareLog.d(TAG, "[aware_mem] clean notification " + info.mProcInfo.mProcessName);
                                }
                                Message msg2 = this.mCleanHandler.obtainMessage(4);
                                msg2.obj = processFastKillInfo;
                                msg2.arg1 = targetUid2;
                                if (info.mHasShownUi) {
                                    msg2.arg2 = awareProcessBlockInfo.mIsNativeForceStop ? 1 : 0;
                                    this.mCleanHandler.sendMessageAtFrontOfQueue(msg2);
                                } else {
                                    msg2.arg2 = 0;
                                    this.mCleanHandler.sendMessage(msg2);
                                }
                                if (AwareConstant.CURRENT_USER_TYPE == 3) {
                                    if (dfxDataList2 == null) {
                                        list = new ArrayList<>();
                                    } else {
                                        list = dfxDataList2;
                                    }
                                    list.add(info);
                                } else {
                                    list = dfxDataList2;
                                }
                            } else {
                                pidCantStop = pidCantStop4;
                                String str3 = appProcName;
                                targetUid2 = targetUid5;
                                packageList2 = packageList5;
                                int i4 = appUid;
                                AwareLog.w(TAG, "not clean res, killResult:" + killResult2 + ", app:" + procInfoAllStopList2 + ", pid:" + info.mPid);
                                isCleanAllRes2 = false;
                                list = dfxDataList2;
                            }
                            if (info.mProcInfo != null) {
                                String killHint = killResult2 ? "success " : "fail ";
                                StringBuilder sb = new StringBuilder();
                                sb.append("[aware_mem] fast kill ");
                                sb.append(killHint);
                                sb.append("pid:");
                                sb.append(info.mPid);
                                sb.append(",uid:");
                                sb.append(info.mProcInfo.mUid);
                                sb.append(",");
                                sb.append(info.mProcInfo.mProcessName);
                                sb.append(",");
                                sb.append(info.mProcInfo.mPackageName);
                                sb.append(",mHasShownUi:");
                                sb.append(info.mHasShownUi);
                                sb.append(",");
                                sb.append(awareProcessBlockInfo.mSubTypeStr);
                                sb.append(",class:");
                                sb.append(awareProcessBlockInfo.mClassRate);
                                sb.append(",");
                                sb.append(awareProcessBlockInfo.mSubClassRate);
                                sb.append(",");
                                sb.append(info.mClassRate);
                                sb.append(",");
                                sb.append(info.mSubClassRate);
                                sb.append(",adj:");
                                sb.append(info.mProcInfo.mCurAdj);
                                sb.append(killResult2 ? " is killed" : "");
                                AwareLog.d(TAG, sb.toString());
                            }
                            list4 = list;
                            packageList5 = packageList2;
                            targetUid5 = targetUid2;
                            it4 = it;
                            procInfoAllStopList3 = procInfoAllStopList;
                            pidCantStop4 = pidCantStop;
                        }
                    } else {
                        i = 3;
                        ArraySet<Integer> arraySet3 = pidCantStop4;
                        List<AwareProcessInfo> list7 = procInfoAllStopList3;
                        dfxDataList = list4;
                        targetUid = targetUid5;
                        packageList = packageList5;
                        j = 200;
                        break;
                    }
                }
                if (isCleanAllRes) {
                    Message msg3 = this.mHandler.obtainMessage(1);
                    msg3.obj = packageList;
                    msg3.arg1 = targetUid;
                    this.mHandler.sendMessageDelayed(msg3, j);
                    AwareLog.d(TAG, "[aware_mem] clean uid notification:" + targetUid + ", pkg: " + awareProcessBlockInfo.mPackageName);
                }
                if (AwareConstant.CURRENT_USER_TYPE == i) {
                    AwareAppMngDFX.getInstance().trackeKillInfo(dfxDataList, isCleanAllRes, quickKillAction);
                } else {
                    boolean z3 = quickKillAction;
                    List<AwareProcessInfo> list8 = dfxDataList;
                }
                return killList.size() > 0 ? killList : null;
            }
            AwareLog.d(TAG, "[aware_mem] new process has started in block, uid: " + targetUid4);
            return null;
        }
        return null;
    }

    public boolean forcestopAppsAsUser(AwareProcessInfo awareProc, String reason) {
        if (awareProc == null) {
            return false;
        }
        ProcessInfo temp = awareProc.mProcInfo;
        if (temp == null) {
            AwareLog.e(TAG, "forcestopAppsAsUser kill package: package info is null ");
            return false;
        }
        String packagename = (String) temp.mPackageName.get(0);
        if (packagename == null || packagename.equals(" ")) {
            AwareLog.e(TAG, "forcestopAppsAsUser kill package: packagename == null");
            return false;
        }
        int userId = UserHandle.getUserId(temp.mUid);
        if (this.mHwAMS != null) {
            ThreadLocal threadLocal = this.mHwAMS.mLocalStopReason;
            threadLocal.set("iAwareF[" + reason + "]");
            this.mHwAMS.forceStopPackage(packagename, userId);
            return true;
        }
        AwareLog.e(TAG, "forcestopAppsAsUser process: mActivityManager is null ");
        return false;
    }

    public boolean forcestopApps(int pid) {
        long start = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "forcestopApps kill process: pid is " + pid);
        }
        ProcessInfo temp = this.mProcInfoCollector.getProcessInfo(pid);
        if (temp == null) {
            Slog.e(TAG, "forcestopApps kill process: process info is null ");
            return false;
        } else if (temp.mCurSchedGroup != 0) {
            Slog.e(TAG, "forcestopApps kill process: process " + temp.mProcessName + " is not in BG");
            return false;
        } else {
            String packagename = (String) temp.mPackageName.get(0);
            if (packagename == null || packagename.equals(" ")) {
                Slog.e(TAG, "forcestopApps kill process: packagename == null");
                return false;
            } else if (this.mActivityManager != null) {
                this.mActivityManager.forceStopPackage(packagename);
                this.mProcInfoCollector.recordKilledProcess(temp);
                long end = SystemClock.elapsedRealtime();
                if (this.mProcInfoCollector.INFO) {
                    Slog.d(TAG, "pforcestopApps kill process: pid is " + pid + ",last time :" + (end - start));
                }
                return true;
            } else {
                Slog.e(TAG, "forcestopApps process: mActivityManager is null ");
                return false;
            }
        }
    }

    private List<String> getPackageList(List<AwareProcessInfo> procInfoAllStopList) {
        List<String> packageList = new ArrayList<>();
        for (AwareProcessInfo curPIAllStop : procInfoAllStopList) {
            if (!(curPIAllStop.mProcInfo == null || curPIAllStop.mProcInfo.mPackageName == null)) {
                Iterator it = curPIAllStop.mProcInfo.mPackageName.iterator();
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
            if (hasNotification(info.mPid)) {
                pidsWithNotification.add(info);
            }
        }
        return pidsWithNotification;
    }

    public boolean killProcessSameUid(int pid, boolean restartservice, boolean isAsynchronous, String reason, boolean isNative, boolean needCheckAdj) {
        long start = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "[aware_mem] process cleaner kill process: pid is " + pid + ", restart service :" + restartservice);
        }
        ProcessInfo temp = this.mProcInfoCollector.getProcessInfo(pid);
        if (temp == null) {
            Slog.e(TAG, "[aware_mem] process cleaner kill process: process info is null ");
            return false;
        }
        if (isNative) {
            if (!HwActivityManager.killProcessRecordFromIAwareNative(temp, restartservice, isAsynchronous, reason)) {
                return false;
            }
        } else if (!HwActivityManager.killProcessRecordFromIAware(temp, restartservice, isAsynchronous, reason, needCheckAdj)) {
            return false;
        }
        this.mProcInfoCollector.recordKilledProcess(temp);
        long end = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "[aware_mem] process cleaner kill process: pid is " + pid + ",last time :" + (end - start));
        }
        return true;
    }

    private boolean killProcessFast(int pid, boolean restartservice, boolean isAsynchronous, String reason, boolean isNative, boolean needCheckAdj) {
        int i = pid;
        ProcessInfo proc = this.mProcInfoCollector.getProcessInfo(i);
        if (proc == null) {
            AwareLog.e(TAG, "[aware_mem] fast kill process: process info is null ");
            return false;
        }
        if (isNative) {
            if (!HwActivityManager.killNativeProcessRecordFast(proc.mProcessName, proc.mPid, proc.mUid, restartservice, isAsynchronous, reason)) {
                return false;
            }
        } else if (!HwActivityManager.killProcessRecordFast(proc.mProcessName, proc.mPid, proc.mUid, restartservice, isAsynchronous, reason, needCheckAdj)) {
            return false;
        }
        this.mProcInfoCollector.recordKilledProcess(proc);
        AwareLog.d(TAG, "[aware_mem] fast kill proc: " + proc.mProcessName + ", pid: " + i + ", restart: " + restartservice);
        return true;
    }

    public void setProtectedListFromMDM(List<String> protectedList) {
        if (protectedList == null) {
            Slog.e(TAG, "[aware_mem] Set MDM protected list error");
            return;
        }
        ArrayList<String> tempList = new ArrayList<>();
        if (protectedList.size() < 3) {
            tempList.addAll(protectedList);
        } else {
            for (int i = 0; i < 3; i++) {
                tempList.add(protectedList.get(i));
            }
            Slog.d(TAG, "[aware_mem] Only 3 apps will be protected from MDM." + tempList.toString());
        }
        synchronized (this.mMDMProtectedList) {
            this.mMDMProtectedList.clear();
            this.mMDMProtectedList.addAll(tempList);
        }
    }

    public void removeProtectedListFromMDM() {
        synchronized (this.mMDMProtectedList) {
            this.mMDMProtectedList.clear();
        }
        Slog.d(TAG, "[aware_mem] Remove MDM protected list");
    }

    public ArrayList<String> getProtectedListFromMDM() {
        ArrayList<String> tempList = new ArrayList<>();
        synchronized (this.mMDMProtectedList) {
            tempList.addAll(this.mMDMProtectedList);
        }
        return tempList;
    }

    private boolean checkPkgInProtectedListFromMDM(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        synchronized (this.mMDMProtectedList) {
            if (this.mMDMProtectedList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void cleanPackageNotifications(List<String> packageList, int targetUid) {
        if (packageList != null) {
            INotificationManager service = NotificationManager.getService();
            if (service != null) {
                int userId = UserHandle.getUserId(targetUid);
                try {
                    Slog.v(TAG, "cleanupPackageNotifications, userId=" + userId + "|" + packageList);
                    for (String packageName : packageList) {
                        service.cancelAllNotifications(packageName, userId);
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to talk to notification manager. Woe!");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void cleanNotificationWithPid(List<String> packageList, int targetUid, int pid) {
        if (packageList != null) {
            INotificationManager service = NotificationManager.getService();
            if (service != null) {
                try {
                    StatusBarNotification[] notifications = service.getActiveNotifications("android");
                    int userId = UserHandle.getUserId(targetUid);
                    if (notifications != null) {
                        for (StatusBarNotification notification : notifications) {
                            if (notification.getInitialPid() == pid) {
                                for (String packageName : packageList) {
                                    service.cancelNotificationWithTag(packageName, notification.getTag(), notification.getId(), userId);
                                }
                            }
                        }
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to talk to notification manager. Woe!");
                }
            }
        }
    }

    private boolean hasNotification(int pid) {
        if (pid < 0) {
            return false;
        }
        INotificationManager service = NotificationManager.getService();
        if (service == null) {
            return false;
        }
        try {
            StatusBarNotification[] notifications = service.getActiveNotifications("android");
            if (notifications == null) {
                return false;
            }
            for (StatusBarNotification notification : notifications) {
                if (notification.getInitialPid() == pid) {
                    return true;
                }
            }
            return false;
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to talk to notification manager. Woe!");
        }
    }

    public boolean isProcessFastKillLocked(String procName, int uid) {
        boolean z;
        synchronized (this.mProcCleanMap) {
            z = ((ProcessFastKillInfo) this.mProcCleanMap.get(procName, uid)) != null;
        }
        return z;
    }

    private final void addProcessFastKillLocked(ProcessFastKillInfo app, String procName, int uid) {
        if (app != null) {
            synchronized (this.mProcCleanMap) {
                this.mProcCleanMap.put(procName, uid, app);
            }
        }
    }

    /* access modifiers changed from: private */
    public final void removeProcessFastKillLocked(String procName, int uid) {
        synchronized (this.mProcCleanMap) {
            this.mProcCleanMap.remove(procName, uid);
        }
    }
}
