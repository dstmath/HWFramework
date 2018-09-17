package com.android.server.ethernet;

import android.content.Context;
import android.net.EthernetManager;
import android.net.IEthernetServiceListener;
import android.net.InterfaceConfiguration;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.StaticIpConfiguration;
import android.net.ip.IpManager;
import android.net.ip.IpManager.Callback;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.ethernet.-$Lambda$eRAo6Nl9jtYC3igbtEDMyHdAcyk.AnonymousClass3;
import com.android.server.ethernet.-$Lambda$eRAo6Nl9jtYC3igbtEDMyHdAcyk.AnonymousClass4;
import com.android.server.ethernet.-$Lambda$eRAo6Nl9jtYC3igbtEDMyHdAcyk.AnonymousClass5;
import com.android.server.ethernet.-$Lambda$eRAo6Nl9jtYC3igbtEDMyHdAcyk.AnonymousClass6;
import com.android.server.ethernet.-$Lambda$eRAo6Nl9jtYC3igbtEDMyHdAcyk.AnonymousClass7;
import com.android.server.ethernet.-$Lambda$eRAo6Nl9jtYC3igbtEDMyHdAcyk.AnonymousClass8;
import com.android.server.net.BaseNetworkObserver;
import java.io.FileDescriptor;
import java.util.concurrent.CountDownLatch;

class EthernetNetworkFactory {
    private static final boolean DBG = true;
    private static final int NETWORK_SCORE = 70;
    private static final String NETWORK_TYPE = "Ethernet";
    private static final String TAG = "EthernetNetworkFactory";
    private static String mIfaceMatch = "";
    private Context mContext;
    private EthernetManager mEthernetManager;
    private LocalNetworkFactory mFactory;
    private Handler mHandler;
    private String mHwAddr;
    private String mIface = "";
    private InterfaceObserver mInterfaceObserver;
    private IpManager mIpManager;
    private LinkProperties mLinkProperties;
    private boolean mLinkUp;
    private final RemoteCallbackList<IEthernetServiceListener> mListeners;
    private INetworkManagementService mNMService;
    private NetworkAgent mNetworkAgent;
    private NetworkCapabilities mNetworkCapabilities;
    private NetworkInfo mNetworkInfo;
    private boolean mNetworkRequested = false;

    private class InterfaceObserver extends BaseNetworkObserver {
        /* synthetic */ InterfaceObserver(EthernetNetworkFactory this$0, InterfaceObserver -this1) {
            this();
        }

        private InterfaceObserver() {
        }

        public void interfaceLinkStateChanged(String iface, boolean up) {
            EthernetNetworkFactory.this.mHandler.post(new AnonymousClass8(up, this, iface));
        }

        /* synthetic */ void lambda$-com_android_server_ethernet_EthernetNetworkFactory$InterfaceObserver_6445(String iface, boolean up) {
            EthernetNetworkFactory.this.updateInterfaceState(iface, up);
        }

        public void interfaceAdded(String iface) {
            EthernetNetworkFactory.this.mHandler.post(new AnonymousClass4(this, iface));
        }

        /* synthetic */ void lambda$-com_android_server_ethernet_EthernetNetworkFactory$InterfaceObserver_6624(String iface) {
            EthernetNetworkFactory.this.maybeTrackInterface(iface);
        }

        public void interfaceRemoved(String iface) {
            EthernetNetworkFactory.this.mHandler.post(new AnonymousClass5(this, iface));
        }

        /* synthetic */ void lambda$-com_android_server_ethernet_EthernetNetworkFactory$InterfaceObserver_6800(String iface) {
            if (EthernetNetworkFactory.this.stopTrackingInterface(iface)) {
                EthernetNetworkFactory.this.lambda$-com_android_server_ethernet_EthernetNetworkFactory_15568();
            }
        }
    }

    private class LocalNetworkFactory extends NetworkFactory {
        LocalNetworkFactory(String name, Context context, Looper looper) {
            super(looper, context, name, new NetworkCapabilities());
        }

        protected void startNetwork() {
            if (!EthernetNetworkFactory.this.mNetworkRequested) {
                EthernetNetworkFactory.this.mNetworkRequested = EthernetNetworkFactory.DBG;
                EthernetNetworkFactory.this.maybeStartIpManager();
            }
        }

        protected void stopNetwork() {
            EthernetNetworkFactory.this.mNetworkRequested = false;
            EthernetNetworkFactory.this.stopIpManager();
        }
    }

