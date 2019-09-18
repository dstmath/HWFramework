package com.android.server.wifi.wifipro;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.LruCache;
import com.android.server.wifi.HwQoE.HidataWechatTraffic;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifipro.WifiProCHRManager;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WifiProHistoryRecordManager implements IGetApRecordCount {
    private static final int AP_INFO_CACHE_SIZE = 10;
    private static final int CHECK_SAVE_RECORD_INTERVAL = 1800000;
    private static final int DAY_VALUE_SATDAY_CALENDAR = 7;
    private static final int DAY_VALUE_SUNDAY_CALENDAR = 1;
    private static final int DBG_LOG_LEVEL = 1;
    private static final int END_SECONDS_OF_DAY = 86399;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int HOME_AP_BACK_TO_NOT_HOME_AP = 100;
    public static final float HOME_AP_DEFAULT_SWITCH_RATE = 1.0f;
    public static final float HOME_AP_LEVEL_FIVE_SWITCH_RATE = 0.3f;
    public static final float HOME_AP_LEVEL_FOUR_SWITCH_RATE = 0.4f;
    public static final float HOME_AP_LEVEL_ONE_SWITCH_RATE = 0.7f;
    public static final float HOME_AP_LEVEL_THREE_SWITCH_RATE = 0.5f;
    public static final float HOME_AP_LEVEL_TWO_SWITCH_RATE = 0.6f;
    private static final int HOME_AP_MIN_DAY_AVG_REST_TIME = 1800;
    private static final float HOME_AP_MIN_TIME_RATE = 0.5f;
    private static final int HOME_AP_MIN_TOTAL_CONN_HOURS = 10;
    private static final HomeApSwitchRate[] HOME_AP_SWITCH_RATE_TABLE = {new HomeApSwitchRate(10, 0.5f, 0.7f), new HomeApSwitchRate(20, 0.5f, 0.6f), new HomeApSwitchRate(50, 0.5f, 0.5f), new HomeApSwitchRate(100, 0.6f, 0.4f), new HomeApSwitchRate(200, 0.6f, 0.3f)};
    private static final int INFO_LOG_LEVEL = 2;
    public static final int INVALID_HOME_AP_JUDGE_TIME = 0;
    private static final int INVALID_TIME_DELTA = 10;
    private static final int JUDGE_ENTERPRISE_AP_MIN_COUNT = 4;
    private static final int MSG_TRY_DELETE_TOO_OLD_AP_INFO_RECORD = 102;
    private static final int MSG_UPDATE_AP_CONNECT_TIME_CMD = 101;
    private static final int MSG_UPDATE_AP_INFO_CMD = 100;
    private static final int MSG_WIFIPRO_CLOSE = 103;
    private static final int MS_OF_ONE_SECOND = 1000;
    private static final long ONE_DAY_S_VALUE = 86400;
    private static final int REST_TIME_BEGIN_HOUR = 20;
    private static final int REST_TIME_BEGIN_PAST_SECONDS = 72000;
    private static final int REST_TIME_END_HOUR = 7;
    private static final int REST_TIME_END_PAST_SECONDS = 25200;
    private static final int SECOND_OF_ONE_DAY = 86400;
    public static final int SECOND_OF_ONE_HOUR = 3600;
    private static final int SECOND_OF_ONE_MINUTE = 60;
    private static final int START_SECOND_OF_DAY = 0;
    private static final String TAG = "WifiProHistoryRecordManager";
    private static final int TRY_DEL_TOO_OLD_DELAY_TIME = 60000;
    private static WifiProHistoryRecordManager mWifiProHistoryRecordManager;
    private static int printLogLevel = 1;
    /* access modifiers changed from: private */
    public LruCache<String, WifiProApInfoRecord> mApInfoCache = new LruCache<>(10);
    /* access modifiers changed from: private */
    public ApInfoProcessHandler mApInfoProcessHandler;
    private Context mContext;
    /* access modifiers changed from: private */
    public WifiProApInfoRecord mCurrentApInfo;
    private int mCurrentDay = 0;
    private long mCurrentElapsedTime = 0;
    private int mCurrentHour = 0;
    private int mCurrentMinute = 0;
    private int mCurrentSecond = 0;
    /* access modifiers changed from: private */
    public WifiProHistoryDBManager mDBMgr;
    /* access modifiers changed from: private */
    public boolean mHadTryDeleteTooOldRecord = false;
    private Object mLock = new Object();
    private WifiProCHRManager mWifiCHRStateManager;
    private WifiManager mWifiManager;
    private WifiProEnterpriseApRecord mWifiProEnterpriseApRecord = null;
    private WifiProStatisticsManager mWifiProStatisticsManager;

    class ApInfoProcessHandler extends Handler {
        private ApInfoProcessHandler(Looper looper) {
            super(looper);
            WifiProHistoryRecordManager.this.logd("new ApInfoProcessHandler");
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    WifiProHistoryRecordManager.this.logd("UPDATE_AP_INFO_CMD rcv.");
                    SetApInfoPara setPara = (SetApInfoPara) msg.obj;
                    if (setPara == null) {
                        WifiProHistoryRecordManager.this.logd("update ap info null error.");
                        return;
                    }
                    int networkSecurityType = -1;
                    if (setPara.targetBssid != null) {
                        networkSecurityType = WifiProHistoryRecordManager.this.getWifiAuthType(setPara.networkID);
                    }
                    synchronized (WifiProHistoryRecordManager.this.mApInfoCache) {
                        WifiProHistoryRecordManager.this.updateCurrentApInfo(setPara.targetBssid, setPara.setSsid, networkSecurityType);
                    }
                    if (!WifiProHistoryRecordManager.this.mHadTryDeleteTooOldRecord) {
                        WifiProHistoryRecordManager.this.logd("TRY_DELETE_TOO_OLD_AP_INFO event send here.");
                        WifiProHistoryRecordManager.this.mApInfoProcessHandler.sendEmptyMessageDelayed(102, HidataWechatTraffic.MIN_VALID_TIME);
                        boolean unused = WifiProHistoryRecordManager.this.mHadTryDeleteTooOldRecord = true;
                        return;
                    }
                    return;
                case 101:
                    WifiProHistoryRecordManager.this.logd("MSG_UPDATE_AP_CONNECT_TIME_CMD precess start.");
                    synchronized (WifiProHistoryRecordManager.this.mApInfoCache) {
                        WifiProHistoryRecordManager.this.periodSaveRecord(WifiProHistoryRecordManager.this.mCurrentApInfo);
                    }
                    WifiProHistoryRecordManager.this.mApInfoProcessHandler.sendEmptyMessageDelayed(101, 1800000);
                    return;
                case 102:
                    if (WifiProHistoryRecordManager.this.mDBMgr != null) {
                        WifiProHistoryRecordManager.this.logd("restart phone and connect first WIFI trigger too old record delete.");
                        synchronized (WifiProHistoryRecordManager.this.mApInfoCache) {
                            WifiProHistoryRecordManager.this.mDBMgr.removeTooOldApInfoRecord();
                        }
                        return;
                    }
                    return;
                case 103:
                    WifiProHistoryRecordManager.this.logd("MSG_WIFIPRO_CLOSE precess start.");
                    synchronized (WifiProHistoryRecordManager.this.mApInfoCache) {
                        WifiProHistoryRecordManager.this.afterDisconnectProcess(WifiProHistoryRecordManager.this.mCurrentApInfo);
                        WifiProApInfoRecord unused2 = WifiProHistoryRecordManager.this.mCurrentApInfo = null;
                    }
                    return;
                default:
                    WifiProHistoryRecordManager.this.loge("ApInfoProcessHandler got unknow message.");
                    return;
            }
        }
    }

    private static class HomeApSwitchRate {
        public final float REST_TIME_RATE;
        public final float SWITCH_RATE;
        public final int TOTAL_CONNECT_TIME;

        public HomeApSwitchRate(int totalTime, float restTimeRate, float switchRate) {
            this.TOTAL_CONNECT_TIME = totalTime;
            this.REST_TIME_RATE = restTimeRate;
            this.SWITCH_RATE = switchRate;
        }
    }

    private static class SetApInfoPara {
        public int networkID;
        public String setSsid;
        public String targetBssid;

        public SetApInfoPara(String bssid, String ssid, int netID) {
            this.targetBssid = bssid;
            this.setSsid = ssid;
            this.networkID = netID;
        }
    }

    public WifiProHistoryRecordManager(Context context, WifiManager wifiManager) {
        this.mContext = context;
        if (wifiManager != null) {
            this.mWifiManager = wifiManager;
        }
        initStatHandler();
        this.mDBMgr = WifiProHistoryDBManager.getInstance(context);
        this.mWifiCHRStateManager = WifiProCHRManager.getInstance();
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
        this.mWifiProStatisticsManager.registerGetApRecordCountCallBack(this);
    }

    public static WifiProHistoryRecordManager getInstance(Context context, WifiManager wifiManager) {
        if (mWifiProHistoryRecordManager == null) {
            mWifiProHistoryRecordManager = new WifiProHistoryRecordManager(context, wifiManager);
        }
        return mWifiProHistoryRecordManager;
    }

    public void sendEmptyMsg(int what) {
        if (this.mApInfoProcessHandler != null) {
            this.mApInfoProcessHandler.sendEmptyMessage(what);
        }
    }

    public void sendMsg(int what, int arg1, int arg2) {
        if (this.mApInfoProcessHandler != null) {
            this.mApInfoProcessHandler.sendMessage(Message.obtain(this.mApInfoProcessHandler, what, arg1, arg2));
        }
    }

    public void sendMsgWithObj(int what, int arg1, int arg2, Object objarg) {
        if (this.mApInfoProcessHandler != null) {
            this.mApInfoProcessHandler.sendMessage(Message.obtain(this.mApInfoProcessHandler, what, arg1, arg2, objarg));
        }
    }

    public void sendEmptyMsgDelayed(int what, long delayMillis) {
        if (this.mApInfoProcessHandler != null) {
            this.mApInfoProcessHandler.sendEmptyMessageDelayed(what, delayMillis);
        }
    }

    private void initStatHandler() {
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mApInfoProcessHandler = new ApInfoProcessHandler(thread.getLooper());
    }

    private boolean isApInfoRcdValid(WifiProApInfoRecord apInfoRcd) {
        if (apInfoRcd == null) {
            loge("isApInfoRcdValid null .");
            return false;
        } else if (apInfoRcd.apBSSID != null && apInfoRcd.lastConnectTime > 0 && apInfoRcd.firstConnectTime > 0 && apInfoRcd.lastConnectTime >= apInfoRcd.firstConnectTime) {
            return true;
        } else {
            return false;
        }
    }

    private long getCurrentTimeMs() {
        return new Date().getTime();
    }

    private void initFirstConnectApInfo(WifiProApInfoRecord apInfo) {
        if (apInfo != null) {
            long currDateMs = getCurrentTimeMs();
            apInfo.firstConnectTime = currDateMs;
            apInfo.lastConnectTime = currDateMs;
            logd("initFirstConnectApInfo enter for ssid:" + apInfo.apSSID + " , currDateMs" + currDateMs);
        }
    }

    private boolean isValid(WifiConfiguration config) {
        int cc;
        boolean z = false;
        if (config == null) {
            return false;
        }
        logd("config isValid cardinality=" + cc);
        if (cc <= 1) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public int getWifiAuthType(int networkID) {
        if (this.mWifiManager == null) {
            Context context = this.mContext;
            Context context2 = this.mContext;
            this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        }
        if (this.mWifiManager == null) {
            return -1;
        }
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            return -1;
        }
        for (WifiConfiguration config : configNetworks) {
            if (config != null && networkID == config.networkId && isValid(config)) {
                int secType = config.getAuthType();
                logd("getWifiAuthType for network:" + networkID + ", secType = " + secType);
                return secType;
            }
        }
        logd("getWifiAuthType failed ret:-1");
        return -1;
    }

    public void updateCurrConntAp(String apBssid, String apSsid, int networkID) {
        sendMsgWithObj(100, 0, 0, new SetApInfoPara(apBssid, apSsid, networkID));
    }

    public void wifiproClose() {
        logi("mCurrentApInfo bssid not changed.");
        sendEmptyMsg(103);
    }

    /* access modifiers changed from: private */
    public void updateCurrentApInfo(String bssid, String ssid, int securityType) {
        if (bssid == null) {
            if (this.mCurrentApInfo != null) {
                logi("******** updateCurrentApInfo, AP:" + this.mCurrentApInfo.apSSID + " was disconnected. set CurrentApInfo=null");
                afterDisconnectProcess(this.mCurrentApInfo);
                this.mCurrentApInfo = null;
            }
        } else if (this.mCurrentApInfo == null || !bssid.equals(this.mCurrentApInfo.apBSSID)) {
            logi("######## updateCurrentApInfo new SSID:" + ssid);
            if (this.mCurrentApInfo != null) {
                afterDisconnectProcess(this.mCurrentApInfo);
            }
            this.mCurrentApInfo = this.mApInfoCache.get(bssid);
            if (this.mCurrentApInfo == null) {
                this.mCurrentApInfo = new WifiProApInfoRecord(bssid, ssid, securityType);
                if (isEnterpriseAP(ssid, securityType)) {
                    initFirstConnectApInfo(this.mCurrentApInfo);
                    this.mCurrentApInfo.isEnterpriseAP = true;
                    logi("Connected to enterprise AP.");
                } else {
                    queryApInfo(bssid, this.mCurrentApInfo);
                    if (!isApInfoRcdValid(this.mCurrentApInfo)) {
                        initFirstConnectApInfo(this.mCurrentApInfo);
                        saveApInfoRecord(this.mCurrentApInfo);
                    }
                }
                this.mApInfoCache.put(bssid, this.mCurrentApInfo);
            } else {
                logd(" get mCurrentApInfo inCache for new bssid. catch size:" + this.mApInfoCache.size());
            }
            afterConnectProcess(this.mCurrentApInfo);
            if (this.mCurrentApInfo != null) {
                if (ssid != null) {
                    this.mCurrentApInfo.apSSID = ssid;
                }
                this.mCurrentApInfo.apSecurityType = securityType;
            }
        } else {
            logi("mCurrentApInfo bssid not changed.");
        }
    }

    private boolean queryApInfo(String apBssid, WifiProApInfoRecord apInfo) {
        if (this.mDBMgr == null || apInfo == null || apBssid == null) {
            return false;
        }
        if (!this.mDBMgr.queryApInfoRecord(apBssid, apInfo)) {
            logi("queryApInfoRecord failed.");
            return false;
        }
        logi("queryApInfoRecord succ.");
        return true;
    }

    private void afterConnectProcess(WifiProApInfoRecord apInfo) {
        if (apInfo != null && this.mApInfoProcessHandler != null) {
            logd("afterConnectProcess enter for ssid:" + apInfo.apSSID);
            if (!apInfo.isEnterpriseAP) {
                apInfo.connectStartTimeSave = getCurrentTimeMs();
                apInfo.lastConnectTime = getCurrentTimeMs();
                updateRecordTime(apInfo);
                if (this.mApInfoProcessHandler.hasMessages(101)) {
                    this.mApInfoProcessHandler.removeMessages(101);
                }
                logd("UPDATE_AP_CONNECT_TIME_CMD is send.");
                sendEmptyMsgDelayed(101, 1800000);
                homeAPJudgeProcess(apInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    public void periodSaveRecord(WifiProApInfoRecord apInfo) {
        if (apInfo == null || this.mDBMgr == null) {
            logd("periodSaveRecord null error.");
            return;
        }
        if (!apInfo.isEnterpriseAP) {
            logd("periodSaveRecord enter for normal ssid:" + apInfo.apSSID);
            apInfo.lastConnectTime = getCurrentTimeMs();
            updateConnectTime(apInfo, apInfo.connectStartTimeSave, apInfo.lastConnectTime);
            this.mDBMgr.addOrUpdateApInfoRecord(apInfo);
            apInfo.connectStartTimeSave = getCurrentTimeMs();
            homeAPJudgeProcess(apInfo);
        }
        if (this.mWifiProStatisticsManager != null) {
            this.mWifiProStatisticsManager.updateStatisticToDB();
        }
    }

    /* access modifiers changed from: private */
    public void afterDisconnectProcess(WifiProApInfoRecord apInfo) {
        if (apInfo != null && this.mDBMgr != null) {
            if (!apInfo.isEnterpriseAP) {
                logd("afterDisconnectProcess enter for normal ssid:" + apInfo.apSSID);
                apInfo.lastConnectTime = getCurrentTimeMs();
                updateConnectTime(apInfo, apInfo.connectStartTimeSave, apInfo.lastConnectTime);
                saveApInfoRecord(apInfo);
                if (this.mApInfoProcessHandler.hasMessages(101)) {
                    this.mApInfoProcessHandler.removeMessages(101);
                }
            } else {
                logd("afterDisconnectProcess enter ssid:" + apInfo.apSSID + ", is enterprise AP.");
            }
            if (this.mWifiProStatisticsManager != null) {
                this.mWifiProStatisticsManager.updateStatisticToDB();
            }
        }
    }

    public boolean isEnterpriseAP(String ssid, int secType) {
        boolean retVal;
        if (ssid == null) {
            return false;
        }
        boolean retVal2 = false;
        if (this.mWifiProEnterpriseApRecord != null && this.mWifiProEnterpriseApRecord.apSSID.equals(ssid) && this.mWifiProEnterpriseApRecord.apSecurityType == secType) {
            retVal2 = true;
        }
        if (!retVal2) {
            retVal = this.mDBMgr.queryEnterpriseApRecord(ssid, secType);
            if (retVal) {
                logi("isEnterpriseAP match from DB.");
                this.mWifiProEnterpriseApRecord = new WifiProEnterpriseApRecord(ssid, secType);
            }
        } else {
            logi("isEnterpriseAP match from catch.");
            retVal = true;
        }
        logi("isEnterpriseAP return " + retVal);
        return retVal;
    }

    private void saveApInfoRecord(WifiProApInfoRecord apInfo) {
        if (apInfo != null && this.mDBMgr != null && this.mWifiCHRStateManager != null) {
            if (apInfo.isEnterpriseAP) {
                logi("saveApInfoRecord: do not save Enterprise AP to DB.");
                return;
            }
            int sameCount = this.mDBMgr.querySameSSIDApCount(apInfo.apBSSID, apInfo.apSSID, apInfo.apSecurityType);
            logi("saveApInfoRecord DB same AP Count = " + sameCount);
            if (sameCount < 4) {
                this.mDBMgr.addOrUpdateApInfoRecord(apInfo);
            } else if (this.mDBMgr.deleteEnterpriseApRecord(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, apInfo.apSSID, apInfo.apSecurityType)) {
                apInfo.isEnterpriseAP = true;
                this.mApInfoCache.evictAll();
                this.mApInfoCache.put(apInfo.apBSSID, apInfo);
                logi("after clean ap info record catch. size=" + this.mApInfoCache.size());
                this.mDBMgr.addOrUpdateEnterpriseApRecord(apInfo.apSSID, apInfo.apSecurityType);
                this.mWifiProEnterpriseApRecord = new WifiProEnterpriseApRecord(apInfo.apSSID, apInfo.apSecurityType);
                logi("add new Enterprise Ap Record succ.");
                this.mWifiCHRStateManager.updateSSID(apInfo.apSSID);
                this.mWifiCHRStateManager.updateAPSecurityType(apInfo.apSecurityType);
                this.mWifiCHRStateManager.updateWifiException(HwQoEUtils.QOE_MSG_UPDATE_QUALITY_INFO, "ENTERPRISE_AP_INFO");
            } else {
                logi("delete Enterprise Record from AP info DB failed.");
            }
        }
    }

    private void updateConnectTimeInOneDay(WifiProApInfoRecord apInfo, int day, int startTime, int endTime) {
        int restTime = 0;
        if (apInfo != null) {
            apInfo.totalUseTime += endTime - startTime;
            if (day == 1 || day == 7) {
                apInfo.totalUseTimeAtWeekend += endTime - startTime;
                logd("add weekend home time " + restTime + "s");
                return;
            }
            if (startTime < REST_TIME_END_PAST_SECONDS) {
                restTime = endTime < REST_TIME_END_PAST_SECONDS ? 0 + (endTime - startTime) : endTime < REST_TIME_BEGIN_PAST_SECONDS ? 0 + (REST_TIME_END_PAST_SECONDS - startTime) : 0 + (REST_TIME_END_PAST_SECONDS - startTime) + (endTime - REST_TIME_BEGIN_PAST_SECONDS);
            } else if (startTime < REST_TIME_BEGIN_PAST_SECONDS) {
                if (endTime >= REST_TIME_BEGIN_PAST_SECONDS && endTime >= REST_TIME_BEGIN_PAST_SECONDS) {
                    restTime = 0 + (endTime - REST_TIME_BEGIN_PAST_SECONDS);
                }
            } else if (startTime >= REST_TIME_BEGIN_PAST_SECONDS) {
                restTime = 0 + (endTime - startTime);
            }
            apInfo.totalUseTimeAtNight += restTime;
            logd("add night home time " + restTime + "s");
        }
    }

    /* access modifiers changed from: package-private */
    public void updateRecordTime(WifiProApInfoRecord apInfo) {
        if (apInfo != null) {
            Calendar cal = Calendar.getInstance();
            this.mCurrentDay = cal.get(7);
            this.mCurrentHour = cal.get(11);
            this.mCurrentMinute = cal.get(12);
            this.mCurrentSecond = cal.get(13);
            this.mCurrentElapsedTime = SystemClock.elapsedRealtime();
            apInfo.lastRecordDay = this.mCurrentDay;
            apInfo.lastRecordHour = this.mCurrentHour;
            apInfo.lastRecordMin = this.mCurrentMinute;
            apInfo.lastRecordSec = this.mCurrentSecond;
            apInfo.lastRecordRealTime = this.mCurrentElapsedTime;
        }
    }

    private void updateConnectTime(WifiProApInfoRecord apInfo, long startDate, long endDate) {
        WifiProApInfoRecord wifiProApInfoRecord = apInfo;
        if (wifiProApInfoRecord != null) {
            int lastRecordDay = wifiProApInfoRecord.lastRecordDay;
            long lastRecordRealTime = wifiProApInfoRecord.lastRecordRealTime;
            int startSecondsOfDay = (wifiProApInfoRecord.lastRecordHour * SECOND_OF_ONE_HOUR) + (wifiProApInfoRecord.lastRecordMin * SECOND_OF_ONE_MINUTE) + wifiProApInfoRecord.lastRecordSec;
            updateRecordTime(apInfo);
            int endSecondsOfDay = (this.mCurrentHour * SECOND_OF_ONE_HOUR) + (this.mCurrentMinute * SECOND_OF_ONE_MINUTE) + this.mCurrentSecond;
            long durationCalendar = (endDate - startDate) / 1000;
            long realDuration = (this.mCurrentElapsedTime - lastRecordRealTime) / 1000;
            long absVal = Math.abs(durationCalendar - realDuration);
            long j = lastRecordRealTime;
            logd("durationCalendar=" + durationCalendar + ", realDuration=" + realDuration);
            if (durationCalendar < 0 || absVal > 10) {
                logd("updateConnectTime invalid date recod, return");
            } else if (durationCalendar > ONE_DAY_S_VALUE) {
                logd("update connection time more than 1 day, ignore");
            } else {
                if (this.mCurrentDay != lastRecordDay) {
                    updateConnectTimeInOneDay(wifiProApInfoRecord, lastRecordDay, startSecondsOfDay, END_SECONDS_OF_DAY);
                    updateConnectTimeInOneDay(wifiProApInfoRecord, this.mCurrentDay, 0, endSecondsOfDay);
                } else {
                    updateConnectTimeInOneDay(wifiProApInfoRecord, this.mCurrentDay, startSecondsOfDay, endSecondsOfDay);
                }
                logd("calcTotalConnectTime record total=" + wifiProApInfoRecord.totalUseTime + "s, record night=" + wifiProApInfoRecord.totalUseTimeAtNight + "s, record weekend=" + wifiProApInfoRecord.totalUseTimeAtWeekend);
            }
        }
    }

    private boolean checkIsHomeAP(WifiProApInfoRecord apInfo) {
        if (apInfo == null || this.mDBMgr == null) {
            return false;
        }
        if (apInfo.isEnterpriseAP) {
            logi("checkIsHomeAP: do not check Enterprise AP to DB.");
            return false;
        }
        long totPassTime = (apInfo.lastConnectTime - apInfo.firstConnectTime) / 1000;
        if (totPassTime > 0) {
            long dayAvgRestTime = 0;
            long pass_days = totPassTime / ONE_DAY_S_VALUE;
            if (pass_days != 0) {
                dayAvgRestTime = ((long) (apInfo.totalUseTimeAtNight + apInfo.totalUseTimeAtWeekend)) / pass_days;
            }
            logi("checkIsHomeAP , pass time (s) =" + totPassTime + ", pass day=" + pass_days + ", tot rest time:" + (apInfo.totalUseTimeAtNight + apInfo.totalUseTimeAtWeekend));
            if (dayAvgRestTime < 1800) {
                logi("checkIsHomeAP dayAvgRestTime not enough return default rate");
                return false;
            }
        }
        int totalConnectHours = apInfo.totalUseTime / SECOND_OF_ONE_HOUR;
        float restTimeRate = 0.0f;
        if (apInfo.totalUseTime != 0) {
            restTimeRate = ((float) (apInfo.totalUseTimeAtNight + apInfo.totalUseTimeAtWeekend)) / ((float) apInfo.totalUseTime);
        }
        logi("checkIsHomeAP totalConnectHours:" + totalConnectHours + ", restTimeRate=" + restTimeRate);
        if (totalConnectHours <= 10 || restTimeRate <= 0.5f) {
            return false;
        }
        logi("checkIsHomeAP SSID:" + apInfo.apSSID + " is home AP");
        return true;
    }

    private void homeAPJudgeProcess(WifiProApInfoRecord apInfo) {
        if (apInfo != null && this.mDBMgr != null && this.mWifiCHRStateManager != null) {
            if (checkIsHomeAP(apInfo)) {
                if (apInfo.judgeHomeAPTime == 0) {
                    apInfo.judgeHomeAPTime = apInfo.lastConnectTime;
                    this.mDBMgr.addOrUpdateApInfoRecord(apInfo);
                    this.mWifiCHRStateManager.updateSSID(apInfo.apSSID);
                    this.mWifiCHRStateManager.updateAPSecurityType(apInfo.apSecurityType);
                    long judgeTime = (apInfo.judgeHomeAPTime - apInfo.firstConnectTime) / 3600000;
                    logd("Home ap judge time(hour):" + judgeTime);
                    this.mWifiCHRStateManager.updateHomeAPJudgeTime((int) judgeTime);
                    this.mWifiCHRStateManager.updateWifiException(HwQoEUtils.QOE_MSG_UPDATE_QUALITY_INFO, "HOME_AP_INFO");
                } else {
                    logd("already record as home ap.");
                }
            } else if (apInfo.judgeHomeAPTime > 0) {
                this.mWifiCHRStateManager.updateSSID(apInfo.apSSID);
                this.mWifiCHRStateManager.updateAPSecurityType(100);
                long homeApTime = (apInfo.lastConnectTime - apInfo.judgeHomeAPTime) / 3600000;
                logd("Home ap last time(hour):" + homeApTime);
                this.mWifiCHRStateManager.updateHomeAPJudgeTime((int) homeApTime);
                this.mWifiCHRStateManager.updateWifiException(HwQoEUtils.QOE_MSG_UPDATE_QUALITY_INFO, "HOME_AP_INFO");
                apInfo.judgeHomeAPTime = 0;
                this.mDBMgr.addOrUpdateApInfoRecord(apInfo);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0029, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002b, code lost:
        return false;
     */
    public boolean getIsHomeAP(String bssid) {
        synchronized (this.mApInfoCache) {
            boolean z = false;
            if (this.mCurrentApInfo != null) {
                if (bssid != null) {
                    if (!bssid.equals(this.mCurrentApInfo.apBSSID)) {
                        logd("getIsHomeAP false for different BSSID AP.");
                        return false;
                    } else if (this.mCurrentApInfo.judgeHomeAPTime > 0) {
                        z = true;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:46:0x010d, code lost:
        return 1.0f;
     */
    public float getHomeApSwitchRate(String bssid) {
        float rate;
        synchronized (this.mApInfoCache) {
            if (this.mCurrentApInfo != null) {
                if (bssid != null) {
                    if (!this.mCurrentApInfo.isEnterpriseAP) {
                        if (this.mCurrentApInfo.judgeHomeAPTime != 0) {
                            if (!bssid.equals(this.mCurrentApInfo.apBSSID)) {
                                logd("HomeApSwitchRate return default rate for different BSSID AP.");
                                return 1.0f;
                            }
                            int index = HOME_AP_SWITCH_RATE_TABLE.length - 1;
                            int totalConnectHours = this.mCurrentApInfo.totalUseTime / SECOND_OF_ONE_HOUR;
                            float restTimeRate = 0.0f;
                            if (this.mCurrentApInfo.totalUseTime != 0) {
                                restTimeRate = ((float) (this.mCurrentApInfo.totalUseTimeAtNight + this.mCurrentApInfo.totalUseTimeAtWeekend)) / ((float) this.mCurrentApInfo.totalUseTime);
                            }
                            long totPassTime = (this.mCurrentApInfo.lastConnectTime - this.mCurrentApInfo.firstConnectTime) / 1000;
                            if (totPassTime > 0) {
                                long dayAvgRestTime = 0;
                                long pass_days = totPassTime / ONE_DAY_S_VALUE;
                                if (pass_days != 0) {
                                    dayAvgRestTime = ((long) (this.mCurrentApInfo.totalUseTimeAtNight + this.mCurrentApInfo.totalUseTimeAtWeekend)) / pass_days;
                                }
                                logi("HomeApSwitchRate, pass time (s) =" + totPassTime + ", pass day=" + pass_days + ", tot rest time:" + (this.mCurrentApInfo.totalUseTimeAtNight + this.mCurrentApInfo.totalUseTimeAtWeekend));
                                if (dayAvgRestTime < 1800) {
                                    logi("HomeApSwitchRate dayAvgRestTime not enough return default rate");
                                    return 1.0f;
                                }
                            }
                            logi("HomeApSwitchRate totalConnectHours:" + totalConnectHours + ", restTimeRate=" + restTimeRate);
                            while (index >= 0) {
                                if (totalConnectHours <= HOME_AP_SWITCH_RATE_TABLE[index].TOTAL_CONNECT_TIME || restTimeRate <= HOME_AP_SWITCH_RATE_TABLE[index].REST_TIME_RATE) {
                                    index--;
                                } else {
                                    logi("HomeApSwitchRate, rate=" + rate);
                                    return rate;
                                }
                            }
                            return 1.0f;
                        }
                    }
                    logd("HomeApSwitchRate return default rate for not Home AP.");
                    return 1.0f;
                }
            }
        }
    }

    public synchronized boolean statisticApInfoRecord() {
        if (this.mDBMgr == null) {
            return false;
        }
        return this.mDBMgr.statisticApInfoRecord();
    }

    public int getTotRecordCount() {
        if (this.mDBMgr == null) {
            return 0;
        }
        return this.mDBMgr.getTotRecordCount();
    }

    public int getHomeApRecordCount() {
        if (this.mDBMgr == null) {
            return 0;
        }
        return this.mDBMgr.getHomeApRecordCount();
    }

    public void addHistoryHSCount(int incCount) {
        logd("addHistoryHSCount inc val:" + incCount);
        synchronized (this.mApInfoCache) {
            if (this.mCurrentApInfo != null) {
                this.mCurrentApInfo.highSpdFreq += incCount;
                logd("addHistoryHSCount total:" + this.mCurrentApInfo.highSpdFreq);
            }
        }
    }

    public WifiProApInfoRecord getApInfoRecord(String apBssid) {
        if (this.mDBMgr == null || apBssid == null) {
            return null;
        }
        synchronized (this.mApInfoCache) {
            WifiProApInfoRecord retApInfo = this.mApInfoCache.get(apBssid);
            if (retApInfo == null) {
                WifiProApInfoRecord retApInfo2 = new WifiProApInfoRecord(apBssid, null, 0);
                if (!queryApInfo(apBssid, retApInfo2)) {
                    return null;
                }
                return retApInfo2;
            }
            logd(" get mCurrentApInfo inCache for new bssid. catch size:" + this.mApInfoCache.size());
            WifiProApInfoRecord wifiProApInfoRecord = new WifiProApInfoRecord(retApInfo);
            return wifiProApInfoRecord;
        }
    }

    public boolean loadApQualityRecord(String apBssid, WifiProApQualityRcd apQR) {
        if (this.mDBMgr == null || apQR == null || apBssid == null) {
            return false;
        }
        if (!this.mDBMgr.queryApQualityRcd(apBssid, apQR)) {
            logi("loadApQualityRecord failed.");
            return false;
        }
        logi("loadApQualityRecord succ.");
        return true;
    }

    public void saveApQualityRecord(WifiProApQualityRcd apQR) {
        if (this.mDBMgr != null && apQR != null) {
            if (!this.mDBMgr.addOrUpdateApQualityRcd(apQR)) {
                logi("saveApQualityRecord failed.");
            } else {
                logi("saveApQualityRecord succ.");
            }
        }
    }

    public WifiProDualBandApInfoRcd getDualBandApRecord(String apBssid) {
        synchronized (this.mLock) {
            if (apBssid == null) {
                try {
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                WifiProDualBandApInfoRcd record = new WifiProDualBandApInfoRcd(apBssid);
                if (this.mDBMgr.queryDualBandApInfoRcd(apBssid, record)) {
                    return record;
                }
                return null;
            }
        }
    }

    public List<WifiProDualBandApInfoRcd> getDualBandApInfoBySsid(String ssid) {
        synchronized (this.mLock) {
            if (ssid == null) {
                try {
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                List<WifiProDualBandApInfoRcd> queryDualBandApInfoRcdBySsid = this.mDBMgr.queryDualBandApInfoRcdBySsid(ssid);
                return queryDualBandApInfoRcdBySsid;
            }
        }
    }

    public boolean saveDualBandApInfo(WifiProDualBandApInfoRcd apInfo) {
        if (this.mDBMgr == null || apInfo == null) {
            return false;
        }
        logd("saveDualBandApInfo apInfo.mApSSID = " + apInfo.mApSSID);
        return this.mDBMgr.addOrUpdateDualBandApInfoRcd(apInfo);
    }

    public boolean deleteDualBandApInfo(String bssid) {
        if (this.mDBMgr == null || bssid == null) {
            return false;
        }
        return this.mDBMgr.deleteDualBandApInfoRcd(bssid);
    }

    public boolean saveRelateApInfo(WifiProRelateApRcd relateApInfo) {
        if (this.mDBMgr == null || relateApInfo == null) {
            return false;
        }
        return this.mDBMgr.addOrUpdateRelateApRcd(relateApInfo);
    }

    public boolean deleteRelateApInfo(String bssid) {
        if (this.mDBMgr == null || bssid == null) {
            return false;
        }
        return this.mDBMgr.deleteRelateApRcd(bssid);
    }

    public boolean deleteRelate5GApInfo(String bssid) {
        if (this.mDBMgr == null || bssid == null) {
            return false;
        }
        return this.mDBMgr.deleteRelate5GAPRcd(bssid);
    }

    public boolean getRelateApList(String apBssid, List<WifiProRelateApRcd> relateApList) {
        if (this.mDBMgr == null || apBssid == null || relateApList == null) {
            return false;
        }
        return this.mDBMgr.queryRelateApRcd(apBssid, relateApList);
    }

    public boolean updateRSSIThreshold(String apBssid, int[] rssiEntries) {
        if (this.mDBMgr == null || apBssid == null) {
            return false;
        }
        return this.mDBMgr.addOrUpdateApRSSIThreshold(apBssid, convertArrayToString(rssiEntries));
    }

    public int[] getRSSIThreshold(String apBssid) {
        if (this.mDBMgr == null || apBssid == null) {
            return new int[0];
        }
        String temp = this.mDBMgr.queryApRSSIThreshold(apBssid);
        if (temp == null) {
            return new int[0];
        }
        return convertStringToArray(temp);
    }

    private String convertArrayToString(int[] list) {
        int lenght = list.length;
        if (lenght == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lenght - 1; i++) {
            result.append(String.valueOf(list[i]) + ",");
        }
        result.append(String.valueOf(list[lenght - 1]));
        return result.toString();
    }

    private int[] convertStringToArray(String str) {
        String[] temp = str.split(",");
        int length = temp.length;
        int[] result = new int[length];
        if (length == 0) {
            return new int[0];
        }
        int i = 0;
        while (i < length) {
            try {
                result[i] = Integer.parseInt(temp[i]);
                i++;
            } catch (NumberFormatException e) {
                loge("NumberFormatException:" + e);
                return new int[0];
            }
        }
        return result;
    }

    public void closeWifiProHistoryDB() {
        this.mDBMgr.closeDB();
    }

    /* access modifiers changed from: private */
    public void logd(String msg) {
        if (printLogLevel <= 1) {
            Log.d(TAG, msg);
        }
    }

    private void logi(String msg) {
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

    public boolean isHaveMultipleAP(String bssid, String ssid, int type) {
        int sameCount = this.mDBMgr.querySameSSIDApCount(bssid, ssid, type);
        loge("isHaveMultipleAP sameCount = " + sameCount);
        if (sameCount >= 1) {
            return true;
        }
        return false;
    }
}
