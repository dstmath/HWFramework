package com.android.server.wifi.wifipro;

import android.content.Context;
import android.net.wifi.wifipro.WifiProStatusUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.wifi.HwWifiCHRStateManagerImpl;
import com.android.server.wifipro.WifiProCHRManager;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WifiProStatisticsManager {
    public static final int AC_FAIL_TYPE_REFUSE = 1;
    public static final int AC_FAIL_TYPE_RESET = 2;
    public static final int AC_FAIL_TYPE_UNKNOWN = 0;
    public static final int AF_FAILURE = 0;
    public static final int AF_SUCCESS = 1;
    public static final int AF_TYPE_AUTO_LOGIN = 2;
    public static final int AF_TYPE_FILL_PASSWORD = 1;
    public static final int AF_TYPE_FILL_PHONE_NUM = 0;
    public static final int AF_TYPE_FPN_SUCC_NOT_MSM = 3;
    private static final long BETA_USER_UPLOAD_PERIOD = 86400000;
    public static final int BG_CHR_TYPE_ASSOC_REJECT_CNT = 19;
    public static final int BG_CHR_TYPE_AUTH_FAIL_CNT = 18;
    public static final int BG_CHR_TYPE_BACK_GROUND_RUN_CNT = 1;
    public static final int BG_CHR_TYPE_BG_INET_OK_ACTIVE_NOT_OK = 13;
    public static final int BG_CHR_TYPE_BG_NOT_INET_ACTIVE_IOK = 12;
    public static final int BG_CHR_TYPE_CONNT_TIMEOUT_CNT = 15;
    public static final int BG_CHR_TYPE_DHCP_FAIL_CNT = 17;
    public static final int BG_CHR_TYPE_DNS_FAIL_CNT = 16;
    public static final int BG_CHR_TYPE_FAILED_CNT = 11;
    public static final int BG_CHR_TYPE_FISHING_AP_CNT = 4;
    public static final int BG_CHR_TYPE_FOUND_TWO_MORE_AP_CNT = 10;
    public static final int BG_CHR_TYPE_FREE_INET_OK_AP_CNT = 3;
    public static final int BG_CHR_TYPE_FREE_NOT_INET_AP_CNT = 5;
    public static final int BG_CHR_TYPE_NC_BY_CHECK_FAIL = 21;
    public static final int BG_CHR_TYPE_NC_BY_CONNECT_FAIL = 20;
    public static final int BG_CHR_TYPE_NC_BY_STATE_ERR = 22;
    public static final int BG_CHR_TYPE_NC_BY_UNKNOWN = 23;
    public static final int BG_CHR_TYPE_PORTAL_AP_CNT = 6;
    public static final int BG_CHR_TYPE_SETTING_RUN_CNT = 2;
    public static final int BG_CHR_TYPE_USER_SEL_AP_FISHING_CNT = 14;
    public static final int BG_CHR_TYPE_USER_SEL_FREE_IOK_CNT = 7;
    public static final int BG_CHR_TYPE_USER_SEL_NOT_INET_CNT = 8;
    public static final int BG_CHR_TYPE_USER_SEL_PORTAL_CNT = 9;
    public static final int BQE_BAD_W2W_HANDOVER = 0;
    public static final int BQE_BIND_WLAN_FAIL_COUNT = 14;
    public static final int BQE_CN_URL1_FAIL_COUNT = 7;
    public static final int BQE_CN_URL2_FAIL_COUNT = 8;
    public static final int BQE_CN_URL3_FAIL_COUNT = 9;
    public static final int BQE_NCN_URL1_FAIL_COUNT = 10;
    public static final int BQE_NCN_URL2_FAIL_COUNT = 11;
    public static final int BQE_NCN_URL3_FAIL_COUNT = 12;
    public static final int BQE_RI_REASON_RSSI_RESTORE = 1;
    public static final int BQE_RI_REASON_RSSI_RISE_15DB = 2;
    public static final int BQE_RI_REASON_TIMER_TIMEOUT = 3;
    public static final int BQE_RI_REASON_UNKNOWN = 0;
    public static final int BQE_SCORE_UNKNOWN_COUNT = 13;
    public static final int BQE_STOP_BQE_FAIL_COUNT = 15;
    public static final int BSG_CHR_TYPE_END_IN_4S_CNT = 4;
    public static final int BSG_CHR_TYPE_END_IN_4S_T_7S_CNT = 5;
    public static final int BSG_CHR_TYPE_NOT_END_IN_7S_CNT = 6;
    public static final int BSG_CHR_TYPE_RS_BAD_CNT = 3;
    public static final int BSG_CHR_TYPE_RS_GOOD_CNT = 1;
    public static final int BSG_CHR_TYPE_RS_MID_CNT = 2;
    private static final int CHECK_UPLOAD_TIME_INTERVAL = 1800000;
    public static final int CHR_TYPE_BG_AV_CN_NAV = 1;
    public static final int CHR_TYPE_BG_AV_CN_POT = 2;
    public static final int CHR_TYPE_BG_CGT_CN_AV = 7;
    public static final int CHR_TYPE_BG_CGT_CN_NAV = 8;
    public static final int CHR_TYPE_BG_CGT_CN_POT = 9;
    public static final int CHR_TYPE_BG_CN_UNKNOWN = 0;
    public static final int CHR_TYPE_BG_NAV_CN_AV = 3;
    public static final int CHR_TYPE_BG_NAV_CN_POT = 4;
    public static final int CHR_TYPE_BG_POT_CN_AV = 5;
    public static final int CHR_TYPE_BG_POT_CN_NAV = 6;
    private static final int CLICK_CANCEL_RI_DEFAULT_TIME = 1;
    private static final long COMMERCIAL_USER_UPLOAD_PERIOD = 172800000;
    private static final int DBG_LOG_LEVEL = 1;
    private static boolean DEBUG_MODE = false;
    private static final int DEBUG_MODE_CHECK_UPLOAD_TIME_INTERVAL = 60000;
    private static final long DEBUG_MODE_UPLOAD_PERIOD = 480000;
    public static final int DUALBAND_BT_IS_CONNECTED = 2;
    public static final int DUALBAND_BT_IS_DISCONNECTED = 1;
    public static final int DUALBAND_CUSTOMIZED_SCAN_FAIL_COUNT = 26;
    public static final int DUALBAND_CUSTOMIZED_SCAN_SUCC_COUNT = 25;
    public static final int DUALBAND_HANDOVER_PINGPONG_COUNT = 31;
    public static final int DUALBAND_HANDOVER_TOO_SLOW_COUNT = 28;
    public static final int DUALBAND_HANDOVER_TO_NOT_INET_5G_COUNT = 27;
    public static final int DUALBAND_MIX_AP_DISAPPER_COUNT = 17;
    public static final int DUALBAND_MIX_AP_HANDOVER_FAIL_COUNT = 21;
    public static final int DUALBAND_MIX_AP_HANDOVER_SUC_COUNT = 20;
    public static final int DUALBAND_MIX_AP_HIGH_FREQ_SCAN_5G_COUNT = 24;
    public static final int DUALBAND_MIX_AP_INBLACK_LIST_COUNT = 18;
    public static final int DUALBAND_MIX_AP_LEARNED_COUNT = 13;
    public static final int DUALBAND_MIX_AP_LOW_FREQ_SCAN_5G_COUNT = 22;
    public static final int DUALBAND_MIX_AP_MID_FREQ_SCAN_5G_COUNT = 23;
    public static final int DUALBAND_MIX_AP_MONITOR_COUNT = 15;
    public static final int DUALBAND_MIX_AP_NEARBY_COUNT = 14;
    public static final int DUALBAND_MIX_AP_SATISFIED_COUNT = 16;
    public static final int DUALBAND_MIX_AP_SCORE_NOTSATISFY_COUNT = 19;
    public static final int DUALBAND_MIX_AP_TYPE = 2;
    public static final int DUALBAND_SINGLE_AP_DISAPPER_COUNT = 5;
    public static final int DUALBAND_SINGLE_AP_HANDOVER_FAIL_COUNT = 9;
    public static final int DUALBAND_SINGLE_AP_HANDOVER_SUC_COUNT = 8;
    public static final int DUALBAND_SINGLE_AP_HIGH_FREQ_SCAN_5G_COUNT = 12;
    public static final int DUALBAND_SINGLE_AP_INBLACKLIST_COUNT = 6;
    public static final int DUALBAND_SINGLE_AP_LEARNED_COUNT = 1;
    public static final int DUALBAND_SINGLE_AP_LOW_FREQ_SCAN_5G_COUNT = 10;
    public static final int DUALBAND_SINGLE_AP_MID_FREQ_SCAN_5G_COUNT = 11;
    public static final int DUALBAND_SINGLE_AP_MONITOR_COUNT = 3;
    public static final int DUALBAND_SINGLE_AP_NEARBY_COUNT = 2;
    public static final int DUALBAND_SINGLE_AP_SATISFIED_COUNT = 4;
    public static final int DUALBAND_SINGLE_AP_SCORE_NOT_SATISFY_COUNT = 7;
    public static final int DUALBAND_SINGLE_AP_TYPE = 1;
    public static final int DUALBAND_TO_BAD_5G_COUNT = 29;
    public static final int DUALBAND_USER_REJECT_HANDOVER_COUNT = 30;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int ERROR_TIME_INTERVAL = -1;
    public static final int FALSE_INT_VAL = 0;
    public static final int HMD_10_NOTIFY_TYPE = 1;
    public static final int HMD_50_NOTIFY_TYPE = 2;
    private static final int HMD_DATA_SIZE_10000KB = 10240;
    private static final int HMD_DATA_SIZE_50000KB = 51200;
    private static final int INFO_LOG_LEVEL = 2;
    private static final short INVALID_MOBILE_SIGNAL_LEVEL = (short) -1;
    private static final int INVALID_TIME_VALUE = 0;
    private static final long MAX_BETA_USER_UPLOAD_PERIOD = 432000000;
    private static final long MAX_COMMERCIAL_USER_UPLOAD_PERIOD = 1296000000;
    private static final int MAX_INT_TYPE_VALUE = 2147483632;
    private static final int MAX_SHORT_TYPE_VALUE = 32760;
    private static final int MILLISECONDS_OF_ONE_MINUTE = 60000;
    private static final int MILLISECONDS_OF_ONE_SECOND = 1000;
    public static final int NOT_INET_W2W_HANDOVER = 1;
    public static final int OOBE_WIFIPRO_DISABLE = 2;
    public static final int OOBE_WIFIPRO_ENABLE = 1;
    public static final int OOBE_WIFIPRO_UNKNOWN = 0;
    public static final int RI_REASON_DATA_SERVICE_CLOSE = 3;
    public static final int RI_REASON_DATA_SERVICE_POOR_QUILITY = 7;
    public static final int RI_REASON_HISTORY_RECORD_RI = 6;
    public static final int RI_REASON_HMD_10M_USER_BT_RI = 8;
    public static final int RI_REASON_HMD_50M_USER_BT_RI = 9;
    public static final int RI_REASON_UNKNOWN = 0;
    public static final int RI_REASON_USER_CLICK_CANCEL_BUTTON = 2;
    public static final int RI_REASON_USER_CLOSE_WIFIPRO = 5;
    public static final int RI_REASON_USER_MANUAL_RI_BOTTON = 4;
    public static final int RI_REASON_WIFI_BQE_REPORT_GOOD = 1;
    private static final int ROVE_OUT_MOBILE_DATA_NOTIFY_FIFTY_M = 2;
    private static final int ROVE_OUT_MOBILE_DATA_NOTIFY_TEM_M = 1;
    private static final int ROVE_OUT_MOBILE_DATA_NOTIFY_UNKNOWN = 0;
    private static final int RO_REASON_BIG_RTT = 8;
    public static final int RO_REASON_BQE_REPORT_BAD = 1;
    public static final int RO_REASON_NOT_INET_CAPABILITY = 2;
    private static final int RO_REASON_OTA_TCP_BAD = 2;
    private static final int RO_REASON_RSSI_TCP_BAD = 1;
    private static final int RO_REASON_TCP_BAD = 4;
    public static final int RO_REASON_UNKNOWN = 0;
    private static final int SECONDS_OF_ONE_HOUR = 3600;
    private static final int SECONDS_OF_ONE_MINUTE = 60;
    private static final int STAT_MSG_ACTIVE_CHECK_RS_DIFF = 145;
    private static final int STAT_MSG_ADD_TOTAL_BQE_ROC = 112;
    private static final int STAT_MSG_AF_CHR_UPDATE = 135;
    private static final int STAT_MSG_BACK_GRADING_CHR_UPDATE = 136;
    private static final int STAT_MSG_BQE_BAD_RO_DISCONNECT_MOBILE_DATA = 138;
    private static final int STAT_MSG_BQE_BAD_SETTING_CANCEL = 125;
    private static final int STAT_MSG_BQE_GRADING_SVC_CHR_UPDATE = 137;
    private static final int STAT_MSG_BQE_RO_PARA_UPDATE = 115;
    private static final int STAT_MSG_CELL_AUTO_CLOSE_COUNT = 111;
    private static final int STAT_MSG_CELL_AUTO_OPEN_COUNT = 110;
    private static final int STAT_MSG_CHECK_NEED_UPLOAD_EVENT = 101;
    private static final int STAT_MSG_HIGH_DATA_RATE_STOP_ROC = 123;
    private static final int STAT_MSG_HISTORY_SCORE_RI_COUNT = 116;
    private static final int STAT_MSG_HMD_NOTIFY_CNT = 133;
    private static final int STAT_MSG_HMD_USER_DEL_NOTIFY_CNT = 134;
    private static final int STAT_MSG_HOME_AP_ADD_RO_PERIOD_CNT = 142;
    private static final int STAT_MSG_INCREASE_AC_RS_SAME_COUNT = 149;
    private static final int STAT_MSG_INCREASE_CONN_BLOCK_PORTAL_COUNT = 148;
    private static final int STAT_MSG_INCREASE_DUALBAND_STATISTIC_COUNT = 151;
    private static final int STAT_MSG_INCREASE_HMD_BTN_RI_COUNT = 150;
    private static final int STAT_MSG_INCREASE_PORTAL_AUTH_SUCC_COUNT = 147;
    private static final int STAT_MSG_INCREASE_PORTAL_CONN_COUNT = 146;
    private static final int STAT_MSG_LOAD_DB_RECORD = 100;
    private static final int STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT = 122;
    private static final int STAT_MSG_NOT_INET_ALARM_COUNT = 118;
    private static final int STAT_MSG_NOT_INET_NET_RESTORE_RI = 128;
    private static final int STAT_MSG_NOT_INET_RO_DISCONNECT_MOBILE_DATA = 139;
    private static final int STAT_MSG_NOT_INET_SETTING_CANCEL = 126;
    private static final int STAT_MSG_NOT_INET_USER_CANCEL = 127;
    private static final int STAT_MSG_NOT_INET_USER_MANUAL_RI = 129;
    private static final int STAT_MSG_NO_INET_HANDOVER_COUNT = 104;
    private static final int STAT_MSG_OOB_INIT_STATE = 117;
    private static final int STAT_MSG_PING_PONG_COUNT = 124;
    private static final int STAT_MSG_PORTAL_AP_NOT_AUTO_CONN = 141;
    private static final int STAT_MSG_PORTAL_AUTO_LOGIN_COUNT = 109;
    private static final int STAT_MSG_PORTAL_CODE_PARSE_COUNT = 107;
    private static final int STAT_MSG_PORTAL_UNAUTH_COUNT = 105;
    private static final int STAT_MSG_RCVSMS_COUNT = 108;
    private static final int STAT_MSG_RI_EVENT = 114;
    private static final int STAT_MSG_RO_EVENT = 113;
    private static final int STAT_MSG_SCREEN_ON = 130;
    private static final int STAT_MSG_SELECT_NOT_INET_AP_COUNT = 121;
    private static final int STAT_MSG_SEL_CSP_SETTING_CHG_CNT = 132;
    private static final int STAT_MSG_UPDATE_SATTISTIC_TO_DB = 144;
    private static final int STAT_MSG_UPDATE_WIFI_CONNECTION_STATE = 140;
    private static final int STAT_MSG_USER_REOPEN_WIFI_RI_CNT = 131;
    private static final int STAT_MSG_USER_USE_BG_SCAN_COUNT = 119;
    private static final int STAT_MSG_WIFIPRO_STATE_CHG = 103;
    private static final int STAT_MSG_WIFI_SCO_COUNT = 106;
    private static final int STAT_MSG_WIFI_TO_WIFI_SUCC_COUNT = 120;
    public static final String SUB_EVENT_DUALBAND_HANDOVER_PINGPONG = "DUALBAND_HANDOVER_PINGPONG";
    public static final String SUB_EVENT_DUALBAND_HANDOVER_SNAPSHOP = "DUALBAND_HANDOVER_SNAPSHOP";
    public static final String SUB_EVENT_DUALBAND_HANDOVER_TOO_SLOW = "DUALBAND_HANDOVER_TOO_SLOW";
    public static final String SUB_EVENT_DUALBAND_HANDOVER_TO_BAD_5G = "DUALBAND_HANDOVER_TO_BAD_5G";
    public static final String SUB_EVENT_DUALBAND_HANDOVER_USER_REJECT = "DUALBAND_HANDOVER_USER_REJECT";
    private static final String TAG = "WifiProStatisticsManager";
    public static final int TRUE_INT_VAL = 1;
    private static final short UNKNOWN_RAT_TYPE = (short) 0;
    private static final int VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG = 0;
    private static final int VALUE_WIFI_TO_PDP_AUTO_HANDOVER_MOBILE = 1;
    private static final int VALUE_WIFI_TO_PDP_CANNOT_HANDOVER_MOBILE = 2;
    public static final int WIFI_STATE_CONNECTED = 1;
    public static final int WIFI_STATE_DISCONNECTED = 2;
    private static WifiProStatisticsManager mStatisticsManager;
    private static int printLogLevel;
    private String mBG_AP_SSID;
    private int mBQEROReasonFlag;
    private int mBQERoveInReason;
    private Context mContext;
    private WifiProChrDataBaseManager mDataBaseManager;
    private IGetApRecordCount mGetApRecordCountCallBack;
    private IGetMobileInfoCallBack mGetMobileInfoCallBack;
    private HwWifiCHRStateManagerImpl mHwWifiCHRManagerImpl;
    private boolean mNeedSaveCHRStatistic;
    private WifiProStatisticsRecord mNewestStatRcd;
    private boolean mQoeOrNotInetRoveOutStarted;
    private int mROReason;
    private long mROTime;
    private WifiProRoveOutParaRecord mRoveOutPara;
    private int mRoveoutMobileDataNotifyType;
    private SimpleDateFormat mSimpleDateFmt;
    private Handler mStatHandler;
    private WifiProCHRManager mWiFiCHRMgr;
    private long mWifiConnectStartTime;

    class StatisticsCHRMsgHandler extends Handler {
        private StatisticsCHRMsgHandler(Looper looper) {
            super(looper);
            WifiProStatisticsManager.this.logd("new StatisticsCHRMsgHandler");
        }

        public void handleMessage(Message msg) {
            short wifiproState;
            WifiProStatisticsRecord -get6;
            int mobileData;
            WifiProStatisticsManager wifiProStatisticsManager;
            switch (msg.what) {
                case WifiProStatisticsManager.STAT_MSG_LOAD_DB_RECORD /*100*/:
                    WifiProStatisticsManager.this.logd("ChrDataBaseManager init start.");
                    WifiProStatisticsManager.this.mDataBaseManager = WifiProChrDataBaseManager.getInstance(WifiProStatisticsManager.this.mContext);
                    WifiProStatisticsManager.this.loadStatDBRecord(WifiProStatisticsManager.this.mNewestStatRcd);
                    WifiProStatisticsManager.this.sendStatEmptyMsg(WifiProStatisticsManager.STAT_MSG_CHECK_NEED_UPLOAD_EVENT);
                case WifiProStatisticsManager.STAT_MSG_CHECK_NEED_UPLOAD_EVENT /*101*/:
                    if (WifiProStatisticsManager.this.checkIfNeedUpload(WifiProStatisticsManager.this.mNewestStatRcd)) {
                        WifiProStatisticsManager.this.uploadStatisticsCHREvent(WifiProStatisticsManager.this.mNewestStatRcd);
                    }
                    if (WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState == (short) 0) {
                        WifiProStatisticsManager.this.loge("wifipro state abnormal, try reget it.");
                        wifiproState = WifiProStatisticsManager.this.getWifiproState();
                        if (wifiproState != (short) 0) {
                            WifiProStatisticsManager.this.sendStatMsg(WifiProStatisticsManager.STAT_MSG_WIFIPRO_STATE_CHG, wifiproState, WifiProStatisticsManager.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
                        }
                    }
                    WifiProStatisticsManager.this.sendCheckUploadMsg();
                case WifiProStatisticsManager.STAT_MSG_WIFIPRO_STATE_CHG /*103*/:
                    wifiproState = (short) msg.arg1;
                    if (wifiproState != WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState) {
                        short s;
                        Date currDate = new Date();
                        String currDateStr = WifiProStatisticsManager.this.mSimpleDateFmt.format(currDate);
                        if (wifiproState == WifiProStatisticsManager.WIFI_STATE_DISCONNECTED) {
                            s = WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState;
                            if (r0 == WifiProStatisticsManager.WIFI_STATE_CONNECTED) {
                                long enableTotTime = WifiProStatisticsManager.this.calcTimeInterval(WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproStateUpdateTime, currDate);
                                if (enableTotTime <= -1) {
                                    WifiProStatisticsManager.this.resetStatRecord(WifiProStatisticsManager.this.mNewestStatRcd, "last WifiproStateUpdateTime time record invalid");
                                    return;
                                }
                                enableTotTime /= 1000;
                                -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                                -get6.mEnableTotTime += (int) enableTotTime;
                                WifiProStatisticsManager.this.logd("last state en seconds:" + enableTotTime);
                                -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                                -get6.mWifiproCloseCount = (short) (-get6.mWifiproCloseCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                                WifiProStatisticsManager.this.logd("wifipro old state:" + WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState + ", new state:" + wifiproState + ", date str:" + currDateStr);
                                WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState = wifiproState;
                                WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproStateUpdateTime = currDateStr;
                                WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                                return;
                            }
                        }
                        if (wifiproState == WifiProStatisticsManager.WIFI_STATE_CONNECTED) {
                            s = WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState;
                            if (r0 == WifiProStatisticsManager.WIFI_STATE_DISCONNECTED) {
                                -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                                -get6.mWifiproOpenCount = (short) (-get6.mWifiproOpenCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                            }
                        }
                        WifiProStatisticsManager.this.logd("wifipro old state:" + WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState + ", new state:" + wifiproState + ", date str:" + currDateStr);
                        WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState = wifiproState;
                        WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproStateUpdateTime = currDateStr;
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        return;
                    }
                    WifiProStatisticsManager.this.loge("wifipro state unknow or not changed msg receive:" + wifiproState);
                case WifiProStatisticsManager.STAT_MSG_NO_INET_HANDOVER_COUNT /*104*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNoInetHandoverCount = (short) (-get6.mNoInetHandoverCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = true;
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = WifiProStatisticsManager.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_PORTAL_UNAUTH_COUNT /*105*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mPortalUnauthCount = (short) (-get6.mPortalUnauthCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_WIFI_SCO_COUNT /*106*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mWifiScoCount = (short) (-get6.mWifiScoCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_PORTAL_CODE_PARSE_COUNT /*107*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mPortalCodeParseCount = (short) (-get6.mPortalCodeParseCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_RCVSMS_COUNT /*108*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mRcvSMS_Count = (short) (-get6.mRcvSMS_Count + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_PORTAL_AUTO_LOGIN_COUNT /*109*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mPortalAutoLoginCount = (short) (-get6.mPortalAutoLoginCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_CELL_AUTO_OPEN_COUNT /*110*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mCellAutoOpenCount = (short) (-get6.mCellAutoOpenCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_CELL_AUTO_CLOSE_COUNT /*111*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mCellAutoCloseCount = (short) (-get6.mCellAutoCloseCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_RO_EVENT /*113*/:
                    int roReason = msg.arg1;
                    int bqeRoReason = msg.arg2;
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = true;
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = WifiProStatisticsManager.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStatisticsManager.this.roveOutEventProcess(roReason, bqeRoReason);
                case WifiProStatisticsManager.STAT_MSG_RI_EVENT /*114*/:
                    int riReason = msg.arg1;
                    int bqeRiReason = msg.arg2;
                    if (WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted || WifiProStatisticsManager.WIFI_STATE_DISCONNECTED == riReason) {
                        WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                        WifiProStatisticsManager.this.roveInEventProcess(riReason, bqeRiReason, WifiProStatisticsManager.this.mBQEROReasonFlag, WifiProStatisticsManager.this.mROReason);
                        return;
                    }
                    WifiProStatisticsManager.this.logi("Ignore duplicate qoe RI reason:" + riReason);
                case WifiProStatisticsManager.STAT_MSG_BQE_RO_PARA_UPDATE /*115*/:
                    if (WifiProStatisticsManager.this.mRoveOutPara != null) {
                        WifiProRoveOutParaRecord -get10 = WifiProStatisticsManager.this.mRoveOutPara;
                        -get10.mMobileSignalLevel = (short) WifiProStatisticsManager.this.getMobileSignalLevel();
                        -get10 = WifiProStatisticsManager.this.mRoveOutPara;
                        -get10.mRATType = (short) WifiProStatisticsManager.this.getMobileRATType();
                    }
                case WifiProStatisticsManager.STAT_MSG_HISTORY_SCORE_RI_COUNT /*116*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mHisScoRI_Count = (short) (-get6.mHisScoRI_Count + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_OOB_INIT_STATE /*117*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mWifiOobInitState = (short) msg.arg1;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_ALARM_COUNT /*118*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNoInetAlarmCount = (short) (-get6.mNoInetAlarmCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    if (WifiProStatisticsManager.WIFI_STATE_CONNECTED == msg.arg1) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mNoInetAlarmOnConnCnt = (short) (-get6.mNoInetAlarmOnConnCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    }
                case WifiProStatisticsManager.STAT_MSG_USER_USE_BG_SCAN_COUNT /*119*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mUserUseBgScanAPCount = (short) (-get6.mUserUseBgScanAPCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_WIFI_TO_WIFI_SUCC_COUNT /*120*/:
                    int handOverType = (short) msg.arg1;
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mWifiToWifiSuccCount = (short) (-get6.mWifiToWifiSuccCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    if (WifiProStatisticsManager.WIFI_STATE_CONNECTED == handOverType) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mNotInetWifiToWifiCount = (short) (-get6.mNotInetWifiToWifiCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    }
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_SELECT_NOT_INET_AP_COUNT /*121*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mSelectNotInetAPCount = (short) (-get6.mSelectNotInetAPCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT /*122*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNotAutoConnPortalCnt = (short) (-get6.mNotAutoConnPortalCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_HIGH_DATA_RATE_STOP_ROC /*123*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mHighDataRateStopROC = (short) (-get6.mHighDataRateStopROC + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_PING_PONG_COUNT /*124*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mPingPongCount = (short) (-get6.mPingPongCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    int passTime = (int) ((SystemClock.elapsedRealtime() - WifiProStatisticsManager.this.mROTime) / 1000);
                    if (passTime == WifiProStatisticsManager.WIFI_STATE_CONNECTED) {
                        passTime = WifiProStatisticsManager.WIFI_STATE_DISCONNECTED;
                    }
                    if (passTime > WifiProStatisticsManager.MAX_SHORT_TYPE_VALUE) {
                        passTime = WifiProStatisticsManager.MAX_SHORT_TYPE_VALUE;
                    }
                    short s2 = (short) passTime;
                    WifiProStatisticsManager.this.sendPingpongCHREvent(WifiProStatisticsManager.this.mRoveOutPara, r0);
                case WifiProStatisticsManager.STAT_MSG_BQE_BAD_SETTING_CANCEL /*125*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mBQE_BadSettingCancel = (short) (-get6.mBQE_BadSettingCancel + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_SETTING_CANCEL /*126*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNotInetSettingCancel = (short) (-get6.mNotInetSettingCancel + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_USER_CANCEL /*127*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNotInetUserCancel = (short) (-get6.mNotInetUserCancel + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_NET_RESTORE_RI /*128*/:
                    if (WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted) {
                        WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mNotInetRestoreRI = (short) (-get6.mNotInetRestoreRI + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mNotInet_AutoRI_TotData += WifiProStatisticsManager.this.getRoMobileData();
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        return;
                    }
                    WifiProStatisticsManager.this.logd("Ignore duplicate not inet restore RI event.");
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_USER_MANUAL_RI /*129*/:
                    if (WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted) {
                        int notInetRoDataKB;
                        WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mNotInetUserManualRI = (short) (-get6.mNotInetUserManualRI + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        if (WifiProStatisticsManager.this.mGetMobileInfoCallBack != null) {
                            notInetRoDataKB = WifiProStatisticsManager.this.mGetMobileInfoCallBack.getTotalRoMobileData();
                        } else {
                            notInetRoDataKB = WifiProStatisticsManager.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                        }
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mTotBtnRICount = (short) (-get6.mTotBtnRICount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mRO_TotMobileData += notInetRoDataKB;
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        return;
                    }
                    WifiProStatisticsManager.this.logd("Ignore duplicate not inet user manual RI event.");
                case WifiProStatisticsManager.STAT_MSG_SCREEN_ON /*130*/:
                    WifiProStatisticsManager.this.checkMsgLoopRunning();
                case WifiProStatisticsManager.STAT_MSG_USER_REOPEN_WIFI_RI_CNT /*131*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mReopenWifiRICount = (short) (-get6.mReopenWifiRICount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_SEL_CSP_SETTING_CHG_CNT /*132*/:
                    int cspVal = msg.arg1;
                    if (cspVal == 0) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mSelCSPShowDiglogCount = (short) (-get6.mSelCSPShowDiglogCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    } else if (WifiProStatisticsManager.WIFI_STATE_CONNECTED == cspVal) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mSelCSPAutoSwCount = (short) (-get6.mSelCSPAutoSwCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    } else if (WifiProStatisticsManager.WIFI_STATE_DISCONNECTED == cspVal) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mSelCSPNotSwCount = (short) (-get6.mSelCSPNotSwCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    }
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_HMD_NOTIFY_CNT /*133*/:
                    int notifyType = msg.arg1;
                    if (WifiProStatisticsManager.WIFI_STATE_CONNECTED == notifyType) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mBMD_TenMNotifyCount = (short) (-get6.mBMD_TenMNotifyCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = WifiProStatisticsManager.WIFI_STATE_CONNECTED;
                    } else if (WifiProStatisticsManager.WIFI_STATE_DISCONNECTED == notifyType) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mBMD_FiftyMNotifyCount = (short) (-get6.mBMD_FiftyMNotifyCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = WifiProStatisticsManager.WIFI_STATE_DISCONNECTED;
                    }
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_HMD_USER_DEL_NOTIFY_CNT /*134*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mBMD_UserDelNotifyCount = (short) (-get6.mBMD_UserDelNotifyCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_AF_CHR_UPDATE /*135*/:
                    int state = msg.arg1;
                    int type = msg.arg2;
                    if (type == 0) {
                        if (WifiProStatisticsManager.WIFI_STATE_CONNECTED == state) {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_PhoneNumSuccCnt = (short) (-get6.mAF_PhoneNumSuccCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        } else {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_PhoneNumFailCnt = (short) (-get6.mAF_PhoneNumFailCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        }
                    } else if (WifiProStatisticsManager.WIFI_STATE_CONNECTED == type) {
                        if (WifiProStatisticsManager.WIFI_STATE_CONNECTED == state) {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_PasswordSuccCnt = (short) (-get6.mAF_PasswordSuccCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        } else {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_PasswordFailCnt = (short) (-get6.mAF_PasswordFailCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        }
                    } else if (WifiProStatisticsManager.WIFI_STATE_DISCONNECTED == type) {
                        if (WifiProStatisticsManager.WIFI_STATE_CONNECTED == state) {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_AutoLoginSuccCnt = (short) (-get6.mAF_AutoLoginSuccCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        } else {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_AutoLoginFailCnt = (short) (-get6.mAF_AutoLoginFailCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        }
                    } else if (WifiProStatisticsManager.RI_REASON_DATA_SERVICE_CLOSE == type) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mAF_FPNSuccNotMsmCnt = (short) (-get6.mAF_FPNSuccNotMsmCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    }
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                case WifiProStatisticsManager.STAT_MSG_BACK_GRADING_CHR_UPDATE /*136*/:
                    int bgChrType = msg.arg1;
                    WifiProStatisticsManager.this.updateBGChrProcess(bgChrType);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                case WifiProStatisticsManager.STAT_MSG_BQE_GRADING_SVC_CHR_UPDATE /*137*/:
                    int bqeSvcChrType = msg.arg1;
                    WifiProStatisticsManager.this.updateBqeSvcChrProcess(bqeSvcChrType);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                case WifiProStatisticsManager.STAT_MSG_BQE_BAD_RO_DISCONNECT_MOBILE_DATA /*138*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mQOE_RO_DISCONNECT_Cnt = (short) (-get6.mQOE_RO_DISCONNECT_Cnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    mobileData = WifiProStatisticsManager.this.getRoMobileData();
                    WifiProStatisticsManager.this.logd("qoe ro disconnect mobileData=" + mobileData);
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mQOE_RO_DISCONNECT_TotData += mobileData;
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = WifiProStatisticsManager.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_RO_DISCONNECT_MOBILE_DATA /*139*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNotInetRO_DISCONNECT_Cnt = (short) (-get6.mNotInetRO_DISCONNECT_Cnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    mobileData = WifiProStatisticsManager.this.getRoMobileData();
                    WifiProStatisticsManager.this.logd("inet ro disconnect mobileData=" + mobileData);
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNotInetRO_DISCONNECT_TotData += mobileData;
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = WifiProStatisticsManager.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_UPDATE_WIFI_CONNECTION_STATE /*140*/:
                    int wifiState = msg.arg1;
                    if (WifiProStatisticsManager.WIFI_STATE_CONNECTED == wifiState) {
                        if (WifiProStatisticsManager.this.mWifiConnectStartTime == 0) {
                            WifiProStatisticsManager.this.mWifiConnectStartTime = SystemClock.elapsedRealtime();
                            WifiProStatisticsManager.this.logd("wifi connect start here.");
                            return;
                        }
                        WifiProStatisticsManager.this.logd("wifi already connected.");
                    } else if (WifiProStatisticsManager.WIFI_STATE_DISCONNECTED == wifiState) {
                        if (WifiProStatisticsManager.this.mWifiConnectStartTime != 0) {
                            long connectionTime = SystemClock.elapsedRealtime() - WifiProStatisticsManager.this.mWifiConnectStartTime;
                            if (connectionTime > 0 && connectionTime < WifiProStatisticsManager.MAX_COMMERCIAL_USER_UPLOAD_PERIOD) {
                                long acctime = connectionTime / 1000;
                                -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                                -get6.mTotWifiConnectTime = (int) (((long) -get6.mTotWifiConnectTime) + acctime);
                                wifiProStatisticsManager = WifiProStatisticsManager.this;
                                StringBuilder append = new StringBuilder().append("acc wifi connection time:");
                                r0.logd(r30.append(acctime).append(" s, total").append(WifiProStatisticsManager.this.mNewestStatRcd.mTotWifiConnectTime).toString());
                            }
                            WifiProStatisticsManager.this.logd("wifi connected end.");
                            WifiProStatisticsManager.this.mWifiConnectStartTime = 0;
                        }
                    }
                case WifiProStatisticsManager.STAT_MSG_PORTAL_AP_NOT_AUTO_CONN /*141*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mPortalNoAutoConnCnt = (short) (-get6.mPortalNoAutoConnCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                case WifiProStatisticsManager.STAT_MSG_HOME_AP_ADD_RO_PERIOD_CNT /*142*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mHomeAPAddRoPeriodCnt = (short) (-get6.mHomeAPAddRoPeriodCnt + msg.arg1);
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mHomeAPQoeBadCnt = (short) (-get6.mHomeAPQoeBadCnt + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                case WifiProStatisticsManager.STAT_MSG_UPDATE_SATTISTIC_TO_DB /*144*/:
                    if (WifiProStatisticsManager.this.mNeedSaveCHRStatistic) {
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        WifiProStatisticsManager.this.mNeedSaveCHRStatistic = false;
                    }
                case WifiProStatisticsManager.STAT_MSG_ACTIVE_CHECK_RS_DIFF /*145*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mActiveCheckRS_Diff = (short) (-get6.mActiveCheckRS_Diff + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    wifiProStatisticsManager = WifiProStatisticsManager.this;
                    r0.mWiFiCHRMgr.updateBG_AC_DiffType(msg.arg1);
                    WifiProStatisticsManager.this.mWiFiCHRMgr.updateSSID(WifiProStatisticsManager.this.mBG_AP_SSID);
                    WifiProStatisticsManager.this.mWiFiCHRMgr.updateWifiException(WifiProStatisticsManager.STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "BG_AC_RS_DIFF");
                case WifiProStatisticsManager.STAT_MSG_INCREASE_PORTAL_CONN_COUNT /*146*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mTotalPortalConnCount = (short) (-get6.mTotalPortalConnCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_PORTAL_AUTH_SUCC_COUNT /*147*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mTotalPortalAuthSuccCount = (short) (-get6.mTotalPortalAuthSuccCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_CONN_BLOCK_PORTAL_COUNT /*148*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mManualConnBlockPortalCount = (short) (-get6.mManualConnBlockPortalCount + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_AC_RS_SAME_COUNT /*149*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mActiveCheckRS_Same = (short) (-get6.mActiveCheckRS_Same + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_HMD_BTN_RI_COUNT /*150*/:
                    if (WifiProStatisticsManager.WIFI_STATE_DISCONNECTED == WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mBMD_FiftyM_RI_Count = (short) (-get6.mBMD_FiftyM_RI_Count + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                    } else {
                        if (WifiProStatisticsManager.WIFI_STATE_CONNECTED == WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType) {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mBMD_TenM_RI_Count = (short) (-get6.mBMD_TenM_RI_Count + WifiProStatisticsManager.WIFI_STATE_CONNECTED);
                        }
                    }
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = WifiProStatisticsManager.VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                case WifiProStatisticsManager.STAT_MSG_INCREASE_DUALBAND_STATISTIC_COUNT /*151*/:
                    WifiProStatisticsManager.this.increaseDualbandStatisticCountProcess(msg.arg1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                default:
                    WifiProStatisticsManager.this.loge("statistics manager got unknow message.");
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.WifiProStatisticsManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.WifiProStatisticsManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.WifiProStatisticsManager.<clinit>():void");
    }

    private WifiProStatisticsManager(Context context) {
        this.mHwWifiCHRManagerImpl = null;
        this.mRoveOutPara = null;
        this.mGetMobileInfoCallBack = null;
        this.mBQEROReasonFlag = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mROReason = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mROTime = 0;
        this.mBQERoveInReason = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        this.mWifiConnectStartTime = 0;
        this.mNeedSaveCHRStatistic = false;
        this.mGetApRecordCountCallBack = null;
        this.mQoeOrNotInetRoveOutStarted = false;
        this.mRoveoutMobileDataNotifyType = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        logd("WifiProStatisticsManager enter.");
        this.mContext = context;
        initStatHandler();
        this.mWiFiCHRMgr = WifiProCHRManager.getInstance();
        this.mNewestStatRcd = new WifiProStatisticsRecord();
        this.mSimpleDateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sendStatEmptyMsg(STAT_MSG_LOAD_DB_RECORD);
        this.mHwWifiCHRManagerImpl = (HwWifiCHRStateManagerImpl) HwWifiCHRStateManagerImpl.getDefault();
    }

    public static void initStatisticsManager(Context context) {
        if (mStatisticsManager == null) {
            mStatisticsManager = new WifiProStatisticsManager(context);
        }
    }

    public static WifiProStatisticsManager getInstance() {
        if (mStatisticsManager == null) {
            mStatisticsManager = new WifiProStatisticsManager(null);
        }
        return mStatisticsManager;
    }

    private boolean checkDateValid(String dateStr) {
        Date objDate = null;
        if (this.mSimpleDateFmt == null || dateStr == null) {
            return false;
        }
        try {
            objDate = this.mSimpleDateFmt.parse(dateStr);
        } catch (Exception e) {
            loge("checkDateValid date string invalid:" + dateStr);
        }
        if (objDate == null) {
            return false;
        }
        logd("date string valid: " + dateStr);
        return true;
    }

    private void loadStatDBRecord(WifiProStatisticsRecord statRecord) {
        if (statRecord == null || this.mDataBaseManager == null || this.mStatHandler == null) {
            loge("loadStatDBRecord null error.");
        } else if (this.mDataBaseManager.queryChrStatRcd(statRecord) && this.mNewestStatRcd.mLastStatUploadTime.equals(WifiProStatisticsRecord.WIFIPRO_DEAULT_STR)) {
            logi("Not record in database now.");
            resetStatRecord(statRecord, "new phone, save first record.");
        } else if (checkDateValid(statRecord.mLastStatUploadTime) && checkDateValid(statRecord.mLastWifiproStateUpdateTime)) {
            logd("get record from database success.");
        } else {
            resetStatRecord(statRecord, "date invalid, save new record.");
        }
    }

    private long calcTimeInterval(String oldTimeStr, Date nowDate) {
        Date oldDate = null;
        if (this.mSimpleDateFmt == null || oldTimeStr == null || nowDate == null) {
            return -1;
        }
        try {
            oldDate = this.mSimpleDateFmt.parse(oldTimeStr);
        } catch (Exception ex) {
            loge("There has exception in Date parse" + ex);
        }
        if (oldDate == null) {
            return -1;
        }
        return nowDate.getTime() - oldDate.getTime();
    }

    private boolean checkIfNeedUpload(WifiProStatisticsRecord statRecord) {
        if (statRecord == null || this.mDataBaseManager == null || this.mWiFiCHRMgr == null) {
            loge("checkIfNeedUpload null error.");
            return false;
        }
        Date currDate = new Date();
        long statIntervalMinutes = calcTimeInterval(statRecord.mLastStatUploadTime, currDate);
        if (statIntervalMinutes <= -1) {
            resetStatRecord(statRecord, "checkIfNeedUpload LastStatUploadTime time record invalid");
            return false;
        }
        long intervalTime = COMMERCIAL_USER_UPLOAD_PERIOD;
        long maxItvlTime = MAX_COMMERCIAL_USER_UPLOAD_PERIOD;
        if (DEBUG_MODE) {
            intervalTime = DEBUG_MODE_UPLOAD_PERIOD;
        } else if (!WifiProCHRManager.isCommercialUser()) {
            intervalTime = BETA_USER_UPLOAD_PERIOD;
            maxItvlTime = MAX_BETA_USER_UPLOAD_PERIOD;
        }
        if (statIntervalMinutes >= maxItvlTime) {
            logd("checkIfNeedUpload too big upload interval , reset start time.");
            statRecord.mLastStatUploadTime = this.mSimpleDateFmt.format(currDate);
            this.mDataBaseManager.addOrUpdateChrStatRcd(statRecord);
        }
        if (statIntervalMinutes >= intervalTime) {
            logd("checkIfNeedUpload ret true.");
            return true;
        }
        logd("checkIfNeedUpload time left(mins):" + ((intervalTime - statIntervalMinutes) / 60000));
        return false;
    }

    private void resetStatRecord(WifiProStatisticsRecord statRecord, String reasonStr) {
        logi("ChrStatLog resetStatRecord enter, reason:" + reasonStr);
        if (statRecord == null || this.mDataBaseManager == null) {
            loge("resetStatRecord null error.");
            return;
        }
        String currDateStr = this.mSimpleDateFmt.format(new Date());
        statRecord.resetRecord();
        statRecord.mLastStatUploadTime = currDateStr;
        statRecord.mLastWifiproState = getWifiproState();
        statRecord.mLastWifiproStateUpdateTime = currDateStr;
        this.mDataBaseManager.addOrUpdateChrStatRcd(statRecord);
    }

    private boolean uploadStatisticsCHREvent(WifiProStatisticsRecord statRecord) {
        logd("uploadStatisticsCHREvent enter.");
        if (statRecord == null || this.mDataBaseManager == null || this.mSimpleDateFmt == null || this.mWiFiCHRMgr == null) {
            loge("uploadStatisticsCHREvent null error.");
            return false;
        } else if (this.mNewestStatRcd.mLastStatUploadTime == null || this.mNewestStatRcd.mLastWifiproStateUpdateTime == null) {
            loge("last upload time error, give up upload.");
            resetStatRecord(statRecord, "time record null");
            return false;
        } else {
            Date currDate = new Date();
            String currDateStr = this.mSimpleDateFmt.format(currDate);
            long statIntervalMinutes = calcTimeInterval(this.mNewestStatRcd.mLastStatUploadTime, currDate);
            if (statIntervalMinutes <= -1) {
                resetStatRecord(statRecord, "LastStatUploadTime time record invalid");
                return false;
            }
            long enableTotTime;
            statIntervalMinutes /= 60000;
            if (statRecord.mLastWifiproState == (short) 1) {
                enableTotTime = calcTimeInterval(this.mNewestStatRcd.mLastWifiproStateUpdateTime, currDate);
                if (enableTotTime <= -1) {
                    resetStatRecord(statRecord, "LastWifiproStateUpdateTime time record invalid");
                    return false;
                }
                enableTotTime /= 1000;
                logd("uploadStatisticsCHREvent currDateStr:" + currDateStr + ", last en minutes:" + (enableTotTime / 60));
                statRecord.mEnableTotTime = (int) (((long) statRecord.mEnableTotTime) + enableTotTime);
            }
            enableTotTime = (long) (statRecord.mEnableTotTime / SECONDS_OF_ONE_MINUTE);
            if (statIntervalMinutes == 0 || statIntervalMinutes > 2147483632 || enableTotTime > 2147483632) {
                resetStatRecord(statRecord, "interval time abnormal data record invalid");
                return false;
            }
            if (statIntervalMinutes < enableTotTime) {
                statIntervalMinutes = enableTotTime;
            }
            if (enableTotTime == 0 && statRecord.mWifiOobInitState == (short) 0) {
                logd("wifipro not enable at all, not upload stat CHR. curr Date:" + currDateStr + ", last upload Date" + this.mNewestStatRcd.mLastStatUploadTime);
            } else {
                statRecord.mHistoryTotWifiConnHour += statRecord.mTotWifiConnectTime;
                int historyTotWifiHours = statRecord.mHistoryTotWifiConnHour / SECONDS_OF_ONE_HOUR;
                if (this.mGetApRecordCountCallBack != null) {
                    this.mGetApRecordCountCallBack.statisticApInfoRecord();
                    statRecord.mTotAPRecordCnt = (short) this.mGetApRecordCountCallBack.getTotRecordCount();
                    statRecord.mTotHomeAPCnt = (short) this.mGetApRecordCountCallBack.getHomeApRecordCount();
                }
                logi("upload stat CHR, curr date:" + currDateStr + ", interval mins:" + statIntervalMinutes + ", tot ap record:" + statRecord.mTotAPRecordCnt + ", tot home ap record:" + statRecord.mTotHomeAPCnt);
                statRecord.mWifiproStateAtReportTime = getWifiproState();
                this.mWiFiCHRMgr.updateStatParaPart1((int) statIntervalMinutes, (int) enableTotTime, statRecord.mNoInetHandoverCount, statRecord.mPortalUnauthCount, statRecord.mWifiScoCount, statRecord.mPortalCodeParseCount, statRecord.mRcvSMS_Count, statRecord.mPortalAutoLoginCount);
                this.mWiFiCHRMgr.updateStatParaPart2(statRecord.mCellAutoOpenCount, statRecord.mCellAutoCloseCount, statRecord.mTotalBQE_BadROC, statRecord.mManualBackROC, statRecord.mRSSI_RO_Tot, statRecord.mRSSI_ErrRO_Tot, statRecord.mOTA_RO_Tot, statRecord.mOTA_ErrRO_Tot, statRecord.mTCP_RO_Tot);
                this.mWiFiCHRMgr.updateStatParaPart3(statRecord.mTCP_ErrRO_Tot, statRecord.mManualRI_TotTime, statRecord.mAutoRI_TotTime, statRecord.mAutoRI_TotCount, statRecord.mRSSI_RestoreRI_Count, statRecord.mRSSI_BetterRI_Count, statRecord.mTimerRI_Count, statRecord.mHisScoRI_Count, statRecord.mUserCancelROC);
                this.mWiFiCHRMgr.updateStatParaPart4(statRecord.mWifiToWifiSuccCount, statRecord.mNoInetAlarmCount, statRecord.mWifiOobInitState, statRecord.mNotAutoConnPortalCnt, statRecord.mHighDataRateStopROC, statRecord.mSelectNotInetAPCount, statRecord.mUserUseBgScanAPCount, statRecord.mPingPongCount);
                this.mWiFiCHRMgr.updateStatParaPart5(statRecord.mBQE_BadSettingCancel, statRecord.mNotInetSettingCancel, statRecord.mNotInetUserCancel, statRecord.mNotInetRestoreRI, statRecord.mNotInetUserManualRI, statRecord.mNotInetWifiToWifiCount, statRecord.mReopenWifiRICount, statRecord.mSelCSPShowDiglogCount, statRecord.mSelCSPAutoSwCount, statRecord.mSelCSPNotSwCount, statRecord.mTotBtnRICount, statRecord.mBMD_TenMNotifyCount);
                this.mWiFiCHRMgr.updateStatParaPart6(statRecord.mBMD_TenM_RI_Count, statRecord.mBMD_FiftyMNotifyCount, statRecord.mBMD_FiftyM_RI_Count, statRecord.mBMD_UserDelNotifyCount, statRecord.mRO_TotMobileData, statRecord.mAF_PhoneNumSuccCnt, statRecord.mAF_PhoneNumFailCnt, statRecord.mAF_PasswordSuccCnt, statRecord.mAF_PasswordFailCnt, statRecord.mAF_AutoLoginSuccCnt, statRecord.mAF_AutoLoginFailCnt);
                this.mWiFiCHRMgr.updateStatParaPart7(statRecord.mBG_BgRunCnt, statRecord.mBG_SettingRunCnt, statRecord.mBG_FreeInetOkApCnt, statRecord.mBG_FishingApCnt, statRecord.mBG_FreeNotInetApCnt, statRecord.mBG_PortalApCnt, statRecord.mBG_FailedCnt, statRecord.mBG_InetNotOkActiveOk, statRecord.mBG_InetOkActiveNotOk);
                this.mWiFiCHRMgr.updateStatParaPart8(statRecord.mBG_UserSelApFishingCnt, statRecord.mBG_ConntTimeoutCnt, statRecord.mBG_DNSFailCnt, statRecord.mBG_DHCPFailCnt, statRecord.mBG_AUTH_FailCnt, statRecord.mBG_AssocRejectCnt, statRecord.mBG_UserSelFreeInetOkCnt, statRecord.mBG_UserSelNoInetCnt, statRecord.mBG_UserSelPortalCnt);
                this.mWiFiCHRMgr.updateStatParaPart9(statRecord.mBG_FoundTwoMoreApCnt, statRecord.mAF_FPNSuccNotMsmCnt, statRecord.mBSG_RsGoodCnt, statRecord.mBSG_RsMidCnt, statRecord.mBSG_RsBadCnt, statRecord.mBSG_EndIn4sCnt, statRecord.mBSG_EndIn4s7sCnt, statRecord.mBSG_NotEndIn7sCnt);
                this.mWiFiCHRMgr.updateStatParaPart10(statRecord.mBG_NCByConnectFail, statRecord.mBG_NCByCheckFail, statRecord.mBG_NCByStateErr, statRecord.mBG_NCByUnknown, statRecord.mBQE_CNUrl1FailCount, statRecord.mBQE_CNUrl2FailCount, statRecord.mBQE_CNUrl3FailCount, statRecord.mBQE_NCNUrl1FailCount, statRecord.mBQE_NCNUrl2FailCount, statRecord.mBQE_NCNUrl3FailCount);
                this.mWiFiCHRMgr.updateStatParaPart11(statRecord.mBQE_ScoreUnknownCount, statRecord.mBQE_BindWlanFailCount, statRecord.mBQE_StopBqeFailCount, statRecord.mQOE_AutoRI_TotData, statRecord.mNotInet_AutoRI_TotData, statRecord.mQOE_RO_DISCONNECT_Cnt, statRecord.mQOE_RO_DISCONNECT_TotData, statRecord.mNotInetRO_DISCONNECT_Cnt, statRecord.mNotInetRO_DISCONNECT_TotData, statRecord.mTotWifiConnectTime);
                this.mWiFiCHRMgr.updateStatParaPart12(statRecord.mActiveCheckRS_Diff, statRecord.mNoInetAlarmOnConnCnt, statRecord.mPortalNoAutoConnCnt, statRecord.mHomeAPAddRoPeriodCnt, statRecord.mHomeAPQoeBadCnt, historyTotWifiHours, statRecord.mTotAPRecordCnt, statRecord.mTotHomeAPCnt, statRecord.mBigRTT_RO_Tot, statRecord.mBigRTT_ErrRO_Tot);
                this.mWiFiCHRMgr.updateStatParaPart13(statRecord.mTotalPortalConnCount, statRecord.mTotalPortalAuthSuccCount, statRecord.mManualConnBlockPortalCount, statRecord.mWifiproStateAtReportTime, statRecord.mWifiproOpenCount, statRecord.mWifiproCloseCount, statRecord.mActiveCheckRS_Same);
                if (this.mHwWifiCHRManagerImpl != null) {
                    this.mHwWifiCHRManagerImpl.setDualbandParameter(statRecord);
                }
                this.mWiFiCHRMgr.updateWifiException(STAT_MSG_SELECT_NOT_INET_AP_COUNT, "NO_SUB_EVENT");
            }
            int saveOldHistoryConnTime = statRecord.mHistoryTotWifiConnHour;
            resetStatRecord(statRecord, "statistics CHR event upload success, new period start.");
            statRecord.mHistoryTotWifiConnHour = saveOldHistoryConnTime;
            return true;
        }
    }

    private boolean checkInitOk() {
        if (this.mContext != null && this.mNewestStatRcd != null && this.mDataBaseManager != null && this.mSimpleDateFmt != null && this.mStatHandler != null) {
            return true;
        }
        loge("checkInitOk null error.");
        return false;
    }

    public void updateInitialWifiproState(boolean boolState) {
        if (checkInitOk()) {
            logd("updateInitialWifiproState enter.");
            sendStatMsg(STAT_MSG_OOB_INIT_STATE, boolState ? WIFI_STATE_CONNECTED : WIFI_STATE_DISCONNECTED, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void updateWifiproState(boolean boolState) {
        if (checkInitOk()) {
            short state = (short) 2;
            if (boolState) {
                state = (short) 1;
                logi("updateWifiproState rs: enable");
            } else {
                logi("updateWifiproState rs: disable");
            }
            sendStatMsg(STAT_MSG_WIFIPRO_STATE_CHG, state, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    private short getWifiproState() {
        if (!checkInitOk()) {
            return UNKNOWN_RAT_TYPE;
        }
        short state = (short) 2;
        if (WifiProStatusUtils.isWifiProEnabledViaXml(this.mContext)) {
            state = (short) 1;
            logi("getWifiproState rs: enable");
        } else {
            logi("getWifiproState rs: disable");
        }
        return state;
    }

    public void registerMobileInfoCallback(IGetMobileInfoCallBack infoCallback) {
        logd("registerMobileInfoCallback enter.");
        this.mGetMobileInfoCallBack = infoCallback;
    }

    public void registerGetApRecordCountCallBack(IGetApRecordCount callback) {
        logd("registerGetApRecordCountCallBack enter.");
        this.mGetApRecordCountCallBack = callback;
    }

    private int getMobileSignalLevel() {
        if (this.mGetMobileInfoCallBack == null) {
            return ERROR_TIME_INTERVAL;
        }
        int mobileSignalLevel = this.mGetMobileInfoCallBack.onGetMobileSignalLevel();
        logd("getMobileSignalLevel new level:" + mobileSignalLevel);
        return mobileSignalLevel;
    }

    private int getMobileRATType() {
        if (this.mGetMobileInfoCallBack == null) {
            return VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
        }
        int mobileRatType = this.mGetMobileInfoCallBack.onGetMobileRATType();
        logd("getMobileRATType new type:" + mobileRatType);
        return mobileRatType;
    }

    public void setBQERoveOutReason(boolean rssiTcpBad, boolean otaTcpBad, boolean tcpBad, boolean bigRtt, WifiProRoveOutParaRecord roveOutPara) {
        if (checkInitOk()) {
            this.mBQEROReasonFlag = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            if (rssiTcpBad) {
                this.mBQEROReasonFlag |= WIFI_STATE_CONNECTED;
            }
            if (otaTcpBad) {
                this.mBQEROReasonFlag |= WIFI_STATE_DISCONNECTED;
            }
            if (tcpBad) {
                this.mBQEROReasonFlag |= RO_REASON_TCP_BAD;
            }
            if (bigRtt) {
                this.mBQEROReasonFlag |= RO_REASON_BIG_RTT;
            }
            logi("setBQERoveOutReason enter, reason:" + this.mBQEROReasonFlag);
            this.mRoveOutPara = roveOutPara;
            sendStatEmptyMsg(STAT_MSG_BQE_RO_PARA_UPDATE);
        }
    }

    public void sendWifiproRoveOutEvent(int reason) {
        if (checkInitOk()) {
            logi("sendWifiproRoveOutEvent enter, reason:" + reason);
            sendStatMsg(STAT_MSG_RO_EVENT, reason, this.mBQEROReasonFlag);
        }
    }

    public void setBQERoveInReason(int roveInReason) {
        if (checkInitOk()) {
            logi("setBQERoveInReason enter, reason:" + roveInReason);
            this.mBQERoveInReason = roveInReason;
        }
    }

    public void sendWifiproRoveInEvent(int riReason) {
        if (checkInitOk()) {
            logi("sendWifiproRoveInEvent enter, reason:" + riReason);
            sendStatMsg(STAT_MSG_RI_EVENT, riReason, this.mBQERoveInReason);
        }
    }

    public void increaseNoInetHandoverCount() {
        if (checkInitOk()) {
            logd("increaseNoInetHandoverCount enter.");
            sendStatEmptyMsg(STAT_MSG_NO_INET_HANDOVER_COUNT);
        }
    }

    public void increaseWiFiHandoverWiFiCount(int handOverType) {
        if (checkInitOk()) {
            logd("increaseWiFiHandoverWiFiCount enter. handOverType =" + handOverType);
            sendStatMsg(STAT_MSG_WIFI_TO_WIFI_SUCC_COUNT, handOverType, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void increaseUserReopenWifiRiCount() {
        if (checkInitOk()) {
            logd("increaseUserReopenWifiRiCount enter.");
            sendStatEmptyMsg(STAT_MSG_USER_REOPEN_WIFI_RI_CNT);
        }
    }

    public void increaseSelCspSettingChgCount(int newSettingValue) {
        if (checkInitOk()) {
            logd("increaseSelCspSettingChgCount enter. csp val=" + newSettingValue);
            sendStatMsg(STAT_MSG_SEL_CSP_SETTING_CHG_CNT, newSettingValue, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void increaseHMDNotifyCount(int notifyType) {
        if (checkInitOk()) {
            logd("increaseSelCspSettingChgCount enter. notifyType val=" + notifyType);
            sendStatMsg(STAT_MSG_HMD_NOTIFY_CNT, notifyType, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void increaseUserDelNotifyCount() {
        if (checkInitOk()) {
            logd("increaseUserDelNotifyCount enter.");
            sendStatEmptyMsg(STAT_MSG_HMD_USER_DEL_NOTIFY_CNT);
        }
    }

    public void increaseDualbandStatisticCount(int type) {
        if (checkInitOk()) {
            logd("increaseDualbandStatisticCount enter.");
            sendStatMsg(STAT_MSG_INCREASE_DUALBAND_STATISTIC_COUNT, type, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void uploadWifiProDualbandExceptionEvent(String dualbandSubExEvent, WifiProDualbandExceptionRecord rec) {
        if (!(dualbandSubExEvent == null || this.mHwWifiCHRManagerImpl == null)) {
            this.mHwWifiCHRManagerImpl.setWifiProDualbandExceptionRecord(rec);
            this.mHwWifiCHRManagerImpl.updateWifiExceptionByWifipro(STAT_MSG_BQE_BAD_SETTING_CANCEL, dualbandSubExEvent);
        }
    }

    public void uploadWifiProDualbandApInfoEvent(int dualbandApType, String bssid2G, String bssid5G) {
        if (this.mHwWifiCHRManagerImpl != null) {
            this.mHwWifiCHRManagerImpl.setWifiProDualbandApInfo(dualbandApType, bssid2G, bssid5G);
            this.mHwWifiCHRManagerImpl.updateWifiExceptionByWifipro(STAT_MSG_NOT_INET_SETTING_CANCEL, "");
        }
    }

    public void increaseHighMobileDataBtnRiCount() {
        if (checkInitOk()) {
            logd("increaseHighMobileDataBtnRiCount enter.");
            sendStatEmptyMsg(STAT_MSG_INCREASE_HMD_BTN_RI_COUNT);
        }
    }

    public void uploadPortalAutoFillStatus(boolean success, int type) {
        if (checkInitOk()) {
            int state = success ? WIFI_STATE_CONNECTED : VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            logd("uploadPortalAutoFillStatus enter. state=" + success + ", type=" + type);
            sendStatMsg(STAT_MSG_AF_CHR_UPDATE, state, type);
        }
    }

    public void increasePortalNoAutoConnCnt() {
        if (checkInitOk()) {
            logd("increasePortalNoAutoConnCnt enter.");
            sendStatEmptyMsg(STAT_MSG_PORTAL_AP_NOT_AUTO_CONN);
        }
    }

    public void increaseHomeAPAddRoPeriodCnt(int periodCount) {
        if (checkInitOk()) {
            logd("increaseHomeAPAddRoPeriodCnt enter. periodCount=" + periodCount);
            sendStatMsg(STAT_MSG_HOME_AP_ADD_RO_PERIOD_CNT, periodCount, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void updateStatisticToDB() {
        if (checkInitOk()) {
            logd("updateStatisticToDB enter.");
            if (!this.mStatHandler.hasMessages(STAT_MSG_UPDATE_SATTISTIC_TO_DB)) {
                sendStatEmptyMsg(STAT_MSG_UPDATE_SATTISTIC_TO_DB);
            }
        }
    }

    public void increaseBG_AC_DiffType(int diffType) {
        if (checkInitOk()) {
            logd("increaseBG_AC_DiffType enter. diffType=" + diffType);
            sendStatMsg(STAT_MSG_ACTIVE_CHECK_RS_DIFF, diffType, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void updateBG_AP_SSID(String bgAPSSID) {
        if (bgAPSSID == null) {
            bgAPSSID = "";
        }
        logd("updateBG_AP_SSID enter. BG SSID=" + bgAPSSID);
        this.mBG_AP_SSID = bgAPSSID;
    }

    public void updateBGChrStatistic(int bgChrType) {
        if (checkInitOk()) {
            logd("updateBGChrStatistic enter. notifyType val=" + bgChrType);
            sendStatMsg(STAT_MSG_BACK_GRADING_CHR_UPDATE, bgChrType, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void updateBqeSvcChrStatistic(int bqeSvcChrType) {
        if (checkInitOk()) {
            logd("updateBGChrStatistic enter. notifyType val=" + bqeSvcChrType);
            sendStatMsg(STAT_MSG_BQE_GRADING_SVC_CHR_UPDATE, bqeSvcChrType, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void increaseNoInetRemindCount(boolean isConnAlarm) {
        if (checkInitOk()) {
            int i;
            logd("increaseNoInetRemindCount enter. isConnAlarm = " + isConnAlarm);
            if (isConnAlarm) {
                i = WIFI_STATE_CONNECTED;
            } else {
                i = VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
            }
            sendStatMsg(STAT_MSG_NOT_INET_ALARM_COUNT, i, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void increaseUserUseBgScanAPCount() {
        if (checkInitOk()) {
            logd("increaseUserUseBgScanAPCount enter.");
            sendStatEmptyMsg(STAT_MSG_USER_USE_BG_SCAN_COUNT);
        }
    }

    public void increasePingPongCount() {
        if (checkInitOk()) {
            logd("increasePingPongCount enter.");
            sendStatEmptyMsg(STAT_MSG_PING_PONG_COUNT);
        }
    }

    public void increaseBQE_BadSettingCancelCount() {
        if (checkInitOk()) {
            logd("increaseBQE_BadSettingCancelCount enter.");
            sendStatEmptyMsg(STAT_MSG_BQE_BAD_SETTING_CANCEL);
        }
    }

    public void increaseNotInetSettingCancelCount() {
        if (checkInitOk()) {
            logd("increaseNotInetSettingCancelCount enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_INET_SETTING_CANCEL);
        }
    }

    public void increaseNotInetUserCancelCount() {
        if (checkInitOk()) {
            logd("increaseNotInetUserCancelCount enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_INET_USER_CANCEL);
        }
    }

    public void increaseNotInetRestoreRICount() {
        if (checkInitOk()) {
            logd("increaseNotInetRestoreRICount enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_INET_NET_RESTORE_RI);
        }
    }

    public void increaseNotInetUserManualRICount() {
        if (checkInitOk()) {
            logd("increaseNotInetUserManualRICount enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_INET_USER_MANUAL_RI);
        }
    }

    public void increaseSelectNotInetAPCount() {
        if (checkInitOk()) {
            logd("increaseSelectNotInetAPCount enter.");
            sendStatEmptyMsg(STAT_MSG_SELECT_NOT_INET_AP_COUNT);
        }
    }

    public void increaseNotAutoConnPortalCnt() {
        if (checkInitOk()) {
            logd("increaseNotAutoConnPortalCnt enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT);
        }
    }

    public void increasePortalUnauthCount() {
        if (checkInitOk()) {
            logd("increasePortalUnauthCount enter.");
            sendStatEmptyMsg(STAT_MSG_PORTAL_UNAUTH_COUNT);
        }
    }

    public void increaseWifiScoCount() {
        if (checkInitOk()) {
            logd("increaseWifiScoCount enter.");
            sendStatEmptyMsg(STAT_MSG_WIFI_SCO_COUNT);
        }
    }

    public void increasePortalCodeParseCount() {
        if (checkInitOk()) {
            logd("increasePortalCodeParseCount enter.");
            sendStatEmptyMsg(STAT_MSG_PORTAL_CODE_PARSE_COUNT);
        }
    }

    public void increaseRcvSMS_Count() {
        if (checkInitOk()) {
            logd("increaseRcvSMS_Count enter.");
            sendStatEmptyMsg(STAT_MSG_RCVSMS_COUNT);
        }
    }

    public void increasePortalAutoLoginCount() {
        if (checkInitOk()) {
            logd("increasePortalAutoLoginCount enter.");
            sendStatEmptyMsg(STAT_MSG_PORTAL_AUTO_LOGIN_COUNT);
        }
    }

    public void increaseAutoOpenCount() {
        if (checkInitOk()) {
            logd("increaseCellAutoOpenCount enter.");
            sendStatEmptyMsg(STAT_MSG_CELL_AUTO_OPEN_COUNT);
        }
    }

    public void increaseAutoCloseCount() {
        if (checkInitOk()) {
            logd("increaseCellAutoCloseCount enter.");
            sendStatEmptyMsg(STAT_MSG_CELL_AUTO_CLOSE_COUNT);
        }
    }

    public void increaseHighDataRateStopROC() {
        if (checkInitOk()) {
            logd("increaseHighDataRateStopROC enter.");
            sendStatEmptyMsg(STAT_MSG_HIGH_DATA_RATE_STOP_ROC);
        }
    }

    public void increasePortalConnectedCnt() {
        if (checkInitOk()) {
            logd("increasePortalConnectedCnt enter.");
            sendStatEmptyMsg(STAT_MSG_INCREASE_PORTAL_CONN_COUNT);
        }
    }

    public void increasePortalConnectedAndAuthenCnt() {
        if (checkInitOk()) {
            logd("increasePortalConnectedAndAuthenCnt enter.");
            sendStatEmptyMsg(STAT_MSG_INCREASE_PORTAL_AUTH_SUCC_COUNT);
        }
    }

    public void increasePortalRefusedButUserTouchCnt() {
        if (checkInitOk()) {
            logd("increasePortalRefusedButUserTouchCnt enter.");
            sendStatEmptyMsg(STAT_MSG_INCREASE_CONN_BLOCK_PORTAL_COUNT);
        }
    }

    public void increaseActiveCheckRS_Same() {
        if (checkInitOk()) {
            logd("increaseActiveCheckRS_Same enter.");
            sendStatEmptyMsg(STAT_MSG_INCREASE_AC_RS_SAME_COUNT);
        }
    }

    public void accuQOEBadRoDisconnectData() {
        if (checkInitOk()) {
            logd("accuQOERoDisconnectData enter.");
            sendStatEmptyMsg(STAT_MSG_BQE_BAD_RO_DISCONNECT_MOBILE_DATA);
        }
    }

    public void accuNotInetRoDisconnectData() {
        if (checkInitOk()) {
            logd("accuNotInetRoDisconnectData enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_INET_RO_DISCONNECT_MOBILE_DATA);
        }
    }

    public void updateWifiConnectState(int newWifiState) {
        if (checkInitOk()) {
            logd("updateWifiConnectState enter.");
            sendStatMsg(STAT_MSG_UPDATE_WIFI_CONNECTION_STATE, newWifiState, VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG);
        }
    }

    public void sendScreenOnEvent() {
        if (checkInitOk()) {
            logd("sendScreenOnEvent enter.");
            sendStatEmptyMsg(STAT_MSG_SCREEN_ON);
        }
    }

    private void logd(String msg) {
        if (printLogLevel <= WIFI_STATE_CONNECTED) {
            Log.d(TAG, msg);
        }
    }

    private void logi(String msg) {
        if (printLogLevel <= WIFI_STATE_DISCONNECTED) {
            Log.i(TAG, msg);
        }
    }

    private void loge(String msg) {
        if (printLogLevel <= RI_REASON_DATA_SERVICE_CLOSE) {
            Log.e(TAG, msg);
        }
    }

    public void sendStatEmptyMsg(int what) {
        if (this.mStatHandler != null) {
            this.mStatHandler.sendEmptyMessage(what);
        }
    }

    public void sendStatMsg(int what, int arg1, int arg2) {
        if (this.mStatHandler != null) {
            this.mStatHandler.sendMessage(Message.obtain(this.mStatHandler, what, arg1, arg2));
        }
    }

    public void sendStatEmptyMsgDelayed(int what, long delayMillis) {
        if (this.mStatHandler != null) {
            this.mStatHandler.sendEmptyMessageDelayed(what, delayMillis);
        }
    }

    private void roveOutEventProcess(int roReason, int bqeRoReasonFlag) {
        logi("roveOutEventProcess enter, RO reason:" + roReason + ", BQE RO reason:" + bqeRoReasonFlag);
        if (WIFI_STATE_CONNECTED == roReason) {
            WifiProStatisticsRecord wifiProStatisticsRecord;
            if ((bqeRoReasonFlag & WIFI_STATE_CONNECTED) != 0) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRSSI_RO_Tot = (short) (wifiProStatisticsRecord.mRSSI_RO_Tot + WIFI_STATE_CONNECTED);
            }
            if ((bqeRoReasonFlag & WIFI_STATE_DISCONNECTED) != 0) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mOTA_RO_Tot = (short) (wifiProStatisticsRecord.mOTA_RO_Tot + WIFI_STATE_CONNECTED);
            }
            if ((bqeRoReasonFlag & RO_REASON_TCP_BAD) != 0) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mTCP_RO_Tot = (short) (wifiProStatisticsRecord.mTCP_RO_Tot + WIFI_STATE_CONNECTED);
            }
            if ((bqeRoReasonFlag & RO_REASON_BIG_RTT) != 0) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBigRTT_RO_Tot = (short) (wifiProStatisticsRecord.mBigRTT_RO_Tot + WIFI_STATE_CONNECTED);
            }
            wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mTotalBQE_BadROC = (short) (wifiProStatisticsRecord.mTotalBQE_BadROC + WIFI_STATE_CONNECTED);
            this.mDataBaseManager.addOrUpdateChrStatRcd(this.mNewestStatRcd);
        }
        this.mROReason = roReason;
        this.mROTime = SystemClock.elapsedRealtime();
    }

    private int getRoMobileData() {
        return this.mGetMobileInfoCallBack != null ? this.mGetMobileInfoCallBack.getTotalRoMobileData() : VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
    }

    private void roveInEventProcess(int riReason, int bqeRiReasonFlag, int bqeRoReasonFlag, int roReason) {
        long passTime;
        WifiProStatisticsRecord wifiProStatisticsRecord;
        logi("roveInEventProcess enter, RI reason:" + riReason + ", BQE RI reason:" + bqeRiReasonFlag + ", roReasonFlag:" + bqeRoReasonFlag + ", roReason:" + roReason);
        boolean needUpdateDB = false;
        if (WIFI_STATE_DISCONNECTED == riReason) {
            passTime = 1;
            logd("update ro reason by cancel ri event.");
            roReason = WIFI_STATE_CONNECTED;
            roveOutEventProcess(WIFI_STATE_CONNECTED, bqeRoReasonFlag);
        } else if (this.mROTime == 0) {
            passTime = 0;
        } else {
            passTime = (SystemClock.elapsedRealtime() - this.mROTime) / 1000;
            if (passTime == 1) {
                passTime = 2;
            }
        }
        this.mROTime = 0;
        if (WIFI_STATE_CONNECTED == riReason) {
            if (WIFI_STATE_CONNECTED == bqeRiReasonFlag) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRSSI_RestoreRI_Count = (short) (wifiProStatisticsRecord.mRSSI_RestoreRI_Count + WIFI_STATE_CONNECTED);
            } else if (WIFI_STATE_DISCONNECTED == bqeRiReasonFlag) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRSSI_BetterRI_Count = (short) (wifiProStatisticsRecord.mRSSI_BetterRI_Count + WIFI_STATE_CONNECTED);
            } else if (RI_REASON_DATA_SERVICE_CLOSE == bqeRiReasonFlag) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mTimerRI_Count = (short) (wifiProStatisticsRecord.mTimerRI_Count + WIFI_STATE_CONNECTED);
            }
            wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mAutoRI_TotCount = (short) (wifiProStatisticsRecord.mAutoRI_TotCount + WIFI_STATE_CONNECTED);
            wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mAutoRI_TotTime = (int) (((long) wifiProStatisticsRecord.mAutoRI_TotTime) + passTime);
            wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mQOE_AutoRI_TotData += getRoMobileData();
            needUpdateDB = true;
        } else if (RI_REASON_HISTORY_RECORD_RI == riReason) {
            wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mHisScoRI_Count = (short) (wifiProStatisticsRecord.mHisScoRI_Count + WIFI_STATE_CONNECTED);
            needUpdateDB = true;
        }
        boolean isManualRI = (RI_REASON_DATA_SERVICE_CLOSE == riReason || RO_REASON_TCP_BAD == riReason) ? true : RI_REASON_USER_CLOSE_WIFIPRO == riReason;
        boolean isUserCancelRI = WIFI_STATE_DISCONNECTED == riReason;
        if (WIFI_STATE_CONNECTED == roReason) {
            if (isUserCancelRI || isManualRI) {
                logd("abnormal BQE bad rove out statistics process.");
                if ((bqeRoReasonFlag & WIFI_STATE_CONNECTED) != 0) {
                    wifiProStatisticsRecord = this.mNewestStatRcd;
                    wifiProStatisticsRecord.mRSSI_ErrRO_Tot = (short) (wifiProStatisticsRecord.mRSSI_ErrRO_Tot + WIFI_STATE_CONNECTED);
                }
                if ((bqeRoReasonFlag & WIFI_STATE_DISCONNECTED) != 0) {
                    wifiProStatisticsRecord = this.mNewestStatRcd;
                    wifiProStatisticsRecord.mOTA_ErrRO_Tot = (short) (wifiProStatisticsRecord.mOTA_ErrRO_Tot + WIFI_STATE_CONNECTED);
                }
                if ((bqeRoReasonFlag & RO_REASON_TCP_BAD) != 0) {
                    wifiProStatisticsRecord = this.mNewestStatRcd;
                    wifiProStatisticsRecord.mTCP_ErrRO_Tot = (short) (wifiProStatisticsRecord.mTCP_ErrRO_Tot + WIFI_STATE_CONNECTED);
                }
                if ((bqeRoReasonFlag & RO_REASON_BIG_RTT) != 0) {
                    wifiProStatisticsRecord = this.mNewestStatRcd;
                    wifiProStatisticsRecord.mBigRTT_ErrRO_Tot = (short) (wifiProStatisticsRecord.mBigRTT_ErrRO_Tot + WIFI_STATE_CONNECTED);
                }
                if (passTime > 32760) {
                    passTime = 32760;
                }
                sendUnexpectedROParaCHREvent(this.mRoveOutPara, (short) ((int) passTime));
            }
            if (isManualRI) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mManualRI_TotTime = (int) (((long) wifiProStatisticsRecord.mManualRI_TotTime) + passTime);
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mManualBackROC = (short) (wifiProStatisticsRecord.mManualBackROC + WIFI_STATE_CONNECTED);
                int roDataKB = this.mGetMobileInfoCallBack != null ? this.mGetMobileInfoCallBack.getTotalRoMobileData() : VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG;
                if (RO_REASON_TCP_BAD == riReason) {
                    wifiProStatisticsRecord = this.mNewestStatRcd;
                    wifiProStatisticsRecord.mTotBtnRICount = (short) (wifiProStatisticsRecord.mTotBtnRICount + WIFI_STATE_CONNECTED);
                }
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRO_TotMobileData += roDataKB;
            }
            if (isUserCancelRI) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mUserCancelROC = (short) (wifiProStatisticsRecord.mUserCancelROC + WIFI_STATE_CONNECTED);
            }
            needUpdateDB = true;
        }
        if (needUpdateDB) {
            this.mDataBaseManager.addOrUpdateChrStatRcd(this.mNewestStatRcd);
        }
    }

    private void sendUnexpectedROParaCHREvent(WifiProRoveOutParaRecord roveOutPara, short roTime) {
        if (roveOutPara != null) {
            logi("unexpected RO para CHR event send.");
            roveOutPara.mRO_Duration = roTime;
            this.mWiFiCHRMgr.updateExcpRoParaPart1(roveOutPara.mRSSI_VALUE, roveOutPara.mOTA_PacketDropRate, roveOutPara.mRttAvg, roveOutPara.mTcpInSegs, roveOutPara.mTcpOutSegs, roveOutPara.mTcpRetransSegs, roveOutPara.mWIFI_NetSpeed, roveOutPara.mIPQLevel);
            this.mWiFiCHRMgr.updateExcpRoParaPart2(roveOutPara.mRO_APSsid, roveOutPara.mMobileSignalLevel, roveOutPara.mRATType, roveOutPara.mHistoryQuilityRO_Rate, roveOutPara.mHighDataRateRO_Rate, roveOutPara.mCreditScoreRO_Rate, roveOutPara.mRO_Duration);
            this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "ROVE_OUT_PARAMETER");
        }
    }

    private void sendPingpongCHREvent(WifiProRoveOutParaRecord roveOutPara, short roTime) {
        if (roveOutPara != null) {
            logi("Pingpong RO para CHR event send.");
            roveOutPara.mRO_Duration = roTime;
            this.mWiFiCHRMgr.updateExcpRoParaPart1(roveOutPara.mRSSI_VALUE, roveOutPara.mOTA_PacketDropRate, roveOutPara.mRttAvg, roveOutPara.mTcpInSegs, roveOutPara.mTcpOutSegs, roveOutPara.mTcpRetransSegs, roveOutPara.mWIFI_NetSpeed, roveOutPara.mIPQLevel);
            this.mWiFiCHRMgr.updateExcpRoParaPart2(roveOutPara.mRO_APSsid, roveOutPara.mMobileSignalLevel, roveOutPara.mRATType, roveOutPara.mHistoryQuilityRO_Rate, roveOutPara.mHighDataRateRO_Rate, roveOutPara.mCreditScoreRO_Rate, roveOutPara.mRO_Duration);
            this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "SWITCH_PINGPONG");
        }
    }

    private void initStatHandler() {
        HandlerThread thread = new HandlerThread("StatisticsCHRMsgHandler");
        thread.start();
        this.mStatHandler = new StatisticsCHRMsgHandler(thread.getLooper(), null);
    }

    private void sendCheckUploadMsg() {
        if (this.mStatHandler.hasMessages(STAT_MSG_CHECK_NEED_UPLOAD_EVENT)) {
            loge("There has CHECK_NEED_UPLOAD_EVENT msg at queue.");
        } else if (DEBUG_MODE) {
            sendStatEmptyMsgDelayed(STAT_MSG_CHECK_NEED_UPLOAD_EVENT, 60000);
        } else {
            sendStatEmptyMsgDelayed(STAT_MSG_CHECK_NEED_UPLOAD_EVENT, 1800000);
        }
    }

    private void checkMsgLoopRunning() {
        if (this.mStatHandler.hasMessages(STAT_MSG_CHECK_NEED_UPLOAD_EVENT)) {
            logd("msg Loop is running.");
            return;
        }
        logd("restart msg Loop.");
        sendStatEmptyMsg(STAT_MSG_CHECK_NEED_UPLOAD_EVENT);
    }

    private void updateBGChrProcess(int bgChrType) {
        logd("updateBGChrProcess enter for bg type:" + bgChrType);
        WifiProStatisticsRecord wifiProStatisticsRecord;
        switch (bgChrType) {
            case WIFI_STATE_CONNECTED /*1*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_BgRunCnt = (short) (wifiProStatisticsRecord.mBG_BgRunCnt + WIFI_STATE_CONNECTED);
            case WIFI_STATE_DISCONNECTED /*2*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_SettingRunCnt = (short) (wifiProStatisticsRecord.mBG_SettingRunCnt + WIFI_STATE_CONNECTED);
            case RI_REASON_DATA_SERVICE_CLOSE /*3*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_FreeInetOkApCnt = (short) (wifiProStatisticsRecord.mBG_FreeInetOkApCnt + WIFI_STATE_CONNECTED);
            case RO_REASON_TCP_BAD /*4*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_FishingApCnt = (short) (wifiProStatisticsRecord.mBG_FishingApCnt + WIFI_STATE_CONNECTED);
            case RI_REASON_USER_CLOSE_WIFIPRO /*5*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_FreeNotInetApCnt = (short) (wifiProStatisticsRecord.mBG_FreeNotInetApCnt + WIFI_STATE_CONNECTED);
            case RI_REASON_HISTORY_RECORD_RI /*6*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_PortalApCnt = (short) (wifiProStatisticsRecord.mBG_PortalApCnt + WIFI_STATE_CONNECTED);
            case RI_REASON_DATA_SERVICE_POOR_QUILITY /*7*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_UserSelFreeInetOkCnt = (short) (wifiProStatisticsRecord.mBG_UserSelFreeInetOkCnt + WIFI_STATE_CONNECTED);
            case RO_REASON_BIG_RTT /*8*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_UserSelNoInetCnt = (short) (wifiProStatisticsRecord.mBG_UserSelNoInetCnt + WIFI_STATE_CONNECTED);
            case RI_REASON_HMD_50M_USER_BT_RI /*9*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_UserSelPortalCnt = (short) (wifiProStatisticsRecord.mBG_UserSelPortalCnt + WIFI_STATE_CONNECTED);
            case DUALBAND_SINGLE_AP_LOW_FREQ_SCAN_5G_COUNT /*10*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_FoundTwoMoreApCnt = (short) (wifiProStatisticsRecord.mBG_FoundTwoMoreApCnt + WIFI_STATE_CONNECTED);
            case DUALBAND_SINGLE_AP_MID_FREQ_SCAN_5G_COUNT /*11*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_FailedCnt = (short) (wifiProStatisticsRecord.mBG_FailedCnt + WIFI_STATE_CONNECTED);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "BG_FAILED_CNT");
            case DUALBAND_SINGLE_AP_HIGH_FREQ_SCAN_5G_COUNT /*12*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_InetNotOkActiveOk = (short) (wifiProStatisticsRecord.mBG_InetNotOkActiveOk + WIFI_STATE_CONNECTED);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "BG_NOT_INET_ACTIVE_IOK");
            case DUALBAND_MIX_AP_LEARNED_COUNT /*13*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_InetOkActiveNotOk = (short) (wifiProStatisticsRecord.mBG_InetOkActiveNotOk + WIFI_STATE_CONNECTED);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "BG_INET_OK_ACTIVE_NOT_OK");
            case DUALBAND_MIX_AP_NEARBY_COUNT /*14*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_UserSelApFishingCnt = (short) (wifiProStatisticsRecord.mBG_UserSelApFishingCnt + WIFI_STATE_CONNECTED);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "BG_USER_SEL_AP_FISHING_CNT");
            case DUALBAND_MIX_AP_MONITOR_COUNT /*15*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_ConntTimeoutCnt = (short) (wifiProStatisticsRecord.mBG_ConntTimeoutCnt + WIFI_STATE_CONNECTED);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "BG_CONNT_TIMEOUT_CNT");
            case DUALBAND_MIX_AP_SATISFIED_COUNT /*16*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_DNSFailCnt = (short) (wifiProStatisticsRecord.mBG_DNSFailCnt + WIFI_STATE_CONNECTED);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "BG_DNS_FAIL_CNT");
            case DUALBAND_MIX_AP_DISAPPER_COUNT /*17*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_DHCPFailCnt = (short) (wifiProStatisticsRecord.mBG_DHCPFailCnt + WIFI_STATE_CONNECTED);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "BG_DHCP_FAIL_CNT");
            case DUALBAND_MIX_AP_INBLACK_LIST_COUNT /*18*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_AUTH_FailCnt = (short) (wifiProStatisticsRecord.mBG_AUTH_FailCnt + WIFI_STATE_CONNECTED);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "BG_AUTH_FAIL_CNT");
            case DUALBAND_MIX_AP_SCORE_NOTSATISFY_COUNT /*19*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_AssocRejectCnt = (short) (wifiProStatisticsRecord.mBG_AssocRejectCnt + WIFI_STATE_CONNECTED);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT, "BG_ASSOC_REJECT_CNT");
            case DUALBAND_MIX_AP_HANDOVER_SUC_COUNT /*20*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_NCByConnectFail = (short) (wifiProStatisticsRecord.mBG_NCByConnectFail + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_HANDOVER_FAIL_COUNT /*21*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_NCByCheckFail = (short) (wifiProStatisticsRecord.mBG_NCByCheckFail + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_LOW_FREQ_SCAN_5G_COUNT /*22*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_NCByStateErr = (short) (wifiProStatisticsRecord.mBG_NCByStateErr + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_MID_FREQ_SCAN_5G_COUNT /*23*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_NCByUnknown = (short) (wifiProStatisticsRecord.mBG_NCByUnknown + WIFI_STATE_CONNECTED);
            default:
                loge("updateBGChrProcess error type:" + bgChrType);
        }
    }

    private void updateBqeSvcChrProcess(int bqeSvcChrType) {
        logd("updateBqeSvcChrProcess enter for bg type:" + bqeSvcChrType);
        WifiProStatisticsRecord wifiProStatisticsRecord;
        switch (bqeSvcChrType) {
            case WIFI_STATE_CONNECTED /*1*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBSG_RsGoodCnt = (short) (wifiProStatisticsRecord.mBSG_RsGoodCnt + WIFI_STATE_CONNECTED);
            case WIFI_STATE_DISCONNECTED /*2*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBSG_RsMidCnt = (short) (wifiProStatisticsRecord.mBSG_RsMidCnt + WIFI_STATE_CONNECTED);
            case RI_REASON_DATA_SERVICE_CLOSE /*3*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBSG_RsBadCnt = (short) (wifiProStatisticsRecord.mBSG_RsBadCnt + WIFI_STATE_CONNECTED);
            case RO_REASON_TCP_BAD /*4*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBSG_EndIn4sCnt = (short) (wifiProStatisticsRecord.mBSG_EndIn4sCnt + WIFI_STATE_CONNECTED);
            case RI_REASON_USER_CLOSE_WIFIPRO /*5*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBSG_EndIn4s7sCnt = (short) (wifiProStatisticsRecord.mBSG_EndIn4s7sCnt + WIFI_STATE_CONNECTED);
            case RI_REASON_HISTORY_RECORD_RI /*6*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBSG_NotEndIn7sCnt = (short) (wifiProStatisticsRecord.mBSG_NotEndIn7sCnt + WIFI_STATE_CONNECTED);
            case RI_REASON_DATA_SERVICE_POOR_QUILITY /*7*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBQE_CNUrl1FailCount = (short) (wifiProStatisticsRecord.mBQE_CNUrl1FailCount + WIFI_STATE_CONNECTED);
            case RO_REASON_BIG_RTT /*8*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBQE_CNUrl2FailCount = (short) (wifiProStatisticsRecord.mBQE_CNUrl2FailCount + WIFI_STATE_CONNECTED);
            case RI_REASON_HMD_50M_USER_BT_RI /*9*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBQE_CNUrl3FailCount = (short) (wifiProStatisticsRecord.mBQE_CNUrl3FailCount + WIFI_STATE_CONNECTED);
            case DUALBAND_SINGLE_AP_LOW_FREQ_SCAN_5G_COUNT /*10*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBQE_NCNUrl1FailCount = (short) (wifiProStatisticsRecord.mBQE_NCNUrl1FailCount + WIFI_STATE_CONNECTED);
            case DUALBAND_SINGLE_AP_MID_FREQ_SCAN_5G_COUNT /*11*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBQE_NCNUrl2FailCount = (short) (wifiProStatisticsRecord.mBQE_NCNUrl2FailCount + WIFI_STATE_CONNECTED);
            case DUALBAND_SINGLE_AP_HIGH_FREQ_SCAN_5G_COUNT /*12*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBQE_NCNUrl3FailCount = (short) (wifiProStatisticsRecord.mBQE_NCNUrl3FailCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_LEARNED_COUNT /*13*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBQE_ScoreUnknownCount = (short) (wifiProStatisticsRecord.mBQE_ScoreUnknownCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_NEARBY_COUNT /*14*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBQE_BindWlanFailCount = (short) (wifiProStatisticsRecord.mBQE_BindWlanFailCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_MONITOR_COUNT /*15*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBQE_StopBqeFailCount = (short) (wifiProStatisticsRecord.mBQE_StopBqeFailCount + WIFI_STATE_CONNECTED);
            default:
                loge("updateBqeSvcChrProcess error type:" + bqeSvcChrType);
        }
    }

    private void increaseDualbandStatisticCountProcess(int type) {
        logd("increaseDualbandStatisticCountProcess enter for type:" + type);
        WifiProStatisticsRecord wifiProStatisticsRecord;
        switch (type) {
            case WIFI_STATE_CONNECTED /*1*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_LearnedCount = (short) (wifiProStatisticsRecord.mSingleAP_LearnedCount + WIFI_STATE_CONNECTED);
            case WIFI_STATE_DISCONNECTED /*2*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_NearbyCount = (short) (wifiProStatisticsRecord.mSingleAP_NearbyCount + WIFI_STATE_CONNECTED);
            case RI_REASON_DATA_SERVICE_CLOSE /*3*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_MonitorCount = (short) (wifiProStatisticsRecord.mSingleAP_MonitorCount + WIFI_STATE_CONNECTED);
            case RO_REASON_TCP_BAD /*4*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_SatisfiedCount = (short) (wifiProStatisticsRecord.mSingleAP_SatisfiedCount + WIFI_STATE_CONNECTED);
            case RI_REASON_USER_CLOSE_WIFIPRO /*5*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_DisapperCount = (short) (wifiProStatisticsRecord.mSingleAP_DisapperCount + WIFI_STATE_CONNECTED);
            case RI_REASON_HISTORY_RECORD_RI /*6*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_InblacklistCount = (short) (wifiProStatisticsRecord.mSingleAP_InblacklistCount + WIFI_STATE_CONNECTED);
            case RI_REASON_DATA_SERVICE_POOR_QUILITY /*7*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_ScoreNotSatisfyCount = (short) (wifiProStatisticsRecord.mSingleAP_ScoreNotSatisfyCount + WIFI_STATE_CONNECTED);
            case RO_REASON_BIG_RTT /*8*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_HandoverSucCount = (short) (wifiProStatisticsRecord.mSingleAP_HandoverSucCount + WIFI_STATE_CONNECTED);
            case RI_REASON_HMD_50M_USER_BT_RI /*9*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_HandoverFailCount = (short) (wifiProStatisticsRecord.mSingleAP_HandoverFailCount + WIFI_STATE_CONNECTED);
            case DUALBAND_SINGLE_AP_LOW_FREQ_SCAN_5G_COUNT /*10*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_LowFreqScan5GCount = (short) (wifiProStatisticsRecord.mSingleAP_LowFreqScan5GCount + WIFI_STATE_CONNECTED);
            case DUALBAND_SINGLE_AP_MID_FREQ_SCAN_5G_COUNT /*11*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_MidFreqScan5GCount = (short) (wifiProStatisticsRecord.mSingleAP_MidFreqScan5GCount + WIFI_STATE_CONNECTED);
            case DUALBAND_SINGLE_AP_HIGH_FREQ_SCAN_5G_COUNT /*12*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mSingleAP_HighFreqScan5GCount = (short) (wifiProStatisticsRecord.mSingleAP_HighFreqScan5GCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_LEARNED_COUNT /*13*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_LearnedCount = (short) (wifiProStatisticsRecord.mMixedAP_LearnedCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_NEARBY_COUNT /*14*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_NearbyCount = (short) (wifiProStatisticsRecord.mMixedAP_NearbyCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_MONITOR_COUNT /*15*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_MonitorCount = (short) (wifiProStatisticsRecord.mMixedAP_MonitorCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_SATISFIED_COUNT /*16*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_SatisfiedCount = (short) (wifiProStatisticsRecord.mMixedAP_SatisfiedCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_DISAPPER_COUNT /*17*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_DisapperCount = (short) (wifiProStatisticsRecord.mMixedAP_DisapperCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_INBLACK_LIST_COUNT /*18*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_InblacklistCount = (short) (wifiProStatisticsRecord.mMixedAP_InblacklistCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_SCORE_NOTSATISFY_COUNT /*19*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_ScoreNotSatisfyCount = (short) (wifiProStatisticsRecord.mMixedAP_ScoreNotSatisfyCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_HANDOVER_SUC_COUNT /*20*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_HandoverSucCount = (short) (wifiProStatisticsRecord.mMixedAP_HandoverSucCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_HANDOVER_FAIL_COUNT /*21*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_HandoverFailCount = (short) (wifiProStatisticsRecord.mMixedAP_HandoverFailCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_LOW_FREQ_SCAN_5G_COUNT /*22*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_LowFreqScan5GCount = (short) (wifiProStatisticsRecord.mMixedAP_LowFreqScan5GCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_MID_FREQ_SCAN_5G_COUNT /*23*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_MidFreqScan5GCount = (short) (wifiProStatisticsRecord.mMixedAP_MidFreqScan5GCount + WIFI_STATE_CONNECTED);
            case DUALBAND_MIX_AP_HIGH_FREQ_SCAN_5G_COUNT /*24*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mMixedAP_HighFreqScan5GCount = (short) (wifiProStatisticsRecord.mMixedAP_HighFreqScan5GCount + WIFI_STATE_CONNECTED);
            case DUALBAND_CUSTOMIZED_SCAN_SUCC_COUNT /*25*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mCustomizedScan_SuccCount = (short) (wifiProStatisticsRecord.mCustomizedScan_SuccCount + WIFI_STATE_CONNECTED);
            case DUALBAND_CUSTOMIZED_SCAN_FAIL_COUNT /*26*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mCustomizedScan_FailCount = (short) (wifiProStatisticsRecord.mCustomizedScan_FailCount + WIFI_STATE_CONNECTED);
            case DUALBAND_HANDOVER_TO_NOT_INET_5G_COUNT /*27*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mHandoverToNotInet5GCount = (short) (wifiProStatisticsRecord.mHandoverToNotInet5GCount + WIFI_STATE_CONNECTED);
            case DUALBAND_HANDOVER_TOO_SLOW_COUNT /*28*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mHandoverTooSlowCount = (short) (wifiProStatisticsRecord.mHandoverTooSlowCount + WIFI_STATE_CONNECTED);
            case DUALBAND_TO_BAD_5G_COUNT /*29*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mHandoverToBad5GCount = (short) (wifiProStatisticsRecord.mHandoverToBad5GCount + WIFI_STATE_CONNECTED);
            case DUALBAND_USER_REJECT_HANDOVER_COUNT /*30*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mUserRejectHandoverCount = (short) (wifiProStatisticsRecord.mUserRejectHandoverCount + WIFI_STATE_CONNECTED);
            case DUALBAND_HANDOVER_PINGPONG_COUNT /*31*/:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mHandoverPingpongCount = (short) (wifiProStatisticsRecord.mHandoverPingpongCount + WIFI_STATE_CONNECTED);
            default:
                loge("increaseDualbandStatisticCountProcess error type:" + type);
        }
    }
}
