package com.huawei.hwwifiproservice;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.LruCache;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.Calendar;
import java.util.Collections;
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
    private final LruCache<String, WifiProApInfoRecord> mApInfoCache = new LruCache<>(10);
    private ApInfoProcessHandler mApInfoProcessHandler;
    private Context mContext;
    private WifiProApInfoRecord mCurrentApInfo;
    private int mCurrentDay = 0;
    private long mCurrentElapsedTime = 0;
    private int mCurrentHour = 0;
    private int mCurrentMinute = 0;
    private int mCurrentSecond = 0;
    private WifiProHistoryDBManager mDBMgr;
    private boolean mHadTryDeleteTooOldRecord = false;
    private final Object mLock = new Object();
    private WifiManager mWifiManager;
    private WifiProEnterpriseApRecord mWifiProEnterpriseApRecord = null;
    private WifiProStatisticsManager mWifiProStatisticsManager;

    /* access modifiers changed from: private */
    public static class SetApInfoPara {
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
        ApInfoProcessHandler apInfoProcessHandler = this.mApInfoProcessHandler;
        if (apInfoProcessHandler != null) {
            apInfoProcessHandler.sendEmptyMessage(what);
        }
    }

    public void sendMsg(int what, int arg1, int arg2) {
        ApInfoProcessHandler apInfoProcessHandler = this.mApInfoProcessHandler;
        if (apInfoProcessHandler != null) {
            apInfoProcessHandler.sendMessage(Message.obtain(apInfoProcessHandler, what, arg1, arg2));
        }
    }

    public void sendMsgWithObj(int what, int arg1, int arg2, Object objarg) {
        ApInfoProcessHandler apInfoProcessHandler = this.mApInfoProcessHandler;
        if (apInfoProcessHandler != null) {
            apInfoProcessHandler.sendMessage(Message.obtain(apInfoProcessHandler, what, arg1, arg2, objarg));
        }
    }

    public void sendEmptyMsgDelayed(int what, long delayMillis) {
        ApInfoProcessHandler apInfoProcessHandler = this.mApInfoProcessHandler;
        if (apInfoProcessHandler != null) {
            apInfoProcessHandler.sendEmptyMessageDelayed(what, delayMillis);
        }
    }

    private void initStatHandler() {
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mApInfoProcessHandler = new ApInfoProcessHandler(thread.getLooper());
    }

    private boolean isApInfoRcdValid(WifiProApInfoRecord apInfoRcd) {
        if (apInfoRcd == null) {
            HwHiLog.e(TAG, false, "isApInfoRcdValid null .", new Object[0]);
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
            HwHiLog.d(TAG, false, "initFirstConnectApInfo enter for ssid:%{public}s, currDateMs %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(apInfo.apSSID), String.valueOf(currDateMs)});
        }
    }

    private boolean isValid(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        int cc = config.allowedKeyManagement.cardinality();
        HwHiLog.d(TAG, false, "config isValid cardinality=%{public}d", new Object[]{Integer.valueOf(cc)});
        if (cc <= 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getWifiAuthType(int networkID) {
        List<WifiConfiguration> configNetworks;
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        if (this.mWifiManager == null || (configNetworks = WifiproUtils.getAllConfiguredNetworks()) == null || configNetworks.size() == 0) {
            return -1;
        }
        for (WifiConfiguration config : configNetworks) {
            if (config != null && networkID == config.networkId && isValid(config)) {
                int secType = config.getAuthType();
                HwHiLog.d(TAG, false, "getWifiAuthType for network:%{public}d, secType = %{public}d", new Object[]{Integer.valueOf(networkID), Integer.valueOf(secType)});
                return secType;
            }
        }
        HwHiLog.d(TAG, false, "getWifiAuthType failed ret:%{public}d", new Object[]{-1});
        return -1;
    }

    public void updateCurrConntAp(String apBssid, String apSsid, int networkID) {
        sendMsgWithObj(100, 0, 0, new SetApInfoPara(apBssid, apSsid, networkID));
    }

    public void wifiproClose() {
        HwHiLog.i(TAG, false, "mCurrentApInfo bssid not changed.", new Object[0]);
        sendEmptyMsg(103);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCurrentApInfo(String bssid, String ssid, int securityType) {
        if (bssid == null) {
            WifiProApInfoRecord wifiProApInfoRecord = this.mCurrentApInfo;
            if (wifiProApInfoRecord != null) {
                HwHiLog.i(TAG, false, "******** updateCurrentApInfo, AP:%{public}s was disconnected. set CurrentApInfo=null", new Object[]{StringUtilEx.safeDisplaySsid(wifiProApInfoRecord.apSSID)});
                afterDisconnectProcess(this.mCurrentApInfo);
                this.mCurrentApInfo = null;
                return;
            }
            return;
        }
        WifiProApInfoRecord wifiProApInfoRecord2 = this.mCurrentApInfo;
        if (wifiProApInfoRecord2 == null || !bssid.equals(wifiProApInfoRecord2.apBSSID)) {
            HwHiLog.i(TAG, false, "######## updateCurrentApInfo new SSID:%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid)});
            WifiProApInfoRecord wifiProApInfoRecord3 = this.mCurrentApInfo;
            if (wifiProApInfoRecord3 != null) {
                afterDisconnectProcess(wifiProApInfoRecord3);
            }
            this.mCurrentApInfo = this.mApInfoCache.get(bssid);
            if (this.mCurrentApInfo == null) {
                this.mCurrentApInfo = new WifiProApInfoRecord(bssid, ssid, securityType);
                if (isEnterpriseAP(ssid, securityType)) {
                    initFirstConnectApInfo(this.mCurrentApInfo);
                    this.mCurrentApInfo.isEnterpriseAP = true;
                    HwHiLog.i(TAG, false, "Connected to enterprise AP.", new Object[0]);
                } else {
                    queryApInfo(bssid, this.mCurrentApInfo);
                    if (!isApInfoRcdValid(this.mCurrentApInfo)) {
                        initFirstConnectApInfo(this.mCurrentApInfo);
                        saveApInfoRecord(this.mCurrentApInfo);
                    }
                }
                this.mApInfoCache.put(bssid, this.mCurrentApInfo);
            } else {
                HwHiLog.d(TAG, false, " get mCurrentApInfo inCache for new bssid. catch size:%{public}d", new Object[]{Integer.valueOf(this.mApInfoCache.size())});
            }
            afterConnectProcess(this.mCurrentApInfo);
            WifiProApInfoRecord wifiProApInfoRecord4 = this.mCurrentApInfo;
            if (wifiProApInfoRecord4 != null) {
                if (ssid != null) {
                    wifiProApInfoRecord4.apSSID = ssid;
                }
                this.mCurrentApInfo.apSecurityType = securityType;
                return;
            }
            return;
        }
        HwHiLog.i(TAG, false, "mCurrentApInfo bssid not changed.", new Object[0]);
    }

    private boolean queryApInfo(String apBssid, WifiProApInfoRecord apInfo) {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager == null || apInfo == null || apBssid == null) {
            return false;
        }
        if (!wifiProHistoryDBManager.queryApInfoRecord(apBssid, apInfo)) {
            HwHiLog.i(TAG, false, "queryApInfoRecord failed.", new Object[0]);
            return false;
        }
        HwHiLog.i(TAG, false, "queryApInfoRecord succ.", new Object[0]);
        return true;
    }

    private void afterConnectProcess(WifiProApInfoRecord apInfo) {
        if (apInfo != null && this.mApInfoProcessHandler != null) {
            HwHiLog.d(TAG, false, "afterConnectProcess enter for ssid:%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(apInfo.apSSID)});
            if (!apInfo.isEnterpriseAP) {
                apInfo.connectStartTimeSave = getCurrentTimeMs();
                apInfo.lastConnectTime = getCurrentTimeMs();
                updateRecordTime(apInfo);
                if (this.mApInfoProcessHandler.hasMessages(101)) {
                    this.mApInfoProcessHandler.removeMessages(101);
                }
                HwHiLog.d(TAG, false, "UPDATE_AP_CONNECT_TIME_CMD is send.", new Object[0]);
                sendEmptyMsgDelayed(101, 1800000);
                homeAPJudgeProcess(apInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void periodSaveRecord(WifiProApInfoRecord apInfo) {
        if (apInfo == null || this.mDBMgr == null) {
            HwHiLog.d(TAG, false, "periodSaveRecord null error.", new Object[0]);
            return;
        }
        if (!apInfo.isEnterpriseAP) {
            HwHiLog.d(TAG, false, "periodSaveRecord enter for normal ssid:%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(apInfo.apSSID)});
            apInfo.lastConnectTime = getCurrentTimeMs();
            updateConnectTime(apInfo, apInfo.connectStartTimeSave, apInfo.lastConnectTime);
            this.mDBMgr.addOrUpdateApInfoRecord(apInfo);
            apInfo.connectStartTimeSave = getCurrentTimeMs();
            homeAPJudgeProcess(apInfo);
        }
        WifiProStatisticsManager wifiProStatisticsManager = this.mWifiProStatisticsManager;
        if (wifiProStatisticsManager != null) {
            wifiProStatisticsManager.updateStatisticToDB();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void afterDisconnectProcess(WifiProApInfoRecord apInfo) {
        if (apInfo != null && this.mDBMgr != null) {
            if (!apInfo.isEnterpriseAP) {
                HwHiLog.d(TAG, false, "afterDisconnectProcess enter for normal ssid:%{public}s", new Object[]{StringUtilEx.safeDisplaySsid(apInfo.apSSID)});
                apInfo.lastConnectTime = getCurrentTimeMs();
                updateConnectTime(apInfo, apInfo.connectStartTimeSave, apInfo.lastConnectTime);
                saveApInfoRecord(apInfo);
                if (this.mApInfoProcessHandler.hasMessages(101)) {
                    this.mApInfoProcessHandler.removeMessages(101);
                }
            } else {
                HwHiLog.d(TAG, false, "afterDisconnectProcess enter ssid:%{public}s, is enterprise AP.", new Object[]{StringUtilEx.safeDisplaySsid(apInfo.apSSID)});
            }
            WifiProStatisticsManager wifiProStatisticsManager = this.mWifiProStatisticsManager;
            if (wifiProStatisticsManager != null) {
                wifiProStatisticsManager.updateStatisticToDB();
            }
        }
    }

    public boolean isEnterpriseAP(String ssid, int secType) {
        boolean retVal;
        if (ssid == null) {
            return false;
        }
        boolean retVal2 = false;
        WifiProEnterpriseApRecord wifiProEnterpriseApRecord = this.mWifiProEnterpriseApRecord;
        if (wifiProEnterpriseApRecord != null && ssid.equals(wifiProEnterpriseApRecord.getApSsid()) && this.mWifiProEnterpriseApRecord.getApSecurityType() == secType) {
            retVal2 = true;
        }
        if (!retVal2) {
            retVal = this.mDBMgr.queryEnterpriseApRecord(ssid, secType);
            if (retVal) {
                HwHiLog.i(TAG, false, "isEnterpriseAP match from DB.", new Object[0]);
                this.mWifiProEnterpriseApRecord = new WifiProEnterpriseApRecord(ssid, secType);
            }
        } else {
            HwHiLog.i(TAG, false, "isEnterpriseAP match from catch.", new Object[0]);
            retVal = true;
        }
        HwHiLog.i(TAG, false, "isEnterpriseAP return %{public}s", new Object[]{String.valueOf(retVal)});
        return retVal;
    }

    private void saveApInfoRecord(WifiProApInfoRecord apInfo) {
        if (apInfo.isEnterpriseAP) {
            HwHiLog.i(TAG, false, "saveApInfoRecord: do not save Enterprise AP to DB.", new Object[0]);
            return;
        }
        int sameCount = this.mDBMgr.querySameSsidApCount(apInfo.apBSSID, apInfo.apSSID, apInfo.apSecurityType);
        HwHiLog.i(TAG, false, "saveApInfoRecord DB same AP Count = %{public}d", new Object[]{Integer.valueOf(sameCount)});
        if (sameCount < 4) {
            this.mDBMgr.addOrUpdateApInfoRecord(apInfo);
        } else if (this.mDBMgr.deleteEnterpriseApRecord(WifiProHistoryDBHelper.WP_AP_INFO_TB_NAME, apInfo.apSSID, apInfo.apSecurityType)) {
            apInfo.isEnterpriseAP = true;
            this.mApInfoCache.evictAll();
            this.mApInfoCache.put(apInfo.apBSSID, apInfo);
            HwHiLog.i(TAG, false, "after clean ap info record catch. size=%{public}d", new Object[]{Integer.valueOf(this.mApInfoCache.size())});
            this.mDBMgr.addOrUpdateEnterpriseApRecord(apInfo.apSSID, apInfo.apSecurityType);
            this.mWifiProEnterpriseApRecord = new WifiProEnterpriseApRecord(apInfo.apSSID, apInfo.apSecurityType);
            HwHiLog.i(TAG, false, "add new Enterprise Ap Record succ.", new Object[0]);
        } else {
            HwHiLog.i(TAG, false, "delete Enterprise Record from AP info DB failed.", new Object[0]);
        }
    }

    private void updateConnectTimeInOneDay(WifiProApInfoRecord apInfo, int day, int startTime, int endTime) {
        int restTime = 0;
        if (apInfo != null) {
            apInfo.totalUseTime += endTime - startTime;
            if (day == 1 || day == 7) {
                int restTime2 = endTime - startTime;
                apInfo.totalUseTimeAtWeekend += restTime2;
                HwHiLog.d(TAG, false, "add weekend home time %{public}d s", new Object[]{Integer.valueOf(restTime2)});
                return;
            }
            if (startTime < REST_TIME_END_PAST_SECONDS) {
                restTime = endTime < REST_TIME_END_PAST_SECONDS ? 0 + (endTime - startTime) : endTime < REST_TIME_BEGIN_PAST_SECONDS ? 0 + (REST_TIME_END_PAST_SECONDS - startTime) : 0 + (REST_TIME_END_PAST_SECONDS - startTime) + (endTime - REST_TIME_BEGIN_PAST_SECONDS);
            } else if (startTime < REST_TIME_BEGIN_PAST_SECONDS) {
                if (endTime >= REST_TIME_BEGIN_PAST_SECONDS) {
                    restTime = 0 + (endTime - REST_TIME_BEGIN_PAST_SECONDS);
                }
            } else if (startTime >= REST_TIME_BEGIN_PAST_SECONDS) {
                restTime = 0 + (endTime - startTime);
            }
            apInfo.totalUseTimeAtNight += restTime;
            HwHiLog.d(TAG, false, "add night home time %{public}d s", new Object[]{Integer.valueOf(restTime)});
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
        if (apInfo != null) {
            int lastRecordDay = apInfo.lastRecordDay;
            long lastRecordRealTime = apInfo.lastRecordRealTime;
            int startSecondsOfDay = (apInfo.lastRecordHour * SECOND_OF_ONE_HOUR) + (apInfo.lastRecordMin * SECOND_OF_ONE_MINUTE) + apInfo.lastRecordSec;
            updateRecordTime(apInfo);
            int endSecondsOfDay = (this.mCurrentHour * SECOND_OF_ONE_HOUR) + (this.mCurrentMinute * SECOND_OF_ONE_MINUTE) + this.mCurrentSecond;
            long durationCalendar = (endDate - startDate) / 1000;
            long realDuration = (this.mCurrentElapsedTime - lastRecordRealTime) / 1000;
            long absVal = Math.abs(durationCalendar - realDuration);
            HwHiLog.d(TAG, false, "durationCalendar=%{public}s, realDuration=%{public}s", new Object[]{String.valueOf(durationCalendar), String.valueOf(realDuration)});
            if (durationCalendar < 0 || absVal > 10) {
                HwHiLog.d(TAG, false, "updateConnectTime invalid date recod, return", new Object[0]);
            } else if (durationCalendar > ONE_DAY_S_VALUE) {
                HwHiLog.d(TAG, false, "update connection time more than 1 day, ignore", new Object[0]);
            } else {
                int i = this.mCurrentDay;
                if (i != lastRecordDay) {
                    updateConnectTimeInOneDay(apInfo, lastRecordDay, startSecondsOfDay, END_SECONDS_OF_DAY);
                    updateConnectTimeInOneDay(apInfo, this.mCurrentDay, 0, endSecondsOfDay);
                } else {
                    updateConnectTimeInOneDay(apInfo, i, startSecondsOfDay, endSecondsOfDay);
                }
                HwHiLog.d(TAG, false, "calcTotalConnectTime record total=%{public}d s, record night=%{public}d s, record weekend=%{public}d", new Object[]{Integer.valueOf(apInfo.totalUseTime), Integer.valueOf(apInfo.totalUseTimeAtNight), Integer.valueOf(apInfo.totalUseTimeAtWeekend)});
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

    private boolean checkIsHomeAP(WifiProApInfoRecord apInfo) {
        if (apInfo == null || this.mDBMgr == null) {
            return false;
        }
        if (apInfo.isEnterpriseAP) {
            HwHiLog.i(TAG, false, "checkIsHomeAP: do not check Enterprise AP to DB.", new Object[0]);
            return false;
        }
        long totPassTime = (apInfo.lastConnectTime - apInfo.firstConnectTime) / 1000;
        if (totPassTime > 0) {
            long dayAvgRestTime = 0;
            long passDays = totPassTime / ONE_DAY_S_VALUE;
            if (passDays != 0) {
                dayAvgRestTime = ((long) (apInfo.totalUseTimeAtNight + apInfo.totalUseTimeAtWeekend)) / passDays;
            }
            HwHiLog.i(TAG, false, "checkIsHomeAP , pass time (s) =%{public}s, pass day=%{public}s, tot rest time:%{public}s", new Object[]{String.valueOf(totPassTime), String.valueOf(passDays), String.valueOf(apInfo.totalUseTimeAtNight + apInfo.totalUseTimeAtWeekend)});
            if (dayAvgRestTime < 1800) {
                HwHiLog.i(TAG, false, "checkIsHomeAP dayAvgRestTime not enough return default rate", new Object[0]);
                return false;
            }
        }
        int totalConnectHours = apInfo.totalUseTime / SECOND_OF_ONE_HOUR;
        float restTimeRate = 0.0f;
        if (apInfo.totalUseTime != 0) {
            restTimeRate = ((float) (apInfo.totalUseTimeAtNight + apInfo.totalUseTimeAtWeekend)) / ((float) apInfo.totalUseTime);
        }
        HwHiLog.i(TAG, false, "checkIsHomeAP totalConnectHours:%{public}d, restTimeRate=%{public}s", new Object[]{Integer.valueOf(totalConnectHours), String.valueOf(restTimeRate)});
        if (totalConnectHours <= 10 || restTimeRate <= 0.5f) {
            return false;
        }
        HwHiLog.i(TAG, false, "checkIsHomeAP SSID:%{public}s is home AP", new Object[]{StringUtilEx.safeDisplaySsid(apInfo.apSSID)});
        return true;
    }

    private void homeAPJudgeProcess(WifiProApInfoRecord apInfo) {
        if (checkIsHomeAP(apInfo)) {
            if (apInfo.judgeHomeAPTime == 0) {
                apInfo.judgeHomeAPTime = apInfo.lastConnectTime;
                this.mDBMgr.addOrUpdateApInfoRecord(apInfo);
                HwHiLog.d(TAG, false, "Home ap judge time(hour):%{public}s", new Object[]{String.valueOf((apInfo.judgeHomeAPTime - apInfo.firstConnectTime) / 3600000)});
                return;
            }
            HwHiLog.d(TAG, false, "already record as home ap.", new Object[0]);
        } else if (apInfo.judgeHomeAPTime > 0) {
            HwHiLog.d(TAG, false, "Home ap last time(hour):%{public}s", new Object[]{String.valueOf((apInfo.lastConnectTime - apInfo.judgeHomeAPTime) / 3600000)});
            apInfo.judgeHomeAPTime = 0;
            this.mDBMgr.addOrUpdateApInfoRecord(apInfo);
        }
    }

    public boolean getIsHomeAP(String bssid) {
        synchronized (this.mApInfoCache) {
            boolean z = false;
            if (this.mCurrentApInfo != null) {
                if (bssid != null) {
                    if (!bssid.equals(this.mCurrentApInfo.apBSSID)) {
                        HwHiLog.d(TAG, false, "getIsHomeAP false for different BSSID AP.", new Object[0]);
                        return false;
                    }
                    if (this.mCurrentApInfo.judgeHomeAPTime > 0) {
                        z = true;
                    }
                    return z;
                }
            }
            return false;
        }
    }

    private boolean checkParameterValid(String bssid) {
        WifiProApInfoRecord wifiProApInfoRecord = this.mCurrentApInfo;
        if (wifiProApInfoRecord == null || bssid == null) {
            return false;
        }
        if (wifiProApInfoRecord.isEnterpriseAP || this.mCurrentApInfo.judgeHomeAPTime == 0) {
            HwHiLog.d(TAG, false, "HomeApSwitchRate return default rate for not Home AP.", new Object[0]);
            return false;
        } else if (bssid.equals(this.mCurrentApInfo.apBSSID)) {
            return true;
        } else {
            HwHiLog.d(TAG, false, "HomeApSwitchRate return default rate for different BSSID AP.", new Object[0]);
            return false;
        }
    }

    public float getHomeApSwitchRate(String bssid) {
        synchronized (this.mApInfoCache) {
            if (!checkParameterValid(bssid)) {
                return 1.0f;
            }
            int totalConnectHours = this.mCurrentApInfo.totalUseTime / SECOND_OF_ONE_HOUR;
            float restTimeRate = 0.0f;
            if (this.mCurrentApInfo.totalUseTime != 0) {
                restTimeRate = ((float) (this.mCurrentApInfo.totalUseTimeAtNight + this.mCurrentApInfo.totalUseTimeAtWeekend)) / ((float) this.mCurrentApInfo.totalUseTime);
            }
            long totPassTime = (this.mCurrentApInfo.lastConnectTime - this.mCurrentApInfo.firstConnectTime) / 1000;
            if (totPassTime > 0) {
                long dayAvgRestTime = 0;
                long passDays = totPassTime / ONE_DAY_S_VALUE;
                if (passDays != 0) {
                    dayAvgRestTime = ((long) (this.mCurrentApInfo.totalUseTimeAtNight + this.mCurrentApInfo.totalUseTimeAtWeekend)) / passDays;
                }
                HwHiLog.i(TAG, false, "HomeApSwitchRate, pass time (s) =%{public}s, pass day=%{public}s, tot rest time:%{public}s", new Object[]{String.valueOf(totPassTime), String.valueOf(passDays), String.valueOf(this.mCurrentApInfo.totalUseTimeAtNight + this.mCurrentApInfo.totalUseTimeAtWeekend)});
                if (dayAvgRestTime < 1800) {
                    HwHiLog.i(TAG, false, "HomeApSwitchRate dayAvgRestTime not enough return default rate", new Object[0]);
                    return 1.0f;
                }
            }
            HwHiLog.i(TAG, false, "HomeApSwitchRate totalConnectHours:%{public}d, restTimeRate=%{public}s", new Object[]{Integer.valueOf(totalConnectHours), String.valueOf(restTimeRate)});
            for (int index = HOME_AP_SWITCH_RATE_TABLE.length - 1; index >= 0; index--) {
                if (totalConnectHours > HOME_AP_SWITCH_RATE_TABLE[index].TOTAL_CONNECT_TIME && restTimeRate > HOME_AP_SWITCH_RATE_TABLE[index].REST_TIME_RATE) {
                    float rate = HOME_AP_SWITCH_RATE_TABLE[index].SWITCH_RATE;
                    HwHiLog.i(TAG, false, "HomeApSwitchRate, rate=%{public}s", new Object[]{String.valueOf(rate)});
                    return rate;
                }
            }
            return 1.0f;
        }
    }

    @Override // com.huawei.hwwifiproservice.IGetApRecordCount
    public synchronized boolean statisticApInfoRecord() {
        if (this.mDBMgr == null) {
            return false;
        }
        return this.mDBMgr.statisticApInfoRecord();
    }

    @Override // com.huawei.hwwifiproservice.IGetApRecordCount
    public int getTotRecordCount() {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager == null) {
            return 0;
        }
        return wifiProHistoryDBManager.getTotRecordCount();
    }

    @Override // com.huawei.hwwifiproservice.IGetApRecordCount
    public int getHomeApRecordCount() {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager == null) {
            return 0;
        }
        return wifiProHistoryDBManager.getHomeApRecordCount();
    }

    /* access modifiers changed from: package-private */
    public class ApInfoProcessHandler extends Handler {
        private ApInfoProcessHandler(Looper looper) {
            super(looper);
            HwHiLog.d(WifiProHistoryRecordManager.TAG, false, "new ApInfoProcessHandler", new Object[0]);
        }

        private void handleUpdateApInfoCommand(Message msg) {
            int networkSecurityType;
            if (msg == null || msg.obj == null) {
                HwHiLog.w(WifiProHistoryRecordManager.TAG, false, "handleUpdateApIndoCommand parameter error.", new Object[0]);
                return;
            }
            HwHiLog.d(WifiProHistoryRecordManager.TAG, false, "UPDATE_AP_INFO_CMD rcv.", new Object[0]);
            if (msg.obj instanceof SetApInfoPara) {
                SetApInfoPara setPara = (SetApInfoPara) msg.obj;
                if (setPara.targetBssid != null) {
                    networkSecurityType = WifiProHistoryRecordManager.this.getWifiAuthType(setPara.networkID);
                } else {
                    networkSecurityType = -1;
                }
                synchronized (WifiProHistoryRecordManager.this.mApInfoCache) {
                    WifiProHistoryRecordManager.this.updateCurrentApInfo(setPara.targetBssid, setPara.setSsid, networkSecurityType);
                }
                if (!WifiProHistoryRecordManager.this.mHadTryDeleteTooOldRecord) {
                    HwHiLog.d(WifiProHistoryRecordManager.TAG, false, "TRY_DELETE_TOO_OLD_AP_INFO event send here.", new Object[0]);
                    WifiProHistoryRecordManager.this.mApInfoProcessHandler.sendEmptyMessageDelayed(102, 60000);
                    WifiProHistoryRecordManager.this.mHadTryDeleteTooOldRecord = true;
                    return;
                }
                return;
            }
            HwHiLog.w(WifiProHistoryRecordManager.TAG, false, "handleUpdateApIndoCommand:Class is not match", new Object[0]);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    handleUpdateApInfoCommand(msg);
                    return;
                case 101:
                    HwHiLog.d(WifiProHistoryRecordManager.TAG, false, "MSG_UPDATE_AP_CONNECT_TIME_CMD precess start.", new Object[0]);
                    synchronized (WifiProHistoryRecordManager.this.mApInfoCache) {
                        WifiProHistoryRecordManager.this.periodSaveRecord(WifiProHistoryRecordManager.this.mCurrentApInfo);
                    }
                    WifiProHistoryRecordManager.this.mApInfoProcessHandler.sendEmptyMessageDelayed(101, 1800000);
                    return;
                case 102:
                    if (WifiProHistoryRecordManager.this.mDBMgr != null) {
                        HwHiLog.d(WifiProHistoryRecordManager.TAG, false, "restart phone and connect first WIFI trigger too old record delete.", new Object[0]);
                        synchronized (WifiProHistoryRecordManager.this.mApInfoCache) {
                            WifiProHistoryRecordManager.this.mDBMgr.removeTooOldApInfoRecord();
                        }
                        return;
                    }
                    return;
                case 103:
                    HwHiLog.d(WifiProHistoryRecordManager.TAG, false, "MSG_WIFIPRO_CLOSE precess start.", new Object[0]);
                    synchronized (WifiProHistoryRecordManager.this.mApInfoCache) {
                        WifiProHistoryRecordManager.this.afterDisconnectProcess(WifiProHistoryRecordManager.this.mCurrentApInfo);
                        WifiProHistoryRecordManager.this.mCurrentApInfo = null;
                    }
                    return;
                default:
                    HwHiLog.e(WifiProHistoryRecordManager.TAG, false, "ApInfoProcessHandler got unknow message.", new Object[0]);
                    return;
            }
        }
    }

    public void addHistoryHSCount(int incCount) {
        HwHiLog.d(TAG, false, "addHistoryHSCount inc val:%{public}d", new Object[]{Integer.valueOf(incCount)});
        synchronized (this.mApInfoCache) {
            if (this.mCurrentApInfo != null) {
                this.mCurrentApInfo.highSpdFreq += incCount;
                HwHiLog.d(TAG, false, "addHistoryHSCount total:%{public}d", new Object[]{Integer.valueOf(this.mCurrentApInfo.highSpdFreq)});
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
            HwHiLog.d(TAG, false, " get mCurrentApInfo inCache for new bssid. catch size:%{public}d", new Object[]{Integer.valueOf(this.mApInfoCache.size())});
            return new WifiProApInfoRecord(retApInfo);
        }
    }

    public boolean loadApQualityRecord(String apBssid, WifiProApQualityRcd apQR) {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager == null || apQR == null || apBssid == null) {
            return false;
        }
        if (!wifiProHistoryDBManager.queryApQualityRcd(apBssid, apQR)) {
            HwHiLog.i(TAG, false, "loadApQualityRecord failed.", new Object[0]);
            return false;
        }
        HwHiLog.i(TAG, false, "loadApQualityRecord succ.", new Object[0]);
        return true;
    }

    public void saveApQualityRecord(WifiProApQualityRcd apQR) {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager != null && apQR != null) {
            if (!wifiProHistoryDBManager.addOrUpdateApQualityRcd(apQR)) {
                HwHiLog.i(TAG, false, "saveApQualityRecord failed.", new Object[0]);
            } else {
                HwHiLog.i(TAG, false, "saveApQualityRecord succ.", new Object[0]);
            }
        }
    }

    public WifiProDualBandApInfoRcd getDualBandApRecord(String apBssid) {
        synchronized (this.mLock) {
            if (apBssid == null) {
                return null;
            }
            WifiProDualBandApInfoRcd record = new WifiProDualBandApInfoRcd(apBssid);
            if (this.mDBMgr.queryDualBandApInfoRcd(apBssid, record)) {
                return record;
            }
            return null;
        }
    }

    public List<WifiProDualBandApInfoRcd> getDualBandApInfoBySsid(String ssid) {
        List<WifiProDualBandApInfoRcd> queryDualBandApInfoRcdBySsid;
        synchronized (this.mLock) {
            if (ssid == null) {
                Collections.emptyList();
            }
            queryDualBandApInfoRcdBySsid = this.mDBMgr.queryDualBandApInfoRcdBySsid(ssid);
        }
        return queryDualBandApInfoRcdBySsid;
    }

    public boolean saveDualBandApInfo(WifiProDualBandApInfoRcd apInfo) {
        if (this.mDBMgr == null || apInfo == null) {
            return false;
        }
        HwHiLog.d(TAG, false, "saveDualBandApInfo apInfo.mApSSID = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(apInfo.mApSSID)});
        return this.mDBMgr.addOrUpdateDualBandApInfoRcd(apInfo);
    }

    public boolean deleteDualBandApInfo(String bssid) {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager == null || bssid == null) {
            return false;
        }
        return wifiProHistoryDBManager.deleteDualBandApInfoRcd(bssid);
    }

    public boolean saveRelateApInfo(WifiProRelateApRcd relateApInfo) {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager == null || relateApInfo == null) {
            return false;
        }
        return wifiProHistoryDBManager.addOrUpdateRelateApRcd(relateApInfo);
    }

    public boolean deleteRelateApInfo(String bssid) {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager == null || bssid == null) {
            return false;
        }
        return wifiProHistoryDBManager.deleteRelateApRcd(bssid);
    }

    public boolean deleteRelate5GApInfo(String bssid) {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager == null || bssid == null) {
            return false;
        }
        return wifiProHistoryDBManager.deleteRelate5GAPRcd(bssid);
    }

    public boolean getRelateApList(String apBssid, List<WifiProRelateApRcd> relateApList) {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager == null || apBssid == null || relateApList == null) {
            return false;
        }
        return wifiProHistoryDBManager.queryRelateApRcd(apBssid, relateApList);
    }

    public boolean updateRSSIThreshold(String apBssid, int[] rssiEntries) {
        if (this.mDBMgr == null || apBssid == null) {
            return false;
        }
        return this.mDBMgr.addOrUpdateApRssiThreshold(apBssid, convertArrayToString(rssiEntries));
    }

    public int[] getRSSIThreshold(String apBssid) {
        WifiProHistoryDBManager wifiProHistoryDBManager = this.mDBMgr;
        if (wifiProHistoryDBManager == null || apBssid == null) {
            return new int[0];
        }
        String temp = wifiProHistoryDBManager.queryApRssiThreshold(apBssid);
        if (temp == null) {
            return new int[0];
        }
        return convertStringToArray(temp);
    }

    private String convertArrayToString(int[] list) {
        int lenght = list.length;
        if (lenght == 0) {
            return "";
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
        for (int i = 0; i < length; i++) {
            try {
                result[i] = Integer.parseInt(temp[i]);
            } catch (NumberFormatException e) {
                HwHiLog.e(TAG, false, "Exception happened in convertStringToArray()", new Object[0]);
                return new int[0];
            }
        }
        return result;
    }

    public void closeWifiProHistoryDB() {
        this.mDBMgr.closeDB();
    }

    public boolean isHaveMultipleAP(String bssid, String ssid, int type) {
        int sameCount = this.mDBMgr.querySameSsidApCount(bssid, ssid, type);
        HwHiLog.i(TAG, false, "isHaveMultipleAP sameCount = %{public}d", new Object[]{Integer.valueOf(sameCount)});
        return sameCount >= 1;
    }
}
