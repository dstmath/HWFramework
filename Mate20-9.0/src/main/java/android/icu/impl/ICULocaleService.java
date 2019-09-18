package android.icu.impl;

import android.icu.impl.ICUService;
import android.icu.util.ULocale;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ICULocaleService extends ICUService {
    private ULocale fallbackLocale;
    private String fallbackLocaleName;

    public static class ICUResourceBundleFactory extends LocaleKeyFactory {
        protected final String bundleName;

        public ICUResourceBundleFactory() {
            this(ICUData.ICU_BASE_NAME);
        }

        public ICUResourceBundleFactory(String bundleName2) {
            super(true);
            this.bundleName = bundleName2;
        }

        /* access modifiers changed from: protected */
        public Set<String> getSupportedIDs() {
            return ICUResourceBundle.getFullLocaleNameSet(this.bundleName, loader());
        }

        public void updateVisibleIDs(Map<String, ICUService.Factory> result) {
            for (String id : ICUResourceBundle.getAvailableLocaleNameSet(this.bundleName, loader())) {
                result.put(id, this);
            }
        }

        /* access modifiers changed from: protected */
        public Object handleCreate(ULocale loc, int kind, ICUService service) {
            return ICUResourceBundle.getBundleInstance(this.bundleName, loc, loader());
        }

        /* access modifiers changed from: protected */
        public ClassLoader loader() {
            return ClassLoaderUtil.getClassLoader(getClass());
        }

        public String toString() {
            return super.toString() + ", bundle: " + this.bundleName;
        }
    }

    public static class LocaleKey extends ICUService.Key {
        public static final int KIND_ANY = -1;
        private String currentID;
        private String fallbackID;
        private int kind;
        private String primaryID;
        private int varstart;

        public static LocaleKey createWithCanonicalFallback(String primaryID2, String canonicalFallbackID) {
            return createWithCanonicalFallback(primaryID2, canonicalFallbackID, -1);
        }

        public static LocaleKey createWithCanonicalFallback(String primaryID2, String canonicalFallbackID, int kind2) {
            if (primaryID2 == null) {
                return null;
            }
            return new LocaleKey(primaryID2, ULocale.getName(primaryID2), canonicalFallbackID, kind2);
        }

        public static LocaleKey createWithCanonical(ULocale locale, String canonicalFallbackID, int kind2) {
            if (locale == null) {
                return null;
            }
            String canonicalPrimaryID = locale.getName();
            return new LocaleKey(canonicalPrimaryID, canonicalPrimaryID, canonicalFallbackID, kind2);
        }

        protected LocaleKey(String primaryID2, String canonicalPrimaryID, String canonicalFallbackID, int kind2) {
            super(primaryID2);
            this.kind = kind2;
            if (canonicalPrimaryID == null || canonicalPrimaryID.equalsIgnoreCase("root")) {
                this.primaryID = "";
                this.fallbackID = null;
            } else {
                int idx = canonicalPrimaryID.indexOf(64);
                if (idx != 4 || !canonicalPrimaryID.regionMatches(true, 0, "root", 0, 4)) {
                    this.primaryID = canonicalPrimaryID;
                    this.varstart = idx;
                    if (canonicalFallbackID == null || this.primaryID.equals(canonicalFallbackID)) {
                        this.fallbackID = "";
                    } else {
                        this.fallbackID = canonicalFallbackID;
                    }
                } else {
                    this.primaryID = canonicalPrimaryID.substring(4);
                    this.varstart = 0;
                    this.fallbackID = null;
                }
            }
            this.currentID = this.varstart == -1 ? this.primaryID : this.primaryID.substring(0, this.varstart);
        }

        public String prefix() {
            if (this.kind == -1) {
                return null;
            }
            return Integer.toString(kind());
        }

        public int kind() {
            return this.kind;
        }

        public String canonicalID() {
            return this.primaryID;
        }

        public String currentID() {
            return this.currentID;
        }

        public String currentDescriptor() {
            String result = currentID();
            if (result == null) {
                return result;
            }
            StringBuilder buf = new StringBuilder();
            if (this.kind != -1) {
                buf.append(prefix());
            }
            buf.append('/');
            buf.append(result);
            if (this.varstart != -1) {
                buf.append(this.primaryID.substring(this.varstart, this.primaryID.length()));
            }
            return buf.toString();
        }

        public ULocale canonicalLocale() {
            return new ULocale(this.primaryID);
        }

        public ULocale currentLocale() {
            if (this.varstart == -1) {
                return new ULocale(this.currentID);
            }
            return new ULocale(this.currentID + this.primaryID.substring(this.varstart));
        }

        public boolean fallback() {
            int x = this.currentID.lastIndexOf(95);
            if (x != -1) {
                do {
                    x--;
                    if (x < 0) {
                        break;
                    }
                } while (this.currentID.charAt(x) == '_');
                this.currentID = this.currentID.substring(0, x + 1);
                return true;
            } else if (this.fallbackID != null) {
                this.currentID = this.fallbackID;
                if (this.fallbackID.length() == 0) {
                    this.fallbackID = null;
                } else {
                    this.fallbackID = "";
                }
                return true;
            } else {
                this.currentID = null;
                return false;
            }
        }

        public boolean isFallbackOf(String id) {
            return LocaleUtility.isFallbackOf(canonicalID(), id);
        }
    }

    public static abstract class LocaleKeyFactory implements ICUService.Factory {
        public static final boolean INVISIBLE = false;
        public static final boolean VISIBLE = true;
        protected final String name;
        protected final boolean visible;

        protected LocaleKeyFactory(boolean visible2) {
            this.visible = visible2;
            this.name = null;
        }

        protected LocaleKeyFactory(boolean visible2, String name2) {
            this.visible = visible2;
            this.name = name2;
        }

        public Object create(ICUService.Key key, ICUService service) {
            if (!handlesKey(key)) {
                return null;
            }
            LocaleKey lkey = (LocaleKey) key;
            return handleCreate(lkey.currentLocale(), lkey.kind(), service);
        }

        /* access modifiers changed from: protected */
        public boolean handlesKey(ICUService.Key key) {
            if (key == null) {
                return false;
            }
            return getSupportedIDs().contains(key.currentID());
        }

        public void updateVisibleIDs(Map<String, ICUService.Factory> result) {
            for (String id : getSupportedIDs()) {
                if (this.visible) {
                    result.put(id, this);
                } else {
                    result.remove(id);
                }
            }
        }

        public String getDisplayName(String id, ULocale locale) {
            if (locale == null) {
                return id;
            }
            return new ULocale(id).getDisplayName(locale);
        }

        /* access modifiers changed from: protected */
        public Object handleCreate(ULocale loc, int kind, ICUService service) {
            return null;
        }

        /* access modifiers changed from: protected */
        public boolean isSupportedID(String id) {
            return getSupportedIDs().contains(id);
        }

        /* access modifiers changed from: protected */
        public Set<String> getSupportedIDs() {
            return Collections.emptySet();
        }

        public String toString() {
            StringBuilder buf = new StringBuilder(super.toString());
            if (this.name != null) {
                buf.append(", name: ");
                buf.append(this.name);
            }
            buf.append(", visible: ");
            buf.append(this.visible);
            return buf.toString();
        }
    }

    public static class SimpleLocaleKeyFactory extends LocaleKeyFactory {
        private final String id;
        private final int kind;
        private final Object obj;

        public SimpleLocaleKeyFactory(Object obj2, ULocale locale, int kind2, boolean visible) {
            this(obj2, locale, kind2, visible, null);
        }

        public SimpleLocaleKeyFactory(Object obj2, ULocale locale, int kind2, boolean visible, String name) {
            super(visible, name);
            this.obj = obj2;
            this.id = locale.getBaseName();
            this.kind = kind2;
        }

        public Object create(ICUService.Key key, ICUService service) {
            if (!(key instanceof LocaleKey)) {
                return null;
            }
            LocaleKey lkey = (LocaleKey) key;
            if ((this.kind == -1 || this.kind == lkey.kind()) && this.id.equals(lkey.currentID())) {
                return this.obj;
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public boolean isSupportedID(String idToCheck) {
            return this.id.equals(idToCheck);
        }

        public void updateVisibleIDs(Map<String, ICUService.Factory> result) {
            if (this.visible) {
                result.put(this.id, this);
            } else {
                result.remove(this.id);
            }
        }

        public String toString() {
            return super.toString() + ", id: " + this.id + ", kind: " + this.kind;
        }
    }

    public ICULocaleService() {
    }

    public ICULocaleService(String name) {
        super(name);
    }

    public Object get(ULocale locale) {
        return get(locale, -1, null);
    }

    public Object get(ULocale locale, int kind) {
        return get(locale, kind, null);
    }

    public Object get(ULocale locale, ULocale[] actualReturn) {
        return get(locale, -1, actualReturn);
    }

    public Object get(ULocale locale, int kind, ULocale[] actualReturn) {
        ICUService.Key key = createKey(locale, kind);
        if (actualReturn == null) {
            return getKey(key);
        }
        String[] temp = new String[1];
        Object result = getKey(key, temp);
        if (result != null) {
            int n = temp[0].indexOf("/");
            if (n >= 0) {
                temp[0] = temp[0].substring(n + 1);
            }
            actualReturn[0] = new ULocale(temp[0]);
        }
        return result;
    }

    public ICUService.Factory registerObject(Object obj, ULocale locale) {
        return registerObject(obj, locale, -1, true);
    }

    public ICUService.Factory registerObject(Object obj, ULocale locale, boolean visible) {
        return registerObject(obj, locale, -1, visible);
    }

    public ICUService.Factory registerObject(Object obj, ULocale locale, int kind) {
        return registerObject(obj, locale, kind, true);
    }

    public ICUService.Factory registerObject(Object obj, ULocale locale, int kind, boolean visible) {
        return registerFactory(new SimpleLocaleKeyFactory(obj, locale, kind, visible));
    }

    public Locale[] getAvailableLocales() {
        Set<String> visIDs = getVisibleIDs();
        Locale[] locales = new Locale[visIDs.size()];
        int n = 0;
        for (String id : visIDs) {
            locales[n] = LocaleUtility.getLocaleFromName(id);
            n++;
        }
        return locales;
    }

    public ULocale[] getAvailableULocales() {
        Set<String> visIDs = getVisibleIDs();
        ULocale[] locales = new ULocale[visIDs.size()];
        int n = 0;
        for (String id : visIDs) {
            locales[n] = new ULocale(id);
            n++;
        }
        return locales;
    }

    public String validateFallbackLocale() {
        ULocale loc = ULocale.getDefault();
        if (loc != this.fallbackLocale) {
            synchronized (this) {
                if (loc != this.fallbackLocale) {
                    this.fallbackLocale = loc;
                    this.fallbackLocaleName = loc.getBaseName();
                    clearServiceCache();
                }
            }
        }
        return this.fallbackLocaleName;
    }

    public ICUService.Key createKey(String id) {
        return LocaleKey.createWithCanonicalFallback(id, validateFallbackLocale());
    }

    public ICUService.Key createKey(String id, int kind) {
        return LocaleKey.createWithCanonicalFallback(id, validateFallbackLocale(), kind);
    }

    public ICUService.Key createKey(ULocale l, int kind) {
        return LocaleKey.createWithCanonical(l, validateFallbackLocale(), kind);
    }
}
