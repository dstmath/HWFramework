package ohos.global.icu.util;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.ICUResourceBundleReader;
import ohos.global.icu.impl.ResourceBundleWrapper;

public abstract class UResourceBundle extends ResourceBundle {
    public static final int ARRAY = 8;
    public static final int BINARY = 1;
    public static final int INT = 7;
    public static final int INT_VECTOR = 14;
    public static final int NONE = -1;
    private static Map<String, RootType> ROOT_CACHE = new ConcurrentHashMap();
    public static final int STRING = 0;
    public static final int TABLE = 2;

    /* access modifiers changed from: private */
    public enum RootType {
        MISSING,
        ICU,
        JAVA
    }

    /* access modifiers changed from: protected */
    public abstract String getBaseName();

    public String getKey() {
        return null;
    }

    /* access modifiers changed from: protected */
    public abstract String getLocaleID();

    /* access modifiers changed from: protected */
    public abstract UResourceBundle getParent();

    public int getSize() {
        return 1;
    }

    public int getType() {
        return -1;
    }

    public abstract ULocale getULocale();

    public VersionInfo getVersion() {
        return null;
    }

    /* access modifiers changed from: protected */
    public UResourceBundle handleGet(int i, HashMap<String, String> hashMap, UResourceBundle uResourceBundle) {
        return null;
    }

    /* access modifiers changed from: protected */
    public UResourceBundle handleGet(String str, HashMap<String, String> hashMap, UResourceBundle uResourceBundle) {
        return null;
    }

    /* access modifiers changed from: protected */
    public Enumeration<String> handleGetKeys() {
        return null;
    }

    /* access modifiers changed from: protected */
    public String[] handleGetStringArray() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean isTopLevelResource() {
        return true;
    }

