package com.huawei.server.rme.hyperhold;

import android.content.Context;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.SystemClock;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.memory.policy.CachedMemoryCleanPolicy;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.android.server.rms.iaware.memory.utils.PackageTracker;
import com.huawei.android.os.DebugExt;
import com.huawei.android.util.SlogEx;
import com.huawei.server.rme.collector.ResourceCollector;
import com.huawei.server.rme.hyperhold.ParaConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.LongStream;

public class AdvancedKiller extends DefaultAdvancedKiller {
    private final int ANON_MIN_SIZE_KB = 4096;
    private final int BLOCK_HIGH_WEIGHT = 100;
    private final int COMPRESSION_RATIO = 3;
    private final boolean DEFAULT_QUICKKILL_MODE = false;
    private final String DEFAULT_REASON = MemoryConstant.MEM_SCENE_DEFAULT;
    private final boolean ERROR_LOG = true;
    private final int EXPECTED_PKGS_COUNT = 50;
    private final String GL_INFO_FILE_PATH = "/proc/gpu_memory";
    private final int GPU_FIELDS = 5;
    private final int GPU_TGID_FIELD = 2;
    private final int GPU_UNMAPPED_FIELD = 4;
    private final boolean INFO_LOG = false;
    private final int MB_TO_KB = 1024;
    private final String MEMCG_DIRECTORY = "/dev/memcg/apps/";
    private final int PAGE_SIZE = 4;
    private final int RESULT_FAIL = 0;
    private final int TOTAL_ANON_FIELD = 1;
    private final int TOTAL_ESWAP_FIELD = 3;
    private final int TOTAL_FIELDS = 4;
    private final String TOTAL_INFO_FILE_NAME = "memory.total_info_per_app";
    private final int TOTAL_PKGNAME_FIELD = 0;
    private final int TOTAL_ZRAM_FIELD = 2;
    private final int TYPE_EMERG = 1;
    private final int TYPE_UB = 0;
    private final int USS_ARRAY_SIZE = 2;
    private final int USS_VALUE = 0;
    private final int ZRAM_VALUE = 1;
    private ParaConfig.AdvancedKillParam akParam;
    private Context context;
    private long finishAction;
    private AtomicBoolean interruptState = new AtomicBoolean(false);
    private long startExecute;
    private long startKilling;
    private long startMemRead;
    private long startPreparingList;
    private long timeGpuMemRead;
    private long timeTotalMemCgRead;

    /* access modifiers changed from: private */
    public interface MemoryFileLineProcessor<T> {
        void processMemoryFileLine(String str, T t);
    }

    /* access modifiers changed from: private */
    public class MemoryInfo {
        private long anonMem = 0;
        private long eswapComprMem = 0;
        private long eswapOrigMem = 0;
        private long fileMem = 0;
        private long graphicBufferMem = 0;
        private long zramComprMem = 0;
        private long zramOrigMem = 0;

        public MemoryInfo() {
        }
    }

    /* access modifiers changed from: private */
    public class KilledPackageInfo {
        private String packageName;
        private int type;

        public KilledPackageInfo(int typeNum, String pkg) {
            this.type = typeNum;
            this.packageName = pkg;
        }
    }

    /* access modifiers changed from: private */
    public class AkProcessBlockInfo {
        private String packageName;
        private long uss;
        private double weight = 0.0d;

        public AkProcessBlockInfo(String pkgName, long size) {
            this.packageName = pkgName;
            this.uss = size;
        }

        public void setWeight(double dt, double ubWeight) {
            long j = this.uss;
            if (j != 0) {
                this.weight = (ubWeight / ((double) j)) * dt;
            }
        }

        public double getWeight() {
            return this.weight;
        }

        public long getMemory() {
            return this.uss;
        }
    }

    private boolean isDebugLog() {
        return this.akParam.getAkEnableDebug();
    }

    private boolean isTimeDebugMeasurement() {
        return this.akParam.getAkEnableDebugTime();
    }

    public void setInterrupt(boolean interrupt) {
        this.interruptState.set(interrupt);
    }

    private boolean shouldInterrupt() {
        boolean interrupt = this.interruptState.get();
        if (interrupt) {
            logcatPrint(false, "AdvancedKiller", "Interrupt was triggered!");
        }
        return interrupt;
    }

    public AdvancedKiller(Context contextInit) {
        this.context = contextInit;
        this.akParam = ParaConfig.getInstance().getAdvancedKillParam();
        logcatPrint(false, "AdvancedKiller", "AK: AdvancedKiller is online");
    }

    public int execute(Bundle extras) {
        if (this.context == null || extras == null || shouldInterrupt()) {
            return 0;
        }
        boolean isProcFast = CleanSource.setSchedPriority();
        int res = executeInternal(extras);
        CleanSource.resetSchedPriority(isProcFast);
        return res;
    }

