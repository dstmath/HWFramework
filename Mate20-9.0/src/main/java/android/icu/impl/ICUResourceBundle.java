package android.icu.impl;

import android.icu.impl.ICUResourceBundleImpl;
import android.icu.impl.ICUResourceBundleReader;
import android.icu.impl.URLHandler;
import android.icu.impl.UResource;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import android.icu.util.UResourceTypeMismatchException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public class ICUResourceBundle extends UResourceBundle {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int ALIAS = 3;
    public static final int ARRAY16 = 9;
    private static CacheBase<String, ICUResourceBundle, Loader> BUNDLE_CACHE = new SoftCache<String, ICUResourceBundle, Loader>() {
        /* access modifiers changed from: protected */
        public ICUResourceBundle createInstance(String unusedKey, Loader loader) {
            return loader.load();
        }
    };
    /* access modifiers changed from: private */
    public static final boolean DEBUG = ICUDebug.enabled("localedata");
    private static final String DEFAULT_TAG = "default";
    private static final String FULL_LOCALE_NAMES_LIST = "fullLocaleNames.lst";
    private static CacheBase<String, AvailEntry, ClassLoader> GET_AVAILABLE_CACHE = new SoftCache<String, AvailEntry, ClassLoader>() {
        /* access modifiers changed from: protected */
        public AvailEntry createInstance(String key, ClassLoader loader) {
            return new AvailEntry(key, loader);
        }
    };
    private static final char HYPHEN = '-';
    private static final String ICUDATA = "ICUDATA";
    public static final ClassLoader ICU_DATA_CLASS_LOADER = ClassLoaderUtil.getClassLoader(ICUData.class);
    private static final String ICU_RESOURCE_INDEX = "res_index";
    protected static final String INSTALLED_LOCALES = "InstalledLocales";
    private static final String LOCALE = "LOCALE";
    public static final String NO_INHERITANCE_MARKER = "∅∅∅";
    public static final int RES_BOGUS = -1;
    private static final char RES_PATH_SEP_CHAR = '/';
    private static final String RES_PATH_SEP_STR = "/";
    public static final int STRING_V2 = 6;
    public static final int TABLE16 = 5;
    public static final int TABLE32 = 4;
    private ICUResourceBundle container;
    protected String key;
    WholeBundle wholeBundle;

    private static final class AvailEntry {
        private volatile Set<String> fullNameSet;
        private ClassLoader loader;
        private volatile Locale[] locales;
        private volatile Set<String> nameSet;
        private String prefix;
        private volatile ULocale[] ulocales;

        AvailEntry(String prefix2, ClassLoader loader2) {
            this.prefix = prefix2;
            this.loader = loader2;
        }

        /* access modifiers changed from: package-private */
        public ULocale[] getULocaleList() {
            if (this.ulocales == null) {
                synchronized (this) {
                    if (this.ulocales == null) {
                        this.ulocales = ICUResourceBundle.createULocaleList(this.prefix, this.loader);
                    }
                }
            }
            return this.ulocales;
        }

        /* access modifiers changed from: package-private */
        public Locale[] getLocaleList() {
            if (this.locales == null) {
                getULocaleList();
                synchronized (this) {
                    if (this.locales == null) {
                        this.locales = ICUResourceBundle.getLocaleList(this.ulocales);
                    }
                }
            }
            return this.locales;
        }

        /* access modifiers changed from: package-private */
        public Set<String> getLocaleNameSet() {
            if (this.nameSet == null) {
                synchronized (this) {
                    if (this.nameSet == null) {
                        this.nameSet = ICUResourceBundle.createLocaleNameSet(this.prefix, this.loader);
                    }
                }
            }
            return this.nameSet;
        }

        /* access modifiers changed from: package-private */
        public Set<String> getFullLocaleNameSet() {
            if (this.fullNameSet == null) {
                synchronized (this) {
                    if (this.fullNameSet == null) {
                        this.fullNameSet = ICUResourceBundle.createFullLocaleNameSet(this.prefix, this.loader);
                    }
                }
            }
            return this.fullNameSet;
        }
    }

    private static abstract class Loader {
        /* access modifiers changed from: package-private */
        public abstract ICUResourceBundle load();

        private Loader() {
        }
    }

    public enum OpenType {
        LOCALE_DEFAULT_ROOT,
        LOCALE_ROOT,
        LOCALE_ONLY,
        DIRECT
    }

    protected static final class WholeBundle {
        String baseName;
        ClassLoader loader;
        String localeID;
        ICUResourceBundleReader reader;
        Set<String> topLevelKeys;
        ULocale ulocale;

        WholeBundle(String baseName2, String localeID2, ClassLoader loader2, ICUResourceBundleReader reader2) {
            this.baseName = baseName2;
            this.localeID = localeID2;
            this.ulocale = new ULocale(localeID2);
            this.loader = loader2;
            this.reader = reader2;
        }
    }

    public static final ULocale getFunctionalEquivalent(String baseName, ClassLoader loader, String resName, String keyword, ULocale locID, boolean[] isAvailable, boolean omitDefault) {
        String kwVal;
        String kwVal2;
        String str = baseName;
        String str2 = resName;
        String kwVal3 = locID.getKeywordValue(keyword);
        String baseLoc = locID.getBaseName();
        String defStr = null;
        ULocale parent = new ULocale(baseLoc);
        ULocale defLoc = null;
        boolean lookForDefault = false;
        ULocale fullBase = null;
        int defDepth = 0;
        int resDepth = 0;
        if (kwVal3 == null || kwVal3.length() == 0 || kwVal3.equals(DEFAULT_TAG)) {
            kwVal3 = "";
            lookForDefault = true;
        }
        ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance(str, parent);
        if (isAvailable != null) {
            isAvailable[0] = false;
            ULocale[] availableULocales = getAvailEntry(baseName, loader).getULocaleList();
            int i = 0;
            while (true) {
                kwVal = kwVal3;
                int i2 = i;
                if (i2 < availableULocales.length) {
                    if (parent.equals(availableULocales[i2])) {
                        isAvailable[0] = true;
                        break;
                    }
                    i = i2 + 1;
                    kwVal3 = kwVal;
                    ULocale uLocale = locID;
                }
            }
        } else {
            kwVal = kwVal3;
        }
        while (true) {
            try {
                defStr = ((ICUResourceBundle) r.get(str2)).getString(DEFAULT_TAG);
                if (lookForDefault) {
                    lookForDefault = false;
                    kwVal = defStr;
                }
                try {
                    defLoc = r.getULocale();
                } catch (MissingResourceException e) {
                }
            } catch (MissingResourceException e2) {
            }
            kwVal2 = kwVal;
            if (defLoc == null) {
                defDepth++;
                r = r.getParent();
            }
            if (r == null || defLoc != null) {
                String defStr2 = defStr;
                ICUResourceBundle r2 = (ICUResourceBundle) UResourceBundle.getBundleInstance(str, new ULocale(baseLoc));
            } else {
                kwVal = kwVal2;
            }
        }
        String defStr22 = defStr;
        ICUResourceBundle r22 = (ICUResourceBundle) UResourceBundle.getBundleInstance(str, new ULocale(baseLoc));
        do {
            try {
                ICUResourceBundle irb = (ICUResourceBundle) r22.get(str2);
                irb.get(kwVal2);
                fullBase = irb.getULocale();
                if (fullBase != null && resDepth > defDepth) {
                    defStr22 = irb.getString(DEFAULT_TAG);
                    defLoc = r22.getULocale();
                    defDepth = resDepth;
                }
            } catch (MissingResourceException e3) {
            }
            if (fullBase == null) {
                resDepth++;
                r22 = r22.getParent();
            }
            if (r22 == null) {
                break;
            }
        } while (fullBase == null);
        if (fullBase == null && defStr22 != null && !defStr22.equals(kwVal2)) {
            kwVal2 = defStr22;
            ULocale defLoc2 = defLoc;
            String defStr3 = defStr22;
            int resDepth2 = 0;
            ICUResourceBundle r3 = (ICUResourceBundle) UResourceBundle.getBundleInstance(str, new ULocale(baseLoc));
            while (true) {
                try {
                    ICUResourceBundle irb2 = (ICUResourceBundle) r3.get(str2);
                    fullBase = r3.getULocale();
                    if (!fullBase.getBaseName().equals(((ICUResourceBundle) irb2.get(kwVal2)).getULocale().getBaseName())) {
                        fullBase = null;
                    }
                    if (fullBase != null && resDepth2 > defDepth) {
                        defStr3 = irb2.getString(DEFAULT_TAG);
                        defLoc2 = r3.getULocale();
                        defDepth = resDepth2;
                    }
                } catch (MissingResourceException e4) {
                }
                if (fullBase == null) {
                    resDepth2++;
                    r3 = r3.getParent();
                }
                if (r3 == null || fullBase != null) {
                    ULocale uLocale2 = defLoc2;
                    resDepth = resDepth2;
                    defStr22 = defStr3;
                    ULocale uLocale3 = uLocale2;
                } else {
                    str2 = resName;
                }
            }
            ULocale uLocale22 = defLoc2;
            resDepth = resDepth2;
            defStr22 = defStr3;
            ULocale uLocale32 = uLocale22;
        }
        if (fullBase == null) {
            throw new MissingResourceException("Could not find locale containing requested or default keyword.", str, r3 + "=" + kwVal2);
        } else if (omitDefault && defStr22.equals(kwVal2) && resDepth <= defDepth) {
            return fullBase;
        } else {
            return new ULocale(fullBase.getBaseName() + "@" + r3 + "=" + kwVal2);
        }
    }

    public static final String[] getKeywordValues(String baseName, String keyword) {
        Set<String> keywords = new HashSet<>();
        ULocale[] locales = getAvailEntry(baseName, ICU_DATA_CLASS_LOADER).getULocaleList();
        for (int i = 0; i < locales.length; i++) {
            try {
                Enumeration<String> e = ((ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, locales[i]).getObject(keyword)).getKeys();
                while (e.hasMoreElements()) {
                    String s = e.nextElement();
                    if (!DEFAULT_TAG.equals(s) && !s.startsWith("private-")) {
                        keywords.add(s);
                    }
                }
            } catch (Throwable th) {
            }
        }
        return (String[]) keywords.toArray(new String[0]);
    }

    public ICUResourceBundle getWithFallback(String path) throws MissingResourceException {
        ICUResourceBundle result = findResourceWithFallback(path, this, null);
        if (result == null) {
            throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getType(), path, getKey());
        } else if (result.getType() != 0 || !result.getString().equals(NO_INHERITANCE_MARKER)) {
            return result;
        } else {
            throw new MissingResourceException("Encountered NO_INHERITANCE_MARKER", path, getKey());
        }
    }

    public ICUResourceBundle at(int index) {
        return (ICUResourceBundle) handleGet(index, (HashMap<String, String>) null, (UResourceBundle) this);
    }

    public ICUResourceBundle at(String key2) {
        if (this instanceof ICUResourceBundleImpl.ResourceTable) {
            return (ICUResourceBundle) handleGet(key2, (HashMap<String, String>) null, (UResourceBundle) this);
        }
        return null;
    }

    public ICUResourceBundle findTopLevel(int index) {
        return (ICUResourceBundle) super.findTopLevel(index);
    }

    public ICUResourceBundle findTopLevel(String aKey) {
        return (ICUResourceBundle) super.findTopLevel(aKey);
    }

    public ICUResourceBundle findWithFallback(String path) {
        return findResourceWithFallback(path, this, null);
    }

    public String findStringWithFallback(String path) {
        return findStringWithFallback(path, this, null);
    }

    public String getStringWithFallback(String path) throws MissingResourceException {
        String result = findStringWithFallback(path, this, null);
        if (result == null) {
            throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getType(), path, getKey());
        } else if (!result.equals(NO_INHERITANCE_MARKER)) {
            return result;
        } else {
            throw new MissingResourceException("Encountered NO_INHERITANCE_MARKER", path, getKey());
        }
    }

    public void getAllItemsWithFallbackNoFail(String path, UResource.Sink sink) {
        try {
            getAllItemsWithFallback(path, sink);
        } catch (MissingResourceException e) {
        }
    }

    public void getAllItemsWithFallback(String path, UResource.Sink sink) throws MissingResourceException {
        ICUResourceBundle rb;
        int numPathKeys = countPathKeys(path);
        if (numPathKeys == 0) {
            rb = this;
        } else {
            int depth = getResDepth();
            String[] pathKeys = new String[(depth + numPathKeys)];
            getResPathKeys(path, numPathKeys, pathKeys, depth);
            ICUResourceBundle rb2 = findResourceWithFallback(pathKeys, depth, this, null);
            if (rb2 != null) {
                rb = rb2;
            } else {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getType(), path, getKey());
            }
        }
        rb.getAllItemsWithFallback(new UResource.Key(), new ICUResourceBundleReader.ReaderValue(), sink);
    }

    private void getAllItemsWithFallback(UResource.Key key2, ICUResourceBundleReader.ReaderValue readerValue, UResource.Sink sink) {
        ICUResourceBundle rb;
        ICUResourceBundleImpl impl = (ICUResourceBundleImpl) this;
        readerValue.reader = impl.wholeBundle.reader;
        readerValue.res = impl.getResource();
        key2.setString(this.key != null ? this.key : "");
        sink.put(key2, readerValue, this.parent == null);
        if (this.parent != null) {
            ICUResourceBundle parentBundle = (ICUResourceBundle) this.parent;
            int depth = getResDepth();
            if (depth == 0) {
                rb = parentBundle;
            } else {
                String[] pathKeys = new String[depth];
                getResPathKeys(pathKeys, depth);
                rb = findResourceWithFallback(pathKeys, 0, parentBundle, null);
            }
            if (rb != null) {
                rb.getAllItemsWithFallback(key2, readerValue, sink);
            }
        }
    }

    public static Set<String> getAvailableLocaleNameSet(String bundlePrefix, ClassLoader loader) {
        return getAvailEntry(bundlePrefix, loader).getLocaleNameSet();
    }

    public static Set<String> getFullLocaleNameSet() {
        return getFullLocaleNameSet(ICUData.ICU_BASE_NAME, ICU_DATA_CLASS_LOADER);
    }

    public static Set<String> getFullLocaleNameSet(String bundlePrefix, ClassLoader loader) {
        return getAvailEntry(bundlePrefix, loader).getFullLocaleNameSet();
    }

    public static Set<String> getAvailableLocaleNameSet() {
        return getAvailableLocaleNameSet(ICUData.ICU_BASE_NAME, ICU_DATA_CLASS_LOADER);
    }

    public static final ULocale[] getAvailableULocales(String baseName, ClassLoader loader) {
        return getAvailEntry(baseName, loader).getULocaleList();
    }

    public static final ULocale[] getAvailableULocales() {
        return getAvailableULocales(ICUData.ICU_BASE_NAME, ICU_DATA_CLASS_LOADER);
    }

    public static final Locale[] getAvailableLocales(String baseName, ClassLoader loader) {
        return getAvailEntry(baseName, loader).getLocaleList();
    }

    public static final Locale[] getAvailableLocales() {
        return getAvailEntry(ICUData.ICU_BASE_NAME, ICU_DATA_CLASS_LOADER).getLocaleList();
    }

    public static final Locale[] getLocaleList(ULocale[] ulocales) {
        ArrayList<Locale> list = new ArrayList<>(ulocales.length);
        HashSet<Locale> uniqueSet = new HashSet<>();
        for (ULocale locale : ulocales) {
            Locale loc = locale.toLocale();
            if (!uniqueSet.contains(loc)) {
                list.add(loc);
                uniqueSet.add(loc);
            }
        }
        return (Locale[]) list.toArray(new Locale[list.size()]);
    }

    public Locale getLocale() {
        return getULocale().toLocale();
    }

    /* access modifiers changed from: private */
    public static final ULocale[] createULocaleList(String baseName, ClassLoader root) {
        int i;
        ICUResourceBundle bundle = (ICUResourceBundle) ((ICUResourceBundle) UResourceBundle.instantiateBundle(baseName, ICU_RESOURCE_INDEX, root, true)).get(INSTALLED_LOCALES);
        int i2 = 0;
        ULocale[] locales = new ULocale[bundle.getSize()];
        UResourceBundleIterator iter = bundle.getIterator();
        iter.reset();
        while (iter.hasNext()) {
            String locstr = iter.next().getKey();
            if (locstr.equals("root")) {
                i = i2 + 1;
                locales[i2] = ULocale.ROOT;
            } else {
                i = i2 + 1;
                locales[i2] = new ULocale(locstr);
            }
            i2 = i;
        }
        return locales;
    }

    private static final void addLocaleIDsFromIndexBundle(String baseName, ClassLoader root, Set<String> locales) {
        try {
            UResourceBundleIterator iter = ((ICUResourceBundle) ((ICUResourceBundle) UResourceBundle.instantiateBundle(baseName, ICU_RESOURCE_INDEX, root, true)).get(INSTALLED_LOCALES)).getIterator();
            iter.reset();
            while (iter.hasNext()) {
                locales.add(iter.next().getKey());
            }
        } catch (MissingResourceException e) {
            if (DEBUG) {
                PrintStream printStream = System.out;
                printStream.println("couldn't find " + baseName + RES_PATH_SEP_CHAR + ICU_RESOURCE_INDEX + ".res");
                Thread.dumpStack();
            }
        }
    }

    private static final void addBundleBaseNamesFromClassLoader(final String bn, final ClassLoader root, final Set<String> names) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                try {
                    Enumeration<URL> urls = root.getResources(bn);
                    if (urls == null) {
                        return null;
                    }
                    URLHandler.URLVisitor v = new URLHandler.URLVisitor() {
                        public void visit(String s) {
                            if (s.endsWith(".res")) {
                                names.add(s.substring(0, s.length() - 4));
                            }
                        }
                    };
                    while (urls.hasMoreElements()) {
                        URL url = urls.nextElement();
                        URLHandler handler = URLHandler.get(url);
                        if (handler != null) {
                            handler.guide(v, false);
                        } else if (ICUResourceBundle.DEBUG) {
                            PrintStream printStream = System.out;
                            printStream.println("handler for " + url + " is null");
                        }
                    }
                    return null;
                } catch (IOException e) {
                    if (ICUResourceBundle.DEBUG) {
                        PrintStream printStream2 = System.out;
                        printStream2.println("ouch: " + e.getMessage());
                    }
                }
            }
        });
    }

    private static void addLocaleIDsFromListFile(String bn, ClassLoader root, Set<String> locales) {
        BufferedReader br;
        try {
            InputStream s = root.getResourceAsStream(bn + FULL_LOCALE_NAMES_LIST);
            if (s != null) {
                br = new BufferedReader(new InputStreamReader(s, "ASCII"));
                while (true) {
                    String readLine = br.readLine();
                    String line = readLine;
                    if (readLine == null) {
                        br.close();
                        return;
                    } else if (line.length() != 0 && !line.startsWith("#")) {
                        locales.add(line);
                    }
                }
            }
        } catch (IOException e) {
        } catch (Throwable th) {
            br.close();
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static Set<String> createFullLocaleNameSet(String baseName, ClassLoader loader) {
        String bn;
        String folder;
        if (baseName.endsWith(RES_PATH_SEP_STR)) {
            bn = baseName;
        } else {
            bn = baseName + RES_PATH_SEP_STR;
        }
        Set<String> set = new HashSet<>();
        if (!ICUConfig.get("android.icu.impl.ICUResourceBundle.skipRuntimeLocaleResourceScan", "false").equalsIgnoreCase("true")) {
            addBundleBaseNamesFromClassLoader(bn, loader, set);
            if (baseName.startsWith(ICUData.ICU_BASE_NAME)) {
                if (baseName.length() == ICUData.ICU_BASE_NAME.length()) {
                    folder = "";
                } else if (baseName.charAt(ICUData.ICU_BASE_NAME.length()) == '/') {
                    folder = baseName.substring(ICUData.ICU_BASE_NAME.length() + 1);
                } else {
                    folder = null;
                }
                if (folder != null) {
                    ICUBinary.addBaseNamesInFileFolder(folder, ".res", set);
                }
            }
            set.remove(ICU_RESOURCE_INDEX);
            Iterator<String> iter = set.iterator();
            while (iter.hasNext()) {
                String name = iter.next();
                if ((name.length() == 1 || name.length() > 3) && name.indexOf(95) < 0) {
                    iter.remove();
                }
            }
        }
        if (set.isEmpty()) {
            if (DEBUG) {
                System.out.println("unable to enumerate data files in " + baseName);
            }
            addLocaleIDsFromListFile(bn, loader, set);
        }
        if (set.isEmpty()) {
            addLocaleIDsFromIndexBundle(baseName, loader, set);
        }
        set.remove("root");
        set.add(ULocale.ROOT.toString());
        return Collections.unmodifiableSet(set);
    }

    /* access modifiers changed from: private */
    public static Set<String> createLocaleNameSet(String baseName, ClassLoader loader) {
        HashSet<String> set = new HashSet<>();
        addLocaleIDsFromIndexBundle(baseName, loader, set);
        return Collections.unmodifiableSet(set);
    }

    private static AvailEntry getAvailEntry(String key2, ClassLoader loader) {
        return GET_AVAILABLE_CACHE.getInstance(key2, loader);
    }

    private static final ICUResourceBundle findResourceWithFallback(String path, UResourceBundle actualBundle, UResourceBundle requested) {
        if (path.length() == 0) {
            return null;
        }
        ICUResourceBundle base = (ICUResourceBundle) actualBundle;
        int depth = base.getResDepth();
        int numPathKeys = countPathKeys(path);
        String[] keys = new String[(depth + numPathKeys)];
        getResPathKeys(path, numPathKeys, keys, depth);
        return findResourceWithFallback(keys, depth, base, requested);
    }

    private static final ICUResourceBundle findResourceWithFallback(String[] keys, int depth, ICUResourceBundle base, UResourceBundle requested) {
        if (requested == null) {
            requested = base;
        }
        while (true) {
            int depth2 = depth + 1;
            ICUResourceBundle sub = (ICUResourceBundle) base.handleGet(keys[depth], (HashMap<String, String>) null, requested);
            if (sub == null) {
                int depth3 = depth2 - 1;
                ICUResourceBundle nextBase = base.getParent();
                if (nextBase == null) {
                    return null;
                }
                int baseDepth = base.getResDepth();
                if (depth3 != baseDepth) {
                    String[] newKeys = new String[((keys.length - depth3) + baseDepth)];
                    System.arraycopy(keys, depth3, newKeys, baseDepth, keys.length - depth3);
                    keys = newKeys;
                }
                base.getResPathKeys(keys, baseDepth);
                base = nextBase;
                depth = 0;
            } else if (depth2 == keys.length) {
                return sub;
            } else {
                base = sub;
                depth = depth2;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ea  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00e8 A[SYNTHETIC] */
    private static final String findStringWithFallback(String path, UResourceBundle actualBundle, UResourceBundle requested) {
        UResourceBundle requested2;
        ICUResourceBundle nextBase;
        ICUResourceBundleReader.Container readerContainer;
        int res;
        ICUResourceBundle sub;
        int depth;
        UResourceBundle uResourceBundle = actualBundle;
        if (path.length() == 0 || !(uResourceBundle instanceof ICUResourceBundleImpl.ResourceContainer)) {
            return null;
        }
        if (requested == null) {
            requested2 = uResourceBundle;
        } else {
            requested2 = requested;
        }
        ICUResourceBundle base = (ICUResourceBundle) uResourceBundle;
        ICUResourceBundleReader reader = base.wholeBundle.reader;
        int res2 = -1;
        int baseDepth = base.getResDepth();
        int depth2 = baseDepth;
        int numPathKeys = countPathKeys(path);
        String[] keys = new String[(depth2 + numPathKeys)];
        getResPathKeys(path, numPathKeys, keys, depth2);
        ICUResourceBundle base2 = base;
        ICUResourceBundleReader reader2 = reader;
        int baseDepth2 = baseDepth;
        String[] keys2 = keys;
        while (true) {
            if (res2 == -1) {
                int type = base2.getType();
                if (type == 2 || type == 8) {
                    readerContainer = ((ICUResourceBundleImpl.ResourceContainer) base2).value;
                }
                nextBase = base2.getParent();
                if (nextBase == null) {
                    return null;
                }
                base2.getResPathKeys(keys2, baseDepth2);
                base2 = nextBase;
                reader2 = base2.wholeBundle.reader;
                baseDepth2 = 0;
                depth2 = 0;
            } else {
                int type2 = ICUResourceBundleReader.RES_GET_TYPE(res2);
                if (ICUResourceBundleReader.URES_IS_TABLE(type2)) {
                    readerContainer = reader2.getTable(res2);
                } else if (ICUResourceBundleReader.URES_IS_ARRAY(type2)) {
                    readerContainer = reader2.getArray(res2);
                } else {
                    res2 = -1;
                    nextBase = base2.getParent();
                    if (nextBase == null) {
                    }
                }
            }
            ICUResourceBundleReader.Container readerContainer2 = readerContainer;
            int depth3 = depth2 + 1;
            String subKey = keys2[depth2];
            int res3 = readerContainer2.getResource(reader2, subKey);
            if (res3 == -1) {
                int i = depth3 - 1;
                res2 = res3;
                nextBase = base2.getParent();
                if (nextBase == null) {
                }
            } else {
                if (ICUResourceBundleReader.RES_GET_TYPE(res3) == 3) {
                    base2.getResPathKeys(keys2, baseDepth2);
                    res = res3;
                    String str = subKey;
                    depth = depth3;
                    ICUResourceBundleReader.Container container2 = readerContainer2;
                    sub = getAliasedResource(base2, keys2, depth3, subKey, res, null, requested2);
                } else {
                    res = res3;
                    String str2 = subKey;
                    depth = depth3;
                    ICUResourceBundleReader.Container container3 = readerContainer2;
                    sub = null;
                }
                if (depth != keys2.length) {
                    int res4 = res;
                    if (sub != null) {
                        base2 = sub;
                        ICUResourceBundleReader iCUResourceBundleReader = base2.wholeBundle.reader;
                        res4 = -1;
                        int baseDepth3 = base2.getResDepth();
                        if (depth != baseDepth3) {
                            String[] newKeys = new String[((keys2.length - depth) + baseDepth3)];
                            System.arraycopy(keys2, depth, newKeys, baseDepth3, keys2.length - depth);
                            keys2 = newKeys;
                            depth = baseDepth3;
                        }
                        depth2 = depth;
                        reader2 = iCUResourceBundleReader;
                        baseDepth2 = baseDepth3;
                    } else {
                        depth2 = depth;
                    }
                    res2 = res4;
                } else if (sub != null) {
                    return sub.getString();
                } else {
                    String s = reader2.getString(res);
                    if (s != null) {
                        return s;
                    }
                    throw new UResourceTypeMismatchException("");
                }
            }
        }
    }

    private int getResDepth() {
        if (this.container == null) {
            return 0;
        }
        return this.container.getResDepth() + 1;
    }

    private void getResPathKeys(String[] keys, int depth) {
        int depth2 = depth;
        ICUResourceBundle b = this;
        while (depth2 > 0) {
            depth2--;
            keys[depth2] = b.key;
            b = b.container;
        }
    }

    private static int countPathKeys(String path) {
        if (path.isEmpty()) {
            return 0;
        }
        int num = 1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                num++;
            }
        }
        return num;
    }

    private static void getResPathKeys(String path, int num, String[] keys, int start) {
        if (num != 0) {
            if (num == 1) {
                keys[start] = path;
                return;
            }
            int i = 0;
            while (true) {
                int j = path.indexOf(47, i);
                int start2 = start + 1;
                keys[start] = path.substring(i, j);
                if (num == 2) {
                    keys[start2] = path.substring(j + 1);
                    return;
                }
                i = j + 1;
                num--;
                start = start2;
            }
        }
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ICUResourceBundle) {
            ICUResourceBundle o = (ICUResourceBundle) other;
            if (getBaseName().equals(o.getBaseName()) && getLocaleID().equals(o.getLocaleID())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return 42;
    }

    public static ICUResourceBundle getBundleInstance(String baseName, String localeID, ClassLoader root, boolean disableFallback) {
        return getBundleInstance(baseName, localeID, root, disableFallback ? OpenType.DIRECT : OpenType.LOCALE_DEFAULT_ROOT);
    }

    public static ICUResourceBundle getBundleInstance(String baseName, ULocale locale, OpenType openType) {
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        return getBundleInstance(baseName, locale.getBaseName(), ICU_DATA_CLASS_LOADER, openType);
    }

    public static ICUResourceBundle getBundleInstance(String baseName, String localeID, ClassLoader root, OpenType openType) {
        ICUResourceBundle b;
        if (baseName == null) {
            baseName = ICUData.ICU_BASE_NAME;
        }
        String localeID2 = ULocale.getBaseName(localeID);
        if (openType == OpenType.LOCALE_DEFAULT_ROOT) {
            b = instantiateBundle(baseName, localeID2, ULocale.getDefault().getBaseName(), root, openType);
        } else {
            b = instantiateBundle(baseName, localeID2, null, root, openType);
        }
        if (b != null) {
            return b;
        }
        throw new MissingResourceException("Could not find the bundle " + baseName + RES_PATH_SEP_STR + localeID2 + ".res", "", "");
    }

    /* access modifiers changed from: private */
    public static boolean localeIDStartsWithLangSubtag(String localeID, String lang) {
        return localeID.startsWith(lang) && (localeID.length() == lang.length() || localeID.charAt(lang.length()) == '_');
    }

    /* access modifiers changed from: private */
    public static ICUResourceBundle instantiateBundle(String baseName, String localeID, String defaultID, ClassLoader root, OpenType openType) {
        String str;
        String str2;
        String fullName = ICUResourceBundleReader.getFullName(baseName, localeID);
        char openTypeChar = (char) (48 + openType.ordinal());
        OpenType openType2 = openType;
        if (openType2 != OpenType.LOCALE_DEFAULT_ROOT) {
            str2 = fullName + '#' + openTypeChar;
            str = defaultID;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(fullName);
            sb.append('#');
            sb.append(openTypeChar);
            sb.append('#');
            str = defaultID;
            sb.append(str);
            str2 = sb.toString();
        }
        String cacheKey = str2;
        CacheBase<String, ICUResourceBundle, Loader> cacheBase = BUNDLE_CACHE;
        final String str3 = fullName;
        final String str4 = baseName;
        final String str5 = localeID;
        final ClassLoader classLoader = root;
        final OpenType openType3 = openType2;
        final String str6 = str;
        AnonymousClass4 r0 = new Loader() {
            public ICUResourceBundle load() {
                ICUResourceBundle b;
                if (ICUResourceBundle.DEBUG) {
                    PrintStream printStream = System.out;
                    printStream.println("Creating " + str3);
                }
                String rootLocale = str4.indexOf(46) == -1 ? "root" : "";
                String localeName = str5.isEmpty() ? rootLocale : str5;
                ICUResourceBundle b2 = ICUResourceBundle.createBundle(str4, localeName, classLoader);
                if (ICUResourceBundle.DEBUG) {
                    PrintStream printStream2 = System.out;
                    StringBuilder sb = new StringBuilder();
                    sb.append("The bundle created is: ");
                    sb.append(b2);
                    sb.append(" and openType=");
                    sb.append(openType3);
                    sb.append(" and bundle.getNoFallback=");
                    sb.append(b2 != null && b2.getNoFallback());
                    printStream2.println(sb.toString());
                }
                if (openType3 == OpenType.DIRECT || (b2 != null && b2.getNoFallback())) {
                    return b2;
                }
                if (b2 == null) {
                    int i = localeName.lastIndexOf(95);
                    if (i != -1) {
                        b = ICUResourceBundle.instantiateBundle(str4, localeName.substring(0, i), str6, classLoader, openType3);
                    } else if (openType3 == OpenType.LOCALE_DEFAULT_ROOT && !ICUResourceBundle.localeIDStartsWithLangSubtag(str6, localeName)) {
                        b = ICUResourceBundle.instantiateBundle(str4, str6, str6, classLoader, openType3);
                    } else if (openType3 != OpenType.LOCALE_ONLY && !rootLocale.isEmpty()) {
                        b = ICUResourceBundle.createBundle(str4, rootLocale, classLoader);
                    }
                    b2 = b;
                } else {
                    UResourceBundle parent = null;
                    String localeName2 = b2.getLocaleID();
                    int i2 = localeName2.lastIndexOf(95);
                    String parentLocaleName = ((ICUResourceBundleImpl.ResourceTable) b2).findString("%%Parent");
                    if (parentLocaleName != null) {
                        parent = ICUResourceBundle.instantiateBundle(str4, parentLocaleName, str6, classLoader, openType3);
                    } else if (i2 != -1) {
                        parent = ICUResourceBundle.instantiateBundle(str4, localeName2.substring(0, i2), str6, classLoader, openType3);
                    } else if (!localeName2.equals(rootLocale)) {
                        parent = ICUResourceBundle.instantiateBundle(str4, rootLocale, str6, classLoader, openType3);
                    }
                    if (!b2.equals(parent)) {
                        b2.setParent(parent);
                    }
                }
                return b2;
            }
        };
        return cacheBase.getInstance(cacheKey, r0);
    }

    /* access modifiers changed from: package-private */
    public ICUResourceBundle get(String aKey, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
        ICUResourceBundle obj = (ICUResourceBundle) handleGet(aKey, aliasesVisited, requested);
        if (obj == null) {
            obj = getParent();
            if (obj != null) {
                obj = obj.get(aKey, aliasesVisited, requested);
            }
            if (obj == null) {
                String fullName = ICUResourceBundleReader.getFullName(getBaseName(), getLocaleID());
                throw new MissingResourceException("Can't find resource for bundle " + fullName + ", key " + aKey, getClass().getName(), aKey);
            }
        }
        return obj;
    }

    public static ICUResourceBundle createBundle(String baseName, String localeID, ClassLoader root) {
        ICUResourceBundleReader reader = ICUResourceBundleReader.getReader(baseName, localeID, root);
        if (reader == null) {
            return null;
        }
        return getBundle(reader, baseName, localeID, root);
    }

    /* access modifiers changed from: protected */
    public String getLocaleID() {
        return this.wholeBundle.localeID;
    }

    /* access modifiers changed from: protected */
    public String getBaseName() {
        return this.wholeBundle.baseName;
    }

    public ULocale getULocale() {
        return this.wholeBundle.ulocale;
    }

    public boolean isRoot() {
        return this.wholeBundle.localeID.isEmpty() || this.wholeBundle.localeID.equals("root");
    }

    public ICUResourceBundle getParent() {
        return (ICUResourceBundle) this.parent;
    }

    /* access modifiers changed from: protected */
    public void setParent(ResourceBundle parent) {
        this.parent = parent;
    }

    public String getKey() {
        return this.key;
    }

    /* access modifiers changed from: private */
    public boolean getNoFallback() {
        return this.wholeBundle.reader.getNoFallback();
    }

    private static ICUResourceBundle getBundle(ICUResourceBundleReader reader, String baseName, String localeID, ClassLoader loader) {
        int rootRes = reader.getRootResource();
        if (ICUResourceBundleReader.URES_IS_TABLE(ICUResourceBundleReader.RES_GET_TYPE(rootRes))) {
            ICUResourceBundleImpl.ResourceTable rootTable = new ICUResourceBundleImpl.ResourceTable(new WholeBundle(baseName, localeID, loader, reader), rootRes);
            String aliasString = rootTable.findString("%%ALIAS");
            if (aliasString != null) {
                return (ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, aliasString);
            }
            return rootTable;
        }
        throw new IllegalStateException("Invalid format error");
    }

    protected ICUResourceBundle(WholeBundle wholeBundle2) {
        this.wholeBundle = wholeBundle2;
    }

    protected ICUResourceBundle(ICUResourceBundle container2, String key2) {
        this.key = key2;
        this.wholeBundle = container2.wholeBundle;
        this.container = container2;
        this.parent = container2.parent;
    }

    protected static ICUResourceBundle getAliasedResource(ICUResourceBundle base, String[] keys, int depth, String key2, int _resource, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
        HashMap<String, String> aliasesVisited2;
        String locale;
        String bundleName;
        ClassLoader loaderToUse;
        String[] keys2;
        int numKeys;
        String locale2;
        ICUResourceBundle iCUResourceBundle = base;
        String str = key2;
        UResourceBundle uResourceBundle = requested;
        WholeBundle wholeBundle2 = iCUResourceBundle.wholeBundle;
        ClassLoader loaderToUse2 = wholeBundle2.loader;
        String keyPath = null;
        String rpath = wholeBundle2.reader.getAlias(_resource);
        if (aliasesVisited == null) {
            aliasesVisited2 = new HashMap<>();
        } else {
            aliasesVisited2 = aliasesVisited;
        }
        if (aliasesVisited2.get(rpath) == null) {
            aliasesVisited2.put(rpath, "");
            if (rpath.indexOf(47) == 0) {
                int i = rpath.indexOf(47, 1);
                int j = rpath.indexOf(47, i + 1);
                bundleName = rpath.substring(1, i);
                if (j < 0) {
                    locale = rpath.substring(i + 1);
                } else {
                    locale = rpath.substring(i + 1, j);
                    keyPath = rpath.substring(j + 1, rpath.length());
                }
                if (bundleName.equals(ICUDATA)) {
                    bundleName = ICUData.ICU_BASE_NAME;
                    loaderToUse = ICU_DATA_CLASS_LOADER;
                } else {
                    if (bundleName.indexOf(ICUDATA) > -1) {
                        int idx = bundleName.indexOf(45);
                        if (idx > -1) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("android/icu/impl/data/icudt60b/");
                            ClassLoader classLoader = loaderToUse2;
                            sb.append(bundleName.substring(idx + 1, bundleName.length()));
                            bundleName = sb.toString();
                            loaderToUse = ICU_DATA_CLASS_LOADER;
                        }
                    }
                    loaderToUse = loaderToUse2;
                }
            } else {
                ClassLoader loaderToUse3 = loaderToUse2;
                int i2 = rpath.indexOf(47);
                if (i2 != -1) {
                    locale2 = rpath.substring(0, i2);
                    keyPath = rpath.substring(i2 + 1);
                } else {
                    locale2 = rpath;
                }
                locale = locale2;
                bundleName = wholeBundle2.baseName;
                loaderToUse = loaderToUse3;
            }
            String bundleName2 = bundleName;
            ICUResourceBundle sub = null;
            if (bundleName2.equals(LOCALE)) {
                String bundleName3 = wholeBundle2.baseName;
                String keyPath2 = rpath.substring(LOCALE.length() + 2, rpath.length());
                ICUResourceBundle bundle = (ICUResourceBundle) uResourceBundle;
                while (bundle.container != null) {
                    bundle = bundle.container;
                }
                sub = findResourceWithFallback(keyPath2, bundle, null);
                String[] strArr = keys;
                int i3 = depth;
                ClassLoader classLoader2 = loaderToUse;
            } else {
                ICUResourceBundle bundle2 = getBundleInstance(bundleName2, locale, loaderToUse, false);
                if (keyPath != null) {
                    numKeys = countPathKeys(keyPath);
                    if (numKeys > 0) {
                        keys2 = new String[numKeys];
                        getResPathKeys(keyPath, numKeys, keys2, 0);
                    } else {
                        keys2 = keys;
                    }
                } else if (keys != null) {
                    int i4 = depth;
                    numKeys = depth;
                    keys2 = keys;
                } else {
                    int numKeys2 = base.getResDepth();
                    int numKeys3 = numKeys2 + 1;
                    String[] keys3 = new String[numKeys3];
                    iCUResourceBundle.getResPathKeys(keys3, numKeys2);
                    keys3[numKeys2] = str;
                    String[] strArr2 = keys3;
                    numKeys = numKeys3;
                    keys2 = strArr2;
                }
                if (numKeys > 0) {
                    sub = bundle2;
                    int i5 = 0;
                    while (true) {
                        int i6 = i5;
                        if (sub == null) {
                            break;
                        }
                        int i7 = i6;
                        if (i7 >= numKeys) {
                            break;
                        }
                        sub = sub.get(keys2[i7], aliasesVisited2, uResourceBundle);
                        i5 = i7 + 1;
                        loaderToUse = loaderToUse;
                        ICUResourceBundle iCUResourceBundle2 = base;
                    }
                }
            }
            if (sub != null) {
                return sub;
            }
            throw new MissingResourceException(wholeBundle2.localeID, wholeBundle2.baseName, str);
        }
        ClassLoader classLoader3 = loaderToUse2;
        throw new IllegalArgumentException("Circular references in the resource bundles");
    }

    @Deprecated
    public final Set<String> getTopLevelKeySet() {
        return this.wholeBundle.topLevelKeys;
    }

    @Deprecated
    public final void setTopLevelKeySet(Set<String> keySet) {
        this.wholeBundle.topLevelKeys = keySet;
    }

    /* access modifiers changed from: protected */
    public Enumeration<String> handleGetKeys() {
        return Collections.enumeration(handleKeySet());
    }

    /* access modifiers changed from: protected */
    public boolean isTopLevelResource() {
        return this.container == null;
    }
}
