package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.net.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;
import sun.net.ApplicationProxy;
import sun.net.www.protocol.file.Handler;
import sun.security.util.SecurityConstants;

public final class URL implements Serializable {
    private static final Set<String> BUILTIN_HANDLER_CLASS_NAMES = createBuiltinHandlerClassNames();
    static URLStreamHandlerFactory factory = null;
    static Hashtable<String, URLStreamHandler> handlers = new Hashtable<>();
    private static final String protocolPathProp = "java.protocol.handler.pkgs";
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("protocol", String.class), new ObjectStreamField("host", String.class), new ObjectStreamField("port", Integer.TYPE), new ObjectStreamField("authority", String.class), new ObjectStreamField("file", String.class), new ObjectStreamField("ref", String.class)};
    static final long serialVersionUID = -7627629688361524110L;
    private static Object streamHandlerLock = new Object();
    private String authority;
    private String file;
    transient URLStreamHandler handler;
    private int hashCode;
    private String host;
    transient InetAddress hostAddress;
    private transient String path;
    private int port;
    private String protocol;
    private transient String query;
    private String ref;
    private transient UrlDeserializedState tempState;
    private transient String userInfo;

    public URL(String protocol2, String host2, int port2, String file2) throws MalformedURLException {
        this(protocol2, host2, port2, file2, null);
    }

    public URL(String protocol2, String host2, String file2) throws MalformedURLException {
        this(protocol2, host2, -1, file2);
    }

    public URL(String protocol2, String host2, int port2, String file2, URLStreamHandler handler2) throws MalformedURLException {
        String str;
        this.port = -1;
        this.hashCode = -1;
        if (handler2 != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                checkSpecifyHandler(sm);
            }
        }
        String protocol3 = protocol2.toLowerCase();
        this.protocol = protocol3;
        if (host2 != null) {
            if (host2.indexOf(58) >= 0 && !host2.startsWith("[")) {
                host2 = "[" + host2 + "]";
            }
            this.host = host2;
            if (port2 >= -1) {
                this.port = port2;
                if (port2 == -1) {
                    str = host2;
                } else {
                    str = host2 + ":" + port2;
                }
                this.authority = str;
            } else {
                throw new MalformedURLException("Invalid port number :" + port2);
            }
        }
        Parts parts = new Parts(file2, host2);
        this.path = parts.getPath();
        this.query = parts.getQuery();
        if (this.query != null) {
            this.file = this.path + "?" + this.query;
        } else {
            this.file = this.path;
        }
        this.ref = parts.getRef();
        if (handler2 == null) {
            URLStreamHandler uRLStreamHandler = getURLStreamHandler(protocol3);
            handler2 = uRLStreamHandler;
            if (uRLStreamHandler == null) {
                throw new MalformedURLException("unknown protocol: " + protocol3);
            }
        }
        this.handler = handler2;
    }

    public URL(String spec) throws MalformedURLException {
        this(null, spec);
    }

    public URL(URL context, String spec) throws MalformedURLException {
        this(context, spec, (URLStreamHandler) null);
    }

    public URL(URL context, String spec, URLStreamHandler handler2) throws MalformedURLException {
        this.port = -1;
        this.hashCode = -1;
        String original = spec;
        int start = 0;
        String newProtocol = null;
        boolean aRef = false;
        boolean isRelative = false;
        if (handler2 != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                checkSpecifyHandler(sm);
            }
        }
        try {
            int limit = spec.length();
            while (limit > 0 && spec.charAt(limit - 1) <= ' ') {
                limit--;
            }
            while (start < limit && spec.charAt(start) <= ' ') {
                start++;
            }
            start = spec.regionMatches(true, start, "url:", 0, 4) ? start + 4 : start;
            if (start < spec.length() && spec.charAt(start) == '#') {
                aRef = true;
            }
            int i = start;
            while (true) {
                if (aRef || i >= limit) {
                    break;
                }
                int charAt = spec.charAt(i);
                int c = charAt;
                if (charAt == 47) {
                    break;
                } else if (c == 58) {
                    String s = spec.substring(start, i).toLowerCase();
                    if (isValidProtocol(s)) {
                        newProtocol = s;
                        start = i + 1;
                    }
                } else {
                    i++;
                }
            }
            this.protocol = newProtocol;
            if (context != null && (newProtocol == null || newProtocol.equalsIgnoreCase(context.protocol))) {
                handler2 = handler2 == null ? context.handler : handler2;
                if (context.path != null && context.path.startsWith("/")) {
                    newProtocol = null;
                }
                if (newProtocol == null) {
                    this.protocol = context.protocol;
                    this.authority = context.authority;
                    this.userInfo = context.userInfo;
                    this.host = context.host;
                    this.port = context.port;
                    this.file = context.file;
                    this.path = context.path;
                    isRelative = true;
                }
            }
            if (this.protocol != null) {
                if (handler2 == null) {
                    URLStreamHandler uRLStreamHandler = getURLStreamHandler(this.protocol);
                    handler2 = uRLStreamHandler;
                    if (uRLStreamHandler == null) {
                        throw new MalformedURLException("unknown protocol: " + this.protocol);
                    }
                }
                this.handler = handler2;
                int i2 = spec.indexOf(35, start);
                if (i2 >= 0) {
                    this.ref = spec.substring(i2 + 1, limit);
                    limit = i2;
                }
                if (isRelative && start == limit) {
                    this.query = context.query;
                    if (this.ref == null) {
                        this.ref = context.ref;
                    }
                }
                handler2.parseURL(this, spec, start, limit);
                return;
            }
            throw new MalformedURLException("no protocol: " + original);
        } catch (MalformedURLException e) {
            throw e;
        } catch (Exception e2) {
            MalformedURLException exception = new MalformedURLException(e2.getMessage());
            exception.initCause(e2);
            throw exception;
        }
    }

    private boolean isValidProtocol(String protocol2) {
        int len = protocol2.length();
        if (len < 1) {
            return false;
        }
        char c = protocol2.charAt(0);
        if (!Character.isLetter(c)) {
            return false;
        }
        char c2 = c;
        for (int i = 1; i < len; i++) {
            char c3 = protocol2.charAt(i);
            if (!Character.isLetterOrDigit(c3) && c3 != '.' && c3 != '+' && c3 != '-') {
                return false;
            }
        }
        return true;
    }

    private void checkSpecifyHandler(SecurityManager sm) {
        sm.checkPermission(SecurityConstants.SPECIFY_HANDLER_PERMISSION);
    }

    /* access modifiers changed from: package-private */
    public void set(String protocol2, String host2, int port2, String file2, String ref2) {
        String str;
        synchronized (this) {
            this.protocol = protocol2;
            this.host = host2;
            if (port2 == -1) {
                str = host2;
            } else {
                str = host2 + ":" + port2;
            }
            this.authority = str;
            this.port = port2;
            this.file = file2;
            this.ref = ref2;
            this.hashCode = -1;
            this.hostAddress = null;
            int q = file2.lastIndexOf(63);
            if (q != -1) {
                this.query = file2.substring(q + 1);
                this.path = file2.substring(0, q);
            } else {
                this.path = file2;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void set(String protocol2, String host2, int port2, String authority2, String userInfo2, String path2, String query2, String ref2) {
        String str;
        synchronized (this) {
            this.protocol = protocol2;
            this.host = host2;
            this.port = port2;
            if (query2 != null) {
                if (!query2.isEmpty()) {
                    str = path2 + "?" + query2;
                    this.file = str;
                    this.userInfo = userInfo2;
                    this.path = path2;
                    this.ref = ref2;
                    this.hashCode = -1;
                    this.hostAddress = null;
                    this.query = query2;
                    this.authority = authority2;
                }
            }
            str = path2;
            this.file = str;
            this.userInfo = userInfo2;
            this.path = path2;
            this.ref = ref2;
            this.hashCode = -1;
            this.hostAddress = null;
            this.query = query2;
            this.authority = authority2;
        }
    }

    public String getQuery() {
        return this.query;
    }

    public String getPath() {
        return this.path;
    }

    public String getUserInfo() {
        return this.userInfo;
    }

    public String getAuthority() {
        return this.authority;
    }

    public int getPort() {
        return this.port;
    }

    public int getDefaultPort() {
        return this.handler.getDefaultPort();
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getHost() {
        return this.host;
    }

    public String getFile() {
        return this.file;
    }

    public String getRef() {
        return this.ref;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof URL)) {
            return false;
        }
        return this.handler.equals(this, (URL) obj);
    }

    public synchronized int hashCode() {
        if (this.hashCode != -1) {
            return this.hashCode;
        }
        this.hashCode = this.handler.hashCode(this);
        return this.hashCode;
    }

    public boolean sameFile(URL other) {
        return this.handler.sameFile(this, other);
    }

    public String toString() {
        return toExternalForm();
    }

    public String toExternalForm() {
        return this.handler.toExternalForm(this);
    }

    public URI toURI() throws URISyntaxException {
        return new URI(toString());
    }

    public URLConnection openConnection() throws IOException {
        return this.handler.openConnection(this);
    }

    public URLConnection openConnection(Proxy proxy) throws IOException {
        if (proxy != null) {
            Proxy p = proxy == Proxy.NO_PROXY ? Proxy.NO_PROXY : ApplicationProxy.create(proxy);
            SecurityManager sm = System.getSecurityManager();
            if (!(p.type() == Proxy.Type.DIRECT || sm == null)) {
                InetSocketAddress epoint = (InetSocketAddress) p.address();
                if (epoint.isUnresolved()) {
                    sm.checkConnect(epoint.getHostName(), epoint.getPort());
                } else {
                    sm.checkConnect(epoint.getAddress().getHostAddress(), epoint.getPort());
                }
            }
            return this.handler.openConnection(this, p);
        }
        throw new IllegalArgumentException("proxy can not be null");
    }

    public final InputStream openStream() throws IOException {
        return openConnection().getInputStream();
    }

    public final Object getContent() throws IOException {
        return openConnection().getContent();
    }

    public final Object getContent(Class[] classes) throws IOException {
        return openConnection().getContent(classes);
    }

    public static void setURLStreamHandlerFactory(URLStreamHandlerFactory fac) {
        synchronized (streamHandlerLock) {
            if (factory == null) {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    security.checkSetFactory();
                }
                handlers.clear();
                factory = fac;
            } else {
                throw new Error("factory already defined");
            }
        }
    }

    static URLStreamHandler getURLStreamHandler(String protocol2) {
        URLStreamHandler handler2 = handlers.get(protocol2);
        if (handler2 == null) {
            boolean checkedWithFactory = false;
            if (factory != null) {
                handler2 = factory.createURLStreamHandler(protocol2);
                checkedWithFactory = true;
            }
            if (handler2 == null) {
                StringTokenizer packagePrefixIter = new StringTokenizer(System.getProperty(protocolPathProp, ""), "|");
                while (handler2 == null && packagePrefixIter.hasMoreTokens()) {
                    String packagePrefix = packagePrefixIter.nextToken().trim();
                    try {
                        String clsName = packagePrefix + "." + protocol2 + ".Handler";
                        Class<?> cls = null;
                        try {
                            cls = Class.forName(clsName, true, ClassLoader.getSystemClassLoader());
                        } catch (ClassNotFoundException e) {
                            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
                            if (contextLoader != null) {
                                cls = Class.forName(clsName, true, contextLoader);
                            }
                        }
                        if (cls != null) {
                            handler2 = (URLStreamHandler) cls.newInstance();
                        }
                    } catch (ReflectiveOperationException e2) {
                    }
                }
            }
            if (handler2 == null) {
                try {
                    handler2 = createBuiltinHandler(protocol2);
                } catch (Exception e3) {
                    throw new AssertionError((Object) e3);
                }
            }
            synchronized (streamHandlerLock) {
                URLStreamHandler handler22 = handlers.get(protocol2);
                if (handler22 != null) {
                    return handler22;
                }
                if (!checkedWithFactory && factory != null) {
                    handler22 = factory.createURLStreamHandler(protocol2);
                }
                if (handler22 != null) {
                    handler2 = handler22;
                }
                if (handler2 != null) {
                    handlers.put(protocol2, handler2);
                }
            }
        }
        return handler2;
    }

    private static URLStreamHandler createBuiltinHandler(String protocol2) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (protocol2.equals("file")) {
            return new Handler();
        }
        if (protocol2.equals("ftp")) {
            return new sun.net.www.protocol.ftp.Handler();
        }
        if (protocol2.equals("jar")) {
            return new sun.net.www.protocol.jar.Handler();
        }
        if (protocol2.equals("http")) {
            return (URLStreamHandler) Class.forName("com.android.okhttp.HttpHandler").newInstance();
        }
        if (protocol2.equals("https")) {
            return (URLStreamHandler) Class.forName("com.android.okhttp.HttpsHandler").newInstance();
        }
        return null;
    }

    private static Set<String> createBuiltinHandlerClassNames() {
        Set<String> result = new HashSet<>();
        result.add("sun.net.www.protocol.file.Handler");
        result.add("sun.net.www.protocol.ftp.Handler");
        result.add("sun.net.www.protocol.jar.Handler");
        result.add("com.android.okhttp.HttpHandler");
        result.add("com.android.okhttp.HttpsHandler");
        return Collections.unmodifiableSet(result);
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    private synchronized void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        String str;
        synchronized (this) {
            ObjectInputStream.GetField gf = s.readFields();
            String protocol2 = (String) gf.get("protocol", (Object) null);
            if (getURLStreamHandler(protocol2) != null) {
                String host2 = (String) gf.get("host", (Object) null);
                int port2 = gf.get("port", -1);
                String authority2 = (String) gf.get("authority", (Object) null);
                String file2 = (String) gf.get("file", (Object) null);
                String ref2 = (String) gf.get("ref", (Object) null);
                if (authority2 == null && ((host2 != null && host2.length() > 0) || port2 != -1)) {
                    if (host2 == null) {
                        host2 = "";
                    }
                    if (port2 == -1) {
                        str = host2;
                    } else {
                        str = host2 + ":" + port2;
                    }
                    authority2 = str;
                }
                UrlDeserializedState urlDeserializedState = new UrlDeserializedState(protocol2, host2, port2, authority2, file2, ref2, -1);
                this.tempState = urlDeserializedState;
            } else {
                throw new IOException("unknown protocol: " + protocol2);
            }
        }
    }

    private Object readResolve() throws ObjectStreamException {
        URLStreamHandler handler2 = getURLStreamHandler(this.tempState.getProtocol());
        if (isBuiltinStreamHandler(handler2.getClass().getName())) {
            return fabricateNewURL();
        }
        return setDeserializedFields(handler2);
    }

    private URL setDeserializedFields(URLStreamHandler handler2) {
        String str;
        String userInfo2 = null;
        String protocol2 = this.tempState.getProtocol();
        String host2 = this.tempState.getHost();
        int port2 = this.tempState.getPort();
        String authority2 = this.tempState.getAuthority();
        String file2 = this.tempState.getFile();
        String ref2 = this.tempState.getRef();
        int hashCode2 = this.tempState.getHashCode();
        if (authority2 == null && ((host2 != null && host2.length() > 0) || port2 != -1)) {
            if (host2 == null) {
                host2 = "";
            }
            if (port2 == -1) {
                str = host2;
            } else {
                str = host2 + ":" + port2;
            }
            authority2 = str;
            int at = host2.lastIndexOf(64);
            if (at != -1) {
                userInfo2 = host2.substring(0, at);
                host2 = host2.substring(at + 1);
            }
        } else if (authority2 != null) {
            int ind = authority2.indexOf(64);
            if (ind != -1) {
                userInfo2 = authority2.substring(0, ind);
            }
        }
        String path2 = null;
        String query2 = null;
        if (file2 != null) {
            int q = file2.lastIndexOf(63);
            if (q != -1) {
                query2 = file2.substring(q + 1);
                path2 = file2.substring(0, q);
            } else {
                path2 = file2;
            }
        }
        this.protocol = protocol2;
        this.host = host2;
        this.port = port2;
        this.file = file2;
        this.authority = authority2;
        this.ref = ref2;
        this.hashCode = hashCode2;
        this.handler = handler2;
        this.query = query2;
        this.path = path2;
        this.userInfo = userInfo2;
        return this;
    }

    private URL fabricateNewURL() throws InvalidObjectException {
        String urlString = this.tempState.reconstituteUrlString();
        try {
            URL replacementURL = new URL(urlString);
            replacementURL.setSerializedHashCode(this.tempState.getHashCode());
            resetState();
            return replacementURL;
        } catch (MalformedURLException mEx) {
            resetState();
            InvalidObjectException invoEx = new InvalidObjectException("Malformed URL: " + urlString);
            invoEx.initCause(mEx);
            throw invoEx;
        }
    }

    private boolean isBuiltinStreamHandler(String handlerClassName) {
        return BUILTIN_HANDLER_CLASS_NAMES.contains(handlerClassName);
    }

    private void resetState() {
        this.protocol = null;
        this.host = null;
        this.port = -1;
        this.file = null;
        this.authority = null;
        this.ref = null;
        this.hashCode = -1;
        this.handler = null;
        this.query = null;
        this.path = null;
        this.userInfo = null;
        this.tempState = null;
    }

    private void setSerializedHashCode(int hc) {
        this.hashCode = hc;
    }
}
