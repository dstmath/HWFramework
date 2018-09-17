package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.feature.MemoryFeature2;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GpuCompressAction extends Action {
    private static final int MAX_PID_COUNT_GMC = 5;
    private static final int MIN_GMC_UID = 10000;
    private static final String TAG = "AwareMem_GMC";
    private static Map<Integer, GmcStat> hasGMCMap = new ArrayMap();
    private int mGmcCount = 0;

    private static class GmcStat {
        public int mPid = 0;
        public long mTime = 0;

        public GmcStat(int pid) {
            this.mPid = pid;
            this.mTime = SystemClock.elapsedRealtime();
        }
    }

    public GpuCompressAction(Context context) {
        super(context);
    }

    public static void removeUidFromGMCMap(int uid) {
        int pid = 0;
        long time = 0;
        synchronized (hasGMCMap) {
            if (hasGMCMap.containsKey(Integer.valueOf(uid))) {
                GmcStat gs = (GmcStat) hasGMCMap.get(Integer.valueOf(uid));
                pid = gs.mPid;
                time = gs.mTime;
                hasGMCMap.remove(Integer.valueOf(uid));
            }
        }
        if (pid > 0) {
            MemoryUtils.setReclaimGPUMemory(false, pid);
            AwareLog.d(TAG, "decompress gpu memory, uid:" + uid + " proc:" + pid + " time:" + time);
        }
    }

    public static void doGmc(int uid) {
        long startTime = SystemClock.elapsedRealtime();
        HwActivityManagerService hwAms = HwActivityManagerService.self();
        if (hwAms != null && uid >= 10000) {
            List<Integer> pids = hwAms.getPidWithUiFromUid(uid);
            if (pids.size() == 0) {
                AwareLog.d(TAG, "this uid:" + uid + " has no UI.");
                return;
            }
            for (Integer intValue : pids) {
                int pid = intValue.intValue();
                synchronized (hasGMCMap) {
                    if (hasGMCMap.containsKey(Integer.valueOf(uid))) {
                        AwareLog.d(TAG, "this uid " + uid + " has compressed gpu memory.");
                    } else {
                        hasGMCMap.put(Integer.valueOf(uid), new GmcStat(pid));
                        MemoryUtils.setReclaimGPUMemory(true, pid);
                        AwareLog.d(TAG, "compress gpu memory from hiber req, uid:" + uid + " pid:" + pid);
                    }
                }
            }
            AwareLog.d(TAG, "do gmc uid:" + uid + " used time:" + (SystemClock.elapsedRealtime() - startTime));
        }
    }

    private void generatePidList(List<AwareProcessBlockInfo> procsGroups, List<AwareProcessInfo> waitForCompressPidList) {
        if (procsGroups != null && waitForCompressPidList != null) {
            for (AwareProcessBlockInfo blockInfo : procsGroups) {
                if (blockInfo != null && blockInfo.mUid >= 10000) {
                    List<AwareProcessInfo> processList = blockInfo.getProcessList();
                    if (processList != null) {
                        synchronized (hasGMCMap) {
                            if (!hasGMCMap.containsKey(Integer.valueOf(blockInfo.mUid))) {
                                for (AwareProcessInfo proc : processList) {
                                    if (proc != null && proc.mProcInfo != null && proc.mHasShownUi && proc.mProcInfo.mCurAdj > 0) {
                                        waitForCompressPidList.add(proc);
                                        hasGMCMap.put(Integer.valueOf(blockInfo.mUid), new GmcStat(proc.mPid));
                                        this.mGmcCount--;
                                        if (this.mGmcCount <= 0) {
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
    }

    public int execute(Bundle extras) {
        if (extras == null) {
            AwareLog.w(TAG, "gmc not action for extras null");
            return -1;
        } else if (!MemoryFeature2.isUpMemoryFeature.get() || MemoryConstant.getConfigGmcSwitch() == 0) {
            AwareLog.d(TAG, "gmc can not action for not iaware2.0 or gmc function is close");
            return 0;
        } else {
            long availableRam = MemoryReader.getInstance().getMemAvailable();
            if (availableRam > MemoryConstant.getGpuMemoryLimit()) {
                AwareLog.d(TAG, "gmc can not action for available mem:" + availableRam + " > gmc limit:" + MemoryConstant.getGpuMemoryLimit());
                return 0;
            }
            long start = SystemClock.elapsedRealtime();
            this.mGmcCount = 5;
            AwareAppMngSortPolicy policy = MemoryUtils.getAppMngSortPolicy(1, 3);
            if (policy == null) {
                AwareLog.w(TAG, "getAppMngSortPolicy null policy!");
                return -1;
            }
            List<AwareProcessInfo> waitForCompressPidList = new ArrayList();
            generatePidList(MemoryUtils.getAppMngProcGroup(policy, 2), waitForCompressPidList);
            if (this.mGmcCount > 0) {
                generatePidList(MemoryUtils.getAppMngProcGroup(policy, 1), waitForCompressPidList);
            }
            doCompress(waitForCompressPidList);
            AwareLog.d(TAG, "compress gpu memory use time:" + (SystemClock.elapsedRealtime() - start));
            return 0;
        }
    }

    private boolean doCompress(List<AwareProcessInfo> waitForCompressPidList) {
        boolean ret = false;
        if (waitForCompressPidList == null) {
            return false;
        }
        for (AwareProcessInfo pinfo : waitForCompressPidList) {
            if (!(pinfo == null || pinfo.mProcInfo == null || pinfo.mProcInfo.mCurAdj <= 0)) {
                ret = true;
                MemoryUtils.setReclaimGPUMemory(true, pinfo.mPid);
                AwareLog.d(TAG, "compress gpu memory, pid:" + pinfo.mPid + " proc:" + pinfo.mProcInfo.mProcessName);
            }
        }
        return ret;
    }

    public void reset() {
    }
}
