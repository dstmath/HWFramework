package com.android.server.rms.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.app.mtm.iaware.appmng.AppMngConstant.CleanReason;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.internal.os.SomeArgs;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag;
import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import com.android.server.mtm.utils.AppStatusUtils.Status;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.memrepair.ProcStateStatisData;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;

public class AppCleanupDumpRadar {
    private static final /* synthetic */ int[] -android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues = null;
    private static boolean DEBUG = false;
    private static final int MSG_MEMORY_DATA = 1;
    private static final String TAG = "AppCleanupDumpRadar";
    private static final boolean isBetaUser;
    private static volatile AppCleanupDumpRadar mCleanDumpRadar;
    private ArrayMap<String, CleanupData> mCleanupDataList = new ArrayMap();
    private long mCleanupTime = System.currentTimeMillis();
    private Handler mHandler;

    private static class CleanupData {
        private static final /* synthetic */ int[] -android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues = null;
        private static boolean DEBUG = false;
        private static final int INVALID_VALUE = -1;
        private Map<String, Integer> mCrashCleanup;
        private int mCrashTotal;
        private Map<String, Integer> mMemoryCleanup;
        private int mMemoryTotal;
        private Map<String, Integer> mPGCleanup;
        private Map<String, Integer> mPGNCleanup;
        private int mPGTotal;
        private String mPackageName;
        private Map<String, Integer> mSMCleanup;
        private int mSMTotal;
        private Map<String, Integer> mSmartCleanup;
        private int mSmartTotal;

