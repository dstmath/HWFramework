package sun.net.spi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import sun.misc.REException;
import sun.misc.RegexpPool;
import sun.net.NetProperties;
import sun.net.SocksProxy;

public class DefaultProxySelector extends ProxySelector {
    private static final String SOCKS_PROXY_VERSION = "socksProxyVersion";
    private static boolean hasSystemProxies;
    static final String[][] props = null;

    /* renamed from: sun.net.spi.DefaultProxySelector.1 */
    class AnonymousClass1 implements PrivilegedAction<Proxy> {
        final /* synthetic */ NonProxyInfo val$nprop;
        final /* synthetic */ String val$proto;
        final /* synthetic */ String val$urlhost;

        AnonymousClass1(String val$proto, NonProxyInfo val$nprop, String val$urlhost) {
            this.val$proto = val$proto;
            this.val$nprop = val$nprop;
            this.val$urlhost = val$urlhost;
        }

        public Proxy run() {
            String phost = null;
            int i = 0;
            while (i < DefaultProxySelector.props.length) {
                if (DefaultProxySelector.props[i][0].equalsIgnoreCase(this.val$proto)) {
                    int j = 1;
                    while (j < DefaultProxySelector.props[i].length) {
                        phost = NetProperties.get(DefaultProxySelector.props[i][j] + "Host");
                        if (phost != null && phost.length() != 0) {
                            break;
                        }
                        j++;
                    }
                    if (phost == null || phost.length() == 0) {
                        return Proxy.NO_PROXY;
                    }
                    if (this.val$nprop != null) {
                        String nphosts = NetProperties.get(this.val$nprop.property);
                        synchronized (this.val$nprop) {
                            if (nphosts == null) {
                                if (this.val$nprop.defaultVal != null) {
                                    nphosts = this.val$nprop.defaultVal;
                                } else {
                                    this.val$nprop.hostsSource = null;
                                    this.val$nprop.hostsPool = null;
                                }
                            } else if (nphosts.length() != 0) {
                                nphosts = nphosts + "|localhost|127.*|[::1]|0.0.0.0|[::0]";
                            }
                            if (!(nphosts == null || nphosts.equals(this.val$nprop.hostsSource))) {
                                RegexpPool pool = new RegexpPool();
                                StringTokenizer st = new StringTokenizer(nphosts, "|", false);
                                while (st.hasMoreTokens()) {
                                    try {
                                        pool.add(st.nextToken().toLowerCase(), Boolean.TRUE);
                                    } catch (REException e) {
                                    }
                                }
                                this.val$nprop.hostsPool = pool;
                                this.val$nprop.hostsSource = nphosts;
                            }
                            if (this.val$nprop.hostsPool == null || this.val$nprop.hostsPool.match(this.val$urlhost) == null) {
                            } else {
                                Proxy proxy = Proxy.NO_PROXY;
                                return proxy;
                            }
                        }
                    }
                    int pport = NetProperties.getInteger(DefaultProxySelector.props[i][j] + "Port", 0).intValue();
                    if (pport == 0 && j < DefaultProxySelector.props[i].length - 1) {
                        for (int k = 1; k < DefaultProxySelector.props[i].length - 1; k++) {
                            if (k != j && pport == 0) {
                                pport = NetProperties.getInteger(DefaultProxySelector.props[i][k] + "Port", 0).intValue();
                            }
                        }
                    }
                    if (pport == 0) {
                        if (j == DefaultProxySelector.props[i].length - 1) {
                            pport = DefaultProxySelector.this.defaultPort("socket");
                        } else {
                            pport = DefaultProxySelector.this.defaultPort(this.val$proto);
                        }
                    }
                    InetSocketAddress saddr = InetSocketAddress.createUnresolved(phost, pport);
                    if (j == DefaultProxySelector.props[i].length - 1) {
                        return SocksProxy.create(saddr, NetProperties.getInteger(DefaultProxySelector.SOCKS_PROXY_VERSION, 5).intValue());
                    }
                    return new Proxy(Type.HTTP, saddr);
                }
                i++;
            }
            return Proxy.NO_PROXY;
        }
    }

    static class NonProxyInfo {
        static final String defStringVal = "localhost|127.*|[::1]|0.0.0.0|[::0]";
        static NonProxyInfo ftpNonProxyInfo;
        static NonProxyInfo httpNonProxyInfo;
        static NonProxyInfo httpsNonProxyInfo;
        final String defaultVal;
        RegexpPool hostsPool;
        String hostsSource;
        final String property;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.spi.DefaultProxySelector.NonProxyInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.spi.DefaultProxySelector.NonProxyInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.net.spi.DefaultProxySelector.NonProxyInfo.<clinit>():void");
        }

        NonProxyInfo(String p, String s, RegexpPool pool, String d) {
            this.property = p;
            this.hostsSource = s;
            this.hostsPool = pool;
            this.defaultVal = d;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.spi.DefaultProxySelector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.spi.DefaultProxySelector.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.net.spi.DefaultProxySelector.<clinit>():void");
    }

    public DefaultProxySelector() {
    }

    public List<Proxy> select(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }
        String protocol = uri.getScheme();
        String host = uri.getHost();
        if (host == null) {
            String auth = uri.getAuthority();
            if (auth != null) {
                int i = auth.indexOf(64);
                if (i >= 0) {
                    auth = auth.substring(i + 1);
                }
                i = auth.lastIndexOf(58);
                if (i >= 0) {
                    auth = auth.substring(0, i);
                }
                host = auth;
            }
        }
        if (protocol == null || host == null) {
            throw new IllegalArgumentException("protocol = " + protocol + " host = " + host);
        }
        List<Proxy> proxyl = new ArrayList(1);
        NonProxyInfo pinfo = null;
        if ("http".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.httpNonProxyInfo;
        } else if ("https".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.httpsNonProxyInfo;
        } else if ("ftp".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.ftpNonProxyInfo;
        }
        String proto = protocol;
        proxyl.add((Proxy) AccessController.doPrivileged(new AnonymousClass1(protocol, pinfo, host.toLowerCase())));
        return proxyl;
    }

    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (uri == null || sa == null || ioe == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
    }

    private int defaultPort(String protocol) {
        if ("http".equalsIgnoreCase(protocol)) {
            return 80;
        }
        if ("https".equalsIgnoreCase(protocol)) {
            return 443;
        }
        if ("ftp".equalsIgnoreCase(protocol)) {
            return 80;
        }
        if ("socket".equalsIgnoreCase(protocol)) {
            return SocksConsts.DEFAULT_PORT;
        }
        if ("gopher".equalsIgnoreCase(protocol)) {
            return 80;
        }
        return -1;
    }
}
