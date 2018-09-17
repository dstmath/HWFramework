package com.android.server.rms.iaware.appmng;

import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import java.util.List;
import java.util.Map.Entry;
import org.json.JSONArray;

public class AwareAppMngDFX {
    public static final int APPLICATION_STARTTYPE_COLD = 10;
    public static final int APPLICATION_STARTTYPE_TOTAL = 11;
    private static final String APPSTATIS_COLDHOTSTARTCOUNT = ",\n\"ColdHotStartCount\":";
    private static final String APPSTATIS_END = "\n}\n[iAwareAPPStatis_End]";
    private static final String APPSTATIS_START = "[iAwareAPPStatis_Start]\n{\n\"APPKilledReason\":";
    private static final boolean DEBUG = true;
    public static final int KILL_REASON_BIGMEMFORCESTOP = 1;
    public static final int KILL_REASON_BIGMEMKILL = 0;
    public static final int KILL_REASON_LOWMEMFORCESTOP = 3;
    public static final int KILL_REASON_LOWMEMKILL = 2;
    private static final int MAX_TRACK_NUM = 1000;
    private static final String TAG = "AwareAppMngDFX";
    private static AwareAppMngDFX sInstance;
    private ArrayMap<String, AwareAppMngDfxData> mAwareAppMngDfxData;
    private ArrayMap<String, AwareAppMngDfxData> mDfxAppStartData;

    public static synchronized AwareAppMngDFX getInstance() {
        AwareAppMngDFX awareAppMngDFX;
        synchronized (AwareAppMngDFX.class) {
            if (sInstance == null) {
                sInstance = new AwareAppMngDFX();
            }
            awareAppMngDFX = sInstance;
        }
        return awareAppMngDFX;
    }

    private AwareAppMngDFX() {
        this.mAwareAppMngDfxData = new ArrayMap();
        this.mDfxAppStartData = new ArrayMap();
        init();
    }

    private void init() {
    }

    private boolean isBetaUser() {
        return AwareConstant.CURRENT_USER_TYPE == KILL_REASON_LOWMEMFORCESTOP ? DEBUG : false;
    }

    private void clearArrayList() {
        synchronized (this.mAwareAppMngDfxData) {
            if (!this.mAwareAppMngDfxData.isEmpty()) {
                this.mAwareAppMngDfxData.clear();
            }
        }
        synchronized (this.mDfxAppStartData) {
            if (this.mDfxAppStartData.isEmpty()) {
                return;
            }
            this.mDfxAppStartData.clear();
        }
    }

    public void trackeAppStartInfo(String packageName, String processName, int type) {
        if (isBetaUser() && AwareAppMngSort.checkAppMngEnable() && packageName != null) {
            synchronized (this.mDfxAppStartData) {
                if (this.mDfxAppStartData.size() >= MAX_TRACK_NUM) {
                    AwareLog.i(TAG, "tracker app start info exceeded limited size");
                    return;
                }
                AwareAppMngDfxData temp = new AwareAppMngDfxData(packageName, processName);
                AwareAppMngDfxData find = (AwareAppMngDfxData) this.mDfxAppStartData.get(temp.getKey());
                if (find == null) {
                    find = temp;
                    this.mDfxAppStartData.put(temp.getKey(), temp);
                }
                find.trackeAppStartInfo(type);
            }
        }
    }

    private void addTrackeKillInfo(AwareProcessInfo processInfo, boolean cleanRes, int type) {
        if (processInfo != null) {
            synchronized (this.mAwareAppMngDfxData) {
                if (this.mAwareAppMngDfxData.size() >= MAX_TRACK_NUM) {
                    AwareLog.i(TAG, "tracker kill info exceeded limited size");
                    return;
                }
                AwareAppMngDfxData temp = new AwareAppMngDfxData(processInfo.mProcInfo.mPackageName, processInfo.mProcInfo.mProcessName, processInfo.mSubClassRate, processInfo.mClassRate, processInfo.getRestartFlag(), cleanRes);
                AwareAppMngDfxData find = (AwareAppMngDfxData) this.mAwareAppMngDfxData.get(temp.getKey());
                if (find == null) {
                    find = temp;
                    this.mAwareAppMngDfxData.put(temp.getKey(), temp);
                }
                find.addTrackeKillInfo(type);
            }
        }
    }

    private int getType(boolean cleanRes, boolean quickKillAction) {
        if (quickKillAction) {
            if (cleanRes) {
                return KILL_REASON_BIGMEMFORCESTOP;
            }
            return KILL_REASON_BIGMEMKILL;
        } else if (cleanRes) {
            return KILL_REASON_LOWMEMFORCESTOP;
        } else {
            return KILL_REASON_LOWMEMKILL;
        }
    }

    public void trackeKillInfo(List<AwareProcessInfo> list, boolean cleanRes, boolean quickKillAction) {
        if (isBetaUser() && AwareAppMngSort.checkAppMngEnable() && list != null && !list.isEmpty()) {
            int type = getType(cleanRes, quickKillAction);
            for (AwareProcessInfo info : list) {
                if (info != null) {
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
        buf.append(APPSTATIS_START);
        buf.append(makeKillInfoJson().toString());
        buf.append(APPSTATIS_COLDHOTSTARTCOUNT);
        buf.append(makeStartAppInfoJson().toString());
        buf.append(APPSTATIS_END);
        String result = buf.toString();
        if (clear) {
            clearArrayList();
        }
        return result;
    }

    private JSONArray makeKillInfoJson() {
        Throwable th;
        synchronized (this.mAwareAppMngDfxData) {
            try {
                JSONArray json = new JSONArray();
                try {
                    for (Entry<String, AwareAppMngDfxData> m : this.mAwareAppMngDfxData.entrySet()) {
                        AwareAppMngDfxData data = (AwareAppMngDfxData) m.getValue();
                        if (data != null) {
                            json.put(data.makeJson(DEBUG));
                        }
                    }
                    return json;
                } catch (Throwable th2) {
                    th = th2;
                    JSONArray jSONArray = json;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private JSONArray makeStartAppInfoJson() {
        Throwable th;
        synchronized (this.mDfxAppStartData) {
            try {
                JSONArray json = new JSONArray();
                try {
                    for (Entry<String, AwareAppMngDfxData> m : this.mDfxAppStartData.entrySet()) {
                        AwareAppMngDfxData data = (AwareAppMngDfxData) m.getValue();
                        if (data != null) {
                            json.put(data.makeJson(false));
                        }
                    }
                    return json;
                } catch (Throwable th2) {
                    th = th2;
                    JSONArray jSONArray = json;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }
}
