package com.android.server.wifi.HwQoE;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.HwQoE.HwQoEQualityInfo;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.ArrayMap;
import com.android.server.wifi.HwUidTcpMonitor;
import com.android.server.wifi.HwWifiCHRStateManager;
import com.android.server.wifi.HwWifiConnectivityMonitor;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.HwWifiStatStore;
import com.android.server.wifi.HwWifiStatStoreImpl;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HwQoEService implements IHwQoEContentAwareCallback {
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int DS_KOG_LATENCY_100 = 100;
    private static final int DS_KOG_LATENCY_200 = 200;
    private static final int DS_KOG_LATENCY_300 = 300;
    private static final int DS_KOG_LATENCY_400 = 400;
    private static final int DS_KOG_LATENCY_TIME_THRESHOLD = 400;
    private static final int DS_KOG_REPORT_TIME_COUNT = 1000;
    private static final int DS_KOG_REPORT_TIME_INTERVAL = 1800000;
    private static final int DS_KOG_RTT_BIG_LATENCY_REPORT = 10;
    private static final int DS_KOG_RTT_STATIC_REPORT = 11;
    public static final int GAME_KOG_INWAR = 1;
    public static final int GAME_KOG_OFFWAR = 0;
    public static final String GAME_KOG_PROCESSNAME = "com.tencent.tmgp.sgame";
    public static final long GAME_RTT_NOTIFY_INTERVAL = 600000;
    private static final String INTENT_DS_KOG_RTT_REPORT = "com.android.intent.action.kog_rtt_report";
    public static final int KOG_CHECK_FG_APP_PERIOD = 30000;
    public static final int KOG_CHR_LATENCY_TIME_THRESHOLD = 460;
    public static final int KOG_DEFAULT_SCENE = 0;
    public static final int KOG_DEFAULT_STATUS = 0;
    public static final int KOG_DEFAULT_TYPE = 0;
    public static final int KOG_DISABLE_FUNC = 0;
    public static final int KOG_DISABLE_PS = 4;
    public static final int KOG_ENABLE_FUNC = 1;
    public static final int KOG_ENABLE_PS = 3;
    public static final int KOG_EXP_ACCEPTABLE = 2;
    public static final int KOG_EXP_DEFAULT_VALUE = 0;
    public static final int KOG_EXP_FLUENT = 1;
    public static final int KOG_EXP_STUCK = 3;
    public static final int KOG_GAME_BEGIN = 5;
    public static final int KOG_GAME_BG_TO_FG = 4;
    public static final int KOG_GAME_DIED = 5;
    public static final int KOG_GAME_EXIT = 2;
    public static final int KOG_GAME_FG_TO_BG = 3;
    public static final int KOG_GAME_PM_LOWPWR = 7;
    public static final int KOG_GAME_PM_NORMAL = 6;
    public static final int KOG_GAME_START = 1;
    public static final int KOG_IN_BUFFER = 1;
    public static final int KOG_IN_GAME = 1;
    public static final int KOG_IN_LOGGING = 4;
    public static final int KOG_IN_PLAY = 2;
    public static final int KOG_IN_VEDIO = 2;
    public static final int KOG_IN_WAR = 3;
    public static final int KOG_LATENCY_SIGNAL_THRESHOLD = 2;
    public static final int KOG_LATENCY_TIME_LOWSPEED_THRESHOLD = 460;
    public static final int KOG_LATENCY_TIME_THRESHOLD = 200;
    public static final int KOG_NOT_SUPPORT = 5;
    public static final int KOG_RTT_DEFAULT_VALUE = 0;
    public static final int KOG_RTT_VALUE = 3;
    public static final int KOG_UDP_TYPE = 17;
    public static final int MAX_GAME_RTT_NOTIFY_TIMES = 3;
    private static final String[] gameKOGReasonCode = new String[]{"GAME_LAG_1", "GAME_LAG_2", "GAME_LAG_3", "GAME_LAG_4", "GAME_LAG_5", "GAME_LAG_6", "GAME_LAG_7", "GAME_LAG_8", "GAME_LAG_9", "GAME_LAG_10"};
    private static HwQoEService mHwQoEService = null;
    public long gameKOGChrLastReportTime = 0;
    public long gameKOGTcpRtt = 0;
    public long gameKOGTcpRttDuration = 0;
    public long gameKOGTcpRttSegs = 0;
    private int gameKOGUid = 0;
    public int gameRttNotifyCountInOneGame = 0;
    private boolean isGameKOGInWar = false;
    private boolean isGameNeedDis = false;
    private boolean isKOGInBackGround = false;
    private boolean isKOGInBgRateLimitMode = false;
    private boolean isKOGInRateAjustMode = false;
    public boolean isSameKOGWar = false;
    private boolean isWiFiConnected = false;
    private int lastScene = 0;
    private int mBcnInterval;
    Runnable mCheckFGApp = new Runnable() {
        public void run() {
            if (HwQoEService.this.isKOGInRateAjustMode) {
                if (HwQoEService.isKOGInFg(HwQoEService.this.mContext)) {
                    HwQoEService.this.mQoEHandler.postDelayed(this, 30000);
                } else {
                    HwQoEService.this.setGameKOGMode(0);
                }
            }
        }
    };
    private Context mContext;
    private long mDsKOGRTT = 0;
    private int mDsKOGRtt100Count = 0;
    private int mDsKOGRtt200Count = 0;
    private int mDsKOGRtt300Count = 0;
    private int mDsKOGRtt400Count = 0;
    private int mDsKOGRttOver400Count = 0;
    private int mDsKOGTotalRttCount = 0;
    private int mDsKOGTotalRttSum = 0;
    private int mDtim;
    private ArrayList<IHwQoECallback> mEvaluateCallbackList = new ArrayList();
    private HwQoEContentAware mHwQoEContentAware;
    private HwQoEWeChatBooster mHwQoEWeChatBooster;
    private HwQoEWiFiOptimization mHwQoEWiFiOptimization;
    private HwWifiStatStore mHwWifiStatStore;
    private ArrayMap<Integer, HwQoEMonitorAdapter> mIHwQoEMonitorList = new ArrayMap();
    private boolean mIsInGameState = false;
    private Date mLastRptRttBigLatencyTime = null;
    private Object mLock = new Object();
    private Handler mQoEHandler;
    private HwWifiCHRStateManager mWiFiCHRManager;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;
    private WifiStateMachine mWifiStateMachine;

    private HwQoEService(Context context, WifiStateMachine wifiStateMachine) {
        this.mContext = context;
        this.mWifiStateMachine = wifiStateMachine;
        initQoEAdapter();
        new HwQoEWiFiScenario(context, this.mQoEHandler).startMonitor();
        this.mHwQoEContentAware = HwQoEContentAware.createInstance(this.mContext, this);
        this.mHwQoEWeChatBooster = HwQoEWeChatBooster.createInstance(context);
        this.mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
        this.mHwWifiStatStore = HwWifiStatStoreImpl.getDefault();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwQoEWiFiOptimization = HwQoEWiFiOptimization.getInstance(context);
    }

    public static void createHwQoEService(Context context, WifiStateMachine wifiStateMachine) {
        HwQoEUtils.logE("createHwQoEService");
        mHwQoEService = new HwQoEService(context, wifiStateMachine);
    }

    public static HwQoEService getInstance() {
        if (mHwQoEService != null) {
            return mHwQoEService;
        }
        return null;
    }

    public void updateWifiTimParam(int dtim, int bcnInterval) {
        HwQoEUtils.logD("updateWifiTimParam dtim = " + dtim + " bcnInterval = " + bcnInterval);
        this.mDtim = dtim;
        this.mBcnInterval = bcnInterval;
    }

    public void updateWifiSleepWhiteList(int type, List<String> packageWhiteList) {
        this.mHwQoEContentAware.updateWifiSleepWhiteList(type, packageWhiteList);
    }

    /* JADX WARNING: Missing block: B:17:0x0067, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean registerHwQoEMonitor(int type, int period, IHwQoECallback callback) {
        synchronized (this.mLock) {
            HwQoEUtils.logE("registerHwQoEMonitor period = " + period + " type = " + type);
            if (callback == null) {
                HwQoEUtils.logE("registerHwQoEMonitor callbakc error ");
                return false;
            }
            HwQoEMonitorAdapter rd = (HwQoEMonitorAdapter) this.mIHwQoEMonitorList.get(Integer.valueOf(type));
            if (rd == null) {
                HwQoEUtils.logE("registerHwQoEMonitor add new call back");
                HwQoEMonitorAdapter mAdapter = new HwQoEMonitorAdapter(this.mContext, this.mWifiStateMachine, new HwQoEMonitorConfig(period, true), callback);
                this.mIHwQoEMonitorList.put(Integer.valueOf(type), mAdapter);
                rd = mAdapter;
            } else {
                rd.updateCallback(callback);
            }
            if (this.isWiFiConnected) {
                rd.startMonitor();
            }
        }
    }

    public boolean unRegisterHwQoEMonitor(int type) {
        synchronized (this.mLock) {
            HwQoEUtils.logD("unRegisterHwQoEMonitor");
            HwQoEMonitorAdapter rd = (HwQoEMonitorAdapter) this.mIHwQoEMonitorList.get(Integer.valueOf(type));
            if (rd != null) {
                HwQoEUtils.logD("unRegisterHwQoEMonitor find target adapter");
                rd.stopMonitor();
                rd.release();
                this.mIHwQoEMonitorList.remove(Integer.valueOf(type));
            }
        }
        return true;
    }

    public boolean evaluateNetworkQuality(IHwQoECallback callback) {
        synchronized (this.mLock) {
            if (callback != null) {
                HwQoEUtils.logD("evaluateNetworkQuality");
                this.mEvaluateCallbackList.add(callback);
                this.mQoEHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_START_EVALUATE);
                return true;
            }
            HwQoEUtils.logD("evaluateNetworkQuality callback == null");
            return false;
        }
    }

    public boolean updateVOWIFIState(int state) {
        HwQoEMonitorAdapter rd = (HwQoEMonitorAdapter) this.mIHwQoEMonitorList.get(Integer.valueOf(HwQoEQualityInfo.HWQOE_MONITOR_TYPE_VOWIFI));
        if (rd != null) {
            rd.updateVOWIFIState(state);
        }
        return true;
    }

    public boolean notifyNetworkRoaming() {
        synchronized (this.mLock) {
            HwQoEUtils.logD("HwQoEService NotifyNetworkRoaming");
            this.mQoEHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_ROAMING);
        }
        return true;
    }

    private void initGameKOGParams() {
        this.isGameKOGInWar = false;
        this.isKOGInBackGround = false;
        this.isKOGInRateAjustMode = false;
        this.gameKOGUid = 0;
    }

    private void reportNetGameSense(int enable) {
        HwQoEUtils.logD("HwQoEService: reportNetGameSense: " + enable);
        this.mHwQoEWiFiOptimization.hwQoELimitedSpeed(enable);
    }

    private void setGameKOGHighPriorityTransmit(int enable) {
        HwQoEUtils.logD("HwQoEService: setGameKOGHighPriorityTransmit uid: " + this.gameKOGUid + " enable: " + enable);
        this.mHwQoEWiFiOptimization.hwQoEHighPriorityTransmit(this.gameKOGUid, 17, enable);
    }

    private void setGameKOGMode(int mode) {
        if ((6 != mode && 7 != mode) || (isPermitUpdateWifiPowerMode(mode) ^ 1) == 0) {
            HwQoEUtils.logD("HwQoEService: setGameKOGMode uid: " + this.gameKOGUid + " mode: " + mode);
            this.mHwQoEWiFiOptimization.hwQoEAdjustSpeed(mode);
        }
    }

    private void setTXPower(int enable) {
        this.mHwQoEWiFiOptimization.setTXPower(enable);
    }

    private static List<RunningAppProcessInfo> getRunningProcesses(Context context) {
        if (context == null) {
            HwQoEUtils.logD("HwQoEService: getRunningProcesses, input is null");
            return null;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager != null) {
            return activityManager.getRunningAppProcesses();
        }
        HwQoEUtils.logD("HwQoEService: getRunningProcesses, get ams service failed");
        return null;
    }

    private static boolean isKOGInFg(Context context) {
        List<RunningAppProcessInfo> processes = getRunningProcesses(context);
        if (processes == null) {
            HwQoEUtils.logD("HwQoEService: isKOGInFg failed");
            return false;
        }
        for (RunningAppProcessInfo processInfo : processes) {
            if (processInfo.importance == 100) {
                HwQoEUtils.logD("HwQoEService: isKOGInFg " + processInfo.processName);
                if ("com.tencent.tmgp.sgame".equals(processInfo.processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isWifi() {
        NetworkInfo activeNetInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetInfo == null || activeNetInfo.getType() != 1) {
            return false;
        }
        return true;
    }

    private void initGameKOGChr() {
        this.isSameKOGWar = false;
        this.gameRttNotifyCountInOneGame = 0;
        this.gameKOGChrLastReportTime = 0;
        this.gameKOGTcpRttDuration = 0;
        this.gameKOGTcpRttSegs = 0;
        this.gameKOGTcpRtt = 0;
    }

    private void exitGameKOGWar() {
        HwQoEUtils.logD("HwQoEService:exitGameKOGWar");
        if (this.isKOGInRateAjustMode) {
            setGameKOGMode(0);
            this.mQoEHandler.removeCallbacks(this.mCheckFGApp);
        }
        if (this.isGameKOGInWar) {
            setGameKOGHighPriorityTransmit(0);
            reportNetGameSense(0);
            setGameKOGMode(3);
            setTXPower(0);
        }
        this.isGameNeedDis = false;
        this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
    }

    private int getScene(int status, int scene) {
        if (this.lastScene == 0 && 3 == scene) {
            this.lastScene = 3;
            return 1;
        } else if (3 == this.lastScene && scene == 0) {
            this.lastScene = 0;
            return 2;
        } else if (3 == scene && 3 == status) {
            return 3;
        } else {
            if (3 == scene && 4 == status) {
                return 4;
            }
            return 5 == status ? 5 : 0;
        }
    }

    private void handleWifiScene(int scene) {
        switch (scene) {
            case 1:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_START");
                initGameKOGChr();
                if (!this.isGameKOGInWar) {
                    this.isGameKOGInWar = true;
                    setGameKOGMode(5);
                    setGameKOGMode(4);
                    setGameKOGHighPriorityTransmit(1);
                    reportNetGameSense(1);
                    setTXPower(1);
                    if (this.mHwWifiStatStore != null) {
                        this.mHwWifiStatStore.setGameKogScene(1);
                        return;
                    }
                    return;
                }
                return;
            case 2:
            case 5:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_EXIT or KOG_GAME_DIED");
                exitGameKOGWar();
                initGameKOGParams();
                if (this.mHwWifiStatStore != null) {
                    this.mHwWifiStatStore.setGameKogScene(0);
                    return;
                }
                return;
            case 3:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_FG_TO_BG");
                if (!this.isKOGInBackGround) {
                    this.isKOGInBackGround = true;
                    exitGameKOGWar();
                }
                if (this.mHwWifiStatStore != null) {
                    this.mHwWifiStatStore.setGameKogScene(0);
                    return;
                }
                return;
            case 4:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_BG_TO_FG");
                if (this.isKOGInBackGround) {
                    this.isKOGInBackGround = false;
                    if (this.isKOGInRateAjustMode) {
                        setGameKOGMode(1);
                    }
                    if (this.isGameKOGInWar) {
                        setGameKOGMode(4);
                        setGameKOGHighPriorityTransmit(1);
                        reportNetGameSense(1);
                        setTXPower(1);
                        if (this.mHwWifiStatStore != null) {
                            this.mHwWifiStatStore.setGameKogScene(1);
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void handleCellularScene(int scene) {
        switch (scene) {
            case 1:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_START, use cellular, enable bg rate limit");
                if (!this.isKOGInBgRateLimitMode) {
                    reportNetGameSense(1);
                    this.isKOGInBgRateLimitMode = true;
                    return;
                }
                return;
            case 2:
            case 5:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_EXIT or KOG_GAME_DIED, use cellular, disable bg rate limit");
                if (this.isKOGInBgRateLimitMode) {
                    reportNetGameSense(0);
                    this.isKOGInBgRateLimitMode = false;
                    return;
                }
                return;
            case 3:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_FG_TO_BG, use cellular, disable bg rate limit");
                if (!this.isKOGInBackGround) {
                    this.isKOGInBackGround = true;
                    reportNetGameSense(0);
                    return;
                }
                return;
            case 4:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_BG_TO_FG, use cellular, enable bg rate limit");
                if (this.isKOGInBackGround) {
                    this.isKOGInBackGround = false;
                    reportNetGameSense(1);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public boolean updateAppRunningStatus(int uid, int type, int status, int scene, int reserved) {
        HwQoEUtils.logD("HwQoEService: updateAppRunningStatus uid " + uid + " type " + type + " status " + status + " scene " + scene);
        this.gameKOGUid = uid;
        if (isWifi()) {
            handleWifiScene(getScene(status, scene));
        } else {
            handleCellularScene(getScene(status, scene));
        }
        return true;
    }

    public boolean updateAppExperienceStatus(int uid, int experience, long rtt, int reserved) {
        long gameKOGCurrentTime = System.currentTimeMillis();
        HwQoEUtils.logD("HwQoEService: updateAppExperienceStatus uid:" + uid + " experience:" + experience + " rtt:" + rtt);
        int rssiLevel = 2;
        if (isWifi()) {
            if (rtt == 460) {
                HwQoEUtils.logD("HwQoEService: gameKOGElapseTime:" + (gameKOGCurrentTime - this.gameKOGChrLastReportTime));
                if (this.gameRttNotifyCountInOneGame < 3 && gameKOGCurrentTime - this.gameKOGChrLastReportTime > GAME_RTT_NOTIFY_INTERVAL && this.isGameKOGInWar) {
                    HwUidTcpMonitor gameKOGTcpMonitor = HwUidTcpMonitor.getInstance(this.mContext);
                    this.gameKOGTcpRttDuration = gameKOGTcpMonitor.getRttDuration(uid);
                    this.gameKOGTcpRttSegs = gameKOGTcpMonitor.getRttSegs(uid);
                    if (0 != this.gameKOGTcpRttSegs) {
                        this.gameKOGTcpRtt = this.gameKOGTcpRttDuration / this.gameKOGTcpRttSegs;
                    } else {
                        this.gameKOGTcpRtt = 0;
                    }
                    if (this.mWiFiCHRManager != null) {
                        this.mWiFiCHRManager.updateGameBoostLag(gameKOGReasonCode[this.gameRttNotifyCountInOneGame], "com.tencent.tmgp.sgame", (int) rtt, (int) this.gameKOGTcpRtt);
                    }
                    HwQoEUtils.logD("HwQoEService: gameRttNotifyCountInOneGame:" + this.gameRttNotifyCountInOneGame);
                    HwQoEUtils.logD("HwQoEService: gameKOGTcpRttDuration:" + this.gameKOGTcpRttDuration + " gameKOGTcpRttSegs:" + this.gameKOGTcpRttSegs + " gameKOGTcpRtt:" + this.gameKOGTcpRtt);
                    this.gameKOGChrLastReportTime = gameKOGCurrentTime;
                    this.gameRttNotifyCountInOneGame++;
                }
            }
            if (rtt > 200) {
                if (this.mHwWifiStatStore != null) {
                    this.mHwWifiStatStore.updateGameBoostStatic("com.tencent.tmgp.sgame", this.isSameKOGWar ^ 1);
                }
                if (!this.isSameKOGWar) {
                    this.isSameKOGWar = true;
                }
            }
            if (this.mWifiManager != null) {
                this.mWifiInfo = this.mWifiManager.getConnectionInfo();
                if (this.mWifiInfo != null) {
                    rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mWifiInfo.getFrequency(), this.mWifiInfo.getRssi());
                }
            }
            if (!this.isKOGInRateAjustMode && this.isGameKOGInWar && rtt == 460 && rssiLevel < 2) {
                setGameKOGMode(1);
                this.mQoEHandler.postDelayed(this.mCheckFGApp, 30000);
                this.isKOGInRateAjustMode = true;
            }
            if (this.isKOGInRateAjustMode && this.isGameKOGInWar && rtt < 200) {
                setGameKOGMode(0);
                this.mQoEHandler.removeCallbacks(this.mCheckFGApp);
                this.isKOGInRateAjustMode = false;
            }
            HwQoEUtils.logD("HwQoEService: isGameKOGInWar  = " + this.isGameKOGInWar + " rtt = " + rtt + " rssiLevel = " + rssiLevel);
            if (rtt <= 200 || rssiLevel >= 2) {
                if (rtt > 200) {
                    this.mDsKOGRTT = rtt;
                }
            } else if (!this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
                this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
            }
            if (rtt < 200) {
                this.mDsKOGRTT = 0;
                this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
            }
            return true;
        }
        if (rtt > 400) {
            sendIntentDsKOGRttBigLatency(this.mContext, rtt);
        }
        if (rtt > 0 && rtt <= 100) {
            this.mDsKOGRtt100Count++;
        } else if (rtt <= 200) {
            this.mDsKOGRtt200Count++;
        } else if (rtt <= 300) {
            this.mDsKOGRtt300Count++;
        } else if (rtt <= 400) {
            this.mDsKOGRtt400Count++;
        } else {
            this.mDsKOGRttOver400Count++;
        }
        this.mDsKOGTotalRttSum = (int) (((long) this.mDsKOGTotalRttSum) + rtt);
        this.mDsKOGTotalRttCount++;
        if (this.mDsKOGTotalRttCount > 1000) {
            sendIntentDsKOGRttStatic(this.mContext);
        }
        return true;
    }

    private void initQoEAdapter() {
        HandlerThread handlerThread = new HandlerThread("initQoEAdapter monior Thread");
        handlerThread.start();
        this.mQoEHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                Object -get10;
                int i;
                switch (msg.what) {
                    case HwQoEUtils.QOE_MSG_WIFI_DISABLE /*108*/:
                        HwQoEUtils.logD("HwQoEService: WIFI is disable,MAINLAND_REGION:" + HwQoEUtils.MAINLAND_REGION);
                        HwQoEService.this.exitGameKOGWar();
                        HwQoEService.this.initGameKOGParams();
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_CONNECTED /*109*/:
                    case HwQoEUtils.QOE_MSG_WIFI_INTERNET /*111*/:
                        -get10 = HwQoEService.this.mLock;
                        synchronized (-get10) {
                            if (!HwQoEService.this.isWiFiConnected) {
                                HwQoEUtils.logD("HwQoEContentAware,registerProcessObserver");
                                HwQoEService.this.mHwQoEContentAware.queryForegroundAppType();
                                HwQoEService.this.mHwQoEContentAware.registerProcessObserver();
                            }
                            HwQoEService.this.isWiFiConnected = true;
                            HwQoEUtils.logD("WIFI is connected");
                            for (i = 0; i < HwQoEService.this.mIHwQoEMonitorList.size(); i++) {
                                ((HwQoEMonitorAdapter) HwQoEService.this.mIHwQoEMonitorList.valueAt(i)).startMonitor();
                            }
                            break;
                        }
                    case 110:
                        synchronized (HwQoEService.this.mLock) {
                            HwQoEService.this.isWiFiConnected = false;
                            for (i = 0; i < HwQoEService.this.mIHwQoEMonitorList.size(); i++) {
                                ((HwQoEMonitorAdapter) HwQoEService.this.mIHwQoEMonitorList.valueAt(i)).stopMonitor();
                            }
                        }
                        HwQoEUtils.logD("HwQoEService: WIFI is disconnected");
                        HwQoEService.this.exitGameKOGWar();
                        HwQoEService.this.initGameKOGParams();
                        HwQoEService.this.mHwQoEContentAware.unregisterProcessObserver();
                        HwQoEService.this.mDtim = 0;
                        HwQoEService.this.mBcnInterval = 0;
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_START_EVALUATE /*113*/:
                        HwQoEUtils.logD("WIFI is QOE_MSG_WIFI_START_EVALUATE");
                        -get10 = HwQoEService.this.mLock;
                        synchronized (-get10) {
                            HwQoEEvaluateAdapter mHwQoEEvaluateAdapter = HwQoEEvaluateAdapter.getInstance(HwQoEService.this.mContext, HwQoEService.this.mWifiStateMachine);
                            if (mHwQoEEvaluateAdapter != null) {
                                for (IHwQoECallback callback : HwQoEService.this.mEvaluateCallbackList) {
                                    mHwQoEEvaluateAdapter.evaluateNetworkQuality(callback);
                                }
                                HwQoEService.this.mEvaluateCallbackList.clear();
                                break;
                            }
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_ROAMING /*115*/:
                        HwQoEUtils.logD("HwQoEService: WIFI is roaming");
                        if (HwQoEService.this.isKOGInRateAjustMode) {
                            HwQoEService.this.setGameKOGMode(0);
                            HwQoEService.this.isKOGInRateAjustMode = false;
                        }
                        if (!HwQoEService.this.isKOGInBackGround && HwQoEService.this.isGameKOGInWar) {
                            HwQoEService.this.setGameKOGMode(4);
                            break;
                        }
                    case HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT /*116*/:
                        HwQoEUtils.logD("HwQoEService: QOE_MSG_WIFI_DELAY_DISCONNECT");
                        HwQoEService.this.mWifiInfo = HwQoEService.this.mWifiManager.getConnectionInfo();
                        if (HwQoEService.this.mWifiInfo != null && HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(HwQoEService.this.mWifiInfo.getFrequency(), HwQoEService.this.mWifiInfo.getRssi()) < 2) {
                            HwQoEService.this.disconWiFiNetwork();
                            break;
                        }
                    case HwQoEUtils.QOE_MSG_WIFI_RSSI_CHANGED /*117*/:
                        HwQoEService.this.mWifiInfo = HwQoEService.this.mWifiManager.getConnectionInfo();
                        if (HwQoEService.this.mWifiInfo != null) {
                            int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(HwQoEService.this.mWifiInfo.getFrequency(), HwQoEService.this.mWifiInfo.getRssi());
                            HwQoEUtils.logD("HwQoEService: QOE_MSG_WIFI_RSSI_CHANGED mDsKOGRTT = " + HwQoEService.this.mDsKOGRTT + " rssiLevel = " + rssiLevel);
                            if (!HwQoEService.this.mIsInGameState) {
                                if (HwQoEService.this.mDsKOGRTT > 200 && rssiLevel < 2 && !HwQoEService.this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
                                    HwQoEService.this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                                    break;
                                }
                            } else if (rssiLevel >= 2) {
                                if (HwQoEService.this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
                                    HwQoEService.this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
                                    break;
                                }
                            } else if (!HwQoEService.this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
                                HwQoEService.this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT, 4000);
                                break;
                            }
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void sendIntentDsKOGRttBigLatency(Context context, long rtt) {
        try {
            Date now = new Date();
            if (this.mLastRptRttBigLatencyTime == null || now.getTime() - this.mLastRptRttBigLatencyTime.getTime() > 1800000) {
                Intent chrIntent = new Intent(INTENT_DS_KOG_RTT_REPORT);
                Bundle extras = new Bundle();
                extras.putInt("ReportType", 10);
                extras.putInt("RTT", (int) rtt);
                chrIntent.putExtras(extras);
                context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
            }
            if (!(now == null || this.mLastRptRttBigLatencyTime == null)) {
                HwQoEUtils.logD(" sendIntentDsKOGRttBigLatency now = " + now.getTime() + " mLastRptRttBigLatencyTime " + this.mLastRptRttBigLatencyTime.getTime());
            }
            this.mLastRptRttBigLatencyTime = now;
        } catch (RuntimeException e) {
            HwQoEUtils.logD("sendIntentDsKOGRttBigLatency get state RuntimeException");
        } catch (Exception e2) {
            HwQoEUtils.logD("sendIntentDsKOGRttBigLatency get state Exception");
        }
    }

    private void sendIntentDsKOGRttStatic(Context context) {
        try {
            Intent chrIntent = new Intent(INTENT_DS_KOG_RTT_REPORT);
            Bundle extras = new Bundle();
            extras.putInt("ReportType", 11);
            extras.putInt("mDsKOGRtt100Count", this.mDsKOGRtt100Count);
            extras.putInt("mDsKOGRtt200Count", this.mDsKOGRtt200Count);
            extras.putInt("mDsKOGRtt300Count", this.mDsKOGRtt300Count);
            extras.putInt("mDsKOGRtt400Count", this.mDsKOGRtt400Count);
            extras.putInt("mDsKOGRttOver400Count", this.mDsKOGRttOver400Count);
            extras.putInt("mDsKOGTotalRttSum", this.mDsKOGTotalRttSum);
            extras.putInt("mDsKOGTotalRttCount", this.mDsKOGTotalRttCount);
            chrIntent.putExtras(extras);
            context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
            HwQoEUtils.logD(" sendIntentDsKOGRttStatic mDsKOGRtt100Count = " + this.mDsKOGRtt100Count + " mDsKOGRtt200Count = " + this.mDsKOGRtt200Count + " mDsKOGRtt300Count = " + this.mDsKOGRtt300Count + " mDsKOGRtt400Count = " + this.mDsKOGRtt400Count + " mDsKOGRttOver400Count = " + this.mDsKOGRttOver400Count + " mDsKOGTotalRttSum = " + this.mDsKOGTotalRttSum + " mDsKOGTotalRttCount = " + this.mDsKOGTotalRttCount);
            this.mDsKOGRtt100Count = 0;
            this.mDsKOGRtt200Count = 0;
            this.mDsKOGRtt300Count = 0;
            this.mDsKOGRtt400Count = 0;
            this.mDsKOGRttOver400Count = 0;
            this.mDsKOGTotalRttSum = 0;
            this.mDsKOGTotalRttCount = 0;
        } catch (RuntimeException e) {
            HwQoEUtils.logD("sendIntentDsKOGRttStatic get state RuntimeException");
        } catch (Exception e2) {
            HwQoEUtils.logD("sendIntentDsKOGRttStatic get state Exception ");
        }
    }

    private void disconWiFiNetwork() {
        HwQoEUtils.logD("HwQoEService: disconWiFiNetwork");
        if (isGameSwitchOpen()) {
            this.isGameNeedDis = true;
            HwWifiConnectivityMonitor.getInstance().disconnectePoorWifi();
        }
    }

    private boolean isPermitUpdateWifiPowerMode(int mode) {
        if (!HwQoEUtils.MAINLAND_REGION) {
            return false;
        }
        if (7 != mode) {
            return true;
        }
        HwQoEUtils.logD("isPermitWifiPowerMode mDtim:" + this.mDtim + ", mBcnInterval: " + this.mBcnInterval);
        return (this.mDtim == 1 || this.mDtim == 2) && this.mBcnInterval == 100;
    }

    public boolean isInGameAndNeedDisc() {
        return this.isGameNeedDis;
    }

    public void updateVNPStateChanged(boolean isVpnConnected) {
        this.mHwQoEWiFiOptimization.updateVNPStateChanged(isVpnConnected);
    }

    public boolean isBgLimitAllowed(int uid) {
        if (this.mHwQoEContentAware.isLiveStreamApp(uid)) {
            return false;
        }
        return true;
    }

    public void onPeriodSpeed(long speed) {
    }

    public void onSensitiveAppStateChange(int uid, int state, boolean isBackground) {
    }

    public void onForegroundAppWifiSleepChange(boolean config, int sleeptime, int type, String appName) {
        String hwSsid = "\"Huawei-Employee\"";
        HwQoEUtils.logD("onForegroundAppWifiSleepChange: config:" + config + ", sleeptime:" + sleeptime + ",type: " + type + ", appname:" + appName);
        if (config) {
            setGameKOGMode(6);
        } else if (this.mHwQoEContentAware.isGameType(type, appName)) {
            setGameKOGMode(6);
            if (!this.isKOGInBackGround && this.isGameKOGInWar) {
                setGameKOGMode(4);
            }
        } else if (this.mWifiManager != null && this.mWifiManager.getConnectionInfo() != null) {
            String ssid = this.mWifiManager.getConnectionInfo().getSSID();
            boolean isMobileAP = this.mWifiManager.getConnectionInfo().getMeteredHint();
            boolean isAndroidMobileAP = false;
            if (this.mWifiStateMachine != null && (this.mWifiStateMachine instanceof HwWifiStateMachine)) {
                isAndroidMobileAP = ((HwWifiStateMachine) this.mWifiStateMachine).isAndroidMobileAP();
            }
            if ((ssid != null && ssid.equals(hwSsid)) || (isMobileAP && isAndroidMobileAP)) {
                setGameKOGMode(7);
            }
        }
    }

    public void onForegroundAppTypeChange(int type, String appName) {
        HwQoEUtils.logD("APP type: " + type + " appName= " + appName);
        if (this.mHwQoEContentAware.isGameType(type, appName)) {
            HwQoEUtils.logD("APP_TYPE_GAME, isKOGInBackGround = " + this.isKOGInBackGround);
            if ("com.tencent.tmgp.sgame".equals(appName)) {
                this.mIsInGameState = false;
                this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
                return;
            }
            this.mIsInGameState = true;
            return;
        }
        HwQoEUtils.logD("no game, APP_TYPE_GAME, appName =" + appName);
        this.mIsInGameState = false;
        this.isGameNeedDis = false;
        this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
    }

    private boolean isGameSwitchOpen() {
        if (SystemProperties.get("ro.product.name", "").startsWith("BKL")) {
            return true;
        }
        return false;
    }
}
