package com.android.server.mtm.taskstatus;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.huawei.android.app.HwActivityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessCleaner {
    private static final /* synthetic */ int[] -com-android-server-mtm-taskstatus-ProcessCleaner$CleanTypeSwitchesValues = null;
    private static final int CLEAN_PID_NOTIFICATION = 2;
    private static final int CLEAN_UID_NOTIFICATION = 1;
    private static final int PROTECTED_APP_NUM_FROM_MDM = 3;
    private static final String TAG = "ProcessCleaner";
    private static ProcessCleaner mProcessCleaner = null;
    private ActivityManager mActivityManager;
    Handler mHandler;
    private HwActivityManagerService mHwAMS;
    private ArrayList<String> mMDMProtectedList;
    private ProcessInfoCollector mProcInfoCollector;

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
        FORCESTOP_ALARM("force-stop-alarm");
        
        String mDescription;

        private CleanType(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private static /* synthetic */ int[] -getcom-android-server-mtm-taskstatus-ProcessCleaner$CleanTypeSwitchesValues() {
        if (-com-android-server-mtm-taskstatus-ProcessCleaner$CleanTypeSwitchesValues != null) {
            return -com-android-server-mtm-taskstatus-ProcessCleaner$CleanTypeSwitchesValues;
        }
        int[] iArr = new int[CleanType.values().length];
        try {
            iArr[CleanType.COMPACT.ordinal()] = 7;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CleanType.FORCESTOP.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CleanType.FORCESTOP_ALARM.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CleanType.FORCESTOP_REMOVETASK.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CleanType.FREEZE_NOMAL.ordinal()] = 8;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CleanType.FREEZE_UP_DOWNLOAD.ordinal()] = 9;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CleanType.IOLIMIT.ordinal()] = 10;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CleanType.KILL_ALLOW_START.ordinal()] = 4;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[CleanType.KILL_DELAY_START.ordinal()] = 11;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[CleanType.KILL_FORBID_START.ordinal()] = 5;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[CleanType.NONE.ordinal()] = 12;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[CleanType.REMOVETASK.ordinal()] = 6;
        } catch (NoSuchFieldError e12) {
        }
        -com-android-server-mtm-taskstatus-ProcessCleaner$CleanTypeSwitchesValues = iArr;
        return iArr;
    }

    private ProcessCleaner(Context context) {
        this.mHwAMS = null;
        this.mProcInfoCollector = null;
        this.mActivityManager = null;
        this.mMDMProtectedList = new ArrayList();
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (ProcessCleaner.this.mHwAMS != null) {
                            ProcessCleaner.this.mHwAMS.cleanPackageNotifications((List) msg.obj, msg.arg1);
                            return;
                        }
                        return;
                    case 2:
                        if (ProcessCleaner.this.mHwAMS != null) {
                            ProcessCleaner.this.mHwAMS.cleanNotificationWithPid((List) msg.obj, msg.arg1, msg.arg2);
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mProcInfoCollector = ProcessInfoCollector.getInstance();
        this.mHwAMS = HwActivityManagerService.self();
        if (this.mHwAMS == null) {
            Slog.e(TAG, "init failed to get HwAMS handler");
        }
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
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

    public int uniformClean(AwareProcessBlockInfo procGroup, Bundle extras, String reason) {
        int killedCount = 0;
        if (procGroup == null) {
            return 0;
        }
        if (!(procGroup.mProcessList == null || (procGroup.mProcessList.isEmpty() ^ 1) == 0)) {
            List<Integer> killedPid;
            switch (-getcom-android-server-mtm-taskstatus-ProcessCleaner$CleanTypeSwitchesValues()[procGroup.mCleanType.ordinal()]) {
                case 1:
                case 2:
                    killedPid = killProcessesSameUidExt(procGroup, null, false, false, reason);
                    if (killedPid != null) {
                        killedCount = killedPid.size() + 0;
                        break;
                    }
                    break;
                case 3:
                    killedPid = killProcessesSameUidExt(procGroup, null, false, false, reason);
                    if (killedPid != null) {
                        killedCount = killedPid.size() + 0;
                    }
                    removetask(procGroup);
                    break;
                case 4:
                case 5:
                    for (AwareProcessInfo awareProc : procGroup.mProcessList) {
                        boolean z;
                        int i = awareProc.mPid;
                        if (procGroup.mCleanType == CleanType.KILL_ALLOW_START) {
                            z = true;
                        } else {
                            z = false;
                        }
                        if (killProcess(i, z, reason)) {
                            killedCount++;
                        }
                    }
                    break;
                case 6:
                    int result = removetask(procGroup);
                    if (result > 0) {
                        killedCount = result + 0;
                        break;
                    }
                    break;
            }
        }
        return killedCount;
    }

    private Map<String, List<String>> getAlarmTags(int uid, List<String> packageList) {
        if (packageList == null || packageList.isEmpty()) {
            return null;
        }
        Map<String, List<String>> tags = new ArrayMap();
        boolean clearAll = true;
        for (String pkg : packageList) {
            List<String> list = AwareIntelligentRecg.getInstance().getAllInvalidAlarmTags(uid, pkg);
            if (list != null) {
                clearAll = false;
                tags.put(pkg, list);
            }
        }
        if (clearAll) {
            tags = null;
        }
        return tags;
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
        } else if (this.mHwAMS.killProcessRecordFromMTM(temp, restartservice, reason)) {
            this.mProcInfoCollector.recordKilledProcess(temp);
            long end = SystemClock.elapsedRealtime();
            if (this.mProcInfoCollector.INFO) {
                Slog.d(TAG, "process cleaner kill process: pid is " + pid + ",last time :" + (end - start));
            }
            return true;
        } else {
            Slog.e(TAG, "process cleaner kill process: failed to kill ");
            return false;
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
            HashSet<Integer> taskIdSet = new HashSet();
            boolean success = false;
            for (AwareProcessInfo awareProc : procGroup.mProcessList) {
                if (awareProc != null) {
                    taskIdSet.add(Integer.valueOf(awareProc.mTaskId));
                }
            }
            for (Integer taskId : taskIdSet) {
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

    public List<Integer> killProcessesSameUidExt(AwareProcessBlockInfo procGroup, boolean quickKillAction, String reason) {
        return killProcessesSameUidExt(procGroup, null, true, quickKillAction, reason);
    }

    /* JADX WARNING: Missing block: B:77:0x01e1, code:
            if (r19 == false) goto L_0x0203;
     */
    /* JADX WARNING: Missing block: B:78:0x01e3, code:
            r24 = r33.mHandler.obtainMessage(1);
            r24.obj = r26;
            r24.arg1 = r31;
            r33.mHandler.sendMessageDelayed(r24, 200);
     */
    /* JADX WARNING: Missing block: B:80:0x0209, code:
            if (r33.mProcInfoCollector.INFO == false) goto L_0x0229;
     */
    /* JADX WARNING: Missing block: B:81:0x020b, code:
            android.util.Slog.d(TAG, "[aware_mem] process cleaner kill pids:" + r22.toString());
     */
    /* JADX WARNING: Missing block: B:83:0x022c, code:
            if (android.rms.iaware.AwareConstant.CURRENT_USER_TYPE != 3) goto L_0x0239;
     */
    /* JADX WARNING: Missing block: B:84:0x022e, code:
            com.android.server.rms.iaware.appmng.AwareAppMngDFX.getInstance().trackeKillInfo(r14, r19, r37);
     */
    /* JADX WARNING: Missing block: B:86:0x023d, code:
            if (r22.size() <= 0) goto L_0x048d;
     */
    /* JADX WARNING: Missing block: B:87:0x023f, code:
            return r22;
     */
    /* JADX WARNING: Missing block: B:141:0x048d, code:
            r22 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<Integer> killProcessesSameUidExt(AwareProcessBlockInfo procGroup, AtomicBoolean interrupt, boolean isAsynchronous, boolean quickKillAction, String reason) {
        Throwable th;
        if (procGroup == null) {
            return null;
        }
        int targetUid = procGroup.mUid;
        boolean resCleanAllow = procGroup.mResCleanAllow;
        List<AwareProcessInfo> procInfoAllStopList = procGroup.getProcessList();
        if (targetUid == 0 || procInfoAllStopList == null) {
            return null;
        }
        if (this.mHwAMS == null) {
            Slog.e(TAG, "[aware_mem] Why mHwAMS is null!!");
            return null;
        }
        if (checkPkgInProtectedListFromMDM(procGroup.mPackageName)) {
            Slog.d(TAG, "[aware_mem] " + procGroup.mPackageName + " protected by MDM");
            return null;
        }
        List<Integer> killList = new ArrayList();
        List<AwareProcessInfo> dfxDataList = null;
        List<String> packageList = getPackageList(procInfoAllStopList);
        List<AwareProcessInfo> pidsWithNotification = getPidsWithNotification(procInfoAllStopList);
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "[aware_mem] start process cleaner kill process start");
        }
        AwareAppMngSort appMngSort = AwareAppMngSort.getInstance();
        if (procGroup.mIsNativeForceStop || appMngSort == null || !appMngSort.isProcessBlockPidChanged(procGroup)) {
            boolean isCleanAllRes = resCleanAllow;
            boolean isCleanUidActivity = false;
            boolean needCheckAlarm = appMngSort != null ? appMngSort.needCheckAlarm(procGroup) : true;
            Map<String, List<String>> alarmTagMap = null;
            if (!procGroup.mIsNativeForceStop) {
                alarmTagMap = getAlarmTags(targetUid, packageList);
            }
            boolean hasPerceptAlarm = AwareIntelligentRecg.getInstance().hasPerceptAlarm(targetUid, packageList);
            synchronized (this.mHwAMS) {
                Iterator info$iterator;
                AwareProcessInfo info;
                if (resCleanAllow) {
                    Slog.d(TAG, "[aware_mem] start process cleaner setPackageStoppedState");
                    this.mHwAMS.setPackageStoppedState(packageList, true, targetUid);
                }
                ArraySet<Integer> pidCantStop = new ArraySet();
                if (!procGroup.mIsNativeForceStop) {
                    for (AwareProcessInfo info2 : procInfoAllStopList) {
                        if (!(appMngSort == null || info2 == null || info2.mProcInfo == null || !appMngSort.isGroupBeHigher(info2.mPid, info2.mProcInfo.mUid, info2.mProcInfo.mProcessName, info2.mProcInfo.mPackageName, info2.mMemGroup))) {
                            pidCantStop.add(Integer.valueOf(info2.mPid));
                        }
                    }
                }
                try {
                    info$iterator = procInfoAllStopList.iterator();
                    while (true) {
                        List<AwareProcessInfo> dfxDataList2;
                        try {
                            dfxDataList2 = dfxDataList;
                            if (!info$iterator.hasNext()) {
                                break;
                            }
                            info2 = (AwareProcessInfo) info$iterator.next();
                            if (interrupt != null && interrupt.get()) {
                                isCleanAllRes = false;
                                break;
                            }
                            boolean killResult = false;
                            if (procGroup.mIsNativeForceStop) {
                                killProcessSameUid(info2.mPid, info2.getRestartFlag(), isAsynchronous, reason, true);
                                killResult = true;
                            } else {
                                if (!pidCantStop.contains(Integer.valueOf(info2.mPid))) {
                                    killResult = killProcessSameUid(info2.mPid, info2.getRestartFlag(), isAsynchronous, reason, false);
                                }
                            }
                            if (killResult) {
                                killList.add(Integer.valueOf(info2.mPid));
                                if (!(resCleanAllow || info2.mProcInfo == null || !pidsWithNotification.contains(info2) || info2.mRestartFlag)) {
                                    Message msg = this.mHandler.obtainMessage(2);
                                    msg.obj = packageList;
                                    msg.arg1 = targetUid;
                                    msg.arg2 = info2.mPid;
                                    this.mHandler.sendMessageDelayed(msg, 200);
                                    Slog.d(TAG, "[aware_mem] clean notification " + info2.mProcInfo.mProcessName);
                                }
                                if (!resCleanAllow && info2.mHasShownUi && this.mHwAMS.numOfPidWithActivity(targetUid) == 0) {
                                    isCleanUidActivity = true;
                                }
                                if (AwareConstant.CURRENT_USER_TYPE == 3) {
                                    if (dfxDataList2 == null) {
                                        dfxDataList = new ArrayList();
                                    } else {
                                        dfxDataList = dfxDataList2;
                                    }
                                    dfxDataList.add(info2);
                                } else {
                                    dfxDataList = dfxDataList2;
                                }
                            } else {
                                isCleanAllRes = false;
                                dfxDataList = dfxDataList2;
                            }
                            if (info2.mProcInfo != null) {
                                String str;
                                String killHint = killResult ? "success " : "fail ";
                                String str2 = TAG;
                                StringBuilder append = new StringBuilder().append("[aware_mem] process cleaner ").append(killHint).append("pid:").append(info2.mPid).append(",uid:").append(info2.mProcInfo.mUid).append(",").append(info2.mProcInfo.mProcessName).append(",").append(info2.mProcInfo.mPackageName).append(",mHasShownUi:").append(info2.mHasShownUi).append(",").append(procGroup.mSubTypeStr).append(",class:").append(procGroup.mClassRate).append(",").append(procGroup.mSubClassRate).append(",").append(info2.mClassRate).append(",").append(info2.mSubClassRate).append(",adj:").append(info2.mProcInfo.mCurAdj);
                                if (killResult) {
                                    str = " is killed";
                                } else {
                                    str = "";
                                }
                                Slog.d(str2, append.append(str).toString());
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            dfxDataList = dfxDataList2;
                            throw th;
                        }
                    }
                    if (isCleanAllRes) {
                        boolean isAlarmFlag = false;
                        if (needCheckAlarm) {
                            isAlarmFlag = this.mHwAMS.isPkgHasAlarm(packageList, targetUid);
                        }
                        if (isAlarmFlag) {
                            Slog.d(TAG, "[aware_mem] is alarm " + packageList);
                            this.mHwAMS.setPackageStoppedState(packageList, false, targetUid);
                        } else {
                            Slog.d(TAG, "[aware_mem] start process cleaner cleanPackageRes, clnAlarm:" + procGroup.mCleanAlarm + ", hasPerceptAlarm:" + hasPerceptAlarm + ", isNative:" + procGroup.mIsNativeForceStop + ", cleanResult: " + HwActivityManager.cleanPackageRes(packageList, alarmTagMap, targetUid, procGroup.mCleanAlarm, procGroup.mIsNativeForceStop, hasPerceptAlarm));
                            if (!procGroup.mIsNativeForceStop && hasPerceptAlarm) {
                                this.mHwAMS.setPackageStoppedState(packageList, false, targetUid);
                            }
                        }
                    } else if (resCleanAllow) {
                        Slog.d(TAG, "[aware_mem] start process cleaner reset PackageStoppedState");
                        this.mHwAMS.setPackageStoppedState(packageList, false, targetUid);
                    }
                    if (isCleanUidActivity) {
                        Slog.d(TAG, "[aware_mem] clean uid activity:" + targetUid);
                        this.mHwAMS.cleanActivityByUid(packageList, targetUid);
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            }
        } else {
            if (this.mProcInfoCollector.INFO) {
                Slog.d(TAG, "[aware_mem] new process has started in block, uid: " + targetUid);
            }
            return null;
        }
    }

    public boolean forcestopAppsAsUser(AwareProcessInfo awareProc) {
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
        List<String> packageList = new ArrayList();
        for (AwareProcessInfo curPIAllStop : procInfoAllStopList) {
            if (!(curPIAllStop.mProcInfo == null || curPIAllStop.mProcInfo.mPackageName == null)) {
                for (String packageName : curPIAllStop.mProcInfo.mPackageName) {
                    if (!packageList.contains(packageName)) {
                        packageList.add(packageName);
                    }
                }
            }
        }
        return packageList;
    }

    private List<AwareProcessInfo> getPidsWithNotification(List<AwareProcessInfo> procInfoAllStopList) {
        List<AwareProcessInfo> pidsWithNotification = new ArrayList();
        for (AwareProcessInfo info : procInfoAllStopList) {
            if (this.mHwAMS.hasNotification(info.mPid)) {
                pidsWithNotification.add(info);
            }
        }
        return pidsWithNotification;
    }

    public boolean killProcessSameUid(int pid, boolean restartservice, boolean isAsynchronous, String reason, boolean isNative) {
        long start = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "[aware_mem] process cleaner kill process: pid is " + pid + ", restart service :" + restartservice);
        }
        ProcessInfo temp = this.mProcInfoCollector.getProcessInfo(pid);
        if (temp == null) {
            Slog.e(TAG, "[aware_mem] process cleaner kill process: process info is null ");
            return false;
        } else if (this.mHwAMS == null) {
            Slog.e(TAG, "[aware_mem] process cleaner kill process: mHwAMS is null ");
            return false;
        } else {
            if (isNative) {
                if (!this.mHwAMS.killProcessRecordFromIAwareNative(temp, restartservice, isAsynchronous, reason)) {
                    return false;
                }
            } else if (!this.mHwAMS.killProcessRecordFromIAware(temp, restartservice, isAsynchronous, reason)) {
                return false;
            }
            this.mProcInfoCollector.recordKilledProcess(temp);
            long end = SystemClock.elapsedRealtime();
            if (this.mProcInfoCollector.INFO) {
                Slog.d(TAG, "[aware_mem] process cleaner kill process: pid is " + pid + ",last time :" + (end - start));
            }
            return true;
        }
    }

    public void setProtectedListFromMDM(List<String> protectedList) {
        if (protectedList == null) {
            Slog.e(TAG, "[aware_mem] Set MDM protected list error");
            return;
        }
        ArrayList<String> tempList = new ArrayList();
        if (protectedList.size() < 3) {
            tempList.addAll(protectedList);
        } else {
            for (int i = 0; i < 3; i++) {
                tempList.add((String) protectedList.get(i));
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
        ArrayList<String> tempList = new ArrayList();
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
}
