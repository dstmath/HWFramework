package com.huawei.server.rme.hyperhold;

import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.appactcontrol.AppActConstant;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.rme.collector.ResourceCollector;
import com.huawei.server.rme.hyperhold.AdvancedKiller;
import com.huawei.server.rme.hyperhold.AdvancedKillerAwareUtils;
import com.huawei.server.rme.hyperhold.AdvancedKillerIO;
import com.huawei.server.rme.hyperhold.AdvancedKillerPackageInfo;
import com.huawei.server.rme.hyperhold.ParaConfig;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class AdvancedKiller {
    private final String AK_TAG = "AdvancedKiller";
    private final int ANON_MIN_SIZE_KB = 3072;
    private final int COMPRESSION_RATIO = 3;
    private final boolean DEFAULT_QUICKKILL = false;
    private final String DEFAULT_REASON = AppActConstant.VALUE_DEFAULT;
    private final String EMERG_KILL_TYPE = "emerg";
    private final int MB_TO_KB = 1024;
    private final String MEMCG_DIRECTORY = "/dev/memcg/apps/";
    private final String PER_PROCESS_KILL_TYPE = "per-process emerg";
    private final String REGULAR_KILL_TYPE = "regular";
    private final int RESULT_FAIL = 0;
    private final String TOTAL_INFO_FILE_NAME = "memory.total_info_per_app";
    private final int USS_ARRAY_SIZE = 2;
    private final int USS_VALUE = 0;
    private final int ZRAM_VALUE = 1;
    private ParaConfig.AdvancedKillParam akParam;
    private Context context;
    private GlConstantsBundle glBundle = new KirinGlConstantsBundle(this, null);
    private AtomicBoolean interruptState = new AtomicBoolean(false);
    private AdvancedKillerIO reader;
    private Statistics stat;
    private ExecutionTimeMeasurement timeValue = new ExecutionTimeMeasurement(this, null);
    private AdvancedKillerAwareUtils utils;

    public enum LogType {
        INFO_LOG,
        TIME_LOG,
        CLOUD_INFO_LOG,
        IMPORTANT_LOG,
        ERROR_LOG
    }

    public enum TimeSector {
        START_EXECUTION,
        START_LEVEL,
        START_MEM_READ,
        START_KILL,
        FINISH_EXECUTION,
        MEMCG_READ_DONE,
        GPU_READ_DONE
    }

    public class AdvancedKillerIterationInfo {
        private int killWeightLimit;
        private boolean lowendList;
        private boolean perProcessKill;
        private boolean quickKill;
        private int ubSortWeightLimit;

        private AdvancedKillerIterationInfo() {
            AdvancedKiller.this = r1;
        }

        /* synthetic */ AdvancedKillerIterationInfo(AdvancedKiller x0, AnonymousClass1 x1) {
            this();
        }
    }

    public class MemoryInfo {
        private long anonMem;
        private long eswapComprMem;
        private long graphicBufferMem;
        private long zramComprMem;

        private MemoryInfo() {
            AdvancedKiller.this = r1;
        }

        /* synthetic */ MemoryInfo(AdvancedKiller x0, AnonymousClass1 x1) {
            this();
        }

        static /* synthetic */ long access$1214(MemoryInfo x0, long x1) {
            long j = x0.anonMem + x1;
            x0.anonMem = j;
            return j;
        }

        static /* synthetic */ long access$1314(MemoryInfo x0, long x1) {
            long j = x0.zramComprMem + x1;
            x0.zramComprMem = j;
            return j;
        }

        private long getTotalMem() {
            return this.anonMem + this.zramComprMem + this.graphicBufferMem;
        }
    }

    public class MemoryPackageInfo {
        private MemoryInfo memInfo;
        private AdvancedKillerPackageInfo packageInfo;

        /* synthetic */ MemoryPackageInfo(AdvancedKiller x0, MemoryInfo x1, AdvancedKillerPackageInfo x2, AnonymousClass1 x3) {
            this(x1, x2);
        }

        private MemoryPackageInfo(MemoryInfo memInfo2, AdvancedKillerPackageInfo packageInfo2) {
            AdvancedKiller.this = r1;
            this.memInfo = memInfo2;
            this.packageInfo = packageInfo2;
        }
    }

    public class KillerResult {
        private long killedMem;
        private int killedPackages;

        private KillerResult() {
            AdvancedKiller.this = r1;
        }

        /* synthetic */ KillerResult(AdvancedKiller x0, AnonymousClass1 x1) {
            this();
        }

        static /* synthetic */ long access$614(KillerResult x0, long x1) {
            long j = x0.killedMem + x1;
            x0.killedMem = j;
            return j;
        }

        static /* synthetic */ int access$808(KillerResult x0) {
            int i = x0.killedPackages;
            x0.killedPackages = i + 1;
            return i;
        }

        private void add(KillerResult killRes) {
            int i;
            if (killRes != null) {
                long j = killRes.killedMem;
                if (j >= 0 && (i = killRes.killedPackages) >= 0) {
                    this.killedMem += j;
                    this.killedPackages += i;
                }
            }
        }
    }

    public class VictimInfo {
        private int emergIndex;
        private List<MemoryPackageInfo> victims;

        /* synthetic */ VictimInfo(AdvancedKiller x0, List x1, int x2, AnonymousClass1 x3) {
            this(x1, x2);
        }

        private VictimInfo(List<MemoryPackageInfo> victims2, int emergIndex2) {
            AdvancedKiller.this = r1;
            this.victims = victims2;
            this.emergIndex = emergIndex2;
        }
    }

    public class ExecutionTimeMeasurement {
        private long finishExecution;
        private long startExecution;
        private long startKilling;
        private long startLevel;
        private long startMemRead;
        private long timeGpuMemRead;
        private long timeTotalMemCgRead;

        private ExecutionTimeMeasurement() {
            AdvancedKiller.this = r1;
        }

        /* synthetic */ ExecutionTimeMeasurement(AdvancedKiller x0, AnonymousClass1 x1) {
            this();
        }

        private void storeTimeForSectorNow(TimeSector sector) {
            long currentTime = SystemClock.elapsedRealtime();
            switch (sector) {
                case START_EXECUTION:
                    this.startExecution = currentTime;
                    return;
                case START_LEVEL:
                    this.startLevel = currentTime;
                    return;
                case START_MEM_READ:
                    this.startMemRead = currentTime;
                    return;
                case START_KILL:
                    this.startKilling = currentTime;
                    return;
                case FINISH_EXECUTION:
                    this.finishExecution = currentTime;
                    return;
                case MEMCG_READ_DONE:
                    this.timeTotalMemCgRead = currentTime;
                    return;
                case GPU_READ_DONE:
                    this.timeGpuMemRead = currentTime;
                    return;
                default:
                    return;
            }
        }

        private String dumpLevelTime() {
            return "Level: " + (this.finishExecution - this.startLevel) + " " + (this.startMemRead - this.startExecution) + " " + (this.startKilling - this.startMemRead) + " " + (this.finishExecution - this.startKilling) + " " + (this.timeTotalMemCgRead - this.startMemRead);
        }

        private String dumpExecTime() {
            return "TIME: " + (this.finishExecution - this.startExecution);
        }
    }

    public abstract class GlConstantsBundle {
        protected String fileName;
        protected int gpuFields;
        protected int gpuTgidField;
        protected int gpuUnmappedField;

        private GlConstantsBundle() {
            AdvancedKiller.this = r1;
        }

        /* synthetic */ GlConstantsBundle(AdvancedKiller x0, AnonymousClass1 x1) {
            this();
        }
    }

    /* access modifiers changed from: private */
    public final class KirinGlConstantsBundle extends GlConstantsBundle {
        /* synthetic */ KirinGlConstantsBundle(AdvancedKiller x0, AnonymousClass1 x1) {
            this();
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        private KirinGlConstantsBundle() {
            super(r2, null);
            AdvancedKiller.this = r2;
            this.fileName = "/proc/gpu_memory";
            this.gpuTgidField = 2;
            this.gpuUnmappedField = 4;
            this.gpuFields = 5;
        }
    }

    /* access modifiers changed from: private */
    public final class MtkGlConstantsBundle extends GlConstantsBundle {
        /* synthetic */ MtkGlConstantsBundle(AdvancedKiller x0, AnonymousClass1 x1) {
            this();
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        private MtkGlConstantsBundle() {
            super(r2, null);
            AdvancedKiller.this = r2;
            this.fileName = "/sys/kernel/debug/mali0/gpu_memory";
            this.gpuTgidField = 2;
            this.gpuUnmappedField = 1;
            this.gpuFields = 3;
        }
    }

    public static final class AdvancedKillerListSettings implements Serializable {
        private static final int MAX_WEIGHT = Integer.MAX_VALUE;
        private static final List<AdvancedKillerAwareUtils.MemoryLevelListType> QUICK_KILL_LISTS = Arrays.asList(AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_0, AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_1, AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_CACHED_PROCESS);
        private static final List<AdvancedKillerAwareUtils.MemoryLevelListType> REGULAR_KILL_LISTS = Arrays.asList(AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_1, AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_CACHED_PROCESS);
        private List<AdvancedKillerAwareUtils.MemoryLevelListType> killList;
        private int killWeightLimit;
        private int ubSortWeightLimit;

        public AdvancedKillerListSettings() {
            this(REGULAR_KILL_LISTS);
        }

        public AdvancedKillerListSettings(List<AdvancedKillerAwareUtils.MemoryLevelListType> killList2) {
            this(killList2, 0, MAX_WEIGHT);
        }

        public AdvancedKillerListSettings(List<AdvancedKillerAwareUtils.MemoryLevelListType> killList2, int ubSortWeightLimit2, int killWeightLimit2) {
            this.killList = killList2;
            this.ubSortWeightLimit = ubSortWeightLimit2;
            this.killWeightLimit = killWeightLimit2;
        }

        @Override // java.lang.Object
        public String toString() {
            return "ubSortLimit: " + this.ubSortWeightLimit + " killLimit: " + this.killWeightLimit + " Lists: " + this.killList;
        }

        private boolean validate() {
            List<AdvancedKillerAwareUtils.MemoryLevelListType> list = this.killList;
            return list != null && !list.isEmpty() && this.ubSortWeightLimit >= 0 && this.killWeightLimit >= 0;
        }
    }

    public AdvancedKiller(Context contextInit) {
        this.context = contextInit;
        this.utils = HwPartIawareUtil.getAdvancedKillerAwareUtils(this.context);
        this.reader = new AdvancedKillerIO();
        this.akParam = ParaConfig.getInstance().getAdvancedKillParam();
        this.stat = Statistics.getInstance();
        if (SystemProperties.get("ro.board.platform", "DEFAULT").startsWith("mt")) {
            this.glBundle = new MtkGlConstantsBundle(this, null);
        }
        logPrint(LogType.CLOUD_INFO_LOG, "Online", "AdvancedKiller");
    }

    public void updateModel(String pkg) {
        logPrint(LogType.CLOUD_INFO_LOG, "FG", pkg);
        this.stat.notifyUpdate(pkg);
        ResourceCollector.updateKillModel(pkg);
    }

    public void serializeModel() {
        logPrint(LogType.CLOUD_INFO_LOG, "Event", "ScreenOff");
        this.stat.dumpData();
        ResourceCollector.serializeKillModel();
    }

    public void notifyAkBg(String pkg) {
        logPrint(LogType.CLOUD_INFO_LOG, "BG", pkg);
    }

    public void notifyScreenOn() {
        logPrint(LogType.CLOUD_INFO_LOG, "Event", "ScreenOn");
    }

    public int execute(Bundle extras) {
        if (this.context == null || this.utils == null || extras == null || shouldInterrupt()) {
            return 0;
        }
        boolean isProcFast = this.utils.setSchedPriority();
        int res = executeInternal(extras);
        this.utils.resetSchedPriority(isProcFast);
        return res;
    }

    public void setInterrupt(boolean interrupt) {
        this.interruptState.set(interrupt);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00e4, code lost:
        logPrint(com.huawei.server.rme.hyperhold.AdvancedKiller.LogType.ERROR_LOG, "ExecuteInternal", "Fail reading memory");
     */
    private int executeInternal(Bundle extras) {
        setTimeForDebug(TimeSector.START_EXECUTION);
        long memAvailable = this.utils.getMemAvailable();
        long requiredMem = extras.getLong("reqMem") * 1024;
        boolean quickKill = extras.getBoolean("quickKill", false);
        String reason = extras.getString("reason", AppActConstant.VALUE_DEFAULT);
        AdvancedKillerListSettings listSettings = parseSettings(extras.getSerializable("listSettings"), quickKill);
        LogType logType = LogType.CLOUD_INFO_LOG;
        StringBuilder sb = new StringBuilder();
        sb.append(requiredMem);
        sb.append(" ");
        sb.append(memAvailable);
        sb.append(" ");
        sb.append(reason);
        sb.append(" ");
        sb.append(quickKill ? "quick" : "def");
        logPrint(logType, "KillStart", sb.toString());
        logPrint(LogType.CLOUD_INFO_LOG, "Settings", listSettings.toString());
        this.stat.notifyKillerStart(quickKill);
        KillerResult killResult = new KillerResult(this, null);
        Iterator it = listSettings.killList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            AdvancedKillerAwareUtils.MemoryLevelListType memLevelList = (AdvancedKillerAwareUtils.MemoryLevelListType) it.next();
            if (killResult.killedMem >= requiredMem) {
                break;
            }
            AdvancedKillerIterationInfo info = createCurrentIterationInfo(memLevelList, quickKill, listSettings);
            dumpDebugMemStat(info);
            setTimeForDebug(TimeSector.START_LEVEL);
            Map<String, AdvancedKillerPackageInfo> packageInfos = this.utils.getPackageInfoMap(memLevelList);
            if (packageInfos == null || packageInfos.isEmpty()) {
                LogType logType2 = LogType.ERROR_LOG;
                logPrint(logType2, "ExecuteInternal", "List empty: " + memLevelList);
                memAvailable = memAvailable;
            } else if (shouldInterrupt()) {
                break;
            } else {
                setTimeForDebug(TimeSector.START_MEM_READ);
                Map<String, MemoryPackageInfo> packageMemInfos = readMemoryData(packageInfos);
                if (packageMemInfos == null || packageMemInfos.isEmpty()) {
                    break;
                }
                setTimeForDebug(TimeSector.START_KILL);
                killResult.add(chooseAndKillPackages(requiredMem, info, packageMemInfos));
                setTimeForDebug(TimeSector.FINISH_EXECUTION);
                logLevelTimeString();
            }
        }
        if (killResult.killedPackages == 0) {
            this.stat.notifyErrorKillerExec();
        }
        logExecTimeString();
        logPrint(LogType.CLOUD_INFO_LOG, "TotalKilledUss", String.valueOf(killResult.killedMem));
        return killResult.killedPackages;
    }

    private Map<String, MemoryPackageInfo> readMemoryData(Map<String, AdvancedKillerPackageInfo> packageInfos) {
        File memcgDir = new File("/dev/memcg/apps/");
        if (!memcgDir.exists() || !memcgDir.isDirectory()) {
            return Collections.emptyMap();
        }
        HashMap<String, MemoryInfo> memoryMap = new HashMap<>();
        this.reader.readDataFile("/dev/memcg/apps/memory.total_info_per_app", memoryMap, new TotalInfoFileLineProcessor(this, null));
        setTimeForDebug(TimeSector.MEMCG_READ_DONE);
        HashMap<Integer, Long> gpuMemoryMap = new HashMap<>();
        this.reader.readDataFile(this.glBundle.fileName, gpuMemoryMap, new GlInfoFileLineProcessor(this.glBundle));
        setTimeForDebug(TimeSector.GPU_READ_DONE);
        if (memoryMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, MemoryPackageInfo> packageMemInfos = new HashMap<>();
        for (Map.Entry<String, AdvancedKillerPackageInfo> packageInfoEntry : packageInfos.entrySet()) {
            AdvancedKillerPackageInfo packageInfo = packageInfoEntry.getValue();
            MemoryInfo memInfo = memoryMap.get(packageInfo.getPackageName());
            if (memInfo == null) {
                memInfo = new MemoryInfo(this, null);
            }
            List<Integer> pids = packageInfo.getPids();
            if (pids != null && !pids.isEmpty() && !gpuMemoryMap.isEmpty()) {
                memInfo.graphicBufferMem = calculateGpuMemory(pids, gpuMemoryMap);
            }
            if (memInfo.anonMem == 0 && memInfo.zramComprMem == 0 && memInfo.eswapComprMem == 0 && pids != null && !pids.isEmpty()) {
                collectMemoryPerProcess(memInfo, pids);
            }
            logPrint(LogType.CLOUD_INFO_LOG, "Candidate", packageInfoEntry.getKey() + " " + packageInfo.getWeight() + " " + memInfo.anonMem + " " + memInfo.zramComprMem + " " + memInfo.graphicBufferMem + " " + memInfo.eswapComprMem);
            packageMemInfos.put(packageInfoEntry.getKey(), new MemoryPackageInfo(this, memInfo, packageInfo, null));
            packageInfo.setTotalMem(memInfo.getTotalMem());
        }
        return packageMemInfos;
    }

    private long calculateGpuMemory(List<Integer> pids, HashMap<Integer, Long> gpuMemoryMap) {
        long gpuMem = 0;
        for (Integer num : pids) {
            Long procGpuMem = gpuMemoryMap.get(Integer.valueOf(num.intValue()));
            if (procGpuMem != null) {
                gpuMem += procGpuMem.longValue();
            }
        }
        return gpuMem;
    }

    public class TotalInfoFileLineProcessor implements AdvancedKillerIO.FileLineProcessor<HashMap<String, MemoryInfo>> {
        private final int TOTAL_ANON_FIELD;
        private final int TOTAL_ESWAP_FIELD;
        private final int TOTAL_FIELDS;
        private final int TOTAL_PKGNAME_FIELD;
        private final int TOTAL_ZRAM_FIELD;

        private TotalInfoFileLineProcessor() {
            AdvancedKiller.this = r1;
            this.TOTAL_PKGNAME_FIELD = 0;
            this.TOTAL_ANON_FIELD = 1;
            this.TOTAL_ZRAM_FIELD = 2;
            this.TOTAL_ESWAP_FIELD = 3;
            this.TOTAL_FIELDS = 4;
        }

        /* synthetic */ TotalInfoFileLineProcessor(AdvancedKiller x0, AnonymousClass1 x1) {
            this();
        }

        public void processFileLine(String memoryFileLine, HashMap<String, MemoryInfo> memoryMap) {
            try {
                String[] memoryDataString = memoryFileLine.split("\\s+");
                if (memoryDataString.length < 4) {
                    AdvancedKiller advancedKiller = AdvancedKiller.this;
                    LogType logType = LogType.ERROR_LOG;
                    advancedKiller.logPrint(logType, "ProcessTotalInfo", "Read wrong line: " + memoryFileLine);
                    return;
                }
                MemoryInfo minfo = new MemoryInfo(AdvancedKiller.this, null);
                minfo.anonMem = Long.parseLong(memoryDataString[1]);
                minfo.zramComprMem = Long.parseLong(memoryDataString[2]);
                minfo.eswapComprMem = Long.parseLong(memoryDataString[3]);
                memoryMap.put(memoryDataString[0], minfo);
            } catch (NumberFormatException e) {
                AdvancedKiller advancedKiller2 = AdvancedKiller.this;
                LogType logType2 = LogType.ERROR_LOG;
                advancedKiller2.logPrint(logType2, "ProcessTotalInfo", "Error in convertion " + memoryFileLine);
            }
        }
    }

    public class GlInfoFileLineProcessor implements AdvancedKillerIO.FileLineProcessor<HashMap<Integer, Long>> {
        private final int PAGE_SIZE = 4;
        private GlConstantsBundle bundle;

        public GlInfoFileLineProcessor(GlConstantsBundle architectureBundle) {
            AdvancedKiller.this = r1;
            this.bundle = architectureBundle;
        }

        public void processFileLine(String memoryFileLine, HashMap<Integer, Long> gpuMemoryMap) {
            try {
                String[] gpuDataString = memoryFileLine.trim().split("\\s+");
                if (gpuDataString.length >= this.bundle.gpuFields) {
                    int tgid = Integer.parseInt(gpuDataString[this.bundle.gpuTgidField]);
                    long gpuMem = Long.parseLong(gpuDataString[this.bundle.gpuUnmappedField]) * 4;
                    if (tgid > 0 && gpuMem > 0) {
                        gpuMemoryMap.put(Integer.valueOf(tgid), Long.valueOf(gpuMem));
                    }
                }
            } catch (NumberFormatException e) {
                AdvancedKiller advancedKiller = AdvancedKiller.this;
                LogType logType = LogType.ERROR_LOG;
                advancedKiller.logPrint(logType, "ProcessGpuInfo", "Error in convertion " + memoryFileLine);
            }
        }
    }

    private void collectMemoryPerProcess(MemoryInfo memInfo, List<Integer> pids) {
        if (!(memInfo == null || pids == null || pids.isEmpty())) {
            for (Integer pid : pids) {
                long[] outUss = new long[2];
                Debug.getPss(pid.intValue(), outUss, null);
                MemoryInfo.access$1214(memInfo, outUss[0]);
                MemoryInfo.access$1314(memInfo, outUss[1] / 3);
            }
            memInfo.graphicBufferMem = 0;
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: checkFilteringConditions */
    private boolean lambda$chooseAndKillPackages$1$AdvancedKiller(MemoryPackageInfo value, AdvancedKillerIterationInfo info, Set<MemoryPackageInfo> weightPackages) {
        if (value == null) {
            return true;
        }
        MemoryInfo memInfo = value.memInfo;
        AdvancedKillerPackageInfo pInfo = value.packageInfo;
        if (memInfo == null || pInfo == null) {
            return true;
        }
        if (memInfo.getTotalMem() < 3072 && !info.lowendList) {
            return true;
        }
        if (info.perProcessKill) {
            return false;
        }
        if (!pInfo.canPackageBeKilled()) {
            logPrint(LogType.CLOUD_INFO_LOG, "ProtectC", pInfo.getPackageName());
            return true;
        } else if (info.quickKill || pInfo.getFreqType() != AdvancedKillerPackageInfo.FrequencyType.FREQUENCY_CRIT) {
            int weight = pInfo.getWeight();
            if (weight >= info.killWeightLimit) {
                return true;
            }
            if (weight < info.ubSortWeightLimit) {
                return false;
            }
            weightPackages.add(value);
            return true;
        } else {
            logPrint(LogType.CLOUD_INFO_LOG, "ProtectF", pInfo.getPackageName());
            return true;
        }
    }

    private KillerResult chooseAndKillPackages(long requiredMem, AdvancedKillerIterationInfo info, Map<String, MemoryPackageInfo> packageInfos) {
        String[] sortedPackages;
        String[] sortedPackages2 = new String[0];
        Set<MemoryPackageInfo> weightPackages = new TreeSet<>($$Lambda$AdvancedKiller$6nYGc580pGhkJff98HBOjkClp6w.INSTANCE);
        packageInfos.values().removeIf(new Predicate(info, weightPackages) {
            /* class com.huawei.server.rme.hyperhold.$$Lambda$AdvancedKiller$27atnzdvEbkplav3cFNZtN0AuY */
            private final /* synthetic */ AdvancedKiller.AdvancedKillerIterationInfo f$1;
            private final /* synthetic */ Set f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AdvancedKiller.this.lambda$chooseAndKillPackages$1$AdvancedKiller(this.f$1, this.f$2, (AdvancedKiller.MemoryPackageInfo) obj);
            }
        });
        if (!info.perProcessKill) {
            String[] sortedPackages3 = sortPackagesWithModel(packageInfos);
            if (sortedPackages3 == null || sortedPackages3.length == 0) {
                logPrint(LogType.ERROR_LOG, "ChoosePackages", "Model returns empty list");
                return null;
            }
            sortedPackages = sortedPackages3;
        } else {
            sortedPackages = sortedPackages2;
        }
        VictimInfo victimInfo = getVictimInfo(requiredMem, packageInfos, weightPackages, sortedPackages, info.perProcessKill);
        if (victimInfo != null && victimInfo.victims != null) {
            if (victimInfo.victims.size() != 0) {
                return killForMem(requiredMem, victimInfo, info);
            }
        }
        logPrint(LogType.ERROR_LOG, "ChoosePackages", "Converting victims failed");
        return null;
    }

    static /* synthetic */ int lambda$chooseAndKillPackages$0(MemoryPackageInfo package1, MemoryPackageInfo package2) {
        return package1.packageInfo.getWeight() - package2.packageInfo.getWeight();
    }

    private String[] sortPackagesWithModel(Map<String, MemoryPackageInfo> packageInfos) {
        String[] resultsFromNative = null;
        String[] packageNamesToNative = new String[packageInfos.size()];
        long[] ussToNative = new long[packageInfos.size()];
        int index = 0;
        for (Map.Entry<String, MemoryPackageInfo> packageInfoEntry : packageInfos.entrySet()) {
            packageNamesToNative[index] = packageInfoEntry.getKey();
            ussToNative[index] = packageInfoEntry.getValue().memInfo.getTotalMem();
            index++;
        }
        if (isDebugLog()) {
            for (int i = 0; i < packageNamesToNative.length; i++) {
                LogType logType = LogType.INFO_LOG;
                logPrint(logType, "FilteredCandidate", packageNamesToNative[i] + " " + ussToNative[i]);
            }
        }
        if (packageNamesToNative.length != 0) {
            resultsFromNative = ResourceCollector.sortWithKillModel(packageNamesToNative, ussToNative);
        } else {
            logPrint(LogType.ERROR_LOG, "SortPackages", "Nothing to send to model");
        }
        return resultsFromNative == null ? new String[0] : resultsFromNative;
    }

    private VictimInfo getVictimInfo(long requiredMem, Map<String, MemoryPackageInfo> packageInfos, Set<MemoryPackageInfo> prioritizedPackages, String[] sortedPackages, boolean isGreedy) {
        if (sortedPackages == null || sortedPackages.length == 0) {
            return getVictimInfoWithoutSort(packageInfos, prioritizedPackages);
        }
        List<MemoryPackageInfo> victimList = new ArrayList<>(sortedPackages.length);
        int index = 0;
        int emergIndex = sortedPackages.length + prioritizedPackages.size();
        long accumMem = 0;
        if (!isGreedy) {
            Stack<MemoryPackageInfo> greedyChoice = new Stack<>();
            Stack<MemoryPackageInfo> filteredChoice = new Stack<>();
            while (index < sortedPackages.length && accumMem < requiredMem) {
                int index2 = index + 1;
                MemoryPackageInfo packageInfo = packageInfos.get(sortedPackages[index]);
                if (packageInfo != null) {
                    accumMem += packageInfo.memInfo.getTotalMem();
                    greedyChoice.push(packageInfo);
                }
                index = index2;
            }
            while (!greedyChoice.empty()) {
                MemoryPackageInfo chosenPackage = greedyChoice.pop();
                long memSize = chosenPackage.memInfo.getTotalMem();
                if (accumMem - memSize >= requiredMem) {
                    filteredChoice.push(chosenPackage);
                    accumMem -= memSize;
                } else {
                    victimList.add(chosenPackage);
                }
            }
            emergIndex = victimList.size();
            while (!filteredChoice.empty()) {
                victimList.add(filteredChoice.pop());
            }
        }
        for (int i = index; i < sortedPackages.length; i++) {
            MemoryPackageInfo packageInfo2 = packageInfos.get(sortedPackages[i]);
            if (packageInfo2 != null) {
                victimList.add(packageInfo2);
            }
        }
        for (MemoryPackageInfo packageInfo3 : prioritizedPackages) {
            victimList.add(packageInfo3);
            if (!isGreedy && accumMem < requiredMem) {
                accumMem += packageInfo3.memInfo.getTotalMem();
                emergIndex++;
            }
        }
        return new VictimInfo(this, victimList, emergIndex, null);
    }

    private VictimInfo getVictimInfoWithoutSort(Map<String, MemoryPackageInfo> packageInfos, Set<MemoryPackageInfo> prioritizedPackages) {
        List<MemoryPackageInfo> victims = new ArrayList<>();
        victims.addAll(packageInfos.values());
        victims.addAll(prioritizedPackages);
        return new VictimInfo(this, victims, victims.size(), null);
    }

    private KillerResult killForMem(long requiredMem, VictimInfo victimInfo, AdvancedKillerIterationInfo info) {
        String killType;
        List<MemoryPackageInfo> victimList = victimInfo.victims;
        KillerResult result = new KillerResult(this, null);
        this.utils.beginKillFast();
        int i = 0;
        while (i < victimList.size() && result.killedMem < requiredMem && !shouldInterrupt()) {
            MemoryPackageInfo victimMemoryPackageInfo = victimList.get(i);
            AdvancedKillerPackageInfo victim = victimMemoryPackageInfo.packageInfo;
            String killedName = info.perProcessKill ? victim.getFirstProcessName() : victim.getPackageName();
            List<Integer> pids = victim.killPackage(this.context, this.interruptState, info.lowendList);
            if (pids == null || pids.isEmpty()) {
                LogType logType = LogType.ERROR_LOG;
                logPrint(logType, "KillForMem", "Failed to kill " + killedName);
                this.stat.notifyFailedKill();
            } else {
                if (info.perProcessKill) {
                    killType = "per-process emerg";
                    victim.killNotifyCachedClean();
                } else {
                    victim.killNotifyPackageTracker();
                    this.stat.notifyKilledPackage(killedName);
                    killType = i < victimInfo.emergIndex ? "regular" : "emerg";
                    KillerResult.access$614(result, victimMemoryPackageInfo.memInfo.getTotalMem());
                    KillerResult.access$808(result);
                }
                LogType logType2 = LogType.CLOUD_INFO_LOG;
                logPrint(logType2, "Killed", killedName + " " + killType);
            }
            i++;
        }
        this.utils.endKillFast();
        return result;
    }

    private boolean isDebugLog() {
        return this.akParam.getAkEnableDebug();
    }

    private boolean isTimeDebugMeasurement() {
        return this.akParam.getAkEnableDebugTime();
    }

    private void setTimeForDebug(TimeSector sector) {
        ExecutionTimeMeasurement executionTimeMeasurement;
        if (isTimeDebugMeasurement() && (executionTimeMeasurement = this.timeValue) != null) {
            executionTimeMeasurement.storeTimeForSectorNow(sector);
        }
    }

    private void logLevelTimeString() {
        if (isTimeDebugMeasurement() && this.timeValue != null) {
            logPrint(LogType.TIME_LOG, "LevelTime", this.timeValue.dumpLevelTime());
        }
    }

    private void logExecTimeString() {
        if (isTimeDebugMeasurement() && this.timeValue != null) {
            logPrint(LogType.TIME_LOG, "TotalTime", this.timeValue.dumpExecTime());
        }
    }

    private boolean shouldInterrupt() {
        boolean interrupt = this.interruptState.get();
        if (interrupt) {
            logPrint(LogType.INFO_LOG, "Interrupt", "Interrupt was triggered!");
        }
        return interrupt;
    }

    /* renamed from: com.huawei.server.rme.hyperhold.AdvancedKiller$1 */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$LogType = new int[LogType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$LogType[LogType.CLOUD_INFO_LOG.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$LogType[LogType.INFO_LOG.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$LogType[LogType.IMPORTANT_LOG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$LogType[LogType.TIME_LOG.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$LogType[LogType.ERROR_LOG.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$TimeSector = new int[TimeSector.values().length];
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$TimeSector[TimeSector.START_EXECUTION.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$TimeSector[TimeSector.START_LEVEL.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$TimeSector[TimeSector.START_MEM_READ.ordinal()] = 3;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$TimeSector[TimeSector.START_KILL.ordinal()] = 4;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$TimeSector[TimeSector.FINISH_EXECUTION.ordinal()] = 5;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$TimeSector[TimeSector.MEMCG_READ_DONE.ordinal()] = 6;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$TimeSector[TimeSector.GPU_READ_DONE.ordinal()] = 7;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    private void logPrint(LogType logType, String subtag, String message) {
        String fullMessage = subtag + " " + message;
        int i = AnonymousClass1.$SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKiller$LogType[logType.ordinal()];
        if (i == 1) {
            this.stat.writeLog(fullMessage);
            Slog.i("AdvancedKiller", fullMessage);
        } else if (i != 2) {
            if (i == 3) {
                Slog.i("AdvancedKiller", fullMessage);
            } else if (i != 4) {
                if (i != 5) {
                    Slog.w("AdvancedKiller", "AkLog Unknown type of log: " + fullMessage);
                    return;
                }
                Slog.e("AdvancedKiller", fullMessage);
            } else if (isTimeDebugMeasurement()) {
                Slog.i("AdvancedKiller", fullMessage);
            }
        } else if (isDebugLog()) {
            Slog.i("AdvancedKiller", fullMessage);
        }
    }

    private AdvancedKillerListSettings parseSettings(Serializable rawListSettings, boolean quickKill) {
        List list;
        if (rawListSettings != null && (rawListSettings instanceof AdvancedKillerListSettings)) {
            AdvancedKillerListSettings listSettings = (AdvancedKillerListSettings) rawListSettings;
            if (listSettings.validate()) {
                return listSettings;
            }
            LogType logType = LogType.CLOUD_INFO_LOG;
            logPrint(logType, "Settings", "Invalid settings " + listSettings);
        }
        if (quickKill) {
            list = AdvancedKillerListSettings.QUICK_KILL_LISTS;
        } else {
            list = AdvancedKillerListSettings.REGULAR_KILL_LISTS;
        }
        return new AdvancedKillerListSettings(list);
    }

    private AdvancedKillerIterationInfo createCurrentIterationInfo(AdvancedKillerAwareUtils.MemoryLevelListType memLevelList, boolean quickKill, AdvancedKillerListSettings listSettings) {
        AdvancedKillerIterationInfo info = new AdvancedKillerIterationInfo(this, null);
        info.quickKill = quickKill;
        boolean z = true;
        info.perProcessKill = memLevelList == AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_CACHED_PROCESS;
        if (!(memLevelList == AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_4 || memLevelList == AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_5)) {
            z = false;
        }
        info.lowendList = z;
        info.ubSortWeightLimit = listSettings.ubSortWeightLimit;
        info.killWeightLimit = listSettings.killWeightLimit;
        return info;
    }

    private void dumpDebugMemStat(AdvancedKillerIterationInfo info) {
        if (info.perProcessKill) {
            this.stat.dumpDebugMemStat();
        }
    }
}
