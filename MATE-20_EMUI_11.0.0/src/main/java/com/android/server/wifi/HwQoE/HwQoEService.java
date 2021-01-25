package com.android.server.wifi.HwQoE;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.arbitration.HwArbitrationCallbackImpl;
import com.android.server.hidata.arbitration.HwArbitrationManager;
import com.android.server.hidata.arbitration.IGameCHRCallback;
import com.android.server.hidata.arbitration.IHiDataCHRCallBack;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.histream.HwHistreamCHRQoeInfo;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.MSS.HwMSSArbitrager;
import com.android.server.wifi.wifipro.HwWifiProServiceManager;
import com.huawei.android.telephony.SubscriptionManagerEx;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HwQoEService implements IHwQoEContentAwareCallback, IGameCHRCallback, IHiDataCHRCallBack {
    private static final int BETA_USER = 3;
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int COMMERCIAL_USER = 1;
    private static final int DISABLED = 0;
    private static final int DS_KOG_LATENCY_TIME_THRESHOLD = 400;
    private static final int DS_KOG_REPORT_THRESHOLD_0_200_SAMPLING_RATIO = 1000;
    private static final int DS_KOG_REPORT_THRESHOLD_200_400_SAMPLING_RATIO = 64;
    private static final int DS_KOG_REPORT_THRESHOLD_OVER_400_SAMPLING_RATIO = 3;
    private static final int DS_KOG_REPORT_TIME_INTERVAL = 180000;
    private static final int DS_KOG_REPORT_TIME_INTERVAL_FOR_BETA = 60000;
    private static final int DS_KOG_RTT_BIG_LATENCY_REPORT = 1;
    private static final int ENABLED = 1;
    public static final int GAME_KOG_INWAR = 1;
    public static final int GAME_KOG_OFFWAR = 0;
    public static final String GAME_KOG_PROCESSNAME = "com.tencent.tmgp.sgame";
    private static final String[] GAME_KOG_REASON_CODE = {"GAME_LAG_1", "GAME_LAG_2", "GAME_LAG_3", "GAME_LAG_4", "GAME_LAG_5", "GAME_LAG_6", "GAME_LAG_7", "GAME_LAG_8", "GAME_LAG_9", "GAME_LAG_10"};
    public static final long GAME_RTT_NOTIFY_INTERVAL = 600000;
    private static final boolean HIDATA_ENABLED = SystemProperties.getBoolean("ro.config.hw_hidata.enabled", true);
    private static final int HWQOE_MONITOR_TYPE_VOWIFI = 1;
    private static final String INTENT_DS_KOG_RTT_REPORT = "com.huawei.intent.action.kog_rtt_report";
    private static final String INTENT_HIDATA_DS_CHR_REPORT = "com.huawei.intent.action.hidata_ds_report";
    private static final int INVALID_NUM = -1;
    public static final int KOG_CHECK_FG_APP_PERIOD = 30000;
    public static final int KOG_CHR_LATENCY_TIME_THRESHOLD = 460;
    private static final boolean KOG_CHR_REPORT_IN_ONE_MINUTE = SystemProperties.getBoolean("ro.config.kog_chr_one_minute", false);
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
    private static final int MAX_SLOT_ID = 2;
    private static final int NSA_STATE0 = 0;
    private static final int NSA_STATE1 = 1;
    private static final int NSA_STATE2 = 2;
    private static final int NSA_STATE3 = 3;
    private static final int NSA_STATE4 = 4;
    private static final int NSA_STATE5 = 5;
    private static final int NSA_STATE6 = 6;
    private static final int OVER_SEA_BETA_USER = 5;
    public static final int SEL_ENGINE_END_FAILED = 4;
    public static final int SEL_ENGINE_END_SUCCESS = 3;
    public static final int SEL_ENGINE_START = 2;
    private static final int TIMEOUT_GAME_TIMEOUT_MONITOR = 120000;
    private static final int TIMEOUT_UPDATE_TIMEOUT_MONITOR = 600000;
    public static final int TYPE_VOWIFI = 1;
    private static final int UPDATE_UID_TCP_RTT_MILLISECONDS = 8000;
    private static final int WIFI_DISCONNECT_MSG_DELAY_INGAME = 4000;
    private static final int WIFI_DISCONNECT_MSG_DELAY_OUTGAME = 3000;
    private static HwQoEService mHwQoEService = null;
    public long gameKOGChrLastReportTime = 0;
    public long gameKOGTcpRtt = 0;
    public long gameKOGTcpRttDuration = 0;
    public long gameKOGTcpRttSegs = 0;
    private int gameKOGUid = 0;
    public int gameRttNotifyCountInOneGame = 0;
    private boolean isGameKogInWar = false;
    private boolean isGameNeedDis = false;
    private boolean isKOGInBackGround = false;
    private boolean isKOGInBgRateLimitMode = false;
    private boolean isKOGInRateAjustMode = false;
    public boolean isSameKOGWar = false;
    private boolean isWiFiConnected = false;
    public long lastGameKOGTcpRttDuration = 0;
    public long lastGameKOGTcpRttSegs = 0;
    private int lastScene = 0;
    private int mBcnInterval;
    Runnable mCheckFGApp = new Runnable() {
        /* class com.android.server.wifi.HwQoE.HwQoEService.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            if (!HwQoEService.this.isKOGInRateAjustMode || HwQoEService.isKogInFg(HwQoEService.this.mContext)) {
                HwQoEService.this.mQoEHandler.postDelayed(this, 30000);
            }
        }
    };
    private Context mContext;
    private int mCurNetworkType;
    private long mDsKOGRTT = 0;
    private int mDtim;
    private ArrayList<IHwQoECallback> mEvaluateCallbackList = new ArrayList<>();
    private int mForegroundAppUid;
    private HwMSSArbitrager mHwMssArb = null;
    private HwQoEAccAppAware mHwQoEAccAppAware = null;
    private HwQoEContentAware mHwQoEContentAware;
    private HwQoEGameCHRImpl mHwQoEGameCHRImpl;
    private HwQoEHilink mHwQoEHilink = null;
    private HwQoEWiFiOptimization mHwQoEWiFiOptimization;
    private HwWifiBoost mHwWifiBoost;
    private HwWifiCHRService mHwWifiCHRService;
    private HwWifiProServiceManager mHwWifiProServiceManager;
    private ArrayMap<Integer, HwQoEMonitorAdapter> mIHwQoEMonitorList = new ArrayMap<>();
    private boolean mIsInGameState = false;
    private Date mLastRptRttBigLatencyTime = null;
    private final Object mLock = new Object();
    private Map<Integer, PhoneStateListener> mPhoneStateListeners = new ArrayMap();
    private int mPreviousUid = 0;
    private Handler mQoEHandler;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener = null;
    private SubscriptionManager mSubscriptionManager = null;
    private TelephonyManager mTelephonyManager = null;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;
    private ClientModeImpl mWifiStateMachine;
    private boolean wifiHotLimitSpeedEnable = false;

    private HwQoEService(Context context, ClientModeImpl wifiStateMachine) {
        this.mContext = context;
        this.mWifiStateMachine = wifiStateMachine;
        initQoEAdapter();
        new HwQoEWiFiScenario(context, this.mQoEHandler).startMonitor();
        this.mHwQoEContentAware = HwQoEContentAware.createInstance(this.mContext, this);
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwQoEWiFiOptimization = HwQoEWiFiOptimization.getInstance(context);
        this.mHwWifiBoost = HwWifiBoost.getInstance(context);
        this.mHwMssArb = HwMSSArbitrager.getInstance(context);
        this.mHwQoEGameCHRImpl = new HwQoEGameCHRImpl(context);
        this.mHwWifiProServiceManager = HwWifiProServiceManager.createHwWifiProServiceManager(context);
        if (HIDATA_ENABLED) {
            HwArbitrationManager.createInstance(context, this);
            HwQoEUtils.logE(false, "HiDATA Arbitration has started in HwQoEService", new Object[0]);
        }
        this.mHwQoEHilink = HwQoEHilink.getInstance(this.mContext);
        this.mHwQoEAccAppAware = new HwQoEAccAppAware(context, this.mHwQoEHilink);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        registerSubscriptionsChangedListeners();
    }

    public static void createHwQoEService(Context context, ClientModeImpl wifiStateMachine) {
        HwQoEUtils.logE(false, "createHwQoEService", new Object[0]);
        mHwQoEService = new HwQoEService(context, wifiStateMachine);
    }

    public static HwQoEService getInstance() {
        HwQoEService hwQoEService = mHwQoEService;
        if (hwQoEService != null) {
            return hwQoEService;
        }
        return null;
    }

    public void updateWifiConnectionMode(boolean isUserManualConnect, boolean isUserHandoverWiFi) {
    }

    public void updateWifiTimParam(int dtim, int bcnInterval) {
        HwQoEUtils.logD(false, "updateWifiTimParam dtim = %{public}d bcnInterval = %{public}d", Integer.valueOf(dtim), Integer.valueOf(bcnInterval));
        this.mDtim = dtim;
        this.mBcnInterval = bcnInterval;
    }

    public void updateWifiSleepWhiteList(int type, List<String> packageWhiteList) {
        this.mHwQoEContentAware.updateWifiSleepWhiteList(type, packageWhiteList);
    }

    public boolean registerHwQoEMonitor(int type, int period, IHwQoECallback callback) {
        synchronized (this.mLock) {
            HwQoEUtils.logE(false, "registerHwQoEMonitor period = %{public}d type = %{public}d", Integer.valueOf(period), Integer.valueOf(type));
            if (callback == null) {
                HwQoEUtils.logE(false, "registerHwQoEMonitor callbakc error ", new Object[0]);
                return false;
            }
            HwQoEMonitorAdapter rd = this.mIHwQoEMonitorList.get(Integer.valueOf(type));
            if (rd == null) {
                HwQoEUtils.logE(false, "registerHwQoEMonitor add new call back", new Object[0]);
                HwQoEMonitorAdapter mAdapter = new HwQoEMonitorAdapter(this.mContext, this.mWifiStateMachine, new HwQoEMonitorConfig(period, true), callback);
                this.mIHwQoEMonitorList.put(Integer.valueOf(type), mAdapter);
                rd = mAdapter;
            } else {
                rd.updateCallback(callback);
            }
            if (this.isWiFiConnected) {
                rd.startMonitor();
            }
            return true;
        }
    }

    public boolean unRegisterHwQoEMonitor(int type) {
        synchronized (this.mLock) {
            HwQoEUtils.logD(false, "unRegisterHwQoEMonitor", new Object[0]);
            HwQoEMonitorAdapter rd = this.mIHwQoEMonitorList.get(Integer.valueOf(type));
            if (rd != null) {
                HwQoEUtils.logD(false, "unRegisterHwQoEMonitor find target adapter", new Object[0]);
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
                HwQoEUtils.logD(false, "evaluateNetworkQuality", new Object[0]);
                this.mEvaluateCallbackList.add(callback);
                this.mQoEHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_START_EVALUATE);
                return true;
            }
            HwQoEUtils.logD(false, "evaluateNetworkQuality callback == null", new Object[0]);
            return false;
        }
    }

    public boolean updateVoWiFiState(int state) {
        HwQoEMonitorAdapter hwQoeMonitorAdapter;
        synchronized (this.mLock) {
            hwQoeMonitorAdapter = this.mIHwQoEMonitorList.get(1);
        }
        if (hwQoeMonitorAdapter != null) {
            hwQoeMonitorAdapter.updateVoWiFiState(state);
        }
        return true;
    }

    public boolean notifyNetworkRoaming() {
        synchronized (this.mLock) {
            HwQoEUtils.logD(false, "HwQoEService NotifyNetworkRoaming", new Object[0]);
            this.mQoEHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_ROAMING);
        }
        return true;
    }

    private void initGameKogParams() {
        this.isGameKogInWar = false;
        this.isKOGInBackGround = false;
        this.isKOGInRateAjustMode = false;
        this.gameKOGUid = 0;
    }

    private void reportNetGameSense(int enable, int mode) {
        HwQoEUtils.logD(false, "HwQoEService: reportNetGameSense: enable=%{public}d mode=%{public}d", Integer.valueOf(enable), Integer.valueOf(mode));
        this.mHwQoEWiFiOptimization.hwQoELimitedSpeed(enable, mode);
    }

    private void setGameKogHighPriorityTransmit(int enable) {
        HwQoEUtils.logD(false, "HwQoEService: setGameKogHighPriorityTransmit", new Object[0]);
    }

    private static List<ActivityManager.RunningAppProcessInfo> getRunningProcesses(Context context) {
        if (context == null) {
            HwQoEUtils.logD(false, "HwQoEService: getRunningProcesses, input is null", new Object[0]);
            return null;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager != null) {
            return activityManager.getRunningAppProcesses();
        }
        HwQoEUtils.logD(false, "HwQoEService: getRunningProcesses, get ams service failed", new Object[0]);
        return null;
    }

    /* access modifiers changed from: private */
    public static boolean isKogInFg(Context context) {
        List<ActivityManager.RunningAppProcessInfo> processes = getRunningProcesses(context);
        if (processes == null) {
            HwQoEUtils.logD(false, "HwQoEService: isKogInFg failed", new Object[0]);
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.importance == 100) {
                HwQoEUtils.logD(false, "HwQoEService: isKogInFg %{public}s", processInfo.processName);
                if (GAME_KOG_PROCESSNAME.equals(processInfo.processName)) {
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

    private void initGameKogChr() {
        this.isSameKOGWar = false;
        this.gameRttNotifyCountInOneGame = 0;
        this.gameKOGChrLastReportTime = 0;
        this.gameKOGTcpRttDuration = 0;
        this.gameKOGTcpRttSegs = 0;
        this.gameKOGTcpRtt = 0;
        this.lastGameKOGTcpRttSegs = 0;
        this.lastGameKOGTcpRttDuration = 0;
    }

    private void exitGameKogWar() {
        HwQoEUtils.logD(false, "HwQoEService:exitGameKogWar", new Object[0]);
        this.isGameNeedDis = false;
        this.mDsKOGRTT = 0;
        this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
    }

    private void exitWifiChrStatistics() {
        if (this.isGameKogInWar) {
            this.mHwQoEGameCHRImpl.updateTencentTmgpGameStateChanged(0, 1);
        }
        if (this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT)) {
            this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT);
        }
    }

    private int getScene(int status, int scene) {
        if (this.lastScene == 0 && scene == 3) {
            this.lastScene = 3;
            return 1;
        } else if (this.lastScene == 3 && scene == 0) {
            this.lastScene = 0;
            return 2;
        } else if (scene == 3 && status == 3) {
            return 3;
        } else {
            if (scene == 3 && status == 4) {
                return 4;
            }
            if (status == 5) {
                return 5;
            }
            return 0;
        }
    }

    private void handleWifiKogGameStart() {
        HwQoEUtils.logD(false, "HwQoEService: KOG_GAME_START", new Object[0]);
        this.mQoEHandler.postDelayed(this.mCheckFGApp, 30000);
        initGameKogChr();
        if (!this.isGameKogInWar) {
            this.mForegroundAppUid = this.mHwQoEContentAware.getForegroundAppUid();
            if (!this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT)) {
                this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT, 8000);
            }
            this.mHwQoEGameCHRImpl.updateTencentTmgpGameStateChanged(1, 1);
            this.mWifiInfo = this.mWifiManager.getConnectionInfo();
            WifiInfo wifiInfo = this.mWifiInfo;
            if (wifiInfo != null) {
                this.mHwQoEGameCHRImpl.updateWifiFrequency(wifiInfo.is5GHz());
            }
            this.isGameKogInWar = true;
            HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
            if (hwWifiCHRService != null) {
                hwWifiCHRService.setGameKogScene(1);
            }
        }
    }

    private void handleWifiScene(int scene) {
        HwWifiCHRService hwWifiCHRService;
        if (scene != 1) {
            if (scene != 2) {
                if (scene == 3) {
                    HwQoEUtils.logD(false, "HwQoEService: KOG_GAME_FG_TO_BG", new Object[0]);
                    if (!this.isKOGInBackGround) {
                        this.isKOGInBackGround = true;
                        exitGameKogWar();
                    }
                    HwWifiCHRService hwWifiCHRService2 = this.mHwWifiCHRService;
                    if (hwWifiCHRService2 != null) {
                        hwWifiCHRService2.setGameKogScene(0);
                        return;
                    }
                    return;
                } else if (scene == 4) {
                    HwQoEUtils.logD(false, "HwQoEService: KOG_GAME_BG_TO_FG", new Object[0]);
                    if (this.isKOGInBackGround) {
                        this.isKOGInBackGround = false;
                        if (this.isGameKogInWar && (hwWifiCHRService = this.mHwWifiCHRService) != null) {
                            hwWifiCHRService.setGameKogScene(1);
                            return;
                        }
                        return;
                    }
                    return;
                } else if (scene != 5) {
                    return;
                }
            }
            HwQoEUtils.logD(false, "HwQoEService: KOG_GAME_EXIT or KOG_GAME_DIED", new Object[0]);
            this.mQoEHandler.removeCallbacks(this.mCheckFGApp);
            if (this.isGameKogInWar) {
                exitWifiChrStatistics();
                exitGameKogWar();
                initGameKogParams();
            }
            HwWifiCHRService hwWifiCHRService3 = this.mHwWifiCHRService;
            if (hwWifiCHRService3 != null) {
                hwWifiCHRService3.setGameKogScene(0);
                return;
            }
            return;
        }
        handleWifiKogGameStart();
    }

    private void handleCellKogGameStart() {
        if (!this.isGameKogInWar) {
            HwQoEUtils.logD(false, "upopti:KOG_GAME_START,enable highPri trans", new Object[0]);
            setGameKogHighPriorityTransmit(1);
            this.isGameKogInWar = true;
        }
        HwQoEUtils.logD(false, "HwQoEService: KOG_GAME_START, use cellular, enable bg rate limit", new Object[0]);
        this.mHwQoEGameCHRImpl.updateTencentTmgpGameStateChanged(1, 0);
        if (!this.isKOGInBgRateLimitMode) {
            this.isKOGInBgRateLimitMode = true;
        }
    }

    private void handleCellKogGameFgToBg() {
        if (this.isGameKogInWar) {
            HwQoEUtils.logD(false, "upopti:KOG_GAME_FG_TO_BG,disable highPri trans", new Object[0]);
            setGameKogHighPriorityTransmit(0);
        }
        HwQoEUtils.logD(false, "HwQoEService: KOG_GAME_FG_TO_BG, use cellular, disable bg rate limit", new Object[0]);
        if (!this.isKOGInBackGround) {
            this.isKOGInBackGround = true;
        }
    }

    private void handleCellKogGameBgToFg() {
        if (this.isGameKogInWar) {
            HwQoEUtils.logD(false, "upopti:KOG_GAME_BG_TO_FG,enable highPri trans", new Object[0]);
            setGameKogHighPriorityTransmit(1);
        }
        HwQoEUtils.logD(false, "HwQoEService: KOG_GAME_BG_TO_FG, use cellular, enable bg rate limit", new Object[0]);
        if (this.isKOGInBackGround) {
            this.isKOGInBackGround = false;
        }
    }

    private void handleCellKogGameExit() {
        if (this.isGameKogInWar) {
            HwQoEUtils.logD(false, "upopti:KOG_GAME_EXIT,disable highPri trans", new Object[0]);
            setGameKogHighPriorityTransmit(0);
        }
        HwQoEUtils.logD(false, "HwQoEService: KOG_GAME_EXIT or KOG_GAME_DIED, use cellular, disable bg rate limit", new Object[0]);
        this.mHwQoEGameCHRImpl.updateTencentTmgpGameStateChanged(0, 0);
        if (this.isKOGInBgRateLimitMode) {
            this.isKOGInBgRateLimitMode = false;
        }
        this.mDsKOGRTT = 0;
        initGameKogParams();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCellularScene(int scene) {
        if (scene != 1) {
            if (scene != 2) {
                if (scene == 3) {
                    handleCellKogGameFgToBg();
                    return;
                } else if (scene == 4) {
                    handleCellKogGameBgToFg();
                    return;
                } else if (scene != 5) {
                    return;
                }
            }
            handleCellKogGameExit();
            return;
        }
        handleCellKogGameStart();
    }

    public boolean updateLimitSpeedStatus(int mode, int reserve1, int reserve2) {
        boolean z = false;
        HwQoEUtils.logD(false, "HwQoEService: updateLimitSpeedStatus mode %{public}d reserve1 %{public}d reserve2 %{public}d isWifi() %{public}s", Integer.valueOf(mode), Integer.valueOf(reserve1), Integer.valueOf(reserve2), String.valueOf(isWifi()));
        int enable = mode == 0 ? 0 : 1;
        if (this.mHwWifiBoost == null) {
            return false;
        }
        if (isWifi()) {
            if (enable != 0) {
                z = true;
            }
            this.wifiHotLimitSpeedEnable = z;
            this.mHwWifiBoost.limitedSpeed(3, enable, mode);
        } else if (enable == 0) {
            this.mHwWifiBoost.limitedSpeed(3, enable, mode);
            this.wifiHotLimitSpeedEnable = false;
        }
        return true;
    }

    public boolean updateAppRunningStatus(int uid, int type, int status, int scene, int reserved) {
        HwQoEUtils.logD(false, "HwQoEService: updateAppRunningStatus uid %{public}d type %{public}d status %{public}d scene %{public}d", Integer.valueOf(uid), Integer.valueOf(type), Integer.valueOf(status), Integer.valueOf(scene));
        this.gameKOGUid = uid;
        int i = this.mCurNetworkType;
        if (i == 0) {
            handleCellularScene(getScene(status, scene));
        } else if (i == 1) {
            handleWifiScene(getScene(status, scene));
        }
        return true;
    }

    public boolean updateAppRunningStatus(int uid, int scene) {
        HwQoEUtils.logD(false, "HwQoEService: updateAppRunningStatus uid: %{public}d scene: %{public}d mCurNetworkType: %{public}d", Integer.valueOf(uid), Integer.valueOf(scene), Integer.valueOf(this.mCurNetworkType));
        this.gameKOGUid = uid;
        int i = this.mCurNetworkType;
        if (i == 0) {
            handleCellularScene(scene);
        } else if (i == 1) {
            handleWifiScene(scene);
        }
        return true;
    }

    private void gameTimeoutProcess(long rtt) {
        if (this.mQoEHandler.hasMessages(128)) {
            this.mQoEHandler.removeMessages(128);
        }
        if (this.mCurNetworkType == 0) {
            Message msg = this.mQoEHandler.obtainMessage();
            msg.what = 128;
            if (rtt < 460) {
                this.mQoEHandler.sendMessageDelayed(msg, GAME_RTT_NOTIFY_INTERVAL);
            } else {
                this.mQoEHandler.sendMessageDelayed(msg, 120000);
            }
        }
    }

    private void handleCellAppExperienceStatus(int uid, long rtt) {
        this.mHwQoEGameCHRImpl.updateMobileTencentTmgpGameRttChanged(rtt);
        gameTimeoutProcess(rtt);
        this.gameKOGTcpRttDuration = this.mHwWifiProServiceManager.getRttDuration(uid, 0);
        this.gameKOGTcpRttSegs = this.mHwWifiProServiceManager.getRttSegs(uid, 0);
        long j = this.gameKOGTcpRttSegs;
        if (j != 0) {
            this.gameKOGTcpRtt = this.gameKOGTcpRttDuration / j;
        } else {
            this.gameKOGTcpRtt = 0;
        }
        sendIntentDsKogRttRandomSampling(this.mContext, rtt, this.gameKOGTcpRtt);
        this.mHwQoEGameCHRImpl.updateTencentTmgpGameRttCounter(rtt);
    }

    private void handleWifiExperience460(int uid, long rtt) {
        long gameKOGCurrentTime = System.currentTimeMillis();
        HwQoEUtils.logD(false, "HwQoEService: gameKOGElapseTime:%{public}s", String.valueOf(gameKOGCurrentTime - this.gameKOGChrLastReportTime));
        if (this.gameRttNotifyCountInOneGame < 3 && gameKOGCurrentTime - this.gameKOGChrLastReportTime > GAME_RTT_NOTIFY_INTERVAL && this.isGameKogInWar) {
            this.gameKOGTcpRttDuration = this.mHwWifiProServiceManager.getRttDuration(uid, 1);
            this.gameKOGTcpRttSegs = this.mHwWifiProServiceManager.getRttSegs(uid, 1);
            long j = this.gameKOGTcpRttSegs;
            if (j != 0) {
                this.gameKOGTcpRtt = this.gameKOGTcpRttDuration / j;
            } else {
                this.gameKOGTcpRtt = 0;
            }
            HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
            if (hwWifiCHRService != null) {
                hwWifiCHRService.updateGameBoostLag(GAME_KOG_REASON_CODE[this.gameRttNotifyCountInOneGame], GAME_KOG_PROCESSNAME, (int) rtt, (int) this.gameKOGTcpRtt);
            }
            HwQoEUtils.logD(false, "HwQoEService: gameRttNotifyCountInOneGame:%{public}d gameKOGTcpRttDuration:%{public}s gameKOGTcpRttSegs:%{public}s gameKOGTcpRtt:%{public}s", Integer.valueOf(this.gameRttNotifyCountInOneGame), String.valueOf(this.gameKOGTcpRttDuration), String.valueOf(this.gameKOGTcpRttSegs), String.valueOf(this.gameKOGTcpRtt));
            this.gameKOGChrLastReportTime = gameKOGCurrentTime;
            this.gameRttNotifyCountInOneGame++;
        }
    }

    private boolean enableRateControl(long rtt, int rssiLevel) {
        if (this.isKOGInRateAjustMode || !this.isGameKogInWar || rtt != 460 || rssiLevel >= 2) {
            return false;
        }
        return true;
    }

    private boolean disabeRateControl(long rtt) {
        if (!this.isKOGInRateAjustMode || !this.isGameKogInWar || rtt >= 200) {
            return false;
        }
        return true;
    }

    private void handleWifiAppExperienceStatus(int uid, long rtt) {
        int rssiLevel = 2;
        if (rtt == 460) {
            handleWifiExperience460(uid, rtt);
        }
        if (rtt > 200) {
            HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
            if (hwWifiCHRService != null) {
                hwWifiCHRService.updateGameBoostStatic(GAME_KOG_PROCESSNAME, !this.isSameKOGWar);
            }
            if (!this.isSameKOGWar) {
                this.isSameKOGWar = true;
            }
        }
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager != null) {
            this.mWifiInfo = wifiManager.getConnectionInfo();
            if (this.mWifiInfo != null) {
                rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mWifiInfo.getFrequency(), this.mWifiInfo.getRssi());
            }
        }
        if (enableRateControl(rtt, rssiLevel)) {
            this.isKOGInRateAjustMode = true;
        }
        if (disabeRateControl(rtt)) {
            this.isKOGInRateAjustMode = false;
        }
        HwQoEUtils.logD(false, "HwQoEService: isGameKogInWar = %{public}s rtt = %{public}s rssiLevel = %{public}d", String.valueOf(this.isGameKogInWar), String.valueOf(rtt), Integer.valueOf(rssiLevel));
        if (rtt <= 200 || rssiLevel >= 2) {
            if (rtt > 200) {
                this.mDsKOGRTT = rtt;
            }
        } else if (!this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
            this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT, 3000);
        }
        if (rtt < 200) {
            this.mDsKOGRTT = 0;
            this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
        }
    }

    public boolean updateAppExperienceStatus(int uid, int experience, long rtt) {
        HwQoEUtils.logD(false, "HwQoEService: updateAppExperienceStatus uid:%{public}d experience:%{public}d rtt:%{public}s", Integer.valueOf(uid), Integer.valueOf(experience), String.valueOf(rtt));
        this.mHwQoEGameCHRImpl.updateTencentTmgpGameRttChanged(rtt);
        int i = this.mCurNetworkType;
        if (i == 0) {
            handleCellAppExperienceStatus(uid, rtt);
            return true;
        } else if (i != 1) {
            return false;
        } else {
            handleWifiAppExperienceStatus(uid, rtt);
            return true;
        }
    }

    private boolean isSsidAllowLowPower() {
        boolean isAndroidMobileAP = false;
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null || wifiManager.getConnectionInfo() == null) {
            return false;
        }
        String ssid = this.mWifiManager.getConnectionInfo().getSSID();
        boolean isMobileAP = this.mWifiManager.getConnectionInfo().getMeteredHint();
        HwWifiStateMachine hwWifiStateMachine = this.mWifiStateMachine;
        if (hwWifiStateMachine != null && (hwWifiStateMachine instanceof HwWifiStateMachine)) {
            isAndroidMobileAP = hwWifiStateMachine.isAndroidMobileAP();
        }
        if (ssid != null && ssid.equals("\"Huawei-Employee\"")) {
            return true;
        }
        if (!isMobileAP || !isAndroidMobileAP) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiConnectForQowAdapter(Message msg) {
        if (msg == null) {
            HwQoEUtils.logD(false, "handleWifiConnectForQowAdapter: msg is null", new Object[0]);
            return;
        }
        synchronized (this.mLock) {
            if (!this.isWiFiConnected) {
                this.mHwQoEContentAware.queryForegroundAppType();
                this.mCurNetworkType = 1;
            }
            this.isWiFiConnected = true;
            for (int i = 0; i < this.mIHwQoEMonitorList.size(); i++) {
                this.mIHwQoEMonitorList.valueAt(i).startMonitor();
            }
        }
        this.mHwQoEGameCHRImpl.handleWifiStateChanged(HwQoEUtils.QOE_MSG_WIFI_CONNECTED);
        if (msg.what == 109) {
            this.mHwQoEHilink.handleAccWifiStateChanged(HwQoEUtils.QOE_MSG_WIFI_CONNECTED);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiDisconnectForQowAdapter() {
        synchronized (this.mLock) {
            if (this.isWiFiConnected && this.isGameKogInWar) {
                this.mHwQoEGameCHRImpl.updateWiFiDisCounter();
            }
            this.mCurNetworkType = 0;
            this.mHwQoEGameCHRImpl.handleWifiStateChanged(HwQoEUtils.QOE_MSG_WIFI_DISCONNECT);
            this.isWiFiConnected = false;
            for (int i = 0; i < this.mIHwQoEMonitorList.size(); i++) {
                this.mIHwQoEMonitorList.valueAt(i).stopMonitor();
            }
        }
        HwQoEUtils.logD(false, "HwQoEService: WIFI is disconnected", new Object[0]);
        exitWifiChrStatistics();
        exitGameKogWar();
        initGameKogParams();
        this.mHwQoEHilink.handleAccWifiStateChanged(HwQoEUtils.QOE_MSG_WIFI_DISCONNECT);
        this.mDtim = 0;
        this.mBcnInterval = 0;
        if (this.wifiHotLimitSpeedEnable) {
            updateLimitSpeedStatus(0, 0, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiRssiChangedForQowAdapter() {
        this.mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (this.mWifiInfo == null) {
            HwQoEUtils.logD(false, "handleWifiRssiChangedForQowAdapter: mWifiInfo is null", new Object[0]);
            return;
        }
        int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mWifiInfo.getFrequency(), this.mWifiInfo.getRssi());
        if (this.mIsInGameState) {
            if (rssiLevel < 2) {
                if (!this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
                    this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT, 4000);
                }
            } else if (this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
                this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
            }
        } else if (this.mDsKOGRTT > 200 && rssiLevel < 2 && !this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
            this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT, 3000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiDisableForQowAdapter() {
        HwQoEUtils.logD(false, "HwQoEService: WIFI is disable,MAINLAND_REGION:%{public}s", String.valueOf(HwQoEUtils.MAINLAND_REGION));
        exitWifiChrStatistics();
        exitGameKogWar();
        initGameKogParams();
        this.mHwQoEHilink.handleAccWifiStateChanged(HwQoEUtils.QOE_MSG_WIFI_DISABLE);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiRoamingForQowAdapter() {
        HwQoEUtils.logD(false, "HwQoEService: WIFI is roaming", new Object[0]);
        if (this.isKOGInRateAjustMode) {
            this.isKOGInRateAjustMode = false;
        }
        if (this.isGameKogInWar) {
            this.mHwQoEGameCHRImpl.updateWifiRoaming();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBootCompleteForQowAdapter() {
        this.mHwQoEContentAware.onSystemBootCompled();
        if (HIDATA_ENABLED && HwArbitrationCallbackImpl.getInstanceForChr() != null) {
            HwArbitrationCallbackImpl.getInstanceForChr().regisGameCHR(mHwQoEService);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateUidTcpRttForQowAdapter() {
        this.gameKOGTcpRttDuration = this.mHwWifiProServiceManager.getRttDuration(this.mForegroundAppUid, this.mCurNetworkType);
        this.gameKOGTcpRttSegs = this.mHwWifiProServiceManager.getRttSegs(this.mForegroundAppUid, this.mCurNetworkType);
        long j = this.gameKOGTcpRttSegs;
        long j2 = this.lastGameKOGTcpRttSegs;
        if (j != j2) {
            long j3 = this.gameKOGTcpRttDuration;
            long j4 = this.lastGameKOGTcpRttDuration;
            this.gameKOGTcpRtt = (j3 - j4) / (j - j2);
            this.mHwQoEGameCHRImpl.updateWifiTcpRtt(j - j2, this.gameKOGTcpRtt, j3 - j4);
        }
        this.lastGameKOGTcpRttSegs = this.gameKOGTcpRttSegs;
        this.lastGameKOGTcpRttDuration = this.gameKOGTcpRttDuration;
        if (!this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT)) {
            this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT, 8000);
        }
    }

    private void initQoEAdapter() {
        HandlerThread handlerThread = new HandlerThread("initQoEAdapter monior Thread");
        handlerThread.start();
        this.mQoEHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.HwQoE.HwQoEService.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwQoEUtils.QOE_MSG_WIFI_DISABLE /* 108 */:
                        HwQoEService.this.handleWifiDisableForQowAdapter();
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_CONNECTED /* 109 */:
                    case HwQoEUtils.QOE_MSG_WIFI_INTERNET /* 111 */:
                        HwQoEService.this.handleWifiConnectForQowAdapter(msg);
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_DISCONNECT /* 110 */:
                        HwQoEService.this.handleWifiDisconnectForQowAdapter();
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_START_EVALUATE /* 113 */:
                        HwQoEUtils.logD(false, "WIFI is QOE_MSG_WIFI_START_EVALUATE, do nothing", new Object[0]);
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_ROAMING /* 115 */:
                        HwQoEService.this.handleWifiRoamingForQowAdapter();
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT /* 116 */:
                        HwQoEUtils.logD(false, "QOE_MSG_WIFI_DELAY_DISCONNECT, ignore it", new Object[0]);
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_RSSI_CHANGED /* 117 */:
                        HwQoEService.this.handleWifiRssiChangedForQowAdapter();
                        break;
                    case HwQoEUtils.QOE_MSG_BOOT_COMPLETED /* 119 */:
                        HwQoEService.this.handleBootCompleteForQowAdapter();
                        break;
                    case HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT /* 123 */:
                        HwQoEService.this.handleUpdateUidTcpRttForQowAdapter();
                        break;
                    case HwQoEUtils.QOE_MSG_SCAN_RESULTS /* 124 */:
                        if (HwQoEService.this.isGameKogInWar) {
                            HwQoEService.this.mHwQoEGameCHRImpl.updateWiFiScanCounter();
                            break;
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_BT_SCAN_STARTED /* 125 */:
                        if (HwQoEService.this.isGameKogInWar) {
                            HwQoEService.this.mHwQoEGameCHRImpl.updateBTScanCounter();
                            break;
                        }
                        break;
                    case 128:
                        HwQoEService.this.handleCellularScene(5);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void registerSubscriptionsChangedListeners() {
        if (this.mSubscriptionManager == null) {
            HwQoEUtils.logE(false, "registerSubscriptionsChangedListeners: mSubscriptionManager is null, return!", new Object[0]);
            return;
        }
        if (this.mSubscriptionListener == null) {
            this.mSubscriptionListener = new SubListener();
        }
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
    }

    /* access modifiers changed from: private */
    public class SubListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        private SubListener() {
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            if (HwQoEService.this.mPhoneStateListeners == null) {
                HwQoEUtils.logE(false, "onSubscriptionsChanged failed, mPhoneStateListeners is null", new Object[0]);
                return;
            }
            List<SubscriptionInfo> subInfos = HwQoEService.this.mSubscriptionManager.getActiveSubscriptionInfoList();
            List<Integer> subIdList = new ArrayList<>(HwQoEService.this.mPhoneStateListeners.keySet());
            for (int subIdCounter = subIdList.size() - 1; subIdCounter >= 0; subIdCounter--) {
                int subId = subIdList.get(subIdCounter).intValue();
                if (!HwQoEService.this.containSubId(subInfos, subId)) {
                    HwQoEUtils.logD(false, "onSubscriptionsChanged, remove deactive subId listener:%{public}d", Integer.valueOf(subId));
                    HwQoEService.this.mTelephonyManager.listen((PhoneStateListener) HwQoEService.this.mPhoneStateListeners.get(Integer.valueOf(subId)), 0);
                    HwQoEService.this.mPhoneStateListeners.remove(Integer.valueOf(subId));
                }
            }
            if (subInfos == null) {
                HwQoEUtils.logE(false, "onSubscriptionsChanged, subscriptions is null", new Object[0]);
                return;
            }
            int num = subInfos.size();
            HwQoEUtils.logD(false, "onSubscriptionsChanged num:" + num, new Object[0]);
            for (int i = 0; i < num; i++) {
                int subId2 = subInfos.get(i).getSubscriptionId();
                if (!HwQoEService.this.isActiveSubId(subId2)) {
                    HwQoEUtils.logE(false, "onSubscriptionsChanged failed, invalid subId:%{public}d, check next subId", Integer.valueOf(subId2));
                } else if (!HwQoEService.this.mPhoneStateListeners.containsKey(Integer.valueOf(subId2))) {
                    PhoneStateListener listener = HwQoEService.this.getPhoneStateListener(subId2);
                    HwQoEService.this.mTelephonyManager.listen(listener, 1);
                    HwQoEService.this.mPhoneStateListeners.put(Integer.valueOf(subId2), listener);
                }
            }
        }
    }

    private boolean isNsaState(int state) {
        if (2 > state || state > 5) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDataNetworkType(int subId, ServiceState ss) {
        int networkType = 0;
        if (this.mTelephonyManager != null && isValidSubId(subId)) {
            networkType = this.mTelephonyManager.getDataNetworkType(subId);
        }
        if (ss == null || !isNsaState(ss.getNsaState())) {
            return networkType;
        }
        return ss.getConfigRadioTechnology();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PhoneStateListener getPhoneStateListener(final int subId) {
        final int slotId = getSlotId(subId);
        return new PhoneStateListener(Integer.valueOf(subId)) {
            /* class com.android.server.wifi.HwQoE.HwQoEService.AnonymousClass3 */

            @Override // android.telephony.PhoneStateListener
            public void onServiceStateChanged(ServiceState state) {
                if (state == null) {
                    HwQoEUtils.logE(false, "onServiceStateChanged, ss is null, return", new Object[0]);
                } else if (!HwQoEService.this.isValidSubId(subId)) {
                    HwQoEUtils.logE(false, "onServiceStateChanged, invalid mSubId, return", new Object[0]);
                } else {
                    int default4GSlotId = HwTelephonyManager.getDefault().getDefault4GSlotId();
                    if (default4GSlotId == slotId && HwQoEService.this.mTelephonyManager != null) {
                        int dataRegTech = HwQoEService.this.getDataNetworkType(subId, state);
                        int nsaState = state.getNsaState();
                        if (HwQoEService.this.mCurNetworkType == 0) {
                            HwQoEService.this.mHwQoEGameCHRImpl.updateRegTypeAndTencentTmgpGameRttTimer(dataRegTech, nsaState);
                        }
                        HwQoEUtils.logD(false, "onServiceStateChanged, default4GSlotId=%{public}d, currentSlotId=%{public}d, dataregtech=%{public}d, nsaState=%{public}d", Integer.valueOf(default4GSlotId), Integer.valueOf(slotId), Integer.valueOf(dataRegTech), Integer.valueOf(nsaState));
                    }
                }
            }
        };
    }

    private int getSlotId(int subId) {
        if (!isValidSubId(subId)) {
            return -1;
        }
        return SubscriptionManagerEx.getSlotIndex(subId);
    }

    private boolean isBetaUserAndCust() {
        if (!KOG_CHR_REPORT_IN_ONE_MINUTE) {
            return false;
        }
        int userType = SystemProperties.getInt("ro.logsystem.usertype", 1);
        if (userType == 3 || userType == 5) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidSubId(int subId) {
        if (subId >= 0) {
            return true;
        }
        return false;
    }

    private boolean isValidSlotId(int slotId) {
        if (slotId < 0 || slotId > 2) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isActiveSubId(int subId) {
        if (isValidSubId(subId) && isValidSlotId(getSlotId(subId))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean containSubId(List<SubscriptionInfo> subInfos, int subId) {
        if (subInfos == null) {
            HwQoEUtils.logE(false, "containSubId fail, subInfos is null", new Object[0]);
            return false;
        } else if (!isValidSubId(subId)) {
            HwQoEUtils.logE(false, "containSubId fail, invalid subId:%{public}d", Integer.valueOf(subId));
            return false;
        } else {
            int size = subInfos.size();
            for (int i = 0; i < size; i++) {
                if (subInfos.get(i).getSubscriptionId() == subId) {
                    return true;
                }
            }
            return false;
        }
    }

    private void sendIntentDsKogRttRandomSampling(Context context, long rtt, long tcprtt) {
        int reportInterval = DS_KOG_REPORT_TIME_INTERVAL;
        Random random = new Random();
        if (isBetaUserAndCust()) {
            if (rtt >= 200) {
                reportInterval = DS_KOG_REPORT_TIME_INTERVAL_FOR_BETA;
            } else {
                return;
            }
        } else if (rtt > 400) {
            if (random.nextInt(3) != 0) {
                return;
            }
        } else if (rtt <= 200 || rtt > 400) {
            if (rtt >= 200) {
                HwQoEUtils.logD(false, "rtt is not in valid in sendIntentDsKogRttRandomSampling()", new Object[0]);
            } else if (random.nextInt(1000) != 0) {
                return;
            }
        } else if (random.nextInt(64) != 0) {
            return;
        }
        Date now = new Date();
        if (this.mLastRptRttBigLatencyTime == null || now.getTime() - this.mLastRptRttBigLatencyTime.getTime() > ((long) reportInterval)) {
            Intent chrIntent = new Intent(INTENT_DS_KOG_RTT_REPORT);
            Bundle extras = new Bundle();
            extras.putInt("ReportType", 1);
            extras.putInt("RTT", (int) rtt);
            extras.putInt("TCPRTT", (int) tcprtt);
            chrIntent.putExtras(extras);
            context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
            this.mLastRptRttBigLatencyTime = now;
        }
    }

    public void disconWiFiNetwork() {
        HwQoEUtils.logD(false, "HwQoEService: disconWiFiNetwork", new Object[0]);
        this.isGameNeedDis = true;
        this.mHwWifiProServiceManager.disconnectePoorWifi();
    }

    public boolean isInGameAndNeedDisc() {
        return this.isGameNeedDis;
    }

    public boolean isPermitUpdateWifiPowerMode(int mode) {
        if (!HwQoEUtils.MAINLAND_REGION) {
            return false;
        }
        if (mode != 7) {
            return true;
        }
        HwQoEUtils.logD(false, "isPermitWifiPowerMode mDtim:%{public}d, mBcnInterval: %{public}d", Integer.valueOf(this.mDtim), Integer.valueOf(this.mBcnInterval));
        if (isSsidAllowLowPower()) {
            return true;
        }
        return false;
    }

    public void updateVNPStateChanged(boolean isVpnConnected) {
        this.mHwQoEWiFiOptimization.updateVNPStateChanged(isVpnConnected);
    }

    public boolean isBgLimitAllowed(int uid) {
        return this.isGameKogInWar ? !this.mHwQoEContentAware.isLiveStreamApp(uid) : !isWifi() || !this.wifiHotLimitSpeedEnable || this.mHwQoEContentAware.isDownloadApp(uid);
    }

    @Override // com.android.server.wifi.HwQoE.IHwQoEContentAwareCallback
    public void onPeriodSpeed(long outSpeed, long inSpeed) {
    }

    @Override // com.android.server.wifi.HwQoE.IHwQoEContentAwareCallback
    public void onSensitiveAppStateChange(int uid, int state, boolean isBackground) {
    }

    @Override // com.android.server.wifi.HwQoE.IHwQoEContentAwareCallback
    public void onForegroundAppWifiSleepChange(boolean config, int sleeptime, int type, String appName) {
        HwQoEUtils.logD(false, "onForegroundAppWifiSleepChange: config:%{public}s, sleeptime:%{public}d,type: %{public}d, appname:%{public}s", String.valueOf(config), Integer.valueOf(sleeptime), Integer.valueOf(type), appName);
        if (config) {
            this.mHwMssArb.setGameForeground(true);
        } else if (this.mHwQoEContentAware.isGameType(type, appName)) {
            this.mHwMssArb.setGameForeground(true);
        } else {
            this.mHwMssArb.setGameForeground(false);
        }
    }

    @Override // com.android.server.wifi.HwQoE.IHwQoEContentAwareCallback
    public void onForegroundAppTypeChange(int type, String appName) {
        HwQoEUtils.logD(false, "APP type: %{public}d appName= %{public}s", Integer.valueOf(type), appName);
        if (this.mHwQoEContentAware.isGameType(type, appName)) {
            HwQoEUtils.logD(false, "APP_TYPE_GAME, isKOGInBackGround = %{public}s", String.valueOf(this.isKOGInBackGround));
            if (!GAME_KOG_PROCESSNAME.equals(appName)) {
                this.mIsInGameState = true;
                return;
            }
            this.mIsInGameState = false;
            this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
            return;
        }
        HwQoEUtils.logD(false, "no game, APP_TYPE_GAME, appName =%{public}s", appName);
        this.mIsInGameState = false;
        this.isGameNeedDis = false;
        this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
    }

    public void updateArpResult(boolean success, int arpRtt) {
        HwQoEUtils.logD(false, "updateArpResult, success =%{public}s, arprtt:%{public}d", String.valueOf(success), Integer.valueOf(arpRtt));
        this.mHwQoEGameCHRImpl.updateArpResult(success, arpRtt);
    }

    public void updateGameState(HwAPPStateInfo appStateInfo, int gamestate) {
        if (appStateInfo != null && appStateInfo.mAppId == 2001) {
            updateAppRunningStatus(appStateInfo.mAppUID, gamestate);
        }
    }

    public void updataGameExperience(HwAPPStateInfo appExperience) {
        if (appExperience != null && appExperience.mAppId == 2001) {
            updateAppExperienceStatus(appExperience.mAppUID, appExperience.mAppRTT, (long) appExperience.mAppRTT);
        }
    }

    public void updateHistreamExperience(HwHistreamCHRQoeInfo qoeInfo) {
        if (qoeInfo == null || this.mContext == null) {
            HwQoEUtils.logD(false, "qoeInfo or mContext is null", new Object[0]);
            return;
        }
        Intent chrIntent = new Intent(INTENT_HIDATA_DS_CHR_REPORT);
        Bundle extras = new Bundle();
        extras.putInt("sceneId", qoeInfo.mSceneId);
        extras.putInt("videoQoe", qoeInfo.mVideoQoe);
        extras.putInt("ulTup", qoeInfo.mUlTup);
        extras.putInt("dlTup", qoeInfo.mDlTup);
        extras.putInt("netDlTup", qoeInfo.mNetDlTup);
        chrIntent.putExtras(extras);
        this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
    }

    public void notifySelEngineStateStart() {
        HwQoEUtils.logD(false, "notifySelEngineStateStart", new Object[0]);
        notifyCallback(2);
    }

    public void notifySelEngineStateEnd(boolean success) {
        HwQoEUtils.logD(false, "notifySelEngineStateEnd success = %{public}s", String.valueOf(success));
        if (success) {
            notifyCallback(3);
        } else {
            notifyCallback(4);
        }
    }

    private void notifyCallback(int msg) {
        synchronized (this.mLock) {
            if (this.mIHwQoEMonitorList.size() > 0) {
                HwQoEMonitorAdapter adapter = this.mIHwQoEMonitorList.get(1);
                if (!(adapter == null || adapter.mCallback == null)) {
                    try {
                        adapter.mCallback.onNetworkStateChange(msg);
                    } catch (RemoteException e) {
                        HwQoEUtils.logE(false, "processCheckResult error %{public}s", e.getMessage());
                    }
                }
            }
        }
    }

    public void uploadHiDataDFTEvent(int type, Bundle bundle) {
        this.mHwWifiCHRService.uploadDFTEvent(type, bundle);
    }

    public void setWifiSlicing(int uid, int protocolType, int mode) {
        if (this.mHwWifiBoost == null) {
            HwQoEUtils.logE(false, "mHwWifiBoost is null", new Object[0]);
            return;
        }
        HwQoEUtils.logW(false, "HwQoEService: setWifiSlicing uid %{public}d, protocolType %{public}d,mode %{public}d, isWifi() %{public}s", Integer.valueOf(uid), Integer.valueOf(protocolType), Integer.valueOf(mode), String.valueOf(isWifi()));
        this.mHwWifiBoost.highPriorityTransmit(uid, protocolType, mode);
    }
}
