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
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.misc.Resource;
import sun.misc.URLClassPath;
import sun.net.www.ParseUtil;
import sun.net.www.protocol.file.FileURLConnection;
import sun.security.util.SecurityConstants;
import sun.util.locale.LanguageTag;

public class URLClassLoader extends SecureClassLoader implements Closeable {
    /* access modifiers changed from: private */
    public final AccessControlContext acc;
    private WeakHashMap<Closeable, Void> closeables = new WeakHashMap<>();
    /* access modifiers changed from: private */
    public final URLClassPath ucp;

    public URLClassLoader(URL[] urls, ClassLoader parent) {
        super(parent);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.acc = AccessController.getContext();
        this.ucp = new URLClassPath(urls, this.acc);
    }

    URLClassLoader(URL[] urls, ClassLoader parent, AccessControlContext acc2) {
        super(parent);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.acc = acc2;
        this.ucp = new URLClassPath(urls, acc2);
    }

    public URLClassLoader(URL[] urls) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.acc = AccessController.getContext();
        this.ucp = new URLClassPath(urls, this.acc);
    }

    URLClassLoader(URL[] urls, AccessControlContext acc2) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.acc = acc2;
        this.ucp = new URLClassPath(urls, acc2);
    }

    public URLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(parent);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        this.acc = AccessController.getContext();
        this.ucp = new URLClassPath(urls, factory, this.acc);
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
            IOException firstex = errors.remove(0);
            for (IOException error : errors) {
                firstex.addSuppressed(error);
            }
            throw firstex;
        }
    }

    /* access modifiers changed from: protected */
    public void addURL(URL url) {
        this.ucp.addURL(url);
    }

    public URL[] getURLs() {
        return this.ucp.getURLs();
    }

    /* access modifiers changed from: protected */
    public Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            Class<?> result = (Class) AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
                public Class<?> run() throws ClassNotFoundException {
                    Resource res = URLClassLoader.this.ucp.getResource(name.replace('.', '/').concat(".class"), false);
                    if (res == null) {
                        return null;
                    }
                    try {
                        return URLClassLoader.this.defineClass(name, res);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
            }, this.acc);
            if (result != null) {
                return result;
            }
            throw new ClassNotFoundException(name);
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

    private void definePackageInternal(String pkgname, Manifest man, URL url) {
        if (getAndVerifyPackage(pkgname, man, url) != null) {
            return;
        }
        if (man != null) {
            try {
                definePackage(pkgname, man, url);
            } catch (IllegalArgumentException e) {
                if (getAndVerifyPackage(pkgname, man, url) == null) {
                    throw new AssertionError((Object) "Cannot find package " + pkgname);
                }
            }
        } else {
            definePackage(pkgname, null, null, null, null, null, null, null);
        }
    }

    /* access modifiers changed from: private */
    public Class<?> defineClass(String name, Resource res) throws IOException {
        String str = name;
        long nanoTime = System.nanoTime();
        int i = str.lastIndexOf(46);
        URL url = res.getCodeSourceURL();
        if (i != -1) {
            definePackageInternal(str.substring(0, i), res.getManifest(), url);
        }
        ByteBuffer bb = res.getByteBuffer();
        if (bb != null) {
            return defineClass(str, bb, new CodeSource(url, res.getCodeSigners()));
        }
        byte[] b = res.getBytes();
        return defineClass(str, b, 0, b.length, new CodeSource(url, res.getCodeSigners()));
    }

    /* access modifiers changed from: protected */
    public Package definePackage(String name, Manifest man, URL url) throws IllegalArgumentException {
        String str = name;
        String specTitle = null;
        String specVersion = null;
        String specVendor = null;
        String implTitle = null;
        String implVersion = null;
        String implVendor = null;
        String sealed = null;
        Attributes attr = man.getAttributes(str.replace('.', '/').concat("/"));
        if (attr != null) {
            specTitle = attr.getValue(Attributes.Name.SPECIFICATION_TITLE);
            specVersion = attr.getValue(Attributes.Name.SPECIFICATION_VERSION);
            specVendor = attr.getValue(Attributes.Name.SPECIFICATION_VENDOR);
            implTitle = attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            implVersion = attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            implVendor = attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
            sealed = attr.getValue(Attributes.Name.SEALED);
        }
        Attributes attr2 = man.getMainAttributes();
        if (attr2 != null) {
            if (specTitle == null) {
                specTitle = attr2.getValue(Attributes.Name.SPECIFICATION_TITLE);
            }
            if (specVersion == null) {
                specVersion = attr2.getValue(Attributes.Name.SPECIFICATION_VERSION);
            }
            if (specVendor == null) {
                specVendor = attr2.getValue(Attributes.Name.SPECIFICATION_VENDOR);
            }
            if (implTitle == null) {
                implTitle = attr2.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            }
            if (implVersion == null) {
                implVersion = attr2.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            }
            if (implVendor == null) {
                implVendor = attr2.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
            }
            if (sealed == null) {
                sealed = attr2.getValue(Attributes.Name.SEALED);
            }
        }
        return definePackage(str, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, "true".equalsIgnoreCase(sealed) ? url : null);
    }

    private boolean isSealed(String name, Manifest man) {
        Attributes attr = man.getAttributes(name.replace('.', '/').concat("/"));
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Attributes.Name.SEALED);
        }
        if (sealed == null) {
            Attributes mainAttributes = man.getMainAttributes();
            Attributes attr2 = mainAttributes;
            if (mainAttributes != null) {
                sealed = attr2.getValue(Attributes.Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);
    }

    public URL findResource(final String name) {
        URL url = (URL) AccessController.doPrivileged(new PrivilegedAction<URL>() {
            public URL run() {
                return URLClassLoader.this.ucp.findResource(name, true);
            }
        }, this.acc);
        if (url != null) {
            return this.ucp.checkURL(url);
        }
        return null;
    }

    public Enumeration<URL> findResources(String name) throws IOException {
        final Enumeration<URL> e = this.ucp.findResources(name, true);
        return new Enumeration<URL>() {
            private URL url = null;

            private boolean next() {
                boolean z = true;
                if (this.url != null) {
                    return true;
                }
                do {
                    URL u = (URL) AccessController.doPrivileged(new PrivilegedAction<URL>() {
                        public URL run() {
                            if (!e.hasMoreElements()) {
                                return null;
                            }
                            return (URL) e.nextElement();
                        }
                    }, URLClassLoader.this.acc);
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
        };
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: java.net.JarURLConnection} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: java.net.SocketPermission} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.io.FilePermission} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: java.io.FilePermission} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: java.net.JarURLConnection} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: java.net.URLConnection} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: java.net.JarURLConnection} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public PermissionCollection getPermissions(CodeSource codesource) {
        Permission p;
        JarURLConnection jarURLConnection;
        PermissionCollection perms = super.getPermissions(codesource);
        URL url = codesource.getLocation();
        try {
            URLConnection urlConnection = url.openConnection();
            p = urlConnection.getPermission();
            jarURLConnection = urlConnection;
        } catch (IOException e) {
            p = null;
            jarURLConnection = null;
        }
        if (p instanceof FilePermission) {
            if (p.getName().endsWith(File.separator)) {
                p = new FilePermission(path + LanguageTag.SEP, "read");
            }
        } else if (p != null || !url.getProtocol().equals("file")) {
            URL locUrl = url;
            if (jarURLConnection instanceof JarURLConnection) {
                locUrl = jarURLConnection.getJarFileURL();
            }
            String host = locUrl.getHost();
            if (host != null && host.length() > 0) {
                p = new SocketPermission(host, SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION);
            }
        } else {
            String path = ParseUtil.decode(url.getFile().replace('/', File.separatorChar));
            if (path.endsWith(File.separator)) {
                path = path + LanguageTag.SEP;
            }
            p = new FilePermission(path, "read");
        }
        if (p != null) {
            final SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                final Permission fp = p;
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() throws SecurityException {
                        sm.checkPermission(fp);
                        return null;
                    }
                }, this.acc);
            }
            perms.add(p);
        }
        return perms;
    }

    public static URLClassLoader newInstance(final URL[] urls, final ClassLoader parent) {
        final AccessControlContext acc2 = AccessController.getContext();
        return (URLClassLoader) AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
            public URLClassLoader run() {
                return new FactoryURLClassLoader(urls, parent, acc2);
            }
        });
    }

    public static URLClassLoader newInstance(final URL[] urls) {
        final AccessControlContext acc2 = AccessController.getContext();
        return (URLClassLoader) AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
            public URLClassLoader run() {
                return new FactoryURLClassLoader(urls, acc2);
            }
        });
    }

    static {
        ClassLoader.registerAsParallelCapable();
    }
}
