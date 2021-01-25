package com.huawei.hwwifiproservice;

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
import android.os.Bundle;
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
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.hwwifiproservice.TrafficMonitor;
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
    private static boolean DDBG_TOAST_DISPLAY = false;
    private static final String DEFAULT_DISPLAY_DATA_STR = "CanNotDisplay";
    private static final int DEFAULT_GOOD_LINK_TARGET_COUNT = 3;
    private static final int DEFAULT_RATE_GOOD_SPEED = 1;
    private static final int DEFAULT_RATE_RSSI = 1;
    private static final int DEFAULT_TARGET_MONITOR_RSSI = -70;
    private static final int DEFAULT_TARGET_RSSI = -65;
    private static final int DEFAULT_WIFI_LEVEL = 3;
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
    private static final GoodLinkTarget[] GOOD_LINK_TARGET = {new GoodLinkTarget(0, 3, 900000), new GoodLinkTarget(2, 4, 480000), new GoodLinkTarget(4, 4, 240000), new GoodLinkTarget(5, 5, 120000), new GoodLinkTarget(6, 5, 0)};
    private static final int GOOD_LINK_TARGET_MIN_ADD_DB_VAL = 3;
    private static final int GOOD_RTT_TARGET = 1500;
    private static final GoodSpdCountRate[] GOOD_SPD_COUNT_RATE_TBL = {new GoodSpdCountRate(25.0f, 0.5f), new GoodSpdCountRate(20.0f, 0.6f), new GoodSpdCountRate(15.0f, 0.7f), new GoodSpdCountRate(10.0f, SCORE_BACK_OFF_RATE), new GoodSpdCountRate(5.0f, 0.9f)};
    private static final int GOOD_TCP_JUDGE_RTT_PKT = 50;
    private static final int HALF_LINK_SAMPLING_INTERVAL = 2;
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
    private static final int LOG_REC_DEFAULT_SIZE = 25;
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
    private static final int POOR_LINK_RSSI_THRESHOLD = -75;
    private static final int POOR_LINK_SAMPLE_COUNT = 1;
    private static final int POOR_NET_MIN_RSSI_TH = -85;
    private static final int POOR_NET_RSSI_TH_OFFSET = 1;
    private static final double PRESET_LOSSESS_POWER = 1.5d;
    private static final int PRESET_LOSSESS_SIZE = 90;
    private static final int PTOP_LEVEL_GOOD = 8;
    private static final int QOS_GOOD_LEVEL_OF_RSSI = -65;
    private static final int QOS_NOT_GOOD_LEVEL_OF_RSSI = -82;
    private static final int QUERY_TCP_INFO_RETRY_CNT = 10;
    private static final int QUERY_TCP_INFO_WAIT_ITVL = 2;
    private static final int REALTIME_TO_MS = 1000;
    private static final int REDUCE_ONE_TIME_UNIT = 64000;
    private static final int REPORT_GOOD_MAX_RTT_VALUE = 1200;
    private static final RestoreWifiTime[] RESTORE_WIFI_TIME = {new RestoreWifiTime(120000, 900000), new RestoreWifiTime(240000, 600000), new RestoreWifiTime(MAX_RESTORE_WIFI_TIME, 0)};
    private static final long ROVE_OUT_LINK_SAMPLING_INTERVAL_MS = 2000;
    private static final RssiGoodSpdTH[] RSSI_GOOD_SPD_TBL = {new RssiGoodSpdTH(-75, 81920, WifiproBqeUtils.BQE_NOT_GOOD_SPEED), new RssiGoodSpdTH(QOS_NOT_GOOD_LEVEL_OF_RSSI, SCORE_BACK_OFF_NETGOOD_SPEED_TH, 15360), new RssiGoodSpdTH(INVALID_RSSI, MIN_SPEED_VAL, 10240)};
    private static final RssiRate[] RSSI_RATE_TBL = {new RssiRate(-50, 0.6f), new RssiRate(-65, 0.7f), new RssiRate(-75, SCORE_BACK_OFF_RATE), new RssiRate(QOS_NOT_GOOD_LEVEL_OF_RSSI, 0.9f)};
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
    private static final double TCP_RETRANS_RATE_GOOD = 0.1d;
    private static final int TCP_TX_ZERO_PKT = 0;
    private static final int TIME_ELAP_2_MIN = 120000;
    private static final int VERY_BAD_RTT_TH_FOR_TCP_BAD = 16000;
    private static final int VL_RSSI_SMOOTH_DIV_NUM = 2;
    private static final int WIFI_POOR_OTA_OR_TCP_BAD = 1;
    private static final int WIFI_POOR_RSSI_BAD = 0;
    private static final String WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
    private static final int WLAN_INET_RSSI_LEVEL_3_GOOD = 0;
    private static final int WLAN_INET_RSSI_LEVEL_4_BETTER = 5;
    private static final int WLAN_INET_RSSI_LEVEL_5_BEST = 8;
    private static int printLogLevel = 1;
    private static double[] sPresetLoss;
    private int LOW_RSSI_TH = QOS_NOT_GOOD_LEVEL_OF_RSSI;
    private int QOE_LEVEL_RTT_MIN_PKT = 3;
    private int RSSI_TH_FOR_BAD_QOE_LEVEL = QOS_NOT_GOOD_LEVEL_OF_RSSI;
    private int RSSI_TH_FOR_NOT_BAD_QOE_LEVEL = -75;
    private boolean isOTABad = false;
    private boolean isPoorLinkForLowRssi = false;
    private boolean isRssiBad = false;
    private boolean isRttGood = false;
    private boolean isTCPBad = false;
    private boolean isTcpRetranBad = false;
    private boolean isVeryBadRTT = false;
    private boolean isWlanCongestion = false;
    private boolean isWlanLossRateBad = false;
    private boolean lastPeriodTcpResultValid = false;
    private double lossRate = 0.0d;
    private long mBestSpeedInPeriod = 0;
    private BroadcastReceiver mBroadcastReceiver;
    private LruCache<String, BssidStatistics> mBssidCache = new LruCache<>(20);
    private int mChrDetecdNoInternetCount = 0;
    private Context mContext;
    private TcpChkResult mCurTcpChkResult;
    private int mCurrRssi = INVALID_RSSI;
    private String mCurrSSID = null;
    private int mCurrentBqeLevel = 0;
    private BssidStatistics mCurrentBssid;
    private DecimalFormat mFormatData;
    private int mGoodDetectBaseRssi;
    private long mHighDataFlowLastRxBytes = 0;
    private int mHighDataFlowNotDetectCounter = 0;
    private int mHighDataFlowPeriodCounter = 0;
    private int mHighDataFlowProtection = 0;
    private float mHighDataFlowRate = 1.0f;
    private int mHighDataFlowScenario = 0;
    private int mHighSpeedToken = 0;
    private int mHistoryHSCount = 0;
    private HwDualBandQualityEngine mHwDualBandQualityEngine;
    private IntentFilter mIntentFilter;
    private boolean mIsDualbandEnable = false;
    private boolean mIsMonitoring = false;
    private boolean mIsNotStatic;
    private boolean mIsRegister = false;
    private boolean mIsSpeedOkDuringPeriod = false;
    private boolean mIsWifiSwitchRobotAlgorithmEnabled = false;
    private int mLastDetectLevel = 0;
    private int mLastDnsFailCount;
    private boolean mLastHighDataFlow = false;
    private TcpChkResult mLastPeriodTcpChkResult;
    private String mLastSampleBssid = null;
    private int mLastSamplePkts = 0;
    private int mLastSampleRssi = 0;
    private int mLastSampleRtt = 0;
    private long mLinkSampleIntervalTime = 0;
    private final Object mLock = new Object();
    private TcpChkResult mNewestTcpChkResult = new TcpChkResult();
    private float mPeriodGoodSpdScore = 0.0f;
    private int mPoorNetRssiRealTH = -75;
    private int mPoorNetRssiTH = -75;
    private int mPoorNetRssiTHNext = -75;
    private ProcessedTcpResult mProcessedTcpResult;
    private Handler mQMHandler;
    private int mRssiChangedToken = 0;
    private int mRssiFetchToken = 0;
    private int mRssiTHTimeoutToken = 0;
    private int mRssiTHValidTime = 180000;
    private float mSpeedGoodCount = 0.0f;
    private int mSpeedNotGoodCount = 0;
    private Runnable mSpeedUpdate = new Runnable() {
        /* class com.huawei.hwwifiproservice.HuaweiWifiWatchdogStateMachine.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            TrafficMonitor.TxRxStat txRxStat = HuaweiWifiWatchdogStateMachine.this.mTraffic.getStatic(2);
            synchronized (HuaweiWifiWatchdogStateMachine.this.mLock) {
                HuaweiWifiWatchdogStateMachine.this.mTxrxStat = txRxStat;
            }
            if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 24576 || HuaweiWifiWatchdogStateMachine.this.mTxrxStat.tx_speed >= 32768) {
                HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod = true;
            }
            if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed > HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.mBestSpeedInPeriod = huaweiWifiWatchdogStateMachine.mTxrxStat.rx_speed;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.logI("Get speed information rx_speed = " + HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed + " ,tx_speed = " + HuaweiWifiWatchdogStateMachine.this.mTxrxStat.tx_speed + " ,isSpeedOk = " + HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod + " ,mBestSpeedInPeriod = " + HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod);
            if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi != HuaweiWifiWatchdogStateMachine.INVALID_RSSI && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < -75 && HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 65536) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine3.sendMessage(huaweiWifiWatchdogStateMachine3.obtainMessage(HuaweiWifiWatchdogStateMachine.EVENT_HIGH_NET_SPEED_DETECT_MSG, HuaweiWifiWatchdogStateMachine.access$704(huaweiWifiWatchdogStateMachine3), 0));
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine4 = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine.access$816(huaweiWifiWatchdogStateMachine4, huaweiWifiWatchdogStateMachine4.checkGoodSpd(huaweiWifiWatchdogStateMachine4.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed));
            if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 1048576) {
                HuaweiWifiWatchdogStateMachine.access$1008(HuaweiWifiWatchdogStateMachine.this);
            }
        }
    };
    private long mSwitchOutTime = 0;
    private volatile int mTcpReportLevel = 0;
    private TrafficMonitor mTraffic = null;
    private volatile TrafficMonitor.TxRxStat mTxrxStat = null;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;
    private int mWifiPoorReason;
    private int mWifiPoorRssi;
    private WifiProDefaultState mWifiProDefaultState = new WifiProDefaultState();
    private WifiProHistoryRecordManager mWifiProHistoryRecordManager;
    private WifiProLinkMonitoringState mWifiProLinkMonitoringState = new WifiProLinkMonitoringState();
    private WifiProRoveOutParaRecord mWifiProRoveOutParaRecord;
    private WifiProStatisticsManager mWifiProStatisticsManager;
    private WifiProStopVerifyState mWifiProStopVerifyState = new WifiProStopVerifyState();
    private WifiProVerifyingLinkState mWifiProVerifyingLinkState = new WifiProVerifyingLinkState();
    private WifiProWatchdogDisabledState mWifiProWatchdogDisabledState = new WifiProWatchdogDisabledState();
    private WifiProWatchdogEnabledState mWifiProWatchdogEnabledState = new WifiProWatchdogEnabledState();
    private boolean mWpLinkMonitorRunning = false;
    private AsyncChannel mWsmChannel = new AsyncChannel();
    private int pTot = 0;
    private int tcpRetransPkts = 0;
    private double tcpRetransRate = 0.0d;
    private int tcpRxPkts = 0;
    private int tcpTxPkts = 0;
    private boolean wifiOtaChkIsEnable;

    static /* synthetic */ int access$1008(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mHistoryHSCount;
        x0.mHistoryHSCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$10308(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mChrDetecdNoInternetCount;
        x0.mChrDetecdNoInternetCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$1108(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mRssiChangedToken;
        x0.mRssiChangedToken = i + 1;
        return i;
    }

    static /* synthetic */ int access$2804(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mRssiFetchToken + 1;
        x0.mRssiFetchToken = i;
        return i;
    }

    static /* synthetic */ int access$4304(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mRssiTHTimeoutToken + 1;
        x0.mRssiTHTimeoutToken = i;
        return i;
    }

    static /* synthetic */ int access$4308(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mRssiTHTimeoutToken;
        x0.mRssiTHTimeoutToken = i + 1;
        return i;
    }

    static /* synthetic */ int access$5512(HuaweiWifiWatchdogStateMachine x0, int x1) {
        int i = x0.mRssiTHValidTime + x1;
        x0.mRssiTHValidTime = i;
        return i;
    }

    static /* synthetic */ float access$5910(HuaweiWifiWatchdogStateMachine x0) {
        float f = x0.mSpeedGoodCount;
        x0.mSpeedGoodCount = f - 1.0f;
        return f;
    }

    static /* synthetic */ float access$5916(HuaweiWifiWatchdogStateMachine x0, float x1) {
        float f = x0.mSpeedGoodCount + x1;
        x0.mSpeedGoodCount = f;
        return f;
    }

    static /* synthetic */ float access$5924(HuaweiWifiWatchdogStateMachine x0, float x1) {
        float f = x0.mSpeedGoodCount - x1;
        x0.mSpeedGoodCount = f;
        return f;
    }

    static /* synthetic */ int access$6008(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mSpeedNotGoodCount;
        x0.mSpeedNotGoodCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$6908(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mHighDataFlowNotDetectCounter;
        x0.mHighDataFlowNotDetectCounter = i + 1;
        return i;
    }

    static /* synthetic */ int access$7008(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mHighDataFlowPeriodCounter;
        x0.mHighDataFlowPeriodCounter = i + 1;
        return i;
    }

    static /* synthetic */ int access$704(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mHighSpeedToken + 1;
        x0.mHighSpeedToken = i;
        return i;
    }

    static /* synthetic */ int access$708(HuaweiWifiWatchdogStateMachine x0) {
        int i = x0.mHighSpeedToken;
        x0.mHighSpeedToken = i + 1;
        return i;
    }

    static /* synthetic */ float access$816(HuaweiWifiWatchdogStateMachine x0, float x1) {
        float f = x0.mPeriodGoodSpdScore + x1;
        x0.mPeriodGoodSpdScore = f;
        return f;
    }

    private HuaweiWifiWatchdogStateMachine(Context context, Messenger dstMessenger, Handler handler) {
        super(TAG);
        this.mContext = context;
        this.mIsWifiSwitchRobotAlgorithmEnabled = WifiProCommonUtils.isWifiSwitchRobotAlgorithmEnabled();
        if (!this.mIsWifiSwitchRobotAlgorithmEnabled) {
            this.mLinkSampleIntervalTime = LINK_SAMPLING_INTERVAL_MS;
        } else {
            this.mLinkSampleIntervalTime = 4000;
        }
        this.mQMHandler = handler;
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
            this.mHwDualBandQualityEngine = new HwDualBandQualityEngine(context, handler);
        }
    }

    /* access modifiers changed from: private */
    public static class RssiRate {
        public final int mRssiVal;
        public final float mSwRate;

        public RssiRate(int rssiVal, float swRate) {
            this.mRssiVal = rssiVal;
            this.mSwRate = swRate;
        }
    }

    /* access modifiers changed from: private */
    public static class RssiGoodSpdTH {
        public final int mRssiVal;
        public final int mSpdVal;
        public final int mUnitSpdVal;

        public RssiGoodSpdTH(int rssiVal, int spd, int unitSpd) {
            this.mRssiVal = rssiVal;
            this.mSpdVal = spd;
            this.mUnitSpdVal = unitSpd;
        }
    }

    /* access modifiers changed from: private */
    public static class GoodSpdCountRate {
        public final float goodCount;
        public final float swRate;

        public GoodSpdCountRate(float currentGoodCount, float currentRate) {
            this.goodCount = currentGoodCount;
            this.swRate = currentRate;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float checkGoodSpd(int rssi, long spd) {
        if (spd < 30720 || rssi >= -65 || rssi == INVALID_RSSI) {
            return 0.0f;
        }
        int len = RSSI_GOOD_SPD_TBL.length;
        for (int loop = 0; loop < len; loop++) {
            if (rssi >= RSSI_GOOD_SPD_TBL[loop].mRssiVal) {
                if (spd < ((long) RSSI_GOOD_SPD_TBL[loop].mSpdVal)) {
                    return 0.0f;
                } else {
                    float retScore = ((((float) (spd - ((long) RSSI_GOOD_SPD_TBL[loop].mSpdVal))) * ADD_UNIT_SPD_SCORE) / ((float) RSSI_GOOD_SPD_TBL[loop].mUnitSpdVal)) + 0.5f;
                    if (retScore > MAX_SCORE_ONE_CHECK) {
                        retScore = MAX_SCORE_ONE_CHECK;
                    }
                    logI("checkGoodSpd at rssi:" + rssi + ", spd:" + (spd / 1024) + "K > " + (RSSI_GOOD_SPD_TBL[loop].mSpdVal / 1024) + "K, unit:" + (RSSI_GOOD_SPD_TBL[loop].mUnitSpdVal / 1024) + "K, score:" + formatFloatToStr((double) retScore));
                    return retScore;
                }
            }
        }
        return 0.0f;
    }

    public static HuaweiWifiWatchdogStateMachine makeHuaweiWifiWatchdogStateMachine(Context context, Messenger dstMessenger, Handler handler) {
        HuaweiWifiWatchdogStateMachine wwsm = new HuaweiWifiWatchdogStateMachine(context, dstMessenger, handler);
        wwsm.start();
        return wwsm;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void monitorNetworkQos(boolean enable) {
        if (enable) {
            if (!this.mIsMonitoring) {
                logI("monitorNetworkQos start speed track.");
                this.mTraffic.enableMonitor(true, 1);
                this.mTraffic.setExpireTime(5000);
                this.mIsMonitoring = true;
            }
        } else if (this.mIsMonitoring) {
            logI("monitorNetworkQos stop speed track.");
            this.mTraffic.enableMonitor(false, 1);
            this.mIsMonitoring = false;
        }
    }

    private void setupNetworkReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.huawei.hwwifiproservice.HuaweiWifiWatchdogStateMachine.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
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
                        HuaweiWifiWatchdogStateMachine.access$1108(HuaweiWifiWatchdogStateMachine.this);
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.sendMessage(huaweiWifiWatchdogStateMachine.obtainMessage(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_CHANGE, newRssiVal, huaweiWifiWatchdogStateMachine.mRssiChangedToken, null));
                    }
                } else {
                    HuaweiWifiWatchdogStateMachine.this.logD("nothing to do");
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
            logI("Enable WIFI OTA Check.");
            registBroadcastReceiver();
            if (!this.wifiOtaChkIsEnable) {
                sendMessage(EVENT_WP_WATCHDOG_ENABLE_CMD);
                return;
            }
            return;
        }
        logI("Disable WIFI OTA Check.");
        unRegisterBroadcastReceiver();
        if (this.wifiOtaChkIsEnable) {
            sendMessage(EVENT_WP_WATCHDOG_DISABLE_CMD);
        }
    }

    public void doWifiOTACheck(int verifyType) {
        if (135671 == verifyType) {
            logI("start VERIFY_WITH_NOT_DATA_LINK");
            sendMessage(EVENT_START_VERIFY_WITH_NOT_DATA_LINK);
        } else if (135672 == verifyType) {
            logI("start VERIFY_WITH_DATA_LINK ");
            sendMessage(EVENT_START_VERIFY_WITH_DATA_LINK);
        } else if (135673 == verifyType) {
            logI("stop VERIFY_WITH_NOT_DATA_LINK");
            sendMessage(EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK);
        } else if (135674 == verifyType) {
            logI("stop VERIFY_WITH_DATA_LINK");
            sendMessage(EVENT_STOP_VERIFY_WITH_DATA_LINK);
        } else {
            logE("doWifiOTACheck not support command received: " + verifyType);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postEvent(int what, int arg1, int arg2) {
        Handler handler = this.mQMHandler;
        if (handler != null) {
            this.mQMHandler.sendMessage(handler.obtainMessage(what, arg1, arg2));
            return;
        }
        logE("postEvent msg handle null point error.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendResultMsgToQM(int mQosLevel) {
        int tempQosLevel = mQosLevel;
        int i = this.mCurrRssi;
        if (i < this.RSSI_TH_FOR_BAD_QOE_LEVEL) {
            tempQosLevel = 1;
        } else if (i >= this.RSSI_TH_FOR_NOT_BAD_QOE_LEVEL || tempQosLevel != 3) {
            logD("nothing to do.");
        } else {
            tempQosLevel = 2;
        }
        String strBssid = "null";
        BssidStatistics bssidStatistics = this.mCurrentBssid;
        if (bssidStatistics != null) {
            strBssid = bssidStatistics.mBssid;
        }
        logI("sendResultMsgToQM bssid:" + WifiProCommonUtils.safeDisplayBssid(strBssid) + ", qoslevel=" + tempQosLevel);
        postEvent(1, 1, tempQosLevel);
    }

    private void sendTCPResultQuery() {
        postEvent(4, 1, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetHighDataFlow() {
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

    class WifiProDefaultState extends State {
        WifiProDefaultState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logI("WifiProDefaultState enter.");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_NETWORK_STATE_CHANGE /* 135170 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_SUPPLICANT_STATE_CHANGE /* 135172 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_WIFI_RADIO_STATE_CHANGE /* 135173 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_BSSID_CHANGE /* 135175 */:
                case HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH /* 135179 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_NOT_DATA_LINK /* 135199 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_DATA_LINK /* 135200 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK /* 135201 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_DATA_LINK /* 135202 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_HIGH_NET_SPEED_DETECT_MSG /* 136170 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT /* 136171 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_RSSI_CHANGE /* 136172 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_GET_5G_AP_RSSI_THRESHOLD /* 136268 */:
                case HuaweiWifiWatchdogStateMachine.EVENT_GET_AP_HISTORY_QUALITY_SCORE /* 136269 */:
                case 151573:
                case 151574:
                    return true;
                default:
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.logE("Unprocessed messages " + msg + " in state " + HuaweiWifiWatchdogStateMachine.this.getCurrentState().getName());
                    return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class WifiProWatchdogDisabledState extends State {
        WifiProWatchdogDisabledState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logI("DisabledState enter.");
            HuaweiWifiWatchdogStateMachine.this.wifiOtaChkIsEnable = false;
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == HuaweiWifiWatchdogStateMachine.EVENT_NETWORK_STATE_CHANGE) {
                NetworkInfo networkInfo = (NetworkInfo) ((Intent) msg.obj).getParcelableExtra("networkInfo");
                if (!(networkInfo == null || HuaweiWifiWatchdogStateMachine.this.mWifiManager == null)) {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mWifiInfo = huaweiWifiWatchdogStateMachine.mWifiManager.getConnectionInfo();
                    if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null) {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine2.updateCurrentBssid(huaweiWifiWatchdogStateMachine2.mWifiInfo.getBSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getNetworkId());
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, 0);
                    }
                    if (AnonymousClass3.$SwitchMap$android$net$NetworkInfo$DetailedState[networkInfo.getDetailedState().ordinal()] == 1) {
                        HuaweiWifiWatchdogStateMachine.this.logI(" rcv VERIFYING_POOR_LINK, do nothing.");
                    }
                }
            } else if (i == HuaweiWifiWatchdogStateMachine.EVENT_WP_WATCHDOG_ENABLE_CMD) {
                HuaweiWifiWatchdogStateMachine.this.logI("receive enable command, transition to WifiProStopVerifyState.");
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine3.transitionTo(huaweiWifiWatchdogStateMachine3.mWifiProStopVerifyState);
            } else if (i != HuaweiWifiWatchdogStateMachine.EVENT_WP_WATCHDOG_DISABLE_CMD) {
                return false;
            } else {
                HuaweiWifiWatchdogStateMachine.this.logI("receive disable command, ignore.");
            }
            return true;
        }
    }

    /* renamed from: com.huawei.hwwifiproservice.HuaweiWifiWatchdogStateMachine$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$DetailedState = new int[NetworkInfo.DetailedState.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.VERIFYING_POOR_LINK.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    class WifiProWatchdogEnabledState extends State {
        WifiProWatchdogEnabledState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logI("EnabledState enter.");
            HuaweiWifiWatchdogStateMachine.this.wifiOtaChkIsEnable = true;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_NETWORK_STATE_CHANGE /* 135170 */:
                    NetworkInfo networkInfo = (NetworkInfo) ((Intent) msg.obj).getParcelableExtra("networkInfo");
                    if (networkInfo == null || HuaweiWifiWatchdogStateMachine.this.mWifiManager == null) {
                        return true;
                    }
                    processMessageOfNetworkStateChange(networkInfo);
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_SUPPLICANT_STATE_CHANGE /* 135172 */:
                    processMessageOfSupplicantStateChange((SupplicantState) ((Intent) msg.obj).getParcelableExtra("newState"));
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_WIFI_RADIO_STATE_CHANGE /* 135173 */:
                    if (msg.arg1 != 0) {
                        return true;
                    }
                    HuaweiWifiWatchdogStateMachine.this.logI("WIFI in DISABLING state.");
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.transitionTo(huaweiWifiWatchdogStateMachine.mWifiProStopVerifyState);
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_WP_WATCHDOG_ENABLE_CMD /* 135198 */:
                    HuaweiWifiWatchdogStateMachine.this.logI("receive enable command ignore.");
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_WP_WATCHDOG_DISABLE_CMD /* 135203 */:
                    processMessageOfWatchdogDisableCmd();
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_GET_5G_AP_RSSI_THRESHOLD /* 136268 */:
                    if (!HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                        return true;
                    }
                    HuaweiWifiWatchdogStateMachine.this.target5GApRssiTHProcess((WifiProEstimateApInfo) msg.obj);
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_GET_AP_HISTORY_QUALITY_SCORE /* 136269 */:
                    if (!HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable || !(msg.obj instanceof WifiProEstimateApInfo)) {
                        return true;
                    }
                    HuaweiWifiWatchdogStateMachine.this.targetApHistoryQualityScoreProcess((WifiProEstimateApInfo) msg.obj);
                    return true;
                default:
                    return false;
            }
        }

        private void processMessageOfWatchdogDisableCmd() {
            HuaweiWifiWatchdogStateMachine.this.logI("receive disable command.");
            if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager != null) {
                HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.wifiproClose();
            }
            if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null && HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.storeHistoryQuality();
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.transitionTo(huaweiWifiWatchdogStateMachine.mWifiProWatchdogDisabledState);
        }

        private void processMessageOfNetworkStateChange(NetworkInfo networkInfo) {
            if (networkInfo.getDetailedState() != NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.mWifiInfo = huaweiWifiWatchdogStateMachine.mWifiManager.getConnectionInfo();
                if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null) {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine2.updateCurrentBssid(huaweiWifiWatchdogStateMachine2.mWifiInfo.getBSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getNetworkId());
                    return;
                }
                HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, 0);
                return;
            }
            HuaweiWifiWatchdogStateMachine.this.logI("CAPTIVE_PORTAL_CHECK state, not call updateCurrentBssid.");
        }

        private void processMessageOfSupplicantStateChange(SupplicantState supplicantState) {
            if (supplicantState == SupplicantState.COMPLETED) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.mWifiInfo = huaweiWifiWatchdogStateMachine.mWifiManager.getConnectionInfo();
                if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null) {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine2.updateCurrentBssid(huaweiWifiWatchdogStateMachine2.mWifiInfo.getBSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getNetworkId());
                    return;
                }
                HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, 0);
            } else if (supplicantState == SupplicantState.ASSOCIATING) {
                Bundle result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 1, new Bundle());
                if (result == null) {
                    HuaweiWifiWatchdogStateMachine.this.logI("get Bundle fail,Bundle is null ");
                } else if (result.getString("apvendorinfo") != null) {
                    WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 3, result);
                }
            } else {
                HuaweiWifiWatchdogStateMachine.this.logD("nothing to do.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class WifiProVerifyingLinkState extends State {
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
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.mCurrRssi = huaweiWifiWatchdogStateMachine2.mWifiPoorRssi;
            this.mIsOtaPoorTimeOut = false;
            this.mIsReportWiFiGoodAllow = false;
            this.mVLRssiWaitCount = 0;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine3.sendMessage(huaweiWifiWatchdogStateMachine3.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, HuaweiWifiWatchdogStateMachine.access$2804(huaweiWifiWatchdogStateMachine3), 0));
            HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_AVOID_TO_WIFI_DELAY_MSG, 30000);
            if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null) {
                if (1 == HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason) {
                    HuaweiWifiWatchdogStateMachine.this.logI("start delay verify.");
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.otaTcpPoorLinkDetected();
                    if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime < 120000 || HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime > HuaweiWifiWatchdogStateMachine.MAX_RESTORE_WIFI_TIME) {
                        HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime = 120000;
                    }
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine4 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine4.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_DELAY_OTATCP_TIME_OUT_MSG, (long) huaweiWifiWatchdogStateMachine4.mCurrentBssid.mRestoreDelayTime);
                } else if (HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason == 0) {
                    HuaweiWifiWatchdogStateMachine.this.logI("start RSSI verify.");
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.poorLinkDetected(HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi);
                } else {
                    HuaweiWifiWatchdogStateMachine.this.logD("nothing to do.");
                }
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine5 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine5.logI("GoodLinkTargetRssi = " + HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetRssi + " , GoodLinkTargetCount = " + HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetCount);
            }
        }

        public void exit() {
            this.mIsReportWiFiGoodAllow = false;
            this.mIsOtaPoorTimeOut = false;
            HuaweiWifiWatchdogStateMachine.this.removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_DELAY_OTATCP_TIME_OUT_MSG);
            HuaweiWifiWatchdogStateMachine.this.removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_AVOID_TO_WIFI_DELAY_MSG);
            HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi = -90;
        }

        private void processMessageOfCmdRssiFetch() {
            if (!HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("VL first check bssid:" + WifiProCommonUtils.safeDisplayBssid(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid) + ",call newLinkDetected");
                HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.newLinkDetected();
                HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk = true;
            }
            if (this.mVLRssiWaitCount <= 0) {
                HuaweiWifiWatchdogStateMachine.this.mWsmChannel.sendMessage(151572);
                this.mVLRssiWaitCount = 15;
                return;
            }
            HuaweiWifiWatchdogStateMachine.this.logI("vl wait rssi request respond, not send new request.");
            this.mVLRssiWaitCount = (int) (((long) this.mVLRssiWaitCount) - 2);
        }

        private void processMessageOfRssiPktCntFetchSucc(Message msg) {
            this.mVLRssiWaitCount = 0;
            if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo == null || HuaweiWifiWatchdogStateMachine.this.mCurrentBssid == null || msg.obj == null) {
                RssiPacketCountInfo info = HuaweiWifiWatchdogStateMachine.this;
                info.logI("null error or WIFI is not connected, ignore packet fetch event. fg=" + HuaweiWifiWatchdogStateMachine.this.mIsNotStatic);
                return;
            }
            RssiPacketCountInfo info2 = (RssiPacketCountInfo) msg.obj;
            HwWifiConnectivityMonitor monitor = HwWifiConnectivityMonitor.getInstance();
            if (monitor != null) {
                monitor.notifyBackgroundWifiLinkInfo(info2.rssi, info2.txgood, info2.txbad, info2.rxgood);
            }
            HuaweiWifiWatchdogStateMachine.this.sendCurrentAPRssi(info2.rssi);
            if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi) {
                HuaweiWifiWatchdogStateMachine.this.mCurrRssi = info2.rssi;
            } else {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.mCurrRssi = (huaweiWifiWatchdogStateMachine.mCurrRssi + info2.rssi) / 2;
            }
            HuaweiWifiWatchdogStateMachine.this.countDownGoodRssiByTime();
            if (1 == HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason) {
                tryRecoverWifiByOta(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
            } else {
                tryRecoverWifiByRssi(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_BSSID_CHANGE /* 135175 */:
                    break;
                case HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH /* 135179 */:
                    if (msg.arg1 != HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken) {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.logI("VL msg.arg1:" + msg.arg1 + " != RssiFetchToken:" + HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken + ", ignore this command.");
                        break;
                    } else {
                        if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid == null) {
                            HuaweiWifiWatchdogStateMachine.this.logI("VL WIFI is not connected, not fetch RSSI.");
                        } else {
                            processMessageOfCmdRssiFetch();
                        }
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine2.sendMessageDelayed(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, HuaweiWifiWatchdogStateMachine.access$2804(huaweiWifiWatchdogStateMachine2), 0), HuaweiWifiWatchdogStateMachine.ROVE_OUT_LINK_SAMPLING_INTERVAL_MS);
                        break;
                    }
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_NOT_DATA_LINK /* 135199 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProVerifyingLinkState receive VERIFY_WITH_NOT_DATA_LINK, ignore.");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_DATA_LINK /* 135200 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProVerifyingLinkState receive VERIFY_WITH_DATA_LINK cmd, start transition.");
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine3.transitionTo(huaweiWifiWatchdogStateMachine3.mWifiProLinkMonitoringState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK /* 135201 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProVerifyingLinkState receive STOP_VERIFY_WITH_NOT_DATA_LINK, stop now.");
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine4 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine4.transitionTo(huaweiWifiWatchdogStateMachine4.mWifiProStopVerifyState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_DATA_LINK /* 135202 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProVerifyingLinkState error receive STOP_VERIFY_WITH_DATA_LINK ignore");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_DELAY_OTATCP_TIME_OUT_MSG /* 136168 */:
                    HuaweiWifiWatchdogStateMachine.this.logI("VL OtaPoorTimeOut = true.");
                    this.mIsOtaPoorTimeOut = true;
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_AVOID_TO_WIFI_DELAY_MSG /* 136169 */:
                    HuaweiWifiWatchdogStateMachine.this.logI("VL ReportWiFiGoodAllow = true.");
                    this.mIsReportWiFiGoodAllow = true;
                    break;
                case 151573:
                    processMessageOfRssiPktCntFetchSucc(msg);
                    break;
                case 151574:
                    HuaweiWifiWatchdogStateMachine.this.logI("RSSI_FETCH_FAILED");
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
                        return;
                    }
                    return;
                }
                this.mRssiGoodCount = 0;
            }
        }

        private void tryRecoverWifiByOta(int current_rssi) {
            if (this.mIsReportWiFiGoodAllow) {
                int tmpRssi = current_rssi - HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi;
                boolean isRssiIncrement = tmpRssi >= 15;
                boolean isRoTimerTimeout = this.mIsOtaPoorTimeOut && tmpRssi > HuaweiWifiWatchdogStateMachine.GOOD_LINK_JUDGE_SUB_VAL;
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
                        return;
                    }
                    return;
                }
                this.mRssiGoodCount = 0;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void countDownGoodRssiByTime() {
        if (this.mCurrentBssid.mGoodLinkTargetRssi > this.mCurrentBssid.mTimeElapGoodLinkTargetRssi) {
            long elapTime = SystemClock.elapsedRealtime() - this.mCurrentBssid.mTimeElapBaseTime;
            if (elapTime >= 120000) {
                long subDb = (long) ((int) (elapTime / 120000));
                if (subDb > 0) {
                    int oldRssi = this.mCurrentBssid.mGoodLinkTargetRssi;
                    if (subDb >= ((long) (this.mCurrentBssid.mGoodLinkTargetRssi - this.mCurrentBssid.mTimeElapGoodLinkTargetRssi))) {
                        BssidStatistics bssidStatistics = this.mCurrentBssid;
                        bssidStatistics.mGoodLinkTargetRssi = bssidStatistics.mTimeElapGoodLinkTargetRssi;
                    } else {
                        this.mCurrentBssid.mGoodLinkTargetRssi -= (int) subDb;
                    }
                    logI(" elapse time GL target rssi=" + this.mCurrentBssid.mTimeElapGoodLinkTargetRssi + ", adjust GL rssi from " + oldRssi + " to " + this.mCurrentBssid.mGoodLinkTargetRssi + " by elapse time :" + (2 * subDb) + " Min.");
                    BssidStatistics bssidStatistics2 = this.mCurrentBssid;
                    bssidStatistics2.mTimeElapBaseTime = bssidStatistics2.mTimeElapBaseTime + (120000 * subDb);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class WifiProLinkMonitoringState extends State {
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
        private int mLastQosEvaluateResult;
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
        private int mSmoothQosResultCount;
        private float mSwitchScore = 0.0f;
        private boolean networkBadDetected = false;
        private float noHomeAPSwitchScore = 0.0f;

        WifiProLinkMonitoringState() {
        }

        private void initPktChkPara() {
            this.mPktChkTxbad = 0;
            this.mPktChkTxgood = 0;
            this.mPktChkRxgood = 0;
            this.mPktChkCnt = 0;
            this.mPktChkBadCnt = 0;
            HuaweiWifiWatchdogStateMachine.this.mLastDnsFailCount = 0;
            this.mNetworkDisableCount = 0;
            HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount = 0;
        }

        private void initScoreParaResetCnt() {
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
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logI(" WifiProLinkMonitoringState enter. ");
            this.mRssiBadCount = 0;
            this.mLMRssiWaitCount = 0;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.sendMessage(huaweiWifiWatchdogStateMachine.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, HuaweiWifiWatchdogStateMachine.access$2804(huaweiWifiWatchdogStateMachine), 0));
            HuaweiWifiWatchdogStateMachine.this.mWpLinkMonitorRunning = true;
            initPktChkPara();
            HuaweiWifiWatchdogStateMachine.this.monitorNetworkQos(true);
            this.isLastRssiBad = false;
            this.isLastOTABad = false;
            this.isLastTCPBad = false;
            this.adjustRssiCounter = 0;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.mLastPeriodTcpChkResult = new TcpChkResult();
            resetPoorRssiTh();
            HuaweiWifiWatchdogStateMachine.access$4308(HuaweiWifiWatchdogStateMachine.this);
            HuaweiWifiWatchdogStateMachine.access$708(HuaweiWifiWatchdogStateMachine.this);
            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = HuaweiWifiWatchdogStateMachine.INVALID_RSSI;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine3.logI("new conn, poor rssi th :" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH);
            initScoreParaResetCnt();
            HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = 0;
            HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = 0;
            HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod = 0;
            HuaweiWifiWatchdogStateMachine.access$1108(HuaweiWifiWatchdogStateMachine.this);
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
            HuaweiWifiWatchdogStateMachine.this.logI("WifiProLinkMonitoringState exit.");
            HuaweiWifiWatchdogStateMachine.this.monitorNetworkQos(false);
            HuaweiWifiWatchdogStateMachine.this.mWpLinkMonitorRunning = false;
            HuaweiWifiWatchdogStateMachine.this.mSwitchOutTime = SystemClock.elapsedRealtime();
            HwUidTcpMonitor.getInstance(HuaweiWifiWatchdogStateMachine.this.mContext).notifyWifiMonitorEnabled(false);
        }

        private void resetPeriodState() {
            this.mPktChkCnt = 0;
            this.mPktChkTxbad = 0;
            this.mPktChkTxgood = 0;
            this.mPktChkRxgood = 0;
            HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel = 0;
            this.mRssiBadCount = 0;
            HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod = 0;
        }

        private void networkGoodDetect(int tcpRxPkt, int crssi, int avgrtt, boolean speedGood) {
            if (this.isPoorLinkReported) {
                boolean isRssiBetter = crssi - HuaweiWifiWatchdogStateMachine.this.mGoodDetectBaseRssi >= 15;
                HuaweiWifiWatchdogStateMachine.this.isRttGood = avgrtt > 0 && avgrtt <= 1200;
                if (isRssiBetter || HuaweiWifiWatchdogStateMachine.this.isRttGood || speedGood) {
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
            HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod = false;
            return isSpeedOk;
        }

        private boolean detectNetworkAvailable(int txPkts, int rxPkts, int tcpRetransPkts, String currentBssid) {
            int currentDnsFailCount = 0;
            String dnsFailCountStr = SystemProperties.get("hw.wifipro.dns_fail_count", "0");
            if (dnsFailCountStr == null) {
                HuaweiWifiWatchdogStateMachine.this.logE("detectNetworkAvailable null point error.");
                return true;
            }
            try {
                currentDnsFailCount = Integer.parseInt(dnsFailCountStr);
            } catch (NumberFormatException e) {
                HuaweiWifiWatchdogStateMachine.this.logE("detectNetworkAvailable parseInt err:" + e);
            }
            if (rxPkts != 0) {
                this.mNetworkDisableCount = 0;
            } else if (txPkts > 2) {
                this.mNetworkDisableCount++;
            } else if (currentDnsFailCount - HuaweiWifiWatchdogStateMachine.this.mLastDnsFailCount >= 2) {
                this.mNetworkDisableCount++;
            }
            HuaweiWifiWatchdogStateMachine.this.mLastDnsFailCount = currentDnsFailCount;
            int detectCountRequirement = 2;
            if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager != null && this.mNetworkDisableCount >= 2) {
                this.mHomeApSwichRate = HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.getHomeApSwitchRate(currentBssid);
                this.mIsHomeApSwitchRateReaded = true;
                float f = this.mHomeApSwichRate;
                detectCountRequirement = f <= 0.3f ? 3 : f <= 0.5f ? 3 : f <= 0.7f ? 3 : 2;
            }
            int i = this.mNetworkDisableCount;
            if (i >= detectCountRequirement) {
                HuaweiWifiWatchdogStateMachine.this.logI("detectNetworkAvailable: mNetworkDisableCount = " + this.mNetworkDisableCount);
                return false;
            }
            if (i == 2 && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < -75) {
                HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, -2);
                HuaweiWifiWatchdogStateMachine.this.logI("report maybe poor for detecting network disable 2 periods");
            }
            return true;
        }

        public void resetPoorRssiTh() {
            HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH = -75;
            HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = -75;
            HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH = -75;
        }

        private void sendTHTimeOutDelayMsg(int delayTime) {
            if (HuaweiWifiWatchdogStateMachine.this.getHandler().hasMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT)) {
                HuaweiWifiWatchdogStateMachine.this.logI("remove old RSSI_TH_TIMEOUT msg.");
                HuaweiWifiWatchdogStateMachine.this.getHandler().removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT);
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.sendMessageDelayed(huaweiWifiWatchdogStateMachine.obtainMessage(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT, HuaweiWifiWatchdogStateMachine.access$4304(huaweiWifiWatchdogStateMachine), 0), (long) delayTime);
        }

        private void resetRssiTHValue(int newTHRssi) {
            if (newTHRssi >= -75) {
                resetPoorRssiTh();
                if (HuaweiWifiWatchdogStateMachine.this.getHandler().hasMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT)) {
                    HuaweiWifiWatchdogStateMachine.this.logI("resetRssiTHValue force reset threshold.");
                    HuaweiWifiWatchdogStateMachine.this.getHandler().removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT);
                    return;
                }
                return;
            }
            HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH = newTHRssi;
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH < HuaweiWifiWatchdogStateMachine.POOR_NET_MIN_RSSI_TH) {
                HuaweiWifiWatchdogStateMachine.this.logI("TH set to minimum -85");
                HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH = HuaweiWifiWatchdogStateMachine.POOR_NET_MIN_RSSI_TH;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mPoorNetRssiRealTH = huaweiWifiWatchdogStateMachine.mPoorNetRssiTH - 1;
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.mPoorNetRssiTHNext = huaweiWifiWatchdogStateMachine2.mPoorNetRssiTH;
            }
        }

        private void updatePoorNetRssiTH(int currRssi) {
            if (currRssi != HuaweiWifiWatchdogStateMachine.INVALID_RSSI) {
                if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH < -75) {
                    if (currRssi <= HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.mPoorNetRssiTHNext = huaweiWifiWatchdogStateMachine.mPoorNetRssiTH;
                        resetRssiTHValue(currRssi);
                        HuaweiWifiWatchdogStateMachine.access$5512(HuaweiWifiWatchdogStateMachine.this, 30000);
                        if (HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime > 300000) {
                            HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = 300000;
                        }
                        sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine2.logI("new HIGH SPEED update th rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + ", rth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", nth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext + ", vtime: " + HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                    } else if (currRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext) {
                        HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = currRssi;
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine3.logI("new HIGH SPEED update nth to rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
                    }
                } else if (currRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                    HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = 180000;
                    resetRssiTHValue(currRssi);
                    HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = -75;
                    sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine4 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine4.logI("new HIGH SPEED TH rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + ", rth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", nth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext + ", vtime: " + HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                }
            }
        }

        private void rssiTHValidTimeout() {
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext < -75) {
                resetRssiTHValue(HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
                HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = -75;
                HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = 180000;
                sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("new HIGH SPEED TH timeout, turn to next:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH);
                return;
            }
            resetPoorRssiTh();
            HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED TH timeout reset.");
        }

        private void checkRssiTHBackoff(int currRssi, double retransRate, int txPkts, int retransPkts, int rtt) {
            boolean isRttbad = true;
            HuaweiWifiWatchdogStateMachine.this.isTcpRetranBad = (retransRate >= 0.4d && txPkts > 3) || (txPkts <= 3 && retransPkts >= 3);
            if (rtt <= 2000 && rtt != 0) {
                isRttbad = false;
            }
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH <= currRssi && HuaweiWifiWatchdogStateMachine.this.isTcpRetranBad && isRttbad) {
                resetRssiTHValue(HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + 5);
                HuaweiWifiWatchdogStateMachine.this.logI("after backoff rssi th: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", next TH:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
            }
        }

        private float getRssiSWRate(int rssi) {
            int len = HuaweiWifiWatchdogStateMachine.RSSI_RATE_TBL.length;
            if (rssi < -75) {
                return 1.0f;
            }
            for (int loop = 0; loop < len; loop++) {
                if (rssi >= HuaweiWifiWatchdogStateMachine.RSSI_RATE_TBL[loop].mRssiVal) {
                    return HuaweiWifiWatchdogStateMachine.RSSI_RATE_TBL[loop].mSwRate;
                }
            }
            return 1.0f;
        }

        private void ssidChangeDetection() {
            if (HuaweiWifiWatchdogStateMachine.this.mCurrSSID == null || HuaweiWifiWatchdogStateMachine.this.mWifiInfo == null) {
                HuaweiWifiWatchdogStateMachine.this.logI("SSID is null, reset SGC.");
                resetSpdGoodCounter();
            } else if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID() == null || HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID().equals(HuaweiWifiWatchdogStateMachine.this.mCurrSSID)) {
                spdGoodParameterAge(HuaweiWifiWatchdogStateMachine.this.mSwitchOutTime);
            } else {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("SSID change to:" + StringUtilEx.safeDisplaySsid(HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID()) + ", old SSID: " + StringUtilEx.safeDisplaySsid(HuaweiWifiWatchdogStateMachine.this.mCurrSSID) + ", reset SGC.");
                resetSpdGoodCounter();
            }
        }

        private void resetSpdGoodCounter() {
            this.mGoodSpeedRate = 1.0f;
            HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore = 0.0f;
            HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = 0.0f;
            HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
        }

        private void spdGoodParameterAge(long swoTime) {
            long reduceCount = 0;
            if (swoTime > 0) {
                reduceCount = (SystemClock.elapsedRealtime() - swoTime) / 64000;
                if (((float) reduceCount) >= HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount || reduceCount < 0) {
                    HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = 0.0f;
                } else {
                    HuaweiWifiWatchdogStateMachine.access$5924(HuaweiWifiWatchdogStateMachine.this, (float) reduceCount);
                }
                HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore = 0.0f;
                HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logI("SSID: " + StringUtilEx.safeDisplaySsid(HuaweiWifiWatchdogStateMachine.this.mCurrSSID) + "not changed, not reset SGC. reduceCount:" + reduceCount + ", SGC:" + HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount);
        }

        private float getSpdGoodSWRate(float count) {
            int len = HuaweiWifiWatchdogStateMachine.GOOD_SPD_COUNT_RATE_TBL.length;
            for (int loop = 0; loop < len; loop++) {
                if (count > HuaweiWifiWatchdogStateMachine.GOOD_SPD_COUNT_RATE_TBL[loop].goodCount) {
                    return HuaweiWifiWatchdogStateMachine.GOOD_SPD_COUNT_RATE_TBL[loop].swRate;
                }
            }
            return 1.0f;
        }

        private void updateSpdGoodCounter() {
            if (HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore > 0.0f) {
                if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount < HuaweiWifiWatchdogStateMachine.MAX_SPD_GOOD_COUNT) {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    HuaweiWifiWatchdogStateMachine.access$5916(huaweiWifiWatchdogStateMachine, huaweiWifiWatchdogStateMachine.mPeriodGoodSpdScore);
                    if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount > HuaweiWifiWatchdogStateMachine.MAX_SPD_GOOD_COUNT) {
                        HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = HuaweiWifiWatchdogStateMachine.MAX_SPD_GOOD_COUNT;
                    }
                }
                HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore = 0.0f;
                HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
            } else if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount > 0.0f) {
                HuaweiWifiWatchdogStateMachine.access$6008(HuaweiWifiWatchdogStateMachine.this);
                if (((float) HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount) >= HuaweiWifiWatchdogStateMachine.BACKOFF_NOT_SPD_GOOD_PERIOD_COUNT) {
                    HuaweiWifiWatchdogStateMachine.access$5910(HuaweiWifiWatchdogStateMachine.this);
                    if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount < 0.0f) {
                        HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = 0.0f;
                    }
                    HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine2.logI("spd good count backoff 1 to:" + HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount);
                }
            }
            this.mGoodSpeedRate = getSpdGoodSWRate(HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount);
        }

        private float getCurrPeriodScore(int rssi, boolean rssiTcpBad, boolean otaBad, boolean tcpBad, boolean veryBadRTT, String currentBssid) {
            float homeApRate;
            if (this.mIsHomeApSwitchRateReaded) {
                homeApRate = this.mHomeApSwichRate;
            } else {
                homeApRate = HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.getHomeApSwitchRate(currentBssid);
            }
            float biggestScore = getBiggestScore(rssiTcpBad, otaBad, tcpBad, veryBadRTT);
            this.mRssiRate = getRssiSWRate(rssi);
            float retScore = this.mRssiRate * biggestScore;
            if (1.0f != HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate) {
                retScore *= HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate;
            }
            float f = this.mGoodSpeedRate;
            if (1.0f != f && this.mPktChkBadCnt == 0) {
                retScore *= f;
            }
            this.noHomeAPSwitchScore += retScore;
            if (1.0f != homeApRate && rssi >= -75) {
                retScore *= homeApRate;
            }
            HuaweiWifiWatchdogStateMachine.this.logI("Get rate: Rssi" + rssi + ", RssiRate:" + this.mRssiRate + ", HighDataFlow:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate + ", History: " + this.mGoodSpeedRate + ", add score: " + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr((double) retScore) + ", Homerate: " + homeApRate + ", mPktChkBadCnt:" + this.mPktChkBadCnt);
            return retScore;
        }

        private float getBiggestScore(boolean rssiTcpBad, boolean otaBad, boolean tcpBad, boolean veryBadRTT) {
            float veryBadRTTScore = 0.0f;
            if (veryBadRTT) {
                veryBadRTTScore = 3.0f;
            }
            float rssiBadScore = 0.0f;
            if (rssiTcpBad) {
                rssiBadScore = 5.0f;
            }
            float otaBadScore = 0.0f;
            if (otaBad) {
                otaBadScore = HuaweiWifiWatchdogStateMachine.MAY_BE_POOR_TH;
            }
            float tcpBadScore = 0.0f;
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
            return veryBadRTTScore > biggestScore ? veryBadRTTScore : biggestScore;
        }

        private void updateHighDataFlowRate() {
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario != 1 || HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection <= 0) {
                if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == 2 && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 0) {
                    if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 50) {
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 0.0f;
                    } else {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.mHighDataFlowRate = (((float) (50 - huaweiWifiWatchdogStateMachine.mHighDataFlowProtection)) * HuaweiWifiWatchdogStateMachine.STREAMING_STEP_RATE) + 0.3f;
                    }
                }
            } else if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 20) {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 0.0f;
            } else {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.mHighDataFlowRate = (((float) (20 - huaweiWifiWatchdogStateMachine2.mHighDataFlowProtection)) * HuaweiWifiWatchdogStateMachine.DOWNLOAD_STEP_RATE) + 0.6f;
            }
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate > 1.0f || HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate < 0.0f) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine3.logI("wrong rate! mHighDataFlowRate  = %d" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate);
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 1.0f;
            }
        }

        private void handleHighDataFlowEvery4Period() {
            long currentHighDataFlowRxBytes = TrafficStats.getRxBytes(HuaweiWifiWatchdogStateMachine.WLAN_IFACE);
            long highDataFlowRxBytes = currentHighDataFlowRxBytes - HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes;
            HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes = currentHighDataFlowRxBytes;
            int lastScenario = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario;
            if (highDataFlowRxBytes >= 3145728) {
                int i = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario;
                if (i == 0) {
                    if (highDataFlowRxBytes >= 5242880) {
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 1;
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 40;
                    } else if (HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow) {
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 1;
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 40;
                    }
                    HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow = true;
                } else if (i != 1) {
                    if (i != 2) {
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 0;
                        HuaweiWifiWatchdogStateMachine.this.logI("wrong high data scenario, reset to HIGH_DATA_FLOW_NONE ");
                    } else if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter >= 2) {
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.LONGEST_STREAMING_PROTECTION;
                    }
                } else if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter >= 2) {
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 2;
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.LONGEST_STREAMING_PROTECTION;
                } else {
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 40;
                }
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter = 0;
            } else {
                HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow = false;
                HuaweiWifiWatchdogStateMachine.access$6908(HuaweiWifiWatchdogStateMachine.this);
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logI("high data flow: protection_counter = " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection + ",  not_detect_counter = " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter);
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.logI("high data flow scenario: " + lastScenario + " --> " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario + " rx bytes =" + (highDataFlowRxBytes / 1024) + "KB");
        }

        private void handleHighDataFlow() {
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 0) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.mHighDataFlowProtection -= 4;
                if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection < 0) {
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 0;
                }
                if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection == 0) {
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 0;
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter = 0;
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 1.0f;
                    HuaweiWifiWatchdogStateMachine.this.logI("mHighDataFlowProtection = 0, reset to HIGH_DATA_FLOW_NONE");
                }
            }
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowPeriodCounter == 0) {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes = TrafficStats.getRxBytes(HuaweiWifiWatchdogStateMachine.WLAN_IFACE);
            }
            HuaweiWifiWatchdogStateMachine.access$7008(HuaweiWifiWatchdogStateMachine.this);
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowPeriodCounter % 4 == 0) {
                handleHighDataFlowEvery4Period();
            }
            updateHighDataFlowRate();
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == 2 && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate >= 0.6f) {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 1;
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
            Context context = HuaweiWifiWatchdogStateMachine.this.mContext;
            Toast.makeText(context, "RSSI:" + mrssi + " TotalScore:" + this.mSwitchScore + "  BadCount:" + this.mPktChkBadCnt + System.lineSeparator() + scenario + ": Protection:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection + " Rate:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate, 1).show();
        }

        private void smoothConsecutiveQosResults(int currentQosLevel) {
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logI("smoothConsecutiveQosResults, currentQosLevel = " + currentQosLevel);
            int i = this.mSmoothQosResultCount;
            if (i == 0) {
                this.mSmoothQosResultCount = i + 1;
                this.mLastQosEvaluateResult = currentQosLevel;
                return;
            }
            this.mSmoothQosResultCount = 0;
            int i2 = this.mLastQosEvaluateResult;
            if (currentQosLevel > i2) {
                i2 = currentQosLevel;
            }
            updateQoeLevel(i2);
        }

        private void updateQoeLevel(int level) {
            if (level != 0) {
                if (level == HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel) {
                    HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                    return;
                }
                if (level == 2 && HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel != 3) {
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = level;
                    HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                } else if (Math.abs(level - HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel) == 2) {
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = 2;
                    HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                } else if (HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel == level) {
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = level;
                    HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                } else {
                    HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                    return;
                }
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("Leave updateQoeLevel level = " + HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel);
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.postEvent(10, 1, huaweiWifiWatchdogStateMachine2.mCurrentBqeLevel);
            }
        }

        private int computeQosLevelUpdateSpeedLevel() {
            if (HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod > 358400) {
                return 3;
            }
            if (HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod > 20480) {
                return 2;
            }
            return 0;
        }

        private int computeQosLevelUpdateRttLevel(int tcpRtt, int tcpRttPkts) {
            if (tcpRttPkts < HuaweiWifiWatchdogStateMachine.this.QOE_LEVEL_RTT_MIN_PKT || tcpRtt == 0) {
                return 0;
            }
            if (tcpRtt <= 1200) {
                return 3;
            }
            if (tcpRtt <= 4800) {
                return 2;
            }
            return 1;
        }

        private void computeQosLevel(int tcpRtt, int tcpRttPkts, int rssi, boolean isLossRateBad) {
            int qosLevel;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logI("start computeQosLevel tcpRtt = " + tcpRtt + " ,rssi = " + rssi + " ,mBestSpeedInPeriod = " + HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod);
            int speedLevel = computeQosLevelUpdateSpeedLevel();
            int rttLevel = computeQosLevelUpdateRttLevel(tcpRtt, tcpRttPkts);
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
                smoothConsecutiveQosResults(qosLevel);
            }
        }

        private void tryBackOffScore(long periodMaxSpeed, int avgRtt) {
            if (this.mSwitchScore > 0.0f) {
                boolean isScoreBackoffRTTGood = true;
                boolean isScoreBackoffSpeedGood = periodMaxSpeed > 51200;
                if (avgRtt <= 0 || avgRtt > 1200) {
                    isScoreBackoffRTTGood = false;
                }
                if (!isScoreBackoffSpeedGood) {
                    float f = this.mSwitchScore;
                    if (f >= 1.0f) {
                        if (isScoreBackoffRTTGood) {
                            this.mSwitchScore = f * 0.5f;
                            this.noHomeAPSwitchScore *= 0.5f;
                            HuaweiWifiWatchdogStateMachine.this.logI("Good RTT:score backoff newScore=" + this.mSwitchScore);
                            return;
                        }
                        this.mSwitchScore = f * HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_RATE;
                        this.noHomeAPSwitchScore *= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_RATE;
                        HuaweiWifiWatchdogStateMachine.this.logI("score backoff newSco=" + this.mSwitchScore);
                        return;
                    }
                }
                resetPoorNetState();
                return;
            }
            resetPoorNetState();
        }

        private void handleRssiChanged(Message msg) {
            if (msg.arg2 == HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken) {
                long nowTime = SystemClock.elapsedRealtime();
                int newRssiVal = msg.arg1;
                if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi || nowTime - this.lastRSSIUpdateTime >= 4000) {
                    HuaweiWifiWatchdogStateMachine.this.mCurrRssi = newRssiVal;
                } else {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mCurrRssi = ((huaweiWifiWatchdogStateMachine.mCurrRssi * 1) + newRssiVal) / 2;
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
                    HuaweiWifiWatchdogStateMachine.this.logI("MonitoringState WIFI not connected, skip fetch RSSI.");
                } else {
                    if (!HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk) {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.logI("WP Link Monitor State first check bssid:" + WifiProCommonUtils.safeDisplayBssid(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid) + ", call newLinkDetected");
                        HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.newLinkDetected();
                        HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk = true;
                    }
                    if (this.mLMRssiWaitCount <= 0) {
                        HuaweiWifiWatchdogStateMachine.this.mWsmChannel.sendMessage(151572);
                        this.mLMRssiWaitCount = 15;
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.logI("wait rssi request respond, not send new request.");
                        this.mLMRssiWaitCount = (int) (((long) this.mLMRssiWaitCount) - (HuaweiWifiWatchdogStateMachine.this.mLinkSampleIntervalTime / HuaweiWifiWatchdogStateMachine.MS_OF_ONE_SECOND));
                    }
                }
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.sendMessageDelayed(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, HuaweiWifiWatchdogStateMachine.access$2804(huaweiWifiWatchdogStateMachine2), 0), HuaweiWifiWatchdogStateMachine.this.mLinkSampleIntervalTime);
                return;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine3.logI("MonitoringState msg arg1:" + msg.arg1 + " != token:" + HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken + ", ignore this command.");
        }

        public void updateCurrentRssi(RssiPacketCountInfo info, long now) {
            HuaweiWifiWatchdogStateMachine.this.sendCurrentAPRssi(info.rssi);
            int rssi = info.rssi;
            HuaweiWifiWatchdogStateMachine.access$1108(HuaweiWifiWatchdogStateMachine.this);
            if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi || now - this.lastRSSIUpdateTime >= 4000) {
                HuaweiWifiWatchdogStateMachine.this.mCurrRssi = rssi;
            } else {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.mCurrRssi = ((huaweiWifiWatchdogStateMachine.mCurrRssi * 1) + rssi) / 2;
            }
            this.lastRSSIUpdateTime = now;
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine != null) {
                HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine.querySampleRtt();
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.mLastSampleRssi = huaweiWifiWatchdogStateMachine2.mCurrRssi;
            }
        }

        public boolean calculateTcpQuality(int txbad, int txgood, int rxgood, long now, int rssi) {
            int dgood = txgood - this.mLastTxGood;
            HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mLastTimeSample = now;
            int dbad = txbad - this.mLastTxBad;
            int drxgood = rxgood - this.mLastRxGood;
            this.mLastTxBad = txbad;
            this.mLastTxGood = txgood;
            this.mLastRxGood = rxgood;
            this.mIsHomeApSwitchRateReaded = false;
            if (now - HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mLastTimeSample >= HuaweiWifiWatchdogStateMachine.this.mLinkSampleIntervalTime * 2) {
                return false;
            }
            int dtotal = dbad + dgood;
            if (dtotal > 0) {
                HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.updateLoss(rssi, ((double) dbad) / ((double) dtotal), dtotal);
            }
            if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi <= HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi) {
                this.mRssiBadCount++;
            }
            this.mPktChkTxbad += dbad;
            this.mPktChkTxgood += dgood;
            this.mPktChkRxgood += drxgood;
            this.mPktChkCnt++;
            return true;
        }

        public void calculateTxRxInfo() {
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mCurTcpChkResult = huaweiWifiWatchdogStateMachine.getQueryTcpResult();
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.mProcessedTcpResult = new ProcessedTcpResult();
            if (HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult == null) {
                HuaweiWifiWatchdogStateMachine.this.logI("LastMinTcpChkResult is null, new it.");
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine3.mLastPeriodTcpChkResult = new TcpChkResult();
            }
            if (HuaweiWifiWatchdogStateMachine.this.mCurTcpChkResult == null || HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult == null) {
                HuaweiWifiWatchdogStateMachine.this.logI("read TCP Result failed. not calc tcp 30s pkt.");
            } else {
                HuaweiWifiWatchdogStateMachine.this.logI("LastMinTcpChkResult is not null, get it.");
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine4 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine4.getCurrTcpRtt(huaweiWifiWatchdogStateMachine4.mProcessedTcpResult);
                if (HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mValueIsSet) {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine5 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine5.tcpTxPkts = huaweiWifiWatchdogStateMachine5.mCurTcpChkResult.mTcpTxPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpTxPkts;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine6 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine6.tcpRxPkts = huaweiWifiWatchdogStateMachine6.mCurTcpChkResult.mTcpRxPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpRxPkts;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine7 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine7.tcpRetransPkts = huaweiWifiWatchdogStateMachine7.mCurTcpChkResult.mTcpRetransPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpRetransPkts;
                    HuaweiWifiWatchdogStateMachine.this.lastPeriodTcpResultValid = true;
                } else {
                    HuaweiWifiWatchdogStateMachine.this.logI("not calc TCP period pkt, last result is null.");
                }
            }
            if (HuaweiWifiWatchdogStateMachine.this.mCurTcpChkResult != null) {
                HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.reset();
                HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.copyFrom(HuaweiWifiWatchdogStateMachine.this.mCurTcpChkResult);
            }
            if (HuaweiWifiWatchdogStateMachine.this.isRssiBad) {
                HuaweiWifiWatchdogStateMachine.this.logI("rssi bad, mrssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrRssi + " < TargetRssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi);
                if (HuaweiWifiWatchdogStateMachine.this.isTCPBad || HuaweiWifiWatchdogStateMachine.this.isOTABad || this.isLastTCPBad || this.isLastOTABad || HuaweiWifiWatchdogStateMachine.this.tcpTxPkts <= 3 || HuaweiWifiWatchdogStateMachine.this.tcpRxPkts <= 0) {
                    this.adjustRssiCounter = 0;
                    return;
                }
                this.adjustRssiCounter++;
                if (this.adjustRssiCounter >= 8) {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine8 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine8.adjustTargetPoorRssi(huaweiWifiWatchdogStateMachine8.mCurrRssi);
                    this.adjustRssiCounter = 0;
                }
            }
        }

        private void chrStatistics() {
            short wifiNetSpeed;
            if (this.mSwitchScore > HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE || this.homeAPAddPeriodCount > 1) {
                if (HuaweiWifiWatchdogStateMachine.this.isRssiBad && this.isLastRssiBad) {
                    HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 0;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.wpPoorLinkLevelCalcByRssi(huaweiWifiWatchdogStateMachine.lossRate, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt);
                } else {
                    HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 1;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine2.wpPoorLinkLevelCalcByTcp(huaweiWifiWatchdogStateMachine2.lossRate, HuaweiWifiWatchdogStateMachine.this.tcpRetransRate, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt);
                }
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine3.mWifiPoorRssi = huaweiWifiWatchdogStateMachine3.mCurrRssi;
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine4 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine4.mGoodDetectBaseRssi = huaweiWifiWatchdogStateMachine4.mCurrRssi;
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine5 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine5.logI("mWifiPoorRssi = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi + ", WifiPoorReason = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason);
                if (!(HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager == null || HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord == null || HuaweiWifiWatchdogStateMachine.this.pTot <= 8)) {
                    HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mOtaPacketDropRate = (short) ((int) (HuaweiWifiWatchdogStateMachine.this.lossRate * 1000.0d));
                }
                if (HuaweiWifiWatchdogStateMachine.this.mCurrSSID != null) {
                    HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mRoApSsid = HuaweiWifiWatchdogStateMachine.this.mCurrSSID;
                }
                if (HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager == null || HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord == null) {
                    HuaweiWifiWatchdogStateMachine.this.logE("chr obj null error.");
                    return;
                }
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.resetAllParameters();
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mRssiValue = (short) HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mRttAvg = (short) HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt;
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpInSegs = (short) HuaweiWifiWatchdogStateMachine.this.tcpRxPkts;
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpOutSegs = (short) HuaweiWifiWatchdogStateMachine.this.tcpTxPkts;
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpRetransSegs = (short) HuaweiWifiWatchdogStateMachine.this.tcpRetransPkts;
                synchronized (HuaweiWifiWatchdogStateMachine.this.mLock) {
                    wifiNetSpeed = (short) ((int) (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed / 1024));
                }
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mWifiNetSpeed = wifiNetSpeed;
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mIpqLevel = (short) HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel;
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mHistoryQuilityRoRate = (short) ((int) (this.mGoodSpeedRate * 1000.0f));
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mHighDataRateRoRate = (short) ((int) (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate * 1000.0f));
                HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mCreditScoreRoRate = 1000;
                HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.setBQERoveOutReason(HuaweiWifiWatchdogStateMachine.this.isPoorLinkForLowRssi, HuaweiWifiWatchdogStateMachine.this.isWlanCongestion, HuaweiWifiWatchdogStateMachine.this.isTCPBad, HuaweiWifiWatchdogStateMachine.this.isVeryBadRTT, HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord);
            }
        }

        public void calculateTcpRetranRate() {
            if (HuaweiWifiWatchdogStateMachine.this.tcpTxPkts > 0) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.tcpRetransRate = ((double) huaweiWifiWatchdogStateMachine.tcpRetransPkts) / ((double) HuaweiWifiWatchdogStateMachine.this.tcpTxPkts);
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            StringBuilder sb = new StringBuilder();
            sb.append("PTcp RTT:");
            sb.append(HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt);
            sb.append(", rtt pkt=");
            sb.append(HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mTotPkt);
            sb.append(", tcp_rx=");
            sb.append(HuaweiWifiWatchdogStateMachine.this.tcpRxPkts);
            sb.append(", tcp_tx=");
            sb.append(HuaweiWifiWatchdogStateMachine.this.tcpTxPkts);
            sb.append(", tcp_reTran=");
            sb.append(HuaweiWifiWatchdogStateMachine.this.tcpRetransPkts);
            sb.append(", rtRate=");
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
            sb.append(huaweiWifiWatchdogStateMachine3.formatFloatToStr(huaweiWifiWatchdogStateMachine3.tcpRetransRate));
            huaweiWifiWatchdogStateMachine2.logI(sb.toString());
            boolean z = false;
            if (HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount > 0 && HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.addHistoryHSCount(HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount);
                HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount = 0;
            }
            if (HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt < 1000 && HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mTotPkt > 2 && HuaweiWifiWatchdogStateMachine.this.lossRate < HuaweiWifiWatchdogStateMachine.PKT_CHK_POOR_LINK_MIN_LOSE_RATE && HuaweiWifiWatchdogStateMachine.this.pTot > 8 && HuaweiWifiWatchdogStateMachine.this.tcpRetransRate < 0.1d && HuaweiWifiWatchdogStateMachine.this.tcpTxPkts > 2) {
                HuaweiWifiWatchdogStateMachine.this.isRttGood = true;
            }
            if (this.mRssiBadCount >= 1) {
                HuaweiWifiWatchdogStateMachine.this.isRssiBad = true;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine4 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine4.isTcpRetranBad = (huaweiWifiWatchdogStateMachine4.tcpRetransRate >= 0.2d && HuaweiWifiWatchdogStateMachine.this.tcpTxPkts > 3) || (HuaweiWifiWatchdogStateMachine.this.tcpTxPkts <= 3 && HuaweiWifiWatchdogStateMachine.this.tcpRetransPkts >= 3);
            boolean isRttBadForLowRssi = HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt > 2000 && HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mTotPkt > 2;
            boolean lossRateVeryBad = HuaweiWifiWatchdogStateMachine.this.lossRate >= 0.4d && HuaweiWifiWatchdogStateMachine.this.pTot >= 50;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine5 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine5.isPoorLinkForLowRssi = huaweiWifiWatchdogStateMachine5.mCurrRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH && (isRttBadForLowRssi || lossRateVeryBad);
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine6 = HuaweiWifiWatchdogStateMachine.this;
            if (huaweiWifiWatchdogStateMachine6.lossRate > HuaweiWifiWatchdogStateMachine.PKT_CHK_POOR_LINK_MIN_LOSE_RATE && HuaweiWifiWatchdogStateMachine.this.pTot > 8) {
                z = true;
            }
            huaweiWifiWatchdogStateMachine6.isWlanLossRateBad = z;
            calculateTcpRetranRateExtra();
        }

        private void calculateTcpRetranRateExtra() {
            if (!HuaweiWifiWatchdogStateMachine.this.isPoorLinkForLowRssi && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.this.LOW_RSSI_TH && HuaweiWifiWatchdogStateMachine.this.isWlanLossRateBad) {
                HuaweiWifiWatchdogStateMachine.this.isPoorLinkForLowRssi = true;
            }
            boolean isRttBadForCongestion = (HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt > 3000 || HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt == 0) && (HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mInadequateAvgRtt > 1200 || HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mInadequateAvgRtt == 0);
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.isWlanCongestion = (huaweiWifiWatchdogStateMachine.isWlanLossRateBad && isRttBadForCongestion) || (HuaweiWifiWatchdogStateMachine.this.isWlanLossRateBad && HuaweiWifiWatchdogStateMachine.this.isTcpRetranBad);
            HuaweiWifiWatchdogStateMachine.this.isVeryBadRTT = false;
            if (HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt > HuaweiWifiWatchdogStateMachine.VERY_BAD_RTT_TH_FOR_TCP_BAD && HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mTotPkt < 50) {
                HuaweiWifiWatchdogStateMachine.this.isVeryBadRTT = true;
            }
            HuaweiWifiWatchdogStateMachine.this.isOTABad = false;
            if (HuaweiWifiWatchdogStateMachine.this.isPoorLinkForLowRssi || HuaweiWifiWatchdogStateMachine.this.isWlanCongestion) {
                if (!HuaweiWifiWatchdogStateMachine.this.isRttGood) {
                    HuaweiWifiWatchdogStateMachine.this.isOTABad = true;
                } else {
                    HuaweiWifiWatchdogStateMachine.this.isPoorLinkForLowRssi = false;
                    HuaweiWifiWatchdogStateMachine.this.isWlanCongestion = false;
                }
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.isTCPBad = !huaweiWifiWatchdogStateMachine2.isRttGood && (HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel == 1 || HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel == 2);
            if (HuaweiWifiWatchdogStateMachine.this.isRssiBad) {
                HuaweiWifiWatchdogStateMachine.this.logI("rssi bad, mrssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrRssi + " < TargetRssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi);
                if (HuaweiWifiWatchdogStateMachine.this.isTCPBad || HuaweiWifiWatchdogStateMachine.this.isOTABad || this.isLastTCPBad || this.isLastOTABad || HuaweiWifiWatchdogStateMachine.this.tcpTxPkts <= 3 || HuaweiWifiWatchdogStateMachine.this.tcpRxPkts <= 0) {
                    this.adjustRssiCounter = 0;
                    return;
                }
                this.adjustRssiCounter++;
                if (this.adjustRssiCounter >= 8) {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine3.adjustTargetPoorRssi(huaweiWifiWatchdogStateMachine3.mCurrRssi);
                    this.adjustRssiCounter = 0;
                }
            }
        }

        public boolean calcTcpTxRx() {
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mCurTcpChkResult = huaweiWifiWatchdogStateMachine.getQueryTcpResult();
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.mProcessedTcpResult = new ProcessedTcpResult();
            HuaweiWifiWatchdogStateMachine.this.lastPeriodTcpResultValid = false;
            if (HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult == null) {
                HuaweiWifiWatchdogStateMachine.this.logI("LastMinTcpChkResult is null, new it.");
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine3.mLastPeriodTcpChkResult = new TcpChkResult();
            }
            if (HuaweiWifiWatchdogStateMachine.this.mCurTcpChkResult == null || HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult == null) {
                HuaweiWifiWatchdogStateMachine.this.logI("read TCP Result failed. not calc tcp 30s pkt.");
            } else {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine4 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine4.getCurrTcpRtt(huaweiWifiWatchdogStateMachine4.mProcessedTcpResult);
                if (HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mValueIsSet) {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine5 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine5.tcpTxPkts = huaweiWifiWatchdogStateMachine5.mCurTcpChkResult.mTcpTxPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpTxPkts;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine6 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine6.tcpRxPkts = huaweiWifiWatchdogStateMachine6.mCurTcpChkResult.mTcpRxPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpRxPkts;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine7 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine7.tcpRetransPkts = huaweiWifiWatchdogStateMachine7.mCurTcpChkResult.mTcpRetransPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpRetransPkts;
                    HuaweiWifiWatchdogStateMachine.this.lastPeriodTcpResultValid = true;
                    if (HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager != null) {
                        HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.updateTcpTxRxInfo(HuaweiWifiWatchdogStateMachine.this.tcpTxPkts, HuaweiWifiWatchdogStateMachine.this.tcpRetransPkts, HuaweiWifiWatchdogStateMachine.this.tcpRxPkts, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mTotPkt);
                    }
                } else {
                    HuaweiWifiWatchdogStateMachine.this.logI("not calc TCP period pkt, last result is null.");
                }
            }
            if (HuaweiWifiWatchdogStateMachine.this.mCurTcpChkResult != null) {
                HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.reset();
                HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.copyFrom(HuaweiWifiWatchdogStateMachine.this.mCurTcpChkResult);
            }
            if (this.isFirstEnterMontoringState) {
                this.isFirstEnterMontoringState = false;
                return false;
            }
            calculateTcpRetranRate();
            if (handleTcpCondition()) {
                return false;
            }
            return true;
        }

        private boolean handleTcpCondition() {
            if (isNetSpeedOk(HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt)) {
                networkGoodDetect(1, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, 0, true);
                computeQosLevel(HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mTotPkt, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.isWlanLossRateBad);
                resetPeriodState();
                if (HuaweiWifiWatchdogStateMachine.DDBG_TOAST_DISPLAY) {
                    debugToast(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                }
                this.mNetworkDisableCount = 0;
                return true;
            } else if (HuaweiWifiWatchdogStateMachine.this.lastPeriodTcpResultValid && !detectNetworkAvailable(HuaweiWifiWatchdogStateMachine.this.tcpTxPkts, HuaweiWifiWatchdogStateMachine.this.tcpRxPkts, HuaweiWifiWatchdogStateMachine.this.tcpRetransPkts, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid)) {
                HuaweiWifiWatchdogStateMachine.this.logI("maybe no internet REQUEST_WIFI_INET_CHECK.");
                if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi >= -75) {
                    HuaweiWifiWatchdogStateMachine.this.sendResultMsgToQM(-103);
                    HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 1;
                    HuaweiWifiWatchdogStateMachine.access$10308(HuaweiWifiWatchdogStateMachine.this);
                } else {
                    HuaweiWifiWatchdogStateMachine.this.sendResultMsgToQM(-104);
                    HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 0;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mWifiPoorRssi = huaweiWifiWatchdogStateMachine.mCurrRssi;
                }
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.mGoodDetectBaseRssi = huaweiWifiWatchdogStateMachine2.mCurrRssi;
                this.isPoorLinkReported = true;
                this.goodPeriodCounter = 0;
                return true;
            } else if (HuaweiWifiWatchdogStateMachine.this.tcpTxPkts > 2 || HuaweiWifiWatchdogStateMachine.this.tcpRxPkts > 2) {
                return false;
            } else {
                computeQosLevel(HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mTotPkt, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.isWlanLossRateBad);
                return true;
            }
        }

        public void handleSsidFetchSuccessed(Message msg) {
            if (!HuaweiWifiWatchdogStateMachine.this.mIsWifiSwitchRobotAlgorithmEnabled) {
                HwUidTcpMonitor.getInstance(HuaweiWifiWatchdogStateMachine.this.mContext).updateUidTcpStatistics();
            }
            this.mLMRssiWaitCount = 0;
            if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo == null || HuaweiWifiWatchdogStateMachine.this.mCurrentBssid == null || msg.obj == null) {
                HuaweiWifiWatchdogStateMachine.this.logE("null error or wifi not connected, ignore RSSI_PKTCNT_FETCH event.");
                return;
            }
            RssiPacketCountInfo info = (RssiPacketCountInfo) msg.obj;
            long now = SystemClock.elapsedRealtime();
            updateCurrentRssi(info, now);
            if (calculateTcpQuality(info.txbad, info.txgood, info.rxgood, now, info.rssi) && this.mPktChkCnt >= 1) {
                HuaweiWifiWatchdogStateMachine.this.pTot = this.mPktChkTxgood + this.mPktChkTxbad;
                if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null) {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mCurrSSID = huaweiWifiWatchdogStateMachine.mWifiInfo.getSSID();
                }
                updateSpdGoodCounter();
                handleHighDataFlow();
                if (!handlePtot()) {
                    if (HuaweiWifiWatchdogStateMachine.DDBG_TOAST_DISPLAY) {
                        debugToast(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                    }
                    resetPeriodState();
                }
            }
        }

        private boolean handlePtot() {
            if (HuaweiWifiWatchdogStateMachine.this.pTot > 0) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.lossRate = ((double) this.mPktChkTxbad) / ((double) huaweiWifiWatchdogStateMachine.pTot);
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                StringBuilder sb = new StringBuilder();
                sb.append("POta txb txg rxg:");
                sb.append(this.mPktChkTxbad);
                sb.append(", ");
                sb.append(this.mPktChkTxgood);
                sb.append(", ");
                sb.append(this.mPktChkRxgood);
                sb.append(". Lr =");
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                sb.append(huaweiWifiWatchdogStateMachine3.formatFloatToStr(huaweiWifiWatchdogStateMachine3.lossRate * 100.0d));
                sb.append("% Totpkt=");
                sb.append(HuaweiWifiWatchdogStateMachine.this.pTot);
                huaweiWifiWatchdogStateMachine2.logI(sb.toString());
                if (!calcTcpTxRx()) {
                    return true;
                }
                handleBadCondition();
                this.isLastRssiBad = HuaweiWifiWatchdogStateMachine.this.isRssiBad;
                this.isLastOTABad = HuaweiWifiWatchdogStateMachine.this.isOTABad;
                this.isLastTCPBad = HuaweiWifiWatchdogStateMachine.this.isTCPBad;
                return false;
            }
            handleNoTxPeriodInLinkMonitor();
            return false;
        }

        private void handleBadCondition() {
            if (HuaweiWifiWatchdogStateMachine.this.isOTABad || HuaweiWifiWatchdogStateMachine.this.isTCPBad || HuaweiWifiWatchdogStateMachine.this.isVeryBadRTT) {
                HuaweiWifiWatchdogStateMachine.this.logI("rs ota tcp lr rttvb bad: " + HuaweiWifiWatchdogStateMachine.this.isPoorLinkForLowRssi + ", " + HuaweiWifiWatchdogStateMachine.this.isWlanCongestion + ", " + HuaweiWifiWatchdogStateMachine.this.isTCPBad + ", " + HuaweiWifiWatchdogStateMachine.this.isRssiBad + ", " + HuaweiWifiWatchdogStateMachine.this.isVeryBadRTT + "; rsth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH);
                this.mSwitchScore = this.mSwitchScore + getCurrPeriodScore(HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.isPoorLinkForLowRssi, HuaweiWifiWatchdogStateMachine.this.isWlanCongestion, HuaweiWifiWatchdogStateMachine.this.isTCPBad, HuaweiWifiWatchdogStateMachine.this.isVeryBadRTT, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid);
                this.mPktChkBadCnt = this.mPktChkBadCnt + 1;
                smoothConsecutiveQosResults(1);
                if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager != null && HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.getIsHomeAP(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid)) {
                    this.networkBadDetected = true;
                }
                if (this.noHomeAPSwitchScore > HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE && this.mSwitchScore <= HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE) {
                    this.homeAPAddPeriodCount++;
                }
                handleSwitchScore();
                if (this.isPoorLinkReported) {
                    this.goodPeriodCounter = 0;
                    return;
                }
                return;
            }
            networkGoodDetect(HuaweiWifiWatchdogStateMachine.this.tcpRxPkts, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt, false);
            checkRssiTHBackoff(HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.tcpRetransRate, HuaweiWifiWatchdogStateMachine.this.tcpTxPkts, HuaweiWifiWatchdogStateMachine.this.tcpRetransPkts, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt);
            computeQosLevel(HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mTotPkt, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.isWlanLossRateBad);
            tryBackOffScore(HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt);
        }

        private void handleSwitchScore() {
            int currentWiFiLevel;
            float f = this.mSwitchScore;
            if (f > HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE || this.homeAPAddPeriodCount > 1) {
                if (HuaweiWifiWatchdogStateMachine.this.isRssiBad && this.isLastRssiBad) {
                    HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 0;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    currentWiFiLevel = huaweiWifiWatchdogStateMachine.wpPoorLinkLevelCalcByRssi(huaweiWifiWatchdogStateMachine.lossRate, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt);
                } else {
                    HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 1;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    currentWiFiLevel = huaweiWifiWatchdogStateMachine2.wpPoorLinkLevelCalcByTcp(huaweiWifiWatchdogStateMachine2.lossRate, HuaweiWifiWatchdogStateMachine.this.tcpRetransRate, HuaweiWifiWatchdogStateMachine.this.mProcessedTcpResult.mAvgRtt);
                }
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine3.mWifiPoorRssi = huaweiWifiWatchdogStateMachine3.mCurrRssi;
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine4 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine4.mGoodDetectBaseRssi = huaweiWifiWatchdogStateMachine4.mCurrRssi;
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine5 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine5.logI("mWifiPoorRssi = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi + ", WifiPoorReason = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason);
                chrStatistics();
                HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, currentWiFiLevel);
                this.isPoorLinkReported = true;
                tryReportHomeApChr();
                this.mPktChkBadCnt = 0;
                this.mSwitchScore = 0.0f;
                this.isMaybePoorSend = false;
                return;
            }
            if (!this.isMaybePoorSend && ((f > HuaweiWifiWatchdogStateMachine.MAY_BE_POOR_TH || HuaweiWifiWatchdogStateMachine.this.isPoorLinkForLowRssi) && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.MAY_BE_POOR_RSSI_TH)) {
                HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, -2);
                this.isMaybePoorSend = true;
            }
            if (this.mPktChkBadCnt == 2 && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario != 0) {
                HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.increaseHighDataRateStopROC();
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_BSSID_CHANGE /* 135175 */:
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.transitionTo(huaweiWifiWatchdogStateMachine.mWifiProLinkMonitoringState);
                    return true;
                case HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH /* 135179 */:
                    handleRssiFetched(msg);
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_NOT_DATA_LINK /* 135199 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProLinkMonitoringState receive START_VERIFY_WITH_NOT_DATA_LINK cmd, start transition.");
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine2.transitionTo(huaweiWifiWatchdogStateMachine2.mWifiProVerifyingLinkState);
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_DATA_LINK /* 135200 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProLinkMonitoringState receive start data link monitor event, ignore.");
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK /* 135201 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProLinkMonitoringState error receive STOP_VERIFY_WITH_NOT_DATA_LINK ignore.");
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_DATA_LINK /* 135202 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProLinkMonitoringState receive STOP_VERIFY_WITH_DATA_LINK, stop now.");
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine3.transitionTo(huaweiWifiWatchdogStateMachine3.mWifiProStopVerifyState);
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_HIGH_NET_SPEED_DETECT_MSG /* 136170 */:
                    handleNetSpeedDetect(msg);
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT /* 136171 */:
                    if (HuaweiWifiWatchdogStateMachine.this.mRssiTHTimeoutToken != msg.arg1) {
                        return true;
                    }
                    rssiTHValidTimeout();
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_RSSI_CHANGE /* 136172 */:
                    handleRssiChanged(msg);
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY /* 136270 */:
                    handleHistoryQuality();
                    return true;
                case 151573:
                    handleSsidFetchSuccessed(msg);
                    return true;
                case 151574:
                    HuaweiWifiWatchdogStateMachine.this.logI("RSSI_FETCH_FAILED");
                    return true;
                default:
                    return false;
            }
        }

        private void handleNetSpeedDetect(Message msg) {
            if (HuaweiWifiWatchdogStateMachine.this.mHighSpeedToken != msg.arg1) {
                HuaweiWifiWatchdogStateMachine.this.logI(" have new high speed msg.");
            } else {
                updatePoorNetRssiTH(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
            }
        }

        private void handleHistoryQuality() {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine.this.logI("EVENT_STORE_HISTORY_QUALITY triggered!");
                if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null) {
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.storeHistoryQuality();
                }
                HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY, 1800000);
            }
        }

        private void handleNoTxPeriodInLinkMonitor() {
            String dnsFailCountStr = SystemProperties.get("hw.wifipro.dns_fail_count", "0");
            if (dnsFailCountStr != null) {
                int tmpDnsFailedCnt = 0;
                try {
                    tmpDnsFailedCnt = Integer.parseInt(dnsFailCountStr);
                } catch (NumberFormatException e) {
                    HuaweiWifiWatchdogStateMachine.this.logE("no any pkt, detectNetworkAvailable  parseInt err!");
                }
                if (tmpDnsFailedCnt > 0) {
                    if (HuaweiWifiWatchdogStateMachine.this.mLastDnsFailCount > 0 && tmpDnsFailedCnt - HuaweiWifiWatchdogStateMachine.this.mLastDnsFailCount >= 2) {
                        HuaweiWifiWatchdogStateMachine.this.sendResultMsgToQM(-103);
                        HuaweiWifiWatchdogStateMachine.this.logI("txbad/txgood is 0, detectNetwork dns failed!");
                    }
                    HuaweiWifiWatchdogStateMachine.this.mLastDnsFailCount = tmpDnsFailedCnt;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void adjustTargetPoorRssi(int badRssi) {
        int oldRssi = this.mCurrentBssid.mPoorLinkTargetRssi;
        this.mCurrentBssid.mPoorLinkTargetRssi = (oldRssi + badRssi) / 2;
        logI("adjustTargetPoorRssi current mrssi=" + badRssi + ", ajust poor target rssi from:" + oldRssi + " to " + this.mCurrentBssid.mPoorLinkTargetRssi);
    }

    /* access modifiers changed from: package-private */
    public class WifiProStopVerifyState extends State {
        WifiProStopVerifyState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod = false;
            HuaweiWifiWatchdogStateMachine.this.resetHighDataFlow();
            HuaweiWifiWatchdogStateMachine.this.logI(" WifiProStopVerifyState enter.");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_NOT_DATA_LINK /* 135199 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProStopVerifyState receive START_VERIFY_WITH_NOT_DATA_LINK cmd, start transition.");
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.transitionTo(huaweiWifiWatchdogStateMachine.mWifiProVerifyingLinkState);
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_DATA_LINK /* 135200 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProStopVerifyState receive START_VERIFY_WITH_DATA_LINK cmd, start transition.");
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine2.transitionTo(huaweiWifiWatchdogStateMachine2.mWifiProLinkMonitoringState);
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK /* 135201 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProStopVerifyState rcv STOP_VERIFY_WITH_NOT_DATA_LINK ignore.");
                    return true;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_DATA_LINK /* 135202 */:
                    HuaweiWifiWatchdogStateMachine.this.logI(" WifiProStopVerifyState rcv STOP_VERIFY_WITH_DATA_LINK ignore");
                    return true;
                default:
                    return false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int wpPoorLinkLevelCalcByRssi(double lossRate2, int mrssi, int poor_link_rssi, int rtt) {
        if (((double) mrssi) <= ((double) poor_link_rssi) - BAD_RSSI_LEVEL_0_NOT_AVAILABLE_SUB_VAL || lossRate2 >= 0.4d || rtt >= 8000) {
            return 0;
        }
        if (((double) mrssi) <= ((double) poor_link_rssi) - BAD_RSSI_LEVEL_1_VERY_POOR_SUB_VAL || lossRate2 >= 0.30000000000000004d || rtt >= 5000) {
            return 1;
        }
        return 2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int wpPoorLinkLevelCalcByTcp(double lossRate2, double tcpRetxRate, int rtt) {
        int currentCnePoorLinkLevel;
        if (tcpRetxRate >= PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE || lossRate2 >= 0.4d || rtt >= 8000) {
            currentCnePoorLinkLevel = 0;
        } else if (tcpRetxRate >= PKT_CHK_RETXRATE_LEVEL_1_VERY_POOR || lossRate2 >= 0.30000000000000004d || rtt >= 5000) {
            currentCnePoorLinkLevel = 1;
        } else {
            currentCnePoorLinkLevel = 2;
        }
        logI("poorLinkLevelCalcByTcp, lossRate= " + formatFloatToStr(lossRate2) + ", tcpRetxRate=" + formatFloatToStr(tcpRetxRate) + ". get PoorLinkLevel=" + currentCnePoorLinkLevel);
        return currentCnePoorLinkLevel;
    }

    /* access modifiers changed from: private */
    public class TcpChkResult {
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
        public boolean mValueIsSet = false;

        private void paramInit() {
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

        TcpChkResult() {
            paramInit();
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
            paramInit();
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
                HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel = this.mTcpQuality;
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("TCP result ##### Quality=" + HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel);
            }
            this.mUpdateTime = SystemClock.elapsedRealtime();
            this.mValueIsSet = true;
            return true;
        }
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
                return this.mNewestTcpChkResult;
            }
            logI("getTcpResult tcp result value not valid.");
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private TcpChkResult getQueryTcpResult() {
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

    /* access modifiers changed from: package-private */
    public class ProcessedTcpResult {
        public int mAvgRtt = 0;
        public int mInadequateAvgRtt;
        public int mTotPkt = 0;

        ProcessedTcpResult() {
            HuaweiWifiWatchdogStateMachine.this.mIsNotStatic = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getCurrTcpRtt(ProcessedTcpResult tcpResult) {
        long nowtime = SystemClock.elapsedRealtime();
        TcpChkResult tmpTcr = getTcpResult();
        if (tmpTcr != null) {
            long dtime = (nowtime - tmpTcr.mUpdateTime) + ((long) (tmpTcr.mTcpRttWhen * 1000));
            if (dtime < LINK_SAMPLING_INTERVAL_MS && tmpTcr.mTcpRttPkts > 2) {
                tcpResult.mAvgRtt = tmpTcr.mTcpRtt;
                tcpResult.mTotPkt = tmpTcr.mTcpRttPkts;
            } else if (dtime < LINK_SAMPLING_INTERVAL_MS) {
                tcpResult.mInadequateAvgRtt = tmpTcr.mTcpRtt;
            }
        }
    }

    public void tcpChkResultUpdate(int[] resultArray, int len, int resultType) {
        saveNewTcpResult(resultArray, len, resultType);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCurrentBssid(String bssid, String ssid, int networkID) {
        if (!handleBssidNull(bssid)) {
            logI("mCurrentBssid  current bssid is:" + WifiProCommonUtils.safeDisplayBssid(bssid));
            BssidStatistics bssidStatistics = this.mCurrentBssid;
            if (bssidStatistics == null || !bssid.equals(bssidStatistics.mBssid)) {
                BssidStatistics bssidStatistics2 = this.mCurrentBssid;
                if (bssidStatistics2 != null) {
                    bssidStatistics2.afterDisconnectProcess();
                }
                this.mCurrentBssid = this.mBssidCache.get(bssid);
                if (this.mCurrentBssid == null) {
                    this.mCurrentBssid = new BssidStatistics(bssid);
                    this.mBssidCache.put(bssid, this.mCurrentBssid);
                } else {
                    logI(" get mCurrentBssid inCache for new bssid.");
                }
                WifiProHistoryRecordManager wifiProHistoryRecordManager = this.mWifiProHistoryRecordManager;
                if (wifiProHistoryRecordManager != null) {
                    wifiProHistoryRecordManager.updateCurrConntAp(bssid, ssid, networkID);
                } else {
                    logE("updateCurrentBssid APInfoProcess null error.");
                }
                this.mCurrentBssid.afterConnectProcess();
                logI(" send BSSID changed event.");
                sendMessage(EVENT_BSSID_CHANGE);
                this.mWifiProStatisticsManager.updateWifiConnectState(1);
            }
        }
    }

    private boolean handleBssidNull(String bssid) {
        if (bssid != null) {
            return false;
        }
        if (this.mCurrentBssid == null) {
            return true;
        }
        logI(" BSSID changed, bssid:" + WifiProCommonUtils.safeDisplayBssid(this.mCurrentBssid.mBssid) + " was disconnected. set mCurrentBssid=null");
        WifiProHistoryRecordManager wifiProHistoryRecordManager = this.mWifiProHistoryRecordManager;
        if (wifiProHistoryRecordManager != null) {
            wifiProHistoryRecordManager.updateCurrConntAp(null, null, 0);
        } else {
            logE("updateCurrentBssid APInfoProcess null error.");
        }
        this.mCurrentBssid.afterDisconnectProcess();
        this.mCurrentBssid = null;
        sendMessage(EVENT_BSSID_CHANGE);
        this.mWifiProStatisticsManager.updateWifiConnectState(2);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String formatFloatToStr(double data) {
        DecimalFormat decimalFormat = this.mFormatData;
        if (decimalFormat != null) {
            return decimalFormat.format(data);
        }
        return DEFAULT_DISPLAY_DATA_STR;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendLinkStatusNotification(boolean isGood, int linkLevel) {
        if (isGood) {
            logI("judge good link######, goodLinkLevel=" + linkLevel);
            sendResultMsgToQM(linkLevel);
            return;
        }
        logI("judge poor link######, poorLinkLevel=" + linkLevel);
        sendResultMsgToQM(linkLevel);
    }

    /* access modifiers changed from: private */
    public static class GoodLinkTarget {
        public final int reduceTimeMs;
        public final int rssiAdjDbm;
        public final int sampleCount;

        public GoodLinkTarget(int adj, int count, int time) {
            this.rssiAdjDbm = adj;
            this.sampleCount = count;
            this.reduceTimeMs = time;
        }
    }

    /* access modifiers changed from: private */
    public static class RestoreWifiTime {
        public final int LEVEL_SEL_TIME;
        public final int RESTORE_TIME;

        public RestoreWifiTime(int restoreTime, int selTime) {
            this.RESTORE_TIME = restoreTime;
            this.LEVEL_SEL_TIME = selTime;
        }
    }

    public void setSampleRtt(int[] ipqos, int len) {
        BssidStatistics bssidStatistics;
        if (this.mIsDualbandEnable) {
            if (ipqos == null || (bssidStatistics = this.mCurrentBssid) == null || bssidStatistics.mBssid == null) {
                Log.e(TAG, "setSampleRtt: null pointer exception, return");
            } else if (!this.mCurrentBssid.mBssid.equals(this.mLastSampleBssid)) {
                this.mLastSampleBssid = this.mCurrentBssid.mBssid;
                Log.i(TAG, "setSampleRtt: Bssid changed, return");
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
                HwDualBandQualityEngine hwDualBandQualityEngine = this.mHwDualBandQualityEngine;
                if (hwDualBandQualityEngine != null) {
                    hwDualBandQualityEngine.resetSampleRtt();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class VolumeWeightedEMA {
        public long mAvgRtt = 0;
        public double mOtaLossRate = 0.0d;
        public double mOtaProduct = 0.0d;
        public double mOtaVolume = 0.0d;
        public long mRttProduct = 0;
        public long mRttVolume = 0;

        public VolumeWeightedEMA(double coefficient) {
            HuaweiWifiWatchdogStateMachine.this.mIsNotStatic = true;
        }

        public void updateLossRate(double newValue, int newVolume) {
            if (newVolume > 0) {
                this.mOtaProduct += ((double) newVolume) * newValue;
                this.mOtaVolume = ((double) newVolume) + this.mOtaVolume;
                double d = this.mOtaVolume;
                if (d != 0.0d) {
                    this.mOtaLossRate = this.mOtaProduct / d;
                }
            }
        }

        public void updateAvgRtt(int newValue, int newVolume) {
            if (newVolume > 0) {
                this.mRttProduct = ((long) (newValue * newVolume)) + this.mRttProduct;
                this.mRttVolume = ((long) newVolume) + this.mRttVolume;
                long j = this.mRttVolume;
                if (j != 0) {
                    this.mAvgRtt = this.mRttProduct / j;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class BssidStatistics {
        private static final int MAX_NEAR_RSSI_COUNT = 5;
        private static final int MIN_NEAR_RSSI_COUNT = 2;
        private final String mBssid;
        private VolumeWeightedEMA[] mEntries;
        private int mEntriesSize;
        public int mGoodLinkTargetCount;
        private int mGoodLinkTargetIndex;
        public int mGoodLinkTargetRssi;
        private boolean mIsHistoryLoaded = false;
        public boolean mIsNotFirstChk = false;
        private long mLastTimeRssiPoor = 0;
        private long mLastTimeSample;
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

        public int checkRssi(int rssi, int volume) {
            int tempRssi = rssi;
            if (volume <= 0) {
                return -1;
            }
            if (tempRssi >= -45) {
                tempRssi = -45;
            } else if (tempRssi < -90) {
                tempRssi = -90;
            } else {
                HuaweiWifiWatchdogStateMachine.this.logD("nothing to do");
            }
            int index = tempRssi - this.mRssiBase;
            if (index < 0 || index >= this.mEntriesSize) {
                return -1;
            }
            return index;
        }

        public void updateLoss(int rssi, double value, int volume) {
            int index = checkRssi(rssi, volume);
            if (index != -1) {
                this.mEntries[index].updateLossRate(value, volume);
            }
        }

        public void updateRtt(int rssi, int value, int volume) {
            int index;
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && (index = checkRssi(rssi, volume)) != -1) {
                this.mEntries[index].updateAvgRtt(value, volume);
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
            huaweiWifiWatchdogStateMachine2.logI(" getRssiLoseRate: loss[" + rssi + "]=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[index].mOtaLossRate * 100.0d) + "%, volume=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[index].mOtaVolume) + ", index =" + index);
            return this.mEntries[index];
        }

        public VolumeWeightedEMA getEMAInfo(int rssi) {
            int tempRssi = rssi;
            if (tempRssi > -45) {
                tempRssi = -45;
            } else if (tempRssi < -90) {
                tempRssi = -90;
            } else {
                HuaweiWifiWatchdogStateMachine.this.logD("nothing to do");
            }
            return this.mEntries[tempRssi - this.mRssiBase];
        }

        public int getHistoryNearRtt(int baseRssi) {
            int tempRssi = baseRssi;
            if (tempRssi > -45) {
                tempRssi = -45;
            } else if (tempRssi < -90) {
                tempRssi = -90;
            } else {
                HuaweiWifiWatchdogStateMachine.this.logD("nothing to do.");
            }
            int baseIndex = tempRssi - this.mRssiBase;
            int nearRssicount = 0;
            long avgRtt = this.mEntries[baseIndex].mAvgRtt;
            long totalRttVolume = this.mEntries[baseIndex].mRttVolume;
            long totalRttProduct = this.mEntries[baseIndex].mRttProduct;
            while (nearRssicount <= 5) {
                nearRssicount++;
                int index = baseIndex + nearRssicount;
                if (index >= 0 && index < this.mEntriesSize) {
                    totalRttProduct += this.mEntries[index].mRttProduct;
                    totalRttVolume += this.mEntries[index].mRttVolume;
                }
                int index2 = baseIndex - nearRssicount;
                if (index2 >= 0 && index2 < this.mEntriesSize) {
                    totalRttProduct += this.mEntries[index2].mRttProduct;
                    totalRttVolume += this.mEntries[index2].mRttVolume;
                }
                if (nearRssicount >= 2 && totalRttVolume >= 10) {
                    break;
                }
            }
            if (totalRttVolume > 0) {
                avgRtt = totalRttProduct / totalRttVolume;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logI("from rssi " + (tempRssi - nearRssicount) + " to " + (tempRssi + nearRssicount) + " avgRtt=" + avgRtt);
            return (int) avgRtt;
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0096: APUT  
          (r9v0 'lossRateInfo' double[] A[D('lossRateInfo' double[])])
          (0 ??[int, short, byte, char])
          (r2v3 'avgLossRate' double A[D('avgLossRate' double)])
         */
        public double[] getHistoryNearLossRate(int baseRssi) {
            int baseIndex = baseRssi - this.mRssiBase;
            int nearRssiCount = 0;
            double avgLossRate = this.mEntries[baseIndex].mOtaLossRate;
            double totalOtaVolume = this.mEntries[baseIndex].mOtaVolume;
            double totalOtaProduct = this.mEntries[baseIndex].mOtaProduct;
            double[] lossRateInfo = new double[2];
            while (nearRssiCount <= 5) {
                nearRssiCount++;
                int index = baseIndex + nearRssiCount;
                if (index >= 0 && index < this.mEntriesSize) {
                    totalOtaProduct += this.mEntries[index].mOtaProduct;
                    totalOtaVolume += this.mEntries[index].mOtaVolume;
                }
                int index2 = baseIndex - nearRssiCount;
                if (index2 >= 0 && index2 < this.mEntriesSize) {
                    totalOtaProduct += this.mEntries[index2].mOtaProduct;
                    totalOtaVolume += this.mEntries[index2].mOtaVolume;
                }
                if (nearRssiCount >= 2 && totalOtaVolume >= 10.0d) {
                    break;
                }
            }
            if (totalOtaVolume > 0.0d) {
                avgLossRate = totalOtaProduct / totalOtaVolume;
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logI("from rssi " + (baseRssi - nearRssiCount) + " to " + (baseRssi + nearRssiCount) + " avgLossRate=" + String.valueOf(avgLossRate) + "voludme=" + String.valueOf(totalOtaVolume));
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
            if (HuaweiWifiWatchdogStateMachine.sPresetLoss == null) {
                double[] unused = HuaweiWifiWatchdogStateMachine.sPresetLoss = new double[HuaweiWifiWatchdogStateMachine.PRESET_LOSSESS_SIZE];
                for (int loop = 0; loop < HuaweiWifiWatchdogStateMachine.PRESET_LOSSESS_SIZE; loop++) {
                    HuaweiWifiWatchdogStateMachine.sPresetLoss[loop] = HuaweiWifiWatchdogStateMachine.PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE / Math.pow((double) (90 - loop), HuaweiWifiWatchdogStateMachine.PRESET_LOSSESS_POWER);
                }
            }
            return HuaweiWifiWatchdogStateMachine.sPresetLoss[-rssi];
        }

        public boolean poorLinkDetected(int rssi) {
            HuaweiWifiWatchdogStateMachine.this.logI(" Poor link detected, base rssi=" + rssi + ", GoodLinkTargetIndex=" + this.mGoodLinkTargetIndex);
            int tempRssi = rssi;
            long now = SystemClock.elapsedRealtime();
            long lastPoor = now - this.mLastTimeRssiPoor;
            this.mLastTimeRssiPoor = now;
            if (tempRssi < this.mPoorLinkTargetRssi) {
                tempRssi = this.mPoorLinkTargetRssi;
                HuaweiWifiWatchdogStateMachine.this.logI(" Poor link detected,GoodLinkTargetRssi should not < " + this.mPoorLinkTargetRssi + " dB, set to: " + tempRssi + " dB.");
            }
            while (this.mGoodLinkTargetIndex > 0 && lastPoor >= ((long) HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET[this.mGoodLinkTargetIndex - 1].reduceTimeMs)) {
                this.mGoodLinkTargetIndex--;
            }
            this.mGoodLinkTargetCount = HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET[this.mGoodLinkTargetIndex].sampleCount;
            this.mTimeElapGoodLinkTargetRssi = findRssiTarget(tempRssi, tempRssi + 17, 0.2d);
            this.mTimeElapBaseTime = now;
            int i = this.mTimeElapGoodLinkTargetRssi;
            if (i < tempRssi + 3) {
                this.mGoodLinkTargetRssi = tempRssi + 3;
            } else {
                this.mGoodLinkTargetRssi = i;
            }
            this.mGoodLinkTargetRssi += HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET[this.mGoodLinkTargetIndex].rssiAdjDbm;
            if (this.mGoodLinkTargetIndex < HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET.length - 1) {
                this.mGoodLinkTargetIndex++;
            }
            HuaweiWifiWatchdogStateMachine.this.logI("goodRssi=" + this.mGoodLinkTargetRssi + " TimeElapGoodRssiTarget=" + this.mTimeElapGoodLinkTargetRssi + " goodSampleCount=" + this.mGoodLinkTargetCount + " lastPoor=" + lastPoor + ",new GoodLinkTargetIndex=" + this.mGoodLinkTargetIndex);
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void otaTcpPoorLinkDetected() {
            HuaweiWifiWatchdogStateMachine.this.logI("otaTcpPoor enter.");
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
            HuaweiWifiWatchdogStateMachine.this.logI(" newLinkDetected enter");
            this.mGoodLinkTargetRssi = findRssiTarget(-90, -45, 0.2d);
            this.mPoorLinkTargetRssi = -75;
            if (this.mGoodLinkTargetRssi < this.mPoorLinkTargetRssi) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI(" Poor link detected, select " + this.mGoodLinkTargetRssi + " < " + this.mPoorLinkTargetRssi + "dB, set GoodLink base rssi=" + this.mPoorLinkTargetRssi + "dB.");
                this.mGoodLinkTargetRssi = this.mPoorLinkTargetRssi;
            }
            this.mTimeElapGoodLinkTargetRssi = this.mGoodLinkTargetRssi;
            this.mGoodLinkTargetCount = 3;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine2.logI("New link verifying target set, rssi=" + this.mGoodLinkTargetRssi + " count=" + this.mGoodLinkTargetCount);
        }

        public int findRssiTarget(int from, int to, double threshold) {
            int i = this.mRssiBase;
            int begin = from - i;
            int end = to - i;
            int emptyCount = 0;
            int step = begin < end ? 1 : -1;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logI(" findRssiTarget need rssi with lose rate < " + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(threshold * 100.0d) + "%, idx begin:" + begin + "~ end:" + end);
            for (int loop = begin; loop != end; loop += step) {
                if (loop < 0 || loop >= this.mEntriesSize || this.mEntries[loop].mOtaVolume <= HuaweiWifiWatchdogStateMachine.PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE) {
                    emptyCount++;
                    if (emptyCount >= 3) {
                        int rssi = this.mRssiBase + loop;
                        double lossPreset = presetLoss(rssi);
                        if (lossPreset < threshold) {
                            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                            huaweiWifiWatchdogStateMachine2.logI(" get good link rssi target in default table.target rssi=" + rssi + "dB, lose rate=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(lossPreset * 100.0d) + "% < threshold:" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(100.0d * threshold) + "%,i=" + loop);
                            return rssi;
                        }
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.logD("nothing to do.");
                    }
                } else {
                    emptyCount = 0;
                    if (this.mEntries[loop].mOtaLossRate < threshold) {
                        int rssi2 = this.mRssiBase + loop;
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine3.logI(" Meet ok target in rssi=" + rssi2 + "dB, lose rate value=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[loop].mOtaLossRate * 100.0d) + "%, stot pkt =" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[loop].mOtaVolume) + ",i=" + loop);
                        return rssi2;
                    }
                    HuaweiWifiWatchdogStateMachine.this.logD("nothing to do");
                }
            }
            return this.mRssiBase + to;
        }

        public int findRssiTargetByRtt(int from, int to, long rttThreshold, int defaultTargetRssi) {
            int i = this.mRssiBase;
            int begin = from - i;
            int end = to - i;
            int step = begin < end ? 1 : -1;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logI("findRssiTargetByRtt: rtt < " + rttThreshold + ", idx begin " + begin + "~ " + end);
            for (int loop = begin; loop != end; loop += step) {
                if (loop >= 0 && loop < this.mEntriesSize && this.mEntries[loop].mRttVolume > 5 && this.mEntries[loop].mAvgRtt != 0) {
                    if (this.mEntries[loop].mAvgRtt < rttThreshold) {
                        int rssi = this.mRssiBase + loop;
                        if (((long) getHistoryNearRtt(rssi)) < rttThreshold) {
                            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                            huaweiWifiWatchdogStateMachine2.logI("findRssiTargetByRtt:Meet ok target in rssi=" + rssi + "dB, rtt value=" + this.mEntries[loop].mAvgRtt + ", packets =" + this.mEntries[loop].mRttVolume);
                            return rssi;
                        }
                    } else {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine3.logI("findRssiTargetByRtt: rssi=" + (this.mRssiBase + loop) + "dB, rtt value=" + this.mEntries[loop].mAvgRtt + ", packets =" + this.mEntries[loop].mRttVolume);
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
            huaweiWifiWatchdogStateMachine.logI("save bssid:" + WifiProCommonUtils.safeDisplayBssid(this.mBssid) + " history record, mEntriesSize = " + this.mEntriesSize);
            for (int loop = 0; loop < this.mEntriesSize; loop++) {
                apQualityRcd.putAvgRttToRecord(this.mEntries[loop].mAvgRtt, loop);
                apQualityRcd.putRttProductToRecord(this.mEntries[loop].mRttProduct, loop);
                apQualityRcd.putRttVolumeToRecord(this.mEntries[loop].mRttVolume, loop);
                apQualityRcd.putLostRateToRecord(this.mEntries[loop].mOtaLossRate, loop);
                apQualityRcd.putLostVolumeToRecord(this.mEntries[loop].mOtaVolume, loop);
                apQualityRcd.putLostProductToRecord(this.mEntries[loop].mOtaProduct, loop);
            }
            apQualityRcd.setApBssid(this.mBssid);
            HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.saveApQualityRecord(apQualityRcd);
        }

        private boolean loadApQualityRecord(WifiProApQualityRcd apQualityRcd) {
            HuaweiWifiWatchdogStateMachine.this.logI("loadApQualityRecord enter.");
            if (apQualityRcd == null) {
                HuaweiWifiWatchdogStateMachine.this.logE("apQualityRcd is null");
                return false;
            } else if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.loadApQualityRecord(apQualityRcd.getApBssid(), apQualityRcd)) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("restoreBssid:" + WifiProCommonUtils.safeDisplayBssid(this.mBssid) + " HistoreyRecord restore record, mEntriesSize = " + this.mEntriesSize);
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
                huaweiWifiWatchdogStateMachine2.logE("loadApQualityRecord read DB failed for bssid:" + WifiProCommonUtils.safeDisplayBssid(this.mBssid));
                return false;
            }
        }

        private void initBQEHistoryRecord(WifiProApQualityRcd apQualityRcd) {
            if (!loadApQualityRecord(apQualityRcd)) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("not ApQualityRcd for bssid:" + WifiProCommonUtils.safeDisplayBssid(this.mBssid) + ".");
            } else {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.logI("succ load ApQualityRcd for bssid:" + WifiProCommonUtils.safeDisplayBssid(this.mBssid) + ".");
                HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY, 1800000);
            }
            this.mIsHistoryLoaded = true;
        }

        public void afterConnectProcess() {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("afterConnectProcess enter for bssid:" + WifiProCommonUtils.safeDisplayBssid(this.mBssid));
                initBQEHistoryRecord(this.mWifiProApQualityRcd);
            }
        }

        public void afterDisconnectProcess() {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.logI("afterDisconnectProcess enter for bssid:" + WifiProCommonUtils.safeDisplayBssid(this.mBssid));
                if (this.mIsHistoryLoaded) {
                    saveApQualityRecord(this.mWifiProApQualityRcd);
                    HuaweiWifiWatchdogStateMachine.this.removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY);
                    return;
                }
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine2.logE("afterDisconnectProcess not read bssid:" + WifiProCommonUtils.safeDisplayBssid(this.mBssid) + " record, can not save.");
            }
        }

        public void initQualityRecordFromDatabase() {
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.logI("initQualityRecordFromDatabase enter for bssid:" + StringUtilEx.safeDisplayBssid(this.mBssid));
            initBQEHistoryRecord(this.mWifiProApQualityRcd);
        }

        public VolumeWeightedEMA getHistoryEntryByRssi(int rssi) {
            VolumeWeightedEMA[] volumeWeightedEMAArr = this.mEntries;
            if (volumeWeightedEMAArr == null) {
                return null;
            }
            int arrayIdx = rssi - this.mRssiBase;
            if (arrayIdx >= 0 && arrayIdx < this.mEntriesSize) {
                return volumeWeightedEMAArr[arrayIdx];
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

    public int updateQosLevelByHistory() {
        WifiInfo wifiInfo;
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null) {
            return 0;
        }
        int rssi = wifiInfo.getRssi();
        if (this.mCurrentBssid == null) {
            return 0;
        }
        if (rssi >= -65) {
            return 3;
        }
        if (rssi >= QOS_NOT_GOOD_LEVEL_OF_RSSI) {
            return 2;
        }
        return 1;
    }

    public boolean getHandover5GApRssiThreshold(WifiProEstimateApInfo estimateApInfo) {
        logI("start getHandover5GApRssiThreshold");
        sendMessage(EVENT_GET_5G_AP_RSSI_THRESHOLD, estimateApInfo);
        return true;
    }

    public boolean getApHistoryQualityScore(WifiProEstimateApInfo estimateApInfo) {
        logI("start getApHistoryQualityScore");
        sendMessage(EVENT_GET_AP_HISTORY_QUALITY_SCORE, estimateApInfo);
        return true;
    }

    public void getApHistoryQualityScoreForWifi2Wifi(WifiProEstimateApInfo estimateApInfo) {
        logI("getApHistoryQualityScoreForWifi2Wifi");
        targetApHistoryQualityScoreProcess(estimateApInfo);
    }

    private void sendDBQoeResult(int eventId, WifiProEstimateApInfo estimateApInfo) {
        logI("sendDBQoeResult enter.");
        Handler handler = this.mQMHandler;
        if (handler != null) {
            this.mQMHandler.sendMessage(handler.obtainMessage(eventId, estimateApInfo));
            return;
        }
        logE("sendDBQoeResult null error.");
    }

    private BssidStatistics getApQualityRecord(String apBssid) {
        if (apBssid == null) {
            return null;
        }
        logI("getApQualityRecord enter, for bssid:" + WifiProCommonUtils.safeDisplayBssid(apBssid));
        BssidStatistics currentBssidStat = this.mBssidCache.get(apBssid);
        if (currentBssidStat == null) {
            BssidStatistics currentBssidStat2 = new BssidStatistics(apBssid);
            currentBssidStat2.initQualityRecordFromDatabase();
            this.mBssidCache.put(apBssid, currentBssidStat2);
            return currentBssidStat2;
        }
        logI(" get mCurrentBssid inCache for new bssid.");
        return currentBssidStat;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void target5GApRssiTHProcess(WifiProEstimateApInfo estimateApInfo) {
        logI("target5GApRssiTHProcess enter: ssid=" + estimateApInfo.getApSsid() + " bssid=" + WifiProCommonUtils.safeDisplayBssid(estimateApInfo.getApBssid()));
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
            logI("target5GApRssiTHProcess test last connected time:" + apInfoRecord.lastConnectTime + ", total connect time:" + apInfoRecord.totalUseTime + ", night connect time:" + apInfoRecord.totalUseTimeAtNight + ", weekend connect time:" + apInfoRecord.totalUseTimeAtWeekend);
        }
        int rssiTH = apQualityInfo.findRssiTargetByRtt(-80, -45, 1500, -65);
        if (rssiTH < DEFAULT_TARGET_MONITOR_RSSI) {
            rssiTH = DEFAULT_TARGET_MONITOR_RSSI;
        }
        estimateApInfo.setRetRssiTH(rssiTH);
        sendDBQoeResult(12, estimateApInfo);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void targetApHistoryQualityScoreProcess(WifiProEstimateApInfo estimateApInfo) {
        estimateApInfo.setRetHistoryScore(getApScore(estimateApInfo));
        sendDBQoeResult(13, estimateApInfo);
    }

    private int getApScore(WifiProEstimateApInfo estimateApInfo) {
        BssidStatistics bssidStatistics = this.mCurrentBssid;
        if (bssidStatistics == null || bssidStatistics.mBssid == null || this.mHwDualBandQualityEngine == null || estimateApInfo == null) {
            logE("getApHistoryQualityScore null pointer.");
            return -1;
        }
        logI("getApHistoryQualityScore: ssid=" + estimateApInfo.getApSsid() + " bssid=" + WifiProCommonUtils.safeDisplayBssid(estimateApInfo.getApBssid()));
        int retScore = 0 + getApFixedScore(estimateApInfo);
        int variedScore = getApVariedScore(estimateApInfo);
        if (variedScore == -1) {
            return -1;
        }
        int retScore2 = retScore + variedScore;
        logI("ssid:" + StringUtilEx.safeDisplaySsid(estimateApInfo.getApSsid()) + " bssid:" + WifiProCommonUtils.safeDisplayBssid(estimateApInfo.getApBssid()) + " total score is " + retScore2);
        return retScore2;
    }

    private int getApFixedScore(WifiProEstimateApInfo estimateApInfo) {
        int fixedScore = 0;
        String apBssid = estimateApInfo.getApBssid();
        WifiProApInfoRecord apInfoRecord = this.mWifiProHistoryRecordManager.getApInfoRecord(apBssid);
        if (apInfoRecord != null) {
            int score = this.mHwDualBandQualityEngine.getScoreByConnectTime(apInfoRecord.totalUseTime / WifiProHistoryRecordManager.SECOND_OF_ONE_HOUR);
            logI("bssid:" + WifiProCommonUtils.safeDisplayBssid(apBssid) + " ConnectTime score is " + score);
            fixedScore = 0 + score;
        }
        int score2 = this.mHwDualBandQualityEngine.getScoreByRssi(estimateApInfo.getApRssi());
        logI("bssid:" + WifiProCommonUtils.safeDisplayBssid(apBssid) + " rssi score is " + score2);
        int fixedScore2 = fixedScore + score2;
        if (estimateApInfo.is5GAP()) {
            int score3 = this.mHwDualBandQualityEngine.getScoreByBluetoothUsage();
            logI("bssid:" + WifiProCommonUtils.safeDisplayBssid(apBssid) + " BluetoothUsage score is " + score3);
            fixedScore2 += score3;
        }
        BssidStatistics bssidStatistics = this.mCurrentBssid;
        if (bssidStatistics == null || !HwDualBandRelationManager.isDualBandAP(bssidStatistics.mBssid, apBssid) || !estimateApInfo.is5GAP()) {
            return fixedScore2;
        }
        logI("bssid:" + WifiProCommonUtils.safeDisplayBssid(apBssid) + " DUAL_BAND_SINGLE_AP score is 6");
        return fixedScore2 + 6;
    }

    private int getApVariedScore(WifiProEstimateApInfo estimateApInfo) {
        BssidStatistics apQualityInfo;
        int variedScore = 0;
        String apBssid = estimateApInfo.getApBssid();
        BssidStatistics bssidStatistics = this.mCurrentBssid;
        if (bssidStatistics == null || !bssidStatistics.mBssid.equals(apBssid)) {
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
        } else {
            logD("nothing to do.");
        }
        int avgRtt = apQualityInfo.getHistoryNearRtt(queryRssi);
        if (avgRtt != 0) {
            int score = this.mHwDualBandQualityEngine.getScoreByRtt(avgRtt);
            logI("bssid:" + WifiProCommonUtils.safeDisplayBssid(apBssid) + " rtt score is " + score);
            variedScore = 0 + score;
        } else {
            logI("There is no rtt history about rssi " + queryRssi);
        }
        double[] lossRateInfo = apQualityInfo.getHistoryNearLossRate(queryRssi);
        if (lossRateInfo[1] <= 10.0d) {
            return variedScore;
        }
        int score2 = this.mHwDualBandQualityEngine.getScoreByLossRate(lossRateInfo[0]);
        logI("bssid:" + WifiProCommonUtils.safeDisplayBssid(apBssid) + " rtt score is " + score2);
        return variedScore + score2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCurrentAPRssi(int rssi) {
        if (this.mIsDualbandEnable) {
            Handler handler = this.mQMHandler;
            if (handler == null || INVALID_RSSI == rssi) {
                logE("sendCurryAPRssi null error.");
                return;
            }
            this.mQMHandler.sendMessage(handler.obtainMessage(14, Integer.valueOf(rssi)));
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

    public synchronized boolean isHighDataFlowModel() {
        if (this.mHighDataFlowScenario != 0) {
            return true;
        }
        return false;
    }

    public Bundle getChrHandoverNetworkQuality() {
        Bundle chrNetworkQuality = new Bundle();
        ProcessedTcpResult processedTcpResult = this.mProcessedTcpResult;
        if (processedTcpResult != null) {
            chrNetworkQuality.putInt("LATENCY", processedTcpResult.mAvgRtt);
        }
        chrNetworkQuality.putInt("TXPKT", this.tcpTxPkts);
        chrNetworkQuality.putInt("RXPKT", this.tcpRxPkts);
        chrNetworkQuality.putInt("DNSFAIL", this.mLastDnsFailCount);
        chrNetworkQuality.putInt("RTT", (int) (this.tcpRetransRate * 100.0d));
        return chrNetworkQuality;
    }

    public Bundle getApHistoryQuality(WifiProEstimateApInfo estimateApInfo) {
        if (estimateApInfo == null || this.mWifiProHistoryRecordManager == null) {
            logE("getApHistoryQuality estimateApInfo is null.");
            return new Bundle();
        }
        BssidStatistics apQualityInfo = getApQualityRecord(estimateApInfo.getApBssid());
        WifiProApInfoRecord apInfoRecord = this.mWifiProHistoryRecordManager.getApInfoRecord(estimateApInfo.getApBssid());
        if (apQualityInfo == null || apInfoRecord == null) {
            logE("getApQualityRecord apQualityInfo is null.");
            return new Bundle();
        }
        int queryRssi = estimateApInfo.getApRssi();
        if (queryRssi > -45) {
            queryRssi = -45;
        } else if (queryRssi < -90) {
            queryRssi = -90;
        } else {
            logD("nothing to do.");
        }
        int avgRtt = apQualityInfo.getHistoryNearRtt(queryRssi);
        double[] lossRateInfo = apQualityInfo.getHistoryNearLossRate(queryRssi);
        Bundle data = new Bundle();
        data.putInt("RSSI", estimateApInfo.getApRssi());
        data.putInt("LATENCY", avgRtt);
        data.putInt("LOSERATE", (int) (lossRateInfo[0] * 100.0d));
        data.putInt("ISBTON", this.mHwDualBandQualityEngine.isBluetoothConnected() ? 1 : 0);
        data.putInt("ONLINETIME", apInfoRecord.totalUseTime);
        data.putString("SSID", estimateApInfo.getApSsid());
        return data;
    }

    public Bundle getChrInterentDetectAlgorithmDuration() {
        this.mChrDetecdNoInternetCount = 0;
        Bundle result = new Bundle();
        result.putInt("NoInternetDutation", this.mChrDetecdNoInternetCount * 8);
        return result;
    }
}
