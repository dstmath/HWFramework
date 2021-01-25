package com.android.server.rms.iaware.appmng;

import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.huawei.android.util.HwLogEx;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;

public class AwareAppMngDfx {
    public static final int APPLICATION_START_TYPE_COLD = 10;
    public static final int APPLICATION_START_TYPE_TOTAL = 11;
    private static final String APP_STAT_IS_COLD_HOT_START_COUNT;
    private static final String APP_STAT_IS_END = (SEPRATOR + "}" + SEPRATOR + "[iAwareAPPStatis_End]");
    private static final String APP_STAT_IS_START = ("[iAwareAPPStatis_Start]" + SEPRATOR + "{" + SEPRATOR + "\"APPKilledReason\":");
    public static final int KILL_REASON_BIG_MEM_FORCE_STOP = 1;
    public static final int KILL_REASON_BIG_MEM_KILL = 0;
    public static final int KILL_REASON_LOW_MEM_FORCE_STOP = 3;
    public static final int KILL_REASON_LOW_MEM_KILL = 2;
    private static final Object LOCK = new Object();
    private static final int MAX_TRACK_NUM = 1000;
    private static final String SEPRATOR = System.lineSeparator();
    private static final String TAG = "AwareAppMngDfx";
    private static AwareAppMngDfx sInstance;
    private final ArrayMap<String, AwareAppMngDfxData> mAwareAppMngDfxData = new ArrayMap<>();
    private final ArrayMap<String, AwareAppMngDfxData> mDfxAppStartData = new ArrayMap<>();

    static {
        StringBuilder sb = new StringBuilder();
        sb.append(",");
        sb.append(SEPRATOR);
        sb.append("\"ColdHotStartCount\":");
        APP_STAT_IS_COLD_HOT_START_COUNT = sb.toString();
    }

    public static AwareAppMngDfx getInstance() {
        AwareAppMngDfx awareAppMngDfx;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AwareAppMngDfx();
            }
            awareAppMngDfx = sInstance;
        }
        return awareAppMngDfx;
    }

    private boolean isBetaUser() {
        return AwareConstant.CURRENT_USER_TYPE == 3;
    }

    private void clearArrayList() {
        synchronized (this.mAwareAppMngDfxData) {
            if (!this.mAwareAppMngDfxData.isEmpty()) {
                this.mAwareAppMngDfxData.clear();
            }
        }
        synchronized (this.mDfxAppStartData) {
            if (!this.mDfxAppStartData.isEmpty()) {
                this.mDfxAppStartData.clear();
            }
        }
    }

    public void trackeAppStartInfo(String packageName, String processName, int type) {
        if (isBetaUser() && AwareAppMngSort.checkAppMngEnable() && packageName != null) {
            synchronized (this.mDfxAppStartData) {
                if (this.mDfxAppStartData.size() >= 1000) {
                    AwareLog.i(TAG, "tracker app start info exceeded limited size");
                    return;
                }
                AwareAppMngDfxData temp = new AwareAppMngDfxData(packageName, processName);
                AwareAppMngDfxData find = this.mDfxAppStartData.get(temp.getKey());
                if (find == null) {
                    find = temp;
                    this.mDfxAppStartData.put(find.getKey(), find);
                }
                find.trackAppStartInfo(type);
            }
        }
    }

    private void addTrackeKillInfo(AwareProcessInfo processInfo, boolean cleanRes, int type) {
        if (processInfo != null) {
            synchronized (this.mAwareAppMngDfxData) {
                if (this.mAwareAppMngDfxData.size() >= 1000) {
                    AwareLog.i(TAG, "tracker kill info exceeded limited size");
                    return;
                }
                AwareAppMngDfxData temp = new AwareAppMngDfxData(processInfo.procProcInfo.mPackageName, processInfo.procProcInfo.mProcessName, processInfo.procSubClassRate, processInfo.procClassRate, processInfo.getRestartFlag(), cleanRes);
                AwareAppMngDfxData find = this.mAwareAppMngDfxData.get(temp.getKey());
                if (find == null) {
                    find = temp;
                    this.mAwareAppMngDfxData.put(find.getKey(), find);
                }
                find.addTrackKillInfo(type);
            }
        }
    }

    private int getType(boolean cleanRes, boolean quickKillAction) {
        if (quickKillAction) {
            if (cleanRes) {
                return 1;
            }
            return 0;
        } else if (cleanRes) {
            return 3;
        } else {
            return 2;
        }
    }

    public void trackeKillInfo(List<AwareProcessInfo> list, boolean cleanRes, boolean quickKillAction) {
        if (isBetaUser() && AwareAppMngSort.checkAppMngEnable() && list != null && !list.isEmpty()) {
            int type = getType(cleanRes, quickKillAction);
            for (AwareProcessInfo info : list) {
                if (info != null) {
                    HwLogEx.dubaie("DUBAI_TAG_LOWMEM_KILL_STATE", "pkgName=" + info.procProcInfo.mPackageName + " reason=" + type);
                    addTrackeKillInfo(info, cleanRes, type);
                }
            }
        }
    }

    public String getAppMngDfxData(boolean clear) {
        if (!isBetaUser() || !AwareAppMngSort.checkAppMngEnable()) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(APP_STAT_IS_START);
        buf.append(makeKillInfoJson().toString());
        buf.append(APP_STAT_IS_COLD_HOT_START_COUNT);
        buf.append(makeStartAppInfoJson().toString());
        buf.append(APP_STAT_IS_END);
        String result = buf.toString();
        if (clear) {
            clearArrayList();
        }
        return result;
    }

    private JSONArray makeKillInfoJson() {
        JSONArray json;
        synchronized (this.mAwareAppMngDfxData) {
            json = new JSONArray();
            for (Map.Entry<String, AwareAppMngDfxData> entry : this.mAwareAppMngDfxData.entrySet()) {
                AwareAppMngDfxData data = entry.getValue();
                if (data != null) {
                    json.put(data.makeJson(true));
                }
            }
        }
        return json;
    }

    private JSONArray makeStartAppInfoJson() {
        JSONArray json;
        synchronized (this.mDfxAppStartData) {
            json = new JSONArray();
            for (Map.Entry<String, AwareAppMngDfxData> entry : this.mDfxAppStartData.entrySet()) {
                AwareAppMngDfxData data = entry.getValue();
                if (data != null) {
                    json.put(data.makeJson(false));
                }
            }
        }
        return json;
    }
}
