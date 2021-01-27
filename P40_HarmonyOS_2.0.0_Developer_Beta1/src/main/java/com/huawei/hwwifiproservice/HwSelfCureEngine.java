package com.huawei.hwwifiproservice;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwSelfCureEngine extends StateMachine {
    private static final String ADD_REPLACE_DNS_RECORD = "replaceDnsCnt";
    private static final int ARP_DETECTED_FAILED_COUNT = 5;
    private static final String ARP_TEST = "arptest";
    private static final int BASE = 131072;
    private static final int CMD_ARP_FAILED_DETECTED = 139;
    private static final int CMD_AUTO_CONN_FAILED_CURE = 102;
    private static final int CMD_AUTO_CONN_FAILED_DETECT = 101;
    private static final int CMD_BSSID_DHCP_FAILED_EVENT = 135;
    private static final int CMD_CONFIGURED_NETWORK_DELETED = 107;
    private static final int CMD_CURE_CONNECTED_TIMEOUT = 103;
    private static final int CMD_DHCP_OFFER_PKT_RCV = 126;
    private static final int CMD_DHCP_RESULTS_UPDATE = 125;
    private static final int CMD_DNS_FAILED_DETECTED = 133;
    private static final int CMD_DNS_FAILED_MONITOR = 123;
    private static final int CMD_GATEWAY_CHANGED_DETECT = 115;
    private static final int CMD_HTTP_REACHABLE_RCV = 136;
    private static final int CMD_INTERNET_FAILED_SELF_CURE = 112;
    private static final int CMD_INTERNET_FAILURE_DETECTED = 122;
    private static final int CMD_INTERNET_RECOVERY_CONFIRM = 113;
    private static final int CMD_INTERNET_STATUS_DETECT = 111;
    private static final int CMD_INVALID_DHCP_OFFER_EVENT = 130;
    private static final int CMD_INVALID_IP_CONFIRM = 129;
    private static final int CMD_IP_CONFIG_COMPLETED = 117;
    private static final int CMD_IP_CONFIG_LOST_EVENT = 124;
    private static final int CMD_IP_CONFIG_TIMEOUT = 116;
    private static final int CMD_MULTIGW_SELFCURE = 138;
    private static final int CMD_NETWORK_CONNECTED_RCVD = 104;
    private static final int CMD_NETWORK_DISCONNECTED_RCVD = 108;
    private static final int CMD_NETWORK_ROAMING_DETECT = 110;
    private static final int CMD_NEW_RSSI_RCVD = 109;
    private static final int CMD_NEW_SCAN_RESULTS_RCV = 127;
    private static final int CMD_NO_TCP_RX_DETECTED = 121;
    private static final int CMD_P2P_DISCONNECTED_EVENT = 128;
    private static final int CMD_PERIODIC_ARP_DETECTED = 306;
    private static final int CMD_RESETUP_SELF_CURE_MONITOR = 118;
    private static final int CMD_ROUTER_GATEWAY_UNREACHABLE_EVENT = 132;
    private static final int CMD_SELF_CURE_WIFI_FAILED = 120;
    private static final int CMD_SELF_CURE_WIFI_LINK = 114;
    private static final int CMD_SETTINGS_DISPLAY_NO_INTERNET_EVENT = 131;
    private static final int CMD_UPDATE_CONN_SELF_CURE_HISTORY = 119;
    private static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    private static final int CMD_USER_ENTER_WLAN_SETTINGS = 137;
    private static final int CMD_USER_PRESENT_RCVD = 134;
    private static final int CMD_WIFI6_BACKOFF_SELFCURE = 141;
    private static final int CMD_WIFI6_SELFCURE = 140;
    private static final int CMD_WIFI6_WITHOUT_HTC_ARP_FAILED_DETECTED = 310;
    private static final int CMD_WIFI6_WITHOUT_HTC_PERIODIC_ARP_DETECTED = 307;
    private static final int CMD_WIFI6_WITH_HTC_ARP_FAILED_DETECTED = 309;
    private static final int CMD_WIFI6_WITH_HTC_PERIODIC_ARP_DETECTED = 308;
    private static final int CMD_WIFI_DISABLED_RCVD = 105;
    private static final int CMD_WIFI_ENABLED_RCVD = 106;
    private static final int CURE_OUT_OF_DATE_MS = 7200000;
    private static final int DEAUTH_BSSID_CNT = 3;
    private static final int DEFAULT_ARP_DETECTED_MS = 60000;
    private static final int DEFAULT_ARP_TIMEOUT_MS = 1000;
    private static final int DEFAULT_GATEWAY_NUMBER = 1;
    private static final int DELTA_DNS_FAIL_CNT = 3;
    private static final int DELTA_RX_CNT = 0;
    private static final int DHCP_CONFIRM_DELAYED_MS = 500;
    private static final int DHCP_RENEW_TIMEOUT_MS = 6000;
    private static final int DNS_UPDATE_CONFIRM_DELAYED_MS = 1000;
    private static final int ENABLE_NETWORK_RSSI_THR = -75;
    private static final int EVENT_ARP_DETECT = 1133;
    private static final int EVENT_AX_BLACKLIST = 131;
    private static final int EVENT_AX_CLOSE_HTC = 132;
    private static final int FAC_MAC_REASSOC = 2;
    private static final int FAST_ARP_DETECTED_MS = 10000;
    private static final String GATEWAY = "gateway";
    private static final int GRATUITOUS_ARP_TIMEOUT_MS = 100;
    private static final int HANDLE_WIFI_ON_DELAYED_MS = 1000;
    private static final String IFACE = "wlan0";
    private static final int INITIAL_RSSI = -200;
    private static final int INTERNET_DETECT_INTERVAL_MS = 6000;
    private static final int INTERNET_FAILED_INVALID_IP = 305;
    private static final int INTERNET_FAILED_RAND_MAC = 307;
    private static final int INTERNET_FAILED_TYPE_DNS = 303;
    private static final int INTERNET_FAILED_TYPE_GATEWAY = 302;
    private static final int INTERNET_FAILED_TYPE_ROAMING = 301;
    private static final int INTERNET_FAILED_TYPE_TCP = 304;
    private static final int INTERNET_OK = 300;
    private static final int IP_CONFIG_CONFIRM_DELAYED_MS = 2000;
    private static final String KEY_HUAWEI_EMPLOYEE = "\"Huawei-Employee\"WPA_EAP";
    private static final int MULTI_BSSID_NUM = 2;
    private static final int NORMAL_REASSOC = 1;
    private static final String PROP_DISABLE_SELF_CURE = "hw.wifi.disable_self_cure";
    private static final int RAND_MAC_FAIL_EXPIRATION_AGE_MILLIS = 30000;
    private static final int RAND_MAC_REASSOC = 3;
    private static final int REQ_HTTP_DELAYED_MS = 500;
    private static final int SELF_CURE_DELAYED_MS = 100;
    private static final String SELF_CURE_EVENT = "selfCureEvent";
    private static final String SELF_CURE_INTERNET_ERROR = "internetError";
    private static final int SELF_CURE_MONITOR_DELAYED_MS = 2000;
    private static final int SELF_CURE_RAND_MAC_CONNECT_FAIL_MAX_COUNT = 3;
    private static final String SELF_CURE_STOP_USE = "stopUse";
    private static final String SELF_CURE_SUCC_EVENT = "selfCureSuccEvent";
    private static final int SELF_CURE_TIMEOUT_MS = 20000;
    private static final String SELF_CURE_USER_REJECT = "rejected";
    private static final String SELF_CURE_WRONG_PWD = "wrongPassword";
    private static final int SET_STATIC_IP_TIMEOUT_MS = 3000;
    private static final String TAG = "HwSelfCureEngine";
    private static final String UNIQUE_CURE_EVENT = "uniqueCureEvent";
    private static final int WIFI6_ARP_DETECTED_MS = 500;
    private static final long WIFI6_BLACKLIST_TIME_EXPIRED = 172800000;
    private static final int WIFI6_HTC_ARP_DETECTED_MS = 300;
    private static final int WIFI_6_SUPPORT = 2;
    private static int mDisableReason = 0;
    private static HwSelfCureEngine mHwSelfCureEngine = null;
    private static int mSelfCureReason = 0;
    private Map<String, WifiConfiguration> autoConnectFailedNetworks = new HashMap();
    private Map<String, Integer> autoConnectFailedNetworksRssi = new HashMap();
    private int mArpDetectionFailedCnt = 0;
    private State mConnectedMonitorState = new ConnectedMonitorState();
    private long mConnectedTimeMills;
    private String mConnectionCureConfigKey = null;
    private State mConnectionSelfCureState = new ConnectionSelfCureState();
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private final Object mDhcpFailedBssidLock = new Object();
    private ArrayList<String> mDhcpFailedBssids = new ArrayList<>();
    private ArrayList<String> mDhcpFailedConfigKeys = new ArrayList<>();
    private Map<String, String> mDhcpOfferPackets = new HashMap();
    private ArrayList<String> mDhcpResultsTestDone = new ArrayList<>();
    private State mDisconnectedMonitorState = new DisconnectedMonitorState();
    private boolean mHasTestWifi6Reassoc = false;
    private HwWifiProPartManager mHwWifiProPartManager;
    private boolean mInitialized = false;
    private State mInternetSelfCureState = new InternetSelfCureState();
    private boolean mInternetUnknown = false;
    private int mIpConfigLostCnt = 0;
    private boolean mIsCaptivePortalCheckEnabled;
    private boolean mIsHttpRedirected = false;
    private boolean mIsWifi6ArpSuccess = false;
    private AtomicBoolean mIsWifiBackground = new AtomicBoolean(false);
    private boolean mMobileHotspot = false;
    private HwNetworkPropertyChecker mNetworkChecker;
    private WifiConfiguration mNoAutoConnConfig;
    private int mNoAutoConnCounter = 0;
    private int mNoAutoConnReason = -1;
    private int mNoTcpRxCounter = 0;
    private AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private PowerManager mPowerManager;
    private HwRouterInternetDetector mRouterInternetDetector = null;
    private WifiConfiguration mSelfCureConfig = null;
    private AtomicBoolean mSelfCureOngoing = new AtomicBoolean(false);
    private boolean mStaticIpCureSuccess = false;
    private int mUseWithRandMacAddress = 0;
    private Map<String, Wifi6BlackListInfo> mWifi6BlackListCache = new HashMap();
    private State mWifi6SelfCureState = new Wifi6SelfCureState();
    private WifiManager mWifiManager;
    private WifiNative mWifiNative = null;
    private ClientModeImpl mWifiStateMachine;
    private Map<String, CureFailedNetworkInfo> networkCureFailedHistory = new HashMap();
    private WifiProChrUploadManager uploadManager;

    static /* synthetic */ int access$412(HwSelfCureEngine x0, int x1) {
        int i = x0.mNoTcpRxCounter + x1;
        x0.mNoTcpRxCounter = i;
        return i;
    }

    static /* synthetic */ int access$4204(HwSelfCureEngine x0) {
        int i = x0.mNoAutoConnCounter + 1;
        x0.mNoAutoConnCounter = i;
        return i;
    }

    public static synchronized HwSelfCureEngine getInstance(Context context) {
        HwSelfCureEngine hwSelfCureEngine;
        synchronized (HwSelfCureEngine.class) {
            if (mHwSelfCureEngine == null) {
                mHwSelfCureEngine = new HwSelfCureEngine(context);
            }
            hwSelfCureEngine = mHwSelfCureEngine;
        }
        return hwSelfCureEngine;
    }

    public static synchronized HwSelfCureEngine getInstance() {
        HwSelfCureEngine hwSelfCureEngine;
        synchronized (HwSelfCureEngine.class) {
            hwSelfCureEngine = mHwSelfCureEngine;
        }
        return hwSelfCureEngine;
    }

    private HwSelfCureEngine(Context context) {
        super(TAG);
        boolean z = false;
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mNetworkChecker = new HwNetworkPropertyChecker(context, this.mWifiManager, null, true, null, true);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mRouterInternetDetector = HwRouterInternetDetector.getInstance(context, this);
        this.mHwWifiProPartManager = HwWifiProPartManager.getHwWifiProPartManager(this.mContext);
        this.uploadManager = WifiProChrUploadManager.getInstance(this.mContext);
        this.mIsCaptivePortalCheckEnabled = Settings.Global.getInt(this.mContext.getContentResolver(), "captive_portal_mode", 1) != 0 ? true : z;
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        this.mWifiStateMachine = WifiInjector.getInstance().getClientModeImpl();
        addState(this.mDefaultState);
        addState(this.mConnectedMonitorState, this.mDefaultState);
        addState(this.mDisconnectedMonitorState, this.mDefaultState);
        addState(this.mConnectionSelfCureState, this.mDefaultState);
        addState(this.mInternetSelfCureState, this.mDefaultState);
        addState(this.mWifi6SelfCureState, this.mInternetSelfCureState);
        setInitialState(this.mDisconnectedMonitorState);
        start();
        HwSelfCureUtils.initDnsServer();
    }

    public synchronized void setup() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            HwHiLog.d(TAG, false, "setup DONE!", new Object[0]);
            registerReceivers();
        }
    }

    public void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intentFilter.addAction("com.hw.wifipro.action.DHCP_OFFER_INFO");
        intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
        intentFilter.addAction("com.hw.wifipro.action.INVALID_DHCP_OFFER_RCVD");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.huawei.hwwifiproservice.HwSelfCureEngine.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                HwSelfCureEngine.this.handleBroadcastReceiver(intent);
            }
        }, intentFilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBroadcastReceiver(Intent intent) {
        if (intent != null) {
            if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
                if (!intent.getBooleanExtra("resultsUpdated", false)) {
                    return;
                }
                if (getCurrentState() == this.mConnectedMonitorState) {
                    sendMessage(CMD_NEW_SCAN_RESULTS_RCV);
                } else {
                    sendMessage(101);
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                handleNetworkStateChanged(intent);
            } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                if (this.mWifiManager.isWifiEnabled()) {
                    sendMessageDelayed(CMD_WIFI_ENABLED_RCVD, 1000);
                } else {
                    sendMessage(CMD_WIFI_DISABLED_RCVD);
                }
            } else if ("android.net.wifi.RSSI_CHANGED".equals(intent.getAction())) {
                int newRssi = intent.getIntExtra("newRssi", -127);
                if (newRssi != -127) {
                    sendMessage(CMD_NEW_RSSI_RCVD, newRssi, 0);
                }
            } else if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(intent.getAction())) {
                WifiConfiguration config = null;
                Object configTmp = intent.getParcelableExtra("wifiConfiguration");
                if (configTmp instanceof WifiConfiguration) {
                    config = (WifiConfiguration) configTmp;
                }
                if (intent.getIntExtra("changeReason", 0) == 1) {
                    sendMessage(CMD_CONFIGURED_NETWORK_DELETED, config);
                }
            } else if ("com.hw.wifipro.action.DHCP_OFFER_INFO".equals(intent.getAction())) {
                String dhcpResults = intent.getStringExtra("com.hw.wifipro.FLAG_DHCP_OFFER_INFO");
                String ifaceName = intent.getStringExtra("com.hw.wifipro.NETWORK_INTERFACE_NAME");
                if (dhcpResults != null && IFACE.equals(ifaceName)) {
                    sendMessage(CMD_DHCP_OFFER_PKT_RCV, dhcpResults);
                }
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(intent.getAction())) {
                handleP2pDisconnected(intent);
            } else {
                handleBroadcastReceiverAdditional(intent);
            }
        }
    }

    private void handleBroadcastReceiverAdditional(Intent intent) {
        if (intent != null) {
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                if (getCurrentState() == this.mConnectedMonitorState || getCurrentState() == this.mInternetSelfCureState) {
                    if (hasMessages(CMD_PERIODIC_ARP_DETECTED)) {
                        removeMessages(CMD_PERIODIC_ARP_DETECTED);
                    }
                    sendMessageDelayed(CMD_PERIODIC_ARP_DETECTED, 10000);
                }
            } else if ("com.hw.wifipro.action.INVALID_DHCP_OFFER_RCVD".equals(intent.getAction())) {
                String dhcpResults = intent.getStringExtra("com.hw.wifipro.FLAG_DHCP_OFFER_INFO");
                if (dhcpResults != null) {
                    HwHiLog.d(TAG, false, "ACTION_INVALID_DHCP_OFFER_RCVD, dhcpResults = %{private}s", new Object[]{dhcpResults});
                    sendMessageDelayed(CMD_INVALID_DHCP_OFFER_EVENT, dhcpResults, 2000);
                }
            } else if ("android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(intent.getAction())) {
                handleP2pConnected(intent);
            } else if ("android.intent.action.USER_PRESENT".equals(intent.getAction()) && getCurrentState() == this.mDisconnectedMonitorState) {
                sendMessage(CMD_USER_PRESENT_RCVD);
            }
        }
    }

    private void handleNetworkStateChanged(Intent intent) {
        NetworkInfo info = null;
        Object infoTmp = intent.getParcelableExtra("networkInfo");
        if (infoTmp instanceof NetworkInfo) {
            info = (NetworkInfo) infoTmp;
        }
        if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
            sendMessage(CMD_NETWORK_DISCONNECTED_RCVD);
        } else if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            sendMessage(CMD_NETWORK_CONNECTED_RCVD);
        }
    }

    private void handleP2pDisconnected(Intent intent) {
        NetworkInfo p2pNetworkInfo = null;
        Object p2pNetworkInfoTmp = intent.getParcelableExtra("networkInfo");
        if (p2pNetworkInfoTmp instanceof NetworkInfo) {
            p2pNetworkInfo = (NetworkInfo) p2pNetworkInfoTmp;
        }
        if (p2pNetworkInfo != null && p2pNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
            this.mP2pConnected.set(false);
            if (getCurrentState() == this.mInternetSelfCureState) {
                sendMessage(CMD_P2P_DISCONNECTED_EVENT);
            }
        }
    }

    private void handleP2pConnected(Intent intent) {
        int p2pState = intent.getIntExtra("extraState", -1);
        if (p2pState == 1 || p2pState == 2) {
            this.mP2pConnected.set(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNetworkCheckingStatus(String action, String flag, int property) {
        Intent intent = new Intent(action);
        intent.setFlags(67108864);
        intent.putExtra(flag, property);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            if (message.what != HwSelfCureEngine.CMD_DHCP_OFFER_PKT_RCV) {
                return true;
            }
            handleDhcpOfferPacketRcv((String) message.obj);
            return true;
        }

        private void handleDhcpOfferPacketRcv(String dhcpResutls) {
            String gateway;
            Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 44, new Bundle());
            boolean isWifiProEvaluatingAP = false;
            if (result != null) {
                isWifiProEvaluatingAP = result.getBoolean("isWifiProEvaluatingAP");
            }
            if (dhcpResutls != null && HwSelfCureEngine.this.isSuppOnCompletedState() && !isWifiProEvaluatingAP && (gateway = WifiProCommonUtils.dhcpResults2Gateway(dhcpResutls)) != null) {
                HwSelfCureEngine.this.mDhcpOfferPackets.put(gateway.replace("/", ""), dhcpResutls);
            }
        }
    }

    class ConnectedMonitorState extends State {
        private int mConfigAuthType = -1;
        private boolean mGatewayInvalid = false;
        private boolean mHasInternetRecently;
        private boolean mIpv4DnsEnabled;
        private String mLastConnectedBssid;
        private int mLastDnsFailedCounter;
        private int mLastDnsRefuseCounter;
        private int mLastSignalLevel;
        private boolean mMobileHotspot;
        private boolean mPortalUnthenEver;
        private boolean mUserSetStaticIpConfig;
        private boolean mWifiSwitchAllowed;

        ConnectedMonitorState() {
        }

        public void enter() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "==> ##ConnectedMonitorState", new Object[0]);
            this.mLastConnectedBssid = WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager);
            this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
            this.mLastDnsRefuseCounter = HwSelfCureUtils.getCurrentDnsRefuseCounter();
            HwSelfCureEngine.this.mNoTcpRxCounter = 0;
            this.mLastSignalLevel = 0;
            HwSelfCureEngine.this.mArpDetectionFailedCnt = 0;
            this.mHasInternetRecently = false;
            this.mPortalUnthenEver = false;
            HwSelfCureEngine.this.mInternetUnknown = false;
            this.mUserSetStaticIpConfig = false;
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            this.mIpv4DnsEnabled = true;
            this.mWifiSwitchAllowed = false;
            this.mMobileHotspot = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(HwSelfCureEngine.this.mContext);
            WifiInfo wifiInfo = HwSelfCureEngine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mLastSignalLevel = WifiProCommonUtils.getCurrenSignalLevel(wifiInfo);
                HwHiLog.d(HwSelfCureEngine.TAG, false, "ConnectedMonitorState, network = %{public}s, signal = %{public}d, mobileHotspot = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(wifiInfo.getSSID()), Integer.valueOf(this.mLastSignalLevel), String.valueOf(this.mMobileHotspot)});
            }
            if (!HwSelfCureEngine.this.mIsWifiBackground.get() && !setupSelfCureMonitor()) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "ConnectedMonitorState, config is null when connected broadcast received, delay to setup again.", new Object[0]);
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR, 2000);
            }
            HwSelfCureEngine.this.sendBlacklistToDriver();
            HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED, 10000);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == HwSelfCureEngine.CMD_NETWORK_CONNECTED_RCVD) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "ConnectedMonitorState, CMD_NETWORK_CONNECTED_RCVD!", new Object[0]);
                if (HwSelfCureEngine.this.mIsWifiBackground.get()) {
                    HwSelfCureEngine.this.mIsWifiBackground.set(false);
                    enter();
                }
            } else if (i != HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT) {
                if (i == HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_RESETUP_SELF_CURE_MONITOR rcvd", new Object[0]);
                    setupSelfCureMonitor();
                } else if (i == HwSelfCureEngine.CMD_DHCP_RESULTS_UPDATE) {
                    updateDhcpResultsByBssid(WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager), (String) message.obj);
                } else if (i == HwSelfCureEngine.CMD_NEW_SCAN_RESULTS_RCV) {
                    handleNewScanResults();
                } else if (i == HwSelfCureEngine.CMD_INVALID_IP_CONFIRM) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (HwSelfCureEngine.this.isHttpReachable(false)) {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                        HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    } else {
                        int selfCureType = HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2 ? 302 : HwSelfCureEngine.INTERNET_FAILED_INVALID_IP;
                        int unused = HwSelfCureEngine.mSelfCureReason = selfCureType;
                        transitionToSelfCureState(selfCureType);
                    }
                } else if (i != HwSelfCureEngine.CMD_DNS_FAILED_DETECTED) {
                    if (i == HwSelfCureEngine.CMD_HTTP_REACHABLE_RCV) {
                        WifiConfiguration current = WifiproUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
                        if (current != null) {
                            current.internetSelfCureHistory = "0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0";
                            current.validatedInternetAccess = true;
                            current.noInternetAccess = false;
                            current.wifiProNoInternetAccess = false;
                            current.wifiProNoInternetReason = 0;
                            Bundle data = new Bundle();
                            data.putInt("messageWhat", HwSelfCureEngine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS);
                            data.putParcelable("messageObj", current);
                            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 28, data);
                        }
                    } else if (i != HwSelfCureEngine.CMD_ARP_FAILED_DETECTED) {
                        if (i != HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED) {
                            switch (i) {
                                case HwSelfCureEngine.CMD_NETWORK_DISCONNECTED_RCVD /* 108 */:
                                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT)) {
                                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT);
                                    }
                                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR)) {
                                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR);
                                    }
                                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                                    hwSelfCureEngine.transitionTo(hwSelfCureEngine.mDisconnectedMonitorState);
                                    break;
                                case HwSelfCureEngine.CMD_NEW_RSSI_RCVD /* 109 */:
                                    this.mLastSignalLevel = WifiProCommonUtils.getCurrenSignalLevel(HwSelfCureEngine.this.mWifiManager.getConnectionInfo());
                                    break;
                                case HwSelfCureEngine.CMD_NETWORK_ROAMING_DETECT /* 110 */:
                                    if (!HwSelfCureEngine.this.mIsWifiBackground.get()) {
                                        String newBssid = message.obj != null ? (String) message.obj : null;
                                        if (newBssid == null || !newBssid.equals(this.mLastConnectedBssid)) {
                                            if (!this.mUserSetStaticIpConfig) {
                                                updateInternetAccessHistory();
                                                if (!this.mHasInternetRecently && !this.mPortalUnthenEver && !HwSelfCureEngine.this.mInternetUnknown) {
                                                    HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_NETWORK_ROAMING_DETECT rcvd, but no internet access always.", new Object[0]);
                                                    break;
                                                } else {
                                                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT)) {
                                                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT);
                                                    }
                                                    this.mLastConnectedBssid = newBssid;
                                                    DhcpResults dhcpResults = HwSelfCureEngine.this.syncGetDhcpResults();
                                                    if (dhcpResults != null) {
                                                        Bundle data2 = new Bundle();
                                                        data2.putSerializable(HwSelfCureEngine.GATEWAY, dhcpResults.gateway);
                                                        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 75, data2);
                                                        boolean ArpTest = false;
                                                        if (result != null) {
                                                            ArpTest = result.getBoolean(HwSelfCureEngine.ARP_TEST);
                                                        }
                                                        if (ArpTest) {
                                                            HwHiLog.d(HwSelfCureEngine.TAG, false, "last gateway reachable, don't use http-get, gateway unchanged after roaming!", new Object[0]);
                                                            HwSelfCureEngine.this.sendNetworkCheckingStatus("huawei.conn.NETWORK_CONDITIONS_MEASURED", "extra_is_internet_ready", 5);
                                                            break;
                                                        }
                                                    }
                                                    if (!HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_NETWORK_DISCONNECTED_RCVD)) {
                                                        HwHiLog.d(HwSelfCureEngine.TAG, false, "gateway changed or unknow, need to check http response!", new Object[0]);
                                                        HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                                                        int unused2 = HwSelfCureEngine.mSelfCureReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING;
                                                        transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING);
                                                        break;
                                                    }
                                                }
                                            } else {
                                                HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_NETWORK_ROAMING_DETECT rcvd, but user set static ip config, ignore it.", new Object[0]);
                                                break;
                                            }
                                        } else {
                                            HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_NETWORK_ROAMING_DETECT rcvd, but bssid is unchanged, ignore it.", new Object[0]);
                                            break;
                                        }
                                    }
                                    break;
                                default:
                                    switch (i) {
                                        case HwSelfCureEngine.CMD_NO_TCP_RX_DETECTED /* 121 */:
                                            if (!this.mMobileHotspot && !this.mGatewayInvalid && !HwSelfCureEngine.this.mIsWifiBackground.get()) {
                                                updateInternetAccessHistory();
                                                handleNoTcpRxDetected();
                                                break;
                                            }
                                        case HwSelfCureEngine.CMD_INTERNET_FAILURE_DETECTED /* 122 */:
                                            if (((Boolean) message.obj).booleanValue()) {
                                                HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_INTERNET_FAILURE_DETECTED rcvd, delete dhcp cache", new Object[0]);
                                                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 45, new Bundle());
                                            }
                                            if (!this.mMobileHotspot || HwSelfCureEngine.this.isWifi6Network(this.mLastConnectedBssid)) {
                                                handleInternetFailedDetected(message);
                                                break;
                                            }
                                        case HwSelfCureEngine.CMD_DNS_FAILED_MONITOR /* 123 */:
                                            handleDnsFailedMonitor();
                                            break;
                                        default:
                                            return false;
                                    }
                            }
                        } else {
                            HwSelfCureEngine.this.periodicArpDetection();
                        }
                    } else if (!HwSelfCureEngine.this.shouldTransToWifi6SelfCureState(message, this.mLastConnectedBssid)) {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                        if (HwSelfCureEngine.this.isHttpReachable(false)) {
                            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                            HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                        } else {
                            int unused3 = HwSelfCureEngine.mSelfCureReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP;
                            transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP);
                        }
                    }
                } else if (!this.mMobileHotspot && !this.mGatewayInvalid && !HwSelfCureEngine.this.mIsWifiBackground.get()) {
                    updateInternetAccessHistory();
                    handleDnsFailedDetected();
                }
            } else if (HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2 || (this.mHasInternetRecently && WifiProCommonUtils.isEncryptedAuthType(this.mConfigAuthType))) {
                checkHttpResponseAndSelfCure(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT);
            }
            return true;
        }

        public void exit() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "ConnectedMonitorState exit", new Object[0]);
            HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED);
        }

        private void handleNewScanResults() {
            List<ScanResult> scanResults = HwSelfCureEngine.this.mWifiManager.getScanResults();
            List<WifiConfiguration> configNetworks = WifiproUtils.getAllConfiguredNetworks();
            WifiConfiguration config = WifiproUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            this.mWifiSwitchAllowed = config != null && WifiProCommonUtils.isAllowWifiSwitch(scanResults, configNetworks, WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager), WifiProCommonUtils.getCurrentSsid(HwSelfCureEngine.this.mWifiManager), config.configKey(), HwSelfCureEngine.ENABLE_NETWORK_RSSI_THR);
        }

        private boolean setupSelfCureMonitor() {
            WifiConfiguration config = WifiproUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (config == null) {
                return false;
            }
            HwSelfCureEngine.this.mSelfCureConfig = config;
            this.mConfigAuthType = config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType();
            this.mUserSetStaticIpConfig = config.getIpAssignment() != null && config.getIpAssignment() == IpConfiguration.IpAssignment.STATIC;
            HwSelfCureEngine.this.mInternetUnknown = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, (int) HwSelfCureEngine.CMD_CURE_CONNECTED_TIMEOUT);
            updateInternetAccessHistory();
            HwHiLog.d(HwSelfCureEngine.TAG, false, "ConnectedMonitorState, hasInternet = %{public}s, portalUnthen = %{public}s, userSetStaticIp = %{public}s, history empty = %{public}s", new Object[]{String.valueOf(this.mHasInternetRecently), String.valueOf(this.mPortalUnthenEver), String.valueOf(this.mUserSetStaticIpConfig), String.valueOf(HwSelfCureEngine.this.mInternetUnknown)});
            if (!this.mMobileHotspot) {
                DhcpResults dhcpResults = HwSelfCureEngine.this.syncGetDhcpResults();
                this.mGatewayInvalid = dhcpResults == null || dhcpResults.gateway == null;
                HwHiLog.d(HwSelfCureEngine.TAG, false, "ConnectedMonitorState, gatewayInvalid = %{public}s", new Object[]{String.valueOf(this.mGatewayInvalid)});
                if (HwSelfCureEngine.this.mIsWifiBackground.get() || HwSelfCureEngine.this.mStaticIpCureSuccess || ((!this.mHasInternetRecently && !HwSelfCureEngine.this.mInternetUnknown) || !HwSelfCureEngine.this.isIpAddressInvalid())) {
                    StaticIpConfiguration staticIpConfig = WifiProCommonUtils.dhcpResults2StaticIpConfig(config.lastDhcpResults);
                    if ((this.mUserSetStaticIpConfig || dhcpResults == null || staticIpConfig == null || dhcpResults.gateway == null || staticIpConfig.gateway == null || staticIpConfig.ipAddress == null || staticIpConfig.dnsServers == null) ? false : true) {
                        String currentGateway = dhcpResults.gateway.getHostAddress();
                        String lastGateway = staticIpConfig.gateway.getHostAddress();
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "ConnectedMonitorState, currentGateway = %{public}s, lastGateway = %{public}s", new Object[]{WifiProCommonUtils.safeDisplayIpAddress(currentGateway), WifiProCommonUtils.safeDisplayIpAddress(lastGateway)});
                        if (!HwSelfCureEngine.this.mStaticIpCureSuccess && currentGateway != null && lastGateway != null && !currentGateway.equals(lastGateway)) {
                            HwHiLog.d(HwSelfCureEngine.TAG, false, "current gateway is different with history gateway that has internet.", new Object[0]);
                            HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT, 300);
                            return true;
                        }
                    } else if (TextUtils.isEmpty(config.lastDhcpResults) && HwSelfCureEngine.this.mInternetUnknown) {
                        HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT, 2000);
                        return true;
                    }
                } else {
                    HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INVALID_IP_CONFIRM, 2000);
                    return true;
                }
            }
            if (!this.mMobileHotspot && !HwSelfCureEngine.this.mIsWifiBackground.get() && !HwSelfCureEngine.this.mStaticIpCureSuccess && this.mHasInternetRecently) {
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_DNS_FAILED_MONITOR, 6000);
            }
            return true;
        }

        private boolean hasIpv4Dnses(DhcpResults dhcpResults) {
            if (dhcpResults == null || dhcpResults.dnsServers == null || dhcpResults.dnsServers.size() == 0) {
                return false;
            }
            for (int i = 0; i < dhcpResults.dnsServers.size(); i++) {
                InetAddress dns = (InetAddress) dhcpResults.dnsServers.get(i);
                if (!(dns == null || dns.getHostAddress() == null || !(dns instanceof Inet4Address))) {
                    return true;
                }
            }
            return false;
        }

        private void handleDnsFailedMonitor() {
            if (this.mLastSignalLevel <= 1) {
                this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_DNS_FAILED_MONITOR, 6000);
                return;
            }
            int currentDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
            int deltaFailedDns = currentDnsFailedCounter - this.mLastDnsFailedCounter;
            this.mLastDnsFailedCounter = currentDnsFailedCounter;
            int currentDnsRefuseCounter = HwSelfCureUtils.getCurrentDnsRefuseCounter();
            int deltaDnsResfuseDns = currentDnsRefuseCounter - this.mLastDnsRefuseCounter;
            this.mLastDnsRefuseCounter = currentDnsRefuseCounter;
            if (this.mGatewayInvalid) {
                return;
            }
            if (deltaFailedDns >= 2 || deltaDnsResfuseDns >= 2) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (HwSelfCureEngine.this.isHttpReachable(true)) {
                    HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                    return;
                }
                HwHiLog.d(HwSelfCureEngine.TAG, false, "handleDnsFailedMonitor, deltaFailedDns = %{public}d, and HTTP unreachable, transition to SelfCureState.", new Object[]{Integer.valueOf(deltaFailedDns)});
                int unused = HwSelfCureEngine.mSelfCureReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS;
                transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
            }
        }

        private void checkHttpResponseAndSelfCure(int eventType) {
            HwSelfCureEngine.this.mSelfCureOngoing.set(true);
            if (!HwSelfCureEngine.this.isHttpReachable(false)) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "checkHttpResponseAndSelfCure, HTTP unreachable for eventType = %{public}d, dhcp offer size = %{public}d", new Object[]{Integer.valueOf(eventType), Integer.valueOf(HwSelfCureEngine.this.mDhcpOfferPackets.size())});
                int internetFailedReason = 300;
                if (eventType == HwSelfCureEngine.CMD_NETWORK_ROAMING_DETECT) {
                    internetFailedReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING;
                } else if (eventType == HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT) {
                    internetFailedReason = 302;
                }
                int unused = HwSelfCureEngine.mSelfCureReason = internetFailedReason;
                transitionToSelfCureState(internetFailedReason);
                return;
            }
            HwHiLog.d(HwSelfCureEngine.TAG, false, "checkHttpResponseAndSelfCure, HTTP reachable for eventType = %{public}d", new Object[]{Integer.valueOf(eventType)});
            this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
            HwSelfCureEngine.this.mNoTcpRxCounter = 0;
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
        }

        private void handleDnsFailedDetected() {
            if (this.mLastSignalLevel > 2) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "handleDnsFailedDetected, start scan and parse the context for wifi 2 wifi.", new Object[0]);
                HwSelfCureEngine.this.startScan();
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (HwSelfCureEngine.this.isHttpReachable(false)) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    return;
                }
                handleNewScanResults();
                if (this.mWifiSwitchAllowed) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "handleDnsFailedDetected, notify WLAN+ to do wifi swtich first.", new Object[0]);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    return;
                }
                HwHiLog.d(HwSelfCureEngine.TAG, false, "handleDnsFailedDetected, HTTP unreachable, transition to SelfCureState.", new Object[0]);
                int unused = HwSelfCureEngine.mSelfCureReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS;
                transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
            }
        }

        private void handleNoTcpRxDetected() {
            if (this.mLastSignalLevel > 2) {
                HwSelfCureEngine.access$412(HwSelfCureEngine.this, 1);
                if (HwSelfCureEngine.this.mNoTcpRxCounter == 1) {
                    this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "handleNoTcpRxDetected, start scan and parse the context for wifi 2 wifi.", new Object[0]);
                    HwSelfCureEngine.this.startScan();
                } else if (HwSelfCureEngine.this.mNoTcpRxCounter != 2 || !this.mWifiSwitchAllowed) {
                    if (this.mHasInternetRecently || this.mPortalUnthenEver) {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                        if (HwSelfCureEngine.this.isHttpReachable(true)) {
                            HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                            return;
                        }
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "handleNoTcpRxDetected, HTTP unreachable, transition to SelfCureState.", new Object[0]);
                        int unused = HwSelfCureEngine.mSelfCureReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP;
                        transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP);
                    }
                } else if (HwSelfCureEngine.this.isHttpReachable(false)) {
                    this.mWifiSwitchAllowed = false;
                    HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                } else {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "handleNoTcpRxDetected, notify WLAN+ to do wifi swtich first.", new Object[0]);
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void requestReassocWithFactoryMac() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "handleInternetFailedDetected, wifi has no internet when connected.", new Object[0]);
            HwSelfCureEngine.this.mUseWithRandMacAddress = 2;
            int unused = HwSelfCureEngine.mSelfCureReason = 307;
            transitionToSelfCureState(307);
        }

        private void handleInternetFailedDetected(Message message) {
            if (!isWifi6SelfCureNeed(message)) {
                if (!(message.obj instanceof Boolean) || !((Boolean) message.obj).booleanValue() || !HwSelfCureEngine.this.isNeedWifiReassocUseDeviceMac()) {
                    boolean z = HwSelfCureEngine.this.mStaticIpCureSuccess;
                    int i = HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP;
                    if (z || !((Boolean) message.obj).booleanValue()) {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                        if (HwSelfCureEngine.this.isHttpReachable(false)) {
                            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                            HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                            if (HwSelfCureEngine.this.mIsHttpRedirected) {
                                HwSelfCureEngine.this.notifyHttpRedirectedForWifiPro();
                            } else {
                                HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                            }
                        } else {
                            String errReason = this.mPortalUnthenEver ? "ERROR_PORTAL" : "OTHER";
                            Bundle data = new Bundle();
                            data.putString("errReason", errReason);
                            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 60, data);
                            Bundle bundledata = new Bundle();
                            bundledata.putInt("reason", 0);
                            bundledata.putBoolean("succ", false);
                            bundledata.putBoolean("isPortalAP", this.mPortalUnthenEver);
                            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 32, bundledata);
                            HwHiLog.d(HwSelfCureEngine.TAG, false, "handleInternetFailedDetected, HTTP unreachable, transition to SelfCureState.", new Object[0]);
                            int currentDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                            int deltaFailedDns = currentDnsFailedCounter - this.mLastDnsFailedCounter;
                            this.mLastDnsFailedCounter = currentDnsFailedCounter;
                            int currentDnsRefuseCounter = HwSelfCureUtils.getCurrentDnsRefuseCounter();
                            int deltaDnsResfuseDns = currentDnsRefuseCounter - this.mLastDnsRefuseCounter;
                            this.mLastDnsRefuseCounter = currentDnsRefuseCounter;
                            if (deltaFailedDns >= 2 || deltaDnsResfuseDns >= 2) {
                                i = HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS;
                            }
                            int unused = HwSelfCureEngine.mSelfCureReason = i;
                            transitionToSelfCureState(HwSelfCureEngine.mSelfCureReason);
                        }
                    } else if (this.mHasInternetRecently || this.mPortalUnthenEver || HwSelfCureEngine.this.mInternetUnknown) {
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "handleInternetFailedDetected, wifi has no internet when connected.", new Object[0]);
                        int unused2 = HwSelfCureEngine.mSelfCureReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS;
                        transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
                    } else if (!HwSelfCureEngine.this.mInternetUnknown || !HwSelfCureEngine.this.multiGateway()) {
                        HwHiLog.w(HwSelfCureEngine.TAG, false, "handleInternetFailedDetected, There is not a expectant condition !", new Object[0]);
                    } else {
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "handleInternetFailedDetected, multi gateway due to no internet access.", new Object[0]);
                        int unused3 = HwSelfCureEngine.mSelfCureReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP;
                        transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP);
                    }
                } else {
                    requestReassocWithFactoryMac();
                }
            }
        }

        private boolean isWifi6SelfCureNeed(Message message) {
            if (message == null) {
                return false;
            }
            boolean z = true;
            if (HwSelfCureEngine.this.shouldTransToWifi6SelfCureState(message, this.mLastConnectedBssid)) {
                return true;
            }
            if (!HwSelfCureEngine.this.mInternetUnknown) {
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                if (message.arg1 != 1) {
                    z = false;
                }
                hwSelfCureEngine.mInternetUnknown = z;
            }
            return false;
        }

        private void transitionToSelfCureState(int reason) {
            boolean z = this.mMobileHotspot;
            if (!z || reason == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                DhcpResults dhcpResults = HwSelfCureEngine.this.syncGetDhcpResults();
                this.mIpv4DnsEnabled = hasIpv4Dnses(dhcpResults);
                this.mGatewayInvalid = dhcpResults == null || dhcpResults.gateway == null;
                if (SystemProperties.getBoolean(HwSelfCureEngine.PROP_DISABLE_SELF_CURE, false) || !HwSelfCureEngine.this.mIsCaptivePortalCheckEnabled || !this.mIpv4DnsEnabled || this.mGatewayInvalid || "factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "transitionToSelfCureState, don't support SCE, do nothing or mIpv4DnsEnabled =%{public}s", new Object[]{String.valueOf(this.mIpv4DnsEnabled)});
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    return;
                }
                Message dmsg = Message.obtain();
                dmsg.what = HwSelfCureEngine.CMD_INTERNET_FAILED_SELF_CURE;
                dmsg.arg1 = reason;
                HwSelfCureEngine.this.sendMessageDelayed(dmsg, 100);
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.transitionTo(hwSelfCureEngine.mInternetSelfCureState);
                return;
            }
            HwHiLog.d(HwSelfCureEngine.TAG, false, "transitionToSelfCureState, don't support SCE, do nothing or mMobileHotspot =%{public}s", new Object[]{String.valueOf(z)});
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
        }

        private void updateInternetAccessHistory() {
            WifiConfiguration config = WifiproUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (config != null) {
                this.mHasInternetRecently = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
                this.mPortalUnthenEver = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102);
            }
        }

        private void updateDhcpResultsByBssid(String bssid, String dhcpResults) {
            if (bssid != null && dhcpResults != null && HwSelfCureEngine.this.mHwWifiProPartManager != null) {
                HwSelfCureEngine.this.mHwWifiProPartManager.updateDhcpResultsByBssid(bssid, dhcpResults);
            }
        }
    }

    class DisconnectedMonitorState extends State {
        private boolean mSetStaticIpConfig;

        DisconnectedMonitorState() {
        }

        public void enter() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "==> ##DisconnectedMonitorState", new Object[0]);
            HwSelfCureEngine.this.mStaticIpCureSuccess = false;
            HwSelfCureEngine.this.mIsWifi6ArpSuccess = false;
            HwSelfCureEngine.this.mHasTestWifi6Reassoc = false;
            HwSelfCureEngine.this.mNoAutoConnCounter = 0;
            HwSelfCureEngine.this.mNoAutoConnReason = -1;
            HwSelfCureEngine.this.mNoAutoConnConfig = null;
            HwSelfCureEngine.this.mSelfCureConfig = null;
            HwSelfCureEngine.this.mConnectionCureConfigKey = null;
            HwSelfCureEngine.this.mIsWifiBackground.set(false);
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            this.mSetStaticIpConfig = false;
            HwSelfCureEngine.this.mConnectedTimeMills = 0;
            HwSelfCureEngine.this.mDhcpOfferPackets.clear();
            HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
            HwSelfCureEngine.this.mRouterInternetDetector.notifyDisconnected();
            HwSelfCureEngine.this.mUseWithRandMacAddress = 0;
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 101) {
                WifiConfiguration wifiConfiguration = null;
                if (i != 102) {
                    if (i != HwSelfCureEngine.CMD_IP_CONFIG_COMPLETED) {
                        if (i == HwSelfCureEngine.CMD_IP_CONFIG_LOST_EVENT) {
                            String currentBssid = WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager);
                            if (message.obj != null) {
                                wifiConfiguration = (WifiConfiguration) message.obj;
                            }
                            this.mSetStaticIpConfig = handleIpConfigLost(currentBssid, wifiConfiguration);
                            return true;
                        } else if (i == HwSelfCureEngine.CMD_USER_ENTER_WLAN_SETTINGS) {
                            List<Integer> enabledReasons = new ArrayList<>();
                            enabledReasons.add(3);
                            enabledReasons.add(2);
                            enabledReasons.add(4);
                            HwSelfCureEngine.this.enableAllNetworksByEnterSettings(enabledReasons);
                            return true;
                        } else if (i == HwSelfCureEngine.CMD_USER_PRESENT_RCVD) {
                            handleUserPresentEvent();
                            return true;
                        } else if (i != HwSelfCureEngine.CMD_BSSID_DHCP_FAILED_EVENT) {
                            switch (i) {
                                case HwSelfCureEngine.CMD_NETWORK_CONNECTED_RCVD /* 104 */:
                                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT)) {
                                        HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_INVALID_DHCP_OFFER_EVENT msg removed because of rcv other Dhcp Offer.", new Object[0]);
                                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT);
                                    }
                                    HwSelfCureEngine.this.handleNetworkConnected();
                                    return true;
                                case HwSelfCureEngine.CMD_WIFI_DISABLED_RCVD /* 105 */:
                                    HwSelfCureEngine.this.handleWifiDisabled(false);
                                    return true;
                                case HwSelfCureEngine.CMD_WIFI_ENABLED_RCVD /* 106 */:
                                    HwSelfCureEngine.this.handleWifiEnabled();
                                    return true;
                                case HwSelfCureEngine.CMD_CONFIGURED_NETWORK_DELETED /* 107 */:
                                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                                    if (message.obj != null) {
                                        wifiConfiguration = (WifiConfiguration) message.obj;
                                    }
                                    hwSelfCureEngine.handleNetworkRemoved(wifiConfiguration);
                                    return true;
                                default:
                                    return false;
                            }
                        } else {
                            handleBssidDhcpFailed((WifiConfiguration) message.obj);
                            return true;
                        }
                    } else if (!this.mSetStaticIpConfig) {
                        return true;
                    } else {
                        this.mSetStaticIpConfig = false;
                        HwSelfCureEngine.this.updateScCHRCount(12);
                        HwSelfCureEngine.this.updateScCHRCount(25);
                        HwSelfCureEngine.this.requestArpConflictTest(HwSelfCureEngine.this.syncGetDhcpResults());
                        return true;
                    }
                } else if (HwSelfCureUtils.isOnWlanSettings(HwSelfCureEngine.this.mContext)) {
                    return true;
                } else {
                    if (message.obj != null) {
                        wifiConfiguration = (WifiConfiguration) message.obj;
                    }
                    trySelfCureSelectedNetwork(wifiConfiguration);
                    return true;
                }
            } else if (HwSelfCureEngine.this.isConnectingOrConnected() || HwSelfCureUtils.isOnWlanSettings(HwSelfCureEngine.this.mContext)) {
                return true;
            } else {
                List<ScanResult> scanResults = HwSelfCureEngine.this.mWifiManager.getScanResults();
                updateAutoConnFailedNetworks(scanResults);
                HwSelfCureUtils.selectDisabledNetworks(scanResults, WifiproUtils.getAllConfiguredNetworks(), HwSelfCureEngine.this.autoConnectFailedNetworks, HwSelfCureEngine.this.autoConnectFailedNetworksRssi);
                selectHighestFailedNetworkAndCure();
                return true;
            }
        }

        private void handleUserPresentEvent() {
            if (HwSelfCureEngine.this.mWifiManager != null && HwSelfCureEngine.this.mWifiManager.isWifiEnabled() && !HwSelfCureEngine.this.isConnectingOrConnected() && !WifiProCommonUtils.isQueryActivityMatched(HwSelfCureEngine.this.mContext, WifiProCommonUtils.HUAWEI_SETTINGS_WLAN)) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "ENTER: handleUserPresentEvent()", new Object[0]);
                List<Integer> enabledReasons = new ArrayList<>();
                enabledReasons.add(2);
                enabledReasons.add(3);
                enabledReasons.add(4);
                HwSelfCureEngine.this.enableAllNetworksByReason(enabledReasons, true);
            }
        }

        private boolean handleIpConfigLost(String bssid, WifiConfiguration config) {
            if (bssid == null || config == null) {
                return false;
            }
            String dhcpResults = null;
            if (HwSelfCureEngine.this.mHwWifiProPartManager != null) {
                dhcpResults = HwSelfCureEngine.this.mHwWifiProPartManager.syncQueryDhcpResultsByBssid(bssid);
            }
            if (dhcpResults == null) {
                dhcpResults = config.lastDhcpResults;
            }
            if (dhcpResults == null) {
                return false;
            }
            HwSelfCureEngine.this.requestUseStaticIpConfig(WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResults));
            return true;
        }

        private void handleBssidDhcpFailed(WifiConfiguration config) {
            synchronized (HwSelfCureEngine.this.mDhcpFailedBssidLock) {
                String bssid = WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager);
                if (bssid != null && !HwSelfCureEngine.this.mDhcpFailedBssids.contains(bssid)) {
                    int bssidCnt = WifiProCommonUtils.getBssidCounter(config, HwSelfCureEngine.this.getScanResults());
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "handleBssidDhcpFailed, bssidCnt = %{public}d", new Object[]{Integer.valueOf(bssidCnt)});
                    if (bssidCnt >= 2) {
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "handleBssidDhcpFailed, add key = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                        HwSelfCureEngine.this.mDhcpFailedBssids.add(bssid);
                        if (config.configKey() != null && !HwSelfCureEngine.this.mDhcpFailedConfigKeys.contains(config.configKey())) {
                            HwSelfCureEngine.this.mDhcpFailedConfigKeys.add(config.configKey());
                        }
                    }
                }
            }
        }

        private void updateAutoConnFailedNetworks(List<ScanResult> scanResults) {
            for (String itemRefreshedNetworksKey : HwSelfCureUtils.getRefreshedCureFailedNetworks(HwSelfCureEngine.this.networkCureFailedHistory)) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "updateAutoConnFailedNetworks, refreshed cure failed network, currKey = %{private}s", new Object[]{itemRefreshedNetworksKey});
                HwSelfCureEngine.this.networkCureFailedHistory.remove(itemRefreshedNetworksKey);
            }
            for (String item : HwSelfCureUtils.searchUnstableNetworks(HwSelfCureEngine.this.autoConnectFailedNetworks, scanResults)) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "updateAutoConnFailedNetworks, remove it due to signal unstable, currKey = %{private}s", new Object[]{item});
                HwSelfCureEngine.this.autoConnectFailedNetworks.remove(item);
                HwSelfCureEngine.this.autoConnectFailedNetworksRssi.remove(item);
            }
        }

        private void selectHighestFailedNetworkAndCure() {
            if (HwSelfCureEngine.this.autoConnectFailedNetworks.size() == 0) {
                HwSelfCureEngine.this.mNoAutoConnCounter = 0;
            } else if (HwSelfCureEngine.access$4204(HwSelfCureEngine.this) < 3) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "selectHighestFailedNetworkAndCure, MAX_FAILED_CURE unmatched, wait more time for self cure.", new Object[0]);
            } else {
                WifiConfiguration bestSelfCureCandidate = HwSelfCureUtils.selectHighestFailedNetwork(HwSelfCureEngine.this.networkCureFailedHistory, HwSelfCureEngine.this.autoConnectFailedNetworks, HwSelfCureEngine.this.autoConnectFailedNetworksRssi);
                if (bestSelfCureCandidate != null) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "selectHighestFailedNetworkAndCure, delay 1s to self cure the selected candidate = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(bestSelfCureCandidate.getPrintableSsid())});
                    Message dmsg = Message.obtain();
                    dmsg.what = 102;
                    dmsg.obj = bestSelfCureCandidate;
                    HwSelfCureEngine.this.sendMessageDelayed(dmsg, 100);
                }
            }
        }

        private void trySelfCureSelectedNetwork(WifiConfiguration config) {
            if (config != null && config.networkId != -1 && !HwSelfCureEngine.this.isConnectingOrConnected()) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "ENTER: trySelfCureSelectedNetwork(), config = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                if (WifiCommonUtils.doesNotWifiConnectRejectByCust(config.getNetworkSelectionStatus(), config.SSID, HwSelfCureEngine.this.mContext)) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "trySelfCureSelectedNetwork can not connect wifi", new Object[0]);
                    return;
                }
                if (WifiProCommonUtils.isWifiProSwitchOn(HwSelfCureEngine.this.mContext)) {
                    if (WifiProCommonUtils.isOpenAndPortal(config) || WifiProCommonUtils.isOpenAndMaybePortal(config)) {
                        HwSelfCureEngine.this.setWifiBackgroundReason(0);
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "self cure at background, due to [maybe] portal, candidate = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                    } else if (config.noInternetAccess && !WifiProCommonUtils.allowWifiConfigRecovery(config.internetHistory)) {
                        HwSelfCureEngine.this.setWifiBackgroundReason(3);
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "trySelfCureSelectedNetwork, self cure at background, due to no internet, candidate = %{private}s", new Object[]{config.configKey()});
                    }
                }
                Bundle data = new Bundle();
                data.putInt("networkId", config.networkId);
                data.putInt("CallingUid", Binder.getCallingUid());
                data.putString("bssid", null);
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 22, data);
                int disableReason = config.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                int unused = HwSelfCureEngine.mDisableReason = disableReason;
                HwHiLog.d(HwSelfCureEngine.TAG, false, "disableReason is: %{public}d", new Object[]{Integer.valueOf(disableReason)});
                updateSelfCure(disableReason);
                HwSelfCureEngine.this.mConnectionCureConfigKey = config.configKey();
                Message dmsg = Message.obtain();
                dmsg.what = HwSelfCureEngine.CMD_CURE_CONNECTED_TIMEOUT;
                dmsg.obj = config.configKey();
                HwSelfCureEngine.this.sendMessageDelayed(dmsg, 20000);
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.transitionTo(hwSelfCureEngine.mConnectionSelfCureState);
            }
        }

        private void updateSelfCure(int disableReason) {
            if (HwSelfCureEngine.this.uploadManager != null) {
                if (disableReason == 11) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.SELF_CURE_EVENT, HwSelfCureEngine.SELF_CURE_STOP_USE);
                } else if (disableReason == 2) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.SELF_CURE_EVENT, HwSelfCureEngine.SELF_CURE_USER_REJECT);
                } else if (disableReason == 13) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.SELF_CURE_EVENT, HwSelfCureEngine.SELF_CURE_WRONG_PWD);
                } else if (disableReason == 4) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.SELF_CURE_EVENT, HwSelfCureEngine.SELF_CURE_INTERNET_ERROR);
                } else {
                    HwHiLog.w(HwSelfCureEngine.TAG, false, "selfCureEvent StopUse, Rejected, WrongPassword, InternetError is not here", new Object[0]);
                }
            }
        }
    }

    class ConnectionSelfCureState extends State {
        ConnectionSelfCureState() {
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            WifiConfiguration wifiConfiguration = null;
            String str = null;
            if (i == HwSelfCureEngine.CMD_CONFIGURED_NETWORK_DELETED) {
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                if (message.obj != null) {
                    wifiConfiguration = (WifiConfiguration) message.obj;
                }
                hwSelfCureEngine.handleNetworkRemoved(wifiConfiguration);
            } else if (i == HwSelfCureEngine.CMD_NETWORK_DISCONNECTED_RCVD) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_NETWORK_DISCONNECTED_RCVD during connection self cure state.", new Object[0]);
                if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_UPDATE_CONN_SELF_CURE_HISTORY)) {
                    HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_UPDATE_CONN_SELF_CURE_HISTORY);
                }
                handleConnSelfCureFailed(HwSelfCureEngine.this.mConnectionCureConfigKey);
            } else if (i != HwSelfCureEngine.CMD_UPDATE_CONN_SELF_CURE_HISTORY) {
                switch (i) {
                    case HwSelfCureEngine.CMD_CURE_CONNECTED_TIMEOUT /* 103 */:
                        if (message.obj != null) {
                            str = (String) message.obj;
                        }
                        handleConnSelfCureFailed(str);
                        break;
                    case HwSelfCureEngine.CMD_NETWORK_CONNECTED_RCVD /* 104 */:
                        if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_CURE_CONNECTED_TIMEOUT)) {
                            HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_CURE_CONNECTED_TIMEOUT msg removed", new Object[0]);
                            HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_CURE_CONNECTED_TIMEOUT);
                        }
                        updateSelfCureSucc();
                        HwSelfCureEngine.this.handleNetworkConnected();
                        break;
                    case HwSelfCureEngine.CMD_WIFI_DISABLED_RCVD /* 105 */:
                        if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_CURE_CONNECTED_TIMEOUT)) {
                            HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_CURE_CONNECTED_TIMEOUT msg removed", new Object[0]);
                            HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_CURE_CONNECTED_TIMEOUT);
                        }
                        HwSelfCureEngine.this.handleWifiDisabled(true);
                        break;
                    default:
                        return false;
                }
            } else {
                HwSelfCureEngine.this.updateConnSelfCureFailedHistory();
            }
            return true;
        }

        private void handleConnSelfCureFailed(String configKey) {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "ENTER: handleConnSelfCureFailed(), configKey = %{private}s", new Object[]{configKey});
            if (configKey != null) {
                HwSelfCureEngine.this.mNoAutoConnCounter = 0;
                HwSelfCureEngine.this.autoConnectFailedNetworks.clear();
                HwSelfCureEngine.this.autoConnectFailedNetworksRssi.clear();
                CureFailedNetworkInfo cureHistory = (CureFailedNetworkInfo) HwSelfCureEngine.this.networkCureFailedHistory.get(configKey);
                if (cureHistory != null) {
                    cureHistory.cureFailedCounter++;
                    cureHistory.lastCureFailedTime = System.currentTimeMillis();
                } else {
                    HwSelfCureEngine.this.networkCureFailedHistory.put(configKey, new CureFailedNetworkInfo(configKey, 1, System.currentTimeMillis()));
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "handleConnSelfCureFailed, networkCureFailedHistory added, configKey = %{private}s", new Object[]{configKey});
                }
            }
            if (HwSelfCureEngine.this.mNoAutoConnReason != -1) {
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.updateScCHRCount(hwSelfCureEngine.mNoAutoConnReason);
            }
            HwSelfCureEngine hwSelfCureEngine2 = HwSelfCureEngine.this;
            hwSelfCureEngine2.transitionTo(hwSelfCureEngine2.mDisconnectedMonitorState);
        }

        private void updateSelfCureSucc() {
            if (HwSelfCureEngine.this.uploadManager != null) {
                if (HwSelfCureEngine.mDisableReason == 11) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.SELF_CURE_SUCC_EVENT, HwSelfCureEngine.SELF_CURE_STOP_USE);
                } else if (HwSelfCureEngine.mDisableReason == 2) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.SELF_CURE_SUCC_EVENT, HwSelfCureEngine.SELF_CURE_USER_REJECT);
                } else if (HwSelfCureEngine.mDisableReason == 13) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.SELF_CURE_SUCC_EVENT, HwSelfCureEngine.SELF_CURE_WRONG_PWD);
                } else if (HwSelfCureEngine.mDisableReason == 4) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.SELF_CURE_SUCC_EVENT, HwSelfCureEngine.SELF_CURE_INTERNET_ERROR);
                } else {
                    HwHiLog.w(HwSelfCureEngine.TAG, false, "CHR updateSelfCureSucc disable reason is not match", new Object[0]);
                }
                int unused = HwSelfCureEngine.mDisableReason = 0;
            }
        }
    }

    class InternetSelfCureState extends State {
        private String[] mAssignedDnses = null;
        private int mConfigAuthType = -1;
        private boolean mConfigStaticIp4GatewayChanged = false;
        private boolean mConfigStaticIp4MultiDhcpServer = false;
        private int mCurrentAbnormalType;
        private String mCurrentBssid = null;
        private String mCurrentGateway;
        private int mCurrentRssi;
        private int mCurrentSelfCureLevel;
        private boolean mDelayedRandMacReassocSelfCure = false;
        private boolean mDelayedReassocSelfCure = false;
        private boolean mDelayedResetSelfCure = false;
        private boolean mFinalSelfCureUsed = false;
        private boolean mHasInternetRecently;
        private boolean mIsRenewDhcpTimeout = false;
        private long mLastHasInetTimeMillis;
        private int mLastMultiGwselfFailedType;
        private int mLastSelfCureLevel;
        private boolean mPortalUnthenEver;
        private int mRenewDhcpCount;
        private int mSelfCureFailedCounter;
        private InternetSelfCureHistoryInfo mSelfCureHistoryInfo;
        private boolean mSetStaticIp4InvalidIp = false;
        private List<Integer> mTestedSelfCureLevel = new ArrayList();
        private String mUnconflictedIp;
        private boolean mUsedMultiGwSelfcure = false;
        private boolean mUserSetStaticIpConfig;

        InternetSelfCureState() {
        }

        public void enter() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "==> ##InternetSelfCureState", new Object[0]);
            this.mCurrentRssi = HwSelfCureEngine.INITIAL_RSSI;
            this.mSelfCureFailedCounter = 0;
            int i = -1;
            this.mCurrentAbnormalType = -1;
            this.mLastSelfCureLevel = -1;
            this.mCurrentSelfCureLevel = 200;
            this.mHasInternetRecently = false;
            this.mPortalUnthenEver = false;
            this.mUserSetStaticIpConfig = false;
            this.mCurrentGateway = getCurrentGateway();
            this.mTestedSelfCureLevel.clear();
            this.mFinalSelfCureUsed = false;
            this.mDelayedReassocSelfCure = false;
            this.mDelayedRandMacReassocSelfCure = false;
            this.mDelayedResetSelfCure = false;
            this.mSetStaticIp4InvalidIp = false;
            this.mUnconflictedIp = null;
            this.mRenewDhcpCount = 0;
            this.mLastMultiGwselfFailedType = -1;
            this.mUsedMultiGwSelfcure = false;
            WifiInfo wifiInfo = HwSelfCureEngine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mCurrentRssi = wifiInfo.getRssi();
                this.mCurrentBssid = wifiInfo.getBSSID();
                HwHiLog.d(HwSelfCureEngine.TAG, false, "InternetSelfCureState, network = %{public}s, signal rssi = %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(wifiInfo.getSSID()), Integer.valueOf(this.mCurrentRssi)});
            }
            HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED, 60000);
            WifiConfiguration config = WifiproUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (config != null) {
                this.mSelfCureHistoryInfo = HwSelfCureUtils.string2InternetSelfCureHistoryInfo(config.internetSelfCureHistory);
                this.mHasInternetRecently = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
                this.mPortalUnthenEver = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102);
                this.mUserSetStaticIpConfig = config.getIpAssignment() != null && config.getIpAssignment() == IpConfiguration.IpAssignment.STATIC;
                this.mLastHasInetTimeMillis = config.lastHasInternetTimestamp;
                if (config.allowedKeyManagement.cardinality() <= 1) {
                    i = config.getAuthType();
                }
                this.mConfigAuthType = i;
                HwHiLog.d(HwSelfCureEngine.TAG, false, "InternetSelfCureState, hasInternet = %{public}s, portalUnthenEver = %{public}s, userSetStaticIp = %{public}s, historyInfo = %{public}s, gw = %{public}s", new Object[]{String.valueOf(this.mHasInternetRecently), String.valueOf(this.mPortalUnthenEver), String.valueOf(this.mUserSetStaticIpConfig), this.mSelfCureHistoryInfo, WifiProCommonUtils.safeDisplayIpAddress(this.mCurrentGateway)});
            }
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == HwSelfCureEngine.CMD_IP_CONFIG_TIMEOUT) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_IP_CONFIG_TIMEOUT during self cure state. currentAbnormalType = %{public}d", new Object[]{Integer.valueOf(this.mCurrentAbnormalType)});
                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                this.mIsRenewDhcpTimeout = true;
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 47, new Bundle());
                if (HwSelfCureEngine.this.mHwWifiProPartManager != null) {
                    HwSelfCureEngine.this.mHwWifiProPartManager.notifyRenewDhcpTimeoutForWifiPro();
                }
                if (this.mCurrentAbnormalType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING && WifiProCommonUtils.isEncryptedAuthType(this.mConfigAuthType) && WifiProCommonUtils.getBssidCounter(HwSelfCureEngine.this.mSelfCureConfig, HwSelfCureEngine.this.getScanResults()) <= 3 && !this.mFinalSelfCureUsed) {
                    this.mFinalSelfCureUsed = true;
                    HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_DEAUTH_BSSID, 0);
                }
            } else if (i == HwSelfCureEngine.CMD_IP_CONFIG_COMPLETED) {
                if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_IP_CONFIG_TIMEOUT)) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_IP_CONFIG_TIMEOUT msg removed because of ip config success.", new Object[0]);
                    HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_IP_CONFIG_TIMEOUT);
                    this.mIsRenewDhcpTimeout = false;
                    handleIpConfigCompletedAfterRenewDhcp();
                }
                if (this.mIsRenewDhcpTimeout) {
                    handleIpConfigCompletedAfterRenewDhcp();
                }
                if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT)) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_INVALID_DHCP_OFFER_EVENT msg removed because of rcv other Dhcp Offer.", new Object[0]);
                    HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT);
                }
            } else if (i != HwSelfCureEngine.CMD_SELF_CURE_WIFI_FAILED) {
                if (i != HwSelfCureEngine.CMD_INTERNET_FAILURE_DETECTED) {
                    if (i == HwSelfCureEngine.CMD_P2P_DISCONNECTED_EVENT) {
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_P2P_DISCONNECTED_EVENT during self cure state.", new Object[0]);
                        handleRssiChanged();
                    } else if (i == HwSelfCureEngine.CMD_HTTP_REACHABLE_RCV) {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        if (HwSelfCureEngine.this.mSelfCureConfig != null) {
                            HwSelfCureEngine.this.mSelfCureConfig.internetSelfCureHistory = "0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0";
                            HwSelfCureEngine.this.mSelfCureConfig.validatedInternetAccess = true;
                            HwSelfCureEngine.this.mSelfCureConfig.noInternetAccess = false;
                            HwSelfCureEngine.this.mSelfCureConfig.wifiProNoInternetAccess = false;
                            HwSelfCureEngine.this.mSelfCureConfig.wifiProNoInternetReason = 0;
                            Bundle data = new Bundle();
                            data.putInt("messageWhat", HwSelfCureEngine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS);
                            data.putParcelable("messageObj", HwSelfCureEngine.this.mSelfCureConfig);
                            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 28, data);
                        }
                        HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                        hwSelfCureEngine.transitionTo(hwSelfCureEngine.mConnectedMonitorState);
                    } else if (i == HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED) {
                        HwSelfCureEngine.this.periodicArpDetection();
                    } else if (i == HwSelfCureEngine.CMD_MULTIGW_SELFCURE) {
                        multiGatewaySelfcure();
                    } else if (i != HwSelfCureEngine.CMD_ARP_FAILED_DETECTED) {
                        switch (i) {
                            case HwSelfCureEngine.CMD_NETWORK_DISCONNECTED_RCVD /* 108 */:
                                HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_NETWORK_DISCONNECTED_RCVD during self cure state.", new Object[0]);
                                HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_INTERNET_RECOVERY_CONFIRM);
                                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 47, new Bundle());
                                HwSelfCureEngine hwSelfCureEngine2 = HwSelfCureEngine.this;
                                hwSelfCureEngine2.transitionTo(hwSelfCureEngine2.mDisconnectedMonitorState);
                                break;
                            case HwSelfCureEngine.CMD_NEW_RSSI_RCVD /* 109 */:
                                this.mCurrentRssi = message.arg1;
                                handleRssiChanged();
                                break;
                            case HwSelfCureEngine.CMD_NETWORK_ROAMING_DETECT /* 110 */:
                                handleRoamingDetected((String) message.obj);
                                break;
                            default:
                                switch (i) {
                                    case HwSelfCureEngine.CMD_INTERNET_FAILED_SELF_CURE /* 112 */:
                                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                                        if (HwSelfCureEngine.this.isSuppOnCompletedState()) {
                                            selectSelfCureByFailedReason(message.arg1);
                                            break;
                                        }
                                        break;
                                    case HwSelfCureEngine.CMD_INTERNET_RECOVERY_CONFIRM /* 113 */:
                                        if (this.mCurrentSelfCureLevel == 205) {
                                            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 62, new Bundle());
                                        }
                                        HwSelfCureUtils.updateSelfCureConnectHistoryInfo(this.mSelfCureHistoryInfo, this.mCurrentSelfCureLevel, true);
                                        if (!confirmInternetSelfCure(this.mCurrentSelfCureLevel)) {
                                            HwSelfCureEngine.this.notifyVoWiFiSelCureEnd(-1);
                                            break;
                                        } else {
                                            this.mCurrentSelfCureLevel = 200;
                                            this.mSelfCureFailedCounter = 0;
                                            this.mHasInternetRecently = true;
                                            HwSelfCureEngine.this.notifyVoWiFiSelCureEnd(0);
                                            break;
                                        }
                                    case HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK /* 114 */:
                                        if (HwSelfCureEngine.this.isSuppOnCompletedState()) {
                                            this.mCurrentSelfCureLevel = message.arg1;
                                            selfCureWifiLink(message.arg1);
                                            HwSelfCureEngine.this.notifyVoWiFiSelCureBegin();
                                            break;
                                        }
                                        break;
                                    default:
                                        switch (i) {
                                            case HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT /* 130 */:
                                                this.mSetStaticIp4InvalidIp = HwSelfCureEngine.this.handleInvalidDhcpOffer(this.mUnconflictedIp);
                                                break;
                                            case 131:
                                                HwSelfCureEngine.this.updateScCHRCount(28);
                                                break;
                                            case 132:
                                                HwSelfCureEngine.this.updateScCHRCount(27);
                                                break;
                                            default:
                                                return false;
                                        }
                                }
                        }
                    } else if (!HwSelfCureEngine.this.shouldTransToWifi6SelfCureState(message, this.mCurrentBssid)) {
                        HwSelfCureEngine.this.handleArpFailedDetected();
                    }
                } else if (((Boolean) message.obj).booleanValue()) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "CMD_INTERNET_FAILURE_DETECTED rcvd under InternetSelfCureState, delete dhcp cache", new Object[0]);
                    WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 45, new Bundle());
                }
            } else if (this.mCurrentSelfCureLevel != 209 || message.arg1 == -5) {
                HwSelfCureUtils.updateSelfCureConnectHistoryInfo(this.mSelfCureHistoryInfo, this.mCurrentSelfCureLevel, false);
                updateWifiConfig(HwSelfCureEngine.this.mSelfCureConfig);
                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            }
            return true;
        }

        public void exit() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "InternetSelfCureState exit", new Object[0]);
            int unused = HwSelfCureEngine.mSelfCureReason = 0;
            this.mConfigStaticIp4MultiDhcpServer = false;
            this.mIsRenewDhcpTimeout = false;
            HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED);
        }

        private void uploadCurrentAbnormalStatistics() {
            int i = this.mCurrentAbnormalType;
            if (i != -1) {
                HwSelfCureEngine.this.updateScCHRCount(i);
                this.mCurrentAbnormalType = -1;
            }
        }

        private void uploadInternetCureSuccCounter(int selfCureType) {
            uploadCurrentAbnormalStatistics();
            int chrType = -1;
            if (selfCureType == 201) {
                chrType = 4;
            } else if (selfCureType == 202) {
                chrType = 5;
            } else if (selfCureType == 203) {
                chrType = 6;
            } else if (selfCureType == 207) {
                chrType = 30;
            } else if (selfCureType == 204) {
                chrType = 7;
            } else if (selfCureType == 205) {
                int i = this.mLastSelfCureLevel;
                if (i == 201) {
                    chrType = 20;
                } else if (i == 202) {
                    chrType = 21;
                } else if (i == 203) {
                    chrType = 22;
                } else if (i == 204) {
                    chrType = 8;
                }
            } else if (selfCureType == 208) {
                chrType = 8;
            }
            if (chrType != -1) {
                HwSelfCureEngine.this.updateScCHRCount(chrType);
            }
        }

        private void handleInternetFailedAndUserSetStaticIp(int internetFailedType) {
            if (!this.mHasInternetRecently || !HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, " user set static ip config, ignore to update config for user.", new Object[0]);
                if (!HwSelfCureEngine.this.mInternetUnknown) {
                    this.mCurrentAbnormalType = HwSelfCureUtils.RESET_REJECTED_BY_STATIC_IP_ENABLED;
                    uploadCurrentAbnormalStatistics();
                    return;
                }
                return;
            }
            if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
            } else if (internetFailedType == 302) {
                this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
            }
            HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
        }

        private int selectBestSelfCureSolution(int internetFailedType) {
            boolean multipleDhcpServer = HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2;
            long j = this.mLastHasInetTimeMillis;
            boolean noInternetWhenConnected = j <= 0 || j < HwSelfCureEngine.this.mConnectedTimeMills;
            HwHiLog.d(HwSelfCureEngine.TAG, false, "selectBestSelfCureSolution, multipleDhcpServer = %{public}s, noInternetWhenConnected = %{public}s", new Object[]{String.valueOf(multipleDhcpServer), String.valueOf(noInternetWhenConnected)});
            if (multipleDhcpServer && noInternetWhenConnected && getNextTestDhcpResults() != null && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) && (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS || internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP)) {
                this.mConfigStaticIp4MultiDhcpServer = true;
                return HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
            } else if (internetFailedType == 302 && multipleDhcpServer && getNextTestDhcpResults() != null && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP)) {
                this.mConfigStaticIp4MultiDhcpServer = true;
                return HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
            } else if (internetFailedType == 302 && WifiProCommonUtils.isEncryptedAuthType(this.mConfigAuthType) && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP)) {
                HwSelfCureEngine.this.mDhcpResultsTestDone.add(this.mCurrentGateway);
                this.mConfigStaticIp4GatewayChanged = true;
                return HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_INVALID_IP) {
                return HwSelfCureUtils.RESET_LEVEL_RECONNECT_4_INVALID_IP;
            } else {
                if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                    if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP)) {
                        return HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
                    }
                    return 200;
                } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                    if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS)) {
                        return HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
                    }
                    return 200;
                } else if (internetFailedType == 307) {
                    if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_RAND_MAC_REASSOC)) {
                        return HwSelfCureUtils.RESET_LEVEL_RAND_MAC_REASSOC;
                    }
                    return 200;
                } else if (internetFailedType != HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP || !HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, 204)) {
                    return 200;
                } else {
                    return 204;
                }
            }
        }

        private boolean isNeedMultiGatewaySelfcure() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "isNeedMultiGatewaySelfcure mUsedMultiGwSelfcure = %{public}s", new Object[]{String.valueOf(this.mUsedMultiGwSelfcure)});
            if (this.mUsedMultiGwSelfcure) {
                return false;
            }
            return HwSelfCureEngine.this.multiGateway();
        }

        private void multiGatewaySelfcure() {
            if (HwSelfCureEngine.this.isSuppOnCompletedState()) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mUsedMultiGwSelfcure = true;
                Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 70, new Bundle());
                if (result != null) {
                    String mac = result.getString("mac");
                    String gateway = result.getString(HwSelfCureEngine.GATEWAY);
                    if (mac == null || gateway == null) {
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "multi gateway selfcure failed.", new Object[0]);
                        int i = this.mLastMultiGwselfFailedType;
                        if (i != -1) {
                            selectSelfCureByFailedReason(i);
                        }
                    } else {
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "start multi gateway selfcure", new Object[0]);
                        Bundle data = new Bundle();
                        data.putString(HwSelfCureEngine.GATEWAY, gateway);
                        data.putString("mac", mac);
                        WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 57, data);
                        HwSelfCureEngine.this.flushVmDnsCache();
                        if (!HwSelfCureEngine.this.isHttpReachable(false)) {
                            HwHiLog.d(HwSelfCureEngine.TAG, false, "multi gateway selfcure failed , delStaticARP!", new Object[0]);
                            data.clear();
                            data.putString(HwSelfCureEngine.GATEWAY, gateway);
                            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 58, data);
                            HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_MULTIGW_SELFCURE);
                        } else {
                            HwHiLog.d(HwSelfCureEngine.TAG, false, "multi gateway selfcure success!", new Object[0]);
                            HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                            HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                            hwSelfCureEngine.transitionTo(hwSelfCureEngine.mConnectedMonitorState);
                        }
                    }
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                }
            }
        }

        private void selectSelfCureByFailedReason(int internetFailedType) {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "selectSelfCureByFailedReason, internetFailedType = %{public}d, userSetStaticIp = %{public}s", new Object[]{Integer.valueOf(internetFailedType), String.valueOf(this.mUserSetStaticIpConfig)});
            if (isNeedMultiGatewaySelfcure()) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "Start multi gateway selfcure.", new Object[0]);
                this.mLastMultiGwselfFailedType = internetFailedType;
                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_MULTIGW_SELFCURE);
            } else if (!this.mUserSetStaticIpConfig || !(internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS || internetFailedType == 302 || internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING)) {
                int requestSelfCureLevel = selectBestSelfCureSolution(internetFailedType);
                if (requestSelfCureLevel != 200) {
                    this.mCurrentAbnormalType = internetFailedType;
                    HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, requestSelfCureLevel, 0);
                } else if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "selectSelfCureByFailedReason, use wifi reset to cure this failed type = %{public}d", new Object[]{Integer.valueOf(internetFailedType)});
                    this.mCurrentAbnormalType = internetFailedType;
                    if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                        this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
                    } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                        this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
                    } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP) {
                        this.mLastSelfCureLevel = 204;
                    }
                    HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
                } else {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "selectSelfCureByFailedReason, no usable self cure for this failed type = %{public}d", new Object[]{Integer.valueOf(internetFailedType)});
                    handleHttpUnreachableFinally();
                }
            } else {
                handleInternetFailedAndUserSetStaticIp(internetFailedType);
            }
        }

        private void resetDnses(String[] dnses) {
            if (dnses == null || dnses.length == 0) {
                HwSelfCureUtils.requestUpdateDnsServers(new ArrayList(Arrays.asList("")));
            } else {
                HwSelfCureUtils.requestUpdateDnsServers(new ArrayList(Arrays.asList(dnses)));
            }
        }

        private boolean confirmInternetSelfCure(int currentCureLevel) {
            WifiConfiguration currentConfig;
            int curCureLevel = currentCureLevel;
            HwHiLog.d(HwSelfCureEngine.TAG, false, "confirmInternetSelfCure, cureLevel = %{public}d, last failed counter = %{public}d, finally = %{public}s", new Object[]{Integer.valueOf(curCureLevel), Integer.valueOf(this.mSelfCureFailedCounter), String.valueOf(this.mFinalSelfCureUsed)});
            if (curCureLevel != 200) {
                if (!HwSelfCureEngine.this.isHttpReachable(true)) {
                    if (curCureLevel == 201 && HwSelfCureEngine.this.mInternetUnknown) {
                        resetDnses(this.mAssignedDnses);
                    }
                    if (curCureLevel == 209) {
                        handleSelfCureFailedForRandMacReassoc();
                        return false;
                    }
                    this.mSelfCureFailedCounter++;
                    HwSelfCureUtils.updateSelfCureHistoryInfo(this.mSelfCureHistoryInfo, curCureLevel, false);
                    updateWifiConfig(null);
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "HTTP unreachable, self cure failed for %{public}d, selfCureHistoryInfo = %{public}s", new Object[]{Integer.valueOf(curCureLevel), this.mSelfCureHistoryInfo});
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    if (this.mFinalSelfCureUsed) {
                        handleHttpUnreachableFinally();
                        return false;
                    } else if (curCureLevel == 204 && HwSelfCureEngine.this.mHasTestWifi6Reassoc && HwSelfCureEngine.this.isNeedWifiReassocUseDeviceMac()) {
                        Message msg = Message.obtain();
                        msg.what = HwSelfCureEngine.CMD_INTERNET_FAILED_SELF_CURE;
                        msg.arg1 = 307;
                        HwSelfCureEngine.this.sendMessage(msg);
                        return false;
                    } else if (curCureLevel != 205) {
                        if (curCureLevel == 203) {
                            if (getNextTestDhcpResults() != null) {
                                this.mLastSelfCureLevel = curCureLevel;
                                HwHiLog.d(HwSelfCureEngine.TAG, false, "HTTP unreachable, and has next dhcp results, try next one.", new Object[0]);
                                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP, 0);
                                return false;
                            }
                            this.mConfigStaticIp4MultiDhcpServer = false;
                            if (selectedSelfCureAcceptable()) {
                                return false;
                            }
                            if (this.mCurrentAbnormalType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                                curCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
                            }
                        } else if ((curCureLevel == 202 || curCureLevel == 201) && HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2 && getNextTestDhcpResults() != null && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP)) {
                            this.mLastSelfCureLevel = curCureLevel;
                            this.mConfigStaticIp4MultiDhcpServer = true;
                            HwHiLog.d(HwSelfCureEngine.TAG, false, "HTTP unreachable, has next dhcp results, try next one for re-dhcp failed.", new Object[0]);
                            HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP, 0);
                            return false;
                        }
                        if (hasBeenTested(HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) || !HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                            handleHttpUnreachableFinally();
                        } else {
                            this.mLastSelfCureLevel = curCureLevel;
                            HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
                        }
                    } else {
                        if (HwSelfCureEngine.this.mHasTestWifi6Reassoc) {
                            HwSelfCureEngine.this.sendBlacklistToDriver();
                        }
                        if (getNextTestDhcpResults() == null || hasBeenTested(HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP)) {
                            handleHttpUnreachableFinally();
                        } else {
                            this.mFinalSelfCureUsed = true;
                            this.mLastSelfCureLevel = curCureLevel;
                            this.mConfigStaticIp4MultiDhcpServer = true;
                            HwHiLog.d(HwSelfCureEngine.TAG, false, "HTTP unreachable, and has next dhcp results, try next one for wifi reset failed.", new Object[0]);
                            HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP, 0);
                        }
                    }
                } else {
                    updateChrSelfCureSucc(HwSelfCureEngine.mSelfCureReason, curCureLevel);
                    if (curCureLevel == 201 && HwSelfCureEngine.this.mInternetUnknown) {
                        HwSelfCureUtils.requestUpdateDnsServers(HwSelfCureUtils.getPublicDnsServers());
                    }
                    if (curCureLevel == 209 && HwSelfCureEngine.this.mUseWithRandMacAddress == 2 && HwSelfCureEngine.this.isUseFactoryMac() && (currentConfig = WifiproUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager)) != null) {
                        currentConfig.isReassocSelfcureWithFactoryMacAddress = true;
                        updateWifiConfig(currentConfig);
                    }
                    handleHttpReachableAfterSelfCure(curCureLevel);
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    hwSelfCureEngine.transitionTo(hwSelfCureEngine.mConnectedMonitorState);
                    return true;
                }
            }
            return false;
        }

        private void updateChrSelfCureSucc(int reason, int currentCureLevel) {
            switch (reason) {
                case HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING /* 301 */:
                    addChrRoamingCnt(currentCureLevel);
                    return;
                case 302:
                    addChrMultiDhcpCnt(currentCureLevel);
                    return;
                case HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS /* 303 */:
                    addChrDnsCnt(currentCureLevel);
                    return;
                case HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP /* 304 */:
                    addChrTcpCnt(currentCureLevel);
                    return;
                default:
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "DHCP offer reason:%{public}d", new Object[]{Integer.valueOf(reason)});
                    return;
            }
        }

        private void addChrDnsCnt(int currentCureLevel) {
            if (HwSelfCureEngine.this.uploadManager != null) {
                if (currentCureLevel == 201) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.UNIQUE_CURE_EVENT, "replaceDnsSuccCnt");
                } else if (currentCureLevel == 203) {
                    Bundle dhcpData = new Bundle();
                    dhcpData.putInt("selfCureType", 0);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("dnsCureRecoveryEvent", "dhcpOfferSuccCnt", dhcpData);
                } else if (currentCureLevel == 205) {
                    Bundle chipData = new Bundle();
                    chipData.putInt("selfCureType", 0);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("dnsCureRecoveryEvent", "chipCureSuccCnt", chipData);
                } else {
                    HwHiLog.w(HwSelfCureEngine.TAG, false, "addChrDnsCnt: update DNS selfcure succ cnt, but current Cure Level is not here", new Object[0]);
                }
            }
        }

        private void addChrTcpCnt(int currentCureLevel) {
            if (HwSelfCureEngine.this.uploadManager != null) {
                if (currentCureLevel == 204) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.UNIQUE_CURE_EVENT, "reassocSuccCnt");
                } else if (currentCureLevel == 203) {
                    Bundle dhcpData = new Bundle();
                    dhcpData.putInt("selfCureType", 1);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("tcpCureRecoveryEvent", "dhcpOfferSuccCnt", dhcpData);
                } else if (currentCureLevel == 205) {
                    Bundle chipData = new Bundle();
                    chipData.putInt("selfCureType", 1);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("tcpCureRecoveryEvent", "chipCureSuccCnt", chipData);
                } else {
                    HwHiLog.w(HwSelfCureEngine.TAG, false, "addChrTcpCnt: update TCP selfcure succ cnt, but current Cure Level is not here", new Object[0]);
                }
            }
        }

        private void addChrRoamingCnt(int currentCureLevel) {
            if (HwSelfCureEngine.this.uploadManager != null) {
                if (currentCureLevel == 202) {
                    HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.UNIQUE_CURE_EVENT, "reDhcpSuccCnt");
                } else if (currentCureLevel == 203) {
                    Bundle dhcpData = new Bundle();
                    dhcpData.putInt("selfCureType", 2);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("roamCureRecoveryEvent", "dhcpOfferSuccCnt", dhcpData);
                } else if (currentCureLevel == 205) {
                    Bundle chipData = new Bundle();
                    chipData.putInt("selfCureType", 2);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("roamCureRecoveryEvent", "chipCureSuccCnt", chipData);
                } else {
                    HwHiLog.w(HwSelfCureEngine.TAG, false, "addChrRoamingCnt:but current Cure Level is not here", new Object[0]);
                }
            }
        }

        private void addChrMultiDhcpCnt(int currentCureLevel) {
            if (HwSelfCureEngine.this.uploadManager != null) {
                if (currentCureLevel == 203) {
                    Bundle dhcpData = new Bundle();
                    dhcpData.putInt("selfCureType", 3);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("multiCureRecoveryEvent", "dhcpOfferSuccCnt", dhcpData);
                } else if (currentCureLevel == 205) {
                    Bundle chipData = new Bundle();
                    chipData.putInt("selfCureType", 3);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("multiCureRecoveryEvent", "chipCureSuccCnt", chipData);
                } else {
                    HwHiLog.w(HwSelfCureEngine.TAG, false, "addChrMultiDhcpCnt:but current Cure Level is not here", new Object[0]);
                }
            }
        }

        private boolean selectedSelfCureAcceptable() {
            int i = this.mCurrentAbnormalType;
            if (i == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS || i == 302) {
                this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
                if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS)) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "HTTP unreachable, use dns replace to cure for dns failed.", new Object[0]);
                    HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS, 0);
                    return true;
                }
            } else if (i != HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP) {
                return false;
            } else {
                this.mLastSelfCureLevel = 204;
                if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, 204)) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "HTTP unreachable,  use reassoc to cure for no rx pkt.", new Object[0]);
                    HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, 204, 0);
                    return true;
                }
            }
            return false;
        }

        private boolean hasBeenTested(int cureLevel) {
            for (Integer num : this.mTestedSelfCureLevel) {
                if (num.intValue() == cureLevel) {
                    return true;
                }
            }
            return false;
        }

        private void handleHttpUnreachableFinally() {
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            if (!HwSelfCureEngine.this.mInternetUnknown) {
                uploadCurrentAbnormalStatistics();
            }
            HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
            HwSelfCureEngine.this.mRouterInternetDetector.notifyNoInternetAfterCure(this.mCurrentGateway, this.mConfigAuthType, HwSelfCureEngine.this.mMobileHotspot);
        }

        private void handleHttpReachableAfterSelfCure(int currentCureLevel) {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "handleHttpReachableAfterSelfCure, cureLevel = %{public}d, HTTP reachable, --> ConnectedMonitorState.", new Object[]{Integer.valueOf(currentCureLevel)});
            HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
            HwSelfCureEngine.this.mRouterInternetDetector.notifyInternetAccessRecovery();
            HwSelfCureUtils.updateSelfCureHistoryInfo(this.mSelfCureHistoryInfo, currentCureLevel, true);
            DhcpResults dhcpResults = HwSelfCureEngine.this.syncGetDhcpResults();
            String strDhcpResults = WifiProCommonUtils.dhcpResults2String(dhcpResults, -1);
            WifiConfiguration currentConfig = WifiproUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (!(currentConfig == null || strDhcpResults == null)) {
                currentConfig.lastDhcpResults = strDhcpResults;
            }
            updateWifiConfig(currentConfig);
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            if (this.mSetStaticIp4InvalidIp) {
                HwSelfCureEngine.this.requestArpConflictTest(dhcpResults);
                HwSelfCureEngine.this.mStaticIpCureSuccess = true;
            } else if (currentCureLevel == 203) {
                this.mCurrentAbnormalType = 302;
                HwSelfCureEngine.this.requestArpConflictTest(dhcpResults);
                HwSelfCureEngine.this.mStaticIpCureSuccess = true;
            }
            uploadInternetCureSuccCounter(currentCureLevel);
            HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_DHCP_RESULTS_UPDATE, strDhcpResults, 500);
        }

        private void handleRssiChanged() {
            if (WifiProCommonUtils.getCurrenSignalLevel(HwSelfCureEngine.this.mWifiManager.getConnectionInfo()) >= HwSelfCureEngine.ENABLE_NETWORK_RSSI_THR && !HwSelfCureEngine.this.mSelfCureOngoing.get() && !HwSelfCureEngine.this.mP2pConnected.get()) {
                if (this.mDelayedReassocSelfCure || this.mDelayedResetSelfCure || this.mDelayedRandMacReassocSelfCure) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (!HwSelfCureEngine.this.isHttpReachable(true)) {
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "handleRssiChanged, Http failed, delayedReassoc = %{public}s, delayedReset = %{public}s", new Object[]{String.valueOf(this.mDelayedReassocSelfCure), String.valueOf(this.mDelayedResetSelfCure)});
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        if (this.mDelayedReassocSelfCure) {
                            HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, 204, 0);
                        } else if (this.mDelayedRandMacReassocSelfCure) {
                            HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_RAND_MAC_REASSOC, 0);
                        } else if (this.mDelayedResetSelfCure) {
                            HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
                        }
                    } else {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                        this.mDelayedReassocSelfCure = false;
                        this.mDelayedResetSelfCure = false;
                        this.mDelayedRandMacReassocSelfCure = false;
                        HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                        hwSelfCureEngine.transitionTo(hwSelfCureEngine.mConnectedMonitorState);
                    }
                }
            }
        }

        private void handleRoamingDetected(String newBssid) {
            if (newBssid == null || newBssid.equals(this.mCurrentBssid)) {
                HwHiLog.e(HwSelfCureEngine.TAG, false, "handleRoamingDetected, but bssid is unchanged, ignore it.", new Object[0]);
                return;
            }
            this.mCurrentBssid = newBssid;
            if (HwSelfCureEngine.this.canArpReachable()) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "last gateway reachable, don't use http-get, gateway unchanged after roaming!", new Object[0]);
                HwSelfCureEngine.this.sendNetworkCheckingStatus("huawei.conn.NETWORK_CONDITIONS_MEASURED", "extra_is_internet_ready", 5);
            } else if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK)) {
            } else {
                if ((!hasBeenTested(HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) || (hasBeenTested(HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) && this.mRenewDhcpCount == 1)) && !HwSelfCureEngine.this.mSelfCureOngoing.get() && !this.mDelayedReassocSelfCure && !this.mDelayedResetSelfCure) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (!HwSelfCureEngine.this.isHttpReachable(false)) {
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "Roaming self-cure event: Re-Dhcp enter", new Object[0]);
                        HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.UNIQUE_CURE_EVENT, "reDhcpCnt");
                        HwHiLog.d(HwSelfCureEngine.TAG, false, "handleRoamingDetected, and HTTP access failed, trigger Re-Dhcp for it first time.", new Object[0]);
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP, 0);
                        return;
                    }
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    hwSelfCureEngine.transitionTo(hwSelfCureEngine.mConnectedMonitorState);
                }
            }
        }

        private void handleIpConfigCompletedAfterRenewDhcp() {
            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 47, new Bundle());
            this.mCurrentGateway = getCurrentGateway();
            HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INTERNET_RECOVERY_CONFIRM, 2000);
        }

        private String getCurrentGateway() {
            DhcpInfo dhcpInfo = HwSelfCureEngine.this.mWifiManager.getDhcpInfo();
            if (dhcpInfo == null || dhcpInfo.gateway == 0) {
                return null;
            }
            return NetworkUtils.intToInetAddress(dhcpInfo.gateway).getHostAddress();
        }

        private void updateWifiConfig(WifiConfiguration wifiConfig) {
            WifiConfiguration config;
            if (wifiConfig == null) {
                config = WifiproUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            } else {
                config = wifiConfig;
            }
            if (config != null) {
                config.internetSelfCureHistory = HwSelfCureUtils.internetSelfCureHistoryInfo2String(this.mSelfCureHistoryInfo);
                Bundle data = new Bundle();
                data.putInt("messageWhat", HwSelfCureEngine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS);
                data.putParcelable("messageObj", config);
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 28, data);
            }
        }

        private String getNextTestDhcpResults() {
            for (Map.Entry entry : HwSelfCureEngine.this.mDhcpOfferPackets.entrySet()) {
                String gatewayKey = (String) entry.getKey();
                String dhcpResults = (String) entry.getValue();
                if (gatewayKey != null && !gatewayKey.equals(this.mCurrentGateway)) {
                    boolean untested = true;
                    int i = 0;
                    while (true) {
                        if (i >= HwSelfCureEngine.this.mDhcpResultsTestDone.size()) {
                            break;
                        } else if (gatewayKey.equals(HwSelfCureEngine.this.mDhcpResultsTestDone.get(i))) {
                            untested = false;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (untested) {
                        return dhcpResults;
                    }
                }
            }
            return null;
        }

        private void selfCureForDns() {
            int i = 0;
            HwHiLog.d(HwSelfCureEngine.TAG, false, "begin to self cure for internet access: RESET_LEVEL_LOW_1_DNS", new Object[0]);
            HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.UNIQUE_CURE_EVENT, HwSelfCureEngine.ADD_REPLACE_DNS_RECORD);
            HwSelfCureEngine.this.mSelfCureOngoing.set(true);
            this.mTestedSelfCureLevel.add(Integer.valueOf((int) HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS));
            if (HwSelfCureEngine.this.mInternetUnknown) {
                ConnectivityManager connectivityManager = (ConnectivityManager) HwSelfCureEngine.this.mContext.getSystemService("connectivity");
                if (connectivityManager != null) {
                    Network[] allNetworks = connectivityManager.getAllNetworks();
                    int length = allNetworks.length;
                    while (true) {
                        if (i < length) {
                            Network network = allNetworks[i];
                            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                            if (networkInfo != null && networkInfo.getType() == 1) {
                                this.mAssignedDnses = NetworkUtils.makeStrings(connectivityManager.getLinkProperties(network).getDnsServers());
                                break;
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                    String[] strArr = this.mAssignedDnses;
                    if (strArr == null || strArr.length == 0) {
                        HwSelfCureUtils.requestUpdateDnsServers(HwSelfCureUtils.getPublicDnsServers());
                    } else {
                        HwSelfCureUtils.requestUpdateDnsServers(HwSelfCureUtils.getReplacedDnsServers(strArr));
                    }
                } else {
                    return;
                }
            } else {
                HwSelfCureUtils.requestUpdateDnsServers(HwSelfCureUtils.getPublicDnsServers());
            }
            HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INTERNET_RECOVERY_CONFIRM, 1000);
        }

        private void selfCureForRandMacReassoc() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "begin to self cure for internet access: RESET_LEVEL_RAND_MAC_REASSOC", new Object[0]);
            if (this.mCurrentRssi < HwSelfCureEngine.ENABLE_NETWORK_RSSI_THR || HwSelfCureEngine.this.mP2pConnected.get()) {
                HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                this.mDelayedRandMacReassocSelfCure = true;
                return;
            }
            HwSelfCureEngine.this.mSelfCureOngoing.set(true);
            this.mDelayedRandMacReassocSelfCure = false;
            this.mTestedSelfCureLevel.add(Integer.valueOf((int) HwSelfCureUtils.RESET_LEVEL_RAND_MAC_REASSOC));
            HwSelfCureEngine.this.mUseWithRandMacAddress = 2;
            Bundle data = new Bundle();
            data.putInt("useWithReassocType", HwSelfCureEngine.this.mUseWithRandMacAddress);
            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 50, data);
        }

        private void handleSelfCureFailedForRandMacReassoc() {
            if (HwSelfCureEngine.this.mUseWithRandMacAddress != 2 || !HwSelfCureEngine.this.isUseFactoryMac()) {
                this.mSelfCureFailedCounter++;
                HwSelfCureUtils.updateSelfCureHistoryInfo(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_RAND_MAC_REASSOC, false);
                updateWifiConfig(null);
                HwHiLog.d(HwSelfCureEngine.TAG, false, "HTTP unreachable, self cure failed for rand mac reassoc, selfCureHistoryInfo = %{public}s", new Object[]{this.mSelfCureHistoryInfo});
                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                Message msg = Message.obtain();
                msg.what = HwSelfCureEngine.CMD_INTERNET_FAILED_SELF_CURE;
                msg.arg1 = HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS;
                HwSelfCureEngine.this.sendMessage(msg);
                return;
            }
            HwHiLog.d(HwSelfCureEngine.TAG, false, "HTTP unreachable, factory mac failed and use rand mac instead of", new Object[0]);
            HwSelfCureEngine.this.mUseWithRandMacAddress = 3;
            Bundle data = new Bundle();
            data.putInt("useWithReassocType", HwSelfCureEngine.this.mUseWithRandMacAddress);
            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 50, data);
        }

        private String getRecordDhcpResults() {
            if (!this.mConfigStaticIp4GatewayChanged) {
                return null;
            }
            HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
            String dhcpResult = hwSelfCureEngine.getDhcpResultsHasInternet(WifiProCommonUtils.getCurrentBssid(hwSelfCureEngine.mWifiManager), HwSelfCureEngine.this.mSelfCureConfig);
            StaticIpConfiguration dhcpIpConfig = WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResult);
            if (dhcpIpConfig == null) {
                return null;
            }
            InetAddress dhcpGateAddr = dhcpIpConfig.gateway;
            if (!(dhcpGateAddr instanceof Inet4Address) || HwSelfCureEngine.this.doSlowArpTest((Inet4Address) dhcpGateAddr)) {
                return dhcpResult;
            }
            HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INTERNET_RECOVERY_CONFIRM, 500);
            return null;
        }

        private void selfCureWifiLink(int requestCureLevel) {
            String dhcpResults;
            HwHiLog.d(HwSelfCureEngine.TAG, false, "selfCureWifiLink, cureLevel = %{public}d, signal rssi = %{public}d", new Object[]{Integer.valueOf(requestCureLevel), Integer.valueOf(this.mCurrentRssi)});
            if (requestCureLevel == 201) {
                selfCureForDns();
            } else if (requestCureLevel == 202) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "re-DHCP event (ROAMING event) : reDhcpCnt enter", new Object[0]);
                HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.UNIQUE_CURE_EVENT, "reDhcpCnt");
                HwHiLog.d(HwSelfCureEngine.TAG, false, "begin to self cure for internet access: RESET_LEVEL_LOW_2_RENEW_DHCP", new Object[0]);
                HwSelfCureEngine.this.mDhcpOfferPackets.clear();
                HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                this.mRenewDhcpCount++;
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 48, new Bundle());
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_IP_CONFIG_TIMEOUT, 6000);
            } else if (requestCureLevel == 208) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "begin to self cure for internet access: RESET_LEVEL_DEAUTH_BSSID", new Object[0]);
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 59, new Bundle());
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INTERNET_RECOVERY_CONFIRM, 15000);
            } else if (requestCureLevel == 203) {
                if (this.mConfigStaticIp4MultiDhcpServer) {
                    dhcpResults = getNextTestDhcpResults();
                } else {
                    dhcpResults = getRecordDhcpResults();
                }
                String gatewayKey = WifiProCommonUtils.dhcpResults2Gateway(dhcpResults);
                if (dhcpResults == null || gatewayKey == null) {
                    HwHiLog.e(HwSelfCureEngine.TAG, false, "dhcpResults or gatewayKey is null", new Object[0]);
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    return;
                }
                Bundle data = new Bundle();
                if (HwSelfCureEngine.mSelfCureReason == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "DHCP offer reason: DNS", new Object[0]);
                    data.putInt("selfCureType", 0);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("dnsCureRecoveryEvent", "dhcpOfferCnt", data);
                } else if (HwSelfCureEngine.mSelfCureReason == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "DHCP offer reason: TCP", new Object[0]);
                    data.putInt("selfCureType", 1);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("tcpCureRecoveryEvent", "dhcpOfferCnt", data);
                } else if (HwSelfCureEngine.mSelfCureReason == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "DHCP offer reason: ROAMING", new Object[0]);
                    data.putInt("selfCureType", 2);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("roamCureRecoveryEvent", "dhcpOfferCnt", data);
                } else if (HwSelfCureEngine.mSelfCureReason == 302) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "DHCP offer reason: Multi-DHCP", new Object[0]);
                    data.putInt("selfCureType", 3);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("multiCureRecoveryEvent", "dhcpOfferCnt", data);
                } else {
                    HwHiLog.w(HwSelfCureEngine.TAG, false, "DHCP Offer Reason is not the 4 reasons", new Object[0]);
                }
                String gatewayKey2 = gatewayKey.replace("/", "");
                HwHiLog.d(HwSelfCureEngine.TAG, false, "begin to self cure for internet access: TRY_NEXT_DHCP_OFFER", new Object[0]);
                HwSelfCureEngine.this.mDhcpResultsTestDone.add(gatewayKey2);
                StaticIpConfiguration staticIpConfig = WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResults);
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                HwSelfCureEngine.this.requestUseStaticIpConfig(staticIpConfig);
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_IP_CONFIG_TIMEOUT, 3000);
            } else if (requestCureLevel == 207) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mUnconflictedIp = HwSelfCureEngine.this.getLegalIpConfiguration();
                HwHiLog.d(HwSelfCureEngine.TAG, false, "begin to self cure for internet access: RESET_LEVEL_RECONNECT_4_INVALID_IP", new Object[0]);
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 49, new Bundle());
            } else if (requestCureLevel == 204) {
                if (this.mCurrentRssi < HwSelfCureEngine.ENABLE_NETWORK_RSSI_THR || HwSelfCureEngine.this.mP2pConnected.get()) {
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    this.mDelayedReassocSelfCure = true;
                    return;
                }
                HwHiLog.d(HwSelfCureEngine.TAG, false, "begin to self cure for internet access: RESET_LEVEL_MIDDLE_REASSOC", new Object[0]);
                HwHiLog.d(HwSelfCureEngine.TAG, false, "TCP self-cure event: reassocation enter", new Object[0]);
                HwSelfCureEngine.this.uploadManager.addChrSsidCntStat(HwSelfCureEngine.UNIQUE_CURE_EVENT, "reassocCnt");
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                this.mDelayedReassocSelfCure = false;
                Bundle data2 = new Bundle();
                data2.putInt("useWithReassocType", 1);
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 50, data2);
            } else if (requestCureLevel == 209) {
                selfCureForRandMacReassoc();
            } else if (requestCureLevel != 205) {
            } else {
                if (HwSelfCureEngine.this.mInternetUnknown || !this.mHasInternetRecently || WifiProCommonUtils.isQueryActivityMatched(HwSelfCureEngine.this.mContext, WifiProCommonUtils.HUAWEI_SETTINGS_WLAN)) {
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    HwSelfCureEngine.this.mRouterInternetDetector.notifyNoInternetAfterCure(this.mCurrentGateway, this.mConfigAuthType, HwSelfCureEngine.this.mMobileHotspot);
                    return;
                }
                HwSelfCureEngine.this.mWifiManager.getConnectionInfo();
                if (this.mCurrentRssi < -70 || HwSelfCureEngine.this.mP2pConnected.get()) {
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    this.mDelayedResetSelfCure = true;
                    return;
                }
                Bundle data3 = new Bundle();
                if (HwSelfCureEngine.mSelfCureReason == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "Chip recovery reason: DNS", new Object[0]);
                    data3.putInt("selfCureType", 0);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("dnsCureRecoveryEvent", "chipCureCnt", data3);
                } else if (HwSelfCureEngine.mSelfCureReason == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "Chip recovery reason: TCP", new Object[0]);
                    data3.putInt("selfCureType", 1);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("tcpCureRecoveryEvent", "chipCureCnt", data3);
                } else if (HwSelfCureEngine.mSelfCureReason == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "Chip recovery reason: ROAMING", new Object[0]);
                    data3.putInt("selfCureType", 2);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("roamCureRecoveryEvent", "chipCureCnt", data3);
                } else if (HwSelfCureEngine.mSelfCureReason == 302) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "Chip recovery reason: Multi-DHCP", new Object[0]);
                    data3.putInt("selfCureType", 3);
                    HwSelfCureEngine.this.uploadManager.addChrSsidBundleStat("multiCureRecoveryEvent", "chipCureCnt", data3);
                } else {
                    HwHiLog.w(HwSelfCureEngine.TAG, false, "Chip recovery Reason is not the 4 reasons", new Object[0]);
                }
                HwHiLog.d(HwSelfCureEngine.TAG, false, "begin to self cure for internet access: RESET_LEVEL_HIGH_RESET", new Object[0]);
                HwSelfCureEngine.this.mDhcpOfferPackets.clear();
                HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mDelayedResetSelfCure = false;
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 51, new Bundle());
            }
        }
    }

    class Wifi6SelfCureState extends State {
        private static final int ACTION_TYPE_HTC = 0;
        private static final int ACTION_TYPE_WIFI6 = 1;
        private String mCurrentBssid = null;
        private int mInternetValue = 0;
        private boolean mIsForceHttpCheck = true;
        private int mWifi6ArpDetectionFailedCnt = 0;
        private int mWifi6HtcArpDetectionFailedCnt = 0;

        Wifi6SelfCureState() {
        }

        public void enter() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "==> ## enter Wifi6SelfCureState", new Object[0]);
            this.mWifi6HtcArpDetectionFailedCnt = 0;
            this.mWifi6ArpDetectionFailedCnt = 0;
            WifiInfo wifiInfo = HwSelfCureEngine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mCurrentBssid = wifiInfo.getBSSID();
            }
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == HwSelfCureEngine.CMD_NETWORK_ROAMING_DETECT) {
                String newBssid = null;
                if (message.obj instanceof String) {
                    newBssid = (String) message.obj;
                }
                if (newBssid == null || newBssid.equals(this.mCurrentBssid)) {
                    HwHiLog.d(HwSelfCureEngine.TAG, false, "roamingDetected in Wifi6SelfCureState, bssid not changed, ignore.", new Object[0]);
                } else {
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    hwSelfCureEngine.transitionTo(hwSelfCureEngine.mInternetSelfCureState);
                    return false;
                }
            } else if (i == HwSelfCureEngine.CMD_WIFI6_SELFCURE) {
                this.mInternetValue = message.arg1;
                if (message.obj instanceof Boolean) {
                    this.mIsForceHttpCheck = !((Boolean) message.obj).booleanValue();
                }
                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_WIFI6_WITH_HTC_PERIODIC_ARP_DETECTED);
            } else if (i != HwSelfCureEngine.CMD_WIFI6_BACKOFF_SELFCURE) {
                switch (i) {
                    case 307:
                        periodicWifi6WithouHtcArpDetect();
                        break;
                    case HwSelfCureEngine.CMD_WIFI6_WITH_HTC_PERIODIC_ARP_DETECTED /* 308 */:
                        periodicWifi6WithHtcArpDetect();
                        break;
                    case HwSelfCureEngine.CMD_WIFI6_WITH_HTC_ARP_FAILED_DETECTED /* 309 */:
                        handleWifi6WithHtcArpFail();
                        break;
                    case HwSelfCureEngine.CMD_WIFI6_WITHOUT_HTC_ARP_FAILED_DETECTED /* 310 */:
                        handleWifi6WithouHtcArpFail();
                        break;
                    default:
                        return false;
                }
            } else {
                this.mInternetValue = message.arg1;
                if (message.obj instanceof Boolean) {
                    this.mIsForceHttpCheck = !((Boolean) message.obj).booleanValue();
                }
                HwSelfCureEngine.this.sendMessage(307);
            }
            return false;
        }

        public void exit() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "==> ##Wifi6SelfCureState exit", new Object[0]);
        }

        private void handleWifi6WithHtcArpFail() {
            HwHiLog.d(HwSelfCureEngine.TAG, false, "wifi6 with htc arp detect failed", new Object[0]);
            HwSelfCureEngine.this.mIsWifi6ArpSuccess = false;
            Wifi6BlackListInfo wifi6BlackListInfo = new Wifi6BlackListInfo(0, SystemClock.elapsedRealtime());
            String lastConnectedBssid = WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager);
            HwSelfCureEngine.this.mWifi6BlackListCache.put(lastConnectedBssid, wifi6BlackListInfo);
            HwHiLog.d(HwSelfCureEngine.TAG, false, "add %{public}s to HTC blacklist", new Object[]{WifiProCommonUtils.safeDisplayBssid(lastConnectedBssid)});
            HwSelfCureEngine.this.sendBlacklistToDriver();
            HwSelfCureEngine.this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(HwSelfCureEngine.IFACE, 132, new byte[]{1});
            HwSelfCureEngine.this.sendMessage(307);
        }

        private void handleWifi6WithouHtcArpFail() {
            String lastConnectedBssid = WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager);
            HwHiLog.d(HwSelfCureEngine.TAG, false, "wifi6 without htc arp detect failed", new Object[0]);
            HwSelfCureEngine.this.mIsWifi6ArpSuccess = false;
            HwSelfCureEngine.this.mWifi6BlackListCache.put(lastConnectedBssid, new Wifi6BlackListInfo(1, SystemClock.elapsedRealtime()));
            HwHiLog.d(HwSelfCureEngine.TAG, false, "add %{public}s to WIFI6 blacklist", new Object[]{WifiProCommonUtils.safeDisplayBssid(lastConnectedBssid)});
            HwSelfCureEngine.this.sendBlacklistToDriver();
            wifi6ReassocSelfcure();
        }

        private void periodicWifi6WithHtcArpDetect() {
            if (doWifi6ArpDetec(true)) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "wifi6 with htc arp detect success", new Object[0]);
                this.mWifi6HtcArpDetectionFailedCnt = 0;
                HwSelfCureEngine.this.mIsWifi6ArpSuccess = true;
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.deferMessage(hwSelfCureEngine.obtainMessage(HwSelfCureEngine.CMD_INTERNET_FAILURE_DETECTED, this.mInternetValue, 0, Boolean.valueOf(true ^ this.mIsForceHttpCheck)));
                HwSelfCureEngine hwSelfCureEngine2 = HwSelfCureEngine.this;
                hwSelfCureEngine2.transitionTo(hwSelfCureEngine2.mConnectedMonitorState);
                return;
            }
            this.mWifi6HtcArpDetectionFailedCnt++;
            if (this.mWifi6HtcArpDetectionFailedCnt == 5) {
                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_WIFI6_WITH_HTC_ARP_FAILED_DETECTED);
            }
            int i = this.mWifi6HtcArpDetectionFailedCnt;
            if (i > 0 && i < 5) {
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_WIFI6_WITH_HTC_PERIODIC_ARP_DETECTED, 300);
            }
        }

        private void periodicWifi6WithouHtcArpDetect() {
            if (doWifi6ArpDetec(false)) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "wifi6 without htc arp detect success", new Object[0]);
                this.mWifi6ArpDetectionFailedCnt = 0;
                HwSelfCureEngine.this.mIsWifi6ArpSuccess = true;
                if (!HwSelfCureEngine.this.isHttpReachable(false)) {
                    HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INTERNET_FAILURE_DETECTED, this.mInternetValue, 0, Boolean.valueOf(!this.mIsForceHttpCheck), 100);
                } else {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                }
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.transitionTo(hwSelfCureEngine.mConnectedMonitorState);
                return;
            }
            this.mWifi6ArpDetectionFailedCnt++;
            if (this.mWifi6ArpDetectionFailedCnt == 5) {
                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_WIFI6_WITHOUT_HTC_ARP_FAILED_DETECTED);
            }
            int i = this.mWifi6ArpDetectionFailedCnt;
            if (i > 0 && i < 5) {
                HwSelfCureEngine.this.sendMessageDelayed(307, 500);
            }
        }

        private void wifi6ReassocSelfcure() {
            HwSelfCureEngine.this.mHasTestWifi6Reassoc = true;
            Message msg = Message.obtain();
            msg.what = HwSelfCureEngine.CMD_INTERNET_FAILED_SELF_CURE;
            msg.arg1 = HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP;
            HwSelfCureEngine.this.deferMessage(msg);
            HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
            hwSelfCureEngine.transitionTo(hwSelfCureEngine.mInternetSelfCureState);
        }

        private boolean doWifi6ArpDetec(boolean isArpDetectWithHtc) {
            int interfaceId;
            if (isArpDetectWithHtc) {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "do wifi6 with htc arp detect", new Object[0]);
                interfaceId = 83;
            } else {
                HwHiLog.d(HwSelfCureEngine.TAG, false, "do wifi6 without htc arp detect", new Object[0]);
                interfaceId = 84;
            }
            Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, interfaceId, new Bundle());
            if (result != null) {
                return result.getBoolean("arpResult");
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleArpFailedDetected() {
        this.mSelfCureOngoing.set(true);
        if (isHttpReachable(false)) {
            this.mSelfCureOngoing.set(false);
            notifyHttpReachableForWifiPro(true);
            return;
        }
        sendMessage(CMD_SELF_CURE_WIFI_LINK, 204);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canArpReachable() {
        DhcpResults dhcpResults = syncGetDhcpResults();
        if (dhcpResults == null) {
            return false;
        }
        Bundle data = new Bundle();
        data.putSerializable(GATEWAY, dhcpResults.gateway);
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 75, data);
        if (result != null) {
            return result.getBoolean(ARP_TEST);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DhcpResults syncGetDhcpResults() {
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 46, new Bundle());
        if (result == null) {
            return null;
        }
        result.setClassLoader(DhcpResults.class.getClassLoader());
        return result.getParcelable("dhcpResults");
    }

    private WifiInfo getWifiInfo() {
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 52, new Bundle());
        if (result != null) {
            return (WifiInfo) result.getParcelable("WifiInfo");
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestUseStaticIpConfig(StaticIpConfiguration staticIpConfig) {
        Bundle data = new Bundle();
        data.putParcelable("staticIpConfig", staticIpConfig);
        WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 53, data);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWifiBackgroundReason(int reason) {
        Bundle data = new Bundle();
        data.putInt("reason", reason);
        WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 55, data);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScCHRCount(int count) {
        Bundle data = new Bundle();
        data.putInt("count", count);
        WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 61, data);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean doSlowArpTest(Inet4Address addr) {
        Bundle data = new Bundle();
        data.putSerializable("testIpAddr", addr);
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 76, data);
        if (result != null) {
            return result.getBoolean("slowArpTest");
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getDhcpResultsHasInternet(String currentBssid, WifiConfiguration config) {
        HwWifiProPartManager hwWifiProPartManager;
        String dhcpResults = null;
        if (!(currentBssid == null || (hwWifiProPartManager = this.mHwWifiProPartManager) == null)) {
            dhcpResults = hwWifiProPartManager.syncQueryDhcpResultsByBssid(currentBssid);
        }
        if (dhcpResults != null || config == null) {
            return dhcpResults;
        }
        return config.lastDhcpResults;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean handleInvalidDhcpOffer(String dhcpResults) {
        if (dhcpResults == null) {
            return false;
        }
        requestUseStaticIpConfig(WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResults));
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getLegalIpConfiguration() {
        DhcpResults dhcpResults = syncGetDhcpResults();
        HwHiLog.d(TAG, false, "getLegalIpConfiguration, dhcpResults are %{private}s", new Object[]{dhcpResults});
        if (dhcpResults == null || dhcpResults.gateway == null || dhcpResults.ipAddress == null) {
            return null;
        }
        InetAddress gateway = dhcpResults.gateway;
        InetAddress initialIpAddr = dhcpResults.ipAddress.getAddress();
        int testCnt = 0;
        ArrayList<InetAddress> conflictedIpAddr = new ArrayList<>();
        InetAddress testIpAddr = initialIpAddr;
        while (true) {
            int testCnt2 = testCnt + 1;
            if (testCnt >= 3 || testIpAddr == null) {
                break;
            }
            conflictedIpAddr.add(testIpAddr);
            testIpAddr = HwSelfCureUtils.getNextIpAddr(gateway, initialIpAddr, conflictedIpAddr);
            if (testIpAddr != null) {
                if (!doSlowArpTest((Inet4Address) testIpAddr)) {
                    HwHiLog.d(TAG, false, "getLegalIpConfiguration, find a new unconflicted one.", new Object[0]);
                    dhcpResults.ipAddress = new LinkAddress(testIpAddr, dhcpResults.ipAddress.getPrefixLength(), dhcpResults.ipAddress.getFlags(), dhcpResults.ipAddress.getScope());
                    return WifiProCommonUtils.dhcpResults2String(dhcpResults, -1);
                }
            }
            testCnt = testCnt2;
        }
        try {
            byte[] oldIpAddr = dhcpResults.ipAddress.getAddress().getAddress();
            oldIpAddr[3] = -100;
            LinkAddress newIpAddress = new LinkAddress(InetAddress.getByAddress(oldIpAddr), dhcpResults.ipAddress.getPrefixLength(), dhcpResults.ipAddress.getFlags(), dhcpResults.ipAddress.getScope());
            HwHiLog.d(TAG, false, "getLegalIpConfiguration newIpAddress = %{private}s", new Object[]{newIpAddress});
            dhcpResults.ipAddress = newIpAddress;
            return WifiProCommonUtils.dhcpResults2String(dhcpResults, -1);
        } catch (UnknownHostException e) {
            HwHiLog.e(TAG, false, "Exception happened in getLegalIpConfiguration()", new Object[0]);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyHttpReachableForWifiPro(boolean httpReachable) {
        HwWifiProPartManager hwWifiProPartManager = this.mHwWifiProPartManager;
        if (hwWifiProPartManager != null) {
            hwWifiProPartManager.notifyHttpReachableForWifiPro(httpReachable);
        }
        HwWifiproLiteStateMachine liteStateMachine = HwWifiproLiteStateMachine.getInstance();
        if (liteStateMachine != null) {
            liteStateMachine.notifyHttpReachable(httpReachable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyHttpRedirectedForWifiPro() {
        HwWifiProPartManager hwWifiProPartManager = this.mHwWifiProPartManager;
        if (hwWifiProPartManager != null) {
            hwWifiProPartManager.notifyHttpRedirectedForWifiPro();
        }
        HwWifiproLiteStateMachine liteStateMachine = HwWifiproLiteStateMachine.getInstance();
        if (liteStateMachine != null) {
            liteStateMachine.notifyHttpRedirectedForWifiPro();
        }
    }

    private void notifyRoamingCompletedForWifiPro(String newBssid) {
        HwWifiProPartManager hwWifiProPartManager = this.mHwWifiProPartManager;
        if (hwWifiProPartManager != null) {
            hwWifiProPartManager.notifyRoamingCompletedForWifiPro(newBssid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isConnectingOrConnected() {
        WifiInfo info = getWifiInfo();
        if (info == null || info.getSupplicantState().ordinal() < SupplicantState.AUTHENTICATING.ordinal()) {
            return false;
        }
        HwHiLog.d(TAG, false, "Supplicant is connectingOrConnected, no need to self cure for auto connection.", new Object[0]);
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        this.mNoAutoConnCounter = 0;
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSuppOnCompletedState() {
        WifiInfo info = getWifiInfo();
        if (info == null || info.getSupplicantState().ordinal() != SupplicantState.COMPLETED.ordinal()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkConnected() {
        HwHiLog.d(TAG, false, "ENTER: handleNetworkConnected()", new Object[0]);
        if (!updateConnSelfCureFailedHistory()) {
            HwHiLog.d(TAG, false, "handleNetworkConnected, config is null for update, delay 2s to update again.", new Object[0]);
            sendMessageDelayed(CMD_UPDATE_CONN_SELF_CURE_HISTORY, 2000);
        }
        this.mNoAutoConnCounter = 0;
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        List<Integer> enabledReasons = new ArrayList<>();
        enabledReasons.add(1);
        enabledReasons.add(10);
        enabledReasons.add(11);
        enableAllNetworksByReason(enabledReasons, false);
        this.mConnectedTimeMills = System.currentTimeMillis();
        synchronized (this.mDhcpFailedBssidLock) {
            this.mDhcpFailedBssids.clear();
            this.mDhcpFailedConfigKeys.clear();
        }
        transitionTo(this.mConnectedMonitorState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateConnSelfCureFailedHistory() {
        WifiConfiguration config = WifiproUtils.getCurrentWifiConfig(this.mWifiManager);
        if (config == null || config.configKey() == null) {
            return false;
        }
        this.networkCureFailedHistory.remove(config.configKey());
        HwHiLog.d(TAG, false, "updateConnSelfCureFailedHistory(), networkCureFailedHistory remove %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableAllNetworksByReason(List<Integer> enabledReasons, boolean isNeedCheckRssi) {
        List<WifiConfiguration> savedNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (savedNetworks == null || savedNetworks.size() == 0) {
            HwHiLog.e(TAG, false, "enableAllNetworksByReason, no saved networks found.", new Object[0]);
            return;
        }
        for (WifiConfiguration itemSaveNetworks : savedNetworks) {
            WifiConfiguration.NetworkSelectionStatus status = itemSaveNetworks.getNetworkSelectionStatus();
            int disableReason = status.getNetworkSelectionDisableReason();
            boolean isNeedEnableNetwork = false;
            if (WifiCommonUtils.doesNotWifiConnectRejectByCust(status, itemSaveNetworks.SSID, this.mContext)) {
                HwHiLog.d(TAG, false, "enableAllNetworksByReason can not enable wifi", new Object[0]);
            } else {
                if (!status.isNetworkEnabled()) {
                    HwHiLog.d(TAG, false, "enableAllNetworksByReason, isNeedCheckRssi: %{public}s, rssiStatusDisabled: %{public}d", new Object[]{String.valueOf(isNeedCheckRssi), Integer.valueOf(itemSaveNetworks.rssiStatusDisabled)});
                    if (!isNeedCheckRssi || (isNeedCheckRssi && itemSaveNetworks.rssiStatusDisabled != INITIAL_RSSI && itemSaveNetworks.rssiStatusDisabled <= ENABLE_NETWORK_RSSI_THR)) {
                        isNeedEnableNetwork = true;
                    }
                }
                if (isNeedEnableNetwork) {
                    Iterator<Integer> it = enabledReasons.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            if (disableReason == it.next().intValue()) {
                                HwHiLog.d(TAG, false, "To enable network which status is %{public}d, config = %{public}s, id = %{public}d", new Object[]{Integer.valueOf(disableReason), StringUtilEx.safeDisplaySsid(itemSaveNetworks.getPrintableSsid()), Integer.valueOf(itemSaveNetworks.networkId)});
                                itemSaveNetworks.rssiStatusDisabled = INITIAL_RSSI;
                                this.mWifiManager.enableNetwork(itemSaveNetworks.networkId, false);
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableAllNetworksByEnterSettings(List<Integer> enabledReasons) {
        List<WifiConfiguration> savedNetworks = WifiproUtils.getAllConfiguredNetworks();
        if (savedNetworks == null || savedNetworks.size() == 0) {
            HwHiLog.e(TAG, false, "enableAllNetworksByEnterSettings, no saved networks found.", new Object[0]);
            return;
        }
        int networkSize = savedNetworks.size();
        for (int i = 0; i < networkSize; i++) {
            WifiConfiguration nextConfig = savedNetworks.get(i);
            if (nextConfig != null) {
                WifiConfiguration.NetworkSelectionStatus status = nextConfig.getNetworkSelectionStatus();
                int disableReason = status.getNetworkSelectionDisableReason();
                if (WifiCommonUtils.doesNotWifiConnectRejectByCust(status, nextConfig.SSID, this.mContext)) {
                    HwHiLog.d(TAG, false, "enableAllNetworksByEnterSettings can not enable wifi", new Object[0]);
                } else if (!status.isNetworkEnabled() && (KEY_HUAWEI_EMPLOYEE.equals(nextConfig.configKey()) || KEY_HUAWEI_EMPLOYEE.equals(nextConfig.configKey()))) {
                    HwHiLog.d(TAG, false, "##enableAllNetworksByEnterSettings, HUAWEI_EMPLOYEE networkId = %{public}d", new Object[]{Integer.valueOf(nextConfig.networkId)});
                    this.mWifiManager.enableNetwork(nextConfig.networkId, false);
                } else if (!status.isNetworkEnabled() && !nextConfig.noInternetAccess && !nextConfig.portalNetwork) {
                    Iterator<Integer> it = enabledReasons.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            if (disableReason == it.next().intValue()) {
                                HwHiLog.d(TAG, false, "enableAllNetworksByEnterSettings, status is %{public}d, config = %{public}s, id = %{public}d", new Object[]{Integer.valueOf(disableReason), StringUtilEx.safeDisplaySsid(nextConfig.getPrintableSsid()), Integer.valueOf(nextConfig.networkId)});
                                this.mWifiManager.enableNetwork(nextConfig.networkId, false);
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isHttpReachable(boolean useDoubleServers) {
        synchronized (this.mNetworkChecker) {
            int mainSvrRespCode = this.mNetworkChecker.isCaptivePortal(true);
            if (WifiProCommonUtils.unreachableRespCodeByAndroid(mainSvrRespCode)) {
                return false;
            }
            if (mainSvrRespCode == 302) {
                this.mIsHttpRedirected = true;
            } else {
                this.mIsHttpRedirected = false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isIpAddressInvalid() {
        byte[] currAddr;
        DhcpInfo dhcpInfo = this.mWifiManager.getDhcpInfo();
        if (!(dhcpInfo == null || dhcpInfo.ipAddress == 0 || (currAddr = NetworkUtils.intToInetAddress(dhcpInfo.ipAddress).getAddress()) == null || currAddr.length != 4)) {
            int intCurrAddr3 = currAddr[3] & 255;
            int netmaskLenth = NetworkUtils.netmaskIntToPrefixLength(dhcpInfo.netmask);
            HwHiLog.d(TAG, false, "isIpAddressLegal, currAddr[3] is %{public}d netmask lenth is: %{public}d", new Object[]{Integer.valueOf(intCurrAddr3), Integer.valueOf(netmaskLenth)});
            boolean ipEqualsGw = dhcpInfo.ipAddress == dhcpInfo.gateway;
            boolean invalidIp = intCurrAddr3 == 0 || intCurrAddr3 == 1 || intCurrAddr3 == 255;
            if (ipEqualsGw || (netmaskLenth == 24 && invalidIp)) {
                HwHiLog.w(TAG, false, "current rcvd ip is invalid, maybe no internet access, need to comfirm and cure it.", new Object[0]);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiDisabled(boolean selfCureGoing) {
        HwHiLog.d(TAG, false, "ENTER: handleWifiDisabled(), selfCureGoing = %{public}s", new Object[]{String.valueOf(selfCureGoing)});
        this.mNoAutoConnCounter = 0;
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        this.networkCureFailedHistory.clear();
        if (selfCureGoing) {
            transitionTo(this.mDisconnectedMonitorState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiEnabled() {
        HwHiLog.d(TAG, false, "ENTER: handleWifiEnabled()", new Object[0]);
        List<Integer> enabledReasons = new ArrayList<>();
        enabledReasons.add(1);
        enabledReasons.add(10);
        enabledReasons.add(11);
        enableAllNetworksByReason(enabledReasons, false);
        sendBlacklistToDriver();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkRemoved(WifiConfiguration config) {
        if (config != null) {
            this.networkCureFailedHistory.remove(config.configKey());
            this.autoConnectFailedNetworks.remove(config.configKey());
            this.autoConnectFailedNetworksRssi.remove(config.configKey());
        }
    }

    private boolean hasDhcpResultsSaved(WifiConfiguration config) {
        return WifiProCommonUtils.dhcpResults2StaticIpConfig(config.lastDhcpResults) != null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean multiGateway() {
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 71, new Bundle());
        if (result != null) {
            return result.getBoolean("isMultiGateway");
        }
        return false;
    }

    public synchronized void notifyWifiScanResultsAvailable(boolean success) {
        if (this.mInitialized && success) {
            sendMessage(101);
        }
    }

    public synchronized void notifyDhcpResultsInternetOk(String dhcpResults) {
        if (this.mInitialized && dhcpResults != null) {
            sendMessage(CMD_DHCP_RESULTS_UPDATE, dhcpResults);
        }
    }

    public synchronized void notifyWifiConnectedBackground() {
        HwHiLog.d(TAG, false, "ENTER: notifyWifiConnectedBackground()", new Object[0]);
        if (this.mInitialized) {
            this.mIsWifiBackground.set(true);
            this.mIpConfigLostCnt = 0;
            sendMessage(CMD_NETWORK_CONNECTED_RCVD);
        }
    }

    public synchronized void notifyWifiRoamingCompleted(String bssid) {
        HwHiLog.d(TAG, false, "ENTER: notifyWifiRoamingCompleted()", new Object[0]);
        if (this.mInitialized && bssid != null) {
            sendMessageDelayed(CMD_NETWORK_ROAMING_DETECT, bssid, 500);
            notifyRoamingCompletedForWifiPro(bssid);
        }
    }

    public synchronized void notifySefCureCompleted(int status) {
        HwHiLog.d(TAG, false, "ENTER: notifySefCureCompleted, status = %{public}d", new Object[]{Integer.valueOf(status)});
        if (!this.mInitialized || status != 0) {
            if (-1 != status) {
                if (-2 != status) {
                    if (status == -5) {
                        sendMessage(CMD_SELF_CURE_WIFI_FAILED, status);
                        notifyVoWiFiSelCureEnd(-1);
                    } else {
                        this.mSelfCureOngoing.set(false);
                        notifyVoWiFiSelCureEnd(-1);
                    }
                }
            }
            sendMessage(CMD_SELF_CURE_WIFI_FAILED);
            notifyVoWiFiSelCureEnd(-1);
        } else {
            sendMessage(CMD_INTERNET_RECOVERY_CONFIRM);
        }
    }

    public synchronized void notifyTcpStatResults(int deltaTx, int deltaRx, int deltaReTx, int deltaDnsFailed) {
        if (this.mInitialized) {
            if (deltaDnsFailed < 3 || deltaRx != 0) {
                if (deltaTx < 3 || deltaRx != 0) {
                    if (deltaRx > 0) {
                        this.mNoTcpRxCounter = 0;
                        removeMessages(CMD_DNS_FAILED_DETECTED);
                        removeMessages(CMD_NO_TCP_RX_DETECTED);
                    }
                } else if (!hasMessages(CMD_NO_TCP_RX_DETECTED)) {
                    sendMessage(CMD_NO_TCP_RX_DETECTED);
                }
            } else if (!hasMessages(CMD_DNS_FAILED_DETECTED)) {
                sendMessage(CMD_DNS_FAILED_DETECTED);
            }
        }
    }

    public synchronized void notifyWifiDisconnected() {
        HwHiLog.d(TAG, false, "ENTER: notifyWifiDisconnected()", new Object[0]);
        if (this.mInitialized) {
            sendMessage(CMD_NETWORK_DISCONNECTED_RCVD);
        }
    }

    public synchronized void notifyIpConfigCompleted() {
        if (this.mInitialized) {
            HwHiLog.d(TAG, false, "ENTER: notifyIpConfigCompleted()", new Object[0]);
            this.mIpConfigLostCnt = 0;
            sendMessage(CMD_IP_CONFIG_COMPLETED);
        }
    }

    public synchronized boolean notifyIpConfigLostAndHandle(WifiConfiguration config) {
        if (this.mInitialized && config != null) {
            if (config.isEnterprise()) {
                HwHiLog.d(TAG, false, "notifyIpConfigLostAndHandle, no self cure for enterprise network", new Object[0]);
                return false;
            }
            int signalLevel = WifiProCommonUtils.getCurrenSignalLevel(this.mWifiManager.getConnectionInfo());
            this.mIpConfigLostCnt++;
            HwHiLog.d(TAG, false, "ENTER: notifyIpConfigLostAndHandle() IpConfigLostCnt = %{public}d, ssid = %{public}s, signalLevel = %{public}d", new Object[]{Integer.valueOf(this.mIpConfigLostCnt), StringUtilEx.safeDisplaySsid(config.SSID), Integer.valueOf(signalLevel)});
            if (signalLevel >= 3 && getCurrentState() == this.mDisconnectedMonitorState) {
                if (this.mIpConfigLostCnt == 2 && hasDhcpResultsSaved(config)) {
                    sendMessage(CMD_IP_CONFIG_LOST_EVENT, config);
                    return true;
                } else if (this.mIpConfigLostCnt >= 1 && !hasDhcpResultsSaved(config)) {
                    sendMessage(CMD_BSSID_DHCP_FAILED_EVENT, config);
                }
            }
        }
        return false;
    }

    public boolean isDhcpFailedBssid(String bssid) {
        boolean contains;
        synchronized (this.mDhcpFailedBssidLock) {
            contains = this.mDhcpFailedBssids.contains(bssid);
        }
        return contains;
    }

    public boolean isDhcpFailedConfigKey(String configKey) {
        boolean contains;
        synchronized (this.mDhcpFailedBssidLock) {
            contains = this.mDhcpFailedConfigKeys.contains(configKey);
        }
        return contains;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWifi6Network(String lastConnectedBssid) {
        List<ScanResult> scanResults = getScanResults();
        if (scanResults == null) {
            return false;
        }
        for (ScanResult scanResult : scanResults) {
            if (scanResult != null && scanResult.BSSID != null && scanResult.BSSID.equals(lastConnectedBssid) && WifiProCommonUtils.isSsidSupportWiFi6(scanResult)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void requestChangeWifiStatus(boolean enabled) {
        if (this.mInitialized && this.mWifiManager != null) {
            this.mWifiManager.setWifiEnabled(enabled);
        }
    }

    public synchronized boolean isSelfCureOngoing() {
        if (!this.mInitialized) {
            return false;
        }
        return this.mSelfCureOngoing.get();
    }

    public synchronized void notifyInternetFailureDetected(boolean forceNoHttpCheck) {
        if (this.mInitialized) {
            HwHiLog.d(TAG, false, "ENTER: notifyInternetFailureDetected, forceNoHttpCheck = %{public}s", new Object[]{String.valueOf(forceNoHttpCheck)});
            sendMessage(CMD_INTERNET_FAILURE_DETECTED, Boolean.valueOf(forceNoHttpCheck));
        }
    }

    public synchronized void notifyInternetAccessRecovery() {
        if (this.mInitialized) {
            this.mRouterInternetDetector.notifyInternetAccessRecovery();
            HwHiLog.d(TAG, false, "ENTER: notifyInternetAccessRecovery", new Object[0]);
            sendMessage(CMD_HTTP_REACHABLE_RCV);
        }
    }

    public synchronized void notifyUserEnterWlanSettings() {
        if (this.mInitialized) {
            sendMessage(CMD_USER_ENTER_WLAN_SETTINGS);
        }
    }

    public synchronized void notifySettingsDisplayNoInternet() {
        sendMessage(131);
    }

    public synchronized void notifyRouterGatewayUnreachable() {
        sendMessage(132);
    }

    public void requestArpConflictTest(DhcpResults dhcpResults) {
        InetAddress addr;
        if (dhcpResults != null && dhcpResults.ipAddress != null && (addr = dhcpResults.ipAddress.getAddress()) != null && (addr instanceof Inet4Address) && doSlowArpTest((Inet4Address) addr)) {
            HwHiLog.d(TAG, false, "requestArpConflictTest, Upload static ip conflicted chr!", new Object[0]);
            updateScCHRCount(26);
        }
    }

    /* access modifiers changed from: package-private */
    public static class CureFailedNetworkInfo {
        public String configKey;
        public int cureFailedCounter;
        public long lastCureFailedTime;

        public CureFailedNetworkInfo(String key, int counter, long time) {
            this.configKey = key;
            this.cureFailedCounter = counter;
            this.lastCureFailedTime = time;
        }

        public String toString() {
            return "[ configKey = " + StringUtilEx.safeDisplaySsid(this.configKey) + ", cureFailedCounter = " + this.cureFailedCounter + ", lastCureFailedTime = " + DateFormat.getDateTimeInstance().format(new Date(this.lastCureFailedTime)) + " ]";
        }
    }

    /* access modifiers changed from: package-private */
    public static class InternetSelfCureHistoryInfo {
        public int dnsSelfCureFailedCnt = 0;
        public long lastDnsSelfCureFailedTs = 0;
        public long lastRandMacSelfCureConnectFailedCntTs = 0;
        public long lastRandMacSelfCureFailedCntTs = 0;
        public long lastReassocSelfCureConnectFailedTs = 0;
        public long lastReassocSelfCureFailedTs = 0;
        public long lastRenewDhcpSelfCureFailedTs = 0;
        public long lastResetSelfCureConnectFailedTs = 0;
        public long lastResetSelfCureFailedTs = 0;
        public long lastStaticIpSelfCureFailedTs = 0;
        public int randMacSelfCureConnectFailedCnt = 0;
        public int randMacSelfCureFailedCnt = 0;
        public int reassocSelfCureConnectFailedCnt = 0;
        public int reassocSelfCureFailedCnt = 0;
        public int renewDhcpSelfCureFailedCnt = 0;
        public int resetSelfCureConnectFailedCnt = 0;
        public int resetSelfCureFailedCnt = 0;
        public int staticIpSelfCureFailedCnt = 0;

        public String toString() {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("[ ");
            sbuf.append("dnsSelfCureFailedCnt = " + this.dnsSelfCureFailedCnt);
            sbuf.append(", renewDhcpSelfCureFailedCnt = " + this.renewDhcpSelfCureFailedCnt);
            sbuf.append(", staticIpSelfCureFailedCnt = " + this.staticIpSelfCureFailedCnt);
            sbuf.append(", reassocSelfCureFailedCnt = " + this.reassocSelfCureFailedCnt);
            sbuf.append(", randMacSelfCureFailedCnt = " + this.randMacSelfCureFailedCnt);
            sbuf.append(", resetSelfCureFailedCnt = " + this.resetSelfCureFailedCnt);
            sbuf.append(", reassocSelfCureConnectFailedCnt = " + this.reassocSelfCureConnectFailedCnt);
            sbuf.append(", randMacSelfCureConnectFailedCnt = " + this.randMacSelfCureConnectFailedCnt);
            sbuf.append(", resetSelfCureConnectFailedCnt = " + this.resetSelfCureConnectFailedCnt);
            sbuf.append(" ]");
            return sbuf.toString();
        }
    }

    public static class Wifi6BlackListInfo {
        private int actionType = -1;
        private long updateTime = 0;

        Wifi6BlackListInfo(int actionType2, long updateTime2) {
            this.actionType = actionType2;
            this.updateTime = updateTime2;
        }

        public int getActionType() {
            return this.actionType;
        }

        public long getUpdateTime() {
            return this.updateTime;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startScan() {
        WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 73, new Bundle());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<ScanResult> getScanResults() {
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 74, new Bundle());
        if (result == null) {
            return null;
        }
        result.setClassLoader(ScanResult.class.getClassLoader());
        return result.getParcelableArrayList("results");
    }

    private List<ScanResult> getHistoryScanResults() {
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 86, new Bundle());
        if (result == null) {
            return null;
        }
        result.setClassLoader(ScanResult.class.getClassLoader());
        return result.getParcelableArrayList("results");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void periodicArpDetection() {
        int signalLevel = WifiProCommonUtils.getCurrenSignalLevel(this.mWifiManager.getConnectionInfo());
        HwHiLog.d(TAG, false, "periodicArpDetection signalLevel = %{public}d, isScreenOn = %{public}s , mArpDetectionFailedCnt = %{public}d", new Object[]{Integer.valueOf(signalLevel), String.valueOf(this.mPowerManager.isScreenOn()), Integer.valueOf(this.mArpDetectionFailedCnt)});
        if (hasMessages(CMD_PERIODIC_ARP_DETECTED)) {
            removeMessages(CMD_PERIODIC_ARP_DETECTED);
        }
        if (signalLevel >= 2 && this.mPowerManager.isScreenOn() && isSuppOnCompletedState() && !this.mSelfCureOngoing.get()) {
            Bundle result = WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 68, new Bundle());
            boolean arpResult = false;
            long time = 0;
            if (result != null) {
                arpResult = result.getBoolean("arpResult");
                time = result.getLong("time");
            }
            Bundle data = new Bundle();
            data.putBoolean("succ", arpResult);
            data.putLong("spendTime", time);
            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 63, data);
            if (!arpResult) {
                this.mArpDetectionFailedCnt++;
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 67, new Bundle());
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 66, new Bundle());
                if (this.mArpDetectionFailedCnt == 5) {
                    data.clear();
                    data.putInt("eventId", 909002024);
                    data.putString("eventData", "ARP_DETECTED_FAILED");
                    WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 4, data);
                    sendMessage(CMD_ARP_FAILED_DETECTED);
                }
                int i = this.mArpDetectionFailedCnt;
                if (i > 0 && i < 5) {
                    sendMessageDelayed(CMD_PERIODIC_ARP_DETECTED, 10000);
                    updateArpDetect(this.mArpDetectionFailedCnt, time);
                    return;
                }
            } else {
                this.mArpDetectionFailedCnt = 0;
            }
            updateArpDetect(this.mArpDetectionFailedCnt, time);
        }
        sendMessageDelayed(CMD_PERIODIC_ARP_DETECTED, 60000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void flushVmDnsCache() {
        Intent intent = new Intent("android.intent.action.CLEAR_DNS_CACHE");
        intent.addFlags(536870912);
        intent.addFlags(67108864);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyVoWiFiSelCureBegin() {
        if (this.mSelfCureOngoing.get()) {
            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 69, new Bundle());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyVoWiFiSelCureEnd(int status) {
        boolean success = false;
        if (status == 0) {
            success = true;
        }
        Bundle data = new Bundle();
        data.putBoolean("success", success);
        WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 72, data);
    }

    private void updateArpDetect(int failCnt, long rtt) {
        Bundle arp = new Bundle();
        arp.putInt("ARPFAILCNT", failCnt);
        arp.putLong("RTTARP", rtt);
        Bundle data = new Bundle();
        data.putInt("eventId", EVENT_ARP_DETECT);
        data.putBundle("eventData", arp);
        WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 2, data);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBlacklistToDriver() {
        Map<String, Wifi6BlackListInfo> map = this.mWifi6BlackListCache;
        if (map != null && !map.isEmpty()) {
            ageOutWifi6BlackList();
            byte[] bytes = HwSelfCureUtils.blackListToByteArray(this.mWifi6BlackListCache);
            HwHiLog.d(TAG, false, "sendBlacklistToDriver size:%{public}d", new Object[]{Integer.valueOf(this.mWifi6BlackListCache.size())});
            this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 131, bytes);
        }
    }

    private void ageOutWifi6BlackList() {
        Iterator<Map.Entry<String, Wifi6BlackListInfo>> iterator = this.mWifi6BlackListCache.entrySet().iterator();
        while (iterator.hasNext()) {
            if (SystemClock.elapsedRealtime() - iterator.next().getValue().getUpdateTime() >= WIFI6_BLACKLIST_TIME_EXPIRED) {
                iterator.remove();
            }
        }
        if (this.mWifi6BlackListCache.size() >= 16) {
            long earliestTime = Long.MAX_VALUE;
            String delBssid = null;
            for (Map.Entry<String, Wifi6BlackListInfo> map : this.mWifi6BlackListCache.entrySet()) {
                if (map.getValue().getUpdateTime() < earliestTime) {
                    delBssid = map.getKey();
                    earliestTime = map.getValue().getUpdateTime();
                }
            }
            this.mWifi6BlackListCache.remove(delBssid);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: com.huawei.hwwifiproservice.HwSelfCureEngine */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v1, types: [boolean, int] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Unknown variable types count: 1 */
    private boolean shouldTransToWifi6SelfCureState(Message message, String currentBssid) {
        if (currentBssid != null && isWifi6SelfCureConditionMatched(currentBssid)) {
            ?? r1 = this.mInternetUnknown;
            if (!this.mWifi6BlackListCache.containsKey(currentBssid)) {
                HwHiLog.d(TAG, false, "start wifi6 selfcure", new Object[0]);
                deferMessage(obtainMessage(CMD_WIFI6_SELFCURE, r1, 0, message.obj));
                transitionTo(this.mWifi6SelfCureState);
                return true;
            } else if (this.mWifi6BlackListCache.get(currentBssid) == null || this.mWifi6BlackListCache.get(currentBssid).getActionType() != 0) {
                HwHiLog.d(TAG, false, "no need to do wifi6 selfcure", new Object[0]);
            } else {
                HwHiLog.d(TAG, false, "start wifi6 without htc back off to wifi5 selfcure", new Object[0]);
                deferMessage(obtainMessage(CMD_WIFI6_BACKOFF_SELFCURE, r1 == true ? 1 : 0, 0, message.obj));
                transitionTo(this.mWifi6SelfCureState);
                return true;
            }
        }
        return false;
    }

    private boolean isWifi6SelfCureConditionMatched(String currentBssid) {
        if (isWifi6Network(currentBssid) && !this.mIsWifi6ArpSuccess && WifiProCommonUtils.getCurrentRssi(this.mWifiManager) >= ENABLE_NETWORK_RSSI_THR) {
            return true;
        }
        HwHiLog.d(TAG, false, "wifi6SelfCureCondition not match", new Object[0]);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedWifiReassocUseDeviceMac() {
        if (this.mSelfCureConfig == null || !canArpReachable()) {
            HwHiLog.d(TAG, false, "mSelfCureConfig is null or arp is not reachablel!", new Object[0]);
            return false;
        } else if (this.mSelfCureConfig.macRandomizationSetting != 1 || !this.mWifiStateMachine.isConnectedMacRandomizationEnabled()) {
            HwHiLog.i(TAG, false, " RandomizedMacTake no Effect", new Object[0]);
            return false;
        } else if (isUseFactoryMac()) {
            return false;
        } else {
            if (WifiProCommonUtils.getBssidCounter(this.mSelfCureConfig, getScanResults()) >= 2 || WifiProCommonUtils.getBssidCounter(this.mSelfCureConfig, getHistoryScanResults()) >= 2) {
                boolean hasInternetEver = WifiProCommonUtils.matchedRequestByHistory(this.mSelfCureConfig.internetHistory, (int) CMD_NETWORK_CONNECTED_RCVD);
                boolean hasPortalHistory = WifiProCommonUtils.matchedRequestByHistory(this.mSelfCureConfig.internetHistory, 102);
                if (hasInternetEver || hasPortalHistory) {
                    HwHiLog.d(TAG, false, "has 1 or 2 in internet history, don't to reassoc with factory mac!", new Object[0]);
                    return false;
                }
                InternetSelfCureHistoryInfo selfCureHistoryInfo = HwSelfCureUtils.string2InternetSelfCureHistoryInfo(this.mSelfCureConfig.internetSelfCureHistory);
                HwHiLog.d(TAG, false, "config.internetSelfCureHistory= %{public}s", new Object[]{this.mSelfCureConfig.internetSelfCureHistory});
                if (selfCureHistoryInfo.randMacSelfCureConnectFailedCnt > 3 || selfCureHistoryInfo.randMacSelfCureFailedCnt > 20) {
                    HwHiLog.d(TAG, false, "has connect fail three times or randMacSelf fail 20 times!", new Object[0]);
                    return false;
                } else if (System.currentTimeMillis() - selfCureHistoryInfo.lastRandMacSelfCureConnectFailedCntTs >= 30000) {
                    return true;
                } else {
                    HwHiLog.d(TAG, false, "Too close to the last connection failure time return", new Object[0]);
                    return false;
                }
            } else {
                HwHiLog.d(TAG, false, "not multi bssid condition!", new Object[0]);
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isUseFactoryMac() {
        String currentMac = this.mWifiNative.getMacAddress(IFACE);
        String deviceMac = this.mWifiStateMachine.getFactoryMacAddress();
        if (currentMac == null || !currentMac.equals(deviceMac)) {
            return false;
        }
        HwHiLog.i(TAG, false, "current use factory mac address", new Object[0]);
        return true;
    }
}
