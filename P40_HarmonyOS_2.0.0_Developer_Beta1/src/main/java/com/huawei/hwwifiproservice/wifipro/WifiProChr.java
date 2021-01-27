package com.huawei.hwwifiproservice.wifipro;

import android.util.Log;
import huawei.hiview.HiEvent;
import huawei.hiview.HiView;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WifiProChr {
    private static final int DEFAULT_LENGTH = 4;
    private static final int DEFAULT_VALUE = 0;
    private static final int EVENT_ID_WIFI_TO_CELL_DURATION = 909009155;
    private static final int EVENT_ID_WIFI_TO_CELL_FAIL = 909002100;
    private static final int EVENT_ID_WIFI_TO_CELL_QOE = 909009154;
    private static final int EVENT_ID_WIFI_TO_CELL_SUCC = 909002099;
    private static final String KEY_AVE_EFFTIVE_TIME = "AVEEFFTIVETIME";
    private static final String KEY_AVE_SWITCH_TIME = "AVESWITCHTIME";
    private static final String KEY_AVE_TIME = "AVETIME";
    private static final String KEY_AVE_TRAFFIC = "AVETRAFFIC";
    private static final String KEY_CHLOAD = "CHLOAD";
    private static final String KEY_CNT = "CNT";
    private static final String KEY_EFFTIVE_TIME = "EFFTIVETIME";
    private static final String KEY_ENTERPRISE_AP = "ENTERPRISEAP";
    private static final String KEY_FAILURE_REASON = "FAILUREREASON";
    private static final String KEY_GAIN_STATE = "GAINSTATE";
    private static final String KEY_GOOD_CNT = "GOODCNT";
    private static final String KEY_MASTER_CELL_QOE = "MCELLQOE";
    private static final String KEY_MASTER_CELL_UP_RATE = "MCELLUPRATE";
    private static final String KEY_MASTER_WIFI_QOE = "MWIFIQOE";
    private static final String KEY_MP_STATE = "MPSTATE";
    private static final String KEY_PKG_NAME = "PKGNAME";
    private static final String KEY_RSSI = "RSSI";
    private static final String KEY_RX_RATE = "RXRATE";
    private static final String KEY_SLAVE_CELL_QOE = "SCELLQOE";
    private static final String KEY_SLAVE_CELL_UP_RATE = "SCELLUPRATE";
    private static final String KEY_SLAVE_WIFI_QOE = "SWIFIQOE";
    private static final String KEY_STATUS_CODE = "STATUSCODE";
    private static final String KEY_SWITCH_TIME = "SWITCHTIME";
    private static final String KEY_TRAFFIC = "TRAFFIC";
    private static final String KEY_TRIGGER_REASON = "TRIGGERREASON";
    private static final String KEY_TX_RATE = "TXRATE";
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_UL_DELAY = "ULDELAY";
    private static final String TAG = ("WiFi_PRO_" + WifiProChr.class.getSimpleName());
    private static final int WIFI_2_CELL_EXIT_TYPE_HANDOVER_FAIL = 1;
    private static final int WIFI_2_CELL_EXIT_TYPE_INVAILD = 0;
    private static final int WIFI_2_CELL_EXIT_TYPE_SUCC_EXIT = 2;
    public static final int WIFI_2_CELL_FAIL_REASON_2_MIN_HANDOVER_PROTECTION = 28;
    public static final int WIFI_2_CELL_FAIL_REASON_AIRPLANE_MODE_ON = 22;
    public static final int WIFI_2_CELL_FAIL_REASON_CELL_2G_3G_SIGNAL_BAD = 16;
    public static final int WIFI_2_CELL_FAIL_REASON_CELL_DISCONNECTED = 17;
    public static final int WIFI_2_CELL_FAIL_REASON_CELL_OVER_SEA = 13;
    public static final int WIFI_2_CELL_FAIL_REASON_CELL_QOE_BAD = 30;
    public static final int WIFI_2_CELL_FAIL_REASON_CS_CALLING = 11;
    public static final int WIFI_2_CELL_FAIL_REASON_DATA_ENABLE_OFF = 26;
    public static final int WIFI_2_CELL_FAIL_REASON_FULL_SCREEN = 20;
    public static final int WIFI_2_CELL_FAIL_REASON_HADDOVER_SUCC_ALREADY = 8;
    public static final int WIFI_2_CELL_FAIL_REASON_HANDOVER_UI_SHOWING = 29;
    public static final int WIFI_2_CELL_FAIL_REASON_HILINK_UNCONFIG = 5;
    public static final int WIFI_2_CELL_FAIL_REASON_INVAILD = 0;
    public static final int WIFI_2_CELL_FAIL_REASON_IS_LANDSCAPE_MODE = 19;
    public static final int WIFI_2_CELL_FAIL_REASON_MASTER_OR_SLAVE_ACTIVE_FAIL = 15;
    public static final int WIFI_2_CELL_FAIL_REASON_MAX_OTHERS = 199;
    public static final int WIFI_2_CELL_FAIL_REASON_NOT_ALLOW_WIFI_2_CELL = 9;
    public static final int WIFI_2_CELL_FAIL_REASON_PDP_CANNOT_HANDOVER_MOBILE = 31;
    public static final int WIFI_2_CELL_FAIL_REASON_SCREEN_OFF = 14;
    public static final int WIFI_2_CELL_FAIL_REASON_SIM_STATE_NOT_READLY = 25;
    public static final int WIFI_2_CELL_FAIL_REASON_STOP_BY_HIDATA = 18;
    public static final int WIFI_2_CELL_FAIL_REASON_SWITCHING = 23;
    public static final int WIFI_2_CELL_FAIL_REASON_USER_HANDOVER_WIFI = 4;
    public static final int WIFI_2_CELL_FAIL_REASON_VPN_IS_USING = 3;
    public static final int WIFI_2_CELL_FAIL_REASON_WAIT_CELL_QOE_GOOD = 27;
    public static final int WIFI_2_CELL_FAIL_REASON_WHITE_APPLIST = 6;
    public static final int WIFI_2_CELL_FAIL_REASON_WIFIPRO_DISABLE = 10;
    public static final int WIFI_2_CELL_FAIL_REASON_WIFI_2_WIFI_SUCC = 12;
    public static final int WIFI_2_CELL_FAIL_REASON_WIFI_AUTO_SWITCH_OFF = 21;
    public static final int WIFI_2_CELL_FAIL_REASON_WIFI_DISCONNECTED = 1;
    public static final int WIFI_2_CELL_FAIL_REASON_WIFI_PRIORITY = 24;
    public static final int WIFI_2_CELL_FAIL_REASON_WIFI_QOE_BETTER_CELL_QOE_POOR = 2;
    public static final int WIFI_2_CELL_FAIL_REASON_WIFI_REPEATER_ON = 7;
    private static final int WIFI_2_CELL_STATUE_FAIL = 3;
    private static final int WIFI_2_CELL_STATUE_INVAILD = -1;
    private static final int WIFI_2_CELL_STATUE_START = 0;
    private static final int WIFI_2_CELL_STATUE_SUCC = 1;
    private static final int WIFI_2_CELL_STATUE_SUCC_EXIT = 2;
    public static final int WIFI_2_CELL_SUCC_CNT_NUM = 1;
    public static final int WIFI_2_CELL_SUCC_EXIT_REASION_BASE = 200;
    public static final int WIFI_2_CELL_SUCC_EXIT_REASION_MOBILE_DATA_DISCONNECTED = 203;
    public static final int WIFI_2_CELL_SUCC_EXIT_REASION_MOBILE_DATA_OFF = 202;
    public static final int WIFI_2_CELL_SUCC_EXIT_REASION_OTHERS = 255;
    public static final int WIFI_2_CELL_SUCC_EXIT_REASION_WIFI_BETTER_OR_CELL_BAD = 201;
    public static final int WIFI_2_CELL_SUCC_EXIT_WIFI_DISCONNECTED = 204;
    public static final int WIFI_2_CELL_SUCC_GAIN_STATE_BAD = 0;
    public static final int WIFI_2_CELL_SUCC_GAIN_STATE_GOOD = 1;
    public static final int WIFI_2_CELL_TRIGGER_REASON_APPQOE_BAD_1_NETWORK_SLOW_1_WIFI_SIGNAL_2 = 3;
    public static final int WIFI_2_CELL_TRIGGER_REASON_APPQOE_BAD_3_NETWORK_SLOW_3 = 4;
    public static final int WIFI_2_CELL_TRIGGER_REASON_APPQOE_BAD_9_ENTERPRISE_WIFI = 5;
    public static final int WIFI_2_CELL_TRIGGER_REASON_INVAILD = 0;
    public static final int WIFI_2_CELL_TRIGGER_REASON_SIGNAL_LEVEL_1 = 6;
    public static final int WIFI_2_CELL_TRIGGER_REASON_WIFI_CHANNEL_BAD_1_WIFI_SIGNAL_2 = 1;
    public static final int WIFI_2_CELL_TRIGGER_REASON_WIFI_CHANNEL_BAD_3 = 2;
    public static final int WIFI_2_CELL_TRIGGER_TYPE_APP_QOE_BAD = 1;
    public static final int WIFI_2_CELL_TRIGGER_TYPE_CHANNEL_QOE_BAD = 2;
    public static final int WIFI_2_CELL_TRIGGER_TYPE_INVAILD = 0;
    public static final int WIFI_2_CELL_TRIGGER_TYPE_WIFI_SIGNAL_BAD = 3;
    private static volatile WifiProChr sWifiProChr = null;
    private int mChrWifi2CellSuccExitReason = WIFI_2_CELL_SUCC_EXIT_REASION_OTHERS;
    private int mChrWifi2CellSuccGainState = 1;
    private long mChrWifi2CellSuccStartTraffic = 0;
    private int mChrWifi2CellSuccTrafficSum = 0;
    private int mChrWifi2CellSuccTriggerType = 0;
    private ConcurrentHashMap<Integer, Wifi2CellDuration> mDurationStatisticsMap = new ConcurrentHashMap<>();
    private String mForePkgName = "";
    private ConcurrentHashMap<Integer, Wifi2CellQoe> mQoeStatisticsMap = new ConcurrentHashMap<>();
    private Wifi2CellParam mWifi2CellParam = new Wifi2CellParam();

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

    private void resetWifi2CellParam() {
        this.mWifi2CellParam.type = 0;
        this.mWifi2CellParam.rssi = 0;
        this.mWifi2CellParam.txRate = 0;
        this.mWifi2CellParam.rxRate = 0;
        this.mWifi2CellParam.ulDelay = 0;
        this.mWifi2CellParam.chload = 0;
        this.mWifi2CellParam.masterWifiQoe = 0;
        this.mWifi2CellParam.slaveWifiQoe = 0;
        this.mWifi2CellParam.masterCellQoe = 0;
        this.mWifi2CellParam.slaveCellQoe = 0;
        this.mWifi2CellParam.masterCellUpRate = 0;
        this.mWifi2CellParam.slaveCellUpRate = 0;
        this.mWifi2CellParam.triggerReason = 0;
        this.mWifi2CellParam.mpState = 0;
        this.mWifi2CellParam.statusCode = "";
        this.mWifi2CellParam.pkgName = "";
        this.mWifi2CellParam.enterpriseAp = 0;
        this.mWifi2CellParam.switchTime = 0;
        this.mWifi2CellParam.efftiveTime = 0;
        this.mWifi2CellParam.gainState = 0;
        this.mWifi2CellParam.traffic = 0;
        this.mWifi2CellParam.switchFailReason = 0;
        this.mWifi2CellParam.switchSuccExitReason = WIFI_2_CELL_SUCC_EXIT_REASION_BASE;
        this.mWifi2CellParam.wifi2CellStatus = -1;
    }

    private boolean isUpdateWifiCellParam(int wifi2CellStatus) {
        if (wifi2CellStatus == 0) {
            if (this.mWifi2CellParam.wifi2CellStatus == -1) {
                return true;
            }
            String str = TAG;
            Log.e(str, "isUpdateWifiCellParam: not update param, current wifi2CellStatus:" + this.mWifi2CellParam.wifi2CellStatus);
            return false;
        } else if (wifi2CellStatus == 1) {
            if (this.mWifi2CellParam.wifi2CellStatus == 0) {
                return true;
            }
            String str2 = TAG;
            Log.e(str2, "isUpdateWifiCellParam: not update param, current wifi2CellStatus:" + this.mWifi2CellParam.wifi2CellStatus);
            return false;
        } else if (wifi2CellStatus == 2) {
            if (this.mWifi2CellParam.wifi2CellStatus == 1) {
                return true;
            }
            String str3 = TAG;
            Log.e(str3, "isUpdateWifiCellParam: not update param, current wifi2CellStatus:" + this.mWifi2CellParam.wifi2CellStatus);
            return false;
        } else if (wifi2CellStatus != 3 || this.mWifi2CellParam.wifi2CellStatus == 0) {
            return true;
        } else {
            String str4 = TAG;
            Log.e(str4, "isUpdateWifiCellParam: not update param, current wifi2CellStatus:" + this.mWifi2CellParam.wifi2CellStatus);
            return false;
        }
    }

    public void updateWifi2CellParamHandoverStart(Wifi2CellParam wifi2CellParam) {
        if (this.mWifi2CellParam == null || wifi2CellParam == null) {
            Log.e(TAG, "updateWifi2CellParamHandoverStart: mWifi2CellParam or wifi2CellParam is null");
        } else if (isUpdateWifiCellParam(0)) {
            this.mWifi2CellParam.type = wifi2CellParam.type;
            this.mWifi2CellParam.rssi = wifi2CellParam.rssi;
            this.mWifi2CellParam.txRate = wifi2CellParam.txRate;
            this.mWifi2CellParam.rxRate = wifi2CellParam.rxRate;
            this.mWifi2CellParam.ulDelay = wifi2CellParam.ulDelay;
            this.mWifi2CellParam.chload = wifi2CellParam.chload;
            this.mWifi2CellParam.masterWifiQoe = wifi2CellParam.masterWifiQoe;
            this.mWifi2CellParam.slaveWifiQoe = wifi2CellParam.slaveWifiQoe;
            Wifi2CellParam wifi2CellParam2 = this.mWifi2CellParam;
            wifi2CellParam2.masterCellQoe = wifi2CellParam2.masterCellQoe;
            this.mWifi2CellParam.slaveCellQoe = wifi2CellParam.slaveCellQoe;
            this.mWifi2CellParam.masterCellUpRate = wifi2CellParam.masterCellUpRate;
            this.mWifi2CellParam.slaveCellUpRate = wifi2CellParam.slaveCellUpRate;
            this.mWifi2CellParam.triggerReason = wifi2CellParam.triggerReason;
            this.mWifi2CellParam.mpState = wifi2CellParam.mpState;
            this.mWifi2CellParam.statusCode = wifi2CellParam.statusCode;
            this.mWifi2CellParam.pkgName = wifi2CellParam.pkgName;
            this.mWifi2CellParam.enterpriseAp = wifi2CellParam.enterpriseAp;
            this.mWifi2CellParam.wifi2CellStatus = 0;
            String str = TAG;
            Log.d(str, "updateWifi2CellParamHandoverStart wifi to cell start: " + this.mWifi2CellParam.toString());
        }
    }

    public void updateWifi2CellParamHandoverSucc(int masterCellQoe, int slaveCellQoe, int masterCellUpRate, int slaveCellUpRate, int switchTime) {
        if (this.mWifi2CellParam == null) {
            Log.e(TAG, "updateWifi2CellParamHandoverSucc: mWifi2CellParam is null");
        } else if (isUpdateWifiCellParam(1)) {
            this.mWifi2CellParam.masterCellQoe = masterCellQoe;
            this.mWifi2CellParam.slaveCellQoe = slaveCellQoe;
            this.mWifi2CellParam.masterCellUpRate = masterCellUpRate;
            this.mWifi2CellParam.slaveCellUpRate = slaveCellUpRate;
            this.mWifi2CellParam.switchTime = switchTime;
            this.mWifi2CellParam.wifi2CellStatus = 1;
            String str = TAG;
            Log.d(str, "updateWifi2CellParamHandoverSucc: wifi2CellStatus:" + this.mWifi2CellParam.wifi2CellStatus + ", masterCellQoe:" + masterCellQoe + ", slaveCellQoe:" + slaveCellQoe + ", masterCellUpRate:" + masterCellUpRate + ", slaveCellUpRate:" + slaveCellUpRate + ", switchTime:" + switchTime);
        }
    }

    public void updateWifi2CellParamSuccExit(int switchSuccExitReason, int rssi, int efftiveTime, int gainState, int traffic) {
        if (this.mWifi2CellParam == null) {
            Log.e(TAG, "updateWifi2CellParamSuccExit: mWifi2CellParam is null");
        } else if (isUpdateWifiCellParam(2)) {
            this.mWifi2CellParam.rssi = rssi;
            this.mWifi2CellParam.efftiveTime = efftiveTime;
            this.mWifi2CellParam.gainState = gainState;
            this.mWifi2CellParam.traffic = traffic;
            this.mWifi2CellParam.switchSuccExitReason = switchSuccExitReason;
            this.mWifi2CellParam.wifi2CellStatus = 2;
            String str = TAG;
            Log.d(str, "updateWifi2CellParamSuccExit: wifi2CellStatus:" + this.mWifi2CellParam.wifi2CellStatus + ", rssi:" + rssi + ", efftiveTime:" + efftiveTime + ", gainState:" + gainState + ", traffic:" + traffic + ", switchSuccExitReason:" + switchSuccExitReason);
        }
    }

    public void updateWifi2CellParamHandoverFail(int failureReason, int rssi) {
        if (this.mWifi2CellParam == null) {
            Log.e(TAG, "updateWifi2CellParamHandoverFail: mWifi2CellParam is null");
        } else if (isUpdateWifiCellParam(3)) {
            this.mWifi2CellParam.rssi = rssi;
            this.mWifi2CellParam.switchFailReason = failureReason;
            this.mWifi2CellParam.wifi2CellStatus = 3;
            String str = TAG;
            Log.d(str, "updateWifi2CellParamHandoverFail: wifi2CellStatus:" + this.mWifi2CellParam.wifi2CellStatus + ", failureReason:" + failureReason + ", rssi:" + rssi);
        }
    }

    public void updateWifi2CellQoe(Wifi2CellQoe wifi2CellQoe) {
        if (this.mQoeStatisticsMap == null || wifi2CellQoe == null) {
            Log.e(TAG, "updateWifi2CellQoe: mQoeStatisticsMap or wifi2CellQoe is null");
        } else if (wifi2CellQoe.type == 0) {
            String str = TAG;
            Log.d(str, "updateWifi2CellQoe: type is invaild:" + wifi2CellQoe.type);
        } else {
            if (this.mQoeStatisticsMap.containsKey(Integer.valueOf(wifi2CellQoe.type))) {
                Wifi2CellQoe wifi2CellQoeNode = this.mQoeStatisticsMap.get(Integer.valueOf(wifi2CellQoe.type));
                Wifi2CellQoe.access$2512(wifi2CellQoeNode, wifi2CellQoe.qoeGoodCount);
                Wifi2CellQoe.access$2612(wifi2CellQoeNode, wifi2CellQoe.qoeFirstGoodTime);
                Wifi2CellQoe.access$2712(wifi2CellQoeNode, wifi2CellQoe.count);
                Wifi2CellQoe.access$2812(wifi2CellQoeNode, wifi2CellQoe.switchTimeSum);
                Wifi2CellQoe.access$2912(wifi2CellQoeNode, wifi2CellQoe.efftiveTimeSum);
                Wifi2CellQoe.access$3012(wifi2CellQoeNode, wifi2CellQoe.trafficSum);
            } else {
                Wifi2CellQoe wifi2CellQoeNode2 = new Wifi2CellQoe();
                wifi2CellQoeNode2.type = wifi2CellQoe.type;
                wifi2CellQoeNode2.qoeGoodCount = wifi2CellQoe.qoeGoodCount;
                wifi2CellQoeNode2.qoeFirstGoodTime = wifi2CellQoe.qoeFirstGoodTime;
                wifi2CellQoeNode2.count = wifi2CellQoe.count;
                wifi2CellQoeNode2.switchTimeSum = wifi2CellQoe.switchTimeSum;
                wifi2CellQoeNode2.efftiveTimeSum = wifi2CellQoe.efftiveTimeSum;
                wifi2CellQoeNode2.trafficSum = wifi2CellQoe.trafficSum;
                this.mQoeStatisticsMap.put(Integer.valueOf(wifi2CellQoe.type), wifi2CellQoeNode2);
            }
            String str2 = TAG;
            Log.d(str2, "updateWifi2CellQoe: " + wifi2CellQoe.toString());
        }
    }

    public void updateWifi2CellDuration(int type, int durationType, int count) {
        if (type == 0) {
            String str = TAG;
            Log.d(str, "updateWifi2CellDuration: type is invaild:" + type);
        } else if (durationType >= 4) {
            Log.e(TAG, "updateWifi2CellDuration: durationType is illegal");
        } else {
            ConcurrentHashMap<Integer, Wifi2CellDuration> concurrentHashMap = this.mDurationStatisticsMap;
            if (concurrentHashMap == null) {
                Log.e(TAG, "updateWifi2CellDuration: mDurationStatisticsMap is null");
            } else if (concurrentHashMap.containsKey(Integer.valueOf(type))) {
                int[] iArr = this.mDurationStatisticsMap.get(Integer.valueOf(type)).durationCounts;
                iArr[durationType] = iArr[durationType] + count;
            } else {
                Wifi2CellDuration wifi2CellDuration = new Wifi2CellDuration();
                wifi2CellDuration.durationCounts[durationType] = count;
                this.mDurationStatisticsMap.put(Integer.valueOf(type), wifi2CellDuration);
            }
        }
    }

    public void reportWifi2CellParam() {
        Wifi2CellParam wifi2CellParam = this.mWifi2CellParam;
        if (wifi2CellParam == null) {
            Log.e(TAG, "reportWifi2CellParam: wifi2CellParam is null");
            return;
        }
        if (wifi2CellParam.wifi2CellStatus == 3) {
            reportWifi2CellFailParam();
        }
        if (this.mWifi2CellParam.wifi2CellStatus == 2) {
            reportWifi2CellSuccParam();
            reportWifi2CellSuccExitParam();
        }
        resetWifi2CellParam();
    }

    public void reportWifi2CellQoe() {
        ConcurrentHashMap<Integer, Wifi2CellQoe> concurrentHashMap = this.mQoeStatisticsMap;
        if (concurrentHashMap == null) {
            Log.e(TAG, "reportWifi2CellQoe: mQoeStatisticsMap is null");
            return;
        }
        for (Map.Entry<Integer, Wifi2CellQoe> entry : concurrentHashMap.entrySet()) {
            int type = entry.getKey().intValue();
            Wifi2CellQoe wifi2CellQoe = entry.getValue();
            int aveTime = 0;
            int aveSwitchTime = 0;
            int aveEfftiveTime = 0;
            int aveTraffic = 0;
            if (wifi2CellQoe.count != 0) {
                aveTime = wifi2CellQoe.qoeFirstGoodTime / wifi2CellQoe.count;
                aveSwitchTime = wifi2CellQoe.switchTimeSum / wifi2CellQoe.count;
                aveEfftiveTime = wifi2CellQoe.efftiveTimeSum / wifi2CellQoe.count;
                aveTraffic = wifi2CellQoe.trafficSum / wifi2CellQoe.count;
            }
            HiEvent event = new HiEvent((int) EVENT_ID_WIFI_TO_CELL_QOE);
            event.putInt(KEY_TYPE, type).putInt(KEY_GOOD_CNT, wifi2CellQoe.qoeGoodCount).putInt(KEY_AVE_TIME, aveTime).putInt(KEY_CNT, wifi2CellQoe.count).putInt(KEY_AVE_SWITCH_TIME, aveSwitchTime).putInt(KEY_AVE_EFFTIVE_TIME, aveEfftiveTime).putInt(KEY_AVE_TRAFFIC, aveTraffic);
            HiView.report(event);
            String str = TAG;
            Log.d(str, "reportWifi2CellQoe: type=" + type + ", " + wifi2CellQoe.toString());
        }
        this.mQoeStatisticsMap.clear();
    }

    public void reportWifi2CellDuration() {
        ConcurrentHashMap<Integer, Wifi2CellDuration> concurrentHashMap = this.mDurationStatisticsMap;
        if (concurrentHashMap == null) {
            Log.e(TAG, "reportWifi2CellTime: mDurationStatisticsMap is null");
            return;
        }
        for (Map.Entry<Integer, Wifi2CellDuration> entry : concurrentHashMap.entrySet()) {
            int type = entry.getKey().intValue();
            Wifi2CellDuration wifi2CellDuration = entry.getValue();
            for (int i = 0; i < 4; i++) {
                if (wifi2CellDuration.durationCounts[i] != 0) {
                    HiEvent event = new HiEvent((int) EVENT_ID_WIFI_TO_CELL_DURATION);
                    event.putInt(KEY_TYPE, type).putInt(KEY_SWITCH_TIME, i).putInt(KEY_CNT, wifi2CellDuration.durationCounts[i]);
                    HiView.report(event);
                }
            }
            String str = TAG;
            Log.d(str, "reportWifi2CellDuration: type=" + type + ", " + wifi2CellDuration.toString());
        }
        this.mDurationStatisticsMap.clear();
    }

    private void reportWifi2CellFailParam() {
        HiEvent event = new HiEvent((int) EVENT_ID_WIFI_TO_CELL_FAIL);
        event.putInt(KEY_TYPE, 1).putInt(KEY_FAILURE_REASON, this.mWifi2CellParam.getSwitchFailReason()).putInt(KEY_TRIGGER_REASON, this.mWifi2CellParam.getTriggerReason()).putString(KEY_PKG_NAME, this.mWifi2CellParam.getPkgName()).putInt(KEY_ENTERPRISE_AP, this.mWifi2CellParam.getEnterpriseAp()).putInt(KEY_RSSI, this.mWifi2CellParam.getRssi());
        HiView.report(event);
        String str = TAG;
        Log.d(str, "reportWifi2CellFailParam: " + this.mWifi2CellParam.toString());
    }

    private void reportWifi2CellSuccExitParam() {
        HiEvent event = new HiEvent((int) EVENT_ID_WIFI_TO_CELL_FAIL);
        event.putInt(KEY_TYPE, 2).putInt(KEY_FAILURE_REASON, this.mWifi2CellParam.getSwitchSuccExitReason()).putInt(KEY_TRIGGER_REASON, this.mWifi2CellParam.getTriggerReason()).putString(KEY_PKG_NAME, this.mWifi2CellParam.getPkgName()).putInt(KEY_ENTERPRISE_AP, this.mWifi2CellParam.getEnterpriseAp()).putInt(KEY_RSSI, this.mWifi2CellParam.getRssi());
        HiView.report(event);
        String str = TAG;
        Log.d(str, "reportWifi2CellSuccExitParam: " + this.mWifi2CellParam.toString());
    }

    private void reportWifi2CellSuccParam() {
        HiEvent event = new HiEvent((int) EVENT_ID_WIFI_TO_CELL_SUCC);
        event.putInt(KEY_TYPE, this.mWifi2CellParam.getType()).putInt(KEY_RSSI, this.mWifi2CellParam.getRssi()).putInt(KEY_TX_RATE, this.mWifi2CellParam.getTxRate()).putInt(KEY_RX_RATE, this.mWifi2CellParam.getRxRate()).putInt(KEY_UL_DELAY, this.mWifi2CellParam.getUlDelay()).putInt(KEY_CHLOAD, this.mWifi2CellParam.getChload()).putInt(KEY_MASTER_WIFI_QOE, this.mWifi2CellParam.getMasterWifiQoe()).putInt(KEY_SLAVE_WIFI_QOE, this.mWifi2CellParam.getSlaveWifiQoe()).putInt(KEY_MASTER_CELL_QOE, this.mWifi2CellParam.getMasterCellQoe()).putInt(KEY_SLAVE_CELL_QOE, this.mWifi2CellParam.getSlaveCellQoe()).putInt(KEY_MASTER_CELL_UP_RATE, this.mWifi2CellParam.getMasterCellUpRate()).putInt(KEY_SLAVE_CELL_UP_RATE, this.mWifi2CellParam.getSlaveCellUpRate()).putInt(KEY_TRIGGER_REASON, this.mWifi2CellParam.getTriggerReason()).putInt(KEY_MP_STATE, this.mWifi2CellParam.getMpState()).putString(KEY_STATUS_CODE, this.mWifi2CellParam.getStatusCode()).putString(KEY_PKG_NAME, this.mWifi2CellParam.getPkgName()).putInt(KEY_ENTERPRISE_AP, this.mWifi2CellParam.getEnterpriseAp()).putInt(KEY_SWITCH_TIME, this.mWifi2CellParam.getSwitchTime()).putInt(KEY_EFFTIVE_TIME, this.mWifi2CellParam.getEfftiveTime()).putInt(KEY_GAIN_STATE, this.mWifi2CellParam.getGainState()).putInt(KEY_TRAFFIC, this.mWifi2CellParam.getTraffic());
        HiView.report(event);
        String str = TAG;
        Log.d(str, "reportWifi2CellSuccParam: " + this.mWifi2CellParam.toString());
    }

    public static class Wifi2CellParam {
        private int chload = 0;
        private int efftiveTime = 0;
        private int enterpriseAp = 0;
        private int gainState = 0;
        private int masterCellQoe = 0;
        private int masterCellUpRate = 0;
        private int masterWifiQoe = 0;
        private int mpState = 0;
        private String pkgName = "";
        private int rssi = 0;
        private int rxRate = 0;
        private int slaveCellQoe = 0;
        private int slaveCellUpRate = 0;
        private int slaveWifiQoe = 0;
        private String statusCode = "";
        private int switchFailReason = 0;
        private int switchSuccExitReason = WifiProChr.WIFI_2_CELL_SUCC_EXIT_REASION_BASE;
        private int switchTime = 0;
        private int traffic = 0;
        private int triggerReason = 0;
        private int txRate = 0;
        private int type = 0;
        private int ulDelay = 0;
        private int wifi2CellStatus = -1;

        public int getType() {
            return this.type;
        }

        public void setType(int type2) {
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

        public int getTriggerReason() {
            return this.triggerReason;
        }

        public void setTriggerReason(int triggerReason2) {
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

        public String getPkgName() {
            return this.pkgName;
        }

        public void setPkgName(String pkgName2) {
            this.pkgName = pkgName2;
        }

        public int getEnterpriseAp() {
            return this.enterpriseAp;
        }

        public void setEnterpriseAp(int enterpriseAp2) {
            this.enterpriseAp = enterpriseAp2;
        }

        public int getSwitchTime() {
            return this.switchTime;
        }

        public void setSwitchTime(int switchTimeSum) {
            this.switchTime = this.switchTime;
        }

        public int getEfftiveTime() {
            return this.efftiveTime;
        }

        public void setEfftiveTime(int efftiveTime2) {
            this.efftiveTime = efftiveTime2;
        }

        public int getGainState() {
            return this.gainState;
        }

        public void setGainState(int gainState2) {
            this.gainState = gainState2;
        }

        public int getTraffic() {
            return this.traffic;
        }

        public void setTraffic(int traffic2) {
            this.traffic = traffic2;
        }

        public int getSwitchFailReason() {
            return this.switchFailReason;
        }

        public void setSwitchFailReason(int switchFailReason2) {
            this.switchFailReason = switchFailReason2;
        }

        public int getSwitchSuccExitReason() {
            return this.switchSuccExitReason;
        }

        public void setSwitchSuccExitReason(int switchSuccExitReason2) {
            this.switchSuccExitReason = switchSuccExitReason2;
        }

        public String toString() {
            return "Wifi2CellParam{type=" + this.type + ", rssi=" + this.rssi + ", txRate=" + this.txRate + ", rxRate=" + this.rxRate + ", ulDelay=" + this.ulDelay + ", chload=" + this.chload + ", masterWifiQoe=" + this.masterWifiQoe + ", slaveWifiQoe=" + this.slaveWifiQoe + ", masterCellQoe=" + this.masterCellQoe + ", slaveCellQoe=" + this.slaveCellQoe + ", masterCellUpRate=" + this.masterCellUpRate + ", slaveCellUpRate=" + this.slaveCellUpRate + ", triggerReason=" + this.triggerReason + ", mpState=" + this.mpState + ", statusCode=" + this.statusCode + ", pkgName=" + this.pkgName + ", enterpriseAp=" + this.enterpriseAp + ", switchTimeSum=" + this.switchTime + ", efftiveTimeSum=" + this.efftiveTime + ", gainState=" + this.gainState + ", trafficSum=" + this.traffic + ", switchFailReason=" + this.switchFailReason + ", switchSuccExitReason=" + this.switchSuccExitReason + '}';
        }
    }

    public static class Wifi2CellQoe {
        private int count = 0;
        private int efftiveTimeSum = 0;
        private int qoeFirstGoodTime = 0;
        private int qoeGoodCount = 0;
        private int switchTimeSum = 0;
        private int trafficSum = 0;
        private int type = 0;

        static /* synthetic */ int access$2512(Wifi2CellQoe x0, int x1) {
            int i = x0.qoeGoodCount + x1;
            x0.qoeGoodCount = i;
            return i;
        }

        static /* synthetic */ int access$2612(Wifi2CellQoe x0, int x1) {
            int i = x0.qoeFirstGoodTime + x1;
            x0.qoeFirstGoodTime = i;
            return i;
        }

        static /* synthetic */ int access$2712(Wifi2CellQoe x0, int x1) {
            int i = x0.count + x1;
            x0.count = i;
            return i;
        }

        static /* synthetic */ int access$2812(Wifi2CellQoe x0, int x1) {
            int i = x0.switchTimeSum + x1;
            x0.switchTimeSum = i;
            return i;
        }

        static /* synthetic */ int access$2912(Wifi2CellQoe x0, int x1) {
            int i = x0.efftiveTimeSum + x1;
            x0.efftiveTimeSum = i;
            return i;
        }

        static /* synthetic */ int access$3012(Wifi2CellQoe x0, int x1) {
            int i = x0.trafficSum + x1;
            x0.trafficSum = i;
            return i;
        }

        public int getType() {
            return this.type;
        }

        public void setType(int type2) {
            this.type = type2;
        }

        public int getQoeGoodCount() {
            return this.qoeGoodCount;
        }

        public void setQoeGoodCount(int qoeGoodCount2) {
            this.qoeGoodCount = qoeGoodCount2;
        }

        public int getQoeFirstGoodTime() {
            return this.qoeFirstGoodTime;
        }

        public void setQoeFirstGoodTime(int qoeFirstGoodTime2) {
            this.qoeFirstGoodTime = qoeFirstGoodTime2;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count2) {
            this.count = count2;
        }

        public int getSwitchTimeSum() {
            return this.switchTimeSum;
        }

        public void setSwitchTimeSum(int switchTimeSum2) {
            this.switchTimeSum = switchTimeSum2;
        }

        public int getEfftiveTimeSum() {
            return this.efftiveTimeSum;
        }

        public void setEfftiveTimeSum(int efftiveTimeSum2) {
            this.efftiveTimeSum = efftiveTimeSum2;
        }

        public int getTrafficSum() {
            return this.trafficSum;
        }

        public void setTrafficSum(int trafficSum2) {
            this.trafficSum = trafficSum2;
        }

        public String toString() {
            return "Wifi2CellQoe{qoeGoodCount=" + this.qoeGoodCount + ", qoeFirstGoodTime=" + this.qoeFirstGoodTime + ", count=" + this.count + ", switchTimeSum=" + this.switchTimeSum + ", efftiveTimeSum=" + this.efftiveTimeSum + ", trafficSum=" + this.trafficSum + '}';
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

    public void setChrForePkgName(String pkgName) {
        if (pkgName != null) {
            this.mForePkgName = pkgName;
        }
    }

    public String getChrForePkgName() {
        return this.mForePkgName;
    }

    public void setChrSwitchSuccStartTraffic(long startTraffic) {
        this.mChrWifi2CellSuccStartTraffic = startTraffic;
    }

    public long getChrSwitchSuccStartTraffic() {
        return this.mChrWifi2CellSuccStartTraffic;
    }

    public void setChrSwitchSuccTrafficSum(int trafficSum) {
        if (trafficSum >= 0) {
            this.mChrWifi2CellSuccTrafficSum = trafficSum;
        }
    }

    public int getChrSwitchSuccTrafficSum() {
        return this.mChrWifi2CellSuccTrafficSum;
    }

    public void setChrSwitchCellGainState(int gainState) {
        this.mChrWifi2CellSuccGainState = gainState;
    }

    public int getChrSwitchCellGainState() {
        return this.mChrWifi2CellSuccGainState;
    }

    public void setChrSwitchCellSuccTriggerType(int triggerType) {
        this.mChrWifi2CellSuccTriggerType = triggerType;
    }

    public int getChrSwitchCellSuccTriggerType() {
        return this.mChrWifi2CellSuccTriggerType;
    }

    public void setChrSwitchSuccedExitReason(int exitReason) {
        this.mChrWifi2CellSuccExitReason = exitReason;
    }

    public int getChrSwitchSuccedExitReason() {
        return this.mChrWifi2CellSuccExitReason;
    }
}
