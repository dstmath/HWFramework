package com.android.server.wifi.wifipro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
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
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.HwUidTcpMonitor;
import com.android.server.wifi.HwWifiCHRStateManager;
import com.android.server.wifi.HwWifiCHRStateManagerImpl;
import com.android.server.wifi.HwWifiConnectivityMonitor;
import com.android.server.wifi.wifipro.TrafficMonitor.TxRxStat;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
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
    private static final GoodLinkTarget[] GOOD_LINK_TARGET = new GoodLinkTarget[]{new GoodLinkTarget(0, 3, 900000), new GoodLinkTarget(2, 4, 480000), new GoodLinkTarget(4, 4, 240000), new GoodLinkTarget(5, 5, MessageUtil.AUTO_CLOSE_SCAN_TIMER), new GoodLinkTarget(6, 5, 0)};
    private static final int GOOD_LINK_TARGET_MIN_ADD_DB_VAL = 3;
    private static final int GOOD_RTT_TARGET = 1500;
    private static final GoodSpdCountRate[] GOOD_SPD_COUNT_RATE_TBL = new GoodSpdCountRate[]{new GoodSpdCountRate(25.0f, 0.5f), new GoodSpdCountRate(20.0f, 0.6f), new GoodSpdCountRate(15.0f, 0.7f), new GoodSpdCountRate(10.0f, SCORE_BACK_OFF_RATE), new GoodSpdCountRate(5.0f, 0.9f)};
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
    private static final RestoreWifiTime[] RESTORE_WIFI_TIME = new RestoreWifiTime[]{new RestoreWifiTime(MessageUtil.AUTO_CLOSE_SCAN_TIMER, 900000), new RestoreWifiTime(240000, 600000), new RestoreWifiTime(MAX_RESTORE_WIFI_TIME, 0)};
    private static final long ROVE_OUT_LINK_SAMPLING_INTERVAL_MS = 2000;
    private static final RssiGoodSpdTH[] RSSI_GOOD_SPD_TBL = new RssiGoodSpdTH[]{new RssiGoodSpdTH(-75, 81920, WifiproBqeUtils.BQE_NOT_GOOD_SPEED), new RssiGoodSpdTH(QOS_NOT_GOOD_LEVEL_OF_RSSI, SCORE_BACK_OFF_NETGOOD_SPEED_TH, 15360), new RssiGoodSpdTH(INVALID_RSSI, MIN_SPEED_VAL, 10240)};
    private static final RssiRate[] RSSI_RATE_TBL = new RssiRate[]{new RssiRate(-50, 0.6f), new RssiRate(-65, 0.7f), new RssiRate(-75, SCORE_BACK_OFF_RATE), new RssiRate(QOS_NOT_GOOD_LEVEL_OF_RSSI, 0.9f)};
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
    private static final String WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
    private static final int WLAN_INET_RSSI_LEVEL_3_GOOD = 0;
    private static final int WLAN_INET_RSSI_LEVEL_4_BETTER = 5;
    private static final int WLAN_INET_RSSI_LEVEL_5_BEST = 8;
    private static double[] mSPresetLoss;
    private static int printLogLevel = 1;
    private int LOW_RSSI_TH = QOS_NOT_GOOD_LEVEL_OF_RSSI;
    private int QOE_LEVEL_RTT_MIN_PKT = 3;
    private int RSSI_TH_FOR_BAD_QOE_LEVEL = QOS_NOT_GOOD_LEVEL_OF_RSSI;
    private int RSSI_TH_FOR_NOT_BAD_QOE_LEVEL = -75;
    private long mBestSpeedInPeriod = 0;
    private BroadcastReceiver mBroadcastReceiver;
    private LruCache<String, BssidStatistics> mBssidCache = new LruCache(20);
    private Context mContext;
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
    private int mLastDetectLevel = 0;
    private boolean mLastHighDataFlow = false;
    private TcpChkResult mLastPeriodTcpChkResult;
    private String mLastSampleBssid = null;
    private int mLastSamplePkts = 0;
    private int mLastSampleRssi = 0;
    private int mLastSampleRtt = 0;
    private TcpChkResult mNewestTcpChkResult = new TcpChkResult();
    private float mPeriodGoodSpdScore = 0.0f;
    private int mPoorNetRssiRealTH = -75;
    private int mPoorNetRssiTH = -75;
    private int mPoorNetRssiTHNext = -75;
    private Handler mQMHandler;
    private int mRssiChangedToken = 0;
    private int mRssiFetchToken = 0;
    private int mRssiTHTimeoutToken = 0;
    private int mRssiTHValidTime = 180000;
    private float mSpeedGoodCount = 0.0f;
    private int mSpeedNotGoodCount = 0;
    private Runnable mSpeedUpdate = new Runnable() {
        public void run() {
            synchronized (HuaweiWifiWatchdogStateMachine.this.mTraffic) {
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine;
                HuaweiWifiWatchdogStateMachine.this.mTxrxStat = HuaweiWifiWatchdogStateMachine.this.mTraffic.getStatic(2);
                HuaweiWifiWatchdogStateMachine.this.logD("speed: rxpkt:" + HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rxPkts + ", rxSpd:" + HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed + "B/s, txpkt:" + HuaweiWifiWatchdogStateMachine.this.mTxrxStat.txPkts + ", txSpd:" + HuaweiWifiWatchdogStateMachine.this.mTxrxStat.tx_speed);
                if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 24576 || HuaweiWifiWatchdogStateMachine.this.mTxrxStat.tx_speed >= 32768) {
                    HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod = true;
                }
                if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed > HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod) {
                    HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod = HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed;
                }
                if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi != HuaweiWifiWatchdogStateMachine.INVALID_RSSI && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < -75 && HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 65536) {
                    huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.sendMessage(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.EVENT_HIGH_NET_SPEED_DETECT_MSG, huaweiWifiWatchdogStateMachine3.mHighSpeedToken = huaweiWifiWatchdogStateMachine3.mHighSpeedToken + 1, 0));
                }
                huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.mPeriodGoodSpdScore = huaweiWifiWatchdogStateMachine.mPeriodGoodSpdScore + HuaweiWifiWatchdogStateMachine.this.checkGoodSpd(HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed);
                if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 1048576) {
                    huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mHistoryHSCount = huaweiWifiWatchdogStateMachine.mHistoryHSCount + 1;
                }
            }
        }
    };
    private long mSwitchOutTime = 0;
    private volatile int mTcpReportLevel = 0;
    private TrafficMonitor mTraffic = null;
    private volatile TxRxStat mTxrxStat = null;
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
    private boolean wifiOtaChkIsEnable;

    private class BssidStatistics {
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
            this.mRestoreDelayTime = MessageUtil.AUTO_CLOSE_SCAN_TIMER;
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
                    if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                        HuaweiWifiWatchdogStateMachine.this.logD(HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + " current loss[" + rssi + "]=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(value * 100.0d) + "%, volume=" + volume);
                        HuaweiWifiWatchdogStateMachine.this.logD(HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + " history loss[" + rssi + "]=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[index].mOtaLossRate * 100.0d) + "%, volume=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[index].mOtaVolume));
                    }
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
                    HuaweiWifiWatchdogStateMachine.this.logD(HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + " current RTT[" + rssi + "]=" + value + ", volume=" + volume);
                    HuaweiWifiWatchdogStateMachine.this.logD(HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + " history RTT[" + rssi + "]=" + this.mEntries[index].mAvgRtt + ", volume=" + this.mEntries[index].mRttVolume);
                }
            }
        }

        public VolumeWeightedEMA getRssiLoseRate(int rssi) {
            int index = rssi - this.mRssiBase;
            if (index < 0 || index >= this.mEntriesSize) {
                HuaweiWifiWatchdogStateMachine.this.logE(" getRssiLoseRate rssi=" + rssi + ", index=" + index + ", mRssiBase=" + this.mRssiBase);
                return null;
            }
            HuaweiWifiWatchdogStateMachine.this.logD(" getRssiLoseRate: loss[" + rssi + "]=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[index].mOtaLossRate * 100.0d) + "%, volume=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[index].mOtaVolume) + ", index =" + index);
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
                index = baseIndex - near_rssi_count;
                if (index >= 0 && index < this.mEntriesSize) {
                    totalRttProduct += this.mEntries[index].mRttProduct;
                    totalRttVolume += this.mEntries[index].mRttVolume;
                }
                if (near_rssi_count >= 2 && totalRttVolume >= 10) {
                    break;
                }
            }
            if (totalRttVolume > 0) {
                avgRtt = totalRttProduct / totalRttVolume;
            }
            HuaweiWifiWatchdogStateMachine.this.logD("from rssi " + (baseRssi - near_rssi_count) + " to " + (baseRssi + near_rssi_count) + " avgRtt=" + avgRtt);
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
                index = baseIndex - near_rssi_count;
                if (index >= 0 && index < this.mEntriesSize) {
                    totalOtaProduct += this.mEntries[index].mOtaProduct;
                    totalOtaVolume += this.mEntries[index].mOtaVolume;
                }
                if (near_rssi_count >= 2 && totalOtaVolume >= 10.0d) {
                    break;
                }
            }
            if (totalOtaVolume > 0.0d) {
                avgLossRate = totalOtaProduct / totalOtaVolume;
            }
            HuaweiWifiWatchdogStateMachine.this.logD("from rssi " + (baseRssi - near_rssi_count) + " to " + (baseRssi + near_rssi_count) + " avgLossRate=" + String.valueOf(avgLossRate) + "voludme=" + String.valueOf(totalOtaVolume));
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
                HuaweiWifiWatchdogStateMachine.mSPresetLoss = new double[90];
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

        private void otaTcpPoorLinkDetected() {
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
                HuaweiWifiWatchdogStateMachine.this.logD(" Poor link detected, select " + this.mGoodLinkTargetRssi + " < " + this.mPoorLinkTargetRssi + "dB, set GoodLink base rssi=" + this.mPoorLinkTargetRssi + "dB.");
                this.mGoodLinkTargetRssi = this.mPoorLinkTargetRssi;
            }
            this.mTimeElapGoodLinkTargetRssi = this.mGoodLinkTargetRssi;
            this.mGoodLinkTargetCount = 3;
            HuaweiWifiWatchdogStateMachine.this.logI("New link verifying target set, rssi=" + this.mGoodLinkTargetRssi + " count=" + this.mGoodLinkTargetCount);
        }

        public int findRssiTarget(int from, int to, double threshold) {
            from -= this.mRssiBase;
            to -= this.mRssiBase;
            int emptyCount = 0;
            int d = from < to ? 1 : -1;
            HuaweiWifiWatchdogStateMachine.this.logD(" findRssiTarget need rssi with lose rate < " + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(100.0d * threshold) + "%, idx from:" + from + "~ to:" + to);
            int i = from;
            while (i != to) {
                int rssi;
                if (i < 0 || i >= this.mEntriesSize || this.mEntries[i].mOtaVolume <= HuaweiWifiWatchdogStateMachine.PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE) {
                    emptyCount++;
                    if (emptyCount >= 3) {
                        rssi = this.mRssiBase + i;
                        double lossPreset = presetLoss(rssi);
                        if (lossPreset < threshold) {
                            HuaweiWifiWatchdogStateMachine.this.logD(" get good link rssi target in default table.target rssi=" + rssi + "dB, lose rate=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(100.0d * lossPreset) + "% < threshold:" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(100.0d * threshold) + "%,i=" + i);
                            return rssi;
                        }
                    } else {
                        continue;
                    }
                } else {
                    emptyCount = 0;
                    if (this.mEntries[i].mOtaLossRate < threshold) {
                        rssi = this.mRssiBase + i;
                        HuaweiWifiWatchdogStateMachine.this.logD(" Meet ok target in rssi=" + rssi + "dB, lose rate value=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[i].mOtaLossRate * 100.0d) + "%, stot pkt =" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[i].mOtaVolume) + ",i=" + i);
                        return rssi;
                    }
                }
                i += d;
            }
            return this.mRssiBase + to;
        }

        public int findRssiTargetByRtt(int from, int to, long rttThreshold, int defaultTargetRssi) {
            from -= this.mRssiBase;
            to -= this.mRssiBase;
            int d = from < to ? 1 : -1;
            HuaweiWifiWatchdogStateMachine.this.logD("findRssiTargetByRtt: rtt < " + rttThreshold + ", idx from " + from + "~ " + to);
            int i = from;
            while (i != to) {
                if (i >= 0 && i < this.mEntriesSize && this.mEntries[i].mRttVolume > 5 && this.mEntries[i].mAvgRtt != 0) {
                    if (this.mEntries[i].mAvgRtt < rttThreshold) {
                        int rssi = this.mRssiBase + i;
                        if (((long) getHistoryNearRtt(rssi)) < rttThreshold) {
                            HuaweiWifiWatchdogStateMachine.this.logD("findRssiTargetByRtt:Meet ok target in rssi=" + rssi + "dB, rtt value=" + this.mEntries[i].mAvgRtt + ", packets =" + this.mEntries[i].mRttVolume);
                            return rssi;
                        }
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.logD("findRssiTargetByRtt: rssi=" + (this.mRssiBase + i) + "dB, rtt value=" + this.mEntries[i].mAvgRtt + ", packets =" + this.mEntries[i].mRttVolume);
                    }
                }
                i += d;
            }
            return defaultTargetRssi;
        }

        private void saveApQualityRecord(WifiProApQualityRcd apQualityRcd) {
            if (apQualityRcd == null) {
                HuaweiWifiWatchdogStateMachine.this.logE("apQualityRcd is null");
                return;
            }
            HuaweiWifiWatchdogStateMachine.this.logD("save bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + " history record, mEntriesSize = " + this.mEntriesSize);
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
                HuaweiWifiWatchdogStateMachine.this.logD("restoreBssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + " HistoreyRecord restore record, mEntriesSize = " + this.mEntriesSize);
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
                HuaweiWifiWatchdogStateMachine.this.logE("loadApQualityRecord read DB failed for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid));
                return false;
            }
        }

        private void initBQEHistoryRecord(WifiProApQualityRcd apQualityRcd) {
            if (loadApQualityRecord(apQualityRcd)) {
                HuaweiWifiWatchdogStateMachine.this.logD("succ load ApQualityRcd for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + ".");
                HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY, 1800000);
            } else {
                HuaweiWifiWatchdogStateMachine.this.logD("not ApQualityRcd for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid) + ".");
            }
            this.mIsHistoryLoaded = true;
        }

        public void afterConnectProcess() {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine.this.logD("afterConnectProcess enter for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid));
                initBQEHistoryRecord(this.mWifiProApQualityRcd);
            }
        }

        public void afterDisconnectProcess() {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine.this.logD("afterDisconnectProcess enter for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid));
                if (this.mIsHistoryLoaded) {
                    saveApQualityRecord(this.mWifiProApQualityRcd);
                    HuaweiWifiWatchdogStateMachine.this.removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY);
                } else {
                    HuaweiWifiWatchdogStateMachine.this.logE("afterDisconnectProcess not read bssid:" + this.mBssid + " record, can not save.");
                }
            }
        }

        public void initQualityRecordFromDatabase() {
            HuaweiWifiWatchdogStateMachine.this.logD("initQualityRecordFromDatabase enter for bssid:" + this.mBssid);
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
            HuaweiWifiWatchdogStateMachine.this.logE("getHistoryEntryByRssi invalid rssi idx: " + rssi);
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
            HuaweiWifiWatchdogStateMachine.this.mIsNotStatic = true;
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
                HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel = this.mTcpQuality;
                HuaweiWifiWatchdogStateMachine.this.logI("TCP result ##### Quality=" + HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel);
            }
            this.mUpdateTime = SystemClock.elapsedRealtime();
            this.mValueIsSet = true;
            return true;
        }

        public void dumpLog() {
            HuaweiWifiWatchdogStateMachine.this.logD("dumpTcpResult: len=" + this.lenth + ",rtt=" + this.mTcpRtt + ",rttPkt=" + this.mTcpRttPkts + ",rttWn=" + this.mTcpRttWhen + ",cgt=" + this.mTcpCongestion + ",cgtWn=" + this.mTcpCongWhen);
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
            HuaweiWifiWatchdogStateMachine.this.mIsNotStatic = true;
        }

        public void updateLossRate(double newValue, int newVolume) {
            if (newVolume > 0) {
                this.mOtaProduct += newValue * ((double) newVolume);
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
                    HuaweiWifiWatchdogStateMachine.this.logD("DefaultState ignore cmd: " + msg.what);
                    break;
                default:
                    HuaweiWifiWatchdogStateMachine.this.logE("Unhandled message " + msg + " in state " + HuaweiWifiWatchdogStateMachine.this.getCurrentState().getName());
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
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.sendMessage(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, huaweiWifiWatchdogStateMachine3.mRssiFetchToken = huaweiWifiWatchdogStateMachine3.mRssiFetchToken + 1, 0));
            HuaweiWifiWatchdogStateMachine.this.mWpLinkMonitorRunning = true;
            this.mPktChkTxbad = 0;
            this.mPktChkTxgood = 0;
            this.mPktChkRxgood = 0;
            this.mPktChkCnt = 0;
            this.mPktChkBadCnt = 0;
            this.mLastDnsFailCount = 0;
            this.mNetworkDisableCount = 0;
            HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount = 0;
            HuaweiWifiWatchdogStateMachine.this.monitorNetworkQos(true);
            this.isLastRssiBad = false;
            this.isLastOTABad = false;
            this.isLastTCPBad = false;
            this.adjustRssiCounter = 0;
            HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult = new TcpChkResult();
            resetPoorRssiTh();
            huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mRssiTHTimeoutToken = huaweiWifiWatchdogStateMachine.mRssiTHTimeoutToken + 1;
            huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mHighSpeedToken = huaweiWifiWatchdogStateMachine.mHighSpeedToken + 1;
            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = HuaweiWifiWatchdogStateMachine.INVALID_RSSI;
            HuaweiWifiWatchdogStateMachine.this.logD("new conn, poor rssi th :" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH);
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
            HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = 0;
            HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = 0;
            HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod = 0;
            huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mRssiChangedToken = huaweiWifiWatchdogStateMachine.mRssiChangedToken + 1;
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
                boolean isRttGood = avgrtt > 0 && avgrtt <= WifiproBqeUtils.BQE_GOOD_RTT;
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
                    HuaweiWifiWatchdogStateMachine.this.logD("goodperiod count=" + this.goodPeriodCounter + ", rb rttg sg =" + isRssiBetter + ", " + isRttGood + ", " + speedGood);
                }
            }
        }

        private void tryReportHomeApChr() {
            if (this.networkBadDetected) {
                HuaweiWifiWatchdogStateMachine.this.logI("tryReportHomeApChr have networkBadDetected.");
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
            HuaweiWifiWatchdogStateMachine.this.logI(" pkt chk not bad, reset sc to 0.");
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
                HuaweiWifiWatchdogStateMachine.this.logD("detectNetworkAvailable:hw.wifipro.dns_fail_count = " + currentDnsFailCount);
            } catch (NumberFormatException e) {
                HuaweiWifiWatchdogStateMachine.this.logE("detectNetworkAvailable  parseInt err:" + e);
            }
            if (tcpRxPkts != 0) {
                HuaweiWifiWatchdogStateMachine.this.logD("detectNetworkAvailable: tcpRxPkts != 0, reset");
                this.mNetworkDisableCount = 0;
            } else if (tcpTxPkts > 2) {
                HuaweiWifiWatchdogStateMachine.this.logD("detectNetworkAvailable: tcpTxPkts > 2");
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
                this.mNetworkDisableCount = 0;
                return false;
            }
            if (this.mNetworkDisableCount == 2 && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < -75) {
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
                HuaweiWifiWatchdogStateMachine.this.logD("remove old RSSI_TH_TIMEOUT msg.");
                HuaweiWifiWatchdogStateMachine.this.getHandler().removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT);
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.sendMessageDelayed(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT, huaweiWifiWatchdogStateMachine3.mRssiTHTimeoutToken = huaweiWifiWatchdogStateMachine3.mRssiTHTimeoutToken + 1, 0), (long) delayTime);
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
            HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH = newTHRssi;
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH < HuaweiWifiWatchdogStateMachine.POOR_NET_MIN_RSSI_TH) {
                HuaweiWifiWatchdogStateMachine.this.logD("TH set to minimum -85");
                HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH = HuaweiWifiWatchdogStateMachine.POOR_NET_MIN_RSSI_TH;
            }
            HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH - 1;
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH;
            }
        }

        private void updatePoorNetRssiTH(int currRssi) {
            if (currRssi != HuaweiWifiWatchdogStateMachine.INVALID_RSSI) {
                if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH < -75) {
                    if (currRssi <= HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                        HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH;
                        resetRssiTHValue(currRssi);
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.mRssiTHValidTime = huaweiWifiWatchdogStateMachine.mRssiTHValidTime + HwQoEService.KOG_CHECK_FG_APP_PERIOD;
                        if (HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime > 300000) {
                            HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = 300000;
                        }
                        sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                        HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED update th rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + ", rth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", nth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext + ", vtime: " + HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                    } else if (currRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext) {
                        HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = currRssi;
                        HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED update nth to rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
                    }
                } else if (currRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                    HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = 180000;
                    resetRssiTHValue(currRssi);
                    HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = -75;
                    sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                    HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED TH rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + ", rth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", nth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext + ", vtime: " + HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                }
            }
        }

        private void rssiTHValidTimeout() {
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext < -75) {
                resetRssiTHValue(HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
                HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = -75;
                HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = 180000;
                sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED TH timeout, turn to next:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH);
                return;
            }
            resetPoorRssiTh();
            HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED TH timeout reset.");
        }

        private void checkRssiTHBackoff(int currRssi, double tcpRetransRate, int tcpTxPkts, int tcpRetransPkts, int tcpRtt) {
            boolean isTcpRetranBad = (tcpRetransRate < 0.4d || tcpTxPkts <= 3) ? tcpTxPkts <= 3 && tcpRetransPkts >= 3 : true;
            boolean isRttbad = tcpRtt > 2000 || tcpRtt == 0;
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
            for (int i = 0; i < len; i++) {
                if (rssi >= HuaweiWifiWatchdogStateMachine.RSSI_RATE_TBL[i].RSSI_VAL) {
                    retRate = HuaweiWifiWatchdogStateMachine.RSSI_RATE_TBL[i].SW_RATE;
                    break;
                }
            }
            return retRate;
        }

        private void ssidChangeDetection() {
            if (HuaweiWifiWatchdogStateMachine.this.mCurrSSID == null || HuaweiWifiWatchdogStateMachine.this.mWifiInfo == null) {
                HuaweiWifiWatchdogStateMachine.this.logD("SSID is null, reset SGC.");
                resetSpdGoodCounter();
            } else if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID() == null || (HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID().equals(HuaweiWifiWatchdogStateMachine.this.mCurrSSID) ^ 1) == 0) {
                spdGoodParameterAge(HuaweiWifiWatchdogStateMachine.this.mSwitchOutTime);
            } else {
                HuaweiWifiWatchdogStateMachine.this.logI("SSID change to:" + HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID() + ", old SSID: " + HuaweiWifiWatchdogStateMachine.this.mCurrSSID + ", reset SGC.");
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
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mSpeedGoodCount = huaweiWifiWatchdogStateMachine.mSpeedGoodCount - ((float) reduceCount);
                }
                HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore = 0.0f;
                HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
            }
            HuaweiWifiWatchdogStateMachine.this.logD("SSID: " + HuaweiWifiWatchdogStateMachine.this.mCurrSSID + "not changed, not reset SGC. reduceCount:" + reduceCount + ", SGC:" + HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount);
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
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine;
            if (HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore > 0.0f) {
                if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount < HuaweiWifiWatchdogStateMachine.MAX_SPD_GOOD_COUNT) {
                    huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mSpeedGoodCount = huaweiWifiWatchdogStateMachine.mSpeedGoodCount + HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore;
                    if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount > HuaweiWifiWatchdogStateMachine.MAX_SPD_GOOD_COUNT) {
                        HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = HuaweiWifiWatchdogStateMachine.MAX_SPD_GOOD_COUNT;
                    }
                }
                HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore = 0.0f;
                HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
            } else if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount > 0.0f) {
                huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.mSpeedNotGoodCount = huaweiWifiWatchdogStateMachine.mSpeedNotGoodCount + 1;
                if (((float) HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount) >= HuaweiWifiWatchdogStateMachine.BACKOFF_NOT_SPD_GOOD_PERIOD_COUNT) {
                    huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mSpeedGoodCount = huaweiWifiWatchdogStateMachine.mSpeedGoodCount - 1.0f;
                    if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount < 0.0f) {
                        HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = 0.0f;
                    }
                    HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = 0;
                    HuaweiWifiWatchdogStateMachine.this.logD("spd good count backoff 1 to:" + HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount);
                }
            }
            this.mGoodSpeedRate = getSpdGoodSWRate(HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount);
            HuaweiWifiWatchdogStateMachine.this.logI("spd good count:" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr((double) HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount) + ", add sc:" + HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore + ", rate:" + this.mGoodSpeedRate);
        }

        private float getCurrPeriodScore(int rssi, boolean rssiTcpBad, boolean otaBad, boolean tcpBad, boolean veryBadRTT, String currentBssid) {
            float homeApRate;
            float rssiBadScore = 0.0f;
            float otaBadScore = 0.0f;
            float tcpBadScore = 0.0f;
            float veryBadRTTScore = 0.0f;
            if (this.mIsHomeApSwitchRateReaded) {
                homeApRate = this.mHomeApSwichRate;
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
            if (otaBadScore > rssiBadScore) {
                biggestScore = otaBadScore;
            }
            if (tcpBadScore > biggestScore) {
                biggestScore = tcpBadScore;
            }
            if (veryBadRTTScore > biggestScore) {
                biggestScore = veryBadRTTScore;
            }
            this.mRssiRate = getRssiSWRate(rssi);
            float retScore = biggestScore * this.mRssiRate;
            if (1.0f != HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate) {
                retScore *= HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate;
            }
            if (1.0f != this.mGoodSpeedRate && this.mPktChkBadCnt == 0) {
                retScore *= this.mGoodSpeedRate;
            }
            this.noHomeAPSwitchScore += retScore;
            if (1.0f != homeApRate && rssi >= -75) {
                retScore *= homeApRate;
            }
            HuaweiWifiWatchdogStateMachine.this.logI("Get rate: Rssi" + rssi + ", RssiRate:" + this.mRssiRate + ", HighDataFlow:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate + ", History: " + this.mGoodSpeedRate + ", add score: " + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr((double) retScore) + ", Homerate: " + homeApRate + ", mPktChkBadCnt:" + this.mPktChkBadCnt);
            return retScore;
        }

        private void updateHighDataFlowRate() {
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario != 1 || HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection <= 0) {
                if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == 2 && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 0) {
                    if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 50) {
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 0.0f;
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = (((float) (50 - HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection)) * HuaweiWifiWatchdogStateMachine.STREAMING_STEP_RATE) + 0.3f;
                    }
                }
            } else if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 20) {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 0.0f;
            } else {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = (((float) (20 - HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection)) * HuaweiWifiWatchdogStateMachine.DOWNLOAD_STEP_RATE) + 0.6f;
            }
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate > 1.0f || HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate < 0.0f) {
                HuaweiWifiWatchdogStateMachine.this.logI("wrong rate! mHighDataFlowRate  = %d" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate);
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 1.0f;
            }
            HuaweiWifiWatchdogStateMachine.this.logI("current mHighDataFlowRate = " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate);
        }

        private void handleHighDataFlow() {
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 0) {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection - 4;
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
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mHighDataFlowPeriodCounter = huaweiWifiWatchdogStateMachine.mHighDataFlowPeriodCounter + 1;
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowPeriodCounter % 4 == 0) {
                long currentHighDataFlowRxBytes = TrafficStats.getRxBytes(HuaweiWifiWatchdogStateMachine.WLAN_IFACE);
                long highDataFlowRxBytes = currentHighDataFlowRxBytes - HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes;
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes = currentHighDataFlowRxBytes;
                int lastScenario = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario;
                if (highDataFlowRxBytes >= 3145728) {
                    switch (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario) {
                        case 0:
                            if (highDataFlowRxBytes >= 5242880) {
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 1;
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 40;
                            } else if (HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow) {
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 1;
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 40;
                            }
                            HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow = true;
                            break;
                        case 1:
                            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter < 2) {
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 40;
                                break;
                            }
                            HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 2;
                            HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 80;
                            break;
                        case 2:
                            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter >= 2) {
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = 80;
                                break;
                            }
                            break;
                        default:
                            HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = 0;
                            HuaweiWifiWatchdogStateMachine.this.logI("wrong high data scenario, reset to HIGH_DATA_FLOW_NONE ");
                            break;
                    }
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter = 0;
                } else {
                    HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow = false;
                    huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mHighDataFlowNotDetectCounter = huaweiWifiWatchdogStateMachine.mHighDataFlowNotDetectCounter + 1;
                }
                HuaweiWifiWatchdogStateMachine.this.logD("high data flow: protection_counter = " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection + ",  not_detect_counter = " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter);
                HuaweiWifiWatchdogStateMachine.this.logD("high data flow scenario: " + lastScenario + " --> " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario + " rx bytes =" + (highDataFlowRxBytes / 1024) + "KB");
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
            Toast.makeText(HuaweiWifiWatchdogStateMachine.this.mContext, "RSSI:" + mrssi + " TotalScore:" + this.mSwitchScore + "  BadCount:" + this.mPktChkBadCnt + "\n" + scenario + HwQoEUtils.SEPARATOR + " Protection:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection + " Rate:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate, 1).show();
        }

        private void updateQoeLevel(int level) {
            HuaweiWifiWatchdogStateMachine.this.logD("Enter updateQoeLevel level = " + level);
            if (level != 0) {
                if (level == HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel) {
                    HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                    HuaweiWifiWatchdogStateMachine.this.logD("updateQoeLevel current level equal to mLastDetectLevel, return");
                    return;
                }
                if (level == 2) {
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
                    HuaweiWifiWatchdogStateMachine.this.logD("updateQoeLevel need two more periods, return");
                    return;
                }
                HuaweiWifiWatchdogStateMachine.this.logD("Leave updateQoeLevel level = " + HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel);
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
            } else if (tcp_rtt <= WifiproBqeUtils.BQE_GOOD_RTT) {
                rttLevel = 3;
            } else if (tcp_rtt <= WifiproBqeUtils.BQE_NOT_GOOD_RTT) {
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
            } else if (rssi >= HuaweiWifiWatchdogStateMachine.this.RSSI_TH_FOR_NOT_BAD_QOE_LEVEL && qosLevel == 0) {
                qosLevel = 2;
            }
            HuaweiWifiWatchdogStateMachine.this.logD("bqeLevel = " + qosLevel + " rtt=" + tcp_rtt + ",rtt_pkts=" + tcp_rtt_pkts + " speed=" + HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod + " rssi=" + rssi);
            if (qosLevel != 0) {
                updateQoeLevel(qosLevel);
            }
        }

        private void tryBackOffScore(long periodMaxSpeed, int avgRtt) {
            if (this.mSwitchScore > 0.0f) {
                boolean isScoreBackoffSpeedGood = periodMaxSpeed > 51200;
                boolean isScoreBackoffRTTGood = avgRtt > 0 && avgRtt <= WifiproBqeUtils.BQE_GOOD_RTT;
                HuaweiWifiWatchdogStateMachine.this.logD("score backoff: spdG=" + isScoreBackoffSpeedGood + ", rttG=" + isScoreBackoffRTTGood + ", oldSco=" + this.mSwitchScore);
                if (isScoreBackoffSpeedGood || this.mSwitchScore < 1.0f) {
                    resetPoorNetState();
                    return;
                } else if (isScoreBackoffRTTGood) {
                    this.mSwitchScore *= 0.5f;
                    this.noHomeAPSwitchScore *= 0.5f;
                    HuaweiWifiWatchdogStateMachine.this.logD("Good RTT:score backoff newScore=" + this.mSwitchScore);
                    return;
                } else {
                    this.mSwitchScore *= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_RATE;
                    this.noHomeAPSwitchScore *= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_RATE;
                    HuaweiWifiWatchdogStateMachine.this.logD("score backoff newSco=" + this.mSwitchScore);
                    return;
                }
            }
            resetPoorNetState();
        }

        private void handleRssiChanged(Message msg) {
            if (msg.arg2 == HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken) {
                long nowTime = SystemClock.elapsedRealtime();
                int newRssiVal = msg.arg1;
                if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi || nowTime - this.lastRSSIUpdateTime >= HuaweiWifiWatchdogStateMachine.IGNORE_RSSI_BROADCAST_TIME_TH) {
                    HuaweiWifiWatchdogStateMachine.this.mCurrRssi = newRssiVal;
                } else {
                    HuaweiWifiWatchdogStateMachine.this.mCurrRssi = ((HuaweiWifiWatchdogStateMachine.this.mCurrRssi * 1) + newRssiVal) / 2;
                }
                this.lastRSSIUpdateTime = nowTime;
                HuaweiWifiWatchdogStateMachine.this.logD("rssi change nRssi =" + newRssiVal + ", sRssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine != null) {
                    HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine.querySampleRtt();
                    return;
                }
                return;
            }
            HuaweiWifiWatchdogStateMachine.this.logD("ignore old rssi change message.");
        }

        private void handleRssiFetched(Message msg) {
            if (msg.arg1 == HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken) {
                if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid == null) {
                    HuaweiWifiWatchdogStateMachine.this.logD("MonitoringState WIFI not connected, skip fetch RSSI.");
                } else {
                    if (!HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk) {
                        HuaweiWifiWatchdogStateMachine.this.logI("WP Link Monitor State first check bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid) + ", call newLinkDetected");
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
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.sendMessageDelayed(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, huaweiWifiWatchdogStateMachine3.mRssiFetchToken = huaweiWifiWatchdogStateMachine3.mRssiFetchToken + 1, 0), HuaweiWifiWatchdogStateMachine.LINK_SAMPLING_INTERVAL_MS);
                return;
            }
            HuaweiWifiWatchdogStateMachine.this.logD("MonitoringState msg arg1:" + msg.arg1 + " != token:" + HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken + ", ignore this command.");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_BSSID_CHANGE /*135175*/:
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProLinkMonitoringState);
                    break;
                case HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH /*135179*/:
                    handleRssiFetched(msg);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_NOT_DATA_LINK /*135199*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState receive START_VERIFY_WITH_NOT_DATA_LINK cmd, start transition.");
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProVerifyingLinkState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_START_VERIFY_WITH_DATA_LINK /*135200*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState receive start data link monitor event, ignore.");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK /*135201*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState error receive STOP_VERIFY_WITH_NOT_DATA_LINK ignore.");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_STOP_VERIFY_WITH_DATA_LINK /*135202*/:
                    HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState receive STOP_VERIFY_WITH_DATA_LINK, stop now.");
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProStopVerifyState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_HIGH_NET_SPEED_DETECT_MSG /*136170*/:
                    if (HuaweiWifiWatchdogStateMachine.this.mHighSpeedToken == msg.arg1) {
                        updatePoorNetRssiTH(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                        break;
                    }
                    HuaweiWifiWatchdogStateMachine.this.logD(" have new high speed msg.");
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT /*136171*/:
                    if (HuaweiWifiWatchdogStateMachine.this.mRssiTHTimeoutToken == msg.arg1) {
                        rssiTHValidTimeout();
                        break;
                    }
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_RSSI_CHANGE /*136172*/:
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
                    this.mLMRssiWaitCount = 0;
                    if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null && HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null && msg.obj != null) {
                        RssiPacketCountInfo info = msg.obj;
                        HuaweiWifiWatchdogStateMachine.this.sendCurrentAPRssi(info.rssi);
                        int rssi = info.rssi;
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.mRssiChangedToken = huaweiWifiWatchdogStateMachine.mRssiChangedToken + 1;
                        long now = SystemClock.elapsedRealtime();
                        if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi || now - this.lastRSSIUpdateTime >= HuaweiWifiWatchdogStateMachine.IGNORE_RSSI_BROADCAST_TIME_TH) {
                            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = rssi;
                        } else {
                            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = ((HuaweiWifiWatchdogStateMachine.this.mCurrRssi * 1) + rssi) / 2;
                        }
                        this.lastRSSIUpdateTime = now;
                        if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine != null) {
                            HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine.querySampleRtt();
                            HuaweiWifiWatchdogStateMachine.this.mLastSampleRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                        }
                        HuaweiWifiWatchdogStateMachine.this.logD("LM mrssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                        int txbad = info.txbad;
                        int txgood = info.txgood;
                        int rxgood = info.rxgood;
                        int dbad = txbad - this.mLastTxBad;
                        int dgood = txgood - this.mLastTxGood;
                        int drxgood = rxgood - this.mLastRxGood;
                        int dtotal = dbad + dgood;
                        HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mLastTimeSample = now;
                        this.mLastTxBad = txbad;
                        this.mLastTxGood = txgood;
                        this.mLastRxGood = rxgood;
                        this.mIsHomeApSwitchRateReaded = false;
                        if (now - HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mLastTimeSample < 16000) {
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
                            if (this.mPktChkCnt >= 1) {
                                int pTot = this.mPktChkTxgood + this.mPktChkTxbad;
                                if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null) {
                                    HuaweiWifiWatchdogStateMachine.this.mCurrSSID = HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID();
                                }
                                updateSpdGoodCounter();
                                handleHighDataFlow();
                                if (pTot > 0) {
                                    double lossRate = ((double) this.mPktChkTxbad) / ((double) pTot);
                                    HuaweiWifiWatchdogStateMachine.this.logI("POta txb txg rxg:" + this.mPktChkTxbad + ", " + this.mPktChkTxgood + ", " + this.mPktChkRxgood + ". Lr =" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(100.0d * lossRate) + "% Totpkt=" + pTot);
                                    TcpChkResult mCurTcpChkResult = HuaweiWifiWatchdogStateMachine.this.getQueryTcpResult();
                                    ProcessedTcpResult processedTcpResult = new ProcessedTcpResult();
                                    int tcpTxPkts = 0;
                                    int tcpRxPkts = 0;
                                    int tcpRetransPkts = 0;
                                    boolean lastPeriodTcpResultValid = false;
                                    if (HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult == null) {
                                        HuaweiWifiWatchdogStateMachine.this.logD("LastMinTcpChkResult is null, new it.");
                                        HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult = new TcpChkResult();
                                    }
                                    if (mCurTcpChkResult == null || HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult == null) {
                                        HuaweiWifiWatchdogStateMachine.this.logD("read TCP Result failed. not calc tcp 30s pkt.");
                                    } else {
                                        HuaweiWifiWatchdogStateMachine.this.getCurrTcpRtt(processedTcpResult);
                                        if (HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mValueIsSet) {
                                            tcpTxPkts = mCurTcpChkResult.mTcpTxPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpTxPkts;
                                            tcpRxPkts = mCurTcpChkResult.mTcpRxPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpRxPkts;
                                            tcpRetransPkts = mCurTcpChkResult.mTcpRetransPkts - HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.mTcpRetransPkts;
                                            lastPeriodTcpResultValid = true;
                                        } else {
                                            HuaweiWifiWatchdogStateMachine.this.logD("not calc TCP period pkt, last result is null.");
                                        }
                                        mCurTcpChkResult.dumpLog();
                                    }
                                    if (mCurTcpChkResult != null) {
                                        HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.reset();
                                        HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult.copyFrom(mCurTcpChkResult);
                                    }
                                    if (!this.isFirstEnterMontoringState) {
                                        double tcpRetransRate = 0.0d;
                                        if (tcpTxPkts > 0) {
                                            tcpRetransRate = ((double) tcpRetransPkts) / ((double) tcpTxPkts);
                                        }
                                        HuaweiWifiWatchdogStateMachine.this.logI("PTcp RTT:" + processedTcpResult.mAvgRtt + ", rtt pkt=" + processedTcpResult.mTotPkt + ", tcp_rx=" + tcpRxPkts + ", tcp_tx=" + tcpTxPkts + ", tcp_reTran=" + tcpRetransPkts + ", rtRate=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(tcpRetransRate));
                                        if (HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount > 0 && HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                                            HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.addHistoryHSCount(HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount);
                                            HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount = 0;
                                        }
                                        boolean isRttGood = false;
                                        if (processedTcpResult.mAvgRtt < 1000 && processedTcpResult.mTotPkt > 2 && lossRate < HuaweiWifiWatchdogStateMachine.PKT_CHK_POOR_LINK_MIN_LOSE_RATE && pTot > 8 && tcpRetransRate < 0.1d && tcpTxPkts > 2) {
                                            HuaweiWifiWatchdogStateMachine.this.logD("isRttGood is good, AvgRtt = " + processedTcpResult.mAvgRtt + ", TotPkt = " + processedTcpResult.mTotPkt);
                                            isRttGood = true;
                                        }
                                        boolean isTCPBad = !isRttGood ? HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel != 1 ? HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel == 2 : true : false;
                                        boolean isRssiBad = false;
                                        if (this.mRssiBadCount >= 1) {
                                            isRssiBad = true;
                                        }
                                        boolean isTcpRetranBad = (tcpRetransRate < 0.2d || tcpTxPkts <= 3) ? tcpTxPkts <= 3 && tcpRetransPkts >= 3 : true;
                                        boolean isRttBadForLowRssi = processedTcpResult.mAvgRtt > 2000 && processedTcpResult.mTotPkt > 2;
                                        boolean lossRateVeryBad = lossRate >= 0.4d && pTot >= 50;
                                        boolean isPoorLinkForLowRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH ? !isRttBadForLowRssi ? lossRateVeryBad : true : false;
                                        boolean isWlanLossRateBad = lossRate > HuaweiWifiWatchdogStateMachine.PKT_CHK_POOR_LINK_MIN_LOSE_RATE && pTot > 8;
                                        if (!isPoorLinkForLowRssi && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.this.LOW_RSSI_TH && isWlanLossRateBad) {
                                            isPoorLinkForLowRssi = true;
                                        }
                                        boolean isRttBadForCongestion = (processedTcpResult.mAvgRtt > HwSelfCureUtils.SELFCURE_WIFI_ON_TIMEOUT || processedTcpResult.mAvgRtt == 0) ? processedTcpResult.mInadequateAvgRtt <= WifiproBqeUtils.BQE_GOOD_RTT ? processedTcpResult.mInadequateAvgRtt == 0 : true : false;
                                        boolean isWlanCongestion = (isWlanLossRateBad && isRttBadForCongestion) ? true : isWlanLossRateBad ? isTcpRetranBad : false;
                                        boolean isVeryBadRTT = false;
                                        if (processedTcpResult.mAvgRtt > HuaweiWifiWatchdogStateMachine.VERY_BAD_RTT_TH_FOR_TCP_BAD && processedTcpResult.mTotPkt < 50) {
                                            isVeryBadRTT = true;
                                        }
                                        boolean isOTABad = false;
                                        if (isPoorLinkForLowRssi || isWlanCongestion) {
                                            if (isRttGood) {
                                                isPoorLinkForLowRssi = false;
                                                isWlanCongestion = false;
                                                HuaweiWifiWatchdogStateMachine.this.logD("rtt good, igore bad result.");
                                            } else {
                                                isOTABad = true;
                                            }
                                        }
                                        if (isRssiBad) {
                                            HuaweiWifiWatchdogStateMachine.this.logD("rssi bad, mrssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrRssi + " < TargetRssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi);
                                            if (isTCPBad || (isOTABad ^ 1) == 0 || (this.isLastTCPBad ^ 1) == 0 || (this.isLastOTABad ^ 1) == 0 || tcpTxPkts <= 3 || tcpRxPkts <= 0) {
                                                this.adjustRssiCounter = 0;
                                            } else {
                                                this.adjustRssiCounter++;
                                                if (this.adjustRssiCounter >= 8) {
                                                    HuaweiWifiWatchdogStateMachine.this.adjustTargetPoorRssi(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                                                    this.adjustRssiCounter = 0;
                                                }
                                            }
                                        } else {
                                            HuaweiWifiWatchdogStateMachine.this.logD("mRssiBadCount =" + this.mRssiBadCount + ", RSSI not bad.");
                                        }
                                        if (!isNetSpeedOk(processedTcpResult.mAvgRtt)) {
                                            if (lastPeriodTcpResultValid) {
                                                if (!detectNetworkAvailable(tcpTxPkts, tcpRxPkts, tcpRetransPkts, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid)) {
                                                    if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi >= -75) {
                                                        HuaweiWifiWatchdogStateMachine.this.logI("Inet check network is not ok, triger active check.");
                                                        HuaweiWifiWatchdogStateMachine.this.sendResultMsgToQM(WifiproUtils.REQUEST_WIFI_INET_CHECK);
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 1;
                                                    } else {
                                                        HuaweiWifiWatchdogStateMachine.this.logI("Inet check bad and week rssi, triger active check.");
                                                        HuaweiWifiWatchdogStateMachine.this.sendResultMsgToQM(WifiproUtils.REQUEST_POOR_RSSI_INET_CHECK);
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 0;
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                    }
                                                    HuaweiWifiWatchdogStateMachine.this.mGoodDetectBaseRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                    this.isPoorLinkReported = true;
                                                    this.goodPeriodCounter = 0;
                                                    break;
                                                }
                                            }
                                            HuaweiWifiWatchdogStateMachine.this.logD("Inet check network, not get tcp data, skip.");
                                            if (tcpTxPkts <= 2 && tcpRxPkts <= 2) {
                                                computeQosLevel(processedTcpResult.mAvgRtt, processedTcpResult.mTotPkt, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, isWlanLossRateBad);
                                                HuaweiWifiWatchdogStateMachine.this.logD("did not have any network action");
                                                break;
                                            }
                                            HuaweiWifiWatchdogStateMachine.this.logI("rs ota tcp lr rttvb bad: " + isPoorLinkForLowRssi + ", " + isWlanCongestion + ", " + isTCPBad + ", " + isRssiBad + ", " + isVeryBadRTT + "; rsth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH);
                                            HuaweiWifiWatchdogStateMachine.this.logD("GSC:" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr((double) HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount) + ", GSR:" + this.mGoodSpeedRate + ", RSR:" + this.mRssiRate + ", RTH:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH);
                                            if (isOTABad || isTCPBad || isVeryBadRTT) {
                                                float retScore = getCurrPeriodScore(HuaweiWifiWatchdogStateMachine.this.mCurrRssi, isPoorLinkForLowRssi, isWlanCongestion, isTCPBad, isVeryBadRTT, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid);
                                                this.mSwitchScore += retScore;
                                                HuaweiWifiWatchdogStateMachine.this.logI("Add Score: " + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr((double) retScore) + "Total Score:" + this.mSwitchScore + ", notHomeAp:" + this.noHomeAPSwitchScore);
                                                this.mPktChkBadCnt++;
                                                updateQoeLevel(1);
                                                if (this.mPktChkBadCnt >= 2) {
                                                    HwWifiCHRStateManager hwWifiCHRStateManager = HwWifiCHRStateManagerImpl.getDefault();
                                                    if (hwWifiCHRStateManager != null) {
                                                        HuaweiWifiWatchdogStateMachine.this.logD("upload WIFI_ACCESS_WEB_SLOWLY event");
                                                        hwWifiCHRStateManager.updateWifiException(102, "WIFIPRO_WEB_SLOW");
                                                    }
                                                }
                                                if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager != null && HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.getIsHomeAP(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid)) {
                                                    this.networkBadDetected = true;
                                                }
                                                if (this.noHomeAPSwitchScore > HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE && this.mSwitchScore <= HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE) {
                                                    HuaweiWifiWatchdogStateMachine.this.logD("homeAPAddPeriodCount add 1");
                                                    this.homeAPAddPeriodCount++;
                                                }
                                                if (this.mSwitchScore > HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE || this.homeAPAddPeriodCount > 1) {
                                                    int currentWiFiLevel;
                                                    HuaweiWifiWatchdogStateMachine.this.logI(" pkt chk bad, ### mPktChkBadCnt=" + this.mPktChkBadCnt);
                                                    if (isRssiBad ? this.isLastRssiBad : false) {
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 0;
                                                        currentWiFiLevel = HuaweiWifiWatchdogStateMachine.this.wpPoorLinkLevelCalcByRssi(lossRate, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi, processedTcpResult.mAvgRtt);
                                                    } else {
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = 1;
                                                        currentWiFiLevel = HuaweiWifiWatchdogStateMachine.this.wpPoorLinkLevelCalcByTcp(lossRate, tcpRetransRate, processedTcpResult.mAvgRtt);
                                                    }
                                                    HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                    HuaweiWifiWatchdogStateMachine.this.mGoodDetectBaseRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                    HuaweiWifiWatchdogStateMachine.this.logI("mWifiPoorRssi = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi + ", WifiPoorReason = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason);
                                                    if (HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager == null || HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord == null) {
                                                        HuaweiWifiWatchdogStateMachine.this.logE("chr obj null error.");
                                                    } else {
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.resetAllParameters();
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mRSSI_VALUE = (short) HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                        if (pTot > 8) {
                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mOTA_PacketDropRate = (short) ((int) (1000.0d * lossRate));
                                                        }
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mRttAvg = (short) processedTcpResult.mAvgRtt;
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpInSegs = (short) tcpRxPkts;
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpOutSegs = (short) tcpTxPkts;
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpRetransSegs = (short) tcpRetransPkts;
                                                        synchronized (HuaweiWifiWatchdogStateMachine.this.mTraffic) {
                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mWIFI_NetSpeed = (short) ((int) (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed / 1024));
                                                        }
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mIPQLevel = (short) HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel;
                                                        if (HuaweiWifiWatchdogStateMachine.this.mCurrSSID != null) {
                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mRO_APSsid = HuaweiWifiWatchdogStateMachine.this.mCurrSSID;
                                                        }
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mHistoryQuilityRO_Rate = (short) ((int) (this.mGoodSpeedRate * 1000.0f));
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mHighDataRateRO_Rate = (short) ((int) (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate * 1000.0f));
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mCreditScoreRO_Rate = (short) 1000;
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.setBQERoveOutReason(isPoorLinkForLowRssi, isWlanCongestion, isTCPBad, isVeryBadRTT, HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord);
                                                    }
                                                    HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, currentWiFiLevel);
                                                    this.isPoorLinkReported = true;
                                                    tryReportHomeApChr();
                                                    this.mPktChkBadCnt = 0;
                                                    this.mSwitchScore = 0.0f;
                                                    this.isMaybePoorSend = false;
                                                } else {
                                                    HuaweiWifiWatchdogStateMachine.this.logI(" pkt chk bad, not chk now, ### mPktChkBadCnt=" + this.mPktChkBadCnt);
                                                    if (!this.isMaybePoorSend && ((this.mSwitchScore > HuaweiWifiWatchdogStateMachine.MAY_BE_POOR_TH || isPoorLinkForLowRssi) && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.MAY_BE_POOR_RSSI_TH)) {
                                                        HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, -2);
                                                        HuaweiWifiWatchdogStateMachine.this.logI("report maybe poor nw. sc:" + this.mSwitchScore);
                                                        this.isMaybePoorSend = true;
                                                    }
                                                    if (this.mPktChkBadCnt == 2 && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario != 0) {
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.increaseHighDataRateStopROC();
                                                    }
                                                }
                                                if (this.isPoorLinkReported) {
                                                    this.goodPeriodCounter = 0;
                                                }
                                            } else {
                                                networkGoodDetect(tcpRxPkts, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, processedTcpResult.mAvgRtt, false);
                                                checkRssiTHBackoff(HuaweiWifiWatchdogStateMachine.this.mCurrRssi, tcpRetransRate, tcpTxPkts, tcpRetransPkts, processedTcpResult.mAvgRtt);
                                                computeQosLevel(processedTcpResult.mAvgRtt, processedTcpResult.mTotPkt, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, isWlanLossRateBad);
                                                tryBackOffScore(HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod, processedTcpResult.mAvgRtt);
                                                HuaweiWifiWatchdogStateMachine.this.logD("### mPktChkBadCnt=" + this.mPktChkBadCnt);
                                            }
                                            this.isLastRssiBad = isRssiBad;
                                            this.isLastOTABad = isOTABad;
                                            this.isLastTCPBad = isTCPBad;
                                        } else {
                                            HuaweiWifiWatchdogStateMachine.this.logI("net speed good");
                                            networkGoodDetect(1, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, 0, true);
                                            computeQosLevel(processedTcpResult.mAvgRtt, processedTcpResult.mTotPkt, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, isWlanLossRateBad);
                                            HuaweiWifiWatchdogStateMachine.this.logD("### mPktChkBadCnt=" + this.mPktChkBadCnt);
                                            resetPeriodState();
                                            if (HuaweiWifiWatchdogStateMachine.DDBG_TOAST_DISPLAY) {
                                                debugToast(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                                            }
                                            this.mNetworkDisableCount = 0;
                                            break;
                                        }
                                    }
                                    this.isFirstEnterMontoringState = false;
                                    break;
                                }
                                handleNoTxPeriodInLinkMonitor();
                                if (HuaweiWifiWatchdogStateMachine.DDBG_TOAST_DISPLAY) {
                                    debugToast(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                                }
                                resetPeriodState();
                                break;
                            }
                        }
                    }
                    HuaweiWifiWatchdogStateMachine.this.logE("null error or wifi not connected, ignore RSSI_PKTCNT_FETCH event.");
                    break;
                    break;
                case 151574:
                    HuaweiWifiWatchdogStateMachine.this.logD("RSSI_FETCH_FAILED");
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void handleNoTxPeriodInLinkMonitor() {
            HuaweiWifiWatchdogStateMachine.this.logI("txbad/txgood is 0, no any pkt, ignore this period chk result.");
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
            HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod = false;
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
            HuaweiWifiWatchdogStateMachine.this.logI("WifiProVerifyingLinkState enter. PoorReason = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason + " , PoorRssi = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi);
            this.mRssiGoodCount = 0;
            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi;
            this.mIsOtaPoorTimeOut = false;
            this.mIsReportWiFiGoodAllow = false;
            this.mVLRssiWaitCount = 0;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.sendMessage(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, huaweiWifiWatchdogStateMachine3.mRssiFetchToken = huaweiWifiWatchdogStateMachine3.mRssiFetchToken + 1, 0));
            HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_AVOID_TO_WIFI_DELAY_MSG, 30000);
            if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null) {
                if (1 == HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason) {
                    HuaweiWifiWatchdogStateMachine.this.logI("start delay verify.");
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.otaTcpPoorLinkDetected();
                    if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime < MessageUtil.AUTO_CLOSE_SCAN_TIMER || HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime > HuaweiWifiWatchdogStateMachine.MAX_RESTORE_WIFI_TIME) {
                        HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime = MessageUtil.AUTO_CLOSE_SCAN_TIMER;
                    }
                    HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_DELAY_OTATCP_TIME_OUT_MSG, (long) HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime);
                } else if (HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason == 0) {
                    HuaweiWifiWatchdogStateMachine.this.logI("start RSSI verify.");
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.poorLinkDetected(HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi);
                }
                HuaweiWifiWatchdogStateMachine.this.logI("GoodLinkTargetRssi = " + HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetRssi + " , GoodLinkTargetCount =" + HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetCount);
            }
        }

        public void exit() {
            this.mIsReportWiFiGoodAllow = false;
            this.mIsOtaPoorTimeOut = false;
            HuaweiWifiWatchdogStateMachine.this.removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_DELAY_OTATCP_TIME_OUT_MSG);
            HuaweiWifiWatchdogStateMachine.this.removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_AVOID_TO_WIFI_DELAY_MSG);
            HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi = -90;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_BSSID_CHANGE /*135175*/:
                    break;
                case HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH /*135179*/:
                    if (msg.arg1 != HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken) {
                        HuaweiWifiWatchdogStateMachine.this.logD("VL msg.arg1:" + msg.arg1 + " != RssiFetchToken:" + HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken + ", ignore this command.");
                        break;
                    }
                    if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid == null) {
                        HuaweiWifiWatchdogStateMachine.this.logD("VL WIFI is not connected, not fetch RSSI.");
                    } else {
                        if (!HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk) {
                            HuaweiWifiWatchdogStateMachine.this.logD("VL first check bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid) + ",call newLinkDetected");
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
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.sendMessageDelayed(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, huaweiWifiWatchdogStateMachine3.mRssiFetchToken = huaweiWifiWatchdogStateMachine3.mRssiFetchToken + 1, 0), HuaweiWifiWatchdogStateMachine.ROVE_OUT_LINK_SAMPLING_INTERVAL_MS);
                    break;
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
                        RssiPacketCountInfo info = msg.obj;
                        HwWifiConnectivityMonitor monitor = HwWifiConnectivityMonitor.getInstance();
                        if (monitor != null) {
                            monitor.notifyBackgroundWifiLinkInfo(info.rssi, info.txgood, info.txbad, info.rxgood);
                        }
                        HuaweiWifiWatchdogStateMachine.this.sendCurrentAPRssi(info.rssi);
                        if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi) {
                            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = info.rssi;
                        } else {
                            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = (HuaweiWifiWatchdogStateMachine.this.mCurrRssi + info.rssi) / 2;
                        }
                        HuaweiWifiWatchdogStateMachine.this.logI("VL RSSI=" + HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                        HuaweiWifiWatchdogStateMachine.this.countDownGoodRssiByTime();
                        if (1 != HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason) {
                            tryRecoverWifiByRssi(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                            break;
                        }
                        tryRecoverWifiByOta(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                        break;
                    }
                    HuaweiWifiWatchdogStateMachine.this.logD("null error or WIFI is not connected, ignore packet fetch event. fg=" + HuaweiWifiWatchdogStateMachine.this.mIsNotStatic);
                    break;
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
                        HuaweiWifiWatchdogStateMachine.this.logI("tryRecoverByRssi, rssi=" + current_rssi + "dB, mRssiGoodCount =" + this.mRssiGoodCount);
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
                        HuaweiWifiWatchdogStateMachine.this.logI("Recover By Ota, rssi=" + current_rssi + ", poor rssi=" + HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi + ", RssiGoodCount =" + this.mRssiGoodCount + ", OtaPoorTimeOut =" + this.mIsOtaPoorTimeOut);
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
        private static final /* synthetic */ int[] -android-net-NetworkInfo$DetailedStateSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$android$net$NetworkInfo$DetailedState;

        private static /* synthetic */ int[] -getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
            if (-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
                return -android-net-NetworkInfo$DetailedStateSwitchesValues;
            }
            int[] iArr = new int[DetailedState.values().length];
            try {
                iArr[DetailedState.AUTHENTICATING.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DetailedState.BLOCKED.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 4;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[DetailedState.CONNECTED.ordinal()] = 5;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[DetailedState.CONNECTING.ordinal()] = 6;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[DetailedState.DISCONNECTED.ordinal()] = 7;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[DetailedState.DISCONNECTING.ordinal()] = 8;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[DetailedState.FAILED.ordinal()] = 9;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[DetailedState.IDLE.ordinal()] = 10;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 11;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[DetailedState.SCANNING.ordinal()] = 12;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[DetailedState.SUSPENDED.ordinal()] = 13;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 1;
            } catch (NoSuchFieldError e13) {
            }
            -android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
            return iArr;
        }

        WifiProWatchdogDisabledState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logD("DisabledState enter.");
            HuaweiWifiWatchdogStateMachine.this.wifiOtaChkIsEnable = false;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_NETWORK_STATE_CHANGE /*135170*/:
                    Intent intent = msg.obj;
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    HuaweiWifiWatchdogStateMachine.this.mWifiInfo = (WifiInfo) intent.getParcelableExtra("wifiInfo");
                    if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null) {
                        HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getBSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getNetworkId());
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, 0);
                    }
                    if (networkInfo != null) {
                        HuaweiWifiWatchdogStateMachine.this.logD("Network state change " + networkInfo.getDetailedState());
                        switch (-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[networkInfo.getDetailedState().ordinal()]) {
                            case 1:
                                HuaweiWifiWatchdogStateMachine.this.logD(" rcv VERIFYING_POOR_LINK, do nothing.");
                                break;
                        }
                    }
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_WP_WATCHDOG_ENABLE_CMD /*135198*/:
                    HuaweiWifiWatchdogStateMachine.this.logD("receive enable command, transition to WifiProStopVerifyState.");
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProStopVerifyState);
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_WP_WATCHDOG_DISABLE_CMD /*135203*/:
                    HuaweiWifiWatchdogStateMachine.this.logD("receive disable command, ignore.");
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class WifiProWatchdogEnabledState extends State {
        WifiProWatchdogEnabledState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logD("EnabledState enter.");
            HuaweiWifiWatchdogStateMachine.this.wifiOtaChkIsEnable = true;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_NETWORK_STATE_CHANGE /*135170*/:
                    Intent intent = msg.obj;
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    HuaweiWifiWatchdogStateMachine.this.mWifiInfo = (WifiInfo) intent.getParcelableExtra("wifiInfo");
                    if (networkInfo != null) {
                        HuaweiWifiWatchdogStateMachine.this.logI("nw state change to: " + networkInfo.getDetailedState());
                        if (networkInfo.getDetailedState() != DetailedState.CAPTIVE_PORTAL_CHECK) {
                            if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo == null) {
                                HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, 0);
                                break;
                            }
                            HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getBSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getNetworkId());
                            break;
                        }
                        HuaweiWifiWatchdogStateMachine.this.logD("CAPTIVE_PORTAL_CHECK state, not call updateCurrentBssid.");
                        break;
                    }
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_SUPPLICANT_STATE_CHANGE /*135172*/:
                    if (((SupplicantState) ((Intent) msg.obj).getParcelableExtra("newState")) == SupplicantState.COMPLETED) {
                        HuaweiWifiWatchdogStateMachine.this.mWifiInfo = HuaweiWifiWatchdogStateMachine.this.mWifiManager.getConnectionInfo();
                        if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo == null) {
                            HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, 0);
                            break;
                        }
                        HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getBSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID(), HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getNetworkId());
                        break;
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

    private float checkGoodSpd(int rssi, long spd) {
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
                float retScore = 0.5f + ((((float) (spd - ((long) RSSI_GOOD_SPD_TBL[i].SPD_VAL))) * ADD_UNIT_SPD_SCORE) / ((float) RSSI_GOOD_SPD_TBL[i].UNIT_SPD_VAL));
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
        this.mTraffic = new TrafficMonitor(this.mSpeedUpdate);
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

    private void monitorNetworkQos(boolean enable) {
        if (enable) {
            if (!this.mIsMonitoring) {
                logD("monitorNetworkQos start speed track.");
                this.mTraffic.enableMonitor(true, 1);
                this.mTraffic.setExpireTime(ClientHandler.DEFAULT_WIFI_ROUTE_CHECK_TIME_FIRST);
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
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.mRssiChangedToken = huaweiWifiWatchdogStateMachine.mRssiChangedToken + 1;
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
        if (WifiproUtils.WIFIPRO_START_VERIFY_WITH_NOT_DATA_LINK == mVerifyType) {
            logD("start VERIFY_WITH_NOT_DATA_LINK");
            sendMessage(EVENT_START_VERIFY_WITH_NOT_DATA_LINK);
        } else if (WifiproUtils.WIFIPRO_START_VERIFY_WITH_DATA_LINK == mVerifyType) {
            logD("start VERIFY_WITH_DATA_LINK ");
            sendMessage(EVENT_START_VERIFY_WITH_DATA_LINK);
        } else if (WifiproUtils.WIFIPRO_STOP_VERIFY_WITH_NOT_DATA_LINK == mVerifyType) {
            logD("stop VERIFY_WITH_NOT_DATA_LINK");
            sendMessage(EVENT_STOP_VERIFY_WITH_NOT_DATA_LINK);
        } else if (WifiproUtils.WIFIPRO_STOP_VERIFY_WITH_DATA_LINK == mVerifyType) {
            logD("stop VERIFY_WITH_DATA_LINK");
            sendMessage(EVENT_STOP_VERIFY_WITH_DATA_LINK);
        } else {
            logE("doWifiOTACheck not support command received: " + mVerifyType);
        }
    }

    private void postEvent(int what, int arg1, int arg2) {
        if (this.mQMHandler != null) {
            this.mQMHandler.sendMessage(this.mQMHandler.obtainMessage(what, arg1, arg2));
            return;
        }
        logE("postEvent msg handle null point error.");
    }

    private void sendResultMsgToQM(int mQosLevel) {
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

    private void countDownGoodRssiByTime() {
        if (this.mCurrentBssid.mGoodLinkTargetRssi > this.mCurrentBssid.mTimeElapGoodLinkTargetRssi) {
            long elapTime = SystemClock.elapsedRealtime() - this.mCurrentBssid.mTimeElapBaseTime;
            if (elapTime >= 120000) {
                long subDb = (long) ((int) (elapTime / 120000));
                if (subDb > 0) {
                    BssidStatistics bssidStatistics;
                    int old_rssi = this.mCurrentBssid.mGoodLinkTargetRssi;
                    if (subDb >= ((long) (this.mCurrentBssid.mGoodLinkTargetRssi - this.mCurrentBssid.mTimeElapGoodLinkTargetRssi))) {
                        this.mCurrentBssid.mGoodLinkTargetRssi = this.mCurrentBssid.mTimeElapGoodLinkTargetRssi;
                    } else {
                        bssidStatistics = this.mCurrentBssid;
                        bssidStatistics.mGoodLinkTargetRssi -= (int) subDb;
                    }
                    logD(" elapse time GL target rssi=" + this.mCurrentBssid.mTimeElapGoodLinkTargetRssi + ", adjust GL rssi from " + old_rssi + " to " + this.mCurrentBssid.mGoodLinkTargetRssi + " by elapse time :" + (2 * subDb) + " Min.");
                    bssidStatistics = this.mCurrentBssid;
                    bssidStatistics.mTimeElapBaseTime += 120000 * subDb;
                }
            }
        }
    }

    private void adjustTargetPoorRssi(int badRssi) {
        int oldRssi = this.mCurrentBssid.mPoorLinkTargetRssi;
        this.mCurrentBssid.mPoorLinkTargetRssi = (oldRssi + badRssi) / 2;
        logD("adjustTargetPoorRssi current mrssi=" + badRssi + ", ajust poor target rssi from:" + oldRssi + " to " + this.mCurrentBssid.mPoorLinkTargetRssi);
    }

    private int wpPoorLinkLevelCalcByRssi(double lossRate, int mrssi, int poor_link_rssi, int rtt) {
        if (((double) mrssi) <= ((double) poor_link_rssi) - BAD_RSSI_LEVEL_0_NOT_AVAILABLE_SUB_VAL || lossRate >= 0.4d || rtt >= 8000) {
            return 0;
        }
        if (((double) mrssi) <= ((double) poor_link_rssi) - BAD_RSSI_LEVEL_1_VERY_POOR_SUB_VAL || lossRate >= 0.30000000000000004d || rtt >= ClientHandler.DEFAULT_WIFI_ROUTE_CHECK_TIME_FIRST) {
            return 1;
        }
        return 2;
    }

    private int wpPoorLinkLevelCalcByTcp(double lossRate, double tcpRetxRate, int rtt) {
        int mCnePoorLinkLevel;
        if (tcpRetxRate >= PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE || lossRate >= 0.4d || rtt >= 8000) {
            mCnePoorLinkLevel = 0;
        } else if (tcpRetxRate >= PKT_CHK_RETXRATE_LEVEL_1_VERY_POOR || lossRate >= 0.30000000000000004d || rtt >= ClientHandler.DEFAULT_WIFI_ROUTE_CHECK_TIME_FIRST) {
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

    private void getCurrTcpRtt(ProcessedTcpResult mTcpResult) {
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

    private void updateCurrentBssid(String bssid, String ssid, int networkID) {
        logI("Update current BSSID to " + (bssid != null ? partDisplayBssid(bssid) : "null"));
        if (bssid != null) {
            logI(" mCurrentBssid  current bssid is:" + partDisplayBssid(bssid));
            if (this.mCurrentBssid == null || !bssid.equals(this.mCurrentBssid.mBssid)) {
                if (this.mCurrentBssid != null) {
                    this.mCurrentBssid.afterDisconnectProcess();
                }
                this.mCurrentBssid = (BssidStatistics) this.mBssidCache.get(bssid);
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

    private String formatFloatToStr(double data) {
        if (this.mFormatData != null) {
            return this.mFormatData.format(data);
        }
        return DEFAULT_DISPLAY_DATA_STR;
    }

    private void sendLinkStatusNotification(boolean isGood, int linkLevel) {
        if (isGood) {
            logI("judge good link######, goodLinkLevel=" + linkLevel);
            sendResultMsgToQM(linkLevel);
            return;
        }
        logI("judge poor link######, poorLinkLevel=" + linkLevel);
        sendResultMsgToQM(linkLevel);
    }

    public void setSampleRtt(int[] ipqos, int len) {
        int i = 0;
        if (this.mIsDualbandEnable) {
            try {
                if (this.mCurrentBssid == null) {
                    Log.d(TAG, "setSampleRtt: Bssid is null, return");
                } else if (this.mCurrentBssid.mBssid.equals(this.mLastSampleBssid)) {
                    this.mLastSampleBssid = this.mCurrentBssid.mBssid;
                    int tcp_rtt_when = len > 2 ? ipqos[2] : 0;
                    if (tcp_rtt_when >= 10) {
                        Log.d(TAG, "setSampleRtt: unvalid rtt");
                        this.mLastSampleRtt = 0;
                        this.mLastSamplePkts = 0;
                        return;
                    }
                    int i2;
                    if (len > 0) {
                        i2 = ipqos[0];
                    } else {
                        i2 = 0;
                    }
                    this.mLastSampleRtt = i2;
                    if (len > 1) {
                        i = ipqos[1];
                    }
                    this.mLastSamplePkts = i;
                    this.mCurrentBssid.updateRtt(this.mLastSampleRssi, this.mLastSampleRtt, this.mLastSamplePkts);
                    this.mHwDualBandQualityEngine.resetSampleRtt();
                    Log.d(TAG, "setSampleRtt: rtt=" + this.mLastSampleRtt + ",rtt_pkts =" + this.mLastSamplePkts + ",rtt_when=" + tcp_rtt_when);
                } else {
                    this.mLastSampleBssid = this.mCurrentBssid.mBssid;
                    Log.d(TAG, "setSampleRtt: Bssid changed, return");
                }
            } catch (NullPointerException e) {
                Log.d(TAG, "NullPointerException e" + e);
            }
        }
    }

    public int updateQosLevelByHistory() {
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
        int qosLevel;
        int avgRtt = this.mCurrentBssid.getHistoryNearRtt(rssi);
        if (avgRtt == 0) {
            qosLevel = 0;
        } else if (avgRtt <= WifiproBqeUtils.BQE_GOOD_RTT) {
            qosLevel = 3;
        } else if (avgRtt <= WifiproBqeUtils.BQE_NOT_GOOD_RTT) {
            qosLevel = 2;
        } else {
            qosLevel = 1;
        }
        if (qosLevel != 0) {
            return qosLevel;
        }
        if (rssi >= -65) {
            qosLevel = 3;
        } else if (rssi >= QOS_NOT_GOOD_LEVEL_OF_RSSI) {
            qosLevel = 2;
        } else {
            qosLevel = 1;
        }
        return qosLevel;
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
        BssidStatistics mBssidStat = (BssidStatistics) this.mBssidCache.get(apBssid);
        if (mBssidStat == null) {
            mBssidStat = new BssidStatistics(apBssid);
            mBssidStat.initQualityRecordFromDatabase();
            this.mBssidCache.put(apBssid, mBssidStat);
        } else {
            logI(" get mCurrentBssid inCache for new bssid.");
        }
        return mBssidStat;
    }

    private void target5GApRssiTHProcess(WifiProEstimateApInfo estimateApInfo) {
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
        estimateApInfo.setRetRssiTH(apQualityInfo.findRssiTargetByRtt(-80, -45, 1500, -65));
        sendDBQoeResult(12, estimateApInfo);
    }

    private void targetApHistoryQualityScoreProcess(WifiProEstimateApInfo estimateApInfo) {
        estimateApInfo.setRetHistoryScore(getApScore(estimateApInfo));
        sendDBQoeResult(13, estimateApInfo);
    }

    private int getApScore(WifiProEstimateApInfo estimateApInfo) {
        if (this.mCurrentBssid == null || this.mCurrentBssid.mBssid == null || this.mHwDualBandQualityEngine == null || estimateApInfo == null) {
            logE("getApHistoryQualityScore null pointer.");
            return -1;
        }
        logD("getApHistoryQualityScore: ssid=" + estimateApInfo.getApSsid() + " bssid=" + partDisplayBssid(estimateApInfo.getApBssid()));
        int retScore = getApFixedScore(estimateApInfo) + 0;
        int variedScore = getApVariedScore(estimateApInfo);
        if (variedScore == -1) {
            return -1;
        }
        retScore += variedScore;
        logD("ssid:" + estimateApInfo.getApSsid() + " bssid:" + partDisplayBssid(estimateApInfo.getApBssid()) + " total score is " + retScore);
        return retScore;
    }

    private int getApFixedScore(WifiProEstimateApInfo estimateApInfo) {
        int score;
        int fixedScore = 0;
        String apBssid = estimateApInfo.getApBssid();
        WifiProApInfoRecord apInfoRecord = this.mWifiProHistoryRecordManager.getApInfoRecord(apBssid);
        if (apInfoRecord != null) {
            score = this.mHwDualBandQualityEngine.getScoreByConnectTime(apInfoRecord.totalUseTime / WifiProHistoryRecordManager.SECOND_OF_ONE_HOUR);
            logD("bssid:" + partDisplayBssid(apBssid) + " ConnectTime score is " + score);
            fixedScore = score + 0;
        }
        score = this.mHwDualBandQualityEngine.getScoreByRssi(estimateApInfo.getApRssi());
        logD("bssid:" + partDisplayBssid(apBssid) + " rssi score is " + score);
        fixedScore += score;
        if (estimateApInfo.is5GAP()) {
            score = this.mHwDualBandQualityEngine.getScoreByBluetoothUsage();
            logD("bssid:" + partDisplayBssid(apBssid) + " BluetoothUsage score is " + score);
            fixedScore += score;
        }
        if (!HwDualBandRelationManager.isDualBandAP(this.mCurrentBssid.mBssid, apBssid) || !estimateApInfo.is5GAP()) {
            return fixedScore;
        }
        logD("bssid:" + partDisplayBssid(apBssid) + " DUAL_BAND_SINGLE_AP score is " + 6);
        return fixedScore + 6;
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
        int score;
        int queryRssi = estimateApInfo.getApRssi();
        if (queryRssi > -45) {
            queryRssi = -45;
        } else if (queryRssi < -90) {
            queryRssi = -90;
        }
        int avgRtt = apQualityInfo.getHistoryNearRtt(queryRssi);
        if (avgRtt != 0) {
            score = this.mHwDualBandQualityEngine.getScoreByRtt(avgRtt);
            logD("bssid:" + partDisplayBssid(apBssid) + " rtt score is " + score);
            variedScore = score + 0;
        } else {
            logD("There is no rtt history about rssi " + queryRssi);
        }
        double[] lossRateInfo = apQualityInfo.getHistoryNearLossRate(queryRssi);
        if (lossRateInfo[1] > 10.0d) {
            score = this.mHwDualBandQualityEngine.getScoreByLossRate(lossRateInfo[0]);
            logD("bssid:" + partDisplayBssid(apBssid) + " rtt score is " + score);
            variedScore += score;
        }
        return variedScore;
    }

    private void sendCurrentAPRssi(int rssi) {
        if (this.mIsDualbandEnable) {
            if (this.mQMHandler == null || INVALID_RSSI == rssi) {
                logE("sendCurryAPRssi null error.");
            } else {
                this.mQMHandler.sendMessage(this.mQMHandler.obtainMessage(14, Integer.valueOf(rssi)));
            }
        }
    }

    private void logD(String msg) {
        if (printLogLevel <= 1) {
            Log.d(TAG, msg);
        }
    }

    private void logI(String msg) {
        if (printLogLevel <= 2) {
            Log.i(TAG, msg);
        }
    }

    private void logE(String msg) {
        if (printLogLevel <= 3) {
            Log.e(TAG, msg);
        }
    }

    private String partDisplayBssid(String srcBssid) {
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
