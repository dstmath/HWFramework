package sun.misc;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
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
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import sun.net.util.URLUtil;
import sun.net.www.ParseUtil;
import sun.security.util.SecurityConstants;
import sun.util.logging.PlatformLogger;

public class URLClassPath {
    private static final boolean DEBUG = false;
    private static final boolean DISABLE_JAR_CHECKING = false;
    static final String JAVA_VERSION = null;
    static final String USER_AGENT_JAVA_VERSION = "UA-Java-Version";
    private boolean closed;
    private URLStreamHandler jarHandler;
    HashMap<String, Loader> lmap;
    ArrayList<Loader> loaders;
    private ArrayList<URL> path;
    Stack<URL> urls;

    /* renamed from: sun.misc.URLClassPath.1 */
    class AnonymousClass1 implements Enumeration<URL> {
        private int index;
        private URL url;
        final /* synthetic */ boolean val$check;
        final /* synthetic */ String val$name;

        AnonymousClass1(String val$name, boolean val$check) {
            this.val$name = val$name;
            this.val$check = val$check;
            this.index = 0;
            this.url = null;
        }

        private boolean next() {
            if (this.url != null) {
                return true;
            }
            do {
                URLClassPath uRLClassPath = URLClassPath.this;
                int i = this.index;
                this.index = i + 1;
                Loader loader = uRLClassPath.getLoader(i);
                if (loader == null) {
                    return URLClassPath.DISABLE_JAR_CHECKING;
                }
                this.url = loader.findResource(this.val$name, this.val$check);
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
    }

    /* renamed from: sun.misc.URLClassPath.2 */
    class AnonymousClass2 implements Enumeration<Resource> {
        private int index;
        private Resource res;
        final /* synthetic */ boolean val$check;
        final /* synthetic */ String val$name;

        AnonymousClass2(String val$name, boolean val$check) {
            this.val$name = val$name;
            this.val$check = val$check;
            this.index = 0;
            this.res = null;
        }

        private boolean next() {
            if (this.res != null) {
                return true;
            }
            do {
                URLClassPath uRLClassPath = URLClassPath.this;
                int i = this.index;
                this.index = i + 1;
                Loader loader = uRLClassPath.getLoader(i);
                if (loader == null) {
                    return URLClassPath.DISABLE_JAR_CHECKING;
                }
                this.res = loader.getResource(this.val$name, this.val$check);
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
    }

    /* renamed from: sun.misc.URLClassPath.3 */
    class AnonymousClass3 implements PrivilegedExceptionAction<Loader> {
        final /* synthetic */ URL val$url;

        AnonymousClass3(URL val$url) {
            this.val$url = val$url;
        }

        public Loader run() throws IOException {
            String file = this.val$url.getFile();
            if (file == null || !file.endsWith("/")) {
                return new JarLoader(this.val$url, URLClassPath.this.jarHandler, URLClassPath.this.lmap);
            }
            if ("file".equals(this.val$url.getProtocol())) {
                return new FileLoader(this.val$url);
            }
            return new Loader(this.val$url);
        }
    }

    private static class Loader implements Closeable {
        private final URL base;
        private JarFile jarfile;

        /* renamed from: sun.misc.URLClassPath.Loader.1 */
        class AnonymousClass1 extends Resource {
            final /* synthetic */ String val$name;
            final /* synthetic */ URLConnection val$uc;
            final /* synthetic */ URL val$url;

            AnonymousClass1(String val$name, URL val$url, URLConnection val$uc) {
                this.val$name = val$name;
                this.val$url = val$url;
                this.val$uc = val$uc;
            }

            public String getName() {
                return this.val$name;
            }

            public URL getURL() {
                return this.val$url;
            }

            public URL getCodeSourceURL() {
                return Loader.this.base;
            }

            public InputStream getInputStream() throws IOException {
                return this.val$uc.getInputStream();
            }

            public int getContentLength() throws IOException {
                return this.val$uc.getContentLength();
            }
        }

        Loader(URL url) {
            this.base = url;
        }

        URL getBaseURL() {
            return this.base;
        }

        URL findResource(String name, boolean check) {
            try {
                URL url = new URL(this.base, ParseUtil.encodePath(name, URLClassPath.DISABLE_JAR_CHECKING));
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
                    if (hconn.getResponseCode() >= PlatformLogger.FINER) {
                        return null;
                    }
                }
                url.openStream().close();
                return url;
            } catch (MalformedURLException e2) {
                throw new IllegalArgumentException("name");
            }
        }

        Resource getResource(String name, boolean check) {
            try {
                URL url = new URL(this.base, ParseUtil.encodePath(name, URLClassPath.DISABLE_JAR_CHECKING));
                if (check) {
                    try {
                        URLClassPath.check(url);
                    } catch (Exception e) {
                        return null;
                    }
                }
                URLConnection uc = url.openConnection();
                InputStream in = uc.getInputStream();
                if (uc instanceof JarURLConnection) {
                    this.jarfile = JarLoader.checkJar(((JarURLConnection) uc).getJarFile());
                }
                return new AnonymousClass1(name, url, uc);
            } catch (MalformedURLException e2) {
                throw new IllegalArgumentException("name");
            }
        }

        Resource getResource(String name) {
            return getResource(name, true);
        }

        public void close() throws IOException {
            if (this.jarfile != null) {
                this.jarfile.close();
            }
        }

        URL[] getClassPath() throws IOException {
            return null;
        }
    }

    private static class FileLoader extends Loader {
        private File dir;

        /* renamed from: sun.misc.URLClassPath.FileLoader.1 */
        class AnonymousClass1 extends Resource {
            final /* synthetic */ File val$file;
            final /* synthetic */ String val$name;
            final /* synthetic */ URL val$url;

            AnonymousClass1(String val$name, URL val$url, File val$file) {
                this.val$name = val$name;
                this.val$url = val$url;
                this.val$file = val$file;
            }

            public String getName() {
                return this.val$name;
            }

            public URL getURL() {
                return this.val$url;
            }

            public URL getCodeSourceURL() {
                return FileLoader.this.getBaseURL();
            }

            public InputStream getInputStream() throws IOException {
                return new FileInputStream(this.val$file);
            }

            public int getContentLength() throws IOException {
                return (int) this.val$file.length();
            }
        }

        FileLoader(URL url) throws IOException {
            super(url);
            if ("file".equals(url.getProtocol())) {
                this.dir = new File(ParseUtil.decode(url.getFile().replace('/', File.separatorChar))).getCanonicalFile();
                return;
            }
            throw new IllegalArgumentException("url");
        }

        URL findResource(String name, boolean check) {
            Resource rsc = getResource(name, check);
            if (rsc != null) {
                return rsc.getURL();
            }
            return null;
        }

        Resource getResource(String name, boolean check) {
            try {
                URL normalizedBase = new URL(getBaseURL(), ".");
                URL url = new URL(getBaseURL(), ParseUtil.encodePath(name, URLClassPath.DISABLE_JAR_CHECKING));
                if (!url.getFile().startsWith(normalizedBase.getFile())) {
                    return null;
                }
                File file;
                if (check) {
                    URLClassPath.check(url);
                }
                if (name.indexOf("..") != -1) {
                    file = new File(this.dir, name.replace('/', File.separatorChar)).getCanonicalFile();
                    if (!file.getPath().startsWith(this.dir.getPath())) {
                        return null;
                    }
                }
                file = new File(this.dir, name.replace('/', File.separatorChar));
                if (file.exists()) {
                    return new AnonymousClass1(name, url, file);
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    static class JarLoader extends Loader {
        private boolean closed;
        private URL csu;
        private URLStreamHandler handler;
        private JarIndex index;
        private JarFile jar;
        private HashMap<String, Loader> lmap;
        private MetaIndex metaIndex;

        /* renamed from: sun.misc.URLClassPath.JarLoader.2 */
        class AnonymousClass2 extends Resource {
            final /* synthetic */ JarEntry val$entry;
            final /* synthetic */ String val$name;
            final /* synthetic */ URL val$url;

            AnonymousClass2(String val$name, URL val$url, JarEntry val$entry) {
                this.val$name = val$name;
                this.val$url = val$url;
                this.val$entry = val$entry;
            }

            public String getName() {
                return this.val$name;
            }

            public URL getURL() {
                return this.val$url;
            }

            public URL getCodeSourceURL() {
                return JarLoader.this.csu;
            }

            public InputStream getInputStream() throws IOException {
                return JarLoader.this.jar.getInputStream(this.val$entry);
            }

            public int getContentLength() {
                return (int) this.val$entry.getSize();
            }

            public Manifest getManifest() throws IOException {
                return JarLoader.this.jar.getManifest();
            }

            public Certificate[] getCertificates() {
                return this.val$entry.getCertificates();
            }

            public CodeSigner[] getCodeSigners() {
                return this.val$entry.getCodeSigners();
            }
        }

        /* renamed from: sun.misc.URLClassPath.JarLoader.3 */
        class AnonymousClass3 implements PrivilegedExceptionAction<JarLoader> {
            final /* synthetic */ URL val$url;

            AnonymousClass3(URL val$url) {
                this.val$url = val$url;
            }

            public JarLoader run() throws IOException {
                return new JarLoader(this.val$url, JarLoader.this.handler, JarLoader.this.lmap);
            }
        }

        JarLoader(URL url, URLStreamHandler jarHandler, HashMap<String, Loader> loaderMap) throws IOException {
            super(new URL("jar", "", -1, url + "!/", jarHandler));
            this.closed = URLClassPath.DISABLE_JAR_CHECKING;
            this.csu = url;
            this.handler = jarHandler;
            this.lmap = loaderMap;
            if (isOptimizable(url)) {
                String fileName = url.getFile();
                if (fileName != null) {
                    File f = new File(ParseUtil.decode(fileName));
                    this.metaIndex = MetaIndex.forJar(f);
                    if (!(this.metaIndex == null || f.exists())) {
                        this.metaIndex = null;
                    }
                }
                if (this.metaIndex == null) {
                    ensureOpen();
                    return;
                }
                return;
            }
            ensureOpen();
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                ensureOpen();
                this.jar.close();
            }
        }

        JarFile getJarFile() {
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
                                System.err.println("Opening " + JarLoader.this.csu);
                                Thread.dumpStack();
                            }
                            JarLoader.this.jar = JarLoader.this.getJarFile(JarLoader.this.csu);
                            JarLoader.this.index = JarIndex.getJarIndex(JarLoader.this.jar, JarLoader.this.metaIndex);
                            if (JarLoader.this.index != null) {
                                String[] jarfiles = JarLoader.this.index.getJarFiles();
                                for (String url : jarfiles) {
                                    try {
                                        String urlNoFragString = URLUtil.urlNoFragString(new URL(JarLoader.this.csu, url));
                                        if (!JarLoader.this.lmap.containsKey(urlNoFragString)) {
                                            JarLoader.this.lmap.put(urlNoFragString, null);
                                        }
                                    } catch (MalformedURLException e) {
                                    }
                                }
                            }
                            return null;
                        }
                    });
                } catch (PrivilegedActionException pae) {
                    throw ((IOException) pae.getException());
                }
            }
        }

