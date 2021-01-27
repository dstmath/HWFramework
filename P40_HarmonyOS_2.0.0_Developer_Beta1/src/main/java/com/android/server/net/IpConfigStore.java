package com.android.server.net;

import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.BatteryService;
import com.android.server.net.DelayedDiskWrite;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Iterator;

public class IpConfigStore {
    private static final boolean DBG = false;
    protected static final String DNS_KEY = "dns";
    protected static final String EOS = "eos";
    protected static final String EXCLUSION_LIST_KEY = "exclusionList";
    protected static final String GATEWAY_KEY = "gateway";
    protected static final String ID_KEY = "id";
    protected static final int IPCONFIG_FILE_VERSION = 3;
    protected static final String IP_ASSIGNMENT_KEY = "ipAssignment";
    protected static final String LINK_ADDRESS_KEY = "linkAddress";
    protected static final String PROXY_HOST_KEY = "proxyHost";
    protected static final String PROXY_PAC_FILE = "proxyPac";
    protected static final String PROXY_PORT_KEY = "proxyPort";
    protected static final String PROXY_SETTINGS_KEY = "proxySettings";
    private static final String TAG = "IpConfigStore";
    private static boolean isTv = "tv".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR));
    protected final DelayedDiskWrite mWriter;

    public IpConfigStore(DelayedDiskWrite writer) {
        this.mWriter = writer;
    }

    public IpConfigStore() {
        this(new DelayedDiskWrite());
    }

    private static boolean writeConfig(DataOutputStream out, String configKey, IpConfiguration config) throws IOException {
        return writeConfig(out, configKey, config, 3);
    }

    /* JADX INFO: Multiple debug info for r1v11 android.net.ProxyInfo: [D('proxyProperties' android.net.ProxyInfo), D('proxyPacProperties' android.net.ProxyInfo)] */
    @VisibleForTesting
    public static boolean writeConfig(DataOutputStream out, String configKey, IpConfiguration config, int version) throws IOException {
        boolean written = false;
        try {
            int i = AnonymousClass1.$SwitchMap$android$net$IpConfiguration$IpAssignment[config.ipAssignment.ordinal()];
            if (i == 1) {
                out.writeUTF(IP_ASSIGNMENT_KEY);
                out.writeUTF(config.ipAssignment.toString());
                StaticIpConfiguration staticIpConfiguration = config.staticIpConfiguration;
                if (staticIpConfiguration != null) {
                    if (staticIpConfiguration.ipAddress != null) {
                        LinkAddress ipAddress = staticIpConfiguration.ipAddress;
                        out.writeUTF(LINK_ADDRESS_KEY);
                        out.writeUTF(ipAddress.getAddress().getHostAddress());
                        out.writeInt(ipAddress.getPrefixLength());
                    }
                    if (staticIpConfiguration.gateway != null) {
                        out.writeUTF(GATEWAY_KEY);
                        out.writeInt(0);
                        out.writeInt(1);
                        out.writeUTF(staticIpConfiguration.gateway.getHostAddress());
                    }
                    Iterator it = staticIpConfiguration.dnsServers.iterator();
                    while (it.hasNext()) {
                        out.writeUTF(DNS_KEY);
                        out.writeUTF(((InetAddress) it.next()).getHostAddress());
                    }
                }
                written = true;
            } else if (i == 2) {
                out.writeUTF(IP_ASSIGNMENT_KEY);
                out.writeUTF(config.ipAssignment.toString());
                written = true;
            } else if (i != 3) {
                loge("Ignore invalid ip assignment while writing");
            }
            int i2 = AnonymousClass1.$SwitchMap$android$net$IpConfiguration$ProxySettings[config.proxySettings.ordinal()];
            if (i2 == 1) {
                ProxyInfo proxyPacProperties = config.httpProxy;
                String exclusionList = proxyPacProperties.getExclusionListAsString();
                out.writeUTF(PROXY_SETTINGS_KEY);
                out.writeUTF(config.proxySettings.toString());
                out.writeUTF(PROXY_HOST_KEY);
                out.writeUTF(proxyPacProperties.getHost());
                out.writeUTF(PROXY_PORT_KEY);
                out.writeInt(proxyPacProperties.getPort());
                if (exclusionList != null) {
                    out.writeUTF(EXCLUSION_LIST_KEY);
                    out.writeUTF(exclusionList);
                }
                written = true;
            } else if (i2 == 2) {
                ProxyInfo proxyPacProperties2 = config.httpProxy;
                out.writeUTF(PROXY_SETTINGS_KEY);
                out.writeUTF(config.proxySettings.toString());
                out.writeUTF(PROXY_PAC_FILE);
                out.writeUTF(proxyPacProperties2.getPacFileUrl().toString());
                written = true;
            } else if (i2 == 3) {
                out.writeUTF(PROXY_SETTINGS_KEY);
                out.writeUTF(config.proxySettings.toString());
                written = true;
            } else if (i2 != 4) {
                loge("Ignore invalid proxy settings while writing");
            }
            if (written) {
                out.writeUTF(ID_KEY);
                if (version < 3) {
                    out.writeInt(Integer.valueOf(configKey).intValue());
                } else {
                    out.writeUTF(configKey);
                }
            }
        } catch (NullPointerException e) {
            loge("Failure in writing " + config + e);
        }
        out.writeUTF(EOS);
        if (isTv) {
            out.flush();
            Runtime.getRuntime().exec("sync");
        }
        return written;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.net.IpConfigStore$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$net$IpConfiguration$IpAssignment = new int[IpConfiguration.IpAssignment.values().length];
        static final /* synthetic */ int[] $SwitchMap$android$net$IpConfiguration$ProxySettings = new int[IpConfiguration.ProxySettings.values().length];

        static {
            try {
                $SwitchMap$android$net$IpConfiguration$ProxySettings[IpConfiguration.ProxySettings.STATIC.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$ProxySettings[IpConfiguration.ProxySettings.PAC.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$ProxySettings[IpConfiguration.ProxySettings.NONE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$ProxySettings[IpConfiguration.ProxySettings.UNASSIGNED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$IpAssignment[IpConfiguration.IpAssignment.STATIC.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$IpAssignment[IpConfiguration.IpAssignment.DHCP.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$net$IpConfiguration$IpAssignment[IpConfiguration.IpAssignment.UNASSIGNED.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    @Deprecated
    public void writeIpAndProxyConfigurationsToFile(String filePath, SparseArray<IpConfiguration> networks) {
        this.mWriter.write(filePath, new DelayedDiskWrite.Writer(networks) {
            /* class com.android.server.net.$$Lambda$IpConfigStore$O2tmBZ0pfEt3xGZYo5ZrQq4edzM */
            private final /* synthetic */ SparseArray f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.server.net.DelayedDiskWrite.Writer
            public final void onWriteCalled(DataOutputStream dataOutputStream) {
                IpConfigStore.lambda$writeIpAndProxyConfigurationsToFile$0(this.f$0, dataOutputStream);
            }
        });
    }

    static /* synthetic */ void lambda$writeIpAndProxyConfigurationsToFile$0(SparseArray networks, DataOutputStream out) throws IOException {
        out.writeInt(3);
        for (int i = 0; i < networks.size(); i++) {
            writeConfig(out, String.valueOf(networks.keyAt(i)), (IpConfiguration) networks.valueAt(i));
        }
    }

    public void writeIpConfigurations(String filePath, ArrayMap<String, IpConfiguration> networks) {
        this.mWriter.write(filePath, new DelayedDiskWrite.Writer(networks) {
            /* class com.android.server.net.$$Lambda$IpConfigStore$rFY3yG3j6RGRgrQey7yYfi0Yze0 */
            private final /* synthetic */ ArrayMap f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.server.net.DelayedDiskWrite.Writer
            public final void onWriteCalled(DataOutputStream dataOutputStream) {
                IpConfigStore.lambda$writeIpConfigurations$1(this.f$0, dataOutputStream);
            }
        });
    }

    static /* synthetic */ void lambda$writeIpConfigurations$1(ArrayMap networks, DataOutputStream out) throws IOException {
        out.writeInt(3);
        for (int i = 0; i < networks.size(); i++) {
            writeConfig(out, (String) networks.keyAt(i), (IpConfiguration) networks.valueAt(i));
        }
    }

    public static ArrayMap<String, IpConfiguration> readIpConfigurations(String filePath) {
        try {
            return readIpConfigurations(new BufferedInputStream(new FileInputStream(filePath)));
        } catch (FileNotFoundException e) {
            loge("Error opening configuration file: " + e);
            return new ArrayMap<>(0);
        }
    }

    @Deprecated
    public static SparseArray<IpConfiguration> readIpAndProxyConfigurations(String filePath) {
        try {
            return readIpAndProxyConfigurations(new BufferedInputStream(new FileInputStream(filePath)));
        } catch (FileNotFoundException e) {
            loge("Error opening configuration file: " + e);
            return new SparseArray<>();
        }
    }

    @Deprecated
    public static SparseArray<IpConfiguration> readIpAndProxyConfigurations(InputStream inputStream) {
        ArrayMap<String, IpConfiguration> networks = readIpConfigurations(inputStream);
        if (networks == null) {
            return null;
        }
        SparseArray<IpConfiguration> networksById = new SparseArray<>();
        for (int i = 0; i < networks.size(); i++) {
            networksById.put(Integer.valueOf(networks.keyAt(i)).intValue(), networks.valueAt(i));
        }
        return networksById;
    }

    /* JADX WARNING: Removed duplicated region for block: B:132:0x02d5 A[SYNTHETIC, Splitter:B:132:0x02d5] */
    public static ArrayMap<String, IpConfiguration> readIpConfigurations(InputStream inputStream) {
        DataInputStream in;
        Throwable th;
        String key;
        char c;
        IllegalArgumentException e;
        IllegalArgumentException e2;
        char c2;
        IllegalArgumentException e3;
        ArrayMap<String, IpConfiguration> networks = new ArrayMap<>();
        DataInputStream in2 = null;
        try {
            DataInputStream in3 = new DataInputStream(inputStream);
            int version = in3.readInt();
            String exclusionList = null;
            int i = 3;
            if (version == 3 || version == 2 || version == 1) {
                while (true) {
                    String uniqueToken = null;
                    IpConfiguration.IpAssignment ipAssignment = IpConfiguration.IpAssignment.DHCP;
                    IpConfiguration.ProxySettings proxySettings = IpConfiguration.ProxySettings.NONE;
                    StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
                    String proxyHost = null;
                    String pacFileUrl = null;
                    int proxyPort = -1;
                    while (true) {
                        key = in3.readUTF();
                        try {
                            if (key.equals(ID_KEY)) {
                                if (version < i) {
                                    key = version;
                                    uniqueToken = String.valueOf(in3.readInt());
                                    c = 2;
                                } else {
                                    key = version;
                                    uniqueToken = in3.readUTF();
                                    c = 2;
                                }
                            } else if (key.equals(IP_ASSIGNMENT_KEY)) {
                                key = version;
                                ipAssignment = IpConfiguration.IpAssignment.valueOf(in3.readUTF());
                                c = 2;
                            } else if (key.equals(LINK_ADDRESS_KEY)) {
                                LinkAddress linkAddr = new LinkAddress(NetworkUtils.numericToInetAddress(in3.readUTF()), in3.readInt());
                                if (!(linkAddr.getAddress() instanceof Inet4Address) || staticIpConfiguration.ipAddress != null) {
                                    loge("Non-IPv4 or duplicate address: " + linkAddr);
                                } else {
                                    staticIpConfiguration.ipAddress = linkAddr;
                                }
                                key = version;
                                c = 2;
                            } else if (key.equals(GATEWAY_KEY)) {
                                LinkAddress dest = null;
                                InetAddress gateway = null;
                                if (version == 1) {
                                    try {
                                        InetAddress gateway2 = NetworkUtils.numericToInetAddress(in3.readUTF());
                                        if (staticIpConfiguration.gateway == null) {
                                            staticIpConfiguration.gateway = gateway2;
                                            key = version;
                                        } else {
                                            StringBuilder sb = new StringBuilder();
                                            key = version;
                                            try {
                                                sb.append("Duplicate gateway: ");
                                                sb.append(gateway2.getHostAddress());
                                                loge(sb.toString());
                                            } catch (IllegalArgumentException e4) {
                                                e3 = e4;
                                                e = e3;
                                                c = 2;
                                                loge("Ignore invalid address while reading" + e);
                                                version = key;
                                                i = 3;
                                            }
                                        }
                                    } catch (IllegalArgumentException e5) {
                                        e3 = e5;
                                        key = version;
                                        e = e3;
                                        c = 2;
                                        loge("Ignore invalid address while reading" + e);
                                        version = key;
                                        i = 3;
                                    }
                                } else {
                                    key = version;
                                    if (in3.readInt() == 1) {
                                        dest = new LinkAddress(NetworkUtils.numericToInetAddress(in3.readUTF()), in3.readInt());
                                    }
                                    if (in3.readInt() == 1) {
                                        gateway = NetworkUtils.numericToInetAddress(in3.readUTF());
                                    }
                                    RouteInfo route = new RouteInfo(dest, gateway);
                                    if (!route.isIPv4Default() || staticIpConfiguration.gateway != null) {
                                        loge("Non-IPv4 default or duplicate route: " + route);
                                    } else {
                                        staticIpConfiguration.gateway = gateway;
                                    }
                                }
                                c = 2;
                            } else {
                                key = version;
                                try {
                                    if (key.equals(DNS_KEY)) {
                                        staticIpConfiguration.dnsServers.add(NetworkUtils.numericToInetAddress(in3.readUTF()));
                                        c = 2;
                                    } else if (key.equals(PROXY_SETTINGS_KEY)) {
                                        proxySettings = IpConfiguration.ProxySettings.valueOf(in3.readUTF());
                                        c = 2;
                                    } else if (key.equals(PROXY_HOST_KEY)) {
                                        proxyHost = in3.readUTF();
                                        c = 2;
                                    } else if (key.equals(PROXY_PORT_KEY)) {
                                        proxyPort = in3.readInt();
                                        c = 2;
                                    } else if (key.equals(PROXY_PAC_FILE)) {
                                        pacFileUrl = in3.readUTF();
                                        c = 2;
                                    } else if (key.equals(EXCLUSION_LIST_KEY)) {
                                        exclusionList = in3.readUTF();
                                        c = 2;
                                    } else if (key.equals(EOS)) {
                                        break;
                                    } else {
                                        c = 2;
                                        try {
                                            loge("Ignore unknown key " + key + "while reading");
                                        } catch (IllegalArgumentException e6) {
                                            e2 = e6;
                                            e = e2;
                                            loge("Ignore invalid address while reading" + e);
                                            version = key;
                                            i = 3;
                                        }
                                    }
                                } catch (IllegalArgumentException e7) {
                                    e2 = e7;
                                    c = 2;
                                    e = e2;
                                    loge("Ignore invalid address while reading" + e);
                                    version = key;
                                    i = 3;
                                }
                            }
                            version = key;
                            i = 3;
                        } catch (IllegalArgumentException e8) {
                            e2 = e8;
                            key = version;
                            c = 2;
                            e = e2;
                            loge("Ignore invalid address while reading" + e);
                            version = key;
                            i = 3;
                        }
                    }
                    if (uniqueToken != null) {
                        IpConfiguration config = new IpConfiguration();
                        networks.put(uniqueToken, config);
                        int i2 = AnonymousClass1.$SwitchMap$android$net$IpConfiguration$IpAssignment[ipAssignment.ordinal()];
                        if (i2 == 1) {
                            config.staticIpConfiguration = staticIpConfiguration;
                            config.ipAssignment = ipAssignment;
                        } else if (i2 == 2) {
                            config.ipAssignment = ipAssignment;
                        } else if (i2 != 3) {
                            loge("Ignore invalid ip assignment while reading.");
                            config.ipAssignment = IpConfiguration.IpAssignment.UNASSIGNED;
                        } else {
                            loge("BUG: Found UNASSIGNED IP on file, use DHCP");
                            config.ipAssignment = IpConfiguration.IpAssignment.DHCP;
                        }
                        int i3 = AnonymousClass1.$SwitchMap$android$net$IpConfiguration$ProxySettings[proxySettings.ordinal()];
                        if (i3 != 1) {
                            c2 = 2;
                            if (i3 == 2) {
                                ProxyInfo proxyPacProperties = new ProxyInfo(pacFileUrl);
                                config.proxySettings = proxySettings;
                                config.httpProxy = proxyPacProperties;
                            } else if (i3 == 3) {
                                config.proxySettings = proxySettings;
                            } else if (i3 != 4) {
                                loge("Ignore invalid proxy settings while reading");
                                config.proxySettings = IpConfiguration.ProxySettings.UNASSIGNED;
                            } else {
                                loge("BUG: Found UNASSIGNED proxy on file, use NONE");
                                config.proxySettings = IpConfiguration.ProxySettings.NONE;
                            }
                        } else {
                            c2 = 2;
                            ProxyInfo proxyInfo = new ProxyInfo(proxyHost, proxyPort, exclusionList);
                            config.proxySettings = proxySettings;
                            config.httpProxy = proxyInfo;
                        }
                    } else {
                        c2 = 2;
                    }
                    exclusionList = null;
                    version = key;
                    i = 3;
                }
            } else {
                loge("Bad version on IP configuration file, ignore read");
                try {
                    in3.close();
                } catch (Exception e9) {
                }
                return null;
            }
        } catch (EOFException e10) {
            if (0 != 0) {
                in2.close();
            }
        } catch (IOException e11) {
            in = null;
            loge("Error parsing configuration: " + e11);
            if (0 != 0) {
                try {
                    in.close();
                } catch (Exception e12) {
                }
            }
        } catch (Throwable th2) {
            th = th2;
            if (in != null) {
            }
            throw th;
        }
        return networks;
    }

    protected static void loge(String s) {
        Log.e(TAG, s);
    }

    protected static void log(String s) {
        Log.d(TAG, s);
    }
}
