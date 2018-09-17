package android.net;

import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Proxy {
    private static final Pattern EXCLLIST_PATTERN = null;
    private static final String EXCLLIST_REGEXP = "^$|^[a-zA-Z0-9*]+(\\-[a-zA-Z0-9*]+)*(\\.[a-zA-Z0-9*]+(\\-[a-zA-Z0-9*]+)*)*(,[a-zA-Z0-9*]+(\\-[a-zA-Z0-9*]+)*(\\.[a-zA-Z0-9*]+(\\-[a-zA-Z0-9*]+)*)*)*$";
    private static final String EXCL_REGEX = "[a-zA-Z0-9*]+(\\-[a-zA-Z0-9*]+)*(\\.[a-zA-Z0-9*]+(\\-[a-zA-Z0-9*]+)*)*";
    public static final String EXTRA_PROXY_INFO = "android.intent.extra.PROXY_INFO";
    private static final Pattern HOSTNAME_PATTERN = null;
    private static final String HOSTNAME_REGEXP = "^$|^[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*(\\.[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*)*$";
    private static final String NAME_IP_REGEX = "[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*(\\.[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*)*";
    public static final String PROXY_CHANGE_ACTION = "android.intent.action.PROXY_CHANGE";
    public static final int PROXY_EXCLLIST_INVALID = 5;
    public static final int PROXY_HOSTNAME_EMPTY = 1;
    public static final int PROXY_HOSTNAME_INVALID = 2;
    public static final int PROXY_PORT_EMPTY = 3;
    public static final int PROXY_PORT_INVALID = 4;
    public static final int PROXY_VALID = 0;
    private static final String TAG = "Proxy";
    private static ConnectivityManager sConnectivityManager;
    private static final ProxySelector sDefaultProxySelector = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.Proxy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.Proxy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.Proxy.<clinit>():void");
    }

    public static final java.net.Proxy getProxy(Context ctx, String url) {
        String host = ProxyInfo.LOCAL_EXCL_LIST;
        if (!(url == null || isLocalHost(host))) {
            List<java.net.Proxy> proxyList = ProxySelector.getDefault().select(URI.create(url));
            if (proxyList.size() > 0) {
                return (java.net.Proxy) proxyList.get(PROXY_VALID);
            }
        }
        return java.net.Proxy.NO_PROXY;
    }

    public static final String getHost(Context ctx) {
        java.net.Proxy proxy = getProxy(ctx, null);
        if (proxy == java.net.Proxy.NO_PROXY) {
            return null;
        }
        try {
            return ((InetSocketAddress) proxy.address()).getHostName();
        } catch (Exception e) {
            return null;
        }
    }

    public static final int getPort(Context ctx) {
        java.net.Proxy proxy = getProxy(ctx, null);
        if (proxy == java.net.Proxy.NO_PROXY) {
            return -1;
        }
        try {
            return ((InetSocketAddress) proxy.address()).getPort();
        } catch (Exception e) {
            return -1;
        }
    }

    public static final String getDefaultHost() {
        String host = System.getProperty("http.proxyHost");
        if (TextUtils.isEmpty(host)) {
            return null;
        }
        return host;
    }

    public static final int getDefaultPort() {
        if (getDefaultHost() == null) {
            return -1;
        }
        try {
            return Integer.parseInt(System.getProperty("http.proxyPort"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static final boolean isLocalHost(String host) {
        if (!(host == null || host == null)) {
            try {
                return host.equalsIgnoreCase(ProxyInfo.LOCAL_HOST) || NetworkUtils.numericToInetAddress(host).isLoopbackAddress();
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public static int validate(String hostname, String port, String exclList) {
        Matcher match = HOSTNAME_PATTERN.matcher(hostname);
        Matcher listMatch = EXCLLIST_PATTERN.matcher(exclList);
        if (!match.matches()) {
            return PROXY_HOSTNAME_INVALID;
        }
        if (!listMatch.matches()) {
            return PROXY_EXCLLIST_INVALID;
        }
        if (hostname.length() > 0 && port.length() == 0) {
            return PROXY_PORT_EMPTY;
        }
        if (port.length() > 0) {
            if (hostname.length() == 0) {
                return PROXY_HOSTNAME_EMPTY;
            }
            try {
                int portVal = Integer.parseInt(port);
                if (portVal <= 0 || portVal > PowerManager.WAKE_LOCK_LEVEL_MASK) {
                    return PROXY_PORT_INVALID;
                }
            } catch (NumberFormatException e) {
                return PROXY_PORT_INVALID;
            }
        }
        return PROXY_VALID;
    }

    public static final void setHttpProxySystemProperty(ProxyInfo p) {
        String host = null;
        String port = null;
        String exclList = null;
        Uri pacFileUrl = Uri.EMPTY;
        if (p != null) {
            host = p.getHost();
            port = Integer.toString(p.getPort());
            exclList = p.getExclusionListAsString();
            pacFileUrl = p.getPacFileUrl();
        }
        setHttpProxySystemProperty(host, port, exclList, pacFileUrl);
    }

    public static final void setHttpProxySystemProperty(String host, String port, String exclList, Uri pacFileUrl) {
        if (exclList != null) {
            exclList = exclList.replace(",", "|");
        }
        if (host != null) {
            System.setProperty("http.proxyHost", host);
            System.setProperty("https.proxyHost", host);
        } else {
            System.clearProperty("http.proxyHost");
            System.clearProperty("https.proxyHost");
        }
        if (port != null) {
            System.setProperty("http.proxyPort", port);
            System.setProperty("https.proxyPort", port);
        } else {
            System.clearProperty("http.proxyPort");
            System.clearProperty("https.proxyPort");
        }
        if (exclList != null) {
            System.setProperty("http.nonProxyHosts", exclList);
            System.setProperty("https.nonProxyHosts", exclList);
        } else {
            System.clearProperty("http.nonProxyHosts");
            System.clearProperty("https.nonProxyHosts");
        }
        if (Uri.EMPTY.equals(pacFileUrl)) {
            ProxySelector.setDefault(sDefaultProxySelector);
        } else {
            ProxySelector.setDefault(new PacProxySelector());
        }
    }
}
