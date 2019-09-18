package java.util;

import dalvik.system.VMStack;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import sun.reflect.CallerSensitive;
import sun.util.locale.BaseLocale;
import sun.util.locale.LocaleObjectCache;

public abstract class ResourceBundle {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int INITIAL_CACHE_SIZE = 32;
    private static final ResourceBundle NONEXISTENT_BUNDLE = new ResourceBundle() {
        public Enumeration<String> getKeys() {
            return null;
        }

        /* access modifiers changed from: protected */
        public Object handleGetObject(String key) {
            return null;
        }

        public String toString() {
            return "NONEXISTENT_BUNDLE";
        }
    };
    private static final ConcurrentMap<CacheKey, BundleReference> cacheList = new ConcurrentHashMap(32);
    /* access modifiers changed from: private */
    public static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
    private volatile CacheKey cacheKey;
    private volatile boolean expired;
    private volatile Set<String> keySet;
    private Locale locale = null;
    private String name;
    protected ResourceBundle parent = null;

    private static class BundleReference extends SoftReference<ResourceBundle> implements CacheKeyReference {
        private CacheKey cacheKey;

        BundleReference(ResourceBundle referent, ReferenceQueue<Object> q, CacheKey key) {
            super(referent, q);
            this.cacheKey = key;
        }

        public CacheKey getCacheKey() {
            return this.cacheKey;
        }
    }

    private static class CacheKey implements Cloneable {
        private Throwable cause;
        /* access modifiers changed from: private */
        public volatile long expirationTime;
        private String format;
        private int hashCodeCache;
        /* access modifiers changed from: private */
        public volatile long loadTime;
        private LoaderReference loaderRef;
        private Locale locale;
        private String name;

        CacheKey(String baseName, Locale locale2, ClassLoader loader) {
            this.name = baseName;
            this.locale = locale2;
            if (loader == null) {
                this.loaderRef = null;
            } else {
                this.loaderRef = new LoaderReference(loader, ResourceBundle.referenceQueue, this);
            }
            calculateHashCode();
        }

        /* access modifiers changed from: package-private */
        public String getName() {
            return this.name;
        }

        /* access modifiers changed from: package-private */
        public CacheKey setName(String baseName) {
            if (!this.name.equals(baseName)) {
                this.name = baseName;
                calculateHashCode();
            }
            return this;
        }

        /* access modifiers changed from: package-private */
        public Locale getLocale() {
            return this.locale;
        }

        /* access modifiers changed from: package-private */
        public CacheKey setLocale(Locale locale2) {
            if (!this.locale.equals(locale2)) {
                this.locale = locale2;
                calculateHashCode();
            }
            return this;
        }

        /* access modifiers changed from: package-private */
        public ClassLoader getLoader() {
            if (this.loaderRef != null) {
                return (ClassLoader) this.loaderRef.get();
            }
            return null;
        }

        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            try {
                CacheKey otherEntry = (CacheKey) other;
                if (this.hashCodeCache != otherEntry.hashCodeCache || !this.name.equals(otherEntry.name) || !this.locale.equals(otherEntry.locale)) {
                    return ResourceBundle.$assertionsDisabled;
                }
                if (this.loaderRef == null) {
                    if (otherEntry.loaderRef != null) {
                        z = false;
                    }
                    return z;
                }
                ClassLoader loader = (ClassLoader) this.loaderRef.get();
                if (otherEntry.loaderRef == null || loader == null || loader != otherEntry.loaderRef.get()) {
                    z = false;
                }
                return z;
            } catch (ClassCastException | NullPointerException e) {
                return ResourceBundle.$assertionsDisabled;
            }
        }

        public int hashCode() {
            return this.hashCodeCache;
        }

        private void calculateHashCode() {
            this.hashCodeCache = this.name.hashCode() << 3;
            this.hashCodeCache ^= this.locale.hashCode();
            ClassLoader loader = getLoader();
            if (loader != null) {
                this.hashCodeCache ^= loader.hashCode();
            }
        }

        public Object clone() {
            try {
                CacheKey clone = (CacheKey) super.clone();
                if (this.loaderRef != null) {
                    clone.loaderRef = new LoaderReference((ClassLoader) this.loaderRef.get(), ResourceBundle.referenceQueue, clone);
                }
                clone.cause = null;
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new InternalError((Throwable) e);
            }
        }

        /* access modifiers changed from: package-private */
        public String getFormat() {
            return this.format;
        }

