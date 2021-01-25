package com.android.server.ethernet;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpResults;
import android.net.IpConfiguration;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.NetworkUtils;
import android.net.StringNetworkSpecifier;
import android.net.ip.IIpClient;
import android.net.ip.IpClientCallbacks;
import android.net.ip.IpClientUtil;
import android.net.shared.LinkPropertiesParcelableUtil;
import android.net.shared.ProvisioningConfiguration;
import android.net.util.InterfaceParams;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.ethernet.EthernetNetworkFactory;
import java.io.FileDescriptor;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class EthernetNetworkFactory extends NetworkFactory {
    private static final String ACTION_IP_CONFLICT = "android.net.conn.IP_ADDRESS_CONFLICTED";
    static final boolean DBG = true;
    private static final String EXTRA_IPCONFILICT_INTERFACE = "interface";
    private static final int IP_CONFLICT = 1;
    private static final String NETWORK_TYPE = "Ethernet";
    private static final String TAG = EthernetNetworkFactory.class.getSimpleName();
    private static final int WIFI_PRIORITY_SCORE = 50;
    private static boolean isTv = "tv".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static boolean isWifiPriority = "wifi".equals(SystemProperties.get("persist.network.firstpriority", "ethernet"));
    private final Context mContext;
    private final Handler mHandler;
    private int mScore = 70;
    private final ConcurrentHashMap<String, NetworkInterfaceState> mTrackingInterfaces = new ConcurrentHashMap<>();

    public static class ConfigurationException extends AndroidRuntimeException {
        public ConfigurationException(String msg) {
            super(msg);
        }
    }

    public EthernetNetworkFactory(Handler handler, Context context, NetworkCapabilities filter) {
        super(handler.getLooper(), context, NETWORK_TYPE, filter);
        this.mHandler = handler;
        this.mContext = context;
        if (isTv && isWifiPriority) {
            Log.i(TAG, "tv divice and wifi is first. set Ethernet 50 score.");
            this.mScore = WIFI_PRIORITY_SCORE;
        }
        setScoreFilter(this.mScore);
    }

    public boolean acceptRequest(NetworkRequest request, int score) {
        if (request.type == NetworkRequest.Type.TRACK_DEFAULT) {
            return false;
        }
        String str = TAG;
        Log.i(str, "acceptRequest, request: " + request + ", score: " + score);
        if (networkForRequest(request) != null) {
            return DBG;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        NetworkInterfaceState network = networkForRequest(networkRequest);
        if (network == null) {
            String str = TAG;
            Log.e(str, "needNetworkFor, failed to get a network for " + networkRequest);
            return;
        }
        long j = network.refCount + 1;
        network.refCount = j;
        if (j == 1 && network.mLinkUp) {
            network.start();
        }
    }

    /* access modifiers changed from: protected */
    public void releaseNetworkFor(NetworkRequest networkRequest) {
        NetworkInterfaceState network = networkForRequest(networkRequest);
        if (network == null) {
            String str = TAG;
            Log.e(str, "needNetworkFor, failed to get a network for " + networkRequest);
            return;
        }
        long j = network.refCount - 1;
        network.refCount = j;
        if (j == 1) {
            network.stop();
        }
    }

    /* access modifiers changed from: package-private */
    public String[] getAvailableInterfaces(boolean includeRestricted) {
        return (String[]) this.mTrackingInterfaces.values().stream().filter(new Predicate(includeRestricted) {
            /* class com.android.server.ethernet.$$Lambda$EthernetNetworkFactory$b1ndnzBiSX1ihvZw7GtATwTUsto */
            private final /* synthetic */ boolean f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return EthernetNetworkFactory.lambda$getAvailableInterfaces$0(this.f$0, (EthernetNetworkFactory.NetworkInterfaceState) obj);
            }
        }).sorted($$Lambda$EthernetNetworkFactory$EmftAjIay22czoGb8k_mrRGmnzg.INSTANCE).map($$Lambda$EthernetNetworkFactory$KXwxO15KBNVyyYSUjDFlm1vQ0.INSTANCE).toArray($$Lambda$EthernetNetworkFactory$TVQUJVMLGgbguTOK63vgn0fV1JA.INSTANCE);
    }

    static /* synthetic */ boolean lambda$getAvailableInterfaces$0(boolean includeRestricted, NetworkInterfaceState iface) {
        if (!iface.isRestricted() || includeRestricted) {
            return DBG;
        }
        return false;
    }

    static /* synthetic */ int lambda$getAvailableInterfaces$1(NetworkInterfaceState iface1, NetworkInterfaceState iface2) {
        int r = Boolean.compare(iface1.isRestricted(), iface2.isRestricted());
        return r == 0 ? iface1.name.compareTo(iface2.name) : r;
    }

    static /* synthetic */ String[] lambda$getAvailableInterfaces$3(int x$0) {
        return new String[x$0];
    }

    /* access modifiers changed from: package-private */
    public void addInterface(String ifaceName, String hwAddress, NetworkCapabilities capabilities, IpConfiguration ipConfiguration) {
        if (this.mTrackingInterfaces.containsKey(ifaceName)) {
            String str = TAG;
            Log.e(str, "Interface with name " + ifaceName + " already exists.");
            return;
        }
        String str2 = TAG;
        Log.i(str2, "addInterface, iface: " + ifaceName + ", capabilities: " + capabilities);
        NetworkInterfaceState iface = new NetworkInterfaceState(ifaceName, hwAddress, this.mHandler, this.mContext, capabilities);
        iface.setIpConfig(ipConfiguration);
        this.mTrackingInterfaces.put(ifaceName, iface);
        updateCapabilityFilter();
    }

    private void updateCapabilityFilter() {
        NetworkCapabilities capabilitiesFilter = new NetworkCapabilities();
        capabilitiesFilter.clearAll();
        for (NetworkInterfaceState iface : this.mTrackingInterfaces.values()) {
            capabilitiesFilter.combineCapabilities(iface.mCapabilities);
        }
        String str = TAG;
        Log.i(str, "updateCapabilityFilter: " + capabilitiesFilter);
        setCapabilityFilter(capabilitiesFilter);
    }

    /* access modifiers changed from: package-private */
    public void removeInterface(String interfaceName) {
        NetworkInterfaceState iface = this.mTrackingInterfaces.remove(interfaceName);
        if (iface != null) {
            iface.stop();
        }
        updateCapabilityFilter();
    }

    /* access modifiers changed from: package-private */
    public boolean updateInterfaceLinkState(String ifaceName, boolean up) {
        if (!this.mTrackingInterfaces.containsKey(ifaceName)) {
            return false;
        }
        String str = TAG;
        Log.i(str, "updateInterfaceLinkState, iface: " + ifaceName + ", up: " + up);
        return this.mTrackingInterfaces.get(ifaceName).updateLinkState(up);
    }

    /* access modifiers changed from: package-private */
    public boolean hasInterface(String interfacName) {
        return this.mTrackingInterfaces.containsKey(interfacName);
    }

    /* access modifiers changed from: package-private */
    public void updateIpConfiguration(String iface, IpConfiguration ipConfiguration) {
        NetworkInterfaceState network = this.mTrackingInterfaces.get(iface);
        if (network != null) {
            network.setIpConfig(ipConfiguration);
        }
    }

    private NetworkInterfaceState networkForRequest(NetworkRequest request) {
        String requestedIface = null;
        NetworkSpecifier specifier = request.networkCapabilities.getNetworkSpecifier();
        if (specifier instanceof StringNetworkSpecifier) {
            requestedIface = ((StringNetworkSpecifier) specifier).specifier;
        }
        NetworkInterfaceState network = null;
        if (TextUtils.isEmpty(requestedIface)) {
            Iterator<NetworkInterfaceState> it = this.mTrackingInterfaces.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NetworkInterfaceState n = it.next();
                if (n.statisified(request.networkCapabilities)) {
                    network = n;
                    break;
                }
            }
        } else {
            NetworkInterfaceState n2 = this.mTrackingInterfaces.get(requestedIface);
            if (n2 != null && n2.statisified(request.networkCapabilities)) {
                network = n2;
            }
        }
        String str = TAG;
        Log.i(str, "networkForRequest, request: " + request + ", network: " + network);
        return network;
    }

    /* access modifiers changed from: private */
    public static class NetworkInterfaceState {
        private static String sTcpBufferSizes = null;
        private static final SparseArray<TransportInfo> sTransports = new SparseArray<>();
        private final NetworkCapabilities mCapabilities;
        private final Context mContext;
        private final Handler mHandler;
        private final String mHwAddress;
        private volatile IIpClient mIpClient;
        private IpClientCallbacksImpl mIpClientCallback;
        private IpConfiguration mIpConfig;
        private LinkProperties mLinkProperties = new LinkProperties();
        private boolean mLinkUp;
        private NetworkAgent mNetworkAgent;
        private final NetworkInfo mNetworkInfo;
        final String name;
        long refCount = 0;

        static {
            sTransports.put(6, new TransportInfo(-1, 30));
            sTransports.put(5, new TransportInfo(-1, EthernetNetworkFactory.IP_CONFLICT));
            sTransports.put(3, new TransportInfo(9, 70));
            sTransports.put(2, new TransportInfo(7, 69));
            sTransports.put(EthernetNetworkFactory.IP_CONFLICT, new TransportInfo(EthernetNetworkFactory.IP_CONFLICT, 60));
            sTransports.put(0, new TransportInfo(0, EthernetNetworkFactory.WIFI_PRIORITY_SCORE));
        }

        /* access modifiers changed from: private */
        public static class TransportInfo {
            final int mLegacyType;
            final int mScore;

            private TransportInfo(int legacyType, int score) {
                this.mLegacyType = legacyType;
                this.mScore = score;
            }
        }

        /* access modifiers changed from: private */
        public class IpClientCallbacksImpl extends IpClientCallbacks {
            private final ConditionVariable mIpClientShutdownCv;
            private final ConditionVariable mIpClientStartCv;

            private IpClientCallbacksImpl() {
                this.mIpClientStartCv = new ConditionVariable(false);
                this.mIpClientShutdownCv = new ConditionVariable(false);
            }

            public void onIpClientCreated(IIpClient ipClient) {
                NetworkInterfaceState.this.mIpClient = ipClient;
                this.mIpClientStartCv.open();
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private void awaitIpClientStart() {
                this.mIpClientStartCv.block();
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private void awaitIpClientShutdown() {
                this.mIpClientShutdownCv.block();
            }

            public /* synthetic */ void lambda$onProvisioningSuccess$0$EthernetNetworkFactory$NetworkInterfaceState$IpClientCallbacksImpl(LinkProperties newLp) {
                NetworkInterfaceState.this.onIpLayerStarted(newLp);
            }

            public void onProvisioningSuccess(LinkProperties newLp) {
                NetworkInterfaceState.this.mHandler.post(new Runnable(newLp) {
                    /* class com.android.server.ethernet.$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$IpClientCallbacksImpl$tMv_9FjFsYD4UnoWCCV1V3NZ6z8 */
                    private final /* synthetic */ LinkProperties f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        EthernetNetworkFactory.NetworkInterfaceState.IpClientCallbacksImpl.this.lambda$onProvisioningSuccess$0$EthernetNetworkFactory$NetworkInterfaceState$IpClientCallbacksImpl(this.f$1);
                    }
                });
            }

            public /* synthetic */ void lambda$onProvisioningFailure$1$EthernetNetworkFactory$NetworkInterfaceState$IpClientCallbacksImpl(LinkProperties newLp) {
                NetworkInterfaceState.this.onIpLayerStopped(newLp);
            }

            public void onProvisioningFailure(LinkProperties newLp) {
                NetworkInterfaceState.this.mHandler.post(new Runnable(newLp) {
                    /* class com.android.server.ethernet.$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$IpClientCallbacksImpl$FZif1d4H4R7Ocud9hpc5Sbf6o7k */
                    private final /* synthetic */ LinkProperties f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        EthernetNetworkFactory.NetworkInterfaceState.IpClientCallbacksImpl.this.lambda$onProvisioningFailure$1$EthernetNetworkFactory$NetworkInterfaceState$IpClientCallbacksImpl(this.f$1);
                    }
                });
            }

            public /* synthetic */ void lambda$onLinkPropertiesChange$2$EthernetNetworkFactory$NetworkInterfaceState$IpClientCallbacksImpl(LinkProperties newLp) {
                NetworkInterfaceState.this.updateLinkProperties(newLp);
            }

            public void onLinkPropertiesChange(LinkProperties newLp) {
                NetworkInterfaceState.this.mHandler.post(new Runnable(newLp) {
                    /* class com.android.server.ethernet.$$Lambda$EthernetNetworkFactory$NetworkInterfaceState$IpClientCallbacksImpl$Ijv9vAtt3O7tCPOHIl8QUewI09g */
                    private final /* synthetic */ LinkProperties f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        EthernetNetworkFactory.NetworkInterfaceState.IpClientCallbacksImpl.this.lambda$onLinkPropertiesChange$2$EthernetNetworkFactory$NetworkInterfaceState$IpClientCallbacksImpl(this.f$1);
                    }
                });
            }

            public void onQuit() {
                NetworkInterfaceState.this.mIpClient = null;
                this.mIpClientShutdownCv.open();
            }

            public void doArpDetection(int type, String uniqueStr, DhcpResults dhcpResults) {
                try {
                    if (NetworkInterfaceState.this.mIpClient != null) {
                        Log.i(EthernetNetworkFactory.TAG, "don't do ARP detection for ethernet and default report false");
                        NetworkInterfaceState.this.mIpClient.reportArpResult(type, uniqueStr, false);
                    }
                } catch (RemoteException e) {
                    Log.e(EthernetNetworkFactory.TAG, "RemoteException happens");
                }
            }
        }

        private static void shutdownIpClient(IIpClient ipClient) {
            try {
                ipClient.shutdown();
            } catch (RemoteException e) {
                Log.e(EthernetNetworkFactory.TAG, "Error stopping IpClient", e);
            }
        }

        NetworkInterfaceState(String ifaceName, String hwAddress, Handler handler, Context context, NetworkCapabilities capabilities) {
            this.name = ifaceName;
            this.mCapabilities = (NetworkCapabilities) Preconditions.checkNotNull(capabilities);
            this.mHandler = handler;
            this.mContext = context;
            int[] transportTypes = this.mCapabilities.getTransportTypes();
            if (transportTypes.length > 0) {
                int legacyType = getLegacyType(transportTypes[0]);
                this.mHwAddress = hwAddress;
                this.mNetworkInfo = new NetworkInfo(legacyType, 0, EthernetNetworkFactory.NETWORK_TYPE, "");
                this.mNetworkInfo.setExtraInfo(this.mHwAddress);
                this.mNetworkInfo.setIsAvailable(EthernetNetworkFactory.DBG);
                return;
            }
            throw new ConfigurationException("Network Capabilities do not have an associated transport type.");
        }

        /* access modifiers changed from: package-private */
        public void setIpConfig(IpConfiguration ipConfig) {
            this.mIpConfig = ipConfig;
        }

        /* access modifiers changed from: package-private */
        public boolean statisified(NetworkCapabilities requestedCapabilities) {
            return requestedCapabilities.satisfiedByNetworkCapabilities(this.mCapabilities);
        }

        /* access modifiers changed from: package-private */
        public boolean isRestricted() {
            return this.mCapabilities.hasCapability(13);
        }

        private static int getLegacyType(int transport) {
            TransportInfo transportInfo = sTransports.get(transport, null);
            if (transportInfo != null) {
                return transportInfo.mLegacyType;
            }
            return -1;
        }

        private int getNetworkScore() {
            if (!this.mLinkUp) {
                return 0;
            }
            int[] transportTypes = this.mCapabilities.getTransportTypes();
            if (transportTypes.length < EthernetNetworkFactory.IP_CONFLICT) {
                String str = EthernetNetworkFactory.TAG;
                Log.w(str, "Network interface '" + this.mLinkProperties.getInterfaceName() + "' has no transport type associated with it. Score set to zero");
                return 0;
            }
            TransportInfo transportInfo = sTransports.get(transportTypes[0], null);
            if (EthernetNetworkFactory.isTv && EthernetNetworkFactory.isWifiPriority && transportInfo != null && transportTypes[0] == 3) {
                Log.i(EthernetNetworkFactory.TAG, "tv divice and wifi is first. return 50 score");
                return EthernetNetworkFactory.WIFI_PRIORITY_SCORE;
            } else if (transportInfo != null) {
                return transportInfo.mScore;
            } else {
                return 0;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void start() {
            if (this.mIpClient != null) {
                Log.i(EthernetNetworkFactory.TAG, "IpClient already started");
                return;
            }
            Log.i(EthernetNetworkFactory.TAG, String.format("starting IpClient(%s): mNetworkInfo=%s", this.name, this.mNetworkInfo));
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.OBTAINING_IPADDR, null, this.mHwAddress);
            this.mIpClientCallback = new IpClientCallbacksImpl();
            IpClientUtil.makeIpClient(this.mContext, this.name, this.mIpClientCallback);
            this.mIpClientCallback.awaitIpClientStart();
            if (sTcpBufferSizes == null) {
                sTcpBufferSizes = this.mContext.getResources().getString(17039849);
            }
            provisionIpClient(this.mIpClient, this.mIpConfig, sTcpBufferSizes);
        }

        /* access modifiers changed from: package-private */
        public void onIpLayerStarted(LinkProperties linkProperties) {
            IpConfiguration ipConfiguration;
            if (this.mNetworkAgent != null) {
                Log.e(EthernetNetworkFactory.TAG, "Already have a NetworkAgent - aborting new request");
                if (EthernetNetworkFactory.isTv) {
                    LinkProperties linkProperties2 = this.mLinkProperties;
                    if (linkProperties2 == null || linkProperties2.equals(linkProperties)) {
                        Log.i(EthernetNetworkFactory.TAG, "link no change, do nothing");
                    } else {
                        updateLinkProperties(linkProperties);
                    }
                } else {
                    stop();
                }
            } else {
                this.mLinkProperties = linkProperties;
                this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, this.mHwAddress);
                this.mNetworkInfo.setIsAvailable(EthernetNetworkFactory.DBG);
                if (EthernetNetworkFactory.isTv && (ipConfiguration = this.mIpConfig) != null && ipConfiguration.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
                    runCheckIpConflict();
                }
                this.mNetworkAgent = new NetworkAgent(this.mHandler.getLooper(), this.mContext, EthernetNetworkFactory.NETWORK_TYPE, this.mNetworkInfo, this.mCapabilities, this.mLinkProperties, getNetworkScore()) {
                    /* class com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.AnonymousClass1 */

                    public void unwanted() {
                        if (this == NetworkInterfaceState.this.mNetworkAgent) {
                            NetworkInterfaceState.this.stop();
                        } else if (NetworkInterfaceState.this.mNetworkAgent != null) {
                            Log.i(EthernetNetworkFactory.TAG, "Ignoring unwanted as we have a more modern instance");
                        }
                    }
                };
            }
        }

        /* access modifiers changed from: package-private */
        public void onIpLayerStopped(LinkProperties linkProperties) {
            stop();
            if (InterfaceParams.getByName(this.name) != null) {
                start();
            }
        }

        /* access modifiers changed from: package-private */
        public void updateLinkProperties(LinkProperties linkProperties) {
            this.mLinkProperties = linkProperties;
            NetworkAgent networkAgent = this.mNetworkAgent;
            if (networkAgent != null) {
                networkAgent.sendLinkProperties(linkProperties);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean updateLinkState(boolean up) {
            if (this.mLinkUp == up) {
                return false;
            }
            this.mLinkUp = up;
            stop();
            if (!up) {
                return EthernetNetworkFactory.DBG;
            }
            start();
            return EthernetNetworkFactory.DBG;
        }

        /* access modifiers changed from: package-private */
        public void stop() {
            if (this.mIpClient != null) {
                shutdownIpClient(this.mIpClient);
                this.mIpClientCallback.awaitIpClientShutdown();
                this.mIpClient = null;
            }
            this.mIpClientCallback = null;
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, this.mHwAddress);
            if (this.mNetworkAgent != null) {
                updateAgent();
                this.mNetworkAgent = null;
            }
            clear();
        }

        private void updateAgent() {
            if (this.mNetworkAgent != null) {
                String str = EthernetNetworkFactory.TAG;
                Log.i(str, "Updating mNetworkAgent with: " + this.mCapabilities + ", " + this.mNetworkInfo + ", " + this.mLinkProperties);
                this.mNetworkAgent.sendNetworkCapabilities(this.mCapabilities);
                this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
                this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
                this.mNetworkAgent.sendNetworkScore(getNetworkScore());
            }
        }

        private void clear() {
            this.mLinkProperties.clear();
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.IDLE, null, null);
            this.mNetworkInfo.setIsAvailable(false);
        }

        private static void provisionIpClient(IIpClient ipClient, IpConfiguration config, String tcpBufferSizes) {
            ProvisioningConfiguration provisioningConfiguration;
            if (config.getProxySettings() == IpConfiguration.ProxySettings.STATIC || config.getProxySettings() == IpConfiguration.ProxySettings.PAC) {
                try {
                    ipClient.setHttpProxy(LinkPropertiesParcelableUtil.toStableParcelable(config.getHttpProxy()));
                } catch (RemoteException e) {
                    e.rethrowFromSystemServer();
                }
            }
            if (!TextUtils.isEmpty(tcpBufferSizes)) {
                try {
                    ipClient.setTcpBufferSizes(tcpBufferSizes);
                } catch (RemoteException e2) {
                    e2.rethrowFromSystemServer();
                }
            }
            if (config.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
                provisioningConfiguration = new ProvisioningConfiguration.Builder().withStaticConfiguration(config.getStaticIpConfiguration()).build();
            } else {
                provisioningConfiguration = new ProvisioningConfiguration.Builder().withProvisioningTimeoutMs(0).build();
            }
            try {
                ipClient.startProvisioning(provisioningConfiguration.toStableParcelable());
            } catch (RemoteException e3) {
                e3.rethrowFromSystemServer();
            }
        }

        private void runCheckIpConflict() {
            if (this.mIpConfig.getStaticIpConfiguration() == null || this.mIpConfig.getStaticIpConfiguration().getIpAddress() == null) {
                Log.i(EthernetNetworkFactory.TAG, "no ip address, can't run checkIpConflict.");
            } else {
                new Thread(new Runnable() {
                    /* class com.android.server.ethernet.EthernetNetworkFactory.NetworkInterfaceState.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        if (NetworkUtils.checkIpConflict(NetworkInterfaceState.this.name, NetworkInterfaceState.this.mIpConfig.getStaticIpConfiguration().getIpAddress().getAddress().toString().replaceAll("/", "")) == EthernetNetworkFactory.IP_CONFLICT) {
                            Log.i(EthernetNetworkFactory.TAG, "ip conflict");
                            Intent intent = new Intent(EthernetNetworkFactory.ACTION_IP_CONFLICT);
                            intent.addFlags(67108864);
                            intent.putExtra(EthernetNetworkFactory.EXTRA_IPCONFILICT_INTERFACE, 0);
                            NetworkInterfaceState.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                            return;
                        }
                        Log.i(EthernetNetworkFactory.TAG, "ip not conflict");
                    }
                }).start();
            }
        }

        public String toString() {
            return getClass().getSimpleName() + "{ refCount: " + this.refCount + ", iface: " + this.name + ", up: " + this.mLinkUp + ", hwAddress: ***, networkInfo: " + this.mNetworkInfo + ", networkCapabilities: " + this.mCapabilities + ", networkAgent: " + this.mNetworkAgent + ", score: " + getNetworkScore() + ", ipClient: " + this.mIpClient + ",linkProperties: " + this.mLinkProperties + "}";
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, IndentingPrintWriter pw, String[] args) {
        EthernetNetworkFactory.super.dump(fd, pw, args);
        pw.println(getClass().getSimpleName());
        pw.println("Tracking interfaces:");
        pw.increaseIndent();
        for (String iface : this.mTrackingInterfaces.keySet()) {
            NetworkInterfaceState ifaceState = this.mTrackingInterfaces.get(iface);
            pw.println(iface + ":" + ifaceState);
            pw.increaseIndent();
            IIpClient ipClient = ifaceState.mIpClient;
            if (ipClient != null) {
                IpClientUtil.dumpIpClient(ipClient, fd, pw, args);
            } else {
                pw.println("IpClient is null");
            }
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }
}
