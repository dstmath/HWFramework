package com.android.server.wifi.aware;

import android.content.Context;
import android.hardware.wifi.V1_2.NanDataPathChannelInfo;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.MatchAllNetworkSpecifier;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.RouteInfo;
import android.net.wifi.aware.TlvBufferUtils;
import android.net.wifi.aware.WifiAwareAgentNetworkSpecifier;
import android.net.wifi.aware.WifiAwareNetworkInfo;
import android.net.wifi.aware.WifiAwareNetworkSpecifier;
import android.net.wifi.aware.WifiAwareUtils;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.Clock;
import com.android.server.wifi.aware.WifiAwareDataPathStateManager;
import com.android.server.wifi.aware.WifiAwareDiscoverySessionState;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import libcore.util.HexEncoding;

public class WifiAwareDataPathStateManager {
    @VisibleForTesting
    public static final int ADDRESS_VALIDATION_RETRY_INTERVAL_MS = 1000;
    @VisibleForTesting
    public static final int ADDRESS_VALIDATION_TIMEOUT_MS = 5000;
    private static final String AGENT_TAG_PREFIX = "WIFI_AWARE_AGENT_";
    private static final String AWARE_INTERFACE_PREFIX = "aware_data";
    private static final int NETWORK_FACTORY_BANDWIDTH_AVAIL = 1;
    private static final int NETWORK_FACTORY_SCORE_AVAIL = 1;
    private static final int NETWORK_FACTORY_SIGNAL_STRENGTH_AVAIL = 1;
    private static final String NETWORK_TAG = "WIFI_AWARE_FACTORY";
    private static final String TAG = "WifiAwareDataPathStMgr";
    private static final boolean VDBG = false;
    private static final NetworkCapabilities sNetworkCapabilitiesFilter = new NetworkCapabilities();
    boolean mAllowNdpResponderFromAnyOverride = false;
    private WifiAwareMetrics mAwareMetrics;
    private final Clock mClock;
    private Context mContext;
    boolean mDbg = false;
    private Handler mHandler;
    private final Set<String> mInterfaces = new HashSet();
    private Looper mLooper;
    private final WifiAwareStateManager mMgr;
    private WifiAwareNetworkFactory mNetworkFactory;
    private final Map<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> mNetworkRequestsCache = new ArrayMap();
    public NetworkInterfaceWrapper mNiWrapper = new NetworkInterfaceWrapper();
    public INetworkManagementService mNwService;
    private WifiPermissionsWrapper mPermissionsWrapper;
    private WifiPermissionsUtil mWifiPermissionsUtil;

    public WifiAwareDataPathStateManager(WifiAwareStateManager mgr, Clock clock) {
        this.mMgr = mgr;
        this.mClock = clock;
    }

    public void start(Context context, Looper looper, WifiAwareMetrics awareMetrics, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper permissionsWrapper) {
        this.mContext = context;
        this.mAwareMetrics = awareMetrics;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mPermissionsWrapper = permissionsWrapper;
        this.mLooper = looper;
        this.mHandler = new Handler(this.mLooper);
        sNetworkCapabilitiesFilter.clearAll();
        sNetworkCapabilitiesFilter.addTransportType(5);
        sNetworkCapabilitiesFilter.addCapability(15).addCapability(11).addCapability(18).addCapability(20).addCapability(13).addCapability(14);
        sNetworkCapabilitiesFilter.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        sNetworkCapabilitiesFilter.setLinkUpstreamBandwidthKbps(1);
        sNetworkCapabilitiesFilter.setLinkDownstreamBandwidthKbps(1);
        sNetworkCapabilitiesFilter.setSignalStrength(1);
        this.mNetworkFactory = new WifiAwareNetworkFactory(looper, context, sNetworkCapabilitiesFilter);
        this.mNetworkFactory.setScoreFilter(1);
        this.mNetworkFactory.register();
        this.mNwService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
    }

