package com.android.server.mtm.iaware.appmng.appfreeze;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.internal.os.KernelUidCpuTimeReader;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.SystemUnremoveUidCache;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMService;
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
    private static List<ProcessCleaner.CleanType> mPriority;
    private Context mContext;
    private ArrayList<AppInfo> mFreezeAppInfos = new ArrayList<>();
    private final KernelUidCpuTimeReader mKernelUidCpuTimeReader = new KernelUidCpuTimeReader();
    private long mLastSampleTime = SystemClock.uptimeMillis();
    private Set<String> mSysUnremoveBadPkgs = new ArraySet();
    private SystemUnremoveUidCache mSystemUnremoveUidCache;
    private UidCpuTimeReaderCallback mUidCpuTimeReaderCallback = new UidCpuTimeReaderCallback();
    private UidLoadComparator mUidLoadComparator = new UidLoadComparator();
    /* access modifiers changed from: private */
    public SparseArray<UidLoadPair> mUidLoadMap = new SparseArray<>();

    private static class ProcessInfoComparator implements Comparator<ProcessInfo>, Serializable {
        private static final long serialVersionUID = 1;

        private ProcessInfoComparator() {
        }

        public int compare(ProcessInfo arg0, ProcessInfo arg1) {
            int i = -1;
            if (arg0 == null) {
                return arg1 == null ? 0 : -1;
            }
            if (arg1 == null) {
                return 1;
            }
            if (arg0.mUid >= arg1.mUid) {
                i = arg0.mUid == arg1.mUid ? 0 : 1;
            }
            return i;
        }
    }

    private class UidCpuTimeReaderCallback implements KernelUidCpuTimeReader.Callback {
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

        private UidLoadComparator() {
        }

        public int compare(UidLoadPair lhs, UidLoadPair rhs) {
            return Long.compare(rhs.runningTime, lhs.runningTime);
        }
    }

    private static class UidLoadPair {
        public AppInfo info;
        public long runningTime = 0;

        public UidLoadPair(AppInfo info2) {
            this.info = info2;
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
        ArrayList<String> badList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_FREEZE.getDesc(), FREEZE_BAD);
        if (badList != null) {
            this.mSysUnremoveBadPkgs.clear();
            this.mSysUnremoveBadPkgs.addAll(badList);
        }
    }

    public void doFrozen(String pkgName, AppMngConstant.AppFreezeSource config, int duration, String reason) {
        String str = pkgName;
        if (str == null) {
            AwareLog.w(TAG, "The input params is invalid");
            return;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos = getAllFreezeApp(config);
        if (awareProcessBlockInfos == null || awareProcessBlockInfos.isEmpty()) {
            int i = duration;
            String str2 = reason;
            AwareLog.w(TAG, "no pid need to freeze");
            return;
        }
        int pidCnt = 0;
        this.mUidLoadMap.clear();
        synchronized (this.mFreezeAppInfos) {
            try {
                this.mFreezeAppInfos.clear();
                StringBuffer sb = new StringBuffer();
                for (AwareProcessBlockInfo processInfo : awareProcessBlockInfos) {
                    if (processInfo.mCleanType == ProcessCleaner.CleanType.FREEZE_NOMAL || processInfo.mCleanType == ProcessCleaner.CleanType.FREEZE_UP_DOWNLOAD) {
                        if (!str.equals(processInfo.mPackageName) || UserHandle.getUserId(processInfo.mUid) != AwareAppAssociate.getInstance().getCurUserId()) {
                            List<AwareProcessInfo> infos = processInfo.mProcessList;
                            if (infos != null) {
                                if (infos.size() != 0) {
                                    AppInfo appInfo = new AppInfo(processInfo.mUid, processInfo.mPackageName);
                                    List<Integer> pids = new ArrayList<>(infos.size());
                                    Iterator iterator = infos.iterator();
                                    while (iterator.hasNext()) {
                                        AwareProcessInfo info = iterator.next();
                                        if (info.mProcInfo == null) {
                                            iterator.remove();
                                        } else {
                                            int pid = info.mPid;
                                            if (!checkPidValid(pid, info.mProcInfo.mUid)) {
                                                iterator.remove();
                                            } else {
                                                pids.add(Integer.valueOf(pid));
                                                sb.append(pid);
                                                sb.append(',');
                                            }
                                        }
                                        String str3 = pkgName;
                                    }
                                    pidCnt += infos.size();
                                    appInfo.setPids(pids);
                                    this.mUidLoadMap.put(processInfo.mUid, new UidLoadPair(appInfo));
                                    if (processInfo.mCleanType == ProcessCleaner.CleanType.FREEZE_UP_DOWNLOAD) {
                                        this.mFreezeAppInfos.add(0, appInfo);
                                    } else {
                                        this.mFreezeAppInfos.add(appInfo);
                                    }
                                }
                            }
                        }
                    }
                    str = pkgName;
                }
                sortFreezeAppByCPULoad(pidCnt);
                doFrozenInternel(duration, reason, sb);
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    private void doFrozenInternel(int duration, String reason, StringBuffer sb) {
        if (!this.mFreezeAppInfos.isEmpty()) {
            try {
                PGSdk pgInstance = PGSdk.getInstance();
                if (pgInstance != null) {
                    pgInstance.fastHibernation(this.mContext, this.mFreezeAppInfos, duration, reason);
                    AwareLog.i(TAG, "Fast freeze the pid=[" + sb.toString() + "]");
                }
            } catch (RemoteException re) {
                AwareLog.w(TAG, "do Frozen failed because not find the PG" + re.getMessage());
            }
        } else {
            AwareLog.i(TAG, "no pid need to freeze");
        }
    }

    private List<AwareProcessBlockInfo> getAllFreezeApp(AppMngConstant.AppFreezeSource config) {
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
        List<AwareProcessBlockInfo> awareProcessBlockInfos = DecisionMaker.getInstance().decideAll(awareProcList, getLevel(), AppMngConstant.AppMngFeature.APP_FREEZE, config);
        if (awareProcessBlockInfos != null && awareProcessBlockInfos.size() != 0) {
            return mergeBlock(awareProcessBlockInfos, mPriority);
        }
        AwareLog.w(TAG, "no pid need to freeze");
        return null;
    }

    private static void setPriority() {
        mPriority = new ArrayList();
        mPriority.add(ProcessCleaner.CleanType.FREEZE_NOMAL);
        mPriority.add(ProcessCleaner.CleanType.FREEZE_UP_DOWNLOAD);
        mPriority.add(ProcessCleaner.CleanType.NONE);
    }

    private List<AwareProcessInfo> filterUnRemoveSystemApp(List<ProcessInfo> infos) {
        List<AwareProcessInfo> awareProcList = new ArrayList<>(DEFAULT_CAPACITY);
        if (infos == null || infos.size() == 0) {
            return awareProcList;
        }
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            ProcessInfo procInfo = infos.get(i);
            if (isSystemUnRemoveBadPkg(procInfo) || !isSystemUnRemoveApp(procInfo.mAppUid % LaserTSMService.EXCUTE_OTA_RESULT_SUCCESS)) {
                AwareProcessInfo awareProcInfo = new AwareProcessInfo(procInfo.mPid, 0, 0, AwareAppMngSort.ClassRate.NORMAL.ordinal(), procInfo);
                awareProcList.add(awareProcInfo);
            }
        }
        return awareProcList;
    }

    private boolean isSystemUnRemoveBadPkg(ProcessInfo procInfo) {
        if (!this.mSysUnremoveBadPkgs.isEmpty() && procInfo.mPackageName != null && !procInfo.mPackageName.isEmpty()) {
            Iterator it = procInfo.mPackageName.iterator();
            while (it.hasNext()) {
                if (this.mSysUnremoveBadPkgs.contains((String) it.next())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSystemUnRemoveApp(int uid) {
        if (uid > 0 && uid < 10000) {
            return true;
        }
        if (this.mSystemUnremoveUidCache == null || !this.mSystemUnremoveUidCache.checkUidExist(uid)) {
            return false;
        }
        return true;
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
        List<AppInfo> apps = new ArrayList<>();
        AppInfo lastApp = null;
        for (ProcessInfo info : procList) {
            AppInfo appInfo = new AppInfo(info.mUid, pkg);
            List<Integer> pids = new ArrayList<>();
            pids.add(Integer.valueOf(info.mPid));
            appInfo.setPids(pids);
            if (lastApp == null) {
                lastApp = appInfo;
                apps.add(lastApp);
            } else if (lastApp.getUid() != info.mUid) {
                lastApp = appInfo;
                apps.add(lastApp);
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
                    Iterator<AppInfo> it = this.mFreezeAppInfos.iterator();
                    while (it.hasNext()) {
                        AppInfo app = it.next();
                        StringBuffer sb = new StringBuffer();
                        sb.append("    UID=");
                        sb.append(app.getUid());
                        sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                        sb.append("PID=");
                        if (app.getPids() != null) {
                            for (Integer intValue : app.getPids()) {
                                sb.append(intValue.intValue());
                                sb.append(",");
                            }
                        }
                        sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                        sb.append("PKG=");
                        sb.append(app.getPkg());
                        pw.println(sb.toString());
                    }
                }
            }
            pw.println();
            pw.println("Current can freeze app by default policy");
            List<AwareProcessBlockInfo> awareProcessBlockInfos = getAllFreezeApp(AppMngConstant.AppFreezeSource.FAST_FREEZE);
            if (awareProcessBlockInfos == null || awareProcessBlockInfos.isEmpty()) {
                pw.println("no pid need to freeze");
                return;
            }
            Iterator<AwareProcessBlockInfo> it2 = awareProcessBlockInfos.iterator();
            while (it2.hasNext()) {
                pw.println("    " + it2.next().toString());
            }
            pw.println();
            pw.println("Current can freeze app by camera policy");
            List<AwareProcessBlockInfo> awareProcessBlockInfos2 = getAllFreezeApp(AppMngConstant.AppFreezeSource.CAMERA_FREEZE);
            if (awareProcessBlockInfos2 == null || awareProcessBlockInfos2.isEmpty()) {
                pw.println("no pid need to freeze");
                return;
            }
            Iterator<AwareProcessBlockInfo> it3 = awareProcessBlockInfos2.iterator();
            while (it3.hasNext()) {
                pw.println("    " + it3.next().toString());
            }
        }
    }

    public void dumpFreezeApp(PrintWriter pw, String pkg, int time) {
        if (pw != null) {
            pw.println("Freeze current " + pkg + " app");
            List<AppInfo> apps = getAppInfoByPkg(pkg);
            if (apps != null && !apps.isEmpty()) {
                for (AppInfo app : apps) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("    UID=");
                    sb.append(app.getUid());
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("PID=");
                    if (app.getPids() != null) {
                        for (Integer intValue : app.getPids()) {
                            sb.append(intValue.intValue());
                            sb.append(",");
                        }
                    }
                    sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("PKG=");
                    sb.append(app.getPkg());
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
        this.mLastSampleTime = nowTime;
        getCpuTimeForUidList();
        if (nowTime - this.mLastSampleTime <= 60000) {
            List<UidLoadPair> uidLoadArray = new ArrayList<>();
            for (int i = 0; i < uidSize; i++) {
                UidLoadPair uidPair = this.mUidLoadMap.get(this.mUidLoadMap.keyAt(i));
                if (uidPair != null) {
                    uidLoadArray.add(uidPair);
                }
            }
            if (uidLoadArray.isEmpty() == 0) {
                Collections.sort(uidLoadArray, this.mUidLoadComparator);
                synchronized (this.mFreezeAppInfos) {
                    this.mFreezeAppInfos.clear();
                    int size = uidLoadArray.size();
                    for (int i2 = 0; i2 < size; i2++) {
                        UidLoadPair uidLoad = uidLoadArray.get(i2);
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
                int realUid = ((Integer) Files.getAttribute(Paths.get("/proc/" + pid, new String[0]), "unix:uid", new LinkOption[0])).intValue();
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
