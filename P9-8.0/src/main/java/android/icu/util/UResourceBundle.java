package android.icu.util;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.ICUResourceBundleReader;
import android.icu.impl.ResourceBundleWrapper;
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

public abstract class UResourceBundle extends ResourceBundle {
    private static final /* synthetic */ int[] -android-icu-util-UResourceBundle$RootTypeSwitchesValues = null;
    public static final int ARRAY = 8;
    public static final int BINARY = 1;
    public static final int INT = 7;
    public static final int INT_VECTOR = 14;
    public static final int NONE = -1;
    private static Map<String, RootType> ROOT_CACHE = new ConcurrentHashMap();
    public static final int STRING = 0;
    public static final int TABLE = 2;

    private enum RootType {
        MISSING,
        ICU,
        JAVA
    }

    private static /* synthetic */ int[] -getandroid-icu-util-UResourceBundle$RootTypeSwitchesValues() {
        if (-android-icu-util-UResourceBundle$RootTypeSwitchesValues != null) {
            return -android-icu-util-UResourceBundle$RootTypeSwitchesValues;
        }
        int[] iArr = new int[RootType.values().length];
        try {
            iArr[RootType.ICU.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RootType.JAVA.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RootType.MISSING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -android-icu-util-UResourceBundle$RootTypeSwitchesValues = iArr;
        return iArr;
    }

    protected abstract String getBaseName();

    protected abstract String getLocaleID();

    protected abstract UResourceBundle getParent();

    public abstract ULocale getULocale();

    public static UResourceBundle getBundleInstance(String baseName, String localeName) {
        return getBundleInstance(baseName, localeName, ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String baseName, String localeName, ClassLoader root) {
        return getBundleInstance(baseName, localeName, root, false);
    }

    protected static UResourceBundle getBundleInstance(String baseName, String localeName, ClassLoader root, boolean disableFallback) {
        return instantiateBundle(baseName, localeName, root, disableFallback);
    }

    public static UResourceBundle getBundleInstance(ULocale locale) {
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        return getBundleInstance(ICUData.ICU_BASE_NAME, locale.getBaseName(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String baseName) {
        if (baseName == null) {
            baseName = ICUData.ICU_BASE_NAME;
        }
        return getBundleInstance(baseName, ULocale.getDefault().getBaseName(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String baseName, Locale locale) {
        if (baseName == null) {
            baseName = ICUData.ICU_BASE_NAME;
        }
        return getBundleInstance(baseName, (locale == null ? ULocale.getDefault() : ULocale.forLocale(locale)).getBaseName(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String baseName, ULocale locale) {
        if (baseName == null) {
            baseName = ICUData.ICU_BASE_NAME;
        }
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        return getBundleInstance(baseName, locale.getBaseName(), ICUResourceBundle.ICU_DATA_CLASS_LOADER, false);
    }

    public static UResourceBundle getBundleInstance(String baseName, Locale locale, ClassLoader loader) {
        if (baseName == null) {
            baseName = ICUData.ICU_BASE_NAME;
        }
        return getBundleInstance(baseName, (locale == null ? ULocale.getDefault() : ULocale.forLocale(locale)).getBaseName(), loader, false);
    }

    public static UResourceBundle getBundleInstance(String baseName, ULocale locale, ClassLoader loader) {
        if (baseName == null) {
            baseName = ICUData.ICU_BASE_NAME;
        }
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        return getBundleInstance(baseName, locale.getBaseName(), loader, false);
    }

    public Locale getLocale() {
        return getULocale().toLocale();
    }

    private static RootType getRootType(String baseName, ClassLoader root) {
        RootType rootType = (RootType) ROOT_CACHE.get(baseName);
        if (rootType == null) {
            String rootLocale = baseName.indexOf(46) == -1 ? "root" : "";
            try {
                ICUResourceBundle.getBundleInstance(baseName, rootLocale, root, true);
                rootType = RootType.ICU;
            } catch (MissingResourceException e) {
                try {
                    ResourceBundleWrapper.getBundleInstance(baseName, rootLocale, root, true);
                    rootType = RootType.JAVA;
                } catch (MissingResourceException e2) {
                    rootType = RootType.MISSING;
                }
            }
            ROOT_CACHE.put(baseName, rootType);
        }
        return rootType;
    }

    private static void setRootType(String baseName, RootType rootType) {
        ROOT_CACHE.put(baseName, rootType);
    }

    protected static UResourceBundle instantiateBundle(String baseName, String localeName, ClassLoader root, boolean disableFallback) {
        switch (-getandroid-icu-util-UResourceBundle$RootTypeSwitchesValues()[getRootType(baseName, root).ordinal()]) {
            case 1:
                return ICUResourceBundle.getBundleInstance(baseName, localeName, root, disableFallback);
            case 2:
                return ResourceBundleWrapper.getBundleInstance(baseName, localeName, root, disableFallback);
            default:
                UResourceBundle b;
                try {
                    b = ICUResourceBundle.getBundleInstance(baseName, localeName, root, disableFallback);
                    setRootType(baseName, RootType.ICU);
                } catch (MissingResourceException e) {
                    b = ResourceBundleWrapper.getBundleInstance(baseName, localeName, root, disableFallback);
                    setRootType(baseName, RootType.JAVA);
                }
                return b;
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

    public byte[] getBinary(byte[] ba) {
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

    public UResourceBundle get(String aKey) {
        UResourceBundle obj = findTopLevel(aKey);
        if (obj != null) {
            return obj;
        }
        throw new MissingResourceException("Can't find resource for bundle " + ICUResourceBundleReader.getFullName(getBaseName(), getLocaleID()) + ", key " + aKey, getClass().getName(), aKey);
    }

    @Deprecated
    protected UResourceBundle findTopLevel(String aKey) {
        for (UResourceBundle res = this; res != null; res = res.getParent()) {
            UResourceBundle obj = res.handleGet(aKey, null, this);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    public String getString(int index) {
        ICUResourceBundle temp = (ICUResourceBundle) get(index);
        if (temp.getType() == 0) {
            return temp.getString();
        }
        throw new UResourceTypeMismatchException("");
    }

    public UResourceBundle get(int index) {
        UResourceBundle obj = handleGet(index, null, this);
        if (obj == null) {
            obj = getParent();
            if (obj != null) {
                obj = obj.get(index);
            }
            if (obj == null) {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + getKey(), getClass().getName(), getKey());
            }
        }
        return obj;
    }

    @Deprecated
    protected UResourceBundle findTopLevel(int index) {
        for (UResourceBundle res = this; res != null; res = res.getParent()) {
            UResourceBundle obj = res.handleGet(index, null, this);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }

    @Deprecated
    public Set<String> keySet() {
        Set<String> keys = null;
        ICUResourceBundle icurb = null;
        if (isTopLevelResource() && (this instanceof ICUResourceBundle)) {
            icurb = (ICUResourceBundle) this;
            keys = icurb.getTopLevelKeySet();
        }
        if (keys == null) {
            if (!isTopLevelResource()) {
                return handleKeySet();
            }
            TreeSet<String> newKeySet;
            if (this.parent == null) {
                newKeySet = new TreeSet();
            } else if (this.parent instanceof UResourceBundle) {
                newKeySet = new TreeSet(((UResourceBundle) this.parent).keySet());
            } else {
                newKeySet = new TreeSet();
                Enumeration<String> parentKeys = this.parent.getKeys();
                while (parentKeys.hasMoreElements()) {
                    newKeySet.add((String) parentKeys.nextElement());
                }
            }
            newKeySet.addAll(handleKeySet());
            keys = Collections.unmodifiableSet(newKeySet);
            if (icurb != null) {
                icurb.setTopLevelKeySet(keys);
            }
        }
        return keys;
    }

    @Deprecated
    protected Set<String> handleKeySet() {
        return Collections.emptySet();
    }

    public int getSize() {
        return 1;
    }

    public int getType() {
        return -1;
    }

    public VersionInfo getVersion() {
        return null;
    }

    public UResourceBundleIterator getIterator() {
        return new UResourceBundleIterator(this);
    }

    public String getKey() {
        return null;
    }

    protected UResourceBundle handleGet(String aKey, HashMap<String, String> hashMap, UResourceBundle requested) {
        return null;
    }

    protected UResourceBundle handleGet(int index, HashMap<String, String> hashMap, UResourceBundle requested) {
        return null;
    }

    protected String[] handleGetStringArray() {
        return null;
    }

    protected Enumeration<String> handleGetKeys() {
        return null;
    }

    protected Object handleGetObject(String aKey) {
        return handleGetObjectImpl(aKey, this);
    }

    private Object handleGetObjectImpl(String aKey, UResourceBundle requested) {
        Object obj = resolveObject(aKey, requested);
        if (obj == null) {
            UResourceBundle parentBundle = getParent();
            if (parentBundle != null) {
                obj = parentBundle.handleGetObjectImpl(aKey, requested);
            }
            if (obj == null) {
                throw new MissingResourceException("Can't find resource for bundle " + getClass().getName() + ", key " + aKey, getClass().getName(), aKey);
            }
        }
        return obj;
    }

    private Object resolveObject(String aKey, UResourceBundle requested) {
        if (getType() == 0) {
            return getString();
        }
        UResourceBundle obj = handleGet(aKey, null, requested);
        if (obj != null) {
            if (obj.getType() == 0) {
                return obj.getString();
            }
            try {
                if (obj.getType() == 8) {
                    return obj.handleGetStringArray();
                }
            } catch (UResourceTypeMismatchException e) {
                return obj;
            }
        }
        return obj;
    }

    @Deprecated
    protected boolean isTopLevelResource() {
        return true;
    }
}