        /* access modifiers changed from: package-private */
        public void setFormat(String format2) {
            this.format = format2;
        }

        /* access modifiers changed from: private */
        public void setCause(Throwable cause2) {
            if (this.cause == null) {
                this.cause = cause2;
            } else if (this.cause instanceof ClassNotFoundException) {
                this.cause = cause2;
            }
        }

        /* access modifiers changed from: private */
        public Throwable getCause() {
            return this.cause;
        }

        public String toString() {
            String l = this.locale.toString();
            if (l.length() == 0) {
                if (this.locale.getVariant().length() != 0) {
                    l = "__" + this.locale.getVariant();
                } else {
                    l = "\"\"";
                }
            }
            return "CacheKey[" + this.name + ", lc=" + l + ", ldr=" + getLoader() + "(format=" + this.format + ")]";
        }
    }

    private interface CacheKeyReference {
        CacheKey getCacheKey();
    }

    public static class Control {
        private static final CandidateListCache CANDIDATES_CACHE = new CandidateListCache();
        public static final List<String> FORMAT_CLASS = Collections.unmodifiableList(Arrays.asList("java.class"));
        public static final List<String> FORMAT_DEFAULT = Collections.unmodifiableList(Arrays.asList("java.class", "java.properties"));
        public static final List<String> FORMAT_PROPERTIES = Collections.unmodifiableList(Arrays.asList("java.properties"));
        /* access modifiers changed from: private */
        public static final Control INSTANCE = new Control();
        public static final long TTL_DONT_CACHE = -1;
        public static final long TTL_NO_EXPIRATION_CONTROL = -2;

        private static class CandidateListCache extends LocaleObjectCache<BaseLocale, List<Locale>> {
            private CandidateListCache() {
            }

            /* access modifiers changed from: protected */
            /* JADX WARNING: Can't fix incorrect switch cases order */
            /* JADX WARNING: Code restructure failed: missing block: B:40:0x009b, code lost:
                if (r2.equals("HK") != false) goto L_0x00a9;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ce, code lost:
                if (r1.equals("Hant") != false) goto L_0x00dc;
             */
            /* JADX WARNING: Removed duplicated region for block: B:46:0x00ad  */
            /* JADX WARNING: Removed duplicated region for block: B:47:0x00b0  */
            public List<Locale> createObject(BaseLocale base) {
                String language = base.getLanguage();
                String script = base.getScript();
                String region = base.getRegion();
                String variant = base.getVariant();
                boolean isNorwegianBokmal = ResourceBundle.$assertionsDisabled;
                boolean isNorwegianNynorsk = ResourceBundle.$assertionsDisabled;
                if (language.equals("no")) {
                    if (!region.equals("NO") || !variant.equals("NY")) {
                        isNorwegianBokmal = true;
                    } else {
                        variant = "";
                        isNorwegianNynorsk = true;
                    }
                }
                if (language.equals("nb") || isNorwegianBokmal) {
                    List<Locale> tmpList = getDefaultList("nb", script, region, variant);
                    List<Locale> bokmalList = new LinkedList<>();
                    for (Locale l : tmpList) {
                        bokmalList.add(l);
                        if (l.getLanguage().length() == 0) {
                            break;
                        }
                        bokmalList.add(Locale.getInstance("no", l.getScript(), l.getCountry(), l.getVariant(), null));
                    }
                    return bokmalList;
                }
                char c = 1;
                if (language.equals("nn") || isNorwegianNynorsk) {
                    List<Locale> nynorskList = getDefaultList("nn", script, region, variant);
                    int idx = nynorskList.size() - 1;
                    int idx2 = idx + 1;
                    nynorskList.add(idx, Locale.getInstance("no", "NO", "NY"));
                    int idx3 = idx2 + 1;
                    nynorskList.add(idx2, Locale.getInstance("no", "NO", ""));
                    int idx4 = idx3 + 1;
                    nynorskList.add(idx3, Locale.getInstance("no", "", ""));
                    return nynorskList;
                }
                if (language.equals("zh")) {
                    if (script.length() != 0 || region.length() <= 0) {
                        if (script.length() > 0 && region.length() == 0) {
                            switch (script.hashCode()) {
                                case 2241694:
                                    if (script.equals("Hans")) {
                                        c = 0;
                                        break;
                                    }
                                case 2241695:
                                    break;
                                default:
                                    c = 65535;
                                    break;
                            }
                            switch (c) {
                                case 0:
                                    region = "CN";
                                    break;
                                case 1:
                                    region = "TW";
                                    break;
                            }
                        }
                    } else {
                        int hashCode = region.hashCode();
                        if (hashCode == 2155) {
                            if (region.equals("CN")) {
                                c = 3;
                                switch (c) {
                                    case 0:
                                    case 1:
                                    case 2:
                                        break;
                                    case 3:
                                    case 4:
                                        break;
                                }
                            }
                        } else if (hashCode != 2307) {
                            if (hashCode == 2466) {
                                if (region.equals("MO")) {
                                    c = 2;
                                    switch (c) {
                                        case 0:
                                        case 1:
                                        case 2:
                                            break;
                                        case 3:
                                        case 4:
                                            break;
                                    }
                                }
                            } else if (hashCode == 2644) {
                                if (region.equals("SG")) {
                                    c = 4;
                                    switch (c) {
                                        case 0:
                                        case 1:
                                        case 2:
                                            break;
                                        case 3:
                                        case 4:
                                            break;
                                    }
                                }
                            } else if (hashCode == 2691 && region.equals("TW")) {
                                c = 0;
                                switch (c) {
                                    case 0:
                                    case 1:
                                    case 2:
                                        script = "Hant";
                                        break;
                                    case 3:
                                    case 4:
                                        script = "Hans";
                                        break;
                                }
                            }
                        }
                        c = 65535;
                        switch (c) {
                            case 0:
                            case 1:
                            case 2:
                                break;
                            case 3:
                            case 4:
                                break;
                        }
                    }
                }
                return getDefaultList(language, script, region, variant);
            }

            private static List<Locale> getDefaultList(String language, String script, String region, String variant) {
                List<String> variants = null;
                if (variant.length() > 0) {
                    variants = new LinkedList<>();
                    int idx = variant.length();
                    while (idx != -1) {
                        variants.add(variant.substring(0, idx));
                        idx = variant.lastIndexOf(95, idx - 1);
                    }
                }
                List<Locale> list = new LinkedList<>();
                if (variants != null) {
                    for (String v : variants) {
                        list.add(Locale.getInstance(language, script, region, v, null));
                    }
                }
                if (region.length() > 0) {
                    list.add(Locale.getInstance(language, script, region, "", null));
                }
                if (script.length() > 0) {
                    list.add(Locale.getInstance(language, script, "", "", null));
                    if (variants != null) {
                        for (String v2 : variants) {
                            list.add(Locale.getInstance(language, "", region, v2, null));
                        }
                    }
                    if (region.length() > 0) {
                        list.add(Locale.getInstance(language, "", region, "", null));
                    }
                }
                if (language.length() > 0) {
                    list.add(Locale.getInstance(language, "", "", "", null));
                }
                list.add(Locale.ROOT);
                return list;
            }
        }

        protected Control() {
        }

        public static final Control getControl(List<String> formats) {
            if (formats.equals(FORMAT_PROPERTIES)) {
                return SingleFormatControl.PROPERTIES_ONLY;
            }
            if (formats.equals(FORMAT_CLASS)) {
                return SingleFormatControl.CLASS_ONLY;
            }
            if (formats.equals(FORMAT_DEFAULT)) {
                return INSTANCE;
            }
            throw new IllegalArgumentException();
        }

        public static final Control getNoFallbackControl(List<String> formats) {
            if (formats.equals(FORMAT_DEFAULT)) {
                return NoFallbackControl.NO_FALLBACK;
            }
            if (formats.equals(FORMAT_PROPERTIES)) {
                return NoFallbackControl.PROPERTIES_ONLY_NO_FALLBACK;
            }
            if (formats.equals(FORMAT_CLASS)) {
                return NoFallbackControl.CLASS_ONLY_NO_FALLBACK;
            }
            throw new IllegalArgumentException();
        }

        public List<String> getFormats(String baseName) {
            if (baseName != null) {
                return FORMAT_DEFAULT;
            }
            throw new NullPointerException();
        }

        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            if (baseName != null) {
                return new ArrayList((Collection) CANDIDATES_CACHE.get(locale.getBaseLocale()));
            }
            throw new NullPointerException();
        }

        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName != null) {
                Locale defaultLocale = Locale.getDefault();
                if (locale.equals(defaultLocale)) {
                    return null;
                }
                return defaultLocale;
            }
            throw new NullPointerException();
        }

        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            ResourceBundle bundle = null;
            if (format.equals("java.class")) {
                try {
                    Class<?> loadClass = loader.loadClass(bundleName);
                    if (ResourceBundle.class.isAssignableFrom(loadClass)) {
                        bundle = (ResourceBundle) loadClass.newInstance();
                    } else {
                        throw new ClassCastException(loadClass.getName() + " cannot be cast to ResourceBundle");
                    }
                } catch (ClassNotFoundException e) {
                }
            } else if (format.equals("java.properties")) {
                final String resourceName = toResourceName0(bundleName, "properties");
                if (resourceName == null) {
                    return null;
                }
                final ClassLoader classLoader = loader;
                final boolean reloadFlag = reload;
                try {
                    InputStream stream = (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                        public InputStream run() throws IOException {
                            if (!reloadFlag) {
                                return classLoader.getResourceAsStream(resourceName);
                            }
                            URL url = classLoader.getResource(resourceName);
                            if (url == null) {
                                return null;
                            }
                            URLConnection connection = url.openConnection();
                            if (connection == null) {
                                return null;
                            }
                            connection.setUseCaches(ResourceBundle.$assertionsDisabled);
                            return connection.getInputStream();
                        }
                    });
                    if (stream != null) {
                        try {
                            bundle = new PropertyResourceBundle((Reader) new InputStreamReader(stream, StandardCharsets.UTF_8));
                        } finally {
                            stream.close();
                        }
                    }
                } catch (PrivilegedActionException e2) {
                    throw ((IOException) e2.getException());
                }
            } else {
                throw new IllegalArgumentException("unknown format: " + format);
            }
            return bundle;
        }

        public long getTimeToLive(String baseName, Locale locale) {
            if (baseName != null && locale != null) {
                return -2;
            }
            throw new NullPointerException();
        }

        public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {
            String str = format;
            if (bundle != null) {
                if (str.equals("java.class") || str.equals("java.properties")) {
                    str = str.substring(5);
                }
                String format2 = str;
                boolean z = ResourceBundle.$assertionsDisabled;
                boolean result = false;
                try {
                    try {
                        String resourceName = toResourceName0(toBundleName(baseName, locale), format2);
                        if (resourceName == null) {
                            return ResourceBundle.$assertionsDisabled;
                        }
                        try {
                            URL url = loader.getResource(resourceName);
                            if (url != null) {
                                long lastModified = 0;
                                URLConnection connection = url.openConnection();
                                if (connection != null) {
                                    connection.setUseCaches(ResourceBundle.$assertionsDisabled);
                                    if (connection instanceof JarURLConnection) {
                                        JarEntry ent = ((JarURLConnection) connection).getJarEntry();
                                        if (ent != null) {
                                            lastModified = ent.getTime();
                                            if (lastModified == -1) {
                                                lastModified = 0;
                                            }
                                        }
                                    } else {
                                        lastModified = connection.getLastModified();
                                    }
                                }
                                if (lastModified >= loadTime) {
                                    z = true;
                                }
                                result = z;
                            }
                        } catch (NullPointerException e) {
                            npe = e;
                            throw npe;
                        } catch (Exception e2) {
                        }
                        return result;
                    } catch (NullPointerException e3) {
                        npe = e3;
                        ClassLoader classLoader = loader;
                        throw npe;
                    } catch (Exception e4) {
                        ClassLoader classLoader2 = loader;
                        return result;
                    }
                } catch (NullPointerException e5) {
                    npe = e5;
                    ClassLoader classLoader3 = loader;
                    throw npe;
                } catch (Exception e6) {
                    ClassLoader classLoader22 = loader;
                    return result;
                }
            } else {
                ClassLoader classLoader4 = loader;
                throw new NullPointerException();
            }
        }

        public String toBundleName(String baseName, Locale locale) {
            if (locale == Locale.ROOT) {
                return baseName;
            }
            String language = locale.getLanguage();
            String script = locale.getScript();
            String country = locale.getCountry();
            String variant = locale.getVariant();
            if (language == "" && country == "" && variant == "") {
                return baseName;
            }
            StringBuilder sb = new StringBuilder(baseName);
            sb.append('_');
            if (script != "") {
                if (variant != "") {
                    sb.append(language);
                    sb.append('_');
                    sb.append(script);
                    sb.append('_');
                    sb.append(country);
                    sb.append('_');
                    sb.append(variant);
                } else if (country != "") {
                    sb.append(language);
                    sb.append('_');
                    sb.append(script);
                    sb.append('_');
                    sb.append(country);
                } else {
                    sb.append(language);
                    sb.append('_');
                    sb.append(script);
                }
            } else if (variant != "") {
                sb.append(language);
                sb.append('_');
                sb.append(country);
                sb.append('_');
                sb.append(variant);
            } else if (country != "") {
                sb.append(language);
                sb.append('_');
                sb.append(country);
            } else {
                sb.append(language);
            }
            return sb.toString();
        }

        public final String toResourceName(String bundleName, String suffix) {
            StringBuilder sb = new StringBuilder(bundleName.length() + 1 + suffix.length());
            sb.append(bundleName.replace('.', '/'));
            sb.append('.');
            sb.append(suffix);
            return sb.toString();
        }

        private String toResourceName0(String bundleName, String suffix) {
            if (bundleName.contains("://")) {
                return null;
            }
            return toResourceName(bundleName, suffix);
        }
    }

    private static class LoaderReference extends WeakReference<ClassLoader> implements CacheKeyReference {
        private CacheKey cacheKey;

        LoaderReference(ClassLoader referent, ReferenceQueue<Object> q, CacheKey key) {
            super(referent, q);
            this.cacheKey = key;
        }

        public CacheKey getCacheKey() {
            return this.cacheKey;
        }
    }

    private static final class NoFallbackControl extends SingleFormatControl {
        /* access modifiers changed from: private */
        public static final Control CLASS_ONLY_NO_FALLBACK = new NoFallbackControl(FORMAT_CLASS);
        /* access modifiers changed from: private */
        public static final Control NO_FALLBACK = new NoFallbackControl(FORMAT_DEFAULT);
        /* access modifiers changed from: private */
        public static final Control PROPERTIES_ONLY_NO_FALLBACK = new NoFallbackControl(FORMAT_PROPERTIES);

        protected NoFallbackControl(List<String> formats) {
            super(formats);
        }

        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName != null && locale != null) {
                return null;
            }
            throw new NullPointerException();
        }
    }

    private static class RBClassLoader extends ClassLoader {
        /* access modifiers changed from: private */
        public static final RBClassLoader INSTANCE = ((RBClassLoader) AccessController.doPrivileged(new PrivilegedAction<RBClassLoader>() {
            public RBClassLoader run() {
                return new RBClassLoader();
            }
        }));
        private static final ClassLoader loader = ClassLoader.getSystemClassLoader();

        private RBClassLoader() {
        }

        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (loader != null) {
                return loader.loadClass(name);
            }
            return Class.forName(name);
        }

        public URL getResource(String name) {
            if (loader != null) {
                return loader.getResource(name);
            }
            return ClassLoader.getSystemResource(name);
        }

        public InputStream getResourceAsStream(String name) {
            if (loader != null) {
                return loader.getResourceAsStream(name);
            }
            return ClassLoader.getSystemResourceAsStream(name);
        }
    }

    private static class SingleFormatControl extends Control {
        /* access modifiers changed from: private */
        public static final Control CLASS_ONLY = new SingleFormatControl(FORMAT_CLASS);
        /* access modifiers changed from: private */
        public static final Control PROPERTIES_ONLY = new SingleFormatControl(FORMAT_PROPERTIES);
        private final List<String> formats;

        protected SingleFormatControl(List<String> formats2) {
            this.formats = formats2;
        }

        public List<String> getFormats(String baseName) {
            if (baseName != null) {
                return this.formats;
            }
            throw new NullPointerException();
        }
    }

    public abstract Enumeration<String> getKeys();

    /* access modifiers changed from: protected */
    public abstract Object handleGetObject(String str);

    public String getBaseBundleName() {
        return this.name;
    }

    public final String getString(String key) {
        return (String) getObject(key);
    }

    public final String[] getStringArray(String key) {
        return (String[]) getObject(key);
    }

    public final Object getObject(String key) {
        Object obj = handleGetObject(key);
        if (obj == null) {
            if (this.parent != null) {
                obj = this.parent.getObject(key);
            }
            if (obj == null) {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + key, getClass().getName(), key);
            }
        }
        return obj;
    }

    public Locale getLocale() {
        return this.locale;
    }

    private static ClassLoader getLoader(ClassLoader cl) {
        if (cl == null) {
            return RBClassLoader.INSTANCE;
        }
        return cl;
    }

    /* access modifiers changed from: protected */
    public void setParent(ResourceBundle parent2) {
        this.parent = parent2;
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName) {
        return getBundleImpl(baseName, Locale.getDefault(), getLoader(VMStack.getCallingClassLoader()), getDefaultControl(baseName));
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Control control) {
        return getBundleImpl(baseName, Locale.getDefault(), getLoader(VMStack.getCallingClassLoader()), control);
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Locale locale2) {
        return getBundleImpl(baseName, locale2, getLoader(VMStack.getCallingClassLoader()), getDefaultControl(baseName));
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Locale targetLocale, Control control) {
        return getBundleImpl(baseName, targetLocale, getLoader(VMStack.getCallingClassLoader()), control);
    }

    public static ResourceBundle getBundle(String baseName, Locale locale2, ClassLoader loader) {
        if (loader != null) {
            return getBundleImpl(baseName, locale2, loader, getDefaultControl(baseName));
        }
        throw new NullPointerException();
    }

    public static ResourceBundle getBundle(String baseName, Locale targetLocale, ClassLoader loader, Control control) {
        if (loader != null && control != null) {
            return getBundleImpl(baseName, targetLocale, loader, control);
        }
        throw new NullPointerException();
    }

    private static Control getDefaultControl(String baseName) {
        return Control.INSTANCE;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v8, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v12, resolved type: java.util.ResourceBundle} */
    /* JADX WARNING: Multi-variable type inference failed */
    private static ResourceBundle getBundleImpl(String baseName, Locale locale2, ClassLoader loader, Control control) {
        ResourceBundle bundle;
        String str = baseName;
        Locale locale3 = locale2;
        Control control2 = control;
        if (locale3 == null || control2 == null) {
            ClassLoader classLoader = loader;
            throw new NullPointerException();
        }
        CacheKey cacheKey2 = new CacheKey(str, locale3, loader);
        ResourceBundle bundle2 = null;
        BundleReference bundleRef = cacheList.get(cacheKey2);
        if (bundleRef != null) {
            bundle2 = bundleRef.get();
            bundleRef = null;
        }
        if (isValidBundle(bundle2) && hasValidParentChain(bundle2)) {
            return bundle2;
        }
        boolean isKnownControl = (control2 == Control.INSTANCE || (control2 instanceof SingleFormatControl)) ? true : $assertionsDisabled;
        List<String> formats = control2.getFormats(str);
        if (isKnownControl || checkList(formats)) {
            ResourceBundle bundle3 = bundle2;
            ResourceBundle baseBundle = null;
            Locale targetLocale = locale3;
            while (true) {
                Locale targetLocale2 = targetLocale;
                if (targetLocale2 == null) {
                    break;
                }
                List<Locale> candidateLocales = control2.getCandidateLocales(str, targetLocale2);
                if (isKnownControl || checkList(candidateLocales)) {
                    List<Locale> candidateLocales2 = candidateLocales;
                    Locale targetLocale3 = targetLocale2;
                    bundle = findBundle(cacheKey2, candidateLocales, formats, 0, control2, baseBundle);
                    if (isValidBundle(bundle)) {
                        boolean isBaseBundle = Locale.ROOT.equals(bundle.locale);
                        if (!isBaseBundle || bundle.locale.equals(locale3)) {
                            break;
                        }
                        if (candidateLocales2.size() == 1) {
                            if (bundle.locale.equals(candidateLocales2.get(0))) {
                                break;
                            }
                        }
                        if (isBaseBundle && baseBundle == null) {
                            baseBundle = bundle;
                        }
                    }
                    bundle3 = bundle;
                    targetLocale = control2.getFallbackLocale(str, targetLocale3);
                } else {
                    throw new IllegalArgumentException("Invalid Control: getCandidateLocales");
                }
            }
            bundle3 = bundle;
            if (bundle3 == null) {
                if (baseBundle == null) {
                    throwMissingResourceException(str, locale3, cacheKey2.getCause());
                }
                bundle3 = baseBundle;
            }
            return bundle3;
        }
        throw new IllegalArgumentException("Invalid Control: getFormats");
    }

    private static boolean checkList(List<?> a) {
        boolean valid = a != null && !a.isEmpty();
        if (!valid) {
            return valid;
        }
        int size = a.size();
        boolean valid2 = valid;
        int i = 0;
        while (valid2 && i < size) {
            valid2 = a.get(i) != null;
            i++;
        }
        return valid2;
    }

    private static ResourceBundle findBundle(CacheKey cacheKey2, List<Locale> candidateLocales, List<String> formats, int index, Control control, ResourceBundle baseBundle) {
        Locale targetLocale = candidateLocales.get(index);
        ResourceBundle parent2 = null;
        if (index != candidateLocales.size() - 1) {
            parent2 = findBundle(cacheKey2, candidateLocales, formats, index + 1, control, baseBundle);
        } else if (baseBundle != null && Locale.ROOT.equals(targetLocale)) {
            return baseBundle;
        }
        while (true) {
            Object poll = referenceQueue.poll();
            Object ref = poll;
            if (poll == null) {
                break;
            }
            cacheList.remove(((CacheKeyReference) ref).getCacheKey());
        }
        boolean expiredBundle = $assertionsDisabled;
        cacheKey2.setLocale(targetLocale);
        ResourceBundle bundle = findBundleInCache(cacheKey2, control);
        if (isValidBundle(bundle)) {
            expiredBundle = bundle.expired;
            if (!expiredBundle) {
                if (bundle.parent == parent2) {
                    return bundle;
                }
                BundleReference bundleRef = cacheList.get(cacheKey2);
                if (bundleRef != null && bundleRef.get() == bundle) {
                    cacheList.remove(cacheKey2, bundleRef);
                }
            }
        }
        if (bundle != NONEXISTENT_BUNDLE) {
            CacheKey constKey = (CacheKey) cacheKey2.clone();
            try {
                ResourceBundle bundle2 = loadBundle(cacheKey2, formats, control, expiredBundle);
                if (bundle2 != null) {
                    if (bundle2.parent == null) {
                        bundle2.setParent(parent2);
                    }
                    bundle2.locale = targetLocale;
                    return putBundleInCache(cacheKey2, bundle2, control);
                }
                putBundleInCache(cacheKey2, NONEXISTENT_BUNDLE, control);
                if (constKey.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            } finally {
                if (constKey.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return parent2;
    }

    private static ResourceBundle loadBundle(CacheKey cacheKey2, List<String> formats, Control control, boolean reload) {
        Locale targetLocale = cacheKey2.getLocale();
        int size = formats.size();
        ResourceBundle bundle = null;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= size) {
                break;
            }
            String format = formats.get(i2);
            try {
                bundle = control.newBundle(cacheKey2.getName(), targetLocale, format, cacheKey2.getLoader(), reload);
            } catch (LinkageError error) {
                cacheKey2.setCause(error);
            } catch (Exception cause) {
                cacheKey2.setCause(cause);
            }
            if (bundle != null) {
                cacheKey2.setFormat(format);
                bundle.name = cacheKey2.getName();
                bundle.locale = targetLocale;
                bundle.expired = $assertionsDisabled;
                break;
            }
            i = i2 + 1;
        }
        return bundle;
    }

    private static boolean isValidBundle(ResourceBundle bundle) {
        if (bundle == null || bundle == NONEXISTENT_BUNDLE) {
            return $assertionsDisabled;
        }
        return true;
    }

    private static boolean hasValidParentChain(ResourceBundle bundle) {
        long now = System.currentTimeMillis();
        while (bundle != null) {
            if (bundle.expired) {
                return $assertionsDisabled;
            }
            CacheKey key = bundle.cacheKey;
            if (key != null) {
                long expirationTime = key.expirationTime;
                if (expirationTime >= 0 && expirationTime <= now) {
                    return $assertionsDisabled;
                }
            }
            bundle = bundle.parent;
        }
        return true;
    }

    private static void throwMissingResourceException(String baseName, Locale locale2, Throwable cause) {
        if (cause instanceof MissingResourceException) {
            cause = null;
        }
        throw new MissingResourceException("Can't find bundle for base name " + baseName + ", locale " + locale2, baseName + BaseLocale.SEP + locale2, "", cause);
    }

    private static ResourceBundle findBundleInCache(CacheKey cacheKey2, Control control) {
        CacheKey cacheKey3 = cacheKey2;
        BundleReference bundleRef = cacheList.get(cacheKey3);
        if (bundleRef == null) {
            return null;
        }
        ResourceBundle bundle = (ResourceBundle) bundleRef.get();
        if (bundle == null) {
            return null;
        }
        ResourceBundle p = bundle.parent;
        if (p == null || !p.expired) {
            CacheKey key = bundleRef.getCacheKey();
            long expirationTime = key.expirationTime;
            if (!bundle.expired && expirationTime >= 0 && expirationTime <= System.currentTimeMillis()) {
                if (bundle != NONEXISTENT_BUNDLE) {
                    synchronized (bundle) {
                        try {
                            long expirationTime2 = key.expirationTime;
                            try {
                                if (!bundle.expired && expirationTime2 >= 0 && expirationTime2 <= System.currentTimeMillis()) {
                                    bundle.expired = control.needsReload(key.getName(), key.getLocale(), key.getFormat(), key.getLoader(), bundle, key.loadTime);
                                    if (bundle.expired) {
                                        bundle.cacheKey = null;
                                        cacheList.remove(cacheKey3, bundleRef);
                                    } else {
                                        try {
                                            setExpirationTime(key, control);
                                        } catch (Throwable th) {
                                            th = th;
                                            while (true) {
                                                try {
                                                    break;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                }
                                            }
                                            throw th;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                cacheKey3.setCause(e);
                            } catch (Throwable th3) {
                                th = th3;
                                Control control2 = control;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                            Control control3 = control;
                        } catch (Throwable th4) {
                            th = th4;
                            Control control4 = control;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                } else {
                    Control control5 = control;
                    cacheList.remove(cacheKey3, bundleRef);
                    bundle = null;
                }
                return bundle;
            }
        } else {
            bundle.expired = true;
            bundle.cacheKey = null;
            cacheList.remove(cacheKey3, bundleRef);
            bundle = null;
        }
        Control control6 = control;
        return bundle;
    }

    private static ResourceBundle putBundleInCache(CacheKey cacheKey2, ResourceBundle bundle, Control control) {
        setExpirationTime(cacheKey2, control);
        if (cacheKey2.expirationTime == -1) {
            return bundle;
        }
        CacheKey key = (CacheKey) cacheKey2.clone();
        BundleReference bundleRef = new BundleReference(bundle, referenceQueue, key);
        bundle.cacheKey = key;
        BundleReference result = cacheList.putIfAbsent(key, bundleRef);
        if (result == null) {
            return bundle;
        }
        ResourceBundle rb = (ResourceBundle) result.get();
        if (rb == null || rb.expired) {
            cacheList.put(key, bundleRef);
            return bundle;
        }
        bundle.cacheKey = null;
        ResourceBundle bundle2 = rb;
        bundleRef.clear();
        return bundle2;
    }

    private static void setExpirationTime(CacheKey cacheKey2, Control control) {
        long ttl = control.getTimeToLive(cacheKey2.getName(), cacheKey2.getLocale());
        if (ttl >= 0) {
            long now = System.currentTimeMillis();
            long unused = cacheKey2.loadTime = now;
            long unused2 = cacheKey2.expirationTime = now + ttl;
        } else if (ttl >= -2) {
            long unused3 = cacheKey2.expirationTime = ttl;
        } else {
            throw new IllegalArgumentException("Invalid Control: TTL=" + ttl);
        }
    }

    @CallerSensitive
    public static final void clearCache() {
        clearCache(getLoader(VMStack.getCallingClassLoader()));
    }

    public static final void clearCache(ClassLoader loader) {
        if (loader != null) {
            Set<CacheKey> set = cacheList.keySet();
            for (CacheKey key : set) {
                if (key.getLoader() == loader) {
                    set.remove(key);
                }
            }
            return;
        }
        throw new NullPointerException();
    }

    public boolean containsKey(String key) {
        if (key != null) {
            for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
                if (rb.handleKeySet().contains(key)) {
                    return true;
                }
            }
            return $assertionsDisabled;
        }
        throw new NullPointerException();
    }

    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
            keys.addAll(rb.handleKeySet());
        }
        return keys;
    }

    /* access modifiers changed from: protected */
    public Set<String> handleKeySet() {
        if (this.keySet == null) {
            synchronized (this) {
                if (this.keySet == null) {
                    Set<String> keys = new HashSet<>();
                    Enumeration<String> enumKeys = getKeys();
                    while (enumKeys.hasMoreElements()) {
                        String key = enumKeys.nextElement();
                        if (handleGetObject(key) != null) {
                            keys.add(key);
                        }
                    }
                    this.keySet = keys;
                }
            }
        }
        return this.keySet;
    }
}
