package com.huawei.wifi2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.IpConfiguration;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.MatchAllNetworkSpecifier;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.TrafficStats;
import android.net.ip.IIpClient;
import android.net.ip.IpClientCallbacks;
import android.net.ip.IpClientManager;
import android.net.ip.IpClientUtil;
import android.net.shared.ProvisioningConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkAgentSpecifier;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.LruCache;
import android.util.Pair;
import android.util.wifi.HwHiLog;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.huawei.wifi2.HwWifi2Native;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwWifi2ClientModeImpl extends HwWifi2BaseStateMachine {
    private static final int BAND_WIDTH = 1048576;
    private static final int CODE_SET_ARP_IGNORE_CMD = 1124;
    private static final int DEFAULT_POLL_RSSI_INTERVAL_MSECS = 3000;
    private static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    private static final int DHCP_RESULT_CACHE_SIZE = 5;
    private static final int DISABLE_ARP_IGNORE = 0;
    private static final int ENABLE_ARP_IGNORE = 1;
    private static final String IFACE_NAME_ALL_STR = "all";
    private static final String IFACE_NAME_WLAN0_STR = "wlan0";
    private static final String IFACE_NAME_WLAN1_STR = "wlan1";
    private static final String NETWORKTYPE_SLAVE_WIFI = "SLAVE_WIFI";
    private static final int SCREEN_OFF = 0;
    private static final int SCREEN_ON = 1;
    private static final int SLAVE_WIFI_TRANSITION_SCORE = 10;
    private static final String SUPPLICANT_BSSID_ANY = "any";
    private static final String TAG = "HwWifi2ClientModeImpl";
    private static final int TWO_SECOND_LENGTH = 2000;
    private static final String TYPE_GET_CACHE_DHCP_RESULT = "getCachedDhcpResultsForCurrentConfig";
    private static final int WIFI_TRANSITION_INIT_SCORE = 0;
    private final HwWifi2Clock mClock;
    private State mConnectModeState = new ConnectModeState();
    private State mConnectedState = new ConnectedState();
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private final LruCache<String, DhcpResults> mDhcpResultCache = new LruCache<>(DHCP_RESULT_CACHE_SIZE);
    private DhcpResults mDhcpResults;
    private final Object mDhcpResultsLock = new Object();
    private State mDisconnectedState = new DisconnectedState();
    private State mDisconnectingState = new DisconnectingState();
    private int mDisconnectingWatchdogCount = 0;
    private final HwWifi2ConnectivityManager mHwWifi2ConnectivityManager;
    private String mInterfaceName;
    private volatile IpClientManager mIpClient;
    private IpClientCallbacksImpl mIpClientCallbacks;
    private boolean mIsAutoRoaming = false;
    private boolean mIsBluetoothConnectionActive = false;
    private boolean mIsConnectedMacRandomzationSupported = false;
    private boolean mIsEnableRssiPolling = false;
    private boolean mIsIpReachabilityDisconnectEnabled = true;
    private boolean mIsRegisterNetworkFactoryFlag = false;
    private State mL2ConnectedState = new L2ConnectedState();
    private String mLastBssid;
    private long mLastDriverRoamAttempt = 0;
    private long mLastLinkLayerStatsUpdate = 0;
    private int mLastNetworkId;
    private int mLastSignalLevel = -1;
    private LinkProperties mLinkProperties;
    private HwWifi2NetworkAgent mNetworkAgent;
    private final Object mNetworkAgentLock = new Object();
    private final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();
    private HwWifi2NetworkFactory mNetworkFactory;
    private NetworkInfo mNetworkInfo;
    private final NetworkMisc mNetworkMisc = new NetworkMisc();
    private State mObtainingIpState = new ObtainingIpState();
    private volatile int mPollRssiIntervalMsecs = DEFAULT_POLL_RSSI_INTERVAL_MSECS;
    private AsyncChannel mReplyChannel = new AsyncChannel();
    private int mRoamWatchdogCount = 0;
    private State mRoamingState = new RoamingState();
    private int mRssiPollToken = 0;
    private byte[] mRssiRanges;
    private HwWifi2SupplicantStateTracker mSupplicantStateTracker;
    private int mSuspendOptNeedsDisabled = 0;
    private PowerManager.WakeLock mSuspendWakeLock;
    private String mTargetRoamBssid = "any";
    private final String mTcpBufferSizes;
    private AtomicBoolean mUserWantsSuspendOpt = new AtomicBoolean(true);
    private WifiConfiguration mWifiConfiguration = null;
    private final HwExtendedWifi2Info mWifiInfo;
    private final HwWifi2Injector mWifiInjector;
    private final HwWifi2Monitor mWifiMonitor;
    private final HwWifi2Native mWifiNative;

    static /* synthetic */ int access$4008(HwWifi2ClientModeImpl x0) {
        int i = x0.mRssiPollToken;
        x0.mRssiPollToken = i + 1;
        return i;
    }

    static /* synthetic */ int access$6608(HwWifi2ClientModeImpl x0) {
        int i = x0.mRoamWatchdogCount;
        x0.mRoamWatchdogCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$7208(HwWifi2ClientModeImpl x0) {
        int i = x0.mDisconnectingWatchdogCount;
        x0.mDisconnectingWatchdogCount = i + 1;
        return i;
    }

    public HwWifi2ClientModeImpl(Context context, Looper looper, HwWifi2Injector wifiInjector, HwWifi2Native wifiNative) {
        super(TAG, looper);
        this.mWifiInjector = wifiInjector;
        this.mClock = wifiInjector.getClock();
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mNetworkInfo = new NetworkInfo(1, 0, NETWORKTYPE_SLAVE_WIFI, "");
        this.mWifiMonitor = this.mWifiInjector.getWifiMonitor();
        this.mWifiInfo = new HwExtendedWifi2Info();
        this.mWifiInfo.score = SLAVE_WIFI_TRANSITION_SCORE;
        this.mSupplicantStateTracker = new HwWifi2SupplicantStateTracker(context, getHandler());
        this.mLinkProperties = new LinkProperties();
        this.mNetworkInfo.setIsAvailable(false);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mHwWifi2ConnectivityManager = this.mWifiInjector.makeHwWifi2ConnectivityManager(this);
        this.mNetworkCapabilitiesFilter.addTransportType(1);
        this.mNetworkCapabilitiesFilter.addCapability(18);
        this.mNetworkCapabilitiesFilter.addCapability(39);
        this.mNetworkCapabilitiesFilter.setLinkUpstreamBandwidthKbps(BAND_WIDTH);
        this.mNetworkCapabilitiesFilter.setLinkDownstreamBandwidthKbps(BAND_WIDTH);
        this.mNetworkCapabilitiesFilter.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        this.mSuspendWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "WifiSuspend");
        this.mSuspendWakeLock.setReferenceCounted(false);
        this.mWifiInfo.setEnableConnectedMacRandomization(this.mIsConnectedMacRandomzationSupported);
        this.mTcpBufferSizes = this.mContext.getResources().getString(17039901);
        registerHwWifi2ClientModeBroadcast();
        addState(this.mDefaultState);
        addState(this.mConnectModeState, this.mDefaultState);
        addState(this.mDisconnectedState, this.mConnectModeState);
        addState(this.mL2ConnectedState, this.mConnectModeState);
        addState(this.mDisconnectingState, this.mConnectModeState);
        addState(this.mConnectedState, this.mL2ConnectedState);
        addState(this.mRoamingState, this.mL2ConnectedState);
        addState(this.mObtainingIpState, this.mL2ConnectedState);
        setInitialState(this.mDefaultState);
    }

    private void registerHwWifi2ClientModeBroadcast() {
        IntentFilter wifi2ScreenFilter = new IntentFilter();
        wifi2ScreenFilter.addAction("android.intent.action.SCREEN_ON");
        wifi2ScreenFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.huawei.wifi2.HwWifi2ClientModeImpl.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int screenState = -1;
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    screenState = 1;
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    screenState = 0;
                } else {
                    HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "BroadcastReceiver do nothing", new Object[0]);
                }
                if (screenState != -1) {
                    HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_SCREEN_STATE_CHANGED, screenState);
                }
            }
        }, wifi2ScreenFilter);
    }

    public void start() {
        super.start();
    }

    public boolean clearTargetBssid(String dbg) {
        HwHiLog.i(TAG, false, "clearTargetBssid: %{public}s", new Object[]{dbg});
        return this.mWifiNative.setConfiguredNetworkBssid(this.mInterfaceName, "any");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setTargetBssid(WifiConfiguration config, String bssid) {
        if (config == null || bssid == null) {
            return false;
        }
        String targetBssid = bssid;
        if (config.BSSID != null) {
            targetBssid = config.BSSID;
        }
        HwHiLog.i(TAG, false, "setTargetBssid: %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(targetBssid)});
        this.mTargetRoamBssid = targetBssid;
        config.getNetworkSelectionStatus().setNetworkSelectionBSSID(targetBssid);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processRssiThreshold(byte curRssi, int reason, HwWifi2Native.WifiRssiEventHandler rssiHandler) {
        if (curRssi == Byte.MAX_VALUE || curRssi == Byte.MIN_VALUE) {
            HwHiLog.e(TAG, false, "processRssiThreshold: Invalid rssi = %{public}d", new Object[]{Byte.valueOf(curRssi)});
            return;
        }
        int i = 0;
        while (true) {
            byte[] bArr = this.mRssiRanges;
            if (i >= bArr.length) {
                return;
            }
            if (curRssi < bArr[i]) {
                byte maxRssi = bArr[i];
                byte minRssi = bArr[i - 1];
                this.mWifiInfo.setRssi(curRssi);
                updateCapabilities();
                HwHiLog.i(TAG, false, "Re-program RSSI thresholds for : [%{public}d, %{public}d], curRssi= %{public}d, ret = %{public}d", new Object[]{Byte.valueOf(minRssi), Byte.valueOf(maxRssi), Byte.valueOf(curRssi), Integer.valueOf(startRssiMonitoringOffload(maxRssi, minRssi, rssiHandler))});
                return;
            }
            i++;
        }
    }

    private void registerForWifiMonitorEvents() {
        this.mWifiMonitor.registerHandler(this.mInterfaceName, HwWifi2ClientModeImplConst.CMD_TARGET_BSSID, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, HwWifi2ClientModeImplConst.CMD_ASSOCIATED_BSSID, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, HwWifi2Monitor.AUTHENTICATION_FAILURE_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, HwWifi2Monitor.ASSOCIATION_REJECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, HwWifi2Monitor.NETWORK_CONNECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, HwWifi2Monitor.NETWORK_DISCONNECTION_EVENT, getHandler());
        this.mWifiMonitor.registerHandler(this.mInterfaceName, HwWifi2Monitor.SUPPLICANT_STATE_CHANGE_EVENT, getHandler());
    }

    private void setMulticastFilter(boolean isFilterEnabled) {
        if (this.mIpClient != null) {
            this.mIpClient.setMulticastFilter(isFilterEnabled);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DhcpResults getCachedDhcpResultsForCurrentConfig() {
        HwHiLog.i(TAG, false, "getCachedDhcpResultsForCurrentConfig enter", new Object[0]);
        return this.mDhcpResultCache.get(this.mWifiInfo.getBSSID());
    }

    /* access modifiers changed from: package-private */
    public class IpClientCallbacksImpl extends IpClientCallbacks {
        private final ConditionVariable mWaitForCreationCv = new ConditionVariable(false);
        private final ConditionVariable mWaitForStopCv = new ConditionVariable(false);

        IpClientCallbacksImpl() {
        }

        public void onIpClientCreated(IIpClient ipClient) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "onIpClientCreated", new Object[0]);
            HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
            hwWifi2ClientModeImpl.mIpClient = new IpClientManager(ipClient, hwWifi2ClientModeImpl.getName());
            this.mWaitForCreationCv.open();
        }

        public void onPreDhcpAction() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "onPreDhcpAction", new Object[0]);
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_PRE_DHCP_ACTION);
        }

        public void onPostDhcpAction() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "onPostDhcpAction", new Object[0]);
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_POST_DHCP_ACTION);
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            if (dhcpResults == null) {
                HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_IPV4_PROVISIONING_FAILURE);
                HwWifi2ChrManager.uploadWifi2DhcpState(4);
            } else if ("CMD_TRY_CACHED_IP".equals(dhcpResults.domains)) {
                HwWifi2ClientModeImpl.this.sendMessage(131330);
            } else {
                HwWifi2ClientModeImpl.this.uploadDhcpState(dhcpResults);
                HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_IPV4_PROVISIONING_SUCCESS, dhcpResults);
                String bssid = HwWifi2ClientModeImpl.this.mWifiInfo.getBSSID();
                if (bssid != null) {
                    HwWifi2ClientModeImpl.this.mDhcpResultCache.put(bssid, new DhcpResults(dhcpResults));
                }
            }
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_UPDATE_LINKPROPERTIES, newLp);
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_IP_CONFIGURATION_SUCCESSFUL);
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_IP_CONFIGURATION_LOST);
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_UPDATE_LINKPROPERTIES, newLp);
        }

        public void onReachabilityLost(String logMsg) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "onReachabilityLost", new Object[0]);
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_IP_REACHABILITY_LOST, logMsg);
        }

        public void installPacketFilter(byte[] filter) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "installPacketFilter", new Object[0]);
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_INSTALL_PACKET_FILTER, filter);
        }

        public void startReadPacketFilter() {
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_READ_PACKET_FILTER);
        }

        public void setFallbackMulticastFilter(boolean isFilterEnabled) {
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_SET_FALLBACK_PACKET_FILTERING, Boolean.valueOf(isFilterEnabled));
        }

        public void setNeighborDiscoveryOffload(boolean isOffloadEnabled) {
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_CONFIG_ND_OFFLOAD, isOffloadEnabled ? 1 : 0);
        }

        public void onQuit() {
            this.mWaitForStopCv.open();
        }

        /* access modifiers changed from: package-private */
        public boolean awaitCreation() {
            return this.mWaitForCreationCv.block(10000);
        }

        /* access modifiers changed from: package-private */
        public boolean awaitShutdown() {
            return this.mWaitForStopCv.block(10000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopIpClient() {
        HwHiLog.i(TAG, false, "stopIpClient enter", new Object[0]);
        handlePostDhcpSetup();
        if (this.mIpClient != null) {
            this.mIpClient.stop();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean connectToUserSelectNetwork(int netId, int uid, boolean isForceReconnect) {
        HwHiLog.i(TAG, false, "connectToUserSelectNetwork netId = %{public}d, uid = %{public}d, isForceReconnect = %{public}s", new Object[]{Integer.valueOf(netId), Integer.valueOf(uid), String.valueOf(isForceReconnect)});
        if (this.mWifiConfiguration == null) {
            HwHiLog.e(TAG, false, "connectToUserSelectNetwork Invalid mWifiConfiguration", new Object[0]);
            return false;
        }
        if (isForceReconnect || this.mWifiInfo.getNetworkId() != netId || netId == -1) {
            startConnectToNetwork(netId, uid, "any");
        } else {
            HwHiLog.i(TAG, false, "connectToUserSelectNetwork already connect = %{public}d", new Object[]{Integer.valueOf(netId)});
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public WifiLinkLayerStats getWifiLinkLayerStats() {
        if (this.mInterfaceName == null) {
            HwHiLog.e(TAG, false, "getWifiLinkLayerStats called without an interface", new Object[0]);
            return null;
        }
        this.mLastLinkLayerStatsUpdate = this.mClock.getWallClockMillis();
        WifiLinkLayerStats stats = this.mWifiNative.getWifiLinkLayerStats(this.mInterfaceName);
        if (stats != null) {
            this.mWifiInfo.updatePacketRates(stats, this.mLastLinkLayerStatsUpdate);
        } else {
            this.mWifiInfo.updatePacketRates(TrafficStats.getTxPackets(this.mInterfaceName), TrafficStats.getRxPackets(this.mInterfaceName), this.mLastLinkLayerStatsUpdate);
        }
        return stats;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int stopWifiIpPacketOffload(int slot) {
        int ret = this.mWifiNative.stopSendingOffloadedPacket(this.mInterfaceName, slot);
        if (ret == 0) {
            return 0;
        }
        HwHiLog.e(TAG, false, "stopWifiIpPacketOffload(%{public}d): error %{public}d", new Object[]{Integer.valueOf(slot), Integer.valueOf(ret)});
        return -31;
    }

    private int startRssiMonitoringOffload(byte maxRssi, byte minRssi, HwWifi2Native.WifiRssiEventHandler handler) {
        HwHiLog.i(TAG, false, "start Rssi Monitoring", new Object[0]);
        return this.mWifiNative.startRssiMonitoring(this.mInterfaceName, maxRssi, minRssi, handler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int stopRssiMonitoringOffload() {
        HwHiLog.i(TAG, false, "stop Rssi Monitoring", new Object[0]);
        return this.mWifiNative.stopRssiMonitoring(this.mInterfaceName);
    }

    public boolean isConnected() {
        return getCurrentState() == this.mConnectedState;
    }

    public WifiInfo getWifiInfo() {
        return this.mWifiInfo;
    }

    public void handleIfaceDestroyed() {
        handleNetworkDisconnect();
    }

    public String getWifi2IfaceName() {
        return this.mInterfaceName;
    }

    public void setOperationalMode(int mode, String ifaceName) {
        HwHiLog.i(TAG, false, "setting mode to %{public}s for iface: %{public}s", new Object[]{HwWifi2ClientModeImplConst.connectModeMsgToString(mode), ifaceName});
        if (mode != 1) {
            transitionTo(this.mDefaultState);
        } else if (ifaceName != null) {
            this.mInterfaceName = ifaceName;
            transitionTo(this.mDisconnectedState);
        } else {
            HwHiLog.e(TAG, false, "supposed enter connect mode, but iface is null -> DefaultState", new Object[0]);
            transitionTo(this.mDefaultState);
        }
        HwHiLog.i(TAG, false, "%{public}s sendMessage CMD_SET_OPERATIONAL_MODE", new Object[]{getCurrentState().getName()});
        sendMessageAtFrontOfQueue(HwWifi2ClientModeImplConst.CMD_SET_OPERATIONAL_MODE);
    }

    public void disconnectCommand() {
        sendMessage(HwWifi2ClientModeImplConst.CMD_DISCONNECT);
    }

    public void enableRssiPolling(boolean isPollEnabled) {
        sendMessage(HwWifi2ClientModeImplConst.CMD_ENABLE_RSSI_POLL, isPollEnabled ? 1 : 0, 0);
    }

    public void setHighPerfModeEnabled(boolean isHighPerfEnable) {
        sendMessage(HwWifi2ClientModeImplConst.CMD_SET_HIGH_PERF_MODE, isHighPerfEnable ? 1 : 0, 0);
    }

    public Network getCurrentNetwork() {
        Network network = null;
        synchronized (this.mNetworkAgentLock) {
            if (this.mNetworkAgent != null) {
                network = new Network(this.mNetworkAgent.netId);
            }
        }
        return network;
    }

    public void sendBluetoothAdapterStateChange(int state) {
        sendMessage(HwWifi2ClientModeImplConst.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE, state, 0);
    }

    public void handleBootCompleted() {
        sendMessage(HwWifi2ClientModeImplConst.CMD_BOOT_COMPLETED);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenStateChanged(boolean isScreenOn) {
        if (this.mNetworkFactory == null) {
            HwHiLog.e(TAG, false, "handleScreenStateChanged before bootcomplete, just return ", new Object[0]);
            return;
        }
        HwHiLog.i(TAG, false, "Screen State Change to  = %{public}s", new Object[]{String.valueOf(isScreenOn)});
        enableRssiPolling(isScreenOn);
        if (this.mUserWantsSuspendOpt.get()) {
            int shouldReleaseWakeLock = 0;
            if (isScreenOn) {
                sendMessage(HwWifi2ClientModeImplConst.CMD_SET_SUSPEND_OPT_ENABLED, 0, 0);
            } else {
                if (isConnected()) {
                    this.mSuspendWakeLock.acquire(2000);
                    shouldReleaseWakeLock = 1;
                }
                sendMessage(HwWifi2ClientModeImplConst.CMD_SET_SUSPEND_OPT_ENABLED, 1, shouldReleaseWakeLock);
            }
        }
        getWifiLinkLayerStats();
    }

    private boolean checkAndSetConnectivityInstance() {
        if (this.mConnectivityManager != null) {
            return true;
        }
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSuspendOptimizationsNative(int reason, boolean isSuspendEnabled) {
        if (isSuspendEnabled) {
            this.mSuspendOptNeedsDisabled &= ~reason;
            if (this.mSuspendOptNeedsDisabled == 0 && this.mUserWantsSuspendOpt.get()) {
                this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, true);
                return;
            }
            return;
        }
        this.mSuspendOptNeedsDisabled |= reason;
        this.mWifiNative.setSuspendOptimizations(this.mInterfaceName, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSuspendOptimizations(int reason, boolean isSuspendEnabled) {
        HwHiLog.i(TAG, false, "setSuspendOptimizations: %{public}d %{public}s", new Object[]{Integer.valueOf(reason), String.valueOf(isSuspendEnabled)});
        if (isSuspendEnabled) {
            this.mSuspendOptNeedsDisabled &= ~reason;
        } else {
            this.mSuspendOptNeedsDisabled |= reason;
        }
        HwHiLog.i(TAG, false, "mSuspendOptNeedsDisabled %{public}d", new Object[]{Integer.valueOf(this.mSuspendOptNeedsDisabled)});
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fetchRssiLinkSpeedAndFrequencyNative() {
        HwWifi2Native.SignalPollResult pollResult = this.mWifiNative.signalPoll(this.mInterfaceName);
        if (pollResult != null) {
            int newRssi = pollResult.currentRssi;
            int newTxLinkSpeed = pollResult.txBitrate;
            int newFrequency = pollResult.associationFrequency;
            int newRxLinkSpeed = pollResult.rxBitrate;
            HwHiLog.i(TAG, false, "fetchRssiLinkSpeedAndFrequencyNative enter, newRssi= %{public}d TxLinkspeed=  %{public}d freq= %{public}d RxLinkSpeed= %{public}d", new Object[]{Integer.valueOf(newRssi), Integer.valueOf(newTxLinkSpeed), Integer.valueOf(newFrequency), Integer.valueOf(newRxLinkSpeed)});
            if (newRssi <= -127 || newRssi >= 200) {
                this.mWifiInfo.setRssi(-127);
                updateCapabilities();
                HwHiLog.e(TAG, false, "fetchRssi return invalid RSSI = %{public}d", new Object[]{Integer.valueOf(newRssi)});
            } else {
                if (newRssi > 0) {
                    HwHiLog.e(TAG, false, "get invalid value RSSI: %{public}d", new Object[]{Integer.valueOf(newRssi)});
                    newRssi -= 256;
                }
                this.mWifiInfo.setRssi(newRssi);
                HwHiLog.i(TAG, false, "set new RSSI = %{public}d", new Object[]{Integer.valueOf(newRssi)});
                int newSignalLevel = WifiManager.calculateSignalLevel(newRssi, DHCP_RESULT_CACHE_SIZE);
                if (newSignalLevel != this.mLastSignalLevel) {
                    this.mLastSignalLevel = newSignalLevel;
                    updateCapabilities();
                    sendRssiChangeBroadcast(newRssi);
                    HwHiLog.i(TAG, false, "newSignalLevel = %{public}d", new Object[]{Integer.valueOf(newSignalLevel)});
                }
            }
            if (newTxLinkSpeed > 0) {
                this.mWifiInfo.setLinkSpeed(newTxLinkSpeed);
                this.mWifiInfo.setTxLinkSpeedMbps(newTxLinkSpeed);
            }
            if (newRxLinkSpeed > 0) {
                this.mWifiInfo.setRxLinkSpeedMbps(newRxLinkSpeed);
            }
            if (newFrequency > 0) {
                this.mWifiInfo.setFrequency(newFrequency);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanWifiScore() {
        HwHiLog.i(TAG, false, "cleanWifiScore enter", new Object[0]);
        HwExtendedWifi2Info hwExtendedWifi2Info = this.mWifiInfo;
        hwExtendedWifi2Info.txBadRate = 0.0d;
        hwExtendedWifi2Info.txSuccessRate = 0.0d;
        hwExtendedWifi2Info.txRetriesRate = 0.0d;
        hwExtendedWifi2Info.rxSuccessRate = 0.0d;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLinkProperties(LinkProperties newLp) {
        HwHiLog.i(TAG, false, "updateLinkProperties Enter ", new Object[0]);
        this.mLinkProperties = newLp;
        HwWifi2NetworkAgent hwWifi2NetworkAgent = this.mNetworkAgent;
        if (hwWifi2NetworkAgent != null) {
            hwWifi2NetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
        if (getNetworkDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            sendLinkConfigurationChangedBroadcast();
        }
    }

    private void clearLinkProperties() {
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.clear();
            }
        }
        this.mLinkProperties.clear();
        HwWifi2NetworkAgent hwWifi2NetworkAgent = this.mNetworkAgent;
        if (hwWifi2NetworkAgent != null) {
            hwWifi2NetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
    }

    private void sendLinkConfigurationChangedBroadcast() {
        HwHiLog.i(TAG, false, "sendLinkConfigurationChangedBroadcast enter", new Object[0]);
        Intent intent = new Intent("huawei.net.slave_wifi.LINK_CONFIGURATION_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("linkProperties", new LinkProperties(this.mLinkProperties));
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendRssiChangeBroadcast(int newRssi) {
        HwHiLog.i(TAG, false, "sendRssiChangeBroadcast enter , newRssi = %{public}d", new Object[]{Integer.valueOf(newRssi)});
        Intent intent = new Intent("huawei.net.slave_wifi.RSSI_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("newRssi", newRssi);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.ACCESS_WIFI_STATE");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNetworkStateChangeBroadcast(String bssid) {
        Intent intent = new Intent("huawei.net.slave_wifi.STATE_CHANGED");
        intent.addFlags(67108864);
        NetworkInfo networkInfo = new NetworkInfo(this.mNetworkInfo);
        networkInfo.setExtraInfo(null);
        intent.putExtra("networkInfo", networkInfo);
        intent.putExtra(HwWifi2ClientModeImplConst.WIFI2_ICON_INFO, getWifi2IconInfo());
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private int getWifi2IconInfo() {
        if (this.mNetworkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED) {
            return 0;
        }
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null) {
            HwHiLog.e(TAG, false, "getWifi2IconInfo wifiManager is null", new Object[0]);
            return 0;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            HwHiLog.e(TAG, false, "getWifi2IconInfo wifiInfo is null", new Object[0]);
            return 0;
        } else if (wifiInfo.getMeteredHint()) {
            HwHiLog.i(TAG, false, "getWifi2IconInfo wifi1 is MeteredHint return WIFI2_NONE_ICON", new Object[0]);
            return 0;
        } else {
            HwHiLog.i(TAG, false, "getWifi2IconInfo wifi1 cate : %{public}d", new Object[]{Integer.valueOf(wifiInfo.getSupportedWifiCategory())});
            int supportedWifiCategory = wifiInfo.getSupportedWifiCategory();
            if (supportedWifiCategory == 1) {
                return 1;
            }
            if (supportedWifiCategory == 2) {
                return 2;
            }
            if (supportedWifiCategory != 3) {
                return 0;
            }
            return 3;
        }
    }

    public void handleWifi1CateChange() {
        NetworkInfo networkInfo = this.mNetworkInfo;
        if (networkInfo == null) {
            HwHiLog.e(TAG, false, "handleWifi1CateChange, mNetworkInfo is null", new Object[0]);
        } else if (networkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED) {
            HwHiLog.e(TAG, false, "handleWifi1CateChange, wifi2 is not connected drop it", new Object[0]);
        } else {
            sendNetworkStateChangeBroadcast(this.mLastBssid);
        }
    }

    private void sendSupplicantConnectionChangedBroadcast(boolean connected) {
        HwHiLog.i(TAG, false, "sendSupplicantConnectionChangedBroadcast enter", new Object[0]);
        Intent intent = new Intent("huawei.net.slave_wifi.supplicant.CONNECTION_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("connected", connected);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setNetworkDetailedState(NetworkInfo.DetailedState state) {
        if (this.mIsAutoRoaming || state == this.mNetworkInfo.getDetailedState()) {
            return false;
        }
        this.mNetworkInfo.setDetailedState(state, null, null);
        HwWifi2NetworkAgent hwWifi2NetworkAgent = this.mNetworkAgent;
        if (hwWifi2NetworkAgent != null) {
            hwWifi2NetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        sendNetworkStateChangeBroadcast(null);
        HwHiLog.i(TAG, false, "setNetworkDetailedState success, state = %{public}s", new Object[]{String.valueOf(state)});
        return true;
    }

    private NetworkInfo.DetailedState getNetworkDetailedState() {
        HwHiLog.i(TAG, false, "getNetworkDetailedState enter", new Object[0]);
        return this.mNetworkInfo.getDetailedState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SupplicantState handleSupplicantStateChange(Message message) {
        HwHiLog.i(TAG, false, "handleSupplicantStateChange enter", new Object[0]);
        StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
        SupplicantState state = stateChangeResult.state;
        this.mWifiInfo.setSupplicantState(state);
        if (SupplicantState.isConnecting(state)) {
            this.mWifiInfo.setNetworkId(stateChangeResult.networkId);
            this.mWifiInfo.setBSSID(stateChangeResult.bssid);
            this.mWifiInfo.setSSID(stateChangeResult.wifiSsid);
        } else {
            this.mWifiInfo.setNetworkId(-1);
            this.mWifiInfo.setBSSID(null);
            this.mWifiInfo.setSSID(null);
        }
        updateCapabilities();
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            this.mWifiInfo.setEphemeral(config.ephemeral);
            this.mWifiInfo.setTrusted(config.trusted);
            if (config.fromWifiNetworkSpecifier || config.fromWifiNetworkSuggestion) {
                this.mWifiInfo.setNetworkSuggestionOrSpecifierPackageName(config.creatorName);
            }
            if (config.getNetworkSelectionStatus().getCandidate() != null) {
                this.mWifiInfo.setFrequency(config.getNetworkSelectionStatus().getCandidate().frequency);
            }
        }
        this.mSupplicantStateTracker.sendMessage(Message.obtain(message));
        return state;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkDisconnect() {
        HwHiLog.i(TAG, false, "Slave wifi handleNetworkDisconnect Enter", new Object[0]);
        getCurrentWifiConfiguration();
        stopRssiMonitoringOffload();
        clearTargetBssid("handleNetworkDisconnect");
        stopIpClient();
        this.mWifiInfo.reset();
        this.mIsAutoRoaming = false;
        setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
        synchronized (this.mNetworkAgentLock) {
            if (this.mNetworkAgent != null) {
                this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
                this.mNetworkAgent = null;
            }
        }
        clearLinkProperties();
        sendNetworkStateChangeBroadcast(this.mLastBssid);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePreDhcpSetup() {
        if (!this.mIsBluetoothConnectionActive) {
            this.mWifiNative.setBluetoothCoexistenceMode(this.mInterfaceName, 1);
        }
        HwHiLog.i(TAG, false, "handlePreDhcpSetup, setBluetoothCoexistenceMode to disable", new Object[0]);
        setSuspendOptimizationsNative(1, false);
        setPowerSave(false);
        getWifiLinkLayerStats();
        sendMessage(HwWifi2ClientModeImplConst.CMD_PRE_DHCP_ACTION_COMPLETE);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePostDhcpSetup() {
        HwHiLog.i(TAG, false, "handlePostDhcpSteup, setBluetoothCoexistenceMode", new Object[0]);
        setSuspendOptimizationsNative(1, true);
        setPowerSave(true);
        this.mWifiNative.setBluetoothCoexistenceMode(this.mInterfaceName, 2);
    }

    public boolean setPowerSave(boolean isPowerSave) {
        String str = this.mInterfaceName;
        if (str != null) {
            HwHiLog.i(TAG, false, "Setting power save for: %{public}s to: %{public}s", new Object[]{str, String.valueOf(isPowerSave)});
            this.mWifiNative.setPowerSave(this.mInterfaceName, isPowerSave);
            return true;
        }
        HwHiLog.e(TAG, false, "Failed to setPowerSave, interfaceName is null", new Object[0]);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIpv4Success(DhcpResults dhcpResults) {
        Inet4Address addr;
        if (dhcpResults.ipAddress == null) {
            HwHiLog.e(TAG, false, "handleIPv4Success dhcpResults.ipAddress == null", new Object[0]);
            return;
        }
        synchronized (this.mDhcpResultsLock) {
            this.mDhcpResults = dhcpResults;
            InetAddress address = dhcpResults.ipAddress.getAddress();
            if (address instanceof Inet4Address) {
                addr = (Inet4Address) address;
            } else {
                return;
            }
        }
        if (this.mIsAutoRoaming && this.mWifiInfo.getIpAddress() != NetworkUtils.inetAddressToInt(addr)) {
            HwHiLog.i(TAG, false, "handleIpv4Success, roaming and address changed", new Object[0]);
        }
        this.mWifiInfo.setInetAddress(addr);
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            this.mWifiInfo.setEphemeral(config.ephemeral);
            this.mWifiInfo.setTrusted(config.trusted);
        }
        if (dhcpResults.hasMeteredHint()) {
            this.mWifiInfo.setMeteredHint(true);
        }
        updateCapabilities(config);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSuccessfulIpConfiguration() {
        this.mLastSignalLevel = -1;
        WifiConfiguration wifiConfig = getCurrentWifiConfiguration();
        if (wifiConfig != null) {
            wifiConfig.getNetworkSelectionStatus().clearDisableReasonCounter(4);
            updateCapabilities(wifiConfig);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIpv4Failure() {
        int count = -1;
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            count = config.getNetworkSelectionStatus().getDisableReasonCounter(4);
        }
        HwHiLog.i(TAG, false, "DHCP failure count = %{public}d", new Object[]{Integer.valueOf(count)});
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.clear();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIpConfigurationLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiNative.disconnect(this.mInterfaceName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerNetworkFactory() {
        if (this.mIsRegisterNetworkFactoryFlag) {
            HwHiLog.i(TAG, false, "registerNetworkFactory already done.", new Object[0]);
        } else if (checkAndSetConnectivityInstance()) {
            this.mNetworkFactory.register();
            this.mIsRegisterNetworkFactoryFlag = true;
            HwHiLog.i(TAG, false, "registerNetworkFactory finish.", new Object[0]);
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Enter DefaultState State", new Object[0]);
        }

        public void exit() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "exit DefaultState State", new Object[0]);
        }

        public boolean processMessage(Message message) {
            boolean z = false;
            int i = 1;
            switch (message.what) {
                case HwWifi2ClientModeImplConst.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /* 131103 */:
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                    if (message.arg1 != 0) {
                        z = true;
                    }
                    hwWifi2ClientModeImpl.mIsBluetoothConnectionActive = z;
                    return true;
                case HwWifi2ClientModeImplConst.CMD_SET_HIGH_PERF_MODE /* 131149 */:
                    setHighPrefMode(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_ENABLE_RSSI_POLL /* 131154 */:
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    hwWifi2ClientModeImpl2.mIsEnableRssiPolling = z;
                    return true;
                case HwWifi2ClientModeImplConst.CMD_SET_SUSPEND_OPT_ENABLED /* 131158 */:
                    setSuspendOptEnable(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_SCREEN_STATE_CHANGED /* 131167 */:
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl3 = HwWifi2ClientModeImpl.this;
                    if (message.arg1 != 0) {
                        z = true;
                    }
                    hwWifi2ClientModeImpl3.handleScreenStateChanged(z);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_BOOT_COMPLETED /* 131206 */:
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl4 = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl4.mNetworkFactory = hwWifi2ClientModeImpl4.mWifiInjector.makeHwWifi2NetworkFactory(HwWifi2ClientModeImpl.this.mNetworkCapabilitiesFilter);
                    HwWifi2ClientModeImpl.this.registerNetworkFactory();
                    return true;
                case HwWifi2ClientModeImplConst.CMD_INITIALIZE /* 131207 */:
                    boolean isInitializeOk = HwWifi2ClientModeImpl.this.mWifiNative.initialize();
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl5 = HwWifi2ClientModeImpl.this;
                    int i2 = message.what;
                    if (!isInitializeOk) {
                        i = -1;
                    }
                    hwWifi2ClientModeImpl5.replyToMessage(message, i2, i);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_UPDATE_LINKPROPERTIES /* 131212 */:
                    HwWifi2ClientModeImpl.this.updateLinkProperties((LinkProperties) message.obj);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_START_IP_PACKET_OFFLOAD /* 131232 */:
                case HwWifi2ClientModeImplConst.CMD_STOP_IP_PACKET_OFFLOAD /* 131233 */:
                case HwWifi2ClientModeImplConst.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF /* 131281 */:
                case HwWifi2ClientModeImplConst.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF /* 131282 */:
                    if (HwWifi2ClientModeImpl.this.mNetworkAgent == null) {
                        return true;
                    }
                    HwWifi2ClientModeImpl.this.mNetworkAgent.onSocketKeepaliveEvent(message.arg1, -20);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_INSTALL_PACKET_FILTER /* 131274 */:
                    HwWifi2ClientModeImpl.this.mWifiNative.installPacketFilter(HwWifi2ClientModeImpl.this.mInterfaceName, (byte[]) message.obj);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_SET_FALLBACK_PACKET_FILTERING /* 131275 */:
                    setFallbackPacketFilting(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_READ_PACKET_FILTER /* 131280 */:
                    if (HwWifi2ClientModeImpl.this.mIpClient == null) {
                        return true;
                    }
                    HwWifi2ClientModeImpl.this.mIpClient.readPacketFilterComplete(HwWifi2ClientModeImpl.this.mWifiNative.readPacketFilter(HwWifi2ClientModeImpl.this.mInterfaceName));
                    return true;
                case HwWifi2ClientModeImplConst.CMD_START_SUBSCRIPTION_PROVISIONING /* 131326 */:
                    HwWifi2ClientModeImpl.this.replyToMessage(message, message.what, 0);
                    return true;
                case 151553:
                    HwWifi2ClientModeImpl.this.replyToMessage(message, 151554, 2);
                    return true;
                default:
                    return false;
            }
        }

        private void setHighPrefMode(Message message) {
            HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
            boolean z = true;
            if (message.arg1 == 1) {
                z = false;
            }
            hwWifi2ClientModeImpl.setSuspendOptimizations(2, z);
        }

        private void setSuspendOptEnable(Message message) {
            if (message.arg1 == 1) {
                if (message.arg2 == 1) {
                    HwWifi2ClientModeImpl.this.mSuspendWakeLock.release();
                }
                HwWifi2ClientModeImpl.this.setSuspendOptimizations(4, true);
                return;
            }
            HwWifi2ClientModeImpl.this.setSuspendOptimizations(4, false);
        }

        private void setFallbackPacketFilting(Message message) {
            if (!(message.obj instanceof Boolean)) {
                return;
            }
            if (((Boolean) message.obj).booleanValue()) {
                HwWifi2ClientModeImpl.this.mWifiNative.startFilteringMulticastV4Packets(HwWifi2ClientModeImpl.this.mInterfaceName);
            } else {
                HwWifi2ClientModeImpl.this.mWifiNative.stopFilteringMulticastV4Packets(HwWifi2ClientModeImpl.this.mInterfaceName);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setupClientMode() {
        HwHiLog.i(TAG, false, "setupClientMode() ifacename = %{public}s", new Object[]{this.mInterfaceName});
        setHighPerfModeEnabled(false);
        this.mIpClientCallbacks = new IpClientCallbacksImpl();
        IpClientUtil.makeIpClient(this.mContext, this.mInterfaceName, this.mIpClientCallbacks);
        if (!this.mIpClientCallbacks.awaitCreation()) {
            HwHiLog.e(TAG, false, "Timeout waiting for IpClient", new Object[0]);
        }
        setMulticastFilter(true);
        registerForWifiMonitorEvents();
        this.mSupplicantStateTracker.sendMessage(HwWifi2ClientModeImplConst.CMD_RESET_SUPPLICANT_STATE);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mWifiInfo.setMacAddress(this.mWifiNative.getMacAddress(this.mInterfaceName));
        sendSupplicantConnectionChangedBroadcast(true);
        this.mWifiNative.setExternalSim(this.mInterfaceName, true);
        setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
        this.mWifiNative.stopFilteringMulticastV4Packets(this.mInterfaceName);
        this.mWifiNative.stopFilteringMulticastV6Packets(this.mInterfaceName);
        this.mWifiNative.setConcurrencyPriority(true);
        this.mWifiNative.enableStaAutoReconnect(this.mInterfaceName, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopClientMode() {
        if (this.mIpClient != null && this.mIpClient.shutdown()) {
            this.mIpClientCallbacks.awaitShutdown();
        }
        this.mNetworkInfo.setIsAvailable(false);
        HwWifi2NetworkAgent hwWifi2NetworkAgent = this.mNetworkAgent;
        if (hwWifi2NetworkAgent != null) {
            hwWifi2NetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        this.mInterfaceName = null;
        sendSupplicantConnectionChangedBroadcast(false);
    }

    public WifiConfiguration getCurrentWifiConfiguration() {
        return this.mWifiConfiguration;
    }

    private WifiConfiguration getTargetWifiConfiguration() {
        return this.mWifiConfiguration;
    }

    private String getCurrentBssid() {
        return this.mLastBssid;
    }

    class ConnectModeState extends State {
        ConnectModeState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Enter ConnectModeState: ifaceName = %{public}s", new Object[]{HwWifi2ClientModeImpl.this.mInterfaceName});
            HwWifi2ClientModeImpl.this.setupClientMode();
            if (!HwWifi2ClientModeImpl.this.mWifiNative.removeAllNetworks(HwWifi2ClientModeImpl.this.mInterfaceName)) {
                HwHiLog.e(HwWifi2ClientModeImpl.TAG, false, "Failed to remove networks on entering connect mode", new Object[0]);
            }
            HwWifi2ClientModeImpl.this.mWifiInfo.reset();
            HwWifi2ClientModeImpl.this.mWifiInfo.score = HwWifi2ClientModeImpl.SLAVE_WIFI_TRANSITION_SCORE;
            HwWifi2ClientModeImpl.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            HwWifi2ClientModeImpl.this.mNetworkInfo.setIsAvailable(true);
            if (HwWifi2ClientModeImpl.this.mNetworkAgent != null) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "ConnectModeState start sendNetworkInfo", new Object[0]);
                HwWifi2ClientModeImpl.this.mNetworkAgent.sendNetworkInfo(HwWifi2ClientModeImpl.this.mNetworkInfo);
            }
            HwWifi2ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
            HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager.handleWifi2Up(true);
        }

        public void exit() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Exit ConnectMode State", new Object[0]);
            HwWifi2ClientModeImpl.this.mNetworkInfo.setIsAvailable(false);
            if (HwWifi2ClientModeImpl.this.mNetworkAgent != null) {
                HwWifi2ClientModeImpl.this.mNetworkAgent.sendNetworkInfo(HwWifi2ClientModeImpl.this.mNetworkInfo);
            }
            HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager.handleWifi2Up(false);
            if (!HwWifi2ClientModeImpl.this.mWifiNative.removeAllNetworks(HwWifi2ClientModeImpl.this.mInterfaceName)) {
                HwHiLog.e(HwWifi2ClientModeImpl.TAG, false, "Failed to remove networks on exiting connect mode", new Object[0]);
            }
            HwWifi2ClientModeImpl.this.mWifiInfo.reset();
            HwWifi2ClientModeImpl.this.mWifiInfo.score = HwWifi2ClientModeImpl.SLAVE_WIFI_TRANSITION_SCORE;
            HwWifi2ClientModeImpl.this.mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            HwWifi2ClientModeImpl.this.stopClientMode();
        }

        public boolean processMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case HwWifi2ClientModeImplConst.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /* 131103 */:
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    hwWifi2ClientModeImpl.mIsBluetoothConnectionActive = z;
                    HwWifi2ClientModeImpl.this.mWifiNative.setBluetoothCoexistenceScanMode(HwWifi2ClientModeImpl.this.mInterfaceName, HwWifi2ClientModeImpl.this.mIsBluetoothConnectionActive);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_SET_HIGH_PERF_MODE /* 131149 */:
                    setHighPrefModeInConnectMode(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_SET_SUSPEND_OPT_ENABLED /* 131158 */:
                    setSuppendOptEnable(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_TARGET_BSSID /* 131213 */:
                    if (message.obj == null) {
                        return true;
                    }
                    HwWifi2ClientModeImpl.this.mTargetRoamBssid = (String) message.obj;
                    return true;
                case HwWifi2ClientModeImplConst.CMD_START_CONNECT /* 131215 */:
                    startConnectCmdInConnectMode(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_ASSOCIATED_BSSID /* 131219 */:
                    String str = (String) message.obj;
                    return false;
                case HwWifi2ClientModeImplConst.CMD_STOP_IP_PACKET_OFFLOAD /* 131233 */:
                    stopIpPacketOffload(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_CONFIG_ND_OFFLOAD /* 131276 */:
                    HwWifi2Native hwWifi2Native = HwWifi2ClientModeImpl.this.mWifiNative;
                    String str2 = HwWifi2ClientModeImpl.this.mInterfaceName;
                    if (message.arg1 <= 0) {
                        z = false;
                    }
                    hwWifi2Native.configureNeighborDiscoveryOffload(str2, z);
                    return true;
                case HwWifi2Monitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    networkConnectionEventInConnectMode(message);
                    return true;
                case HwWifi2Monitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, " ConnectModeState recv NETWORK_DISCONNECTION_EVENT", new Object[0]);
                    HwWifi2ClientModeImpl.this.handleNetworkDisconnect();
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl2.transitionTo(hwWifi2ClientModeImpl2.mDisconnectedState);
                    return true;
                case HwWifi2Monitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    supplicantStateChange(message);
                    return true;
                case HwWifi2Monitor.AUTHENTICATION_FAILURE_EVENT /* 147463 */:
                    HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager.handleConnectFail();
                    HwWifi2ClientModeImpl.this.mSupplicantStateTracker.sendMessage(HwWifi2Monitor.AUTHENTICATION_FAILURE_EVENT);
                    return true;
                case HwWifi2Monitor.ASSOCIATION_REJECTION_EVENT /* 147499 */:
                    HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager.handleConnectFail();
                    associateReject(message);
                    return true;
                case 151553:
                    connectToNetwork(message);
                    return true;
                default:
                    return false;
            }
        }

        private void connectToNetwork(Message message) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "ConnectModeState recv CONNECT_NETWORK", new Object[0]);
            int i = message.arg1;
            HwWifi2ClientModeImpl.this.mWifiConfiguration = (WifiConfiguration) message.obj;
            HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
            if (!hwWifi2ClientModeImpl.connectToUserSelectNetwork(hwWifi2ClientModeImpl.mWifiConfiguration.networkId, HwWifi2ClientModeImpl.this.mWifiConfiguration.creatorUid, false)) {
                HwWifi2ClientModeImpl.this.replyToMessage(message, 151554, 9);
            } else {
                HwWifi2ClientModeImpl.this.replyToMessage(message, 151555);
            }
        }

        private void supplicantStateChange(Message message) {
            SupplicantState state = HwWifi2ClientModeImpl.this.handleSupplicantStateChange(message);
            if (state == SupplicantState.DISCONNECTED && HwWifi2ClientModeImpl.this.mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Missed CTRL-EVENT-DISCONNECTED, disconnect", new Object[0]);
                HwWifi2ClientModeImpl.this.handleNetworkDisconnect();
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mDisconnectedState);
            }
            if (state == SupplicantState.COMPLETED && HwWifi2ClientModeImpl.this.mIpClient != null) {
                HwWifi2ClientModeImpl.this.mIpClient.confirmConfiguration();
            }
        }

        private void associateReject(Message message) {
            String bssid = (String) message.obj;
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Associat Reject event: bssid= %{public}s reason code= %{public}d isTimedOut= %{public}s", new Object[]{StringUtilEx.safeDisplayBssid(bssid), Integer.valueOf(message.arg2), String.valueOf(message.arg1 > 0)});
            if (TextUtils.isEmpty(bssid)) {
                String bssid2 = HwWifi2ClientModeImpl.this.mTargetRoamBssid;
            }
            HwWifi2ClientModeImpl.this.mSupplicantStateTracker.sendMessage(HwWifi2Monitor.ASSOCIATION_REJECTION_EVENT);
        }

        private void networkConnectionEventInConnectMode(Message message) {
            HwWifi2ClientModeImpl.this.mLastNetworkId = message.arg1;
            HwWifi2ClientModeImpl.this.mLastBssid = (String) message.obj;
            WifiConfiguration config = HwWifi2ClientModeImpl.this.getCurrentWifiConfiguration();
            if (config != null) {
                HwWifi2ClientModeImpl.this.mWifiInfo.setBSSID(config.BSSID);
                HwWifi2ClientModeImpl.this.mWifiInfo.setNetworkId(config.networkId);
                HwWifi2ClientModeImpl.this.mWifiInfo.setMacAddress(HwWifi2ClientModeImpl.this.mWifiNative.getMacAddress(HwWifi2ClientModeImpl.this.mInterfaceName));
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "setBSSID = %{public}s, setNetworkId = %{public}d", new Object[]{StringUtilEx.safeDisplayBssid(config.BSSID), Integer.valueOf(config.networkId)});
                HwWifi2ClientModeImpl.this.sendNetworkStateChangeBroadcast(config.BSSID);
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mObtainingIpState);
                return;
            }
            HwHiLog.e(HwWifi2ClientModeImpl.TAG, false, "Connected to unknown networkId %{public}d", new Object[]{Integer.valueOf(HwWifi2ClientModeImpl.this.mLastNetworkId)});
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_DISCONNECT);
        }

        private void setHighPrefModeInConnectMode(Message message) {
            if (message.arg1 == 1) {
                HwWifi2ClientModeImpl.this.setSuspendOptimizationsNative(2, false);
            } else {
                HwWifi2ClientModeImpl.this.setSuspendOptimizationsNative(2, true);
            }
        }

        private void setSuppendOptEnable(Message message) {
            if (message.arg1 == 1) {
                HwWifi2ClientModeImpl.this.setSuspendOptimizationsNative(4, true);
                if (message.arg2 == 1) {
                    HwWifi2ClientModeImpl.this.mSuspendWakeLock.release();
                    return;
                }
                return;
            }
            HwWifi2ClientModeImpl.this.setSuspendOptimizationsNative(4, false);
        }

        private void stopIpPacketOffload(Message message) {
            int slot = message.arg1;
            int ret = HwWifi2ClientModeImpl.this.stopWifiIpPacketOffload(slot);
            if (HwWifi2ClientModeImpl.this.mNetworkAgent != null) {
                HwWifi2ClientModeImpl.this.mNetworkAgent.onSocketKeepaliveEvent(slot, ret);
            }
        }

        private void startConnectCmdInConnectMode(Message message) {
            int i = message.arg1;
            int i2 = message.arg2;
            String str = (String) message.obj;
            WifiConfiguration config = HwWifi2ClientModeImpl.this.mWifiConfiguration;
            if (config == null) {
                HwHiLog.e(HwWifi2ClientModeImpl.TAG, false, "CMD_START_CONNECT and no config", new Object[0]);
                return;
            }
            HwWifi2ClientModeImpl.this.mWifiInfo.setMacAddress(HwWifi2ClientModeImpl.this.mWifiNative.getMacAddress(HwWifi2ClientModeImpl.this.mInterfaceName));
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Connecting to networkId = %{public}s", new Object[]{Integer.valueOf(config.networkId)});
            if (HwWifi2ClientModeImpl.this.mWifiNative.connectToNetwork(HwWifi2ClientModeImpl.this.mInterfaceName, config)) {
                HwWifi2ClientModeImpl.this.mIsAutoRoaming = false;
                if (HwWifi2ClientModeImpl.this.getCurrentState() != HwWifi2ClientModeImpl.this.mDisconnectedState) {
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mDisconnectingState);
                    return;
                }
                return;
            }
            HwHiLog.e(HwWifi2ClientModeImpl.TAG, false, "Failed to connection to network", new Object[0]);
            HwWifi2ClientModeImpl.this.replyToMessage(message, 151554, 0);
        }
    }

    private WifiNetworkAgentSpecifier createNetworkAgentSpecifier(WifiConfiguration currentWifiConfiguration, String currentBssid, int specificRequestUid, String specificRequestPackageName) {
        currentWifiConfiguration.BSSID = currentBssid;
        return new WifiNetworkAgentSpecifier(currentWifiConfiguration, specificRequestUid, specificRequestPackageName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NetworkCapabilities getCapabilities(WifiConfiguration currentWifiConfiguration) {
        NetworkCapabilities result = new NetworkCapabilities(this.mNetworkCapabilitiesFilter);
        result.setNetworkSpecifier(null);
        if (currentWifiConfiguration == null) {
            HwHiLog.i(TAG, false, "currentWifiConfiguration == null, just return", new Object[0]);
            return result;
        }
        if (!this.mWifiInfo.isTrusted()) {
            HwHiLog.i(TAG, false, "start remove NET_CAPABILITY_TRUSTED Capability", new Object[0]);
            result.removeCapability(14);
        } else {
            result.addCapability(14);
        }
        if (!WifiConfiguration.isMetered(currentWifiConfiguration, this.mWifiInfo)) {
            result.addCapability(11);
        } else {
            result.removeCapability(11);
        }
        if (this.mWifiInfo.getRssi() != -127) {
            result.setSignalStrength(this.mWifiInfo.getRssi());
        } else {
            HwHiLog.i(TAG, false, "getCapabilities: getRssi() is INVALID_RSSI", new Object[0]);
            result.setSignalStrength(Integer.MIN_VALUE);
        }
        if (!"<unknown ssid>".equals(this.mWifiInfo.getSSID())) {
            result.setSSID(this.mWifiInfo.getSSID());
        } else {
            result.setSSID(null);
        }
        Pair<Integer, String> specificRequestUidAndPackageName = this.mNetworkFactory.getSpecificNetworkRequestUidAndPackageName();
        if (((Integer) specificRequestUidAndPackageName.first).intValue() != -1) {
            HwHiLog.i(TAG, false, "getCapabilities: remove internet Capability", new Object[0]);
            result.removeCapability(12);
        }
        result.setNetworkSpecifier(createNetworkAgentSpecifier(currentWifiConfiguration, getCurrentBssid(), ((Integer) specificRequestUidAndPackageName.first).intValue(), (String) specificRequestUidAndPackageName.second));
        return result;
    }

    public void updateCapabilities() {
        updateCapabilities(getCurrentWifiConfiguration());
    }

    private void updateCapabilities(WifiConfiguration currentWifiConfiguration) {
        HwHiLog.i(TAG, false, "Slave wifi update Capabilities enter", new Object[0]);
        HwWifi2NetworkAgent hwWifi2NetworkAgent = this.mNetworkAgent;
        if (hwWifi2NetworkAgent == null) {
            HwHiLog.e(TAG, false, "update Capabilities mNetworkAgent is null", new Object[0]);
        } else {
            hwWifi2NetworkAgent.sendNetworkCapabilities(getCapabilities(currentWifiConfiguration));
        }
    }

    /* access modifiers changed from: private */
    public class HwWifi2NetworkAgent extends NetworkAgent {
        private int mLastNetworkStatus = -1;

        HwWifi2NetworkAgent(Looper loop, Context context, String tag, NetworkInfo info, NetworkCapabilities capability, LinkProperties linkPro, int score, NetworkMisc misc) {
            super(loop, context, tag, info, capability, linkPro, score, misc);
        }

        /* access modifiers changed from: protected */
        public void unwanted() {
            if (this == HwWifi2ClientModeImpl.this.mNetworkAgent) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "HwWifi2NetworkAgent unwanted score %{public}d", new Object[]{Integer.valueOf(HwWifi2ClientModeImpl.this.mWifiInfo.score)});
                HwWifi2ClientModeImpl.this.unwantedNetwork(0);
            }
        }

        /* access modifiers changed from: protected */
        public void networkStatus(int status, String redirectUrl) {
            if (this == HwWifi2ClientModeImpl.this.mNetworkAgent && status != this.mLastNetworkStatus) {
                this.mLastNetworkStatus = status;
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "recv Wifi network Status = %{public}d", new Object[]{Integer.valueOf(status)});
                if (status == 2) {
                    HwWifi2ClientModeImpl.this.unwantedNetwork(1);
                }
                if (status == 1) {
                    HwWifi2ClientModeImpl.this.doNetworkStatus(status);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void saveAcceptUnvalidated(boolean isAccept) {
            if (this == HwWifi2ClientModeImpl.this.mNetworkAgent) {
                HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_ACCEPT_UNVALIDATED, isAccept ? 1 : 0);
            }
        }

        /* access modifiers changed from: protected */
        public void startSocketKeepalive(Message msg) {
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_START_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void stopSocketKeepalive(Message msg) {
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_STOP_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void addKeepalivePacketFilter(Message msg) {
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void removeKeepalivePacketFilter(Message msg) {
            HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF, msg.arg1, msg.arg2, msg.obj);
        }

        /* access modifiers changed from: protected */
        public void setSignalStrengthThresholds(int[] thresholds) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Received signal strength thresholds: %{public}s", new Object[]{Arrays.toString(thresholds)});
            if (thresholds.length == 0) {
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.sendMessage(HwWifi2ClientModeImplConst.CMD_STOP_RSSI_MONITORING_OFFLOAD, hwWifi2ClientModeImpl.mWifiInfo.getRssi());
                return;
            }
            int[] rssiValues = Arrays.copyOf(thresholds, thresholds.length + 2);
            rssiValues[rssiValues.length - 2] = -128;
            rssiValues[rssiValues.length - 1] = 127;
            Arrays.sort(rssiValues);
            byte[] rssiRange = new byte[rssiValues.length];
            for (int i = 0; i < rssiValues.length; i++) {
                int val = rssiValues[i];
                if (val > 127 || val < -128) {
                    HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Illegal value %{public}d for RSSI thresholds: %{public}s", new Object[]{Integer.valueOf(val), Arrays.toString(rssiValues)});
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl2.sendMessage(HwWifi2ClientModeImplConst.CMD_STOP_RSSI_MONITORING_OFFLOAD, hwWifi2ClientModeImpl2.mWifiInfo.getRssi());
                    return;
                }
                rssiRange[i] = (byte) val;
            }
            HwWifi2ClientModeImpl.this.mRssiRanges = rssiRange;
            HwWifi2ClientModeImpl hwWifi2ClientModeImpl3 = HwWifi2ClientModeImpl.this;
            hwWifi2ClientModeImpl3.sendMessage(HwWifi2ClientModeImplConst.CMD_START_RSSI_MONITORING_OFFLOAD, hwWifi2ClientModeImpl3.mWifiInfo.getRssi());
        }

        /* access modifiers changed from: protected */
        public void preventAutomaticReconnect() {
            if (this == HwWifi2ClientModeImpl.this.mNetworkAgent) {
                HwWifi2ClientModeImpl.this.unwantedNetwork(2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unwantedNetwork(int reason) {
        HwHiLog.i(TAG, false, "unwantedNetwork enter", new Object[0]);
        sendMessage(HwWifi2ClientModeImplConst.CMD_UNWANTED_NETWORK, reason);
    }

    /* access modifiers changed from: package-private */
    public void doNetworkStatus(int status) {
        HwHiLog.i(TAG, false, "doNetworkStatus enter", new Object[0]);
        sendMessage(HwWifi2ClientModeImplConst.CMD_NETWORK_STATUS, status);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hwSetArpIgnore(int value) {
        HwHiLog.i(TAG, false, "hwSetArpIgnore enabled = %{public}d", new Object[]{Integer.valueOf(value)});
        boolean status = false;
        IBinder binder = ServiceManager.getService("network_management");
        if (binder == null) {
            HwHiLog.e(TAG, false, "hwSetArpIgnore binder is null", new Object[0]);
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
            data.writeInt(value);
            data.writeString(IFACE_NAME_WLAN0_STR);
            data.writeString(IFACE_NAME_WLAN1_STR);
            data.writeString(IFACE_NAME_ALL_STR);
            status = binder.transact(CODE_SET_ARP_IGNORE_CMD, data, reply, 0);
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "hwSetArpIgnore RemoteException", new Object[0]);
        } catch (ServiceSpecificException e2) {
            HwHiLog.e(TAG, false, "hwSetArpIgnore serviceSpecificException", new Object[0]);
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return status;
    }

    class L2ConnectedState extends State {
        RssiEventHandler mRssiEventHandler = new RssiEventHandler();

        L2ConnectedState() {
        }

        class RssiEventHandler implements HwWifi2Native.WifiRssiEventHandler {
            RssiEventHandler() {
            }

            @Override // com.huawei.wifi2.HwWifi2Native.WifiRssiEventHandler
            public void onRssiThresholdBreached(byte curRssi) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "onRssiThresholdBreach event. Cur Rssi = %{public}d", new Object[]{Byte.valueOf(curRssi)});
                HwWifi2ClientModeImpl.this.sendMessage(HwWifi2ClientModeImplConst.CMD_RSSI_THRESHOLD_BREACHED, curRssi);
            }
        }

        public void enter() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Enter L2ConnectedState", new Object[0]);
            HwWifi2ClientModeImpl.access$4008(HwWifi2ClientModeImpl.this);
            if (HwWifi2ClientModeImpl.this.mIsEnableRssiPolling) {
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.sendMessage(HwWifi2ClientModeImplConst.CMD_RSSI_POLL, hwWifi2ClientModeImpl.mRssiPollToken, 0);
            }
            if (HwWifi2ClientModeImpl.this.mNetworkAgent != null) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Have NetworkAgent when entering L2Connected", new Object[0]);
                HwWifi2ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
            }
            HwWifi2ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTING);
            HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
            NetworkCapabilities nc = hwWifi2ClientModeImpl2.getCapabilities(hwWifi2ClientModeImpl2.getCurrentWifiConfiguration());
            synchronized (HwWifi2ClientModeImpl.this.mNetworkAgentLock) {
                HwWifi2ClientModeImpl.this.mNetworkAgent = new HwWifi2NetworkAgent(HwWifi2ClientModeImpl.this.getHandler().getLooper(), HwWifi2ClientModeImpl.this.mContext, "Wifi2NetworkAgent", HwWifi2ClientModeImpl.this.mNetworkInfo, nc, HwWifi2ClientModeImpl.this.mLinkProperties, HwWifi2ClientModeImpl.SLAVE_WIFI_TRANSITION_SCORE, HwWifi2ClientModeImpl.this.mNetworkMisc);
                if (HwWifi2ClientModeImpl.this.mNetworkAgent != null) {
                    HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "NetworkAgent create success", new Object[0]);
                } else {
                    HwHiLog.e(HwWifi2ClientModeImpl.TAG, false, "NetworkAgent create fail", new Object[0]);
                }
            }
            HwWifi2ClientModeImpl.this.clearTargetBssid("L2ConnectedState");
            if (!HwWifi2ClientModeImpl.this.hwSetArpIgnore(1)) {
                HwWifi2ClientModeImpl.this.disconnectCommand();
            }
        }

        public void exit() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Exit L2ConnectedState", new Object[0]);
            if (HwWifi2ClientModeImpl.this.mIpClient != null) {
                HwWifi2ClientModeImpl.this.mIpClient.stop();
            }
            if (HwWifi2ClientModeImpl.this.mLastBssid != null || HwWifi2ClientModeImpl.this.mLastNetworkId != -1) {
                HwWifi2ClientModeImpl.this.handleNetworkDisconnect();
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwWifi2ClientModeImplConst.CMD_DISCONNECT /* 131145 */:
                    HwWifi2ClientModeImpl.this.mWifiNative.disconnect(HwWifi2ClientModeImpl.this.mInterfaceName);
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mDisconnectingState);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_ENABLE_RSSI_POLL /* 131154 */:
                    enableRssiPollInL2Connect(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_RSSI_POLL /* 131155 */:
                    HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "L2ConnectedState recv message CMD_RSSI_POLL", new Object[0]);
                    configRssiPollInL2Connect(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_ONESHOT_RSSI_POLL /* 131156 */:
                    if (HwWifi2ClientModeImpl.this.mIsEnableRssiPolling) {
                        return true;
                    }
                    updateLinkLayerStatsRssiAndScoreReportInternal();
                    return true;
                case HwWifi2ClientModeImplConst.CMD_IP_CONFIGURATION_SUCCESSFUL /* 131210 */:
                    ipConfigurationSuccessInL2Connect();
                    return true;
                case HwWifi2ClientModeImplConst.CMD_IP_CONFIGURATION_LOST /* 131211 */:
                    HwWifi2ClientModeImpl.this.getWifiLinkLayerStats();
                    HwWifi2ClientModeImpl.this.handleIpConfigurationLost();
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl2.transitionTo(hwWifi2ClientModeImpl2.mDisconnectingState);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_ASSOCIATED_BSSID /* 131219 */:
                    associatedBssidInL2Connect(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_IP_REACHABILITY_LOST /* 131221 */:
                    ipReachableLostInL2Connect();
                    return true;
                case HwWifi2ClientModeImplConst.CMD_START_RSSI_MONITORING_OFFLOAD /* 131234 */:
                case HwWifi2ClientModeImplConst.CMD_RSSI_THRESHOLD_BREACHED /* 131236 */:
                    HwWifi2ClientModeImpl.this.processRssiThreshold((byte) message.arg1, message.what, this.mRssiEventHandler);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_STOP_RSSI_MONITORING_OFFLOAD /* 131235 */:
                    HwWifi2ClientModeImpl.this.stopRssiMonitoringOffload();
                    return true;
                case HwWifi2ClientModeImplConst.CMD_IPV4_PROVISIONING_SUCCESS /* 131272 */:
                    HwWifi2ClientModeImpl.this.handleIpv4Success((DhcpResults) message.obj);
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl3 = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl3.sendNetworkStateChangeBroadcast(hwWifi2ClientModeImpl3.mLastBssid);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_IPV4_PROVISIONING_FAILURE /* 131273 */:
                    HwWifi2ClientModeImpl.this.handleIpv4Failure();
                    return true;
                case HwWifi2ClientModeImplConst.CMD_PRE_DHCP_ACTION /* 131327 */:
                    HwWifi2ClientModeImpl.this.handlePreDhcpSetup();
                    if (HwWifi2ClientModeImpl.this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                        HwWifi2ChrManager.uploadWifi2DhcpState(0);
                    }
                    if (HwWifi2ClientModeImpl.this.mNetworkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED) {
                        return true;
                    }
                    HwWifi2ChrManager.uploadWifi2DhcpState(HwWifi2ClientModeImpl.SLAVE_WIFI_TRANSITION_SCORE);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_PRE_DHCP_ACTION_COMPLETE /* 131328 */:
                    if (HwWifi2ClientModeImpl.this.mIpClient == null) {
                        return true;
                    }
                    HwWifi2ClientModeImpl.this.mIpClient.completedPreDhcpAction();
                    return true;
                case HwWifi2ClientModeImplConst.CMD_POST_DHCP_ACTION /* 131329 */:
                    HwWifi2ClientModeImpl.this.handlePostDhcpSetup();
                    return true;
                case 131330:
                    tryCachedIpProcedure();
                    return true;
                case HwWifi2Monitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    networkConnectionEventInL2Connect(message);
                    return true;
                case 151553:
                    reveiveConnectNetworkCommandInL2Connect(message);
                    return false;
                default:
                    HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "L2ConnectedState recv NOT_HANDLED msg = %{public}s", new Object[]{HwWifi2ClientModeImplConst.messageNumToString(message.what)});
                    return false;
            }
        }

        private void tryCachedIpProcedure() {
            DhcpResults dhcpResults = HwWifi2ClientModeImpl.this.getCachedDhcpResultsForCurrentConfig();
            if (dhcpResults != null && HwWifi2ClientModeImpl.this.mIpClient != null) {
                HwWifi2ClientModeImpl.this.stopIpClient();
                dhcpResults.domains = HwWifi2ClientModeImpl.TYPE_GET_CACHE_DHCP_RESULT;
                HwWifi2ClientModeImpl.this.mIpClient.startProvisioning(new ProvisioningConfiguration.Builder().withStaticConfiguration(dhcpResults.toStaticIpConfiguration()).withoutIpReachabilityMonitor().build());
            }
        }

        private void ipConfigurationSuccessInL2Connect() {
            if (HwWifi2ClientModeImpl.this.getCurrentWifiConfiguration() == null) {
                HwWifi2ClientModeImpl.this.mWifiNative.disconnect(HwWifi2ClientModeImpl.this.mInterfaceName);
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mDisconnectingState);
                return;
            }
            HwWifi2ClientModeImpl.this.handleSuccessfulIpConfiguration();
            HwWifi2ClientModeImpl.this.sendConnectedState();
            HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
            hwWifi2ClientModeImpl2.transitionTo(hwWifi2ClientModeImpl2.mConnectedState);
        }

        private void ipReachableLostInL2Connect() {
            if (HwWifi2ClientModeImpl.this.mIsIpReachabilityDisconnectEnabled) {
                HwWifi2ClientModeImpl.this.handleIpConfigurationLost();
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mDisconnectingState);
                return;
            }
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "CMD_IP_REACHABILITY_LOST but disconnect disabled, ignore", new Object[0]);
        }

        private void reveiveConnectNetworkCommandInL2Connect(Message message) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "L2ConnectedState recv CONNECT_NETWORK", new Object[0]);
            if (HwWifi2ClientModeImpl.this.mWifiInfo.getNetworkId() == message.arg1) {
                HwWifi2ClientModeImpl.this.replyToMessage(message, 151555);
            }
        }

        private void networkConnectionEventInL2Connect(Message message) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "L2ConnectedState recv NETWORK_CONNECTION_EVENT", new Object[0]);
            if (message.obj instanceof String) {
                HwWifi2ClientModeImpl.this.mWifiInfo.setBSSID((String) message.obj);
                HwWifi2ClientModeImpl.this.mLastNetworkId = message.arg1;
                HwWifi2ClientModeImpl.this.mWifiInfo.setNetworkId(HwWifi2ClientModeImpl.this.mLastNetworkId);
                HwWifi2ClientModeImpl.this.mWifiInfo.setMacAddress(HwWifi2ClientModeImpl.this.mWifiNative.getMacAddress(HwWifi2ClientModeImpl.this.mInterfaceName));
                if (!HwWifi2ClientModeImpl.this.mLastBssid.equals(message.obj)) {
                    HwWifi2ClientModeImpl.this.mLastBssid = (String) message.obj;
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl.sendNetworkStateChangeBroadcast(hwWifi2ClientModeImpl.mLastBssid);
                }
            }
        }

        private void configRssiPollInL2Connect(Message message) {
            if (message.arg1 == HwWifi2ClientModeImpl.this.mRssiPollToken) {
                updateLinkLayerStatsRssiAndScoreReportInternal();
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.sendMessageDelayed(hwWifi2ClientModeImpl.obtainMessage(HwWifi2ClientModeImplConst.CMD_RSSI_POLL, hwWifi2ClientModeImpl.mRssiPollToken, 0), (long) HwWifi2ClientModeImpl.this.mPollRssiIntervalMsecs);
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl2.sendRssiChangeBroadcast(hwWifi2ClientModeImpl2.mWifiInfo.getRssi());
                return;
            }
            HwHiLog.e(HwWifi2ClientModeImpl.TAG, false, "mRssiPollToken is wrong", new Object[0]);
        }

        private void enableRssiPollInL2Connect(Message message) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "ENABLE_RSSI_POLL enable", new Object[0]);
            HwWifi2ClientModeImpl.this.cleanWifiScore();
            HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
            boolean z = true;
            if (message.arg1 != 1) {
                z = false;
            }
            hwWifi2ClientModeImpl.mIsEnableRssiPolling = z;
            HwWifi2ClientModeImpl.access$4008(HwWifi2ClientModeImpl.this);
            if (HwWifi2ClientModeImpl.this.mIsEnableRssiPolling) {
                HwWifi2ClientModeImpl.this.mWifiInfo.score = HwWifi2ClientModeImpl.SLAVE_WIFI_TRANSITION_SCORE;
                HwWifi2ClientModeImpl.this.mLastSignalLevel = -1;
                HwWifi2ClientModeImpl.this.fetchRssiLinkSpeedAndFrequencyNative();
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl2.sendMessageDelayed(hwWifi2ClientModeImpl2.obtainMessage(HwWifi2ClientModeImplConst.CMD_RSSI_POLL, hwWifi2ClientModeImpl2.mRssiPollToken, 0), (long) HwWifi2ClientModeImpl.this.mPollRssiIntervalMsecs);
            }
        }

        private void associatedBssidInL2Connect(Message message) {
            if (message.obj == null) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Associated command w/o BSSID", new Object[0]);
            } else if (message.obj instanceof String) {
                HwWifi2ClientModeImpl.this.mLastBssid = (String) message.obj;
                if (HwWifi2ClientModeImpl.this.mLastBssid == null) {
                    return;
                }
                if (HwWifi2ClientModeImpl.this.mWifiInfo.getBSSID() == null || !HwWifi2ClientModeImpl.this.mLastBssid.equals(HwWifi2ClientModeImpl.this.mWifiInfo.getBSSID())) {
                    HwWifi2ClientModeImpl.this.mWifiInfo.setBSSID(HwWifi2ClientModeImpl.this.mLastBssid);
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl.sendNetworkStateChangeBroadcast(hwWifi2ClientModeImpl.mLastBssid);
                }
            }
        }

        private void reportScore(WifiInfo wifiInfo, NetworkAgent networkAgent) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "reportScore enter, wifiInfo.score = %{public}d", new Object[]{Integer.valueOf(wifiInfo.score)});
            if (wifiInfo.getRssi() == -127) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Not reporting score because RSSI is invalid", new Object[0]);
            } else if (wifiInfo.score != 0 && networkAgent != null) {
                networkAgent.sendNetworkScore(wifiInfo.score);
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "sendNetworkScore over", new Object[0]);
            }
        }

        private WifiLinkLayerStats updateLinkLayerStatsRssiAndScoreReportInternal() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "updateLinkLayerStatsRssiAndScoreReportInternal enter", new Object[0]);
            WifiLinkLayerStats stats = HwWifi2ClientModeImpl.this.getWifiLinkLayerStats();
            HwWifi2ClientModeImpl.this.fetchRssiLinkSpeedAndFrequencyNative();
            reportScore(HwWifi2ClientModeImpl.this.mWifiInfo, HwWifi2ClientModeImpl.this.mNetworkAgent);
            return stats;
        }
    }

    public WifiInfo getSlaveWifiConnectionInfo() {
        return new WifiInfo(this.mWifiInfo);
    }

    public LinkProperties getLinkPropertiesForSlaveWifi() {
        return this.mLinkProperties;
    }

    public NetworkInfo getNetworkInfoForSlaveWifi() {
        return this.mNetworkInfo;
    }

    class ObtainingIpState extends State {
        ObtainingIpState() {
        }

        public void enter() {
            StaticIpConfiguration staticIpConfig;
            WifiConfiguration currentConfig = HwWifi2ClientModeImpl.this.getCurrentWifiConfiguration();
            boolean isUsingStaticIp = currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC;
            currentConfig.configKey();
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Enter ObtainingIpState, netId= %{public}d %{public}s roam= %{public}s static= %{public}s", new Object[]{Integer.valueOf(HwWifi2ClientModeImpl.this.mLastNetworkId), StringUtilEx.safeDisplaySsid(currentConfig.SSID), String.valueOf(HwWifi2ClientModeImpl.this.mIsAutoRoaming), String.valueOf(isUsingStaticIp)});
            HwWifi2ClientModeImpl.this.setNetworkDetailedState(NetworkInfo.DetailedState.OBTAINING_IPADDR);
            HwWifi2ClientModeImpl.this.clearTargetBssid("ObtainingIpAddress");
            HwWifi2ClientModeImpl.this.stopIpClient();
            if (HwWifi2ClientModeImpl.this.mIpClient != null) {
                HwWifi2ClientModeImpl.this.mIpClient.setHttpProxy(currentConfig.getHttpProxy());
                if (!TextUtils.isEmpty(HwWifi2ClientModeImpl.this.mTcpBufferSizes)) {
                    HwWifi2ClientModeImpl.this.mIpClient.setTcpBufferSizes(HwWifi2ClientModeImpl.this.mTcpBufferSizes);
                }
            }
            if (!isUsingStaticIp) {
                staticIpConfig = new ProvisioningConfiguration.Builder().withPreDhcpAction().withoutIpReachabilityMonitor().withApfCapabilities(HwWifi2ClientModeImpl.this.mWifiNative.getApfCapabilities(HwWifi2ClientModeImpl.this.mInterfaceName)).withNetwork(HwWifi2ClientModeImpl.this.getCurrentNetwork()).withDisplayName(currentConfig.SSID).withRandomMacAddress().build();
            } else {
                StaticIpConfiguration prov = new ProvisioningConfiguration.Builder().withStaticConfiguration(currentConfig.getStaticIpConfiguration()).withoutIpReachabilityMonitor().withApfCapabilities(HwWifi2ClientModeImpl.this.mWifiNative.getApfCapabilities(HwWifi2ClientModeImpl.this.mInterfaceName)).withNetwork(HwWifi2ClientModeImpl.this.getCurrentNetwork()).withDisplayName(currentConfig.SSID).build();
                HwWifi2ChrManager.uploadWifi2DhcpState(8);
                staticIpConfig = prov;
            }
            if (((HwWifi2ClientModeImpl) HwWifi2ClientModeImpl.this).mIpClient != null) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "ObtainingIpState startProvisioning", new Object[0]);
                HwWifi2ClientModeImpl.this.mIpClient.startProvisioning(staticIpConfig);
            }
            HwWifi2ClientModeImpl.this.getWifiLinkLayerStats();
        }

        public void exit() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Exit ObtainingIpState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwWifi2ClientModeImplConst.CMD_SET_HIGH_PERF_MODE /* 131149 */:
                case 151559:
                    HwWifi2ClientModeImpl.this.deferMessage(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_START_CONNECT /* 131215 */:
                case HwWifi2ClientModeImplConst.CMD_START_ROAM /* 131217 */:
                    return true;
                default:
                    return false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendConnectedState() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        HwHiLog.i(TAG, false, "acceptUnvalidated = %{public}s", new Object[]{String.valueOf(config.noInternetAccessExpected)});
        HwWifi2NetworkAgent hwWifi2NetworkAgent = this.mNetworkAgent;
        if (hwWifi2NetworkAgent != null) {
            hwWifi2NetworkAgent.explicitlySelected(false, config.noInternetAccessExpected);
        }
        setNetworkDetailedState(NetworkInfo.DetailedState.CONNECTED);
        sendNetworkStateChangeBroadcast(this.mLastBssid);
    }

    class RoamingState extends State {
        boolean mIsAssociated;

        RoamingState() {
        }

        public void enter() {
            HwWifi2ClientModeImpl.access$6608(HwWifi2ClientModeImpl.this);
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Enter RoamingState", new Object[0]);
            HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
            hwWifi2ClientModeImpl.sendMessageDelayed(hwWifi2ClientModeImpl.obtainMessage(HwWifi2ClientModeImplConst.CMD_ROAM_WATCHDOG_TIMER, hwWifi2ClientModeImpl.mRoamWatchdogCount, 0), 15000);
            this.mIsAssociated = false;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwWifi2ClientModeImplConst.CMD_ROAM_WATCHDOG_TIMER /* 131166 */:
                    HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "RoamingState recv message CMD_ROAM_WATCHDOG_TIMER", new Object[0]);
                    roamingWatchDogTimeOut(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_IP_CONFIGURATION_LOST /* 131211 */:
                    HwWifi2ClientModeImpl.this.getCurrentWifiConfiguration();
                    return false;
                case HwWifi2ClientModeImplConst.CMD_UNWANTED_NETWORK /* 131216 */:
                    return true;
                case HwWifi2Monitor.NETWORK_CONNECTION_EVENT /* 147459 */:
                    networkConnectInRoamingState(message);
                    return true;
                case HwWifi2Monitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    networkDisconnectInRoamingState(message);
                    return true;
                case HwWifi2Monitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    supplicantStateChangeInRoamingState(message);
                    return true;
                default:
                    return false;
            }
        }

        private void supplicantStateChangeInRoamingState(Message message) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "RoamingState recv SUPPLICANT_STATE_CHANGE_EVENT", new Object[0]);
            StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
            if (stateChangeResult.state == SupplicantState.DISCONNECTED || stateChangeResult.state == SupplicantState.INACTIVE || stateChangeResult.state == SupplicantState.INTERFACE_DISABLED) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "STATE_CHANGE in roaming state %{public}s", new Object[]{stateChangeResult});
                if (stateChangeResult.bssid != null && stateChangeResult.bssid.equals(HwWifi2ClientModeImpl.this.mTargetRoamBssid)) {
                    HwWifi2ClientModeImpl.this.handleNetworkDisconnect();
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mDisconnectedState);
                }
            }
            if (stateChangeResult.state == SupplicantState.ASSOCIATED) {
                this.mIsAssociated = true;
                if (stateChangeResult.bssid != null) {
                    HwWifi2ClientModeImpl.this.mTargetRoamBssid = stateChangeResult.bssid;
                }
            }
        }

        private void roamingWatchDogTimeOut(Message message) {
            if (HwWifi2ClientModeImpl.this.mRoamWatchdogCount == message.arg1) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "roaming watchdog! -> disconnect", new Object[0]);
                HwWifi2ClientModeImpl.this.handleNetworkDisconnect();
                HwWifi2ClientModeImpl.this.mWifiNative.disconnect(HwWifi2ClientModeImpl.this.mInterfaceName);
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mDisconnectedState);
            }
        }

        private void networkConnectInRoamingState(Message message) {
            if (this.mIsAssociated) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "roaming and Network connection established", new Object[0]);
                HwWifi2ClientModeImpl.this.mLastNetworkId = message.arg1;
                HwWifi2ClientModeImpl.this.mLastBssid = (String) message.obj;
                HwWifi2ClientModeImpl.this.mWifiInfo.setBSSID(HwWifi2ClientModeImpl.this.mLastBssid);
                HwWifi2ClientModeImpl.this.mWifiInfo.setNetworkId(HwWifi2ClientModeImpl.this.mLastNetworkId);
                int i = message.arg2;
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.sendNetworkStateChangeBroadcast(hwWifi2ClientModeImpl.mLastBssid);
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl2.transitionTo(hwWifi2ClientModeImpl2.mConnectedState);
            }
        }

        private void networkDisconnectInRoamingState(Message message) {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "RoamingState recv NETWORK_DISCONNECTION_EVENT", new Object[0]);
            String bssid = (String) message.obj;
            if (HwWifi2ClientModeImpl.this.mTargetRoamBssid != null) {
                String target = HwWifi2ClientModeImpl.this.mTargetRoamBssid;
            }
            if (bssid != null && bssid.equals(HwWifi2ClientModeImpl.this.mTargetRoamBssid)) {
                HwWifi2ClientModeImpl.this.handleNetworkDisconnect();
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mDisconnectedState);
            }
        }

        public void exit() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Exit Roaming state", new Object[0]);
        }
    }

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Enter ConnectedState", new Object[0]);
            HwWifi2ClientModeImpl.this.mLastSignalLevel = -1;
            HwWifi2ClientModeImpl.this.mIsAutoRoaming = false;
            HwWifi2ClientModeImpl.this.mLastDriverRoamAttempt = 0;
            HwWifi2ConnectivityManager hwWifi2ConnectivityManager = HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager;
            HwWifi2ConnectivityManager unused = HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager;
            hwWifi2ConnectivityManager.handleConnectionStateChanged(1);
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwWifi2ClientModeImplConst.CMD_UNWANTED_NETWORK /* 131216 */:
                    if (message.arg1 != 0) {
                        return true;
                    }
                    HwWifi2ChrManager.uploadWifi2DisconnectException(1011);
                    HwWifi2ClientModeImpl.this.mWifiNative.disconnect(HwWifi2ClientModeImpl.this.mInterfaceName);
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mDisconnectingState);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_START_ROAM /* 131217 */:
                    startRoaming(message);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_ASSOCIATED_BSSID /* 131219 */:
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
                    hwWifi2ClientModeImpl2.mLastDriverRoamAttempt = hwWifi2ClientModeImpl2.mClock.getWallClockMillis();
                    return false;
                case HwWifi2ClientModeImplConst.CMD_NETWORK_STATUS /* 131220 */:
                    if (message.arg1 != 1) {
                        return true;
                    }
                    HwWifi2ClientModeImpl.this.removeMessages(HwWifi2ClientModeImplConst.CMD_DIAGS_CONNECT_TIMEOUT);
                    return true;
                case HwWifi2Monitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "ConnectedState recv NETWORK_DISCONNECTION_EVENT", new Object[0]);
                    if (HwWifi2ClientModeImpl.this.mLastDriverRoamAttempt == 0) {
                        return true;
                    }
                    long wallClockMillis = HwWifi2ClientModeImpl.this.mClock.getWallClockMillis() - HwWifi2ClientModeImpl.this.mLastDriverRoamAttempt;
                    HwWifi2ClientModeImpl.this.mLastDriverRoamAttempt = 0;
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Exit Connected state", new Object[0]);
            HwWifi2ClientModeImpl.this.mLastDriverRoamAttempt = 0;
            HwWifi2ConnectivityManager hwWifi2ConnectivityManager = HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager;
            HwWifi2ConnectivityManager unused = HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager;
            hwWifi2ConnectivityManager.handleConnectionStateChanged(3);
        }

        private void startRoaming(Message message) {
            HwWifi2ClientModeImpl.this.mLastDriverRoamAttempt = 0;
            ScanResult candidate = (ScanResult) message.obj;
            String bssid = "any";
            if (candidate != null) {
                bssid = candidate.BSSID;
            }
            WifiConfiguration config = HwWifi2ClientModeImpl.this.mWifiConfiguration;
            if (config == null) {
                HwHiLog.e(HwWifi2ClientModeImpl.TAG, false, "CMD_START_ROAM and no config, bail out...", new Object[0]);
                return;
            }
            int netId = message.arg1;
            HwWifi2ClientModeImpl.this.setTargetBssid(config, bssid);
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "CMD_START_ROAM sup state my state %{public}s nid= %{public}d config %{public}s targetRoamBSSID = %{public}s", new Object[]{HwWifi2ClientModeImpl.this.getCurrentState().getName(), Integer.valueOf(netId), StringUtilEx.safeDisplaySsid(config.SSID), StringUtilEx.safeDisplayBssid(HwWifi2ClientModeImpl.this.mTargetRoamBssid)});
            if (HwWifi2ClientModeImpl.this.mWifiNative.roamToNetwork(HwWifi2ClientModeImpl.this.mInterfaceName, config)) {
                HwWifi2ClientModeImpl.this.mIsAutoRoaming = true;
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mRoamingState);
                return;
            }
            HwWifi2ClientModeImpl.this.replyToMessage(message, 151554, 0);
        }
    }

    class DisconnectingState extends State {
        DisconnectingState() {
        }

        public void enter() {
            HwWifi2ClientModeImpl.this.mWifiConfiguration = null;
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Enter DisconnectingState State", new Object[0]);
        }

        public void exit() {
            HwWifi2ClientModeImpl.access$7208(HwWifi2ClientModeImpl.this);
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Start Disconnecting Watchdog %{public}d", new Object[]{Integer.valueOf(HwWifi2ClientModeImpl.this.mDisconnectingWatchdogCount)});
            HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
            hwWifi2ClientModeImpl.sendMessageDelayed(hwWifi2ClientModeImpl.obtainMessage(HwWifi2ClientModeImplConst.CMD_DISCONNECTING_WATCHDOG_TIMER, hwWifi2ClientModeImpl.mDisconnectingWatchdogCount, 0), 5000);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 131145) {
                return true;
            }
            if (i == 131168) {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "DisconnectingState recv message CMD_DISCONNECTING_WATCHDOG_TIMER", new Object[0]);
                if (HwWifi2ClientModeImpl.this.mDisconnectingWatchdogCount != message.arg1) {
                    return true;
                }
                HwWifi2ClientModeImpl.this.handleNetworkDisconnect();
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl.transitionTo(hwWifi2ClientModeImpl.mDisconnectedState);
                return true;
            } else if (i != 147462) {
                return false;
            } else {
                HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "DisconnectingState recv SUPPLICANT_STATE_CHANGE_EVENT", new Object[0]);
                HwWifi2ClientModeImpl.this.deferMessage(message);
                HwWifi2ClientModeImpl.this.handleNetworkDisconnect();
                HwWifi2ClientModeImpl hwWifi2ClientModeImpl2 = HwWifi2ClientModeImpl.this;
                hwWifi2ClientModeImpl2.transitionTo(hwWifi2ClientModeImpl2.mDisconnectedState);
                return true;
            }
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Enter disconnectedstate", new Object[0]);
            HwWifi2ClientModeImpl.this.mIsAutoRoaming = false;
            HwWifi2ClientModeImpl.this.mWifiConfiguration = null;
            HwWifi2ConnectivityManager hwWifi2ConnectivityManager = HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager;
            HwWifi2ConnectivityManager unused = HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager;
            hwWifi2ConnectivityManager.handleConnectionStateChanged(2);
            if (!HwWifi2ClientModeImpl.this.hwSetArpIgnore(0)) {
                HwHiLog.e(HwWifi2ClientModeImpl.TAG, false, "disable arp ignore fail", new Object[0]);
            }
        }

        public boolean processMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case HwWifi2ClientModeImplConst.CMD_DISCONNECT /* 131145 */:
                    HwWifi2ClientModeImpl.this.mWifiNative.disconnect(HwWifi2ClientModeImpl.this.mInterfaceName);
                    return true;
                case HwWifi2ClientModeImplConst.CMD_SCREEN_STATE_CHANGED /* 131167 */:
                    HwWifi2ClientModeImpl hwWifi2ClientModeImpl = HwWifi2ClientModeImpl.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    hwWifi2ClientModeImpl.handleScreenStateChanged(z);
                    return true;
                case HwWifi2Monitor.NETWORK_DISCONNECTION_EVENT /* 147460 */:
                    return true;
                case HwWifi2Monitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "SUPPLICANT_STATE_CHANGE state= %{public}s-> state= %{public}s", new Object[]{String.valueOf(stateChangeResult.state), String.valueOf(WifiInfo.getDetailedStateOf(stateChangeResult.state))});
                    if (SupplicantState.isConnecting(stateChangeResult.state)) {
                        HwWifi2ClientModeImpl.this.getCurrentWifiConfiguration();
                        HwWifi2ClientModeImpl.this.mWifiInfo.setFQDN(null);
                        HwWifi2ClientModeImpl.this.mWifiInfo.setOsuAp(false);
                        HwWifi2ClientModeImpl.this.mWifiInfo.setProviderFriendlyName(null);
                    }
                    HwWifi2ClientModeImpl.this.setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    return false;
                default:
                    return false;
            }
        }

        public void exit() {
            HwHiLog.i(HwWifi2ClientModeImpl.TAG, false, "Exit disconnectedstate", new Object[0]);
            HwWifi2ConnectivityManager hwWifi2ConnectivityManager = HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager;
            HwWifi2ConnectivityManager unused = HwWifi2ClientModeImpl.this.mHwWifi2ConnectivityManager;
            hwWifi2ConnectivityManager.handleConnectionStateChanged(3);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            this.mReplyChannel.replyToMessage(msg, obtainMessageWithArg(msg, what));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithArg(msg, what);
            dstMsg.arg1 = arg1;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private void replyToMessage(Message msg, int what, Object obj) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithArg(msg, what);
            dstMsg.obj = obj;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private Message obtainMessageWithArg(Message srcMsg, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg2 = srcMsg.arg2;
        return msg;
    }

    public void startConnectToNetwork(int networkId, int uid, String bssid) {
        sendMessage(HwWifi2ClientModeImplConst.CMD_START_CONNECT, networkId, uid, bssid);
    }

    public void startRoamToNetwork(int networkId, ScanResult scanResult) {
        sendMessage(HwWifi2ClientModeImplConst.CMD_START_ROAM, networkId, 0, scanResult);
    }

    public void syncInitialize() {
        sendMessage(HwWifi2ClientModeImplConst.CMD_INITIALIZE);
    }

    public String getFactoryMacAddress() {
        MacAddress macAddress = this.mWifiNative.getFactoryMacAddress(this.mInterfaceName);
        if (macAddress != null) {
            return macAddress.toString();
        }
        if (!this.mIsConnectedMacRandomzationSupported) {
            return this.mWifiNative.getMacAddress(this.mInterfaceName);
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadDhcpState(DhcpResults dhcpResults) {
        if (dhcpResults.ipAddress != null && this.mNetworkInfo != null) {
            WifiConfiguration currentConfig = getCurrentWifiConfiguration();
            if (currentConfig == null || currentConfig.getIpAssignment() != IpConfiguration.IpAssignment.STATIC) {
                if (this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    if (TYPE_GET_CACHE_DHCP_RESULT.equals(dhcpResults.domains)) {
                        HwWifi2ChrManager.uploadWifi2DhcpState(16);
                    } else {
                        HwWifi2ChrManager.uploadWifi2DhcpState(2);
                    }
                }
                if (this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    HwWifi2ChrManager.uploadWifi2DhcpState(3);
                    return;
                }
                return;
            }
            HwWifi2ChrManager.uploadWifi2DhcpState(9);
        }
    }
}