    private Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> getNetworkRequestByNdpId(int ndpId) {
        for (Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> entry : this.mNetworkRequestsCache.entrySet()) {
            if (entry.getValue().ndpId == ndpId) {
                return entry;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> getNetworkRequestByCanonicalDescriptor(CanonicalConnectionInfo cci) {
        for (Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> entry : this.mNetworkRequestsCache.entrySet()) {
            if (entry.getValue().getCanonicalDescriptor().matches(cci)) {
                return entry;
            }
        }
        return null;
    }

    public void createAllInterfaces() {
        if (this.mMgr.getCapabilities() == null) {
            Log.e(TAG, "createAllInterfaces: capabilities aren't initialized yet!");
            return;
        }
        for (int i = 0; i < this.mMgr.getCapabilities().maxNdiInterfaces; i++) {
            String name = AWARE_INTERFACE_PREFIX + i;
            if (this.mInterfaces.contains(name)) {
                Log.e(TAG, "createAllInterfaces(): interface already up, " + name + ", possibly failed to delete - deleting/creating again to be safe");
                this.mMgr.deleteDataPathInterface(name);
                this.mInterfaces.remove(name);
            }
            this.mMgr.createDataPathInterface(name);
        }
    }

    public void deleteAllInterfaces() {
        onAwareDownCleanupDataPaths();
        if (this.mMgr.getCapabilities() == null) {
            Log.e(TAG, "deleteAllInterfaces: capabilities aren't initialized yet!");
            return;
        }
        for (int i = 0; i < this.mMgr.getCapabilities().maxNdiInterfaces; i++) {
            this.mMgr.deleteDataPathInterface(AWARE_INTERFACE_PREFIX + i);
        }
        this.mMgr.releaseAwareInterface();
    }

    public void onInterfaceCreated(String interfaceName) {
        if (this.mInterfaces.contains(interfaceName)) {
            Log.w(TAG, "onInterfaceCreated: already contains interface -- " + interfaceName);
        }
        this.mInterfaces.add(interfaceName);
    }

    public void onInterfaceDeleted(String interfaceName) {
        if (!this.mInterfaces.contains(interfaceName)) {
            Log.w(TAG, "onInterfaceDeleted: interface not on list -- " + interfaceName);
        }
        this.mInterfaces.remove(interfaceName);
    }

    public void onDataPathInitiateSuccess(WifiAwareNetworkSpecifier networkSpecifier, int ndpId) {
        AwareNetworkRequestInformation nnri = this.mNetworkRequestsCache.get(networkSpecifier);
        if (nnri == null) {
            Log.w(TAG, "onDataPathInitiateSuccess: network request not found for networkSpecifier=" + networkSpecifier);
            this.mMgr.endDataPath(ndpId);
        } else if (nnri.state != 103) {
            Log.w(TAG, "onDataPathInitiateSuccess: network request in incorrect state: state=" + nnri.state);
            this.mNetworkRequestsCache.remove(networkSpecifier);
            declareUnfullfillableAndEndDp(nnri, ndpId);
        } else {
            nnri.state = 101;
            nnri.ndpId = ndpId;
        }
    }

    public void onDataPathInitiateFail(WifiAwareNetworkSpecifier networkSpecifier, int reason) {
        AwareNetworkRequestInformation nnri = this.mNetworkRequestsCache.remove(networkSpecifier);
        if (nnri == null) {
            Log.w(TAG, "onDataPathInitiateFail: network request not found for networkSpecifier=" + networkSpecifier);
            return;
        }
        this.mNetworkFactory.letAppKnowThatRequestsAreUnavailable(nnri);
        if (nnri.state != 103) {
            Log.w(TAG, "onDataPathInitiateFail: network request in incorrect state: state=" + nnri.state);
        }
        this.mAwareMetrics.recordNdpStatus(reason, networkSpecifier.isOutOfBand(), nnri.startTimestamp);
    }

    public WifiAwareNetworkSpecifier onDataPathRequest(int pubSubId, byte[] mac, int ndpId, byte[] message) {
        WifiAwareNetworkSpecifier networkSpecifier;
        AwareNetworkRequestInformation nnri;
        Iterator<Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation>> it = this.mNetworkRequestsCache.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                networkSpecifier = null;
                nnri = null;
                break;
            }
            Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> entry = it.next();
            if ((entry.getValue().pubSubId == 0 || entry.getValue().pubSubId == pubSubId) && ((entry.getValue().peerDiscoveryMac == null || Arrays.equals(entry.getValue().peerDiscoveryMac, mac)) && entry.getValue().state == 104)) {
                networkSpecifier = entry.getKey();
                nnri = entry.getValue();
                break;
            }
        }
        Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> nnriE = getNetworkRequestByNdpId(ndpId);
        if (nnriE != null) {
            NetworkInformationData.ParsedResults peerServerInfo = NetworkInformationData.parseTlv(message);
            if (peerServerInfo != null) {
                if (peerServerInfo.port != 0) {
                    nnriE.getValue().peerPort = peerServerInfo.port;
                }
                if (peerServerInfo.transportProtocol != -1) {
                    nnriE.getValue().peerTransportProtocol = peerServerInfo.transportProtocol;
                }
                if (peerServerInfo.ipv6Override != null) {
                    nnriE.getValue().peerIpv6Override = peerServerInfo.ipv6Override;
                }
            }
            return null;
        } else if (nnri == null) {
            Log.w(TAG, "onDataPathRequest: can't find a request with specified pubSubId=" + pubSubId + ", mac=" + String.valueOf(HexEncoding.encode(mac)));
            this.mMgr.respondToDataPathRequest(false, ndpId, "", null, null, null, false);
            return null;
        } else {
            if (nnri.peerDiscoveryMac == null) {
                nnri.peerDiscoveryMac = mac;
            }
            nnri.interfaceName = selectInterfaceForRequest(nnri);
            if (nnri.interfaceName == null) {
                Log.w(TAG, "onDataPathRequest: request " + networkSpecifier + " no interface available");
                this.mMgr.respondToDataPathRequest(false, ndpId, "", null, null, null, false);
                this.mNetworkRequestsCache.remove(networkSpecifier);
                this.mNetworkFactory.letAppKnowThatRequestsAreUnavailable(nnri);
                return null;
            }
            nnri.state = ISupplicantStaIfaceCallback.StatusCode.ENABLEMENT_DENIED;
            nnri.ndpId = ndpId;
            nnri.startTimestamp = this.mClock.getElapsedSinceBootMillis();
            this.mMgr.respondToDataPathRequest(true, ndpId, nnri.interfaceName, nnri.networkSpecifier.pmk, nnri.networkSpecifier.passphrase, NetworkInformationData.buildTlv(nnri.networkSpecifier.port, nnri.networkSpecifier.transportProtocol), nnri.networkSpecifier.isOutOfBand());
            return networkSpecifier;
        }
    }

    public void onRespondToDataPathRequest(int ndpId, boolean success, int reasonOnFailure) {
        WifiAwareNetworkSpecifier networkSpecifier = null;
        AwareNetworkRequestInformation nnri = null;
        Iterator<Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation>> it = this.mNetworkRequestsCache.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> entry = it.next();
            if (entry.getValue().ndpId == ndpId) {
                networkSpecifier = entry.getKey();
                nnri = entry.getValue();
                break;
            }
        }
        if (nnri == null) {
            Log.w(TAG, "onRespondToDataPathRequest: can't find a request with specified ndpId=" + ndpId);
        } else if (!success) {
            Log.w(TAG, "onRespondToDataPathRequest: request " + networkSpecifier + " failed responding");
            this.mMgr.endDataPath(ndpId);
            this.mNetworkRequestsCache.remove(networkSpecifier);
            this.mNetworkFactory.letAppKnowThatRequestsAreUnavailable(nnri);
            this.mAwareMetrics.recordNdpStatus(reasonOnFailure, networkSpecifier.isOutOfBand(), nnri.startTimestamp);
        } else if (nnri.state != 105) {
            Log.w(TAG, "onRespondToDataPathRequest: request " + networkSpecifier + " is incorrect state=" + nnri.state);
            this.mMgr.endDataPath(ndpId);
            this.mNetworkRequestsCache.remove(networkSpecifier);
            this.mNetworkFactory.letAppKnowThatRequestsAreUnavailable(nnri);
        } else {
            nnri.state = 101;
        }
    }

