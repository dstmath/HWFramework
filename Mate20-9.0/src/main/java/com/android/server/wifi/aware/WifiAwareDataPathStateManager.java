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
import android.net.wifi.aware.WifiAwareAgentNetworkSpecifier;
import android.net.wifi.aware.WifiAwareNetworkSpecifier;
import android.net.wifi.aware.WifiAwareUtils;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.aware.WifiAwareDiscoverySessionState;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import libcore.util.HexEncoding;

public class WifiAwareDataPathStateManager {
    private static final String AGENT_TAG_PREFIX = "WIFI_AWARE_AGENT_";
    private static final String AWARE_INTERFACE_PREFIX = "aware_data";
    private static final int NETWORK_FACTORY_BANDWIDTH_AVAIL = 1;
    private static final int NETWORK_FACTORY_SCORE_AVAIL = 1;
    private static final int NETWORK_FACTORY_SIGNAL_STRENGTH_AVAIL = 1;
    private static final String NETWORK_TAG = "WIFI_AWARE_FACTORY";
    private static final String TAG = "WifiAwareDataPathStMgr";
    private static final boolean VDBG = false;
    /* access modifiers changed from: private */
    public static final NetworkCapabilities sNetworkCapabilitiesFilter = new NetworkCapabilities();
    boolean mAllowNdpResponderFromAnyOverride = false;
    private WifiAwareMetrics mAwareMetrics;
    private Context mContext;
    boolean mDbg = false;
    /* access modifiers changed from: private */
    public final Set<String> mInterfaces = new HashSet();
    private Looper mLooper;
    /* access modifiers changed from: private */
    public final WifiAwareStateManager mMgr;
    private WifiAwareNetworkFactory mNetworkFactory;
    /* access modifiers changed from: private */
    public final Map<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> mNetworkRequestsCache = new ArrayMap();
    public NetworkInterfaceWrapper mNiWrapper = new NetworkInterfaceWrapper();
    public INetworkManagementService mNwService;
    /* access modifiers changed from: private */
    public WifiPermissionsWrapper mPermissionsWrapper;
    /* access modifiers changed from: private */
    public WifiPermissionsUtil mWifiPermissionsUtil;

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
        public Set<WifiAwareNetworkSpecifier> equivalentSpecifiers = new HashSet();
        public String interfaceName;
        public int ndpId = 0;
        public WifiAwareNetworkAgent networkAgent;
        public WifiAwareNetworkSpecifier networkSpecifier;
        public byte[] peerDataMac;
        public byte[] peerDiscoveryMac = null;
        public int peerInstanceId = 0;
        public int pubSubId = 0;
        public long startTimestamp = 0;
        public int state;
        public int uid;

        /* access modifiers changed from: package-private */
        public void updateToSupportNewRequest(WifiAwareNetworkSpecifier ns) {
            if (this.equivalentSpecifiers.add(ns) && this.state == 102) {
                if (this.networkAgent == null) {
                    Log.wtf(WifiAwareDataPathStateManager.TAG, "updateToSupportNewRequest: null agent in CONFIRMED state!?");
                    return;
                }
                this.networkAgent.sendNetworkCapabilities(getNetworkCapabilities());
            }
        }

        /* access modifiers changed from: package-private */
        public void removeSupportForRequest(WifiAwareNetworkSpecifier ns) {
            this.equivalentSpecifiers.remove(ns);
        }

        private NetworkCapabilities getNetworkCapabilities() {
            NetworkCapabilities nc = new NetworkCapabilities(WifiAwareDataPathStateManager.sNetworkCapabilitiesFilter);
            nc.setNetworkSpecifier(new WifiAwareAgentNetworkSpecifier((WifiAwareNetworkSpecifier[]) this.equivalentSpecifiers.toArray(new WifiAwareNetworkSpecifier[this.equivalentSpecifiers.size()])));
            return nc;
        }

