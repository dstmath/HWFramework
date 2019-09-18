package com.android.server.mtm.iaware.appmng.appcpulimit;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.feature.CpuLimitFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AwareAppDefaultCpuLimit extends CleanSource {
    private static final String TAG = "AwareAppDefaultCpuLimit";
    private static AwareAppDefaultCpuLimit mAwareAppDefaultCpuLimit = null;
    private Map<Integer, Integer> mCpuLimitPids = new ArrayMap();
    private Set<Integer> mCpulimitUids = new ArraySet();
    private PackageManager mPm = null;

    public static synchronized AwareAppDefaultCpuLimit getInstance() {
        AwareAppDefaultCpuLimit awareAppDefaultCpuLimit;
        synchronized (AwareAppDefaultCpuLimit.class) {
            if (mAwareAppDefaultCpuLimit == null) {
                mAwareAppDefaultCpuLimit = new AwareAppDefaultCpuLimit();
            }
            awareAppDefaultCpuLimit = mAwareAppDefaultCpuLimit;
        }
        return awareAppDefaultCpuLimit;
    }

    private AwareAppDefaultCpuLimit() {
    }

    public void init(Context ctx) {
        if (ctx != null) {
            this.mPm = ctx.getPackageManager();
        }
    }

    public void deInitDefaultFree() {
        this.mPm = null;
    }

    public boolean isUidLimited(int uid) {
        return this.mCpulimitUids.contains(Integer.valueOf(uid));
    }

    public boolean isPidLimited(int pid) {
        return this.mCpuLimitPids.containsKey(Integer.valueOf(pid));
    }

    public void doLimitCpu(String pkgName, AppMngConstant.AppCpuLimitSource config) {
        if (pkgName == null) {
            AwareLog.w(TAG, "The input params is invalid");
            return;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos = getAllCpuLimitApp(config);
        if (awareProcessBlockInfos == null || awareProcessBlockInfos.isEmpty()) {
            AwareLog.w(TAG, "no pid need to limit");
            return;
        }
        synchronized (this) {
            this.mCpuLimitPids.clear();
            this.mCpulimitUids.clear();
            List<Integer> assocWithFGPids = getAssocPidsByUid(getUidByPackageName(pkgName));
            int curUserId = AwareAppAssociate.getInstance().getCurUserId();
            for (AwareProcessBlockInfo processInfo : awareProcessBlockInfos) {
                if (processInfo.mCleanType == ProcessCleaner.CleanType.CPULIMIT) {
                    if (!pkgName.equals(processInfo.mPackageName) || UserHandle.getUserId(processInfo.mUid) != curUserId) {
                        List<AwareProcessInfo> infos = processInfo.mProcessList;
                        if (infos != null) {
                            if (!infos.isEmpty()) {
                                doCpuGroup(infos, assocWithFGPids, processInfo.mPackageName);
                            }
                        }
                    }
                }
            }
            doCpuLimit();
        }
    }

    private void doCpuGroup(List<AwareProcessInfo> infos, List<Integer> filterPids, String pkgName) {
        if (infos != null && !infos.isEmpty()) {
            for (AwareProcessInfo info : infos) {
                this.mCpulimitUids.add(Integer.valueOf(info.mProcInfo.mUid));
                if (!filterPids.contains(Integer.valueOf(info.mPid))) {
                    this.mCpuLimitPids.put(Integer.valueOf(info.mPid), Integer.valueOf(info.mProcInfo.mUid));
                }
            }
        }
    }

    private void doCpuLimit() {
        if (!this.mCpuLimitPids.isEmpty()) {
            CpuLimitFeature.getInstance().setCpuLimitTaskList(this.mCpuLimitPids, true);
            AwareLog.i(TAG, "do Limit cpu count: " + this.mCpuLimitPids.size() + ", pids: " + this.mCpuLimitPids.toString());
        }
    }

    private List<AwareProcessBlockInfo> getAllCpuLimitApp(AppMngConstant.AppCpuLimitSource config) {
        List<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procList.size() == 0) {
            AwareLog.d(TAG, "no pid exit in mtm");
            return null;
        }
        List<AwareProcessInfo> awareProcList = new ArrayList<>();
        for (ProcessInfo procInfo : procList) {
            AwareProcessInfo awareProcInfo = new AwareProcessInfo(procInfo.mPid, 0, 0, AwareAppMngSort.ClassRate.NORMAL.ordinal(), procInfo);
            awareProcList.add(awareProcInfo);
        }
        if (awareProcList.size() == 0) {
            AwareLog.w(TAG, "no pid need to app filter");
            return null;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos = DecisionMaker.getInstance().decideAll(awareProcList, getLevel(), AppMngConstant.AppMngFeature.APP_CPULIMIT, config);
        if (awareProcessBlockInfos != null && awareProcessBlockInfos.size() != 0) {
            return mergeBlock(awareProcessBlockInfos);
        }
        AwareLog.w(TAG, "no pid need to do cpu limit");
        return null;
    }

    private int getUidByPackageName(String pkgName) {
        if (pkgName == null) {
            return 0;
        }
        try {
            if (pkgName.isEmpty() || this.mPm == null) {
                return 0;
            }
            return this.mPm.getApplicationInfoAsUser(pkgName, 1, AwareAppAssociate.getInstance().getCurUserId()).uid;
        } catch (PackageManager.NameNotFoundException e) {
            AwareLog.w(TAG, "get application info failed");
            return 0;
        }
    }

    private List<Integer> getAssocPidsByUid(int uid) {
        List<Integer> assocPids = new ArrayList<>();
        List<Integer> pids = ProcessInfoCollector.getInstance().getPidsFromUid(uid, UserHandle.getUserId(uid));
        if (pids.isEmpty()) {
            return assocPids;
        }
        assocPids.addAll(pids);
        int size = pids.size();
        for (int i = 0; i < size; i++) {
            int sPid = pids.get(i).intValue();
            Set<Integer> strongPids = new ArraySet<>();
            AwareAppAssociate.getInstance().getAssocListForPid(sPid, strongPids);
            if (!strongPids.isEmpty()) {
                assocPids.addAll(strongPids);
            }
        }
        return assocPids;
    }

    private List<Integer> getAssocPidsByPid(int pid) {
        List<Integer> assocPids = new ArrayList<>();
        assocPids.add(Integer.valueOf(pid));
        Set<Integer> strongPids = new ArraySet<>();
        AwareAppAssociate.getInstance().getAssocListForPid(pid, strongPids);
        if (!strongPids.isEmpty()) {
            assocPids.addAll(strongPids);
        }
        return assocPids;
    }

    public void doUnLimitCPU() {
        CpuLimitFeature.getInstance().setCpuLimitTaskList(null, true);
        AwareLog.i(TAG, "do CPU UnLimit ok");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006e, code lost:
        return;
     */
    public void doRemoveCpuPids(int uid, int pid) {
        List<Integer> cpuLimitPids;
        synchronized (this) {
            if (this.mCpulimitUids.contains(Integer.valueOf(uid))) {
                if (pid <= 0 || this.mCpuLimitPids.containsKey(Integer.valueOf(pid))) {
                    if (pid == 0) {
                        cpuLimitPids = getAssocPidsByUid(uid);
                        this.mCpulimitUids.remove(Integer.valueOf(uid));
                    } else {
                        cpuLimitPids = getAssocPidsByPid(pid);
                    }
                    if (!cpuLimitPids.isEmpty()) {
                        CpuLimitFeature.getInstance().removeCpuLimitTaskList(cpuLimitPids);
                        AwareLog.i(TAG, "doRemoveCpuPids uid: " + uid + ", pid:" + pid + ", remove pids: " + cpuLimitPids.toString());
                    }
                }
            }
        }
    }

    private int getLevel() {
        return 0;
    }
}
