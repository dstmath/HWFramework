package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.bluetooth.BluetoothAdapter;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import com.android.server.wifi.p2p.HwWifiP2pService;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.wifipro.HwDualBandMessageUtil;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiProDualbandExceptionRecord;
import com.android.server.wifi.wifipro.WifiProStatisticsRecord;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.huawei.connectivitylog.ChrCommonInfo;
import com.huawei.connectivitylog.ConnectivityLogManager;
import com.huawei.connectivitylog.ConnectivityLogManager.CHRDataPlus;
import com.huawei.connectivitylog.LogManager;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_ABNORMAL_DISCONNECT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_ACCESS_INTERNET_FAILED;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_ACCESS_WEB_SLOWLY;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_ANTS_SWITCH_FAILED;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_AP_INFO_COLLECT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_CONNECT_ASSOC_FAILED;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_CONNECT_AUTH_FAILED;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_CONNECT_DHCP_FAILED;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_CONNECT_EVENT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_DEVICE_ERROR;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_HAL_DRIVER_DEVICE_EXCEPTION;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_OPEN_CLOSE_FAILED;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_POOR_LEVEL;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_PORTAL_AUTH_MSG_COLLECTE;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_PORTAL_SAMPLES_COLLECTE;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_REPEATER_OPEN_OR_CLOSE_FAILED;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_SCAN_FAILED;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_STABILITY_STAT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_STATUS_CHANGEDBY_APK;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_USER_CONNECT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_EXCEPTION_EVENT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WIFIPRO_STATISTICS_EVENT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_WORKAROUND_STAT;
import com.huawei.device.connectivitychrlog.CSubApRoaming;
import com.huawei.device.connectivitychrlog.CSubAssoc_Chr_Event;
import com.huawei.device.connectivitychrlog.CSubAuth_Chr_Event;
import com.huawei.device.connectivitychrlog.CSubBTStatus;
import com.huawei.device.connectivitychrlog.CSubCPUInfo;
import com.huawei.device.connectivitychrlog.CSubCellID;
import com.huawei.device.connectivitychrlog.CSubDHCP_Chr_Event;
import com.huawei.device.connectivitychrlog.CSubDNS;
import com.huawei.device.connectivitychrlog.CSubMemInfo;
import com.huawei.device.connectivitychrlog.CSubNET_CFG;
import com.huawei.device.connectivitychrlog.CSubPacketCount;
import com.huawei.device.connectivitychrlog.CSubRSSIGROUP_EVENT;
import com.huawei.device.connectivitychrlog.CSubRSSIGROUP_EVENT_EX;
import com.huawei.device.connectivitychrlog.CSubTCP_STATIST;
import com.huawei.device.connectivitychrlog.CSubTRAFFIC_GROUND;
import com.huawei.device.connectivitychrlog.ChrLogBaseModel;
import com.huawei.device.connectivitychrlog.ChrLogModel;
import com.huawei.device.connectivitychrlog.LogByte;
import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONException;
import org.json.JSONObject;

public class HwWifiCHRStateManagerImpl extends HwWifiCHRStateManager {
    private static final long ACCESSWEB_SLOW_OR_FAILED_BETA_UPLOAD_COUNT = 30;
    private static final long ACCESSWEB_SLOW_OR_FAILED_BETA_UPLOAD_MIN = 600000;
    private static final long ACCESSWEB_SLOW_OR_FAILED_UPLOAD_COUNT = 10;
    private static final long ACCESSWEB_SLOW_OR_FAILED_UPLOAD_MIN = 1800000;
    private static final int ANT_LAST_SW_TIME = 2;
    private static final int ANT_MAIN_WORK_TIME = 8;
    private static final int ANT_SEC_WORK_TIME = 16;
    private static final String ANT_SWITCH_FAILED_REASON = "ant_sw_failed_reason";
    private static final int ANT_SW_COUNT = 4;
    private static final int ANT_WHICH_CUR_WORK = 1;
    private static final int ANT_WIFI_OPEN_TIME = 32;
    private static final long COMM_UPLOAD_MIN_SPAN = 86400000;
    private static final int DELAY_GET_ANTS_SW_CNT_TIME = 86400000;
    private static final byte DISABLE_NETWORK_PHASE1 = (byte) 1;
    private static final byte DISABLE_NETWORK_PHASE2 = (byte) 2;
    private static final int DISCONNET_CNT_PHASE1 = 3;
    private static final int DISCONNET_CNT_PHASE2 = 15;
    private static final int DISCONNET_TIME_PHASE1 = 300000;
    private static final int DISCONNET_TIME_PHASE2 = 1800000;
    public static final int DUALBAND_MIX_AP_TYPE = 2;
    public static final int DUALBAND_SINGLE_AP_TYPE = 1;
    private static final int GET_ANTS_ALL_INFO_PARAM = -1;
    private static final String GET_ANTS_INFO_FAILED = "GET_ANTS_INFO_FAILED";
    private static final int GET_CONNECT_EVENT_DATA_WAIT_TIME = 1;
    protected static final boolean HWFLOW;
    private static final int MAX_CANT_CONNECT_FOR_LONG = 8;
    private static final int MAX_DISCONN_CNT = 6;
    private static final int MAX_RSSI_LEVEL = 5;
    private static final int MAX_UPLOAD_ACCESS_SLOWLY_PER_CONNECT_CNT = 2;
    private static final int POOR_RSSI_TRIGGER_TIME_THRESHOLD = 24000;
    private static final int SECURITY_EAP = 3;
    private static final int SECURITY_NONE = 0;
    private static final int SECURITY_PSK = 2;
    private static final int SECURITY_WAPI_CERT = 5;
    private static final int SECURITY_WAPI_PSK = 4;
    private static final int SECURITY_WEP = 1;
    private static final String SETTING_AND_WIFISERVICE_STATE_DIFFERENT_ACTION = "com.huawei.chr.wifi.action.SETTING_AND_WIFISERVICE_STATE_DIFFERENT";
    private static final int SSID_MAX_LENGTH = 32;
    private static final int STATE_SCANING = 8;
    private static final String STR_ANT_LAST_SW_TIME = "ANT_LAST_SW_TIME";
    private static final String STR_ANT_MAIN_WORK_TIME = "ANT_MAIN_WORK_TIME";
    private static final String STR_ANT_SEC_WORK_TIME = "ANT_SEC_WORK_TIME";
    private static final String STR_ANT_SW_COUNT = "ANT_SW_COUNT";
    private static final String STR_ANT_WHICH_CUR_WORK = "ANT_WHICH_CUR_WORK";
    private static final String STR_ANT_WIFI_OPEN_TIME = "ANT_WIFI_OPEN_TIME";
    private static final String SUB_EVENT_ACTIVE_CHECK_FAIL = "ACTIVE_CHECK_FAIL";
    private static final String SUB_EVENT_AUTO_CLOSE_TERMINATION = "AUTO_CLOSE_TERMINATION";
    private static final String SUB_EVENT_BG_AC_RS_DIFF = "BG_AC_RS_DIFF";
    private static final String SUB_EVENT_BG_AC_TIME_LEN = "BG_AC_TIME_LEN";
    private static final String SUB_EVENT_BG_ASSOC_REJECT_CNT = "BG_ASSOC_REJECT_CNT";
    private static final String SUB_EVENT_BG_AUTH_FAIL_CNT = "BG_AUTH_FAIL_CNT";
    private static final String SUB_EVENT_BG_CONNT_TIMEOUT_CNT = "BG_CONNT_TIMEOUT_CNT";
    private static final String SUB_EVENT_BG_CONN_AP_TIME_LEN = "BG_CONN_AP_TIME_LEN";
    private static final String SUB_EVENT_BG_DHCP_FAIL_CNT = "BG_DHCP_FAIL_CNT";
    private static final String SUB_EVENT_BG_DNS_FAIL_CNT = "BG_DNS_FAIL_CNT";
    private static final String SUB_EVENT_BG_FAILED_CNT = "BG_FAILED_CNT";
    private static final String SUB_EVENT_BG_INET_OK_ACTIVE_NOT_OK = "BG_INET_OK_ACTIVE_NOT_OK";
    private static final String SUB_EVENT_BG_NOT_INET_ACTIVE_IOK = "BG_NOT_INET_ACTIVE_IOK";
    private static final String SUB_EVENT_BG_USER_SEL_AP_FISHING_CNT = "BG_USER_SEL_AP_FISHING_CNT";
    private static final String SUB_EVENT_CANT_CONNECT_FOR_LONG = "CANT_CONNECT_FOR_LONG";
    public static final String SUB_EVENT_DUALBAND_HANDOVER_PINGPONG = "DUALBAND_HANDOVER_PINGPONG";
    public static final String SUB_EVENT_DUALBAND_HANDOVER_SNAPSHOP = "DUALBAND_HANDOVER_SNAPSHOP";
    public static final String SUB_EVENT_DUALBAND_HANDOVER_TOO_SLOW = "DUALBAND_HANDOVER_TOO_SLOW";
    public static final String SUB_EVENT_DUALBAND_HANDOVER_TO_BAD_5G = "DUALBAND_HANDOVER_TO_BAD_5G";
    public static final String SUB_EVENT_DUALBAND_HANDOVER_USER_REJECT = "DUALBAND_HANDOVER_USER_REJECT";
    private static final String SUB_EVENT_ENTERPRISE_AP_INFO = "ENTERPRISE_AP_INFO";
    private static final String SUB_EVENT_HOME_AP_INFO = "HOME_AP_INFO";
    private static final String SUB_EVENT_NOT_OPEN_AP_REDIRECT = "NOT_OPEN_AP_REDIRECT";
    private static final String SUB_EVENT_PORTALAP_IN_WHITE = "PORTALAP_IN_WHITE";
    private static final String SUB_EVENT_ROVE_OUT_PARAMETER = "ROVE_OUT_PARAMETER";
    private static final String SUB_EVENT_SWITCH_PINGPONG = "SWITCH_PINGPONG";
    private static final String SUB_EVENT_WHITE_MORETHAN_500 = "WHITE_MORETHAN_500";
    private static final String TAG = "HwWifiCHRStateManager";
    private static final short THRESHOD_ACCESS_NETWORK_ASSOC_COUNT = (short) 5;
    private static final short THRESHOD_ACCESS_NETWORK_AUTH_FAILED_COUNT = (short) 5;
    private static final short THRESHOD_ACCESS_NETWORK_DHCP_FAILED_COUNT = (short) 5;
    private static final short THRESHOD_ACCESS_NETWORK_TOTOAL_FAILED_COUNT = (short) 20;
    private static final int TRIGGER_DELAYED = 60000;
    private static final int TRIGGER_DELAYED_DAILY = 45000;
    private static final int UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE = 3;
    private static final int UPLOAD_NUMBER_ONE_CYCLE = 50;
    private static final int USER_TYPE_BETA = 1;
    private static final int USER_TYPE_COMMERCIAL = 2;
    private static final int WIFI_CHR_DEVICE_ERROR_OFFSET = 15;
    private static final String WIFI_EVALUATE_TAG = "wifipro_recommending_access_points";
    private static final String WIFI_OPEN_CLOSE_FAILED_STATE = "wifi_open_close_failed_state";
    private static int WIFI_REPEATER_CLOSED = 0;
    private static int WIFI_REPEATER_OPENED = 0;
    private static int WIFI_REPEATER_TETHER = 0;
    private static final int WIFI_SECURITY_TYPE_UNKNOWN = -1;
    private static final int WIFI_SETTING_CLOSED_AND_SERVICE_OPENED = 0;
    private static final int WIFI_SETTING_OPENED_AND_SERVICE_CLOSED = 1;
    private static final int WIFI_SETTING_OPENED_TIMEOUT = 2;
    public static final int WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT = 126;
    public static final int WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT = 125;
    private static int mWifiType;
    private static HwWifiCHRStateManager wsm;
    private long MIN_UPLOAD_CONNECT_FAIL_SPAN;
    private int accessSlowlyerrrCode;
    private short aucAbnormal_disconnect;
    private short aucAccess_internet_failed;
    private short aucClose_failed;
    private short aucDhcp_failed;
    private short aucOpen_failed;
    private short aucScan_failed;
    private ConnectivityManager connMgr;
    private int connectedType;
    private int dftType;
    private HwWifiCHRService hwWifiCHRService;
    private int mAC_FailType;
    public short mAF_AutoLoginFailCnt;
    public short mAF_AutoLoginSuccCnt;
    public short mAF_FPNSuccNotMsmCnt;
    public short mAF_PasswordFailCnt;
    public short mAF_PasswordSuccCnt;
    public short mAF_PhoneNumFailCnt;
    public short mAF_PhoneNumSuccCnt;
    private String mAP1BSsid;
    private String mAP1Ssid;
    private String mAP2BSsid;
    private String mAP2Ssid;
    private String mAP3BSsid;
    private String mAP3Ssid;
    private String mAPBssid;
    private int mAPSecurity;
    private String mAPSsid;
    private String mAPVendorInfo;
    private AccessWebStatus mAWS;
    private int mAccessFailCount;
    private int mAccessWebSlowCount;
    public short mActiveCheckRS_Diff;
    public short mActiveCheckRS_Same;
    private boolean mApRedirectUrlSendCount;
    private int mApkAction;
    private int mApkChangedCount;
    private int mApkTriggerTimes;
    private int mAssocFailCount;
    private int mAuthFailCount;
    private int mAuthSubCode;
    private short mAutoCloseRootCause;
    private short mAutoOpenRootCause;
    private short mAutoOpenWhiteNum;
    private short mAutoRI_TotCount;
    private int mAutoRI_TotTime;
    private int mBG_AC_DiffType;
    public short mBG_AUTH_FailCnt;
    public short mBG_AssocRejectCnt;
    public short mBG_BgRunCnt;
    public short mBG_ConntTimeoutCnt;
    public short mBG_DHCPFailCnt;
    public short mBG_DNSFailCnt;
    public short mBG_FailedCnt;
    public short mBG_FishingApCnt;
    public short mBG_FoundTwoMoreApCnt;
    public short mBG_FreeInetOkApCnt;
    public short mBG_FreeNotInetApCnt;
    public short mBG_InetNotOkActiveOk;
    public short mBG_InetOkActiveNotOk;
    public short mBG_NCByCheckFail;
    public short mBG_NCByConnectFail;
    public short mBG_NCByStateErr;
    public short mBG_NCByUnknown;
    public short mBG_PortalApCnt;
    public short mBG_SettingRunCnt;
    public short mBG_UserSelApFishingCnt;
    public short mBG_UserSelFreeInetOkCnt;
    public short mBG_UserSelNoInetCnt;
    public short mBG_UserSelPortalCnt;
    public short mBMD_FiftyMNotifyCount;
    public short mBMD_FiftyM_RI_Count;
    public short mBMD_TenMNotifyCount;
    public short mBMD_TenM_RI_Count;
    public short mBMD_UserDelNotifyCount;
    public short mBQE_BadSettingCancel;
    public short mBQE_BindWlanFailCount;
    public short mBQE_CNUrl1FailCount;
    public short mBQE_CNUrl2FailCount;
    public short mBQE_CNUrl3FailCount;
    public short mBQE_NCNUrl1FailCount;
    public short mBQE_NCNUrl2FailCount;
    public short mBQE_NCNUrl3FailCount;
    public short mBQE_ScoreUnknownCount;
    public short mBQE_StopBqeFailCount;
    public short mBSG_EndIn4s7sCnt;
    public short mBSG_EndIn4sCnt;
    public short mBSG_NotEndIn7sCnt;
    public short mBSG_RsBadCnt;
    public short mBSG_RsGoodCnt;
    public short mBSG_RsMidCnt;
    public short mBigRTT_ErrRO_Tot;
    public short mBigRTT_RO_Tot;
    private short mCellAutoCloseCount;
    private short mCellAutoOpenCount;
    private byte mCheckReason;
    protected ChrCommonInfo mChrComInfo;
    private ArrayList<ChrLogBaseModel> mChrLogBaseModelList;
    protected ChrLogModel mChrLogModel;
    private String mCodeInputId;
    private short mConnectFailedReason;
    private short mConnectFailedSubErrorCode;
    private long mConnectSuccessTime;
    private String mConnectThreadName;
    private String mConnectType;
    private Context mContextRef;
    private HwCHRWifiLinkMonitor mCounter_monitor;
    private String mCountryCode;
    private short mCreditScoreRO_Rate;
    private String mCurrentApBssid;
    private int mCurrentApSecurity;
    private String mCurrentMsgIface;
    private short mCustomizedScan_FailCount;
    private short mCustomizedScan_SuccCount;
    private long mDailyUploadTime;
    private int mDeltaTime;
    private int mDhcpFailCount;
    private int mDhcpSerialNo;
    private int mDiffFreqStationRepeaterDuration;
    private byte mDisableNetworkflag;
    private String mDisableThreadName;
    private int mDisconnTotalCount;
    private int mDisconnectCnt;
    private int[] mDisconnectFail;
    private boolean mDualAntsChr;
    private String mDualband2GAPBssid;
    private String mDualband5GAPBssid;
    private int mDualbandAPType;
    private int mEnableTotTime;
    private int mEssCount;
    private int mFailReason;
    private long mFirstDisconnectTime;
    private String mFreeAPcellID;
    private int mHTML_Input_Number;
    private short mHandoverPingpongCount;
    private short mHandoverToBad5GCount;
    private short mHandoverToNotInet5GCount;
    private short mHandoverTooSlowCount;
    private short mHighDataRateRO_Rate;
    public short mHighDataRateStopROC;
    private short mHisScoRI_Count;
    private short mHistoryQuilityRO_Rate;
    public int mHistoryTotWifiConnHour;
    public short mHomeAPAddRoPeriodCnt;
    private int mHomeAPJudgeTime;
    public short mHomeAPQoeBadCnt;
    private List<HwCHRAccessNetworkEventInfo> mHwCHRAccessNetworkEventInfoList;
    private HwWiFiLogUtils mHwLogUtils;
    private HwWifiDFTConst mHwWifiDFTConst;
    private HwWifiDFTUtilImpl mHwWifiDFTUtilImpl;
    private HwWifiStatStore mHwWifiStatStore;
    private short mIPQLevel;
    private int mIpType;
    private int mIsPortalConnection;
    private long mLastAccessWebFailedTime;
    private long mLastAccessWebSlowTime;
    private String mLastApkName;
    private long mLastConnectFailTimestamp;
    private int mLastNetIdFromUI;
    private int mLastNetworkId;
    private BluetoothAdapter mLocalBluetoothAdapter;
    private ReentrantLock mLock;
    private Object mLockObj;
    private short mManualBackROC;
    public short mManualConnBlockPortalCount;
    private int mManualRI_TotTime;
    private short mMixedAP_DisapperCount;
    private short mMixedAP_HandoverFailCount;
    private short mMixedAP_HandoverSucCount;
    private short mMixedAP_HighFreqScan5GCount;
    private short mMixedAP_InblacklistCount;
    private short mMixedAP_LearnedCount;
    private short mMixedAP_LowFreqScan5GCount;
    private short mMixedAP_MidFreqScan5GCount;
    private short mMixedAP_MonitorCount;
    private short mMixedAP_NearbyCount;
    private short mMixedAP_SatisfiedCount;
    private short mMixedAP_ScoreNotSatisfyCount;
    private short mMobileSignalLevel;
    private int mMultiGWCount;
    private int mNeedEnableNetworkId;
    private int mNetNormalTime;
    private int mNetSlowlyTime;
    private int mNetworkId;
    public short mNoInetAlarmCount;
    public short mNoInetAlarmOnConnCnt;
    private short mNoInetHandoverCount;
    public short mNotAutoConnPortalCnt;
    public short mNotInetRO_DISCONNECT_Cnt;
    public int mNotInetRO_DISCONNECT_TotData;
    public short mNotInetRestoreRI;
    public short mNotInetSettingCancel;
    public short mNotInetUserCancel;
    public short mNotInetUserManualRI;
    public short mNotInetWifiToWifiCount;
    public int mNotInet_AutoRI_TotData;
    private String mNotOpenApRedirectUrl;
    private short mOTA_ErrRO_Tot;
    private short mOTA_PacketDropRate;
    private short mOTA_RO_Tot;
    private int mOpenCloseFailCount;
    private String mPhoneInputId;
    public short mPingPongCount;
    private String mPortalAPBssid;
    private byte[] mPortalAPKeyLines;
    private byte[] mPortalAPSsid;
    private short mPortalAutoLoginCount;
    private String mPortalCellId;
    private short mPortalCodeParseCount;
    public short mPortalNoAutoConnCnt;
    private int mPortalStatus;
    private short mPortalUnauthCount;
    private int mPrevStaChannel;
    public int mQOE_AutoRI_TotData;
    public short mQOE_RO_DISCONNECT_Cnt;
    public int mQOE_RO_DISCONNECT_TotData;
    private short mRATType;
    private String mRO_APSsid;
    private short mRO_Duration;
    public int mRO_TotMobileData;
    private short mRSSI_BetterRI_Count;
    private short mRSSI_ErrRO_Tot;
    private short mRSSI_RO_Tot;
    private short mRSSI_RestoreRI_Count;
    private short mRSSI_VALUE;
    private short mRcvSMS_Count;
    private String mRemark;
    public short mReopenWifiRICount;
    private int mRepeterConnFailedCount;
    private int mRepeterDiffBegin;
    private int mRepeterDiffEnd;
    private int mRepeterMaxClientCount;
    private int mRepeterOpenOrClose;
    private String mRouterBrand;
    private String mRouterModel;
    private int mRssi;
    private int[] mRssi2gCnt;
    private int mRssi2gMaxRssi;
    private int mRssi2gMinRssi;
    private int[] mRssi2gSum;
    private int[] mRssi5gCnt;
    private int mRssi5gMaxRssi;
    private int mRssi5gMinRssi;
    private int[] mRssi5gSum;
    private int mRssiCnt;
    private HwCHRWifiRSSIGroupSummery mRssiGroup;
    private int mRssiSum;
    private short mRttAvg;
    private int mSMS_Body_Len;
    private int mScanFailCount;
    private Object mScanResultLock;
    private List<ScanResult> mScanResults;
    private int mScreenState;
    private long mSecondDisconnectTime;
    public short mSelCSPAutoSwCount;
    public short mSelCSPNotSwCount;
    public short mSelCSPShowDiglogCount;
    public short mSelectNotInetAPCount;
    private short mSingleAP_DisapperCount;
    private short mSingleAP_HandoverFailCount;
    private short mSingleAP_HandoverSucCount;
    private short mSingleAP_HighFreqScan5GCount;
    private short mSingleAP_InblacklistCount;
    private short mSingleAP_LearnedCount;
    private short mSingleAP_LowFreqScan5GCount;
    private short mSingleAP_MidFreqScan5GCount;
    private short mSingleAP_MonitorCount;
    private short mSingleAP_NearbyCount;
    private short mSingleAP_SatisfiedCount;
    private short mSingleAP_ScoreNotSatisfyCount;
    private byte[] mSms_Body;
    private String mSms_Num;
    private String mSndBtnId;
    private int mSsid_Len;
    private int mStatIntervalTime;
    private short mSubcodeReject;
    private String mSubmitBtnId;
    private short mTCP_ErrRO_Tot;
    private short mTCP_RO_Tot;
    private short mTcpInSegs;
    private short mTcpOutSegs;
    private short mTcpRetransSegs;
    private long mTimeStampSessionFinish;
    private long mTimeStampSessionFirstConnect;
    private long mTimeStampSessionStart;
    private Timer mTimer;
    private short mTimerRI_Count;
    public short mTotAPRecordCnt;
    public short mTotBtnRICount;
    public short mTotHomeAPCnt;
    public int mTotWifiConnectTime;
    private short mTotalBQE_BadROC;
    public short mTotalPortalAuthSuccCount;
    public short mTotalPortalConnCount;
    private int mTriggerReportType;
    private int mUploadAccessWebFailedCount;
    private int mUploadAccessWebSlowly;
    private int mUploadAccessWebSlowlySSID;
    private String mUserAction;
    private short mUserCancelROC;
    private short mUserRejectHandoverCount;
    public short mUserUseBgScanAPCount;
    private short mWIFI_NetSpeed;
    private String mWebUrl;
    private int mWifiAntsPrevSWCnt;
    private int mWifiConnentEventCount;
    public short mWifiOobInitState;
    private WifiProDualbandExceptionRecord mWifiProDualbandExceptionRecord;
    private long mWifiProFreeAPUploadTime;
    private int mWifiRepeaterOpenedCount;
    private long mWifiRepeaterWorkingTime;
    private short mWifiScoCount;
    private final BroadcastReceiver mWifiSettingStateErrorReceiver;
    private HwWifiStateMachine mWifiStateMachine;
    public short mWifiToWifiSuccCount;
    public short mWifiproCloseCount;
    public short mWifiproOpenCount;
    public short mWifiproStateAtReportTime;
    private short mWorkaroundCode;
    private int mWorkaroundCount;
    private String mWorkaroundRemark;
    private short mWorkaroundRet;
    private short mWorkaroundStatus;
    protected HashMap<String, HalDrivceEventFreq> mapHalDriverEventTriggerFreq;
    protected HashMap<Integer, HashMap<Integer, String>> mapWifiEventReaseon;
    private int mpoorRssi2gCnt;
    private int mpoorRssi5gCnt;
    private NetworkInfo networkInfoMobile;
    private NetworkInfo networkInfoWlan;
    private String strAP_auth_alg;
    private String strAP_eap;
    private String strAP_gruop;
    private String strAP_key_mgmt;
    private String strAP_pairwise;
    private String strAP_proto;
    private String strAp_Ssid;
    private String strAp_mac;
    private String strIp_leasetime;
    private String strRoutes;
    private String strSpeedInfo;
    private String strSta_mac;
    private String strUIDSpeedInfo;
    private String str_Wifi_ip;
    private String str_dns;
    private String str_gate_ip;
    private short usAP_channel;
    private short usLinkSpeed;
    private WifiAntsStatus wifiAntsStatus;
    private int wifiproCanotConnectForLongCount;

    protected static class HalDrivceEventFreq {
        long lastTime;
        int triggerCount;

        protected HalDrivceEventFreq(int count, long time) {
            this.triggerCount = count;
            this.lastTime = time;
        }

        public int getTriggerCount() {
            return this.triggerCount;
        }

        public void setTriggerCount(int count) {
            this.triggerCount = count;
        }

        public long getLastTime() {
            return this.lastTime;
        }

        public void setLastTime(long time) {
            this.lastTime = time;
        }
    }

    protected static class WifiAntsStatus {
        int mAntCurWork;
        int mAntSWCntIntraday;
        int mAntsOpenTime;
        int mAntsSwitchFailedDir;
        int mAvgRSSI;
        boolean mIsAntSWCauseError;
        long mMainAntTime;
        long mSecAntTime;

        protected WifiAntsStatus() {
        }

        public int getAntsOpenTime() {
            return this.mAntsOpenTime;
        }

        public void setAntsOpenTime(int time) {
            this.mAntsOpenTime = time;
        }

        public int getAntCurWork() {
            return this.mAntCurWork;
        }

        public void setAntCurWork(int antStatus) {
            this.mAntCurWork = antStatus;
        }

        public int getAntsSwitchFailedDir() {
            return this.mAntsSwitchFailedDir;
        }

        public void setAntsSwitchFailedDir(int dir) {
            this.mAntsSwitchFailedDir = dir;
        }

        public boolean getIsAntSWCauseError() {
            return this.mIsAntSWCauseError;
        }

        public void setIsAntSWCauseError(boolean isSw) {
            this.mIsAntSWCauseError = isSw;
        }

        public int getAntSWCntIntraday() {
            return this.mAntSWCntIntraday;
        }

        public void setAntSWCntIntraday(int cnt) {
            this.mAntSWCntIntraday = cnt;
        }

        public long getMainAntTime() {
            return this.mMainAntTime;
        }

        public void setMainAntTime(long time) {
            this.mMainAntTime = time;
        }

        public long getSecAntTime() {
            return this.mSecAntTime;
        }

        public void setSecAntTime(long time) {
            this.mSecAntTime = time;
        }

        public int getAvgRSSI() {
            return this.mAvgRSSI;
        }

        public void setAvgRSSI(int avg) {
            this.mAvgRSSI = avg;
        }
    }

    public static native String get_wifi_ants_info(int i);

