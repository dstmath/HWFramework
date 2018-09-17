package com.android.server.wifi.aware;

import android.content.Context;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.MatchAllNetworkSpecifier;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.RouteInfo;
import android.net.wifi.aware.WifiAwareNetworkSpecifier;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Looper;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;
import com.android.server.wifi.HwWifiCHRConst;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import libcore.util.HexEncoding;

public class WifiAwareDataPathStateManager {
    private static final String AGENT_TAG_PREFIX = "WIFI_AWARE_AGENT_";
    private static final String AWARE_INTERFACE_PREFIX = "aware_data";
    private static final boolean DBG = false;
    private static final int NETWORK_FACTORY_BANDWIDTH_AVAIL = 1;
    private static final int NETWORK_FACTORY_SCORE_AVAIL = 1;
    private static final int NETWORK_FACTORY_SIGNAL_STRENGTH_AVAIL = 1;
    private static final String NETWORK_TAG = "WIFI_AWARE_FACTORY";
    private static final String TAG = "WifiAwareDataPathStMgr";
    private static final boolean VDBG = false;
    private Context mContext;
    private final Set<String> mInterfaces = new HashSet();
    private Looper mLooper;
    private final WifiAwareStateManager mMgr;
    private final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();
    private WifiAwareNetworkFactory mNetworkFactory;
    private final Map<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> mNetworkRequestsCache = new ArrayMap();
    private final NetworkInterfaceWrapper mNiWrapper = new NetworkInterfaceWrapper();
    private INetworkManagementService mNwService;

    public static class AwareNetworkRequestInformation {
        static final int STATE_INITIATOR_CONFIRMED = 103;
        static final int STATE_INITIATOR_IDLE = 100;
        static final int STATE_INITIATOR_WAIT_FOR_CONFIRM = 102;
        static final int STATE_INITIATOR_WAIT_FOR_REQUEST_RESPONSE = 101;
        static final int STATE_RESPONDER_CONFIRMED = 204;
        static final int STATE_RESPONDER_IDLE = 200;
        static final int STATE_RESPONDER_WAIT_FOR_CONFIRM = 203;
        static final int STATE_RESPONDER_WAIT_FOR_REQUEST = 201;
        static final int STATE_RESPONDER_WAIT_FOR_RESPOND_RESPONSE = 202;
        public String interfaceName;
        public int ndpId;
        public WifiAwareNetworkAgent networkAgent;
        public WifiAwareNetworkSpecifier networkSpecifier;
        public byte[] peerDataMac;
        public byte[] peerDiscoveryMac = null;
        public int pubSubId = 0;
        public int state;
        public int uid;

