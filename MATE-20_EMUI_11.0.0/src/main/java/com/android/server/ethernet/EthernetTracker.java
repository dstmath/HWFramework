package com.android.server.ethernet;

import android.content.Context;
import android.net.IEthernetServiceListener;
import android.net.InterfaceConfiguration;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkCapabilities;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.ethernet.EthernetTracker;
import com.android.server.net.BaseNetworkObserver;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public final class EthernetTracker {
    private static final boolean DBG = true;
    private static final String TAG = EthernetTracker.class.getSimpleName();
    private final EthernetConfigStore mConfigStore;
    private final EthernetNetworkFactory mFactory;
    private final Handler mHandler;
    private final String mIfaceMatch;
    private volatile IpConfiguration mIpConfigForDefaultInterface;
    private final ConcurrentHashMap<String, IpConfiguration> mIpConfigurations = new ConcurrentHashMap<>();
    private final RemoteCallbackList<IEthernetServiceListener> mListeners = new RemoteCallbackList<>();
    private final INetworkManagementService mNMService;
    private final ConcurrentHashMap<String, NetworkCapabilities> mNetworkCapabilities = new ConcurrentHashMap<>();

    EthernetTracker(Context context, Handler handler) {
        this.mHandler = handler;
        this.mNMService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        this.mIfaceMatch = context.getResources().getString(17039848);
        for (String strConfig : context.getResources().getStringArray(17236021)) {
            parseEthernetConfig(strConfig);
        }
        this.mConfigStore = new EthernetConfigStore();
        this.mFactory = new EthernetNetworkFactory(handler, context, createNetworkCapabilities(DBG));
        this.mFactory.register();
    }

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
            /* class com.android.server.ethernet.$$Lambda$EthernetTracker$7ZSuSvoSqcExye5DLwv_gyq6gyM */

            @Override // java.lang.Runnable
            public final void run() {
                EthernetTracker.lambda$7ZSuSvoSqcExye5DLwv_gyq6gyM(EthernetTracker.this);
            }
        });
    }

    public void updateIpConfiguration(String iface, IpConfiguration ipConfiguration) {
        String str = TAG;
        Log.i(str, "updateIpConfiguration, iface: " + iface + ", cfg: " + ipConfiguration);
        this.mConfigStore.write(iface, ipConfiguration);
        this.mIpConfigurations.put(iface, ipConfiguration);
        this.mHandler.post(new Runnable(iface, ipConfiguration) {
            /* class com.android.server.ethernet.$$Lambda$EthernetTracker$WrfGoZ0jmrS_2ZYW4ZE33ZnJcBI */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ IpConfiguration f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                EthernetTracker.this.lambda$updateIpConfiguration$0$EthernetTracker(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$updateIpConfiguration$0$EthernetTracker(String iface, IpConfiguration ipConfiguration) {
        this.mFactory.updateIpConfiguration(iface, ipConfiguration);
    }

    public IpConfiguration getIpConfiguration(String iface) {
        return this.mIpConfigurations.get(iface);
    }

    public boolean isTrackingInterface(String iface) {
        return this.mFactory.hasInterface(iface);
    }

    public String[] getInterfaces(boolean includeRestricted) {
        return this.mFactory.getAvailableInterfaces(includeRestricted);
    }

    public boolean isRestrictedInterface(String iface) {
        NetworkCapabilities nc = this.mNetworkCapabilities.get(iface);
        if (nc == null || nc.hasCapability(13)) {
            return false;
        }
        return DBG;
    }

    public void addListener(IEthernetServiceListener listener, boolean canUseRestrictedNetworks) {
        this.mListeners.register(listener, new ListenerInfo(canUseRestrictedNetworks));
    }

    public void removeListener(IEthernetServiceListener listener) {
        this.mListeners.unregister(listener);
    }

    private void removeInterface(String iface) {
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
        if (nc == null && (nc = this.mNetworkCapabilities.get(hwAddress)) == null) {
            nc = createDefaultNetworkCapabilities();
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

    private void updateInterfaceState(String iface, boolean up) {
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

    private void maybeTrackInterface(String iface) {
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

    /* access modifiers changed from: public */
    private void trackAvailableInterfaces() {
        try {
            for (String iface : this.mNMService.listInterfaces()) {
                maybeTrackInterface(iface);
            }
        } catch (RemoteException | IllegalStateException e) {
            Log.e(TAG, "Could not get list of interfaces " + e);
        }
    }

    public class InterfaceObserver extends BaseNetworkObserver {
        private InterfaceObserver() {
            EthernetTracker.this = r1;
        }

        public void interfaceLinkStateChanged(String iface, boolean up) {
            String str = EthernetTracker.TAG;
            Log.i(str, "interfaceLinkStateChanged, iface: " + iface + ", up: " + up);
            EthernetTracker.this.mHandler.post(new Runnable(iface, up) {
                /* class com.android.server.ethernet.$$Lambda$EthernetTracker$InterfaceObserver$RwJVEk3mzxwZqyoQwiconpRi8 */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    EthernetTracker.InterfaceObserver.this.lambda$interfaceLinkStateChanged$0$EthernetTracker$InterfaceObserver(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$interfaceLinkStateChanged$0$EthernetTracker$InterfaceObserver(String iface, boolean up) {
            EthernetTracker.this.updateInterfaceState(iface, up);
        }

        public void interfaceAdded(String iface) {
            EthernetTracker.this.mHandler.post(new Runnable(iface) {
                /* class com.android.server.ethernet.$$Lambda$EthernetTracker$InterfaceObserver$d1ixKZZuAxwm1Dz_AX3HmL4JVLA */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    EthernetTracker.InterfaceObserver.this.lambda$interfaceAdded$1$EthernetTracker$InterfaceObserver(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$interfaceAdded$1$EthernetTracker$InterfaceObserver(String iface) {
            EthernetTracker.this.maybeTrackInterface(iface);
        }

        public void interfaceRemoved(String iface) {
            EthernetTracker.this.mHandler.post(new Runnable(iface) {
                /* class com.android.server.ethernet.$$Lambda$EthernetTracker$InterfaceObserver$N47vO7QrVbS59gsxVAc8Mt2Opco */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    EthernetTracker.InterfaceObserver.this.lambda$interfaceRemoved$2$EthernetTracker$InterfaceObserver(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$interfaceRemoved$2$EthernetTracker$InterfaceObserver(String iface) {
            EthernetTracker.this.removeInterface(iface);
        }
    }

    public static class ListenerInfo {
        boolean canUseRestrictedNetworks = false;

        ListenerInfo(boolean canUseRestrictedNetworks2) {
            this.canUseRestrictedNetworks = canUseRestrictedNetworks2;
        }
    }

    private void parseEthernetConfig(String configString) {
        String[] tokens = configString.split(";", 4);
        String name = tokens[0];
        String transport = null;
        String capabilities = tokens.length > 1 ? tokens[1] : null;
        if (tokens.length > 3) {
            transport = tokens[3];
        }
        this.mNetworkCapabilities.put(name, createNetworkCapabilities(true ^ TextUtils.isEmpty(capabilities), capabilities, transport));
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
        return createNetworkCapabilities(clearDefaultCapabilities, null, null);
    }

    static NetworkCapabilities createNetworkCapabilities(boolean clearDefaultCapabilities, String commaSeparatedCapabilities, String overrideTransport) {
        NetworkCapabilities nc = new NetworkCapabilities();
        if (clearDefaultCapabilities) {
            nc.clearAll();
        }
        int transport = 3;
        if (!TextUtils.isEmpty(overrideTransport)) {
            try {
                int parsedTransport = Integer.valueOf(overrideTransport).intValue();
                if (!(parsedTransport == 4 || parsedTransport == 5)) {
                    if (parsedTransport != 6) {
                        transport = parsedTransport;
                    }
                }
                Log.e(TAG, "Override transport '" + parsedTransport + "' is not supported. Defaulting to TRANSPORT_ETHERNET");
            } catch (NumberFormatException e) {
                Log.e(TAG, "Override transport type '" + overrideTransport + "' could not be parsed. Defaulting to TRANSPORT_ETHERNET");
            }
        }
        try {
            nc.addTransportType(transport);
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, transport + " is not a valid NetworkCapability.TRANSPORT_* value. Defaulting to TRANSPORT_ETHERNET");
            nc.addTransportType(3);
        }
        nc.setLinkUpstreamBandwidthKbps(100000);
        nc.setLinkDownstreamBandwidthKbps(100000);
        if (!TextUtils.isEmpty(commaSeparatedCapabilities)) {
            String[] split = commaSeparatedCapabilities.split(",");
            for (String strNetworkCapability : split) {
                if (!TextUtils.isEmpty(strNetworkCapability)) {
                    try {
                        nc.addCapability(Integer.valueOf(strNetworkCapability).intValue());
                    } catch (NumberFormatException e3) {
                        Log.e(TAG, "Capability '" + strNetworkCapability + "' could not be parsed");
                    } catch (IllegalArgumentException e4) {
                        Log.e(TAG, strNetworkCapability + " is not a valid NetworkCapability.NET_CAPABILITY_* value");
                    }
                }
            }
        }
        return nc;
    }

    static IpConfiguration parseStaticIpConfiguration(String staticIpConfig) {
        StaticIpConfiguration ipConfig = new StaticIpConfiguration();
        String[] split = staticIpConfig.trim().split(" ");
        int length = split.length;
        char c = 0;
        int i = 0;
        while (i < length) {
            String keyValueAsString = split[i];
            if (!TextUtils.isEmpty(keyValueAsString)) {
                String[] pair = keyValueAsString.split("=");
                if (pair.length == 2) {
                    String key = pair[c];
                    String value = pair[1];
                    char c2 = 65535;
                    int hashCode = key.hashCode();
                    if (hashCode != -189118908) {
                        if (hashCode != 3367) {
                            if (hashCode != 99625) {
                                if (hashCode == 1837548591 && key.equals("domains")) {
                                    c2 = 1;
                                }
                            } else if (key.equals("dns")) {
                                c2 = 3;
                            }
                        } else if (key.equals("ip")) {
                            c2 = 0;
                        }
                    } else if (key.equals("gateway")) {
                        c2 = 2;
                    }
                    if (c2 == 0) {
                        ipConfig.ipAddress = new LinkAddress(value);
                    } else if (c2 == 1) {
                        ipConfig.domains = value;
                    } else if (c2 == 2) {
                        ipConfig.gateway = InetAddress.parseNumericAddress(value);
                    } else if (c2 == 3) {
                        ArrayList<InetAddress> dnsAddresses = new ArrayList<>();
                        for (String address : value.split(",")) {
                            dnsAddresses.add(InetAddress.parseNumericAddress(address));
                        }
                        ipConfig.dnsServers.addAll(dnsAddresses);
                    } else {
                        throw new IllegalArgumentException("Unexpected key: " + key + " in " + staticIpConfig);
                    }
                } else {
                    throw new IllegalArgumentException("Unexpected token: " + keyValueAsString + " in " + staticIpConfig);
                }
            }
            i++;
            c = 0;
        }
        return new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, ipConfig, (ProxyInfo) null);
    }

    private static IpConfiguration createDefaultIpConfiguration() {
        return new IpConfiguration(IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, (StaticIpConfiguration) null, (ProxyInfo) null);
    }

    private void postAndWaitForRunnable(Runnable r) {
        this.mHandler.runWithScissors(r, 2000);
    }

    public void dump(FileDescriptor fd, IndentingPrintWriter pw, String[] args) {
        postAndWaitForRunnable(new Runnable(pw, fd, args) {
            /* class com.android.server.ethernet.$$Lambda$EthernetTracker$rMvwcG7iXM6tWTHAeEh6PlZCT8 */
            private final /* synthetic */ IndentingPrintWriter f$1;
            private final /* synthetic */ FileDescriptor f$2;
            private final /* synthetic */ String[] f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                EthernetTracker.this.lambda$dump$1$EthernetTracker(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$dump$1$EthernetTracker(IndentingPrintWriter pw, FileDescriptor fd, String[] args) {
        pw.println(getClass().getSimpleName());
        pw.println("Ethernet interface name filter: " + this.mIfaceMatch);
        pw.println("Listeners: " + this.mListeners.getRegisteredCallbackCount());
        pw.println("IP Configurations:");
        pw.increaseIndent();
        for (String iface : this.mIpConfigurations.keySet()) {
            pw.println(iface + ": " + this.mIpConfigurations.get(iface));
        }
        pw.decreaseIndent();
        pw.println();
        pw.println("Network Capabilities:");
        pw.increaseIndent();
        for (String iface2 : this.mNetworkCapabilities.keySet()) {
            pw.println(iface2 + ": " + this.mNetworkCapabilities.get(iface2));
        }
        pw.decreaseIndent();
        pw.println();
        this.mFactory.dump(fd, pw, args);
    }
}
