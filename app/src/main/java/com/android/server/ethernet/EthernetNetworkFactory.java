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
import android.net.ip.IpManager.WaitForProvisioningCallback;
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
import com.android.server.net.BaseNetworkObserver;
import java.io.FileDescriptor;

class EthernetNetworkFactory {
    private static final boolean DBG = true;
    private static final int NETWORK_SCORE = 70;
    private static final String NETWORK_TYPE = "Ethernet";
    private static final String TAG = "EthernetNetworkFactory";
    private static String mIface;
    private static String mIfaceMatch;
    private static boolean mLinkUp;
    private Context mContext;
    private EthernetManager mEthernetManager;
    private LocalNetworkFactory mFactory;
    private String mHwAddr;
    private InterfaceObserver mInterfaceObserver;
    private IpManager mIpManager;
    private Thread mIpProvisioningThread;
    private LinkProperties mLinkProperties;
    private final RemoteCallbackList<IEthernetServiceListener> mListeners;
    private INetworkManagementService mNMService;
    private NetworkAgent mNetworkAgent;
    private NetworkCapabilities mNetworkCapabilities;
    private NetworkInfo mNetworkInfo;

    private class InterfaceObserver extends BaseNetworkObserver {
        private InterfaceObserver() {
        }

        public void interfaceLinkStateChanged(String iface, boolean up) {
            EthernetNetworkFactory.this.updateInterfaceState(iface, up);
        }

        public void interfaceAdded(String iface) {
            EthernetNetworkFactory.this.maybeTrackInterface(iface);
        }

        public void interfaceRemoved(String iface) {
            EthernetNetworkFactory.this.stopTrackingInterface(iface);
        }
    }

    private class LocalNetworkFactory extends NetworkFactory {
        LocalNetworkFactory(String name, Context context, Looper looper) {
            super(looper, context, name, new NetworkCapabilities());
        }

        protected void startNetwork() {
            EthernetNetworkFactory.this.onRequestNetwork();
        }

        protected void stopNetwork() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.ethernet.EthernetNetworkFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.ethernet.EthernetNetworkFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.ethernet.EthernetNetworkFactory.<clinit>():void");
    }

    EthernetNetworkFactory(RemoteCallbackList<IEthernetServiceListener> listeners) {
        this.mNetworkInfo = new NetworkInfo(9, 0, NETWORK_TYPE, "");
        this.mLinkProperties = new LinkProperties();
        initNetworkCapabilities();
        this.mListeners = listeners;
    }

    private void stopIpManagerLocked() {
        if (this.mIpManager != null) {
            this.mIpManager.shutdown();
            this.mIpManager = null;
        }
    }

    private void stopIpProvisioningThreadLocked() {
        stopIpManagerLocked();
        if (this.mIpProvisioningThread != null) {
            this.mIpProvisioningThread.interrupt();
            this.mIpProvisioningThread = null;
        }
    }

