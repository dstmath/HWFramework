package com.android.server.mtm.iaware.srms;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.android.server.rms.iaware.srms.BroadcastFeature;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AwareBroadcastDumpRadar {
    private static final int CACHE_BR_LIST_MAX = 10000;
    private static final String FEATURE_BREX_BETA_FILTER = "broadcastex_brfilter";
    private static final String FEATURE_BREX_BETA_LEAK = "broadcastex_brleak";
    private static final String FEATURE_BREX_BETA_SEND = "broadcastex_brsend";
    private static final String FEATURE_BREX_COMMERCIAL = "broadcastex";
    private static final String FEATURE_BR_COMMERCIAL = "broadcast";
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
    private static final boolean isBetaUser;
    private static long mBrAfterFilterCount = 0;
    private static long mBrBeforeFilterCount = 0;
    private static long mBrNoProcessCount = 0;
    private static long mBrPersistAppNodropCount = 0;
    private static long mBrSystemServerNodropCount = 0;
    private static final int mLeakHighLevel = 400;
    private static final int mLeakLowLevel = 20;
    private static final int mLeakMediumLevel = 100;
    private static Object mObject = new Object();
    private List<String> mCacheBrNumList = new ArrayList();
    private long mCacheSpeedAfterCtrlNoP = 0;
    private long mCacheSpeedAfterCtrlP = 0;
    private long mCacheSpeedAllBeforeCtrl = 0;
    private long mCacheSpeedBeforeCtrl = 0;
    private Map<Integer, List<Long>> mCacheSpeedMap = new HashMap();
    private long mCacheSpeedOperateAfterCtrlNoP = 0;
    private long mCacheSpeedOperateBeforeCtrl = 0;
    private long[][] mCmcDataSpeeds = ((long[][]) Array.newInstance(long.class, new int[]{6, 8}));
    private final Handler mHandler;
    private HashMap<String, Integer> mLeakBrsToReport = new HashMap<>();
    private long mScheduleTrackTime = 0;
    private long mStartTime = 0;
    private long mStartTimeBrEx = 0;
    private long mStartTimeDetailBrex = 0;
    private HashMap<String, Integer> mTrackBrFilterMap = new HashMap<>();
    private Map<String, Map<String, TrackBrData>> mTrackDataMap = null;
    private long[][] mTrackDataSpeeds = ((long[][]) Array.newInstance(long.class, new int[]{6, 8}));
    private long mTrackSpeedTime = 0;

    private final class IawareBroadcastRadarHandler extends Handler {
        public IawareBroadcastRadarHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 5) {
                AwareBroadcastDumpRadar.this.processData();
            }
        }
    }

    private static class TrackBrData {
        String brAction;
        String packageName;
        long[] rawDataList = new long[6];

        public TrackBrData(String packageName2, String brAction2) {
            this.packageName = packageName2;
            this.brAction = brAction2;
        }

        public void addTrack(int type) {
            if (type >= 0 && type < this.rawDataList.length) {
                this.rawDataList[type] = this.rawDataList[type] + 1;
            }
        }

        public String toJsonStr() {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("pkg", this.packageName);
                jsonObj.put(XmlConst.PreciseIgnore.RECEIVER_ACTION_ELEMENT_KEY, this.brAction);
            } catch (JSONException e) {
                AwareLog.e(AwareBroadcastDumpRadar.TAG, "TrackBrData.toJsonStr catch JSONException.");
            }
            for (int i = 0; i < this.rawDataList.length; i++) {
                addTypeValue(jsonObj, i);
            }
            return jsonObj.toString();
        }

        private void addTypeValue(JSONObject jsonObj, int type) {
            String key;
            if (type >= 0 && type < this.rawDataList.length) {
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
        long currentTimeMillis = System.currentTimeMillis();
        this.mStartTimeBrEx = currentTimeMillis;
        this.mStartTimeDetailBrex = currentTimeMillis;
    }

    public void trackImplicitBrDetail(boolean inControl, boolean startup, String packageName, String action) {
        if (isBetaUser && BroadcastFeature.isFeatureEnabled(11)) {
            StringBuilder builder = new StringBuilder();
            if (startup) {
                builder.append(inControl ? 4 : 3);
            } else {
                builder.append(5);
            }
            builder.append(",");
            builder.append(packageName);
            builder.append(",");
            builder.append(action);
            synchronized (this.mCacheBrNumList) {
                if (this.mCacheBrNumList.size() < 10000) {
                    this.mCacheBrNumList.add(builder.toString());
                }
            }
        }
    }

    public void trackBrFlowDetail(boolean enqueue, boolean isProxyed, boolean flowCtrlStarted, String packageName, String action) {
        if (isBetaUser && BroadcastFeature.isFeatureEnabled(10)) {
            if (enqueue) {
                synchronized (this.mCacheBrNumList) {
                    if (this.mCacheBrNumList.size() < 10000) {
                        this.mCacheBrNumList.add(2 + "," + packageName + "," + action);
                    }
                }
            } else if (!isProxyed) {
                synchronized (this.mCacheBrNumList) {
                    if (this.mCacheBrNumList.size() < 10000) {
                        this.mCacheBrNumList.add(((int) flowCtrlStarted) + "," + packageName + "," + action);
                    }
                }
            }
        }
    }

    public void trackBrFlowSpeed(boolean enqueue, boolean isProxyed, boolean isOperation, boolean isScreenOn) {
        if (BroadcastFeature.isFeatureEnabled(10)) {
            if (this.mTrackSpeedTime <= 0) {
                this.mTrackSpeedTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - this.mTrackSpeedTime >= 1000) {
                synchronized (this.mCacheSpeedMap) {
                    int i = 0;
                    while (i < 6) {
                        try {
                            if (!this.mCacheSpeedMap.containsKey(Integer.valueOf(i))) {
                                this.mCacheSpeedMap.put(Integer.valueOf(i), new ArrayList<>());
                            }
                            i++;
                        } catch (Throwable th) {
                            while (true) {
                                throw th;
                            }
                        }
                    }
                    List<Long> beforeList = this.mCacheSpeedMap.get(0);
                    List<Long> afterNPList = this.mCacheSpeedMap.get(2);
                    List<Long> afterPList = this.mCacheSpeedMap.get(1);
                    List<Long> beforeOpeList = this.mCacheSpeedMap.get(3);
                    List<Long> afterOpeNPList = this.mCacheSpeedMap.get(4);
                    List<Long> beforeAllList = this.mCacheSpeedMap.get(5);
                    if (beforeList.size() < 10000) {
                        beforeList.add(Long.valueOf(this.mCacheSpeedBeforeCtrl));
                    }
                    if (afterNPList.size() < 10000) {
                        afterNPList.add(Long.valueOf(this.mCacheSpeedAfterCtrlNoP));
                    }
                    if (afterPList.size() < 10000) {
                        afterPList.add(Long.valueOf(this.mCacheSpeedAfterCtrlP));
                    }
                    if (beforeOpeList.size() < 10000) {
                        beforeOpeList.add(Long.valueOf(this.mCacheSpeedOperateBeforeCtrl));
                    }
                    if (afterOpeNPList.size() < 10000) {
                        afterOpeNPList.add(Long.valueOf(this.mCacheSpeedOperateAfterCtrlNoP));
                    }
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
    public void processData() {
        if (this.mStartTime == 0) {
            this.mStartTime = System.currentTimeMillis();
        }
        int i = 0;
        if (isBetaUser) {
            List<String> tempList = new ArrayList<>();
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
        Map<Integer, List<Long>> temp = new HashMap<>();
        synchronized (this.mCacheSpeedMap) {
            for (Integer i2 = 0; i2.intValue() < 6; i2 = Integer.valueOf(i2.intValue() + 1)) {
                List<Long> list = new ArrayList<>();
                if (this.mCacheSpeedMap.containsKey(i2) && this.mCacheSpeedMap.get(i2) != null) {
                    list.addAll(this.mCacheSpeedMap.get(i2));
                    this.mCacheSpeedMap.get(i2).clear();
                }
                temp.put(i2, list);
            }
        }
        if (isBetaUser) {
            synchronized (this.mTrackDataSpeeds) {
                int i3 = 0;
                while (i3 < 6) {
                    try {
                        addBrSpeedDataLocked(this.mTrackDataSpeeds[i3], temp.get(Integer.valueOf(i3)));
                        i3++;
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
        }
        synchronized (this.mCmcDataSpeeds) {
            while (i < 6) {
                try {
                    addBrSpeedDataLocked(this.mCmcDataSpeeds[i], temp.get(Integer.valueOf(i)));
                    i++;
                } catch (Throwable th2) {
                    throw th2;
                }
            }
        }
    }

    private void addBrDetailData(int type, String packageName, String brAction) {
        if (packageName != null && packageName.length() != 0 && brAction != null && brAction.length() != 0 && type >= 0 && type < 6) {
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
                        sb.append("\n");
                        sb.append(speedJson.toString());
                    }
                    if (clear) {
                        Arrays.fill(this.mTrackDataSpeeds[i], 0);
                    }
                }
            }
            sb.append("\nendTime: ");
            sb.append(String.valueOf(System.currentTimeMillis()));
            sb.append("\n[iAwareBroadcastSpeed_End]");
        } else {
            sb.append(getCommercialBigData(clear));
        }
        if (clear) {
            this.mStartTime = System.currentTimeMillis();
        }
        return sb.toString();
    }

    private void getBrDetailData(StringBuilder sb, boolean clear) {
        if (this.mTrackDataMap != null && isBetaUser && sb != null) {
            synchronized (this.mTrackDataMap) {
                if (this.mTrackDataMap.size() != 0) {
                    sb.append("\n[iAwareBroadcast_Start]\nstartTime: ");
                    sb.append(String.valueOf(this.mStartTime));
                    for (Map.Entry<String, Map<String, TrackBrData>> pkgEntry : this.mTrackDataMap.entrySet()) {
                        Map<String, TrackBrData> pkgDatas = pkgEntry.getValue();
                        if (pkgDatas != null) {
                            for (Map.Entry<String, TrackBrData> dataEntry : pkgDatas.entrySet()) {
                                TrackBrData data = dataEntry.getValue();
                                if (data != null) {
                                    String dataStr = data.toJsonStr();
                                    if (dataStr != null && dataStr.length() > 0) {
                                        sb.append("\n");
                                        sb.append(dataStr);
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

    public String getDFTData(boolean forBeta, boolean clear, boolean betaEncode) {
        StringBuilder sb = new StringBuilder();
        if (!forBeta) {
            getCommercialBigDataEx(sb, clear);
            if (clear) {
                this.mStartTimeBrEx = System.currentTimeMillis();
            }
        } else if (!isBetaUser) {
            return sb.toString();
        } else {
            if (betaEncode) {
                getBRLeakBetaData(sb, clear);
                getBRSendBetaData(sb, clear);
                getBRFilterBetaData(sb, clear);
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
                getBRFilterCommercialData(jsonObj, clear);
                getBRSendCommercialData(jsonObj, clear);
                getBRLeakCommercialData(jsonObj, clear);
                sb.append(jsonObj.toString());
            } catch (JSONException e) {
                AwareLog.e(TAG, "getCommercialExBigData catch JSONException.");
            }
        }
    }

    private void getBRFilterCommercialData(JSONObject jsonObj, boolean clear) {
        synchronized (mObject) {
            try {
                jsonObj.put("brBeforeReceiver", mBrBeforeFilterCount - mBrNoProcessCount);
                jsonObj.put("brAfterReceiver", (mBrBeforeFilterCount - mBrAfterFilterCount) - mBrNoProcessCount);
            } catch (JSONException e) {
                AwareLog.e(TAG, "catch JSONException.");
            }
            if (clear) {
                resetBrFilterDataLocked();
            }
        }
    }

    private void getBRSendCommercialData(JSONObject jsonObj, boolean clear) {
        if (jsonObj != null) {
            HashMap<String, String> statData = AwareBroadcastSend.getInstance().getStatisticsData();
            if (statData.size() != 0) {
                int sendTotalCnt = 0;
                int sendSkipCnt = 0;
                for (Map.Entry<String, String> entry : statData.entrySet()) {
                    String[] sData = entry.getValue().split(",");
                    try {
                        sendSkipCnt += Integer.parseInt(sData[0]);
                        sendTotalCnt += Integer.parseInt(sData[1]);
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

    private void getBRLeakCommercialData(JSONObject jsonObj, boolean clear) {
        if (jsonObj != null) {
            int leakCntLowLevel = 0;
            int leakCntMediumLevel = 0;
            int leakCntHighLevel = 0;
            updateBRLeakReportData();
            synchronized (this.mLeakBrsToReport) {
                for (Map.Entry<String, Integer> brEntry : this.mLeakBrsToReport.entrySet()) {
                    Integer regCnt = brEntry.getValue();
                    if (regCnt.intValue() >= 20 && regCnt.intValue() < 100) {
                        leakCntLowLevel++;
                    } else if (regCnt.intValue() >= 100 && regCnt.intValue() < 400) {
                        leakCntMediumLevel++;
                    } else if (regCnt.intValue() >= 400) {
                        leakCntHighLevel++;
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

    private void getBRLeakBetaData(StringBuilder sb, boolean clear) {
        updateBRLeakReportData();
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
                    sb.append("\n");
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

    private void getBRFilterBetaData(StringBuilder sb, boolean clear) {
        synchronized (mObject) {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("feature", FEATURE_BREX_BETA_FILTER);
                jsonObj.put("start", this.mStartTimeBrEx);
                jsonObj.put("end", System.currentTimeMillis());
                jsonObj.put("brBeforeReceiver", mBrBeforeFilterCount - mBrNoProcessCount);
                jsonObj.put("brAfterReceiver", (mBrBeforeFilterCount - mBrAfterFilterCount) - mBrNoProcessCount);
                jsonObj.put("sysSvrNodrop", mBrSystemServerNodropCount);
                jsonObj.put("persistAppNodrop", mBrPersistAppNodropCount);
            } catch (JSONException e) {
                AwareLog.e(TAG, "catch JSONException.");
            }
            sb.append(jsonObj.toString());
            sb.append("\n");
            if (clear) {
                resetBrFilterDataLocked();
            }
        }
    }

    private void getBrFilterDetailData(StringBuilder sb, boolean clear) {
        sb.append("\n[iAwareBrFilter_Start]\nstartTime: ");
        sb.append(String.valueOf(this.mStartTimeDetailBrex));
        synchronized (this.mTrackBrFilterMap) {
            for (Map.Entry<String, Integer> ent : this.mTrackBrFilterMap.entrySet()) {
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put(ent.getKey(), ent.getValue());
                    sb.append("\n");
                    sb.append(jsonObj.toString());
                } catch (JSONException e) {
                    AwareLog.e(TAG, "TrackBrData.toJsonStr catch JSONException.");
                }
            }
            if (clear) {
                this.mTrackBrFilterMap.clear();
            }
        }
        sb.append("\nendTime: ");
        sb.append(String.valueOf(System.currentTimeMillis()));
        sb.append("\n[iAwareBrFilter_End]");
    }

    private void updateBRLeakReportData() {
        HashMap<String, Integer> mBRCounts = AwareBroadcastRegister.getInstance().getBRCounts();
        int threshold = AwareBroadcastRegister.getInstance().getBRRegisterReportThreshold();
        synchronized (mBRCounts) {
            if (mBRCounts.size() != 0) {
                for (Map.Entry<String, Integer> brEntry : mBRCounts.entrySet()) {
                    String brId = AwareBroadcastRegister.removeBRIdUncommonData(brEntry.getKey());
                    Integer brCount = brEntry.getValue();
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
            }
        }
    }

    private void getBRSendBetaData(StringBuilder sb, boolean clear) {
        HashMap<String, String> statData = AwareBroadcastSend.getInstance().getStatisticsData();
        if (statData.size() != 0) {
            for (Map.Entry<String, String> entry : statData.entrySet()) {
                String[] sData = entry.getValue().split(",");
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put("feature", FEATURE_BREX_BETA_SEND);
                    jsonObj.put("start", this.mStartTimeBrEx);
                    jsonObj.put("end", System.currentTimeMillis());
                    jsonObj.put(XmlConst.PreciseIgnore.RECEIVER_ACTION_ELEMENT_KEY, entry.getKey());
                    jsonObj.put("skipCnt", Integer.parseInt(sData[0]));
                    jsonObj.put("totalCnt", Integer.parseInt(sData[1]));
                    jsonObj.put("dtCnt1", Integer.parseInt(sData[2]));
                    jsonObj.put("dtCnt2", Integer.parseInt(sData[3]));
                    jsonObj.put("dtCnt3", Integer.parseInt(sData[4]));
                    jsonObj.put("dtCnt4", Integer.parseInt(sData[5]));
                    jsonObj.put("dtCnt5", Integer.parseInt(sData[6]));
                    sb.append(jsonObj.toString());
                    sb.append("\n");
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
            getBRLeakBetaData(sb, clear);
        } else {
            getCommercialBigDataEx(sb, clear);
        }
        return sb.toString();
    }

    public String dumpBrSendBigData(boolean forBeta, boolean clear) {
        StringBuilder sb = new StringBuilder();
        if (forBeta) {
            getBRSendBetaData(sb, clear);
        } else {
            getCommercialBigDataEx(sb, clear);
        }
        return sb.toString();
    }

    public void addBrFilterDetail(String key) {
        int count = 0;
        synchronized (this.mTrackBrFilterMap) {
            if (this.mTrackBrFilterMap.containsKey(key)) {
                count = this.mTrackBrFilterMap.get(key).intValue();
            }
            this.mTrackBrFilterMap.put(key, Integer.valueOf(count + 1));
        }
    }

    public HashMap<String, Integer> getBrFilterDetail() {
        HashMap<String, Integer> results;
        synchronized (this.mTrackBrFilterMap) {
            results = (HashMap) this.mTrackBrFilterMap.clone();
        }
        return results;
    }

    public static boolean isBetaUser() {
        return isBetaUser;
    }

    public static void increatBrBeforeCount(int count) {
        synchronized (mObject) {
            mBrBeforeFilterCount += (long) count;
        }
    }

    public static long getBrBeforeCount() {
        long j;
        synchronized (mObject) {
            j = mBrBeforeFilterCount;
        }
        return j;
    }

    public static void increatBrAfterCount(int count) {
        synchronized (mObject) {
            mBrAfterFilterCount += (long) count;
        }
    }

    public static long getBrAfterCount() {
        long j;
        synchronized (mObject) {
            j = mBrBeforeFilterCount - mBrAfterFilterCount;
        }
        return j;
    }

    public static void increatBrNoProcessCount(int count) {
        synchronized (mObject) {
            mBrNoProcessCount += (long) count;
        }
    }

    public static long getBrNoProcessCount() {
        long j;
        synchronized (mObject) {
            j = mBrNoProcessCount;
        }
        return j;
    }

    public static void increatSsNoDropCount(int count) {
        synchronized (mObject) {
            mBrSystemServerNodropCount += (long) count;
        }
    }

    public static long getSsNoDropCount() {
        long j;
        synchronized (mObject) {
            j = mBrSystemServerNodropCount;
        }
        return j;
    }

    public static void increatPerAppNoDropCount(int count) {
        synchronized (mObject) {
            mBrPersistAppNodropCount += (long) count;
        }
    }

    public static long getPerAppNoDropCount() {
        long j;
        synchronized (mObject) {
            j = mBrPersistAppNodropCount;
        }
        return j;
    }

    private static void resetBrFilterDataLocked() {
        mBrBeforeFilterCount = 0;
        mBrAfterFilterCount = 0;
        mBrNoProcessCount = 0;
        mBrSystemServerNodropCount = 0;
        mBrPersistAppNodropCount = 0;
    }
}
