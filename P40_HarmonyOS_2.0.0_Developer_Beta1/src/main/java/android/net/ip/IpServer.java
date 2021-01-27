package android.net.ip;

import android.net.INetd;
import android.net.INetworkStackStatusCallback;
import android.net.INetworkStatsService;
import android.net.InterfaceConfiguration;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkStackClient;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.dhcp.DhcpServerCallbacks;
import android.net.dhcp.DhcpServingParamsParcel;
import android.net.dhcp.DhcpServingParamsParcelExt;
import android.net.dhcp.IDhcpServer;
import android.net.ip.IpServer;
import android.net.ip.RouterAdvertisementDaemon;
import android.net.util.InterfaceParams;
import android.net.util.InterfaceSet;
import android.net.util.NetdService;
import android.net.util.NetworkConstants;
import android.net.util.SharedLog;
import android.os.Build;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class IpServer extends StateMachine {
    public static final int AVOID_STOP_INTERFACE = 1;
    private static final int BASE_IFACE = 327780;
    private static final int BLUETOOTH_DHCP_PREFIX_LENGTH = 24;
    private static final String BLUETOOTH_IFACE_ADDR = "192.168.44.1";
    public static final int CMD_INTERFACE_DOWN = 327784;
    public static final int CMD_IPV6_TETHER_UPDATE = 327793;
    public static final int CMD_IP_FORWARDING_DISABLE_ERROR = 327788;
    public static final int CMD_IP_FORWARDING_ENABLE_ERROR = 327787;
    public static final int CMD_SET_DNS_FORWARDERS_ERROR = 327791;
    public static final int CMD_START_TETHERING_ERROR = 327789;
    public static final int CMD_STOP_TETHERING_ERROR = 327790;
    public static final int CMD_TETHER_CONNECTION_CHANGED = 327792;
    public static final int CMD_TETHER_REQUESTED = 327782;
    public static final int CMD_TETHER_UNREQUESTED = 327783;
    private static final boolean DBG = false;
    private static final int DHCP_LEASE_TIME_SECS = 3600;
    private static final byte DOUG_ADAMS = 42;
    private static final boolean HW_DEBUG = Build.IS_DEBUGGABLE;
    public static final int STATE_AVAILABLE = 1;
    public static final int STATE_LOCAL_ONLY = 3;
    public static final int STATE_TETHERED = 2;
    public static final int STATE_UNAVAILABLE = 0;
    private static final String TAG = "IpServer";
    private static final String USB_NEAR_IFACE_ADDR = "192.168.42.129";
    private static final int USB_PREFIX_LENGTH = 24;
    private static final boolean VDBG = false;
    private static final String WIFI_HOST_IFACE_ADDR = "192.168.43.1";
    private static final int WIFI_HOST_IFACE_PREFIX_LENGTH = 24;
    private static final Class[] messageClasses = {IpServer.class};
    private static final SparseArray<String> sMagicDecoderRing = MessageUtils.findMessageNames(messageClasses);
    private boolean isFixed = false;
    private final Callback mCallback;
    private final Dependencies mDeps;
    private IDhcpServer mDhcpServer;
    private int mDhcpServerStartIndex = 0;
    private final String mIfaceName;
    private final State mInitialState;
    private final InterfaceController mInterfaceCtrl;
    private InterfaceParams mInterfaceParams;
    private final int mInterfaceType;
    private int mLastError;
    private LinkProperties mLastIPv6LinkProperties;
    private RouterAdvertisementDaemon.RaParams mLastRaParams;
    private final LinkProperties mLinkProperties;
    private final State mLocalHotspotState;
    private final SharedLog mLog;
    private final INetworkManagementService mNMService;
    private final INetd mNetd;
    private RouterAdvertisementDaemon mRaDaemon;
    private int mServingMode;
    private final INetworkStatsService mStatsService;
    private final State mTetheredState;
    private final State mUnavailableState;
    private InterfaceSet mUpstreamIfaceSet;
    private final boolean mUsingLegacyDhcp;

    public static String getStateString(int state) {
        if (state == 0) {
            return "UNAVAILABLE";
        }
        if (state == 1) {
            return "AVAILABLE";
        }
        if (state == 2) {
            return "TETHERED";
        }
        if (state == 3) {
            return "LOCAL_ONLY";
        }
        return "UNKNOWN: " + state;
    }

    public static class Callback {
        public void updateInterfaceState(IpServer who, int state, int lastError) {
        }

        public void updateLinkProperties(IpServer who, LinkProperties newLp) {
        }
    }

    public static class Dependencies {
        public RouterAdvertisementDaemon getRouterAdvertisementDaemon(InterfaceParams ifParams) {
            return new RouterAdvertisementDaemon(ifParams);
        }

        public InterfaceParams getInterfaceParams(String ifName) {
            return InterfaceParams.getByName(ifName);
        }

        public INetd getNetdService() {
            return NetdService.getInstance();
        }

        public void makeDhcpServer(String ifName, DhcpServingParamsParcel params, DhcpServerCallbacks cb) {
            NetworkStackClient.getInstance().makeDhcpServer(ifName, params, cb);
        }
    }

    public IpServer(String ifaceName, Looper looper, int interfaceType, SharedLog log, INetworkManagementService nMService, INetworkStatsService statsService, Callback callback, boolean usingLegacyDhcp, Dependencies deps) {
        super(ifaceName, looper);
        this.mLog = log.forSubComponent(ifaceName);
        this.mNMService = nMService;
        this.mNetd = deps.getNetdService();
        this.mStatsService = statsService;
        this.mCallback = callback;
        this.mInterfaceCtrl = new InterfaceController(ifaceName, this.mNetd, this.mLog);
        this.mIfaceName = ifaceName;
        this.mInterfaceType = interfaceType;
        this.mLinkProperties = new LinkProperties();
        this.mUsingLegacyDhcp = usingLegacyDhcp;
        this.mDeps = deps;
        resetLinkProperties();
        this.mLastError = 0;
        this.mServingMode = 1;
        this.mInitialState = new InitialState();
        this.mLocalHotspotState = new LocalHotspotState();
        this.mTetheredState = new TetheredState();
        this.mUnavailableState = new UnavailableState();
        addState(this.mInitialState);
        addState(this.mLocalHotspotState);
        addState(this.mTetheredState);
        addState(this.mUnavailableState);
        setInitialState(this.mInitialState);
    }

    public String interfaceName() {
        return this.mIfaceName;
    }

    public int interfaceType() {
        return this.mInterfaceType;
    }

    public int lastError() {
        return this.mLastError;
    }

    public int servingMode() {
        return this.mServingMode;
    }

    public LinkProperties linkProperties() {
        return new LinkProperties(this.mLinkProperties);
    }

    public void stop() {
        sendMessage(CMD_INTERFACE_DOWN);
    }

    public void unwanted() {
        sendMessage(CMD_TETHER_UNREQUESTED);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean startIPv4() {
        return configureIPv4(true);
    }

    /* access modifiers changed from: private */
    public abstract class OnHandlerStatusCallback extends INetworkStackStatusCallback.Stub {
        /* renamed from: callback */
        public abstract void lambda$onStatusAvailable$0$IpServer$OnHandlerStatusCallback(int i);

        private OnHandlerStatusCallback() {
        }

        @Override // android.net.INetworkStackStatusCallback
        public void onStatusAvailable(int statusCode) {
            IpServer.this.getHandler().post(new Runnable(statusCode) {
                /* class android.net.ip.$$Lambda$IpServer$OnHandlerStatusCallback$czoKoFzZQJY8J5O14qT9czTIoo */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    IpServer.OnHandlerStatusCallback.this.lambda$onStatusAvailable$0$IpServer$OnHandlerStatusCallback(this.f$1);
                }
            });
        }

        @Override // android.net.INetworkStackStatusCallback
        public int getInterfaceVersion() {
            return 3;
        }
    }

    /* access modifiers changed from: private */
    public class DhcpServerCallbacksImpl extends DhcpServerCallbacks {
        private final int mStartIndex;

        private DhcpServerCallbacksImpl(int startIndex) {
            this.mStartIndex = startIndex;
        }

        @Override // android.net.dhcp.IDhcpServerCallbacks
        public void onDhcpServerCreated(int statusCode, IDhcpServer server) throws RemoteException {
            IpServer.this.getHandler().post(new Runnable(statusCode, server) {
                /* class android.net.ip.$$Lambda$IpServer$DhcpServerCallbacksImpl$nBlfeyPZEu2j0KBs4BJklDJTve4 */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ IDhcpServer f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    IpServer.DhcpServerCallbacksImpl.this.lambda$onDhcpServerCreated$0$IpServer$DhcpServerCallbacksImpl(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onDhcpServerCreated$0$IpServer$DhcpServerCallbacksImpl(int statusCode, IDhcpServer server) {
            if (this.mStartIndex == IpServer.this.mDhcpServerStartIndex) {
                if (statusCode != 1) {
                    SharedLog sharedLog = IpServer.this.mLog;
                    sharedLog.e("Error obtaining DHCP server: " + statusCode);
                    handleError();
                    return;
                }
                IpServer.this.mDhcpServer = server;
                try {
                    IpServer.this.mDhcpServer.start(new OnHandlerStatusCallback() {
                        /* class android.net.ip.IpServer.DhcpServerCallbacksImpl.AnonymousClass1 */

                        {
                            IpServer ipServer = IpServer.this;
                        }

                        @Override // android.net.ip.IpServer.OnHandlerStatusCallback
                        public void callback(int startStatusCode) {
                            if (startStatusCode != 1) {
                                SharedLog sharedLog = IpServer.this.mLog;
                                sharedLog.e("Error starting DHCP server: " + startStatusCode);
                                DhcpServerCallbacksImpl.this.handleError();
                            }
                        }
                    });
                } catch (RemoteException e) {
                    e.rethrowFromSystemServer();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleError() {
            IpServer.this.mLastError = 12;
            IpServer ipServer = IpServer.this;
            ipServer.transitionTo(ipServer.mInitialState);
        }
    }

    private boolean startDhcp(Inet4Address addr, int prefixLen) {
        if (this.mUsingLegacyDhcp) {
            return true;
        }
        DhcpServingParamsParcel params = new DhcpServingParamsParcelExt().setDefaultRouters(addr).setDhcpLeaseTimeSecs(3600).setDnsServers(addr).setServerAddr(new LinkAddress(addr, prefixLen)).setMetered(true);
        this.mDhcpServerStartIndex++;
        this.mDeps.makeDhcpServer(this.mIfaceName, params, new DhcpServerCallbacksImpl(this.mDhcpServerStartIndex));
        return true;
    }

    private void stopDhcp() {
        this.mDhcpServerStartIndex++;
        IDhcpServer iDhcpServer = this.mDhcpServer;
        if (iDhcpServer != null) {
            try {
                iDhcpServer.stop(new OnHandlerStatusCallback() {
                    /* class android.net.ip.IpServer.AnonymousClass1 */

                    @Override // android.net.ip.IpServer.OnHandlerStatusCallback
                    public void callback(int statusCode) {
                        if (statusCode != 1) {
                            SharedLog sharedLog = IpServer.this.mLog;
                            sharedLog.e("Error stopping DHCP server: " + statusCode);
                            IpServer.this.mLastError = 12;
                        }
                    }
                });
                this.mDhcpServer = null;
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        }
    }

    private boolean configureDhcp(boolean enable, Inet4Address addr, int prefixLen) {
        if (enable) {
            return startDhcp(addr, prefixLen);
        }
        stopDhcp();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopIPv4() {
        configureIPv4(false);
        this.mInterfaceCtrl.clearIPv4Address();
    }

    public void setIsFixed(boolean fixed) {
        this.isFixed = fixed;
    }

    private String getCustomWifiIPv4Address() {
        String resultString = this.isFixed ? WIFI_HOST_IFACE_ADDR : getRandomWifiIPv4Address();
        this.isFixed = false;
        if (!WIFI_HOST_IFACE_ADDR.equals(resultString)) {
            return WIFI_HOST_IFACE_ADDR;
        }
        return resultString;
    }

    private boolean configureIPv4(boolean enabled) {
        int prefixLen;
        String ipAsString;
        int i = this.mInterfaceType;
        if (i == 1) {
            ipAsString = USB_NEAR_IFACE_ADDR;
            prefixLen = 24;
        } else if (i != 0) {
            return configureDhcp(enabled, (Inet4Address) NetworkUtils.numericToInetAddress(BLUETOOTH_IFACE_ADDR), 24);
        } else {
            ipAsString = getCustomWifiIPv4Address();
            prefixLen = 24;
        }
        try {
            InterfaceConfiguration ifcg = this.mNMService.getInterfaceConfig(this.mIfaceName);
            if (ifcg == null) {
                this.mLog.e("Received null interface config");
                return false;
            }
            InetAddress addr = NetworkUtils.numericToInetAddress(ipAsString);
            LinkAddress linkAddr = new LinkAddress(addr, prefixLen);
            ifcg.setLinkAddress(linkAddr);
            if (this.mInterfaceType == 0) {
                ifcg.ignoreInterfaceUpDownStatus();
            } else if (enabled) {
                ifcg.setInterfaceUp();
            } else {
                ifcg.setInterfaceDown();
            }
            ifcg.clearFlag(INetd.IF_FLAG_RUNNING);
            this.mNMService.setInterfaceConfig(this.mIfaceName, ifcg);
            if (!configureDhcp(enabled, (Inet4Address) addr, prefixLen)) {
                return false;
            }
            RouteInfo route = new RouteInfo(linkAddr);
            if (enabled) {
                this.mLinkProperties.addLinkAddress(linkAddr);
                this.mLinkProperties.addRoute(route);
            } else {
                this.mLinkProperties.removeLinkAddress(linkAddr);
                this.mLinkProperties.removeRoute(route);
            }
            return true;
        } catch (Exception e) {
            SharedLog sharedLog = this.mLog;
            StringBuilder sb = new StringBuilder();
            sb.append("Error configuring interface ");
            sb.append(HW_DEBUG ? e : "*");
            sharedLog.e(sb.toString());
            if (!enabled) {
                try {
                    stopDhcp();
                } catch (Exception dhcpError) {
                    this.mLog.e("Error stopping DHCP", dhcpError);
                }
            }
            return false;
        }
    }

    private String getRandomWifiIPv4Address() {
        try {
            byte[] bytes = NetworkUtils.numericToInetAddress(WIFI_HOST_IFACE_ADDR).getAddress();
            bytes[3] = getRandomSanitizedByte((byte) 42, NetworkConstants.asByte(0), NetworkConstants.asByte(1), NetworkConstants.FF);
            return InetAddress.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            return WIFI_HOST_IFACE_ADDR;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean startIPv6() {
        this.mInterfaceParams = this.mDeps.getInterfaceParams(this.mIfaceName);
        InterfaceParams interfaceParams = this.mInterfaceParams;
        if (interfaceParams == null) {
            this.mLog.e("Failed to find InterfaceParams");
            stopIPv6();
            return false;
        }
        this.mRaDaemon = this.mDeps.getRouterAdvertisementDaemon(interfaceParams);
        if (this.mRaDaemon.start()) {
            return true;
        }
        stopIPv6();
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopIPv6() {
        this.mInterfaceParams = null;
        setRaParams(null);
        RouterAdvertisementDaemon routerAdvertisementDaemon = this.mRaDaemon;
        if (routerAdvertisementDaemon != null) {
            routerAdvertisementDaemon.stop();
            this.mRaDaemon = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateUpstreamIPv6LinkProperties(LinkProperties v6only) {
        if (this.mRaDaemon != null && !Objects.equals(this.mLastIPv6LinkProperties, v6only)) {
            RouterAdvertisementDaemon.RaParams params = null;
            if (v6only != null) {
                params = new RouterAdvertisementDaemon.RaParams();
                params.mtu = v6only.getMtu();
                params.hasDefaultRoute = v6only.hasIpv6DefaultRoute();
                if (params.hasDefaultRoute) {
                    params.hopLimit = getHopLimit(v6only.getInterfaceName());
                }
                for (LinkAddress linkAddr : v6only.getLinkAddresses()) {
                    if (linkAddr.getPrefixLength() == 64) {
                        IpPrefix prefix = new IpPrefix(linkAddr.getAddress(), linkAddr.getPrefixLength());
                        params.prefixes.add(prefix);
                        Inet6Address dnsServer = getLocalDnsIpFor(prefix);
                        if (dnsServer != null) {
                            params.dnses.add(dnsServer);
                        }
                    }
                }
            }
            setRaParams(params);
            this.mLastIPv6LinkProperties = v6only;
        }
    }

    private void configureLocalIPv6Routes(HashSet<IpPrefix> deprecatedPrefixes, HashSet<IpPrefix> newPrefixes) {
        Object obj = "*";
        if (!deprecatedPrefixes.isEmpty()) {
            ArrayList<RouteInfo> toBeRemoved = getLocalRoutesFor(this.mIfaceName, deprecatedPrefixes);
            try {
                int removalFailures = this.mNMService.removeRoutesFromLocalNetwork(toBeRemoved);
                if (removalFailures > 0) {
                    this.mLog.e(String.format("Failed to remove %d IPv6 routes from local table.", Integer.valueOf(removalFailures)));
                }
            } catch (RemoteException e) {
                SharedLog sharedLog = this.mLog;
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to remove IPv6 routes from local table: ");
                sb.append(HW_DEBUG ? e : obj);
                sharedLog.e(sb.toString());
            }
            Iterator<RouteInfo> it = toBeRemoved.iterator();
            while (it.hasNext()) {
                this.mLinkProperties.removeRoute(it.next());
            }
        }
        if (newPrefixes != null && !newPrefixes.isEmpty()) {
            HashSet<IpPrefix> addedPrefixes = (HashSet) newPrefixes.clone();
            RouterAdvertisementDaemon.RaParams raParams = this.mLastRaParams;
            if (raParams != null) {
                addedPrefixes.removeAll(raParams.prefixes);
            }
            if (!addedPrefixes.isEmpty()) {
                ArrayList<RouteInfo> toBeAdded = getLocalRoutesFor(this.mIfaceName, addedPrefixes);
                try {
                    this.mNMService.addInterfaceToLocalNetwork(this.mIfaceName, toBeAdded);
                } catch (RemoteException | IllegalStateException e2) {
                    SharedLog sharedLog2 = this.mLog;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Failed to add IPv6 routes to local table: ");
                    if (HW_DEBUG) {
                        obj = e2;
                    }
                    sb2.append(obj);
                    sharedLog2.e(sb2.toString());
                }
                Iterator<RouteInfo> it2 = toBeAdded.iterator();
                while (it2.hasNext()) {
                    this.mLinkProperties.addRoute(it2.next());
                }
            }
        }
    }

    private void configureLocalIPv6Dns(HashSet<Inet6Address> deprecatedDnses, HashSet<Inet6Address> newDnses) {
        if (this.mNetd == null) {
            if (newDnses != null) {
                newDnses.clear();
            }
            this.mLog.e("No netd service instance available; not setting local IPv6 addresses");
            return;
        }
        if (!deprecatedDnses.isEmpty()) {
            Iterator<Inet6Address> it = deprecatedDnses.iterator();
            while (it.hasNext()) {
                Inet6Address dns = it.next();
                if (!this.mInterfaceCtrl.removeAddress(dns, 64)) {
                    SharedLog sharedLog = this.mLog;
                    sharedLog.e("Failed to remove local dns IP " + dns);
                }
                this.mLinkProperties.removeLinkAddress(new LinkAddress(dns, 64));
            }
        }
        if (newDnses != null && !newDnses.isEmpty()) {
            HashSet<Inet6Address> addedDnses = (HashSet) newDnses.clone();
            RouterAdvertisementDaemon.RaParams raParams = this.mLastRaParams;
            if (raParams != null) {
                addedDnses.removeAll(raParams.dnses);
            }
            Iterator<Inet6Address> it2 = addedDnses.iterator();
            while (it2.hasNext()) {
                Inet6Address dns2 = it2.next();
                if (!this.mInterfaceCtrl.addAddress(dns2, 64)) {
                    SharedLog sharedLog2 = this.mLog;
                    sharedLog2.e("Failed to add local dns IP " + dns2);
                    newDnses.remove(dns2);
                }
                this.mLinkProperties.addLinkAddress(new LinkAddress(dns2, 64));
            }
        }
        try {
            this.mNetd.tetherApplyDnsInterfaces();
        } catch (RemoteException | ServiceSpecificException e) {
            this.mLog.e("Failed to update local DNS caching server");
            if (newDnses != null) {
                newDnses.clear();
            }
        }
    }

    private byte getHopLimit(String upstreamIface) {
        try {
            return (byte) Integer.min(Integer.parseUnsignedInt(this.mNetd.getProcSysNet(6, 1, upstreamIface, "hop_limit")) + 1, 255);
        } catch (Exception e) {
            this.mLog.e("Failed to find upstream interface hop limit", e);
            return 65;
        }
    }

    private void setRaParams(RouterAdvertisementDaemon.RaParams newParams) {
        if (this.mRaDaemon != null) {
            RouterAdvertisementDaemon.RaParams deprecatedParams = RouterAdvertisementDaemon.RaParams.getDeprecatedRaParams(this.mLastRaParams, newParams);
            HashSet<Inet6Address> hashSet = null;
            configureLocalIPv6Routes(deprecatedParams.prefixes, newParams != null ? newParams.prefixes : null);
            HashSet<Inet6Address> hashSet2 = deprecatedParams.dnses;
            if (newParams != null) {
                hashSet = newParams.dnses;
            }
            configureLocalIPv6Dns(hashSet2, hashSet);
            this.mRaDaemon.buildNewRa(deprecatedParams, newParams);
        }
        this.mLastRaParams = newParams;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logMessage(State state, int what) {
        SharedLog sharedLog = this.mLog;
        sharedLog.log(state.getName() + " got " + sMagicDecoderRing.get(what, Integer.toString(what)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendInterfaceState(int newInterfaceState) {
        this.mServingMode = newInterfaceState;
        this.mCallback.updateInterfaceState(this, newInterfaceState, this.mLastError);
        sendLinkProperties();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendLinkProperties() {
        this.mCallback.updateLinkProperties(this, new LinkProperties(this.mLinkProperties));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetLinkProperties() {
        this.mLinkProperties.clear();
        this.mLinkProperties.setInterfaceName(this.mIfaceName);
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            IpServer.this.sendInterfaceState(1);
        }

        public boolean processMessage(Message message) {
            IpServer.this.logMessage(this, message.what);
            int i = message.what;
            if (i == 327782) {
                IpServer.this.mLastError = 0;
                int i2 = message.arg1;
                if (i2 == 2) {
                    IpServer ipServer = IpServer.this;
                    ipServer.transitionTo(ipServer.mTetheredState);
                    return true;
                } else if (i2 != 3) {
                    IpServer.this.mLog.e("Invalid tethering interface serving state specified.");
                    return true;
                } else {
                    IpServer ipServer2 = IpServer.this;
                    ipServer2.transitionTo(ipServer2.mLocalHotspotState);
                    return true;
                }
            } else if (i == 327784) {
                IpServer ipServer3 = IpServer.this;
                ipServer3.transitionTo(ipServer3.mUnavailableState);
                return true;
            } else if (i != 327793) {
                return false;
            } else {
                IpServer.this.updateUpstreamIPv6LinkProperties((LinkProperties) message.obj);
                return true;
            }
        }
    }

    class BaseServingState extends State {
        private boolean isAvoidStopInterface = false;

        BaseServingState() {
        }

        public void enter() {
            if (!IpServer.this.startIPv4()) {
                IpServer.this.mLastError = 10;
                return;
            }
            try {
                IpServer.this.mNMService.tetherInterface(IpServer.this.mIfaceName);
                if (!IpServer.this.startIPv6()) {
                    IpServer.this.mLog.e("Failed to startIPv6");
                }
            } catch (Exception e) {
                SharedLog sharedLog = IpServer.this.mLog;
                StringBuilder sb = new StringBuilder();
                sb.append("Error Tethering: ");
                sb.append(IpServer.HW_DEBUG ? e : "*");
                sharedLog.e(sb.toString());
                IpServer.this.mLastError = 6;
            }
        }

        public void exit() {
            IpServer.this.stopIPv6();
            if (IpServer.this.mInterfaceType != 1 || !this.isAvoidStopInterface) {
                try {
                    IpServer.this.mNMService.untetherInterface(IpServer.this.mIfaceName);
                } catch (Exception e) {
                    IpServer.this.mLastError = 7;
                    SharedLog sharedLog = IpServer.this.mLog;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Failed to untether interface: ");
                    sb.append(IpServer.HW_DEBUG ? e : "*");
                    sharedLog.e(sb.toString());
                }
                IpServer.this.stopIPv4();
                IpServer.this.resetLinkProperties();
                return;
            }
            IpServer.this.mLog.i("UsbP2p AVOID stopping ipv4 interface.");
            this.isAvoidStopInterface = false;
        }

        public boolean processMessage(Message message) {
            IpServer.this.logMessage(this, message.what);
            boolean z = false;
            switch (message.what) {
                case IpServer.CMD_TETHER_UNREQUESTED /* 327783 */:
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    this.isAvoidStopInterface = z;
                    IpServer ipServer = IpServer.this;
                    ipServer.transitionTo(ipServer.mInitialState);
                    break;
                case IpServer.CMD_INTERFACE_DOWN /* 327784 */:
                    IpServer ipServer2 = IpServer.this;
                    ipServer2.transitionTo(ipServer2.mUnavailableState);
                    break;
                case 327785:
                case 327786:
                case IpServer.CMD_TETHER_CONNECTION_CHANGED /* 327792 */:
                default:
                    return false;
                case IpServer.CMD_IP_FORWARDING_ENABLE_ERROR /* 327787 */:
                case IpServer.CMD_IP_FORWARDING_DISABLE_ERROR /* 327788 */:
                case IpServer.CMD_START_TETHERING_ERROR /* 327789 */:
                case IpServer.CMD_STOP_TETHERING_ERROR /* 327790 */:
                case IpServer.CMD_SET_DNS_FORWARDERS_ERROR /* 327791 */:
                    IpServer.this.mLastError = 5;
                    IpServer ipServer3 = IpServer.this;
                    ipServer3.transitionTo(ipServer3.mInitialState);
                    break;
                case IpServer.CMD_IPV6_TETHER_UPDATE /* 327793 */:
                    IpServer.this.updateUpstreamIPv6LinkProperties((LinkProperties) message.obj);
                    IpServer.this.sendLinkProperties();
                    break;
            }
            return true;
        }
    }

    class LocalHotspotState extends BaseServingState {
        LocalHotspotState() {
            super();
        }

        @Override // android.net.ip.IpServer.BaseServingState
        public void enter() {
            super.enter();
            if (IpServer.this.mLastError != 0) {
                IpServer ipServer = IpServer.this;
                ipServer.transitionTo(ipServer.mInitialState);
            }
            IpServer.this.sendInterfaceState(3);
        }

        @Override // android.net.ip.IpServer.BaseServingState
        public boolean processMessage(Message message) {
            if (super.processMessage(message)) {
                return true;
            }
            IpServer.this.logMessage(this, message.what);
            int i = message.what;
            if (i == 327782) {
                IpServer.this.mLog.e("CMD_TETHER_REQUESTED while in local-only hotspot mode.");
            } else if (i != 327792) {
                return false;
            }
            return true;
        }
    }

    class TetheredState extends BaseServingState {
        TetheredState() {
            super();
        }

        @Override // android.net.ip.IpServer.BaseServingState
        public void enter() {
            super.enter();
            if (IpServer.this.mLastError != 0) {
                IpServer ipServer = IpServer.this;
                ipServer.transitionTo(ipServer.mInitialState);
            }
            IpServer.this.sendInterfaceState(2);
        }

        @Override // android.net.ip.IpServer.BaseServingState
        public void exit() {
            cleanupUpstream();
            super.exit();
        }

        private void cleanupUpstream() {
            if (IpServer.this.mUpstreamIfaceSet != null) {
                for (String ifname : IpServer.this.mUpstreamIfaceSet.ifnames) {
                    cleanupUpstreamInterface(ifname);
                }
                IpServer.this.mUpstreamIfaceSet = null;
            }
        }

        private void cleanupUpstreamInterface(String upstreamIface) {
            try {
                IpServer.this.mStatsService.forceUpdate();
            } catch (Exception e) {
            }
            try {
                IpServer.this.mNMService.stopInterfaceForwarding(IpServer.this.mIfaceName, upstreamIface);
            } catch (Exception e2) {
            }
            try {
                IpServer.this.mNMService.disableNat(IpServer.this.mIfaceName, upstreamIface);
            } catch (Exception e3) {
            }
        }

        @Override // android.net.ip.IpServer.BaseServingState
        public boolean processMessage(Message message) {
            if (super.processMessage(message)) {
                return true;
            }
            IpServer.this.logMessage(this, message.what);
            int i = message.what;
            if (i == 327782) {
                IpServer.this.mLog.e("CMD_TETHER_REQUESTED while already tethering.");
            } else if (i != 327792) {
                return false;
            } else {
                InterfaceSet newUpstreamIfaceSet = (InterfaceSet) message.obj;
                if (!noChangeInUpstreamIfaceSet(newUpstreamIfaceSet)) {
                    if (newUpstreamIfaceSet == null) {
                        cleanupUpstream();
                    } else {
                        for (String removed : upstreamInterfacesRemoved(newUpstreamIfaceSet)) {
                            cleanupUpstreamInterface(removed);
                        }
                        Set<String> added = upstreamInterfacesAdd(newUpstreamIfaceSet);
                        IpServer.this.mUpstreamIfaceSet = newUpstreamIfaceSet;
                        for (String ifname : added) {
                            try {
                                IpServer.this.mNMService.enableNat(IpServer.this.mIfaceName, ifname);
                                IpServer.this.mNMService.startInterfaceForwarding(IpServer.this.mIfaceName, ifname);
                            } catch (Exception e) {
                                SharedLog sharedLog = IpServer.this.mLog;
                                StringBuilder sb = new StringBuilder();
                                sb.append("Exception enabling NAT: ");
                                sb.append(IpServer.HW_DEBUG ? e : "*");
                                sharedLog.e(sb.toString());
                                cleanupUpstream();
                                IpServer.this.mLastError = 8;
                                IpServer ipServer = IpServer.this;
                                ipServer.transitionTo(ipServer.mInitialState);
                                return true;
                            }
                        }
                    }
                }
            }
            return true;
        }

        private boolean noChangeInUpstreamIfaceSet(InterfaceSet newIfaces) {
            if (IpServer.this.mUpstreamIfaceSet == null && newIfaces == null) {
                return true;
            }
            if (IpServer.this.mUpstreamIfaceSet == null || newIfaces == null) {
                return false;
            }
            return IpServer.this.mUpstreamIfaceSet.equals(newIfaces);
        }

        private Set<String> upstreamInterfacesRemoved(InterfaceSet newIfaces) {
            if (IpServer.this.mUpstreamIfaceSet == null) {
                return new HashSet();
            }
            HashSet<String> removed = new HashSet<>(IpServer.this.mUpstreamIfaceSet.ifnames);
            removed.removeAll(newIfaces.ifnames);
            return removed;
        }

        private Set<String> upstreamInterfacesAdd(InterfaceSet newIfaces) {
            HashSet<String> added = new HashSet<>(newIfaces.ifnames);
            if (IpServer.this.mUpstreamIfaceSet != null) {
                added.removeAll(IpServer.this.mUpstreamIfaceSet.ifnames);
            }
            return added;
        }
    }

    class UnavailableState extends State {
        UnavailableState() {
        }

        public void enter() {
            IpServer.this.mLastError = 0;
            IpServer.this.sendInterfaceState(0);
        }
    }

    private static ArrayList<RouteInfo> getLocalRoutesFor(String ifname, HashSet<IpPrefix> prefixes) {
        ArrayList<RouteInfo> localRoutes = new ArrayList<>();
        Iterator<IpPrefix> it = prefixes.iterator();
        while (it.hasNext()) {
            localRoutes.add(new RouteInfo(it.next(), null, ifname));
        }
        return localRoutes;
    }

    private static Inet6Address getLocalDnsIpFor(IpPrefix localPrefix) {
        byte[] dnsBytes = localPrefix.getRawAddress();
        dnsBytes[dnsBytes.length - 1] = getRandomSanitizedByte((byte) 42, NetworkConstants.asByte(0), NetworkConstants.asByte(1));
        try {
            return Inet6Address.getByAddress((String) null, dnsBytes, 0);
        } catch (UnknownHostException e) {
            Slog.wtf(TAG, "Failed to construct Inet6Address from: " + localPrefix);
            return null;
        }
    }

    private static byte getRandomSanitizedByte(byte dflt, byte... excluded) {
        byte random = (byte) new Random().nextInt();
        for (byte b : excluded) {
            if (random == b) {
                return dflt;
            }
        }
        return random;
    }

    public static String getUsbNearIfaceAddr() {
        return USB_NEAR_IFACE_ADDR;
    }
}
