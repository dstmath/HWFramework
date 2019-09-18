package com.android.server.wifi;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.arp.HWMultiGW;
import android.net.dhcp.HwArpClient;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.wifi.ABS.HwABSDetectorService;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.HwArpUtils;
import com.android.server.wifi.HwQoE.HidataWechatTraffic;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.util.WifiCommonUtils;
import com.android.server.wifi.wifipro.HwAutoConnectManager;
import com.android.server.wifi.wifipro.HwWifiproLiteStateMachine;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifipro.PortalDataBaseManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwSelfCureEngine extends StateMachine {
    private static final int ARP_DETECTED_FAILED_COUNT = 5;
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
    private static final int CMD_USER_ENTER_WLAN_SETTINGS = 137;
    private static final int CMD_USER_PRESENT_RCVD = 134;
    private static final int CMD_WIFI_DISABLED_RCVD = 105;
    private static final int CMD_WIFI_ENABLED_RCVD = 106;
    private static final int CURE_OUT_OF_DATE_MS = 7200000;
    private static final int DEFAULT_ARP_DETECTED_MS = 60000;
    private static final int DEFAULT_ARP_TIMEOUT_MS = 1000;
    private static final int DEFAULT_GATEWAY_NUMBER = 1;
    private static final int DHCP_RENEW_TIMEOUT_MS = 6000;
    private static final int DNS_UPDATE_CONFIRM_DELAYED_MS = 1000;
    private static final int FAST_ARP_DETECTED_MS = 10000;
    private static final int GRATUITOUS_ARP_TIMEOUT_MS = 100;
    private static final int HANDLE_WIFI_ON_DELAYED_MS = 1000;
    private static final int INTERNET_DETECT_INTERVAL_MS = 6000;
    private static final int INTERNET_FAILED_INVALID_IP = 305;
    private static final int INTERNET_FAILED_TYPE_DNS = 303;
    private static final int INTERNET_FAILED_TYPE_GATEWAY = 302;
    private static final int INTERNET_FAILED_TYPE_ROAMING = 301;
    private static final int INTERNET_FAILED_TYPE_TCP = 304;
    private static final int INTERNET_OK = 300;
    private static final int IP_CONFIG_CONFIRM_DELAYED_MS = 2000;
    private static final String PROP_DISABLE_SELF_CURE = "hw.wifi.disable_self_cure";
    private static final int REQ_HTTP_DELAYED_MS = 500;
    private static final int SELF_CURE_DELAYED_MS = 100;
    private static final int SELF_CURE_MONITOR_DELAYED_MS = 2000;
    private static final int SELF_CURE_TIMEOUT_MS = 20000;
    private static final int SET_STATIC_IP_TIMEOUT_MS = 3000;
    private static final String TAG = "HwSelfCureEngine";
    private static HwSelfCureEngine mHwSelfCureEngine = null;
    /* access modifiers changed from: private */
    public Map<String, WifiConfiguration> autoConnectFailedNetworks = new HashMap();
    /* access modifiers changed from: private */
    public Map<String, Integer> autoConnectFailedNetworksRssi = new HashMap();
    /* access modifiers changed from: private */
    public HwArpClient mArpClient;
    /* access modifiers changed from: private */
    public int mArpDetectionFailedCnt = 0;
    /* access modifiers changed from: private */
    public State mConnectedMonitorState = new ConnectedMonitorState();
    /* access modifiers changed from: private */
    public long mConnectedTimeMills;
    /* access modifiers changed from: private */
    public String mConnectionCureConfigKey = null;
    /* access modifiers changed from: private */
    public State mConnectionSelfCureState = new ConnectionSelfCureState();
    /* access modifiers changed from: private */
    public Context mContext;
    private State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public final Object mDhcpFailedBssidLock = new Object();
    /* access modifiers changed from: private */
    public ArrayList<String> mDhcpFailedBssids = new ArrayList<>();
    /* access modifiers changed from: private */
    public ArrayList<String> mDhcpFailedConfigKeys = new ArrayList<>();
    /* access modifiers changed from: private */
    public Map<String, String> mDhcpOfferPackets = new HashMap();
    /* access modifiers changed from: private */
    public ArrayList<String> mDhcpResultsTestDone = new ArrayList<>();
    /* access modifiers changed from: private */
    public State mDisconnectedMonitorState = new DisconnectedMonitorState();
    /* access modifiers changed from: private */
    public HWMultiGW mHWMultiGW;
    /* access modifiers changed from: private */
    public HwArpUtils mHwArpUtils;
    /* access modifiers changed from: private */
    public HwWifiCHRService mHwWifiCHRService;
    private boolean mInitialized = false;
    /* access modifiers changed from: private */
    public State mInternetSelfCureState = new InternetSelfCureState();
    /* access modifiers changed from: private */
    public boolean mInternetUnknown = false;
    private int mIpConfigLostCnt = 0;
    /* access modifiers changed from: private */
    public boolean mIsCaptivePortalCheckEnabled;
    /* access modifiers changed from: private */
    public AtomicBoolean mIsWifiBackground = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public boolean mMobileHotspot = false;
    private HwNetworkPropertyChecker mNetworkChecker;
    /* access modifiers changed from: private */
    public WifiConfiguration mNoAutoConnConfig;
    /* access modifiers changed from: private */
    public int mNoAutoConnCounter = 0;
    /* access modifiers changed from: private */
    public int mNoAutoConnReason = -1;
    /* access modifiers changed from: private */
    public int mNoTcpRxCounter = 0;
    /* access modifiers changed from: private */
    public AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public HwRouterInternetDetector mRouterInternetDetector = null;
    /* access modifiers changed from: private */
    public WifiConfiguration mSelfCureConfig = null;
    /* access modifiers changed from: private */
    public AtomicBoolean mSelfCureOngoing = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public boolean mStaticIpCureSuccess = false;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public WifiStateMachine mWifiStateMachine;
    /* access modifiers changed from: private */
    public Map<String, CureFailedNetworkInfo> networkCureFailedHistory = new HashMap();

    class ConnectedMonitorState extends State {
        private int mConfigAuthType = -1;
        private boolean mGatewayInvalid = false;
        private boolean mHasInternetRecently;
        private boolean mIpv6DnsEnabled;
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
            HwSelfCureEngine.this.LOGD("==> ##ConnectedMonitorState");
            this.mLastConnectedBssid = HwSelfCureEngine.this.mWifiStateMachine.getCurrentBSSID();
            this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
            this.mLastDnsRefuseCounter = HwSelfCureUtils.getCurrentDnsRefuseCounter();
            int unused = HwSelfCureEngine.this.mNoTcpRxCounter = 0;
            this.mLastSignalLevel = 0;
            int unused2 = HwSelfCureEngine.this.mArpDetectionFailedCnt = 0;
            this.mHasInternetRecently = false;
            this.mPortalUnthenEver = false;
            boolean unused3 = HwSelfCureEngine.this.mInternetUnknown = false;
            this.mUserSetStaticIpConfig = false;
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            this.mIpv6DnsEnabled = true;
            this.mWifiSwitchAllowed = false;
            this.mMobileHotspot = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(HwSelfCureEngine.this.mContext);
            WifiInfo wifiInfo = HwSelfCureEngine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mLastSignalLevel = WifiProCommonUtils.getCurrenSignalLevel(wifiInfo);
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.LOGD("ConnectedMonitorState, network = " + wifiInfo.getSSID() + ", signal = " + this.mLastSignalLevel + ", mobileHotspot = " + this.mMobileHotspot);
            }
            if (!HwSelfCureEngine.this.mIsWifiBackground.get() && !setupSelfCureMonitor()) {
                HwSelfCureEngine.this.LOGD("ConnectedMonitorState, config is null when connected broadcast received, delay to setup again.");
                HwSelfCureEngine.this.sendMessageDelayed(118, 2000);
            }
            HwArpUtils unused4 = HwSelfCureEngine.this.mHwArpUtils;
            HwArpUtils.checkWifiDefaultRoute();
            HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED, 10000);
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 104:
                    HwSelfCureEngine.this.LOGD("ConnectedMonitorState, CMD_NETWORK_CONNECTED_RCVD!");
                    if (HwSelfCureEngine.this.mIsWifiBackground.get()) {
                        HwSelfCureEngine.this.mIsWifiBackground.set(false);
                        enter();
                        break;
                    }
                    break;
                case 108:
                    if (HwSelfCureEngine.this.hasMessages(115)) {
                        HwSelfCureEngine.this.removeMessages(115);
                    }
                    if (HwSelfCureEngine.this.hasMessages(118)) {
                        HwSelfCureEngine.this.removeMessages(118);
                    }
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mDisconnectedMonitorState);
                    break;
                case 109:
                    this.mLastSignalLevel = WifiProCommonUtils.getCurrenSignalLevel(HwSelfCureEngine.this.mWifiManager.getConnectionInfo());
                    break;
                case 110:
                    if (!HwSelfCureEngine.this.mIsWifiBackground.get() && !this.mGatewayInvalid) {
                        String newBssid = message.obj != null ? (String) message.obj : null;
                        if (newBssid == null || !newBssid.equals(this.mLastConnectedBssid)) {
                            if (!this.mUserSetStaticIpConfig) {
                                updateInternetAccessHistory();
                                if (!this.mHasInternetRecently && !this.mPortalUnthenEver && !HwSelfCureEngine.this.mInternetUnknown) {
                                    HwSelfCureEngine.this.LOGD("CMD_NETWORK_ROAMING_DETECT rcvd, but no internet access always.");
                                    break;
                                } else {
                                    if (HwSelfCureEngine.this.hasMessages(115)) {
                                        HwSelfCureEngine.this.removeMessages(115);
                                    }
                                    this.mLastConnectedBssid = newBssid;
                                    DhcpResults dhcpResults = HwSelfCureEngine.this.mWifiStateMachine.syncGetDhcpResults();
                                    if (dhcpResults == null || HwSelfCureEngine.this.mArpClient == null || !HwSelfCureEngine.this.mArpClient.doGatewayArpTest((Inet4Address) dhcpResults.gateway)) {
                                        if (!HwSelfCureEngine.this.hasMessages(108)) {
                                            HwSelfCureEngine.this.LOGD("gateway changed or unknow, need to check http response!");
                                            HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                                            transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING);
                                            break;
                                        }
                                    } else {
                                        HwSelfCureEngine.this.LOGD("last gateway reachable, don't use http-get, gateway unchanged after roaming!");
                                        break;
                                    }
                                }
                            } else {
                                HwSelfCureEngine.this.LOGD("CMD_NETWORK_ROAMING_DETECT rcvd, but user set static ip config, ignore it.");
                                break;
                            }
                        } else {
                            HwSelfCureEngine.this.LOGD("CMD_NETWORK_ROAMING_DETECT rcvd, but bssid is unchanged, ignore it.");
                            break;
                        }
                    }
                    break;
                case 115:
                    if (HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2 || (this.mHasInternetRecently && (this.mConfigAuthType == 1 || this.mConfigAuthType == 4))) {
                        checkHttpResponseAndSelfCure(115);
                        break;
                    }
                case 118:
                    HwSelfCureEngine.this.LOGD("CMD_RESETUP_SELF_CURE_MONITOR rcvd");
                    setupSelfCureMonitor();
                    break;
                case 121:
                    if (!this.mMobileHotspot && !this.mGatewayInvalid && !HwSelfCureEngine.this.mIsWifiBackground.get()) {
                        updateInternetAccessHistory();
                        handleNoTcpRxDetected();
                        break;
                    }
                case 122:
                    if (((Boolean) message.obj).booleanValue()) {
                        HwSelfCureEngine.this.LOGD("CMD_INTERNET_FAILURE_DETECTED rcvd, delete dhcp cache");
                        HwSelfCureEngine.this.mWifiStateMachine.handleNoInternetIp();
                    }
                    if (!this.mMobileHotspot && !this.mGatewayInvalid) {
                        handleInternetFailedDetected(message);
                        break;
                    }
                case 123:
                    handleDnsFailedMonitor();
                    break;
                case 125:
                    updateDhcpResultsByBssid(HwSelfCureEngine.this.mWifiStateMachine.getCurrentBSSID(), (String) message.obj);
                    break;
                case 127:
                    handleNewScanResults();
                    break;
                case HwSelfCureEngine.CMD_INVALID_IP_CONFIRM /*129*/:
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (!HwSelfCureEngine.this.isHttpReachable(false)) {
                        transitionToSelfCureState(HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2 ? HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY : HwSelfCureEngine.INTERNET_FAILED_INVALID_IP);
                        break;
                    } else {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                        int unused = HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                        break;
                    }
                case HwSelfCureEngine.CMD_DNS_FAILED_DETECTED /*133*/:
                    if (!this.mMobileHotspot && !this.mGatewayInvalid && !HwSelfCureEngine.this.mIsWifiBackground.get() && !HwSelfCureEngine.this.mInternetUnknown) {
                        updateInternetAccessHistory();
                        handleDnsFailedDetected();
                        break;
                    }
                case HwSelfCureEngine.CMD_HTTP_REACHABLE_RCV /*136*/:
                    WifiConfiguration current = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
                    if (current != null) {
                        current.internetSelfCureHistory = "0|0|0|0|0|0|0|0|0|0|0|0|0|0";
                        current.validatedInternetAccess = true;
                        current.noInternetAccess = false;
                        current.wifiProNoInternetAccess = false;
                        current.wifiProNoInternetReason = 0;
                        HwSelfCureEngine.this.mWifiStateMachine.sendMessage(HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS, current);
                        break;
                    }
                    break;
                case HwSelfCureEngine.CMD_ARP_FAILED_DETECTED /*139*/:
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (!HwSelfCureEngine.this.isHttpReachable(false)) {
                        transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP);
                        break;
                    } else {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                        break;
                    }
                case HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED /*306*/:
                    HwSelfCureEngine.this.periodicArpDetection();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwSelfCureEngine.this.LOGD("ConnectedMonitorState exit");
            HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED);
        }

        private void handleNewScanResults() {
            boolean z;
            List<ScanResult> scanResults = HwSelfCureEngine.this.mWifiManager.getScanResults();
            List<WifiConfiguration> configNetworks = HwSelfCureEngine.this.mWifiManager.getConfiguredNetworks();
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            String bssid = WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager);
            String ssid = WifiProCommonUtils.getCurrentSsid(HwSelfCureEngine.this.mWifiManager);
            if (config != null) {
                if (WifiProCommonUtils.isAllowWifiSwitch(scanResults, configNetworks, bssid, ssid, config.configKey(), -75)) {
                    z = true;
                    this.mWifiSwitchAllowed = z;
                }
            }
            z = false;
            this.mWifiSwitchAllowed = z;
        }

        private boolean setupSelfCureMonitor() {
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            boolean gatewayChanged = false;
            if (config == null) {
                return false;
            }
            WifiConfiguration unused = HwSelfCureEngine.this.mSelfCureConfig = config;
            this.mConfigAuthType = config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType();
            this.mUserSetStaticIpConfig = config.getIpAssignment() != null && config.getIpAssignment() == IpConfiguration.IpAssignment.STATIC;
            boolean unused2 = HwSelfCureEngine.this.mInternetUnknown = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 103);
            updateInternetAccessHistory();
            HwSelfCureEngine.this.LOGD("ConnectedMonitorState, hasInternet = " + this.mHasInternetRecently + ", portalUnthen = " + this.mPortalUnthenEver + ", userSetStaticIp = " + this.mUserSetStaticIpConfig + ", history empty = " + HwSelfCureEngine.this.mInternetUnknown);
            if (!this.mMobileHotspot) {
                DhcpResults dhcpResults = HwSelfCureEngine.this.mWifiStateMachine.syncGetDhcpResults();
                this.mIpv6DnsEnabled = parseIpv6Enabled(dhcpResults);
                this.mGatewayInvalid = dhcpResults == null || dhcpResults.gateway == null;
                HwSelfCureEngine.this.LOGD("ConnectedMonitorState, gatewayInvalid = " + this.mGatewayInvalid);
                if (HwSelfCureEngine.this.mIsWifiBackground.get() || HwSelfCureEngine.this.mStaticIpCureSuccess || ((!this.mHasInternetRecently && !HwSelfCureEngine.this.mInternetUnknown) || !HwSelfCureEngine.this.isIpAddressInvalid())) {
                    StaticIpConfiguration staticIpConfig = WifiProCommonUtils.dhcpResults2StaticIpConfig(config.lastDhcpResults);
                    if ((this.mUserSetStaticIpConfig || dhcpResults == null || staticIpConfig == null || dhcpResults.gateway == null || staticIpConfig.gateway == null || staticIpConfig.ipAddress == null || staticIpConfig.dnsServers == null) ? false : true) {
                        String currentGateway = dhcpResults.gateway.getHostAddress();
                        String lastGateway = staticIpConfig.gateway.getHostAddress();
                        HwSelfCureEngine.this.LOGD("ConnectedMonitorState, currentGateway = " + currentGateway + ", lastGateway = " + lastGateway);
                        if (!HwSelfCureEngine.this.mStaticIpCureSuccess && currentGateway != null && lastGateway != null && !currentGateway.equals(lastGateway)) {
                            gatewayChanged = true;
                        }
                        if (gatewayChanged) {
                            HwSelfCureEngine.this.LOGD("ConnectedMonitorState, current gateway is different with history gateway that has internet.");
                            HwSelfCureEngine.this.sendMessageDelayed(115, 300);
                            return true;
                        }
                    } else if (TextUtils.isEmpty(config.lastDhcpResults) && HwSelfCureEngine.this.mInternetUnknown) {
                        HwSelfCureEngine.this.sendMessageDelayed(115, 300);
                        return true;
                    }
                } else {
                    HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INVALID_IP_CONFIRM, 2000);
                    return true;
                }
            }
            if (!this.mMobileHotspot && !HwSelfCureEngine.this.mIsWifiBackground.get() && !HwSelfCureEngine.this.mStaticIpCureSuccess && this.mHasInternetRecently) {
                HwSelfCureEngine.this.sendMessageDelayed(123, 6000);
            }
            return true;
        }

        private boolean parseIpv6Enabled(DhcpResults dhcpResults) {
            if (dhcpResults == null || dhcpResults.dnsServers == null || dhcpResults.dnsServers.size() == 0) {
                return true;
            }
            for (int i = 0; i < dhcpResults.dnsServers.size(); i++) {
                InetAddress dns = (InetAddress) dhcpResults.dnsServers.get(i);
                if (dns != null && dns.getHostAddress() != null && ((dns instanceof Inet6Address) || dns.getHostAddress().contains(":"))) {
                    return true;
                }
            }
            return false;
        }

        private void handleDnsFailedMonitor() {
            if (this.mLastSignalLevel <= 1) {
                this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                HwSelfCureEngine.this.sendMessageDelayed(123, 6000);
                return;
            }
            int currentDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
            int deltaFailedDns = currentDnsFailedCounter - this.mLastDnsFailedCounter;
            this.mLastDnsFailedCounter = currentDnsFailedCounter;
            int currentDnsRefuseCounter = HwSelfCureUtils.getCurrentDnsRefuseCounter();
            int deltaDnsResfuseDns = currentDnsRefuseCounter - this.mLastDnsRefuseCounter;
            this.mLastDnsRefuseCounter = currentDnsRefuseCounter;
            if (!this.mGatewayInvalid && (deltaFailedDns >= 2 || deltaDnsResfuseDns >= 2)) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (HwSelfCureEngine.this.isHttpReachable(true)) {
                    int unused = HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                } else {
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    hwSelfCureEngine.LOGD("handleDnsFailedMonitor, deltaFailedDns = " + deltaFailedDns + ", and HTTP unreachable, transition to SelfCureState.");
                    transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
                }
            }
        }

        private void checkHttpResponseAndSelfCure(int eventType) {
            HwSelfCureEngine.this.mSelfCureOngoing.set(true);
            if (!HwSelfCureEngine.this.isHttpReachable(false)) {
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.LOGD("checkHttpResponseAndSelfCure, HTTP unreachable for eventType = " + eventType + ", dhcp offer size = " + HwSelfCureEngine.this.mDhcpOfferPackets.size());
                int internetFailedReason = 300;
                if (eventType == 110) {
                    internetFailedReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING;
                } else if (eventType == 115) {
                    internetFailedReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY;
                }
                transitionToSelfCureState(internetFailedReason);
                return;
            }
            HwSelfCureEngine hwSelfCureEngine2 = HwSelfCureEngine.this;
            hwSelfCureEngine2.LOGD("checkHttpResponseAndSelfCure, HTTP reachable for eventType = " + eventType);
            this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
            int unused = HwSelfCureEngine.this.mNoTcpRxCounter = 0;
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
        }

        private void handleDnsFailedDetected() {
            if (this.mLastSignalLevel > 2) {
                HwSelfCureEngine.this.LOGD("handleDnsFailedDetected, start scan and parse the context for wifi 2 wifi.");
                HwSelfCureEngine.this.startScan();
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (HwSelfCureEngine.this.isHttpReachable(false)) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    int unused = HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                } else {
                    handleNewScanResults();
                    if (this.mWifiSwitchAllowed) {
                        HwSelfCureEngine.this.LOGD("handleDnsFailedDetected, notify WLAN+ to do wifi swtich first.");
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                        return;
                    }
                    HwSelfCureEngine.this.LOGD("handleDnsFailedDetected, HTTP unreachable, transition to SelfCureState.");
                    transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
                }
            }
        }

        private void handleNoTcpRxDetected() {
            if (this.mLastSignalLevel > 2) {
                HwSelfCureEngine.access$1012(HwSelfCureEngine.this, 1);
                if (HwSelfCureEngine.this.mNoTcpRxCounter == 1) {
                    this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, start scan and parse the context for wifi 2 wifi.");
                    HwSelfCureEngine.this.startScan();
                } else if (HwSelfCureEngine.this.mNoTcpRxCounter == 2 && this.mWifiSwitchAllowed) {
                    if (HwSelfCureEngine.this.isHttpReachable(false)) {
                        this.mWifiSwitchAllowed = false;
                        int unused = HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    } else {
                        HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, notify WLAN+ to do wifi swtich first.");
                        HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    }
                } else if (this.mHasInternetRecently || this.mPortalUnthenEver) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (HwSelfCureEngine.this.isHttpReachable(true)) {
                        int unused2 = HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        return;
                    }
                    HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, HTTP unreachable, transition to SelfCureState.");
                    transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP);
                }
            }
        }

        private void handleInternetFailedDetected(Message message) {
            boolean access$3000 = HwSelfCureEngine.this.mStaticIpCureSuccess;
            int i = HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS;
            if (access$3000 || !((Boolean) message.obj).booleanValue()) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (HwSelfCureEngine.this.isHttpReachable(false)) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    int unused = HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                } else {
                    if (HwSelfCureEngine.this.mHwWifiCHRService != null) {
                        HwSelfCureEngine.this.mHwWifiCHRService.updateAccessWebException(0, this.mPortalUnthenEver ? "ERROR_PORTAL" : "OTHER");
                        HwSelfCureEngine.this.mHwWifiCHRService.incrAccessWebRecord(0, false, this.mPortalUnthenEver);
                    }
                    HwSelfCureEngine.this.LOGD("handleInternetFailedDetected, HTTP unreachable, transition to SelfCureState.");
                    int currentDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    int deltaFailedDns = currentDnsFailedCounter - this.mLastDnsFailedCounter;
                    this.mLastDnsFailedCounter = currentDnsFailedCounter;
                    int currentDnsRefuseCounter = HwSelfCureUtils.getCurrentDnsRefuseCounter();
                    int deltaDnsResfuseDns = currentDnsRefuseCounter - this.mLastDnsRefuseCounter;
                    this.mLastDnsRefuseCounter = currentDnsRefuseCounter;
                    if (deltaFailedDns < 2 && deltaDnsResfuseDns < 2) {
                        i = HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP;
                    }
                    transitionToSelfCureState(i);
                }
                return;
            }
            if (this.mHasInternetRecently || this.mPortalUnthenEver) {
                HwSelfCureEngine.this.LOGD("handleInternetFailedDetected, wifi has no internet when connected.");
                transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
            } else if (HwSelfCureEngine.this.mInternetUnknown && HwSelfCureEngine.this.multiGateway()) {
                HwSelfCureEngine.this.LOGD("handleInternetFailedDetected, multi gateway due to no internet access.");
                transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP);
            }
        }

        private void transitionToSelfCureState(int reason) {
            if (SystemProperties.getBoolean(HwSelfCureEngine.PROP_DISABLE_SELF_CURE, false) || !HwSelfCureEngine.this.mIsCaptivePortalCheckEnabled || this.mIpv6DnsEnabled || "factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.LOGD("transitionToSelfCureState, don't support SCE, do nothing or mIpv6DnsEnabled =" + this.mIpv6DnsEnabled);
                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                return;
            }
            Message dmsg = Message.obtain();
            dmsg.what = 112;
            dmsg.arg1 = reason;
            HwSelfCureEngine.this.sendMessageDelayed(dmsg, 100);
            HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mInternetSelfCureState);
        }

        private void updateInternetAccessHistory() {
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (config != null) {
                this.mHasInternetRecently = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
                this.mPortalUnthenEver = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102);
            }
        }

        private void updateDhcpResultsByBssid(String bssid, String dhcpResults) {
            if (bssid != null && dhcpResults != null) {
                PortalDataBaseManager database = PortalDataBaseManager.getInstance(HwSelfCureEngine.this.mContext);
                if (database != null) {
                    database.updateDhcpResultsByBssid(bssid, dhcpResults);
                }
            }
        }
    }

    class ConnectionSelfCureState extends State {
        ConnectionSelfCureState() {
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: java.lang.String} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: android.net.wifi.WifiConfiguration} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v7 */
        /* JADX WARNING: type inference failed for: r3v8 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 119) {
                ? r3 = 0;
                switch (i) {
                    case 103:
                        if (message.obj != null) {
                            r3 = (String) message.obj;
                        }
                        handleConnSelfCureFailed(r3);
                        break;
                    case 104:
                        if (HwSelfCureEngine.this.hasMessages(103)) {
                            HwSelfCureEngine.this.LOGD("CMD_CURE_CONNECTED_TIMEOUT msg removed");
                            HwSelfCureEngine.this.removeMessages(103);
                        }
                        HwSelfCureEngine.this.handleNetworkConnected();
                        break;
                    case 105:
                        if (HwSelfCureEngine.this.hasMessages(103)) {
                            HwSelfCureEngine.this.LOGD("CMD_CURE_CONNECTED_TIMEOUT msg removed");
                            HwSelfCureEngine.this.removeMessages(103);
                        }
                        HwSelfCureEngine.this.handleWifiDisabled(true);
                        break;
                    default:
                        switch (i) {
                            case 107:
                                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                                if (message.obj != null) {
                                    r3 = (WifiConfiguration) message.obj;
                                }
                                hwSelfCureEngine.handleNetworkRemoved(r3);
                                break;
                            case 108:
                                HwSelfCureEngine.this.LOGD("CMD_NETWORK_DISCONNECTED_RCVD during connection self cure state.");
                                if (HwSelfCureEngine.this.hasMessages(119)) {
                                    HwSelfCureEngine.this.removeMessages(119);
                                }
                                handleConnSelfCureFailed(HwSelfCureEngine.this.mConnectionCureConfigKey);
                                break;
                            default:
                                return false;
                        }
                }
            } else {
                boolean unused = HwSelfCureEngine.this.updateConnSelfCureFailedHistory();
            }
            return true;
        }

        private void handleConnSelfCureFailed(String configKey) {
            HwSelfCureEngine.this.LOGD("ENTER: handleConnSelfCureFailed(), configKey = " + configKey);
            if (configKey != null) {
                int unused = HwSelfCureEngine.this.mNoAutoConnCounter = 0;
                HwSelfCureEngine.this.autoConnectFailedNetworks.clear();
                HwSelfCureEngine.this.autoConnectFailedNetworksRssi.clear();
                CureFailedNetworkInfo cureHistory = (CureFailedNetworkInfo) HwSelfCureEngine.this.networkCureFailedHistory.get(configKey);
                if (cureHistory != null) {
                    cureHistory.cureFailedCounter++;
                    cureHistory.lastCureFailedTime = System.currentTimeMillis();
                } else {
                    HwSelfCureEngine.this.networkCureFailedHistory.put(configKey, new CureFailedNetworkInfo(configKey, 1, System.currentTimeMillis()));
                    HwSelfCureEngine.this.LOGD("handleConnSelfCureFailed, networkCureFailedHistory added, configKey = " + configKey);
                }
            }
            if (!(HwSelfCureEngine.this.mNoAutoConnReason == -1 || HwSelfCureEngine.this.mHwWifiCHRService == null)) {
                HwSelfCureEngine.this.mHwWifiCHRService.updateScCHRCount(HwSelfCureEngine.this.mNoAutoConnReason);
            }
            HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mDisconnectedMonitorState);
        }
    }

    static class CureFailedNetworkInfo {
        public String configKey;
        public int cureFailedCounter;
        public long lastCureFailedTime;

        public CureFailedNetworkInfo(String key, int counter, long time) {
            this.configKey = key;
            this.cureFailedCounter = counter;
            this.lastCureFailedTime = time;
        }

        public String toString() {
            return "[ " + ("configKey = " + this.configKey) + (", cureFailedCounter = " + this.cureFailedCounter) + (", lastCureFailedTime = " + DateFormat.getDateTimeInstance().format(new Date(this.lastCureFailedTime))) + " ]";
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            if (message.what == 126) {
                handleDhcpOfferPacketRcv((String) message.obj);
            }
            return true;
        }

        private void handleDhcpOfferPacketRcv(String dhcpResutls) {
            if (dhcpResutls != null && HwSelfCureEngine.this.isSuppOnCompletedState() && !HwSelfCureEngine.this.mWifiStateMachine.isWifiProEvaluatingAP()) {
                String gateway = WifiProCommonUtils.dhcpResults2Gateway(dhcpResutls);
                if (gateway != null) {
                    HwSelfCureEngine.this.mDhcpOfferPackets.put(gateway.replace("/", ""), dhcpResutls);
                }
            }
        }
    }

    class DisconnectedMonitorState extends State {
        private boolean mSetStaticIpConfig;

        DisconnectedMonitorState() {
        }

        public void enter() {
            HwSelfCureEngine.this.LOGD("==> ##DisconnectedMonitorState");
            boolean unused = HwSelfCureEngine.this.mStaticIpCureSuccess = false;
            int unused2 = HwSelfCureEngine.this.mNoAutoConnCounter = 0;
            int unused3 = HwSelfCureEngine.this.mNoAutoConnReason = -1;
            WifiConfiguration unused4 = HwSelfCureEngine.this.mNoAutoConnConfig = null;
            WifiConfiguration unused5 = HwSelfCureEngine.this.mSelfCureConfig = null;
            String unused6 = HwSelfCureEngine.this.mConnectionCureConfigKey = null;
            HwSelfCureEngine.this.mIsWifiBackground.set(false);
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            this.mSetStaticIpConfig = false;
            long unused7 = HwSelfCureEngine.this.mConnectedTimeMills = 0;
            HwSelfCureEngine.this.mDhcpOfferPackets.clear();
            HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
            HwSelfCureEngine.this.mRouterInternetDetector.notifyDisconnected();
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v14, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: android.net.wifi.WifiConfiguration} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean processMessage(Message message) {
            WifiConfiguration wifiConfiguration = null;
            switch (message.what) {
                case 101:
                    if (!HwSelfCureEngine.this.isConnectingOrConnected() && !HwSelfCureUtils.isOnWlanSettings(HwSelfCureEngine.this.mContext)) {
                        List<ScanResult> scanResults = HwSelfCureEngine.this.mWifiManager.getScanResults();
                        updateAutoConnFailedNetworks(scanResults);
                        HwSelfCureUtils.selectDisabledNetworks(scanResults, HwSelfCureEngine.this.mWifiManager.getConfiguredNetworks(), HwSelfCureEngine.this.autoConnectFailedNetworks, HwSelfCureEngine.this.autoConnectFailedNetworksRssi, HwSelfCureEngine.this.mWifiStateMachine);
                        selectHighestFailedNetworkAndCure();
                        break;
                    }
                case 102:
                    if (!HwSelfCureUtils.isOnWlanSettings(HwSelfCureEngine.this.mContext)) {
                        if (message.obj != null) {
                            wifiConfiguration = message.obj;
                        }
                        trySelfCureSelectedNetwork(wifiConfiguration);
                        break;
                    }
                    break;
                case 104:
                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT)) {
                        HwSelfCureEngine.this.LOGD("CMD_INVALID_DHCP_OFFER_EVENT msg removed because of rcv other Dhcp Offer.");
                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT);
                    }
                    HwSelfCureEngine.this.handleNetworkConnected();
                    break;
                case 105:
                    HwSelfCureEngine.this.handleWifiDisabled(false);
                    break;
                case 106:
                    HwSelfCureEngine.this.handleWifiEnabled();
                    break;
                case 107:
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    if (message.obj != null) {
                        wifiConfiguration = (WifiConfiguration) message.obj;
                    }
                    hwSelfCureEngine.handleNetworkRemoved(wifiConfiguration);
                    break;
                case 117:
                    if (this.mSetStaticIpConfig) {
                        this.mSetStaticIpConfig = false;
                        if (HwSelfCureEngine.this.mHwWifiCHRService != null) {
                            HwSelfCureEngine.this.mHwWifiCHRService.updateScCHRCount(12);
                            HwSelfCureEngine.this.mHwWifiCHRService.updateScCHRCount(25);
                        }
                        if (HwSelfCureEngine.this.mWifiStateMachine != null) {
                            HwSelfCureEngine.this.requestArpConflictTest(HwSelfCureEngine.this.mWifiStateMachine.syncGetDhcpResults());
                            break;
                        }
                    }
                    break;
                case 124:
                    String currentBSSID = HwSelfCureEngine.this.mWifiStateMachine.getCurrentBSSID();
                    if (message.obj != null) {
                        wifiConfiguration = (WifiConfiguration) message.obj;
                    }
                    this.mSetStaticIpConfig = handleIpConfigLost(currentBSSID, wifiConfiguration);
                    break;
                case HwSelfCureEngine.CMD_USER_PRESENT_RCVD /*134*/:
                    handleUserPresentEvent();
                    break;
                case HwSelfCureEngine.CMD_BSSID_DHCP_FAILED_EVENT /*135*/:
                    handleBssidDhcpFailed((WifiConfiguration) message.obj);
                    break;
                case HwSelfCureEngine.CMD_USER_ENTER_WLAN_SETTINGS /*137*/:
                    HwSelfCureEngine.this.enableAllNetworksByEnterSettings(new int[]{3, 2, 4});
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void handleUserPresentEvent() {
            if (HwSelfCureEngine.this.mWifiManager != null && HwSelfCureEngine.this.mWifiManager.isWifiEnabled() && !HwSelfCureEngine.this.isConnectingOrConnected() && !WifiProCommonUtils.isQueryActivityMatched(HwSelfCureEngine.this.mContext, "com.android.settings.Settings$WifiSettingsActivity")) {
                HwSelfCureEngine.this.LOGD("ENTER: handleUserPresentEvent()");
                HwSelfCureEngine.this.enableAllNetworksByReason(new int[]{2, 3, 4});
            }
        }

        private boolean handleIpConfigLost(String bssid, WifiConfiguration config) {
            if (!(bssid == null || config == null)) {
                String dhcpResults = null;
                PortalDataBaseManager database = PortalDataBaseManager.getInstance(HwSelfCureEngine.this.mContext);
                if (database != null) {
                    dhcpResults = database.syncQueryDhcpResultsByBssid(bssid);
                }
                if (dhcpResults == null) {
                    dhcpResults = config.lastDhcpResults;
                }
                if (dhcpResults != null) {
                    HwSelfCureEngine.this.mWifiStateMachine.requestUseStaticIpConfig(WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResults));
                    return true;
                }
            }
            return false;
        }

        private void handleBssidDhcpFailed(WifiConfiguration config) {
            synchronized (HwSelfCureEngine.this.mDhcpFailedBssidLock) {
                String bssid = WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager);
                if (bssid != null && !HwSelfCureEngine.this.mDhcpFailedBssids.contains(bssid)) {
                    int bssidCnt = WifiProCommonUtils.getBssidCounter(config, HwSelfCureEngine.this.getScanResults());
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    hwSelfCureEngine.LOGD("handleBssidDhcpFailed, bssidCnt = " + bssidCnt);
                    if (bssidCnt >= 2) {
                        HwSelfCureEngine hwSelfCureEngine2 = HwSelfCureEngine.this;
                        hwSelfCureEngine2.LOGD("handleBssidDhcpFailed, add key = " + config.configKey());
                        HwSelfCureEngine.this.mDhcpFailedBssids.add(bssid);
                        if (config.configKey() != null && !HwSelfCureEngine.this.mDhcpFailedConfigKeys.contains(config.configKey())) {
                            HwSelfCureEngine.this.mDhcpFailedConfigKeys.add(config.configKey());
                        }
                    }
                }
            }
        }

        private void updateAutoConnFailedNetworks(List<ScanResult> scanResults) {
            List<String> refreshedNetworksKey = HwSelfCureUtils.getRefreshedCureFailedNetworks(HwSelfCureEngine.this.networkCureFailedHistory);
            for (int i = 0; i < refreshedNetworksKey.size(); i++) {
                HwSelfCureEngine.this.LOGD("updateAutoConnFailedNetworks, refreshed cure failed network, currKey = " + refreshedNetworksKey.get(i));
                HwSelfCureEngine.this.networkCureFailedHistory.remove(refreshedNetworksKey.get(i));
            }
            List<String> unstableKey = HwSelfCureUtils.searchUnstableNetworks(HwSelfCureEngine.this.autoConnectFailedNetworks, scanResults);
            for (int j = 0; j < unstableKey.size(); j++) {
                HwSelfCureEngine.this.LOGD("updateAutoConnFailedNetworks, remove it due to signal unstable, currKey = " + unstableKey.get(j));
                HwSelfCureEngine.this.autoConnectFailedNetworks.remove(unstableKey.get(j));
                HwSelfCureEngine.this.autoConnectFailedNetworksRssi.remove(unstableKey.get(j));
            }
        }

        private void selectHighestFailedNetworkAndCure() {
            if (HwSelfCureEngine.this.autoConnectFailedNetworks.size() == 0) {
                int unused = HwSelfCureEngine.this.mNoAutoConnCounter = 0;
            } else if (HwSelfCureEngine.access$3604(HwSelfCureEngine.this) < 3) {
                HwSelfCureEngine.this.LOGD("selectHighestFailedNetworkAndCure, MAX_FAILED_CURE unmatched, wait more time for self cure.");
            } else {
                WifiConfiguration bestSelfCureCandidate = HwSelfCureUtils.selectHighestFailedNetwork(HwSelfCureEngine.this.networkCureFailedHistory, HwSelfCureEngine.this.autoConnectFailedNetworks, HwSelfCureEngine.this.autoConnectFailedNetworksRssi);
                if (bestSelfCureCandidate != null) {
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    hwSelfCureEngine.LOGD("selectHighestFailedNetworkAndCure, delay 1s to self cure the selected candidate = " + bestSelfCureCandidate.configKey());
                    Message dmsg = Message.obtain();
                    dmsg.what = 102;
                    dmsg.obj = bestSelfCureCandidate;
                    HwSelfCureEngine.this.sendMessageDelayed(dmsg, 100);
                }
            }
        }

        private void trySelfCureSelectedNetwork(WifiConfiguration config) {
            if (!(config == null || config.networkId == -1 || HwSelfCureEngine.this.isConnectingOrConnected())) {
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.LOGD("ENTER: trySelfCureSelectedNetwork(), config = " + config.configKey());
                if (WifiCommonUtils.doesNotWifiConnectRejectByCust(config.getNetworkSelectionStatus(), config.SSID, HwSelfCureEngine.this.mContext)) {
                    Log.d(HwSelfCureEngine.TAG, "trySelfCureSelectedNetwork can not connect wifi");
                    return;
                }
                if (WifiProCommonUtils.isWifiProSwitchOn(HwSelfCureEngine.this.mContext)) {
                    if (WifiProCommonUtils.isOpenAndPortal(config) || WifiProCommonUtils.isOpenAndMaybePortal(config)) {
                        HwSelfCureEngine.this.mWifiStateMachine.setWifiBackgroundReason(0);
                        HwSelfCureEngine hwSelfCureEngine2 = HwSelfCureEngine.this;
                        hwSelfCureEngine2.LOGD("trySelfCureSelectedNetwork, self cure at background, due to [maybe] portal, candidate = " + config.configKey());
                    } else if (config.noInternetAccess && !WifiProCommonUtils.allowWifiConfigRecovery(config.internetHistory)) {
                        HwSelfCureEngine.this.mWifiStateMachine.setWifiBackgroundReason(3);
                        HwSelfCureEngine hwSelfCureEngine3 = HwSelfCureEngine.this;
                        hwSelfCureEngine3.LOGD("trySelfCureSelectedNetwork, self cure at background, due to no internet, candidate = " + config.configKey());
                    }
                }
                HwSelfCureEngine.this.mWifiStateMachine.startConnectToUserSelectNetwork(config.networkId, Binder.getCallingUid(), null);
                int chrType = -1;
                WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                int disableReason = status.getNetworkSelectionDisableReason();
                if (disableReason == 3) {
                    chrType = 10;
                } else if (disableReason == 2) {
                    chrType = 11;
                } else if (disableReason == 4) {
                    chrType = 12;
                } else if (disableReason == 11) {
                    chrType = 13;
                } else if (status.isNetworkEnabled()) {
                    chrType = 24;
                }
                if (chrType != -1) {
                    int unused = HwSelfCureEngine.this.mNoAutoConnReason = chrType;
                    WifiConfiguration unused2 = HwSelfCureEngine.this.mNoAutoConnConfig = config;
                }
                String unused3 = HwSelfCureEngine.this.mConnectionCureConfigKey = config.configKey();
                Message dmsg = Message.obtain();
                dmsg.what = 103;
                dmsg.obj = config.configKey();
                HwSelfCureEngine.this.sendMessageDelayed(dmsg, 20000);
                HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectionSelfCureState);
            }
        }
    }

    static class InternetSelfCureHistoryInfo {
        public int dnsSelfCureFailedCnt = 0;
        public long lastDnsSelfCureFailedTs = 0;
        public long lastReassocSelfCureConnectFailedTs = 0;
        public long lastReassocSelfCureFailedTs = 0;
        public long lastRenewDhcpSelfCureFailedTs = 0;
        public long lastResetSelfCureConnectFailedTs = 0;
        public long lastResetSelfCureFailedTs = 0;
        public long lastStaticIpSelfCureFailedTs = 0;
        public int reassocSelfCureConnectFailedCnt = 0;
        public int reassocSelfCureFailedCnt = 0;
        public int renewDhcpSelfCureFailedCnt = 0;
        public int resetSelfCureConnectFailedCnt = 0;
        public int resetSelfCureFailedCnt = 0;
        public int staticIpSelfCureFailedCnt = 0;

        public String toString() {
            return "[ " + ("dnsSelfCureFailedCnt = " + this.dnsSelfCureFailedCnt) + (", renewDhcpSelfCureFailedCnt = " + this.renewDhcpSelfCureFailedCnt) + (", staticIpSelfCureFailedCnt = " + this.staticIpSelfCureFailedCnt) + (", reassocSelfCureFailedCnt = " + this.reassocSelfCureFailedCnt) + (", resetSelfCureFailedCnt = " + this.resetSelfCureFailedCnt) + (", reassocSelfCureConnectFailedCnt = " + this.reassocSelfCureConnectFailedCnt) + (", resetSelfCureConnectFailedCnt = " + this.resetSelfCureConnectFailedCnt) + " ]";
        }
    }

    class InternetSelfCureState extends State {
        private int mConfigAuthType = -1;
        private boolean mConfigStaticIp4GatewayChanged = false;
        private boolean mConfigStaticIp4MultiDhcpServer = false;
        private int mCurrentAbnormalType;
        private String mCurrentBssid = null;
        private String mCurrentGateway;
        private int mCurrentRssi;
        private int mCurrentSelfCureLevel;
        private boolean mDelayedReassocSelfCure = false;
        private boolean mDelayedResetSelfCure = false;
        private boolean mFinalSelfCureUsed = false;
        private boolean mHasInternetRecently;
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
            HwSelfCureEngine.this.LOGD("==> ##InternetSelfCureState");
            this.mCurrentRssi = WifiHandover.INVALID_RSSI;
            boolean z = false;
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
                HwSelfCureEngine.this.LOGD("InternetSelfCureState, network = " + wifiInfo.getSSID() + ", signal rssi = " + this.mCurrentRssi);
            }
            HwArpUtils unused = HwSelfCureEngine.this.mHwArpUtils;
            HwArpUtils.checkWifiDefaultRoute();
            HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED, HidataWechatTraffic.MIN_VALID_TIME);
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (config != null) {
                this.mSelfCureHistoryInfo = HwSelfCureUtils.string2InternetSelfCureHistoryInfo(config.internetSelfCureHistory);
                this.mHasInternetRecently = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
                this.mPortalUnthenEver = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102);
                if (config.getIpAssignment() != null && config.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
                    z = true;
                }
                this.mUserSetStaticIpConfig = z;
                this.mLastHasInetTimeMillis = config.lastHasInternetTimestamp;
                if (config.allowedKeyManagement.cardinality() <= 1) {
                    i = config.getAuthType();
                }
                this.mConfigAuthType = i;
                HwSelfCureEngine.this.LOGD("InternetSelfCureState, hasInternet = " + this.mHasInternetRecently + ", portalUnthenEver = " + this.mPortalUnthenEver + ", userSetStaticIp = " + this.mUserSetStaticIpConfig + ", historyInfo = " + this.mSelfCureHistoryInfo + ", gw = " + this.mCurrentGateway);
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 108:
                    HwSelfCureEngine.this.LOGD("CMD_NETWORK_DISCONNECTED_RCVD during self cure state.");
                    HwSelfCureEngine.this.removeMessages(113);
                    HwSelfCureEngine.this.mWifiStateMachine.resetIpConfigStatus();
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mDisconnectedMonitorState);
                    break;
                case 109:
                    this.mCurrentRssi = message.arg1;
                    handleRssiChanged();
                    break;
                case 110:
                    handleRoamingDetected((String) message.obj);
                    break;
                case 112:
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    if (HwSelfCureEngine.this.isSuppOnCompletedState()) {
                        selectSelfCureByFailedReason(message.arg1);
                        break;
                    }
                    break;
                case 113:
                    if (this.mCurrentSelfCureLevel == 205 && HwABSUtils.getABSEnable()) {
                        HwABSDetectorService service = HwABSDetectorService.getInstance();
                        if (service != null) {
                            service.notifySelEngineResetCompelete();
                        }
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
                case 114:
                    if (HwSelfCureEngine.this.isSuppOnCompletedState()) {
                        this.mCurrentSelfCureLevel = message.arg1;
                        selfCureWifiLink(message.arg1);
                        HwSelfCureEngine.this.notifyVoWiFiSelCureBegin();
                        break;
                    }
                    break;
                case 116:
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    hwSelfCureEngine.LOGD("CMD_IP_CONFIG_TIMEOUT during self cure state. currentAbnormalType = " + this.mCurrentAbnormalType);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.mWifiStateMachine.resetIpConfigStatus();
                    if (this.mCurrentAbnormalType == 2 && ((this.mConfigAuthType == 1 || this.mConfigAuthType == 4) && WifiProCommonUtils.getBssidCounter(HwSelfCureEngine.this.mSelfCureConfig, HwSelfCureEngine.this.getScanResults()) <= 3 && !this.mFinalSelfCureUsed)) {
                        this.mFinalSelfCureUsed = true;
                        HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_DEAUTH_BSSID, 0);
                        break;
                    }
                case 117:
                    if (HwSelfCureEngine.this.hasMessages(116)) {
                        HwSelfCureEngine.this.LOGD("CMD_IP_CONFIG_TIMEOUT msg removed because of ip config success.");
                        HwSelfCureEngine.this.removeMessages(116);
                        HwSelfCureEngine.this.mWifiStateMachine.resetIpConfigStatus();
                        this.mCurrentGateway = getCurrentGateway();
                        HwSelfCureEngine.this.sendMessageDelayed(113, 2000);
                    }
                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT)) {
                        HwSelfCureEngine.this.LOGD("CMD_INVALID_DHCP_OFFER_EVENT msg removed because of rcv other Dhcp Offer.");
                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT);
                        break;
                    }
                    break;
                case 120:
                    HwSelfCureUtils.updateSelfCureConnectHistoryInfo(this.mSelfCureHistoryInfo, this.mCurrentSelfCureLevel, false);
                    updateWifiConfig(HwSelfCureEngine.this.mSelfCureConfig);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    uploadConnectFailedCounter(this.mCurrentSelfCureLevel);
                    break;
                case 122:
                    if (((Boolean) message.obj).booleanValue()) {
                        HwSelfCureEngine.this.LOGD("CMD_INTERNET_FAILURE_DETECTED rcvd under InternetSelfCureState, delete dhcp cache");
                        HwSelfCureEngine.this.mWifiStateMachine.handleNoInternetIp();
                        break;
                    }
                    break;
                case 128:
                    HwSelfCureEngine.this.LOGD("CMD_P2P_DISCONNECTED_EVENT during self cure state.");
                    handleRssiChanged();
                    break;
                case HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT /*130*/:
                    this.mSetStaticIp4InvalidIp = HwSelfCureEngine.this.handleInvalidDhcpOffer(this.mUnconflictedIp);
                    break;
                case HwSelfCureEngine.CMD_SETTINGS_DISPLAY_NO_INTERNET_EVENT /*131*/:
                    if (HwSelfCureEngine.this.mHwWifiCHRService != null) {
                        HwSelfCureEngine.this.mHwWifiCHRService.updateScCHRCount(28);
                        break;
                    }
                    break;
                case HwSelfCureEngine.CMD_ROUTER_GATEWAY_UNREACHABLE_EVENT /*132*/:
                    if (HwSelfCureEngine.this.mHwWifiCHRService != null) {
                        HwSelfCureEngine.this.mHwWifiCHRService.updateScCHRCount(27);
                        break;
                    }
                    break;
                case HwSelfCureEngine.CMD_HTTP_REACHABLE_RCV /*136*/:
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    if (HwSelfCureEngine.this.mSelfCureConfig != null) {
                        HwSelfCureEngine.this.mSelfCureConfig.internetSelfCureHistory = "0|0|0|0|0|0|0|0|0|0|0|0|0|0";
                        HwSelfCureEngine.this.mSelfCureConfig.validatedInternetAccess = true;
                        HwSelfCureEngine.this.mSelfCureConfig.noInternetAccess = false;
                        HwSelfCureEngine.this.mSelfCureConfig.wifiProNoInternetAccess = false;
                        HwSelfCureEngine.this.mSelfCureConfig.wifiProNoInternetReason = 0;
                        HwSelfCureEngine.this.mWifiStateMachine.sendMessage(HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS, HwSelfCureEngine.this.mSelfCureConfig);
                    }
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                    break;
                case HwSelfCureEngine.CMD_MULTIGW_SELFCURE /*138*/:
                    multiGatewaySelfcure();
                    break;
                case HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED /*306*/:
                    HwSelfCureEngine.this.periodicArpDetection();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwSelfCureEngine.this.LOGD("InternetSelfCureState exit");
            HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED);
        }

        private void saveCurrentAbnormalType(int internetFailedType) {
            int chrType = -1;
            if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                chrType = 0;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY) {
                chrType = 3;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                chrType = 2;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP) {
                chrType = 1;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_INVALID_IP) {
                chrType = 29;
            }
            if (chrType != -1) {
                this.mCurrentAbnormalType = chrType;
            }
        }

        private void uploadConnectFailedCounter(int connectType) {
            int chrType = -1;
            if (connectType == 204) {
                chrType = 18;
            } else if (connectType == 205) {
                chrType = 19;
            }
            if (chrType != -1 && HwSelfCureEngine.this.mHwWifiCHRService != null) {
                HwSelfCureEngine.this.mHwWifiCHRService.updateScCHRCount(chrType);
            }
        }

        private void uploadCurrentAbnormalStatistics() {
            if (this.mCurrentAbnormalType != -1 && HwSelfCureEngine.this.mHwWifiCHRService != null) {
                HwSelfCureEngine.this.mHwWifiCHRService.updateScCHRCount(this.mCurrentAbnormalType);
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
                if (this.mLastSelfCureLevel == 201) {
                    chrType = 20;
                } else if (this.mLastSelfCureLevel == 202) {
                    chrType = 21;
                } else if (this.mLastSelfCureLevel == 203) {
                    chrType = 22;
                } else if (this.mLastSelfCureLevel == 204) {
                    chrType = 8;
                }
            } else if (selfCureType == 208) {
                chrType = 8;
            }
            if (chrType != -1 && HwSelfCureEngine.this.mHwWifiCHRService != null) {
                HwSelfCureEngine.this.mHwWifiCHRService.updateScCHRCount(chrType);
            }
        }

        private void handleInternetFailedAndUserSetStaticIp(int internetFailedType) {
            if (!this.mHasInternetRecently || !HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                HwSelfCureEngine.this.LOGD("handleInternetFailedAndUserSetStaticIp, user set static ip config, ignore to update config for user.");
                if (!HwSelfCureEngine.this.mInternetUnknown) {
                    this.mCurrentAbnormalType = HwSelfCureUtils.RESET_REJECTED_BY_STATIC_IP_ENABLED;
                    uploadCurrentAbnormalStatistics();
                    return;
                }
                return;
            }
            saveCurrentAbnormalType(internetFailedType);
            if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY) {
                this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
            }
            HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
        }

        private int selectBestSelfCureSolution(int internetFailedType) {
            boolean noInternetWhenConnected = false;
            boolean multipleDhcpServer = HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2;
            if (this.mLastHasInetTimeMillis <= 0 || this.mLastHasInetTimeMillis < HwSelfCureEngine.this.mConnectedTimeMills) {
                noInternetWhenConnected = true;
            }
            HwSelfCureEngine.this.LOGD("selectBestSelfCureSolution, multipleDhcpServer = " + multipleDhcpServer + ", noInternetWhenConnected = " + noInternetWhenConnected);
            if (multipleDhcpServer && noInternetWhenConnected && getNextTestDhcpResults() != null && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) && (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS || internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP)) {
                this.mConfigStaticIp4MultiDhcpServer = true;
                return HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY && multipleDhcpServer && getNextTestDhcpResults() != null && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP)) {
                this.mConfigStaticIp4MultiDhcpServer = true;
                return HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY && ((this.mConfigAuthType == 1 || this.mConfigAuthType == 4) && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP))) {
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
                } else if (internetFailedType != HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP || !HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC)) {
                    return 200;
                } else {
                    return HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                }
            }
        }

        private boolean isNeedMultiGatewaySelfcure() {
            HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
            hwSelfCureEngine.LOGD("isNeedMultiGatewaySelfcure mUsedMultiGwSelfcure = " + this.mUsedMultiGwSelfcure);
            if (this.mUsedMultiGwSelfcure) {
                return false;
            }
            return HwSelfCureEngine.this.multiGateway();
        }

        private void multiGatewaySelfcure() {
            if (HwSelfCureEngine.this.isSuppOnCompletedState()) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mUsedMultiGwSelfcure = true;
                String mac = HwSelfCureEngine.this.mHWMultiGW.getNextGWMACAddr();
                String gateway = HwSelfCureEngine.this.mHWMultiGW.getGWIPAddr();
                if (mac == null || gateway == null) {
                    Log.d(HwSelfCureEngine.TAG, "multi gateway selfcure failed.");
                    if (this.mLastMultiGwselfFailedType != -1) {
                        selectSelfCureByFailedReason(this.mLastMultiGwselfFailedType);
                    }
                } else {
                    Log.d(HwSelfCureEngine.TAG, "start multi gateway selfcure");
                    WifiInjector.getInstance().getWifiNative().setStaticARP(gateway, mac);
                    HwSelfCureEngine.this.flushVmDnsCache();
                    if (!HwSelfCureEngine.this.isHttpReachable(false)) {
                        Log.d(HwSelfCureEngine.TAG, "multi gateway selfcure failed , delStaticARP!");
                        WifiInjector.getInstance().getWifiNative().delStaticARP(gateway);
                        HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_MULTIGW_SELFCURE);
                    } else {
                        Log.d(HwSelfCureEngine.TAG, "multi gateway selfcure success!");
                        HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                        HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                    }
                }
                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            }
        }

        private void selectSelfCureByFailedReason(int internetFailedType) {
            HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
            hwSelfCureEngine.LOGD("selectSelfCureByFailedReason, internetFailedType = " + internetFailedType + ", userSetStaticIp = " + this.mUserSetStaticIpConfig);
            if (isNeedMultiGatewaySelfcure()) {
                HwSelfCureEngine.this.LOGD("Start multi gateway selfcure.");
                this.mLastMultiGwselfFailedType = internetFailedType;
                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_MULTIGW_SELFCURE);
            } else if (!this.mUserSetStaticIpConfig || !(internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS || internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY || internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING)) {
                int requestSelfCureLevel = selectBestSelfCureSolution(internetFailedType);
                if (requestSelfCureLevel != 200) {
                    saveCurrentAbnormalType(internetFailedType);
                    HwSelfCureEngine.this.sendMessage(114, requestSelfCureLevel, 0);
                } else if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                    HwSelfCureEngine hwSelfCureEngine2 = HwSelfCureEngine.this;
                    hwSelfCureEngine2.LOGD("selectSelfCureByFailedReason, use wifi reset to cure this failed type = " + internetFailedType);
                    saveCurrentAbnormalType(internetFailedType);
                    if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                        this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
                    } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                        this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
                    } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP) {
                        this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                    }
                    HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
                } else {
                    HwSelfCureEngine hwSelfCureEngine3 = HwSelfCureEngine.this;
                    hwSelfCureEngine3.LOGD("selectSelfCureByFailedReason, no usable self cure for this failed type = " + internetFailedType);
                    handleHttpUnreachableFinally();
                }
            } else {
                handleInternetFailedAndUserSetStaticIp(internetFailedType);
            }
        }

        private boolean confirmInternetSelfCure(int currentCureLevel) {
            HwSelfCureEngine.this.LOGD("confirmInternetSelfCure, cureLevel = " + currentCureLevel + ", last failed counter = " + this.mSelfCureFailedCounter + ", finally = " + this.mFinalSelfCureUsed);
            if (currentCureLevel != 200) {
                if (!HwSelfCureEngine.this.isHttpReachable(true)) {
                    this.mSelfCureFailedCounter++;
                    HwSelfCureUtils.updateSelfCureHistoryInfo(this.mSelfCureHistoryInfo, currentCureLevel, false);
                    updateWifiConfig(null);
                    HwSelfCureEngine.this.LOGD("HTTP unreachable, self cure failed for " + currentCureLevel + ", selfCureHistoryInfo = " + this.mSelfCureHistoryInfo);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    if (this.mFinalSelfCureUsed) {
                        handleHttpUnreachableFinally();
                        return false;
                    } else if (currentCureLevel != 205) {
                        if (currentCureLevel == 203) {
                            if (getNextTestDhcpResults() != null) {
                                this.mLastSelfCureLevel = currentCureLevel;
                                HwSelfCureEngine.this.LOGD("HTTP unreachable, and has next dhcp results, try next one.");
                                HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP, 0);
                                return false;
                            }
                            this.mConfigStaticIp4MultiDhcpServer = false;
                            if (this.mCurrentAbnormalType == 0) {
                                this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
                                if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS)) {
                                    HwSelfCureEngine.this.LOGD("HTTP unreachable, and no next dhcp results, use dns replace to cure for dns failed.");
                                    HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS, 0);
                                    return false;
                                }
                            } else if (this.mCurrentAbnormalType == 1) {
                                this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                                if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC)) {
                                    HwSelfCureEngine.this.LOGD("HTTP unreachable, and no next dhcp results, use reassoc to cure for no rx pkt.");
                                    HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC, 0);
                                    return false;
                                }
                            } else if (this.mCurrentAbnormalType == 2) {
                                currentCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
                            }
                        } else if (currentCureLevel == 202 && HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2) {
                            if (getNextTestDhcpResults() != null && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP)) {
                                this.mLastSelfCureLevel = currentCureLevel;
                                this.mConfigStaticIp4MultiDhcpServer = true;
                                HwSelfCureEngine.this.LOGD("HTTP unreachable, and has next dhcp results, try next one for re-dhcp failed.");
                                HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP, 0);
                            }
                            return false;
                        }
                        if (hasBeenTested(HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) || !HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                            handleHttpUnreachableFinally();
                        } else {
                            this.mLastSelfCureLevel = currentCureLevel;
                            HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
                        }
                    } else if (getNextTestDhcpResults() == null || hasBeenTested(HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP)) {
                        handleHttpUnreachableFinally();
                    } else {
                        this.mFinalSelfCureUsed = true;
                        this.mLastSelfCureLevel = currentCureLevel;
                        this.mConfigStaticIp4MultiDhcpServer = true;
                        HwSelfCureEngine.this.LOGD("HTTP unreachable, and has next dhcp results, try next one for wifi reset failed.");
                        HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP, 0);
                    }
                } else {
                    handleHttpReachableAfterSelfCure(currentCureLevel);
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                    return true;
                }
            }
            return false;
        }

        private boolean hasBeenTested(int cureLevel) {
            for (int i = 0; i < this.mTestedSelfCureLevel.size(); i++) {
                if (this.mTestedSelfCureLevel.get(i).intValue() == cureLevel) {
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
            HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
            hwSelfCureEngine.LOGD("handleHttpReachableAfterSelfCure, cureLevel = " + currentCureLevel + ", HTTP reachable, --> ConnectedMonitorState.");
            HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
            HwSelfCureUtils.updateSelfCureHistoryInfo(this.mSelfCureHistoryInfo, currentCureLevel, true);
            DhcpResults dhcpResults = HwSelfCureEngine.this.mWifiStateMachine.syncGetDhcpResults();
            String strDhcpResults = WifiProCommonUtils.dhcpResults2String(dhcpResults, -1);
            WifiConfiguration currentConfig = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (!(currentConfig == null || strDhcpResults == null)) {
                currentConfig.lastDhcpResults = strDhcpResults;
            }
            updateWifiConfig(currentConfig);
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            if (this.mSetStaticIp4InvalidIp) {
                HwSelfCureEngine.this.requestArpConflictTest(dhcpResults);
                boolean unused = HwSelfCureEngine.this.mStaticIpCureSuccess = true;
            } else if (currentCureLevel == 203) {
                this.mCurrentAbnormalType = 3;
                HwSelfCureEngine.this.requestArpConflictTest(dhcpResults);
                boolean unused2 = HwSelfCureEngine.this.mStaticIpCureSuccess = true;
            }
            uploadInternetCureSuccCounter(currentCureLevel);
            HwSelfCureEngine.this.sendMessageDelayed(125, strDhcpResults, 500);
        }

        private void handleRssiChanged() {
            if (WifiProCommonUtils.getCurrenSignalLevel(HwSelfCureEngine.this.mWifiManager.getConnectionInfo()) > 2 && !HwSelfCureEngine.this.mSelfCureOngoing.get() && !HwSelfCureEngine.this.mP2pConnected.get()) {
                if (this.mDelayedReassocSelfCure || this.mDelayedResetSelfCure) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (!HwSelfCureEngine.this.isHttpReachable(true)) {
                        HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                        hwSelfCureEngine.LOGD("handleRssiChanged, Http failed, delayedReassoc = " + this.mDelayedReassocSelfCure + ", delayedReset = " + this.mDelayedResetSelfCure);
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        if (this.mDelayedReassocSelfCure) {
                            HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC, 0);
                        } else if (this.mDelayedResetSelfCure) {
                            HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
                        }
                    } else {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                        this.mDelayedReassocSelfCure = false;
                        this.mDelayedResetSelfCure = false;
                        HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                    }
                }
            }
        }

        private void handleRoamingDetected(String newBssid) {
            if (newBssid == null || newBssid.equals(this.mCurrentBssid)) {
                HwSelfCureEngine.this.LOGD("handleRoamingDetected, but bssid is unchanged, ignore it.");
                return;
            }
            this.mCurrentBssid = newBssid;
            if (!HwSelfCureEngine.this.hasMessages(114) && ((!hasBeenTested(HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) || (hasBeenTested(HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) && this.mRenewDhcpCount == 1)) && !HwSelfCureEngine.this.mSelfCureOngoing.get() && !this.mDelayedReassocSelfCure && !this.mDelayedResetSelfCure)) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (!HwSelfCureEngine.this.isHttpReachable(false)) {
                    HwSelfCureEngine.this.LOGD("handleRoamingDetected, and HTTP access failed, trigger Re-Dhcp for it first time.");
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP, 0);
                } else {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                }
            }
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
                config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            } else {
                config = wifiConfig;
            }
            if (config != null) {
                config.internetSelfCureHistory = HwSelfCureUtils.internetSelfCureHistoryInfo2String(this.mSelfCureHistoryInfo);
                HwSelfCureEngine.this.mWifiStateMachine.sendMessage(HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS, config);
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

        private void selfCureWifiLink(int requestCureLevel) {
            HwSelfCureEngine.this.LOGD("selfCureWifiLink, cureLevel = " + requestCureLevel + ", signal rssi = " + this.mCurrentRssi);
            if (requestCureLevel == 201) {
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_LOW_1_DNS");
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                HwSelfCureEngine.this.mWifiStateMachine.requestUpdateDnsServers(HwSelfCureUtils.getPublicDnsServers());
                HwSelfCureEngine.this.sendMessageDelayed(113, 1000);
            } else if (requestCureLevel == 202) {
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_LOW_2_RENEW_DHCP");
                HwSelfCureEngine.this.mDhcpOfferPackets.clear();
                HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                this.mRenewDhcpCount++;
                HwSelfCureEngine.this.mWifiStateMachine.requestRenewDhcp();
                HwSelfCureEngine.this.sendMessageDelayed(116, 6000);
            } else if (requestCureLevel == 208) {
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_DEAUTH_BSSID");
                WifiInjector.getInstance().getWifiNative().deauthLastRoamingBssidHw("2", "");
                HwSelfCureEngine.this.sendMessageDelayed(113, 15000);
            } else if (requestCureLevel == 203) {
                String dhcpResults = null;
                if (this.mConfigStaticIp4MultiDhcpServer) {
                    dhcpResults = getNextTestDhcpResults();
                } else if (this.mConfigStaticIp4GatewayChanged) {
                    dhcpResults = HwSelfCureEngine.this.getDhcpResultsHasInternet(HwSelfCureEngine.this.mWifiStateMachine.getCurrentBSSID(), HwSelfCureEngine.this.mSelfCureConfig);
                }
                String gatewayKey = WifiProCommonUtils.dhcpResults2Gateway(dhcpResults);
                if (!(dhcpResults == null || gatewayKey == null)) {
                    String gatewayKey2 = gatewayKey.replace("/", "");
                    HwSelfCureEngine.this.LOGD("begin to self cure for internet access: TRY_NEXT_DHCP_OFFER, gatewayKey = " + gatewayKey2);
                    HwSelfCureEngine.this.mDhcpResultsTestDone.add(gatewayKey2);
                    StaticIpConfiguration staticIpConfig = WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResults);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                    HwSelfCureEngine.this.mWifiStateMachine.requestUseStaticIpConfig(staticIpConfig);
                    HwSelfCureEngine.this.sendMessageDelayed(116, 3000);
                }
            } else if (requestCureLevel == 207) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mUnconflictedIp = HwSelfCureEngine.this.getLegalIpConfiguration();
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_RECONNECT_4_INVALID_IP");
                HwSelfCureEngine.this.mWifiStateMachine.handleInvalidIpAddr();
            } else if (requestCureLevel == 204) {
                if (this.mCurrentRssi < -75 || HwSelfCureEngine.this.mP2pConnected.get()) {
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    this.mDelayedReassocSelfCure = true;
                } else {
                    HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_MIDDLE_REASSOC");
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                    this.mDelayedReassocSelfCure = false;
                    HwSelfCureEngine.this.mWifiStateMachine.requestReassocLink();
                }
            } else if (requestCureLevel == 205) {
                if (HwSelfCureEngine.this.mInternetUnknown || WifiProCommonUtils.isQueryActivityMatched(HwSelfCureEngine.this.mContext, "com.android.settings.Settings$WifiSettingsActivity")) {
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    HwSelfCureEngine.this.mRouterInternetDetector.notifyNoInternetAfterCure(this.mCurrentGateway, this.mConfigAuthType, HwSelfCureEngine.this.mMobileHotspot);
                    return;
                }
                WifiInfo connectionInfo = HwSelfCureEngine.this.mWifiManager.getConnectionInfo();
                if (this.mCurrentRssi < -70 || HwSelfCureEngine.this.mP2pConnected.get()) {
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    this.mDelayedResetSelfCure = true;
                } else {
                    HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_HIGH_RESET");
                    HwSelfCureEngine.this.mDhcpOfferPackets.clear();
                    HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    this.mDelayedResetSelfCure = false;
                    this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                    HwSelfCureEngine.this.mWifiStateMachine.requestResetWifi();
                }
            }
        }
    }

    static /* synthetic */ int access$1012(HwSelfCureEngine x0, int x1) {
        int i = x0.mNoTcpRxCounter + x1;
        x0.mNoTcpRxCounter = i;
        return i;
    }

    static /* synthetic */ int access$3604(HwSelfCureEngine x0) {
        int i = x0.mNoAutoConnCounter + 1;
        x0.mNoAutoConnCounter = i;
        return i;
    }

    public static synchronized HwSelfCureEngine getInstance(Context context, WifiStateMachine wsm) {
        HwSelfCureEngine hwSelfCureEngine;
        synchronized (HwSelfCureEngine.class) {
            if (mHwSelfCureEngine == null) {
                mHwSelfCureEngine = new HwSelfCureEngine(context, wsm);
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

    private HwSelfCureEngine(Context context, WifiStateMachine wsm) {
        super(TAG);
        boolean z = false;
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        HwNetworkPropertyChecker hwNetworkPropertyChecker = new HwNetworkPropertyChecker(context, this.mWifiManager, null, true, null, true);
        this.mNetworkChecker = hwNetworkPropertyChecker;
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        this.mArpClient = new HwArpClient(this.mContext);
        this.mHwArpUtils = new HwArpUtils(this.mContext);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mRouterInternetDetector = HwRouterInternetDetector.getInstance(context, this);
        this.mIsCaptivePortalCheckEnabled = Settings.Global.getInt(this.mContext.getContentResolver(), "captive_portal_mode", 1) != 0 ? true : z;
        addState(this.mDefaultState);
        addState(this.mConnectedMonitorState, this.mDefaultState);
        addState(this.mDisconnectedMonitorState, this.mDefaultState);
        addState(this.mConnectionSelfCureState, this.mDefaultState);
        addState(this.mInternetSelfCureState, this.mDefaultState);
        setInitialState(this.mDisconnectedMonitorState);
        start();
    }

    public synchronized void setup() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            LOGD("setup DONE!");
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
        intentFilter.addAction("com.hw.wifipro.action.INVALID_DHCP_OFFER_RCVD");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
                    if (!intent.getBooleanExtra("resultsUpdated", false)) {
                        return;
                    }
                    if (HwSelfCureEngine.this.getCurrentState() == HwSelfCureEngine.this.mConnectedMonitorState) {
                        HwSelfCureEngine.this.sendMessage(127);
                    } else {
                        HwSelfCureEngine.this.sendMessage(101);
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                        HwSelfCureEngine.this.sendMessage(108);
                    } else if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        HwSelfCureEngine.this.sendMessage(104);
                    }
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                    if (HwSelfCureEngine.this.mWifiManager.isWifiEnabled()) {
                        HwSelfCureEngine.this.sendMessageDelayed(106, 1000);
                    } else {
                        HwSelfCureEngine.this.sendMessage(105);
                    }
                } else if ("android.net.wifi.RSSI_CHANGED".equals(intent.getAction())) {
                    int newRssi = intent.getIntExtra("newRssi", -127);
                    if (newRssi != -127) {
                        HwSelfCureEngine.this.sendMessage(109, newRssi, 0);
                    }
                } else if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(intent.getAction())) {
                    WifiConfiguration config = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                    if (intent.getIntExtra("changeReason", 0) == 1) {
                        HwSelfCureEngine.this.sendMessage(107, config);
                    }
                } else if ("com.hw.wifipro.action.DHCP_OFFER_INFO".equals(intent.getAction())) {
                    String dhcpResults = intent.getStringExtra("com.hw.wifipro.FLAG_DHCP_OFFER_INFO");
                    if (dhcpResults != null) {
                        HwSelfCureEngine.this.sendMessage(126, dhcpResults);
                    }
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (p2pNetworkInfo != null && p2pNetworkInfo.isConnectedOrConnecting()) {
                        HwSelfCureEngine.this.mP2pConnected.set(true);
                    } else if (p2pNetworkInfo != null && p2pNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        HwSelfCureEngine.this.mP2pConnected.set(false);
                        if (HwSelfCureEngine.this.getCurrentState() == HwSelfCureEngine.this.mInternetSelfCureState) {
                            HwSelfCureEngine.this.sendMessage(128);
                        }
                    }
                } else if ("com.hw.wifipro.action.INVALID_DHCP_OFFER_RCVD".equals(intent.getAction())) {
                    String dhcpResults2 = intent.getStringExtra("com.hw.wifipro.FLAG_DHCP_OFFER_INFO");
                    if (dhcpResults2 != null) {
                        HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                        hwSelfCureEngine.LOGD("ACTION_INVALID_DHCP_OFFER_RCVD, dhcpResults = " + dhcpResults2);
                        HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT, dhcpResults2, 2000);
                    }
                } else if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                    if (HwSelfCureEngine.this.getCurrentState() == HwSelfCureEngine.this.mDisconnectedMonitorState) {
                        HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_USER_PRESENT_RCVD);
                    }
                } else if (!"android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                } else {
                    if (HwSelfCureEngine.this.getCurrentState() == HwSelfCureEngine.this.mConnectedMonitorState || HwSelfCureEngine.this.getCurrentState() == HwSelfCureEngine.this.mInternetSelfCureState) {
                        if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED)) {
                            HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED);
                        }
                        HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_PERIODIC_ARP_DETECTED, 10000);
                    }
                }
            }
        }, intentFilter);
    }

    /* access modifiers changed from: private */
    public String getDhcpResultsHasInternet(String currentBssid, WifiConfiguration config) {
        String dhcpResults = null;
        if (currentBssid != null) {
            PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
            if (database != null) {
                dhcpResults = database.syncQueryDhcpResultsByBssid(currentBssid);
            }
        }
        if (dhcpResults != null || config == null) {
            return dhcpResults;
        }
        return config.lastDhcpResults;
    }

    /* access modifiers changed from: private */
    public boolean handleInvalidDhcpOffer(String dhcpResults) {
        if (dhcpResults == null) {
            return false;
        }
        this.mWifiStateMachine.requestUseStaticIpConfig(WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResults));
        return true;
    }

    /* access modifiers changed from: private */
    public String getLegalIpConfiguration() {
        LinkAddress newIpAddress;
        DhcpResults dhcpResults = this.mWifiStateMachine.syncGetDhcpResults();
        LOGD("getLegalIpConfiguration, dhcpResults " + dhcpResults);
        if (dhcpResults == null || dhcpResults.gateway == null || dhcpResults.ipAddress == null) {
            return null;
        }
        InetAddress gateway = dhcpResults.gateway;
        InetAddress initialIpAddr = dhcpResults.ipAddress.getAddress();
        ArrayList<InetAddress> conflictedIpAddr = new ArrayList<>();
        int testCnt = 0;
        InetAddress testIpAddr = initialIpAddr;
        while (true) {
            InetAddress testIpAddr2 = testIpAddr;
            int testCnt2 = testCnt + 1;
            if (testCnt < 3 && testIpAddr2 != null) {
                conflictedIpAddr.add(testIpAddr2);
                InetAddress testIpAddr3 = HwSelfCureUtils.getNextIpAddr(gateway, initialIpAddr, conflictedIpAddr);
                if (testIpAddr3 == null || this.mArpClient.doSlowArpTest((Inet4Address) testIpAddr3)) {
                    testIpAddr = testIpAddr3;
                    testCnt = testCnt2;
                } else {
                    LOGD("getLegalIpConfiguration, find a new unconflicted one.");
                    dhcpResults.ipAddress = new LinkAddress(testIpAddr3, dhcpResults.ipAddress.getPrefixLength(), dhcpResults.ipAddress.getFlags(), dhcpResults.ipAddress.getScope());
                    return WifiProCommonUtils.dhcpResults2String(dhcpResults, -1);
                }
            }
        }
        try {
            byte[] oldIpAddr = dhcpResults.ipAddress.getAddress().getAddress();
            oldIpAddr[3] = -100;
            LOGD("handleInvalidDhcpOffer newIpAddress = " + newIpAddress);
            dhcpResults.ipAddress = newIpAddress;
            return WifiProCommonUtils.dhcpResults2String(dhcpResults, -1);
        } catch (UnknownHostException e) {
            LOGD("handleInvalidDhcpOffer, UnknownHostException rcvd!");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void notifyHttpReachableForWifiPro(boolean httpReachable) {
        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        if (wifiProStateMachine != null) {
            wifiProStateMachine.notifyHttpReachable(httpReachable);
        }
        HwWifiproLiteStateMachine liteStateMachine = HwWifiproLiteStateMachine.getInstance();
        if (liteStateMachine != null) {
            liteStateMachine.notifyHttpReachable(httpReachable);
        }
    }

    private void notifyRoamingCompletedForWifiPro(String newBssid) {
        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        if (wifiProStateMachine != null) {
            wifiProStateMachine.notifyRoamingCompleted(newBssid);
        }
    }

    /* access modifiers changed from: private */
    public boolean isConnectingOrConnected() {
        WifiInfo info = this.mWifiStateMachine.getWifiInfo();
        if (info == null || info.getSupplicantState().ordinal() < SupplicantState.AUTHENTICATING.ordinal()) {
            return false;
        }
        LOGD("Supplicant is connectingOrConnected, no need to self cure for auto connection.");
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        this.mNoAutoConnCounter = 0;
        return true;
    }

    /* access modifiers changed from: private */
    public boolean isSuppOnCompletedState() {
        WifiInfo info = this.mWifiStateMachine.getWifiInfo();
        if (info == null || info.getSupplicantState().ordinal() != SupplicantState.COMPLETED.ordinal()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void handleNetworkConnected() {
        LOGD("ENTER: handleNetworkConnected()");
        if (!updateConnSelfCureFailedHistory()) {
            LOGD("handleNetworkConnected, config is null for update, delay 2s to update again.");
            sendMessageDelayed(119, 2000);
        }
        this.mNoAutoConnCounter = 0;
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        enableAllNetworksByReason(new int[]{1, 10, 11});
        this.mConnectedTimeMills = System.currentTimeMillis();
        synchronized (this.mDhcpFailedBssidLock) {
            this.mDhcpFailedBssids.clear();
            this.mDhcpFailedConfigKeys.clear();
        }
        transitionTo(this.mConnectedMonitorState);
    }

    /* access modifiers changed from: private */
    public boolean updateConnSelfCureFailedHistory() {
        WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        if (config == null || config.configKey() == null) {
            return false;
        }
        this.networkCureFailedHistory.remove(config.configKey());
        LOGD("updateConnSelfCureFailedHistory(), networkCureFailedHistory remove " + config.configKey());
        if (this.mNoAutoConnConfig != null && config.configKey().equals(this.mNoAutoConnConfig.configKey())) {
            if (!(this.mNoAutoConnReason == -1 || this.mHwWifiCHRService == null)) {
                this.mHwWifiCHRService.updateScCHRCount(this.mNoAutoConnReason);
            }
            int chrType = -1;
            if (this.mNoAutoConnReason == 10) {
                chrType = 14;
            } else if (this.mNoAutoConnReason == 11) {
                chrType = 15;
            } else if (this.mNoAutoConnReason == 12) {
                chrType = 16;
            } else if (this.mNoAutoConnReason == 13) {
                chrType = 17;
            } else if (this.mNoAutoConnReason == 24) {
                chrType = 23;
            }
            if (!(chrType == -1 || this.mHwWifiCHRService == null)) {
                this.mHwWifiCHRService.updateScCHRCount(chrType);
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void enableAllNetworksByReason(int[] enabledReasons) {
        List<WifiConfiguration> savedNetworks = this.mWifiManager.getConfiguredNetworks();
        if (savedNetworks == null || savedNetworks.size() == 0) {
            LOGD("enableAllNetworksByReason, no saved networks found.");
            return;
        }
        for (int i = 0; i < savedNetworks.size(); i++) {
            WifiConfiguration nextConfig = savedNetworks.get(i);
            WifiConfiguration.NetworkSelectionStatus status = nextConfig.getNetworkSelectionStatus();
            int disableReason = status.getNetworkSelectionDisableReason();
            if (WifiCommonUtils.doesNotWifiConnectRejectByCust(status, nextConfig.SSID, this.mContext)) {
                Log.d(TAG, "enableAllNetworksByReason can not enable wifi");
            } else if (!status.isNetworkEnabled()) {
                int j = 0;
                while (true) {
                    if (j >= enabledReasons.length) {
                        break;
                    } else if (disableReason == enabledReasons[j]) {
                        LOGD("To enable network which status is " + disableReason + ", config = " + nextConfig.configKey() + ", id = " + nextConfig.networkId);
                        this.mWifiManager.enableNetwork(nextConfig.networkId, false);
                        break;
                    } else {
                        j++;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void enableAllNetworksByEnterSettings(int[] enabledReasons) {
        List<WifiConfiguration> savedNetworks = this.mWifiManager.getConfiguredNetworks();
        if (savedNetworks == null || savedNetworks.size() == 0) {
            LOGD("enableAllNetworksByEnterSettings, no saved networks found.");
            return;
        }
        int NETWORKS_SIZE = savedNetworks.size();
        for (int i = 0; i < NETWORKS_SIZE; i++) {
            WifiConfiguration nextConfig = savedNetworks.get(i);
            if (nextConfig != null) {
                WifiConfiguration.NetworkSelectionStatus status = nextConfig.getNetworkSelectionStatus();
                int disableReason = status.getNetworkSelectionDisableReason();
                if (WifiCommonUtils.doesNotWifiConnectRejectByCust(status, nextConfig.SSID, this.mContext)) {
                    Log.d(TAG, "enableAllNetworksByEnterSettings can not enable wifi");
                } else if (!status.isNetworkEnabled() && HwAutoConnectManager.KEY_HUAWEI_EMPLOYEE.equals(nextConfig.configKey())) {
                    LOGD("##enableAllNetworksByEnterSettings, HUAWEI_EMPLOYEE networkId = " + nextConfig.networkId);
                    this.mWifiManager.enableNetwork(nextConfig.networkId, false);
                } else if (!status.isNetworkEnabled() && !nextConfig.noInternetAccess && !nextConfig.portalNetwork) {
                    int j = 0;
                    while (true) {
                        if (j >= enabledReasons.length) {
                            break;
                        } else if (disableReason == enabledReasons[j]) {
                            LOGD("enableAllNetworksByEnterSettings, status is " + disableReason + ", config = " + nextConfig.configKey() + ", id = " + nextConfig.networkId);
                            this.mWifiManager.enableNetwork(nextConfig.networkId, false);
                            break;
                        } else {
                            j++;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isHttpReachable(boolean useDoubleServers) {
        synchronized (this.mNetworkChecker) {
            if (!WifiProCommonUtils.unreachableRespCodeByAndroid(this.mNetworkChecker.isCaptivePortal(true))) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean isIpAddressInvalid() {
        DhcpInfo dhcpInfo = this.mWifiManager.getDhcpInfo();
        if (!(dhcpInfo == null || dhcpInfo.ipAddress == 0)) {
            byte[] currAddr = NetworkUtils.intToInetAddress(dhcpInfo.ipAddress).getAddress();
            if (currAddr != null && currAddr.length == 4) {
                int intCurrAddr3 = currAddr[3] & 255;
                int netmaskLenth = NetworkUtils.netmaskIntToPrefixLength(dhcpInfo.netmask);
                LOGD("isIpAddressLegal, currAddr[3] is " + intCurrAddr3 + " netmask lenth is: " + netmaskLenth);
                boolean ipEqualsGw = dhcpInfo.ipAddress == dhcpInfo.gateway;
                boolean invalidIp = intCurrAddr3 == 0 || intCurrAddr3 == 1 || intCurrAddr3 == 255;
                if (ipEqualsGw || (netmaskLenth == 24 && invalidIp)) {
                    LOGD("the current rcvd ip address is invalid, maybe no internet access, need to comfirm and cure it.");
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void handleWifiDisabled(boolean selfCureGoing) {
        LOGD("ENTER: handleWifiDisabled(), selfCureGoing = " + selfCureGoing);
        this.mNoAutoConnCounter = 0;
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        this.networkCureFailedHistory.clear();
        if (selfCureGoing) {
            transitionTo(this.mDisconnectedMonitorState);
        }
    }

    /* access modifiers changed from: private */
    public void handleWifiEnabled() {
        LOGD("ENTER: handleWifiEnabled()");
        enableAllNetworksByReason(new int[]{1, 10, 11});
    }

    /* access modifiers changed from: private */
    public void handleNetworkRemoved(WifiConfiguration config) {
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
    public boolean multiGateway() {
        boolean z = true;
        this.mHWMultiGW = this.mHwArpUtils.getGateWayArpResponses(1, 1000);
        int gwNum = this.mHWMultiGW.getGWNum();
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateMultiGWCount((byte) gwNum);
        }
        LOGD("multiGateway gateway num is " + gwNum);
        String gateway = this.mHWMultiGW.getGWIPAddr();
        if (gateway == null) {
            return false;
        }
        ArrayList<String> unfoundlist = new ArrayList<>();
        ArrayList<String> maclist = this.mHWMultiGW.getGWMACAddrList();
        for (HwArpUtils.ArpItem arpitem : this.mHwArpUtils.readArpFromFile()) {
            boolean found = true;
            int i = 0;
            int list_size = maclist.size();
            while (true) {
                if (i >= list_size) {
                    break;
                }
                if (arpitem.isValid() && arpitem.sameIpaddress(gateway)) {
                    if (!arpitem.sameMacAddress(maclist.get(i))) {
                        found = false;
                    } else if (arpitem.isStaticArp()) {
                        return false;
                    } else {
                        found = true;
                    }
                }
                i++;
            }
            if (!found) {
                unfoundlist.add(arpitem.hwaddr);
            }
        }
        maclist.addAll(unfoundlist);
        if (maclist.size() <= 1) {
            z = false;
        }
        return z;
    }

    public synchronized void notifyWifiScanResultsAvailable(boolean success) {
        if (this.mInitialized && success) {
            sendMessage(101);
        }
    }

    public synchronized void notifyDhcpResultsInternetOk(String dhcpResults) {
        if (this.mInitialized && dhcpResults != null) {
            sendMessage(125, dhcpResults);
        }
    }

    public synchronized void notifyWifiConnectedBackground() {
        LOGD("ENTER: notifyWifiConnectedBackground()");
        if (this.mInitialized) {
            this.mIsWifiBackground.set(true);
            this.mIpConfigLostCnt = 0;
            sendMessage(104);
        }
    }

    public synchronized void notifyWifiRoamingCompleted(String bssid) {
        LOGD("ENTER: notifyWifiRoamingCompleted()");
        if (this.mInitialized && bssid != null) {
            sendMessageDelayed(110, bssid, 500);
            notifyRoamingCompletedForWifiPro(bssid);
        }
    }

    public synchronized void notifySefCureCompleted(int status) {
        LOGD("ENTER: notifySefCureCompleted, status = " + status);
        if (!this.mInitialized || status != 0) {
            if (-1 != status) {
                if (-2 != status) {
                    this.mSelfCureOngoing.set(false);
                    notifyVoWiFiSelCureEnd(-1);
                }
            }
            sendMessage(120);
            notifyVoWiFiSelCureEnd(-1);
        } else {
            sendMessage(113);
        }
    }

    public synchronized void notifyTcpStatResults(int deltaTx, int deltaRx, int deltaReTx, int deltaDnsFailed) {
        if (this.mInitialized) {
            if (deltaDnsFailed < 3 || deltaRx != 0) {
                if (deltaTx < 3 || deltaRx != 0) {
                    if (deltaRx > 0) {
                        this.mNoTcpRxCounter = 0;
                        removeMessages(CMD_DNS_FAILED_DETECTED);
                        removeMessages(121);
                    }
                } else if (!hasMessages(121)) {
                    sendMessage(121);
                }
            } else if (!hasMessages(CMD_DNS_FAILED_DETECTED)) {
                sendMessage(CMD_DNS_FAILED_DETECTED);
            }
        }
    }

    public synchronized void notifyWifiDisconnected() {
        LOGD("ENTER: notifyWifiDisconnected()");
        if (this.mInitialized) {
            sendMessage(108);
        }
    }

    public synchronized void notifyIpConfigCompleted() {
        if (this.mInitialized) {
            LOGD("ENTER: notifyIpConfigCompleted()");
            this.mIpConfigLostCnt = 0;
            sendMessage(117);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007a, code lost:
        return false;
     */
    public synchronized boolean notifyIpConfigLostAndHandle(WifiConfiguration config) {
        if (this.mInitialized && config != null) {
            if (config.isEnterprise()) {
                LOGD("notifyIpConfigLostAndHandle, no self cure for enterprise network");
                return false;
            }
            int signalLevel = WifiProCommonUtils.getCurrenSignalLevel(this.mWifiManager.getConnectionInfo());
            this.mIpConfigLostCnt++;
            LOGD("ENTER: notifyIpConfigLostAndHandle() IpConfigLostCnt = " + this.mIpConfigLostCnt + ", ssid = " + config.SSID + ", signalLevel = " + signalLevel);
            if (signalLevel >= 3 && getCurrentState() == this.mDisconnectedMonitorState) {
                if (this.mIpConfigLostCnt == 2 && hasDhcpResultsSaved(config)) {
                    sendMessage(124, config);
                    return true;
                } else if (this.mIpConfigLostCnt >= 1 && !hasDhcpResultsSaved(config)) {
                    sendMessage(CMD_BSSID_DHCP_FAILED_EVENT, config);
                }
            }
        }
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
            LOGD("ENTER: notifyInternetFailureDetected, forceNoHttpCheck = " + forceNoHttpCheck);
            sendMessage(122, Boolean.valueOf(forceNoHttpCheck));
        }
    }

    public synchronized void notifyInternetAccessRecovery() {
        if (this.mInitialized) {
            this.mRouterInternetDetector.notifyInternetAccessRecovery();
            LOGD("ENTER: notifyInternetAccessRecovery");
            sendMessage(CMD_HTTP_REACHABLE_RCV);
        }
    }

    public synchronized void notifyUserEnterWlanSettings() {
        if (this.mInitialized) {
            sendMessage(CMD_USER_ENTER_WLAN_SETTINGS);
        }
    }

    public synchronized void notifySettingsDisplayNoInternet() {
        sendMessage(CMD_SETTINGS_DISPLAY_NO_INTERNET_EVENT);
    }

    public synchronized void notifyRouterGatewayUnreachable() {
        sendMessage(CMD_ROUTER_GATEWAY_UNREACHABLE_EVENT);
    }

    public void requestArpConflictTest(DhcpResults dhcpResults) {
        if (dhcpResults != null && dhcpResults.ipAddress != null && this.mHwWifiCHRService != null) {
            InetAddress addr = dhcpResults.ipAddress.getAddress();
            if (this.mArpClient != null && addr != null && (addr instanceof Inet4Address) && this.mArpClient.doSlowArpTest((Inet4Address) addr)) {
                LOGD("requestArpConflictTest, Upload static ip conflicted chr!");
                this.mHwWifiCHRService.updateScCHRCount(26);
            }
        }
    }

    /* access modifiers changed from: private */
    public void startScan() {
        ScanRequestProxy scanRequest = WifiInjector.getInstance().getScanRequestProxy();
        if (scanRequest == null || this.mContext == null) {
            LOGD("can't start wifi scan!");
        } else {
            scanRequest.startScan(Binder.getCallingUid(), this.mContext.getOpPackageName());
        }
    }

    /* access modifiers changed from: private */
    public List<ScanResult> getScanResults() {
        List<ScanResult> results = new ArrayList<>();
        ScanRequestProxy scanRequest = WifiInjector.getInstance().getScanRequestProxy();
        if (scanRequest != null) {
            results.addAll(scanRequest.getScanResults());
        } else {
            LOGD("can't start wifi scan!");
        }
        return results;
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    /* access modifiers changed from: private */
    public void periodicArpDetection() {
        int signalLevel;
        LOGD("periodicArpDetection signalLevel = " + signalLevel + ", isScreenOn = " + this.mPowerManager.isScreenOn() + " , mArpDetectionFailedCnt = " + this.mArpDetectionFailedCnt);
        if (hasMessages(CMD_PERIODIC_ARP_DETECTED)) {
            removeMessages(CMD_PERIODIC_ARP_DETECTED);
        }
        if (signalLevel >= 2 && this.mPowerManager.isScreenOn() && isSuppOnCompletedState() && !this.mSelfCureOngoing.get()) {
            Pair<Boolean, Long> pair = this.mHwArpUtils.getGateWayArpResult(1, 1000);
            if (this.mHwWifiCHRService != null) {
                this.mHwWifiCHRService.updateArpSummery(((Boolean) pair.first).booleanValue(), ((Long) pair.second).intValue(), -1);
            }
            if (!((Boolean) pair.first).booleanValue()) {
                this.mArpDetectionFailedCnt++;
                this.mHwArpUtils.doGratuitousArp(100);
                this.mHwArpUtils.pingGateway();
                if (this.mArpDetectionFailedCnt == 5) {
                    if (this.mHwWifiCHRService != null) {
                        this.mHwWifiCHRService.updateWifiException(87, "ARP_DETECTED_FAILED");
                    }
                    sendMessage(CMD_ARP_FAILED_DETECTED);
                }
                if (this.mArpDetectionFailedCnt > 0 && this.mArpDetectionFailedCnt < 5) {
                    sendMessageDelayed(CMD_PERIODIC_ARP_DETECTED, 10000);
                    return;
                }
            } else {
                this.mArpDetectionFailedCnt = 0;
            }
        }
        sendMessageDelayed(CMD_PERIODIC_ARP_DETECTED, HidataWechatTraffic.MIN_VALID_TIME);
    }

    /* access modifiers changed from: private */
    public void flushVmDnsCache() {
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
    public void notifyVoWiFiSelCureBegin() {
        if (this.mSelfCureOngoing.get()) {
            HwQoEService mHwQoEService = HwQoEService.getInstance();
            if (mHwQoEService != null) {
                mHwQoEService.notifySelEngineStateStart();
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyVoWiFiSelCureEnd(int status) {
        boolean success = false;
        if (status == 0) {
            success = true;
        }
        HwQoEService mHwQoEService = HwQoEService.getInstance();
        if (mHwQoEService != null) {
            mHwQoEService.notifySelEngineStateEnd(success);
        }
    }
}
