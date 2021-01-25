package ohos.global.icu.impl;

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
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.event.notification.NotificationRequest;
import ohos.global.icu.impl.ICURWLock;
import ohos.global.icu.util.ULocale;

public class ICUService extends ICUNotifier {
    private static final boolean DEBUG = ICUDebug.enabled(NotificationRequest.CLASSIFICATION_SERVICE);
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

    public interface ServiceListener extends EventListener {
        void serviceChanged(ICUService iCUService);
    }

    /* access modifiers changed from: protected */
    public Object handleDefault(Key key, String[] strArr) {
        return null;
    }

    public ICUService() {
        this.factoryLock = new ICURWLock();
        this.factories = new ArrayList();
        this.defaultSize = 0;
        this.name = "";
    }

    public ICUService(String str) {
        this.factoryLock = new ICURWLock();
        this.factories = new ArrayList();
        this.defaultSize = 0;
        this.name = str;
    }

    public static class Key {
        private final String id;

        public boolean fallback() {
            return false;
        }

        public Key(String str) {
            this.id = str;
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
            return PsuedoNames.PSEUDONAME_ROOT + currentID();
        }

        public boolean isFallbackOf(String str) {
            return canonicalID().equals(str);
        }
    }

    public static class SimpleFactory implements Factory {
        protected String id;
        protected Object instance;
        protected boolean visible;

        public SimpleFactory(Object obj, String str) {
            this(obj, str, true);
        }

        public SimpleFactory(Object obj, String str, boolean z) {
            if (obj == null || str == null) {
                throw new IllegalArgumentException("Instance or id is null");
            }
            this.instance = obj;
            this.id = str;
            this.visible = z;
        }

        @Override // ohos.global.icu.impl.ICUService.Factory
        public Object create(Key key, ICUService iCUService) {
            if (this.id.equals(key.currentID())) {
                return this.instance;
            }
            return null;
        }

        @Override // ohos.global.icu.impl.ICUService.Factory
        public void updateVisibleIDs(Map<String, Factory> map) {
            if (this.visible) {
                map.put(this.id, this);
            } else {
                map.remove(this.id);
            }
        }

        @Override // ohos.global.icu.impl.ICUService.Factory
        public String getDisplayName(String str, ULocale uLocale) {
            if (!this.visible || !this.id.equals(str)) {
                return null;
            }
            return str;
        }

        public String toString() {
            return super.toString() + ", id: " + this.id + ", visible: " + this.visible;
        }
    }

    public Object get(String str) {
        return getKey(createKey(str), null);
    }

    public Object get(String str, String[] strArr) {
        if (str != null) {
            return getKey(createKey(str), strArr);
        }
        throw new NullPointerException("descriptor must not be null");
    }

    public Object getKey(Key key) {
        return getKey(key, null);
    }

