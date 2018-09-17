package android.icu.impl;

import android.icu.impl.ICURWLock.Stats;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ICUService extends ICUNotifier {
    private static final boolean DEBUG = ICUDebug.enabled("service");
    private Map<String, CacheEntry> cache;
    private int defaultSize;
    private LocaleRef dnref;
    private final List<Factory> factories;
    private final ICURWLock factoryLock;
    private Map<String, Factory> idcache;
    protected final String name;

    public interface Factory {
        Object create(Key key, ICUService iCUService);

        String getDisplayName(String str, ULocale uLocale);

        void updateVisibleIDs(Map<String, Factory> map);
    }

    public static class Key {
        private final String id;

        public Key(String id) {
            this.id = id;
        }

        public final String id() {
            return this.id;
        }

        public String canonicalID() {
            return this.id;
        }

        public String currentID() {
            return canonicalID();
        }

        public String currentDescriptor() {
            return "/" + currentID();
        }

        public boolean fallback() {
            return false;
        }

        public boolean isFallbackOf(String idToCheck) {
            return canonicalID().equals(idToCheck);
        }
    }

    private static final class CacheEntry {
        final String actualDescriptor;
        final Object service;

        CacheEntry(String actualDescriptor, Object service) {
            this.actualDescriptor = actualDescriptor;
            this.service = service;
        }
    }

    private static class LocaleRef {
        private Comparator<Object> com;
        private SortedMap<String, String> dnCache;
        private final ULocale locale;

        LocaleRef(SortedMap<String, String> dnCache, ULocale locale, Comparator<Object> com) {
            this.locale = locale;
            this.com = com;
            this.dnCache = dnCache;
        }

        SortedMap<String, String> get(ULocale loc, Comparator<Object> comp) {
            SortedMap<String, String> m = this.dnCache;
            if (m == null || !this.locale.equals(loc) || (this.com != comp && (this.com == null || !this.com.equals(comp)))) {
                return null;
            }
            return m;
        }
    }

    public interface ServiceListener extends EventListener {
        void serviceChanged(ICUService iCUService);
    }

    public static class SimpleFactory implements Factory {
        protected String id;
        protected Object instance;
        protected boolean visible;

        public SimpleFactory(Object instance, String id) {
            this(instance, id, true);
        }

        public SimpleFactory(Object instance, String id, boolean visible) {
            if (instance == null || id == null) {
                throw new IllegalArgumentException("Instance or id is null");
            }
            this.instance = instance;
            this.id = id;
            this.visible = visible;
        }

        public Object create(Key key, ICUService service) {
            if (this.id.equals(key.currentID())) {
                return this.instance;
            }
            return null;
        }

        public void updateVisibleIDs(Map<String, Factory> result) {
            if (this.visible) {
                result.put(this.id, this);
            } else {
                result.remove(this.id);
            }
        }

        public String getDisplayName(String identifier, ULocale locale) {
            return (this.visible && this.id.equals(identifier)) ? identifier : null;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder(super.toString());
            buf.append(", id: ");
            buf.append(this.id);
            buf.append(", visible: ");
            buf.append(this.visible);
            return buf.toString();
        }
    }

    public ICUService() {
        this.factoryLock = new ICURWLock();
        this.factories = new ArrayList();
        this.defaultSize = 0;
        this.name = "";
    }

    public ICUService(String name) {
        this.factoryLock = new ICURWLock();
        this.factories = new ArrayList();
        this.defaultSize = 0;
        this.name = name;
    }

    public Object get(String descriptor) {
        return getKey(createKey(descriptor), null);
    }

    public Object get(String descriptor, String[] actualReturn) {
        if (descriptor != null) {
            return getKey(createKey(descriptor), actualReturn);
        }
        throw new NullPointerException("descriptor must not be null");
    }

    public Object getKey(Key key) {
        return getKey(key, null);
    }

    public Object getKey(Key key, String[] actualReturn) {
        return getKey(key, actualReturn, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:101:0x0376  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00f6 A:{Catch:{ all -> 0x00de }} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x01f3 A:{Catch:{ all -> 0x00de }} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0136 A:{Catch:{ all -> 0x00de }} */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0340  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0161 A:{Catch:{ all -> 0x00de }} */
    /* JADX WARNING: Missing block: B:27:0x00df, code:
            r23.factoryLock.releaseRead();
     */
    /* JADX WARNING: Missing block: B:28:0x00e8, code:
            throw r20;
     */
    /* JADX WARNING: Missing block: B:64:0x0267, code:
            r0 = new android.icu.impl.ICUService.CacheEntry(r7, r18);
     */
    /* JADX WARNING: Missing block: B:67:0x0272, code:
            if (DEBUG == false) goto L_0x02a0;
     */
    /* JADX WARNING: Missing block: B:68:0x0274, code:
            java.lang.System.out.println(r23.name + " factory supported: " + r7 + ", caching");
     */
    /* JADX WARNING: Missing block: B:69:0x02a0, code:
            r16 = r0;
     */
    /* JADX WARNING: Missing block: B:78:0x02d8, code:
            if (r24.fallback() != false) goto L_0x02da;
     */
    /* JADX WARNING: Missing block: B:99:0x0371, code:
            r20 = th;
     */
    /* JADX WARNING: Missing block: B:100:0x0372, code:
            r16 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Object getKey(Key key, String[] actualReturn, Factory factory) {
        if (this.factories.size() == 0) {
            return handleDefault(key, actualReturn);
        }
        if (DEBUG) {
            System.out.println("Service: " + this.name + " key: " + key.canonicalID());
        }
        if (key != null) {
            try {
                int NDebug;
                String currentDescriptor;
                CacheEntry result;
                this.factoryLock.acquireRead();
                Map<String, CacheEntry> cache = this.cache;
                if (cache == null) {
                    if (DEBUG) {
                        System.out.println("Service " + this.name + " cache was empty");
                    }
                    cache = new ConcurrentHashMap();
                }
                Iterable iterable = null;
                boolean putInCache = false;
                int NDebug2 = 0;
                int startIndex = 0;
                int limit = this.factories.size();
                boolean cacheResult = true;
                if (factory != null) {
                    for (int i = 0; i < limit; i++) {
                        if (factory == this.factories.get(i)) {
                            startIndex = i + 1;
                            break;
                        }
                    }
                    if (startIndex == 0) {
                        throw new IllegalStateException("Factory " + factory + "not registered with service: " + this);
                    }
                    cacheResult = false;
                    NDebug = 0;
                    currentDescriptor = key.currentDescriptor();
                    if (DEBUG) {
                        NDebug2 = NDebug;
                    } else {
                        NDebug2 = NDebug + 1;
                        System.out.println(this.name + "[" + NDebug + "] looking for: " + currentDescriptor);
                    }
                    result = (CacheEntry) cache.get(currentDescriptor);
                    if (result == null) {
                        if (DEBUG) {
                            System.out.println("did not find: " + currentDescriptor + " in cache");
                        }
                        putInCache = cacheResult;
                        int i2 = startIndex;
                        while (i2 < limit) {
                            int index = i2 + 1;
                            Factory f = (Factory) this.factories.get(i2);
                            if (DEBUG) {
                                System.out.println("trying factory[" + (index - 1) + "] " + f.toString());
                            }
                            Object service = f.create(key, this);
                            if (service != null) {
                                break;
                            }
                            if (DEBUG) {
                                System.out.println("factory did not support: " + currentDescriptor);
                            }
                            i2 = index;
                        }
                        if (iterable == null) {
                            iterable = new ArrayList(5);
                        }
                        iterable.add(currentDescriptor);
                    } else if (DEBUG) {
                        System.out.println(this.name + " found with descriptor: " + currentDescriptor);
                    }
                    if (result == null) {
                        if (putInCache) {
                            if (DEBUG) {
                                System.out.println("caching '" + result.actualDescriptor + "'");
                            }
                            cache.put(result.actualDescriptor, result);
                            if (r5 != null) {
                                for (String desc : r5) {
                                    if (DEBUG) {
                                        System.out.println(this.name + " adding descriptor: '" + desc + "' for actual: '" + result.actualDescriptor + "'");
                                    }
                                    cache.put(desc, result);
                                }
                            }
                            this.cache = cache;
                        }
                        if (actualReturn != null) {
                            if (result.actualDescriptor.indexOf("/") == 0) {
                                actualReturn[0] = result.actualDescriptor.substring(1);
                            } else {
                                actualReturn[0] = result.actualDescriptor;
                            }
                        }
                        if (DEBUG) {
                            System.out.println("found in service: " + this.name);
                        }
                        Object obj = result.service;
                        this.factoryLock.releaseRead();
                        return obj;
                    }
                    this.factoryLock.releaseRead();
                }
                NDebug = NDebug2;
                currentDescriptor = key.currentDescriptor();
                if (DEBUG) {
                }
                result = (CacheEntry) cache.get(currentDescriptor);
                if (result == null) {
                }
                if (result == null) {
                }
            } catch (Throwable th) {
                Throwable th2 = th;
            }
        }
        if (DEBUG) {
            System.out.println("not found in service: " + this.name);
        }
        return handleDefault(key, actualReturn);
    }

    protected Object handleDefault(Key key, String[] actualIDReturn) {
        return null;
    }

    public Set<String> getVisibleIDs() {
        return getVisibleIDs(null);
    }

    public Set<String> getVisibleIDs(String matchID) {
        Set<String> result = getVisibleIDMap().keySet();
        Key fallbackKey = createKey(matchID);
        if (fallbackKey == null) {
            return result;
        }
        Set<String> temp = new HashSet(result.size());
        for (String id : result) {
            if (fallbackKey.isFallbackOf(id)) {
                temp.add(id);
            }
        }
        return temp;
    }

    private Map<String, Factory> getVisibleIDMap() {
        synchronized (this) {
            if (this.idcache == null) {
                try {
                    this.factoryLock.acquireRead();
                    Map<String, Factory> mutableMap = new HashMap();
                    ListIterator<Factory> lIter = this.factories.listIterator(this.factories.size());
                    while (lIter.hasPrevious()) {
                        ((Factory) lIter.previous()).updateVisibleIDs(mutableMap);
                    }
                    this.idcache = Collections.unmodifiableMap(mutableMap);
                    this.factoryLock.releaseRead();
                } catch (Throwable th) {
                    this.factoryLock.releaseRead();
                }
            }
        }
        return this.idcache;
    }

    public String getDisplayName(String id) {
        return getDisplayName(id, ULocale.getDefault(Category.DISPLAY));
    }

    public String getDisplayName(String id, ULocale locale) {
        Map<String, Factory> m = getVisibleIDMap();
        Factory f = (Factory) m.get(id);
        if (f != null) {
            return f.getDisplayName(id, locale);
        }
        Key key = createKey(id);
        while (key.fallback()) {
            f = (Factory) m.get(key.currentID());
            if (f != null) {
                return f.getDisplayName(id, locale);
            }
        }
        return null;
    }

    public SortedMap<String, String> getDisplayNames() {
        return getDisplayNames(ULocale.getDefault(Category.DISPLAY), null, null);
    }

    public SortedMap<String, String> getDisplayNames(ULocale locale) {
        return getDisplayNames(locale, null, null);
    }

    public SortedMap<String, String> getDisplayNames(ULocale locale, Comparator<Object> com) {
        return getDisplayNames(locale, com, null);
    }

    public SortedMap<String, String> getDisplayNames(ULocale locale, String matchID) {
        return getDisplayNames(locale, null, matchID);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0030 A:{Catch:{ all -> 0x004a }} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x000e  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x006e A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SortedMap<String, String> getDisplayNames(ULocale locale, Comparator<Object> com, String matchID) {
        SortedMap<String, String> dncache;
        Throwable th;
        Key matchKey;
        SortedMap<String, String> dncache2 = null;
        LocaleRef ref = this.dnref;
        if (ref != null) {
            dncache = ref.get(locale, com);
            if (dncache != null) {
                synchronized (this) {
                    try {
                        try {
                            for (Entry<String, Factory> e : getVisibleIDMap().entrySet()) {
                            }
                            dncache2 = Collections.unmodifiableSortedMap(dncache2);
                            this.dnref = new LocaleRef(dncache2, locale, com);
                        } catch (Throwable th2) {
                            th = th2;
                            dncache2 = dncache;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                    if (ref == this.dnref || this.dnref == null) {
                        dncache2 = new TreeMap(com);
                        for (Entry<String, Factory> e2 : getVisibleIDMap().entrySet()) {
                            String id = (String) e2.getKey();
                            dncache2.put(((Factory) e2.getValue()).getDisplayName(id, locale), id);
                        }
                        dncache2 = Collections.unmodifiableSortedMap(dncache2);
                        this.dnref = new LocaleRef(dncache2, locale, com);
                    } else {
                        ref = this.dnref;
                        dncache2 = ref.get(locale, com);
                    }
                    dncache2 = new TreeMap(com);
                }
            }
            matchKey = createKey(matchID);
            if (matchKey != null) {
                return dncache;
            }
            SortedMap<String, String> result = new TreeMap(dncache);
            Iterator<Entry<String, String>> iter = result.entrySet().iterator();
            while (iter.hasNext()) {
                if (!matchKey.isFallbackOf((String) ((Entry) iter.next()).getValue())) {
                    iter.remove();
                }
            }
            return result;
        }
        dncache = dncache2;
        if (dncache != null) {
            matchKey = createKey(matchID);
        }
        matchKey = createKey(matchID);
        if (matchKey != null) {
        }
    }

    public final List<Factory> factories() {
        try {
            this.factoryLock.acquireRead();
            List<Factory> arrayList = new ArrayList(this.factories);
            return arrayList;
        } finally {
            this.factoryLock.releaseRead();
        }
    }

    public Factory registerObject(Object obj, String id) {
        return registerObject(obj, id, true);
    }

    public Factory registerObject(Object obj, String id, boolean visible) {
        return registerFactory(new SimpleFactory(obj, createKey(id).canonicalID(), visible));
    }

    public final Factory registerFactory(Factory factory) {
        if (factory == null) {
            throw new NullPointerException();
        }
        try {
            this.factoryLock.acquireWrite();
            this.factories.add(0, factory);
            clearCaches();
            notifyChanged();
            return factory;
        } finally {
            this.factoryLock.releaseWrite();
        }
    }

    public final boolean unregisterFactory(Factory factory) {
        if (factory == null) {
            throw new NullPointerException();
        }
        boolean result = false;
        try {
            this.factoryLock.acquireWrite();
            if (this.factories.remove(factory)) {
                result = true;
                clearCaches();
            }
            this.factoryLock.releaseWrite();
            if (result) {
                notifyChanged();
            }
            return result;
        } catch (Throwable th) {
            this.factoryLock.releaseWrite();
        }
    }

    public final void reset() {
        try {
            this.factoryLock.acquireWrite();
            reInitializeFactories();
            clearCaches();
            notifyChanged();
        } finally {
            this.factoryLock.releaseWrite();
        }
    }

    protected void reInitializeFactories() {
        this.factories.clear();
    }

    public boolean isDefault() {
        return this.factories.size() == this.defaultSize;
    }

    protected void markDefault() {
        this.defaultSize = this.factories.size();
    }

    public Key createKey(String id) {
        return id == null ? null : new Key(id);
    }

    protected void clearCaches() {
        this.cache = null;
        this.idcache = null;
        this.dnref = null;
    }

    protected void clearServiceCache() {
        this.cache = null;
    }

    protected boolean acceptsListener(EventListener l) {
        return l instanceof ServiceListener;
    }

    protected void notifyListener(EventListener l) {
        ((ServiceListener) l).serviceChanged(this);
    }

    public String stats() {
        Stats stats = this.factoryLock.resetStats();
        if (stats != null) {
            return stats.toString();
        }
        return "no stats";
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return super.toString() + "{" + this.name + "}";
    }
}