        /* access modifiers changed from: package-private */
        public CanonicalConnectionInfo getCanonicalDescriptor() {
            return new CanonicalConnectionInfo(this.peerDiscoveryMac, this.networkSpecifier.pmk, this.networkSpecifier.sessionId, this.networkSpecifier.passphrase);
        }

        static AwareNetworkRequestInformation processNetworkSpecifier(WifiAwareNetworkSpecifier ns, WifiAwareStateManager mgr, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper permissionWrapper, boolean allowNdpResponderFromAnyOverride) {
            WifiAwareNetworkSpecifier wifiAwareNetworkSpecifier = ns;
            int pubSubId2 = 0;
            int peerInstanceId2 = 0;
            byte[] peerMac = wifiAwareNetworkSpecifier.peerMac;
            if (wifiAwareNetworkSpecifier.type < 0 || wifiAwareNetworkSpecifier.type > 3) {
                WifiAwareStateManager wifiAwareStateManager = mgr;
                WifiPermissionsUtil wifiPermissionsUtil2 = wifiPermissionsUtil;
                WifiPermissionsWrapper wifiPermissionsWrapper = permissionWrapper;
                Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier + ", invalid 'type' value");
                return null;
            } else if (wifiAwareNetworkSpecifier.role != 0 && wifiAwareNetworkSpecifier.role != 1) {
                Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier + " -- invalid 'role' value");
                return null;
            } else if (wifiAwareNetworkSpecifier.role != 0 || wifiAwareNetworkSpecifier.type == 0 || wifiAwareNetworkSpecifier.type == 2) {
                WifiAwareClientState client = mgr.getClient(wifiAwareNetworkSpecifier.clientId);
                if (client == null) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier + " -- not client with this id -- clientId=" + wifiAwareNetworkSpecifier.clientId);
                    return null;
                }
                int uid2 = client.getUid();
                if (!allowNdpResponderFromAnyOverride) {
                    if (!(wifiPermissionsUtil.isLegacyVersion(client.getCallingPackage(), 28) || wifiAwareNetworkSpecifier.type == 0 || wifiAwareNetworkSpecifier.type == 2)) {
                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier + " -- no ANY specifications allowed for this API level");
                        return null;
                    }
                } else {
                    WifiPermissionsUtil wifiPermissionsUtil3 = wifiPermissionsUtil;
                }
                if (wifiAwareNetworkSpecifier.type == 0 || wifiAwareNetworkSpecifier.type == 1) {
                    WifiAwareDiscoverySessionState session = client.getSession(wifiAwareNetworkSpecifier.sessionId);
                    if (session == null) {
                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier + " -- no session with this id -- sessionId=" + wifiAwareNetworkSpecifier.sessionId);
                        return null;
                    } else if ((session.isPublishSession() && wifiAwareNetworkSpecifier.role != 1) || (!session.isPublishSession() && wifiAwareNetworkSpecifier.role != 0)) {
                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier + " -- invalid role for session type");
                        return null;
                    } else if (wifiAwareNetworkSpecifier.type == 0) {
                        int pubSubId3 = session.getPubSubId();
                        WifiAwareDiscoverySessionState.PeerInfo peerInfo = session.getPeerInfo(wifiAwareNetworkSpecifier.peerId);
                        if (peerInfo == null) {
                            Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier + " -- no peer info associated with this peer id -- peerId=" + wifiAwareNetworkSpecifier.peerId);
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
                            Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier + " -- invalid peer MAC address");
                            return null;
                        } catch (IllegalArgumentException e) {
                            Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier + " -- invalid peer MAC address -- e=" + e);
                            return null;
                        }
                    }
                }
                if (wifiAwareNetworkSpecifier.requestorUid != uid2) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier.toString() + " -- UID mismatch to clientId's uid=" + uid2);
                    return null;
                }
                if (wifiAwareNetworkSpecifier.pmk == null || wifiAwareNetworkSpecifier.pmk.length == 0) {
                    WifiPermissionsWrapper wifiPermissionsWrapper2 = permissionWrapper;
                } else {
                    if (permissionWrapper.getUidPermission("android.permission.CONNECTIVITY_INTERNAL", wifiAwareNetworkSpecifier.requestorUid) != 0) {
                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier.toString() + " -- UID doesn't have permission to use PMK API");
                        return null;
                    }
                }
                if (!TextUtils.isEmpty(wifiAwareNetworkSpecifier.passphrase) && !WifiAwareUtils.validatePassphrase(wifiAwareNetworkSpecifier.passphrase)) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier.toString() + " -- invalid passphrase length: " + wifiAwareNetworkSpecifier.passphrase.length());
                    return null;
                } else if (wifiAwareNetworkSpecifier.pmk == null || WifiAwareUtils.validatePmk(wifiAwareNetworkSpecifier.pmk)) {
                    AwareNetworkRequestInformation nnri = new AwareNetworkRequestInformation();
                    nnri.state = 100;
                    nnri.uid = uid2;
                    nnri.pubSubId = pubSubId2;
                    nnri.peerInstanceId = peerInstanceId2;
                    nnri.peerDiscoveryMac = peerMac;
                    nnri.networkSpecifier = wifiAwareNetworkSpecifier;
                    nnri.equivalentSpecifiers.add(wifiAwareNetworkSpecifier);
                    return nnri;
                } else {
                    Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier.toString() + " -- invalid pmk length: " + wifiAwareNetworkSpecifier.pmk.length);
                    return null;
                }
            } else {
                Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + wifiAwareNetworkSpecifier + " -- invalid 'type' value for INITIATOR (only IB and OOB are permitted)");
                return null;
            }
        }

        public String toString() {
            String str;
            String str2;
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
            if (this.peerDiscoveryMac == null) {
                str = "";
            } else {
                str = String.valueOf(HexEncoding.encode(this.peerDiscoveryMac));
            }
            sb.append(str);
            sb.append(", ndpId=");
            sb.append(this.ndpId);
            sb.append(", peerDataMac=");
            if (this.peerDataMac == null) {
                str2 = "";
            } else {
                str2 = String.valueOf(HexEncoding.encode(this.peerDataMac));
            }
            sb.append(str2);
            sb.append(", startTimestamp=");
            sb.append(this.startTimestamp);
            sb.append(", channelInfo=");
            sb.append(this.channelInfo);
            sb.append(", equivalentSpecifiers=[");
            for (WifiAwareNetworkSpecifier ns : this.equivalentSpecifiers) {
                sb.append(ns.toString());
                sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    static class CanonicalConnectionInfo {
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
            return (other.peerDiscoveryMac == null || Arrays.equals(this.peerDiscoveryMac, other.peerDiscoveryMac)) && Arrays.equals(this.pmk, other.pmk) && TextUtils.equals(this.passphrase, other.passphrase) && (TextUtils.isEmpty(this.passphrase) || this.sessionId == other.sessionId);
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder("CanonicalConnectionInfo: [");
            sb.append("peerDiscoveryMac=");
            if (this.peerDiscoveryMac == null) {
                str = "";
            } else {
                str = String.valueOf(HexEncoding.encode(this.peerDiscoveryMac));
            }
            sb.append(str);
            sb.append(", pmk=");
            sb.append(this.pmk == null ? "" : "*");
            sb.append(", sessionId=");
            sb.append(this.sessionId);
            sb.append(", passphrase=");
            sb.append(this.passphrase == null ? "" : "*");
            sb.append("]");
            return sb.toString();
        }
    }

    @VisibleForTesting
    public class NetworkInterfaceWrapper {
        public NetworkInterfaceWrapper() {
        }

        public boolean configureAgentProperties(AwareNetworkRequestInformation nnri, Set<WifiAwareNetworkSpecifier> networkSpecifiers, int ndpId, NetworkInfo networkInfo, NetworkCapabilities networkCapabilities, LinkProperties linkProperties) {
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
                networkCapabilities.setNetworkSpecifier(new WifiAwareAgentNetworkSpecifier((WifiAwareNetworkSpecifier[]) networkSpecifiers.toArray(new WifiAwareNetworkSpecifier[0])));
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
    }

    private class WifiAwareNetworkAgent extends NetworkAgent {
        private AwareNetworkRequestInformation mAwareNetworkRequestInfo;
        private NetworkInfo mNetworkInfo;
        final /* synthetic */ WifiAwareDataPathStateManager this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        WifiAwareNetworkAgent(WifiAwareDataPathStateManager wifiAwareDataPathStateManager, Looper looper, Context context, String logTag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, AwareNetworkRequestInformation anri) {
            super(looper, context, logTag, ni, nc, lp, score);
            this.this$0 = wifiAwareDataPathStateManager;
            this.mNetworkInfo = ni;
            this.mAwareNetworkRequestInfo = anri;
        }

        /* access modifiers changed from: protected */
        public void unwanted() {
            this.this$0.mMgr.endDataPath(this.mAwareNetworkRequestInfo.ndpId);
            this.mAwareNetworkRequestInfo.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
        }

        /* access modifiers changed from: package-private */
        public void reconfigureAgentAsDisconnected() {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, "");
            sendNetworkInfo(this.mNetworkInfo);
        }
    }

    private class WifiAwareNetworkFactory extends NetworkFactory {
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
                AwareNetworkRequestInformation nnri2 = AwareNetworkRequestInformation.processNetworkSpecifier(networkSpecifier, WifiAwareDataPathStateManager.this.mMgr, WifiAwareDataPathStateManager.this.mWifiPermissionsUtil, WifiAwareDataPathStateManager.this.mPermissionsWrapper, WifiAwareDataPathStateManager.this.mAllowNdpResponderFromAnyOverride);
                if (nnri2 == null) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "WifiAwareNetworkFactory.acceptRequest: request=" + request + " - can't parse network specifier");
                    return false;
                }
                Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> primaryRequest = WifiAwareDataPathStateManager.this.getNetworkRequestByCanonicalDescriptor(nnri2.getCanonicalDescriptor());
                if (primaryRequest != null) {
                    if (primaryRequest.getValue().state == 106) {
                        this.mWaitingForTermination = true;
                    } else {
                        primaryRequest.getValue().updateToSupportNewRequest(networkSpecifier);
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
                        return;
                    }
                    WifiAwareDataPathStateManager.this.mMgr.initiateDataPathSetup(networkSpecifier, nnri.peerInstanceId, 0, WifiAwareDataPathStateManager.this.selectChannelForRequest(nnri), nnri.peerDiscoveryMac, nnri.interfaceName, nnri.networkSpecifier.pmk, nnri.networkSpecifier.passphrase, nnri.networkSpecifier.isOutOfBand());
                    nnri.state = 103;
                    nnri.startTimestamp = SystemClock.elapsedRealtime();
                } else {
                    nnri.state = ISupplicantStaIfaceCallback.StatusCode.ASSOC_DENIED_NO_VHT;
                }
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
                nnri.removeSupportForRequest(networkSpecifier);
                if (nnri.equivalentSpecifiers.isEmpty()) {
                    if (nnri.ndpId != 0) {
                        WifiAwareDataPathStateManager.this.mMgr.endDataPath(nnri.ndpId);
                        nnri.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
                    } else {
                        WifiAwareDataPathStateManager.this.mNetworkRequestsCache.remove(networkSpecifier);
                    }
                }
            }
        }
    }

    public WifiAwareDataPathStateManager(WifiAwareStateManager mgr) {
        this.mMgr = mgr;
    }

    public void start(Context context, Looper looper, WifiAwareMetrics awareMetrics, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper permissionsWrapper) {
        this.mContext = context;
        this.mAwareMetrics = awareMetrics;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mPermissionsWrapper = permissionsWrapper;
        this.mLooper = looper;
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
    public Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> getNetworkRequestByCanonicalDescriptor(CanonicalConnectionInfo cci) {
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
            this.mMgr.endDataPath(ndpId);
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
        if (nnri.state != 103) {
            Log.w(TAG, "onDataPathInitiateFail: network request in incorrect state: state=" + nnri.state);
        }
        this.mAwareMetrics.recordNdpStatus(reason, networkSpecifier.isOutOfBand(), nnri.startTimestamp);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v9, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.net.wifi.aware.WifiAwareNetworkSpecifier} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v10, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: com.android.server.wifi.aware.WifiAwareDataPathStateManager$AwareNetworkRequestInformation} */
    /* JADX WARNING: Multi-variable type inference failed */
    public WifiAwareNetworkSpecifier onDataPathRequest(int pubSubId, byte[] mac, int ndpId) {
        WifiAwareNetworkSpecifier networkSpecifier = null;
        AwareNetworkRequestInformation nnri = null;
        Iterator<Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation>> it = this.mNetworkRequestsCache.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> entry = it.next();
            if ((entry.getValue().pubSubId == 0 || entry.getValue().pubSubId == pubSubId) && ((entry.getValue().peerDiscoveryMac == null || Arrays.equals(entry.getValue().peerDiscoveryMac, mac)) && entry.getValue().state == 104)) {
                networkSpecifier = entry.getKey();
                nnri = entry.getValue();
                break;
            }
        }
        if (nnri == null) {
            Log.w(TAG, "onDataPathRequest: can't find a request with specified pubSubId=" + pubSubId + ", mac=" + String.valueOf(HexEncoding.encode(mac)));
            this.mMgr.respondToDataPathRequest(false, ndpId, "", null, null, false);
            return null;
        }
        if (nnri.peerDiscoveryMac == null) {
            nnri.peerDiscoveryMac = mac;
        }
        nnri.interfaceName = selectInterfaceForRequest(nnri);
        if (nnri.interfaceName == null) {
            Log.w(TAG, "onDataPathRequest: request " + networkSpecifier + " no interface available");
            this.mMgr.respondToDataPathRequest(false, ndpId, "", null, null, false);
            this.mNetworkRequestsCache.remove(networkSpecifier);
            return null;
        }
        nnri.state = ISupplicantStaIfaceCallback.StatusCode.ENABLEMENT_DENIED;
        nnri.ndpId = ndpId;
        nnri.startTimestamp = SystemClock.elapsedRealtime();
        this.mMgr.respondToDataPathRequest(true, ndpId, nnri.interfaceName, nnri.networkSpecifier.pmk, nnri.networkSpecifier.passphrase, nnri.networkSpecifier.isOutOfBand());
        return networkSpecifier;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v13, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.net.wifi.aware.WifiAwareNetworkSpecifier} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v14, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: com.android.server.wifi.aware.WifiAwareDataPathStateManager$AwareNetworkRequestInformation} */
    /* JADX WARNING: Multi-variable type inference failed */
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
            this.mAwareMetrics.recordNdpStatus(reasonOnFailure, networkSpecifier.isOutOfBand(), nnri.startTimestamp);
        } else if (nnri.state != 105) {
            Log.w(TAG, "onRespondToDataPathRequest: request " + networkSpecifier + " is incorrect state=" + nnri.state);
            this.mMgr.endDataPath(ndpId);
            this.mNetworkRequestsCache.remove(networkSpecifier);
        } else {
            nnri.state = 101;
        }
    }

    public WifiAwareNetworkSpecifier onDataPathConfirm(int ndpId, byte[] mac, boolean accept, int reason, byte[] message, List<NanDataPathChannelInfo> channelInfo) {
        WifiAwareNetworkSpecifier networkSpecifier;
        int i = ndpId;
        Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> nnriE = getNetworkRequestByNdpId(ndpId);
        if (nnriE == null) {
            Log.w(TAG, "onDataPathConfirm: network request not found for ndpId=" + i);
            if (accept) {
                this.mMgr.endDataPath(i);
            }
            return null;
        }
        WifiAwareNetworkSpecifier networkSpecifier2 = nnriE.getKey();
        AwareNetworkRequestInformation nnri = nnriE.getValue();
        if (nnri.state != 101) {
            Log.w(TAG, "onDataPathConfirm: invalid state=" + nnri.state);
            this.mNetworkRequestsCache.remove(networkSpecifier2);
            if (accept) {
                this.mMgr.endDataPath(i);
            }
            return networkSpecifier2;
        }
        if (accept) {
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
                    Log.e(TAG, "onDataPathConfirm: ACCEPT nnri=" + nnri + ": can't configure network - " + e);
                    this.mMgr.endDataPath(i);
                    nnri.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
                    return networkSpecifier2;
                }
            }
            if (!this.mNiWrapper.configureAgentProperties(nnri, nnri.equivalentSpecifiers, i, networkInfo, networkCapabilities, linkProperties)) {
                return networkSpecifier2;
            }
            Looper looper = this.mLooper;
            Context context = this.mContext;
            AwareNetworkRequestInformation nnri2 = nnri;
            networkSpecifier = networkSpecifier2;
            Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> entry = nnriE;
            WifiAwareNetworkAgent wifiAwareNetworkAgent = new WifiAwareNetworkAgent(this, looper, context, AGENT_TAG_PREFIX + nnri.ndpId, new NetworkInfo(-1, 0, NETWORK_TAG, ""), networkCapabilities, linkProperties, 1, nnri2);
            nnri2.networkAgent = wifiAwareNetworkAgent;
            nnri2.networkAgent.sendNetworkInfo(networkInfo);
            this.mAwareMetrics.recordNdpStatus(0, networkSpecifier.isOutOfBand(), nnri2.startTimestamp);
            nnri2.startTimestamp = SystemClock.elapsedRealtime();
            this.mAwareMetrics.recordNdpCreation(nnri2.uid, this.mNetworkRequestsCache);
            int i2 = reason;
        } else {
            networkSpecifier = networkSpecifier2;
            Map.Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> entry2 = nnriE;
            this.mNetworkRequestsCache.remove(networkSpecifier);
            this.mAwareMetrics.recordNdpStatus(reason, networkSpecifier.isOutOfBand(), nnri.startTimestamp);
        }
        return networkSpecifier;
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
        for (Integer intValue : ndpIds) {
            int ndpId = intValue.intValue();
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
            tearDownInterfaceIfPossible((AwareNetworkRequestInformation) it.next().getValue());
            it.remove();
        }
    }

    public void handleDataPathTimeout(NetworkSpecifier networkSpecifier) {
        if (this.mDbg) {
            Log.v(TAG, "handleDataPathTimeout: networkSpecifier=" + networkSpecifier);
        }
        AwareNetworkRequestInformation nnri = this.mNetworkRequestsCache.remove(networkSpecifier);
        if (nnri == null) {
            if (this.mDbg) {
                Log.v(TAG, "handleDataPathTimeout: network request not found for networkSpecifier=" + networkSpecifier);
            }
            return;
        }
        this.mAwareMetrics.recordNdpStatus(1, nnri.networkSpecifier.isOutOfBand(), nnri.startTimestamp);
        this.mMgr.endDataPath(nnri.ndpId);
        nnri.state = ISupplicantStaIfaceCallback.StatusCode.RESTRICTION_FROM_AUTHORIZED_GDB;
    }

    private void tearDownInterfaceIfPossible(AwareNetworkRequestInformation nnri) {
        if (!TextUtils.isEmpty(nnri.interfaceName) && !isInterfaceUpAndUsedByAnotherNdp(nnri)) {
            try {
                this.mNwService.setInterfaceDown(nnri.interfaceName);
            } catch (Exception e) {
                Log.e(TAG, "tearDownInterfaceIfPossible: nnri=" + nnri + ": can't bring interface down - " + e);
            }
        }
        if (nnri.networkAgent != null) {
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
    public String selectInterfaceForRequest(AwareNetworkRequestInformation req) {
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
    public int selectChannelForRequest(AwareNetworkRequestInformation req) {
        return 2437;
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