        static JarFile checkJar(JarFile jar) throws IOException {
            if (System.getSecurityManager() == null || URLClassPath.DISABLE_JAR_CHECKING || jar.startsWithLocHeader()) {
                return jar;
            }
            IOException x = new IOException("Invalid Jar file");
            try {
                jar.close();
            } catch (IOException ex) {
                x.addSuppressed(ex);
            }
            throw x;
        }

        private JarFile getJarFile(URL url) throws IOException {
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

        JarIndex getIndex() {
            try {
                ensureOpen();
                return this.index;
            } catch (IOException e) {
                throw ((InternalError) new InternalError().initCause(e));
            }
        }

        Resource checkResource(String name, boolean check, JarEntry entry) {
            try {
                URL url = new URL(getBaseURL(), ParseUtil.encodePath(name, URLClassPath.DISABLE_JAR_CHECKING));
                if (check) {
                    URLClassPath.check(url);
                }
                return new AnonymousClass2(name, url, entry);
            } catch (MalformedURLException e) {
                return null;
            } catch (IOException e2) {
                return null;
            } catch (AccessControlException e3) {
                return null;
            }
        }

        boolean validIndex(String name) {
            String packageName = name;
            int pos = name.lastIndexOf("/");
            if (pos != -1) {
                packageName = name.substring(0, pos);
            }
            Enumeration<JarEntry> enum_ = this.jar.entries();
            while (enum_.hasMoreElements()) {
                String entryName = ((ZipEntry) enum_.nextElement()).getName();
                pos = entryName.lastIndexOf("/");
                if (pos != -1) {
                    entryName = entryName.substring(0, pos);
                }
                if (entryName.equals(packageName)) {
                    return true;
                }
            }
            return URLClassPath.DISABLE_JAR_CHECKING;
        }

        URL findResource(String name, boolean check) {
            Resource rsc = getResource(name, check);
            if (rsc != null) {
                return rsc.getURL();
            }
            return null;
        }

        Resource getResource(String name, boolean check) {
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
                return getResource(name, check, new HashSet());
            } catch (IOException e) {
                throw ((InternalError) new InternalError().initCause(e));
            }
        }

