package ohos.global.icu.impl;

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
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.icu.impl.ICUResourceBundleImpl;
import ohos.global.icu.impl.ICUResourceBundleReader;
import ohos.global.icu.impl.URLHandler;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;
import ohos.global.icu.util.UResourceBundleIterator;
import ohos.global.icu.util.UResourceTypeMismatchException;

public class ICUResourceBundle extends UResourceBundle {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int ALIAS = 3;
    public static final int ARRAY16 = 9;
    private static CacheBase<String, ICUResourceBundle, Loader> BUNDLE_CACHE = new SoftCache<String, ICUResourceBundle, Loader>() {
        /* class ohos.global.icu.impl.ICUResourceBundle.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public ICUResourceBundle createInstance(String str, Loader loader) {
            return loader.load();
        }
    };
    private static final boolean DEBUG = ICUDebug.enabled("localedata");
    private static final String DEFAULT_TAG = "default";
    private static final String FULL_LOCALE_NAMES_LIST = "fullLocaleNames.lst";
    private static CacheBase<String, AvailEntry, ClassLoader> GET_AVAILABLE_CACHE = new SoftCache<String, AvailEntry, ClassLoader>() {
        /* class ohos.global.icu.impl.ICUResourceBundle.AnonymousClass3 */

        /* access modifiers changed from: protected */
        public AvailEntry createInstance(String str, ClassLoader classLoader) {
            return new AvailEntry(str, classLoader);
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

    public enum OpenType {
        LOCALE_DEFAULT_ROOT,
        LOCALE_ROOT,
        LOCALE_ONLY,
        DIRECT
    }

    public int hashCode() {
        return 42;
    }

    /* access modifiers changed from: protected */
    public static final class WholeBundle {
        String baseName;
        ClassLoader loader;
        String localeID;
        ICUResourceBundleReader reader;
        Set<String> topLevelKeys;
        ULocale ulocale;

        WholeBundle(String str, String str2, ClassLoader classLoader, ICUResourceBundleReader iCUResourceBundleReader) {
            this.baseName = str;
            this.localeID = str2;
            this.ulocale = new ULocale(str2);
            this.loader = classLoader;
            this.reader = iCUResourceBundleReader;
        }
    }

    /* access modifiers changed from: private */
    public static abstract class Loader {
        /* access modifiers changed from: package-private */
        public abstract ICUResourceBundle load();

        private Loader() {
        }
    }

    public static final ULocale getFunctionalEquivalent(String str, ClassLoader classLoader, String str2, String str3, ULocale uLocale, boolean[] zArr, boolean z) {
        String str4;
        boolean z2;
        String str5;
        String keywordValue = uLocale.getKeywordValue(str3);
        String baseName = uLocale.getBaseName();
        ULocale uLocale2 = new ULocale(baseName);
        int i = 0;
        if (keywordValue == null || keywordValue.length() == 0 || keywordValue.equals("default")) {
            str4 = "";
            z2 = true;
        } else {
            str4 = keywordValue;
            z2 = false;
        }
        ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(str, uLocale2);
        if (zArr != null) {
            zArr[0] = false;
            ULocale[] uLocaleList = getAvailEntry(str, classLoader).getULocaleList(ULocale.AvailableType.DEFAULT);
            int i2 = 0;
            while (true) {
                if (i2 >= uLocaleList.length) {
                    break;
                } else if (uLocale2.equals(uLocaleList[i2])) {
                    zArr[0] = true;
                    break;
                } else {
                    i2++;
                }
            }
        }
        ULocale uLocale3 = null;
        String str6 = null;
        int i3 = 0;
        do {
            try {
                str6 = bundleInstance.get(str2).getString("default");
                if (z2) {
                    z2 = false;
                    str4 = str6;
                }
                uLocale3 = bundleInstance.getULocale();
            } catch (MissingResourceException unused) {
            }
            if (uLocale3 == null) {
                bundleInstance = bundleInstance.getParent();
                i3++;
            }
            if (bundleInstance == null) {
                break;
            }
        } while (uLocale3 == null);
        ICUResourceBundle bundleInstance2 = UResourceBundle.getBundleInstance(str, new ULocale(baseName));
        ULocale uLocale4 = null;
        int i4 = i3;
        int i5 = 0;
        do {
            try {
                ICUResourceBundle iCUResourceBundle = bundleInstance2.get(str2);
                iCUResourceBundle.get(str4);
                uLocale4 = iCUResourceBundle.getULocale();
                if (uLocale4 != null && i5 > i4) {
                    str6 = iCUResourceBundle.getString("default");
                    bundleInstance2.getULocale();
                    i4 = i5;
                }
            } catch (MissingResourceException unused2) {
            }
            if (uLocale4 == null) {
                bundleInstance2 = bundleInstance2.getParent();
                i5++;
            }
            if (bundleInstance2 == null) {
                break;
            }
        } while (uLocale4 == null);
        if (uLocale4 != null || str6 == null || str6.equals(str4)) {
            i = i5;
            str5 = str6;
        } else {
            ICUResourceBundle bundleInstance3 = UResourceBundle.getBundleInstance(str, new ULocale(baseName));
            str5 = str6;
            do {
                try {
                    ICUResourceBundle iCUResourceBundle2 = bundleInstance3.get(str2);
                    uLocale4 = bundleInstance3.getULocale();
                    if (!uLocale4.getBaseName().equals(iCUResourceBundle2.get(str6).getULocale().getBaseName())) {
                        uLocale4 = null;
                    }
                    if (uLocale4 != null && i > i4) {
                        str5 = iCUResourceBundle2.getString("default");
                        bundleInstance3.getULocale();
                        i4 = i;
                    }
                } catch (MissingResourceException unused3) {
                }
                if (uLocale4 == null) {
                    bundleInstance3 = bundleInstance3.getParent();
                    i++;
                }
                if (bundleInstance3 == null) {
                    break;
                }
            } while (uLocale4 == null);
            str4 = str6;
        }
        if (uLocale4 == null) {
            throw new MissingResourceException("Could not find locale containing requested or default keyword.", str, str3 + "=" + str4);
        } else if (z && str5.equals(str4) && i <= i4) {
            return uLocale4;
        } else {
            return new ULocale(uLocale4.getBaseName() + "@" + str3 + "=" + str4);
        }
    }

    public static final String[] getKeywordValues(String str, String str2) {
        HashSet hashSet = new HashSet();
        ULocale[] uLocaleList = getAvailEntry(str, ICU_DATA_CLASS_LOADER).getULocaleList(ULocale.AvailableType.DEFAULT);
        for (int i = 0; i < uLocaleList.length; i++) {
            try {
                Enumeration keys = ((ICUResourceBundle) UResourceBundle.getBundleInstance(str, uLocaleList[i]).getObject(str2)).getKeys();
                while (keys.hasMoreElements()) {
                    String str3 = (String) keys.nextElement();
                    if (!"default".equals(str3) && !str3.startsWith("private-")) {
                        hashSet.add(str3);
                    }
                }
            } catch (Throwable unused) {
            }
        }
        return (String[]) hashSet.toArray(new String[0]);
    }

    public ICUResourceBundle getWithFallback(String str) throws MissingResourceException {
        ICUResourceBundle findResourceWithFallback = findResourceWithFallback(str, this, null);
        if (findResourceWithFallback == null) {
            throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getType(), str, getKey());
        } else if (findResourceWithFallback.getType() != 0 || !findResourceWithFallback.getString().equals(NO_INHERITANCE_MARKER)) {
            return findResourceWithFallback;
        } else {
            throw new MissingResourceException("Encountered NO_INHERITANCE_MARKER", str, getKey());
        }
    }

    public ICUResourceBundle at(int i) {
        return handleGet(i, null, this);
    }

    public ICUResourceBundle at(String str) {
        if (this instanceof ICUResourceBundleImpl.ResourceTable) {
            return handleGet(str, null, this);
        }
        return null;
    }

    public ICUResourceBundle findTopLevel(int i) {
        return ICUResourceBundle.super.findTopLevel(i);
    }

    public ICUResourceBundle findTopLevel(String str) {
        return ICUResourceBundle.super.findTopLevel(str);
    }

    public ICUResourceBundle findWithFallback(String str) {
        return findResourceWithFallback(str, this, null);
    }

    public String findStringWithFallback(String str) {
        return findStringWithFallback(str, this, null);
    }

    public String getStringWithFallback(String str) throws MissingResourceException {
        String findStringWithFallback = findStringWithFallback(str, this, null);
        if (findStringWithFallback == null) {
            throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getType(), str, getKey());
        } else if (!findStringWithFallback.equals(NO_INHERITANCE_MARKER)) {
            return findStringWithFallback;
        } else {
            throw new MissingResourceException("Encountered NO_INHERITANCE_MARKER", str, getKey());
        }
    }

