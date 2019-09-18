package android.icu.impl;

import android.icu.impl.ICURWLock;
import android.icu.util.ULocale;
import java.io.PrintStream;
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

    private static final class CacheEntry {
        final String actualDescriptor;
        final Object service;

        CacheEntry(String actualDescriptor2, Object service2) {
            this.actualDescriptor = actualDescriptor2;
            this.service = service2;
        }
    }

    public interface Factory {
        Object create(Key key, ICUService iCUService);

        String getDisplayName(String str, ULocale uLocale);

        void updateVisibleIDs(Map<String, Factory> map);
    }

    public static class Key {
        private final String id;

        public Key(String id2) {
            this.id = id2;
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

    private static class LocaleRef {
        private Comparator<Object> com;
        private SortedMap<String, String> dnCache;
        private final ULocale locale;

        LocaleRef(SortedMap<String, String> dnCache2, ULocale locale2, Comparator<Object> com2) {
            this.locale = locale2;
            this.com = com2;
            this.dnCache = dnCache2;
        }

        /* access modifiers changed from: package-private */
        public SortedMap<String, String> get(ULocale loc, Comparator<Object> comp) {
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

        public SimpleFactory(Object instance2, String id2) {
            this(instance2, id2, true);
        }

        public SimpleFactory(Object instance2, String id2, boolean visible2) {
            if (instance2 == null || id2 == null) {
                throw new IllegalArgumentException("Instance or id is null");
            }
            this.instance = instance2;
            this.id = id2;
            this.visible = visible2;
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
            if (!this.visible || !this.id.equals(identifier)) {
                return null;
            }
            return identifier;
        }

        public String toString() {
            return super.toString() + ", id: " + this.id + ", visible: " + this.visible;
        }
    }

    public ICUService() {
        this.factoryLock = new ICURWLock();
        this.factories = new ArrayList();
        this.defaultSize = 0;
        this.name = "";
    }

    public ICUService(String name2) {
        this.factoryLock = new ICURWLock();
        this.factories = new ArrayList();
        this.defaultSize = 0;
        this.name = name2;
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

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0167, code lost:
        r4 = new android.icu.impl.ICUService.CacheEntry(r5, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x016e, code lost:
        if (DEBUG == false) goto L_0x019a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0170, code lost:
        r13 = java.lang.System.out;
        r16 = r3;
        r3 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0179, code lost:
        r17 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
        r3.append(r1.name);
        r3.append(" factory supported: ");
        r3.append(r5);
        r3.append(", caching");
        r13.println(r3.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0195, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0196, code lost:
        r4 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x019a, code lost:
        r17 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x019c, code lost:
        r4 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x019f, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x01a0, code lost:
        r17 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01c7, code lost:
        r15 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01c8, code lost:
        if (r6 != null) goto L_0x01d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x01ca, code lost:
        r6 = new java.util.ArrayList<>(5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01d6, code lost:
        r6.add(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01dd, code lost:
        if (r20.fallback() != false) goto L_0x029e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01df, code lost:
        r4 = r15;
     */
    public Object getKey(Key key, String[] actualReturn, Factory factory) {
        CacheEntry result;
        Iterator<String> it;
        CacheEntry result2;
        CacheEntry result3;
        Key key2 = key;
        Factory factory2 = factory;
        if (this.factories.size() == 0) {
            return handleDefault(key, actualReturn);
        }
        if (DEBUG) {
            PrintStream printStream = System.out;
            printStream.println("Service: " + this.name + " key: " + key.canonicalID());
        }
        if (key2 != null) {
            try {
                this.factoryLock.acquireRead();
                Map<String, CacheEntry> cache2 = this.cache;
                if (cache2 == null) {
                    if (DEBUG) {
                        PrintStream printStream2 = System.out;
                        printStream2.println("Service " + this.name + " cache was empty");
                    }
                    cache2 = new ConcurrentHashMap<>();
                }
                ArrayList<String> cacheDescriptorList = null;
                boolean putInCache = false;
                int NDebug = 0;
                int startIndex = 0;
                int limit = this.factories.size();
                boolean cacheResult = true;
                if (factory2 != null) {
                    int i = 0;
                    while (true) {
                        if (i >= limit) {
                            break;
                        } else if (factory2 == this.factories.get(i)) {
                            startIndex = i + 1;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (startIndex != 0) {
                        cacheResult = false;
                    } else {
                        throw new IllegalStateException("Factory " + factory2 + "not registered with service: " + this);
                    }
                }
                loop1:
                while (true) {
                    String currentDescriptor = key.currentDescriptor();
                    if (DEBUG) {
                        PrintStream printStream3 = System.out;
                        printStream3.println(this.name + "[" + NDebug + "] looking for: " + currentDescriptor);
                        NDebug++;
                    }
                    result = cache2.get(currentDescriptor);
                    if (result == null) {
                        try {
                            if (DEBUG) {
                                PrintStream printStream4 = System.out;
                                printStream4.println("did not find: " + currentDescriptor + " in cache");
                            }
                            putInCache = cacheResult;
                            int index = startIndex;
                            while (true) {
                                if (index >= limit) {
                                    break;
                                }
                                int index2 = index + 1;
                                Factory f = this.factories.get(index);
                                if (DEBUG) {
                                    PrintStream printStream5 = System.out;
                                    StringBuilder sb = new StringBuilder();
                                    result3 = result;
                                    try {
                                        sb.append("trying factory[");
                                        sb.append(index2 - 1);
                                        sb.append("] ");
                                        sb.append(f.toString());
                                        printStream5.println(sb.toString());
                                    } catch (Throwable th) {
                                        th = th;
                                        CacheEntry cacheEntry = result3;
                                        this.factoryLock.releaseRead();
                                        throw th;
                                    }
                                } else {
                                    result3 = result;
                                }
                                Object service = f.create(key2, this);
                                if (service != null) {
                                    break loop1;
                                }
                                Object obj = service;
                                if (DEBUG) {
                                    PrintStream printStream6 = System.out;
                                    printStream6.println("factory did not support: " + currentDescriptor);
                                }
                                index = index2;
                                result = result3;
                                Factory factory3 = factory;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            CacheEntry cacheEntry2 = result;
                            this.factoryLock.releaseRead();
                            throw th;
                        }
                    } else if (DEBUG) {
                        PrintStream printStream7 = System.out;
                        printStream7.println(this.name + " found with descriptor: " + currentDescriptor);
                    }
                    Factory factory4 = factory;
                }
                if (result != null) {
                    if (putInCache) {
                        if (DEBUG) {
                            PrintStream printStream8 = System.out;
                            printStream8.println("caching '" + result.actualDescriptor + "'");
                        }
                        cache2.put(result.actualDescriptor, result);
                        if (cacheDescriptorList != null) {
                            Iterator<String> it2 = cacheDescriptorList.iterator();
                            while (it2.hasNext()) {
                                String desc = it2.next();
                                if (DEBUG) {
                                    PrintStream printStream9 = System.out;
                                    StringBuilder sb2 = new StringBuilder();
                                    it = it2;
                                    sb2.append(this.name);
                                    sb2.append(" adding descriptor: '");
                                    sb2.append(desc);
                                    sb2.append("' for actual: '");
                                    sb2.append(result.actualDescriptor);
                                    sb2.append("'");
                                    printStream9.println(sb2.toString());
                                } else {
                                    it = it2;
                                }
                                cache2.put(desc, result);
                                it2 = it;
                            }
                        }
                        this.cache = cache2;
                    }
                    if (actualReturn != null) {
                        if (result.actualDescriptor.indexOf("/") == 0) {
                            actualReturn[0] = result.actualDescriptor.substring(1);
                        } else {
                            actualReturn[0] = result.actualDescriptor;
                        }
                    }
                    if (DEBUG) {
                        PrintStream printStream10 = System.out;
                        printStream10.println("found in service: " + this.name);
                    }
                    Object obj2 = result.service;
                    this.factoryLock.releaseRead();
                    return obj2;
                }
                this.factoryLock.releaseRead();
            } catch (Throwable th3) {
                th = th3;
                this.factoryLock.releaseRead();
                throw th;
            }
        }
        if (DEBUG) {
            PrintStream printStream11 = System.out;
            printStream11.println("not found in service: " + this.name);
        }
        return handleDefault(key, actualReturn);
    }

    /* access modifiers changed from: protected */
    public Object handleDefault(Key key, String[] actualIDReturn) {
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
        Set<String> temp = new HashSet<>(result.size());
        for (String id : result) {
            if (fallbackKey.isFallbackOf(id)) {
                temp.add(id);
            }
        }
        return temp;
    }

    /* JADX INFO: finally extract failed */
    private Map<String, Factory> getVisibleIDMap() {
        synchronized (this) {
            if (this.idcache == null) {
                try {
                    this.factoryLock.acquireRead();
                    Map<String, Factory> mutableMap = new HashMap<>();
                    ListIterator<Factory> lIter = this.factories.listIterator(this.factories.size());
                    while (lIter.hasPrevious()) {
                        lIter.previous().updateVisibleIDs(mutableMap);
                    }
                    this.idcache = Collections.unmodifiableMap(mutableMap);
                    this.factoryLock.releaseRead();
                } catch (Throwable th) {
                    this.factoryLock.releaseRead();
                    throw th;
                }
            }
        }
        return this.idcache;
    }

    public String getDisplayName(String id) {
        return getDisplayName(id, ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public String getDisplayName(String id, ULocale locale) {
        Map<String, Factory> m = getVisibleIDMap();
        Factory f = m.get(id);
        if (f != null) {
            return f.getDisplayName(id, locale);
        }
        Key key = createKey(id);
        while (key.fallback()) {
            Factory f2 = m.get(key.currentID());
            if (f2 != null) {
                return f2.getDisplayName(id, locale);
            }
        }
        return null;
    }

    public SortedMap<String, String> getDisplayNames() {
        return getDisplayNames(ULocale.getDefault(ULocale.Category.DISPLAY), null, null);
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

    public SortedMap<String, String> getDisplayNames(ULocale locale, Comparator<Object> com, String matchID) {
        SortedMap<String, String> dncache;
        SortedMap<String, String> dncache2 = null;
        LocaleRef ref = this.dnref;
        if (ref != null) {
            dncache2 = ref.get(locale, com);
        }
        while (dncache == null) {
            synchronized (this) {
                if (ref != this.dnref) {
                    if (this.dnref != null) {
                        ref = this.dnref;
                        dncache = ref.get(locale, com);
                    }
                }
                SortedMap<String, String> dncache3 = new TreeMap<>(com);
                for (Map.Entry<String, Factory> e : getVisibleIDMap().entrySet()) {
                    String id = e.getKey();
                    dncache3.put(e.getValue().getDisplayName(id, locale), id);
                }
                dncache = Collections.unmodifiableSortedMap(dncache3);
                this.dnref = new LocaleRef(dncache, locale, com);
            }
        }
        Key matchKey = createKey(matchID);
        if (matchKey == null) {
            return dncache;
        }
        SortedMap<String, String> result = new TreeMap<>(dncache);
        Iterator<Map.Entry<String, String>> iter = result.entrySet().iterator();
        while (iter.hasNext()) {
            if (!matchKey.isFallbackOf(iter.next().getValue())) {
                iter.remove();
            }
        }
        return result;
    }

    public final List<Factory> factories() {
        try {
            this.factoryLock.acquireRead();
            return new ArrayList(this.factories);
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

    /* JADX INFO: finally extract failed */
    public final Factory registerFactory(Factory factory) {
        if (factory != null) {
            try {
                this.factoryLock.acquireWrite();
                this.factories.add(0, factory);
                clearCaches();
                this.factoryLock.releaseWrite();
                notifyChanged();
                return factory;
            } catch (Throwable th) {
                this.factoryLock.releaseWrite();
                throw th;
            }
        } else {
            throw new NullPointerException();
        }
    }

    public final boolean unregisterFactory(Factory factory) {
        if (factory != null) {
            boolean result = false;
            try {
                this.factoryLock.acquireWrite();
                if (this.factories.remove(factory)) {
                    result = true;
                    clearCaches();
                }
                if (result) {
                    notifyChanged();
                }
                return result;
            } finally {
                this.factoryLock.releaseWrite();
            }
        } else {
            throw new NullPointerException();
        }
    }

    /* JADX INFO: finally extract failed */
    public final void reset() {
        try {
            this.factoryLock.acquireWrite();
            reInitializeFactories();
            clearCaches();
            this.factoryLock.releaseWrite();
            notifyChanged();
        } catch (Throwable th) {
            this.factoryLock.releaseWrite();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void reInitializeFactories() {
        this.factories.clear();
    }

    public boolean isDefault() {
        return this.factories.size() == this.defaultSize;
    }

    /* access modifiers changed from: protected */
    public void markDefault() {
        this.defaultSize = this.factories.size();
    }

    public Key createKey(String id) {
        if (id == null) {
            return null;
        }
        return new Key(id);
    }

    /* access modifiers changed from: protected */
    public void clearCaches() {
        this.cache = null;
        this.idcache = null;
        this.dnref = null;
    }

    /* access modifiers changed from: protected */
    public void clearServiceCache() {
        this.cache = null;
    }

    /* access modifiers changed from: protected */
    public boolean acceptsListener(EventListener l) {
        return l instanceof ServiceListener;
    }

    /* access modifiers changed from: protected */
    public void notifyListener(EventListener l) {
        ((ServiceListener) l).serviceChanged(this);
    }

    public String stats() {
        ICURWLock.Stats stats = this.factoryLock.resetStats();
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