        Resource getResource(String name, boolean check, Set<String> visited) {
            int count = 0;
            LinkedList jarFilesList = this.index.get(name);
            if (jarFilesList == null) {
                return null;
            }
            loop0:
            while (true) {
                Object[] jarFiles = jarFilesList.toArray();
                int size = jarFilesList.size();
                int count2 = count;
                while (count2 < size) {
                    count = count2 + 1;
                    String jarName = jarFiles[count2];
                    try {
                        URL url = new URL(this.csu, jarName);
                        String urlNoFragString = URLUtil.urlNoFragString(url);
                        JarLoader newLoader = (JarLoader) this.lmap.get(urlNoFragString);
                        if (newLoader == null) {
                            newLoader = (JarLoader) AccessController.doPrivileged(new AnonymousClass3(url));
                            JarIndex newIndex = newLoader.getIndex();
                            if (newIndex != null) {
                                int pos = jarName.lastIndexOf("/");
                                newIndex.merge(this.index, pos == -1 ? null : jarName.substring(0, pos + 1));
                            }
                            this.lmap.put(urlNoFragString, newLoader);
                        }
                        boolean visitedURL = visited.add(URLUtil.urlNoFragString(url)) ? URLClassPath.DISABLE_JAR_CHECKING : true;
                        if (!visitedURL) {
                            try {
                                newLoader.ensureOpen();
                                JarEntry entry = newLoader.jar.getJarEntry(name);
                                if (entry == null) {
                                    if (!newLoader.validIndex(name)) {
                                        break loop0;
                                    }
                                }
                                return newLoader.checkResource(name, check, entry);
                            } catch (IOException e) {
                                throw ((InternalError) new InternalError().initCause(e));
                            }
                        }
                        if (visitedURL || newLoader == this || newLoader.getIndex() == null) {
                            count2 = count;
                        } else {
                            Resource res = newLoader.getResource(name, check, visited);
                            if (res != null) {
                                return res;
                            }
                            count2 = count;
                        }
                    } catch (PrivilegedActionException e2) {
                        count2 = count;
                    } catch (MalformedURLException e3) {
                        count2 = count;
                    }
                }
                jarFilesList = this.index.get(name);
                if (count2 >= jarFilesList.size()) {
                    return null;
                }
                count = count2;
            }
            throw new InvalidJarIndexException("Invalid index");
        }