    public Object getKey(Key key, String[] strArr) {
        return getKey(key, strArr, null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:56:0x01ab, code lost:
        if (r9 != null) goto L_0x01b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x01ad, code lost:
        r9 = new java.util.ArrayList(5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01b4, code lost:
        r9.add(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x01bb, code lost:
        if (r17.fallback() != false) goto L_0x0270;
     */
    public Object getKey(Key key, String[] strArr, Factory factory) {
        ArrayList arrayList;
        int i;
        boolean z;
        int i2;
        CacheEntry cacheEntry;
        if (this.factories.size() == 0) {
            return handleDefault(key, strArr);
        }
        if (DEBUG) {
            System.out.println("Service: " + this.name + " key: " + key.canonicalID());
        }
        if (key != null) {
            try {
                this.factoryLock.acquireRead();
                Map<String, CacheEntry> map = this.cache;
                if (map == null) {
                    if (DEBUG) {
                        System.out.println("Service " + this.name + " cache was empty");
                    }
                    map = new ConcurrentHashMap<>();
                }
                int size = this.factories.size();
                if (factory != null) {
                    int i3 = 0;
                    while (true) {
                        if (i3 >= size) {
                            i = 0;
                            break;
                        } else if (factory == this.factories.get(i3)) {
                            i = i3 + 1;
                            break;
                        } else {
                            i3++;
                        }
                    }
                    if (i != 0) {
                        arrayList = null;
                        i2 = 0;
                        z = false;
                    } else {
                        throw new IllegalStateException("Factory " + factory + "not registered with service: " + this);
                    }
                } else {
                    arrayList = null;
                    z = true;
                    i2 = 0;
                    i = 0;
                }
                boolean z2 = false;
                while (true) {
                    String currentDescriptor = key.currentDescriptor();
                    if (DEBUG) {
                        System.out.println(this.name + "[" + i2 + "] looking for: " + currentDescriptor);
                        i2++;
                    }
                    cacheEntry = map.get(currentDescriptor);
                    if (cacheEntry == null) {
                        if (DEBUG) {
                            System.out.println("did not find: " + currentDescriptor + " in cache");
                        }
                        int i4 = i;
                        while (true) {
                            if (i4 >= size) {
                                break;
                            }
                            int i5 = i4 + 1;
                            Factory factory2 = this.factories.get(i4);
                            if (DEBUG) {
                                PrintStream printStream = System.out;
                                StringBuilder sb = new StringBuilder();
                                sb.append("trying factory[");
                                sb.append(i5 - 1);
                                sb.append("] ");
                                sb.append(factory2.toString());
                                printStream.println(sb.toString());
                            }
                            Object create = factory2.create(key, this);
                            if (create != null) {
                                cacheEntry = new CacheEntry(currentDescriptor, create);
                                if (DEBUG) {
                                    System.out.println(this.name + " factory supported: " + currentDescriptor + ", caching");
                                }
                            } else {
                                if (DEBUG) {
                                    System.out.println("factory did not support: " + currentDescriptor);
                                }
                                i4 = i5;
                            }
                        }
                    } else {
                        if (DEBUG) {
                            System.out.println(this.name + " found with descriptor: " + currentDescriptor);
                        }
                        z = z2;
                    }
                    z2 = z;
                }
                if (cacheEntry != null) {
                    if (z) {
                        if (DEBUG) {
                            System.out.println("caching '" + cacheEntry.actualDescriptor + "'");
                        }
                        map.put(cacheEntry.actualDescriptor, cacheEntry);
                        if (arrayList != null) {
                            Iterator it = arrayList.iterator();
                            while (it.hasNext()) {
                                String str = (String) it.next();
                                if (DEBUG) {
                                    System.out.println(this.name + " adding descriptor: '" + str + "' for actual: '" + cacheEntry.actualDescriptor + "'");
                                }
                                map.put(str, cacheEntry);
                            }
                        }
                        this.cache = map;
                    }
                    if (strArr != null) {
                        if (cacheEntry.actualDescriptor.indexOf(PsuedoNames.PSEUDONAME_ROOT) == 0) {
                            strArr[0] = cacheEntry.actualDescriptor.substring(1);
                        } else {
                            strArr[0] = cacheEntry.actualDescriptor;
                        }
                    }
                    if (DEBUG) {
                        System.out.println("found in service: " + this.name);
                    }
                    return cacheEntry.service;
                }
                this.factoryLock.releaseRead();
            } finally {
                this.factoryLock.releaseRead();
            }
        }
        if (DEBUG) {
            System.out.println("not found in service: " + this.name);
        }
        return handleDefault(key, strArr);
    }

    /* access modifiers changed from: private */
    public static final class CacheEntry {
        final String actualDescriptor;
        final Object service;

        CacheEntry(String str, Object obj) {
            this.actualDescriptor = str;
            this.service = obj;
        }
    }

    public Set<String> getVisibleIDs() {
        return getVisibleIDs(null);
    }

    public Set<String> getVisibleIDs(String str) {
        Set<String> keySet = getVisibleIDMap().keySet();
        Key createKey = createKey(str);
        if (createKey == null) {
            return keySet;
        }
        HashSet hashSet = new HashSet(keySet.size());
        for (String str2 : keySet) {
            if (createKey.isFallbackOf(str2)) {
                hashSet.add(str2);
            }
        }
        return hashSet;
    }

    private Map<String, Factory> getVisibleIDMap() {
        synchronized (this) {
            if (this.idcache == null) {
                try {
                    this.factoryLock.acquireRead();
                    HashMap hashMap = new HashMap();
                    ListIterator<Factory> listIterator = this.factories.listIterator(this.factories.size());
                    while (listIterator.hasPrevious()) {
                        listIterator.previous().updateVisibleIDs(hashMap);
                    }
                    this.idcache = Collections.unmodifiableMap(hashMap);
                } finally {
                    this.factoryLock.releaseRead();
                }
            }
        }
        return this.idcache;
    }

    public String getDisplayName(String str) {
        return getDisplayName(str, ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public String getDisplayName(String str, ULocale uLocale) {
        Map<String, Factory> visibleIDMap = getVisibleIDMap();
        Factory factory = visibleIDMap.get(str);
        if (factory != null) {
            return factory.getDisplayName(str, uLocale);
        }
        Key createKey = createKey(str);
        while (createKey.fallback()) {
            Factory factory2 = visibleIDMap.get(createKey.currentID());
            if (factory2 != null) {
                return factory2.getDisplayName(str, uLocale);
            }
        }
        return null;
    }

    public SortedMap<String, String> getDisplayNames() {
        return getDisplayNames(ULocale.getDefault(ULocale.Category.DISPLAY), null, null);
    }

    public SortedMap<String, String> getDisplayNames(ULocale uLocale) {
        return getDisplayNames(uLocale, null, null);
    }

    public SortedMap<String, String> getDisplayNames(ULocale uLocale, Comparator<Object> comparator) {
        return getDisplayNames(uLocale, comparator, null);
    }

    public SortedMap<String, String> getDisplayNames(ULocale uLocale, String str) {
        return getDisplayNames(uLocale, null, str);
    }

    public SortedMap<String, String> getDisplayNames(ULocale uLocale, Comparator<Object> comparator, String str) {
        LocaleRef localeRef = this.dnref;
        SortedMap<String, String> sortedMap = localeRef != null ? localeRef.get(uLocale, comparator) : null;
        while (sortedMap == null) {
            synchronized (this) {
                if (localeRef != this.dnref) {
                    if (this.dnref != null) {
                        localeRef = this.dnref;
                        sortedMap = localeRef.get(uLocale, comparator);
                    }
                }
                TreeMap treeMap = new TreeMap(comparator);
                for (Map.Entry<String, Factory> entry : getVisibleIDMap().entrySet()) {
                    String key = entry.getKey();
                    treeMap.put(entry.getValue().getDisplayName(key, uLocale), key);
                }
                sortedMap = Collections.unmodifiableSortedMap(treeMap);
                this.dnref = new LocaleRef(sortedMap, uLocale, comparator);
            }
        }
        Key createKey = createKey(str);
        if (createKey == null) {
            return sortedMap;
        }
        TreeMap treeMap2 = new TreeMap((SortedMap) sortedMap);
        Iterator it = treeMap2.entrySet().iterator();
        while (it.hasNext()) {
            if (!createKey.isFallbackOf((String) ((Map.Entry) it.next()).getValue())) {
                it.remove();
            }
        }
        return treeMap2;
    }

    /* access modifiers changed from: private */
    public static class LocaleRef {

        /* renamed from: com  reason: collision with root package name */
        private Comparator<Object> f0com;
        private SortedMap<String, String> dnCache;
        private final ULocale locale;

        LocaleRef(SortedMap<String, String> sortedMap, ULocale uLocale, Comparator<Object> comparator) {
            this.locale = uLocale;
            this.f0com = comparator;
            this.dnCache = sortedMap;
        }

        /* access modifiers changed from: package-private */
        public SortedMap<String, String> get(ULocale uLocale, Comparator<Object> comparator) {
            SortedMap<String, String> sortedMap = this.dnCache;
            if (sortedMap == null || !this.locale.equals(uLocale)) {
                return null;
            }
            Comparator<Object> comparator2 = this.f0com;
            if (comparator2 == comparator || (comparator2 != null && comparator2.equals(comparator))) {
                return sortedMap;
            }
            return null;
        }
    }

    public final List<Factory> factories() {
        try {
            this.factoryLock.acquireRead();
            return new ArrayList(this.factories);
        } finally {
            this.factoryLock.releaseRead();
        }
    }

    public Factory registerObject(Object obj, String str) {
        return registerObject(obj, str, true);
    }

    public Factory registerObject(Object obj, String str, boolean z) {
        return registerFactory(new SimpleFactory(obj, createKey(str).canonicalID(), z));
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
            boolean z = false;
            try {
                this.factoryLock.acquireWrite();
                if (this.factories.remove(factory)) {
                    z = true;
                    clearCaches();
                }
                if (z) {
                    notifyChanged();
                }
                return z;
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

    public Key createKey(String str) {
        if (str == null) {
            return null;
        }
        return new Key(str);
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
    @Override // ohos.global.icu.impl.ICUNotifier
    public boolean acceptsListener(EventListener eventListener) {
        return eventListener instanceof ServiceListener;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.ICUNotifier
    public void notifyListener(EventListener eventListener) {
        ((ServiceListener) eventListener).serviceChanged(this);
    }

    public String stats() {
        ICURWLock.Stats resetStats = this.factoryLock.resetStats();
        return resetStats != null ? resetStats.toString() : "no stats";
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return super.toString() + "{" + this.name + "}";
    }
}
