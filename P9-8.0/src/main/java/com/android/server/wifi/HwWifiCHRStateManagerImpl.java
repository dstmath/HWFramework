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
import android.net.DhcpResults;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.p2p.HwWifiP2pService;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiProHistoryRecordManager;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.device.connectivitychrlog.CSubApRoaming;
import com.huawei.device.connectivitychrlog.CSubBTStatus;
import com.huawei.device.connectivitychrlog.CSubCPUInfo;
import com.huawei.device.connectivitychrlog.CSubCellID;
import com.huawei.device.connectivitychrlog.CSubDNS;
import com.huawei.device.connectivitychrlog.CSubMemInfo;
import com.huawei.device.connectivitychrlog.CSubNET_CFG;
import com.huawei.device.connectivitychrlog.CSubPacketCount;
import com.huawei.device.connectivitychrlog.CSubRSSIGROUP_EVENT;
import com.huawei.device.connectivitychrlog.CSubTCP_STATIST;
import com.huawei.device.connectivitychrlog.CSubWL_COUNTERS;
import com.huawei.device.connectivitychrlog.ChrLogBaseModel;
import com.huawei.ncdft.HwWifiDFTConnManager;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONException;
import org.json.JSONObject;

public class HwWifiCHRStateManagerImpl extends Handler implements HwWifiCHRStateManager {
    private static final long ACCESSWEB_SLOW_OR_FAILED_BETA_UPLOAD_COUNT = 30;
    private static final long ACCESSWEB_SLOW_OR_FAILED_BETA_UPLOAD_MIN = 600000;
    private static final long ACCESSWEB_SLOW_OR_FAILED_UPLOAD_COUNT = 20;
    private static final long ACCESSWEB_SLOW_OR_FAILED_UPLOAD_MIN = 1800000;
    private static final int ANT_LAST_SW_TIME = 2;
    private static final int ANT_MAIN_WORK_TIME = 8;
    private static final int ANT_SEC_WORK_TIME = 16;
    private static final String ANT_SWITCH_FAILED_REASON = "ant_sw_failed_reason";
    private static final int ANT_SW_COUNT = 4;
    private static final int ANT_WHICH_CUR_WORK = 1;
    private static final int ANT_WIFI_OPEN_TIME = 32;
    private static final int BROWSER_APP = 10001;
    private static final int CAMERA = 10007;
    private static final long COMM_UPLOAD_MIN_SPAN = 86400000;
    private static final long CONNECT_WIFI_EVENT_TIMEOUT_INTERVAL = 300000;
    private static final int DEFAULT = 10000;
    private static final int DELAY_GET_ANTS_SW_CNT_TIME = 86400000;
    public static final String DNS_ERR_MONITOR_FLAG = "hw.wifipro.dns_err_count";
    private static final int DNS_FAIL_TYPE = 6;
    private static final int EBOOK = 10003;
    private static final String EXTRA_WIFI_NO_INTERNET = "extra_wifipro_no_Internet";
    private static final int GAME_2D = 10011;
    private static final int GAME_3D = 10002;
    private static final int GET_ANTS_ALL_INFO_PARAM = -1;
    private static final String GET_ANTS_INFO_FAILED = "GET_ANTS_INFO_FAILED";
    private static final int GET_CONNECT_EVENT_DATA_WAIT_TIME = 1;
    protected static final boolean HWFLOW;
    private static final int MAX_CANT_CONNECT_FOR_LONG = 8;
    private static final int MAX_DISCONN_CNT = 6;
    private static final int MAX_UPLOAD_ACCESS_SLOWLY_PER_CONNECT_CNT = 2;
    private static final int MSS_REASON_CNT = 3;
    private static final String ROUTER_NAME_FAIL = "fail";
    private static final int SECURITY_EAP = 3;
    private static final int SECURITY_NONE = 0;
    private static final int SECURITY_PSK = 2;
    private static final int SECURITY_WAPI_CERT = 5;
    private static final int SECURITY_WAPI_PSK = 4;
    private static final int SECURITY_WEP = 1;
    private static final String SETTING_AND_WIFISERVICE_STATE_DIFFERENT_ACTION = "com.huawei.chr.wifi.action.SETTING_AND_WIFISERVICE_STATE_DIFFERENT";
    private static final int SSID_MAX_LENGTH = 32;
    private static final int START_HAL_FAILED_TIMEOUT = 5000;
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
    private static final int TX_BOOST_SIGNAL = 5;
    private static final int UPLOAD_HAL_DRIVER_EVENT_NUMBER_ONE_CYCLE = 3;
    private static final int UPLOAD_NUMBER_ONE_CYCLE = 50;
    private static final int USER_TYPE_BETA = 1;
    private static final int USER_TYPE_COMMERCIAL = 2;
    private static final int VIDEO = 10009;
    private static final int WIFI_CHR_DEVICE_ERROR_OFFSET = 15;
    private static final String WIFI_EVALUATE_TAG = "wifipro_recommending_access_points";
    private static final String WIFI_OPEN_CLOSE_FAILED_STATE = "wifi_open_close_failed_state";
    private static final String WIFI_PRO_INTERNET_ACCESS_CHANGE_ACTION = "huawei.wifi.pro.INTERNET_ACCESS_CHANGE";
    private static int WIFI_REPEATER_CLOSED = 0;
    private static int WIFI_REPEATER_OPENED = 1;
    private static int WIFI_REPEATER_TETHER = 2;
    private static final int WIFI_SECURITY_TYPE_UNKNOWN = -1;
    private static final int WIFI_SETTING_CLOSED_AND_SERVICE_OPENED = 0;
    private static final int WIFI_SETTING_OPENED_AND_SERVICE_CLOSED = 1;
    private static final int WIFI_SETTING_OPENED_TIMEOUT = 2;
    private static final byte WL_COUNRTS_VERSION = (byte) 11;
    private static Context mContextRef = null;
    private static int mWifiType = 15;
    private static HwWifiCHRStateManager sInstance = null;
    private long MIN_UPLOAD_CONNECT_FAIL_SPAN = 2000;
    private int MSS_DISCONNECT = 3;
    private int accessSlowlyerrrCode = 0;
    private short aucAbnormal_disconnect;
    private short aucAccess_internet_failed;
    private short aucClose_failed;
    private short aucDhcp_failed;
    private short aucOpen_failed;
    private short aucScan_failed;
    private boolean commercialUser = HwWifiDFTConnManager.getInstance().isCommercialUser();
    private ConnectivityManager connMgr = null;
    private boolean connect_event_flag = false;
    private int connectedType = 0;
    private boolean hadSentDnsReport = false;
    private HwWifiCHRService hwWifiCHRService;
    private int mABSReassFaileReason = -1;
    private int mABSState;
    private int mAC_FailType = 0;
    public short mAF_AutoLoginFailCnt = (short) 0;
    public short mAF_AutoLoginSuccCnt = (short) 0;
    public short mAF_FPNSuccNotMsmCnt = (short) 0;
    public short mAF_PasswordFailCnt = (short) 0;
    public short mAF_PasswordSuccCnt = (short) 0;
    public short mAF_PhoneNumFailCnt = (short) 0;
    public short mAF_PhoneNumSuccCnt = (short) 0;
    private String mAP1BSsid = "";
    private String mAP1Ssid = "";
    private String mAP2BSsid = "";
    private String mAP2Ssid = "";
    private String mAP3BSsid = "";
    private String mAP3Ssid = "";
    private String mAPBssid = "";
    private int mAPSecurity = -1;
    private String mAPSsid = "";
    private String mAPVendorInfo = "";
    private AccessWebStatus mAWS = new AccessWebStatus();
    private int mAbnorMalDisConnCnt = 0;
    private long mAbnorMalDisConnTimeStamp = 0;
    private int mAccessFailCount = 0;
    private int mAccessWebFailCnt = 0;
    private boolean mAccessWebFailFlag = false;
    private String mAccessWebFailSsid = "";
    private long mAccessWebFailTimeStamp = 0;
    private int mAccessWebSlowCount = 0;
    private int mAccessWebSlowlyCnt = 0;
    private long mAccessWebSlowlyTimeStamp = 0;
    private long mAccessWebSuccTmsp = 0;
    public short mActiveCheckRS_Diff = (short) 0;
    public short mActiveCheckRS_Same = (short) 0;
    private short mApStreamInfo = (short) 0;
    private int mApkAction = 0;
    private int mApkChangedCount = 0;
    private int mApkTriggerTimes = 0;
    private int mAssocFailCount = 0;
    private int mAuthFailCount = 0;
    private int mAuthSubCode = 0;
    private short mAutoCloseRootCause = (short) 0;
    private short mAutoOpenRootCause = (short) 0;
    private short mAutoOpenWhiteNum = (short) 0;
    private short mAutoRI_TotCount = (short) 0;
    private int mAutoRI_TotTime = 0;
    private int mAvgRssi = WifiHandover.INVALID_RSSI;
    private int mBG_AC_DiffType = 0;
    public short mBG_AUTH_FailCnt = (short) 0;
    public short mBG_AssocRejectCnt = (short) 0;
    public short mBG_BgRunCnt = (short) 0;
    public short mBG_ConntTimeoutCnt = (short) 0;
    public short mBG_DHCPFailCnt = (short) 0;
    public short mBG_DNSFailCnt = (short) 0;
    public short mBG_FailedCnt = (short) 0;
    public short mBG_FishingApCnt = (short) 0;
    public short mBG_FoundTwoMoreApCnt = (short) 0;
    public short mBG_FreeInetOkApCnt = (short) 0;
    public short mBG_FreeNotInetApCnt = (short) 0;
    public short mBG_InetNotOkActiveOk = (short) 0;
    public short mBG_InetOkActiveNotOk = (short) 0;
    public short mBG_NCByCheckFail = (short) 0;
    public short mBG_NCByConnectFail = (short) 0;
    public short mBG_NCByStateErr = (short) 0;
    public short mBG_NCByUnknown = (short) 0;
    public short mBG_PortalApCnt = (short) 0;
    public short mBG_SettingRunCnt = (short) 0;
    public short mBG_UserSelApFishingCnt = (short) 0;
    public short mBG_UserSelFreeInetOkCnt = (short) 0;
    public short mBG_UserSelNoInetCnt = (short) 0;
    public short mBG_UserSelPortalCnt = (short) 0;
    public short mBMD_FiftyMNotifyCount = (short) 0;
    public short mBMD_FiftyM_RI_Count = (short) 0;
    public short mBMD_TenMNotifyCount = (short) 0;
    public short mBMD_TenM_RI_Count = (short) 0;
    public short mBMD_UserDelNotifyCount = (short) 0;
    public short mBQE_BadSettingCancel = (short) 0;
    public short mBQE_BindWlanFailCount = (short) 0;
    public short mBQE_CNUrl1FailCount = (short) 0;
    public short mBQE_CNUrl2FailCount = (short) 0;
    public short mBQE_CNUrl3FailCount = (short) 0;
    public short mBQE_NCNUrl1FailCount = (short) 0;
    public short mBQE_NCNUrl2FailCount = (short) 0;
    public short mBQE_NCNUrl3FailCount = (short) 0;
    public short mBQE_ScoreUnknownCount = (short) 0;
    public short mBQE_StopBqeFailCount = (short) 0;
    public short mBSG_EndIn4s7sCnt = (short) 0;
    public short mBSG_EndIn4sCnt = (short) 0;
    public short mBSG_NotEndIn7sCnt = (short) 0;
    public short mBSG_RsBadCnt = (short) 0;
    public short mBSG_RsGoodCnt = (short) 0;
    public short mBSG_RsMidCnt = (short) 0;
    private byte mBandWidth = (byte) -1;
    public short mBigRTT_ErrRO_Tot = (short) 0;
    public short mBigRTT_RO_Tot = (short) 0;
    private short mCellAutoCloseCount = (short) 0;
    private short mCellAutoOpenCount = (short) 0;
    private byte mCheckReason = (byte) 0;
    private ArrayList<ChrLogBaseModel> mChrLogBaseModelList = null;
    private String mCloseErrorCode = "UNKNOWN";
    private String mCodeInputId = "";
    private int mConnFailCnt = 0;
    private long mConnFailTimeStamp = 0;
    private int mConnectFailedReason = 0;
    private int mConnectFailedSubErrorCode = 0;
    private long mConnectSuccessTime = 0;
    private String mConnectThreadName = "";
    private String mConnectType = "";
    private HwCHRWifiLinkMonitor mCounter_monitor = HwCHRWifiLinkMonitor.getDefault();
    private String mCountryCode = "";
    private short mCreditScoreRO_Rate = (short) 0;
    private String mCurrentApBssid = "";
    private int mCurrentApSecurity = -1;
    private String mCurrentMsgIface = "";
    private long mDailyUploadTime = 0;
    private int mDhcpFailCount = 0;
    private int mDiffFreqStationRepeaterDuration = 0;
    private String mDisableThreadName = "";
    private int mDisconnTotalCount = 0;
    private int mDisconnectCode;
    private int[] mDisconnectFail = new int[6];
    private boolean mDualAntsChr = false;
    private int mEnableTotTime = 0;
    private int mEssCount = 0;
    private int mFailReason = 0;
    private long mFirstAccessWebFailTimeStamp = 0;
    private long mFirstAccessWebSlowlyTimeStamp = 0;
    private long mFirstMssReportTimeStamp;
    private String mFreeAPcellID = "";
    private int mH110XDevErrCnt = 0;
    private long mH110XDevErrTimeStamp = 0;
    private int mHTML_Input_Number = 0;
    private short mHighDataRateRO_Rate = (short) 0;
    public short mHighDataRateStopROC = (short) 0;
    private short mHisScoRI_Count = (short) 0;
    private short mHistoryQuilityRO_Rate = (short) 0;
    public int mHistoryTotWifiConnHour = 0;
    public short mHomeAPAddRoPeriodCnt = (short) 0;
    private int mHomeAPJudgeTime = 0;
    public short mHomeAPQoeBadCnt = (short) 0;
    private List<HwCHRAccessNetworkEventInfo> mHwCHRAccessNetworkEventInfoList = new ArrayList();
    private HwWiFiLogUtils mHwLogUtils = null;
    private HwWifiDFTConst mHwWifiDFTConst;
    private HwWifiDFTUtilImpl mHwWifiDFTUtilImpl = null;
    private HwWifiStatStore mHwWifiStatStore;
    private short mIPQLevel = (short) 0;
    private int mIpType = 0;
    private long mIp_leasetime = 0;
    private short mIsAPOpen = (short) 0;
    private boolean mIsHiddenSsid = false;
    private int mIsPortalConnection = 0;
    private String mLastApkName = "";
    private long mLastConnectFailTimestamp = 0;
    private int mLastNetIdFromUI = -1;
    private BluetoothAdapter mLocalBluetoothAdapter = null;
    private ReentrantLock mLock = new ReentrantLock();
    private Object mLockObj = null;
    private short mManualBackROC = (short) 0;
    public short mManualConnBlockPortalCount = (short) 0;
    private int mManualRI_TotTime = 0;
    private short mMobileSignalLevel = (short) 0;
    private int mMssReportCnt;
    private String mMssScene;
    private int mMssTemperLevel;
    private int mMssTemperature;
    private int mMssTriggerReason;
    private int mMssTxBad;
    private int mMssTxGood;
    private int mMultiGWCount = 0;
    private int mNetChangeInterval = 0;
    private int mNetNormalTime = 0;
    private int mNetSlowlyTime = 0;
    public short mNoInetAlarmCount = (short) 0;
    public short mNoInetAlarmOnConnCnt = (short) 0;
    private short mNoInetHandoverCount = (short) 0;
    public short mNotAutoConnPortalCnt = (short) 0;
    public short mNotInetRO_DISCONNECT_Cnt = (short) 0;
    public int mNotInetRO_DISCONNECT_TotData = 0;
    public short mNotInetRestoreRI = (short) 0;
    public short mNotInetSettingCancel = (short) 0;
    public short mNotInetUserCancel = (short) 0;
    public short mNotInetUserManualRI = (short) 0;
    public short mNotInetWifiToWifiCount = (short) 0;
    public int mNotInet_AutoRI_TotData = 0;
    private short mOTA_ErrRO_Tot = (short) 0;
    private short mOTA_PacketDropRate = (short) 0;
    private short mOTA_RO_Tot = (short) 0;
    private long mOpenClodeTimeStamp = 0;
    private int mOpenCloseCnt = 0;
    private String mOpenErrorCode = "UNKNOWN";
    private String mPhoneInputId = "";
    public short mPingPongCount = (short) 0;
    private String mPortalAPBssid = "";
    private byte[] mPortalAPKeyLines = new byte[1];
    private byte[] mPortalAPSsid = new byte[1];
    private short mPortalAutoLoginCount = (short) 0;
    private String mPortalCellId = "";
    private short mPortalCodeParseCount = (short) 0;
    public short mPortalNoAutoConnCnt = (short) 0;
    private int mPortalStatus = 0;
    private short mPortalUnauthCount = (short) 0;
    private int mPrevStaChannel = 0;
    private String mPreviousConnectEventSSID = "";
    private byte mPublicEssCnt = (byte) 0;
    public int mQOE_AutoRI_TotData = 0;
    public short mQOE_RO_DISCONNECT_Cnt = (short) 0;
    public int mQOE_RO_DISCONNECT_TotData = 0;
    private short mRATType = (short) 0;
    private String mRO_APSsid = "";
    private short mRO_Duration = (short) 0;
    public int mRO_TotMobileData = 0;
    private short mRSSI_BetterRI_Count = (short) 0;
    private short mRSSI_ErrRO_Tot = (short) 0;
    private short mRSSI_RO_Tot = (short) 0;
    private short mRSSI_RestoreRI_Count = (short) 0;
    private short mRSSI_VALUE = (short) 0;
    private short mRcvSMS_Count = (short) 0;
    private int mReasonCode;
    public short mReopenWifiRICount = (short) 0;
    private int mRepeterConnFailedCount = 0;
    private int mRepeterDiffBegin = 0;
    private int mRepeterDiffEnd = 0;
    private int mRepeterMaxClientCount = 0;
    private int mRepeterOpenOrClose = -1;
    private String mRouterBrand = "";
    private String mRouterModel = "";
    private int mRssi = WifiHandover.INVALID_RSSI;
    private int mRssiCnt = 0;
    private HwCHRWifiRSSIGroupSummery mRssiGroup = new HwCHRWifiRSSIGroupSummery();
    private int mRssiSum = 0;
    private short mRttAvg = (short) 0;
    private int mSMS_Body_Len = 0;
    private int mScanFailCount = 0;
    private Object mScanResultLock = new Object();
    private List<ScanResult> mScanResults = new ArrayList();
    private int mScreenState = 1;
    public short mSelCSPAutoSwCount = (short) 0;
    public short mSelCSPNotSwCount = (short) 0;
    public short mSelCSPShowDiglogCount = (short) 0;
    public short mSelectNotInetAPCount = (short) 0;
    private byte[] mSms_Body = new byte[1];
    private String mSms_Num = "";
    private String mSndBtnId = "";
    private int mSsid_Len = 0;
    private int mStatIntervalTime = 0;
    private int mSubcodeReject = 0;
    private String mSubmitBtnId = "";
    private int mSwitchType;
    private short mTCP_ErrRO_Tot = (short) 0;
    private short mTCP_RO_Tot = (short) 0;
    private short mTcpInSegs = (short) 0;
    private short mTcpOutSegs = (short) 0;
    private short mTcpRetransSegs = (short) 0;
    private TelephonyManager mTelephonyManager;
    private long mTimeStampSessionFinish = 0;
    private long mTimeStampSessionFirstConnect = 0;
    private long mTimeStampSessionStart = 0;
    private Timer mTimer = null;
    private short mTimerRI_Count = (short) 0;
    public short mTotAPRecordCnt = (short) 0;
    public short mTotBtnRICount = (short) 0;
    public short mTotHomeAPCnt = (short) 0;
    public int mTotWifiConnectTime = 0;
    private short mTotalBQE_BadROC = (short) 0;
    public short mTotalPortalAuthSuccCount = (short) 0;
    public short mTotalPortalConnCount = (short) 0;
    private int mTriggerReportType = 0;
    private int mUploadAccessWebSlowly = 0;
    private String mUserAction = "";
    private short mUserCancelROC = (short) 0;
    public short mUserUseBgScanAPCount = (short) 0;
    private short mWIFI_NetSpeed = (short) 0;
    private String mWebUrl = "";
    private int mWifiAntsPrevSWCnt = 0;
    private int mWifiConnentEventCount = 0;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    public short mWifiOobInitState = (short) 0;
    private long mWifiProFreeAPUploadTime = 0;
    private WifiProHistoryRecordManager mWifiProHistoryRecordManager;
    private int mWifiRepeaterOpenedCount = 0;
    private long mWifiRepeaterWorkingTime = 0;
    private short mWifiScoCount = (short) 0;
    private final BroadcastReceiver mWifiSettingStateErrorReceiver = new BroadcastReceiver() {
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
                            HwWifiCHRStateManagerImpl.this.networkInfoWlan = HwWifiCHRStateManagerImpl.this.connMgr.getNetworkInfo(1);
                            HwWifiCHRStateManagerImpl.this.networkInfoMobile = HwWifiCHRStateManagerImpl.this.connMgr.getNetworkInfo(0);
                            if (!(HwWifiCHRStateManagerImpl.this.networkInfoWlan == null || HwWifiCHRStateManagerImpl.this.networkInfoMobile == null)) {
                                if (HwWifiCHRStateManagerImpl.this.networkInfoWlan.isConnected()) {
                                    HwWifiCHRStateManagerImpl.this.connectedType = 1;
                                    HwWifiCHRStateManagerImpl.this.getApVendorInfo();
                                } else if (HwWifiCHRStateManagerImpl.this.networkInfoMobile.isConnected()) {
                                    HwWifiCHRStateManagerImpl.this.connectedType = 2;
                                } else {
                                    HwWifiCHRStateManagerImpl.this.connectedType = 0;
                                }
                            }
                            if (HwWifiCHRStateManagerImpl.this.mHwWifiStatStore != null) {
                                HwWifiCHRStateManagerImpl.this.mHwWifiStatStore.updateCurrentConnectType(HwWifiCHRStateManagerImpl.this.connectedType);
                            }
                            HwWifiCHRStateManagerImpl.this.getDnsErrorCnt(HwWifiCHRStateManagerImpl.this.mdnsWifiStartFailCnt);
                            HwWifiCHRStateManagerImpl.this.hadSentDnsReport = false;
                        }
                    }
                    if (HwWifiCHRStateManagerImpl.SETTING_AND_WIFISERVICE_STATE_DIFFERENT_ACTION.equals(action)) {
                        int flag = intent.getIntExtra(HwWifiCHRStateManagerImpl.WIFI_OPEN_CLOSE_FAILED_STATE, -1);
                        if (flag != -1) {
                            switch (flag) {
                                case 0:
                                    HwWifiCHRStateManagerImpl.this.updateWifiException(80, "WIFI_SETTING_CLOSED_AND_SERVICE_OPENED");
                                    break;
                                case 1:
                                    HwWifiCHRStateManagerImpl.this.updateWifiException(81, "WIFI_SETTING_OPENED_AND_SERVICE_CLOSED");
                                    break;
                                case 2:
                                    HwWifiCHRStateManagerImpl.this.updateWifiException(80, "WIFI_SETTINGS_TIMEOUT");
                                    break;
                            }
                        }
                        return;
                    }
                    if (HwWifiCHRStateManagerImpl.WIFI_PRO_INTERNET_ACCESS_CHANGE_ACTION.equals(action) && HwWifiCHRStateManagerImpl.this.connectedType == 1) {
                        boolean wifiNoInternet = intent.getBooleanExtra(HwWifiCHRStateManagerImpl.EXTRA_WIFI_NO_INTERNET, true);
                        Log.d(HwWifiCHRStateManagerImpl.TAG, "WIFI_PRO_INTERNET_ACCESS_CHANGE_ACTION action get ,wifiNoInternet : " + wifiNoInternet + " ,is connect to wifi:" + HwWifiCHRStateManagerImpl.this.connectedType + " ,ssid is:" + HwWifiCHRStateManagerImpl.this.strAp_Ssid);
                        if (HwWifiCHRStateManagerImpl.this.mAccessWebFailFlag && (wifiNoInternet ^ 1) != 0 && HwWifiCHRStateManagerImpl.this.mAccessWebFailSsid.equals(HwWifiCHRStateManagerImpl.this.strAp_Ssid) && (HwWifiCHRStateManagerImpl.this.isWifiProEvaluate() ^ 1) != 0) {
                            Log.d(HwWifiCHRStateManagerImpl.TAG, "upload WIFI_ACCESS_INTERNET_FAILED event for RESUME_INTERNET,ssid:" + HwWifiCHRStateManagerImpl.this.strAp_Ssid);
                            HwWifiCHRStateManagerImpl.this.updateWifiException(87, "RESUME_INTERNET");
                        }
                        if (!wifiNoInternet) {
                            HwWifiCHRStateManagerImpl.this.mAccessWebSuccTmsp = SystemClock.elapsedRealtime();
                        }
                    }
                }
            }
        }
    };
    private HwWifiStateMachine mWifiStateMachine = null;
    public short mWifiToWifiSuccCount = (short) 0;
    public short mWifiproCloseCount = (short) 0;
    public short mWifiproOpenCount = (short) 0;
    public short mWifiproStateAtReportTime = (short) 0;
    private int mWorkaroundCount = 0;
    private int[] mdnsLastReportFailCnt = new int[6];
    private int[] mdnsWifiStartFailCnt = new int[6];
    private String mgameName = "";
    private int mgameRTT = 0;
    private int mgameTcpRTT = 0;
    private short misAPRecognized = (short) 0;
    private boolean misWifiProEvalu = false;
    private int mlteEarfcn = -1;
    private int mlterssi = 0;
    private NetworkInfo networkInfoMobile = null;
    private NetworkInfo networkInfoWlan = null;
    private String strAP_auth_alg = "";
    private String strAP_eap = "";
    private String strAP_gruop = "";
    private String strAP_key_mgmt = "";
    private String strAP_pairwise = "";
    private String strAP_proto = "";
    private String strAp_Ssid = "";
    private String strAp_mac = "";
    private String strRoutes = "";
    private String strSpeedInfo = "";
    private String strSta_mac = "";
    private String strUIDSpeedInfo = "";
    private String str_Wifi_ip = "";
    private String str_Wifi_ip_org = "";
    private String str_dns = "";
    private String str_gate_ip = "";
    private String str_gate_ip_org = "";
    private short usAP_channel = (short) 0;
    private short usLinkSpeed = (short) 0;
    private WifiAntsStatus wifiAntsStatus = new WifiAntsStatus();
    private int wifiproCanotConnectForLongCount = 0;

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
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 4);
        } else {
            z = false;
        }
        HWFLOW = z;
    }

    public static synchronized void init(Context context) {
        synchronized (HwWifiCHRStateManagerImpl.class) {
            if (context != null) {
                if (mContextRef == null) {
                    if (HWFLOW) {
                        Log.d(TAG, "HwWifiCHRStateManagerImpl init");
                    }
                    mContextRef = context;
                    if (sInstance == null) {
                        HandlerThread thread = new HandlerThread(TAG);
                        thread.start();
                        sInstance = new HwWifiCHRStateManagerImpl(thread.getLooper());
                    }
                }
            }
        }
    }

    private HwWifiCHRStateManagerImpl(Looper looper) {
        super(looper);
        if (HWFLOW) {
            Log.d(TAG, "HwWifiCHRStateManagerImpl construct");
        }
        this.mLocalBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHwWifiStatStore = HwWifiStatStoreImpl.getDefault();
        this.mTelephonyManager = (TelephonyManager) mContextRef.getSystemService("phone");
        this.mHwWifiDFTConst = new HwWifiDFTConst();
        this.mHwLogUtils = HwWiFiLogUtils.getDefault();
        this.mHwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SETTING_AND_WIFISERVICE_STATE_DIFFERENT_ACTION);
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction(WIFI_PRO_INTERNET_ACCESS_CHANGE_ACTION);
        mContextRef.registerReceiver(this.mWifiSettingStateErrorReceiver, intentFilter);
    }

    public void updateChannel(int channel) {
        Message msg = new Message();
        msg.what = HwWifiLogMsgID.EVENT_UPDATE_CHANNEL;
        msg.arg1 = channel;
        sendMessage(msg);
    }

    public void syncSetScanResultsList(List<ScanDetail> scanList) {
        Message msg = new Message();
        msg.what = HwWifiLogMsgID.EVENT_SET_SCAN_RESULT;
        msg.obj = scanList;
        sendMessage(msg);
    }

    public void uploadDisconnectException(int reasoncode) {
        Message msg = new Message();
        msg.what = HwWifiLogMsgID.EVENT_UPLOAD_DISCONNECT;
        msg.arg1 = reasoncode;
        sendMessage(msg);
    }

    public void updateAP(String iface, int networkId, SupplicantState newSupplicantState, String BSSID) {
        Message msg = new Message();
        ArrayList list = new ArrayList();
        list.clear();
        list.add(iface);
        list.add(Integer.valueOf(networkId));
        list.add(newSupplicantState);
        list.add(BSSID);
        msg.what = HwWifiLogMsgID.EVENT_UPDATE_AP;
        msg.obj = list;
        sendMessage(msg);
    }

    public void checkAppName(WifiConfiguration config, Context mContext) {
        Message msg = new Message();
        ArrayList list = new ArrayList();
        list.clear();
        list.add(config);
        list.add(mContext);
        msg.what = HwWifiLogMsgID.EVENT_CHECK_APP_NAME;
        msg.obj = list;
        sendMessage(msg);
    }

    public void updateLTECoexInfo() {
        List<CellInfo> cellInfoList = this.mTelephonyManager.getAllCellInfo();
        if (cellInfoList == null) {
            Log.e(TAG, "cellInfoList is NULL");
            return;
        }
        for (CellInfo cellInfo : cellInfoList) {
            if ((cellInfo instanceof CellInfoLte) && cellInfo.isRegistered()) {
                this.mlteEarfcn = ((CellInfoLte) cellInfo).getCellIdentity().getEarfcn();
                this.mlterssi = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
                Log.d(TAG, "mlteEarfcn = " + this.mlteEarfcn + ", mlterssi = " + this.mlterssi);
            }
        }
    }

    public void handleIPv4SuccessException(DhcpResults dhcpResults) {
        Message msg = new Message();
        ArrayList list = new ArrayList();
        list.clear();
        list.add(dhcpResults);
        msg.what = 1010;
        msg.obj = list;
        sendMessage(msg);
    }

    public void handleSupplicantException() {
        Message msg = new Message();
        msg.what = HwWifiLogMsgID.EVENT_SUPPLICANT_EXCEPTION;
        sendMessage(msg);
    }

    public void uploadDFTEvent(int type, String ucSubErrorCode) {
        Message msg = new Message();
        ArrayList list = new ArrayList();
        list.add(Integer.valueOf(type));
        list.add(ucSubErrorCode);
        msg.what = HwWifiLogMsgID.EVENT_UPLOAD_DFT;
        msg.obj = list;
        sendMessage(msg);
    }

    public void notifyNcSTATDftEvent() {
        Message msg = new Message();
        msg.what = HwWifiLogMsgID.EVENT_NOTIFY_NC_STAT;
        sendMessage(msg);
    }

    public void reportHwCHRAccessNetworkEventInfoList(int ReportReason) {
        removeMessages(HwWifiLogMsgID.EVENT_CONNECT_WIFI_UPLOAD_TIMEOUT);
        Message msg = new Message();
        msg.arg1 = ReportReason;
        msg.what = HwWifiLogMsgID.EVENT_ACCESS_NET_INFO;
        sendMessage(msg);
    }

    public void updateMSSCHR(int switchType, int absState, int reasonCode, ArrayList paramList) {
        Message msg = new Message();
        ArrayList list = new ArrayList();
        list.add(Integer.valueOf(switchType));
        list.add(Integer.valueOf(absState));
        list.add(Integer.valueOf(reasonCode));
        list.add(paramList);
        msg.obj = list;
        msg.what = HwWifiLogMsgID.EVENT_UPLOAD_MSS;
        sendMessage(msg);
    }

    private void getApVendorInfo() {
        Message msg = new Message();
        msg.what = HwWifiLogMsgID.EVENT_UPLOAD_APVENDORINFO;
        sendMessage(msg);
    }

    public void handleMessage(Message msg) {
        if (msg != null) {
            Log.d(TAG, "handleMessage : " + msg.what);
            ArrayList list;
            switch (msg.what) {
                case HwWifiLogMsgID.EVENT_UPDATE_CHANNEL /*1001*/:
                    handleUpdateChannel(msg.arg1);
                    break;
                case HwWifiLogMsgID.EVENT_UPLOAD_MSS /*1002*/:
                    list = (ArrayList) msg.obj;
                    handleUpdateMSSCHR(((Integer) list.get(0)).intValue(), ((Integer) list.get(1)).intValue(), ((Integer) list.get(2)).intValue(), (ArrayList) list.get(3));
                    break;
                case HwWifiLogMsgID.EVENT_UPLOAD_APVENDORINFO /*1003*/:
                    updateApVendorInfo();
                    break;
                case HwWifiLogMsgID.EVENT_SET_SCAN_RESULT /*1005*/:
                    handleSyncSetScanResultsList((List) msg.obj);
                    break;
                case HwWifiLogMsgID.EVENT_UPLOAD_DISCONNECT /*1006*/:
                    handleUploadDisconnectException(msg.arg1);
                    break;
                case HwWifiLogMsgID.EVENT_UPDATE_AP /*1007*/:
                    list = msg.obj;
                    handleUpdateAP((String) list.get(0), ((Integer) list.get(1)).intValue(), (SupplicantState) list.get(2), (String) list.get(3));
                    break;
                case HwWifiLogMsgID.EVENT_CHECK_APP_NAME /*1008*/:
                    list = (ArrayList) msg.obj;
                    handleCheckAppName((WifiConfiguration) list.get(0), (Context) list.get(1));
                    break;
                case 1010:
                    handleIpv4SuccessException((DhcpResults) ((ArrayList) msg.obj).get(0));
                    break;
                case HwWifiLogMsgID.EVENT_SUPPLICANT_EXCEPTION /*1011*/:
                    handleSupplicantExceptions();
                    break;
                case HwWifiLogMsgID.EVENT_UPLOAD_DFT /*1014*/:
                    list = (ArrayList) msg.obj;
                    handleUploadDFTEvent(((Integer) list.get(0)).intValue(), (String) list.get(1));
                    break;
                case HwWifiLogMsgID.EVENT_NOTIFY_NC_STAT /*1015*/:
                    handleNotifyNcSTATDftEvent();
                    break;
                case HwWifiLogMsgID.EVENT_ACCESS_NET_INFO /*1017*/:
                    handleReportHwCHRAccessNetworkEventInfoList(msg.arg1);
                    break;
                case HwWifiLogMsgID.EVENT_CONNECT_WIFI_UPLOAD_TIMEOUT /*1018*/:
                    handleReportHwCHRAccessNetworkEventInfoList(8);
                    break;
                case HwWifiLogMsgID.EVENT_START_HAL_FAILED /*1019*/:
                    writeNETInfo(80, "START_HAL_FAILED");
                    break;
                default:
                    Log.e(TAG, "cannot handle the message" + msg.what);
                    break;
            }
        }
    }

    public void setIsDualAntsChr(boolean isAntsChr) {
        this.mDualAntsChr = isAntsChr;
    }

    public static HwWifiCHRStateManager getDefault() {
        return sInstance;
    }

    public static HwWifiCHRStateManagerImpl getDefaultImpl() {
        return (HwWifiCHRStateManagerImpl) sInstance;
    }

    public void isHiddenSsid(boolean isHidden) {
        this.mIsHiddenSsid = isHidden;
    }

    public void updateScreenState(boolean on) {
        this.mScreenState = on ? 1 : 0;
        Log.d(TAG, "handlUpdateScreenState screen on is  " + this.mScreenState);
        if (this.mScreenState == 1) {
            updateTimeStampSessionStart(SystemClock.elapsedRealtime());
        }
    }

    private void updateApVendorInfo() {
        this.mAPVendorInfo = "UNKNOWN";
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        if (this.mWifiNative != null) {
            String apvendorinfo = this.mWifiNative.getApVendorInfo();
            if (apvendorinfo != null) {
                this.mAPVendorInfo = apvendorinfo;
                if (this.mHwWifiStatStore != null) {
                    this.mHwWifiStatStore.setApVendorInfo(this.mAPVendorInfo);
                }
                Log.d(TAG, "this ssid: " + this.strAp_Ssid + "  ap vendor info is:" + this.mAPVendorInfo);
            }
        }
    }

    public void handleUpdateChannel(int channel) {
        Log.d(TAG, "handleUpdateChannel:" + channel);
        this.usAP_channel = (short) channel;
        int repeaterStatus = getRepeaterStatus();
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.setApFreqParam(this.usAP_channel);
        }
        if (repeaterStatus == WIFI_REPEATER_TETHER) {
            int wifiRepeaterFreq = getRepeaterFreq();
            if (wifiRepeaterFreq != 0 && this.mPrevStaChannel != 0 && channel != this.mPrevStaChannel && channel != wifiRepeaterFreq) {
                this.mRepeterDiffBegin = Integer.parseInt(String.valueOf(SystemClock.elapsedRealtime() / 1000));
            } else if (!(channel == this.mPrevStaChannel || channel != wifiRepeaterFreq || channel == 0)) {
                this.mRepeterDiffEnd = Integer.parseInt(String.valueOf(SystemClock.elapsedRealtime() / 1000));
                this.mDiffFreqStationRepeaterDuration += this.mRepeterDiffEnd - this.mRepeterDiffBegin;
                this.mRepeterDiffEnd = 0;
                this.mRepeterDiffBegin = 0;
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
            if (this.mAvgRssi >= 0 || this.mAvgRssi <= -110) {
                this.mAvgRssi = rssi;
            } else {
                this.mAvgRssi = (this.mAvgRssi + rssi) / 2;
            }
        }
        this.mRssi = rssi;
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.setRssi(rssi);
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

    private void updateWifiIp(String ip) {
        if (ip == null) {
            ip = "";
        }
        this.str_Wifi_ip = ip;
        this.str_Wifi_ip_org = this.str_Wifi_ip;
        Log.d(TAG, "update wifiIP");
    }

    private void updateAPMAC(String AP_MAC) {
        String str;
        if (AP_MAC == null) {
            str = "";
        } else {
            str = AP_MAC;
        }
        this.strAp_mac = str;
        Log.d(TAG, "Update AP_MAC");
        this.mCurrentApBssid = this.strAp_mac;
        this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (!(this.hwWifiCHRService == null || (TextUtils.isEmpty(this.strAp_mac) ^ 1) == 0)) {
            this.hwWifiCHRService.updateTargetBssid(this.strAp_mac);
        }
        if (this.mHwWifiStatStore != null && isValidHardwareAddr(AP_MAC)) {
            this.mHwWifiStatStore.setApMac(AP_MAC);
        }
        this.mEssCount = (byte) (getRelatedApInfo(this.strAp_mac) >> 16);
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
        } else if (ssid.length() >= 32) {
            this.strAp_Ssid = ssid.substring(0, 31);
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

    private void updateGateWay(String gateWay) {
        if (gateWay == null) {
            gateWay = "";
        }
        this.str_gate_ip = gateWay;
        this.str_gate_ip_org = this.str_gate_ip;
    }

    private void updateDNS(Collection<InetAddress> dnses) {
        StringBuilder sBuilder_dns = new StringBuilder();
        if (dnses != null && dnses.size() > 0) {
            for (InetAddress addr : dnses) {
                sBuilder_dns.append(addr.getHostAddress()).append(";");
            }
            if (sBuilder_dns.length() > 0) {
                this.str_dns = sBuilder_dns.deleteCharAt(sBuilder_dns.length() - 1).toString();
            }
        }
    }

    private void updateStrucRoutes(String strucRoutes) {
        if (strucRoutes == null) {
            strucRoutes = "";
        }
        this.strRoutes = strucRoutes;
        Log.d(TAG, "updateStrucRoutes:" + this.strRoutes);
    }

    public void updateLeaseIP(long leaseTime) {
        this.mIp_leasetime = leaseTime;
        Log.d(TAG, "updateLeaseTime:" + String.valueOf(leaseTime));
    }

    private WifiConfiguration getCurrentConfig(int networkId) {
        WifiConfiguration result = null;
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            return null;
        }
        for (WifiConfiguration nextConfig : configNetworks) {
            if (networkId == nextConfig.networkId) {
                result = nextConfig;
                break;
            }
        }
        return result;
    }

    private void updateApMessage() {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) mContextRef.getSystemService("wifi");
            if (WifiProCommonUtils.isWifiProPropertyEnabled()) {
                this.mWifiProHistoryRecordManager = WifiProHistoryRecordManager.getInstance(mContextRef, this.mWifiManager);
            }
        }
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            Log.d(TAG, "wifiInfo is null, return");
            return;
        }
        WifiConfiguration config = getCurrentConfig(wifiInfo.getNetworkId());
        if (config != null) {
            switch (config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType()) {
                case 0:
                    this.strAP_key_mgmt = "NONE";
                    break;
                case 1:
                    this.strAP_key_mgmt = "WPA_PSK";
                    break;
                case 2:
                    this.strAP_key_mgmt = "WPA_EAP";
                    break;
                case 3:
                    this.strAP_key_mgmt = "IEEE8021X";
                    break;
                case 4:
                    this.strAP_key_mgmt = "WPA2_PSK";
                    break;
                case 5:
                    this.strAP_key_mgmt = "OSEN";
                    break;
                case 6:
                    this.strAP_key_mgmt = "FT_PSK";
                    break;
                case 7:
                    this.strAP_key_mgmt = "FT_EAP";
                    break;
                default:
                    this.strAP_key_mgmt = "";
                    break;
            }
        }
        if (!(config == null || config.enterpriseConfig == null)) {
            switch (config.enterpriseConfig.getEapMethod()) {
                case -1:
                    this.strAP_eap = "NONE";
                    break;
                case 0:
                    this.strAP_eap = "PEAP";
                    break;
                case 1:
                    this.strAP_eap = "TLS";
                    break;
                case 2:
                    this.strAP_eap = "TTLS";
                    break;
                case 3:
                    this.strAP_eap = "PWD";
                    break;
                case 4:
                    this.strAP_eap = "SIM";
                    break;
                case 5:
                    this.strAP_eap = "AKA";
                    break;
                case 6:
                    this.strAP_eap = "AKA_PRIME";
                    break;
                case 7:
                    this.strAP_eap = "UNAUTH_TLS";
                    break;
                default:
                    this.strAP_eap = "";
                    break;
            }
        }
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.setApencInfo(this.strAP_key_mgmt, this.strAP_eap);
        }
        Log.d(TAG, "updateApMessage strAP_key_mgmt" + this.strAP_key_mgmt + ", strAP_eap" + this.strAP_eap);
    }

    public void updateRouterModelInfo(String routerModel, String routerBrand, boolean uploadDFT) {
        if (routerModel == null) {
            routerModel = "";
        }
        this.mRouterModel = routerModel;
        if (routerBrand == null) {
            routerBrand = "";
        }
        this.mRouterBrand = routerBrand;
        if (this.mRouterModel == null || (this.mRouterModel.equals(ROUTER_NAME_FAIL) ^ 1) == 0) {
            this.misAPRecognized = (short) 0;
        } else {
            this.misAPRecognized = (short) 1;
        }
        if (uploadDFT) {
            writeNETInfo(213, "");
        }
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.setRouterModelParam(this.mRouterModel);
        }
        Log.d(TAG, "updateRouterModelInfo mRouterModel = " + this.mRouterModel + " mRouterBrand = " + this.mRouterBrand);
    }

    public void updateWifiException(int ucErrorCode, String ucSubErrorCode) {
        Log.d(TAG, "errorcode " + ucErrorCode);
        if (!isWifiConnectTypeEvent(ucErrorCode) || (isWifiProEvaluate() ^ 1) == 0 || (this.mConnectType.equals("REASSOC") ^ 1) == 0 || (this.mConnectType.equals("ROAM_CONNECT") ^ 1) == 0) {
            if (ucErrorCode == 87 && this.mHwWifiStatStore != null) {
                if ("RESUME_INTERNET".equals(ucSubErrorCode)) {
                    this.mAccessWebFailFlag = false;
                } else {
                    this.mAccessWebFailSsid = this.strAp_Ssid;
                    if (!("FIRST_CONNECT_NO_INTERNET".equals(ucSubErrorCode) || this.mAccessWebSuccTmsp == 0)) {
                        this.mNetChangeInterval = (int) (SystemClock.elapsedRealtime() - this.mAccessWebSuccTmsp);
                    }
                    this.mAccessWebFailFlag = true;
                    this.mAccessWebSuccTmsp = 0;
                }
                this.mHwWifiStatStore.setAccessWebFlag(ucSubErrorCode);
            }
            if (ucErrorCode == 213) {
                try {
                    JSONObject jsonStr = new JSONObject(ucSubErrorCode);
                    try {
                        this.mApStreamInfo = (short) jsonStr.getInt(HwWifiStateMachine.AP_CAP_KEY);
                        JSONObject jSONObject = jsonStr;
                    } catch (JSONException e) {
                        Log.e(TAG, "ApStreamInfo:JSON parse failed");
                        return;
                    }
                } catch (JSONException e2) {
                    Log.e(TAG, "ApStreamInfo:JSON parse failed");
                    return;
                }
                return;
            }
            if (ucErrorCode == 80 || ucErrorCode == 81) {
                boolean enable = ucErrorCode == 80;
                this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
                if (this.hwWifiCHRService != null) {
                    this.hwWifiCHRService.removeOpenCloseMsg(enable);
                }
                if (ucSubErrorCode.equals("START_HAL_FAILED")) {
                    removeMessages(HwWifiLogMsgID.EVENT_START_HAL_FAILED);
                    sendEmptyMessageDelayed(HwWifiLogMsgID.EVENT_START_HAL_FAILED, 5000);
                    return;
                }
            }
            writeNETInfo(ucErrorCode, ucSubErrorCode);
            return;
        }
        this.mPreviousConnectEventSSID = this.strAp_Ssid;
        syncAddHwCHRAccessNetworkEventInfoList(constructHwCHRAccessNetworkEventInfo(ucErrorCode, ucSubErrorCode));
        removeMessages(HwWifiLogMsgID.EVENT_CONNECT_WIFI_UPLOAD_TIMEOUT);
        sendEmptyMessageDelayed(HwWifiLogMsgID.EVENT_CONNECT_WIFI_UPLOAD_TIMEOUT, CONNECT_WIFI_EVENT_TIMEOUT_INTERVAL);
    }

    private boolean isWifiConnectTypeEvent(int ucErrorCode) {
        if (ucErrorCode == 82 || ucErrorCode == 84 || ucErrorCode == 83) {
            return true;
        }
        return false;
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

    public void updateGameBoostLag(String reasoncode, String gameName, int gameRTT, int TcpRtt) {
        Log.d(TAG, "updateGameBoostLag upload WIFI_ACCESS_WEB_SLOWLY event gameName " + gameName + ", gameRTT " + gameRTT + ", TcpRtt" + TcpRtt);
        this.mgameName = gameName;
        this.mgameRTT = gameRTT;
        this.mgameTcpRTT = TcpRtt;
        if (reasoncode != null && !reasoncode.isEmpty()) {
            updateWifiException(102, reasoncode);
        }
    }

    private int getBluetoothState() {
        if (this.mLocalBluetoothAdapter != null) {
            return this.mLocalBluetoothAdapter.getState();
        }
        return 10;
    }

    private int getBluetoothConnectionState() {
        if (this.mLocalBluetoothAdapter == null) {
            return 0;
        }
        int stat = this.mLocalBluetoothAdapter.getConnectionState();
        if (stat == 0 && this.mLocalBluetoothAdapter.isDiscovering()) {
            return 8;
        }
        return stat;
    }

    public void handleSyncSetScanResultsList(List<ScanDetail> scanList) {
        synchronized (this.mScanResultLock) {
            HwWifiStatStoreImpl.getDefault().checkScanResults(scanList);
            this.mScanResults.clear();
            for (ScanDetail result : scanList) {
                this.mScanResults.add(new ScanResult(result.getScanResult()));
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clearHwCHRAccessNetworkEventInfoList() {
        try {
            boolean bGetLock = this.mLock.tryLock(1, TimeUnit.SECONDS);
            if (bGetLock) {
                Log.d(TAG, "clearHwCHRAccessNetworkEventInfoList");
                this.mHwCHRAccessNetworkEventInfoList.clear();
            }
            if (bGetLock) {
                this.mLock.unlock();
            }
        } catch (InterruptedException e) {
            if (HWFLOW) {
                Log.d(TAG, "clearHwCHRAccessNetworkEventInfoList" + e);
            }
            if (null != null) {
                this.mLock.unlock();
            }
        } catch (Throwable th) {
            if (null != null) {
                this.mLock.unlock();
            }
        }
    }

    public void updateTimeStampSessionStart(long TimeStampSessionStart) {
        this.mTimeStampSessionStart = TimeStampSessionStart;
        this.mTimeStampSessionFirstConnect = 0;
        this.mTimeStampSessionFinish = 0;
        this.mConnectSuccessTime = 0;
        this.mIsAPOpen = (short) 0;
    }

    public void updateTimeStampSessionFirstConnect(long TimeStampSessionFirstConnect) {
        if (0 == this.mTimeStampSessionFirstConnect) {
            this.mTimeStampSessionFirstConnect = TimeStampSessionFirstConnect;
            this.mTimeStampSessionFinish = 0;
            this.mConnectSuccessTime = 0;
        }
    }

    public void updateAPOpenState() {
        this.mIsAPOpen = (short) 1;
        Log.d(TAG, "updateAPOpenState mIsAPOpen is " + this.mIsAPOpen);
    }

    public void updateTimeStampSessionFinish(long TimeStampFinish) {
        this.mTimeStampSessionFinish = TimeStampFinish;
    }

    public void updateConnectType(String ConnectType) {
        this.mConnectType = ConnectType;
        this.mConnectThreadName = "";
        this.mDisableThreadName = "";
    }

    public void updateDisableThreadName(String DisableThreadName) {
        if (DisableThreadName != null) {
            this.mDisableThreadName = DisableThreadName;
        }
    }

    public void updateConnectSuccessTime() {
        this.mConnectSuccessTime = this.mTimeStampSessionFinish - this.mTimeStampSessionStart;
    }

    /* JADX WARNING: Missing block: B:27:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void syncAddHwCHRAccessNetworkEventInfoList(HwCHRAccessNetworkEventInfo stHwCHRAccessNetworkEventInfo) {
        if (HWFLOW) {
            Log.d(TAG, "====syncAddHwCHRAccessNetworkEventInfoList getEventId =" + stHwCHRAccessNetworkEventInfo.getEventId());
        }
        try {
            boolean bGetLock = this.mLock.tryLock(1, TimeUnit.SECONDS);
            if (bGetLock) {
                if (this.mHwCHRAccessNetworkEventInfoList.size() >= 20) {
                    reportHwCHRAccessNetworkEventInfoList(5);
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
            if (null != null) {
                this.mLock.unlock();
            }
        } catch (Throwable th) {
            if (null != null) {
                this.mLock.unlock();
            }
        }
    }

    private boolean isWrongPswd() {
        boolean wrongPswd = false;
        for (HwCHRAccessNetworkEventInfo tmpEvent : new ArrayList(this.mHwCHRAccessNetworkEventInfoList)) {
            if (tmpEvent.getEventId() != 82) {
                return false;
            }
            if (tmpEvent.getSubErrorCode() == 2) {
                wrongPswd = true;
            }
        }
        if (!wrongPswd) {
            return false;
        }
        Log.d(TAG, "connect fail because of wrong password");
        return true;
    }

    public void handleReportHwCHRAccessNetworkEventInfoList(int ReportReason) {
        boolean bGetLock = false;
        try {
            bGetLock = this.mLock.tryLock(1, TimeUnit.SECONDS);
            if (bGetLock) {
                this.mTriggerReportType = ReportReason;
                if (HWFLOW) {
                    Log.d(TAG, "reportHwCHRAccessNetworkEventInfoList:ReportReason = " + ReportReason);
                }
                if (1 == ReportReason) {
                    if (this.mHwCHRAccessNetworkEventInfoList.size() > 0 && (this.mCurrentApBssid.equals(((HwCHRAccessNetworkEventInfo) this.mHwCHRAccessNetworkEventInfoList.get(0)).getAP_MAC()) ^ 1) != 0) {
                        if (!(this.mHwWifiStatStore == null || (this.misWifiProEvalu ^ 1) == 0 || (isWrongPswd() ^ 1) == 0)) {
                            this.mHwWifiStatStore.updateCHRConnectFailedCount(1);
                        }
                        updateTimeStampSessionFinish(SystemClock.elapsedRealtime());
                        updateWifiException(214, "");
                        updateTimeStampSessionStart(SystemClock.elapsedRealtime());
                    }
                    updateTimeStampSessionFirstConnect(SystemClock.elapsedRealtime());
                } else if (this.mHwCHRAccessNetworkEventInfoList.size() <= 0) {
                    if (bGetLock) {
                        this.mLock.unlock();
                    }
                    return;
                } else {
                    if (!(this.mHwWifiStatStore == null || ReportReason == 4 || (this.misWifiProEvalu ^ 1) == 0 || (isWrongPswd() ^ 1) == 0)) {
                        this.mHwWifiStatStore.updateCHRConnectFailedCount(0);
                    }
                    updateWifiException(214, "");
                }
            }
            if (bGetLock) {
                this.mLock.unlock();
            }
        } catch (InterruptedException e) {
            if (HWFLOW) {
                Log.d(TAG, "reportHwCHRAccessNetworkEventInfoList" + e);
            }
            if (bGetLock) {
                this.mLock.unlock();
            }
        } catch (Throwable th) {
            if (bGetLock) {
                this.mLock.unlock();
            }
        }
    }

    public HwCHRAccessNetworkEventInfo constructHwCHRAccessNetworkEventInfo(int ucErrorCode, String ucSubErrorCode) {
        HwCHRAccessNetworkEventInfo stHwCHRAccessNetworkEventInfo = new HwCHRAccessNetworkEventInfo();
        if (HWFLOW) {
            Log.d(TAG, "reportHwCHRAccessNetworkEventInfoList  errorcode " + ucErrorCode + ", suberrorcode =" + ucSubErrorCode);
        }
        stHwCHRAccessNetworkEventInfo.setEventId(ucErrorCode);
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
        stHwCHRAccessNetworkEventInfo.setPublicEss((short) this.mPublicEssCnt);
        stHwCHRAccessNetworkEventInfo.set_rssi_summery(this.mRssiGroup);
        stHwCHRAccessNetworkEventInfo.setIsOnScreen(this.mScreenState);
        switch (ucErrorCode) {
            case HwWifiCHRConstImpl.WIFI_CONNECT_AUTH_FAILED /*82*/:
                stHwCHRAccessNetworkEventInfo.setSubErrorCode(this.mAuthSubCode);
                this.mConnectFailedSubErrorCode = this.mAuthSubCode;
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
        this.mIp_leasetime = 0;
        this.strRoutes = "";
        this.str_gate_ip = "";
        this.str_dns = "";
        this.str_Wifi_ip = "";
        this.mAPVendorInfo = "";
        this.mIpType = 0;
        this.mIsPortalConnection = 0;
        this.mPortalStatus = 0;
        this.mNetSlowlyTime = 0;
        this.mNetNormalTime = 0;
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
            return false;
        }
        return true;
    }

    private int getRelatedApInfo(String bssid) {
        int essCount = 0;
        int relatedCount = 0;
        int sameFreq = 0;
        ScanResult currentSR = null;
        if (!isValidHardwareAddr(bssid)) {
            return 0;
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
                    return 0;
                }
                int bandWith;
                this.mBandWidth = (byte) currentSR.channelWidth;
                if (currentSR.is24GHz()) {
                    bandWith = 20;
                } else {
                    bandWith = 40;
                }
                for (ScanResult result2 : this.mScanResults) {
                    if (currentSR.SSID.equals(result2.SSID) && currentSR.capabilities.equals(result2.capabilities)) {
                        essCount++;
                    }
                    if (currentSR.frequency == result2.frequency) {
                        sameFreq++;
                        this.mRssiGroup.addRelationAps(true, result2.level);
                    } else if (Math.abs(currentSR.frequency - result2.frequency) < bandWith) {
                        relatedCount++;
                        this.mRssiGroup.addRelationAps(false, result2.level);
                    }
                }
            } catch (Exception e) {
            }
        }
        loge("getRelatedApInfo( sameFreq:" + sameFreq + ", essCount:" + essCount + " ,  relatedCount:" + relatedCount + ")");
        return ((essCount << 16) | (sameFreq << 8)) | relatedCount;
    }

    private static int getSecurity(ScanResult result) {
        if (result == null) {
            return -1;
        }
        if (result.capabilities.contains("WEP")) {
            return 1;
        }
        if (result.capabilities.contains("WAPI-PSK")) {
            return 4;
        }
        if (result.capabilities.contains("WAPI-CERT")) {
            return 5;
        }
        if (result.capabilities.contains("PSK")) {
            return 2;
        }
        if (result.capabilities.contains("EAP")) {
            return 3;
        }
        return 0;
    }

    private CSubApRoaming getApRoaming(String ssid, String bssid) {
        CSubApRoaming ApRoaming = new CSubApRoaming();
        int firstRssi = -100;
        int secondRssi = -100;
        int firstChannel = 0;
        int secondChannel = 0;
        if (ssid.isEmpty() || bssid.isEmpty()) {
            return ApRoaming;
        }
        this.mPublicEssCnt = (byte) 0;
        ScanResult currentAP = getScanResultByBssid(bssid);
        if (currentAP == null) {
            return ApRoaming;
        }
        this.mCurrentApSecurity = getSecurity(currentAP);
        synchronized (this.mScanResultLock) {
            try {
                for (ScanResult result : this.mScanResults) {
                    if (currentAP.SSID.equals(result.SSID) && this.mCurrentApSecurity == getSecurity(result)) {
                        this.mPublicEssCnt = (byte) (this.mPublicEssCnt + 1);
                        if (!bssid.equals(result.BSSID)) {
                            if (result.level > firstRssi) {
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
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.setApRoamingParam(this.mPublicEssCnt);
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

    public boolean isWifiProEvaluate() {
        boolean z = true;
        int tag = -1;
        if (mContextRef != null) {
            tag = Secure.getInt(mContextRef.getContentResolver(), WIFI_EVALUATE_TAG, 0);
        }
        Log.d(TAG, "isWifiProEvaluate flag is " + tag);
        if (tag != 1) {
            z = false;
        }
        this.misWifiProEvalu = z;
        return this.misWifiProEvalu;
    }

    private void writeNETInfo(int type, String ucSubErrorCode) {
        Log.d(TAG, "type:" + type + ", writeNETInfo begin");
        preWifiException(this.commercialUser);
        if (type == 80) {
            this.mOpenErrorCode = ucSubErrorCode;
            this.mCloseErrorCode = "NOT_CLOSE_ERROR";
            if (this.mHwWifiStatStore != null) {
                this.mHwWifiStatStore.updateWifiState(true, false);
            }
        }
        if (type == 81) {
            this.mCloseErrorCode = ucSubErrorCode;
            this.mOpenErrorCode = "NOT_OPEN_ERROR";
            if (this.mHwWifiStatStore != null) {
                this.mHwWifiStatStore.updateWifiState(false, false);
            }
        }
        int dftType = -1;
        if (this.mHwWifiDFTUtilImpl != null) {
            dftType = this.mHwWifiDFTUtilImpl.getDFTEventType(type);
        }
        if (isNeedTriggerDFTEvent(dftType)) {
            uploadDFTEvent(dftType, ucSubErrorCode);
            return;
        }
        if (dftType == 909002023) {
            clearHwCHRAccessNetworkEventInfoList();
        }
        Log.d(TAG, "exceed max upload CHR count");
    }

    private void preWifiException(boolean commercialUser) {
        if (commercialUser) {
            this.strSta_mac = "";
            this.str_Wifi_ip = maskIpAddress(this.str_Wifi_ip);
            this.strRoutes = "";
        }
    }

    public void updateWifiAuthFailEvent(String iface, int reason) {
        Log.d(TAG, "updateWifiAuthFailEvent,iface is:" + iface + " ,reason is :" + reason);
        if ("wlan0".equals(iface)) {
            this.mAuthSubCode = reason;
            updateWifiException(82, "");
        }
    }

    public void uploadAssocRejectException(int status, String bssid) {
        ScanResult scanResult = getScanResultByBssid(bssid);
        if (scanResult != null) {
            updateChannel(scanResult.frequency);
        }
        uploadAssocRejectException(status);
    }

    public void uploadAssocRejectException(int status) {
        this.mSubcodeReject = status;
        Log.d(TAG, "uploadAssocRejectException status is " + status);
        if (this.mSubcodeReject > 0 && this.mSubcodeReject != 17) {
            updateWifiException(83, "");
        }
        this.hwWifiCHRService = HwWifiCHRServiceImpl.getDefault();
        if (this.mSubcodeReject > 0 && this.hwWifiCHRService != null) {
            this.hwWifiCHRService.assocRejectEvent(status);
        }
        if (this.mSubcodeReject > 0 && this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.updateReasonCode(83, status);
        }
    }

    public void uploadDhcpException(String strDhcpError) {
        this.aucDhcp_failed = (short) 0;
        Log.d(TAG, "uploadDhcpException the error is:" + strDhcpError);
        updateWifiException(84, strDhcpError);
    }

    public void uploadWifiStat() {
        Log.d(TAG, "uploadWifiStat");
        HwWifiDFTConnManager.getInstance().triggerWifiUpload(909001003, 1);
    }

    public void uploadUserConnectFailed(int type) {
        updateWifiException(101, String.valueOf(type));
    }

    public void handleUploadDisconnectException(int reasoncode) {
        String strReasoncode = HwWifiCHRConstImpl.getDefault().getDisconnectReasonCode(reasoncode);
        if ("".equals(strReasoncode) || this.mRssi < -82 || this.mHwWifiStatStore == null) {
            if (this.mHwWifiStatStore != null && this.mHwWifiStatStore.isConnectToNetwork()) {
                this.mHwWifiStatStore.updateConnectState(false);
            }
            Log.d(TAG, "disconnected but reason code is unmatch");
            return;
        }
        this.aucAbnormal_disconnect = (short) reasoncode;
        if (this.mHwWifiStatStore.isConnectToNetwork()) {
            updateWifiException(85, strReasoncode);
            if (this.mHwWifiStatStore != null) {
                this.mHwWifiStatStore.setAbDisconnectFlg(this.strAp_Ssid, reasoncode);
            }
        }
    }

    public void updatePortalAutSms(String sms_num, byte[] sms_body, int sms_body_len) {
        this.mSms_Num = "";
        this.mSMS_Body_Len = 0;
        this.mSms_Body = new byte[1];
        if (sms_num == null || sms_body == null) {
            Log.d(TAG, "updatePortalAutSms: invalid sms_num or sms_body");
            return;
        }
        this.mSms_Num = sms_num;
        this.mSMS_Body_Len = sms_body_len;
        this.mSms_Body = (byte[]) sms_body.clone();
    }

    public void updatePortalAPInfo(byte[] ssid, String bssid, String cellId, int bssid_len) {
        this.mPortalAPSsid = new byte[1];
        this.mPortalAPBssid = "";
        this.mSsid_Len = 0;
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
            this.mPortalAPKeyLines = new byte[1];
        }
    }

    public void updatePortalStatus(int respCode) {
        int isRedirect = (respCode < 300 || respCode > 399) ? 0 : 1;
        if (this.mIsPortalConnection == 0) {
            this.mPortalStatus = 0;
        } else if (this.mIsPortalConnection == 1 && isRedirect == 1) {
            this.mPortalStatus = 0;
        } else {
            this.mPortalStatus = 1;
        }
        Log.d(TAG, "updatePortalStatus: IsPortalConnection= " + this.mIsPortalConnection + ",  respCode =" + respCode + ",  portal Status =" + this.mPortalStatus);
    }

    public void updatePortalConnection(int isPortalconnection) {
        this.mIsPortalConnection = isPortalconnection;
    }

    public int isPortalConnection() {
        return this.mIsPortalConnection;
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

    public void setCountryCode(String countryCode) {
        Log.d(TAG, "setCountryCode " + countryCode);
        this.mCountryCode = countryCode;
    }

    public void setIpType(int type) {
        this.mIpType = type;
    }

    private byte iIsMobileAP() {
        if (mContextRef == null) {
            return (byte) 0;
        }
        return (byte) (HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(mContextRef) ? 1 : 0);
    }

    private boolean isNeedTriggerDFTEvent(int type) {
        long now = SystemClock.elapsedRealtime();
        Log.d(TAG, "isNeedTriggerDFTEvent type is " + type);
        switch (type) {
            case 909001001:
            case 909001002:
            case 909002029:
                return true;
            case 909002021:
                if (this.mOpenCloseCnt == 0) {
                    this.mOpenClodeTimeStamp = now;
                    this.mOpenCloseCnt++;
                    return true;
                } else if (this.mOpenCloseCnt < 50 && now - this.mOpenClodeTimeStamp < COMM_UPLOAD_MIN_SPAN) {
                    this.mOpenCloseCnt++;
                    return true;
                } else if (now - this.mOpenClodeTimeStamp <= COMM_UPLOAD_MIN_SPAN) {
                    return false;
                } else {
                    this.mOpenCloseCnt = 1;
                    this.mOpenClodeTimeStamp = now;
                    return true;
                }
            case 909002022:
                if (this.mAbnorMalDisConnCnt == 0) {
                    this.mAbnorMalDisConnTimeStamp = now;
                    this.mAbnorMalDisConnCnt++;
                    return true;
                } else if (this.mAbnorMalDisConnCnt < 50 && now - this.mAbnorMalDisConnTimeStamp < COMM_UPLOAD_MIN_SPAN) {
                    this.mAbnorMalDisConnCnt++;
                    return true;
                } else if (now - this.mAbnorMalDisConnTimeStamp <= COMM_UPLOAD_MIN_SPAN) {
                    return false;
                } else {
                    this.mAbnorMalDisConnCnt = 1;
                    this.mAbnorMalDisConnTimeStamp = now;
                    return true;
                }
            case 909002023:
                if (this.mConnFailCnt == 0) {
                    this.mConnFailTimeStamp = now;
                    this.mConnFailCnt++;
                    return true;
                } else if (this.mConnFailCnt < 50 && now - this.mConnFailTimeStamp < COMM_UPLOAD_MIN_SPAN) {
                    this.mConnFailCnt++;
                    return true;
                } else if (now - this.mConnFailTimeStamp <= COMM_UPLOAD_MIN_SPAN) {
                    return false;
                } else {
                    this.mConnFailCnt = 1;
                    this.mConnFailTimeStamp = now;
                    return true;
                }
            case 909002024:
                if (this.mAccessWebFailCnt == 0) {
                    this.mFirstAccessWebFailTimeStamp = now;
                    this.mAccessWebFailTimeStamp = now;
                    this.mAccessWebFailCnt++;
                    return true;
                } else if (this.commercialUser) {
                    if ((this.mAccessWebFailTimeStamp == 0 || now - this.mAccessWebFailTimeStamp > ACCESSWEB_SLOW_OR_FAILED_UPLOAD_MIN) && ((long) this.mAccessWebFailCnt) < ACCESSWEB_SLOW_OR_FAILED_UPLOAD_COUNT && now - this.mFirstAccessWebFailTimeStamp < COMM_UPLOAD_MIN_SPAN) {
                        this.mAccessWebFailCnt++;
                        this.mAccessWebFailTimeStamp = now;
                        return true;
                    } else if (now - this.mFirstAccessWebFailTimeStamp <= COMM_UPLOAD_MIN_SPAN) {
                        return false;
                    } else {
                        this.mAccessWebFailCnt = 1;
                        this.mFirstAccessWebFailTimeStamp = now;
                        this.mAccessWebFailTimeStamp = now;
                        return true;
                    }
                } else if ((this.mAccessWebFailTimeStamp == 0 || now - this.mAccessWebFailTimeStamp > 600000) && ((long) this.mAccessWebFailCnt) < ACCESSWEB_SLOW_OR_FAILED_BETA_UPLOAD_COUNT && now - this.mFirstAccessWebFailTimeStamp < COMM_UPLOAD_MIN_SPAN) {
                    this.mAccessWebFailCnt++;
                    this.mAccessWebFailTimeStamp = now;
                    return true;
                } else if (now - this.mFirstAccessWebFailTimeStamp <= COMM_UPLOAD_MIN_SPAN) {
                    return false;
                } else {
                    this.mAccessWebFailCnt = 1;
                    this.mFirstAccessWebFailTimeStamp = now;
                    this.mAccessWebFailTimeStamp = now;
                    return true;
                }
            case 909002025:
                if (this.mAccessWebSlowlyCnt == 0) {
                    this.mFirstAccessWebSlowlyTimeStamp = now;
                    this.mAccessWebSlowlyTimeStamp = now;
                    this.mAccessWebSlowlyCnt++;
                    return true;
                } else if (this.commercialUser) {
                    if ((this.mAccessWebSlowlyTimeStamp == 0 || now - this.mAccessWebSlowlyTimeStamp > ACCESSWEB_SLOW_OR_FAILED_UPLOAD_MIN) && ((long) this.mAccessWebSlowlyCnt) < ACCESSWEB_SLOW_OR_FAILED_UPLOAD_COUNT && now - this.mFirstAccessWebSlowlyTimeStamp < COMM_UPLOAD_MIN_SPAN) {
                        this.mAccessWebSlowlyCnt++;
                        this.mAccessWebSlowlyTimeStamp = now;
                        return true;
                    } else if (now - this.mFirstAccessWebSlowlyTimeStamp <= COMM_UPLOAD_MIN_SPAN) {
                        return false;
                    } else {
                        this.mAccessWebSlowlyCnt = 1;
                        this.mFirstAccessWebSlowlyTimeStamp = now;
                        this.mAccessWebSlowlyTimeStamp = now;
                        return true;
                    }
                } else if ((this.mAccessWebSlowlyTimeStamp == 0 || now - this.mAccessWebSlowlyTimeStamp > 600000) && ((long) this.mAccessWebSlowlyCnt) < ACCESSWEB_SLOW_OR_FAILED_BETA_UPLOAD_COUNT && now - this.mFirstAccessWebSlowlyTimeStamp < COMM_UPLOAD_MIN_SPAN) {
                    this.mAccessWebSlowlyCnt++;
                    this.mAccessWebSlowlyTimeStamp = now;
                    return true;
                } else if (now - this.mFirstAccessWebSlowlyTimeStamp <= COMM_UPLOAD_MIN_SPAN) {
                    return false;
                } else {
                    this.mAccessWebSlowlyCnt = 1;
                    this.mFirstAccessWebSlowlyTimeStamp = now;
                    this.mAccessWebSlowlyTimeStamp = now;
                    return true;
                }
            case 909002026:
                if (this.mH110XDevErrCnt == 0) {
                    this.mH110XDevErrTimeStamp = now;
                    this.mH110XDevErrCnt++;
                    return true;
                } else if (this.mH110XDevErrCnt < 50 && now - this.mH110XDevErrTimeStamp < COMM_UPLOAD_MIN_SPAN) {
                    this.mH110XDevErrCnt++;
                    return true;
                } else if (now - this.mH110XDevErrTimeStamp <= COMM_UPLOAD_MIN_SPAN) {
                    return false;
                } else {
                    this.mH110XDevErrCnt = 1;
                    this.mH110XDevErrTimeStamp = now;
                    return true;
                }
            default:
                return false;
        }
    }

    private EventStream getClassEventStream(int eventid) {
        Log.d(TAG, "begin to get EventStream eventid is: " + eventid);
        Date date = new Date();
        HwWifiDFTUtilImpl hwWifiDFTUtilImpl;
        switch (eventid) {
            case 909009001:
                try {
                    EventStream apInfo = IMonitor.openEventStream(eventid);
                    if (apInfo == null) {
                        Log.e(TAG, "class EventStream apInfo is null");
                        return null;
                    }
                    String maskMacAddress;
                    String ApInfoSsid = this.connect_event_flag ? this.mPreviousConnectEventSSID : this.strAp_Ssid;
                    if (this.mWifiProHistoryRecordManager == null || this.mWifiManager == null) {
                        this.mWifiManager = (WifiManager) mContextRef.getSystemService("wifi");
                        if (WifiProCommonUtils.isWifiProPropertyEnabled()) {
                            this.mWifiProHistoryRecordManager = WifiProHistoryRecordManager.getInstance(mContextRef, this.mWifiManager);
                        }
                    }
                    if (this.commercialUser) {
                        maskMacAddress = maskMacAddress(this.mCurrentApBssid);
                    } else {
                        maskMacAddress = this.mCurrentApBssid;
                    }
                    apInfo.setParam((short) 0, maskMacAddress).setParam((short) 1, String.valueOf(this.mCurrentApBssid.hashCode())).setParam((short) 2, ApInfoSsid).setParam((short) 3, this.strAP_key_mgmt).setParam((short) 4, this.strAP_eap).setParam((short) 5, this.usLinkSpeed).setParam((short) 6, this.usAP_channel).setParam((short) 7, this.mRssi).setParam((short) 8, iIsMobileAP()).setParam((short) 9, this.mBandWidth).setParam((short) 10, Boolean.valueOf(this.mIsHiddenSsid));
                    if (this.mWifiProHistoryRecordManager != null) {
                        apInfo.setParam((short) 12, this.mWifiProHistoryRecordManager.getIsHomeAP(this.mCurrentApBssid) ? 1 : 0);
                    }
                    return apInfo;
                } catch (Exception e) {
                    Log.e(TAG, "set Class EventStream apInfo error.");
                    return null;
                }
            case 909009005:
                try {
                    EventStream apRoamInfo = IMonitor.openEventStream(eventid);
                    CSubApRoaming ApRoaming = getApRoaming(this.strAp_Ssid, this.mCurrentApBssid);
                    if (apRoamInfo == null) {
                        Log.e(TAG, "class EventStream apRoamInfo is null");
                        return null;
                    } else if (ApRoaming == null) {
                        Log.e(TAG, "ApRoaming is null");
                        return apRoamInfo;
                    } else {
                        apRoamInfo.setParam((short) 0, ApRoaming.iFirst_Rssi.getValue()).setParam((short) 1, ApRoaming.iFirst_Channel.getValue()).setParam((short) 2, ApRoaming.iSecond_Rssi.getValue()).setParam((short) 3, ApRoaming.iSecond_Channel.getValue());
                        return apRoamInfo;
                    }
                } catch (Exception e2) {
                    Log.e(TAG, "set Class EventStream apRoamInfo error.");
                    return null;
                }
            case 909009006:
                try {
                    updateCHRCounters();
                    EventStream bcmChipInfo = IMonitor.openEventStream(eventid);
                    CSubWL_COUNTERS chrWlCounters = this.mCounter_monitor.getCounterLst().getWLCountersCHR();
                    if (bcmChipInfo == null) {
                        Log.e(TAG, "class EventStream bcmChipInfo is null");
                        return null;
                    } else if (chrWlCounters == null) {
                        Log.e(TAG, " chrWlCounters is null");
                        return bcmChipInfo;
                    } else {
                        bcmChipInfo.setParam((short) 0, WL_COUNRTS_VERSION).setParam((short) 1, chrWlCounters.imonitorDuration.getValue()).setParam((short) 2, chrWlCounters.itxframe.getValue()).setParam((short) 3, chrWlCounters.itxbyte.getValue()).setParam((short) 4, chrWlCounters.itxfail.getValue()).setParam((short) 5, chrWlCounters.itxfrag.getValue()).setParam((short) 6, chrWlCounters.itxfrmsnt.getValue()).setParam((short) 7, chrWlCounters.itxnoack.getValue()).setParam((short) 8, chrWlCounters.itxphyerr.getValue()).setParam((short) 9, chrWlCounters.itxnobuf.getValue()).setParam((short) 10, chrWlCounters.itxnocts.getValue()).setParam((short) 11, chrWlCounters.itxrts.getValue()).setParam((short) 12, chrWlCounters.itxphyerror.getValue()).setParam((short) 13, chrWlCounters.itxnoassoc.getValue()).setParam((short) 14, chrWlCounters.itxctl.getValue()).setParam((short) 15, chrWlCounters.itxallfrm.getValue()).setParam((short) 16, chrWlCounters.itxerror.getValue()).setParam((short) 17, chrWlCounters.itxretrans.getValue()).setParam((short) 18, chrWlCounters.irxframe.getValue()).setParam((short) 19, chrWlCounters.irxbyte.getValue()).setParam((short) 20, chrWlCounters.irxfrag.getValue()).setParam((short) 21, chrWlCounters.irxbadfcs.getValue()).setParam((short) 22, chrWlCounters.irxnobuf.getValue()).setParam((short) 23, chrWlCounters.irxbadcm.getValue()).setParam((short) 24, chrWlCounters.irxbadplcp.getValue()).setParam((short) 25, chrWlCounters.irxcrsglitch.getValue()).setParam((short) 26, chrWlCounters.irxbadproto.getValue()).setParam((short) 27, chrWlCounters.irxmulti.getValue()).setParam((short) 28, chrWlCounters.irxnondata.getValue()).setParam((short) 29, chrWlCounters.irxbadds.getValue()).setParam((short) 30, chrWlCounters.ireset.getValue()).setParam((short) 31, chrWlCounters.irxdfrmocast.getValue()).setParam((short) 32, chrWlCounters.irxmfrmocast.getValue()).setParam((short) 33, chrWlCounters.irxdfrmucastmbss.getValue()).setParam((short) 34, chrWlCounters.irxmfrmucastmbss.getValue()).setParam((short) 35, chrWlCounters.irxbeaconmbss.getValue()).setParam((short) 36, chrWlCounters.irxdfrmucastobss.getValue()).setParam((short) 37, chrWlCounters.irxbeaconobss.getValue()).setParam((short) 38, chrWlCounters.irxstrt.getValue()).setParam((short) 39, chrWlCounters.irxcfrmocast.getValue()).setParam((short) 40, chrWlCounters.irxcfrmucast.getValue()).setParam((short) 41, chrWlCounters.irxdfrmmcast.getValue()).setParam((short) 42, chrWlCounters.irxmfrmmcast.getValue()).setParam((short) 43, chrWlCounters.irxcfrmmcast.getValue());
                        return bcmChipInfo;
                    }
                } catch (Exception e3) {
                    Log.e(TAG, "set Class EventStream bcmChipInfo error.");
                    return null;
                }
            case 909009007:
                try {
                    EventStream CellIDInfo = IMonitor.openEventStream(eventid);
                    CSubCellID CellID = HwCHRWifiRelatedStateMonitor.make(mContextRef).getCellIDCHR();
                    if (CellIDInfo == null) {
                        Log.e(TAG, "class EventStream CellIDInfo is null");
                        return null;
                    } else if (CellID == null) {
                        Log.e(TAG, "CellID is null");
                        return CellIDInfo;
                    } else {
                        CellIDInfo.setParam((short) 0, CellID.iCID.getValue()).setParam((short) 1, CellID.iLAC.getValue()).setParam((short) 2, CellID.strMCC.getValue()).setParam((short) 3, CellID.strMNC.getValue());
                        return CellIDInfo;
                    }
                } catch (Exception e4) {
                    Log.e(TAG, "set Class EventStream CellIDInfo error.");
                    return null;
                }
            case 909009008:
                try {
                    EventStream BTStatusInfo = IMonitor.openEventStream(eventid);
                    CSubBTStatus BtStatus = HwCHRWifiRelatedStateMonitor.make(mContextRef).getBTStateCHR();
                    if (BTStatusInfo == null) {
                        Log.e(TAG, "class EventStream BTStatusInfo is null");
                        return null;
                    } else if (BtStatus == null) {
                        Log.e(TAG, "BtStatus is null");
                        return BTStatusInfo;
                    } else {
                        BTStatusInfo.setParam((short) 0, BtStatus.ucBTState.getValue()).setParam((short) 1, BtStatus.ucBTConnState.getValue()).setParam((short) 2, BtStatus.ucConnectedDevicesCnt.getValue()).setParam((short) 3, BtStatus.ucisAudioOn.getValue()).setParam((short) 4, BtStatus.ucisA2DPPlaying.getValue()).setParam((short) 5, BtStatus.ucisOPP.getValue());
                        return BTStatusInfo;
                    }
                } catch (Exception e5) {
                    Log.e(TAG, "set Class EventStream BTStatusInfo error.");
                    return null;
                }
            case 909009009:
                try {
                    EventStream wifiSettingsInfo = IMonitor.openEventStream(eventid);
                    hwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
                    if (wifiSettingsInfo == null) {
                        Log.e(TAG, "class EventStream wifiSettingsInfo is null");
                        return null;
                    }
                    wifiSettingsInfo.setParam((short) 0, hwWifiDFTUtilImpl.getWifiProState() ? 1 : 0).setParam((short) 1, hwWifiDFTUtilImpl.getWifiAlwaysScanState() ? 1 : 0).setParam((short) 2, hwWifiDFTUtilImpl.getWifiNetworkNotificationState() ? 1 : 0).setParam((short) 3, hwWifiDFTUtilImpl.getWifiSleepPolicyState()).setParam((short) 4, hwWifiDFTUtilImpl.getWifiToPdpState());
                    return wifiSettingsInfo;
                } catch (Exception e6) {
                    Log.e(TAG, "set Class EventStream wifiSettingsInfo error.");
                    return null;
                }
            case 909009011:
                try {
                    EventStream wifiNetConfigInfo = IMonitor.openEventStream(eventid);
                    CSubNET_CFG netCfg = HwCHRWifiRelatedStateMonitor.make(mContextRef).getSSIDSetting();
                    if (wifiNetConfigInfo == null) {
                        Log.e(TAG, "class EventStream wifiNetConfigInfo is null");
                        return null;
                    } else if (netCfg == null) {
                        Log.e(TAG, "netCfg is null");
                        return wifiNetConfigInfo;
                    } else {
                        wifiNetConfigInfo.setParam((short) 0, netCfg.ucVPN.getValue()).setParam((short) 1, netCfg.iIpMask.getValue()).setParam((short) 2, this.mIpType);
                        return wifiNetConfigInfo;
                    }
                } catch (Exception e7) {
                    Log.e(TAG, "set Class EventStream wifiNetConfigInfo error.");
                    return null;
                }
            case 909009012:
                try {
                    EventStream CpuMemInfo = IMonitor.openEventStream(eventid);
                    CSubMemInfo mem = HwCHRWebMonitor.getMemCHR();
                    CSubCPUInfo cpu = HwCHRWebMonitor.getCpuCHR();
                    if (CpuMemInfo == null) {
                        Log.e(TAG, "class EventStream CpuMemInfo is null");
                        return null;
                    } else if (mem == null || cpu == null) {
                        Log.e(TAG, "mem or cpu is null");
                        return CpuMemInfo;
                    } else {
                        CpuMemInfo.setParam((short) 0, mem.iMemLoad.getValue()).setParam((short) 1, cpu.ipercent.getValue()).setParam((short) 2, cpu.lmaxFreq.getValue());
                        return CpuMemInfo;
                    }
                } catch (Exception e8) {
                    Log.e(TAG, "set Class EventStream CpuMemInfo error.");
                    return null;
                }
            case 909009013:
                try {
                    EventStream handsetStateInfo = IMonitor.openEventStream(eventid);
                    if (handsetStateInfo == null) {
                        Log.e(TAG, "class EventStream handsetStateInfo is null");
                        return null;
                    }
                    handsetStateInfo.setParam((short) 0, this.mCountryCode).setParam((short) 1, (byte) this.mScreenState);
                    return handsetStateInfo;
                } catch (Exception e9) {
                    Log.e(TAG, "set Class EventStream handsetStateInfo error.");
                    return null;
                }
            case 909009014:
                try {
                    EventStream tcpPacketInfo = IMonitor.openEventStream(eventid);
                    CSubTCP_STATIST tcpStat = HwCHRWebSpeed.getDefault().getTcpStatistCHR();
                    if (tcpPacketInfo == null) {
                        Log.e(TAG, "class EventStream tcpPacketInfo is null");
                        return null;
                    } else if (tcpStat == null) {
                        Log.e(TAG, "tcpStat is null");
                        return tcpPacketInfo;
                    } else {
                        tcpPacketInfo.setParam((short) 0, "").setParam((short) 1, 0).setParam((short) 2, 0).setParam((short) 3, 0).setParam((short) 4, tcpStat.isend_packets.getValue()).setParam((short) 5, tcpStat.iresend_packets.getValue()).setParam((short) 6, tcpStat.irecv_packets.getValue()).setParam((short) 7, tcpStat.irtt_duration.getValue()).setParam((short) 8, tcpStat.irtt_packets.getValue());
                        return tcpPacketInfo;
                    }
                } catch (Exception e10) {
                    Log.e(TAG, "set Class EventStream tcpPacketInfo error.");
                    return null;
                }
            case 909009015:
                try {
                    EventStream netQualityInfo = IMonitor.openEventStream(eventid);
                    CSubDNS dns = HwCHRWebMonitor.getDNSCHR();
                    CSubPacketCount pkt = HwCHRWebMonitor.getPacketCountCHR();
                    if (netQualityInfo == null) {
                        Log.e(TAG, "class EventStream netQualityInfo is null");
                        return null;
                    } else if (dns == null || pkt == null) {
                        Log.e(TAG, "dns or pkt is null");
                        return netQualityInfo;
                    } else {
                        netQualityInfo.setParam((short) 0, dns.iFailedCnt.getValue()).setParam((short) 1, dns.iresptime.getValue()).setParam((short) 4, pkt.iTX_GOOD.getValue()).setParam((short) 5, pkt.iTX_BAD.getValue()).setParam((short) 6, pkt.iRX_GOOD.getValue());
                        return netQualityInfo;
                    }
                } catch (Exception e11) {
                    Log.e(TAG, "set Class EventStream netQualityInfo error.");
                    return null;
                }
            case 909009016:
                try {
                    EventStream apConnectInfo = IMonitor.openEventStream(eventid);
                    if (apConnectInfo == null) {
                        Log.e(TAG, "class EventStream apConnectInfo is null");
                        return null;
                    }
                    apConnectInfo.setParam((short) 0, Boolean.valueOf(isIPMatchGateWay())).setParam((short) 1, this.mIp_leasetime).setParam((short) 2, this.str_dns).setParam((short) 3, (byte) this.mPortalStatus);
                    return apConnectInfo;
                } catch (Exception e12) {
                    Log.e(TAG, "set Class EventStream apConnectInfo error.");
                    return null;
                }
            case 909009017:
                try {
                    EventStream wifiSDIOInfo = IMonitor.openEventStream(eventid);
                    if (wifiSDIOInfo == null) {
                        Log.e(TAG, "class EventStream wifiSDIOInfo is null");
                        return null;
                    } else if (this.mAWS == null) {
                        Log.e(TAG, "mAWS is null");
                        return wifiSDIOInfo;
                    } else {
                        wifiSDIOInfo.setParam((short) 0, this.mAWS.get_sdio_info_readbreq()).setParam((short) 1, this.mAWS.get_sdio_info_readb()).setParam((short) 2, this.mAWS.get_sdio_info_writebreq()).setParam((short) 3, this.mAWS.get_sdio_info_writeb()).setParam((short) 4, this.mAWS.get_sdio_info_readwreq()).setParam((short) 5, this.mAWS.get_sdio_info_readw()).setParam((short) 6, this.mAWS.get_sdio_info_writewreq()).setParam((short) 7, this.mAWS.get_sdio_info_writew()).setParam((short) 8, this.mAWS.get_sdio_info_ksosetreq()).setParam((short) 9, this.mAWS.get_sdio_info_ksosetretry()).setParam((short) 10, this.mAWS.get_sdio_info_ksoclrreq()).setParam((short) 11, this.mAWS.get_sdio_info_ksoclrretry());
                        return wifiSDIOInfo;
                    }
                } catch (Exception e13) {
                    Log.e(TAG, "set Class EventStream wifiSDIOInfo error.");
                    return null;
                }
            case 909009018:
                try {
                    EventStream MacSsidInfo = IMonitor.openEventStream(eventid);
                    if (MacSsidInfo == null) {
                        Log.e(TAG, "class EventStream MacSsidInfo is null");
                        return null;
                    }
                    MacSsidInfo.setParam((short) 0, this.strAp_Ssid).setParam((short) 1, this.commercialUser ? maskMacAddress(this.mCurrentApBssid) : this.mCurrentApBssid);
                    return MacSsidInfo;
                } catch (Exception e14) {
                    Log.e(TAG, "set Class EventStream MacSsidInfo error.");
                    return null;
                }
            case 909009019:
                try {
                    EventStream apSsidInfo = IMonitor.openEventStream(eventid);
                    hwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
                    if (apSsidInfo == null) {
                        Log.e(TAG, "class EventStream apSsidInfo is null");
                        return null;
                    }
                    apSsidInfo.setParam((short) 0, hwWifiDFTUtilImpl.getWifiProState() ? 1 : 0).setParam((short) 1, hwWifiDFTUtilImpl.getWifiProSwcnt()).setParam((short) 2, hwWifiDFTUtilImpl.getWifiAlwaysScanState() ? 1 : 0).setParam((short) 3, hwWifiDFTUtilImpl.getScanAlwaysSwCnt()).setParam((short) 4, hwWifiDFTUtilImpl.getWifiNetworkNotificationState() ? 1 : 0).setParam((short) 5, hwWifiDFTUtilImpl.getWifiNotifationSwCnt()).setParam((short) 6, hwWifiDFTUtilImpl.getWifiSleepPolicyState()).setParam((short) 7, hwWifiDFTUtilImpl.getWifiSleepSwCnt()).setParam((short) 8, hwWifiDFTUtilImpl.getWifiToPdpState()).setParam((short) 9, hwWifiDFTUtilImpl.getWifiToPdpSwCnt()).setParam((short) 10, 0).setParam((short) 11, 0).setParam((short) 12, 0).setParam((short) 13, 0).setParam((short) 14, 0).setParam((short) 15, 0);
                    return apSsidInfo;
                } catch (Exception e15) {
                    Log.e(TAG, "set Class EventStream apSsidInfo error.");
                    return null;
                }
            case 909009026:
                try {
                    EventStream gameLagInfo = IMonitor.openEventStream(eventid);
                    if (gameLagInfo == null) {
                        Log.e(TAG, "class EventStream gameLagInfo is null");
                        return null;
                    }
                    gameLagInfo.setParam((short) 0, this.mgameName).setParam((short) 1, this.mgameRTT).setParam((short) 2, this.mgameTcpRTT);
                    return gameLagInfo;
                } catch (Exception e16) {
                    Log.e(TAG, "set Class EventStream gameLagInfo error.");
                    return null;
                }
            default:
                Log.w(TAG, "Get Class Event Stream Unknown Event.");
                return null;
        }
    }

    public void handleNotifyNcSTATDftEvent() {
        short i;
        List<String> list;
        Log.d(TAG, "HwWifiDFTConst.EID_STABILITYSTAT2.");
        HwWifiDFT2StabilityStat hwWifiDFT2StabilityStat = new HwWifiDFT2StabilityStat();
        HwWifiStatStoreImpl.getDefault().getWifiDFT2StabilityStat(hwWifiDFT2StabilityStat);
        List<String> statlist = new ArrayList();
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mabDisconnCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.maccWebFailCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.maccWebSlowCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mcloseCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mcloseDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mcloseSucCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mconnectedCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mconnectedDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mconnectingDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mconnectTotalCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mdisconnCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.misScanAlwaysAvailble));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.misWifiNotifationOn));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.misWifiProOn));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mmobileTotalConnDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mmobileTotalTraffic));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mopenCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mopenDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mopenSucCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mrepeaterWorkDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mrepeterClientMaxCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mrepeterConnFailCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mrepeterDiffFreqDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mrepeterOpenCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mrepeterOpenSuccCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mscanAlwaysSwCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mwebFailDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mwebSlowDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mwifiNotifationSwCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mwifiProSwCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mwifiSleepPolicy));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mwifiSleepSwCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mwifiToPDP));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mwifiToPDPSwCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mwlanTotalConnDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mwlanTotalTraffic));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mDsStatSuccNum));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mDsStatWebDelay));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mnoNetByTcp));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mnoNetByUdp));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mdisCheckAbCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mdisCheckFailCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mdisCheckL1));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mdisCheckL2));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mdisCheckL3));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mevaluSuccCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mevaluFailCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mevaluTimeL1));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mevaluTimeL2));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mevaluTimeL3));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mgameName));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mgameCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mGameLag[0]));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mGameLag[1]));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mGameLag[2]));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mGameLag[3]));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mtemCtrlCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mtemCtrlDura));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mmaxTem));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mminDutyCycleCnt));
        statlist.add(String.valueOf(hwWifiDFT2StabilityStat.mExtendMaxTemCnt));
        HwWifiDFTConnManager.getInstance().reportWifiDFTEvent(909001003, statlist);
        List<HwWifiDFT2StabilitySsidStat> listHwWifiDFT2StabilitySsidStat = new ArrayList();
        HwWifiStatStoreImpl.getDefault().getWifiDFT2StabilitySsidStat(listHwWifiDFT2StabilitySsidStat);
        short ssidStatSize = (short) listHwWifiDFT2StabilitySsidStat.size();
        if (ssidStatSize == (short) 0) {
            Log.d(TAG, "wifi ssid stability is null, do not need trigger.");
        }
        Log.d(TAG, "HwWifiDFTConst.EID_STABILITYSSIDSTAT2.");
        for (i = (short) 0; i < ssidStatSize; i++) {
            int j;
            list = new ArrayList();
            HwWifiDFT2StabilitySsidStat hwWifiDFT2StabilitySsidStat = (HwWifiDFT2StabilitySsidStat) listHwWifiDFT2StabilitySsidStat.get(i);
            list.add(hwWifiDFT2StabilitySsidStat.mAPSsid);
            Log.d(TAG, "mAPSsid= " + hwWifiDFT2StabilitySsidStat.mAPSsid + " ,ssidStatSize = " + ssidStatSize);
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mabDisconnCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mABSAssocCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mABSAssocFailCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mABSMimoDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mABSMimoScreenOnDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mABSSisoDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mABSSisoScreenOnDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.maccWebCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.maccWebFailCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.maccWebFailPortal));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.maccWebReDHCPFailPortal));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.maccWebRoamFailPortal));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.maccWebSlowCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.maccWebSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mappDisabledAbCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mappDisabledScSucCnt));
            list.add(hwWifiDFT2StabilitySsidStat.mapVendorInfo);
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.marpReassocOkCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.marpUnreachCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.massocByABSCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.massocCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.massocDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.massocRejAbCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.massocRejFullCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.massocRejScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.massocSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mauthCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mauthDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mauthFailAbCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mauthFailScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mauthSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mbandWidth));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mblackListAbCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mblackListScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mconnectedCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mconnectedDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mconnectingDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mconnectTotalCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdhcpDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdhcpFailAbCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdhcpFailScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdhcpFailStaticScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdhcpRenewCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdhcpRenewDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdhcpRenewSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdhcpStaticSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdhcpSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdisconnCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdnsAbnormalCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdnsAvgTime));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdnsParseFailCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdnsReqCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdnsReqFail));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdnsResetScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mdnsScSucCnt));
            list.add(hwWifiDFT2StabilitySsidStat.meap);
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mfirstConnInetFailCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mfirstConnInetFailDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mfrequency));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mgatewayAbCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mgoodReConnCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mgoodReConnSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mgwIpCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mgwMacCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mgwResetScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mipAutoCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mipDhcpCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mipStaticCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.misMobleAP));
            list.add(hwWifiDFT2StabilitySsidStat.mkeyMgmt);
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mlinkspeed));
            list.add(hwWifiDFT2StabilitySsidStat.mMAC);
            list.add(hwWifiDFT2StabilitySsidStat.mMACHASH);
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mnoUserProcRunCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.monlyTxNoRxCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mOnSceenConnSucDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mOnSceenReConnCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mOnScreenAbDisconnCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mOnScreenConnCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mOnScreenConnSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mOnScreenDisconnCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mproxySetting));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mpublicEssCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mreassocScConnFailCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mreassScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mreDHCPAccWebSuccCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mreDhcpScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mrekeyCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mrekeyDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mrekeySucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mresetScConnFailCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mresetScSuccCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mroamAccWebSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mroamCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mroamDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mroamingAbCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mroamingResetScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mroamSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mRSSI));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mrssiAvg));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mSSID));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mstaticIpScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mtcpRxAbCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mweakReConnCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mweakReConnSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mwebFailDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mwebSlowDura));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mMaxspeed));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsDelayL1));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsDelayL2));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsDelayL3));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsDelayL4));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsDelayL5));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsDelayL6));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsRTTL1));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsRTTL2));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsRTTL3));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsRTTL4));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsRTTL5));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsStatFailNum));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsStatNoAckNum));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsStatRTT));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsStatSuccNum));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsStatTcpTotalNum));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsStatTotalNum));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDsStatWebDelay));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.muserEnableStaticIpCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mstaticIpConflictedScSucCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mrouterUnreachableCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mrouterDisplayNoInternetCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.minvalidIpScAbnormalCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.minvalidIpScSuccCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDnsCount));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDnsTotaldelay));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDnsIpv6TimeOut));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDnsFailCount));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDnsDelayL1));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDnsDelayL2));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDnsDelayL3));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDnsDelayL4));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDnsDelayL5));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mDnsDelayL6));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mMssSuccCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mMssFailCnt));
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mMssReasonCode));
            for (j = 0; j < 5; j++) {
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mTxBoostOnRTT[j]));
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mTxBoostOnRTTCnt[j]));
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mTxBoostOntxBad[j]));
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mTxBoostOntxGood[j]));
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mTxBoostOnTxRetry[j]));
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mTxBoostOffRTT[j]));
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mTxBoostOffRTTCnt[j]));
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mTxBoostOfftxBad[j]));
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mTxBoostOfftxGood[j]));
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mTxBoostOffTxRetry[j]));
            }
            for (j = 0; j < 3; j++) {
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mMssSuccCntArray[j]));
                list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mMssFailCntArray[j]));
            }
            list.add(String.valueOf(hwWifiDFT2StabilitySsidStat.mrouterModel));
            HwWifiDFTConnManager.getInstance().reportWifiDFTEvent(909001004, list);
        }
        List<HwWifiDFTAPKAction> listHwWifiDFTAPKAction = new ArrayList();
        HwWifiStatStoreImpl.getDefault().getWifiDFTAPKAction(listHwWifiDFTAPKAction);
        short apkActionSize = (short) listHwWifiDFTAPKAction.size();
        if (apkActionSize == (short) 0) {
            Log.d(TAG, "apk action size is null, do not need trigger.");
            HwWifiStatStoreImpl.getDefault().clearStatInfo();
            return;
        }
        Log.d(TAG, "HwWifiDFTConst.EID_WIFISTATUSCHANGEDBYAPK.");
        int wifiOpenStatDura = 0;
        if (this.mHwWifiStatStore != null) {
            wifiOpenStatDura = this.mHwWifiStatStore.getWifiOpenStatDura();
        }
        for (i = (short) 0; i < apkActionSize; i++) {
            list = new ArrayList();
            HwWifiDFTAPKAction apkAction = (HwWifiDFTAPKAction) listHwWifiDFTAPKAction.get(i);
            list.add(apkAction.mApkName);
            list.add(String.valueOf(apkAction.mApkAction));
            Log.d(TAG, "mApkName= " + apkAction.mApkName + " ,apkActionSize = " + apkActionSize);
            list.add(apkAction.mAPSsid);
            list.add(String.valueOf(apkAction.mclosewifiCnt));
            list.add(String.valueOf(apkAction.mdisableNetworkCnt));
            list.add(String.valueOf(apkAction.mdisconnectCnt));
            list.add(String.valueOf(apkAction.mopenwifiCnt));
            list.add(String.valueOf(apkAction.mreassocCnt));
            list.add(String.valueOf(apkAction.mreconnectCnt));
            list.add(String.valueOf(apkAction.mtriggerscanCnt));
            list.add(String.valueOf(apkAction.mwifiScanModeCnt));
            list.add(String.valueOf(apkAction.mforeGroundScanCnt));
            list.add(String.valueOf(apkAction.mtotalScanCnt));
            list.add(String.valueOf(apkAction.mscreenOnScanCnt));
            list.add(String.valueOf(apkAction.mscanFreqL1Cnt));
            list.add(String.valueOf(apkAction.mscanFreqL2Cnt));
            list.add(String.valueOf(apkAction.mscanFreqL3Cnt));
            list.add(String.valueOf(hwWifiDFT2StabilityStat.mconnectedDura));
            list.add(String.valueOf(wifiOpenStatDura));
            list.add(String.valueOf(apkAction.mcloseScanCnt));
            HwWifiDFTConnManager.getInstance().reportWifiDFTEvent(909002030, list);
        }
        HwWifiStatStoreImpl.getDefault().clearStatInfo();
    }

    public void handleUploadDFTEvent(int type, String ucSubErrorCode) {
        Log.d(TAG, "IMonitor upload event " + type);
        Date date = new Date();
        EventStream BTStatusInfo;
        EventStream wifiSettingsInfo;
        EventStream handsetStateInfo;
        EventStream apInfo;
        EventStream apConnectInfo;
        int rssiGroupLen;
        EventStream stream;
        EventStream CellIDInfo;
        EventStream apRoamInfo;
        EventStream bcmChipInfo;
        EventStream wifiNetConfigInfo;
        EventStream CpuMemInfo;
        EventStream tcpPacketInfo;
        EventStream netQualityInfo;
        EventStream wifiSDIOInfo;
        switch (type) {
            case 909002021:
                try {
                    if (this.mHwWifiDFTUtilImpl != null) {
                        EventStream eventOpenCloseFailed = IMonitor.openEventStream(909002021);
                        if (eventOpenCloseFailed == null) {
                            Log.e(TAG, "eventOpenCloseFailed is null.");
                            return;
                        }
                        BTStatusInfo = getClassEventStream(909009008);
                        wifiSettingsInfo = getClassEventStream(909009009);
                        handsetStateInfo = getClassEventStream(909009013);
                        eventOpenCloseFailed.setParam((short) 0, this.mOpenErrorCode).setParam((short) 1, this.mCloseErrorCode).setParam((short) 7, this.mIsAPOpen).setParam((short) 4, BTStatusInfo).setParam((short) 5, wifiSettingsInfo).setParam((short) 6, handsetStateInfo);
                        IMonitor.sendEvent(eventOpenCloseFailed);
                        IMonitor.closeEventStream(BTStatusInfo);
                        IMonitor.closeEventStream(wifiSettingsInfo);
                        IMonitor.closeEventStream(handsetStateInfo);
                        IMonitor.closeEventStream(eventOpenCloseFailed);
                        Log.d(TAG, "IMonitor send eventOpenCloseFailed");
                        return;
                    }
                    return;
                } catch (Exception e) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002021; Exception is: " + e);
                    return;
                }
            case 909002022:
                try {
                    EventStream abnormalDisconnect = IMonitor.openEventStream(909002022);
                    if (abnormalDisconnect == null) {
                        Log.e(TAG, "abnormalDisconnect is null.");
                        return;
                    }
                    if (this.mWifiNative == null) {
                        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
                    }
                    updateLTECoexInfo();
                    apInfo = getClassEventStream(909009001);
                    apConnectInfo = getClassEventStream(909009016);
                    BTStatusInfo = getClassEventStream(909009008);
                    wifiSettingsInfo = getClassEventStream(909009009);
                    handsetStateInfo = getClassEventStream(909009013);
                    abnormalDisconnect.setParam((short) 0, ucSubErrorCode).setParam((short) 1, this.mConnectType).setParam((short) 2, apInfo).setParam((short) 3, apConnectInfo).setParam((short) 5, BTStatusInfo).setParam((short) 6, this.mPublicEssCnt).setParam((short) 8, this.mAPVendorInfo).setParam((short) 9, wifiSettingsInfo).setParam((short) 10, 0).setParam((short) 11, this.wifiAntsStatus.getAntCurWork()).setParam((short) 13, handsetStateInfo).setParam((short) 15, this.mlterssi).setParam((short) 16, this.mlteEarfcn).setParam((short) 14, this.mWifiNative.getMssState());
                    rssiGroupLen = 0;
                    for (CSubRSSIGROUP_EVENT rssigroup : this.mRssiGroup.getRSSIGroupCHR()) {
                        if (rssiGroupLen < 4) {
                            stream = IMonitor.openEventStream(909009010);
                            if (stream == null) {
                                Log.e(TAG, "openEventStream error,type is : 909009010");
                                IMonitor.sendEvent(abnormalDisconnect);
                                IMonitor.closeEventStream(apInfo);
                                IMonitor.closeEventStream(apConnectInfo);
                                IMonitor.closeEventStream(BTStatusInfo);
                                IMonitor.closeEventStream(wifiSettingsInfo);
                                IMonitor.closeEventStream(handsetStateInfo);
                                IMonitor.closeEventStream(abnormalDisconnect);
                                resetWhenDisconnect();
                                Log.d(TAG, "IMonitor send abnormalDisconnect");
                                return;
                            }
                            stream.setParam((short) 0, rssigroup.ucRSSIGrpIndex.getValue()).setParam((short) 1, rssigroup.iSAME_FREQ_APS.getValue()).setParam((short) 2, rssigroup.iADJACENT_FREQ_APS.getValue());
                            rssiGroupLen++;
                            abnormalDisconnect.fillArrayParam((short) 7, stream);
                            IMonitor.closeEventStream(stream);
                        }
                    }
                    IMonitor.sendEvent(abnormalDisconnect);
                    IMonitor.closeEventStream(apInfo);
                    IMonitor.closeEventStream(apConnectInfo);
                    IMonitor.closeEventStream(BTStatusInfo);
                    IMonitor.closeEventStream(wifiSettingsInfo);
                    IMonitor.closeEventStream(handsetStateInfo);
                    IMonitor.closeEventStream(abnormalDisconnect);
                    resetWhenDisconnect();
                    Log.d(TAG, "IMonitor send abnormalDisconnect");
                    return;
                } catch (Exception e2) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002022; Exception is: " + e2);
                    return;
                }
            case 909002023:
                try {
                    EventStream connectedFailed = IMonitor.openEventStream(909002023);
                    if (connectedFailed == null) {
                        Log.e(TAG, "connectedFailed is null.");
                        return;
                    }
                    this.connect_event_flag = true;
                    updateLTECoexInfo();
                    apInfo = getClassEventStream(909009001);
                    apConnectInfo = getClassEventStream(909009016);
                    BTStatusInfo = getClassEventStream(909009008);
                    wifiSettingsInfo = getClassEventStream(909009009);
                    CellIDInfo = getClassEventStream(909009007);
                    handsetStateInfo = getClassEventStream(909009013);
                    if (this.mWifiNative == null) {
                        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
                    }
                    int connectType = 0;
                    if (this.mConnectType.equals("FIRST_CONNECT")) {
                        connectType = 1;
                    } else if (this.mConnectType.equals("SELECT_CONNECT")) {
                        connectType = 2;
                    } else if (this.mConnectType.equals("AUTO_CONNECT")) {
                        connectType = 3;
                    } else if (this.mConnectType.equals("WIFIPRO_CONNECT")) {
                        connectType = 4;
                    } else if (this.mConnectType.equals("ROAM_CONNECT")) {
                        connectType = 5;
                    } else if (this.mConnectType.equals("REASSOC")) {
                        connectType = 6;
                    }
                    connectedFailed.setParam((short) 0, connectType).setParam((short) 9, apInfo).setParam((short) 10, apConnectInfo).setParam((short) 12, BTStatusInfo).setParam((short) 13, this.mPublicEssCnt).setParam((short) 15, this.mAPVendorInfo).setParam((short) 16, wifiSettingsInfo).setParam((short) 17, 0).setParam((short) 18, CellIDInfo).setParam((short) 19, String.valueOf(this.mTriggerReportType)).setParam((short) 20, handsetStateInfo).setParam((short) 22, this.mlterssi).setParam((short) 23, this.mlteEarfcn).setParam((short) 21, this.mWifiNative.getMssState());
                    List<HwCHRAccessNetworkEventInfo> arrayList = new ArrayList(this.mHwCHRAccessNetworkEventInfoList);
                    Collections.reverse(arrayList);
                    int auth_len = 0;
                    int assoc_len = 0;
                    int dhcp_len = 0;
                    for (HwCHRAccessNetworkEventInfo tmpEvent : arrayList) {
                        if (tmpEvent.getEventId() == 82 && auth_len < 5) {
                            stream = IMonitor.openEventStream(909009003);
                            stream.setParam((short) 0, tmpEvent.getEventTriggerDate()).setParam((short) 2, tmpEvent.getSubErrorCode()).setParam((short) 3, tmpEvent.getAP_channel()).setParam((short) 4, tmpEvent.getAP_RSSI()).setParam((short) 5, (byte) tmpEvent.getIsOnScreen());
                            auth_len++;
                            connectedFailed.fillArrayParam((short) 6, stream);
                            IMonitor.closeEventStream(stream);
                        }
                        if (tmpEvent.getEventId() == 83 && assoc_len < 5) {
                            stream = IMonitor.openEventStream(909009004);
                            stream.setParam((short) 0, tmpEvent.getEventTriggerDate()).setParam((short) 2, tmpEvent.getSubErrorCode()).setParam((short) 3, tmpEvent.getAP_channel()).setParam((short) 4, tmpEvent.getAP_RSSI()).setParam((short) 5, (byte) tmpEvent.getIsOnScreen());
                            assoc_len++;
                            connectedFailed.fillArrayParam((short) 5, stream);
                            IMonitor.closeEventStream(stream);
                        }
                        if (tmpEvent.getEventId() == 84 && dhcp_len < 5) {
                            stream = IMonitor.openEventStream(909009002);
                            stream.setParam((short) 0, tmpEvent.getEventTriggerDate()).setParam((short) 1, tmpEvent.getDHCP_FAILED()).setParam((short) 3, tmpEvent.getAP_channel()).setParam((short) 4, tmpEvent.getAP_RSSI()).setParam((short) 5, (byte) tmpEvent.getIsOnScreen());
                            dhcp_len++;
                            connectedFailed.fillArrayParam((short) 7, stream);
                            IMonitor.closeEventStream(stream);
                        }
                    }
                    rssiGroupLen = 0;
                    for (CSubRSSIGROUP_EVENT rssigroup2 : this.mRssiGroup.getRSSIGroupCHR()) {
                        if (rssiGroupLen < 4) {
                            stream = IMonitor.openEventStream(909009010);
                            if (stream == null) {
                                Log.e(TAG, "openEventStream error,type is : 909009010");
                                IMonitor.sendEvent(connectedFailed);
                                IMonitor.closeEventStream(apConnectInfo);
                                IMonitor.closeEventStream(BTStatusInfo);
                                IMonitor.closeEventStream(wifiSettingsInfo);
                                IMonitor.closeEventStream(CellIDInfo);
                                IMonitor.closeEventStream(handsetStateInfo);
                                IMonitor.closeEventStream(connectedFailed);
                                IMonitor.closeEventStream(apInfo);
                                clearHwCHRAccessNetworkEventInfoList();
                                this.connect_event_flag = false;
                                Log.d(TAG, "IMonitor send connectedFailed");
                                return;
                            }
                            stream.setParam((short) 0, rssigroup2.ucRSSIGrpIndex.getValue()).setParam((short) 1, rssigroup2.iSAME_FREQ_APS.getValue()).setParam((short) 2, rssigroup2.iADJACENT_FREQ_APS.getValue());
                            rssiGroupLen++;
                            connectedFailed.fillArrayParam((short) 14, stream);
                            IMonitor.closeEventStream(stream);
                        }
                    }
                    IMonitor.sendEvent(connectedFailed);
                    IMonitor.closeEventStream(apConnectInfo);
                    IMonitor.closeEventStream(BTStatusInfo);
                    IMonitor.closeEventStream(wifiSettingsInfo);
                    IMonitor.closeEventStream(CellIDInfo);
                    IMonitor.closeEventStream(handsetStateInfo);
                    IMonitor.closeEventStream(connectedFailed);
                    IMonitor.closeEventStream(apInfo);
                    clearHwCHRAccessNetworkEventInfoList();
                    this.connect_event_flag = false;
                    Log.d(TAG, "IMonitor send connectedFailed");
                    return;
                } catch (Exception e22) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002023 Exception is : " + e22);
                    return;
                }
            case 909002024:
                try {
                    EventStream accessInternetFailed = IMonitor.openEventStream(909002024);
                    if (accessInternetFailed == null) {
                        Log.e(TAG, "accessInternetFailed is null.");
                        return;
                    }
                    boolean z;
                    this.mAWS = HwArpVerifier.getDefault().getAccessWebStatus();
                    updateLTECoexInfo();
                    apRoamInfo = getClassEventStream(909009005);
                    bcmChipInfo = getClassEventStream(909009006);
                    CellIDInfo = getClassEventStream(909009007);
                    BTStatusInfo = getClassEventStream(909009008);
                    wifiSettingsInfo = getClassEventStream(909009009);
                    apInfo = getClassEventStream(909009001);
                    wifiNetConfigInfo = getClassEventStream(909009011);
                    CpuMemInfo = getClassEventStream(909009012);
                    handsetStateInfo = getClassEventStream(909009013);
                    tcpPacketInfo = getClassEventStream(909009014);
                    netQualityInfo = getClassEventStream(909009015);
                    apConnectInfo = getClassEventStream(909009016);
                    wifiSDIOInfo = getClassEventStream(909009017);
                    if (this.mWifiNative == null) {
                        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
                    }
                    EventStream param = accessInternetFailed.setParam((short) 0, ucSubErrorCode).setParam((short) 1, this.mConnectType).setParam((short) 2, 0).setParam((short) 3, apInfo).setParam((short) 4, apConnectInfo).setParam((short) 6, this.mPublicEssCnt).setParam((short) 7, this.mAPVendorInfo).setParam((short) 8, wifiSettingsInfo).setParam((short) 9, this.mMultiGWCount).setParam((short) 10, this.mAWS.getRTTArp()).setParam((short) 11, this.mAWS.getRTTBaidu()).setParam((short) 12, 0).setParam((short) 14, BTStatusInfo).setParam((short) 15, CellIDInfo).setParam((short) 16, wifiNetConfigInfo).setParam((short) 17, tcpPacketInfo).setParam((short) 18, CpuMemInfo).setParam((short) 21, wifiSDIOInfo).setParam((short) 22, bcmChipInfo).setParam((short) 24, apRoamInfo).setParam((short) 25, this.mPortalStatus).setParam((short) 26, netQualityInfo).setParam((short) 27, handsetStateInfo).setParam((short) 29, this.mlterssi).setParam((short) 30, this.mlteEarfcn);
                    if (this.str_gate_ip.equals("")) {
                        z = true;
                    } else {
                        z = false;
                    }
                    param.setParam((short) 31, Boolean.valueOf(z)).setParam((short) 28, this.mWifiNative.getMssState());
                    try {
                        int[] currentDnsFailCNT = new int[6];
                        getDnsErrorCnt(currentDnsFailCNT);
                        if (!this.hadSentDnsReport) {
                            System.arraycopy(this.mdnsWifiStartFailCnt, 0, this.mdnsLastReportFailCnt, 0, 6);
                        }
                        for (int i = 0; i < 6; i++) {
                            stream = IMonitor.openEventStream(909009028);
                            if (stream == null) {
                                Log.e(TAG, "openEventStream error,type is : 909009028");
                                this.hadSentDnsReport = true;
                                IMonitor.sendEvent(accessInternetFailed);
                                IMonitor.closeEventStream(apRoamInfo);
                                IMonitor.closeEventStream(bcmChipInfo);
                                IMonitor.closeEventStream(CellIDInfo);
                                IMonitor.closeEventStream(BTStatusInfo);
                                IMonitor.closeEventStream(wifiSettingsInfo);
                                IMonitor.closeEventStream(apInfo);
                                IMonitor.closeEventStream(wifiNetConfigInfo);
                                IMonitor.closeEventStream(CpuMemInfo);
                                IMonitor.closeEventStream(handsetStateInfo);
                                IMonitor.closeEventStream(tcpPacketInfo);
                                IMonitor.closeEventStream(netQualityInfo);
                                IMonitor.closeEventStream(apConnectInfo);
                                IMonitor.closeEventStream(wifiSDIOInfo);
                                IMonitor.closeEventStream(accessInternetFailed);
                                Log.d(TAG, "IMonitor send accessInternetFailed");
                                return;
                            }
                            stream.setParam((short) 0, i).setParam((short) 1, currentDnsFailCNT[i] - this.mdnsLastReportFailCnt[i]);
                            accessInternetFailed.fillArrayParam((short) 33, stream);
                            Log.e(TAG, "openEventStream error, i = " + i + " currentDnsFail is : " + currentDnsFailCNT[i] + " mdnsLastReportFailCnt[i] = " + this.mdnsLastReportFailCnt[i]);
                            this.mdnsLastReportFailCnt[i] = currentDnsFailCNT[i];
                            IMonitor.closeEventStream(stream);
                        }
                        this.hadSentDnsReport = true;
                    } catch (Exception e222) {
                        Log.e(TAG, "set Class EventStream dnsFailCntInfo error." + e222);
                    }
                    IMonitor.sendEvent(accessInternetFailed);
                    IMonitor.closeEventStream(apRoamInfo);
                    IMonitor.closeEventStream(bcmChipInfo);
                    IMonitor.closeEventStream(CellIDInfo);
                    IMonitor.closeEventStream(BTStatusInfo);
                    IMonitor.closeEventStream(wifiSettingsInfo);
                    IMonitor.closeEventStream(apInfo);
                    IMonitor.closeEventStream(wifiNetConfigInfo);
                    IMonitor.closeEventStream(CpuMemInfo);
                    IMonitor.closeEventStream(handsetStateInfo);
                    IMonitor.closeEventStream(tcpPacketInfo);
                    IMonitor.closeEventStream(netQualityInfo);
                    IMonitor.closeEventStream(apConnectInfo);
                    IMonitor.closeEventStream(wifiSDIOInfo);
                    IMonitor.closeEventStream(accessInternetFailed);
                    Log.d(TAG, "IMonitor send accessInternetFailed");
                    return;
                } catch (Exception e2222) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002024; Exception is: " + e2222);
                    return;
                }
            case 909002025:
                try {
                    EventStream accessWebSlowly = IMonitor.openEventStream(909002025);
                    if (accessWebSlowly == null) {
                        Log.e(TAG, "accessWebSlowly is null.");
                        return;
                    }
                    this.mAWS = HwArpVerifier.getDefault().getAccessWebStatus();
                    updateLTECoexInfo();
                    apRoamInfo = getClassEventStream(909009005);
                    bcmChipInfo = getClassEventStream(909009006);
                    CellIDInfo = getClassEventStream(909009007);
                    BTStatusInfo = getClassEventStream(909009008);
                    wifiSettingsInfo = getClassEventStream(909009009);
                    apInfo = getClassEventStream(909009001);
                    wifiNetConfigInfo = getClassEventStream(909009011);
                    CpuMemInfo = getClassEventStream(909009012);
                    handsetStateInfo = getClassEventStream(909009013);
                    tcpPacketInfo = getClassEventStream(909009014);
                    netQualityInfo = getClassEventStream(909009015);
                    apConnectInfo = getClassEventStream(909009016);
                    wifiSDIOInfo = getClassEventStream(909009017);
                    EventStream gameLagInfo = getClassEventStream(909009026);
                    if (this.mWifiNative == null) {
                        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
                    }
                    int repeaterStatus = getRepeaterStatus();
                    String repeaterValue = "";
                    if (repeaterStatus == WIFI_REPEATER_CLOSED) {
                        repeaterValue = "CLOSED";
                    } else if (repeaterStatus == WIFI_REPEATER_OPENED) {
                        repeaterValue = "OPENED";
                    } else if (repeaterStatus == WIFI_REPEATER_TETHER) {
                        repeaterValue = "WORKING";
                    }
                    accessWebSlowly.setParam((short) 0, ucSubErrorCode).setParam((short) 1, this.mConnectType).setParam((short) 3, apInfo).setParam((short) 4, apConnectInfo).setParam((short) 6, this.mPublicEssCnt).setParam((short) 7, this.mAPVendorInfo).setParam((short) 8, wifiSettingsInfo).setParam((short) 9, BTStatusInfo).setParam((short) 10, CellIDInfo).setParam((short) 11, wifiNetConfigInfo).setParam((short) 12, tcpPacketInfo).setParam((short) 13, CpuMemInfo).setParam((short) 16, bcmChipInfo).setParam((short) 17, this.mMultiGWCount).setParam((short) 18, wifiSDIOInfo).setParam((short) 19, this.mAWS.getRTTArp()).setParam((short) 20, this.mAWS.getRTTBaidu()).setParam((short) 21, 0).setParam((short) 22, this.mPortalStatus).setParam((short) 23, apRoamInfo).setParam((short) 24, repeaterValue).setParam((short) 25, getRepeaterFreq()).setParam((short) 26, netQualityInfo).setParam((short) 27, handsetStateInfo).setParam((short) 30, this.mlterssi).setParam((short) 31, this.mlteEarfcn).setParam((short) 28, this.mWifiNative.getMssState()).setParam((short) 32, gameLagInfo);
                    IMonitor.sendEvent(accessWebSlowly);
                    IMonitor.closeEventStream(apRoamInfo);
                    IMonitor.closeEventStream(bcmChipInfo);
                    IMonitor.closeEventStream(CellIDInfo);
                    IMonitor.closeEventStream(BTStatusInfo);
                    IMonitor.closeEventStream(wifiSettingsInfo);
                    IMonitor.closeEventStream(apInfo);
                    IMonitor.closeEventStream(wifiNetConfigInfo);
                    IMonitor.closeEventStream(CpuMemInfo);
                    IMonitor.closeEventStream(handsetStateInfo);
                    IMonitor.closeEventStream(tcpPacketInfo);
                    IMonitor.closeEventStream(netQualityInfo);
                    IMonitor.closeEventStream(apConnectInfo);
                    IMonitor.closeEventStream(wifiSDIOInfo);
                    IMonitor.closeEventStream(accessWebSlowly);
                    IMonitor.closeEventStream(gameLagInfo);
                    Log.d(TAG, "IMonitor send accessWebSlowly");
                    return;
                } catch (Exception e22222) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002025; Exception is: " + e22222);
                    return;
                }
            case 909002026:
                try {
                    EventStream deviceFirmwareException110X = IMonitor.openEventStream(909002026);
                    if (deviceFirmwareException110X == null) {
                        Log.e(TAG, "deviceFirmwareException110X is null.");
                        return;
                    }
                    deviceFirmwareException110X.setParam((short) 0, ucSubErrorCode);
                    IMonitor.sendEvent(deviceFirmwareException110X);
                    IMonitor.closeEventStream(deviceFirmwareException110X);
                    return;
                } catch (Exception e222222) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002026; Exception is: " + e222222);
                    return;
                }
            case 909002027:
                try {
                    EventStream wifiProException = IMonitor.openEventStream(909002027);
                    if (wifiProException == null) {
                        Log.e(TAG, "wifiProException is null.");
                        return;
                    }
                    wifiProException.setParam((short) 0, 0);
                    IMonitor.sendEvent(wifiProException);
                    IMonitor.closeEventStream(wifiProException);
                    return;
                } catch (Exception e2222222) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002027; Exception is: " + e2222222);
                    return;
                }
            case 909002029:
                try {
                    EventStream apModelCollect = IMonitor.openEventStream(909002029);
                    if (apModelCollect == null) {
                        Log.e(TAG, "apModelCollect is null.");
                        return;
                    }
                    EventStream MacSsidInfo = getClassEventStream(909009018);
                    apModelCollect.setParam((short) 2, this.mRouterBrand).setParam((short) 0, MacSsidInfo).setParam((short) 4, this.mApStreamInfo).setParam((short) 5, this.mAPVendorInfo).setParam((short) 1, this.misAPRecognized).setParam((short) 3, this.mRouterModel);
                    IMonitor.sendEvent(apModelCollect);
                    IMonitor.closeEventStream(MacSsidInfo);
                    IMonitor.closeEventStream(apModelCollect);
                    return;
                } catch (Exception e22222222) {
                    Log.e(TAG, "uploadDFTEvent error. The type is: 909002029; Exception is: " + e22222222);
                    return;
                }
            case 909002032:
                EventStream wifiProStatistic = IMonitor.openEventStream(909002032);
                if (wifiProStatistic == null) {
                    Log.e(TAG, "wifiProStatistic is null.");
                    return;
                }
                wifiProStatistic.setParam((short) 2, this.mWifiOobInitState).setParam((short) 9, this.mWifiToWifiSuccCount).setParam((short) 10, this.mNoInetHandoverCount).setParam((short) 11, this.mTotalBQE_BadROC).setParam((short) 12, this.mManualBackROC).setParam((short) 13, this.mUserCancelROC).setParam((short) 31, this.mCellAutoOpenCount).setParam((short) 32, this.mCellAutoCloseCount).setParam((short) 33, this.mPingPongCount).setParam((short) 45, this.mBMD_TenMNotifyCount).setParam((short) 46, this.mBMD_TenM_RI_Count).setParam((short) 47, this.mBMD_FiftyMNotifyCount).setParam((short) 48, this.mBMD_FiftyM_RI_Count).setParam((short) 50, this.mRO_TotMobileData).setParam((short) 51, this.mAF_PhoneNumSuccCnt).setParam((short) 53, this.mAF_PasswordSuccCnt).setParam((short) 55, this.mAF_AutoLoginSuccCnt).setParam((short) 66, this.mBG_FreeInetOkApCnt).setParam((short) 67, this.mBG_FishingApCnt).setParam((short) 68, this.mBG_FreeNotInetApCnt).setParam((short) 69, this.mBG_PortalApCnt).setParam((short) 83, this.mBG_NCByConnectFail).setParam((short) 84, this.mBG_NCByCheckFail).setParam((short) 115, this.mManualConnBlockPortalCount).setParam((short) 117, this.mWifiproOpenCount).setParam((short) 118, this.mWifiproCloseCount);
                IMonitor.sendEvent(wifiProStatistic);
                IMonitor.closeEventStream(wifiProStatistic);
                return;
            case 909002038:
                EventStream mssSwitchFailEvent = IMonitor.openEventStream(909002038);
                if (mssSwitchFailEvent == null) {
                    Log.e(TAG, "mssSwitchFailEvent is null.");
                    return;
                }
                apInfo = getClassEventStream(909009001);
                EventStream mssPkt = IMonitor.openEventStream(909009024);
                if (mssPkt != null) {
                    mssPkt.setParam((short) 2, this.mMssTxGood).setParam((short) 2, this.mMssTxBad);
                }
                mssSwitchFailEvent.setParam((short) 0, (byte) this.mSwitchType).setParam((short) 1, (byte) this.mABSState).setParam((short) 2, (byte) this.mReasonCode).setParam((short) 3, (byte) this.mDisconnectCode).setParam((short) 4, this.mAPVendorInfo).setParam((short) 5, apInfo).setParam((short) 6, (byte) this.mScreenState).setParam((short) 7, (byte) this.mApStreamInfo).setParam((short) 8, mssPkt).setParam((short) 9, this.mMssTriggerReason).setParam((short) 10, this.mMssTemperature).setParam((short) 11, this.mMssScene).setParam((short) 12, this.mMssTemperLevel);
                IMonitor.sendEvent(mssSwitchFailEvent);
                IMonitor.closeEventStream(apInfo);
                IMonitor.closeEventStream(mssPkt);
                IMonitor.closeEventStream(mssSwitchFailEvent);
                return;
            default:
                Log.w(TAG, "IMonitor unknown event.");
                return;
        }
    }

    public void handleUpdateMSSCHR(int switchType, int absState, int reasonCode, ArrayList list) {
        if (list != null) {
            this.mMssTxGood = ((Integer) list.get(0)).intValue();
            this.mMssTxBad = ((Integer) list.get(1)).intValue();
            this.mMssTemperLevel = ((Integer) list.get(2)).intValue();
            this.mMssTemperature = ((Integer) list.get(3)).intValue();
            this.mMssTriggerReason = ((Integer) list.get(5)).intValue();
            switch (((Integer) list.get(4)).intValue()) {
                case DEFAULT /*10000*/:
                    this.mMssScene = "default_config";
                    break;
                case BROWSER_APP /*10001*/:
                    this.mMssScene = "browser_app";
                    break;
                case GAME_3D /*10002*/:
                    this.mMssScene = "3D_game";
                    break;
                case EBOOK /*10003*/:
                    this.mMssScene = "ebook";
                    break;
                case CAMERA /*10007*/:
                    this.mMssScene = "camera";
                    break;
                case VIDEO /*10009*/:
                    this.mMssScene = "video";
                    break;
                case GAME_2D /*10011*/:
                    this.mMssScene = "2D_game";
                    break;
                default:
                    this.mMssScene = "while_list";
                    break;
            }
        }
        if (this.mHwWifiStatStore != null) {
            this.mHwWifiStatStore.updateMssSucCont(this.mMssTriggerReason, reasonCode);
        }
        this.mDisconnectCode = -1;
        long now = SystemClock.elapsedRealtime();
        if (reasonCode != 0) {
            this.mSwitchType = switchType;
            this.mABSState = absState;
            this.mReasonCode = reasonCode;
            if (reasonCode == this.MSS_DISCONNECT) {
                this.mDisconnectCode = this.mABSReassFaileReason;
            }
            this.mFirstMssReportTimeStamp = this.mMssReportCnt == 0 ? now : this.mFirstMssReportTimeStamp;
            if (now - this.mFirstMssReportTimeStamp >= COMM_UPLOAD_MIN_SPAN) {
                this.mFirstMssReportTimeStamp = now;
                this.mMssReportCnt = 0;
            }
            if (this.mMssReportCnt < 50) {
                handleUploadDFTEvent(909002038, "");
                this.mMssReportCnt++;
            } else {
                Log.d(TAG, "do not report MSS exception,because mMssReportCnt is beyond threshold in one day");
            }
        }
        Log.d(TAG, "update MSS CHR switch direction is:" + switchType + " ,trigger reason is:" + this.mMssTriggerReason + " ,absState is:" + absState + " ,reasonCode is:" + reasonCode + " ,disconectCode is:" + this.mDisconnectCode + " ,Scene is:" + this.mMssScene + " ,temperature is:" + this.mMssTemperature + " ,level is:" + this.mMssTemperLevel);
    }

    private String maskMacAddress(String macAddress) {
        if (macAddress != null) {
            if (macAddress.split(HwQoEUtils.SEPARATOR).length >= 4) {
                return String.format("%s:%s:%s:%s:FF:FF", new Object[]{macAddress.split(HwQoEUtils.SEPARATOR)[0], macAddress.split(HwQoEUtils.SEPARATOR)[1], macAddress.split(HwQoEUtils.SEPARATOR)[2], macAddress.split(HwQoEUtils.SEPARATOR)[3]});
            }
        }
        return macAddress;
    }

    private String maskIpAddress(String ipAddress) {
        if (ipAddress != null) {
            if (ipAddress.split("\\.").length >= 3) {
                return String.format("%s.%s.%s.XXX", new Object[]{ipAddress.split("\\.")[0], ipAddress.split("\\.")[1], ipAddress.split("\\.")[2]});
            }
        }
        return ipAddress;
    }

    private void handleUpdateAP(String iface, int networkId, SupplicantState newSupplicantState, String BSSID) {
        if ("wlan0".equals(iface)) {
            updateAPMAC(BSSID);
            updateApMessage();
            if (this.mHwWifiStatStore != null) {
                boolean wifiprotempflag = false;
                if (networkId != -1 && newSupplicantState == SupplicantState.ASSOCIATING) {
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

    private void handleCheckAppName(WifiConfiguration config, Context mContext) {
        if (config.callingPid > 0) {
            String appNameOfUI = "com.android.settings";
            String callingAppName = getAppName(config.callingPid, mContext);
            if (callingAppName == null || (appNameOfUI.equals(callingAppName) ^ 1) != 0) {
                config.callingPid = 0;
            }
        }
    }

    public void LinkPropertiesUpdate(RouteInfo route) {
        if (route.isDefaultRoute()) {
            updateGateWay(route.getGateway().getHostAddress());
            updateStrucRoutes(route.getInterface());
            log("updateLinkProperties strucRoutes:" + route.getInterface());
        }
    }

    private void handleIpv4SuccessException(DhcpResults dhcpResults) {
        if (dhcpResults != null) {
            String ipaddr = String.valueOf(dhcpResults.ipAddress);
            String gateway = String.valueOf(dhcpResults.gateway);
            updateWifiIp(ipaddr);
            updateGateWay(gateway);
            updateDNS(dhcpResults.dnsServers);
            updateTimeStampSessionFinish(SystemClock.elapsedRealtime());
            updateConnectSuccessTime();
            if (HwWifiDFTConnManager.getInstance().isCommercialUser()) {
                clearHwCHRAccessNetworkEventInfoList();
            } else {
                updateTimeStampSessionFinish(SystemClock.elapsedRealtime());
                handleReportHwCHRAccessNetworkEventInfoList(4);
            }
            if (this.mHwWifiStatStore != null) {
                this.mHwWifiStatStore.setIPv4SuccFlag();
            }
            this.mTimeStampSessionStart = 0;
            this.mTimeStampSessionFirstConnect = 0;
            this.mTimeStampSessionFinish = 0;
            this.mConnectSuccessTime = 0;
        }
    }

    private boolean isIPMatchGateWay() {
        boolean z = true;
        if (this.str_Wifi_ip_org == null || this.str_gate_ip_org == null) {
            return false;
        }
        String[] IPparam = this.str_Wifi_ip_org.split("\\/");
        if (IPparam.length != 2 || (IPparam[1].matches("[0-9]+") ^ 1) != 0) {
            return false;
        }
        long ipmask = ((long) Math.scalb(1.0f, 32)) - ((long) Math.scalb(1.0f, 32 - Integer.parseInt(checkStr(IPparam[1]))));
        if ((ipToNum(IPparam[0]) & ipmask) != (ipToNum(this.str_gate_ip_org) & ipmask)) {
            z = false;
        }
        return z;
    }

    private long ipToNum(String ip) {
        long num = 0;
        String[] str = ip.split("\\.");
        int i = 3;
        for (int j = 0; j < str.length; j++) {
            if (!str[j].matches("[0-9]+")) {
                str[j] = checkStr(str[j]);
            }
            try {
                num += Long.parseLong(str[j]) << (i * 8);
                i--;
            } catch (NumberFormatException e) {
                loge("IP Has Format Exception");
            }
        }
        return num;
    }

    private String checkStr(String str) {
        if (str == null) {
            return "0";
        }
        int n = str.length();
        char[] a = new char[n];
        int i = 0;
        int len = 0;
        while (i < n) {
            int len2;
            char ch = str.charAt(i);
            if (ch < '0' || ch > '9') {
                len2 = len;
            } else {
                len2 = len + 1;
                a[len] = ch;
            }
            i++;
            len = len2;
        }
        if (len == 0) {
            return "0";
        }
        return new String(a, 0, len);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x005b  */
    /* JADX WARNING: Removed duplicated region for block: B:24:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0054  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    if (scanner == null) {
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
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            loge("could not open wifi state sys node" + e);
            if (scanner == null) {
            }
        }
    }

    public void setLastNetIdFromUI(WifiConfiguration config, int netId) {
        this.mIsHiddenSsid = config.hiddenSSID;
        if (config.callingPid > 0) {
            setLastNetIdFromUI(netId);
            config.callingPid = 0;
            loge("CONNECT_NETWORK config is from UI");
            return;
        }
        setLastNetIdFromUI(-1);
    }

    private void handleSupplicantExceptions() {
        int count = 0;
        String supplicantStauts = SystemProperties.get("init.svc.p2p_supplicant", "running");
        while (!"stopped".equals(supplicantStauts) && count < 5) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            count++;
            supplicantStauts = SystemProperties.get("init.svc.p2p_supplicant", "running");
            log("supplicantStauts = " + supplicantStauts + "count = " + count);
        }
        if (!"stopped".equals(supplicantStauts)) {
            updateWifiException(81, "CLOSE_SUPPLICANT_CONNECT_FAILED");
        }
    }

    public int updateWifiException(int mScanFailedCount) {
        mScanFailedCount++;
        if (mScanFailedCount < 10) {
            return mScanFailedCount;
        }
        updateWifiException(86, "");
        return 0;
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

    public void processWifiHalDriverEvent(String devErrData) {
        Log.d(TAG, "processWifiHalDriverEvent , devErrData is " + devErrData);
        updateWifiException(HwSelfCureUtils.RESET_LEVEL_DEAUTH_BSSID, devErrData);
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

    public boolean getWifiAntsInfo(int items) {
        String strRet = "";
        if (HWFLOW) {
            Log.d(TAG, "Enter getAntsInfo");
        }
        if (!this.mDualAntsChr) {
            return false;
        }
        int param;
        if (-1 == items) {
            param = ((((1 | 2) | 4) | 8) | 16) | 32;
        } else {
            param = items;
        }
        try {
            strRet = get_wifi_ants_info(param);
            if (strRet == null || strRet.isEmpty()) {
                if (HWFLOW) {
                    Log.d(TAG, "getAntsInfo: JNI call ret == null, return");
                }
                return false;
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
                    boolean isAntSWCauseError = antSWCauseError == 1;
                    this.wifiAntsStatus.setAntCurWork(antCurWork);
                    this.wifiAntsStatus.setAvgRSSI(this.mAvgRssi);
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
                    return false;
                }
            } catch (JSONException e2) {
                Log.e(TAG, "new JSON failed");
                return false;
            }
        } catch (Exception e3) {
            Log.e(TAG, "get_wifi_ants_info Exception");
            return false;
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

    public int getRepeaterMaxClientCount() {
        return this.mRepeterMaxClientCount;
    }

    public int getRepeaterConnFailedCount() {
        return this.mRepeterConnFailedCount;
    }

    public int getWifiRepeaterOpenedCount() {
        return this.mWifiRepeaterOpenedCount;
    }

    public long getWifiRepeaterWorkingTime() {
        return this.mWifiRepeaterWorkingTime;
    }

    public int getDiffFreqStationRepeaterDuration() {
        return this.mDiffFreqStationRepeaterDuration;
    }

    public int getRepeaterFreq() {
        HwWifiServiceManager hwWifiServiceManager = HwWifiServiceFactory.getHwWifiServiceManager();
        if (hwWifiServiceManager == null || !(hwWifiServiceManager instanceof HwWifiServiceManagerImpl)) {
            return 0;
        }
        WifiP2pServiceImpl wifiP2PServiceImpl = ((HwWifiServiceManagerImpl) hwWifiServiceManager).getHwWifiP2pService();
        if (wifiP2PServiceImpl == null || !(wifiP2PServiceImpl instanceof HwWifiP2pService)) {
            return 0;
        }
        return ((HwWifiP2pService) wifiP2PServiceImpl).getWifiRepeaterFreq();
    }

    public int getRepeaterStatus() {
        int ret = WIFI_REPEATER_CLOSED;
        boolean isWifiRepeaterOpened = false;
        boolean bRepeterTether = false;
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

    private void getDnsErrorCnt(int[] dnsfailcnt) {
        String dnscnt = SystemProperties.get("hw.wifipro.dns_err_count", "0");
        Log.e(TAG, "openEventStream error,dnscnt is : " + dnscnt);
        String[] counterStr = dnscnt.split(",");
        if (counterStr.length == 6) {
            for (int i = 0; i < 6; i++) {
                dnsfailcnt[i] = Integer.parseInt(counterStr[i]);
            }
        }
    }
}
