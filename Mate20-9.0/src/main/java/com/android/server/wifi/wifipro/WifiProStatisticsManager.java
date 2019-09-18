package com.android.server.wifi.wifipro;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.wifi.HwQoE.HidataWechatTraffic;
import com.android.server.wifi.HwQoE.HwWifiGameNetChrInfo;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiConnectivityMonitor;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifipro.WifiProCHRManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    private static final short INVALID_MOBILE_SIGNAL_LEVEL = -1;
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
    private static final short UNKNOWN_RAT_TYPE = 0;
    private static final int VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG = 0;
    private static final int VALUE_WIFI_TO_PDP_AUTO_HANDOVER_MOBILE = 1;
    private static final int VALUE_WIFI_TO_PDP_CANNOT_HANDOVER_MOBILE = 2;
    public static final int WIFI_STATE_CONNECTED = 1;
    public static final int WIFI_STATE_DISCONNECTED = 2;
    private static WifiProStatisticsManager mStatisticsManager;
    private static int printLogLevel = 1;
    /* access modifiers changed from: private */
    public String mBG_AP_SSID;
    /* access modifiers changed from: private */
    public int mBQEROReasonFlag = 0;
    private int mBQERoveInReason = 0;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public WifiProChrDataBaseManager mDataBaseManager;
    private IGetApRecordCount mGetApRecordCountCallBack = null;
    /* access modifiers changed from: private */
    public IGetMobileInfoCallBack mGetMobileInfoCallBack = null;
    /* access modifiers changed from: private */
    public boolean mNeedSaveCHRStatistic = false;
    /* access modifiers changed from: private */
    public WifiProStatisticsRecord mNewestStatRcd;
    /* access modifiers changed from: private */
    public boolean mQoeOrNotInetRoveOutStarted = false;
    /* access modifiers changed from: private */
    public int mROReason = 0;
    /* access modifiers changed from: private */
    public long mROTime = 0;
    /* access modifiers changed from: private */
    public WifiProRoveOutParaRecord mRoveOutPara = null;
    /* access modifiers changed from: private */
    public int mRoveoutMobileDataNotifyType = 0;
    /* access modifiers changed from: private */
    public SimpleDateFormat mSimpleDateFmt;
    private Handler mStatHandler;
    /* access modifiers changed from: private */
    public WifiProCHRManager mWiFiCHRMgr;
    /* access modifiers changed from: private */
    public long mWifiConnectStartTime = 0;

    class StatisticsCHRMsgHandler extends Handler {
        private StatisticsCHRMsgHandler(Looper looper) {
            super(looper);
            WifiProStatisticsManager.this.logd("new StatisticsCHRMsgHandler");
        }

        public void handleMessage(Message msg) {
            int notInetRoDataKB = 0;
            switch (msg.what) {
                case 100:
                    WifiProStatisticsManager.this.logd("ChrDataBaseManager init start.");
                    WifiProChrDataBaseManager unused = WifiProStatisticsManager.this.mDataBaseManager = WifiProChrDataBaseManager.getInstance(WifiProStatisticsManager.this.mContext);
                    WifiProStatisticsManager.this.loadStatDBRecord(WifiProStatisticsManager.this.mNewestStatRcd);
                    WifiProStatisticsManager.this.sendStatEmptyMsg(101);
                    return;
                case 101:
                    if (WifiProStatisticsManager.this.checkIfNeedUpload(WifiProStatisticsManager.this.mNewestStatRcd)) {
                        boolean unused2 = WifiProStatisticsManager.this.uploadStatisticsCHREvent(WifiProStatisticsManager.this.mNewestStatRcd);
                    }
                    if (WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState == 0) {
                        WifiProStatisticsManager.this.loge("wifipro state abnormal, try reget it.");
                        short wifiproState = WifiProStatisticsManager.this.getWifiproState();
                        if (wifiproState != 0) {
                            WifiProStatisticsManager.this.sendStatMsg(103, wifiproState, 0);
                        }
                    }
                    WifiProStatisticsManager.this.sendCheckUploadMsg();
                    return;
                case 103:
                    short wifiproState2 = (short) msg.arg1;
                    if (wifiproState2 != WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState) {
                        Date currDate = new Date();
                        String currDateStr = WifiProStatisticsManager.this.mSimpleDateFmt.format(currDate);
                        if (wifiproState2 == 2 && WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState == 1) {
                            long enableTotTime = WifiProStatisticsManager.this.calcTimeInterval(WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproStateUpdateTime, currDate);
                            if (enableTotTime <= -1) {
                                WifiProStatisticsManager.this.resetStatRecord(WifiProStatisticsManager.this.mNewestStatRcd, "last WifiproStateUpdateTime time record invalid");
                                return;
                            }
                            WifiProStatisticsRecord access$400 = WifiProStatisticsManager.this.mNewestStatRcd;
                            access$400.mEnableTotTime += (int) (enableTotTime / 1000);
                            WifiProStatisticsManager wifiProStatisticsManager = WifiProStatisticsManager.this;
                            wifiProStatisticsManager.logd("last state en seconds:" + enableTotTime);
                            WifiProStatisticsRecord access$4002 = WifiProStatisticsManager.this.mNewestStatRcd;
                            access$4002.mWifiproCloseCount = (short) (access$4002.mWifiproCloseCount + 1);
                        } else if (wifiproState2 == 1 && WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState == 2) {
                            WifiProStatisticsRecord access$4003 = WifiProStatisticsManager.this.mNewestStatRcd;
                            access$4003.mWifiproOpenCount = (short) (access$4003.mWifiproOpenCount + 1);
                        }
                        WifiProStatisticsManager.this.logd("wifipro old state:" + WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState + ", new state:" + wifiproState2 + ", date str:" + currDateStr);
                        WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState = wifiproState2;
                        WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproStateUpdateTime = currDateStr;
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        return;
                    }
                    WifiProStatisticsManager.this.loge("wifipro state unknow or not changed msg receive:" + wifiproState2);
                    return;
                case 104:
                    WifiProStatisticsRecord access$4004 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$4004.mNoInetHandoverCount = (short) (access$4004.mNoInetHandoverCount + 1);
                    boolean unused3 = WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = true;
                    int unused4 = WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 105:
                    WifiProStatisticsRecord access$4005 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$4005.mPortalUnauthCount = (short) (access$4005.mPortalUnauthCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 106:
                    WifiProStatisticsRecord access$4006 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$4006.mWifiScoCount = (short) (access$4006.mWifiScoCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 107:
                    WifiProStatisticsRecord access$4007 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$4007.mPortalCodeParseCount = (short) (access$4007.mPortalCodeParseCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 108:
                    WifiProStatisticsRecord access$4008 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$4008.mRcvSMS_Count = (short) (access$4008.mRcvSMS_Count + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 109:
                    WifiProStatisticsRecord access$4009 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$4009.mPortalAutoLoginCount = (short) (access$4009.mPortalAutoLoginCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 110:
                    WifiProStatisticsRecord access$40010 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40010.mCellAutoOpenCount = (short) (access$40010.mCellAutoOpenCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 111:
                    WifiProStatisticsRecord access$40011 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40011.mCellAutoCloseCount = (short) (access$40011.mCellAutoCloseCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 113:
                    int roReason = msg.arg1;
                    int bqeRoReason = msg.arg2;
                    boolean unused5 = WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = true;
                    int unused6 = WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    WifiProStatisticsManager.this.roveOutEventProcess(roReason, bqeRoReason);
                    return;
                case 114:
                    int riReason = msg.arg1;
                    int bqeRiReason = msg.arg2;
                    if (WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted || 2 == riReason) {
                        boolean unused7 = WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
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
                    WifiProStatisticsRecord access$40012 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40012.mHisScoRI_Count = (short) (access$40012.mHisScoRI_Count + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 117:
                    WifiProStatisticsManager.this.mNewestStatRcd.mWifiOobInitState = (short) msg.arg1;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 118:
                    WifiProStatisticsRecord access$40013 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40013.mNoInetAlarmCount = (short) (access$40013.mNoInetAlarmCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    if (1 == msg.arg1) {
                        WifiProStatisticsRecord access$40014 = WifiProStatisticsManager.this.mNewestStatRcd;
                        access$40014.mNoInetAlarmOnConnCnt = (short) (access$40014.mNoInetAlarmOnConnCnt + 1);
                        return;
                    }
                    return;
                case 119:
                    WifiProStatisticsRecord access$40015 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40015.mUserUseBgScanAPCount = (short) (access$40015.mUserUseBgScanAPCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 120:
                    int handOverType = (short) msg.arg1;
                    WifiProStatisticsRecord access$40016 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40016.mWifiToWifiSuccCount = (short) (access$40016.mWifiToWifiSuccCount + 1);
                    if (1 == handOverType) {
                        WifiProStatisticsRecord access$40017 = WifiProStatisticsManager.this.mNewestStatRcd;
                        access$40017.mNotInetWifiToWifiCount = (short) (access$40017.mNotInetWifiToWifiCount + 1);
                    }
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 121:
                    WifiProStatisticsRecord access$40018 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40018.mSelectNotInetAPCount = (short) (access$40018.mSelectNotInetAPCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 122:
                    WifiProStatisticsRecord access$40019 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40019.mNotAutoConnPortalCnt = (short) (access$40019.mNotAutoConnPortalCnt + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 123:
                    WifiProStatisticsRecord access$40020 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40020.mHighDataRateStopROC = (short) (access$40020.mHighDataRateStopROC + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 124:
                    WifiProStatisticsRecord access$40021 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40021.mPingPongCount = (short) (access$40021.mPingPongCount + 1);
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
                case 125:
                    WifiProStatisticsRecord access$40022 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40022.mBQE_BadSettingCancel = (short) (access$40022.mBQE_BadSettingCancel + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 126:
                    WifiProStatisticsRecord access$40023 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40023.mNotInetSettingCancel = (short) (access$40023.mNotInetSettingCancel + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 127:
                    WifiProStatisticsRecord access$40024 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40024.mNotInetUserCancel = (short) (access$40024.mNotInetUserCancel + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 128:
                    if (!WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted) {
                        WifiProStatisticsManager.this.logd("Ignore duplicate not inet restore RI event.");
                        return;
                    }
                    boolean unused8 = WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                    WifiProStatisticsRecord access$40025 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40025.mNotInetRestoreRI = (short) (access$40025.mNotInetRestoreRI + 1);
                    WifiProStatisticsManager.this.mNewestStatRcd.mNotInet_AutoRI_TotData += WifiProStatisticsManager.this.getRoMobileData();
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_USER_MANUAL_RI /*129*/:
                    if (!WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted) {
                        WifiProStatisticsManager.this.logd("Ignore duplicate not inet user manual RI event.");
                        return;
                    }
                    boolean unused9 = WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                    WifiProStatisticsRecord access$40026 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40026.mNotInetUserManualRI = (short) (access$40026.mNotInetUserManualRI + 1);
                    if (WifiProStatisticsManager.this.mGetMobileInfoCallBack != null) {
                        notInetRoDataKB = WifiProStatisticsManager.this.mGetMobileInfoCallBack.getTotalRoMobileData();
                    }
                    WifiProStatisticsRecord access$40027 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40027.mTotBtnRICount = (short) (access$40027.mTotBtnRICount + 1);
                    WifiProStatisticsManager.this.mNewestStatRcd.mRO_TotMobileData += notInetRoDataKB;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_SCREEN_ON /*130*/:
                    WifiProStatisticsManager.this.checkMsgLoopRunning();
                    return;
                case WifiProStatisticsManager.STAT_MSG_USER_REOPEN_WIFI_RI_CNT /*131*/:
                    WifiProStatisticsRecord access$40028 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40028.mReopenWifiRICount = (short) (access$40028.mReopenWifiRICount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_SEL_CSP_SETTING_CHG_CNT /*132*/:
                    int cspVal = msg.arg1;
                    if (cspVal == 0) {
                        WifiProStatisticsRecord access$40029 = WifiProStatisticsManager.this.mNewestStatRcd;
                        access$40029.mSelCSPShowDiglogCount = (short) (access$40029.mSelCSPShowDiglogCount + 1);
                    } else if (1 == cspVal) {
                        WifiProStatisticsRecord access$40030 = WifiProStatisticsManager.this.mNewestStatRcd;
                        access$40030.mSelCSPAutoSwCount = (short) (access$40030.mSelCSPAutoSwCount + 1);
                    } else if (2 == cspVal) {
                        WifiProStatisticsRecord access$40031 = WifiProStatisticsManager.this.mNewestStatRcd;
                        access$40031.mSelCSPNotSwCount = (short) (access$40031.mSelCSPNotSwCount + 1);
                    }
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_HMD_NOTIFY_CNT /*133*/:
                    int notifyType = msg.arg1;
                    if (1 == notifyType) {
                        WifiProStatisticsRecord access$40032 = WifiProStatisticsManager.this.mNewestStatRcd;
                        access$40032.mBMD_TenMNotifyCount = (short) (access$40032.mBMD_TenMNotifyCount + 1);
                        int unused10 = WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 1;
                    } else if (2 == notifyType) {
                        WifiProStatisticsRecord access$40033 = WifiProStatisticsManager.this.mNewestStatRcd;
                        access$40033.mBMD_FiftyMNotifyCount = (short) (access$40033.mBMD_FiftyMNotifyCount + 1);
                        int unused11 = WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 2;
                    }
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_HMD_USER_DEL_NOTIFY_CNT /*134*/:
                    WifiProStatisticsRecord access$40034 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40034.mBMD_UserDelNotifyCount = (short) (access$40034.mBMD_UserDelNotifyCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_AF_CHR_UPDATE /*135*/:
                    int state = msg.arg1;
                    int type = msg.arg2;
                    if (type == 0) {
                        if (1 == state) {
                            WifiProStatisticsRecord access$40035 = WifiProStatisticsManager.this.mNewestStatRcd;
                            access$40035.mAF_PhoneNumSuccCnt = (short) (access$40035.mAF_PhoneNumSuccCnt + 1);
                        } else {
                            WifiProStatisticsRecord access$40036 = WifiProStatisticsManager.this.mNewestStatRcd;
                            access$40036.mAF_PhoneNumFailCnt = (short) (access$40036.mAF_PhoneNumFailCnt + 1);
                        }
                    } else if (1 == type) {
                        if (1 == state) {
                            WifiProStatisticsRecord access$40037 = WifiProStatisticsManager.this.mNewestStatRcd;
                            access$40037.mAF_PasswordSuccCnt = (short) (access$40037.mAF_PasswordSuccCnt + 1);
                        } else {
                            WifiProStatisticsRecord access$40038 = WifiProStatisticsManager.this.mNewestStatRcd;
                            access$40038.mAF_PasswordFailCnt = (short) (access$40038.mAF_PasswordFailCnt + 1);
                        }
                    } else if (2 == type) {
                        if (1 == state) {
                            WifiProStatisticsRecord access$40039 = WifiProStatisticsManager.this.mNewestStatRcd;
                            access$40039.mAF_AutoLoginSuccCnt = (short) (access$40039.mAF_AutoLoginSuccCnt + 1);
                        } else {
                            WifiProStatisticsRecord access$40040 = WifiProStatisticsManager.this.mNewestStatRcd;
                            access$40040.mAF_AutoLoginFailCnt = (short) (access$40040.mAF_AutoLoginFailCnt + 1);
                        }
                    } else if (3 == type) {
                        WifiProStatisticsRecord access$40041 = WifiProStatisticsManager.this.mNewestStatRcd;
                        access$40041.mAF_FPNSuccNotMsmCnt = (short) (access$40041.mAF_FPNSuccNotMsmCnt + 1);
                    }
                    boolean unused12 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_BACK_GRADING_CHR_UPDATE /*136*/:
                    WifiProStatisticsManager.this.updateBGChrProcess(msg.arg1);
                    boolean unused13 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_BQE_GRADING_SVC_CHR_UPDATE /*137*/:
                    WifiProStatisticsManager.this.updateBqeSvcChrProcess(msg.arg1);
                    boolean unused14 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_BQE_BAD_RO_DISCONNECT_MOBILE_DATA /*138*/:
                    WifiProStatisticsRecord access$40042 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40042.mQOE_RO_DISCONNECT_Cnt = (short) (access$40042.mQOE_RO_DISCONNECT_Cnt + 1);
                    int mobileData = WifiProStatisticsManager.this.getRoMobileData();
                    WifiProStatisticsManager.this.logd("qoe ro disconnect mobileData=" + mobileData);
                    WifiProStatisticsRecord access$40043 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40043.mQOE_RO_DISCONNECT_TotData = access$40043.mQOE_RO_DISCONNECT_TotData + mobileData;
                    boolean unused15 = WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                    int unused16 = WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    boolean unused17 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_RO_DISCONNECT_MOBILE_DATA /*139*/:
                    WifiProStatisticsRecord access$40044 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40044.mNotInetRO_DISCONNECT_Cnt = (short) (access$40044.mNotInetRO_DISCONNECT_Cnt + 1);
                    int mobileData2 = WifiProStatisticsManager.this.getRoMobileData();
                    WifiProStatisticsManager.this.logd("inet ro disconnect mobileData=" + mobileData2);
                    WifiProStatisticsRecord access$40045 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40045.mNotInetRO_DISCONNECT_TotData = access$40045.mNotInetRO_DISCONNECT_TotData + mobileData2;
                    boolean unused18 = WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                    int unused19 = WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_UPDATE_WIFI_CONNECTION_STATE /*140*/:
                    int wifiState = msg.arg1;
                    if (1 == wifiState) {
                        if (WifiProStatisticsManager.this.mWifiConnectStartTime == 0) {
                            long unused20 = WifiProStatisticsManager.this.mWifiConnectStartTime = SystemClock.elapsedRealtime();
                            WifiProStatisticsManager.this.logd("wifi connect start here.");
                            return;
                        }
                        WifiProStatisticsManager.this.logd("wifi already connected.");
                        return;
                    } else if (2 == wifiState && WifiProStatisticsManager.this.mWifiConnectStartTime != 0) {
                        long connectionTime = SystemClock.elapsedRealtime() - WifiProStatisticsManager.this.mWifiConnectStartTime;
                        if (connectionTime > 0 && connectionTime < WifiProStatisticsManager.MAX_COMMERCIAL_USER_UPLOAD_PERIOD) {
                            WifiProStatisticsRecord access$40046 = WifiProStatisticsManager.this.mNewestStatRcd;
                            access$40046.mTotWifiConnectTime = (int) (((long) access$40046.mTotWifiConnectTime) + (connectionTime / 1000));
                            WifiProStatisticsManager wifiProStatisticsManager2 = WifiProStatisticsManager.this;
                            wifiProStatisticsManager2.logd("acc wifi connection time:" + acctime + " s, total" + WifiProStatisticsManager.this.mNewestStatRcd.mTotWifiConnectTime);
                        }
                        WifiProStatisticsManager.this.logd("wifi connected end.");
                        long unused21 = WifiProStatisticsManager.this.mWifiConnectStartTime = 0;
                        return;
                    } else {
                        return;
                    }
                case WifiProStatisticsManager.STAT_MSG_PORTAL_AP_NOT_AUTO_CONN /*141*/:
                    WifiProStatisticsRecord access$40047 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40047.mPortalNoAutoConnCnt = (short) (access$40047.mPortalNoAutoConnCnt + 1);
                    boolean unused22 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_HOME_AP_ADD_RO_PERIOD_CNT /*142*/:
                    return;
                case WifiProStatisticsManager.STAT_MSG_UPDATE_SATTISTIC_TO_DB /*144*/:
                    if (WifiProStatisticsManager.this.mNeedSaveCHRStatistic) {
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        boolean unused23 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = false;
                        return;
                    }
                    return;
                case WifiProStatisticsManager.STAT_MSG_ACTIVE_CHECK_RS_DIFF /*145*/:
                    WifiProStatisticsRecord access$40048 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40048.mActiveCheckRS_Diff = (short) (access$40048.mActiveCheckRS_Diff + 1);
                    boolean unused24 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    WifiProStatisticsManager.this.mWiFiCHRMgr.updateBG_AC_DiffType(msg.arg1);
                    WifiProStatisticsManager.this.mWiFiCHRMgr.updateSSID(WifiProStatisticsManager.this.mBG_AP_SSID);
                    WifiProStatisticsManager.this.mWiFiCHRMgr.updateWifiException(122, "BG_AC_RS_DIFF");
                    return;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_PORTAL_CONN_COUNT /*146*/:
                    WifiProStatisticsRecord access$40049 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40049.mTotalPortalConnCount = (short) (access$40049.mTotalPortalConnCount + 1);
                    boolean unused25 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_PORTAL_AUTH_SUCC_COUNT /*147*/:
                    WifiProStatisticsRecord access$40050 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40050.mTotalPortalAuthSuccCount = (short) (access$40050.mTotalPortalAuthSuccCount + 1);
                    boolean unused26 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_CONN_BLOCK_PORTAL_COUNT /*148*/:
                    WifiProStatisticsRecord access$40051 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40051.mManualConnBlockPortalCount = (short) (access$40051.mManualConnBlockPortalCount + 1);
                    boolean unused27 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_AC_RS_SAME_COUNT /*149*/:
                    WifiProStatisticsRecord access$40052 = WifiProStatisticsManager.this.mNewestStatRcd;
                    access$40052.mActiveCheckRS_Same = (short) (access$40052.mActiveCheckRS_Same + 1);
                    boolean unused28 = WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                    return;
                case WifiProStatisticsManager.STAT_MSG_INCREASE_HMD_BTN_RI_COUNT /*150*/:
                    if (2 == WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType) {
                        WifiProStatisticsRecord access$40053 = WifiProStatisticsManager.this.mNewestStatRcd;
                        access$40053.mBMD_FiftyM_RI_Count = (short) (access$40053.mBMD_FiftyM_RI_Count + 1);
                    } else if (1 == WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType) {
                        WifiProStatisticsRecord access$40054 = WifiProStatisticsManager.this.mNewestStatRcd;
                        access$40054.mBMD_TenM_RI_Count = (short) (access$40054.mBMD_TenM_RI_Count + 1);
                    }
                    int unused29 = WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                default:
                    WifiProStatisticsManager.this.loge("statistics manager got unknow message.");
                    return;
            }
        }
    }

    private WifiProStatisticsManager(Context context, Looper looper) {
        logd("WifiProStatisticsManager enter.");
        this.mContext = context;
        initStatHandler(looper);
        this.mWiFiCHRMgr = WifiProCHRManager.getInstance();
        this.mNewestStatRcd = new WifiProStatisticsRecord();
        this.mSimpleDateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.mStatHandler.sendEmptyMessage(100);
    }

    public static void initStatisticsManager(Context context, Looper looper) {
        if (mStatisticsManager == null) {
            mStatisticsManager = new WifiProStatisticsManager(context, looper);
        }
    }

    public static WifiProStatisticsManager getInstance() {
        if (mStatisticsManager == null) {
            mStatisticsManager = new WifiProStatisticsManager(null, null);
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

    /* access modifiers changed from: private */
    public void loadStatDBRecord(WifiProStatisticsRecord statRecord) {
        if (statRecord == null || this.mDataBaseManager == null || this.mStatHandler == null) {
            loge("loadStatDBRecord null error.");
        } else if (this.mDataBaseManager.queryChrStatRcd(statRecord) && this.mNewestStatRcd.mLastStatUploadTime.equals("DEAULT_STR")) {
            logi("Not record in database now.");
            resetStatRecord(statRecord, "new phone, save first record.");
        } else if (!checkDateValid(statRecord.mLastStatUploadTime) || !checkDateValid(statRecord.mLastWifiproStateUpdateTime)) {
            resetStatRecord(statRecord, "date invalid, save new record.");
        } else {
            logd("get record from database success.");
        }
    }

    /* access modifiers changed from: private */
    public long calcTimeInterval(String oldTimeStr, Date nowDate) {
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

    /* access modifiers changed from: private */
    public boolean checkIfNeedUpload(WifiProStatisticsRecord statRecord) {
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
        logd("checkIfNeedUpload time left(mins):" + ((intervalTime - statIntervalMinutes) / HidataWechatTraffic.MIN_VALID_TIME));
        return false;
    }

    /* access modifiers changed from: private */
    public void resetStatRecord(WifiProStatisticsRecord statRecord, String reasonStr) {
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

    /* access modifiers changed from: private */
    public boolean uploadStatisticsCHREvent(WifiProStatisticsRecord statRecord) {
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
            long statIntervalMinutes2 = statIntervalMinutes / HidataWechatTraffic.MIN_VALID_TIME;
            if (statRecord.mLastWifiproState == 1) {
                long enableTotTime = calcTimeInterval(this.mNewestStatRcd.mLastWifiproStateUpdateTime, currDate);
                if (enableTotTime <= -1) {
                    resetStatRecord(statRecord, "LastWifiproStateUpdateTime time record invalid");
                    return false;
                }
                logd("uploadStatisticsCHREvent currDateStr:" + currDateStr + ", last en minutes:" + ((enableTotTime / 1000) / 60));
                statRecord.mEnableTotTime = (int) (((long) statRecord.mEnableTotTime) + (enableTotTime / 1000));
            }
            long enableTotTime2 = (long) (statRecord.mEnableTotTime / SECONDS_OF_ONE_MINUTE);
            if (statIntervalMinutes2 == 0 || statIntervalMinutes2 > 2147483632 || enableTotTime2 > 2147483632) {
                resetStatRecord(statRecord, "interval time abnormal data record invalid");
                return false;
            }
            if (statIntervalMinutes2 < enableTotTime2) {
                statIntervalMinutes2 = enableTotTime2;
            }
            if (enableTotTime2 != 0) {
                statRecord.mHistoryTotWifiConnHour += statRecord.mTotWifiConnectTime;
                int i = statRecord.mHistoryTotWifiConnHour / 3600;
                if (this.mGetApRecordCountCallBack != null) {
                    this.mGetApRecordCountCallBack.statisticApInfoRecord();
                    statRecord.mTotAPRecordCnt = (short) this.mGetApRecordCountCallBack.getTotRecordCount();
                    statRecord.mTotHomeAPCnt = (short) this.mGetApRecordCountCallBack.getHomeApRecordCount();
                }
                logi("upload stat CHR, curr date:" + currDateStr + ", interval mins:" + statIntervalMinutes2 + ", tot ap record:" + statRecord.mTotAPRecordCnt + ", tot home ap record:" + statRecord.mTotHomeAPCnt);
                statRecord.mWifiproStateAtReportTime = getWifiproState();
                Bundle wifiproStatPara = new Bundle();
                wifiproStatPara.putInt("mWifiOobInitState", statRecord.mWifiOobInitState);
                wifiproStatPara.putInt("mWifiproOpenCount", statRecord.mWifiproOpenCount);
                wifiproStatPara.putInt("mCellAutoOpenCount", statRecord.mCellAutoOpenCount);
                wifiproStatPara.putInt("mWifiToWifiSuccCount", statRecord.mWifiToWifiSuccCount);
                wifiproStatPara.putInt("mTotalBQE_BadROC", statRecord.mTotalBQE_BadROC);
                wifiproStatPara.putInt("mManualBackROC", statRecord.mManualBackROC);
                wifiproStatPara.putInt("mSelectNotInetAPCount", statRecord.mSelectNotInetAPCount);
                wifiproStatPara.putInt("mNotInetWifiToWifiCount", statRecord.mNotInetWifiToWifiCount);
                wifiproStatPara.putInt("mReopenWifiRICount", statRecord.mReopenWifiRICount);
                wifiproStatPara.putInt("mBG_FreeInetOkApCnt", statRecord.mBG_FreeInetOkApCnt);
                wifiproStatPara.putInt("mBG_FishingApCnt", statRecord.mBG_FishingApCnt);
                wifiproStatPara.putInt("mBG_FreeNotInetApCnt", statRecord.mBG_FreeNotInetApCnt);
                wifiproStatPara.putInt("mBG_PortalApCnt", statRecord.mBG_PortalApCnt);
                wifiproStatPara.putInt("mBG_FailedCnt", statRecord.mBG_FailedCnt);
                wifiproStatPara.putInt("mBG_UserSelApFishingCnt", statRecord.mBG_UserSelApFishingCnt);
                wifiproStatPara.putInt("mBG_UserSelNoInetCnt", statRecord.mBG_UserSelNoInetCnt);
                wifiproStatPara.putInt("mBG_UserSelPortalCnt", statRecord.mBG_UserSelPortalCnt);
                wifiproStatPara.putInt("mManualConnBlockPortalCount", statRecord.mManualConnBlockPortalCount);
                HwWifiCHRService chrInstance = HwWifiServiceFactory.getHwWifiCHRService();
                if (chrInstance != null) {
                    chrInstance.uploadDFTEvent(909002032, wifiproStatPara);
                }
            } else {
                logd("wifipro not enable at all, not upload stat CHR. curr Date:" + currDateStr + ", last upload Date" + this.mNewestStatRcd.mLastStatUploadTime);
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
            short state = 2;
            if (boolState) {
                state = 1;
                logi("updateWifiproState rs: enable");
            } else {
                logi("updateWifiproState rs: disable");
            }
            sendStatMsg(103, state, 0);
        }
    }

    /* access modifiers changed from: private */
    public short getWifiproState() {
        if (!checkInitOk()) {
            return 0;
        }
        short state = 2;
        if (WifiProCommonUtils.isWifiProSwitchOn(this.mContext)) {
            state = 1;
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

    /* access modifiers changed from: private */
    public int getMobileSignalLevel() {
        if (this.mGetMobileInfoCallBack == null) {
            return -1;
        }
        int mobileSignalLevel = this.mGetMobileInfoCallBack.onGetMobileSignalLevel();
        logd("getMobileSignalLevel new level:" + mobileSignalLevel);
        return mobileSignalLevel;
    }

    /* access modifiers changed from: private */
    public int getMobileRATType() {
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
            logd("uploadPortalAutoFillStatus enter. state=" + success + ", type=" + type);
            sendStatMsg(STAT_MSG_AF_CHR_UPDATE, (int) success, type);
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
            logd("increaseNoInetRemindCount enter. isConnAlarm = " + isConnAlarm);
            sendStatMsg(118, isConnAlarm, 0);
        }
    }

    public void increaseUserUseBgScanAPCount() {
        if (checkInitOk()) {
            logd("increaseUserUseBgScanAPCount enter.");
            sendStatEmptyMsg(119);
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
            sendStatEmptyMsg(125);
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
            sendStatEmptyMsg(123);
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

    /* access modifiers changed from: private */
    public void logd(String msg) {
        if (printLogLevel <= 1) {
            Log.d(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    public void logi(String msg) {
        if (printLogLevel <= 2) {
            Log.i(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    public void loge(String msg) {
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

    /* access modifiers changed from: private */
    public void roveOutEventProcess(int roReason, int bqeRoReasonFlag) {
        logi("roveOutEventProcess enter, RO reason:" + roReason + ", BQE RO reason:" + bqeRoReasonFlag);
        if (1 == roReason) {
            if ((bqeRoReasonFlag & 1) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRSSI_RO_Tot = (short) (wifiProStatisticsRecord.mRSSI_RO_Tot + 1);
            }
            if ((bqeRoReasonFlag & 2) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord2 = this.mNewestStatRcd;
                wifiProStatisticsRecord2.mOTA_RO_Tot = (short) (wifiProStatisticsRecord2.mOTA_RO_Tot + 1);
            }
            if ((bqeRoReasonFlag & 4) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord3 = this.mNewestStatRcd;
                wifiProStatisticsRecord3.mTCP_RO_Tot = (short) (wifiProStatisticsRecord3.mTCP_RO_Tot + 1);
            }
            if ((bqeRoReasonFlag & 8) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord4 = this.mNewestStatRcd;
                wifiProStatisticsRecord4.mBigRTT_RO_Tot = (short) (wifiProStatisticsRecord4.mBigRTT_RO_Tot + 1);
            }
            WifiProStatisticsRecord wifiProStatisticsRecord5 = this.mNewestStatRcd;
            wifiProStatisticsRecord5.mTotalBQE_BadROC = (short) (wifiProStatisticsRecord5.mTotalBQE_BadROC + 1);
            this.mDataBaseManager.addOrUpdateChrStatRcd(this.mNewestStatRcd);
        }
        this.mROReason = roReason;
        this.mROTime = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: private */
    public int getRoMobileData() {
        if (this.mGetMobileInfoCallBack != null) {
            return this.mGetMobileInfoCallBack.getTotalRoMobileData();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public void roveInEventProcess(int riReason, int bqeRiReasonFlag, int bqeRoReasonFlag, int roReason) {
        long passTime;
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
                WifiProStatisticsRecord wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRSSI_RestoreRI_Count = (short) (wifiProStatisticsRecord.mRSSI_RestoreRI_Count + 1);
            } else if (2 == bqeRiReasonFlag) {
                WifiProStatisticsRecord wifiProStatisticsRecord2 = this.mNewestStatRcd;
                wifiProStatisticsRecord2.mRSSI_BetterRI_Count = (short) (wifiProStatisticsRecord2.mRSSI_BetterRI_Count + 1);
            } else if (3 == bqeRiReasonFlag) {
                WifiProStatisticsRecord wifiProStatisticsRecord3 = this.mNewestStatRcd;
                wifiProStatisticsRecord3.mTimerRI_Count = (short) (wifiProStatisticsRecord3.mTimerRI_Count + 1);
            }
            WifiProStatisticsRecord wifiProStatisticsRecord4 = this.mNewestStatRcd;
            wifiProStatisticsRecord4.mAutoRI_TotCount = (short) (wifiProStatisticsRecord4.mAutoRI_TotCount + 1);
            WifiProStatisticsRecord wifiProStatisticsRecord5 = this.mNewestStatRcd;
            wifiProStatisticsRecord5.mAutoRI_TotTime = (int) (((long) wifiProStatisticsRecord5.mAutoRI_TotTime) + passTime);
            this.mNewestStatRcd.mQOE_AutoRI_TotData += getRoMobileData();
            needUpdateDB = true;
        } else if (6 == riReason) {
            WifiProStatisticsRecord wifiProStatisticsRecord6 = this.mNewestStatRcd;
            wifiProStatisticsRecord6.mHisScoRI_Count = (short) (wifiProStatisticsRecord6.mHisScoRI_Count + 1);
            needUpdateDB = true;
        }
        int roDataKB = 0;
        boolean isManualRI = 3 == riReason || 4 == riReason || 5 == riReason;
        boolean isUserCancelRI = 2 == riReason;
        if (1 == roReason) {
            if (isUserCancelRI || isManualRI) {
                logd("abnormal BQE bad rove out statistics process.");
                if ((bqeRoReasonFlag & 1) != 0) {
                    WifiProStatisticsRecord wifiProStatisticsRecord7 = this.mNewestStatRcd;
                    wifiProStatisticsRecord7.mRSSI_ErrRO_Tot = (short) (wifiProStatisticsRecord7.mRSSI_ErrRO_Tot + 1);
                }
                if ((bqeRoReasonFlag & 2) != 0) {
                    WifiProStatisticsRecord wifiProStatisticsRecord8 = this.mNewestStatRcd;
                    wifiProStatisticsRecord8.mOTA_ErrRO_Tot = (short) (wifiProStatisticsRecord8.mOTA_ErrRO_Tot + 1);
                }
                if ((bqeRoReasonFlag & 4) != 0) {
                    WifiProStatisticsRecord wifiProStatisticsRecord9 = this.mNewestStatRcd;
                    wifiProStatisticsRecord9.mTCP_ErrRO_Tot = (short) (wifiProStatisticsRecord9.mTCP_ErrRO_Tot + 1);
                }
                if ((bqeRoReasonFlag & 8) != 0) {
                    WifiProStatisticsRecord wifiProStatisticsRecord10 = this.mNewestStatRcd;
                    wifiProStatisticsRecord10.mBigRTT_ErrRO_Tot = (short) (wifiProStatisticsRecord10.mBigRTT_ErrRO_Tot + 1);
                }
                if (passTime > 32760) {
                    passTime = 32760;
                }
                sendUnexpectedROParaCHREvent(this.mRoveOutPara, (short) ((int) passTime));
            }
            if (isManualRI) {
                WifiProStatisticsRecord wifiProStatisticsRecord11 = this.mNewestStatRcd;
                wifiProStatisticsRecord11.mManualRI_TotTime = (int) (((long) wifiProStatisticsRecord11.mManualRI_TotTime) + passTime);
                WifiProStatisticsRecord wifiProStatisticsRecord12 = this.mNewestStatRcd;
                wifiProStatisticsRecord12.mManualBackROC = (short) (wifiProStatisticsRecord12.mManualBackROC + 1);
                if (this.mGetMobileInfoCallBack != null) {
                    roDataKB = this.mGetMobileInfoCallBack.getTotalRoMobileData();
                }
                if (4 == riReason) {
                    WifiProStatisticsRecord wifiProStatisticsRecord13 = this.mNewestStatRcd;
                    wifiProStatisticsRecord13.mTotBtnRICount = (short) (wifiProStatisticsRecord13.mTotBtnRICount + 1);
                }
                this.mNewestStatRcd.mRO_TotMobileData += roDataKB;
            }
            if (isUserCancelRI) {
                WifiProStatisticsRecord wifiProStatisticsRecord14 = this.mNewestStatRcd;
                wifiProStatisticsRecord14.mUserCancelROC = (short) (wifiProStatisticsRecord14.mUserCancelROC + 1);
            }
            needUpdateDB = true;
        }
        if (needUpdateDB) {
            this.mDataBaseManager.addOrUpdateChrStatRcd(this.mNewestStatRcd);
        }
    }

    private void sendUnexpectedROParaCHREvent(WifiProRoveOutParaRecord roveOutPara, short roTime) {
        WifiProRoveOutParaRecord wifiProRoveOutParaRecord = roveOutPara;
        if (wifiProRoveOutParaRecord != null) {
            logi("unexpected RO para CHR event send.");
            wifiProRoveOutParaRecord.mRO_Duration = roTime;
            this.mWiFiCHRMgr.updateExcpRoParaPart1(wifiProRoveOutParaRecord.mRSSI_VALUE, wifiProRoveOutParaRecord.mOTA_PacketDropRate, wifiProRoveOutParaRecord.mRttAvg, wifiProRoveOutParaRecord.mTcpInSegs, wifiProRoveOutParaRecord.mTcpOutSegs, wifiProRoveOutParaRecord.mTcpRetransSegs, wifiProRoveOutParaRecord.mWIFI_NetSpeed, wifiProRoveOutParaRecord.mIPQLevel);
            this.mWiFiCHRMgr.updateExcpRoParaPart2(wifiProRoveOutParaRecord.mRO_APSsid, wifiProRoveOutParaRecord.mMobileSignalLevel, wifiProRoveOutParaRecord.mRATType, wifiProRoveOutParaRecord.mHistoryQuilityRO_Rate, wifiProRoveOutParaRecord.mHighDataRateRO_Rate, wifiProRoveOutParaRecord.mCreditScoreRO_Rate, wifiProRoveOutParaRecord.mRO_Duration);
            this.mWiFiCHRMgr.updateWifiException(122, "ROVE_OUT_PARAMETER");
            return;
        }
        short s = roTime;
    }

    /* access modifiers changed from: private */
    public void sendPingpongCHREvent(WifiProRoveOutParaRecord roveOutPara, short roTime) {
        WifiProRoveOutParaRecord wifiProRoveOutParaRecord = roveOutPara;
        if (wifiProRoveOutParaRecord != null) {
            logi("Pingpong RO para CHR event send.");
            wifiProRoveOutParaRecord.mRO_Duration = roTime;
            this.mWiFiCHRMgr.updateExcpRoParaPart1(wifiProRoveOutParaRecord.mRSSI_VALUE, wifiProRoveOutParaRecord.mOTA_PacketDropRate, wifiProRoveOutParaRecord.mRttAvg, wifiProRoveOutParaRecord.mTcpInSegs, wifiProRoveOutParaRecord.mTcpOutSegs, wifiProRoveOutParaRecord.mTcpRetransSegs, wifiProRoveOutParaRecord.mWIFI_NetSpeed, wifiProRoveOutParaRecord.mIPQLevel);
            this.mWiFiCHRMgr.updateExcpRoParaPart2(wifiProRoveOutParaRecord.mRO_APSsid, wifiProRoveOutParaRecord.mMobileSignalLevel, wifiProRoveOutParaRecord.mRATType, wifiProRoveOutParaRecord.mHistoryQuilityRO_Rate, wifiProRoveOutParaRecord.mHighDataRateRO_Rate, wifiProRoveOutParaRecord.mCreditScoreRO_Rate, wifiProRoveOutParaRecord.mRO_Duration);
            this.mWiFiCHRMgr.updateWifiException(122, "SWITCH_PINGPONG");
            return;
        }
        short s = roTime;
    }

    private void initStatHandler(Looper looper) {
        if (looper == null) {
            logi("looper null, force create single thread");
            HandlerThread thread = new HandlerThread("StatisticsCHRMsgHandler");
            thread.start();
            looper = thread.getLooper();
        }
        this.mStatHandler = new StatisticsCHRMsgHandler(looper);
    }

    /* access modifiers changed from: private */
    public void sendCheckUploadMsg() {
        if (this.mStatHandler.hasMessages(101)) {
            loge("There has CHECK_NEED_UPLOAD_EVENT msg at queue.");
        } else if (DEBUG_MODE) {
            sendStatEmptyMsgDelayed(101, HidataWechatTraffic.MIN_VALID_TIME);
        } else {
            sendStatEmptyMsgDelayed(101, 1800000);
        }
    }

    /* access modifiers changed from: private */
    public void checkMsgLoopRunning() {
        if (!this.mStatHandler.hasMessages(101)) {
            logd("restart msg Loop.");
            sendStatEmptyMsg(101);
            return;
        }
        logd("msg Loop is running.");
    }

    /* access modifiers changed from: private */
    public void updateBGChrProcess(int bgChrType) {
        logd("updateBGChrProcess enter for bg type:" + bgChrType);
        switch (bgChrType) {
            case 1:
                WifiProStatisticsRecord wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mBG_BgRunCnt = (short) (wifiProStatisticsRecord.mBG_BgRunCnt + 1);
                return;
            case 2:
                return;
            case 3:
                WifiProStatisticsRecord wifiProStatisticsRecord2 = this.mNewestStatRcd;
                wifiProStatisticsRecord2.mBG_FreeInetOkApCnt = (short) (wifiProStatisticsRecord2.mBG_FreeInetOkApCnt + 1);
                return;
            case 4:
                WifiProStatisticsRecord wifiProStatisticsRecord3 = this.mNewestStatRcd;
                wifiProStatisticsRecord3.mBG_FishingApCnt = (short) (wifiProStatisticsRecord3.mBG_FishingApCnt + 1);
                return;
            case 5:
                WifiProStatisticsRecord wifiProStatisticsRecord4 = this.mNewestStatRcd;
                wifiProStatisticsRecord4.mBG_FreeNotInetApCnt = (short) (wifiProStatisticsRecord4.mBG_FreeNotInetApCnt + 1);
                return;
            case 6:
                WifiProStatisticsRecord wifiProStatisticsRecord5 = this.mNewestStatRcd;
                wifiProStatisticsRecord5.mBG_PortalApCnt = (short) (wifiProStatisticsRecord5.mBG_PortalApCnt + 1);
                return;
            case 7:
                WifiProStatisticsRecord wifiProStatisticsRecord6 = this.mNewestStatRcd;
                wifiProStatisticsRecord6.mBG_UserSelFreeInetOkCnt = (short) (wifiProStatisticsRecord6.mBG_UserSelFreeInetOkCnt + 1);
                return;
            case 8:
                WifiProStatisticsRecord wifiProStatisticsRecord7 = this.mNewestStatRcd;
                wifiProStatisticsRecord7.mBG_UserSelNoInetCnt = (short) (wifiProStatisticsRecord7.mBG_UserSelNoInetCnt + 1);
                return;
            case 9:
                WifiProStatisticsRecord wifiProStatisticsRecord8 = this.mNewestStatRcd;
                wifiProStatisticsRecord8.mBG_UserSelPortalCnt = (short) (wifiProStatisticsRecord8.mBG_UserSelPortalCnt + 1);
                return;
            case 10:
                WifiProStatisticsRecord wifiProStatisticsRecord9 = this.mNewestStatRcd;
                wifiProStatisticsRecord9.mBG_FoundTwoMoreApCnt = (short) (wifiProStatisticsRecord9.mBG_FoundTwoMoreApCnt + 1);
                return;
            case 11:
                WifiProStatisticsRecord wifiProStatisticsRecord10 = this.mNewestStatRcd;
                wifiProStatisticsRecord10.mBG_FailedCnt = (short) (wifiProStatisticsRecord10.mBG_FailedCnt + 1);
                this.mWiFiCHRMgr.updateSSID(this.mBG_AP_SSID);
                this.mWiFiCHRMgr.updateWifiException(122, "BG_FAILED_CNT");
                return;
            default:
                switch (bgChrType) {
                    case 20:
                        WifiProStatisticsRecord wifiProStatisticsRecord11 = this.mNewestStatRcd;
                        wifiProStatisticsRecord11.mBG_NCByConnectFail = (short) (wifiProStatisticsRecord11.mBG_NCByConnectFail + 1);
                        return;
                    case 21:
                        WifiProStatisticsRecord wifiProStatisticsRecord12 = this.mNewestStatRcd;
                        wifiProStatisticsRecord12.mBG_NCByCheckFail = (short) (wifiProStatisticsRecord12.mBG_NCByCheckFail + 1);
                        return;
                    default:
                        loge("updateBGChrProcess error type:" + bgChrType);
                        return;
                }
        }
    }

    /* access modifiers changed from: private */
    public void updateBqeSvcChrProcess(int bqeSvcChrType) {
    }

    public void update24gGameCHR(HwWifiGameNetChrInfo gameChrFor24G) {
        if (gameChrFor24G == null || !checkInitOk()) {
            loge("Game CHR Info is null, can not upload");
            return;
        }
        WifiProStatisticsRecord wifiProStatisticsRecord = this.mNewestStatRcd;
        wifiProStatisticsRecord.mBG_SettingRunCnt = (short) (wifiProStatisticsRecord.mBG_SettingRunCnt + 1);
        WifiProStatisticsRecord wifiProStatisticsRecord2 = this.mNewestStatRcd;
        wifiProStatisticsRecord2.mBG_DHCPFailCnt = (short) (wifiProStatisticsRecord2.mBG_DHCPFailCnt + gameChrFor24G.mWifiDisCounter);
        WifiProStatisticsRecord wifiProStatisticsRecord3 = this.mNewestStatRcd;
        wifiProStatisticsRecord3.mBG_AUTH_FailCnt = (short) (wifiProStatisticsRecord3.mBG_AUTH_FailCnt + gameChrFor24G.mWifiRoamingCounter);
        WifiProStatisticsRecord wifiProStatisticsRecord4 = this.mNewestStatRcd;
        wifiProStatisticsRecord4.mBG_AssocRejectCnt = (short) (wifiProStatisticsRecord4.mBG_AssocRejectCnt + gameChrFor24G.mWifiScanCounter);
        WifiProStatisticsRecord wifiProStatisticsRecord5 = this.mNewestStatRcd;
        wifiProStatisticsRecord5.mHomeAPAddRoPeriodCnt = (short) (wifiProStatisticsRecord5.mHomeAPAddRoPeriodCnt + gameChrFor24G.mBTScan24GCounter);
        if (gameChrFor24G.mAP24gBTCoexist) {
            WifiProStatisticsRecord wifiProStatisticsRecord6 = this.mNewestStatRcd;
            wifiProStatisticsRecord6.mBG_DNSFailCnt = (short) (wifiProStatisticsRecord6.mBG_DNSFailCnt + 1);
        }
        WifiProStatisticsRecord wifiProStatisticsRecord7 = this.mNewestStatRcd;
        wifiProStatisticsRecord7.mBG_NCByStateErr = (short) (wifiProStatisticsRecord7.mBG_NCByStateErr + gameChrFor24G.mNetworkSmoothDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord8 = this.mNewestStatRcd;
        wifiProStatisticsRecord8.mBG_NCByUnknown = (short) (wifiProStatisticsRecord8.mBG_NCByUnknown + gameChrFor24G.mNetworkGeneralDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord9 = this.mNewestStatRcd;
        wifiProStatisticsRecord9.mBQE_CNUrl1FailCount = (short) (wifiProStatisticsRecord9.mBQE_CNUrl1FailCount + gameChrFor24G.mNetworkPoorDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord10 = this.mNewestStatRcd;
        wifiProStatisticsRecord10.mBQE_CNUrl2FailCount = (short) (wifiProStatisticsRecord10.mBQE_CNUrl2FailCount + gameChrFor24G.mArpRttSmoothDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord11 = this.mNewestStatRcd;
        wifiProStatisticsRecord11.mBQE_CNUrl3FailCount = (short) (wifiProStatisticsRecord11.mBQE_CNUrl3FailCount + gameChrFor24G.mArpRttGeneralDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord12 = this.mNewestStatRcd;
        wifiProStatisticsRecord12.mBQE_NCNUrl1FailCount = (short) (wifiProStatisticsRecord12.mBQE_NCNUrl1FailCount + gameChrFor24G.mArpRttPoorDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord13 = this.mNewestStatRcd;
        wifiProStatisticsRecord13.mBQE_NCNUrl2FailCount = (short) (wifiProStatisticsRecord13.mBQE_NCNUrl2FailCount + gameChrFor24G.mTcpRttSmoothDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord14 = this.mNewestStatRcd;
        wifiProStatisticsRecord14.mBQE_NCNUrl3FailCount = (short) (wifiProStatisticsRecord14.mBQE_NCNUrl3FailCount + gameChrFor24G.mTcpRttGeneralDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord15 = this.mNewestStatRcd;
        wifiProStatisticsRecord15.mBQE_ScoreUnknownCount = (short) (wifiProStatisticsRecord15.mBQE_ScoreUnknownCount + gameChrFor24G.mTcpRttPoorDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord16 = this.mNewestStatRcd;
        wifiProStatisticsRecord16.mBQE_BindWlanFailCount = (short) (wifiProStatisticsRecord16.mBQE_BindWlanFailCount + gameChrFor24G.mTcpRttBadDuration);
        this.mNeedSaveCHRStatistic = true;
    }

    public void update5gGameCHR(HwWifiGameNetChrInfo gameChrFor5G) {
        if (gameChrFor5G == null || !checkInitOk()) {
            loge("Game CHR Info is null, can not upload");
            return;
        }
        WifiProStatisticsRecord wifiProStatisticsRecord = this.mNewestStatRcd;
        wifiProStatisticsRecord.mBG_SettingRunCnt = (short) (wifiProStatisticsRecord.mBG_SettingRunCnt + 1);
        WifiProStatisticsRecord wifiProStatisticsRecord2 = this.mNewestStatRcd;
        wifiProStatisticsRecord2.mBG_DHCPFailCnt = (short) (wifiProStatisticsRecord2.mBG_DHCPFailCnt + gameChrFor5G.mWifiDisCounter);
        WifiProStatisticsRecord wifiProStatisticsRecord3 = this.mNewestStatRcd;
        wifiProStatisticsRecord3.mBG_AUTH_FailCnt = (short) (wifiProStatisticsRecord3.mBG_AUTH_FailCnt + gameChrFor5G.mWifiRoamingCounter);
        WifiProStatisticsRecord wifiProStatisticsRecord4 = this.mNewestStatRcd;
        wifiProStatisticsRecord4.mBG_AssocRejectCnt = (short) (wifiProStatisticsRecord4.mBG_AssocRejectCnt + gameChrFor5G.mWifiScanCounter);
        WifiProStatisticsRecord wifiProStatisticsRecord5 = this.mNewestStatRcd;
        wifiProStatisticsRecord5.mBG_InetNotOkActiveOk = (short) (wifiProStatisticsRecord5.mBG_InetNotOkActiveOk + gameChrFor5G.mTcpRttSmoothDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord6 = this.mNewestStatRcd;
        wifiProStatisticsRecord6.mBG_InetOkActiveNotOk = (short) (wifiProStatisticsRecord6.mBG_InetOkActiveNotOk + gameChrFor5G.mTcpRttGeneralDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord7 = this.mNewestStatRcd;
        wifiProStatisticsRecord7.mBG_UserSelApFishingCnt = (short) (wifiProStatisticsRecord7.mBG_UserSelApFishingCnt + gameChrFor5G.mTcpRttPoorDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord8 = this.mNewestStatRcd;
        wifiProStatisticsRecord8.mBG_ConntTimeoutCnt = (short) (wifiProStatisticsRecord8.mBG_ConntTimeoutCnt + gameChrFor5G.mTcpRttBadDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord9 = this.mNewestStatRcd;
        wifiProStatisticsRecord9.mBSG_RsGoodCnt = (short) (wifiProStatisticsRecord9.mBSG_RsGoodCnt + gameChrFor5G.mNetworkSmoothDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord10 = this.mNewestStatRcd;
        wifiProStatisticsRecord10.mBSG_RsMidCnt = (short) (wifiProStatisticsRecord10.mBSG_RsMidCnt + gameChrFor5G.mNetworkGeneralDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord11 = this.mNewestStatRcd;
        wifiProStatisticsRecord11.mBSG_RsBadCnt = (short) (wifiProStatisticsRecord11.mBSG_RsBadCnt + gameChrFor5G.mNetworkPoorDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord12 = this.mNewestStatRcd;
        wifiProStatisticsRecord12.mBSG_EndIn4sCnt = (short) (wifiProStatisticsRecord12.mBSG_EndIn4sCnt + gameChrFor5G.mArpRttSmoothDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord13 = this.mNewestStatRcd;
        wifiProStatisticsRecord13.mBSG_EndIn4s7sCnt = (short) (wifiProStatisticsRecord13.mBSG_EndIn4s7sCnt + gameChrFor5G.mArpRttGeneralDuration);
        WifiProStatisticsRecord wifiProStatisticsRecord14 = this.mNewestStatRcd;
        wifiProStatisticsRecord14.mBSG_NotEndIn7sCnt = (short) (wifiProStatisticsRecord14.mBSG_NotEndIn7sCnt + gameChrFor5G.mArpRttPoorDuration);
        if (gameChrFor5G.mAP5gOnly) {
            WifiProStatisticsRecord wifiProStatisticsRecord15 = this.mNewestStatRcd;
            wifiProStatisticsRecord15.mHomeAPQoeBadCnt = (short) (wifiProStatisticsRecord15.mHomeAPQoeBadCnt + 1);
        }
        this.mNeedSaveCHRStatistic = true;
    }

    public void uploadWifiproEvent(int eventId) {
        int mReason;
        HwWifiConnectivityMonitor mWifiConnectivityMonitor = HwWifiConnectivityMonitor.getInstance();
        if (mWifiConnectivityMonitor != null) {
            List<String> mCircleStat = mWifiConnectivityMonitor.getCircleStat();
            if (eventId == 909002057) {
                mReason = mWifiConnectivityMonitor.getNotifyWifiLinkPoorReason();
            } else {
                mReason = mWifiConnectivityMonitor.getStopWifiSwitchReason();
            }
            HwWifiCHRService chrInstance = HwWifiServiceFactory.getHwWifiCHRService();
            if (!(chrInstance == null || mCircleStat == null)) {
                int circleStatSize = mCircleStat.size();
                Bundle data = new Bundle();
                logd("uploadWifiproEvent  reason: " + mReason);
                data.putInt("reason", mReason);
                for (int i = 0; i < circleStatSize; i++) {
                    logd("uploadWifiproEvent circle-" + i + ": " + mCircleStat.get(i));
                    StringBuilder sb = new StringBuilder();
                    sb.append("circle-");
                    sb.append(i);
                    data.putString(sb.toString(), mCircleStat.get(i));
                }
                chrInstance.uploadDFTEvent(eventId, data);
            }
            mWifiConnectivityMonitor.resetCircleStat();
        }
    }
}
