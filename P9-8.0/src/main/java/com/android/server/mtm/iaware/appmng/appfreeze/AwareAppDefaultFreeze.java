package com.android.server.mtm.iaware.appmng.appfreeze;

import android.app.mtm.iaware.appmng.AppMngConstant.AppFreezeSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.content.Context;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.internal.os.KernelUidCpuTimeReader;
import com.android.internal.os.KernelUidCpuTimeReader.Callback;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort.ClassRate;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.SystemUnremoveUidCache;
import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMServiceImpl;
import com.huawei.pgmng.plug.AppInfo;
import com.huawei.pgmng.plug.PGSdk;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AwareAppDefaultFreeze extends CleanSource {
    private static int DEFAULT_CAPACITY = 10;
    private static final String FREEZE_BAD = "app_freeze_bad";
    private static final long INTERVAL_TIME = 60000;
    private static final int MAX_PID_CNT = 20;
    private static final String REASON_DUMP_FREEZE = "Only for dump test";
    private static final String TAG = "mtm.AwareAppDefaultFreeze";
    private static AwareAppDefaultFreeze mAwareAppDefaultFreeze = null;
    private static List<CleanType> mPriority;
    private Context mContext;
    private ArrayList<AppInfo> mFreezeAppInfos = new ArrayList();
    private final KernelUidCpuTimeReader mKernelUidCpuTimeReader = new KernelUidCpuTimeReader();
    private long mLastSampleTime = SystemClock.uptimeMillis();
    private Set<String> mSysUnremoveBadPkgs = new ArraySet();
    private SystemUnremoveUidCache mSystemUnremoveUidCache;
    private UidCpuTimeReaderCallback mUidCpuTimeReaderCallback = new UidCpuTimeReaderCallback(this, null);
    private UidLoadComparator mUidLoadComparator = new UidLoadComparator();
    private SparseArray<UidLoadPair> mUidLoadMap = new SparseArray();

    private static class ProcessInfoComparator implements Comparator<ProcessInfo>, Serializable {
        private static final long serialVersionUID = 1;

        /* synthetic */ ProcessInfoComparator(ProcessInfoComparator -this0) {
            this();
        }

        private ProcessInfoComparator() {
        }

        public int compare(ProcessInfo arg0, ProcessInfo arg1) {
            int i = -1;
            if (arg0 == null) {
                return arg1 == null ? 0 : -1;
            } else {
                if (arg1 == null) {
                    return 1;
                }
                if (arg0.mUid >= arg1.mUid) {
                    i = arg0.mUid == arg1.mUid ? 0 : 1;
                }
                return i;
            }
        }
    }

    private class UidCpuTimeReaderCallback implements Callback {
        /* synthetic */ UidCpuTimeReaderCallback(AwareAppDefaultFreeze this$0, UidCpuTimeReaderCallback -this1) {
            this();
        }

        private UidCpuTimeReaderCallback() {
        }

        public void onUidCpuTime(int uid, long userTimeUs, long systemTimeUs) {
            UidLoadPair pair = (UidLoadPair) AwareAppDefaultFreeze.this.mUidLoadMap.get(uid);
            if (pair != null) {
                pair.runningTime = userTimeUs + systemTimeUs;
            }
        }
    }

    private static class UidLoadComparator implements Comparator<UidLoadPair>, Serializable {
        private static final long serialVersionUID = 1;

        /* synthetic */ UidLoadComparator(UidLoadComparator -this0) {
            this();
        }

        private UidLoadComparator() {
        }

        public int compare(UidLoadPair lhs, UidLoadPair rhs) {
            return Long.compare(rhs.runningTime, lhs.runningTime);
        }
    }

    private static class UidLoadPair {
        public AppInfo info;
        public long runningTime = 0;

        public UidLoadPair(AppInfo info) {
            this.info = info;
        }
    }

    public static synchronized AwareAppDefaultFreeze getInstance() {
        AwareAppDefaultFreeze awareAppDefaultFreeze;
        synchronized (AwareAppDefaultFreeze.class) {
            if (mAwareAppDefaultFreeze == null) {
                mAwareAppDefaultFreeze = new AwareAppDefaultFreeze();
                setPriority();
            }
            awareAppDefaultFreeze = mAwareAppDefaultFreeze;
        }
        return awareAppDefaultFreeze;
    }

    private AwareAppDefaultFreeze() {
    }

    public void init(Context ctx) {
        this.mContext = ctx;
        this.mSystemUnremoveUidCache = SystemUnremoveUidCache.getInstance(ctx);
        initSystemUnremoveBadApp();
    }

    public void deInitDefaultFree() {
        this.mContext = null;
        this.mSysUnremoveBadPkgs.clear();
    }

    private void initSystemUnremoveBadApp() {
        ArrayList<String> badList = DecisionMaker.getInstance().getRawConfig(AppMngFeature.APP_FREEZE.getDesc(), FREEZE_BAD);
        if (badList != null) {
            this.mSysUnremoveBadPkgs.clear();
            this.mSysUnremoveBadPkgs.addAll(badList);
        }
    }

    public void doFrozen(String pkgName, AppFreezeSource config, int duration, String reason) {
        if (pkgName == null) {
            AwareLog.w(TAG, "The input params is invalid");
            return;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos = getAllFreezeApp(config);
        if (awareProcessBlockInfos == null || awareProcessBlockInfos.isEmpty()) {
            AwareLog.w(TAG, "no pid need to freeze");
            return;
        }
        int pidCnt = 0;
        this.mUidLoadMap.clear();
        synchronized (this.mFreezeAppInfos) {
            this.mFreezeAppInfos.clear();
            StringBuffer sb = new StringBuffer();
            for (AwareProcessBlockInfo processInfo : awareProcessBlockInfos) {
                if (processInfo.mCleanType == CleanType.FREEZE_NOMAL || processInfo.mCleanType == CleanType.FREEZE_UP_DOWNLOAD) {
                    if (pkgName.equals(processInfo.mPackageName)) {
                        continue;
                    } else {
                        List<AwareProcessInfo> infos = processInfo.mProcessList;
                        if (!(infos == null || infos.size() == 0)) {
                            AppInfo appInfo = new AppInfo(processInfo.mUid, processInfo.mPackageName);
                            List<Integer> pids = new ArrayList(infos.size());
                            Iterator iterator = infos.iterator();
                            while (iterator.hasNext()) {
                                AwareProcessInfo info = (AwareProcessInfo) iterator.next();
                                if (info.mProcInfo == null) {
                                    iterator.remove();
                                } else {
                                    int pid = info.mPid;
                                    if (checkPidValid(pid, info.mProcInfo.mUid)) {
                                        pids.add(Integer.valueOf(pid));
                                        sb.append(pid).append(',');
                                    } else {
                                        iterator.remove();
                                    }
                                }
                            }
                            pidCnt += infos.size();
                            appInfo.setPids(pids);
                            this.mUidLoadMap.put(processInfo.mUid, new UidLoadPair(appInfo));
                            if (processInfo.mCleanType == CleanType.FREEZE_UP_DOWNLOAD) {
                                this.mFreezeAppInfos.add(0, appInfo);
                            } else {
                                this.mFreezeAppInfos.add(appInfo);
                            }
                        }
                    }
                }
            }
            sortFreezeAppByCPULoad(pidCnt);
            doFrozenInternel(duration, reason, sb);
        }
    }

    private void doFrozenInternel(int duration, String reason, StringBuffer sb) {
        if (this.mFreezeAppInfos.isEmpty()) {
            AwareLog.i(TAG, "no pid need to freeze");
            return;
        }
        try {
            PGSdk pgInstance = PGSdk.getInstance();
            if (pgInstance != null) {
                pgInstance.fastHibernation(this.mContext, this.mFreezeAppInfos, duration, reason);
                AwareLog.i(TAG, "Fast freeze the pid=[" + sb.toString() + "]");
            }
        } catch (RemoteException re) {
            AwareLog.w(TAG, "do Frozen failed because not find the PG" + re.getMessage());
        }
    }

    private List<AwareProcessBlockInfo> getAllFreezeApp(AppFreezeSource config) {
        List<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procList.size() == 0) {
            AwareLog.d(TAG, "no pid exit in mtm");
            return null;
        }
        List<AwareProcessInfo> awareProcList = filterUnRemoveSystemApp(procList);
        if (awareProcList.size() == 0) {
            AwareLog.d(TAG, "no pids exit in mtm without system and system unremove app");
            return null;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos = DecisionMaker.getInstance().decideAll(awareProcList, getLevel(), AppMngFeature.APP_FREEZE, config);
        if (awareProcessBlockInfos != null && awareProcessBlockInfos.size() != 0) {
            return CleanSource.mergeBlock(awareProcessBlockInfos, mPriority);
        }
        AwareLog.w(TAG, "no pid need to freeze");
        return null;
    }

    private static void setPriority() {
        mPriority = new ArrayList();
        mPriority.add(CleanType.FREEZE_NOMAL);
        mPriority.add(CleanType.FREEZE_UP_DOWNLOAD);
        mPriority.add(CleanType.NONE);
    }

    private List<AwareProcessInfo> filterUnRemoveSystemApp(List<ProcessInfo> infos) {
        List<AwareProcessInfo> awareProcList = new ArrayList(DEFAULT_CAPACITY);
        if (infos == null || infos.size() == 0) {
            return awareProcList;
        }
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo procInfo = (ProcessInfo) infos.get(i);
            if (isSystemUnRemoveBadPkg(procInfo) || !isSystemUnRemoveApp(procInfo.mAppUid % LaserTSMServiceImpl.EXCUTE_OTA_RESULT_SUCCESS)) {
                awareProcList.add(new AwareProcessInfo(procInfo.mPid, 0, 0, ClassRate.NORMAL.ordinal(), procInfo));
            }
        }
        return awareProcList;
    }

    private boolean isSystemUnRemoveBadPkg(ProcessInfo procInfo) {
        if (!(this.mSysUnremoveBadPkgs.isEmpty() || procInfo.mPackageName == null || (procInfo.mPackageName.isEmpty() ^ 1) == 0)) {
            for (String pkg : procInfo.mPackageName) {
                if (this.mSysUnremoveBadPkgs.contains(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSystemUnRemoveApp(int uid) {
        if (uid <= 0 || uid >= 10000) {
            return this.mSystemUnremoveUidCache != null && this.mSystemUnremoveUidCache.checkUidExist(uid);
        } else {
            return true;
        }
    }

    private int getLevel() {
        return 0;
    }

    private List<AppInfo> getAppInfoByPkg(String pkg) {
        List<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfosFromPackage(pkg, UserHandle.myUserId());
        if (procList.isEmpty()) {
            AwareLog.d(TAG, "no pid exit in mtm");
            return null;
        }
        Collections.sort(procList, new ProcessInfoComparator());
        List<AppInfo> apps = new ArrayList();
        AppInfo lastApp = null;
        for (ProcessInfo info : procList) {
            AppInfo appInfo = new AppInfo(info.mUid, pkg);
            List<Integer> pids = new ArrayList();
            pids.add(Integer.valueOf(info.mPid));
            appInfo.setPids(pids);
            if (lastApp == null) {
                lastApp = appInfo;
                apps.add(appInfo);
            } else if (lastApp.getUid() != info.mUid) {
                lastApp = appInfo;
                apps.add(appInfo);
            } else if (lastApp.getPids() != null) {
                lastApp.getPids().add(Integer.valueOf(info.mPid));
            } else {
                lastApp.setPids(pids);
            }
        }
        return apps;
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("Lasted freeze app");
            synchronized (this.mFreezeAppInfos) {
                if (!this.mFreezeAppInfos.isEmpty()) {
                    for (AppInfo app : this.mFreezeAppInfos) {
                        StringBuffer sb = new StringBuffer();
                        sb.append("    UID=").append(app.getUid()).append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER).append("PID=");
                        if (app.getPids() != null) {
                            for (Integer intValue : app.getPids()) {
                                sb.append(intValue.intValue()).append(",");
                            }
                        }
                        sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER).append("PKG=").append(app.getPkg());
                        pw.println(sb.toString());
                    }
                }
            }
            pw.println();
            pw.println("Current can freeze app by default policy");
            List<AwareProcessBlockInfo> awareProcessBlockInfos = getAllFreezeApp(AppFreezeSource.FAST_FREEZE);
            if (awareProcessBlockInfos == null || awareProcessBlockInfos.isEmpty()) {
                pw.println("no pid need to freeze");
                return;
            }
            for (AwareProcessBlockInfo processInfo : awareProcessBlockInfos) {
                pw.println("    " + processInfo.toString());
            }
            pw.println();
            pw.println("Current can freeze app by camera policy");
            awareProcessBlockInfos = getAllFreezeApp(AppFreezeSource.CAMERA_FREEZE);
            if (awareProcessBlockInfos == null || awareProcessBlockInfos.isEmpty()) {
                pw.println("no pid need to freeze");
                return;
            }
            for (AwareProcessBlockInfo processInfo2 : awareProcessBlockInfos) {
                pw.println("    " + processInfo2.toString());
            }
        }
    }

    public void dumpFreezeApp(PrintWriter pw, String pkg, int time) {
        if (pw != null) {
            pw.println("Freeze current " + pkg + " app");
            List<AppInfo> apps = getAppInfoByPkg(pkg);
            if (!(apps == null || (apps.isEmpty() ^ 1) == 0)) {
                for (AppInfo app : apps) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("    UID=").append(app.getUid()).append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER).append("PID=");
                    if (app.getPids() != null) {
                        for (Integer intValue : app.getPids()) {
                            sb.append(intValue.intValue()).append(",");
                        }
                    }
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER).append("PKG=").append(app.getPkg());
                    pw.println(sb.toString());
                }
                try {
                    PGSdk.getInstance().fastHibernation(this.mContext, apps, time, REASON_DUMP_FREEZE);
                } catch (RemoteException re) {
                    AwareLog.w(TAG, "do UnFrozen failed because not find the PG" + re.getMessage());
                }
            }
        }
    }

    private void sortFreezeAppByCPULoad(int pidCnt) {
        if (pidCnt < 20 || this.mUidLoadMap.size() == 0) {
            AwareLog.d(TAG, "sortFreezeAppByCPULoad pidCnt =  " + pidCnt + " no need sort!");
            return;
        }
        int uidSize = this.mUidLoadMap.size();
        long nowTime = SystemClock.uptimeMillis();
        long intervalTime = nowTime - this.mLastSampleTime;
        this.mLastSampleTime = nowTime;
        getCpuTimeForUidList();
        if (intervalTime <= 60000) {
            int i;
            List<UidLoadPair> uidLoadArray = new ArrayList();
            for (i = 0; i < uidSize; i++) {
                UidLoadPair uidPair = (UidLoadPair) this.mUidLoadMap.get(this.mUidLoadMap.keyAt(i));
                if (uidPair != null) {
                    uidLoadArray.add(uidPair);
                }
            }
            if (!uidLoadArray.isEmpty()) {
                Collections.sort(uidLoadArray, this.mUidLoadComparator);
                synchronized (this.mFreezeAppInfos) {
                    this.mFreezeAppInfos.clear();
                    int size = uidLoadArray.size();
                    for (i = 0; i < size; i++) {
                        UidLoadPair uidLoad = (UidLoadPair) uidLoadArray.get(i);
                        if (uidLoad != null) {
                            this.mFreezeAppInfos.add(uidLoad.info);
                        }
                    }
                }
            }
        }
    }

    private void getCpuTimeForUidList() {
        this.mKernelUidCpuTimeReader.readDelta(this.mUidCpuTimeReaderCallback);
    }

    private boolean checkPidValid(int pid, int uid) {
        if (!new File("/acct/uid_" + uid + "/pid_" + pid).exists()) {
            try {
                int realUid = ((Integer) Files.getAttribute(Paths.get("/proc/" + pid + "/status/", new String[0]), "unix:uid", new LinkOption[0])).intValue();
                if (realUid != uid) {
                    AwareLog.w(TAG, "read uid " + realUid + " of " + pid + " is not match");
                    return false;
                }
            } catch (IOException e) {
                AwareLog.w(TAG, "read status of " + pid + " failed");
                return false;
            }
        }
        return true;
    }

    public void dumpFreezeBadPid(PrintWriter pw, int pid, int uid) {
        if (pw != null && pid >= 0 && uid >= 0) {
            if (checkPidValid(pid, uid)) {
                pw.println("pid " + pid + " match uid " + uid);
            } else {
                pw.println("pid " + pid + " not match uid " + uid);
            }
        }
    }
}
