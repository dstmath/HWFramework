package java.net;

import java.io.Closeable;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Pack200.Unpacker;
import sun.misc.Resource;
import sun.misc.URLClassPath;
import sun.net.www.ParseUtil;
import sun.net.www.protocol.file.FileURLConnection;
import sun.security.util.SecurityConstants;
import sun.util.locale.LanguageTag;

public class URLClassLoader extends SecureClassLoader implements Closeable {
    private final AccessControlContext acc;
    private WeakHashMap<Closeable, Void> closeables;
    private final URLClassPath ucp;

    /* renamed from: java.net.URLClassLoader.1 */
    class AnonymousClass1 implements PrivilegedExceptionAction<Class> {
        final /* synthetic */ String val$name;

        AnonymousClass1(String val$name) {
            this.val$name = val$name;
        }

        public Class run() throws ClassNotFoundException {
            Resource res = URLClassLoader.this.ucp.getResource(this.val$name.replace('.', '/').concat(".class"), false);
            if (res != null) {
                try {
                    return URLClassLoader.this.defineClass(this.val$name, res);
                } catch (IOException e) {
                    throw new ClassNotFoundException(this.val$name, e);
                }
            }
            throw new ClassNotFoundException(this.val$name);
        }
    }

    /* renamed from: java.net.URLClassLoader.2 */
    class AnonymousClass2 implements PrivilegedAction<URL> {
        final /* synthetic */ String val$name;

        AnonymousClass2(String val$name) {
            this.val$name = val$name;
        }

        public URL run() {
            return URLClassLoader.this.ucp.findResource(this.val$name, true);
        }
    }

    /* renamed from: java.net.URLClassLoader.3 */
    class AnonymousClass3 implements Enumeration<URL> {
        private URL url;
        final /* synthetic */ Enumeration val$e;

        /* renamed from: java.net.URLClassLoader.3.1 */
        class AnonymousClass1 implements PrivilegedAction<URL> {
            final /* synthetic */ Enumeration val$e;

            AnonymousClass1(Enumeration val$e) {
                this.val$e = val$e;
            }

            public URL run() {
                if (this.val$e.hasMoreElements()) {
                    return (URL) this.val$e.nextElement();
                }
                return null;
            }
        }

        AnonymousClass3(Enumeration val$e) {
            this.val$e = val$e;
            this.url = null;
        }

        private boolean next() {
            boolean z = true;
            if (this.url != null) {
                return true;
            }
            do {
                URL u = (URL) AccessController.doPrivileged(new AnonymousClass1(this.val$e), URLClassLoader.this.acc);
                if (u == null) {
                    break;
                }
                this.url = URLClassLoader.this.ucp.checkURL(u);
            } while (this.url == null);
            if (this.url == null) {
                z = false;
            }
            return z;
        }

        public URL nextElement() {
            if (next()) {
                URL u = this.url;
                this.url = null;
                return u;
            }
            throw new NoSuchElementException();
        }

        public boolean hasMoreElements() {
            return next();
        }
    }

    /* renamed from: java.net.URLClassLoader.4 */
    class AnonymousClass4 implements PrivilegedAction<Void> {
        final /* synthetic */ Permission val$fp;
        final /* synthetic */ SecurityManager val$sm;

        AnonymousClass4(SecurityManager val$sm, Permission val$fp) {
            this.val$sm = val$sm;
            this.val$fp = val$fp;
        }

        public Void run() throws SecurityException {
            this.val$sm.checkPermission(this.val$fp);
            return null;
        }
    }

    /* renamed from: java.net.URLClassLoader.5 */
    static class AnonymousClass5 implements PrivilegedAction<URLClassLoader> {
        final /* synthetic */ AccessControlContext val$acc;
        final /* synthetic */ ClassLoader val$parent;
        final /* synthetic */ URL[] val$urls;

        AnonymousClass5(URL[] val$urls, ClassLoader val$parent, AccessControlContext val$acc) {
            this.val$urls = val$urls;
            this.val$parent = val$parent;
            this.val$acc = val$acc;
        }

        public URLClassLoader run() {
            return new FactoryURLClassLoader(this.val$urls, this.val$parent, this.val$acc);
        }
    }

    /* renamed from: java.net.URLClassLoader.6 */
    static class AnonymousClass6 implements PrivilegedAction<URLClassLoader> {
        final /* synthetic */ AccessControlContext val$acc;
        final /* synthetic */ URL[] val$urls;