    public WifiAwareNetworkSpecifier onDataPathConfirm(int ndpId, byte[] mac, boolean accept, int reason, byte[] message, List<NanDataPathChannelInfo> channelInfo) {
        NetworkInformationData.ParsedResults peerServerInfo;
        Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> nnriE = getNetworkRequestByNdpId(ndpId);
        if (nnriE == null) {
            Log.w(TAG, "onDataPathConfirm: network request not found for ndpId=" + ndpId);
            if (accept) {
                this.mMgr.endDataPath(ndpId);
            }
            return null;
        }
        WifiAwareNetworkSpecifier networkSpecifier = nnriE.getKey();
        AwareNetworkRequestInformation nnri = nnriE.getValue();
        if (nnri.state != 101) {
            Log.w(TAG, "onDataPathConfirm: invalid state=" + nnri.state);
            this.mNetworkRequestsCache.remove(networkSpecifier);
            this.mNetworkFactory.letAppKnowThatRequestsAreUnavailable(nnri);
            if (accept) {
                this.mMgr.endDataPath(ndpId);
            }
            return networkSpecifier;
        } else if (accept) {
            nnri.state = 102;
            nnri.peerDataMac = mac;
            nnri.channelInfo = channelInfo;
            NetworkInfo networkInfo = new NetworkInfo(-1, 0, NETWORK_TAG, "");
            NetworkCapabilities networkCapabilities = new NetworkCapabilities(sNetworkCapabilitiesFilter);
            LinkProperties linkProperties = new LinkProperties();
            if (!isInterfaceUpAndUsedByAnotherNdp(nnri)) {
                try {
                    this.mNwService.setInterfaceUp(nnri.interfaceName);
                    this.mNwService.enableIpv6(nnri.interfaceName);
                } catch (Exception e) {
                    Log.e(TAG, "onDataPathConfirm: ACCEPT nnri=" + nnri + ": can't configure network");
                    declareUnfullfillableAndEndDp(nnri, ndpId);
                    return networkSpecifier;
                }
            }
            if (nnri.networkSpecifier.role == 0 && (peerServerInfo = NetworkInformationData.parseTlv(message)) != null) {
                if (peerServerInfo.port != 0) {
                    nnri.peerPort = peerServerInfo.port;
                }
                if (peerServerInfo.transportProtocol != -1) {
                    nnri.peerTransportProtocol = peerServerInfo.transportProtocol;
                }
                if (peerServerInfo.ipv6Override != null) {
                    nnri.peerIpv6Override = peerServerInfo.ipv6Override;
                }
            }
            try {
                if (nnri.peerIpv6Override == null) {
                    nnri.peerIpv6 = Inet6Address.getByAddress((String) null, MacAddress.fromBytes(mac).getLinkLocalIpv6FromEui48Mac().getAddress(), NetworkInterface.getByName(nnri.interfaceName));
                } else {
                    byte[] addr = new byte[16];
                    addr[0] = -2;
                    addr[1] = Byte.MIN_VALUE;
                    addr[8] = nnri.peerIpv6Override[0];
                    addr[9] = nnri.peerIpv6Override[1];
                    addr[10] = nnri.peerIpv6Override[2];
                    addr[11] = nnri.peerIpv6Override[3];
                    addr[12] = nnri.peerIpv6Override[4];
                    addr[13] = nnri.peerIpv6Override[5];
                    addr[14] = nnri.peerIpv6Override[6];
                    addr[15] = nnri.peerIpv6Override[7];
                    nnri.peerIpv6 = Inet6Address.getByAddress((String) null, addr, NetworkInterface.getByName(nnri.interfaceName));
                }
            } catch (SocketException | UnknownHostException e2) {
                Log.e(TAG, "onDataPathConfirm: error obtaining scoped IPv6 address -- " + e2);
                nnri.peerIpv6 = null;
            }
            if (nnri.peerIpv6 != null) {
                networkCapabilities.setTransportInfo(new WifiAwareNetworkInfo(nnri.peerIpv6, nnri.peerPort, nnri.peerTransportProtocol));
            }
            if (!this.mNiWrapper.configureAgentProperties(nnri, nnri.equivalentRequests, ndpId, networkInfo, networkCapabilities, linkProperties)) {
                declareUnfullfillableAndEndDp(nnri, ndpId);
                return networkSpecifier;
            }
            Looper looper = this.mLooper;
            Context context = this.mContext;
            nnri.networkAgent = new WifiAwareNetworkAgent(looper, context, AGENT_TAG_PREFIX + nnri.ndpId, new NetworkInfo(-1, 0, NETWORK_TAG, ""), networkCapabilities, linkProperties, 1, nnri);
            nnri.startValidationTimestamp = this.mClock.getElapsedSinceBootMillis();
            lambda$handleAddressValidation$0$WifiAwareDataPathStateManager(nnri, linkProperties, networkInfo, ndpId, networkSpecifier.isOutOfBand());
            return networkSpecifier;
        } else {
            this.mNetworkRequestsCache.remove(networkSpecifier);
            this.mNetworkFactory.letAppKnowThatRequestsAreUnavailable(nnri);
            this.mAwareMetrics.recordNdpStatus(reason, networkSpecifier.isOutOfBand(), nnri.startTimestamp);
            return networkSpecifier;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: handleAddressValidation */
    public void lambda$handleAddressValidation$0$WifiAwareDataPathStateManager(AwareNetworkRequestInformation nnri, LinkProperties linkProperties, NetworkInfo networkInfo, int ndpId, boolean isOutOfBand) {
        if (this.mNiWrapper.isAddressUsable(linkProperties)) {
            this.mNiWrapper.sendAgentNetworkInfo(nnri.networkAgent, networkInfo);
            this.mAwareMetrics.recordNdpStatus(0, isOutOfBand, nnri.startTimestamp);
            nnri.startTimestamp = this.mClock.getElapsedSinceBootMillis();
            this.mAwareMetrics.recordNdpCreation(nnri.uid, this.mNetworkRequestsCache);
        } else if (this.mClock.getElapsedSinceBootMillis() - nnri.startValidationTimestamp > RttServiceImpl.HAL_RANGING_TIMEOUT_MS) {
            Log.e(TAG, "Timed-out while waiting for IPv6 address to be usable");
            declareUnfullfillableAndEndDp(nnri, ndpId);
        } else {
            if (this.mDbg) {
                Log.d(TAG, "Failed address validation");
            }
            this.mHandler.postDelayed(new Runnable(nnri, linkProperties, networkInfo, ndpId, isOutOfBand) {
                /* class com.android.server.wifi.aware.$$Lambda$WifiAwareDataPathStateManager$_8Gs2Y5ZqAGbeSGaJ_LndHzkxw */
                private final /* synthetic */ WifiAwareDataPathStateManager.AwareNetworkRequestInformation f$1;
                private final /* synthetic */ LinkProperties f$2;
                private final /* synthetic */ NetworkInfo f$3;
                private final /* synthetic */ int f$4;
                private final /* synthetic */ boolean f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiAwareDataPathStateManager.this.lambda$handleAddressValidation$0$WifiAwareDataPathStateManager(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                }
            }, 1000);
        }
    }

    private void declareUnfullfillableAndEndDp(AwareNetworkRequestInformation nnri, int ndpId) {
        this.mNetworkFactory.letAppKnowThatRequestsAreUnavailable(nnri);
        this.mMgr.endDataPath(ndpId);
        nnri.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
    }

    public void onDataPathEnd(int ndpId) {
        Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> nnriE = getNetworkRequestByNdpId(ndpId);
        if (nnriE != null) {
            tearDownInterfaceIfPossible(nnriE.getValue());
            if (nnriE.getValue().state == 102 || nnriE.getValue().state == 106) {
                this.mAwareMetrics.recordNdpSessionDuration(nnriE.getValue().startTimestamp);
            }
            this.mNetworkRequestsCache.remove(nnriE.getKey());
            this.mNetworkFactory.tickleConnectivityIfWaiting();
        }
    }

    public void onDataPathSchedUpdate(byte[] peerMac, List<Integer> ndpIds, List<NanDataPathChannelInfo> channelInfo) {
        for (Integer num : ndpIds) {
            int ndpId = num.intValue();
            Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> nnriE = getNetworkRequestByNdpId(ndpId);
            if (nnriE == null) {
                Log.e(TAG, "onDataPathSchedUpdate: ndpId=" + ndpId + " - not found");
            } else if (!Arrays.equals(peerMac, nnriE.getValue().peerDiscoveryMac)) {
                Log.e(TAG, "onDataPathSchedUpdate: ndpId=" + ndpId + ", report NMI=" + MacAddress.fromBytes(peerMac).toString() + " doesn't match NDP NMI=" + MacAddress.fromBytes(nnriE.getValue().peerDiscoveryMac).toString());
            } else {
                nnriE.getValue().channelInfo = channelInfo;
            }
        }
    }

    public void onAwareDownCleanupDataPaths() {
        Iterator<Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation>> it = this.mNetworkRequestsCache.entrySet().iterator();
        while (it.hasNext()) {
            tearDownInterfaceIfPossible(it.next().getValue());
            it.remove();
        }
    }

    public void handleDataPathTimeout(NetworkSpecifier networkSpecifier) {
        if (this.mDbg) {
            Log.v(TAG, "handleDataPathTimeout: networkSpecifier=" + networkSpecifier);
        }
        AwareNetworkRequestInformation nnri = this.mNetworkRequestsCache.remove(networkSpecifier);
        if (nnri != null) {
            this.mAwareMetrics.recordNdpStatus(1, nnri.networkSpecifier.isOutOfBand(), nnri.startTimestamp);
            this.mNetworkFactory.letAppKnowThatRequestsAreUnavailable(nnri);
            this.mMgr.endDataPath(nnri.ndpId);
            nnri.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
        } else if (this.mDbg) {
            Log.v(TAG, "handleDataPathTimeout: network request not found for networkSpecifier=" + networkSpecifier);
        }
    }

    /* access modifiers changed from: private */
    public class WifiAwareNetworkFactory extends NetworkFactory {
        private boolean mWaitingForTermination = false;

        WifiAwareNetworkFactory(Looper looper, Context context, NetworkCapabilities filter) {
            super(looper, context, WifiAwareDataPathStateManager.NETWORK_TAG, filter);
        }

        public void tickleConnectivityIfWaiting() {
            if (this.mWaitingForTermination) {
                this.mWaitingForTermination = false;
                reevaluateAllRequests();
            }
        }

        public boolean acceptRequest(NetworkRequest request, int score) {
            if (!WifiAwareDataPathStateManager.this.mMgr.isUsageEnabled()) {
                return false;
            }
            if (WifiAwareDataPathStateManager.this.mInterfaces.isEmpty()) {
                Log.w(WifiAwareDataPathStateManager.TAG, "WifiAwareNetworkFactory.acceptRequest: request=" + request + " -- No Aware interfaces are up");
                return false;
            }
            NetworkSpecifier networkSpecifierBase = request.networkCapabilities.getNetworkSpecifier();
            if (!(networkSpecifierBase instanceof WifiAwareNetworkSpecifier)) {
                Log.w(WifiAwareDataPathStateManager.TAG, "WifiAwareNetworkFactory.acceptRequest: request=" + request + " - not a WifiAwareNetworkSpecifier");
                return false;
            }
            WifiAwareNetworkSpecifier networkSpecifier = (WifiAwareNetworkSpecifier) networkSpecifierBase;
            AwareNetworkRequestInformation nnri = (AwareNetworkRequestInformation) WifiAwareDataPathStateManager.this.mNetworkRequestsCache.get(networkSpecifier);
            if (nnri == null) {
                AwareNetworkRequestInformation nnri2 = AwareNetworkRequestInformation.processNetworkSpecifier(request, networkSpecifier, WifiAwareDataPathStateManager.this.mMgr, WifiAwareDataPathStateManager.this.mWifiPermissionsUtil, WifiAwareDataPathStateManager.this.mPermissionsWrapper, WifiAwareDataPathStateManager.this.mAllowNdpResponderFromAnyOverride);
                if (nnri2 == null) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "WifiAwareNetworkFactory.acceptRequest: request=" + request + " - can't parse network specifier");
                    releaseRequestAsUnfulfillableByAnyFactory(request);
                    return false;
                }
                Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> primaryRequest = WifiAwareDataPathStateManager.this.getNetworkRequestByCanonicalDescriptor(nnri2.getCanonicalDescriptor());
                if (primaryRequest != null) {
                    if (primaryRequest.getValue().state == 106) {
                        this.mWaitingForTermination = true;
                    } else {
                        primaryRequest.getValue().updateToSupportNewRequest(request);
                    }
                    return false;
                }
                WifiAwareDataPathStateManager.this.mNetworkRequestsCache.put(networkSpecifier, nnri2);
                return true;
            } else if (nnri.state != 106) {
                return true;
            } else {
                this.mWaitingForTermination = true;
                return false;
            }
        }

        /* access modifiers changed from: protected */
        public void needNetworkFor(NetworkRequest networkRequest, int score) {
            NetworkSpecifier networkSpecifierObj = networkRequest.networkCapabilities.getNetworkSpecifier();
            WifiAwareNetworkSpecifier networkSpecifier = null;
            if (networkSpecifierObj instanceof WifiAwareNetworkSpecifier) {
                networkSpecifier = (WifiAwareNetworkSpecifier) networkSpecifierObj;
            }
            AwareNetworkRequestInformation nnri = (AwareNetworkRequestInformation) WifiAwareDataPathStateManager.this.mNetworkRequestsCache.get(networkSpecifier);
            if (nnri == null) {
                Log.e(WifiAwareDataPathStateManager.TAG, "WifiAwareNetworkFactory.needNetworkFor: networkRequest=" + networkRequest + " not in cache!?");
            } else if (nnri.state == 100) {
                if (nnri.networkSpecifier.role == 0) {
                    nnri.interfaceName = WifiAwareDataPathStateManager.this.selectInterfaceForRequest(nnri);
                    if (nnri.interfaceName == null) {
                        Log.w(WifiAwareDataPathStateManager.TAG, "needNetworkFor: request " + networkSpecifier + " no interface available");
                        WifiAwareDataPathStateManager.this.mNetworkRequestsCache.remove(networkSpecifier);
                        letAppKnowThatRequestsAreUnavailable(nnri);
                        return;
                    }
                    WifiAwareDataPathStateManager.this.mMgr.initiateDataPathSetup(networkSpecifier, nnri.peerInstanceId, 0, WifiAwareDataPathStateManager.this.selectChannelForRequest(nnri), nnri.peerDiscoveryMac, nnri.interfaceName, nnri.networkSpecifier.pmk, nnri.networkSpecifier.passphrase, nnri.networkSpecifier.isOutOfBand(), null);
                    nnri.state = 103;
                    nnri.startTimestamp = WifiAwareDataPathStateManager.this.mClock.getElapsedSinceBootMillis();
                    return;
                }
                nnri.state = ISupplicantStaIfaceCallback.StatusCode.ASSOC_DENIED_NO_VHT;
            }
        }

        /* access modifiers changed from: protected */
        public void releaseNetworkFor(NetworkRequest networkRequest) {
            NetworkSpecifier networkSpecifierObj = networkRequest.networkCapabilities.getNetworkSpecifier();
            WifiAwareNetworkSpecifier networkSpecifier = null;
            if (networkSpecifierObj instanceof WifiAwareNetworkSpecifier) {
                networkSpecifier = (WifiAwareNetworkSpecifier) networkSpecifierObj;
            }
            AwareNetworkRequestInformation nnri = (AwareNetworkRequestInformation) WifiAwareDataPathStateManager.this.mNetworkRequestsCache.get(networkSpecifier);
            if (nnri == null) {
                Log.e(WifiAwareDataPathStateManager.TAG, "WifiAwareNetworkFactory.releaseNetworkFor: networkRequest=" + networkRequest + " not in cache!?");
            } else if (nnri.networkAgent == null) {
                nnri.removeSupportForRequest(networkRequest);
                if (!nnri.equivalentRequests.isEmpty()) {
                    return;
                }
                if (nnri.ndpId != 0) {
                    WifiAwareDataPathStateManager.this.mMgr.endDataPath(nnri.ndpId);
                    nnri.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
                    return;
                }
                WifiAwareDataPathStateManager.this.mNetworkRequestsCache.remove(networkSpecifier);
                if (nnri.networkAgent != null) {
                    letAppKnowThatRequestsAreUnavailable(nnri);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void letAppKnowThatRequestsAreUnavailable(AwareNetworkRequestInformation nnri) {
            for (NetworkRequest nr : nnri.equivalentRequests) {
                releaseRequestAsUnfulfillableByAnyFactory(nr);
            }
        }
    }

    @VisibleForTesting
    public class WifiAwareNetworkAgent extends NetworkAgent {
        private AwareNetworkRequestInformation mAwareNetworkRequestInfo;
        private NetworkInfo mNetworkInfo;

        WifiAwareNetworkAgent(Looper looper, Context context, String logTag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, AwareNetworkRequestInformation anri) {
            super(looper, context, logTag, ni, nc, lp, score);
            this.mNetworkInfo = ni;
            this.mAwareNetworkRequestInfo = anri;
        }

        /* access modifiers changed from: protected */
        public void unwanted() {
            WifiAwareDataPathStateManager.this.mMgr.endDataPath(this.mAwareNetworkRequestInfo.ndpId);
            this.mAwareNetworkRequestInfo.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
        }

        /* access modifiers changed from: package-private */
        public void reconfigureAgentAsDisconnected() {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, "");
            sendNetworkInfo(this.mNetworkInfo);
        }
    }

    private void tearDownInterfaceIfPossible(AwareNetworkRequestInformation nnri) {
        if (!TextUtils.isEmpty(nnri.interfaceName) && !isInterfaceUpAndUsedByAnotherNdp(nnri)) {
            try {
                this.mNwService.setInterfaceDown(nnri.interfaceName);
            } catch (Exception e) {
                Log.e(TAG, "tearDownInterfaceIfPossible: nnri=" + nnri + ": can't bring interface down");
            }
        }
        if (nnri.networkAgent == null) {
            this.mNetworkFactory.letAppKnowThatRequestsAreUnavailable(nnri);
        } else {
            nnri.networkAgent.reconfigureAgentAsDisconnected();
        }
    }

    private boolean isInterfaceUpAndUsedByAnotherNdp(AwareNetworkRequestInformation nri) {
        for (AwareNetworkRequestInformation lnri : this.mNetworkRequestsCache.values()) {
            if (lnri != nri && nri.interfaceName.equals(lnri.interfaceName)) {
                if (lnri.state == 102 || lnri.state == 106) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String selectInterfaceForRequest(AwareNetworkRequestInformation req) {
        SortedSet<String> potential = new TreeSet<>(this.mInterfaces);
        Set<String> used = new HashSet<>();
        for (AwareNetworkRequestInformation nnri : this.mNetworkRequestsCache.values()) {
            if (nnri != req && Arrays.equals(req.peerDiscoveryMac, nnri.peerDiscoveryMac)) {
                used.add(nnri.interfaceName);
            }
        }
        for (String ifName : potential) {
            if (!used.contains(ifName)) {
                return ifName;
            }
        }
        Log.e(TAG, "selectInterfaceForRequest: req=" + req + " - no interfaces available!");
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int selectChannelForRequest(AwareNetworkRequestInformation req) {
        return 2437;
    }

    @VisibleForTesting
    public static class AwareNetworkRequestInformation {
        static final int STATE_CONFIRMED = 102;
        static final int STATE_IDLE = 100;
        static final int STATE_INITIATOR_WAIT_FOR_REQUEST_RESPONSE = 103;
        static final int STATE_RESPONDER_WAIT_FOR_REQUEST = 104;
        static final int STATE_RESPONDER_WAIT_FOR_RESPOND_RESPONSE = 105;
        static final int STATE_TERMINATING = 106;
        static final int STATE_WAIT_FOR_CONFIRM = 101;
        public List<NanDataPathChannelInfo> channelInfo;
        public Set<NetworkRequest> equivalentRequests = new HashSet();
        public String interfaceName;
        public int ndpId = 0;
        public WifiAwareNetworkAgent networkAgent;
        public WifiAwareNetworkSpecifier networkSpecifier;
        public byte[] peerDataMac;
        public byte[] peerDiscoveryMac = null;
        public int peerInstanceId = 0;
        public Inet6Address peerIpv6;
        public byte[] peerIpv6Override = null;
        public int peerPort = 0;
        public int peerTransportProtocol = -1;
        public int pubSubId = 0;
        public long startTimestamp = 0;
        public long startValidationTimestamp = 0;
        public int state;
        public int uid;

        /* access modifiers changed from: package-private */
        public void updateToSupportNewRequest(NetworkRequest ns) {
            if (this.equivalentRequests.add(ns) && this.state == 102) {
                WifiAwareNetworkAgent wifiAwareNetworkAgent = this.networkAgent;
                if (wifiAwareNetworkAgent == null) {
                    Log.wtf(WifiAwareDataPathStateManager.TAG, "updateToSupportNewRequest: null agent in CONFIRMED state!?");
                } else {
                    wifiAwareNetworkAgent.sendNetworkCapabilities(getNetworkCapabilities());
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void removeSupportForRequest(NetworkRequest ns) {
            this.equivalentRequests.remove(ns);
        }

        private NetworkCapabilities getNetworkCapabilities() {
            NetworkCapabilities nc = new NetworkCapabilities(WifiAwareDataPathStateManager.sNetworkCapabilitiesFilter);
            nc.setNetworkSpecifier(new WifiAwareAgentNetworkSpecifier((WifiAwareNetworkSpecifier[]) ((List) this.equivalentRequests.stream().map($$Lambda$WifiAwareDataPathStateManager$AwareNetworkRequestInformation$39ENKv5hDa6RLtoJkAXWF8pVxAs.INSTANCE).collect(Collectors.toList())).toArray(new WifiAwareNetworkSpecifier[0])));
            Inet6Address inet6Address = this.peerIpv6;
            if (inet6Address != null) {
                nc.setTransportInfo(new WifiAwareNetworkInfo(inet6Address, this.peerPort, this.peerTransportProtocol));
            }
            return nc;
        }

        /* access modifiers changed from: package-private */
        public CanonicalConnectionInfo getCanonicalDescriptor() {
            return new CanonicalConnectionInfo(this.peerDiscoveryMac, this.networkSpecifier.pmk, this.networkSpecifier.sessionId, this.networkSpecifier.passphrase);
        }

        static AwareNetworkRequestInformation processNetworkSpecifier(NetworkRequest request, WifiAwareNetworkSpecifier ns, WifiAwareStateManager mgr, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper permissionWrapper, boolean allowNdpResponderFromAnyOverride) {
            int pubSubId2 = 0;
            int peerInstanceId2 = 0;
            byte[] peerMac = ns.peerMac;
            if (ns.type >= 0) {
                if (ns.type <= 3) {
                    if (ns.role != 0 && ns.role != 1) {
                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid 'role' value");
                        return null;
                    } else if (ns.role != 0 || ns.type == 0 || ns.type == 2) {
                        WifiAwareClientState client = mgr.getClient(ns.clientId);
                        if (client == null) {
                            Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- not client with this id -- clientId=" + ns.clientId);
                            return null;
                        }
                        int uid2 = client.getUid();
                        if (!allowNdpResponderFromAnyOverride) {
                            if (!(wifiPermissionsUtil.isTargetSdkLessThan(client.getCallingPackage(), 28) || ns.type == 0 || ns.type == 2)) {
                                Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- no ANY specifications allowed for this API level");
                                return null;
                            }
                        }
                        if (ns.port >= 0) {
                            if (ns.transportProtocol >= -1) {
                                if (!(ns.port == 0 && ns.transportProtocol == -1)) {
                                    if (ns.role != 1) {
                                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- port/transportProtocol can only be specified on responder");
                                        return null;
                                    } else if (TextUtils.isEmpty(ns.passphrase) && ns.pmk == null) {
                                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- port/transportProtocol can only be specified on secure ndp");
                                        return null;
                                    }
                                }
                                if (ns.type == 0 || ns.type == 1) {
                                    WifiAwareDiscoverySessionState session = client.getSession(ns.sessionId);
                                    if (session == null) {
                                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- no session with this id -- sessionId=" + ns.sessionId);
                                        return null;
                                    } else if ((session.isPublishSession() && ns.role != 1) || (!session.isPublishSession() && ns.role != 0)) {
                                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid role for session type");
                                        return null;
                                    } else if (ns.type == 0) {
                                        int pubSubId3 = session.getPubSubId();
                                        WifiAwareDiscoverySessionState.PeerInfo peerInfo = session.getPeerInfo(ns.peerId);
                                        if (peerInfo == null) {
                                            Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- no peer info associated with this peer id -- peerId=" + ns.peerId);
                                            return null;
                                        }
                                        peerInstanceId2 = peerInfo.mInstanceId;
                                        try {
                                            peerMac = peerInfo.mMac;
                                            if (peerMac != null) {
                                                if (peerMac.length == 6) {
                                                    pubSubId2 = pubSubId3;
                                                }
                                            }
                                            Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid peer MAC address");
                                            return null;
                                        } catch (IllegalArgumentException e) {
                                            Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid peer MAC address -- e=" + e);
                                            return null;
                                        }
                                    }
                                }
                                if (ns.requestorUid != uid2) {
                                    Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns.toString() + " -- UID mismatch to clientId's uid=" + uid2);
                                    return null;
                                }
                                if (ns.pmk != null && ns.pmk.length != 0) {
                                    if (permissionWrapper.getUidPermission("android.permission.CONNECTIVITY_INTERNAL", ns.requestorUid) != 0) {
                                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns.toString() + " -- UID doesn't have permission to use PMK API");
                                        return null;
                                    }
                                }
                                if (!TextUtils.isEmpty(ns.passphrase) && !WifiAwareUtils.validatePassphrase(ns.passphrase)) {
                                    Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns.toString() + " -- invalid passphrase length: " + ns.passphrase.length());
                                    return null;
                                } else if (ns.pmk == null || WifiAwareUtils.validatePmk(ns.pmk)) {
                                    AwareNetworkRequestInformation nnri = new AwareNetworkRequestInformation();
                                    nnri.state = 100;
                                    nnri.uid = uid2;
                                    nnri.pubSubId = pubSubId2;
                                    nnri.peerInstanceId = peerInstanceId2;
                                    nnri.peerDiscoveryMac = peerMac;
                                    nnri.networkSpecifier = ns;
                                    nnri.equivalentRequests.add(request);
                                    return nnri;
                                } else {
                                    Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns.toString() + " -- invalid pmk length: " + ns.pmk.length);
                                    return null;
                                }
                            }
                        }
                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid port/transportProtocol");
                        return null;
                    } else {
                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid 'type' value for INITIATOR (only IB and OOB are permitted)");
                        return null;
                    }
                }
            }
            Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + ", invalid 'type' value");
            return null;
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder("AwareNetworkRequestInformation: ");
            sb.append("state=");
            sb.append(this.state);
            sb.append(", ns=");
            sb.append(this.networkSpecifier);
            sb.append(", uid=");
            sb.append(this.uid);
            sb.append(", interfaceName=");
            sb.append(this.interfaceName);
            sb.append(", pubSubId=");
            sb.append(this.pubSubId);
            sb.append(", peerInstanceId=");
            sb.append(this.peerInstanceId);
            sb.append(", peerDiscoveryMac=");
            byte[] bArr = this.peerDiscoveryMac;
            String str2 = "";
            if (bArr == null) {
                str = str2;
            } else {
                str = String.valueOf(HexEncoding.encode(bArr));
            }
            sb.append(str);
            sb.append(", ndpId=");
            sb.append(this.ndpId);
            sb.append(", peerDataMac=");
            byte[] bArr2 = this.peerDataMac;
            if (bArr2 != null) {
                str2 = String.valueOf(HexEncoding.encode(bArr2));
            }
            sb.append(str2);
            sb.append(", peerIpv6=");
            sb.append(this.peerIpv6);
            sb.append(", peerPort=");
            sb.append(this.peerPort);
            sb.append(", peerTransportProtocol=");
            sb.append(this.peerTransportProtocol);
            sb.append(", startTimestamp=");
            sb.append(this.startTimestamp);
            sb.append(", channelInfo=");
            sb.append(this.channelInfo);
            sb.append(", equivalentSpecifiers=[");
            for (NetworkRequest nr : this.equivalentRequests) {
                sb.append(nr.toString());
                sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public static class CanonicalConnectionInfo {
        public final String passphrase;
        public final byte[] peerDiscoveryMac;
        public final byte[] pmk;
        public final int sessionId;

        CanonicalConnectionInfo(byte[] peerDiscoveryMac2, byte[] pmk2, int sessionId2, String passphrase2) {
            this.peerDiscoveryMac = peerDiscoveryMac2;
            this.pmk = pmk2;
            this.sessionId = sessionId2;
            this.passphrase = passphrase2;
        }

        public boolean matches(CanonicalConnectionInfo other) {
            byte[] bArr = other.peerDiscoveryMac;
            return (bArr == null || Arrays.equals(this.peerDiscoveryMac, bArr)) && Arrays.equals(this.pmk, other.pmk) && TextUtils.equals(this.passphrase, other.passphrase) && (TextUtils.isEmpty(this.passphrase) || this.sessionId == other.sessionId);
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder("CanonicalConnectionInfo: [");
            sb.append("peerDiscoveryMac=");
            byte[] bArr = this.peerDiscoveryMac;
            String str2 = "";
            if (bArr == null) {
                str = str2;
            } else {
                str = String.valueOf(HexEncoding.encode(bArr));
            }
            sb.append(str);
            sb.append(", pmk=");
            sb.append(this.pmk == null ? str2 : "*");
            sb.append(", sessionId=");
            sb.append(this.sessionId);
            sb.append(", passphrase=");
            if (this.passphrase != null) {
                str2 = "*";
            }
            sb.append(str2);
            sb.append("]");
            return sb.toString();
        }
    }

    @VisibleForTesting
    public class NetworkInterfaceWrapper {
        public NetworkInterfaceWrapper() {
        }

        public boolean configureAgentProperties(AwareNetworkRequestInformation nnri, Set<NetworkRequest> networkRequests, int ndpId, NetworkInfo networkInfo, NetworkCapabilities networkCapabilities, LinkProperties linkProperties) {
            InetAddress linkLocal = null;
            try {
                NetworkInterface ni = NetworkInterface.getByName(nnri.interfaceName);
                if (ni == null) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "onDataPathConfirm: ACCEPT nnri=" + nnri + ": can't get network interface (null)");
                    WifiAwareDataPathStateManager.this.mMgr.endDataPath(ndpId);
                    nnri.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
                    return false;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (true) {
                    if (!addresses.hasMoreElements()) {
                        break;
                    }
                    InetAddress ip = addresses.nextElement();
                    if ((ip instanceof Inet6Address) && ip.isLinkLocalAddress()) {
                        linkLocal = ip;
                        break;
                    }
                }
                if (linkLocal == null) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "onDataPathConfirm: ACCEPT nnri=" + nnri + ": no link local addresses");
                    WifiAwareDataPathStateManager.this.mMgr.endDataPath(ndpId);
                    nnri.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
                    return false;
                }
                networkInfo.setIsAvailable(true);
                networkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, null);
                networkCapabilities.setNetworkSpecifier(new WifiAwareAgentNetworkSpecifier((WifiAwareNetworkSpecifier[]) ((List) networkRequests.stream().map($$Lambda$WifiAwareDataPathStateManager$NetworkInterfaceWrapper$5dfGOhPStI7PI7XT9E1QFwOyQdc.INSTANCE).collect(Collectors.toList())).toArray(new WifiAwareNetworkSpecifier[0])));
                linkProperties.setInterfaceName(nnri.interfaceName);
                linkProperties.addLinkAddress(new LinkAddress(linkLocal, 64));
                linkProperties.addRoute(new RouteInfo(new IpPrefix("fe80::/64"), null, nnri.interfaceName));
                return true;
            } catch (SocketException e) {
                Log.e(WifiAwareDataPathStateManager.TAG, "onDataPathConfirm: ACCEPT nnri=" + nnri + ": can't get network interface - " + e);
                WifiAwareDataPathStateManager.this.mMgr.endDataPath(ndpId);
                nnri.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
                return false;
            }
        }

        public boolean isAddressUsable(LinkProperties linkProperties) {
            InetAddress address = linkProperties.getLinkAddresses().get(0).getAddress();
            DatagramSocket testDatagramSocket = null;
            try {
                new DatagramSocket(0, address).close();
                return true;
            } catch (SocketException e) {
                if (WifiAwareDataPathStateManager.this.mDbg) {
                    Log.d(WifiAwareDataPathStateManager.TAG, "Can't create socket on address " + address + " -- " + e);
                }
                if (0 != 0) {
                    testDatagramSocket.close();
                }
                return false;
            } catch (Throwable th) {
                if (0 != 0) {
                    testDatagramSocket.close();
                }
                throw th;
            }
        }

        public void sendAgentNetworkInfo(WifiAwareNetworkAgent networkAgent, NetworkInfo networkInfo) {
            networkAgent.sendNetworkInfo(networkInfo);
        }
    }

    @VisibleForTesting
    public static class NetworkInformationData {
        static final int GENERIC_SERVICE_PROTOCOL_TYPE = 2;
        static final int IPV6_LL_TYPE = 0;
        static final int SERVICE_INFO_TYPE = 1;
        static final int SUB_TYPE_PORT = 0;
        static final int SUB_TYPE_TRANSPORT_PROTOCOL = 1;
        static final byte[] WFA_OUI = {80, 111, -102};

        public static byte[] buildTlv(int port, int transportProtocol) {
            if (port == 0 && transportProtocol == -1) {
                return null;
            }
            TlvBufferUtils.TlvConstructor tlvc = new TlvBufferUtils.TlvConstructor(1, 2);
            tlvc.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            tlvc.allocate(20);
            tlvc.putRawByteArray(WFA_OUI);
            tlvc.putRawByte((byte) 2);
            if (port != 0) {
                tlvc.putShort(0, (short) port);
            }
            if (transportProtocol != -1) {
                tlvc.putByte(1, (byte) transportProtocol);
            }
            byte[] subTypes = tlvc.getArray();
            tlvc.allocate(20);
            tlvc.putByteArray(1, subTypes);
            return tlvc.getArray();
        }

        /* access modifiers changed from: package-private */
        public static class ParsedResults {
            public byte[] ipv6Override = null;
            public int port = 0;
            public int transportProtocol = -1;

            ParsedResults(int port2, int transportProtocol2, byte[] ipv6Override2) {
                this.port = port2;
                this.transportProtocol = transportProtocol2;
                this.ipv6Override = ipv6Override2;
            }
        }

        public static ParsedResults parseTlv(byte[] tlvs) {
            int port = 0;
            int transportProtocol = -1;
            byte[] ipv6Override = null;
            try {
                TlvBufferUtils.TlvIterable tlvi = new TlvBufferUtils.TlvIterable(1, 2, tlvs);
                tlvi.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                Iterator it = tlvi.iterator();
                while (it.hasNext()) {
                    TlvBufferUtils.TlvElement tlve = (TlvBufferUtils.TlvElement) it.next();
                    int i = tlve.type;
                    if (i != 0) {
                        if (i != 1) {
                            Log.w(WifiAwareDataPathStateManager.TAG, "NetworkInformationData: ignoring unknown T -- " + tlve.type);
                        } else {
                            Pair<Integer, Integer> serviceInfo = parseServiceInfoTlv(tlve.getRawData());
                            if (serviceInfo == null) {
                                return null;
                            }
                            port = ((Integer) serviceInfo.first).intValue();
                            transportProtocol = ((Integer) serviceInfo.second).intValue();
                        }
                    } else if (tlve.length != 8) {
                        Log.e(WifiAwareDataPathStateManager.TAG, "NetworkInformationData: invalid IPv6 TLV -- length: " + tlve.length);
                        return null;
                    } else {
                        ipv6Override = tlve.getRawData();
                    }
                }
                return new ParsedResults(port, transportProtocol, ipv6Override);
            } catch (Exception e) {
                Log.e(WifiAwareDataPathStateManager.TAG, "NetworkInformationData: error parsing TLV");
                return null;
            }
        }

        private static Pair<Integer, Integer> parseServiceInfoTlv(byte[] tlv) {
            int port = 0;
            int transportProtocol = -1;
            if (tlv.length < 4) {
                Log.e(WifiAwareDataPathStateManager.TAG, "NetworkInformationData: invalid SERVICE_INFO_TYPE length");
                return null;
            }
            byte b = tlv[0];
            byte[] bArr = WFA_OUI;
            if (b != bArr[0] || tlv[1] != bArr[1] || tlv[2] != bArr[2]) {
                Log.e(WifiAwareDataPathStateManager.TAG, "NetworkInformationData: unexpected OUI");
                return null;
            } else if (tlv[3] != 2) {
                Log.e(WifiAwareDataPathStateManager.TAG, "NetworkInformationData: invalid type -- " + ((int) tlv[3]));
                return null;
            } else {
                TlvBufferUtils.TlvIterable subTlvi = new TlvBufferUtils.TlvIterable(1, 2, Arrays.copyOfRange(tlv, 4, tlv.length));
                subTlvi.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                Iterator it = subTlvi.iterator();
                while (it.hasNext()) {
                    TlvBufferUtils.TlvElement subTlve = (TlvBufferUtils.TlvElement) it.next();
                    int i = subTlve.type;
                    if (i != 0) {
                        if (i != 1) {
                            Log.w(WifiAwareDataPathStateManager.TAG, "NetworkInformationData: ignoring unknown SERVICE_INFO.T -- " + subTlve.type);
                        } else if (subTlve.length != 1) {
                            Log.e(WifiAwareDataPathStateManager.TAG, "NetworkInformationData: invalid transport protocol TLV length -- " + subTlve.length);
                            return null;
                        } else {
                            transportProtocol = subTlve.getByte();
                            if (transportProtocol < 0) {
                                transportProtocol += 256;
                            }
                        }
                    } else if (subTlve.length != 2) {
                        Log.e(WifiAwareDataPathStateManager.TAG, "NetworkInformationData: invalid port TLV length -- " + subTlve.length);
                        return null;
                    } else {
                        port = subTlve.getShort();
                        if (port < 0) {
                            port += 65536;
                        }
                        if (port == 0) {
                            Log.e(WifiAwareDataPathStateManager.TAG, "NetworkInformationData: invalid port " + port);
                            return null;
                        }
                    }
                }
                return Pair.create(Integer.valueOf(port), Integer.valueOf(transportProtocol));
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("WifiAwareDataPathStateManager:");
        pw.println("  mInterfaces: " + this.mInterfaces);
        pw.println("  sNetworkCapabilitiesFilter: " + sNetworkCapabilitiesFilter);
        pw.println("  mNetworkRequestsCache: " + this.mNetworkRequestsCache);
        pw.println("  mNetworkFactory:");
        this.mNetworkFactory.dump(fd, pw, args);
    }
}
