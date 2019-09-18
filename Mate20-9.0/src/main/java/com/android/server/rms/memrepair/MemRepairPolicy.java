package com.android.server.rms.memrepair;

import android.os.Debug;
import android.rms.iaware.AwareLog;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import android.rms.iaware.memrepair.MemRepairProcInfo;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.location.HwLogRecordManager;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.memrepair.MemRepairAlgorithm;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemRepairPolicy {
    private static final long KB2MB_FACTOR = 1024;
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
    public static final int SCENE_TYPE_LOW_MEMORY = 2;
    private static final int SCENE_TYPE_MIDNIGHT = 1;
    private static final int SCENE_TYPE_SCREENOFF = 0;
    private static final String TAG = "AwareMem_MRPolicy";
    private static final int TYPE_EMERG_BG_THRESHOLD = 4;
    private static final int TYPE_EMERG_FG_THRESHOLD = 2;
    public static final int TYPE_FOREGROUND = 8;
    private static final int TYPE_MEM_GROW_UP = 1;
    private static final int TYPE_NONE = 0;
    private static final long VSS_DECENT_PERCENT = 1;
    private static final long VSS_INCREASE_SIZE = 10;
    public static final int VSS_INTERVAL_SIZE = 3;
    private static final long VSS_MAX_THRESHOLD = 4096;
    private static final long VSS_NORMALIZATION_SIZE = 50;
    public static final int VSS_PARAMETER_SIZE = 5;
    private static final int VSS_TYPE_DROP = 1;
    private static final int VSS_TYPE_NO_TREND = 4;
    private static final int VSS_TYPE_PERCENT = 2;
    private static final int VSS_TYPE_THRESHOLD = 5;
    private static final int VSS_TYPE_TREND = 3;
    private static MemRepairPolicy mMemRepairPolicy;
    private int mDValueFloatPercent = 5;
    private long[][] mDefaultProcThresHolds = null;
    private long[][] mFloatThresHolds = null;
    private long[][] mLowMemProcThresHolds = null;
    private AlgoCallback mMRCallback = new AlgoCallback();
    private int mMaxCollectCount = 50;
    private int mMinCollectCount = 6;
    private long[][] mProcThresHolds = null;
    private long mVssInitPercent = 0;
    private long mVssInitStepValue = 0;
    private long[] mVssIntervals = null;
    private long mVssThreshold = 0;

    private static final class AlgoCallback implements MemRepairAlgorithm.MRCallback {
        private AlgoCallback() {
        }

        public int estimateLinear(Object user, MemRepairAlgorithm.CallbackData outData) {
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
        AwareLog.d(TAG, "updateCollectCount fgCount=" + fgCollectCount + ",bgCount=" + bgCollectCount);
        int minCount = fgCollectCount < bgCollectCount ? bgCollectCount : fgCollectCount;
        int i = 6;
        if (minCount >= 6) {
            i = minCount;
        }
        this.mMinCollectCount = i;
        this.mMaxCollectCount = this.mMinCollectCount * 2;
        int i2 = 100;
        if (this.mMaxCollectCount < 100) {
            i2 = this.mMaxCollectCount;
        }
        this.mMaxCollectCount = i2;
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
        this.mFloatThresHolds = (long[][]) Array.newInstance(long.class, new int[]{floatThresHolds.length, floatThresHolds[0].length});
        int i = 0;
        while (true) {
            if (i >= floatThresHolds.length) {
                break;
            } else if (floatThresHolds[i] == null || floatThresHolds[i].length != 3) {
                AwareLog.w(TAG, "updateFloatThresHold error params=" + Arrays.toString(floatThresHolds[i]));
                this.mFloatThresHolds = null;
            } else {
                System.arraycopy(floatThresHolds[i], 0, this.mFloatThresHolds[i], 0, floatThresHolds[0].length);
                AwareLog.d(TAG, "updateFloatThresHold thresHolds=" + Arrays.toString(this.mFloatThresHolds[i]));
                i++;
            }
        }
        AwareLog.w(TAG, "updateFloatThresHold error params=" + Arrays.toString(floatThresHolds[i]));
        this.mFloatThresHolds = null;
    }

    public void updateProcThresHold(long[][] procThresHolds, boolean isCust) {
        AwareLog.d(TAG, "enter updateProcThresHold...isCust=" + isCust);
        if (procThresHolds == null || procThresHolds.length != 1 || procThresHolds[0] == null || procThresHolds[0].length != 2) {
            AwareLog.w(TAG, "updateProcThresHold error params");
            return;
        }
        if (isCust) {
            this.mLowMemProcThresHolds = (long[][]) Array.newInstance(long.class, new int[]{procThresHolds.length, procThresHolds[0].length});
            updateThresHolds(procThresHolds, this.mLowMemProcThresHolds);
        } else {
            this.mDefaultProcThresHolds = (long[][]) Array.newInstance(long.class, new int[]{procThresHolds.length, procThresHolds[0].length});
            updateThresHolds(procThresHolds, this.mDefaultProcThresHolds);
            this.mProcThresHolds = this.mDefaultProcThresHolds;
        }
    }

    private void updateThresHolds(long[][] procThresHolds, long[][] need2ProcThresHolds) {
        for (int i = 0; i < procThresHolds.length; i++) {
            if (procThresHolds[i] == null || procThresHolds[i].length != 2) {
                AwareLog.w(TAG, "updateProcThresHold error params=" + Arrays.toString(procThresHolds[i]));
                return;
            }
            System.arraycopy(procThresHolds[i], 0, need2ProcThresHolds[i], 0, procThresHolds[0].length);
            AwareLog.d(TAG, "updateProcThresHold thresHolds=" + Arrays.toString(need2ProcThresHolds[i]));
        }
    }

    private void changeValue(boolean isLowMem) {
        AwareLog.d(TAG, "isLowMem=" + isLowMem);
        if (this.mLowMemProcThresHolds != null && isLowMem) {
            this.mProcThresHolds = this.mLowMemProcThresHolds;
            AwareLog.d(TAG, "updateProcThresHold thresHolds=" + Arrays.toString(this.mProcThresHolds[0]));
        }
        if (this.mDefaultProcThresHolds != null && !isLowMem) {
            this.mProcThresHolds = this.mDefaultProcThresHolds;
            AwareLog.d(TAG, "updateProcThresHold thresHolds=" + Arrays.toString(this.mProcThresHolds[0]));
        }
    }

    public List<MemRepairPkgInfo> getMemRepairPolicy(int sceneType) {
        if (sceneType != 0 && sceneType != 1 && sceneType != 2) {
            AwareLog.i(TAG, "getMemRepairPolicy invalid param=" + sceneType);
            return null;
        } else if (sceneType == 2 && this.mLowMemProcThresHolds == null) {
            AwareLog.i(TAG, "mLowMemProcThresHolds is null , sceneType=" + sceneType);
            return null;
        } else {
            long availableRam = MemoryReader.getInstance().getMemAvailable();
            if (availableRam <= 0) {
                AwareLog.e(TAG, "execute faild to read availableRam =" + availableRam);
                return null;
            }
            if (availableRam <= MemoryConstant.getCriticalMemory()) {
                changeValue(true);
            } else {
                changeValue(false);
            }
            if (this.mProcThresHolds != null && this.mFloatThresHolds != null) {
                return mergePssAndVss(sceneType);
            }
            AwareLog.i(TAG, "getMemRepairPolicy null thresholds!");
            return null;
        }
    }

    private List<MemRepairPkgInfo> getMemRepairPkgInfo(AwareAppMngSortPolicy policy, int sceneType, boolean isPss) {
        List<MemRepairProcInfo> procList = getMemRepairProcList(isPss);
        List<MemRepairPkgInfo> list = null;
        if (procList == null) {
            return null;
        }
        List<MemRepairPkgInfo> mrPkgList = matchAndBuildPkgList(sceneType, policy, procList, isPss);
        if (mrPkgList != null && mrPkgList.size() > 0) {
            list = mrPkgList;
        }
        return list;
    }

    private List<MemRepairPkgInfo> mergePssAndVss(int sceneType) {
        AwareAppMngSortPolicy pssPolicy = MemoryUtils.getAppMngSortPolicyForMemRepair(sceneType);
        if (pssPolicy == null) {
            AwareLog.i(TAG, "getMemRepairPolicy null policy!");
            return null;
        }
        List<MemRepairPkgInfo> pssList = getMemRepairPkgInfo(pssPolicy, sceneType, true);
        AwareLog.d(TAG, "vss para: " + this.mVssThreshold + "|" + this.mVssInitPercent + "|" + this.mVssInitStepValue + "|" + Arrays.toString(this.mVssIntervals));
        if (this.mVssThreshold <= 0 || this.mVssInitPercent <= 0 || this.mVssInitStepValue <= 0 || this.mVssIntervals == null) {
            AwareLog.w(TAG, "Get vss parameters failed");
            return pssList;
        }
        AwareAppMngSortPolicy vssPolicy = MemoryUtils.getAppSortPolicyForMemRepairVss(sceneType, pssPolicy);
        if (vssPolicy == null) {
            AwareLog.i(TAG, "getMemRepairVssPolicy null policy!");
            return null;
        }
        List<MemRepairPkgInfo> vssList = getMemRepairPkgInfo(vssPolicy, sceneType, false);
        if (pssList == null) {
            return vssList;
        }
        if (vssList == null) {
            return pssList;
        }
        pssList.addAll(vssList);
        return pssList;
    }

    private List<MemRepairProcInfo> getMemRepairProcList(boolean isPss) {
        Iterator<Map.Entry<String, List<ProcStateData>>> it;
        MemRepairAlgorithm.MemRepairHolder holder;
        Map<String, List<ProcStateData>> memMap;
        boolean z;
        int normThreshold;
        MemRepairPolicy memRepairPolicy = this;
        boolean z2 = isPss;
        Map<String, List<ProcStateData>> memMap2 = getMemMap(isPss);
        boolean z3 = true;
        if (memMap2.size() < 1) {
            AwareLog.d(TAG, "isPss:" + z2 + " ,zero memMap size");
            return null;
        }
        AwareLog.d(TAG, "isPss:" + z2 + " ,memMap size=" + memMap2.size());
        int ret = 0;
        long[] memSets = new long[memRepairPolicy.mMaxCollectCount];
        List<MemRepairProcInfo> procList = new ArrayList<>();
        int normThreshold2 = getNormThreshold(isPss);
        MemRepairAlgorithm.MemRepairHolder holder2 = new MemRepairAlgorithm.MemRepairHolder(normThreshold2, memRepairPolicy.mMinCollectCount, memRepairPolicy.mMaxCollectCount);
        holder2.updateFloatPercent(memRepairPolicy.mDValueFloatPercent);
        Iterator<Map.Entry<String, List<ProcStateData>>> it2 = memMap2.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry<String, List<ProcStateData>> entry = it2.next();
            Iterator it3 = entry.getValue().iterator();
            int ret2 = ret;
            MemRepairProcInfo procInfo = null;
            while (true) {
                if (it3.hasNext() == 0) {
                    it = it2;
                    holder = holder2;
                    memMap = memMap2;
                    z = z3;
                    normThreshold = normThreshold2;
                    break;
                }
                ProcStateData procStateData = (ProcStateData) it3.next();
                if (memRepairPolicy.checkProcStateData(procStateData)) {
                    int procState = procStateData.getState();
                    List<Long> procMemList = procStateData.getStateMemList();
                    boolean isForgroundState = ProcStateStatisData.getInstance().isForgroundState(procState);
                    long emergMem = memRepairPolicy.matchEmergProc(procStateData, isForgroundState);
                    if (emergMem > 0) {
                        boolean z4 = isForgroundState;
                        List<Long> list = procMemList;
                        int i = procState;
                        Iterator it4 = it3;
                        memMap = memMap2;
                        Map.Entry<String, List<ProcStateData>> entry2 = entry;
                        it = it2;
                        holder = holder2;
                        procInfo = memRepairPolicy.getNewProcInfo(procList, procInfo, procStateData, entry.getKey(), emergMem, isForgroundState ? 2 : 4, z2);
                        if (procInfo != null) {
                            AwareLog.i(TAG, "proc emergency:" + procStateData.getProcName() + "|" + entry2.getKey() + "|" + z2);
                            normThreshold = normThreshold2;
                            z = true;
                            break;
                        }
                        entry = entry2;
                        it3 = it4;
                        holder2 = holder;
                        memMap2 = memMap;
                        it2 = it;
                        z3 = true;
                    } else {
                        List<Long> procMemList2 = procMemList;
                        int procState2 = procState;
                        ProcStateData procStateData2 = procStateData;
                        Iterator it5 = it3;
                        Iterator<Map.Entry<String, List<ProcStateData>>> it6 = it2;
                        MemRepairAlgorithm.MemRepairHolder holder3 = holder2;
                        Map<String, List<ProcStateData>> memMap3 = memMap2;
                        Map.Entry<String, List<ProcStateData>> entry3 = entry;
                        int ret3 = memRepairPolicy.applyMemRpairAlgorithm(holder3, procStateData2, procMemList2, memSets, z2);
                        if (ret3 != 1) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("memory ok:");
                            sb.append(procStateData2.getProcName());
                            sb.append("|");
                            sb.append(entry3.getKey());
                            sb.append(",procState=");
                            sb.append(procState2);
                            sb.append(",isPss=");
                            sb.append(procStateData2.isPss());
                            sb.append(",size=");
                            List<Long> procMemList3 = procMemList2;
                            sb.append(procMemList3.size());
                            sb.append(",procMemList=");
                            sb.append(Arrays.toString(procMemList3.toArray()));
                            sb.append(",memSets=");
                            sb.append(Arrays.toString(memSets));
                            sb.append(",initMem=");
                            sb.append(procStateData2.getInitMem());
                            sb.append(",minMem=");
                            sb.append(procStateData2.getMinMem());
                            sb.append(",maxMem=");
                            sb.append(procStateData2.getMaxMem());
                            AwareLog.d(TAG, sb.toString());
                            z3 = true;
                            ret2 = ret3;
                            entry = entry3;
                            it3 = it5;
                            holder2 = holder3;
                            memMap2 = memMap3;
                            it2 = it6;
                        } else {
                            int normThreshold3 = normThreshold2;
                            List<Long> procMemList4 = procMemList2;
                            ret2 = ret3;
                            MemRepairProcInfo procInfo2 = memRepairPolicy.getNewProcInfo(procList, procInfo, procStateData2, entry3.getKey(), procStateData2.getLastMem(), 1, z2);
                            if (procInfo2 != null) {
                                procInfo2.addMemSets(memSets, procMemList4.size(), procStateData2.getState(), procStateData2.getMergeCount());
                                AwareLog.i(TAG, "memory increase:" + procStateData2.getProcName() + "|" + entry3.getKey() + ",procState=" + procState + ",isPss=" + procStateData2.isPss() + ",size=" + procMemList4.size() + ",procMemList=" + Arrays.toString(procMemList4.toArray()) + ",memSets=" + Arrays.toString(memSets) + ",initMem=" + procStateData2.getInitMem() + ",minMem=" + procStateData2.getMinMem() + ",maxMem=" + procStateData2.getMaxMem());
                            }
                            procInfo = procInfo2;
                            entry = entry3;
                            it3 = it5;
                            holder2 = holder3;
                            z3 = true;
                            memMap2 = memMap3;
                            it2 = it6;
                            normThreshold2 = normThreshold3;
                            memRepairPolicy = this;
                        }
                    }
                }
            }
            ret = ret2;
            holder2 = holder;
            z3 = z;
            memMap2 = memMap;
            it2 = it;
            normThreshold2 = normThreshold;
            memRepairPolicy = this;
        }
        Map<String, List<ProcStateData>> map = memMap2;
        int i2 = normThreshold2;
        return procList;
    }

    private boolean checkProcStateData(ProcStateData procStateData) {
        if (procStateData == null || procStateData.getStateMemList() == null || !ProcStateStatisData.getInstance().isValidProcState(procStateData.getState())) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0052  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0032  */
    private int applyMemRpairAlgorithm(MemRepairAlgorithm.MemRepairHolder holder, ProcStateData procStateData, List<Long> procMemList, long[] memSets, boolean isPss) {
        long[] jArr;
        int i;
        int ret;
        int setsCount = procMemList.size();
        int procState = procStateData.getState();
        int minCount = ProcStateStatisData.getInstance().getMinCount(procState);
        long minMem = procStateData.getMinMem();
        long maxMem = procStateData.getMaxMem();
        long initMem = procStateData.getInitMem();
        boolean z = false;
        int i2 = 1;
        if (setsCount >= minCount) {
            jArr = memSets;
            if (setsCount <= jArr.length) {
                i = true;
                int canPredictIncrease = i;
                if (isPss) {
                    long j = minMem;
                    boolean canPredictIncrease2 = canPredictIncrease;
                    long initMem2 = initMem;
                    int pid = procStateData.getPid();
                    int type = getVssIntervalType(initMem2);
                    boolean z2 = type == 2;
                    if (type == 3) {
                        z = true;
                    }
                    long j2 = initMem2;
                    boolean needNext = z2 | z;
                    int pid2 = pid;
                    boolean isRuleSatisfied = estimateVssInterval(type, initMem2, maxMem, pid);
                    StringBuilder sb = new StringBuilder();
                    sb.append("pid:");
                    int pid3 = pid2;
                    sb.append(pid3);
                    sb.append(" |type:");
                    sb.append(type);
                    sb.append(" |predictIncrease:");
                    sb.append(canPredictIncrease2);
                    sb.append(" |needNext:");
                    sb.append(needNext);
                    sb.append(" |isRuleSatisfied:");
                    sb.append(isRuleSatisfied);
                    AwareLog.d(TAG, sb.toString());
                    if (!isRuleSatisfied || !needNext) {
                        int i3 = pid3;
                        if (!needNext) {
                            if (!isRuleSatisfied) {
                                i2 = 6;
                            }
                            ret = i2;
                        } else {
                            ret = 6;
                        }
                    } else if (!canPredictIncrease2) {
                        return 6;
                    } else {
                        int i4 = pid3;
                        ret = memIncreaseJudge(holder, procMemList, memSets, minCount, setsCount);
                    }
                } else if (canPredictIncrease != true || !estimateMinMaxMemory(minMem, maxMem)) {
                    return 6;
                } else {
                    int i5 = procState;
                    int procState2 = canPredictIncrease;
                    long j3 = minMem;
                    ret = memIncreaseJudge(holder, procMemList, jArr, minCount, setsCount);
                    long j4 = initMem;
                }
                return ret;
            }
        } else {
            jArr = memSets;
        }
        i = false;
        int canPredictIncrease3 = i;
        if (isPss) {
        }
        return ret;
    }

    private int memIncreaseJudge(MemRepairAlgorithm.MemRepairHolder holder, List<Long> procMemList, long[] memSets, int minCount, int setsCount) {
        long[] memSets2 = getAndUpdateMemSets(procMemList, memSets);
        holder.updateCollectCount(minCount, minCount * 2);
        holder.updateSrcValue(memSets2, setsCount);
        return MemRepairAlgorithm.translateMemRepair(holder, this.mMRCallback, null);
    }

    private Map<String, List<ProcStateData>> getMemMap(boolean isPss) {
        if (isPss) {
            return ProcStateStatisData.getInstance().getPssListMap();
        }
        return ProcStateStatisData.getInstance().getVssListMap();
    }

    private int getNormThreshold(boolean isPss) {
        return isPss ? 5120 : 15360;
    }

    private long matchEmergProc(ProcStateData procStateData, boolean isForeground) {
        if (procStateData.isPss()) {
            return matchEmergProcPss(procStateData, isForeground);
        }
        return matchEmergProcVss(procStateData);
    }

    private long matchEmergProcPss(ProcStateData procStateData, boolean isForeground) {
        if (matchEmergPssThreshold((procStateData.getLastMem() * 3) / 2, isForeground) == 0) {
            return 0;
        }
        long pss = getPssByPid(procStateData.getPid());
        AwareLog.i(TAG, "matchEmergProc:" + procStateData.getProcName() + ",lastPss=" + procStateData.getLastMem() + ",curPss=" + pss);
        if (matchEmergPssThreshold(pss, isForeground) == 0) {
            return 0;
        }
        return pss;
    }

    private long matchEmergProcVss(ProcStateData procStateData) {
        long val = getVssByPid(procStateData.getPid());
        AwareLog.i(TAG, "matchEmergProc:" + procStateData + ",curVss" + val + "|" + this.mVssThreshold);
        return val > this.mVssThreshold ? val : 0;
    }

    private long[] getAndUpdateMemSets(List<Long> procPssList, long[] memSets) {
        int setsCount = procPssList.size();
        if (setsCount > memSets.length) {
            memSets = new long[setsCount];
        }
        Arrays.fill(memSets, 0);
        for (int i = 0; i < setsCount; i++) {
            memSets[i] = procPssList.get(i).longValue();
        }
        return memSets;
    }

    private boolean estimateVssInterval(int type, long initMem, long maxMem, int pid) {
        int i = type;
        long j = initMem;
        long j2 = maxMem;
        int i2 = pid;
        boolean z = false;
        if (!isParametersValid(j, j2, i2)) {
            return false;
        }
        boolean result = false;
        AwareLog.d(TAG, "estimateVssInterval: " + i + "|" + j + "|" + j2 + "|" + i2);
        switch (i) {
            case 1:
                result = false;
                AwareLog.d(TAG, "VSS_TYPE_DROP: " + false);
                break;
            case 2:
                long thresPercent = this.mVssInitPercent - ((j - this.mVssIntervals[0]) / 51200);
                long realPercent = (100 * (j2 - j)) / j;
                if (thresPercent > 0 && realPercent >= thresPercent) {
                    z = true;
                }
                result = z;
                AwareLog.d(TAG, "VSS_TYPE_PERCENT: " + result + "|" + thresPercent + "|" + realPercent);
                break;
            case 3:
                long thresValue = ((j - this.mVssIntervals[1]) / 5) + this.mVssIntervals[2];
                result = checkIsInRange(thresValue > this.mVssThreshold ? this.mVssThreshold : thresValue, j2);
                AwareLog.d(TAG, "VSS_TYPE_TREND: " + result + "|" + thresValue + "|" + j2);
                break;
            case 4:
                long thresValue2 = ((this.mVssInitStepValue * KB2MB_FACTOR) - ((j - this.mVssIntervals[2]) / 5)) + j;
                result = checkIsInRange(thresValue2 > this.mVssThreshold ? this.mVssThreshold : thresValue2, j2);
                AwareLog.d(TAG, "VSS_TYPE_NO_TREND: " + result + "|" + thresValue + "|" + j2);
                break;
            case 5:
                if (getVssByPid(i2) > this.mVssThreshold) {
                    z = true;
                }
                result = z;
                AwareLog.d(TAG, "VSS_TYPE_THRESHOLD: " + result + "|" + curPss);
                break;
            default:
                AwareLog.d(TAG, "VSS_TYPE_ERROR: " + i + "|" + j + "|" + j2 + "|" + i2);
                break;
        }
        return result;
    }

    private boolean isParametersValid(long initMem, long maxMem, int pid) {
        return initMem > 0 && pid > 0 && maxMem >= initMem;
    }

    private boolean checkIsInRange(long thresValue, long maxMem) {
        return 0 < thresValue && thresValue < maxMem;
    }

    private int getVssIntervalType(long initMem) {
        if (initMem <= 0) {
            return 0;
        }
        if (initMem < this.mVssIntervals[0]) {
            return 1;
        }
        if (initMem < this.mVssIntervals[1]) {
            return 2;
        }
        if (initMem < this.mVssIntervals[2]) {
            return 3;
        }
        if (initMem < this.mVssThreshold) {
            return 4;
        }
        if (initMem < Long.MAX_VALUE) {
            return 5;
        }
        return 0;
    }

    private boolean estimateMinMaxMemory(long minMem, long maxMem) {
        if (minMem < VSS_DECENT_PERCENT || maxMem < VSS_DECENT_PERCENT || minMem >= maxMem) {
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
            while (true) {
                if (i >= this.mFloatThresHolds.length) {
                    break;
                } else if (diff < this.mFloatThresHolds[i][0] || diff >= this.mFloatThresHolds[i][1]) {
                    i++;
                } else if (multi >= this.mFloatThresHolds[i][2]) {
                    estimated = true;
                }
            }
            return estimated;
        }
    }

    private MemRepairProcInfo getNewProcInfo(List<MemRepairProcInfo> procList, MemRepairProcInfo procInfo, ProcStateData procStateData, String procKey, long pss, int type, boolean isPss) {
        if (procInfo == null) {
            procInfo = createMRProcInfo(procStateData, procKey, pss, type, isPss);
            if (procInfo == null) {
                AwareLog.i(TAG, "null procInfo:" + procStateData.getProcName() + "|" + procKey);
                return null;
            }
            procList.add(procInfo);
        } else {
            procInfo.updateThresHoldType(type);
        }
        return procInfo;
    }

    private MemRepairProcInfo createMRProcInfo(ProcStateData procStateData, String procKey, long mem, int type, boolean isPss) {
        int uid;
        int pid;
        if (TextUtils.isEmpty(procKey)) {
            return null;
        }
        String procName = procStateData.getProcName();
        if (TextUtils.isEmpty(procName)) {
            return null;
        }
        String[] procKeys = procKey.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE);
        if (procKeys.length != 2) {
            return null;
        }
        try {
            int uid2 = Integer.parseInt(procKeys[0]);
            pid = Integer.parseInt(procKeys[1]);
            uid = uid2;
        } catch (NumberFormatException e) {
            AwareLog.w(TAG, "Failed parsing process=" + procName);
            uid = -1;
            pid = 0;
        }
        if (pid < 1 || uid < 0) {
            int i = type;
            return null;
        }
        MemRepairProcInfo procInfo = MemRepairProcInfo.createMemRepairProcInfo(uid, pid, procName, mem, isPss);
        procInfo.updateThresHoldType(type);
        return procInfo;
    }

    private List<MemRepairPkgInfo> matchAndBuildPkgList(int sceneType, AwareAppMngSortPolicy policy, List<MemRepairProcInfo> mrProcInfoList, boolean isPss) {
        AwareAppMngSortPolicy awareAppMngSortPolicy = policy;
        List<AwareProcessBlockInfo> forbidStopList = MemoryUtils.getAppMngProcGroup(awareAppMngSortPolicy, 0);
        List<AwareProcessBlockInfo> shortageStopList = MemoryUtils.getAppMngProcGroup(awareAppMngSortPolicy, 1);
        List<AwareProcessBlockInfo> allowStopList = MemoryUtils.getAppMngProcGroup(awareAppMngSortPolicy, 2);
        ArrayMap<String, MemRepairPkgInfo> mrPkgMap = new ArrayMap<>();
        List<MemRepairProcInfo> list = mrProcInfoList;
        List<AwareProcessBlockInfo> list2 = forbidStopList;
        List<AwareProcessBlockInfo> list3 = shortageStopList;
        List<AwareProcessBlockInfo> list4 = allowStopList;
        matchMemLeakPkgList(sceneType, mrPkgMap, list, list2, list3, list4);
        AwareLog.d(TAG, "all pkg list=" + mrPkgMap.toString());
        buildMemLeakPkgList(sceneType, mrPkgMap, list, list2, list3, list4, isPss);
        if (mrPkgMap.size() < 1) {
            return null;
        }
        List<MemRepairPkgInfo> mrPkgList = new ArrayList<>();
        mrPkgList.addAll(0, mrPkgMap.values());
        return mrPkgList;
    }

    private void matchMemLeakPkgList(int sceneType, ArrayMap<String, MemRepairPkgInfo> emergPkgMap, List<MemRepairProcInfo> mrProcInfoList, List<AwareProcessBlockInfo> forbidStopList, List<AwareProcessBlockInfo> shortageStopList, List<AwareProcessBlockInfo> allowStopList) {
        if (mrProcInfoList != null && mrProcInfoList.size() >= 1) {
            int i = 0;
            Object[] appMngList = {forbidStopList, shortageStopList, allowStopList};
            while (true) {
                int i2 = i;
                if (i2 < appMngList.length) {
                    List<AwareProcessBlockInfo> pkgList = (List) appMngList[i2];
                    if (pkgList != null && !pkgList.isEmpty()) {
                        for (MemRepairProcInfo mrProcInfo : mrProcInfoList) {
                            matchMemLeakPkgInfo(sceneType, emergPkgMap, mrProcInfo, pkgList);
                        }
                    }
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
    }

    private void buildMemLeakPkgList(int sceneType, ArrayMap<String, MemRepairPkgInfo> mrPkgMap, List<MemRepairProcInfo> mrProcList, List<AwareProcessBlockInfo> forbidStopList, List<AwareProcessBlockInfo> shortageStopList, List<AwareProcessBlockInfo> allowStopList, boolean isPss) {
        if (mrPkgMap != null && mrPkgMap.size() >= 1) {
            int i = 0;
            Object[] appMngList = {forbidStopList, shortageStopList, allowStopList};
            ArrayMap<Integer, MemRepairProcInfo> mrProcMap = new ArrayMap<>();
            for (MemRepairProcInfo mrProcInfo : mrProcList) {
                mrProcMap.put(Integer.valueOf(mrProcInfo.getPid()), mrProcInfo);
            }
            while (true) {
                int i2 = i;
                if (i2 < appMngList.length) {
                    List<AwareProcessBlockInfo> pkgList = (List) appMngList[i2];
                    if (pkgList != null && !pkgList.isEmpty()) {
                        buildMemLeakPkgInfo(sceneType, mrPkgMap, mrProcMap, pkgList, isPss);
                    }
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v16, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v13, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void matchMemLeakPkgInfo(int sceneType, ArrayMap<String, MemRepairPkgInfo> mrPkgMap, MemRepairProcInfo mrProcInfo, List<AwareProcessBlockInfo> pkgList) {
        MemRepairProcInfo memRepairProcInfo = mrProcInfo;
        for (AwareProcessBlockInfo blockInfo : pkgList) {
            List<AwareProcessInfo> procList = checkAndGetProcList(blockInfo, mrProcInfo.isPss());
            if (procList != null) {
                Set<Integer> forePids = getForeGroundApp();
                for (AwareProcessInfo procInfo : procList) {
                    if (checkProcInfo(procInfo) && procInfo.mPid == mrProcInfo.getPid()) {
                        String mrProcName = mrProcInfo.getProcName();
                        if (TextUtils.isEmpty(mrProcName) || !mrProcName.equals(procInfo.mProcInfo.mProcessName)) {
                            int i = sceneType;
                            AwareLog.i(TAG, "matchMemLeakPkgInfo:while diff procName?! Process=" + procInfo.mProcInfo.mProcessName);
                            return;
                        }
                        String pkgName = null;
                        if (procInfo.mProcInfo.mPackageName.size() > 0) {
                            pkgName = procInfo.mProcInfo.mPackageName.get(0);
                        }
                        String pkgName2 = pkgName;
                        if (TextUtils.isEmpty(pkgName2)) {
                            AwareLog.i(TAG, "matchMemLeakPkgInfo:while null pkgName?! Process=" + procInfo.mProcInfo.mProcessName);
                            return;
                        }
                        MemRepairPkgInfo mrPkgInfo = updatePackageInfo(mrPkgMap, getPkgkey(blockInfo.mUid, pkgName2), pkgName2, forePids, procInfo);
                        matchVisibleApp(sceneType, procInfo);
                        memRepairProcInfo.updateThresHoldType(matchEmergThreshold(mrProcInfo.getMem(), procInfo.isForegroundApp(), mrProcInfo.isPss()));
                        updateMemRepairProc(memRepairProcInfo, procInfo);
                        mrPkgInfo.addProcInfo(memRepairProcInfo);
                        AwareLog.d(TAG, "matchMemLeakPkgInfo:" + mrPkgInfo.toString());
                        return;
                    }
                }
                int i2 = sceneType;
            }
        }
        int i3 = sceneType;
    }

    private MemRepairPkgInfo updatePackageInfo(ArrayMap<String, MemRepairPkgInfo> mrPkgMap, String key, String pkgName, Set<Integer> forePids, AwareProcessInfo procInfo) {
        MemRepairPkgInfo mrPkgInfo = mrPkgMap.get(key);
        if (mrPkgInfo == null) {
            mrPkgInfo = new MemRepairPkgInfo(pkgName);
            mrPkgMap.put(key, mrPkgInfo);
        }
        if (forePids.contains(Integer.valueOf(procInfo.mPid))) {
            mrPkgInfo.updateThresHoldType(8);
        }
        return mrPkgInfo;
    }

    private List<AwareProcessBlockInfo> getAwareProcessBlockInfos(AwareAppMngSortPolicy policy) {
        List<AwareProcessBlockInfo> forbidStopList = MemoryUtils.getAppMngProcGroup(policy, 0);
        List<AwareProcessBlockInfo> allowStopList = MemoryUtils.getAppMngProcGroup(policy, 2);
        List<AwareProcessBlockInfo> totalBlockInfoList = new ArrayList<>();
        totalBlockInfoList.addAll(forbidStopList);
        totalBlockInfoList.addAll(allowStopList);
        return totalBlockInfoList;
    }

    private boolean checkInleakPkgInfo(MemRepairProcInfo mrProcInfo, List<MemRepairProcInfo> pkgList) {
        if (mrProcInfo == null || pkgList == null || pkgList.size() < 1) {
            return false;
        }
        for (MemRepairProcInfo pkgInfo : pkgList) {
            if (mrProcInfo.getPid() == pkgInfo.getPid()) {
                AwareLog.d(TAG, "pid=" + mrProcInfo.getPid());
                return true;
            }
        }
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v12, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v18, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void buildMemLeakPkgInfo(int sceneType, ArrayMap<String, MemRepairPkgInfo> mrPkgMap, ArrayMap<Integer, MemRepairProcInfo> mrProcMap, List<AwareProcessBlockInfo> pkgList, boolean isPss) {
        Iterator<AwareProcessBlockInfo> it;
        MemRepairPkgInfo mrPkgInfo;
        Iterator<AwareProcessBlockInfo> it2 = pkgList.iterator();
        while (it2.hasNext()) {
            AwareProcessBlockInfo blockInfo = it2.next();
            boolean z = isPss;
            List<AwareProcessInfo> procList = checkAndGetProcList(blockInfo, z);
            if (procList != null) {
                Set<Integer> forePids = getForeGroundApp();
                for (AwareProcessInfo procInfo : procList) {
                    if (!checkProcInfo(procInfo)) {
                        ArrayMap<Integer, MemRepairProcInfo> arrayMap = mrProcMap;
                    } else {
                        if (mrProcMap.get(Integer.valueOf(procInfo.mPid)) == null) {
                            String pkgName = null;
                            if (procInfo.mProcInfo.mPackageName.size() > 0) {
                                pkgName = procInfo.mProcInfo.mPackageName.get(0);
                            }
                            String pkgName2 = pkgName;
                            if (TextUtils.isEmpty(pkgName2)) {
                                AwareLog.i(TAG, "buildMemLeakPkgInfo null pkgName?! Process=" + procInfo.mProcInfo.mProcessName);
                            } else {
                                String key = getPkgkey(blockInfo.mUid, pkgName2);
                                MemRepairPkgInfo mrPkgInfo2 = mrPkgMap.get(key);
                                if (mrPkgInfo2 == null) {
                                    int i = sceneType;
                                    it = it2;
                                    mrPkgInfo = mrPkgInfo2;
                                } else {
                                    if (forePids.contains(Integer.valueOf(procInfo.mPid))) {
                                        mrPkgInfo2.updateThresHoldType(8);
                                    }
                                    it = it2;
                                    mrPkgInfo = mrPkgInfo2;
                                    String str = key;
                                    String str2 = pkgName2;
                                    MemRepairProcInfo mrProcInfo = MemRepairProcInfo.createMemRepairProcInfo(procInfo.mProcInfo.mUid, procInfo.mProcInfo.mPid, procInfo.mProcInfo.mProcessName, 0, z);
                                    matchVisibleApp(sceneType, procInfo);
                                    mrProcInfo.updateThresHoldType(0);
                                    updateMemRepairProc(mrProcInfo, procInfo);
                                    mrPkgInfo.addProcInfo(mrProcInfo);
                                    AwareLog.d(TAG, "buildMemLeakPkgInfo:" + mrPkgInfo.toString());
                                }
                                MemRepairPkgInfo memRepairPkgInfo = mrPkgInfo;
                                it2 = it;
                            }
                        }
                    }
                }
                int i2 = sceneType;
                ArrayMap<Integer, MemRepairProcInfo> arrayMap2 = mrProcMap;
                Iterator<AwareProcessBlockInfo> it3 = it2;
            }
        }
        int i3 = sceneType;
        ArrayMap<Integer, MemRepairProcInfo> arrayMap3 = mrProcMap;
        boolean z2 = isPss;
    }

    private void updateMemRepairProc(MemRepairProcInfo info, AwareProcessInfo apInfo) {
        info.updateAppMngInfo(apInfo.mCleanType == ProcessCleaner.CleanType.NONE ? 0 : 1, apInfo.isAwareProtected(), apInfo.getProcessStatus(), apInfo.mProcInfo.mCurAdj);
    }

    private List<AwareProcessInfo> checkAndGetProcList(AwareProcessBlockInfo blockInfo, boolean isPss) {
        if (blockInfo == null) {
            return null;
        }
        List<AwareProcessInfo> procList = blockInfo.getProcessList();
        if (procList == null || procList.size() < 1) {
            return null;
        }
        if (isPss || !blockInfo.mReason.contains("TYPE:9")) {
            return procList;
        }
        return null;
    }

    private boolean checkProcInfo(AwareProcessInfo procInfo) {
        if (procInfo == null || procInfo.mProcInfo == null || TextUtils.isEmpty(procInfo.mProcInfo.mProcessName)) {
            return false;
        }
        return true;
    }

    private Set<Integer> getForeGroundApp() {
        Set<Integer> forePids = new ArraySet<>();
        AwareAppAssociate.getInstance().getForeGroundApp(forePids);
        return forePids;
    }

    private String getPkgkey(int uid, String pkgName) {
        return uid + "|" + pkgName;
    }

    private int matchEmergThreshold(long val, boolean isForeground, boolean isPss) {
        if (isPss) {
            return matchEmergPssThreshold(val, isForeground);
        }
        return matchEmergVssThreshold(val, isForeground);
    }

    private int matchEmergPssThreshold(long pss, boolean isForeground) {
        if (this.mProcThresHolds == null) {
            AwareLog.w(TAG, "matchEmergThreshold: why null thresHolds!!");
            return 0;
        }
        if (this.mProcThresHolds[0][!isForeground] > pss) {
            return 0;
        }
        return isForeground ? 2 : 4;
    }

    private int matchEmergVssThreshold(long vss, boolean isForeground) {
        if (this.mVssThreshold > vss) {
            return 0;
        }
        return isForeground ? 2 : 4;
    }

    private void matchVisibleApp(int sceneType, AwareProcessInfo procInfo) {
        if (sceneType == 1 && procInfo.isVisibleApp(100)) {
            procInfo.mCleanType = ProcessCleaner.CleanType.KILL_ALLOW_START;
        }
    }

    private long getPssByPid(int pid) {
        if (pid > 0) {
            return Debug.getPss(pid, null, null);
        }
        return 0;
    }

    public void updateVssThreshold(long vssThreshold) {
        if (0 < vssThreshold && vssThreshold < VSS_MAX_THRESHOLD) {
            this.mVssThreshold = KB2MB_FACTOR * vssThreshold;
        }
        AwareLog.d(TAG, "updateVssThreshold: " + this.mVssThreshold);
    }

    public void updateVssParameters(long[] fields) {
        if (fields != null && fields.length == 5) {
            long j = 0;
            this.mVssInitPercent = (0 >= fields[0] || fields[0] >= 100) ? 0 : fields[0];
            this.mVssInitStepValue = fields[1] > 0 ? fields[1] : 0;
            if (fields[2] > 0) {
                j = fields[2];
            }
            long dropThreshold = j;
            this.mVssIntervals = createVssIntervals();
            long lastValue = 0;
            for (int i = 0; i < this.mVssIntervals.length; i++) {
                long current = fields[i + 2] * KB2MB_FACTOR;
                if (current <= lastValue) {
                    this.mVssIntervals = null;
                    return;
                }
                this.mVssIntervals[i] = current;
                lastValue = current;
            }
            if (lastValue > this.mVssThreshold) {
                this.mVssIntervals = null;
                return;
            }
            AwareLog.d(TAG, "updateVssParameters: " + this.mVssInitPercent + "|" + this.mVssInitStepValue + "|" + Arrays.toString(this.mVssIntervals));
            ProcStateStatisData.getInstance().updateDropThreshold(KB2MB_FACTOR * dropThreshold);
        }
    }

    private long[] createVssIntervals() {
        if (this.mVssIntervals == null) {
            this.mVssIntervals = new long[3];
        }
        return this.mVssIntervals;
    }

    private long getVssByPid(int pid) {
        if (pid > 0) {
            return MemoryCollector.getVSS(pid);
        }
        return 0;
    }
}
