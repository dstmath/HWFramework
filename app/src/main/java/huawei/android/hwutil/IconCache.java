package huawei.android.hwutil;

import java.util.HashMap;

public class IconCache {
    private static final HashMap<String, CacheEntry> mCache = null;

    public static class CacheEntry {
        public String name;
        public int type;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.hwutil.IconCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.hwutil.IconCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.hwutil.IconCache.<clinit>():void");
    }

    public static void add(String idAndPackageName, CacheEntry entry) {
        synchronized (mCache) {
            if (!mCache.containsKey(idAndPackageName)) {
                mCache.put(idAndPackageName, entry);
            }
        }
    }

    public static boolean contains(String idAndPackageName) {
        boolean containsKey;
        synchronized (mCache) {
            containsKey = mCache.containsKey(idAndPackageName);
        }
        return containsKey;
    }

    public static CacheEntry get(String idAndPackageName) {
        CacheEntry cacheEntry;
        synchronized (mCache) {
            cacheEntry = (CacheEntry) mCache.get(idAndPackageName);
        }
        return cacheEntry;
    }

    public static void remove(String idAndPackageName) {
        synchronized (mCache) {
            mCache.remove(idAndPackageName);
        }
    }

    public static void update(String idAndPackageName, CacheEntry entry) {
        synchronized (mCache) {
            mCache.put(idAndPackageName, entry);
        }
    }
}
