package com.android.server.mtm.iaware.srms;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.bigdata.BigDataSupervisor;
import com.android.server.rms.iaware.srms.BroadcastFeature;
import com.huawei.android.os.HandlerEx;
import com.huawei.android.os.SystemPropertiesEx;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AwareBroadcastDumpRadar extends BigDataSupervisor {
    private static final int CACHE_BR_LIST_MAX = 10000;
    private static final String FEATURE_BREX_BETA_FILTER = "broadcastex_brfilter";
    private static final String FEATURE_BREX_BETA_LEAK = "broadcastex_brleak";
    private static final String FEATURE_BREX_BETA_SEND = "broadcastex_brsend";
    private static final String FEATURE_BREX_COMMERCIAL = "broadcastex";
    private static final String FEATURE_BR_COMMERCIAL = "broadcast";
    private static final boolean IS_BETA_USER;
    private static final int LEAK_HIGH_LEVEL = 400;
    private static final int LEAK_LOW_LEVEL = 20;
    private static final int LEAK_MEDIUM_LEVEL = 100;
    private static final Object LOCK = new Object();
    private static final int MSG_TRACK = 5;
    private static final int SCHEDULE_TRACK_DATA_DURATION = 2000;
    private static final String SPLIT_VALUE = ",";
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
    private static final int SP_TYPE_ALL_BEFORE_CTRL = 5;
    private static final int SP_TYPE_BEFORE_CTRL = 0;
    private static final int SP_TYPE_NUM = 6;
    private static final int SP_TYPE_OPEARTION_AFTER_CTRL_NP = 4;
    private static final int SP_TYPE_OPEARTION_BEFORE_CTRL = 3;
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
    private static long sBrAfterFilterCount = 0;
    private static long sBrBeforeFilterCount = 0;
    private static long sBrNoProcessCount = 0;
    private static long sBrPersistAppNodropCount = 0;
    private static long sBrSystemServerNodropCount = 0;
    private final List<String> mCacheBrNumList = new ArrayList();
    private long mCacheSpeedAfterCtrlNoP = 0;
    private long mCacheSpeedAfterCtrlP = 0;
    private long mCacheSpeedAllBeforeCtrl = 0;
    private long mCacheSpeedBeforeCtrl = 0;
    private Map<Integer, List<Long>> mCacheSpeedMap = new HashMap();
    private long mCacheSpeedOperateAfterCtrlNoP = 0;
    private long mCacheSpeedOperateBeforeCtrl = 0;
    private long[][] mCmcDataSpeeds = ((long[][]) Array.newInstance(long.class, 6, 8));
    private final Handler mHandler;
    private HashMap<String, Integer> mLeakBrsToReport = new HashMap<>();
    private long mScheduleTrackTime = 0;
    private long mStartTime = 0;
    private long mStartTimeBrEx = 0;
    private long mStartTimeDetailBrex = 0;
    private final HashMap<String, Integer> mTrackBrFilterMap = new HashMap<>();
    private final Map<String, Map<String, TrackBrData>> mTrackDataMap = new HashMap();
    private long[][] mTrackDataSpeeds = ((long[][]) Array.newInstance(long.class, 6, 8));
    private long mTrackSpeedTime = 0;

    static {
        boolean z = true;
        if (SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_BETA_USER = z;
    }

    /* JADX WARN: Type inference failed for: r0v7, types: [android.os.Handler, com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar$AwareBroadcastRadarHandler] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public AwareBroadcastDumpRadar(Handler handler) {
        this.mHandler = new AwareBroadcastRadarHandler(handler.getLooper());
        long currentTimeMillis = System.currentTimeMillis();
        this.mStartTimeBrEx = currentTimeMillis;
        this.mStartTimeDetailBrex = currentTimeMillis;
    }

    public void trackImplicitBrDetail(boolean inControl, boolean startup, String packageName, String action) {
        if (IS_BETA_USER && BroadcastFeature.isFeatureEnabled(11)) {
            StringBuilder builder = new StringBuilder();
            if (startup) {
                builder.append(inControl ? 4 : 3);
            } else {
                builder.append(5);
            }
            builder.append(SPLIT_VALUE);
            builder.append(packageName);
            builder.append(SPLIT_VALUE);
            builder.append(action);
            synchronized (this.mCacheBrNumList) {
                if (this.mCacheBrNumList.size() < 10000) {
                    this.mCacheBrNumList.add(builder.toString());
                }
            }
        }
    }

    public void trackBrFlowDetail(boolean enqueue, boolean isProxyed, boolean flowCtrlStarted, String packageName, String action) {
        if (!IS_BETA_USER || !BroadcastFeature.isFeatureEnabled(10)) {
            return;
        }
        if (enqueue) {
            synchronized (this.mCacheBrNumList) {
                if (this.mCacheBrNumList.size() < 10000) {
                    this.mCacheBrNumList.add(2 + SPLIT_VALUE + packageName + SPLIT_VALUE + action);
                }
            }
        } else if (!isProxyed) {
            synchronized (this.mCacheBrNumList) {
                if (this.mCacheBrNumList.size() < 10000) {
                    this.mCacheBrNumList.add((flowCtrlStarted ? 1 : 0) + SPLIT_VALUE + packageName + SPLIT_VALUE + action);
                }
            }
        }
    }

    private void addSpeedToList() {
        synchronized (this.mCacheSpeedMap) {
            for (int i = 0; i < 6; i++) {
                if (!this.mCacheSpeedMap.containsKey(Integer.valueOf(i))) {
                    this.mCacheSpeedMap.put(Integer.valueOf(i), new ArrayList<>());
                }
            }
            List<Long> beforeList = this.mCacheSpeedMap.get(0);
            if (beforeList.size() < 10000) {
                beforeList.add(Long.valueOf(this.mCacheSpeedBeforeCtrl));
            }
            List<Long> afterNpList = this.mCacheSpeedMap.get(2);
            if (afterNpList.size() < 10000) {
                afterNpList.add(Long.valueOf(this.mCacheSpeedAfterCtrlNoP));
            }
            List<Long> afterProxyList = this.mCacheSpeedMap.get(1);
            if (afterProxyList.size() < 10000) {
                afterProxyList.add(Long.valueOf(this.mCacheSpeedAfterCtrlP));
            }
            List<Long> beforeOpeList = this.mCacheSpeedMap.get(3);
            if (beforeOpeList.size() < 10000) {
                beforeOpeList.add(Long.valueOf(this.mCacheSpeedOperateBeforeCtrl));
            }
            List<Long> afterOpeNpList = this.mCacheSpeedMap.get(4);
            if (afterOpeNpList.size() < 10000) {
                afterOpeNpList.add(Long.valueOf(this.mCacheSpeedOperateAfterCtrlNoP));
            }
            List<Long> beforeAllList = this.mCacheSpeedMap.get(5);
            if (beforeAllList.size() < 10000) {
                beforeAllList.add(Long.valueOf(this.mCacheSpeedAllBeforeCtrl));
            }
        }
        this.mCacheSpeedBeforeCtrl = 0;
        this.mCacheSpeedAfterCtrlP = 0;
        this.mCacheSpeedAfterCtrlNoP = 0;
        this.mCacheSpeedOperateBeforeCtrl = 0;
        this.mCacheSpeedOperateAfterCtrlNoP = 0;
        this.mCacheSpeedAllBeforeCtrl = 0;
        this.mTrackSpeedTime = System.currentTimeMillis();
    }

    public void trackBrFlowSpeed(boolean enqueue, boolean isProxyed, boolean isOperation, boolean isScreenOn) {
        if (BroadcastFeature.isFeatureEnabled(10)) {
            if (this.mTrackSpeedTime <= 0) {
                this.mTrackSpeedTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - this.mTrackSpeedTime >= 1000) {
                addSpeedToList();
            }
            if (!isProxyed) {
                if (isScreenOn) {
                    this.mCacheSpeedBeforeCtrl++;
                }
                this.mCacheSpeedAllBeforeCtrl++;
                if (isOperation && isScreenOn) {
                    this.mCacheSpeedOperateBeforeCtrl++;
                }
            }
            if (!enqueue && isScreenOn) {
                if (isProxyed) {
                    this.mCacheSpeedAfterCtrlP++;
                }
                this.mCacheSpeedAfterCtrlNoP++;
                if (isOperation) {
                    this.mCacheSpeedOperateAfterCtrlNoP++;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processData() {
        if (this.mStartTime == 0) {
            this.mStartTime = System.currentTimeMillis();
        }
        if (IS_BETA_USER) {
            List<String> tempList = new ArrayList<>();
            synchronized (this.mCacheBrNumList) {
                tempList.addAll(this.mCacheBrNumList);
                this.mCacheBrNumList.clear();
            }
            for (String item : tempList) {
                if (item != null && item.length() > 0) {
                    String[] splits = item.split(SPLIT_VALUE);
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
        processSpeedData();
    }

    private void processSpeedData() {
        Map<Integer, List<Long>> temp = new HashMap<>();
        synchronized (this.mCacheSpeedMap) {
            for (Integer i = 0; i.intValue() < 6; i = Integer.valueOf(i.intValue() + 1)) {
                List<Long> list = new ArrayList<>();
                if (this.mCacheSpeedMap.containsKey(i) && this.mCacheSpeedMap.get(i) != null) {
                    list.addAll(this.mCacheSpeedMap.get(i));
                    this.mCacheSpeedMap.get(i).clear();
                }
                temp.put(i, list);
            }
        }
        if (IS_BETA_USER) {
            synchronized (this.mTrackDataSpeeds) {
                for (int i2 = 0; i2 < 6; i2++) {
                    addBrSpeedDataLocked(this.mTrackDataSpeeds[i2], temp.get(Integer.valueOf(i2)));
                }
            }
        }
        synchronized (this.mCmcDataSpeeds) {
            for (int i3 = 0; i3 < 6; i3++) {
                addBrSpeedDataLocked(this.mCmcDataSpeeds[i3], temp.get(Integer.valueOf(i3)));
            }
        }
    }

    private void addBrDetailData(int type, String packageName, String brAction) {
        if (!canRecord(this, TAG)) {
            AwareLog.d(TAG, "canRecord false, maybe the bigdata cache is full");
        } else if (packageName != null && packageName.length() != 0 && brAction != null && brAction.length() != 0 && type >= 0 && type < 6) {
            synchronized (this.mTrackDataMap) {
                Map<String, TrackBrData> pkgDatas = this.mTrackDataMap.get(packageName);
                if (pkgDatas == null) {
                    Map<String, TrackBrData> pkgDatas2 = new HashMap<>();
                    TrackBrData data = new TrackBrData(packageName, brAction);
                    data.addTrack(type);
                    pkgDatas2.put(brAction, data);
                    this.mTrackDataMap.put(packageName, pkgDatas2);
                } else {
                    TrackBrData data2 = pkgDatas.get(brAction);
                    if (data2 == null) {
                        data2 = new TrackBrData(packageName, brAction);
                        pkgDatas.put(brAction, data2);
                    }
                    data2.addTrack(type);
                }
            }
        }
    }

    private void countOutSpeedArray(long speed, long[] outSpeedArray) {
        if (speed > 0) {
            if (speed > 0 && speed < 30) {
                outSpeedArray[0] = outSpeedArray[0] + 1;
            } else if (speed >= 30 && speed < 60) {
                outSpeedArray[1] = outSpeedArray[1] + 1;
            } else if (speed >= 60 && speed < 100) {
                outSpeedArray[2] = outSpeedArray[2] + 1;
            } else if (speed >= 100 && speed < 300) {
                outSpeedArray[3] = outSpeedArray[3] + 1;
            } else if (speed >= 300 && speed < 450) {
                outSpeedArray[4] = outSpeedArray[4] + 1;
            } else if (speed >= 450 && speed < 600) {
                outSpeedArray[5] = outSpeedArray[5] + 1;
            } else if (speed < 600 || speed >= 800) {
                outSpeedArray[7] = outSpeedArray[7] + 1;
            } else {
                outSpeedArray[6] = outSpeedArray[6] + 1;
            }
        }
    }

    private void addBrSpeedDataLocked(long[] outSpeedArray, List<Long> rawSpeedList) {
        if (!(outSpeedArray == null || outSpeedArray.length != 8 || rawSpeedList == null || rawSpeedList.size() == 0)) {
            for (Long speed : rawSpeedList) {
                countOutSpeedArray(speed.longValue(), outSpeedArray);
            }
        }
    }

    private StringBuilder getSpeedData(boolean clear, StringBuilder sb) {
        synchronized (this.mTrackDataSpeeds) {
            for (int i = 0; i < this.mTrackDataSpeeds.length; i++) {
                JSONObject speedJson = getSpeedJsonLocked(this.mTrackDataSpeeds[i], i);
                if (speedJson != null) {
                    sb.append(System.lineSeparator());
                    sb.append(speedJson.toString());
                }
                if (clear) {
                    Arrays.fill(this.mTrackDataSpeeds[i], 0L);
                }
            }
        }
        return sb;
    }

    public String getData(boolean forBeta, boolean clear) {
        StringBuilder sb = new StringBuilder();
        if (forBeta) {
            String newLine = System.lineSeparator();
            getBrDetailData(sb, clear);
            sb.append(newLine + "[iAwareBroadcastSpeed_Start]" + newLine + "startTime: ");
            sb.append(String.valueOf(this.mStartTime));
            sb = getSpeedData(clear, sb);
            sb.append(newLine + "endTime: ");
            sb.append(String.valueOf(System.currentTimeMillis()));
            sb.append(newLine + "[iAwareBroadcastSpeed_End]");
        } else {
            sb.append(getCommercialBigData(clear));
        }
        if (clear) {
            this.mStartTime = System.currentTimeMillis();
        }
        return sb.toString();
    }

    private StringBuilder getBrDetailDataForEveryPkg(StringBuilder sb) {
        String dataStr;
        for (Map.Entry<String, Map<String, TrackBrData>> pkgEntry : this.mTrackDataMap.entrySet()) {
            Map<String, TrackBrData> pkgDatas = pkgEntry.getValue();
            if (pkgDatas != null) {
                for (Map.Entry<String, TrackBrData> dataEntry : pkgDatas.entrySet()) {
                    TrackBrData data = dataEntry.getValue();
                    if (!(data == null || (dataStr = data.toJsonStr()) == null || dataStr.length() <= 0)) {
                        sb.append(System.lineSeparator());
                        sb.append(dataStr);
                    }
                }
            }
        }
        return sb;
    }

    private void getBrDetailData(StringBuilder sb, boolean clear) {
        if (IS_BETA_USER && sb != null) {
            synchronized (this.mTrackDataMap) {
                if (this.mTrackDataMap.size() != 0) {
                    String newLine = System.lineSeparator();
                    sb.append(newLine + "[iAwareBroadcast_Start]" + newLine + "startTime: ");
                    sb.append(String.valueOf(this.mStartTime));
                    StringBuilder sb2 = getBrDetailDataForEveryPkg(sb);
                    sb2.append(newLine + "endTime: ");
                    sb2.append(String.valueOf(System.currentTimeMillis()));
                    sb2.append(newLine + "[iAwareBroadcast_End]");
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
                    Arrays.fill(this.mCmcDataSpeeds[i], 0L);
                }
            }
        }
        try {
            jsonObj.put("feature", FEATURE_BR_COMMERCIAL);
            jsonObj.put("start", String.valueOf(this.mStartTime));
            jsonObj.put("end", String.valueOf(System.currentTimeMillis()));
            jsonObj.put("data", dataArry);
        } catch (JSONException e) {
            AwareLog.e(TAG, "getCommercialBigData catch JSONException.");
        }
        return jsonObj.toString();
    }

    private String getSpeedKey(int index) {
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

    private final class AwareBroadcastRadarHandler extends HandlerEx {
        public AwareBroadcastRadarHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 5) {
                AwareBroadcastDumpRadar.this.processData();
            }
        }
    }

    /* access modifiers changed from: private */
    public static class TrackBrData {
        String brAction;
        String packageName;
        long[] rawDataList = new long[6];

        public TrackBrData(String packageName2, String brAction2) {
            this.packageName = packageName2;
            this.brAction = brAction2;
        }

        public void addTrack(int type) {
            if (type >= 0) {
                long[] jArr = this.rawDataList;
                if (type < jArr.length) {
                    jArr[type] = jArr[type] + 1;
                }
            }
        }

        public String toJsonStr() {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("pkg", this.packageName);
                jsonObj.put("action", this.brAction);
            } catch (JSONException e) {
                AwareLog.e(AwareBroadcastDumpRadar.TAG, "TrackBrData.toJsonStr catch JSONException.");
            }
            for (int i = 0; i < this.rawDataList.length; i++) {
                addTypeValue(jsonObj, i);
            }
            return jsonObj.toString();
        }

        private void addTypeValue(JSONObject jsonObj, int type) {
            String jsonName;
            if (type >= 0 && type < this.rawDataList.length) {
                if (type == 0) {
                    jsonName = "hdNoPNoC";
                } else if (type == 1) {
                    jsonName = "hdNoPInC";
                } else if (type == 2) {
                    jsonName = "hdP";
                } else if (type == 3) {
                    jsonName = "startNoCon";
                } else if (type == 4) {
                    jsonName = "startCon";
                } else if (type != 5) {
                    AwareLog.e(AwareBroadcastDumpRadar.TAG, "type name not defined!");
                    jsonName = "undefined";
                } else {
                    jsonName = "stoppedCon";
                }
                try {
                    jsonObj.put(jsonName, this.rawDataList[type]);
                } catch (JSONException e) {
                    AwareLog.e(AwareBroadcastDumpRadar.TAG, "TrackBrData.addTypeValue catch JSONException.");
                }
            }
        }
    }

    public String getDftData(boolean forBeta, boolean clear, boolean betaEncode) {
        StringBuilder sb = new StringBuilder();
        if (!forBeta) {
            getCommercialBigDataEx(sb, clear);
            if (clear) {
                this.mStartTimeBrEx = System.currentTimeMillis();
            }
        } else if (!IS_BETA_USER) {
            return sb.toString();
        } else {
            if (betaEncode) {
                getBrLeakBetaData(sb, clear);
                getBrSendBetaData(sb, clear);
                getBrFilterBetaData(sb, clear);
                if (clear) {
                    this.mStartTimeBrEx = System.currentTimeMillis();
                }
            } else {
                getBrFilterDetailData(sb, clear);
                if (clear) {
                    this.mStartTimeDetailBrex = System.currentTimeMillis();
                }
            }
        }
        return sb.toString();
    }

    private void getCommercialBigDataEx(StringBuilder sb, boolean clear) {
        if (sb != null) {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("feature", FEATURE_BREX_COMMERCIAL);
                jsonObj.put("start", this.mStartTimeBrEx);
                jsonObj.put("end", System.currentTimeMillis());
                getBrFilterCommercialData(jsonObj, clear);
                getBrSendCommercialData(jsonObj, clear);
                getBrLeakCommercialData(jsonObj, clear);
                sb.append(jsonObj.toString());
            } catch (JSONException e) {
                AwareLog.e(TAG, "getCommercialExBigData catch JSONException.");
            }
        }
    }

    private void getBrFilterCommercialData(JSONObject jsonObj, boolean clear) {
        synchronized (LOCK) {
            try {
                jsonObj.put("brBeforeReceiver", sBrBeforeFilterCount - sBrNoProcessCount);
                jsonObj.put("brAfterReceiver", (sBrBeforeFilterCount - sBrAfterFilterCount) - sBrNoProcessCount);
            } catch (JSONException e) {
                AwareLog.e(TAG, "catch JSONException.");
            }
            if (clear) {
                resetBrFilterDataLocked();
            }
        }
    }

    private void getBrSendCommercialData(JSONObject jsonObj, boolean clear) {
        if (jsonObj != null) {
            HashMap<String, String> statData = AwareBroadcastSend.getInstance().getStatisticsData();
            if (statData.size() != 0) {
                int sendTotalCnt = 0;
                int sendSkipCnt = 0;
                for (Map.Entry<String, String> entry : statData.entrySet()) {
                    String[] data = entry.getValue().split(SPLIT_VALUE);
                    try {
                        sendSkipCnt += Integer.parseInt(data[0]);
                        sendTotalCnt += Integer.parseInt(data[1]);
                    } catch (IndexOutOfBoundsException e) {
                        AwareLog.e(TAG, "catch IndexOutOfBoundsException.");
                    } catch (NumberFormatException e2) {
                        AwareLog.e(TAG, "catch NumberFormatException.");
                    }
                }
                try {
                    jsonObj.put("brsendtotal", sendTotalCnt);
                    jsonObj.put("brsendskip", sendSkipCnt);
                } catch (JSONException e3) {
                    AwareLog.e(TAG, "catch JSONException.");
                }
                if (clear) {
                    AwareBroadcastSend.getInstance().resetStatisticsData();
                }
            }
        }
    }

    private void getBrLeakCommercialData(JSONObject jsonObj, boolean clear) {
        if (jsonObj != null) {
            int leakCntLowLevel = 0;
            int leakCntMediumLevel = 0;
            int leakCntHighLevel = 0;
            updateBrLeakReportData();
            synchronized (this.mLeakBrsToReport) {
                for (Map.Entry<String, Integer> brEntry : this.mLeakBrsToReport.entrySet()) {
                    Integer regCnt = brEntry.getValue();
                    if (regCnt.intValue() >= 20) {
                        if (regCnt.intValue() >= 20 && regCnt.intValue() < 100) {
                            leakCntLowLevel++;
                        } else if (regCnt.intValue() < 100 || regCnt.intValue() >= 400) {
                            leakCntHighLevel++;
                        } else {
                            leakCntMediumLevel++;
                        }
                    }
                }
            }
            try {
                jsonObj.put("brleak20_100", leakCntLowLevel);
                jsonObj.put("brleak100_400", leakCntMediumLevel);
                jsonObj.put("brleak400_n", leakCntHighLevel);
            } catch (JSONException e) {
                AwareLog.e(TAG, "catch JSONException.");
            }
            if (clear) {
                synchronized (this.mLeakBrsToReport) {
                    this.mLeakBrsToReport.clear();
                }
            }
        }
    }

    private void getBrLeakBetaData(StringBuilder sb, boolean clear) {
        updateBrLeakReportData();
        synchronized (this.mLeakBrsToReport) {
            for (Map.Entry<String, Integer> brEntry : this.mLeakBrsToReport.entrySet()) {
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put("feature", FEATURE_BREX_BETA_LEAK);
                    jsonObj.put("start", this.mStartTimeBrEx);
                    jsonObj.put("end", System.currentTimeMillis());
                    jsonObj.put("brId", brEntry.getKey());
                    jsonObj.put("regCnt", brEntry.getValue());
                    jsonObj.put("dropCnt", 0);
                    sb.append(jsonObj.toString());
                    sb.append(System.lineSeparator());
                } catch (JSONException e) {
                    AwareLog.e(TAG, "catch JSONException.");
                }
            }
        }
        if (clear) {
            synchronized (this.mLeakBrsToReport) {
                this.mLeakBrsToReport.clear();
            }
        }
    }

    private void getBrFilterBetaData(StringBuilder sb, boolean clear) {
        synchronized (LOCK) {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("feature", FEATURE_BREX_BETA_FILTER);
                jsonObj.put("start", this.mStartTimeBrEx);
                jsonObj.put("end", System.currentTimeMillis());
                jsonObj.put("brBeforeReceiver", sBrBeforeFilterCount - sBrNoProcessCount);
                jsonObj.put("brAfterReceiver", (sBrBeforeFilterCount - sBrAfterFilterCount) - sBrNoProcessCount);
                jsonObj.put("sysSvrNodrop", sBrSystemServerNodropCount);
                jsonObj.put("persistAppNodrop", sBrPersistAppNodropCount);
            } catch (JSONException e) {
                AwareLog.e(TAG, "catch JSONException.");
            }
            sb.append(jsonObj.toString());
            sb.append(System.lineSeparator());
            if (clear) {
                resetBrFilterDataLocked();
            }
        }
    }

    private void getBrFilterDetailData(StringBuilder sb, boolean clear) {
        String newLine = System.lineSeparator();
        sb.append(newLine + "[iAwareBrFilter_Start]" + newLine + "startTime: ");
        sb.append(String.valueOf(this.mStartTimeDetailBrex));
        synchronized (this.mTrackBrFilterMap) {
            for (Map.Entry<String, Integer> ent : this.mTrackBrFilterMap.entrySet()) {
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put(ent.getKey(), ent.getValue());
                    sb.append(newLine);
                    sb.append(jsonObj.toString());
                } catch (JSONException e) {
                    AwareLog.e(TAG, "TrackBrData.toJsonStr catch JSONException.");
                }
            }
            if (clear) {
                this.mTrackBrFilterMap.clear();
            }
        }
        sb.append(newLine + "endTime: ");
        sb.append(String.valueOf(System.currentTimeMillis()));
        sb.append(newLine + "[iAwareBrFilter_End]");
    }

    private void updateBrLeakReportDataProcessOne(String brId, Integer brCount, int threshold) {
        if (brCount.intValue() > threshold && brId.length() > 0) {
            synchronized (this.mLeakBrsToReport) {
                if (!this.mLeakBrsToReport.containsKey(brId)) {
                    this.mLeakBrsToReport.put(brId, brCount);
                } else if (brCount.intValue() > this.mLeakBrsToReport.get(brId).intValue()) {
                    this.mLeakBrsToReport.put(brId, brCount);
                }
            }
        }
    }

    private void updateBrLeakReportData() {
        HashMap<String, Integer> mBrCounts = AwareBroadcastRegister.getInstance().getBrCounts();
        int threshold = AwareBroadcastRegister.getInstance().getBrRegisterReportThreshold();
        synchronized (mBrCounts) {
            if (mBrCounts.size() != 0) {
                for (Map.Entry<String, Integer> brEntry : mBrCounts.entrySet()) {
                    updateBrLeakReportDataProcessOne(AwareBroadcastRegister.removeBrIdUncommonData(brEntry.getKey()), brEntry.getValue(), threshold);
                }
            }
        }
    }

    private void getBrSendBetaData(StringBuilder sb, boolean clear) {
        HashMap<String, String> statData = AwareBroadcastSend.getInstance().getStatisticsData();
        if (statData.size() != 0) {
            for (Map.Entry<String, String> entry : statData.entrySet()) {
                String[] data = entry.getValue().split(SPLIT_VALUE);
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put("feature", FEATURE_BREX_BETA_SEND);
                    jsonObj.put("start", this.mStartTimeBrEx);
                    jsonObj.put("end", System.currentTimeMillis());
                    jsonObj.put("action", entry.getKey());
                    jsonObj.put("skipCnt", Integer.parseInt(data[0]));
                    jsonObj.put("totalCnt", Integer.parseInt(data[1]));
                    jsonObj.put("dtCnt1", Integer.parseInt(data[2]));
                    jsonObj.put("dtCnt2", Integer.parseInt(data[3]));
                    jsonObj.put("dtCnt3", Integer.parseInt(data[4]));
                    jsonObj.put("dtCnt4", Integer.parseInt(data[5]));
                    jsonObj.put("dtCnt5", Integer.parseInt(data[6]));
                    sb.append(jsonObj.toString());
                    sb.append(System.lineSeparator());
                } catch (JSONException e) {
                    AwareLog.e(TAG, "catch JSONException.");
                } catch (IndexOutOfBoundsException e2) {
                    AwareLog.e(TAG, "catch IndexOutOfBoundsException.");
                } catch (NumberFormatException e3) {
                    AwareLog.e(TAG, "catch NumberFormatException.");
                }
            }
            if (clear) {
                AwareBroadcastSend.getInstance().resetStatisticsData();
            }
        }
    }

    public String dumpBrRegBigData(boolean forBeta, boolean clear) {
        StringBuilder sb = new StringBuilder();
        if (forBeta) {
            getBrLeakBetaData(sb, clear);
        } else {
            getCommercialBigDataEx(sb, clear);
        }
        return sb.toString();
    }

    public String dumpBrSendBigData(boolean forBeta, boolean clear) {
        StringBuilder sb = new StringBuilder();
        if (forBeta) {
            getBrSendBetaData(sb, clear);
        } else {
            getCommercialBigDataEx(sb, clear);
        }
        return sb.toString();
    }

    public void addBrFilterDetail(String key) {
        int count = 0;
        synchronized (this.mTrackBrFilterMap) {
            if (!canRecord(this, TAG)) {
                AwareLog.d(TAG, "canRecord false, maybe the bigdata cache is full");
                return;
            }
            if (this.mTrackBrFilterMap.containsKey(key)) {
                count = this.mTrackBrFilterMap.get(key).intValue();
            }
            this.mTrackBrFilterMap.put(key, Integer.valueOf(count + 1));
        }
    }

    public HashMap<String, Integer> getBrFilterDetail() {
        synchronized (this.mTrackBrFilterMap) {
            if (!(this.mTrackBrFilterMap.clone() instanceof HashMap)) {
                return null;
            }
            return (HashMap) this.mTrackBrFilterMap.clone();
        }
    }

    public static boolean isBetaUser() {
        return IS_BETA_USER;
    }

    public static void increaseBrBeforeCount(int count) {
        synchronized (LOCK) {
            sBrBeforeFilterCount += (long) count;
        }
    }

    public static long getBrBeforeCount() {
        long j;
        synchronized (LOCK) {
            j = sBrBeforeFilterCount;
        }
        return j;
    }

    public static void increaseBrAfterCount(int count) {
        synchronized (LOCK) {
            sBrAfterFilterCount += (long) count;
        }
    }

    public static long getBrAfterCount() {
        long j;
        synchronized (LOCK) {
            j = sBrBeforeFilterCount - sBrAfterFilterCount;
        }
        return j;
    }

    public static void increaseBrNoProcessCount(int count) {
        synchronized (LOCK) {
            sBrNoProcessCount += (long) count;
        }
    }

    public static long getBrNoProcessCount() {
        long j;
        synchronized (LOCK) {
            j = sBrNoProcessCount;
        }
        return j;
    }

    public static void increaseSsNoDropCount(int count) {
        synchronized (LOCK) {
            sBrSystemServerNodropCount += (long) count;
        }
    }

    public static long getSsNoDropCount() {
        long j;
        synchronized (LOCK) {
            j = sBrSystemServerNodropCount;
        }
        return j;
    }

    public static void increasePerAppNoDropCount(int count) {
        synchronized (LOCK) {
            sBrPersistAppNodropCount += (long) count;
        }
    }

    public static long getPerAppNoDropCount() {
        long j;
        synchronized (LOCK) {
            j = sBrPersistAppNodropCount;
        }
        return j;
    }

    private static void resetBrFilterDataLocked() {
        sBrBeforeFilterCount = 0;
        sBrAfterFilterCount = 0;
        sBrNoProcessCount = 0;
        sBrSystemServerNodropCount = 0;
        sBrPersistAppNodropCount = 0;
    }

    @Override // com.android.server.rms.iaware.bigdata.BigDataSupervisor
    public int monitorBigDataRecord() {
        int size;
        int size2;
        synchronized (this.mTrackBrFilterMap) {
            size = 0 + this.mTrackBrFilterMap.size();
        }
        synchronized (this.mTrackDataMap) {
            size2 = size + this.mTrackDataMap.size();
        }
        return size2;
    }
}
