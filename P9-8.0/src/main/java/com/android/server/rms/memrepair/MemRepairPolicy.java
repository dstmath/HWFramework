package com.android.server.rms.memrepair;

import android.os.Debug;
import android.rms.iaware.AwareLog;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import android.rms.iaware.memrepair.MemRepairProcInfo;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.memrepair.MemRepairAlgorithm.CallbackData;
import com.android.server.rms.memrepair.MemRepairAlgorithm.MRCallback;
import com.android.server.rms.memrepair.MemRepairAlgorithm.MemRepairHolder;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MemRepairPolicy {
    public static final int MAX_DVALUE_FLOAT_PERCENT = 30;
    public static final int MAX_MEM_THRES_COUNT = 20;
    public static final int MAX_PROCESS_THRES_COUNT = 1;
    public static final int MEM_THRES_ITEM_COUNT = 3;
    public static final int MEM_THRES_MAX_FLOAT_IDX = 1;
    public static final int MEM_THRES_MIN_FLOAT_IDX = 0;
    public static final int MEM_THRES_PERCENTAGE_IDX = 2;
    public static final int MIN_DVALUE_FLOAT_PERCENT = 1;
    private static final int NORMALIZATION_THREASHOLD = 5120;
    public static final int PROCESS_THRES_BG_IDX = 1;
    private static final int PROCESS_THRES_EMERG = 0;
    public static final int PROCESS_THRES_FG_IDX = 0;
    public static final int PROCESS_THRES_ITEM_COUNT = 2;
    private static final int SCENE_TYPE_MIDNIGHT = 1;
    private static final int SCENE_TYPE_SCREENOFF = 0;
    private static final String TAG = "AwareMem_MRPolicy";
    private static final int TYPE_EMERG_BG_THRESHOLD = 4;
    private static final int TYPE_EMERG_FG_THRESHOLD = 2;
    private static final int TYPE_MEM_GROW_UP = 1;
    private static final int TYPE_NONE = 0;
    private static MemRepairPolicy mMemRepairPolicy;
    private int mDValueFloatPercent = 5;
    private long[][] mFloatThresHolds = null;
    private AlgoCallback mMRCallback = new AlgoCallback();
    private int mMaxCollectCount = 50;
    private int mMinCollectCount = 6;
    private long[][] mProcThresHolds = null;

    private static final class AlgoCallback implements MRCallback {
        /* synthetic */ AlgoCallback(AlgoCallback -this0) {
            this();
        }

        private AlgoCallback() {
        }

        public int estimateLinear(Object user, CallbackData outData) {
            if (outData == null) {
                return 5;
            }
            if (outData.isIncreased()) {
                AwareLog.d(MemRepairPolicy.TAG, "leak:state=" + outData.mDValueState + ", dvalues=" + Arrays.toString(outData.mDValues));
                return 1;
            }
            AwareLog.d(MemRepairPolicy.TAG, "continue:state=" + outData.mDValueState + ", dvalues=" + Arrays.toString(outData.mDValues));
            return 3;
        }
    }

    private MemRepairPolicy() {
    }

    public static MemRepairPolicy getInstance() {
        MemRepairPolicy memRepairPolicy;
        synchronized (MemRepairPolicy.class) {
            if (mMemRepairPolicy == null) {
                mMemRepairPolicy = new MemRepairPolicy();
            }
            memRepairPolicy = mMemRepairPolicy;
        }
        return memRepairPolicy;
    }

    public void updateCollectCount(int fgCollectCount, int bgCollectCount) {
        int i;
        AwareLog.d(TAG, "updateCollectCount fgCount=" + fgCollectCount + ",bgCount=" + bgCollectCount);
        int minCount = fgCollectCount < bgCollectCount ? bgCollectCount : fgCollectCount;
        if (minCount < 6) {
            minCount = 6;
        }
        this.mMinCollectCount = minCount;
        this.mMaxCollectCount = this.mMinCollectCount * 2;
        if (this.mMaxCollectCount < 100) {
            i = this.mMaxCollectCount;
        } else {
            i = 100;
        }
        this.mMaxCollectCount = i;
    }

    public void updateDValueFloatPercent(int percent) {
        if (percent < 1 || percent > 30) {
            AwareLog.w(TAG, "updateDValueFloatPercent percent=" + percent);
            return;
        }
        this.mDValueFloatPercent = percent;
        AwareLog.d(TAG, "updateDValueFloatPercent=" + percent);
    }

    public void updateFloatThresHold(long[][] floatThresHolds) {
        if (floatThresHolds == null || floatThresHolds.length < 1 || floatThresHolds.length > 20 || floatThresHolds[0] == null || floatThresHolds[0].length != 3) {
            AwareLog.w(TAG, "updateFloatThresHold error params");
            return;
        }
        this.mFloatThresHolds = (long[][]) Array.newInstance(Long.TYPE, new int[]{floatThresHolds.length, floatThresHolds[0].length});
        int i = 0;
        while (i < floatThresHolds.length) {
            if (floatThresHolds[i] == null || floatThresHolds[i].length != 3) {
                AwareLog.w(TAG, "updateFloatThresHold error params=" + Arrays.toString(floatThresHolds[i]));
                this.mFloatThresHolds = null;
                break;
            }
            System.arraycopy(floatThresHolds[i], 0, this.mFloatThresHolds[i], 0, floatThresHolds[0].length);
            AwareLog.d(TAG, "updateFloatThresHold thresHolds=" + Arrays.toString(this.mFloatThresHolds[i]));
            i++;
        }
    }

    public void updateProcThresHold(long[][] procThresHolds) {
        if (procThresHolds == null || procThresHolds.length != 1 || procThresHolds[0] == null || procThresHolds[0].length != 2) {
            AwareLog.w(TAG, "updateProcThresHold error params");
            return;
        }
        this.mProcThresHolds = (long[][]) Array.newInstance(Long.TYPE, new int[]{procThresHolds.length, procThresHolds[0].length});
        int i = 0;
        while (i < procThresHolds.length) {
            if (procThresHolds[i] == null || procThresHolds[i].length != 2) {
                AwareLog.w(TAG, "updateProcThresHold error params=" + Arrays.toString(procThresHolds[i]));
                this.mProcThresHolds = null;
                break;
            }
            System.arraycopy(procThresHolds[i], 0, this.mProcThresHolds[i], 0, procThresHolds[0].length);
            AwareLog.d(TAG, "updateProcThresHold thresHolds=" + Arrays.toString(this.mProcThresHolds[i]));
            i++;
        }
    }

    public List<MemRepairPkgInfo> getMemRepairPolicy(int sceneType) {
        if (sceneType != 0 && sceneType != 1) {
            AwareLog.i(TAG, "getMemRepairPolicy invalid param=" + sceneType);
            return null;
        } else if (this.mProcThresHolds == null || this.mFloatThresHolds == null) {
            AwareLog.i(TAG, "getMemRepairPolicy null thresholds!");
            return null;
        } else {
            AwareAppMngSortPolicy policy = MemoryUtils.getAppMngSortPolicyForMemRepair(sceneType);
            if (policy == null) {
                AwareLog.i(TAG, "getMemRepairPolicy null policy!");
                return null;
            }
            List<MemRepairProcInfo> procList = getMemRepairProcList();
            if (procList == null || procList.size() < 1) {
                AwareLog.i(TAG, "getMemRepairPolicy null procList!");
                return null;
            }
            AwareLog.d(TAG, "getMemRepairPolicy procList size=" + procList.size());
            List<MemRepairPkgInfo> mrPkgList = matchAndBuildPkgList(sceneType, policy, procList);
            if (mrPkgList == null || mrPkgList.size() <= 0) {
                mrPkgList = null;
            }
            return mrPkgList;
        }
    }

    private List<MemRepairProcInfo> getMemRepairProcList() {
        Map<String, List<ProcStateData>> pssMap = ProcStateStatisData.getInstance().getPssListMap();
        if (pssMap.size() < 1) {
            AwareLog.d(TAG, "zero pssMap size");
            return null;
        }
        AwareLog.d(TAG, "pssMap size=" + pssMap.size());
        long[] memSets = new long[this.mMaxCollectCount];
        List<MemRepairProcInfo> procList = new ArrayList();
        MemRepairHolder memRepairHolder = new MemRepairHolder(NORMALIZATION_THREASHOLD, this.mMinCollectCount, this.mMaxCollectCount);
        memRepairHolder.updateFloatPercent(this.mDValueFloatPercent);
        for (Entry<String, List<ProcStateData>> entry : pssMap.entrySet()) {
            MemRepairProcInfo procInfo = null;
            for (ProcStateData procStateData : (List) entry.getValue()) {
                if (procStateData != null) {
                    int procState = procStateData.getState();
                    List<Long> procPssList = procStateData.getStatePssList();
                    if (procPssList != null && (ProcStateStatisData.getInstance().isValidProcState(procState) ^ 1) == 0) {
                        boolean isForgroundState = ProcStateStatisData.getInstance().isForgroundState(procState);
                        long emergPss = matchEmergProc(procStateData, isForgroundState);
                        if (emergPss > 0) {
                            procInfo = getNewProcInfo(procList, procInfo, procStateData, (String) entry.getKey(), emergPss, isForgroundState ? 2 : 4);
                            if (procInfo != null) {
                                AwareLog.i(TAG, "proc emergency:" + procStateData.getProcName() + ProcStateStatisData.SEPERATOR_CHAR + ((String) entry.getKey()));
                                break;
                            }
                        } else {
                            int minCount = ProcStateStatisData.getInstance().getMinCount(procState);
                            if (procPssList.size() < minCount || procPssList.size() > memSets.length) {
                                AwareLog.d(TAG, "less/more size:" + procStateData.getProcName() + ProcStateStatisData.SEPERATOR_CHAR + ((String) entry.getKey()) + ",procState=" + procState + ",size=" + procPssList.size());
                            } else {
                                int setsCount = procPssList.size();
                                memSets = getAndUpdateMemSets(procPssList, memSets);
                                long minMem = procStateData.getMinPss();
                                long maxMem = procStateData.getMaxPss();
                                if (estimateMinMaxMemory(minMem, maxMem)) {
                                    memRepairHolder.updateCollectCount(minCount, minCount * 2);
                                    memRepairHolder.updateSrcValue(memSets, setsCount);
                                    if (MemRepairAlgorithm.translateMemRepair(memRepairHolder, this.mMRCallback, null) != 1) {
                                        AwareLog.d(TAG, "memory ok:" + procStateData.getProcName() + ProcStateStatisData.SEPERATOR_CHAR + ((String) entry.getKey()) + ",procState=" + procState + ",size=" + procPssList.size() + ",pssSets=" + Arrays.toString(memSets) + ",minMem=" + minMem + ",maxMem=" + maxMem);
                                    } else {
                                        procInfo = getNewProcInfo(procList, procInfo, procStateData, (String) entry.getKey(), procStateData.getLastPss(), 1);
                                        if (procInfo != null) {
                                            procInfo.addPssSets(memSets, setsCount, procStateData.getState(), procStateData.getMergeCount());
                                            AwareLog.i(TAG, "memory increase:" + procStateData.getProcName() + ProcStateStatisData.SEPERATOR_CHAR + ((String) entry.getKey()) + ",procState=" + procState + ",size=" + procPssList.size() + ",pssSets=" + Arrays.toString(memSets) + ",minMem=" + minMem + ",maxMem=" + maxMem);
                                        }
                                    }
                                } else {
                                    AwareLog.d(TAG, "less min/max-mem:" + procStateData.getProcName() + ProcStateStatisData.SEPERATOR_CHAR + ((String) entry.getKey()) + ",procState=" + procState + ",size=" + procPssList.size() + ",pssSets=" + Arrays.toString(memSets) + ",minMem=" + minMem + ",maxMem=" + maxMem);
                                }
                            }
                        }
                    }
                }
            }
        }
        return procList;
    }

    private long matchEmergProc(ProcStateData procStateData, boolean isForeground) {
        if (matchEmergThreshold((procStateData.getLastPss() * 3) / 2, isForeground) == 0) {
            return 0;
        }
        long pss = getPssByPid(procStateData.getPid());
        AwareLog.i(TAG, "matchEmergProc:" + procStateData.getProcName() + ",lastPss=" + procStateData.getLastPss() + ",curPss=" + pss);
        if (matchEmergThreshold(pss, isForeground) == 0) {
            return 0;
        }
        return pss;
    }

    private long[] getAndUpdateMemSets(List<Long> procPssList, long[] memSets) {
        int setsCount = procPssList.size();
        if (setsCount > memSets.length) {
            memSets = new long[setsCount];
        }
        Arrays.fill(memSets, 0);
        for (int i = 0; i < setsCount; i++) {
            memSets[i] = ((Long) procPssList.get(i)).longValue();
        }
        return memSets;
    }

    private boolean estimateMinMaxMemory(long minMem, long maxMem) {
        if (minMem < 1 || maxMem < 1 || minMem >= maxMem) {
            AwareLog.i(TAG, "Error min/max Mem!! minMem=" + minMem + ",maxMem=" + maxMem);
            return false;
        } else if (this.mFloatThresHolds == null) {
            AwareLog.w(TAG, "estimateMinMaxMemory: why null thresHolds!!");
            return false;
        } else {
            long diff = maxMem - minMem;
            long multi = (100 * diff) / minMem;
            boolean estimated = false;
            int i = 0;
            while (i < this.mFloatThresHolds.length) {
                if (diff < this.mFloatThresHolds[i][0] || diff >= this.mFloatThresHolds[i][1]) {
                    i++;
                } else {
                    if (multi >= this.mFloatThresHolds[i][2]) {
                        estimated = true;
                    }
                    return estimated;
                }
            }
            return estimated;
        }
    }

    private MemRepairProcInfo getNewProcInfo(List<MemRepairProcInfo> procList, MemRepairProcInfo procInfo, ProcStateData procStateData, String procKey, long pss, int type) {
        if (procInfo == null) {
            procInfo = createMRProcInfo(procStateData, procKey, pss, type);
            if (procInfo == null) {
                AwareLog.i(TAG, "null procInfo:" + procStateData.getProcName() + ProcStateStatisData.SEPERATOR_CHAR + procKey);
                return null;
            }
            procList.add(procInfo);
        } else {
            procInfo.updateThresHoldType(type);
        }
        return procInfo;
    }

    private MemRepairProcInfo createMRProcInfo(ProcStateData procStateData, String procKey, long pss, int type) {
        if (TextUtils.isEmpty(procKey)) {
            return null;
        }
        String procName = procStateData.getProcName();
        if (TextUtils.isEmpty(procName)) {
            return null;
        }
        String[] procKeys = procKey.split("\\|");
        if (procKeys.length != 2) {
            return null;
        }
        int uid = -1;
        int pid = 0;
        try {
            uid = Integer.parseInt(procKeys[0]);
            pid = Integer.parseInt(procKeys[1]);
        } catch (NumberFormatException e) {
            AwareLog.w(TAG, "Failed parsing process=" + procName);
        }
        if (pid < 1 || uid < 0) {
            return null;
        }
        MemRepairProcInfo procInfo = new MemRepairProcInfo(uid, pid, procName, pss);
        procInfo.updateThresHoldType(type);
        return procInfo;
    }

    private List<MemRepairPkgInfo> matchAndBuildPkgList(int sceneType, AwareAppMngSortPolicy policy, List<MemRepairProcInfo> mrProcInfoList) {
        List<AwareProcessBlockInfo> forbidStopList = MemoryUtils.getAppMngProcGroup(policy, 0);
        List<AwareProcessBlockInfo> shortageStopList = MemoryUtils.getAppMngProcGroup(policy, 1);
        List<AwareProcessBlockInfo> allowStopList = MemoryUtils.getAppMngProcGroup(policy, 2);
        ArrayMap<String, MemRepairPkgInfo> mrPkgMap = new ArrayMap();
        matchMemLeakPkgList(sceneType, mrPkgMap, mrProcInfoList, forbidStopList, shortageStopList, allowStopList);
        AwareLog.d(TAG, "all pkg list=" + mrPkgMap.toString());
        buildMemLeakPkgList(sceneType, mrPkgMap, mrProcInfoList, forbidStopList, shortageStopList, allowStopList);
        if (mrPkgMap.size() < 1) {
            return null;
        }
        List<MemRepairPkgInfo> mrPkgList = new ArrayList();
        mrPkgList.addAll(0, mrPkgMap.values());
        return mrPkgList;
    }

    private void matchMemLeakPkgList(int sceneType, ArrayMap<String, MemRepairPkgInfo> emergPkgMap, List<MemRepairProcInfo> mrProcInfoList, List<AwareProcessBlockInfo> forbidStopList, List<AwareProcessBlockInfo> shortageStopList, List<AwareProcessBlockInfo> allowStopList) {
        if (mrProcInfoList != null && mrProcInfoList.size() >= 1) {
            Object[] appMngList = new Object[]{forbidStopList, shortageStopList, allowStopList};
            for (List<AwareProcessBlockInfo> pkgList : appMngList) {
                if (!(pkgList == null || pkgList.isEmpty())) {
                    for (MemRepairProcInfo mrProcInfo : mrProcInfoList) {
                        matchMemLeakPkgInfo(sceneType, emergPkgMap, mrProcInfo, pkgList);
                    }
                }
            }
        }
    }

    private void buildMemLeakPkgList(int sceneType, ArrayMap<String, MemRepairPkgInfo> mrPkgMap, List<MemRepairProcInfo> mrProcList, List<AwareProcessBlockInfo> forbidStopList, List<AwareProcessBlockInfo> shortageStopList, List<AwareProcessBlockInfo> allowStopList) {
        if (mrPkgMap != null && mrPkgMap.size() >= 1) {
            Object[] appMngList = new Object[]{forbidStopList, shortageStopList, allowStopList};
            ArrayMap<Integer, MemRepairProcInfo> mrProcMap = new ArrayMap();
            for (MemRepairProcInfo mrProcInfo : mrProcList) {
                mrProcMap.put(Integer.valueOf(mrProcInfo.getPid()), mrProcInfo);
            }
            for (List<AwareProcessBlockInfo> pkgList : appMngList) {
                if (!(pkgList == null || pkgList.isEmpty())) {
                    buildMemLeakPkgInfo(sceneType, mrPkgMap, mrProcMap, pkgList);
                }
            }
        }
    }

    private void matchMemLeakPkgInfo(int sceneType, ArrayMap<String, MemRepairPkgInfo> mrPkgMap, MemRepairProcInfo mrProcInfo, List<AwareProcessBlockInfo> pkgList) {
        for (AwareProcessBlockInfo blockInfo : pkgList) {
            List<AwareProcessInfo> procList = checkAndGetProcList(blockInfo);
            if (procList != null) {
                for (AwareProcessInfo procInfo : procList) {
                    if (checkProcInfo(procInfo) && procInfo.mPid == mrProcInfo.getPid()) {
                        String mrProcName = mrProcInfo.getProcName();
                        if (TextUtils.isEmpty(mrProcName) || (mrProcName.equals(procInfo.mProcInfo.mProcessName) ^ 1) != 0) {
                            AwareLog.i(TAG, "matchMemLeakPkgInfo:while diff procName?! Process=" + procInfo.mProcInfo.mProcessName);
                            return;
                        }
                        String str = null;
                        if (procInfo.mProcInfo.mPackageName.size() > 0) {
                            str = (String) procInfo.mProcInfo.mPackageName.get(0);
                        }
                        if (TextUtils.isEmpty(str)) {
                            AwareLog.i(TAG, "matchMemLeakPkgInfo:while null pkgName?! Process=" + procInfo.mProcInfo.mProcessName);
                            return;
                        }
                        String key = getPkgkey(blockInfo.mUid, str);
                        MemRepairPkgInfo mrPkgInfo = (MemRepairPkgInfo) mrPkgMap.get(key);
                        if (mrPkgInfo == null) {
                            mrPkgInfo = new MemRepairPkgInfo(str);
                            mrPkgMap.put(key, mrPkgInfo);
                        }
                        matchVisibleApp(sceneType, procInfo);
                        mrProcInfo.updateThresHoldType(matchEmergThreshold(mrProcInfo.getPss(), procInfo.isForegroundApp()));
                        mrProcInfo.updateAppMngInfo(procInfo.mCleanType == CleanType.NONE ? 0 : 1, procInfo.isAwareProtected(), procInfo.getProcessStatus(), procInfo.mProcInfo.mCurAdj);
                        mrPkgInfo.addProcInfo(mrProcInfo);
                        AwareLog.d(TAG, "matchMemLeakPkgInfo:" + mrPkgInfo.toString());
                        return;
                    }
                }
                continue;
            }
        }
    }

    private void buildMemLeakPkgInfo(int sceneType, ArrayMap<String, MemRepairPkgInfo> mrPkgMap, ArrayMap<Integer, MemRepairProcInfo> mrProcMap, List<AwareProcessBlockInfo> pkgList) {
        for (AwareProcessBlockInfo blockInfo : pkgList) {
            List<AwareProcessInfo> procList = checkAndGetProcList(blockInfo);
            if (procList != null) {
                for (AwareProcessInfo procInfo : procList) {
                    if (checkProcInfo(procInfo)) {
                        if (mrProcMap.get(Integer.valueOf(procInfo.mPid)) == null) {
                            Object obj = null;
                            if (procInfo.mProcInfo.mPackageName.size() > 0) {
                                obj = (String) procInfo.mProcInfo.mPackageName.get(0);
                            }
                            if (TextUtils.isEmpty(obj)) {
                                AwareLog.i(TAG, "buildMemLeakPkgInfo null pkgName?! Process=" + procInfo.mProcInfo.mProcessName);
                            } else {
                                MemRepairPkgInfo mrPkgInfo = (MemRepairPkgInfo) mrPkgMap.get(getPkgkey(blockInfo.mUid, obj));
                                if (mrPkgInfo != null) {
                                    MemRepairProcInfo mrProcInfo = new MemRepairProcInfo(procInfo.mProcInfo.mUid, procInfo.mProcInfo.mPid, procInfo.mProcInfo.mProcessName, 0);
                                    matchVisibleApp(sceneType, procInfo);
                                    mrProcInfo.updateThresHoldType(0);
                                    mrProcInfo.updateAppMngInfo(procInfo.mCleanType == CleanType.NONE ? 0 : 1, procInfo.isAwareProtected(), procInfo.getProcessStatus(), procInfo.mProcInfo.mCurAdj);
                                    mrPkgInfo.addProcInfo(mrProcInfo);
                                    AwareLog.d(TAG, "buildMemLeakPkgInfo:" + mrPkgInfo.toString());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private List<AwareProcessInfo> checkAndGetProcList(AwareProcessBlockInfo blockInfo) {
        if (blockInfo == null) {
            return null;
        }
        List<AwareProcessInfo> procList = blockInfo.getProcessList();
        if (procList == null || procList.size() < 1) {
            return null;
        }
        return procList;
    }

    private boolean checkProcInfo(AwareProcessInfo procInfo) {
        if (procInfo == null || procInfo.mProcInfo == null || TextUtils.isEmpty(procInfo.mProcInfo.mProcessName)) {
            return false;
        }
        return true;
    }

    private String getPkgkey(int uid, String pkgName) {
        return uid + ProcStateStatisData.SEPERATOR_CHAR + pkgName;
    }

    private int matchEmergThreshold(long pss, boolean isForeground) {
        if (this.mProcThresHolds == null) {
            AwareLog.w(TAG, "matchEmergThreshold: why null thresHolds!!");
            return 0;
        }
        if (this.mProcThresHolds[0][isForeground ? 0 : 1] > pss) {
            return 0;
        }
        return isForeground ? 2 : 4;
    }

    private void matchVisibleApp(int sceneType, AwareProcessInfo procInfo) {
        if (sceneType == 1 && procInfo.isVisibleApp(100)) {
            procInfo.mCleanType = CleanType.KILL_ALLOW_START;
        }
    }

    private long getPssByPid(int pid) {
        if (pid > 0) {
            return Debug.getPss(pid, null, null);
        }
        return 0;
    }
}
