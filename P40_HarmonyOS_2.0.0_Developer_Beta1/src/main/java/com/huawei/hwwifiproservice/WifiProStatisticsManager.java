package com.huawei.hwwifiproservice;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiLinkLayerStats;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private static final int DEBUG_MODE_CHECK_UPLOAD_TIME_INTERVAL = 60000;
    private static final long DEBUG_MODE_UPLOAD_PERIOD = 480000;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int ERROR_TIME_INTERVAL = -1;
    private static final String EVENT_DATA = "eventData";
    private static final String EVENT_ID = "eventId";
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
    private static final String RX_GOOD = "rxGood";
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
    private static final int TCP_STAT_MAX_SIZE = 2;
    private static final String TOP_UID_TCP_STAT = "top-uid-tcp-stat-";
    public static final int TRUE_INT_VAL = 1;
    private static final String TX_BAD = "txBad";
    private static final String TX_GOOD = "txGood";
    private static final String TX_RX_TCP_STAT = "tx-rx-tcp-stat-";
    private static final short UNKNOWN_RAT_TYPE = 0;
    private static final int VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG = 0;
    private static final int VALUE_WIFI_TO_PDP_AUTO_HANDOVER_MOBILE = 1;
    private static final int VALUE_WIFI_TO_PDP_CANNOT_HANDOVER_MOBILE = 2;
    public static final int WIFI_STATE_CONNECTED = 1;
    public static final int WIFI_STATE_DISCONNECTED = 2;
    private static boolean debugMode = false;
    private static WifiProStatisticsManager mStatisticsManager;
    private static int printLogLevel = 1;
    private int mBQEROReasonFlag = 0;
    private int mBQERoveInReason = 0;
    private String mBgApSsid;
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
    private final Object mStatLock = new Object();
    private List<String> mTxRxTcpStat = new ArrayList(2);
    private long mWifiConnectStartTime = 0;

    private WifiProStatisticsManager(Context context, Looper looper) {
        logD("WifiProStatisticsManager enter.");
        this.mContext = context;
        initStatHandler(looper);
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
        SimpleDateFormat simpleDateFormat = this.mSimpleDateFmt;
        if (simpleDateFormat == null || dateStr == null) {
            return false;
        }
        try {
            objDate = simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            logE("checkDateValid date string invalid:" + dateStr);
        }
        if (objDate == null) {
            return false;
        }
        logI("date string valid: " + dateStr);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadStatDBRecord(WifiProStatisticsRecord statRecord) {
        WifiProChrDataBaseManager wifiProChrDataBaseManager;
        if (statRecord == null || (wifiProChrDataBaseManager = this.mDataBaseManager) == null || this.mStatHandler == null) {
            logE("loadStatDBRecord null error.");
        } else if (wifiProChrDataBaseManager.queryChrStatRcd(statRecord) && this.mNewestStatRcd.mLastStatUploadTime.equals("DEAULT_STR")) {
            logI("Not record in database now.");
            resetStatRecord(statRecord, "new phone, save first record.");
        } else if (!checkDateValid(statRecord.mLastStatUploadTime) || !checkDateValid(statRecord.mLastWifiproStateUpdateTime)) {
            resetStatRecord(statRecord, "date invalid, save new record.");
        } else {
            logI("get record from database success.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long calcTimeInterval(String oldTimeStr, Date nowDate) {
        Date oldDate = null;
        SimpleDateFormat simpleDateFormat = this.mSimpleDateFmt;
        if (simpleDateFormat == null || oldTimeStr == null || nowDate == null) {
            return -1;
        }
        try {
            oldDate = simpleDateFormat.parse(oldTimeStr);
        } catch (ParseException ex) {
            logE("There has exception in Date parse" + ex);
        }
        if (oldDate == null) {
            return -1;
        }
        return nowDate.getTime() - oldDate.getTime();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkIfNeedUpload(WifiProStatisticsRecord statRecord) {
        if (statRecord == null || this.mDataBaseManager == null) {
            logE("checkIfNeedUpload null error.");
            return false;
        }
        Date currDate = new Date();
        long statIntervalMinutes = calcTimeInterval(statRecord.mLastStatUploadTime, currDate);
        if (statIntervalMinutes <= -1) {
            resetStatRecord(statRecord, "checkIfNeedUpload LastStatUploadTime time record invalid");
            return false;
        }
        long intervalTime = COMMERCIAL_USER_UPLOAD_PERIOD;
        if (debugMode) {
            intervalTime = DEBUG_MODE_UPLOAD_PERIOD;
        }
        if (statIntervalMinutes >= MAX_COMMERCIAL_USER_UPLOAD_PERIOD) {
            logI("checkIfNeedUpload too big upload interval , reset start time.");
            statRecord.mLastStatUploadTime = this.mSimpleDateFmt.format(currDate);
            this.mDataBaseManager.addOrUpdateChrStatRcd(statRecord);
        }
        if (statIntervalMinutes >= intervalTime) {
            logI("checkIfNeedUpload ret true.");
            return true;
        }
        logI("checkIfNeedUpload time left(mins):" + ((intervalTime - statIntervalMinutes) / 60000));
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetStatRecord(WifiProStatisticsRecord statRecord, String reasonStr) {
        logI("ChrStatLog resetStatRecord enter, reason:" + reasonStr);
        if (statRecord == null || this.mDataBaseManager == null) {
            logE("resetStatRecord null error.");
            return;
        }
        String currDateStr = this.mSimpleDateFmt.format(new Date());
        statRecord.resetRecord();
        statRecord.mLastStatUploadTime = currDateStr;
        statRecord.mLastWifiproState = getWifiproState();
        statRecord.mLastWifiproStateUpdateTime = currDateStr;
        this.mDataBaseManager.addOrUpdateChrStatRcd(statRecord);
    }

    private void writeStatisticsParam(WifiProStatisticsRecord statRecord) {
        if (statRecord == null || this.mDataBaseManager == null || this.mSimpleDateFmt == null) {
            logE("uploadStatisticsCHREvent null error.");
            return;
        }
        statRecord.mHistoryTotWifiConnHour += statRecord.mTotWifiConnectTime;
        int i = statRecord.mHistoryTotWifiConnHour / 3600;
        IGetApRecordCount iGetApRecordCount = this.mGetApRecordCountCallBack;
        if (iGetApRecordCount != null) {
            iGetApRecordCount.statisticApInfoRecord();
            statRecord.mTotAPRecordCnt = (short) this.mGetApRecordCountCallBack.getTotRecordCount();
            statRecord.mTotHomeAPCnt = (short) this.mGetApRecordCountCallBack.getHomeApRecordCount();
        }
        statRecord.mWifiproStateAtReportTime = getWifiproState();
        Bundle wifiproStatPara = new Bundle();
        wifiproStatPara.putInt("mWifiOobInitState", statRecord.mWifiOobInitState);
        wifiproStatPara.putInt("mWifiproOpenCount", statRecord.mWifiproOpenCount);
        wifiproStatPara.putInt("mCellAutoOpenCount", statRecord.mCellAutoOpenCount);
        wifiproStatPara.putInt("mWifiToWifiSuccCount", statRecord.mWifiToWifiSuccCount);
        wifiproStatPara.putInt("mTotalBQE_BadROC", statRecord.mTotalBqeBadRoc);
        wifiproStatPara.putInt("mManualBackROC", statRecord.mManualBackROC);
        wifiproStatPara.putInt("mSelectNotInetAPCount", statRecord.mSelectNotInetAPCount);
        wifiproStatPara.putInt("mNotInetWifiToWifiCount", statRecord.mNotInetWifiToWifiCount);
        wifiproStatPara.putInt("mReopenWifiRICount", statRecord.mReopenWifiRICount);
        wifiproStatPara.putInt("mBG_FreeInetOkApCnt", statRecord.mBgFreeInetOkApCnt);
        wifiproStatPara.putInt("mBG_FishingApCnt", statRecord.mBgFishingApCnt);
        wifiproStatPara.putInt("mBG_FreeNotInetApCnt", statRecord.mBgFreeNotInetApCnt);
        wifiproStatPara.putInt("mBG_PortalApCnt", statRecord.mBgPortalApCnt);
        wifiproStatPara.putInt("mBG_FailedCnt", statRecord.mBgFailedCnt);
        wifiproStatPara.putInt("mBG_UserSelApFishingCnt", statRecord.mBgUserSelApFishingCnt);
        wifiproStatPara.putInt("mBG_UserSelNoInetCnt", statRecord.mBgUserSelNoInetCnt);
        wifiproStatPara.putInt("mBG_UserSelPortalCnt", statRecord.mBgUserSelPortalCnt);
        wifiproStatPara.putInt("mManualConnBlockPortalCount", statRecord.mManualConnBlockPortalCount);
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, 909002032);
        dftEventData.putBundle(EVENT_DATA, wifiproStatPara);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean uploadStatisticsCHREvent(WifiProStatisticsRecord statRecord) {
        logD("uploadStatisticsCHREvent enter.");
        if (statRecord == null || this.mDataBaseManager == null || this.mSimpleDateFmt == null) {
            logE("uploadStatisticsCHREvent null error.");
            return false;
        } else if (this.mNewestStatRcd.mLastStatUploadTime == null || this.mNewestStatRcd.mLastWifiproStateUpdateTime == null) {
            logE("last upload time error, give up upload.");
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
            long statIntervalMinutes2 = statIntervalMinutes / 60000;
            if (statRecord.mLastWifiproState == 1) {
                long enableTotTime = calcTimeInterval(this.mNewestStatRcd.mLastWifiproStateUpdateTime, currDate);
                if (enableTotTime <= -1) {
                    resetStatRecord(statRecord, "LastWifiproStateUpdateTime time record invalid");
                    return false;
                }
                long enableTotTime2 = enableTotTime / 1000;
                logI("uploadStatisticsCHREvent currDateStr:" + currDateStr + ", last en minutes:" + (enableTotTime2 / 60));
                statRecord.mEnableTotTime = (int) (((long) statRecord.mEnableTotTime) + enableTotTime2);
            }
            long enableTotTime3 = (long) (statRecord.mEnableTotTime / SECONDS_OF_ONE_MINUTE);
            if (statIntervalMinutes2 == 0 || statIntervalMinutes2 > 2147483632 || enableTotTime3 > 2147483632) {
                resetStatRecord(statRecord, "interval time abnormal data record invalid");
                return false;
            }
            if (statIntervalMinutes2 < enableTotTime3) {
                statIntervalMinutes2 = enableTotTime3;
            }
            if (enableTotTime3 != 0) {
                logI("upload stat CHR, curr date:" + currDateStr + ", interval mins:" + statIntervalMinutes2 + ", tot ap record:" + ((int) statRecord.mTotAPRecordCnt) + ", tot home ap record:" + ((int) statRecord.mTotHomeAPCnt));
                writeStatisticsParam(statRecord);
            } else {
                logI("wifipro not enable at all, not upload stat CHR. curr Date:" + currDateStr + ", last upload Date" + this.mNewestStatRcd.mLastStatUploadTime);
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
        logE("checkInitOk null error.");
        return false;
    }

    public void updateInitialWifiproState(boolean boolState) {
        if (checkInitOk()) {
            logI("updateInitialWifiproState enter.");
            sendStatMsg(STAT_MSG_OOB_INIT_STATE, boolState ? 1 : 2, 0);
        }
    }

    public void updateWifiproState(boolean boolState) {
        if (checkInitOk()) {
            short state = 2;
            if (boolState) {
                state = 1;
                logI("updateWifiproState rs: enable");
            } else {
                logI("updateWifiproState rs: disable");
            }
            sendStatMsg(103, state, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private short getWifiproState() {
        if (!checkInitOk()) {
            return 0;
        }
        if (WifiProCommonUtils.isWifiProSwitchOn(this.mContext)) {
            logI("getWifiproState rs: enable");
            return 1;
        }
        logI("getWifiproState rs: disable");
        return 2;
    }

    public void registerMobileInfoCallback(IGetMobileInfoCallBack infoCallback) {
        logI("registerMobileInfoCallback enter.");
        this.mGetMobileInfoCallBack = infoCallback;
    }

    public void registerGetApRecordCountCallBack(IGetApRecordCount callback) {
        logI("registerGetApRecordCountCallBack enter.");
        this.mGetApRecordCountCallBack = callback;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getMobileSignalLevel() {
        IGetMobileInfoCallBack iGetMobileInfoCallBack = this.mGetMobileInfoCallBack;
        if (iGetMobileInfoCallBack == null) {
            return -1;
        }
        int mobileSignalLevel = iGetMobileInfoCallBack.onGetMobileSignalLevel();
        logI("getMobileSignalLevel new level:" + mobileSignalLevel);
        return mobileSignalLevel;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getMobileRATType() {
        IGetMobileInfoCallBack iGetMobileInfoCallBack = this.mGetMobileInfoCallBack;
        if (iGetMobileInfoCallBack == null) {
            return 0;
        }
        int mobileRatType = iGetMobileInfoCallBack.onGetMobileRATType();
        logI("getMobileRATType new type:" + mobileRatType);
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
            logI("setBQERoveOutReason enter, reason:" + this.mBQEROReasonFlag);
            this.mRoveOutPara = roveOutPara;
            sendStatEmptyMsg(STAT_MSG_BQE_RO_PARA_UPDATE);
        }
    }

    public void sendWifiproRoveOutEvent(int reason) {
        if (checkInitOk()) {
            logI("sendWifiproRoveOutEvent enter, reason:" + reason);
            sendStatMsg(STAT_MSG_RO_EVENT, reason, this.mBQEROReasonFlag);
        }
    }

    public void setBQERoveInReason(int roveInReason) {
        if (checkInitOk()) {
            logI("setBQERoveInReason enter, reason:" + roveInReason);
            this.mBQERoveInReason = roveInReason;
        }
    }

    public void sendWifiproRoveInEvent(int riReason) {
        if (checkInitOk()) {
            logI("sendWifiproRoveInEvent enter, reason:" + riReason);
            sendStatMsg(STAT_MSG_RI_EVENT, riReason, this.mBQERoveInReason);
        }
    }

    public void increaseNoInetHandoverCount() {
        if (checkInitOk()) {
            logI("increaseNoInetHandoverCount enter.");
            sendStatEmptyMsg(104);
        }
    }

    public void increaseWiFiHandoverWiFiCount(int handOverType) {
        if (checkInitOk()) {
            logI("increaseWiFiHandoverWiFiCount enter. handOverType =" + handOverType);
            sendStatMsg(STAT_MSG_WIFI_TO_WIFI_SUCC_COUNT, handOverType, 0);
        }
    }

    public void increaseUserReopenWifiRiCount() {
        if (checkInitOk()) {
            logD("increaseUserReopenWifiRiCount enter.");
            sendStatEmptyMsg(STAT_MSG_USER_REOPEN_WIFI_RI_CNT);
        }
    }

    public void increaseSelCspSettingChgCount(int newSettingValue) {
        if (checkInitOk()) {
            logI("increaseSelCspSettingChgCount enter. csp val=" + newSettingValue);
            sendStatMsg(STAT_MSG_SEL_CSP_SETTING_CHG_CNT, newSettingValue, 0);
        }
    }

    public void increaseHMDNotifyCount(int notifyType) {
        if (checkInitOk()) {
            logI("increaseSelCspSettingChgCount enter. notifyType val=" + notifyType);
            sendStatMsg(STAT_MSG_HMD_NOTIFY_CNT, notifyType, 0);
        }
    }

    public void increaseUserDelNotifyCount() {
        if (checkInitOk()) {
            logD("increaseUserDelNotifyCount enter.");
            sendStatEmptyMsg(STAT_MSG_HMD_USER_DEL_NOTIFY_CNT);
        }
    }

    public void increaseHighMobileDataBtnRiCount() {
        if (checkInitOk()) {
            logI("increaseHighMobileDataBtnRiCount enter.");
            sendStatEmptyMsg(STAT_MSG_INCREASE_HMD_BTN_RI_COUNT);
        }
    }

    public void uploadPortalAutoFillStatus(boolean success, int type) {
        if (checkInitOk()) {
            logI("uploadPortalAutoFillStatus enter. state=" + success + ", type=" + type);
            sendStatMsg(STAT_MSG_AF_CHR_UPDATE, success ? 1 : 0, type);
        }
    }

    public void increasePortalNoAutoConnCnt() {
        if (checkInitOk()) {
            logD("increasePortalNoAutoConnCnt enter.");
            sendStatEmptyMsg(STAT_MSG_PORTAL_AP_NOT_AUTO_CONN);
        }
    }

    public void increaseHomeAPAddRoPeriodCnt(int periodCount) {
        if (checkInitOk()) {
            logI("increaseHomeAPAddRoPeriodCnt enter. periodCount=" + periodCount);
            sendStatMsg(STAT_MSG_HOME_AP_ADD_RO_PERIOD_CNT, periodCount, 0);
        }
    }

    public void updateStatisticToDB() {
        if (checkInitOk()) {
            logD("updateStatisticToDB enter.");
            if (!this.mStatHandler.hasMessages(STAT_MSG_UPDATE_SATTISTIC_TO_DB)) {
                sendStatEmptyMsg(STAT_MSG_UPDATE_SATTISTIC_TO_DB);
            }
        }
    }

    public void increaseBgAcDiffType(int diffType) {
        if (checkInitOk()) {
            logI("increaseBG_AC_DiffType enter. diffType=" + diffType);
            sendStatMsg(STAT_MSG_ACTIVE_CHECK_RS_DIFF, diffType, 0);
        }
    }

    public void updateBgApSsid(String backGroundApSsid) {
        if (backGroundApSsid == null) {
            this.mBgApSsid = "";
        } else {
            this.mBgApSsid = backGroundApSsid;
        }
        logI("updateBG_AP_SSID enter. BG SSID=" + StringUtilEx.safeDisplaySsid(backGroundApSsid));
    }

    public void updateBGChrStatistic(int bgChrType) {
        if (checkInitOk()) {
            logI("updateBGChrStatistic enter. notifyType val=" + bgChrType);
            sendStatMsg(STAT_MSG_BACK_GRADING_CHR_UPDATE, bgChrType, 0);
        }
    }

    public void updateBqeSvcChrStatistic(int bqeSvcChrType) {
        if (checkInitOk()) {
            logI("updateBGChrStatistic enter. notifyType val=" + bqeSvcChrType);
            sendStatMsg(STAT_MSG_BQE_GRADING_SVC_CHR_UPDATE, bqeSvcChrType, 0);
        }
    }

    public void increaseNoInetRemindCount(boolean isConnAlarm) {
        if (checkInitOk()) {
            logI("increaseNoInetRemindCount enter. isConnAlarm = " + isConnAlarm);
            sendStatMsg(STAT_MSG_NOT_INET_ALARM_COUNT, isConnAlarm ? 1 : 0, 0);
        }
    }

    public void increaseUserUseBgScanAPCount() {
        if (checkInitOk()) {
            logD("increaseUserUseBgScanAPCount enter.");
            sendStatEmptyMsg(STAT_MSG_USER_USE_BG_SCAN_COUNT);
        }
    }

    public void increasePingPongCount() {
        if (checkInitOk()) {
            logD("increasePingPongCount enter.");
            sendStatEmptyMsg(STAT_MSG_PING_PONG_COUNT);
        }
    }

    public void increaseBqeBadSettingCancelCount() {
        if (checkInitOk()) {
            logD("increaseBQE_BadSettingCancelCount enter.");
            sendStatEmptyMsg(STAT_MSG_BQE_BAD_SETTING_CANCEL);
        }
    }

    public void increaseNotInetSettingCancelCount() {
        if (checkInitOk()) {
            logD("increaseNotInetSettingCancelCount enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_INET_SETTING_CANCEL);
        }
    }

    public void increaseNotInetUserCancelCount() {
        if (checkInitOk()) {
            logD("increaseNotInetUserCancelCount enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_INET_USER_CANCEL);
        }
    }

    public void increaseNotInetRestoreRICount() {
        if (checkInitOk()) {
            logD("increaseNotInetRestoreRICount enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_INET_NET_RESTORE_RI);
        }
    }

    public void increaseNotInetUserManualRICount() {
        if (checkInitOk()) {
            logD("increaseNotInetUserManualRICount enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_INET_USER_MANUAL_RI);
        }
    }

    public void increaseSelectNotInetAPCount() {
        if (checkInitOk()) {
            logD("increaseSelectNotInetAPCount enter.");
            sendStatEmptyMsg(STAT_MSG_SELECT_NOT_INET_AP_COUNT);
        }
    }

    public void increaseNotAutoConnPortalCnt() {
        if (checkInitOk()) {
            logD("increaseNotAutoConnPortalCnt enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT);
        }
    }

    public void increasePortalUnauthCount() {
        if (checkInitOk()) {
            logD("increasePortalUnauthCount enter.");
            sendStatEmptyMsg(STAT_MSG_PORTAL_UNAUTH_COUNT);
        }
    }

    public void increaseWifiScoCount() {
        if (checkInitOk()) {
            logD("increaseWifiScoCount enter.");
            sendStatEmptyMsg(STAT_MSG_WIFI_SCO_COUNT);
        }
    }

    public void increasePortalCodeParseCount() {
        if (checkInitOk()) {
            logD("increasePortalCodeParseCount enter.");
            sendStatEmptyMsg(107);
        }
    }

    public void increaseRcvSmsCount() {
        if (checkInitOk()) {
            logD("increaseRcvSMS_Count enter.");
            sendStatEmptyMsg(STAT_MSG_RCVSMS_COUNT);
        }
    }

    public void increasePortalAutoLoginCount() {
        if (checkInitOk()) {
            logD("increasePortalAutoLoginCount enter.");
            sendStatEmptyMsg(STAT_MSG_PORTAL_AUTO_LOGIN_COUNT);
        }
    }

    public void increaseAutoOpenCount() {
        if (checkInitOk()) {
            logD("increaseCellAutoOpenCount enter.");
            sendStatEmptyMsg(STAT_MSG_CELL_AUTO_OPEN_COUNT);
        }
    }

    public void increaseAutoCloseCount() {
        if (checkInitOk()) {
            logD("increaseCellAutoCloseCount enter.");
            sendStatEmptyMsg(STAT_MSG_CELL_AUTO_CLOSE_COUNT);
        }
    }

    public void increaseHighDataRateStopROC() {
        if (checkInitOk()) {
            logD("increaseHighDataRateStopROC enter.");
            sendStatEmptyMsg(STAT_MSG_HIGH_DATA_RATE_STOP_ROC);
        }
    }

    public void increasePortalConnectedCnt() {
        if (checkInitOk()) {
            logD("increasePortalConnectedCnt enter.");
            sendStatEmptyMsg(STAT_MSG_INCREASE_PORTAL_CONN_COUNT);
        }
    }

    public void increasePortalConnectedAndAuthenCnt() {
        if (checkInitOk()) {
            logD("increasePortalConnectedAndAuthenCnt enter.");
            sendStatEmptyMsg(STAT_MSG_INCREASE_PORTAL_AUTH_SUCC_COUNT);
        }
    }

    public void increasePortalRefusedButUserTouchCnt() {
        if (checkInitOk()) {
            logD("increasePortalRefusedButUserTouchCnt enter.");
            sendStatEmptyMsg(STAT_MSG_INCREASE_CONN_BLOCK_PORTAL_COUNT);
        }
    }

    public void increaseActiveCheckRsSame() {
        if (checkInitOk()) {
            logD("increaseActiveCheckRS_Same enter.");
            sendStatEmptyMsg(STAT_MSG_INCREASE_AC_RS_SAME_COUNT);
        }
    }

    public void accuQOEBadRoDisconnectData() {
        if (checkInitOk()) {
            logD("accuQOERoDisconnectData enter.");
            sendStatEmptyMsg(STAT_MSG_BQE_BAD_RO_DISCONNECT_MOBILE_DATA);
        }
    }

    public void accuNotInetRoDisconnectData() {
        if (checkInitOk()) {
            logD("accuNotInetRoDisconnectData enter.");
            sendStatEmptyMsg(STAT_MSG_NOT_INET_RO_DISCONNECT_MOBILE_DATA);
        }
    }

    public void updateWifiConnectState(int newWifiState) {
        if (checkInitOk()) {
            logD("updateWifiConnectState enter.");
            sendStatMsg(STAT_MSG_UPDATE_WIFI_CONNECTION_STATE, newWifiState, 0);
        }
    }

    public void sendScreenOnEvent() {
        if (checkInitOk()) {
            logD("sendScreenOnEvent enter.");
            sendStatEmptyMsg(STAT_MSG_SCREEN_ON);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logD(String msg) {
        if (printLogLevel <= 1) {
            Log.d(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logI(String msg) {
        if (printLogLevel <= 2) {
            Log.i(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logE(String msg) {
        if (printLogLevel <= 3) {
            Log.e(TAG, msg);
        }
    }

    public void sendStatEmptyMsg(int what) {
        Handler handler = this.mStatHandler;
        if (handler != null) {
            handler.sendEmptyMessage(what);
        }
    }

    public void sendStatMsg(int what, int arg1, int arg2) {
        Handler handler = this.mStatHandler;
        if (handler != null) {
            handler.sendMessage(Message.obtain(handler, what, arg1, arg2));
        }
    }

    public void sendStatEmptyMsgDelayed(int what, long delayMillis) {
        Handler handler = this.mStatHandler;
        if (handler != null) {
            handler.sendEmptyMessageDelayed(what, delayMillis);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void roveOutEventProcess(int roReason, int bqeRoReasonFlag) {
        logI("roveOutEventProcess enter, RO reason:" + roReason + ", BQE RO reason:" + bqeRoReasonFlag);
        if (1 == roReason) {
            if ((bqeRoReasonFlag & 1) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRssiRoTot = (short) (wifiProStatisticsRecord.mRssiRoTot + 1);
            }
            if ((bqeRoReasonFlag & 2) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord2 = this.mNewestStatRcd;
                wifiProStatisticsRecord2.mOtaRoTot = (short) (wifiProStatisticsRecord2.mOtaRoTot + 1);
            }
            if ((bqeRoReasonFlag & 4) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord3 = this.mNewestStatRcd;
                wifiProStatisticsRecord3.mTcpRoTot = (short) (wifiProStatisticsRecord3.mTcpRoTot + 1);
            }
            if ((bqeRoReasonFlag & 8) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord4 = this.mNewestStatRcd;
                wifiProStatisticsRecord4.mBigRttRoTot = (short) (wifiProStatisticsRecord4.mBigRttRoTot + 1);
            }
            WifiProStatisticsRecord wifiProStatisticsRecord5 = this.mNewestStatRcd;
            wifiProStatisticsRecord5.mTotalBqeBadRoc = (short) (wifiProStatisticsRecord5.mTotalBqeBadRoc + 1);
            this.mDataBaseManager.addOrUpdateChrStatRcd(this.mNewestStatRcd);
        }
        this.mROReason = roReason;
        this.mROTime = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getRoMobileData() {
        IGetMobileInfoCallBack iGetMobileInfoCallBack = this.mGetMobileInfoCallBack;
        if (iGetMobileInfoCallBack != null) {
            return iGetMobileInfoCallBack.getTotalRoMobileData();
        }
        return 0;
    }

    private void handleBqeGood(long passTime, int bqeRiReasonFlag) {
        if (bqeRiReasonFlag == 1) {
            WifiProStatisticsRecord wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mRssiRestoreRiCount = (short) (wifiProStatisticsRecord.mRssiRestoreRiCount + 1);
        } else if (bqeRiReasonFlag == 2) {
            WifiProStatisticsRecord wifiProStatisticsRecord2 = this.mNewestStatRcd;
            wifiProStatisticsRecord2.mRssiBetterRiCount = (short) (wifiProStatisticsRecord2.mRssiBetterRiCount + 1);
        } else if (bqeRiReasonFlag == 3) {
            WifiProStatisticsRecord wifiProStatisticsRecord3 = this.mNewestStatRcd;
            wifiProStatisticsRecord3.mTimerRiCount = (short) (wifiProStatisticsRecord3.mTimerRiCount + 1);
        }
        WifiProStatisticsRecord wifiProStatisticsRecord4 = this.mNewestStatRcd;
        wifiProStatisticsRecord4.mAutoRiTotCount = (short) (wifiProStatisticsRecord4.mAutoRiTotCount + 1);
        WifiProStatisticsRecord wifiProStatisticsRecord5 = this.mNewestStatRcd;
        wifiProStatisticsRecord5.mAutoRiTotTime = (int) (((long) wifiProStatisticsRecord5.mAutoRiTotTime) + passTime);
        this.mNewestStatRcd.mQoeAutoRiTotData += getRoMobileData();
    }

    private void handleBqeBad(int riReason, int bqeRoReasonFlag, long passTime) {
        int roDataKB = 0;
        boolean isManualRi = riReason == 3 || 4 == riReason || 5 == riReason;
        boolean isUserCancelRi = riReason == 2;
        if (isUserCancelRi || isManualRi) {
            logI("abnormal BQE bad rove out statistics process.");
            if ((bqeRoReasonFlag & 1) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord = this.mNewestStatRcd;
                wifiProStatisticsRecord.mRssiErrRoTot = (short) (wifiProStatisticsRecord.mRssiErrRoTot + 1);
            }
            if ((bqeRoReasonFlag & 2) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord2 = this.mNewestStatRcd;
                wifiProStatisticsRecord2.mOtaErrRoTot = (short) (wifiProStatisticsRecord2.mOtaErrRoTot + 1);
            }
            if ((bqeRoReasonFlag & 4) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord3 = this.mNewestStatRcd;
                wifiProStatisticsRecord3.mTcpErrRoTot = (short) (wifiProStatisticsRecord3.mTcpErrRoTot + 1);
            }
            if ((bqeRoReasonFlag & 8) != 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord4 = this.mNewestStatRcd;
                wifiProStatisticsRecord4.mBigRttErrRoTot = (short) (wifiProStatisticsRecord4.mBigRttErrRoTot + 1);
            }
            if (passTime > 32760) {
                passTime = 32760;
            }
            sendUnexpectedROParaCHREvent(this.mRoveOutPara, (short) ((int) passTime));
        }
        if (isManualRi) {
            WifiProStatisticsRecord wifiProStatisticsRecord5 = this.mNewestStatRcd;
            wifiProStatisticsRecord5.mManualRiTotTime = (int) (((long) wifiProStatisticsRecord5.mManualRiTotTime) + passTime);
            WifiProStatisticsRecord wifiProStatisticsRecord6 = this.mNewestStatRcd;
            wifiProStatisticsRecord6.mManualBackROC = (short) (wifiProStatisticsRecord6.mManualBackROC + 1);
            IGetMobileInfoCallBack iGetMobileInfoCallBack = this.mGetMobileInfoCallBack;
            if (iGetMobileInfoCallBack != null) {
                roDataKB = iGetMobileInfoCallBack.getTotalRoMobileData();
            }
            if (4 == riReason) {
                WifiProStatisticsRecord wifiProStatisticsRecord7 = this.mNewestStatRcd;
                wifiProStatisticsRecord7.mTotBtnRICount = (short) (wifiProStatisticsRecord7.mTotBtnRICount + 1);
            }
            this.mNewestStatRcd.mRoTotMobileData += roDataKB;
        }
        if (isUserCancelRi) {
            WifiProStatisticsRecord wifiProStatisticsRecord8 = this.mNewestStatRcd;
            wifiProStatisticsRecord8.mUserCancelROC = (short) (wifiProStatisticsRecord8.mUserCancelROC + 1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void roveInEventProcess(int riReason, int bqeRiReasonFlag, int bqeRoReasonFlag, int roReason) {
        long passTime;
        boolean needUpdateDB = false;
        int currentRoReason = roReason;
        logI("roveInEventProcess enter, RI reason:" + riReason + ", BQE RI reason:" + bqeRiReasonFlag + ", roReasonFlag:" + bqeRoReasonFlag + ", roReason:" + roReason);
        if (2 == riReason) {
            passTime = 1;
            logI("update ro reason by cancel ri event.");
            currentRoReason = 1;
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
            handleBqeGood(passTime, bqeRiReasonFlag);
            needUpdateDB = true;
        } else if (6 == riReason) {
            WifiProStatisticsRecord wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mHisScoRiCount = (short) (wifiProStatisticsRecord.mHisScoRiCount + 1);
            needUpdateDB = true;
        }
        if (1 == currentRoReason) {
            handleBqeBad(riReason, bqeRoReasonFlag, passTime);
            needUpdateDB = true;
        }
        if (needUpdateDB) {
            this.mDataBaseManager.addOrUpdateChrStatRcd(this.mNewestStatRcd);
        }
    }

    private void sendUnexpectedROParaCHREvent(WifiProRoveOutParaRecord roveOutPara, short roTime) {
        if (roveOutPara != null) {
            logI("unexpected RO para CHR event send.");
            roveOutPara.mRoDuration = roTime;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendPingpongCHREvent(WifiProRoveOutParaRecord roveOutPara, short roTime) {
        if (roveOutPara != null) {
            logI("Pingpong RO para CHR event send.");
            roveOutPara.mRoDuration = roTime;
        }
    }

    private void initStatHandler(Looper looper) {
        Looper currentLooper = looper;
        if (currentLooper == null) {
            logI("looper null, force create single thread");
            HandlerThread thread = new HandlerThread("StatisticsCHRMsgHandler");
            thread.start();
            currentLooper = thread.getLooper();
        }
        this.mStatHandler = new StatisticsCHRMsgHandler(currentLooper);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCheckUploadMsg() {
        if (this.mStatHandler.hasMessages(101)) {
            logE("There has CHECK_NEED_UPLOAD_EVENT msg at queue.");
        } else if (debugMode) {
            sendStatEmptyMsgDelayed(101, 60000);
        } else {
            sendStatEmptyMsgDelayed(101, 1800000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkMsgLoopRunning() {
        if (!this.mStatHandler.hasMessages(101)) {
            logI("restart msg Loop.");
            sendStatEmptyMsg(101);
            return;
        }
        logD("msg Loop is running.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBGChrProcess(int bgChrType) {
        logI("updateBGChrProcess enter for bg type:" + bgChrType);
        if (bgChrType == 20) {
            WifiProStatisticsRecord wifiProStatisticsRecord = this.mNewestStatRcd;
            wifiProStatisticsRecord.mBgNcbyConnectFail = (short) (wifiProStatisticsRecord.mBgNcbyConnectFail + 1);
        } else if (bgChrType != 21) {
            switch (bgChrType) {
                case 1:
                    WifiProStatisticsRecord wifiProStatisticsRecord2 = this.mNewestStatRcd;
                    wifiProStatisticsRecord2.mBgBgRunCnt = (short) (wifiProStatisticsRecord2.mBgBgRunCnt + 1);
                    return;
                case 2:
                    return;
                case 3:
                    WifiProStatisticsRecord wifiProStatisticsRecord3 = this.mNewestStatRcd;
                    wifiProStatisticsRecord3.mBgFreeInetOkApCnt = (short) (wifiProStatisticsRecord3.mBgFreeInetOkApCnt + 1);
                    return;
                case 4:
                    WifiProStatisticsRecord wifiProStatisticsRecord4 = this.mNewestStatRcd;
                    wifiProStatisticsRecord4.mBgFishingApCnt = (short) (wifiProStatisticsRecord4.mBgFishingApCnt + 1);
                    return;
                case 5:
                    WifiProStatisticsRecord wifiProStatisticsRecord5 = this.mNewestStatRcd;
                    wifiProStatisticsRecord5.mBgFreeNotInetApCnt = (short) (wifiProStatisticsRecord5.mBgFreeNotInetApCnt + 1);
                    return;
                case 6:
                    WifiProStatisticsRecord wifiProStatisticsRecord6 = this.mNewestStatRcd;
                    wifiProStatisticsRecord6.mBgPortalApCnt = (short) (wifiProStatisticsRecord6.mBgPortalApCnt + 1);
                    return;
                case 7:
                    WifiProStatisticsRecord wifiProStatisticsRecord7 = this.mNewestStatRcd;
                    wifiProStatisticsRecord7.mBgUserSelFreeInetOkCnt = (short) (wifiProStatisticsRecord7.mBgUserSelFreeInetOkCnt + 1);
                    return;
                case 8:
                    WifiProStatisticsRecord wifiProStatisticsRecord8 = this.mNewestStatRcd;
                    wifiProStatisticsRecord8.mBgUserSelNoInetCnt = (short) (wifiProStatisticsRecord8.mBgUserSelNoInetCnt + 1);
                    return;
                case 9:
                    WifiProStatisticsRecord wifiProStatisticsRecord9 = this.mNewestStatRcd;
                    wifiProStatisticsRecord9.mBgUserSelPortalCnt = (short) (wifiProStatisticsRecord9.mBgUserSelPortalCnt + 1);
                    return;
                case 10:
                    WifiProStatisticsRecord wifiProStatisticsRecord10 = this.mNewestStatRcd;
                    wifiProStatisticsRecord10.mBgFoundTwoMoreApCnt = (short) (wifiProStatisticsRecord10.mBgFoundTwoMoreApCnt + 1);
                    return;
                case 11:
                    WifiProStatisticsRecord wifiProStatisticsRecord11 = this.mNewestStatRcd;
                    wifiProStatisticsRecord11.mBgFailedCnt = (short) (wifiProStatisticsRecord11.mBgFailedCnt + 1);
                    return;
                default:
                    logE("updateBGChrProcess error type:" + bgChrType);
                    return;
            }
        } else {
            WifiProStatisticsRecord wifiProStatisticsRecord12 = this.mNewestStatRcd;
            wifiProStatisticsRecord12.mBgNcbyCheckFail = (short) (wifiProStatisticsRecord12.mBgNcbyCheckFail + 1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBqeSvcChrProcess(int bqeSvcChrType) {
    }

    /* access modifiers changed from: package-private */
    public class StatisticsCHRMsgHandler extends Handler {
        private StatisticsCHRMsgHandler(Looper looper) {
            super(looper);
            WifiProStatisticsManager.this.logD("new StatisticsCHRMsgHandler");
        }

        private void handleChrDbInit() {
            WifiProStatisticsManager.this.logI("ChrDataBaseManager init start.");
            WifiProStatisticsManager wifiProStatisticsManager = WifiProStatisticsManager.this;
            wifiProStatisticsManager.mDataBaseManager = WifiProChrDataBaseManager.getInstance(wifiProStatisticsManager.mContext);
            WifiProStatisticsManager wifiProStatisticsManager2 = WifiProStatisticsManager.this;
            wifiProStatisticsManager2.loadStatDBRecord(wifiProStatisticsManager2.mNewestStatRcd);
            WifiProStatisticsManager.this.sendStatEmptyMsg(101);
        }

        private void checkUpload() {
            WifiProStatisticsManager wifiProStatisticsManager = WifiProStatisticsManager.this;
            if (wifiProStatisticsManager.checkIfNeedUpload(wifiProStatisticsManager.mNewestStatRcd)) {
                WifiProStatisticsManager wifiProStatisticsManager2 = WifiProStatisticsManager.this;
                wifiProStatisticsManager2.uploadStatisticsCHREvent(wifiProStatisticsManager2.mNewestStatRcd);
            }
            if (WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState == 0) {
                WifiProStatisticsManager.this.logE("wifipro state abnormal, try reget it.");
                short wifiproState = WifiProStatisticsManager.this.getWifiproState();
                if (wifiproState != 0) {
                    WifiProStatisticsManager.this.sendStatMsg(103, wifiproState, 0);
                }
            }
            WifiProStatisticsManager.this.sendCheckUploadMsg();
        }

        private void countHmdNotification(Message msg) {
            int notifyType = msg.arg1;
            if (1 == notifyType) {
                WifiProStatisticsRecord wifiProStatisticsRecord = WifiProStatisticsManager.this.mNewestStatRcd;
                wifiProStatisticsRecord.mBmdTenmNotifyCount = (short) (wifiProStatisticsRecord.mBmdTenmNotifyCount + 1);
                WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 1;
            } else if (2 == notifyType) {
                WifiProStatisticsRecord wifiProStatisticsRecord2 = WifiProStatisticsManager.this.mNewestStatRcd;
                wifiProStatisticsRecord2.mBmdFiftymNotifyCount = (short) (wifiProStatisticsRecord2.mBmdFiftymNotifyCount + 1);
                WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 2;
            }
            WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
        }

        private void handleStateChange(Message msg) {
            short wifiproState = (short) msg.arg1;
            if (wifiproState != WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState) {
                Date currDate = new Date();
                String currDateStr = WifiProStatisticsManager.this.mSimpleDateFmt.format(currDate);
                if (wifiproState == 2 && WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState == 1) {
                    WifiProStatisticsManager wifiProStatisticsManager = WifiProStatisticsManager.this;
                    long enableTotTime = wifiProStatisticsManager.calcTimeInterval(wifiProStatisticsManager.mNewestStatRcd.mLastWifiproStateUpdateTime, currDate);
                    if (enableTotTime <= -1) {
                        WifiProStatisticsManager wifiProStatisticsManager2 = WifiProStatisticsManager.this;
                        wifiProStatisticsManager2.resetStatRecord(wifiProStatisticsManager2.mNewestStatRcd, "last WifiproStateUpdateTime time record invalid");
                        return;
                    }
                    long enableTotTime2 = enableTotTime / 1000;
                    WifiProStatisticsManager.this.mNewestStatRcd.mEnableTotTime += (int) enableTotTime2;
                    WifiProStatisticsManager.this.logI("last state en seconds:" + enableTotTime2);
                    WifiProStatisticsRecord wifiProStatisticsRecord = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord.mWifiproCloseCount = (short) (wifiProStatisticsRecord.mWifiproCloseCount + 1);
                } else if (wifiproState == 1 && WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState == 2) {
                    WifiProStatisticsRecord wifiProStatisticsRecord2 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord2.mWifiproOpenCount = (short) (wifiProStatisticsRecord2.mWifiproOpenCount + 1);
                }
                WifiProStatisticsManager.this.logI("wifipro old state:" + ((int) WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState) + ", new state:" + ((int) wifiproState) + ", date str:" + currDateStr);
                WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproState = wifiproState;
                WifiProStatisticsManager.this.mNewestStatRcd.mLastWifiproStateUpdateTime = currDateStr;
                WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                return;
            }
            WifiProStatisticsManager.this.logE("wifipro state unknow or not changed msg receive:" + ((int) wifiproState));
        }

        private void sendPingPongEvent() {
            int passTime = (int) ((SystemClock.elapsedRealtime() - WifiProStatisticsManager.this.mROTime) / 1000);
            if (passTime == 1) {
                passTime = 2;
            }
            if (passTime > WifiProStatisticsManager.MAX_SHORT_TYPE_VALUE) {
                passTime = WifiProStatisticsManager.MAX_SHORT_TYPE_VALUE;
            }
            WifiProStatisticsManager wifiProStatisticsManager = WifiProStatisticsManager.this;
            wifiProStatisticsManager.sendPingpongCHREvent(wifiProStatisticsManager.mRoveOutPara, (short) passTime);
        }

        private void handleRiEvent(Message msg) {
            int riReason = msg.arg1;
            int bqeRiReason = msg.arg2;
            if (WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted || 2 == riReason) {
                WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                WifiProStatisticsManager wifiProStatisticsManager = WifiProStatisticsManager.this;
                wifiProStatisticsManager.roveInEventProcess(riReason, bqeRiReason, wifiProStatisticsManager.mBQEROReasonFlag, WifiProStatisticsManager.this.mROReason);
                return;
            }
            WifiProStatisticsManager wifiProStatisticsManager2 = WifiProStatisticsManager.this;
            wifiProStatisticsManager2.logI("Ignore duplicate qoe RI reason:" + riReason);
        }

        private void countCspChange(Message msg) {
            int cspVal = msg.arg1;
            if (cspVal == 0) {
                WifiProStatisticsRecord wifiProStatisticsRecord = WifiProStatisticsManager.this.mNewestStatRcd;
                wifiProStatisticsRecord.mSelCSPShowDiglogCount = (short) (wifiProStatisticsRecord.mSelCSPShowDiglogCount + 1);
            } else if (1 == cspVal) {
                WifiProStatisticsRecord wifiProStatisticsRecord2 = WifiProStatisticsManager.this.mNewestStatRcd;
                wifiProStatisticsRecord2.mSelCSPAutoSwCount = (short) (wifiProStatisticsRecord2.mSelCSPAutoSwCount + 1);
            } else if (2 == cspVal) {
                WifiProStatisticsRecord wifiProStatisticsRecord3 = WifiProStatisticsManager.this.mNewestStatRcd;
                wifiProStatisticsRecord3.mSelCSPNotSwCount = (short) (wifiProStatisticsRecord3.mSelCSPNotSwCount + 1);
            }
            WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
        }

        private void afChrUpdate(Message msg) {
            int state = msg.arg1;
            int type = msg.arg2;
            if (type == 0) {
                if (1 == state) {
                    WifiProStatisticsRecord wifiProStatisticsRecord = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord.mAfPhoneNumSuccCnt = (short) (wifiProStatisticsRecord.mAfPhoneNumSuccCnt + 1);
                } else {
                    WifiProStatisticsRecord wifiProStatisticsRecord2 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord2.mAfPhoneNumFailCnt = (short) (wifiProStatisticsRecord2.mAfPhoneNumFailCnt + 1);
                }
            } else if (1 == type) {
                if (1 == state) {
                    WifiProStatisticsRecord wifiProStatisticsRecord3 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord3.mAfPasswordSuccCnt = (short) (wifiProStatisticsRecord3.mAfPasswordSuccCnt + 1);
                } else {
                    WifiProStatisticsRecord wifiProStatisticsRecord4 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord4.mAfPasswordFailCnt = (short) (wifiProStatisticsRecord4.mAfPasswordFailCnt + 1);
                }
            } else if (2 == type) {
                if (1 == state) {
                    WifiProStatisticsRecord wifiProStatisticsRecord5 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord5.mAfAutoLoginSuccCnt = (short) (wifiProStatisticsRecord5.mAfAutoLoginSuccCnt + 1);
                } else {
                    WifiProStatisticsRecord wifiProStatisticsRecord6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord6.mAfAutoLoginFailCnt = (short) (wifiProStatisticsRecord6.mAfAutoLoginFailCnt + 1);
                }
            } else if (3 == type) {
                WifiProStatisticsRecord wifiProStatisticsRecord7 = WifiProStatisticsManager.this.mNewestStatRcd;
                wifiProStatisticsRecord7.mAfFpnsSuccNotMsmCnt = (short) (wifiProStatisticsRecord7.mAfFpnsSuccNotMsmCnt + 1);
            }
            WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
        }

        private void handleWiFiConnection(Message msg) {
            int wifiState = msg.arg1;
            if (1 == wifiState) {
                if (WifiProStatisticsManager.this.mWifiConnectStartTime == 0) {
                    WifiProStatisticsManager.this.mWifiConnectStartTime = SystemClock.elapsedRealtime();
                    WifiProStatisticsManager.this.logI("wifi connect start here.");
                    return;
                }
                WifiProStatisticsManager.this.logD("wifi already connected.");
            } else if (2 != wifiState) {
                WifiProStatisticsManager.this.logD("STAT_MSG_UPDATE_WIFI_CONNECTION_STATE: wifi state is not connected or disconnected ");
            } else if (WifiProStatisticsManager.this.mWifiConnectStartTime != 0) {
                long connectionTime = SystemClock.elapsedRealtime() - WifiProStatisticsManager.this.mWifiConnectStartTime;
                if (connectionTime > 0 && connectionTime < WifiProStatisticsManager.MAX_COMMERCIAL_USER_UPLOAD_PERIOD) {
                    long acctime = connectionTime / 1000;
                    WifiProStatisticsRecord wifiProStatisticsRecord = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord.mTotWifiConnectTime = (int) (((long) wifiProStatisticsRecord.mTotWifiConnectTime) + acctime);
                    WifiProStatisticsManager wifiProStatisticsManager = WifiProStatisticsManager.this;
                    wifiProStatisticsManager.logI("acc wifi connection time:" + acctime + " s, total" + WifiProStatisticsManager.this.mNewestStatRcd.mTotWifiConnectTime);
                }
                WifiProStatisticsManager.this.logI("wifi connected end.");
                WifiProStatisticsManager.this.mWifiConnectStartTime = 0;
            }
        }

        private void countBmdNotification() {
            if (2 == WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType) {
                WifiProStatisticsRecord wifiProStatisticsRecord = WifiProStatisticsManager.this.mNewestStatRcd;
                wifiProStatisticsRecord.mBmdFiftymRiCount = (short) (wifiProStatisticsRecord.mBmdFiftymRiCount + 1);
            } else if (1 == WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType) {
                WifiProStatisticsRecord wifiProStatisticsRecord2 = WifiProStatisticsManager.this.mNewestStatRcd;
                wifiProStatisticsRecord2.mBmdTenmRiCount = (short) (wifiProStatisticsRecord2.mBmdTenmRiCount + 1);
            } else {
                WifiProStatisticsManager.this.logI("mRoveoutMobileDataNotifyType is not FIFTY_M or TEM_M");
            }
            WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
            WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int notInetRoDataKB = 0;
            switch (msg.what) {
                case 100:
                    handleChrDbInit();
                    return;
                case 101:
                    checkUpload();
                    return;
                case 102:
                case WifiProStatisticsManager.STAT_MSG_ADD_TOTAL_BQE_ROC /* 112 */:
                case WifiProStatisticsManager.STAT_MSG_RO_EVENT /* 113 */:
                case WifiProStatisticsManager.STAT_MSG_RI_EVENT /* 114 */:
                case WifiProStatisticsManager.STAT_MSG_BQE_RO_PARA_UPDATE /* 115 */:
                case WifiProStatisticsManager.STAT_MSG_HISTORY_SCORE_RI_COUNT /* 116 */:
                case WifiProStatisticsManager.STAT_MSG_SELECT_NOT_INET_AP_COUNT /* 121 */:
                case WifiProStatisticsManager.STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT /* 122 */:
                default:
                    keepHandleMessage(msg);
                    return;
                case 103:
                    handleStateChange(msg);
                    return;
                case 104:
                    WifiProStatisticsRecord wifiProStatisticsRecord = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord.mNoInetHandoverCount = (short) (wifiProStatisticsRecord.mNoInetHandoverCount + 1);
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = true;
                    WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_PORTAL_UNAUTH_COUNT /* 105 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord2 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord2.mPortalUnauthCount = (short) (wifiProStatisticsRecord2.mPortalUnauthCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_WIFI_SCO_COUNT /* 106 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord3 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord3.mWifiScoCount = (short) (wifiProStatisticsRecord3.mWifiScoCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case 107:
                    WifiProStatisticsRecord wifiProStatisticsRecord4 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord4.mPortalCodeParseCount = (short) (wifiProStatisticsRecord4.mPortalCodeParseCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_RCVSMS_COUNT /* 108 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord5 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord5.mRcvSmsCount = (short) (wifiProStatisticsRecord5.mRcvSmsCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_PORTAL_AUTO_LOGIN_COUNT /* 109 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord6 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord6.mPortalAutoLoginCount = (short) (wifiProStatisticsRecord6.mPortalAutoLoginCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_CELL_AUTO_OPEN_COUNT /* 110 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord7 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord7.mCellAutoOpenCount = (short) (wifiProStatisticsRecord7.mCellAutoOpenCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_CELL_AUTO_CLOSE_COUNT /* 111 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord8 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord8.mCellAutoCloseCount = (short) (wifiProStatisticsRecord8.mCellAutoCloseCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_OOB_INIT_STATE /* 117 */:
                    WifiProStatisticsManager.this.mNewestStatRcd.mWifiOobInitState = (short) msg.arg1;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_ALARM_COUNT /* 118 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord9 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord9.mNoInetAlarmCount = (short) (wifiProStatisticsRecord9.mNoInetAlarmCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    if (1 == msg.arg1) {
                        WifiProStatisticsRecord wifiProStatisticsRecord10 = WifiProStatisticsManager.this.mNewestStatRcd;
                        wifiProStatisticsRecord10.mNoInetAlarmOnConnCnt = (short) (wifiProStatisticsRecord10.mNoInetAlarmOnConnCnt + 1);
                        return;
                    }
                    return;
                case WifiProStatisticsManager.STAT_MSG_USER_USE_BG_SCAN_COUNT /* 119 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord11 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord11.mUserUseBgScanAPCount = (short) (wifiProStatisticsRecord11.mUserUseBgScanAPCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_WIFI_TO_WIFI_SUCC_COUNT /* 120 */:
                    int handOverType = (short) msg.arg1;
                    WifiProStatisticsRecord wifiProStatisticsRecord12 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord12.mWifiToWifiSuccCount = (short) (wifiProStatisticsRecord12.mWifiToWifiSuccCount + 1);
                    if (1 == handOverType) {
                        WifiProStatisticsRecord wifiProStatisticsRecord13 = WifiProStatisticsManager.this.mNewestStatRcd;
                        wifiProStatisticsRecord13.mNotInetWifiToWifiCount = (short) (wifiProStatisticsRecord13.mNotInetWifiToWifiCount + 1);
                    }
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_HIGH_DATA_RATE_STOP_ROC /* 123 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord14 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord14.mHighDataRateStopROC = (short) (wifiProStatisticsRecord14.mHighDataRateStopROC + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_PING_PONG_COUNT /* 124 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord15 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord15.mPingPongCount = (short) (wifiProStatisticsRecord15.mPingPongCount + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    sendPingPongEvent();
                    return;
                case WifiProStatisticsManager.STAT_MSG_BQE_BAD_SETTING_CANCEL /* 125 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord16 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord16.mBqeBadSettingCancel = (short) (wifiProStatisticsRecord16.mBqeBadSettingCancel + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_SETTING_CANCEL /* 126 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord17 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord17.mNotInetSettingCancel = (short) (wifiProStatisticsRecord17.mNotInetSettingCancel + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_USER_CANCEL /* 127 */:
                    WifiProStatisticsRecord wifiProStatisticsRecord18 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord18.mNotInetUserCancel = (short) (wifiProStatisticsRecord18.mNotInetUserCancel + 1);
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_NET_RESTORE_RI /* 128 */:
                    if (!WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted) {
                        WifiProStatisticsManager.this.logI("Ignore duplicate not inet restore RI event.");
                        return;
                    }
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                    WifiProStatisticsRecord wifiProStatisticsRecord19 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord19.mNotInetRestoreRI = (short) (wifiProStatisticsRecord19.mNotInetRestoreRI + 1);
                    WifiProStatisticsManager.this.mNewestStatRcd.mNotInetAutoRiTotData += WifiProStatisticsManager.this.getRoMobileData();
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_NOT_INET_USER_MANUAL_RI /* 129 */:
                    if (!WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted) {
                        WifiProStatisticsManager.this.logI("Ignore duplicate not inet user manual RI event.");
                        return;
                    }
                    WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                    WifiProStatisticsRecord wifiProStatisticsRecord20 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord20.mNotInetUserManualRI = (short) (wifiProStatisticsRecord20.mNotInetUserManualRI + 1);
                    if (WifiProStatisticsManager.this.mGetMobileInfoCallBack != null) {
                        notInetRoDataKB = WifiProStatisticsManager.this.mGetMobileInfoCallBack.getTotalRoMobileData();
                    }
                    WifiProStatisticsRecord wifiProStatisticsRecord21 = WifiProStatisticsManager.this.mNewestStatRcd;
                    wifiProStatisticsRecord21.mTotBtnRICount = (short) (wifiProStatisticsRecord21.mTotBtnRICount + 1);
                    WifiProStatisticsManager.this.mNewestStatRcd.mRoTotMobileData += notInetRoDataKB;
                    WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                    return;
                case WifiProStatisticsManager.STAT_MSG_SCREEN_ON /* 130 */:
                    WifiProStatisticsManager.this.checkMsgLoopRunning();
                    return;
            }
        }

        /* JADX INFO: Multiple debug info for r1v28 int: [D('bqeSvcChrType' int), D('bgChrType' int)] */
        public void keepHandleMessage(Message msg) {
            int i = msg.what;
            if (i == WifiProStatisticsManager.STAT_MSG_SELECT_NOT_INET_AP_COUNT) {
                WifiProStatisticsRecord wifiProStatisticsRecord = WifiProStatisticsManager.this.mNewestStatRcd;
                wifiProStatisticsRecord.mSelectNotInetAPCount = (short) (wifiProStatisticsRecord.mSelectNotInetAPCount + 1);
                WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
            } else if (i != WifiProStatisticsManager.STAT_MSG_NOT_AUTO_CONN_PORTAL_COUNT) {
                switch (i) {
                    case WifiProStatisticsManager.STAT_MSG_RO_EVENT /* 113 */:
                        int roReason = msg.arg1;
                        int bqeRoReason = msg.arg2;
                        WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = true;
                        WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                        WifiProStatisticsManager.this.roveOutEventProcess(roReason, bqeRoReason);
                        return;
                    case WifiProStatisticsManager.STAT_MSG_RI_EVENT /* 114 */:
                        handleRiEvent(msg);
                        return;
                    case WifiProStatisticsManager.STAT_MSG_BQE_RO_PARA_UPDATE /* 115 */:
                        if (WifiProStatisticsManager.this.mRoveOutPara != null) {
                            WifiProStatisticsManager.this.mRoveOutPara.mMobileSignalLevel = (short) WifiProStatisticsManager.this.getMobileSignalLevel();
                            WifiProStatisticsManager.this.mRoveOutPara.mRatType = (short) WifiProStatisticsManager.this.getMobileRATType();
                            return;
                        }
                        return;
                    case WifiProStatisticsManager.STAT_MSG_HISTORY_SCORE_RI_COUNT /* 116 */:
                        WifiProStatisticsRecord wifiProStatisticsRecord2 = WifiProStatisticsManager.this.mNewestStatRcd;
                        wifiProStatisticsRecord2.mHisScoRiCount = (short) (wifiProStatisticsRecord2.mHisScoRiCount + 1);
                        WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                        return;
                    default:
                        switch (i) {
                            case WifiProStatisticsManager.STAT_MSG_USER_REOPEN_WIFI_RI_CNT /* 131 */:
                                WifiProStatisticsRecord wifiProStatisticsRecord3 = WifiProStatisticsManager.this.mNewestStatRcd;
                                wifiProStatisticsRecord3.mReopenWifiRICount = (short) (wifiProStatisticsRecord3.mReopenWifiRICount + 1);
                                WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                                return;
                            case WifiProStatisticsManager.STAT_MSG_SEL_CSP_SETTING_CHG_CNT /* 132 */:
                                countCspChange(msg);
                                return;
                            case WifiProStatisticsManager.STAT_MSG_HMD_NOTIFY_CNT /* 133 */:
                                countHmdNotification(msg);
                                return;
                            case WifiProStatisticsManager.STAT_MSG_HMD_USER_DEL_NOTIFY_CNT /* 134 */:
                                WifiProStatisticsRecord wifiProStatisticsRecord4 = WifiProStatisticsManager.this.mNewestStatRcd;
                                wifiProStatisticsRecord4.mBmdUserDelNotifyCount = (short) (wifiProStatisticsRecord4.mBmdUserDelNotifyCount + 1);
                                WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                                return;
                            case WifiProStatisticsManager.STAT_MSG_AF_CHR_UPDATE /* 135 */:
                                afChrUpdate(msg);
                                return;
                            case WifiProStatisticsManager.STAT_MSG_BACK_GRADING_CHR_UPDATE /* 136 */:
                                WifiProStatisticsManager.this.updateBGChrProcess(msg.arg1);
                                WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                                return;
                            case WifiProStatisticsManager.STAT_MSG_BQE_GRADING_SVC_CHR_UPDATE /* 137 */:
                                WifiProStatisticsManager.this.updateBqeSvcChrProcess(msg.arg1);
                                WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                                return;
                            case WifiProStatisticsManager.STAT_MSG_BQE_BAD_RO_DISCONNECT_MOBILE_DATA /* 138 */:
                                WifiProStatisticsRecord wifiProStatisticsRecord5 = WifiProStatisticsManager.this.mNewestStatRcd;
                                wifiProStatisticsRecord5.mQoeRoDisconnectCnt = (short) (wifiProStatisticsRecord5.mQoeRoDisconnectCnt + 1);
                                int mobileData = WifiProStatisticsManager.this.getRoMobileData();
                                WifiProStatisticsManager wifiProStatisticsManager = WifiProStatisticsManager.this;
                                wifiProStatisticsManager.logI("qoe ro disconnect mobileData=" + mobileData);
                                WifiProStatisticsRecord wifiProStatisticsRecord6 = WifiProStatisticsManager.this.mNewestStatRcd;
                                wifiProStatisticsRecord6.mQoeRoDisconnectTotData = wifiProStatisticsRecord6.mQoeRoDisconnectTotData + mobileData;
                                WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                                WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                                WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                                return;
                            case WifiProStatisticsManager.STAT_MSG_NOT_INET_RO_DISCONNECT_MOBILE_DATA /* 139 */:
                                WifiProStatisticsRecord wifiProStatisticsRecord7 = WifiProStatisticsManager.this.mNewestStatRcd;
                                wifiProStatisticsRecord7.mNotInetRoDisconnectCnt = (short) (wifiProStatisticsRecord7.mNotInetRoDisconnectCnt + 1);
                                int mobileData2 = WifiProStatisticsManager.this.getRoMobileData();
                                WifiProStatisticsManager wifiProStatisticsManager2 = WifiProStatisticsManager.this;
                                wifiProStatisticsManager2.logI("inet ro disconnect mobileData=" + mobileData2);
                                WifiProStatisticsRecord wifiProStatisticsRecord8 = WifiProStatisticsManager.this.mNewestStatRcd;
                                wifiProStatisticsRecord8.mNotInetRoDisconnectTotData = wifiProStatisticsRecord8.mNotInetRoDisconnectTotData + mobileData2;
                                WifiProStatisticsManager.this.mQoeOrNotInetRoveOutStarted = false;
                                WifiProStatisticsManager.this.mRoveoutMobileDataNotifyType = 0;
                                WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                                return;
                            case WifiProStatisticsManager.STAT_MSG_UPDATE_WIFI_CONNECTION_STATE /* 140 */:
                                handleWiFiConnection(msg);
                                return;
                            case WifiProStatisticsManager.STAT_MSG_PORTAL_AP_NOT_AUTO_CONN /* 141 */:
                                WifiProStatisticsRecord wifiProStatisticsRecord9 = WifiProStatisticsManager.this.mNewestStatRcd;
                                wifiProStatisticsRecord9.mPortalNoAutoConnCnt = (short) (wifiProStatisticsRecord9.mPortalNoAutoConnCnt + 1);
                                WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                                return;
                            case WifiProStatisticsManager.STAT_MSG_HOME_AP_ADD_RO_PERIOD_CNT /* 142 */:
                                return;
                            default:
                                switch (i) {
                                    case WifiProStatisticsManager.STAT_MSG_UPDATE_SATTISTIC_TO_DB /* 144 */:
                                        if (WifiProStatisticsManager.this.mNeedSaveCHRStatistic) {
                                            WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
                                            WifiProStatisticsManager.this.mNeedSaveCHRStatistic = false;
                                            return;
                                        }
                                        return;
                                    case WifiProStatisticsManager.STAT_MSG_ACTIVE_CHECK_RS_DIFF /* 145 */:
                                        WifiProStatisticsRecord wifiProStatisticsRecord10 = WifiProStatisticsManager.this.mNewestStatRcd;
                                        wifiProStatisticsRecord10.mActiveCheckRsDiff = (short) (wifiProStatisticsRecord10.mActiveCheckRsDiff + 1);
                                        WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                                        return;
                                    case WifiProStatisticsManager.STAT_MSG_INCREASE_PORTAL_CONN_COUNT /* 146 */:
                                        WifiProStatisticsRecord wifiProStatisticsRecord11 = WifiProStatisticsManager.this.mNewestStatRcd;
                                        wifiProStatisticsRecord11.mTotalPortalConnCount = (short) (wifiProStatisticsRecord11.mTotalPortalConnCount + 1);
                                        WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                                        return;
                                    case WifiProStatisticsManager.STAT_MSG_INCREASE_PORTAL_AUTH_SUCC_COUNT /* 147 */:
                                        WifiProStatisticsRecord wifiProStatisticsRecord12 = WifiProStatisticsManager.this.mNewestStatRcd;
                                        wifiProStatisticsRecord12.mTotalPortalAuthSuccCount = (short) (wifiProStatisticsRecord12.mTotalPortalAuthSuccCount + 1);
                                        WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                                        return;
                                    case WifiProStatisticsManager.STAT_MSG_INCREASE_CONN_BLOCK_PORTAL_COUNT /* 148 */:
                                        WifiProStatisticsRecord wifiProStatisticsRecord13 = WifiProStatisticsManager.this.mNewestStatRcd;
                                        wifiProStatisticsRecord13.mManualConnBlockPortalCount = (short) (wifiProStatisticsRecord13.mManualConnBlockPortalCount + 1);
                                        WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                                        return;
                                    case WifiProStatisticsManager.STAT_MSG_INCREASE_AC_RS_SAME_COUNT /* 149 */:
                                        WifiProStatisticsRecord wifiProStatisticsRecord14 = WifiProStatisticsManager.this.mNewestStatRcd;
                                        wifiProStatisticsRecord14.mActiveCheckRsSame = (short) (wifiProStatisticsRecord14.mActiveCheckRsSame + 1);
                                        WifiProStatisticsManager.this.mNeedSaveCHRStatistic = true;
                                        return;
                                    case WifiProStatisticsManager.STAT_MSG_INCREASE_HMD_BTN_RI_COUNT /* 150 */:
                                        countBmdNotification();
                                        return;
                                    default:
                                        WifiProStatisticsManager.this.logE("statistics manager got unknow message.");
                                        return;
                                }
                        }
                }
            } else {
                WifiProStatisticsRecord wifiProStatisticsRecord15 = WifiProStatisticsManager.this.mNewestStatRcd;
                wifiProStatisticsRecord15.mNotAutoConnPortalCnt = (short) (wifiProStatisticsRecord15.mNotAutoConnPortalCnt + 1);
                WifiProStatisticsManager.this.mDataBaseManager.addOrUpdateChrStatRcd(WifiProStatisticsManager.this.mNewestStatRcd);
            }
        }
    }

    public void updateTcpTxRxInfo(int txPkts, int txRePkts, int rxPkts, int rtt, int rttPkts) {
        synchronized (this.mStatLock) {
            String tcpInfo = txPkts + "|" + txRePkts + "|" + rxPkts + "|" + rtt + "|" + rttPkts;
            if (this.mTxRxTcpStat.size() < 2) {
                this.mTxRxTcpStat.add(tcpInfo);
            } else {
                this.mTxRxTcpStat.remove(0);
                this.mTxRxTcpStat.add(tcpInfo);
            }
        }
    }

    public void uploadChrNetQualityInfo(int eventId) {
        WifiLinkLayerStats stats;
        Bundle data = new Bundle();
        HwWifiConnectivityMonitor mWifiConnectivityMonitor = HwWifiConnectivityMonitor.getInstance();
        if (mWifiConnectivityMonitor != null) {
            List<String> topUidTcpStat = mWifiConnectivityMonitor.getCircleStat();
            if (topUidTcpStat != null) {
                int size = topUidTcpStat.size();
                for (int i = 0; i < size; i++) {
                    data.putString(TOP_UID_TCP_STAT + i, topUidTcpStat.get(i));
                }
            }
            mWifiConnectivityMonitor.resetCircleStat();
        }
        List<String> topUidTcpStat2 = this.mTxRxTcpStat;
        if (topUidTcpStat2 != null) {
            int size2 = topUidTcpStat2.size();
            for (int i2 = 0; i2 < size2; i2++) {
                data.putString(TX_RX_TCP_STAT + i2, this.mTxRxTcpStat.get(i2));
            }
        }
        WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
        if (!(wifiNative == null || (stats = wifiNative.getWifiLinkLayerStats("wlan0")) == null)) {
            data.putInt(TX_GOOD, (int) (stats.txmpdu_be + stats.txmpdu_bk + stats.txmpdu_vi + stats.txmpdu_vo));
            data.putInt(TX_BAD, (int) (stats.lostmpdu_be + stats.lostmpdu_bk + stats.lostmpdu_vi + stats.lostmpdu_vo));
            data.putInt(RX_GOOD, (int) (stats.rxmpdu_be + stats.rxmpdu_bk + stats.rxmpdu_vi + stats.rxmpdu_vo));
        }
        Bundle dftEventData = new Bundle();
        dftEventData.putInt(EVENT_ID, eventId);
        dftEventData.putBundle(EVENT_DATA, data);
        WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
        resetNetQualityInfo();
    }

    private void resetNetQualityInfo() {
        synchronized (this.mStatLock) {
            this.mTxRxTcpStat.clear();
        }
    }
}
