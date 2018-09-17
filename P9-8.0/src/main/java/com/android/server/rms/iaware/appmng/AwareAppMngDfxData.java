package com.android.server.rms.iaware.appmng;

import android.rms.iaware.AwareLog;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class AwareAppMngDfxData {
    public static final int DFX_DATA_TYPE_KILLINFO = 1;
    public static final int DFX_DATA_TYPE_STARTINFO = 2;
    private static final int MASK_CLASSRATE = 983040;
    private static final int MASK_CLEANRES = 1;
    private static final int MASK_RESTART = 2;
    private static final int MASK_SUBCLASS = 3840;
    private static final String SEPARATOR = "#";
    private static final String TAG = "AwareAppMngDfxData";
    public long mAppColdStartCount;
    public long mAppTotalStartCount;
    public long mAwareBigMemForceStop;
    public long mAwareBigMemKill;
    public long mAwareLowMemForceStop;
    public long mAwareLowMemKill;
    public int mHwAdj;
    public String mName;

    public AwareAppMngDfxData(String packageName, String processName) {
        StringBuffer buf = new StringBuffer();
        if (packageName == null) {
            packageName = "";
        }
        buf.append(packageName);
        buf.append("#");
        if (processName != null) {
            buf.append(processName);
        }
        this.mName = buf.toString();
        this.mHwAdj = 0;
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
            this.mName = buf.toString();
            this.mHwAdj = 0;
            if (cleanRes) {
                this.mHwAdj = 1;
            }
            if (restart) {
                this.mHwAdj += 2;
            }
            this.mHwAdj += subClsRate << 8;
            this.mHwAdj += clsRate << 16;
        }
    }

    public String toString() {
        return "name:" + this.mName + ",cleanRes:" + ((this.mHwAdj & 1) != 0) + ",restart:" + ((this.mHwAdj & 2) != 0) + ",classRate:" + ((this.mHwAdj & 983040) >> 16) + ",subClass:" + ((this.mHwAdj & 3840) >> 8);
    }

    public JSONObject makeJson(boolean killInfo) {
        JSONObject jsonObj = new JSONObject();
        if (killInfo) {
            try {
                jsonObj.put("AppName", this.mName);
                jsonObj.put("hwAdj", this.mHwAdj);
                jsonObj.put("bmk", this.mAwareBigMemKill);
                jsonObj.put("bmf", this.mAwareBigMemForceStop);
                jsonObj.put("lmk", this.mAwareLowMemKill);
                jsonObj.put("lmf", this.mAwareLowMemForceStop);
            } catch (JSONException e) {
                AwareLog.e(TAG, "makeJson error!");
            }
        } else {
            jsonObj.put("AppName", this.mName);
            jsonObj.put("cNum", this.mAppColdStartCount);
            jsonObj.put("hNum", this.mAppTotalStartCount > this.mAppColdStartCount ? this.mAppTotalStartCount - this.mAppColdStartCount : 0);
        }
        return jsonObj;
    }

    public String getKey() {
        return (this.mName == null ? "" : this.mName) + this.mHwAdj;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof AwareAppMngDfxData)) {
            return false;
        }
        AwareAppMngDfxData data = (AwareAppMngDfxData) obj;
        if (this.mName != null && this.mName.equals(data.mName) && this.mHwAdj == data.mHwAdj) {
            z = true;
        }
        return z;
    }

    public void addTrackeKillInfo(int type) {
        switch (type) {
            case 0:
                this.mAwareBigMemKill++;
                return;
            case 1:
                this.mAwareBigMemForceStop++;
                return;
            case 2:
                this.mAwareLowMemKill++;
                return;
            case 3:
                this.mAwareLowMemForceStop++;
                return;
            default:
                return;
        }
    }

    public void trackeAppStartInfo(int type) {
        switch (type) {
            case 10:
                this.mAppColdStartCount++;
                return;
            case 11:
                this.mAppTotalStartCount++;
                return;
            default:
                return;
        }
    }
}