        URL[] getClassPath() throws IOException {
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
                        String value = attr.getValue(Name.CLASS_PATH);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.URLClassPath.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.URLClassPath.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.URLClassPath.<clinit>():void");
    }

    public URLClassPath(URL[] urls, URLStreamHandlerFactory factory) {
        this.path = new ArrayList();
        this.urls = new Stack();
        this.loaders = new ArrayList();
        this.lmap = new HashMap();
        this.closed = DISABLE_JAR_CHECKING;
        for (Object add : urls) {
            this.path.add(add);
        }
        push(urls);
        if (factory != null) {
            this.jarHandler = factory.createURLStreamHandler("jar");
        }
    }

    public URLClassPath(URL[] urls) {
        this(urls, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized List<IOException> closeLoaders() {
        if (this.closed) {
            return Collections.emptyList();
        }
        List<IOException> result = new LinkedList();
        for (Loader loader : this.loaders) {
            try {
                loader.close();
            } catch (IOException e) {
                result.add(e);
            }
        }
        this.closed = true;
        return result;
    }

    public synchronized void addURL(URL url) {
        if (!this.closed) {
            synchronized (this.urls) {
                if (url != null) {
                    if (!this.path.contains(url)) {
                        this.urls.add(0, url);
                        this.path.add(url);
                        return;
                    }
                }
                return;
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
        int i = 0;
        while (true) {
            Loader loader = getLoader(i);
            if (loader == null) {
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
            System.err.println("URLClassPath.getResource(\"" + name + "\")");
        }
        int i = 0;
        while (true) {
            Loader loader = getLoader(i);
            if (loader == null) {
                return null;
            }
            Resource res = loader.getResource(name, check);
            if (res != null) {
                return res;
            }
            i++;
        }
    }

    public Enumeration<URL> findResources(String name, boolean check) {
        return new AnonymousClass1(name, check);
    }

    public Resource getResource(String name) {
        return getResource(name, true);
    }

    public Enumeration<Resource> getResources(String name, boolean check) {
        return new AnonymousClass2(name, check);
    }

    public Enumeration<Resource> getResources(String name) {
        return getResources(name, true);
    }

    private synchronized Loader getLoader(int index) {
        if (this.closed) {
            return null;
        }
        while (this.loaders.size() < index + 1) {
            synchronized (this.urls) {
                if (this.urls.empty()) {
                    return null;
                }
                URL url = (URL) this.urls.pop();
                String urlNoFragString = URLUtil.urlNoFragString(url);
                if (!this.lmap.containsKey(urlNoFragString)) {
                    try {
                        Loader loader = getLoader(url);
                        URL[] urls = loader.getClassPath();
                        if (urls != null) {
                            push(urls);
                        }
                        this.loaders.add(loader);
                        this.lmap.put(urlNoFragString, loader);
                    } catch (IOException e) {
                    }
                }
            }
        }
        return (Loader) this.loaders.get(index);
    }

    private Loader getLoader(URL url) throws IOException {
        try {
            return (Loader) AccessController.doPrivileged(new AnonymousClass3(url));
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

    public static URL[] pathToURLs(String path) {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        Object urls = new URL[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            File f = new File(st.nextToken());
            try {
                f = new File(f.getCanonicalPath());
            } catch (IOException e) {
            }
            int count2 = count + 1;
            try {
                urls[count] = ParseUtil.fileToEncodedURL(f);
            } catch (IOException e2) {
            }
            count = count2;
        }
        if (urls.length == count) {
            return urls;
        }
        Object tmp = new URL[count];
        System.arraycopy(urls, 0, tmp, 0, count);
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
                    if ((perm instanceof FilePermission) && perm.getActions().indexOf(SecurityConstants.PROPERTY_READ_ACTION) != -1) {
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
