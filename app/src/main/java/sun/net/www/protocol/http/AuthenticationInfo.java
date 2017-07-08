package sun.net.www.protocol.http;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import sun.net.www.HeaderParser;
import sun.net.www.protocol.http.AuthCacheValue.Type;

public abstract class AuthenticationInfo extends AuthCacheValue implements Cloneable {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final char PROXY_AUTHENTICATION = 'p';
    public static final char SERVER_AUTHENTICATION = 's';
    private static HashMap<String, Thread> requests;
    static boolean serializeAuth;
    AuthScheme authScheme;
    String host;
    String path;
    int port;
    String protocol;
    protected transient PasswordAuthentication pw;
    String realm;
    String s1;
    String s2;
    char type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.www.protocol.http.AuthenticationInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.www.protocol.http.AuthenticationInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.www.protocol.http.AuthenticationInfo.<clinit>():void");
    }

    public abstract String getHeaderValue(URL url, String str);

    public abstract boolean isAuthorizationStale(String str);

    public abstract boolean setHeaders(HttpURLConnection httpURLConnection, HeaderParser headerParser, String str);

    public abstract boolean supportsPreemptiveAuthorization();

    public PasswordAuthentication credentials() {
        return this.pw;
    }

    public Type getAuthType() {
        if (this.type == SERVER_AUTHENTICATION) {
            return Type.Server;
        }
        return Type.Proxy;
    }

    AuthScheme getAuthScheme() {
        return this.authScheme;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getRealm() {
        return this.realm;
    }

    public String getPath() {
        return this.path;
    }

    public String getProtocolScheme() {
        return this.protocol;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean requestIsInProgress(String key) {
        if (!serializeAuth) {
            return -assertionsDisabled;
        }
        synchronized (requests) {
            Thread c = Thread.currentThread();
            Thread t = (Thread) requests.get(key);
            if (t == null) {
                requests.put(key, c);
                return -assertionsDisabled;
            } else if (t == c) {
                return -assertionsDisabled;
            } else {
                while (true) {
                    if (requests.containsKey(key)) {
                        try {
                            requests.wait();
                        } catch (InterruptedException e) {
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
    }

    private static void requestCompleted(String key) {
        synchronized (requests) {
            Thread thread = (Thread) requests.get(key);
            if (thread != null && thread == Thread.currentThread()) {
                boolean waspresent = requests.remove(key) != null ? true : -assertionsDisabled;
                if (!(-assertionsDisabled || waspresent)) {
                    throw new AssertionError();
                }
            }
            requests.notifyAll();
        }
    }

    public AuthenticationInfo(char type, AuthScheme authScheme, String host, int port, String realm) {
        this.type = type;
        this.authScheme = authScheme;
        this.protocol = "";
        this.host = host.toLowerCase();
        this.port = port;
        this.realm = realm;
        this.path = null;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public AuthenticationInfo(char type, AuthScheme authScheme, URL url, String realm) {
        this.type = type;
        this.authScheme = authScheme;
        this.protocol = url.getProtocol().toLowerCase();
        this.host = url.getHost().toLowerCase();
        this.port = url.getPort();
        if (this.port == -1) {
            this.port = url.getDefaultPort();
        }
        this.realm = realm;
        String urlPath = url.getPath();
        if (urlPath.length() == 0) {
            this.path = urlPath;
        } else {
            this.path = reducePath(urlPath);
        }
    }

    static String reducePath(String urlPath) {
        int sepIndex = urlPath.lastIndexOf(47);
        int targetSuffixIndex = urlPath.lastIndexOf(46);
        if (sepIndex == -1 || sepIndex >= targetSuffixIndex) {
            return urlPath;
        }
        return urlPath.substring(0, sepIndex + 1);
    }

    static AuthenticationInfo getServerAuth(URL url) {
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        return getAuth("s:" + url.getProtocol().toLowerCase() + ":" + url.getHost().toLowerCase() + ":" + port, url);
    }

    static String getServerAuthKey(URL url, String realm, AuthScheme scheme) {
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        return "s:" + scheme + ":" + url.getProtocol().toLowerCase() + ":" + url.getHost().toLowerCase() + ":" + port + ":" + realm;
    }

    static AuthenticationInfo getServerAuth(String key) {
        AuthenticationInfo cached = getAuth(key, null);
        if (cached == null && requestIsInProgress(key)) {
            return getAuth(key, null);
        }
        return cached;
    }

    static AuthenticationInfo getAuth(String key, URL url) {
        if (url == null) {
            return (AuthenticationInfo) cache.get(key, null);
        }
        return (AuthenticationInfo) cache.get(key, url.getPath());
    }

    static AuthenticationInfo getProxyAuth(String host, int port) {
        return (AuthenticationInfo) cache.get("p::" + host.toLowerCase() + ":" + port, null);
    }

    static String getProxyAuthKey(String host, int port, String realm, AuthScheme scheme) {
        return "p:" + scheme + "::" + host.toLowerCase() + ":" + port + ":" + realm;
    }

    static AuthenticationInfo getProxyAuth(String key) {
        AuthenticationInfo cached = (AuthenticationInfo) cache.get(key, null);
        if (cached == null && requestIsInProgress(key)) {
            return (AuthenticationInfo) cache.get(key, null);
        }
        return cached;
    }

    void addToCache() {
        String key = cacheKey(true);
        cache.put(key, this);
        if (supportsPreemptiveAuthorization()) {
            cache.put(cacheKey(-assertionsDisabled), this);
        }
        endAuthRequest(key);
    }

    static void endAuthRequest(String key) {
        if (serializeAuth) {
            synchronized (requests) {
                requestCompleted(key);
            }
        }
    }

    void removeFromCache() {
        cache.remove(cacheKey(true), this);
        if (supportsPreemptiveAuthorization()) {
            cache.remove(cacheKey(-assertionsDisabled), this);
        }
    }

    public String getHeaderName() {
        if (this.type == SERVER_AUTHENTICATION) {
            return "Authorization";
        }
        return "Proxy-authorization";
    }

    String cacheKey(boolean includeRealm) {
        if (includeRealm) {
            return this.type + ":" + this.authScheme + ":" + this.protocol + ":" + this.host + ":" + this.port + ":" + this.realm;
        }
        return this.type + ":" + this.protocol + ":" + this.host + ":" + this.port;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.pw = new PasswordAuthentication(this.s1, this.s2.toCharArray());
        this.s1 = null;
        this.s2 = null;
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        this.s1 = this.pw.getUserName();
        this.s2 = new String(this.pw.getPassword());
        s.defaultWriteObject();
    }
}