    EthernetNetworkFactory(RemoteCallbackList<IEthernetServiceListener> listeners) {
        initNetworkCapabilities();
        clearInfo();
        this.mListeners = listeners;
    }

    private void clearInfo() {
        this.mLinkProperties = new LinkProperties();
        this.mNetworkInfo = new NetworkInfo(9, 0, NETWORK_TYPE, "");
        this.mNetworkInfo.setExtraInfo(this.mHwAddr);
        this.mNetworkInfo.setIsAvailable(isTrackingInterface());
    }

    private void stopIpManager() {
        if (this.mIpManager != null) {
            this.mIpManager.shutdown();
            this.mIpManager = null;
        }
        this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, this.mHwAddr);
        if (this.mNetworkAgent != null) {
            updateAgent();
            this.mNetworkAgent = null;
        }
        clearInfo();
    }

    private void updateInterfaceState(String iface, boolean up) {
        if (this.mIface.equals(iface)) {
            Log.d(TAG, "updateInterface: " + iface + " link " + (up ? "up" : "down"));
            this.mLinkUp = up;
            if (up) {
                maybeStartIpManager();
            } else {
                stopIpManager();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0078 A:{ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:10:0x0078, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0079, code:
            android.util.Log.e(TAG, "Error upping interface " + r6.mIface + ": " + r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setInterfaceUp(String iface) {
        try {
            this.mNMService.setInterfaceUp(iface);
            InterfaceConfiguration config = this.mNMService.getInterfaceConfig(iface);
            if (config == null) {
                Log.e(TAG, "Null interface config for " + iface + ". Bailing out.");
                return;
            }
            if (isTrackingInterface()) {
                Log.e(TAG, "Interface unexpectedly changed from " + iface + " to " + this.mIface);
                this.mNMService.setInterfaceDown(iface);
            } else {
                setInterfaceInfo(iface, config.getHardwareAddress());
                this.mNetworkInfo.setIsAvailable(DBG);
                this.mNetworkInfo.setExtraInfo(this.mHwAddr);
            }
        } catch (Exception e) {
        }
    }

    private boolean maybeTrackInterface(String iface) {
        if (!iface.matches(mIfaceMatch) || isTrackingInterface()) {
            return false;
        }
        Log.d(TAG, "Started tracking interface " + iface);
        setInterfaceUp(iface);
        return DBG;
    }

    private boolean stopTrackingInterface(String iface) {
        if (!iface.equals(this.mIface)) {
            return false;
        }
        Log.d(TAG, "Stopped tracking interface " + iface);
        setInterfaceInfo("", null);
        stopIpManager();
        return DBG;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x004e A:{ExcHandler: android.os.RemoteException (r1_0 'e' java.lang.Exception), Splitter: B:6:0x0011} */
    /* JADX WARNING: Missing block: B:10:0x004e, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x004f, code:
            android.util.Log.e(TAG, "Setting static IP address failed: " + r1.getMessage());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean setStaticIpAddress(StaticIpConfiguration staticConfig) {
        if (staticConfig.ipAddress == null || staticConfig.gateway == null || staticConfig.dnsServers.size() <= 0) {
            Log.e(TAG, "Invalid static IP configuration.");
        } else {
            try {
                Log.i(TAG, "Applying static IPv4 configuration to " + this.mIface + ": " + staticConfig);
                InterfaceConfiguration config = this.mNMService.getInterfaceConfig(this.mIface);
                config.setLinkAddress(staticConfig.ipAddress);
                this.mNMService.setInterfaceConfig(this.mIface, config);
                return DBG;
            } catch (Exception e) {
            }
        }
        return false;
    }

    public void updateAgent() {
        if (this.mNetworkAgent != null) {
            Log.i(TAG, "Updating mNetworkAgent with: " + this.mNetworkCapabilities + ", " + this.mNetworkInfo + ", " + this.mLinkProperties);
            this.mNetworkAgent.sendNetworkCapabilities(this.mNetworkCapabilities);
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
            this.mNetworkAgent.sendNetworkScore(this.mLinkUp ? NETWORK_SCORE : 0);
        }
    }

    void onIpLayerStarted(LinkProperties linkProperties) {
        if (this.mNetworkAgent != null) {
            Log.e(TAG, "Already have a NetworkAgent - aborting new request");
            stopIpManager();
            return;
        }
        this.mLinkProperties = linkProperties;
        this.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, null, this.mHwAddr);
        this.mNetworkAgent = new NetworkAgent(this.mHandler.getLooper(), this.mContext, NETWORK_TYPE, this.mNetworkInfo, this.mNetworkCapabilities, this.mLinkProperties, NETWORK_SCORE) {
            public void unwanted() {
                if (this == EthernetNetworkFactory.this.mNetworkAgent) {
                    EthernetNetworkFactory.this.stopIpManager();
                } else if (EthernetNetworkFactory.this.mNetworkAgent != null) {
                    Log.d(EthernetNetworkFactory.TAG, "Ignoring unwanted as we have a more modern instance");
                }
            }
        };
    }

    void onIpLayerStopped(LinkProperties linkProperties) {
        stopIpManager();
        maybeStartIpManager();
    }

    void updateLinkProperties(LinkProperties linkProperties) {
        this.mLinkProperties = linkProperties;
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendLinkProperties(linkProperties);
        }
    }

    public void maybeStartIpManager() {
        if (this.mNetworkRequested && this.mIpManager == null && isTrackingInterface()) {
            startIpManager();
        }
    }

    public void startIpManager() {
        Log.d(TAG, String.format("starting IpManager(%s): mNetworkInfo=%s", new Object[]{this.mIface, this.mNetworkInfo}));
        IpConfiguration config = this.mEthernetManager.getConfiguration();
        if (config.getIpAssignment() != IpAssignment.STATIC) {
            this.mNetworkInfo.setDetailedState(DetailedState.OBTAINING_IPADDR, null, this.mHwAddr);
            Callback ipmCallback = new Callback() {
                /* synthetic */ void lambda$-com_android_server_ethernet_EthernetNetworkFactory$2_12831(LinkProperties newLp) {
                    EthernetNetworkFactory.this.onIpLayerStarted(newLp);
                }

                public void onProvisioningSuccess(LinkProperties newLp) {
                    EthernetNetworkFactory.this.mHandler.post(new AnonymousClass3(this, newLp));
                }

                /* synthetic */ void lambda$-com_android_server_ethernet_EthernetNetworkFactory$2_13016(LinkProperties newLp) {
                    EthernetNetworkFactory.this.onIpLayerStopped(newLp);
                }

                public void onProvisioningFailure(LinkProperties newLp) {
                    EthernetNetworkFactory.this.mHandler.post(new com.android.server.ethernet.-$Lambda$eRAo6Nl9jtYC3igbtEDMyHdAcyk.AnonymousClass2(this, newLp));
                }

                /* synthetic */ void lambda$-com_android_server_ethernet_EthernetNetworkFactory$2_13202(LinkProperties newLp) {
                    EthernetNetworkFactory.this.updateLinkProperties(newLp);
                }

                public void onLinkPropertiesChange(LinkProperties newLp) {
                    EthernetNetworkFactory.this.mHandler.post(new com.android.server.ethernet.-$Lambda$eRAo6Nl9jtYC3igbtEDMyHdAcyk.AnonymousClass1(this, newLp));
                }
            };
            stopIpManager();
            this.mIpManager = new IpManager(this.mContext, this.mIface, ipmCallback);
            if (config.getProxySettings() == ProxySettings.STATIC || config.getProxySettings() == ProxySettings.PAC) {
                this.mIpManager.setHttpProxy(config.getHttpProxy());
            }
            String tcpBufferSizes = this.mContext.getResources().getString(17039782);
            if (!TextUtils.isEmpty(tcpBufferSizes)) {
                this.mIpManager.setTcpBufferSizes(tcpBufferSizes);
            }
            IpManager ipManager = this.mIpManager;
            this.mIpManager.startProvisioning(IpManager.buildProvisioningConfiguration().withProvisioningTimeoutMs(0).build());
        } else if (setStaticIpAddress(config.getStaticIpConfiguration())) {
            LinkProperties toLinkProperties = config.getStaticIpConfiguration().toLinkProperties(this.mIface);
        }
    }

    public void start(Context context, Handler handler) {
        this.mHandler = handler;
        this.mNMService = Stub.asInterface(ServiceManager.getService("network_management"));
        this.mEthernetManager = (EthernetManager) context.getSystemService("ethernet");
        mIfaceMatch = context.getResources().getString(17039781);
        this.mFactory = new LocalNetworkFactory(NETWORK_TYPE, context, this.mHandler.getLooper());
        this.mFactory.setCapabilityFilter(this.mNetworkCapabilities);
        this.mFactory.setScoreFilter(NETWORK_SCORE);
        this.mFactory.register();
        this.mContext = context;
        this.mInterfaceObserver = new InterfaceObserver(this, null);
        try {
            this.mNMService.registerObserver(this.mInterfaceObserver);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not register InterfaceObserver " + e);
        }
        this.mHandler.post(new -$Lambda$eRAo6Nl9jtYC3igbtEDMyHdAcyk(this));
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0029 A:{ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:9:0x0029, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x002a, code:
            android.util.Log.e(TAG, "Could not get list of interfaces " + r0);
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    /* renamed from: trackFirstAvailableInterface */
    public void lambda$-com_android_server_ethernet_EthernetNetworkFactory_15568() {
        try {
            String[] ifaces = this.mNMService.listInterfaces();
            int i = 0;
            int length = ifaces.length;
            while (i < length) {
                String iface = ifaces[i];
                if (!maybeTrackInterface(iface)) {
                    i++;
                } else if (this.mNMService.getInterfaceConfig(iface).hasFlag("running")) {
                    updateInterfaceState(iface, DBG);
                    return;
                } else {
                    return;
                }
            }
        } catch (Exception e) {
        }
    }

    public void stop() {
        stopIpManager();
        setInterfaceInfo("", null);
        this.mFactory.unregister();
    }

    private void initNetworkCapabilities() {
        this.mNetworkCapabilities = new NetworkCapabilities();
        this.mNetworkCapabilities.addTransportType(3);
        this.mNetworkCapabilities.addCapability(12);
        this.mNetworkCapabilities.addCapability(13);
        this.mNetworkCapabilities.setLinkUpstreamBandwidthKbps(100000);
        this.mNetworkCapabilities.setLinkDownstreamBandwidthKbps(100000);
    }

    public boolean isTrackingInterface() {
        return TextUtils.isEmpty(this.mIface) ^ 1;
    }

    private void setInterfaceInfo(String iface, String hwAddr) {
        boolean oldAvailable = isTrackingInterface();
        this.mIface = iface;
        this.mHwAddr = hwAddr;
        boolean available = isTrackingInterface();
        this.mNetworkInfo.setExtraInfo(this.mHwAddr);
        this.mNetworkInfo.setIsAvailable(available);
        if (oldAvailable != available) {
            int n = this.mListeners.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ((IEthernetServiceListener) this.mListeners.getBroadcastItem(i)).onAvailabilityChanged(available);
                } catch (RemoteException e) {
                }
            }
            this.mListeners.finishBroadcast();
        }
    }

    private void postAndWaitForRunnable(Runnable r) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        this.mHandler.post(new AnonymousClass6(latch, r));
        latch.await();
    }

    static /* synthetic */ void lambda$-com_android_server_ethernet_EthernetNetworkFactory_18300(CountDownLatch latch, Runnable r) {
        try {
            r.run();
        } finally {
            latch.countDown();
        }
    }

    void dump(FileDescriptor fd, IndentingPrintWriter pw, String[] args) {
        try {
            postAndWaitForRunnable(new AnonymousClass7(this, pw, fd, args));
        } catch (InterruptedException e) {
            throw new IllegalStateException("dump() interrupted");
        }
    }

    /* synthetic */ void lambda$-com_android_server_ethernet_EthernetNetworkFactory_18591(IndentingPrintWriter pw, FileDescriptor fd, String[] args) {
        pw.println("Network Requested: " + this.mNetworkRequested);
        if (isTrackingInterface()) {
            pw.println("Tracking interface: " + this.mIface);
            pw.increaseIndent();
            pw.println("MAC address: " + this.mHwAddr);
            pw.println("Link state: " + (this.mLinkUp ? "up" : "down"));
            pw.decreaseIndent();
        } else {
            pw.println("Not tracking any interface");
        }
        pw.println();
        pw.println("NetworkInfo: " + this.mNetworkInfo);
        pw.println("LinkProperties: " + this.mLinkProperties);
        pw.println("NetworkAgent: " + this.mNetworkAgent);
        if (this.mIpManager != null) {
            pw.println("IpManager:");
            pw.increaseIndent();
            this.mIpManager.dump(fd, pw, args);
            pw.decreaseIndent();
        }
    }
}
