package com.huawei.hwwifiproservice.wifipro;

import android.text.TextUtils;
import android.util.Log;
import huawei.hiview.HiEvent;
import huawei.hiview.HiView;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WifiProChr {
    private static final int DEFAULT_LENGTH = 4;
    private static final int DEFAULT_VALUE = 0;
    private static final int EVENT_ID_START_WIFI_TO_CELL = 909002099;
    private static final int EVENT_ID_WIFI_TO_CELL_DURATION = 909009155;
    private static final int EVENT_ID_WIFI_TO_CELL_QOE = 909009154;
    private static final String KEY_AVE_TIME = "AVETIME";
    private static final String KEY_CHLOAD = "CHLOAD";
    private static final String KEY_CNT = "CNT";
    private static final String KEY_GOOD_CNT = "GOODCNT";
    private static final String KEY_MASTER_CELL_QOE = "MCELLQOE";
    private static final String KEY_MASTER_CELL_UP_RATE = "MCELLUPRATE";
    private static final String KEY_MASTER_WIFI_QOE = "MWIFIQOE";
    private static final String KEY_MP_STATE = "MPSTATE";
    private static final String KEY_RSSI = "RSSI";
    private static final String KEY_RX_RATE = "RXRATE";
    private static final String KEY_SLAVE_CELL_QOE = "SCELLQOE";
    private static final String KEY_SLAVE_CELL_UP_RATE = "SCELLUPRATE";
    private static final String KEY_SLAVE_WIFI_QOE = "SWIFIQOE";
    private static final String KEY_STATUS_CODE = "STATUSCODE";
    private static final String KEY_SWITCH_TIME = "SWITCHTIME";
    private static final String KEY_TRIGGER_REASON = "TRIGGERREASON";
    private static final String KEY_TX_RATE = "TXRATE";
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_UL_DELAY = "ULDELAY";
    private static final String TAG = ("WiFi_PRO_" + WifiProChr.class.getSimpleName());
    private static volatile WifiProChr sWifiProChr = null;
    private ConcurrentHashMap<String, Wifi2CellDuration> mDurationStatisticsMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Wifi2CellQoe> mQoeStatisticsMap = new ConcurrentHashMap<>();

    private WifiProChr() {
    }

    public static WifiProChr getInstance() {
        if (sWifiProChr == null) {
            synchronized (WifiProChr.class) {
                if (sWifiProChr == null) {
                    sWifiProChr = new WifiProChr();
                }
            }
        }
        return sWifiProChr;
    }

    public void updateWifi2CellQoe(String type, int qoeGoodCount, int qoeFirstGoodTime, int count) {
        if (TextUtils.isEmpty(type)) {
            Log.e(TAG, "updateWifi2CellQoe: type is empty");
            return;
        }
        ConcurrentHashMap<String, Wifi2CellQoe> concurrentHashMap = this.mQoeStatisticsMap;
        if (concurrentHashMap == null) {
            Log.e(TAG, "updateWifi2CellQoe: mQoeStatisticsMap is null");
        } else if (concurrentHashMap.containsKey(type)) {
            Wifi2CellQoe wifi2CellQoe = this.mQoeStatisticsMap.get(type);
            Wifi2CellQoe.access$012(wifi2CellQoe, qoeGoodCount);
            Wifi2CellQoe.access$112(wifi2CellQoe, qoeFirstGoodTime);
            Wifi2CellQoe.access$212(wifi2CellQoe, count);
        } else {
            Wifi2CellQoe wifi2CellQoe2 = new Wifi2CellQoe();
            wifi2CellQoe2.qoeGoodCount = qoeGoodCount;
            wifi2CellQoe2.qoeFirstGoodTime = qoeFirstGoodTime;
            wifi2CellQoe2.count = count;
            this.mQoeStatisticsMap.put(type, wifi2CellQoe2);
        }
    }

    public void updateWifi2CellDuration(String type, int durationType, int count) {
        if (TextUtils.isEmpty(type)) {
            Log.e(TAG, "updateWifi2CellDuration: type is empty");
        } else if (durationType >= 4) {
            Log.e(TAG, "updateWifi2CellDuration: durationType is illegal");
        } else {
            ConcurrentHashMap<String, Wifi2CellDuration> concurrentHashMap = this.mDurationStatisticsMap;
            if (concurrentHashMap == null) {
                Log.e(TAG, "updateWifi2CellDuration: mDurationStatisticsMap is null");
            } else if (concurrentHashMap.containsKey(type)) {
                int[] iArr = this.mDurationStatisticsMap.get(type).durationCounts;
                iArr[durationType] = iArr[durationType] + count;
            } else {
                Wifi2CellDuration wifi2CellDuration = new Wifi2CellDuration();
                wifi2CellDuration.durationCounts[durationType] = count;
                this.mDurationStatisticsMap.put(type, wifi2CellDuration);
            }
        }
    }

    public void reportWifi2CellParam(Wifi2CellParam wifi2CellParam) {
        if (wifi2CellParam == null) {
            Log.e(TAG, "reportWifi2CellParam: wifi2CellParam is null");
            return;
        }
        HiEvent event = new HiEvent((int) EVENT_ID_START_WIFI_TO_CELL);
        event.putString(KEY_TYPE, wifi2CellParam.getType()).putInt(KEY_RSSI, wifi2CellParam.getRssi()).putInt(KEY_TX_RATE, wifi2CellParam.getTxRate()).putInt(KEY_RX_RATE, wifi2CellParam.getRxRate()).putInt(KEY_UL_DELAY, wifi2CellParam.getUlDelay()).putInt(KEY_CHLOAD, wifi2CellParam.getChload()).putInt(KEY_MASTER_WIFI_QOE, wifi2CellParam.getMasterWifiQoe()).putInt(KEY_SLAVE_WIFI_QOE, wifi2CellParam.getSlaveWifiQoe()).putInt(KEY_MASTER_CELL_QOE, wifi2CellParam.getMasterCellQoe()).putInt(KEY_SLAVE_CELL_QOE, wifi2CellParam.getSlaveCellQoe()).putInt(KEY_MASTER_CELL_UP_RATE, wifi2CellParam.getMasterCellUpRate()).putInt(KEY_SLAVE_CELL_UP_RATE, wifi2CellParam.getSlaveCellUpRate()).putString(KEY_TRIGGER_REASON, wifi2CellParam.getTriggerReason()).putInt(KEY_MP_STATE, wifi2CellParam.getMpState()).putString(KEY_STATUS_CODE, wifi2CellParam.getStatusCode());
        HiView.report(event);
        String str = TAG;
        Log.d(str, "reportWifi2CellParam: " + wifi2CellParam.toString());
    }

    public void reportWifi2CellQoe() {
        ConcurrentHashMap<String, Wifi2CellQoe> concurrentHashMap = this.mQoeStatisticsMap;
        if (concurrentHashMap == null) {
            Log.e(TAG, "reportWifi2CellQoe: mQoeStatisticsMap is null");
            return;
        }
        for (Map.Entry<String, Wifi2CellQoe> entry : concurrentHashMap.entrySet()) {
            String type = entry.getKey();
            Wifi2CellQoe wifi2CellQoe = entry.getValue();
            int aveTime = 0;
            if (wifi2CellQoe.count != 0) {
                aveTime = wifi2CellQoe.qoeFirstGoodTime / wifi2CellQoe.count;
            }
            HiEvent event = new HiEvent((int) EVENT_ID_WIFI_TO_CELL_QOE);
            event.putString(KEY_TYPE, type).putInt(KEY_GOOD_CNT, wifi2CellQoe.qoeGoodCount).putInt(KEY_AVE_TIME, aveTime).putInt(KEY_CNT, wifi2CellQoe.count);
            HiView.report(event);
            String str = TAG;
            Log.d(str, "reportWifi2CellQoe: type=" + type + ", " + wifi2CellQoe.toString());
        }
        this.mQoeStatisticsMap.clear();
    }

    public void reportWifi2CellDuration() {
        ConcurrentHashMap<String, Wifi2CellDuration> concurrentHashMap = this.mDurationStatisticsMap;
        if (concurrentHashMap == null) {
            Log.e(TAG, "reportWifi2CellTime: mDurationStatisticsMap is null");
            return;
        }
        for (Map.Entry<String, Wifi2CellDuration> entry : concurrentHashMap.entrySet()) {
            String type = entry.getKey();
            Wifi2CellDuration wifi2CellDuration = entry.getValue();
            for (int i = 0; i < 4; i++) {
                if (wifi2CellDuration.durationCounts[i] != 0) {
                    HiEvent event = new HiEvent((int) EVENT_ID_WIFI_TO_CELL_DURATION);
                    event.putString(KEY_TYPE, type).putInt(KEY_SWITCH_TIME, i).putInt(KEY_CNT, wifi2CellDuration.durationCounts[i]);
                    HiView.report(event);
                }
            }
            String str = TAG;
            Log.d(str, "reportWifi2CellDuration: type=" + type + ", " + wifi2CellDuration.toString());
        }
        this.mDurationStatisticsMap.clear();
    }

    public static class Wifi2CellParam {
        private int chload = 0;
        private int masterCellQoe = 0;
        private int masterCellUpRate = 0;
        private int masterWifiQoe = 0;
        private int mpState = 0;
        private int rssi = 0;
        private int rxRate = 0;
        private int slaveCellQoe = 0;
        private int slaveCellUpRate = 0;
        private int slaveWifiQoe = 0;
        private String statusCode = "";
        private String triggerReason = "";
        private int txRate = 0;
        private String type = "";
        private int ulDelay = 0;

        public String getType() {
            return this.type;
        }

        public void setType(String type2) {
            this.type = type2;
        }

        public int getRssi() {
            return this.rssi;
        }

        public void setRssi(int rssi2) {
            this.rssi = rssi2;
        }

        public int getTxRate() {
            return this.txRate;
        }

        public void setTxRate(int txRate2) {
            this.txRate = txRate2;
        }

        public int getRxRate() {
            return this.rxRate;
        }

        public void setRxRate(int rxRate2) {
            this.rxRate = rxRate2;
        }

        public int getUlDelay() {
            return this.ulDelay;
        }

        public void setUlDelay(int ulDelay2) {
            this.ulDelay = ulDelay2;
        }

        public int getChload() {
            return this.chload;
        }

        public void setChload(int chload2) {
            this.chload = chload2;
        }

        public int getMasterWifiQoe() {
            return this.masterWifiQoe;
        }

        public void setMasterWifiQoe(int masterWifiQoe2) {
            this.masterWifiQoe = masterWifiQoe2;
        }

        public int getSlaveWifiQoe() {
            return this.slaveWifiQoe;
        }

        public void setSlaveWifiQoe(int slaveWifiQoe2) {
            this.slaveWifiQoe = slaveWifiQoe2;
        }

        public int getMasterCellQoe() {
            return this.masterCellQoe;
        }

        public void setMasterCellQoe(int masterCellQoe2) {
            this.masterCellQoe = masterCellQoe2;
        }

        public int getSlaveCellQoe() {
            return this.slaveCellQoe;
        }

        public void setSlaveCellQoe(int slaveCellQoe2) {
            this.slaveCellQoe = slaveCellQoe2;
        }

        public int getMasterCellUpRate() {
            return this.masterCellUpRate;
        }

        public void setMasterCellUpRate(int masterCellUpRate2) {
            this.masterCellUpRate = masterCellUpRate2;
        }

        public int getSlaveCellUpRate() {
            return this.slaveCellUpRate;
        }

        public void setSlaveCellUpRate(int slaveCellUpRate2) {
            this.slaveCellUpRate = slaveCellUpRate2;
        }

        public String getTriggerReason() {
            return this.triggerReason;
        }

        public void setTriggerReason(String triggerReason2) {
            this.triggerReason = triggerReason2;
        }

        public int getMpState() {
            return this.mpState;
        }

        public void setMpState(int mpState2) {
            this.mpState = mpState2;
        }

        public String getStatusCode() {
            return this.statusCode;
        }

        public void setStatusCode(String statusCode2) {
            this.statusCode = statusCode2;
        }

        public String toString() {
            return "Wifi2CellParam{type=" + this.type + ", rssi=" + this.rssi + ", txRate=" + this.txRate + ", rxRate=" + this.rxRate + ", ulDelay=" + this.ulDelay + ", chload=" + this.chload + ", masterWifiQoe=" + this.masterWifiQoe + ", slaveWifiQoe=" + this.slaveWifiQoe + ", masterCellQoe=" + this.masterCellQoe + ", slaveCellQoe=" + this.slaveCellQoe + ", masterCellUpRate=" + this.masterCellUpRate + ", slaveCellUpRate=" + this.slaveCellUpRate + ", triggerReason=" + this.triggerReason + ", mpState=" + this.mpState + ", statusCode=" + this.statusCode + '}';
        }
    }

    /* access modifiers changed from: private */
    public class Wifi2CellQoe {
        private int count = 0;
        private int qoeFirstGoodTime = 0;
        private int qoeGoodCount = 0;

        static /* synthetic */ int access$012(Wifi2CellQoe x0, int x1) {
            int i = x0.qoeGoodCount + x1;
            x0.qoeGoodCount = i;
            return i;
        }

        static /* synthetic */ int access$112(Wifi2CellQoe x0, int x1) {
            int i = x0.qoeFirstGoodTime + x1;
            x0.qoeFirstGoodTime = i;
            return i;
        }

        static /* synthetic */ int access$212(Wifi2CellQoe x0, int x1) {
            int i = x0.count + x1;
            x0.count = i;
            return i;
        }

        public Wifi2CellQoe() {
        }

        public String toString() {
            return "Wifi2CellQoe{qoeGoodCount=" + this.qoeGoodCount + ", qoeFirstGoodTime=" + this.qoeFirstGoodTime + ", count=" + this.count + '}';
        }
    }

    /* access modifiers changed from: private */
    public class Wifi2CellDuration {
        private int[] durationCounts = new int[4];

        public Wifi2CellDuration() {
        }

        public String toString() {
            return "Wifi2CellDuration{durationCounts=" + Arrays.toString(this.durationCounts) + '}';
        }
    }
}
