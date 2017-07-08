package android.icu.impl;

import android.icu.impl.ICURWLock.Stats;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.xmlpull.v1.XmlPullParser;

public class ICUService extends ICUNotifier {
    private static final boolean DEBUG = false;
    private SoftReference<Map<String, CacheEntry>> cacheref;
    private int defaultSize;
    private LocaleRef dnref;
    private final List<Factory> factories;
    private final ICURWLock factoryLock;
    private SoftReference<Map<String, Factory>> idref;
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
        private final ULocale locale;
        private SoftReference<SortedMap<String, String>> ref;

        LocaleRef(SortedMap<String, String> dnCache, ULocale locale, Comparator<Object> com) {
            this.locale = locale;
            this.com = com;
            this.ref = new SoftReference(dnCache);
        }

        SortedMap<String, String> get(ULocale loc, Comparator<Object> comp) {
            SortedMap<String, String> m = (SortedMap) this.ref.get();
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.<clinit>():void");
    }

    public ICUService() {
        this.factoryLock = new ICURWLock();
        this.factories = new ArrayList();
        this.defaultSize = 0;
        this.name = XmlPullParser.NO_NAMESPACE;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Object getKey(Key key, String[] actualReturn, Factory factory) {
        Throwable th;
        if (this.factories.size() == 0) {
            return handleDefault(key, actualReturn);
        }
        if (DEBUG) {
            System.out.println("Service: " + this.name + " key: " + key.canonicalID());
        }
        if (key != null) {
            int NDebug;
            String currentDescriptor;
            CacheEntry result;
            int i;
            int index;
            Factory f;
            Object service;
            StringBuilder append;
            Object obj;
            this.factoryLock.acquireRead();
            Map map = null;
            SoftReference<Map<String, CacheEntry>> cref = this.cacheref;
            if (cref != null) {
                if (DEBUG) {
                    System.out.println("Service " + this.name + " ref exists");
                }
                map = (Map) cref.get();
            }
            if (map == null) {
                if (DEBUG) {
                    System.out.println("Service " + this.name + " cache was empty");
                }
                map = Collections.synchronizedMap(new HashMap());
                cref = new SoftReference(map);
            }
            Iterable iterable = null;
            boolean z = false;
            int NDebug2 = 0;
            int startIndex = 0;
            int limit = this.factories.size();
            boolean cacheResult = true;
            if (factory != null) {
                for (int i2 = 0; i2 < limit; i2++) {
                    if (factory == this.factories.get(i2)) {
                        startIndex = i2 + 1;
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
                result = (CacheEntry) map.get(currentDescriptor);
                if (result == null) {
                    if (DEBUG) {
                        System.out.println("did not find: " + currentDescriptor + " in cache");
                    }
                    z = cacheResult;
                    i = startIndex;
                    while (i < limit) {
                        index = i + 1;
                        f = (Factory) this.factories.get(i);
                        if (DEBUG) {
                            System.out.println("trying factory[" + (index - 1) + "] " + f.toString());
                        }
                        service = f.create(key, this);
                        if (service == null) {
                            break;
                        }
                        try {
                            if (DEBUG) {
                                System.out.println("factory did not support: " + currentDescriptor);
                            }
                            i = index;
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                    if (iterable == null) {
                        iterable = new ArrayList(5);
                    }
                    iterable.add(currentDescriptor);
                } else if (DEBUG) {
                    System.out.println(this.name + " found with descriptor: " + currentDescriptor);
                }
                if (result == null) {
                    if (z) {
                        if (DEBUG) {
                            System.out.println("caching '" + result.actualDescriptor + "'");
                        }
                        map.put(result.actualDescriptor, result);
                        if (r5 != null) {
                            for (String desc : r5) {
                                if (DEBUG) {
                                    append = new StringBuilder().append(this.name).append(" adding descriptor: '");
                                    System.out.println(r22.append(desc).append("' for actual: '").append(result.actualDescriptor).append("'").toString());
                                }
                                map.put(desc, result);
                            }
                        }
                        this.cacheref = cref;
                    }
                    if (actualReturn != null) {
                        if (result.actualDescriptor.indexOf("/") != 0) {
                            actualReturn[0] = result.actualDescriptor.substring(1);
                        } else {
                            actualReturn[0] = result.actualDescriptor;
                        }
                    }
                    if (DEBUG) {
                        System.out.println("found in service: " + this.name);
                    }
                    obj = result.service;
                    this.factoryLock.releaseRead();
                    return obj;
                }
                this.factoryLock.releaseRead();
            }
            NDebug = NDebug2;
            currentDescriptor = key.currentDescriptor();
            if (DEBUG) {
                NDebug2 = NDebug;
            } else {
                NDebug2 = NDebug + 1;
                System.out.println(this.name + "[" + NDebug + "] looking for: " + currentDescriptor);
            }
            result = (CacheEntry) map.get(currentDescriptor);
            if (result == null) {
                if (DEBUG) {
                    System.out.println("did not find: " + currentDescriptor + " in cache");
                }
                z = cacheResult;
                i = startIndex;
                while (i < limit) {
                    index = i + 1;
                    f = (Factory) this.factories.get(i);
                    if (DEBUG) {
                        System.out.println("trying factory[" + (index - 1) + "] " + f.toString());
                    }
                    service = f.create(key, this);
                    if (service == null) {
                        if (DEBUG) {
                            System.out.println("factory did not support: " + currentDescriptor);
                        }
                        i = index;
                    } else {
                        break;
                        CacheEntry cacheEntry = new CacheEntry(currentDescriptor, service);
                        try {
                            if (DEBUG) {
                                System.out.println(this.name + " factory supported: " + currentDescriptor + ", caching");
                            }
                            result = cacheEntry;
                        } catch (Throwable th3) {
                            th = th3;
                            result = cacheEntry;
                            this.factoryLock.releaseRead();
                            throw th;
                        }
                    }
                }
                if (iterable == null) {
                    iterable = new ArrayList(5);
                }
                iterable.add(currentDescriptor);
            } else if (DEBUG) {
                System.out.println(this.name + " found with descriptor: " + currentDescriptor);
            }
            if (result == null) {
                this.factoryLock.releaseRead();
            } else {
                if (z) {
                    if (DEBUG) {
                        System.out.println("caching '" + result.actualDescriptor + "'");
                    }
                    map.put(result.actualDescriptor, result);
                    if (r5 != null) {
                        for (String desc2 : r5) {
                            if (DEBUG) {
                                append = new StringBuilder().append(this.name).append(" adding descriptor: '");
                                System.out.println(r22.append(desc2).append("' for actual: '").append(result.actualDescriptor).append("'").toString());
                            }
                            map.put(desc2, result);
                        }
                    }
                    this.cacheref = cref;
                }
                if (actualReturn != null) {
                    if (result.actualDescriptor.indexOf("/") != 0) {
                        actualReturn[0] = result.actualDescriptor;
                    } else {
                        actualReturn[0] = result.actualDescriptor.substring(1);
                    }
                }
                if (DEBUG) {
                    System.out.println("found in service: " + this.name);
                }
                obj = result.service;
                this.factoryLock.releaseRead();
                return obj;
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

    private java.util.Map<java.lang.String, android.icu.impl.ICUService.Factory> getVisibleIDMap() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r7 = this;
        r1 = 0;
        r4 = r7.idref;
        if (r4 == 0) goto L_0x0058;
    L_0x0005:
        r1 = r4.get();
        r1 = (java.util.Map) r1;
        r2 = r1;
    L_0x000c:
        if (r2 != 0) goto L_0x0063;
    L_0x000e:
        monitor-enter(r7);
        r5 = r7.idref;	 Catch:{ all -> 0x0064 }
        if (r4 == r5) goto L_0x0017;	 Catch:{ all -> 0x0064 }
    L_0x0013:
        r5 = r7.idref;	 Catch:{ all -> 0x0064 }
        if (r5 != 0) goto L_0x005a;
    L_0x0017:
        r5 = r7.factoryLock;	 Catch:{ all -> 0x0067 }
        r5.acquireRead();	 Catch:{ all -> 0x0067 }
        r1 = new java.util.HashMap;	 Catch:{ all -> 0x0067 }
        r1.<init>();	 Catch:{ all -> 0x0067 }
        r5 = r7.factories;	 Catch:{ all -> 0x003d }
        r6 = r7.factories;	 Catch:{ all -> 0x003d }
        r6 = r6.size();	 Catch:{ all -> 0x003d }
        r3 = r5.listIterator(r6);	 Catch:{ all -> 0x003d }
    L_0x002d:
        r5 = r3.hasPrevious();	 Catch:{ all -> 0x003d }
        if (r5 == 0) goto L_0x0047;	 Catch:{ all -> 0x003d }
    L_0x0033:
        r0 = r3.previous();	 Catch:{ all -> 0x003d }
        r0 = (android.icu.impl.ICUService.Factory) r0;	 Catch:{ all -> 0x003d }
        r0.updateVisibleIDs(r1);	 Catch:{ all -> 0x003d }
        goto L_0x002d;
    L_0x003d:
        r5 = move-exception;
    L_0x003e:
        r6 = r7.factoryLock;	 Catch:{ all -> 0x0044 }
        r6.releaseRead();	 Catch:{ all -> 0x0044 }
        throw r5;	 Catch:{ all -> 0x0044 }
    L_0x0044:
        r5 = move-exception;
    L_0x0045:
        monitor-exit(r7);
        throw r5;
    L_0x0047:
        r1 = java.util.Collections.unmodifiableMap(r1);	 Catch:{ all -> 0x003d }
        r5 = new java.lang.ref.SoftReference;	 Catch:{ all -> 0x003d }
        r5.<init>(r1);	 Catch:{ all -> 0x003d }
        r7.idref = r5;	 Catch:{ all -> 0x003d }
        r5 = r7.factoryLock;	 Catch:{ all -> 0x0044 }
        r5.releaseRead();	 Catch:{ all -> 0x0044 }
    L_0x0057:
        monitor-exit(r7);
    L_0x0058:
        r2 = r1;
        goto L_0x000c;
    L_0x005a:
        r4 = r7.idref;	 Catch:{ all -> 0x0064 }
        r1 = r4.get();	 Catch:{ all -> 0x0064 }
        r1 = (java.util.Map) r1;	 Catch:{ all -> 0x0064 }
        goto L_0x0057;
    L_0x0063:
        return r2;
    L_0x0064:
        r5 = move-exception;
        r1 = r2;
        goto L_0x0045;
    L_0x0067:
        r5 = move-exception;
        r1 = r2;
        goto L_0x003e;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.getVisibleIDMap():java.util.Map<java.lang.String, android.icu.impl.ICUService$Factory>");
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

    public java.util.SortedMap<java.lang.String, java.lang.String> getDisplayNames(android.icu.util.ULocale r15, java.util.Comparator<java.lang.Object> r16, java.lang.String r17) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r14 = this;
        r1 = 0;
        r11 = r14.dnref;
        if (r11 == 0) goto L_0x005b;
    L_0x0005:
        r0 = r16;
        r1 = r11.get(r15, r0);
        r2 = r1;
    L_0x000c:
        if (r2 != 0) goto L_0x0066;
    L_0x000e:
        monitor-enter(r14);
        r13 = r14.dnref;	 Catch:{ all -> 0x0099 }
        if (r11 == r13) goto L_0x0017;	 Catch:{ all -> 0x0099 }
    L_0x0013:
        r13 = r14.dnref;	 Catch:{ all -> 0x0099 }
        if (r13 != 0) goto L_0x005d;	 Catch:{ all -> 0x0099 }
    L_0x0017:
        r1 = new java.util.TreeMap;	 Catch:{ all -> 0x0099 }
        r0 = r16;	 Catch:{ all -> 0x0099 }
        r1.<init>(r0);	 Catch:{ all -> 0x0099 }
        r9 = r14.getVisibleIDMap();	 Catch:{ all -> 0x004a }
        r13 = r9.entrySet();	 Catch:{ all -> 0x004a }
        r5 = r13.iterator();	 Catch:{ all -> 0x004a }
    L_0x002a:
        r13 = r5.hasNext();	 Catch:{ all -> 0x004a }
        if (r13 == 0) goto L_0x004d;	 Catch:{ all -> 0x004a }
    L_0x0030:
        r3 = r5.next();	 Catch:{ all -> 0x004a }
        r3 = (java.util.Map.Entry) r3;	 Catch:{ all -> 0x004a }
        r7 = r3.getKey();	 Catch:{ all -> 0x004a }
        r7 = (java.lang.String) r7;	 Catch:{ all -> 0x004a }
        r6 = r3.getValue();	 Catch:{ all -> 0x004a }
        r6 = (android.icu.impl.ICUService.Factory) r6;	 Catch:{ all -> 0x004a }
        r13 = r6.getDisplayName(r7, r15);	 Catch:{ all -> 0x004a }
        r1.put(r13, r7);	 Catch:{ all -> 0x004a }
        goto L_0x002a;
    L_0x004a:
        r13 = move-exception;
    L_0x004b:
        monitor-exit(r14);
        throw r13;
    L_0x004d:
        r1 = java.util.Collections.unmodifiableSortedMap(r1);	 Catch:{ all -> 0x004a }
        r13 = new android.icu.impl.ICUService$LocaleRef;	 Catch:{ all -> 0x004a }
        r0 = r16;	 Catch:{ all -> 0x004a }
        r13.<init>(r1, r15, r0);	 Catch:{ all -> 0x004a }
        r14.dnref = r13;	 Catch:{ all -> 0x004a }
    L_0x005a:
        monitor-exit(r14);
    L_0x005b:
        r2 = r1;
        goto L_0x000c;
    L_0x005d:
        r11 = r14.dnref;	 Catch:{ all -> 0x0099 }
        r0 = r16;	 Catch:{ all -> 0x0099 }
        r1 = r11.get(r15, r0);	 Catch:{ all -> 0x0099 }
        goto L_0x005a;
    L_0x0066:
        r0 = r17;
        r10 = r14.createKey(r0);
        if (r10 != 0) goto L_0x006f;
    L_0x006e:
        return r2;
    L_0x006f:
        r12 = new java.util.TreeMap;
        r12.<init>(r2);
        r13 = r12.entrySet();
        r8 = r13.iterator();
    L_0x007c:
        r13 = r8.hasNext();
        if (r13 == 0) goto L_0x0098;
    L_0x0082:
        r4 = r8.next();
        r4 = (java.util.Map.Entry) r4;
        r13 = r4.getValue();
        r13 = (java.lang.String) r13;
        r13 = r10.isFallbackOf(r13);
        if (r13 != 0) goto L_0x007c;
    L_0x0094:
        r8.remove();
        goto L_0x007c;
    L_0x0098:
        return r12;
    L_0x0099:
        r13 = move-exception;
        r1 = r2;
        goto L_0x004b;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUService.getDisplayNames(android.icu.util.ULocale, java.util.Comparator, java.lang.String):java.util.SortedMap<java.lang.String, java.lang.String>");
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
        this.cacheref = null;
        this.idref = null;
        this.dnref = null;
    }

    protected void clearServiceCache() {
        this.cacheref = null;
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
