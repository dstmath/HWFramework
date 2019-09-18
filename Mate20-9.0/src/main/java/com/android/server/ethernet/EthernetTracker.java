package com.android.server.ethernet;

import android.content.Context;
import android.net.IEthernetServiceListener;
import android.net.InterfaceConfiguration;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkCapabilities;
import android.net.StaticIpConfiguration;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.net.BaseNetworkObserver;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

final class EthernetTracker {
    private static final boolean DBG = true;
    /* access modifiers changed from: private */
    public static final String TAG = EthernetTracker.class.getSimpleName();
    private final EthernetConfigStore mConfigStore;
    private final EthernetNetworkFactory mFactory;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final String mIfaceMatch;
    private volatile IpConfiguration mIpConfigForDefaultInterface;
    private final ConcurrentHashMap<String, IpConfiguration> mIpConfigurations = new ConcurrentHashMap<>();
    private final RemoteCallbackList<IEthernetServiceListener> mListeners = new RemoteCallbackList<>();
    private final INetworkManagementService mNMService;
    private final ConcurrentHashMap<String, NetworkCapabilities> mNetworkCapabilities = new ConcurrentHashMap<>();

    private class InterfaceObserver extends BaseNetworkObserver {
        private InterfaceObserver() {
        }

        public void interfaceLinkStateChanged(String iface, boolean up) {
            String access$100 = EthernetTracker.TAG;
            Log.i(access$100, "interfaceLinkStateChanged, iface: " + iface + ", up: " + up);
            EthernetTracker.this.mHandler.post(new Runnable(iface, up) {
                private final /* synthetic */ String f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    EthernetTracker.this.updateInterfaceState(this.f$1, this.f$2);
                }
            });
        }

