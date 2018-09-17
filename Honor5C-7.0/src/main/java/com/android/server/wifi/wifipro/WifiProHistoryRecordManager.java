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
import com.android.server.wifi.HwWifiCHRConstImpl;
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
    private static final HomeApSwitchRate[] HOME_AP_SWITCH_RATE_TABLE = null;
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
    private static int printLogLevel;
    private LruCache<String, WifiProApInfoRecord> mApInfoCache;
    private ApInfoProcessHandler mApInfoProcessHandler;
    private Context mContext;
    private WifiProApInfoRecord mCurrentApInfo;
    private int mCurrentDay;
    private long mCurrentElapsedTime;
    private int mCurrentHour;
    private int mCurrentMinute;
    private int mCurrentSecond;
    private WifiProHistoryDBManager mDBMgr;
    private boolean mHadTryDeleteTooOldRecord;
    private Object mLock;
    private WifiProCHRManager mWifiCHRStateManager;
    private WifiManager mWifiManager;
    private WifiProEnterpriseApRecord mWifiProEnterpriseApRecord;
    private WifiProStatisticsManager mWifiProStatisticsManager;

    class ApInfoProcessHandler extends Handler {
        private ApInfoProcessHandler(Looper looper) {
            super(looper);
            WifiProHistoryRecordManager.this.logd("new ApInfoProcessHandler");
        }

        public void handleMessage(Message msg) {
            LruCache -get0;
            switch (msg.what) {
                case WifiProHistoryRecordManager.MSG_UPDATE_AP_INFO_CMD /*100*/:
                    WifiProHistoryRecordManager.this.logd("UPDATE_AP_INFO_CMD rcv.");
                    SetApInfoPara setPara = msg.obj;
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
                        break;
                    }
                    if (!WifiProHistoryRecordManager.this.mHadTryDeleteTooOldRecord) {
                        WifiProHistoryRecordManager.this.logd("TRY_DELETE_TOO_OLD_AP_INFO event send here.");
                        WifiProHistoryRecordManager.this.mApInfoProcessHandler.sendEmptyMessageDelayed(WifiProHistoryRecordManager.MSG_TRY_DELETE_TOO_OLD_AP_INFO_RECORD, 60000);
                        WifiProHistoryRecordManager.this.mHadTryDeleteTooOldRecord = true;
                        return;
                    }
                    return;
                case WifiProHistoryRecordManager.MSG_UPDATE_AP_CONNECT_TIME_CMD /*101*/:
                    WifiProHistoryRecordManager.this.logd("MSG_UPDATE_AP_CONNECT_TIME_CMD precess start.");
                    synchronized (WifiProHistoryRecordManager.this.mApInfoCache) {
                        WifiProHistoryRecordManager.this.periodSaveRecord(WifiProHistoryRecordManager.this.mCurrentApInfo);
                        break;
                    }
                    WifiProHistoryRecordManager.this.mApInfoProcessHandler.sendEmptyMessageDelayed(WifiProHistoryRecordManager.MSG_UPDATE_AP_CONNECT_TIME_CMD, 1800000);
                    return;
                case WifiProHistoryRecordManager.MSG_TRY_DELETE_TOO_OLD_AP_INFO_RECORD /*102*/:
                    if (WifiProHistoryRecordManager.this.mDBMgr != null) {
                        WifiProHistoryRecordManager.this.logd("restart phone and connect first WIFI trigger too old record delete.");
                        -get0 = WifiProHistoryRecordManager.this.mApInfoCache;
                        synchronized (-get0) {
                            break;
                        }
                        WifiProHistoryRecordManager.this.mDBMgr.removeTooOldApInfoRecord();
                        break;
                    }
                    return;
                case WifiProHistoryRecordManager.MSG_WIFIPRO_CLOSE /*103*/:
                    WifiProHistoryRecordManager.this.logd("MSG_WIFIPRO_CLOSE precess start.");
                    -get0 = WifiProHistoryRecordManager.this.mApInfoCache;
                    synchronized (-get0) {
                        break;
                    }
                    WifiProHistoryRecordManager.this.afterDisconnectProcess(WifiProHistoryRecordManager.this.mCurrentApInfo);
                    WifiProHistoryRecordManager.this.mCurrentApInfo = null;
                    break;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.WifiProHistoryRecordManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.WifiProHistoryRecordManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.WifiProHistoryRecordManager.<clinit>():void");
    }

    public WifiProHistoryRecordManager(Context context, WifiManager wifiManager) {
        this.mCurrentDay = START_SECOND_OF_DAY;
        this.mCurrentHour = START_SECOND_OF_DAY;
        this.mCurrentMinute = START_SECOND_OF_DAY;
        this.mCurrentSecond = START_SECOND_OF_DAY;
        this.mCurrentElapsedTime = 0;
        this.mApInfoCache = new LruCache(INVALID_TIME_DELTA);
        this.mWifiProEnterpriseApRecord = null;
        this.mHadTryDeleteTooOldRecord = false;
        this.mLock = new Object();
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
        this.mApInfoProcessHandler = new ApInfoProcessHandler(thread.getLooper(), null);
    }

    private boolean isApInfoRcdValid(WifiProApInfoRecord apInfoRcd) {
        if (apInfoRcd == null) {
            loge("isApInfoRcdValid null .");
            return false;
        } else if (apInfoRcd.apBSSID == null || apInfoRcd.lastConnectTime <= 0 || apInfoRcd.firstConnectTime <= 0 || apInfoRcd.lastConnectTime < apInfoRcd.firstConnectTime) {
            return false;
        } else {
            logd("History record valid for bssid:" + apInfoRcd.apBSSID);
            return true;
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
            logd("initFirstConnectApInfo enter for bssid:" + apInfo.apBSSID + " , currDateMs" + currDateMs);
        }
    }

    private boolean isValid(WifiConfiguration config) {
        boolean z = true;
        if (config == null) {
            return false;
        }
        int cc = config.allowedKeyManagement.cardinality();
        logd("config isValid cardinality=" + cc);
        if (cc > DBG_LOG_LEVEL) {
            z = false;
        }
        return z;
    }

    private int getWifiAuthType(int networkID) {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
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
        sendMsgWithObj(MSG_UPDATE_AP_INFO_CMD, START_SECOND_OF_DAY, START_SECOND_OF_DAY, new SetApInfoPara(apBssid, apSsid, networkID));
    }

    public void wifiproClose() {
        logi("mCurrentApInfo bssid not changed.");
        sendEmptyMsg(MSG_WIFIPRO_CLOSE);
    }

    private void updateCurrentApInfo(String bssid, String ssid, int securityType) {
        if (bssid == null) {
            if (this.mCurrentApInfo != null) {
                logi("******** updateCurrentApInfo, AP:" + this.mCurrentApInfo.apBSSID + " was disconnected. set CurrentApInfo=null");
                afterDisconnectProcess(this.mCurrentApInfo);
                this.mCurrentApInfo = null;
            }
        } else if (this.mCurrentApInfo == null || !bssid.equals(this.mCurrentApInfo.apBSSID)) {
            logi("######## updateCurrentApInfo new BSSID:" + bssid + ", ssid" + ssid);
            if (this.mCurrentApInfo != null) {
                afterDisconnectProcess(this.mCurrentApInfo);
            }
            this.mCurrentApInfo = (WifiProApInfoRecord) this.mApInfoCache.get(bssid);
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
        if (this.mDBMgr.queryApInfoRecord(apBssid, apInfo)) {
            logi("queryApInfoRecord bssid:" + apBssid + " succ.");
            return true;
        }
        logi("queryApInfoRecord bssid:" + apBssid + " failed.");
        return false;
    }

    private void afterConnectProcess(WifiProApInfoRecord apInfo) {
        if (apInfo != null && this.mApInfoProcessHandler != null) {
            logd("afterConnectProcess enter for ssid:" + apInfo.apSSID + ", bssid:" + apInfo.apBSSID);
            if (!apInfo.isEnterpriseAP) {
                apInfo.connectStartTimeSave = getCurrentTimeMs();
                apInfo.lastConnectTime = getCurrentTimeMs();
                updateRecordTime(apInfo);
                if (this.mApInfoProcessHandler.hasMessages(MSG_UPDATE_AP_CONNECT_TIME_CMD)) {
                    this.mApInfoProcessHandler.removeMessages(MSG_UPDATE_AP_CONNECT_TIME_CMD);
                }
                logd("UPDATE_AP_CONNECT_TIME_CMD is send.");
                sendEmptyMsgDelayed(MSG_UPDATE_AP_CONNECT_TIME_CMD, 1800000);
                homeAPJudgeProcess(apInfo);
            }
        }
    }

    private void periodSaveRecord(WifiProApInfoRecord apInfo) {
        if (apInfo == null || this.mDBMgr == null) {
            logd("periodSaveRecord null error.");
            return;
        }
        if (!apInfo.isEnterpriseAP) {
            logd("periodSaveRecord enter for normal ssid:" + apInfo.apSSID + ", bssid:" + apInfo.apBSSID);
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

    private void afterDisconnectProcess(WifiProApInfoRecord apInfo) {
        if (apInfo != null && this.mDBMgr != null) {
            if (apInfo.isEnterpriseAP) {
                logd("afterDisconnectProcess enter ssid:" + apInfo.apSSID + ", bssid:" + apInfo.apBSSID + ", is enterprise AP.");
            } else {
                logd("afterDisconnectProcess enter for normal ssid:" + apInfo.apSSID + ", bssid:" + apInfo.apBSSID);
                apInfo.lastConnectTime = getCurrentTimeMs();
                updateConnectTime(apInfo, apInfo.connectStartTimeSave, apInfo.lastConnectTime);
                saveApInfoRecord(apInfo);
                if (this.mApInfoProcessHandler.hasMessages(MSG_UPDATE_AP_CONNECT_TIME_CMD)) {
                    this.mApInfoProcessHandler.removeMessages(MSG_UPDATE_AP_CONNECT_TIME_CMD);
                }
            }
            if (this.mWifiProStatisticsManager != null) {
                this.mWifiProStatisticsManager.updateStatisticToDB();
            }
        }
    }

    public boolean isEnterpriseAP(String ssid, int secType) {
        if (ssid == null) {
            return false;
        }
        boolean retVal = false;
        if (this.mWifiProEnterpriseApRecord != null && this.mWifiProEnterpriseApRecord.apSSID.equals(ssid) && this.mWifiProEnterpriseApRecord.apSecurityType == secType) {
            retVal = true;
        }
        if (retVal) {
            logi("isEnterpriseAP match from catch.");
            retVal = true;
        } else {
            retVal = this.mDBMgr.queryEnterpriseApRecord(ssid, secType);
            if (retVal) {
                logi("isEnterpriseAP match from DB.");
                this.mWifiProEnterpriseApRecord = new WifiProEnterpriseApRecord(ssid, secType);
            }
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
            if (sameCount < JUDGE_ENTERPRISE_AP_MIN_COUNT) {
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
                this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "ENTERPRISE_AP_INFO");
            } else {
                logi("delete Enterprise Record from AP info DB failed.");
            }
        }
    }

    private void updateConnectTimeInOneDay(WifiProApInfoRecord apInfo, int day, int startTime, int endTime) {
        int restTime = START_SECOND_OF_DAY;
        if (apInfo != null) {
            apInfo.totalUseTime += endTime - startTime;
            if (day == DBG_LOG_LEVEL || day == REST_TIME_END_HOUR) {
                restTime = endTime - startTime;
                apInfo.totalUseTimeAtWeekend += restTime;
                logd("add weekend home time " + restTime + "s");
                return;
            }
            if (startTime < REST_TIME_END_PAST_SECONDS) {
                restTime = endTime < REST_TIME_END_PAST_SECONDS ? (endTime - startTime) + START_SECOND_OF_DAY : endTime < REST_TIME_BEGIN_PAST_SECONDS ? (25200 - startTime) + START_SECOND_OF_DAY : ((25200 - startTime) + START_SECOND_OF_DAY) + (endTime - REST_TIME_BEGIN_PAST_SECONDS);
            } else if (startTime < REST_TIME_BEGIN_PAST_SECONDS) {
                if (endTime >= REST_TIME_BEGIN_PAST_SECONDS && endTime >= REST_TIME_BEGIN_PAST_SECONDS) {
                    restTime = (endTime - REST_TIME_BEGIN_PAST_SECONDS) + START_SECOND_OF_DAY;
                }
            } else if (startTime >= REST_TIME_BEGIN_PAST_SECONDS) {
                restTime = (endTime - startTime) + START_SECOND_OF_DAY;
            }
            apInfo.totalUseTimeAtNight += restTime;
            logd("add night home time " + restTime + "s");
        }
    }

    void updateRecordTime(WifiProApInfoRecord apInfo) {
        if (apInfo != null) {
            Calendar cal = Calendar.getInstance();
            this.mCurrentDay = cal.get(REST_TIME_END_HOUR);
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
            int startSecondsOfDay = ((apInfo.lastRecordHour * SECOND_OF_ONE_HOUR) + (apInfo.lastRecordMin * SECOND_OF_ONE_MINUTE)) + apInfo.lastRecordSec;
            updateRecordTime(apInfo);
            int endSecondsOfDay = ((this.mCurrentHour * SECOND_OF_ONE_HOUR) + (this.mCurrentMinute * SECOND_OF_ONE_MINUTE)) + this.mCurrentSecond;
            long durationCalendar = (endDate - startDate) / 1000;
            long realDuration = (this.mCurrentElapsedTime - lastRecordRealTime) / 1000;
            long absVal = Math.abs(durationCalendar - realDuration);
            logd("durationCalendar=" + durationCalendar + ", realDuration=" + realDuration);
            if (durationCalendar < 0 || absVal > 10) {
                logd("updateConnectTime invalid date recod, return");
            } else if (durationCalendar > ONE_DAY_S_VALUE) {
                logd("update connection time more than 1 day, ignore");
            } else {
                if (this.mCurrentDay != lastRecordDay) {
                    updateConnectTimeInOneDay(apInfo, lastRecordDay, startSecondsOfDay, END_SECONDS_OF_DAY);
                    updateConnectTimeInOneDay(apInfo, this.mCurrentDay, START_SECOND_OF_DAY, endSecondsOfDay);
                } else {
                    updateConnectTimeInOneDay(apInfo, this.mCurrentDay, startSecondsOfDay, endSecondsOfDay);
                }
                logd("calcTotalConnectTime record total=" + apInfo.totalUseTime + "s, record night=" + apInfo.totalUseTimeAtNight + "s, record weekend=" + apInfo.totalUseTimeAtWeekend);
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
            logi("checkIsHomeAP bssid:" + apInfo.apSSID + ", pass time (s) =" + totPassTime + ", pass day=" + pass_days + ", tot rest time:" + (apInfo.totalUseTimeAtNight + apInfo.totalUseTimeAtWeekend));
            if (dayAvgRestTime < 1800) {
                logi("checkIsHomeAP dayAvgRestTime not enough return default rate for bssid:" + apInfo.apSSID);
                return false;
            }
        }
        int totalConnectHours = apInfo.totalUseTime / SECOND_OF_ONE_HOUR;
        float restTimeRate = 0.0f;
        if (apInfo.totalUseTime != 0) {
            restTimeRate = ((float) (apInfo.totalUseTimeAtNight + apInfo.totalUseTimeAtWeekend)) / ((float) apInfo.totalUseTime);
        }
        logi("checkIsHomeAP totalConnectHours:" + totalConnectHours + ", restTimeRate=" + restTimeRate);
        if (totalConnectHours <= INVALID_TIME_DELTA || restTimeRate <= HOME_AP_MIN_TIME_RATE) {
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
                    this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "HOME_AP_INFO");
                } else {
                    logd("already record as home ap.");
                }
            } else if (apInfo.judgeHomeAPTime > 0) {
                this.mWifiCHRStateManager.updateSSID(apInfo.apSSID);
                this.mWifiCHRStateManager.updateAPSecurityType(MSG_UPDATE_AP_INFO_CMD);
                long homeApTime = (apInfo.lastConnectTime - apInfo.judgeHomeAPTime) / 3600000;
                logd("Home ap last time(hour):" + homeApTime);
                this.mWifiCHRStateManager.updateHomeAPJudgeTime((int) homeApTime);
                this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "HOME_AP_INFO");
                apInfo.judgeHomeAPTime = 0;
                this.mDBMgr.addOrUpdateApInfoRecord(apInfo);
            }
        }
    }

    public boolean getIsHomeAP(String bssid) {
        boolean z = false;
        synchronized (this.mApInfoCache) {
            if (this.mCurrentApInfo == null || bssid == null) {
                return false;
            } else if (bssid.equals(this.mCurrentApInfo.apBSSID)) {
                if (this.mCurrentApInfo.judgeHomeAPTime > 0) {
                    z = true;
                }
                return z;
            } else {
                logd("getIsHomeAP false for different BSSID AP.");
                return false;
            }
        }
    }

    public float getHomeApSwitchRate(String bssid) {
        synchronized (this.mApInfoCache) {
            if (this.mCurrentApInfo == null || bssid == null) {
                return HOME_AP_DEFAULT_SWITCH_RATE;
            } else if (this.mCurrentApInfo.isEnterpriseAP || this.mCurrentApInfo.judgeHomeAPTime == 0) {
                logd("HomeApSwitchRate return default rate for not Home AP.");
                return HOME_AP_DEFAULT_SWITCH_RATE;
            } else {
                if (bssid.equals(this.mCurrentApInfo.apBSSID)) {
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
                        logi("HomeApSwitchRate bssid:" + bssid + ", pass time (s) =" + totPassTime + ", pass day=" + pass_days + ", tot rest time:" + (this.mCurrentApInfo.totalUseTimeAtNight + this.mCurrentApInfo.totalUseTimeAtWeekend));
                        if (dayAvgRestTime < 1800) {
                            logi("HomeApSwitchRate dayAvgRestTime not enough return default rate for bssid:" + bssid);
                            return HOME_AP_DEFAULT_SWITCH_RATE;
                        }
                    }
                    logi("HomeApSwitchRate totalConnectHours:" + totalConnectHours + ", restTimeRate=" + restTimeRate);
                    while (index >= 0) {
                        if (totalConnectHours <= HOME_AP_SWITCH_RATE_TABLE[index].TOTAL_CONNECT_TIME || restTimeRate <= HOME_AP_SWITCH_RATE_TABLE[index].REST_TIME_RATE) {
                            index--;
                        } else {
                            float rate = HOME_AP_SWITCH_RATE_TABLE[index].SWITCH_RATE;
                            logi("HomeApSwitchRate bssid:" + bssid + ", rate=" + rate);
                            return rate;
                        }
                    }
                    return HOME_AP_DEFAULT_SWITCH_RATE;
                }
                logd("HomeApSwitchRate return default rate for different BSSID AP.");
                return HOME_AP_DEFAULT_SWITCH_RATE;
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
            return START_SECOND_OF_DAY;
        }
        return this.mDBMgr.getTotRecordCount();
    }

    public int getHomeApRecordCount() {
        if (this.mDBMgr == null) {
            return START_SECOND_OF_DAY;
        }
        return this.mDBMgr.getHomeApRecordCount();
    }

    public void addHistoryHSCount(int incCount) {
        logd("addHistoryHSCount inc val:" + incCount);
        synchronized (this.mApInfoCache) {
            if (this.mCurrentApInfo != null) {
                WifiProApInfoRecord wifiProApInfoRecord = this.mCurrentApInfo;
                wifiProApInfoRecord.highSpdFreq += incCount;
                logd("addHistoryHSCount total:" + this.mCurrentApInfo.highSpdFreq);
            }
        }
    }

    public WifiProApInfoRecord getApInfoRecord(String apBssid) {
        if (this.mDBMgr == null || apBssid == null) {
            return null;
        }
        synchronized (this.mApInfoCache) {
            WifiProApInfoRecord retApInfo = (WifiProApInfoRecord) this.mApInfoCache.get(apBssid);
            if (retApInfo == null) {
                retApInfo = new WifiProApInfoRecord(apBssid, null, START_SECOND_OF_DAY);
                if (queryApInfo(apBssid, retApInfo)) {
                    return retApInfo;
                }
                return null;
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
        if (this.mDBMgr.queryApQualityRcd(apBssid, apQR)) {
            logi("loadApQualityRecord bssid:" + apBssid + " succ.");
            return true;
        }
        logi("loadApQualityRecord bssid:" + apBssid + " failed.");
        return false;
    }

    public void saveApQualityRecord(WifiProApQualityRcd apQR) {
        if (this.mDBMgr != null && apQR != null) {
            if (this.mDBMgr.addOrUpdateApQualityRcd(apQR)) {
                logi("saveApQualityRecord bssid:" + apQR.apBSSID + " succ.");
            } else {
                logi("saveApQualityRecord bssid:" + apQR.apBSSID + " failed.");
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
        synchronized (this.mLock) {
            if (ssid == null) {
                return null;
            }
            List<WifiProDualBandApInfoRcd> queryDualBandApInfoRcdBySsid = this.mDBMgr.queryDualBandApInfoRcdBySsid(ssid);
            return queryDualBandApInfoRcdBySsid;
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
            return new int[START_SECOND_OF_DAY];
        }
        String temp = this.mDBMgr.queryApRSSIThreshold(apBssid);
        if (temp == null) {
            return new int[START_SECOND_OF_DAY];
        }
        return convertStringToArray(temp);
    }

    private String convertArrayToString(int[] list) {
        int lenght = list.length;
        if (lenght == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = START_SECOND_OF_DAY; i < lenght - 1; i += DBG_LOG_LEVEL) {
            result.append(String.valueOf(list[i])).append(",");
        }
        result.append(String.valueOf(list[lenght - 1]));
        return result.toString();
    }

    private int[] convertStringToArray(String str) {
        String[] temp = str.split(",");
        int length = temp.length;
        int[] result = new int[length];
        if (length == 0) {
            return new int[START_SECOND_OF_DAY];
        }
        int i = START_SECOND_OF_DAY;
        while (i < length) {
            try {
                result[i] = Integer.parseInt(temp[i]);
                i += DBG_LOG_LEVEL;
            } catch (NumberFormatException e) {
                loge("NumberFormatException:" + e);
                return new int[START_SECOND_OF_DAY];
            }
        }
        return result;
    }

    public void closeWifiProHistoryDB() {
        this.mDBMgr.closeDB();
    }

    private void logd(String msg) {
        if (printLogLevel <= DBG_LOG_LEVEL) {
            Log.d(TAG, msg);
        }
    }

    private void logi(String msg) {
        if (printLogLevel <= INFO_LOG_LEVEL) {
            Log.i(TAG, msg);
        }
    }

    private void loge(String msg) {
        if (printLogLevel <= ERROR_LOG_LEVEL) {
            Log.e(TAG, msg);
        }
    }

    public boolean isHaveMultipleAP(String bssid, String ssid, int type) {
        int sameCount = this.mDBMgr.querySameSSIDApCount(bssid, ssid, type);
        loge("isHaveMultipleAP sameCount = " + sameCount);
        if (sameCount >= DBG_LOG_LEVEL) {
            return true;
        }
        return false;
    }
}