    public static UResourceBundle getBundleInstance(String str, String str2) {
        return getBundleInstance(str, str2, ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String str, String str2, ClassLoader classLoader) {
        return getBundleInstance(str, str2, classLoader, false);
    }

    protected static UResourceBundle getBundleInstance(String str, String str2, ClassLoader classLoader, boolean z) {
        return instantiateBundle(str, str2, classLoader, z);
    }

    public static UResourceBundle getBundleInstance(ULocale uLocale) {
        if (uLocale == null) {
            uLocale = ULocale.getDefault();
        }
        return getBundleInstance("ohos/global/icu/impl/data/icudt66b", uLocale.getBaseName(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String str) {
        if (str == null) {
            str = "ohos/global/icu/impl/data/icudt66b";
        }
        return getBundleInstance(str, ULocale.getDefault().getBaseName(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String str, Locale locale) {
        if (str == null) {
            str = "ohos/global/icu/impl/data/icudt66b";
        }
        return getBundleInstance(str, (locale == null ? ULocale.getDefault() : ULocale.forLocale(locale)).getBaseName(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String str, ULocale uLocale) {
        if (str == null) {
            str = "ohos/global/icu/impl/data/icudt66b";
        }
        if (uLocale == null) {
            uLocale = ULocale.getDefault();
        }
        return getBundleInstance(str, uLocale.getBaseName(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String str, Locale locale, ClassLoader classLoader) {
        if (str == null) {
            str = "ohos/global/icu/impl/data/icudt66b";
        }
        return getBundleInstance(str, (locale == null ? ULocale.getDefault() : ULocale.forLocale(locale)).getBaseName(), classLoader, false);
    }

    public static UResourceBundle getBundleInstance(String str, ULocale uLocale, ClassLoader classLoader) {
        if (str == null) {
            str = "ohos/global/icu/impl/data/icudt66b";
        }
        if (uLocale == null) {
            uLocale = ULocale.getDefault();
        }
        return getBundleInstance(str, uLocale.getBaseName(), classLoader, false);
    }

    @Override // java.util.ResourceBundle
    public Locale getLocale() {
        return getULocale().toLocale();
    }

    private static RootType getRootType(String str, ClassLoader classLoader) {
        RootType rootType;
        RootType rootType2 = ROOT_CACHE.get(str);
        if (rootType2 == null) {
            String str2 = str.indexOf(46) == -1 ? "root" : "";
            try {
                ICUResourceBundle.getBundleInstance(str, str2, classLoader, true);
                rootType = RootType.ICU;
            } catch (MissingResourceException unused) {
                try {
                    ResourceBundleWrapper.getBundleInstance(str, str2, classLoader, true);
                    rootType = RootType.JAVA;
                } catch (MissingResourceException unused2) {
                    rootType = RootType.MISSING;
                }
            }
            rootType2 = rootType;
            ROOT_CACHE.put(str, rootType2);
        }
        return rootType2;
    }

    private static void setRootType(String str, RootType rootType) {
        ROOT_CACHE.put(str, rootType);
    }

    protected static UResourceBundle instantiateBundle(String str, String str2, ClassLoader classLoader, boolean z) {
        int i = AnonymousClass1.$SwitchMap$ohos$global$icu$util$UResourceBundle$RootType[getRootType(str, classLoader).ordinal()];
        if (i == 1) {
            return ICUResourceBundle.getBundleInstance(str, str2, classLoader, z);
        }
        if (i == 2) {
            return ResourceBundleWrapper.getBundleInstance(str, str2, classLoader, z);
        }
        try {
            ICUResourceBundle bundleInstance = ICUResourceBundle.getBundleInstance(str, str2, classLoader, z);
            setRootType(str, RootType.ICU);
            return bundleInstance;
        } catch (MissingResourceException unused) {
            ResourceBundleWrapper bundleInstance2 = ResourceBundleWrapper.getBundleInstance(str, str2, classLoader, z);
            setRootType(str, RootType.JAVA);
            return bundleInstance2;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.util.UResourceBundle$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$util$UResourceBundle$RootType = new int[RootType.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$util$UResourceBundle$RootType[RootType.ICU.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$UResourceBundle$RootType[RootType.JAVA.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$UResourceBundle$RootType[RootType.MISSING.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public ByteBuffer getBinary() {
        throw new UResourceTypeMismatchException("");
    }

    public String getString() {
        throw new UResourceTypeMismatchException("");
    }

    public String[] getStringArray() {
        throw new UResourceTypeMismatchException("");
    }

    public byte[] getBinary(byte[] bArr) {
        throw new UResourceTypeMismatchException("");
    }

    public int[] getIntVector() {
        throw new UResourceTypeMismatchException("");
    }

    public int getInt() {
        throw new UResourceTypeMismatchException("");
    }

    public int getUInt() {
        throw new UResourceTypeMismatchException("");
    }

    public UResourceBundle get(String str) {
        UResourceBundle findTopLevel = findTopLevel(str);
        if (findTopLevel != null) {
            return findTopLevel;
        }
        String fullName = ICUResourceBundleReader.getFullName(getBaseName(), getLocaleID());
        throw new MissingResourceException("Can't find resource for bundle " + fullName + ", key " + str, getClass().getName(), str);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public UResourceBundle findTopLevel(String str) {
        for (UResourceBundle uResourceBundle = this; uResourceBundle != null; uResourceBundle = uResourceBundle.getParent()) {
            UResourceBundle handleGet = uResourceBundle.handleGet(str, (HashMap<String, String>) null, this);
            if (handleGet != null) {
                return handleGet;
            }
        }
        return null;
    }

    public String getString(int i) {
        ICUResourceBundle iCUResourceBundle = get(i);
        if (iCUResourceBundle.getType() == 0) {
            return iCUResourceBundle.getString();
        }
        throw new UResourceTypeMismatchException("");
    }

    public UResourceBundle get(int i) {
        UResourceBundle handleGet = handleGet(i, (HashMap<String, String>) null, this);
        if (handleGet == null) {
            handleGet = getParent();
            if (handleGet != null) {
                handleGet = handleGet.get(i);
            }
            if (handleGet == null) {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getKey(), getClass().getName(), getKey());
            }
        }
        return handleGet;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public UResourceBundle findTopLevel(int i) {
        for (UResourceBundle uResourceBundle = this; uResourceBundle != null; uResourceBundle = uResourceBundle.getParent()) {
            UResourceBundle handleGet = uResourceBundle.handleGet(i, (HashMap<String, String>) null, this);
            if (handleGet != null) {
                return handleGet;
            }
        }
        return null;
    }

    @Override // java.util.ResourceBundle
    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }

    @Override // java.util.ResourceBundle
    @Deprecated
    public Set<String> keySet() {
        Set<String> set;
        TreeSet treeSet;
        ICUResourceBundle iCUResourceBundle = null;
        if (!isTopLevelResource() || !(this instanceof ICUResourceBundle)) {
            set = null;
        } else {
            iCUResourceBundle = (ICUResourceBundle) this;
            set = iCUResourceBundle.getTopLevelKeySet();
        }
        if (set == null) {
            if (!isTopLevelResource()) {
                return handleKeySet();
            }
            if (this.parent == null) {
                treeSet = new TreeSet();
            } else if (this.parent instanceof UResourceBundle) {
                treeSet = new TreeSet(((UResourceBundle) this.parent).keySet());
            } else {
                treeSet = new TreeSet();
                Enumeration<String> keys = this.parent.getKeys();
                while (keys.hasMoreElements()) {
                    treeSet.add(keys.nextElement());
                }
            }
            treeSet.addAll(handleKeySet());
            set = Collections.unmodifiableSet(treeSet);
            if (iCUResourceBundle != null) {
                iCUResourceBundle.setTopLevelKeySet(set);
            }
        }
        return set;
    }

    /* access modifiers changed from: protected */
    @Override // java.util.ResourceBundle
    @Deprecated
    public Set<String> handleKeySet() {
        return Collections.emptySet();
    }

    public UResourceBundleIterator getIterator() {
        return new UResourceBundleIterator(this);
    }

    /* access modifiers changed from: protected */
    @Override // java.util.ResourceBundle
    public Object handleGetObject(String str) {
        return handleGetObjectImpl(str, this);
    }

    private Object handleGetObjectImpl(String str, UResourceBundle uResourceBundle) {
        Object resolveObject = resolveObject(str, uResourceBundle);
        if (resolveObject == null) {
            UResourceBundle parent = getParent();
            if (parent != null) {
                resolveObject = parent.handleGetObjectImpl(str, uResourceBundle);
            }
            if (resolveObject == null) {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + str, getClass().getName(), str);
            }
        }
        return resolveObject;
    }

    private Object resolveObject(String str, UResourceBundle uResourceBundle) {
        if (getType() == 0) {
            return getString();
        }
        UResourceBundle handleGet = handleGet(str, (HashMap<String, String>) null, uResourceBundle);
        if (handleGet == null) {
            return handleGet;
        }
        if (handleGet.getType() == 0) {
            return handleGet.getString();
        }
        try {
            return handleGet.getType() == 8 ? handleGet.handleGetStringArray() : handleGet;
        } catch (UResourceTypeMismatchException unused) {
            return handleGet;
        }
    }
}
