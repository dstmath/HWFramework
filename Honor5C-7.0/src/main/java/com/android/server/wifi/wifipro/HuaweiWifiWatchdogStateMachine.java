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
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.wifipro.TrafficMonitor.TxRxStat;
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
    private static final GoodLinkTarget[] GOOD_LINK_TARGET = null;
    private static final int GOOD_LINK_TARGET_MIN_ADD_DB_VAL = 3;
    private static final int GOOD_RTT_TARGET = 1500;
    private static final GoodSpdCountRate[] GOOD_SPD_COUNT_RATE_TBL = null;
    private static final int GOOD_TCP_JUDGE_RTT_PKT = 50;
    private static final int HIGH_DATA_FLOW_BYTES_TH = 3145728;
    private static final float HIGH_DATA_FLOW_DEFAULT_RATE = 1.0f;
    private static final int HIGH_DATA_FLOW_DOWNLOAD = 1;
    private static final int HIGH_DATA_FLOW_DOWNLOAD_TH = 5242880;
    private static final int HIGH_DATA_FLOW_NONE = 0;
    private static final int HIGH_DATA_FLOW_STREAMING = 2;
    private static final int HIGH_NET_SPEED_TH = 65536;
    private static final int HISTORY_HIGH_SPEED_THRESHOLD = 1048576;
    private static final boolean HISTORY_RCD_DBG_MODE = true;
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
    private static final RestoreWifiTime[] RESTORE_WIFI_TIME = null;
    private static final long ROVE_OUT_LINK_SAMPLING_INTERVAL_MS = 2000;
    private static final RssiGoodSpdTH[] RSSI_GOOD_SPD_TBL = null;
    private static final RssiRate[] RSSI_RATE_TBL = null;
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
    private static final String WLAN_IFACE = null;
    private static final int WLAN_INET_RSSI_LEVEL_3_GOOD = 0;
    private static final int WLAN_INET_RSSI_LEVEL_4_BETTER = 5;
    private static final int WLAN_INET_RSSI_LEVEL_5_BEST = 8;
    private static double[] mSPresetLoss;
    private static int printLogLevel;
    private int LOW_RSSI_TH;
    private int QOE_LEVEL_RTT_MIN_PKT;
    private int RSSI_TH_FOR_BAD_QOE_LEVEL;
    private int RSSI_TH_FOR_NOT_BAD_QOE_LEVEL;
    private long mBestSpeedInPeriod;
    private BroadcastReceiver mBroadcastReceiver;
    private LruCache<String, BssidStatistics> mBssidCache;
    private Context mContext;
    private int mCurrRssi;
    private String mCurrSSID;
    private int mCurrentBqeLevel;
    private BssidStatistics mCurrentBssid;
    private DecimalFormat mFormatData;
    private int mGoodDetectBaseRssi;
    private long mHighDataFlowLastRxBytes;
    private int mHighDataFlowNotDetectCounter;
    private int mHighDataFlowPeriodCounter;
    private int mHighDataFlowProtection;
    private float mHighDataFlowRate;
    private int mHighDataFlowScenario;
    private int mHighSpeedToken;
    private int mHistoryHSCount;
    private HwDualBandQualityEngine mHwDualBandQualityEngine;
    private IntentFilter mIntentFilter;
    private boolean mIsDualbandEnable;
    private boolean mIsMonitoring;
    private boolean mIsNotStatic;
    private boolean mIsRegister;
    private boolean mIsSpeedOkDuringPeriod;
    private int mLastDetectLevel;
    private boolean mLastHighDataFlow;
    private TcpChkResult mLastPeriodTcpChkResult;
    private String mLastSampleBssid;
    private int mLastSamplePkts;
    private int mLastSampleRssi;
    private int mLastSampleRtt;
    private TcpChkResult mNewestTcpChkResult;
    private float mPeriodGoodSpdScore;
    private int mPoorNetRssiRealTH;
    private int mPoorNetRssiTH;
    private int mPoorNetRssiTHNext;
    private Handler mQMHandler;
    private int mRssiChangedToken;
    private int mRssiFetchToken;
    private int mRssiTHTimeoutToken;
    private int mRssiTHValidTime;
    private float mSpeedGoodCount;
    private int mSpeedNotGoodCount;
    private Runnable mSpeedUpdate;
    private long mSwitchOutTime;
    private volatile int mTcpReportLevel;
    private TrafficMonitor mTraffic;
    private volatile TxRxStat mTxrxStat;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;
    private int mWifiPoorReason;
    private int mWifiPoorRssi;
    private WifiProDefaultState mWifiProDefaultState;
    private WifiProHistoryRecordManager mWifiProHistoryRecordManager;
    private WifiProLinkMonitoringState mWifiProLinkMonitoringState;
    private WifiProRoveOutParaRecord mWifiProRoveOutParaRecord;
    private WifiProStatisticsManager mWifiProStatisticsManager;
    private WifiProStopVerifyState mWifiProStopVerifyState;
    private WifiProVerifyingLinkState mWifiProVerifyingLinkState;
    private WifiProWatchdogDisabledState mWifiProWatchdogDisabledState;
    private WifiProWatchdogEnabledState mWifiProWatchdogEnabledState;
    private boolean mWpLinkMonitorRunning;
    private AsyncChannel mWsmChannel;
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
        private boolean mIsHistoryLoaded;
        public boolean mIsNotFirstChk;
        private long mLastTimeRssiPoor;
        private long mLastTimeSample;
        private long mLastTimeotaTcpPoor;
        public int mPoorLinkTargetRssi;
        public int mRestoreDelayTime;
        public int mRestoreTimeIdx;
        private int mRssiBase;
        public long mTimeElapBaseTime;
        public int mTimeElapGoodLinkTargetRssi;
        private WifiProApQualityRcd mWifiProApQualityRcd;

        public BssidStatistics(String bssid) {
            this.mIsNotFirstChk = false;
            this.mPoorLinkTargetRssi = HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD;
            this.mLastTimeRssiPoor = 0;
            this.mLastTimeotaTcpPoor = 0;
            this.mIsHistoryLoaded = false;
            this.mBssid = bssid;
            this.mRssiBase = HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM;
            this.mEntriesSize = 46;
            this.mEntries = new VolumeWeightedEMA[this.mEntriesSize];
            for (int i = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD; i < this.mEntriesSize; i += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
                this.mEntries[i] = new VolumeWeightedEMA(HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_1_VERY_POOR);
            }
            this.mRestoreDelayTime = HuaweiWifiWatchdogStateMachine.TIME_ELAP_2_MIN;
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                this.mWifiProApQualityRcd = new WifiProApQualityRcd(this.mBssid);
            }
        }

        public void updateLoss(int rssi, double value, int volume) {
            if (volume > 0) {
                if (rssi >= HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_HIGH_DBM) {
                    rssi = HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_HIGH_DBM;
                } else if (rssi < HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM) {
                    rssi = HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM;
                }
                int index = rssi - this.mRssiBase;
                if (index >= 0 && index < this.mEntriesSize) {
                    this.mEntries[index].updateLossRate(value, volume);
                    if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                        HuaweiWifiWatchdogStateMachine.this.logD(this.mBssid + " current loss[" + rssi + "]=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(value * 100.0d) + "%, volume=" + volume);
                        HuaweiWifiWatchdogStateMachine.this.logD(this.mBssid + " history loss[" + rssi + "]=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[index].mOtaLossRate * 100.0d) + "%, volume=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(this.mEntries[index].mOtaVolume));
                    }
                }
            }
        }

        public void updateRtt(int rssi, int value, int volume) {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && volume > 0) {
                if (rssi >= HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_HIGH_DBM) {
                    rssi = HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_HIGH_DBM;
                } else if (rssi < HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM) {
                    rssi = HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM;
                }
                int index = rssi - this.mRssiBase;
                if (index >= 0 && index < this.mEntriesSize) {
                    this.mEntries[index].updateAvgRtt(value, volume);
                    HuaweiWifiWatchdogStateMachine.this.logD(this.mBssid + " current RTT[" + rssi + "]=" + value + ", volume=" + volume);
                    HuaweiWifiWatchdogStateMachine.this.logD(this.mBssid + " history RTT[" + rssi + "]=" + this.mEntries[index].mAvgRtt + ", volume=" + this.mEntries[index].mRttVolume);
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
            if (rssi > HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_HIGH_DBM) {
                rssi = HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_HIGH_DBM;
            } else if (rssi < HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM) {
                rssi = HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM;
            }
            return this.mEntries[rssi - this.mRssiBase];
        }

        public int getHistoryNearRtt(int baseRssi) {
            if (baseRssi > HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_HIGH_DBM) {
                baseRssi = HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_HIGH_DBM;
            } else if (baseRssi < HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM) {
                baseRssi = HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM;
            }
            int baseIndex = baseRssi - this.mRssiBase;
            int near_rssi_count = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            long avgRtt = this.mEntries[baseIndex].mAvgRtt;
            long totalRttVolume = this.mEntries[baseIndex].mRttVolume;
            long totalRttProduct = this.mEntries[baseIndex].mRttProduct;
            while (near_rssi_count <= MAX_NEAR_RSSI_COUNT) {
                near_rssi_count += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
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
                if (near_rssi_count >= MIN_NEAR_RSSI_COUNT && totalRttVolume >= 10) {
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
            int near_rssi_count = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            double avgLossRate = this.mEntries[baseIndex].mOtaLossRate;
            double totalOtaVolume = this.mEntries[baseIndex].mOtaVolume;
            double totalOtaProduct = this.mEntries[baseIndex].mOtaProduct;
            double[] lossRateInfo = new double[MIN_NEAR_RSSI_COUNT];
            while (near_rssi_count <= MAX_NEAR_RSSI_COUNT) {
                near_rssi_count += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
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
                if (near_rssi_count >= MIN_NEAR_RSSI_COUNT && totalOtaVolume >= 10.0d) {
                    break;
                }
            }
            if (totalOtaVolume > HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_2_POOR) {
                avgLossRate = totalOtaProduct / totalOtaVolume;
            }
            HuaweiWifiWatchdogStateMachine.this.logD("from rssi " + (baseRssi - near_rssi_count) + " to " + (baseRssi + near_rssi_count) + " avgLossRate=" + String.valueOf(avgLossRate) + "voludme=" + String.valueOf(totalOtaVolume));
            lossRateInfo[HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD] = avgLossRate;
            lossRateInfo[HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD] = totalOtaVolume;
            return lossRateInfo;
        }

        public double presetLoss(int rssi) {
            if (rssi <= HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM) {
                return HuaweiWifiWatchdogStateMachine.PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE;
            }
            if (rssi > 0) {
                return HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_2_POOR;
            }
            if (HuaweiWifiWatchdogStateMachine.mSPresetLoss == null) {
                HuaweiWifiWatchdogStateMachine.mSPresetLoss = new double[90];
                for (int i = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD; i < 90; i += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
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
            while (this.mGoodLinkTargetIndex > 0 && lastPoor >= ((long) HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET[this.mGoodLinkTargetIndex + HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID].REDUCE_TIME_MS)) {
                this.mGoodLinkTargetIndex += HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID;
            }
            this.mGoodLinkTargetCount = HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET[this.mGoodLinkTargetIndex].SAMPLE_COUNT;
            this.mTimeElapGoodLinkTargetRssi = findRssiTarget(rssi, rssi + HuaweiWifiWatchdogStateMachine.GOOD_LINK_RSSI_RANGE_MAX, HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_0_NOT_AVAILABLE);
            this.mTimeElapBaseTime = now;
            if (this.mTimeElapGoodLinkTargetRssi < rssi + HuaweiWifiWatchdogStateMachine.TCP_MIN_TX_PKT) {
                this.mGoodLinkTargetRssi = rssi + HuaweiWifiWatchdogStateMachine.TCP_MIN_TX_PKT;
            } else {
                this.mGoodLinkTargetRssi = this.mTimeElapGoodLinkTargetRssi;
            }
            this.mGoodLinkTargetRssi += HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET[this.mGoodLinkTargetIndex].RSSI_ADJ_DBM;
            if (this.mGoodLinkTargetIndex < HuaweiWifiWatchdogStateMachine.GOOD_LINK_TARGET.length + HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID) {
                this.mGoodLinkTargetIndex += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            }
            HuaweiWifiWatchdogStateMachine.this.logI("goodRssi=" + this.mGoodLinkTargetRssi + " TimeElapGoodRssiTarget=" + this.mTimeElapGoodLinkTargetRssi + " goodSampleCount=" + this.mGoodLinkTargetCount + " lastPoor=" + lastPoor + ",new GoodLinkTargetIndex=" + this.mGoodLinkTargetIndex);
            return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
        }

        private void otaTcpPoorLinkDetected() {
            HuaweiWifiWatchdogStateMachine.this.logD("otaTcpPoor enter.");
            long now = SystemClock.elapsedRealtime();
            long lastPoor = now - this.mLastTimeotaTcpPoor;
            this.mLastTimeotaTcpPoor = now;
            if (this.mRestoreTimeIdx < 0) {
                this.mRestoreTimeIdx = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            }
            if (this.mRestoreTimeIdx >= HuaweiWifiWatchdogStateMachine.RESTORE_WIFI_TIME.length) {
                this.mRestoreTimeIdx = HuaweiWifiWatchdogStateMachine.RESTORE_WIFI_TIME.length + HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID;
            }
            while (this.mRestoreTimeIdx > 0 && lastPoor >= ((long) HuaweiWifiWatchdogStateMachine.RESTORE_WIFI_TIME[this.mRestoreTimeIdx + HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID].LEVEL_SEL_TIME)) {
                this.mRestoreTimeIdx += HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID;
            }
            this.mRestoreDelayTime = HuaweiWifiWatchdogStateMachine.RESTORE_WIFI_TIME[this.mRestoreTimeIdx].RESTORE_TIME;
            this.mRestoreTimeIdx += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            HuaweiWifiWatchdogStateMachine.this.logI("otaTcpPoor Restore Time=" + this.mRestoreDelayTime + ", RestoreTimeIdx=" + this.mRestoreTimeIdx);
        }

        public void newLinkDetected() {
            HuaweiWifiWatchdogStateMachine.this.logD(" newLinkDetected enter");
            this.mGoodLinkTargetRssi = findRssiTarget(HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM, HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_HIGH_DBM, HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_0_NOT_AVAILABLE);
            this.mPoorLinkTargetRssi = HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD;
            if (this.mGoodLinkTargetRssi < this.mPoorLinkTargetRssi) {
                HuaweiWifiWatchdogStateMachine.this.logD(" Poor link detected, select " + this.mGoodLinkTargetRssi + " < " + this.mPoorLinkTargetRssi + "dB, set GoodLink base rssi=" + this.mPoorLinkTargetRssi + "dB.");
                this.mGoodLinkTargetRssi = this.mPoorLinkTargetRssi;
            }
            this.mTimeElapGoodLinkTargetRssi = this.mGoodLinkTargetRssi;
            this.mGoodLinkTargetCount = HuaweiWifiWatchdogStateMachine.TCP_MIN_TX_PKT;
            HuaweiWifiWatchdogStateMachine.this.logI("New link verifying target set, rssi=" + this.mGoodLinkTargetRssi + " count=" + this.mGoodLinkTargetCount);
        }

        public int findRssiTarget(int from, int to, double threshold) {
            from -= this.mRssiBase;
            to -= this.mRssiBase;
            int emptyCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            int d = from < to ? HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD : HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID;
            HuaweiWifiWatchdogStateMachine.this.logD(" findRssiTarget need rssi with lose rate < " + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(100.0d * threshold) + "%, idx from:" + from + "~ to:" + to);
            int i = from;
            while (i != to) {
                int rssi;
                if (i < 0 || i >= this.mEntriesSize || this.mEntries[i].mOtaVolume <= HuaweiWifiWatchdogStateMachine.PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE) {
                    emptyCount += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                    if (emptyCount >= HuaweiWifiWatchdogStateMachine.TCP_MIN_TX_PKT) {
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
                    emptyCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
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
            int d = from < to ? HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD : HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID;
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
            HuaweiWifiWatchdogStateMachine.this.logD("save bssid:" + this.mBssid + " history record, mEntriesSize = " + this.mEntriesSize);
            for (int i = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD; i < this.mEntriesSize; i += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
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
                HuaweiWifiWatchdogStateMachine.this.logD("restoreBssid:" + this.mBssid + " HistoreyRecord restore record, mEntriesSize = " + this.mEntriesSize);
                for (int i = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD; i < this.mEntriesSize; i += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
                    this.mEntries[i].mAvgRtt = apQualityRcd.getAvgRttFromRecord(i);
                    this.mEntries[i].mRttProduct = apQualityRcd.getRttProductFromRecord(i);
                    this.mEntries[i].mRttVolume = apQualityRcd.getRttVolumeFromRecord(i);
                    this.mEntries[i].mOtaLossRate = apQualityRcd.getLostRateFromRecord(i);
                    this.mEntries[i].mOtaVolume = apQualityRcd.getLostVolumeFromRecord(i);
                    this.mEntries[i].mOtaProduct = apQualityRcd.getLostProductFromRecord(i);
                }
                return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
            } else {
                HuaweiWifiWatchdogStateMachine.this.logE("loadApQualityRecord read DB failed for bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(this.mBssid));
                return false;
            }
        }

        private void initBQEHistoryRecord(WifiProApQualityRcd apQualityRcd) {
            if (loadApQualityRecord(apQualityRcd)) {
                HuaweiWifiWatchdogStateMachine.this.logD("succ load ApQualityRcd for bssid:" + this.mBssid + ".");
                HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_STORE_HISTORY_QUALITY, 1800000);
            } else {
                HuaweiWifiWatchdogStateMachine.this.logD("not ApQualityRcd for bssid:" + this.mBssid + ".");
            }
            this.mIsHistoryLoaded = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
        }

        public void afterConnectProcess() {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine.this.logD("afterConnectProcess enter for bssid:" + this.mBssid);
                initBQEHistoryRecord(this.mWifiProApQualityRcd);
            }
        }

        public void afterDisconnectProcess() {
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                HuaweiWifiWatchdogStateMachine.this.logD("afterDisconnectProcess enter for bssid:" + this.mBssid);
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
        public int mAvgRtt;
        public int mInadequateAvgRtt;
        public int mTotPkt;

        ProcessedTcpResult() {
            this.mAvgRtt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTotPkt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine.this.mIsNotStatic = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
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
            this.mTcpRtt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpRttPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpRttWhen = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpCongestion = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpCongWhen = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpQuality = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mUpdateTime = 0;
            this.mResultType = HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID;
            this.mTcpTxPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpRxPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpRetransPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
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
            this.mTcpRtt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpRttPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpRttWhen = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpCongestion = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpCongWhen = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpQuality = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mUpdateTime = 0;
            this.mResultType = HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID;
            this.mTcpTxPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpRxPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mTcpRetransPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mValueIsSet = false;
        }

        public boolean tcpResultUpdate(int[] resultArray, int len, int resultType) {
            if (!HuaweiWifiWatchdogStateMachine.this.mWpLinkMonitorRunning) {
                HuaweiWifiWatchdogStateMachine.this.logD(" not in link monitoring mode, ignore TCP result .");
                return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
            } else if (resultArray == null) {
                HuaweiWifiWatchdogStateMachine.this.logE(" tcpResultUpdate null error.");
                return false;
            } else {
                this.lenth = len;
                if (len == 0) {
                    HuaweiWifiWatchdogStateMachine.this.logE(" tcpResultUpdate 0 len error.");
                    return false;
                }
                this.mResultType = resultType;
                this.mTcpRtt = resultArray[HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD];
                if (len > HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
                    this.mTcpRttPkts = resultArray[HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD];
                }
                if (len > HuaweiWifiWatchdogStateMachine.VL_RSSI_SMOOTH_DIV_NUM) {
                    this.mTcpRttWhen = resultArray[HuaweiWifiWatchdogStateMachine.VL_RSSI_SMOOTH_DIV_NUM];
                }
                if (len > HuaweiWifiWatchdogStateMachine.TCP_MIN_TX_PKT) {
                    this.mTcpCongestion = resultArray[HuaweiWifiWatchdogStateMachine.TCP_MIN_TX_PKT];
                }
                if (len > HuaweiWifiWatchdogStateMachine.BASE_SCORE_OTA_BAD) {
                    this.mTcpCongWhen = resultArray[HuaweiWifiWatchdogStateMachine.BASE_SCORE_OTA_BAD];
                }
                if (len > HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_4_BETTER) {
                    this.mTcpQuality = resultArray[HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_4_BETTER];
                }
                if (len > 6) {
                    this.mTcpTxPkts = resultArray[6];
                }
                if (len > 7) {
                    this.mTcpRxPkts = resultArray[7];
                }
                if (len > HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_5_BEST) {
                    this.mTcpRetransPkts = resultArray[HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_5_BEST];
                }
                if (this.mResultType == HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
                    HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel = this.mTcpQuality;
                    HuaweiWifiWatchdogStateMachine.this.logI("TCP result ##### Quality=" + HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel);
                }
                this.mUpdateTime = SystemClock.elapsedRealtime();
                this.mValueIsSet = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
            }
        }

        public void dumpLog() {
            HuaweiWifiWatchdogStateMachine.this.logD("dumpTcpResult: len=" + this.lenth + ",rtt=" + this.mTcpRtt + ",rttPkt=" + this.mTcpRttPkts + ",rttWn=" + this.mTcpRttWhen + ",cgt=" + this.mTcpCongestion + ",cgtWn=" + this.mTcpCongWhen);
        }
    }

    private class VolumeWeightedEMA {
        public long mAvgRtt;
        public double mOtaLossRate;
        public double mOtaProduct;
        public double mOtaVolume;
        public long mRttProduct;
        public long mRttVolume;

        public VolumeWeightedEMA(double coefficient) {
            this.mOtaLossRate = HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_2_POOR;
            this.mOtaVolume = HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_2_POOR;
            this.mOtaProduct = HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_2_POOR;
            this.mAvgRtt = 0;
            this.mRttVolume = 0;
            this.mRttProduct = 0;
            HuaweiWifiWatchdogStateMachine.this.mIsNotStatic = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
        }

        public void updateLossRate(double newValue, int newVolume) {
            if (newVolume > 0) {
                this.mOtaProduct += newValue * ((double) newVolume);
                this.mOtaVolume = ((double) newVolume) + this.mOtaVolume;
                if (this.mOtaVolume != HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_2_POOR) {
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
            return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
        }
    }

    class WifiProLinkMonitoringState extends State {
        private static final int DEFAULT_NET_DISABLE_DETECT_COUNT = 2;
        private static final int HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT = 3;
        private static final int HOMEAP_LEVEL_THREE_NET_DISABLE_DETECT_COUNT = 5;
        private static final int HOMEAP_LEVEL_TWO_NET_DISABLE_DETECT_COUNT = 4;
        private int adjustRssiCounter;
        private int goodPeriodCounter;
        private int goodPeriodRxPkt;
        private int homeAPAddPeriodCount;
        private boolean isFirstEnterMontoringState;
        boolean isLastOTABad;
        boolean isLastRssiBad;
        boolean isLastTCPBad;
        private boolean isMaybePoorSend;
        private boolean isPoorLinkReported;
        private long lastRSSIUpdateTime;
        private float mGoodSpeedRate;
        private float mHomeApSwichRate;
        private boolean mIsHomeApSwitchRateReaded;
        private int mLMRssiWaitCount;
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
        private float mRssiRate;
        private float mSwitchScore;
        private boolean networkBadDetected;
        private float noHomeAPSwitchScore;

        WifiProLinkMonitoringState() {
            this.mLMRssiWaitCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.isLastRssiBad = false;
            this.isLastOTABad = false;
            this.isLastTCPBad = false;
            this.mSwitchScore = 0.0f;
            this.mGoodSpeedRate = HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
            this.mRssiRate = HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
            this.isMaybePoorSend = false;
            this.isPoorLinkReported = false;
            this.goodPeriodCounter = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.goodPeriodRxPkt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.lastRSSIUpdateTime = 0;
            this.noHomeAPSwitchScore = 0.0f;
            this.homeAPAddPeriodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.networkBadDetected = false;
            this.isFirstEnterMontoringState = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
            this.mIsHomeApSwitchRateReaded = false;
            this.mHomeApSwichRate = HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState enter. ");
            this.mRssiBadCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mLMRssiWaitCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.sendMessage(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, huaweiWifiWatchdogStateMachine3.mRssiFetchToken = huaweiWifiWatchdogStateMachine3.mRssiFetchToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD, HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD));
            HuaweiWifiWatchdogStateMachine.this.mWpLinkMonitorRunning = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
            this.mPktChkTxbad = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mPktChkTxgood = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mPktChkRxgood = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mPktChkCnt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mPktChkBadCnt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mLastDnsFailCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mNetworkDisableCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine.this.monitorNetworkQos(HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE);
            this.isLastRssiBad = false;
            this.isLastOTABad = false;
            this.isLastTCPBad = false;
            this.adjustRssiCounter = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine.this.mLastPeriodTcpChkResult = new TcpChkResult();
            resetPoorRssiTh();
            huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mRssiTHTimeoutToken = huaweiWifiWatchdogStateMachine.mRssiTHTimeoutToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mHighSpeedToken = huaweiWifiWatchdogStateMachine.mHighSpeedToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = HuaweiWifiWatchdogStateMachine.INVALID_RSSI;
            HuaweiWifiWatchdogStateMachine.this.logD("new conn, poor rssi th :" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH);
            this.mSwitchScore = 0.0f;
            this.noHomeAPSwitchScore = 0.0f;
            this.homeAPAddPeriodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.networkBadDetected = false;
            this.mRssiRate = HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
            ssidChangeDetection();
            this.isMaybePoorSend = false;
            this.isPoorLinkReported = false;
            this.goodPeriodCounter = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.goodPeriodRxPkt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod = 0;
            huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mRssiChangedToken = huaweiWifiWatchdogStateMachine.mRssiChangedToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            this.lastRSSIUpdateTime = 0;
            if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine != null) {
                HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine.resetSampleRtt();
            }
            this.mIsHomeApSwitchRateReaded = false;
            this.mHomeApSwichRate = HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
            this.isFirstEnterMontoringState = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
        }

        public void exit() {
            HuaweiWifiWatchdogStateMachine.this.logD(" WifiProLinkMonitoringState exit.");
            HuaweiWifiWatchdogStateMachine.this.monitorNetworkQos(false);
            HuaweiWifiWatchdogStateMachine.this.mWpLinkMonitorRunning = false;
            HuaweiWifiWatchdogStateMachine.this.mSwitchOutTime = SystemClock.elapsedRealtime();
        }

        private void resetPeriodState() {
            this.mPktChkCnt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mPktChkTxbad = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mPktChkTxgood = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mPktChkRxgood = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mRssiBadCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod = 0;
        }

        private void networkGoodDetect(int tcpRxPkt, int crssi, int avgrtt, boolean speedGood) {
            if (this.isPoorLinkReported) {
                boolean isRssiBetter = crssi - HuaweiWifiWatchdogStateMachine.this.mGoodDetectBaseRssi >= HuaweiWifiWatchdogStateMachine.MAX_RSSI_PKT_READ_WAIT_SECOND ? HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false;
                boolean isRttGood = (avgrtt <= 0 || avgrtt > HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_NETGOOD_MAX_RTT) ? false : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                if (isRssiBetter || isRttGood || speedGood) {
                    this.goodPeriodRxPkt += tcpRxPkt;
                    int i = this.goodPeriodCounter + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                    this.goodPeriodCounter = i;
                    if (i >= DEFAULT_NET_DISABLE_DETECT_COUNT && this.goodPeriodRxPkt >= HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
                        HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE, HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT);
                        HuaweiWifiWatchdogStateMachine.this.logI("link good reported, good base rssi:" + HuaweiWifiWatchdogStateMachine.this.mGoodDetectBaseRssi);
                        this.goodPeriodRxPkt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                        this.goodPeriodCounter = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
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
            this.homeAPAddPeriodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.noHomeAPSwitchScore = 0.0f;
        }

        private void resetPoorNetState() {
            tryReportHomeApChr();
            this.mPktChkBadCnt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            this.mSwitchScore = 0.0f;
            this.isMaybePoorSend = false;
            HuaweiWifiWatchdogStateMachine.this.logI(" pkt chk not bad, reset sc to 0.");
        }

        private boolean isNetSpeedOk(int avgRtt) {
            boolean isSpeedOk = false;
            if (HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod) {
                isSpeedOk = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                tryBackOffScore(HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod, avgRtt);
            }
            HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod = false;
            return isSpeedOk;
        }

        private boolean detectNetworkAvailable(int tcpTxPkts, int tcpRxPkts, int tcpRetransPkts, String currentBssid) {
            int currentDnsFailCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            String dnsFailCountStr = SystemProperties.get(HwSelfCureUtils.DNS_MONITOR_FLAG, "0");
            int detectCountRequirement = DEFAULT_NET_DISABLE_DETECT_COUNT;
            if (dnsFailCountStr == null) {
                HuaweiWifiWatchdogStateMachine.this.logE("detectNetworkAvailable null point error.");
                return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
            }
            try {
                currentDnsFailCount = Integer.parseInt(dnsFailCountStr);
                HuaweiWifiWatchdogStateMachine.this.logD("detectNetworkAvailable:hw.wifipro.dns_fail_count = " + currentDnsFailCount);
            } catch (NumberFormatException e) {
                HuaweiWifiWatchdogStateMachine.this.logE("detectNetworkAvailable  parseInt err:" + e);
            }
            if (tcpRxPkts != 0) {
                HuaweiWifiWatchdogStateMachine.this.logD("detectNetworkAvailable: tcpRxPkts != 0, reset");
                this.mNetworkDisableCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            } else if (tcpTxPkts > DEFAULT_NET_DISABLE_DETECT_COUNT) {
                HuaweiWifiWatchdogStateMachine.this.logD("detectNetworkAvailable: tcpTxPkts > 2");
                this.mNetworkDisableCount += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            } else if (currentDnsFailCount - this.mLastDnsFailCount > 0) {
                this.mNetworkDisableCount += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            }
            this.mLastDnsFailCount = currentDnsFailCount;
            if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager != null && this.mNetworkDisableCount >= DEFAULT_NET_DISABLE_DETECT_COUNT) {
                this.mHomeApSwichRate = HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.getHomeApSwitchRate(currentBssid);
                this.mIsHomeApSwitchRateReaded = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                detectCountRequirement = this.mHomeApSwichRate <= HuaweiWifiWatchdogStateMachine.STREAMING_ORIG_RATE ? HOMEAP_LEVEL_THREE_NET_DISABLE_DETECT_COUNT : this.mHomeApSwichRate <= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_GOOD_RTT_RATE ? HOMEAP_LEVEL_TWO_NET_DISABLE_DETECT_COUNT : this.mHomeApSwichRate <= WifiProHistoryRecordManager.HOME_AP_LEVEL_ONE_SWITCH_RATE ? HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT : DEFAULT_NET_DISABLE_DETECT_COUNT;
            }
            if (this.mNetworkDisableCount >= detectCountRequirement) {
                HuaweiWifiWatchdogStateMachine.this.logI("detectNetworkAvailable: mNetworkDisableCount = " + this.mNetworkDisableCount);
                this.mNetworkDisableCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                return false;
            }
            if (this.mNetworkDisableCount == DEFAULT_NET_DISABLE_DETECT_COUNT) {
                HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, -2);
                HuaweiWifiWatchdogStateMachine.this.logI("report maybe poor for detecting network disable 2 periods");
            }
            return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
        }

        public void resetPoorRssiTh() {
            HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH = HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD;
            HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD;
            HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH = HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD;
        }

        private void sendTHTimeOutDelayMsg(int delayTime) {
            if (HuaweiWifiWatchdogStateMachine.this.getHandler().hasMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT)) {
                HuaweiWifiWatchdogStateMachine.this.logD("remove old RSSI_TH_TIMEOUT msg.");
                HuaweiWifiWatchdogStateMachine.this.getHandler().removeMessages(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT);
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.sendMessageDelayed(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.EVENT_RSSI_TH_VALID_TIMEOUT, huaweiWifiWatchdogStateMachine3.mRssiTHTimeoutToken = huaweiWifiWatchdogStateMachine3.mRssiTHTimeoutToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD, HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD), (long) delayTime);
        }

        private void resetRssiTHValue(int newTHRssi) {
            if (newTHRssi >= HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD) {
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
            HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + HuaweiWifiWatchdogStateMachine.TCP_RESULT_TYPE_INVALID;
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH;
            }
        }

        private void updatePoorNetRssiTH(int currRssi) {
            if (currRssi != HuaweiWifiWatchdogStateMachine.INVALID_RSSI) {
                if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH < HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD) {
                    if (currRssi <= HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                        HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH;
                        resetRssiTHValue(currRssi);
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.mRssiTHValidTime = huaweiWifiWatchdogStateMachine.mRssiTHValidTime + HuaweiWifiWatchdogStateMachine.START_VL_RSSI_CHK_TIME;
                        if (HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime > HuaweiWifiWatchdogStateMachine.RSSI_TH_MAX_VALID_TIME) {
                            HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = HuaweiWifiWatchdogStateMachine.RSSI_TH_MAX_VALID_TIME;
                        }
                        sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                        HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED update th rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + ", rth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", nth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext + ", vtime: " + HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                    } else if (currRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext) {
                        HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = currRssi;
                        HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED update nth to rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
                    }
                } else if (currRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH) {
                    HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = HuaweiWifiWatchdogStateMachine.RSSI_TH_MIN_VALID_TIME;
                    resetRssiTHValue(currRssi);
                    HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD;
                    sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                    HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED TH rssi: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + ", rth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", nth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext + ", vtime: " + HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                }
            }
        }

        private void rssiTHValidTimeout() {
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext < HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD) {
                resetRssiTHValue(HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
                HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext = HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD;
                HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime = HuaweiWifiWatchdogStateMachine.RSSI_TH_MIN_VALID_TIME;
                sendTHTimeOutDelayMsg(HuaweiWifiWatchdogStateMachine.this.mRssiTHValidTime);
                HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED TH timeout, turn to next:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH);
                return;
            }
            resetPoorRssiTh();
            HuaweiWifiWatchdogStateMachine.this.logI("new HIGH SPEED TH timeout reset.");
        }

        private void checkRssiTHBackoff(int currRssi, double tcpRetransRate, int tcpTxPkts, int tcpRetransPkts, int tcpRtt) {
            boolean isTcpRetranBad = (tcpRetransRate < HuaweiWifiWatchdogStateMachine.RSSI_TH_BACKOFF_TCP_RETRAN_TH || tcpTxPkts <= HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT) ? (tcpTxPkts > HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT || tcpRetransPkts < HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT) ? false : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
            boolean isRttbad = (tcpRtt > HuaweiWifiWatchdogStateMachine.RSSI_TH_BACKOFF_RTT_TH || tcpRtt == 0) ? HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false;
            if (HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH <= currRssi && isTcpRetranBad && isRttbad) {
                resetRssiTHValue(HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTH + HOMEAP_LEVEL_THREE_NET_DISABLE_DETECT_COUNT);
                HuaweiWifiWatchdogStateMachine.this.logI("after backoff rssi th: " + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH + ", next TH:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiTHNext);
            }
        }

        private float getRssiSWRate(int rssi) {
            int len = HuaweiWifiWatchdogStateMachine.RSSI_RATE_TBL.length;
            float retRate = HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
            if (rssi < HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD) {
                return HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
            }
            for (int i = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD; i < len; i += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
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
            } else if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID() == null || HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID().equals(HuaweiWifiWatchdogStateMachine.this.mCurrSSID)) {
                spdGoodParameterAge(HuaweiWifiWatchdogStateMachine.this.mSwitchOutTime);
            } else {
                HuaweiWifiWatchdogStateMachine.this.logI("SSID change to:" + HuaweiWifiWatchdogStateMachine.this.mWifiInfo.getSSID() + ", old SSID: " + HuaweiWifiWatchdogStateMachine.this.mCurrSSID + ", reset SGC.");
                resetSpdGoodCounter();
            }
        }

        private void resetSpdGoodCounter() {
            this.mGoodSpeedRate = HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
            HuaweiWifiWatchdogStateMachine.this.mPeriodGoodSpdScore = 0.0f;
            HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = 0.0f;
            HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
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
                HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            }
            HuaweiWifiWatchdogStateMachine.this.logD("SSID: " + HuaweiWifiWatchdogStateMachine.this.mCurrSSID + "not changed, not reset SGC. reduceCount:" + reduceCount + ", SGC:" + HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount);
        }

        private float getSpdGoodSWRate(float count) {
            int len = HuaweiWifiWatchdogStateMachine.GOOD_SPD_COUNT_RATE_TBL.length;
            for (int i = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD; i < len; i += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
                if (count > HuaweiWifiWatchdogStateMachine.GOOD_SPD_COUNT_RATE_TBL[i].GOOD_COUNT) {
                    return HuaweiWifiWatchdogStateMachine.GOOD_SPD_COUNT_RATE_TBL[i].SW_RATE;
                }
            }
            return HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
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
                HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            } else if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount > 0.0f) {
                huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                huaweiWifiWatchdogStateMachine.mSpeedNotGoodCount = huaweiWifiWatchdogStateMachine.mSpeedNotGoodCount + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                if (((float) HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount) >= HuaweiWifiWatchdogStateMachine.BACKOFF_NOT_SPD_GOOD_PERIOD_COUNT) {
                    huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mSpeedGoodCount = huaweiWifiWatchdogStateMachine.mSpeedGoodCount - HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
                    if (HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount < 0.0f) {
                        HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount = 0.0f;
                    }
                    HuaweiWifiWatchdogStateMachine.this.mSpeedNotGoodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
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
            if (HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE != HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate) {
                retScore *= HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate;
            }
            if (HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE != this.mGoodSpeedRate && this.mPktChkBadCnt == 0) {
                retScore *= this.mGoodSpeedRate;
            }
            this.noHomeAPSwitchScore += retScore;
            if (HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE != homeApRate && rssi >= HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD) {
                retScore *= homeApRate;
            }
            HuaweiWifiWatchdogStateMachine.this.logI("Get rate: Rssi" + rssi + ", RssiRate:" + this.mRssiRate + ", HighDataFlow:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate + ", History: " + this.mGoodSpeedRate + ", add score: " + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr((double) retScore) + ", Homerate: " + homeApRate + ", mPktChkBadCnt:" + this.mPktChkBadCnt);
            return retScore;
        }

        private void updateHighDataFlowRate() {
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario != HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD || HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection <= 0) {
                if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == DEFAULT_NET_DISABLE_DETECT_COUNT && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 0) {
                    if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > HuaweiWifiWatchdogStateMachine.STREAMING_PROHIBIT_SWITCH_PROTECTION) {
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 0.0f;
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = (((float) (50 - HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection)) * HuaweiWifiWatchdogStateMachine.STREAMING_STEP_RATE) + HuaweiWifiWatchdogStateMachine.STREAMING_ORIG_RATE;
                    }
                }
            } else if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > HuaweiWifiWatchdogStateMachine.DOWNLOAD_PROHIBIT_SWITCH_PROTECTION) {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = 0.0f;
            } else {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = (((float) (20 - HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection)) * HuaweiWifiWatchdogStateMachine.DOWNLOAD_STEP_RATE) + HuaweiWifiWatchdogStateMachine.DOWNLOAD_ORIG_RATE;
            }
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate > HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE || HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate < 0.0f) {
                HuaweiWifiWatchdogStateMachine.this.logI("wrong rate! mHighDataFlowRate  = %d" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate);
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
            }
            HuaweiWifiWatchdogStateMachine.this.logI("current mHighDataFlowRate = " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate);
        }

        private void handleHighDataFlow() {
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection > 0) {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection - 4;
                if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection < 0) {
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                }
                if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection == 0) {
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate = HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE;
                    HuaweiWifiWatchdogStateMachine.this.logI("mHighDataFlowProtection = 0, reset to HIGH_DATA_FLOW_NONE");
                }
            }
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowPeriodCounter == 0) {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes = TrafficStats.getRxBytes(HuaweiWifiWatchdogStateMachine.WLAN_IFACE);
            }
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.mHighDataFlowPeriodCounter = huaweiWifiWatchdogStateMachine.mHighDataFlowPeriodCounter + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowPeriodCounter % HOMEAP_LEVEL_TWO_NET_DISABLE_DETECT_COUNT == 0) {
                long currentHighDataFlowRxBytes = TrafficStats.getRxBytes(HuaweiWifiWatchdogStateMachine.WLAN_IFACE);
                long highDataFlowRxBytes = currentHighDataFlowRxBytes - HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes;
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowLastRxBytes = currentHighDataFlowRxBytes;
                int lastScenario = HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario;
                if (highDataFlowRxBytes >= 3145728) {
                    switch (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario) {
                        case HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD /*0*/:
                            if (highDataFlowRxBytes >= 5242880) {
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.LONGEST_DOWNLOAD_PROTECTION;
                            } else if (HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow) {
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.LONGEST_DOWNLOAD_PROTECTION;
                            }
                            HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                            break;
                        case HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD /*1*/:
                            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter < DEFAULT_NET_DISABLE_DETECT_COUNT) {
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.LONGEST_DOWNLOAD_PROTECTION;
                                break;
                            }
                            HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = DEFAULT_NET_DISABLE_DETECT_COUNT;
                            HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.LONGEST_STREAMING_PROTECTION;
                            break;
                        case DEFAULT_NET_DISABLE_DETECT_COUNT /*2*/:
                            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter >= DEFAULT_NET_DISABLE_DETECT_COUNT) {
                                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection = HuaweiWifiWatchdogStateMachine.LONGEST_STREAMING_PROTECTION;
                                break;
                            }
                            break;
                        default:
                            HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                            HuaweiWifiWatchdogStateMachine.this.logI("wrong high data scenario, reset to HIGH_DATA_FLOW_NONE ");
                            break;
                    }
                    HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                } else {
                    HuaweiWifiWatchdogStateMachine.this.mLastHighDataFlow = false;
                    huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mHighDataFlowNotDetectCounter = huaweiWifiWatchdogStateMachine.mHighDataFlowNotDetectCounter + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                }
                HuaweiWifiWatchdogStateMachine.this.logD("high data flow: protection_counter = " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection + ",  not_detect_counter = " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowNotDetectCounter);
                HuaweiWifiWatchdogStateMachine.this.logD("high data flow scenario: " + lastScenario + " --> " + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario + " rx bytes =" + (highDataFlowRxBytes / 1024) + "KB");
            }
            updateHighDataFlowRate();
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == DEFAULT_NET_DISABLE_DETECT_COUNT && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate >= HuaweiWifiWatchdogStateMachine.DOWNLOAD_ORIG_RATE) {
                HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario = HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            }
        }

        private void debugToast(int mrssi) {
            String scenario;
            if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
                scenario = "DOWN";
            } else if (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario == DEFAULT_NET_DISABLE_DETECT_COUNT) {
                scenario = "STREAM";
            } else {
                scenario = "NONE";
            }
            Toast.makeText(HuaweiWifiWatchdogStateMachine.this.mContext, "RSSI:" + mrssi + " TotalScore:" + this.mSwitchScore + "  BadCount:" + this.mPktChkBadCnt + "\n" + scenario + ":" + " Protection:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowProtection + " Rate:" + HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate, HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD).show();
        }

        private void updateQoeLevel(int level) {
            HuaweiWifiWatchdogStateMachine.this.logD("Enter updateQoeLevel level = " + level);
            if (level != 0) {
                if (level == HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel) {
                    HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                    HuaweiWifiWatchdogStateMachine.this.logD("updateQoeLevel current level equal to mLastDetectLevel, return");
                    return;
                }
                if (level == DEFAULT_NET_DISABLE_DETECT_COUNT) {
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = level;
                    HuaweiWifiWatchdogStateMachine.this.mLastDetectLevel = level;
                } else if (Math.abs(level - HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel) == DEFAULT_NET_DISABLE_DETECT_COUNT) {
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel = DEFAULT_NET_DISABLE_DETECT_COUNT;
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
                HuaweiWifiWatchdogStateMachine.this.postEvent(HuaweiWifiWatchdogStateMachine.QUERY_TCP_INFO_RETRY_CNT, HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD, HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel);
            }
        }

        private void computeQosLevel(int tcp_rtt, int tcp_rtt_pkts, int rssi, boolean isLossRateBad) {
            int speedLevel;
            int rttLevel;
            int qosLevel;
            if (HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod > 358400) {
                speedLevel = HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT;
            } else if (HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod > 20480) {
                speedLevel = DEFAULT_NET_DISABLE_DETECT_COUNT;
            } else {
                speedLevel = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            }
            if (tcp_rtt_pkts < HuaweiWifiWatchdogStateMachine.this.QOE_LEVEL_RTT_MIN_PKT || tcp_rtt == 0) {
                rttLevel = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            } else if (tcp_rtt <= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_NETGOOD_MAX_RTT) {
                rttLevel = HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT;
            } else if (tcp_rtt <= WifiproBqeUtils.BQE_NOT_GOOD_RTT) {
                rttLevel = DEFAULT_NET_DISABLE_DETECT_COUNT;
            } else {
                rttLevel = HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            }
            if (rttLevel >= speedLevel) {
                qosLevel = rttLevel;
            } else if (speedLevel > HuaweiWifiWatchdogStateMachine.this.mCurrentBqeLevel) {
                qosLevel = speedLevel;
            } else {
                qosLevel = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            }
            if (rssi < HuaweiWifiWatchdogStateMachine.this.RSSI_TH_FOR_BAD_QOE_LEVEL && (qosLevel > HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD || qosLevel == 0)) {
                qosLevel = HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
            } else if (rssi < HuaweiWifiWatchdogStateMachine.this.RSSI_TH_FOR_NOT_BAD_QOE_LEVEL && (qosLevel > DEFAULT_NET_DISABLE_DETECT_COUNT || qosLevel == 0)) {
                qosLevel = DEFAULT_NET_DISABLE_DETECT_COUNT;
                if (isLossRateBad) {
                    qosLevel = HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                }
            }
            HuaweiWifiWatchdogStateMachine.this.logD("bqeLevel = " + qosLevel + " rtt=" + tcp_rtt + ",rtt_pkts=" + tcp_rtt_pkts + " speed=" + HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod + " rssi=" + rssi);
            if (qosLevel != 0) {
                updateQoeLevel(qosLevel);
            }
        }

        private void tryBackOffScore(long periodMaxSpeed, int avgRtt) {
            if (this.mSwitchScore > 0.0f) {
                boolean isScoreBackoffSpeedGood = periodMaxSpeed > 51200 ? HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false;
                boolean isScoreBackoffRTTGood = (avgRtt <= 0 || avgRtt > HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_NETGOOD_MAX_RTT) ? false : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                HuaweiWifiWatchdogStateMachine.this.logD("score backoff: spdG=" + isScoreBackoffSpeedGood + ", rttG=" + isScoreBackoffRTTGood + ", oldSco=" + this.mSwitchScore);
                if (isScoreBackoffSpeedGood || this.mSwitchScore < HuaweiWifiWatchdogStateMachine.HIGH_DATA_FLOW_DEFAULT_RATE) {
                    resetPoorNetState();
                    return;
                } else if (isScoreBackoffRTTGood) {
                    this.mSwitchScore *= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_GOOD_RTT_RATE;
                    this.noHomeAPSwitchScore *= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_GOOD_RTT_RATE;
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

        public boolean processMessage(Message msg) {
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine;
            switch (msg.what) {
                case HuaweiWifiWatchdogStateMachine.EVENT_BSSID_CHANGE /*135175*/:
                    HuaweiWifiWatchdogStateMachine.this.transitionTo(HuaweiWifiWatchdogStateMachine.this.mWifiProLinkMonitoringState);
                    break;
                case HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH /*135179*/:
                    if (msg.arg1 != HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken) {
                        HuaweiWifiWatchdogStateMachine.this.logD("MonitoringState msg arg1:" + msg.arg1 + " != token:" + HuaweiWifiWatchdogStateMachine.this.mRssiFetchToken + ", ignore this command.");
                        break;
                    }
                    if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid == null) {
                        HuaweiWifiWatchdogStateMachine.this.logD("MonitoringState WIFI not connected, skip fetch RSSI.");
                    } else {
                        if (!HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk) {
                            HuaweiWifiWatchdogStateMachine.this.logI("WP Link Monitor State first check bssid:" + HuaweiWifiWatchdogStateMachine.this.partDisplayBssid(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid) + ", call newLinkDetected");
                            HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.newLinkDetected();
                            HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                        }
                        if (this.mLMRssiWaitCount <= 0) {
                            HuaweiWifiWatchdogStateMachine.this.mWsmChannel.sendMessage(151572);
                            this.mLMRssiWaitCount = HuaweiWifiWatchdogStateMachine.MAX_RSSI_PKT_READ_WAIT_SECOND;
                        } else {
                            HuaweiWifiWatchdogStateMachine.this.logI("wait rssi request respond, not send new request.");
                            this.mLMRssiWaitCount = (int) (((long) this.mLMRssiWaitCount) - 8);
                        }
                    }
                    huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.sendMessageDelayed(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, huaweiWifiWatchdogStateMachine3.mRssiFetchToken = huaweiWifiWatchdogStateMachine3.mRssiFetchToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD, HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD), HuaweiWifiWatchdogStateMachine.LINK_SAMPLING_INTERVAL_MS);
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
                    if (msg.arg2 != HuaweiWifiWatchdogStateMachine.this.mRssiChangedToken) {
                        HuaweiWifiWatchdogStateMachine.this.logD("ignore old rssi change message.");
                        break;
                    }
                    long nowTime = SystemClock.elapsedRealtime();
                    int newRssiVal = msg.arg1;
                    if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi || nowTime - this.lastRSSIUpdateTime >= HuaweiWifiWatchdogStateMachine.IGNORE_RSSI_BROADCAST_TIME_TH) {
                        HuaweiWifiWatchdogStateMachine.this.mCurrRssi = newRssiVal;
                    } else {
                        HuaweiWifiWatchdogStateMachine.this.mCurrRssi = ((HuaweiWifiWatchdogStateMachine.this.mCurrRssi * HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) + newRssiVal) / DEFAULT_NET_DISABLE_DETECT_COUNT;
                    }
                    this.lastRSSIUpdateTime = nowTime;
                    HuaweiWifiWatchdogStateMachine.this.logD("rssi change nRssi =" + newRssiVal + ", sRssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                    if (HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable && HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine != null) {
                        HuaweiWifiWatchdogStateMachine.this.mHwDualBandQualityEngine.querySampleRtt();
                        break;
                    }
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
                    this.mLMRssiWaitCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                    if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null && HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null && msg.obj != null) {
                        RssiPacketCountInfo info = msg.obj;
                        HuaweiWifiWatchdogStateMachine.this.sendCurrentAPRssi(info.rssi);
                        int rssi = info.rssi;
                        huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.mRssiChangedToken = huaweiWifiWatchdogStateMachine.mRssiChangedToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                        long now = SystemClock.elapsedRealtime();
                        if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi || now - this.lastRSSIUpdateTime >= HuaweiWifiWatchdogStateMachine.IGNORE_RSSI_BROADCAST_TIME_TH) {
                            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = rssi;
                        } else {
                            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = ((HuaweiWifiWatchdogStateMachine.this.mCurrRssi * HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) + rssi) / DEFAULT_NET_DISABLE_DETECT_COUNT;
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
                                this.mRssiBadCount += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                            }
                            this.mPktChkTxbad += dbad;
                            this.mPktChkTxgood += dgood;
                            this.mPktChkRxgood += drxgood;
                            this.mPktChkCnt += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                            if (this.mPktChkCnt >= HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
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
                                    int tcpTxPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                                    int tcpRxPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                                    int tcpRetransPkts = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
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
                                            lastPeriodTcpResultValid = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
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
                                        double tcpRetransRate = HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_2_POOR;
                                        if (tcpTxPkts > 0) {
                                            tcpRetransRate = ((double) tcpRetransPkts) / ((double) tcpTxPkts);
                                        }
                                        HuaweiWifiWatchdogStateMachine.this.logI("PTcp RTT:" + processedTcpResult.mAvgRtt + ", rtt pkt=" + processedTcpResult.mTotPkt + ", tcp_rx=" + tcpRxPkts + ", tcp_tx=" + tcpTxPkts + ", tcp_reTran=" + tcpRetransPkts + ", rtRate=" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr(tcpRetransRate));
                                        if (HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount > 0 && HuaweiWifiWatchdogStateMachine.this.mIsDualbandEnable) {
                                            HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.addHistoryHSCount(HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount);
                                            HuaweiWifiWatchdogStateMachine.this.mHistoryHSCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                                        }
                                        boolean isRttGood = false;
                                        if (processedTcpResult.mAvgRtt < HuaweiWifiWatchdogStateMachine.TCP_PERIOD_CHK_RTT_GOOD_TH && processedTcpResult.mTotPkt > DEFAULT_NET_DISABLE_DETECT_COUNT && lossRate < HuaweiWifiWatchdogStateMachine.PKT_CHK_POOR_LINK_MIN_LOSE_RATE && pTot > HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_5_BEST && tcpRetransRate < HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_1_VERY_POOR && tcpTxPkts > DEFAULT_NET_DISABLE_DETECT_COUNT) {
                                            HuaweiWifiWatchdogStateMachine.this.logD("isRttGood is good, AvgRtt = " + processedTcpResult.mAvgRtt + ", TotPkt = " + processedTcpResult.mTotPkt);
                                            isRttGood = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                        }
                                        boolean isTCPBad = !isRttGood ? HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel != HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD ? HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel == DEFAULT_NET_DISABLE_DETECT_COUNT ? HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false;
                                        boolean isRssiBad = false;
                                        if (this.mRssiBadCount >= HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
                                            isRssiBad = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                        }
                                        boolean isTcpRetranBad = (tcpRetransRate < HuaweiWifiWatchdogStateMachine.PKT_CHK_LOSE_RATE_LEVEL_0_NOT_AVAILABLE || tcpTxPkts <= HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT) ? (tcpTxPkts > HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT || tcpRetransPkts < HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT) ? false : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                        boolean isRttBadForLowRssi = (processedTcpResult.mAvgRtt > HuaweiWifiWatchdogStateMachine.RSSI_TH_BACKOFF_RTT_TH || processedTcpResult.mAvgRtt == 0) ? processedTcpResult.mInadequateAvgRtt <= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_NETGOOD_MAX_RTT ? processedTcpResult.mInadequateAvgRtt == 0 ? HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false;
                                        boolean lossRateVeryBad = (lossRate < HuaweiWifiWatchdogStateMachine.RSSI_TH_BACKOFF_TCP_RETRAN_TH || pTot < HuaweiWifiWatchdogStateMachine.STREAMING_PROHIBIT_SWITCH_PROTECTION) ? false : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                        boolean z = HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH ? !isRttBadForLowRssi ? lossRateVeryBad : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false;
                                        boolean isWlanLossRateBad = (lossRate <= HuaweiWifiWatchdogStateMachine.PKT_CHK_POOR_LINK_MIN_LOSE_RATE || pTot <= HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_5_BEST) ? false : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                        if (!z && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.this.LOW_RSSI_TH && isWlanLossRateBad) {
                                            z = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                        }
                                        boolean isRttBadForCongestion = (processedTcpResult.mAvgRtt > HuaweiWifiWatchdogStateMachine.PKT_CHK_RTT_BAD_LEVEL_2_POOR || processedTcpResult.mAvgRtt == 0) ? processedTcpResult.mInadequateAvgRtt <= HuaweiWifiWatchdogStateMachine.SCORE_BACK_OFF_NETGOOD_MAX_RTT ? processedTcpResult.mInadequateAvgRtt == 0 ? HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false;
                                        boolean z2 = (isWlanLossRateBad && isRttBadForCongestion) ? HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : isWlanLossRateBad ? isTcpRetranBad : false;
                                        boolean isVeryBadRTT = false;
                                        if (processedTcpResult.mAvgRtt > HuaweiWifiWatchdogStateMachine.VERY_BAD_RTT_TH_FOR_TCP_BAD && processedTcpResult.mTotPkt < HuaweiWifiWatchdogStateMachine.STREAMING_PROHIBIT_SWITCH_PROTECTION) {
                                            isVeryBadRTT = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                        }
                                        boolean isOTABad = false;
                                        if (z || r7) {
                                            if (isRttGood) {
                                                z = false;
                                                z2 = false;
                                                HuaweiWifiWatchdogStateMachine.this.logD("rtt good, igore bad result.");
                                            } else {
                                                isOTABad = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                            }
                                        }
                                        if (isRssiBad) {
                                            HuaweiWifiWatchdogStateMachine.this.logD("rssi bad, mrssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrRssi + " < TargetRssi=" + HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi);
                                            if (isTCPBad || isOTABad || this.isLastTCPBad || this.isLastOTABad || tcpTxPkts <= HOMEAP_LEVEL_ONE_NET_DISABLE_DETECT_COUNT || tcpRxPkts <= 0) {
                                                this.adjustRssiCounter = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                                            } else {
                                                this.adjustRssiCounter += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                                                if (this.adjustRssiCounter >= HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_5_BEST) {
                                                    HuaweiWifiWatchdogStateMachine.this.adjustTargetPoorRssi(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                                                    this.adjustRssiCounter = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                                                }
                                            }
                                        } else {
                                            HuaweiWifiWatchdogStateMachine.this.logD("mRssiBadCount =" + this.mRssiBadCount + ", RSSI not bad.");
                                        }
                                        if (!isNetSpeedOk(processedTcpResult.mAvgRtt)) {
                                            if (lastPeriodTcpResultValid) {
                                                if (!detectNetworkAvailable(tcpTxPkts, tcpRxPkts, tcpRetransPkts, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid)) {
                                                    if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi >= HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD) {
                                                        HuaweiWifiWatchdogStateMachine.this.logI("Inet check network is not ok, triger active check.");
                                                        HuaweiWifiWatchdogStateMachine.this.sendResultMsgToQM(WifiproUtils.REQUEST_WIFI_INET_CHECK);
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                                                    } else {
                                                        HuaweiWifiWatchdogStateMachine.this.logI("Inet check bad and week rssi, triger active check.");
                                                        HuaweiWifiWatchdogStateMachine.this.sendResultMsgToQM(WifiproUtils.REQUEST_POOR_RSSI_INET_CHECK);
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                    }
                                                    HuaweiWifiWatchdogStateMachine.this.mGoodDetectBaseRssi = HuaweiWifiWatchdogStateMachine.this.mCurrRssi;
                                                    this.isPoorLinkReported = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                                    this.goodPeriodCounter = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                                                }
                                            } else {
                                                HuaweiWifiWatchdogStateMachine.this.logD("Inet check network, not get tcp data, skip.");
                                            }
                                            if (tcpTxPkts <= DEFAULT_NET_DISABLE_DETECT_COUNT && tcpRxPkts <= DEFAULT_NET_DISABLE_DETECT_COUNT) {
                                                HuaweiWifiWatchdogStateMachine.this.logD("did not have any network action");
                                                break;
                                            }
                                            HuaweiWifiWatchdogStateMachine.this.logI("rs ota tcp lr rttvb bad: " + z + ", " + z2 + ", " + isTCPBad + ", " + isRssiBad + ", " + isVeryBadRTT + "; rsth:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH);
                                            HuaweiWifiWatchdogStateMachine.this.logD("GSC:" + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr((double) HuaweiWifiWatchdogStateMachine.this.mSpeedGoodCount) + ", GSR:" + this.mGoodSpeedRate + ", RSR:" + this.mRssiRate + ", RTH:" + HuaweiWifiWatchdogStateMachine.this.mPoorNetRssiRealTH);
                                            if (isOTABad || isTCPBad || isVeryBadRTT) {
                                                float retScore = getCurrPeriodScore(HuaweiWifiWatchdogStateMachine.this.mCurrRssi, z, z2, isTCPBad, isVeryBadRTT, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid);
                                                this.mSwitchScore += retScore;
                                                HuaweiWifiWatchdogStateMachine.this.logI("Add Score: " + HuaweiWifiWatchdogStateMachine.this.formatFloatToStr((double) retScore) + "Total Score:" + this.mSwitchScore + ", notHomeAp:" + this.noHomeAPSwitchScore);
                                                this.mPktChkBadCnt += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                                                updateQoeLevel(HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD);
                                                if (HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager != null && HuaweiWifiWatchdogStateMachine.this.mWifiProHistoryRecordManager.getIsHomeAP(HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mBssid)) {
                                                    this.networkBadDetected = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                                }
                                                if (this.noHomeAPSwitchScore > HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE && this.mSwitchScore <= HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE) {
                                                    HuaweiWifiWatchdogStateMachine.this.logD("homeAPAddPeriodCount add 1");
                                                    this.homeAPAddPeriodCount += HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                                                }
                                                if (this.mSwitchScore > HuaweiWifiWatchdogStateMachine.SWITCH_OUT_SCORE || this.homeAPAddPeriodCount > HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD) {
                                                    int currentWiFiLevel;
                                                    HuaweiWifiWatchdogStateMachine.this.logI(" pkt chk bad, ### mPktChkBadCnt=" + this.mPktChkBadCnt);
                                                    if (isRssiBad ? this.isLastRssiBad : false) {
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                                                        currentWiFiLevel = HuaweiWifiWatchdogStateMachine.this.wpPoorLinkLevelCalcByRssi(lossRate, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mPoorLinkTargetRssi, processedTcpResult.mAvgRtt);
                                                    } else {
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason = HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
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
                                                        if (pTot > HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_5_BEST) {
                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mOTA_PacketDropRate = (short) ((int) (1000.0d * lossRate));
                                                        }
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mRttAvg = (short) processedTcpResult.mAvgRtt;
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpInSegs = (short) tcpRxPkts;
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpOutSegs = (short) tcpTxPkts;
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mTcpRetransSegs = (short) tcpRetransPkts;
                                                        synchronized (HuaweiWifiWatchdogStateMachine.this.mTraffic) {
                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mWIFI_NetSpeed = (short) ((int) (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed / 1024));
                                                            break;
                                                        }
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mIPQLevel = (short) HuaweiWifiWatchdogStateMachine.this.mTcpReportLevel;
                                                        if (HuaweiWifiWatchdogStateMachine.this.mCurrSSID != null) {
                                                            HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mRO_APSsid = HuaweiWifiWatchdogStateMachine.this.mCurrSSID;
                                                        }
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mHistoryQuilityRO_Rate = (short) ((int) (this.mGoodSpeedRate * 1000.0f));
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mHighDataRateRO_Rate = (short) ((int) (HuaweiWifiWatchdogStateMachine.this.mHighDataFlowRate * 1000.0f));
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord.mCreditScoreRO_Rate = (short) 1000;
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.setBQERoveOutReason(z, z2, isTCPBad, isVeryBadRTT, HuaweiWifiWatchdogStateMachine.this.mWifiProRoveOutParaRecord);
                                                    }
                                                    HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, currentWiFiLevel);
                                                    this.isPoorLinkReported = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                                    tryReportHomeApChr();
                                                    this.mPktChkBadCnt = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                                                    this.mSwitchScore = 0.0f;
                                                    this.isMaybePoorSend = false;
                                                } else {
                                                    HuaweiWifiWatchdogStateMachine.this.logI(" pkt chk bad, not chk now, ### mPktChkBadCnt=" + this.mPktChkBadCnt);
                                                    if (!this.isMaybePoorSend && ((this.mSwitchScore > HuaweiWifiWatchdogStateMachine.MAY_BE_POOR_TH || z) && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.MAY_BE_POOR_RSSI_TH)) {
                                                        HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(false, -2);
                                                        HuaweiWifiWatchdogStateMachine.this.logI("report maybe poor nw. sc:" + this.mSwitchScore);
                                                        this.isMaybePoorSend = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                                                    }
                                                    if (this.mPktChkBadCnt == DEFAULT_NET_DISABLE_DETECT_COUNT && HuaweiWifiWatchdogStateMachine.this.mHighDataFlowScenario != 0) {
                                                        HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.increaseHighDataRateStopROC();
                                                    }
                                                }
                                                if (this.isPoorLinkReported) {
                                                    this.goodPeriodCounter = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
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
                                            networkGoodDetect(HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD, HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE);
                                            computeQosLevel(processedTcpResult.mAvgRtt, processedTcpResult.mTotPkt, HuaweiWifiWatchdogStateMachine.this.mCurrRssi, isWlanLossRateBad);
                                            HuaweiWifiWatchdogStateMachine.this.logD("### mPktChkBadCnt=" + this.mPktChkBadCnt);
                                            resetPeriodState();
                                            if (HuaweiWifiWatchdogStateMachine.DDBG_TOAST_DISPLAY) {
                                                debugToast(HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                                            }
                                            this.mNetworkDisableCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                                            break;
                                        }
                                    }
                                    this.isFirstEnterMontoringState = false;
                                    break;
                                }
                                HuaweiWifiWatchdogStateMachine.this.logI("not any pkt, ignore this period chk result.");
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
            return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
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
            return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
        }
    }

    class WifiProVerifyingLinkState extends State {
        private boolean mIsOtaPoorTimeOut;
        private boolean mIsReportWiFiGoodAllow;
        private int mRssiGoodCount;
        private int mVLRssiWaitCount;

        WifiProVerifyingLinkState() {
            this.mVLRssiWaitCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logI("WifiProVerifyingLinkState enter. PoorReason = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason + " , PoorRssi = " + HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi);
            this.mRssiGoodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi;
            this.mIsOtaPoorTimeOut = false;
            this.mIsReportWiFiGoodAllow = false;
            this.mVLRssiWaitCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
            HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
            huaweiWifiWatchdogStateMachine.sendMessage(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, huaweiWifiWatchdogStateMachine3.mRssiFetchToken = huaweiWifiWatchdogStateMachine3.mRssiFetchToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD, HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD));
            HuaweiWifiWatchdogStateMachine.this.sendMessageDelayed(HuaweiWifiWatchdogStateMachine.EVENT_AVOID_TO_WIFI_DELAY_MSG, 30000);
            if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null) {
                if (HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD == HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason) {
                    HuaweiWifiWatchdogStateMachine.this.logI("start delay verify.");
                    HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.otaTcpPoorLinkDetected();
                    if (HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime < HuaweiWifiWatchdogStateMachine.TIME_ELAP_2_MIN || HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime > HuaweiWifiWatchdogStateMachine.MAX_RESTORE_WIFI_TIME) {
                        HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mRestoreDelayTime = HuaweiWifiWatchdogStateMachine.TIME_ELAP_2_MIN;
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
            HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi = HuaweiWifiWatchdogStateMachine.BSSID_STAT_RANGE_LOW_DBM;
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
                            HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mIsNotFirstChk = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                        }
                        if (this.mVLRssiWaitCount <= 0) {
                            HuaweiWifiWatchdogStateMachine.this.mWsmChannel.sendMessage(151572);
                            this.mVLRssiWaitCount = HuaweiWifiWatchdogStateMachine.MAX_RSSI_PKT_READ_WAIT_SECOND;
                        } else {
                            HuaweiWifiWatchdogStateMachine.this.logI("vl wait rssi request respond, not send new request.");
                            this.mVLRssiWaitCount = (int) (((long) this.mVLRssiWaitCount) - 2);
                        }
                    }
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.sendMessageDelayed(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.CMD_RSSI_FETCH, huaweiWifiWatchdogStateMachine3.mRssiFetchToken = huaweiWifiWatchdogStateMachine3.mRssiFetchToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD, HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD), HuaweiWifiWatchdogStateMachine.ROVE_OUT_LINK_SAMPLING_INTERVAL_MS);
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
                    this.mIsOtaPoorTimeOut = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                    break;
                case HuaweiWifiWatchdogStateMachine.EVENT_AVOID_TO_WIFI_DELAY_MSG /*136169*/:
                    HuaweiWifiWatchdogStateMachine.this.logI("VL ReportWiFiGoodAllow = true.");
                    this.mIsReportWiFiGoodAllow = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                    break;
                case 151573:
                    this.mVLRssiWaitCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                    if (HuaweiWifiWatchdogStateMachine.this.mWifiInfo != null && HuaweiWifiWatchdogStateMachine.this.mCurrentBssid != null && msg.obj != null) {
                        RssiPacketCountInfo info = msg.obj;
                        HuaweiWifiWatchdogStateMachine.this.sendCurrentAPRssi(info.rssi);
                        if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI == HuaweiWifiWatchdogStateMachine.this.mCurrRssi) {
                            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = info.rssi;
                        } else {
                            HuaweiWifiWatchdogStateMachine.this.mCurrRssi = (HuaweiWifiWatchdogStateMachine.this.mCurrRssi + info.rssi) / HuaweiWifiWatchdogStateMachine.VL_RSSI_SMOOTH_DIV_NUM;
                        }
                        HuaweiWifiWatchdogStateMachine.this.logI("VL RSSI=" + HuaweiWifiWatchdogStateMachine.this.mCurrRssi);
                        HuaweiWifiWatchdogStateMachine.this.countDownGoodRssiByTime();
                        if (HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD != HuaweiWifiWatchdogStateMachine.this.mWifiPoorReason) {
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
            return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
        }

        private void tryRecoverWifiByRssi(int current_rssi) {
            if (this.mIsReportWiFiGoodAllow) {
                if (current_rssi >= HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetRssi) {
                    int i = this.mRssiGoodCount + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                    this.mRssiGoodCount = i;
                    if (i >= HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetCount) {
                        HuaweiWifiWatchdogStateMachine.this.logI("tryRecoverByRssi, rssi=" + current_rssi + "dB, mRssiGoodCount =" + this.mRssiGoodCount);
                        this.mRssiGoodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                        if (HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager != null) {
                            HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.setBQERoveInReason(HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD);
                        }
                        HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE, HuaweiWifiWatchdogStateMachine.TCP_MIN_TX_PKT);
                    }
                } else {
                    this.mRssiGoodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                }
            }
        }

        private void tryRecoverWifiByOta(int current_rssi) {
            if (this.mIsReportWiFiGoodAllow) {
                int d_rssi = current_rssi - HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi;
                boolean isRssiIncrement = d_rssi >= HuaweiWifiWatchdogStateMachine.MAX_RSSI_PKT_READ_WAIT_SECOND ? HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE : false;
                boolean isRoTimerTimeout = (!this.mIsOtaPoorTimeOut || d_rssi <= HuaweiWifiWatchdogStateMachine.GOOD_LINK_JUDGE_SUB_VAL) ? false : HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                if (isRssiIncrement || isRoTimerTimeout) {
                    int i = this.mRssiGoodCount + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                    this.mRssiGoodCount = i;
                    if (i >= HuaweiWifiWatchdogStateMachine.this.mCurrentBssid.mGoodLinkTargetCount) {
                        HuaweiWifiWatchdogStateMachine.this.logI("Recover By Ota, rssi=" + current_rssi + ", poor rssi=" + HuaweiWifiWatchdogStateMachine.this.mWifiPoorRssi + ", RssiGoodCount =" + this.mRssiGoodCount + ", OtaPoorTimeOut =" + this.mIsOtaPoorTimeOut);
                        this.mRssiGoodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
                        if (HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager != null) {
                            if (isRssiIncrement) {
                                HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.setBQERoveInReason(HuaweiWifiWatchdogStateMachine.VL_RSSI_SMOOTH_DIV_NUM);
                            } else {
                                HuaweiWifiWatchdogStateMachine.this.mWifiProStatisticsManager.setBQERoveInReason(HuaweiWifiWatchdogStateMachine.TCP_MIN_TX_PKT);
                            }
                        }
                        HuaweiWifiWatchdogStateMachine.this.sendLinkStatusNotification(HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE, HuaweiWifiWatchdogStateMachine.TCP_MIN_TX_PKT);
                    }
                } else {
                    this.mRssiGoodCount = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD;
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
                iArr[DetailedState.AUTHENTICATING.ordinal()] = HuaweiWifiWatchdogStateMachine.VL_RSSI_SMOOTH_DIV_NUM;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DetailedState.BLOCKED.ordinal()] = HuaweiWifiWatchdogStateMachine.TCP_MIN_TX_PKT;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = HuaweiWifiWatchdogStateMachine.BASE_SCORE_OTA_BAD;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[DetailedState.CONNECTED.ordinal()] = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_4_BETTER;
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
                iArr[DetailedState.DISCONNECTING.ordinal()] = HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_5_BEST;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[DetailedState.FAILED.ordinal()] = 9;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[DetailedState.IDLE.ordinal()] = HuaweiWifiWatchdogStateMachine.QUERY_TCP_INFO_RETRY_CNT;
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
                iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
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
                        HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD);
                    }
                    if (networkInfo != null) {
                        HuaweiWifiWatchdogStateMachine.this.logD("Network state change " + networkInfo.getDetailedState());
                        switch (-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[networkInfo.getDetailedState().ordinal()]) {
                            case HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD /*1*/:
                                HuaweiWifiWatchdogStateMachine.this.logD(" rcv VERIFYING_POOR_LINK, do nothing.");
                                break;
                            default:
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
            return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
        }
    }

    class WifiProWatchdogEnabledState extends State {
        WifiProWatchdogEnabledState() {
        }

        public void enter() {
            HuaweiWifiWatchdogStateMachine.this.logD("EnabledState enter.");
            HuaweiWifiWatchdogStateMachine.this.wifiOtaChkIsEnable = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
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
                                HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD);
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
                            HuaweiWifiWatchdogStateMachine.this.updateCurrentBssid(null, null, HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD);
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
            return HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.HuaweiWifiWatchdogStateMachine.<clinit>():void");
    }

    private float checkGoodSpd(int rssi, long spd) {
        if (spd < 30720 || rssi >= QOS_GOOD_LEVEL_OF_RSSI || rssi == INVALID_RSSI) {
            return 0.0f;
        }
        int len = RSSI_GOOD_SPD_TBL.length;
        int i = WLAN_INET_RSSI_LEVEL_3_GOOD;
        while (i < len) {
            if (rssi < RSSI_GOOD_SPD_TBL[i].RSSI_VAL) {
                i += WIFI_POOR_OTA_OR_TCP_BAD;
            } else if (spd < ((long) RSSI_GOOD_SPD_TBL[i].SPD_VAL)) {
                return 0.0f;
            } else {
                float retScore = SCORE_BACK_OFF_GOOD_RTT_RATE + ((((float) (spd - ((long) RSSI_GOOD_SPD_TBL[i].SPD_VAL))) * ADD_UNIT_SPD_SCORE) / ((float) RSSI_GOOD_SPD_TBL[i].UNIT_SPD_VAL));
                if (retScore > MAX_SCORE_ONE_CHECK) {
                    retScore = MAX_SCORE_ONE_CHECK;
                }
                logD("checkGoodSpd at rssi:" + rssi + ", spd:" + (spd / 1024) + "K > " + (RSSI_GOOD_SPD_TBL[i].SPD_VAL / SPEED_VAL_1KBPS) + "K, unit:" + (RSSI_GOOD_SPD_TBL[i].UNIT_SPD_VAL / SPEED_VAL_1KBPS) + "K, score:" + formatFloatToStr((double) retScore));
                return retScore;
            }
        }
        return 0.0f;
    }

    private HuaweiWifiWatchdogStateMachine(Context context, Messenger dstMessenger, Handler h) {
        super(TAG);
        this.QOE_LEVEL_RTT_MIN_PKT = TCP_MIN_TX_PKT;
        this.LOW_RSSI_TH = QOS_NOT_GOOD_LEVEL_OF_RSSI;
        this.RSSI_TH_FOR_NOT_BAD_QOE_LEVEL = MAY_BE_POOR_RSSI_TH;
        this.RSSI_TH_FOR_BAD_QOE_LEVEL = POOR_NET_MIN_RSSI_TH;
        this.mWsmChannel = new AsyncChannel();
        this.mBssidCache = new LruCache(DOWNLOAD_PROHIBIT_SWITCH_PROTECTION);
        this.mRssiFetchToken = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mIsRegister = false;
        this.mNewestTcpChkResult = new TcpChkResult();
        this.mWpLinkMonitorRunning = false;
        this.mTcpReportLevel = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mWifiProDefaultState = new WifiProDefaultState();
        this.mWifiProWatchdogDisabledState = new WifiProWatchdogDisabledState();
        this.mWifiProWatchdogEnabledState = new WifiProWatchdogEnabledState();
        this.mWifiProVerifyingLinkState = new WifiProVerifyingLinkState();
        this.mWifiProLinkMonitoringState = new WifiProLinkMonitoringState();
        this.mWifiProStopVerifyState = new WifiProStopVerifyState();
        this.mTraffic = null;
        this.mIsMonitoring = false;
        this.mIsSpeedOkDuringPeriod = false;
        this.mTxrxStat = null;
        this.mPoorNetRssiRealTH = POOR_LINK_RSSI_THRESHOLD;
        this.mPoorNetRssiTH = POOR_LINK_RSSI_THRESHOLD;
        this.mPoorNetRssiTHNext = POOR_LINK_RSSI_THRESHOLD;
        this.mRssiTHValidTime = RSSI_TH_MIN_VALID_TIME;
        this.mHighSpeedToken = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mRssiTHTimeoutToken = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mCurrRssi = INVALID_RSSI;
        this.mSpeedGoodCount = 0.0f;
        this.mSpeedNotGoodCount = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mPeriodGoodSpdScore = 0.0f;
        this.mSwitchOutTime = 0;
        this.mCurrSSID = null;
        this.mLastHighDataFlow = false;
        this.mHighDataFlowScenario = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mHighDataFlowProtection = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mHighDataFlowPeriodCounter = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mHighDataFlowNotDetectCounter = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mHighDataFlowLastRxBytes = 0;
        this.mHighDataFlowRate = HIGH_DATA_FLOW_DEFAULT_RATE;
        this.mCurrentBqeLevel = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mLastDetectLevel = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mBestSpeedInPeriod = 0;
        this.mRssiChangedToken = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mIsDualbandEnable = false;
        this.mHistoryHSCount = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mLastSampleRssi = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mLastSampleRtt = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mLastSamplePkts = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mLastSampleBssid = null;
        this.mSpeedUpdate = new Runnable() {
            public void run() {
                synchronized (HuaweiWifiWatchdogStateMachine.this.mTraffic) {
                    HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine;
                    HuaweiWifiWatchdogStateMachine.this.mTxrxStat = HuaweiWifiWatchdogStateMachine.this.mTraffic.getStatic(HuaweiWifiWatchdogStateMachine.VL_RSSI_SMOOTH_DIV_NUM);
                    HuaweiWifiWatchdogStateMachine.this.logD("speed: rxpkt:" + HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rxPkts + ", rxSpd:" + HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed + "B/s, txpkt:" + HuaweiWifiWatchdogStateMachine.this.mTxrxStat.txPkts + ", txSpd:" + HuaweiWifiWatchdogStateMachine.this.mTxrxStat.tx_speed);
                    if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 24576 || HuaweiWifiWatchdogStateMachine.this.mTxrxStat.tx_speed >= 32768) {
                        HuaweiWifiWatchdogStateMachine.this.mIsSpeedOkDuringPeriod = HuaweiWifiWatchdogStateMachine.HISTORY_RCD_DBG_MODE;
                    }
                    if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed > HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod) {
                        HuaweiWifiWatchdogStateMachine.this.mBestSpeedInPeriod = HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed;
                    }
                    if (HuaweiWifiWatchdogStateMachine.this.mCurrRssi != HuaweiWifiWatchdogStateMachine.INVALID_RSSI && HuaweiWifiWatchdogStateMachine.this.mCurrRssi < HuaweiWifiWatchdogStateMachine.POOR_LINK_RSSI_THRESHOLD && HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 65536) {
                        huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine2 = HuaweiWifiWatchdogStateMachine.this;
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine3 = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.sendMessage(huaweiWifiWatchdogStateMachine2.obtainMessage(HuaweiWifiWatchdogStateMachine.EVENT_HIGH_NET_SPEED_DETECT_MSG, huaweiWifiWatchdogStateMachine3.mHighSpeedToken = huaweiWifiWatchdogStateMachine3.mHighSpeedToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD, HuaweiWifiWatchdogStateMachine.WLAN_INET_RSSI_LEVEL_3_GOOD));
                    }
                    huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                    huaweiWifiWatchdogStateMachine.mPeriodGoodSpdScore = huaweiWifiWatchdogStateMachine.mPeriodGoodSpdScore + HuaweiWifiWatchdogStateMachine.this.checkGoodSpd(HuaweiWifiWatchdogStateMachine.this.mCurrRssi, HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed);
                    if (HuaweiWifiWatchdogStateMachine.this.mTxrxStat.rx_speed >= 1048576) {
                        huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.mHistoryHSCount = huaweiWifiWatchdogStateMachine.mHistoryHSCount + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
                    }
                }
            }
        };
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
        setLogOnlyTransitions(HISTORY_RCD_DBG_MODE);
        this.mTraffic = new TrafficMonitor(this.mSpeedUpdate);
        this.mFormatData = new DecimalFormat("#.##");
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
        this.mWifiProRoveOutParaRecord = new WifiProRoveOutParaRecord();
        this.mWifiProHistoryRecordManager = WifiProHistoryRecordManager.getInstance(this.mContext, this.mWifiManager);
        this.mIsDualbandEnable = HISTORY_RCD_DBG_MODE;
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
                this.mTraffic.enableMonitor(HISTORY_RCD_DBG_MODE, WIFI_POOR_OTA_OR_TCP_BAD);
                this.mTraffic.setExpireTime(SPEED_EXPIRE_LATENCY);
                this.mIsMonitoring = HISTORY_RCD_DBG_MODE;
            }
        } else if (this.mIsMonitoring) {
            logD("monitorNetworkQos stop speed track.");
            this.mTraffic.enableMonitor(false, WIFI_POOR_OTA_OR_TCP_BAD);
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
                    HuaweiWifiWatchdogStateMachine.this.sendMessage(HuaweiWifiWatchdogStateMachine.EVENT_WIFI_RADIO_STATE_CHANGE, intent.getIntExtra("wifi_state", HuaweiWifiWatchdogStateMachine.BASE_SCORE_OTA_BAD));
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    int newRssiVal = intent.getIntExtra("newRssi", HuaweiWifiWatchdogStateMachine.INVALID_RSSI);
                    if (HuaweiWifiWatchdogStateMachine.INVALID_RSSI != newRssiVal) {
                        HuaweiWifiWatchdogStateMachine huaweiWifiWatchdogStateMachine = HuaweiWifiWatchdogStateMachine.this;
                        huaweiWifiWatchdogStateMachine.mRssiChangedToken = huaweiWifiWatchdogStateMachine.mRssiChangedToken + HuaweiWifiWatchdogStateMachine.WIFI_POOR_OTA_OR_TCP_BAD;
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
            this.mIsRegister = HISTORY_RCD_DBG_MODE;
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
        String strBssid = "null";
        if (this.mCurrentBssid != null) {
            strBssid = this.mCurrentBssid.mBssid;
        }
        logI("sendResultMsgToQM bssid:" + partDisplayBssid(strBssid) + ", qoslevel=" + mQosLevel);
        postEvent(WIFI_POOR_OTA_OR_TCP_BAD, WIFI_POOR_OTA_OR_TCP_BAD, mQosLevel);
    }

    private void sendTCPResultQuery() {
        postEvent(BASE_SCORE_OTA_BAD, WIFI_POOR_OTA_OR_TCP_BAD, WLAN_INET_RSSI_LEVEL_3_GOOD);
    }

    private void resetHighDataFlow() {
        this.mHighDataFlowRate = HIGH_DATA_FLOW_DEFAULT_RATE;
        this.mHighDataFlowScenario = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mHighDataFlowProtection = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mHighDataFlowPeriodCounter = WLAN_INET_RSSI_LEVEL_3_GOOD;
        this.mHighDataFlowNotDetectCounter = WLAN_INET_RSSI_LEVEL_3_GOOD;
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
        this.mCurrentBssid.mPoorLinkTargetRssi = (oldRssi + badRssi) / VL_RSSI_SMOOTH_DIV_NUM;
        logD("adjustTargetPoorRssi current mrssi=" + badRssi + ", ajust poor target rssi from:" + oldRssi + " to " + this.mCurrentBssid.mPoorLinkTargetRssi);
    }

    private int wpGoodLinkLevelCaculate(int mGoodLinkTgtRssi, int avgRssi, double RssiLastLoseRate) {
        int mCneGoodLinkLevel;
        if (avgRssi - mGoodLinkTgtRssi >= WLAN_INET_RSSI_LEVEL_5_BEST || RssiLastLoseRate <= GOOD_LINK_LEVEL_5_BEST_LOSERATE) {
            mCneGoodLinkLevel = WLAN_INET_RSSI_LEVEL_4_BETTER;
        } else if (avgRssi - mGoodLinkTgtRssi >= WLAN_INET_RSSI_LEVEL_4_BETTER || RssiLastLoseRate <= GOOD_LINK_LEVEL_4_BETTER_LOSERATE) {
            mCneGoodLinkLevel = BASE_SCORE_OTA_BAD;
        } else {
            mCneGoodLinkLevel = TCP_MIN_TX_PKT;
        }
        logD("wpGoodLinkLevelCaculate, Target RSSI:" + mGoodLinkTgtRssi + ", avgRssi:" + avgRssi + ",  RssiLastLoseRate = " + formatFloatToStr(RssiLastLoseRate) + "get mCneGoodLinkLevel=" + mCneGoodLinkLevel);
        return mCneGoodLinkLevel;
    }

    private int wpPoorLinkLevelCalcByRssi(double lossRate, int mrssi, int poor_link_rssi, int rtt) {
        if (((double) mrssi) <= ((double) poor_link_rssi) - BAD_RSSI_LEVEL_0_NOT_AVAILABLE_SUB_VAL || lossRate >= RSSI_TH_BACKOFF_TCP_RETRAN_TH || rtt >= RTT_VALID_MAX_TIME) {
            return WLAN_INET_RSSI_LEVEL_3_GOOD;
        }
        if (((double) mrssi) <= ((double) poor_link_rssi) - BAD_RSSI_LEVEL_1_VERY_POOR_SUB_VAL || lossRate >= 0.30000000000000004d || rtt >= SPEED_EXPIRE_LATENCY) {
            return WIFI_POOR_OTA_OR_TCP_BAD;
        }
        return VL_RSSI_SMOOTH_DIV_NUM;
    }

    private int wpPoorLinkLevelCalcByTcp(double lossRate, double tcpRetxRate, int rtt) {
        int mCnePoorLinkLevel;
        if (tcpRetxRate >= PKT_CHK_RETXRATE_LEVEL_0_NOT_AVAILABLE || lossRate >= RSSI_TH_BACKOFF_TCP_RETRAN_TH || rtt >= RTT_VALID_MAX_TIME) {
            mCnePoorLinkLevel = WLAN_INET_RSSI_LEVEL_3_GOOD;
        } else if (tcpRetxRate >= PKT_CHK_RETXRATE_LEVEL_1_VERY_POOR || lossRate >= 0.30000000000000004d || rtt >= SPEED_EXPIRE_LATENCY) {
            mCnePoorLinkLevel = WIFI_POOR_OTA_OR_TCP_BAD;
        } else {
            mCnePoorLinkLevel = VL_RSSI_SMOOTH_DIV_NUM;
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
        int retryCnt = QUERY_TCP_INFO_RETRY_CNT;
        while (true) {
            retryCnt += TCP_RESULT_TYPE_INVALID;
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
            long dtime = (nowtime - tmpTcr.mUpdateTime) + ((long) (tmpTcr.mTcpRttWhen * TCP_PERIOD_CHK_RTT_GOOD_TH));
            if (dtime < LINK_SAMPLING_INTERVAL_MS && tmpTcr.mTcpRttPkts > VL_RSSI_SMOOTH_DIV_NUM) {
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
                this.mWifiProStatisticsManager.updateWifiConnectState(WIFI_POOR_OTA_OR_TCP_BAD);
            }
        } else if (this.mCurrentBssid != null) {
            logI(" BSSID changed, bssid:" + partDisplayBssid(this.mCurrentBssid.mBssid) + " was disconnected. set mCurrentBssid=null");
            if (this.mWifiProHistoryRecordManager != null) {
                this.mWifiProHistoryRecordManager.updateCurrConntAp(null, null, WLAN_INET_RSSI_LEVEL_3_GOOD);
            } else {
                logE("updateCurrentBssid APInfoProcess null error.");
            }
            this.mCurrentBssid.afterDisconnectProcess();
            this.mCurrentBssid = null;
            sendMessage(EVENT_BSSID_CHANGE);
            this.mWifiProStatisticsManager.updateWifiConnectState(VL_RSSI_SMOOTH_DIV_NUM);
        }
    }

    private String formatFloatToStr(double data) {
        if (this.mFormatData != null) {
            return this.mFormatData.format(data);
        }
        return DEFAULT_DISPLAY_DATA_STR;
    }

    private void sendLinkStatusNotification(boolean isGood, int linkLevel) {
        if (WifiproUtils.NET_INET_QOS_LEVEL_UNKNOWN == linkLevel) {
            logD(" sendLinkStatusNotification send unknow result.");
            sendResultMsgToQM(WifiproUtils.NET_INET_QOS_LEVEL_UNKNOWN);
            return;
        }
        if (isGood) {
            logI("judge good link######, goodLinkLevel=" + linkLevel);
            sendResultMsgToQM(linkLevel);
        } else {
            logI("judge poor link######, poorLinkLevel=" + linkLevel);
            sendResultMsgToQM(linkLevel);
        }
    }

    public void setSampleRtt(int[] ipqos, int len) {
        int i = WLAN_INET_RSSI_LEVEL_3_GOOD;
        if (this.mIsDualbandEnable) {
            try {
                if (this.mCurrentBssid == null) {
                    Log.d(TAG, "setSampleRtt: Bssid is null, return");
                } else if (this.mCurrentBssid.mBssid.equals(this.mLastSampleBssid)) {
                    this.mLastSampleBssid = this.mCurrentBssid.mBssid;
                    int tcp_rtt_when = len > VL_RSSI_SMOOTH_DIV_NUM ? ipqos[VL_RSSI_SMOOTH_DIV_NUM] : WLAN_INET_RSSI_LEVEL_3_GOOD;
                    if (tcp_rtt_when >= QUERY_TCP_INFO_RETRY_CNT) {
                        Log.d(TAG, "setSampleRtt: unvalid rtt");
                        this.mLastSampleRtt = WLAN_INET_RSSI_LEVEL_3_GOOD;
                        this.mLastSamplePkts = WLAN_INET_RSSI_LEVEL_3_GOOD;
                        return;
                    }
                    int i2;
                    if (len > 0) {
                        i2 = ipqos[WLAN_INET_RSSI_LEVEL_3_GOOD];
                    } else {
                        i2 = WLAN_INET_RSSI_LEVEL_3_GOOD;
                    }
                    this.mLastSampleRtt = i2;
                    if (len > WIFI_POOR_OTA_OR_TCP_BAD) {
                        i = ipqos[WIFI_POOR_OTA_OR_TCP_BAD];
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
            return WLAN_INET_RSSI_LEVEL_3_GOOD;
        }
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return WLAN_INET_RSSI_LEVEL_3_GOOD;
        }
        int rssi = wifiInfo.getRssi();
        if (this.mCurrentBssid == null) {
            return WLAN_INET_RSSI_LEVEL_3_GOOD;
        }
        int qosLevel;
        int avgRtt = this.mCurrentBssid.getHistoryNearRtt(rssi);
        if (avgRtt == 0) {
            qosLevel = WLAN_INET_RSSI_LEVEL_3_GOOD;
        } else if (avgRtt <= SCORE_BACK_OFF_NETGOOD_MAX_RTT) {
            qosLevel = TCP_MIN_TX_PKT;
        } else if (avgRtt <= WifiproBqeUtils.BQE_NOT_GOOD_RTT) {
            qosLevel = VL_RSSI_SMOOTH_DIV_NUM;
        } else {
            qosLevel = WIFI_POOR_OTA_OR_TCP_BAD;
        }
        if (qosLevel != 0) {
            return qosLevel;
        }
        if (rssi >= QOS_GOOD_LEVEL_OF_RSSI) {
            qosLevel = TCP_MIN_TX_PKT;
        } else if (rssi >= QOS_NOT_GOOD_LEVEL_OF_RSSI) {
            qosLevel = VL_RSSI_SMOOTH_DIV_NUM;
        } else {
            qosLevel = WIFI_POOR_OTA_OR_TCP_BAD;
        }
        return qosLevel;
    }

    public boolean getHandover5GApRssiThreshold(WifiProEstimateApInfo estimateApInfo) {
        logD("start getHandover5GApRssiThreshold");
        sendMessage(EVENT_GET_5G_AP_RSSI_THRESHOLD, estimateApInfo);
        return HISTORY_RCD_DBG_MODE;
    }

    public boolean getApHistoryQualityScore(WifiProEstimateApInfo estimateApInfo) {
        logD("start getApHistoryQualityScore");
        sendMessage(EVENT_GET_AP_HISTORY_QUALITY_SCORE, estimateApInfo);
        return HISTORY_RCD_DBG_MODE;
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
        logI("getApQualityRecord enter, for bssid:" + apBssid);
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
        logD("target5GApRssiTHProcess enter: ssid=" + estimateApInfo.getApSsid() + " bssid=" + estimateApInfo.getApBssid());
        String apBssid = estimateApInfo.getApBssid();
        if (apBssid == null) {
            logE("target5GApRssiTHProcess apBssid null error.");
            estimateApInfo.setRetRssiTH(QOS_GOOD_LEVEL_OF_RSSI);
            sendDBQoeResult(12, estimateApInfo);
            return;
        }
        BssidStatistics apQualityInfo = getApQualityRecord(apBssid);
        WifiProApInfoRecord apInfoRecord = this.mWifiProHistoryRecordManager.getApInfoRecord(apBssid);
        if (apInfoRecord != null) {
            logD("target5GApRssiTHProcess test last connected time:" + apInfoRecord.lastConnectTime + ", total connect time:" + apInfoRecord.totalUseTime + ", night connect time:" + apInfoRecord.totalUseTimeAtNight + ", weekend connect time:" + apInfoRecord.totalUseTimeAtWeekend);
        }
        estimateApInfo.setRetRssiTH(apQualityInfo.findRssiTargetByRtt(-80, BSSID_STAT_RANGE_HIGH_DBM, 1500, QOS_GOOD_LEVEL_OF_RSSI));
        sendDBQoeResult(12, estimateApInfo);
    }

    private void targetApHistoryQualityScoreProcess(WifiProEstimateApInfo estimateApInfo) {
        estimateApInfo.setRetHistoryScore(getApScore(estimateApInfo));
        sendDBQoeResult(13, estimateApInfo);
    }

    private int getApScore(WifiProEstimateApInfo estimateApInfo) {
        if (this.mCurrentBssid == null || this.mCurrentBssid.mBssid == null || this.mHwDualBandQualityEngine == null || estimateApInfo == null) {
            logE("getApHistoryQualityScore null pointer.");
            return TCP_RESULT_TYPE_INVALID;
        }
        logD("getApHistoryQualityScore: ssid=" + estimateApInfo.getApSsid() + " bssid=" + estimateApInfo.getApBssid());
        int retScore = getApFixedScore(estimateApInfo) + WLAN_INET_RSSI_LEVEL_3_GOOD;
        int variedScore = getApVariedScore(estimateApInfo);
        if (variedScore == TCP_RESULT_TYPE_INVALID) {
            return TCP_RESULT_TYPE_INVALID;
        }
        retScore += variedScore;
        logD("ssid:" + estimateApInfo.getApSsid() + " bssid:" + estimateApInfo.getApBssid() + " total score is " + retScore);
        return retScore;
    }

    private int getApFixedScore(WifiProEstimateApInfo estimateApInfo) {
        int score;
        int fixedScore = WLAN_INET_RSSI_LEVEL_3_GOOD;
        String apBssid = estimateApInfo.getApBssid();
        WifiProApInfoRecord apInfoRecord = this.mWifiProHistoryRecordManager.getApInfoRecord(apBssid);
        if (apInfoRecord != null) {
            score = this.mHwDualBandQualityEngine.getScoreByConnectTime(apInfoRecord.totalUseTime / WifiProHistoryRecordManager.SECOND_OF_ONE_HOUR);
            logD("bssid:" + apBssid + " ConnectTime score is " + score);
            fixedScore = score + WLAN_INET_RSSI_LEVEL_3_GOOD;
        }
        score = this.mHwDualBandQualityEngine.getScoreByRssi(estimateApInfo.getApRssi());
        logD("bssid:" + apBssid + " rssi score is " + score);
        fixedScore += score;
        if (estimateApInfo.is5GAP()) {
            score = this.mHwDualBandQualityEngine.getScoreByBluetoothUsage();
            logD("bssid:" + apBssid + " BluetoothUsage score is " + score);
            fixedScore += score;
        }
        if (!HwDualBandRelationManager.isDualBandAP(this.mCurrentBssid.mBssid, apBssid) || !estimateApInfo.is5GAP()) {
            return fixedScore;
        }
        logD("bssid:" + apBssid + " DUAL_BAND_SINGLE_AP score is " + 6);
        return fixedScore + 6;
    }

    private int getApVariedScore(WifiProEstimateApInfo estimateApInfo) {
        BssidStatistics apQualityInfo;
        int variedScore = WLAN_INET_RSSI_LEVEL_3_GOOD;
        String apBssid = estimateApInfo.getApBssid();
        if (this.mCurrentBssid == null || !this.mCurrentBssid.mBssid.equals(apBssid)) {
            apQualityInfo = getApQualityRecord(apBssid);
        } else {
            apQualityInfo = this.mCurrentBssid;
        }
        if (apQualityInfo == null) {
            logE("get5GApHandoverVariedScore apQualityInfo null pointer.");
            return TCP_RESULT_TYPE_INVALID;
        }
        int queryRssi = estimateApInfo.getApRssi();
        if (queryRssi > BSSID_STAT_RANGE_HIGH_DBM) {
            queryRssi = BSSID_STAT_RANGE_HIGH_DBM;
        } else if (queryRssi < BSSID_STAT_RANGE_LOW_DBM) {
            queryRssi = BSSID_STAT_RANGE_LOW_DBM;
        }
        int avgRtt = apQualityInfo.getHistoryNearRtt(queryRssi);
        if (avgRtt != 0) {
            int score = this.mHwDualBandQualityEngine.getScoreByRtt(avgRtt);
            logD("bssid:" + apBssid + " rtt score is " + score);
            variedScore = score + WLAN_INET_RSSI_LEVEL_3_GOOD;
        } else {
            logD("There is no rtt history about rssi " + queryRssi);
        }
        double[] lossRateInfo = apQualityInfo.getHistoryNearLossRate(queryRssi);
        if (lossRateInfo[WIFI_POOR_OTA_OR_TCP_BAD] > 10.0d) {
            score = this.mHwDualBandQualityEngine.getScoreByLossRate(lossRateInfo[WLAN_INET_RSSI_LEVEL_3_GOOD]);
            logD("bssid:" + apBssid + " rtt score is " + score);
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
        if (printLogLevel <= WIFI_POOR_OTA_OR_TCP_BAD) {
            Log.d(TAG, msg);
        }
    }

    private void logI(String msg) {
        if (printLogLevel <= VL_RSSI_SMOOTH_DIV_NUM) {
            Log.i(TAG, msg);
        }
    }

    private void logE(String msg) {
        if (printLogLevel <= TCP_MIN_TX_PKT) {
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
        return srcBssid.substring(WLAN_INET_RSSI_LEVEL_3_GOOD, 6) + "**:**" + srcBssid.substring(len - 6, len);
    }

    public synchronized boolean isHighDataFlowModel() {
        if (this.mHighDataFlowScenario != 0) {
            return HISTORY_RCD_DBG_MODE;
        }
        return false;
    }
}
