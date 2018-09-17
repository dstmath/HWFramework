package com.android.server.mtm.taskstatus;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareConstant;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.appmng.AppMngDumpRadar;
import com.android.server.rms.iaware.appmng.AwareAppMngDFX;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessCleaner {
    private static final int CLEAN_PID_NOTIFICATION = 2;
    private static final int CLEAN_UID_NOTIFICATION = 1;
    private static final boolean DEBUG = false;
    private static final String TAG = "ProcessCleaner";
    private static ProcessCleaner mProcessCleaner;
    private ActivityManager mActivityManager;
    Handler mHandler;
    private HwActivityManagerService mHwAMS;
    private ProcessInfoCollector mProcInfoCollector;

    /* renamed from: com.android.server.mtm.taskstatus.ProcessCleaner.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ProcessCleaner.CLEAN_UID_NOTIFICATION /*1*/:
                    if (ProcessCleaner.this.mHwAMS != null) {
                        ProcessCleaner.this.mHwAMS.cleanPackageNotifications((List) msg.obj, msg.arg1);
                    }
                case ProcessCleaner.CLEAN_PID_NOTIFICATION /*2*/:
                    if (ProcessCleaner.this.mHwAMS != null) {
                        ProcessCleaner.this.mHwAMS.cleanNotificationWithPid((List) msg.obj, msg.arg1, msg.arg2);
                    }
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.taskstatus.ProcessCleaner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.taskstatus.ProcessCleaner.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.taskstatus.ProcessCleaner.<clinit>():void");
    }

    private ProcessCleaner(Context context) {
        this.mHwAMS = null;
        this.mProcInfoCollector = null;
        this.mActivityManager = null;
        this.mHandler = new AnonymousClass1(Looper.getMainLooper());
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

    public boolean killProcess(int pid, boolean restartservice) {
        long start = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "process cleaner kill process: pid is " + pid + ", restart service :" + restartservice);
        }
        ProcessInfo temp = this.mProcInfoCollector.getProcessInfo(pid);
        if (temp == null) {
            Slog.e(TAG, "process cleaner kill process: process info is null ");
            return DEBUG;
        } else if (this.mHwAMS == null) {
            Slog.e(TAG, "process cleaner kill process: mHwAMS is null ");
            return DEBUG;
        } else if (this.mHwAMS.killProcessRecordFromMTM(temp, restartservice)) {
            this.mProcInfoCollector.recordKilledProcess(temp);
            long end = SystemClock.elapsedRealtime();
            if (this.mProcInfoCollector.INFO) {
                Slog.d(TAG, "process cleaner kill process: pid is " + pid + ",last time :" + (end - start));
            }
            return true;
        } else {
            Slog.e(TAG, "process cleaner kill process: failed to kill ");
            return DEBUG;
        }
    }

    public List<Integer> killProcessesSameUidExt(AwareProcessBlockInfo procGroup, boolean quickKillAction) {
        return killProcessesSameUidExt(procGroup, null, true, quickKillAction);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<Integer> killProcessesSameUidExt(AwareProcessBlockInfo procGroup, AtomicBoolean interrupt, boolean isAsynchronous, boolean quickKillAction) {
        Throwable th;
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
        List<Integer> killList = new ArrayList();
        List<String> packageList = getPackageList(procInfoAllStopList);
        List<AwareProcessInfo> pidsWithNotification = getPidsWithNotification(procInfoAllStopList);
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "[aware_mem] start process cleaner kill process start");
        }
        AwareAppMngSort appMngSort = AwareAppMngSort.getInstance();
        if (appMngSort == null || !appMngSort.isProcessBlockPidChanged(procGroup)) {
            boolean isCleanAllRes = resCleanAllow;
            boolean isCleanUidActivity = DEBUG;
            boolean needCheckAlarm = appMngSort != null ? appMngSort.needCheckAlarm(procGroup) : true;
            synchronized (this.mHwAMS) {
                Message msg;
                if (resCleanAllow) {
                    try {
                        Slog.d(TAG, "[aware_mem] start process cleaner setPackageStoppedState");
                        this.mHwAMS.setPackageStoppedState(packageList, true);
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
                ArraySet<Integer> pidCantStop = new ArraySet();
                for (AwareProcessInfo info : procInfoAllStopList) {
                    if (!(appMngSort == null || info == null || info.mProcInfo == null || !appMngSort.isGroupBeHigher(info.mPid, info.mProcInfo.mUid, info.mProcInfo.mProcessName, info.mProcInfo.mPackageName, info.mMemGroup))) {
                        pidCantStop.add(Integer.valueOf(info.mPid));
                    }
                }
                List<AwareProcessInfo> dfxDataList = null;
                for (AwareProcessInfo info2 : procInfoAllStopList) {
                    List<AwareProcessInfo> dfxDataList2;
                    try {
                        if (interrupt != null && interrupt.get()) {
                            isCleanAllRes = DEBUG;
                            break;
                        }
                        boolean killResult = DEBUG;
                        if (!pidCantStop.contains(Integer.valueOf(info2.mPid))) {
                            killResult = killProcessSameUid(info2.mPid, info2.getRestartFlag(), isAsynchronous);
                        }
                        if (killResult) {
                            killList.add(Integer.valueOf(info2.mPid));
                            if (!(resCleanAllow || info2.mProcInfo == null || !pidsWithNotification.contains(info2) || info2.mRestartFlag)) {
                                msg = this.mHandler.obtainMessage(CLEAN_PID_NOTIFICATION);
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
                                if (dfxDataList == null) {
                                    dfxDataList2 = new ArrayList();
                                } else {
                                    dfxDataList2 = dfxDataList;
                                }
                                dfxDataList2.add(info2);
                            } else {
                                dfxDataList2 = dfxDataList;
                            }
                        } else {
                            isCleanAllRes = DEBUG;
                            dfxDataList2 = dfxDataList;
                        }
                        if (info2.mProcInfo != null) {
                            String str;
                            String killHint = killResult ? "success " : "fail ";
                            String str2 = TAG;
                            StringBuilder append = new StringBuilder().append("[aware_mem] process cleaner ").append(killHint).append("pid:").append(info2.mPid).append(",uid:").append(info2.mProcInfo.mUid).append(",").append(info2.mProcInfo.mProcessName).append(",").append(info2.mProcInfo.mPackageName).append(",mHasShownUi:").append(info2.mHasShownUi).append(",").append(procGroup.mSubTypeStr).append(",class:").append(procGroup.mClassRate).append(",").append(procGroup.mSubClassRate).append(",").append(info2.mClassRate).append(",").append(info2.mSubClassRate).append(",adj:").append(info2.mProcInfo.mCurAdj);
                            if (killResult) {
                                str = " is killed";
                            } else {
                                str = AppHibernateCst.INVALID_PKG;
                            }
                            Slog.d(str2, append.append(str).toString());
                        }
                        AppMngDumpRadar.getInstance().insertStatisticData(killResult ? "Kill-Success" : "Kill-Failed", 0, 0);
                        AppMngDumpRadar.getInstance().insertDumpData(System.currentTimeMillis(), "AMng-Kill", 0, info2.getStatisticsInfo());
                        dfxDataList = dfxDataList2;
                    } catch (Throwable th3) {
                        th = th3;
                        dfxDataList2 = dfxDataList;
                    }
                }
                AwareAppMngDFX.getInstance().trackeKillInfo(dfxDataList, isCleanAllRes, quickKillAction);
                if (isCleanAllRes) {
                    boolean isAlarmFlag = DEBUG;
                    if (needCheckAlarm) {
                        isAlarmFlag = this.mHwAMS.isPkgHasAlarm(packageList, targetUid);
                    }
                    if (isAlarmFlag) {
                        Slog.d(TAG, "[aware_mem] is alarm " + packageList);
                        this.mHwAMS.setPackageStoppedState(packageList, DEBUG);
                    } else {
                        Slog.d(TAG, "[aware_mem] start process cleaner cleanPackageRes, clnAlarm:" + procGroup.mCleanAlarm);
                        this.mHwAMS.cleanPackageRes(packageList, targetUid, procGroup.mCleanAlarm);
                    }
                } else if (resCleanAllow) {
                    Slog.d(TAG, "[aware_mem] start process cleaner reset PackageStoppedState");
                    this.mHwAMS.setPackageStoppedState(packageList, DEBUG);
                }
                if (isCleanUidActivity) {
                    Slog.d(TAG, "[aware_mem] clean uid activity:" + targetUid);
                    this.mHwAMS.cleanActivityByUid(packageList, targetUid);
                }
                if (isCleanAllRes) {
                    msg = this.mHandler.obtainMessage(CLEAN_UID_NOTIFICATION);
                    msg.obj = packageList;
                    msg.arg1 = targetUid;
                    this.mHandler.sendMessageDelayed(msg, 200);
                }
                if (this.mProcInfoCollector.INFO) {
                    Slog.d(TAG, "[aware_mem] process cleaner kill pids:" + killList.toString());
                }
                if (killList.size() <= 0) {
                    killList = null;
                }
                return killList;
            }
        }
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "[aware_mem] new process has started in block, uid: " + targetUid);
        }
        return null;
    }

    public boolean forcestopApps(int pid) {
        long start = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "forcestopApps kill process: pid is " + pid);
        }
        ProcessInfo temp = this.mProcInfoCollector.getProcessInfo(pid);
        if (temp == null) {
            Slog.e(TAG, "forcestopApps kill process: process info is null ");
            return DEBUG;
        } else if (temp.mCurSchedGroup != 0) {
            Slog.e(TAG, "forcestopApps kill process: process " + temp.mProcessName + " is not in BG");
            return DEBUG;
        } else {
            String packagename = (String) temp.mPackageName.get(0);
            if (packagename == null || packagename.equals(" ")) {
                Slog.e(TAG, "forcestopApps kill process: packagename == null");
                return DEBUG;
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
                return DEBUG;
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

    public boolean killProcessSameUid(int pid, boolean restartservice, boolean isAsynchronous) {
        long start = SystemClock.elapsedRealtime();
        if (this.mProcInfoCollector.INFO) {
            Slog.d(TAG, "[aware_mem] process cleaner kill process: pid is " + pid + ", restart service :" + restartservice);
        }
        ProcessInfo temp = this.mProcInfoCollector.getProcessInfo(pid);
        if (temp == null) {
            Slog.e(TAG, "[aware_mem] process cleaner kill process: process info is null ");
            return DEBUG;
        } else if (this.mHwAMS == null) {
            Slog.e(TAG, "[aware_mem] process cleaner kill process: mHwAMS is null ");
            return DEBUG;
        } else if (!this.mHwAMS.killProcessRecordFromIAware(temp, restartservice, isAsynchronous)) {
            return DEBUG;
        } else {
            this.mProcInfoCollector.recordKilledProcess(temp);
            long end = SystemClock.elapsedRealtime();
            if (this.mProcInfoCollector.INFO) {
                Slog.d(TAG, "[aware_mem] process cleaner kill process: pid is " + pid + ",last time :" + (end - start));
            }
            return true;
        }
    }
}