        public void interfaceAdded(String iface) {
            EthernetTracker.this.mHandler.post(new Runnable(iface) {
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    EthernetTracker.this.maybeTrackInterface(this.f$1);
                }
            });
        }

        public void interfaceRemoved(String iface) {
            EthernetTracker.this.mHandler.post(new Runnable(iface) {
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    EthernetTracker.this.removeInterface(this.f$1);
                }
            });
        }
    }

    private static class ListenerInfo {
        boolean canUseRestrictedNetworks = false;

        ListenerInfo(boolean canUseRestrictedNetworks2) {
            this.canUseRestrictedNetworks = canUseRestrictedNetworks2;
        }
    }

    EthernetTracker(Context context, Handler handler) {
        this.mHandler = handler;
        this.mNMService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        this.mIfaceMatch = context.getResources().getString(17039803);
        for (String strConfig : context.getResources().getStringArray(17236009)) {
            parseEthernetConfig(strConfig);
        }
        this.mConfigStore = new EthernetConfigStore();
        this.mFactory = new EthernetNetworkFactory(handler, context, createNetworkCapabilities(DBG));
        this.mFactory.register();
    }

    /* access modifiers changed from: package-private */
    public void start() {
        this.mConfigStore.read();
        this.mIpConfigForDefaultInterface = this.mConfigStore.getIpConfigurationForDefaultInterface();
        ArrayMap<String, IpConfiguration> configs = this.mConfigStore.getIpConfigurations();
        for (int i = 0; i < configs.size(); i++) {
            this.mIpConfigurations.put(configs.keyAt(i), configs.valueAt(i));
        }
        try {
            this.mNMService.registerObserver(new InterfaceObserver());
        } catch (RemoteException e) {
            String str = TAG;
            Log.e(str, "Could not register InterfaceObserver " + e);
        }
        this.mHandler.post(new Runnable() {
            public final void run() {
                EthernetTracker.this.trackAvailableInterfaces();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void updateIpConfiguration(String iface, IpConfiguration ipConfiguration) {
        String str = TAG;
        Log.i(str, "updateIpConfiguration, iface: " + iface + ", cfg: " + ipConfiguration);
        this.mConfigStore.write(iface, ipConfiguration);
        this.mIpConfigurations.put(iface, ipConfiguration);
        this.mHandler.post(new Runnable(iface, ipConfiguration) {
            private final /* synthetic */ String f$1;
            private final /* synthetic */ IpConfiguration f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                EthernetTracker.this.mFactory.updateIpConfiguration(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public IpConfiguration getIpConfiguration(String iface) {
        return this.mIpConfigurations.get(iface);
    }

    /* access modifiers changed from: package-private */
    public boolean isTrackingInterface(String iface) {
        return this.mFactory.hasInterface(iface);
    }

    /* access modifiers changed from: package-private */
    public String[] getInterfaces(boolean includeRestricted) {
        return this.mFactory.getAvailableInterfaces(includeRestricted);
    }

    /* access modifiers changed from: package-private */
    public boolean isRestrictedInterface(String iface) {
        NetworkCapabilities nc = this.mNetworkCapabilities.get(iface);
        if (nc == null || nc.hasCapability(13)) {
            return false;
        }
        return DBG;
    }

    /* access modifiers changed from: package-private */
    public void addListener(IEthernetServiceListener listener, boolean canUseRestrictedNetworks) {
        this.mListeners.register(listener, new ListenerInfo(canUseRestrictedNetworks));
    }

    /* access modifiers changed from: package-private */
    public void removeListener(IEthernetServiceListener listener) {
        this.mListeners.unregister(listener);
    }

    /* access modifiers changed from: private */
    public void removeInterface(String iface) {
        this.mFactory.removeInterface(iface);
    }

    private void addInterface(String iface) {
        InterfaceConfiguration config = null;
        try {
            this.mNMService.setInterfaceUp(iface);
            config = this.mNMService.getInterfaceConfig(iface);
        } catch (RemoteException | IllegalStateException e) {
            String str = TAG;
            Log.e(str, "Error upping interface " + iface, e);
        }
        if (config == null) {
            String str2 = TAG;
            Log.e(str2, "Null interface config for " + iface + ". Bailing out.");
            return;
        }
        String hwAddress = config.getHardwareAddress();
        NetworkCapabilities nc = this.mNetworkCapabilities.get(iface);
        if (nc == null) {
            nc = this.mNetworkCapabilities.get(hwAddress);
            if (nc == null) {
                nc = createDefaultNetworkCapabilities();
            }
        }
        IpConfiguration ipConfiguration = this.mIpConfigurations.get(iface);
        if (ipConfiguration == null) {
            ipConfiguration = createDefaultIpConfiguration();
        }
        String str3 = TAG;
        Log.d(str3, "Started tracking interface " + iface);
        this.mFactory.addInterface(iface, hwAddress, nc, ipConfiguration);
        if (config.hasFlag("running")) {
            updateInterfaceState(iface, DBG);
        }
    }

    /* access modifiers changed from: private */
    public void updateInterfaceState(String iface, boolean up) {
        if (this.mFactory.updateInterfaceLinkState(iface, up)) {
            boolean restricted = isRestrictedInterface(iface);
            int n = this.mListeners.beginBroadcast();
            for (int i = 0; i < n; i++) {
                if (restricted) {
                    try {
                        if (!((ListenerInfo) this.mListeners.getBroadcastCookie(i)).canUseRestrictedNetworks) {
                        }
                    } catch (RemoteException e) {
                    }
                }
                this.mListeners.getBroadcastItem(i).onAvailabilityChanged(iface, up);
            }
            this.mListeners.finishBroadcast();
        }
    }

    /* access modifiers changed from: private */
    public void maybeTrackInterface(String iface) {
        String str = TAG;
        Log.i(str, "maybeTrackInterface " + iface);
        if (iface.matches(this.mIfaceMatch) && !this.mFactory.hasInterface(iface)) {
            if (this.mIpConfigForDefaultInterface != null) {
                updateIpConfiguration(iface, this.mIpConfigForDefaultInterface);
                this.mIpConfigForDefaultInterface = null;
            }
            addInterface(iface);
        }
    }

    /* access modifiers changed from: private */
    public void trackAvailableInterfaces() {
        try {
            for (String iface : this.mNMService.listInterfaces()) {
                maybeTrackInterface(iface);
            }
        } catch (RemoteException | IllegalStateException e) {
            Log.e(TAG, "Could not get list of interfaces " + e);
        }
    }

    private void parseEthernetConfig(String configString) {
        String[] tokens = configString.split(";");
        String name = tokens[0];
        String capabilities = tokens.length > 1 ? tokens[1] : null;
        this.mNetworkCapabilities.put(name, createNetworkCapabilities(true ^ TextUtils.isEmpty(capabilities), capabilities));
        if (tokens.length > 2 && !TextUtils.isEmpty(tokens[2])) {
            this.mIpConfigurations.put(name, parseStaticIpConfiguration(tokens[2]));
        }
    }

    private static NetworkCapabilities createDefaultNetworkCapabilities() {
        NetworkCapabilities nc = createNetworkCapabilities(false);
        nc.addCapability(12);
        nc.addCapability(13);
        nc.addCapability(11);
        nc.addCapability(18);
        nc.addCapability(20);
        return nc;
    }

    private static NetworkCapabilities createNetworkCapabilities(boolean clearDefaultCapabilities) {
        return createNetworkCapabilities(clearDefaultCapabilities, null);
    }

    private static NetworkCapabilities createNetworkCapabilities(boolean clearDefaultCapabilities, String commaSeparatedCapabilities) {
        NetworkCapabilities nc = new NetworkCapabilities();
        if (clearDefaultCapabilities) {
            nc.clearAll();
        }
        nc.addTransportType(3);
        nc.setLinkUpstreamBandwidthKbps(100000);
        nc.setLinkDownstreamBandwidthKbps(100000);
        if (!TextUtils.isEmpty(commaSeparatedCapabilities)) {
            for (String strNetworkCapability : commaSeparatedCapabilities.split(",")) {
                if (!TextUtils.isEmpty(strNetworkCapability)) {
                    nc.addCapability(Integer.valueOf(strNetworkCapability).intValue());
                }
            }
        }
        return nc;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006a, code lost:
        if (r7.equals("gateway") != false) goto L_0x006e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b1  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00b8  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00bb  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0071 A[SYNTHETIC] */
    @VisibleForTesting
    static IpConfiguration parseStaticIpConfiguration(String staticIpConfig) {
        StaticIpConfiguration ipConfig = new StaticIpConfiguration();
        for (String keyValueAsString : staticIpConfig.trim().split(" ")) {
            if (!TextUtils.isEmpty(keyValueAsString)) {
                String[] pair = keyValueAsString.split("=");
                char c = 2;
                if (pair.length == 2) {
                    String key = pair[0];
                    String value = pair[1];
                    int hashCode = key.hashCode();
                    if (hashCode != -189118908) {
                        if (hashCode == 3367) {
                            if (key.equals("ip")) {
                                c = 0;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                }
                            }
                        } else if (hashCode == 99625) {
                            if (key.equals("dns")) {
                                c = 3;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                }
                            }
                        } else if (hashCode == 1837548591 && key.equals("domains")) {
                            c = 1;
                            switch (c) {
                                case 0:
                                    ipConfig.ipAddress = new LinkAddress(value);
                                    break;
                                case 1:
                                    ipConfig.domains = value;
                                    break;
                                case 2:
                                    ipConfig.gateway = InetAddress.parseNumericAddress(value);
                                    break;
                                case 3:
                                    ArrayList<InetAddress> dnsAddresses = new ArrayList<>();
                                    for (String address : value.split(",")) {
                                        dnsAddresses.add(InetAddress.parseNumericAddress(address));
                                    }
                                    ipConfig.dnsServers.addAll(dnsAddresses);
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unexpected key: " + key + " in " + staticIpConfig);
                            }
                        }
                    }
                    c = 65535;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                } else {
                    throw new IllegalArgumentException("Unexpected token: " + keyValueAsString + " in " + staticIpConfig);
                }
            }
        }
        return new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, ipConfig, null);
    }

    private static IpConfiguration createDefaultIpConfiguration() {
        return new IpConfiguration(IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, null, null);
    }

    private void postAndWaitForRunnable(Runnable r) {
        this.mHandler.runWithScissors(r, 2000);
    }

    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, IndentingPrintWriter pw, String[] args) {
        postAndWaitForRunnable(new Runnable(pw, fd, args) {
            private final /* synthetic */ IndentingPrintWriter f$1;
            private final /* synthetic */ FileDescriptor f$2;
            private final /* synthetic */ String[] f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                EthernetTracker.lambda$dump$1(EthernetTracker.this, this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public static /* synthetic */ void lambda$dump$1(EthernetTracker ethernetTracker, IndentingPrintWriter pw, FileDescriptor fd, String[] args) {
        pw.println(ethernetTracker.getClass().getSimpleName());
        pw.println("Ethernet interface name filter: " + ethernetTracker.mIfaceMatch);
        pw.println("Listeners: " + ethernetTracker.mListeners.getRegisteredCallbackCount());
        pw.println("IP Configurations:");
        pw.increaseIndent();
        for (String iface : ethernetTracker.mIpConfigurations.keySet()) {
            pw.println(iface + ": " + ethernetTracker.mIpConfigurations.get(iface));
        }
        pw.decreaseIndent();
        pw.println();
        pw.println("Network Capabilities:");
        pw.increaseIndent();
        for (String iface2 : ethernetTracker.mNetworkCapabilities.keySet()) {
            pw.println(iface2 + ": " + ethernetTracker.mNetworkCapabilities.get(iface2));
        }
        pw.decreaseIndent();
        pw.println();
        ethernetTracker.mFactory.dump(fd, pw, args);
    }
}