    public UResource.Value getValueWithFallback(String str) throws MissingResourceException {
        if (!str.isEmpty()) {
            ICUResourceBundle findResourceWithFallback = findResourceWithFallback(str, this, null);
            if (findResourceWithFallback != null) {
                this = findResourceWithFallback;
            } else {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getType(), str, getKey());
            }
        }
        ICUResourceBundleReader.ReaderValue readerValue = new ICUResourceBundleReader.ReaderValue();
        ICUResourceBundleImpl iCUResourceBundleImpl = (ICUResourceBundleImpl) this;
        readerValue.reader = iCUResourceBundleImpl.wholeBundle.reader;
        readerValue.res = iCUResourceBundleImpl.getResource();
        return readerValue;
    }

    public void getAllItemsWithFallbackNoFail(String str, UResource.Sink sink) {
        try {
            getAllItemsWithFallback(str, sink);
        } catch (MissingResourceException unused) {
        }
    }

    public void getAllItemsWithFallback(String str, UResource.Sink sink) throws MissingResourceException {
        int countPathKeys = countPathKeys(str);
        if (countPathKeys != 0) {
            int resDepth = getResDepth();
            String[] strArr = new String[(resDepth + countPathKeys)];
            getResPathKeys(str, countPathKeys, strArr, resDepth);
            ICUResourceBundle findResourceWithFallback = findResourceWithFallback(strArr, resDepth, this, null);
            if (findResourceWithFallback != null) {
                this = findResourceWithFallback;
            } else {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getType(), str, getKey());
            }
        }
        this.getAllItemsWithFallback(new UResource.Key(), new ICUResourceBundleReader.ReaderValue(), sink);
    }

    private void getAllItemsWithFallback(UResource.Key key2, ICUResourceBundleReader.ReaderValue readerValue, UResource.Sink sink) {
        ICUResourceBundleImpl iCUResourceBundleImpl = (ICUResourceBundleImpl) this;
        readerValue.reader = iCUResourceBundleImpl.wholeBundle.reader;
        readerValue.res = iCUResourceBundleImpl.getResource();
        String str = this.key;
        if (str == null) {
            str = "";
        }
        key2.setString(str);
        sink.put(key2, readerValue, this.parent == null);
        if (this.parent != null) {
            ICUResourceBundle iCUResourceBundle = (ICUResourceBundle) this.parent;
            int resDepth = getResDepth();
            if (resDepth != 0) {
                String[] strArr = new String[resDepth];
                getResPathKeys(strArr, resDepth);
                iCUResourceBundle = findResourceWithFallback(strArr, 0, iCUResourceBundle, null);
            }
            if (iCUResourceBundle != null) {
                iCUResourceBundle.getAllItemsWithFallback(key2, readerValue, sink);
            }
        }
    }

    public static Set<String> getAvailableLocaleNameSet(String str, ClassLoader classLoader) {
        return getAvailEntry(str, classLoader).getLocaleNameSet();
    }

    public static Set<String> getFullLocaleNameSet() {
        return getFullLocaleNameSet(ICUData.ICU_BASE_NAME, ICU_DATA_CLASS_LOADER);
    }

    public static Set<String> getFullLocaleNameSet(String str, ClassLoader classLoader) {
        return getAvailEntry(str, classLoader).getFullLocaleNameSet();
    }

    public static Set<String> getAvailableLocaleNameSet() {
        return getAvailableLocaleNameSet(ICUData.ICU_BASE_NAME, ICU_DATA_CLASS_LOADER);
    }

    public static final ULocale[] getAvailableULocales(String str, ClassLoader classLoader, ULocale.AvailableType availableType) {
        return getAvailEntry(str, classLoader).getULocaleList(availableType);
    }

    public static final ULocale[] getAvailableULocales() {
        return getAvailableULocales(ICUData.ICU_BASE_NAME, ICU_DATA_CLASS_LOADER, ULocale.AvailableType.DEFAULT);
    }

    public static final ULocale[] getAvailableULocales(ULocale.AvailableType availableType) {
        return getAvailableULocales(ICUData.ICU_BASE_NAME, ICU_DATA_CLASS_LOADER, availableType);
    }

    public static final ULocale[] getAvailableULocales(String str, ClassLoader classLoader) {
        return getAvailableULocales(str, classLoader, ULocale.AvailableType.DEFAULT);
    }

    public static final Locale[] getAvailableLocales(String str, ClassLoader classLoader, ULocale.AvailableType availableType) {
        return getAvailEntry(str, classLoader).getLocaleList(availableType);
    }

    public static final Locale[] getAvailableLocales() {
        return getAvailableLocales(ICUData.ICU_BASE_NAME, ICU_DATA_CLASS_LOADER, ULocale.AvailableType.DEFAULT);
    }

    public static final Locale[] getAvailableLocales(ULocale.AvailableType availableType) {
        return getAvailableLocales(ICUData.ICU_BASE_NAME, ICU_DATA_CLASS_LOADER, availableType);
    }

    public static final Locale[] getAvailableLocales(String str, ClassLoader classLoader) {
        return getAvailableLocales(str, classLoader, ULocale.AvailableType.DEFAULT);
    }

    public static final Locale[] getLocaleList(ULocale[] uLocaleArr) {
        ArrayList arrayList = new ArrayList(uLocaleArr.length);
        HashSet hashSet = new HashSet();
        for (ULocale uLocale : uLocaleArr) {
            Locale locale = uLocale.toLocale();
            if (!hashSet.contains(locale)) {
                arrayList.add(locale);
                hashSet.add(locale);
            }
        }
        return (Locale[]) arrayList.toArray(new Locale[arrayList.size()]);
    }

    public Locale getLocale() {
        return getULocale().toLocale();
    }

    /* access modifiers changed from: private */
    public static final class AvailableLocalesSink extends UResource.Sink {
        EnumMap<ULocale.AvailableType, ULocale[]> output;

        public AvailableLocalesSink(EnumMap<ULocale.AvailableType, ULocale[]> enumMap) {
            this.output = enumMap;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            ULocale.AvailableType availableType;
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                if (key.contentEquals(ICUResourceBundle.INSTALLED_LOCALES)) {
                    availableType = ULocale.AvailableType.DEFAULT;
                } else if (key.contentEquals("AliasLocales")) {
                    availableType = ULocale.AvailableType.ONLY_LEGACY_ALIASES;
                }
                UResource.Table table2 = value.getTable();
                ULocale[] uLocaleArr = new ULocale[table2.getSize()];
                for (int i2 = 0; table2.getKeyAndValue(i2, key, value); i2++) {
                    uLocaleArr[i2] = new ULocale(key.toString());
                }
                this.output.put((EnumMap<ULocale.AvailableType, ULocale[]>) availableType, (ULocale.AvailableType) uLocaleArr);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final EnumMap<ULocale.AvailableType, ULocale[]> createULocaleList(String str, ClassLoader classLoader) {
        EnumMap<ULocale.AvailableType, ULocale[]> enumMap = new EnumMap<>(ULocale.AvailableType.class);
        UResourceBundle.instantiateBundle(str, ICU_RESOURCE_INDEX, classLoader, true).getAllItemsWithFallback("", new AvailableLocalesSink(enumMap));
        return enumMap;
    }

    private static final void addLocaleIDsFromIndexBundle(String str, ClassLoader classLoader, Set<String> set) {
        try {
            UResourceBundleIterator iterator = UResourceBundle.instantiateBundle(str, ICU_RESOURCE_INDEX, classLoader, true).get(INSTALLED_LOCALES).getIterator();
            iterator.reset();
            while (iterator.hasNext()) {
                set.add(iterator.next().getKey());
            }
        } catch (MissingResourceException unused) {
            if (DEBUG) {
                PrintStream printStream = System.out;
                printStream.println("couldn't find " + str + RES_PATH_SEP_CHAR + ICU_RESOURCE_INDEX + ".res");
                Thread.dumpStack();
            }
        }
    }

    private static final void addBundleBaseNamesFromClassLoader(final String str, final ClassLoader classLoader, final Set<String> set) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            /* class ohos.global.icu.impl.ICUResourceBundle.AnonymousClass2 */

            @Override // java.security.PrivilegedAction
            public Void run() {
                try {
                    Enumeration<URL> resources = classLoader.getResources(str);
                    if (resources == null) {
                        return null;
                    }
                    AnonymousClass1 r2 = new URLHandler.URLVisitor() {
                        /* class ohos.global.icu.impl.ICUResourceBundle.AnonymousClass2.AnonymousClass1 */

                        @Override // ohos.global.icu.impl.URLHandler.URLVisitor
                        public void visit(String str) {
                            if (str.endsWith(".res")) {
                                set.add(str.substring(0, str.length() - 4));
                            }
                        }
                    };
                    while (resources.hasMoreElements()) {
                        URL nextElement = resources.nextElement();
                        URLHandler uRLHandler = URLHandler.get(nextElement);
                        if (uRLHandler != null) {
                            uRLHandler.guide(r2, false);
                        } else if (ICUResourceBundle.DEBUG) {
                            PrintStream printStream = System.out;
                            printStream.println("handler for " + nextElement + " is null");
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

    private static void addLocaleIDsFromListFile(String str, ClassLoader classLoader, Set<String> set) {
        try {
            InputStream resourceAsStream = classLoader.getResourceAsStream(str + FULL_LOCALE_NAMES_LIST);
            if (resourceAsStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, "ASCII"));
                while (true) {
                    try {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            return;
                        }
                        if (readLine.length() != 0 && !readLine.startsWith("#")) {
                            set.add(readLine);
                        }
                    } finally {
                        bufferedReader.close();
                    }
                }
            }
        } catch (IOException unused) {
        }
    }

    /* access modifiers changed from: private */
    public static Set<String> createFullLocaleNameSet(String str, ClassLoader classLoader) {
        String str2;
        String str3;
        if (str.endsWith("/")) {
            str2 = str;
        } else {
            str2 = str + "/";
        }
        HashSet hashSet = new HashSet();
        if (!ICUConfig.get("ohos.global.icu.impl.ICUResourceBundle.skipRuntimeLocaleResourceScan", "false").equalsIgnoreCase("true")) {
            addBundleBaseNamesFromClassLoader(str2, classLoader, hashSet);
            if (str.startsWith(ICUData.ICU_BASE_NAME)) {
                if (str.length() == 34) {
                    str3 = "";
                } else {
                    str3 = str.charAt(34) == '/' ? str.substring(35) : null;
                }
                if (str3 != null) {
                    ICUBinary.addBaseNamesInFileFolder(str3, ".res", hashSet);
                }
            }
            hashSet.remove(ICU_RESOURCE_INDEX);
            Iterator it = hashSet.iterator();
            while (it.hasNext()) {
                String str4 = (String) it.next();
                if ((str4.length() == 1 || str4.length() > 3) && str4.indexOf(95) < 0) {
                    it.remove();
                }
            }
        }
        if (hashSet.isEmpty()) {
            if (DEBUG) {
                System.out.println("unable to enumerate data files in " + str);
            }
            addLocaleIDsFromListFile(str2, classLoader, hashSet);
        }
        if (hashSet.isEmpty()) {
            addLocaleIDsFromIndexBundle(str, classLoader, hashSet);
        }
        hashSet.remove(Constants.ELEMNAME_ROOT_STRING);
        hashSet.add(ULocale.ROOT.toString());
        return Collections.unmodifiableSet(hashSet);
    }

    /* access modifiers changed from: private */
    public static Set<String> createLocaleNameSet(String str, ClassLoader classLoader) {
        HashSet hashSet = new HashSet();
        addLocaleIDsFromIndexBundle(str, classLoader, hashSet);
        return Collections.unmodifiableSet(hashSet);
    }

    /* access modifiers changed from: private */
    public static final class AvailEntry {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private volatile Set<String> fullNameSet;
        private ClassLoader loader;
        private volatile Locale[] locales;
        private volatile Set<String> nameSet;
        private String prefix;
        private volatile EnumMap<ULocale.AvailableType, ULocale[]> ulocales;

        AvailEntry(String str, ClassLoader classLoader) {
            this.prefix = str;
            this.loader = classLoader;
        }

        /* access modifiers changed from: package-private */
        public ULocale[] getULocaleList(ULocale.AvailableType availableType) {
            if (this.ulocales == null) {
                synchronized (this) {
                    if (this.ulocales == null) {
                        this.ulocales = ICUResourceBundle.createULocaleList(this.prefix, this.loader);
                    }
                }
            }
            return this.ulocales.get(availableType);
        }

        /* access modifiers changed from: package-private */
        public Locale[] getLocaleList(ULocale.AvailableType availableType) {
            if (this.locales == null) {
                getULocaleList(availableType);
                synchronized (this) {
                    if (this.locales == null) {
                        this.locales = ICUResourceBundle.getLocaleList(this.ulocales.get(availableType));
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

    private static AvailEntry getAvailEntry(String str, ClassLoader classLoader) {
        return GET_AVAILABLE_CACHE.getInstance(str, classLoader);
    }

    private static final ICUResourceBundle findResourceWithFallback(String str, UResourceBundle uResourceBundle, UResourceBundle uResourceBundle2) {
        if (str.length() == 0) {
            return null;
        }
        ICUResourceBundle iCUResourceBundle = (ICUResourceBundle) uResourceBundle;
        int resDepth = iCUResourceBundle.getResDepth();
        int countPathKeys = countPathKeys(str);
        String[] strArr = new String[(resDepth + countPathKeys)];
        getResPathKeys(str, countPathKeys, strArr, resDepth);
        return findResourceWithFallback(strArr, resDepth, iCUResourceBundle, uResourceBundle2);
    }

    private static final ICUResourceBundle findResourceWithFallback(String[] strArr, int i, ICUResourceBundle iCUResourceBundle, UResourceBundle uResourceBundle) {
        if (uResourceBundle == null) {
            uResourceBundle = iCUResourceBundle;
        }
        while (true) {
            int i2 = i + 1;
            ICUResourceBundle iCUResourceBundle2 = (ICUResourceBundle) iCUResourceBundle.handleGet(strArr[i], null, uResourceBundle);
            if (iCUResourceBundle2 == null) {
                int i3 = i2 - 1;
                ICUResourceBundle parent = iCUResourceBundle.getParent();
                if (parent == null) {
                    return null;
                }
                int resDepth = iCUResourceBundle.getResDepth();
                if (i3 != resDepth) {
                    String[] strArr2 = new String[((strArr.length - i3) + resDepth)];
                    System.arraycopy(strArr, i3, strArr2, resDepth, strArr.length - i3);
                    strArr = strArr2;
                }
                iCUResourceBundle.getResPathKeys(strArr, resDepth);
                iCUResourceBundle = parent;
                i = 0;
            } else if (i2 == strArr.length) {
                return iCUResourceBundle2;
            } else {
                iCUResourceBundle = iCUResourceBundle2;
                i = i2;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00bd A[SYNTHETIC] */
    private static final String findStringWithFallback(String str, UResourceBundle uResourceBundle, UResourceBundle uResourceBundle2) {
        ICUResourceBundle iCUResourceBundle;
        ICUResourceBundleReader.Container container2;
        int i;
        ICUResourceBundleReader iCUResourceBundleReader;
        int resDepth;
        if (str.length() == 0 || !(uResourceBundle instanceof ICUResourceBundleImpl.ResourceContainer)) {
            return null;
        }
        if (uResourceBundle2 == null) {
            uResourceBundle2 = uResourceBundle;
        }
        ICUResourceBundle iCUResourceBundle2 = (ICUResourceBundle) uResourceBundle;
        ICUResourceBundleReader iCUResourceBundleReader2 = iCUResourceBundle2.wholeBundle.reader;
        int resDepth2 = iCUResourceBundle2.getResDepth();
        int countPathKeys = countPathKeys(str);
        String[] strArr = new String[(resDepth2 + countPathKeys)];
        getResPathKeys(str, countPathKeys, strArr, resDepth2);
        ICUResourceBundleReader iCUResourceBundleReader3 = iCUResourceBundleReader2;
        int i2 = resDepth2;
        String[] strArr2 = strArr;
        ICUResourceBundle iCUResourceBundle3 = iCUResourceBundle2;
        while (true) {
            int i3 = -1;
            while (true) {
                if (i3 != -1) {
                    int RES_GET_TYPE = ICUResourceBundleReader.RES_GET_TYPE(i3);
                    if (ICUResourceBundleReader.URES_IS_TABLE(RES_GET_TYPE)) {
                        container2 = iCUResourceBundleReader3.getTable(i3);
                    } else if (ICUResourceBundleReader.URES_IS_ARRAY(RES_GET_TYPE)) {
                        container2 = iCUResourceBundleReader3.getArray(i3);
                    } else {
                        i3 = -1;
                        iCUResourceBundle = iCUResourceBundle3.getParent();
                        if (iCUResourceBundle == null) {
                            return null;
                        }
                        iCUResourceBundle3.getResPathKeys(strArr2, i2);
                        iCUResourceBundleReader3 = iCUResourceBundle.wholeBundle.reader;
                        i2 = 0;
                        iCUResourceBundle3 = iCUResourceBundle;
                        resDepth2 = i2;
                    }
                } else {
                    int type = iCUResourceBundle3.getType();
                    if (type == 2 || type == 8) {
                        container2 = ((ICUResourceBundleImpl.ResourceContainer) iCUResourceBundle3).value;
                    }
                    iCUResourceBundle = iCUResourceBundle3.getParent();
                    if (iCUResourceBundle == null) {
                    }
                }
                i = resDepth2 + 1;
                String str2 = strArr2[resDepth2];
                i3 = container2.getResource(iCUResourceBundleReader3, str2);
                if (i3 != -1) {
                    if (ICUResourceBundleReader.RES_GET_TYPE(i3) == 3) {
                        iCUResourceBundle3.getResPathKeys(strArr2, i2);
                        iCUResourceBundle = getAliasedResource(iCUResourceBundle3, strArr2, i, str2, i3, null, uResourceBundle2);
                    } else {
                        iCUResourceBundle = null;
                    }
                    if (i == strArr2.length) {
                        if (iCUResourceBundle != null) {
                            return iCUResourceBundle.getString();
                        }
                        String string = iCUResourceBundleReader3.getString(i3);
                        if (string != null) {
                            return string;
                        }
                        throw new UResourceTypeMismatchException("");
                    } else if (iCUResourceBundle != null) {
                        iCUResourceBundleReader = iCUResourceBundle.wholeBundle.reader;
                        resDepth = iCUResourceBundle.getResDepth();
                        if (i == resDepth) {
                            break;
                        }
                        String[] strArr3 = new String[((strArr2.length - i) + resDepth)];
                        System.arraycopy(strArr2, i, strArr3, resDepth, strArr2.length - i);
                        iCUResourceBundleReader3 = iCUResourceBundleReader;
                        i2 = resDepth;
                        strArr2 = strArr3;
                        i3 = -1;
                        iCUResourceBundle3 = iCUResourceBundle;
                        resDepth2 = i2;
                    } else {
                        resDepth2 = i;
                    }
                }
                iCUResourceBundle = iCUResourceBundle3.getParent();
                if (iCUResourceBundle == null) {
                }
            }
            iCUResourceBundleReader3 = iCUResourceBundleReader;
            i2 = resDepth;
            iCUResourceBundle3 = iCUResourceBundle;
            resDepth2 = i;
        }
    }

    private int getResDepth() {
        ICUResourceBundle iCUResourceBundle = this.container;
        if (iCUResourceBundle == null) {
            return 0;
        }
        return iCUResourceBundle.getResDepth() + 1;
    }

    private void getResPathKeys(String[] strArr, int i) {
        while (i > 0) {
            i--;
            strArr[i] = this.key;
            this = this.container;
        }
    }

    private static int countPathKeys(String str) {
        if (str.isEmpty()) {
            return 0;
        }
        int i = 1;
        for (int i2 = 0; i2 < str.length(); i2++) {
            if (str.charAt(i2) == '/') {
                i++;
            }
        }
        return i;
    }

    private static void getResPathKeys(String str, int i, String[] strArr, int i2) {
        if (i != 0) {
            if (i == 1) {
                strArr[i2] = str;
                return;
            }
            int i3 = 0;
            while (true) {
                int indexOf = str.indexOf(47, i3);
                int i4 = i2 + 1;
                strArr[i2] = str.substring(i3, indexOf);
                if (i == 2) {
                    strArr[i4] = str.substring(indexOf + 1);
                    return;
                }
                i3 = indexOf + 1;
                i--;
                i2 = i4;
            }
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ICUResourceBundle)) {
            return false;
        }
        ICUResourceBundle iCUResourceBundle = (ICUResourceBundle) obj;
        if (!getBaseName().equals(iCUResourceBundle.getBaseName()) || !getLocaleID().equals(iCUResourceBundle.getLocaleID())) {
            return false;
        }
        return true;
    }

    public static ICUResourceBundle getBundleInstance(String str, String str2, ClassLoader classLoader, boolean z) {
        return getBundleInstance(str, str2, classLoader, z ? OpenType.DIRECT : OpenType.LOCALE_DEFAULT_ROOT);
    }

    public static ICUResourceBundle getBundleInstance(String str, ULocale uLocale, OpenType openType) {
        if (uLocale == null) {
            uLocale = ULocale.getDefault();
        }
        return getBundleInstance(str, uLocale.getBaseName(), ICU_DATA_CLASS_LOADER, openType);
    }

    public static ICUResourceBundle getBundleInstance(String str, String str2, ClassLoader classLoader, OpenType openType) {
        ICUResourceBundle iCUResourceBundle;
        if (str == null) {
            str = ICUData.ICU_BASE_NAME;
        }
        String baseName = ULocale.getBaseName(str2);
        if (openType == OpenType.LOCALE_DEFAULT_ROOT) {
            iCUResourceBundle = instantiateBundle(str, baseName, ULocale.getDefault().getBaseName(), classLoader, openType);
        } else {
            iCUResourceBundle = instantiateBundle(str, baseName, null, classLoader, openType);
        }
        if (iCUResourceBundle != null) {
            return iCUResourceBundle;
        }
        throw new MissingResourceException("Could not find the bundle " + str + "/" + baseName + ".res", "", "");
    }

    /* access modifiers changed from: private */
    public static boolean localeIDStartsWithLangSubtag(String str, String str2) {
        return str.startsWith(str2) && (str.length() == str2.length() || str.charAt(str2.length()) == '_');
    }

    /* access modifiers changed from: private */
    public static ICUResourceBundle instantiateBundle(final String str, final String str2, final String str3, final ClassLoader classLoader, final OpenType openType) {
        String str4;
        final String fullName = ICUResourceBundleReader.getFullName(str, str2);
        char ordinal = (char) (openType.ordinal() + 48);
        if (openType != OpenType.LOCALE_DEFAULT_ROOT) {
            str4 = fullName + '#' + ordinal;
        } else {
            str4 = fullName + '#' + ordinal + '#' + str3;
        }
        return BUNDLE_CACHE.getInstance(str4, new Loader() {
            /* class ohos.global.icu.impl.ICUResourceBundle.AnonymousClass4 */

            /* JADX DEBUG: Multi-variable search result rejected for r2v6, resolved type: ohos.global.icu.impl.ICUResourceBundle */
            /* JADX DEBUG: Multi-variable search result rejected for r2v8, resolved type: ohos.global.icu.impl.ICUResourceBundle */
            /* JADX DEBUG: Multi-variable search result rejected for r2v10, resolved type: ohos.global.icu.impl.ICUResourceBundle */
            /* JADX WARN: Multi-variable type inference failed */
            @Override // ohos.global.icu.impl.ICUResourceBundle.Loader
            public ICUResourceBundle load() {
                if (ICUResourceBundle.DEBUG) {
                    PrintStream printStream = System.out;
                    printStream.println("Creating " + fullName);
                }
                String str = str.indexOf(46) == -1 ? Constants.ELEMNAME_ROOT_STRING : "";
                String str2 = str2.isEmpty() ? str : str2;
                ICUResourceBundle createBundle = ICUResourceBundle.createBundle(str, str2, classLoader);
                if (ICUResourceBundle.DEBUG) {
                    PrintStream printStream2 = System.out;
                    StringBuilder sb = new StringBuilder();
                    sb.append("The bundle created is: ");
                    sb.append(createBundle);
                    sb.append(" and openType=");
                    sb.append(openType);
                    sb.append(" and bundle.getNoFallback=");
                    sb.append(createBundle != null && createBundle.getNoFallback());
                    printStream2.println(sb.toString());
                }
                if (openType == OpenType.DIRECT) {
                    return createBundle;
                }
                if (createBundle != null && createBundle.getNoFallback()) {
                    return createBundle;
                }
                if (createBundle == null) {
                    int lastIndexOf = str2.lastIndexOf(95);
                    if (lastIndexOf != -1) {
                        return ICUResourceBundle.instantiateBundle(str, str2.substring(0, lastIndexOf), str3, classLoader, openType);
                    } else if (openType == OpenType.LOCALE_DEFAULT_ROOT && !ICUResourceBundle.localeIDStartsWithLangSubtag(str3, str2)) {
                        String str3 = str;
                        String str4 = str3;
                        return ICUResourceBundle.instantiateBundle(str3, str4, str4, classLoader, openType);
                    } else if (openType == OpenType.LOCALE_ONLY || str.isEmpty()) {
                        return createBundle;
                    } else {
                        return ICUResourceBundle.createBundle(str, str, classLoader);
                    }
                } else {
                    ResourceBundle resourceBundle = null;
                    String localeID = createBundle.getLocaleID();
                    int lastIndexOf2 = localeID.lastIndexOf(95);
                    String findString = ((ICUResourceBundleImpl.ResourceTable) createBundle).findString("%%Parent");
                    if (findString != null) {
                        resourceBundle = ICUResourceBundle.instantiateBundle(str, findString, str3, classLoader, openType);
                    } else if (lastIndexOf2 != -1) {
                        resourceBundle = ICUResourceBundle.instantiateBundle(str, localeID.substring(0, lastIndexOf2), str3, classLoader, openType);
                    } else if (!localeID.equals(str)) {
                        resourceBundle = ICUResourceBundle.instantiateBundle(str, str, str3, classLoader, openType);
                    }
                    if (createBundle.equals(resourceBundle)) {
                        return createBundle;
                    }
                    createBundle.setParent(resourceBundle);
                    return createBundle;
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public ICUResourceBundle get(String str, HashMap<String, String> hashMap, UResourceBundle uResourceBundle) {
        ICUResourceBundle iCUResourceBundle = (ICUResourceBundle) handleGet(str, hashMap, uResourceBundle);
        if (iCUResourceBundle == null) {
            iCUResourceBundle = getParent();
            if (iCUResourceBundle != null) {
                iCUResourceBundle = iCUResourceBundle.get(str, hashMap, uResourceBundle);
            }
            if (iCUResourceBundle == null) {
                String fullName = ICUResourceBundleReader.getFullName(getBaseName(), getLocaleID());
                throw new MissingResourceException("Can't find resource for bundle " + fullName + ", key " + str, getClass().getName(), str);
            }
        }
        return iCUResourceBundle;
    }

    public static ICUResourceBundle createBundle(String str, String str2, ClassLoader classLoader) {
        ICUResourceBundleReader reader = ICUResourceBundleReader.getReader(str, str2, classLoader);
        if (reader == null) {
            return null;
        }
        return getBundle(reader, str, str2, classLoader);
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
        return this.wholeBundle.localeID.isEmpty() || this.wholeBundle.localeID.equals(Constants.ELEMNAME_ROOT_STRING);
    }

    public ICUResourceBundle getParent() {
        return (ICUResourceBundle) this.parent;
    }

    /* access modifiers changed from: protected */
    public void setParent(ResourceBundle resourceBundle) {
        this.parent = resourceBundle;
    }

    public String getKey() {
        return this.key;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getNoFallback() {
        return this.wholeBundle.reader.getNoFallback();
    }

    private static ICUResourceBundle getBundle(ICUResourceBundleReader iCUResourceBundleReader, String str, String str2, ClassLoader classLoader) {
        int rootResource = iCUResourceBundleReader.getRootResource();
        if (ICUResourceBundleReader.URES_IS_TABLE(ICUResourceBundleReader.RES_GET_TYPE(rootResource))) {
            ICUResourceBundleImpl.ResourceTable resourceTable = new ICUResourceBundleImpl.ResourceTable(new WholeBundle(str, str2, classLoader, iCUResourceBundleReader), rootResource);
            String findString = resourceTable.findString("%%ALIAS");
            return findString != null ? UResourceBundle.getBundleInstance(str, findString) : resourceTable;
        }
        throw new IllegalStateException("Invalid format error");
    }

    protected ICUResourceBundle(WholeBundle wholeBundle2) {
        this.wholeBundle = wholeBundle2;
    }

    protected ICUResourceBundle(ICUResourceBundle iCUResourceBundle, String str) {
        this.key = str;
        this.wholeBundle = iCUResourceBundle.wholeBundle;
        this.container = iCUResourceBundle;
        this.parent = iCUResourceBundle.parent;
    }

    protected static ICUResourceBundle getAliasedResource(ICUResourceBundle iCUResourceBundle, String[] strArr, int i, String str, int i2, HashMap<String, String> hashMap, UResourceBundle uResourceBundle) {
        String str2;
        String str3;
        String str4;
        String[] strArr2;
        int i3;
        int indexOf;
        WholeBundle wholeBundle2 = iCUResourceBundle.wholeBundle;
        ClassLoader classLoader = wholeBundle2.loader;
        String alias = wholeBundle2.reader.getAlias(i2);
        HashMap<String, String> hashMap2 = hashMap == null ? new HashMap<>() : hashMap;
        if (hashMap2.get(alias) == null) {
            hashMap2.put(alias, "");
            ICUResourceBundle iCUResourceBundle2 = null;
            int i4 = 0;
            if (alias.indexOf(47) == 0) {
                int indexOf2 = alias.indexOf(47, 1);
                int i5 = indexOf2 + 1;
                int indexOf3 = alias.indexOf(47, i5);
                str3 = alias.substring(1, indexOf2);
                if (indexOf3 < 0) {
                    str2 = alias.substring(i5);
                    str4 = null;
                } else {
                    str2 = alias.substring(i5, indexOf3);
                    str4 = alias.substring(indexOf3 + 1, alias.length());
                }
                if (str3.equals(ICUDATA)) {
                    classLoader = ICU_DATA_CLASS_LOADER;
                    str3 = ICUData.ICU_BASE_NAME;
                } else if (str3.indexOf(ICUDATA) > -1 && (indexOf = str3.indexOf(45)) > -1) {
                    str3 = "ohos/global/icu/impl/data/icudt66b/" + str3.substring(indexOf + 1, str3.length());
                    classLoader = ICU_DATA_CLASS_LOADER;
                }
            } else {
                int indexOf4 = alias.indexOf(47);
                if (indexOf4 != -1) {
                    String substring = alias.substring(0, indexOf4);
                    str4 = alias.substring(indexOf4 + 1);
                    str2 = substring;
                } else {
                    str2 = alias;
                    str4 = null;
                }
                str3 = wholeBundle2.baseName;
            }
            if (str3.equals(LOCALE)) {
                String str5 = wholeBundle2.baseName;
                String substring2 = alias.substring(8, alias.length());
                ICUResourceBundle iCUResourceBundle3 = (ICUResourceBundle) uResourceBundle;
                while (true) {
                    ICUResourceBundle iCUResourceBundle4 = iCUResourceBundle3.container;
                    if (iCUResourceBundle4 == null) {
                        break;
                    }
                    iCUResourceBundle3 = iCUResourceBundle4;
                }
                iCUResourceBundle2 = findResourceWithFallback(substring2, iCUResourceBundle3, null);
            } else {
                ICUResourceBundle bundleInstance = getBundleInstance(str3, str2, classLoader, false);
                if (str4 != null) {
                    i3 = countPathKeys(str4);
                    if (i3 > 0) {
                        strArr2 = new String[i3];
                        getResPathKeys(str4, i3, strArr2, 0);
                    } else {
                        strArr2 = strArr;
                    }
                } else if (strArr != null) {
                    strArr2 = strArr;
                    i3 = i;
                } else {
                    int resDepth = iCUResourceBundle.getResDepth();
                    int i6 = resDepth + 1;
                    String[] strArr3 = new String[i6];
                    iCUResourceBundle.getResPathKeys(strArr3, resDepth);
                    strArr3[resDepth] = str;
                    i3 = i6;
                    strArr2 = strArr3;
                }
                if (i3 > 0) {
                    iCUResourceBundle2 = bundleInstance;
                    while (iCUResourceBundle2 != null && i4 < i3) {
                        iCUResourceBundle2 = iCUResourceBundle2.get(strArr2[i4], hashMap2, uResourceBundle);
                        i4++;
                    }
                }
            }
            if (iCUResourceBundle2 != null) {
                return iCUResourceBundle2;
            }
            throw new MissingResourceException(wholeBundle2.localeID, wholeBundle2.baseName, str);
        }
        throw new IllegalArgumentException("Circular references in the resource bundles");
    }

    @Deprecated
    public final Set<String> getTopLevelKeySet() {
        return this.wholeBundle.topLevelKeys;
    }

    @Deprecated
    public final void setTopLevelKeySet(Set<String> set) {
        this.wholeBundle.topLevelKeys = set;
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
