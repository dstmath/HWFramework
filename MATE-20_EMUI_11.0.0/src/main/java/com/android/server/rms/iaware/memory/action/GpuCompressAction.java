package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.feature.MemoryFeatureEx;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.huawei.android.app.HwActivityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GpuCompressAction extends Action {
    private static final Map<Integer, GmcStat> HAS_GMC_MAP = new ArrayMap();
    private static final int MAX_PID_COUNT_GMC = 1;
    private static final int MIN_GMC_UID = 10000;
    private static final String TAG = "AwareMem_GMC";
    private int mGmcCount = 0;

    public GpuCompressAction(Context context) {
        super(context);
    }

    /* access modifiers changed from: private */
    public static class GmcStat {
        private int mPid = 0;
        private long mTime = 0;

        GmcStat(int pid) {
            this.mPid = pid;
            this.mTime = SystemClock.elapsedRealtime();
        }
    }

    public static void removeUidFromGmcMap(int uid) {
        int pid = 0;
        long time = 0;
        synchronized (HAS_GMC_MAP) {
            if (HAS_GMC_MAP.containsKey(Integer.valueOf(uid))) {
                GmcStat gs = HAS_GMC_MAP.get(Integer.valueOf(uid));
                pid = gs.mPid;
                time = gs.mTime;
                HAS_GMC_MAP.remove(Integer.valueOf(uid));
            }
        }
        if (pid > 0) {
            MemoryUtils.setReclaimGpuMemory(false, pid);
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "decompress gpu memory, uid:" + uid + " proc:" + pid + " time:" + time);
            }
        }
    }

    public static void doGmc(int uid) {
        long startTime = SystemClock.elapsedRealtime();
        if (uid >= 10000) {
            List<String> pids = HwActivityManager.getPidWithUiFromUid(uid);
            if (pids.size() != 0) {
                for (String pid : pids) {
                    try {
                        int intPid = Integer.parseInt(pid);
                        synchronized (HAS_GMC_MAP) {
                            if (!HAS_GMC_MAP.containsKey(Integer.valueOf(uid))) {
                                HAS_GMC_MAP.put(Integer.valueOf(uid), new GmcStat(intPid));
                                MemoryUtils.setReclaimGpuMemory(true, intPid);
                                if (AwareLog.getDebugLogSwitch()) {
                                    AwareLog.d(TAG, "compress gpu memory from hiber req, uid:" + uid + " pid:" + pid);
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "do gmc uid:" + uid + " used time:" + (SystemClock.elapsedRealtime() - startTime));
                }
            }
        }
    }

    private void generatePidList(List<AwareProcessBlockInfo> procsGroups, List<AwareProcessInfo> waitForCompressPidList) {
        if (!(procsGroups == null || waitForCompressPidList == null)) {
            for (AwareProcessBlockInfo blockInfo : procsGroups) {
                if (blockInfo != null && blockInfo.procUid >= 10000) {
                    List<AwareProcessInfo> processList = blockInfo.getProcessList();
                    if (processList != null) {
                        synchronized (HAS_GMC_MAP) {
                            if (!HAS_GMC_MAP.containsKey(Integer.valueOf(blockInfo.procUid))) {
                                for (AwareProcessInfo proc : processList) {
                                    if (proc != null) {
                                        if (proc.procProcInfo != null) {
                                            if (!proc.procHasShownUi) {
                                                continue;
                                            } else if (proc.procProcInfo.mCurAdj > 0) {
                                                getCompressPidList(waitForCompressPidList, blockInfo, proc);
                                                if (this.mGmcCount <= 0) {
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void getCompressPidList(List<AwareProcessInfo> waitForCompressPidList, AwareProcessBlockInfo blockInfo, AwareProcessInfo proc) {
        String processName = proc.procProcInfo.mProcessName;
        String packageName = blockInfo.procPackageName;
        if (processName != null && packageName != null && isGameApp(packageName) && isMainProcess(processName, packageName)) {
            waitForCompressPidList.add(proc);
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "isGameApp, PackageName:" + packageName + " mCurAdj:" + proc.procProcInfo.mCurAdj);
            }
            HAS_GMC_MAP.put(Integer.valueOf(blockInfo.procUid), new GmcStat(proc.procPid));
            this.mGmcCount--;
        }
    }

    private boolean isMainProcess(String processName, String pkgName) {
        return processName.equals(pkgName);
    }

    private boolean isGameApp(String pkg) {
        return AppTypeRecoManager.getInstance().getAppType(pkg) == 9;
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public int execute(Bundle extras) {
        if (extras == null) {
            AwareLog.w(TAG, "not action for extras null");
            return -1;
        } else if (!MemoryFeatureEx.IS_UP_MEMORY_FEATURE.get() || MemoryConstant.getConfigGmcSwitch() == 0) {
            AwareLog.d(TAG, "not action for not iaware2.0 or gmc is close");
            return 0;
        } else {
            long availableRam = MemoryReader.getInstance().getMemAvailable();
            if (availableRam > MemoryConstant.getGpuMemoryLimit()) {
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "not action for available mem:" + availableRam + " > gmc limit:" + MemoryConstant.getGpuMemoryLimit());
                }
                return 0;
            }
            this.mGmcCount = 1;
            AwareAppMngSortPolicy policy = MemoryUtils.getAppMngSortPolicy(1, 3);
            if (policy == null) {
                AwareLog.w(TAG, "getAppMngSortPolicy null policy!");
                return -1;
            }
            List<AwareProcessInfo> waitForCompressPidList = new ArrayList<>();
            generatePidList(MemoryUtils.getAppMngProcGroup(policy, 2), waitForCompressPidList);
            if (this.mGmcCount > 0) {
                generatePidList(MemoryUtils.getAppMngProcGroup(policy, 1), waitForCompressPidList);
            }
            doCompress(waitForCompressPidList);
            return 0;
        }
    }

    private boolean doCompress(List<AwareProcessInfo> waitForCompressPidList) {
        boolean ret = false;
        if (waitForCompressPidList == null) {
            return false;
        }
        for (AwareProcessInfo pinfo : waitForCompressPidList) {
            if (!(pinfo == null || pinfo.procProcInfo == null || pinfo.procProcInfo.mCurAdj <= 0)) {
                ret = true;
                MemoryUtils.setReclaimGpuMemory(true, pinfo.procPid);
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(TAG, "compress gpu memory, pid:" + pinfo.procPid + " proc:" + pinfo.procProcInfo.mProcessName);
                }
            }
        }
        return ret;
    }

    @Override // com.android.server.rms.iaware.memory.action.Action
    public void reset() {
    }
}
