package com.android.server.rms.iaware.appmng;

import android.rms.iaware.AwareLog;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class AwareAppMngDfxData {
    public static final int DFX_DATA_TYPE_KILL_INFO = 1;
    public static final int DFX_DATA_TYPE_START_INFO = 2;
    private static final int MASK_CLASS_RATE = 983040;
    private static final int MASK_CLEANRES = 1;
    private static final int MASK_RESTART = 2;
    private static final int MASK_SUB_CLASS = 3840;
    private static final String SEPARATOR = "#";
    private static final String TAG = "AwareAppMngDfxData";
    public long numAppColdStart;
    public long numAppTotalStart;
    public long numAwareBigMemForceStop;
    public long numAwareBigMemKill;
    public long numAwareLowMemForceStop;
    public long numAwareLowMemKill;
    public int proHwAdj;
    public String proName;

    public AwareAppMngDfxData(String packageName, String processName) {
        StringBuffer buf = new StringBuffer();
        buf.append(packageName == null ? "" : packageName);
        buf.append("#");
        if (processName != null) {
            buf.append(processName);
        }
        this.proName = buf.toString();
        this.proHwAdj = 0;
    }

    public AwareAppMngDfxData(List<String> packageName, String processName, int subClsRate, int clsRate, boolean restart, boolean cleanRes) {
        if (packageName != null) {
            StringBuffer buf = new StringBuffer();
            for (String pkg : packageName) {
                buf.append(pkg);
                buf.append("#");
            }
            if (processName != null) {
                buf.append(processName);
            }
            this.proName = buf.toString();
            this.proHwAdj = 0;
            if (cleanRes) {
                this.proHwAdj = 1;
            }
            if (restart) {
                this.proHwAdj += 2;
            }
            this.proHwAdj += subClsRate << 8;
            this.proHwAdj += clsRate << 16;
        }
    }

    public String toString() {
        boolean restart = true;
        boolean cleanRes = (this.proHwAdj & 1) != 0;
        if ((this.proHwAdj & 2) == 0) {
            restart = false;
        }
        int i = this.proHwAdj;
        return "name:" + this.proName + ",cleanRes:" + cleanRes + ",restart:" + restart + ",classRate:" + ((i & 983040) >> 16) + ",subClass:" + ((i & MASK_SUB_CLASS) >> 8);
    }

    public JSONObject makeJson(boolean killInfo) {
        JSONObject jsonObj = new JSONObject();
        if (killInfo) {
            try {
                jsonObj.put("AppName", this.proName);
                jsonObj.put("hwAdj", this.proHwAdj);
                jsonObj.put("bmk", this.numAwareBigMemKill);
                jsonObj.put("bmf", this.numAwareBigMemForceStop);
                jsonObj.put("lmk", this.numAwareLowMemKill);
                jsonObj.put("lmf", this.numAwareLowMemForceStop);
            } catch (JSONException e) {
                AwareLog.e(TAG, "makeJson error!");
            }
        } else {
            jsonObj.put("AppName", this.proName);
            jsonObj.put("cNum", this.numAppColdStart);
            jsonObj.put("hNum", this.numAppTotalStart > this.numAppColdStart ? this.numAppTotalStart - this.numAppColdStart : 0);
        }
        return jsonObj;
    }

    public String getKey() {
        StringBuilder sb = new StringBuilder();
        String str = this.proName;
        if (str == null) {
            str = "";
        }
        sb.append(str);
        sb.append(this.proHwAdj);
        return sb.toString();
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AwareAppMngDfxData)) {
            return false;
        }
        AwareAppMngDfxData data = (AwareAppMngDfxData) obj;
        String str = this.proName;
        if (str == null || !str.equals(data.proName) || this.proHwAdj != data.proHwAdj) {
            return false;
        }
        return true;
    }

    public void addTrackKillInfo(int type) {
        if (type == 0) {
            this.numAwareBigMemKill++;
        } else if (type == 1) {
            this.numAwareBigMemForceStop++;
        } else if (type == 2) {
            this.numAwareLowMemKill++;
        } else if (type == 3) {
            this.numAwareLowMemForceStop++;
        }
    }

    public void trackAppStartInfo(int type) {
        if (type == 10) {
            this.numAppColdStart++;
        } else if (type == 11) {
            this.numAppTotalStart++;
        }
    }
}