        private static /* synthetic */ int[] -getandroid-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues() {
            if (-android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues != null) {
                return -android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues;
            }
            int[] iArr = new int[AppCleanSource.values().length];
            try {
                iArr[AppCleanSource.COMPACT.ordinal()] = 6;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[AppCleanSource.CRASH.ordinal()] = 1;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[AppCleanSource.MEMORY.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[AppCleanSource.MEMORY_REPAIR.ordinal()] = 7;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[AppCleanSource.POWER_GENIE.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[AppCleanSource.SMART_CLEAN.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[AppCleanSource.SYSTEM_MANAGER.ordinal()] = 5;
            } catch (NoSuchFieldError e7) {
            }
            -android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues = iArr;
            return iArr;
        }

        /* synthetic */ CleanupData(String pkg, CleanupData -this1) {
            this(pkg);
        }

        private CleanupData(String pkg) {
            this.mPackageName = pkg;
            this.mPGTotal = 0;
            this.mSMTotal = 0;
            this.mCrashTotal = 0;
            this.mSmartTotal = 0;
            this.mMemoryTotal = 0;
            this.mPGCleanup = new LinkedHashMap();
            this.mPGNCleanup = new LinkedHashMap();
            this.mSMCleanup = new LinkedHashMap();
            this.mCrashCleanup = new LinkedHashMap();
            this.mSmartCleanup = new LinkedHashMap();
            this.mMemoryCleanup = new LinkedHashMap();
            this.mPGCleanup.put("lvl0", Integer.valueOf(0));
            this.mPGCleanup.put("lvl1", Integer.valueOf(0));
            this.mPGNCleanup.put("nlvl0", Integer.valueOf(0));
            this.mPGNCleanup.put("nlvl1", Integer.valueOf(0));
        }

        private void increase(AppCleanSource source, Map<String, Integer> data) {
            if (data != null && source != null) {
                if (DEBUG) {
                    for (Entry<String, Integer> entry : data.entrySet()) {
                        AwareLog.i(AppCleanupDumpRadar.TAG, "key = " + ((String) entry.getKey()) + ", value = " + entry.getValue());
                    }
                }
                switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues()[source.ordinal()]) {
                    case 1:
                        increaseCrash(data);
                        break;
                    case 2:
                        increaseMemory(data);
                        break;
                    case 3:
                        increasePG(data);
                        break;
                    case 4:
                        increaseSmart(data);
                        break;
                    case 5:
                        increaseSM(data);
                        break;
                    default:
                        return;
                }
            }
        }

        private void addTimes(Map<String, Integer> cleanup, String key) {
            if (cleanup.containsKey(key)) {
                cleanup.put(key, Integer.valueOf(((Integer) cleanup.get(key)).intValue() + 1));
            } else {
                cleanup.put(key, Integer.valueOf(1));
            }
        }

        private String getCondition(Map<String, Integer> data) {
            String condition = "";
            Integer spec = (Integer) data.get("spec");
            if (spec == null || spec.intValue() < 0 || spec.intValue() >= CleanReason.values().length) {
                Integer status = (Integer) data.get("status");
                if (status != null && status.intValue() >= 0 && status.intValue() < Status.values().length) {
                    condition = condition + Status.values()[status.intValue()].description();
                }
                Integer type = (Integer) data.get(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE);
                if (!(type == null || -1 == type.intValue())) {
                    condition = condition + ProcStateStatisData.SEPERATOR_CHAR + HwSecDiagnoseConstant.ANTIMAL_APK_TYPE + ":" + type;
                }
                Integer type_topn = (Integer) data.get("type_topn");
                if (!(type_topn == null || -1 == type_topn.intValue())) {
                    condition = condition + ProcStateStatisData.SEPERATOR_CHAR + "typen" + ":" + type_topn;
                }
                Integer recent = (Integer) data.get("recent");
                if (!(recent == null || -1 == recent.intValue())) {
                    condition = condition + ProcStateStatisData.SEPERATOR_CHAR + "rec" + ":" + recent;
                }
                Integer tristate = (Integer) data.get("tristate");
                if (!(tristate == null || -1 == tristate.intValue())) {
                    condition = condition + ProcStateStatisData.SEPERATOR_CHAR + "tri" + ":" + tristate;
                }
                return condition;
            }
            return condition + CleanReason.values()[spec.intValue()].getAbbr();
        }

        private void increasePG(Map<String, Integer> data) {
            Integer policy = (Integer) data.get("policy");
            Integer level = (Integer) data.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
            if (policy != null && level != null) {
                if (CleanType.NONE.ordinal() == policy.intValue()) {
                    increasePGN(data);
                } else if (CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    String strLevel = "lvl" + level;
                    addTimes(this.mPGCleanup, CleanType.values()[policy.intValue()].description());
                    addTimes(this.mPGCleanup, strLevel);
                    this.mPGTotal++;
                }
            }
        }

        private void increasePGN(Map<String, Integer> data) {
            String condition = getCondition(data);
            if (condition.equals("")) {
                addTimes(this.mPGNCleanup, "others");
            } else {
                addTimes(this.mPGNCleanup, condition);
            }
            addTimes(this.mPGNCleanup, "nlvl" + ((Integer) data.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL)));
            this.mPGTotal++;
        }

        private void increaseSM(Map<String, Integer> data) {
            Integer policy = (Integer) data.get("policy");
            if (policy != null) {
                if (CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mSMCleanup, CleanType.values()[policy.intValue()].description());
                    this.mSMTotal++;
                }
            }
        }

        private void increaseCrash(Map<String, Integer> data) {
            Integer policy = (Integer) data.get("policy");
            if (policy != null) {
                if (CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (condition.equals("")) {
                        addTimes(this.mCrashCleanup, "others");
                    } else {
                        addTimes(this.mCrashCleanup, condition);
                    }
                    this.mCrashTotal++;
                } else if (CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mCrashCleanup, CleanType.values()[policy.intValue()].description());
                    this.mCrashTotal++;
                }
            }
        }

