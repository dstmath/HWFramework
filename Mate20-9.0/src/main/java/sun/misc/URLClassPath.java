package sun.misc;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.net.util.URLUtil;
import sun.net.www.ParseUtil;
import sun.security.action.GetPropertyAction;
import sun.security.util.SecurityConstants;

public class URLClassPath {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (AccessController.doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.debug")) != null);
    private static final boolean DEBUG_LOOKUP_CACHE = (AccessController.doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.debugLookupCache")) != null);
    private static final boolean DISABLE_ACC_CHECKING;
    /* access modifiers changed from: private */
    public static final boolean DISABLE_JAR_CHECKING;
    static final String JAVA_VERSION = ((String) AccessController.doPrivileged(new GetPropertyAction("java.version")));
    static final String USER_AGENT_JAVA_VERSION = "UA-Java-Version";
    private static volatile boolean lookupCacheEnabled = false;
    /* access modifiers changed from: private */
    public final AccessControlContext acc;
    private boolean closed;
    /* access modifiers changed from: private */
    public URLStreamHandler jarHandler;
    HashMap<String, Loader> lmap;
    ArrayList<Loader> loaders;
    private ClassLoader lookupCacheLoader;
    private URL[] lookupCacheURLs;
    private ArrayList<URL> path;
    Stack<URL> urls;

    private static class FileLoader extends Loader {
        private File dir;

        FileLoader(URL url) throws IOException {
            super(url);
            if ("file".equals(url.getProtocol())) {
                this.dir = new File(ParseUtil.decode(url.getFile().replace('/', File.separatorChar))).getCanonicalFile();
                return;
            }
            throw new IllegalArgumentException("url");
        }

        /* access modifiers changed from: package-private */
        public URL findResource(String name, boolean check) {
            Resource rsc = getResource(name, check);
            if (rsc != null) {
                return rsc.getURL();
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public Resource getResource(final String name, boolean check) {
            final File file;
            try {
                URL normalizedBase = new URL(getBaseURL(), ".");
                final URL url = new URL(getBaseURL(), ParseUtil.encodePath(name, false));
                if (!url.getFile().startsWith(normalizedBase.getFile())) {
                    return null;
                }
                if (check) {
                    URLClassPath.check(url);
                }
                if (name.indexOf("..") != -1) {
                    file = new File(this.dir, name.replace('/', File.separatorChar)).getCanonicalFile();
                    if (!file.getPath().startsWith(this.dir.getPath())) {
                        return null;
                    }
                } else {
                    file = new File(this.dir, name.replace('/', File.separatorChar));
                }
                if (file.exists()) {
                    return new Resource() {
                        public String getName() {
                            return name;
                        }

                        public URL getURL() {
                            return url;
                        }

                        public URL getCodeSourceURL() {
                            return FileLoader.this.getBaseURL();
                        }

                        public InputStream getInputStream() throws IOException {
                            return new FileInputStream(file);
                        }

                        public int getContentLength() throws IOException {
                            return (int) file.length();
                        }
                    };
                }
                URL url2 = url;
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    static class JarLoader extends Loader {
        /* access modifiers changed from: private */
        public final AccessControlContext acc;
        private boolean closed = false;
        /* access modifiers changed from: private */
        public final URL csu;
        /* access modifiers changed from: private */
        public URLStreamHandler handler;
        /* access modifiers changed from: private */
        public JarIndex index;
        /* access modifiers changed from: private */
        public JarFile jar;
        /* access modifiers changed from: private */
        public final HashMap<String, Loader> lmap;
        /* access modifiers changed from: private */
        public MetaIndex metaIndex;

        /* JADX WARNING: Illegal instructions before constructor call */
        JarLoader(URL url, URLStreamHandler jarHandler, HashMap<String, Loader> loaderMap, AccessControlContext acc2) throws IOException {
            super(r0);
            URL url2 = new URL("jar", "", -1, url + "!/", jarHandler);
            this.csu = url;
            this.handler = jarHandler;
            this.lmap = loaderMap;
            this.acc = acc2;
            if (!isOptimizable(url)) {
                ensureOpen();
                return;
            }
            String fileName = url.getFile();
            if (fileName != null) {
                File f = new File(ParseUtil.decode(fileName));
                this.metaIndex = MetaIndex.forJar(f);
                if (this.metaIndex != null && !f.exists()) {
                    this.metaIndex = null;
                }
            }
            if (this.metaIndex == null) {
                ensureOpen();
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                ensureOpen();
                this.jar.close();
            }
        }

        /* access modifiers changed from: package-private */
        public JarFile getJarFile() {
            return this.jar;
        }

        private boolean isOptimizable(URL url) {
            return "file".equals(url.getProtocol());
        }

        private void ensureOpen() throws IOException {
            if (this.jar == null) {
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                        public Void run() throws IOException {
                            if (URLClassPath.DEBUG) {
                                PrintStream printStream = System.err;
                                printStream.println("Opening " + JarLoader.this.csu);
                                Thread.dumpStack();
                            }
                            JarFile unused = JarLoader.this.jar = JarLoader.this.getJarFile(JarLoader.this.csu);
                            JarIndex unused2 = JarLoader.this.index = JarIndex.getJarIndex(JarLoader.this.jar, JarLoader.this.metaIndex);
                            if (JarLoader.this.index != null) {
                                String[] jarfiles = JarLoader.this.index.getJarFiles();
                                for (int i = 0; i < jarfiles.length; i++) {
                                    try {
                                        String urlNoFragString = URLUtil.urlNoFragString(new URL(JarLoader.this.csu, jarfiles[i]));
                                        if (!JarLoader.this.lmap.containsKey(urlNoFragString)) {
                                            JarLoader.this.lmap.put(urlNoFragString, null);
                                        }
                                    } catch (MalformedURLException e) {
                                    }
                                }
                            }
                            return null;
                        }
                    }, this.acc);
                } catch (PrivilegedActionException pae) {
                    throw ((IOException) pae.getException());
                }
            }
        }

        static JarFile checkJar(JarFile jar2) throws IOException {
            if (System.getSecurityManager() == null || URLClassPath.DISABLE_JAR_CHECKING || jar2.startsWithLocHeader()) {
                return jar2;
            }
            IOException x = new IOException("Invalid Jar file");
            try {
                jar2.close();
            } catch (IOException ex) {
                x.addSuppressed(ex);
            }
            throw x;
        }

        /* access modifiers changed from: private */
        public JarFile getJarFile(URL url) throws IOException {
            if (isOptimizable(url)) {
                FileURLMapper p = new FileURLMapper(url);
                if (p.exists()) {
                    return checkJar(new JarFile(p.getPath()));
                }
                throw new FileNotFoundException(p.getPath());
            }
            URLConnection uc = getBaseURL().openConnection();
            uc.setRequestProperty(URLClassPath.USER_AGENT_JAVA_VERSION, URLClassPath.JAVA_VERSION);
            return checkJar(((JarURLConnection) uc).getJarFile());
        }

        /* access modifiers changed from: package-private */
        public JarIndex getIndex() {
            try {
                ensureOpen();
                return this.index;
            } catch (IOException e) {
                throw new InternalError((Throwable) e);
            }
        }

        /* access modifiers changed from: package-private */
        public Resource checkResource(final String name, boolean check, final JarEntry entry) {
            try {
                final URL url = new URL(getBaseURL(), ParseUtil.encodePath(name, false));
                if (check) {
                    URLClassPath.check(url);
                }
                return new Resource() {
                    public String getName() {
                        return name;
                    }

                    public URL getURL() {
                        return url;
                    }

                    public URL getCodeSourceURL() {
                        return JarLoader.this.csu;
                    }

                    public InputStream getInputStream() throws IOException {
                        return JarLoader.this.jar.getInputStream(entry);
                    }

                    public int getContentLength() {
                        return (int) entry.getSize();
                    }

                    public Manifest getManifest() throws IOException {
                        return JarLoader.this.jar.getManifest();
                    }

                    public Certificate[] getCertificates() {
                        return entry.getCertificates();
                    }

                    public CodeSigner[] getCodeSigners() {
                        return entry.getCodeSigners();
                    }
                };
            } catch (MalformedURLException e) {
                return null;
            } catch (IOException e2) {
                return null;
            } catch (AccessControlException e3) {
                return null;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean validIndex(String name) {
            String packageName = name;
            int lastIndexOf = name.lastIndexOf("/");
            int pos = lastIndexOf;
            if (lastIndexOf != -1) {
                packageName = name.substring(0, pos);
            }
            Enumeration<JarEntry> enum_ = this.jar.entries();
            while (enum_.hasMoreElements()) {
                String entryName = enum_.nextElement().getName();
                int lastIndexOf2 = entryName.lastIndexOf("/");
                int pos2 = lastIndexOf2;
                if (lastIndexOf2 != -1) {
                    entryName = entryName.substring(0, pos2);
                }
                if (entryName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public URL findResource(String name, boolean check) {
            Resource rsc = getResource(name, check);
            if (rsc != null) {
                return rsc.getURL();
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public Resource getResource(String name, boolean check) {
            if (this.metaIndex != null && !this.metaIndex.mayContain(name)) {
                return null;
            }
            try {
                ensureOpen();
                JarEntry entry = this.jar.getJarEntry(name);
                if (entry != null) {
                    return checkResource(name, check, entry);
                }
                if (this.index == null) {
                    return null;
                }
                return getResource(name, check, new HashSet<>());
            } catch (IOException e) {
                throw new InternalError((Throwable) e);
            }
        }

        /* access modifiers changed from: package-private */
        public Resource getResource(String name, boolean check, Set<String> visited) {
            LinkedList<String> jarFilesList;
            String str;
            String str2 = name;
            boolean z = check;
            Set<String> set = visited;
            int count = 0;
            LinkedList<String> linkedList = this.index.get(str2);
            LinkedList<String> jarFilesList2 = linkedList;
            if (linkedList == null) {
                return null;
            }
            while (true) {
                int size = jarFilesList2.size();
                String[] jarFiles = (String[]) jarFilesList2.toArray(new String[size]);
                while (count < size) {
                    int count2 = count + 1;
                    String jarName = jarFiles[count];
                    try {
                        final URL url = new URL(this.csu, jarName);
                        String urlNoFragString = URLUtil.urlNoFragString(url);
                        JarLoader jarLoader = (JarLoader) this.lmap.get(urlNoFragString);
                        JarLoader newLoader = jarLoader;
                        if (jarLoader == null) {
                            newLoader = (JarLoader) AccessController.doPrivileged(new PrivilegedExceptionAction<JarLoader>() {
                                public JarLoader run() throws IOException {
                                    return new JarLoader(url, JarLoader.this.handler, JarLoader.this.lmap, JarLoader.this.acc);
                                }
                            }, this.acc);
                            JarIndex newIndex = newLoader.getIndex();
                            if (newIndex != null) {
                                int pos = jarName.lastIndexOf("/");
                                JarIndex jarIndex = this.index;
                                if (pos == -1) {
                                    jarFilesList = jarFilesList2;
                                    str = null;
                                } else {
                                    jarFilesList = jarFilesList2;
                                    try {
                                        str = jarName.substring(0, pos + 1);
                                    } catch (MalformedURLException | PrivilegedActionException e) {
                                    }
                                }
                                newIndex.merge(jarIndex, str);
                            } else {
                                jarFilesList = jarFilesList2;
                            }
                            this.lmap.put(urlNoFragString, newLoader);
                        } else {
                            jarFilesList = jarFilesList2;
                        }
                        JarLoader newLoader2 = newLoader;
                        boolean visitedURL = !set.add(URLUtil.urlNoFragString(url));
                        if (!visitedURL) {
                            try {
                                newLoader2.ensureOpen();
                                JarEntry entry = newLoader2.jar.getJarEntry(str2);
                                if (entry != null) {
                                    return newLoader2.checkResource(str2, z, entry);
                                }
                                if (!newLoader2.validIndex(str2)) {
                                    throw new InvalidJarIndexException("Invalid index");
                                }
                            } catch (IOException e2) {
                                IOException iOException = e2;
                                throw new InternalError((Throwable) e2);
                            }
                        }
                        if (!(visitedURL || newLoader2 == this || newLoader2.getIndex() == null)) {
                            Resource resource = newLoader2.getResource(str2, z, set);
                            Resource res = resource;
                            if (resource != null) {
                                return res;
                            }
                        }
                    } catch (PrivilegedActionException e3) {
                        jarFilesList = jarFilesList2;
                    } catch (MalformedURLException e4) {
                        jarFilesList = jarFilesList2;
                    }
                    count = count2;
                    jarFilesList2 = jarFilesList;
                }
                jarFilesList2 = this.index.get(str2);
                if (count >= jarFilesList2.size()) {
                    return null;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public URL[] getClassPath() throws IOException {
            if (this.index != null || this.metaIndex != null) {
                return null;
            }
            ensureOpen();
            parseExtensionsDependencies();
            if (this.jar.hasClassPathAttribute()) {
                Manifest man = this.jar.getManifest();
                if (man != null) {
                    Attributes attr = man.getMainAttributes();
                    if (attr != null) {
                        String value = attr.getValue(Attributes.Name.CLASS_PATH);
                        if (value != null) {
                            return parseClassPath(this.csu, value);
                        }
                    }
                }
            }
            return null;
        }

        private void parseExtensionsDependencies() throws IOException {
        }

        private URL[] parseClassPath(URL base, String value) throws MalformedURLException {
            StringTokenizer st = new StringTokenizer(value);
            URL[] urls = new URL[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                urls[i] = new URL(base, st.nextToken());
                i++;
            }
            return urls;
        }
    }

    private static class Loader implements Closeable {
        /* access modifiers changed from: private */
        public final URL base;
        private JarFile jarfile;

        Loader(URL url) {
            this.base = url;
        }

        /* access modifiers changed from: package-private */
        public URL getBaseURL() {
            return this.base;
        }

        /* access modifiers changed from: package-private */
        public URL findResource(String name, boolean check) {
            try {
                URL url = new URL(this.base, ParseUtil.encodePath(name, false));
                if (check) {
                    try {
                        URLClassPath.check(url);
                    } catch (Exception e) {
                        return null;
                    }
                }
                URLConnection uc = url.openConnection();
                if (uc instanceof HttpURLConnection) {
                    HttpURLConnection hconn = (HttpURLConnection) uc;
                    hconn.setRequestMethod("HEAD");
                    if (hconn.getResponseCode() >= 400) {
                        return null;
                    }
                } else {
                    uc.setUseCaches(false);
                    uc.getInputStream().close();
                }
                return url;
            } catch (MalformedURLException e2) {
                throw new IllegalArgumentException("name");
            }
        }

        /* access modifiers changed from: package-private */
        public Resource getResource(final String name, boolean check) {
            try {
                final URL url = new URL(this.base, ParseUtil.encodePath(name, false));
                if (check) {
                    try {
                        URLClassPath.check(url);
                    } catch (Exception e) {
                        return null;
                    }
                }
                final URLConnection uc = url.openConnection();
                InputStream inputStream = uc.getInputStream();
                if (uc instanceof JarURLConnection) {
                    this.jarfile = JarLoader.checkJar(((JarURLConnection) uc).getJarFile());
                }
                return new Resource() {
                    public String getName() {
                        return name;
                    }

                    public URL getURL() {
                        return url;
                    }

                    public URL getCodeSourceURL() {
                        return Loader.this.base;
                    }

                    public InputStream getInputStream() throws IOException {
                        return uc.getInputStream();
                    }

                    public int getContentLength() throws IOException {
                        return uc.getContentLength();
                    }
                };
            } catch (MalformedURLException e2) {
                throw new IllegalArgumentException("name");
            }
        }

        /* access modifiers changed from: package-private */
        public Resource getResource(String name) {
            return getResource(name, true);
        }

        public void close() throws IOException {
            if (this.jarfile != null) {
                this.jarfile.close();
            }
        }

        /* access modifiers changed from: package-private */
        public URL[] getClassPath() throws IOException {
            return null;
        }
    }

    static {
        boolean z = true;
        String p = (String) AccessController.doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.disableJarChecking"));
        DISABLE_JAR_CHECKING = p != null && (p.equals("true") || p.equals(""));
        String p2 = (String) AccessController.doPrivileged(new GetPropertyAction("jdk.net.URLClassPath.disableRestrictedPermissions"));
        if (p2 == null || (!p2.equals("true") && !p2.equals(""))) {
            z = false;
        }
        DISABLE_ACC_CHECKING = z;
    }

    public URLClassPath(URL[] urls2, URLStreamHandlerFactory factory, AccessControlContext acc2) {
        this.path = new ArrayList<>();
        this.urls = new Stack<>();
        this.loaders = new ArrayList<>();
        this.lmap = new HashMap<>();
        this.closed = false;
        for (URL add : urls2) {
            this.path.add(add);
        }
        push(urls2);
        if (factory != null) {
            this.jarHandler = factory.createURLStreamHandler("jar");
        }
        if (DISABLE_ACC_CHECKING) {
            this.acc = null;
        } else {
            this.acc = acc2;
        }
    }

    public URLClassPath(URL[] urls2) {
        this(urls2, null, null);
    }

    public URLClassPath(URL[] urls2, AccessControlContext acc2) {
        this(urls2, null, acc2);
    }

    public synchronized List<IOException> closeLoaders() {
        if (this.closed) {
            return Collections.emptyList();
        }
        List<IOException> result = new LinkedList<>();
        Iterator<Loader> it = this.loaders.iterator();
        while (it.hasNext()) {
            try {
                it.next().close();
            } catch (IOException e) {
                result.add(e);
            }
        }
        this.closed = true;
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0029, code lost:
        return;
     */
    public synchronized void addURL(URL url) {
        if (!this.closed) {
            synchronized (this.urls) {
                if (url != null) {
                    if (!this.path.contains(url)) {
                        this.urls.add(0, url);
                        this.path.add(url);
                        if (this.lookupCacheURLs != null) {
                            disableAllLookupCaches();
                        }
                    }
                }
            }
        }
    }

    public URL[] getURLs() {
        URL[] urlArr;
        synchronized (this.urls) {
            urlArr = (URL[]) this.path.toArray(new URL[this.path.size()]);
        }
        return urlArr;
    }

    public URL findResource(String name, boolean check) {
        int[] cache = getLookupCache(name);
        int i = 0;
        while (true) {
            Loader nextLoader = getNextLoader(cache, i);
            Loader loader = nextLoader;
            if (nextLoader == null) {
                return null;
            }
            URL url = loader.findResource(name, check);
            if (url != null) {
                return url;
            }
            i++;
        }
    }

    public Resource getResource(String name, boolean check) {
        if (DEBUG) {
            PrintStream printStream = System.err;
            printStream.println("URLClassPath.getResource(\"" + name + "\")");
        }
        int[] cache = getLookupCache(name);
        int i = 0;
        while (true) {
            Loader nextLoader = getNextLoader(cache, i);
            Loader loader = nextLoader;
            if (nextLoader == null) {
                return null;
            }
            Resource res = loader.getResource(name, check);
            if (res != null) {
                return res;
            }
            i++;
        }
    }

    public Enumeration<URL> findResources(final String name, final boolean check) {
        return new Enumeration<URL>() {
            private int[] cache = URLClassPath.this.getLookupCache(name);
            private int index = 0;
            private URL url = null;

            private boolean next() {
                if (this.url != null) {
                    return true;
                }
                do {
                    URLClassPath uRLClassPath = URLClassPath.this;
                    int[] iArr = this.cache;
                    int i = this.index;
                    this.index = i + 1;
                    Loader access$100 = uRLClassPath.getNextLoader(iArr, i);
                    Loader loader = access$100;
                    if (access$100 == null) {
                        return false;
                    }
                    this.url = loader.findResource(name, check);
                } while (this.url == null);
                return true;
            }

            public boolean hasMoreElements() {
                return next();
            }

            public URL nextElement() {
                if (next()) {
                    URL u = this.url;
                    this.url = null;
                    return u;
                }
                throw new NoSuchElementException();
            }
        };
    }

    public Resource getResource(String name) {
        return getResource(name, true);
    }

    public Enumeration<Resource> getResources(final String name, final boolean check) {
        return new Enumeration<Resource>() {
            private int[] cache = URLClassPath.this.getLookupCache(name);
            private int index = 0;
            private Resource res = null;

            private boolean next() {
                if (this.res != null) {
                    return true;
                }
                do {
                    URLClassPath uRLClassPath = URLClassPath.this;
                    int[] iArr = this.cache;
                    int i = this.index;
                    this.index = i + 1;
                    Loader access$100 = uRLClassPath.getNextLoader(iArr, i);
                    Loader loader = access$100;
                    if (access$100 == null) {
                        return false;
                    }
                    this.res = loader.getResource(name, check);
                } while (this.res == null);
                return true;
            }

            public boolean hasMoreElements() {
                return next();
            }

            public Resource nextElement() {
                if (next()) {
                    Resource r = this.res;
                    this.res = null;
                    return r;
                }
                throw new NoSuchElementException();
            }
        };
    }

    public Enumeration<Resource> getResources(String name) {
        return getResources(name, true);
    }

    /* access modifiers changed from: package-private */
    public synchronized void initLookupCache(ClassLoader loader) {
        URL[] lookupCacheURLs2 = getLookupCacheURLs(loader);
        this.lookupCacheURLs = lookupCacheURLs2;
        if (lookupCacheURLs2 != null) {
            this.lookupCacheLoader = loader;
        } else {
            disableAllLookupCaches();
        }
    }

    static void disableAllLookupCaches() {
        lookupCacheEnabled = false;
    }

    private URL[] getLookupCacheURLs(ClassLoader loader) {
        return null;
    }

    private static int[] getLookupCacheForClassLoader(ClassLoader loader, String name) {
        return null;
    }

    private static boolean knownToNotExist0(ClassLoader loader, String className) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean knownToNotExist(String className) {
        if (this.lookupCacheURLs == null || !lookupCacheEnabled) {
            return false;
        }
        return knownToNotExist0(this.lookupCacheLoader, className);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004a, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004c, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004e, code lost:
        return null;
     */
    public synchronized int[] getLookupCache(String name) {
        if (this.lookupCacheURLs != null) {
            if (lookupCacheEnabled) {
                int[] cache = getLookupCacheForClassLoader(this.lookupCacheLoader, name);
                if (cache != null && cache.length > 0) {
                    int maxindex = cache[cache.length - 1];
                    if (!ensureLoaderOpened(maxindex)) {
                        if (DEBUG_LOOKUP_CACHE) {
                            PrintStream printStream = System.out;
                            printStream.println("Expanded loaders FAILED " + this.loaders.size() + " for maxindex=" + maxindex);
                        }
                    }
                }
            }
        }
    }

    private boolean ensureLoaderOpened(int index) {
        if (this.loaders.size() <= index) {
            if (getLoader(index) == null || !lookupCacheEnabled) {
                return false;
            }
            if (DEBUG_LOOKUP_CACHE) {
                PrintStream printStream = System.out;
                printStream.println("Expanded loaders " + this.loaders.size() + " to index=" + index);
            }
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0040, code lost:
        return;
     */
    private synchronized void validateLookupCache(int index, String urlNoFragString) {
        if (this.lookupCacheURLs != null && lookupCacheEnabled) {
            if (index >= this.lookupCacheURLs.length || !urlNoFragString.equals(URLUtil.urlNoFragString(this.lookupCacheURLs[index]))) {
                if (DEBUG || DEBUG_LOOKUP_CACHE) {
                    PrintStream printStream = System.out;
                    printStream.println("WARNING: resource lookup cache invalidated for lookupCacheLoader at " + index);
                }
                disableAllLookupCaches();
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0040, code lost:
        return r0;
     */
    public synchronized Loader getNextLoader(int[] cache, int index) {
        if (this.closed) {
            return null;
        }
        if (cache == null) {
            return getLoader(index);
        } else if (index >= cache.length) {
            return null;
        } else {
            Loader loader = this.loaders.get(cache[index]);
            if (DEBUG_LOOKUP_CACHE) {
                PrintStream printStream = System.out;
                printStream.println("HASCACHE: Loading from : " + cache[index] + " = " + loader.getBaseURL());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0 = sun.net.util.URLUtil.urlNoFragString(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0033, code lost:
        if (r7.lmap.containsKey(r0) == false) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r3 = getLoader(r2);
        r4 = r3.getClassPath();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003e, code lost:
        if (r4 == null) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0040, code lost:
        push(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        validateLookupCache(r7.loaders.size(), r0);
        r7.loaders.add(r3);
        r7.lmap.put(r0, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0059, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005c, code lost:
        if (DEBUG != false) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005e, code lost:
        r4 = java.lang.System.err;
        r4.println("Failed to access " + r2 + ", " + r3);
     */
    private synchronized Loader getLoader(int index) {
        if (this.closed) {
            return null;
        }
        while (this.loaders.size() < index + 1) {
            synchronized (this.urls) {
                if (this.urls.empty()) {
                    return null;
                }
                URL url = this.urls.pop();
            }
        }
        if (DEBUG_LOOKUP_CACHE) {
            PrintStream printStream = System.out;
            printStream.println("NOCACHE: Loading from : " + index);
        }
        return this.loaders.get(index);
    }

    private Loader getLoader(final URL url) throws IOException {
        try {
            return (Loader) AccessController.doPrivileged(new PrivilegedExceptionAction<Loader>() {
                public Loader run() throws IOException {
                    String file = url.getFile();
                    if (file == null || !file.endsWith("/")) {
                        return new JarLoader(url, URLClassPath.this.jarHandler, URLClassPath.this.lmap, URLClassPath.this.acc);
                    }
                    if ("file".equals(url.getProtocol())) {
                        return new FileLoader(url);
                    }
                    return new Loader(url);
                }
            }, this.acc);
        } catch (PrivilegedActionException pae) {
            throw ((IOException) pae.getException());
        }
    }

    private void push(URL[] us) {
        synchronized (this.urls) {
            for (int i = us.length - 1; i >= 0; i--) {
                this.urls.push(us[i]);
            }
        }
    }

    public static URL[] pathToURLs(String path2) {
        StringTokenizer st = new StringTokenizer(path2, File.pathSeparator);
        URL[] urls2 = new URL[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            File f = new File(st.nextToken());
            try {
                f = new File(f.getCanonicalPath());
            } catch (IOException e) {
            }
            int count2 = count + 1;
            try {
                urls2[count] = ParseUtil.fileToEncodedURL(f);
            } catch (IOException e2) {
            }
            count = count2;
        }
        if (urls2.length == count) {
            return urls2;
        }
        URL[] tmp = new URL[count];
        System.arraycopy((Object) urls2, 0, (Object) tmp, 0, count);
        return tmp;
    }

    public URL checkURL(URL url) {
        try {
            check(url);
            return url;
        } catch (Exception e) {
            return null;
        }
    }

    static void check(URL url) throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            URLConnection urlConnection = url.openConnection();
            Permission perm = urlConnection.getPermission();
            if (perm != null) {
                try {
                    security.checkPermission(perm);
                } catch (SecurityException se) {
                    if ((perm instanceof FilePermission) && perm.getActions().indexOf("read") != -1) {
                        security.checkRead(perm.getName());
                    } else if (!(perm instanceof SocketPermission) || perm.getActions().indexOf(SecurityConstants.SOCKET_CONNECT_ACTION) == -1) {
                        throw se;
                    } else {
                        URL locUrl = url;
                        if (urlConnection instanceof JarURLConnection) {
                            locUrl = ((JarURLConnection) urlConnection).getJarFileURL();
                        }
                        security.checkConnect(locUrl.getHost(), locUrl.getPort());
                    }
                }
            }
        }
    }
}
