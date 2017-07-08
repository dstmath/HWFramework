package com.android.server.net;

import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.net.DelayedDiskWrite.Writer;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

public class IpConfigStore {
    private static final /* synthetic */ int[] -android-net-IpConfiguration$IpAssignmentSwitchesValues = null;
    private static final /* synthetic */ int[] -android-net-IpConfiguration$ProxySettingsSwitchesValues = null;
    private static final boolean DBG = false;
    protected static final String DNS_KEY = "dns";
    protected static final String EOS = "eos";
    protected static final String EXCLUSION_LIST_KEY = "exclusionList";
    protected static final String GATEWAY_KEY = "gateway";
    protected static final String ID_KEY = "id";
    protected static final int IPCONFIG_FILE_VERSION = 2;
    protected static final String IP_ASSIGNMENT_KEY = "ipAssignment";
    protected static final String LINK_ADDRESS_KEY = "linkAddress";
    protected static final String PROXY_HOST_KEY = "proxyHost";
    protected static final String PROXY_PAC_FILE = "proxyPac";
    protected static final String PROXY_PORT_KEY = "proxyPort";
    protected static final String PROXY_SETTINGS_KEY = "proxySettings";
    private static final String TAG = "IpConfigStore";
    protected final DelayedDiskWrite mWriter;

    /* renamed from: com.android.server.net.IpConfigStore.1 */
    class AnonymousClass1 implements Writer {
        final /* synthetic */ SparseArray val$networks;

        AnonymousClass1(SparseArray val$networks) {
            this.val$networks = val$networks;
        }

        public void onWriteCalled(DataOutputStream out) throws IOException {
            out.writeInt(IpConfigStore.IPCONFIG_FILE_VERSION);
            for (int i = 0; i < this.val$networks.size(); i++) {
                IpConfigStore.this.writeConfig(out, this.val$networks.keyAt(i), (IpConfiguration) this.val$networks.valueAt(i));
            }
        }
    }