        private void increaseSmart(Map<String, Integer> data) {
            Integer policy = (Integer) data.get("policy");
            if (policy != null) {
                if (CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (condition.equals("")) {
                        addTimes(this.mSmartCleanup, "others");
                    } else {
                        addTimes(this.mSmartCleanup, condition);
                    }
                    this.mSmartTotal++;
                } else if (CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mSmartCleanup, CleanType.values()[policy.intValue()].description());
                    this.mSmartTotal++;
                }
            }
        }

        private void increaseMemory(Map<String, Integer> data) {
            Integer policy = (Integer) data.get("policy");
            if (policy != null) {
                Integer level = (Integer) data.get(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
                if (!(level == null || -1 == level.intValue())) {
                    addTimes(this.mMemoryCleanup, "lvl" + level);
                }
                if (CleanType.NONE.ordinal() == policy.intValue()) {
                    String condition = getCondition(data);
                    if (condition.equals("")) {
                        addTimes(this.mMemoryCleanup, "others");
                    } else {
                        addTimes(this.mMemoryCleanup, condition);
                    }
                    this.mMemoryTotal++;
                } else if (CleanType.NONE.ordinal() >= policy.intValue() || policy.intValue() >= CleanType.values().length) {
                    AwareLog.e(AppCleanupDumpRadar.TAG, "policy = " + policy);
                } else {
                    addTimes(this.mMemoryCleanup, CleanType.values()[policy.intValue()].description());
                    this.mMemoryTotal++;
                }
            }
        }

        private JSONObject makeJson(int total, Map<String, Integer> cleanup) {
            JSONObject jsonObj = new JSONObject();
            if (total > 0) {
                try {
                    jsonObj.put(HwGpsPowerTracker.DEL_PKG, this.mPackageName);
                    jsonObj.put("total", total);
                    for (Entry<String, Integer> entry : cleanup.entrySet()) {
                        jsonObj.put((String) entry.getKey(), entry.getValue());
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
            switch (msg.what) {
                case 1:
                    SomeArgs args = msg.obj;
                    AppCleanupDumpRadar.this.processMemoryData(args.arg1, ((Integer) args.arg2).intValue());
                    return;
                default:
                    return;
            }
        }
    }

    private static /* synthetic */ int[] -getandroid-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues() {
        if (-android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues != null) {
            return -android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues;
        }
        int[] iArr = new int[AppCleanSource.values().length];
        try {
            iArr[AppCleanSource.COMPACT.ordinal()] = 6;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppCleanSource.CRASH.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppCleanSource.MEMORY.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppCleanSource.MEMORY_REPAIR.ordinal()] = 7;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppCleanSource.POWER_GENIE.ordinal()] = 3;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppCleanSource.SMART_CLEAN.ordinal()] = 4;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[AppCleanSource.SYSTEM_MANAGER.ordinal()] = 5;
        } catch (NoSuchFieldError e7) {
        }
        -android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean z;
        if (AwareConstant.CURRENT_USER_TYPE == 3) {
            z = true;
        } else {
            z = false;
        }
        isBetaUser = z;
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

    private String makeCleanJson(AppCleanSource source) {
        StringBuilder cleanupStat = new StringBuilder("");
        for (Entry<String, CleanupData> item : this.mCleanupDataList.entrySet()) {
            CleanupData cleanup = (CleanupData) item.getValue();
            switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues()[source.ordinal()]) {
                case 1:
                    JSONObject crashJson = cleanup.makeJson(cleanup.mCrashTotal, cleanup.mCrashCleanup);
                    if (crashJson.length() == 0) {
                        break;
                    }
                    cleanupStat.append("\n").append(crashJson.toString());
                    break;
                case 2:
                    JSONObject memJson = cleanup.makeJson(cleanup.mMemoryTotal, cleanup.mMemoryCleanup);
                    if (memJson.length() == 0) {
                        break;
                    }
                    cleanupStat.append("\n").append(memJson.toString());
                    break;
                case 3:
                    Map<String, Integer> pgCleanup = new LinkedHashMap();
                    pgCleanup.putAll(cleanup.mPGCleanup);
                    pgCleanup.putAll(cleanup.mPGNCleanup);
                    JSONObject pjJson = cleanup.makeJson(cleanup.mPGTotal, pgCleanup);
                    if (pjJson.length() == 0) {
                        break;
                    }
                    cleanupStat.append("\n").append(pjJson.toString());
                    break;
                case 4:
                    JSONObject smartJson = cleanup.makeJson(cleanup.mSmartTotal, cleanup.mSmartCleanup);
                    if (smartJson.length() == 0) {
                        break;
                    }
                    cleanupStat.append("\n").append(smartJson.toString());
                    break;
                case 5:
                    JSONObject smJson = cleanup.makeJson(cleanup.mSMTotal, cleanup.mSMCleanup);
                    if (smJson.length() == 0) {
                        break;
                    }
                    cleanupStat.append("\n").append(smJson.toString());
                    break;
                default:
                    break;
            }
        }
        return cleanupStat.toString();
    }

    public String saveCleanBigData(boolean clear) {
        String stringBuilder;
        synchronized (this.mCleanupDataList) {
            long startTime = this.mCleanupTime;
            this.mCleanupTime = System.currentTimeMillis();
            StringBuilder data = new StringBuilder("");
            String pgStr = makeCleanJson(AppCleanSource.POWER_GENIE);
            if (!pgStr.equals("")) {
                data = data.append("\n[iAwareAppPGClean_Start]").append("\nstartTime: ").append(String.valueOf(startTime)).append(pgStr).append("\nendTime: ").append(String.valueOf(this.mCleanupTime)).append("\n[iAwareAppPGClean_End]");
            }
            String smStr = makeCleanJson(AppCleanSource.SYSTEM_MANAGER);
            if (!smStr.equals("")) {
                data = data.append("\n[iAwareAppSMClean_Start]").append("\nstartTime: ").append(String.valueOf(startTime)).append(smStr).append("\nendTime: ").append(String.valueOf(this.mCleanupTime)).append("\n[iAwareAppSMClean_End]");
            }
            String crashStr = makeCleanJson(AppCleanSource.CRASH);
            if (!crashStr.equals("")) {
                data = data.append("\n[iAwareAppCrashClean_Start]").append("\nstartTime: ").append(String.valueOf(startTime)).append(crashStr).append("\nendTime: ").append(String.valueOf(this.mCleanupTime)).append("\n[iAwareAppCrashClean_End]");
            }
            String smartStr = makeCleanJson(AppCleanSource.SMART_CLEAN);
            if (!smartStr.equals("")) {
                data = data.append("\n[iAwareAppSmartClean_Start]").append("\nstartTime: ").append(String.valueOf(startTime)).append(smartStr).append("\nendTime: ").append(String.valueOf(this.mCleanupTime)).append("\n[iAwareAppSmartClean_End]");
            }
            String memStr = makeCleanJson(AppCleanSource.MEMORY);
            if (!memStr.equals("")) {
                data = data.append("\n[iAwareAppMemoryClean_Start]").append("\nstartTime: ").append(String.valueOf(startTime)).append(memStr).append("\nendTime: ").append(String.valueOf(this.mCleanupTime)).append("\n[iAwareAppMemoryClean_End]");
            }
            if (clear) {
                this.mCleanupDataList.clear();
            }
            stringBuilder = data.toString();
        }
        return stringBuilder;
    }

    public void updateCleanData(String pkg, AppCleanSource source, Map<String, Integer> data) {
        if (isBetaUser) {
            if (DEBUG) {
                AwareLog.i(TAG, "updateCleanData pkg = " + pkg + ", source = " + source + ", data = " + data);
            }
            if (pkg != null && !pkg.isEmpty()) {
                synchronized (this.mCleanupDataList) {
                    CleanupData cleanup = (CleanupData) this.mCleanupDataList.get(pkg);
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

    private void processMemoryData(List<AwareProcessBlockInfo> blockInfos, int position) {
        int size = blockInfos.size();
        int i = 0;
        while (i < size) {
            AwareProcessBlockInfo info = (AwareProcessBlockInfo) blockInfos.get(i);
            if (info != null) {
                if (i <= position) {
                    info.mCleanType = CleanType.NONE;
                    info.mReason = CleanReason.MEMORY_ENOUGH.getCode();
                    HashMap<String, Integer> detailedReason = new HashMap();
                    detailedReason.put(AppMngTag.POLICY.getDesc(), Integer.valueOf(CleanType.NONE.ordinal()));
                    detailedReason.put("spec", Integer.valueOf(CleanReason.MEMORY_ENOUGH.ordinal()));
                    info.mDetailedReason = detailedReason;
                }
                updateCleanData(info.mPackageName, AppCleanSource.MEMORY, info.mDetailedReason);
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
