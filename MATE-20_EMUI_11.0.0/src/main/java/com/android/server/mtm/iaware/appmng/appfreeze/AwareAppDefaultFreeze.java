package com.android.server.mtm.iaware.appmng.appfreeze;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.iaware.appmng.appstart.datamgr.SystemUnremoveUidCache;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.rms.dualfwk.AwareMiddleware;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.cpu.CpuCustBaseConfig;
import com.huawei.android.internal.os.KernelCpuUidTimeReaderEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.pgmng.plug.AppInfo;
import com.huawei.android.pgmng.plug.PowerKit;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppDefaultFreeze extends CleanSource {
    private static final String FREEZE_BAD = "app_freeze_bad";
    private static final long INTERVAL_TIME = 60000;
    private static final Object LOCK = new Object();
    private static final int MAX_PID_CNT = 20;
    private static final String REASON_DUMP_FREEZE = "Only for dump test";
    private static final String TAG = "mtm.AwareAppDefaultFreeze";
    private static AwareAppDefaultFreeze sAwareAppDefaultFreeze = null;
    private static List<ProcessCleaner.CleanType> sPriority;
    private final AtomicBoolean isExecutorAlive = new AtomicBoolean(false);
    private Context mContext;
    private final ArrayList<AppInfo> mFreezeAppInfos = new ArrayList<>();
    private final KernelCpuUidTimeReaderEx.KernelCpuUidUserSysTimeReaderEx mKernelCpuUidTimeReader = new KernelCpuUidTimeReaderEx.KernelCpuUidUserSysTimeReaderEx(true);
    private long mLastSampleTime = SystemClock.uptimeMillis();
    private Set<String> mSysUnremoveBadPkgs = new ArraySet();
    private SystemUnremoveUidCache mSystemUnremoveUidCache;
    private UidCpuTimeReaderCallback mUidCpuTimeReaderCallback = new UidCpuTimeReaderCallback();
    private UidLoadComparator mUidLoadComparator = new UidLoadComparator();
    private SparseArray<UidLoadPair> mUidLoadMap = new SparseArray<>();

    public static AwareAppDefaultFreeze getInstance() {
        AwareAppDefaultFreeze awareAppDefaultFreeze;
        synchronized (LOCK) {
            if (sAwareAppDefaultFreeze == null) {
                sAwareAppDefaultFreeze = new AwareAppDefaultFreeze();
                setPriority();
            }
            awareAppDefaultFreeze = sAwareAppDefaultFreeze;
        }
        return awareAppDefaultFreeze;
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

    private void addFreezePids(List<AwareProcessInfo> infos, List<Integer> pids, StringBuffer sb) {
        Iterator<AwareProcessInfo> iterator = infos.iterator();
        while (iterator.hasNext()) {
            AwareProcessInfo info = iterator.next();
            if (info.procProcInfo == null) {
                iterator.remove();
            } else {
                int pid = info.procPid;
                if (!checkPidValid(pid, info.procProcInfo.mUid)) {
                    iterator.remove();
                } else {
                    pids.add(Integer.valueOf(pid));
                    sb.append(pid);
                    sb.append(',');
                }
            }
        }
    }

    private int addFreezeAppInfos(List<AwareProcessBlockInfo> awareProcessBlockInfos, String pkgName, StringBuffer sb) {
        List<AwareProcessInfo> infos;
        int pidCnt = 0;
        for (AwareProcessBlockInfo processInfo : awareProcessBlockInfos) {
            if ((processInfo.procCleanType == ProcessCleaner.CleanType.FREEZE_NOMAL || processInfo.procCleanType == ProcessCleaner.CleanType.FREEZE_UP_DOWNLOAD) && !((pkgName.equals(processInfo.procPackageName) && UserHandleEx.getUserId(processInfo.procUid) == AwareAppAssociate.getInstance().getCurUserId()) || (infos = processInfo.procProcessList) == null || infos.size() == 0)) {
                AppInfo appInfo = new AppInfo(processInfo.procUid, processInfo.procPackageName);
                List<Integer> pids = new ArrayList<>(infos.size());
                addFreezePids(infos, pids, sb);
                pidCnt += infos.size();
                appInfo.setPids(pids);
                this.mUidLoadMap.put(processInfo.procUid, new UidLoadPair(appInfo));
                if (processInfo.procCleanType == ProcessCleaner.CleanType.FREEZE_UP_DOWNLOAD) {
                    this.mFreezeAppInfos.add(0, appInfo);
                } else {
                    this.mFreezeAppInfos.add(appInfo);
                }
            }
        }
        return pidCnt;
    }

    public void doFrozen(String pkgName, AppMngConstant.AppFreezeSource config, int duration, String reason) {
        if (this.isExecutorAlive.compareAndSet(false, true)) {
            new Thread(new Runnable(pkgName, config, duration, reason) {
                /* class com.android.server.mtm.iaware.appmng.appfreeze.$$Lambda$AwareAppDefaultFreeze$7JZLdXQa4PwWuOcLRsPzJyhr9Tw */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ AppMngConstant.AppFreezeSource f$2;
                private final /* synthetic */ int f$3;
                private final /* synthetic */ String f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    AwareAppDefaultFreeze.this.lambda$doFrozen$0$AwareAppDefaultFreeze(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            }, "iaware.freeze").start();
        }
    }

    public /* synthetic */ void lambda$doFrozen$0$AwareAppDefaultFreeze(String pkgName, AppMngConstant.AppFreezeSource config, int duration, String reason) {
        CleanSource.setSchedPriority();
        if (pkgName == null) {
            AwareLog.w(TAG, "The input params is invalid");
            return;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos = getAllFreezeApp(config);
        if (awareProcessBlockInfos.isEmpty()) {
            AwareLog.w(TAG, "no pid need to freeze");
            return;
        }
        synchronized (this.mFreezeAppInfos) {
            this.mUidLoadMap.clear();
            this.mFreezeAppInfos.clear();
            StringBuffer sb = new StringBuffer();
            sortFreezeAppByCPULoad(addFreezeAppInfos(awareProcessBlockInfos, pkgName, sb));
            doFrozenInternel(duration, reason, sb);
        }
        this.isExecutorAlive.set(false);
    }

    private void doFrozenInternel(int duration, String reason, StringBuffer sb) {
        if (this.mFreezeAppInfos.isEmpty()) {
            AwareLog.i(TAG, "no pid need to freeze");
            return;
        }
        try {
            PowerKit pgInstance = PowerKit.getInstance();
            if (pgInstance != null) {
                pgInstance.fastHibernation(this.mContext, this.mFreezeAppInfos, duration, reason);
                AwareLog.i(TAG, "Fast freeze the pid=[" + sb.toString() + "]");
            }
        } catch (RemoteException re) {
            AwareLog.w(TAG, "do Frozen failed because not find the PG" + re.getMessage());
        }
    }

    private List<AwareProcessBlockInfo> getAllFreezeApp(AppMngConstant.AppFreezeSource config) {
        List<AwareProcessBlockInfo> emptyBlockInfo = new ArrayList<>();
        List<AwareProcessInfo> awareProcListBeforeFiler = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (awareProcListBeforeFiler == null || awareProcListBeforeFiler.isEmpty()) {
            AwareLog.d(TAG, "no pid exit in mtm");
            return emptyBlockInfo;
        }
        List<AwareProcessInfo> awareProcList = filterFrozenApp(awareProcListBeforeFiler);
        if (awareProcList.size() == 0) {
            AwareLog.d(TAG, "no pids exit in mtm without system and system unremove app");
            return emptyBlockInfo;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos = DecisionMaker.getInstance().decideAll(awareProcList, getLevel(), AppMngConstant.AppMngFeature.APP_FREEZE, (AppMngConstant.EnumWithDesc) config);
        if (awareProcessBlockInfos != null && awareProcessBlockInfos.size() != 0) {
            return mergeBlock(awareProcessBlockInfos, sPriority);
        }
        AwareLog.w(TAG, "no pid need to freeze");
        return emptyBlockInfo;
    }

    private static void setPriority() {
        sPriority = new ArrayList();
        sPriority.add(ProcessCleaner.CleanType.FREEZE_NOMAL);
        sPriority.add(ProcessCleaner.CleanType.FREEZE_UP_DOWNLOAD);
        sPriority.add(ProcessCleaner.CleanType.NONE);
    }

    private List<AwareProcessInfo> filterFrozenApp(List<AwareProcessInfo> awareProcListBeforeFiler) {
        List<AwareProcessInfo> awareProcList = new ArrayList<>(awareProcListBeforeFiler.size());
        int size = awareProcListBeforeFiler.size();
        for (int i = 0; i < size; i++) {
            AwareProcessInfo awareProcInfo = awareProcListBeforeFiler.get(i);
            ProcessInfo procInfo = awareProcInfo.procProcInfo;
            if ((isSystemUnRemoveBadPkg(procInfo) || !isSystemUnRemoveApp(procInfo.mAppUid % 100000)) && !isZApp(procInfo)) {
                awareProcList.add(awareProcInfo);
            }
        }
        return awareProcList;
    }

    private boolean isZApp(ProcessInfo procInfo) {
        if (procInfo.mPackageName == null || procInfo.mPackageName.isEmpty()) {
            return false;
        }
        Iterator it = procInfo.mPackageName.iterator();
        while (it.hasNext()) {
            if (AwareMiddleware.getInstance().isZApp((String) it.next())) {
                return true;
            }
        }
        return false;
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
        SystemUnremoveUidCache systemUnremoveUidCache = this.mSystemUnremoveUidCache;
        if (systemUnremoveUidCache == null || !systemUnremoveUidCache.checkUidExist(uid)) {
            return false;
        }
        return true;
    }

    private int getLevel() {
        return 0;
    }

    /* access modifiers changed from: private */
    public static class ProcessInfoComparator implements Comparator<ProcessInfo>, Serializable {
        private static final long serialVersionUID = 1;

        private ProcessInfoComparator() {
        }

        public int compare(ProcessInfo arg0, ProcessInfo arg1) {
            if (arg0 == null) {
                return arg1 == null ? 0 : -1;
            }
            if (arg1 == null) {
                return 1;
            }
            if (arg0.mUid < arg1.mUid) {
                return -1;
            }
            if (arg0.mUid == arg1.mUid) {
                return 0;
            }
            return 1;
        }
    }

    private List<AppInfo> getAppInfoByPkg(String pkg) {
        List<AppInfo> emptyInfo = new ArrayList<>();
        List<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfosFromPackage(pkg, UserHandleEx.myUserId());
        if (procList.isEmpty()) {
            AwareLog.d(TAG, "no pid exit in mtm");
            return emptyInfo;
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
                Iterator<AppInfo> it = this.mFreezeAppInfos.iterator();
                while (it.hasNext()) {
                    AppInfo app = it.next();
                    StringBuffer sb = new StringBuffer();
                    sb.append("    UID=");
                    sb.append(app.getUid());
                    sb.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("PID=");
                    if (app.getPids() != null) {
                        for (Integer num : app.getPids()) {
                            sb.append(num.intValue());
                            sb.append(",");
                        }
                    }
                    sb.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("PKG=");
                    sb.append(app.getPkg());
                    pw.println(sb.toString());
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
            if (!apps.isEmpty()) {
                for (AppInfo app : apps) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("    UID=");
                    sb.append(app.getUid());
                    sb.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("PID=");
                    if (app.getPids() != null) {
                        for (Integer num : app.getPids()) {
                            sb.append(num.intValue());
                            sb.append(",");
                        }
                    }
                    sb.append(CpuCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    sb.append("PKG=");
                    sb.append(app.getPkg());
                    pw.println(sb.toString());
                }
                try {
                    PowerKit.getInstance().fastHibernation(this.mContext, apps, time, REASON_DUMP_FREEZE);
                } catch (RemoteException re) {
                    AwareLog.w(TAG, "do UnFrozen failed because not find the PG" + re.getMessage());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class UidLoadPair {
        public AppInfo info;
        public long runningTime = 0;

        public UidLoadPair(AppInfo info2) {
            this.info = info2;
        }
    }

    /* access modifiers changed from: private */
    public static class UidLoadComparator implements Comparator<UidLoadPair>, Serializable {
        private static final long serialVersionUID = 1;

        private UidLoadComparator() {
        }

        public int compare(UidLoadPair lhs, UidLoadPair rhs) {
            return Long.compare(rhs.runningTime, lhs.runningTime);
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
            if (!uidLoadArray.isEmpty()) {
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
        this.mKernelCpuUidTimeReader.readDelta(this.mUidCpuTimeReaderCallback);
    }

    /* access modifiers changed from: private */
    public class UidCpuTimeReaderCallback extends KernelCpuUidTimeReaderEx.CallbackEx {
        private UidCpuTimeReaderCallback() {
        }

        public void onUidCpuTime(int uid, long[] time) {
            UidLoadPair pair;
            if (time != null && time.length == 2 && (pair = (UidLoadPair) AwareAppDefaultFreeze.this.mUidLoadMap.get(uid)) != null) {
                pair.runningTime = time[0] + time[1];
            }
        }
    }

    private boolean checkPidValid(int pid, int uid) {
        if (new File("/acct/uid_" + uid + "/pid_" + pid).exists()) {
            return true;
        }
        try {
            Object obj = Files.getAttribute(Paths.get("/proc/" + pid, new String[0]), "unix:uid", new LinkOption[0]);
            if (!(obj instanceof Integer)) {
                return false;
            }
            int realUid = ((Integer) obj).intValue();
            if (realUid == uid) {
                return true;
            }
            AwareLog.w(TAG, "read uid " + realUid + " of " + pid + " is not match");
            return false;
        } catch (IOException e) {
            AwareLog.w(TAG, "read status of " + pid + " failed");
            return false;
        }
    }

    public void dumpFreezeBadPid(PrintWriter pw, int pid, int uid) {
        if (pw == null || pid < 0 || uid < 0) {
            return;
        }
        if (checkPidValid(pid, uid)) {
            pw.println("pid " + pid + " match uid " + uid);
            return;
        }
        pw.println("pid " + pid + " not match uid " + uid);
    }
}
