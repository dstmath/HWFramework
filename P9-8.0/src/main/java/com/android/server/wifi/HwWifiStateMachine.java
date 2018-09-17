package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.StaticIpConfiguration;
import android.net.ip.IpManager;
import android.net.ip.IpManager.ProvisioningConfiguration;
import android.net.wifi.HwInnerNetworkManagerImpl;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiSsid;
import android.net.wifi.wifipro.HwNetworkAgent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.LruCache;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.IState;
import com.android.server.wifi.ABS.HwABSDetectorService;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.WifiNative.SignalPollResult;
import com.android.server.wifi.WifiNative.TxPacketCounters;
import com.android.server.wifi.routermodelrecognition.HwRouterModelRecognition;
import com.android.server.wifi.util.StringUtil;
import com.android.server.wifi.util.WifiCommonUtils;
import com.android.server.wifi.wifipro.HwDualBandManager;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiProConfigStore;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifi.wifipro.WifiproUtils;
import com.android.server.wifi.wifipro.hwintelligencewifi.HwIntelligenceWiFiManager;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
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
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    private static final int DHCP_RESULT_CACHE_SIZE = 50;
    public static final int ENTERPRISE_HOTSPOT_THRESHOLD = 4;
    private static final String HIGEO_PACKAGE_NAME = "com.huawei.lbs";
    private static final int HIGEO_STATE_DEFAULT_MODE = 0;
    private static final int HIGEO_STATE_WIFI_SCAN_MODE = 1;
    private static final String HUAWEI_SETTINGS = "com.android.settings.Settings$WifiSettingsActivity";
    public static final int PM_LOWPWR = 7;
    public static final int PM_NORMAL = 6;
    public static final int SCAN_ONLY_CONNECT_MODE = 100;
    private static final long SCREENOFF_NO_LOWPWR_FIRMWARE_TIME_SPAN = 1800000;
    private static final String SCREENOFF_START_LOWPWR_FIRMWARE = "com.huawei.intent.action.SCREENOFF_START_LOWPWR_FIRMWARE";
    private static final String SOFTAP_IFACE = "wlan0";
    private static final int SUCCESS = 1;
    public static final String SUPPLICANT_WAPI_EVENT = "android.net.wifi.supplicant.WAPI_EVENT";
    private static final String TAG = "HwWifiStateMachine";
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
    private static WifiNativeUtils wifiNativeUtils = ((WifiNativeUtils) EasyInvokeFactory.getInvokeUtils(WifiNativeUtils.class));
    private static WifiStateMachineUtils wifiStateMachineUtils = ((WifiStateMachineUtils) EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class));
    private boolean isInGlobalScanCtrl = false;
    private long lastConnectTime = -1;
    private HashMap<String, String> lastDhcps = new HashMap();
    private long lastScanResultTimestamp = 0;
    private ActivityManager mActivityManager;
    private AlarmManager mAlarmManager;
    private int mBQEUid;
    private BroadcastReceiver mBcastReceiver = new BroadcastReceiver() {
        private static final /* synthetic */ int[] -android-net-NetworkInfo$DetailedStateSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$android$net$NetworkInfo$DetailedState;

        private static /* synthetic */ int[] -getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
            if (-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
                return -android-net-NetworkInfo$DetailedStateSwitchesValues;
            }
            int[] iArr = new int[DetailedState.values().length];
            try {
                iArr[DetailedState.AUTHENTICATING.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DetailedState.BLOCKED.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 5;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[DetailedState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[DetailedState.CONNECTING.ordinal()] = 6;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[DetailedState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[DetailedState.DISCONNECTING.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[DetailedState.FAILED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[DetailedState.IDLE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[DetailedState.SCANNING.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[DetailedState.SUSPENDED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            -android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
            return iArr;
        }

        public void onReceive(Context context, Intent intent) {
            int PluggedType;
            String action = intent.getAction();
            String chipName = SystemProperties.get("ro.connectivity.sub_chiptype", "");
            WifiInfo wifiInfo = HwWifiStateMachine.wifiStateMachineUtils.getWifiInfo(HwWifiStateMachine.this);
            boolean isMobileAP = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(HwWifiStateMachine.this.myContext);
            if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                PluggedType = intent.getIntExtra("plugged", 0);
                if (PluggedType == 2 || PluggedType == 5) {
                    HwWifiStateMachine.this.mIsScanCtrlPluggedin = true;
                } else if (!HwWifiStateMachine.this.getChargingState()) {
                    HwWifiStateMachine.this.mIsScanCtrlPluggedin = false;
                }
                HwWifiStateMachine.this.logd("mBcastReceiver: PluggedType = " + PluggedType + " mIsScanCtrlPluggedin = " + HwWifiStateMachine.this.mIsScanCtrlPluggedin);
            }
            if (HwWifiStateMachine.BRCM_CHIP_4359.equals(chipName)) {
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    PluggedType = intent.getIntExtra("plugged", 0);
                    if (PluggedType == 2 || PluggedType == 5) {
                        HwWifiStateMachine.this.mIsChargePluggedin = true;
                    } else {
                        HwWifiStateMachine.this.mIsChargePluggedin = false;
                        HwWifiStateMachine.this.mIsAllowedManualPwrBoost = 0;
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null) {
                        switch (AnonymousClass1.-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[networkInfo.getDetailedState().ordinal()]) {
                            case 1:
                                HwWifiStateMachine.this.logd("setpmlock:CONNECTED");
                                HwWifiStateMachine.this.mWifiConnectState = true;
                                if (wifiInfo != null) {
                                    HwWifiStateMachine.this.mSsid = wifiInfo.getSSID();
                                }
                                HwWifiStateMachine.this.setLowPwrMode(HwWifiStateMachine.this.mWifiConnectState, HwWifiStateMachine.this.mSsid, isMobileAP, HwWifiStateMachine.this.mScreenState);
                                break;
                            case 2:
                                HwWifiStateMachine.this.logd("setpmlock:DISCONNECTED");
                                HwWifiStateMachine.this.mWifiConnectState = false;
                                HwWifiStateMachine.this.setLowPwrMode(HwWifiStateMachine.this.mWifiConnectState, null, isMobileAP, HwWifiStateMachine.this.mScreenState);
                                break;
                        }
                    }
                    return;
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    HwWifiStateMachine.this.logd("setpmlock:action = " + action);
                    HwWifiStateMachine.this.mScreenState = true;
                    HwWifiStateMachine.this.stopScreenoffTrack();
                    HwWifiStateMachine.this.setLowPwrMode(HwWifiStateMachine.this.mWifiConnectState, HwWifiStateMachine.this.mSsid, isMobileAP, HwWifiStateMachine.this.mScreenState);
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    HwWifiStateMachine.this.logd("setpmlock:action = " + action);
                    HwWifiStateMachine.this.mScreenState = false;
                    HwWifiStateMachine.this.startScreenoffTrack();
                }
                if (HwWifiStateMachine.SCREENOFF_START_LOWPWR_FIRMWARE.equals(action)) {
                    Log.d(HwWifiStateMachine.TAG, "startUsingLowpwrFirmware " + action);
                    HwWifiStateMachine.this.setLowPwrMode(HwWifiStateMachine.this.mWifiConnectState, HwWifiStateMachine.this.mSsid, isMobileAP, true);
                }
            }
        }
    };
    private CodeReceiver mCodeReceiver;
    public boolean mCurrNetworkHistoryInserted = false;
    private int mCurrentConfigNetId = -1;
    private String mCurrentConfigurationKey = null;
    private boolean mCurrentPwrBoostStat = false;
    private Queue<IState> mDestStates = null;
    private final LruCache<String, DhcpResults> mDhcpResultCache = new LruCache(50);
    private int mFoulTimes = 0;
    private int mFreedTimes = 0;
    private HiLinkController mHiLinkController = null;
    private HwInnerNetworkManagerImpl mHwInnerNetworkManagerImpl;
    public int mIsAllowedManualPwrBoost = 0;
    private boolean mIsChargePluggedin = false;
    private boolean mIsFinishLinkDetect = false;
    private boolean mIsScanCtrlPluggedin = false;
    private boolean mIsSetWifiCountryCode = SystemProperties.getBoolean("ro.config.wifi_country_code", false);
    private long mLastScanTimestamp = 0;
    private int mLastTxPktCnt = 0;
    private HashMap<String, Integer> mPidBlackList = new HashMap();
    private long mPidBlackListInteval = 0;
    private HashMap<String, Integer> mPidConnectedBlackList = new HashMap();
    private HashMap<Integer, Long> mPidLastScanSuccTimestamp = new HashMap();
    private HashMap<Integer, Long> mPidLastScanTimestamp = new HashMap();
    private HashMap<Integer, Integer> mPidWifiScanCount = new HashMap();
    private int mPwrBoostOffcnt = 0;
    private int mPwrBoostOncnt = 0;
    private AtomicBoolean mRenewDhcpSelfCuring = new AtomicBoolean(false);
    private int mScreenOffScanToken = 0;
    private boolean mScreenState = true;
    private PendingIntent mScreenoffStartLowpwrFirmwareIntent;
    public WifiConfiguration mSelectedConfig = null;
    private DetailedState mSelfCureNetworkLastState = DetailedState.IDLE;
    private int mSelfCureWifiConnectRetry = 0;
    private int mSelfCureWifiLastState = 0;
    private String mSsid = null;
    private WifiSsid mWiFiProRoamingSSID = null;
    public boolean mWifiAlwaysOnBeforeCure = false;
    public boolean mWifiBackgroundConnected = false;
    private boolean mWifiConnectState = false;
    private WifiDetectConfInfo mWifiDetectConfInfo = new WifiDetectConfInfo();
    private int mWifiDetectperiod = -1;
    private long mWifiEnabledTimeStamp = 0;
    private WifiScanGenieController mWifiScanGenieController;
    private int mWifiSelfCureState = 0;
    private AtomicBoolean mWifiSelfCuring = new AtomicBoolean(false);
    private AtomicBoolean mWifiSoftSwitchRunning = new AtomicBoolean(false);
    public boolean mWifiSwitchOnGoing = false;
    public int mWpsCompletedNetId = -1;
    private HashMap<String, Boolean> mapApCapChr = new HashMap();
    private Context myContext;
    private final Object selectConfigLock = new Object();
    private boolean usingStaticIpConfig = false;
    private int wifiConnectedBackgroundReason = 0;

    private class CodeReceiver extends BroadcastReceiver {
        /* synthetic */ CodeReceiver(HwWifiStateMachine this$0, CodeReceiver -this1) {
            this();
        }

        private CodeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("com.android.net.wifi.countryCode".equals(intent.getAction())) {
                HwWifiStateMachine.this.log("com.android.net.wifi.countryCode is RECEIVER");
                String countryCode = Global.getString(HwWifiStateMachine.this.myContext.getContentResolver(), "wifi_country_code");
                HwWifiCHRStateManager hwWifiCHR = HwWifiServiceFactory.getHwWifiCHRStateManager();
                if (hwWifiCHR != null && (hwWifiCHR instanceof HwWifiCHRStateManagerImpl)) {
                    ((HwWifiCHRStateManagerImpl) hwWifiCHR).setCountryCode(countryCode);
                }
            }
        }
    }

    private class FilterScanRunnable implements Runnable {
        List<ScanDetail> lstScanRet = null;

        public FilterScanRunnable(List<ScanDetail> lstScan) {
            this.lstScanRet = lstScan;
        }

        /* JADX WARNING: Missing block: B:7:0x0016, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            HwWifiCHRStateManager hwmCHR = HwWifiCHRStateManagerImpl.getDefault();
            if (hwmCHR != null) {
                String strCurBssid = HwWifiStateMachine.this.getCurrentBSSID();
                if (strCurBssid != null && !strCurBssid.isEmpty() && !HwWifiStateMachine.this.mapApCapChr.containsKey(strCurBssid) && this.lstScanRet != null && this.lstScanRet.size() != 0) {
                    for (ScanDetail scanned : this.lstScanRet) {
                        if (scanned != null) {
                            String strBssid = scanned.getBSSIDString();
                            if (!(strBssid == null || strBssid.isEmpty() || !strCurBssid.equals(strBssid))) {
                                int stream1 = scanned.getNetworkDetail().getStream1();
                                int stream2 = scanned.getNetworkDetail().getStream2();
                                int stream3 = scanned.getNetworkDetail().getStream3();
                                int stream4 = scanned.getNetworkDetail().getStream4();
                                hwmCHR.updateWifiException(213, "{BSSID:\"" + strCurBssid + "\"," + HwWifiStateMachine.AP_CAP_KEY + HwQoEUtils.SEPARATOR + (((scanned.getNetworkDetail().getPrimaryFreq() / 1000) * 10) + Math.abs(((stream1 + stream2) + stream3) + stream4)) + "," + HwWifiStateMachine.TX_MCS_SET + HwQoEUtils.SEPARATOR + scanned.getNetworkDetail().getTxMcsSet() + "}");
                                HwWifiStateMachine.this.mapApCapChr.put(strCurBssid, Boolean.valueOf(true));
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
                            HwWifiStateMachine hwWifiStateMachine;
                            if (!HwWifiStateMachine.this.mCurrentPwrBoostStat) {
                                WifiInjector.getInstance().getWifiNative().setPwrBoost(1);
                                HwWifiStateMachine.this.mCurrentPwrBoostStat = true;
                                HwWifiStateMachine.this.linkMeasureAndStatic(HwWifiStateMachine.this.mCurrentPwrBoostStat);
                                hwWifiStateMachine = HwWifiStateMachine.this;
                                hwWifiStateMachine.mPwrBoostOncnt = hwWifiStateMachine.mPwrBoostOncnt + 1;
                            } else if (HwWifiStateMachine.this.mCurrentPwrBoostStat) {
                                WifiInjector.getInstance().getWifiNative().setPwrBoost(0);
                                HwWifiStateMachine.this.mCurrentPwrBoostStat = false;
                                HwWifiStateMachine.this.linkMeasureAndStatic(HwWifiStateMachine.this.mCurrentPwrBoostStat);
                                hwWifiStateMachine = HwWifiStateMachine.this;
                                hwWifiStateMachine.mPwrBoostOffcnt = hwWifiStateMachine.mPwrBoostOffcnt + 1;
                            }
                        }
                        if (HwWifiStateMachine.this.mPwrBoostOncnt >= 3 && HwWifiStateMachine.this.mPwrBoostOffcnt >= 3) {
                            HwWifiStateMachine.this.mIsFinishLinkDetect = true;
                            break;
                        } else {
                            HwWifiStateMachine.this.mIsFinishLinkDetect = false;
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

    public HwWifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode, WifiNative wifiNative) {
        super(context, facade, looper, userManager, wifiInjector, backupManagerProxy, countryCode, wifiNative);
        HwWifiCHRStateManager hwWifiCHR = HwWifiServiceFactory.getHwWifiCHRStateManager();
        if (hwWifiCHR != null && (hwWifiCHR instanceof HwWifiCHRStateManagerImpl)) {
            ((HwWifiCHRStateManagerImpl) hwWifiCHR).setWifiStateMachine(this);
        }
        this.myContext = context;
        this.mBQEUid = 1000;
        this.mHwInnerNetworkManagerImpl = (HwInnerNetworkManagerImpl) HwFrameworkFactory.getHwInnerNetworkManager();
        registerReceiverInWifiPro(context);
        registerForWifiEvaluateChanges();
        if (WifiRadioPowerController.isRadioPowerEnabled()) {
            WifiRadioPowerController.setInstance(context, this, wifiStateMachineUtils.getWifiNative(this), (HwInnerNetworkManagerImpl) HwFrameworkFactory.getHwInnerNetworkManager());
        }
        if (this.mIsSetWifiCountryCode) {
            this.mCodeReceiver = new CodeReceiver(this, null);
            IntentFilter myfilter = new IntentFilter();
            myfilter.addAction("com.android.net.wifi.countryCode");
            this.myContext.registerReceiver(this.mCodeReceiver, myfilter);
        }
        if (context.getResources().getBoolean(17957059)) {
            registerForPasspointChanges();
        }
        HwCHRExceptionListener.getInstance(context).startChrWifiListener();
        this.mHiLinkController = new HiLinkController(context, this);
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        if (!HwRouterModelRecognition.startInstance(context) && HWFLOW) {
            Log.d(TAG, "HwRouterModelRecognition.startInstance failed");
        }
        this.mDestStates = new LinkedList();
        this.mAlarmManager = (AlarmManager) this.myContext.getSystemService("alarm");
        this.mScreenoffStartLowpwrFirmwareIntent = PendingIntent.getBroadcast(this.myContext, 0, new Intent(SCREENOFF_START_LOWPWR_FIRMWARE, null), 0);
        pwrBoostRegisterBcastReceiver();
    }

    public String getWpaSuppConfig() {
        log("WiFIStateMachine  getWpaSuppConfig InterfaceName ");
        if (this.myContext.checkCallingPermission("com.huawei.permission.ACCESS_AP_INFORMATION") == 0) {
            return WifiInjector.getInstance().getWifiNative().getWpaSuppConfig();
        }
        log("getWpaSuppConfig(): permissin deny");
        return null;
    }

    protected void enableAllNetworksByMode() {
        log("enableAllNetworks mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        if (wifiStateMachineUtils.getOperationalMode(this) != 100) {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
        }
    }

    protected void handleNetworkDisconnect() {
        log("handle network disconnect mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            HwDisableLastNetwork();
        }
        log("handleNetworkDisconnect,resetWifiProManualConnect");
        resetWifiProManualConnect();
        super.handleNetworkDisconnect();
    }

    protected void loadAndEnableAllNetworksByMode() {
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
        if (getCurrentState() != wifiStateMachineUtils.getSupplicantStoppingState(this)) {
            int lastNetworkId = wifiStateMachineUtils.getLastNetworkId(this);
        }
    }

    protected boolean processSupplicantStartingSetMode(Message message) {
        if (131144 != message.what) {
            return false;
        }
        wifiStateMachineUtils.setOperationalMode(this, message.arg1);
        log("set operation mode mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        return true;
    }

    protected boolean processScanModeSetMode(Message message, int mLastOperationMode) {
        if (message.arg1 != 100) {
            return false;
        }
        log("SCAN_ONLY_CONNECT_MODE, do not enable all networks here.");
        if (mLastOperationMode == 3) {
            wifiStateMachineUtils.setWifiState(this, 3);
            WifiConfigStoreUtils.loadConfiguredNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            wifiStateMachineUtils.getWifiP2pChannel(this).sendMessage(131203);
        }
        wifiStateMachineUtils.setOperationalMode(this, 100);
        transitionTo(wifiStateMachineUtils.getDisconnectedState(this));
        return true;
    }

    protected boolean processConnectModeSetMode(Message message) {
        if (wifiStateMachineUtils.getOperationalMode(this) != 100 || message.arg2 != 0) {
            return false;
        }
        log("CMD_ENABLE_NETWORK command is ignored.");
        wifiStateMachineUtils.replyToMessage((WifiStateMachine) this, message, message.what, 1);
        return true;
    }

    protected boolean processL2ConnectedSetMode(Message message) {
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

    protected boolean processDisconnectedSetMode(Message message) {
        wifiStateMachineUtils.setOperationalMode(this, message.arg1);
        log("set operation mode mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            WifiConfigStoreUtils.disableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            return true;
        } else if (wifiStateMachineUtils.getOperationalMode(this) != 1) {
            return false;
        } else {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            wifiStateMachineUtils.getWifiNative(this).reconnect();
            return true;
        }
    }

    protected void enterConnectedStateByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) == 100) {
            log("wifi connected. disable other networks.");
            disableAllNetworksExceptLastConnected();
        }
    }

    protected boolean enterDriverStartedStateByMode() {
        this.mWifiScanGenieController = WifiScanGenieController.createWifiScanGenieControllerImpl(this.myContext);
        if (wifiStateMachineUtils.getOperationalMode(this) != 100) {
            return false;
        }
        log("SCAN_ONLY_CONNECT_MODE, disable all networks.");
        wifiStateMachineUtils.getWifiNative(this).disconnect();
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

    protected boolean processConnectModeAutoConnectByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) != 100) {
            return false;
        }
        log("CMD_AUTO_CONNECT command is ignored..");
        return true;
    }

    protected void recordAssociationRejectStatusCode(int statusCode) {
        System.putInt(this.myContext.getContentResolver(), ASSOCIATION_REJECT_STATUS_CODE, statusCode);
    }

    protected void startScreenOffScan() {
        int configNetworksSize = wifiStateMachineUtils.getWifiConfigManager(this).getSavedNetworks().size();
        if (!wifiStateMachineUtils.getScreenOn(this) && configNetworksSize > 0) {
            logd("begin scan when screen off");
            startScan(wifiStateMachineUtils.getUnKnownScanSource(this), -1, null, null);
            int i = this.mScreenOffScanToken + 1;
            this.mScreenOffScanToken = i;
            sendMessageDelayed(obtainMessage(CMD_SCREEN_OFF_SCAN, i, 0), wifiStateMachineUtils.getSupplicantScanIntervalMs(this));
        }
    }

    protected boolean processScreenOffScan(Message message) {
        if (CMD_SCREEN_OFF_SCAN != message.what) {
            return false;
        }
        if (message.arg1 == this.mScreenOffScanToken) {
            startScreenOffScan();
        }
        return true;
    }

    protected void makeHwDefaultIPTable(DhcpResults dhcpResults) {
        synchronized (this.mDhcpResultCache) {
            String key = wifiStateMachineUtils.getWifiInfo(this).getBSSID();
            if (key == null) {
                Log.w(TAG, "makeHwDefaultIPTable key is null!");
                return;
            }
            if (((DhcpResults) this.mDhcpResultCache.get(key)) != null) {
                log("make default IP configuration map, remove old rec.");
                this.mDhcpResultCache.remove(key);
            }
            boolean isPublicESS = false;
            int count = 0;
            String ssid = "";
            String capabilities = "";
            List<ScanResult> scanList = syncGetScanResultsList();
            try {
                for (ScanResult result : scanList) {
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

    protected boolean handleHwDefaultIPConfiguration() {
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
        DhcpResults dhcpResult = (DhcpResults) this.mDhcpResultCache.get(key);
        if (dhcpResult == null) {
            log("set default IP configuration failed for no rec found");
            return false;
        }
        DhcpResults dhcpResults = new DhcpResults(dhcpResult);
        InterfaceConfiguration ifcg = new InterfaceConfiguration();
        try {
            ifcg.setLinkAddress(dhcpResults.ipAddress);
            ifcg.setInterfaceUp();
            wifiStateMachineUtils.getNwService(this).setInterfaceConfig(wifiStateMachineUtils.getInterfaceName(this), ifcg);
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
            return (DhcpResults) this.mDhcpResultCache.get(key);
        }
        Log.w(TAG, "getCachedDhcpResultsForCurrentConfig key is null!");
        return null;
    }

    protected boolean hasMeteredHintForWi(Inet4Address ip) {
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
        return !isIphone ? isWindowsPhone : true;
    }

    public int[] syncGetApChannelListFor5G(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_CHANNEL_LIST_5G);
        int[] iArr = null;
        if (resultMsg.obj != null) {
            iArr = resultMsg.obj;
        }
        resultMsg.recycle();
        return iArr;
    }

    public void setLocalMacAddressFromMacfile() {
        wifiStateMachineUtils.getWifiInfo(this).setMacAddress(getMacAddressFromFile());
    }

    public void setVoWifiDetectMode(WifiDetectConfInfo info) {
        if (info != null && (this.mWifiDetectConfInfo.isEqual(info) ^ 1) != 0) {
            this.mWifiDetectConfInfo = info;
            sendMessage(131772, info);
        }
    }

    protected void processSetVoWifiDetectMode(Message msg) {
        WifiDetectConfInfo info = msg.obj;
        Log.d(TAG, "set VoWifi Detect Mode " + info);
        boolean ret = false;
        if (info != null) {
            if (info.mWifiDetectMode == 1) {
                ret = WifiInjector.getInstance().getWifiNative().voWifiDetectSet("LOW_THRESHOLD " + info.mThreshold);
            } else if (info.mWifiDetectMode == 2) {
                ret = WifiInjector.getInstance().getWifiNative().voWifiDetectSet("HIGH_THRESHOLD " + info.mThreshold);
            } else {
                ret = WifiInjector.getInstance().getWifiNative().voWifiDetectSet("MODE " + info.mWifiDetectMode);
            }
            if (ret && WifiInjector.getInstance().getWifiNative().voWifiDetectSet("TRIGGER_COUNT " + info.mEnvalueCount)) {
                ret = WifiInjector.getInstance().getWifiNative().voWifiDetectSet("MODE " + info.mWifiDetectMode);
            }
        }
        if (ret) {
            Log.d(TAG, "done set  VoWifi Detect Mode " + info);
        } else {
            Log.d(TAG, "Failed to set VoWifi Detect Mode " + info);
        }
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

    protected void processSetVoWifiDetectPeriod(Message msg) {
        int period = msg.arg1;
        Log.d(TAG, "set VoWifiDetect Period " + period);
        if (WifiInjector.getInstance().getWifiNative().voWifiDetectSet("PERIOD " + period)) {
            Log.d(TAG, "done set set VoWifiDetect  Period" + period);
        } else {
            Log.d(TAG, "set VoWifiDetect Period" + period);
        }
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

    protected void processIsSupportVoWifiDetect(Message msg) {
        wifiStateMachineUtils.replyToMessage((WifiStateMachine) this, msg, msg.what, WifiInjector.getInstance().getWifiNative().isSupportVoWifiDetect() ? 0 : -1);
    }

    protected void processStatistics(int event) {
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
        ByteBuffer rawByteBuffer = ByteBuffer.allocate(52);
        rawByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        SignalPollResult signalInfo = WifiInjector.getInstance().getWifiNative().signalPoll();
        int rssi = signalInfo.currentRssi;
        int linkSpeed = signalInfo.txBitrate;
        int frequency = signalInfo.associationFrequency;
        RssiPacketCountInfo info = new RssiPacketCountInfo();
        TxPacketCounters txPacketCounters = WifiInjector.getInstance().getWifiNative().getTxPacketCounters();
        info.txgood = txPacketCounters.txSucceeded;
        long nativeTxGood = (long) txPacketCounters.txSucceeded;
        info.txbad = txPacketCounters.txFailed;
        long nativeTxBad = (long) txPacketCounters.txFailed;
        rawByteBuffer.putInt(rssi);
        rawByteBuffer.putInt(0);
        rawByteBuffer.putInt((int) ((((double) info.txbad) / ((double) (info.txgood + info.txbad))) * 100.0d));
        int dpktcnt = info.txgood - this.mLastTxPktCnt;
        this.mLastTxPktCnt = info.txgood;
        rawByteBuffer.putInt(dpktcnt);
        rawByteBuffer.putInt(convertToAccessType(linkSpeed, frequency));
        rawByteBuffer.putInt(0);
        rawByteBuffer.putLong(nativeTxGood);
        rawByteBuffer.putLong(nativeTxBad);
        String macStr = wifiStateMachineUtils.getWifiInfo(this).getBSSID().replace(HwQoEUtils.SEPARATOR, "");
        byte[] macBytes = new byte[16];
        try {
            macBytes = macStr.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        rawByteBuffer.put(macBytes);
        Log.d(TAG, "rssi=" + rssi + ",nativeTxBad=" + nativeTxBad + ", nativeTxGood=" + nativeTxGood + ", dpktcnt=" + dpktcnt + ", linkSpeed=" + linkSpeed + ", frequency=" + frequency + ", noise=" + 0 + ", mac=" + macStr);
        return rawByteBuffer.array();
    }

    private static int convertToAccessType(int linkSpeed, int frequency) {
        return 0;
    }

    private String getMacAddressFromFile() {
        IOException e;
        Throwable th;
        String wifiMacAddr = "02:00:00:00:00:00";
        InputStream in = null;
        InputStream dis = null;
        File file = new File(Environment.getDataDirectory(), "misc/wifi/macwifi");
        if (file.exists()) {
            try {
                InputStream dis2;
                InputStream in2 = new FileInputStream(file);
                try {
                    dis2 = new DataInputStream(in2);
                } catch (IOException e2) {
                    e = e2;
                    in = in2;
                    try {
                        e.printStackTrace();
                        closeInputStream(dis);
                        closeInputStream(in);
                        return wifiMacAddr;
                    } catch (Throwable th2) {
                        th = th2;
                        closeInputStream(dis);
                        closeInputStream(in);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    closeInputStream(dis);
                    closeInputStream(in);
                    throw th;
                }
                try {
                    byte[] wifiBuf = new byte[17];
                    if (dis2.read(wifiBuf, 0, 17) != 17) {
                        loge("macwifi address has error in file");
                    } else {
                        wifiMacAddr = new String(wifiBuf, "utf-8");
                    }
                    closeInputStream(dis2);
                    closeInputStream(in2);
                    in = in2;
                } catch (IOException e3) {
                    e = e3;
                    dis = dis2;
                    in = in2;
                    e.printStackTrace();
                    closeInputStream(dis);
                    closeInputStream(in);
                    return wifiMacAddr;
                } catch (Throwable th4) {
                    th = th4;
                    dis = dis2;
                    in = in2;
                    closeInputStream(dis);
                    closeInputStream(in);
                    throw th;
                }
            } catch (IOException e4) {
                e = e4;
                e.printStackTrace();
                closeInputStream(dis);
                closeInputStream(in);
                return wifiMacAddr;
            }
        }
        loge("macwifi is not existed");
        return wifiMacAddr;
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
                    HwWifiStateMachine.this.log("sync update network history, internetHistory = " + newConfig.internetHistory);
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
                HwWifiStateMachine.this.log("ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER, switchType = " + switchType);
                if (!HwWifiStateMachine.this.mWifiSwitchOnGoing && changeConfig != null) {
                    if (switchType == 1) {
                        HwWifiStateMachine.this.requestWifiSoftSwitch();
                        HwWifiStateMachine.this.startConnectToUserSelectNetwork(changeConfig.networkId, null);
                    } else {
                        HwWifiStateMachine.this.startRoamToNetwork(changeConfig.networkId, null);
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
        return WifiHandover.isWifiProEnabled();
    }

    public int resetScoreByInetAccess(int score) {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        if (isWifiProEnabled() && networkInfo != null && networkInfo.getDetailedState() == DetailedState.VERIFYING_POOR_LINK) {
            return 0;
        }
        return score;
    }

    public void getConfiguredNetworks(Message message) {
        wifiStateMachineUtils.replyToMessage((WifiStateMachine) this, message, message.what, wifiStateMachineUtils.getWifiConfigManager(this).getSavedNetworks());
    }

    public void saveConnectingNetwork(WifiConfiguration config) {
        synchronized (this.selectConfigLock) {
            this.mSelectedConfig = config;
        }
    }

    public void reportPortalNetworkStatus() {
        unwantedNetwork(3);
    }

    public boolean ignoreEnterConnectedState() {
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        if (!isWifiProEnabled() || networkInfo == null || networkInfo.getDetailedState() != DetailedState.VERIFYING_POOR_LINK) {
            return false;
        }
        log("L2ConnectedState, case CMD_IP_CONFIGURATION_SUCCESSFUL, ignore to enter CONNECTED State");
        return true;
    }

    public void wifiNetworkExplicitlyUnselected() {
        if (isWifiProEnabled()) {
            WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
            HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
            if (wifiInfo != null) {
                wifiInfo.score = 0;
            }
            if (networkAgent != null) {
                networkAgent.explicitlyUnselected();
                networkAgent.sendNetworkScore(0);
            }
        }
    }

    public void wifiNetworkExplicitlySelected() {
        if (isWifiProEnabled()) {
            WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
            HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
            if (wifiInfo != null) {
                wifiInfo.score = 100;
            }
            if (networkAgent != null) {
                networkAgent.explicitlySelected(true);
                networkAgent.sendNetworkScore(100);
            }
        }
    }

    public void handleConnectedInWifiPro() {
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
        handleWiFiConnectedByScanGenie(wifiConfigManager);
        if (this.mWifiSwitchOnGoing) {
            WifiConfiguration config;
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
                bssid = config.BSSID;
                ssid = config.SSID;
                configKey = config.configKey();
            }
            sendWifiHandoverCompletedBroadcast(0, bssid, ssid, configKey);
        }
        setEnableAutoJoinWhenAssociated(false);
        int lastNetworkId = wifiStateMachineUtils.getLastNetworkId(this);
        if (isWifiProEnabled()) {
            WifiConfiguration connectedConfig = wifiConfigManager.getConfiguredNetwork(lastNetworkId);
            if (connectedConfig != null && connectedConfig.portalCheckStatus == 1) {
                connectedConfig.portalCheckStatus = 0;
                wifiConfigManager.updateInternetInfoByWifiPro(connectedConfig);
            }
            if (!(connectedConfig == null || !isWifiProEvaluatingAP() || this.usingStaticIpConfig || connectedConfig.SSID == null || (connectedConfig.SSID.equals("<unknown ssid>") ^ 1) == 0)) {
                String strDhcpResults = WifiProCommonUtils.dhcpResults2String(wifiStateMachineUtils.getDhcpResults(this), WifiStateMachineUtils.getCurrentCellId(this.myContext));
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
        this.mWpsCompletedNetId = -1;
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
        this.mWifiScanGenieController.handleWiFiDisconnected();
        this.mCurrNetworkHistoryInserted = false;
        if (this.wifiConnectedBackgroundReason == 2 || this.wifiConnectedBackgroundReason == 3) {
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, false);
        }
        this.wifiConnectedBackgroundReason = 0;
        setEnableAutoJoinWhenAssociated(true);
        WifiNative wifiNative = wifiStateMachineUtils.getWifiNative(this);
        synchronized (this.selectConfigLock) {
            if (WifiProCommonUtils.isWifiProSwitchOn(this.myContext) && wifiNative != null) {
                for (WifiConfiguration config : wifiStateMachineUtils.getWifiConfigManager(this).getSavedNetworks()) {
                    if (((config.noInternetAccess && (WifiProCommonUtils.allowWifiConfigRecovery(config.internetHistory) ^ 1) != 0) || WifiProCommonUtils.isOpenAndPortal(config) || WifiProCommonUtils.isOpenAndMaybePortal(config)) && ((this.mSelectedConfig == null || this.mSelectedConfig.networkId != config.networkId) && (this.mWpsCompletedNetId == -1 || this.mWpsCompletedNetId != config.networkId))) {
                        log("DisconnectedState, disable network in supplicant because of no internet, netid = " + config.networkId + ", ssid = " + config.SSID);
                    }
                }
            }
            this.mSelectedConfig = null;
        }
        this.usingStaticIpConfig = false;
        this.mRenewDhcpSelfCuring.set(false);
        this.mWpsCompletedNetId = -1;
    }

    public void handleUnwantedNetworkInWifiPro(WifiConfiguration config, int unwantedType) {
        if (config != null) {
            boolean updated = false;
            if (unwantedType == wifiStateMachineUtils.getUnwantedValidationFailed(this)) {
                boolean z;
                config.noInternetAccess = true;
                config.validatedInternetAccess = false;
                if (WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102)) {
                    z = true;
                } else {
                    z = false;
                }
                config.portalNetwork = z;
                if (this.mCurrNetworkHistoryInserted) {
                    config.internetHistory = WifiProCommonUtils.updateWifiConfigHistory(config.internetHistory, 0);
                } else {
                    config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 0);
                    this.mCurrNetworkHistoryInserted = true;
                }
                updated = true;
            } else if (unwantedType == 3) {
                config.portalNetwork = true;
                config.noInternetAccess = false;
                if (this.mCurrNetworkHistoryInserted) {
                    config.internetHistory = WifiProCommonUtils.updateWifiConfigHistory(config.internetHistory, 2);
                } else {
                    config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 2);
                    this.mCurrNetworkHistoryInserted = true;
                }
                updated = true;
            }
            if (updated) {
                WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                wifiConfigManager.updateInternetInfoByWifiPro(config);
                wifiConfigManager.saveToStore(true);
            }
        }
    }

    public void handleValidNetworkInWifiPro(WifiConfiguration config) {
        if (config != null) {
            config.noInternetAccess = false;
            config.portalNetwork = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102);
            if (this.mCurrNetworkHistoryInserted) {
                config.internetHistory = WifiProCommonUtils.updateWifiConfigHistory(config.internetHistory, 1);
            } else {
                config.internetHistory = WifiProCommonUtils.insertWifiConfigHistory(config.internetHistory, 1);
                this.mCurrNetworkHistoryInserted = true;
            }
            if (config.portalCheckStatus == 2 || config.portalCheckStatus == 1) {
                config.portalCheckStatus = 0;
            }
            String strDhcpResults = WifiProCommonUtils.dhcpResults2String(wifiStateMachineUtils.getDhcpResults(this), -1);
            if (strDhcpResults != null) {
                config.lastDhcpResults = strDhcpResults;
                if (!isWifiProEvaluatingAP()) {
                    HwSelfCureEngine.getInstance(this.myContext, this).notifyDhcpResultsInternetOk(strDhcpResults);
                }
            }
            config.lastHasInternetTimestamp = System.currentTimeMillis();
            wifiStateMachineUtils.getWifiConfigManager(this).updateInternetInfoByWifiPro(config);
        }
    }

    public void saveWpsNetIdInWifiPro(int netId) {
        this.mWpsCompletedNetId = netId;
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
            WifiConfiguration config = message.obj;
            boolean uiOnly = message.arg1 == 1;
            if (config != null && config.networkId != -1) {
                WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                wifiConfigManager.updateWifiConfigByWifiPro(config, uiOnly);
                if (config.configKey() != null && config.wifiProNoInternetAccess) {
                    log("updateWifiproWifiConfiguration, noInternetReason = " + config.wifiProNoInternetReason + ", ssid = " + config.SSID);
                    this.lastDhcps.remove(config.configKey());
                }
                wifiConfigManager.saveToStore(true);
            }
        }
    }

    public void updateNetworkConnFailedInfo(int netId, int rssi, int reason) {
        if (netId == -1) {
            return;
        }
        if (reason == 3 || reason == 2 || reason == 4) {
            log("updateNetworkConnFailedInfo, netId = " + netId + ", rssi = " + rssi + ", reason = " + reason);
            WifiConfigManager configManager = wifiStateMachineUtils.getWifiConfigManager(this);
            if (reason == 4) {
                configManager.updateNetworkConnFailedInfo(netId, rssi, reason);
                return;
            }
            WifiConfiguration selectedConfig = configManager.getConfiguredNetwork(netId);
            if (selectedConfig != null) {
                ScanResult scanResult = selectedConfig.getNetworkSelectionStatus().getCandidate();
                if (scanResult != null) {
                    rssi = scanResult.level;
                }
            }
            configManager.updateNetworkConnFailedInfo(netId, rssi, reason);
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

    /* JADX WARNING: Missing block: B:27:0x00d8, code:
            return r3;
     */
    /* JADX WARNING: Missing block: B:36:0x0100, code:
            return r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isWifiProEvaluatingAP() {
        boolean z = false;
        String str = null;
        if (isWifiProEnabled()) {
            WifiConfiguration connectedConfig = wifiStateMachineUtils.getWifiConfigManager(this).getConfiguredNetwork(wifiStateMachineUtils.getLastNetworkId(this));
            StringBuilder append = new StringBuilder().append("isWifiProEvaluatingAP, connectedConfig = ");
            if (connectedConfig != null) {
                str = connectedConfig.SSID;
            }
            log(append.append(str).toString());
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
                        boolean z2 = WifiProStateMachine.isWifiEvaluating() ? this.mSelectedConfig.isTempCreated : false;
                    } else {
                        log("==connectedConfig&mSelectedConfig are null, backgroundReason = " + this.wifiConnectedBackgroundReason);
                        if (WifiProStateMachine.isWifiEvaluating() || this.wifiConnectedBackgroundReason >= 1) {
                            z = true;
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
                    this.mSelectedConfig.lastDhcpResults = (String) this.lastDhcps.get(this.mSelectedConfig.configKey());
                    log("tryUseStaticIpForFastConnecting, lastDhcpResults = " + this.mSelectedConfig.lastDhcpResults);
                    if (this.mSelectedConfig.lastDhcpResults != null && this.mSelectedConfig.lastDhcpResults.length() > 0 && this.mSelectedConfig.getStaticIpConfiguration() == null && wifiConfigManager.tryUseStaticIpForFastConnecting(lastNid)) {
                        this.usingStaticIpConfig = true;
                    }
                }
            }
        }
    }

    public void updateNetworkConcurrently() {
        DetailedState state = DetailedState.CONNECTED;
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
        int lastNetworkId = wifiStateMachineUtils.getLastNetworkId(this);
        HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
        if (!(networkInfo.getExtraInfo() == null || wifiInfo.getSSID() == null || (wifiInfo.getSSID().equals("<unknown ssid>") ^ 1) == 0)) {
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

    public void notifyWifiConnectedBackgroundReady() {
        Intent intent;
        if (this.wifiConnectedBackgroundReason == 1) {
            log("notifyWifiConnectedBackgroundReady, ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY sent");
            intent = new Intent(WifiproUtils.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY);
            intent.setFlags(67108864);
            this.myContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } else if (this.wifiConnectedBackgroundReason == 2) {
            log("notifyWifiConnectedBackgroundReady, WIFI_BACKGROUND_PORTAL_CHECKING sent");
            intent = new Intent(WifiproUtils.ACTION_NOTIFY_PORTAL_CONNECTED_BACKGROUND);
            intent.setFlags(67108864);
            this.myContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } else if (this.wifiConnectedBackgroundReason == 3) {
            log("notifyWifiConnectedBackgroundReady, ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND sent");
            intent = new Intent(WifiproUtils.ACTION_NOTIFY_NO_INTERNET_CONNECTED_BACKGROUND);
            intent.setFlags(67108864);
            this.myContext.sendBroadcastAsUser(intent, UserHandle.ALL);
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
                    connectedConfig = this.mSelectedConfig;
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
        if ((!isWifiProEvaluatingAP() || (networkInfo.getDetailedState() != DetailedState.CONNECTING && networkInfo.getDetailedState() != DetailedState.SCANNING && networkInfo.getDetailedState() != DetailedState.AUTHENTICATING && networkInfo.getDetailedState() != DetailedState.OBTAINING_IPADDR && networkInfo.getDetailedState() != DetailedState.CONNECTED)) && !selfCureIgnoreNetworkStateChange(networkInfo) && !softSwitchIgnoreNetworkStateChanged(networkInfo)) {
            return false;
        }
        Log.d("WiFi_PRO", "ignoreNetworkStateChange, DetailedState = " + networkInfo.getDetailedState());
        return true;
    }

    public boolean selfCureIgnoreNetworkStateChange(NetworkInfo networkInfo) {
        if ((!isWifiSelfCuring() || !this.mWifiBackgroundConnected) && ((!isWifiSelfCuring() || (this.mWifiBackgroundConnected ^ 1) == 0 || networkInfo.getDetailedState() == DetailedState.CONNECTED) && (!isRenewDhcpSelfCuring() || networkInfo.getDetailedState() == DetailedState.DISCONNECTED))) {
            return false;
        }
        Log.d("HwSelfCureEngine", "selfCureIgnoreNetworkStateChange, detailedState = " + networkInfo.getDetailedState());
        return true;
    }

    private boolean selfCureIgnoreSuppStateChange(SupplicantState state) {
        if (isWifiSelfCuring() || isRenewDhcpSelfCuring() || this.mWifiSoftSwitchRunning.get()) {
            return true;
        }
        return false;
    }

    public boolean softSwitchIgnoreNetworkStateChanged(NetworkInfo networkInfo) {
        if (!this.mWifiSoftSwitchRunning.get() || networkInfo.getDetailedState() == DetailedState.CONNECTED) {
            return false;
        }
        Log.d("WIFIPRO", "softSwitchIgnoreNetworkStateChanged, detailedState = " + networkInfo.getDetailedState());
        if (networkInfo.getDetailedState() == DetailedState.DISCONNECTED) {
            HwSelfCureEngine.getInstance(this.myContext, this).notifyWifiDisconnected();
            HwWifiConnectivityMonitor.getInstance().notifyWifiDisconnected();
        }
        return true;
    }

    public boolean ignoreSupplicantStateChange(SupplicantState state) {
        if ((!isWifiProEvaluatingAP() || (state != SupplicantState.SCANNING && state != SupplicantState.ASSOCIATING && state != SupplicantState.AUTHENTICATING && state != SupplicantState.ASSOCIATED && state != SupplicantState.FOUR_WAY_HANDSHAKE && state != SupplicantState.AUTHENTICATING && state != SupplicantState.GROUP_HANDSHAKE && state != SupplicantState.COMPLETED)) && !selfCureIgnoreSuppStateChange(state)) {
            return false;
        }
        Log.d("WiFi_PRO", "ignoreSupplicantStateChange, state = " + state);
        return true;
    }

    private void resetWifiProManualConnect() {
        System.putInt(this.myContext.getContentResolver(), "wifipro_manual_connect_ap", 0);
    }

    private int getAppUid(String processName) {
        try {
            ApplicationInfo ai = this.myContext.getPackageManager().getApplicationInfo(processName, 1);
            if (ai != null) {
                return ai.uid;
            }
            return 1000;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return 1000;
        }
    }

    private void registerForWifiEvaluateChanges() {
        this.myContext.getContentResolver().registerContentObserver(Secure.getUriFor(WIFI_EVALUATE_TAG), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                int tag = Secure.getInt(HwWifiStateMachine.this.myContext.getContentResolver(), HwWifiStateMachine.WIFI_EVALUATE_TAG, 0);
                if (HwWifiStateMachine.this.mBQEUid == 1000) {
                    HwWifiStateMachine.this.mBQEUid = HwWifiStateMachine.this.getAppUid("com.huawei.wifiprobqeservice");
                }
                HwWifiStateMachine.this.logd("**wifipro tag is chenge, setWifiproFirewallEnable**,tag =" + tag);
                if (tag == 1) {
                    try {
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallEnable(true);
                        if (HwWifiStateMachine.this.mBQEUid != 1000) {
                            HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallWhitelist(HwWifiStateMachine.this.mBQEUid);
                        }
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallWhitelist(1000);
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallDrop();
                    } catch (Exception e) {
                        HwWifiStateMachine.this.loge("**setWifiproCmdEnable,Error Exception :" + e);
                    }
                } else if (tag == 0) {
                    try {
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallEnable(false);
                    } catch (Exception e1) {
                        HwWifiStateMachine.this.loge("**Disable WifiproCmdEnable***Error Exception " + e1);
                    }
                }
            }
        });
    }

    private void registerForPasspointChanges() {
        this.myContext.getContentResolver().registerContentObserver(Global.getUriFor(DBKEY_HOTSPOT20_VALUE), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (Global.getInt(HwWifiStateMachine.this.myContext.getContentResolver(), HwWifiStateMachine.DBKEY_HOTSPOT20_VALUE, 1) == 0) {
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
        if (!(currentWifiConfig == null || (currentWifiConfig.isTempCreated ^ 1) == 0)) {
            Log.d(TAG, "mWifiScanGenieController.handleWiFiConnected");
            this.mWifiScanGenieController.handleWiFiConnected(currentWifiConfig, false);
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
        }
    }

    public boolean isWlanSettingsActivity() {
        List<RunningTaskInfo> runningTaskInfos = this.mActivityManager.getRunningTasks(1);
        if (!(runningTaskInfos == null || (runningTaskInfos.isEmpty() ^ 1) == 0)) {
            ComponentName cn = ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity;
            return (cn == null || cn.getClassName() == null || !cn.getClassName().startsWith(HUAWEI_SETTINGS)) ? false : true;
        }
    }

    public void requestUpdateDnsServers(ArrayList<String> dnses) {
        if (dnses != null && !dnses.isEmpty()) {
            sendMessage(131882, dnses);
        }
    }

    public void sendUpdateDnsServersRequest(Message msg, LinkProperties lp) {
        if (msg != null && msg.obj != null) {
            ArrayList<String> dnsesStr = msg.obj;
            ArrayList<InetAddress> dnses = new ArrayList();
            int i = 0;
            while (i < dnsesStr.size()) {
                try {
                    dnses.add(Inet4Address.getByName((String) dnsesStr.get(i)));
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

    public void setForceDhcpDiscovery(IpManager ipManager) {
        if ((this.mRenewDhcpSelfCuring.get() || this.mWifiSelfCuring.get()) && ipManager != null) {
            logd("setForceDhcpDiscovery, force dhcp discovery for sce background cure internet.");
            ipManager.setForceDhcpDiscovery();
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

    public void handleStaticIpConfig(IpManager ipManager, WifiNative wifiNative, StaticIpConfiguration config) {
        if (ipManager != null && wifiNative != null && config != null) {
            ProvisioningConfiguration prov = IpManager.buildProvisioningConfiguration().withStaticConfiguration(config).withoutIpReachabilityMonitor().withApfCapabilities(wifiNative.getApfCapabilities()).build();
            logd("handleStaticIpConfig, startProvisioning");
            ipManager.startProvisioning(prov);
        }
    }

    public void notifyIpConfigCompleted() {
        HwSelfCureEngine.getInstance(this.myContext, this).notifyIpConfigCompleted();
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
        if (saveCurrentConfig()) {
            checkScanAlwaysAvailable();
            this.mWifiSelfCuring.set(true);
            resetSelfCureCandidateLostCnt();
            WifiProCommonUtils.setWifiSelfCureStatus(102);
            checkWifiBackgroundStatus();
            selfCureWifiDisable();
            return;
        }
        stopSelfCureDelay(1, 0);
    }

    public void startSelfCureWifiReassoc() {
        resetSelfCureParam();
        if (saveCurrentConfig()) {
            this.mWifiSelfCuring.set(true);
            resetSelfCureCandidateLostCnt();
            WifiProCommonUtils.setWifiSelfCureStatus(101);
            checkWifiBackgroundStatus();
            reassociateCommand();
            setSelfCureWifiTimeOut(4);
            return;
        }
        stopSelfCureDelay(1, 0);
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
        this.mSelfCureWifiLastState = 0;
        this.mSelfCureNetworkLastState = DetailedState.IDLE;
        this.mSelfCureWifiConnectRetry = 0;
        removeMessages(131888);
        removeMessages(131889);
        removeMessages(131890);
        removeMessages(131891);
    }

    private void checkWifiBackgroundStatus() {
        boolean z;
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        logd("checkWifiBackgroundStatus: detailstate=" + networkInfo.getDetailedState() + " isMobileDataInactive=" + WifiProCommonUtils.isMobileDataInactive(this.myContext));
        if (networkInfo == null || networkInfo.getDetailedState() != DetailedState.VERIFYING_POOR_LINK) {
            z = false;
        } else {
            z = WifiProCommonUtils.isMobileDataInactive(this.myContext) ^ 1;
        }
        setWifiBackgroundStatus(z);
    }

    public void setWifiBackgroundStatus(boolean background) {
        if (isWifiSelfCuring()) {
            logd("setWifiBackgroundStatus: " + background + " wifiBackgroundConnected=" + this.mWifiBackgroundConnected);
            this.mWifiBackgroundConnected = background;
        }
    }

    private void checkScanAlwaysAvailable() {
        boolean scanAvailable = Global.getInt(this.myContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1;
        if (isWifiSelfCuring()) {
            if (this.mWifiAlwaysOnBeforeCure && (scanAvailable ^ 1) != 0) {
                logd("enable scan always available");
                Global.putInt(this.myContext.getContentResolver(), "wifi_scan_always_enabled", 1);
            }
        } else if (scanAvailable) {
            logd("disable scan always available");
            this.mWifiAlwaysOnBeforeCure = true;
            Global.putInt(this.myContext.getContentResolver(), "wifi_scan_always_enabled", 0);
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
        this.mWifiSelfCureState = wifiSelfCureState;
        switch (this.mWifiSelfCureState) {
            case 1:
                logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_OFF_TIMEOUT 2000");
                sendMessageDelayed(131888, -1, 0, 2000);
                break;
            case 2:
                logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_ON_TIMEOUT 3000");
                sendMessageDelayed(131889, -1, 0, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                break;
            case 3:
                int i;
                if (((PowerManager) this.myContext.getSystemService("power")).isScreenOn()) {
                    i = 15000;
                } else {
                    i = HwQoEService.KOG_CHECK_FG_APP_PERIOD;
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

    public boolean checkSelfCureWifiResult() {
        int i = 0;
        int wifiState = syncGetWifiState();
        if (wifiState == 2) {
            this.mWifiEnabledTimeStamp = System.currentTimeMillis();
        }
        if (wifiState == 0 && !isWifiSelfCuring() && wifiStateMachineUtils.getScreenOn(this)) {
            WifiConfigManager wifiConfigMgr = wifiStateMachineUtils.getWifiConfigManager(this);
            for (WifiConfiguration config : wifiConfigMgr.getConfiguredNetworks()) {
                if (config.portalCheckStatus != 0) {
                    config.portalCheckStatus = 0;
                    wifiConfigMgr.updateInternetInfoByWifiPro(config);
                }
            }
        }
        if (this.mWifiSoftSwitchRunning.get() && wifiState == 0) {
            logd("checkSelfCureWifiResult, WifiSoftSwitchRunning, WIFI_STATE_DISABLING.");
            removeMessages(131897);
            sendMessage(131897, -4, 0);
            return false;
        } else if (!isWifiSelfCuring()) {
            return false;
        } else {
            if ((this.mSelfCureWifiLastState > wifiState && this.mWifiSelfCureState != 1) || (this.mSelfCureWifiLastState == wifiState && wifiState == 0)) {
                logd("last state =" + this.mSelfCureWifiLastState + "current state=" + wifiState + " user may toggle wifi! stop selfcure");
                sendMessageDelayed(131894, 200);
                exitWifiSelfCure(1, -1);
            }
            this.mSelfCureWifiLastState = wifiState;
            NetworkInfo networkInfo;
            boolean connSucc;
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
                    networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
                    if ((!isDuplicateNetworkState(networkInfo) && networkInfo.getDetailedState() == DetailedState.CONNECTED) || networkInfo.getDetailedState() == DetailedState.VERIFYING_POOR_LINK) {
                        logd("wifi connect > CMD_SCE_WIFI_CONNECT_TIMEOUT msg removed state=" + networkInfo.getDetailedState());
                        removeMessages(131890);
                        connSucc = isWifiConnectToSameAP();
                        if (!connSucc) {
                            i = -1;
                        }
                        notifySelfCureComplete(connSucc, i);
                        break;
                    }
                case 4:
                case 5:
                    networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
                    if ((isDuplicateNetworkState(networkInfo) || networkInfo.getDetailedState() != DetailedState.CONNECTED) && networkInfo.getDetailedState() != DetailedState.VERIFYING_POOR_LINK) {
                        if (!isDuplicateNetworkState(networkInfo) && networkInfo.getDetailedState() == DetailedState.DISCONNECTED) {
                            logd("wifi reassociate/reconnect > CMD_SCE_WIFI_REASSOC_TIMEOUT msg removed state=" + networkInfo.getDetailedState());
                            removeMessages(131891);
                            removeMessages(131896);
                            notifySelfCureComplete(false, -1);
                            break;
                        }
                    }
                    logd("wifi reassociate/reconnect > CMD_SCE_WIFI_REASSOC_TIMEOUT msg removed state=" + networkInfo.getDetailedState());
                    removeMessages(131891);
                    removeMessages(131896);
                    connSucc = isWifiConnectToSameAP();
                    if (!connSucc) {
                        i = -1;
                    }
                    notifySelfCureComplete(connSucc, i);
                    break;
                    break;
            }
            return true;
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

    public void notifySelfCureComplete(boolean success, int reasonCode) {
        if (!success && reasonCode == -4) {
            Log.d("WIFIPRO", "notifySelfCureComplete SOFT_CONNECT_FAILED, timeout happend");
            this.mWifiSoftSwitchRunning.set(false);
            WifiProCommonUtils.setWifiSelfCureStatus(0);
            stopSelfCureDelay(-4, 0);
        } else if (isWifiSelfCuring()) {
            if (success) {
                handleSelfCureNormal();
            } else {
                handleSelfCureException(reasonCode);
            }
        } else {
            logd("notifySelfCureComplete: not Curing!");
            stopSelfCureDelay(1, 0);
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
                    startConnectToUserSelectNetwork(this.mCurrentConfigNetId, null);
                    setSelfCureWifiTimeOut(3);
                    break;
                }
                int reason;
                if (reasonCode == -2) {
                    reason = -2;
                } else {
                    reason = -1;
                }
                stopSelfCureDelay(reason, 0);
                if (!this.mWifiBackgroundConnected) {
                    if (reasonCode != -2) {
                        startConnectToUserSelectNetwork(this.mCurrentConfigNetId, null);
                    }
                    this.mCurrentConfigNetId = -1;
                    break;
                }
                disconnectCommand();
                break;
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
            checkScanAlwaysAvailable();
            NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
            if (this.mWifiBackgroundConnected && networkInfo != null && networkInfo.getDetailedState() == DetailedState.CONNECTED) {
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
                if (exitedType == 151553 || exitedType == 151556) {
                    status = -3;
                } else if (exitedType == 1) {
                    boolean scanAlwaysAvailable = Global.getInt(this.myContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1;
                    if (hasMessages(131891) && scanAlwaysAvailable && getCurrentState() == wifiStateMachineUtils.getDisconnectedState(this)) {
                        status = -3;
                    }
                }
                stopSelfCureDelay(status, 0);
            } else {
                logd("exitWifiSelfCure, user forget other network, do nothing.");
            }
        }
    }

    public List<String> syncGetApLinkedStaList(AsyncChannel channel) {
        log("HwWiFIStateMachine syncGetApLinkedStaList");
        Message resultMsg = channel.sendMessageSynchronously(CMD_AP_STARTED_GET_STA_LIST);
        List<String> ret = resultMsg.obj;
        resultMsg.recycle();
        return ret;
    }

    public void setSoftapMacFilter(String macFilter) {
        handleSetSoftapMacFilter(macFilter);
    }

    public void setSoftapDisassociateSta(String mac) {
        sendMessage(obtainMessage(CMD_AP_STARTED_SET_DISASSOCIATE_STA, mac));
    }

    public void handleSetSoftapMacFilter(String macFilter) {
        log("HwWifiStateMachine handleSetSoftapMacFilter is called, macFilter =" + macFilter);
        WifiInjector.getInstance().getWifiNative().setSoftapMacFltrHw(macFilter);
    }

    public void handleSetSoftapDisassociateSta(String mac) {
        log("HwWifiStateMachine handleSetSoftapDisassociateSta is called, mac =" + mac);
        WifiInjector.getInstance().getWifiNative().disassociateSoftapStaHw(mac);
    }

    public void handleSetWifiApConfigurationHw(String channel) {
        log("HwWifiStateMachine handleSetWifiApConfigurationHw is called");
        WifiInjector.getInstance().getWifiNative().setSoftapHw(channel, String.valueOf(Secure.getInt(this.myContext.getContentResolver(), "wifi_ap_maxscb", 8)));
    }

    public boolean handleWapiFailureEvent(Message message, SupplicantStateTracker mSupplicantStateTracker) {
        Intent intent;
        if (147474 == message.what) {
            log("Handling WAPI_EVENT, msg [" + message.what + "]");
            intent = new Intent(SUPPLICANT_WAPI_EVENT);
            intent.putExtra("wapi_string", 16);
            this.myContext.sendBroadcast(intent);
            mSupplicantStateTracker.sendMessage(147474);
            return true;
        } else if (147475 != message.what) {
            return false;
        } else {
            log("Handling WAPI_EVENT, msg [" + message.what + "]");
            intent = new Intent(SUPPLICANT_WAPI_EVENT);
            intent.putExtra("wapi_string", 17);
            this.myContext.sendBroadcast(intent);
            return true;
        }
    }

    public void handleStopWifiRepeater(AsyncChannel wifiP2pChannel) {
        wifiP2pChannel.sendMessage(CMD_STOP_WIFI_REPEATER);
    }

    public boolean isWifiRepeaterStarted() {
        return 1 == Global.getInt(this.myContext.getContentResolver(), "wifi_repeater_on", 0) || 6 == Global.getInt(this.myContext.getContentResolver(), "wifi_repeater_on", 0);
    }

    public void setWifiRepeaterStoped() {
        Global.putInt(this.myContext.getContentResolver(), "wifi_repeater_on", 0);
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
        return super.isHiLinkActive();
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
        return this.mHiLinkController.saveWpsOkcConfiguration(connectionNetId, connectionBssid, syncGetScanResultsList());
    }

    public void handleAntenaPreempted() {
        log(getName() + "EVENT_ANT_CORE_ROB");
        String ACTION_WIFI_ANTENNA_PREEMPTED = HwABSUtils.ACTION_WIFI_ANTENNA_PREEMPTED;
        String HUAWEI_BUSSINESS_PERMISSION = HwABSUtils.HUAWEI_BUSSINESS_PERMISSION;
        this.myContext.sendBroadcastAsUser(new Intent(ACTION_WIFI_ANTENNA_PREEMPTED), UserHandle.ALL, HUAWEI_BUSSINESS_PERMISSION);
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
            List<ScanResult> scanResults = syncGetScanResultsList();
            if (scanResults == null) {
                return false;
            }
            int foundCounter = 0;
            for (int i = 0; i < scanResults.size(); i++) {
                ScanResult nextResult = (ScanResult) scanResults.get(i);
                String scanSsid = "\"" + nextResult.SSID + "\"";
                String capabilities = nextResult.capabilities;
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
            log("getConnectionRawPsk: " + ret);
            return ret;
        } else {
            log("getConnectionRawPsk: netId is invalid.");
            return null;
        }
    }

    protected void notifyWlanChannelNumber(int channel) {
        if (channel > 13) {
            channel = 0;
        }
        WifiCommonUtils.notifyDeviceState("WLAN", String.valueOf(channel), "");
    }

    protected void notifyWlanState(String state) {
        WifiCommonUtils.notifyDeviceState("WLAN", state, "");
    }

    private long getScanInterval() {
        long scanInterval;
        if (wifiStateMachineUtils.getOperationalMode(this) == 3) {
            scanInterval = Global.getLong(this.myContext.getContentResolver(), WIFI_SCAN_INTERVAL_WLAN_CLOSE, WIFI_SCAN_INTERVAL_WLAN_CLOSE_DEFAULT);
        } else if (wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
            scanInterval = Global.getLong(this.myContext.getContentResolver(), WIFI_SCAN_INTERVAL_WHITE_WLAN_CONNECTED, 10000);
        } else {
            scanInterval = Global.getLong(this.myContext.getContentResolver(), WIFI_SCAN_INTERVAL_WLAN_NOT_CONNECTED, 10000);
        }
        logd("the wifi_scan interval is:" + scanInterval);
        return scanInterval;
    }

    public synchronized boolean allowWifiScanRequest(int pid) {
        RunningAppProcessInfo appProcessInfo = getAppProcessInfoByPid(pid);
        if (pid > 0 && appProcessInfo != null) {
            if (!(appProcessInfo.pkgList == null || this.mIsScanCtrlPluggedin)) {
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
                        appLastScanRequestTimestamp = ((Long) this.mPidLastScanSuccTimestamp.get(Integer.valueOf(pid))).longValue();
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
        logd("wifi_scan pid[" + pid + "] is not correct or is charging. mIsScanCtrlPluggedin = " + this.mIsScanCtrlPluggedin + " isInGlobalScanCtrl = " + this.isInGlobalScanCtrl);
        return false;
    }

    public boolean isRSDBSupported() {
        return WifiInjector.getInstance().getWifiNative().isSupportRsdbByDriver();
    }

    private void wifiScanBlackListLearning(RunningAppProcessInfo appProcessInfo) {
        long now = System.currentTimeMillis();
        long scanInterval = getScanInterval();
        int pid = appProcessInfo.pid;
        clearDeadPidCache();
        if (this.mPidLastScanTimestamp.containsKey(Integer.valueOf(pid))) {
            if (!this.mPidWifiScanCount.containsKey(Integer.valueOf(pid))) {
                this.mPidWifiScanCount.put(Integer.valueOf(pid), Integer.valueOf(0));
            }
            long tmpLastScanRequestTimestamp = ((Long) this.mPidLastScanTimestamp.get(Integer.valueOf(pid))).longValue();
            this.mPidLastScanTimestamp.put(Integer.valueOf(pid), Long.valueOf(now));
            if (tmpLastScanRequestTimestamp != 0 && now >= tmpLastScanRequestTimestamp) {
                if (isWifiScanInBlacklistCache(pid) || now - tmpLastScanRequestTimestamp >= scanInterval) {
                    if (isWifiScanInBlacklistCache(pid) && now - tmpLastScanRequestTimestamp > WIFI_SCAN_BLACKLIST_REMOVE_INTERVAL) {
                        logd("wifi_scan blacklist cache remove pid:" + pid);
                        removeWifiScanBlacklistCache(pid);
                    }
                    this.mPidWifiScanCount.put(Integer.valueOf(pid), Integer.valueOf(0));
                    return;
                }
                int count = ((Integer) this.mPidWifiScanCount.get(Integer.valueOf(pid))).intValue() + 1;
                this.mPidWifiScanCount.put(Integer.valueOf(pid), Integer.valueOf(count));
                if (((long) count) >= WIFI_SCAN_OVER_INTERVAL_MAX_COUNT) {
                    this.mPidLastScanTimestamp.remove(Integer.valueOf(pid));
                    this.mPidWifiScanCount.remove(Integer.valueOf(pid));
                    logd("pid:" + pid + " wifi_scan interval is frequent");
                    if (!isWifiScanWhitelisted(appProcessInfo)) {
                        addWifiScanBlacklistCache(appProcessInfo);
                    }
                }
                return;
            }
            return;
        }
        this.mPidLastScanTimestamp.put(Integer.valueOf(pid), Long.valueOf(now));
        this.mPidWifiScanCount.put(Integer.valueOf(pid), Integer.valueOf(0));
    }

    private boolean isWifiScanInBlacklistCache(int pid) {
        for (Entry<String, Integer> entry : this.mPidBlackList.entrySet()) {
            if (pid == ((Integer) entry.getValue()).intValue()) {
                logd("pid:" + pid + " in wifi_scan cache blacklist, appname=" + ((String) entry.getKey()));
                return true;
            }
        }
        for (Entry<String, Integer> entry2 : this.mPidConnectedBlackList.entrySet()) {
            if (pid == ((Integer) entry2.getValue()).intValue()) {
                logd("pid:" + pid + " in wifi_scan connected cache blacklist, appname=" + ((String) entry2.getKey()));
                return true;
            }
        }
        return false;
    }

    private void removeWifiScanBlacklistCache(int pid) {
        Entry<String, Integer> entry;
        this.mPidLastScanSuccTimestamp.remove(Integer.valueOf(pid));
        this.mPidLastScanTimestamp.remove(Integer.valueOf(pid));
        this.mPidWifiScanCount.remove(Integer.valueOf(pid));
        Iterator iter = this.mPidBlackList.entrySet().iterator();
        while (iter.hasNext()) {
            entry = (Entry) iter.next();
            if (pid == ((Integer) entry.getValue()).intValue()) {
                logd("pid:" + pid + " remove from wifi_scan cache blacklist success, appname=" + ((String) entry.getKey()));
                iter.remove();
                break;
            }
        }
        iter = this.mPidConnectedBlackList.entrySet().iterator();
        while (iter.hasNext()) {
            entry = (Entry) iter.next();
            if (pid == ((Integer) entry.getValue()).intValue()) {
                logd("pid:" + pid + " remove from wifi_scan connected cache blacklist success, appname=" + ((String) entry.getKey()));
                iter.remove();
                return;
            }
        }
    }

    private void addWifiScanBlacklistCache(RunningAppProcessInfo appProcessInfo) {
        int pid = appProcessInfo.pid;
        String appName = appProcessInfo.pkgList[0];
        logd("pid:" + pid + " add to wifi_scan connected limited blacklist");
        if (wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
            this.mPidConnectedBlackList.put(appName, Integer.valueOf(pid));
        } else {
            this.mPidBlackList.put(appName, Integer.valueOf(pid));
        }
    }

    private boolean isWifiScanBlacklisted(RunningAppProcessInfo appProcessInfo, long scanInterval) {
        if (isPackagesNamesMatched(appProcessInfo.pkgList, this.myContext.getResources().getStringArray(33816587), null)) {
            logd("config blacklist wifi_scan name:callingPkgNames[pid=" + appProcessInfo.pid + "]=" + appProcessInfo.processName);
            return true;
        }
        if (wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
            this.mPidBlackList.clear();
        } else {
            this.mPidConnectedBlackList.clear();
        }
        if (!(isWifiScanConnectedLimitedWhitelisted(appProcessInfo) || this.mPidBlackListInteval <= 0 || this.mPidBlackListInteval == scanInterval)) {
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
        int j;
        for (j = 0; j < whitePkgsLength; j++) {
            logd("config--list:" + whitePkgs[j]);
        }
        logd("config--db:" + whiteDbPkgs);
        int i = 0;
        while (i < callingPkgNames.length) {
            for (j = 0; j < whitePkgsLength; j++) {
                if (callingPkgNames[i].equals(whitePkgs[j])) {
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

    private boolean isWifiScanConnectedLimitedWhitelisted(RunningAppProcessInfo appProcessInfo) {
        String[] callingPkgNames = appProcessInfo.pkgList;
        String[] whitePkgs = this.myContext.getResources().getStringArray(33816588);
        String whiteDbPkgs = Global.getString(this.myContext.getContentResolver(), WIFI_SCAN_CONNECTED_LIMITED_WHITE_PACKAGENAME);
        if (appProcessInfo.uid == 1000) {
            return true;
        }
        if (!isPackagesNamesMatched(callingPkgNames, whitePkgs, whiteDbPkgs)) {
            return false;
        }
        logd("wifi_scan pkgname is in connected whitelist pkgs");
        return true;
    }

    private boolean isWifiScanWhitelisted(RunningAppProcessInfo appProcessInfo) {
        if (isPackagesNamesMatched(appProcessInfo.pkgList, this.myContext.getResources().getStringArray(33816589), Global.getString(this.myContext.getContentResolver(), WIFI_SCAN_WHITE_PACKAGENAME))) {
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

    private RunningAppProcessInfo getAppProcessInfoByPid(int pid) {
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.myContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pid) {
                logd("PkgInfo--uid=" + appProcess.uid + ", processName=" + appProcess.processName + ",pid=" + pid);
                return appProcess;
            }
        }
        return null;
    }

    private void clearDeadPidCache() {
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.myContext.getSystemService("activity")).getRunningAppProcesses();
        ArrayList<Integer> tmpPidSet = new ArrayList();
        Iterator iter = this.mPidLastScanTimestamp.entrySet().iterator();
        if (appProcessList != null) {
            for (RunningAppProcessInfo appProcess : appProcessList) {
                tmpPidSet.add(Integer.valueOf(appProcess.pid));
            }
            while (iter.hasNext()) {
                Integer key = (Integer) ((Entry) iter.next()).getKey();
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

    protected void onPostHandleMessage(Message msg) {
        if (this.mDestStates != null) {
            IState destState = (IState) this.mDestStates.poll();
            if (destState != null) {
                Log.i(TAG, "transition to " + destState.getClass().getSimpleName() + " finished.");
            }
        }
    }

    private void setLowPwrMode(boolean isConnected, String ssid, boolean isMobileAP, boolean isScreenOn) {
        String hwSsid = "\"Huawei-Employee\"";
        boolean isHwSsid = false;
        if (ssid != null) {
            isHwSsid = ssid.equals(hwSsid);
        }
        logd("setpmlock:isConnected: " + isConnected + " ssid:" + ssid + " isMobileAP:" + isMobileAP + " isAndroidMobileAP:" + isAndroidMobileAP());
        if (!isConnected || (!isHwSsid && (!(isMobileAP && isAndroidMobileAP()) && (isScreenOn ^ 1) == 0))) {
            WifiInjector.getInstance().getWifiNative().gameKOGAdjustSpeed(0, 6);
        } else {
            WifiInjector.getInstance().getWifiNative().gameKOGAdjustSpeed(0, 7);
        }
    }

    private void pwrBoostRegisterBcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(SCREENOFF_START_LOWPWR_FIRMWARE);
        this.myContext.registerReceiver(this.mBcastReceiver, filter);
    }

    private void linkMeasureAndStatic(boolean enable) {
        long arpRtt = 0;
        int arpCnt = 0;
        HwArpVerifier mArpVerifier = HwArpVerifier.getDefault();
        TxPacketCounters txPacketCounters = WifiInjector.getInstance().getWifiNative().getTxPacketCounters();
        if (txPacketCounters != null) {
            int lastTxGoodCnt = txPacketCounters.txSucceeded;
            int lastTxBadCnt = txPacketCounters.txFailed;
            long lastTxRetries = wifiStateMachineUtils.getWifiInfo(this).txRetries;
            if (mArpVerifier != null) {
                for (int i = 0; i < 5; i++) {
                    long ret = mArpVerifier.getGateWayArpRTT(1000);
                    if (ret != -1) {
                        arpRtt += ret;
                        arpCnt++;
                    }
                }
            }
            txPacketCounters = WifiInjector.getInstance().getWifiNative().getTxPacketCounters();
            if (txPacketCounters != null) {
                int dltTxGoodCnt = txPacketCounters.txSucceeded - lastTxGoodCnt;
                int dltTxBadCnt = txPacketCounters.txFailed - lastTxBadCnt;
                long dltTxRetries = wifiStateMachineUtils.getWifiInfo(this).txRetries - lastTxRetries;
                logd("pwr:dltTxGoodCnt:" + dltTxGoodCnt + " dltTxBadCnt:" + dltTxBadCnt + " dltTxRetries:" + dltTxRetries + " arpRtt:" + arpRtt + " arpCnt:" + arpCnt + " enable:" + enable);
                HwWifiStatStoreImpl.getDefault().txPwrBoostChrStatic(Boolean.valueOf(enable), (int) arpRtt, arpCnt, dltTxGoodCnt, dltTxBadCnt, (int) dltTxRetries);
            }
        }
    }

    public int isAllowedManualWifiPwrBoost() {
        return this.mIsAllowedManualPwrBoost;
    }

    public boolean isWifiConnectivityManagerEnabled() {
        return this.mWifiConnectivityManager != null ? this.mWifiConnectivityManager.isWifiConnectivityManagerEnabled() : false;
    }

    private void clearPwrBoostChrStatus() {
        this.mCurrentPwrBoostStat = false;
        this.mIsFinishLinkDetect = false;
        this.mPwrBoostOncnt = 0;
        this.mPwrBoostOffcnt = 0;
    }

    private boolean isGlobalScanCtrl(RunningAppProcessInfo appProcessInfo) {
        logd("isGlobalScanCtrl begin ");
        if (!isWifiScanWhitelisted(appProcessInfo)) {
            logd("wifi_scan return isInGlobalScanCtrl = " + this.isInGlobalScanCtrl);
            if (!this.isInGlobalScanCtrl || System.currentTimeMillis() - this.mLastScanTimestamp > 5000) {
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
                if (scanInterval < 5000) {
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
        String flag = "1";
        String usb = HwArpVerifier.readFileByChars(USB_SUPPLY);
        if (usb.length() == 0) {
            usb = HwArpVerifier.readFileByChars(USB_SUPPLY_QCOM);
        }
        if (flag.equals(usb.trim())) {
            return true;
        }
        logd("getChargingState return false");
        return false;
    }

    private void startScreenoffTrack() {
        if (this.mAlarmManager == null) {
            Log.d(TAG, "AlarmManager null, error!");
        } else {
            this.mAlarmManager.setExact(1, System.currentTimeMillis() + SCREENOFF_NO_LOWPWR_FIRMWARE_TIME_SPAN, this.mScreenoffStartLowpwrFirmwareIntent);
        }
    }

    private void stopScreenoffTrack() {
        if (this.mAlarmManager == null) {
            Log.d(TAG, "AlarmManager null, error!");
        } else {
            this.mAlarmManager.cancel(this.mScreenoffStartLowpwrFirmwareIntent);
        }
    }

    public boolean isAndroidMobileAP() {
        String androidMobileIpAddress = "192.168.43.";
        String ipAddress = "";
        WifiInfo wifiInfo = getWifiInfo();
        if (wifiInfo != null) {
            ipAddress = intIpToStringIp(wifiInfo.getIpAddress());
        }
        if (ipAddress == null || !ipAddress.startsWith(androidMobileIpAddress)) {
            return false;
        }
        return true;
    }

    private String intIpToStringIp(int ip) {
        return String.format("%d.%d.%d.%d", new Object[]{Integer.valueOf(ip & 255), Integer.valueOf((ip >> 8) & 255), Integer.valueOf((ip >> 16) & 255), Integer.valueOf((ip >> 24) & 255)});
    }
}
