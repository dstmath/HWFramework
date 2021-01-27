package com.android.server.rms.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.rms.iaware.bigdata.BigDataSupervisor;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.memrepair.ProcStateStatisData;
import com.huawei.android.internal.os.SomeArgsEx;
import com.huawei.android.os.HandlerEx;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class AppCleanupDumpRadar extends BigDataSupervisor {
    private static final int INIT_DETAILED_REASON_MAP_SIZE = 2;
    private static final boolean IS_BETA_USER = (AwareConstant.CURRENT_USER_TYPE == 3);
    private static final Object LOCK = new Object();
    private static final int MSG_MEMORY_DATA = 1;
    private static final String TAG = "AppCleanupDumpRadar";
    private static boolean debug = false;
    private static volatile AppCleanupDumpRadar sCleanDumpRadar;
    private final ArrayMap<String, CleanupData> mCleanupDataList = new ArrayMap<>();
    private long mCrashStartTime;
    private Handler mHandler;
    private long mMemStartTime;
    private long mPgStartTime = System.currentTimeMillis();
    private long mSmStartTime;
    private long mSmartStartTime;
    private long mThermalStartTime;

    public AppCleanupDumpRadar() {
        long j = this.mPgStartTime;
        this.mSmStartTime = j;
        this.mCrashStartTime = j;
        this.mSmartStartTime = j;
        this.mMemStartTime = j;
        this.mThermalStartTime = j;
    }

    /* access modifiers changed from: private */
    public static class CleanupData {
        private static final int INVALID_VALUE = -1;
        private static boolean debug = false;
        private Map<String, Integer> mCrashCleanup;
        private int mCrashTotal;
        private Map<String, Integer> mMemoryCleanup;
        private int mMemoryTotal;
        private String mPackageName;
        private Map<String, Integer> mPgCleanup;
        private int mPgTotal;
        private Map<String, Integer> mPgnCleanup;
        private Map<String, Integer> mSmCleanup;
        private int mSmTotal;
        private Map<String, Integer> mSmartCleanup;
        private int mSmartTotal;
        private Map<String, Integer> mThermalCleanup;
        private int mThermalTotal;

        /* synthetic */ CleanupData(String x0, AnonymousClass1 x1) {
            this(x0);
        }

        private CleanupData(String pkg) {
            this.mPackageName = pkg;
            this.mPgTotal = 0;
            this.mSmTotal = 0;
            this.mCrashTotal = 0;
            this.mSmartTotal = 0;
            this.mMemoryTotal = 0;
            this.mThermalTotal = 0;
            this.mPgCleanup = new LinkedHashMap();
            this.mPgnCleanup = new LinkedHashMap();
            this.mSmCleanup = new LinkedHashMap();
            this.mCrashCleanup = new LinkedHashMap();
            this.mSmartCleanup = new LinkedHashMap();
            this.mMemoryCleanup = new LinkedHashMap();
            this.mThermalCleanup = new LinkedHashMap();
            this.mPgCleanup.put("lvl0", 0);
            this.mPgCleanup.put("lvl1", 0);
            this.mPgnCleanup.put("nlvl0", 0);
            this.mPgnCleanup.put("nlvl1", 0);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void increase(AppMngConstant.AppCleanSource source, Map<String, Integer> data) {
            if (data != null && source != null) {
                if (debug) {
                    for (Map.Entry<String, Integer> entry : data.entrySet()) {
                        AwareLog.i(AppCleanupDumpRadar.TAG, "key = " + entry.getKey() + ", value = " + entry.getValue());
                    }
                }
                switch (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[source.ordinal()]) {
                    case 1:
                        increasePg(data);
                        return;
                    case 2:
                        increaseSm(data);
                        return;
                    case 3:
                        increaseCrash(data);
                        return;
                    case 4:
                        increaseSmart(data);
                        return;
                    case 5:
                        increaseMemory(data);
                        return;
                    case 6:
                        increaseThermal(data);
                        return;
                    default:
                        return;
                }
            }
        }

        private void addTimes(Map<String, Integer> cleanup, String key) {
            if (cleanup.containsKey(key)) {
                cleanup.put(key, Integer.valueOf(cleanup.get(key).intValue() + 1));
            } else {
                cleanup.put(key, 1);
            }
        }

        private String getCondition(Map<String, Integer> data) {
            int i;
            StringBuilder condition = new StringBuilder("");
            Integer spec = data.get("spec");
            if (spec != null && spec.intValue() >= 0 && spec.intValue() < AppMngConstant.CleanReason.values().length) {
                return condition.append(AppMngConstant.CleanReason.values()[spec.intValue()].getAbbr()).toString();
            }
            RuleParserUtil.AppMngTag[] values = RuleParserUtil.AppMngTag.values();
            for (RuleParserUtil.AppMngTag enums : values) {
                Integer value = data.get(enums.getDesc());
                if (!(value == null || value.intValue() == -1 || (i = AnonymousClass1.$SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppMngTag[enums.ordinal()]) == 1 || i == 2)) {
                    if (i != 3) {
                        if (i != 4) {
                            condition.append(ProcStateStatisData.SEPERATOR_CHAR);
                            condition.append(enums.getUploadBdTag());
                            condition.append(":");
                            condition = condition.append(String.valueOf(value));
                        } else {
                            condition.append(ProcStateStatisData.SEPERATOR_CHAR);
                            condition = condition.append(enums.getUploadBdTag());
                        }
                    } else if (value.intValue() >= 0 && value.intValue() < AppStatusUtils.Status.values().length) {
                        condition = condition.append(AppStatusUtils.Status.values()[value.intValue()].description());
                    }
                }
            }
            return condition.toString();
        }

        private void increasePg(Map<String, Integer> data) {
            Integer policy = data.get(MemoryConstant.MEM_SYSTRIM_POLICY);
            Integer level = data.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
            if (policy != null && level != null) {
                if (ProcessCleaner.CleanType.NONE.ordinal() == policy.intValue()) {
                    increasePgn(data);
                } else if (ProcessCleaner.CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= ProcessCleaner.CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mPgCleanup, ProcessCleaner.CleanType.values()[policy.intValue()].description());
                    addTimes(this.mPgCleanup, "lvl" + level);
                    this.mPgTotal++;
                }
            }
        }

        private void increasePgn(Map<String, Integer> data) {
            String condition = getCondition(data);
            if (!"".equals(condition)) {
                addTimes(this.mPgnCleanup, condition);
            } else {
                addTimes(this.mPgnCleanup, "others");
            }
            addTimes(this.mPgnCleanup, "nlvl" + data.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL));
            this.mPgTotal = this.mPgTotal + 1;
        }

        private void increaseSm(Map<String, Integer> data) {
            Integer policy = data.get(MemoryConstant.MEM_SYSTRIM_POLICY);
            if (policy != null) {
                if (ProcessCleaner.CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= ProcessCleaner.CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                    return;
                }
                addTimes(this.mSmCleanup, ProcessCleaner.CleanType.values()[policy.intValue()].description());
                this.mSmTotal++;
            }
        }

        private void increaseCrash(Map<String, Integer> data) {
            Integer policy = data.get(MemoryConstant.MEM_SYSTRIM_POLICY);
            if (policy != null) {
                if (ProcessCleaner.CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (!"".equals(condition)) {
                        addTimes(this.mCrashCleanup, condition);
                    } else {
                        addTimes(this.mCrashCleanup, "others");
                    }
                    this.mCrashTotal++;
                } else if (ProcessCleaner.CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= ProcessCleaner.CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mCrashCleanup, ProcessCleaner.CleanType.values()[policy.intValue()].description());
                    this.mCrashTotal++;
                }
            }
        }

        private void increaseSmart(Map<String, Integer> data) {
            Integer policy = data.get(MemoryConstant.MEM_SYSTRIM_POLICY);
            if (policy != null) {
                if (ProcessCleaner.CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (!"".equals(condition)) {
                        addTimes(this.mSmartCleanup, condition);
                    } else {
                        addTimes(this.mSmartCleanup, "others");
                    }
                    this.mSmartTotal++;
                } else if (ProcessCleaner.CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= ProcessCleaner.CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mSmartCleanup, ProcessCleaner.CleanType.values()[policy.intValue()].description());
                    this.mSmartTotal++;
                }
            }
        }

        private void increaseMemory(Map<String, Integer> data) {
            Integer policy = data.get(MemoryConstant.MEM_SYSTRIM_POLICY);
            if (policy != null) {
                Integer level = data.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
                if (!(level == null || level.intValue() == -1)) {
                    addTimes(this.mMemoryCleanup, "lvl" + level);
                }
                if (ProcessCleaner.CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (!"".equals(condition)) {
                        addTimes(this.mMemoryCleanup, condition);
                    } else {
                        addTimes(this.mMemoryCleanup, "others");
                    }
                    this.mMemoryTotal++;
                } else if (ProcessCleaner.CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= ProcessCleaner.CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mMemoryCleanup, ProcessCleaner.CleanType.values()[policy.intValue()].description());
                    this.mMemoryTotal++;
                }
            }
        }

        private void increaseThermal(Map<String, Integer> data) {
            Integer policy = data.get(MemoryConstant.MEM_SYSTRIM_POLICY);
            if (policy != null) {
                if (ProcessCleaner.CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (!"".equals(condition)) {
                        addTimes(this.mThermalCleanup, condition);
                    } else {
                        addTimes(this.mThermalCleanup, "others");
                    }
                    this.mThermalTotal++;
                } else if (ProcessCleaner.CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= ProcessCleaner.CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mThermalCleanup, ProcessCleaner.CleanType.values()[policy.intValue()].description());
                    this.mThermalTotal++;
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private JSONObject makeJson(int total, Map<String, Integer> cleanup) {
            JSONObject jsonObj = new JSONObject();
            if (total > 0) {
                try {
                    jsonObj.put("pkg", this.mPackageName);
                    jsonObj.put("total", total);
                    for (Map.Entry<String, Integer> entry : cleanup.entrySet()) {
                        jsonObj.put(entry.getKey(), entry.getValue());
                    }
                } catch (JSONException e) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "makeJson catch JSONException e: " + e);
                }
            }
            return jsonObj;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.rms.iaware.srms.AppCleanupDumpRadar$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource = new int[AppMngConstant.AppCleanSource.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppMngTag = new int[RuleParserUtil.AppMngTag.values().length];

        static {
            try {
                $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppMngTag[RuleParserUtil.AppMngTag.POLICY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppMngTag[RuleParserUtil.AppMngTag.LEVEL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppMngTag[RuleParserUtil.AppMngTag.STATUS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppMngTag[RuleParserUtil.AppMngTag.OVERSEA.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.POWER_GENIE.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.SYSTEM_MANAGER.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.CRASH.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.SMART_CLEAN.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.MEMORY.ordinal()] = 5;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.THERMAL.ordinal()] = 6;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    public static AppCleanupDumpRadar getInstance() {
        if (sCleanDumpRadar == null) {
            synchronized (LOCK) {
                if (sCleanDumpRadar == null) {
                    sCleanDumpRadar = new AppCleanupDumpRadar();
                }
            }
        }
        return sCleanDumpRadar;
    }

    private String makeCleanJson(AppMngConstant.AppCleanSource source) {
        StringBuilder cleanupStat = new StringBuilder("");
        for (Map.Entry<String, CleanupData> item : this.mCleanupDataList.entrySet()) {
            CleanupData cleanup = item.getValue();
            switch (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[source.ordinal()]) {
                case 1:
                    Map<String, Integer> pgCleanup = new LinkedHashMap<>();
                    pgCleanup.putAll(cleanup.mPgCleanup);
                    pgCleanup.putAll(cleanup.mPgnCleanup);
                    JSONObject pjJson = cleanup.makeJson(cleanup.mPgTotal, pgCleanup);
                    if (pjJson.length() != 0) {
                        cleanupStat.append("\n");
                        cleanupStat.append(pjJson.toString());
                        break;
                    } else {
                        break;
                    }
                case 2:
                    JSONObject smJson = cleanup.makeJson(cleanup.mSmTotal, cleanup.mSmCleanup);
                    if (smJson.length() != 0) {
                        cleanupStat.append("\n");
                        cleanupStat.append(smJson.toString());
                        break;
                    } else {
                        break;
                    }
                case 3:
                    JSONObject crashJson = cleanup.makeJson(cleanup.mCrashTotal, cleanup.mCrashCleanup);
                    if (crashJson.length() != 0) {
                        cleanupStat.append("\n");
                        cleanupStat.append(crashJson.toString());
                        break;
                    } else {
                        break;
                    }
                case 4:
                    JSONObject smartJson = cleanup.makeJson(cleanup.mSmartTotal, cleanup.mSmartCleanup);
                    if (smartJson.length() != 0) {
                        cleanupStat.append("\n");
                        cleanupStat.append(smartJson.toString());
                        break;
                    } else {
                        break;
                    }
                case 5:
                    JSONObject memJson = cleanup.makeJson(cleanup.mMemoryTotal, cleanup.mMemoryCleanup);
                    if (memJson.length() != 0) {
                        cleanupStat.append("\n");
                        cleanupStat.append(memJson.toString());
                        break;
                    } else {
                        break;
                    }
                case 6:
                    JSONObject thermalJson = cleanup.makeJson(cleanup.mThermalTotal, cleanup.mThermalCleanup);
                    if (thermalJson.length() != 0) {
                        cleanupStat.append("\n");
                        cleanupStat.append(thermalJson.toString());
                        break;
                    } else {
                        break;
                    }
            }
        }
        return cleanupStat.toString();
    }

    public String saveCleanBigData(boolean clear) {
        String sb;
        synchronized (this.mCleanupDataList) {
            long updateTime = System.currentTimeMillis();
            StringBuilder data = getThermalStartTime(clear, updateTime, getMemStartTime(clear, updateTime, getSmartStartTime(clear, updateTime, getCrashStartTime(clear, updateTime, getSmStartTime(clear, updateTime, getPgStartTime(clear, updateTime, new StringBuilder("")))))));
            if (clear) {
                this.mCleanupDataList.clear();
            }
            sb = data.toString();
        }
        return sb;
    }

    public void updateCleanData(String pkg, AppMngConstant.AppCleanSource source, Map<String, Integer> data) {
        if (IS_BETA_USER) {
            if (debug) {
                AwareLog.i(TAG, "updateCleanData pkg = " + pkg + ", source = " + source + ", data = " + data);
            }
            if (pkg != null && !pkg.isEmpty()) {
                synchronized (this.mCleanupDataList) {
                    if (!canRecord(this, TAG)) {
                        AwareLog.d(TAG, "canRecord false, maybe the bigdata cache is full");
                        return;
                    }
                    CleanupData cleanup = this.mCleanupDataList.get(pkg);
                    if (cleanup != null) {
                        cleanup.increase(source, data);
                    } else {
                        cleanup = new CleanupData(pkg, null);
                        cleanup.increase(source, data);
                    }
                    this.mCleanupDataList.put(pkg, cleanup);
                }
            }
        }
    }

    public void dumpBigData(PrintWriter pw) {
        pw.println("" + saveCleanBigData(false));
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [android.os.Handler, com.android.server.rms.iaware.srms.AppCleanupDumpRadar$DumpRadarHandler] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setHandler(Handler handler) {
        if (handler != null) {
            this.mHandler = new DumpRadarHandler(handler.getLooper());
        }
    }

    private StringBuilder getThermalStartTime(boolean clear, long updateTime, StringBuilder data) {
        String thermalStr = makeCleanJson(AppMngConstant.AppCleanSource.THERMAL);
        if (!"".equals(thermalStr)) {
            data.append("\n[iAwareAppThermalClean_Start]");
            data.append("\nstartTime: ");
            data.append(String.valueOf(this.mThermalStartTime));
            data.append(thermalStr);
            data.append("\nendTime: ");
            data.append(String.valueOf(updateTime));
            data = data.append("\n[iAwareAppThermalClean_End]");
            this.mThermalStartTime = clear ? updateTime : this.mThermalStartTime;
        }
        return data;
    }

    private StringBuilder getMemStartTime(boolean clear, long updateTime, StringBuilder data) {
        String memStr = makeCleanJson(AppMngConstant.AppCleanSource.MEMORY);
        if (!"".equals(memStr)) {
            data.append("\n[iAwareAppMemoryClean_Start]");
            data.append("\nstartTime: ");
            data.append(String.valueOf(this.mMemStartTime));
            data.append(memStr);
            data.append("\nendTime: ");
            data.append(String.valueOf(updateTime));
            data = data.append("\n[iAwareAppMemoryClean_End]");
            this.mMemStartTime = clear ? updateTime : this.mMemStartTime;
        }
        return data;
    }

    private StringBuilder getSmartStartTime(boolean clear, long updateTime, StringBuilder data) {
        String smartStr = makeCleanJson(AppMngConstant.AppCleanSource.SMART_CLEAN);
        if (!"".equals(smartStr)) {
            data.append("\n[iAwareAppSmartClean_Start]");
            data.append("\nstartTime: ");
            data.append(String.valueOf(this.mSmartStartTime));
            data.append(smartStr);
            data.append("\nendTime: ");
            data.append(String.valueOf(updateTime));
            data = data.append("\n[iAwareAppSmartClean_End]");
            this.mSmartStartTime = clear ? updateTime : this.mSmartStartTime;
        }
        return data;
    }

    private StringBuilder getCrashStartTime(boolean clear, long updateTime, StringBuilder data) {
        String crashStr = makeCleanJson(AppMngConstant.AppCleanSource.CRASH);
        if (!"".equals(crashStr)) {
            data.append("\n[iAwareAppCrashClean_Start]");
            data.append("\nstartTime: ");
            data.append(String.valueOf(this.mCrashStartTime));
            data.append(crashStr);
            data.append("\nendTime: ");
            data.append(String.valueOf(updateTime));
            data = data.append("\n[iAwareAppCrashClean_End]");
            this.mCrashStartTime = clear ? updateTime : this.mCrashStartTime;
        }
        return data;
    }

    private StringBuilder getSmStartTime(boolean clear, long updateTime, StringBuilder data) {
        String smStr = makeCleanJson(AppMngConstant.AppCleanSource.SYSTEM_MANAGER);
        if (!"".equals(smStr)) {
            data.append("\n[iAwareAppSMClean_Start]");
            data.append("\nstartTime: ");
            data.append(String.valueOf(this.mSmStartTime));
            data.append(smStr);
            data.append("\nendTime: ");
            data.append(String.valueOf(updateTime));
            data = data.append("\n[iAwareAppSMClean_End]");
            this.mSmStartTime = clear ? updateTime : this.mSmStartTime;
        }
        return data;
    }

    private StringBuilder getPgStartTime(boolean clear, long updateTime, StringBuilder data) {
        String pgStr = makeCleanJson(AppMngConstant.AppCleanSource.POWER_GENIE);
        if (!"".equals(pgStr)) {
            data.append("\n[iAwareAppPGClean_Start]");
            data.append("\nstartTime: ");
            data.append(String.valueOf(this.mPgStartTime));
            data.append(pgStr);
            data.append("\nendTime: ");
            data.append(String.valueOf(updateTime));
            data = data.append("\n[iAwareAppPGClean_End]");
            this.mPgStartTime = clear ? updateTime : this.mPgStartTime;
        }
        return data;
    }

    @Override // com.android.server.rms.iaware.bigdata.BigDataSupervisor
    public int monitorBigDataRecord() {
        int size;
        synchronized (this.mCleanupDataList) {
            size = this.mCleanupDataList.size();
        }
        return size;
    }

    /* access modifiers changed from: private */
    public final class DumpRadarHandler extends HandlerEx {
        public DumpRadarHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1 && (msg.obj instanceof SomeArgsEx)) {
                SomeArgsEx args = (SomeArgsEx) msg.obj;
                if (args.arg1() instanceof List) {
                    int position = ((Integer) args.arg2()).intValue();
                    AppCleanupDumpRadar.this.processMemoryData((List) args.arg1(), position);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processMemoryData(List<AwareProcessBlockInfo> blockInfos, int position) {
        int size = blockInfos.size();
        for (int i = 0; i < size; i++) {
            AwareProcessBlockInfo info = blockInfos.get(i);
            if (info != null) {
                if (i <= position) {
                    info.procCleanType = ProcessCleaner.CleanType.NONE;
                    info.procReason = AppMngConstant.CleanReason.MEMORY_ENOUGH.getCode();
                    HashMap<String, Integer> detailedReason = new HashMap<>(2);
                    detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.NONE.ordinal()));
                    detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.MEMORY_ENOUGH.ordinal()));
                    info.procDetailedReason = detailedReason;
                }
                updateCleanData(info.procPackageName, AppMngConstant.AppCleanSource.MEMORY, info.procDetailedReason);
            } else {
                return;
            }
        }
    }

    public void reportMemoryData(List<AwareProcessBlockInfo> blockInfos, int position) {
        Handler handler;
        if (blockInfos != null && position < blockInfos.size() && (handler = this.mHandler) != null) {
            Message msg = handler.obtainMessage();
            msg.what = 1;
            SomeArgsEx args = SomeArgsEx.obtain();
            args.setArg1(blockInfos);
            args.setArg2(Integer.valueOf(position));
            msg.obj = args;
            this.mHandler.sendMessage(msg);
        }
    }
}
