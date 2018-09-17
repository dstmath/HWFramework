package com.android.server.mtm.iaware.srms;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.iaware.srms.BroadcastFeature;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AwareBroadcastDumpRadar {
    private static final int CACHE_BR_LIST_MAX = 10000;
    private static final String COMMERCIAL_FEATURE_NAME = "broadcast";
    private static final int MSG_TRACK = 5;
    private static final int SCHEDULE_TRACK_DATA_DURATION = 2000;
    private static final int SP_INDEX_0_30 = 0;
    private static final int SP_INDEX_1H_3H = 3;
    private static final int SP_INDEX_30_60 = 1;
    private static final int SP_INDEX_3H_450 = 4;
    private static final int SP_INDEX_450_6H = 5;
    private static final int SP_INDEX_60_1H = 2;
    private static final int SP_INDEX_6H_8H = 6;
    private static final int SP_INDEX_8H_N = 7;
    private static final int SP_INDEX_TOTAL_NUM = 8;
    private static final int SP_THRESHOLD_0 = 0;
    private static final int SP_THRESHOLD_1H = 100;
    private static final int SP_THRESHOLD_30 = 30;
    private static final int SP_THRESHOLD_3H = 300;
    private static final int SP_THRESHOLD_450 = 450;
    private static final int SP_THRESHOLD_60 = 60;
    private static final int SP_THRESHOLD_6H = 600;
    private static final int SP_THRESHOLD_8H = 800;
    private static final int SP_TYPE_AFTER_CTRL_NP = 2;
    private static final int SP_TYPE_AFTER_CTRL_P = 1;
    private static final int SP_TYPE_BEFORE_CTRL = 0;
    private static final int SP_TYPE_NUM = 3;
    private static final String TAG = "AwareBroadcastDumpRadar";
    private static final int TRACK_BR_SPEED_DURATION = 1000;
    private static final int TRACK_BR_SPLIT_ACTION = 2;
    private static final int TRACK_BR_SPLIT_NUM = 3;
    private static final int TRACK_BR_SPLIT_PKG = 1;
    private static final int TRACK_BR_SPLIT_TYPE = 0;
    private static final int TYPE_HANDLE_NO_PROXY_IN_CTRL = 1;
    private static final int TYPE_HANDLE_NO_PROXY_NO_CTRL = 0;
    private static final int TYPE_HANDLE_WITH_PROXY = 2;
    private static final int TYPE_START_COMBIN_CONDITION = 4;
    private static final int TYPE_START_NO_COMBIN_CONDITION = 3;
    private static final int TYPE_STOPPED_CONDITION = 5;
    private static final int TYPE_TOTAL_NUM = 6;
    private static final boolean isBetaUser;
    private List<String> mCacheBrNumList = new ArrayList();
    private long mCacheSpeedAfterCtrlNoP = 0;
    private long mCacheSpeedAfterCtrlP = 0;
    private long mCacheSpeedBeforeCtrl = 0;
    private Map<Integer, List<Long>> mCacheSpeedMap = new HashMap();
    private long[][] mCmcDataSpeeds = ((long[][]) Array.newInstance(Long.TYPE, new int[]{3, 8}));
    private long mCmcStartTime = 0;
    private final Handler mHandler;
    private long mScheduleTrackTime = 0;
    private long mStartTime = 0;
    private Map<String, Map<String, TrackBrData>> mTrackDataMap = null;
    private long[][] mTrackDataSpeeds = ((long[][]) Array.newInstance(Long.TYPE, new int[]{3, 8}));
    private long mTrackSpeedTime = 0;

    private final class IawareBroadcastRadarHandler extends Handler {
        public IawareBroadcastRadarHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 5:
                    AwareBroadcastDumpRadar.this.processData();
                    return;
                default:
                    return;
            }
        }
    }

    private static class TrackBrData {
        String brAction;
        String packageName;
        long[] rawDataList = new long[6];

        public TrackBrData(String packageName, String brAction) {
            this.packageName = packageName;
            this.brAction = brAction;
        }

        public void addTrack(int type) {
            if (type >= 0 && type < this.rawDataList.length) {
                this.rawDataList[type] = this.rawDataList[type] + 1;
            }
        }

        public String toJsonStr() {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put(HwGpsPowerTracker.DEL_PKG, this.packageName);
                jsonObj.put(PreciseIgnore.RECEIVER_ACTION_ELEMENT_KEY, this.brAction);
            } catch (JSONException e) {
                AwareLog.e(AwareBroadcastDumpRadar.TAG, "TrackBrData.toJsonStr catch JSONException.");
            }
            for (int i = 0; i < this.rawDataList.length; i++) {
                addTypeValue(jsonObj, i);
            }
            return jsonObj.toString();
        }

        private void addTypeValue(JSONObject jsonObj, int type) {
            if (type >= 0 && type < this.rawDataList.length) {
                String key = "";
                switch (type) {
                    case 0:
                        key = "hdNoPNoC";
                        break;
                    case 1:
                        key = "hdNoPInC";
                        break;
                    case 2:
                        key = "hdP";
                        break;
                    case 3:
                        key = "startNoCon";
                        break;
                    case 4:
                        key = "startCon";
                        break;
                    case 5:
                        key = "stoppedCon";
                        break;
                    default:
                        AwareLog.e(AwareBroadcastDumpRadar.TAG, "type name not defined!");
                        key = "undefined";
                        break;
                }
                try {
                    jsonObj.put(key, this.rawDataList[type]);
                } catch (JSONException e) {
                    AwareLog.e(AwareBroadcastDumpRadar.TAG, "TrackBrData.addTypeValue catch JSONException.");
                }
            }
        }
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        isBetaUser = z;
    }

    public AwareBroadcastDumpRadar(Handler handler) {
        this.mHandler = new IawareBroadcastRadarHandler(handler.getLooper());
        this.mTrackDataMap = new HashMap();
    }

    public void trackImplicitBrDetail(boolean inControl, boolean startup, String packageName, String action) {
        if (isBetaUser && BroadcastFeature.isFeatureEnabled(11)) {
            StringBuilder builder = new StringBuilder();
            if (startup) {
                builder.append(inControl ? 4 : 3);
            } else {
                builder.append(5);
            }
            builder.append(",").append(packageName).append(",").append(action);
            synchronized (this.mCacheBrNumList) {
                if (this.mCacheBrNumList.size() < 10000) {
                    this.mCacheBrNumList.add(builder.toString());
                }
            }
        }
    }

    public void trackBrFlowDetail(boolean enqueue, boolean isProxyed, boolean flowCtrlStarted, String packageName, String action) {
        if (isBetaUser && BroadcastFeature.isFeatureEnabled(10)) {
            StringBuilder builder;
            List list;
            if (enqueue) {
                builder = new StringBuilder();
                builder.append(2).append(",").append(packageName).append(",").append(action);
                list = this.mCacheBrNumList;
                synchronized (list) {
                    if (this.mCacheBrNumList.size() < 10000) {
                        this.mCacheBrNumList.add(builder.toString());
                    }
                }
            } else if (!isProxyed) {
                int type = flowCtrlStarted ? 1 : 0;
                builder = new StringBuilder();
                builder.append(type).append(",").append(packageName).append(",").append(action);
                list = this.mCacheBrNumList;
                synchronized (list) {
                    if (this.mCacheBrNumList.size() < 10000) {
                        this.mCacheBrNumList.add(builder.toString());
                    }
                }
            } else {
                return;
            }
        }
    }

    public void trackBrFlowSpeed(boolean enqueue, boolean isProxyed) {
        if (BroadcastFeature.isFeatureEnabled(10)) {
            if (this.mTrackSpeedTime <= 0) {
                this.mTrackSpeedTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - this.mTrackSpeedTime >= 1000) {
                synchronized (this.mCacheSpeedMap) {
                    for (int i = 0; i < 3; i++) {
                        if (!this.mCacheSpeedMap.containsKey(Integer.valueOf(i))) {
                            this.mCacheSpeedMap.put(Integer.valueOf(i), new ArrayList());
                        }
                    }
                    List<Long> beforeList = (List) this.mCacheSpeedMap.get(Integer.valueOf(0));
                    List<Long> afterNPList = (List) this.mCacheSpeedMap.get(Integer.valueOf(2));
                    List<Long> afterPList = (List) this.mCacheSpeedMap.get(Integer.valueOf(1));
                    if (beforeList.size() < 10000) {
                        beforeList.add(Long.valueOf(this.mCacheSpeedBeforeCtrl));
                    }
                    if (afterNPList.size() < 10000) {
                        afterNPList.add(Long.valueOf(this.mCacheSpeedAfterCtrlNoP));
                    }
                    if (afterPList.size() < 10000) {
                        afterPList.add(Long.valueOf(this.mCacheSpeedAfterCtrlP));
                    }
                }
                this.mCacheSpeedBeforeCtrl = 0;
                this.mCacheSpeedAfterCtrlP = 0;
                this.mCacheSpeedAfterCtrlNoP = 0;
                this.mTrackSpeedTime = System.currentTimeMillis();
            }
            if (!isProxyed) {
                this.mCacheSpeedBeforeCtrl++;
            }
            if (!enqueue) {
                if (isProxyed) {
                    this.mCacheSpeedAfterCtrlP++;
                } else {
                    this.mCacheSpeedAfterCtrlNoP++;
                }
            }
        }
    }

    public void scheduleTrackBrFlowData() {
        if (BroadcastFeature.isFeatureEnabled(10)) {
            if (this.mScheduleTrackTime <= 0) {
                this.mScheduleTrackTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - this.mScheduleTrackTime >= 2000) {
                Message msg = this.mHandler.obtainMessage();
                msg.what = 5;
                this.mHandler.sendMessage(msg);
                this.mScheduleTrackTime = System.currentTimeMillis();
            }
        }
    }

    private void processData() {
        int i;
        if (this.mStartTime == 0) {
            this.mStartTime = System.currentTimeMillis();
        }
        if (this.mCmcStartTime == 0) {
            this.mCmcStartTime = System.currentTimeMillis();
        }
        if (isBetaUser) {
            List<String> tempList = new ArrayList();
            synchronized (this.mCacheBrNumList) {
                tempList.addAll(this.mCacheBrNumList);
                this.mCacheBrNumList.clear();
            }
            for (String item : tempList) {
                if (item != null && item.length() > 0) {
                    String[] splits = item.split(",");
                    if (splits.length == 3) {
                        int type = -1;
                        try {
                            type = Integer.parseInt(splits[0]);
                        } catch (NumberFormatException e) {
                            AwareLog.e(TAG, "parseInt error: " + item);
                        }
                        addBrDetailData(type, splits[1], splits[2]);
                    }
                }
            }
        }
        Map<Integer, List<Long>> temp = new HashMap();
        synchronized (this.mCacheSpeedMap) {
            Integer i2 = Integer.valueOf(0);
            while (i2.intValue() < 3) {
                List<Long> list = new ArrayList();
                if (this.mCacheSpeedMap.containsKey(i2) && this.mCacheSpeedMap.get(i2) != null) {
                    list.addAll((Collection) this.mCacheSpeedMap.get(i2));
                    ((List) this.mCacheSpeedMap.get(i2)).clear();
                }
                temp.put(i2, list);
                i2 = Integer.valueOf(i2.intValue() + 1);
            }
        }
        if (isBetaUser) {
            synchronized (this.mTrackDataSpeeds) {
                for (i = 0; i < 3; i++) {
                    addBrSpeedDataLocked(this.mTrackDataSpeeds[i], (List) temp.get(Integer.valueOf(i)));
                }
            }
        }
        synchronized (this.mCmcDataSpeeds) {
            for (i = 0; i < 3; i++) {
                addBrSpeedDataLocked(this.mCmcDataSpeeds[i], (List) temp.get(Integer.valueOf(i)));
            }
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0008, code:
            return;
     */
    /* JADX WARNING: Missing block: B:7:0x0011, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addBrDetailData(int type, String packageName, String brAction) {
        if (packageName != null && packageName.length() != 0 && brAction != null && brAction.length() != 0 && type >= 0 && type < 6) {
            synchronized (this.mTrackDataMap) {
                Map<String, TrackBrData> pkgDatas = (Map) this.mTrackDataMap.get(packageName);
                TrackBrData data;
                if (pkgDatas == null) {
                    pkgDatas = new HashMap();
                    data = new TrackBrData(packageName, brAction);
                    data.addTrack(type);
                    pkgDatas.put(brAction, data);
                    this.mTrackDataMap.put(packageName, pkgDatas);
                } else {
                    data = (TrackBrData) pkgDatas.get(brAction);
                    if (data == null) {
                        data = new TrackBrData(packageName, brAction);
                        pkgDatas.put(brAction, data);
                    }
                    data.addTrack(type);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addBrSpeedDataLocked(long[] outSpeedArray, List<Long> rawSpeedList) {
        if (outSpeedArray != null && outSpeedArray.length == 8 && rawSpeedList != null && rawSpeedList.size() != 0) {
            for (Long speed : rawSpeedList) {
                if (speed.longValue() > 0 && speed.longValue() < 30) {
                    outSpeedArray[0] = outSpeedArray[0] + 1;
                } else if (speed.longValue() >= 30 && speed.longValue() < 60) {
                    outSpeedArray[1] = outSpeedArray[1] + 1;
                } else if (speed.longValue() >= 60 && speed.longValue() < 100) {
                    outSpeedArray[2] = outSpeedArray[2] + 1;
                } else if (speed.longValue() >= 100 && speed.longValue() < 300) {
                    outSpeedArray[3] = outSpeedArray[3] + 1;
                } else if (speed.longValue() >= 300 && speed.longValue() < 450) {
                    outSpeedArray[4] = outSpeedArray[4] + 1;
                } else if (speed.longValue() >= 450 && speed.longValue() < 600) {
                    outSpeedArray[5] = outSpeedArray[5] + 1;
                } else if (speed.longValue() >= 600 && speed.longValue() < 800) {
                    outSpeedArray[6] = outSpeedArray[6] + 1;
                } else if (speed.longValue() >= 800) {
                    outSpeedArray[7] = outSpeedArray[7] + 1;
                }
            }
        }
    }

    public String getData(boolean forBeta, boolean clear) {
        StringBuilder sb = new StringBuilder();
        if (forBeta) {
            getBrDetailData(sb, clear);
            sb.append("\n[iAwareBroadcastSpeed_Start]\nstartTime: ");
            sb.append(String.valueOf(this.mStartTime));
            synchronized (this.mTrackDataSpeeds) {
                for (int i = 0; i < this.mTrackDataSpeeds.length; i++) {
                    JSONObject speedJson = getSpeedJsonLocked(this.mTrackDataSpeeds[i], i);
                    if (speedJson != null) {
                        sb.append("\n").append(speedJson.toString());
                    }
                    if (clear) {
                        Arrays.fill(this.mTrackDataSpeeds[i], 0);
                    }
                }
            }
            sb.append("\nendTime: ");
            sb.append(String.valueOf(System.currentTimeMillis()));
            sb.append("\n[iAwareBroadcastSpeed_End]");
            if (clear) {
                this.mStartTime = System.currentTimeMillis();
            }
        } else {
            sb.append(getCommercialBigData(clear));
            if (clear) {
                this.mCmcStartTime = System.currentTimeMillis();
            }
        }
        return sb.toString();
    }

    private void getBrDetailData(StringBuilder sb, boolean clear) {
        if (this.mTrackDataMap != null && (isBetaUser ^ 1) == 0 && sb != null) {
            synchronized (this.mTrackDataMap) {
                if (this.mTrackDataMap.size() != 0) {
                    sb.append("\n[iAwareBroadcast_Start]\nstartTime: ");
                    sb.append(String.valueOf(this.mStartTime));
                    for (Entry<String, Map<String, TrackBrData>> pkgEntry : this.mTrackDataMap.entrySet()) {
                        Map<String, TrackBrData> pkgDatas = (Map) pkgEntry.getValue();
                        if (pkgDatas != null) {
                            for (Entry<String, TrackBrData> dataEntry : pkgDatas.entrySet()) {
                                TrackBrData data = (TrackBrData) dataEntry.getValue();
                                if (data != null) {
                                    String dataStr = data.toJsonStr();
                                    if (dataStr != null && dataStr.length() > 0) {
                                        sb.append("\n").append(dataStr);
                                    }
                                }
                            }
                        }
                    }
                    sb.append("\nendTime: ");
                    sb.append(String.valueOf(System.currentTimeMillis()));
                    sb.append("\n[iAwareBroadcast_End]");
                    if (clear) {
                        this.mTrackDataMap.clear();
                    }
                }
            }
        }
    }

    private JSONObject getSpeedJsonLocked(long[] speeds, int speedType) {
        if (speeds == null || speeds.length != 8) {
            return null;
        }
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("spType", speedType);
            for (int i = 0; i < speeds.length; i++) {
                jsonObj.put(getSpeedKey(i), speeds[i]);
            }
        } catch (JSONException e) {
            AwareLog.e(TAG, "getSpeedJsonLocked catch JSONException.");
        }
        return jsonObj;
    }

    private String getCommercialBigData(boolean clear) {
        JSONObject jsonObj = new JSONObject();
        JSONArray dataArry = new JSONArray();
        synchronized (this.mCmcDataSpeeds) {
            for (int i = 0; i < this.mCmcDataSpeeds.length; i++) {
                JSONObject speedJson = getSpeedJsonLocked(this.mCmcDataSpeeds[i], i);
                if (speedJson != null) {
                    dataArry.put(speedJson);
                }
                if (clear) {
                    Arrays.fill(this.mCmcDataSpeeds[i], 0);
                }
            }
        }
        try {
            jsonObj.put("feature", COMMERCIAL_FEATURE_NAME);
            jsonObj.put("start", String.valueOf(this.mCmcStartTime));
            jsonObj.put("end", String.valueOf(System.currentTimeMillis()));
            jsonObj.put("data", dataArry);
        } catch (JSONException e) {
            AwareLog.e(TAG, "getCommercialBigData catch JSONException.");
        }
        return jsonObj.toString();
    }

    private String getSpeedKey(int index) {
        String key = "";
        switch (index) {
            case 0:
                return "inter0_30";
            case 1:
                return "inter30_60";
            case 2:
                return "inter60_1h";
            case 3:
                return "inter1h_3h";
            case 4:
                return "inter3h_4h";
            case 5:
                return "inter4h_6h";
            case 6:
                return "inter6h_8h";
            case 7:
                return "inter8h_n";
            default:
                AwareLog.e(TAG, "speed region name not defined!");
                return "undefined";
        }
    }
}