    static {
        boolean z;
        wsm = new HwWifiCHRStateManagerImpl();
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, SECURITY_WAPI_PSK);
        } else {
            z = HWFLOW;
        }
        HWFLOW = z;
        mWifiType = WIFI_CHR_DEVICE_ERROR_OFFSET;
        WIFI_REPEATER_CLOSED = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        WIFI_REPEATER_OPENED = WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
        WIFI_REPEATER_TETHER = WIFI_SETTING_OPENED_TIMEOUT;
    }

    public void setContextRef(Context context) {
        this.mContextRef = context;
        if (this.mContextRef != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SETTING_AND_WIFISERVICE_STATE_DIFFERENT_ACTION);
            intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
            intentFilter.addAction(MessageUtil.CONFIGURED_NETWORKS_CHANGED_ACTION);
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.mContextRef.registerReceiver(this.mWifiSettingStateErrorReceiver, intentFilter);
            if (HWFLOW) {
                Log.d(TAG, "construct context");
            }
        }
    }

    private HwWifiCHRStateManagerImpl() {
        this.mapWifiEventReaseon = null;
        this.mapHalDriverEventTriggerFreq = new HashMap();
        this.mCurrentMsgIface = "";
        this.mScreenState = WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
        this.usLinkSpeed = (short) 0;
        this.usAP_channel = (short) 0;
        this.strSta_mac = "";
        this.strAp_mac = "";
        this.strAp_Ssid = "";
        this.strIp_leasetime = "";
        this.strRoutes = "";
        this.str_gate_ip = "";
        this.str_dns = "";
        this.str_Wifi_ip = "";
        this.strAP_proto = "";
        this.strAP_key_mgmt = "";
        this.strAP_auth_alg = "";
        this.strAP_pairwise = "";
        this.strAP_gruop = "";
        this.strAP_eap = "";
        this.mRemark = "";
        this.mAPVendorInfo = "";
        this.mRssi = WifiHandover.INVALID_RSSI;
        this.mChrComInfo = new ChrCommonInfo();
        this.mTimer = null;
        this.mLockObj = null;
        this.mChrLogBaseModelList = null;
        this.mDailyUploadTime = 0;
        this.mSubcodeReject = (short) 0;
        this.mOpenCloseFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mApkChangedCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAuthFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mDhcpFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mScanFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAccessFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAssocFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mDisconnTotalCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mWorkaroundCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mDisconnectFail = new int[MAX_DISCONN_CNT];
        this.mAuthSubCode = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mLastNetIdFromUI = WIFI_SECURITY_TYPE_UNKNOWN;
        this.mWorkaroundCode = (short) 0;
        this.mWorkaroundRet = (short) 0;
        this.mWorkaroundStatus = (short) 0;
        this.mWorkaroundRemark = "";
        this.mWifiConnentEventCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mScanResultLock = new Object();
        this.mScanResults = new ArrayList();
        this.mLocalBluetoothAdapter = null;
        this.mLastConnectFailTimestamp = 0;
        this.MIN_UPLOAD_CONNECT_FAIL_SPAN = 2000;
        this.mSms_Body = new byte[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED];
        this.mPortalAPSsid = new byte[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED];
        this.mPortalAPKeyLines = new byte[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED];
        this.mSms_Num = "";
        this.mPortalCellId = "";
        this.mWebUrl = "";
        this.mSndBtnId = "";
        this.mPhoneInputId = "";
        this.mCodeInputId = "";
        this.mSubmitBtnId = "";
        this.mPortalAPBssid = "";
        this.mSMS_Body_Len = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mSsid_Len = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mHTML_Input_Number = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mNotOpenApRedirectUrl = "";
        this.mApRedirectUrlSendCount = HWFLOW;
        this.mAC_FailType = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mStatIntervalTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mEnableTotTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mNoInetHandoverCount = (short) 0;
        this.mPortalUnauthCount = (short) 0;
        this.mWifiScoCount = (short) 0;
        this.mPortalCodeParseCount = (short) 0;
        this.mRcvSMS_Count = (short) 0;
        this.mPortalAutoLoginCount = (short) 0;
        this.mCellAutoOpenCount = (short) 0;
        this.mCellAutoCloseCount = (short) 0;
        this.mTotalBQE_BadROC = (short) 0;
        this.mManualBackROC = (short) 0;
        this.mRSSI_RO_Tot = (short) 0;
        this.mRSSI_ErrRO_Tot = (short) 0;
        this.mOTA_RO_Tot = (short) 0;
        this.mOTA_ErrRO_Tot = (short) 0;
        this.mTCP_RO_Tot = (short) 0;
        this.mTCP_ErrRO_Tot = (short) 0;
        this.mManualRI_TotTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAutoRI_TotTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAutoRI_TotCount = (short) 0;
        this.mRSSI_RestoreRI_Count = (short) 0;
        this.mRSSI_BetterRI_Count = (short) 0;
        this.mTimerRI_Count = (short) 0;
        this.mHisScoRI_Count = (short) 0;
        this.mUserCancelROC = (short) 0;
        this.mWifiToWifiSuccCount = (short) 0;
        this.mNoInetAlarmCount = (short) 0;
        this.mWifiOobInitState = (short) 0;
        this.mNotAutoConnPortalCnt = (short) 0;
        this.mHighDataRateStopROC = (short) 0;
        this.mSelectNotInetAPCount = (short) 0;
        this.mUserUseBgScanAPCount = (short) 0;
        this.mPingPongCount = (short) 0;
        this.mBQE_BadSettingCancel = (short) 0;
        this.mNotInetSettingCancel = (short) 0;
        this.mNotInetUserCancel = (short) 0;
        this.mNotInetRestoreRI = (short) 0;
        this.mNotInetUserManualRI = (short) 0;
        this.mNotInetWifiToWifiCount = (short) 0;
        this.mReopenWifiRICount = (short) 0;
        this.mSelCSPShowDiglogCount = (short) 0;
        this.mSelCSPAutoSwCount = (short) 0;
        this.mSelCSPNotSwCount = (short) 0;
        this.mTotBtnRICount = (short) 0;
        this.mBMD_TenMNotifyCount = (short) 0;
        this.mBMD_TenM_RI_Count = (short) 0;
        this.mBMD_FiftyMNotifyCount = (short) 0;
        this.mBMD_FiftyM_RI_Count = (short) 0;
        this.mBMD_UserDelNotifyCount = (short) 0;
        this.mRO_TotMobileData = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAF_PhoneNumSuccCnt = (short) 0;
        this.mAF_PhoneNumFailCnt = (short) 0;
        this.mAF_PasswordSuccCnt = (short) 0;
        this.mAF_PasswordFailCnt = (short) 0;
        this.mAF_AutoLoginSuccCnt = (short) 0;
        this.mAF_AutoLoginFailCnt = (short) 0;
        this.mBG_BgRunCnt = (short) 0;
        this.mBG_SettingRunCnt = (short) 0;
        this.mBG_FreeInetOkApCnt = (short) 0;
        this.mBG_FishingApCnt = (short) 0;
        this.mBG_FreeNotInetApCnt = (short) 0;
        this.mBG_PortalApCnt = (short) 0;
        this.mBG_FailedCnt = (short) 0;
        this.mBG_InetNotOkActiveOk = (short) 0;
        this.mBG_InetOkActiveNotOk = (short) 0;
        this.mBG_UserSelApFishingCnt = (short) 0;
        this.mBG_ConntTimeoutCnt = (short) 0;
        this.mBG_DNSFailCnt = (short) 0;
        this.mBG_DHCPFailCnt = (short) 0;
        this.mBG_AUTH_FailCnt = (short) 0;
        this.mBG_AssocRejectCnt = (short) 0;
        this.mBG_UserSelFreeInetOkCnt = (short) 0;
        this.mBG_UserSelNoInetCnt = (short) 0;
        this.mBG_UserSelPortalCnt = (short) 0;
        this.mBG_FoundTwoMoreApCnt = (short) 0;
        this.mAF_FPNSuccNotMsmCnt = (short) 0;
        this.mBSG_RsGoodCnt = (short) 0;
        this.mBSG_RsMidCnt = (short) 0;
        this.mBSG_RsBadCnt = (short) 0;
        this.mBSG_EndIn4sCnt = (short) 0;
        this.mBSG_EndIn4s7sCnt = (short) 0;
        this.mBSG_NotEndIn7sCnt = (short) 0;
        this.mBG_NCByConnectFail = (short) 0;
        this.mBG_NCByCheckFail = (short) 0;
        this.mBG_NCByStateErr = (short) 0;
        this.mBG_NCByUnknown = (short) 0;
        this.mBQE_CNUrl1FailCount = (short) 0;
        this.mBQE_CNUrl2FailCount = (short) 0;
        this.mBQE_CNUrl3FailCount = (short) 0;
        this.mBQE_NCNUrl1FailCount = (short) 0;
        this.mBQE_NCNUrl2FailCount = (short) 0;
        this.mBQE_NCNUrl3FailCount = (short) 0;
        this.mBQE_ScoreUnknownCount = (short) 0;
        this.mBQE_BindWlanFailCount = (short) 0;
        this.mBQE_StopBqeFailCount = (short) 0;
        this.mQOE_AutoRI_TotData = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mNotInet_AutoRI_TotData = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mQOE_RO_DISCONNECT_Cnt = (short) 0;
        this.mQOE_RO_DISCONNECT_TotData = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mNotInetRO_DISCONNECT_Cnt = (short) 0;
        this.mNotInetRO_DISCONNECT_TotData = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mTotWifiConnectTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mActiveCheckRS_Diff = (short) 0;
        this.mNoInetAlarmOnConnCnt = (short) 0;
        this.mPortalNoAutoConnCnt = (short) 0;
        this.mHomeAPAddRoPeriodCnt = (short) 0;
        this.mHomeAPQoeBadCnt = (short) 0;
        this.mHistoryTotWifiConnHour = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mBigRTT_RO_Tot = (short) 0;
        this.mBigRTT_ErrRO_Tot = (short) 0;
        this.mTotAPRecordCnt = (short) 0;
        this.mTotHomeAPCnt = (short) 0;
        this.mTotalPortalConnCount = (short) 0;
        this.mTotalPortalAuthSuccCount = (short) 0;
        this.mManualConnBlockPortalCount = (short) 0;
        this.mWifiproStateAtReportTime = (short) 0;
        this.mWifiproOpenCount = (short) 0;
        this.mWifiproCloseCount = (short) 0;
        this.mActiveCheckRS_Same = (short) 0;
        this.mSingleAP_LearnedCount = (short) 0;
        this.mSingleAP_NearbyCount = (short) 0;
        this.mSingleAP_MonitorCount = (short) 0;
        this.mSingleAP_SatisfiedCount = (short) 0;
        this.mSingleAP_DisapperCount = (short) 0;
        this.mSingleAP_InblacklistCount = (short) 0;
        this.mSingleAP_ScoreNotSatisfyCount = (short) 0;
        this.mSingleAP_HandoverSucCount = (short) 0;
        this.mSingleAP_HandoverFailCount = (short) 0;
        this.mSingleAP_LowFreqScan5GCount = (short) 0;
        this.mSingleAP_MidFreqScan5GCount = (short) 0;
        this.mSingleAP_HighFreqScan5GCount = (short) 0;
        this.mMixedAP_LearnedCount = (short) 0;
        this.mMixedAP_NearbyCount = (short) 0;
        this.mMixedAP_MonitorCount = (short) 0;
        this.mMixedAP_SatisfiedCount = (short) 0;
        this.mMixedAP_DisapperCount = (short) 0;
        this.mMixedAP_InblacklistCount = (short) 0;
        this.mMixedAP_ScoreNotSatisfyCount = (short) 0;
        this.mMixedAP_HandoverSucCount = (short) 0;
        this.mMixedAP_HandoverFailCount = (short) 0;
        this.mMixedAP_LowFreqScan5GCount = (short) 0;
        this.mMixedAP_MidFreqScan5GCount = (short) 0;
        this.mMixedAP_HighFreqScan5GCount = (short) 0;
        this.mCustomizedScan_SuccCount = (short) 0;
        this.mCustomizedScan_FailCount = (short) 0;
        this.mHandoverToNotInet5GCount = (short) 0;
        this.mHandoverTooSlowCount = (short) 0;
        this.mHandoverToBad5GCount = (short) 0;
        this.mUserRejectHandoverCount = (short) 0;
        this.mHandoverPingpongCount = (short) 0;
        this.mWifiProDualbandExceptionRecord = null;
        this.mDualbandAPType = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mDualband2GAPBssid = "";
        this.mDualband5GAPBssid = "";
        this.mRSSI_VALUE = (short) 0;
        this.mOTA_PacketDropRate = (short) 0;
        this.mRttAvg = (short) 0;
        this.mTcpInSegs = (short) 0;
        this.mTcpOutSegs = (short) 0;
        this.mTcpRetransSegs = (short) 0;
        this.mWIFI_NetSpeed = (short) 0;
        this.mIPQLevel = (short) 0;
        this.mRO_APSsid = "";
        this.mMobileSignalLevel = (short) 0;
        this.mRATType = (short) 0;
        this.mHistoryQuilityRO_Rate = (short) 0;
        this.mHighDataRateRO_Rate = (short) 0;
        this.mCreditScoreRO_Rate = (short) 0;
        this.mRO_Duration = (short) 0;
        this.mAPBssid = "";
        this.mAPSsid = "";
        this.mAutoOpenWhiteNum = (short) 0;
        this.mAutoOpenRootCause = (short) 0;
        this.mAutoCloseRootCause = (short) 0;
        this.mWifiProFreeAPUploadTime = 0;
        this.mAP1Ssid = "";
        this.mAP2Ssid = "";
        this.mAP3Ssid = "";
        this.mAP1BSsid = "";
        this.mAP2BSsid = "";
        this.mAP3BSsid = "";
        this.mFreeAPcellID = "";
        this.mConnectFailedSubErrorCode = (short) 0;
        this.mConnectFailedReason = (short) 0;
        this.dftType = WIFI_SECURITY_TYPE_UNKNOWN;
        this.mCheckReason = (byte) 0;
        this.mLastAccessWebSlowTime = 0;
        this.mAccessWebSlowCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.strSpeedInfo = "";
        this.strUIDSpeedInfo = "";
        this.mMultiGWCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mUploadAccessWebSlowly = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mUploadAccessWebSlowlySSID = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mCounter_monitor = HwCHRWifiLinkMonitor.getDefault();
        this.mRssiGroup = new HwCHRWifiRSSIGroupSummery();
        this.mCountryCode = "";
        this.mIpType = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.accessSlowlyerrrCode = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mFailReason = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAPSecurity = WIFI_SECURITY_TYPE_UNKNOWN;
        this.mHomeAPJudgeTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mBG_AC_DiffType = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAWS = null;
        this.mUploadAccessWebFailedCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mLastAccessWebFailedTime = 0;
        this.mRssi2gMaxRssi = -127;
        this.mRssi2gMinRssi = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRssi2gSum = new int[SECURITY_WAPI_CERT];
        this.mRssi2gCnt = new int[SECURITY_WAPI_CERT];
        this.mpoorRssi2gCnt = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRssi5gMaxRssi = -127;
        this.mRssi5gMinRssi = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRssi5gSum = new int[SECURITY_WAPI_CERT];
        this.mRssi5gCnt = new int[SECURITY_WAPI_CERT];
        this.mpoorRssi5gCnt = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mNetworkId = WIFI_SECURITY_TYPE_UNKNOWN;
        this.mLastNetworkId = WIFI_SECURITY_TYPE_UNKNOWN;
        this.mFirstDisconnectTime = 0;
        this.mSecondDisconnectTime = 0;
        this.mDisableNetworkflag = (byte) 0;
        this.mDisconnectCnt = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mDeltaTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mNeedEnableNetworkId = WIFI_SECURITY_TYPE_UNKNOWN;
        this.mEssCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRssiSum = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRssiCnt = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mLastApkName = "";
        this.mUserAction = "";
        this.mApkTriggerTimes = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mApkAction = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.wifiAntsStatus = new WifiAntsStatus();
        this.mWifiAntsPrevSWCnt = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mDualAntsChr = HWFLOW;
        this.mHwLogUtils = null;
        this.mIsPortalConnection = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mPortalStatus = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mCurrentApSecurity = WIFI_SECURITY_TYPE_UNKNOWN;
        this.mCurrentApBssid = "";
        this.mNetSlowlyTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mNetNormalTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mContextRef = null;
        this.mHwWifiDFTUtilImpl = null;
        this.mTimeStampSessionStart = 0;
        this.mTimeStampSessionFirstConnect = 0;
        this.mTimeStampSessionFinish = 0;
        this.mConnectSuccessTime = 0;
        this.mConnectType = "";
        this.mHwCHRAccessNetworkEventInfoList = new ArrayList();
        this.mConnectThreadName = "";
        this.mDisableThreadName = "";
        this.mRouterModel = "";
        this.mRouterBrand = "";
        this.mLock = new ReentrantLock();
        this.mTriggerReportType = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mWifiRepeaterWorkingTime = 0;
        this.mWifiRepeaterOpenedCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mWifiStateMachine = null;
        this.mDiffFreqStationRepeaterDuration = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mPrevStaChannel = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRepeterMaxClientCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRepeterConnFailedCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRepeterDiffBegin = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRepeterDiffEnd = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRepeterOpenOrClose = WIFI_SECURITY_TYPE_UNKNOWN;
        this.connectedType = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.connMgr = null;
        this.networkInfoWlan = null;
        this.networkInfoMobile = null;
        this.mDhcpSerialNo = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.wifiproCanotConnectForLongCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mWifiSettingStateErrorReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (action != null) {
                        if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                            if (intent.getExtra("android.intent.extra.SHUTDOWN_USERSPACE_ONLY") != null && intent.getExtra("android.intent.extra.SHUTDOWN_USERSPACE_ONLY").toString().equals("true")) {
                                return;
                            }
                            if (HwWifiCHRStateManagerImpl.this.mHwWifiStatStore != null) {
                                HwWifiCHRStateManagerImpl.this.mHwWifiStatStore.updateDisconnectCnt();
                                HwWifiCHRStateManagerImpl.this.mHwWifiStatStore.triggerConnectedDuration();
                            }
                        } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                            HwWifiCHRStateManagerImpl.this.connMgr = (ConnectivityManager) context.getSystemService("connectivity");
                            if (HwWifiCHRStateManagerImpl.this.connMgr != null) {
                                HwWifiCHRStateManagerImpl.this.networkInfoWlan = HwWifiCHRStateManagerImpl.this.connMgr.getNetworkInfo(HwWifiCHRStateManagerImpl.WIFI_SETTING_OPENED_AND_SERVICE_CLOSED);
                                HwWifiCHRStateManagerImpl.this.networkInfoMobile = HwWifiCHRStateManagerImpl.this.connMgr.getNetworkInfo(HwWifiCHRStateManagerImpl.WIFI_SETTING_CLOSED_AND_SERVICE_OPENED);
                                if (!(HwWifiCHRStateManagerImpl.this.networkInfoWlan == null || HwWifiCHRStateManagerImpl.this.networkInfoMobile == null)) {
                                    if (HwWifiCHRStateManagerImpl.this.networkInfoWlan.isConnected()) {
                                        HwWifiCHRStateManagerImpl.this.connectedType = HwWifiCHRStateManagerImpl.WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                                    } else if (HwWifiCHRStateManagerImpl.this.networkInfoMobile.isConnected()) {
                                        HwWifiCHRStateManagerImpl.this.connectedType = HwWifiCHRStateManagerImpl.WIFI_SETTING_OPENED_TIMEOUT;
                                    } else {
                                        HwWifiCHRStateManagerImpl.this.connectedType = HwWifiCHRStateManagerImpl.WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                                    }
                                }
                                if (HwWifiCHRStateManagerImpl.this.mHwWifiStatStore != null) {
                                    HwWifiCHRStateManagerImpl.this.mHwWifiStatStore.updateCurrentConnectType(HwWifiCHRStateManagerImpl.this.connectedType);
                                }
                            }
                        }
                        if (HwWifiCHRStateManagerImpl.SETTING_AND_WIFISERVICE_STATE_DIFFERENT_ACTION.equals(action)) {
                            int flag = intent.getIntExtra(HwWifiCHRStateManagerImpl.WIFI_OPEN_CLOSE_FAILED_STATE, HwWifiCHRStateManagerImpl.WIFI_SECURITY_TYPE_UNKNOWN);
                            if (flag != HwWifiCHRStateManagerImpl.WIFI_SECURITY_TYPE_UNKNOWN) {
                                switch (flag) {
                                    case HwWifiCHRStateManagerImpl.WIFI_SETTING_CLOSED_AND_SERVICE_OPENED /*0*/:
                                        HwWifiCHRStateManagerImpl.this.updateWifiException(80, "WIFI_SETTING_CLOSED_AND_SERVICE_OPENED");
                                        break;
                                    case HwWifiCHRStateManagerImpl.WIFI_SETTING_OPENED_AND_SERVICE_CLOSED /*1*/:
                                        HwWifiCHRStateManagerImpl.this.updateWifiException(81, "WIFI_SETTING_OPENED_AND_SERVICE_CLOSED");
                                        break;
                                    case HwWifiCHRStateManagerImpl.WIFI_SETTING_OPENED_TIMEOUT /*2*/:
                                        HwWifiCHRStateManagerImpl.this.updateWifiException(80, "TIMEOUT");
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        };
        this.mChrLogModel = new ChrLogModel();
        this.mLockObj = new Object();
        this.mChrLogBaseModelList = new ArrayList();
        resetDailyStat();
        this.mLocalBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHwWifiStatStore = HwWifiStatStoreImpl.getDefault();
        this.mapWifiEventReaseon = new HashMap();
        initWifiEventReasonMap();
        this.mHwWifiDFTConst = new HwWifiDFTConst();
        this.mHwLogUtils = HwWiFiLogUtils.getDefault();
        this.mHwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
    }

    public void setIsDualAntsChr(boolean isAntsChr) {
        this.mDualAntsChr = isAntsChr;
    }

    public static HwWifiCHRStateManager getDefault() {
        return wsm;
    }

    public static HwWifiCHRStateManagerImpl getDefaultImpl() {
        return (HwWifiCHRStateManagerImpl) wsm;
    }

    public void updateScreenState(boolean on) {
        this.mScreenState = on ? WIFI_SETTING_OPENED_AND_SERVICE_CLOSED : WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        if (this.mScreenState == WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
            restoreDisableNetwork();
            updateConnectType("SCREENON_TO_SUCCESS");
            updateTimeStampSessionStart(SystemClock.elapsedRealtime());
        }
    }

    public void updateChannel(int channel) {
        this.usAP_channel = (short) channel;
        if (getRepeaterStatus() == WIFI_REPEATER_TETHER) {
            int wifiRepeaterFreq = getRepeaterFreq();
            if (wifiRepeaterFreq != 0 && this.mPrevStaChannel != 0 && channel != this.mPrevStaChannel && channel != wifiRepeaterFreq) {
                this.mRepeterDiffBegin = Integer.parseInt(String.valueOf(SystemClock.elapsedRealtime() / 1000));
            } else if (!(channel == this.mPrevStaChannel || channel != wifiRepeaterFreq || channel == 0)) {
                this.mRepeterDiffEnd = Integer.parseInt(String.valueOf(SystemClock.elapsedRealtime() / 1000));
                this.mDiffFreqStationRepeaterDuration += this.mRepeterDiffEnd - this.mRepeterDiffBegin;
                this.mRepeterDiffEnd = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                this.mRepeterDiffBegin = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
            }
            if (HWFLOW) {
                Log.d(TAG, "updateChannel channel = " + channel + ", DiffFreqStationRepeaterDuration = " + this.mDiffFreqStationRepeaterDuration);
            }
        }
        this.mPrevStaChannel = channel;
    }

    public void updateCHRCounters() {
        this.mCounter_monitor.runCounters();
    }

    public void setCHRCounters(Object obj) {
        if (obj != null && (obj instanceof HwCHRWifiBcmIncrCounterLst)) {
            this.mCounter_monitor.setCounterLst((HwCHRWifiBcmIncrCounterLst) obj);
        }
    }

    public void updateRSSI(int rssi) {
        if (rssi < 0 && rssi > -110) {
            if (this.mRssi >= 0 || this.mRssi <= -110) {
                this.mRssi = rssi;
            } else {
                this.mRssi = (this.mRssi + rssi) / WIFI_SETTING_OPENED_TIMEOUT;
            }
        }
        this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.hwWifiCHRService != null) {
            this.hwWifiCHRService.setRssi(rssi);
        }
        if (HwArpVerifier.getDefault() != null) {
            HwArpVerifier.getDefault();
            HwArpVerifier.setRssi(rssi);
        }
    }

    public void updateLinkSpeed(int linkSpeed) {
        this.usLinkSpeed = (short) linkSpeed;
    }

    public void updateStaMAC(String mac) {
        if (mac == null) {
            mac = "";
        }
        this.strSta_mac = mac;
        Log.d(TAG, "update statMac: ******** ");
    }

    public void updateWifiIp(String ip) {
        if (ip == null) {
            ip = "";
        }
        this.str_Wifi_ip = ip;
        Log.d(TAG, "update wifiIP");
    }

    public void updateAPMAC(String AP_MAC) {
        if (AP_MAC == null) {
            AP_MAC = "";
        }
        this.strAp_mac = AP_MAC;
        Log.d(TAG, "Update AP_MAC:" + this.strAp_mac);
        this.mCurrentApBssid = this.strAp_mac;
        this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (!(this.hwWifiCHRService == null || TextUtils.isEmpty(this.strAp_mac))) {
            this.hwWifiCHRService.updateTargetBssid(this.strAp_mac);
        }
        this.mEssCount = (byte) (getRelatedApInfo(this.strAp_mac) >> ANT_SEC_WORK_TIME);
    }

    public void updateSpeedInfo(String speedInfo, String uidSpeedInfo, int errCode, int failReason) {
        this.strSpeedInfo = speedInfo;
        this.strUIDSpeedInfo = uidSpeedInfo;
        this.accessSlowlyerrrCode = errCode;
        this.mFailReason = failReason;
    }

    public void updateAPSsid(String ssid) {
        if (ssid == null) {
            this.strAp_Ssid = "";
        } else if (ssid.length() >= SSID_MAX_LENGTH) {
            this.strAp_Ssid = ssid.substring(WIFI_SETTING_CLOSED_AND_SERVICE_OPENED, 31);
        } else {
            this.strAp_Ssid = ssid;
        }
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.setAPSSID(this.strAp_Ssid);
            Log.d(TAG, "Update AP_Ssid " + this.strAp_Ssid);
            if (isValidHardwareAddr(this.mCurrentApBssid)) {
                this.mHwWifiStatStore.setApMac(this.mCurrentApBssid);
            }
        }
    }

    public void updateMultiGWCount(byte count) {
        this.mMultiGWCount = count;
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.setMultiGWCount(count);
        }
    }

    public void updateGateWay(String gateWay) {
        if (gateWay == null) {
            gateWay = "";
        }
        this.str_gate_ip = gateWay;
        Log.d("updateGateWay", "gateWay_ip:" + this.str_gate_ip);
    }

    public void updateDNS(Collection<InetAddress> dnses) {
        StringBuilder sBuilder_dns = new StringBuilder();
        if (dnses != null && dnses.size() > 0) {
            for (InetAddress addr : dnses) {
                sBuilder_dns.append(addr.getHostAddress()).append(";");
            }
            if (sBuilder_dns.length() > 0) {
                this.str_dns = sBuilder_dns.deleteCharAt(sBuilder_dns.length() + WIFI_SECURITY_TYPE_UNKNOWN).toString();
            }
        }
        Log.d("updateDNS", "updateDNS:" + this.str_dns);
    }

    public void updateStrucRoutes(String strucRoutes) {
        if (strucRoutes == null) {
            strucRoutes = "";
        }
        this.strRoutes = strucRoutes;
        Log.d(TAG, "updateStrucRoutes:" + this.strRoutes);
    }

    public void updateLeaseIP(long leaseTime) {
        this.strIp_leasetime = transeLongToString(leaseTime);
        Log.d(TAG, "updateLeaseTime:" + String.valueOf(leaseTime));
    }

    private String transeLongToString(long time) {
        long minute_value = time / 60;
        return ((minute_value / 60) / 24) + "D" + ((minute_value % 1440) / 60) + "H" + ((minute_value % 1440) % 60) + "M";
    }

    public void updateApMessage(String proto, String key_mgmt, String auth_alg, String pairwise, String group, String eap) {
        if (proto == null) {
            proto = "";
        }
        this.strAP_proto = proto;
        if (key_mgmt == null) {
            key_mgmt = "";
        }
        this.strAP_key_mgmt = key_mgmt;
        if (auth_alg == null) {
            auth_alg = "";
        }
        this.strAP_auth_alg = auth_alg;
        if (pairwise == null) {
            pairwise = "";
        }
        this.strAP_pairwise = pairwise;
        if (group == null) {
            group = "";
        }
        this.strAP_gruop = group;
        if (eap == null) {
            eap = "";
        }
        this.strAP_eap = eap;
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.setApencInfo(this.strAP_proto, this.strAP_key_mgmt, this.strAP_auth_alg, this.strAP_pairwise, this.strAP_gruop, this.strAP_eap);
        }
    }

    public void updateApVendorInfo(String vendorInfo) {
        String str;
        if (vendorInfo == null) {
            str = "";
        } else {
            str = vendorInfo;
        }
        this.mAPVendorInfo = str;
        Log.d(TAG, "updateApVendorInfo:" + vendorInfo);
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.setApVendorInfo(this.mAPVendorInfo);
        }
    }

    public void updateRouterModelInfo(String routerModel, String routerBrand) {
        if (routerModel == null) {
            routerModel = "";
        }
        this.mRouterModel = routerModel;
        if (routerBrand == null) {
            routerBrand = "";
        }
        this.mRouterBrand = routerBrand;
        Log.d(TAG, "updateRouterModelInfo mRouterModel = " + this.mRouterModel + " mRouterBrand = " + this.mRouterBrand);
    }

    public void updateWifiException(int ucErrorCode, String ucSubErrorCode) {
        Log.d(TAG, "errorcode " + ucErrorCode + ", suberrorcode =" + ucSubErrorCode);
        if (isWifiConnectTypeEvent(ucErrorCode)) {
            syncAddHwCHRAccessNetworkEventInfoList(constructHwCHRAccessNetworkEventInfo(ucErrorCode, ucSubErrorCode));
            if (this.mHwWifiDFTUtilImpl != null) {
                this.mConnectFailedReason = (short) ucErrorCode;
                uploadDFTEvent(909002014);
            }
            return;
        }
        writeNETInfo(ucErrorCode, ucSubErrorCode);
    }

    private boolean isWifiConnectTypeEvent(int ucErrorCode) {
        if (!(ucErrorCode == 82 || ucErrorCode == 84)) {
            if (ucErrorCode != 83) {
                return HWFLOW;
            }
        }
        return true;
    }

    public void updateAccessWebException(int cCheckReason, int errorCode) {
        Log.d(TAG, "updateAccessWebException errorcode " + 87 + "cCheckReason =" + cCheckReason);
        this.mCheckReason = (byte) cCheckReason;
        this.aucAccess_internet_failed = (short) errorCode;
        writeNETInfo(87, "CONNECT_INTENT_FAILED");
        this.aucAccess_internet_failed = (short) 0;
        this.mCheckReason = (byte) 0;
    }

    public void updateAccessWebException(int cCheckReason, String errReason) {
        if (HWFLOW) {
            Log.d(TAG, "updateAccessWebException errorcode " + 87 + "cCheckReason =" + cCheckReason);
        }
        this.mCheckReason = (byte) cCheckReason;
        writeNETInfo(87, errReason);
        this.mCheckReason = (byte) 0;
    }

    public void setLastNetIdFromUI(int netid) {
        Log.d(TAG, "setLastNetIdFromUI: " + netid);
        this.mLastNetIdFromUI = netid;
    }

    private int getBluetoothState() {
        if (this.mLocalBluetoothAdapter != null) {
            return this.mLocalBluetoothAdapter.getState();
        }
        return 10;
    }

    public void uploadTls12Stat(int ret, int status) {
        this.mWorkaroundCode = (short) 1;
        this.mWorkaroundRet = (short) ret;
        this.mWorkaroundStatus = (short) status;
        this.mWorkaroundRemark = "";
        updateWifiException(113, "");
    }

    private int getBluetoothConnectionState() {
        if (this.mLocalBluetoothAdapter == null) {
            return WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        }
        int stat = this.mLocalBluetoothAdapter.getConnectionState();
        if (stat == 0 && this.mLocalBluetoothAdapter.isDiscovering()) {
            return STATE_SCANING;
        }
        return stat;
    }

    public void syncSetScanResultsList(List<ScanDetail> scanList) {
        synchronized (this.mScanResultLock) {
            this.mScanResults.clear();
            for (ScanDetail result : scanList) {
                this.mScanResults.add(new ScanResult(result.getScanResult()));
            }
        }
    }

    public void clearHwCHRAccessNetworkEventInfoList() {
        try {
            boolean bGetLock = this.mLock.tryLock(1, TimeUnit.SECONDS);
            if (bGetLock) {
                this.mHwCHRAccessNetworkEventInfoList.clear();
            }
            if (bGetLock) {
                this.mLock.unlock();
            }
        } catch (InterruptedException e) {
            if (HWFLOW) {
                Log.d(TAG, "clearHwCHRAccessNetworkEventInfoList" + e);
            }
            if (WIFI_SETTING_CLOSED_AND_SERVICE_OPENED != null) {
                this.mLock.unlock();
            }
        } catch (Throwable th) {
            if (WIFI_SETTING_CLOSED_AND_SERVICE_OPENED != null) {
                this.mLock.unlock();
            }
        }
    }

    public void updateTimeStampSessionStart(long TimeStampSessionStart) {
        this.mTimeStampSessionStart = TimeStampSessionStart;
        this.mTimeStampSessionFirstConnect = 0;
        this.mTimeStampSessionFinish = 0;
        this.mConnectSuccessTime = 0;
    }

    public void updateTimeStampSessionFirstConnect(long TimeStampSessionFirstConnect) {
        if (0 == this.mTimeStampSessionFirstConnect) {
            this.mTimeStampSessionFirstConnect = TimeStampSessionFirstConnect;
            this.mTimeStampSessionFinish = 0;
            this.mConnectSuccessTime = 0;
        }
    }

    public void updateTimeStampSessionFinish(long TimeStampFinish) {
        this.mTimeStampSessionFinish = TimeStampFinish;
    }

    public void updateConnectType(String ConnectType) {
        this.mConnectType = ConnectType;
        this.mConnectThreadName = "";
        this.mDisableThreadName = "";
    }

    public void updateConnectThreadName(String ConnectThreadName) {
        String appNameOfUI = "com.android.settings";
        if (ConnectThreadName == null) {
            this.mConnectThreadName = "";
        } else {
            this.mConnectThreadName = ConnectThreadName;
        }
        reportHwCHRAccessNetworkEventInfoList(MAX_DISCONN_CNT);
        if (appNameOfUI.equals(this.mConnectThreadName)) {
            updateConnectType("MANUALCONNECT_TO_SUCCESS");
        } else {
            updateConnectType("APKCONNECT_TO_SUCCESS");
        }
        updateTimeStampSessionStart(SystemClock.elapsedRealtime());
    }

    public void updateDisableThreadName(String DisableThreadName) {
        if (DisableThreadName != null) {
            this.mDisableThreadName = DisableThreadName;
        }
    }

    public void updateConnectSuccessTime() {
        this.mConnectSuccessTime = this.mTimeStampSessionFinish - this.mTimeStampSessionStart;
    }

    public void updateConnectSuccessTime(long connectSuccessTime) {
        if (connectSuccessTime <= 2147483647L) {
            this.mConnectSuccessTime = connectSuccessTime;
        }
    }

    public void syncAddHwCHRAccessNetworkEventInfoList(HwCHRAccessNetworkEventInfo stHwCHRAccessNetworkEventInfo) {
        if (HWFLOW) {
            Log.d(TAG, "====syncAddHwCHRAccessNetworkEventInfoList getEventId =" + stHwCHRAccessNetworkEventInfo.getEventId());
        }
        try {
            boolean bGetLock = this.mLock.tryLock(1, TimeUnit.SECONDS);
            if (bGetLock) {
                if (this.mHwCHRAccessNetworkEventInfoList.size() >= 20) {
                    reportHwCHRAccessNetworkEventInfoList(SECURITY_WAPI_CERT);
                }
                this.mHwCHRAccessNetworkEventInfoList.add(stHwCHRAccessNetworkEventInfo);
            }
            if (bGetLock) {
                this.mLock.unlock();
            }
        } catch (InterruptedException e) {
            if (HWFLOW) {
                Log.d(TAG, "syncAddHwCHRAccessNetworkEventInfoList" + e);
            }
            if (WIFI_SETTING_CLOSED_AND_SERVICE_OPENED != null) {
                this.mLock.unlock();
            }
        } catch (Throwable th) {
            if (WIFI_SETTING_CLOSED_AND_SERVICE_OPENED != null) {
                this.mLock.unlock();
            }
        }
    }

    public void reportHwCHRAccessNetworkEventInfoList(int ReportReason) {
        if (!isWifiProEvaluate()) {
            boolean z = HWFLOW;
            try {
                z = this.mLock.tryLock(1, TimeUnit.SECONDS);
                if (z) {
                    this.mTriggerReportType = ReportReason;
                    if (HWFLOW) {
                        Log.d(TAG, "reportHwCHRAccessNetworkEventInfoList:ReportReason = " + ReportReason);
                    }
                    if (WIFI_SETTING_OPENED_AND_SERVICE_CLOSED == ReportReason) {
                        if (this.mHwCHRAccessNetworkEventInfoList.size() > 0 && !this.mCurrentApBssid.equals(((HwCHRAccessNetworkEventInfo) this.mHwCHRAccessNetworkEventInfoList.get(WIFI_SETTING_CLOSED_AND_SERVICE_OPENED)).getAP_MAC())) {
                            if (this.mHwWifiStatStore != null) {
                                this.mHwWifiStatStore.updateCHRConnectFailedCount(WIFI_SETTING_OPENED_AND_SERVICE_CLOSED);
                            }
                            updateTimeStampSessionFinish(SystemClock.elapsedRealtime());
                            updateWifiException(214, "");
                            clearHwCHRAccessNetworkEventInfoList();
                            updateTimeStampSessionStart(SystemClock.elapsedRealtime());
                        }
                        updateTimeStampSessionFirstConnect(SystemClock.elapsedRealtime());
                    } else if (this.mHwCHRAccessNetworkEventInfoList.size() <= 0) {
                        if (z) {
                            this.mLock.unlock();
                        }
                        return;
                    } else {
                        if (this.mHwWifiStatStore != null) {
                            this.mHwWifiStatStore.updateCHRConnectFailedCount(WIFI_SETTING_CLOSED_AND_SERVICE_OPENED);
                        }
                        updateWifiException(214, "");
                        clearHwCHRAccessNetworkEventInfoList();
                    }
                }
                if (z) {
                    this.mLock.unlock();
                }
            } catch (InterruptedException e) {
                if (HWFLOW) {
                    Log.d(TAG, "reportHwCHRAccessNetworkEventInfoList" + e);
                }
                if (z) {
                    this.mLock.unlock();
                }
            } catch (Throwable th) {
                if (z) {
                    this.mLock.unlock();
                }
            }
        }
    }

    public HwCHRAccessNetworkEventInfo constructHwCHRAccessNetworkEventInfo(int ucErrorCode, String ucSubErrorCode) {
        HwCHRAccessNetworkEventInfo stHwCHRAccessNetworkEventInfo = new HwCHRAccessNetworkEventInfo();
        if (HWFLOW) {
            Log.d(TAG, "reportHwCHRAccessNetworkEventInfoList  errorcode " + ucErrorCode + ", suberrorcode =" + ucSubErrorCode);
        }
        stHwCHRAccessNetworkEventInfo.setEventId(ucErrorCode);
        byte essCount = (byte) (getRelatedApInfo(this.strAp_mac) >> ANT_SEC_WORK_TIME);
        stHwCHRAccessNetworkEventInfo.setConnectType(this.mConnectType);
        stHwCHRAccessNetworkEventInfo.setEventTriggerDate(new Date());
        stHwCHRAccessNetworkEventInfo.setAP_MAC(this.mCurrentApBssid);
        stHwCHRAccessNetworkEventInfo.setAP_SSID(this.strAp_Ssid);
        stHwCHRAccessNetworkEventInfo.setAP_proto(this.strAP_proto);
        stHwCHRAccessNetworkEventInfo.setAP_key_mgmt(this.strAP_key_mgmt);
        stHwCHRAccessNetworkEventInfo.setAP_auth_alg(this.strAP_auth_alg);
        stHwCHRAccessNetworkEventInfo.setAP_pairwise(this.strAP_pairwise);
        stHwCHRAccessNetworkEventInfo.setAP_group(this.strAP_gruop);
        stHwCHRAccessNetworkEventInfo.setAP_eap(this.strAP_eap);
        stHwCHRAccessNetworkEventInfo.setAP_link_speed(this.usLinkSpeed);
        stHwCHRAccessNetworkEventInfo.setAP_RSSI(this.mRssi);
        stHwCHRAccessNetworkEventInfo.setAP_channel(this.usAP_channel);
        stHwCHRAccessNetworkEventInfo.setBTState((short) ((byte) getBluetoothState()));
        stHwCHRAccessNetworkEventInfo.setBTConnState((short) ((byte) getBluetoothConnectionState()));
        stHwCHRAccessNetworkEventInfo.setPublicEss((short) essCount);
        stHwCHRAccessNetworkEventInfo.set_rssi_summery(this.mRssiGroup);
        stHwCHRAccessNetworkEventInfo.setIsOnScreen(this.mScreenState);
        switch (ucErrorCode) {
            case HwWifiCHRConstImpl.WIFI_CONNECT_AUTH_FAILED /*82*/:
                stHwCHRAccessNetworkEventInfo.setSubErrorCode(this.mAuthSubCode);
                this.mConnectFailedSubErrorCode = (short) this.mAuthSubCode;
                break;
            case HwWifiCHRConstImpl.WIFI_CONNECT_ASSOC_FAILED /*83*/:
                stHwCHRAccessNetworkEventInfo.setSubErrorCode(this.mSubcodeReject);
                this.mConnectFailedSubErrorCode = this.mSubcodeReject;
                break;
            case HwWifiCHRConstImpl.WIFI_CONNECT_DHCP_FAILED /*84*/:
                stHwCHRAccessNetworkEventInfo.setDHCP_FAILED(ucSubErrorCode);
                stHwCHRAccessNetworkEventInfo.setSubErrorCode(this.aucDhcp_failed);
                break;
        }
        if (HWFLOW) {
            Log.d(TAG, "stHwCHRAccessNetworkEventInfo = " + stHwCHRAccessNetworkEventInfo.getAP_SSID());
        }
        return stHwCHRAccessNetworkEventInfo;
    }

    public void resetWhenDisconnect() {
        Log.e(TAG, "resetWhenDisconnect Reset Info");
        this.usLinkSpeed = (short) 0;
        this.usAP_channel = (short) 0;
        this.strAp_mac = "";
        this.strAp_Ssid = "";
        this.strIp_leasetime = "";
        this.strRoutes = "";
        this.str_gate_ip = "";
        this.str_dns = "";
        this.str_Wifi_ip = "";
        this.strAP_proto = "";
        this.strAP_key_mgmt = "";
        this.strAP_auth_alg = "";
        this.strAP_pairwise = "";
        this.strAP_gruop = "";
        this.strAP_eap = "";
        this.mRemark = "";
        this.mAPVendorInfo = "";
        this.mRssi = WifiHandover.INVALID_RSSI;
        this.mUploadAccessWebSlowlySSID = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mLastAccessWebSlowTime = 0;
        this.mIpType = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mUploadAccessWebFailedCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mLastAccessWebFailedTime = 0;
        this.mIsPortalConnection = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mPortalStatus = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mCurrentApSecurity = WIFI_SECURITY_TYPE_UNKNOWN;
        this.mNetSlowlyTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mNetNormalTime = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
    }

    public ScanResult getScanResultByBssid(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            return null;
        }
        synchronized (this.mScanResultLock) {
            for (ScanResult sr : this.mScanResults) {
                if (bssid.equals(sr.BSSID)) {
                    return sr;
                }
            }
            return null;
        }
    }

    private boolean isValidHardwareAddr(String mac) {
        if (TextUtils.isEmpty(mac) || MessageUtil.ILLEGAL_BSSID_02.equals(mac) || "FF:FF:FF:FF:FF:FF".equalsIgnoreCase(mac)) {
            return HWFLOW;
        }
        return true;
    }

    private int getRelatedApInfo(String bssid) {
        int essCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        int relatedCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        int sameFreq = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        ScanResult currentSR = null;
        if (!isValidHardwareAddr(bssid)) {
            return WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        }
        this.mRssiGroup = HwArpVerifier.getDefault().getRSSIGroup();
        synchronized (this.mScanResultLock) {
            try {
                for (ScanResult result : this.mScanResults) {
                    if (bssid.equals(result.BSSID)) {
                        currentSR = result;
                        break;
                    }
                }
                if (currentSR == null) {
                    return WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                }
                int bandWith;
                if (currentSR.is24GHz()) {
                    bandWith = 20;
                } else {
                    bandWith = 40;
                }
                for (ScanResult result2 : this.mScanResults) {
                    if (currentSR.SSID.equals(result2.SSID) && currentSR.capabilities.equals(result2.capabilities)) {
                        essCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                    }
                    if (currentSR.frequency == result2.frequency) {
                        sameFreq += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                        this.mRssiGroup.addRelationAps(true, result2.level);
                    } else if (Math.abs(currentSR.frequency - result2.frequency) < bandWith) {
                        relatedCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                        this.mRssiGroup.addRelationAps(HWFLOW, result2.level);
                    }
                }
                loge("getRelatedApInfo( sameFreq:" + sameFreq + ", essCount:" + essCount + " ,  relatedCount:" + relatedCount + ")");
                return ((essCount << ANT_SEC_WORK_TIME) | (sameFreq << STATE_SCANING)) | relatedCount;
            } catch (Exception e) {
            }
        }
    }

    public void updateSignalLevel(int rssi, int freq, int level) {
        if (level >= SECURITY_WAPI_CERT || level < 0 || freq < 0) {
            Log.d(TAG, "update SignalLevel error.");
            return;
        }
        this.mRssiSum += rssi;
        int[] iArr;
        if (is2G(freq)) {
            iArr = this.mRssi2gSum;
            iArr[level] = iArr[level] + rssi;
            iArr = this.mRssi2gCnt;
            iArr[level] = iArr[level] + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
            this.mpoorRssi2gCnt = level == SECURITY_WAPI_PSK ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mpoorRssi2gCnt + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
            if (this.mRssi2gMaxRssi < rssi) {
                this.mRssi2gMaxRssi = rssi;
            } else if (rssi < this.mRssi2gMinRssi) {
                this.mRssi2gMinRssi = rssi;
            }
        } else {
            iArr = this.mRssi5gSum;
            iArr[level] = iArr[level] + rssi;
            iArr = this.mRssi5gCnt;
            iArr[level] = iArr[level] + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
            this.mpoorRssi5gCnt = level == SECURITY_WAPI_PSK ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mpoorRssi5gCnt + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
            if (this.mRssi5gMaxRssi < rssi) {
                this.mRssi5gMaxRssi = rssi;
            } else if (rssi < this.mRssi5gMinRssi) {
                this.mRssi5gMinRssi = rssi;
            }
        }
        int rssi2gCnt = (((this.mRssi2gCnt[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED] + this.mRssi2gCnt[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED]) + this.mRssi2gCnt[WIFI_SETTING_OPENED_TIMEOUT]) + this.mRssi2gCnt[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE]) + this.mRssi2gCnt[SECURITY_WAPI_PSK];
        int rssi5gCnt = (((this.mRssi5gCnt[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED] + this.mRssi5gCnt[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED]) + this.mRssi5gCnt[WIFI_SETTING_OPENED_TIMEOUT]) + this.mRssi5gCnt[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE]) + this.mRssi5gCnt[SECURITY_WAPI_PSK];
        this.mRssiCnt = rssi2gCnt + rssi5gCnt;
        if (rssi2gCnt + rssi5gCnt == 0) {
            Log.d(TAG, "poor rssi count error.");
        } else if (rssi5gCnt != 0 && (this.mpoorRssi5gCnt > POOR_RSSI_TRIGGER_TIME_THRESHOLD || (rssi5gCnt > POOR_RSSI_TRIGGER_TIME_THRESHOLD && (this.mRssi5gCnt[SECURITY_WAPI_PSK] * 100) / rssi5gCnt < SECURITY_WAPI_CERT))) {
            writeNETInfo(HwDualBandMessageUtil.CMD_STOP_MONITOR, "");
            clearWifiPoorLevel();
        } else if (rssi2gCnt != 0 && (this.mpoorRssi2gCnt > POOR_RSSI_TRIGGER_TIME_THRESHOLD || (rssi2gCnt > POOR_RSSI_TRIGGER_TIME_THRESHOLD && (this.mRssi2gCnt[SECURITY_WAPI_PSK] * 100) / rssi2gCnt < SECURITY_WAPI_CERT))) {
            writeNETInfo(HwDualBandMessageUtil.CMD_STOP_MONITOR, "");
            clearWifiPoorLevel();
        } else if (this.mpoorRssi2gCnt + this.mpoorRssi5gCnt > POOR_RSSI_TRIGGER_TIME_THRESHOLD || (rssi2gCnt + rssi5gCnt > POOR_RSSI_TRIGGER_TIME_THRESHOLD && ((this.mRssi2gCnt[SECURITY_WAPI_PSK] + this.mRssi5gCnt[SECURITY_WAPI_PSK]) * 100) / (rssi2gCnt + rssi5gCnt) < SECURITY_WAPI_CERT)) {
            writeNETInfo(HwDualBandMessageUtil.CMD_STOP_MONITOR, "");
            clearWifiPoorLevel();
        }
    }

    private boolean is2G(int freq) {
        return freq < 2500 ? true : HWFLOW;
    }

    private static int getSecurity(ScanResult result) {
        if (result == null) {
            return WIFI_SECURITY_TYPE_UNKNOWN;
        }
        if (result.capabilities.contains("WEP")) {
            return WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
        }
        if (result.capabilities.contains("WAPI-PSK")) {
            return SECURITY_WAPI_PSK;
        }
        if (result.capabilities.contains("WAPI-CERT")) {
            return SECURITY_WAPI_CERT;
        }
        if (result.capabilities.contains("PSK")) {
            return WIFI_SETTING_OPENED_TIMEOUT;
        }
        if (result.capabilities.contains("EAP")) {
            return UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE;
        }
        return WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
    }

    private CSubApRoaming getApRoaming(String ssid, String bssid) {
        CSubApRoaming ApRoaming = new CSubApRoaming();
        int firstRssi = -100;
        int secondRssi = -100;
        int firstChannel = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        int secondChannel = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        if (ssid.isEmpty() || bssid.isEmpty()) {
            return ApRoaming;
        }
        ScanResult currentAP = getScanResultByBssid(bssid);
        if (currentAP != null) {
            this.mCurrentApSecurity = getSecurity(currentAP);
        } else if (this.mCurrentApSecurity == WIFI_SECURITY_TYPE_UNKNOWN) {
            return ApRoaming;
        }
        synchronized (this.mScanResultLock) {
            try {
                for (ScanResult result : this.mScanResults) {
                    if (ssid.equals(result.SSID) && this.mCurrentApSecurity == getSecurity(result)) {
                        if (bssid.equals(result.BSSID)) {
                            continue;
                        } else if (result.level > firstRssi) {
                            secondRssi = firstRssi;
                            secondChannel = firstChannel;
                            firstRssi = result.level;
                            firstChannel = result.frequency;
                        } else if (result.level > secondRssi) {
                            secondRssi = result.level;
                            secondChannel = result.frequency;
                        }
                    }
                }
                ApRoaming.iFirst_Rssi.setValue(firstRssi);
                ApRoaming.iFirst_Channel.setValue(firstChannel);
                ApRoaming.iSecond_Rssi.setValue(secondRssi);
                ApRoaming.iSecond_Channel.setValue(secondChannel);
            } catch (RuntimeException e) {
                Log.d(TAG, "RuntimeException e" + e);
            } catch (Exception e2) {
                Log.d(TAG, "Exception e" + e2);
            }
        }
        Log.d(TAG, "getApRoaming( first_rssi:" + ApRoaming.iFirst_Rssi.getValue() + "  first_channel:" + ApRoaming.iFirst_Channel.getValue() + " second_rssi:" + ApRoaming.iSecond_Rssi.getValue() + "  second_channel:" + ApRoaming.iSecond_Channel.getValue() + ")");
        return ApRoaming;
    }

    public void updateApkChangewWifiStatus(int apkAction, String apkName, int count, String userAction) {
        this.mLastApkName = apkName;
        this.mApkTriggerTimes = count;
        this.mApkAction = apkAction;
        this.mUserAction = userAction;
        writeNETInfo(98, "");
    }

    private boolean isWifiProEvaluate() {
        int tag = WIFI_SECURITY_TYPE_UNKNOWN;
        if (this.mContextRef != null) {
            tag = Secure.getInt(this.mContextRef.getContentResolver(), WIFI_EVALUATE_TAG, WIFI_SETTING_CLOSED_AND_SERVICE_OPENED);
        }
        if (tag == WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
            return true;
        }
        return HWFLOW;
    }

    private void writeNETInfo(int type, String ucSubErrorCode) {
        if (!SystemProperties.getBoolean("ro.config.hw_gps_wifi_chr", true)) {
            return;
        }
        if (SystemProperties.getBoolean("persist.sys.wifi_chr_onoff.dis", true)) {
            Date date = new Date();
            Log.d(TAG, "type:" + type + ", writeNETInfo begin");
            boolean commercialUser = LogManager.getInstance().isCommercialUser();
            preWifiException(commercialUser);
            if (this.mHwWifiStatStore != null) {
                this.mHwWifiStatStore.updateUserType(commercialUser);
            }
            if (this.mHwWifiDFTUtilImpl != null) {
                this.dftType = this.mHwWifiDFTUtilImpl.getDFTEventType(type);
                this.mHwWifiDFTUtilImpl.updateWifiDFTEvent(type, ucSubErrorCode);
            }
            String strReason = null;
            if (ConnectivityLogManager.mapCHRDataPlus.containsKey(Integer.valueOf(type))) {
                CHRDataPlus dataPlus = ConnectivityLogManager.mapGetCHRData(type);
                if (dataPlus != null) {
                    strReason = dataPlus.getErrorReason();
                }
            }
            if (strReason == null) {
                strReason = ucSubErrorCode;
            } else if (strReason.isEmpty()) {
                strReason = ucSubErrorCode;
            }
            List<ChrLogBaseModel> result = getWiFiCHRModel(type, strReason, commercialUser, date);
            if (result.size() != 0) {
                for (int i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < result.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                    Log.d(TAG, "normal@trigger:  type:" + type + " ; mWifiType = " + mWifiType + " ; msg_type = " + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED);
                    ConnectivityLogManager.getInstance().reportAbnormalEventEx((ChrLogBaseModel) result.get(i), mWifiType, WIFI_SETTING_OPENED_AND_SERVICE_CLOSED, type, date, WIFI_SETTING_OPENED_AND_SERVICE_CLOSED);
                }
            } else {
                Log.d(TAG, "normal@not trigger:  type:" + type + " ; mWifiType" + mWifiType);
            }
            long elapsedRealtime = SystemClock.elapsedRealtime();
            if (elapsedRealtime - this.mDailyUploadTime > COMM_UPLOAD_MIN_SPAN) {
                this.mDailyUploadTime = elapsedRealtime;
                resetDailyStat();
            }
            return;
        }
        Log.e(TAG, "wifi chr is turn off, do not upload wifi chr log.");
    }

    private void preWifiException(boolean commercialUser) {
        if (commercialUser) {
            this.strSta_mac = "";
            this.str_Wifi_ip = maskIpAddress(this.str_Wifi_ip);
            this.strRoutes = "";
        }
    }

    public void uploadConnectFailed(int netid, int reason) {
        Log.d(TAG, "lastnetid:" + this.mLastNetIdFromUI + ",netid:" + netid + ",reason:" + reason);
        long now = SystemClock.elapsedRealtime();
        if (now - this.mLastConnectFailTimestamp >= this.MIN_UPLOAD_CONNECT_FAIL_SPAN) {
            this.mLastConnectFailTimestamp = now;
            this.mAuthSubCode = (short) reason;
            if (this.mLastNetIdFromUI == netid && netid != WIFI_SECURITY_TYPE_UNKNOWN) {
                this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                if (this.hwWifiCHRService != null) {
                    this.hwWifiCHRService.disableNetwork(netid, reason);
                    updateWifiException(82, "");
                } else {
                    updateWifiException(82, "");
                }
                this.mLastNetIdFromUI = WIFI_SECURITY_TYPE_UNKNOWN;
            } else if (UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE == reason) {
                this.mAuthSubCode = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                updateWifiException(82, "");
            }
        }
    }

    public void uploadAssocRejectException(String eventData) {
        int idx;
        this.mSubcodeReject = (short) -1;
        if (eventData != null) {
            idx = eventData.indexOf("status_code=");
        } else {
            idx = WIFI_SECURITY_TYPE_UNKNOWN;
        }
        if (idx >= 0) {
            try {
                this.mSubcodeReject = Short.parseShort(eventData.substring(idx + 12));
            } catch (Exception e) {
                Log.d(TAG, "AssocReject error status code:" + eventData);
                this.mSubcodeReject = (short) -1;
            }
        }
        uploadAssocRejectException(this.mSubcodeReject);
    }

    public void uploadAssocRejectException(int status, String bssid) {
        ScanResult scanResult = getScanResultByBssid(bssid);
        if (scanResult != null) {
            updateChannel(scanResult.frequency);
        }
        uploadAssocRejectException(status);
    }

    public void uploadAssocRejectException(int status) {
        this.mSubcodeReject = (short) status;
        if (this.mSubcodeReject > (short) 0 && this.mSubcodeReject != (short) 17) {
            updateWifiException(83, "");
        }
        this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mSubcodeReject > (short) 0 && this.hwWifiCHRService != null) {
            this.hwWifiCHRService.assocRejectEvent(status);
        }
        if (this.mSubcodeReject > (short) 0 && this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.updateReasonCode(83, status);
        }
    }

    public void uploadDhcpException(String strDhcpError) {
        this.aucDhcp_failed = (short) 0;
        if (strDhcpError.contains("Timed out waiting for dhcpcd to start")) {
            updateWifiException(84, "DHCPCD_TO_START");
        } else if (strDhcpError.contains("Timed out waiting for DHCP to finish") || strDhcpError.equals("dhcpclient Timed out")) {
            updateWifiException(84, "DHCP_TO_FINISH");
        } else if (strDhcpError.contains("DHCP result property was not set")) {
            updateWifiException(84, "PROPERTY_WAS_NOT_SET");
        } else if (strDhcpError.contains("DHCP result was")) {
            updateWifiException(84, "RESULT_WAS");
        } else if (strDhcpError.contains("Timed out waiting for DHCP Renew to finish") || strDhcpError.equals("dhcpclient renew Timed out")) {
            updateWifiException(84, "DHCP_RENEW_TO_FINISH");
        } else if (strDhcpError.contains("DHCP Renew result property was not set")) {
            updateWifiException(84, "RENEW_RESULT_PROPERTY_WAS_NOT_SET");
        } else if (strDhcpError.contains("DHCP Renew result was")) {
            updateWifiException(84, "RENEW_RESULT_WAS");
        } else if (strDhcpError.contains("dhcpclient initialize failed")) {
            updateWifiException(84, "CLIENT_INIT_FAILED");
        } else if (strDhcpError.contains("dhcpclient Configured IP failed")) {
            updateWifiException(84, "CLIENT_CONFIG_IP_FAILED");
        } else {
            updateWifiException(84, strDhcpError);
        }
    }

    public void updateDhcpSerialNo(int serialNo) {
        this.mDhcpSerialNo = serialNo;
    }

    public int getDhcpSerialNo() {
        return this.mDhcpSerialNo;
    }

    public void waitForDhcpStopping(String ifname) {
        if (!LogManager.getInstance().isCommercialUserFromCache()) {
            if ("wlan0".equals(ifname) || "p2p".equals(ifname)) {
                String propname = "init.svc.dhcpcd_wlan0";
                String errorcode = "STOP_ERROR_WLAN";
                String dhcpsrvname = "dhcpcd_wlan0";
                if ("p2p".equals(ifname)) {
                    propname = "init.svc.dhcpcd_p2p";
                    errorcode = "STOP_ERROR_P2P";
                    dhcpsrvname = "dhcpcd_p2p";
                }
                int count = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                String dhcpStauts = SystemProperties.get(propname, "stopped");
                while (count < SECURITY_WAPI_CERT && "running".equals(dhcpStauts)) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    count += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                    dhcpStauts = SystemProperties.get(propname, "stopped");
                    log("ifname: " + ifname + ",dhcpStauts: " + dhcpStauts + ",count:" + count);
                }
                if ("running".equals(dhcpStauts)) {
                    uploadDhcpException(errorcode);
                }
            }
        }
    }

    public void uploadDhcpException(int result, int serialNo) {
        if (serialNo != this.mDhcpSerialNo) {
            Log.d(TAG, "uploadDhcpException:skip serial:" + serialNo + "current serial:" + this.mDhcpSerialNo);
            return;
        }
        this.aucDhcp_failed = (short) (result > 0 ? WIFI_SETTING_OPENED_TIMEOUT : WIFI_SETTING_OPENED_AND_SERVICE_CLOSED);
        Log.d(TAG, "uploadDhcpException:" + this.aucDhcp_failed);
        updateWifiException(84, "DHCPCD_TO_START");
    }

    public void uploadWifiStat() {
        updateWifiException(HwWifiCHRConstImpl.WIFI_STABILITY_STAT, "");
    }

    public void uploadUserConnectFailed(int type) {
        if (!isWifiProEvaluate()) {
            updateWifiException(MessageUtil.CMD_ON_STOP, String.valueOf(type));
        }
    }

    public void uploadDisconnectException(String remainder) {
        Log.d(TAG, "remainder =" + remainder);
        int ind = remainder.indexOf("reason=");
        if (ind >= 0) {
            int ireason;
            String reason = remainder.substring("reason=".length() + ind);
            if (reason.indexOf(HwCHRWifiCPUUsage.COL_SEP) >= 0) {
                reason = reason.substring(WIFI_SETTING_CLOSED_AND_SERVICE_OPENED, reason.indexOf(HwCHRWifiCPUUsage.COL_SEP)).trim();
            } else {
                reason = reason.trim();
            }
            try {
                ireason = Integer.parseInt(reason);
            } catch (NumberFormatException e) {
                ireason = -10;
                Log.d(TAG, "disconnected reason code error!" + reason);
            }
            triggerDisableNetworkIfNeed();
            uploadDisconnectException(ireason);
        }
    }

    private void uploadDisconnectException(int reasoncode) {
        String strReasoncode = HwWifiCHRConstImpl.getDefault().getDisconnectReasonCode(reasoncode);
        if ("".equals(strReasoncode) || this.mRssi < -80 || this.mHwWifiStatStore == null) {
            Log.d(TAG, "disconnected but reason code is unmatch");
        } else {
            this.aucAbnormal_disconnect = (short) reasoncode;
            if (this.mHwWifiStatStore.isConnectToNetwork()) {
                updateWifiException(85, strReasoncode);
            }
        }
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.updateConnectState(HWFLOW);
        }
    }

    private void triggerDisableNetworkIfNeed() {
        long now = SystemClock.elapsedRealtime();
        if (this.mNetworkId == WIFI_SECURITY_TYPE_UNKNOWN || this.mNetworkId != this.mLastNetworkId) {
            this.mDisableNetworkflag = (byte) 0;
            this.mLastNetworkId = this.mNetworkId;
            this.mDisconnectCnt = WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
            this.mFirstDisconnectTime = now;
            this.mSecondDisconnectTime = now;
            Log.e(TAG, "new network disconnected");
            return;
        }
        this.mDisconnectCnt += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
        if (this.mDisableNetworkflag == DISABLE_NETWORK_PHASE2) {
            if (this.mScreenState == 0) {
                this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                if (this.hwWifiCHRService != null) {
                    Log.e(TAG, "(ignore this disable)disable the Network when screen off and network disconnected 15 times in 30 minute");
                }
            }
        } else if (this.mDisconnectCnt >= WIFI_CHR_DEVICE_ERROR_OFFSET && now - this.mFirstDisconnectTime < ACCESSWEB_SLOW_OR_FAILED_UPLOAD_MIN) {
            this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
            if (this.hwWifiCHRService != null) {
                this.mNeedEnableNetworkId = this.mNetworkId;
                this.mDisableNetworkflag = DISABLE_NETWORK_PHASE2;
                Log.e(TAG, "(ignore this disable)disable the Network because this network disconnected 15 times in 30 minute");
            }
            this.mDeltaTime = (int) (now - this.mFirstDisconnectTime);
        } else if (this.mDisableNetworkflag != DISABLE_NETWORK_PHASE1) {
            if (this.mDisconnectCnt < UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE) {
                this.mSecondDisconnectTime = now;
            } else if (now - this.mFirstDisconnectTime >= 300000) {
                this.mFirstDisconnectTime = this.mSecondDisconnectTime;
                this.mSecondDisconnectTime = now;
            } else if (this.mScreenState == 0) {
                this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                if (this.hwWifiCHRService != null) {
                    this.mNeedEnableNetworkId = this.mNetworkId;
                    this.mDisableNetworkflag = DISABLE_NETWORK_PHASE1;
                    Log.e(TAG, "(ignore this disable)disable the Network because this network disconnected 3 times in 5 minute");
                }
                this.mDeltaTime = (int) (now - this.mFirstDisconnectTime);
            }
        }
    }

    public void clearDisconnectData() {
        this.mNetworkId = WIFI_SECURITY_TYPE_UNKNOWN;
        this.mLastNetworkId = WIFI_SECURITY_TYPE_UNKNOWN;
        this.mFirstDisconnectTime = 0;
        this.mSecondDisconnectTime = 0;
        this.mDisconnectCnt = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mDisableNetworkflag = (byte) 0;
    }

    private void restoreDisableNetwork() {
        if (this.mDisableNetworkflag == WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
            this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
            if (this.hwWifiCHRService != null) {
                Log.e(TAG, "(ignore this enable)enable the network by screen on");
            }
            this.mDisableNetworkflag = (byte) 0;
            this.mNeedEnableNetworkId = WIFI_SECURITY_TYPE_UNKNOWN;
        }
    }

    public void updateWifiExceptionByWifipro(int ucErrorCode, String ucSubErrorCode) {
        updateWifiException(ucErrorCode, ucSubErrorCode);
    }

    public void updatePortalAutSms(String sms_num, byte[] sms_body, int sms_body_len) {
        this.mSms_Num = "";
        this.mSMS_Body_Len = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mSms_Body = new byte[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED];
        if (sms_num == null || sms_body == null) {
            Log.d(TAG, "updatePortalAutSms: invalid sms_num or sms_body");
            return;
        }
        this.mSms_Num = sms_num;
        this.mSMS_Body_Len = sms_body_len;
        this.mSms_Body = (byte[]) sms_body.clone();
    }

    public void updatePortalAPInfo(byte[] ssid, String bssid, String cellId, int bssid_len) {
        this.mPortalAPSsid = new byte[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED];
        this.mPortalAPBssid = "";
        this.mSsid_Len = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        if (!(ssid == null || bssid == null)) {
            this.mSsid_Len = bssid_len;
            this.mPortalAPSsid = (byte[]) ssid.clone();
            this.mPortalAPBssid = bssid;
        }
        if (cellId != null) {
            this.mPortalCellId = cellId;
        } else {
            this.mPortalCellId = "";
        }
    }

    public void updatePortalWebpageInfo(String url, String phoneInputId, String sndBtnId, String codeInputId, String submitBtnId, int htmlBtnNum) {
        if (url != null) {
            this.mWebUrl = url;
        } else {
            this.mWebUrl = "";
        }
        if (phoneInputId != null) {
            this.mPhoneInputId = phoneInputId;
        } else {
            this.mPhoneInputId = "";
        }
        if (sndBtnId != null) {
            this.mSndBtnId = sndBtnId;
        } else {
            this.mSndBtnId = "";
        }
        if (codeInputId != null) {
            this.mCodeInputId = codeInputId;
        } else {
            this.mCodeInputId = "";
        }
        if (submitBtnId != null) {
            this.mSubmitBtnId = submitBtnId;
        } else {
            this.mSubmitBtnId = "";
        }
        this.mHTML_Input_Number = htmlBtnNum;
    }

    public void updatePortalKeyLines(byte[] lines) {
        if (lines != null) {
            this.mPortalAPKeyLines = (byte[]) lines.clone();
        } else {
            this.mPortalAPKeyLines = new byte[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED];
        }
    }

    public void updatePortalStatus(int respCode) {
        int isRedirect = (respCode < TCPIpqRtt.RTT_FINE_5 || respCode > 399) ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
        if (this.mIsPortalConnection == 0) {
            this.mPortalStatus = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        } else if (this.mIsPortalConnection == WIFI_SETTING_OPENED_AND_SERVICE_CLOSED && isRedirect == WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
            this.mPortalStatus = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        } else {
            this.mPortalStatus = WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
        }
        Log.d(TAG, "updatePortalStatus: IsPortalConnection= " + this.mIsPortalConnection + ",  respCode =" + respCode + ",  portal Status =" + this.mPortalStatus);
    }

    public void updatePortalConnection(int isPortalconnection) {
        this.mIsPortalConnection = isPortalconnection;
    }

    public int isPortalConnection() {
        return this.mIsPortalConnection;
    }

    public void updateApRedirectUrl(String url) {
        if (url != null) {
            this.mNotOpenApRedirectUrl = url;
        } else {
            this.mNotOpenApRedirectUrl = "";
        }
    }

    private String getACFailTypeStr(int acFailType) {
        switch (acFailType) {
            case WIFI_SETTING_CLOSED_AND_SERVICE_OPENED /*0*/:
                return "UNKNOWN";
            case WIFI_SETTING_OPENED_AND_SERVICE_CLOSED /*1*/:
                return "REFUSE";
            case WIFI_SETTING_OPENED_TIMEOUT /*2*/:
                return "RESET";
            default:
                Log.d(TAG, "getACFailTypeStr: invalid value");
                return "UNKNOWN";
        }
    }

    private String getBG_AC_DiffTypeStr(int acDiffType) {
        switch (acDiffType) {
            case WIFI_SETTING_CLOSED_AND_SERVICE_OPENED /*0*/:
                return "UNKNOWN";
            case WIFI_SETTING_OPENED_AND_SERVICE_CLOSED /*1*/:
                return "BG_AV_CN_NAV";
            case WIFI_SETTING_OPENED_TIMEOUT /*2*/:
                return "BG_AV_CN_POT";
            case UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE /*3*/:
                return "BG_NAV_CN_AV";
            case SECURITY_WAPI_PSK /*4*/:
                return "BG_NAV_CN_POT";
            case SECURITY_WAPI_CERT /*5*/:
                return "BG_POT_CN_AV";
            case MAX_DISCONN_CNT /*6*/:
                return "BG_POT_CN_NAV";
            case MessageUtil.MSG_WIFI_UPDATE_SCAN_RESULT /*7*/:
                return "BG_CGT_CN_AV";
            case STATE_SCANING /*8*/:
                return "BG_CGT_CN_NAV";
            case MessageUtil.MSG_WIFI_HANDLE_DISABLE /*9*/:
                return "BG_CGT_CN_POT";
            default:
                Log.d(TAG, "getBG_AC_DiffTypeStr: invalid value");
                return "UNKNOWN";
        }
    }

    public void updateActiveCheckFail(String ssid, String serverName, int failType) {
        if (ssid != null) {
            this.mAPSsid = ssid;
        } else {
            this.mAPSsid = "";
            Log.e(TAG, "updateActiveCheckFail: invalid ssid");
        }
        if (serverName != null) {
            this.mNotOpenApRedirectUrl = serverName;
        } else {
            this.mNotOpenApRedirectUrl = "";
        }
        this.mAC_FailType = failType;
    }

    public void updateStatParaPart1(int statIntervalTime, int enableTotTime, short noInetHandoverCount, short portalUnauthCount, short wifiScoCount, short portalCodeParseCount, short rcvSMS_Count, short portalAutoLoginCount) {
        this.mStatIntervalTime = statIntervalTime;
        this.mEnableTotTime = enableTotTime;
        this.mNoInetHandoverCount = noInetHandoverCount;
        this.mPortalUnauthCount = portalUnauthCount;
        this.mWifiScoCount = wifiScoCount;
        this.mPortalCodeParseCount = portalCodeParseCount;
        this.mRcvSMS_Count = rcvSMS_Count;
        this.mPortalAutoLoginCount = portalAutoLoginCount;
    }

    public void updateStatParaPart2(short cellAutoOpenCount, short cellAutoCloseCount, short totalBQEBadROC, short manualBackROC, short rssi_RO_Tot, short rssi_ErrRO_Tot, short ota_RO_Tot, short ota_ErrRO_Tot, short tcp_RO_Tot) {
        this.mCellAutoOpenCount = cellAutoOpenCount;
        this.mCellAutoCloseCount = cellAutoCloseCount;
        this.mTotalBQE_BadROC = totalBQEBadROC;
        this.mManualBackROC = manualBackROC;
        this.mRSSI_RO_Tot = rssi_RO_Tot;
        this.mRSSI_ErrRO_Tot = rssi_ErrRO_Tot;
        this.mOTA_RO_Tot = ota_RO_Tot;
        this.mOTA_ErrRO_Tot = ota_ErrRO_Tot;
        this.mTCP_RO_Tot = tcp_RO_Tot;
    }

    public void updateStatParaPart3(short tcp_ErrRO_Tot, int manualRI_TotTime, int autoRI_TotTime, short autoRI_TotCount, short rssi_RestoreRI_Count, short rssi_BetterRI_Count, short timerRI_Count, short hisScoRI_Count, short userCancelROC) {
        this.mTCP_ErrRO_Tot = tcp_ErrRO_Tot;
        this.mManualRI_TotTime = manualRI_TotTime;
        this.mAutoRI_TotTime = autoRI_TotTime;
        this.mAutoRI_TotCount = autoRI_TotCount;
        this.mRSSI_RestoreRI_Count = rssi_RestoreRI_Count;
        this.mRSSI_BetterRI_Count = rssi_BetterRI_Count;
        this.mTimerRI_Count = timerRI_Count;
        this.mHisScoRI_Count = hisScoRI_Count;
        this.mUserCancelROC = userCancelROC;
    }

    public void updateStatParaPart4(short wifiToWifiSuccCount, short noInetAlarmCount, short wifiOobInitState, short notAutoConnPortalCnt, short highDataRateStopROC, short selectNotInetAPCount, short userUseBgScanAPCount, short pingPongCount) {
        this.mWifiToWifiSuccCount = wifiToWifiSuccCount;
        this.mNoInetAlarmCount = noInetAlarmCount;
        this.mWifiOobInitState = wifiOobInitState;
        this.mNotAutoConnPortalCnt = notAutoConnPortalCnt;
        this.mHighDataRateStopROC = highDataRateStopROC;
        this.mSelectNotInetAPCount = selectNotInetAPCount;
        this.mUserUseBgScanAPCount = userUseBgScanAPCount;
        this.mPingPongCount = pingPongCount;
    }

    public void updateStatParaPart5(short bqeBadSettingCancel, short notInetSettingCancel, short notInetUserCancel, short notInetRestoreRI, short notInetUserManualRI, short notInetWifiToWifiCount, short reopenWifiRICount, short selCSPShowDiglogCount, short selCSPAutoSwCount, short selCSPNotSwCount, short totBtnRICount, short bmd_TenMNotifyCount) {
        this.mBQE_BadSettingCancel = bqeBadSettingCancel;
        this.mNotInetSettingCancel = notInetSettingCancel;
        this.mNotInetUserCancel = notInetUserCancel;
        this.mNotInetRestoreRI = notInetRestoreRI;
        this.mNotInetUserManualRI = notInetUserManualRI;
        this.mNotInetWifiToWifiCount = notInetWifiToWifiCount;
        this.mReopenWifiRICount = reopenWifiRICount;
        this.mSelCSPShowDiglogCount = selCSPShowDiglogCount;
        this.mSelCSPAutoSwCount = selCSPAutoSwCount;
        this.mSelCSPNotSwCount = selCSPNotSwCount;
        this.mTotBtnRICount = totBtnRICount;
        this.mBMD_TenMNotifyCount = bmd_TenMNotifyCount;
    }

    public void updateStatParaPart6(short bmd_TenM_RI_Count, short bmd_FiftyMNotifyCount, short bmd_FiftyM_RI_Count, short bmd_UserDelNotifyCount, int ro_TotMobileData, short af_PhoneNumSuccCnt, short af_PhoneNumFailCnt, short af_PasswordSuccCnt, short af_PasswordFailCnt, short af_AutoLoginSuccCnt, short af_AutoLoginFailCnt) {
        this.mBMD_TenM_RI_Count = bmd_TenM_RI_Count;
        this.mBMD_FiftyMNotifyCount = bmd_FiftyMNotifyCount;
        this.mBMD_FiftyM_RI_Count = bmd_FiftyM_RI_Count;
        this.mBMD_UserDelNotifyCount = bmd_UserDelNotifyCount;
        this.mRO_TotMobileData = ro_TotMobileData;
        this.mAF_PhoneNumSuccCnt = af_PhoneNumSuccCnt;
        this.mAF_PhoneNumFailCnt = af_PhoneNumFailCnt;
        this.mAF_PasswordSuccCnt = af_PasswordSuccCnt;
        this.mAF_PasswordFailCnt = af_PasswordFailCnt;
        this.mAF_AutoLoginSuccCnt = af_AutoLoginSuccCnt;
        this.mAF_AutoLoginFailCnt = af_AutoLoginFailCnt;
    }

    public void updateStatParaPart7(short bg_RunCnt, short settingBG_RunCnt, short bg_FreeInetOkApCnt, short bg_FishingApCnt, short bg_FreeNotInetApCnt, short bg_PortalApCnt, short bg_FailedCnt, short bg_InetNotOkActiveOk, short bg_InetOkActiveNotOk) {
        this.mBG_BgRunCnt = bg_RunCnt;
        this.mBG_SettingRunCnt = settingBG_RunCnt;
        this.mBG_FreeInetOkApCnt = bg_FreeInetOkApCnt;
        this.mBG_FishingApCnt = bg_FishingApCnt;
        this.mBG_FreeNotInetApCnt = bg_FreeNotInetApCnt;
        this.mBG_PortalApCnt = bg_PortalApCnt;
        this.mBG_FailedCnt = bg_FailedCnt;
        this.mBG_InetNotOkActiveOk = bg_InetNotOkActiveOk;
        this.mBG_InetOkActiveNotOk = bg_InetOkActiveNotOk;
    }

    public void updateStatParaPart8(short bg_UserSelApFishingCnt, short bg_ConntTimeoutCnt, short bg_DNSFailCnt, short bg_DHCPFailCnt, short bg_AUTH_FailCnt, short bg_AssocRejectCnt, short bg_UserSelFreeInetOkCnt, short bg_UserSelNoInetCnt, short bg_UserSelPortalCnt) {
        this.mBG_UserSelApFishingCnt = bg_UserSelApFishingCnt;
        this.mBG_ConntTimeoutCnt = bg_ConntTimeoutCnt;
        this.mBG_DNSFailCnt = bg_DNSFailCnt;
        this.mBG_DHCPFailCnt = bg_DHCPFailCnt;
        this.mBG_AUTH_FailCnt = bg_AUTH_FailCnt;
        this.mBG_AssocRejectCnt = bg_AssocRejectCnt;
        this.mBG_UserSelFreeInetOkCnt = bg_UserSelFreeInetOkCnt;
        this.mBG_UserSelNoInetCnt = bg_UserSelNoInetCnt;
        this.mBG_UserSelPortalCnt = bg_UserSelPortalCnt;
    }

    public void updateStatParaPart9(short bg_FoundTwoMoreApCnt, short af_FPNSuccNotMsmCnt, short bsg_RsGoodCnt, short bsg_RsMidCnt, short bsg_RsBadCnt, short bsg_EndIn4sCnt, short bsg_EndIn4s7sCnt, short bsg_NotEndIn7sCnt) {
        this.mBG_FoundTwoMoreApCnt = bg_FoundTwoMoreApCnt;
        this.mAF_FPNSuccNotMsmCnt = af_FPNSuccNotMsmCnt;
        this.mBSG_RsGoodCnt = bsg_RsGoodCnt;
        this.mBSG_RsMidCnt = bsg_RsMidCnt;
        this.mBSG_RsBadCnt = bsg_RsBadCnt;
        this.mBSG_EndIn4sCnt = bsg_EndIn4sCnt;
        this.mBSG_EndIn4s7sCnt = bsg_EndIn4s7sCnt;
        this.mBSG_NotEndIn7sCnt = bsg_NotEndIn7sCnt;
    }

    public void updateStatParaPart10(short pBG_NCByConnectFail, short pBG_NCByCheckFail, short pBG_NCByStateErr, short pBG_NCByUnknown, short pBQE_CNUrl1FailCount, short pBQE_CNUrl2FailCount, short pBQE_CNUrl3FailCount, short pBQE_NCNUrl1FailCount, short pBQE_NCNUrl2FailCount, short pBQE_NCNUrl3FailCount) {
        this.mBG_NCByConnectFail = pBG_NCByConnectFail;
        this.mBG_NCByCheckFail = pBG_NCByCheckFail;
        this.mBG_NCByStateErr = pBG_NCByStateErr;
        this.mBG_NCByUnknown = pBG_NCByUnknown;
        this.mBQE_CNUrl1FailCount = pBQE_CNUrl1FailCount;
        this.mBQE_CNUrl2FailCount = pBQE_CNUrl2FailCount;
        this.mBQE_CNUrl3FailCount = pBQE_CNUrl3FailCount;
        this.mBQE_NCNUrl1FailCount = pBQE_NCNUrl1FailCount;
        this.mBQE_NCNUrl2FailCount = pBQE_NCNUrl2FailCount;
        this.mBQE_NCNUrl3FailCount = pBQE_NCNUrl3FailCount;
    }

    public void updateStatParaPart11(short pBQE_ScoreUnknownCount, short pBQE_BindWlanFailCount, short pBQE_StopBqeFailCount, int pQOE_AutoRI_TotData, int pNotInet_AutoRI_TotData, short pQOE_RO_DISCONNECT_Cnt, int pQOE_RO_DISCONNECT_TotData, short pNotInetRO_DISCONNECT_Cnt, int pNotInetRO_DISCONNECT_TotData, int pTotWifiConnectTime) {
        this.mBQE_ScoreUnknownCount = pBQE_ScoreUnknownCount;
        this.mBQE_BindWlanFailCount = pBQE_BindWlanFailCount;
        this.mBQE_StopBqeFailCount = pBQE_StopBqeFailCount;
        this.mQOE_AutoRI_TotData = pQOE_AutoRI_TotData;
        this.mNotInet_AutoRI_TotData = pNotInet_AutoRI_TotData;
        this.mQOE_RO_DISCONNECT_Cnt = pQOE_RO_DISCONNECT_Cnt;
        this.mQOE_RO_DISCONNECT_TotData = pQOE_RO_DISCONNECT_TotData;
        this.mNotInetRO_DISCONNECT_Cnt = pNotInetRO_DISCONNECT_Cnt;
        this.mNotInetRO_DISCONNECT_TotData = pNotInetRO_DISCONNECT_TotData;
        this.mTotWifiConnectTime = pTotWifiConnectTime;
    }

    public void updateStatParaPart12(short pActiveCheckRS_Diff, short pNoInetAlarmOnConnCnt, short pPortalNoAutoConnCnt, short pHomeAPAddRoPeriodCnt, short pHomeAPQoeBadCnt, int pHistoryTotWifiConnHour, short pTotAPRecordCnt, short pTotHomeAPCnt, short pBigRTT_RO_Tot, short pBigRTT_ErrRO_Tot) {
        this.mActiveCheckRS_Diff = pActiveCheckRS_Diff;
        this.mNoInetAlarmOnConnCnt = pNoInetAlarmOnConnCnt;
        this.mPortalNoAutoConnCnt = pPortalNoAutoConnCnt;
        this.mHomeAPAddRoPeriodCnt = pHomeAPAddRoPeriodCnt;
        this.mHomeAPQoeBadCnt = pHomeAPQoeBadCnt;
        this.mHistoryTotWifiConnHour = pHistoryTotWifiConnHour;
        this.mTotAPRecordCnt = pTotAPRecordCnt;
        this.mTotHomeAPCnt = pTotHomeAPCnt;
        this.mBigRTT_RO_Tot = pBigRTT_RO_Tot;
        this.mBigRTT_ErrRO_Tot = pBigRTT_ErrRO_Tot;
    }

    public void updateStatParaPart13(short pTotalPortalConnCount, short pTotalPortalAuthSuccCount, short pManualConnBlockPortalCount, short pWifiproStateAtReportTime, short pWifiproOpenCount, short pWifiproCloseCount, short pActiveCheckRS_Same) {
        this.mTotalPortalConnCount = pTotalPortalConnCount;
        this.mTotalPortalAuthSuccCount = pTotalPortalAuthSuccCount;
        this.mManualConnBlockPortalCount = pManualConnBlockPortalCount;
        this.mWifiproStateAtReportTime = pWifiproStateAtReportTime;
        this.mWifiproOpenCount = pWifiproOpenCount;
        this.mWifiproCloseCount = pWifiproCloseCount;
        this.mActiveCheckRS_Same = pActiveCheckRS_Same;
    }

    public void setDualbandParameter(WifiProStatisticsRecord staRecord) {
        if (staRecord == null) {
            Log.e(TAG, "setDualbandParameter null error.");
            return;
        }
        Log.d(TAG, "setDualbandParameter enter.");
        this.mSingleAP_LearnedCount = staRecord.mSingleAP_LearnedCount;
        this.mSingleAP_NearbyCount = staRecord.mSingleAP_NearbyCount;
        this.mSingleAP_MonitorCount = staRecord.mSingleAP_MonitorCount;
        this.mSingleAP_SatisfiedCount = staRecord.mSingleAP_SatisfiedCount;
        this.mSingleAP_DisapperCount = staRecord.mSingleAP_DisapperCount;
        this.mSingleAP_InblacklistCount = staRecord.mSingleAP_InblacklistCount;
        this.mSingleAP_ScoreNotSatisfyCount = staRecord.mSingleAP_ScoreNotSatisfyCount;
        this.mSingleAP_HandoverSucCount = staRecord.mSingleAP_HandoverSucCount;
        this.mSingleAP_HandoverFailCount = staRecord.mSingleAP_HandoverFailCount;
        this.mSingleAP_LowFreqScan5GCount = staRecord.mSingleAP_LowFreqScan5GCount;
        this.mSingleAP_MidFreqScan5GCount = staRecord.mSingleAP_MidFreqScan5GCount;
        this.mSingleAP_HighFreqScan5GCount = staRecord.mSingleAP_HighFreqScan5GCount;
        this.mMixedAP_LearnedCount = staRecord.mMixedAP_LearnedCount;
        this.mMixedAP_NearbyCount = staRecord.mMixedAP_NearbyCount;
        this.mMixedAP_MonitorCount = staRecord.mMixedAP_MonitorCount;
        this.mMixedAP_SatisfiedCount = staRecord.mMixedAP_SatisfiedCount;
        this.mMixedAP_DisapperCount = staRecord.mMixedAP_DisapperCount;
        this.mMixedAP_InblacklistCount = staRecord.mMixedAP_InblacklistCount;
        this.mMixedAP_ScoreNotSatisfyCount = staRecord.mMixedAP_ScoreNotSatisfyCount;
        this.mMixedAP_HandoverSucCount = staRecord.mMixedAP_HandoverSucCount;
        this.mMixedAP_HandoverFailCount = staRecord.mMixedAP_HandoverFailCount;
        this.mMixedAP_LowFreqScan5GCount = staRecord.mMixedAP_LowFreqScan5GCount;
        this.mMixedAP_MidFreqScan5GCount = staRecord.mMixedAP_MidFreqScan5GCount;
        this.mMixedAP_HighFreqScan5GCount = staRecord.mMixedAP_HighFreqScan5GCount;
        this.mCustomizedScan_SuccCount = staRecord.mCustomizedScan_SuccCount;
        this.mCustomizedScan_FailCount = staRecord.mCustomizedScan_FailCount;
        this.mHandoverToNotInet5GCount = staRecord.mHandoverToNotInet5GCount;
        this.mHandoverTooSlowCount = staRecord.mHandoverTooSlowCount;
        this.mHandoverToBad5GCount = staRecord.mHandoverToBad5GCount;
        this.mUserRejectHandoverCount = staRecord.mUserRejectHandoverCount;
        this.mHandoverPingpongCount = staRecord.mHandoverPingpongCount;
    }

    public void setWifiProDualbandExceptionRecord(WifiProDualbandExceptionRecord exRecord) {
        this.mWifiProDualbandExceptionRecord = exRecord;
    }

    public void setWifiProDualbandApInfo(int dualbandApType, String bssid2G, String bssid5G) {
        this.mDualbandAPType = dualbandApType;
        this.mDualband2GAPBssid = bssid2G;
        this.mDualband5GAPBssid = bssid5G;
    }

    private boolean canUploadWifiproEvent(String subEventName, boolean commercialUser) {
        boolean retVal = true;
        if (subEventName == null) {
            Log.e(TAG, "canUploadWifiproEvent null error.");
            return HWFLOW;
        }
        if (commercialUser) {
            if (subEventName.equals(SUB_EVENT_PORTALAP_IN_WHITE) || subEventName.equals(SUB_EVENT_AUTO_CLOSE_TERMINATION) || subEventName.equals(SUB_EVENT_HOME_AP_INFO) || subEventName.equals(SUB_EVENT_ENTERPRISE_AP_INFO) || subEventName.equals(SUB_EVENT_BG_FAILED_CNT) || subEventName.equals(SUB_EVENT_BG_NOT_INET_ACTIVE_IOK) || subEventName.equals(SUB_EVENT_BG_INET_OK_ACTIVE_NOT_OK) || subEventName.equals(SUB_EVENT_BG_CONN_AP_TIME_LEN) || subEventName.equals(SUB_EVENT_BG_AC_TIME_LEN)) {
                Log.i(TAG, "commercial user not upload wifipro subevent:" + subEventName);
                retVal = HWFLOW;
            }
            if (subEventName.equals(SUB_EVENT_CANT_CONNECT_FOR_LONG)) {
                int i = this.wifiproCanotConnectForLongCount + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                this.wifiproCanotConnectForLongCount = i;
                if (i > STATE_SCANING) {
                    Log.i(TAG, "commercial user not upload event:" + subEventName + " more than 8");
                    retVal = HWFLOW;
                }
            }
        }
        if (subEventName.equals(SUB_EVENT_NOT_OPEN_AP_REDIRECT)) {
            if (this.mApRedirectUrlSendCount) {
                Log.i(TAG, "already send ap redirect URL during this power up.");
                retVal = HWFLOW;
            } else {
                this.mApRedirectUrlSendCount = true;
                retVal = true;
            }
        }
        return retVal;
    }

    private boolean canUploadWifiproDualbandEvent(String subEventName, boolean commercialUser) {
        boolean retVal = true;
        if (subEventName == null) {
            Log.e(TAG, "canUploadWifiproDualbandEvent null error.");
            return HWFLOW;
        }
        if (commercialUser && subEventName.equals(SUB_EVENT_DUALBAND_HANDOVER_SNAPSHOP)) {
            Log.d(TAG, "commercial user not upload dualband subevent:" + subEventName);
            retVal = HWFLOW;
        }
        return retVal;
    }

    private String getAutoOpenCauseNameStr(int autoOpenRootCause) {
        switch (autoOpenRootCause) {
            case WIFI_SETTING_CLOSED_AND_SERVICE_OPENED /*0*/:
                return "UNKNOWN";
            case WIFI_SETTING_OPENED_AND_SERVICE_CLOSED /*1*/:
                return "PASSWORD_FAILURE";
            case WIFI_SETTING_OPENED_TIMEOUT /*2*/:
                return "DHCP_FAILURE";
            case UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE /*3*/:
                return "SERVER_FULL";
            case SECURITY_WAPI_PSK /*4*/:
                return "ASSOCIATION_REJECT";
            case SECURITY_WAPI_CERT /*5*/:
                return "DNS_FAILURE";
            default:
                Log.d(TAG, "getAutoOpenCauseNameStr: invalid value");
                return "UNKNOWN";
        }
    }

    private String getAutoCloseCauseNameStr(int autoCloseRootCause) {
        switch (autoCloseRootCause) {
            case WIFI_SETTING_CLOSED_AND_SERVICE_OPENED /*0*/:
                return "UNKNOWN";
            case WIFI_SETTING_OPENED_AND_SERVICE_CLOSED /*1*/:
                return "CONNECT_TO_NEW_AP";
            case WIFI_SETTING_OPENED_TIMEOUT /*2*/:
                return "CLOSE_BY_USER";
            case UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE /*3*/:
                return "ENTER_NEW_CELL";
            default:
                Log.d(TAG, "getAutoCloseCauseNameStr: invalid value");
                return "UNKNOWN";
        }
    }

    private String getWifiUserTypeStr(int wifiUserType) {
        switch (wifiUserType) {
            case WIFI_SETTING_CLOSED_AND_SERVICE_OPENED /*0*/:
                return "UNKNOWN";
            case WIFI_SETTING_OPENED_AND_SERVICE_CLOSED /*1*/:
                return "BETA";
            case WIFI_SETTING_OPENED_TIMEOUT /*2*/:
                return "COMMERCIAL";
            default:
                Log.d(TAG, "getWifiUserTypeStr: invalid value");
                return "UNKNOWN";
        }
    }

    private String getRATTypeNameStr(int RATType) {
        switch (RATType) {
            case WIFI_SETTING_CLOSED_AND_SERVICE_OPENED /*0*/:
                return "UNKNOWN";
            case WIFI_SETTING_OPENED_AND_SERVICE_CLOSED /*1*/:
                return "RAT_2G";
            case WIFI_SETTING_OPENED_TIMEOUT /*2*/:
                return "RAT_3G_TDS";
            case UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE /*3*/:
                return "RAT_3G_CDMA";
            case SECURITY_WAPI_PSK /*4*/:
                return "RAT_3G_UMTS";
            case SECURITY_WAPI_CERT /*5*/:
                return "RAT_4G";
            default:
                Log.d(TAG, "getRATTypeNameStr: invalid RATType");
                return "UNKNOWN";
        }
    }

    private String getTriggerReasonNameStr(int TriggerReason) {
        switch (TriggerReason) {
            case WIFI_SETTING_CLOSED_AND_SERVICE_OPENED /*0*/:
                return "TRIGGER_OTHER";
            case WIFI_SETTING_OPENED_AND_SERVICE_CLOSED /*1*/:
                return "TRIGGER_CONNECT_OTHER_AP";
            case WIFI_SETTING_OPENED_TIMEOUT /*2*/:
                return "TRIGGER_USER_FORGET_SSID";
            case UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE /*3*/:
                return "TRIGGER_WIFI_OFF";
            case SECURITY_WAPI_PSK /*4*/:
                return "TRIGGER_DHCP_SUCCESS";
            case SECURITY_WAPI_CERT /*5*/:
                return "TRIGGER_COUNT_BEYOND_THRESHOLD";
            case MAX_DISCONN_CNT /*6*/:
                return "TRIGGER_APK_CONNECT";
            case MessageUtil.MSG_WIFI_UPDATE_SCAN_RESULT /*7*/:
                return "TRIGGER_DISABLE_NETWORK";
            default:
                Log.d(TAG, "getTriggerReasonNameStr: invalid value");
                return "UNKNOWN";
        }
    }

    public void updateExcpRoParaPart1(short rssi_VALUE, short ota_PacketDropRate, short rttAvg, short tcpInSegs, short tcpOutSegs, short tcpRetransSegs, short wifi_NetSpeed, short ipQLevel) {
        this.mRSSI_VALUE = rssi_VALUE;
        this.mOTA_PacketDropRate = ota_PacketDropRate;
        this.mRttAvg = rttAvg;
        this.mTcpInSegs = tcpInSegs;
        this.mTcpOutSegs = tcpOutSegs;
        this.mTcpRetransSegs = tcpRetransSegs;
        this.mWIFI_NetSpeed = wifi_NetSpeed;
        this.mIPQLevel = ipQLevel;
    }

    public void updateExcpRoParaPart2(String ro_APSsid, short mobileSignalLevel, short ratType, short historyQuilityRO_Rate, short highDataRateRO_Rate, short creditScoreRO_Rate, short ro_Duration) {
        if (ro_APSsid != null) {
            this.mRO_APSsid = ro_APSsid;
        } else {
            this.mRO_APSsid = "";
        }
        this.mMobileSignalLevel = mobileSignalLevel;
        this.mRATType = ratType;
        this.mHistoryQuilityRO_Rate = historyQuilityRO_Rate;
        this.mHighDataRateRO_Rate = highDataRateRO_Rate;
        this.mCreditScoreRO_Rate = creditScoreRO_Rate;
        this.mRO_Duration = ro_Duration;
    }

    public void updateWifiproTimeLen(short timeLen) {
        this.mRO_Duration = timeLen;
    }

    public void updateSSID(String ssid) {
        if (ssid != null) {
            this.mAPSsid = ssid;
            return;
        }
        this.mAPSsid = "";
        Log.e(TAG, "updateSSID: invalid ssid");
    }

    public void updateBSSID(String bssid) {
        if (bssid != null) {
            this.mAPBssid = bssid;
            return;
        }
        this.mAPBssid = "";
        Log.e(TAG, "updateBSSID: invalid bssid");
    }

    public void updateHomeAPJudgeTime(int hours) {
        this.mHomeAPJudgeTime = hours;
    }

    public void updateAPSecurityType(int secType) {
        this.mAPSecurity = secType;
    }

    public void updateBG_AC_DiffType(int acDiffType) {
        this.mBG_AC_DiffType = acDiffType;
    }

    public void updateAutoOpenWhiteNum(short autoOpenWhiteNum) {
        this.mAutoOpenWhiteNum = autoOpenWhiteNum;
    }

    public void updateAutoOpenRootCause(short autoOpenRootCause) {
        this.mAutoOpenRootCause = autoOpenRootCause;
    }

    public void updateAutoCloseRootCause(short autoCloseRootCause) {
        this.mAutoCloseRootCause = autoCloseRootCause;
    }

    public void updateFreeAP1Info(String ssid, String bssid) {
        if (ssid != null) {
            this.mAP1Ssid = ssid;
        } else {
            this.mAP1Ssid = "";
            Log.d(TAG, "updateAP1Info: invalid ssid");
        }
        if (bssid != null) {
            this.mAP1BSsid = bssid;
            return;
        }
        this.mAP1BSsid = "";
        Log.d(TAG, "updateAP1Info: invalid bssid");
    }

    public void updateFreeAP2Info(String ssid, String bssid) {
        if (ssid != null) {
            this.mAP2Ssid = ssid;
        } else {
            this.mAP2Ssid = "";
            Log.d(TAG, "updateAP2Info: invalid ssid");
        }
        if (bssid != null) {
            this.mAP2BSsid = bssid;
            return;
        }
        this.mAP2BSsid = "";
        Log.d(TAG, "updateAP2Info: invalid bssid");
    }

    public void updateFreeAP3Info(String ssid, String bssid) {
        if (ssid != null) {
            this.mAP3Ssid = ssid;
        } else {
            this.mAP3Ssid = "";
            Log.d(TAG, "updateAP3Info: invalid ssid");
        }
        if (bssid != null) {
            this.mAP3BSsid = bssid;
            return;
        }
        this.mAP3BSsid = "";
        Log.d(TAG, "updateAP3Info: invalid bssid");
    }

    public void updateFreeAPCellID(String freeAPcellID) {
        if (freeAPcellID != null) {
            this.mFreeAPcellID = freeAPcellID;
            return;
        }
        this.mFreeAPcellID = "";
        Log.d(TAG, "updateFreeAPCellID: invalid cellID");
    }

    private boolean matchCommercialTrigger(boolean commercialUser, long lastUploadTime, long now, int count) {
        boolean ret = true;
        if (count >= UPLOAD_NUMBER_ONE_CYCLE && now - lastUploadTime <= COMM_UPLOAD_MIN_SPAN) {
            ret = HWFLOW;
        }
        if (ret && this.dftType != WIFI_SECURITY_TYPE_UNKNOWN) {
            uploadDFTEvent(this.dftType);
        }
        return ret;
    }

    private boolean isNeedCreateAccessWebSlow(long now, boolean commercialUser) {
        if (commercialUser) {
            if ((this.mLastAccessWebSlowTime == 0 || now - this.mLastAccessWebSlowTime > ACCESSWEB_SLOW_OR_FAILED_UPLOAD_MIN) && ((long) this.mUploadAccessWebSlowlySSID) < ACCESSWEB_SLOW_OR_FAILED_UPLOAD_COUNT) {
                return true;
            }
            return HWFLOW;
        } else if ((this.mLastAccessWebSlowTime == 0 || now - this.mLastAccessWebSlowTime > ACCESSWEB_SLOW_OR_FAILED_BETA_UPLOAD_MIN) && ((long) this.mUploadAccessWebSlowlySSID) < ACCESSWEB_SLOW_OR_FAILED_BETA_UPLOAD_COUNT) {
            return true;
        } else {
            return HWFLOW;
        }
    }

    public void setCountryCode(String countryCode) {
        this.mCountryCode = countryCode;
    }

    public void setIpType(int type) {
        this.mIpType = type;
    }

    private boolean isNeedReportAccessWebFailedEvent(long now, boolean commercialUser) {
        if (commercialUser) {
            if ((this.mLastAccessWebFailedTime == 0 || now - this.mLastAccessWebFailedTime > ACCESSWEB_SLOW_OR_FAILED_UPLOAD_MIN) && ((long) this.mUploadAccessWebFailedCount) < ACCESSWEB_SLOW_OR_FAILED_UPLOAD_COUNT) {
                return true;
            }
            return HWFLOW;
        } else if ((this.mLastAccessWebFailedTime == 0 || now - this.mLastAccessWebFailedTime > ACCESSWEB_SLOW_OR_FAILED_BETA_UPLOAD_MIN) && ((long) this.mUploadAccessWebFailedCount) < ACCESSWEB_SLOW_OR_FAILED_BETA_UPLOAD_COUNT) {
            return true;
        } else {
            return HWFLOW;
        }
    }

    public void uploadDFTEvent(int type) {
        Log.d(TAG, "IMonitor upload event " + type);
        switch (type) {
            case 909001001:
                try {
                    HwWifiDFTStabilityStat hwWifiDFTStabilityStat = new HwWifiDFTStabilityStat();
                    HwWifiStatStoreImpl.getDefault().getWifiStabilityStat(hwWifiDFTStabilityStat);
                    EventStream eventStabilityStat = IMonitor.openEventStream(909001001);
                    if (eventStabilityStat == null) {
                        Log.e(TAG, "eventStabilityStat is null.");
                        return;
                    }
                    eventStabilityStat.setParam((short) 0, hwWifiDFTStabilityStat.mOpenCount).setParam((short) 1, hwWifiDFTStabilityStat.mOPenSuccCount).setParam((short) 2, hwWifiDFTStabilityStat.mOPenDuration).setParam((short) 3, hwWifiDFTStabilityStat.mCloseCount).setParam((short) 4, hwWifiDFTStabilityStat.mCloseSuccCount).setParam(THRESHOD_ACCESS_NETWORK_DHCP_FAILED_COUNT, hwWifiDFTStabilityStat.mCloseDuration).setParam((short) 6, Boolean.valueOf(hwWifiDFTStabilityStat.mIsWifiProON)).setParam((short) 7, hwWifiDFTStabilityStat.mWifiProSwcnt).setParam((short) 8, Boolean.valueOf(hwWifiDFTStabilityStat.mIsScanAlwaysAvalible)).setParam((short) 9, hwWifiDFTStabilityStat.mScanAlwaysSwCnt).setParam((short) 10, Boolean.valueOf(hwWifiDFTStabilityStat.mIsWifiNotificationOn)).setParam((short) 11, hwWifiDFTStabilityStat.mWifiNotifationSwCnt).setParam((short) 12, hwWifiDFTStabilityStat.mWifiSleepPolicy).setParam((short) 13, hwWifiDFTStabilityStat.mWifiSleepSwCnt).setParam((short) 14, hwWifiDFTStabilityStat.mWifiToPdp).setParam((short) 15, hwWifiDFTStabilityStat.mWifiToPdpSwCnt);
                    IMonitor.sendEvent(eventStabilityStat);
                    IMonitor.closeEventStream(eventStabilityStat);
                    Log.d(TAG, "IMonitor send eventStabilityStat");
                } catch (Exception e) {
                    Log.e(TAG, "uploadDFTEvent error.");
                }
            case 909001002:
                try {
                    List<HwWifiDFTStabilitySsidStat> listHwWifiDFTStabilitySsidStat = new ArrayList();
                    HwWifiStatStoreImpl.getDefault().getWifiStabilitySsidStat(listHwWifiDFTStabilitySsidStat);
                    short ssidStatSize = (short) listHwWifiDFTStabilitySsidStat.size();
                    if (ssidStatSize == (short) 0) {
                        Log.d(TAG, "wifi ssid stability do not need trigger.");
                        return;
                    }
                    for (short i = (short) 0; i < ssidStatSize; i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                        EventStream eventStabilitySsidStat = IMonitor.openEventStream(909001002);
                        if (eventStabilitySsidStat == null) {
                            Log.e(TAG, "eventStabilitySsidStat is null.");
                            return;
                        }
                        int i2;
                        HwWifiDFTStabilitySsidStat hwWifiDFTStabilitySsidStat = (HwWifiDFTStabilitySsidStat) listHwWifiDFTStabilitySsidStat.get(i);
                        EventStream param = eventStabilitySsidStat.setParam((short) 0, hwWifiDFTStabilitySsidStat.mApSsid).setParam((short) 1, this.mEssCount).setParam((short) 2, hwWifiDFTStabilitySsidStat.mAssocCount).setParam((short) 3, hwWifiDFTStabilitySsidStat.mAssocSuccCount).setParam((short) 4, hwWifiDFTStabilitySsidStat.mAuthCount).setParam(THRESHOD_ACCESS_NETWORK_DHCP_FAILED_COUNT, hwWifiDFTStabilitySsidStat.mAuthSuccCount).setParam((short) 6, hwWifiDFTStabilitySsidStat.mIpDhcpCount).setParam((short) 7, hwWifiDFTStabilitySsidStat.mDhcpSuccCount).setParam((short) 8, hwWifiDFTStabilitySsidStat.mIpStaticCount).setParam((short) 9, hwWifiDFTStabilitySsidStat.mIpAutoCount).setParam((short) 10, hwWifiDFTStabilitySsidStat.mConnectedCount).setParam((short) 11, hwWifiDFTStabilitySsidStat.mAbnormalDisconnCount).setParam((short) 12, hwWifiDFTStabilitySsidStat.mAssocDuration).setParam((short) 13, hwWifiDFTStabilitySsidStat.mAuthDuration).setParam((short) 14, hwWifiDFTStabilitySsidStat.mDhcpDuration).setParam((short) 15, hwWifiDFTStabilitySsidStat.mConnectingDuration).setParam((short) 16, hwWifiDFTStabilitySsidStat.mConnectionDuration).setParam((short) 17, hwWifiDFTStabilitySsidStat.mDnsReqCnt).setParam((short) 18, hwWifiDFTStabilitySsidStat.mDnsReqFail).setParam((short) 19, hwWifiDFTStabilitySsidStat.mDnsAvgTime).setParam(THRESHOD_ACCESS_NETWORK_TOTOAL_FAILED_COUNT, hwWifiDFTStabilitySsidStat.mDhcpRenewCount).setParam((short) 21, hwWifiDFTStabilitySsidStat.mDhcpRenewSuccCount).setParam((short) 22, hwWifiDFTStabilitySsidStat.mDhcpRenewDuration).setParam((short) 23, hwWifiDFTStabilitySsidStat.mRoamingCount).setParam((short) 24, hwWifiDFTStabilitySsidStat.mRoamingSuccCount).setParam((short) 25, hwWifiDFTStabilitySsidStat.mRoamingDuration).setParam((short) 26, hwWifiDFTStabilitySsidStat.mRekeyCount).setParam((short) 27, hwWifiDFTStabilitySsidStat.mRekeySuccCount).setParam((short) 28, hwWifiDFTStabilitySsidStat.mRekeyDuration).setParam((short) 29, hwWifiDFTStabilitySsidStat.mAccessWebfailCnt).setParam((short) 30, hwWifiDFTStabilitySsidStat.mAccessWebSlowlyCnt).setParam((short) 31, hwWifiDFTStabilitySsidStat.mGwIpCount).setParam((short) 32, this.mMultiGWCount);
                        if (this.mRssiCnt == 0) {
                            i2 = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                        } else {
                            i2 = this.mRssiSum / this.mRssiCnt;
                        }
                        param.setParam((short) 33, i2);
                        IMonitor.sendEvent(eventStabilitySsidStat);
                        IMonitor.closeEventStream(eventStabilitySsidStat);
                        Log.d(TAG, "IMonitor eventStabilitySsidStat " + i + "/" + ssidStatSize);
                    }
                } catch (Exception e2) {
                    Log.e(TAG, "uploadDFTEvent error.");
                }
            case 909002011:
                try {
                    if (this.mHwWifiDFTUtilImpl != null) {
                        EventStream eventOpenCloseFailed = IMonitor.openEventStream(909002011);
                        if (eventOpenCloseFailed == null) {
                            Log.e(TAG, "eventOpenCloseFailed is null.");
                            return;
                        }
                        eventOpenCloseFailed.setParam((short) 0, this.mHwWifiDFTUtilImpl.mOpenCloseFailed).setParam((short) 1, this.mHwWifiDFTUtilImpl.mSubErrorOfOpenCloseFailed);
                        IMonitor.sendEvent(eventOpenCloseFailed);
                        IMonitor.closeEventStream(eventOpenCloseFailed);
                    }
                } catch (Exception e3) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002011");
                }
            case 909002012:
                try {
                    EventStream abnormalDisconnect = IMonitor.openEventStream(909002012);
                    if (abnormalDisconnect == null) {
                        Log.e(TAG, "abnormalDisconnect is null.");
                        return;
                    }
                    abnormalDisconnect.setParam((short) 0, this.aucAbnormal_disconnect).setParam((short) 1, WIFI_SETTING_CLOSED_AND_SERVICE_OPENED);
                    IMonitor.sendEvent(abnormalDisconnect);
                    IMonitor.closeEventStream(abnormalDisconnect);
                } catch (Exception e4) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002012");
                }
            case 909002013:
                try {
                    EventStream scanFailed = IMonitor.openEventStream(909002013);
                    if (scanFailed == null) {
                        Log.e(TAG, "scanFailed is null.");
                        return;
                    }
                    scanFailed.setParam((short) 0, WIFI_SETTING_CLOSED_AND_SERVICE_OPENED).setParam((short) 1, this.mScanFailCount);
                    IMonitor.sendEvent(scanFailed);
                    IMonitor.closeEventStream(scanFailed);
                } catch (Exception e5) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002013");
                }
            case 909002014:
                try {
                    EventStream connectedFailed = IMonitor.openEventStream(909002014);
                    if (connectedFailed == null) {
                        Log.e(TAG, "connectedFailed is null.");
                        return;
                    }
                    connectedFailed.setParam((short) 0, this.mConnectFailedReason).setParam((short) 1, this.mConnectFailedSubErrorCode);
                    IMonitor.sendEvent(connectedFailed);
                    IMonitor.closeEventStream(connectedFailed);
                } catch (Exception e6) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002014");
                }
            case 909002015:
                try {
                    EventStream accessInternetFailed = IMonitor.openEventStream(909002015);
                    if (accessInternetFailed == null) {
                        Log.e(TAG, "accessInternetFailed is null.");
                        return;
                    }
                    accessInternetFailed.setParam((short) 0, this.aucAccess_internet_failed).setParam((short) 1, WIFI_SETTING_CLOSED_AND_SERVICE_OPENED);
                    IMonitor.sendEvent(accessInternetFailed);
                    IMonitor.closeEventStream(accessInternetFailed);
                } catch (Exception e7) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002015");
                }
            case 909002016:
                try {
                    EventStream accessWebSlowly = IMonitor.openEventStream(909002016);
                    if (accessWebSlowly == null) {
                        Log.e(TAG, "accessWebSlowly is null.");
                        return;
                    }
                    accessWebSlowly.setParam((short) 0, (short) this.accessSlowlyerrrCode).setParam((short) 1, WIFI_SETTING_CLOSED_AND_SERVICE_OPENED);
                    IMonitor.sendEvent(accessWebSlowly);
                    IMonitor.closeEventStream(accessWebSlowly);
                } catch (Exception e8) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002016");
                }
            case 909002017:
                try {
                    EventStream deviceFirmwareException110X = IMonitor.openEventStream(909002017);
                    if (deviceFirmwareException110X == null) {
                        Log.e(TAG, "deviceFirmwareException110X is null.");
                        return;
                    }
                    deviceFirmwareException110X.setParam((short) 0, WIFI_SETTING_CLOSED_AND_SERVICE_OPENED).setParam((short) 1, WIFI_SETTING_CLOSED_AND_SERVICE_OPENED);
                    IMonitor.sendEvent(deviceFirmwareException110X);
                    IMonitor.closeEventStream(deviceFirmwareException110X);
                } catch (Exception e9) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002017");
                }
            case 909002018:
                try {
                    EventStream wifiProException = IMonitor.openEventStream(909002018);
                    if (wifiProException == null) {
                        Log.e(TAG, "wifiProException is null.");
                        return;
                    }
                    wifiProException.setParam((short) 0, WIFI_SETTING_CLOSED_AND_SERVICE_OPENED);
                    IMonitor.sendEvent(wifiProException);
                    IMonitor.closeEventStream(wifiProException);
                } catch (Exception e10) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002018");
                }
            case 909002019:
                try {
                    if (this.mHwWifiDFTUtilImpl != null) {
                        EventStream wifiProDualbandException = IMonitor.openEventStream(909002019);
                        if (wifiProDualbandException == null) {
                            Log.e(TAG, "wifiProDualbandException is null.");
                            return;
                        }
                        wifiProDualbandException.setParam((short) 0, this.mHwWifiDFTUtilImpl.mWifiProExceptionDualbandSubError);
                        IMonitor.sendEvent(wifiProDualbandException);
                        IMonitor.closeEventStream(wifiProDualbandException);
                    }
                } catch (Exception e11) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002019");
                }
            default:
                Log.w(TAG, "IMonitor unknown event.");
        }
    }

    private List<ChrLogBaseModel> getWiFiCHRModel(int type, String ucSubErrorCode, boolean commercialUser, Date date) {
        int iIsMobileAP;
        InterruptedException e;
        Throwable th;
        CSegEVENT_WIFI_CONNECT_EVENT cSegEVENT_WIFI_CONNECT_EVENT;
        long elapsedRealtime = SystemClock.elapsedRealtime();
        List<ChrLogBaseModel> result = new ArrayList();
        ChrLogBaseModel chrLogBaseModel = null;
        byte bluetoothState = (byte) getBluetoothState();
        byte bluetoothConnState = (byte) getBluetoothConnectionState();
        String bssid = this.strAp_mac;
        String bssidMask = this.strAp_mac;
        boolean rstFlg = HWFLOW;
        if (commercialUser) {
            bssidMask = maskMacAddress(this.strAp_mac);
        }
        if (HWFLOW) {
            Log.d(TAG, "getWiFiCHRModel type = " + type);
        }
        int relatedAps;
        List<CSubRSSIGROUP_EVENT> rssiGroupCHR;
        byte essCount;
        int i;
        List<CSubRSSIGROUP_EVENT_EX> rssiGroupCHR2;
        CSubBTStatus BtStatus;
        CSubCellID CellID;
        CSubNET_CFG netCfg;
        CSubTRAFFIC_GROUND traffic_Group;
        CSubMemInfo mem;
        CSubCPUInfo cpu;
        CSubDNS dns;
        CSubTCP_STATIST tcpStat;
        CSubApRoaming ApRoaming;
        int tx_frame_amount;
        int tx_byte_amount;
        int tx_data_frame_err_amount;
        int tx_retrans_amount;
        int rx_frame_amount;
        int rx_byte_amount;
        int rx_beacon_from_assoc_ap;
        int lost_beacon_amount;
        switch (type) {
            case HwWifiCHRConstImpl.WIFI_OPEN_FAILED /*80*/:
            case HwWifiCHRConstImpl.WIFI_CLOSE_FAILED /*81*/:
                String openFailReason = "NOT_OPEN_FAIL";
                String closeFailReason = "NOT_CLOSE_FAIL";
                boolean opencloseflag = HWFLOW;
                if (type == 80) {
                    openFailReason = ucSubErrorCode;
                    opencloseflag = true;
                } else {
                    closeFailReason = ucSubErrorCode;
                }
                if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mOpenCloseFailCount)) {
                    CSegEVENT_WIFI_OPEN_CLOSE_FAILED cSegEVENT_WIFI_OPEN_CLOSE_FAILED = new CSegEVENT_WIFI_OPEN_CLOSE_FAILED();
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.enucHwStatus.setValue("READY");
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.tmTimeStamp.setValue(date);
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.strSTA_MAC.setValue(this.strSta_mac);
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.enOPEN_FAILED.setValue(openFailReason);
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.enCLOSE_FAILED.setValue(closeFailReason);
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.usSubErrorCode.setValue(this.aucClose_failed);
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.strRemark.setValue(this.mRemark);
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.ucBTState.setValue(bluetoothState);
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.ucBTConnState.setValue(bluetoothConnState);
                    cSegEVENT_WIFI_OPEN_CLOSE_FAILED.ucIsOnScreen.setValue(this.mScreenState);
                    this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                    if (this.hwWifiCHRService != null) {
                        cSegEVENT_WIFI_OPEN_CLOSE_FAILED.ucScanAlwaysAvailble.setValue(this.hwWifiCHRService.getPersistedScanAlwaysAvailable());
                        cSegEVENT_WIFI_OPEN_CLOSE_FAILED.ucWIFIAlwaysNotifation.setValue(this.hwWifiCHRService.getWIFINetworkAvailableNotificationON());
                        cSegEVENT_WIFI_OPEN_CLOSE_FAILED.ucWIFISleepPolicy.setValue(this.hwWifiCHRService.getWIFISleepPolicy());
                        cSegEVENT_WIFI_OPEN_CLOSE_FAILED.ucWifiProStatus.setValue(this.hwWifiCHRService.getWIFIProStatus());
                        cSegEVENT_WIFI_OPEN_CLOSE_FAILED.ucWifiToPDP.setValue(this.hwWifiCHRService.getWIFITOPDP());
                    }
                    chrLogBaseModel = cSegEVENT_WIFI_OPEN_CLOSE_FAILED;
                }
                this.mOpenCloseFailCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                if (this.hwWifiCHRService != null) {
                    this.hwWifiCHRService.updateOpenCloseStat(opencloseflag);
                    break;
                }
                break;
            case HwWifiCHRConstImpl.WIFI_CONNECT_AUTH_FAILED /*82*/:
                if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mAuthFailCount)) {
                    relatedAps = getRelatedApInfo(bssid);
                    rssiGroupCHR = this.mRssiGroup.getRSSIGroupCHR();
                    essCount = (byte) (relatedAps >> ANT_SEC_WORK_TIME);
                    CSegEVENT_WIFI_CONNECT_AUTH_FAILED connectAuthFailedModel = new CSegEVENT_WIFI_CONNECT_AUTH_FAILED();
                    connectAuthFailedModel.enucHwStatus.setValue("READY");
                    connectAuthFailedModel.iAP_RSSI.setValue(this.mRssi);
                    connectAuthFailedModel.strAP_MAC.setValue(bssidMask);
                    connectAuthFailedModel.strSTA_MAC.setValue(this.strSta_mac);
                    connectAuthFailedModel.strAP_SSID.setValue(this.strAp_Ssid);
                    connectAuthFailedModel.tmTimeStamp.setValue(date);
                    connectAuthFailedModel.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    connectAuthFailedModel.usAP_channel.setValue(this.usAP_channel);
                    connectAuthFailedModel.strAP_proto.setValue(this.strAP_proto);
                    connectAuthFailedModel.strAP_key_mgmt.setValue(this.strAP_key_mgmt);
                    connectAuthFailedModel.strAP_auth_alg.setValue(this.strAP_auth_alg);
                    connectAuthFailedModel.strAP_pairwise.setValue(this.strAP_pairwise);
                    connectAuthFailedModel.strAP_group.setValue(this.strAP_gruop);
                    connectAuthFailedModel.strAP_eap.setValue(this.strAP_eap);
                    connectAuthFailedModel.usSubErrorCode.setValue(this.mAuthSubCode);
                    connectAuthFailedModel.strapVendorInfo.setValue(this.mAPVendorInfo);
                    connectAuthFailedModel.ucBTState.setValue(bluetoothState);
                    connectAuthFailedModel.ucBTConnState.setValue(bluetoothConnState);
                    connectAuthFailedModel.ucPublicEss.setValue(essCount);
                    for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < rssiGroupCHR.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                        connectAuthFailedModel.setCSubRSSIGROUP_EVENTList((CSubRSSIGROUP_EVENT) rssiGroupCHR.get(i));
                    }
                    connectAuthFailedModel.ucIsOnScreen.setValue(this.mScreenState);
                    this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                    if (this.hwWifiCHRService != null) {
                        connectAuthFailedModel.ucScanAlwaysAvailble.setValue(this.hwWifiCHRService.getPersistedScanAlwaysAvailable());
                        connectAuthFailedModel.ucWIFIAlwaysNotifation.setValue(this.hwWifiCHRService.getWIFINetworkAvailableNotificationON());
                        connectAuthFailedModel.ucWIFISleepPolicy.setValue(this.hwWifiCHRService.getWIFISleepPolicy());
                        connectAuthFailedModel.ucWifiProStatus.setValue(this.hwWifiCHRService.getWIFIProStatus());
                        connectAuthFailedModel.ucWifiToPDP.setValue(this.hwWifiCHRService.getWIFITOPDP());
                    }
                    chrLogBaseModel = connectAuthFailedModel;
                }
                rstFlg = true;
                this.mAuthFailCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                break;
            case HwWifiCHRConstImpl.WIFI_CONNECT_ASSOC_FAILED /*83*/:
                if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mAssocFailCount)) {
                    relatedAps = getRelatedApInfo(bssid);
                    rssiGroupCHR = this.mRssiGroup.getRSSIGroupCHR();
                    essCount = (byte) (relatedAps >> ANT_SEC_WORK_TIME);
                    ScanResult sr = getScanResultByBssid(bssid);
                    int rssi = sr != null ? sr.level : WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                    CSegEVENT_WIFI_CONNECT_ASSOC_FAILED assocRejectModel = new CSegEVENT_WIFI_CONNECT_ASSOC_FAILED();
                    assocRejectModel.enucHwStatus.setValue("READY");
                    assocRejectModel.iAP_RSSI.setValue(rssi);
                    assocRejectModel.strAP_MAC.setValue(bssidMask);
                    assocRejectModel.strSTA_MAC.setValue(this.strSta_mac);
                    assocRejectModel.strAP_SSID.setValue(this.strAp_Ssid);
                    assocRejectModel.tmTimeStamp.setValue(date);
                    assocRejectModel.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    assocRejectModel.usAP_channel.setValue(this.usAP_channel);
                    assocRejectModel.usSubErrorCode.setValue(this.mSubcodeReject);
                    assocRejectModel.ucBTState.setValue(bluetoothState);
                    assocRejectModel.ucBTConnState.setValue(bluetoothConnState);
                    assocRejectModel.ucPublicEss.setValue(essCount);
                    for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < rssiGroupCHR.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                        assocRejectModel.setCSubRSSIGROUP_EVENTList((CSubRSSIGROUP_EVENT) rssiGroupCHR.get(i));
                    }
                    assocRejectModel.ucIsOnScreen.setValue(this.mScreenState);
                    this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                    if (this.hwWifiCHRService != null) {
                        assocRejectModel.ucScanAlwaysAvailble.setValue(this.hwWifiCHRService.getPersistedScanAlwaysAvailable());
                        assocRejectModel.ucWIFIAlwaysNotifation.setValue(this.hwWifiCHRService.getWIFINetworkAvailableNotificationON());
                        assocRejectModel.ucWIFISleepPolicy.setValue(this.hwWifiCHRService.getWIFISleepPolicy());
                        assocRejectModel.ucWifiProStatus.setValue(this.hwWifiCHRService.getWIFIProStatus());
                        assocRejectModel.ucWifiToPDP.setValue(this.hwWifiCHRService.getWIFITOPDP());
                    }
                    chrLogBaseModel = assocRejectModel;
                }
                this.mAssocFailCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                rstFlg = true;
                break;
            case HwWifiCHRConstImpl.WIFI_CONNECT_DHCP_FAILED /*84*/:
                if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mDhcpFailCount)) {
                    relatedAps = getRelatedApInfo(bssid);
                    rssiGroupCHR = this.mRssiGroup.getRSSIGroupCHR();
                    essCount = (byte) (relatedAps >> ANT_SEC_WORK_TIME);
                    CSegEVENT_WIFI_CONNECT_DHCP_FAILED cSegEVENT_WIFI_CONNECT_DHCP_FAILED = new CSegEVENT_WIFI_CONNECT_DHCP_FAILED();
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.enucHwStatus.setValue("READY");
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strWIFI_IP.setValue(this.str_Wifi_ip);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.iAP_RSSI.setValue(this.mRssi);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strWIFI_GATE.setValue(this.str_gate_ip);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strIP_LEASETIME.setValue(this.strIp_leasetime);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strDNS_ADDRESS.setValue(this.str_dns);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strAP_MAC.setValue(bssidMask);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strSTA_MAC.setValue(this.strSta_mac);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strAP_SSID.setValue(this.strAp_Ssid);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strRoutes.setValue(this.strRoutes);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.tmTimeStamp.setValue(date);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.usAP_link_speed.setValue(this.usLinkSpeed);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.usAP_channel.setValue(this.usAP_channel);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strAP_proto.setValue(this.strAP_proto);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strAP_key_mgmt.setValue(this.strAP_key_mgmt);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strAP_auth_alg.setValue(this.strAP_auth_alg);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strAP_pairwise.setValue(this.strAP_pairwise);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strAP_group.setValue(this.strAP_gruop);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strAP_eap.setValue(this.strAP_eap);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.usSubErrorCode.setValue(this.aucDhcp_failed);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.enDHCP_FAILED.setValue(ucSubErrorCode);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strapVendorInfo.setValue(this.mAPVendorInfo);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucBTState.setValue(bluetoothState);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucBTConnState.setValue(bluetoothConnState);
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucPublicEss.setValue(essCount);
                    for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < rssiGroupCHR.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                        cSegEVENT_WIFI_CONNECT_DHCP_FAILED.setCSubRSSIGROUP_EVENTList((CSubRSSIGROUP_EVENT) rssiGroupCHR.get(i));
                    }
                    cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucIsOnScreen.setValue(this.mScreenState);
                    this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                    if (this.hwWifiCHRService != null) {
                        cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucScanAlwaysAvailble.setValue(this.hwWifiCHRService.getPersistedScanAlwaysAvailable());
                        cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucWIFIAlwaysNotifation.setValue(this.hwWifiCHRService.getWIFINetworkAvailableNotificationON());
                        cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucWIFISleepPolicy.setValue(this.hwWifiCHRService.getWIFISleepPolicy());
                        cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucWifiProStatus.setValue(this.hwWifiCHRService.getWIFIProStatus());
                        cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucProxySettings.setValue(this.hwWifiCHRService.getProxyStatus());
                        cSegEVENT_WIFI_CONNECT_DHCP_FAILED.strProxySettingInfo.setValue(this.hwWifiCHRService.getProxyInfo());
                        cSegEVENT_WIFI_CONNECT_DHCP_FAILED.ucWifiToPDP.setValue(this.hwWifiCHRService.getWIFITOPDP());
                    }
                    chrLogBaseModel = cSegEVENT_WIFI_CONNECT_DHCP_FAILED;
                }
                this.mDhcpFailCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                rstFlg = true;
                break;
            case HwWifiCHRConstImpl.WIFI_ABNORMAL_DISCONNECT /*85*/:
                if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mDisconnTotalCount)) {
                    relatedAps = getRelatedApInfo(bssid);
                    rssiGroupCHR2 = this.mRssiGroup.getRSSIGroupEXCHR();
                    essCount = (byte) (relatedAps >> ANT_SEC_WORK_TIME);
                    CSegEVENT_WIFI_ABNORMAL_DISCONNECT cSegEVENT_WIFI_ABNORMAL_DISCONNECT = new CSegEVENT_WIFI_ABNORMAL_DISCONNECT();
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.enucHwStatus.setValue("READY");
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strWIFI_IP.setValue(this.str_Wifi_ip);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.iAP_RSSI.setValue(this.mRssi);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strWIFI_GATE.setValue(this.str_gate_ip);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strDNS_ADDRESS.setValue(this.str_dns);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strAP_MAC.setValue(bssidMask);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strIP_LEASETIME.setValue(this.strIp_leasetime);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strSTA_MAC.setValue(this.strSta_mac);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strAP_SSID.setValue(this.strAp_Ssid);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strRoutes.setValue(this.strRoutes);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.tmTimeStamp.setValue(date);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.usAP_link_speed.setValue(this.usLinkSpeed);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.usAP_channel.setValue(this.usAP_channel);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strAP_proto.setValue(this.strAP_proto);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strAP_key_mgmt.setValue(this.strAP_key_mgmt);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strAP_auth_alg.setValue(this.strAP_auth_alg);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strAP_pairwise.setValue(this.strAP_pairwise);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strAP_group.setValue(this.strAP_gruop);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strAP_eap.setValue(this.strAP_eap);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.usSubErrorCode.setValue(this.aucAbnormal_disconnect);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.enABNORMAL_DISCONNECT.setValue(ucSubErrorCode);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.strapVendorInfo.setValue(this.mAPVendorInfo);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucBTState.setValue(bluetoothState);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucBTConnState.setValue(bluetoothConnState);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucPublicEss.setValue(essCount);
                    for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < rssiGroupCHR2.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                        cSegEVENT_WIFI_ABNORMAL_DISCONNECT.setCSubRSSIGROUP_EVENT_EXList((CSubRSSIGROUP_EVENT_EX) rssiGroupCHR2.get(i));
                    }
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucIsOnScreen.setValue(this.mScreenState);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ideltaTime.setValue(this.mDeltaTime / HwWifiStateMachine.AP_CAP_CACHE_COUNT);
                    cSegEVENT_WIFI_ABNORMAL_DISCONNECT.usdisconnectCnt.setValue(this.mDisconnectCnt);
                    this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                    if (this.hwWifiCHRService != null) {
                        cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucScanAlwaysAvailble.setValue(this.hwWifiCHRService.getPersistedScanAlwaysAvailable());
                        cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucWIFIAlwaysNotifation.setValue(this.hwWifiCHRService.getWIFINetworkAvailableNotificationON());
                        cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucWIFISleepPolicy.setValue(this.hwWifiCHRService.getWIFISleepPolicy());
                        cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucWifiProStatus.setValue(this.hwWifiCHRService.getWIFIProStatus());
                        cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucWifiToPDP.setValue(this.hwWifiCHRService.getWIFITOPDP());
                    }
                    Log.d(TAG, "strAbnormal_disconnect " + ucSubErrorCode + " dns " + this.str_dns + " ip " + this.str_Wifi_ip);
                    String chipType = SystemProperties.get("ro.connectivity.chiptype", "");
                    if (this.mDualAntsChr && chipType != null) {
                        if (chipType.equalsIgnoreCase("hisi")) {
                            if (HWFLOW) {
                                Log.d(TAG, "getWifiAntsInfo: WIFI_ABNORMAL_DISCONNECT start");
                            }
                            if (getWifiAntsInfo(WIFI_SECURITY_TYPE_UNKNOWN)) {
                                cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucIsAntSWCauseBreak.setValue(this.wifiAntsStatus.getIsAntSWCauseError() ? WIFI_SETTING_OPENED_AND_SERVICE_CLOSED : WIFI_SETTING_CLOSED_AND_SERVICE_OPENED);
                                cSegEVENT_WIFI_ABNORMAL_DISCONNECT.ucAntCurWork.setValue(this.wifiAntsStatus.getAntCurWork());
                                if (HWFLOW) {
                                    Log.d(TAG, "getWifiAntsInfo: WIFI_ABNORMAL_DISCONNECT end");
                                }
                            }
                        }
                    }
                    chrLogBaseModel = cSegEVENT_WIFI_ABNORMAL_DISCONNECT;
                }
                i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                while (i < MAX_DISCONN_CNT) {
                    if (this.mDisconnectFail[i] == 0) {
                        this.mDisconnectFail[i] = (this.aucAbnormal_disconnect << ANT_SEC_WORK_TIME) + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                    } else if ((this.mDisconnectFail[i] >> ANT_SEC_WORK_TIME) == this.aucAbnormal_disconnect) {
                        int[] iArr = this.mDisconnectFail;
                        iArr[i] = iArr[i] + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                    } else {
                        i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                    }
                    HwWifiStatStoreImpl.getDefault().setAbDisconnectFlg(this.strAp_Ssid);
                    this.mDisconnTotalCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                    rstFlg = true;
                    break;
                }
                HwWifiStatStoreImpl.getDefault().setAbDisconnectFlg(this.strAp_Ssid);
                this.mDisconnTotalCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                rstFlg = true;
            case HwWifiCHRConstImpl.WIFI_SCAN_FAILED /*86*/:
                if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mScanFailCount)) {
                    CSegEVENT_WIFI_SCAN_FAILED cSegEVENT_WIFI_SCAN_FAILED = new CSegEVENT_WIFI_SCAN_FAILED();
                    cSegEVENT_WIFI_SCAN_FAILED.enucHwStatus.setValue("READY");
                    cSegEVENT_WIFI_SCAN_FAILED.strSTA_MAC.setValue(this.strSta_mac);
                    cSegEVENT_WIFI_SCAN_FAILED.tmTimeStamp.setValue(date);
                    cSegEVENT_WIFI_SCAN_FAILED.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    cSegEVENT_WIFI_SCAN_FAILED.usSubErrorCode.setValue(this.aucScan_failed);
                    cSegEVENT_WIFI_SCAN_FAILED.enSCAN_FAILED.setValue("CONNECTED");
                    cSegEVENT_WIFI_SCAN_FAILED.ucBTState.setValue(bluetoothState);
                    cSegEVENT_WIFI_SCAN_FAILED.ucBTConnState.setValue(bluetoothConnState);
                    this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                    if (this.hwWifiCHRService != null) {
                        cSegEVENT_WIFI_SCAN_FAILED.ucScanAlwaysAvailble.setValue(this.hwWifiCHRService.getPersistedScanAlwaysAvailable());
                        cSegEVENT_WIFI_SCAN_FAILED.ucWIFIAlwaysNotifation.setValue(this.hwWifiCHRService.getWIFINetworkAvailableNotificationON());
                        cSegEVENT_WIFI_SCAN_FAILED.ucWIFISleepPolicy.setValue(this.hwWifiCHRService.getWIFISleepPolicy());
                        cSegEVENT_WIFI_SCAN_FAILED.ucWifiProStatus.setValue(this.hwWifiCHRService.getWIFIProStatus());
                    }
                    cSegEVENT_WIFI_SCAN_FAILED.ucIsOnScreen.setValue(this.mScreenState);
                    chrLogBaseModel = cSegEVENT_WIFI_SCAN_FAILED;
                }
                this.mScanFailCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                break;
            case HwWifiCHRConstImpl.WIFI_ACCESS_INTERNET_FAILED /*87*/:
                if (this.mContextRef != null) {
                    if (isNeedReportAccessWebFailedEvent(elapsedRealtime, commercialUser)) {
                        if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mAccessFailCount)) {
                            updateCHRConnInternetFailedType(ucSubErrorCode);
                            BtStatus = HwCHRWifiRelatedStateMonitor.make(this.mContextRef).getBTStateCHR();
                            CellID = HwCHRWifiRelatedStateMonitor.make(this.mContextRef).getCellIDCHR();
                            netCfg = HwCHRWifiRelatedStateMonitor.make(this.mContextRef).getSSIDSetting();
                            traffic_Group = HWNetstatManager.getBack_Front_Summery();
                            netCfg.iIp_Type.setValue(this.mIpType);
                            mem = HwCHRWebMonitor.getMemCHR();
                            cpu = HwCHRWebMonitor.getCpuCHR();
                            dns = HwCHRWebMonitor.getDNSCHR();
                            tcpStat = HwCHRWebSpeed.getDefault().getTcpStatistCHR();
                            iIsMobileAP = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                            if (this.mContextRef != null) {
                                iIsMobileAP = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContextRef) ? WIFI_SETTING_OPENED_AND_SERVICE_CLOSED : WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                            }
                            relatedAps = getRelatedApInfo(bssid);
                            rssiGroupCHR2 = this.mRssiGroup.getRSSIGroupEXCHR();
                            essCount = (byte) (relatedAps >> ANT_SEC_WORK_TIME);
                            ApRoaming = getApRoaming(this.strAp_Ssid, this.mCurrentApBssid);
                            CSegEVENT_WIFI_ACCESS_INTERNET_FAILED accessInterModel = new CSegEVENT_WIFI_ACCESS_INTERNET_FAILED();
                            accessInterModel.enucHwStatus.setValue("READY");
                            accessInterModel.strWIFI_IP.setValue(this.str_Wifi_ip);
                            accessInterModel.iAP_RSSI.setValue(this.mRssi);
                            accessInterModel.strWIFI_GATE.setValue(this.str_gate_ip);
                            accessInterModel.strIP_LEASETIME.setValue(this.strIp_leasetime);
                            accessInterModel.strDNS_ADDRESS.setValue(this.str_dns);
                            accessInterModel.strAP_MAC.setValue(bssidMask);
                            accessInterModel.ucIsMobleAP.setValue(iIsMobileAP);
                            accessInterModel.strSTA_MAC.setValue(this.strSta_mac);
                            accessInterModel.strAP_SSID.setValue(this.strAp_Ssid);
                            accessInterModel.strRoutes.setValue(this.strRoutes);
                            accessInterModel.tmTimeStamp.setValue(date);
                            accessInterModel.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                            accessInterModel.usAP_link_speed.setValue(this.usLinkSpeed);
                            accessInterModel.usAP_channel.setValue(this.usAP_channel);
                            accessInterModel.strAP_proto.setValue(this.strAP_proto);
                            accessInterModel.strAP_key_mgmt.setValue(this.strAP_key_mgmt);
                            accessInterModel.strAP_auth_alg.setValue(this.strAP_auth_alg);
                            accessInterModel.strAP_pairwise.setValue(this.strAP_pairwise);
                            accessInterModel.strAP_group.setValue(this.strAP_gruop);
                            accessInterModel.strAP_eap.setValue(this.strAP_eap);
                            accessInterModel.usSubErrorCode.setValue(this.aucAccess_internet_failed);
                            accessInterModel.enACCESS_INTERNET_FAILED.setValue(ucSubErrorCode);
                            accessInterModel.strapVendorInfo.setValue(this.mAPVendorInfo);
                            accessInterModel.ucPublicEss.setValue(essCount);
                            accessInterModel.ucIsOnScreen.setValue(this.mScreenState);
                            accessInterModel.ucGWCount.setValue(this.mMultiGWCount);
                            accessInterModel.ucCheckReason.setValue(this.mCheckReason);
                            accessInterModel.setCSubCellID(CellID);
                            accessInterModel.setCSubNET_CFG(netCfg);
                            accessInterModel.setCSubTRAFFIC_GROUND(traffic_Group);
                            accessInterModel.setCSubBTStatus(BtStatus);
                            accessInterModel.setCSubCPUInfo(cpu);
                            accessInterModel.setCSubMemInfo(mem);
                            accessInterModel.setCSubDNS(dns);
                            accessInterModel.ucisPortal.setValue(this.mIsPortalConnection);
                            accessInterModel.setCSubTCP_STATIST(tcpStat);
                            accessInterModel.ucPortalStatus.setValue(this.mPortalStatus);
                            accessInterModel.setCSubApRoaming(ApRoaming);
                            accessInterModel.strCountryCode.setValue(this.mCountryCode);
                            for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < rssiGroupCHR2.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                                accessInterModel.setCSubRSSIGROUP_EVENT_EXList((CSubRSSIGROUP_EVENT_EX) rssiGroupCHR2.get(i));
                            }
                            accessInterModel.setCSubWL_COUNTERS(this.mCounter_monitor.getCounterLst().getWLCountersCHR());
                            this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                            if (this.hwWifiCHRService != null) {
                                accessInterModel.ucScanAlwaysAvailble.setValue(this.hwWifiCHRService.getPersistedScanAlwaysAvailable());
                                accessInterModel.ucWIFIAlwaysNotifation.setValue(this.hwWifiCHRService.getWIFINetworkAvailableNotificationON());
                                accessInterModel.ucWIFISleepPolicy.setValue(this.hwWifiCHRService.getWIFISleepPolicy());
                                accessInterModel.ucWifiProStatus.setValue(this.hwWifiCHRService.getWIFIProStatus());
                                accessInterModel.ucWifiToPDP.setValue(this.hwWifiCHRService.getWIFITOPDP());
                            }
                            if (this.mAWS != null) {
                                accessInterModel.strIP_public.setValue(this.mAWS.getChinazIp() + HwCHRWifiCPUUsage.COL_SEP + this.mAWS.getChinazAddr());
                                accessInterModel.isdio_info_readbreq.setValue(this.mAWS.get_sdio_info_readbreq());
                                accessInterModel.isdio_info_readb.setValue(this.mAWS.get_sdio_info_readb());
                                accessInterModel.isdio_info_writebreq.setValue(this.mAWS.get_sdio_info_writebreq());
                                accessInterModel.isdio_info_writeb.setValue(this.mAWS.get_sdio_info_writeb());
                                accessInterModel.isdio_info_readwreq.setValue(this.mAWS.get_sdio_info_readwreq());
                                accessInterModel.isdio_info_readw.setValue(this.mAWS.get_sdio_info_readw());
                                accessInterModel.isdio_info_writewreq.setValue(this.mAWS.get_sdio_info_writewreq());
                                accessInterModel.isdio_info_writew.setValue(this.mAWS.get_sdio_info_writew());
                                accessInterModel.isdio_info_ksosetreq.setValue(this.mAWS.get_sdio_info_ksosetreq());
                                accessInterModel.isdio_info_ksosetretry.setValue(this.mAWS.get_sdio_info_ksosetretry());
                                accessInterModel.isdio_info_ksoclrreq.setValue(this.mAWS.get_sdio_info_ksoclrreq());
                                accessInterModel.isdio_info_ksoclrretry.setValue(this.mAWS.get_sdio_info_ksoclrretry());
                                accessInterModel.idetect_RTT_arp.setValue(this.mAWS.getRTTArp());
                                accessInterModel.idetect_RTT_baidu.setValue(this.mAWS.getRTTBaidu());
                                accessInterModel.uctraffic_aftersuspend.setValue(this.mAWS.getRxSleepCnt());
                                accessInterModel.strIP_route.setValue(this.mAWS.getIPRouteRet());
                                accessInterModel.strIP_firewall.setValue(this.mAWS.getDisableWlanApps());
                                accessInterModel.enCANNOT_ACCESS_WEB_STATUS.setValue(this.mAWS.getDetectWebStatus() == WIFI_SETTING_OPENED_AND_SERVICE_CLOSED ? "NEVER_ABLE" : "ONCE_ABLE");
                                accessInterModel.iAccessNetFailedCount.setValue(this.mAWS.getAccessNetFailedCount());
                                accessInterModel.iTime_NetUnusableTime.setValue(this.mAWS.getNetUnusableTime());
                                accessInterModel.iTime_NetAccessibleTime.setValue(this.mAWS.getNetAccessibleTime());
                                tx_frame_amount = this.mAWS.getTx_frame_amount() - this.mAWS.getPrevTx_frame_amount();
                                tx_byte_amount = this.mAWS.getTx_byte_amount() - this.mAWS.getPrevTx_byte_amount();
                                tx_data_frame_err_amount = this.mAWS.getTx_data_frame_error_amount() - this.mAWS.getPrevTx_data_frame_error_amount();
                                tx_retrans_amount = this.mAWS.getTx_retrans_amount() - this.mAWS.getPrevTx_retrans_amount();
                                rx_frame_amount = this.mAWS.getRx_frame_amount() - this.mAWS.getPrevRx_frame_amount();
                                rx_byte_amount = this.mAWS.getRx_byte_amount() - this.mAWS.getPrevRx_byte_amount();
                                rx_beacon_from_assoc_ap = this.mAWS.getRx_beacon_from_assoc_ap() - this.mAWS.getPrevRx_beacon_from_assoc_ap();
                                lost_beacon_amount = this.mAWS.getLost_beacon_amount() - this.mAWS.getPrevLost_beacon_amount();
                                if (tx_frame_amount < 0) {
                                    tx_frame_amount += Integer.MAX_VALUE;
                                }
                                if (tx_byte_amount < 0) {
                                    tx_byte_amount += Integer.MAX_VALUE;
                                }
                                if (tx_data_frame_err_amount < 0) {
                                    tx_data_frame_err_amount += Integer.MAX_VALUE;
                                }
                                if (tx_retrans_amount < 0) {
                                    tx_retrans_amount += Integer.MAX_VALUE;
                                }
                                if (rx_frame_amount < 0) {
                                    rx_frame_amount += Integer.MAX_VALUE;
                                }
                                if (rx_byte_amount < 0) {
                                    rx_byte_amount += Integer.MAX_VALUE;
                                }
                                if (rx_beacon_from_assoc_ap < 0) {
                                    rx_beacon_from_assoc_ap += Integer.MAX_VALUE;
                                }
                                if (lost_beacon_amount < 0) {
                                    lost_beacon_amount += Integer.MAX_VALUE;
                                }
                                accessInterModel.imonitor_interval.setValue(this.mAWS.getMonitor_interval());
                                accessInterModel.itx_frame_amount.setValue(tx_frame_amount);
                                accessInterModel.itx_byte_amount.setValue(tx_byte_amount);
                                accessInterModel.itx_data_frame_err_amount.setValue(tx_data_frame_err_amount);
                                accessInterModel.itx_retrans_amount.setValue(tx_retrans_amount);
                                accessInterModel.irx_frame_amount.setValue(rx_frame_amount);
                                accessInterModel.irx_byte_amount.setValue(rx_byte_amount);
                                accessInterModel.irx_beacon_from_assoc_ap.setValue(rx_beacon_from_assoc_ap);
                                accessInterModel.ucap_distance.setValue(this.mAWS.getAp_distance());
                                accessInterModel.ucdisturbing_degree.setValue(this.mAWS.getDisturbing_degree());
                                accessInterModel.ilost_beacon_amount.setValue(lost_beacon_amount);
                                if (HWFLOW) {
                                    Log.d(TAG, "WIFI_ACCESS_INTERNET_FAILED prev, monitor_interval = " + this.mAWS.getMonitor_interval() + ", tx_frame_amount = " + this.mAWS.getPrevTx_frame_amount() + ", tx_byte_amount = " + this.mAWS.getPrevTx_byte_amount() + ", tx_data_frame_err_amount = " + this.mAWS.getPrevTx_data_frame_error_amount() + ", tx_retrans_amount = " + this.mAWS.getPrevTx_retrans_amount() + ", rx_frame_amount = " + this.mAWS.getPrevRx_frame_amount() + ", rx_byte_amount = " + this.mAWS.getPrevRx_byte_amount() + ", rx_beacon_from_assoc_ap = " + this.mAWS.getPrevRx_beacon_from_assoc_ap() + ", ap_distance = " + this.mAWS.getPrevAp_distance() + ", disturbing_degree = " + this.mAWS.getPrevDisturbing_degree() + ", lost_beacon_amount = " + this.mAWS.getPrevLost_beacon_amount());
                                }
                                if (HWFLOW) {
                                    Log.d(TAG, "WIFI_ACCESS_INTERNET_FAILED, monitor_interval = " + this.mAWS.getMonitor_interval() + ", tx_frame_amount = " + tx_frame_amount + ", tx_byte_amount = " + tx_byte_amount + ", tx_data_frame_err_amount = " + tx_data_frame_err_amount + ", tx_retrans_amount = " + tx_retrans_amount + ", rx_frame_amount = " + rx_frame_amount + ", rx_byte_amount = " + rx_byte_amount + ", rx_beacon_from_assoc_ap = " + rx_beacon_from_assoc_ap + ", ap_distance = " + this.mAWS.getAp_distance() + ", disturbing_degree = " + this.mAWS.getDisturbing_degree() + ", lost_beacon_amount = " + lost_beacon_amount + ", dns = " + this.str_dns);
                                }
                            }
                            this.mUploadAccessWebFailedCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                            this.mLastAccessWebFailedTime = elapsedRealtime;
                            chrLogBaseModel = accessInterModel;
                        }
                    }
                    this.mAccessFailCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                    rstFlg = HWFLOW;
                    break;
                }
                break;
            case HwWifiCHRConstImpl.WIFI_STATUS_CHANGEDBY_APK /*98*/:
                if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mApkChangedCount)) {
                    CSegEVENT_WIFI_STATUS_CHANGEDBY_APK csegEVENT_WIFI_STATUS_CHANGEDBY_APK = new CSegEVENT_WIFI_STATUS_CHANGEDBY_APK();
                    csegEVENT_WIFI_STATUS_CHANGEDBY_APK.tmTimeStamp.setValue(date);
                    csegEVENT_WIFI_STATUS_CHANGEDBY_APK.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    csegEVENT_WIFI_STATUS_CHANGEDBY_APK.ucApkAction.setValue(this.mApkAction);
                    csegEVENT_WIFI_STATUS_CHANGEDBY_APK.strApkName.setValue(this.mLastApkName);
                    csegEVENT_WIFI_STATUS_CHANGEDBY_APK.ucApkChangeTimes.setValue(this.mApkTriggerTimes);
                    csegEVENT_WIFI_STATUS_CHANGEDBY_APK.strSsid.setValue(this.strAp_Ssid);
                    csegEVENT_WIFI_STATUS_CHANGEDBY_APK.strUserAction.setValue(this.mUserAction);
                    chrLogBaseModel = csegEVENT_WIFI_STATUS_CHANGEDBY_APK;
                }
                this.mApkChangedCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                break;
            case MessageUtil.CMD_ON_STOP /*101*/:
                this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                CSegEVENT_WIFI_USER_CONNECT userConnModel = new CSegEVENT_WIFI_USER_CONNECT();
                userConnModel.strAP_MAC.setValue(bssid);
                userConnModel.strSTA_MAC.setValue(this.strSta_mac);
                userConnModel.strAP_SSID.setValue(this.strAp_Ssid);
                userConnModel.tmTimeStamp.setValue(date);
                userConnModel.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                userConnModel.strAP_proto.setValue(this.strAP_proto);
                userConnModel.strAP_key_mgmt.setValue(this.strAP_key_mgmt);
                userConnModel.strAP_auth_alg.setValue(this.strAP_auth_alg);
                userConnModel.strAP_pairwise.setValue(this.strAP_pairwise);
                userConnModel.strAP_group.setValue(this.strAP_gruop);
                userConnModel.strAP_eap.setValue(this.strAP_eap);
                userConnModel.strapVendorInfo.setValue(this.mAPVendorInfo);
                userConnModel.ucBTState.setValue(bluetoothState);
                userConnModel.ucBTConnState.setValue(bluetoothConnState);
                if (this.hwWifiCHRService != null) {
                    try {
                        userConnModel.usSubErrorCode.setValue(Short.parseShort(ucSubErrorCode));
                        this.hwWifiCHRService.fillUserConnectModel(userConnModel);
                        int usrrelatedAps = getRelatedApInfo(bssid);
                        rssiGroupCHR = this.mRssiGroup.getRSSIGroupCHR();
                        userConnModel.ucPublicEss.setValue((byte) (usrrelatedAps >> ANT_SEC_WORK_TIME));
                        for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < rssiGroupCHR.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                            userConnModel.setCSubRSSIGROUP_EVENTList((CSubRSSIGROUP_EVENT) rssiGroupCHR.get(i));
                        }
                        if (commercialUser) {
                            userConnModel.strAP_MAC.setValue(bssidMask);
                        }
                        chrLogBaseModel = userConnModel;
                    } catch (Exception e2) {
                    }
                }
                rstFlg = HWFLOW;
                break;
            case MessageUtil.CMD_START_SCAN /*102*/:
                if (this.mContextRef != null) {
                    if (isNeedCreateAccessWebSlow(elapsedRealtime, commercialUser)) {
                        if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mAccessWebSlowCount)) {
                            BtStatus = HwCHRWifiRelatedStateMonitor.make(this.mContextRef).getBTStateCHR();
                            CellID = HwCHRWifiRelatedStateMonitor.make(this.mContextRef).getCellIDCHR();
                            netCfg = HwCHRWifiRelatedStateMonitor.make(this.mContextRef).getSSIDSetting();
                            traffic_Group = HWNetstatManager.getBack_Front_Summery();
                            netCfg.iIp_Type.setValue(this.mIpType);
                            mem = HwCHRWebMonitor.getMemCHR();
                            cpu = HwCHRWebMonitor.getCpuCHR();
                            dns = HwCHRWebMonitor.getDNSCHR();
                            CSubPacketCount pkt = HwCHRWebMonitor.getPacketCountCHR();
                            tcpStat = HwCHRWebSpeed.getDefault().getTcpStatistCHR();
                            iIsMobileAP = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                            if (this.mContextRef != null) {
                                iIsMobileAP = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContextRef) ? WIFI_SETTING_OPENED_AND_SERVICE_CLOSED : WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                            }
                            relatedAps = getRelatedApInfo(bssid);
                            rssiGroupCHR2 = this.mRssiGroup.getRSSIGroupEXCHR();
                            essCount = (byte) (relatedAps >> ANT_SEC_WORK_TIME);
                            ApRoaming = getApRoaming(this.strAp_Ssid, this.mCurrentApBssid);
                            CSegEVENT_WIFI_ACCESS_WEB_SLOWLY accessWebModel = new CSegEVENT_WIFI_ACCESS_WEB_SLOWLY();
                            accessWebModel.enucHwStatus.setValue("READY");
                            accessWebModel.strWIFI_IP.setValue(this.str_Wifi_ip);
                            accessWebModel.iAP_RSSI.setValue(this.mRssi);
                            accessWebModel.strWIFI_GATE.setValue(this.str_gate_ip);
                            accessWebModel.strIP_LEASETIME.setValue(this.strIp_leasetime);
                            accessWebModel.strDNS_ADDRESS.setValue(this.str_dns);
                            accessWebModel.strAP_MAC.setValue(bssidMask);
                            accessWebModel.strSTA_MAC.setValue(this.strSta_mac);
                            accessWebModel.strAP_SSID.setValue(this.strAp_Ssid);
                            accessWebModel.strRoutes.setValue(this.strRoutes);
                            accessWebModel.tmTimeStamp.setValue(date);
                            accessWebModel.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                            accessWebModel.usAP_link_speed.setValue(this.usLinkSpeed);
                            accessWebModel.usAP_channel.setValue(this.usAP_channel);
                            accessWebModel.strAP_proto.setValue(this.strAP_proto);
                            accessWebModel.strAP_key_mgmt.setValue(this.strAP_key_mgmt);
                            accessWebModel.strAP_auth_alg.setValue(this.strAP_auth_alg);
                            accessWebModel.strAP_pairwise.setValue(this.strAP_pairwise);
                            accessWebModel.strAP_group.setValue(this.strAP_gruop);
                            accessWebModel.strAP_eap.setValue(this.strAP_eap);
                            accessWebModel.usSubErrorCode.setValue(this.accessSlowlyerrrCode);
                            accessWebModel.strapVendorInfo.setValue(this.mAPVendorInfo);
                            accessWebModel.ucPublicEss.setValue(essCount);
                            accessWebModel.ucIsOnScreen.setValue(this.mScreenState);
                            accessWebModel.strInfo.setValue(this.strSpeedInfo);
                            accessWebModel.strUIDInfo.setValue(this.strUIDSpeedInfo);
                            accessWebModel.ucFailReason.setValue((byte) this.mFailReason);
                            accessWebModel.setCSubCellID(CellID);
                            accessWebModel.setCSubNET_CFG(netCfg);
                            accessWebModel.setCSubTRAFFIC_GROUND(traffic_Group);
                            accessWebModel.setCSubBTStatus(BtStatus);
                            accessWebModel.setCSubCPUInfo(cpu);
                            accessWebModel.setCSubMemInfo(mem);
                            accessWebModel.setCSubDNS(dns);
                            accessWebModel.ucisPortal.setValue(this.mIsPortalConnection);
                            accessWebModel.setCSubPacketCount(pkt);
                            accessWebModel.strCountryCode.setValue(this.mCountryCode);
                            accessWebModel.setCSubApRoaming(ApRoaming);
                            accessWebModel.ucPortalStatus.setValue(this.mPortalStatus);
                            accessWebModel.ucGWCount.setValue(this.mMultiGWCount);
                            for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < rssiGroupCHR2.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                                accessWebModel.setCSubRSSIGROUP_EVENT_EXList((CSubRSSIGROUP_EVENT_EX) rssiGroupCHR2.get(i));
                            }
                            accessWebModel.setCSubWL_COUNTERS(this.mCounter_monitor.getCounterLst().getWLCountersCHR());
                            accessWebModel.setCSubTCP_STATIST(tcpStat);
                            if (this.hwWifiCHRService != null) {
                                accessWebModel.ucScanAlwaysAvailble.setValue(this.hwWifiCHRService.getPersistedScanAlwaysAvailable());
                                accessWebModel.ucWIFIAlwaysNotifation.setValue(this.hwWifiCHRService.getWIFINetworkAvailableNotificationON());
                                accessWebModel.ucWIFISleepPolicy.setValue(this.hwWifiCHRService.getWIFISleepPolicy());
                                accessWebModel.ucWifiProStatus.setValue(this.hwWifiCHRService.getWIFIProStatus());
                                accessWebModel.ucWifiToPDP.setValue(this.hwWifiCHRService.getWIFITOPDP());
                            }
                            accessWebModel.iTime_NetSlowlyTime.setValue(this.mNetSlowlyTime);
                            accessWebModel.iTime_NetNormalTime.setValue(this.mNetNormalTime);
                            accessWebModel.ucIsMobleAP.setValue(iIsMobileAP);
                            if (HWFLOW) {
                                Log.d(TAG, "NetNormalTime = " + this.mNetNormalTime + ", NetSlowlyTime = " + this.mNetSlowlyTime + ", iIsMobileAP = " + iIsMobileAP);
                            }
                            accessWebModel.enWifiAccessWebSlowlyReason.setValue(ucSubErrorCode);
                            if (this.mAWS != null) {
                                accessWebModel.strIP_public.setValue(this.mAWS.getChinazIp() + HwCHRWifiCPUUsage.COL_SEP + this.mAWS.getChinazAddr());
                                accessWebModel.isdio_info_readbreq.setValue(this.mAWS.get_sdio_info_readbreq());
                                accessWebModel.isdio_info_readb.setValue(this.mAWS.get_sdio_info_readb());
                                accessWebModel.isdio_info_writebreq.setValue(this.mAWS.get_sdio_info_writebreq());
                                accessWebModel.isdio_info_writeb.setValue(this.mAWS.get_sdio_info_writeb());
                                accessWebModel.isdio_info_readwreq.setValue(this.mAWS.get_sdio_info_readwreq());
                                accessWebModel.isdio_info_readw.setValue(this.mAWS.get_sdio_info_readw());
                                accessWebModel.isdio_info_writewreq.setValue(this.mAWS.get_sdio_info_writewreq());
                                accessWebModel.isdio_info_writew.setValue(this.mAWS.get_sdio_info_writew());
                                accessWebModel.isdio_info_ksosetreq.setValue(this.mAWS.get_sdio_info_ksosetreq());
                                accessWebModel.isdio_info_ksosetretry.setValue(this.mAWS.get_sdio_info_ksosetretry());
                                accessWebModel.isdio_info_ksoclrreq.setValue(this.mAWS.get_sdio_info_ksoclrreq());
                                accessWebModel.isdio_info_ksoclrretry.setValue(this.mAWS.get_sdio_info_ksoclrretry());
                                accessWebModel.idetect_RTT_arp.setValue(this.mAWS.getRTTArp());
                                accessWebModel.idetect_RTT_baidu.setValue(this.mAWS.getRTTBaidu());
                                accessWebModel.uctraffic_aftersuspend.setValue(this.mAWS.getRxSleepCnt());
                                tx_frame_amount = this.mAWS.getTx_frame_amount() - this.mAWS.getPrevTx_frame_amount();
                                tx_byte_amount = this.mAWS.getTx_byte_amount() - this.mAWS.getPrevTx_byte_amount();
                                tx_data_frame_err_amount = this.mAWS.getTx_data_frame_error_amount() - this.mAWS.getPrevTx_data_frame_error_amount();
                                tx_retrans_amount = this.mAWS.getTx_retrans_amount() - this.mAWS.getPrevTx_retrans_amount();
                                rx_frame_amount = this.mAWS.getRx_frame_amount() - this.mAWS.getPrevRx_frame_amount();
                                rx_byte_amount = this.mAWS.getRx_byte_amount() - this.mAWS.getPrevRx_byte_amount();
                                rx_beacon_from_assoc_ap = this.mAWS.getRx_beacon_from_assoc_ap() - this.mAWS.getPrevRx_beacon_from_assoc_ap();
                                lost_beacon_amount = this.mAWS.getLost_beacon_amount() - this.mAWS.getPrevLost_beacon_amount();
                                if (tx_frame_amount < 0) {
                                    tx_frame_amount += Integer.MAX_VALUE;
                                }
                                if (tx_byte_amount < 0) {
                                    tx_byte_amount += Integer.MAX_VALUE;
                                }
                                if (tx_data_frame_err_amount < 0) {
                                    tx_data_frame_err_amount += Integer.MAX_VALUE;
                                }
                                if (tx_retrans_amount < 0) {
                                    tx_retrans_amount += Integer.MAX_VALUE;
                                }
                                if (rx_frame_amount < 0) {
                                    rx_frame_amount += Integer.MAX_VALUE;
                                }
                                if (rx_byte_amount < 0) {
                                    rx_byte_amount += Integer.MAX_VALUE;
                                }
                                if (rx_beacon_from_assoc_ap < 0) {
                                    rx_beacon_from_assoc_ap += Integer.MAX_VALUE;
                                }
                                if (lost_beacon_amount < 0) {
                                    lost_beacon_amount += Integer.MAX_VALUE;
                                }
                                accessWebModel.imonitor_interval.setValue(this.mAWS.getMonitor_interval());
                                accessWebModel.itx_frame_amount.setValue(tx_frame_amount);
                                accessWebModel.itx_byte_amount.setValue(tx_byte_amount);
                                accessWebModel.itx_data_frame_err_amount.setValue(tx_data_frame_err_amount);
                                accessWebModel.itx_retrans_amount.setValue(tx_retrans_amount);
                                accessWebModel.irx_frame_amount.setValue(rx_frame_amount);
                                accessWebModel.irx_byte_amount.setValue(rx_byte_amount);
                                accessWebModel.irx_beacon_from_assoc_ap.setValue(rx_beacon_from_assoc_ap);
                                accessWebModel.ucap_distance.setValue(this.mAWS.getAp_distance());
                                accessWebModel.ucdisturbing_degree.setValue(this.mAWS.getDisturbing_degree());
                                accessWebModel.ilost_beacon_amount.setValue(lost_beacon_amount);
                                if (HWFLOW) {
                                    Log.d(TAG, "WIFI_ACCESS_WEB_SLOWLY prev, monitor_interval = " + this.mAWS.getMonitor_interval() + ", tx_frame_amount = " + this.mAWS.getPrevTx_frame_amount() + ", tx_byte_amount = " + this.mAWS.getPrevTx_byte_amount() + ", tx_data_frame_err_amount = " + this.mAWS.getPrevTx_data_frame_error_amount() + ", tx_retrans_amount = " + this.mAWS.getPrevTx_retrans_amount() + ", rx_frame_amount = " + this.mAWS.getPrevRx_frame_amount() + ", rx_byte_amount = " + this.mAWS.getPrevRx_byte_amount() + ", rx_beacon_from_assoc_ap = " + this.mAWS.getPrevRx_beacon_from_assoc_ap() + ", ap_distance = " + this.mAWS.getPrevAp_distance() + ", disturbing_degree = " + this.mAWS.getPrevDisturbing_degree() + ", lost_beacon_amount = " + this.mAWS.getPrevLost_beacon_amount());
                                }
                                if (HWFLOW) {
                                    Log.d(TAG, "WIFI_ACCESS_WEB_SLOWLY, monitor_interval = " + this.mAWS.getMonitor_interval() + ", tx_frame_amount = " + tx_frame_amount + ", tx_byte_amount = " + tx_byte_amount + ", tx_data_frame_err_amount = " + tx_data_frame_err_amount + ", tx_retrans_amount = " + tx_retrans_amount + ", rx_frame_amount = " + rx_frame_amount + ", rx_byte_amount = " + rx_byte_amount + ", rx_beacon_from_assoc_ap = " + rx_beacon_from_assoc_ap + ", ap_distance = " + this.mAWS.getAp_distance() + ", disturbing_degree = " + this.mAWS.getDisturbing_degree() + ", lost_beacon_amount = " + lost_beacon_amount);
                                }
                            }
                            String repeaterValue = "";
                            accessWebModel.iRepeterFreq.setValue(getRepeaterFreq());
                            int repeaterStatus = getRepeaterStatus();
                            if (getRepeaterStatus() == WIFI_REPEATER_CLOSED) {
                                repeaterValue = "CLOSED";
                            } else if (getRepeaterStatus() == WIFI_REPEATER_OPENED) {
                                repeaterValue = "OPENED";
                            } else if (getRepeaterStatus() == WIFI_REPEATER_TETHER) {
                                repeaterValue = "WORKING";
                            }
                            accessWebModel.enWIFI_REPEATER_STATUS.setValue(repeaterValue);
                            if (HWFLOW) {
                                Log.d(TAG, "WIFI_ACCESS_WEB_SLOWLY WifiRepeater status = " + repeaterValue + "WifiRepeater freq = " + accessWebModel.iRepeterFreq.getValue());
                            }
                            this.mLastAccessWebSlowTime = elapsedRealtime;
                            this.mAccessWebSlowCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                            this.mUploadAccessWebSlowly += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                            this.mUploadAccessWebSlowlySSID += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                            chrLogBaseModel = accessWebModel;
                        }
                    }
                    rstFlg = HWFLOW;
                    break;
                }
                break;
            case HwDualBandMessageUtil.CMD_STOP_MONITOR /*103*/:
                int i2;
                CSegEVENT_WIFI_POOR_LEVEL segWifipoorLevel = new CSegEVENT_WIFI_POOR_LEVEL();
                segWifipoorLevel.tmTimeStamp.setValue(date);
                segWifipoorLevel.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                segWifipoorLevel.ucRssi2gMaxRssi.setValue(this.mRssi2gMaxRssi);
                segWifipoorLevel.ucRssi2gMinRssi.setValue(this.mRssi2gMinRssi);
                segWifipoorLevel.ucRssi2g0LevelAvg.setValue(this.mRssi2gCnt[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED] == 0 ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mRssi2gSum[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED] / this.mRssi2gCnt[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED]);
                segWifipoorLevel.ucRssi2g1LevelAvg.setValue(this.mRssi2gCnt[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED] == 0 ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mRssi2gSum[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED] / this.mRssi2gCnt[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED]);
                segWifipoorLevel.ucRssi2g2LevelAvg.setValue(this.mRssi2gCnt[WIFI_SETTING_OPENED_TIMEOUT] == 0 ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mRssi2gSum[WIFI_SETTING_OPENED_TIMEOUT] / this.mRssi2gCnt[WIFI_SETTING_OPENED_TIMEOUT]);
                segWifipoorLevel.ucRssi2g3LevelAvg.setValue(this.mRssi2gCnt[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE] == 0 ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mRssi2gSum[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE] / this.mRssi2gCnt[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE]);
                segWifipoorLevel.ucRssi2g4LevelAvg.setValue(this.mRssi2gCnt[SECURITY_WAPI_PSK] == 0 ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mRssi2gSum[SECURITY_WAPI_PSK] / this.mRssi2gCnt[SECURITY_WAPI_PSK]);
                segWifipoorLevel.iRssi2g0LevelCnt.setValue(this.mRssi2gCnt[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED]);
                segWifipoorLevel.iRssi2g1LevelCnt.setValue(this.mRssi2gCnt[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED]);
                segWifipoorLevel.iRssi2g2LevelCnt.setValue(this.mRssi2gCnt[WIFI_SETTING_OPENED_TIMEOUT]);
                segWifipoorLevel.iRssi2g3LevelCnt.setValue(this.mRssi2gCnt[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE]);
                segWifipoorLevel.iRssi2g4LevelCnt.setValue(this.mRssi2gCnt[SECURITY_WAPI_PSK]);
                segWifipoorLevel.ucRssi5gMaxRssi.setValue(this.mRssi5gMaxRssi);
                segWifipoorLevel.ucRssi5gMinRssi.setValue(this.mRssi5gMinRssi);
                segWifipoorLevel.ucRssi5g0LevelAvg.setValue(this.mRssi5gCnt[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED] == 0 ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mRssi5gSum[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED] / this.mRssi5gCnt[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED]);
                segWifipoorLevel.ucRssi5g1LevelAvg.setValue(this.mRssi5gCnt[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED] == 0 ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mRssi5gSum[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED] / this.mRssi5gCnt[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED]);
                segWifipoorLevel.ucRssi5g2LevelAvg.setValue(this.mRssi5gCnt[WIFI_SETTING_OPENED_TIMEOUT] == 0 ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mRssi5gSum[WIFI_SETTING_OPENED_TIMEOUT] / this.mRssi5gCnt[WIFI_SETTING_OPENED_TIMEOUT]);
                segWifipoorLevel.ucRssi5g3LevelAvg.setValue(this.mRssi5gCnt[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE] == 0 ? WIFI_SETTING_CLOSED_AND_SERVICE_OPENED : this.mRssi5gSum[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE] / this.mRssi5gCnt[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE]);
                LogByte logByte = segWifipoorLevel.ucRssi5g4LevelAvg;
                if (this.mRssi5gCnt[SECURITY_WAPI_PSK] == 0) {
                    i2 = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                } else {
                    i2 = this.mRssi5gSum[SECURITY_WAPI_PSK] / this.mRssi5gCnt[SECURITY_WAPI_PSK];
                }
                logByte.setValue(i2);
                segWifipoorLevel.iRssi5g0LevelCnt.setValue(this.mRssi5gCnt[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED]);
                segWifipoorLevel.iRssi5g1LevelCnt.setValue(this.mRssi5gCnt[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED]);
                segWifipoorLevel.iRssi5g2LevelCnt.setValue(this.mRssi5gCnt[WIFI_SETTING_OPENED_TIMEOUT]);
                segWifipoorLevel.iRssi5g3LevelCnt.setValue(this.mRssi5gCnt[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE]);
                segWifipoorLevel.iRssi5g4LevelCnt.setValue(this.mRssi5gCnt[SECURITY_WAPI_PSK]);
                chrLogBaseModel = segWifipoorLevel;
                break;
            case HwWifiCHRConstImpl.WIFI_STABILITY_STAT /*110*/:
                CSegEVENT_WIFI_STABILITY_STAT stability = null;
                if (this.mHwWifiStatStore != null) {
                    List<ChrLogBaseModel> lstModel = this.mHwWifiStatStore.getWifiStatModel(date);
                    for (ChrLogBaseModel model : lstModel) {
                        if (model instanceof CSegEVENT_WIFI_STABILITY_STAT) {
                            stability = (CSegEVENT_WIFI_STABILITY_STAT) model;
                        }
                    }
                    if (stability != null) {
                        stability.ucRepeterClientMaxCount.setValue(this.mRepeterMaxClientCount);
                        stability.iRepeterConnFailedCount.setValue(this.mRepeterConnFailedCount);
                        stability.iRepeterOpenSuccCount.setValue(this.mWifiRepeaterOpenedCount);
                        stability.iRepeterDiffFreqWorkDuration.setValue(this.mDiffFreqStationRepeaterDuration);
                        stability.iRepeaterWorkDuration.setValue(Integer.parseInt(String.valueOf(this.mWifiRepeaterWorkingTime)));
                        if (HWFLOW) {
                            Log.d(TAG, "CSegEVENT_WIFI_STABILITY_STAT WifiRepeater mRepeterMaxClientCount = " + this.mRepeterMaxClientCount + ", mRepeterConnFailedCount = " + this.mRepeterConnFailedCount + ", mWifiRepeaterOpenedCount = " + this.mWifiRepeaterOpenedCount + ", mDiffFreqStationRepeaterDuration = " + this.mDiffFreqStationRepeaterDuration + ", mDiffFreqStationRepeaterDuration = " + this.mDiffFreqStationRepeaterDuration);
                        }
                        result.addAll(lstModel);
                        break;
                    }
                }
                break;
            case 113:
                if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mWorkaroundCount)) {
                    CSegEVENT_WIFI_WORKAROUND_STAT workaroundModel = new CSegEVENT_WIFI_WORKAROUND_STAT();
                    workaroundModel.strAP_MAC.setValue(bssidMask);
                    workaroundModel.strAP_SSID.setValue(this.strAp_Ssid);
                    workaroundModel.tmTimeStamp.setValue(date);
                    workaroundModel.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    workaroundModel.usSubErrorCode.setValue(this.mWorkaroundCode);
                    workaroundModel.usWork_Ret.setValue(this.mWorkaroundRet);
                    workaroundModel.usWork_Status.setValue(this.mWorkaroundStatus);
                    workaroundModel.strRemark.setValue(this.mWorkaroundRemark == null ? "" : this.mWorkaroundRemark);
                    chrLogBaseModel = workaroundModel;
                }
                this.mWorkaroundCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                break;
            case HwWifiCHRConstImpl.WIFI_PORTAL_SAMPLES_COLLECTE /*120*/:
                Log.d(TAG, "send WIFI_PORTAL_SAMPLES_COLLECTE CHR event.");
                CSegEVENT_WIFI_PORTAL_SAMPLES_COLLECTE wifiPortalSample = new CSegEVENT_WIFI_PORTAL_SAMPLES_COLLECTE();
                wifiPortalSample.tmTimeStamp.setValue(date);
                wifiPortalSample.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                wifiPortalSample.ucAPSsid_len.setValue(this.mSsid_Len);
                wifiPortalSample.aucAPSsid.setValue(this.mPortalAPSsid);
                wifiPortalSample.strAPBssid.setValue(this.mPortalAPBssid);
                wifiPortalSample.strAP_URL.setValue(this.mWebUrl);
                wifiPortalSample.strLOC_CellID.setValue(this.mPortalCellId);
                wifiPortalSample.strPhone_Numbe_Input_ID.setValue(this.mPhoneInputId);
                wifiPortalSample.strHTML_Send_Button_ID.setValue(this.mSndBtnId);
                wifiPortalSample.strHTML_Code_Input_ID.setValue(this.mCodeInputId);
                wifiPortalSample.strHTML_Login_Button_ID.setValue(this.mSubmitBtnId);
                wifiPortalSample.ucHTML_Input_Number.setValue(this.mHTML_Input_Number);
                wifiPortalSample.aucWebContent.setValue(this.mPortalAPKeyLines);
                chrLogBaseModel = wifiPortalSample;
                rstFlg = HWFLOW;
                break;
            case HwWifiCHRConstImpl.WIFI_WIFIPRO_STATISTICS_EVENT /*121*/:
                Log.d(TAG, "send WIFI_WIFIPRO_STATISTICS_EVENT CHR event.");
                CSegEVENT_WIFI_WIFIPRO_STATISTICS_EVENT statisticsEvent = new CSegEVENT_WIFI_WIFIPRO_STATISTICS_EVENT();
                statisticsEvent.tmTimeStamp.setValue(date);
                statisticsEvent.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                statisticsEvent.iStatIntervalTime.setValue(this.mStatIntervalTime);
                statisticsEvent.iEnableTotTime.setValue(this.mEnableTotTime);
                statisticsEvent.usNoInetHandoverCount.setValue(this.mNoInetHandoverCount);
                statisticsEvent.usPortalUnauthCount.setValue(this.mPortalUnauthCount);
                statisticsEvent.usWifiScoCount.setValue(this.mWifiScoCount);
                statisticsEvent.usPortalCodeParseCount.setValue(this.mPortalCodeParseCount);
                statisticsEvent.usRcvSMS_Count.setValue(this.mRcvSMS_Count);
                statisticsEvent.usPortalAutoLoginCount.setValue(this.mPortalAutoLoginCount);
                statisticsEvent.usCellAutoOpenCount.setValue(this.mCellAutoOpenCount);
                statisticsEvent.usCellAutoCloseCount.setValue(this.mCellAutoCloseCount);
                statisticsEvent.usTotalBQE_BadROC.setValue(this.mTotalBQE_BadROC);
                statisticsEvent.usManualBackROC.setValue(this.mManualBackROC);
                statisticsEvent.usRSSI_RO_Tot.setValue(this.mRSSI_RO_Tot);
                statisticsEvent.usRSSI_ErrRO_Tot.setValue(this.mRSSI_ErrRO_Tot);
                statisticsEvent.usOTA_RO_Tot.setValue(this.mOTA_RO_Tot);
                statisticsEvent.usOTA_ErrRO_Tot.setValue(this.mOTA_ErrRO_Tot);
                statisticsEvent.usTCP_RO_Tot.setValue(this.mTCP_RO_Tot);
                statisticsEvent.usTCP_ErrRO_Tot.setValue(this.mTCP_ErrRO_Tot);
                statisticsEvent.iManualRI_TotTime.setValue(this.mManualRI_TotTime);
                statisticsEvent.iAutoRI_TotTime.setValue(this.mAutoRI_TotTime);
                statisticsEvent.usAutoRI_TotCount.setValue(this.mAutoRI_TotCount);
                statisticsEvent.usRSSI_RestoreRI_Count.setValue(this.mRSSI_RestoreRI_Count);
                statisticsEvent.usRSSI_BetterRI_Count.setValue(this.mRSSI_BetterRI_Count);
                statisticsEvent.usTimerRI_Count.setValue(this.mTimerRI_Count);
                statisticsEvent.usHisScoRI_Count.setValue(this.mHisScoRI_Count);
                statisticsEvent.usUserCancelROC.setValue(this.mUserCancelROC);
                statisticsEvent.usWifiToWifiSuccCount.setValue(this.mWifiToWifiSuccCount);
                statisticsEvent.usNoInetAlarmCount.setValue(this.mNoInetAlarmCount);
                statisticsEvent.usWifiOobInitState.setValue(this.mWifiOobInitState);
                statisticsEvent.usNotAutoConnPortalCnt.setValue(this.mNotAutoConnPortalCnt);
                statisticsEvent.usHighDataRateStopROC.setValue(this.mHighDataRateStopROC);
                statisticsEvent.usSelectNotInetAPCount.setValue(this.mSelectNotInetAPCount);
                statisticsEvent.usUserUseBgScanAPCount.setValue(this.mUserUseBgScanAPCount);
                statisticsEvent.usPingPongCount.setValue(this.mPingPongCount);
                statisticsEvent.usBQE_BadSettingCancel.setValue(this.mBQE_BadSettingCancel);
                statisticsEvent.usNotInetSettingCancel.setValue(this.mNotInetSettingCancel);
                statisticsEvent.usNotInetUserCancel.setValue(this.mNotInetUserCancel);
                statisticsEvent.usNotInetRestoreRI.setValue(this.mNotInetRestoreRI);
                statisticsEvent.usNotInetUserManualRI.setValue(this.mNotInetUserManualRI);
                statisticsEvent.usNotInetWifiToWifiCount.setValue(this.mNotInetWifiToWifiCount);
                statisticsEvent.usReopenWifiRICount.setValue(this.mReopenWifiRICount);
                statisticsEvent.usSelCSPShowDiglogCount.setValue(this.mSelCSPShowDiglogCount);
                statisticsEvent.usSelCSPAutoSwCount.setValue(this.mSelCSPAutoSwCount);
                statisticsEvent.usSelCSPNotSwCount.setValue(this.mSelCSPNotSwCount);
                statisticsEvent.usTotBtnRICount.setValue(this.mTotBtnRICount);
                statisticsEvent.usBMD_TenMNotifyCount.setValue(this.mBMD_TenMNotifyCount);
                statisticsEvent.usBMD_TenM_RI_Count.setValue(this.mBMD_TenM_RI_Count);
                statisticsEvent.usBMD_FiftyMNotifyCount.setValue(this.mBMD_FiftyMNotifyCount);
                statisticsEvent.usBMD_FiftyM_RI_Count.setValue(this.mBMD_FiftyM_RI_Count);
                statisticsEvent.usBMD_UserDelNotifyCount.setValue(this.mBMD_UserDelNotifyCount);
                statisticsEvent.iRO_TotMobileData.setValue(this.mRO_TotMobileData);
                statisticsEvent.usAF_PhoneNumSuccCnt.setValue(this.mAF_PhoneNumSuccCnt);
                statisticsEvent.usAF_PhoneNumFailCnt.setValue(this.mAF_PhoneNumFailCnt);
                statisticsEvent.usAF_PasswordSuccCnt.setValue(this.mAF_PasswordSuccCnt);
                statisticsEvent.usAF_PasswordFailCnt.setValue(this.mAF_PasswordFailCnt);
                statisticsEvent.usAF_AutoLoginSuccCnt.setValue(this.mAF_AutoLoginSuccCnt);
                statisticsEvent.usAF_AutoLoginFailCnt.setValue(this.mAF_AutoLoginFailCnt);
                statisticsEvent.usAF_FPNSuccNotMsmCnt.setValue(this.mAF_FPNSuccNotMsmCnt);
                statisticsEvent.usBSG_RsGoodCnt.setValue(this.mBSG_RsGoodCnt);
                statisticsEvent.usBSG_RsMidCnt.setValue(this.mBSG_RsMidCnt);
                statisticsEvent.usBSG_RsBadCnt.setValue(this.mBSG_RsBadCnt);
                statisticsEvent.usBSG_EndIn4sCnt.setValue(this.mBSG_EndIn4sCnt);
                statisticsEvent.usBSG_EndIn4s7sCnt.setValue(this.mBSG_EndIn4s7sCnt);
                statisticsEvent.usBSG_NotEndIn7sCnt.setValue(this.mBSG_NotEndIn7sCnt);
                statisticsEvent.usBG_BgRunCnt.setValue(this.mBG_BgRunCnt);
                statisticsEvent.usBG_SettingRunCnt.setValue(this.mBG_SettingRunCnt);
                statisticsEvent.usBG_FreeInetOkApCnt.setValue(this.mBG_FreeInetOkApCnt);
                statisticsEvent.usBG_FishingApCnt.setValue(this.mBG_FishingApCnt);
                statisticsEvent.usBG_FreeNotInetApCnt.setValue(this.mBG_FreeNotInetApCnt);
                statisticsEvent.usBG_PortalApCnt.setValue(this.mBG_PortalApCnt);
                statisticsEvent.usBG_UserSelFreeInetOkCnt.setValue(this.mBG_UserSelFreeInetOkCnt);
                statisticsEvent.usBG_UserSelNoInetCnt.setValue(this.mBG_UserSelNoInetCnt);
                statisticsEvent.usBG_UserSelPortalCnt.setValue(this.mBG_UserSelPortalCnt);
                statisticsEvent.usBG_FoundTwoMoreApCnt.setValue(this.mBG_FoundTwoMoreApCnt);
                statisticsEvent.usBG_FailedCnt.setValue(this.mBG_FailedCnt);
                statisticsEvent.usBG_InetNotOkActiveOk.setValue(this.mBG_InetNotOkActiveOk);
                statisticsEvent.usBG_InetOkActiveNotOk.setValue(this.mBG_InetOkActiveNotOk);
                statisticsEvent.usBG_UserSelApFishingCnt.setValue(this.mBG_UserSelApFishingCnt);
                statisticsEvent.usBG_ConntTimeoutCnt.setValue(this.mBG_ConntTimeoutCnt);
                statisticsEvent.usBG_DNSFailCnt.setValue(this.mBG_DNSFailCnt);
                statisticsEvent.usBG_DHCPFailCnt.setValue(this.mBG_DHCPFailCnt);
                statisticsEvent.usBG_AUTH_FailCnt.setValue(this.mBG_AUTH_FailCnt);
                statisticsEvent.usBG_AssocRejectCnt.setValue(this.mBG_AssocRejectCnt);
                statisticsEvent.usBG_NCByConnectFail.setValue(this.mBG_NCByConnectFail);
                statisticsEvent.usBG_NCByCheckFail.setValue(this.mBG_NCByCheckFail);
                statisticsEvent.usBG_NCByStateErr.setValue(this.mBG_NCByStateErr);
                statisticsEvent.usBG_NCByUnknown.setValue(this.mBG_NCByUnknown);
                statisticsEvent.usBQE_CNUrl1FailCount.setValue(this.mBQE_CNUrl1FailCount);
                statisticsEvent.usBQE_CNUrl2FailCount.setValue(this.mBQE_CNUrl2FailCount);
                statisticsEvent.usBQE_CNUrl3FailCount.setValue(this.mBQE_CNUrl3FailCount);
                statisticsEvent.usBQE_NCNUrl1FailCount.setValue(this.mBQE_NCNUrl1FailCount);
                statisticsEvent.usBQE_NCNUrl2FailCount.setValue(this.mBQE_NCNUrl2FailCount);
                statisticsEvent.usBQE_NCNUrl3FailCount.setValue(this.mBQE_NCNUrl3FailCount);
                statisticsEvent.usBQE_ScoreUnknownCount.setValue(this.mBQE_ScoreUnknownCount);
                statisticsEvent.usBQE_BindWlanFailCount.setValue(this.mBQE_BindWlanFailCount);
                statisticsEvent.usBQE_StopBqeFailCount.setValue(this.mBQE_StopBqeFailCount);
                statisticsEvent.iQOE_AutoRI_TotData.setValue(this.mQOE_AutoRI_TotData);
                statisticsEvent.iNotInet_AutoRI_TotData.setValue(this.mNotInet_AutoRI_TotData);
                statisticsEvent.usQOE_RO_DISCONNECT_Cnt.setValue(this.mQOE_RO_DISCONNECT_Cnt);
                statisticsEvent.iQOE_RO_DISCONNECT_TotData.setValue(this.mQOE_RO_DISCONNECT_TotData);
                statisticsEvent.usNotInetRO_DISCONNECT_Cnt.setValue(this.mNotInetRO_DISCONNECT_Cnt);
                statisticsEvent.iNotInetRO_DISCONNECT_TotData.setValue(this.mNotInetRO_DISCONNECT_TotData);
                statisticsEvent.iTotWifiConnectTime.setValue(this.mTotWifiConnectTime);
                statisticsEvent.usActiveCheckRS_Diff.setValue(this.mActiveCheckRS_Diff);
                statisticsEvent.usNoInetAlarmOnConnCnt.setValue(this.mNoInetAlarmOnConnCnt);
                statisticsEvent.usPortalNoAutoConnCnt.setValue(this.mPortalNoAutoConnCnt);
                statisticsEvent.usHomeAPAddRoPeriodCnt.setValue(this.mHomeAPAddRoPeriodCnt);
                statisticsEvent.usHomeAPQoeBadCnt.setValue(this.mHomeAPQoeBadCnt);
                statisticsEvent.iHistoryTotWifiConnHour.setValue(this.mHistoryTotWifiConnHour);
                statisticsEvent.usTotAPRecordCnt.setValue(this.mTotAPRecordCnt);
                statisticsEvent.usTotHomeAPCnt.setValue(this.mTotHomeAPCnt);
                statisticsEvent.usBigRTT_RO_Tot.setValue(this.mBigRTT_RO_Tot);
                statisticsEvent.usBigRTT_ErrRO_Tot.setValue(this.mBigRTT_ErrRO_Tot);
                statisticsEvent.usTotalPortalConnCount.setValue(this.mTotalPortalConnCount);
                statisticsEvent.usTotalPortalAuthSuccCount.setValue(this.mTotalPortalAuthSuccCount);
                statisticsEvent.ucManualConnBlockPortalCount.setValue(this.mManualConnBlockPortalCount);
                statisticsEvent.ucWifiproStateAtReportTime.setValue(this.mWifiproStateAtReportTime);
                statisticsEvent.ucWifiproOpenCount.setValue(this.mWifiproOpenCount);
                statisticsEvent.ucWifiproCloseCount.setValue(this.mWifiproCloseCount);
                statisticsEvent.usActiveCheckRS_Same.setValue(this.mActiveCheckRS_Same);
                statisticsEvent.usSingleAP_LearnedCount.setValue(this.mSingleAP_LearnedCount);
                statisticsEvent.usSingleAP_NearbyCount.setValue(this.mSingleAP_NearbyCount);
                statisticsEvent.usSingleAP_MonitorCount.setValue(this.mSingleAP_MonitorCount);
                statisticsEvent.usSingleAP_SatisfiedCount.setValue(this.mSingleAP_SatisfiedCount);
                statisticsEvent.usSingleAP_DisapperCount.setValue(this.mSingleAP_DisapperCount);
                statisticsEvent.usSingleAP_InblacklistCount.setValue(this.mSingleAP_InblacklistCount);
                statisticsEvent.usSingleAP_ScoreNotSatisfyCount.setValue(this.mSingleAP_ScoreNotSatisfyCount);
                statisticsEvent.usSingleAP_HandoverSucCount.setValue(this.mSingleAP_HandoverSucCount);
                statisticsEvent.usSingleAP_HandoverFailCount.setValue(this.mSingleAP_HandoverFailCount);
                statisticsEvent.usSingleAP_LowFreqScan5GCount.setValue(this.mSingleAP_LowFreqScan5GCount);
                statisticsEvent.usSingleAP_MidFreqScan5GCount.setValue(this.mSingleAP_MidFreqScan5GCount);
                statisticsEvent.usSingleAP_HighFreqScan5GCount.setValue(this.mSingleAP_HighFreqScan5GCount);
                statisticsEvent.usMixedAP_LearnedCount.setValue(this.mMixedAP_LearnedCount);
                statisticsEvent.usMixedAP_NearbyCount.setValue(this.mMixedAP_NearbyCount);
                statisticsEvent.usMixedAP_MonitorCount.setValue(this.mMixedAP_MonitorCount);
                statisticsEvent.usMixedAP_SatisfiedCount.setValue(this.mMixedAP_SatisfiedCount);
                statisticsEvent.usMixedAP_DisapperCount.setValue(this.mMixedAP_DisapperCount);
                statisticsEvent.usMixedAP_InblacklistCount.setValue(this.mMixedAP_InblacklistCount);
                statisticsEvent.usMixedAP_ScoreNotSatisfyCount.setValue(this.mMixedAP_ScoreNotSatisfyCount);
                statisticsEvent.usMixedAP_HandoverSucCount.setValue(this.mMixedAP_HandoverSucCount);
                statisticsEvent.usMixedAP_HandoverFailCount.setValue(this.mMixedAP_HandoverFailCount);
                statisticsEvent.usMixedAP_LowFreqScan5GCount.setValue(this.mMixedAP_LowFreqScan5GCount);
                statisticsEvent.usMixedAP_MidFreqScan5GCount.setValue(this.mMixedAP_MidFreqScan5GCount);
                statisticsEvent.usMixedAP_HighFreqScan5GCount.setValue(this.mMixedAP_HighFreqScan5GCount);
                statisticsEvent.usCustomizedScan_SuccCount.setValue(this.mCustomizedScan_SuccCount);
                statisticsEvent.usCustomizedScan_FailCount.setValue(this.mCustomizedScan_FailCount);
                statisticsEvent.usHandoverToNotInet5GCount.setValue(this.mHandoverToNotInet5GCount);
                statisticsEvent.usHandoverTooSlowCount.setValue(this.mHandoverTooSlowCount);
                statisticsEvent.usHandoverToBad5GCount.setValue(this.mHandoverToBad5GCount);
                statisticsEvent.usUserRejectHandoverCount.setValue(this.mUserRejectHandoverCount);
                statisticsEvent.usHandoverPingpongCount.setValue(this.mHandoverPingpongCount);
                statisticsEvent.enWifiUserType.setValue(getWifiUserTypeStr(commercialUser ? WIFI_SETTING_OPENED_TIMEOUT : WIFI_SETTING_OPENED_AND_SERVICE_CLOSED));
                chrLogBaseModel = statisticsEvent;
                this.wifiproCanotConnectForLongCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                rstFlg = HWFLOW;
                break;
            case HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT /*122*/:
                if (canUploadWifiproEvent(ucSubErrorCode, commercialUser)) {
                    try {
                        Log.d(TAG, "send WIFI_WIFIPRO_EXCEPTION_EVENT CHR event.");
                        CSegEVENT_WIFI_WIFIPRO_EXCEPTION_EVENT exceptionEvent = new CSegEVENT_WIFI_WIFIPRO_EXCEPTION_EVENT();
                        exceptionEvent.tmTimeStamp.setValue(date);
                        exceptionEvent.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                        exceptionEvent.usRSSI_VALUE.setValue(this.mRSSI_VALUE);
                        exceptionEvent.usOTA_PacketDropRate.setValue(this.mOTA_PacketDropRate);
                        exceptionEvent.usRttAvg.setValue(this.mRttAvg);
                        exceptionEvent.usTcpInSegs.setValue(this.mTcpInSegs);
                        exceptionEvent.usTcpOutSegs.setValue(this.mTcpOutSegs);
                        exceptionEvent.usTcpRetransSegs.setValue(this.mTcpRetransSegs);
                        exceptionEvent.usWIFI_NetSpeed.setValue(this.mWIFI_NetSpeed);
                        exceptionEvent.ucIPQLevel.setValue(this.mIPQLevel);
                        exceptionEvent.aucRO_APSsid.setValue(this.mRO_APSsid.getBytes("UTF-8"));
                        exceptionEvent.ucMobileSignalLevel.setValue(this.mMobileSignalLevel);
                        exceptionEvent.enRATType.setValue(getRATTypeNameStr(this.mRATType));
                        exceptionEvent.usHistoryQuilityRO_Rate.setValue(this.mHistoryQuilityRO_Rate);
                        exceptionEvent.usHighDataRateRO_Rate.setValue(this.mHighDataRateRO_Rate);
                        exceptionEvent.usCreditScoreRO_Rate.setValue(this.mCreditScoreRO_Rate);
                        exceptionEvent.aucAPSsid.setValue(this.mAPSsid.getBytes("UTF-8"));
                        exceptionEvent.strAPBssid.setValue(this.mAPBssid);
                        exceptionEvent.usRO_Duration.setValue(this.mRO_Duration);
                        exceptionEvent.usAutoOpenWhiteNum.setValue(this.mAutoOpenWhiteNum);
                        exceptionEvent.enAutoOpenRootCause.setValue(getAutoOpenCauseNameStr(this.mAutoOpenRootCause));
                        exceptionEvent.enAutoCloseRootCause.setValue(getAutoCloseCauseNameStr(this.mAutoCloseRootCause));
                        exceptionEvent.enWifiProSubEvent.setValue(ucSubErrorCode);
                        exceptionEvent.strREDIRECT_URL.setValue(this.mNotOpenApRedirectUrl);
                        exceptionEvent.enAC_FailType.setValue(getACFailTypeStr(this.mAC_FailType));
                        exceptionEvent.ucAPSecurity.setValue(this.mAPSecurity);
                        exceptionEvent.iHomeAPJudgeTime.setValue(this.mHomeAPJudgeTime);
                        exceptionEvent.enBG_AC_DiffType.setValue(getBG_AC_DiffTypeStr(this.mBG_AC_DiffType));
                        chrLogBaseModel = exceptionEvent;
                    } catch (Exception e3) {
                        Log.e(TAG, "send WIFI_WIFIPRO_EXCEPTION_EVENT CHR error.");
                        e3.printStackTrace();
                    }
                }
                rstFlg = HWFLOW;
                break;
            case HwWifiCHRConstImpl.WIFI_PORTAL_AUTH_MSG_COLLECTE /*124*/:
                Log.d(TAG, "send WIFI_PORTAL_AUTH_MSG_COLLECTE CHR event.");
                CSegEVENT_WIFI_PORTAL_AUTH_MSG_COLLECTE wifiAuthMsgSample = new CSegEVENT_WIFI_PORTAL_AUTH_MSG_COLLECTE();
                wifiAuthMsgSample.tmTimeStamp.setValue(date);
                wifiAuthMsgSample.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                wifiAuthMsgSample.strSMS_Num.setValue(this.mSms_Num);
                wifiAuthMsgSample.ucSMS_body_len.setValue(this.mSMS_Body_Len);
                wifiAuthMsgSample.ucAPSsid_len.setValue(this.mSsid_Len);
                wifiAuthMsgSample.aucSMS_body.setValue(this.mSms_Body);
                wifiAuthMsgSample.aucAPSsid.setValue(this.mPortalAPSsid);
                wifiAuthMsgSample.strAPBssid.setValue(this.mPortalAPBssid);
                wifiAuthMsgSample.strLOC_CellID.setValue(this.mPortalCellId);
                chrLogBaseModel = wifiAuthMsgSample;
                rstFlg = HWFLOW;
                break;
            case WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT /*125*/:
                if (this.mWifiProDualbandExceptionRecord != null) {
                    if (canUploadWifiproDualbandEvent(ucSubErrorCode, commercialUser)) {
                        try {
                            Log.d(TAG, "send CSegEVENT_WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT CHR event.");
                            CSegEVENT_WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT exceptionEvent2 = new CSegEVENT_WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT();
                            exceptionEvent2.tmTimeStamp.setValue(date);
                            exceptionEvent2.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                            exceptionEvent2.enDualbandExSubEvent.setValue(ucSubErrorCode);
                            if (this.mWifiProDualbandExceptionRecord.mSSID_2G != null) {
                                exceptionEvent2.aucSSID_2G.setValue(this.mWifiProDualbandExceptionRecord.mSSID_2G.getBytes("UTF-8"));
                            }
                            if (this.mWifiProDualbandExceptionRecord.mSSID_5G != null) {
                                exceptionEvent2.aucSSID_5G.setValue(this.mWifiProDualbandExceptionRecord.mSSID_5G.getBytes("UTF-8"));
                            }
                            exceptionEvent2.ucSingleOrMixed.setValue(this.mWifiProDualbandExceptionRecord.mSingleOrMixed);
                            exceptionEvent2.usScan_Threshod_RSSI_2G.setValue(this.mWifiProDualbandExceptionRecord.mScan_Threshod_RSSI_2G);
                            exceptionEvent2.usTarget_RSSI_5G.setValue(this.mWifiProDualbandExceptionRecord.mTarget_RSSI_5G);
                            exceptionEvent2.usRSSI_2G.setValue(this.mWifiProDualbandExceptionRecord.mRSSI_2G);
                            exceptionEvent2.usRSSI_5G.setValue(this.mWifiProDualbandExceptionRecord.mRSSI_5G);
                            exceptionEvent2.usScore_2G.setValue(this.mWifiProDualbandExceptionRecord.mScore_2G);
                            exceptionEvent2.usScore_5G.setValue(this.mWifiProDualbandExceptionRecord.mScore_5G);
                            exceptionEvent2.usHandOverErrCode.setValue(this.mWifiProDualbandExceptionRecord.mHandOverErrCode);
                            exceptionEvent2.ucIsBluetoothConnected.setValue(this.mWifiProDualbandExceptionRecord.mIsBluetoothConnected);
                            exceptionEvent2.usRTT_2G.setValue(this.mWifiProDualbandExceptionRecord.mRTT_2G);
                            exceptionEvent2.usLossRate_2G.setValue(this.mWifiProDualbandExceptionRecord.mLossRate_2G);
                            exceptionEvent2.usConnectTime_2G.setValue(this.mWifiProDualbandExceptionRecord.mConnectTime_2G);
                            exceptionEvent2.usRTT_5G.setValue(this.mWifiProDualbandExceptionRecord.mRTT_5G);
                            exceptionEvent2.usLossRate_5G.setValue(this.mWifiProDualbandExceptionRecord.mLossRate_5G);
                            exceptionEvent2.usConnectTime_5G.setValue(this.mWifiProDualbandExceptionRecord.mConnectTime_5G);
                            chrLogBaseModel = exceptionEvent2;
                        } catch (Exception e32) {
                            Log.e(TAG, "send WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT CHR error.");
                            e32.printStackTrace();
                        }
                    }
                    rstFlg = HWFLOW;
                    break;
                }
                Log.e(TAG, "send WIFI_WIFIPRO_DUALBAND_EXCEPTION_EVENT null error.");
                break;
            case WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT /*126*/:
                if (this.mDualband2GAPBssid != null || this.mDualband5GAPBssid != null) {
                    if (this.mDualbandAPType != WIFI_SETTING_OPENED_AND_SERVICE_CLOSED && this.mDualbandAPType != WIFI_SETTING_OPENED_TIMEOUT) {
                        Log.e(TAG, "send DUALBAND_AP_INFO_EVENT DualbandAPType error.");
                        break;
                    }
                    try {
                        Log.d(TAG, "send WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT CHR event.");
                        CSegEVENT_WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT exceptionEvent3 = new CSegEVENT_WIFI_WIFIPRO_DUALBAND_AP_INFO_EVENT();
                        exceptionEvent3.tmTimeStamp.setValue(date);
                        exceptionEvent3.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                        exceptionEvent3.ucSingleOrMixed.setValue(this.mDualbandAPType);
                        exceptionEvent3.strBSSID_2G.setValue(this.mDualband2GAPBssid);
                        exceptionEvent3.strBSSID_5G.setValue(this.mDualband5GAPBssid);
                        chrLogBaseModel = exceptionEvent3;
                    } catch (Exception e322) {
                        Log.e(TAG, "send WIFI_WIFIPRO_EXCEPTION_EVENT CHR error.");
                        e322.printStackTrace();
                    }
                    rstFlg = HWFLOW;
                    break;
                }
                Log.e(TAG, "send DUALBAND_AP_INFO_EVENT null error.");
                break;
                break;
            case ClientHandler.MSG_PROBE_WEB_START /*127*/:
                if (matchHalDriverEventTriggerFreq(commercialUser, ucSubErrorCode)) {
                    CSegEVENT_WIFI_REPEATER_OPEN_OR_CLOSE_FAILED wifiRepeaterOpenOrCloseFailed = new CSegEVENT_WIFI_REPEATER_OPEN_OR_CLOSE_FAILED();
                    wifiRepeaterOpenOrCloseFailed.tmTimeStamp.setValue(date);
                    wifiRepeaterOpenOrCloseFailed.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    wifiRepeaterOpenOrCloseFailed.ucOpenOrClose.setValue(this.mRepeterOpenOrClose);
                    wifiRepeaterOpenOrCloseFailed.enWIFI_REPEATER_OPEN_OR_CLOSE_FAILED_REASON.setValue(ucSubErrorCode);
                    chrLogBaseModel = wifiRepeaterOpenOrCloseFailed;
                    if (HWFLOW) {
                        Log.d(TAG, "WIFI_REPEATER_OPEN_OR_CLOSE_FAILED mRepeterOpenOrClose = " + this.mRepeterOpenOrClose + "reason + " + ucSubErrorCode);
                        break;
                    }
                }
                break;
            case HwSelfCureUtils.SCE_WIFI_DISABLED_DELAY /*200*/:
                if (matchHalDriverEventTriggerFreq(commercialUser, ucSubErrorCode)) {
                    CSegEVENT_WIFI_HAL_DRIVER_DEVICE_EXCEPTION segException = new CSegEVENT_WIFI_HAL_DRIVER_DEVICE_EXCEPTION();
                    segException.tmTimeStamp.setValue(date);
                    segException.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    segException.enWIFI_HAL_DRIVER_DEVICE_EXCEPTION_REASON.setValue(ucSubErrorCode);
                    chrLogBaseModel = segException;
                    break;
                }
                break;
            case 208:
                if (matchHalDriverEventTriggerFreq(commercialUser, ucSubErrorCode)) {
                    CSegEVENT_WIFI_DEVICE_ERROR segWifiDeviceError = new CSegEVENT_WIFI_DEVICE_ERROR();
                    segWifiDeviceError.tmTimeStamp.setValue(date);
                    segWifiDeviceError.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    segWifiDeviceError.enWifiDeviceErrorReason.setValue(ucSubErrorCode);
                    chrLogBaseModel = segWifiDeviceError;
                    break;
                }
                break;
            case 210:
                if (matchHalDriverEventTriggerFreq(commercialUser, ucSubErrorCode)) {
                    CSegEVENT_WIFI_ANTS_SWITCH_FAILED segWifiSwFailedEvent = new CSegEVENT_WIFI_ANTS_SWITCH_FAILED();
                    segWifiSwFailedEvent.tmTimeStamp.setValue(date);
                    segWifiSwFailedEvent.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    segWifiSwFailedEvent.enWifiAntsSwitchFailedReason.setValue(ucSubErrorCode);
                    segWifiSwFailedEvent.ucWifiAntsSwitchDir.setValue(this.wifiAntsStatus.getAntsSwitchFailedDir());
                    chrLogBaseModel = segWifiSwFailedEvent;
                    break;
                }
                break;
            case 213:
                if (HWFLOW) {
                    Log.d(TAG, "WIFI_AP_INFO_COLLECT, ucSubErrorCode = " + ucSubErrorCode);
                }
                if (matchHalDriverEventTriggerFreq(commercialUser, "WIFI_AP_INFO_COLLECT")) {
                    CSegEVENT_WIFI_AP_INFO_COLLECT segWifiScanApInfo = new CSegEVENT_WIFI_AP_INFO_COLLECT();
                    segWifiScanApInfo.tmTimeStamp.setValue(date);
                    segWifiScanApInfo.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    String inBSSID = "";
                    String vendorInfo = "";
                    try {
                        JSONObject jSONObject = new JSONObject(ucSubErrorCode);
                        try {
                            inBSSID = jSONObject.getString(HwWifiStateMachine.BSSID_KEY);
                            int channel = jSONObject.getInt(HwWifiStateMachine.AP_CAP_KEY);
                            int txMcsSet = jSONObject.getInt(HwWifiStateMachine.TX_MCS_SET);
                            int hashCode = inBSSID.hashCode();
                            segWifiScanApInfo.strBSSID.setValue(maskMacAddress(inBSSID));
                            segWifiScanApInfo.ucApStreamInfo.setValue(channel);
                            segWifiScanApInfo.strapVendorInfo.setValue(this.mAPVendorInfo);
                            segWifiScanApInfo.ucTxMcsSet.setValue(txMcsSet);
                            if (HWFLOW) {
                                Log.d(TAG, "WIFI_AP_INFO_COLLECT, mAPVendorInfo = " + this.mAPVendorInfo);
                            }
                            chrLogBaseModel = segWifiScanApInfo;
                            break;
                        } catch (JSONException e4) {
                            e4.printStackTrace();
                            break;
                        }
                    } catch (JSONException e42) {
                        e42.printStackTrace();
                        break;
                    }
                }
                break;
            case 214:
                if (matchCommercialTrigger(commercialUser, this.mDailyUploadTime, elapsedRealtime, this.mWifiConnentEventCount)) {
                    List<CSubRSSIGROUP_EVENT> lstRssiGroupCHR;
                    byte essNumber = (byte) (getRelatedApInfo(bssid) >> ANT_SEC_WORK_TIME);
                    iIsMobileAP = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                    if (this.mContextRef != null) {
                        iIsMobileAP = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContextRef) ? WIFI_SETTING_OPENED_AND_SERVICE_CLOSED : WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
                    }
                    List<CSubAuth_Chr_Event> lstAuth_Chr_EventList = new ArrayList();
                    List<CSubAssoc_Chr_Event> lstAssoc_Chr_EventList = new ArrayList();
                    List<CSubDHCP_Chr_Event> lstDHCP_Chr_EventList = new ArrayList();
                    boolean z = HWFLOW;
                    try {
                        z = this.mLock.tryLock(1, TimeUnit.SECONDS);
                        if (z) {
                            List<HwCHRAccessNetworkEventInfo> arrayList = new ArrayList(this.mHwCHRAccessNetworkEventInfoList);
                            List<HwCHRAccessNetworkEventInfo> list;
                            try {
                                Collections.reverse(arrayList);
                                for (HwCHRAccessNetworkEventInfo tmpEvent : arrayList) {
                                    if (tmpEvent.getEventId() == 82 && lstAuth_Chr_EventList.size() < SECURITY_WAPI_CERT) {
                                        CSubAuth_Chr_Event auth = new CSubAuth_Chr_Event();
                                        auth.tmTimeStamp.setValue(tmpEvent.getEventTriggerDate());
                                        auth.usSubErrorCode.setValue(tmpEvent.getSubErrorCode());
                                        auth.usAP_channel.setValue(tmpEvent.getAP_channel());
                                        auth.iAP_RSSI.setValue(tmpEvent.getAP_RSSI());
                                        auth.ucIsOnScreen.setValue(tmpEvent.getIsOnScreen());
                                        lstAuth_Chr_EventList.add(auth);
                                    } else if (tmpEvent.getEventId() == 83 && lstAssoc_Chr_EventList.size() < SECURITY_WAPI_CERT) {
                                        CSubAssoc_Chr_Event assoc = new CSubAssoc_Chr_Event();
                                        assoc.tmTimeStamp.setValue(tmpEvent.getEventTriggerDate());
                                        assoc.usSubErrorCode.setValue(tmpEvent.getSubErrorCode());
                                        assoc.usAP_channel.setValue(tmpEvent.getAP_channel());
                                        assoc.iAP_RSSI.setValue(tmpEvent.getAP_RSSI());
                                        assoc.ucIsOnScreen.setValue(tmpEvent.getIsOnScreen());
                                        lstAssoc_Chr_EventList.add(assoc);
                                    } else if (tmpEvent.getEventId() == 84 && lstDHCP_Chr_EventList.size() < SECURITY_WAPI_CERT) {
                                        CSubDHCP_Chr_Event dhcp = new CSubDHCP_Chr_Event();
                                        dhcp.tmTimeStamp.setValue(tmpEvent.getEventTriggerDate());
                                        dhcp.enDHCP_FAILED.setValue(tmpEvent.getDHCP_FAILED());
                                        dhcp.usSubErrorCode.setValue(tmpEvent.getSubErrorCode());
                                        dhcp.usAP_channel.setValue(tmpEvent.getAP_channel());
                                        dhcp.iAP_RSSI.setValue(tmpEvent.getAP_RSSI());
                                        dhcp.ucIsOnScreen.setValue(tmpEvent.getIsOnScreen());
                                        lstDHCP_Chr_EventList.add(dhcp);
                                    }
                                }
                                list = arrayList;
                            } catch (InterruptedException e5) {
                                e = e5;
                                list = arrayList;
                            } catch (Throwable th2) {
                                th = th2;
                                list = arrayList;
                            }
                        }
                        if (z) {
                            this.mLock.unlock();
                        }
                    } catch (InterruptedException e6) {
                        e = e6;
                        try {
                            if (HWFLOW) {
                                Log.d(TAG, "get data for WIFI_CONNECT_EVENT : " + e);
                            }
                            if (z) {
                                this.mLock.unlock();
                            }
                            lstRssiGroupCHR = this.mRssiGroup.getRSSIGroupCHR();
                            cSegEVENT_WIFI_CONNECT_EVENT = new CSegEVENT_WIFI_CONNECT_EVENT();
                            cSegEVENT_WIFI_CONNECT_EVENT.tmTimeStamp.setValue(date);
                            cSegEVENT_WIFI_CONNECT_EVENT.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                            cSegEVENT_WIFI_CONNECT_EVENT.enconnect_type.setValue(this.mConnectType);
                            cSegEVENT_WIFI_CONNECT_EVENT.iconnect_success_time.setValue(Integer.parseInt(String.valueOf(this.mConnectSuccessTime)));
                            cSegEVENT_WIFI_CONNECT_EVENT.lTimeStamp1.setValue(this.mTimeStampSessionStart);
                            cSegEVENT_WIFI_CONNECT_EVENT.lTimeStamp2.setValue(this.mTimeStampSessionFirstConnect);
                            cSegEVENT_WIFI_CONNECT_EVENT.lTimeStamp3.setValue(this.mTimeStampSessionFinish);
                            cSegEVENT_WIFI_CONNECT_EVENT.strThreadNameConnectAP.setValue(this.mConnectThreadName);
                            cSegEVENT_WIFI_CONNECT_EVENT.strThreadNameDisableAP.setValue(this.mDisableThreadName);
                            cSegEVENT_WIFI_CONNECT_EVENT.strSTA_MAC.setValue(this.strSta_mac);
                            cSegEVENT_WIFI_CONNECT_EVENT.strAP_MAC.setValue(bssidMask);
                            cSegEVENT_WIFI_CONNECT_EVENT.strAP_SSID.setValue(this.strAp_Ssid);
                            cSegEVENT_WIFI_CONNECT_EVENT.strAP_proto.setValue(this.strAP_proto);
                            cSegEVENT_WIFI_CONNECT_EVENT.strAP_key_mgmt.setValue(this.strAP_key_mgmt);
                            cSegEVENT_WIFI_CONNECT_EVENT.strAP_auth_alg.setValue(this.strAP_auth_alg);
                            cSegEVENT_WIFI_CONNECT_EVENT.strAP_pairwise.setValue(this.strAP_pairwise);
                            cSegEVENT_WIFI_CONNECT_EVENT.strAP_group.setValue(this.strAP_gruop);
                            cSegEVENT_WIFI_CONNECT_EVENT.strAP_eap.setValue(this.strAP_eap);
                            cSegEVENT_WIFI_CONNECT_EVENT.usAP_link_speed.setValue(this.usLinkSpeed);
                            cSegEVENT_WIFI_CONNECT_EVENT.usAP_channel.setValue(this.usAP_channel);
                            cSegEVENT_WIFI_CONNECT_EVENT.iAP_RSSI.setValue(this.mRssi);
                            cSegEVENT_WIFI_CONNECT_EVENT.strWIFI_IP.setValue(this.str_Wifi_ip);
                            cSegEVENT_WIFI_CONNECT_EVENT.strIP_LEASETIME.setValue(this.strIp_leasetime);
                            cSegEVENT_WIFI_CONNECT_EVENT.strWIFI_GATE.setValue(this.str_gate_ip);
                            cSegEVENT_WIFI_CONNECT_EVENT.strDNS_ADDRESS.setValue(this.str_dns);
                            cSegEVENT_WIFI_CONNECT_EVENT.strRoutes.setValue(this.strRoutes);
                            cSegEVENT_WIFI_CONNECT_EVENT.enucHwStatus.setValue("READY");
                            cSegEVENT_WIFI_CONNECT_EVENT.ucBTState.setValue(bluetoothState);
                            cSegEVENT_WIFI_CONNECT_EVENT.ucBTConnState.setValue(bluetoothConnState);
                            cSegEVENT_WIFI_CONNECT_EVENT.ucPublicEss.setValue(essNumber);
                            if (!lstAuth_Chr_EventList.isEmpty()) {
                                Collections.reverse(lstAuth_Chr_EventList);
                            }
                            if (!lstAssoc_Chr_EventList.isEmpty()) {
                                Collections.reverse(lstAssoc_Chr_EventList);
                            }
                            if (!lstDHCP_Chr_EventList.isEmpty()) {
                                Collections.reverse(lstDHCP_Chr_EventList);
                            }
                            for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < lstAuth_Chr_EventList.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                                cSegEVENT_WIFI_CONNECT_EVENT.setCSubAuth_Chr_EventList((CSubAuth_Chr_Event) lstAuth_Chr_EventList.get(i));
                            }
                            for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < lstAssoc_Chr_EventList.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                                cSegEVENT_WIFI_CONNECT_EVENT.setCSubAssoc_Chr_EventList((CSubAssoc_Chr_Event) lstAssoc_Chr_EventList.get(i));
                            }
                            for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < lstDHCP_Chr_EventList.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                                cSegEVENT_WIFI_CONNECT_EVENT.setCSubDHCP_Chr_EventList((CSubDHCP_Chr_Event) lstDHCP_Chr_EventList.get(i));
                            }
                            for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < lstRssiGroupCHR.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                                cSegEVENT_WIFI_CONNECT_EVENT.setCSubRSSIGROUP_EVENTList((CSubRSSIGROUP_EVENT) lstRssiGroupCHR.get(i));
                            }
                            cSegEVENT_WIFI_CONNECT_EVENT.strapVendorInfo.setValue(this.mAPVendorInfo);
                            if (this.hwWifiCHRService != null) {
                                cSegEVENT_WIFI_CONNECT_EVENT.ucScanAlwaysAvailble.setValue(this.hwWifiCHRService.getPersistedScanAlwaysAvailable());
                                cSegEVENT_WIFI_CONNECT_EVENT.ucWIFIAlwaysNotifation.setValue(this.hwWifiCHRService.getWIFINetworkAvailableNotificationON());
                                cSegEVENT_WIFI_CONNECT_EVENT.ucWIFISleepPolicy.setValue(this.hwWifiCHRService.getWIFISleepPolicy());
                                cSegEVENT_WIFI_CONNECT_EVENT.ucProxySettings.setValue(this.hwWifiCHRService.getProxyStatus());
                                cSegEVENT_WIFI_CONNECT_EVENT.ucWifiProStatus.setValue(this.hwWifiCHRService.getWIFIProStatus());
                                cSegEVENT_WIFI_CONNECT_EVENT.strProxySettingInfo.setValue(this.hwWifiCHRService.getProxyInfo());
                                cSegEVENT_WIFI_CONNECT_EVENT.ucWifiToPDP.setValue(this.hwWifiCHRService.getWIFITOPDP());
                            }
                            cSegEVENT_WIFI_CONNECT_EVENT.ucIsMobleAP.setValue(iIsMobileAP);
                            cSegEVENT_WIFI_CONNECT_EVENT.ucIsOnScreen.setValue(this.mScreenState);
                            cSegEVENT_WIFI_CONNECT_EVENT.enTriggerReason.setValue(getTriggerReasonNameStr(this.mTriggerReportType));
                            this.mConnectSuccessTime = 0;
                            rstFlg = true;
                            chrLogBaseModel = cSegEVENT_WIFI_CONNECT_EVENT;
                            this.mWifiConnentEventCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                            if (chrLogBaseModel != null) {
                                result.add(chrLogBaseModel);
                            }
                            if (rstFlg) {
                                resetWhenDisconnect();
                            }
                            return result;
                        } catch (Throwable th3) {
                            th = th3;
                            if (z) {
                                this.mLock.unlock();
                            }
                            throw th;
                        }
                    }
                    lstRssiGroupCHR = this.mRssiGroup.getRSSIGroupCHR();
                    cSegEVENT_WIFI_CONNECT_EVENT = new CSegEVENT_WIFI_CONNECT_EVENT();
                    cSegEVENT_WIFI_CONNECT_EVENT.tmTimeStamp.setValue(date);
                    cSegEVENT_WIFI_CONNECT_EVENT.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
                    cSegEVENT_WIFI_CONNECT_EVENT.enconnect_type.setValue(this.mConnectType);
                    cSegEVENT_WIFI_CONNECT_EVENT.iconnect_success_time.setValue(Integer.parseInt(String.valueOf(this.mConnectSuccessTime)));
                    cSegEVENT_WIFI_CONNECT_EVENT.lTimeStamp1.setValue(this.mTimeStampSessionStart);
                    cSegEVENT_WIFI_CONNECT_EVENT.lTimeStamp2.setValue(this.mTimeStampSessionFirstConnect);
                    cSegEVENT_WIFI_CONNECT_EVENT.lTimeStamp3.setValue(this.mTimeStampSessionFinish);
                    cSegEVENT_WIFI_CONNECT_EVENT.strThreadNameConnectAP.setValue(this.mConnectThreadName);
                    cSegEVENT_WIFI_CONNECT_EVENT.strThreadNameDisableAP.setValue(this.mDisableThreadName);
                    cSegEVENT_WIFI_CONNECT_EVENT.strSTA_MAC.setValue(this.strSta_mac);
                    cSegEVENT_WIFI_CONNECT_EVENT.strAP_MAC.setValue(bssidMask);
                    cSegEVENT_WIFI_CONNECT_EVENT.strAP_SSID.setValue(this.strAp_Ssid);
                    cSegEVENT_WIFI_CONNECT_EVENT.strAP_proto.setValue(this.strAP_proto);
                    cSegEVENT_WIFI_CONNECT_EVENT.strAP_key_mgmt.setValue(this.strAP_key_mgmt);
                    cSegEVENT_WIFI_CONNECT_EVENT.strAP_auth_alg.setValue(this.strAP_auth_alg);
                    cSegEVENT_WIFI_CONNECT_EVENT.strAP_pairwise.setValue(this.strAP_pairwise);
                    cSegEVENT_WIFI_CONNECT_EVENT.strAP_group.setValue(this.strAP_gruop);
                    cSegEVENT_WIFI_CONNECT_EVENT.strAP_eap.setValue(this.strAP_eap);
                    cSegEVENT_WIFI_CONNECT_EVENT.usAP_link_speed.setValue(this.usLinkSpeed);
                    cSegEVENT_WIFI_CONNECT_EVENT.usAP_channel.setValue(this.usAP_channel);
                    cSegEVENT_WIFI_CONNECT_EVENT.iAP_RSSI.setValue(this.mRssi);
                    cSegEVENT_WIFI_CONNECT_EVENT.strWIFI_IP.setValue(this.str_Wifi_ip);
                    cSegEVENT_WIFI_CONNECT_EVENT.strIP_LEASETIME.setValue(this.strIp_leasetime);
                    cSegEVENT_WIFI_CONNECT_EVENT.strWIFI_GATE.setValue(this.str_gate_ip);
                    cSegEVENT_WIFI_CONNECT_EVENT.strDNS_ADDRESS.setValue(this.str_dns);
                    cSegEVENT_WIFI_CONNECT_EVENT.strRoutes.setValue(this.strRoutes);
                    cSegEVENT_WIFI_CONNECT_EVENT.enucHwStatus.setValue("READY");
                    cSegEVENT_WIFI_CONNECT_EVENT.ucBTState.setValue(bluetoothState);
                    cSegEVENT_WIFI_CONNECT_EVENT.ucBTConnState.setValue(bluetoothConnState);
                    cSegEVENT_WIFI_CONNECT_EVENT.ucPublicEss.setValue(essNumber);
                    if (lstAuth_Chr_EventList.isEmpty()) {
                        Collections.reverse(lstAuth_Chr_EventList);
                    }
                    if (lstAssoc_Chr_EventList.isEmpty()) {
                        Collections.reverse(lstAssoc_Chr_EventList);
                    }
                    if (lstDHCP_Chr_EventList.isEmpty()) {
                        Collections.reverse(lstDHCP_Chr_EventList);
                    }
                    for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < lstAuth_Chr_EventList.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                        cSegEVENT_WIFI_CONNECT_EVENT.setCSubAuth_Chr_EventList((CSubAuth_Chr_Event) lstAuth_Chr_EventList.get(i));
                    }
                    for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < lstAssoc_Chr_EventList.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                        cSegEVENT_WIFI_CONNECT_EVENT.setCSubAssoc_Chr_EventList((CSubAssoc_Chr_Event) lstAssoc_Chr_EventList.get(i));
                    }
                    for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < lstDHCP_Chr_EventList.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                        cSegEVENT_WIFI_CONNECT_EVENT.setCSubDHCP_Chr_EventList((CSubDHCP_Chr_Event) lstDHCP_Chr_EventList.get(i));
                    }
                    for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < lstRssiGroupCHR.size(); i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
                        cSegEVENT_WIFI_CONNECT_EVENT.setCSubRSSIGROUP_EVENTList((CSubRSSIGROUP_EVENT) lstRssiGroupCHR.get(i));
                    }
                    cSegEVENT_WIFI_CONNECT_EVENT.strapVendorInfo.setValue(this.mAPVendorInfo);
                    if (this.hwWifiCHRService != null) {
                        cSegEVENT_WIFI_CONNECT_EVENT.ucScanAlwaysAvailble.setValue(this.hwWifiCHRService.getPersistedScanAlwaysAvailable());
                        cSegEVENT_WIFI_CONNECT_EVENT.ucWIFIAlwaysNotifation.setValue(this.hwWifiCHRService.getWIFINetworkAvailableNotificationON());
                        cSegEVENT_WIFI_CONNECT_EVENT.ucWIFISleepPolicy.setValue(this.hwWifiCHRService.getWIFISleepPolicy());
                        cSegEVENT_WIFI_CONNECT_EVENT.ucProxySettings.setValue(this.hwWifiCHRService.getProxyStatus());
                        cSegEVENT_WIFI_CONNECT_EVENT.ucWifiProStatus.setValue(this.hwWifiCHRService.getWIFIProStatus());
                        cSegEVENT_WIFI_CONNECT_EVENT.strProxySettingInfo.setValue(this.hwWifiCHRService.getProxyInfo());
                        cSegEVENT_WIFI_CONNECT_EVENT.ucWifiToPDP.setValue(this.hwWifiCHRService.getWIFITOPDP());
                    }
                    cSegEVENT_WIFI_CONNECT_EVENT.ucIsMobleAP.setValue(iIsMobileAP);
                    cSegEVENT_WIFI_CONNECT_EVENT.ucIsOnScreen.setValue(this.mScreenState);
                    cSegEVENT_WIFI_CONNECT_EVENT.enTriggerReason.setValue(getTriggerReasonNameStr(this.mTriggerReportType));
                    this.mConnectSuccessTime = 0;
                    rstFlg = true;
                    chrLogBaseModel = cSegEVENT_WIFI_CONNECT_EVENT;
                }
                this.mWifiConnentEventCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
                break;
        }
        if (chrLogBaseModel != null) {
            result.add(chrLogBaseModel);
        }
        if (rstFlg) {
            resetWhenDisconnect();
        }
        return result;
    }

    private String maskMacAddress(String macAddress) {
        if (macAddress != null) {
            String[] items = macAddress.split(":");
            if (items.length >= SECURITY_WAPI_PSK) {
                Object[] objArr = new Object[SECURITY_WAPI_PSK];
                objArr[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED] = items[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED];
                objArr[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED] = items[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED];
                objArr[WIFI_SETTING_OPENED_TIMEOUT] = items[WIFI_SETTING_OPENED_TIMEOUT];
                objArr[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE] = items[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE];
                return String.format("%s:%s:%s:%s:FF:FF", objArr);
            }
        }
        return macAddress;
    }

    private String maskIpAddress(String ipAddress) {
        if (ipAddress != null) {
            String[] items = ipAddress.split("\\.");
            if (items.length >= UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE) {
                Object[] objArr = new Object[UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE];
                objArr[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED] = items[WIFI_SETTING_CLOSED_AND_SERVICE_OPENED];
                objArr[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED] = items[WIFI_SETTING_OPENED_AND_SERVICE_CLOSED];
                objArr[WIFI_SETTING_OPENED_TIMEOUT] = items[WIFI_SETTING_OPENED_TIMEOUT];
                return String.format("%s.%s.%s.XXX", objArr);
            }
        }
        return ipAddress;
    }

    private void resetDailyStat() {
        this.mOpenCloseFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mApkChangedCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAuthFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mDhcpFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mScanFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAccessFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAccessWebSlowCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mAssocFailCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mDisconnTotalCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mWorkaroundCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mWifiConnentEventCount = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        for (int i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < MAX_DISCONN_CNT; i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
            this.mDisconnectFail[i] = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        }
        this.mLastAccessWebSlowTime = 0;
        this.mUploadAccessWebSlowly = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
    }

    public void uploadAssocRejectExc(int status) {
        if ("wlan0".equals(this.mCurrentMsgIface)) {
            uploadAssocRejectException(status);
        }
    }

    public void uploadConnectFailed() {
        if ("wlan0".equals(this.mCurrentMsgIface)) {
            uploadConnectFailed(WIFI_SECURITY_TYPE_UNKNOWN, UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE);
        }
    }

    public void uploadDisconnectExc(String remainder) {
        if ("wlan0".equals(this.mCurrentMsgIface)) {
            uploadDisconnectException(remainder);
        }
    }

    public void updateAPSsidByEvent(String eventStr) {
        if ("wlan0".equals(this.mCurrentMsgIface)) {
            int idx = eventStr != null ? eventStr.indexOf("SSID '") : WIFI_SECURITY_TYPE_UNKNOWN;
            if (idx > 0) {
                String ssid = eventStr.substring(idx + MAX_DISCONN_CNT);
                updateAPSsid(ssid.substring(WIFI_SETTING_CLOSED_AND_SERVICE_OPENED, ssid.length() + WIFI_SECURITY_TYPE_UNKNOWN));
            }
        }
    }

    public void updateAP(int networkId, SupplicantState newSupplicantState, String BSSID, int newState, WifiNative wifiNative) {
        if (networkId >= 0 && newSupplicantState == SupplicantState.ASSOCIATED && "wlan0".equals(this.mCurrentMsgIface)) {
            this.mNetworkId = networkId;
            if (wifiNative != null) {
                String proto = wifiNative.getNetworkVariable(networkId, "proto");
                String key_mgmt = wifiNative.getNetworkVariable(networkId, "key_mgmt");
                String auth_alg = wifiNative.getNetworkVariable(networkId, "auth_alg");
                String pairwise = wifiNative.getNetworkVariable(networkId, "pairwise");
                String group = wifiNative.getNetworkVariable(networkId, "group");
                String eap = wifiNative.getNetworkVariable(networkId, "eap");
                updateApMessage(proto, key_mgmt, auth_alg, pairwise, group, eap);
                Log.d(TAG, "handleSupplicantStateChange:proto:" + proto + "; key_mgmt:" + key_mgmt + "; auth_alg:" + auth_alg + "; pairwise:" + pairwise + "; group:" + group + "; eap:" + eap);
            }
        }
        if ("wlan0".equals(this.mCurrentMsgIface)) {
            Log.d(TAG, "handleSupplicantStateChange updateAPMAC: " + BSSID);
            if (newState >= SECURITY_WAPI_PSK && newState <= 9) {
                updateAPMAC(BSSID);
            }
            if (this.mHwWifiStatStore != null) {
                boolean wifiprotempflag = HWFLOW;
                if (networkId != WIFI_SECURITY_TYPE_UNKNOWN && newSupplicantState == SupplicantState.ASSOCIATING) {
                    wifiprotempflag = isWifiProEvaluate();
                }
                this.mHwWifiStatStore.handleSupplicantStateChange(newSupplicantState, wifiprotempflag);
            }
            this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
            if (this.hwWifiCHRService != null) {
                this.hwWifiCHRService.handleSupplicantStateChange(newSupplicantState);
            }
        }
    }

    public void checkAppName(WifiConfiguration config, Context mContext) {
        if (config.callingPid > 0) {
            String appNameOfUI = "com.android.settings";
            String callingAppName = getAppName(config.callingPid, mContext);
            if (callingAppName == null || !appNameOfUI.equals(callingAppName)) {
                config.callingPid = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
            }
        }
    }

    public void uploadFailureIfFailed(int reason, int netId) {
    }

    public void LinkPropertiesUpdate(RouteInfo route) {
        if (route.isDefaultRoute()) {
            updateGateWay(route.getGateway().getHostAddress());
            updateStrucRoutes(route.getInterface());
            log("updateLinkProperties strucRoutes:" + route.getInterface());
        }
    }

    public void updateAPSsid(SupplicantState state, WifiInfo mWifiInfo) {
        if (state == SupplicantState.COMPLETED && mWifiInfo.getWifiSsid() != null) {
            String oriSsid = mWifiInfo.getWifiSsid().oriSsid;
            String ssid = "";
            if (!oriSsid.equals("")) {
                ssid = oriSsid.substring(WIFI_SETTING_OPENED_TIMEOUT, oriSsid.length() + WIFI_SECURITY_TYPE_UNKNOWN);
            }
            try {
                updateAPSsid(ssid);
            } catch (Exception e) {
                loge("setAPSSID e: " + e);
            }
        }
    }

    public void handleIPv4SuccessException(Inet4Address addr) {
        if (addr != null) {
            loge("updateWifiIp  StateMachine :log has ip, hide it");
            updateWifiIp(addr.getHostAddress());
            updateTimeStampSessionFinish(SystemClock.elapsedRealtime());
            if (LogManager.getInstance().isCommercialUser()) {
                clearHwCHRAccessNetworkEventInfoList();
            } else {
                updateTimeStampSessionFinish(SystemClock.elapsedRealtime());
                reportHwCHRAccessNetworkEventInfoList(SECURITY_WAPI_PSK);
            }
            this.mTimeStampSessionStart = 0;
            this.mTimeStampSessionFirstConnect = 0;
            this.mTimeStampSessionFinish = 0;
            this.mConnectSuccessTime = 0;
        }
    }

    public void updateWifiDirverException(String path) {
        Exception e;
        Throwable th;
        Scanner scanner = null;
        try {
            Scanner scanner2 = new Scanner(new File(path), "US-ASCII");
            try {
                int wifiOpenState = scanner2.nextInt();
                if (wifiOpenState != 0) {
                    loge("wifi open state is: " + wifiOpenState);
                    updateWifiException(80, "DIRVER_FAILED");
                }
                if (scanner2 != null) {
                    scanner2.close();
                }
                scanner = scanner2;
            } catch (Exception e2) {
                e = e2;
                scanner = scanner2;
                try {
                    loge("could not open wifi state sys node" + e);
                    if (scanner != null) {
                        scanner.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (scanner != null) {
                        scanner.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                scanner = scanner2;
                if (scanner != null) {
                    scanner.close();
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            loge("could not open wifi state sys node" + e);
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    public void setLastNetIdFromUI(WifiConfiguration config, int netId) {
        if (config.callingPid > 0) {
            setLastNetIdFromUI(netId);
            config.callingPid = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
            loge("CONNECT_NETWORK config is from UI");
            return;
        }
        setLastNetIdFromUI(WIFI_SECURITY_TYPE_UNKNOWN);
    }

    public void handleSupplicantException() {
        int count = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        String supplicantStauts = SystemProperties.get("init.svc.p2p_supplicant", "running");
        while (!"stopped".equals(supplicantStauts) && count < SECURITY_WAPI_CERT) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            count += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
            supplicantStauts = SystemProperties.get("init.svc.p2p_supplicant", "running");
            log("supplicantStauts = " + supplicantStauts + "count = " + count);
        }
        if (!"stopped".equals(supplicantStauts)) {
            updateWifiException(81, "CLOSE_SUPPLICANT_CONNECT_FAILED");
        }
    }

    public int updateWifiException(int mScanFailedCount) {
        mScanFailedCount += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
        if (mScanFailedCount < 10) {
            return mScanFailedCount;
        }
        updateWifiException(86, "");
        return WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
    }

    public void handleCHREvents(String eventStr) {
        int index = eventStr.indexOf(",");
        if (index > 0) {
            String header = eventStr.substring(WIFI_SETTING_CLOSED_AND_SERVICE_OPENED, index);
            String body = eventStr.substring(index + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED);
            int typeIdx = header.indexOf("=");
            if (typeIdx > 0) {
                String strType = header.substring(typeIdx + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED);
                HwWifiCHRStateManager chrMgr = getDefault();
                if ("0".equals(strType)) {
                    chrMgr.updateApVendorInfo(body);
                }
            }
        }
    }

    protected String getAppName(int pID, Context mContext) {
        String processName = "";
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pID) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public void setCurrentMsgIface(String ifac) {
        this.mCurrentMsgIface = ifac;
    }

    protected void log(String s) {
        Log.d(TAG, s);
    }

    protected void loge(String s) {
        Log.e(TAG, s);
    }

    public void processWifiHalDriverEvent(String strJsonExp) {
        if (HWFLOW) {
            Log.d(TAG, "processWifiHalDriverEvent, " + strJsonExp);
        }
        JSONObject jSONObject = null;
        int eventNo = WIFI_SECURITY_TYPE_UNKNOWN;
        int errNo = WIFI_SECURITY_TYPE_UNKNOWN;
        String str = null;
        try {
            jSONObject = new JSONObject(strJsonExp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jSONObject == null) {
            if (HWFLOW) {
                Log.d(TAG, "null == jsonStr, return");
            }
            return;
        }
        try {
            eventNo = jSONObject.getInt("event");
            errNo = jSONObject.getInt("error");
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        if (ConnectivityLogManager.mapCHRDataPlus == null || ConnectivityLogManager.mapCHRDataPlus.isEmpty()) {
            if (HWFLOW) {
                Log.d(TAG, "processWifiHalDriverEvent:processWifiHalDriverEvent, ConnectivityLogManager.mapCHRDataPlus is null or empty");
            }
            return;
        }
        eventNo = adaptEvent(eventNo);
        errNo = adaptErrorCode(eventNo, errNo, WIFI_CHR_DEVICE_ERROR_OFFSET);
        HashMap<Integer, String> mapReason = (HashMap) this.mapWifiEventReaseon.get(Integer.valueOf(eventNo));
        if (mapReason == null) {
            if (HWFLOW) {
                Log.d(TAG, "null == mapReason, return");
            }
            return;
        }
        if (mapReason.containsKey(Integer.valueOf(errNo))) {
            str = (String) mapReason.get(Integer.valueOf(errNo));
        }
        if (HWFLOW) {
            Log.d(TAG, "strReason = " + str);
        }
        if (ConnectivityLogManager.mapCHRDataPlus.containsKey(Integer.valueOf(eventNo))) {
            CHRDataPlus dataPlus = ConnectivityLogManager.mapGetCHRData(eventNo);
            if (dataPlus == null) {
                ConnectivityLogManager.mapPutCHRData(eventNo, new CHRDataPlus(eventNo, errNo, str, null));
            } else {
                dataPlus.setErrorReason(str);
            }
            ConnectivityLogManager.getInstance().sendMessage(ConnectivityLogManager.getInstance().obtainMessage(WIFI_SETTING_OPENED_TIMEOUT, strJsonExp));
            if (HWFLOW) {
                Log.d(TAG, "ConnectivityLogManager.getInstance().sendMessage()");
            }
        } else {
            processWifiCHRPlusException(eventNo, str);
            if (HWFLOW) {
                Log.d(TAG, "processWifiCHRPlusException, strReason = " + str);
            }
        }
    }

    int adaptEvent(int event) {
        int retEvent = event;
        if (event == 208) {
            return HwSelfCureUtils.SCE_WIFI_DISABLED_DELAY;
        }
        return retEvent;
    }

    int adaptErrorCode(int event, int errNo, int offset) {
        int retErrNo = errNo;
        if (event == 208) {
            return errNo + offset;
        }
        return retErrNo;
    }

    void initWifiEventReasonMap() {
        Log.d(TAG, "initWifiEventReasonMap, enter");
        HashMap<Integer, String> mapWifiOpenFailed = new HashMap();
        mapWifiOpenFailed.put(Integer.valueOf(MAX_DISCONN_CNT), "CHR_WIFI_DRV_ERROR_INSMOD_KO");
        mapWifiOpenFailed.put(Integer.valueOf(7), "CHR_WIFI_DRV_ERROR_SYS_VERSION");
        mapWifiOpenFailed.put(Integer.valueOf(STATE_SCANING), "CHR_WIFI_DRV_ERROR_POWER_ON");
        mapWifiOpenFailed.put(Integer.valueOf(9), "CHR_WIFI_DRV_ERROR_CUSTOM_CALL");
        mapWifiOpenFailed.put(Integer.valueOf(10), "CHR_WIFI_HAL_ERROR_CONFIG_READ");
        mapWifiOpenFailed.put(Integer.valueOf(11), "CHR_WIFI_HAL_ERROR_AP_CONFIG_SET_FAT");
        mapWifiOpenFailed.put(Integer.valueOf(12), "CHR_WIFI_HAL_ERROR_SOCKET_FAIL");
        mapWifiOpenFailed.put(Integer.valueOf(13), "CHR_PLAT_DRV_ERROR_FIRMWARE_DOWN");
        mapWifiOpenFailed.put(Integer.valueOf(14), "CHR_PLAT_DRV_ERROR_SDIO_INIT");
        mapWifiOpenFailed.put(Integer.valueOf(WIFI_CHR_DEVICE_ERROR_OFFSET), "CHR_PLAT_DRV_ERROR_OPEN_UART");
        mapWifiOpenFailed.put(Integer.valueOf(ANT_SEC_WORK_TIME), "CHR_PLAT_DRV_ERROR_BCPU_BOOTUP");
        mapWifiOpenFailed.put(Integer.valueOf(17), "CHR_PLAT_DRV_ERROR_OPEN_WCPU");
        mapWifiOpenFailed.put(Integer.valueOf(18), "CHR_PLAT_DRV_ERROR_WCPU_BOOTUP");
        mapWifiOpenFailed.put(Integer.valueOf(19), "CHR_PLAT_DRV_ERROR_OPEN_THREAD");
        mapWifiOpenFailed.put(Integer.valueOf(20), "CHR_PLAT_DRV_ERROR_CFG_UART");
        mapWifiOpenFailed.put(Integer.valueOf(21), "CHR_PLAT_DRV_ERROR_OPEN_BCPU");
        this.mapWifiEventReaseon.put(Integer.valueOf(80), mapWifiOpenFailed);
        HashMap<Integer, String> mapWifiCloseFailed = new HashMap();
        mapWifiCloseFailed.put(Integer.valueOf(UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE), "CHR_WIFI_DRV_ERROR_POWER_OFF");
        mapWifiCloseFailed.put(Integer.valueOf(SECURITY_WAPI_PSK), "CHR_PLAT_DRV_ERROR_CLOSE_BCPU");
        mapWifiCloseFailed.put(Integer.valueOf(SECURITY_WAPI_CERT), "CHR_PLAT_DRV_ERROR_CLOSE_WCPU");
        mapWifiCloseFailed.put(Integer.valueOf(MAX_DISCONN_CNT), "CHR_PLAT_DRV_ERROR_CLOSE_THREAD");
        this.mapWifiEventReaseon.put(Integer.valueOf(81), mapWifiCloseFailed);
        HashMap<Integer, String> mapDHCPFailed = new HashMap();
        mapDHCPFailed.put(Integer.valueOf(14), "CHR_WIFI_DRV_ERROR_DHCP_TX_FAIL");
        this.mapWifiEventReaseon.put(Integer.valueOf(84), mapDHCPFailed);
        HashMap<Integer, String> mapAbnormalDisconnect = new HashMap();
        mapAbnormalDisconnect.put(Integer.valueOf(MessageUtil.CMD_ON_STOP), "CHR_WIFI_DRV_ERROR_LINKLOSS");
        mapAbnormalDisconnect.put(Integer.valueOf(MessageUtil.CMD_START_SCAN), "CHR_WIFI_DRV_ERROR_KEEPALIVE_TIMEOUT");
        mapAbnormalDisconnect.put(Integer.valueOf(HwDualBandMessageUtil.CMD_STOP_MONITOR), "CHR_WIFI_DRV_ERROR_CHANNEL_CHANGE");
        this.mapWifiEventReaseon.put(Integer.valueOf(85), mapAbnormalDisconnect);
        HashMap<Integer, String> mapWifiScanFailed = new HashMap();
        mapWifiScanFailed.put(Integer.valueOf(SECURITY_WAPI_PSK), "CHR_WIFI_DRV_ERROR_SCAN_REFUSED");
        mapWifiScanFailed.put(Integer.valueOf(SECURITY_WAPI_CERT), "CHR_WIFI_DRV_ERROR_SCAN_TIMEOUT");
        mapWifiScanFailed.put(Integer.valueOf(MAX_DISCONN_CNT), "CHR_WIFI_DRV_ERROR_SCAN_ZERO");
        mapWifiScanFailed.put(Integer.valueOf(7), "CHR_WIFI_HAL_ERROR_SCAN_TIME_OUT");
        this.mapWifiEventReaseon.put(Integer.valueOf(86), mapWifiScanFailed);
        HashMap<Integer, String> mapAccessInternetFailed = new HashMap();
        mapAccessInternetFailed.put(Integer.valueOf(MAX_DISCONN_CNT), "CHR_WIFI_DRV_ERROR_RX_NO_BUFFER");
        mapAccessInternetFailed.put(Integer.valueOf(7), "CHR_WIFI_DRV_ERROR_RF_OVERHEAT_EXCEPTION");
        this.mapWifiEventReaseon.put(Integer.valueOf(87), mapAccessInternetFailed);
        HashMap<Integer, String> mapAuthFailed = new HashMap();
        mapAuthFailed.put(Integer.valueOf(WIFI_SETTING_OPENED_AND_SERVICE_CLOSED), "CHR_WIFI_DRV_ERROR_AUTH_TIMEOUT");
        this.mapWifiEventReaseon.put(Integer.valueOf(82), mapAuthFailed);
        HashMap<Integer, String> mapAssocFailed = new HashMap();
        mapAssocFailed.put(Integer.valueOf(WIFI_SETTING_OPENED_AND_SERVICE_CLOSED), "CHR_WIFI_DRV_ERROR_ASSOC_TIMEOUT");
        this.mapWifiEventReaseon.put(Integer.valueOf(83), mapAssocFailed);
        HashMap<Integer, String> mapUserConnectFailed = new HashMap();
        mapUserConnectFailed.put(Integer.valueOf(WIFI_SETTING_OPENED_AND_SERVICE_CLOSED), "CHR_WIFI_DRV_ERROR_CONNECT_CMD");
        mapUserConnectFailed.put(Integer.valueOf(WIFI_SETTING_OPENED_TIMEOUT), "CHR_WIFI_DRV_ERROR_AUTH_REJECTED");
        mapUserConnectFailed.put(Integer.valueOf(UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE), "CHR_WIFI_DRV_ERROR_ASSOC_REJECTED");
        this.mapWifiEventReaseon.put(Integer.valueOf(MessageUtil.CMD_ON_STOP), mapUserConnectFailed);
        HashMap<Integer, String> mapAccessWebSlowlyFailed = new HashMap();
        mapAccessWebSlowlyFailed.put(Integer.valueOf(WIFI_SETTING_OPENED_AND_SERVICE_CLOSED), "CHR_WIFI_DRV_ERROR_INTERFERENCE");
        this.mapWifiEventReaseon.put(Integer.valueOf(MessageUtil.CMD_START_SCAN), mapAccessWebSlowlyFailed);
        HashMap<Integer, String> mapHalDriverEvent = new HashMap();
        mapHalDriverEvent.put(Integer.valueOf(WIFI_SETTING_OPENED_AND_SERVICE_CLOSED), "CHR_WIFI_DRV_ERROR_ARP_TX_FAIL");
        mapHalDriverEvent.put(Integer.valueOf(WIFI_SETTING_OPENED_TIMEOUT), "CHR_WIFI_DRV_ERROR_EAPOL_TX_FAIL");
        mapHalDriverEvent.put(Integer.valueOf(UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE), "CHR_WIFI_DRV_ERROR_BEAT_HEART_TIMEOUT");
        mapHalDriverEvent.put(Integer.valueOf(SECURITY_WAPI_PSK), "CHR_WIFI_DRV_ERROR_WAKEUP_FAIL");
        mapHalDriverEvent.put(Integer.valueOf(SECURITY_WAPI_CERT), "CHR_WIFI_DRV_ERROR_DEVICE_PANIC");
        mapHalDriverEvent.put(Integer.valueOf(MAX_DISCONN_CNT), "CHR_WIFI_DRV_ERROR_SDIO_TRANS_FAIL");
        mapHalDriverEvent.put(Integer.valueOf(7), "CHR_WIFI_DRV_ERROR_WATCHDOG_TIMEOUT");
        mapHalDriverEvent.put(Integer.valueOf(STATE_SCANING), "CHR_WIFI_HAL_ERROR_MODE_CHANGE_FAIL");
        mapHalDriverEvent.put(Integer.valueOf(9), "CHR_WIFI_HAL_ERROR_WPS_FAIL");
        mapHalDriverEvent.put(Integer.valueOf(10), "CHR_WIFI_HAL_ERROR_CMD_SEND_FAIL");
        mapHalDriverEvent.put(Integer.valueOf(11), "CHR_WIFI_HAL_ERROR_CONNECT_FAIL");
        mapHalDriverEvent.put(Integer.valueOf(12), "CHR_WIFI_HAL_ERROR_DISCONNECT_EVENT_RECV");
        mapHalDriverEvent.put(Integer.valueOf(13), "CHR_PLAT_DRV_ERROR_RECV_LASTWORD");
        mapHalDriverEvent.put(Integer.valueOf(14), "CHR_PLAT_DRV_ERROR_WAKEUP_DEV");
        mapHalDriverEvent.put(Integer.valueOf(WIFI_CHR_DEVICE_ERROR_OFFSET), "CHR_PLAT_DRV_ERROR_BEAT_TIMEOUT");
        mapHalDriverEvent.put(Integer.valueOf(ANT_SEC_WORK_TIME), "CHR_WIFI_DEV_ERROR_FEM_FAIL");
        mapHalDriverEvent.put(Integer.valueOf(17), "CHR_WIFI_DEV_ERROR_32K_CLK");
        mapHalDriverEvent.put(Integer.valueOf(18), "CHR_WIFI_DEV_ERROR_GPIO");
        mapHalDriverEvent.put(Integer.valueOf(19), "CHR_WIFI_DEV_ERROR_SDIO_ENUM");
        mapHalDriverEvent.put(Integer.valueOf(20), "CHR_WIFI_DEV_ERROR_IOMUX");
        mapHalDriverEvent.put(Integer.valueOf(21), "CHR_WIFI_DEV_ERROR_UART");
        this.mapWifiEventReaseon.put(Integer.valueOf(HwSelfCureUtils.SCE_WIFI_DISABLED_DELAY), mapHalDriverEvent);
        Log.d(TAG, "initWifiEventReasonMap , mapWifiEventReaseon, size = " + this.mapWifiEventReaseon.size());
        HashMap<Integer, String> mapWifiAntsEvent = new HashMap();
        mapWifiAntsEvent.put(Integer.valueOf(WIFI_SETTING_CLOSED_AND_SERVICE_OPENED), "WIFI_ANTS_SW_FAILED_MODEM_PREEMPTIVE");
        mapWifiAntsEvent.put(Integer.valueOf(WIFI_SETTING_OPENED_AND_SERVICE_CLOSED), "WIFI_ANTS_SW_FAILED_DEVICE_EXCEPTION");
        mapWifiAntsEvent.put(Integer.valueOf(WIFI_SETTING_OPENED_TIMEOUT), "WIFI_ANTS_SW_FAILED_ALG_EXCEPTION");
        mapWifiAntsEvent.put(Integer.valueOf(UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE), "WIFI_ANTS_SW_FAILED_UNKNOWN_ERROR");
        this.mapWifiEventReaseon.put(Integer.valueOf(210), mapWifiAntsEvent);
        Log.d(TAG, "initWifiEventReasonMap , mapWifiEventReaseon, size = " + this.mapWifiEventReaseon.size());
    }

    boolean matchHalDriverEventTriggerFreq(boolean commercialUser, String suberror) {
        boolean isMatch = true;
        long nowTime = SystemClock.elapsedRealtime();
        if (this.mapHalDriverEventTriggerFreq.containsKey(suberror)) {
            HalDrivceEventFreq eventFreq = (HalDrivceEventFreq) this.mapHalDriverEventTriggerFreq.get(suberror);
            if (eventFreq != null) {
                isMatch = (!commercialUser || eventFreq.getTriggerCount() < UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE) ? true : nowTime - eventFreq.getLastTime() > COMM_UPLOAD_MIN_SPAN ? true : HWFLOW;
                if (isMatch) {
                    eventFreq.setTriggerCount(eventFreq.getTriggerCount() + WIFI_SETTING_OPENED_AND_SERVICE_CLOSED);
                    eventFreq.setLastTime(nowTime);
                }
            }
        } else {
            this.mapHalDriverEventTriggerFreq.put(suberror, new HalDrivceEventFreq(WIFI_SETTING_OPENED_AND_SERVICE_CLOSED, nowTime));
        }
        if (HWFLOW) {
            Log.d(TAG, "matchHalDriverEventTriggerFreq , isMatch = " + isMatch);
        }
        return isMatch;
    }

    public void updateAccessWebException(int eventNo, String strErrorReason, AccessWebStatus aws) {
        if (HWFLOW) {
            Log.d(TAG, "updateAccessWebException , eventNo = " + eventNo + " , strErrorReason= " + strErrorReason + ", AccessWebStatus ");
        }
        if (HWFLOW) {
            Log.d(TAG, "updateAccessWebException, sdio : " + aws.toSdioString());
        }
        this.mAWS = new AccessWebStatus(aws);
        updateWifiException(eventNo, strErrorReason);
    }

    protected void processWifiCHRPlusException(int eventNo, String strReason) {
        if (eventNo == 210 || eventNo == 212) {
            int antsSwitchFailedDir = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
            if (eventNo == 210) {
                antsSwitchFailedDir = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
            } else if (eventNo == 212) {
                antsSwitchFailedDir = WIFI_SETTING_OPENED_AND_SERVICE_CLOSED;
            }
            eventNo = 210;
            this.wifiAntsStatus.setAntsSwitchFailedDir(antsSwitchFailedDir);
            if (HWFLOW) {
                Log.d(TAG, "processWifiCHRPlusException:JSON parse ret , strReason= " + strReason);
            }
        }
        updateWifiException(eventNo, strReason);
    }

    public boolean getWifiAntsInfo(int items) {
        String strRet = "";
        if (HWFLOW) {
            Log.d(TAG, "Enter getAntsInfo");
        }
        if (!this.mDualAntsChr) {
            return HWFLOW;
        }
        int param;
        if (WIFI_SECURITY_TYPE_UNKNOWN == items) {
            param = ((((WIFI_SETTING_OPENED_AND_SERVICE_CLOSED | WIFI_SETTING_OPENED_TIMEOUT) | SECURITY_WAPI_PSK) | STATE_SCANING) | ANT_SEC_WORK_TIME) | SSID_MAX_LENGTH;
        } else {
            param = items;
        }
        try {
            strRet = get_wifi_ants_info(param);
            if (strRet == null || strRet.isEmpty()) {
                if (HWFLOW) {
                    Log.d(TAG, "getAntsInfo: JNI call ret == null, return");
                }
                return HWFLOW;
            }
            if (HWFLOW) {
                Log.d(TAG, "getAntsInfo: JNI call ret = " + strRet);
            }
            try {
                JSONObject jSONObject = new JSONObject(strRet);
                try {
                    int antsSw;
                    int antCurWork = jSONObject.getInt(STR_ANT_WHICH_CUR_WORK);
                    int antSWCauseError = jSONObject.getInt(STR_ANT_LAST_SW_TIME);
                    int antSWCntIntraday = jSONObject.getInt(STR_ANT_SW_COUNT);
                    long mainAntTime = jSONObject.getLong(STR_ANT_MAIN_WORK_TIME);
                    long secAntTime = jSONObject.getLong(STR_ANT_SEC_WORK_TIME);
                    int antsWifiOpenTime = jSONObject.getInt(STR_ANT_WIFI_OPEN_TIME);
                    boolean isAntSWCauseError = antSWCauseError == WIFI_SETTING_OPENED_AND_SERVICE_CLOSED ? true : HWFLOW;
                    this.wifiAntsStatus.setAntCurWork(antCurWork);
                    this.wifiAntsStatus.setAvgRSSI(this.mRssi);
                    this.wifiAntsStatus.setMainAntTime(mainAntTime);
                    this.wifiAntsStatus.setSecAntTime(secAntTime);
                    this.wifiAntsStatus.setIsAntSWCauseError(isAntSWCauseError);
                    this.wifiAntsStatus.setAntsOpenTime(antsWifiOpenTime);
                    if ((SystemClock.elapsedRealtime() / 1000) - ((long) antsWifiOpenTime) > 86400) {
                        antsSw = antSWCntIntraday - this.mWifiAntsPrevSWCnt;
                    } else {
                        antsSw = antSWCntIntraday;
                    }
                    if (antsSw < 0) {
                        antsSw = antSWCntIntraday;
                    }
                    this.mWifiAntsPrevSWCnt = antSWCntIntraday;
                    this.wifiAntsStatus.setAntSWCntIntraday(antsSw);
                    return true;
                } catch (JSONException e) {
                    if (HWFLOW) {
                        Log.d(TAG, "processWifiCHRPlusException:JSON parse failed, return");
                    }
                    e.printStackTrace();
                    return HWFLOW;
                }
            } catch (JSONException e2) {
                e2.printStackTrace();
                return HWFLOW;
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            return HWFLOW;
        }
    }

    private void clearWifiPoorLevel() {
        int i;
        this.mRssi2gMaxRssi = -127;
        this.mRssi2gMinRssi = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mpoorRssi2gCnt = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mRssi5gMaxRssi = -127;
        this.mRssi5gMinRssi = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        this.mpoorRssi5gCnt = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < this.mRssi2gSum.length; i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
            this.mRssi2gSum[i] = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        }
        for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < this.mRssi2gCnt.length; i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
            this.mRssi2gCnt[i] = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        }
        for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < this.mRssi5gSum.length; i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
            this.mRssi5gSum[i] = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        }
        for (i = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED; i < this.mRssi5gCnt.length; i += WIFI_SETTING_OPENED_AND_SERVICE_CLOSED) {
            this.mRssi5gCnt[i] = WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        }
    }

    public void setNetSlowlyTime(long duration) {
        this.mNetSlowlyTime = Integer.parseInt(String.valueOf(duration));
    }

    public void setNetNormalTime(long duration) {
        this.mNetNormalTime = Integer.parseInt(String.valueOf(duration));
    }

    public void setWifiRepeaterWorkingTime(long workingTime) {
        this.mWifiRepeaterWorkingTime += workingTime;
        if (HWFLOW) {
            Log.d(TAG, "setWifiRepeaterWorkingTime mWifiRepeaterWorkingTime = " + this.mWifiRepeaterWorkingTime);
        }
    }

    public void addWifiRepeaterOpenedCount(int count) {
        this.mWifiRepeaterOpenedCount += count;
        if (HWFLOW) {
            Log.d(TAG, "addWifiRepeaterOpenedCount mWifiRepeaterOpenedCount = " + this.mWifiRepeaterOpenedCount);
        }
    }

    public void setWifiStateMachine(HwWifiStateMachine stateMachine) {
        this.mWifiStateMachine = stateMachine;
    }

    public void setRepeaterMaxClientCount(int maxCount) {
        if (this.mRepeterMaxClientCount > maxCount) {
            maxCount = this.mRepeterMaxClientCount;
        }
        this.mRepeterMaxClientCount = maxCount;
    }

    public void addRepeaterConnFailedCount(int failed) {
        this.mRepeterConnFailedCount += failed;
        if (HWFLOW) {
            Log.d(TAG, "addRepeaterConnFailedCount mRepeterConnFailedCount = " + this.mRepeterConnFailedCount);
        }
    }

    public void updateRepeaterOpenOrCloseError(int eventId, int openOrClose, String reason) {
        this.mRepeterOpenOrClose = openOrClose;
        updateWifiException(eventId, reason);
    }

    public int getRepeaterFreq() {
        HwWifiServiceManager hwWifiServiceManager = HwWifiServiceFactory.getHwWifiServiceManager();
        if (hwWifiServiceManager == null || !(hwWifiServiceManager instanceof HwWifiServiceManagerImpl)) {
            return WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        }
        WifiP2pServiceImpl wifiP2PServiceImpl = ((HwWifiServiceManagerImpl) hwWifiServiceManager).getHwWifiP2pService();
        if (wifiP2PServiceImpl == null || !(wifiP2PServiceImpl instanceof HwWifiP2pService)) {
            return WIFI_SETTING_CLOSED_AND_SERVICE_OPENED;
        }
        return ((HwWifiP2pService) wifiP2PServiceImpl).getWifiRepeaterFreq();
    }

    public int getRepeaterStatus() {
        int ret = WIFI_REPEATER_CLOSED;
        boolean isWifiRepeaterOpened = HWFLOW;
        boolean bRepeterTether = HWFLOW;
        HwWifiServiceManager hwWifiServiceManager = HwWifiServiceFactory.getHwWifiServiceManager();
        if (this.mWifiStateMachine != null) {
            isWifiRepeaterOpened = this.mWifiStateMachine.isWifiRepeaterStarted();
        }
        if (hwWifiServiceManager != null && (hwWifiServiceManager instanceof HwWifiServiceManagerImpl)) {
            WifiP2pServiceImpl wifiP2PServiceImpl = ((HwWifiServiceManagerImpl) hwWifiServiceManager).getHwWifiP2pService();
            if (wifiP2PServiceImpl != null && (wifiP2PServiceImpl instanceof HwWifiP2pService)) {
                bRepeterTether = ((HwWifiP2pService) wifiP2PServiceImpl).getWifiRepeaterTetherStarted();
            }
        }
        if (isWifiRepeaterOpened) {
            ret = WIFI_REPEATER_OPENED;
        }
        if (bRepeterTether) {
            return WIFI_REPEATER_TETHER;
        }
        return ret;
    }

    private void updateCHRConnInternetFailedType(String ucSubErrorCode) {
        if (this.mHwWifiStatStore != null && ucSubErrorCode != null) {
            this.mHwWifiStatStore.updateConnectInternetFailedType(ucSubErrorCode);
        }
    }
}
