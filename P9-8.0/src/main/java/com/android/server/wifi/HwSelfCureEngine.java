package com.android.server.wifi;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.IpConfiguration.IpAssignment;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.dhcp.HwArpClient;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.wifi.ABS.HwABSDetectorService;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
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
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwSelfCureEngine extends StateMachine {
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
    private static final int CMD_INTERNET_FAILED_SELF_CURE = 112;
    private static final int CMD_INTERNET_FAILURE_DETECTED = 122;
    private static final int CMD_INTERNET_RECOVERY_CONFIRM = 113;
    private static final int CMD_INTERNET_STATUS_DETECT = 111;
    private static final int CMD_INVALID_DHCP_OFFER_EVENT = 130;
    private static final int CMD_INVALID_IP_CONFIRM = 129;
    private static final int CMD_IP_CONFIG_COMPLETED = 117;
    private static final int CMD_IP_CONFIG_LOST_EVENT = 124;
    private static final int CMD_IP_CONFIG_TIMEOUT = 116;
    private static final int CMD_NETWORK_CONNECTED_RCVD = 104;
    private static final int CMD_NETWORK_DISCONNECTED_RCVD = 108;
    private static final int CMD_NETWORK_ROAMING_DETECT = 110;
    private static final int CMD_NEW_RSSI_RCVD = 109;
    private static final int CMD_NEW_SCAN_RESULTS_RCV = 127;
    private static final int CMD_NO_TCP_RX_DETECTED = 121;
    private static final int CMD_P2P_DISCONNECTED_EVENT = 128;
    private static final int CMD_RESETUP_SELF_CURE_MONITOR = 118;
    private static final int CMD_ROUTER_GATEWAY_UNREACHABLE_EVENT = 132;
    private static final int CMD_SELF_CURE_WIFI_FAILED = 120;
    private static final int CMD_SELF_CURE_WIFI_LINK = 114;
    private static final int CMD_SETTINGS_DISPLAY_NO_INTERNET_EVENT = 131;
    private static final int CMD_UPDATE_CONN_SELF_CURE_HISTORY = 119;
    private static final int CMD_USER_PRESENT_RCVD = 134;
    private static final int CMD_WIFI_DISABLED_RCVD = 105;
    private static final int CMD_WIFI_ENABLED_RCVD = 106;
    private static final int CURE_OUT_OF_DATE_MS = 7200000;
    private static final int DHCP_RENEW_TIMEOUT_MS = 6000;
    private static final int DNS_UPDATE_CONFIRM_DELAYED_MS = 1000;
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
    private static final int REQ_HTTP_DELAYED_MS = 2000;
    private static final int SELF_CURE_DELAYED_MS = 1000;
    private static final int SELF_CURE_MONITOR_DELAYED_MS = 2000;
    private static final int SELF_CURE_TIMEOUT_MS = 20000;
    private static final int SET_STATIC_IP_TIMEOUT_MS = 3000;
    private static final String TAG = "HwSelfCureEngine";
    private static HwSelfCureEngine mHwSelfCureEngine = null;
    private Map<String, WifiConfiguration> autoConnectFailedNetworks = new HashMap();
    private Map<String, Integer> autoConnectFailedNetworksRssi = new HashMap();
    private HwArpClient mArpClient;
    private State mConnectedMonitorState = new ConnectedMonitorState();
    private long mConnectedTimeMills;
    private State mConnectionSelfCureState = new ConnectionSelfCureState();
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private final Object mDhcpFailedBssidLock = new Object();
    private ArrayList<String> mDhcpFailedBssids = new ArrayList();
    private ArrayList<String> mDhcpFailedConfigKeys = new ArrayList();
    private Map<String, String> mDhcpOfferPackets = new HashMap();
    private ArrayList<String> mDhcpResultsTestDone = new ArrayList();
    private State mDisconnectedMonitorState = new DisconnectedMonitorState();
    private HwWifiStatStore mHwWifiStatStore;
    private boolean mInitialized = false;
    private State mInternetSelfCureState = new InternetSelfCureState();
    private boolean mInternetUnknown = false;
    private int mIpConfigLostCnt = 0;
    private AtomicBoolean mIsWifiBackground = new AtomicBoolean(false);
    private boolean mMobileHotspot = false;
    private HwNetworkPropertyChecker mNetworkChecker;
    private WifiConfiguration mNoAutoConnConfig;
    private int mNoAutoConnCounter = 0;
    private int mNoAutoConnReason = -1;
    private int mNoTcpRxCounter = 0;
    private AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private HwRouterInternetDetector mRouterInternetDetector = null;
    private WifiConfiguration mSelfCureConfig = null;
    private AtomicBoolean mSelfCureOngoing = new AtomicBoolean(false);
    private boolean mStaticIpCureSuccess = false;
    private WifiManager mWifiManager;
    private WifiStateMachine mWifiStateMachine;
    private Map<String, CureFailedNetworkInfo> networkCureFailedHistory = new HashMap();

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
            HwSelfCureEngine.this.mNoTcpRxCounter = 0;
            this.mLastSignalLevel = 0;
            this.mHasInternetRecently = false;
            this.mPortalUnthenEver = false;
            HwSelfCureEngine.this.mInternetUnknown = false;
            this.mUserSetStaticIpConfig = false;
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            this.mIpv6DnsEnabled = true;
            this.mWifiSwitchAllowed = false;
            this.mMobileHotspot = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(HwSelfCureEngine.this.mContext);
            WifiInfo wifiInfo = HwSelfCureEngine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mLastSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(wifiInfo.getRssi());
                HwSelfCureEngine.this.LOGD("ConnectedMonitorState, network = " + wifiInfo.getSSID() + ", signal = " + this.mLastSignalLevel + ", mobileHotspot = " + this.mMobileHotspot);
            }
            if (!HwSelfCureEngine.this.mIsWifiBackground.get() && (setupSelfCureMonitor() ^ 1) != 0) {
                HwSelfCureEngine.this.LOGD("ConnectedMonitorState, config is null when connected broadcast received, delay to setup again.");
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR, 2000);
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 104:
                    HwSelfCureEngine.this.LOGD("ConnectedMonitorState, CMD_NETWORK_CONNECTED_RCVD!");
                    HwSelfCureEngine.this.mIsWifiBackground.set(false);
                    break;
                case 108:
                    if (HwSelfCureEngine.this.hasMessages(115)) {
                        HwSelfCureEngine.this.removeMessages(115);
                    }
                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR)) {
                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR);
                    }
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mDisconnectedMonitorState);
                    break;
                case 109:
                    this.mLastSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(message.arg1);
                    break;
                case 110:
                    if (!(HwSelfCureEngine.this.mIsWifiBackground.get() || this.mGatewayInvalid)) {
                        String newBssid = message.obj != null ? (String) message.obj : null;
                        if (newBssid == null || !newBssid.equals(this.mLastConnectedBssid)) {
                            if (!this.mUserSetStaticIpConfig) {
                                updateInternetAccessHistory();
                                if (!this.mHasInternetRecently && (this.mPortalUnthenEver ^ 1) != 0 && (HwSelfCureEngine.this.mInternetUnknown ^ 1) != 0) {
                                    HwSelfCureEngine.this.LOGD("CMD_NETWORK_ROAMING_DETECT rcvd, but no internet access always.");
                                    break;
                                }
                                if (HwSelfCureEngine.this.hasMessages(115)) {
                                    HwSelfCureEngine.this.removeMessages(115);
                                }
                                this.mLastConnectedBssid = newBssid;
                                DhcpResults dhcpResults = HwSelfCureEngine.this.mWifiStateMachine.syncGetDhcpResults();
                                if (dhcpResults != null && HwSelfCureEngine.this.mArpClient != null && HwSelfCureEngine.this.mArpClient.doSlowArpTest((Inet4Address) dhcpResults.gateway)) {
                                    HwSelfCureEngine.this.LOGD("last gateway reachable, don't use http get!");
                                    break;
                                }
                                HwSelfCureEngine.this.LOGD("gateway changed or unknow, check http response!");
                                checkHttpResponseAndSelfCure(110);
                                break;
                            }
                            HwSelfCureEngine.this.LOGD("CMD_NETWORK_ROAMING_DETECT rcvd, but user set static ip config, ignore it.");
                            break;
                        }
                        HwSelfCureEngine.this.LOGD("CMD_NETWORK_ROAMING_DETECT rcvd, but bssid is unchanged, ignore it.");
                        break;
                    }
                    break;
                case 115:
                    if (HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2 || (this.mHasInternetRecently && (this.mConfigAuthType == 1 || this.mConfigAuthType == 4))) {
                        checkHttpResponseAndSelfCure(115);
                        break;
                    }
                case HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR /*118*/:
                    HwSelfCureEngine.this.LOGD("CMD_RESETUP_SELF_CURE_MONITOR rcvd");
                    setupSelfCureMonitor();
                    break;
                case 121:
                    if (!(this.mMobileHotspot || (this.mGatewayInvalid ^ 1) == 0 || (HwSelfCureEngine.this.mIsWifiBackground.get() ^ 1) == 0)) {
                        updateInternetAccessHistory();
                        handleNoTcpRxDetected();
                        break;
                    }
                case 122:
                    if (!(this.mMobileHotspot || this.mGatewayInvalid)) {
                        handleInternetFailedDetected(message);
                        break;
                    }
                case HwSelfCureEngine.CMD_DNS_FAILED_MONITOR /*123*/:
                    handleDnsFailedMonitor();
                    break;
                case HwSelfCureEngine.CMD_DHCP_RESULTS_UPDATE /*125*/:
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
                    }
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    break;
                case HwSelfCureEngine.CMD_DNS_FAILED_DETECTED /*133*/:
                    if (!(this.mMobileHotspot || (this.mGatewayInvalid ^ 1) == 0 || (HwSelfCureEngine.this.mIsWifiBackground.get() ^ 1) == 0)) {
                        updateInternetAccessHistory();
                        handleDnsFailedDetected();
                        break;
                    }
                default:
                    return false;
            }
            return true;
        }

        private void handleNewScanResults() {
            List<ScanResult> scanResults = HwSelfCureEngine.this.mWifiManager.getScanResults();
            List<WifiConfiguration> configNetworks = HwSelfCureEngine.this.mWifiManager.getConfiguredNetworks();
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            this.mWifiSwitchAllowed = config != null ? WifiProCommonUtils.isAllowWifiSwitch(scanResults, configNetworks, WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager), WifiProCommonUtils.getCurrentSsid(HwSelfCureEngine.this.mWifiManager), config.configKey(), -75) : false;
        }

        private boolean setupSelfCureMonitor() {
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (config == null) {
                return false;
            }
            HwSelfCureEngine.this.mSelfCureConfig = config;
            this.mConfigAuthType = config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType();
            boolean z = config.getIpAssignment() != null && config.getIpAssignment() == IpAssignment.STATIC;
            this.mUserSetStaticIpConfig = z;
            HwSelfCureEngine.this.mInternetUnknown = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 103);
            updateInternetAccessHistory();
            HwSelfCureEngine.this.LOGD("ConnectedMonitorState, hasInternet = " + this.mHasInternetRecently + ", portalUnthen = " + this.mPortalUnthenEver + ", userSetStaticIp = " + this.mUserSetStaticIpConfig + ", history empty = " + HwSelfCureEngine.this.mInternetUnknown);
            if (!this.mMobileHotspot) {
                DhcpResults dhcpResults = HwSelfCureEngine.this.mWifiStateMachine.syncGetDhcpResults();
                this.mIpv6DnsEnabled = parseIpv6Enabled(dhcpResults);
                z = dhcpResults == null || dhcpResults.gateway == null;
                this.mGatewayInvalid = z;
                HwSelfCureEngine.this.LOGD("ConnectedMonitorState, gatewayInvalid = " + this.mGatewayInvalid);
                if (HwSelfCureEngine.this.mIsWifiBackground.get() || (HwSelfCureEngine.this.mStaticIpCureSuccess ^ 1) == 0 || !((this.mHasInternetRecently || HwSelfCureEngine.this.mInternetUnknown) && HwSelfCureEngine.this.isIpAddressInvalid())) {
                    StaticIpConfiguration staticIpConfig = WifiProCommonUtils.dhcpResults2StaticIpConfig(config.lastDhcpResults);
                    int i = (this.mUserSetStaticIpConfig || dhcpResults == null || staticIpConfig == null || dhcpResults.gateway == null || staticIpConfig.gateway == null || staticIpConfig.ipAddress == null) ? 1 : staticIpConfig.dnsServers == null ? 1 : 0;
                    if (i ^ 1) {
                        int gatewayChanged;
                        String currentGateway = dhcpResults.gateway.getHostAddress();
                        String lastGateway = staticIpConfig.gateway.getHostAddress();
                        HwSelfCureEngine.this.LOGD("ConnectedMonitorState, currentGateway = " + currentGateway + ", lastGateway = " + lastGateway);
                        if (HwSelfCureEngine.this.mStaticIpCureSuccess || currentGateway == null || lastGateway == null) {
                            gatewayChanged = 0;
                        } else {
                            gatewayChanged = currentGateway.equals(lastGateway) ^ 1;
                        }
                        if (gatewayChanged != 0) {
                            HwSelfCureEngine.this.LOGD("ConnectedMonitorState, current gateway is different with history gateway that has internet.");
                            HwSelfCureEngine.this.sendMessageDelayed(115, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                            return true;
                        }
                    } else if (TextUtils.isEmpty(config.lastDhcpResults) && HwSelfCureEngine.this.mInternetUnknown) {
                        HwSelfCureEngine.this.sendMessageDelayed(115, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        return true;
                    }
                }
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INVALID_IP_CONFIRM, 2000);
                return true;
            }
            if (!(this.mMobileHotspot || (HwSelfCureEngine.this.mIsWifiBackground.get() ^ 1) == 0 || (HwSelfCureEngine.this.mStaticIpCureSuccess ^ 1) == 0 || !this.mHasInternetRecently)) {
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_DNS_FAILED_MONITOR, 6000);
            }
            return true;
        }

        private boolean parseIpv6Enabled(DhcpResults dhcpResults) {
            if (dhcpResults == null || dhcpResults.dnsServers == null || dhcpResults.dnsServers.size() == 0) {
                return true;
            }
            for (int i = 0; i < dhcpResults.dnsServers.size(); i++) {
                InetAddress dns = (InetAddress) dhcpResults.dnsServers.get(i);
                if (dns != null && dns.getHostAddress() != null && ((dns instanceof Inet6Address) || dns.getHostAddress().contains(HwQoEUtils.SEPARATOR))) {
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
            if (!this.mGatewayInvalid && (deltaFailedDns >= 2 || deltaDnsResfuseDns >= 2)) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (HwSelfCureEngine.this.isHttpReachable(true)) {
                    HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                } else {
                    HwSelfCureEngine.this.LOGD("handleDnsFailedMonitor, deltaFailedDns = " + deltaFailedDns + ", and HTTP unreachable, transition to SelfCureState.");
                    transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
                }
            }
        }

        private void checkHttpResponseAndSelfCure(int eventType) {
            HwSelfCureEngine.this.mSelfCureOngoing.set(true);
            if (HwSelfCureEngine.this.isHttpReachable(false)) {
                HwSelfCureEngine.this.LOGD("checkHttpResponseAndSelfCure, HTTP reachable for eventType = " + eventType);
                this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                return;
            }
            HwSelfCureEngine.this.LOGD("checkHttpResponseAndSelfCure, HTTP unreachable for eventType = " + eventType + ", dhcp offer size = " + HwSelfCureEngine.this.mDhcpOfferPackets.size());
            int internetFailedReason = 300;
            if (eventType == 110) {
                internetFailedReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING;
            } else if (eventType == 115) {
                internetFailedReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY;
            }
            transitionToSelfCureState(internetFailedReason);
        }

        private void handleDnsFailedDetected() {
            if (this.mLastSignalLevel > 2) {
                HwSelfCureEngine.this.LOGD("handleDnsFailedDetected, start scan and parse the context for wifi 2 wifi.");
                HwSelfCureEngine.this.mWifiStateMachine.startScan(Binder.getCallingUid(), 0, null, null);
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (HwSelfCureEngine.this.isHttpReachable(false)) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    HwSelfCureEngine.this.mNoTcpRxCounter = 0;
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
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.mNoTcpRxCounter = hwSelfCureEngine.mNoTcpRxCounter + 1;
                if (HwSelfCureEngine.this.mNoTcpRxCounter == 1) {
                    this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, start scan and parse the context for wifi 2 wifi.");
                    HwSelfCureEngine.this.mWifiStateMachine.startScan(Binder.getCallingUid(), 0, null, null);
                } else if (HwSelfCureEngine.this.mNoTcpRxCounter == 2 && this.mWifiSwitchAllowed) {
                    if (HwSelfCureEngine.this.isHttpReachable(false)) {
                        this.mWifiSwitchAllowed = false;
                        HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    } else {
                        HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, notify WLAN+ to do wifi swtich first.");
                        HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    }
                } else if (this.mHasInternetRecently || this.mPortalUnthenEver) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (HwSelfCureEngine.this.isHttpReachable(true)) {
                        HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        return;
                    }
                    HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, HTTP unreachable, transition to SelfCureState.");
                    transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP);
                }
            }
        }

        private void handleInternetFailedDetected(Message message) {
            if (HwSelfCureEngine.this.mStaticIpCureSuccess || !((Boolean) message.obj).booleanValue()) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (HwSelfCureEngine.this.isHttpReachable(false)) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
                } else {
                    HwSelfCureEngine.this.LOGD("handleInternetFailedDetected, HTTP unreachable, transition to SelfCureState.");
                    int currentDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    int deltaFailedDns = currentDnsFailedCounter - this.mLastDnsFailedCounter;
                    this.mLastDnsFailedCounter = currentDnsFailedCounter;
                    int currentDnsRefuseCounter = HwSelfCureUtils.getCurrentDnsRefuseCounter();
                    int deltaDnsResfuseDns = currentDnsRefuseCounter - this.mLastDnsRefuseCounter;
                    this.mLastDnsRefuseCounter = currentDnsRefuseCounter;
                    int i = (deltaFailedDns >= 2 || deltaDnsResfuseDns >= 2) ? HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS : HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP;
                    transitionToSelfCureState(i);
                }
                return;
            }
            if (this.mHasInternetRecently || this.mPortalUnthenEver || HwSelfCureEngine.this.mInternetUnknown) {
                HwSelfCureEngine.this.LOGD("handleInternetFailedDetected, wifi has no internet when connected.");
                transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
            }
        }

        private void transitionToSelfCureState(int reason) {
            if (SystemProperties.getBoolean(HwSelfCureEngine.PROP_DISABLE_SELF_CURE, false)) {
                HwSelfCureEngine.this.LOGD("transitionToSelfCureState, don't support SCE, do nothing.");
                return;
            }
            if (!this.mIpv6DnsEnabled) {
                Message dmsg = Message.obtain();
                dmsg.what = 112;
                dmsg.arg1 = reason;
                HwSelfCureEngine.this.sendMessageDelayed(dmsg, 1000);
                HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mInternetSelfCureState);
            }
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

        public boolean processMessage(Message message) {
            WifiConfiguration wifiConfiguration = null;
            switch (message.what) {
                case 103:
                    String str;
                    if (message.obj != null) {
                        str = (String) message.obj;
                    }
                    handleConnSelfCureFailed(str);
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
                case 107:
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    if (message.obj != null) {
                        wifiConfiguration = (WifiConfiguration) message.obj;
                    }
                    hwSelfCureEngine.handleNetworkRemoved(wifiConfiguration);
                    break;
                case 108:
                    HwSelfCureEngine.this.LOGD("CMD_NETWORK_DISCONNECTED_RCVD during connection self cure state.");
                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_UPDATE_CONN_SELF_CURE_HISTORY)) {
                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_UPDATE_CONN_SELF_CURE_HISTORY);
                    }
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mDisconnectedMonitorState);
                    break;
                case HwSelfCureEngine.CMD_UPDATE_CONN_SELF_CURE_HISTORY /*119*/:
                    HwSelfCureEngine.this.updateConnSelfCureFailedHistory();
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void handleConnSelfCureFailed(String configKey) {
            HwSelfCureEngine.this.LOGD("ENTER: handleConnSelfCureFailed(), configKey = " + configKey);
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
                    HwSelfCureEngine.this.LOGD("handleConnSelfCureFailed, networkCureFailedHistory added, configKey = " + configKey);
                }
            }
            if (!(HwSelfCureEngine.this.mNoAutoConnReason == -1 || HwSelfCureEngine.this.mHwWifiStatStore == null)) {
                HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(HwSelfCureEngine.this.mNoAutoConnReason);
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
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("[ ");
            sbuf.append("configKey = ").append(this.configKey);
            sbuf.append(", cureFailedCounter = ").append(this.cureFailedCounter);
            sbuf.append(", lastCureFailedTime = ").append(DateFormat.getDateTimeInstance().format(new Date(this.lastCureFailedTime)));
            sbuf.append(" ]");
            return sbuf.toString();
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 126:
                    handleDhcpOfferPacketRcv((String) message.obj);
                    break;
            }
            return true;
        }

        private void handleDhcpOfferPacketRcv(String dhcpResutls) {
            if (dhcpResutls != null && HwSelfCureEngine.this.isSuppOnCompletedState() && (HwSelfCureEngine.this.mWifiStateMachine.isWifiProEvaluatingAP() ^ 1) != 0) {
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
            HwSelfCureEngine.this.mStaticIpCureSuccess = false;
            HwSelfCureEngine.this.mNoAutoConnCounter = 0;
            HwSelfCureEngine.this.mNoAutoConnReason = -1;
            HwSelfCureEngine.this.mNoAutoConnConfig = null;
            HwSelfCureEngine.this.mSelfCureConfig = null;
            HwSelfCureEngine.this.mIsWifiBackground.set(false);
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            this.mSetStaticIpConfig = false;
            HwSelfCureEngine.this.mConnectedTimeMills = 0;
            HwSelfCureEngine.this.mDhcpOfferPackets.clear();
            HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
            HwSelfCureEngine.this.mRouterInternetDetector.notifyDisconnected();
        }

        public boolean processMessage(Message message) {
            WifiConfiguration wifiConfiguration = null;
            switch (message.what) {
                case 101:
                    if (!(HwSelfCureEngine.this.isConnectingOrConnected() || (HwSelfCureUtils.isOnWlanSettings(HwSelfCureEngine.this.mContext) ^ 1) == 0)) {
                        List<ScanResult> scanResults = HwSelfCureEngine.this.mWifiManager.getScanResults();
                        updateAutoConnFailedNetworks(scanResults);
                        HwSelfCureUtils.selectDisabledNetworks(scanResults, HwSelfCureEngine.this.mWifiManager.getConfiguredNetworks(), HwSelfCureEngine.this.autoConnectFailedNetworks, HwSelfCureEngine.this.autoConnectFailedNetworksRssi, HwSelfCureEngine.this.mWifiStateMachine);
                        selectHighestFailedNetworkAndCure();
                        break;
                    }
                case 102:
                    if (!HwSelfCureUtils.isOnWlanSettings(HwSelfCureEngine.this.mContext)) {
                        if (message.obj != null) {
                            wifiConfiguration = (WifiConfiguration) message.obj;
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
                        if (HwSelfCureEngine.this.mHwWifiStatStore != null) {
                            HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(12);
                            HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(25);
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
                default:
                    return false;
            }
            return true;
        }

        private void handleUserPresentEvent() {
            if (HwSelfCureEngine.this.mWifiManager != null && HwSelfCureEngine.this.mWifiManager.isWifiEnabled() && (HwSelfCureEngine.this.isConnectingOrConnected() ^ 1) != 0 && (WifiProCommonUtils.isQueryActivityMatched(HwSelfCureEngine.this.mContext, "com.android.settings.Settings$WifiSettingsActivity") ^ 1) != 0) {
                HwSelfCureEngine.this.LOGD("ENTER: handleUserPresentEvent()");
                HwSelfCureEngine.this.enableAllNetowkrsByReason(new int[]{2, 3, 4});
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
                if (!(bssid == null || (HwSelfCureEngine.this.mDhcpFailedBssids.contains(bssid) ^ 1) == 0)) {
                    int bssidCnt = WifiProCommonUtils.getBssidCounter(config, HwSelfCureEngine.this.mWifiStateMachine.syncGetScanResultsList());
                    HwSelfCureEngine.this.LOGD("handleBssidDhcpFailed, bssidCnt = " + bssidCnt);
                    if (bssidCnt >= 2) {
                        HwSelfCureEngine.this.LOGD("handleBssidDhcpFailed, add key = " + config.configKey());
                        HwSelfCureEngine.this.mDhcpFailedBssids.add(bssid);
                        if (!(config.configKey() == null || (HwSelfCureEngine.this.mDhcpFailedConfigKeys.contains(config.configKey()) ^ 1) == 0)) {
                            HwSelfCureEngine.this.mDhcpFailedConfigKeys.add(config.configKey());
                        }
                    }
                }
            }
        }

        private void updateAutoConnFailedNetworks(List<ScanResult> scanResults) {
            List<String> refreshedNetworksKey = HwSelfCureUtils.getRefreshedCureFailedNetworks(HwSelfCureEngine.this.networkCureFailedHistory);
            for (int i = 0; i < refreshedNetworksKey.size(); i++) {
                HwSelfCureEngine.this.LOGD("updateAutoConnFailedNetworks, refreshed cure failed network, currKey = " + ((String) refreshedNetworksKey.get(i)));
                HwSelfCureEngine.this.networkCureFailedHistory.remove(refreshedNetworksKey.get(i));
            }
            List<String> unstableKey = HwSelfCureUtils.searchUnstableNetworks(HwSelfCureEngine.this.autoConnectFailedNetworks, scanResults);
            for (int j = 0; j < unstableKey.size(); j++) {
                HwSelfCureEngine.this.LOGD("updateAutoConnFailedNetworks, remove it due to signal unstable, currKey = " + ((String) unstableKey.get(j)));
                HwSelfCureEngine.this.autoConnectFailedNetworks.remove(unstableKey.get(j));
                HwSelfCureEngine.this.autoConnectFailedNetworksRssi.remove(unstableKey.get(j));
            }
        }

        private void selectHighestFailedNetworkAndCure() {
            if (HwSelfCureEngine.this.autoConnectFailedNetworks.size() == 0) {
                HwSelfCureEngine.this.mNoAutoConnCounter = 0;
                return;
            }
            HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
            if (hwSelfCureEngine.mNoAutoConnCounter = hwSelfCureEngine.mNoAutoConnCounter + 1 < 3) {
                HwSelfCureEngine.this.LOGD("selectHighestFailedNetworkAndCure, MAX_FAILED_CURE unmatched, wait more time for self cure.");
                return;
            }
            WifiConfiguration bestSelfCureCandidate = HwSelfCureUtils.selectHighestFailedNetwork(HwSelfCureEngine.this.networkCureFailedHistory, HwSelfCureEngine.this.autoConnectFailedNetworks, HwSelfCureEngine.this.autoConnectFailedNetworksRssi);
            if (bestSelfCureCandidate != null) {
                HwSelfCureEngine.this.LOGD("selectHighestFailedNetworkAndCure, delay 1s to self cure the selected candidate = " + bestSelfCureCandidate.configKey());
                Message dmsg = Message.obtain();
                dmsg.what = 102;
                dmsg.obj = bestSelfCureCandidate;
                HwSelfCureEngine.this.sendMessageDelayed(dmsg, 1000);
            }
        }

        private void trySelfCureSelectedNetwork(WifiConfiguration config) {
            if (config != null && config.networkId != -1 && !HwSelfCureEngine.this.isConnectingOrConnected()) {
                HwSelfCureEngine.this.LOGD("ENTER: trySelfCureSelectedNetwork(), config = " + config.configKey());
                if (HwSelfCureUtils.isWifiProEnabled()) {
                    if (WifiProCommonUtils.isOpenAndPortal(config) || WifiProCommonUtils.isOpenAndMaybePortal(config)) {
                        HwSelfCureEngine.this.mWifiStateMachine.setWifiBackgroundReason(0);
                        HwSelfCureEngine.this.LOGD("trySelfCureSelectedNetwork, self cure at background, due to [maybe] portal, candidate = " + config.configKey());
                    } else if (config.noInternetAccess && (WifiProCommonUtils.allowWifiConfigRecovery(config.internetHistory) ^ 1) != 0) {
                        HwSelfCureEngine.this.mWifiStateMachine.setWifiBackgroundReason(3);
                        HwSelfCureEngine.this.LOGD("trySelfCureSelectedNetwork, self cure at background, due to no internet, candidate = " + config.configKey());
                    }
                }
                HwSelfCureEngine.this.mWifiStateMachine.startConnectToUserSelectNetwork(config.networkId, null);
                int chrType = -1;
                NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                int disableReason = status.getNetworkSelectionDisableReason();
                if (disableReason == 3) {
                    chrType = 10;
                } else if (disableReason == 2) {
                    chrType = 11;
                } else if (disableReason == 4) {
                    chrType = 12;
                } else if (disableReason == 10) {
                    chrType = 13;
                } else if (status.isNetworkEnabled()) {
                    chrType = 24;
                }
                if (chrType != -1) {
                    HwSelfCureEngine.this.mNoAutoConnReason = chrType;
                    HwSelfCureEngine.this.mNoAutoConnConfig = config;
                }
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
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("[ ");
            sbuf.append("dnsSelfCureFailedCnt = ").append(this.dnsSelfCureFailedCnt);
            sbuf.append(", renewDhcpSelfCureFailedCnt = ").append(this.renewDhcpSelfCureFailedCnt);
            sbuf.append(", staticIpSelfCureFailedCnt = ").append(this.staticIpSelfCureFailedCnt);
            sbuf.append(", reassocSelfCureFailedCnt = ").append(this.reassocSelfCureFailedCnt);
            sbuf.append(", resetSelfCureFailedCnt = ").append(this.resetSelfCureFailedCnt);
            sbuf.append(", reassocSelfCureConnectFailedCnt = ").append(this.reassocSelfCureConnectFailedCnt);
            sbuf.append(", resetSelfCureConnectFailedCnt = ").append(this.resetSelfCureConnectFailedCnt);
            sbuf.append(" ]");
            return sbuf.toString();
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
        private int mLastSelfCureLevel;
        private boolean mPortalUnthenEver;
        private int mSelfCureFailedCounter;
        private InternetSelfCureHistoryInfo mSelfCureHistoryInfo;
        private boolean mSetStaticIp4InvalidIp = false;
        private List<Integer> mTestedSelfCureLevel = new ArrayList();
        private String mUnconflictedIp;
        private boolean mUserSetStaticIpConfig;

        InternetSelfCureState() {
        }

        public void enter() {
            boolean z = false;
            HwSelfCureEngine.this.LOGD("==> ##InternetSelfCureState");
            this.mCurrentRssi = WifiHandover.INVALID_RSSI;
            this.mSelfCureFailedCounter = 0;
            this.mCurrentAbnormalType = -1;
            this.mLastSelfCureLevel = -1;
            this.mCurrentSelfCureLevel = 200;
            this.mHasInternetRecently = false;
            this.mPortalUnthenEver = false;
            this.mUserSetStaticIpConfig = false;
            this.mCurrentGateway = getCurrentGateway();
            this.mTestedSelfCureLevel.clear();
            this.mFinalSelfCureUsed = false;
            this.mSetStaticIp4InvalidIp = false;
            this.mUnconflictedIp = null;
            WifiInfo wifiInfo = HwSelfCureEngine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mCurrentRssi = wifiInfo.getRssi();
                this.mCurrentBssid = wifiInfo.getBSSID();
                HwSelfCureEngine.this.LOGD("InternetSelfCureState, network = " + wifiInfo.getSSID() + ", signal rssi = " + this.mCurrentRssi);
            }
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (config != null) {
                this.mSelfCureHistoryInfo = HwSelfCureUtils.string2InternetSelfCureHistoryInfo(config.internetSelfCureHistory);
                this.mHasInternetRecently = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
                this.mPortalUnthenEver = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102);
                if (config.getIpAssignment() != null && config.getIpAssignment() == IpAssignment.STATIC) {
                    z = true;
                }
                this.mUserSetStaticIpConfig = z;
                this.mLastHasInetTimeMillis = config.lastHasInternetTimestamp;
                this.mConfigAuthType = config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType();
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
                    if (this.mCurrentSelfCureLevel == HwSelfCureUtils.RESET_LEVEL_HIGH_RESET && HwABSUtils.getABSEnable()) {
                        HwABSDetectorService service = HwABSDetectorService.getInstance();
                        if (service != null) {
                            service.notifySelEngineResetCompelete();
                        }
                    }
                    HwSelfCureUtils.updateSelfCureConnectHistoryInfo(this.mSelfCureHistoryInfo, this.mCurrentSelfCureLevel, true);
                    if (confirmInternetSelfCure(this.mCurrentSelfCureLevel)) {
                        this.mCurrentSelfCureLevel = 200;
                        this.mSelfCureFailedCounter = 0;
                        this.mHasInternetRecently = true;
                        break;
                    }
                    break;
                case 114:
                    if (HwSelfCureEngine.this.isSuppOnCompletedState()) {
                        this.mCurrentSelfCureLevel = message.arg1;
                        selfCureWifiLink(message.arg1);
                        break;
                    }
                    break;
                case 116:
                    HwSelfCureEngine.this.LOGD("CMD_IP_CONFIG_TIMEOUT during self cure state. currentAbnormalType = " + this.mCurrentAbnormalType);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.mWifiStateMachine.resetIpConfigStatus();
                    if (this.mCurrentAbnormalType == 2 && ((this.mConfigAuthType == 1 || this.mConfigAuthType == 4) && WifiProCommonUtils.getBssidCounter(HwSelfCureEngine.this.mSelfCureConfig, HwSelfCureEngine.this.mWifiStateMachine.syncGetScanResultsList()) <= 3 && (this.mFinalSelfCureUsed ^ 1) != 0)) {
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
                case 128:
                    HwSelfCureEngine.this.LOGD("CMD_P2P_DISCONNECTED_EVENT during self cure state.");
                    handleRssiChanged();
                    break;
                case HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT /*130*/:
                    this.mSetStaticIp4InvalidIp = HwSelfCureEngine.this.handleInvalidDhcpOffer(this.mUnconflictedIp);
                    break;
                case HwSelfCureEngine.CMD_SETTINGS_DISPLAY_NO_INTERNET_EVENT /*131*/:
                    if (HwSelfCureEngine.this.mHwWifiStatStore != null) {
                        HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(28);
                        break;
                    }
                    break;
                case HwSelfCureEngine.CMD_ROUTER_GATEWAY_UNREACHABLE_EVENT /*132*/:
                    if (HwSelfCureEngine.this.mHwWifiStatStore != null) {
                        HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(27);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
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
            if (connectType == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                chrType = 18;
            } else if (connectType == HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) {
                chrType = 19;
            }
            if (chrType != -1 && HwSelfCureEngine.this.mHwWifiStatStore != null) {
                HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(chrType);
            }
        }

        private void uploadCurrentAbnormalStatistics() {
            if (this.mCurrentAbnormalType != -1 && HwSelfCureEngine.this.mHwWifiStatStore != null) {
                HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(this.mCurrentAbnormalType);
                this.mCurrentAbnormalType = -1;
            }
        }

        private void uploadInternetCureSuccCounter(int selfCureType) {
            uploadCurrentAbnormalStatistics();
            int chrType = -1;
            if (selfCureType == HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS) {
                chrType = 4;
            } else if (selfCureType == HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) {
                chrType = 5;
            } else if (selfCureType == HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) {
                chrType = 6;
            } else if (selfCureType == HwSelfCureUtils.RESET_LEVEL_RECONNECT_4_INVALID_IP) {
                chrType = 30;
            } else if (selfCureType == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                chrType = 7;
            } else if (selfCureType == HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) {
                if (this.mLastSelfCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS) {
                    chrType = 20;
                } else if (this.mLastSelfCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) {
                    chrType = 21;
                } else if (this.mLastSelfCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) {
                    chrType = 22;
                } else if (this.mLastSelfCureLevel == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                    chrType = 8;
                }
            } else if (selfCureType == HwSelfCureUtils.RESET_LEVEL_DEAUTH_BSSID) {
                chrType = 8;
            }
            if (chrType != -1 && HwSelfCureEngine.this.mHwWifiStatStore != null) {
                HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(chrType);
            }
        }

        private void handleInternetFailedAndUserSetStaticIp(int internetFailedType) {
            if (this.mHasInternetRecently && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                saveCurrentAbnormalType(internetFailedType);
                if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                    this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
                } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                    this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
                } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY) {
                    this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
                }
                HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
                return;
            }
            HwSelfCureEngine.this.LOGD("handleInternetFailedAndUserSetStaticIp, user set static ip config, ignore to update config for user.");
            if (!HwSelfCureEngine.this.mInternetUnknown) {
                this.mCurrentAbnormalType = HwSelfCureUtils.RESET_REJECTED_BY_STATIC_IP_ENABLED;
                uploadCurrentAbnormalStatistics();
            }
        }

        private int selectBestSelfCureSolution(int internetFailedType) {
            boolean multipleDhcpServer = HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2;
            boolean noInternetWhenConnected = this.mLastHasInetTimeMillis <= 0 || this.mLastHasInetTimeMillis < HwSelfCureEngine.this.mConnectedTimeMills;
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
                } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC)) {
                    return HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                } else {
                    return 200;
                }
            }
        }

        private void selectSelfCureByFailedReason(int internetFailedType) {
            HwSelfCureEngine.this.LOGD("selectSelfCureByFailedReason, internetFailedType = " + internetFailedType + ", userSetStaticIp = " + this.mUserSetStaticIpConfig);
            if (this.mUserSetStaticIpConfig && (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS || internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY || internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING)) {
                handleInternetFailedAndUserSetStaticIp(internetFailedType);
                return;
            }
            int requestSelfCureLevel = selectBestSelfCureSolution(internetFailedType);
            if (requestSelfCureLevel != 200) {
                saveCurrentAbnormalType(internetFailedType);
                HwSelfCureEngine.this.sendMessage(114, requestSelfCureLevel, 0);
            } else if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                HwSelfCureEngine.this.LOGD("selectSelfCureByFailedReason, use wifi reset to cure this failed type = " + internetFailedType);
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
                HwSelfCureEngine.this.LOGD("selectSelfCureByFailedReason, no usable self cure for this failed type = " + internetFailedType);
            }
        }

        private boolean confirmInternetSelfCure(int currentCureLevel) {
            HwSelfCureEngine.this.LOGD("confirmInternetSelfCure, cureLevel = " + currentCureLevel + ", last failed counter = " + this.mSelfCureFailedCounter + ", finally = " + this.mFinalSelfCureUsed);
            if (currentCureLevel != 200) {
                if (HwSelfCureEngine.this.isHttpReachable(true)) {
                    handleHttpReachableAfterSelfCure(currentCureLevel);
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                    return true;
                }
                this.mSelfCureFailedCounter++;
                HwSelfCureUtils.updateSelfCureHistoryInfo(this.mSelfCureHistoryInfo, currentCureLevel, false);
                updateWifiConfig(null);
                HwSelfCureEngine.this.LOGD("HTTP unreachable, self cure failed for " + currentCureLevel + ", selfCureHistoryInfo = " + this.mSelfCureHistoryInfo);
                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                if (this.mFinalSelfCureUsed) {
                    handleHttpUnreachableFinally();
                    return false;
                } else if (currentCureLevel != HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) {
                    if (currentCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) {
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
                    } else if (currentCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP && HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2) {
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
                } else if (getNextTestDhcpResults() == null || (hasBeenTested(HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) ^ 1) == 0) {
                    handleHttpUnreachableFinally();
                } else {
                    this.mFinalSelfCureUsed = true;
                    this.mLastSelfCureLevel = currentCureLevel;
                    this.mConfigStaticIp4MultiDhcpServer = true;
                    HwSelfCureEngine.this.LOGD("HTTP unreachable, and has next dhcp results, try next one for wifi reset failed.");
                    HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP, 0);
                }
            }
            return false;
        }

        private boolean hasBeenTested(int cureLevel) {
            for (int i = 0; i < this.mTestedSelfCureLevel.size(); i++) {
                if (((Integer) this.mTestedSelfCureLevel.get(i)).intValue() == cureLevel) {
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
            HwSelfCureEngine.this.LOGD("handleHttpReachableAfterSelfCure, cureLevel = " + currentCureLevel + ", HTTP reachable, --> ConnectedMonitorState.");
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
                HwSelfCureEngine.this.mStaticIpCureSuccess = true;
            } else if (currentCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) {
                this.mCurrentAbnormalType = 3;
                HwSelfCureEngine.this.requestArpConflictTest(dhcpResults);
                HwSelfCureEngine.this.mStaticIpCureSuccess = true;
            }
            uploadInternetCureSuccCounter(currentCureLevel);
            HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_DHCP_RESULTS_UPDATE, strDhcpResults, 500);
        }

        private void handleRssiChanged() {
            if (this.mCurrentRssi > -76 && (HwSelfCureEngine.this.mSelfCureOngoing.get() ^ 1) != 0 && (HwSelfCureEngine.this.mP2pConnected.get() ^ 1) != 0) {
                if (this.mDelayedReassocSelfCure || this.mDelayedResetSelfCure) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (HwSelfCureEngine.this.isHttpReachable(true)) {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        this.mDelayedReassocSelfCure = false;
                        this.mDelayedResetSelfCure = false;
                        HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                        return;
                    }
                    HwSelfCureEngine.this.LOGD("handleRssiChanged, Http failed, delayedReassoc = " + this.mDelayedReassocSelfCure + ", delayedReset = " + this.mDelayedResetSelfCure);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    if (this.mDelayedReassocSelfCure) {
                        HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC, 0);
                    } else if (this.mDelayedResetSelfCure) {
                        HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
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
            if (!(HwSelfCureEngine.this.hasMessages(114) || (hasBeenTested(HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) ^ 1) == 0 || (HwSelfCureEngine.this.mSelfCureOngoing.get() ^ 1) == 0)) {
                if (((!this.mDelayedReassocSelfCure ? this.mDelayedResetSelfCure : 1) ^ 1) != 0) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (HwSelfCureEngine.this.isHttpReachable(false)) {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                    } else {
                        HwSelfCureEngine.this.LOGD("handleRoamingDetected, and HTTP access failed, trigger Re-Dhcp for it first time.");
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        HwSelfCureEngine.this.sendMessage(114, HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP, 0);
                    }
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
            for (Entry entry : HwSelfCureEngine.this.mDhcpOfferPackets.entrySet()) {
                String gatewayKey = (String) entry.getKey();
                String dhcpResults = (String) entry.getValue();
                if (!(gatewayKey == null || (gatewayKey.equals(this.mCurrentGateway) ^ 1) == 0)) {
                    boolean untested = true;
                    for (int i = 0; i < HwSelfCureEngine.this.mDhcpResultsTestDone.size(); i++) {
                        if (gatewayKey.equals(HwSelfCureEngine.this.mDhcpResultsTestDone.get(i))) {
                            untested = false;
                            break;
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
            if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS) {
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_LOW_1_DNS");
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                HwSelfCureEngine.this.mWifiStateMachine.requestUpdateDnsServers(HwSelfCureUtils.getPublicDnsServers());
                HwSelfCureEngine.this.sendMessageDelayed(113, 1000);
            } else if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) {
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_LOW_2_RENEW_DHCP");
                HwSelfCureEngine.this.mDhcpOfferPackets.clear();
                HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                HwSelfCureEngine.this.mWifiStateMachine.requestRenewDhcp();
                HwSelfCureEngine.this.sendMessageDelayed(116, 6000);
            } else if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_DEAUTH_BSSID) {
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_DEAUTH_BSSID");
                WifiInjector.getInstance().getWifiNative().deauthLastRoamingBssidHw("2", "");
                HwSelfCureEngine.this.sendMessageDelayed(113, 15000);
            } else if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) {
                String dhcpResults = null;
                if (this.mConfigStaticIp4MultiDhcpServer) {
                    dhcpResults = getNextTestDhcpResults();
                } else if (this.mConfigStaticIp4GatewayChanged) {
                    dhcpResults = HwSelfCureEngine.this.getDhcpResultsHasInternet(HwSelfCureEngine.this.mWifiStateMachine.getCurrentBSSID(), HwSelfCureEngine.this.mSelfCureConfig);
                }
                String gatewayKey = WifiProCommonUtils.dhcpResults2Gateway(dhcpResults);
                if (!(dhcpResults == null || gatewayKey == null)) {
                    gatewayKey = gatewayKey.replace("/", "");
                    HwSelfCureEngine.this.LOGD("begin to self cure for internet access: TRY_NEXT_DHCP_OFFER, gatewayKey = " + gatewayKey);
                    HwSelfCureEngine.this.mDhcpResultsTestDone.add(gatewayKey);
                    StaticIpConfiguration staticIpConfig = WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResults);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                    HwSelfCureEngine.this.mWifiStateMachine.requestUseStaticIpConfig(staticIpConfig);
                    HwSelfCureEngine.this.sendMessageDelayed(116, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                }
            } else if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_RECONNECT_4_INVALID_IP) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mUnconflictedIp = HwSelfCureEngine.this.getLegalIpConfiguration();
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_RECONNECT_4_INVALID_IP");
                HwSelfCureEngine.this.mWifiStateMachine.handleInvalidIpAddr();
            } else if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                if (this.mCurrentRssi <= -80 || (HwSelfCureEngine.this.mP2pConnected.get() ^ 1) == 0) {
                    this.mDelayedReassocSelfCure = true;
                } else {
                    HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_MIDDLE_REASSOC");
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                    this.mDelayedReassocSelfCure = false;
                    HwSelfCureEngine.this.mWifiStateMachine.requestReassocLink();
                }
            } else if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) {
                if (HwSelfCureEngine.this.mInternetUnknown) {
                    HwSelfCureEngine.this.mRouterInternetDetector.notifyNoInternetAfterCure(this.mCurrentGateway, this.mConfigAuthType, HwSelfCureEngine.this.mMobileHotspot);
                } else if (this.mCurrentRssi <= -76 || (HwSelfCureEngine.this.mP2pConnected.get() ^ 1) == 0) {
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
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mNetworkChecker = new HwNetworkPropertyChecker(context, this.mWifiManager, null, true, null, true);
        this.mHwWifiStatStore = HwWifiStatStoreImpl.getDefault();
        this.mArpClient = new HwArpClient(this.mContext);
        this.mRouterInternetDetector = HwRouterInternetDetector.getInstance(context, this);
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
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String dhcpResults;
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
                    if (info != null && info.getDetailedState() == DetailedState.DISCONNECTED) {
                        HwSelfCureEngine.this.sendMessage(108);
                    } else if (info != null && info.getDetailedState() == DetailedState.CONNECTED) {
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
                    dhcpResults = intent.getStringExtra("com.hw.wifipro.FLAG_DHCP_OFFER_INFO");
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
                    dhcpResults = intent.getStringExtra("com.hw.wifipro.FLAG_DHCP_OFFER_INFO");
                    if (dhcpResults != null) {
                        HwSelfCureEngine.this.LOGD("ACTION_INVALID_DHCP_OFFER_RCVD, dhcpResults = " + dhcpResults);
                        HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INVALID_DHCP_OFFER_EVENT, dhcpResults, 2000);
                    }
                } else if ("android.intent.action.USER_PRESENT".equals(intent.getAction()) && HwSelfCureEngine.this.getCurrentState() == HwSelfCureEngine.this.mDisconnectedMonitorState) {
                    HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_USER_PRESENT_RCVD);
                }
            }
        }, intentFilter);
    }

    private String getDhcpResultsHasInternet(String currentBssid, WifiConfiguration config) {
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

    private boolean handleInvalidDhcpOffer(String dhcpResults) {
        if (dhcpResults == null) {
            return false;
        }
        this.mWifiStateMachine.requestUseStaticIpConfig(WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResults));
        return true;
    }

    private String getLegalIpConfiguration() {
        DhcpResults dhcpResults = this.mWifiStateMachine.syncGetDhcpResults();
        LOGD("getLegalIpConfiguration, dhcpResults " + dhcpResults);
        if (dhcpResults == null || dhcpResults.gateway == null || dhcpResults.ipAddress == null) {
            return null;
        }
        InetAddress gateway = dhcpResults.gateway;
        InetAddress initialIpAddr = dhcpResults.ipAddress.getAddress();
        int testCnt = 0;
        ArrayList<InetAddress> conflictedIpAddr = new ArrayList();
        InetAddress testIpAddr = initialIpAddr;
        while (true) {
            int i = testCnt;
            testCnt = i + 1;
            if (i < 3 && testIpAddr != null) {
                conflictedIpAddr.add(testIpAddr);
                testIpAddr = HwSelfCureUtils.getNextIpAddr(gateway, initialIpAddr, conflictedIpAddr);
                if (testIpAddr != null && (this.mArpClient.doSlowArpTest((Inet4Address) testIpAddr) ^ 1) != 0) {
                    LOGD("getLegalIpConfiguration, find a new unconflicted one.");
                    dhcpResults.ipAddress = new LinkAddress(testIpAddr, dhcpResults.ipAddress.getPrefixLength(), dhcpResults.ipAddress.getFlags(), dhcpResults.ipAddress.getScope());
                    return WifiProCommonUtils.dhcpResults2String(dhcpResults, -1);
                }
            }
        }
        try {
            byte[] oldIpAddr = dhcpResults.ipAddress.getAddress().getAddress();
            oldIpAddr[3] = (byte) -100;
            LinkAddress newIpAddress = new LinkAddress(InetAddress.getByAddress(oldIpAddr), dhcpResults.ipAddress.getPrefixLength(), dhcpResults.ipAddress.getFlags(), dhcpResults.ipAddress.getScope());
            LOGD("handleInvalidDhcpOffer newIpAddress = " + newIpAddress);
            dhcpResults.ipAddress = newIpAddress;
            return WifiProCommonUtils.dhcpResults2String(dhcpResults, -1);
        } catch (UnknownHostException e) {
            LOGD("handleInvalidDhcpOffer, UnknownHostException rcvd!");
            return null;
        }
    }

    private void notifyHttpReachableForWifiPro(boolean httpReachable) {
        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        if (wifiProStateMachine != null) {
            wifiProStateMachine.notifyHttpReachable(httpReachable);
        }
    }

    private void notifyRoamingCompletedForWifiPro(String newBssid) {
        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        if (wifiProStateMachine != null) {
            wifiProStateMachine.notifyRoamingCompleted(newBssid);
        }
    }

    private boolean isConnectingOrConnected() {
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

    private boolean isSuppOnCompletedState() {
        WifiInfo info = this.mWifiStateMachine.getWifiInfo();
        if (info == null || info.getSupplicantState().ordinal() != SupplicantState.COMPLETED.ordinal()) {
            return false;
        }
        return true;
    }

    private void handleNetworkConnected() {
        LOGD("ENTER: handleNetworkConnected()");
        if (!updateConnSelfCureFailedHistory()) {
            LOGD("handleNetworkConnected, config is null for update, delay 2s to update again.");
            sendMessageDelayed(CMD_UPDATE_CONN_SELF_CURE_HISTORY, 2000);
        }
        this.mNoAutoConnCounter = 0;
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        enableAllNetowkrsByReason(new int[]{1, 9, 10});
        this.mConnectedTimeMills = System.currentTimeMillis();
        synchronized (this.mDhcpFailedBssidLock) {
            this.mDhcpFailedBssids.clear();
            this.mDhcpFailedConfigKeys.clear();
        }
        transitionTo(this.mConnectedMonitorState);
    }

    private boolean updateConnSelfCureFailedHistory() {
        WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        if (config == null || config.configKey() == null) {
            return false;
        }
        this.networkCureFailedHistory.remove(config.configKey());
        LOGD("updateConnSelfCureFailedHistory(), networkCureFailedHistory remove " + config.configKey());
        if (this.mNoAutoConnConfig != null && config.configKey().equals(this.mNoAutoConnConfig.configKey())) {
            if (!(this.mNoAutoConnReason == -1 || this.mHwWifiStatStore == null)) {
                this.mHwWifiStatStore.updateScCHRCount(this.mNoAutoConnReason);
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
            if (!(chrType == -1 || this.mHwWifiStatStore == null)) {
                this.mHwWifiStatStore.updateScCHRCount(chrType);
            }
        }
        return true;
    }

    private void enableAllNetowkrsByReason(int[] enabledReasons) {
        List<WifiConfiguration> savedNetworks = this.mWifiManager.getConfiguredNetworks();
        if (savedNetworks == null || savedNetworks.size() == 0) {
            LOGD("enableAllNetowkrsByReason, no saved networks found.");
            return;
        }
        for (int i = 0; i < savedNetworks.size(); i++) {
            WifiConfiguration nextConfig = (WifiConfiguration) savedNetworks.get(i);
            NetworkSelectionStatus status = nextConfig.getNetworkSelectionStatus();
            int disableReason = status.getNetworkSelectionDisableReason();
            if (!status.isNetworkEnabled()) {
                for (int i2 : enabledReasons) {
                    if (disableReason == i2) {
                        LOGD("To enable network which status is " + disableReason + ", config = " + nextConfig.configKey() + ", id = " + nextConfig.networkId);
                        this.mWifiManager.enableNetwork(nextConfig.networkId, false);
                        break;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x001c, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:14:0x0035, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:17:0x0038, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isHttpReachable(boolean useDoubleServers) {
        synchronized (this.mNetworkChecker) {
            int mainSvrRespCode = this.mNetworkChecker.isCaptivePortal(true);
            int mainSvrRawRespCode = this.mNetworkChecker.getRawHttpRespCode();
            if (!WifiProCommonUtils.unreachableRespCode(mainSvrRespCode) || mainSvrRawRespCode == 602) {
            } else if (useDoubleServers) {
                int backupSvrRespCode = this.mNetworkChecker.recheckWithBakcupServer(true);
                int backupSvrRawRespCode = this.mNetworkChecker.getRawHttpRespCode();
                if (!WifiProCommonUtils.unreachableRespCode(backupSvrRespCode) || backupSvrRawRespCode == 602) {
                }
            }
        }
    }

    private boolean isIpAddressInvalid() {
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

    private void handleWifiDisabled(boolean selfCureGoing) {
        LOGD("ENTER: handleWifiDisabled(), selfCureGoing = " + selfCureGoing);
        this.mNoAutoConnCounter = 0;
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        this.networkCureFailedHistory.clear();
        if (selfCureGoing) {
            transitionTo(this.mDisconnectedMonitorState);
        }
    }

    private void handleWifiEnabled() {
        LOGD("ENTER: handleWifiEnabled()");
        enableAllNetowkrsByReason(new int[]{1, 9, 10});
    }

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
            sendMessageDelayed(110, bssid, 2000);
            notifyRoamingCompletedForWifiPro(bssid);
        }
    }

    public synchronized void notifySefCureCompleted(int status) {
        LOGD("ENTER: notifySefCureCompleted, status = " + status);
        if (this.mInitialized && status == 0) {
            sendMessage(113);
        } else if (-1 == status) {
            sendMessage(120);
        } else {
            this.mSelfCureOngoing.set(false);
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

    public synchronized boolean notifyIpConfigLostAndHandle(WifiConfiguration config) {
        if (this.mInitialized && config != null) {
            int currRssi = WifiProCommonUtils.getCurrentRssi(this.mWifiManager);
            this.mIpConfigLostCnt++;
            LOGD("ENTER: notifyIpConfigLostAndHandle() IpConfigLostCnt = " + this.mIpConfigLostCnt + ", ssid = " + config.SSID + ", currRssi = " + currRssi);
            if (currRssi >= -75 && getCurrentState() == this.mDisconnectedMonitorState) {
                if (this.mIpConfigLostCnt == 2 && hasDhcpResultsSaved(config)) {
                    sendMessage(124, config);
                    return true;
                } else if (this.mIpConfigLostCnt >= 1 && (hasDhcpResultsSaved(config) ^ 1) != 0) {
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
        }
    }

    public synchronized void notifySettingsDisplayNoInternet() {
        sendMessage(CMD_SETTINGS_DISPLAY_NO_INTERNET_EVENT);
    }

    public synchronized void notifyRouterGatewayUnreachable() {
        sendMessage(CMD_ROUTER_GATEWAY_UNREACHABLE_EVENT);
    }

    public void requestArpConflictTest(DhcpResults dhcpResults) {
        if (dhcpResults != null && dhcpResults.ipAddress != null && this.mHwWifiStatStore != null) {
            InetAddress addr = dhcpResults.ipAddress.getAddress();
            if (this.mArpClient != null && addr != null && (addr instanceof Inet4Address) && this.mArpClient.doSlowArpTest((Inet4Address) addr)) {
                LOGD("requestArpConflictTest, Upload static ip conflicted chr!");
                this.mHwWifiStatStore.updateScCHRCount(26);
            }
        }
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
