package com.android.server.wifi.HwQoE;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.HwQoE.HwQoEQualityInfo;
import android.net.wifi.HwQoE.IHwQoECallback;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.arbitration.HwArbitrationCallbackImpl;
import com.android.server.hidata.arbitration.HwArbitrationManager;
import com.android.server.hidata.arbitration.HwArbitrationUXWrapper;
import com.android.server.hidata.arbitration.IGameCHRCallback;
import com.android.server.hidata.arbitration.IHiDataCHRCallBack;
import com.android.server.hidata.hicure.HwHiCureArbitrationManager;
import com.android.server.hidata.hiradio.HwWifiBoost;
import com.android.server.hidata.histream.HwHistreamCHRQoeInfo;
import com.android.server.wifi.HwUidTcpMonitor;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.HwWifiConnectivityMonitor;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.MSS.HwMSSArbitrager;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.wifipro.WifiHandover;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HwQoEService implements IHwQoEContentAwareCallback, IGameCHRCallback, IHiDataCHRCallBack {
    public static final int BETA_USER = 3;
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    public static final int COMMERCIAL_USER = 1;
    private static final int DS_KOG_LATENCY_TIME_THRESHOLD = 400;
    private static final int DS_KOG_REPORT_THRESHOLD_0_200_SAMPLING_RATIO = 1000;
    private static final int DS_KOG_REPORT_THRESHOLD_200_400_SAMPLING_RATIO = 64;
    private static final int DS_KOG_REPORT_THRESHOLD_OVER_400_SAMPLING_RATIO = 3;
    private static final int DS_KOG_REPORT_TIME_INTERVAL = 180000;
    private static final int DS_KOG_REPORT_TIME_INTERVAL_FOR_BETA = 60000;
    private static final int DS_KOG_RTT_BIG_LATENCY_REPORT = 1;
    public static final int GAME_KOG_INWAR = 1;
    public static final int GAME_KOG_OFFWAR = 0;
    public static final String GAME_KOG_PROCESSNAME = "com.tencent.tmgp.sgame";
    public static final long GAME_RTT_NOTIFY_INTERVAL = 600000;
    private static final boolean HiCureEnabled = SystemProperties.getBoolean("ro.config.hw_hicure.enabled", true);
    /* access modifiers changed from: private */
    public static final boolean HiDataEnabled = SystemProperties.getBoolean("ro.config.hw_hidata.enabled", true);
    private static final String INTENT_DS_KOG_RTT_REPORT = "com.android.intent.action.kog_rtt_report";
    private static final String INTENT_HIDATA_DS_CHR_REPORT = "com.android.intent.action.hidata_ds_report";
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
    public static final int OVER_SEA_BETA_USER = 5;
    public static final int SEL_ENGINE_END_FAILED = 4;
    public static final int SEL_ENGINE_END_SUCCESS = 3;
    public static final int SEL_ENGINE_START = 2;
    private static final int TIMEOUT_GAME_TIMEOUT_MONITOR = 120000;
    private static final int TIMEOUT_UPDATE_TIMEOUT_MONITOR = 600000;
    public static final int TYPE_VOWIFI = 1;
    private static final String[] gameKOGReasonCode = {"GAME_LAG_1", "GAME_LAG_2", "GAME_LAG_3", "GAME_LAG_4", "GAME_LAG_5", "GAME_LAG_6", "GAME_LAG_7", "GAME_LAG_8", "GAME_LAG_9", "GAME_LAG_10"};
    /* access modifiers changed from: private */
    public static HwQoEService mHwQoEService = null;
    private static final boolean mKogChrReportInOneMinute = SystemProperties.getBoolean("ro.config.kog_chr_one_minute", false);
    private static final int userType = SystemProperties.getInt("ro.logsystem.usertype", 1);
    /* access modifiers changed from: private */
    public boolean WifiHotLimitSpeedEnable = false;
    public long gameKOGChrLastReportTime = 0;
    public long gameKOGTcpRtt = 0;
    public long gameKOGTcpRttDuration = 0;
    public long gameKOGTcpRttSegs = 0;
    private int gameKOGUid = 0;
    public int gameRttNotifyCountInOneGame = 0;
    /* access modifiers changed from: private */
    public boolean isGameKOGInWar = false;
    private boolean isGameNeedDis = false;
    /* access modifiers changed from: private */
    public boolean isKOGInBackGround = false;
    private boolean isKOGInBgRateLimitMode = false;
    /* access modifiers changed from: private */
    public boolean isKOGInRateAjustMode = false;
    public boolean isSameKOGWar = false;
    /* access modifiers changed from: private */
    public boolean isWiFiConnected = false;
    public long lastGameKOGTcpRttDuration = 0;
    public long lastGameKOGTcpRttSegs = 0;
    private int lastScene = 0;
    /* access modifiers changed from: private */
    public int mBcnInterval;
    Runnable mCheckFGApp = new Runnable() {
        public void run() {
            if (true != HwQoEService.this.isKOGInRateAjustMode || HwQoEService.isKOGInFg(HwQoEService.this.mContext)) {
                if (true == HwQoEService.this.isGameKOGInWar && true == HwQoEService.isKOGInFg(HwQoEService.this.mContext)) {
                    HwQoEService.this.setGameKOGMode(4);
                }
                HwQoEService.this.mQoEHandler.postDelayed(this, 30000);
                return;
            }
            HwQoEService.this.setGameKOGMode(0);
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mCurNetworkType;
    /* access modifiers changed from: private */
    public long mDsKOGRTT = 0;
    /* access modifiers changed from: private */
    public int mDtim;
    /* access modifiers changed from: private */
    public ArrayList<IHwQoECallback> mEvaluateCallbackList = new ArrayList<>();
    /* access modifiers changed from: private */
    public int mForegroundAppUid;
    /* access modifiers changed from: private */
    public HwUidTcpMonitor mGameKOGTcpMonitor;
    private HwMSSArbitrager mHwMssArb = null;
    /* access modifiers changed from: private */
    public HwQoEContentAware mHwQoEContentAware;
    /* access modifiers changed from: private */
    public HwQoEGameCHRImpl mHwQoEGameCHRImpl;
    /* access modifiers changed from: private */
    public HwQoEHilink mHwQoEHilink = null;
    private HwQoEWiFiOptimization mHwQoEWiFiOptimization;
    private HwWifiBoost mHwWifiBoost;
    private HwWifiCHRService mHwWifiCHRService;
    /* access modifiers changed from: private */
    public ArrayMap<Integer, HwQoEMonitorAdapter> mIHwQoEMonitorList = new ArrayMap<>();
    /* access modifiers changed from: private */
    public boolean mIsInGameState = false;
    private Date mLastRptRttBigLatencyTime = null;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    private int mPreviousUid = 0;
    /* access modifiers changed from: private */
    public Handler mQoEHandler;
    /* access modifiers changed from: private */
    public WifiInfo mWifiInfo;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public WifiStateMachine mWifiStateMachine;

    private HwQoEService(Context context, WifiStateMachine wifiStateMachine) {
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
        this.mGameKOGTcpMonitor = HwUidTcpMonitor.getInstance(this.mContext);
        if (HiDataEnabled) {
            HwArbitrationManager.createInstance(context, this);
            HwQoEUtils.logE("HiDATA Arbitration has started in HwQoEService");
        }
        if (HiCureEnabled) {
            HwHiCureArbitrationManager.createInstance(context);
        }
        this.mHwQoEHilink = HwQoEHilink.getInstance(this.mContext);
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

    public void updateWifiConnectionMode(boolean isUserManualConnect, boolean isUserHandoverWiFi) {
    }

    public void updateWifiTimParam(int dtim, int bcnInterval) {
        HwQoEUtils.logD("updateWifiTimParam dtim = " + dtim + " bcnInterval = " + bcnInterval);
        this.mDtim = dtim;
        this.mBcnInterval = bcnInterval;
    }

    public void updateWifiSleepWhiteList(int type, List<String> packageWhiteList) {
        this.mHwQoEContentAware.updateWifiSleepWhiteList(type, packageWhiteList);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0063, code lost:
        return true;
     */
    public boolean registerHwQoEMonitor(int type, int period, IHwQoECallback callback) {
        synchronized (this.mLock) {
            HwQoEUtils.logE("registerHwQoEMonitor period = " + period + " type = " + type);
            if (callback == null) {
                HwQoEUtils.logE("registerHwQoEMonitor callbakc error ");
                return false;
            }
            HwQoEMonitorAdapter rd = this.mIHwQoEMonitorList.get(Integer.valueOf(type));
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
            HwQoEMonitorAdapter rd = this.mIHwQoEMonitorList.get(Integer.valueOf(type));
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
                try {
                    HwQoEUtils.logD("evaluateNetworkQuality");
                    this.mEvaluateCallbackList.add(callback);
                    this.mQoEHandler.sendEmptyMessage(HwQoEUtils.QOE_MSG_WIFI_START_EVALUATE);
                    return true;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                HwQoEUtils.logD("evaluateNetworkQuality callback == null");
                return false;
            }
        }
    }

    public boolean updateVOWIFIState(int state) {
        HwQoEMonitorAdapter rd = this.mIHwQoEMonitorList.get(Integer.valueOf(HwQoEQualityInfo.HWQOE_MONITOR_TYPE_VOWIFI));
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

    /* access modifiers changed from: private */
    public void initGameKOGParams() {
        this.isGameKOGInWar = false;
        this.isKOGInBackGround = false;
        this.isKOGInRateAjustMode = false;
        this.gameKOGUid = 0;
    }

    private void reportNetGameSense(int enable, int mode) {
        HwQoEUtils.logD("HwQoEService: reportNetGameSense: enable=" + enable + " mode=" + mode);
        this.mHwQoEWiFiOptimization.hwQoELimitedSpeed(enable, mode);
    }

    private void setGameKOGHighPriorityTransmit(int enable) {
        HwQoEUtils.logD("HwQoEService: setGameKOGHighPriorityTransmit");
    }

    /* access modifiers changed from: private */
    public void setGameKOGMode(int mode) {
        if ((6 != mode && 7 != mode) || isPermitUpdateWifiPowerMode(mode)) {
        }
    }

    private void setTXPower(int enable) {
        HwQoEUtils.logD("HwQoEService: setTXPower");
    }

    private static List<ActivityManager.RunningAppProcessInfo> getRunningProcesses(Context context) {
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

    /* access modifiers changed from: private */
    public static boolean isKOGInFg(Context context) {
        List<ActivityManager.RunningAppProcessInfo> processes = getRunningProcesses(context);
        if (processes == null) {
            HwQoEUtils.logD("HwQoEService: isKOGInFg failed");
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.importance == 100) {
                HwQoEUtils.logD("HwQoEService: isKOGInFg " + processInfo.processName);
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

    private void initGameKOGChr() {
        this.isSameKOGWar = false;
        this.gameRttNotifyCountInOneGame = 0;
        this.gameKOGChrLastReportTime = 0;
        this.gameKOGTcpRttDuration = 0;
        this.gameKOGTcpRttSegs = 0;
        this.gameKOGTcpRtt = 0;
        this.lastGameKOGTcpRttSegs = 0;
        this.lastGameKOGTcpRttDuration = 0;
    }

    /* access modifiers changed from: private */
    public void exitGameKOGWar() {
        HwQoEUtils.logD("HwQoEService:exitGameKOGWar");
        if (true == this.isKOGInRateAjustMode) {
            setGameKOGMode(0);
        }
        if (true == this.isGameKOGInWar) {
            setGameKOGMode(3);
            setTXPower(0);
        }
        this.isGameNeedDis = false;
        this.mDsKOGRTT = 0;
        this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
    }

    /* access modifiers changed from: private */
    public void exitWifiChrStatistics() {
        if (true == this.isGameKOGInWar) {
            this.mHwQoEGameCHRImpl.updateTencentTmgpGameStateChanged(0, 1);
        }
        if (this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT)) {
            this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT);
        }
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
            if (5 == status) {
                return 5;
            }
            return 0;
        }
    }

    private void handleWifiScene(int scene) {
        switch (scene) {
            case 1:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_START");
                this.mQoEHandler.postDelayed(this.mCheckFGApp, 30000);
                initGameKOGChr();
                if (!this.isGameKOGInWar) {
                    this.mForegroundAppUid = this.mHwQoEContentAware.getForegroundAppUid();
                    if (!this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT)) {
                        this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT, 8000);
                    }
                    this.mHwQoEGameCHRImpl.updateTencentTmgpGameStateChanged(1, 1);
                    this.mWifiInfo = this.mWifiManager.getConnectionInfo();
                    if (this.mWifiInfo != null) {
                        this.mHwQoEGameCHRImpl.updateWifiFrequency(this.mWifiInfo.is5GHz());
                    }
                    this.isGameKOGInWar = true;
                    setGameKOGMode(5);
                    setGameKOGMode(4);
                    setTXPower(1);
                    if (this.mHwWifiCHRService != null) {
                        this.mHwWifiCHRService.setGameKogScene(1);
                        return;
                    }
                    return;
                }
                return;
            case 2:
            case 5:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_EXIT or KOG_GAME_DIED");
                this.mQoEHandler.removeCallbacks(this.mCheckFGApp);
                if (true == this.isGameKOGInWar) {
                    exitWifiChrStatistics();
                    exitGameKOGWar();
                    initGameKOGParams();
                }
                if (this.mHwWifiCHRService != null) {
                    this.mHwWifiCHRService.setGameKogScene(0);
                    return;
                }
                return;
            case 3:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_FG_TO_BG");
                if (!this.isKOGInBackGround) {
                    this.isKOGInBackGround = true;
                    exitGameKOGWar();
                }
                if (this.mHwWifiCHRService != null) {
                    this.mHwWifiCHRService.setGameKogScene(0);
                    return;
                }
                return;
            case 4:
                HwQoEUtils.logD("HwQoEService: KOG_GAME_BG_TO_FG");
                if (true == this.isKOGInBackGround) {
                    this.isKOGInBackGround = false;
                    if (true == this.isKOGInRateAjustMode) {
                        setGameKOGMode(1);
                    }
                    if (true == this.isGameKOGInWar) {
                        setGameKOGMode(4);
                        setTXPower(1);
                        if (this.mHwWifiCHRService != null) {
                            this.mHwWifiCHRService.setGameKogScene(1);
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

    /* access modifiers changed from: private */
    public void handleCellularScene(int scene) {
        switch (scene) {
            case 1:
                if (!this.isGameKOGInWar) {
                    HwQoEUtils.logD("upopti:KOG_GAME_START,enable highPri trans");
                    setGameKOGHighPriorityTransmit(1);
                    this.isGameKOGInWar = true;
                }
                HwQoEUtils.logD("HwQoEService: KOG_GAME_START, use cellular, enable bg rate limit");
                this.mHwQoEGameCHRImpl.updateTencentTmgpGameStateChanged(1, 0);
                if (!this.isKOGInBgRateLimitMode) {
                    reportNetGameSense(1, 7);
                    this.isKOGInBgRateLimitMode = true;
                    return;
                }
                return;
            case 2:
            case 5:
                if (true == this.isGameKOGInWar) {
                    HwQoEUtils.logD("upopti:KOG_GAME_EXIT,disable highPri trans");
                    setGameKOGHighPriorityTransmit(0);
                }
                HwQoEUtils.logD("HwQoEService: KOG_GAME_EXIT or KOG_GAME_DIED, use cellular, disable bg rate limit");
                this.mHwQoEGameCHRImpl.updateTencentTmgpGameStateChanged(0, 0);
                if (true == this.isKOGInBgRateLimitMode) {
                    reportNetGameSense(0, 0);
                    this.isKOGInBgRateLimitMode = false;
                }
                this.mDsKOGRTT = 0;
                initGameKOGParams();
                if (this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_GAME_TIMEOUT)) {
                    this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_GAME_TIMEOUT);
                    return;
                }
                return;
            case 3:
                if (true == this.isGameKOGInWar) {
                    HwQoEUtils.logD("upopti:KOG_GAME_FG_TO_BG,disable highPri trans");
                    setGameKOGHighPriorityTransmit(0);
                }
                HwQoEUtils.logD("HwQoEService: KOG_GAME_FG_TO_BG, use cellular, disable bg rate limit");
                if (!this.isKOGInBackGround) {
                    this.isKOGInBackGround = true;
                    reportNetGameSense(0, 0);
                    return;
                }
                return;
            case 4:
                if (true == this.isGameKOGInWar) {
                    HwQoEUtils.logD("upopti:KOG_GAME_BG_TO_FG,enable highPri trans");
                    setGameKOGHighPriorityTransmit(1);
                }
                HwQoEUtils.logD("HwQoEService: KOG_GAME_BG_TO_FG, use cellular, enable bg rate limit");
                if (true == this.isKOGInBackGround) {
                    this.isKOGInBackGround = false;
                    reportNetGameSense(1, 7);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public boolean updatelimitSpeedStatus(int mode, int reserve1, int reserve2) {
        HwQoEUtils.logD("HwQoEService: updatelimitSpeedStatus mode " + mode + " reserve1 " + reserve1 + " reserve2 " + reserve2 + " isWifi()" + isWifi());
        boolean z = false;
        int enable = mode == 0 ? 0 : 1;
        if (this.mHwWifiBoost == null) {
            return false;
        }
        if (isWifi()) {
            if (enable != 0) {
                z = true;
            }
            this.WifiHotLimitSpeedEnable = z;
            this.mHwWifiBoost.limitedSpeed(3, enable, mode);
        } else if (enable == 0) {
            this.mHwWifiBoost.limitedSpeed(3, enable, mode);
            this.WifiHotLimitSpeedEnable = false;
        }
        return true;
    }

    public List<String> getSupportList() {
        HwQoEUtils.logD("HwQoeService: getSupportList");
        HwArbitrationUXWrapper hwArbitrationUXWrapper = HwArbitrationUXWrapper.getInstance();
        if (hwArbitrationUXWrapper != null) {
            return hwArbitrationUXWrapper.getSupportList();
        }
        return null;
    }

    public void notifyUIEvent(int event) {
        HwQoEUtils.logD("HwQoeService: notifyUIEvent");
        HwArbitrationUXWrapper hwArbitrationUXWrapper = HwArbitrationUXWrapper.getInstance();
        if (hwArbitrationUXWrapper != null) {
            hwArbitrationUXWrapper.notifyUIEvent(event);
        }
    }

    public boolean updateAppRunningStatus(int uid, int type, int status, int scene, int reserved) {
        HwQoEUtils.logD("HwQoEService: updateAppRunningStatus uid " + uid + " type " + type + " status " + status + " scene " + scene);
        this.gameKOGUid = uid;
        if (this.mCurNetworkType == 0) {
            handleCellularScene(getScene(status, scene));
        } else if (1 == this.mCurNetworkType) {
            handleWifiScene(getScene(status, scene));
        }
        return true;
    }

    public boolean updateAppRunningStatus(int uid, int scene) {
        HwQoEUtils.logD("HwQoEService: updateAppRunningStatus uid: " + uid + " scene: " + scene + " mCurNetworkType: " + this.mCurNetworkType);
        this.gameKOGUid = uid;
        if (this.mCurNetworkType == 0) {
            handleCellularScene(scene);
        } else if (1 == this.mCurNetworkType) {
            handleWifiScene(scene);
        }
        return true;
    }

    private void gameTimeoutProcess(long rtt) {
        if (this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_GAME_TIMEOUT)) {
            this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_GAME_TIMEOUT);
        }
        if (this.mCurNetworkType == 0 && this.isGameKOGInWar) {
            Message msg = this.mQoEHandler.obtainMessage();
            msg.what = HwQoEUtils.QOE_MSG_GAME_TIMEOUT;
            if (rtt < 460) {
                this.mQoEHandler.sendMessageDelayed(msg, GAME_RTT_NOTIFY_INTERVAL);
            } else {
                this.mQoEHandler.sendMessageDelayed(msg, 120000);
            }
        }
    }

    public boolean updateAppExperienceStatus(int uid, int experience, long rtt) {
        boolean z;
        boolean z2;
        int i = uid;
        long j = rtt;
        long gameKOGCurrentTime = System.currentTimeMillis();
        HwQoEUtils.logD("HwQoEService: updateAppExperienceStatus uid:" + i + " experience:" + experience + " rtt:" + j);
        int rssiLevel = 2;
        this.mHwQoEGameCHRImpl.updateTencentTmgpGameRttChanged(j);
        gameTimeoutProcess(j);
        if (this.mCurNetworkType == 0) {
            HwUidTcpMonitor gameKOGTcpMonitor = HwUidTcpMonitor.getInstance(this.mContext);
            this.gameKOGTcpRttDuration = gameKOGTcpMonitor.getRttDuration(i, 0);
            this.gameKOGTcpRttSegs = gameKOGTcpMonitor.getRttSegs(i, 0);
            if (0 != this.gameKOGTcpRttSegs) {
                this.gameKOGTcpRtt = this.gameKOGTcpRttDuration / this.gameKOGTcpRttSegs;
            } else {
                this.gameKOGTcpRtt = 0;
            }
            sendIntentDsKOGRttRandomSampling(this.mContext, j, this.gameKOGTcpRtt);
            this.mHwQoEGameCHRImpl.updateTencentTmgpGameRttCounter(j);
            return true;
        } else if (1 != this.mCurNetworkType) {
            return false;
        } else {
            if (j == 460) {
                HwQoEUtils.logD("HwQoEService: gameKOGElapseTime:" + (gameKOGCurrentTime - this.gameKOGChrLastReportTime));
                if (this.gameRttNotifyCountInOneGame < 3 && gameKOGCurrentTime - this.gameKOGChrLastReportTime > GAME_RTT_NOTIFY_INTERVAL && true == this.isGameKOGInWar) {
                    HwUidTcpMonitor gameKOGTcpMonitor2 = HwUidTcpMonitor.getInstance(this.mContext);
                    this.gameKOGTcpRttDuration = gameKOGTcpMonitor2.getRttDuration(i, 1);
                    this.gameKOGTcpRttSegs = gameKOGTcpMonitor2.getRttSegs(i, 1);
                    if (0 != this.gameKOGTcpRttSegs) {
                        this.gameKOGTcpRtt = this.gameKOGTcpRttDuration / this.gameKOGTcpRttSegs;
                    } else {
                        this.gameKOGTcpRtt = 0;
                    }
                    if (this.mHwWifiCHRService != null) {
                        this.mHwWifiCHRService.updateGameBoostLag(gameKOGReasonCode[this.gameRttNotifyCountInOneGame], GAME_KOG_PROCESSNAME, (int) j, (int) this.gameKOGTcpRtt);
                    }
                    HwQoEUtils.logD("HwQoEService: gameRttNotifyCountInOneGame:" + this.gameRttNotifyCountInOneGame);
                    HwQoEUtils.logD("HwQoEService: gameKOGTcpRttDuration:" + this.gameKOGTcpRttDuration + " gameKOGTcpRttSegs:" + this.gameKOGTcpRttSegs + " gameKOGTcpRtt:" + this.gameKOGTcpRtt);
                    this.gameKOGChrLastReportTime = gameKOGCurrentTime;
                    this.gameRttNotifyCountInOneGame = this.gameRttNotifyCountInOneGame + 1;
                }
            }
            if (j > 200) {
                if (this.mHwWifiCHRService != null) {
                    z2 = true;
                    this.mHwWifiCHRService.updateGameBoostStatic(GAME_KOG_PROCESSNAME, !this.isSameKOGWar);
                } else {
                    z2 = true;
                }
                if (!this.isSameKOGWar) {
                    this.isSameKOGWar = z2;
                }
            }
            if (this.mWifiManager != null) {
                this.mWifiInfo = this.mWifiManager.getConnectionInfo();
                if (this.mWifiInfo != null) {
                    rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mWifiInfo.getFrequency(), this.mWifiInfo.getRssi());
                }
            }
            if (!this.isKOGInRateAjustMode) {
                z = true;
                if (true == this.isGameKOGInWar && j == 460 && rssiLevel < 2) {
                    setGameKOGMode(1);
                    this.isKOGInRateAjustMode = true;
                }
            } else {
                z = true;
            }
            if (z == this.isKOGInRateAjustMode && z == this.isGameKOGInWar && j < 200) {
                setGameKOGMode(0);
                this.isKOGInRateAjustMode = false;
            }
            HwQoEUtils.logD("HwQoEService: isGameKOGInWar  = " + this.isGameKOGInWar + " rtt = " + j + " rssiLevel = " + rssiLevel);
            if (j <= 200 || rssiLevel >= 2) {
                if (j > 200) {
                    this.mDsKOGRTT = j;
                }
            } else if (!this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
                this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT, 3000);
            }
            if (j < 200) {
                this.mDsKOGRTT = 0;
                this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void enableLowPowerMode() {
        if (isSsidAllowLowPower()) {
            setGameKOGMode(7);
        }
    }

    private boolean isSsidAllowLowPower() {
        boolean isAndroidMobileAP = false;
        if (!(this.mWifiManager == null || this.mWifiManager.getConnectionInfo() == null)) {
            String ssid = this.mWifiManager.getConnectionInfo().getSSID();
            boolean isMobileAP = this.mWifiManager.getConnectionInfo().getMeteredHint();
            if (this.mWifiStateMachine != null && (this.mWifiStateMachine instanceof HwWifiStateMachine)) {
                isAndroidMobileAP = this.mWifiStateMachine.isAndroidMobileAP();
            }
            if ((ssid != null && ssid.equals("\"Huawei-Employee\"")) || (isMobileAP && isAndroidMobileAP)) {
                return true;
            }
        }
        return false;
    }

    private void initQoEAdapter() {
        HandlerThread handlerThread = new HandlerThread("initQoEAdapter monior Thread");
        handlerThread.start();
        this.mQoEHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                int i = 0;
                switch (msg.what) {
                    case HwQoEUtils.QOE_MSG_WIFI_DISABLE:
                        HwQoEUtils.logD("HwQoEService: WIFI is disable,MAINLAND_REGION:" + HwQoEUtils.MAINLAND_REGION);
                        HwQoEService.this.exitWifiChrStatistics();
                        HwQoEService.this.exitGameKOGWar();
                        HwQoEService.this.initGameKOGParams();
                        HwQoEService.this.mHwQoEHilink.handleAccWifiStateChanged(HwQoEUtils.QOE_MSG_WIFI_DISABLE);
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_CONNECTED:
                    case HwQoEUtils.QOE_MSG_WIFI_INTERNET:
                        synchronized (HwQoEService.this.mLock) {
                            if (!HwQoEService.this.isWiFiConnected) {
                                HwQoEService.this.mHwQoEContentAware.queryForegroundAppType();
                                int unused = HwQoEService.this.mCurNetworkType = 1;
                            }
                            boolean unused2 = HwQoEService.this.isWiFiConnected = true;
                            while (true) {
                                int i2 = i;
                                if (i2 < HwQoEService.this.mIHwQoEMonitorList.size()) {
                                    ((HwQoEMonitorAdapter) HwQoEService.this.mIHwQoEMonitorList.valueAt(i2)).startMonitor();
                                    i = i2 + 1;
                                }
                            }
                        }
                        if (msg.what == 109) {
                            HwQoEService.this.mHwQoEHilink.handleAccWifiStateChanged(HwQoEUtils.QOE_MSG_WIFI_CONNECTED);
                            break;
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_DISCONNECT:
                        if (HwQoEService.this.isWiFiConnected && HwQoEService.this.isGameKOGInWar) {
                            HwQoEService.this.mHwQoEGameCHRImpl.updateWiFiDisCounter();
                        }
                        int unused3 = HwQoEService.this.mCurNetworkType = 0;
                        synchronized (HwQoEService.this.mLock) {
                            boolean unused4 = HwQoEService.this.isWiFiConnected = false;
                            for (int i3 = 0; i3 < HwQoEService.this.mIHwQoEMonitorList.size(); i3++) {
                                ((HwQoEMonitorAdapter) HwQoEService.this.mIHwQoEMonitorList.valueAt(i3)).stopMonitor();
                            }
                        }
                        HwQoEUtils.logD("HwQoEService: WIFI is disconnected");
                        HwQoEService.this.exitWifiChrStatistics();
                        HwQoEService.this.exitGameKOGWar();
                        HwQoEService.this.initGameKOGParams();
                        HwQoEService.this.mHwQoEHilink.handleAccWifiStateChanged(HwQoEUtils.QOE_MSG_WIFI_DISCONNECT);
                        int unused5 = HwQoEService.this.mDtim = 0;
                        int unused6 = HwQoEService.this.mBcnInterval = 0;
                        if (HwQoEService.this.WifiHotLimitSpeedEnable) {
                            HwQoEService.this.updatelimitSpeedStatus(0, 0, 0);
                            break;
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_START_EVALUATE:
                        HwQoEUtils.logD("WIFI is QOE_MSG_WIFI_START_EVALUATE");
                        synchronized (HwQoEService.this.mLock) {
                            HwQoEEvaluateAdapter mHwQoEEvaluateAdapter = HwQoEEvaluateAdapter.getInstance(HwQoEService.this.mContext, HwQoEService.this.mWifiStateMachine);
                            if (mHwQoEEvaluateAdapter != null) {
                                Iterator it = HwQoEService.this.mEvaluateCallbackList.iterator();
                                while (it.hasNext()) {
                                    mHwQoEEvaluateAdapter.evaluateNetworkQuality((IHwQoECallback) it.next());
                                }
                                HwQoEService.this.mEvaluateCallbackList.clear();
                            }
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_ROAMING:
                        HwQoEUtils.logD("HwQoEService: WIFI is roaming");
                        if (true == HwQoEService.this.isKOGInRateAjustMode) {
                            HwQoEService.this.setGameKOGMode(0);
                            boolean unused7 = HwQoEService.this.isKOGInRateAjustMode = false;
                        }
                        if (!HwQoEService.this.isKOGInBackGround && HwQoEService.this.isGameKOGInWar) {
                            HwQoEService.this.setGameKOGMode(4);
                        }
                        if (HwQoEService.this.isGameKOGInWar) {
                            HwQoEService.this.mHwQoEGameCHRImpl.updateWifiRoaming();
                            break;
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT:
                        HwQoEUtils.logD("HwQoEService: QOE_MSG_WIFI_DELAY_DISCONNECT");
                        WifiInfo unused8 = HwQoEService.this.mWifiInfo = HwQoEService.this.mWifiManager.getConnectionInfo();
                        if (HwQoEService.this.mWifiInfo != null) {
                            int disRssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(HwQoEService.this.mWifiInfo.getFrequency(), HwQoEService.this.mWifiInfo.getRssi());
                            break;
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_WIFI_RSSI_CHANGED:
                        WifiInfo unused9 = HwQoEService.this.mWifiInfo = HwQoEService.this.mWifiManager.getConnectionInfo();
                        if (HwQoEService.this.mWifiInfo != null) {
                            int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(HwQoEService.this.mWifiInfo.getFrequency(), HwQoEService.this.mWifiInfo.getRssi());
                            if (!HwQoEService.this.mIsInGameState) {
                                if (HwQoEService.this.mDsKOGRTT > 200 && rssiLevel < 2 && !HwQoEService.this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
                                    HwQoEService.this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT, 3000);
                                    break;
                                }
                            } else if (rssiLevel >= 2) {
                                if (HwQoEService.this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
                                    HwQoEService.this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
                                    break;
                                }
                            } else if (!HwQoEService.this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT)) {
                                HwQoEService.this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT);
                                break;
                            }
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_BOOT_COMPLETED:
                        HwQoEService.this.mHwQoEContentAware.systemBootCompled();
                        HiDataUtilsManager.getInstance(HwQoEService.this.mContext).registerListener();
                        if (HwQoEService.HiDataEnabled && HwArbitrationCallbackImpl.getInstanceForChr() != null) {
                            HwArbitrationCallbackImpl.getInstanceForChr().regisGameCHR(HwQoEService.mHwQoEService);
                            break;
                        }
                    case HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT:
                        HwQoEService.this.gameKOGTcpRttDuration = HwQoEService.this.mGameKOGTcpMonitor.getRttDuration(HwQoEService.this.mForegroundAppUid, HwQoEService.this.mCurNetworkType);
                        HwQoEService.this.gameKOGTcpRttSegs = HwQoEService.this.mGameKOGTcpMonitor.getRttSegs(HwQoEService.this.mForegroundAppUid, HwQoEService.this.mCurNetworkType);
                        if (0 != HwQoEService.this.gameKOGTcpRttSegs - HwQoEService.this.lastGameKOGTcpRttSegs) {
                            HwQoEService.this.gameKOGTcpRtt = (HwQoEService.this.gameKOGTcpRttDuration - HwQoEService.this.lastGameKOGTcpRttDuration) / (HwQoEService.this.gameKOGTcpRttSegs - HwQoEService.this.lastGameKOGTcpRttSegs);
                            HwQoEService.this.mHwQoEGameCHRImpl.updateWifiTcpRtt(HwQoEService.this.gameKOGTcpRttSegs - HwQoEService.this.lastGameKOGTcpRttSegs, HwQoEService.this.gameKOGTcpRtt, HwQoEService.this.gameKOGTcpRttDuration - HwQoEService.this.lastGameKOGTcpRttDuration);
                        }
                        HwQoEService.this.lastGameKOGTcpRttSegs = HwQoEService.this.gameKOGTcpRttSegs;
                        HwQoEService.this.lastGameKOGTcpRttDuration = HwQoEService.this.gameKOGTcpRttDuration;
                        if (!HwQoEService.this.mQoEHandler.hasMessages(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT)) {
                            HwQoEService.this.mQoEHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_UPDATE_UID_TCP_RTT, 8000);
                            break;
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_SCAN_RESULTS:
                        if (HwQoEService.this.isGameKOGInWar) {
                            HwQoEService.this.mHwQoEGameCHRImpl.updateWiFiScanCounter();
                            break;
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_BT_SCAN_STARTED:
                        if (HwQoEService.this.isGameKOGInWar) {
                            HwQoEService.this.mHwQoEGameCHRImpl.updateBTScanCounter();
                            break;
                        }
                        break;
                    case HwQoEUtils.QOE_MSG_SCREEN_ON:
                        if (true == HwQoEService.this.isGameKOGInWar && !HwQoEService.this.isKOGInBackGround) {
                            HwQoEService.this.setGameKOGMode(4);
                            HwQoEService.this.setGameKOGMode(6);
                            break;
                        }
                    case HwQoEUtils.QOE_MSG_SCREEN_OFF:
                        if (true == HwQoEService.this.isGameKOGInWar && !HwQoEService.this.isKOGInBackGround) {
                            HwQoEService.this.enableLowPowerMode();
                            HwQoEService.this.setGameKOGMode(3);
                            break;
                        }
                    case HwQoEUtils.QOE_MSG_GAME_TIMEOUT:
                        HwQoEService.this.handleCellularScene(5);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private boolean isBetaUserAndCust() {
        if (!mKogChrReportInOneMinute || (userType != 3 && userType != 5)) {
            return false;
        }
        return true;
    }

    private void sendIntentDsKOGRttRandomSampling(Context context, long rtt, long tcprtt) {
        int reportInterval = 180000;
        try {
            Random random = new Random();
            if (isBetaUserAndCust()) {
                if (rtt >= 200) {
                    reportInterval = 60000;
                } else {
                    return;
                }
            } else if (rtt > 400) {
                if (random.nextInt(3) != 0) {
                    return;
                }
            } else if (rtt <= 200 || rtt > 400) {
                if (random.nextInt(1000) != 0) {
                    return;
                }
            } else if (random.nextInt(DS_KOG_REPORT_THRESHOLD_200_400_SAMPLING_RATIO) != 0) {
                return;
            }
            Date now = new Date();
            if (this.mLastRptRttBigLatencyTime == null || now.getTime() - this.mLastRptRttBigLatencyTime.getTime() > ((long) reportInterval)) {
                Bundle extras = new Bundle();
                extras.putInt("ReportType", 1);
                extras.putInt("RTT", (int) rtt);
                extras.putInt("TCPRTT", (int) tcprtt);
                Intent chrIntent = new Intent(INTENT_DS_KOG_RTT_REPORT);
                chrIntent.putExtras(extras);
                context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
                this.mLastRptRttBigLatencyTime = now;
            }
        } catch (RuntimeException e) {
            HwQoEUtils.logD("sendIntentDsKOGRttBigLatency get state RuntimeException");
        } catch (Exception e2) {
            HwQoEUtils.logD("sendIntentDsKOGRttBigLatency get state Exception");
        }
    }

    public void disconWiFiNetwork() {
        HwQoEUtils.logD("HwQoEService: disconWiFiNetwork");
        this.isGameNeedDis = true;
        HwWifiConnectivityMonitor.getInstance().disconnectePoorWifi();
    }

    public boolean isInGameAndNeedDisc() {
        return this.isGameNeedDis;
    }

    public boolean isPermitUpdateWifiPowerMode(int mode) {
        if (!HwQoEUtils.MAINLAND_REGION) {
            return false;
        }
        if (7 != mode) {
            return true;
        }
        HwQoEUtils.logD("isPermitWifiPowerMode mDtim:" + this.mDtim + ", mBcnInterval: " + this.mBcnInterval);
        if (isSsidAllowLowPower()) {
            return true;
        }
        return false;
    }

    public boolean isConnectWhenWeChating(ScanResult scanResult) {
        return false;
    }

    public boolean isWeChating() {
        return false;
    }

    public void updateVNPStateChanged(boolean isVpnConnected) {
        this.mHwQoEWiFiOptimization.updateVNPStateChanged(isVpnConnected);
    }

    public boolean isHandoverToMobile() {
        return false;
    }

    public boolean isBgLimitAllowed(int uid) {
        return true == this.isGameKOGInWar ? !this.mHwQoEContentAware.isLiveStreamApp(uid) : !isWifi() || true != this.WifiHotLimitSpeedEnable || this.mHwQoEContentAware.isDownloadApp(uid);
    }

    public void onPeriodSpeed(long outSpeed, long inSpeed) {
    }

    public void onSensitiveAppStateChange(int uid, int state, boolean isBackground) {
    }

    public void onForegroundAppWifiSleepChange(boolean config, int sleeptime, int type, String appName) {
        HwQoEUtils.logD("onForegroundAppWifiSleepChange: config:" + config + ", sleeptime:" + sleeptime + ",type: " + type + ", appname:" + appName);
        if (config) {
            setGameKOGMode(6);
            this.mHwMssArb.setGameForeground(true);
        } else if (this.mHwQoEContentAware.isGameType(type, appName)) {
            setGameKOGMode(6);
            this.mHwMssArb.setGameForeground(true);
            if (!this.isKOGInBackGround && this.isGameKOGInWar) {
                setGameKOGMode(4);
            }
        } else {
            enableLowPowerMode();
            this.mHwMssArb.setGameForeground(false);
        }
    }

    public void onForegroundAppTypeChange(int type, String appName) {
        HwQoEUtils.logD("APP type: " + type + " appName= " + appName);
        if (this.mHwQoEContentAware.isGameType(type, appName)) {
            HwQoEUtils.logD("APP_TYPE_GAME, isKOGInBackGround = " + this.isKOGInBackGround);
            if (!GAME_KOG_PROCESSNAME.equals(appName)) {
                this.mIsInGameState = true;
                return;
            }
            this.mIsInGameState = false;
            this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
            return;
        }
        HwQoEUtils.logD("no game, APP_TYPE_GAME, appName =" + appName);
        this.mIsInGameState = false;
        this.isGameNeedDis = false;
        this.mQoEHandler.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DELAY_DISCONNECT);
    }

    public void updateArpResult(boolean success, int arpRtt) {
        HwQoEUtils.logD("updateArpResult, success =" + success + " , arprtt:" + arpRtt);
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
        if (qoeInfo != null && this.mContext != null) {
            try {
                Intent chrIntent = new Intent(INTENT_HIDATA_DS_CHR_REPORT);
                Bundle extras = new Bundle();
                extras.putInt("sceneId", qoeInfo.mSceneId);
                extras.putInt("videoQoe", qoeInfo.mVideoQoe);
                extras.putInt("ulTup", qoeInfo.mUlTup);
                extras.putInt("dlTup", qoeInfo.mDlTup);
                extras.putInt("netDlTup", qoeInfo.mNetDlTup);
                chrIntent.putExtras(extras);
                this.mContext.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
            } catch (Exception e) {
                HwQoEUtils.logD("Exception: updateHistreamExperience fail! " + e);
            }
        }
    }

    public void notifySelEngineStateStart() {
        HwQoEUtils.logD("notifySelEngineStateStart");
        notifyCallback(2);
    }

    public void notifySelEngineStateEnd(boolean success) {
        HwQoEUtils.logD("notifySelEngineStateEnd success = " + success);
        if (success) {
            notifyCallback(3);
        } else {
            notifyCallback(4);
        }
    }

    private void notifyCallback(int msg) {
        synchronized (this.mLock) {
            if (this.mIHwQoEMonitorList.size() > 0) {
                HwQoEMonitorAdapter adapter = this.mIHwQoEMonitorList.get(Integer.valueOf(HwQoEQualityInfo.HWQOE_MONITOR_TYPE_VOWIFI));
                if (!(adapter == null || adapter.mCallback == null)) {
                    try {
                        adapter.mCallback.onNetworkStateChange(msg);
                    } catch (RemoteException e) {
                        HwQoEUtils.logE("processCheckResult error " + e.toString());
                    }
                }
            }
        }
    }

    public void uploadHiDataDFTEvent(int type, Bundle bundle) {
        this.mHwWifiCHRService.uploadDFTEvent(type, bundle);
    }
}
