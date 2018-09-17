package com.android.server.wifi.wifipro;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.wifipro.WifiProCHRManager;
import com.android.server.wifipro.WifiProCommonUtils;
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
    public static final int BQE_RI_REASON_RSSI_RESTORE = 1;
    public static final int BQE_RI_REASON_RSSI_RISE_15DB = 2;
    public static final int BQE_RI_REASON_TIMER_TIMEOUT = 3;
    public static final int BQE_RI_REASON_UNKNOWN = 0;
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
    private static final String TAG = "WifiProStatisticsManager";
    public static final int TRUE_INT_VAL = 1;
    private static final short UNKNOWN_RAT_TYPE = (short) 0;
    private static final int VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG = 0;
    private static final int VALUE_WIFI_TO_PDP_AUTO_HANDOVER_MOBILE = 1;
    private static final int VALUE_WIFI_TO_PDP_CANNOT_HANDOVER_MOBILE = 2;
    public static final int WIFI_STATE_CONNECTED = 1;
    public static final int WIFI_STATE_DISCONNECTED = 2;
    private static WifiProStatisticsManager mStatisticsManager;
    private static int printLogLevel = 1;
    private String mBG_AP_SSID;
    private int mBQEROReasonFlag = 0;
    private int mBQERoveInReason = 0;
    private Context mContext;
    private WifiProChrDataBaseManager mDataBaseManager;
    private IGetApRecordCount mGetApRecordCountCallBack = null;
    private IGetMobileInfoCallBack mGetMobileInfoCallBack = null;
    private boolean mNeedSaveCHRStatistic = false;
    private WifiProStatisticsRecord mNewestStatRcd;
    private boolean mQoeOrNotInetRoveOutStarted = false;
    private int mROReason = 0;
    private long mROTime = 0;
    private WifiProRoveOutParaRecord mRoveOutPara = null;
    private int mRoveoutMobileDataNotifyType = 0;
    private SimpleDateFormat mSimpleDateFmt;
    private Handler mStatHandler;
    private WifiProCHRManager mWiFiCHRMgr;
    private long mWifiConnectStartTime = 0;

    class StatisticsCHRMsgHandler extends Handler {
        /* synthetic */ StatisticsCHRMsgHandler(WifiProStatisticsManager this$0, Looper looper, StatisticsCHRMsgHandler -this2) {
            this(looper);
        }

        private StatisticsCHRMsgHandler(Looper looper) {
            super(looper);
            WifiProStatisticsManager.this.logd("new StatisticsCHRMsgHandler");
        }

        public void handleMessage(Message msg) {
            short wifiproState;
            WifiProStatisticsRecord -get6;
            int mobileData;
            switch (msg.what) {
                case 100:
                    WifiProStatisticsManager.this.logd("ChrDataBaseManager init start.");
                    WifiProStatisticsManager.this.mDataBaseManager = WifiProChrDataBaseManager.getInstance(WifiProStatisticsManager.this.mContext);
                    WifiProStatisticsManager.this.loadStatDBRecord(WifiProStatisticsManager.this.mNewestStatRcd);
                    WifiProStatisticsManager.this.sendStatEmptyMsg(101);
                    return;
                case 101:
                    if (WifiProStatisticsManager.this.checkIfNeedUpload(WifiProStatisticsManager.this.mNewestStatRcd)) {
                        WifiProStatisticsManager.this.uploadStatisticsCHREvent(WifiProStatisticsManager.this.mNewestStatRcd);
                    }
                    if (WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState == (short) 0) {
                        WifiProStatisticsManager.this.loge("wifipro state abnormal, try reget it.");
                        wifiproState = WifiProStatisticsManager.this.getWifiproState();
                        if (wifiproState != (short) 0) {
                            WifiProStatisticsManager.this.sendStatMsg(103, wifiproState, 0);
                        }
                    }
                    WifiProStatisticsManager.this.sendCheckUploadMsg();
                    return;
                case 103:
                    wifiproState = (short) msg.arg1;
                    if (wifiproState != WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState) {
                        Date currDate = new Date();
                        String currDateStr = WifiProStatisticsManager.this.mSimpleDateFmt.format(currDate);
                        if (wifiproState == (short) 2 && WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState == (short) 1) {
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
                            -get6.mWifiproCloseCount = (short) (-get6.mWifiproCloseCount + 1);
                        } else if (wifiproState == (short) 1 && WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState == (short) 2) {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mWifiproOpenCount = (short) (-get6.mWifiproOpenCount + 1);
                        }
                        WifiProStatisticsManager.this.logd("wifipro old state:" + WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState + ", new state:" + wifiproState + ", date str:" + currDateStr);
                        WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState = wifiproState;
                        WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproStateUpdateTime = currDateStr;
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        return;
                    }
                    WifiProStatisticsManager.this.loge("wifipro state unknow or not changed msg receive:" + wifiproState);
                    return;
                case 104:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNoInetHandoverCount = (short) (-get6.mNoInetHandoverCount + 1);
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = true;
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 105:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mPortalUnauthCount = (short) (-get6.mPortalUnauthCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 106:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mWifiScoCount = (short) (-get6.mWifiScoCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 107:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mPortalCodeParseCount = (short) (-get6.mPortalCodeParseCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 108:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mRcvSMS_Count = (short) (-get6.mRcvSMS_Count + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 109:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mPortalAutoLoginCount = (short) (-get6.mPortalAutoLoginCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 110:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mCellAutoOpenCount = (short) (-get6.mCellAutoOpenCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 111:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mCellAutoCloseCount = (short) (-get6.mCellAutoCloseCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 113:
                    int roReason = msg.arg1;
                    int bqeRoReason = msg.arg2;
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = true;
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    WifiProStatisticsManager.this.roveOutEventProcess(roReason, bqeRoReason);
                    return;
                case 114:
                    int riReason = msg.arg1;
                    int bqeRiReason = msg.arg2;
                    if (WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted || 2 == riReason) {
                        WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                        WifiProStatisticsManager.this.roveInEventProcess(riReason, bqeRiReason, WifiProStatisticsManager.this.mBQEROReasonFlag, WifiProStatisticsManager.this.mROReason);
                        return;
                    }
                    WifiProStatisticsManager.this.logi("Ignore duplicate qoe RI reason:" + riReason);
                    return;
                case 115:
                    if (WifiProStatisticsManager.this.mRoveOutPara != null) {
                        WifiProStatisticsManager.this.mRoveOutPara.mMobileSignalLevel = (short) WifiProStatisticsManager.this.getMobileSignalLevel();
                        WifiProStatisticsManager.this.mRoveOutPara.mRATType = (short) WifiProStatisticsManager.this.getMobileRATType();
                        return;
                    }
                    return;
                case 116:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mHisScoRI_Count = (short) (-get6.mHisScoRI_Count + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 117:
                    WifiProStatisticsManager.this.mNewestStatRcd.mWifiOobInitState = (short) msg.arg1;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_ALARM_COUNT /*118*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNoInetAlarmCount = (short) (-get6.mNoInetAlarmCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    if (1 == msg.arg1) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mNoInetAlarmOnConnCnt = (short) (-get6.mNoInetAlarmOnConnCnt + 1);
                        return;
                    }
                    return;
                case WifiProStatisticsManager.STAT_MSG_USER_USE_BG_SCAN_COUNT /*119*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mUserUseBgScanAPCount = (short) (-get6.mUserUseBgScanAPCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 120:
                    int handOverType = (short) msg.arg1;
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mWifiToWifiSuccCount = (short) (-get6.mWifiToWifiSuccCount + 1);
                    if (1 == handOverType) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mNotInetWifiToWifiCount = (short) (-get6.mNotInetWifiToWifiCount + 1);
                    }
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 121:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mSelectNotInetAPCount = (short) (-get6.mSelectNotInetAPCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 122:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNotAutoConnPortalCnt = (short) (-get6.mNotAutoConnPortalCnt + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_HIGH_DATA_RATE_STOP_ROC /*123*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mHighDataRateStopROC = (short) (-get6.mHighDataRateStopROC + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 124:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mPingPongCount = (short) (-get6.mPingPongCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    int passTime = (int) ((SystemClock.elapsedRealtime() - WifiProStatisticsManager.this.mROTime) / 1000);
                    if (passTime == 1) {
                        passTime = 2;
                    }
                    if (passTime > WifiProStatisticsManager.MAX_SHORT_TYPE_VALUE) {
                        passTime = WifiProStatisticsManager.MAX_SHORT_TYPE_VALUE;
                    }
                    WifiProStatisticsManager.this.sendPingpongCHREvent(WifiProStatisticsManager.this.mRoveOutPara, (short) passTime);
                    return;
                case WifiProStatisticsManager.STAT_MSG_BQE_BAD_SETTING_CANCEL /*125*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mBQE_BadSettingCancel = (short) (-get6.mBQE_BadSettingCancel + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 126:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNotInetSettingCancel = (short) (-get6.mNotInetSettingCancel + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 127:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNotInetUserCancel = (short) (-get6.mNotInetUserCancel + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 128:
                    if (WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted) {
                        WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mNotInetRestoreRI = (short) (-get6.mNotInetRestoreRI + 1);
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mNotInet_AutoRI_TotData += WifiProStatisticsManager.this.getRoMobileData();
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        return;
                    }
                    WifiProStatisticsManager.this.logd("Ignore duplicate not inet restore RI event.");
                    return;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_USER_MANUAL_RI /*129*/:
                    if (WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted) {
                        WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mNotInetUserManualRI = (short) (-get6.mNotInetUserManualRI + 1);
                        int notInetRoDataKB = WifiProStatisticsManager.this.mGetMobileInfoCallBack != null ? WifiProStatisticsManager.this.mGetMobileInfoCallBack.getTotalRoMobileData() : 0;
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mTotBtnRICount = (short) (-get6.mTotBtnRICount + 1);
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mRO_TotMobileData += notInetRoDataKB;
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        return;
                    }
                    WifiProStatisticsManager.this.logd("Ignore duplicate not inet user manual RI event.");
                    return;
                case WifiProStatisticsManager.STAT_MSG_SCREEN_ON /*130*/:
                    WifiProStatisticsManager.this.checkMsgLoopRunning();
                    return;
                case WifiProStatisticsManager.STAT_MSG_USER_REOPEN_WIFI_RI_CNT /*131*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mReopenWifiRICount = (short) (-get6.mReopenWifiRICount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_SEL_CSP_SETTING_CHG_CNT /*132*/:
                    int cspVal = msg.arg1;
                    if (cspVal == 0) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mSelCSPShowDiglogCount = (short) (-get6.mSelCSPShowDiglogCount + 1);
                    } else if (1 == cspVal) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mSelCSPAutoSwCount = (short) (-get6.mSelCSPAutoSwCount + 1);
                    } else if (2 == cspVal) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mSelCSPNotSwCount = (short) (-get6.mSelCSPNotSwCount + 1);
                    }
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_HMD_NOTIFY_CNT /*133*/:
                    int notifyType = msg.arg1;
                    if (1 == notifyType) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mBMD_TenMNotifyCount = (short) (-get6.mBMD_TenMNotifyCount + 1);
                        WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 1;
                    } else if (2 == notifyType) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mBMD_FiftyMNotifyCount = (short) (-get6.mBMD_FiftyMNotifyCount + 1);
                        WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 2;
                    }
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_HMD_USER_DEL_NOTIFY_CNT /*134*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mBMD_UserDelNotifyCount = (short) (-get6.mBMD_UserDelNotifyCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_AF_CHR_UPDATE /*135*/:
                    int state = msg.arg1;
                    int type = msg.arg2;
                    if (type == 0) {
                        if (1 == state) {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_PhoneNumSuccCnt = (short) (-get6.mAF_PhoneNumSuccCnt + 1);
                        } else {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_PhoneNumFailCnt = (short) (-get6.mAF_PhoneNumFailCnt + 1);
                        }
                    } else if (1 == type) {
                        if (1 == state) {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_PasswordSuccCnt = (short) (-get6.mAF_PasswordSuccCnt + 1);
                        } else {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_PasswordFailCnt = (short) (-get6.mAF_PasswordFailCnt + 1);
                        }
                    } else if (2 == type) {
                        if (1 == state) {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_AutoLoginSuccCnt = (short) (-get6.mAF_AutoLoginSuccCnt + 1);
                        } else {
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mAF_AutoLoginFailCnt = (short) (-get6.mAF_AutoLoginFailCnt + 1);
                        }
                    } else if (3 == type) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mAF_FPNSuccNotMsmCnt = (short) (-get6.mAF_FPNSuccNotMsmCnt + 1);
                    }
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_BACK_GRADING_CHR_UPDATE /*136*/:
                    WifiProStatisticsManager.this.updateBGChrProcess(msg.arg1);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_BQE_GRADING_SVC_CHR_UPDATE /*137*/:
                    WifiProStatisticsManager.this.updateBqeSvcChrProcess(msg.arg1);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_BQE_BAD_RO_DISCONNECT_MOBILE_DATA /*138*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mQOE_RO_DISCONNECT_Cnt = (short) (-get6.mQOE_RO_DISCONNECT_Cnt + 1);
                    mobileData = WifiProStatisticsManager.this.getRoMobileData();
                    WifiProStatisticsManager.this.logd("qoe ro disconnect mobileData=" + mobileData);
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mQOE_RO_DISCONNECT_TotData += mobileData;
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_RO_DISCONNECT_MOBILE_DATA /*139*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNotInetRO_DISCONNECT_Cnt = (short) (-get6.mNotInetRO_DISCONNECT_Cnt + 1);
                    mobileData = WifiProStatisticsManager.this.getRoMobileData();
                    WifiProStatisticsManager.this.logd("inet ro disconnect mobileData=" + mobileData);
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mNotInetRO_DISCONNECT_TotData += mobileData;
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_UPDATE_WIFI_CONNECTION_STATE /*140*/:
                    int wifiState = msg.arg1;
                    if (1 == wifiState) {
                        if (WifiProStatisticsManager.this.mWifiConnectStartTime == 0) {
                            WifiProStatisticsManager.this.mWifiConnectStartTime = SystemClock.elapsedRealtime();
                            WifiProStatisticsManager.this.logd("wifi connect start here.");
                            return;
                        }
                        WifiProStatisticsManager.this.logd("wifi already connected.");
                        return;
                    } else if (2 == wifiState && WifiProStatisticsManager.this.mWifiConnectStartTime != 0) {
                        long connectionTime = SystemClock.elapsedRealtime() - WifiProStatisticsManager.this.mWifiConnectStartTime;
                        if (connectionTime > 0 && connectionTime < WifiProStatisticsManager.MAX_COMMERCIAL_USER_UPLOAD_PERIOD) {
                            long acctime = connectionTime / 1000;
                            -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                            -get6.mTotWifiConnectTime = (int) (((long) -get6.mTotWifiConnectTime) + acctime);
                            WifiProStatisticsManager.this.logd("acc wifi connection time:" + acctime + " s, total" + WifiProStatisticsManager.this.mNewestStatRcd.mTotWifiConnectTime);
                        }
                        WifiProStatisticsManager.this.logd("wifi connected end.");
                        WifiProStatisticsManager.this.mWifiConnectStartTime = 0;
                        return;
                    } else {
                        return;
                    }
                case WifiProStatisticsManager.STAT_MSG_PORTAL_AP_NOT_AUTO_CONN /*141*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mPortalNoAutoConnCnt = (short) (-get6.mPortalNoAutoConnCnt + 1);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_HOME_AP_ADD_RO_PERIOD_CNT /*142*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mHomeAPAddRoPeriodCnt = (short) (-get6.mHomeAPAddRoPeriodCnt + msg.arg1);
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mHomeAPQoeBadCnt = (short) (-get6.mHomeAPQoeBadCnt + 1);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_UPDATE_SATTISTIC_TO_DB /*144*/:
                    if (WifiProStatisticsManager.this.mNeedSaveCHRStatistic) {
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        WifiProStatisticsManager.this.mNeedSaveCHRStatistic = false;
                        return;
                    }
                    return;
                case WifiProStatisticsManager.STAT_MSG_ACTIVE_CHECK_RS_DIFF /*145*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mActiveCheckRS_Diff = (short) (-get6.mActiveCheckRS_Diff + 1);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    WifiProStatisticsManager.this.mWiFiCHRMgr.updateBG_AC_DiffType(msg.arg1);
                    WifiProStatisticsManager.this.mWiFiCHRMgr.updateSSID(WifiProStatisticsManager.this.mBG_AP_SSID);
                    WifiProStatisticsManager.this.mWiFiCHRMgr.updateWifiException(122, "BG_AC_RS_DIFF");
                    return;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_PORTAL_CONN_COUNT /*146*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mTotalPortalConnCount = (short) (-get6.mTotalPortalConnCount + 1);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_PORTAL_AUTH_SUCC_COUNT /*147*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mTotalPortalAuthSuccCount = (short) (-get6.mTotalPortalAuthSuccCount + 1);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_CONN_BLOCK_PORTAL_COUNT /*148*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mManualConnBlockPortalCount = (short) (-get6.mManualConnBlockPortalCount + 1);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_AC_RS_SAME_COUNT /*149*/:
                    -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    -get6.mActiveCheckRS_Same = (short) (-get6.mActiveCheckRS_Same + 1);
                    WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_HMD_BTN_RI_COUNT /*150*/:
                    if (2 == WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mBMD_FiftyM_RI_Count = (short) (-get6.mBMD_FiftyM_RI_Count + 1);
                    } else if (1 == WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType) {
                        -get6 = WifiProStatisticsManager.this.mNewestStatRcd;
                        -get6.mBMD_TenM_RI_Count = (short) (-get6.mBMD_TenM_RI_Count + 1);
                    }
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                default:
                    WifiProStatisticsManager.this.loge("statistics manager got unknow message.");
                    return;
            }
        }
    }

    private WifiProStatisticsManager(Context context) {
        logd("WifiProStatisticsManager enter.");
        this.mContext = context;
        initStatHandler();
        this.mWiFiCHRMgr = WifiProCHRManager.getInstance();
        this.mNewestStatRcd = new WifiProStatisticsRecord();
        this.mSimpleDateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sendStatEmptyMsg(100);
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
        } else if (this.mDataBaseManager.queryChrStatRcd(statRecord) && this.mNewestStatRcd.mLastStatUploadTime.equals("DEAULT_STR")) {
            logi("Not record in database now.");
            resetStatRecord(statRecord, "new phone, save first record.");
        } else if (checkDateValid(statRecord.mLastStatUploadTime) && (checkDateValid(statRecord.mLastWifiproStateUpdateTime) ^ 1) == 0) {
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
                int historyTotWifiHours = statRecord.mHistoryTotWifiConnHour / 3600;
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
                this.mWiFiCHRMgr.updateWifiException(909002032, "NO_SUB_EVENT");
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
            sendStatMsg(117, boolState ? 1 : 2, 0);
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
            sendStatMsg(103, state, 0);
        }
    }

    private short getWifiproState() {
        if (!checkInitOk()) {
            return (short) 0;
        }
        short state = (short) 2;
        if (WifiProCommonUtils.isWifiProSwitchOn(this.mContext)) {
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
            return -1;
        }
        int mobileSignalLevel = this.mGetMobileInfoCallBack.onGetMobileSignalLevel();
        logd("getMobileSignalLevel new level:" + mobileSignalLevel);
        return mobileSignalLevel;
    }

    private int getMobileRATType() {
        if (this.mGetMobileInfoCallBack == null) {
            return 0;
        }
        int mobileRatType = this.mGetMobileInfoCallBack.onGetMobileRATType();
        logd("getMobileRATType new type:" + mobileRatType);
        return mobileRatType;
    }

    public void setBQERoveOutReason(boolean rssiTcpBad, boolean otaTcpBad, boolean tcpBad, boolean bigRtt, WifiProRoveOutParaRecord roveOutPara) {
        if (checkInitOk()) {
            this.mBQEROReasonFlag = 0;
            if (rssiTcpBad) {
                this.mBQEROReasonFlag |= 1;
            }
            if (otaTcpBad) {
                this.mBQEROReasonFlag |= 2;
            }
            if (tcpBad) {
                this.mBQEROReasonFlag |= 4;
            }
            if (bigRtt) {
                this.mBQEROReasonFlag |= 8;
            }
            logi("setBQERoveOutReason enter, reason:" + this.mBQEROReasonFlag);
            this.mRoveOutPara = roveOutPara;
            sendStatEmptyMsg(115);
        }
    }

    public void sendWifiproRoveOutEvent(int reason) {
        if (checkInitOk()) {
            logi("sendWifiproRoveOutEvent enter, reason:" + reason);
            sendStatMsg(113, reason, this.mBQEROReasonFlag);
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
            sendStatMsg(114, riReason, this.mBQERoveInReason);
        }
    }

    public void increaseNoInetHandoverCount() {
        if (checkInitOk()) {
            logd("increaseNoInetHandoverCount enter.");
            sendStatEmptyMsg(104);
        }
    }

    public void increaseWiFiHandoverWiFiCount(int handOverType) {
        if (checkInitOk()) {
            logd("increaseWiFiHandoverWiFiCount enter. handOverType =" + handOverType);
            sendStatMsg(120, handOverType, 0);
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
            sendStatMsg(STAT_MSG_SEL_CSP_SETTING_CHG_CNT, newSettingValue, 0);
        }
    }

    public void increaseHMDNotifyCount(int notifyType) {
        if (checkInitOk()) {
            logd("increaseSelCspSettingChgCount enter. notifyType val=" + notifyType);
            sendStatMsg(STAT_MSG_HMD_NOTIFY_CNT, notifyType, 0);
        }
    }

    public void increaseUserDelNotifyCount() {
        if (checkInitOk()) {
            logd("increaseUserDelNotifyCount enter.");
            sendStatEmptyMsg(STAT_MSG_HMD_USER_DEL_NOTIFY_CNT);
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
            int state = success ? 1 : 0;
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
            sendStatMsg(STAT_MSG_HOME_AP_ADD_RO_PERIOD_CNT, periodCount, 0);
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
            sendStatMsg(STAT_MSG_ACTIVE_CHECK_RS_DIFF, diffType, 0);
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
            sendStatMsg(STAT_MSG_BACK_GRADING_CHR_UPDATE, bgChrType, 0);
        }
    }

    public void updateBqeSvcChrStatistic(int bqeSvcChrType) {
        if (checkInitOk()) {
            logd("updateBGChrStatistic enter. notifyType val=" + bqeSvcChrType);
            sendStatMsg(STAT_MSG_BQE_GRADING_SVC_CHR_UPDATE, bqeSvcChrType, 0);
        }
    }

    public void increaseNoInetRemindCount(boolean isConnAlarm) {
        if (checkInitOk()) {
            int i;
            logd("increaseNoInetRemindCount enter. isConnAlarm = " + isConnAlarm);
            if (isConnAlarm) {
                i = 1;
            } else {
                i = 0;
            }
            sendStatMsg(STAT_MSG_NOT_INET_ALARM_COUNT, i, 0);
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
            sendStatEmptyMsg(124);
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
            sendStatEmptyMsg(126);
        }
    }

    public void increaseNotInetUserCancelCount() {
        if (checkInitOk()) {
            logd("increaseNotInetUserCancelCount enter.");
            sendStatEmptyMsg(127);
        }
    }

    public void increaseNotInetRestoreRICount() {
        if (checkInitOk()) {
            logd("increaseNotInetRestoreRICount enter.");
            sendStatEmptyMsg(128);
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
            sendStatEmptyMsg(121);
        }
    }

    public void increaseNotAutoConnPortalCnt() {
        if (checkInitOk()) {
            logd("increaseNotAutoConnPortalCnt enter.");
            sendStatEmptyMsg(122);
        }
    }

    public void increasePortalUnauthCount() {
        if (checkInitOk()) {
            logd("increasePortalUnauthCount enter.");
            sendStatEmptyMsg(105);
        }
    }

    public void increaseWifiScoCount() {
        if (checkInitOk()) {
            logd("increaseWifiScoCount enter.");
            sendStatEmptyMsg(106);
        }
    }

    public void increasePortalCodeParseCount() {
        if (checkInitOk()) {
            logd("increasePortalCodeParseCount enter.");
            sendStatEmptyMsg(107);
        }
    }

    public void increaseRcvSMS_Count() {
        if (checkInitOk()) {
            logd("increaseRcvSMS_Count enter.");
            sendStatEmptyMsg(108);
        }
    }

    public void increasePortalAutoLoginCount() {
        if (checkInitOk()) {
            logd("increasePortalAutoLoginCount enter.");
            sendStatEmptyMsg(109);
        }
    }

    public void increaseAutoOpenCount() {
        if (checkInitOk()) {
            logd("increaseCellAutoOpenCount enter.");
            sendStatEmptyMsg(110);
        }
    }

    public void increaseAutoCloseCount() {
        if (checkInitOk()) {
            logd("increaseCellAutoCloseCount enter.");
            sendStatEmptyMsg(111);
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
            sendStatMsg(STAT_MSG_UPDATE_WIFI_CONNECTION_STATE, newWifiState, 0);
        }
    }

    public void sendScreenOnEvent() {
        if (checkInitOk()) {
            logd("sendScreenOnEvent enter.");
            sendStatEmptyMsg(STAT_MSG_SCREEN_ON);
        }
    }

    private void logd(String msg) {
        if (printLogLevel <= 1) {
            Log.d(TAG, msg);
        }
    }

    private void logi(String msg) {
        if (printLogLevel <= 2) {
            Log.i(TAG, msg);
        }
    }

    private void loge(String msg) {
        if (printLogLevel <= 3) {
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
        if (1 == roReason) {
            WifiProStatisticsRecord wifiProStatisticsRecord;
            if ((bqeRoReasonFlag & 1) != 0) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRSSI_RO_Tot = (short) (wifiProStatisticsRecord.mRSSI_RO_Tot + 1);
            }
            if ((bqeRoReasonFlag & 2) != 0) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mOTA_RO_Tot = (short) (wifiProStatisticsRecord.mOTA_RO_Tot + 1);
            }
            if ((bqeRoReasonFlag & 4) != 0) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mTCP_RO_Tot = (short) (wifiProStatisticsRecord.mTCP_RO_Tot + 1);
            }
            if ((bqeRoReasonFlag & 8) != 0) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBigRTT_RO_Tot = (short) (wifiProStatisticsRecord.mBigRTT_RO_Tot + 1);
            }
            wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mTotalBQE_BadROC = (short) (wifiProStatisticsRecord.mTotalBQE_BadROC + 1);
            this.mDataBaseManager.addOrUpdateChrStatRcd(this.mNewestStatRcd);
        }
        this.mROReason = roReason;
        this.mROTime = SystemClock.elapsedRealtime();
    }

    private int getRoMobileData() {
        return this.mGetMobileInfoCallBack != null ? this.mGetMobileInfoCallBack.getTotalRoMobileData() : 0;
    }

    private void roveInEventProcess(int riReason, int bqeRiReasonFlag, int bqeRoReasonFlag, int roReason) {
        long passTime;
        WifiProStatisticsRecord wifiProStatisticsRecord;
        logi("roveInEventProcess enter, RI reason:" + riReason + ", BQE RI reason:" + bqeRiReasonFlag + ", roReasonFlag:" + bqeRoReasonFlag + ", roReason:" + roReason);
        boolean needUpdateDB = false;
        if (2 == riReason) {
            passTime = 1;
            logd("update ro reason by cancel ri event.");
            roReason = 1;
            roveOutEventProcess(1, bqeRoReasonFlag);
        } else if (this.mROTime == 0) {
            passTime = 0;
        } else {
            passTime = (SystemClock.elapsedRealtime() - this.mROTime) / 1000;
            if (passTime == 1) {
                passTime = 2;
            }
        }
        this.mROTime = 0;
        if (1 == riReason) {
            if (1 == bqeRiReasonFlag) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRSSI_RestoreRI_Count = (short) (wifiProStatisticsRecord.mRSSI_RestoreRI_Count + 1);
            } else if (2 == bqeRiReasonFlag) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRSSI_BetterRI_Count = (short) (wifiProStatisticsRecord.mRSSI_BetterRI_Count + 1);
            } else if (3 == bqeRiReasonFlag) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mTimerRI_Count = (short) (wifiProStatisticsRecord.mTimerRI_Count + 1);
            }
            wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mAutoRI_TotCount = (short) (wifiProStatisticsRecord.mAutoRI_TotCount + 1);
            wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mAutoRI_TotTime = (int) (((long) wifiProStatisticsRecord.mAutoRI_TotTime) + passTime);
            wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mQOE_AutoRI_TotData += getRoMobileData();
            needUpdateDB = true;
        } else if (6 == riReason) {
            wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mHisScoRI_Count = (short) (wifiProStatisticsRecord.mHisScoRI_Count + 1);
            needUpdateDB = true;
        }
        boolean isManualRI = (3 == riReason || 4 == riReason) ? true : 5 == riReason;
        boolean isUserCancelRI = 2 == riReason;
        if (1 == roReason) {
            if (isUserCancelRI || isManualRI) {
                logd("abnormal BQE bad rove out statistics process.");
                if ((bqeRoReasonFlag & 1) != 0) {
                    wifiProStatisticsRecord = this.mNewestStatRcd;
                    wifiProStatisticsRecord.mRSSI_ErrRO_Tot = (short) (wifiProStatisticsRecord.mRSSI_ErrRO_Tot + 1);
                }
                if ((bqeRoReasonFlag & 2) != 0) {
                    wifiProStatisticsRecord = this.mNewestStatRcd;
                    wifiProStatisticsRecord.mOTA_ErrRO_Tot = (short) (wifiProStatisticsRecord.mOTA_ErrRO_Tot + 1);
                }
                if ((bqeRoReasonFlag & 4) != 0) {
                    wifiProStatisticsRecord = this.mNewestStatRcd;
                    wifiProStatisticsRecord.mTCP_ErrRO_Tot = (short) (wifiProStatisticsRecord.mTCP_ErrRO_Tot + 1);
                }
                if ((bqeRoReasonFlag & 8) != 0) {
                    wifiProStatisticsRecord = this.mNewestStatRcd;
                    wifiProStatisticsRecord.mBigRTT_ErrRO_Tot = (short) (wifiProStatisticsRecord.mBigRTT_ErrRO_Tot + 1);
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
                wifiProStatisticsRecord.mManualBackROC = (short) (wifiProStatisticsRecord.mManualBackROC + 1);
                int roDataKB = this.mGetMobileInfoCallBack != null ? this.mGetMobileInfoCallBack.getTotalRoMobileData() : 0;
                if (4 == riReason) {
                    wifiProStatisticsRecord = this.mNewestStatRcd;
                    wifiProStatisticsRecord.mTotBtnRICount = (short) (wifiProStatisticsRecord.mTotBtnRICount + 1);
                }
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRO_TotMobileData += roDataKB;
            }
            if (isUserCancelRI) {
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mUserCancelROC = (short) (wifiProStatisticsRecord.mUserCancelROC + 1);
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
            this.mWiFiCHRMgr.updateWifiException(122, "ROVE_OUT_PARAMETER");
        }
    }

    private void sendPingpongCHREvent(WifiProRoveOutParaRecord roveOutPara, short roTime) {
        if (roveOutPara != null) {
            logi("Pingpong RO para CHR event send.");
            roveOutPara.mRO_Duration = roTime;
            this.mWiFiCHRMgr.updateExcpRoParaPart1(roveOutPara.mRSSI_VALUE, roveOutPara.mOTA_PacketDropRate, roveOutPara.mRttAvg, roveOutPara.mTcpInSegs, roveOutPara.mTcpOutSegs, roveOutPara.mTcpRetransSegs, roveOutPara.mWIFI_NetSpeed, roveOutPara.mIPQLevel);
            this.mWiFiCHRMgr.updateExcpRoParaPart2(roveOutPara.mRO_APSsid, roveOutPara.mMobileSignalLevel, roveOutPara.mRATType, roveOutPara.mHistoryQuilityRO_Rate, roveOutPara.mHighDataRateRO_Rate, roveOutPara.mCreditScoreRO_Rate, roveOutPara.mRO_Duration);
            this.mWiFiCHRMgr.updateWifiException(122, "SWITCH_PINGPONG");
        }
    }

    private void initStatHandler() {
        HandlerThread thread = new HandlerThread("StatisticsCHRMsgHandler");
        thread.start();
        this.mStatHandler = new StatisticsCHRMsgHandler(this, thread.getLooper(), null);
    }

    private void sendCheckUploadMsg() {
        if (this.mStatHandler.hasMessages(101)) {
            loge("There has CHECK_NEED_UPLOAD_EVENT msg at queue.");
        } else if (DEBUG_MODE) {
            sendStatEmptyMsgDelayed(101, 60000);
        } else {
            sendStatEmptyMsgDelayed(101, 1800000);
        }
    }

    private void checkMsgLoopRunning() {
        if (this.mStatHandler.hasMessages(101)) {
            logd("msg Loop is running.");
            return;
        }
        logd("restart msg Loop.");
        sendStatEmptyMsg(101);
    }

    private void updateBGChrProcess(int bgChrType) {
        logd("updateBGChrProcess enter for bg type:" + bgChrType);
        WifiProStatisticsRecord wifiProStatisticsRecord;
        switch (bgChrType) {
            case 1:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_BgRunCnt = (short) (wifiProStatisticsRecord.mBG_BgRunCnt + 1);
                return;
            case 2:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_SettingRunCnt = (short) (wifiProStatisticsRecord.mBG_SettingRunCnt + 1);
                return;
            case 3:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_FreeInetOkApCnt = (short) (wifiProStatisticsRecord.mBG_FreeInetOkApCnt + 1);
                return;
            case 4:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_FishingApCnt = (short) (wifiProStatisticsRecord.mBG_FishingApCnt + 1);
                return;
            case 5:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_FreeNotInetApCnt = (short) (wifiProStatisticsRecord.mBG_FreeNotInetApCnt + 1);
                return;
            case 6:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_PortalApCnt = (short) (wifiProStatisticsRecord.mBG_PortalApCnt + 1);
                return;
            case 7:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_UserSelFreeInetOkCnt = (short) (wifiProStatisticsRecord.mBG_UserSelFreeInetOkCnt + 1);
                return;
            case 8:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_UserSelNoInetCnt = (short) (wifiProStatisticsRecord.mBG_UserSelNoInetCnt + 1);
                return;
            case 9:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_UserSelPortalCnt = (short) (wifiProStatisticsRecord.mBG_UserSelPortalCnt + 1);
                return;
            case 10:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_FoundTwoMoreApCnt = (short) (wifiProStatisticsRecord.mBG_FoundTwoMoreApCnt + 1);
                return;
            case 11:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_FailedCnt = (short) (wifiProStatisticsRecord.mBG_FailedCnt + 1);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(122, "BG_FAILED_CNT");
                return;
            case 12:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_InetNotOkActiveOk = (short) (wifiProStatisticsRecord.mBG_InetNotOkActiveOk + 1);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(122, "BG_NOT_INET_ACTIVE_IOK");
                return;
            case 13:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_InetOkActiveNotOk = (short) (wifiProStatisticsRecord.mBG_InetOkActiveNotOk + 1);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(122, "BG_INET_OK_ACTIVE_NOT_OK");
                return;
            case 14:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_UserSelApFishingCnt = (short) (wifiProStatisticsRecord.mBG_UserSelApFishingCnt + 1);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(122, "BG_USER_SEL_AP_FISHING_CNT");
                return;
            case 15:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_ConntTimeoutCnt = (short) (wifiProStatisticsRecord.mBG_ConntTimeoutCnt + 1);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(122, "BG_CONNT_TIMEOUT_CNT");
                return;
            case 16:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_DNSFailCnt = (short) (wifiProStatisticsRecord.mBG_DNSFailCnt + 1);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(122, "BG_DNS_FAIL_CNT");
                return;
            case 17:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_DHCPFailCnt = (short) (wifiProStatisticsRecord.mBG_DHCPFailCnt + 1);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(122, "BG_DHCP_FAIL_CNT");
                return;
            case 18:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_AUTH_FailCnt = (short) (wifiProStatisticsRecord.mBG_AUTH_FailCnt + 1);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(122, "BG_AUTH_FAIL_CNT");
                return;
            case 19:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_AssocRejectCnt = (short) (wifiProStatisticsRecord.mBG_AssocRejectCnt + 1);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(122, "BG_ASSOC_REJECT_CNT");
                return;
            case 20:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_NCByConnectFail = (short) (wifiProStatisticsRecord.mBG_NCByConnectFail + 1);
                return;
            case 21:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_NCByCheckFail = (short) (wifiProStatisticsRecord.mBG_NCByCheckFail + 1);
                return;
            case 22:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_NCByStateErr = (short) (wifiProStatisticsRecord.mBG_NCByStateErr + 1);
                return;
            case 23:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_NCByUnknown = (short) (wifiProStatisticsRecord.mBG_NCByUnknown + 1);
                return;
            default:
                loge("updateBGChrProcess error type:" + bgChrType);
                return;
        }
    }

    private void updateBqeSvcChrProcess(int bqeSvcChrType) {
        logd("updateBqeSvcChrProcess enter for bg type:" + bqeSvcChrType);
        WifiProStatisticsRecord wifiProStatisticsRecord;
        switch (bqeSvcChrType) {
            case 1:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBSG_RsGoodCnt = (short) (wifiProStatisticsRecord.mBSG_RsGoodCnt + 1);
                return;
            case 2:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBSG_RsMidCnt = (short) (wifiProStatisticsRecord.mBSG_RsMidCnt + 1);
                return;
            case 3:
                wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBSG_RsBadCnt = (short) (wifiProStatisticsRecord.mBSG_RsBadCnt + 1);
                return;
            default:
                loge("updateBqeSvcChrProcess error type:" + bqeSvcChrType);
                return;
        }
    }
}