    private static /* synthetic */ int[] -getandroid-net-IpConfiguration$IpAssignmentSwitchesValues() {
        if (-android-net-IpConfiguration$IpAssignmentSwitchesValues != null) {
            return -android-net-IpConfiguration$IpAssignmentSwitchesValues;
        }
        int[] iArr = new int[IpAssignment.values().length];
        try {
            iArr[IpAssignment.DHCP.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[IpAssignment.STATIC.ordinal()] = IPCONFIG_FILE_VERSION;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[IpAssignment.UNASSIGNED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -android-net-IpConfiguration$IpAssignmentSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-net-IpConfiguration$ProxySettingsSwitchesValues() {
        if (-android-net-IpConfiguration$ProxySettingsSwitchesValues != null) {
            return -android-net-IpConfiguration$ProxySettingsSwitchesValues;
        }
        int[] iArr = new int[ProxySettings.values().length];
        try {
            iArr[ProxySettings.NONE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ProxySettings.PAC.ordinal()] = IPCONFIG_FILE_VERSION;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ProxySettings.STATIC.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ProxySettings.UNASSIGNED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-net-IpConfiguration$ProxySettingsSwitchesValues = iArr;
        return iArr;
    }

    public IpConfigStore(DelayedDiskWrite writer) {
        this.mWriter = writer;
    }

    public IpConfigStore() {
        this(new DelayedDiskWrite());
    }

    private boolean writeConfig(DataOutputStream out, int configKey, IpConfiguration config) throws IOException {
        boolean written = DBG;
        try {
            switch (-getandroid-net-IpConfiguration$IpAssignmentSwitchesValues()[config.ipAssignment.ordinal()]) {
                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                    out.writeUTF(IP_ASSIGNMENT_KEY);
                    out.writeUTF(config.ipAssignment.toString());
                    written = true;
                    break;
                case IPCONFIG_FILE_VERSION /*2*/:
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
                        for (InetAddress inetAddr : staticIpConfiguration.dnsServers) {
                            out.writeUTF(DNS_KEY);
                            out.writeUTF(inetAddr.getHostAddress());
                        }
                    }
                    written = true;
                    break;
                case H.REPORT_LOSING_FOCUS /*3*/:
                    break;
                default:
                    loge("Ignore invalid ip assignment while writing");
                    break;
            }
            switch (-getandroid-net-IpConfiguration$ProxySettingsSwitchesValues()[config.proxySettings.ordinal()]) {
                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                    out.writeUTF(PROXY_SETTINGS_KEY);
                    out.writeUTF(config.proxySettings.toString());
                    written = true;
                    break;
                case IPCONFIG_FILE_VERSION /*2*/:
                    ProxyInfo proxyPacProperties = config.httpProxy;
                    out.writeUTF(PROXY_SETTINGS_KEY);
                    out.writeUTF(config.proxySettings.toString());
                    out.writeUTF(PROXY_PAC_FILE);
                    out.writeUTF(proxyPacProperties.getPacFileUrl().toString());
                    written = true;
                    break;
                case H.REPORT_LOSING_FOCUS /*3*/:
                    ProxyInfo proxyProperties = config.httpProxy;
                    String exclusionList = proxyProperties.getExclusionListAsString();
                    out.writeUTF(PROXY_SETTINGS_KEY);
                    out.writeUTF(config.proxySettings.toString());
                    out.writeUTF(PROXY_HOST_KEY);
                    out.writeUTF(proxyProperties.getHost());
                    out.writeUTF(PROXY_PORT_KEY);
                    out.writeInt(proxyProperties.getPort());
                    if (exclusionList != null) {
                        out.writeUTF(EXCLUSION_LIST_KEY);
                        out.writeUTF(exclusionList);
                    }
                    written = true;
                    break;
                case H.DO_TRAVERSAL /*4*/:
                    break;
                default:
                    loge("Ignore invalid proxy settings while writing");
                    break;
            }
            if (written) {
                out.writeUTF(ID_KEY);
                out.writeInt(configKey);
            }
        } catch (NullPointerException e) {
            loge("Failure in writing " + config + e);
        }
        out.writeUTF(EOS);
        return written;
    }

    public void writeIpAndProxyConfigurations(String filePath, SparseArray<IpConfiguration> networks) {
        this.mWriter.write(filePath, new AnonymousClass1(networks));
    }

    public SparseArray<IpConfiguration> readIpAndProxyConfigurations(String filePath) {
        IOException e;
        Throwable th;
        SparseArray<IpConfiguration> networks = new SparseArray();
        DataInputStream dataInputStream = null;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
            try {
                int version = in.readInt();
                if (version == IPCONFIG_FILE_VERSION || version == 1) {
                    while (true) {
                        int id = -1;
                        IpAssignment ipAssignment = IpAssignment.DHCP;
                        ProxySettings proxySettings = ProxySettings.NONE;
                        StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
                        String proxyHost = null;
                        String pacFileUrl = null;
                        int proxyPort = -1;
                        String str = null;
                        while (true) {
                            String key = in.readUTF();
                            try {
                                if (key.equals(ID_KEY)) {
                                    id = in.readInt();
                                } else {
                                    if (key.equals(IP_ASSIGNMENT_KEY)) {
                                        ipAssignment = IpAssignment.valueOf(in.readUTF());
                                    } else {
                                        if (key.equals(LINK_ADDRESS_KEY)) {
                                            LinkAddress linkAddress = new LinkAddress(NetworkUtils.numericToInetAddress(in.readUTF()), in.readInt());
                                            if ((linkAddress.getAddress() instanceof Inet4Address) && staticIpConfiguration.ipAddress == null) {
                                                staticIpConfiguration.ipAddress = linkAddress;
                                            } else {
                                                loge("Non-IPv4 or duplicate address: " + linkAddress);
                                            }
                                        } else {
                                            if (key.equals(GATEWAY_KEY)) {
                                                LinkAddress linkAddress2 = null;
                                                InetAddress inetAddress = null;
                                                if (version == 1) {
                                                    inetAddress = NetworkUtils.numericToInetAddress(in.readUTF());
                                                    if (staticIpConfiguration.gateway == null) {
                                                        staticIpConfiguration.gateway = inetAddress;
                                                    } else {
                                                        loge("Duplicate gateway: " + inetAddress.getHostAddress());
                                                    }
                                                } else {
                                                    if (in.readInt() == 1) {
                                                        linkAddress2 = new LinkAddress(NetworkUtils.numericToInetAddress(in.readUTF()), in.readInt());
                                                    }
                                                    if (in.readInt() == 1) {
                                                        inetAddress = NetworkUtils.numericToInetAddress(in.readUTF());
                                                    }
                                                    RouteInfo routeInfo = new RouteInfo(linkAddress2, inetAddress);
                                                    if (routeInfo.isIPv4Default() && staticIpConfiguration.gateway == null) {
                                                        staticIpConfiguration.gateway = inetAddress;
                                                    } else {
                                                        loge("Non-IPv4 default or duplicate route: " + routeInfo);
                                                    }
                                                }
                                            } else {
                                                if (key.equals(DNS_KEY)) {
                                                    staticIpConfiguration.dnsServers.add(NetworkUtils.numericToInetAddress(in.readUTF()));
                                                } else {
                                                    if (key.equals(PROXY_SETTINGS_KEY)) {
                                                        proxySettings = ProxySettings.valueOf(in.readUTF());
                                                    } else {
                                                        if (key.equals(PROXY_HOST_KEY)) {
                                                            proxyHost = in.readUTF();
                                                        } else {
                                                            if (key.equals(PROXY_PORT_KEY)) {
                                                                proxyPort = in.readInt();
                                                            } else {
                                                                if (key.equals(PROXY_PAC_FILE)) {
                                                                    pacFileUrl = in.readUTF();
                                                                } else {
                                                                    if (key.equals(EXCLUSION_LIST_KEY)) {
                                                                        str = in.readUTF();
                                                                    } else {
                                                                        if (!key.equals(EOS)) {
                                                                            loge("Ignore unknown key " + key + "while reading");
                                                                        } else if (id != -1) {
                                                                            IpConfiguration config = new IpConfiguration();
                                                                            networks.put(id, config);
                                                                            switch (-getandroid-net-IpConfiguration$IpAssignmentSwitchesValues()[ipAssignment.ordinal()]) {
                                                                                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                                                                                    config.ipAssignment = ipAssignment;
                                                                                    break;
                                                                                case IPCONFIG_FILE_VERSION /*2*/:
                                                                                    config.staticIpConfiguration = staticIpConfiguration;
                                                                                    config.ipAssignment = ipAssignment;
                                                                                    break;
                                                                                case H.REPORT_LOSING_FOCUS /*3*/:
                                                                                    loge("BUG: Found UNASSIGNED IP on file, use DHCP");
                                                                                    config.ipAssignment = IpAssignment.DHCP;
                                                                                    break;
                                                                                default:
                                                                                    loge("Ignore invalid ip assignment while reading.");
                                                                                    config.ipAssignment = IpAssignment.UNASSIGNED;
                                                                                    break;
                                                                            }
                                                                            ProxyInfo proxyInfo;
                                                                            switch (-getandroid-net-IpConfiguration$ProxySettingsSwitchesValues()[proxySettings.ordinal()]) {
                                                                                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                                                                                    config.proxySettings = proxySettings;
                                                                                    break;
                                                                                case IPCONFIG_FILE_VERSION /*2*/:
                                                                                    proxyInfo = new ProxyInfo(pacFileUrl);
                                                                                    config.proxySettings = proxySettings;
                                                                                    config.httpProxy = proxyInfo;
                                                                                    break;
                                                                                case H.REPORT_LOSING_FOCUS /*3*/:
                                                                                    proxyInfo = new ProxyInfo(proxyHost, proxyPort, str);
                                                                                    config.proxySettings = proxySettings;
                                                                                    config.httpProxy = proxyInfo;
                                                                                    break;
                                                                                case H.DO_TRAVERSAL /*4*/:
                                                                                    loge("BUG: Found UNASSIGNED proxy on file, use NONE");
                                                                                    config.proxySettings = ProxySettings.NONE;
                                                                                    break;
                                                                                default:
                                                                                    loge("Ignore invalid proxy settings while reading");
                                                                                    config.proxySettings = ProxySettings.UNASSIGNED;
                                                                                    break;
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (IllegalArgumentException e2) {
                                loge("Ignore invalid address while reading" + e2);
                            }
                        }
                    }
                } else {
                    loge("Bad version on IP configuration file, ignore read");
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e3) {
                        }
                    }
                    return null;
                }
            } catch (EOFException e4) {
                dataInputStream = in;
            } catch (IOException e5) {
                e = e5;
                dataInputStream = in;
            } catch (Throwable th2) {
                th = th2;
                dataInputStream = in;
            }
        } catch (EOFException e6) {
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (Exception e7) {
                }
            }
            return networks;
        } catch (IOException e8) {
            e = e8;
            try {
                loge("Error parsing configuration: " + e);
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (Exception e9) {
                    }
                }
                return networks;
            } catch (Throwable th3) {
                th = th3;
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (Exception e10) {
                    }
                }
                throw th;
            }
        }
    }

    protected void loge(String s) {
        Log.e(TAG, s);
    }

    protected void log(String s) {
        Log.d(TAG, s);
    }
}
