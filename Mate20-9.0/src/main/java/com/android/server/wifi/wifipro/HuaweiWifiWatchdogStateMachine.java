package com.android.server.wifi.wifipro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.HwQoE.HidataWechatTraffic;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.HwUidTcpMonitor;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.HwWifiConnectivityMonitor;
import com.android.server.wifi.MSS.HwMSSUtils;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.wifipro.TrafficMonitor;
import java.text.DecimalFormat;

public class HuaweiWifiWatchdogStateMachine extends StateMachine {
    private static final float ADD_UNIT_SPD_SCORE = 0.1f;
    private static final int ADJUST_RSSI_GOOD_PERIOD_COUNT = 8;
    private static final float BACKOFF_NOT_SPD_GOOD_PERIOD_COUNT = 8.0f;
    private static final float BACK_OFF_MIN_SCORE = 1.0f;
    private static final double BAD_RSSI_GOOD_LK_MAX_LOSSRATE_TH_EASY = 0.2d;
    private static final double BAD_RSSI_LEVEL_0_NOT_AVAILABLE_SUB_VAL = 6.0d;
    private static final double BAD_RSSI_LEVEL_1_VERY_POOR_SUB_VAL = 3.0d;
    private static final double BAD_RSSI_LEVEL_2_POOR = 0.0d;
    private static final int BAD_RTT_DEFAULT_VALUE = 0;
    private static final int BAD_RTT_TH_FOR_LOW_RSSI = 2000;
    private static final int BAD_RTT_TH_FOR_OTA_BAD = 3000;
    private static final double BAD_TCP_RETRAN_TH = 0.2d;
    private static final int BASE = 135168;
    private static final int BASE_SCORE_OTA_BAD = 4;
    private static final int BASE_SCORE_RSSI_BAD = 5;
    private static final int BASE_SCORE_TCP_BAD = 3;
    private static final int BASE_SCORE_VERY_BAD_RTT = 3;
    private static final float BASE_SPD_SCORE = 0.5f;
    private static final int BSSID_STAT_CACHE_SIZE = 20;
    private static final int BSSID_STAT_EMPTY_COUNT = 3;
    public static final int BSSID_STAT_RANGE_HIGH_DBM = -45;
    public static final int BSSID_STAT_RANGE_LOW_DBM = -90;
    private static final int CAN_SEND_RSSI_PKT_READ_REQUEST = 0;
    private static final int CHECK_SAVE_RECORD_INTERVAL = 1800000;
    private static final int CMD_RSSI_FETCH = 135179;
    private static final int DBG_LOG_LEVEL = 1;
    /* access modifiers changed from: private */
    public static boolean DDBG_TOAST_DISPLAY = false;
    private static final String DEFAULT_DISPLAY_DATA_STR = "CanNotDisplay";
    private static final int DEFAULT_GOOD_LINK_TARGET_COUNT = 3;
    private static final int DEFAULT_RATE_GOOD_SPEED = 1;
    private static final int DEFAULT_RATE_RSSI = 1;
    private static final int DEFAULT_TARGET_MONITOR_RSSI = -70;
    private static final int DEFAULT_TARGET_RSSI = -65;
    private static final String DEFAULT_WLAN_IFACE = "wlan0";
    private static final int DELAY_TIME_OUT = 30000;
    private static final int DOUBLE_BYTE_LEN = 8;
    private static final float DOWNLOAD_ORIG_RATE = 0.6f;
    private static final int DOWNLOAD_PROHIBIT_SWITCH_PROTECTION = 20;
    private static final int DOWNLOAD_PROHIBIT_SWITCH_RATE = 0;
    private static final float DOWNLOAD_STEP_RATE = 0.02f;
    private static final int DUAL_BAND_UNVALID_SCORE = -1;
    private static final int ERROR_LOG_LEVEL = 3;
    private static final int EVENT_AVOID_TO_WIFI_DELAY_MSG = 136169;
    private static final int EVENT_BSSID_CHANGE = 135175;
    private static final int EVENT_DELAY_OTATCP_TIME_OUT_MSG = 136168;
    private static final int EVENT_GET_5G_AP_RSSI_THRESHOLD = 136268;
    private static final int EVENT_GET_AP_HISTORY_QUALITY_SCORE = 136269;
    private static final int EVENT_HIGH_NET_SPEED_DETECT_MSG = 136170;
    private static final int EVENT_NETWORK_STATE_CHANGE = 135170;
    private static final int EVENT_RSSI_CHANGE = 136172;
    private static final int EVENT_RSSI_TH_VALID_TIMEOUT = 136171;
    private static final int EVENT_START_VERIFY_WITH_DATA_LINK = 135200;
    private static final int EVENT_START_VERIFY_WITH_NOT_DATA_LINK = 135199;
    private static final int EVENT_STOP_VERIFY_WITH_DATA_LINK = 135202;
    private static final int EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK = 135201;
    private static final int EVENT_STORE_HISTORY_QUALITY = 136270;
    private static final int EVENT_SUPPLICANT_STATE_CHANGE = 135172;
    private static final int EVENT_WIFI_RADIO_STATE_CHANGE = 135173;
    private static final int EVENT_WP_WATCHDOG_DISABLE_CMD = 135203;
    private static final int EVENT_WP_WATCHDOG_ENABLE_CMD = 135198;
    private static final double EXP_COEFFICIENT_RECORD = 0.1d;
    private static final String EXTRA_STOP_WIFI_USE_TIME = "extra_block_wifi_use_time";
    private static final int GOOD_LINK_JUDGE_RSSI_INCREMENT = 15;
    private static final int GOOD_LINK_JUDGE_SUB_VAL = -5;
    private static final double GOOD_LINK_LEVEL_3_GOOD_LOSERATE = 0.2d;
    private static final double GOOD_LINK_LEVEL_4_BETTER_LOSERATE = 0.09d;
    private static final double GOOD_LINK_LEVEL_5_BEST_LOSERATE = 0.05d;
    private static final double GOOD_LINK_LOSS_THRESHOLD = 0.2d;
    private static final int GOOD_LINK_RSSI_RANGE_MAX = 17;
    /* access modifiers changed from: private */
    public static final GoodLinkTarget[] GOOD_LINK_TARGET = {new GoodLinkTarget(0, 3, 900000), new GoodLinkTarget(2, 4, 480000), new GoodLinkTarget(4, 4, 240000), new GoodLinkTarget(5, 5, 120000), new GoodLinkTarget(6, 5, 0)};
    private static final int GOOD_LINK_TARGET_MIN_ADD_DB_VAL = 3;
    private static final int GOOD_RTT_TARGET = 1500;
    /* access modifiers changed from: private */
    public static final GoodSpdCountRate[] GOOD_SPD_COUNT_RATE_TBL = {new GoodSpdCountRate(25.0f, 0.5f), new GoodSpdCountRate(20.0f, 0.6f), new GoodSpdCountRate(15.0f, 0.7f), new GoodSpdCountRate(10.0f, SCORE_BACK_OFF_RATE), new GoodSpdCountRate(5.0f, 0.9f)};
    private static final int GOOD_TCP_JUDGE_RTT_PKT = 50;
    private static final int HIGH_DATA_FLOW_BYTES_TH = 3145728;
    private static final float HIGH_DATA_FLOW_DEFAULT_RATE = 1.0f;
    private static final int HIGH_DATA_FLOW_DOWNLOAD = 1;
    private static final int HIGH_DATA_FLOW_DOWNLOAD_TH = 5242880;
    private static final int HIGH_DATA_FLOW_NONE = 0;
    private static final int HIGH_DATA_FLOW_STREAMING = 2;
    private static final int HIGH_NET_SPEED_TH = 65536;
    private static final int HISTORY_HIGH_SPEED_THRESHOLD = 1048576;
    private static final int HIS_RCD_ARRAY_BODY_START_OFFSET = 0;
    private static final long IGNORE_RSSI_BROADCAST_TIME_TH = 4000;
    private static final int IGNORE_SPEED_RSSI = -65;
    private static final int INFO_LOG_LEVEL = 2;
    private static final int INVALID_RSSI = -127;
    private static final int INVALID_RTT_VALUE = 0;
    private static final long LINK_SAMPLING_INTERVAL_MS = 8000;
    private static final int LONGEST_DOWNLOAD_PROTECTION = 40;
    private static final int LONGEST_STREAMING_PROTECTION = 80;
    private static final int LOSS_RATE_INDEX = 0;
    private static final int LOSS_RATE_INFO_COUNT = 2;
    private static final int LOSS_RATE_VOLUME_INDEX = 1;
    private static final int MAX_RESTORE_WIFI_TIME = 360000;
    private static final int MAX_RSSI_PKT_READ_WAIT_SECOND = 15;
    private static final float MAX_SCORE_ONE_CHECK = 2.0f;
    private static final float MAX_SPD_GOOD_COUNT = 30.0f;
    private static final int MAY_BE_POOR_RSSI_TH = -78;
    private static final float MAY_BE_POOR_TH = 4.0f;
    private static final int MILLISECOND_OF_ONE_SECOND = 1000;
    private static final int MIN_REPORT_GOOD_TCP_RX_PKT = 1;
    private static final int MIN_RESTORE_WIFI_TIME = 120000;
    private static final int MIN_SPEED_VAL = 30720;
    private static final int MIN_TCP_JUDGE_PKT = 2;
    private static final int MIN_VALID_VOLUME = 10;
    private static final long MS_OF_ONE_SECOND = 1000;
    private static final int NEAR_RSSI_COUNT = 3;
    private static final int NEAR_RTT_VALID_TIME = 300;
    private static final int NET_RX_SPEED_OK = 24576;
    private static final int NET_TX_SPEED_OK = 32768;
    private static final int NORMAL_ROVE_OUT_PERIOD_COUNT = 2;
    private static final int PERIOD_COUNT_FOR_REPORT_GOOD = 2;
    private static final int PKT_CHK_INTERVEL = 1;
    private static final double PKT_CHK_LOSE_RATE_LEVEL_0_NOT_AVAILABLE = 0.2d;
    private static final double PKT_CHK_LOSE_RATE_LEVEL_1_VERY_POOR = 0.1d;
    private static final double PKT_CHK_LOSE_RATE_LEVEL_2_POOR = 0.0d;
    private static final int PKT_CHK_MIN_PKT_COUNT = 8;
    private static final double PKT_CHK_POOR_LINK_MIN_LOSE_RATE = 0.3d;
    private static final double PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE = 1.0d;
    private static final double PKT_CHK_RETXRATE_LEVEL_1_VERY_POOR = 0.7d;
    private static final double PKT_CHK_RETXRATE_LEVEL_2_POOR = 0.4d;
    private static final int PKT_CHK_RTT_BAD_LEVEL_0_NOT_AVAILABLE = 8000;
    private static final int PKT_CHK_RTT_BAD_LEVEL_1_VERY_POOR = 5000;
    private static final int PKT_CHK_RTT_BAD_LEVEL_2_POOR = 3000;
    private static final double POOR_LINK_MIN_VOLUME = 16.0d;
    private static final int POOR_LINK_RSSI_THRESHOLD = -75;
    private static final int POOR_LINK_SAMPLE_COUNT = 1;
    private static final int POOR_NET_MIN_RSSI_TH = -85;
    private static final int POOR_NET_RSSI_TH_OFFSET = 1;
    private static final int QOS_GOOD_LEVEL_OF_RSSI = -65;
    private static final int QOS_NOT_GOOD_LEVEL_OF_RSSI = -82;
    private static final int QUERY_TCP_INFO_RETRY_CNT = 10;
    private static final int QUERY_TCP_INFO_WAIT_ITVL = 2;
    private static final int REALTIME_TO_MS = 1000;
    private static final int REDUCE_ONE_TIME_UNIT = 64000;
    private static final int REPORT_GOOD_MAX_RTT_VALUE = 1200;
    /* access modifiers changed from: private */
    public static final RestoreWifiTime[] RESTORE_WIFI_TIME = {new RestoreWifiTime(120000, 900000), new RestoreWifiTime(240000, 600000), new RestoreWifiTime(MAX_RESTORE_WIFI_TIME, 0)};
    private static final long ROVE_OUT_LINK_SAMPLING_INTERVAL_MS = 2000;
    private static final RssiGoodSpdTH[] RSSI_GOOD_SPD_TBL = {new RssiGoodSpdTH(-75, 81920, WifiproBqeUtils.BQE_NOT_GOOD_SPEED), new RssiGoodSpdTH(QOS_NOT_GOOD_LEVEL_OF_RSSI, SCORE_BACK_OFF_NETGOOD_SPEED_TH, 15360), new RssiGoodSpdTH(INVALID_RSSI, MIN_SPEED_VAL, 10240)};
    /* access modifiers changed from: private */
    public static final RssiRate[] RSSI_RATE_TBL = {new RssiRate(-50, 0.6f), new RssiRate(-65, 0.7f), new RssiRate(-75, SCORE_BACK_OFF_RATE), new RssiRate(QOS_NOT_GOOD_LEVEL_OF_RSSI, 0.9f)};
    private static final int RSSI_SMOOTH_DIV_NUM = 2;
    private static final int RSSI_SMOOTH_MULTIPLE = 1;
    private static final int RSSI_TH_BACKOFF_RTT_TH = 2000;
    private static final double RSSI_TH_BACKOFF_TCP_RETRAN_TH = 0.4d;
    private static final int RSSI_TH_MAX_VALID_TIME = 300000;
    private static final int RSSI_TH_MIN_VALID_TIME = 180000;
    private static final int RSSI_TH_VALID_TIME_ACCUMULATION = 30000;
    private static final int RTT_VALID_MAX_TIME = 8000;
    private static final float SCORE_BACK_OFF_GOOD_RTT_RATE = 0.5f;
    private static final int SCORE_BACK_OFF_NETGOOD_MAX_RTT = 1200;
    private static final int SCORE_BACK_OFF_NETGOOD_SPEED_TH = 51200;
    private static final float SCORE_BACK_OFF_RATE = 0.8f;
    private static final int SPEED_EXPIRE_LATENCY = 5000;
    private static final int SPEED_PERIOD_TIME = 2;
    private static final int SPEED_VAL_1KBPS = 1024;
    private static final int START_VL_RSSI_CHK_TIME = 30000;
    private static final float STREAMING_ORIG_RATE = 0.3f;
    private static final int STREAMING_PROHIBIT_SWITCH_PROTECTION = 50;
    private static final int STREAMING_PROHIBIT_SWITCH_RATE = 0;
    private static final float STREAMING_STEP_RATE = 0.01f;
    private static final float SWITCH_OUT_SCORE = 6.0f;
    private static final String TAG = "HuaweiWifiWatchdogStateMachine";
    private static final int TCP_BAD_RETRAN_MIN_PKT = 3;
    private static final int TCP_BAD_RSSI_BACKOFF_VAL = 5;
    private static final int TCP_CHK_RESULT_BAD = 2;
    private static final int TCP_CHK_RESULT_UNKNOWN = 0;
    private static final int TCP_CHK_RESULT_VERY_BAD = 1;
    private static final int TCP_MIN_TX_PKT = 3;
    private static final int TCP_PERIOD_CHK_RTT_BAD_TH = 1500;
    private static final int TCP_PERIOD_CHK_RTT_GOOD_TH = 1000;
    private static final int TCP_RESULT_TYPE_INVALID = -1;
    private static final int TCP_RESULT_TYPE_NOTIFY = 1;
    private static final int TCP_RESULT_TYPE_QUERY_RSP = 0;
    private static final int TCP_TX_ZERO_PKT = 0;
    private static final int TIME_ELAP_2_MIN = 120000;
    private static final int VERY_BAD_RTT_TH_FOR_TCP_BAD = 16000;
    private static final int VL_RSSI_SMOOTH_DIV_NUM = 2;
    private static final int WIFI_POOR_OTA_OR_TCP_BAD = 1;
    private static final int WIFI_POOR_RSSI_BAD = 0;
    /* access modifiers changed from: private */
    public static final String WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
    private static final int WLAN_INET_RSSI_LEVEL_3_GOOD = 0;
    private static final int WLAN_INET_RSSI_LEVEL_4_BETTER = 5;
    private static final int WLAN_INET_RSSI_LEVEL_5_BEST = 8;
    /* access modifiers changed from: private */
    public static double[] mSPresetLoss;
    private static int printLogLevel = 1;
    /* access modifiers changed from: private */
    public int LOW_RSSI_TH = QOS_NOT_GOOD_LEVEL_OF_RSSI;
    /* access modifiers changed from: private */
    public int QOE_LEVEL_RTT_MIN_PKT = 3;
    /* access modifiers changed from: private */
    public int RSSI_TH_FOR_BAD_QOE_LEVEL = QOS_NOT_GOOD_LEVEL_OF_RSSI;
    /* access modifiers changed from: private */
    public int RSSI_TH_FOR_NOT_BAD_QOE_LEVEL = -75;
    /* access modifiers changed from: private */
    public long mBestSpeedInPeriod = 0;
    private BroadcastReceiver mBroadcastReceiver;
    private LruCache<String, BssidStatistics> mBssidCache = new LruCache<>(20);
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mCurrRssi = INVALID_RSSI;
    /* access modifiers changed from: private */
    public String mCurrSSID = null;
    /* access modifiers changed from: private */
    public int mCurrentBqeLevel = 0;
    /* access modifiers changed from: private */
    public BssidStatistics mCurrentBssid;
    private DecimalFormat mFormatData;
    /* access modifiers changed from: private */
    public int mGoodDetectBaseRssi;
    /* access modifiers changed from: private */
    public long mHighDataFlowLastRxBytes = 0;
    /* access modifiers changed from: private */
    public int mHighDataFlowNotDetectCounter = 0;
    /* access modifiers changed from: private */
    public int mHighDataFlowPeriodCounter = 0;
    /* access modifiers changed from: private */
    public int mHighDataFlowProtection = 0;
    /* access modifiers changed from: private */
    public float mHighDataFlowRate = 1.0f;
    /* access modifiers changed from: private */
    public int mHighDataFlowScenario = 0;
    /* access modifiers changed from: private */
    public int mHighSpeedToken = 0;
    /* access modifiers changed from: private */
    public int mHistoryHSCount = 0;
    /* access modifiers changed from: private */
    public HwDualBandQualityEngine mHwDualBandQualityEngine;
    private IntentFilter mIntentFilter;
    /* access modifiers changed from: private */
    public boolean mIsDualbandEnable = false;
    private boolean mIsMonitoring = false;
    /* access modifiers changed from: private */
    public boolean mIsNotStatic;
    private boolean mIsRegister = false;
    /* access modifiers changed from: private */
    public boolean mIsSpeedOkDuringPeriod = false;
    /* access modifiers changed from: private */
    public int mLastDetectLevel = 0;
    /* access modifiers changed from: private */
    public boolean mLastHighDataFlow = false;
    /* access modifiers changed from: private */
    public TcpChkResult mLastPeriodTcpChkResult;
    private String mLastSampleBssid = null;
    private int mLastSamplePkts = 0;
    /* access modifiers changed from: private */
    public int mLastSampleRssi = 0;
    private int mLastSampleRtt = 0;
    private TcpChkResult mNewestTcpChkResult = new TcpChkResult();
    /* access modifiers changed from: private */
    public float mPeriodGoodSpdScore = 0.0f;
    /* access modifiers changed from: private */
    public int mPoorNetRssiRealTH = -75;
    /* access modifiers changed from: private */
    public int mPoorNetRssiTH = -75;
    /* access modifiers changed from: private */
    public int mPoorNetRssiTHNext = -75;
    private Handler mQMHandler;
    /* access modifiers changed from: private */
    public int mRssiChangedToken = 0;
    /* access modifiers changed from: private */
    public int mRssiFetchToken = 0;
    /* access modifiers changed from: private */
    public int mRssiTHTimeoutToken = 0;
    /* access modifiers changed from: private */
    public int mRssiTHValidTime = 180000;
    /* access modifiers changed from: private */
    public float mSpeedGoodCount = 0.0f;
    /* access modifiers changed from: private */
    public int mSpeedNotGoodCount = 0;
    private Runnable mSpeedUpdate = new Runnable() {
        public void run() {
            synchronized (HuaweiWifiWatchdogStateMachine.this.mTraffic) {
                TrafficMonitor.TxRxStat unused = HuaweiWifiWatchdogStateMachine.this.mTxrxStat = HuaweiWifiWatchdogStateMachine.this.mTraffic.getStatic(2);
                if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 24576 || HuaweiWifiWatchdogStateMachine.this.mTxrxStat.tx_speed >= 32768) {
                    boolean unused2 = HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod = true;
                }
                if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed > HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod) {
                    long unused3 = HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod = HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed;
                }
                if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi != HuaweiWifiWatchdogStateMachine.INVALID_RSSI && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < -75 && HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 65536) {
                    HuaweiWifiWatchdogStateMachine.this.sendMessage(HuaweiWifiWatchdogStateMachine.this.obtainMessage(HuaweiWifiWatchdogStateMachine.EVENT_HIGH_NET_SPEED_DETECT_MSG, HuaweiWifiWatchdogStateMachine.access$504(HuaweiWifiWatchdogStateMachine.this), 0));
                }
                HuaweiWifiWatchdogStateMachine.access$616(HuaweiWifiWatchdogStateMachine.this, HuaweiWifiWatchdogStateMachine.this.checkGoodSpd(HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed));
                if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 1048576) {
                    int unused4 = HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount = HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount + 1;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public long mSwitchOutTime = 0;
    /* access modifiers changed from: private */
    public volatile int mTcpReportLevel = 0;
    /* access modifiers changed from: private */
    public TrafficMonitor mTraffic = null;
    /* access modifiers changed from: private */
    public volatile TrafficMonitor.TxRxStat mTxrxStat = null;
    /* access modifiers changed from: private */
    public WifiInfo mWifiInfo;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public int mWifiPoorReason;
    /* access modifiers changed from: private */
    public int mWifiPoorRssi;
    private WifiProDefaultState mWifiProDefaultState = new WifiProDefaultState();
    /* access modifiers changed from: private */
    public WifiProHistoryRecordManager mWifiProHistoryRecordManager;
    /* access modifiers changed from: private */
    public WifiProLinkMonitoringState mWifiProLinkMonitoringState = new WifiProLinkMonitoringState();
    /* access modifiers changed from: private */
    public WifiProRoveOutParaRecord mWifiProRoveOutParaRecord;
    /* access modifiers changed from: private */
    public WifiProStatisticsManager mWifiProStatisticsManager;
    /* access modifiers changed from: private */
    public WifiProStopVerifyState mWifiProStopVerifyState = new WifiProStopVerifyState();
    /* access modifiers changed from: private */
    public WifiProVerifyingLinkState mWifiProVerifyingLinkState = new WifiProVerifyingLinkState();
    /* access modifiers changed from: private */
    public WifiProWatchdogDisabledState mWifiProWatchdogDisabledState = new WifiProWatchdogDisabledState();
    private WifiProWatchdogEnabledState mWifiProWatchdogEnabledState = new WifiProWatchdogEnabledState();
    /* access modifiers changed from: private */
    public boolean mWpLinkMonitorRunning = false;
    /* access modifiers changed from: private */
    public AsyncChannel mWsmChannel = new AsyncChannel();
    /* access modifiers changed from: private */
    public boolean wifiOtaChkIsEnable;

    /* renamed from: com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$DetailedState = new int[NetworkInfo.DetailedState.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.VERIFYING_POOR_LINK.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    private class BssidStatistics {
        private static final int MAX_NEAR_RSSI_COUNT = 5;
        private static final int MIN_NEAR_RSSI_COUNT = 2;
        /* access modifiers changed from: private */
        public final String mBssid;
        private VolumeWeightedEMA[] mEntries;
        private int mEntriesSize;
        public int mGoodLinkTargetCount;
        private int mGoodLinkTargetIndex;
        public int mGoodLinkTargetRssi;
        private boolean mIsHistoryLoaded = false;
        public boolean mIsNotFirstChk = false;
        private long mLastTimeRssiPoor = 0;
        /* access modifiers changed from: private */
        public long mLastTimeSample;
        private long mLastTimeotaTcpPoor = 0;
        public int mPoorLinkTargetRssi = -75;
        public int mRestoreDelayTime;
        public int mRestoreTimeIdx;
        private int mRssiBase;
        public long mTimeElapBaseTime;
        public int mTimeElapGoodLinkTargetRssi;
        private WifiProApQualityRcd mWifiProApQualityRcd;

        public BssidStatistics(String bssid) {
            this.mBssid = bssid;
            this.mRssiBase = -90;
            this.mEntriesSize = 46;
            this.mEntries = new VolumeWeightedEMA[this.mEntriesSize];
            for (int i = 0; i < this.mEntriesSize; i++) {
                this.mEntries[i] = new VolumeWeightedEMA(0.1d);
            }
            this.mRestoreDelayTime = 120000;
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                this.mWifiProApQualityRcd = new WifiProApQualityRcd(this.mBssid);
            }
        }

        public void updateLoss(int rssi, double value, int volume) {
            if (volume > 0) {
                if (rssi >= -45) {
                    rssi = -45;
                } else if (rssi < -90) {
                    rssi = -90;
                }
                int index = rssi - this.mRssiBase;
                if (index >= 0 && index < this.mEntriesSize) {
                    this.mEntries[index].updateLossRate(value, volume);
                }
            }
        }

        public void updateRtt(int rssi, int value, int volume) {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && volume > 0) {
                if (rssi >= -45) {
                    rssi = -45;
                } else if (rssi < -90) {
                    rssi = -90;
                }
                int index = rssi - this.mRssiBase;
                if (index >= 0 && index < this.mEntriesSize) {
                    this.mEntries[index].updateAvgRtt(value, volume);
                }
            }
        }

        public VolumeWeightedEMA getRssiLoseRate(int rssi) {
            int index = rssi - this.mRssiBase;
            if (index < 0 || index >= this.mEntriesSize) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logE(" getRssiLoseRate rssi=" + rssi + ", index=" + index + ", mRssiBase=" + this.mRssiBase);
                return null;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.logD(" getRssiLoseRate: loss[" + rssi + "]=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[index].mOtaLossRate * 100.0d) + "%, volume=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[index].mOtaVolume) + ", index =" + index);
            return this.mEntries[index];
        }

        public VolumeWeightedEMA getEMAInfo(int rssi) {
            if (rssi > -45) {
                rssi = -45;
            } else if (rssi < -90) {
                rssi = -90;
            }
            return this.mEntries[rssi - this.mRssiBase];
        }

        public int getHistoryNearRtt(int baseRssi) {
            if (baseRssi > -45) {
                baseRssi = -45;
            } else if (baseRssi < -90) {
                baseRssi = -90;
            }
            int baseIndex = baseRssi - this.mRssiBase;
            int near_rssi_count = 0;
            long avgRtt = this.mEntries[baseIndex].mAvgRtt;
            long totalRttVolume = this.mEntries[baseIndex].mRttVolume;
            long totalRttProduct = this.mEntries[baseIndex].mRttProduct;
            while (near_rssi_count <= 5) {
                near_rssi_count++;
                int index = baseIndex + near_rssi_count;
                if (index >= 0 && index < this.mEntriesSize) {
                    totalRttProduct += this.mEntries[index].mRttProduct;
                    totalRttVolume += this.mEntries[index].mRttVolume;
                }
                int index2 = baseIndex - near_rssi_count;
                if (index2 >= 0 && index2 < this.mEntriesSize) {
                    totalRttProduct += this.mEntries[index2].mRttProduct;
                    totalRttVolume += this.mEntries[index2].mRttVolume;
                }
                if (near_rssi_count >= 2 && totalRttVolume >= 10) {
                    break;
                }
            }
            if (totalRttVolume > 0) {
                avgRtt = totalRttProduct / totalRttVolume;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logD("from rssi " + (baseRssi - near_rssi_count) + " to " + (baseRssi + near_rssi_count) + " avgRtt=" + avgRtt);
            return (int) avgRtt;
        }

        public double[] getHistoryNearLossRate(int baseRssi) {
            int baseIndex = baseRssi - this.mRssiBase;
            int near_rssi_count = 0;
            double avgLossRate = this.mEntries[baseIndex].mOtaLossRate;
            double totalOtaVolume = this.mEntries[baseIndex].mOtaVolume;
            double totalOtaProduct = this.mEntries[baseIndex].mOtaProduct;
            double[] lossRateInfo = new double[2];
            while (near_rssi_count <= 5) {
                near_rssi_count++;
                int index = baseIndex + near_rssi_count;
                if (index >= 0 && index < this.mEntriesSize) {
                    totalOtaProduct += this.mEntries[index].mOtaProduct;
                    totalOtaVolume += this.mEntries[index].mOtaVolume;
                }
                int index2 = baseIndex - near_rssi_count;
                if (index2 >= 0 && index2 < this.mEntriesSize) {
                    totalOtaProduct += this.mEntries[index2].mOtaProduct;
                    totalOtaVolume += this.mEntries[index2].mOtaVolume;
                }
                if (near_rssi_count >= 2 && totalOtaVolume >= 10.0d) {
                    break;
                }
            }
            if (totalOtaVolume > 0.0d) {
                avgLossRate = totalOtaProduct / totalOtaVolume;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logD("from rssi " + (baseRssi - near_rssi_count) + " to " + (baseRssi + near_rssi_count) + " avgLossRate=" + String.valueOf(avgLossRate) + "voludme=" + String.valueOf(totalOtaVolume));
            lossRateInfo[0] = avgLossRate;
            lossRateInfo[1] = totalOtaVolume;
            return lossRateInfo;
        }

        public double presetLoss(int rssi) {
            if (rssi <= -90) {
                return HuaweiWifiWatchdogStateMachine.PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE;
            }
            if (rssi > 0) {
                return 0.0d;
            }
            if (HuaweiWifiWatchdogStateMachine.mSPresetLoss == null) {
                double[] unused = HuaweiWifiWatchdogStateMachine.mSPresetLoss = new double[90];
                for (int i = 0; i < 90; i++) {
                    HuaweiWifiWatchdogStateMachine.mSPresetLoss[i] = HuaweiWifiWatchdogStateMachine.PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE / Math.pow((double) (90 - i), 1.5d);
                }
            }
            return HuaweiWifiWatchdogStateMachine.mSPresetLoss[-rssi];
        }

        public boolean poorLinkDetected(int rssi) {
            HuaweiWifiWatchdogStateMachine.this.logD(" Poor link detected, base rssi=" + rssi + ", GoodLinkTargetIndex=" + this.mGoodLinkTargetIndex);
            long now = SystemClock.elapsedRealtime();
            long lastPoor = now - this.mLastTimeRssiPoor;
            this.mLastTimeRssiPoor = now;
            if (rssi < this.mPoorLinkTargetRssi) {
                rssi = this.mPoorLinkTargetRssi;
                HuaweiWifiWatchdogStateMachine.this.logD(" Poor link detected,GoodLinkTargetRssi should not < " + this.mPoorLinkTargetRssi + " dB, set to: " + rssi + " dB.");
            }
            while (this.mGoodLinkTargetIndex > 0 && lastPoor >= ((long) HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET[this.mGoodLinkTargetIndex - 1].REDUCE_TIME_MS)) {
                this.mGoodLinkTargetIndex--;
            }
            this.mGoodLinkTargetCount = HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET[this.mGoodLinkTargetIndex].SAMPLE_COUNT;
            this.mTimeElapGoodLinkTargetRssi = findRssiTarget(rssi, rssi + 17, 0.2d);
            this.mTimeElapBaseTime = now;
            if (this.mTimeElapGoodLinkTargetRssi < rssi + 3) {
                this.mGoodLinkTargetRssi = rssi + 3;
            } else {
                this.mGoodLinkTargetRssi = this.mTimeElapGoodLinkTargetRssi;
            }
            this.mGoodLinkTargetRssi += HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET[this.mGoodLinkTargetIndex].RSSI_ADJ_DBM;
            if (this.mGoodLinkTargetIndex < HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET.length - 1) {
                this.mGoodLinkTargetIndex++;
            }
            HuaweiWifiWatchdogStateMachine.this.logI("goodRssi=" + this.mGoodLinkTargetRssi + " TimeElapGoodRssiTarget=" + this.mTimeElapGoodLinkTargetRssi + " goodSampleCount=" + this.mGoodLinkTargetCount + " lastPoor=" + lastPoor + ",new GoodLinkTargetIndex=" + this.mGoodLinkTargetIndex);
            return true;
        }

        /* access modifiers changed from: private */
        public void otaTcpPoorLinkDetected() {
            HuaweiWifiWatchdogStateMachine.this.logD("otaTcpPoor enter.");
            long now = SystemClock.elapsedRealtime();
            long lastPoor = now - this.mLastTimeotaTcpPoor;
            this.mLastTimeotaTcpPoor = now;
            if (this.mRestoreTimeIdx < 0) {
                this.mRestoreTimeIdx = 0;
            }
            if (this.mRestoreTimeIdx >= HuaweiWifiWatchdogStateMachine.RESTORE_WIFI_TIME.length) {
                this.mRestoreTimeIdx = HuaweiWifiWatchdogStateMachine.RESTORE_WIFI_TIME.length - 1;
            }
            while (this.mRestoreTimeIdx > 0 && lastPoor >= ((long) HuaweiWifiWatchdogStateMachine.RESTORE_WIFI_TIME[this.mRestoreTimeIdx - 1].LEVEL_SEL_TIME)) {
                this.mRestoreTimeIdx--;
            }
            this.mRestoreDelayTime = HuaweiWifiWatchdogStateMachine.RESTORE_WIFI_TIME[this.mRestoreTimeIdx].RESTORE_TIME;
            this.mRestoreTimeIdx++;
            HuaweiWifiWatchdogStateMachine.this.logI("otaTcpPoor Restore Time=" + this.mRestoreDelayTime + ", RestoreTimeIdx=" + this.mRestoreTimeIdx);
        }

        public void newLinkDetected() {
            HuaweiWifiWatchdogStateMachine.this.logD(" newLinkDetected enter");
            this.mGoodLinkTargetRssi = findRssiTarget(-90, -45, 0.2d);
            this.mPoorLinkTargetRssi = -75;
            if (this.mGoodLinkTargetRssi < this.mPoorLinkTargetRssi) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logD(" Poor link detected, select " + this.mGoodLinkTargetRssi + " < " + this.mPoorLinkTargetRssi + "dB, set GoodLink base rssi=" + this.mPoorLinkTargetRssi + "dB.");
                this.mGoodLinkTargetRssi = this.mPoorLinkTargetRssi;
            }
            this.mTimeElapGoodLinkTargetRssi = this.mGoodLinkTargetRssi;
            this.mGoodLinkTargetCount = 3;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.logI("New link verifying target set, rssi=" + this.mGoodLinkTargetRssi + " count=" + this.mGoodLinkTargetCount);
        }

        public int findRssiTarget(int from, int to, double threshold) {
            int from2 = from - this.mRssiBase;
            int to2 = to - this.mRssiBase;
            int d = from2 < to2 ? 1 : -1;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logD(" findRssiTarget need rssi with lose rate < " + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(threshold * 100.0d) + "%, idx from:" + from2 + "~ to:" + to2);
            int emptyCount = 0;
            int i = from2;
            while (i != to2) {
                if (i < 0 || i >= this.mEntriesSize || this.mEntries[i].mOtaVolume <= HuaweiWifiWatchdogStateMachine.PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE) {
                    emptyCount++;
                    if (emptyCount >= 3) {
                        int rssi = this.mRssiBase + i;
                        double lossPreset = presetLoss(rssi);
                        if (lossPreset < threshold) {
                            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                            StringBuilder sb = new StringBuilder();
                            sb.append(" get good link rssi target in default table.target rssi=");
                            sb.append(rssi);
                            sb.append("dB, lose rate=");
                            int i2 = from2;
                            int i3 = to2;
                            sb.append(HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(lossPreset * 100.0d));
                            sb.append("% < threshold:");
                            sb.append(HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(100.0d * threshold));
                            sb.append("%,i=");
                            sb.append(i);
                            huaweiWifiWatchdogStateMachine2.logD(sb.toString());
                            return rssi;
                        }
                    } else {
                        continue;
                    }
                } else {
                    emptyCount = 0;
                    if (this.mEntries[i].mOtaLossRate < threshold) {
                        int rssi2 = this.mRssiBase + i;
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine3.logD(" Meet ok target in rssi=" + rssi2 + "dB, lose rate value=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[i].mOtaLossRate * 100.0d) + "%, stot pkt =" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[i].mOtaVolume) + ",i=" + i);
                        return rssi2;
                    }
                }
                i += d;
                from2 = from2;
                to2 = to2;
            }
            return this.mRssiBase + to2;
        }

        public int findRssiTargetByRtt(int from, int to, long rttThreshold, int defaultTargetRssi) {
            int from2 = from - this.mRssiBase;
            int to2 = to - this.mRssiBase;
            int d = from2 < to2 ? 1 : -1;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logD("findRssiTargetByRtt: rtt < " + rttThreshold + ", idx from " + from2 + "~ " + to2);
            for (int i = from2; i != to2; i += d) {
                if (i >= 0 && i < this.mEntriesSize && this.mEntries[i].mRttVolume > 5 && this.mEntries[i].mAvgRtt != 0) {
                    if (this.mEntries[i].mAvgRtt < rttThreshold) {
                        int rssi = this.mRssiBase + i;
                        if (((long) getHistoryNearRtt(rssi)) < rttThreshold) {
                            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                            huaweiWifiWatchdogStateMachine2.logD("findRssiTargetByRtt:Meet ok target in rssi=" + rssi + "dB, rtt value=" + this.mEntries[i].mAvgRtt + ", packets =" + this.mEntries[i].mRttVolume);
                            return rssi;
                        }
                    } else {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine3.logD("findRssiTargetByRtt: rssi=" + (this.mRssiBase + i) + "dB, rtt value=" + this.mEntries[i].mAvgRtt + ", packets =" + this.mEntries[i].mRttVolume);
                    }
                }
            }
            return defaultTargetRssi;
        }

        private void saveApQualityRecord(WifiProApQualityRcd apQualityRcd) {
            if (apQualityRcd == null) {
                HuaweiWifiWatchdogStateMachine.this.logE("apQualityRcd is null");
                return;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logD("save bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + " history record, mEntriesSize = " + this.mEntriesSize);
            for (int i = 0; i < this.mEntriesSize; i++) {
                apQualityRcd.putAvgRttToRecord(this.mEntries[i].mAvgRtt, i);
                apQualityRcd.putRttProductToRecord(this.mEntries[i].mRttProduct, i);
                apQualityRcd.putRttVolumeToRecord(this.mEntries[i].mRttVolume, i);
                apQualityRcd.putLostRateToRecord(this.mEntries[i].mOtaLossRate, i);
                apQualityRcd.putLostVolumeToRecord(this.mEntries[i].mOtaVolume, i);
                apQualityRcd.putLostProductToRecord(this.mEntries[i].mOtaProduct, i);
            }
            apQualityRcd.apBSSID = this.mBssid;
            HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.saveApQualityRecord(apQualityRcd);
        }

        private boolean loadApQualityRecord(WifiProApQualityRcd apQualityRcd) {
            HuaweiWifiWatchdogStateMachine.this.logD("loadApQualityRecord enter.");
            if (apQualityRcd == null) {
                HuaweiWifiWatchdogStateMachine.this.logE("apQualityRcd is null");
                return false;
            } else if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.loadApQualityRecord(apQualityRcd.apBSSID, apQualityRcd)) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logD("restoreBssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + " HistoreyRecord restore record, mEntriesSize = " + this.mEntriesSize);
                for (int i = 0; i < this.mEntriesSize; i++) {
                    this.mEntries[i].mAvgRtt = apQualityRcd.getAvgRttFromRecord(i);
                    this.mEntries[i].mRttProduct = apQualityRcd.getRttProductFromRecord(i);
                    this.mEntries[i].mRttVolume = apQualityRcd.getRttVolumeFromRecord(i);
                    this.mEntries[i].mOtaLossRate = apQualityRcd.getLostRateFromRecord(i);
                    this.mEntries[i].mOtaVolume = apQualityRcd.getLostVolumeFromRecord(i);
                    this.mEntries[i].mOtaProduct = apQualityRcd.getLostProductFromRecord(i);
                }
                return true;
            } else {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.logE("loadApQualityRecord read DB failed for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid));
                return false;
            }
        }

        private void initBQEHistoryRecord(WifiProApQualityRcd apQualityRcd) {
            if (!loadApQualityRecord(apQualityRcd)) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logD("not ApQualityRcd for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + ".");
            } else {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.logD("succ load ApQualityRcd for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + ".");
                HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY, 1800000);
            }
            this.mIsHistoryLoaded = true;
        }

        public void afterConnectProcess() {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logD("afterConnectProcess enter for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid));
                initBQEHistoryRecord(this.mWifiProApQualityRcd);
            }
        }

        public void afterDisconnectProcess() {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logD("afterDisconnectProcess enter for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid));
                if (this.mIsHistoryLoaded) {
                    saveApQualityRecord(this.mWifiProApQualityRcd);
                    HuaweiWifiWatchdogStateMachine.this.removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY);
                } else {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine2.logE("afterDisconnectProcess not read bssid:" + this.mBssid + " record, can not save.");
                }
            }
        }

        public void initQualityRecordFromDatabase() {
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logD("initQualityRecordFromDatabase enter for bssid:" + this.mBssid);
            initBQEHistoryRecord(this.mWifiProApQualityRcd);
        }

        public VolumeWeightedEMA getHistoryEntryByRssi(int rssi) {
            if (this.mEntries == null) {
                return null;
            }
            int arrayIdx = rssi - this.mRssiBase;
            if (arrayIdx >= 0 && arrayIdx < this.mEntriesSize) {
                return this.mEntries[arrayIdx];
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logE("getHistoryEntryByRssi invalid rssi idx: " + rssi);
            return null;
        }

        public void storeHistoryQuality() {
            if (this.mIsHistoryLoaded) {
                saveApQualityRecord(this.mWifiProApQualityRcd);
            } else {
                HuaweiWifiWatchdogStateMachine.this.logE("History Quality is not Loaded!");
            }
        }
    }

    private static class GoodLinkTarget {
        public final int REDUCE_TIME_MS;
        public final int RSSI_ADJ_DBM;
        public final int SAMPLE_COUNT;

        public GoodLinkTarget(int adj, int count, int time) {
            this.RSSI_ADJ_DBM = adj;
            this.SAMPLE_COUNT = count;
            this.REDUCE_TIME_MS = time;
        }
    }

    private static class GoodSpdCountRate {
        public final float GOOD_COUNT;
        public final float SW_RATE;

        public GoodSpdCountRate(float goodCount, float rate) {
            this.GOOD_COUNT = goodCount;
            this.SW_RATE = rate;
        }
    }

    class ProcessedTcpResult {
        public int mAvgRtt = 0;
        public int mInadequateAvgRtt;
        public int mTotPkt = 0;

        ProcessedTcpResult() {
            boolean unused = HuaweiWifiWatchdogStateMachine.this.mIsNotStatic = true;
        }
    }

    private static class RestoreWifiTime {
        public final int LEVEL_SEL_TIME;
        public final int RESTORE_TIME;

        public RestoreWifiTime(int restoreTime, int selTime) {
            this.RESTORE_TIME = restoreTime;
            this.LEVEL_SEL_TIME = selTime;
        }
    }

    private static class RssiGoodSpdTH {
        public final int RSSI_VAL;
        public final int SPD_VAL;
        public final int UNIT_SPD_VAL;

        public RssiGoodSpdTH(int rssiVal, int spd, int unitSpd) {
            this.RSSI_VAL = rssiVal;
            this.SPD_VAL = spd;
            this.UNIT_SPD_VAL = unitSpd;
        }
    }

    private static class RssiRate {
        public final int RSSI_VAL;
        public final float SW_RATE;

        public RssiRate(int rssiVal, float swRate) {
            this.RSSI_VAL = rssiVal;
            this.SW_RATE = swRate;
        }
    }

    private class TcpChkResult {
        public int lenth;
        public int mResultType;
        public int mTcpCongWhen;
        public int mTcpCongestion;
        public int mTcpQuality;
        public int mTcpRetransPkts;
        public int mTcpRtt;
        public int mTcpRttPkts;
        public int mTcpRttWhen;
        public int mTcpRxPkts;
        public int mTcpTxPkts;
        public long mUpdateTime;
        public boolean mValueIsSet;

        TcpChkResult() {
            this.mValueIsSet = false;
            this.mTcpRtt = 0;
            this.mTcpRttPkts = 0;
            this.mTcpRttWhen = 0;
            this.mTcpCongestion = 0;
            this.mTcpCongWhen = 0;
            this.mTcpQuality = 0;
            this.mUpdateTime = 0;
            this.mResultType = -1;
            this.mTcpTxPkts = 0;
            this.mTcpRxPkts = 0;
            this.mTcpRetransPkts = 0;
            this.mValueIsSet = false;
        }

        public void copyFrom(TcpChkResult source) {
            if (source != null) {
                this.mTcpRtt = source.mTcpRtt;
                this.mTcpRttPkts = source.mTcpRttPkts;
                this.mTcpRttWhen = source.mTcpRttWhen;
                this.mTcpCongestion = source.mTcpCongestion;
                this.mTcpCongWhen = source.mTcpCongWhen;
                this.mTcpQuality = source.mTcpQuality;
                this.mUpdateTime = source.mUpdateTime;
                this.mResultType = source.mResultType;
                this.mTcpTxPkts = source.mTcpTxPkts;
                this.mTcpRxPkts = source.mTcpRxPkts;
                this.mTcpRetransPkts = source.mTcpRetransPkts;
                this.lenth = source.lenth;
                this.mValueIsSet = source.mValueIsSet;
            }
        }

        public void reset() {
            this.mTcpRtt = 0;
            this.mTcpRttPkts = 0;
            this.mTcpRttWhen = 0;
            this.mTcpCongestion = 0;
            this.mTcpCongWhen = 0;
            this.mTcpQuality = 0;
            this.mUpdateTime = 0;
            this.mResultType = -1;
            this.mTcpTxPkts = 0;
            this.mTcpRxPkts = 0;
            this.mTcpRetransPkts = 0;
            this.mValueIsSet = false;
        }

        public boolean tcpResultUpdate(int[] resultArray, int len, int resultType) {
            if (!HuaweiWifiWatchdogStateMachine.this.mWpLinkMonitorRunning) {
                return true;
            }
            if (resultArray == null) {
                HuaweiWifiWatchdogStateMachine.this.logE(" tcpResultUpdate null error.");
                return false;
            }
            this.lenth = len;
            if (len == 0) {
                HuaweiWifiWatchdogStateMachine.this.logE(" tcpResultUpdate 0 len error.");
                return false;
            }
            this.mResultType = resultType;
            this.mTcpRtt = resultArray[0];
            if (len > 1) {
                this.mTcpRttPkts = resultArray[1];
            }
            if (len > 2) {
                this.mTcpRttWhen = resultArray[2];
            }
            if (len > 3) {
                this.mTcpCongestion = resultArray[3];
            }
            if (len > 4) {
                this.mTcpCongWhen = resultArray[4];
            }
            if (len > 5) {
                this.mTcpQuality = resultArray[5];
            }
            if (len > 6) {
                this.mTcpTxPkts = resultArray[6];
            }
            if (len > 7) {
                this.mTcpRxPkts = resultArray[7];
            }
            if (len > 8) {
                this.mTcpRetransPkts = resultArray[8];
            }
            if (this.mResultType == 1) {
                int unused = HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel = this.mTcpQuality;
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("TCP result ##### Quality=" + HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel);
            }
            this.mUpdateTime = SystemClock.elapsedRealtime();
            this.mValueIsSet = true;
            return true;
        }
    }

    private class VolumeWeightedEMA {
        public long mAvgRtt = 0;
        public double mOtaLossRate = 0.0d;
        public double mOtaProduct = 0.0d;
        public double mOtaVolume = 0.0d;
        public long mRttProduct = 0;
        public long mRttVolume = 0;

        public VolumeWeightedEMA(double coefficient) {
            boolean unused = HuaweiWifiWatchdogStateMachine.this.mIsNotStatic = true;
        }

        public void updateLossRate(double newValue, int newVolume) {
            if (newVolume > 0) {
                this.mOtaProduct += ((double) newVolume) * newValue;
                this.mOtaVolume = ((double) newVolume) + this.mOtaVolume;
                if (this.mOtaVolume != 0.0d) {
                    this.mOtaLossRate = this.mOtaProduct / this.mOtaVolume;
                }
            }
        }

        public void updateAvgRtt(int newValue, int newVolume) {
            if (newVolume > 0) {
                this.mRttProduct = ((long) (newValue * newVolume)) + this.mRttProduct;
                this.mRttVolume = ((long) newVolume) + this.mRttVolume;
                if (this.mRttVolume != 0) {
                    this.mAvgRtt = this.mRttProduct / this.mRttVolume;
                }
            }
        }
    }

    class WifiProDefaultState extends State {
        WifiProDefaultState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logD("WifiProDefaultState enter.");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_NETWORK_STATE_CHANGE /*135170*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_SUPPLICANT_STATE_CHANGE /*135172*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_WIFI_RADIO_STATE_CHANGE /*135173*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_BSSID_CHANGE /*135175*/:
                case HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH /*135179*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_NOT_DATA_LINK /*135199*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_DATA_LINK /*135200*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK /*135201*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_DATA_LINK /*135202*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_HIGH_NET_SPEED_DETECT_MSG /*136170*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT /*136171*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_RSSI_CHANGE /*136172*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_GET_5G_AP_RSSI_THRESHOLD /*136268*/:
                case HuaweiWifiWatchdogStateMachine.EVENT_GET_AP_HISTORY_QUALITY_SCORE /*136269*/:
                case 151573:
                case 151574:
                    break;
                default:
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.logE("Unhandled message " + msg + " in state " + HuaweiWifiWatchdogStateMachine.this.getCurrentState().getName());
                    break;
            }
            return true;
        }
    }

    class WifiProLinkMonitoringState extends State {
        private static final int DEFAULT_NET_DISABLE_DETECT_COUNT = 2;
        private static final int HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT = 3;
        private static final int HOMEAP_LEVEL_THREE_NET_DISABLE_DETECT_COUNT = 5;
        private static final int HOMEAP_LEVEL_TWO_NET_DISABLE_DETECT_COUNT = 4;
        private int adjustRssiCounter;
        private int goodPeriodCounter = 0;
        private int goodPeriodRxPkt = 0;
        private int homeAPAddPeriodCount = 0;
        private boolean isFirstEnterMontoringState = true;
        boolean isLastOTABad = false;
        boolean isLastRssiBad = false;
        boolean isLastTCPBad = false;
        private boolean isMaybePoorSend = false;
        private boolean isPoorLinkReported = false;
        private long lastRSSIUpdateTime = 0;
        private float mGoodSpeedRate = 1.0f;
        private float mHomeApSwichRate = 1.0f;
        private boolean mIsHomeApSwitchRateReaded = false;
        private int mLMRssiWaitCount = 0;
        private int mLastDnsFailCount;
        private int mLastRxGood;
        private int mLastTxBad;
        private int mLastTxGood;
        private int mNetworkDisableCount;
        private int mPktChkBadCnt;
        private int mPktChkCnt;
        private int mPktChkRxgood;
        private int mPktChkTxbad;
        private int mPktChkTxgood;
        private int mRssiBadCount;
        private float mRssiRate = 1.0f;
        private float mSwitchScore = 0.0f;
        private boolean networkBadDetected = false;
        private float noHomeAPSwitchScore = 0.0f;

        WifiProLinkMonitoringState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState enter. ");
            this.mRssiBadCount = 0;
            this.mLMRssiWaitCount = 0;
            HuaweiWifiWatchdogStateMachine.this.sendMessage(HuaweiWifiWatchdogStateMachine.this.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, HuaweiWifiWatchdogStateMachine.access$2704(HuaweiWifiWatchdogStateMachine.this), 0));
            boolean unused = HuaweiWifiWatchdogStateMachine.this.mWpLinkMonitorRunning = true;
            this.mPktChkTxbad = 0;
            this.mPktChkTxgood = 0;
            this.mPktChkRxgood = 0;
            this.mPktChkCnt = 0;
            this.mPktChkBadCnt = 0;
            this.mLastDnsFailCount = 0;
            this.mNetworkDisableCount = 0;
            int unused2 = HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount = 0;
            HuaweiWifiWatchdogStateMachine.this.monitorNetworkQos(true);
            this.isLastRssiBad = false;
            this.isLastOTABad = false;
            this.isLastTCPBad = false;
            this.adjustRssiCounter = 0;
            TcpChkResult unused3 = HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult = new TcpChkResult();
            resetPoorRssiTh();
            int unused4 = HuaweiWifiWatchdogStateMachine.this.mRssiTHTimeoutToken = HuaweiWifiWatchdogStateMachine.this.mRssiTHTimeoutToken + 1;
            int unused5 = HuaweiWifiWatchdogStateMachine.this.mHighSpeedToken = HuaweiWifiWatchdogStateMachine.this.mHighSpeedToken + 1;
            int unused6 = HuaweiWifiWatchdogStateMachine.this.mCurrRssi = HuaweiWifiWatchdogStateMachine.INVALID_RSSI;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logD("new conn, poor rssi th :" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH);
            this.mSwitchScore = 0.0f;
            this.noHomeAPSwitchScore = 0.0f;
            this.homeAPAddPeriodCount = 0;
            this.networkBadDetected = false;
            this.mRssiRate = 1.0f;
            ssidChangeDetection();
            this.isMaybePoorSend = false;
            this.isPoorLinkReported = false;
            this.goodPeriodCounter = 0;
            this.goodPeriodRxPkt = 0;
            int unused7 = HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = 0;
            int unused8 = HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = 0;
            long unused9 = HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod = 0;
            int unused10 = HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken = HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken + 1;
            this.lastRSSIUpdateTime = 0;
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine != null) {
                HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine.resetSampleRtt();
            }
            this.mIsHomeApSwitchRateReaded = false;
            this.mHomeApSwichRate = 1.0f;
            this.isFirstEnterMontoringState = true;
            HwUidTcpMonitor.getInstance(HuaweiWifiWatchdogStateMachine.this.mContext).notifyWifiMonitorEnabled(true);
        }

        public void exit() {
            HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState exit.");
            HuaweiWifiWatchdogStateMachine.this.monitorNetworkQos(false);
            boolean unused = HuaweiWifiWatchdogStateMachine.this.mWpLinkMonitorRunning = false;
            long unused2 = HuaweiWifiWatchdogStateMachine.this.mSwitchOutTime = SystemClock.elapsedRealtime();
            HwUidTcpMonitor.getInstance(HuaweiWifiWatchdogStateMachine.this.mContext).notifyWifiMonitorEnabled(false);
        }

        private void resetPeriodState() {
            this.mPktChkCnt = 0;
            this.mPktChkTxbad = 0;
            this.mPktChkTxgood = 0;
            this.mPktChkRxgood = 0;
            int unused = HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel = 0;
            this.mRssiBadCount = 0;
            long unused2 = HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod = 0;
        }

        private void networkGoodDetect(int tcpRxPkt, int crssi, int avgrtt, boolean speedGood) {
            if (this.isPoorLinkReported) {
                boolean isRssiBetter = crssi - HuaweiWifiWatchdogStateMachine.this.mGoodDetectBaseRssi >= 15;
                boolean isRttGood = avgrtt > 0 && avgrtt <= 1200;
                if (isRssiBetter || isRttGood || speedGood) {
                    this.goodPeriodRxPkt += tcpRxPkt;
                    int i = this.goodPeriodCounter + 1;
                    this.goodPeriodCounter = i;
                    if (i >= 2 && this.goodPeriodRxPkt >= 1) {
                        HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(true, 3);
                        HuaweiWifiWatchdogStateMachine.this.logI("link good reported, good base rssi:" + HuaweiWifiWatchdogStateMachine.this.mGoodDetectBaseRssi);
                        this.goodPeriodRxPkt = 0;
                        this.goodPeriodCounter = 0;
                    }
                }
            }
        }

        private void tryReportHomeApChr() {
            if (this.networkBadDetected) {
                if (HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager != null) {
                    HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.increaseHomeAPAddRoPeriodCnt(this.homeAPAddPeriodCount);
                }
                this.networkBadDetected = false;
            }
            this.homeAPAddPeriodCount = 0;
            this.noHomeAPSwitchScore = 0.0f;
        }

        private void resetPoorNetState() {
            tryReportHomeApChr();
            this.mPktChkBadCnt = 0;
            this.mSwitchScore = 0.0f;
            this.isMaybePoorSend = false;
        }

        private boolean isNetSpeedOk(int avgRtt) {
            boolean isSpeedOk = false;
            if (HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod) {
                isSpeedOk = true;
                tryBackOffScore(HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod, avgRtt);
            }
            boolean unused = HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod = false;
            return isSpeedOk;
        }

        private boolean detectNetworkAvailable(int tcpTxPkts, int tcpRxPkts, int tcpRetransPkts, String currentBssid) {
            int currentDnsFailCount = 0;
            String dnsFailCountStr = SystemProperties.get(HwSelfCureUtils.DNS_MONITOR_FLAG, "0");
            int detectCountRequirement = 2;
            if (dnsFailCountStr == null) {
                HuaweiWifiWatchdogStateMachine.this.logE("detectNetworkAvailable null point error.");
                return true;
            }
            try {
                currentDnsFailCount = Integer.parseInt(dnsFailCountStr);
            } catch (NumberFormatException e) {
                HuaweiWifiWatchdogStateMachine.this.logE("detectNetworkAvailable  parseInt err:" + e);
            }
            if (tcpRxPkts != 0) {
                this.mNetworkDisableCount = 0;
            } else if (tcpTxPkts > 2) {
                this.mNetworkDisableCount++;
            } else if (currentDnsFailCount - this.mLastDnsFailCount >= 2) {
                this.mNetworkDisableCount++;
            }
            this.mLastDnsFailCount = currentDnsFailCount;
            if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager != null && this.mNetworkDisableCount >= 2) {
                this.mHomeApSwichRate = HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.getHomeApSwitchRate(currentBssid);
                this.mIsHomeApSwitchRateReaded = true;
                detectCountRequirement = this.mHomeApSwichRate <= 0.3f ? 3 : this.mHomeApSwichRate <= 0.5f ? 3 : this.mHomeApSwichRate <= 0.7f ? 3 : 2;
            }
            if (this.mNetworkDisableCount >= detectCountRequirement) {
                HuaweiWifiWatchdogStateMachine.this.logI("detectNetworkAvailable: mNetworkDisableCount = " + this.mNetworkDisableCount);
                return false;
            }
            if (this.mNetworkDisableCount == 2 && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < -75) {
                HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, -2);
                HuaweiWifiWatchdogStateMachine.this.logI("report maybe poor for detecting network disable 2 periods");
            }
            return true;
        }

        public void resetPoorRssiTh() {
            int unused = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH = -75;
            int unused2 = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = -75;
            int unused3 = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH = -75;
        }

        private void sendTHTimeOutDelayMsg(int delayTime) {
            if (HuaweiWifiWatchdogStateMachine.this.getHandler().hasMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT)) {
                HuaweiWifiWatchdogStateMachine.this.logD("remove old RSSI_TH_TIMEOUT msg.");
                HuaweiWifiWatchdogStateMachine.this.getHandler().removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT);
            }
            HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.this.obtainMessage(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT, HuaweiWifiWatchdogStateMachine.access$4204(HuaweiWifiWatchdogStateMachine.this), 0), (long) delayTime);
        }

        private void resetRssiTHValue(int newTHRssi) {
            if (newTHRssi >= -75) {
                resetPoorRssiTh();
                if (HuaweiWifiWatchdogStateMachine.this.getHandler().hasMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT)) {
                    HuaweiWifiWatchdogStateMachine.this.logD("resetRssiTHValue force reset threshold.");
                    HuaweiWifiWatchdogStateMachine.this.getHandler().removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT);
                }
                return;
            }
            int unused = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH = newTHRssi;
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH < HuaweiWifiWatchdogStateMachine.POOR_NET_MIN_RSSI_TH) {
                HuaweiWifiWatchdogStateMachine.this.logD("TH set to minimum -85");
                int unused2 = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH = HuaweiWifiWatchdogStateMachine.POOR_NET_MIN_RSSI_TH;
            }
            int unused3 = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH - 1;
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                int unused4 = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH;
            }
        }

        private void updatePoorNetRssiTH(int currRssi) {
            if (currRssi != HuaweiWifiWatchdogStateMachine.INVALID_RSSI) {
                if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH < -75) {
                    if (currRssi <= HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                        int unused = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH;
                        resetRssiTHValue(currRssi);
                        HuaweiWifiWatchdogStateMachine.access$5312(HuaweiWifiWatchdogStateMachine.this, 30000);
                        if (HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime > 300000) {
                            int unused2 = HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = 300000;
                        }
                        sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.logI("new HIGH SPEED update th rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + ", rth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", nth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext + ", vtime: " + HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                    } else if (currRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext) {
                        int unused3 = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = currRssi;
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine2.logI("new HIGH SPEED update nth to rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
                    }
                } else if (currRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                    int unused4 = HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = 180000;
                    resetRssiTHValue(currRssi);
                    int unused5 = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = -75;
                    sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine3.logI("new HIGH SPEED TH rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + ", rth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", nth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext + ", vtime: " + HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                }
            }
        }

        private void rssiTHValidTimeout() {
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext < -75) {
                resetRssiTHValue(HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
                int unused = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = -75;
                int unused2 = HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = 180000;
                sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("new HIGH SPEED TH timeout, turn to next:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH);
                return;
            }
            resetPoorRssiTh();
            HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED TH timeout reset.");
        }

        private void checkRssiTHBackoff(int currRssi, double tcpRetransRate, int tcpTxPkts, int tcpRetransPkts, int tcpRtt) {
            boolean isRttbad = false;
            boolean isTcpRetranBad = (tcpRetransRate >= 0.4d && tcpTxPkts > 3) || (tcpTxPkts <= 3 && tcpRetransPkts >= 3);
            if (tcpRtt > 2000 || tcpRtt == 0) {
                isRttbad = true;
            }
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH <= currRssi && isTcpRetranBad && isRttbad) {
                resetRssiTHValue(HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + 5);
                HuaweiWifiWatchdogStateMachine.this.logI("after backoff rssi th: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", next TH:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
            }
        }

        private float getRssiSWRate(int rssi) {
            int len = HuaweiWifiWatchdogStateMachine.RSSI_RATE_TBL.length;
            float retRate = 1.0f;
            if (rssi < -75) {
                return 1.0f;
            }
            int i = 0;
            while (true) {
                if (i >= len) {
                    break;
                } else if (rssi >= HuaweiWifiWatchdogStateMachine.RSSI_RATE_TBL[i].RSSI_VAL) {
                    retRate = HuaweiWifiWatchdogStateMachine.RSSI_RATE_TBL[i].SW_RATE;
                    break;
                } else {
                    i++;
                }
            }
            return retRate;
        }

        private void ssidChangeDetection() {
            if (HuaweiWifiWatchdogStateMachine.this.mCurrSSID == null || HuaweiWifiWatchdogStateMachine.this.mWifiInfo == null) {
                HuaweiWifiWatchdogStateMachine.this.logD("SSID is null, reset SGC.");
                resetSpdGoodCounter();
            } else if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID() == null || HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID().equals(HuaweiWifiWatchdogStateMachine.this.mCurrSSID)) {
                spdGoodParameterAge(HuaweiWifiWatchdogStateMachine.this.mSwitchOutTime);
            } else {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("SSID change to:" + HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID() + ", old SSID: " + HuaweiWifiWatchdogStateMachine.this.mCurrSSID + ", reset SGC.");
                resetSpdGoodCounter();
            }
        }

        private void resetSpdGoodCounter() {
            this.mGoodSpeedRate = 1.0f;
            float unused = HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore = 0.0f;
            float unused2 = HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = 0.0f;
            int unused3 = HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
        }

        private void spdGoodParameterAge(long swoTime) {
            long reduceCount = 0;
            if (swoTime > 0) {
                reduceCount = (SystemClock.elapsedRealtime() - swoTime) / 64000;
                if (((float) reduceCount) >= HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount || reduceCount < 0) {
                    float unused = HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = 0.0f;
                } else {
                    HuaweiWifiWatchdogStateMachine.access$5624(HuaweiWifiWatchdogStateMachine.this, (float) reduceCount);
                }
                float unused2 = HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore = 0.0f;
                int unused3 = HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logD("SSID: " + HuaweiWifiWatchdogStateMachine.this.mCurrSSID + "not changed, not reset SGC. reduceCount:" + reduceCount + ", SGC:" + HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount);
        }

        private float getSpdGoodSWRate(float count) {
            int len = HuaweiWifiWatchdogStateMachine.GOOD_SPD_COUNT_RATE_TBL.length;
            for (int i = 0; i < len; i++) {
                if (count > HuaweiWifiWatchdogStateMachine.GOOD_SPD_COUNT_RATE_TBL[i].GOOD_COUNT) {
                    return HuaweiWifiWatchdogStateMachine.GOOD_SPD_COUNT_RATE_TBL[i].SW_RATE;
                }
            }
            return 1.0f;
        }

        private void updateSpdGoodCounter() {
            if (HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore > 0.0f) {
                if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount < HuaweiWifiWatchdogStateMachine.MAX_SPD_GOOD_COUNT) {
                    HuaweiWifiWatchdogStateMachine.access$5616(HuaweiWifiWatchdogStateMachine.this, HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore);
                    if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount > HuaweiWifiWatchdogStateMachine.MAX_SPD_GOOD_COUNT) {
                        float unused = HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = HuaweiWifiWatchdogStateMachine.MAX_SPD_GOOD_COUNT;
                    }
                }
                float unused2 = HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore = 0.0f;
                int unused3 = HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
            } else if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount > 0.0f) {
                int unused4 = HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount + 1;
                if (((float) HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount) >= HuaweiWifiWatchdogStateMachine.BACKOFF_NOT_SPD_GOOD_PERIOD_COUNT) {
                    float unused5 = HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount - 1.0f;
                    if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount < 0.0f) {
                        float unused6 = HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = 0.0f;
                    }
                    int unused7 = HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.logD("spd good count backoff 1 to:" + HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount);
                }
            }
            this.mGoodSpeedRate = getSpdGoodSWRate(HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount);
        }

        private float getCurrPeriodScore(int rssi, boolean rssiTcpBad, boolean otaBad, boolean tcpBad, boolean veryBadRTT, String currentBssid) {
            float homeApRate;
            int i = rssi;
            float rssiBadScore = 0.0f;
            float otaBadScore = 0.0f;
            float tcpBadScore = 0.0f;
            float veryBadRTTScore = 0.0f;
            if (this.mIsHomeApSwitchRateReaded) {
                homeApRate = this.mHomeApSwichRate;
                String str = currentBssid;
            } else {
                homeApRate = HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.getHomeApSwitchRate(currentBssid);
            }
            if (veryBadRTT) {
                veryBadRTTScore = 3.0f;
            }
            if (rssiTcpBad) {
                rssiBadScore = 5.0f;
            }
            if (otaBad) {
                otaBadScore = HuaweiWifiWatchdogStateMachine.MAY_BE_POOR_TH;
            }
            if (tcpBad) {
                tcpBadScore = 3.0f;
            }
            float biggestScore = rssiBadScore;
            if (otaBadScore > biggestScore) {
                biggestScore = otaBadScore;
            }
            if (tcpBadScore > biggestScore) {
                biggestScore = tcpBadScore;
            }
            if (veryBadRTTScore > biggestScore) {
                biggestScore = veryBadRTTScore;
            }
            this.mRssiRate = getRssiSWRate(rssi);
            float retScore = this.mRssiRate * biggestScore;
            if (1.0f != HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate) {
                retScore *= HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate;
            }
            if (1.0f != this.mGoodSpeedRate && this.mPktChkBadCnt == 0) {
                retScore *= this.mGoodSpeedRate;
            }
            this.noHomeAPSwitchScore += retScore;
            if (1.0f != homeApRate && i >= -75) {
                retScore *= homeApRate;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            StringBuilder sb = new StringBuilder();
            float f = rssiBadScore;
            sb.append("Get rate: Rssi");
            sb.append(i);
            sb.append(", RssiRate:");
            sb.append(this.mRssiRate);
            sb.append(", HighDataFlow:");
            sb.append(HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate);
            sb.append(", History: ");
            sb.append(this.mGoodSpeedRate);
            sb.append(", add score: ");
            float f2 = otaBadScore;
            float f3 = tcpBadScore;
            sb.append(HuaweiWifiWatchdogStateMachine.this.formatFloatToStr((double) retScore));
            sb.append(", Homerate: ");
            sb.append(homeApRate);
            sb.append(", mPktChkBadCnt:");
            sb.append(this.mPktChkBadCnt);
            huaweiWifiWatchdogStateMachine.logI(sb.toString());
            return retScore;
        }

        private void updateHighDataFlowRate() {
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario != 1 || HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection <= 0) {
                if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == 2 && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 0) {
                    if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 50) {
                        float unused = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 0.0f;
                    } else {
                        float unused2 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 0.3f + (((float) (50 - HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection)) * HuaweiWifiWatchdogStateMachine.STREAMING_STEP_RATE);
                    }
                }
            } else if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 20) {
                float unused3 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 0.0f;
            } else {
                float unused4 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 0.6f + (((float) (20 - HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection)) * HuaweiWifiWatchdogStateMachine.DOWNLOAD_STEP_RATE);
            }
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate > 1.0f || HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate < 0.0f) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("wrong rate! mHighDataFlowRate  = %d" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate);
                float unused5 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 1.0f;
            }
        }

        private void handleHighDataFlow() {
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 0) {
                int unused = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection - 4;
                if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection < 0) {
                    int unused2 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 0;
                }
                if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection == 0) {
                    int unused3 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 0;
                    int unused4 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter = 0;
                    float unused5 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 1.0f;
                    HuaweiWifiWatchdogStateMachine.this.logI("mHighDataFlowProtection = 0, reset to HIGH_DATA_FLOW_NONE");
                }
            }
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowPeriodCounter == 0) {
                long unused6 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes = TrafficStats.getRxBytes(HuaweiWifiWatchdogStateMachine.WLAN_IFACE);
            }
            int unused7 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowPeriodCounter = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowPeriodCounter + 1;
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowPeriodCounter % 4 == 0) {
                long currentHighDataFlowRxBytes = TrafficStats.getRxBytes(HuaweiWifiWatchdogStateMachine.WLAN_IFACE);
                long highDataFlowRxBytes = currentHighDataFlowRxBytes - HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes;
                long unused8 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes = currentHighDataFlowRxBytes;
                int lastScenario = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario;
                if (highDataFlowRxBytes >= 3145728) {
                    switch (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario) {
                        case 0:
                            if (highDataFlowRxBytes >= 5242880) {
                                int unused9 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 1;
                                int unused10 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 40;
                            } else if (HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow) {
                                int unused11 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 1;
                                int unused12 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 40;
                            }
                            boolean unused13 = HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow = true;
                            break;
                        case 1:
                            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter < 2) {
                                int unused14 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 40;
                                break;
                            } else {
                                int unused15 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 2;
                                int unused16 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.LONGEST_STREAMING_PROTECTION;
                                break;
                            }
                        case 2:
                            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter >= 2) {
                                int unused17 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.LONGEST_STREAMING_PROTECTION;
                                break;
                            }
                            break;
                        default:
                            int unused18 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 0;
                            HuaweiWifiWatchdogStateMachine.this.logI("wrong high data scenario, reset to HIGH_DATA_FLOW_NONE ");
                            break;
                    }
                    int unused19 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter = 0;
                } else {
                    boolean unused20 = HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow = false;
                    int unused21 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter + 1;
                }
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logD("high data flow: protection_counter = " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection + ",  not_detect_counter = " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter);
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.logD("high data flow scenario: " + lastScenario + " --> " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario + " rx bytes =" + (highDataFlowRxBytes / 1024) + "KB");
            }
            updateHighDataFlowRate();
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == 2 && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate >= 0.6f) {
                int unused22 = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 1;
            }
        }

        private void debugToast(int mrssi) {
            String scenario;
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == 1) {
                scenario = "DOWN";
            } else if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == 2) {
                scenario = "STREAM";
            } else {
                scenario = "NONE";
            }
            Context access$4700 = HuaweiWifiWatchdogStateMachine.this.mContext;
            Toast.makeText(access$4700, "RSSI:" + mrssi + " TotalScore:" + this.mSwitchScore + "  BadCount:" + this.mPktChkBadCnt + "\n" + scenario + ": Protection:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection + " Rate:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate, 1).show();
        }

        private void updateQoeLevel(int level) {
            if (level != 0) {
                if (level == HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel) {
                    int unused = HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                    return;
                }
                if (level == 2 && HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel != 3) {
                    int unused2 = HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = level;
                    int unused3 = HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                } else if (Math.abs(level - HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel) == 2) {
                    int unused4 = HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = 2;
                    int unused5 = HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                } else if (HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel == level) {
                    int unused6 = HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = level;
                    int unused7 = HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                } else {
                    int unused8 = HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                    return;
                }
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logD("Leave updateQoeLevel level = " + HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel);
                HuaweiWifiWatchdogStateMachine.this.postEvent(10, 1, HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel);
            }
        }

        private void computeQosLevel(int tcp_rtt, int tcp_rtt_pkts, int rssi, boolean isLossRateBad) {
            int speedLevel;
            int rttLevel;
            int qosLevel;
            if (HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod > 358400) {
                speedLevel = 3;
            } else if (HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod > 20480) {
                speedLevel = 2;
            } else {
                speedLevel = 0;
            }
            if (tcp_rtt_pkts < HuaweiWifiWatchdogStateMachine.this.QOE_LEVEL_RTT_MIN_PKT || tcp_rtt == 0) {
                rttLevel = 0;
            } else if (tcp_rtt <= 1200) {
                rttLevel = 3;
            } else if (tcp_rtt <= 4800) {
                rttLevel = 2;
            } else {
                rttLevel = 1;
            }
            if (rttLevel >= speedLevel) {
                qosLevel = rttLevel;
            } else if (speedLevel > HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel) {
                qosLevel = speedLevel;
            } else {
                qosLevel = 0;
            }
            if (rssi < HuaweiWifiWatchdogStateMachine.this.RSSI_TH_FOR_BAD_QOE_LEVEL && (qosLevel > 1 || qosLevel == 0)) {
                qosLevel = 1;
            } else if (rssi < HuaweiWifiWatchdogStateMachine.this.RSSI_TH_FOR_NOT_BAD_QOE_LEVEL && (qosLevel > 2 || qosLevel == 0)) {
                qosLevel = 2;
                if (isLossRateBad) {
                    qosLevel = 1;
                }
            }
            if (rssi >= -65 && qosLevel == 1) {
                qosLevel = 2;
            } else if (rssi >= HuaweiWifiWatchdogStateMachine.this.RSSI_TH_FOR_NOT_BAD_QOE_LEVEL && qosLevel == 0) {
                qosLevel = HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel;
            }
            if (qosLevel != 0) {
                updateQoeLevel(qosLevel);
            }
        }

        private void tryBackOffScore(long periodMaxSpeed, int avgRtt) {
            if (this.mSwitchScore > 0.0f) {
                boolean isScoreBackoffRTTGood = false;
                boolean isScoreBackoffSpeedGood = periodMaxSpeed > HidataWechatTraffic.AUDIO_TOTAL_TRAFFIC_WEALTHY;
                if (avgRtt > 0 && avgRtt <= 1200) {
                    isScoreBackoffRTTGood = true;
                }
                if (isScoreBackoffSpeedGood || this.mSwitchScore < 1.0f) {
                    resetPoorNetState();
                } else if (isScoreBackoffRTTGood) {
                    this.mSwitchScore *= 0.5f;
                    this.noHomeAPSwitchScore *= 0.5f;
                    HuaweiWifiWatchdogStateMachine.this.logD("Good RTT:score backoff newScore=" + this.mSwitchScore);
                } else {
                    this.mSwitchScore *= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_RATE;
                    this.noHomeAPSwitchScore *= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_RATE;
                    HuaweiWifiWatchdogStateMachine.this.logD("score backoff newSco=" + this.mSwitchScore);
                }
            } else {
                resetPoorNetState();
            }
        }

        private void handleRssiChanged(Message msg) {
            if (msg.arg2 == HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken) {
                long nowTime = SystemClock.elapsedRealtime();
                int newRssiVal = msg.arg1;
                if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi || nowTime - this.lastRSSIUpdateTime >= 4000) {
                    int unused = HuaweiWifiWatchdogStateMachine.this.mCurrRssi = newRssiVal;
                } else {
                    int unused2 = HuaweiWifiWatchdogStateMachine.this.mCurrRssi = ((HuaweiWifiWatchdogStateMachine.this.mCurrRssi * 1) + newRssiVal) / 2;
                }
                this.lastRSSIUpdateTime = nowTime;
                if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine != null) {
                    HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine.querySampleRtt();
                }
            }
        }

        private void handleRssiFetched(Message msg) {
            if (msg.arg1 == HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken) {
                if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid == null) {
                    HuaweiWifiWatchdogStateMachine.this.logD("MonitoringState WIFI not connected, skip fetch RSSI.");
                } else {
                    if (!HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk) {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.logI("WP Link Monitor State first check bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid) + ", call newLinkDetected");
                        HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.newLinkDetected();
                        HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk = true;
                    }
                    if (this.mLMRssiWaitCount <= 0) {
                        HuaweiWifiWatchdogStateMachine.this.mWsmChannel.sendMessage(151572);
                        this.mLMRssiWaitCount = 15;
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.logI("wait rssi request respond, not send new request.");
                        this.mLMRssiWaitCount = (int) (((long) this.mLMRssiWaitCount) - 8);
                    }
                }
                HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.this.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, HuaweiWifiWatchdogStateMachine.access$2704(HuaweiWifiWatchdogStateMachine.this), 0), HuaweiWifiWatchdogStateMachine.LINK_SAMPLING_INTERVAL_MS);
                return;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.logD("MonitoringState msg arg1:" + msg.arg1 + " != token:" + HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken + ", ignore this command.");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:255:0x079f, code lost:
            com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$8200(r8.this$0).mIPQLevel = (short) com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$4900(r8.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:256:0x07b4, code lost:
            if (com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$5500(r8.this$0) == null) goto L_0x07c4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:257:0x07b6, code lost:
            com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$8200(r8.this$0).mRO_APSsid = com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$5500(r8.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:258:0x07c4, code lost:
            com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$8200(r8.this$0).mHistoryQuilityRO_Rate = (short) ((int) (r8.mGoodSpeedRate * 1000.0f));
            com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$8200(r8.this$0).mHighDataRateRO_Rate = (short) ((int) (com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$5900(r8.this$0) * 1000.0f));
            com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$8200(r8.this$0).mCreditScoreRO_Rate = 1000;
            com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$3700(r8.this$0).setBQERoveOutReason(r14, r9, r11, r10, com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.access$8200(r8.this$0));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:281:0x0887, code lost:
            r1 = r69;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:301:0x0932, code lost:
            return true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0016, code lost:
            r1 = r9;
         */
        /* JADX WARNING: Removed duplicated region for block: B:150:0x03d7  */
        /* JADX WARNING: Removed duplicated region for block: B:151:0x03d9  */
        /* JADX WARNING: Removed duplicated region for block: B:153:0x03de  */
        /* JADX WARNING: Removed duplicated region for block: B:168:0x0440  */
        /* JADX WARNING: Removed duplicated region for block: B:171:0x044d  */
        /* JADX WARNING: Removed duplicated region for block: B:175:0x047b  */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x022b  */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0241  */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x0246  */
        public boolean processMessage(Message msg) {
            int tcpRetransPkts;
            int tcpRxPkts;
            boolean lastPeriodTcpResultValid;
            int tcpTxPkts;
            int tcpRetransPkts2;
            int tcpRxPkts2;
            int dbad;
            int txgood;
            double tcpRetransRate;
            boolean isRssiBad;
            boolean isPoorLinkForLowRssi;
            boolean isVeryBadRTT;
            int pTot;
            boolean isRssiBad2;
            int i;
            boolean isOTABad;
            boolean isOTABad2;
            boolean isTCPBad;
            boolean isVeryBadRTT2;
            double tcpRetransRate2;
            int tcpRxPkts3;
            int tcpRetransPkts3;
            int tcpTxPkts2;
            ProcessedTcpResult mProcessedTcpResult;
            int i2;
            int i3;
            int currentWiFiLevel;
            boolean isPoorLinkForLowRssi2;
            WifiProLinkMonitoringState wifiProLinkMonitoringState = this;
            Message message = msg;
            switch (message.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_BSSID_CHANGE /*135175*/:
                    Message message2 = message;
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProLinkMonitoringState);
                    break;
                case HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH /*135179*/:
                    Message message3 = message;
                    handleRssiFetched(msg);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_NOT_DATA_LINK /*135199*/:
                    Message message4 = message;
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState receive START_VERIFY_WITH_NOT_DATA_LINK cmd, start transition.");
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProVerifyingLinkState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_DATA_LINK /*135200*/:
                    Message message5 = message;
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState receive start data link monitor event, ignore.");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK /*135201*/:
                    Message message6 = message;
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState error receive STOP_VERIFY_WITH_NOT_DATA_LINK ignore.");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_DATA_LINK /*135202*/:
                    Message message7 = message;
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState receive STOP_VERIFY_WITH_DATA_LINK, stop now.");
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProStopVerifyState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_HIGH_NET_SPEED_DETECT_MSG /*136170*/:
                    if (HuaweiWifiWatchdogStateMachine.this.mHighSpeedToken == message.arg1) {
                        wifiProLinkMonitoringState.updatePoorNetRssiTH(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                        break;
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.logD(" have new high speed msg.");
                        break;
                    }
                case HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT /*136171*/:
                    if (HuaweiWifiWatchdogStateMachine.this.mRssiTHTimeoutToken == message.arg1) {
                        rssiTHValidTimeout();
                        break;
                    }
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_RSSI_CHANGE /*136172*/:
                    Message message8 = msg;
                    handleRssiChanged(msg);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY /*136270*/:
                    if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                        HuaweiWifiWatchdogStateMachine.this.logD("EVENT_STORE_HISTORY_QUALITY triggered!");
                        if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null) {
                            HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.storeHistoryQuality();
                        }
                        HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY, 1800000);
                        break;
                    }
                    break;
                case 151573:
                    HwUidTcpMonitor.getInstance(HuaweiWifiWatchdogStateMachine.this.mContext).updateUidTcpStatistics();
                    wifiProLinkMonitoringState.mLMRssiWaitCount = 0;
                    if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null && HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null && message.obj != null) {
                        RssiPacketCountInfo info = (RssiPacketCountInfo) message.obj;
                        HuaweiWifiWatchdogStateMachine.this.sendCurrentAPRssi(info.rssi);
                        int rssi = info.rssi;
                        int unused = HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken = HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken + 1;
                        long now = SystemClock.elapsedRealtime();
                        if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi || now - wifiProLinkMonitoringState.lastRSSIUpdateTime >= 4000) {
                            int unused2 = HuaweiWifiWatchdogStateMachine.this.mCurrRssi = rssi;
                        } else {
                            int unused3 = HuaweiWifiWatchdogStateMachine.this.mCurrRssi = ((HuaweiWifiWatchdogStateMachine.this.mCurrRssi * 1) + rssi) / 2;
                        }
                        wifiProLinkMonitoringState.lastRSSIUpdateTime = now;
                        if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine != null) {
                            HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine.querySampleRtt();
                            int unused4 = HuaweiWifiWatchdogStateMachine.this.mLastSampleRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                        }
                        int txbad = info.txbad;
                        int txgood2 = info.txgood;
                        int rxgood = info.rxgood;
                        int dbad2 = txbad - wifiProLinkMonitoringState.mLastTxBad;
                        int dgood = txgood2 - wifiProLinkMonitoringState.mLastTxGood;
                        int drxgood = rxgood - wifiProLinkMonitoringState.mLastRxGood;
                        int dtotal = dbad2 + dgood;
                        long unused5 = HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mLastTimeSample = now;
                        wifiProLinkMonitoringState.mLastTxBad = txbad;
                        wifiProLinkMonitoringState.mLastTxGood = txgood2;
                        wifiProLinkMonitoringState.mLastRxGood = rxgood;
                        wifiProLinkMonitoringState.mIsHomeApSwitchRateReaded = false;
                        if (now - HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mLastTimeSample < 16000) {
                            if (dtotal > 0) {
                                HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.updateLoss(rssi, ((double) dbad2) / ((double) dtotal), dtotal);
                            }
                            if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi <= HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi) {
                                wifiProLinkMonitoringState.mRssiBadCount++;
                            }
                            wifiProLinkMonitoringState.mPktChkTxbad += dbad2;
                            wifiProLinkMonitoringState.mPktChkTxgood += dgood;
                            wifiProLinkMonitoringState.mPktChkRxgood += drxgood;
                            wifiProLinkMonitoringState.mPktChkCnt++;
                            if (wifiProLinkMonitoringState.mPktChkCnt >= 1) {
                                int pTot2 = wifiProLinkMonitoringState.mPktChkTxgood + wifiProLinkMonitoringState.mPktChkTxbad;
                                if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null) {
                                    String unused6 = HuaweiWifiWatchdogStateMachine.this.mCurrSSID = HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID();
                                }
                                updateSpdGoodCounter();
                                handleHighDataFlow();
                                if (pTot2 > 0) {
                                    double lossRate = ((double) wifiProLinkMonitoringState.mPktChkTxbad) / ((double) pTot2);
                                    wifiProLinkMonitoringState = this;
                                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("POta txb txg rxg:");
                                    sb.append(wifiProLinkMonitoringState.mPktChkTxbad);
                                    sb.append(", ");
                                    sb.append(wifiProLinkMonitoringState.mPktChkTxgood);
                                    sb.append(", ");
                                    sb.append(wifiProLinkMonitoringState.mPktChkRxgood);
                                    sb.append(". Lr =");
                                    int dtotal2 = dtotal;
                                    int i4 = rxgood;
                                    sb.append(HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(lossRate * 100.0d));
                                    sb.append("% Totpkt=");
                                    sb.append(pTot2);
                                    huaweiWifiWatchdogStateMachine.logI(sb.toString());
                                    TcpChkResult mCurTcpChkResult = HuaweiWifiWatchdogStateMachine.this.getQueryTcpResult();
                                    ProcessedTcpResult mProcessedTcpResult2 = new ProcessedTcpResult();
                                    if (HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult == null) {
                                        tcpRxPkts = 0;
                                        HuaweiWifiWatchdogStateMachine.this.logD("LastMinTcpChkResult is null, new it.");
                                        tcpRetransPkts = 0;
                                        TcpChkResult unused7 = HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult = new TcpChkResult();
                                    } else {
                                        tcpRxPkts = 0;
                                        tcpRetransPkts = 0;
                                    }
                                    if (mCurTcpChkResult == null || HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult == null) {
                                        HuaweiWifiWatchdogStateMachine.this.logD("read TCP Result failed. not calc tcp 30s pkt.");
                                    } else {
                                        HuaweiWifiWatchdogStateMachine.this.getCurrTcpRtt(mProcessedTcpResult2);
                                        if (HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mValueIsSet) {
                                            lastPeriodTcpResultValid = true;
                                            tcpTxPkts = mCurTcpChkResult.mTcpTxPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpTxPkts;
                                            tcpRetransPkts2 = mCurTcpChkResult.mTcpRxPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpRxPkts;
                                            tcpRxPkts2 = mCurTcpChkResult.mTcpRetransPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpRetransPkts;
                                            if (mCurTcpChkResult != null) {
                                                HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.reset();
                                                HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.copyFrom(mCurTcpChkResult);
                                            }
                                            if (!wifiProLinkMonitoringState.isFirstEnterMontoringState) {
                                                wifiProLinkMonitoringState.isFirstEnterMontoringState = false;
                                                break;
                                            } else {
                                                if (tcpTxPkts > 0) {
                                                    dbad = dbad2;
                                                    txgood = txgood2;
                                                    TcpChkResult tcpChkResult = mCurTcpChkResult;
                                                    RssiPacketCountInfo rssiPacketCountInfo = info;
                                                    tcpRetransRate = ((double) tcpRxPkts2) / ((double) tcpTxPkts);
                                                } else {
                                                    dbad = dbad2;
                                                    txgood = txgood2;
                                                    TcpChkResult tcpChkResult2 = mCurTcpChkResult;
                                                    RssiPacketCountInfo rssiPacketCountInfo2 = info;
                                                    tcpRetransRate = 0.0d;
                                                }
                                                HuaweiWifiWatchdogStateMachine.this.logI("PTcp RTT:" + mProcessedTcpResult2.mAvgRtt + ", rtt pkt=" + mProcessedTcpResult2.mTotPkt + ", tcp_rx=" + tcpRetransPkts2 + ", tcp_tx=" + tcpTxPkts + ", tcp_reTran=" + tcpRxPkts2 + ", rtRate=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(tcpRetransRate));
                                                if (HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount > 0 && HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                                                    HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.addHistoryHSCount(HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount);
                                                    int unused8 = HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount = 0;
                                                }
                                                boolean isRttGood = false;
                                                if (mProcessedTcpResult2.mAvgRtt < 1000 && mProcessedTcpResult2.mTotPkt > 2 && lossRate < HuaweiWifiWatchdogStateMachine.PKT_CHK_POOR_LINK_MIN_LOSE_RATE && pTot2 > 8 && tcpRetransRate < 0.1d && tcpTxPkts > 2) {
                                                    isRttGood = true;
                                                }
                                                boolean isTCPBad2 = !isRttGood && (HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel == 1 || HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel == 2);
                                                if (wifiProLinkMonitoringState.mRssiBadCount >= 1) {
                                                    isRssiBad = true;
                                                } else {
                                                    isRssiBad = false;
                                                }
                                                int txbad2 = txbad;
                                                boolean isTcpRetranBad = (tcpRetransRate >= 0.2d && tcpTxPkts > 3) || (tcpTxPkts <= 3 && tcpRxPkts2 >= 3);
                                                boolean isPoorLinkForLowRssi3 = HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH && ((mProcessedTcpResult2.mAvgRtt > 2000 && mProcessedTcpResult2.mTotPkt > 2) || ((lossRate > 0.4d ? 1 : (lossRate == 0.4d ? 0 : -1)) >= 0 && pTot2 >= 50));
                                                boolean isWlanLossRateBad = lossRate > HuaweiWifiWatchdogStateMachine.PKT_CHK_POOR_LINK_MIN_LOSE_RATE && pTot2 > 8;
                                                if (!isPoorLinkForLowRssi3) {
                                                    isPoorLinkForLowRssi2 = isPoorLinkForLowRssi3;
                                                    int i5 = rssi;
                                                    if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.this.LOW_RSSI_TH && isWlanLossRateBad) {
                                                        isPoorLinkForLowRssi = true;
                                                        long j = now;
                                                        boolean isRttBadForCongestion = (mProcessedTcpResult2.mAvgRtt <= 3000 || mProcessedTcpResult2.mAvgRtt == 0) && (mProcessedTcpResult2.mInadequateAvgRtt > 1200 || mProcessedTcpResult2.mInadequateAvgRtt == 0);
                                                        boolean isWlanCongestion = (!isWlanLossRateBad && isRttBadForCongestion) || (isWlanLossRateBad && isTcpRetranBad);
                                                        boolean z = isRttBadForCongestion;
                                                        if (mProcessedTcpResult2.mAvgRtt > HuaweiWifiWatchdogStateMachine.VERY_BAD_RTT_TH_FOR_TCP_BAD || mProcessedTcpResult2.mTotPkt >= 50) {
                                                            isVeryBadRTT = false;
                                                        } else {
                                                            isVeryBadRTT = true;
                                                        }
                                                        boolean isOTABad3 = false;
                                                        if (isPoorLinkForLowRssi || isWlanCongestion) {
                                                            if (isRttGood) {
                                                                isOTABad3 = true;
                                                            } else {
                                                                isPoorLinkForLowRssi = false;
                                                                isWlanCongestion = false;
                                                            }
                                                        }
                                                        boolean isWlanCongestion2 = isWlanCongestion;
                                                        boolean isPoorLinkForLowRssi4 = isPoorLinkForLowRssi;
                                                        if (!isRssiBad) {
                                                            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                                                            isRssiBad2 = isRssiBad;
                                                            StringBuilder sb2 = new StringBuilder();
                                                            pTot = pTot2;
                                                            sb2.append("rssi bad, mrssi=");
                                                            sb2.append(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                                                            sb2.append(" < TargetRssi=");
                                                            sb2.append(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi);
                                                            huaweiWifiWatchdogStateMachine2.logD(sb2.toString());
                                                            if (isTCPBad2 || isOTABad3 || wifiProLinkMonitoringState.isLastTCPBad || wifiProLinkMonitoringState.isLastOTABad || tcpTxPkts <= 3 || tcpRetransPkts2 <= 0) {
                                                                i = 0;
                                                                wifiProLinkMonitoringState.adjustRssiCounter = 0;
                                                            } else {
                                                                wifiProLinkMonitoringState.adjustRssiCounter++;
                                                                if (wifiProLinkMonitoringState.adjustRssiCounter >= 8) {
                                                                    HuaweiWifiWatchdogStateMachine.this.adjustTargetPoorRssi(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                                                                    i = 0;
                                                                    wifiProLinkMonitoringState.adjustRssiCounter = 0;
                                                                } else {
                                                                    i = 0;
                                                                }
                                                            }
                                                        } else {
                                                            isRssiBad2 = isRssiBad;
                                                            pTot = pTot2;
                                                            i = 0;
                                                        }
                                                        if (wifiProLinkMonitoringState.isNetSpeedOk(mProcessedTcpResult2.mAvgRtt)) {
                                                            if (!lastPeriodTcpResultValid || wifiProLinkMonitoringState.detectNetworkAvailable(tcpTxPkts, tcpRetransPkts2, tcpRxPkts2, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid)) {
                                                                if (tcpTxPkts <= 2 && tcpRetransPkts2 <= 2) {
                                                                    wifiProLinkMonitoringState.computeQosLevel(mProcessedTcpResult2.mAvgRtt, mProcessedTcpResult2.mTotPkt, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, isWlanLossRateBad);
                                                                    break;
                                                                } else {
                                                                    if (isOTABad3 || isTCPBad2) {
                                                                        tcpRetransPkts3 = tcpRxPkts2;
                                                                        tcpRxPkts3 = tcpRetransPkts2;
                                                                        tcpTxPkts2 = tcpTxPkts;
                                                                        tcpRetransRate2 = tcpRetransRate;
                                                                        isVeryBadRTT2 = isVeryBadRTT;
                                                                        isOTABad = isOTABad3;
                                                                        int i6 = dtotal2;
                                                                        int i7 = txgood;
                                                                        int dtotal3 = dbad;
                                                                        int i8 = txbad2;
                                                                        isOTABad2 = isRssiBad2;
                                                                        isTCPBad = isTCPBad2;
                                                                        boolean z2 = isWlanLossRateBad;
                                                                        mProcessedTcpResult = mProcessedTcpResult2;
                                                                    } else if (isVeryBadRTT) {
                                                                        tcpRetransPkts3 = tcpRxPkts2;
                                                                        tcpRxPkts3 = tcpRetransPkts2;
                                                                        tcpTxPkts2 = tcpTxPkts;
                                                                        tcpRetransRate2 = tcpRetransRate;
                                                                        isVeryBadRTT2 = isVeryBadRTT;
                                                                        isOTABad = isOTABad3;
                                                                        int i9 = dtotal2;
                                                                        int i10 = txgood;
                                                                        int dtotal4 = dbad;
                                                                        int i11 = txbad2;
                                                                        isOTABad2 = isRssiBad2;
                                                                        isTCPBad = isTCPBad2;
                                                                        boolean z3 = isWlanLossRateBad;
                                                                        mProcessedTcpResult = mProcessedTcpResult2;
                                                                    } else {
                                                                        wifiProLinkMonitoringState.networkGoodDetect(tcpRetransPkts2, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, mProcessedTcpResult2.mAvgRtt, false);
                                                                        isOTABad = isOTABad3;
                                                                        int i12 = dtotal2;
                                                                        int tcpTxPkts3 = tcpTxPkts;
                                                                        int tcpRetransPkts4 = tcpRxPkts2;
                                                                        int i13 = dbad;
                                                                        isOTABad2 = isRssiBad2;
                                                                        double d = tcpRetransRate;
                                                                        double d2 = tcpRetransRate;
                                                                        int i14 = txgood;
                                                                        isTCPBad = isTCPBad2;
                                                                        int i15 = txbad2;
                                                                        boolean isVeryBadRTT3 = isVeryBadRTT;
                                                                        ProcessedTcpResult mProcessedTcpResult3 = mProcessedTcpResult2;
                                                                        wifiProLinkMonitoringState.checkRssiTHBackoff(HuaweiWifiWatchdogStateMachine.this.mCurrRssi, d, tcpTxPkts3, tcpRetransPkts4, mProcessedTcpResult2.mAvgRtt);
                                                                        wifiProLinkMonitoringState.computeQosLevel(mProcessedTcpResult3.mAvgRtt, mProcessedTcpResult3.mTotPkt, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, isWlanLossRateBad);
                                                                        wifiProLinkMonitoringState.tryBackOffScore(HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod, mProcessedTcpResult3.mAvgRtt);
                                                                        int i16 = pTot;
                                                                        int i17 = tcpTxPkts3;
                                                                        int i18 = tcpRetransPkts4;
                                                                        int i19 = tcpRetransPkts2;
                                                                        boolean z4 = isVeryBadRTT3;
                                                                        wifiProLinkMonitoringState.isLastRssiBad = isOTABad2;
                                                                        wifiProLinkMonitoringState.isLastOTABad = isOTABad;
                                                                        wifiProLinkMonitoringState.isLastTCPBad = isTCPBad;
                                                                    }
                                                                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                                                                    StringBuilder sb3 = new StringBuilder();
                                                                    sb3.append("rs ota tcp lr rttvb bad: ");
                                                                    sb3.append(isPoorLinkForLowRssi4);
                                                                    sb3.append(", ");
                                                                    sb3.append(isWlanCongestion2);
                                                                    sb3.append(", ");
                                                                    sb3.append(isTCPBad);
                                                                    sb3.append(", ");
                                                                    sb3.append(isOTABad2);
                                                                    sb3.append(", ");
                                                                    boolean isVeryBadRTT4 = isVeryBadRTT2;
                                                                    sb3.append(isVeryBadRTT4);
                                                                    sb3.append("; rsth:");
                                                                    sb3.append(HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH);
                                                                    huaweiWifiWatchdogStateMachine3.logI(sb3.toString());
                                                                    float retScore = wifiProLinkMonitoringState.getCurrPeriodScore(HuaweiWifiWatchdogStateMachine.this.mCurrRssi, isPoorLinkForLowRssi4, isWlanCongestion2, isTCPBad, isVeryBadRTT4, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid);
                                                                    wifiProLinkMonitoringState.mSwitchScore += retScore;
                                                                    wifiProLinkMonitoringState.mPktChkBadCnt++;
                                                                    wifiProLinkMonitoringState.updateQoeLevel(1);
                                                                    if (wifiProLinkMonitoringState.mPktChkBadCnt >= 2) {
                                                                        HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
                                                                        if (mHwWifiCHRService != null) {
                                                                            mHwWifiCHRService.updateWifiException(102, "WIFIPRO_WEB_SLOW");
                                                                        }
                                                                    }
                                                                    if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager != null && HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.getIsHomeAP(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid)) {
                                                                        wifiProLinkMonitoringState.networkBadDetected = true;
                                                                    }
                                                                    if (wifiProLinkMonitoringState.noHomeAPSwitchScore <= HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE || wifiProLinkMonitoringState.mSwitchScore > HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE) {
                                                                        i2 = 1;
                                                                    } else {
                                                                        i2 = 1;
                                                                        wifiProLinkMonitoringState.homeAPAddPeriodCount++;
                                                                    }
                                                                    if (wifiProLinkMonitoringState.mSwitchScore > HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE || wifiProLinkMonitoringState.homeAPAddPeriodCount > i2) {
                                                                        boolean isCurrentRssiBad = isOTABad2 && wifiProLinkMonitoringState.isLastRssiBad;
                                                                        if (isCurrentRssiBad) {
                                                                            int unused9 = HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 0;
                                                                            currentWiFiLevel = HuaweiWifiWatchdogStateMachine.this.wpPoorLinkLevelCalcByRssi(lossRate, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi, mProcessedTcpResult.mAvgRtt);
                                                                        } else {
                                                                            int unused10 = HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 1;
                                                                            currentWiFiLevel = HuaweiWifiWatchdogStateMachine.this.wpPoorLinkLevelCalcByTcp(lossRate, tcpRetransRate2, mProcessedTcpResult.mAvgRtt);
                                                                        }
                                                                        int currentWiFiLevel2 = currentWiFiLevel;
                                                                        int unused11 = HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                                        int unused12 = HuaweiWifiWatchdogStateMachine.this.mGoodDetectBaseRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                                        HuaweiWifiWatchdogStateMachine.this.logI("mWifiPoorRssi = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi + ", WifiPoorReason = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason);
                                                                        if (HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager == null || HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord == null) {
                                                                            boolean z5 = isCurrentRssiBad;
                                                                            int i20 = pTot;
                                                                            int i21 = tcpTxPkts2;
                                                                            int i22 = tcpRetransPkts3;
                                                                            int i23 = tcpRxPkts3;
                                                                            HuaweiWifiWatchdogStateMachine.this.logE("chr obj null error.");
                                                                        } else {
                                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.resetAllParameters();
                                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mRSSI_VALUE = (short) HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                                            int pTot3 = pTot;
                                                                            if (pTot3 > 8) {
                                                                                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mOTA_PacketDropRate = (short) ((int) (1000.0d * lossRate));
                                                                            }
                                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mRttAvg = (short) mProcessedTcpResult.mAvgRtt;
                                                                            int tcpRxPkts4 = tcpRxPkts3;
                                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpInSegs = (short) tcpRxPkts4;
                                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpOutSegs = (short) tcpTxPkts2;
                                                                            float f = retScore;
                                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpRetransSegs = (short) tcpRetransPkts3;
                                                                            synchronized (HuaweiWifiWatchdogStateMachine.this.mTraffic) {
                                                                                try {
                                                                                    boolean z6 = isCurrentRssiBad;
                                                                                    try {
                                                                                        int i24 = pTot3;
                                                                                        int i25 = tcpRxPkts4;
                                                                                    } catch (Throwable th) {
                                                                                        th = th;
                                                                                        int i26 = pTot3;
                                                                                        int i27 = tcpRxPkts4;
                                                                                        throw th;
                                                                                    }
                                                                                    try {
                                                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mWIFI_NetSpeed = (short) ((int) (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed / 1024));
                                                                                        break;
                                                                                    } catch (Throwable th2) {
                                                                                        th = th2;
                                                                                        throw th;
                                                                                    }
                                                                                } catch (Throwable th3) {
                                                                                    th = th3;
                                                                                    boolean z7 = isCurrentRssiBad;
                                                                                    int i28 = pTot3;
                                                                                    int i29 = tcpRxPkts4;
                                                                                    throw th;
                                                                                }
                                                                            }
                                                                        }
                                                                        i3 = 0;
                                                                        HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, currentWiFiLevel2);
                                                                        wifiProLinkMonitoringState.isPoorLinkReported = true;
                                                                        tryReportHomeApChr();
                                                                        wifiProLinkMonitoringState.mPktChkBadCnt = 0;
                                                                        wifiProLinkMonitoringState.mSwitchScore = 0.0f;
                                                                        wifiProLinkMonitoringState.isMaybePoorSend = false;
                                                                    } else {
                                                                        if (!wifiProLinkMonitoringState.isMaybePoorSend && ((wifiProLinkMonitoringState.mSwitchScore > HuaweiWifiWatchdogStateMachine.MAY_BE_POOR_TH || isPoorLinkForLowRssi4) && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.MAY_BE_POOR_RSSI_TH)) {
                                                                            HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, -2);
                                                                            wifiProLinkMonitoringState.isMaybePoorSend = true;
                                                                        }
                                                                        if (wifiProLinkMonitoringState.mPktChkBadCnt == 2 && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario != 0) {
                                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.increaseHighDataRateStopROC();
                                                                        }
                                                                        i3 = 0;
                                                                    }
                                                                    if (wifiProLinkMonitoringState.isPoorLinkReported) {
                                                                        wifiProLinkMonitoringState.goodPeriodCounter = i3;
                                                                    }
                                                                    wifiProLinkMonitoringState.isLastRssiBad = isOTABad2;
                                                                    wifiProLinkMonitoringState.isLastOTABad = isOTABad;
                                                                    wifiProLinkMonitoringState.isLastTCPBad = isTCPBad;
                                                                }
                                                            } else {
                                                                if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi >= -75) {
                                                                    HuaweiWifiWatchdogStateMachine.this.sendResultMsgToQM(WifiproUtils.REQUEST_WIFI_INET_CHECK);
                                                                    int unused13 = HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 1;
                                                                } else {
                                                                    HuaweiWifiWatchdogStateMachine.this.sendResultMsgToQM(WifiproUtils.REQUEST_POOR_RSSI_INET_CHECK);
                                                                    int unused14 = HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 0;
                                                                    int unused15 = HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                                }
                                                                int unused16 = HuaweiWifiWatchdogStateMachine.this.mGoodDetectBaseRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                                wifiProLinkMonitoringState.isPoorLinkReported = true;
                                                                wifiProLinkMonitoringState.goodPeriodCounter = 0;
                                                                break;
                                                            }
                                                        } else {
                                                            wifiProLinkMonitoringState.networkGoodDetect(1, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, i, true);
                                                            wifiProLinkMonitoringState.computeQosLevel(mProcessedTcpResult2.mAvgRtt, mProcessedTcpResult2.mTotPkt, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, isWlanLossRateBad);
                                                            resetPeriodState();
                                                            if (HuaweiWifiWatchdogStateMachine.DDBG_TOAST_DISPLAY) {
                                                                wifiProLinkMonitoringState.debugToast(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                                                            }
                                                            wifiProLinkMonitoringState.mNetworkDisableCount = 0;
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    isPoorLinkForLowRssi2 = isPoorLinkForLowRssi3;
                                                    int i30 = rssi;
                                                }
                                                isPoorLinkForLowRssi = isPoorLinkForLowRssi2;
                                                long j2 = now;
                                                if (mProcessedTcpResult2.mAvgRtt <= 3000) {
                                                }
                                                if (!isWlanLossRateBad) {
                                                }
                                                boolean z8 = isRttBadForCongestion;
                                                if (mProcessedTcpResult2.mAvgRtt > HuaweiWifiWatchdogStateMachine.VERY_BAD_RTT_TH_FOR_TCP_BAD) {
                                                }
                                                isVeryBadRTT = false;
                                                boolean isOTABad32 = false;
                                                if (isRttGood) {
                                                }
                                                boolean isWlanCongestion22 = isWlanCongestion;
                                                boolean isPoorLinkForLowRssi42 = isPoorLinkForLowRssi;
                                                if (!isRssiBad) {
                                                }
                                                if (wifiProLinkMonitoringState.isNetSpeedOk(mProcessedTcpResult2.mAvgRtt)) {
                                                }
                                            }
                                        } else {
                                            HuaweiWifiWatchdogStateMachine.this.logD("not calc TCP period pkt, last result is null.");
                                        }
                                    }
                                    lastPeriodTcpResultValid = false;
                                    tcpTxPkts = 0;
                                    tcpRetransPkts2 = tcpRxPkts;
                                    tcpRxPkts2 = tcpRetransPkts;
                                    if (mCurTcpChkResult != null) {
                                    }
                                    if (!wifiProLinkMonitoringState.isFirstEnterMontoringState) {
                                    }
                                } else {
                                    int i31 = rxgood;
                                    int i32 = dbad2;
                                    int i33 = txgood2;
                                    int i34 = txbad;
                                    int i35 = pTot2;
                                    RssiPacketCountInfo rssiPacketCountInfo3 = info;
                                    int i36 = rssi;
                                    long j3 = now;
                                    handleNoTxPeriodInLinkMonitor();
                                }
                                if (HuaweiWifiWatchdogStateMachine.DDBG_TOAST_DISPLAY) {
                                    wifiProLinkMonitoringState.debugToast(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                                }
                                resetPeriodState();
                                break;
                            }
                        }
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.logE("null error or wifi not connected, ignore RSSI_PKTCNT_FETCH event.");
                        break;
                    }
                    break;
                case 151574:
                    HuaweiWifiWatchdogStateMachine.this.logD("RSSI_FETCH_FAILED");
                    break;
                default:
                    Message message9 = message;
                    return false;
            }
        }

        private void handleNoTxPeriodInLinkMonitor() {
            String dnsFailCountStr = SystemProperties.get(HwSelfCureUtils.DNS_MONITOR_FLAG, "0");
            if (dnsFailCountStr != null) {
                int tmpDnsFailedCnt = 0;
                try {
                    tmpDnsFailedCnt = Integer.parseInt(dnsFailCountStr);
                } catch (NumberFormatException e) {
                    HuaweiWifiWatchdogStateMachine.this.logE("no any pkt, detectNetworkAvailable  parseInt err!");
                }
                if (tmpDnsFailedCnt > 0) {
                    if (this.mLastDnsFailCount > 0 && tmpDnsFailedCnt - this.mLastDnsFailCount >= 2) {
                        HuaweiWifiWatchdogStateMachine.this.sendResultMsgToQM(WifiproUtils.REQUEST_WIFI_INET_CHECK);
                        HuaweiWifiWatchdogStateMachine.this.logI("txbad/txgood is 0, detectNetwork dns failed!");
                    }
                    this.mLastDnsFailCount = tmpDnsFailedCnt;
                }
            }
        }
    }

    class WifiProStopVerifyState extends State {
        WifiProStopVerifyState() {
        }

        public void enter() {
            boolean unused = HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod = false;
            HuaweiWifiWatchdogStateMachine.this.resetHighDataFlow();
            HuaweiWifiWatchdogStateMachine.this.logD(" WifiProStopVerifyState enter.");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_NOT_DATA_LINK /*135199*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProStopVerifyState receive START_VERIFY_WITH_NOT_DATA_LINK cmd, start transition.");
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProVerifyingLinkState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_DATA_LINK /*135200*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProStopVerifyState receive START_VERIFY_WITH_DATA_LINK cmd, start transition.");
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProLinkMonitoringState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK /*135201*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProStopVerifyState rcv STOP_VERIFY_WITH_NOT_DATA_LINK ignore.");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_DATA_LINK /*135202*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProStopVerifyState rcv STOP_VERIFY_WITH_DATA_LINK ignore");
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class WifiProVerifyingLinkState extends State {
        private boolean mIsOtaPoorTimeOut;
        private boolean mIsReportWiFiGoodAllow;
        private int mRssiGoodCount;
        private int mVLRssiWaitCount = 0;

        WifiProVerifyingLinkState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logI("WifiProVerifyingLinkState enter. PoorReason = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason + " , PoorRssi = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi);
            this.mRssiGoodCount = 0;
            int unused = HuaweiWifiWatchdogStateMachine.this.mCurrRssi = HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi;
            this.mIsOtaPoorTimeOut = false;
            this.mIsReportWiFiGoodAllow = false;
            this.mVLRssiWaitCount = 0;
            HuaweiWifiWatchdogStateMachine.this.sendMessage(HuaweiWifiWatchdogStateMachine.this.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, HuaweiWifiWatchdogStateMachine.access$2704(HuaweiWifiWatchdogStateMachine.this), 0));
            HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_AVOID_TO_WIFI_DELAY_MSG, 30000);
            if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null) {
                if (1 == HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason) {
                    HuaweiWifiWatchdogStateMachine.this.logI("start delay verify.");
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.otaTcpPoorLinkDetected();
                    if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime < 120000 || HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime > HuaweiWifiWatchdogStateMachine.MAX_RESTORE_WIFI_TIME) {
                        HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime = 120000;
                    }
                    HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_DELAY_OTATCP_TIME_OUT_MSG, (long) HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime);
                } else if (HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason == 0) {
                    HuaweiWifiWatchdogStateMachine.this.logI("start RSSI verify.");
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.poorLinkDetected(HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi);
                }
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.logI("GoodLinkTargetRssi = " + HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetRssi + " , GoodLinkTargetCount =" + HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetCount);
            }
        }

        public void exit() {
            this.mIsReportWiFiGoodAllow = false;
            this.mIsOtaPoorTimeOut = false;
            HuaweiWifiWatchdogStateMachine.this.removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_DELAY_OTATCP_TIME_OUT_MSG);
            HuaweiWifiWatchdogStateMachine.this.removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_AVOID_TO_WIFI_DELAY_MSG);
            int unused = HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi = -90;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_BSSID_CHANGE /*135175*/:
                    break;
                case HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH /*135179*/:
                    if (msg.arg1 != HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken) {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.logD("VL msg.arg1:" + msg.arg1 + " != RssiFetchToken:" + HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken + ", ignore this command.");
                        break;
                    } else {
                        if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid == null) {
                            HuaweiWifiWatchdogStateMachine.this.logD("VL WIFI is not connected, not fetch RSSI.");
                        } else {
                            if (!HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk) {
                                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                                huaweiWifiWatchdogStateMachine2.logD("VL first check bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid) + ",call newLinkDetected");
                                HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.newLinkDetected();
                                HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk = true;
                            }
                            if (this.mVLRssiWaitCount <= 0) {
                                HuaweiWifiWatchdogStateMachine.this.mWsmChannel.sendMessage(151572);
                                this.mVLRssiWaitCount = 15;
                            } else {
                                HuaweiWifiWatchdogStateMachine.this.logI("vl wait rssi request respond, not send new request.");
                                this.mVLRssiWaitCount = (int) (((long) this.mVLRssiWaitCount) - 2);
                            }
                        }
                        HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.this.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, HuaweiWifiWatchdogStateMachine.access$2704(HuaweiWifiWatchdogStateMachine.this), 0), HuaweiWifiWatchdogStateMachine.ROVE_OUT_LINK_SAMPLING_INTERVAL_MS);
                        break;
                    }
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_NOT_DATA_LINK /*135199*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProVerifyingLinkState receive VERIFY_WITH_NOT_DATA_LINK, ignore.");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_DATA_LINK /*135200*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProVerifyingLinkState receive VERIFY_WITH_DATA_LINK cmd, start transition.");
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProLinkMonitoringState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK /*135201*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProVerifyingLinkState receive STOP_VERIFY_WITH_NOT_DATA_LINK, stop now.");
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProStopVerifyState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_DATA_LINK /*135202*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProVerifyingLinkState error receive STOP_VERIFY_WITH_DATA_LINK ignore");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_DELAY_OTATCP_TIME_OUT_MSG /*136168*/:
                    HuaweiWifiWatchdogStateMachine.this.logI("VL OtaPoorTimeOut = true.");
                    this.mIsOtaPoorTimeOut = true;
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_AVOID_TO_WIFI_DELAY_MSG /*136169*/:
                    HuaweiWifiWatchdogStateMachine.this.logI("VL ReportWiFiGoodAllow = true.");
                    this.mIsReportWiFiGoodAllow = true;
                    break;
                case 151573:
                    this.mVLRssiWaitCount = 0;
                    if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null && HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null && msg.obj != null) {
                        RssiPacketCountInfo info = (RssiPacketCountInfo) msg.obj;
                        HwWifiConnectivityMonitor monitor = HwWifiConnectivityMonitor.getInstance();
                        if (monitor != null) {
                            monitor.notifyBackgroundWifiLinkInfo(info.rssi, info.txgood, info.txbad, info.rxgood);
                        }
                        HuaweiWifiWatchdogStateMachine.this.sendCurrentAPRssi(info.rssi);
                        if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi) {
                            int unused = HuaweiWifiWatchdogStateMachine.this.mCurrRssi = info.rssi;
                        } else {
                            int unused2 = HuaweiWifiWatchdogStateMachine.this.mCurrRssi = (HuaweiWifiWatchdogStateMachine.this.mCurrRssi + info.rssi) / 2;
                        }
                        HuaweiWifiWatchdogStateMachine.this.countDownGoodRssiByTime();
                        if (1 != HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason) {
                            tryRecoverWifiByRssi(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                            break;
                        } else {
                            tryRecoverWifiByOta(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                            break;
                        }
                    } else {
                        RssiPacketCountInfo info2 = HuaweiWifiWatchdogStateMachine.this;
                        info2.logD("null error or WIFI is not connected, ignore packet fetch event. fg=" + HuaweiWifiWatchdogStateMachine.this.mIsNotStatic);
                        break;
                    }
                    break;
                case 151574:
                    HuaweiWifiWatchdogStateMachine.this.logD("RSSI_FETCH_FAILED");
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void tryRecoverWifiByRssi(int current_rssi) {
            if (this.mIsReportWiFiGoodAllow) {
                if (current_rssi >= HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetRssi) {
                    int i = this.mRssiGoodCount + 1;
                    this.mRssiGoodCount = i;
                    if (i >= HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetCount) {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.logI("tryRecoverByRssi, rssi=" + current_rssi + "dB, mRssiGoodCount =" + this.mRssiGoodCount);
                        this.mRssiGoodCount = 0;
                        if (HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager != null) {
                            HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.setBQERoveInReason(1);
                        }
                        HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(true, 3);
                    }
                } else {
                    this.mRssiGoodCount = 0;
                }
            }
        }

        private void tryRecoverWifiByOta(int current_rssi) {
            if (this.mIsReportWiFiGoodAllow) {
                int d_rssi = current_rssi - HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi;
                boolean isRssiIncrement = d_rssi >= 15;
                boolean isRoTimerTimeout = this.mIsOtaPoorTimeOut && d_rssi > HuaweiWifiWatchdogStateMachine.GOOD_LINK_JUDGE_SUB_VAL;
                if (isRssiIncrement || isRoTimerTimeout) {
                    int i = this.mRssiGoodCount + 1;
                    this.mRssiGoodCount = i;
                    if (i >= HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetCount) {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.logI("Recover By Ota, rssi=" + current_rssi + ", poor rssi=" + HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi + ", RssiGoodCount =" + this.mRssiGoodCount + ", OtaPoorTimeOut =" + this.mIsOtaPoorTimeOut);
                        this.mRssiGoodCount = 0;
                        if (HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager != null) {
                            if (isRssiIncrement) {
                                HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.setBQERoveInReason(2);
                            } else {
                                HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.setBQERoveInReason(3);
                            }
                        }
                        HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(true, 3);
                    }
                } else {
                    this.mRssiGoodCount = 0;
                }
            }
        }
    }

    class WifiProWatchdogDisabledState extends State {
        WifiProWatchdogDisabledState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logD("DisabledState enter.");
            boolean unused = HuaweiWifiWatchdogStateMachine.this.wifiOtaChkIsEnable = false;
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == HuaweiWifiWatchdogStateMachine.EVENT_NETWORK_STATE_CHANGE) {
                NetworkInfo networkInfo = (NetworkInfo) ((Intent) msg.obj).getParcelableExtra("networkInfo");
                if (!(networkInfo == null || HuaweiWifiWatchdogStateMachine.this.mWifiManager == null)) {
                    WifiInfo unused = HuaweiWifiWatchdogStateMachine.this.mWifiInfo = HuaweiWifiWatchdogStateMachine.this.mWifiManager.getConnectionInfo();
                    if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null) {
                        HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getBSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getNetworkId());
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, 0);
                    }
                    if (AnonymousClass3.$SwitchMap$android$net$NetworkInfo$DetailedState[networkInfo.getDetailedState().ordinal()] == 1) {
                        HuaweiWifiWatchdogStateMachine.this.logD(" rcv VERIFYING_POOR_LINK, do nothing.");
                    }
                }
            } else if (i == HuaweiWifiWatchdogStateMachine.EVENT_WP_WATCHDOG_ENABLE_CMD) {
                HuaweiWifiWatchdogStateMachine.this.logD("receive enable command, transition to WifiProStopVerifyState.");
                HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProStopVerifyState);
            } else if (i != HuaweiWifiWatchdogStateMachine.EVENT_WP_WATCHDOG_DISABLE_CMD) {
                return false;
            } else {
                HuaweiWifiWatchdogStateMachine.this.logD("receive disable command, ignore.");
            }
            return true;
        }
    }

    class WifiProWatchdogEnabledState extends State {
        WifiProWatchdogEnabledState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logD("EnabledState enter.");
            boolean unused = HuaweiWifiWatchdogStateMachine.this.wifiOtaChkIsEnable = true;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_NETWORK_STATE_CHANGE /*135170*/:
                    NetworkInfo networkInfo = (NetworkInfo) ((Intent) msg.obj).getParcelableExtra("networkInfo");
                    if (!(networkInfo == null || HuaweiWifiWatchdogStateMachine.this.mWifiManager == null)) {
                        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                            HuaweiWifiWatchdogStateMachine.this.logD("CAPTIVE_PORTAL_CHECK state, not call updateCurrentBssid.");
                            break;
                        } else {
                            WifiInfo unused = HuaweiWifiWatchdogStateMachine.this.mWifiInfo = HuaweiWifiWatchdogStateMachine.this.mWifiManager.getConnectionInfo();
                            if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo == null) {
                                HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, 0);
                                break;
                            } else {
                                HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getBSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getNetworkId());
                                break;
                            }
                        }
                    }
                case HuaweiWifiWatchdogStateMachine.EVENT_SUPPLICANT_STATE_CHANGE /*135172*/:
                    SupplicantState supplicantState = (SupplicantState) ((Intent) msg.obj).getParcelableExtra("newState");
                    if (supplicantState != SupplicantState.COMPLETED) {
                        if (supplicantState == SupplicantState.ASSOCIATING) {
                            WifiNative mWifiNative = WifiInjector.getInstance().getWifiNative();
                            if (mWifiNative != null) {
                                String apvendorinfo = mWifiNative.getApVendorInfo();
                                if (apvendorinfo != null) {
                                    HwWifiCHRService mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
                                    if (mHwWifiCHRService != null) {
                                        mHwWifiCHRService.updateAPVendorInfo(apvendorinfo);
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        WifiInfo unused2 = HuaweiWifiWatchdogStateMachine.this.mWifiInfo = HuaweiWifiWatchdogStateMachine.this.mWifiManager.getConnectionInfo();
                        if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo == null) {
                            HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, 0);
                            break;
                        } else {
                            HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getBSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getNetworkId());
                            break;
                        }
                    }
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_WIFI_RADIO_STATE_CHANGE /*135173*/:
                    if (msg.arg1 == 0) {
                        HuaweiWifiWatchdogStateMachine.this.logD("WIFI in DISABLING state.");
                        HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProStopVerifyState);
                        break;
                    }
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_WP_WATCHDOG_ENABLE_CMD /*135198*/:
                    HuaweiWifiWatchdogStateMachine.this.logD("receive enable command ignore.");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_WP_WATCHDOG_DISABLE_CMD /*135203*/:
                    HuaweiWifiWatchdogStateMachine.this.logD("receive disable command.");
                    if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager != null) {
                        HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.wifiproClose();
                    }
                    if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null && HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                        HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.storeHistoryQuality();
                    }
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProWatchdogDisabledState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_GET_5G_AP_RSSI_THRESHOLD /*136268*/:
                    if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                        HuaweiWifiWatchdogStateMachine.this.target5GApRssiTHProcess((WifiProEstimateApInfo) msg.obj);
                        break;
                    }
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_GET_AP_HISTORY_QUALITY_SCORE /*136269*/:
                    if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                        HuaweiWifiWatchdogStateMachine.this.targetApHistoryQualityScoreProcess((WifiProEstimateApInfo) msg.obj);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    static /* synthetic */ int access$2704(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mRssiFetchToken + 1;
        x0.mRssiFetchToken = i;
        return i;
    }

    static /* synthetic */ int access$4204(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mRssiTHTimeoutToken + 1;
        x0.mRssiTHTimeoutToken = i;
        return i;
    }

    static /* synthetic */ int access$504(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mHighSpeedToken + 1;
        x0.mHighSpeedToken = i;
        return i;
    }

    static /* synthetic */ int access$5312(HuaweiWifiWatchdogStateMachine x0, int x1) {
        int i = x0.mRssiTHValidTime + x1;
        x0.mRssiTHValidTime = i;
        return i;
    }

    static /* synthetic */ float access$5616(HuaweiWifiWatchdogStateMachine x0, float x1) {
        float f = x0.mSpeedGoodCount + x1;
        x0.mSpeedGoodCount = f;
        return f;
    }

    static /* synthetic */ float access$5624(HuaweiWifiWatchdogStateMachine x0, float x1) {
        float f = x0.mSpeedGoodCount - x1;
        x0.mSpeedGoodCount = f;
        return f;
    }

    static /* synthetic */ float access$616(HuaweiWifiWatchdogStateMachine x0, float x1) {
        float f = x0.mPeriodGoodSpdScore + x1;
        x0.mPeriodGoodSpdScore = f;
        return f;
    }

    /* access modifiers changed from: private */
    public float checkGoodSpd(int rssi, long spd) {
        if (spd < 30720 || rssi >= -65 || rssi == INVALID_RSSI) {
            return 0.0f;
        }
        int len = RSSI_GOOD_SPD_TBL.length;
        int i = 0;
        while (i < len) {
            if (rssi < RSSI_GOOD_SPD_TBL[i].RSSI_VAL) {
                i++;
            } else if (spd < ((long) RSSI_GOOD_SPD_TBL[i].SPD_VAL)) {
                return 0.0f;
            } else {
                float retScore = 0.5f + ((ADD_UNIT_SPD_SCORE * ((float) (spd - ((long) RSSI_GOOD_SPD_TBL[i].SPD_VAL)))) / ((float) RSSI_GOOD_SPD_TBL[i].UNIT_SPD_VAL));
                if (retScore > MAX_SCORE_ONE_CHECK) {
                    retScore = MAX_SCORE_ONE_CHECK;
                }
                logD("checkGoodSpd at rssi:" + rssi + ", spd:" + (spd / 1024) + "K > " + (RSSI_GOOD_SPD_TBL[i].SPD_VAL / 1024) + "K, unit:" + (RSSI_GOOD_SPD_TBL[i].UNIT_SPD_VAL / 1024) + "K, score:" + formatFloatToStr((double) retScore));
                return retScore;
            }
        }
        return 0.0f;
    }

    private HuaweiWifiWatchdogStateMachine(Context context, Messenger dstMessenger, Handler h) {
        super(TAG);
        this.mContext = context;
        this.mQMHandler = h;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mWsmChannel.connectSync(this.mContext, getHandler(), dstMessenger);
        setupNetworkReceiver();
        addState(this.mWifiProDefaultState);
        addState(this.mWifiProWatchdogDisabledState, this.mWifiProDefaultState);
        addState(this.mWifiProWatchdogEnabledState, this.mWifiProDefaultState);
        addState(this.mWifiProStopVerifyState, this.mWifiProWatchdogEnabledState);
        addState(this.mWifiProVerifyingLinkState, this.mWifiProWatchdogEnabledState);
        addState(this.mWifiProLinkMonitoringState, this.mWifiProWatchdogEnabledState);
        this.wifiOtaChkIsEnable = false;
        setInitialState(this.mWifiProWatchdogDisabledState);
        setLogRecSize(25);
        setLogOnlyTransitions(true);
        this.mTraffic = new TrafficMonitor(this.mSpeedUpdate, this.mContext);
        this.mFormatData = new DecimalFormat("#.##");
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
        this.mWifiProRoveOutParaRecord = new WifiProRoveOutParaRecord();
        this.mWifiProHistoryRecordManager = WifiProHistoryRecordManager.getInstance(this.mContext, this.mWifiManager);
        this.mIsDualbandEnable = true;
        if (this.mIsDualbandEnable) {
            this.mHwDualBandQualityEngine = new HwDualBandQualityEngine(context, h);
        }
    }

    public static HuaweiWifiWatchdogStateMachine makeHuaweiWifiWatchdogStateMachine(Context context, Messenger dstMessenger, Handler h) {
        HuaweiWifiWatchdogStateMachine wwsm = new HuaweiWifiWatchdogStateMachine(context, dstMessenger, h);
        wwsm.start();
        return wwsm;
    }

    /* access modifiers changed from: private */
    public void monitorNetworkQos(boolean enable) {
        if (enable) {
            if (!this.mIsMonitoring) {
                logD("monitorNetworkQos start speed track.");
                this.mTraffic.enableMonitor(true, 1);
                this.mTraffic.setExpireTime(HwMSSUtils.MSS_SYNC_AFT_CONNECTED);
                this.mIsMonitoring = true;
            }
        } else if (this.mIsMonitoring) {
            logD("monitorNetworkQos stop speed track.");
            this.mTraffic.enableMonitor(false, 1);
            this.mIsMonitoring = false;
        }
    }

    private void setupNetworkReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                    HuaweiWifiWatchdogStateMachine.this.sendMessage(HuaweiWifiWatchdogStateMachine.EVENT_SUPPLICANT_STATE_CHANGE, intent);
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    HuaweiWifiWatchdogStateMachine.this.sendMessage(HuaweiWifiWatchdogStateMachine.EVENT_NETWORK_STATE_CHANGE, intent);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    HuaweiWifiWatchdogStateMachine.this.sendMessage(HuaweiWifiWatchdogStateMachine.EVENT_WIFI_RADIO_STATE_CHANGE, intent.getIntExtra("wifi_state", 4));
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    int newRssiVal = intent.getIntExtra("newRssi", HuaweiWifiWatchdogStateMachine.INVALID_RSSI);
                    if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI != newRssiVal) {
                        int unused = HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken = HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken + 1;
                        HuaweiWifiWatchdogStateMachine.this.sendMessage(HuaweiWifiWatchdogStateMachine.this.obtainMessage(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_CHANGE, newRssiVal, HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken, null));
                    }
                }
            }
        };
    }

    private void registBroadcastReceiver() {
        if (!this.mIsRegister) {
            this.mIntentFilter = new IntentFilter();
            this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.mIntentFilter.addAction("android.net.wifi.RSSI_CHANGED");
            this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
            this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
            this.mIsRegister = true;
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (this.mIsRegister) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mIsRegister = false;
        }
    }

    public void enableCheck(boolean enable) {
        if (enable) {
            logD("Enable WIFI OTA Check.");
            registBroadcastReceiver();
            if (!this.wifiOtaChkIsEnable) {
                sendMessage(EVENT_WP_WATCHDOG_ENABLE_CMD);
                return;
            }
            return;
        }
        logD("Disable WIFI OTA Check.");
        unRegisterBroadcastReceiver();
        if (this.wifiOtaChkIsEnable) {
            sendMessage(EVENT_WP_WATCHDOG_DISABLE_CMD);
        }
    }

    public void doWifiOTACheck(int mVerifyType) {
        if (135671 == mVerifyType) {
            logD("start VERIFY_WITH_NOT_DATA_LINK");
            sendMessage(EVENT_START_VERIFY_WITH_NOT_DATA_LINK);
        } else if (135672 == mVerifyType) {
            logD("start VERIFY_WITH_DATA_LINK ");
            sendMessage(EVENT_START_VERIFY_WITH_DATA_LINK);
        } else if (135673 == mVerifyType) {
            logD("stop VERIFY_WITH_NOT_DATA_LINK");
            sendMessage(EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK);
        } else if (135674 == mVerifyType) {
            logD("stop VERIFY_WITH_DATA_LINK");
            sendMessage(EVENT_STOP_VERIFY_WITH_DATA_LINK);
        } else {
            logE("doWifiOTACheck not support command received: " + mVerifyType);
        }
    }

    /* access modifiers changed from: private */
    public void postEvent(int what, int arg1, int arg2) {
        if (this.mQMHandler != null) {
            this.mQMHandler.sendMessage(this.mQMHandler.obtainMessage(what, arg1, arg2));
            return;
        }
        logE("postEvent msg handle null point error.");
    }

    /* access modifiers changed from: private */
    public void sendResultMsgToQM(int mQosLevel) {
        if (this.mCurrRssi < this.RSSI_TH_FOR_BAD_QOE_LEVEL) {
            mQosLevel = 1;
        } else if (this.mCurrRssi < this.RSSI_TH_FOR_NOT_BAD_QOE_LEVEL && mQosLevel == 3) {
            mQosLevel = 2;
        }
        String strBssid = "null";
        if (this.mCurrentBssid != null) {
            strBssid = this.mCurrentBssid.mBssid;
        }
        logI("sendResultMsgToQM bssid:" + partDisplayBssid(strBssid) + ", qoslevel=" + mQosLevel);
        postEvent(1, 1, mQosLevel);
    }

    private void sendTCPResultQuery() {
        postEvent(4, 1, 0);
    }

    /* access modifiers changed from: private */
    public void resetHighDataFlow() {
        this.mHighDataFlowRate = 1.0f;
        this.mHighDataFlowScenario = 0;
        this.mHighDataFlowProtection = 0;
        this.mHighDataFlowPeriodCounter = 0;
        this.mHighDataFlowNotDetectCounter = 0;
        this.mHighDataFlowLastRxBytes = TrafficStats.getRxBytes(WLAN_IFACE);
    }

    public void setBqeLevel(int bqeLevel) {
        this.mCurrentBqeLevel = bqeLevel;
    }

    /* access modifiers changed from: private */
    public void countDownGoodRssiByTime() {
        if (this.mCurrentBssid.mGoodLinkTargetRssi > this.mCurrentBssid.mTimeElapGoodLinkTargetRssi) {
            long elapTime = SystemClock.elapsedRealtime() - this.mCurrentBssid.mTimeElapBaseTime;
            if (elapTime >= 120000) {
                long subDb = (long) ((int) (elapTime / 120000));
                if (subDb > 0) {
                    int old_rssi = this.mCurrentBssid.mGoodLinkTargetRssi;
                    if (subDb >= ((long) (this.mCurrentBssid.mGoodLinkTargetRssi - this.mCurrentBssid.mTimeElapGoodLinkTargetRssi))) {
                        this.mCurrentBssid.mGoodLinkTargetRssi = this.mCurrentBssid.mTimeElapGoodLinkTargetRssi;
                    } else {
                        this.mCurrentBssid.mGoodLinkTargetRssi -= (int) subDb;
                    }
                    logD(" elapse time GL target rssi=" + this.mCurrentBssid.mTimeElapGoodLinkTargetRssi + ", adjust GL rssi from " + old_rssi + " to " + this.mCurrentBssid.mGoodLinkTargetRssi + " by elapse time :" + (2 * subDb) + " Min.");
                    BssidStatistics bssidStatistics = this.mCurrentBssid;
                    bssidStatistics.mTimeElapBaseTime = bssidStatistics.mTimeElapBaseTime + (120000 * subDb);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void adjustTargetPoorRssi(int badRssi) {
        int oldRssi = this.mCurrentBssid.mPoorLinkTargetRssi;
        this.mCurrentBssid.mPoorLinkTargetRssi = (oldRssi + badRssi) / 2;
        logD("adjustTargetPoorRssi current mrssi=" + badRssi + ", ajust poor target rssi from:" + oldRssi + " to " + this.mCurrentBssid.mPoorLinkTargetRssi);
    }

    /* access modifiers changed from: private */
    public int wpPoorLinkLevelCalcByRssi(double lossRate, int mrssi, int poor_link_rssi, int rtt) {
        if (((double) mrssi) <= ((double) poor_link_rssi) - BAD_RSSI_LEVEL_0_NOT_AVAILABLE_SUB_VAL || lossRate >= 0.4d || rtt >= 8000) {
            return 0;
        }
        if (((double) mrssi) <= ((double) poor_link_rssi) - BAD_RSSI_LEVEL_1_VERY_POOR_SUB_VAL || lossRate >= 0.30000000000000004d || rtt >= 5000) {
            return 1;
        }
        return 2;
    }

    /* access modifiers changed from: private */
    public int wpPoorLinkLevelCalcByTcp(double lossRate, double tcpRetxRate, int rtt) {
        int mCnePoorLinkLevel;
        if (tcpRetxRate >= PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE || lossRate >= 0.4d || rtt >= 8000) {
            mCnePoorLinkLevel = 0;
        } else if (tcpRetxRate >= PKT_CHK_RETXRATE_LEVEL_1_VERY_POOR || lossRate >= 0.30000000000000004d || rtt >= 5000) {
            mCnePoorLinkLevel = 1;
        } else {
            mCnePoorLinkLevel = 2;
        }
        logD("poorLinkLevelCalcByTcp, lossRate= " + formatFloatToStr(lossRate) + ", tcpRetxRate=" + formatFloatToStr(tcpRetxRate) + ". get PoorLinkLevel=" + mCnePoorLinkLevel);
        return mCnePoorLinkLevel;
    }

    private void saveNewTcpResult(int[] resultArray, int len, int resultType) {
        synchronized (this) {
            this.mNewestTcpChkResult.reset();
            this.mNewestTcpChkResult.tcpResultUpdate(resultArray, len, resultType);
        }
    }

    private TcpChkResult getTcpResult() {
        synchronized (this) {
            if (this.mNewestTcpChkResult.mValueIsSet) {
                TcpChkResult tcpChkResult = this.mNewestTcpChkResult;
                return tcpChkResult;
            }
            logD("getTcpResult tcp result value not valid.");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public TcpChkResult getQueryTcpResult() {
        TcpChkResult result = null;
        long startTime = SystemClock.elapsedRealtime();
        sendTCPResultQuery();
        int retryCnt = 10;
        while (true) {
            retryCnt--;
            if (retryCnt < 0) {
                break;
            }
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                logE(" Thread.sleep exception:" + e);
            }
            result = getTcpResult();
            if (result != null && result.mUpdateTime >= startTime) {
                break;
            }
            result = null;
        }
        if (result != null) {
            return result;
        }
        logE(" getQueryTcpResult failed.");
        return null;
    }

    /* access modifiers changed from: private */
    public void getCurrTcpRtt(ProcessedTcpResult mTcpResult) {
        long nowtime = SystemClock.elapsedRealtime();
        TcpChkResult tmpTcr = getTcpResult();
        if (tmpTcr != null) {
            long dtime = (nowtime - tmpTcr.mUpdateTime) + ((long) (tmpTcr.mTcpRttWhen * 1000));
            if (dtime < LINK_SAMPLING_INTERVAL_MS && tmpTcr.mTcpRttPkts > 2) {
                mTcpResult.mAvgRtt = tmpTcr.mTcpRtt;
                mTcpResult.mTotPkt = tmpTcr.mTcpRttPkts;
            } else if (dtime < LINK_SAMPLING_INTERVAL_MS) {
                mTcpResult.mInadequateAvgRtt = tmpTcr.mTcpRtt;
            }
        }
    }

    public void tcpChkResultUpdate(int[] resultArray, int len, int resultType) {
        saveNewTcpResult(resultArray, len, resultType);
    }

    /* access modifiers changed from: private */
    public void updateCurrentBssid(String bssid, String ssid, int networkID) {
        if (bssid != null) {
            logI(" mCurrentBssid  current bssid is:" + partDisplayBssid(bssid));
            if (this.mCurrentBssid == null || !bssid.equals(this.mCurrentBssid.mBssid)) {
                if (this.mCurrentBssid != null) {
                    this.mCurrentBssid.afterDisconnectProcess();
                }
                this.mCurrentBssid = this.mBssidCache.get(bssid);
                if (this.mCurrentBssid == null) {
                    this.mCurrentBssid = new BssidStatistics(bssid);
                    this.mBssidCache.put(bssid, this.mCurrentBssid);
                } else {
                    logI(" get mCurrentBssid inCache for new bssid.");
                }
                if (this.mWifiProHistoryRecordManager != null) {
                    this.mWifiProHistoryRecordManager.updateCurrConntAp(bssid, ssid, networkID);
                } else {
                    logE("updateCurrentBssid APInfoProcess null error.");
                }
                this.mCurrentBssid.afterConnectProcess();
                logI(" send BSSID changed event.");
                sendMessage(EVENT_BSSID_CHANGE);
                this.mWifiProStatisticsManager.updateWifiConnectState(1);
            }
        } else if (this.mCurrentBssid != null) {
            logI(" BSSID changed, bssid:" + partDisplayBssid(this.mCurrentBssid.mBssid) + " was disconnected. set mCurrentBssid=null");
            if (this.mWifiProHistoryRecordManager != null) {
                this.mWifiProHistoryRecordManager.updateCurrConntAp(null, null, 0);
            } else {
                logE("updateCurrentBssid APInfoProcess null error.");
            }
            this.mCurrentBssid.afterDisconnectProcess();
            this.mCurrentBssid = null;
            sendMessage(EVENT_BSSID_CHANGE);
            this.mWifiProStatisticsManager.updateWifiConnectState(2);
        }
    }

    /* access modifiers changed from: private */
    public String formatFloatToStr(double data) {
        if (this.mFormatData != null) {
            return this.mFormatData.format(data);
        }
        return DEFAULT_DISPLAY_DATA_STR;
    }

    /* access modifiers changed from: private */
    public void sendLinkStatusNotification(boolean isGood, int linkLevel) {
        if (isGood) {
            logI("judge good link######, goodLinkLevel=" + linkLevel);
            sendResultMsgToQM(linkLevel);
            return;
        }
        logI("judge poor link######, poorLinkLevel=" + linkLevel);
        sendResultMsgToQM(linkLevel);
    }

    public void setSampleRtt(int[] ipqos, int len) {
        if (this.mIsDualbandEnable) {
            if (ipqos == null || this.mCurrentBssid == null || this.mCurrentBssid.mBssid == null) {
                Log.d(TAG, "setSampleRtt: null pointer exception, return");
            } else if (!this.mCurrentBssid.mBssid.equals(this.mLastSampleBssid)) {
                this.mLastSampleBssid = this.mCurrentBssid.mBssid;
                Log.d(TAG, "setSampleRtt: Bssid changed, return");
            } else {
                this.mLastSampleBssid = this.mCurrentBssid.mBssid;
                int i = 0;
                if ((len > 2 ? ipqos[2] : 0) >= 10) {
                    this.mLastSampleRtt = 0;
                    this.mLastSamplePkts = 0;
                    return;
                }
                this.mLastSampleRtt = len > 0 ? ipqos[0] : 0;
                if (len > 1) {
                    i = ipqos[1];
                }
                this.mLastSamplePkts = i;
                this.mCurrentBssid.updateRtt(this.mLastSampleRssi, this.mLastSampleRtt, this.mLastSamplePkts);
                if (this.mHwDualBandQualityEngine != null) {
                    this.mHwDualBandQualityEngine.resetSampleRtt();
                }
            }
        }
    }

    public int updateQosLevelByHistory() {
        int qosLevel;
        int qosLevel2;
        if (this.mWifiManager == null) {
            return 0;
        }
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return 0;
        }
        int rssi = wifiInfo.getRssi();
        if (this.mCurrentBssid == null) {
            return 0;
        }
        int avgRtt = this.mCurrentBssid.getHistoryNearRtt(rssi);
        if (avgRtt == 0) {
            qosLevel = 0;
        } else if (avgRtt <= 1200) {
            qosLevel = 3;
        } else if (avgRtt <= 4800 || rssi >= -65) {
            qosLevel = 2;
        } else {
            qosLevel = 1;
        }
        if (qosLevel != 0) {
            return qosLevel;
        }
        if (rssi >= -65) {
            qosLevel2 = 3;
        } else if (rssi >= QOS_NOT_GOOD_LEVEL_OF_RSSI) {
            qosLevel2 = 2;
        } else {
            qosLevel2 = 1;
        }
        return qosLevel2;
    }

    public boolean getHandover5GApRssiThreshold(WifiProEstimateApInfo estimateApInfo) {
        logD("start getHandover5GApRssiThreshold");
        sendMessage(EVENT_GET_5G_AP_RSSI_THRESHOLD, estimateApInfo);
        return true;
    }

    public boolean getApHistoryQualityScore(WifiProEstimateApInfo estimateApInfo) {
        logD("start getApHistoryQualityScore");
        sendMessage(EVENT_GET_AP_HISTORY_QUALITY_SCORE, estimateApInfo);
        return true;
    }

    private void sendDBQoeResult(int eventId, WifiProEstimateApInfo estimateApInfo) {
        logI("sendDBQoeResult enter.");
        if (this.mQMHandler != null) {
            this.mQMHandler.sendMessage(this.mQMHandler.obtainMessage(eventId, estimateApInfo));
            return;
        }
        logE("sendDBQoeResult null error.");
    }

    private BssidStatistics getApQualityRecord(String apBssid) {
        if (apBssid == null) {
            return null;
        }
        logI("getApQualityRecord enter, for bssid:" + partDisplayBssid(apBssid));
        BssidStatistics mBssidStat = this.mBssidCache.get(apBssid);
        if (mBssidStat == null) {
            mBssidStat = new BssidStatistics(apBssid);
            mBssidStat.initQualityRecordFromDatabase();
            this.mBssidCache.put(apBssid, mBssidStat);
        } else {
            logI(" get mCurrentBssid inCache for new bssid.");
        }
        return mBssidStat;
    }

    /* access modifiers changed from: private */
    public void target5GApRssiTHProcess(WifiProEstimateApInfo estimateApInfo) {
        logD("target5GApRssiTHProcess enter: ssid=" + estimateApInfo.getApSsid() + " bssid=" + partDisplayBssid(estimateApInfo.getApBssid()));
        String apBssid = estimateApInfo.getApBssid();
        if (apBssid == null) {
            logE("target5GApRssiTHProcess apBssid null error.");
            estimateApInfo.setRetRssiTH(-65);
            sendDBQoeResult(12, estimateApInfo);
            return;
        }
        BssidStatistics apQualityInfo = getApQualityRecord(apBssid);
        WifiProApInfoRecord apInfoRecord = this.mWifiProHistoryRecordManager.getApInfoRecord(apBssid);
        if (apInfoRecord != null) {
            logD("target5GApRssiTHProcess test last connected time:" + apInfoRecord.lastConnectTime + ", total connect time:" + apInfoRecord.totalUseTime + ", night connect time:" + apInfoRecord.totalUseTimeAtNight + ", weekend connect time:" + apInfoRecord.totalUseTimeAtWeekend);
        }
        int rssiTH = apQualityInfo.findRssiTargetByRtt(-80, -45, 1500, -65);
        if (rssiTH < DEFAULT_TARGET_MONITOR_RSSI) {
            rssiTH = DEFAULT_TARGET_MONITOR_RSSI;
        }
        estimateApInfo.setRetRssiTH(rssiTH);
        sendDBQoeResult(12, estimateApInfo);
    }

    /* access modifiers changed from: private */
    public void targetApHistoryQualityScoreProcess(WifiProEstimateApInfo estimateApInfo) {
        estimateApInfo.setRetHistoryScore(getApScore(estimateApInfo));
        sendDBQoeResult(13, estimateApInfo);
    }

    private int getApScore(WifiProEstimateApInfo estimateApInfo) {
        if (this.mCurrentBssid == null || this.mCurrentBssid.mBssid == null || this.mHwDualBandQualityEngine == null || estimateApInfo == null) {
            logE("getApHistoryQualityScore null pointer.");
            return -1;
        }
        logD("getApHistoryQualityScore: ssid=" + estimateApInfo.getApSsid() + " bssid=" + partDisplayBssid(estimateApInfo.getApBssid()));
        int retScore = 0 + getApFixedScore(estimateApInfo);
        int variedScore = getApVariedScore(estimateApInfo);
        if (variedScore == -1) {
            return -1;
        }
        int retScore2 = retScore + variedScore;
        logD("ssid:" + estimateApInfo.getApSsid() + " bssid:" + partDisplayBssid(estimateApInfo.getApBssid()) + " total score is " + retScore2);
        return retScore2;
    }

    private int getApFixedScore(WifiProEstimateApInfo estimateApInfo) {
        int fixedScore = 0;
        String apBssid = estimateApInfo.getApBssid();
        WifiProApInfoRecord apInfoRecord = this.mWifiProHistoryRecordManager.getApInfoRecord(apBssid);
        if (apInfoRecord != null) {
            int score = this.mHwDualBandQualityEngine.getScoreByConnectTime(apInfoRecord.totalUseTime / WifiProHistoryRecordManager.SECOND_OF_ONE_HOUR);
            logD("bssid:" + partDisplayBssid(apBssid) + " ConnectTime score is " + score);
            fixedScore = 0 + score;
        }
        int score2 = this.mHwDualBandQualityEngine.getScoreByRssi(estimateApInfo.getApRssi());
        logD("bssid:" + partDisplayBssid(apBssid) + " rssi score is " + score2);
        int fixedScore2 = fixedScore + score2;
        if (estimateApInfo.is5GAP()) {
            int score3 = this.mHwDualBandQualityEngine.getScoreByBluetoothUsage();
            logD("bssid:" + partDisplayBssid(apBssid) + " BluetoothUsage score is " + score3);
            fixedScore2 += score3;
        }
        if (!HwDualBandRelationManager.isDualBandAP(this.mCurrentBssid.mBssid, apBssid) || !estimateApInfo.is5GAP()) {
            return fixedScore2;
        }
        logD("bssid:" + partDisplayBssid(apBssid) + " DUAL_BAND_SINGLE_AP score is " + 6);
        return fixedScore2 + 6;
    }

    private int getApVariedScore(WifiProEstimateApInfo estimateApInfo) {
        BssidStatistics apQualityInfo;
        int variedScore = 0;
        String apBssid = estimateApInfo.getApBssid();
        if (this.mCurrentBssid == null || !this.mCurrentBssid.mBssid.equals(apBssid)) {
            apQualityInfo = getApQualityRecord(apBssid);
        } else {
            apQualityInfo = this.mCurrentBssid;
        }
        if (apQualityInfo == null) {
            logE("get5GApHandoverVariedScore apQualityInfo null pointer.");
            return -1;
        }
        int queryRssi = estimateApInfo.getApRssi();
        if (queryRssi > -45) {
            queryRssi = -45;
        } else if (queryRssi < -90) {
            queryRssi = -90;
        }
        int avgRtt = apQualityInfo.getHistoryNearRtt(queryRssi);
        if (avgRtt != 0) {
            int score = this.mHwDualBandQualityEngine.getScoreByRtt(avgRtt);
            logD("bssid:" + partDisplayBssid(apBssid) + " rtt score is " + score);
            variedScore = 0 + score;
        } else {
            logD("There is no rtt history about rssi " + queryRssi);
        }
        double[] lossRateInfo = apQualityInfo.getHistoryNearLossRate(queryRssi);
        if (lossRateInfo[1] > 10.0d) {
            int score2 = this.mHwDualBandQualityEngine.getScoreByLossRate(lossRateInfo[0]);
            logD("bssid:" + partDisplayBssid(apBssid) + " rtt score is " + score2);
            variedScore += score2;
        }
        return variedScore;
    }

    /* access modifiers changed from: private */
    public void sendCurrentAPRssi(int rssi) {
        if (this.mIsDualbandEnable) {
            if (this.mQMHandler == null || INVALID_RSSI == rssi) {
                logE("sendCurryAPRssi null error.");
            } else {
                this.mQMHandler.sendMessage(this.mQMHandler.obtainMessage(14, Integer.valueOf(rssi)));
            }
        }
    }

    /* access modifiers changed from: private */
    public void logD(String msg) {
        if (printLogLevel <= 1) {
            Log.d(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    public void logI(String msg) {
        if (printLogLevel <= 2) {
            Log.i(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    public void logE(String msg) {
        if (printLogLevel <= 3) {
            Log.e(TAG, msg);
        }
    }

    /* access modifiers changed from: private */
    public String partDisplayBssid(String srcBssid) {
        if (srcBssid == null) {
            return "null";
        }
        int len = srcBssid.length();
        if (len < 12) {
            return "Can not display bssid";
        }
        return srcBssid.substring(0, 6) + "**:**" + srcBssid.substring(len - 6, len);
    }

    public synchronized boolean isHighDataFlowModel() {
        if (this.mHighDataFlowScenario != 0) {
            return true;
        }
        return false;
    }
}