        AnonymousClass6(URL[] val$urls, AccessControlContext val$acc) {
            this.val$urls = val$urls;
            this.val$acc = val$acc;
        }

        public URLClassLoader run() {
            return new FactoryURLClassLoader(this.val$urls, this.val$acc);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.net.URLClassLoader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.net.URLClassLoader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.net.URLClassLoader.<clinit>():void");
    }

    public URLClassLoader(URL[] urls, ClassLoader parent) {
        super(parent);
        this.closeables = new WeakHashMap();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.ucp = new URLClassPath(urls);
        this.acc = AccessController.getContext();
    }

    URLClassLoader(URL[] urls, ClassLoader parent, AccessControlContext acc) {
        super(parent);
        this.closeables = new WeakHashMap();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.ucp = new URLClassPath(urls);
        this.acc = acc;
    }

    public URLClassLoader(URL[] urls) {
        this.closeables = new WeakHashMap();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.ucp = new URLClassPath(urls);
        this.acc = AccessController.getContext();
    }

    URLClassLoader(URL[] urls, AccessControlContext acc) {
        this.closeables = new WeakHashMap();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.ucp = new URLClassPath(urls);
        this.acc = acc;
    }

    public URLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(parent);
        this.closeables = new WeakHashMap();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.ucp = new URLClassPath(urls, factory);
        this.acc = AccessController.getContext();
    }

    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        if (url == null) {
            return null;
        }
        try {
            URLConnection urlc = url.openConnection();
            InputStream is = urlc.getInputStream();
            if (urlc instanceof JarURLConnection) {
                JarFile jar = ((JarURLConnection) urlc).getJarFile();
                synchronized (this.closeables) {
                    if (!this.closeables.containsKey(jar)) {
                        this.closeables.put(jar, null);
                    }
                }
            } else if (urlc instanceof FileURLConnection) {
                synchronized (this.closeables) {
                    this.closeables.put(is, null);
                }
            }
            return is;
        } catch (IOException e) {
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("closeClassLoader"));
        }
        List<IOException> errors = this.ucp.closeLoaders();
        synchronized (this.closeables) {
            for (Closeable c : this.closeables.keySet()) {
                try {
                    c.close();
                } catch (IOException ioex) {
                    errors.add(ioex);
                }
            }
            this.closeables.clear();
        }
        if (!errors.isEmpty()) {
            IOException firstex = (IOException) errors.remove(0);
            for (IOException error : errors) {
                firstex.addSuppressed(error);
            }
            throw firstex;
        }
    }

    protected void addURL(URL url) {
        this.ucp.addURL(url);
    }

    public URL[] getURLs() {
        return this.ucp.getURLs();
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return (Class) AccessController.doPrivileged(new AnonymousClass1(name), this.acc);
        } catch (PrivilegedActionException pae) {
            throw ((ClassNotFoundException) pae.getException());
        }
    }

    private Package getAndVerifyPackage(String pkgname, Manifest man, URL url) {
        Package pkg = getPackage(pkgname);
        if (pkg != null) {
            if (pkg.isSealed()) {
                if (!pkg.isSealed(url)) {
                    throw new SecurityException("sealing violation: package " + pkgname + " is sealed");
                }
            } else if (man != null && isSealed(pkgname, man)) {
                throw new SecurityException("sealing violation: can't seal package " + pkgname + ": already loaded");
            }
        }
        return pkg;
    }

    private Class defineClass(String name, Resource res) throws IOException {
        long t0 = System.nanoTime();
        int i = name.lastIndexOf(46);
        URL url = res.getCodeSourceURL();
        if (i != -1) {
            String pkgname = name.substring(0, i);
            Manifest man = res.getManifest();
            if (getAndVerifyPackage(pkgname, man, url) == null) {
                if (man != null) {
                    try {
                        definePackage(pkgname, man, url);
                    } catch (IllegalArgumentException e) {
                        if (getAndVerifyPackage(pkgname, man, url) == null) {
                            throw new AssertionError("Cannot find package " + pkgname);
                        }
                    }
                }
                definePackage(pkgname, null, null, null, null, null, null, null);
            }
        }
        ByteBuffer bb = res.getByteBuffer();
        if (bb != null) {
            return defineClass(name, bb, new CodeSource(url, res.getCodeSigners()));
        }
        byte[] b = res.getBytes();
        return defineClass(name, b, 0, b.length, new CodeSource(url, res.getCodeSigners()));
    }

    protected Package definePackage(String name, Manifest man, URL url) throws IllegalArgumentException {
        String str = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        String sealed = null;
        URL sealBase = null;
        Attributes attr = man.getAttributes(name.replace('.', '/').concat("/"));
        if (attr != null) {
            str = attr.getValue(Name.SPECIFICATION_TITLE);
            str2 = attr.getValue(Name.SPECIFICATION_VERSION);
            str3 = attr.getValue(Name.SPECIFICATION_VENDOR);
            str4 = attr.getValue(Name.IMPLEMENTATION_TITLE);
            str5 = attr.getValue(Name.IMPLEMENTATION_VERSION);
            str6 = attr.getValue(Name.IMPLEMENTATION_VENDOR);
            sealed = attr.getValue(Name.SEALED);
        }
        attr = man.getMainAttributes();
        if (attr != null) {
            if (str == null) {
                str = attr.getValue(Name.SPECIFICATION_TITLE);
            }
            if (str2 == null) {
                str2 = attr.getValue(Name.SPECIFICATION_VERSION);
            }
            if (str3 == null) {
                str3 = attr.getValue(Name.SPECIFICATION_VENDOR);
            }
            if (str4 == null) {
                str4 = attr.getValue(Name.IMPLEMENTATION_TITLE);
            }
            if (str5 == null) {
                str5 = attr.getValue(Name.IMPLEMENTATION_VERSION);
            }
            if (str6 == null) {
                str6 = attr.getValue(Name.IMPLEMENTATION_VENDOR);
            }
            if (sealed == null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        if (Unpacker.TRUE.equalsIgnoreCase(sealed)) {
            sealBase = url;
        }
        return definePackage(name, str, str2, str3, str4, str5, str6, sealBase);
    }

    private boolean isSealed(String name, Manifest man) {
        Attributes attr = man.getAttributes(name.replace('.', '/').concat("/"));
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Name.SEALED);
        }
        if (sealed == null) {
            attr = man.getMainAttributes();
            if (attr != null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        return Unpacker.TRUE.equalsIgnoreCase(sealed);
    }

    public URL findResource(String name) {
        URL url = (URL) AccessController.doPrivileged(new AnonymousClass2(name), this.acc);
        if (url != null) {
            return this.ucp.checkURL(url);
        }
        return null;
    }

    public Enumeration<URL> findResources(String name) throws IOException {
        return new AnonymousClass3(this.ucp.findResources(name, true));
    }

    protected PermissionCollection getPermissions(CodeSource codesource) {
        URLConnection urlConnection;
        Permission p;
        PermissionCollection perms = super.getPermissions(codesource);
        URL url = codesource.getLocation();
        try {
            urlConnection = url.openConnection();
            p = urlConnection.getPermission();
        } catch (IOException e) {
            p = null;
            urlConnection = null;
        }
        String path;
        if (p instanceof FilePermission) {
            path = p.getName();
            if (path.endsWith(File.separator)) {
                p = new FilePermission(path + LanguageTag.SEP, SecurityConstants.PROPERTY_READ_ACTION);
            }
        } else if (p == null && url.getProtocol().equals("file")) {
            path = ParseUtil.decode(url.getFile().replace('/', File.separatorChar));
            if (path.endsWith(File.separator)) {
                path = path + LanguageTag.SEP;
            }
            p = new FilePermission(path, SecurityConstants.PROPERTY_READ_ACTION);
        } else {
            URL locUrl = url;
            if (urlConnection instanceof JarURLConnection) {
                locUrl = ((JarURLConnection) urlConnection).getJarFileURL();
            }
            String host = locUrl.getHost();
            if (host != null && host.length() > 0) {
                p = new SocketPermission(host, SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION);
            }
        }
        if (p != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                AccessController.doPrivileged(new AnonymousClass4(sm, p), this.acc);
            }
            perms.add(p);
        }
        return perms;
    }

    public static URLClassLoader newInstance(URL[] urls, ClassLoader parent) {
        return (URLClassLoader) AccessController.doPrivileged(new AnonymousClass5(urls, parent, AccessController.getContext()));
    }

    public static URLClassLoader newInstance(URL[] urls) {
        return (URLClassLoader) AccessController.doPrivileged(new AnonymousClass6(urls, AccessController.getContext()));
    }
}
