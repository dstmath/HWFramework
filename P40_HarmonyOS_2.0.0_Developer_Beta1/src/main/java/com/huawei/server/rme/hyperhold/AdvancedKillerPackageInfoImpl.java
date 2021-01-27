package com.huawei.server.rme.hyperhold;

import android.content.Context;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.memory.policy.CachedMemoryCleanPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.huawei.server.rme.hyperhold.AdvancedKillerPackageInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdvancedKillerPackageInfoImpl extends AdvancedKillerPackageInfo {
    private AwareProcessBlockInfo block;
    private long totalMem;

    public AdvancedKillerPackageInfoImpl(AwareProcessBlockInfo block2) {
        this.block = block2;
    }

    public List<Integer> getPids() {
        List<Integer> pids = new ArrayList<>();
        AwareProcessBlockInfo awareProcessBlockInfo = this.block;
        if (!(awareProcessBlockInfo == null || awareProcessBlockInfo.procProcessList == null)) {
            for (AwareProcessInfo awareProcess : this.block.procProcessList) {
                if (awareProcess != null) {
                    pids.add(Integer.valueOf(awareProcess.procPid));
                }
            }
        }
        return pids;
    }

    public void setTotalMem(long totalMem2) {
        this.totalMem = totalMem2;
    }

    public long getTotalMem() {
        return this.totalMem;
    }

    public int getAdjScore() {
        AwareProcessBlockInfo awareProcessBlockInfo = this.block;
        if (awareProcessBlockInfo == null) {
            return 0;
        }
        return awareProcessBlockInfo.getAdj();
    }

    public int getUid() {
        AwareProcessBlockInfo awareProcessBlockInfo = this.block;
        if (awareProcessBlockInfo == null) {
            return 0;
        }
        return awareProcessBlockInfo.procUid;
    }

    public String getPackageName() {
        AwareProcessBlockInfo awareProcessBlockInfo = this.block;
        return awareProcessBlockInfo == null ? "" : awareProcessBlockInfo.procPackageName;
    }

    public String getFirstProcessName() {
        AwareProcessInfo proc;
        String procName;
        AwareProcessBlockInfo awareProcessBlockInfo = this.block;
        if (awareProcessBlockInfo == null || awareProcessBlockInfo.procProcessList == null || this.block.procProcessList.isEmpty() || (proc = this.block.procProcessList.get(0)) == null || proc.procProcInfo == null || (procName = proc.procProcInfo.mProcessName) == null) {
            return "";
        }
        return procName;
    }

    public int getWeight() {
        AwareProcessBlockInfo awareProcessBlockInfo = this.block;
        if (awareProcessBlockInfo == null) {
            return -1;
        }
        return awareProcessBlockInfo.procWeight;
    }

    public List<Integer> killPackage(Context context, AtomicBoolean state, boolean needCheckAdj) {
        boolean[] params = {false, false, needCheckAdj};
        if (this.block == null) {
            return Collections.emptyList();
        }
        return ProcessCleaner.getInstance(context).killProcessesSameUidFast(this.block, state, "AdvancedKiller", params);
    }

    public int getFloatingBarPosition() {
        AwareProcessBlockInfo awareProcessBlockInfo = this.block;
        if (awareProcessBlockInfo == null) {
            return -1;
        }
        return awareProcessBlockInfo.floatBallAppImport;
    }

    public boolean canPackageBeKilled() {
        AwareProcessBlockInfo awareProcessBlockInfo = this.block;
        if (awareProcessBlockInfo == null || awareProcessBlockInfo.procProcessList == null || (this.block.procProcessList.size() == 1 && "com.huawei.camera".equals(this.block.procPackageName) && this.totalMem < ((long) MemoryConstant.getCameraPreloadKillUss()))) {
            return false;
        }
        return true;
    }

    public void killNotifyPackageTracker() {
        if (this.block != null) {
            PackageTracker.getInstance().trackKillEvent(this.block.procUid, this.block.getProcessList());
        }
    }

    public void killNotifyCachedClean() {
        AwareProcessBlockInfo awareProcessBlockInfo = this.block;
        if (awareProcessBlockInfo != null && awareProcessBlockInfo.procProcessList != null && this.block.procProcessList.size() == 1 && this.block.procProcessList.get(0).procProcInfo != null) {
            CachedMemoryCleanPolicy.getInstance().updateCachedMemoryCleanRecord(this.block.procUid, this.block.procPackageName, this.block.procProcessList.get(0).procProcInfo.mProcessName, true);
        }
    }

    public AdvancedKillerPackageInfo.FrequencyType getFreqType() {
        AwareProcessBlockInfo awareProcessBlockInfo = this.block;
        if (awareProcessBlockInfo == null) {
            return AdvancedKillerPackageInfo.FrequencyType.FREQUENCY_DEFAULT;
        }
        List<AwareProcessInfo> processList = awareProcessBlockInfo.procProcessList;
        if (processList == null) {
            return AdvancedKillerPackageInfo.FrequencyType.FREQUENCY_DEFAULT;
        }
        int uid = this.block.procUid;
        for (String packageName : getPackageList(processList)) {
            PackageTracker.KilledFrequency freq = getKilledFrequency(uid, packageName, processList);
            if (freq == PackageTracker.KilledFrequency.FREQUENCY_CRITICAL) {
                return AdvancedKillerPackageInfo.FrequencyType.FREQUENCY_CRIT;
            }
            if (freq == PackageTracker.KilledFrequency.FREQUENCY_HIGH) {
                return AdvancedKillerPackageInfo.FrequencyType.FREQUENCY_HIGH;
            }
        }
        return AdvancedKillerPackageInfo.FrequencyType.FREQUENCY_DEFAULT;
    }

    private List<String> getPackageList(List<AwareProcessInfo> procInfoList) {
        List<String> packageList = new ArrayList<>();
        if (procInfoList == null) {
            return packageList;
        }
        for (AwareProcessInfo info : procInfoList) {
            if (!(info.procProcInfo == null || info.procProcInfo.mPackageName == null)) {
                Iterator it = info.procProcInfo.mPackageName.iterator();
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

    private PackageTracker.KilledFrequency getKilledFrequency(int uid, String packageName, List<AwareProcessInfo> processList) {
        PackageTracker.KilledFrequency freq = PackageTracker.KilledFrequency.FREQUENCY_NORMAL;
        if (uid >= 10000) {
            return PackageTracker.getInstance().getPackageKilledFrequency(packageName, uid);
        }
        if (processList.get(0) == null || processList.get(0).procProcInfo == null) {
            return freq;
        }
        return PackageTracker.getInstance().getProcessKilledFrequency(packageName, processList.get(0).procProcInfo.mProcessName, uid);
    }
}