    private void updateInterfaceState(String iface, boolean up) {
        if (mIface.equals(iface)) {
            Log.d(TAG, "updateInterface: " + iface + " link " + (up ? "up" : "down"));
            synchronized (this) {
                int i;
                mLinkUp = up;
                this.mNetworkInfo.setIsAvailable(up);
                if (!up) {
                    this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, this.mHwAddr);
                    stopIpProvisioningThreadLocked();
                }
                updateAgent();
                LocalNetworkFactory localNetworkFactory = this.mFactory;
                if (up) {
                    i = NETWORK_SCORE;
                } else {
                    i = -1;
                }
                localNetworkFactory.setScoreFilter(i);
            }
        }
    }

    private void setInterfaceUp(String iface) {
        try {
            this.mNMService.setInterfaceUp(iface);
            InterfaceConfiguration config = this.mNMService.getInterfaceConfig(iface);
            if (config == null) {
                Log.e(TAG, "Null iterface config for " + iface + ". Bailing out.");
                return;
            }
            synchronized (this) {
                if (isTrackingInterface()) {
                    Log.e(TAG, "Interface unexpectedly changed from " + iface + " to " + mIface);
                    this.mNMService.setInterfaceDown(iface);
                } else {
                    setInterfaceInfoLocked(iface, config.getHardwareAddress());
                    this.mNetworkInfo.setIsAvailable(DBG);
                    this.mNetworkInfo.setExtraInfo(this.mHwAddr);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error upping interface " + mIface + ": " + e);
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

    private void stopTrackingInterface(String iface) {
        if (iface.equals(mIface)) {
            Log.d(TAG, "Stopped tracking interface " + iface);
            synchronized (this) {
                stopIpProvisioningThreadLocked();
                setInterfaceInfoLocked("", null);
                this.mNetworkInfo.setExtraInfo(null);
                mLinkUp = false;
                this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, this.mHwAddr);
                updateAgent();
                this.mNetworkAgent = null;
                this.mNetworkInfo = new NetworkInfo(9, 0, NETWORK_TYPE, "");
                this.mLinkProperties = new LinkProperties();
            }
        }
    }

    private boolean setStaticIpAddress(StaticIpConfiguration staticConfig) {
        if (staticConfig.ipAddress == null || staticConfig.gateway == null || staticConfig.dnsServers.size() <= 0) {
            Log.e(TAG, "Invalid static IP configuration.");
        } else {
            try {
                Log.i(TAG, "Applying static IPv4 configuration to " + mIface + ": " + staticConfig);
                InterfaceConfiguration config = this.mNMService.getInterfaceConfig(mIface);
                config.setLinkAddress(staticConfig.ipAddress);
                this.mNMService.setInterfaceConfig(mIface, config);
                return DBG;
            } catch (Exception e) {
                Log.e(TAG, "Setting static IP address failed: " + e.getMessage());
            }
        }
        return false;
    }

    public void updateAgent() {
        synchronized (this) {
            if (this.mNetworkAgent == null) {
                return;
            }
            int i;
            Log.i(TAG, "Updating mNetworkAgent with: " + this.mNetworkCapabilities + ", " + this.mNetworkInfo + ", " + this.mLinkProperties);
            this.mNetworkAgent.sendNetworkCapabilities(this.mNetworkCapabilities);
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
            NetworkAgent networkAgent = this.mNetworkAgent;
            if (mLinkUp) {
                i = NETWORK_SCORE;
            } else {
                i = 0;
            }
            networkAgent.sendNetworkScore(i);
        }
    }

    public void onRequestNetwork() {
        synchronized (this) {
            if (this.mIpProvisioningThread != null) {
                return;
            }
            Thread ipProvisioningThread = new Thread(new Runnable() {

                /* renamed from: com.android.server.ethernet.EthernetNetworkFactory.1.2 */
                class AnonymousClass2 extends NetworkAgent {
                    AnonymousClass2(Looper $anonymous0, Context $anonymous1, String $anonymous2, NetworkInfo $anonymous3, NetworkCapabilities $anonymous4, LinkProperties $anonymous5, int $anonymous6) {
                        super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6);
                    }

                    public void unwanted() {
                        synchronized (EthernetNetworkFactory.this) {
                            if (this == EthernetNetworkFactory.this.mNetworkAgent) {
                                EthernetNetworkFactory.this.stopIpManagerLocked();
                                EthernetNetworkFactory.this.mLinkProperties.clear();
                                EthernetNetworkFactory.this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, EthernetNetworkFactory.this.mHwAddr);
                                EthernetNetworkFactory.this.updateAgent();
                                EthernetNetworkFactory.this.mNetworkAgent = null;
                                try {
                                    EthernetNetworkFactory.this.mNMService.clearInterfaceAddresses(EthernetNetworkFactory.mIface);
                                } catch (Exception e) {
                                    Log.e(EthernetNetworkFactory.TAG, "Failed to clear addresses or disable ipv6" + e);
                                }
                            } else {
                                Log.d(EthernetNetworkFactory.TAG, "Ignoring unwanted as we have a more modern instance");
                            }
                        }
                    }
                }

                public void run() {
                    LinkProperties linkProperties;
                    Log.d(EthernetNetworkFactory.TAG, String.format("starting ipProvisioningThread(%s): mNetworkInfo=%s", new Object[]{EthernetNetworkFactory.mIface, EthernetNetworkFactory.this.mNetworkInfo}));
                    IpConfiguration config = EthernetNetworkFactory.this.mEthernetManager.getConfiguration();
                    if (config.getIpAssignment() != IpAssignment.STATIC) {
                        EthernetNetworkFactory.this.mNetworkInfo.setDetailedState(DetailedState.OBTAINING_IPADDR, null, EthernetNetworkFactory.this.mHwAddr);
                        WaitForProvisioningCallback ipmCallback = new WaitForProvisioningCallback() {
                            public void onLinkPropertiesChange(LinkProperties newLp) {
                                synchronized (EthernetNetworkFactory.this) {
                                    if (EthernetNetworkFactory.this.mNetworkAgent != null && EthernetNetworkFactory.this.mNetworkInfo.isConnected()) {
                                        EthernetNetworkFactory.this.mLinkProperties = newLp;
                                        EthernetNetworkFactory.this.mNetworkAgent.sendLinkProperties(newLp);
                                    }
                                }
                            }
                        };
                        synchronized (EthernetNetworkFactory.this) {
                            EthernetNetworkFactory.this.stopIpManagerLocked();
                            EthernetNetworkFactory.this.mIpManager = new IpManager(EthernetNetworkFactory.this.mContext, EthernetNetworkFactory.mIface, ipmCallback);
                            if (config.getProxySettings() == ProxySettings.STATIC || config.getProxySettings() == ProxySettings.PAC) {
                                EthernetNetworkFactory.this.mIpManager.setHttpProxy(config.getHttpProxy());
                            }
                            String tcpBufferSizes = EthernetNetworkFactory.this.mContext.getResources().getString(17039451);
                            if (!TextUtils.isEmpty(tcpBufferSizes)) {
                                EthernetNetworkFactory.this.mIpManager.setTcpBufferSizes(tcpBufferSizes);
                            }
                            EthernetNetworkFactory.this.mIpManager;
                            EthernetNetworkFactory.this.mIpManager.startProvisioning(IpManager.buildProvisioningConfiguration().withProvisioningTimeoutMs(0).build());
                        }
                        linkProperties = ipmCallback.waitForProvisioning();
                        if (linkProperties == null) {
                            Log.e(EthernetNetworkFactory.TAG, "IP provisioning error");
                            EthernetNetworkFactory.this.mFactory.setScoreFilter(-1);
                            synchronized (EthernetNetworkFactory.this) {
                                EthernetNetworkFactory.this.stopIpManagerLocked();
                            }
                            return;
                        }
                    } else if (EthernetNetworkFactory.this.setStaticIpAddress(config.getStaticIpConfiguration())) {
                        linkProperties = config.getStaticIpConfiguration().toLinkProperties(EthernetNetworkFactory.mIface);
                    } else {
                        return;
                    }
                    synchronized (EthernetNetworkFactory.this) {
                        if (EthernetNetworkFactory.this.mNetworkAgent != null) {
                            Log.e(EthernetNetworkFactory.TAG, "Already have a NetworkAgent - aborting new request");
                            EthernetNetworkFactory.this.stopIpManagerLocked();
                            EthernetNetworkFactory.this.mIpProvisioningThread = null;
                            return;
                        }
                        EthernetNetworkFactory.this.mLinkProperties = linkProperties;
                        EthernetNetworkFactory.this.mNetworkInfo.setIsAvailable(EthernetNetworkFactory.DBG);
                        EthernetNetworkFactory.this.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, null, EthernetNetworkFactory.this.mHwAddr);
                        EthernetNetworkFactory ethernetNetworkFactory = EthernetNetworkFactory.this;
                        EthernetNetworkFactory ethernetNetworkFactory2 = ethernetNetworkFactory;
                        ethernetNetworkFactory2.mNetworkAgent = new AnonymousClass2(EthernetNetworkFactory.this.mFactory.getLooper(), EthernetNetworkFactory.this.mContext, EthernetNetworkFactory.NETWORK_TYPE, EthernetNetworkFactory.this.mNetworkInfo, EthernetNetworkFactory.this.mNetworkCapabilities, EthernetNetworkFactory.this.mLinkProperties, EthernetNetworkFactory.NETWORK_SCORE);
                        EthernetNetworkFactory.this.mIpProvisioningThread = null;
                        Log.d(EthernetNetworkFactory.TAG, String.format("exiting ipProvisioningThread(%s): mNetworkInfo=%s", new Object[]{EthernetNetworkFactory.mIface, EthernetNetworkFactory.this.mNetworkInfo}));
                    }
                }
            });
            synchronized (this) {
                if (this.mIpProvisioningThread == null) {
                    this.mIpProvisioningThread = ipProvisioningThread;
                    this.mIpProvisioningThread.start();
                }
            }
        }
    }

    public synchronized void start(Context context, Handler target) {
        this.mNMService = Stub.asInterface(ServiceManager.getService("network_management"));
        this.mEthernetManager = (EthernetManager) context.getSystemService("ethernet");
        mIfaceMatch = context.getResources().getString(17039411);
        this.mFactory = new LocalNetworkFactory(NETWORK_TYPE, context, target.getLooper());
        this.mFactory.setCapabilityFilter(this.mNetworkCapabilities);
        this.mFactory.setScoreFilter(-1);
        this.mFactory.register();
        this.mContext = context;
        this.mInterfaceObserver = new InterfaceObserver();
        try {
            this.mNMService.registerObserver(this.mInterfaceObserver);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not register InterfaceObserver " + e);
        }
        try {
            for (String iface : this.mNMService.listInterfaces()) {
                synchronized (this) {
                    if (maybeTrackInterface(iface)) {
                        if (this.mNMService.getInterfaceConfig(iface).hasFlag("running")) {
                            updateInterfaceState(iface, DBG);
                        }
                    }
                }
            }
        } catch (Exception e2) {
            Log.e(TAG, "Could not get list of interfaces " + e2);
        }
    }

    public synchronized void stop() {
        stopIpProvisioningThreadLocked();
        this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, this.mHwAddr);
        mLinkUp = false;
        updateAgent();
        this.mLinkProperties = new LinkProperties();
        this.mNetworkAgent = null;
        setInterfaceInfoLocked("", null);
        this.mNetworkInfo = new NetworkInfo(9, 0, NETWORK_TYPE, "");
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

    public synchronized boolean isTrackingInterface() {
        return TextUtils.isEmpty(mIface) ? false : DBG;
    }

    private void setInterfaceInfoLocked(String iface, String hwAddr) {
        boolean oldAvailable = isTrackingInterface();
        mIface = iface;
        this.mHwAddr = hwAddr;
        boolean available = isTrackingInterface();
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

    synchronized void dump(FileDescriptor fd, IndentingPrintWriter pw, String[] args) {
        if (isTrackingInterface()) {
            pw.println("Tracking interface: " + mIface);
            pw.increaseIndent();
            pw.println("MAC address: " + this.mHwAddr);
            pw.println("Link state: " + (mLinkUp ? "up" : "down"));
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
