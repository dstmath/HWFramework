package com.android.server.wifi;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.KeepalivePacketData;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.NetworkInfo;
import android.net.StaticIpConfiguration;
import android.net.ip.IpClient;
import android.net.wifi.HwInnerNetworkManagerImpl;
import android.net.wifi.ParcelUtil;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiSsid;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.wifipro.HwNetworkAgent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.LruCache;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.IState;
import com.android.server.hidata.mplink.HwMpLinkContentAware;
import com.android.server.os.GetUDIDNative;
import com.android.server.wifi.ABS.HwABSDetectorService;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.MSS.HwMSSArbitrager;
import com.android.server.wifi.MSS.HwMSSHandler;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.util.StringUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.WifiCommonUtils;
import com.android.server.wifi.wifipro.HwAutoConnectManager;
import com.android.server.wifi.wifipro.HwDualBandManager;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiProConfigStore;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifi.wifipro.WifiproUtils;
import com.android.server.wifi.wifipro.hwintelligencewifi.HwIntelligenceWiFiManager;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

public class HwWifiStateMachine extends WifiStateMachine {
    public static final int AP_CAP_CACHE_COUNT = 1000;
    public static final String AP_CAP_KEY = "AP_CAP";
    private static final String ASSOCIATION_REJECT_STATUS_CODE = "wifi_association_reject_status_code";
    private static final String BRCM_CHIP_4359 = "bcm4359";
    public static final String BSSID_KEY = "BSSID";
    public static final int CMD_AP_STARTED_GET_STA_LIST = 131104;
    public static final int CMD_AP_STARTED_SET_DISASSOCIATE_STA = 131106;
    public static final int CMD_AP_STARTED_SET_MAC_FILTER = 131105;
    static final int CMD_GET_CHANNEL_LIST_5G = 131572;
    public static final int CMD_SCREEN_OFF_SCAN = 131578;
    public static final int CMD_STOP_WIFI_REPEATER = 131577;
    public static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    private static final String DBKEY_HOTSPOT20_VALUE = "hw_wifi_hotspot2_on";
    private static final int DEFAULT_ARP_DETECT_TIME = 5;
    private static final int DEFAULT_ARP_TIMEOUT_MS = 1000;
    private static final int DHCP_RESULT_CACHE_SIZE = 50;
    public static final int EID_WIFI_APK_CHANGE_WIFI_CONFIG_INFO = 909002071;
    public static final int ENTERPRISE_HOTSPOT_THRESHOLD = 4;
    private static final String HIGEO_PACKAGE_NAME = "com.huawei.lbs";
    private static final int HIGEO_STATE_DEFAULT_MODE = 0;
    private static final int HIGEO_STATE_WIFI_SCAN_MODE = 1;
    private static final String HUAWEI_SETTINGS = "com.android.settings.Settings$WifiSettingsActivity";
    public static final int PM_LOWPWR = 7;
    public static final int PM_NORMAL = 6;
    public static final int SCAN_ONLY_CONNECT_MODE = 100;
    private static final String SOFTAP_IFACE = "wlan0";
    private static final int SUCCESS = 1;
    public static final String SUPPLICANT_WAPI_EVENT = "android.net.wifi.supplicant.WAPI_EVENT";
    private static final String TAG = "HwWifiStateMachine";
    private static final long TIMEOUT_CONTROL_SCAN_ASSOCIATED = 5000;
    private static final long TIMEOUT_CONTROL_SCAN_ASSOCIATING = 2000;
    public static final String TX_MCS_SET = "TX_MCS_SET";
    private static final String USB_SUPPLY = "/sys/class/power_supply/USB/online";
    private static final String USB_SUPPLY_QCOM = "/sys/class/power_supply/usb/online";
    public static final int WAPI_AUTHENTICATION_FAILURE_EVENT = 147474;
    public static final int WAPI_CERTIFICATION_FAILURE_EVENT = 147475;
    public static final int WAPI_EVENT_AUTH_FAIL_CODE = 16;
    public static final int WAPI_EVENT_CERT_FAIL_CODE = 17;
    private static final String WIFI_EVALUATE_TAG = "wifipro_recommending_access_points";
    private static final int WIFI_GLOBAL_SCAN_CTRL_FOUL_INTERVAL = 5000;
    private static final int WIFI_GLOBAL_SCAN_CTRL_FREED_INTERVAL = 10000;
    private static final int WIFI_LINK_DETECT_CNT = 3;
    private static final int WIFI_MAX_FOUL_TIMES = 5;
    private static final int WIFI_MAX_FREED_TIMES = 5;
    private static final long WIFI_SCAN_BLACKLIST_REMOVE_INTERVAL = 7200000;
    private static final String WIFI_SCAN_CONNECTED_LIMITED_WHITE_PACKAGENAME = "wifi_scan_connected_limited_white_packagename";
    private static final String WIFI_SCAN_INTERVAL_WHITE_WLAN_CONNECTED = "wifi_scan_interval_white_wlan_connected";
    private static final String WIFI_SCAN_INTERVAL_WLAN_CLOSE = "wifi_scan_interval_wlan_close";
    private static final long WIFI_SCAN_INTERVAL_WLAN_CLOSE_DEFAULT = 20000;
    private static final String WIFI_SCAN_INTERVAL_WLAN_NOT_CONNECTED = "wifi_scan_interval_wlan_not_connected";
    private static final long WIFI_SCAN_INTERVAL_WLAN_NOT_CONNECTED_DEFAULT = 10000;
    private static final long WIFI_SCAN_INTERVAL_WLAN_WHITE_CONNECTED_DEFAULT = 10000;
    private static final long WIFI_SCAN_OVER_INTERVAL_MAX_COUNT = 10;
    private static final long WIFI_SCAN_RESULT_DELAY_TIME_DEFAULT = 300;
    private static final String WIFI_SCAN_WHITE_PACKAGENAME = "wifi_scan_white_packagename";
    private static final int WIFI_START_EVALUATE_TAG = 1;
    private static final int WIFI_STOP_EVALUATE_TAG = 0;
    private static int mFrequency = 0;
    private static WifiNativeUtils wifiNativeUtils = EasyInvokeFactory.getInvokeUtils(WifiNativeUtils.class);
    /* access modifiers changed from: private */
    public static WifiStateMachineUtils wifiStateMachineUtils = EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class);
    private boolean isInGlobalScanCtrl = false;
    private long lastConnectTime = -1;
    private HashMap<String, String> lastDhcps = new HashMap<>();
    private long lastScanResultTimestamp = 0;
    private ActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public int mBQEUid;
    private BroadcastReceiver mBcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String chipName = SystemProperties.get("ro.connectivity.sub_chiptype", "");
            WifiInfo wifiInfo = HwWifiStateMachine.wifiStateMachineUtils.getWifiInfo(HwWifiStateMachine.this);
            boolean isMobileAP = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(HwWifiStateMachine.this.myContext);
            if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                int PluggedType = intent.getIntExtra("plugged", 0);
                if (PluggedType == 2 || PluggedType == 5) {
                    boolean unused = HwWifiStateMachine.this.mIsScanCtrlPluggedin = true;
                } else if (!HwWifiStateMachine.this.getChargingState()) {
                    boolean unused2 = HwWifiStateMachine.this.mIsScanCtrlPluggedin = false;
                }
                HwWifiStateMachine hwWifiStateMachine = HwWifiStateMachine.this;
                hwWifiStateMachine.logd("mBcastReceiver: PluggedType = " + PluggedType + " mIsScanCtrlPluggedin = " + HwWifiStateMachine.this.mIsScanCtrlPluggedin);
            } else if ("android.intent.action.SCREEN_OFF".equals(action) && wifiInfo != null && wifiInfo.getNetworkId() != -1 && !HwWifiStateMachine.this.getChargingState() && !HwWifiStateMachine.this.isWifiRepeaterStarted()) {
                Log.d(HwWifiStateMachine.TAG, "SCREEN_OFF, startFilteringMulticastPackets");
                HwWifiStateMachine.this.setScreenOffMulticastFilter(true);
            }
            if (HwWifiStateMachine.BRCM_CHIP_4359.equals(chipName)) {
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    int PluggedType2 = intent.getIntExtra("plugged", 0);
                    if (PluggedType2 == 2 || PluggedType2 == 5) {
                        boolean unused3 = HwWifiStateMachine.this.mIsChargePluggedin = true;
                    } else {
                        boolean unused4 = HwWifiStateMachine.this.mIsChargePluggedin = false;
                        HwWifiStateMachine.this.mIsAllowedManualPwrBoost = 0;
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null) {
                        switch (AnonymousClass6.$SwitchMap$android$net$NetworkInfo$DetailedState[networkInfo.getDetailedState().ordinal()]) {
                            case 1:
                                HwWifiStateMachine.this.logd("setpmlock:CONNECTED");
                                boolean unused5 = HwWifiStateMachine.this.mWifiConnectState = true;
                                if (wifiInfo != null) {
                                    String unused6 = HwWifiStateMachine.this.mSsid = wifiInfo.getSSID();
                                }
                                HwWifiStateMachine.this.setLowPwrMode(HwWifiStateMachine.this.mWifiConnectState, HwWifiStateMachine.this.mSsid, isMobileAP, HwWifiStateMachine.this.mScreenState);
                                break;
                            case 2:
                                HwWifiStateMachine.this.logd("setpmlock:DISCONNECTED");
                                boolean unused7 = HwWifiStateMachine.this.mWifiConnectState = false;
                                HwWifiStateMachine.this.setLowPwrMode(HwWifiStateMachine.this.mWifiConnectState, null, isMobileAP, HwWifiStateMachine.this.mScreenState);
                                break;
                        }
                    }
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    HwWifiStateMachine hwWifiStateMachine2 = HwWifiStateMachine.this;
                    hwWifiStateMachine2.logd("setpmlock:action = " + action);
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    HwWifiStateMachine hwWifiStateMachine3 = HwWifiStateMachine.this;
                    hwWifiStateMachine3.logd("setpmlock:action = " + action);
                }
            }
        }
    };
    private ConnectivityManager mConnMgr = null;
    public boolean mCurrNetworkHistoryInserted = false;
    private int mCurrentConfigNetId = -1;
    private String mCurrentConfigurationKey = null;
    /* access modifiers changed from: private */
    public boolean mCurrentPwrBoostStat = false;
    private boolean mDelayWifiScoreBySelfCureOrSwitch = false;
    private Queue<IState> mDestStates = null;
    private final LruCache<String, DhcpResults> mDhcpResultCache = new LruCache<>(50);
    private int mFoulTimes = 0;
    private int mFreedTimes = 0;
    private HiLinkController mHiLinkController = null;
    /* access modifiers changed from: private */
    public HwInnerNetworkManagerImpl mHwInnerNetworkManagerImpl;
    private HwSoftApManager mHwSoftApManager;
    /* access modifiers changed from: private */
    public HwWifiCHRService mHwWifiCHRService;
    public int mIsAllowedManualPwrBoost = 0;
    /* access modifiers changed from: private */
    public boolean mIsChargePluggedin = false;
    /* access modifiers changed from: private */
    public boolean mIsFinishLinkDetect = false;
    /* access modifiers changed from: private */
    public boolean mIsScanCtrlPluggedin = false;
    private long mLastScanTimestamp = 0;
    private int mLastTxPktCnt = 0;
    private HashMap<String, Integer> mPidBlackList = new HashMap<>();
    private long mPidBlackListInteval = 0;
    private HashMap<String, Integer> mPidConnectedBlackList = new HashMap<>();
    private HashMap<Integer, Long> mPidLastScanSuccTimestamp = new HashMap<>();
    private HashMap<Integer, Long> mPidLastScanTimestamp = new HashMap<>();
    private HashMap<Integer, Integer> mPidWifiScanCount = new HashMap<>();
    /* access modifiers changed from: private */
    public int mPwrBoostOffcnt = 0;
    /* access modifiers changed from: private */
    public int mPwrBoostOncnt = 0;
    private AtomicBoolean mRenewDhcpSelfCuring = new AtomicBoolean(false);
    private int mScreenOffScanToken = 0;
    /* access modifiers changed from: private */
    public boolean mScreenState = true;
    public WifiConfiguration mSelectedConfig = null;
    private NetworkInfo.DetailedState mSelfCureNetworkLastState = NetworkInfo.DetailedState.IDLE;
    private int mSelfCureWifiConnectRetry = 0;
    private int mSelfCureWifiLastState = -1;
    private SoftApChannelXmlParse mSoftApChannelXmlParse;
    /* access modifiers changed from: private */
    public String mSsid = null;
    private long mTimeLastCtrlScanDuringObtainingIp = 0;
    private long mTimeOutScanControlForAssoc = 0;
    private long mTimeStampScanControlForAssoc = 0;
    public boolean mUserCloseWifiWhenSelfCure = false;
    private WifiSsid mWiFiProRoamingSSID = null;
    public boolean mWifiAlwaysOnBeforeCure = false;
    public boolean mWifiBackgroundConnected = false;
    /* access modifiers changed from: private */
    public boolean mWifiConnectState = false;
    private WifiDetectConfInfo mWifiDetectConfInfo = new WifiDetectConfInfo();
    private int mWifiDetectperiod = -1;
    private long mWifiEnabledTimeStamp = 0;
    private int mWifiSelfCureState = 0;
    private AtomicBoolean mWifiSelfCuring = new AtomicBoolean(false);
    private AtomicBoolean mWifiSoftSwitchRunning = new AtomicBoolean(false);
    public boolean mWifiSwitchOnGoing = false;
    /* access modifiers changed from: private */
    public HashMap<String, Boolean> mapApCapChr = new HashMap<>();
    private HwMSSArbitrager mssArbi = null;
    /* access modifiers changed from: private */
    public Context myContext;
    private final Object selectConfigLock = new Object();
    private boolean usingStaticIpConfig = false;
    private int wifiConnectedBackgroundReason = 0;
    private WifiEapUIManager wifiEapUIManager;

    /* renamed from: com.android.server.wifi.HwWifiStateMachine$6  reason: invalid class name */
    static /* synthetic */ class AnonymousClass6 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$DetailedState = new int[NetworkInfo.DetailedState.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private class FilterScanRunnable implements Runnable {
        List<ScanDetail> lstScanRet = null;

        public FilterScanRunnable(List<ScanDetail> lstScan) {
            this.lstScanRet = lstScan;
        }

        public void run() {
            String strCurBssid = HwWifiStateMachine.this.getCurrentBSSID();
            if (strCurBssid != null && !strCurBssid.isEmpty() && !HwWifiStateMachine.this.mapApCapChr.containsKey(strCurBssid) && this.lstScanRet != null && this.lstScanRet.size() != 0) {
                for (ScanDetail scanned : this.lstScanRet) {
                    if (scanned != null) {
                        String strBssid = scanned.getBSSIDString();
                        if (strBssid != null && !strBssid.isEmpty() && strCurBssid.equals(strBssid)) {
                            int stream1 = scanned.getNetworkDetail().getStream1();
                            int stream2 = scanned.getNetworkDetail().getStream2();
                            int stream3 = scanned.getNetworkDetail().getStream3();
                            int stream4 = scanned.getNetworkDetail().getStream4();
                            int txMcsSet = scanned.getNetworkDetail().getTxMcsSet();
                            int value = ((scanned.getNetworkDetail().getPrimaryFreq() / 1000) * 10) + Math.abs(stream1 + stream2 + stream3 + stream4);
                            HwWifiStateMachine.this.mHwWifiCHRService.updateWifiException(213, "{BSSID:\"" + strCurBssid + "\"," + HwWifiStateMachine.AP_CAP_KEY + ":" + value + "," + HwWifiStateMachine.TX_MCS_SET + ":" + txMcsSet + "}");
                            HwWifiStateMachine.this.mapApCapChr.put(strCurBssid, true);
                            if (HwWifiStateMachine.this.mapApCapChr.size() > 1000) {
                                HwWifiStateMachine.this.mapApCapChr.clear();
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    private class pwrBoostHandler extends Handler {
        private static final int PWR_BOOST_END_MSG = 1;
        private static final int PWR_BOOST_MANUAL_DISABLE = 0;
        private static final int PWR_BOOST_MANUAL_ENABLE = 1;
        private static final int PWR_BOOST_START_MSG = 0;

        pwrBoostHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (!HwWifiStateMachine.this.mWifiConnectState) {
                        HwWifiStateMachine.this.clearPwrBoostChrStatus();
                        return;
                    } else if (!HwWifiStateMachine.this.mIsChargePluggedin || !HwWifiStateMachine.this.mIsFinishLinkDetect) {
                        if (HwWifiStateMachine.this.mIsChargePluggedin && !HwWifiStateMachine.this.mIsFinishLinkDetect) {
                            HwWifiStateMachine.this.mIsAllowedManualPwrBoost = 1;
                            if (!HwWifiStateMachine.this.mCurrentPwrBoostStat) {
                                WifiInjector.getInstance().getWifiNative().setPwrBoost(1);
                                boolean unused = HwWifiStateMachine.this.mCurrentPwrBoostStat = true;
                                HwWifiStateMachine.this.linkMeasureAndStatic(HwWifiStateMachine.this.mCurrentPwrBoostStat);
                                int unused2 = HwWifiStateMachine.this.mPwrBoostOncnt = HwWifiStateMachine.this.mPwrBoostOncnt + 1;
                            } else if (HwWifiStateMachine.this.mCurrentPwrBoostStat) {
                                WifiInjector.getInstance().getWifiNative().setPwrBoost(0);
                                boolean unused3 = HwWifiStateMachine.this.mCurrentPwrBoostStat = false;
                                HwWifiStateMachine.this.linkMeasureAndStatic(HwWifiStateMachine.this.mCurrentPwrBoostStat);
                                int unused4 = HwWifiStateMachine.this.mPwrBoostOffcnt = HwWifiStateMachine.this.mPwrBoostOffcnt + 1;
                            }
                        }
                        if (HwWifiStateMachine.this.mPwrBoostOncnt >= 3 && HwWifiStateMachine.this.mPwrBoostOffcnt >= 3) {
                            boolean unused5 = HwWifiStateMachine.this.mIsFinishLinkDetect = true;
                            break;
                        } else {
                            boolean unused6 = HwWifiStateMachine.this.mIsFinishLinkDetect = false;
                            break;
                        }
                    } else {
                        HwWifiStateMachine.this.mIsAllowedManualPwrBoost = 0;
                        return;
                    }
                    break;
                case 1:
                    HwWifiStateMachine.this.clearPwrBoostChrStatus();
                    break;
            }
        }
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public HwWifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier, SarManager sarManager) {
        super(context, facade, looper, userManager, wifiInjector, backupManagerProxy, countryCode, wifiNative, wrongPasswordNotifier, sarManager);
        Context context2 = context;
        this.myContext = context2;
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        this.mBQEUid = 1000;
        this.mHwInnerNetworkManagerImpl = HwFrameworkFactory.getHwInnerNetworkManager();
        registerReceiverInWifiPro(context2);
        registerForWifiEvaluateChanges();
        this.mssArbi = HwMSSArbitrager.getInstance(context2);
        if (WifiRadioPowerController.isRadioPowerEnabled()) {
            WifiRadioPowerController.setInstance(context2, this, wifiStateMachineUtils.getWifiNative(this), HwFrameworkFactory.getHwInnerNetworkManager());
        }
        if (context2.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            registerForPasspointChanges();
        }
        this.mHiLinkController = new HiLinkController(context2, this);
        this.mActivityManager = (ActivityManager) context2.getSystemService("activity");
        this.mDestStates = new LinkedList();
        pwrBoostRegisterBcastReceiver();
        if (PreconfiguredNetworkManager.IS_R1) {
            this.wifiEapUIManager = new WifiEapUIManager(context2);
        }
        wifiStateMachineUtils.getWifiConfigManager(this).setSupportWapiType();
        this.mConnMgr = (ConnectivityManager) context2.getSystemService("connectivity");
        this.mSoftApChannelXmlParse = new SoftApChannelXmlParse(context2);
    }

    public String getWpaSuppConfig() {
        log("WiFIStateMachine  getWpaSuppConfig InterfaceName ");
        if (this.myContext.checkCallingPermission("com.huawei.permission.ACCESS_AP_INFORMATION") == 0) {
            return WifiInjector.getInstance().getWifiNative().getWpaSuppConfig();
        }
        log("getWpaSuppConfig(): permissin deny");
        return null;
    }

    /* access modifiers changed from: protected */
    public void enableAllNetworksByMode() {
        log("enableAllNetworks mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        if (wifiStateMachineUtils.getOperationalMode(this) != 100) {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
        }
    }

    /* access modifiers changed from: protected */
    public void handleNetworkDisconnect() {
        log("handle network disconnect mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            HwDisableLastNetwork();
        }
        log("handleNetworkDisconnect,resetWifiProManualConnect");
        resetWifiProManualConnect();
        HwWifiStateMachine.super.handleNetworkDisconnect();
    }

    /* access modifiers changed from: protected */
    public void loadAndEnableAllNetworksByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            log("supplicant connection mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
            WifiConfigStoreUtils.loadConfiguredNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            WifiConfigStoreUtils.disableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
        } else {
            WifiConfigStoreUtils.loadAndEnableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
        }
        if (isWifiSelfCuring()) {
            updateNetworkId();
        }
    }

    private void HwDisableLastNetwork() {
        log("HwDisableLastNetwork, currentState:" + getCurrentState() + ", mLastNetworkId:" + wifiStateMachineUtils.getLastNetworkId(this));
    }

    /* access modifiers changed from: protected */
    public boolean processScanModeSetMode(Message message, int mLastOperationMode) {
        if (message.arg1 != 100) {
            return false;
        }
        log("SCAN_ONLY_CONNECT_MODE, do not enable all networks here.");
        if (mLastOperationMode == 3) {
            wifiStateMachineUtils.setWifiState(this, 3);
            WifiConfigStoreUtils.loadConfiguredNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            AsyncChannel mWifiP2pChannel = wifiStateMachineUtils.getWifiP2pChannel(this);
            if (mWifiP2pChannel == null) {
                wifiStateMachineUtils.getAdditionalWifiServiceInterfaces(this);
                log("mWifiP2pChannel retry init");
            }
            if (mWifiP2pChannel != null) {
                mWifiP2pChannel.sendMessage(131203);
            } else {
                log("mWifiP2pChannel is null");
            }
        }
        wifiStateMachineUtils.setOperationalMode(this, 100);
        transitionTo(wifiStateMachineUtils.getDisconnectedState(this));
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processConnectModeSetMode(Message message) {
        if (wifiStateMachineUtils.getOperationalMode(this) != 100 || message.arg2 != 0) {
            return false;
        }
        log("CMD_ENABLE_NETWORK command is ignored.");
        wifiStateMachineUtils.replyToMessage((WifiStateMachine) this, message, message.what, 1);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processL2ConnectedSetMode(Message message) {
        wifiStateMachineUtils.setOperationalMode(this, message.arg1);
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            if (!wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
                sendMessage(131145);
            }
            disableAllNetworksExceptLastConnected();
            return true;
        } else if (wifiStateMachineUtils.getOperationalMode(this) != 1) {
            return false;
        } else {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processDisconnectedSetMode(Message message) {
        wifiStateMachineUtils.setOperationalMode(this, message.arg1);
        log("set operation mode mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            WifiConfigStoreUtils.disableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            return true;
        } else if (wifiStateMachineUtils.getOperationalMode(this) != 1) {
            return false;
        } else {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            wifiStateMachineUtils.getWifiNative(this).reconnect(wifiStateMachineUtils.getInterfaceName(this));
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void enterConnectedStateByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            log("wifi connected. disable other networks.");
            disableAllNetworksExceptLastConnected();
        }
    }

    /* access modifiers changed from: protected */
    public boolean enterDriverStartedStateByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) != 100) {
            return false;
        }
        log("SCAN_ONLY_CONNECT_MODE, disable all networks.");
        wifiStateMachineUtils.getWifiNative(this).disconnect(wifiStateMachineUtils.getInterfaceName(this));
        WifiConfigStoreUtils.disableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
        transitionTo(wifiStateMachineUtils.getDisconnectedState(this));
        return true;
    }

    private void disableAllNetworksExceptLastConnected() {
        log("disable all networks except last connected. currentState:" + getCurrentState() + ", mLastNetworkId:" + wifiStateMachineUtils.getLastNetworkId(this));
        for (WifiConfiguration network : WifiConfigStoreUtils.getConfiguredNetworks(wifiStateMachineUtils.getWifiConfigManager(this))) {
            if (network.networkId != wifiStateMachineUtils.getLastNetworkId(this)) {
                int i = network.status;
            }
        }
    }

    public void log(String message) {
        Log.d(TAG, message);
    }

    public boolean isScanAndManualConnectMode() {
        return wifiStateMachineUtils.getOperationalMode(this) == 100;
    }

    /* access modifiers changed from: protected */
    public boolean processConnectModeAutoConnectByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) != 100) {
            return false;
        }
        log("CMD_AUTO_CONNECT command is ignored..");
        return true;
    }

    /* access modifiers changed from: protected */
    public void recordAssociationRejectStatusCode(int statusCode) {
        Settings.System.putInt(this.myContext.getContentResolver(), ASSOCIATION_REJECT_STATUS_CODE, statusCode);
    }

    /* access modifiers changed from: protected */
    public void startScreenOffScan() {
        int configNetworksSize = wifiStateMachineUtils.getWifiConfigManager(this).getSavedNetworks().size();
        if (!wifiStateMachineUtils.getScreenOn(this) && configNetworksSize > 0) {
            logd("begin scan when screen off");
            int i = this.mScreenOffScanToken + 1;
            this.mScreenOffScanToken = i;
            sendMessageDelayed(obtainMessage(CMD_SCREEN_OFF_SCAN, i, 0), wifiStateMachineUtils.getSupplicantScanIntervalMs(this));
        }
    }

    /* access modifiers changed from: protected */
    public boolean processScreenOffScan(Message message) {
        if (131578 != message.what) {
            return false;
        }
        if (message.arg1 == this.mScreenOffScanToken) {
            startScreenOffScan();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void makeHwDefaultIPTable(DhcpResults dhcpResults) {
        synchronized (this.mDhcpResultCache) {
            String key = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
            if (key == null) {
                Log.w(TAG, "makeHwDefaultIPTable key is null!");
                return;
            }
            if (this.mDhcpResultCache.get(key) != null) {
                log("make default IP configuration map, remove old rec.");
                this.mDhcpResultCache.remove(key);
            }
            boolean isPublicESS = false;
            int count = 0;
            String ssid = "";
            String capabilities = "";
            List<ScanResult> scanList = new ArrayList<>();
            if (!WifiInjector.getInstance().getWifiStateMachineHandler().runWithScissors(new Runnable(scanList) {
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    this.f$1.addAll(HwWifiStateMachine.wifiStateMachineUtils.getScanRequestProxy(HwWifiStateMachine.this).getScanResults());
                }
            }, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT)) {
                Log.e(TAG, "Failed to post runnable to fetch scan results");
                return;
            }
            try {
                Iterator<ScanResult> it = scanList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ScanResult result = it.next();
                    if (key.equals(result.BSSID)) {
                        ssid = result.SSID;
                        capabilities = result.capabilities;
                        log("ESS: SSID:" + ssid + ",capabilities:" + capabilities);
                        break;
                    }
                }
                for (ScanResult result2 : scanList) {
                    if (ssid.equals(result2.SSID) && capabilities.equals(result2.capabilities)) {
                        count++;
                        if (count >= 3) {
                            isPublicESS = true;
                        }
                    }
                }
            } catch (Exception e) {
            }
            if (isPublicESS) {
                log("current network is public ESS, dont make default IP");
                return;
            }
            this.mDhcpResultCache.put(key, new DhcpResults(dhcpResults));
            log("make default IP configuration map, add rec for " + StringUtil.safeDisplayBssid(key));
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleHwDefaultIPConfiguration() {
        boolean isCurrentNetworkWEPSecurity = false;
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (!(config == null || config.wepKeys == null)) {
            int idx = config.wepTxKeyIndex;
            isCurrentNetworkWEPSecurity = idx >= 0 && idx < config.wepKeys.length && config.wepKeys[idx] != null;
        }
        if (isCurrentNetworkWEPSecurity) {
            log("current network is WEP, dot set default IP configuration");
            return false;
        }
        String key = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
        log("try to set default IP configuration for " + key);
        if (key == null) {
            Log.w(TAG, "handleHwDefaultIPConfiguration key is null!");
            return false;
        }
        DhcpResults dhcpResult = this.mDhcpResultCache.get(key);
        if (dhcpResult == null) {
            log("set default IP configuration failed for no rec found");
            return false;
        }
        DhcpResults dhcpResults = new DhcpResults(dhcpResult);
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        try {
            ifcg.setLinkAddress(dhcpResults.ipAddress);
            ifcg.setInterfaceUp();
            wifiStateMachineUtils.handleIPv4Success(this, dhcpResults);
            log("set default IP configuration succeeded");
            return true;
        } catch (Exception e) {
            loge("set default IP configuration failed for err: " + e);
            return false;
        }
    }

    public DhcpResults getCachedDhcpResultsForCurrentConfig() {
        boolean isCurrentNetworkWEPSecurity = false;
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (!(config == null || config.wepKeys == null)) {
            int idx = config.wepTxKeyIndex;
            isCurrentNetworkWEPSecurity = idx >= 0 && idx < config.wepKeys.length && config.wepKeys[idx] != null;
        }
        if (isCurrentNetworkWEPSecurity) {
            log("current network is WEP, dot set default IP configuration");
            return null;
        }
        String key = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
        int currRssi = wifiStateMachineUtils.getWifiInfo(this).getRssi();
        Log.d(TAG, "try to set default IP configuration currRssi = " + currRssi);
        if (key != null && currRssi >= -75) {
            return this.mDhcpResultCache.get(key);
        }
        Log.w(TAG, "getCachedDhcpResultsForCurrentConfig key is null!");
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean hasMeteredHintForWi(Inet4Address ip) {
        boolean isIphone = false;
        boolean isWindowsPhone = false;
        if (SystemProperties.get("dhcp.wlan0.vendorInfo", "").startsWith("hostname:") && ip != null && ip.toString().startsWith("/172.20.10.")) {
            Log.d(TAG, "isiphone = true");
            isIphone = true;
        }
        if (SystemProperties.get("dhcp.wlan0.domain", "").equals("mshome.net")) {
            Log.d(TAG, "isWindowsPhone = true");
            isWindowsPhone = true;
        }
        return isIphone || isWindowsPhone;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: int[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    public int[] syncGetApChannelListFor5G(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_CHANNEL_LIST_5G);
        int[] channels = null;
        if (resultMsg.obj != null) {
            channels = resultMsg.obj;
        }
        resultMsg.recycle();
        return channels;
    }

    public void setLocalMacAddressFromMacfile() {
        String ret = "02:00:00:00:00:00";
        String oriMacString = GetUDIDNative.getWifiMacAddress();
        if (oriMacString == null || oriMacString.length() != 12) {
            Log.e(TAG, "MacString: " + oriMacString + " from UDIDNative is unvalid. Use default MAC address");
        } else {
            StringBuilder macBuilder = new StringBuilder();
            for (int i = 0; i < oriMacString.length(); i += 2) {
                macBuilder.append(oriMacString.substring(i, i + 2));
                if (i + 2 < oriMacString.length() - 1) {
                    macBuilder.append(":");
                }
            }
            try {
                ret = MacAddress.fromString(macBuilder.toString()).toString();
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Formatted MacString is unvalid, message" + e.getMessage() + "Use default MAC address");
            }
        }
        Log.i(TAG, "setLocalMacAddress: " + ParcelUtil.safeDisplayMac(ret));
        wifiStateMachineUtils.getWifiInfo(this).setMacAddress(ret);
    }

    public void setVoWifiDetectMode(WifiDetectConfInfo info) {
        if (info != null && !this.mWifiDetectConfInfo.isEqual(info)) {
            this.mWifiDetectConfInfo = info;
            sendMessage(131772, info);
        }
    }

    /* access modifiers changed from: protected */
    public void processSetVoWifiDetectMode(Message msg) {
        WifiDetectConfInfo info = (WifiDetectConfInfo) msg.obj;
        Log.d(TAG, "set VoWifi Detect Mode " + info);
        boolean ret = false;
        if (info != null) {
            if (info.mWifiDetectMode == 1) {
                WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
                ret = wifiNative.voWifiDetectSet("LOW_THRESHOLD " + info.mThreshold);
            } else if (info.mWifiDetectMode == 2) {
                WifiNative wifiNative2 = WifiInjector.getInstance().getWifiNative();
                ret = wifiNative2.voWifiDetectSet("HIGH_THRESHOLD " + info.mThreshold);
            } else {
                WifiNative wifiNative3 = WifiInjector.getInstance().getWifiNative();
                ret = wifiNative3.voWifiDetectSet("MODE " + info.mWifiDetectMode);
            }
            if (ret) {
                WifiNative wifiNative4 = WifiInjector.getInstance().getWifiNative();
                if (wifiNative4.voWifiDetectSet("TRIGGER_COUNT " + info.mEnvalueCount)) {
                    WifiNative wifiNative5 = WifiInjector.getInstance().getWifiNative();
                    ret = wifiNative5.voWifiDetectSet("MODE " + info.mWifiDetectMode);
                }
            }
        }
        if (ret) {
            Log.d(TAG, "done set  VoWifi Detect Mode " + info);
            return;
        }
        Log.d(TAG, "Failed to set VoWifi Detect Mode " + info);
    }

    public WifiDetectConfInfo getVoWifiDetectMode() {
        return this.mWifiDetectConfInfo;
    }

    public void setVoWifiDetectPeriod(int period) {
        if (period != this.mWifiDetectperiod) {
            this.mWifiDetectperiod = period;
            sendMessage(131773, period);
        }
    }

    /* access modifiers changed from: protected */
    public void processSetVoWifiDetectPeriod(Message msg) {
        int period = msg.arg1;
        Log.d(TAG, "set VoWifiDetect Period " + period);
        WifiNative wifiNative = WifiInjector.getInstance().getWifiNative();
        if (wifiNative.voWifiDetectSet("PERIOD " + period)) {
            Log.d(TAG, "done set set VoWifiDetect  Period" + period);
            return;
        }
        Log.d(TAG, "set VoWifiDetect Period" + period);
    }

    public int getVoWifiDetectPeriod() {
        return this.mWifiDetectperiod;
    }

    public boolean syncGetSupportedVoWifiDetect(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(131774);
        boolean supportedVoWifiDetect = resultMsg.arg1 == 0;
        resultMsg.recycle();
        Log.e(TAG, "syncGetSupportedVoWifiDetect " + supportedVoWifiDetect);
        return supportedVoWifiDetect;
    }

    /* access modifiers changed from: protected */
    public void processIsSupportVoWifiDetect(Message msg) {
        wifiStateMachineUtils.replyToMessage((WifiStateMachine) this, msg, msg.what, WifiInjector.getInstance().getWifiNative().isSupportVoWifiDetect() ? 0 : -1);
    }

    /* access modifiers changed from: protected */
    public void processStatistics(int event) {
        if (event == 0) {
            this.lastConnectTime = System.currentTimeMillis();
            Flog.bdReport(this.myContext, 200);
        } else if (1 == event) {
            Flog.bdReport(this.myContext, HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS);
            if (-1 != this.lastConnectTime) {
                JSONObject eventMsg = new JSONObject();
                try {
                    eventMsg.put("duration", (System.currentTimeMillis() - this.lastConnectTime) / 1000);
                } catch (JSONException e) {
                    Log.e(TAG, "processStatistics put error." + e);
                }
                Flog.bdReport(this.myContext, HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP, eventMsg);
                this.lastConnectTime = -1;
            }
        }
    }

    public byte[] fetchWifiSignalInfoForVoWiFi() {
        String macStr;
        byte[] macBytes;
        ByteBuffer rawByteBuffer = ByteBuffer.allocate(52);
        rawByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int linkSpeed = -1;
        int frequency = -1;
        int rssi = -1;
        WifiNative.SignalPollResult signalInfo = WifiInjector.getInstance().getWifiNative().signalPoll(wifiStateMachineUtils.getInterfaceName(this));
        if (signalInfo != null) {
            rssi = signalInfo.currentRssi;
            linkSpeed = signalInfo.txBitrate;
            frequency = signalInfo.associationFrequency;
        }
        int frequency2 = frequency;
        int linkSpeed2 = linkSpeed;
        RssiPacketCountInfo info = new RssiPacketCountInfo();
        WifiNative.TxPacketCounters txPacketCounters = WifiInjector.getInstance().getWifiNative().getTxPacketCounters(wifiStateMachineUtils.getInterfaceName(this));
        long nativeTxGood = 0;
        long nativeTxBad = 0;
        if (txPacketCounters != null) {
            info.txgood = txPacketCounters.txSucceeded;
            nativeTxGood = (long) txPacketCounters.txSucceeded;
            info.txbad = txPacketCounters.txFailed;
            nativeTxBad = (long) txPacketCounters.txFailed;
        }
        rawByteBuffer.putInt(rssi);
        rawByteBuffer.putInt(0);
        WifiNative.SignalPollResult signalPollResult = signalInfo;
        int bler = (int) ((((double) info.txbad) / ((double) (info.txgood + info.txbad))) * 100.0d);
        rawByteBuffer.putInt(bler);
        int dpktcnt = info.txgood - this.mLastTxPktCnt;
        this.mLastTxPktCnt = info.txgood;
        rawByteBuffer.putInt(dpktcnt);
        rawByteBuffer.putInt(convertToAccessType(linkSpeed2, frequency2));
        rawByteBuffer.putInt(0);
        rawByteBuffer.putLong(nativeTxGood);
        rawByteBuffer.putLong(nativeTxBad);
        String bssid = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
        if (!TextUtils.isEmpty(bssid)) {
            Object obj = "ffffffffffff";
            macStr = bssid.replace(":", "");
        } else {
            macStr = "ffffffffffff";
        }
        byte[] macBytes2 = new byte[16];
        try {
            macBytes = macStr.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            macBytes = macBytes2;
        }
        rawByteBuffer.put(macBytes);
        byte[] bArr = macBytes;
        int i = bler;
        StringBuilder sb = new StringBuilder();
        RssiPacketCountInfo rssiPacketCountInfo = info;
        sb.append("rssi=");
        sb.append(rssi);
        sb.append(",nativeTxBad=");
        sb.append(nativeTxBad);
        sb.append(", nativeTxGood=");
        sb.append(nativeTxGood);
        sb.append(", dpktcnt=");
        sb.append(dpktcnt);
        sb.append(", linkSpeed=");
        sb.append(linkSpeed2);
        sb.append(", frequency=");
        sb.append(frequency2);
        sb.append(", noise=");
        sb.append(0);
        int i2 = linkSpeed2;
        sb.append(", mac=");
        int i3 = rssi;
        sb.append(macStr.length() >= 6 ? macStr.substring(0, 6) : "ffffff");
        Log.d(TAG, sb.toString());
        return rawByteBuffer.array();
    }

    private static int convertToAccessType(int linkSpeed, int frequency) {
        return 0;
    }

    private void closeInputStream(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
            }
        }
    }

    private void registerReceiverInWifiPro(Context context) {
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiConfiguration newConfig = (WifiConfiguration) intent.getParcelableExtra("new_wifi_config");
                WifiConfiguration currentConfig = HwWifiStateMachine.this.getCurrentWifiConfiguration();
                WifiConfigManager wifiConfigManager = HwWifiStateMachine.wifiStateMachineUtils.getWifiConfigManager(HwWifiStateMachine.this);
                if (newConfig != null && currentConfig != null && wifiConfigManager != null) {
                    HwWifiStateMachine hwWifiStateMachine = HwWifiStateMachine.this;
                    hwWifiStateMachine.log("sync update network history, internetHistory = " + newConfig.internetHistory);
                    currentConfig.noInternetAccess = newConfig.noInternetAccess;
                    currentConfig.validatedInternetAccess = newConfig.validatedInternetAccess;
                    currentConfig.numNoInternetAccessReports = newConfig.numNoInternetAccessReports;
                    currentConfig.portalNetwork = newConfig.portalNetwork;
                    currentConfig.portalCheckStatus = newConfig.portalCheckStatus;
                    currentConfig.internetHistory = newConfig.internetHistory;
                    currentConfig.lastHasInternetTimestamp = newConfig.lastHasInternetTimestamp;
                    wifiConfigManager.updateInternetInfoByWifiPro(currentConfig);
                    wifiConfigManager.saveToStore(true);
                }
            }
        }, new IntentFilter("com.huawei.wifipro.ACTION_UPDATE_CONFIG_HISTORY"), "com.huawei.wifipro.permission.RECV.NETWORK_CHECKER", null);
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int switchType = intent.getIntExtra(WifiHandover.WIFI_HANDOVER_NETWORK_SWITCHTYPE, 1);
                WifiConfiguration changeConfig = (WifiConfiguration) intent.getParcelableExtra(WifiHandover.WIFI_HANDOVER_NETWORK_WIFICONFIG);
                HwWifiStateMachine hwWifiStateMachine = HwWifiStateMachine.this;
                hwWifiStateMachine.log("ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER, switchType = " + switchType);
                if (!HwWifiStateMachine.this.mWifiSwitchOnGoing && changeConfig != null) {
                    if (switchType == 1) {
                        HwWifiStateMachine.this.requestWifiSoftSwitch();
                        HwWifiStateMachine.this.startConnectToUserSelectNetwork(changeConfig.networkId, Binder.getCallingUid(), changeConfig.BSSID);
                    } else {
                        ScanResult roamScanResult = new ScanResult();
                        roamScanResult.BSSID = changeConfig.BSSID;
                        HwWifiStateMachine.this.startRoamToNetwork(changeConfig.networkId, roamScanResult);
                        HwWifiStateMachine.this.log("roamScanResult, call startRoamToNetwork");
                    }
                    HwWifiStateMachine.this.mWifiSwitchOnGoing = true;
                }
            }
        }, new IntentFilter(WifiHandover.ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER), WifiHandover.WIFI_HANDOVER_RECV_PERMISSION, null);
    }

    public void startWifi2WifiRequest() {
        this.mWifiSwitchOnGoing = true;
    }

    public boolean isWifiProEnabled() {
        return WifiProCommonUtils.isWifiProSwitchOn(this.myContext);
    }

    public int resetScoreByInetAccess(int score) {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        if (!isWifiProEnabled() || networkInfo == null || networkInfo.getDetailedState() != NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            return score;
        }
        return 0;
    }

    public void getConfiguredNetworks(Message message) {
        wifiStateMachineUtils.replyToMessage((WifiStateMachine) this, message, message.what, (Object) wifiStateMachineUtils.getWifiConfigManager(this).getSavedNetworks());
    }

    public void saveConnectingNetwork(WifiConfiguration config, int netId, boolean autoJoin) {
        synchronized (this.selectConfigLock) {
            if (config == null && netId != -1) {
                config = wifiStateMachineUtils.getWifiConfigManager(this).getConfiguredNetwork(netId);
            }
            this.mSelectedConfig = config;
            if (HwAutoConnectManager.getInstance() != null) {
                HwAutoConnectManager.getInstance().releaseBlackListBssid(config, autoJoin);
            }
        }
    }

    public void reportPortalNetworkStatus() {
        unwantedNetwork(3);
    }

    public boolean ignoreEnterConnectedState() {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        if (!isWifiProEnabled() || networkInfo == null || networkInfo.getDetailedState() != NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            return false;
        }
        log("L2ConnectedState, case CMD_IP_CONFIGURATION_SUCCESSFUL, ignore to enter CONNECTED State");
        return true;
    }

    public void wifiNetworkExplicitlyUnselected() {
        WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
        HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
        if (wifiInfo != null) {
            wifiInfo.score = 40;
        }
        if (networkAgent != null) {
            networkAgent.sendNetworkScore(40);
        }
    }

    public void wifiNetworkExplicitlySelected() {
        WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
        HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
        if (wifiInfo != null) {
            wifiInfo.score = 60;
        }
        if (networkAgent != null) {
            networkAgent.sendNetworkScore(60);
        }
    }

    public void handleConnectedInWifiPro() {
        WifiConfiguration config;
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
        handleWiFiConnectedByScanGenie(wifiConfigManager);
        if (this.mWifiSwitchOnGoing) {
            String bssid = null;
            String ssid = null;
            String configKey = null;
            synchronized (this.selectConfigLock) {
                if (this.mSelectedConfig != null) {
                    config = this.mSelectedConfig;
                } else {
                    config = wifiConfigManager.getConfiguredNetwork(wifiStateMachineUtils.getLastNetworkId(this));
                }
            }
            if (config != null) {
                bssid = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
                ssid = config.SSID;
                configKey = config.configKey();
            }
            sendWifiHandoverCompletedBroadcast(0, bssid, ssid, configKey);
        }
        int lastNetworkId = wifiStateMachineUtils.getLastNetworkId(this);
        WifiConfiguration connectedConfig = wifiConfigManager.getConfiguredNetwork(lastNetworkId);
        if (connectedConfig != null) {
            if (connectedConfig.portalNetwork) {
                Bundle data = new Bundle();
                data.putBoolean("protalflag", connectedConfig.portalNetwork);
                this.mHwWifiCHRService.uploadDFTEvent(3, data);
            }
            for (WifiConfiguration config2 : wifiConfigManager.getSavedNetworks()) {
                if (config2.getNetworkSelectionStatus().getConnectChoice() != null) {
                    wifiConfigManager.clearNetworkConnectChoice(config2.networkId);
                }
            }
            if (connectedConfig.portalCheckStatus == 1) {
                log("handleConnectedInWifiPro reset HAS_INTERNET to INTERNET_UNKNOWN!!");
                connectedConfig.portalCheckStatus = 0;
            }
            if (connectedConfig.internetRecoveryStatus == 5) {
                log("handleConnectedInWifiPro reset RECOVERED to INTERNET_UNKNOWN!!");
                connectedConfig.internetRecoveryStatus = 3;
            }
            wifiConfigManager.updateInternetInfoByWifiPro(connectedConfig);
            if (connectedConfig != null && isWifiProEvaluatingAP() && !this.usingStaticIpConfig && connectedConfig.SSID != null && !connectedConfig.SSID.equals("<unknown ssid>")) {
                String strDhcpResults = WifiProCommonUtils.dhcpResults2String(wifiStateMachineUtils.getDhcpResults(this), WifiProCommonUtils.getCurrentCellId());
                if (!(strDhcpResults == null || connectedConfig.configKey() == null)) {
                    log("handleConnectedInWifiPro, lastDhcpResults = " + strDhcpResults + ", ssid = " + connectedConfig.SSID);
                    this.lastDhcps.put(connectedConfig.configKey(), strDhcpResults);
                }
            }
        }
        if (!isWifiProEvaluatingAP()) {
            try {
                this.mHwInnerNetworkManagerImpl.setWifiproFirewallEnable(false);
            } catch (Exception e) {
                log("wifi connected, Disable WifiproFirewall again");
            }
        }
        synchronized (this.selectConfigLock) {
            this.mSelectedConfig = null;
        }
        this.usingStaticIpConfig = false;
        resetSelfCureCandidateLostCnt();
        wifiConfigManager.resetNetworkConnFailedInfo(lastNetworkId);
        wifiConfigManager.updateRssiDiscNonLocally(lastNetworkId, false, 0, 0);
        if (this.mWifiSoftSwitchRunning.get()) {
            log("wifi connected, reset mWifiSoftSwitchRunning and SCE state");
            this.mWifiSoftSwitchRunning.set(false);
            WifiProCommonUtils.setWifiSelfCureStatus(0);
        }
        removeMessages(131897);
    }

    public void handleDisconnectedInWifiPro() {
        WifiScanGenieController.createWifiScanGenieControllerImpl(this.myContext).handleWiFiDisconnected();
        this.mCurrNetworkHistoryInserted = false;
        if (this.wifiConnectedBackgroundReason == 2 || this.wifiConnectedBackgroundReason == 3) {
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, false);
        }
        this.wifiConnectedBackgroundReason = 0;
        if (HwAutoConnectManager.getInstance() != null) {
            HwAutoConnectManager.getInstance().notifyNetworkDisconnected();
        }
        synchronized (this.selectConfigLock) {
            this.mSelectedConfig = null;
        }
        this.usingStaticIpConfig = false;
        this.mRenewDhcpSelfCuring.set(false);
        this.mDelayWifiScoreBySelfCureOrSwitch = false;
    }

    public void handleUnwantedNetworkInWifiPro(WifiConfiguration config, int unwantedType) {
        if (config != null) {
            boolean updated = false;
            if (unwantedType == wifiStateMachineUtils.getUnwantedValidationFailed(this)) {
                if (this.mCurrNetworkHistoryInserted) {
                    log("don't update history for UNWANTED_VALIDATION_FAILED");
                    return;
                }
                config.noInternetAccess = true;
                config.validatedInternetAccess = false;
                config.portalNetwork = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102);
                if (!this.mCurrNetworkHistoryInserted) {
                    config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 0);
                    this.mCurrNetworkHistoryInserted = true;
                }
                updated = true;
            } else if (unwantedType == 3) {
                if (this.mCurrNetworkHistoryInserted) {
                    log("don't update history for NETWORK_STATUS_UNWANTED_PORTAL");
                    return;
                }
                config.portalNetwork = true;
                config.noInternetAccess = false;
                config.validatedInternetAccess = true;
                if (!this.mCurrNetworkHistoryInserted) {
                    config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 2);
                    this.mCurrNetworkHistoryInserted = true;
                    Bundle data = new Bundle();
                    data.putBoolean("protalflag", config.portalNetwork);
                    this.mHwWifiCHRService.uploadDFTEvent(3, data);
                }
                updated = true;
            }
            if (updated) {
                WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                wifiConfigManager.updateInternetInfoByWifiPro(config);
                wifiConfigManager.saveToStore(false);
            }
            this.mDelayWifiScoreBySelfCureOrSwitch = false;
        }
    }

    public void handleValidNetworkInWifiPro(WifiConfiguration config) {
        if (config != null) {
            String strDhcpResults = WifiProCommonUtils.dhcpResults2String(wifiStateMachineUtils.getDhcpResults(this), -1);
            if (strDhcpResults != null) {
                config.lastDhcpResults = strDhcpResults;
                if (!isWifiProEvaluatingAP()) {
                    HwSelfCureEngine.getInstance(this.myContext, this).notifyDhcpResultsInternetOk(strDhcpResults);
                }
            }
            if (!config.portalNetwork || !this.mCurrNetworkHistoryInserted) {
                config.noInternetAccess = false;
                if (!this.mCurrNetworkHistoryInserted) {
                    config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 1);
                    this.mCurrNetworkHistoryInserted = true;
                } else {
                    config.internetHistory = WifiProCommonUtils.updateWifiConfigHistory(config.internetHistory, 1);
                }
                config.lastHasInternetTimestamp = System.currentTimeMillis();
                WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                wifiConfigManager.updateInternetInfoByWifiPro(config);
                wifiConfigManager.saveToStore(false);
                this.mDelayWifiScoreBySelfCureOrSwitch = false;
            }
        }
    }

    public void startRoamToNetwork(int networkId, ScanResult scanResult) {
        HwWifiStateMachine.super.startRoamToNetwork(networkId, scanResult);
        this.mCurrNetworkHistoryInserted = false;
    }

    public void handleConnectFailedInWifiPro(int netId, int disableReason) {
        if (this.mWifiSwitchOnGoing && disableReason >= 2 && disableReason <= 4) {
            log("handleConnectFailedInWifiPro, netId = " + netId + ", disableReason = " + disableReason);
            String failedBssid = null;
            String failedSsid = null;
            int status = -6;
            if (disableReason != 2) {
                status = -7;
            }
            synchronized (this.selectConfigLock) {
                if (this.mSelectedConfig != null) {
                    failedBssid = this.mSelectedConfig.BSSID;
                    failedSsid = this.mSelectedConfig.SSID;
                }
            }
            sendWifiHandoverCompletedBroadcast(status, failedBssid, failedSsid, null);
        }
    }

    private void sendWifiHandoverCompletedBroadcast(int statusCode, String bssid, String ssid, String configKey) {
        if (this.mWifiSwitchOnGoing) {
            this.mWifiSwitchOnGoing = false;
            synchronized (this.selectConfigLock) {
                this.mSelectedConfig = null;
            }
            Intent intent = new Intent();
            if (WifiProStateMachine.getWifiProStateMachineImpl().getNetwoksHandoverType() == 1) {
                intent.setAction(WifiHandover.ACTION_RESPONSE_WIFI_2_WIFI);
            } else {
                intent.setAction(WifiHandover.ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER);
            }
            intent.putExtra(WifiHandover.WIFI_HANDOVER_COMPLETED_STATUS, statusCode);
            intent.putExtra(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID, bssid);
            intent.putExtra(WifiHandover.WIFI_HANDOVER_NETWORK_SSID, ssid);
            intent.putExtra(WifiHandover.WIFI_HANDOVER_NETWORK_CONFIGKYE, configKey);
            this.myContext.sendBroadcastAsUser(intent, UserHandle.ALL, WifiHandover.WIFI_HANDOVER_RECV_PERMISSION);
        }
    }

    public void updateWifiproWifiConfiguration(Message message) {
        if (message != null) {
            WifiConfiguration config = (WifiConfiguration) message.obj;
            boolean z = true;
            if (message.arg1 != 1) {
                z = false;
            }
            boolean uiOnly = z;
            if (config != null && config.networkId != -1) {
                WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                wifiConfigManager.updateWifiConfigByWifiPro(config, uiOnly);
                if (config.configKey() != null && config.wifiProNoInternetAccess) {
                    log("updateWifiproWifiConfiguration, noInternetReason = " + config.wifiProNoInternetReason + ", ssid = " + config.SSID);
                    this.lastDhcps.remove(config.configKey());
                }
                wifiConfigManager.saveToStore(false);
            }
        }
    }

    public void notifyWifiConnFailedInfo(int netId, String bssid, int rssi, int reason, WifiConnectivityManager wcm) {
        if (netId == -1) {
            return;
        }
        if (reason == 3 || reason == 2 || reason == 4) {
            log("updateNetworkConnFailedInfo, netId = " + netId + ", rssi = " + rssi + ", reason = " + reason);
            WifiConfigManager configManager = wifiStateMachineUtils.getWifiConfigManager(this);
            WifiConfiguration selectedConfig = configManager.getConfiguredNetwork(netId);
            if (reason == 4) {
                configManager.updateNetworkConnFailedInfo(netId, rssi, reason);
            } else {
                if (selectedConfig != null) {
                    ScanResult scanResult = selectedConfig.getNetworkSelectionStatus().getCandidate();
                    if (scanResult != null) {
                        rssi = scanResult.level;
                    }
                }
                configManager.updateNetworkConnFailedInfo(netId, rssi, reason);
            }
            if (HwAutoConnectManager.getInstance() != null) {
                HwAutoConnectManager.getInstance().notifyWifiConnFailedInfo(selectedConfig, bssid, rssi, reason, wcm);
            }
        }
    }

    public void notifyNetworkUserConnect(boolean isUserConnect) {
        WifiProStateMachine mWifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        log("notifyNetworkUserConnect : " + isUserConnect);
        if (mWifiProStateMachine != null) {
            mWifiProStateMachine.notifyNetworkUserConnect(isUserConnect);
        }
    }

    public void notifyApkChangeWifiStatus(boolean enable, String packageName) {
        log("notifyApkChangeWifiStatus enable= " + enable + ",packageName =" + packageName);
        WifiProStateMachine mWifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        if (mWifiProStateMachine != null) {
            if (enable) {
                mWifiProStateMachine.notifyApkChangeWifiStatus(true, packageName);
            } else if (packageName.equals("com.android.systemui")) {
                mWifiProStateMachine.notifyApkChangeWifiStatus(false, packageName);
            }
        }
    }

    public void handleDisconnectedReason(WifiConfiguration config, int rssi, int local, int reason) {
        if (config != null && local == 0 && rssi != -127) {
            log("handleDisconnectedReason, rssi = " + rssi + ", reason = " + reason + ", ssid = " + config.SSID);
            if (reason == 0 || reason == 3 || reason == 8) {
                wifiStateMachineUtils.getWifiConfigManager(this).updateRssiDiscNonLocally(config.networkId, true, rssi, System.currentTimeMillis());
            }
        }
    }

    public void setWiFiProScanResultList(List<ScanResult> list) {
        if (isWifiProEnabled()) {
            HwIntelligenceWiFiManager.setWiFiProScanResultList(list);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00bd, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00e2, code lost:
        return r2;
     */
    public boolean isWifiProEvaluatingAP() {
        boolean z = true;
        if (this.wifiConnectedBackgroundReason == 2) {
            log("isWifiProEvaluatingAP, WIFI_BACKGROUND_PORTAL_CHECKING");
            return true;
        }
        if (isWifiProEnabled()) {
            WifiConfiguration connectedConfig = wifiStateMachineUtils.getWifiConfigManager(this).getConfiguredNetwork(wifiStateMachineUtils.getLastNetworkId(this));
            StringBuilder sb = new StringBuilder();
            sb.append("isWifiProEvaluatingAP, connectedConfig = ");
            sb.append(connectedConfig != null ? connectedConfig.SSID : null);
            log(sb.toString());
            if (this.wifiConnectedBackgroundReason == 2 || this.wifiConnectedBackgroundReason == 3) {
                log("isWifiProEvaluatingAP, wifi connected at background matched, reason = " + this.wifiConnectedBackgroundReason);
                return true;
            } else if (connectedConfig != null) {
                log("isWifiProEvaluatingAP, isTempCreated = " + connectedConfig.isTempCreated + ", evaluating = " + WifiProStateMachine.isWifiEvaluating() + ", wifiConnectedBackgroundReason = " + this.wifiConnectedBackgroundReason);
                if (WifiProStateMachine.isWifiEvaluating() && connectedConfig.isTempCreated) {
                    this.wifiConnectedBackgroundReason = 1;
                    return true;
                }
            } else {
                synchronized (this.selectConfigLock) {
                    if (this.mSelectedConfig != null) {
                        log("isWifiProEvaluatingAP = " + WifiProStateMachine.isWifiEvaluating() + ", mSelectedConfig isTempCreated = " + this.mSelectedConfig.isTempCreated);
                        if (!WifiProStateMachine.isWifiEvaluating() || !this.mSelectedConfig.isTempCreated) {
                            z = false;
                        }
                    } else {
                        log("==connectedConfig&mSelectedConfig are null, backgroundReason = " + this.wifiConnectedBackgroundReason);
                        if (!WifiProStateMachine.isWifiEvaluating()) {
                            if (this.wifiConnectedBackgroundReason < 1) {
                                z = false;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void updateScanDetail(ScanDetail scanDetail) {
        ScanResult sc = scanDetail.getScanResult();
        if (sc != null) {
            if (isWifiProEnabled()) {
                WifiProConfigStore.updateScanDetailByWifiPro(sc);
            } else {
                sc.internetAccessType = 0;
                sc.networkQosLevel = 0;
                sc.networkQosScore = 0;
            }
        }
    }

    public void updateScanDetailByWifiPro(List<ScanDetail> scanResults) {
        if (scanResults != null) {
            for (ScanDetail scanDetail : scanResults) {
                updateScanDetail(scanDetail);
            }
        }
    }

    public void tryUseStaticIpForFastConnecting(int lastNid) {
        if (isWifiProEnabled() && lastNid != -1 && isWifiProEvaluatingAP()) {
            synchronized (this.selectConfigLock) {
                if (!(this.mSelectedConfig == null || this.mSelectedConfig.configKey() == null)) {
                    WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                    this.mSelectedConfig.lastDhcpResults = this.lastDhcps.get(this.mSelectedConfig.configKey());
                    log("tryUseStaticIpForFastConnecting, lastDhcpResults = " + this.mSelectedConfig.lastDhcpResults);
                    if (this.mSelectedConfig.lastDhcpResults != null && this.mSelectedConfig.lastDhcpResults.length() > 0 && this.mSelectedConfig.getStaticIpConfiguration() == null && wifiConfigManager.tryUseStaticIpForFastConnecting(lastNid)) {
                        this.usingStaticIpConfig = true;
                    }
                }
            }
        }
    }

    public void updateNetworkConcurrently() {
        NetworkInfo.DetailedState state = NetworkInfo.DetailedState.CONNECTED;
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
        int lastNetworkId = wifiStateMachineUtils.getLastNetworkId(this);
        HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
        if (!(networkInfo.getExtraInfo() == null || wifiInfo.getSSID() == null || wifiInfo.getSSID().equals("<unknown ssid>"))) {
            networkInfo.setExtraInfo(wifiInfo.getSSID());
        }
        if (state != networkInfo.getDetailedState()) {
            networkInfo.setDetailedState(state, null, wifiInfo.getSSID());
            if (networkAgent != null) {
                networkAgent.updateNetworkConcurrently(networkInfo);
            }
        }
        log("updateNetworkConcurrently, lastNetworkId = " + lastNetworkId);
        if (this.wifiConnectedBackgroundReason == 2 || this.wifiConnectedBackgroundReason == 3) {
            HwSelfCureEngine.getInstance(this.myContext, this).notifyWifiConnectedBackground();
            WifiScanGenieController.createWifiScanGenieControllerImpl(this.myContext).notifyWifiConnectedBackground();
        }
    }

    public void triggerRoamingNetworkMonitor(boolean autoRoaming) {
        if (autoRoaming) {
            NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
            HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
            if (networkAgent != null) {
                networkAgent.triggerRoamingNetworkMonitor(networkInfo);
            }
        }
    }

    public boolean isDualbandScanning() {
        HwDualBandManager mHwDualBandManager = HwDualBandManager.getInstance();
        if (mHwDualBandManager != null) {
            return mHwDualBandManager.isDualbandScanning();
        }
        return false;
    }

    public void triggerInvalidlinkNetworkMonitor() {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
        if (networkAgent != null) {
            networkAgent.triggerInvalidlinkNetworkMonitor(networkInfo);
        }
    }

    public void notifyWifiConnectedBackgroundReady() {
        if (this.wifiConnectedBackgroundReason == 1) {
            log("notifyWifiConnectedBackgroundReady, ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY sent");
            Intent intent = new Intent(WifiproUtils.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY);
            intent.setFlags(67108864);
            this.myContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } else if (this.wifiConnectedBackgroundReason == 2) {
            log("notifyWifiConnectedBackgroundReady, WIFI_BACKGROUND_PORTAL_CHECKING sent");
            Intent intent2 = new Intent(WifiproUtils.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND);
            intent2.setFlags(67108864);
            this.myContext.sendBroadcastAsUser(intent2, UserHandle.ALL);
        } else if (this.wifiConnectedBackgroundReason == 3) {
            log("notifyWifiConnectedBackgroundReady, ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND sent");
            Intent intent3 = new Intent(WifiproUtils.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND);
            intent3.setFlags(67108864);
            this.myContext.sendBroadcastAsUser(intent3, UserHandle.ALL);
        }
    }

    public void setWifiBackgroundReason(int status) {
        if (status == 0) {
            this.wifiConnectedBackgroundReason = 2;
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, true);
        } else if (status == 1) {
            this.wifiConnectedBackgroundReason = 0;
        } else if (status == 3) {
            this.wifiConnectedBackgroundReason = 3;
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, true);
        } else if (status == 5) {
            this.wifiConnectedBackgroundReason = 0;
        } else if (status == 6) {
            this.wifiConnectedBackgroundReason = 0;
        }
    }

    public void updateWifiBackgroudStatus(int msgType) {
        if (msgType == 2) {
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, false);
            this.wifiConnectedBackgroundReason = 0;
        }
    }

    public boolean isWiFiProSwitchOnGoing() {
        log("isWiFiProSwitchOnGoing,mWifiSwitchOnGoing = " + this.mWifiSwitchOnGoing);
        return this.mWifiSwitchOnGoing;
    }

    public void resetWifiproEvaluateConfig(WifiInfo mWifiInfo, int netId) {
        if (isWifiProEvaluatingAP() && mWifiInfo != null && mWifiInfo.getNetworkId() == netId) {
            int lastNetworkId = wifiStateMachineUtils.getLastNetworkId(this);
            WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
            WifiConfiguration connectedConfig = wifiConfigManager.getConfiguredNetwork(lastNetworkId);
            synchronized (this.selectConfigLock) {
                if (connectedConfig == null) {
                    try {
                        connectedConfig = this.mSelectedConfig;
                    } catch (Throwable th) {
                        while (true) {
                            throw th;
                        }
                    }
                }
            }
            if (connectedConfig != null) {
                connectedConfig.isTempCreated = false;
                log("resetWifiproEvaluateConfig,ssid = " + connectedConfig.SSID);
                wifiConfigManager.updateWifiConfigByWifiPro(connectedConfig, true);
            }
        }
    }

    public boolean ignoreNetworkStateChange(NetworkInfo networkInfo) {
        if (networkInfo == null) {
            return false;
        }
        boolean isEvaluatingAP = isWifiProEvaluatingAP();
        if ((!isEvaluatingAP || !(networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTING || networkInfo.getDetailedState() == NetworkInfo.DetailedState.SCANNING || networkInfo.getDetailedState() == NetworkInfo.DetailedState.AUTHENTICATING || networkInfo.getDetailedState() == NetworkInfo.DetailedState.OBTAINING_IPADDR || networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED)) && !selfCureIgnoreNetworkStateChange(networkInfo) && !softSwitchIgnoreNetworkStateChanged(networkInfo)) {
            if (isEvaluatingAP && networkInfo.getState() == NetworkInfo.State.DISCONNECTED && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                WifiProStateMachine mWifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
                if (mWifiProStateMachine != null) {
                    Log.d("WiFi_PRO", "notifyWifiDisconnected, DetailedState = " + networkInfo.getDetailedState());
                    Intent intent = new Intent("android.net.wifi.STATE_CHANGE");
                    intent.putExtra("networkInfo", new NetworkInfo(networkInfo));
                    mWifiProStateMachine.notifyWifiDisconnected(intent);
                }
            }
            return false;
        }
        Log.d("WiFi_PRO", "ignoreNetworkStateChange, DetailedState = " + networkInfo.getDetailedState());
        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTING && ((this.mWifiSoftSwitchRunning.get() || isWifiSelfCureByReset()) && !isMobileNetworkActive())) {
            this.mDelayWifiScoreBySelfCureOrSwitch = true;
        }
        return true;
    }

    public boolean selfCureIgnoreNetworkStateChange(NetworkInfo networkInfo) {
        if ((!isWifiSelfCuring() || !this.mWifiBackgroundConnected) && ((!isWifiSelfCuring() || this.mWifiBackgroundConnected || networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) && (!isRenewDhcpSelfCuring() || networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED))) {
            return false;
        }
        Log.d("HwSelfCureEngine", "selfCureIgnoreNetworkStateChange, detailedState = " + networkInfo.getDetailedState());
        return true;
    }

    private boolean selfCureIgnoreSuppStateChange(SupplicantState state) {
        if (!isWifiSelfCuring() && !isRenewDhcpSelfCuring() && !this.mWifiSoftSwitchRunning.get()) {
            return false;
        }
        if (state == SupplicantState.ASSOCIATING && ((this.mWifiSoftSwitchRunning.get() || isWifiSelfCureByReset()) && !isMobileNetworkActive())) {
            this.mDelayWifiScoreBySelfCureOrSwitch = true;
        }
        return true;
    }

    private boolean isWifiSelfCureByReset() {
        return 102 == WifiProCommonUtils.getSelfCuringState();
    }

    private boolean isMobileNetworkActive() {
        if (this.mConnMgr == null) {
            this.mConnMgr = (ConnectivityManager) this.myContext.getSystemService("connectivity");
        }
        boolean z = false;
        if (this.mConnMgr == null) {
            return false;
        }
        NetworkInfo activeNetInfo = this.mConnMgr.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == 0) {
            z = true;
        }
        return z;
    }

    public boolean softSwitchIgnoreNetworkStateChanged(NetworkInfo networkInfo) {
        if (!this.mWifiSoftSwitchRunning.get() || networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            return false;
        }
        Log.d("WIFIPRO", "softSwitchIgnoreNetworkStateChanged, detailedState = " + networkInfo.getDetailedState());
        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
            HwSelfCureEngine.getInstance(this.myContext, this).notifyWifiDisconnected();
            HwWifiConnectivityMonitor.getInstance().notifyWifiDisconnected();
            HwAutoConnectManager.getInstance().notifyNetworkDisconnected();
            HwMSSHandler.getInstance().notifyWifiDisconnected();
        }
        return true;
    }

    public boolean ignoreSupplicantStateChange(SupplicantState state) {
        if (state == SupplicantState.ASSOCIATING) {
            this.mTimeStampScanControlForAssoc = System.currentTimeMillis();
            this.mTimeOutScanControlForAssoc = TIMEOUT_CONTROL_SCAN_ASSOCIATING;
        } else if (state == SupplicantState.ASSOCIATED) {
            this.mTimeStampScanControlForAssoc = System.currentTimeMillis();
            this.mTimeOutScanControlForAssoc = TIMEOUT_CONTROL_SCAN_ASSOCIATED;
        } else if (!(state == SupplicantState.FOUR_WAY_HANDSHAKE || state == SupplicantState.AUTHENTICATING || state == SupplicantState.GROUP_HANDSHAKE)) {
            this.mTimeStampScanControlForAssoc = System.currentTimeMillis();
            this.mTimeOutScanControlForAssoc = 0;
        }
        Log.d(TAG, "update the timeout parameter for the scan control, timeout = " + this.mTimeOutScanControlForAssoc + ", state = " + state);
        if ((!isWifiProEvaluatingAP() || (state != SupplicantState.SCANNING && state != SupplicantState.ASSOCIATING && state != SupplicantState.AUTHENTICATING && state != SupplicantState.ASSOCIATED && state != SupplicantState.FOUR_WAY_HANDSHAKE && state != SupplicantState.AUTHENTICATING && state != SupplicantState.GROUP_HANDSHAKE && state != SupplicantState.COMPLETED)) && !selfCureIgnoreSuppStateChange(state)) {
            return false;
        }
        Log.d("WiFi_PRO", "ignoreSupplicantStateChange, state = " + state);
        return true;
    }

    private boolean disallowWifiScanForConnection() {
        long now = System.currentTimeMillis();
        if (now - this.mTimeStampScanControlForAssoc <= this.mTimeOutScanControlForAssoc) {
            Log.d(TAG, "disallowWifiScanForConnection, mTimeStampScanControlForAssoc = " + this.mTimeStampScanControlForAssoc + " mTimeOutScanControlForAssoc = " + this.mTimeOutScanControlForAssoc);
            return true;
        } else if (!WifiStateMachine.ObtainingIpState.class.equals(getCurrentState().getClass())) {
            this.mTimeLastCtrlScanDuringObtainingIp = 0;
            return false;
        } else if (this.mTimeLastCtrlScanDuringObtainingIp == 0) {
            this.mTimeLastCtrlScanDuringObtainingIp = now;
            Log.d(TAG, "disallowWifiScanForConnection, mTimeLastCtrlScanDuringObtainingIp = " + this.mTimeLastCtrlScanDuringObtainingIp);
            return true;
        } else if (now - this.mTimeLastCtrlScanDuringObtainingIp > TIMEOUT_CONTROL_SCAN_ASSOCIATED) {
            return false;
        } else {
            Log.d(TAG, "disallowWifiScanForConnection, mTimeLastCtrlScanDuringObtainingIp = " + this.mTimeLastCtrlScanDuringObtainingIp);
            return true;
        }
    }

    private void resetWifiProManualConnect() {
        Settings.System.putInt(this.myContext.getContentResolver(), "wifipro_manual_connect_ap", 0);
    }

    /* access modifiers changed from: private */
    public int getAppUid(String processName) {
        try {
            ApplicationInfo ai = this.myContext.getPackageManager().getApplicationInfo(processName, 1);
            if (ai != null) {
                return ai.uid;
            }
            return 1000;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 1000;
        }
    }

    private void registerForWifiEvaluateChanges() {
        this.myContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(WIFI_EVALUATE_TAG), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                int tag = Settings.Secure.getInt(HwWifiStateMachine.this.myContext.getContentResolver(), HwWifiStateMachine.WIFI_EVALUATE_TAG, 0);
                if (HwWifiStateMachine.this.mBQEUid == 1000) {
                    int unused = HwWifiStateMachine.this.mBQEUid = HwWifiStateMachine.this.getAppUid("com.huawei.wifiprobqeservice");
                }
                HwWifiStateMachine hwWifiStateMachine = HwWifiStateMachine.this;
                hwWifiStateMachine.logd("**wifipro tag is chenge, setWifiproFirewallEnable**,tag =" + tag);
                if (tag == 1) {
                    try {
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallEnable(true);
                        if (HwWifiStateMachine.this.mBQEUid != 1000) {
                            HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallWhitelist(HwWifiStateMachine.this.mBQEUid);
                        }
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallWhitelist(1000);
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallDrop();
                    } catch (Exception e) {
                        HwWifiStateMachine hwWifiStateMachine2 = HwWifiStateMachine.this;
                        hwWifiStateMachine2.loge("**setWifiproCmdEnable,Error Exception :" + e);
                    }
                } else if (tag == 0) {
                    try {
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallEnable(false);
                    } catch (Exception e1) {
                        HwWifiStateMachine hwWifiStateMachine3 = HwWifiStateMachine.this;
                        hwWifiStateMachine3.loge("**Disable WifiproCmdEnable***Error Exception " + e1);
                    }
                }
            }
        });
    }

    private void registerForPasspointChanges() {
        this.myContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(DBKEY_HOTSPOT20_VALUE), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (Settings.Global.getInt(HwWifiStateMachine.this.myContext.getContentResolver(), HwWifiStateMachine.DBKEY_HOTSPOT20_VALUE, 1) == 0) {
                    WifiConfiguration config = HwWifiStateMachine.this.getCurrentWifiConfiguration();
                    if (config != null && config.isPasspoint()) {
                        HwWifiStateMachine.this.disconnectCommand();
                    }
                }
            }
        });
    }

    private void handleWiFiConnectedByScanGenie(WifiConfigManager wifiConfigManager) {
        Log.d(TAG, "handleWiFiConnectedByScanGenie");
        if (HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.myContext)) {
            Log.d(TAG, "this is mobile ap,ScanGenie ignor it");
            return;
        }
        WifiConfiguration currentWifiConfig = getCurrentWifiConfiguration();
        if (currentWifiConfig != null && !currentWifiConfig.isTempCreated) {
            Log.d(TAG, "mWifiScanGenieController.handleWiFiConnected");
            WifiScanGenieController.createWifiScanGenieControllerImpl(this.myContext).handleWiFiConnected(currentWifiConfig, false);
        }
    }

    public void notifyWifiScanResultsAvailable(boolean success) {
        HwSelfCureEngine.getInstance(this.myContext, this).notifyWifiScanResultsAvailable(success);
    }

    public void notifyWifiRoamingStarted() {
        HwWifiConnectivityMonitor.getInstance(this.myContext, this).notifyWifiRoamingStarted();
    }

    public void notifyWifiRoamingCompleted(String newBssid) {
        if (newBssid != null) {
            HwQoEService mHwQoEService = HwQoEService.getInstance();
            if (mHwQoEService != null) {
                mHwQoEService.notifyNetworkRoaming();
            }
            HwSelfCureEngine.getInstance(this.myContext, this).notifyWifiRoamingCompleted(newBssid);
            HwWifiConnectivityMonitor.getInstance(this.myContext, this).notifyWifiRoamingCompleted();
            WifiScanGenieController.createWifiScanGenieControllerImpl(this.myContext).notifyNetworkRoamingCompleted(newBssid);
        }
    }

    public void notifyEnableSameNetworkId(int netId) {
        if (HwAutoConnectManager.getInstance() != null) {
            HwAutoConnectManager.getInstance().notifyEnableSameNetworkId(netId);
        }
    }

    public boolean isWlanSettingsActivity() {
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = this.mActivityManager.getRunningTasks(1);
        if (runningTaskInfos != null && !runningTaskInfos.isEmpty()) {
            ComponentName cn = runningTaskInfos.get(0).topActivity;
            if (cn == null || cn.getClassName() == null || !cn.getClassName().startsWith(HUAWEI_SETTINGS)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void requestUpdateDnsServers(ArrayList<String> dnses) {
        if (dnses != null && !dnses.isEmpty()) {
            sendMessage(131882, dnses);
        }
    }

    public void sendUpdateDnsServersRequest(Message msg, LinkProperties lp) {
        if (msg != null && msg.obj != null) {
            ArrayList<String> dnsesStr = (ArrayList) msg.obj;
            ArrayList<InetAddress> dnses = new ArrayList<>();
            int i = 0;
            while (i < dnsesStr.size()) {
                try {
                    dnses.add(Inet4Address.getByName(dnsesStr.get(i)));
                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!dnses.isEmpty()) {
                HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
                LinkProperties newLp = new LinkProperties(lp);
                newLp.setDnsServers(dnses);
                logd("sendUpdateDnsServersRequest, renew dns server newLp is: " + newLp);
                if (networkAgent != null) {
                    networkAgent.sendLinkProperties(newLp);
                }
            }
        }
    }

    public void requestRenewDhcp() {
        this.mRenewDhcpSelfCuring.set(true);
        sendMessage(131883);
    }

    public void handleInvalidIpAddr() {
        sendMessage(131895);
    }

    public void startSelfCureReconnect() {
        resetSelfCureParam();
        if (saveCurrentConfig()) {
            this.mWifiSelfCuring.set(true);
            resetSelfCureCandidateLostCnt();
            WifiProCommonUtils.setWifiSelfCureStatus(103);
            checkWifiBackgroundStatus();
            setSelfCureWifiTimeOut(5);
        }
    }

    public void handleNoInternetIp() {
        sendMessage(131898);
    }

    public void setForceDhcpDiscovery(IpClient ipClient) {
        if ((this.mRenewDhcpSelfCuring.get() || this.mWifiSelfCuring.get()) && ipClient != null) {
            logd("setForceDhcpDiscovery, force dhcp discovery for sce background cure internet.");
            ipClient.setForceDhcpDiscovery();
        }
    }

    public void resetIpConfigStatus() {
        this.mRenewDhcpSelfCuring.set(false);
    }

    public boolean isRenewDhcpSelfCuring() {
        return this.mRenewDhcpSelfCuring.get();
    }

    public void requestUseStaticIpConfig(StaticIpConfiguration staticIpConfig) {
        sendMessage(131884, staticIpConfig);
    }

    public void handleStaticIpConfig(IpClient ipClient, WifiNative wifiNative, StaticIpConfiguration config) {
        if (ipClient != null && wifiNative != null && config != null) {
            IpClient.ProvisioningConfiguration prov = IpClient.buildProvisioningConfiguration().withStaticConfiguration(config).withoutIpReachabilityMonitor().withApfCapabilities(wifiNative.getApfCapabilities(wifiStateMachineUtils.getInterfaceName(this))).build();
            logd("handleStaticIpConfig, startProvisioning");
            ipClient.startProvisioning(prov);
        }
    }

    public void notifyIpConfigCompleted() {
        HwSelfCureEngine.getInstance(this.myContext, this).notifyIpConfigCompleted();
    }

    public int getWifiApTypeFromMpLink() {
        return HwMpLinkContentAware.getInstance(this.myContext).getWifiApTypeAndSendMsg(getCurrentWifiConfiguration());
    }

    public boolean notifyIpConfigLostAndFixedBySce(WifiConfiguration config) {
        return HwSelfCureEngine.getInstance(this.myContext, this).notifyIpConfigLostAndHandle(config);
    }

    public void requestResetWifi() {
        sendMessage(131887);
    }

    public void requestReassocLink() {
        sendMessage(131886);
    }

    public void startSelfCureWifiReset() {
        resetSelfCureParam();
        if (!saveCurrentConfig()) {
            stopSelfCureDelay(1, 0);
            return;
        }
        this.mWifiSelfCuring.set(true);
        resetSelfCureCandidateLostCnt();
        WifiProCommonUtils.setWifiSelfCureStatus(102);
        checkWifiBackgroundStatus();
        selfCureWifiDisable();
    }

    public void startSelfCureWifiReassoc() {
        resetSelfCureParam();
        if (!saveCurrentConfig()) {
            stopSelfCureDelay(1, 0);
            return;
        }
        this.mWifiSelfCuring.set(true);
        resetSelfCureCandidateLostCnt();
        WifiProCommonUtils.setWifiSelfCureStatus(101);
        checkWifiBackgroundStatus();
        reassociateCommand();
        setSelfCureWifiTimeOut(4);
    }

    public void requestWifiSoftSwitch() {
        this.mWifiSoftSwitchRunning.set(true);
        WifiProCommonUtils.setWifiSelfCureStatus(104);
        sendMessageDelayed(131897, -4, 0, 15000);
    }

    private boolean saveCurrentConfig() {
        WifiConfiguration currentConfiguration = getCurrentWifiConfiguration();
        if (currentConfiguration == null) {
            stopSelfCureDelay(1, 0);
            return false;
        }
        this.mCurrentConfigurationKey = currentConfiguration.configKey();
        this.mCurrentConfigNetId = currentConfiguration.networkId;
        logd("saveCurrentConfig >> configKey=" + this.mCurrentConfigurationKey + " netid=" + this.mCurrentConfigNetId);
        return true;
    }

    private void updateNetworkId() {
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
        if (wifiConfigManager != null) {
            WifiConfiguration wifiConfig = wifiConfigManager.getConfiguredNetwork(this.mCurrentConfigurationKey);
            if (wifiConfig != null) {
                this.mCurrentConfigNetId = wifiConfig.networkId;
            }
        }
        logd("updateNetworkId >> configKey=" + this.mCurrentConfigurationKey + " netid=" + this.mCurrentConfigNetId);
    }

    private void resetSelfCureParam() {
        logd("ENTER: resetSelfCureParam");
        this.mWifiSelfCuring.set(false);
        WifiProCommonUtils.setWifiSelfCureStatus(0);
        this.mWifiAlwaysOnBeforeCure = false;
        this.mWifiBackgroundConnected = false;
        this.mCurrentConfigurationKey = null;
        this.mSelfCureWifiLastState = -1;
        this.mUserCloseWifiWhenSelfCure = false;
        this.mSelfCureNetworkLastState = NetworkInfo.DetailedState.IDLE;
        this.mSelfCureWifiConnectRetry = 0;
        removeMessages(131888);
        removeMessages(131889);
        removeMessages(131890);
        removeMessages(131891);
    }

    private void checkWifiBackgroundStatus() {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        logd("checkWifiBackgroundStatus: detailstate=" + networkInfo.getDetailedState() + " isMobileDataInactive=" + WifiProCommonUtils.isMobileDataInactive(this.myContext));
        setWifiBackgroundStatus(networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK && !WifiProCommonUtils.isMobileDataInactive(this.myContext));
    }

    public void setWifiBackgroundStatus(boolean background) {
        if (isWifiSelfCuring()) {
            logd("setWifiBackgroundStatus: " + background + " wifiBackgroundConnected=" + this.mWifiBackgroundConnected);
            this.mWifiBackgroundConnected = background;
        }
    }

    private void selfCureWifiDisable() {
        HwSelfCureEngine.getInstance(this.myContext, this).requestChangeWifiStatus(false);
        setSelfCureWifiTimeOut(1);
    }

    private void selfCureWifiEnable() {
        HwSelfCureEngine.getInstance(this.myContext, this).requestChangeWifiStatus(true);
        setSelfCureWifiTimeOut(2);
    }

    private void setSelfCureWifiTimeOut(int wifiSelfCureState) {
        int i;
        this.mWifiSelfCureState = wifiSelfCureState;
        switch (this.mWifiSelfCureState) {
            case 1:
                logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_OFF_TIMEOUT 2000");
                sendMessageDelayed(131888, -1, 0, TIMEOUT_CONTROL_SCAN_ASSOCIATING);
                break;
            case 2:
                logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_ON_TIMEOUT 3000");
                sendMessageDelayed(131889, -1, 0, 3000);
                break;
            case 3:
                if (((PowerManager) this.myContext.getSystemService("power")).isScreenOn()) {
                    i = 15000;
                } else {
                    i = 30000;
                }
                long delayedMs = (long) i;
                logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_CONNECT_TIMEOUT " + delayedMs);
                sendMessageDelayed(131890, -1, 0, delayedMs);
                break;
            case 4:
                logd("selfCureWifiResetCheck send delay messgae SCE_WIFI_REASSOC_STATE 12000");
                sendMessageDelayed(131891, -1, 0, 12000);
                break;
            case 5:
                logd("selfCureWifiResetCheck send delay messgae SCE_WIFI_RECONNECT_STATE 15000");
                sendMessageDelayed(131896, -1, 0, 15000);
                break;
            default:
                return;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00e9, code lost:
        if (r17 == 101) goto L_0x00eb;
     */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0120  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0197  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01dd  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01ef  */
    public boolean checkSelfCureWifiResult(int event) {
        int wifiState = syncGetWifiState();
        if (wifiState == 2) {
            this.mWifiEnabledTimeStamp = System.currentTimeMillis();
        }
        int i = -1;
        if (wifiState == 0) {
            if (!isWifiSelfCuring() && wifiStateMachineUtils.getScreenOn(this)) {
                WifiConfigManager wifiConfigMgr = wifiStateMachineUtils.getWifiConfigManager(this);
                for (WifiConfiguration config : wifiConfigMgr.getConfiguredNetworks()) {
                    if (config.portalCheckStatus != 0) {
                        config.portalCheckStatus = 0;
                        wifiConfigMgr.updateInternetInfoByWifiPro(config);
                    }
                }
                List<ScanResult> scanResults = new ArrayList<>();
                ScanRequestProxy scanProxy = WifiInjector.getInstance().getScanRequestProxy();
                if (scanProxy != null) {
                    synchronized (scanProxy) {
                        for (ScanResult result : scanProxy.getScanResults()) {
                            scanResults.add(new ScanResult(result));
                        }
                    }
                    if (scanResults.size() > 0) {
                        setWiFiProScanResultList(scanResults);
                    }
                }
            }
            if (isWifiSelfCuring() && !this.mUserCloseWifiWhenSelfCure && !isWifiSelfCureByReset()) {
                logd("checkSelfCureWifiResult, user close wifi during reassoc or reconnect self-cure going.");
                this.mUserCloseWifiWhenSelfCure = true;
                removeMessages(131891);
                removeMessages(131896);
                exitWifiSelfCure(1, -1);
                return false;
            }
        }
        if (this.mWifiSoftSwitchRunning.get() && wifiState == 0) {
            logd("checkSelfCureWifiResult, WifiSoftSwitchRunning, WIFI_STATE_DISABLING.");
            removeMessages(131897);
            sendMessage(131897, -4, 0);
            return false;
        } else if (!isWifiSelfCuring() || this.mUserCloseWifiWhenSelfCure || wifiState == 4) {
            int i2 = event;
            logd("userCloseWifiWhenSelfCure = " + this.mUserCloseWifiWhenSelfCure + ", wifiState = " + wifiState);
            return false;
        } else {
            boolean ret = true;
            if (this.mSelfCureWifiLastState > wifiState && this.mWifiSelfCureState != 1) {
                int i3 = event;
            } else if (wifiState != 0 || this.mSelfCureWifiLastState != wifiState) {
                int i4 = event;
                this.mSelfCureWifiLastState = wifiState;
                switch (this.mWifiSelfCureState) {
                    case 1:
                        if (wifiState == 1) {
                            removeMessages(131888);
                            logd("wifi disabled > CMD_SCE_WIFI_OFF_TIMEOUT msg removed");
                            notifySelfCureComplete(true, 0);
                            break;
                        }
                        break;
                    case 2:
                        if (wifiState == 3) {
                            removeMessages(131889);
                            logd("wifi enabled > CMD_SCE_WIFI_ON_TIMEOUT msg removed");
                            notifySelfCureComplete(true, 0);
                            break;
                        }
                        break;
                    case 3:
                        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
                        if ((!isDuplicateNetworkState(networkInfo) && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) || networkInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                            logd("wifi connect > CMD_SCE_WIFI_CONNECT_TIMEOUT msg removed state=" + networkInfo.getDetailedState());
                            removeMessages(131890);
                            boolean connSucc = isWifiConnectToSameAP();
                            if (connSucc) {
                                i = 0;
                            }
                            notifySelfCureComplete(connSucc, i);
                            break;
                        }
                    case 4:
                    case 5:
                        NetworkInfo networkInfo2 = wifiStateMachineUtils.getNetworkInfo(this);
                        if ((isDuplicateNetworkState(networkInfo2) || networkInfo2.getDetailedState() != NetworkInfo.DetailedState.CONNECTED) && networkInfo2.getDetailedState() != NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                            if (!isDuplicateNetworkState(networkInfo2) && networkInfo2.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                                logd("wifi reassociate/reconnect > CMD_SCE_WIFI_REASSOC_TIMEOUT msg removed state=" + networkInfo2.getDetailedState());
                                removeMessages(131891);
                                removeMessages(131896);
                                notifySelfCureComplete(false, -1);
                                break;
                            }
                        } else {
                            logd("wifi reassociate/reconnect > CMD_SCE_WIFI_REASSOC_TIMEOUT msg removed state=" + networkInfo2.getDetailedState());
                            removeMessages(131891);
                            removeMessages(131896);
                            boolean connSucc2 = isWifiConnectToSameAP();
                            if (connSucc2) {
                                i = 0;
                            }
                            notifySelfCureComplete(connSucc2, i);
                            break;
                        }
                        break;
                }
                return ret;
            }
            logd("last state =" + this.mSelfCureWifiLastState + ", current state=" + wifiState + ", user may toggle wifi! stop selfcure");
            exitWifiSelfCure(1, -1);
            this.mUserCloseWifiWhenSelfCure = true;
            ret = false;
            this.mSelfCureWifiLastState = wifiState;
            switch (this.mWifiSelfCureState) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                case 5:
                    break;
            }
            return ret;
        }
    }

    private boolean isDuplicateNetworkState(NetworkInfo networkInfo) {
        boolean ret = false;
        if (networkInfo != null && this.mSelfCureNetworkLastState == networkInfo.getDetailedState()) {
            log("duplicate network state non-change " + networkInfo.getDetailedState());
            ret = true;
        }
        this.mSelfCureNetworkLastState = networkInfo.getDetailedState();
        return ret;
    }

    private boolean isWifiConnectToSameAP() {
        WifiConfiguration wifiConfig = getCurrentWifiConfiguration();
        if (this.mCurrentConfigurationKey == null || wifiConfig == null || wifiConfig.configKey() == null || !this.mCurrentConfigurationKey.equals(wifiConfig.configKey())) {
            return false;
        }
        return true;
    }

    public boolean isBssidDisabled(String bssid) {
        return false;
    }

    public void resetSelfCureCandidateLostCnt() {
        WifiInjector.getInstance().getSavedNetworkEvaluator().resetSelfCureCandidateLostCnt();
    }

    public boolean isWifiSelfCuring() {
        return this.mWifiSelfCuring.get();
    }

    public int getSelfCureNetworkId() {
        return this.mCurrentConfigNetId;
    }

    public long getWifiEnabledTimeStamp() {
        return this.mWifiEnabledTimeStamp;
    }

    public boolean reportWifiScoreDelayed() {
        return this.mDelayWifiScoreBySelfCureOrSwitch;
    }

    public void notifySelfCureComplete(boolean success, int reasonCode) {
        if (!success && reasonCode == -4) {
            Log.d("WIFIPRO", "notifySelfCureComplete SOFT_CONNECT_FAILED, timeout happend");
            this.mWifiSoftSwitchRunning.set(false);
            WifiProCommonUtils.setWifiSelfCureStatus(0);
            stopSelfCureDelay(-4, 0);
        } else if (!isWifiSelfCuring()) {
            logd("notifySelfCureComplete: not Curing!");
            stopSelfCureDelay(1, 0);
        } else {
            if (success) {
                handleSelfCureNormal();
            } else {
                handleSelfCureException(reasonCode);
            }
        }
    }

    public void notifySelfCureNetworkLost() {
        if (hasMessages(131890)) {
            logd("notifySelfCureNetworkLost, Stop Reset");
            removeMessages(131890);
            sendMessage(131890, -2, 0);
        } else if (hasMessages(131891)) {
            logd("notifySelfCureNetworkLost, Stop Reassociate");
            removeMessages(131891);
            sendMessage(131891, -2, 0);
        } else {
            logd("notifySelfCureNetworkLost, No delay message found.");
        }
    }

    private void handleSelfCureNormal() {
        switch (this.mWifiSelfCureState) {
            case 1:
                logd("handleSelfCureNormal, wifi off OK! -> wifi on");
                selfCureWifiEnable();
                break;
            case 2:
                logd("handleSelfCureNormal, wifi on OK! -> wifi connect");
                setSelfCureWifiTimeOut(3);
                if (HwABSUtils.getABSEnable()) {
                    HwABSDetectorService service = HwABSDetectorService.getInstance();
                    if (service != null) {
                        service.notifySelEngineEnableWiFi();
                        break;
                    }
                }
                break;
            case 3:
            case 4:
            case 5:
                logd("handleSelfCureNormal, wifi connect/reassoc/reconnect OK!");
                if (this.mWifiBackgroundConnected) {
                    logd("handleSelfCureNormal, wifiBackgroundConnected, wifiNetworkExplicitlyUnselected");
                    wifiNetworkExplicitlyUnselected();
                }
                stopSelfCureDelay(0, 500);
                break;
            default:
                return;
        }
    }

    private void handleSelfCureException(int reasonCode) {
        switch (this.mWifiSelfCureState) {
            case 1:
                stopSelfCureDelay(-1, 0);
                logd("handleSelfCureException, wifi off fail! -> wifi off");
                HwSelfCureEngine.getInstance(this.myContext, this).requestChangeWifiStatus(false);
                break;
            case 2:
                stopSelfCureDelay(-1, 0);
                logd("handleSelfCureException, wifi on fail! -> wifi on");
                HwSelfCureEngine.getInstance(this.myContext, this).requestChangeWifiStatus(true);
                break;
            case 3:
            case 4:
            case 5:
                logd("handleSelfCureException, wifi connect/reassoc/reconnect failed! retry = " + this.mSelfCureWifiConnectRetry + ", reason = " + reasonCode);
                if (this.mSelfCureWifiConnectRetry < 1 && reasonCode != -2) {
                    this.mSelfCureWifiConnectRetry++;
                    startConnectToUserSelectNetwork(this.mCurrentConfigNetId, Binder.getCallingUid(), null);
                    setSelfCureWifiTimeOut(3);
                    break;
                } else {
                    stopSelfCureDelay(reasonCode == -2 ? -2 : -1, 0);
                    if (!this.mWifiBackgroundConnected) {
                        if (reasonCode != -2) {
                            startConnectToUserSelectNetwork(this.mCurrentConfigNetId, Binder.getCallingUid(), null);
                        }
                        this.mCurrentConfigNetId = -1;
                        break;
                    } else {
                        disconnectCommand();
                        break;
                    }
                }
            default:
                return;
        }
    }

    public void stopSelfCureWifi(int status) {
        log("stopSelfCureWifi, status =" + status);
        if (status == -4) {
            log("notify soft connect time out failed.");
            sendWifiHandoverCompletedBroadcast(-6, null, null, null);
            sendMessage(131893);
        } else if (isWifiSelfCuring()) {
            NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
            if (this.mWifiBackgroundConnected && networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                logd("stopSelfCureWifi,  CONNECTED => POOR_LINK_DETECTED");
                sendMessage(131873);
            }
            HwSelfCureEngine.getInstance(this.myContext, this).notifySefCureCompleted(status);
            resetSelfCureParam();
            sendMessage(131893);
        }
    }

    public void stopSelfCureDelay(int status, int delay) {
        if (hasMessages(131892)) {
            removeMessages(131892);
        }
        sendMessageDelayed(obtainMessage(131892, status, 0), (long) delay);
    }

    public void exitWifiSelfCure(int exitedType, int networkId) {
        if (isWifiSelfCuring()) {
            if (networkId == -1 || networkId == getSelfCureNetworkId()) {
                logd("exitWifiSelfCure, CONNECT_NETWORK/FORGET_NETWORK/CLOSE_WIFI stop SCE, type = " + exitedType);
                WifiProCommonUtils.setWifiSelfCureStatus(0);
                HwSelfCureEngine.getInstance(this.myContext, this).notifyWifiDisconnected();
                int status = 1;
                if (exitedType == 151553 || exitedType == 151556 || exitedType == 1) {
                    status = -3;
                }
                stopSelfCureDelay(status, 0);
            } else {
                logd("exitWifiSelfCure, user forget other network, do nothing.");
            }
        }
    }

    @Deprecated
    public List<String> syncGetApLinkedStaList(AsyncChannel channel) {
        log("HwWiFIStateMachine syncGetApLinkedStaList");
        Message resultMsg = channel.sendMessageSynchronously(CMD_AP_STARTED_GET_STA_LIST);
        List<String> ret = (List) resultMsg.obj;
        resultMsg.recycle();
        return ret;
    }

    @Deprecated
    public void handleSetSoftapMacFilter(String macFilter) {
        log("HwWifiStateMachine handleSetSoftapMacFilter is called, macFilter =" + macFilter);
        WifiInjector.getInstance().getWifiNative().setSoftapMacFltrHw(macFilter);
    }

    @Deprecated
    public void handleSetSoftapDisassociateSta(String mac) {
        log("HwWifiStateMachine handleSetSoftapDisassociateSta is called, mac =" + mac);
        WifiInjector.getInstance().getWifiNative().disassociateSoftapStaHw(mac);
    }

    public boolean handleWapiFailureEvent(Message message, SupplicantStateTracker mSupplicantStateTracker) {
        if (147474 == message.what) {
            log("Handling WAPI_EVENT, msg [" + message.what + "]");
            Intent intent = new Intent(SUPPLICANT_WAPI_EVENT);
            intent.putExtra("wapi_string", 16);
            this.myContext.sendBroadcast(intent);
            mSupplicantStateTracker.sendMessage(147474);
            return true;
        } else if (147475 != message.what) {
            return false;
        } else {
            log("Handling WAPI_EVENT, msg [" + message.what + "]");
            Intent intent2 = new Intent(SUPPLICANT_WAPI_EVENT);
            intent2.putExtra("wapi_string", 17);
            this.myContext.sendBroadcast(intent2);
            return true;
        }
    }

    public void handleStopWifiRepeater(AsyncChannel wifiP2pChannel) {
        wifiP2pChannel.sendMessage(CMD_STOP_WIFI_REPEATER);
    }

    public boolean isWifiRepeaterStarted() {
        return 1 == Settings.Global.getInt(this.myContext.getContentResolver(), "wifi_repeater_on", 0) || 6 == Settings.Global.getInt(this.myContext.getContentResolver(), "wifi_repeater_on", 0);
    }

    public void setWifiRepeaterStoped() {
        Settings.Global.putInt(this.myContext.getContentResolver(), "wifi_repeater_on", 0);
    }

    public void triggerUpdateAPInfo() {
        if (HWFLOW) {
            Log.d(TAG, "triggerUpdateAPInfo");
        }
        new Thread(new FilterScanRunnable(getScanResultsListNoCopyUnsync())).start();
    }

    public void sendStaFrequency(int frequency) {
        if (mFrequency != frequency && frequency >= 5180) {
            mFrequency = frequency;
            log("sendStaFrequency " + mFrequency);
            Intent intent = new Intent("android.net.wifi.p2p.STA_FREQUENCY_CREATED");
            intent.putExtra("freq", String.valueOf(frequency));
            this.myContext.sendBroadcast(intent);
        }
    }

    public boolean isHiLinkActive() {
        if (this.mHiLinkController != null) {
            return this.mHiLinkController.isHiLinkActive();
        }
        return HwWifiStateMachine.super.isHiLinkActive();
    }

    public void enableHiLinkHandshake(boolean uiEnable, String bssid) {
        if (uiEnable) {
            clearRandomMacOui();
            this.mIsRandomMacCleared = true;
        } else if (this.mIsRandomMacCleared) {
            setRandomMacOui();
            this.mIsRandomMacCleared = false;
        }
        this.mHiLinkController.enableHiLinkHandshake(uiEnable, bssid);
        WifiInjector.getInstance().getWifiNative().enableHiLinkHandshake(uiEnable, bssid);
    }

    public void sendWpsOkcStartedBroadcast() {
        this.mHiLinkController.sendWpsOkcStartedBroadcast();
    }

    public NetworkUpdateResult saveWpsOkcConfiguration(int connectionNetId, String connectionBssid) {
        List<ScanResult> scanResults = new ArrayList<>();
        if (!WifiInjector.getInstance().getWifiStateMachineHandler().runWithScissors(new Runnable(scanResults) {
            private final /* synthetic */ List f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                this.f$1.addAll(HwWifiStateMachine.wifiStateMachineUtils.getScanRequestProxy(HwWifiStateMachine.this).getScanResults());
            }
        }, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT)) {
            Log.e(TAG, "Failed to post runnable to fetch scan results");
        }
        return this.mHiLinkController.saveWpsOkcConfiguration(connectionNetId, connectionBssid, scanResults);
    }

    public void handleAntenaPreempted() {
        log(getName() + "EVENT_ANT_CORE_ROB");
        this.myContext.sendBroadcastAsUser(new Intent(HwABSUtils.ACTION_WIFI_ANTENNA_PREEMPTED), UserHandle.ALL, HwABSUtils.HUAWEI_BUSSINESS_PERMISSION);
    }

    public void handleDualbandHandoverFailed(int disableReason) {
        if (this.mWifiSwitchOnGoing && disableReason == 3 && WifiProStateMachine.getWifiProStateMachineImpl().getNetwoksHandoverType() == 4) {
            log("handleDualbandHandoverFailed, disableReason = " + disableReason);
            String failedBssid = null;
            String failedSsid = null;
            synchronized (this.selectConfigLock) {
                if (this.mSelectedConfig != null) {
                    failedBssid = this.mSelectedConfig.BSSID;
                    failedSsid = this.mSelectedConfig.SSID;
                }
            }
            log("handleDualbandHandoverFailed, sendWifiHandoverCompletedBroadcast, status = " + -7);
            sendWifiHandoverCompletedBroadcast(-7, failedBssid, failedSsid, null);
        }
    }

    public void setWiFiProRoamingSSID(WifiSsid SSID) {
        this.mWiFiProRoamingSSID = SSID;
    }

    public WifiSsid getWiFiProRoamingSSID() {
        return this.mWiFiProRoamingSSID;
    }

    public boolean isEnterpriseHotspot(WifiConfiguration config) {
        if (config != null) {
            String currentSsid = config.SSID;
            String configKey = config.configKey();
            if (TextUtils.isEmpty(currentSsid) || TextUtils.isEmpty(configKey)) {
                return false;
            }
            List<ScanResult> scanResults = new ArrayList<>();
            if (!WifiInjector.getInstance().getWifiStateMachineHandler().runWithScissors(new Runnable(scanResults) {
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    this.f$1.addAll(HwWifiStateMachine.wifiStateMachineUtils.getScanRequestProxy(HwWifiStateMachine.this).getScanResults());
                }
            }, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT)) {
                Log.e(TAG, "Failed to post runnable to fetch scan results");
                return false;
            }
            int foundCounter = 0;
            for (int i = 0; i < scanResults.size(); i++) {
                String scanSsid = "\"" + scanResults.get(i).SSID + "\"";
                String capabilities = scanResults.get(i).capabilities;
                if (currentSsid.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(capabilities, configKey)) {
                    foundCounter++;
                    if (foundCounter >= 4) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getConnectionRawPsk() {
        log("getConnectionRawPsk.");
        if (this.myContext.checkCallingPermission("com.huawei.permission.ACCESS_AP_INFORMATION") != 0) {
            log("getConnectionRawPsk: permissin denied.");
            return null;
        } else if (-1 != wifiStateMachineUtils.getWifiInfo(this).getNetworkId()) {
            String ret = WifiInjector.getInstance().getWifiNative().getConnectionRawPsk();
            log("getConnectionRawPsk: OK");
            return ret;
        } else {
            log("getConnectionRawPsk: netId is invalid.");
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void notifyWlanChannelNumber(int channel) {
        if (channel > 13) {
            channel = 0;
        }
        WifiCommonUtils.notifyDeviceState("WLAN", String.valueOf(channel), "");
    }

    /* access modifiers changed from: protected */
    public void notifyWlanState(String state) {
        WifiCommonUtils.notifyDeviceState("WLAN", state, "");
    }

    private long getScanInterval() {
        long scanInterval;
        if (wifiStateMachineUtils.getOperationalMode(this) == 3) {
            scanInterval = Settings.Global.getLong(this.myContext.getContentResolver(), WIFI_SCAN_INTERVAL_WLAN_CLOSE, WIFI_SCAN_INTERVAL_WLAN_CLOSE_DEFAULT);
        } else if (wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
            scanInterval = Settings.Global.getLong(this.myContext.getContentResolver(), WIFI_SCAN_INTERVAL_WHITE_WLAN_CONNECTED, 10000);
        } else {
            scanInterval = Settings.Global.getLong(this.myContext.getContentResolver(), WIFI_SCAN_INTERVAL_WLAN_NOT_CONNECTED, 10000);
        }
        logd("the wifi_scan interval is:" + scanInterval);
        return scanInterval;
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:41:0x0099=Splitter:B:41:0x0099, B:47:0x00ac=Splitter:B:47:0x00ac} */
    public synchronized boolean disallowWifiScanRequest(int pid) {
        int i = pid;
        synchronized (this) {
            if (disallowWifiScanForConnection()) {
                Log.d(TAG, "disallowWifiScanForConnection");
                return true;
            }
            ActivityManager.RunningAppProcessInfo appProcessInfo = getAppProcessInfoByPid(pid);
            if (!(i <= 0 || appProcessInfo == null || appProcessInfo.pkgList == null)) {
                if (!this.mIsScanCtrlPluggedin) {
                    if (isGlobalScanCtrl(appProcessInfo)) {
                        logd("isGlobalScanCtrl contrl scan ");
                        sendMessageDelayed(CMD_SCREEN_OFF_SCAN, WIFI_SCAN_RESULT_DELAY_TIME_DEFAULT);
                        return true;
                    }
                    wifiScanBlackListLearning(appProcessInfo);
                    long scanInterval = getScanInterval();
                    if (isWifiScanBlacklisted(appProcessInfo, scanInterval)) {
                        long now = System.currentTimeMillis();
                        long appLastScanRequestTimestamp = 0;
                        if (this.mPidLastScanSuccTimestamp.containsKey(Integer.valueOf(pid))) {
                            appLastScanRequestTimestamp = this.mPidLastScanSuccTimestamp.get(Integer.valueOf(pid)).longValue();
                        }
                        if (this.lastScanResultTimestamp == 0 || (now - this.lastScanResultTimestamp >= scanInterval && now - appLastScanRequestTimestamp >= scanInterval)) {
                            this.mPidLastScanSuccTimestamp.put(Integer.valueOf(pid), Long.valueOf(now));
                        } else {
                            if (now - this.lastScanResultTimestamp < 0) {
                                logd("wifi_scan the last scan time is jump!!!");
                                this.lastScanResultTimestamp = now;
                            }
                            sendMessageDelayed(CMD_SCREEN_OFF_SCAN, WIFI_SCAN_RESULT_DELAY_TIME_DEFAULT);
                            return true;
                        }
                    }
                    updateGlobalScanTimes();
                    return false;
                }
            }
            logd("wifi_scan pid[" + i + "] is not correct or is charging. mIsScanCtrlPluggedin = " + this.mIsScanCtrlPluggedin + " isInGlobalScanCtrl = " + this.isInGlobalScanCtrl);
            return false;
        }
    }

    public boolean isRSDBSupported() {
        return WifiInjector.getInstance().getWifiNative().isSupportRsdbByDriver();
    }

    /* access modifiers changed from: protected */
    public void handleSimAbsent(WifiConfiguration config) {
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
        if (PreconfiguredNetworkManager.IS_R1 && config.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod()) && PreconfiguredNetworkManager.getInstance().isPreconfiguredNetwork(config.SSID)) {
            wifiConfigManager.disableNetwork(config.networkId, 1000);
            this.wifiEapUIManager.showDialog(Resources.getSystem().getString(33686238), Resources.getSystem().getString(33686236));
        }
    }

    /* access modifiers changed from: protected */
    public void handleEapErrorcodeReport(int networkId, String ssid, int errorCode) {
        if (PreconfiguredNetworkManager.IS_R1 && PreconfiguredNetworkManager.getInstance().isPreconfiguredNetwork(ssid)) {
            wifiStateMachineUtils.getWifiConfigManager(this).updateNetworkSelectionStatus(networkId, 15);
            this.wifiEapUIManager.showDialog(errorCode);
        }
    }

    private void wifiScanBlackListLearning(ActivityManager.RunningAppProcessInfo appProcessInfo) {
        long now = System.currentTimeMillis();
        long scanInterval = getScanInterval();
        int pid = appProcessInfo.pid;
        clearDeadPidCache();
        if (!this.mPidLastScanTimestamp.containsKey(Integer.valueOf(pid))) {
            this.mPidLastScanTimestamp.put(Integer.valueOf(pid), Long.valueOf(now));
            this.mPidWifiScanCount.put(Integer.valueOf(pid), 0);
            return;
        }
        if (!this.mPidWifiScanCount.containsKey(Integer.valueOf(pid))) {
            this.mPidWifiScanCount.put(Integer.valueOf(pid), 0);
        }
        long tmpLastScanRequestTimestamp = this.mPidLastScanTimestamp.get(Integer.valueOf(pid)).longValue();
        this.mPidLastScanTimestamp.put(Integer.valueOf(pid), Long.valueOf(now));
        if (tmpLastScanRequestTimestamp != 0 && now >= tmpLastScanRequestTimestamp) {
            if (isWifiScanInBlacklistCache(pid) || now - tmpLastScanRequestTimestamp >= scanInterval) {
                if (isWifiScanInBlacklistCache(pid) != 0 && now - tmpLastScanRequestTimestamp > WIFI_SCAN_BLACKLIST_REMOVE_INTERVAL) {
                    logd("wifi_scan blacklist cache remove pid:" + pid);
                    removeWifiScanBlacklistCache(pid);
                }
                this.mPidWifiScanCount.put(Integer.valueOf(pid), 0);
                return;
            }
            int count = this.mPidWifiScanCount.get(Integer.valueOf(pid)).intValue() + 1;
            this.mPidWifiScanCount.put(Integer.valueOf(pid), Integer.valueOf(count));
            if (((long) count) >= WIFI_SCAN_OVER_INTERVAL_MAX_COUNT) {
                this.mPidLastScanTimestamp.remove(Integer.valueOf(pid));
                this.mPidWifiScanCount.remove(Integer.valueOf(pid));
                logd("pid:" + pid + " wifi_scan interval is frequent");
                if (!isWifiScanWhitelisted(appProcessInfo)) {
                    addWifiScanBlacklistCache(appProcessInfo);
                }
            }
        }
    }

    private boolean isWifiScanInBlacklistCache(int pid) {
        for (Map.Entry<String, Integer> entry : this.mPidBlackList.entrySet()) {
            if (pid == entry.getValue().intValue()) {
                logd("pid:" + pid + " in wifi_scan cache blacklist, appname=" + entry.getKey());
                return true;
            }
        }
        for (Map.Entry<String, Integer> entry2 : this.mPidConnectedBlackList.entrySet()) {
            if (pid == entry2.getValue().intValue()) {
                logd("pid:" + pid + " in wifi_scan connected cache blacklist, appname=" + entry2.getKey());
                return true;
            }
        }
        return false;
    }

    private void removeWifiScanBlacklistCache(int pid) {
        this.mPidLastScanSuccTimestamp.remove(Integer.valueOf(pid));
        this.mPidLastScanTimestamp.remove(Integer.valueOf(pid));
        this.mPidWifiScanCount.remove(Integer.valueOf(pid));
        Iterator iter = this.mPidBlackList.entrySet().iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            Map.Entry<String, Integer> entry = iter.next();
            if (pid == entry.getValue().intValue()) {
                logd("pid:" + pid + " remove from wifi_scan cache blacklist success, appname=" + entry.getKey());
                iter.remove();
                break;
            }
        }
        Iterator iter2 = this.mPidConnectedBlackList.entrySet().iterator();
        while (iter2.hasNext()) {
            Map.Entry<String, Integer> entry2 = iter2.next();
            if (pid == entry2.getValue().intValue()) {
                logd("pid:" + pid + " remove from wifi_scan connected cache blacklist success, appname=" + entry2.getKey());
                iter2.remove();
                return;
            }
        }
    }

    private void addWifiScanBlacklistCache(ActivityManager.RunningAppProcessInfo appProcessInfo) {
        int pid = appProcessInfo.pid;
        String appName = appProcessInfo.pkgList[0];
        logd("pid:" + pid + " add to wifi_scan connected limited blacklist");
        if (wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
            this.mPidConnectedBlackList.put(appName, Integer.valueOf(pid));
        } else {
            this.mPidBlackList.put(appName, Integer.valueOf(pid));
        }
    }

    private boolean isWifiScanBlacklisted(ActivityManager.RunningAppProcessInfo appProcessInfo, long scanInterval) {
        if (isPackagesNamesMatched(appProcessInfo.pkgList, this.myContext.getResources().getStringArray(33816587), null)) {
            logd("config blacklist wifi_scan name:callingPkgNames[pid=" + appProcessInfo.pid + "]=" + appProcessInfo.processName);
            return true;
        }
        if (!wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
            this.mPidConnectedBlackList.clear();
        } else {
            this.mPidBlackList.clear();
        }
        if (!isWifiScanConnectedLimitedWhitelisted(appProcessInfo) && this.mPidBlackListInteval > 0 && this.mPidBlackListInteval != scanInterval) {
            logd("wifi_scan blacklist clear because the interval is change");
            this.mPidBlackList.clear();
            this.mPidBlackListInteval = 0;
        }
        return isWifiScanInBlacklistCache(appProcessInfo.pid);
    }

    private boolean isPackagesNamesMatched(String[] callingPkgNames, String[] whitePkgs, String whiteDbPkgs) {
        int whitePkgsLength = 0;
        if (whitePkgs != null) {
            whitePkgsLength = whitePkgs.length;
        }
        if (callingPkgNames == null || (whiteDbPkgs == null && whitePkgsLength == 0)) {
            logd("wifi_scan input PkgNames are not correct");
            return false;
        }
        for (int j = 0; j < whitePkgsLength; j++) {
            logd("config--list:" + whitePkgs[j]);
        }
        logd("config--db:" + whiteDbPkgs);
        int i = 0;
        while (i < callingPkgNames.length) {
            for (int j2 = 0; j2 < whitePkgsLength; j2++) {
                if (callingPkgNames[i].equals(whitePkgs[j2])) {
                    logd("config white wifi_scan name:callingPkgNames[" + Integer.toString(i) + "]=" + callingPkgNames[i]);
                    return true;
                }
            }
            if (whiteDbPkgs == null || !TextUtils.delimitedStringContains(whiteDbPkgs, ',', callingPkgNames[i])) {
                i++;
            } else {
                logd("db white wifi_scan name:callingPkgNames[" + Integer.toString(i) + "]=" + callingPkgNames[i]);
                return true;
            }
        }
        return false;
    }

    private boolean isWifiScanConnectedLimitedWhitelisted(ActivityManager.RunningAppProcessInfo appProcessInfo) {
        String[] callingPkgNames = appProcessInfo.pkgList;
        String[] whitePkgs = this.myContext.getResources().getStringArray(33816588);
        String whiteDbPkgs = Settings.Global.getString(this.myContext.getContentResolver(), WIFI_SCAN_CONNECTED_LIMITED_WHITE_PACKAGENAME);
        if (appProcessInfo.uid == 1000) {
            return true;
        }
        if (!isPackagesNamesMatched(callingPkgNames, whitePkgs, whiteDbPkgs)) {
            return false;
        }
        logd("wifi_scan pkgname is in connected whitelist pkgs");
        return true;
    }

    private boolean isWifiScanWhitelisted(ActivityManager.RunningAppProcessInfo appProcessInfo) {
        if (isPackagesNamesMatched(appProcessInfo.pkgList, this.myContext.getResources().getStringArray(33816589), Settings.Global.getString(this.myContext.getContentResolver(), WIFI_SCAN_WHITE_PACKAGENAME))) {
            logd("wifi_scan pkgname is in whitelist pkgs");
            return true;
        } else if (Binder.getCallingUid() == 1000 || Binder.getCallingUid() == 1010) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized void updateLastScanRequestTimestamp() {
        this.lastScanResultTimestamp = System.currentTimeMillis();
        logd("wifi_scan update lastScanResultTimestamp=" + this.lastScanResultTimestamp);
    }

    private ActivityManager.RunningAppProcessInfo getAppProcessInfoByPid(int pid) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.myContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pid) {
                logd("PkgInfo--uid=" + appProcess.uid + ", processName=" + appProcess.processName + ",pid=" + pid);
                return appProcess;
            }
        }
        return null;
    }

    private void clearDeadPidCache() {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.myContext.getSystemService("activity")).getRunningAppProcesses();
        ArrayList<Integer> tmpPidSet = new ArrayList<>();
        Iterator iter = this.mPidLastScanTimestamp.entrySet().iterator();
        if (appProcessList != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                tmpPidSet.add(Integer.valueOf(appProcess.pid));
            }
            while (iter.hasNext()) {
                Integer key = iter.next().getKey();
                if (!tmpPidSet.contains(key)) {
                    iter.remove();
                    this.mPidWifiScanCount.remove(key);
                    this.mPidLastScanSuccTimestamp.remove(key);
                }
            }
        }
    }

    public void transitionToCallback(IState destState) {
        if (this.mDestStates != null) {
            this.mDestStates.offer(destState);
        }
        Log.i(TAG, "transition to " + destState.getClass().getSimpleName() + " begining.");
    }

    /* access modifiers changed from: protected */
    public void onPostHandleMessage(Message msg) {
        if (this.mDestStates != null) {
            IState destState = this.mDestStates.poll();
            if (destState != null) {
                Log.i(TAG, "transition to " + destState.getClass().getSimpleName() + " finished.");
            }
        }
    }

    /* access modifiers changed from: private */
    public void setLowPwrMode(boolean isConnected, String ssid, boolean isMobileAP, boolean isScreenOn) {
        boolean isHwSsid = false;
        boolean isCloneSsid = false;
        if (ssid != null) {
            isHwSsid = ssid.equals("\"Huawei-Employee\"");
            isCloneSsid = ssid.contains("CloudClone");
        }
        logd("setpmlock:isConnected: " + isConnected + " ssid:" + ssid + " isMobileAP:" + isMobileAP + " isAndroidMobileAP:" + isAndroidMobileAP());
        if (!isConnected || (!isHwSsid && ((!isMobileAP || !isAndroidMobileAP() || isCloneSsid) && isScreenOn && !this.mssArbi.matchAllowMSSApkList()))) {
            WifiInjector.getInstance().getWifiNative().gameKOGAdjustSpeed(0, 6);
        } else {
            WifiInjector.getInstance().getWifiNative().gameKOGAdjustSpeed(0, 7);
        }
    }

    private void pwrBoostRegisterBcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addCategory("android.net.wifi.STATE_CHANGE@hwBrExpand@WifiNetStatus=WIFICON|WifiNetStatus=WIFIDSCON");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.myContext.registerReceiver(this.mBcastReceiver, filter);
    }

    /* access modifiers changed from: private */
    public void linkMeasureAndStatic(boolean enable) {
        long ret = 0;
        long arpRtt = 0;
        int arpCnt = 0;
        WifiNative.TxPacketCounters txPacketCounters = WifiInjector.getInstance().getWifiNative().getTxPacketCounters(wifiStateMachineUtils.getInterfaceName(this));
        if (txPacketCounters != null) {
            int lastTxGoodCnt = txPacketCounters.txSucceeded;
            int lastTxBadCnt = txPacketCounters.txFailed;
            long lastTxRetries = wifiStateMachineUtils.getWifiInfo(this).txRetries;
            HwArpUtils hwArpUtils = new HwArpUtils(this.myContext);
            int i = 0;
            while (true) {
                WifiNative.TxPacketCounters txPacketCounters2 = txPacketCounters;
                if (i >= 5) {
                    break;
                }
                ret = hwArpUtils.getGateWayArpRTT(1000);
                if (ret != -1) {
                    arpRtt += ret;
                    arpCnt++;
                }
                i++;
                txPacketCounters = txPacketCounters2;
            }
            WifiNative.TxPacketCounters txPacketCounters3 = WifiInjector.getInstance().getWifiNative().getTxPacketCounters(wifiStateMachineUtils.getInterfaceName(this));
            if (txPacketCounters3 != null) {
                int dltTxGoodCnt = txPacketCounters3.txSucceeded - lastTxGoodCnt;
                int dltTxBadCnt = txPacketCounters3.txFailed - lastTxBadCnt;
                WifiNative.TxPacketCounters txPacketCounters4 = txPacketCounters3;
                long dltTxRetries = wifiStateMachineUtils.getWifiInfo(this).txRetries - lastTxRetries;
                StringBuilder sb = new StringBuilder();
                long j = ret;
                sb.append("pwr:dltTxGoodCnt:");
                sb.append(dltTxGoodCnt);
                sb.append(" dltTxBadCnt:");
                sb.append(dltTxBadCnt);
                sb.append(" dltTxRetries:");
                sb.append(dltTxRetries);
                sb.append(" arpRtt:");
                sb.append(arpRtt);
                sb.append(" arpCnt:");
                sb.append(arpCnt);
                sb.append(" enable:");
                sb.append(enable);
                logd(sb.toString());
                this.mHwWifiCHRService.txPwrBoostChrStatic(Boolean.valueOf(enable), (int) arpRtt, arpCnt, dltTxGoodCnt, dltTxBadCnt, (int) dltTxRetries);
                return;
            }
            long j2 = ret;
            boolean z = enable;
            return;
        }
        WifiNative.TxPacketCounters txPacketCounters5 = txPacketCounters;
    }

    public int isAllowedManualWifiPwrBoost() {
        return this.mIsAllowedManualPwrBoost;
    }

    public boolean isWifiConnectivityManagerEnabled() {
        return this.mWifiConnectivityManager != null && this.mWifiConnectivityManager.isWifiConnectivityManagerEnabled();
    }

    /* access modifiers changed from: private */
    public void clearPwrBoostChrStatus() {
        this.mCurrentPwrBoostStat = false;
        this.mIsFinishLinkDetect = false;
        this.mPwrBoostOncnt = 0;
        this.mPwrBoostOffcnt = 0;
    }

    private boolean isGlobalScanCtrl(ActivityManager.RunningAppProcessInfo appProcessInfo) {
        logd("isGlobalScanCtrl begin ");
        if (!isWifiScanWhitelisted(appProcessInfo)) {
            logd("wifi_scan return isInGlobalScanCtrl = " + this.isInGlobalScanCtrl);
            if (!this.isInGlobalScanCtrl || System.currentTimeMillis() - this.mLastScanTimestamp > TIMEOUT_CONTROL_SCAN_ASSOCIATED) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void updateGlobalScanTimes() {
        long now = System.currentTimeMillis();
        long scanInterval = now - this.mLastScanTimestamp;
        this.mLastScanTimestamp = now;
        if (scanInterval > 0) {
            logd("wifi_scan interval = " + scanInterval + " mFoulTimes = " + this.mFoulTimes + " mFreedTimes = " + this.mFreedTimes + " isInGlobalScanCtrl = " + this.isInGlobalScanCtrl);
            if (this.isInGlobalScanCtrl) {
                if (scanInterval > 10000) {
                    this.mFreedTimes++;
                } else {
                    this.mFreedTimes = 0;
                }
                if (this.mFreedTimes >= 5) {
                    this.mFoulTimes = 0;
                    this.mFreedTimes = 0;
                    this.isInGlobalScanCtrl = false;
                }
            } else {
                if (scanInterval < TIMEOUT_CONTROL_SCAN_ASSOCIATED) {
                    this.mFoulTimes++;
                } else {
                    this.mFoulTimes = 0;
                }
                if (this.mFoulTimes >= 5) {
                    this.mFoulTimes = 0;
                    this.mFreedTimes = 0;
                    this.isInGlobalScanCtrl = true;
                }
            }
        }
    }

    public boolean getChargingState() {
        String usb = HwArpUtils.readFileByChars(USB_SUPPLY);
        if (usb.length() == 0) {
            usb = HwArpUtils.readFileByChars(USB_SUPPLY_QCOM);
        }
        if ("1".equals(usb.trim())) {
            return true;
        }
        logd("getChargingState return false");
        return false;
    }

    /* access modifiers changed from: package-private */
    public void registHwSoftApManager(HwSoftApManager hwSoftApManager) {
        this.mHwSoftApManager = hwSoftApManager;
        log("HwSoftApManager registed");
    }

    /* access modifiers changed from: package-private */
    public void clearHwSoftApManager() {
        log("Clear HwSoftApManager");
        if (this.mHwSoftApManager != null) {
            this.mHwSoftApManager.clearCallbacksAndMessages();
        }
        this.mHwSoftApManager = null;
    }

    public List<String> getApLinkedStaList() {
        if (this.mHwSoftApManager != null) {
            return this.mHwSoftApManager.getApLinkedStaList();
        }
        Log.w(TAG, "getApLinkedStaList called when mHwSoftApManager is not registed");
        return Collections.emptyList();
    }

    public int[] getSoftApChannelListFor5G() {
        HwSoftApManager hwSoftApManager = this.mHwSoftApManager;
        return HwSoftApManager.getSoftApChannelListFor5G();
    }

    public void setSoftapDisassociateSta(String mac) {
        if (this.mHwSoftApManager != null) {
            this.mHwSoftApManager.setSoftApDisassociateSta(mac);
        } else {
            Log.w(TAG, "setSoftapDisassociateSta called when mHwSoftApManager is not registed");
        }
    }

    public void setSoftapMacFilter(String macFilter) {
        if (this.mHwSoftApManager != null) {
            this.mHwSoftApManager.setSoftapMacFilter(macFilter);
        } else {
            Log.w(TAG, "setSoftapMacFilter called when mHwSoftApManager is not registed");
        }
    }

    public boolean isAndroidMobileAP() {
        String ipAddress = "";
        WifiInfo wifiInfo = getWifiInfo();
        if (wifiInfo != null) {
            ipAddress = intIpToStringIp(wifiInfo.getIpAddress());
        }
        if (ipAddress == null || !ipAddress.startsWith("192.168.43.")) {
            return false;
        }
        return true;
    }

    private String intIpToStringIp(int ip) {
        return String.format("%d.%d.%d.%d", new Object[]{Integer.valueOf(ip & 255), Integer.valueOf((ip >> 8) & 255), Integer.valueOf((ip >> 16) & 255), Integer.valueOf((ip >> 24) & 255)});
    }

    public void startPacketKeepalive(Message msg) {
        KeepalivePacketData data = (KeepalivePacketData) msg.obj;
        if (data != null) {
            Log.e(TAG, "startPacketKeepalive msg.arg1 = " + msg.arg1 + " msg.arg2 =" + msg.arg2 + " srcPort = " + data.srcPort + " dstPort = " + data.dstPort);
        } else {
            Log.e(TAG, "startPacketKeepalive data == null");
        }
        sendMessage(131232, msg.arg1, msg.arg2, msg.obj);
    }

    public void stopPacketKeepalive(Message msg) {
        KeepalivePacketData data = (KeepalivePacketData) msg.obj;
        if (data != null) {
            Log.e(TAG, "stopPacketKeepalive msg.arg1 = " + msg.arg1 + " msg.arg2 =" + msg.arg2 + " srcPort = " + data.srcPort + " dstPort = " + data.dstPort);
        } else {
            Log.e(TAG, "stopPacketKeepalive data == null");
        }
        sendMessage(131233, msg.arg1, msg.arg2, msg.obj);
    }

    public int hwSyncAddOrUpdateNetwork(AsyncChannel channel, WifiConfiguration newConfig, String apkName) {
        Bundle data = new Bundle();
        WifiConfiguration currConfig = null;
        WifiConfigManager wifiConfigManager = WifiInjector.getInstance().getWifiConfigManager();
        if (wifiConfigManager != null) {
            currConfig = wifiConfigManager.getConfiguredNetwork(newConfig.networkId);
        }
        WifiConfiguration currConfig2 = currConfig;
        if (newConfig.networkId == -1 || currConfig2 == null) {
            this.mHwWifiCHRService.updateApkChangeWifiConfig(15, apkName, currConfig2, newConfig, data);
        } else {
            this.mHwWifiCHRService.updateApkChangeWifiConfig(19, apkName, currConfig2, newConfig, data);
        }
        int result = syncAddOrUpdateNetwork(channel, newConfig);
        if (result != -1) {
            this.mHwWifiCHRService.uploadDFTEvent(EID_WIFI_APK_CHANGE_WIFI_CONFIG_INFO, data);
        }
        return result;
    }

    public boolean hwSyncRemoveNetwork(AsyncChannel channel, int netId, String apkName) {
        Bundle data = new Bundle();
        WifiConfiguration currConfig = null;
        WifiConfigManager wifiConfigManager = WifiInjector.getInstance().getWifiConfigManager();
        if (wifiConfigManager != null) {
            currConfig = wifiConfigManager.getConfiguredNetwork(netId);
        }
        this.mHwWifiCHRService.updateApkChangeWifiConfig(17, apkName, currConfig, null, data);
        boolean result = syncRemoveNetwork(channel, netId);
        if (result) {
            this.mHwWifiCHRService.uploadDFTEvent(EID_WIFI_APK_CHANGE_WIFI_CONFIG_INFO, data);
        }
        return result;
    }

    public boolean hwSetApConfiguration(WifiConfiguration newConfig, String apkName) {
        WifiApConfigStore apConfigStore = WifiInjector.getInstance().getWifiApConfigStore();
        if (apConfigStore != null) {
            if (this.mHwWifiCHRService != null) {
                Bundle data = new Bundle();
                this.mHwWifiCHRService.updateApkChangeWifiConfig(14, apkName, apConfigStore.getApConfiguration(), newConfig, data);
                this.mHwWifiCHRService.uploadDFTEvent(EID_WIFI_APK_CHANGE_WIFI_CONFIG_INFO, data);
            }
            apConfigStore.setApConfiguration(newConfig);
        }
        return true;
    }

    public boolean hwSyncRemovePasspointConfig(AsyncChannel channel, String fqdn, String apkName) {
        boolean result = syncRemovePasspointConfig(channel, fqdn);
        if (this.mHwWifiCHRService != null && result) {
            Bundle data = new Bundle();
            data.putString("apk", apkName);
            data.putInt("action", 18);
            this.mHwWifiCHRService.uploadDFTEvent(EID_WIFI_APK_CHANGE_WIFI_CONFIG_INFO, data);
        }
        return result;
    }

    public boolean hwSyncAddOrUpdatePasspointConfig(AsyncChannel channel, PasspointConfiguration config, int uid, String apkName) {
        boolean result = syncAddOrUpdatePasspointConfig(channel, config, uid);
        if (this.mHwWifiCHRService != null && result) {
            Bundle data = new Bundle();
            data.putString("apk", apkName);
            data.putInt("action", 16);
            this.mHwWifiCHRService.uploadDFTEvent(EID_WIFI_APK_CHANGE_WIFI_CONFIG_INFO, data);
        }
        return result;
    }

    public SoftApChannelXmlParse getSoftApChannelXmlParse() {
        return this.mSoftApChannelXmlParse;
    }
}
