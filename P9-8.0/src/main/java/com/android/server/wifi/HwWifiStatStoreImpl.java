package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;
import android.net.wifi.SupplicantState;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFormatException;
import android.os.ParcelableException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.AppTypeRecoManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.MSS.HwMSSHandler;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.huawei.ncdft.HwWifiDFTConnManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class HwWifiStatStoreImpl extends Handler implements HwWifiStatStore {
    private static final int ASSOC_REJECT_ACCESSFULL = 17;
    private static final int CHECK_SCAN_CNT = 5;
    private static final int CHECK_SCAN_RESULTS_DURATION = 300000;
    private static final int CONVERT_UNITS = 1024;
    private static final String DEFAULT_WLAN_IFACE = "wlan0";
    private static final String DNAMESTR = "dname=";
    private static final int DS_WEB_STAT = 0;
    private static final String EXCEED_WARN_TEM_CNT = "exceed_warn_tem_cnt";
    private static final int GAME_BOOST_SIGNAL = 4;
    public static final String GAME_KOG_PROCESSNAME = "com.tencent.tmgp.sgame";
    private static final int HW_CONNECT_REASON_CONNECTING = 1;
    private static final int HW_CONNECT_REASON_REKEY = 3;
    private static final int HW_CONNECT_REASON_ROAMING = 2;
    private static final int HW_CONNECT_REASON_UNINITIAL = 0;
    private static String IMONITOR_WIFI_LOWPOWER_DIR = "/data/log/wifi/";
    private static final String INTENT_DS_WIFI_WEB_STAT_REPORT = "com.huawei.chr.wifi.action.web_stat_report";
    private static final String INTENT_WIFI_DNS_STATISTICS = "com.intent.action.wifi_dns_statistics";
    private static final String MAXTEM = "maxtem";
    private static final String MINDUTYCYCLE = "mindutycycle";
    private static final int MINLENOFDNAME = DNAMESTR.length();
    private static final long MIN_PERIOD_TRIGGER_BETA = 7200000;
    private static final long MIN_PERIOD_TRIGGER_CML = 86400000;
    private static final int MSG_CHECK_SCAN_RESULTS = 101;
    private static final long MSG_SEND_DELAY_DURATION = 1800000;
    private static final int MSG_SEND_DELAY_ID = 100;
    private static final int MSG_WIFI_TIM_ID = 102;
    private static final int MSG_WLAN_CURRENT_TRAFFIC_BYTES = 200;
    private static final int MSS_REASON_CNT = 3;
    private static final int RSSI_TRIGGER_THRESHOLD = -82;
    private static final int RSSI_TRIGGER_THRESHOLD_24G = -80;
    private static final int RSSI_TRIGGER_THRESHOLD_5G = -78;
    private static final int SCAN_FREQ_L1 = 5000;
    private static final int SCAN_FREQ_L2 = 10000;
    private static final int SCAN_FREQ_L3 = 30000;
    private static final int SPEED_INTERVAL = 3;
    private static final String TAG = "HwWifiStatStore";
    private static final String TEM_CTRL_CNT = "tem_ctrl_cnt";
    private static final String TEM_CTRL_TIMES = "tem_ctrl_times";
    private static final int TX_BOOST_SIGNAL = 5;
    private static final int WEB_DELAY_THRESHOLD = 400;
    private static final String WECHAT_NAME = "com.tencent.mm";
    private static final String WIFI_FIRMLOG_TRIGGER = "com.huawei.lcagent.CONFIG_WIFI_LOG";
    private static final String WIFI_LOWPOWER_ITEM = "SSID,TimeStamp,BeaconInterval,DTIM,ScreenOn,appName,appType,RTT,ForeGroundRXSpeed,ForeGroundTXSpeed,TotalRXSpeed,TotalTXSpeed,PMLimit,DelayCount,RXPktCnt,TXPktCnt\r\n";
    private static final int WIFI_LOWPOWER_MAX_LENGTH = 1048576;
    private static String WIFI_LOWPOWER_NAME = "/data/log/wifi/wifi_lowpower_logs.txt";
    private static final int WIFI_SPEED_INTERVAL = 5;
    private static String WIFI_TEM_CTRL_PATH = "/proc/wifi/wifi_tem_stat";
    private static String WIFI_TIM_PATH = "/proc/wifi/wifi_tim_stat";
    private static final int WLAN_SPEED_DELAY_DURATION = 3000;
    private static Context mContextRef = null;
    private static HashMap<String, Long> mScanTimeStamp = new HashMap();
    private static HashMap<Integer, UidTcpStatInfo> mUidTcpStat = new HashMap();
    private static final Object sAPKListLock = new Object();
    private static HwWifiStatStore sInstance = null;
    private static final Object sLock = new Object();
    private final String WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
    private String connectInternetFailedType = "CONNECT_INTERNET_INITIAL";
    private int connectedNetwork = 0;
    private String disConnectSSID = "";
    private long disconnectDate = 0;
    private boolean isAbnormalDisconnect = false;
    private boolean isConnectToNetwork = false;
    private boolean isScreen = true;
    private ArrayList<HwWifiDFTAPKAction> mAPKActionList = new ArrayList();
    private boolean mAccessWebFailFlag = false;
    private boolean mAccessWebFlag = false;
    private AppTypeRecoManager mAppTypeRecoManager = null;
    private int mBcnInterval = 0;
    private int mCheckScanCnt = 0;
    private boolean mCheckScanResults = false;
    private SSIDStat mCheckStat = null;
    private int mCloseCnt = 0;
    private int mCloseDuration = 0;
    private int mCloseSuccCnt = 0;
    private long mConWifi_ScreenOn_Timestamp = 0;
    private int mConnectTotalCnt = 0;
    private int mConnectTotalStartCnt = 0;
    private String mConnectType = "";
    private int mConnectedCnt = 0;
    private long mConnectingStartTimestamp = 0;
    private HwWifiDFTAPKAction mCurAPKAction = null;
    private SSIDStat mCurrentStat = null;
    private long mDhcpTimestamp = 0;
    private int mDnsMaxTime = 0;
    private int mDnsMinTime = 0;
    private int mDnsReqCnt = 0;
    private int mDnsReqFail = 0;
    private int mDnsTotTime = 0;
    private int mDsStatSuccNum = 0;
    private int mDsStatWebDelay = 0;
    private int mDtim = 0;
    private short mExtendMaxTemCnt = (short) 0;
    private boolean mFreshSpeedFlag = false;
    private int mGameCnt = 0;
    private int[] mGameLag = new int[4];
    private String mGameName = "";
    private HwMSSHandler mHwMssHandler;
    private HwQoEService mHwQoEService;
    private boolean mIsScanBackgroundReq = false;
    private boolean mIsWifiClose = false;
    private int mLastConnetReason = 0;
    private long mLastDnsStatReq = 0;
    private int mLastUpdDHCPReason = 0;
    private SupplicantState mLastWpaState;
    private long mMobileTotalConnectedDuration = 0;
    private long mMobileTotalTrafficBytes = 0;
    private HWNetstatManager mNetstatManager = null;
    private int mOpenCnt = 0;
    private int mOpenDuration = 0;
    private int mOpenSuccCnt = 0;
    private int mPmSleepRet = 0;
    private long mPreMobileBytes = 0;
    private long mPreTimestamp = 0;
    private long mPreWLANBytes = 0;
    private long mPretotalBytes = 0;
    private SSIDStat mPreviousStat = null;
    private int mRcvSegs = 0;
    private int mRssi = WifiHandover.INVALID_RSSI;
    private ArrayList<SSIDStat> mSSIDStatList = new ArrayList();
    private int mSendSegs = 0;
    private int mTimEventCnt = 0;
    private long mTimestamp = 0;
    private int mWeChartBackGroundTimes = 0;
    private int mWeChartDisconnectTimes = 0;
    private int mWeChartLowRssiTimes = 0;
    private int mWeChartTimes = 0;
    private int mWeChartVideoTimes = 0;
    private int mWebFailDura = 0;
    private long mWifiConnectTimestamp = 0;
    private long mWifiConnectedTimestamp = 0;
    private int mWifiOpenStatDura = 0;
    private long mWifiOpenSuccTime = 0;
    private long mWifiSwitchTimestamp = 0;
    private final BroadcastReceiver mWifiWebStatReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Log.e(HwWifiStatStoreImpl.TAG, "onReceive: intent = null or intent.getAction = null");
                return;
            }
            Message msg;
            String action = intent.getAction();
            long now = SystemClock.elapsedRealtime();
            Bundle data;
            if (HwWifiStatStoreImpl.INTENT_DS_WIFI_WEB_STAT_REPORT.equals(action)) {
                data = intent.getExtras();
                if (data != null) {
                    msg = new Message();
                    msg.setData(data);
                    msg.what = HwWifiLogMsgID.EVENT_WIFI_WEB_STAT_REPORT;
                    HwWifiStatStoreImpl.this.sendMessage(msg);
                }
            } else if (HwWifiStatStoreImpl.INTENT_WIFI_DNS_STATISTICS.equals(action)) {
                data = intent.getExtras();
                if (data != null) {
                    msg = new Message();
                    msg.setData(data);
                    msg.what = HwWifiLogMsgID.EVENT_WIFI_DNS_STAT_REPORT;
                    HwWifiStatStoreImpl.this.sendMessage(msg);
                }
            }
            if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwWifiStatStoreImpl.this.isScreen = true;
                HwWifiStatStoreImpl.this.removeMessages(200);
                msg = new Message();
                msg.what = 200;
                HwWifiStatStoreImpl.this.sendMessage(msg);
                if (HwWifiStatStoreImpl.this.connectedNetwork == 1 && HwWifiStatStoreImpl.this.mConWifi_ScreenOn_Timestamp == 0) {
                    HwWifiStatStoreImpl.this.mConWifi_ScreenOn_Timestamp = now;
                }
            }
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                HwWifiStatStoreImpl.this.isScreen = false;
                if (HwWifiStatStoreImpl.this.connectedNetwork == 1 && HwWifiStatStoreImpl.this.mConWifi_ScreenOn_Timestamp != 0) {
                    HwWifiStatStoreImpl hwWifiStatStoreImpl = HwWifiStatStoreImpl.this;
                    hwWifiStatStoreImpl.mwlanTotalConnDura = hwWifiStatStoreImpl.mwlanTotalConnDura + (now - HwWifiStatStoreImpl.this.mConWifi_ScreenOn_Timestamp);
                    HwWifiStatStoreImpl.this.mConWifi_ScreenOn_Timestamp = 0;
                }
            }
            if (action.equals(HwWifiStatStoreImpl.WIFI_FIRMLOG_TRIGGER)) {
                int status = intent.getIntExtra(HwCHRWifiRelatedStateMonitor.BTOPP_STATUS, -1);
                Log.d(HwWifiStatStoreImpl.TAG, "receive firmlog brocast from HiView, status is:" + status);
                msg = new Message();
                msg.arg1 = status;
                msg.what = HwWifiLogMsgID.EVENT_WIFI_FIRMLOG_TRIGGER;
                HwWifiStatStoreImpl.this.sendMessage(msg);
            }
        }
    };
    private long mWlanTotalConnectedDuration = 0;
    private long mWlanTotalTrafficBytes = 0;
    private int mabDisconnCnt = 0;
    private short maccWebFailCnt = (short) 0;
    private int mconnectedDura = 0;
    private int mconnectingDura = 0;
    private int mdisconnCnt = 0;
    private int mgameKogScene = 0;
    private short mmaxTem = (short) 0;
    private short mminDutyCycleCnt = (short) 0;
    private short mtemCtrlCnt = (short) 0;
    private int mtemCtrlDura = 0;
    private int mtotalRcvSegs = 0;
    private int mtotalSendSegs = 0;
    private int mtotalTimEventCnt = 0;
    private int mweChatScene = 0;
    private long mwlanTotalConnDura = 0;
    private long onScreenTimestamp = 0;

    private class SSIDStat {
        public String BSSID;
        public String SSID;
        private String apVendorInfo;
        private int mABSAssociateFailedTimes;
        private int mABSAssociateTimes;
        private long mABSMimoScreenOnTime;
        private long mABSMimoTime;
        private long mABSSisoScreenOnTime;
        private long mABSSisoTime;
        private int mAbDisconnectCnt;
        private int mAccWebFailCnt;
        private int mAccessWEBCnt;
        private int mAccessWEBSuccCnt;
        public int mAccessWebFailDura;
        public long mAccessWebFailTimeStamp;
        private int mAccessWebFailedPortal;
        private int mAccessWebReDHCPFailedPortal;
        private int mAccessWebRoamingFailedPortal;
        private int mAppDisabledAbnromalCnt;
        private int mAppDisabledScSuccCnt;
        private int mArpReassocOkCnt;
        private int mArpUnreachableCnt;
        private int mAssocByABSCnt;
        public int mAssocCnt;
        public int mAssocDuration;
        private int mAssocRejectAccessFullCnt;
        private int mAssocRejectedAbnormalCnt;
        private int mAssocRejectedScSuccCnt;
        public int mAssocSuccCnt;
        public long mAssocingTimestamp;
        public int mAuthCnt;
        public int mAuthDuration;
        private int mAuthFailedAbnormalCnt;
        private int mAuthFailedScSuccCnt;
        public int mAuthSuccCnt;
        private int mBlackListAbnormalCnt;
        private int mBlackListScSuccCnt;
        public int mCHRConnectingDuration;
        public int mConnectTotalCnt;
        public int mConnectTotalStartCnt;
        private String mConnectType;
        public int mConnectedCnt;
        public int mConnectedDuration;
        public long mConnectedTimeStamp;
        public int mConnectingDuration;
        public int mDhcpAutoIpCnt;
        public int mDhcpCnt;
        public int mDhcpDuration;
        private int mDhcpFailedAbnormalCnt;
        private int mDhcpFailedScSuccCnt;
        private int mDhcpFailedStaticScSuccCnt;
        public int mDhcpStaticCnt;
        public int mDhcpStaticSuccCnt;
        public int mDhcpSuccCnt;
        public int mDisconnectCnt;
        private int mDnsAbnormalCnt;
        private int mDnsCount;
        private int mDnsDelayL1;
        private int mDnsDelayL2;
        private int mDnsDelayL3;
        private int mDnsDelayL4;
        private int mDnsDelayL5;
        private int mDnsDelayL6;
        private int mDnsFailCount;
        private int mDnsIpv6TimeOut;
        private int mDnsParseFailCnt;
        private int mDnsResetScSuccCnt;
        private int mDnsScSuccCnt;
        private int mDnsTotaldelay;
        private int mDsDelayL1;
        private int mDsDelayL2;
        private int mDsDelayL3;
        private int mDsDelayL4;
        private int mDsDelayL5;
        private int mDsDelayL6;
        private int mDsRTTL1;
        private int mDsRTTL2;
        private int mDsRTTL3;
        private int mDsRTTL4;
        private int mDsRTTL5;
        private int mDsStatFailNum;
        private int mDsStatNoAckNum;
        private int mDsStatRTT;
        private int mDsStatSuccNum;
        private int mDsStatTcpTotalNum;
        private int mDsStatTotalNum;
        private int mDsStatWebDelay;
        private int mFirstConnInternetFailCnt;
        public int mFirstConnInternetFailDuration;
        private short mFreq;
        private int mGatewayAbnormalCnt;
        private int mGoodReConnectCnt;
        private int mGoodReConnectSuccCnt;
        private int mGwResetScSuccCnt;
        private int mInvalidIpScAbnormalCnt;
        private int mInvalidIpScSuccCnt;
        private boolean mIsWifiproFlag;
        private int mMaxspeed;
        private int mMssFailCnt;
        private int[] mMssFailCntArray;
        private int mMssReasonCode;
        private int mMssSuccCnt;
        private int[] mMssSuccCntArray;
        private int mMultiGWCount;
        private int mNoUserProcCnt;
        private int mOnScreenAbDisconnectCnt;
        private int mOnScreenConnectCnt;
        private int mOnScreenConnectDuration;
        private int mOnScreenConnectedCnt;
        private int mOnScreenDisconnectCnt;
        private int mOnScreenReConnectDuration;
        private int mOnScreenReConnectedCnt;
        private int mOnlyTheTxNoRxCnt;
        private byte mPublicEssCnt;
        private int mReDHCPAccessWebSuccCnt;
        private int mReDHCPCnt;
        private int mReDHCPDuration;
        private int mReDHCPSuccCnt;
        private int mReDhcpScSuccCnt;
        private int mReKEYCnt;
        private int mReKEYDuration;
        private int mReKEYSuccCnt;
        private int mReassocScCnt;
        private int mReassocScConnectFailedCnt;
        private int mResetScConnectFailedCnt;
        private int mResetScSuccCnt;
        private int mRoamingAbnormalCnt;
        private int mRoamingAccessWebSuccCnt;
        private int mRoamingCnt;
        private int mRoamingDuration;
        private int mRoamingResetScSuccCnt;
        private int mRoamingSuccCnt;
        private int mRouterDisplayNoInternetCnt;
        private int mRouterUnreachableCnt;
        private int mStaticIpConflictedScAbnormalCnt;
        private int mStaticIpScSuccCnt;
        private int mTcpRxAbnormalCnt;
        private int[] mTxBoostOffRTT;
        private int[] mTxBoostOffRTTCnt;
        private int[] mTxBoostOffTxRetry;
        private int[] mTxBoostOfftxBad;
        private int[] mTxBoostOfftxGood;
        private int[] mTxBoostOnRTT;
        private int[] mTxBoostOnRTTCnt;
        private int[] mTxBoostOnTxRetry;
        private int[] mTxBoostOntxBad;
        private int[] mTxBoostOntxGood;
        private int mUserEnableStaticIpCnt;
        private int mUserLongTimeWaitedCnt;
        private int mWeakReConnectCnt;
        private int mWeakReConnectSuccCnt;
        private String mrouterModel;
        private int mwebFailDura;
        private String strAP_eap;
        private String strAP_key_mgmt;

        /* synthetic */ SSIDStat(HwWifiStatStoreImpl this$0, SSIDStat -this1) {
            this();
        }

        private SSIDStat() {
            this.SSID = "";
            this.BSSID = "";
            this.mAssocCnt = 0;
            this.mAssocSuccCnt = 0;
            this.mAuthCnt = 0;
            this.mAuthSuccCnt = 0;
            this.mDhcpCnt = 0;
            this.mDhcpSuccCnt = 0;
            this.mDhcpStaticCnt = 0;
            this.mDhcpAutoIpCnt = 0;
            this.mAssocingTimestamp = 0;
            this.mConnectingDuration = 0;
            this.mDhcpStaticSuccCnt = 0;
            this.mConnectedCnt = 0;
            this.mDisconnectCnt = 0;
            this.mAssocDuration = 0;
            this.mAuthDuration = 0;
            this.mDhcpDuration = 0;
            this.mConnectedDuration = 0;
            this.mFirstConnInternetFailDuration = 0;
            this.mConnectTotalCnt = 0;
            this.mConnectTotalStartCnt = 0;
            this.mCHRConnectingDuration = 0;
            this.mAccessWebFailDura = 0;
            this.mAccessWebFailTimeStamp = SystemClock.elapsedRealtime();
            this.mConnectedTimeStamp = SystemClock.elapsedRealtime();
            this.mRoamingCnt = 0;
            this.mRoamingSuccCnt = 0;
            this.mRoamingDuration = 0;
            this.mReDHCPCnt = 0;
            this.mReDHCPSuccCnt = 0;
            this.mReDHCPDuration = 0;
            this.mReKEYCnt = 0;
            this.mReKEYSuccCnt = 0;
            this.mReKEYDuration = 0;
            this.mWeakReConnectCnt = 0;
            this.mWeakReConnectSuccCnt = 0;
            this.mGoodReConnectCnt = 0;
            this.mGoodReConnectSuccCnt = 0;
            this.mOnScreenConnectCnt = 0;
            this.mOnScreenConnectedCnt = 0;
            this.mOnScreenAbDisconnectCnt = 0;
            this.mOnScreenReConnectedCnt = 0;
            this.mOnScreenDisconnectCnt = 0;
            this.mOnScreenConnectDuration = 0;
            this.mOnScreenReConnectDuration = 0;
            this.mAccessWEBCnt = 0;
            this.mAccessWEBSuccCnt = 0;
            this.mFirstConnInternetFailCnt = 0;
            this.mOnlyTheTxNoRxCnt = 0;
            this.mDnsParseFailCnt = 0;
            this.mArpUnreachableCnt = 0;
            this.mArpReassocOkCnt = 0;
            this.mDnsAbnormalCnt = 0;
            this.mTcpRxAbnormalCnt = 0;
            this.mRoamingAbnormalCnt = 0;
            this.mGatewayAbnormalCnt = 0;
            this.mDnsScSuccCnt = 0;
            this.mReDhcpScSuccCnt = 0;
            this.mStaticIpScSuccCnt = 0;
            this.mReassocScCnt = 0;
            this.mResetScSuccCnt = 0;
            this.mUserEnableStaticIpCnt = 0;
            this.mAuthFailedAbnormalCnt = 0;
            this.mAssocRejectedAbnormalCnt = 0;
            this.mDhcpFailedAbnormalCnt = 0;
            this.mAppDisabledAbnromalCnt = 0;
            this.mAuthFailedScSuccCnt = 0;
            this.mAssocRejectedScSuccCnt = 0;
            this.mDhcpFailedScSuccCnt = 0;
            this.mAppDisabledScSuccCnt = 0;
            this.mDnsResetScSuccCnt = 0;
            this.mRoamingResetScSuccCnt = 0;
            this.mGwResetScSuccCnt = 0;
            this.mReassocScConnectFailedCnt = 0;
            this.mResetScConnectFailedCnt = 0;
            this.mBlackListScSuccCnt = 0;
            this.mBlackListAbnormalCnt = 0;
            this.mDhcpFailedStaticScSuccCnt = 0;
            this.mStaticIpConflictedScAbnormalCnt = 0;
            this.mRouterDisplayNoInternetCnt = 0;
            this.mRouterUnreachableCnt = 0;
            this.mInvalidIpScSuccCnt = 0;
            this.mInvalidIpScAbnormalCnt = 0;
            this.apVendorInfo = "UNKNOW";
            this.strAP_key_mgmt = "";
            this.strAP_eap = "";
            this.mRoamingAccessWebSuccCnt = 0;
            this.mReDHCPAccessWebSuccCnt = 0;
            this.mNoUserProcCnt = 0;
            this.mUserLongTimeWaitedCnt = 0;
            this.mMultiGWCount = 0;
            this.mAccessWebFailedPortal = 0;
            this.mAccessWebRoamingFailedPortal = 0;
            this.mAccessWebReDHCPFailedPortal = 0;
            this.mAbDisconnectCnt = 0;
            this.mIsWifiproFlag = false;
            this.mAssocRejectAccessFullCnt = 0;
            this.mAssocByABSCnt = 0;
            this.mABSAssociateTimes = 0;
            this.mABSAssociateFailedTimes = 0;
            this.mABSMimoTime = 0;
            this.mABSSisoTime = 0;
            this.mABSMimoScreenOnTime = 0;
            this.mABSSisoScreenOnTime = 0;
            this.mAccWebFailCnt = 0;
            this.mMaxspeed = 0;
            this.mwebFailDura = 0;
            this.mDsStatRTT = 0;
            this.mDsStatWebDelay = 0;
            this.mDsStatSuccNum = 0;
            this.mDsStatFailNum = 0;
            this.mDsStatNoAckNum = 0;
            this.mDsStatTotalNum = 0;
            this.mDsStatTcpTotalNum = 0;
            this.mDsDelayL1 = 0;
            this.mDsDelayL2 = 0;
            this.mDsDelayL3 = 0;
            this.mDsDelayL4 = 0;
            this.mDsDelayL5 = 0;
            this.mDsDelayL6 = 0;
            this.mDsRTTL1 = 0;
            this.mDsRTTL2 = 0;
            this.mDsRTTL3 = 0;
            this.mDsRTTL4 = 0;
            this.mDsRTTL5 = 0;
            this.mDnsCount = 0;
            this.mDnsTotaldelay = 0;
            this.mDnsIpv6TimeOut = 0;
            this.mDnsFailCount = 0;
            this.mDnsDelayL1 = 0;
            this.mDnsDelayL2 = 0;
            this.mDnsDelayL3 = 0;
            this.mDnsDelayL4 = 0;
            this.mDnsDelayL5 = 0;
            this.mDnsDelayL6 = 0;
            this.mMssReasonCode = 0;
            this.mMssSuccCnt = 0;
            this.mMssFailCnt = 0;
            this.mPublicEssCnt = (byte) 0;
            this.mTxBoostOnRTT = new int[5];
            this.mTxBoostOnRTTCnt = new int[5];
            this.mTxBoostOntxGood = new int[5];
            this.mTxBoostOntxBad = new int[5];
            this.mTxBoostOnTxRetry = new int[5];
            this.mTxBoostOffRTT = new int[5];
            this.mTxBoostOffRTTCnt = new int[5];
            this.mTxBoostOfftxGood = new int[5];
            this.mTxBoostOfftxBad = new int[5];
            this.mTxBoostOffTxRetry = new int[5];
            this.mMssSuccCntArray = new int[3];
            this.mMssFailCntArray = new int[3];
            this.mConnectType = "";
            this.mFreq = (short) 0;
            this.mrouterModel = "";
        }

        public boolean cmp(String ssid) {
            if (ssid == null || this.SSID == null) {
                return false;
            }
            return this.SSID.equals(ssid);
        }

        public boolean hasDataToTrigger() {
            if ((((((this.mAssocCnt + this.mAssocSuccCnt) + this.mAuthCnt) + this.mAuthSuccCnt) + (((this.mDhcpCnt + this.mDhcpSuccCnt) + this.mDhcpStaticCnt) + this.mDhcpAutoIpCnt)) + ((this.mConnectedCnt + this.mDisconnectCnt) + this.mAccessWEBSuccCnt)) + ((this.mRoamingAccessWebSuccCnt + this.mReDHCPAccessWebSuccCnt) + this.mAbDisconnectCnt) > 0) {
                return true;
            }
            return false;
        }
    }

    public static void init(Context context) {
        synchronized (sLock) {
            if (context != null) {
                if (mContextRef == null) {
                    Log.e(TAG, "HwWifiStatStoreImpl init");
                    mContextRef = context;
                    if (sInstance == null) {
                        HandlerThread thread = new HandlerThread(TAG);
                        thread.start();
                        sInstance = new HwWifiStatStoreImpl(thread.getLooper());
                    }
                }
            }
        }
    }

    private HwWifiStatStoreImpl(Looper looper) {
        super(looper);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_DS_WIFI_WEB_STAT_REPORT);
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction(INTENT_WIFI_DNS_STATISTICS);
        intentFilter.addAction(WIFI_FIRMLOG_TRIGGER);
        mContextRef.registerReceiver(this.mWifiWebStatReceiver, intentFilter);
    }

    public void updateConnectState(boolean connected) {
        Message msg = new Message();
        msg.arg1 = connected ? 1 : 0;
        msg.what = 2000;
        sendMessage(msg);
    }

    public void updateDhcpState(int state) {
        Message msg = new Message();
        msg.arg1 = state;
        msg.what = HwWifiLogMsgID.EVENT_UPDATE_DHCP_STATE;
        sendMessage(msg);
    }

    public void updateWifiState(boolean enable, boolean success) {
        Message msg = new Message();
        ArrayList list = new ArrayList();
        list.add(Boolean.valueOf(enable));
        list.add(Boolean.valueOf(success));
        msg.obj = list;
        msg.what = HwWifiLogMsgID.EVENT_UPDATE_WIFI_STATE;
        sendMessage(msg);
    }

    public void handleWiFiDnsStats(int netid) {
        Message msg = new Message();
        msg.arg1 = netid;
        msg.what = HwWifiLogMsgID.EVENT_WIFI_DNS_STATE;
        sendMessage(msg);
    }

    public void updateScCHRCount(int type) {
        Message msg = new Message();
        msg.arg1 = type;
        msg.what = HwWifiLogMsgID.EVENT_UPDATE_WIFI_SC_CNT;
        sendMessage(msg);
    }

    public void triggerTotalTrafficBytes() {
        Message msg = new Message();
        msg.what = HwWifiLogMsgID.EVENT_TRIGGER_TOTAL_TRAFFIC;
        sendMessage(msg);
    }

    public void handleMessage(Message msg) {
        if (msg != null) {
            Log.d(TAG, "handleMessage : " + msg.what);
            switch (msg.what) {
                case 100:
                    long now = SystemClock.elapsedRealtime();
                    triggerTotalConnetedDuration(this.connectedNetwork);
                    triggerTotalTrafficBytes();
                    triggerConnectedDuration(now, this.mCurrentStat);
                    setConnectedDuration();
                    getTemperatureCtrlParam();
                    if (!(this.mWifiOpenSuccTime == 0 || (this.mIsWifiClose ^ 1) == 0)) {
                        this.mWifiOpenStatDura += (int) (now - this.mWifiOpenSuccTime);
                        this.mWifiOpenSuccTime = now;
                    }
                    writeWifiStatToDataBase(true);
                    sendEmptyMessageDelayed(100, MSG_SEND_DELAY_DURATION);
                    break;
                case 101:
                    this.mCheckScanResults = true;
                    this.mCheckScanCnt = 0;
                    break;
                case 102:
                    getPowerSaveParam();
                    sendEmptyMessageDelayed(102, 60000);
                    break;
                case 200:
                    if (this.mFreshSpeedFlag) {
                        long loTotalSize = TrafficStats.getRxBytes("lo") + TrafficStats.getTxBytes("lo");
                        long wlanTrafficTotalByts = TrafficStats.getTxBytes(this.WLAN_IFACE) + TrafficStats.getRxBytes(this.WLAN_IFACE);
                        long currentWlanTrafficByts = Math.abs((wlanTrafficTotalByts - this.mPretotalBytes) - loTotalSize);
                        int value = ((int) currentWlanTrafficByts) / 3072;
                        int tPut = ((int) (8 * currentWlanTrafficByts)) / 3145728;
                        this.mHwMssHandler = HwMSSHandler.getInstance();
                        if (this.mHwMssHandler != null) {
                            this.mHwMssHandler.mssSwitchCheckTPut(tPut);
                        }
                        this.mPretotalBytes = wlanTrafficTotalByts - loTotalSize;
                        SSIDStat stat = this.mCurrentStat;
                        if (stat != null) {
                            if (stat.mMaxspeed >= value) {
                                value = stat.mMaxspeed;
                            }
                            stat.mMaxspeed = value;
                        }
                        sendEmptyMessageDelayed(200, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        break;
                    }
                    break;
                case 2000:
                    handleUpdateConnectState(msg.arg1 == 1);
                    break;
                case HwWifiLogMsgID.EVENT_UPDATE_DHCP_STATE /*2002*/:
                    handleUpdateDhcpState(msg.arg1);
                    break;
                case HwWifiLogMsgID.EVENT_UPDATE_WIFI_STATE /*2003*/:
                    ArrayList list = msg.obj;
                    handleUpdateWifiState(((Boolean) list.get(0)).booleanValue(), ((Boolean) list.get(1)).booleanValue());
                    break;
                case HwWifiLogMsgID.EVENT_WIFI_DNS_STATE /*2004*/:
                    handleWiFiDnsState(msg.arg1);
                    break;
                case HwWifiLogMsgID.EVENT_UPDATE_WIFI_SC_CNT /*2009*/:
                    handleUpdateScCHRCount(msg.arg1);
                    break;
                case HwWifiLogMsgID.EVENT_TRIGGER_TOTAL_TRAFFIC /*2010*/:
                    handleTriggerTotalTrafficBytes();
                    break;
                case HwWifiLogMsgID.EVENT_WIFI_WEB_STAT_REPORT /*2011*/:
                    Bundle data = msg.getData();
                    if (data.getInt("ReportType") == 0) {
                        handleWifiWebStatReport(data);
                        break;
                    }
                    break;
                case HwWifiLogMsgID.EVENT_WIFI_DNS_STAT_REPORT /*2012*/:
                    handleWifiDnsStatReport(msg.getData());
                    break;
                case HwWifiLogMsgID.EVENT_WIFI_FIRMLOG_TRIGGER /*2013*/:
                    HwWiFiLogUtils.getDefault().firmwareLog(msg.arg1 == 1);
                    break;
            }
        }
    }

    public void setWifiConnectType(String connectType) {
        LOGD("connect type is:" + connectType);
        HwWifiCHRStateManager hwWifiCHRStateManager = HwWifiCHRStateManagerImpl.getDefault();
        if (connectType != null && hwWifiCHRStateManager != null) {
            this.mConnectType = connectType;
            hwWifiCHRStateManager.updateConnectType(this.mConnectType);
        }
    }

    public void setApRoamingParam(byte publicEssCnt) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("setApRoamingParam, stat is null");
        } else {
            stat.mPublicEssCnt = publicEssCnt;
        }
    }

    public void setApFreqParam(short frequency) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("setApFreqParam, stat is null");
        } else {
            stat.mFreq = frequency;
        }
    }

    public void setRouterModelParam(String routerModel) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("setRouterModelParam, stat is null");
        } else {
            stat.mrouterModel = routerModel;
        }
    }

    public void updateMssSucCont(int triggerReason, int reasonCode) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            stat.mMssReasonCode = reasonCode;
            int[] -get76;
            if (reasonCode == 0) {
                stat.mMssSuccCnt = stat.mMssSuccCnt + 1;
                -get76 = stat.mMssSuccCntArray;
                -get76[triggerReason] = -get76[triggerReason] + 1;
            } else {
                stat.mMssFailCnt = stat.mMssFailCnt + 1;
                -get76 = stat.mMssFailCntArray;
                -get76[triggerReason] = -get76[triggerReason] + 1;
            }
        }
    }

    public void setAccessWebFlag(String subErrorCode) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            if (subErrorCode.equals("TRANS_TO_NO_INTERNET") || subErrorCode.equals("FIRST_CONNECT_NO_INTERNET") || subErrorCode.equals("ARP_UNREACHABLE")) {
                if (this.isScreen && this.mRssi >= RSSI_TRIGGER_THRESHOLD) {
                    stat.mAccWebFailCnt = stat.mAccWebFailCnt + 1;
                }
                this.mAccessWebFlag = false;
            }
            if (subErrorCode.equals("RESUME_INTERNET")) {
                this.mAccessWebFlag = true;
            }
            setConnectedDuration();
        }
    }

    private void setConnectedDuration() {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            long now = SystemClock.elapsedRealtime();
            if (this.connectedNetwork != 1) {
                stat.mConnectedTimeStamp = now;
                return;
            }
            if (now - stat.mConnectedTimeStamp > 0) {
                stat.mConnectedDuration = (int) (((long) stat.mConnectedDuration) + (now - stat.mConnectedTimeStamp));
                stat.mConnectedTimeStamp = now;
            }
            if (now - stat.mAccessWebFailTimeStamp > 0 && (this.mAccessWebFlag ^ 1) != 0) {
                stat.mAccessWebFailDura = (int) (((long) stat.mAccessWebFailDura) + (now - stat.mAccessWebFailTimeStamp));
                stat.mAccessWebFailTimeStamp = now;
            }
        }
    }

    public void setIPv4SuccFlag() {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            this.mAccessWebFlag = true;
            stat.mConnectedTimeStamp = SystemClock.elapsedRealtime();
            stat.mAccessWebFailTimeStamp = 0;
            if (!this.mFreshSpeedFlag) {
                this.mFreshSpeedFlag = true;
                removeMessages(200);
                sendEmptyMessageDelayed(200, 0);
            }
        }
    }

    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    public void readWifiCHRStat() {
    }

    public static HwWifiStatStore getDefault() {
        HwWifiStatStore hwWifiStatStore;
        synchronized (sLock) {
            hwWifiStatStore = sInstance;
        }
        return hwWifiStatStore;
    }

    private void rstDisconnectFlg() {
        this.disconnectDate = 0;
        this.isAbnormalDisconnect = false;
        this.disConnectSSID = "";
    }

    public void setMultiGWCount(byte count) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            stat.mMultiGWCount = count;
        }
    }

    public void incrWebSpeedStatus(int addNoUsrCnt, int addLongWaitingCnt) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            stat.mNoUserProcCnt = stat.mNoUserProcCnt + addNoUsrCnt;
            stat.mUserLongTimeWaitedCnt = stat.mUserLongTimeWaitedCnt + addLongWaitingCnt;
        }
    }

    public int getLastUpdDHCPReason() {
        return this.mLastUpdDHCPReason;
    }

    public int getLastUpdConnectReason() {
        return this.mLastConnetReason;
    }

    public void updateConnectCnt() {
        if (this.mCurrentStat == null || (this.mCurrentStat.mIsWifiproFlag ^ 1) == 0) {
            LOGW("mIsWifiproFlag is true or  mCurrentStat is null ");
            return;
        }
        SSIDStat sSIDStat = this.mCurrentStat;
        sSIDStat.mConnectTotalStartCnt++;
    }

    private void handleUpdateConnectState(boolean connected) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && !stat.mIsWifiproFlag) {
            LOGD("updateConnectState connected: " + connected + " mLastUpdDHCPReason:" + this.mLastUpdDHCPReason);
            long now = SystemClock.elapsedRealtime();
            if (connected) {
                if (this.mLastUpdDHCPReason == 2 || this.mLastUpdDHCPReason == 16 || this.mLastUpdDHCPReason == 9) {
                    stat.mConnectingDuration += (int) (now - stat.mAssocingTimestamp);
                    this.mWifiConnectedTimestamp = now;
                    if (this.isScreen && this.mRssi >= RSSI_TRIGGER_THRESHOLD) {
                        stat.mConnectTotalCnt++;
                        stat.mConnectedCnt++;
                    }
                    this.isConnectToNetwork = true;
                    if (this.isScreen && this.onScreenTimestamp > 0) {
                        stat.mOnScreenConnectedCnt = stat.mOnScreenConnectedCnt + 1;
                        stat.mOnScreenConnectDuration = stat.mOnScreenConnectDuration + ((int) (now - this.onScreenTimestamp));
                        this.onScreenTimestamp = 0;
                    }
                }
                getPowerSaveParam();
                triggerConnectedDuration(now, stat);
            } else {
                updateDisconnectCnt();
                if (this.mWifiConnectedTimestamp > 0) {
                    this.mWifiConnectedTimestamp = 0;
                    this.isAbnormalDisconnect = true;
                    if (this.isScreen) {
                        stat.mOnScreenAbDisconnectCnt = stat.mOnScreenAbDisconnectCnt + 1;
                    }
                }
                getPowerSaveParam();
                triggerConnectedDuration(now, stat);
                updateConnectInternetFailedType("CONNECT_INTERNET_INITIAL");
            }
        }
    }

    public void checkScanResults(List<ScanDetail> scanList) {
        if (!this.mCheckScanResults || this.mCheckScanCnt >= 5) {
            LOGD("not need to check scan results");
        } else if (this.mCheckStat == null) {
            LOGD("mCheckStat is null, return");
        } else {
            String ssid = this.mCheckStat.SSID;
            ssid = ssid.substring(1, ssid.length() - 1);
            this.mCheckScanCnt++;
            for (ScanDetail result : scanList) {
                if (result.getSSID().equals(ssid)) {
                    this.mCheckScanResults = false;
                    SSIDStat sSIDStat = this.mCheckStat;
                    sSIDStat.mConnectTotalCnt++;
                    break;
                }
            }
            if (this.mCheckScanResults && this.mCheckScanCnt >= 5) {
                LOGD("maybe connect to a AP in moving state");
                this.mCheckScanResults = false;
            }
        }
    }

    public void updateCHRConnectFailedCount(int type) {
        SSIDStat stat = null;
        if (type == 0) {
            stat = this.mCurrentStat;
        } else if (type == 1) {
            stat = this.mPreviousStat;
        }
        if (stat == null) {
            LOGD("updateCHRConnectFailedCount stat is null, return");
            return;
        }
        int rssiThreshold = -80;
        if (stat.mFreq > (short) 3000) {
            rssiThreshold = RSSI_TRIGGER_THRESHOLD_5G;
        }
        if (this.isScreen && this.mRssi >= rssiThreshold) {
            LOGD("updateCHRConnectFailedCount mConnectTotalCnt is " + stat.mConnectTotalCnt + ", mConnectType " + stat.mConnectType);
            stat.mConnectTotalCnt++;
        }
    }

    private SSIDStat geStatBySSID(String SSID) {
        for (int i = 0; i < this.mSSIDStatList.size(); i++) {
            SSIDStat item = (SSIDStat) this.mSSIDStatList.get(i);
            if (item.cmp(SSID)) {
                return item;
            }
        }
        return new SSIDStat(this, null);
    }

    private HwWifiDFTAPKAction getAPKActionByAPKName(String APKName) {
        synchronized (sAPKListLock) {
            int listSize = this.mAPKActionList.size();
            for (int i = 0; i < listSize; i++) {
                HwWifiDFTAPKAction item = (HwWifiDFTAPKAction) this.mAPKActionList.get(i);
                if (item.cmp(APKName)) {
                    return item;
                }
            }
            return new HwWifiDFTAPKAction();
        }
    }

    public void setAPKActionName(String APKName) {
        if (!TextUtils.isEmpty(APKName) && APKName.length() > 0 && (this.mCurAPKAction == null || !APKName.equals(this.mCurAPKAction.mApkName))) {
            this.mCurAPKAction = getAPKActionByAPKName(APKName);
            synchronized (sAPKListLock) {
                if (TextUtils.isEmpty(this.mCurAPKAction.mApkName)) {
                    LOGD("setAPKActionName mCurAPKAction is null: " + APKName);
                    this.mCurAPKAction.mApkName = APKName;
                    this.mAPKActionList.add(this.mCurAPKAction);
                }
            }
        }
    }

    public void setBackgroundScanReq(boolean isBackgroundReq) {
        this.mIsScanBackgroundReq = isBackgroundReq;
    }

    public int getWifiOpenStatDura() {
        return this.mWifiOpenStatDura;
    }

    public void updateApkChangewWifiStatus(int apkAction, String apkName, int count) {
        setAPKActionName(apkName);
        LOGD("updateApkChangewWifiStatus APKName is " + apkName + ", size " + this.mAPKActionList.size());
        if (this.mCurAPKAction == null) {
            LOGD("mCurAPKAction is null, return");
            return;
        }
        if (this.mCurrentStat != null) {
            this.mCurAPKAction.mAPSsid = this.mCurrentStat.SSID;
        }
        HwWifiDFTAPKAction hwWifiDFTAPKAction;
        switch (apkAction) {
            case 1:
                hwWifiDFTAPKAction = this.mCurAPKAction;
                hwWifiDFTAPKAction.mclosewifiCnt += count;
                break;
            case 2:
                hwWifiDFTAPKAction = this.mCurAPKAction;
                hwWifiDFTAPKAction.mdisableNetworkCnt += count;
                break;
            case 3:
                hwWifiDFTAPKAction = this.mCurAPKAction;
                hwWifiDFTAPKAction.mwifiScanModeCnt += count;
                break;
            case 4:
                long now = SystemClock.elapsedRealtime();
                synchronized (sAPKListLock) {
                    if (mScanTimeStamp.get(apkName) == null) {
                        mScanTimeStamp.put(apkName, Long.valueOf(now));
                    } else {
                        long scanInterval = now - ((Long) mScanTimeStamp.get(apkName)).longValue();
                        if (scanInterval < 5000) {
                            hwWifiDFTAPKAction = this.mCurAPKAction;
                            hwWifiDFTAPKAction.mscanFreqL1Cnt++;
                        } else if (scanInterval < 10000) {
                            hwWifiDFTAPKAction = this.mCurAPKAction;
                            hwWifiDFTAPKAction.mscanFreqL2Cnt++;
                        } else if (scanInterval < 30000) {
                            hwWifiDFTAPKAction = this.mCurAPKAction;
                            hwWifiDFTAPKAction.mscanFreqL3Cnt++;
                        }
                        mScanTimeStamp.put(apkName, Long.valueOf(now));
                    }
                }
                if (isConnectToNetwork()) {
                    hwWifiDFTAPKAction = this.mCurAPKAction;
                    hwWifiDFTAPKAction.mtriggerscanCnt += count;
                }
                if (this.isScreen) {
                    hwWifiDFTAPKAction = this.mCurAPKAction;
                    hwWifiDFTAPKAction.mscreenOnScanCnt += count;
                }
                if (!this.mIsScanBackgroundReq) {
                    hwWifiDFTAPKAction = this.mCurAPKAction;
                    hwWifiDFTAPKAction.mforeGroundScanCnt += count;
                }
                if (this.mIsWifiClose) {
                    hwWifiDFTAPKAction = this.mCurAPKAction;
                    hwWifiDFTAPKAction.mcloseScanCnt += count;
                }
                hwWifiDFTAPKAction = this.mCurAPKAction;
                hwWifiDFTAPKAction.mtotalScanCnt += count;
                break;
            case 5:
                hwWifiDFTAPKAction = this.mCurAPKAction;
                hwWifiDFTAPKAction.mopenwifiCnt += count;
                break;
            case 6:
                hwWifiDFTAPKAction = this.mCurAPKAction;
                hwWifiDFTAPKAction.mreconnectCnt += count;
                break;
            case 7:
                hwWifiDFTAPKAction = this.mCurAPKAction;
                hwWifiDFTAPKAction.mreassocCnt += count;
                break;
            case 8:
                hwWifiDFTAPKAction = this.mCurAPKAction;
                hwWifiDFTAPKAction.mdisconnectCnt += count;
                break;
        }
    }

    public void setAPSSID(String ssid) {
        if (!TextUtils.isEmpty(ssid) && ssid.length() > 0 && (this.mCurrentStat == null || !ssid.equals(this.mCurrentStat.SSID))) {
            if (!(this.mCurrentStat == null || (TextUtils.isEmpty(this.mCurrentStat.SSID) ^ 1) == 0)) {
                this.mPreviousStat = this.mCurrentStat;
            }
            this.mCurrentStat = geStatBySSID(ssid);
            if (TextUtils.isEmpty(this.mCurrentStat.SSID)) {
                this.mCurrentStat.SSID = ssid;
                this.mSSIDStatList.add(this.mCurrentStat);
            }
            this.mCurrentStat.mConnectType = this.mConnectType;
            LOGD("setAPSSID: " + ssid);
        }
    }

    public void updateAssocByABS() {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            stat.mAssocByABSCnt = stat.mAssocByABSCnt + 1;
        }
    }

    private void incrAccesFailedByPortal(int reason, boolean isFailedByPortal, SSIDStat stat) {
        if (isFailedByPortal) {
            switch (reason) {
                case 0:
                    stat.mAccessWebFailedPortal = stat.mAccessWebFailedPortal + 1;
                    break;
                case 2:
                    stat.mAccessWebRoamingFailedPortal = stat.mAccessWebRoamingFailedPortal + 1;
                    break;
                case 3:
                    stat.mAccessWebReDHCPFailedPortal = stat.mAccessWebReDHCPFailedPortal + 1;
                    break;
            }
        }
    }

    public void incrAccessWebRecord(int reason, boolean succ, boolean isFailedByPortal) {
        LOGD(" incrAccessWebRecord mCurrentStat= " + this.mCurrentStat + " succ=" + succ + "  reason=" + reason);
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            if (reason == 0) {
                stat.mAccessWEBCnt = stat.mAccessWEBCnt + 1;
            }
            if (succ) {
                switch (reason) {
                    case 0:
                        stat.mAccessWEBSuccCnt = stat.mAccessWEBSuccCnt + 1;
                        break;
                    case 2:
                        stat.mRoamingAccessWebSuccCnt = stat.mRoamingAccessWebSuccCnt + 1;
                        break;
                    case 3:
                        stat.mReDHCPAccessWebSuccCnt = stat.mReDHCPAccessWebSuccCnt + 1;
                        break;
                }
                triggerConnectedDuration(SystemClock.elapsedRealtime(), stat);
                return;
            }
            incrAccesFailedByPortal(reason, isFailedByPortal, stat);
        }
    }

    public void setApVendorInfo(String apVendorInfo) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("ignore setApVendorInfo because mCurrentStat is null");
            return;
        }
        if (apVendorInfo != null) {
            stat.apVendorInfo = apVendorInfo;
        }
    }

    public void setApencInfo(String strAP_key_mgmt, String strAP_eap) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("setApencInfo, stat is null");
            return;
        }
        stat.strAP_key_mgmt = strAP_key_mgmt;
        stat.strAP_eap = strAP_eap;
    }

    public void setCHRConnectingSartTimestamp(long connectingStartTimestamp) {
        if (connectingStartTimestamp > 0) {
            this.mConnectingStartTimestamp = connectingStartTimestamp;
        }
    }

    private void addReConnectCnt() {
        long now = SystemClock.elapsedRealtime();
        if (this.disconnectDate == 0 || now - this.disconnectDate >= 60000) {
            LOGD("addReConnectCnt return disconnectDate=" + this.disconnectDate + ", now=" + now);
            rstDisconnectFlg();
            return;
        }
        SSIDStat stat = geStatBySSID(this.disConnectSSID);
        if (TextUtils.isEmpty(stat.SSID)) {
            stat.SSID = this.disConnectSSID;
            this.mSSIDStatList.add(stat);
        }
        LOGD("addReConnectCnt return disConnectSSID=" + this.disConnectSSID + ", isAbnormalDisconnect=" + this.isAbnormalDisconnect);
        if (this.isAbnormalDisconnect) {
            stat.mGoodReConnectCnt = stat.mGoodReConnectCnt + 1;
        } else {
            stat.mWeakReConnectCnt = stat.mWeakReConnectCnt + 1;
        }
    }

    private void addReConnectSuccCnt() {
        long now = SystemClock.elapsedRealtime();
        if (this.disconnectDate == 0 || now - this.disconnectDate >= 60000) {
            LOGD("addReConnectCnt return disconnectDate=" + this.disconnectDate + ", now=" + now);
            rstDisconnectFlg();
            return;
        }
        SSIDStat stat = geStatBySSID(this.disConnectSSID);
        if (TextUtils.isEmpty(stat.SSID)) {
            LOGD("geStatBySSID null");
            stat.SSID = this.disConnectSSID;
            this.mSSIDStatList.add(stat);
        }
        LOGD("addReConnectSuccCnt  isAbnormalDisconnect=" + this.isAbnormalDisconnect);
        if (this.isAbnormalDisconnect) {
            if (this.isScreen || this.onScreenTimestamp > 0) {
                stat.mOnScreenReConnectedCnt = stat.mOnScreenReConnectedCnt + 1;
                stat.mOnScreenReConnectDuration = (int) (((long) stat.mOnScreenReConnectDuration) + (now - this.disconnectDate));
            }
            stat.mGoodReConnectSuccCnt = stat.mGoodReConnectSuccCnt + 1;
        } else {
            stat.mWeakReConnectSuccCnt = stat.mWeakReConnectSuccCnt + 1;
        }
    }

    private void triggerConnectedDuration(long now, SSIDStat stat) {
        if (stat != null && this.mWifiConnectedTimestamp > 0) {
            if ("FIRST_CONNECT_INTERNET_FAILED".equals(this.connectInternetFailedType)) {
                stat.mFirstConnInternetFailDuration = (int) (((long) stat.mFirstConnInternetFailDuration) + (now - this.mWifiConnectedTimestamp));
            }
            this.mWifiConnectedTimestamp = now;
        }
    }

    public void triggerConnectedDuration() {
        SSIDStat stat = this.mCurrentStat;
        long now = SystemClock.elapsedRealtime();
        if (stat != null) {
            setConnectedDuration();
            if (this.isScreen && this.mConWifi_ScreenOn_Timestamp != 0 && this.connectedNetwork == 1) {
                this.mwlanTotalConnDura += now - this.mConWifi_ScreenOn_Timestamp;
                this.mConWifi_ScreenOn_Timestamp = 0;
            }
            if (this.mWifiConnectedTimestamp > 0) {
                if ("FIRST_CONNECT_INTERNET_FAILED".equals(this.connectInternetFailedType)) {
                    stat.mFirstConnInternetFailDuration = (int) (((long) stat.mFirstConnInternetFailDuration) + (now - this.mWifiConnectedTimestamp));
                }
                this.mWifiConnectedTimestamp = now;
                triggerTotalTrafficBytes();
                triggerTotalConnetedDuration(this.connectedNetwork);
                writeWifiStatToDataBase(false);
            }
        }
    }

    public void triggerCHRConnectingDuration(long connectingDuration) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && connectingDuration > 0) {
            stat.mCHRConnectingDuration = (int) (((long) stat.mCHRConnectingDuration) + connectingDuration);
        }
    }

    public void setAbDisconnectFlg(String AP_SSID, int reasoncode) {
        SSIDStat stat = this.mCurrentStat;
        this.isAbnormalDisconnect = true;
        this.disConnectSSID = AP_SSID;
        this.disconnectDate = SystemClock.elapsedRealtime();
        if (stat != null) {
            if (this.isScreen && this.mRssi >= RSSI_TRIGGER_THRESHOLD && reasoncode != 3) {
                stat.mAbDisconnectCnt = stat.mAbDisconnectCnt + 1;
            }
            handleUpdateConnectState(false);
        }
    }

    public void updateCurrentConnectType(int type) {
        long now = SystemClock.elapsedRealtime();
        boolean commercialUser = HwWifiDFTConnManager.getInstance().isCommercialUser();
        switch (type) {
            case 0:
                triggerTotalTrafficBytes();
                triggerTotalConnetedDuration(this.connectedNetwork);
                setConnectedDuration();
                this.connectedNetwork = 0;
                removeMessages(100);
                removeMessages(102);
                if (this.isScreen && this.mConWifi_ScreenOn_Timestamp != 0) {
                    this.mwlanTotalConnDura += now - this.mConWifi_ScreenOn_Timestamp;
                    this.mConWifi_ScreenOn_Timestamp = 0;
                    return;
                }
                return;
            case 1:
                this.mPreTimestamp = now;
                this.connectedNetwork = 1;
                removeMessages(100);
                sendEmptyMessageDelayed(100, MSG_SEND_DELAY_DURATION);
                File file = new File(WIFI_TIM_PATH);
                if (!commercialUser && file.exists()) {
                    removeMessages(102);
                    sendEmptyMessageDelayed(102, 60000);
                }
                if (this.isScreen && this.mConWifi_ScreenOn_Timestamp == 0) {
                    this.mConWifi_ScreenOn_Timestamp = now;
                    return;
                }
                return;
            case 2:
                this.mPreTimestamp = now;
                triggerTotalTrafficBytes();
                this.connectedNetwork = 2;
                removeMessages(100);
                removeMessages(102);
                sendEmptyMessageDelayed(100, MSG_SEND_DELAY_DURATION);
                return;
            default:
                return;
        }
    }

    private void handleWifiWebStatReport(Bundle data) {
        LOGD("handleWebStatReport data is: " + data);
        if (this.mCurrentStat == null) {
            LOGD("mCurrentStat is null, return");
            return;
        }
        SSIDStat stat = this.mCurrentStat;
        stat.mDsStatRTT = stat.mDsStatRTT + data.getInt("RTT");
        stat.mDsStatWebDelay = stat.mDsStatWebDelay + data.getInt("WebDelay");
        stat.mDsStatSuccNum = stat.mDsStatSuccNum + data.getInt("SuccNum");
        stat.mDsStatFailNum = stat.mDsStatFailNum + data.getInt("FailNum");
        stat.mDsStatNoAckNum = stat.mDsStatNoAckNum + data.getInt("NoAckNum");
        stat.mDsStatTotalNum = stat.mDsStatTotalNum + data.getInt("TotalNum");
        stat.mDsStatTcpTotalNum = stat.mDsStatTcpTotalNum + data.getInt("TcpTotalNum");
        stat.mDsDelayL1 = stat.mDsDelayL1 + data.getInt("DelayL1");
        stat.mDsDelayL2 = stat.mDsDelayL2 + data.getInt("DelayL2");
        stat.mDsDelayL3 = stat.mDsDelayL3 + data.getInt("DelayL3");
        stat.mDsDelayL4 = stat.mDsDelayL4 + data.getInt("DelayL4");
        stat.mDsDelayL5 = stat.mDsDelayL5 + data.getInt("DelayL5");
        stat.mDsDelayL6 = stat.mDsDelayL6 + data.getInt("DelayL6");
        stat.mDsRTTL1 = stat.mDsRTTL1 + data.getInt("RTTL1");
        stat.mDsRTTL2 = stat.mDsRTTL2 + data.getInt("RTTL2");
        stat.mDsRTTL3 = stat.mDsRTTL3 + data.getInt("RTTL3");
        stat.mDsRTTL4 = stat.mDsRTTL4 + data.getInt("RTTL4");
        stat.mDsRTTL5 = stat.mDsRTTL5 + data.getInt("RTTL5");
        if (stat.mDsStatSuccNum == 0) {
            LOGD("stat.mDsStatSuccNum is 0, return");
            return;
        }
        int avgWebDelay = stat.mDsStatWebDelay / stat.mDsStatSuccNum;
        if (avgWebDelay > 400) {
            HwWifiCHRStateManager hwWifiCHRStateManager = HwWifiCHRStateManagerImpl.getDefault();
            if (hwWifiCHRStateManager != null) {
                LOGD("web delay over threshold, upload  WIFI_ACCESS_WEB_SLOWLY event avgWebDelay is " + avgWebDelay);
                hwWifiCHRStateManager.updateWifiException(102, Integer.toString(avgWebDelay));
            }
        }
    }

    private void handleWifiDnsStatReport(Bundle data) {
        LOGD("handleWifiDnsStatReport data is: " + data);
        if (this.mCurrentStat == null) {
            LOGD("mCurrentStat is null, return");
            return;
        }
        SSIDStat stat = this.mCurrentStat;
        stat.mDnsCount = stat.mDnsCount + data.getInt("dnsCount");
        stat.mDnsIpv6TimeOut = stat.mDnsIpv6TimeOut + data.getInt("dnsIpv6Timeout");
        stat.mDnsTotaldelay = stat.mDnsTotaldelay + data.getInt("dnsResponseTotalTime");
        stat.mDnsFailCount = stat.mDnsFailCount + data.getInt("dnsFailCount");
        stat.mDnsDelayL1 = stat.mDnsDelayL1 + data.getInt("dnsResponse20Count");
        stat.mDnsDelayL2 = stat.mDnsDelayL2 + data.getInt("dnsResponse150Count");
        stat.mDnsDelayL3 = stat.mDnsDelayL3 + data.getInt("dnsResponse500Count");
        stat.mDnsDelayL4 = stat.mDnsDelayL4 + data.getInt("dnsResponse1000Count");
        stat.mDnsDelayL5 = stat.mDnsDelayL5 + data.getInt("dnsResponse2000Count");
        stat.mDnsDelayL6 = stat.mDnsDelayL6 + data.getInt("dnsResponseOver2000Count");
    }

    public void handleTriggerTotalTrafficBytes() {
        long currentMobileTrafficBytes = (TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes()) - this.mPreMobileBytes;
        long currentWlanTrafficByts = (TrafficStats.getTxBytes(this.WLAN_IFACE) + TrafficStats.getRxBytes(this.WLAN_IFACE)) - this.mPreWLANBytes;
        if (currentMobileTrafficBytes > 0) {
            this.mMobileTotalTrafficBytes += currentMobileTrafficBytes;
        }
        if (currentWlanTrafficByts > 0) {
            this.mWlanTotalTrafficBytes += currentWlanTrafficByts;
        }
        if (TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes() > 0) {
            this.mPreMobileBytes = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
        }
        if (TrafficStats.getTxBytes(this.WLAN_IFACE) + TrafficStats.getRxBytes(this.WLAN_IFACE) > 0) {
            this.mPreWLANBytes = TrafficStats.getTxBytes(this.WLAN_IFACE) + TrafficStats.getRxBytes(this.WLAN_IFACE);
        }
    }

    public void triggerTotalConnetedDuration(int connectedType) {
        long now = SystemClock.elapsedRealtime();
        if (connectedType != 0) {
            switch (connectedType) {
                case 1:
                    long currentWlanConnectedDuration = now - this.mPreTimestamp;
                    if (currentWlanConnectedDuration > 0) {
                        this.mWlanTotalConnectedDuration += currentWlanConnectedDuration;
                        this.mPreTimestamp = now;
                        break;
                    }
                    return;
                case 2:
                    long currentMobileConnectedDuration = now - this.mPreTimestamp;
                    if (currentMobileConnectedDuration > 0) {
                        this.mMobileTotalConnectedDuration += currentMobileConnectedDuration;
                        this.mPreTimestamp = now;
                        break;
                    }
                    return;
            }
        }
    }

    public void setApMac(String apMac) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("setApMac, stat is null ,return");
        } else {
            stat.BSSID = apMac;
        }
    }

    private String maskMacAddress(String macAddress) {
        if (macAddress != null) {
            if (macAddress.split(HwQoEUtils.SEPARATOR).length >= 4) {
                return String.format("%s:%s:%s:%s:FF:FF", new Object[]{macAddress.split(HwQoEUtils.SEPARATOR)[0], macAddress.split(HwQoEUtils.SEPARATOR)[1], macAddress.split(HwQoEUtils.SEPARATOR)[2], macAddress.split(HwQoEUtils.SEPARATOR)[3]});
            }
        }
        return macAddress;
    }

    public void updateConnectInternetFailedType(String reasonType) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            if (reasonType.equals("CONNECT_INTERNET_INITIAL")) {
                this.connectInternetFailedType = "CONNECT_INTERNET_INITIAL";
            } else if (reasonType.equals("FIRST_CONNECT_INTERNET_FAILED")) {
                this.connectInternetFailedType = "FIRST_CONNECT_INTERNET_FAILED";
                stat.mFirstConnInternetFailCnt = stat.mFirstConnInternetFailCnt + 1;
            } else if (reasonType.equals("ONLY_THE_TX_NO_RX")) {
                this.connectInternetFailedType = "ONLY_THE_TX_NO_RX";
                stat.mOnlyTheTxNoRxCnt = stat.mOnlyTheTxNoRxCnt + 1;
            } else if (reasonType.equals("DNS_PARSE_FAILED")) {
                this.connectInternetFailedType = "DNS_PARSE_FAILED";
                stat.mDnsParseFailCnt = stat.mDnsParseFailCnt + 1;
            } else if (reasonType.equals("ARP_UNREACHABLE")) {
                this.connectInternetFailedType = "ARP_UNREACHABLE";
                stat.mArpUnreachableCnt = stat.mArpUnreachableCnt + 1;
            } else if (reasonType.equals("ARP_REASSOC_OK")) {
                this.connectInternetFailedType = "ARP_REASSOC_OK";
                stat.mArpReassocOkCnt = stat.mArpReassocOkCnt + 1;
            }
        }
    }

    public void handleSupplicantStateChange(SupplicantState state, boolean wifiprotempflag) {
        LOGD("handleSupplicantChange SupplicantState is: " + state + " wifi pro?: " + wifiprotempflag);
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("ignore handleSupplicantStateChange because stat is null, state:" + state);
            return;
        }
        if (state == SupplicantState.ASSOCIATING) {
            stat.mIsWifiproFlag = wifiprotempflag;
        }
        if (!stat.mIsWifiproFlag) {
            long now = SystemClock.elapsedRealtime();
            if (state == SupplicantState.ASSOCIATING) {
                this.mLastConnetReason = 1;
                stat.mAssocCnt++;
                addReConnectCnt();
                if (this.mLastWpaState == SupplicantState.COMPLETED) {
                    setWifiConnectType("REASSOC");
                }
                this.onScreenTimestamp = 0;
                if (this.isScreen) {
                    stat.mOnScreenConnectCnt = stat.mOnScreenConnectCnt + 1;
                    this.onScreenTimestamp = now;
                }
                this.mWifiConnectTimestamp = now;
                stat.mAssocingTimestamp = this.mWifiConnectTimestamp;
            } else if (state == SupplicantState.ASSOCIATED) {
                if (this.mLastWpaState == SupplicantState.ASSOCIATING) {
                    stat.mAssocSuccCnt++;
                    stat.mAssocDuration += (int) (now - this.mWifiConnectTimestamp);
                    stat.mAuthCnt++;
                    this.mWifiConnectTimestamp = now;
                } else if (this.mLastWpaState == SupplicantState.COMPLETED) {
                    this.mLastConnetReason = 2;
                    setWifiConnectType("ROAM_CONNECT");
                    stat.mRoamingCnt = stat.mRoamingCnt + 1;
                    this.mWifiConnectTimestamp = now;
                    getPowerSaveParam();
                }
            } else if (state == SupplicantState.COMPLETED) {
                if (1 == this.mLastConnetReason) {
                    stat.mAuthSuccCnt++;
                    stat.mAuthDuration += (int) (now - this.mWifiConnectTimestamp);
                    this.mWifiConnectTimestamp = now;
                    addReConnectSuccCnt();
                    rstDisconnectFlg();
                } else if (2 == this.mLastConnetReason) {
                    stat.mRoamingSuccCnt = stat.mRoamingSuccCnt + 1;
                    stat.mRoamingDuration = stat.mRoamingDuration + ((int) (now - this.mWifiConnectTimestamp));
                    this.mWifiConnectTimestamp = now;
                } else if (3 == this.mLastConnetReason) {
                    stat.mReKEYSuccCnt = stat.mReKEYSuccCnt + 1;
                    stat.mReKEYDuration = stat.mReKEYDuration + ((int) (now - this.mWifiConnectTimestamp));
                    this.mWifiConnectTimestamp = now;
                }
                this.mLastConnetReason = 0;
            } else if (state == SupplicantState.FOUR_WAY_HANDSHAKE || state == SupplicantState.GROUP_HANDSHAKE) {
                if (this.mLastWpaState == SupplicantState.COMPLETED) {
                    this.mLastConnetReason = 3;
                    stat.mReKEYCnt = stat.mReKEYCnt + 1;
                    this.mWifiConnectTimestamp = now;
                }
            } else if (state == SupplicantState.DISCONNECTED) {
                this.disConnectSSID = stat.SSID;
                this.mFreshSpeedFlag = false;
                setConnectedDuration();
                this.mAccessWebFlag = false;
                if (this.mLastConnetReason != 0) {
                    this.mWifiConnectTimestamp = now;
                    this.mLastConnetReason = 0;
                }
                if (this.mWifiConnectedTimestamp > 0) {
                    this.mWifiConnectedTimestamp = 0;
                    this.disconnectDate = now;
                    if (this.isScreen) {
                        stat.mOnScreenDisconnectCnt = stat.mOnScreenDisconnectCnt + 1;
                    }
                }
            }
            this.mLastWpaState = state;
            if (this.mWifiConnectTimestamp == now) {
                triggerConnectedDuration(now, stat);
            }
        }
    }

    public void handleUpdateDhcpState(int state) {
        LOGD("handleUpdateDhcpState " + state);
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && !stat.mIsWifiproFlag) {
            long now = SystemClock.elapsedRealtime();
            if (state == 0) {
                this.mDhcpTimestamp = now;
                stat.mDhcpCnt++;
            } else if (state == 2) {
                stat.mDhcpDuration += (int) (now - this.mDhcpTimestamp);
                stat.mDhcpSuccCnt++;
            } else if (state == 10) {
                this.mDhcpTimestamp = now;
                stat.mReDHCPCnt = stat.mReDHCPCnt + 1;
            } else if (state == 3) {
                stat.mReDHCPDuration = stat.mReDHCPDuration + ((int) (now - this.mDhcpTimestamp));
                stat.mReDHCPSuccCnt = stat.mReDHCPSuccCnt + 1;
            } else if (state == 4) {
                stat.mDhcpCnt++;
            } else if (state == 5) {
                stat.mReDHCPCnt = stat.mReDHCPCnt + 1;
            } else if (state == 8) {
                stat.mDhcpStaticCnt++;
            } else if (state == 9) {
                stat.mDhcpStaticSuccCnt++;
            } else if (state == 16) {
                stat.mDhcpAutoIpCnt++;
            } else {
                return;
            }
            this.mLastUpdDHCPReason = state;
            triggerConnectedDuration(now, stat);
        }
    }

    public void handleUpdateWifiState(boolean enable, boolean success) {
        if (enable) {
            Log.e(TAG, "updateWifiState true" + this.mOpenCnt);
            this.mOpenCnt++;
            if (success) {
                this.mOpenSuccCnt++;
                this.mOpenDuration += (int) (SystemClock.elapsedRealtime() - this.mWifiSwitchTimestamp);
                this.mIsWifiClose = false;
                this.mWifiOpenSuccTime = SystemClock.elapsedRealtime();
            }
            triggerConnectedDuration(SystemClock.elapsedRealtime(), this.mCurrentStat);
            writeWifiStatToDataBase(false);
        } else {
            Log.e(TAG, "updateWifiState false " + this.mCloseCnt);
            this.mCloseCnt++;
            if (success) {
                this.mCloseSuccCnt++;
                this.mCloseDuration += (int) (SystemClock.elapsedRealtime() - this.mWifiSwitchTimestamp);
                updateDisconnectCnt();
                this.mIsWifiClose = true;
                if (this.mWifiOpenSuccTime != 0) {
                    this.mWifiOpenStatDura += (int) (SystemClock.elapsedRealtime() - this.mWifiOpenSuccTime);
                    this.mWifiOpenSuccTime = 0;
                }
            }
            triggerConnectedDuration(SystemClock.elapsedRealtime(), this.mCurrentStat);
            updateConnectInternetFailedType("CONNECT_INTERNET_INITIAL");
            this.mWifiConnectedTimestamp = 0;
            writeWifiStatToDataBase(true);
        }
        this.mWifiSwitchTimestamp = SystemClock.elapsedRealtime();
    }

    public void handleWiFiDnsState(int netid) {
        LOGD("handleWiFiDnsState : " + netid);
        long now = SystemClock.elapsedRealtime();
        if (netid == 0) {
            this.mLastDnsStatReq = now;
        } else if (netid >= 0 && now - this.mLastDnsStatReq >= 300000) {
            this.mLastDnsStatReq = now;
            String dnsstats = "";
            try {
                dnsstats = HwFrameworkFactory.getHwInnerNetworkManager().getWiFiDnsStats(netid);
            } catch (ParcelableException e) {
                Log.e(TAG, "Exception in handleWiFiDnsStats: " + e.getMessage());
            } catch (ParcelFormatException e2) {
                Log.e(TAG, "Exception in handleWiFiDnsStats: " + e2.getMessage());
            } catch (Exception e3) {
                Log.e(TAG, "Exception in handleWiFiDnsStats: " + e3.getMessage());
            }
            if (!TextUtils.isEmpty(dnsstats)) {
                String[] stats = dnsstats.split(";");
                for (String statStr : stats) {
                    String[] statItem = statStr.split(",");
                    if (statItem.length == 6) {
                        try {
                            int reqcnt = Integer.parseInt(statItem[1]);
                            int fcnt = Integer.parseInt(statItem[2]);
                            int max = Integer.parseInt(statItem[3]);
                            int min = Integer.parseInt(statItem[4]);
                            this.mDnsReqCnt += reqcnt;
                            this.mDnsReqFail += fcnt;
                            this.mDnsMaxTime += max;
                            this.mDnsMinTime += min;
                            this.mDnsTotTime += Integer.parseInt(statItem[5]);
                        } catch (NumberFormatException e4) {
                            Log.e(TAG, "NumberFormatException in handleWiFiDnsStats, " + e4.getMessage());
                        }
                    }
                }
            }
        }
    }

    public void updateWifiTriggerState(boolean enable) {
        this.mWifiSwitchTimestamp = SystemClock.elapsedRealtime();
    }

    public void updateReasonCode(int EventId, int reasonCode) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && !stat.mIsWifiproFlag && EventId == 83 && reasonCode == 17) {
            stat.mAssocRejectAccessFullCnt = stat.mAssocRejectAccessFullCnt + 1;
        }
    }

    public void handleUpdateScCHRCount(int type) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            switch (type) {
                case 0:
                    stat.mDnsAbnormalCnt = stat.mDnsAbnormalCnt + 1;
                    break;
                case 1:
                    stat.mTcpRxAbnormalCnt = stat.mTcpRxAbnormalCnt + 1;
                    break;
                case 2:
                    stat.mRoamingAbnormalCnt = stat.mRoamingAbnormalCnt + 1;
                    break;
                case 3:
                    stat.mGatewayAbnormalCnt = stat.mGatewayAbnormalCnt + 1;
                    break;
                case 4:
                    stat.mDnsScSuccCnt = stat.mDnsScSuccCnt + 1;
                    break;
                case 5:
                    stat.mReDhcpScSuccCnt = stat.mReDhcpScSuccCnt + 1;
                    break;
                case 6:
                    stat.mStaticIpScSuccCnt = stat.mStaticIpScSuccCnt + 1;
                    break;
                case 7:
                    stat.mReassocScCnt = stat.mReassocScCnt + 1;
                    break;
                case 8:
                    stat.mResetScSuccCnt = stat.mResetScSuccCnt + 1;
                    break;
                case 9:
                    stat.mUserEnableStaticIpCnt = stat.mUserEnableStaticIpCnt + 1;
                    break;
                case 10:
                    stat.mAuthFailedAbnormalCnt = stat.mAuthFailedAbnormalCnt + 1;
                    break;
                case 11:
                    stat.mAssocRejectedAbnormalCnt = stat.mAssocRejectedAbnormalCnt + 1;
                    break;
                case 12:
                    stat.mDhcpFailedAbnormalCnt = stat.mDhcpFailedAbnormalCnt + 1;
                    break;
                case 13:
                    stat.mAppDisabledAbnromalCnt = stat.mAppDisabledAbnromalCnt + 1;
                    break;
                case 14:
                    stat.mAuthFailedScSuccCnt = stat.mAuthFailedScSuccCnt + 1;
                    break;
                case 15:
                    stat.mAssocRejectedScSuccCnt = stat.mAssocRejectedScSuccCnt + 1;
                    break;
                case 16:
                    stat.mDhcpFailedScSuccCnt = stat.mDhcpFailedScSuccCnt + 1;
                    break;
                case 17:
                    stat.mAppDisabledScSuccCnt = stat.mAppDisabledScSuccCnt + 1;
                    break;
                case 18:
                    stat.mReassocScConnectFailedCnt = stat.mReassocScConnectFailedCnt + 1;
                    break;
                case 19:
                    stat.mResetScConnectFailedCnt = stat.mResetScConnectFailedCnt + 1;
                    break;
                case 20:
                    stat.mDnsResetScSuccCnt = stat.mDnsResetScSuccCnt + 1;
                    break;
                case 21:
                    stat.mRoamingResetScSuccCnt = stat.mRoamingResetScSuccCnt + 1;
                    break;
                case 22:
                    stat.mGwResetScSuccCnt = stat.mGwResetScSuccCnt + 1;
                    break;
                case 23:
                    stat.mBlackListScSuccCnt = stat.mBlackListScSuccCnt + 1;
                    break;
                case 24:
                    stat.mBlackListAbnormalCnt = stat.mBlackListAbnormalCnt + 1;
                    break;
                case 25:
                    stat.mDhcpFailedStaticScSuccCnt = stat.mDhcpFailedStaticScSuccCnt + 1;
                    break;
                case 26:
                    stat.mStaticIpConflictedScAbnormalCnt = stat.mStaticIpConflictedScAbnormalCnt + 1;
                    break;
                case MessageUtil.MSG_WIFI_CONNECTING /*27*/:
                    stat.mRouterUnreachableCnt = stat.mRouterUnreachableCnt + 1;
                    break;
                case 28:
                    stat.mRouterDisplayNoInternetCnt = stat.mRouterDisplayNoInternetCnt + 1;
                    break;
                case 29:
                    stat.mInvalidIpScAbnormalCnt = stat.mInvalidIpScAbnormalCnt + 1;
                    break;
                case HwABSUtils.ABS_ASSOCIATE_TIMES_LOW_RATE /*30*/:
                    stat.mInvalidIpScSuccCnt = stat.mInvalidIpScSuccCnt + 1;
                    break;
            }
        }
    }

    public void updateDisconnectCnt() {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && this.isConnectToNetwork) {
            stat.mDisconnectCnt++;
            this.isConnectToNetwork = false;
        }
    }

    public boolean isConnectToNetwork() {
        return this.isConnectToNetwork;
    }

    private void writeWifiStatToDataBase(boolean isNeedTrigger) {
        LOGD("writeWifiStatToDataBase");
        HwWifiCHRStateManager hwWifiCHRStateManager = HwWifiCHRStateManagerImpl.getDefault();
        if (hwWifiCHRStateManager != null) {
            hwWifiCHRStateManager.notifyNcSTATDftEvent();
        } else {
            LOGD("hwWifiCHRStateManager is null");
        }
        if (isNeedTrigger) {
            triggerUploadIfNeed();
        }
    }

    private void triggerUploadIfNeed() {
        String softwareVersion = SystemProperties.get("ro.build.display.id", "");
        long now = System.currentTimeMillis();
        long minPeriod = MIN_PERIOD_TRIGGER_BETA;
        if (HwWifiDFTConnManager.getInstance().isCommercialUser()) {
            minPeriod = MIN_PERIOD_TRIGGER_CML;
        }
        if (now - this.mTimestamp >= minPeriod) {
            HwWifiCHRStateManager hwWifiCHRStateManager = HwWifiCHRStateManagerImpl.getDefault();
            if (hwWifiCHRStateManager != null) {
                hwWifiCHRStateManager.uploadWifiStat();
                hwWifiCHRStateManager.uploadDFTEvent(909001001, "");
                hwWifiCHRStateManager.uploadDFTEvent(909001002, "");
            }
            this.mTimestamp = now;
        }
    }

    public void clearStatInfo() {
        LOGD("clearStatInfo enter");
        long now = SystemClock.elapsedRealtime();
        this.mOpenCnt = 0;
        this.mOpenSuccCnt = 0;
        this.mCloseCnt = 0;
        this.mCloseSuccCnt = 0;
        this.mOpenDuration = 0;
        this.mCloseDuration = 0;
        this.mDnsReqCnt = 0;
        this.mDnsReqFail = 0;
        this.mDnsMaxTime = 0;
        this.mDnsMinTime = 0;
        this.mDnsTotTime = 0;
        this.mConnectedCnt = 0;
        this.mConnectTotalCnt = 0;
        this.mabDisconnCnt = 0;
        this.mconnectedDura = 0;
        this.mconnectingDura = 0;
        this.mdisconnCnt = 0;
        this.mWebFailDura = 0;
        this.mConnectTotalStartCnt = 0;
        this.maccWebFailCnt = (short) 0;
        this.mDsStatWebDelay = 0;
        this.mDsStatSuccNum = 0;
        this.mWlanTotalTrafficBytes = 0;
        this.mMobileTotalTrafficBytes = 0;
        this.mWlanTotalConnectedDuration = 0;
        this.mMobileTotalConnectedDuration = 0;
        this.mSSIDStatList.clear();
        synchronized (sAPKListLock) {
            this.mAPKActionList.clear();
            mScanTimeStamp.clear();
        }
        this.mWifiOpenStatDura = 0;
        HwWifiDFTUtilImpl hwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
        this.mwlanTotalConnDura = 0;
        if (this.isScreen && this.connectedNetwork == 1) {
            this.mConWifi_ScreenOn_Timestamp = now;
        }
        hwWifiDFTUtilImpl.clearSwCnt();
        if (this.mCurrentStat != null) {
            String currSSID = this.mCurrentStat.SSID;
            this.mCurrentStat = new SSIDStat(this, null);
            this.mPreviousStat = new SSIDStat(this, null);
            this.mPreviousStat.SSID = currSSID;
            this.mSSIDStatList.add(this.mPreviousStat);
            setAPSSID(currSSID);
        }
        if (this.mCurAPKAction != null) {
            String apkName = this.mCurAPKAction.mApkName;
            this.mCurAPKAction = new HwWifiDFTAPKAction();
            setAPKActionName(apkName);
        }
        this.mWeChartTimes = 0;
        this.mWeChartLowRssiTimes = 0;
        this.mWeChartDisconnectTimes = 0;
        this.mWeChartBackGroundTimes = 0;
        this.mWeChartVideoTimes = 0;
    }

    private void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    private void LOGW(String msg) {
        Log.e(TAG, msg);
    }

    public void getWifiDFT2StabilityStat(HwWifiDFT2StabilityStat hwWifiDFT2StabilityStat) {
        int i = 1;
        HwWifiCHRStateManager hwWifiCHRStateManager = HwWifiCHRStateManagerImpl.getDefault();
        updateDFT2StabilityParam();
        try {
            int i2;
            HwWifiDFTUtilImpl hwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
            hwWifiDFT2StabilityStat.mabDisconnCnt = this.mabDisconnCnt;
            hwWifiDFT2StabilityStat.maccWebFailCnt = this.maccWebFailCnt;
            hwWifiDFT2StabilityStat.maccWebSlowCnt = (short) 1;
            hwWifiDFT2StabilityStat.mcloseCnt = this.mCloseCnt;
            hwWifiDFT2StabilityStat.mcloseDura = this.mCloseDuration;
            hwWifiDFT2StabilityStat.mcloseSucCnt = this.mCloseSuccCnt;
            hwWifiDFT2StabilityStat.mconnectedCnt = this.mConnectedCnt;
            hwWifiDFT2StabilityStat.mconnectedDura = (int) this.mWlanTotalConnectedDuration;
            hwWifiDFT2StabilityStat.mconnectingDura = this.mconnectingDura;
            hwWifiDFT2StabilityStat.mconnectTotalCnt = this.mConnectTotalCnt;
            hwWifiDFT2StabilityStat.mdisconnCnt = this.mdisconnCnt;
            if (hwWifiDFTUtilImpl.getWifiAlwaysScanState()) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            hwWifiDFT2StabilityStat.misScanAlwaysAvailble = i2;
            if (hwWifiDFTUtilImpl.getWifiNetworkNotificationState()) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            hwWifiDFT2StabilityStat.misWifiNotifationOn = i2;
            if (!hwWifiDFTUtilImpl.getWifiProState()) {
                i = 0;
            }
            hwWifiDFT2StabilityStat.misWifiProOn = i;
            hwWifiDFT2StabilityStat.mmobileTotalConnDura = (int) this.mMobileTotalConnectedDuration;
            hwWifiDFT2StabilityStat.mmobileTotalTraffic = (int) this.mMobileTotalTrafficBytes;
            hwWifiDFT2StabilityStat.mopenCnt = this.mOpenCnt;
            hwWifiDFT2StabilityStat.mopenDura = this.mOpenDuration;
            hwWifiDFT2StabilityStat.mopenSucCnt = this.mOpenSuccCnt;
            if (hwWifiCHRStateManager != null) {
                hwWifiDFT2StabilityStat.mrepeaterWorkDura = (int) hwWifiCHRStateManager.getWifiRepeaterWorkingTime();
                hwWifiDFT2StabilityStat.mrepeterClientMaxCnt = (byte) hwWifiCHRStateManager.getRepeaterMaxClientCount();
                hwWifiDFT2StabilityStat.mrepeterConnFailCnt = (byte) hwWifiCHRStateManager.getRepeaterConnFailedCount();
                hwWifiDFT2StabilityStat.mrepeterDiffFreqDura = hwWifiCHRStateManager.getDiffFreqStationRepeaterDuration();
                hwWifiDFT2StabilityStat.mrepeterOpenCnt = 1;
                hwWifiDFT2StabilityStat.mrepeterOpenSuccCnt = hwWifiCHRStateManager.getWifiRepeaterOpenedCount();
            }
            hwWifiDFT2StabilityStat.mscanAlwaysSwCnt = hwWifiDFTUtilImpl.getScanAlwaysSwCnt();
            hwWifiDFT2StabilityStat.mwebFailDura = this.mWebFailDura;
            hwWifiDFT2StabilityStat.mwebSlowDura = 1;
            hwWifiDFT2StabilityStat.mwifiNotifationSwCnt = hwWifiDFTUtilImpl.getWifiNotifationSwCnt();
            hwWifiDFT2StabilityStat.mwifiProSwCnt = hwWifiDFTUtilImpl.getWifiProSwcnt();
            hwWifiDFT2StabilityStat.mwifiSleepPolicy = hwWifiDFTUtilImpl.getWifiSleepPolicyState();
            hwWifiDFT2StabilityStat.mwifiSleepSwCnt = hwWifiDFTUtilImpl.getWifiSleepSwCnt();
            hwWifiDFT2StabilityStat.mwifiToPDP = hwWifiDFTUtilImpl.getWifiToPdpState();
            hwWifiDFT2StabilityStat.mwifiToPDPSwCnt = hwWifiDFTUtilImpl.getWifiToPdpSwCnt();
            hwWifiDFT2StabilityStat.mwlanTotalConnDura = (int) this.mwlanTotalConnDura;
            hwWifiDFT2StabilityStat.mwlanTotalTraffic = (int) this.mWlanTotalTrafficBytes;
            hwWifiDFT2StabilityStat.mDsStatSuccNum = this.mDsStatSuccNum;
            hwWifiDFT2StabilityStat.mDsStatWebDelay = this.mDsStatWebDelay;
            hwWifiDFT2StabilityStat.mgameCnt = this.mGameCnt;
            hwWifiDFT2StabilityStat.mgameName = this.mGameName;
            System.arraycopy(this.mGameLag, 0, hwWifiDFT2StabilityStat.mGameLag, 0, 4);
            hwWifiDFT2StabilityStat.mevaluSuccCnt = this.mWeChartTimes;
            hwWifiDFT2StabilityStat.mevaluFailCnt = this.mWeChartLowRssiTimes;
            hwWifiDFT2StabilityStat.mevaluTimeL1 = this.mWeChartDisconnectTimes;
            hwWifiDFT2StabilityStat.mevaluTimeL2 = this.mWeChartBackGroundTimes;
            hwWifiDFT2StabilityStat.mevaluTimeL3 = this.mWeChartVideoTimes;
            hwWifiDFT2StabilityStat.mtemCtrlCnt = this.mtemCtrlCnt;
            hwWifiDFT2StabilityStat.mtemCtrlDura = this.mtemCtrlDura;
            hwWifiDFT2StabilityStat.mmaxTem = this.mmaxTem;
            hwWifiDFT2StabilityStat.mminDutyCycleCnt = this.mminDutyCycleCnt;
            hwWifiDFT2StabilityStat.mExtendMaxTemCnt = this.mExtendMaxTemCnt;
        } catch (ClassCastException e) {
            Log.e(TAG, "getWifiDFT2StabilityStat error. " + e.getMessage());
        } catch (Exception e2) {
            Log.e(TAG, "getWifiDFT2StabilityStat error. " + e2.getMessage());
        }
    }

    public void txPwrBoostChrStatic(Boolean txBoostEnable, int RTT, int RTTCnt, int txGood, int txBad, int TxRetry) {
        int signalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mRssi);
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            Log.d(TAG, "txPwrBoostChrStatic stat is null, return");
            return;
        }
        int[] -get116;
        if (txBoostEnable.booleanValue()) {
            -get116 = stat.mTxBoostOnRTT;
            -get116[signalLevel] = -get116[signalLevel] + RTT;
            -get116 = stat.mTxBoostOnRTTCnt;
            -get116[signalLevel] = -get116[signalLevel] + RTTCnt;
            -get116 = stat.mTxBoostOntxBad;
            -get116[signalLevel] = -get116[signalLevel] + txBad;
            -get116 = stat.mTxBoostOntxGood;
            -get116[signalLevel] = -get116[signalLevel] + txGood;
            -get116 = stat.mTxBoostOnTxRetry;
            -get116[signalLevel] = -get116[signalLevel] + TxRetry;
        } else {
            -get116 = stat.mTxBoostOffRTT;
            -get116[signalLevel] = -get116[signalLevel] + RTT;
            -get116 = stat.mTxBoostOffRTTCnt;
            -get116[signalLevel] = -get116[signalLevel] + RTTCnt;
            -get116 = stat.mTxBoostOfftxBad;
            -get116[signalLevel] = -get116[signalLevel] + txBad;
            -get116 = stat.mTxBoostOfftxGood;
            -get116[signalLevel] = -get116[signalLevel] + txGood;
            -get116 = stat.mTxBoostOffTxRetry;
            -get116[signalLevel] = -get116[signalLevel] + TxRetry;
        }
    }

    public void getTemperatureCtrlParam() {
        short s = (short) 0;
        Log.e(TAG, "getTemperatureCtrlParam Enter");
        List<String> temStatLines = HwCHRWifiFile.getFileResult(WIFI_TEM_CTRL_PATH);
        if (temStatLines.size() != 0) {
            short parseShort;
            int parseInt;
            Log.e(TAG, "temStatLines size " + temStatLines.size());
            String tem_ctrl_cnt = getTempCtrlParamFromString(temStatLines, TEM_CTRL_CNT);
            String tem_ctrl_times = getTempCtrlParamFromString(temStatLines, TEM_CTRL_TIMES);
            String maxtem = getTempCtrlParamFromString(temStatLines, MAXTEM);
            String mindutycycle = getTempCtrlParamFromString(temStatLines, MINDUTYCYCLE);
            String exceed_warn_tem_cnt = getTempCtrlParamFromString(temStatLines, EXCEED_WARN_TEM_CNT);
            if (tem_ctrl_cnt != null) {
                try {
                    parseShort = Short.parseShort(tem_ctrl_cnt);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "getTemperatureCtrlParam error. " + e.getMessage());
                }
            } else {
                parseShort = (short) 0;
            }
            this.mtemCtrlCnt = parseShort;
            if (tem_ctrl_times != null) {
                parseInt = Integer.parseInt(tem_ctrl_times);
            } else {
                parseInt = 0;
            }
            this.mtemCtrlDura = parseInt;
            if (maxtem != null) {
                parseShort = Short.parseShort(maxtem);
            } else {
                parseShort = (short) 0;
            }
            this.mmaxTem = parseShort;
            if (mindutycycle != null) {
                parseShort = Short.parseShort(mindutycycle);
            } else {
                parseShort = (short) 0;
            }
            this.mminDutyCycleCnt = parseShort;
            if (exceed_warn_tem_cnt != null) {
                s = Short.parseShort(exceed_warn_tem_cnt);
            }
            this.mExtendMaxTemCnt = s;
        }
    }

    private void getWifiTimParam() {
        int i = 0;
        List<String> temStatLines = HwCHRWifiFile.getFileResult(WIFI_TIM_PATH);
        if (temStatLines.size() == 0) {
            Log.e(TAG, "temStatLines is empty");
            return;
        }
        int totalTimEventCnt;
        int parseInt;
        if (this.mHwQoEService == null) {
            this.mHwQoEService = HwQoEService.getInstance();
        }
        String timEventCnt = getTempCtrlParamFromString(temStatLines, "tim_event_cnt");
        String pmSleepRet = getTempCtrlParamFromString(temStatLines, "pm2_sleep_ret");
        String bcnInterval = getTempCtrlParamFromString(temStatLines, "bcn_interval");
        String dtim = getTempCtrlParamFromString(temStatLines, "dtim_period");
        String sendSegs = getTempCtrlParamFromString(temStatLines, "send_package_cnt");
        String rcvSegs = getTempCtrlParamFromString(temStatLines, "rcv_package_cnt");
        if (timEventCnt != null) {
            try {
                totalTimEventCnt = Integer.parseInt(timEventCnt);
            } catch (NumberFormatException e) {
                Log.e(TAG, "getTemperatureCtrlParam error. " + e.getMessage());
            }
        } else {
            totalTimEventCnt = 0;
        }
        int totalSendSegs = sendSegs != null ? Integer.parseInt(sendSegs) : 0;
        int totalRcvSegs = rcvSegs != null ? Integer.parseInt(rcvSegs) : 0;
        this.mTimEventCnt = totalTimEventCnt < this.mtotalTimEventCnt ? totalTimEventCnt : totalTimEventCnt - this.mtotalTimEventCnt;
        if (pmSleepRet != null) {
            parseInt = Integer.parseInt(pmSleepRet);
        } else {
            parseInt = 0;
        }
        this.mPmSleepRet = parseInt;
        if (bcnInterval != null) {
            parseInt = Integer.parseInt(bcnInterval);
        } else {
            parseInt = 0;
        }
        this.mBcnInterval = parseInt;
        if (dtim != null) {
            i = Integer.parseInt(dtim);
        }
        this.mDtim = i;
        this.mSendSegs = totalSendSegs < this.mtotalSendSegs ? totalSendSegs : totalSendSegs - this.mtotalSendSegs;
        this.mRcvSegs = totalRcvSegs < this.mtotalRcvSegs ? totalRcvSegs : totalRcvSegs - this.mtotalRcvSegs;
        this.mtotalTimEventCnt = totalTimEventCnt;
        this.mtotalSendSegs = totalSendSegs;
        this.mtotalRcvSegs = totalRcvSegs;
        if (this.mHwQoEService != null) {
            this.mHwQoEService.updateWifiTimParam(this.mDtim, this.mBcnInterval);
        }
    }

    public String getTopPkgName() {
        ActivityManager am = (ActivityManager) mContextRef.getSystemService("activity");
        String topTaskPkg = HwCHRWifiCPUUsage.COL_SEP;
        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            return ((RunningTaskInfo) tasks.get(0)).topActivity.getPackageName();
        }
        Log.d(TAG, "getTopPkgName is " + topTaskPkg);
        return topTaskPkg;
    }

    private void getPowerSaveParam() {
        StringBuilder sBuffer = new StringBuilder();
        long rttDura = 0;
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String foreGroundPkgName = getTopPkgName();
        if (this.mNetstatManager == null) {
            this.mNetstatManager = new HWNetstatManager(mContextRef);
        }
        mUidTcpStat = HwUidTcpMonitor.getInstance(mContextRef).getUidTcpStatistics();
        try {
            ApplicationInfo ai = mContextRef.getPackageManager().getApplicationInfo(foreGroundPkgName, 0);
            Log.d(TAG, "TopPkgName's uid is  " + ai.uid);
            if (mUidTcpStat.get(Integer.valueOf(ai.uid)) != null) {
                UidTcpStatInfo uidTcpStat = (UidTcpStatInfo) mUidTcpStat.get(Integer.valueOf(ai.uid));
                if (uidTcpStat.mRttSegs != 0) {
                    rttDura = uidTcpStat.mRttDuration / uidTcpStat.mRttSegs;
                }
            }
            long totalRxSpeed = (this.mNetstatManager.getRxBytes() * 8) / 5;
            long totalTxSpeed = (this.mNetstatManager.getTxBytes() * 8) / 5;
            long foreGroundRxSpeed = (this.mNetstatManager.getUidRxBytes(ai.uid) * 8) / 5;
            long foreGroundTxSpeed = (this.mNetstatManager.getUidTxBytes(ai.uid) * 8) / 5;
            getWifiTimParam();
            if (this.mCurrentStat != null) {
                sBuffer.append(this.mCurrentStat.SSID).append(',');
            } else {
                sBuffer.append("null").append(',');
            }
            int appType = getAppType(foreGroundPkgName);
            sBuffer.append(timeStamp).append(',');
            sBuffer.append(String.valueOf(this.mBcnInterval)).append(',');
            sBuffer.append(String.valueOf(this.mDtim)).append(',');
            sBuffer.append(String.valueOf(this.isScreen)).append(',');
            sBuffer.append(foreGroundPkgName).append(',');
            sBuffer.append(String.valueOf(appType)).append(',');
            sBuffer.append(String.valueOf(rttDura)).append(',');
            sBuffer.append(String.valueOf(foreGroundRxSpeed)).append(',');
            sBuffer.append(String.valueOf(foreGroundTxSpeed)).append(',');
            sBuffer.append(String.valueOf(totalRxSpeed)).append(',');
            sBuffer.append(String.valueOf(totalTxSpeed)).append(',');
            sBuffer.append(String.valueOf(this.mPmSleepRet)).append(',');
            sBuffer.append(String.valueOf(this.mTimEventCnt)).append(',');
            sBuffer.append(String.valueOf(this.mRcvSegs)).append(',');
            sBuffer.append(String.valueOf(this.mSendSegs));
            saveLowPowerLogs(sBuffer.toString());
        } catch (NameNotFoundException e) {
            Log.e(TAG, "PackageManager NameNotFoundException");
        }
    }

    private int getAppType(String pkgName) {
        if ("com.tencent.tmgp.sgame".equals(pkgName)) {
            return this.mgameKogScene;
        }
        if (WECHAT_NAME.equals(pkgName)) {
            return this.mweChatScene;
        }
        if (this.mAppTypeRecoManager == null) {
            this.mAppTypeRecoManager = AppTypeRecoManager.getInstance();
        }
        return this.mAppTypeRecoManager.getAppType(pkgName);
    }

    public void setGameKogScene(int gameKogScene) {
        this.mgameKogScene = gameKogScene;
    }

    public void setWeChatScene(int weChatScene) {
        this.mweChatScene = weChatScene;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A:{SYNTHETIC, Splitter: B:17:0x0036} */
    /* JADX WARNING: Removed duplicated region for block: B:76:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003b A:{Catch:{ IOException -> 0x00ef }} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0059 A:{SYNTHETIC, Splitter: B:31:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x005e A:{Catch:{ IOException -> 0x00fb }} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0059 A:{SYNTHETIC, Splitter: B:31:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x005e A:{Catch:{ IOException -> 0x00fb }} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A:{SYNTHETIC, Splitter: B:17:0x0036} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003b A:{Catch:{ IOException -> 0x00ef }} */
    /* JADX WARNING: Removed duplicated region for block: B:76:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A:{SYNTHETIC, Splitter: B:17:0x0036} */
    /* JADX WARNING: Removed duplicated region for block: B:76:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003b A:{Catch:{ IOException -> 0x00ef }} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0059 A:{SYNTHETIC, Splitter: B:31:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x005e A:{Catch:{ IOException -> 0x00fb }} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A:{SYNTHETIC, Splitter: B:17:0x0036} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003b A:{Catch:{ IOException -> 0x00ef }} */
    /* JADX WARNING: Removed duplicated region for block: B:76:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0059 A:{SYNTHETIC, Splitter: B:31:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x005e A:{Catch:{ IOException -> 0x00fb }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveLowPowerLogs(String lowPowerLogs) {
        Throwable th;
        File file;
        File file2;
        FileOutputStream fout = null;
        FileInputStream fin = null;
        boolean fileExist = true;
        try {
            File file3;
            File dir = new File(IMONITOR_WIFI_LOWPOWER_DIR);
            try {
                file3 = new File(WIFI_LOWPOWER_NAME);
            } catch (IOException e) {
                try {
                    Log.e(TAG, "saveLowPowerLogs fail");
                    if (fout != null) {
                        try {
                            fout.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "close FileOutputStream fail");
                            return;
                        }
                    }
                    if (fin != null) {
                        fin.close();
                        return;
                    }
                    return;
                } catch (Throwable th2) {
                    th = th2;
                    if (fout != null) {
                    }
                    if (fin != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                file = dir;
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "close FileOutputStream fail");
                        throw th;
                    }
                }
                if (fin != null) {
                    fin.close();
                }
                throw th;
            }
            try {
                if (dir.exists() || dir.mkdirs()) {
                    if (!file3.exists()) {
                        if (file3.createNewFile()) {
                            fileExist = false;
                        } else {
                            throw new IOException("Create new file failed");
                        }
                    }
                    FileOutputStream fout2 = new FileOutputStream(WIFI_LOWPOWER_NAME, true);
                    try {
                        FileInputStream fin2 = new FileInputStream(WIFI_LOWPOWER_NAME);
                        if (fileExist) {
                            try {
                                if (fin2.available() + lowPowerLogs.length() < WIFI_LOWPOWER_MAX_LENGTH) {
                                    fout2.write(lowPowerLogs.getBytes("UTF-8"));
                                    fout2.write("\r\n".getBytes("UTF-8"));
                                } else {
                                    Log.e(TAG, "exceed the file's max size, not write continue");
                                }
                            } catch (IOException e4) {
                                file2 = file3;
                                file = dir;
                                fin = fin2;
                                fout = fout2;
                                Log.e(TAG, "saveLowPowerLogs fail");
                                if (fout != null) {
                                }
                                if (fin != null) {
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                file = dir;
                                fin = fin2;
                                fout = fout2;
                                if (fout != null) {
                                }
                                if (fin != null) {
                                }
                                throw th;
                            }
                        }
                        fout2.write(WIFI_LOWPOWER_ITEM.getBytes("UTF-8"));
                        fout2.write(lowPowerLogs.getBytes("UTF-8"));
                        fout2.write("\r\n".getBytes("UTF-8"));
                        if (fout2 != null) {
                            try {
                                fout2.close();
                            } catch (IOException e5) {
                                Log.e(TAG, "close FileOutputStream fail");
                            }
                        }
                        if (fin2 != null) {
                            fin2.close();
                        }
                        file2 = file3;
                        return;
                    } catch (IOException e6) {
                        file = dir;
                        fout = fout2;
                        Log.e(TAG, "saveLowPowerLogs fail");
                        if (fout != null) {
                        }
                        if (fin != null) {
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        fout = fout2;
                        if (fout != null) {
                        }
                        if (fin != null) {
                        }
                        throw th;
                    }
                }
                throw new IOException("Create file path failed");
            } catch (IOException e7) {
                file = dir;
                Log.e(TAG, "saveLowPowerLogs fail");
                if (fout != null) {
                }
                if (fin != null) {
                }
            } catch (Throwable th6) {
                th = th6;
                file2 = file3;
                file = dir;
                if (fout != null) {
                }
                if (fin != null) {
                }
                throw th;
            }
        } catch (IOException e8) {
            Log.e(TAG, "saveLowPowerLogs fail");
            if (fout != null) {
            }
            if (fin != null) {
            }
        }
    }

    private String getTempCtrlParamFromString(List<String> list, String find) {
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            String temstring = (String) list.get(i);
            if (temstring != null && temstring.contains(find)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            String[] value = ((String) list.get(index)).split(HwQoEUtils.SEPARATOR);
            if (value.length == 2) {
                return value[1];
            }
        }
        return null;
    }

    public void updateGameBoostStatic(String gameName, boolean isNewGame) {
        Log.d(TAG, "updateGameBoostStatic in, gameName " + gameName + ", isNewGame" + isNewGame);
        int signalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mRssi);
        this.mGameName = gameName;
        if (isNewGame) {
            this.mGameCnt++;
        }
        if (signalLevel <= 0 || signalLevel > 4) {
            Log.d(TAG, "updateGameBoostStatic signalLevel size is wrong, not count");
            return;
        }
        int[] iArr = this.mGameLag;
        signalLevel--;
        iArr[signalLevel] = iArr[signalLevel] + 1;
    }

    public void updateDFT2StabilityParam() {
        Log.d(TAG, "updateDFT2StabilityParam mSSIDStatList size is ." + this.mSSIDStatList.size());
        for (int i = 0; i < this.mSSIDStatList.size(); i++) {
            SSIDStat stat = (SSIDStat) this.mSSIDStatList.get(i);
            this.mConnectedCnt += stat.mConnectedCnt;
            this.mConnectTotalCnt += stat.mConnectTotalCnt;
            this.mabDisconnCnt += stat.mAbDisconnectCnt;
            this.mconnectedDura += stat.mConnectedDuration;
            this.mconnectingDura += stat.mConnectingDuration;
            this.mdisconnCnt += stat.mDisconnectCnt;
            this.mConnectTotalStartCnt += stat.mConnectTotalStartCnt;
            this.maccWebFailCnt = (short) (this.maccWebFailCnt + stat.mAccWebFailCnt);
            this.mDsStatWebDelay += stat.mDsStatWebDelay;
            this.mDsStatSuccNum += stat.mDsStatSuccNum;
            this.mWebFailDura += stat.mAccessWebFailDura;
        }
    }

    public void getWifiDFTAPKAction(List<HwWifiDFTAPKAction> listHwWifiDFTAPKAction) {
        synchronized (sAPKListLock) {
            int listSize = this.mAPKActionList.size();
            for (int i = 0; i < listSize; i++) {
                listHwWifiDFTAPKAction.add((HwWifiDFTAPKAction) this.mAPKActionList.get(i));
            }
        }
    }

    public void getWifiDFT2StabilitySsidStat(List<HwWifiDFT2StabilitySsidStat> listHwWifiDFT2StabilitySsidStat) {
        int i = 0;
        while (i < this.mSSIDStatList.size()) {
            try {
                int i2;
                SSIDStat stat = (SSIDStat) this.mSSIDStatList.get(i);
                HwWifiDFTUtilImpl hwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
                HwWifiDFT2StabilitySsidStat hwWifiDFT2StabilitySsidStat = new HwWifiDFT2StabilitySsidStat();
                hwWifiDFT2StabilitySsidStat.mAPSsid = stat.SSID;
                hwWifiDFT2StabilitySsidStat.mabDisconnCnt = stat.mAbDisconnectCnt;
                hwWifiDFT2StabilitySsidStat.mABSAssocCnt = stat.mABSAssociateTimes;
                hwWifiDFT2StabilitySsidStat.mABSAssocFailCnt = stat.mABSAssociateFailedTimes;
                hwWifiDFT2StabilitySsidStat.mABSMimoDura = (int) stat.mABSMimoTime;
                hwWifiDFT2StabilitySsidStat.mABSMimoScreenOnDura = (int) stat.mABSSisoScreenOnTime;
                hwWifiDFT2StabilitySsidStat.mABSSisoDura = (int) stat.mABSSisoTime;
                hwWifiDFT2StabilitySsidStat.mABSSisoScreenOnDura = (int) stat.mABSSisoScreenOnTime;
                hwWifiDFT2StabilitySsidStat.maccWebCnt = stat.mAccessWEBCnt;
                hwWifiDFT2StabilitySsidStat.maccWebFailCnt = (short) stat.mAccWebFailCnt;
                hwWifiDFT2StabilitySsidStat.maccWebFailPortal = stat.mAccessWebFailedPortal;
                hwWifiDFT2StabilitySsidStat.maccWebReDHCPFailPortal = stat.mAccessWebReDHCPFailedPortal;
                hwWifiDFT2StabilitySsidStat.maccWebRoamFailPortal = stat.mAccessWebRoamingFailedPortal;
                hwWifiDFT2StabilitySsidStat.maccWebSlowCnt = (short) stat.mUserLongTimeWaitedCnt;
                hwWifiDFT2StabilitySsidStat.maccWebSucCnt = stat.mAccessWEBSuccCnt;
                hwWifiDFT2StabilitySsidStat.mappDisabledAbCnt = stat.mAppDisabledAbnromalCnt;
                hwWifiDFT2StabilitySsidStat.mappDisabledScSucCnt = stat.mAppDisabledScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mapVendorInfo = stat.apVendorInfo;
                hwWifiDFT2StabilitySsidStat.marpReassocOkCnt = stat.mArpReassocOkCnt;
                hwWifiDFT2StabilitySsidStat.marpUnreachCnt = stat.mArpUnreachableCnt;
                hwWifiDFT2StabilitySsidStat.massocByABSCnt = stat.mAssocByABSCnt;
                hwWifiDFT2StabilitySsidStat.massocCnt = stat.mAssocCnt;
                hwWifiDFT2StabilitySsidStat.massocDura = stat.mAssocDuration;
                hwWifiDFT2StabilitySsidStat.massocRejAbCnt = stat.mAssocRejectedAbnormalCnt;
                hwWifiDFT2StabilitySsidStat.massocRejFullCnt = stat.mAssocRejectAccessFullCnt;
                hwWifiDFT2StabilitySsidStat.massocRejScSucCnt = stat.mAssocRejectedScSuccCnt;
                hwWifiDFT2StabilitySsidStat.massocSucCnt = stat.mAssocSuccCnt;
                hwWifiDFT2StabilitySsidStat.mauthCnt = stat.mAuthCnt;
                hwWifiDFT2StabilitySsidStat.mauthDura = stat.mAuthDuration;
                hwWifiDFT2StabilitySsidStat.mauthFailAbCnt = stat.mAuthFailedAbnormalCnt;
                hwWifiDFT2StabilitySsidStat.mauthFailScSucCnt = stat.mAuthFailedScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mauthSucCnt = stat.mAuthSuccCnt;
                hwWifiDFT2StabilitySsidStat.mbandWidth = (byte) 1;
                hwWifiDFT2StabilitySsidStat.mblackListAbCnt = stat.mBlackListAbnormalCnt;
                hwWifiDFT2StabilitySsidStat.mblackListScSucCnt = stat.mBlackListScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mconnectedCnt = stat.mConnectedCnt;
                hwWifiDFT2StabilitySsidStat.mconnectedDura = stat.mConnectedDuration;
                hwWifiDFT2StabilitySsidStat.mconnectingDura = stat.mConnectingDuration;
                hwWifiDFT2StabilitySsidStat.mconnectTotalCnt = stat.mConnectTotalCnt;
                hwWifiDFT2StabilitySsidStat.mdhcpDura = stat.mDhcpDuration;
                hwWifiDFT2StabilitySsidStat.mdhcpFailAbCnt = stat.mDhcpFailedAbnormalCnt;
                hwWifiDFT2StabilitySsidStat.mdhcpFailScSucCnt = stat.mDhcpFailedScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mdhcpFailStaticScSucCnt = stat.mDhcpFailedStaticScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mdhcpRenewCnt = stat.mReDHCPCnt;
                hwWifiDFT2StabilitySsidStat.mdhcpRenewDura = stat.mReDHCPDuration;
                hwWifiDFT2StabilitySsidStat.mdhcpRenewSucCnt = stat.mReDHCPSuccCnt;
                hwWifiDFT2StabilitySsidStat.mdhcpStaticSucCnt = stat.mDhcpStaticSuccCnt;
                hwWifiDFT2StabilitySsidStat.mdhcpSucCnt = stat.mDhcpSuccCnt;
                hwWifiDFT2StabilitySsidStat.mdisconnCnt = stat.mDisconnectCnt;
                hwWifiDFT2StabilitySsidStat.mdnsAbnormalCnt = stat.mDnsAbnormalCnt;
                if (this.mDnsReqCnt - this.mDnsReqFail == 0) {
                    i2 = 0;
                } else {
                    i2 = this.mDnsTotTime / (this.mDnsReqCnt - this.mDnsReqFail);
                }
                hwWifiDFT2StabilitySsidStat.mdnsAvgTime = i2;
                hwWifiDFT2StabilitySsidStat.mdnsParseFailCnt = stat.mDnsParseFailCnt;
                hwWifiDFT2StabilitySsidStat.mdnsReqCnt = this.mDnsReqCnt;
                hwWifiDFT2StabilitySsidStat.mdnsReqFail = this.mDnsReqFail;
                hwWifiDFT2StabilitySsidStat.mdnsResetScSucCnt = stat.mDnsResetScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mdnsScSucCnt = stat.mDnsScSuccCnt;
                hwWifiDFT2StabilitySsidStat.meap = stat.strAP_eap;
                hwWifiDFT2StabilitySsidStat.mfirstConnInetFailCnt = stat.mFirstConnInternetFailCnt;
                hwWifiDFT2StabilitySsidStat.mfirstConnInetFailDura = stat.mFirstConnInternetFailDuration;
                hwWifiDFT2StabilitySsidStat.mfrequency = stat.mFreq;
                hwWifiDFT2StabilitySsidStat.mgatewayAbCnt = stat.mGatewayAbnormalCnt;
                hwWifiDFT2StabilitySsidStat.mgoodReConnCnt = stat.mGoodReConnectCnt;
                hwWifiDFT2StabilitySsidStat.mgoodReConnSucCnt = stat.mGoodReConnectSuccCnt;
                hwWifiDFT2StabilitySsidStat.mgwIpCnt = (byte) stat.mGatewayAbnormalCnt;
                hwWifiDFT2StabilitySsidStat.mgwMacCnt = (byte) stat.mMultiGWCount;
                hwWifiDFT2StabilitySsidStat.mgwResetScSucCnt = stat.mGwResetScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mipAutoCnt = stat.mDhcpAutoIpCnt;
                hwWifiDFT2StabilitySsidStat.mipDhcpCnt = stat.mDhcpCnt;
                hwWifiDFT2StabilitySsidStat.mipStaticCnt = stat.mDhcpStaticCnt;
                hwWifiDFT2StabilitySsidStat.misMobleAP = (byte) 1;
                hwWifiDFT2StabilitySsidStat.mkeyMgmt = stat.strAP_key_mgmt;
                hwWifiDFT2StabilitySsidStat.mlinkspeed = (short) 1;
                hwWifiDFT2StabilitySsidStat.mMAC = maskMacAddress(stat.BSSID);
                hwWifiDFT2StabilitySsidStat.mMACHASH = String.valueOf(stat.BSSID.hashCode());
                hwWifiDFT2StabilitySsidStat.mnoUserProcRunCnt = stat.mNoUserProcCnt;
                hwWifiDFT2StabilitySsidStat.monlyTxNoRxCnt = stat.mOnlyTheTxNoRxCnt;
                hwWifiDFT2StabilitySsidStat.mOnSceenConnSucDura = stat.mOnScreenConnectDuration;
                hwWifiDFT2StabilitySsidStat.mOnSceenReConnCnt = stat.mOnScreenReConnectedCnt;
                hwWifiDFT2StabilitySsidStat.mOnScreenAbDisconnCnt = stat.mOnScreenAbDisconnectCnt;
                hwWifiDFT2StabilitySsidStat.mOnScreenConnCnt = stat.mOnScreenConnectCnt;
                hwWifiDFT2StabilitySsidStat.mOnScreenConnSucCnt = stat.mOnScreenConnectedCnt;
                hwWifiDFT2StabilitySsidStat.mOnScreenDisconnCnt = stat.mOnScreenDisconnectCnt;
                hwWifiDFT2StabilitySsidStat.mproxySetting = (byte) 1;
                hwWifiDFT2StabilitySsidStat.mpublicEssCnt = stat.mPublicEssCnt;
                hwWifiDFT2StabilitySsidStat.mreassocScConnFailCnt = stat.mReassocScConnectFailedCnt;
                hwWifiDFT2StabilitySsidStat.mreassScSucCnt = stat.mResetScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mreDHCPAccWebSuccCnt = stat.mReDHCPAccessWebSuccCnt;
                hwWifiDFT2StabilitySsidStat.mreDhcpScSucCnt = stat.mReDhcpScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mrekeyCnt = stat.mReKEYCnt;
                hwWifiDFT2StabilitySsidStat.mrekeyDura = stat.mReKEYDuration;
                hwWifiDFT2StabilitySsidStat.mrekeySucCnt = stat.mReKEYSuccCnt;
                hwWifiDFT2StabilitySsidStat.mresetScConnFailCnt = stat.mResetScConnectFailedCnt;
                hwWifiDFT2StabilitySsidStat.mresetScSuccCnt = stat.mResetScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mroamAccWebSucCnt = stat.mRoamingAccessWebSuccCnt;
                hwWifiDFT2StabilitySsidStat.mroamCnt = stat.mRoamingCnt;
                hwWifiDFT2StabilitySsidStat.mroamDura = stat.mRoamingDuration;
                hwWifiDFT2StabilitySsidStat.mroamingAbCnt = stat.mRoamingAbnormalCnt;
                hwWifiDFT2StabilitySsidStat.mroamingResetScSucCnt = stat.mRoamingResetScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mroamSucCnt = stat.mRoamingSuccCnt;
                hwWifiDFT2StabilitySsidStat.mRSSI = this.mRssi;
                hwWifiDFT2StabilitySsidStat.mrssiAvg = 1;
                hwWifiDFT2StabilitySsidStat.mstaticIpScSucCnt = stat.mStaticIpScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mtcpRxAbCnt = stat.mTcpRxAbnormalCnt;
                hwWifiDFT2StabilitySsidStat.muserEnableStaticIpCnt = stat.mUserEnableStaticIpCnt;
                hwWifiDFT2StabilitySsidStat.mstaticIpConflictedScSucCnt = stat.mStaticIpConflictedScAbnormalCnt;
                hwWifiDFT2StabilitySsidStat.mrouterDisplayNoInternetCnt = stat.mRouterDisplayNoInternetCnt;
                hwWifiDFT2StabilitySsidStat.mrouterUnreachableCnt = stat.mRouterUnreachableCnt;
                hwWifiDFT2StabilitySsidStat.minvalidIpScAbnormalCnt = stat.mInvalidIpScAbnormalCnt;
                hwWifiDFT2StabilitySsidStat.minvalidIpScSuccCnt = stat.mInvalidIpScSuccCnt;
                hwWifiDFT2StabilitySsidStat.mweakReConnCnt = stat.mWeakReConnectCnt;
                hwWifiDFT2StabilitySsidStat.mweakReConnSucCnt = stat.mWeakReConnectSuccCnt;
                hwWifiDFT2StabilitySsidStat.mwebFailDura = stat.mAccessWebFailDura;
                hwWifiDFT2StabilitySsidStat.mwebSlowDura = 1;
                hwWifiDFT2StabilitySsidStat.mSSID = stat.SSID;
                hwWifiDFT2StabilitySsidStat.mMaxspeed = stat.mMaxspeed;
                hwWifiDFT2StabilitySsidStat.mDsDelayL1 = stat.mDsDelayL1;
                hwWifiDFT2StabilitySsidStat.mDsDelayL2 = stat.mDsDelayL2;
                hwWifiDFT2StabilitySsidStat.mDsDelayL3 = stat.mDsDelayL3;
                hwWifiDFT2StabilitySsidStat.mDsDelayL4 = stat.mDsDelayL4;
                hwWifiDFT2StabilitySsidStat.mDsDelayL5 = stat.mDsDelayL5;
                hwWifiDFT2StabilitySsidStat.mDsDelayL6 = stat.mDsDelayL6;
                hwWifiDFT2StabilitySsidStat.mDsRTTL1 = stat.mDsRTTL1;
                hwWifiDFT2StabilitySsidStat.mDsRTTL2 = stat.mDsRTTL2;
                hwWifiDFT2StabilitySsidStat.mDsRTTL3 = stat.mDsRTTL3;
                hwWifiDFT2StabilitySsidStat.mDsRTTL4 = stat.mDsRTTL4;
                hwWifiDFT2StabilitySsidStat.mDsRTTL5 = stat.mDsRTTL5;
                hwWifiDFT2StabilitySsidStat.mDsStatFailNum = stat.mDsStatFailNum;
                hwWifiDFT2StabilitySsidStat.mDsStatNoAckNum = stat.mDsStatNoAckNum;
                hwWifiDFT2StabilitySsidStat.mDsStatRTT = stat.mDsStatRTT;
                hwWifiDFT2StabilitySsidStat.mDsStatSuccNum = stat.mDsStatSuccNum;
                hwWifiDFT2StabilitySsidStat.mDsStatTcpTotalNum = stat.mDsStatTcpTotalNum;
                hwWifiDFT2StabilitySsidStat.mDsStatTotalNum = stat.mDsStatTotalNum;
                hwWifiDFT2StabilitySsidStat.mDsStatWebDelay = stat.mDsStatWebDelay;
                hwWifiDFT2StabilitySsidStat.mDnsCount = stat.mDnsCount;
                hwWifiDFT2StabilitySsidStat.mDnsTotaldelay = stat.mDnsTotaldelay;
                hwWifiDFT2StabilitySsidStat.mDnsIpv6TimeOut = stat.mDnsIpv6TimeOut;
                hwWifiDFT2StabilitySsidStat.mDnsFailCount = stat.mDnsFailCount;
                hwWifiDFT2StabilitySsidStat.mDnsDelayL1 = stat.mDnsDelayL1;
                hwWifiDFT2StabilitySsidStat.mDnsDelayL2 = stat.mDnsDelayL2;
                hwWifiDFT2StabilitySsidStat.mDnsDelayL3 = stat.mDnsDelayL3;
                hwWifiDFT2StabilitySsidStat.mDnsDelayL4 = stat.mDnsDelayL4;
                hwWifiDFT2StabilitySsidStat.mDnsDelayL5 = stat.mDnsDelayL5;
                hwWifiDFT2StabilitySsidStat.mDnsDelayL6 = stat.mDnsDelayL6;
                hwWifiDFT2StabilitySsidStat.mMssSuccCnt = stat.mMssSuccCnt;
                hwWifiDFT2StabilitySsidStat.mMssFailCnt = stat.mMssFailCnt;
                hwWifiDFT2StabilitySsidStat.mMssReasonCode = stat.mMssReasonCode;
                System.arraycopy(stat.mTxBoostOnRTT, 0, hwWifiDFT2StabilitySsidStat.mTxBoostOnRTT, 0, 5);
                System.arraycopy(stat.mTxBoostOnRTTCnt, 0, hwWifiDFT2StabilitySsidStat.mTxBoostOnRTTCnt, 0, 5);
                System.arraycopy(stat.mTxBoostOntxBad, 0, hwWifiDFT2StabilitySsidStat.mTxBoostOntxBad, 0, 5);
                System.arraycopy(stat.mTxBoostOntxGood, 0, hwWifiDFT2StabilitySsidStat.mTxBoostOntxGood, 0, 5);
                System.arraycopy(stat.mTxBoostOnTxRetry, 0, hwWifiDFT2StabilitySsidStat.mTxBoostOnTxRetry, 0, 5);
                System.arraycopy(stat.mTxBoostOffRTT, 0, hwWifiDFT2StabilitySsidStat.mTxBoostOffRTT, 0, 5);
                System.arraycopy(stat.mTxBoostOffRTTCnt, 0, hwWifiDFT2StabilitySsidStat.mTxBoostOffRTTCnt, 0, 5);
                System.arraycopy(stat.mTxBoostOfftxBad, 0, hwWifiDFT2StabilitySsidStat.mTxBoostOfftxBad, 0, 5);
                System.arraycopy(stat.mTxBoostOfftxGood, 0, hwWifiDFT2StabilitySsidStat.mTxBoostOfftxGood, 0, 5);
                System.arraycopy(stat.mTxBoostOffTxRetry, 0, hwWifiDFT2StabilitySsidStat.mTxBoostOffTxRetry, 0, 5);
                System.arraycopy(stat.mMssFailCntArray, 0, hwWifiDFT2StabilitySsidStat.mMssFailCntArray, 0, 3);
                System.arraycopy(stat.mMssSuccCntArray, 0, hwWifiDFT2StabilitySsidStat.mMssSuccCntArray, 0, 3);
                hwWifiDFT2StabilitySsidStat.mrouterModel = stat.mrouterModel;
                listHwWifiDFT2StabilitySsidStat.add(hwWifiDFT2StabilitySsidStat);
                i++;
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "getWifiDFT2StabilitySsidStat error; Exception is: " + e.getMessage());
                return;
            } catch (Exception e2) {
                Log.e(TAG, "getWifiDFT2StabilitySsidStat error; Exception is: " + e2.getMessage());
                return;
            }
        }
    }

    public void updateABSTime(String ssid, int associateTimes, int associateFailedTimes, long mimoTime, long sisoTime, long mimoScreenOnTime, long sisoScreenOnTime) {
        SSIDStat sta = geStatBySSID(ssid);
        if (TextUtils.isEmpty(sta.SSID)) {
            Log.e(TAG, "sta == null error");
            sta.SSID = ssid;
            this.mSSIDStatList.add(sta);
        }
        sta.mABSAssociateTimes = sta.mABSAssociateTimes + associateTimes;
        sta.mABSAssociateFailedTimes = sta.mABSAssociateFailedTimes + associateFailedTimes;
        sta.mABSMimoTime = sta.mABSMimoTime + mimoTime;
        sta.mABSSisoTime = sta.mABSSisoTime + sisoTime;
        sta.mABSMimoScreenOnTime = sta.mABSMimoScreenOnTime + mimoScreenOnTime;
        sta.mABSSisoScreenOnTime = sta.mABSSisoScreenOnTime + sisoScreenOnTime;
    }

    public void updataWeChartStatic(int weChartTimes, int lowRssiTimes, int disconnectTimes, int backGroundTimes, int videoTimes) {
        this.mWeChartTimes += weChartTimes;
        this.mWeChartLowRssiTimes += lowRssiTimes;
        this.mWeChartDisconnectTimes += disconnectTimes;
        this.mWeChartBackGroundTimes += backGroundTimes;
        this.mWeChartVideoTimes += videoTimes;
    }
}