        static AwareNetworkRequestInformation processNetworkSpecifier(WifiAwareNetworkSpecifier ns, WifiAwareStateManager mgr) {
            int pubSubId = 0;
            byte[] peerMac = ns.peerMac;
            if (ns.type < 0 || ns.type > 3) {
                Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + ", invalid 'type' value");
                return null;
            } else if (ns.role != 0 && ns.role != 1) {
                Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid 'role' value");
                return null;
            } else if (ns.role != 0 || ns.type == 0 || ns.type == 2) {
                WifiAwareClientState client = mgr.getClient(ns.clientId);
                if (client == null) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- not client with this id -- clientId=" + ns.clientId);
                    return null;
                }
                int i;
                int uid = client.getUid();
                if (ns.type == 0 || ns.type == 1) {
                    WifiAwareDiscoverySessionState session = client.getSession(ns.sessionId);
                    if (session == null) {
                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- no session with this id -- sessionId=" + ns.sessionId);
                        return null;
                    } else if ((session.isPublishSession() && ns.role != 1) || (!session.isPublishSession() && ns.role != 0)) {
                        Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid role for session type");
                        return null;
                    } else if (ns.type == 0) {
                        pubSubId = session.getPubSubId();
                        String peerMacStr = session.getMac(ns.peerId, null);
                        if (peerMacStr == null) {
                            Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- no MAC address associated with this peer id -- peerId=" + ns.peerId);
                            return null;
                        }
                        try {
                            peerMac = HexEncoding.decode(peerMacStr.toCharArray(), false);
                            if (peerMac == null || peerMac.length != 6) {
                                Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid peer MAC address");
                                return null;
                            }
                        } catch (IllegalArgumentException e) {
                            Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid peer MAC address -- e=" + e);
                            return null;
                        }
                    }
                }
                AwareNetworkRequestInformation nnri = new AwareNetworkRequestInformation();
                if (ns.role == 0) {
                    i = 100;
                } else {
                    i = 200;
                }
                nnri.state = i;
                nnri.uid = uid;
                nnri.pubSubId = pubSubId;
                nnri.peerDiscoveryMac = peerMac;
                nnri.networkSpecifier = ns;
                return nnri;
            } else {
                Log.e(WifiAwareDataPathStateManager.TAG, "processNetworkSpecifier: networkSpecifier=" + ns + " -- invalid 'type' value for INITIATOR (only IB and OOB are " + "permitted)");
                return null;
            }
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder("AwareNetworkRequestInformation: ");
            StringBuilder append = sb.append("state=").append(this.state).append(", ns=").append(this.networkSpecifier).append(", uid=").append(this.uid).append(", interfaceName=").append(this.interfaceName).append(", pubSubId=").append(this.pubSubId).append(", peerDiscoveryMac=");
            if (this.peerDiscoveryMac == null) {
                str = "";
            } else {
                str = String.valueOf(HexEncoding.encode(this.peerDiscoveryMac));
            }
            append = append.append(str).append(", ndpId=").append(this.ndpId).append(", peerDataMac=");
            if (this.peerDataMac == null) {
                str = "";
            } else {
                str = String.valueOf(HexEncoding.encode(this.peerDataMac));
            }
            append.append(str);
            return sb.toString();
        }
    }

    public class NetworkInterfaceWrapper {
        public boolean configureAgentProperties(AwareNetworkRequestInformation nnri, WifiAwareNetworkSpecifier networkSpecifier, int ndpId, NetworkInfo networkInfo, NetworkCapabilities networkCapabilities, LinkProperties linkProperties) {
            InetAddress linkLocal = null;
            try {
                NetworkInterface ni = NetworkInterface.getByName(nnri.interfaceName);
                if (ni == null) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "onDataPathConfirm: ACCEPT nnri=" + nnri + ": can't get network interface (null)");
                    WifiAwareDataPathStateManager.this.mMgr.endDataPath(ndpId);
                    return false;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    if ((ip instanceof Inet6Address) && ip.isLinkLocalAddress()) {
                        linkLocal = ip;
                        break;
                    }
                }
                if (linkLocal == null) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "onDataPathConfirm: ACCEPT nnri=" + nnri + ": no link local addresses");
                    WifiAwareDataPathStateManager.this.mMgr.endDataPath(ndpId);
                    return false;
                }
                networkInfo.setIsAvailable(true);
                networkInfo.setDetailedState(DetailedState.CONNECTED, null, null);
                networkCapabilities.setNetworkSpecifier(networkSpecifier);
                linkProperties.setInterfaceName(nnri.interfaceName);
                linkProperties.addLinkAddress(new LinkAddress(linkLocal, 64));
                linkProperties.addRoute(new RouteInfo(new IpPrefix("fe80::/64"), null, nnri.interfaceName));
                return true;
            } catch (SocketException e) {
                Log.e(WifiAwareDataPathStateManager.TAG, "onDataPathConfirm: ACCEPT nnri=" + nnri + ": can't get network interface - " + e);
                WifiAwareDataPathStateManager.this.mMgr.endDataPath(ndpId);
                return false;
            }
        }
    }

    private class WifiAwareNetworkAgent extends NetworkAgent {
        private int mNdpId;
        private NetworkInfo mNetworkInfo;
        private WifiAwareNetworkSpecifier mNetworkSpecifier;

        WifiAwareNetworkAgent(Looper looper, Context context, String logTag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, WifiAwareNetworkSpecifier networkSpecifier, int ndpId) {
            super(looper, context, logTag, ni, nc, lp, score);
            this.mNetworkInfo = ni;
            this.mNetworkSpecifier = networkSpecifier;
            this.mNdpId = ndpId;
        }

        protected void unwanted() {
            WifiAwareDataPathStateManager.this.mMgr.endDataPath(this.mNdpId);
        }

        void reconfigureAgentAsDisconnected() {
            this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, "");
            sendNetworkInfo(this.mNetworkInfo);
        }
    }

    private class WifiAwareNetworkFactory extends NetworkFactory {
        WifiAwareNetworkFactory(Looper looper, Context context, NetworkCapabilities filter) {
            super(looper, context, WifiAwareDataPathStateManager.NETWORK_TAG, filter);
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
            if (networkSpecifierBase instanceof WifiAwareNetworkSpecifier) {
                WifiAwareNetworkSpecifier networkSpecifier = (WifiAwareNetworkSpecifier) networkSpecifierBase;
                if (((AwareNetworkRequestInformation) WifiAwareDataPathStateManager.this.mNetworkRequestsCache.get(networkSpecifier)) != null) {
                    return true;
                }
                AwareNetworkRequestInformation nnri = AwareNetworkRequestInformation.processNetworkSpecifier(networkSpecifier, WifiAwareDataPathStateManager.this.mMgr);
                if (nnri == null) {
                    Log.e(WifiAwareDataPathStateManager.TAG, "WifiAwareNetworkFactory.acceptRequest: request=" + request + " - can't parse network specifier");
                    return false;
                }
                WifiAwareDataPathStateManager.this.mNetworkRequestsCache.put(networkSpecifier, nnri);
                return true;
            }
            Log.w(WifiAwareDataPathStateManager.TAG, "WifiAwareNetworkFactory.acceptRequest: request=" + request + " - not a WifiAwareNetworkSpecifier");
            return false;
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            NetworkSpecifier networkSpecifierObj = networkRequest.networkCapabilities.getNetworkSpecifier();
            WifiAwareNetworkSpecifier networkSpecifier = null;
            if (networkSpecifierObj instanceof WifiAwareNetworkSpecifier) {
                networkSpecifier = (WifiAwareNetworkSpecifier) networkSpecifierObj;
            }
            AwareNetworkRequestInformation nnri = (AwareNetworkRequestInformation) WifiAwareDataPathStateManager.this.mNetworkRequestsCache.get(networkSpecifier);
            if (nnri == null) {
                Log.e(WifiAwareDataPathStateManager.TAG, "WifiAwareNetworkFactory.needNetworkFor: networkRequest=" + networkRequest + " not in cache!?");
                return;
            }
            if (nnri.networkSpecifier.role == 0) {
                if (nnri.state == 100) {
                    nnri.interfaceName = WifiAwareDataPathStateManager.this.selectInterfaceForRequest(nnri);
                    WifiAwareDataPathStateManager.this.mMgr.initiateDataPathSetup(networkSpecifier, nnri.networkSpecifier.peerId, 1, WifiAwareDataPathStateManager.this.selectChannelForRequest(nnri), nnri.peerDiscoveryMac, nnri.interfaceName, nnri.networkSpecifier.pmk, nnri.networkSpecifier.passphrase);
                    nnri.state = 101;
                }
            } else if (nnri.state == 200) {
                nnri.state = HwWifiCHRConst.CHR_GNSS_HAL_EVENT_SYSCALL;
            }
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            NetworkSpecifier networkSpecifierObj = networkRequest.networkCapabilities.getNetworkSpecifier();
            Object networkSpecifier = null;
            if (networkSpecifierObj instanceof WifiAwareNetworkSpecifier) {
                networkSpecifier = (WifiAwareNetworkSpecifier) networkSpecifierObj;
            }
            AwareNetworkRequestInformation nnri = (AwareNetworkRequestInformation) WifiAwareDataPathStateManager.this.mNetworkRequestsCache.get(networkSpecifier);
            if (nnri == null) {
                Log.e(WifiAwareDataPathStateManager.TAG, "WifiAwareNetworkFactory.releaseNetworkFor: networkRequest=" + networkRequest + " not in cache!?");
            } else if (nnri.networkAgent == null) {
                if (nnri.networkSpecifier.role == 0 && nnri.state > 101) {
                    WifiAwareDataPathStateManager.this.mMgr.endDataPath(nnri.ndpId);
                }
                if (nnri.networkSpecifier.role == 1 && nnri.state > HwWifiCHRConst.CHR_GNSS_HAL_EVENT_SYSCALL) {
                    WifiAwareDataPathStateManager.this.mMgr.endDataPath(nnri.ndpId);
                }
            }
        }
    }

    public WifiAwareDataPathStateManager(WifiAwareStateManager mgr) {
        this.mMgr = mgr;
    }

    public void start(Context context, Looper looper) {
        this.mContext = context;
        this.mLooper = looper;
        this.mNetworkCapabilitiesFilter.clearAll();
        this.mNetworkCapabilitiesFilter.addTransportType(5);
        this.mNetworkCapabilitiesFilter.addCapability(15).addCapability(11).addCapability(13).addCapability(14);
        this.mNetworkCapabilitiesFilter.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        this.mNetworkCapabilitiesFilter.setLinkUpstreamBandwidthKbps(1);
        this.mNetworkCapabilitiesFilter.setLinkDownstreamBandwidthKbps(1);
        this.mNetworkCapabilitiesFilter.setSignalStrength(1);
        this.mNetworkFactory = new WifiAwareNetworkFactory(looper, context, this.mNetworkCapabilitiesFilter);
        this.mNetworkFactory.setScoreFilter(1);
        this.mNetworkFactory.register();
        this.mNwService = Stub.asInterface(ServiceManager.getService("network_management"));
    }

    private Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> getNetworkRequestByNdpId(int ndpId) {
        for (Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> entry : this.mNetworkRequestsCache.entrySet()) {
            if (((AwareNetworkRequestInformation) entry.getValue()).ndpId == ndpId) {
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
        for (String name : this.mInterfaces) {
            this.mMgr.deleteDataPathInterface(name);
        }
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
        AwareNetworkRequestInformation nnri = (AwareNetworkRequestInformation) this.mNetworkRequestsCache.get(networkSpecifier);
        if (nnri == null) {
            Log.w(TAG, "onDataPathInitiateSuccess: network request not found for networkSpecifier=" + networkSpecifier);
            this.mMgr.endDataPath(ndpId);
        } else if (nnri.state != 101) {
            Log.w(TAG, "onDataPathInitiateSuccess: network request in incorrect state: state=" + nnri.state);
            this.mNetworkRequestsCache.remove(networkSpecifier);
            this.mMgr.endDataPath(ndpId);
        } else {
            nnri.state = 102;
            nnri.ndpId = ndpId;
        }
    }

    public void onDataPathInitiateFail(WifiAwareNetworkSpecifier networkSpecifier, int reason) {
        AwareNetworkRequestInformation nnri = (AwareNetworkRequestInformation) this.mNetworkRequestsCache.remove(networkSpecifier);
        if (nnri == null) {
            Log.w(TAG, "onDataPathInitiateFail: network request not found for networkSpecifier=" + networkSpecifier);
            return;
        }
        if (nnri.state != 101) {
            Log.w(TAG, "onDataPathInitiateFail: network request in incorrect state: state=" + nnri.state);
        }
        this.mNetworkRequestsCache.remove(networkSpecifier);
    }

    public WifiAwareNetworkSpecifier onDataPathRequest(int pubSubId, byte[] mac, int ndpId) {
        WifiAwareNetworkSpecifier networkSpecifier = null;
        AwareNetworkRequestInformation awareNetworkRequestInformation = null;
        for (Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> entry : this.mNetworkRequestsCache.entrySet()) {
            if ((((AwareNetworkRequestInformation) entry.getValue()).pubSubId == 0 || ((AwareNetworkRequestInformation) entry.getValue()).pubSubId == pubSubId) && (((AwareNetworkRequestInformation) entry.getValue()).peerDiscoveryMac == null || (Arrays.equals(((AwareNetworkRequestInformation) entry.getValue()).peerDiscoveryMac, mac) ^ 1) == 0)) {
                networkSpecifier = (WifiAwareNetworkSpecifier) entry.getKey();
                awareNetworkRequestInformation = (AwareNetworkRequestInformation) entry.getValue();
                break;
            }
        }
        if (awareNetworkRequestInformation == null) {
            Log.w(TAG, "onDataPathRequest: can't find a request with specified pubSubId=" + pubSubId + ", mac=" + String.valueOf(HexEncoding.encode(mac)));
            this.mMgr.respondToDataPathRequest(false, ndpId, "", null, null);
            return null;
        } else if (awareNetworkRequestInformation.state != HwWifiCHRConst.CHR_GNSS_HAL_EVENT_SYSCALL) {
            Log.w(TAG, "onDataPathRequest: request " + networkSpecifier + " is incorrect state=" + awareNetworkRequestInformation.state);
            this.mMgr.respondToDataPathRequest(false, ndpId, "", null, null);
            this.mNetworkRequestsCache.remove(networkSpecifier);
            return null;
        } else {
            awareNetworkRequestInformation.state = HwWifiCHRConst.CHR_GNSS_HAL_EVENT_EXCEPTION;
            awareNetworkRequestInformation.ndpId = ndpId;
            awareNetworkRequestInformation.interfaceName = selectInterfaceForRequest(awareNetworkRequestInformation);
            this.mMgr.respondToDataPathRequest(true, ndpId, awareNetworkRequestInformation.interfaceName, awareNetworkRequestInformation.networkSpecifier.pmk, awareNetworkRequestInformation.networkSpecifier.passphrase);
            return networkSpecifier;
        }
    }

    public void onRespondToDataPathRequest(int ndpId, boolean success) {
        Object networkSpecifier = null;
        AwareNetworkRequestInformation awareNetworkRequestInformation = null;
        for (Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> entry : this.mNetworkRequestsCache.entrySet()) {
            if (((AwareNetworkRequestInformation) entry.getValue()).ndpId == ndpId) {
                networkSpecifier = (WifiAwareNetworkSpecifier) entry.getKey();
                awareNetworkRequestInformation = (AwareNetworkRequestInformation) entry.getValue();
                break;
            }
        }
        if (awareNetworkRequestInformation == null) {
            Log.w(TAG, "onRespondToDataPathRequest: can't find a request with specified ndpId=" + ndpId);
        } else if (!success) {
            Log.w(TAG, "onRespondToDataPathRequest: request " + networkSpecifier + " failed responding");
            this.mMgr.endDataPath(ndpId);
            this.mNetworkRequestsCache.remove(networkSpecifier);
        } else if (awareNetworkRequestInformation.state != HwWifiCHRConst.CHR_GNSS_HAL_EVENT_EXCEPTION) {
            Log.w(TAG, "onRespondToDataPathRequest: request " + networkSpecifier + " is incorrect state=" + awareNetworkRequestInformation.state);
            this.mMgr.endDataPath(ndpId);
            this.mNetworkRequestsCache.remove(networkSpecifier);
        } else {
            awareNetworkRequestInformation.state = HwWifiCHRConst.CHR_GNSS_HAL_EVENT_INJECT;
        }
    }

    public WifiAwareNetworkSpecifier onDataPathConfirm(int ndpId, byte[] mac, boolean accept, int reason, byte[] message) {
        Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> nnriE = getNetworkRequestByNdpId(ndpId);
        if (nnriE == null) {
            Log.w(TAG, "onDataPathConfirm: network request not found for ndpId=" + ndpId);
            if (accept) {
                this.mMgr.endDataPath(ndpId);
            }
            return null;
        }
        WifiAwareNetworkSpecifier networkSpecifier = (WifiAwareNetworkSpecifier) nnriE.getKey();
        AwareNetworkRequestInformation nnri = (AwareNetworkRequestInformation) nnriE.getValue();
        if (nnri.networkSpecifier.role == 0 && nnri.state != 102) {
            Log.w(TAG, "onDataPathConfirm: INITIATOR in invalid state=" + nnri.state);
            this.mNetworkRequestsCache.remove(networkSpecifier);
            if (accept) {
                this.mMgr.endDataPath(ndpId);
            }
            return networkSpecifier;
        } else if (nnri.networkSpecifier.role != 1 || nnri.state == HwWifiCHRConst.CHR_GNSS_HAL_EVENT_INJECT) {
            if (accept) {
                int i;
                if (nnri.networkSpecifier.role == 0) {
                    i = 103;
                } else {
                    i = 204;
                }
                nnri.state = i;
                nnri.peerDataMac = mac;
                NetworkInfo networkInfo = new NetworkInfo(13, 0, NETWORK_TAG, "");
                NetworkCapabilities networkCapabilities = new NetworkCapabilities(this.mNetworkCapabilitiesFilter);
                LinkProperties linkProperties = new LinkProperties();
                try {
                    this.mNwService.setInterfaceUp(nnri.interfaceName);
                    this.mNwService.enableIpv6(nnri.interfaceName);
                    if (!this.mNiWrapper.configureAgentProperties(nnri, networkSpecifier, ndpId, networkInfo, networkCapabilities, linkProperties)) {
                        return networkSpecifier;
                    }
                    nnri.networkAgent = new WifiAwareNetworkAgent(this.mLooper, this.mContext, AGENT_TAG_PREFIX + nnri.ndpId, new NetworkInfo(13, 0, NETWORK_TAG, ""), networkCapabilities, linkProperties, 1, networkSpecifier, ndpId);
                    nnri.networkAgent.sendNetworkInfo(networkInfo);
                } catch (Exception e) {
                    Log.e(TAG, "onDataPathConfirm: ACCEPT nnri=" + nnri + ": can't configure network - " + e);
                    this.mMgr.endDataPath(ndpId);
                    return networkSpecifier;
                }
            }
            this.mNetworkRequestsCache.remove(networkSpecifier);
            return networkSpecifier;
        } else {
            Log.w(TAG, "onDataPathConfirm: RESPONDER in invalid state=" + nnri.state);
            this.mNetworkRequestsCache.remove(networkSpecifier);
            if (accept) {
                this.mMgr.endDataPath(ndpId);
            }
            return networkSpecifier;
        }
    }

    public void onDataPathEnd(int ndpId) {
        Entry<WifiAwareNetworkSpecifier, AwareNetworkRequestInformation> nnriE = getNetworkRequestByNdpId(ndpId);
        if (nnriE != null) {
            tearDownInterface((AwareNetworkRequestInformation) nnriE.getValue());
            this.mNetworkRequestsCache.remove(nnriE.getKey());
        }
    }

    public void onAwareDownCleanupDataPaths() {
        for (AwareNetworkRequestInformation nnri : this.mNetworkRequestsCache.values()) {
            tearDownInterface(nnri);
        }
        this.mNetworkRequestsCache.clear();
    }

    public void handleDataPathTimeout(NetworkSpecifier networkSpecifier) {
        AwareNetworkRequestInformation nnri = (AwareNetworkRequestInformation) this.mNetworkRequestsCache.remove(networkSpecifier);
        if (nnri != null) {
            this.mMgr.endDataPath(nnri.ndpId);
        }
    }

    private void tearDownInterface(AwareNetworkRequestInformation nnri) {
        if (!(nnri.interfaceName == null || (nnri.interfaceName.isEmpty() ^ 1) == 0)) {
            try {
                this.mNwService.setInterfaceDown(nnri.interfaceName);
            } catch (Exception e) {
                Log.e(TAG, "tearDownInterface: nnri=" + nnri + ": can't bring interface down - " + e);
            }
        }
        if (nnri.networkAgent != null) {
            nnri.networkAgent.reconfigureAgentAsDisconnected();
        }
    }

    private String selectInterfaceForRequest(AwareNetworkRequestInformation req) {
        Iterator<String> it = this.mInterfaces.iterator();
        if (it.hasNext()) {
            return (String) it.next();
        }
        Log.e(TAG, "selectInterfaceForRequest: req=" + req + " - but no interfaces available!");
        return "";
    }

    private int selectChannelForRequest(AwareNetworkRequestInformation req) {
        return 2437;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("WifiAwareDataPathStateManager:");
        pw.println("  mInterfaces: " + this.mInterfaces);
        pw.println("  mNetworkCapabilitiesFilter: " + this.mNetworkCapabilitiesFilter);
        pw.println("  mNetworkRequestsCache: " + this.mNetworkRequestsCache);
        pw.println("  mNetworkFactory:");
        this.mNetworkFactory.dump(fd, pw, args);
    }
}
