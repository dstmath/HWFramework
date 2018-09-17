package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
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
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.net.wifi.wifipro.WifiProStatusUtils;
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
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.routermodelrecognition.HwRouterModelRecognition;
import com.android.server.wifi.wifipro.HwDualBandManager;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiProConfigStore;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifi.wifipro.WifiproUtils;
import com.android.server.wifi.wifipro.hwintelligencewifi.HwIntelligenceWiFiManager;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

public class HwWifiStateMachine extends WifiStateMachine {
    public static final int AP_CAP_CACHE_COUNT = 1000;
    public static final String AP_CAP_KEY = "AP_CAP";
    private static final String ASSOCIATION_REJECT_STATUS_CODE = "wifi_association_reject_status_code";
    public static final String BSSID_KEY = "BSSID";
    public static final int CMD_AP_STARTED_GET_STA_LIST = 131104;
    public static final int CMD_AP_STARTED_SET_DISASSOCIATE_STA = 131106;
    public static final int CMD_AP_STARTED_SET_MAC_FILTER = 131105;
    static final int CMD_GET_CHANNEL_LIST_5G = 131572;
    public static final int CMD_SCREEN_OFF_SCAN = 131578;
    public static final int CMD_STOP_WIFI_REPEATER = 131577;
    public static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    private static final int DHCP_RESULT_CACHE_SIZE = 50;
    public static final int ENTERPRISE_HOTSPOT_THRESHOLD = 4;
    private static final String HUAWEI_SETTINGS = "com.android.settings.Settings$WifiSettingsActivity";
    public static final int SCAN_ONLY_CONNECT_MODE = 100;
    private static final String SOFTAP_IFACE = "wlan0";
    private static final int SUCCESS = 1;
    public static final String SUPPLICANT_WAPI_EVENT = "android.net.wifi.supplicant.WAPI_EVENT";
    private static final String TAG = "HwWifiStateMachine";
    public static final String TX_MCS_SET = "TX_MCS_SET";
    public static final int WAPI_AUTHENTICATION_FAILURE_EVENT = 147474;
    public static final int WAPI_CERTIFICATION_FAILURE_EVENT = 147475;
    public static final int WAPI_EVENT_AUTH_FAIL_CODE = 16;
    public static final int WAPI_EVENT_CERT_FAIL_CODE = 17;
    private static final String WIFI_EVALUATE_TAG = "wifipro_recommending_access_points";
    private static final int WIFI_START_EVALUATE_TAG = 1;
    private static final int WIFI_STOP_EVALUATE_TAG = 0;
    private static int mFrequency;
    private static WifiNativeUtils wifiNativeUtils;
    private static WifiStateMachineUtils wifiStateMachineUtils;
    private long lastConnectTime;
    private HashMap<String, String> lastDhcps;
    private ActivityManager mActivityManager;
    private int mBQEUid;
    private CodeReceiver mCodeReceiver;
    public boolean mCurrNetworkHistoryInserted;
    private int mCurrentConfigNetId;
    private String mCurrentConfigurationKey;
    private final LruCache<String, DhcpResults> mDhcpResultCache;
    private HiLinkController mHiLinkController;
    private HwInnerNetworkManagerImpl mHwInnerNetworkManagerImpl;
    private boolean mIsEnableWifiproFirewall;
    private boolean mIsSetWifiCountryCode;
    private int mLastTxPktCnt;
    private AtomicBoolean mRenewDhcpSelfCuring;
    private int mScreenOffScanToken;
    public WifiConfiguration mSelectedConfig;
    private DetailedState mSelfCureNetworkLastState;
    private int mSelfCureWifiConnectRetry;
    private int mSelfCureWifiLastState;
    private WifiSsid mWiFiProRoamingSSID;
    public boolean mWifiAlwaysOnBeforeCure;
    public boolean mWifiBackgroundConnected;
    private WifiDetectConfInfo mWifiDetectConfInfo;
    private int mWifiDetectperiod;
    private WifiScanGenieController mWifiScanGenieController;
    private int mWifiSelfCureState;
    private AtomicBoolean mWifiSelfCuring;
    public boolean mWifiSwitchOnGoing;
    public int mWpsCompletedNetId;
    private HashMap<String, Boolean> mapApCapChr;
    private Context myContext;
    private boolean usingStaticIpConfig;
    private int wifiConnectedBackgroundReason;

