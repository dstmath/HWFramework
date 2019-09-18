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

    /* access modifiers changed from: protected */
    public abstract String getBaseName();

    /* access modifiers changed from: protected */
    public abstract String getLocaleID();

    /* access modifiers changed from: protected */
    public abstract UResourceBundle getParent();

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
        RootType rootType = ROOT_CACHE.get(baseName);
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
        UResourceBundle b;
        switch (getRootType(baseName, root)) {
            case ICU:
                return ICUResourceBundle.getBundleInstance(baseName, localeName, root, disableFallback);
            case JAVA:
                return ResourceBundleWrapper.getBundleInstance(baseName, localeName, root, disableFallback);
            default:
                try {
                    b = ICUResourceBundle.getBundleInstance(baseName, localeName, root, disableFallback);
                    setRootType(baseName, RootType.ICU);
                } catch (MissingResourceException e) {
                    UResourceBundle b2 = ResourceBundleWrapper.getBundleInstance(baseName, localeName, root, disableFallback);
                    setRootType(baseName, RootType.JAVA);
                    b = b2;
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
        String fullName = ICUResourceBundleReader.getFullName(getBaseName(), getLocaleID());
        throw new MissingResourceException("Can't find resource for bundle " + fullName + ", key " + aKey, getClass().getName(), aKey);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public UResourceBundle findTopLevel(String aKey) {
        for (UResourceBundle res = this; res != null; res = res.getParent()) {
            UResourceBundle obj = res.handleGet(aKey, (HashMap<String, String>) null, this);
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
        UResourceBundle obj = handleGet(index, (HashMap<String, String>) null, this);
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

    /* access modifiers changed from: protected */
    @Deprecated
    public UResourceBundle findTopLevel(int index) {
        for (UResourceBundle res = this; res != null; res = res.getParent()) {
            UResourceBundle obj = res.handleGet(index, (HashMap<String, String>) null, this);
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
        TreeSet treeSet;
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
            if (this.parent == null) {
                treeSet = new TreeSet();
            } else if (this.parent instanceof UResourceBundle) {
                treeSet = new TreeSet(((UResourceBundle) this.parent).keySet());
            } else {
                treeSet = new TreeSet();
                Enumeration<String> parentKeys = this.parent.getKeys();
                while (parentKeys.hasMoreElements()) {
                    treeSet.add(parentKeys.nextElement());
                }
            }
            treeSet.addAll(handleKeySet());
            keys = Collections.unmodifiableSet(treeSet);
            if (icurb != null) {
                icurb.setTopLevelKeySet(keys);
            }
        }
        return keys;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public Set<String> handleKeySet() {
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

    /* access modifiers changed from: protected */
    public UResourceBundle handleGet(String aKey, HashMap<String, String> hashMap, UResourceBundle requested) {
        return null;
    }

    /* access modifiers changed from: protected */
    public UResourceBundle handleGet(int index, HashMap<String, String> hashMap, UResourceBundle requested) {
        return null;
    }

    /* access modifiers changed from: protected */
    public String[] handleGetStringArray() {
        return null;
    }

    /* access modifiers changed from: protected */
    public Enumeration<String> handleGetKeys() {
        return null;
    }

    /* access modifiers changed from: protected */
    public Object handleGetObject(String aKey) {
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
        UResourceBundle obj = handleGet(aKey, (HashMap<String, String>) null, requested);
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

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean isTopLevelResource() {
        return true;
    }
}
