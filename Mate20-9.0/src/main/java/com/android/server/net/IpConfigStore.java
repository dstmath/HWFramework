package com.android.server.net;

import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
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
    protected final DelayedDiskWrite mWriter;

    /* renamed from: com.android.server.net.IpConfigStore$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
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

    public IpConfigStore(DelayedDiskWrite writer) {
        this.mWriter = writer;
    }

    public IpConfigStore() {
        this(new DelayedDiskWrite());
    }

    private static boolean writeConfig(DataOutputStream out, String configKey, IpConfiguration config) throws IOException {
        return writeConfig(out, configKey, config, 3);
    }

    @VisibleForTesting
    public static boolean writeConfig(DataOutputStream out, String configKey, IpConfiguration config, int version) throws IOException {
        boolean written = false;
        try {
            switch (AnonymousClass1.$SwitchMap$android$net$IpConfiguration$IpAssignment[config.ipAssignment.ordinal()]) {
                case 1:
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
                    break;
                case 2:
                    out.writeUTF(IP_ASSIGNMENT_KEY);
                    out.writeUTF(config.ipAssignment.toString());
                    written = true;
                    break;
                case 3:
                    break;
                default:
                    loge("Ignore invalid ip assignment while writing");
                    break;
            }
            switch (AnonymousClass1.$SwitchMap$android$net$IpConfiguration$ProxySettings[config.proxySettings.ordinal()]) {
                case 1:
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
                    break;
                case 2:
                    ProxyInfo proxyPacProperties2 = config.httpProxy;
                    out.writeUTF(PROXY_SETTINGS_KEY);
                    out.writeUTF(config.proxySettings.toString());
                    out.writeUTF(PROXY_PAC_FILE);
                    out.writeUTF(proxyPacProperties2.getPacFileUrl().toString());
                    written = true;
                    break;
                case 3:
                    out.writeUTF(PROXY_SETTINGS_KEY);
                    out.writeUTF(config.proxySettings.toString());
                    written = true;
                    break;
                case 4:
                    break;
                default:
                    loge("Ignore invalid proxy settings while writing");
                    break;
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
        return written;
    }

    @Deprecated
    public void writeIpAndProxyConfigurationsToFile(String filePath, SparseArray<IpConfiguration> networks) {
        this.mWriter.write(filePath, new DelayedDiskWrite.Writer(networks) {
            private final /* synthetic */ SparseArray f$0;

            {
                this.f$0 = r1;
            }

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
            private final /* synthetic */ ArrayMap f$0;

            {
                this.f$0 = r1;
            }

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
            return readIpConfigurations((InputStream) new BufferedInputStream(new FileInputStream(filePath)));
        } catch (FileNotFoundException e) {
            loge("Error opening configuration file: " + e);
            return new ArrayMap<>(0);
        }
    }

    @Deprecated
    public static SparseArray<IpConfiguration> readIpAndProxyConfigurations(String filePath) {
        try {
            return readIpAndProxyConfigurations((InputStream) new BufferedInputStream(new FileInputStream(filePath)));
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

    /* JADX WARNING: Code restructure failed: missing block: B:109:0x027e, code lost:
        if (r3 == null) goto L_0x0292;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x028f, code lost:
        if (r3 == null) goto L_0x0292;
     */
    public static ArrayMap<String, IpConfiguration> readIpConfigurations(InputStream inputStream) {
        LinkAddress dest;
        String uniqueToken;
        ArrayMap<String, IpConfiguration> networks = new ArrayMap<>();
        String str = null;
        DataInputStream in = null;
        try {
            in = new DataInputStream(inputStream);
            int version = in.readInt();
            int i = 3;
            if (version == 3 || version == 2 || version == 1) {
                while (true) {
                    IpConfiguration.IpAssignment ipAssignment = IpConfiguration.IpAssignment.DHCP;
                    IpConfiguration.ProxySettings proxySettings = IpConfiguration.ProxySettings.NONE;
                    StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
                    int proxyPort = -1;
                    String pacFileUrl = null;
                    String proxyHost = null;
                    IpConfiguration.ProxySettings proxySettings2 = proxySettings;
                    IpConfiguration.IpAssignment ipAssignment2 = ipAssignment;
                    String uniqueToken2 = null;
                    String exclusionList = str;
                    while (true) {
                        String exclusionList2 = exclusionList;
                        String key = in.readUTF();
                        try {
                            if (key.equals(ID_KEY)) {
                                if (version < i) {
                                    uniqueToken = String.valueOf(in.readInt());
                                } else {
                                    uniqueToken = in.readUTF();
                                }
                                uniqueToken2 = uniqueToken;
                            } else if (key.equals(IP_ASSIGNMENT_KEY)) {
                                ipAssignment2 = IpConfiguration.IpAssignment.valueOf(in.readUTF());
                            } else if (key.equals(LINK_ADDRESS_KEY)) {
                                LinkAddress linkAddr = new LinkAddress(NetworkUtils.numericToInetAddress(in.readUTF()), in.readInt());
                                if (!(linkAddr.getAddress() instanceof Inet4Address) || staticIpConfiguration.ipAddress != null) {
                                    loge("Non-IPv4 or duplicate address: " + linkAddr);
                                } else {
                                    staticIpConfiguration.ipAddress = linkAddr;
                                }
                            } else if (key.equals(GATEWAY_KEY)) {
                                InetAddress gateway = null;
                                if (version == 1) {
                                    InetAddress gateway2 = NetworkUtils.numericToInetAddress(in.readUTF());
                                    if (staticIpConfiguration.gateway == null) {
                                        staticIpConfiguration.gateway = gateway2;
                                    } else {
                                        loge("Duplicate gateway: " + gateway2.getHostAddress());
                                    }
                                } else {
                                    if (in.readInt() == 1) {
                                        dest = new LinkAddress(NetworkUtils.numericToInetAddress(in.readUTF()), in.readInt());
                                    } else {
                                        dest = null;
                                    }
                                    if (in.readInt() == 1) {
                                        gateway = NetworkUtils.numericToInetAddress(in.readUTF());
                                    }
                                    RouteInfo route = new RouteInfo(dest, gateway);
                                    if (!route.isIPv4Default() || staticIpConfiguration.gateway != null) {
                                        StringBuilder sb = new StringBuilder();
                                        LinkAddress linkAddress = dest;
                                        sb.append("Non-IPv4 default or duplicate route: ");
                                        sb.append(route);
                                        loge(sb.toString());
                                    } else {
                                        staticIpConfiguration.gateway = gateway;
                                    }
                                }
                            } else if (key.equals(DNS_KEY)) {
                                staticIpConfiguration.dnsServers.add(NetworkUtils.numericToInetAddress(in.readUTF()));
                            } else if (key.equals(PROXY_SETTINGS_KEY)) {
                                proxySettings2 = IpConfiguration.ProxySettings.valueOf(in.readUTF());
                            } else if (key.equals(PROXY_HOST_KEY)) {
                                proxyHost = in.readUTF();
                            } else if (key.equals(PROXY_PORT_KEY)) {
                                proxyPort = in.readInt();
                            } else if (key.equals(PROXY_PAC_FILE)) {
                                pacFileUrl = in.readUTF();
                            } else if (key.equals(EXCLUSION_LIST_KEY)) {
                                exclusionList = in.readUTF();
                                InputStream inputStream2 = inputStream;
                                i = 3;
                            } else if (key.equals(EOS)) {
                                if (uniqueToken2 != null) {
                                    IpConfiguration config = new IpConfiguration();
                                    networks.put(uniqueToken2, config);
                                    switch (AnonymousClass1.$SwitchMap$android$net$IpConfiguration$IpAssignment[ipAssignment2.ordinal()]) {
                                        case 1:
                                            config.staticIpConfiguration = staticIpConfiguration;
                                            config.ipAssignment = ipAssignment2;
                                            break;
                                        case 2:
                                            config.ipAssignment = ipAssignment2;
                                            break;
                                        case 3:
                                            loge("BUG: Found UNASSIGNED IP on file, use DHCP");
                                            config.ipAssignment = IpConfiguration.IpAssignment.DHCP;
                                            break;
                                        default:
                                            loge("Ignore invalid ip assignment while reading.");
                                            config.ipAssignment = IpConfiguration.IpAssignment.UNASSIGNED;
                                            break;
                                    }
                                    switch (AnonymousClass1.$SwitchMap$android$net$IpConfiguration$ProxySettings[proxySettings2.ordinal()]) {
                                        case 1:
                                            ProxyInfo proxyInfo = new ProxyInfo(proxyHost, proxyPort, exclusionList2);
                                            config.proxySettings = proxySettings2;
                                            config.httpProxy = proxyInfo;
                                            break;
                                        case 2:
                                            ProxyInfo proxyPacProperties = new ProxyInfo(pacFileUrl);
                                            config.proxySettings = proxySettings2;
                                            config.httpProxy = proxyPacProperties;
                                            break;
                                        case 3:
                                            config.proxySettings = proxySettings2;
                                            break;
                                        case 4:
                                            loge("BUG: Found UNASSIGNED proxy on file, use NONE");
                                            config.proxySettings = IpConfiguration.ProxySettings.NONE;
                                            break;
                                        default:
                                            loge("Ignore invalid proxy settings while reading");
                                            config.proxySettings = IpConfiguration.ProxySettings.UNASSIGNED;
                                            break;
                                    }
                                }
                                str = null;
                                InputStream inputStream3 = inputStream;
                                i = 3;
                            } else {
                                loge("Ignore unknown key " + key + "while reading");
                            }
                            exclusionList = exclusionList2;
                        } catch (IllegalArgumentException e) {
                            loge("Ignore invalid address while reading" + e);
                            exclusionList = exclusionList2;
                        }
                        InputStream inputStream22 = inputStream;
                        i = 3;
                    }
                }
            } else {
                loge("Bad version on IP configuration file, ignore read");
                try {
                    in.close();
                } catch (Exception e2) {
                }
                return null;
            }
        } catch (EOFException e3) {
        } catch (IOException e4) {
            loge("Error parsing configuration: " + e4);
        } catch (Throwable th) {
            Throwable th2 = th;
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e5) {
                }
            }
            throw th2;
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
