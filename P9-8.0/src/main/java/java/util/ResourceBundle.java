package java.util;

import dalvik.system.VMStack;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    static final /* synthetic */ boolean -assertionsDisabled = (ResourceBundle.class.desiredAssertionStatus() ^ 1);
    private static final int INITIAL_CACHE_SIZE = 32;
    private static final ResourceBundle NONEXISTENT_BUNDLE = new ResourceBundle() {
        public Enumeration<String> getKeys() {
            return null;
        }

        protected Object handleGetObject(String key) {
            return null;
        }

        public String toString() {
            return "NONEXISTENT_BUNDLE";
        }
    };
    private static final ConcurrentMap<CacheKey, BundleReference> cacheList = new ConcurrentHashMap(32);
    private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue();
    private volatile CacheKey cacheKey;
    private volatile boolean expired;
    private volatile Set<String> keySet;
    private Locale locale = null;
    private String name;
    protected ResourceBundle parent = null;

    private interface CacheKeyReference {
        CacheKey getCacheKey();
    }

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
        private volatile long expirationTime;
        private String format;
        private int hashCodeCache;
        private volatile long loadTime;
        private LoaderReference loaderRef;
        private Locale locale;
        private String name;

        CacheKey(String baseName, Locale locale, ClassLoader loader) {
            this.name = baseName;
            this.locale = locale;
            if (loader == null) {
                this.loaderRef = null;
            } else {
                this.loaderRef = new LoaderReference(loader, ResourceBundle.referenceQueue, this);
            }
            calculateHashCode();
        }

        String getName() {
            return this.name;
        }

        CacheKey setName(String baseName) {
            if (!this.name.equals(baseName)) {
                this.name = baseName;
                calculateHashCode();
            }
            return this;
        }

        Locale getLocale() {
            return this.locale;
        }

        CacheKey setLocale(Locale locale) {
            if (!this.locale.equals(locale)) {
                this.locale = locale;
                calculateHashCode();
            }
            return this;
        }

        ClassLoader getLoader() {
            return this.loaderRef != null ? (ClassLoader) this.loaderRef.get() : null;
        }

        /* JADX WARNING: Removed duplicated region for block: B:27:0x004c A:{Splitter: B:3:0x0005, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
        /* JADX WARNING: Missing block: B:28:0x004d, code:
            return java.util.ResourceBundle.-assertionsDisabled;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            try {
                CacheKey otherEntry = (CacheKey) other;
                if (this.hashCodeCache != otherEntry.hashCodeCache || !this.name.equals(otherEntry.name) || !this.locale.equals(otherEntry.locale)) {
                    return ResourceBundle.-assertionsDisabled;
                }
                if (this.loaderRef == null) {
                    if (otherEntry.loaderRef != null) {
                        z = ResourceBundle.-assertionsDisabled;
                    }
                    return z;
                }
                ClassLoader loader = (ClassLoader) this.loaderRef.get();
                if (otherEntry.loaderRef == null || loader == null) {
                    z = ResourceBundle.-assertionsDisabled;
                } else if (loader != otherEntry.loaderRef.get()) {
                    z = ResourceBundle.-assertionsDisabled;
                }
                return z;
            } catch (NullPointerException e) {
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
            } catch (Throwable e) {
                throw new InternalError(e);
            }
        }

        String getFormat() {
            return this.format;
        }

        void setFormat(String format) {
            this.format = format;
        }

        private void setCause(Throwable cause) {
            if (this.cause == null) {
                this.cause = cause;
            } else if (this.cause instanceof ClassNotFoundException) {
                this.cause = cause;
            }
        }

        private Throwable getCause() {
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

    public static class Control {
        private static final CandidateListCache CANDIDATES_CACHE = new CandidateListCache();
        public static final List<String> FORMAT_CLASS = Collections.unmodifiableList(Arrays.asList("java.class"));
        public static final List<String> FORMAT_DEFAULT = Collections.unmodifiableList(Arrays.asList("java.class", "java.properties"));
        public static final List<String> FORMAT_PROPERTIES = Collections.unmodifiableList(Arrays.asList("java.properties"));
        private static final Control INSTANCE = new Control();
        public static final long TTL_DONT_CACHE = -1;
        public static final long TTL_NO_EXPIRATION_CONTROL = -2;

        private static class CandidateListCache extends LocaleObjectCache<BaseLocale, List<Locale>> {
            /* synthetic */ CandidateListCache(CandidateListCache -this0) {
                this();
            }

            private CandidateListCache() {
            }

            protected List<Locale> createObject(BaseLocale base) {
                String language = base.getLanguage();
                String script = base.getScript();
                String region = base.getRegion();
                String variant = base.getVariant();
                boolean isNorwegianBokmal = ResourceBundle.-assertionsDisabled;
                boolean isNorwegianNynorsk = ResourceBundle.-assertionsDisabled;
                if (language.equals("no")) {
                    if (region.equals("NO") && variant.equals("NY")) {
                        variant = "";
                        isNorwegianNynorsk = true;
                    } else {
                        isNorwegianBokmal = true;
                    }
                }
                if (language.equals("nb") || isNorwegianBokmal) {
                    List<Locale> tmpList = getDefaultList("nb", script, region, variant);
                    List<Locale> bokmalList = new LinkedList();
                    for (Locale l : tmpList) {
                        bokmalList.add(l);
                        if (l.getLanguage().length() == 0) {
                            break;
                        }
                        bokmalList.add(Locale.getInstance("no", l.getScript(), l.getCountry(), l.getVariant(), null));
                    }
                    return bokmalList;
                } else if (language.equals("nn") || isNorwegianNynorsk) {
                    List<Locale> nynorskList = getDefaultList("nn", script, region, variant);
                    int size = nynorskList.size() - 1;
                    int i = size + 1;
                    nynorskList.add(size, Locale.getInstance("no", "NO", "NY"));
                    size = i + 1;
                    nynorskList.add(i, Locale.getInstance("no", "NO", ""));
                    i = size + 1;
                    nynorskList.add(size, Locale.getInstance("no", "", ""));
                    return nynorskList;
                } else {
                    if (language.equals("zh")) {
                        if (script.length() != 0 || region.length() <= 0) {
                            if (script.length() > 0 && region.length() == 0) {
                                if (script.equals("Hans")) {
                                    region = "CN";
                                } else if (script.equals("Hant")) {
                                    region = "TW";
                                }
                            }
                        } else if (region.equals("TW") || region.equals("HK") || region.equals("MO")) {
                            script = "Hant";
                        } else if (region.equals("CN") || region.equals("SG")) {
                            script = "Hans";
                        }
                    }
                    return getDefaultList(language, script, region, variant);
                }
            }

            private static List<Locale> getDefaultList(String language, String script, String region, String variant) {
                Iterable variants = null;
                if (variant.length() > 0) {
                    variants = new LinkedList();
                    int idx = variant.length();
                    while (idx != -1) {
                        variants.add(variant.substring(0, idx));
                        idx = variant.lastIndexOf(95, idx - 1);
                    }
                }
                List<Locale> list = new LinkedList();
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
            if (baseName == null) {
                throw new NullPointerException();
            }
            Locale defaultLocale = Locale.getDefault();
            return locale.equals(defaultLocale) ? null : defaultLocale;
        }

        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            ResourceBundle bundle = null;
            if (format.equals("java.class")) {
                try {
                    Class<? extends ResourceBundle> bundleClass = loader.loadClass(bundleName);
                    if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                        bundle = (ResourceBundle) bundleClass.newInstance();
                    } else {
                        throw new ClassCastException(bundleClass.getName() + " cannot be cast to ResourceBundle");
                    }
                } catch (ClassNotFoundException e) {
                }
            } else {
                if (format.equals("java.properties")) {
                    final String resourceName = toResourceName0(bundleName, "properties");
                    if (resourceName == null) {
                        return null;
                    }
                    ClassLoader classLoader = loader;
                    boolean reloadFlag = reload;
                    try {
                        final boolean z = reload;
                        final ClassLoader classLoader2 = loader;
                        InputStream stream = (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                            public InputStream run() throws IOException {
                                if (!z) {
                                    return classLoader2.getResourceAsStream(resourceName);
                                }
                                URL url = classLoader2.getResource(resourceName);
                                if (url == null) {
                                    return null;
                                }
                                URLConnection connection = url.openConnection();
                                if (connection == null) {
                                    return null;
                                }
                                connection.setUseCaches(ResourceBundle.-assertionsDisabled);
                                return connection.getInputStream();
                            }
                        });
                        if (stream != null) {
                            try {
                                bundle = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
                            } finally {
                                stream.close();
                            }
                        }
                    } catch (PrivilegedActionException e2) {
                        throw ((IOException) e2.getException());
                    }
                }
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

        /* JADX WARNING: Missing block: B:6:0x001c, code:
            if (r17.equals("java.properties") != false) goto L_0x001e;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {
            if (bundle == null) {
                throw new NullPointerException();
            }
            if (!format.equals("java.class")) {
            }
            format = format.substring(5);
            boolean result = ResourceBundle.-assertionsDisabled;
            try {
                String resourceName = toResourceName0(toBundleName(baseName, locale), format);
                if (resourceName == null) {
                    return ResourceBundle.-assertionsDisabled;
                }
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    long lastModified = 0;
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(ResourceBundle.-assertionsDisabled);
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
                    result = lastModified >= loadTime ? true : ResourceBundle.-assertionsDisabled;
                }
                return result;
            } catch (NullPointerException npe) {
                throw npe;
            } catch (Exception e) {
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
                    sb.append(language).append('_').append(script).append('_').append(country).append('_').append(variant);
                } else if (country != "") {
                    sb.append(language).append('_').append(script).append('_').append(country);
                } else {
                    sb.append(language).append('_').append(script);
                }
            } else if (variant != "") {
                sb.append(language).append('_').append(country).append('_').append(variant);
            } else if (country != "") {
                sb.append(language).append('_').append(country);
            } else {
                sb.append(language);
            }
            return sb.toString();
        }

        public final String toResourceName(String bundleName, String suffix) {
            StringBuilder sb = new StringBuilder((bundleName.length() + 1) + suffix.length());
            sb.append(bundleName.replace('.', '/')).append('.').append(suffix);
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

    private static class SingleFormatControl extends Control {
        private static final Control CLASS_ONLY = new SingleFormatControl(FORMAT_CLASS);
        private static final Control PROPERTIES_ONLY = new SingleFormatControl(FORMAT_PROPERTIES);
        private final List<String> formats;

        protected SingleFormatControl(List<String> formats) {
            this.formats = formats;
        }

        public List<String> getFormats(String baseName) {
            if (baseName != null) {
                return this.formats;
            }
            throw new NullPointerException();
        }
    }

    private static final class NoFallbackControl extends SingleFormatControl {
        private static final Control CLASS_ONLY_NO_FALLBACK = new NoFallbackControl(FORMAT_CLASS);
        private static final Control NO_FALLBACK = new NoFallbackControl(FORMAT_DEFAULT);
        private static final Control PROPERTIES_ONLY_NO_FALLBACK = new NoFallbackControl(FORMAT_PROPERTIES);

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
        private static final RBClassLoader INSTANCE = ((RBClassLoader) AccessController.doPrivileged(new PrivilegedAction<RBClassLoader>() {
            public RBClassLoader run() {
                return new RBClassLoader();
            }
        }));
        private static final ClassLoader loader = ClassLoader.getSystemClassLoader();

        /* synthetic */ RBClassLoader(RBClassLoader -this0) {
            this();
        }

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

    public abstract Enumeration<String> getKeys();

    protected abstract Object handleGetObject(String str);

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

    protected void setParent(ResourceBundle parent) {
        if (-assertionsDisabled || parent != NONEXISTENT_BUNDLE) {
            this.parent = parent;
            return;
        }
        throw new AssertionError();
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
    public static final ResourceBundle getBundle(String baseName, Locale locale) {
        return getBundleImpl(baseName, locale, getLoader(VMStack.getCallingClassLoader()), getDefaultControl(baseName));
    }

    @CallerSensitive
    public static final ResourceBundle getBundle(String baseName, Locale targetLocale, Control control) {
        return getBundleImpl(baseName, targetLocale, getLoader(VMStack.getCallingClassLoader()), control);
    }

    public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
        if (loader != null) {
            return getBundleImpl(baseName, locale, loader, getDefaultControl(baseName));
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

    private static ResourceBundle getBundleImpl(String baseName, Locale locale, ClassLoader loader, Control control) {
        if (locale == null || control == null) {
            throw new NullPointerException();
        }
        CacheKey cacheKey = new CacheKey(baseName, locale, loader);
        ResourceBundle bundle = null;
        BundleReference bundleRef = (BundleReference) cacheList.get(cacheKey);
        if (bundleRef != null) {
            bundle = (ResourceBundle) bundleRef.get();
        }
        if (isValidBundle(bundle) && hasValidParentChain(bundle)) {
            return bundle;
        }
        boolean isKnownControl;
        if (control != Control.INSTANCE) {
            isKnownControl = control instanceof SingleFormatControl;
        } else {
            isKnownControl = true;
        }
        List<String> formats = control.getFormats(baseName);
        if (isKnownControl || (checkList(formats) ^ 1) == 0) {
            ResourceBundle baseBundle = null;
            Locale targetLocale = locale;
            while (targetLocale != null) {
                List<Locale> candidateLocales = control.getCandidateLocales(baseName, targetLocale);
                if (isKnownControl || (checkList(candidateLocales) ^ 1) == 0) {
                    bundle = findBundle(cacheKey, candidateLocales, formats, 0, control, baseBundle);
                    if (isValidBundle(bundle)) {
                        boolean isBaseBundle = Locale.ROOT.equals(bundle.locale);
                        if (!isBaseBundle || bundle.locale.equals(locale) || (candidateLocales.size() == 1 && bundle.locale.equals(candidateLocales.get(0)))) {
                            break;
                        } else if (isBaseBundle && baseBundle == null) {
                            baseBundle = bundle;
                        }
                    }
                    targetLocale = control.getFallbackLocale(baseName, targetLocale);
                } else {
                    throw new IllegalArgumentException("Invalid Control: getCandidateLocales");
                }
            }
            if (bundle == null) {
                if (baseBundle == null) {
                    throwMissingResourceException(baseName, locale, cacheKey.getCause());
                }
                bundle = baseBundle;
            }
            return bundle;
        }
        throw new IllegalArgumentException("Invalid Control: getFormats");
    }

    private static boolean checkList(List<?> a) {
        boolean valid = a != null ? a.isEmpty() ^ 1 : -assertionsDisabled;
        if (valid) {
            int size = a.size();
            int i = 0;
            while (valid && i < size) {
                valid = a.get(i) != null ? true : -assertionsDisabled;
                i++;
            }
        }
        return valid;
    }

    private static ResourceBundle findBundle(CacheKey cacheKey, List<Locale> candidateLocales, List<String> formats, int index, Control control, ResourceBundle baseBundle) {
        Locale targetLocale = (Locale) candidateLocales.get(index);
        ResourceBundle parent = null;
        if (index != candidateLocales.size() - 1) {
            parent = findBundle(cacheKey, candidateLocales, formats, index + 1, control, baseBundle);
        } else if (baseBundle != null && Locale.ROOT.equals(targetLocale)) {
            return baseBundle;
        }
        while (true) {
            Object ref = referenceQueue.poll();
            if (ref == null) {
                break;
            }
            cacheList.remove(((CacheKeyReference) ref).getCacheKey());
        }
        boolean expiredBundle = -assertionsDisabled;
        cacheKey.setLocale(targetLocale);
        ResourceBundle bundle = findBundleInCache(cacheKey, control);
        if (isValidBundle(bundle)) {
            expiredBundle = bundle.expired;
            if (!expiredBundle) {
                if (bundle.parent == parent) {
                    return bundle;
                }
                BundleReference bundleRef = (BundleReference) cacheList.get(cacheKey);
                if (bundleRef != null && bundleRef.get() == bundle) {
                    cacheList.remove(cacheKey, bundleRef);
                }
            }
        }
        if (bundle != NONEXISTENT_BUNDLE) {
            CacheKey constKey = (CacheKey) cacheKey.clone();
            try {
                bundle = loadBundle(cacheKey, formats, control, expiredBundle);
                if (bundle != null) {
                    if (bundle.parent == null) {
                        bundle.setParent(parent);
                    }
                    bundle.locale = targetLocale;
                    bundle = putBundleInCache(cacheKey, bundle, control);
                    return bundle;
                }
                putBundleInCache(cacheKey, NONEXISTENT_BUNDLE, control);
                if (constKey.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            } finally {
                if (constKey.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return parent;
    }

    private static ResourceBundle loadBundle(CacheKey cacheKey, List<String> formats, Control control, boolean reload) {
        Locale targetLocale = cacheKey.getLocale();
        ResourceBundle bundle = null;
        int size = formats.size();
        for (int i = 0; i < size; i++) {
            String format = (String) formats.get(i);
            try {
                bundle = control.newBundle(cacheKey.getName(), targetLocale, format, cacheKey.getLoader(), reload);
            } catch (LinkageError error) {
                cacheKey.setCause(error);
            } catch (Exception cause) {
                cacheKey.setCause(cause);
            }
            if (bundle != null) {
                cacheKey.setFormat(format);
                bundle.name = cacheKey.getName();
                bundle.locale = targetLocale;
                bundle.expired = -assertionsDisabled;
                break;
            }
        }
        return bundle;
    }

    private static boolean isValidBundle(ResourceBundle bundle) {
        return (bundle == null || bundle == NONEXISTENT_BUNDLE) ? -assertionsDisabled : true;
    }

    private static boolean hasValidParentChain(ResourceBundle bundle) {
        long now = System.currentTimeMillis();
        while (bundle != null) {
            if (bundle.expired) {
                return -assertionsDisabled;
            }
            CacheKey key = bundle.cacheKey;
            if (key != null) {
                long expirationTime = key.expirationTime;
                if (expirationTime >= 0 && expirationTime <= now) {
                    return -assertionsDisabled;
                }
            }
            bundle = bundle.parent;
        }
        return true;
    }

    private static void throwMissingResourceException(String baseName, Locale locale, Throwable cause) {
        if (cause instanceof MissingResourceException) {
            cause = null;
        }
        throw new MissingResourceException("Can't find bundle for base name " + baseName + ", locale " + locale, baseName + BaseLocale.SEP + locale, "", cause);
    }

    private static ResourceBundle findBundleInCache(CacheKey cacheKey, Control control) {
        BundleReference bundleRef = (BundleReference) cacheList.get(cacheKey);
        if (bundleRef == null) {
            return null;
        }
        ResourceBundle bundle = (ResourceBundle) bundleRef.get();
        if (bundle == null) {
            return null;
        }
        ResourceBundle p = bundle.parent;
        if (-assertionsDisabled || p != NONEXISTENT_BUNDLE) {
            if (p == null || !p.expired) {
                CacheKey key = bundleRef.getCacheKey();
                long expirationTime = key.expirationTime;
                if (!bundle.expired && expirationTime >= 0 && expirationTime <= System.currentTimeMillis()) {
                    if (bundle != NONEXISTENT_BUNDLE) {
                        synchronized (bundle) {
                            expirationTime = key.expirationTime;
                            if (!bundle.expired && expirationTime >= 0 && expirationTime <= System.currentTimeMillis()) {
                                try {
                                    bundle.expired = control.needsReload(key.getName(), key.getLocale(), key.getFormat(), key.getLoader(), bundle, key.loadTime);
                                } catch (Exception e) {
                                    cacheKey.setCause(e);
                                }
                                if (bundle.expired) {
                                    bundle.cacheKey = null;
                                    cacheList.remove(cacheKey, bundleRef);
                                } else {
                                    setExpirationTime(key, control);
                                }
                            }
                        }
                    } else {
                        cacheList.remove(cacheKey, bundleRef);
                        bundle = null;
                    }
                }
            } else if (-assertionsDisabled || bundle != NONEXISTENT_BUNDLE) {
                bundle.expired = true;
                bundle.cacheKey = null;
                cacheList.remove(cacheKey, bundleRef);
                bundle = null;
            } else {
                throw new AssertionError();
            }
            return bundle;
        }
        throw new AssertionError();
    }

    private static ResourceBundle putBundleInCache(CacheKey cacheKey, ResourceBundle bundle, Control control) {
        setExpirationTime(cacheKey, control);
        if (cacheKey.expirationTime == -1) {
            return bundle;
        }
        CacheKey key = (CacheKey) cacheKey.clone();
        BundleReference bundleRef = new BundleReference(bundle, referenceQueue, key);
        bundle.cacheKey = key;
        BundleReference result = (BundleReference) cacheList.putIfAbsent(key, bundleRef);
        if (result == null) {
            return bundle;
        }
        ResourceBundle rb = (ResourceBundle) result.get();
        if (rb == null || (rb.expired ^ 1) == 0) {
            cacheList.put(key, bundleRef);
            return bundle;
        }
        bundle.cacheKey = null;
        bundle = rb;
        bundleRef.clear();
        return bundle;
    }

    private static void setExpirationTime(CacheKey cacheKey, Control control) {
        long ttl = control.getTimeToLive(cacheKey.getName(), cacheKey.getLocale());
        if (ttl >= 0) {
            long now = System.currentTimeMillis();
            cacheKey.loadTime = now;
            cacheKey.expirationTime = now + ttl;
        } else if (ttl >= -2) {
            cacheKey.expirationTime = ttl;
        } else {
            throw new IllegalArgumentException("Invalid Control: TTL=" + ttl);
        }
    }

    @CallerSensitive
    public static final void clearCache() {
        clearCache(getLoader(VMStack.getCallingClassLoader()));
    }

    public static final void clearCache(ClassLoader loader) {
        if (loader == null) {
            throw new NullPointerException();
        }
        Set<CacheKey> set = cacheList.keySet();
        for (CacheKey key : set) {
            if (key.getLoader() == loader) {
                set.remove(key);
            }
        }
    }

    public boolean containsKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
            if (rb.handleKeySet().contains(key)) {
                return true;
            }
        }
        return -assertionsDisabled;
    }

    public Set<String> keySet() {
        Set<String> keys = new HashSet();
        for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
            keys.addAll(rb.handleKeySet());
        }
        return keys;
    }

    protected Set<String> handleKeySet() {
        if (this.keySet == null) {
            synchronized (this) {
                if (this.keySet == null) {
                    Set<String> keys = new HashSet();
                    Enumeration<String> enumKeys = getKeys();
                    while (enumKeys.hasMoreElements()) {
                        String key = (String) enumKeys.nextElement();
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
