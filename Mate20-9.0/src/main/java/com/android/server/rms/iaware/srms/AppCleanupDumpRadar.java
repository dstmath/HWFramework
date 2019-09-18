package com.android.server.rms.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.internal.os.SomeArgs;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class AppCleanupDumpRadar {
    private static boolean DEBUG = false;
    private static final int MSG_MEMORY_DATA = 1;
    private static final String TAG = "AppCleanupDumpRadar";
    private static final boolean isBetaUser = (AwareConstant.CURRENT_USER_TYPE == 3);
    private static volatile AppCleanupDumpRadar mCleanDumpRadar;
    private ArrayMap<String, CleanupData> mCleanupDataList = new ArrayMap<>();
    private long mCrashStartTime = this.mPGStartTime;
    private Handler mHandler;
    private long mMemStartTime = this.mPGStartTime;
    private long mPGStartTime = System.currentTimeMillis();
    private long mSMStartTime = this.mPGStartTime;
    private long mSmartStartTime = this.mPGStartTime;
    private long mThermalStartTime = this.mPGStartTime;

    /* renamed from: com.android.server.rms.iaware.srms.AppCleanupDumpRadar$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource = new int[AppMngConstant.AppCleanSource.values().length];

        static {
            $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppMngTag = new int[RuleParserUtil.AppMngTag.values().length];
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

    private static class CleanupData {
        private static boolean DEBUG = false;
        private static final int INVALID_VALUE = -1;
        /* access modifiers changed from: private */
        public Map<String, Integer> mCrashCleanup;
        /* access modifiers changed from: private */
        public int mCrashTotal;
        /* access modifiers changed from: private */
        public Map<String, Integer> mMemoryCleanup;
        /* access modifiers changed from: private */
        public int mMemoryTotal;
        /* access modifiers changed from: private */
        public Map<String, Integer> mPGCleanup;
        /* access modifiers changed from: private */
        public Map<String, Integer> mPGNCleanup;
        /* access modifiers changed from: private */
        public int mPGTotal;
        private String mPackageName;
        /* access modifiers changed from: private */
        public Map<String, Integer> mSMCleanup;
        /* access modifiers changed from: private */
        public int mSMTotal;
        /* access modifiers changed from: private */
        public Map<String, Integer> mSmartCleanup;
        /* access modifiers changed from: private */
        public int mSmartTotal;
        /* access modifiers changed from: private */
        public Map<String, Integer> mThermalCleanup;
        /* access modifiers changed from: private */
        public int mThermalTotal;

        /* synthetic */ CleanupData(String x0, AnonymousClass1 x1) {
            this(x0);
        }

        private CleanupData(String pkg) {
            this.mPackageName = pkg;
            this.mPGTotal = 0;
            this.mSMTotal = 0;
            this.mCrashTotal = 0;
            this.mSmartTotal = 0;
            this.mMemoryTotal = 0;
            this.mThermalTotal = 0;
            this.mPGCleanup = new LinkedHashMap();
            this.mPGNCleanup = new LinkedHashMap();
            this.mSMCleanup = new LinkedHashMap();
            this.mCrashCleanup = new LinkedHashMap();
            this.mSmartCleanup = new LinkedHashMap();
            this.mMemoryCleanup = new LinkedHashMap();
            this.mThermalCleanup = new LinkedHashMap();
            this.mPGCleanup.put("lvl0", 0);
            this.mPGCleanup.put("lvl1", 0);
            this.mPGNCleanup.put("nlvl0", 0);
            this.mPGNCleanup.put("nlvl1", 0);
        }

        /* access modifiers changed from: private */
        public void increase(AppMngConstant.AppCleanSource source, Map<String, Integer> data) {
            if (data != null && source != null) {
                if (DEBUG) {
                    for (Map.Entry<String, Integer> entry : data.entrySet()) {
                        AwareLog.i(AppCleanupDumpRadar.TAG, "key = " + entry.getKey() + ", value = " + entry.getValue());
                    }
                }
                switch (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[source.ordinal()]) {
                    case 1:
                        increasePG(data);
                        break;
                    case 2:
                        increaseSM(data);
                        break;
                    case 3:
                        increaseCrash(data);
                        break;
                    case 4:
                        increaseSmart(data);
                        break;
                    case 5:
                        increaseMemory(data);
                        break;
                    case 6:
                        increaseThermal(data);
                        break;
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
            StringBuilder condition = new StringBuilder("");
            Integer spec = data.get("spec");
            if (spec != null && spec.intValue() >= 0 && spec.intValue() < AppMngConstant.CleanReason.values().length) {
                return condition.append(AppMngConstant.CleanReason.values()[spec.intValue()].getAbbr()).toString();
            }
            for (RuleParserUtil.AppMngTag enums : RuleParserUtil.AppMngTag.values()) {
                Integer value = data.get(enums.getDesc());
                if (!(value == null || -1 == value.intValue())) {
                    switch (enums) {
                        case POLICY:
                        case LEVEL:
                            break;
                        case STATUS:
                            if (value.intValue() >= 0 && value.intValue() < AppStatusUtils.Status.values().length) {
                                condition = condition.append(AppStatusUtils.Status.values()[value.intValue()].description());
                                break;
                            }
                        case OVERSEA:
                            condition.append("|");
                            condition = condition.append(enums.getUploadBDTag());
                            break;
                        default:
                            condition.append("|");
                            condition.append(enums.getUploadBDTag());
                            condition.append(":");
                            condition = condition.append(String.valueOf(value));
                            break;
                    }
                }
            }
            return condition.toString();
        }

        private void increasePG(Map<String, Integer> data) {
            Integer policy = data.get("policy");
            Integer level = data.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
            if (policy != null && level != null) {
                if (ProcessCleaner.CleanType.NONE.ordinal() == policy.intValue()) {
                    increasePGN(data);
                } else if (ProcessCleaner.CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= ProcessCleaner.CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mPGCleanup, ProcessCleaner.CleanType.values()[policy.intValue()].description());
                    addTimes(this.mPGCleanup, "lvl" + level);
                    this.mPGTotal++;
                }
            }
        }

        private void increasePGN(Map<String, Integer> data) {
            String condition = getCondition(data);
            if (!condition.equals("")) {
                addTimes(this.mPGNCleanup, condition);
            } else {
                addTimes(this.mPGNCleanup, "others");
            }
            addTimes(this.mPGNCleanup, "nlvl" + data.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL));
            this.mPGTotal = this.mPGTotal + 1;
        }

        private void increaseSM(Map<String, Integer> data) {
            Integer policy = data.get("policy");
            if (policy != null) {
                if (ProcessCleaner.CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= ProcessCleaner.CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mSMCleanup, ProcessCleaner.CleanType.values()[policy.intValue()].description());
                    this.mSMTotal++;
                }
            }
        }

        private void increaseCrash(Map<String, Integer> data) {
            Integer policy = data.get("policy");
            if (policy != null) {
                if (ProcessCleaner.CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (!condition.equals("")) {
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
            Integer policy = data.get("policy");
            if (policy != null) {
                if (ProcessCleaner.CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (!condition.equals("")) {
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
            Integer policy = data.get("policy");
            if (policy != null) {
                Integer level = data.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
                if (!(level == null || -1 == level.intValue())) {
                    addTimes(this.mMemoryCleanup, "lvl" + level);
                }
                if (ProcessCleaner.CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (!condition.equals("")) {
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
            Integer policy = data.get("policy");
            if (policy != null) {
                if (ProcessCleaner.CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (!condition.equals("")) {
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
        public JSONObject makeJson(int total, Map<String, Integer> cleanup) {
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

    private final class DumpRadarHandler extends Handler {
        public DumpRadarHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                SomeArgs args = (SomeArgs) msg.obj;
                int position = ((Integer) args.arg2).intValue();
                AppCleanupDumpRadar.this.processMemoryData((List) args.arg1, position);
            }
        }
    }

    public static AppCleanupDumpRadar getInstance() {
        if (mCleanDumpRadar == null) {
            synchronized (AppCleanupDumpRadar.class) {
                if (mCleanDumpRadar == null) {
                    mCleanDumpRadar = new AppCleanupDumpRadar();
                }
            }
        }
        return mCleanDumpRadar;
    }

    private String makeCleanJson(AppMngConstant.AppCleanSource source) {
        StringBuilder cleanupStat = new StringBuilder("");
        for (Map.Entry<String, CleanupData> item : this.mCleanupDataList.entrySet()) {
            CleanupData cleanup = item.getValue();
            switch (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[source.ordinal()]) {
                case 1:
                    Map<String, Integer> pgCleanup = new LinkedHashMap<>();
                    pgCleanup.putAll(cleanup.mPGCleanup);
                    pgCleanup.putAll(cleanup.mPGNCleanup);
                    JSONObject pjJson = cleanup.makeJson(cleanup.mPGTotal, pgCleanup);
                    if (pjJson.length() == 0) {
                        break;
                    } else {
                        cleanupStat.append("\n");
                        cleanupStat.append(pjJson.toString());
                        break;
                    }
                case 2:
                    JSONObject smJson = cleanup.makeJson(cleanup.mSMTotal, cleanup.mSMCleanup);
                    if (smJson.length() == 0) {
                        break;
                    } else {
                        cleanupStat.append("\n");
                        cleanupStat.append(smJson.toString());
                        break;
                    }
                case 3:
                    JSONObject crashJson = cleanup.makeJson(cleanup.mCrashTotal, cleanup.mCrashCleanup);
                    if (crashJson.length() == 0) {
                        break;
                    } else {
                        cleanupStat.append("\n");
                        cleanupStat.append(crashJson.toString());
                        break;
                    }
                case 4:
                    JSONObject smartJson = cleanup.makeJson(cleanup.mSmartTotal, cleanup.mSmartCleanup);
                    if (smartJson.length() == 0) {
                        break;
                    } else {
                        cleanupStat.append("\n");
                        cleanupStat.append(smartJson.toString());
                        break;
                    }
                case 5:
                    JSONObject memJson = cleanup.makeJson(cleanup.mMemoryTotal, cleanup.mMemoryCleanup);
                    if (memJson.length() == 0) {
                        break;
                    } else {
                        cleanupStat.append("\n");
                        cleanupStat.append(memJson.toString());
                        break;
                    }
                case 6:
                    JSONObject thermalJson = cleanup.makeJson(cleanup.mThermalTotal, cleanup.mThermalCleanup);
                    if (thermalJson.length() == 0) {
                        break;
                    } else {
                        cleanupStat.append("\n");
                        cleanupStat.append(thermalJson.toString());
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
            StringBuilder data = new StringBuilder("");
            String pgStr = makeCleanJson(AppMngConstant.AppCleanSource.POWER_GENIE);
            if (!pgStr.equals("")) {
                data.append("\n[iAwareAppPGClean_Start]");
                data.append("\nstartTime: ");
                data.append(String.valueOf(this.mPGStartTime));
                data.append(pgStr);
                data.append("\nendTime: ");
                data.append(String.valueOf(updateTime));
                data.append("\n[iAwareAppPGClean_End]");
                this.mPGStartTime = clear ? updateTime : this.mPGStartTime;
            }
            String smStr = makeCleanJson(AppMngConstant.AppCleanSource.SYSTEM_MANAGER);
            if (!smStr.equals("")) {
                data.append("\n[iAwareAppSMClean_Start]");
                data.append("\nstartTime: ");
                data.append(String.valueOf(this.mSMStartTime));
                data.append(smStr);
                data.append("\nendTime: ");
                data.append(String.valueOf(updateTime));
                data.append("\n[iAwareAppSMClean_End]");
                this.mSMStartTime = clear ? updateTime : this.mSMStartTime;
            }
            String crashStr = makeCleanJson(AppMngConstant.AppCleanSource.CRASH);
            if (!crashStr.equals("")) {
                data.append("\n[iAwareAppCrashClean_Start]");
                data.append("\nstartTime: ");
                data.append(String.valueOf(this.mCrashStartTime));
                data.append(crashStr);
                data.append("\nendTime: ");
                data.append(String.valueOf(updateTime));
                data.append("\n[iAwareAppCrashClean_End]");
                this.mCrashStartTime = clear ? updateTime : this.mCrashStartTime;
            }
            String smartStr = makeCleanJson(AppMngConstant.AppCleanSource.SMART_CLEAN);
            if (!smartStr.equals("")) {
                data.append("\n[iAwareAppSmartClean_Start]");
                data.append("\nstartTime: ");
                data.append(String.valueOf(this.mSmartStartTime));
                data.append(smartStr);
                data.append("\nendTime: ");
                data.append(String.valueOf(updateTime));
                data.append("\n[iAwareAppSmartClean_End]");
                this.mSmartStartTime = clear ? updateTime : this.mSmartStartTime;
            }
            String memStr = makeCleanJson(AppMngConstant.AppCleanSource.MEMORY);
            if (!memStr.equals("")) {
                data.append("\n[iAwareAppMemoryClean_Start]");
                data.append("\nstartTime: ");
                data.append(String.valueOf(this.mMemStartTime));
                data.append(memStr);
                data.append("\nendTime: ");
                data.append(String.valueOf(updateTime));
                data.append("\n[iAwareAppMemoryClean_End]");
                this.mMemStartTime = clear ? updateTime : this.mMemStartTime;
            }
            String thermalStr = makeCleanJson(AppMngConstant.AppCleanSource.THERMAL);
            if (!thermalStr.equals("")) {
                data.append("\n[iAwareAppThermalClean_Start]");
                data.append("\nstartTime: ");
                data.append(String.valueOf(this.mThermalStartTime));
                data.append(thermalStr);
                data.append("\nendTime: ");
                data.append(String.valueOf(updateTime));
                data.append("\n[iAwareAppThermalClean_End]");
                this.mThermalStartTime = clear ? updateTime : this.mThermalStartTime;
            }
            if (clear) {
                this.mCleanupDataList.clear();
            }
            sb = data.toString();
        }
        return sb;
    }

    public void updateCleanData(String pkg, AppMngConstant.AppCleanSource source, Map<String, Integer> data) {
        if (isBetaUser) {
            if (DEBUG) {
                AwareLog.i(TAG, "updateCleanData pkg = " + pkg + ", source = " + source + ", data = " + data);
            }
            if (pkg != null && !pkg.isEmpty()) {
                synchronized (this.mCleanupDataList) {
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

    public void setHandler(Handler handler) {
        if (handler != null) {
            this.mHandler = new DumpRadarHandler(handler.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public void processMemoryData(List<AwareProcessBlockInfo> blockInfos, int position) {
        int size = blockInfos.size();
        int i = 0;
        while (i < size) {
            AwareProcessBlockInfo info = blockInfos.get(i);
            if (info != null) {
                if (i <= position) {
                    info.mCleanType = ProcessCleaner.CleanType.NONE;
                    info.mReason = AppMngConstant.CleanReason.MEMORY_ENOUGH.getCode();
                    HashMap<String, Integer> detailedReason = new HashMap<>();
                    detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.NONE.ordinal()));
                    detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.MEMORY_ENOUGH.ordinal()));
                    info.mDetailedReason = detailedReason;
                }
                updateCleanData(info.mPackageName, AppMngConstant.AppCleanSource.MEMORY, info.mDetailedReason);
                i++;
            } else {
                return;
            }
        }
    }

    public void reportMemoryData(List<AwareProcessBlockInfo> blockInfos, int position) {
        if (blockInfos != null && position < blockInfos.size() && this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 1;
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = blockInfos;
            args.arg2 = Integer.valueOf(position);
            msg.obj = args;
            this.mHandler.sendMessage(msg);
        }
    }
}