    /* renamed from: com.android.server.wifi.HwWifiStateMachine.3 */
    class AnonymousClass3 extends ContentObserver {
        AnonymousClass3(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            int tag = Secure.getInt(HwWifiStateMachine.this.myContext.getContentResolver(), HwWifiStateMachine.WIFI_EVALUATE_TAG, HwWifiStateMachine.DEFAULT_WIFI_AP_CHANNEL);
            if (HwWifiStateMachine.this.mBQEUid == HwWifiStateMachine.AP_CAP_CACHE_COUNT) {
                HwWifiStateMachine.this.mBQEUid = HwWifiStateMachine.this.getAppUid("com.huawei.wifiprobqeservice");
            }
            HwWifiStateMachine.this.logd("**wifipro tag is chenge, setWifiproFirewallEnable**,tag =" + tag);
            if (tag == HwWifiStateMachine.WIFI_START_EVALUATE_TAG) {
                try {
                    HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallEnable(true);
                    if (HwWifiStateMachine.this.mBQEUid != HwWifiStateMachine.AP_CAP_CACHE_COUNT) {
                        HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallWhitelist(HwWifiStateMachine.this.mBQEUid);
                    }
                    HwWifiStateMachine.this.mHwInnerNetworkManagerImpl.setWifiproFirewallWhitelist(HwWifiStateMachine.AP_CAP_CACHE_COUNT);
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
    }

    private class CodeReceiver extends BroadcastReceiver {
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
        List<ScanDetail> lstScanRet;

        public FilterScanRunnable(List<ScanDetail> lstScan) {
            this.lstScanRet = null;
            this.lstScanRet = lstScan;
        }

        /* JADX WARNING: inconsistent code. */
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
                                String strJSON = "{BSSID:\"" + strCurBssid + "\"," + HwWifiStateMachine.AP_CAP_KEY + ":" + (((scanned.getNetworkDetail().getPrimaryFreq() / HwWifiStateMachine.AP_CAP_CACHE_COUNT) * 10) + Math.abs(((stream1 + stream2) + stream3) + stream4)) + "," + HwWifiStateMachine.TX_MCS_SET + ":" + scanned.getNetworkDetail().getTxMcsSet() + "}";
                                if (HwWifiStateMachine.HWFLOW) {
                                    Log.d(HwWifiStateMachine.TAG, "FilterScanRunnable :json = " + strJSON);
                                }
                                hwmCHR.updateWifiException(213, strJSON);
                                HwWifiStateMachine.this.mapApCapChr.put(strCurBssid, Boolean.valueOf(true));
                                if (HwWifiStateMachine.this.mapApCapChr.size() > HwWifiStateMachine.AP_CAP_CACHE_COUNT) {
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

    static {
        wifiNativeUtils = (WifiNativeUtils) EasyInvokeFactory.getInvokeUtils(WifiNativeUtils.class);
        wifiStateMachineUtils = (WifiStateMachineUtils) EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class);
        mFrequency = DEFAULT_WIFI_AP_CHANNEL;
    }

    public HwWifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode) {
        super(context, facade, looper, userManager, wifiInjector, backupManagerProxy, countryCode);
        this.mScreenOffScanToken = DEFAULT_WIFI_AP_CHANNEL;
        this.mDhcpResultCache = new LruCache(DHCP_RESULT_CACHE_SIZE);
        this.mIsSetWifiCountryCode = SystemProperties.getBoolean("ro.config.wifi_country_code", false);
        this.mLastTxPktCnt = DEFAULT_WIFI_AP_CHANNEL;
        this.mIsEnableWifiproFirewall = true;
        this.mapApCapChr = new HashMap();
        this.lastConnectTime = -1;
        this.mRenewDhcpSelfCuring = new AtomicBoolean(false);
        this.mWifiSelfCuring = new AtomicBoolean(false);
        this.mWifiAlwaysOnBeforeCure = false;
        this.mWifiBackgroundConnected = false;
        this.mCurrentConfigurationKey = null;
        this.mCurrentConfigNetId = -1;
        this.mWifiSelfCureState = DEFAULT_WIFI_AP_CHANNEL;
        this.mSelfCureWifiLastState = DEFAULT_WIFI_AP_CHANNEL;
        this.mSelfCureNetworkLastState = DetailedState.IDLE;
        this.mSelfCureWifiConnectRetry = DEFAULT_WIFI_AP_CHANNEL;
        this.mWifiDetectConfInfo = new WifiDetectConfInfo();
        this.mWifiDetectperiod = -1;
        this.mWifiSwitchOnGoing = false;
        this.mSelectedConfig = null;
        this.usingStaticIpConfig = false;
        this.mCurrNetworkHistoryInserted = false;
        this.mWpsCompletedNetId = -1;
        this.wifiConnectedBackgroundReason = DEFAULT_WIFI_AP_CHANNEL;
        this.lastDhcps = new HashMap();
        this.mHiLinkController = null;
        this.mWiFiProRoamingSSID = null;
        HwWifiCHRStateManager hwWifiCHR = HwWifiServiceFactory.getHwWifiCHRStateManager();
        if (hwWifiCHR != null && (hwWifiCHR instanceof HwWifiCHRStateManagerImpl)) {
            HwWifiCHRStateManagerImpl hwWifiCHRImpl = (HwWifiCHRStateManagerImpl) hwWifiCHR;
            if (HWFLOW) {
                Log.d(TAG, "construct context");
            }
            hwWifiCHRImpl.setContextRef(context);
            hwWifiCHRImpl.setWifiStateMachine(this);
        }
        this.myContext = context;
        this.mBQEUid = AP_CAP_CACHE_COUNT;
        this.mHwInnerNetworkManagerImpl = (HwInnerNetworkManagerImpl) HwFrameworkFactory.getHwInnerNetworkManager();
        registerReceiverInWifiPro(context);
        if (this.mIsEnableWifiproFirewall) {
            registerForWifiEvaluateChanges();
        }
        if (WifiRadioPowerController.isRadioPowerEnabled()) {
            WifiRadioPowerController.setInstance(context, this, wifiStateMachineUtils.getWifiNative(this), (HwInnerNetworkManagerImpl) HwFrameworkFactory.getHwInnerNetworkManager());
        }
        if (this.mIsSetWifiCountryCode) {
            this.mCodeReceiver = new CodeReceiver();
            IntentFilter myfilter = new IntentFilter();
            myfilter.addAction("com.android.net.wifi.countryCode");
            this.myContext.registerReceiver(this.mCodeReceiver, myfilter);
        }
        HwCHRExceptionListener.getInstance(context).startChrWifiListener();
        this.mHiLinkController = new HiLinkController(context, this);
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        if (!HwRouterModelRecognition.startInstance(context) && HWFLOW) {
            Log.d(TAG, "HwRouterModelRecognition.startInstance failed");
        }
    }

    public String getWpaSuppConfig() {
        log("WiFIStateMachine  getWpaSuppConfig InterfaceName ");
        if (this.myContext.checkCallingPermission("com.huawei.permission.ACCESS_AP_INFORMATION") == 0) {
            return wifiNativeUtils.doStringCommand(wifiStateMachineUtils.getWifiNative(this), "GET_WPA_SUPP_CONFIG");
        }
        log("getWpaSuppConfig(): permissin deny");
        return null;
    }

    public void enableAllNetworks() {
        log("enableAllNetworks mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        if (wifiStateMachineUtils.getOperationalMode(this) != SCAN_ONLY_CONNECT_MODE) {
            super.enableAllNetworks();
        }
    }

    protected void enableAllNetworksByMode() {
        log("enableAllNetworks mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        if (wifiStateMachineUtils.getOperationalMode(this) != SCAN_ONLY_CONNECT_MODE) {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
        }
    }

    protected void handleNetworkDisconnect() {
        log("handle network disconnect mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        if (wifiStateMachineUtils.getOperationalMode(this) == SCAN_ONLY_CONNECT_MODE) {
            HwDisableLastNetwork();
        }
        log("handleNetworkDisconnect,resetWifiProManualConnect");
        resetWifiProManualConnect();
        super.handleNetworkDisconnect();
    }

    protected void loadAndEnableAllNetworksByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) == SCAN_ONLY_CONNECT_MODE) {
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
        if (getCurrentState() != wifiStateMachineUtils.getSupplicantStoppingState(this) && wifiStateMachineUtils.getLastNetworkId(this) != -1) {
            WifiConfigStoreUtils.disableNetwork(wifiStateMachineUtils.getWifiConfigManager(this), wifiStateMachineUtils.getLastNetworkId(this), DEFAULT_WIFI_AP_CHANNEL);
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
        if (message.arg1 != SCAN_ONLY_CONNECT_MODE) {
            return false;
        }
        log("SCAN_ONLY_CONNECT_MODE, do not enable all networks here.");
        if (mLastOperationMode == 3) {
            wifiStateMachineUtils.setWifiState(this, 3);
            WifiConfigStoreUtils.loadConfiguredNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            wifiStateMachineUtils.getWifiP2pChannel(this).sendMessage(131203);
        }
        wifiStateMachineUtils.setOperationalMode(this, SCAN_ONLY_CONNECT_MODE);
        transitionTo(wifiStateMachineUtils.getDisconnectedState(this));
        return true;
    }

    protected boolean processConnectModeSetMode(Message message) {
        if (wifiStateMachineUtils.getOperationalMode(this) != SCAN_ONLY_CONNECT_MODE || message.arg2 != 0) {
            return false;
        }
        log("CMD_ENABLE_NETWORK command is ignored.");
        wifiStateMachineUtils.replyToMessage((WifiStateMachine) this, message, message.what, (int) WIFI_START_EVALUATE_TAG);
        return true;
    }

    protected boolean processL2ConnectedSetMode(Message message) {
        wifiStateMachineUtils.setOperationalMode(this, message.arg1);
        if (wifiStateMachineUtils.getOperationalMode(this) == SCAN_ONLY_CONNECT_MODE) {
            if (!wifiStateMachineUtils.getNetworkInfo(this).isConnected()) {
                sendMessage(131145);
            }
            disableAllNetworksExceptLastConnected();
            return true;
        } else if (wifiStateMachineUtils.getOperationalMode(this) != WIFI_START_EVALUATE_TAG) {
            return false;
        } else {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            return true;
        }
    }

    protected boolean processDisconnectedSetMode(Message message) {
        wifiStateMachineUtils.setOperationalMode(this, message.arg1);
        log("set operation mode mOperationalMode: " + wifiStateMachineUtils.getOperationalMode(this));
        if (wifiStateMachineUtils.getOperationalMode(this) == SCAN_ONLY_CONNECT_MODE) {
            WifiConfigStoreUtils.disableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            return true;
        } else if (wifiStateMachineUtils.getOperationalMode(this) != WIFI_START_EVALUATE_TAG) {
            return false;
        } else {
            WifiConfigStoreUtils.enableAllNetworks(wifiStateMachineUtils.getWifiConfigManager(this));
            wifiStateMachineUtils.getWifiNative(this).reconnect();
            return true;
        }
    }

    protected void enterConnectedStateByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) == SCAN_ONLY_CONNECT_MODE) {
            log("wifi connected. disable other networks.");
            disableAllNetworksExceptLastConnected();
        }
    }

    protected boolean enterDriverStartedStateByMode() {
        this.mWifiScanGenieController = WifiScanGenieController.createWifiScanGenieControllerImpl(this.myContext);
        if (wifiStateMachineUtils.getOperationalMode(this) != SCAN_ONLY_CONNECT_MODE) {
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
            if (!(network.networkId == wifiStateMachineUtils.getLastNetworkId(this) || network.status == WIFI_START_EVALUATE_TAG)) {
                WifiConfigStoreUtils.disableNetwork(wifiStateMachineUtils.getWifiConfigManager(this), network.networkId, DEFAULT_WIFI_AP_CHANNEL);
            }
        }
    }

    public void log(String message) {
        Log.d(TAG, message);
    }

    protected boolean isScanAndManualConnectMode() {
        return wifiStateMachineUtils.getOperationalMode(this) == SCAN_ONLY_CONNECT_MODE;
    }

    protected boolean processConnectModeAutoConnectByMode() {
        if (wifiStateMachineUtils.getOperationalMode(this) != SCAN_ONLY_CONNECT_MODE) {
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
            int i = this.mScreenOffScanToken + WIFI_START_EVALUATE_TAG;
            this.mScreenOffScanToken = i;
            sendMessageDelayed(obtainMessage(CMD_SCREEN_OFF_SCAN, i, DEFAULT_WIFI_AP_CHANNEL), wifiStateMachineUtils.getSupplicantScanIntervalMs(this));
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
            if (((DhcpResults) this.mDhcpResultCache.get(key)) != null) {
                log("make default IP configuration map, remove old rec.");
                this.mDhcpResultCache.remove(key);
            }
            boolean isPublicESS = false;
            int count = DEFAULT_WIFI_AP_CHANNEL;
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
                        count += WIFI_START_EVALUATE_TAG;
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
            log("make default IP configuration map, add rec for " + key);
            return;
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
        Log.d(TAG, "try to set default IP configuration for " + key);
        return (DhcpResults) this.mDhcpResultCache.get(key);
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
        if (info != null && !this.mWifiDetectConfInfo.isEqual(info)) {
            this.mWifiDetectConfInfo = info;
            sendMessage(131772, info);
        }
    }

    protected void processSetVoWifiDetectMode(Message msg) {
        WifiDetectConfInfo info = msg.obj;
        Log.d(TAG, "set VoWifi Detect Mode " + info);
        boolean ret = false;
        if (info != null) {
            if (info.mWifiDetectMode == WIFI_START_EVALUATE_TAG) {
                ret = wifiNativeUtils.doBooleanCommand(wifiStateMachineUtils.getWifiNative(this), "VOWIFI_DETECT SET LOW_THRESHOLD " + info.mThreshold);
            } else if (info.mWifiDetectMode == 2) {
                ret = wifiNativeUtils.doBooleanCommand(wifiStateMachineUtils.getWifiNative(this), "VOWIFI_DETECT SET HIGH_THRESHOLD " + info.mThreshold);
            } else {
                ret = wifiNativeUtils.doBooleanCommand(wifiStateMachineUtils.getWifiNative(this), "VOWIFI_DETECT SET MODE " + info.mWifiDetectMode);
            }
            if (ret && wifiNativeUtils.doBooleanCommand(wifiStateMachineUtils.getWifiNative(this), "VOWIFI_DETECT SET TRIGGER_COUNT " + info.mEnvalueCount)) {
                ret = wifiNativeUtils.doBooleanCommand(wifiStateMachineUtils.getWifiNative(this), "VOWIFI_DETECT SET MODE " + info.mWifiDetectMode);
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
        if (wifiNativeUtils.doBooleanCommand(wifiStateMachineUtils.getWifiNative(this), "VOWIFI_DETECT SET PERIOD " + period)) {
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
        int i;
        String ret = wifiNativeUtils.doStringCommand(wifiStateMachineUtils.getWifiNative(this), "VOWIFI_DETECT VOWIFi_IS_SUPPORT");
        Log.d(TAG, "isSupportVoWifiDetect ret :" + ret);
        boolean equals = ret != null ? ret.equals("true") : false;
        WifiStateMachineUtils wifiStateMachineUtils = wifiStateMachineUtils;
        int i2 = msg.what;
        if (equals) {
            i = DEFAULT_WIFI_AP_CHANNEL;
        } else {
            i = -1;
        }
        wifiStateMachineUtils.replyToMessage((WifiStateMachine) this, msg, i2, i);
    }

    protected void processStatistics(int event) {
        if (event == 0) {
            this.lastConnectTime = System.currentTimeMillis();
            Flog.bdReport(this.myContext, HwSelfCureUtils.SCE_WIFI_DISABLED_DELAY);
        } else if (WIFI_START_EVALUATE_TAG == event) {
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
        String[] lines;
        int length;
        int i;
        String[] prop;
        int length2;
        ByteBuffer rawByteBuffer = ByteBuffer.allocate(32);
        rawByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int linkSpeed = -1;
        int frequency = -1;
        int rssi = -1;
        String signalInfo = wifiNativeUtils.doStringCommand(wifiStateMachineUtils.getWifiNative(this), "SIGNAL_POLL");
        if (signalInfo != null) {
            lines = signalInfo.split("\n");
            length = lines.length;
            for (i = DEFAULT_WIFI_AP_CHANNEL; i < length; i += WIFI_START_EVALUATE_TAG) {
                prop = lines[i].split("=");
                length2 = prop.length;
                if (r0 >= 2) {
                    try {
                        if (prop[DEFAULT_WIFI_AP_CHANNEL].equals("RSSI")) {
                            rssi = Integer.parseInt(prop[WIFI_START_EVALUATE_TAG]);
                        } else if (prop[DEFAULT_WIFI_AP_CHANNEL].equals("LINKSPEED")) {
                            linkSpeed = Integer.parseInt(prop[WIFI_START_EVALUATE_TAG]);
                        } else if (prop[DEFAULT_WIFI_AP_CHANNEL].equals("FREQUENCY")) {
                            frequency = Integer.parseInt(prop[WIFI_START_EVALUATE_TAG]);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        RssiPacketCountInfo info = new RssiPacketCountInfo();
        String pktcntPoll = wifiNativeUtils.doStringCommand(wifiStateMachineUtils.getWifiNative(this), "PKTCNT_POLL");
        if (pktcntPoll != null) {
            lines = pktcntPoll.split("\n");
            length = lines.length;
            for (i = DEFAULT_WIFI_AP_CHANNEL; i < length; i += WIFI_START_EVALUATE_TAG) {
                prop = lines[i].split("=");
                length2 = prop.length;
                if (r0 >= 2) {
                    try {
                        if (prop[DEFAULT_WIFI_AP_CHANNEL].equals("TXGOOD")) {
                            info.txgood = Integer.parseInt(prop[WIFI_START_EVALUATE_TAG]);
                        } else if (prop[DEFAULT_WIFI_AP_CHANNEL].equals("TXBAD")) {
                            info.txbad = Integer.parseInt(prop[WIFI_START_EVALUATE_TAG]);
                        }
                    } catch (NumberFormatException e2) {
                    }
                }
            }
        }
        rawByteBuffer.putInt(rssi);
        rawByteBuffer.putInt(DEFAULT_WIFI_AP_CHANNEL);
        rawByteBuffer.putInt((int) ((((double) info.txbad) / ((double) (info.txgood + info.txbad))) * 100.0d));
        int dpktcnt = info.txgood - this.mLastTxPktCnt;
        this.mLastTxPktCnt = info.txgood;
        rawByteBuffer.putInt(dpktcnt);
        rawByteBuffer.putInt(convertToAccessType(linkSpeed, frequency));
        String macStr = wifiStateMachineUtils.getWifiInfo(this).getBSSID().replace(":", "");
        byte[] macBytes = new byte[WAPI_EVENT_AUTH_FAIL_CODE];
        try {
            macBytes = macStr.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e3) {
            e3.printStackTrace();
        }
        rawByteBuffer.put(macBytes);
        StringBuilder append = new StringBuilder().append("rssi=");
        length2 = info.txbad;
        Log.d(TAG, r21.append(rssi).append(",txbad=").append(r0).append(", txgood=").append(info.txgood).append(", dpktcnt=").append(dpktcnt).append(", linkSpeed=").append(linkSpeed).append(", frequency=").append(frequency).append(", noise=").append(DEFAULT_WIFI_AP_CHANNEL).append(", mac=").append(macStr).toString());
        return rawByteBuffer.array();
    }

    private static int convertToAccessType(int linkSpeed, int frequency) {
        return DEFAULT_WIFI_AP_CHANNEL;
    }

    private String getMacAddressFromFile() {
        InputStream dis;
        IOException e;
        Throwable th;
        String wifiMacAddr = "02:00:00:00:00:00";
        InputStream inputStream = null;
        InputStream inputStream2 = null;
        File file = new File(Environment.getDataDirectory(), "misc/wifi/macwifi");
        if (file.exists()) {
            try {
                InputStream in = new FileInputStream(file);
                try {
                    dis = new DataInputStream(in);
                } catch (IOException e2) {
                    e = e2;
                    inputStream = in;
                    try {
                        e.printStackTrace();
                        closeInputStream(inputStream2);
                        closeInputStream(inputStream);
                        return wifiMacAddr;
                    } catch (Throwable th2) {
                        th = th2;
                        closeInputStream(inputStream2);
                        closeInputStream(inputStream);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = in;
                    closeInputStream(inputStream2);
                    closeInputStream(inputStream);
                    throw th;
                }
                try {
                    byte[] wifiBuf = new byte[WAPI_EVENT_CERT_FAIL_CODE];
                    if (dis.read(wifiBuf, DEFAULT_WIFI_AP_CHANNEL, WAPI_EVENT_CERT_FAIL_CODE) != WAPI_EVENT_CERT_FAIL_CODE) {
                        loge("macwifi address has error in file");
                    } else {
                        wifiMacAddr = new String(wifiBuf, "utf-8");
                    }
                    closeInputStream(dis);
                    closeInputStream(in);
                    inputStream = in;
                } catch (IOException e3) {
                    e = e3;
                    inputStream2 = dis;
                    inputStream = in;
                    e.printStackTrace();
                    closeInputStream(inputStream2);
                    closeInputStream(inputStream);
                    return wifiMacAddr;
                } catch (Throwable th4) {
                    th = th4;
                    inputStream2 = dis;
                    inputStream = in;
                    closeInputStream(inputStream2);
                    closeInputStream(inputStream);
                    throw th;
                }
            } catch (IOException e4) {
                e = e4;
                e.printStackTrace();
                closeInputStream(inputStream2);
                closeInputStream(inputStream);
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
                WifiConfiguration newConfig = (WifiConfiguration) intent.getParcelableExtra(WifiproUtils.EXTRA_FLAG_NEW_WIFI_CONFIG);
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
                    wifiConfigManager.writeKnownNetworkHistory();
                }
            }
        }, new IntentFilter(WifiproUtils.ACTION_UPDATE_CONFIG_HISTORY), WifiproUtils.NETWORK_CHECKER_RECV_PERMISSION, null);
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int switchType = intent.getIntExtra(WifiHandover.WIFI_HANDOVER_NETWORK_SWITCHTYPE, HwWifiStateMachine.WIFI_START_EVALUATE_TAG);
                WifiConfiguration changeConfig = (WifiConfiguration) intent.getParcelableExtra(WifiHandover.WIFI_HANDOVER_NETWORK_WIFICONFIG);
                HwWifiStateMachine.this.log("ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER, switchType = " + switchType);
                if (!HwWifiStateMachine.this.mWifiSwitchOnGoing && changeConfig != null) {
                    if (switchType == HwWifiStateMachine.WIFI_START_EVALUATE_TAG) {
                        HwWifiStateMachine.this.autoConnectToNetwork(changeConfig.networkId, null);
                    } else {
                        HwWifiStateMachine.this.autoRoamToNetwork(changeConfig.networkId, null);
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
            return DEFAULT_WIFI_AP_CHANNEL;
        }
        return score;
    }

    public void getConfiguredNetworks(Message message) {
        wifiStateMachineUtils.replyToMessage((WifiStateMachine) this, message, message.what, wifiStateMachineUtils.getWifiConfigManager(this).getSavedNetworks());
    }

    public void saveConnectingNetwork(WifiConfiguration config) {
        this.mSelectedConfig = config;
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
                wifiInfo.score = DEFAULT_WIFI_AP_CHANNEL;
            }
            if (networkAgent != null) {
                networkAgent.explicitlyUnselected();
                networkAgent.sendNetworkScore(DEFAULT_WIFI_AP_CHANNEL);
            }
        }
    }

    public void wifiNetworkExplicitlySelected() {
        if (isWifiProEnabled()) {
            WifiInfo wifiInfo = wifiStateMachineUtils.getWifiInfo(this);
            HwNetworkAgent networkAgent = wifiStateMachineUtils.getNetworkAgent(this);
            if (wifiInfo != null) {
                wifiInfo.score = SCAN_ONLY_CONNECT_MODE;
            }
            if (networkAgent != null) {
                networkAgent.explicitlySelected(true);
                networkAgent.sendNetworkScore(SCAN_ONLY_CONNECT_MODE);
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
            if (this.mSelectedConfig != null) {
                config = this.mSelectedConfig;
            } else {
                config = wifiConfigManager.getWifiConfiguration(wifiStateMachineUtils.getLastNetworkId(this));
            }
            if (config != null) {
                bssid = config.BSSID;
                ssid = config.SSID;
            }
            sendWifiHandoverCompletedBroadcast(DEFAULT_WIFI_AP_CHANNEL, bssid, ssid);
        }
        setEnableAutoJoinWhenAssociated(false);
        if (isWifiProEnabled()) {
            WifiConfiguration connectedConfig = wifiConfigManager.getWifiConfiguration(wifiStateMachineUtils.getLastNetworkId(this));
            if (connectedConfig != null && connectedConfig.portalCheckStatus == WIFI_START_EVALUATE_TAG) {
                connectedConfig.portalCheckStatus = DEFAULT_WIFI_AP_CHANNEL;
            }
            if (!(connectedConfig == null || this.mSelectedConfig == null || !isWifiProEvaluatingAP())) {
                if (!this.usingStaticIpConfig && connectedConfig.SSID != null && !connectedConfig.SSID.equals("<unknown ssid>")) {
                    String strDhcpResults = WifiProCommonUtils.dhcpResults2String(wifiStateMachineUtils.getDhcpResults(this), WifiStateMachineUtils.getCurrentCellId(this.myContext));
                    if (!(strDhcpResults == null || connectedConfig.configKey() == null)) {
                        log("handleConnectedInWifiPro, lastDhcpResults = " + strDhcpResults + ", ssid = " + connectedConfig.SSID);
                        this.lastDhcps.put(connectedConfig.configKey(), strDhcpResults);
                    }
                } else if (connectedConfig.networkId != -1) {
                    wifiConfigManager.resetStaticIpConfig(connectedConfig.networkId);
                }
            }
        }
        if (!isWifiProEvaluatingAP() && this.mIsEnableWifiproFirewall) {
            try {
                this.mHwInnerNetworkManagerImpl.setWifiproFirewallEnable(false);
            } catch (Exception e) {
                log("wifi connected, Disable WifiproFirewall again");
            }
        }
        this.mSelectedConfig = null;
        this.usingStaticIpConfig = false;
        this.mWpsCompletedNetId = -1;
        resetSelfCureCandidateLostCnt();
    }

    public void handleDisconnectedInWifiPro() {
        this.mWifiScanGenieController.handleWiFiDisconnected();
        this.mCurrNetworkHistoryInserted = false;
        if (this.wifiConnectedBackgroundReason == 2 || this.wifiConnectedBackgroundReason == 3) {
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, false);
        }
        this.wifiConnectedBackgroundReason = DEFAULT_WIFI_AP_CHANNEL;
        setEnableAutoJoinWhenAssociated(true);
        WifiNative wifiNative = wifiStateMachineUtils.getWifiNative(this);
        if (WifiProStatusUtils.isWifiProEnabledViaXml(this.myContext) && wifiNative != null) {
            for (WifiConfiguration config : wifiStateMachineUtils.getWifiConfigManager(this).getSavedNetworks()) {
                if (((config.noInternetAccess && !NetworkHistoryUtils.allowWifiConfigRecovery(config.internetHistory)) || WifiProCommonUtils.isOpenAndPortal(config) || WifiProCommonUtils.isOpenAndMaybePortal(config)) && ((this.mSelectedConfig == null || this.mSelectedConfig.networkId != config.networkId) && (this.mWpsCompletedNetId == -1 || this.mWpsCompletedNetId != config.networkId))) {
                    log("DisconnectedState, disable network in supplicant because of no internet, netid = " + config.networkId + ", ssid = " + config.SSID);
                    wifiNative.disableNetwork(config.networkId);
                }
            }
        }
        this.mSelectedConfig = null;
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
                if (WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, MessageUtil.CMD_START_SCAN)) {
                    z = true;
                } else {
                    z = false;
                }
                config.portalNetwork = z;
                if (this.mCurrNetworkHistoryInserted) {
                    config.internetHistory = NetworkHistoryUtils.updateWifiConfigHistory(config.internetHistory, DEFAULT_WIFI_AP_CHANNEL);
                } else {
                    config.internetHistory = NetworkHistoryUtils.insertWifiConfigHistory(config.internetHistory, DEFAULT_WIFI_AP_CHANNEL);
                    this.mCurrNetworkHistoryInserted = true;
                }
                updated = true;
            } else if (unwantedType == 3) {
                config.portalNetwork = true;
                config.noInternetAccess = false;
                if (this.mCurrNetworkHistoryInserted) {
                    config.internetHistory = NetworkHistoryUtils.updateWifiConfigHistory(config.internetHistory, 2);
                } else {
                    config.internetHistory = NetworkHistoryUtils.insertWifiConfigHistory(config.internetHistory, 2);
                    this.mCurrNetworkHistoryInserted = true;
                }
                updated = true;
            }
            if (updated) {
                wifiStateMachineUtils.getWifiConfigManager(this).writeKnownNetworkHistory();
            }
        }
    }

    public void handleValidNetworkInWifiPro(WifiConfiguration config) {
        if (config != null) {
            boolean z;
            config.noInternetAccess = false;
            if (WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, MessageUtil.CMD_START_SCAN)) {
                z = true;
            } else {
                z = false;
            }
            config.portalNetwork = z;
            if (this.mCurrNetworkHistoryInserted) {
                config.internetHistory = NetworkHistoryUtils.updateWifiConfigHistory(config.internetHistory, WIFI_START_EVALUATE_TAG);
            } else {
                config.internetHistory = NetworkHistoryUtils.insertWifiConfigHistory(config.internetHistory, WIFI_START_EVALUATE_TAG);
                this.mCurrNetworkHistoryInserted = true;
            }
            if (config.portalCheckStatus == 2 || config.portalCheckStatus == WIFI_START_EVALUATE_TAG) {
                config.portalCheckStatus = DEFAULT_WIFI_AP_CHANNEL;
            }
            String strDhcpResults = WifiProCommonUtils.dhcpResults2String(wifiStateMachineUtils.getDhcpResults(this), -1);
            if (strDhcpResults != null) {
                config.lastDhcpResults = strDhcpResults;
                if (!isWifiProEvaluatingAP()) {
                    HwSelfCureEngine.getInstance(this.myContext, this).notifyDhcpResultsInternetOk(strDhcpResults);
                }
            }
            config.lastHasInternetTimestamp = System.currentTimeMillis();
        }
    }

    public void saveWpsNetIdInWifiPro(int netId) {
        this.mWpsCompletedNetId = netId;
    }

    public void handleConnectFailedInWifiPro(int netId, int disableReason) {
        if (this.mWifiSwitchOnGoing && disableReason >= 2 && disableReason <= ENTERPRISE_HOTSPOT_THRESHOLD) {
            log("handleConnectFailedInWifiPro, netId = " + netId + ", disableReason = " + disableReason + ", candidate = " + this.mSelectedConfig);
            String failedBssid = null;
            String failedSsid = null;
            int status = -6;
            if (disableReason != 2) {
                status = -7;
            }
            if (this.mSelectedConfig != null) {
                failedBssid = this.mSelectedConfig.BSSID;
                failedSsid = this.mSelectedConfig.SSID;
            }
            sendWifiHandoverCompletedBroadcast(status, failedBssid, failedSsid);
        }
    }

    public void sendWifiHandoverCompletedBroadcast(int statusCode, String bssid, String ssid) {
        if (this.mWifiSwitchOnGoing) {
            this.mWifiSwitchOnGoing = false;
            this.mSelectedConfig = null;
            Intent intent = new Intent();
            if (WifiProStateMachine.getWifiProStateMachineImpl().getNetwoksHandoverType() == WIFI_START_EVALUATE_TAG) {
                intent.setAction(WifiHandover.ACTION_RESPONSE_WIFI_2_WIFI);
            } else {
                intent.setAction(WifiHandover.ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER);
            }
            intent.putExtra(WifiHandover.WIFI_HANDOVER_COMPLETED_STATUS, statusCode);
            intent.putExtra(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID, bssid);
            intent.putExtra(WifiHandover.WIFI_HANDOVER_NETWORK_SSID, ssid);
            this.myContext.sendBroadcastAsUser(intent, UserHandle.ALL, WifiHandover.WIFI_HANDOVER_RECV_PERMISSION);
        }
    }

    public void updateWifiproWifiConfiguration(Message message) {
        if (message != null) {
            WifiConfiguration config = message.obj;
            boolean uiOnly = message.arg1 == WIFI_START_EVALUATE_TAG;
            if (config != null && config.networkId != -1) {
                WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
                wifiConfigManager.updateWifiConfigByWifiPro(config, uiOnly);
                if (config.configKey() != null && config.wifiProNoInternetAccess) {
                    log("updateWifiproWifiConfiguration, noInternetReason = " + config.wifiProNoInternetReason + ", ssid = " + config.SSID);
                    this.lastDhcps.remove(config.configKey());
                }
                wifiConfigManager.writeKnownNetworkHistory();
            }
        }
    }

    public void setWiFiProScanResultList(List<ScanResult> list) {
        if (isWifiProEnabled()) {
            HwIntelligenceWiFiManager.setWiFiProScanResultList(list);
        }
    }

    public boolean isWifiProEvaluatingAP() {
        String str = null;
        boolean z = true;
        if (isWifiProEnabled()) {
            WifiConfiguration connectedConfig = wifiStateMachineUtils.getWifiConfigManager(this).getWifiConfiguration(wifiStateMachineUtils.getLastNetworkId(this));
            StringBuilder append = new StringBuilder().append("isWifiProEvaluatingAP, connectedConfig = ").append(connectedConfig != null ? connectedConfig.SSID : null).append(", selectedConfig = ");
            if (this.mSelectedConfig != null) {
                str = this.mSelectedConfig.SSID;
            }
            log(append.append(str).toString());
            if (this.wifiConnectedBackgroundReason == 2 || this.wifiConnectedBackgroundReason == 3) {
                log("isWifiProEvaluatingAP, wifi connected at background matched, reason = " + this.wifiConnectedBackgroundReason);
                return true;
            } else if (connectedConfig != null) {
                log("isWifiProEvaluatingAP, isTempCreated = " + connectedConfig.isTempCreated + ", evaluating = " + WifiProStateMachine.isWifiEvaluating() + ", wifiConnectedBackgroundReason = " + this.wifiConnectedBackgroundReason);
                if (WifiProStateMachine.isWifiEvaluating() || connectedConfig.isTempCreated) {
                    this.wifiConnectedBackgroundReason = WIFI_START_EVALUATE_TAG;
                    return true;
                }
            } else if (this.mSelectedConfig != null) {
                log("isWifiProEvaluatingAP = " + WifiProStateMachine.isWifiEvaluating() + ", mSelectedConfig isTempCreated = " + this.mSelectedConfig.isTempCreated);
                return !WifiProStateMachine.isWifiEvaluating() ? this.mSelectedConfig.isTempCreated : true;
            } else {
                log("==connectedConfig&mSelectedConfig are null, backgroundReason = " + this.wifiConnectedBackgroundReason);
                if (!WifiProStateMachine.isWifiEvaluating() && this.wifiConnectedBackgroundReason < WIFI_START_EVALUATE_TAG) {
                    z = false;
                }
                return z;
            }
        }
        return false;
    }

    public void updateScanDetailByWifiPro(ScanDetail scanDetail) {
        ScanResult sc = scanDetail.getScanResult();
        if (sc != null) {
            if (isWifiProEnabled()) {
                WifiProConfigStore.updateScanDetailByWifiPro(sc);
            } else {
                sc.internetAccessType = DEFAULT_WIFI_AP_CHANNEL;
                sc.networkQosLevel = DEFAULT_WIFI_AP_CHANNEL;
                sc.networkQosScore = DEFAULT_WIFI_AP_CHANNEL;
            }
        }
    }

    public void tryUseStaticIpForFastConnecting(int lastNid) {
        if (isWifiProEnabled() && lastNid != -1 && this.mSelectedConfig != null && this.mSelectedConfig.configKey() != null && isWifiProEvaluatingAP()) {
            WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this);
            this.mSelectedConfig.lastDhcpResults = (String) this.lastDhcps.get(this.mSelectedConfig.configKey());
            log("tryUseStaticIpForFastConnecting, lastDhcpResults = " + this.mSelectedConfig.lastDhcpResults);
            if (this.mSelectedConfig.lastDhcpResults != null && this.mSelectedConfig.lastDhcpResults.length() > 0 && !wifiConfigManager.isUsingStaticIp(lastNid) && wifiConfigManager.tryUseStaticIpForFastConnecting(lastNid)) {
                this.usingStaticIpConfig = true;
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
        if (!(networkInfo.getExtraInfo() == null || wifiInfo.getSSID() == null || wifiInfo.getSSID().equals("<unknown ssid>"))) {
            networkInfo.setExtraInfo(wifiInfo.getSSID());
        }
        if (state != networkInfo.getDetailedState()) {
            networkInfo.setDetailedState(state, null, wifiInfo.getSSID());
            if (networkAgent != null) {
                networkAgent.updateNetworkConcurrently(networkInfo);
            }
        }
        wifiConfigManager.updateStatus(lastNetworkId, DetailedState.CONNECTED);
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
        if (this.wifiConnectedBackgroundReason == WIFI_START_EVALUATE_TAG) {
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
        } else if (status == WIFI_START_EVALUATE_TAG) {
            this.wifiConnectedBackgroundReason = DEFAULT_WIFI_AP_CHANNEL;
        } else if (status == 3) {
            this.wifiConnectedBackgroundReason = 3;
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, true);
        } else if (status == 5) {
            this.wifiConnectedBackgroundReason = DEFAULT_WIFI_AP_CHANNEL;
        }
    }

    public void updateWifiBackgroudStatus(int msgType) {
        if (msgType == 2) {
            WifiProCommonUtils.setBackgroundConnTag(this.myContext, false);
            this.wifiConnectedBackgroundReason = DEFAULT_WIFI_AP_CHANNEL;
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
            WifiConfiguration connectedConfig = wifiConfigManager.getWifiConfiguration(lastNetworkId);
            if (connectedConfig == null) {
                connectedConfig = this.mSelectedConfig;
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
        if ((!isWifiProEvaluatingAP() || (networkInfo.getDetailedState() != DetailedState.CONNECTING && networkInfo.getDetailedState() != DetailedState.SCANNING && networkInfo.getDetailedState() != DetailedState.AUTHENTICATING && networkInfo.getDetailedState() != DetailedState.OBTAINING_IPADDR && networkInfo.getDetailedState() != DetailedState.CONNECTED)) && !selfCureIgnoreNetworkStateChange(networkInfo)) {
            return false;
        }
        Log.d(WifiproUtils.TAG, "ignoreNetworkStateChange, DetailedState = " + networkInfo.getDetailedState());
        return true;
    }

    public boolean selfCureIgnoreNetworkStateChange(NetworkInfo networkInfo) {
        if (!(isWifiSelfCuring() && this.mWifiBackgroundConnected)) {
            if (!isWifiSelfCuring() || this.mWifiBackgroundConnected || networkInfo.getDetailedState() == DetailedState.CONNECTED) {
                if (!isRenewDhcpSelfCuring() || networkInfo.getDetailedState() == DetailedState.DISCONNECTED) {
                    return false;
                }
            }
        }
        Log.d("HwSelfCureEngine", "selfCureIgnoreNetworkStateChange, detailedState = " + networkInfo.getDetailedState());
        return true;
    }

    private boolean selfCureIgnoreSuppStateChange(SupplicantState state) {
        if (!isWifiSelfCuring() && !isRenewDhcpSelfCuring()) {
            return false;
        }
        Log.d("HwSelfCureEngine", "selfCureIgnoreSuppStateChange, state = " + state);
        return true;
    }

    public boolean ignoreSupplicantStateChange(SupplicantState state) {
        if ((!isWifiProEvaluatingAP() || (state != SupplicantState.SCANNING && state != SupplicantState.ASSOCIATING && state != SupplicantState.AUTHENTICATING && state != SupplicantState.ASSOCIATED && state != SupplicantState.FOUR_WAY_HANDSHAKE && state != SupplicantState.AUTHENTICATING && state != SupplicantState.GROUP_HANDSHAKE && state != SupplicantState.COMPLETED)) && !selfCureIgnoreSuppStateChange(state)) {
            return false;
        }
        Log.d(WifiproUtils.TAG, "ignoreSupplicantStateChange, state = " + state);
        return true;
    }

    private void resetWifiProManualConnect() {
        System.putInt(this.myContext.getContentResolver(), "wifipro_manual_connect_ap", DEFAULT_WIFI_AP_CHANNEL);
    }

    private int getAppUid(String processName) {
        try {
            ApplicationInfo ai = this.myContext.getPackageManager().getApplicationInfo(processName, WIFI_START_EVALUATE_TAG);
            if (ai != null) {
                return ai.uid;
            }
            return AP_CAP_CACHE_COUNT;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return AP_CAP_CACHE_COUNT;
        }
    }

    private void registerForWifiEvaluateChanges() {
        this.myContext.getContentResolver().registerContentObserver(Secure.getUriFor(WIFI_EVALUATE_TAG), false, new AnonymousClass3(null));
    }

    private void handleWiFiConnectedByScanGenie(WifiConfigManager wifiConfigManager) {
        Log.d(TAG, "handleWiFiConnectedByScanGenie");
        if (HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.myContext)) {
            Log.d(TAG, "this is mobile ap,ScanGenie ignor it");
            return;
        }
        WifiConfiguration currentWifiConfig = getCurrentWifiConfiguration();
        if (!(currentWifiConfig == null || currentWifiConfig.isTempCreated)) {
            Log.d(TAG, "mWifiScanGenieController.handleWiFiConnected");
            this.mWifiScanGenieController.handleWiFiConnected(currentWifiConfig, false);
        }
    }

    public void notifyWifiScanResultsAvailable(boolean success) {
        HwSelfCureEngine.getInstance(this.myContext, this).notifyWifiScanResultsAvailable(success);
    }

    public void notifyWifiRoamingStarted() {
        HwWifiRoamingEngine.getInstance(this.myContext, this).notifyWifiRoamingStarted();
    }

    public void notifyWifiRoamingCompleted(String newBssid) {
        HwSelfCureEngine.getInstance(this.myContext, this).notifyWifiRoamingCompleted(newBssid);
        HwWifiRoamingEngine.getInstance(this.myContext, this).notifyWifiRoamingCompleted();
    }

    public boolean isWlanSettingsActivity() {
        List<RunningTaskInfo> runningTaskInfos = this.mActivityManager.getRunningTasks(WIFI_START_EVALUATE_TAG);
        if (!(runningTaskInfos == null || runningTaskInfos.isEmpty())) {
            ComponentName cn = ((RunningTaskInfo) runningTaskInfos.get(DEFAULT_WIFI_AP_CHANNEL)).topActivity;
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
            int i = DEFAULT_WIFI_AP_CHANNEL;
            while (i < dnsesStr.size()) {
                try {
                    dnses.add(Inet4Address.getByName((String) dnsesStr.get(i)));
                    i += WIFI_START_EVALUATE_TAG;
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
            WifiProCommonUtils.setWifiSelfCureStatus(true);
            checkWifiBackgroundStatus();
            selfCureWifiDisable();
            return;
        }
        stopSelfCureDelay(WIFI_START_EVALUATE_TAG, DEFAULT_WIFI_AP_CHANNEL);
    }

    public void startSelfCureWifiReassoc() {
        resetSelfCureParam();
        if (saveCurrentConfig()) {
            this.mWifiSelfCuring.set(true);
            resetSelfCureCandidateLostCnt();
            WifiProCommonUtils.setWifiSelfCureStatus(true);
            checkWifiBackgroundStatus();
            reassociateCommand();
            setSelfCureWifiTimeOut(ENTERPRISE_HOTSPOT_THRESHOLD);
            return;
        }
        stopSelfCureDelay(WIFI_START_EVALUATE_TAG, DEFAULT_WIFI_AP_CHANNEL);
    }

    private boolean saveCurrentConfig() {
        WifiConfiguration currentConfiguration = getCurrentWifiConfiguration();
        if (currentConfiguration == null) {
            stopSelfCureDelay(WIFI_START_EVALUATE_TAG, DEFAULT_WIFI_AP_CHANNEL);
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
            WifiConfiguration wifiConfig = wifiConfigManager.getWifiConfiguration(this.mCurrentConfigurationKey);
            if (wifiConfig != null) {
                this.mCurrentConfigNetId = wifiConfig.networkId;
            }
        }
        logd("updateNetworkId >> configKey=" + this.mCurrentConfigurationKey + " netid=" + this.mCurrentConfigNetId);
    }

    private void resetSelfCureParam() {
        logd("ENTER: resetSelfCureParam");
        this.mWifiSelfCuring.set(false);
        WifiProCommonUtils.setWifiSelfCureStatus(false);
        this.mWifiAlwaysOnBeforeCure = false;
        this.mWifiBackgroundConnected = false;
        this.mCurrentConfigurationKey = null;
        this.mSelfCureWifiLastState = DEFAULT_WIFI_AP_CHANNEL;
        this.mSelfCureNetworkLastState = DetailedState.IDLE;
        this.mSelfCureWifiConnectRetry = DEFAULT_WIFI_AP_CHANNEL;
        removeMessages(131888);
        removeMessages(131889);
        removeMessages(131890);
        removeMessages(131891);
    }

    private void checkWifiBackgroundStatus() {
        boolean z = false;
        NetworkInfo networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
        logd("checkWifiBackgroundStatus: detailstate=" + networkInfo.getDetailedState() + " isMobileDataInactive=" + WifiProCommonUtils.isMobileDataInactive(this.myContext));
        if (!(networkInfo == null || networkInfo.getDetailedState() != DetailedState.VERIFYING_POOR_LINK || WifiProCommonUtils.isMobileDataInactive(this.myContext))) {
            z = true;
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
        boolean scanAvailable = Global.getInt(this.myContext.getContentResolver(), "wifi_scan_always_enabled", DEFAULT_WIFI_AP_CHANNEL) == WIFI_START_EVALUATE_TAG;
        if (isWifiSelfCuring()) {
            if (this.mWifiAlwaysOnBeforeCure && !scanAvailable) {
                logd("enable scan always available");
                Global.putInt(this.myContext.getContentResolver(), "wifi_scan_always_enabled", WIFI_START_EVALUATE_TAG);
            }
        } else if (scanAvailable) {
            logd("disable scan always available");
            this.mWifiAlwaysOnBeforeCure = true;
            Global.putInt(this.myContext.getContentResolver(), "wifi_scan_always_enabled", DEFAULT_WIFI_AP_CHANNEL);
        }
    }

    private void selfCureWifiDisable() {
        HwSelfCureEngine.getInstance(this.myContext, this).requestChangeWifiStatus(false);
        setSelfCureWifiTimeOut(WIFI_START_EVALUATE_TAG);
    }

    private void selfCureWifiEnable() {
        HwSelfCureEngine.getInstance(this.myContext, this).requestChangeWifiStatus(true);
        setSelfCureWifiTimeOut(2);
    }

    private void setSelfCureWifiTimeOut(int wifiSelfCureState) {
        this.mWifiSelfCureState = wifiSelfCureState;
        switch (this.mWifiSelfCureState) {
            case WIFI_START_EVALUATE_TAG /*1*/:
                logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_OFF_TIMEOUT 2000");
                sendMessageDelayed(131888, -1, DEFAULT_WIFI_AP_CHANNEL, 2000);
                break;
            case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_ON_TIMEOUT 3000");
                sendMessageDelayed(131889, -1, DEFAULT_WIFI_AP_CHANNEL, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                break;
            case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                int i;
                if (((PowerManager) this.myContext.getSystemService("power")).isScreenOn()) {
                    i = HwSelfCureUtils.SELFCURE_WIFI_CONNECT_TIMEOUT;
                } else {
                    i = 30000;
                }
                long delayedMs = (long) i;
                logd("selfCureWifiResetCheck send delay messgae CMD_SELFCURE_WIFI_CONNECT_TIMEOUT " + delayedMs);
                sendMessageDelayed(131890, -1, DEFAULT_WIFI_AP_CHANNEL, delayedMs);
                break;
            case ENTERPRISE_HOTSPOT_THRESHOLD /*4*/:
                logd("selfCureWifiResetCheck send delay messgae SCE_WIFI_REASSOC_STATE 12000");
                sendMessageDelayed(131891, -1, DEFAULT_WIFI_AP_CHANNEL, 12000);
                break;
            default:
        }
    }

    public boolean checkSelfCureWifiResult() {
        int i = DEFAULT_WIFI_AP_CHANNEL;
        if (!isWifiSelfCuring()) {
            return false;
        }
        NetworkInfo networkInfo;
        boolean connSucc;
        int wifiState = syncGetWifiState();
        if (this.mSelfCureWifiLastState <= wifiState || this.mWifiSelfCureState == WIFI_START_EVALUATE_TAG) {
            if (this.mSelfCureWifiLastState == wifiState && wifiState == 0) {
            }
            this.mSelfCureWifiLastState = wifiState;
            switch (this.mWifiSelfCureState) {
                case WIFI_START_EVALUATE_TAG /*1*/:
                    if (wifiState == WIFI_START_EVALUATE_TAG) {
                        removeMessages(131888);
                        logd("wifi disabled > CMD_SCE_WIFI_OFF_TIMEOUT msg removed");
                        notifySelfCureComplete(true, DEFAULT_WIFI_AP_CHANNEL);
                        break;
                    }
                    break;
                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                    if (wifiState == 3) {
                        removeMessages(131889);
                        logd("wifi enabled > CMD_SCE_WIFI_ON_TIMEOUT msg removed");
                        notifySelfCureComplete(true, DEFAULT_WIFI_AP_CHANNEL);
                        break;
                    }
                    break;
                case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                    networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
                    if (isDuplicateNetworkState(networkInfo) || networkInfo.getDetailedState() != DetailedState.CONNECTED) {
                        if (networkInfo.getDetailedState() == DetailedState.VERIFYING_POOR_LINK) {
                        }
                    }
                    logd("wifi connect > CMD_SCE_WIFI_CONNECT_TIMEOUT msg removed state=" + networkInfo.getDetailedState());
                    removeMessages(131890);
                    connSucc = isWifiConnectToSameAP();
                    if (!connSucc) {
                        i = -1;
                    }
                    notifySelfCureComplete(connSucc, i);
                    break;
                case ENTERPRISE_HOTSPOT_THRESHOLD /*4*/:
                    networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
                    if ((isDuplicateNetworkState(networkInfo) || networkInfo.getDetailedState() != DetailedState.CONNECTED) && networkInfo.getDetailedState() != DetailedState.VERIFYING_POOR_LINK) {
                        if (!isDuplicateNetworkState(networkInfo) && networkInfo.getDetailedState() == DetailedState.DISCONNECTED) {
                            logd("wifi reassociate > CMD_SCE_WIFI_REASSOC_TIMEOUT msg removed state=" + networkInfo.getDetailedState());
                            removeMessages(131891);
                            notifySelfCureComplete(false, -1);
                            break;
                        }
                    }
                    logd("wifi reassociate > CMD_SCE_WIFI_REASSOC_TIMEOUT msg removed state=" + networkInfo.getDetailedState());
                    removeMessages(131891);
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
        logd("last state =" + this.mSelfCureWifiLastState + "current state=" + wifiState + " user may toggle wifi! stop selfcure");
        sendMessageDelayed(131894, 200);
        exitWifiSelfCure(WIFI_START_EVALUATE_TAG, -1);
        this.mSelfCureWifiLastState = wifiState;
        switch (this.mWifiSelfCureState) {
            case WIFI_START_EVALUATE_TAG /*1*/:
                if (wifiState == WIFI_START_EVALUATE_TAG) {
                    removeMessages(131888);
                    logd("wifi disabled > CMD_SCE_WIFI_OFF_TIMEOUT msg removed");
                    notifySelfCureComplete(true, DEFAULT_WIFI_AP_CHANNEL);
                    break;
                }
                break;
            case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                if (wifiState == 3) {
                    removeMessages(131889);
                    logd("wifi enabled > CMD_SCE_WIFI_ON_TIMEOUT msg removed");
                    notifySelfCureComplete(true, DEFAULT_WIFI_AP_CHANNEL);
                    break;
                }
                break;
            case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
                if (networkInfo.getDetailedState() == DetailedState.VERIFYING_POOR_LINK) {
                    logd("wifi connect > CMD_SCE_WIFI_CONNECT_TIMEOUT msg removed state=" + networkInfo.getDetailedState());
                    removeMessages(131890);
                    connSucc = isWifiConnectToSameAP();
                    if (connSucc) {
                        i = -1;
                    }
                    notifySelfCureComplete(connSucc, i);
                    break;
                }
                break;
            case ENTERPRISE_HOTSPOT_THRESHOLD /*4*/:
                networkInfo = wifiStateMachineUtils.getNetworkInfo(this);
                if (!isDuplicateNetworkState(networkInfo)) {
                    break;
                }
                logd("wifi reassociate > CMD_SCE_WIFI_REASSOC_TIMEOUT msg removed state=" + networkInfo.getDetailedState());
                removeMessages(131891);
                notifySelfCureComplete(false, -1);
                break;
        }
        return true;
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
        WifiQualifiedNetworkSelector selector = wifiStateMachineUtils.getWifiQualifiedNetworkSelector(this);
        if (selector == null || bssid == null) {
            return false;
        }
        return selector.isBssidDisabled(bssid);
    }

    public void resetSelfCureCandidateLostCnt() {
        WifiQualifiedNetworkSelector selector = wifiStateMachineUtils.getWifiQualifiedNetworkSelector(this);
        if (selector != null) {
            selector.resetSelfCureCandidateLostCnt();
        }
    }

    public boolean isWifiSelfCuring() {
        return this.mWifiSelfCuring.get();
    }

    public int getSelfCureNetworkId() {
        return this.mCurrentConfigNetId;
    }

    public void notifySelfCureComplete(boolean success, int reasonCode) {
        if (isWifiSelfCuring()) {
            if (success) {
                handleSelfCureNormal();
            } else {
                handleSelfCureException(reasonCode);
            }
            return;
        }
        logd("notifySelfCureComplete: not Curing!");
        stopSelfCureDelay(WIFI_START_EVALUATE_TAG, DEFAULT_WIFI_AP_CHANNEL);
    }

    public void notifySelfCureNetworkLost() {
        if (hasMessages(131890)) {
            logd("notifySelfCureNetworkLost, Stop Reset");
            removeMessages(131890);
            sendMessage(131890, -2, DEFAULT_WIFI_AP_CHANNEL);
        } else if (hasMessages(131891)) {
            logd("notifySelfCureNetworkLost, Stop Reassociate");
            removeMessages(131891);
            sendMessage(131891, -2, DEFAULT_WIFI_AP_CHANNEL);
        } else {
            logd("notifySelfCureNetworkLost, No delay message found.");
        }
    }

    private void handleSelfCureNormal() {
        switch (this.mWifiSelfCureState) {
            case WIFI_START_EVALUATE_TAG /*1*/:
                logd("handleSelfCureNormal, wifi off OK! -> wifi on");
                selfCureWifiEnable();
                break;
            case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                logd("handleSelfCureNormal, wifi on OK! -> wifi connect");
                setSelfCureWifiTimeOut(3);
                break;
            case MessageUtil.MSG_WIFI_ENABLED /*3*/:
            case ENTERPRISE_HOTSPOT_THRESHOLD /*4*/:
                logd("handleSelfCureNormal, wifi connect/reassoc OK!");
                if (this.mWifiBackgroundConnected) {
                    logd("handleSelfCureNormal, wifiBackgroundConnected, wifiNetworkExplicitlyUnselected");
                    wifiNetworkExplicitlyUnselected();
                }
                stopSelfCureDelay(DEFAULT_WIFI_AP_CHANNEL, MessageUtil.DB_BSSID_MAX_QUANTA);
                break;
            default:
        }
    }

    private void handleSelfCureException(int reasonCode) {
        switch (this.mWifiSelfCureState) {
            case WIFI_START_EVALUATE_TAG /*1*/:
                stopSelfCureDelay(-1, DEFAULT_WIFI_AP_CHANNEL);
                logd("handleSelfCureException, wifi off fail! -> wifi off");
                HwSelfCureEngine.getInstance(this.myContext, this).requestChangeWifiStatus(false);
                break;
            case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                stopSelfCureDelay(-1, DEFAULT_WIFI_AP_CHANNEL);
                logd("handleSelfCureException, wifi on fail! -> wifi on");
                HwSelfCureEngine.getInstance(this.myContext, this).requestChangeWifiStatus(true);
                break;
            case MessageUtil.MSG_WIFI_ENABLED /*3*/:
            case ENTERPRISE_HOTSPOT_THRESHOLD /*4*/:
                logd("handleSelfCureException, wifi connect/reassoc fail! retry= " + this.mSelfCureWifiConnectRetry + ", reason = " + reasonCode);
                if (this.mSelfCureWifiConnectRetry < WIFI_START_EVALUATE_TAG && reasonCode != -2) {
                    this.mSelfCureWifiConnectRetry += WIFI_START_EVALUATE_TAG;
                    autoConnectToNetwork(this.mCurrentConfigNetId, null);
                    setSelfCureWifiTimeOut(3);
                    break;
                }
                int reason;
                if (reasonCode == -2) {
                    reason = -2;
                } else {
                    reason = -1;
                }
                stopSelfCureDelay(reason, DEFAULT_WIFI_AP_CHANNEL);
                if (!this.mWifiBackgroundConnected) {
                    if (reasonCode != -2) {
                        autoConnectToNetwork(this.mCurrentConfigNetId, null);
                    }
                    this.mCurrentConfigNetId = -1;
                    break;
                }
                disconnectCommand();
                break;
            default:
        }
    }

    public void stopSelfCureWifi(int status) {
        log("stopSelfCureWifi, status =" + status);
        if (isWifiSelfCuring()) {
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
        sendMessageDelayed(obtainMessage(131892, status, DEFAULT_WIFI_AP_CHANNEL), (long) delay);
    }

    public void exitWifiSelfCure(int exitedType, int networkId) {
        if (isWifiSelfCuring()) {
            if (networkId == -1 || networkId == getSelfCureNetworkId()) {
                logd("exitWifiSelfCure, CONNECT_NETWORK/FORGET_NETWORK/CLOSE_WIFI stop SCE, type = " + exitedType);
                WifiProCommonUtils.setWifiSelfCureStatus(false);
                HwSelfCureEngine.getInstance(this.myContext, this).notifyWifiDisconnected();
                int status = WIFI_START_EVALUATE_TAG;
                if (exitedType == 151553 || exitedType == 151556) {
                    status = -1;
                } else if (exitedType == WIFI_START_EVALUATE_TAG) {
                    boolean scanAlwaysAvailable = Global.getInt(this.myContext.getContentResolver(), "wifi_scan_always_enabled", DEFAULT_WIFI_AP_CHANNEL) == WIFI_START_EVALUATE_TAG;
                    if (hasMessages(131891) && scanAlwaysAvailable && getCurrentState() == wifiStateMachineUtils.getDisconnectedState(this)) {
                        status = -1;
                    }
                }
                stopSelfCureDelay(status, DEFAULT_WIFI_AP_CHANNEL);
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

    public List<String> handleGetApLinkedStaList() {
        log("HwWifiStateMachine handleGetApLinkedStaList is called");
        try {
            return HwFrameworkFactory.getHwInnerNetworkManager().getApLinkedStaList();
        } catch (Exception e) {
            loge("HwWifiStateMachine Exception in processgetApLinkedStaList()");
            return null;
        }
    }

    public void handleSetSoftapMacFilter(String macFilter) {
        log("HwWifiStateMachine handleSetSoftapMacFilter is called, macFilter =" + macFilter);
        try {
            HwFrameworkFactory.getHwInnerNetworkManager().setSoftapMacFilter(macFilter);
        } catch (Exception e) {
            loge("HwWifiStateMachine Exception in processSetSoftapMacFilter()");
        }
    }

    public void handleSetSoftapDisassociateSta(String mac) {
        log("HwWifiStateMachine handleSetSoftapDisassociateSta is called, mac =" + mac);
        try {
            HwFrameworkFactory.getHwInnerNetworkManager().setSoftapDisassociateSta(mac);
        } catch (Exception e) {
            loge("HwWifiStateMachine Exception in processSetSoftapDisassociateSta()");
        }
    }

    public void handleSetWifiApConfigurationHw() {
        log("HwWifiStateMachine handleSetWifiApConfigurationHw is called");
        try {
            HwFrameworkFactory.getHwInnerNetworkManager().setAccessPointHw(wifiStateMachineUtils.getInterfaceName(this), SOFTAP_IFACE);
        } catch (Exception e) {
            loge("HwWifiStateMachine Exception in processSetWifiApConfigurationHw()");
        }
    }

    public boolean handleWapiFailureEvent(Message message, SupplicantStateTracker mSupplicantStateTracker) {
        Intent intent;
        if (WAPI_AUTHENTICATION_FAILURE_EVENT == message.what) {
            log("Handling WAPI_EVENT, msg [" + message.what + "]");
            intent = new Intent(SUPPLICANT_WAPI_EVENT);
            intent.putExtra("wapi_string", WAPI_EVENT_AUTH_FAIL_CODE);
            this.myContext.sendBroadcast(intent);
            mSupplicantStateTracker.sendMessage(WAPI_AUTHENTICATION_FAILURE_EVENT);
            return true;
        } else if (WAPI_CERTIFICATION_FAILURE_EVENT != message.what) {
            return false;
        } else {
            log("Handling WAPI_EVENT, msg [" + message.what + "]");
            intent = new Intent(SUPPLICANT_WAPI_EVENT);
            intent.putExtra("wapi_string", WAPI_EVENT_CERT_FAIL_CODE);
            this.myContext.sendBroadcast(intent);
            return true;
        }
    }

    public void handleStopWifiRepeater(AsyncChannel wifiP2pChannel) {
        wifiP2pChannel.sendMessage(CMD_STOP_WIFI_REPEATER);
    }

    public boolean isWifiRepeaterStarted() {
        return WIFI_START_EVALUATE_TAG == Global.getInt(this.myContext.getContentResolver(), "wifi_repeater_on", DEFAULT_WIFI_AP_CHANNEL) || 6 == Global.getInt(this.myContext.getContentResolver(), "wifi_repeater_on", DEFAULT_WIFI_AP_CHANNEL);
    }

    public void setWifiRepeaterStoped() {
        Global.putInt(this.myContext.getContentResolver(), "wifi_repeater_on", DEFAULT_WIFI_AP_CHANNEL);
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
        wifiStateMachineUtils.getWifiNative(this).enableHiLinkHandshake(uiEnable, bssid);
    }

    public void sendWpsOkcStartedBroadcast() {
        this.mHiLinkController.sendWpsOkcStartedBroadcast();
    }

    public void saveWpsOkcConfiguration(int connectionNetId, String connectionBssid) {
        this.mHiLinkController.saveWpsOkcConfiguration(connectionNetId, connectionBssid, syncGetScanResultsList());
    }

    public void handleAntenaPreempted() {
        log(getName() + "EVENT_ANT_CORE_ROB");
        String ACTION_WIFI_ANTENNA_PREEMPTED = HwABSUtils.ACTION_WIFI_ANTENNA_PREEMPTED;
        String HUAWEI_BUSSINESS_PERMISSION = HwABSUtils.HUAWEI_BUSSINESS_PERMISSION;
        this.myContext.sendBroadcastAsUser(new Intent(ACTION_WIFI_ANTENNA_PREEMPTED), UserHandle.ALL, HUAWEI_BUSSINESS_PERMISSION);
    }

    public void handleDualbandHandoverFailed(int disableReason) {
        if (this.mWifiSwitchOnGoing && disableReason == 3 && WifiProStateMachine.getWifiProStateMachineImpl().getNetwoksHandoverType() == ENTERPRISE_HOTSPOT_THRESHOLD) {
            log("handleDualbandHandoverFailed, disableReason = " + disableReason + ", candidate = " + this.mSelectedConfig);
            String str = null;
            String failedSsid = null;
            if (this.mSelectedConfig != null) {
                str = this.mSelectedConfig.BSSID;
                failedSsid = this.mSelectedConfig.SSID;
            }
            log("handleDualbandHandoverFailed, sendWifiHandoverCompletedBroadcast, status = " + -7);
            sendWifiHandoverCompletedBroadcast(-7, str, failedSsid);
        }
    }

    public void updateCHRDNS(List<InetAddress> dnsList) {
        HwWifiCHRStateManager hwmCHR = HwWifiCHRStateManagerImpl.getDefault();
        if (hwmCHR != null) {
            hwmCHR.updateDNS(dnsList);
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
            int foundCounter = DEFAULT_WIFI_AP_CHANNEL;
            for (int i = DEFAULT_WIFI_AP_CHANNEL; i < scanResults.size(); i += WIFI_START_EVALUATE_TAG) {
                ScanResult nextResult = (ScanResult) scanResults.get(i);
                String scanSsid = "\"" + nextResult.SSID + "\"";
                String capabilities = nextResult.capabilities;
                if (currentSsid.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(capabilities, configKey)) {
                    foundCounter += WIFI_START_EVALUATE_TAG;
                    if (foundCounter >= ENTERPRISE_HOTSPOT_THRESHOLD) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