    private int executeInternal(Bundle extras) {
        String str;
        boolean z;
        int i;
        List<MemoryInfo> memInfo;
        String str2;
        this.finishAction = 0;
        this.startKilling = 0;
        this.startMemRead = 0;
        this.startPreparingList = 0;
        this.startExecute = 0;
        long memAvailable = MemoryReader.getInstance().getMemAvailable();
        long requiredMem = extras.getLong("reqMem") * 1024;
        boolean quickKillMode = extras.getBoolean("quickKill", false);
        String reason = extras.getString("reason", MemoryConstant.MEM_SCENE_DEFAULT);
        int curUid = extras.getInt("curUid");
        logcatPrint(false, "AdvancedKiller", "AK: start kill requiredMem=" + requiredMem + " memAvailable= " + memAvailable + " reason: " + reason);
        logCloudPrint("HyperHold[AkKillStart] " + requiredMem + " " + memAvailable + " " + reason);
        if (quickKillMode) {
            return executeQuick(curUid, requiredMem);
        }
        if (isTimeDebugMeasurement()) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            this.startPreparingList = elapsedRealtime;
            this.startExecute = elapsedRealtime;
        }
        List<AwareProcessBlockInfo> procGroups = generatePackageList(1, false);
        if (procGroups == null) {
            z = true;
            i = 0;
            str = "AdvancedKiller";
        } else if (procGroups.isEmpty()) {
            z = true;
            i = 0;
            str = "AdvancedKiller";
        } else if (shouldInterrupt()) {
            return 0;
        } else {
            if (isTimeDebugMeasurement()) {
                this.startMemRead = SystemClock.elapsedRealtime();
            }
            List<MemoryInfo> memInfo2 = new ArrayList<>();
            HashMap<String, MemoryInfo> memoryMap = new HashMap<>(50);
            if (!readMemoryData(procGroups, memInfo2, memoryMap)) {
                memInfo = memInfo2;
                str2 = "AdvancedKiller";
            } else if (procGroups.size() != memInfo2.size()) {
                memInfo = memInfo2;
                str2 = "AdvancedKiller";
            } else {
                int i2 = 0;
                while (i2 < procGroups.size()) {
                    MemoryInfo minfo = memInfo2.get(i2);
                    String memLog = procGroups.get(i2).procPackageName + " " + procGroups.get(i2).procWeight + " " + minfo.anonMem + " " + minfo.fileMem + " " + minfo.zramComprMem + " " + minfo.eswapComprMem + " " + minfo.graphicBufferMem;
                    if (!isTimeDebugMeasurement()) {
                        logcatPrint(false, "AdvancedKiller", "AK Pkg: " + memLog);
                    }
                    logCloudPrint("HyperHold[AkCandidate] " + memLog);
                    i2++;
                    memInfo2 = memInfo2;
                    memoryMap = memoryMap;
                    procGroups = procGroups;
                }
                if (isTimeDebugMeasurement()) {
                    this.startKilling = SystemClock.elapsedRealtime();
                }
                int killCount = chooseAndKillPackages(requiredMem, procGroups, memInfo2, curUid, memoryMap);
                if (isTimeDebugMeasurement()) {
                    this.finishAction = SystemClock.elapsedRealtime();
                }
                if (isTimeDebugMeasurement()) {
                    logcatPrint(false, "AdvancedKiller", "AK: TIME " + (this.finishAction - this.startExecute) + " " + (this.startPreparingList - this.startExecute) + " " + (this.startMemRead - this.startPreparingList) + " " + (this.startKilling - this.startMemRead) + " " + (this.finishAction - this.startKilling) + " " + this.timeTotalMemCgRead);
                }
                return killCount;
            }
            logcatPrint(true, str2, "AK: error reading memory " + memInfo.size());
            return 0;
        }
        logcatPrint(z, str, "AK: iaware list null: 1");
        return i;
    }

    private int executeQuick(int curUid, long requiredMem) {
        ArrayList<KilledPackageInfo> killedPackages = new ArrayList<>();
        HashMap<String, MemoryInfo> memoryMap = new HashMap<>(50);
        logcatPrint(false, "AdvancedKiller", "AK: start quickkill requiredMem=" + requiredMem);
        int killedMem = 0;
        for (int level = 0; level < 2; level++) {
            killedMem += executeQuickInternal(level, requiredMem - ((long) killedMem), killedPackages, memoryMap);
            if (requiredMem <= ((long) killedMem)) {
                break;
            }
        }
        logcatPrint(false, "AdvancedKiller", "AK: killed total for uss= " + killedMem);
        logCloudPrint("HyperHold[AkTotalKilledUss] " + killedMem);
        Map<String, List<Integer>> cacheKilledMap = new HashMap<>();
        if (((long) killedMem) < requiredMem) {
            cacheKilledMap = emergKillCachedProcesses(curUid, requiredMem - ((long) killedMem), memoryMap);
        }
        if (isTimeDebugMeasurement()) {
            logcatPrint(false, "AdvancedKiller", "AK: TIME " + (this.finishAction - this.startExecute) + " " + (this.startPreparingList - this.startExecute) + " " + (this.startMemRead - this.startPreparingList) + " " + (this.startKilling - this.startMemRead) + " " + (this.finishAction - this.startKilling) + " " + this.timeTotalMemCgRead);
        }
        Iterator<KilledPackageInfo> it = killedPackages.iterator();
        while (it.hasNext()) {
            KilledPackageInfo killedInfo = it.next();
            logcatPrint(false, "HWSFramework", "KILLED " + killedInfo.packageName + " by AdvancedKiller[quick]");
            logCloudPrint("HyperHold[AkAppKilled] " + killedInfo.packageName + " quick");
        }
        printCachedProcessKill(cacheKilledMap);
        return killedPackages.size();
    }

    private int executeQuickInternal(int level, long requiredMem, ArrayList<KilledPackageInfo> killedPackages, HashMap<String, MemoryInfo> memoryMap) {
        int killedMem;
        this.finishAction = 0;
        this.startKilling = 0;
        this.startMemRead = 0;
        this.startPreparingList = 0;
        this.startExecute = 0;
        logcatPrint(false, "AdvancedKiller", "AK: quickkill with level: " + level);
        if (isTimeDebugMeasurement()) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            this.startPreparingList = elapsedRealtime;
            this.startExecute = elapsedRealtime;
        }
        List<AwareProcessBlockInfo> procGroups = generatePackageList(level, false);
        if (procGroups != null) {
            if (!procGroups.isEmpty()) {
                if (shouldInterrupt()) {
                    return 0;
                }
                if (isTimeDebugMeasurement()) {
                    this.startMemRead = SystemClock.elapsedRealtime();
                }
                List<MemoryInfo> memInfo = new ArrayList<>();
                if (!readMemoryData(procGroups, memInfo, memoryMap) || procGroups.size() != memInfo.size()) {
                    logcatPrint(true, "AdvancedKiller", "AK: error reading memory " + memInfo.size());
                    return 0;
                }
                List<AkProcessBlockInfo> akProcsGroup = quickProcessLists(procGroups, memInfo);
                if (akProcsGroup.size() == 0) {
                    logcatPrint(true, "AdvancedKiller", "AK: request to model failed");
                    return 0;
                }
                if (isTimeDebugMeasurement()) {
                    this.startKilling = SystemClock.elapsedRealtime();
                }
                int killedMem2 = 0;
                if (level == 0) {
                    getQuickVictimsList(requiredMem, akProcsGroup, true);
                    killedMem2 = (int) (((long) 0) + quickKillPackages(getQuickVictimsList(requiredMem, akProcsGroup, false), requiredMem, procGroups, memInfo, killedPackages));
                }
                if (((long) killedMem2) < requiredMem) {
                    killedMem = (int) (((long) killedMem2) + quickKillPackages(getQuickVictimsList(requiredMem, akProcsGroup, true), requiredMem, procGroups, memInfo, killedPackages));
                } else {
                    killedMem = killedMem2;
                }
                if (isTimeDebugMeasurement()) {
                    this.finishAction = SystemClock.elapsedRealtime();
                }
                return killedMem;
            }
        }
        logcatPrint(true, "AdvancedKiller", "AK: iaware list null: " + level);
        return 0;
    }

    /* JADX INFO: Multiple debug info for r6v2 com.huawei.server.rme.hyperhold.-$$Lambda$AdvancedKiller$4TB8R7J6hWgR35h3xkeS40JpWXM: [D('i' int), D('compareByWeights' java.util.Comparator<com.huawei.server.rme.hyperhold.AdvancedKiller$AkProcessBlockInfo>)] */
    private List<AkProcessBlockInfo> quickProcessLists(List<AwareProcessBlockInfo> procGroups, List<MemoryInfo> memInfo) {
        List<String> packageNames = new ArrayList<>();
        List<AkProcessBlockInfo> akProcsGroup = new ArrayList<>();
        for (int i = 0; i < procGroups.size(); i++) {
            AwareProcessBlockInfo procBlock = procGroups.get(i);
            MemoryInfo minfo = memInfo.get(i);
            long totalMem = minfo.anonMem + minfo.zramComprMem + minfo.graphicBufferMem;
            if (canPackageBeKilled(procBlock, totalMem)) {
                packageNames.add(procBlock.procPackageName);
                akProcsGroup.add(new AkProcessBlockInfo(procBlock.procPackageName, totalMem));
            }
            String memLog = procGroups.get(i).procPackageName + " " + procGroups.get(i).procWeight + " " + minfo.anonMem + " " + minfo.fileMem + " " + minfo.zramComprMem + " " + minfo.eswapComprMem + " " + minfo.graphicBufferMem;
            if (!isTimeDebugMeasurement()) {
                logcatPrint(false, "AdvancedKiller", "AK Pkg: " + memLog);
            }
            logCloudPrint("HyperHold[AkCandidate] " + memLog);
        }
        double[] outDt = new double[packageNames.size()];
        double[] outWeigths = new double[packageNames.size()];
        if (ResourceCollector.getAppDtAndWeights((String[]) packageNames.toArray(new String[0]), outDt, outWeigths) != 0) {
            return Collections.emptyList();
        }
        for (int i2 = 0; i2 < akProcsGroup.size(); i2++) {
            akProcsGroup.get(i2).setWeight(outDt[i2], outWeigths[i2]);
        }
        Collections.sort(akProcsGroup, $$Lambda$AdvancedKiller$4TB8R7J6hWgR35h3xkeS40JpWXM.INSTANCE);
        return akProcsGroup;
    }

    static /* synthetic */ int lambda$quickProcessLists$0(AkProcessBlockInfo o1, AkProcessBlockInfo o2) {
        return Double.compare(o1.getWeight(), o2.getWeight());
    }

    private List<String> getQuickVictimsList(long requiredMem, List<AkProcessBlockInfo> akProcsGroup, boolean greedy) {
        List<String> victimsPackageList = new ArrayList<>();
        if (!greedy) {
            List<AkProcessBlockInfo> victimList = new ArrayList<>();
            int accumMem = 0;
            for (int i = 0; i < akProcsGroup.size() && ((long) accumMem) < requiredMem; i++) {
                victimList.add(akProcsGroup.get(i));
                accumMem = (int) (((long) accumMem) + akProcsGroup.get(i).getMemory());
            }
            for (int i2 = victimList.size() - 1; i2 >= 0; i2--) {
                long mem = victimList.get(i2).getMemory();
                if (((long) accumMem) - mem >= requiredMem) {
                    victimList.remove(i2);
                    accumMem = (int) (((long) accumMem) - mem);
                }
            }
            for (int i3 = 0; i3 < victimList.size(); i3++) {
                AkProcessBlockInfo procBlock = victimList.get(i3);
                victimsPackageList.add(procBlock.packageName);
                akProcsGroup.remove(procBlock);
            }
        } else {
            for (int i4 = 0; i4 < akProcsGroup.size(); i4++) {
                victimsPackageList.add(akProcsGroup.get(i4).packageName);
            }
        }
        return victimsPackageList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logcatPrint(boolean isError, String tag, String message) {
        int res;
        if (isError) {
            res = SlogEx.e(tag, message);
        } else {
            res = SlogEx.i(tag, message);
        }
        if (res <= 0 && isDebugLog()) {
            logcatFailedMessage("Ret code: " + res + "\nTag: " + tag + "\n" + message);
        }
    }

    private void logCloudPrint(String message) {
        Statistics.getInstance().writeLog(message);
    }

    private void logcatFailedMessage(String message) {
        Object dropboxObject = this.context.getSystemService("dropbox");
        if (dropboxObject instanceof DropBoxManager) {
            ((DropBoxManager) dropboxObject).addText("FAILHAPPENS", "Fail with logcat message:\n" + message + "\n");
        }
    }

    private List<Integer> execChooseKill(AwareProcessBlockInfo procGroup, boolean needCheckAdj) {
        return ProcessCleaner.getInstance(this.context).killProcessesSameUidFast(procGroup, this.interruptState, "AdvancedKiller", new boolean[]{false, false, needCheckAdj});
    }

    private List<AwareProcessBlockInfo> generatePackageList(int memLevel, boolean isCacheClean) {
        AwareAppMngSortPolicy policy;
        if (isCacheClean) {
            policy = MemoryUtils.getCachedCleanPolicy();
            if (policy == null) {
                logcatPrint(true, "AdvancedKiller", "AK: error: getCachedCleanPolicy fail.");
                return null;
            }
        } else {
            policy = MemoryUtils.getAppMngSortPolicy(2, 3, memLevel);
        }
        List<AwareProcessBlockInfo> list = MemoryUtils.getAppMngProcGroup(policy, 2);
        if (list != null) {
            list.removeIf($$Lambda$KOObcVsaJsxvOT87mUp4OCwLSo.INSTANCE);
        }
        return list;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0097, code lost:
        if (r13 == null) goto L_0x0099;
     */
    private boolean readMemoryData(List<AwareProcessBlockInfo> procGroups, List<MemoryInfo> memInfo, HashMap<String, MemoryInfo> memoryMap) {
        MemoryInfo minfo;
        this.timeGpuMemRead = 0;
        this.timeTotalMemCgRead = 0;
        File memcgDir = new File("/dev/memcg/apps/");
        if (!memcgDir.exists()) {
            return false;
        }
        if (!memcgDir.isDirectory()) {
            return false;
        }
        long memcgTotalReadTimeStart = 0;
        if (isTimeDebugMeasurement()) {
            memcgTotalReadTimeStart = SystemClock.elapsedRealtime();
        }
        readMemoryDataFile("/dev/memcg/apps/memory.total_info_per_app", memoryMap, new TotalInfoFileLineProcessor());
        if (isTimeDebugMeasurement()) {
            this.timeTotalMemCgRead += SystemClock.elapsedRealtime() - memcgTotalReadTimeStart;
        }
        HashMap<Integer, Long> gpuMemoryMap = new HashMap<>(50);
        long gpuMemReadTimeStart = 0;
        if (isTimeDebugMeasurement()) {
            gpuMemReadTimeStart = SystemClock.elapsedRealtime();
        }
        readMemoryDataFile("/proc/gpu_memory", gpuMemoryMap, new GlInfoFileLineProcessor());
        if (isTimeDebugMeasurement()) {
            this.timeGpuMemRead += SystemClock.elapsedRealtime() - gpuMemReadTimeStart;
        }
        for (AwareProcessBlockInfo procBlock : procGroups) {
            if (memoryMap != null && !memoryMap.isEmpty()) {
                MemoryInfo memoryInfo = memoryMap.get(procBlock.procPackageName);
                minfo = memoryInfo;
            }
            minfo = new MemoryInfo();
            List<Integer> pids = getPids(procBlock);
            minfo.graphicBufferMem = calculateGpuMemory(pids, gpuMemoryMap);
            if (minfo.anonMem == 0 && minfo.zramComprMem == 0 && minfo.eswapComprMem == 0) {
                collectMemoryPerProcess(minfo, pids);
            }
            memInfo.add(minfo);
        }
        return true;
    }

    private List<Integer> getPids(AwareProcessBlockInfo block) {
        List<Integer> pids = new ArrayList<>();
        if (!(block == null || block.procProcessList == null)) {
            for (AwareProcessInfo awareProcess : block.procProcessList) {
                if (awareProcess != null) {
                    pids.add(Integer.valueOf(awareProcess.procPid));
                }
            }
        }
        return pids;
    }

    private void collectMemoryPerProcess(MemoryInfo memInfo, List<Integer> pids) {
        if (!(memInfo == null || pids == null || pids.isEmpty())) {
            for (Integer pid : pids) {
                long[] outUss = new long[2];
                DebugExt.getPss(pid.intValue(), outUss, (long[]) null);
                memInfo.anonMem += outUss[0];
                memInfo.zramComprMem += outUss[1] / 3;
            }
            memInfo.graphicBufferMem = 0;
        }
    }

    private long calculateGpuMemory(List<Integer> pids, HashMap<Integer, Long> gpuMemoryMap) {
        long gpuMem = 0;
        if (gpuMemoryMap != null && !gpuMemoryMap.isEmpty() && pids != null && !pids.isEmpty()) {
            for (Integer num : pids) {
                Long procGpuMem = gpuMemoryMap.get(Integer.valueOf(num.intValue()));
                if (procGpuMem != null) {
                    gpuMem += procGpuMem.longValue();
                }
            }
        }
        return gpuMem;
    }

    /* access modifiers changed from: private */
    public class TotalInfoFileLineProcessor implements MemoryFileLineProcessor<HashMap<String, MemoryInfo>> {
        private TotalInfoFileLineProcessor() {
        }

        public void processMemoryFileLine(String memoryFileLine, HashMap<String, MemoryInfo> memoryMap) {
            try {
                String[] memoryDataString = memoryFileLine.split("\\s+");
                if (memoryDataString.length < 4) {
                    AdvancedKiller advancedKiller = AdvancedKiller.this;
                    advancedKiller.logcatPrint(true, "AdvancedKiller", "Read wrong line in total_info: " + memoryFileLine);
                    return;
                }
                MemoryInfo minfo = new MemoryInfo();
                minfo.anonMem = Long.parseLong(memoryDataString[1]);
                minfo.zramComprMem = Long.parseLong(memoryDataString[2]);
                minfo.eswapComprMem = Long.parseLong(memoryDataString[3]);
                memoryMap.put(memoryDataString[0], minfo);
            } catch (NumberFormatException e) {
                AdvancedKiller advancedKiller2 = AdvancedKiller.this;
                advancedKiller2.logcatPrint(true, "AdvancedKiller", "Error in convertion for total_info from: " + memoryFileLine);
            }
        }
    }

    /* access modifiers changed from: private */
    public class GlInfoFileLineProcessor implements MemoryFileLineProcessor<HashMap<Integer, Long>> {
        private GlInfoFileLineProcessor() {
        }

        public void processMemoryFileLine(String memoryFileLine, HashMap<Integer, Long> gpuMemoryMap) {
            try {
                String[] gpuDataString = memoryFileLine.trim().split("\\s+");
                if (gpuDataString.length >= 5) {
                    int tgid = Integer.parseInt(gpuDataString[2]);
                    long gpuMem = Long.parseLong(gpuDataString[4]) * 4;
                    if (tgid > 0 && gpuMem > 0) {
                        gpuMemoryMap.put(Integer.valueOf(tgid), Long.valueOf(gpuMem));
                    }
                }
            } catch (NumberFormatException e) {
                AdvancedKiller advancedKiller = AdvancedKiller.this;
                advancedKiller.logcatPrint(true, "AdvancedKiller", "Error in convertion from gpu_info: " + memoryFileLine);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003c, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003d, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0040, code lost:
        throw r6;
     */
    private <T> void readMemoryDataFile(String filename, T memoryStructure, MemoryFileLineProcessor<T> lineProcessor) {
        File memoryFile = new File(filename);
        if (!memoryFile.exists() || !memoryFile.canRead() || !memoryFile.isFile()) {
            logcatPrint(true, "AdvancedKiller", "Can't access file: " + filename + " with stats: " + memoryFile.exists() + " " + memoryFile.canRead() + " " + memoryFile.isFile());
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(memoryFile));
            while (true) {
                String line = br.readLine();
                if (line != null) {
                    lineProcessor.processMemoryFileLine(line, memoryStructure);
                } else {
                    br.close();
                    return;
                }
            }
        } catch (IOException ex) {
            logcatPrint(true, "AdvancedKiller", "Internal exception for br: " + filename + " " + ex);
        }
    }

    private boolean canPackageBeKilled(AwareProcessBlockInfo procBlock, long totalMem) {
        return totalMem >= 4096 && procBlock != null && procBlock.procProcessList != null && !checkPreloadCamera(procBlock, totalMem);
    }

    private boolean isKillFreqType(AwareProcessBlockInfo procBlock) {
        List<AwareProcessInfo> processList = procBlock.procProcessList;
        if (processList == null) {
            return false;
        }
        int uid = procBlock.procUid;
        for (String packageName : getPackageList(processList)) {
            if (getKilledFrequency(uid, packageName, processList) == PackageTracker.KilledFrequency.FREQUENCY_CRITICAL) {
                logcatPrint(false, "AdvancedKiller", "Frequency killed package excluded: " + packageName);
                return true;
            }
        }
        return false;
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

    private boolean checkPreloadCamera(AwareProcessBlockInfo procBlock, long totalMem) {
        if (procBlock.procProcessList.size() != 1 || !"com.huawei.camera".equals(procBlock.procPackageName) || totalMem >= ((long) MemoryConstant.getCameraPreloadKillUss())) {
            return false;
        }
        return true;
    }

    private long[] getArrayFromStream(LongStream stream) {
        return stream.toArray();
    }

    private String[] chooseVictims(long reqMem, List<AwareProcessBlockInfo> procGroups, List<MemoryInfo> memInfo) {
        List<String> packageNames = new ArrayList<>();
        List<Long> usses = new ArrayList<>();
        int i = procGroups.size() - 1;
        while (true) {
            if (i < 0) {
                break;
            }
            AwareProcessBlockInfo procBlock = procGroups.get(i);
            if (procBlock.procWeight > 100) {
                procGroups.subList(0, i + 1).clear();
                memInfo.subList(0, i + 1).clear();
                break;
            }
            MemoryInfo minfo = memInfo.get(i);
            long totalMem = minfo.anonMem + minfo.zramComprMem + minfo.graphicBufferMem;
            if (canPackageBeKilled(procBlock, totalMem) && !isKillFreqType(procBlock)) {
                packageNames.add(procBlock.procPackageName);
                usses.add(Long.valueOf(totalMem));
            }
            i--;
        }
        String[] packageNamesToNative = (String[]) packageNames.toArray(new String[0]);
        long[] ussToNative = getArrayFromStream(usses.stream().mapToLong($$Lambda$AdvancedKiller$0Jjlfa2BxOgQPAOeIMHbzcNJAn0.INSTANCE));
        if (packageNamesToNative.length != ussToNative.length) {
            logcatPrint(true, "AdvancedKiller", "AK: error: uss != packages");
            return null;
        }
        if (!isTimeDebugMeasurement()) {
            for (int i2 = 0; i2 < packageNamesToNative.length; i2++) {
                logcatPrint(false, "AdvancedKiller", "AK: packages= " + packageNamesToNative[i2] + " " + ussToNative[i2]);
            }
        }
        if (packageNamesToNative.length != 0) {
            return ResourceCollector.requestKillModel(reqMem, packageNamesToNative, ussToNative);
        }
        logcatPrint(true, "AdvancedKiller", "AK: error: nothing to send to model");
        return null;
    }

    private long ubKillPackages(long requiredMem, List<AwareProcessBlockInfo> procGroups, List<MemoryInfo> memInfo, String[] victimList, ArrayList<KilledPackageInfo> killedPackages) {
        boolean z;
        boolean z2;
        String s;
        if (victimList != null) {
            if (victimList.length != 0) {
                int length = victimList.length;
                boolean z3 = false;
                long killUss = 0;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String s2 = victimList[i];
                    if (shouldInterrupt()) {
                        break;
                    }
                    int i2 = 0;
                    while (true) {
                        if (i2 >= procGroups.size()) {
                            z = z3;
                            break;
                        } else if (procGroups.get(i2).procPackageName.equals(s2)) {
                            List<Integer> pids = execChooseKill(procGroups.get(i2), z3);
                            killUss += checkKilledPids(i2, s2, pids, memInfo, killedPackages);
                            updatePackageTracker(pids, procGroups.get(i2));
                            procGroups.remove(i2);
                            memInfo.remove(i2);
                            z = false;
                            break;
                        } else {
                            if (i2 == procGroups.size() - 1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("AK: package ");
                                s = s2;
                                sb.append(s);
                                sb.append(" not exist");
                                z2 = false;
                                logcatPrint(false, "AdvancedKiller", sb.toString());
                            } else {
                                s = s2;
                                z2 = false;
                            }
                            i2++;
                            s2 = s;
                            z3 = z2;
                        }
                    }
                    i++;
                    z3 = z;
                }
                return killUss;
            }
        }
        logcatPrint(true, "AdvancedKiller", "AK: error: algo returns bad");
        return 0;
    }

    private long checkKilledPids(int order, String packageName, List<Integer> pids, List<MemoryInfo> memInfo, ArrayList<KilledPackageInfo> killedPackages) {
        if (!isTimeDebugMeasurement()) {
            logcatPrint(false, "AdvancedKiller", "AK: kill package= " + packageName + " some pids= " + pids);
        }
        if (pids == null || pids.isEmpty()) {
            return 0;
        }
        MemoryInfo minfo = memInfo.get(order);
        long freedMem = 0 + minfo.anonMem + minfo.zramComprMem + minfo.graphicBufferMem;
        killedPackages.add(new KilledPackageInfo(0, packageName));
        return freedMem;
    }

    private void updatePackageTracker(List<Integer> pids, AwareProcessBlockInfo procGroup) {
        if (pids != null && !pids.isEmpty()) {
            PackageTracker.getInstance().trackKillEvent(procGroup.procUid, procGroup.getProcessList());
        }
    }

    private long emergKillPackages(long requiredMem, List<AwareProcessBlockInfo> procGroups, List<MemoryInfo> memInfo, ArrayList<KilledPackageInfo> killedPackages) {
        long killUss = 0;
        int i = procGroups.size() - 1;
        while (true) {
            if (i < 0) {
                break;
            } else if (shouldInterrupt()) {
                break;
            } else {
                AwareProcessBlockInfo procBlock = procGroups.get(i);
                MemoryInfo minfo = memInfo.get(i);
                long totalMem = minfo.anonMem + minfo.zramComprMem + minfo.graphicBufferMem;
                if (canPackageBeKilled(procBlock, totalMem) && !isKillFreqType(procBlock)) {
                    String packageName = procBlock.procPackageName;
                    List<Integer> pids = execChooseKill(procBlock, false);
                    updatePackageTracker(pids, procBlock);
                    if (pids != null && !pids.isEmpty()) {
                        killUss += totalMem;
                        killedPackages.add(new KilledPackageInfo(1, packageName));
                    }
                    if (killUss >= requiredMem) {
                        break;
                    }
                }
                i--;
            }
        }
        return killUss;
    }

    private long quickKillPackages(List<String> victimList, long requiredMem, List<AwareProcessBlockInfo> procGroups, List<MemoryInfo> memInfo, ArrayList<KilledPackageInfo> killedPackages) {
        long killUss;
        ProcessCleaner.getInstance(this.context).beginKillFast();
        boolean z = false;
        if (victimList != null) {
            if (!victimList.isEmpty()) {
                Iterator<String> it = victimList.iterator();
                killUss = 0;
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    String s = it.next();
                    if (shouldInterrupt()) {
                        break;
                    }
                    int i = 0;
                    while (true) {
                        if (i >= procGroups.size()) {
                            break;
                        } else if (procGroups.get(i).procPackageName.equals(s)) {
                            List<Integer> pids = execChooseKill(procGroups.get(i), z);
                            killUss += checkKilledPids(i, s, pids, memInfo, killedPackages);
                            updatePackageTracker(pids, procGroups.get(i));
                            procGroups.remove(i);
                            memInfo.remove(i);
                            break;
                        } else {
                            if (i == procGroups.size() - 1) {
                                logcatPrint(false, "AdvancedKiller", "AK: package " + s + " not exist");
                            }
                            i++;
                            z = false;
                        }
                    }
                    if (killUss >= requiredMem) {
                        break;
                    }
                    z = false;
                }
                ProcessCleaner.getInstance(this.context).endKillFast();
                logcatPrint(false, "AdvancedKiller", "AK: killed quick for uss= " + killUss);
                return killUss;
            }
        }
        logcatPrint(true, "AdvancedKiller", "AK: error: algo returns bad");
        killUss = 0;
        ProcessCleaner.getInstance(this.context).endKillFast();
        logcatPrint(false, "AdvancedKiller", "AK: killed quick for uss= " + killUss);
        return killUss;
    }

    private int chooseAndKillPackages(long requiredMem, List<AwareProcessBlockInfo> procGroups, List<MemoryInfo> memInfo, int curUid, HashMap<String, MemoryInfo> memoryMap) {
        String[] victimList = chooseVictims(requiredMem, procGroups, memInfo);
        ProcessCleaner.getInstance(this.context).beginKillFast();
        ArrayList<KilledPackageInfo> killedPackages = new ArrayList<>();
        long killUss = ubKillPackages(requiredMem, procGroups, memInfo, victimList, killedPackages);
        logcatPrint(false, "AdvancedKiller", "AK: killed with UB for uss= " + killUss);
        Map<String, List<Integer>> cacheKilledMap = new HashMap();
        if (killUss < requiredMem && !shouldInterrupt()) {
            killUss += emergKillPackages(requiredMem - killUss, procGroups, memInfo, killedPackages);
            if (killUss < requiredMem) {
                cacheKilledMap = emergKillCachedProcesses(curUid, requiredMem - killUss, memoryMap);
            }
        }
        ProcessCleaner.getInstance(this.context).endKillFast();
        logcatPrint(false, "AdvancedKiller", "AK: killed total for uss= " + killUss);
        logCloudPrint("HyperHold[AkTotalKilledUss] " + killUss);
        if (killUss < requiredMem) {
            logcatPrint(false, "AdvancedKiller", "AK: ISSUE cannot find memory= " + requiredMem + " only find= " + killUss);
        }
        Iterator<KilledPackageInfo> it = killedPackages.iterator();
        while (it.hasNext()) {
            KilledPackageInfo killedInfo = it.next();
            StringBuilder sb = new StringBuilder();
            sb.append("KILLED ");
            sb.append(killedInfo.packageName);
            sb.append(" by AdvancedKiller[");
            sb.append(killedInfo.type == 0 ? "ub]" : "emerg]");
            logcatPrint(false, "HWSFramework", sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("HyperHold[AkAppKilled] ");
            sb2.append(killedInfo.packageName);
            sb2.append(killedInfo.type == 0 ? " ub" : " emerg");
            logCloudPrint(sb2.toString());
        }
        printCachedProcessKill(cacheKilledMap);
        return killedPackages.size();
    }

    public void updateModel(String pkg) {
        boolean timeMeasurementDebug = isTimeDebugMeasurement();
        long startToEvalModel = 0;
        if (timeMeasurementDebug) {
            startToEvalModel = SystemClock.elapsedRealtime();
        }
        logcatPrint(false, "AdvancedKiller", "AK: " + pkg + " counter update called");
        StringBuilder sb = new StringBuilder();
        sb.append("HyperHold[AkModelUpdate] ");
        sb.append(pkg);
        logCloudPrint(sb.toString());
        ResourceCollector.updateKillModel(pkg);
        if (timeMeasurementDebug) {
            SlogEx.e("AdvancedKiller", "AK: EVALMODELFG " + (SystemClock.elapsedRealtime() - startToEvalModel));
        }
    }

    public void serializeModel() {
        ResourceCollector.serializeKillModel();
    }

    private Map<String, List<Integer>> emergKillCachedProcesses(int curUid, long requiredMem, HashMap<String, MemoryInfo> memoryMap) {
        new HashMap();
        logcatPrint(false, "AdvancedKiller", "after pkg kill, not match require, do cache kill. reqMem= " + requiredMem);
        return execKillCachedAction(curUid, memoryMap);
    }

    private void printCachedProcessKill(Map<String, List<Integer>> cacheKilledMap) {
        for (Map.Entry<String, List<Integer>> cacheProc : cacheKilledMap.entrySet()) {
            if (cacheProc.getValue() != null) {
                StringBuilder procList = new StringBuilder();
                for (Integer num : cacheProc.getValue()) {
                    procList.append(num.intValue());
                    procList.append(", ");
                }
                logcatPrint(false, "AdvancedKiller", "cache kill ProcessName: " + cacheProc.getKey() + ", pids: " + procList.toString());
                logCloudPrint("HyperHold[AkAppKilled] " + cacheProc.getKey() + " clean process: " + procList.toString());
            }
        }
    }

    private Map<String, List<Integer>> execKillCachedAction(int curUid, HashMap<String, MemoryInfo> memoryMap) {
        List<AwareProcessBlockInfo> procGroups;
        int i = 0;
        List<AwareProcessBlockInfo> procGroups2 = generatePackageList(0, true);
        if (procGroups2 != null) {
            if (!procGroups2.isEmpty()) {
                Map<String, List<Integer>> resMap = new HashMap<>();
                int i2 = procGroups2.size() - 1;
                while (true) {
                    if (i2 < 0) {
                        break;
                    }
                    AwareProcessBlockInfo procBlockInfo = procGroups2.get(i2);
                    if (!checkProcGroup(procBlockInfo, curUid)) {
                        procGroups = procGroups2;
                    } else if (shouldInterrupt()) {
                        break;
                    } else {
                        List<AwareProcessInfo> procProcessList = procBlockInfo.procProcessList;
                        if (procProcessList == null) {
                            procGroups = procGroups2;
                        } else if (procProcessList.isEmpty()) {
                            procGroups = procGroups2;
                        } else {
                            AwareProcessInfo procInfo = procProcessList.get(i);
                            if (procInfo == null) {
                                procGroups = procGroups2;
                            } else if (procInfo.procProcInfo == null) {
                                procGroups = procGroups2;
                            } else {
                                MemoryInfo minfo = memoryMap.get(procInfo.procProcInfo.mPackageName);
                                long totalMem = 4096;
                                if (minfo != null) {
                                    totalMem = minfo.anonMem + minfo.zramComprMem;
                                }
                                if (canPackageBeKilled(procBlockInfo, totalMem)) {
                                    String procName = procInfo.procProcInfo.mProcessName;
                                    procGroups = procGroups2;
                                    CachedMemoryCleanPolicy.getInstance().updateCachedMemoryCleanRecord(procBlockInfo.procUid, procBlockInfo.procPackageName, procName, true);
                                    i = 0;
                                    List<Integer> pids = execChooseKill(procBlockInfo, false);
                                    if (pids != null && !pids.isEmpty()) {
                                        if (resMap.get(procName) == null) {
                                            resMap.put(procName, new ArrayList<>());
                                        }
                                        resMap.get(procName).addAll(pids);
                                    }
                                } else {
                                    procGroups = procGroups2;
                                }
                            }
                        }
                    }
                    i2--;
                    procGroups2 = procGroups;
                }
                return resMap;
            }
        }
        logcatPrint(true, "AdvancedKiller", "AK: error: get procGroups can kill is null or empty.");
        return new HashMap();
    }

    private boolean checkProcGroup(AwareProcessBlockInfo procGroup, int appUid) {
        if (procGroup == null || appUid == procGroup.procUid) {
            return false;
        }
        return true;
    }
}
