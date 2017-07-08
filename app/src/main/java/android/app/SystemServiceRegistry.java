package android.app;

import android.content.Context;
import java.util.HashMap;

final class SystemServiceRegistry {
    private static final HashMap<String, ServiceFetcher<?>> SYSTEM_SERVICE_FETCHERS = null;
    private static final HashMap<Class<?>, String> SYSTEM_SERVICE_NAMES = null;
    private static final String TAG = "SystemServiceRegistry";
    private static int sServiceCacheSize;

    interface ServiceFetcher<T> {
        T getService(ContextImpl contextImpl);
    }

    static abstract class CachedServiceFetcher<T> implements ServiceFetcher<T> {
        private final int mCacheIndex;

        public abstract T createService(ContextImpl contextImpl);

        public CachedServiceFetcher() {
            int -get0 = SystemServiceRegistry.sServiceCacheSize;
            SystemServiceRegistry.sServiceCacheSize = -get0 + 1;
            this.mCacheIndex = -get0;
        }

        public final T getService(ContextImpl ctx) {
            Object service;
            Object[] cache = ctx.mServiceCache;
            synchronized (cache) {
                service = cache[this.mCacheIndex];
                if (service == null) {
                    service = createService(ctx);
                    cache[this.mCacheIndex] = service;
                }
            }
            return service;
        }
    }

    static abstract class StaticServiceFetcher<T> implements ServiceFetcher<T> {
        private T mCachedInstance;

        public abstract T createService();

        StaticServiceFetcher() {
        }

        public final T getService(ContextImpl unused) {
            T t;
            synchronized (this) {
                if (this.mCachedInstance == null) {
                    this.mCachedInstance = createService();
                }
                t = this.mCachedInstance;
            }
            return t;
        }
    }

    static abstract class StaticApplicationContextServiceFetcher<T> implements ServiceFetcher<T> {
        private T mCachedInstance;

        public abstract T createService(Context context);

        StaticApplicationContextServiceFetcher() {
        }

        public final T getService(ContextImpl ctx) {
            T t;
            synchronized (this) {
                if (this.mCachedInstance == null) {
                    Context appContext = ctx.getApplicationContext();
                    if (appContext == null) {
                        appContext = ctx;
                    }
                    this.mCachedInstance = createService(appContext);
                }
                t = this.mCachedInstance;
            }
            return t;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.SystemServiceRegistry.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.SystemServiceRegistry.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.SystemServiceRegistry.<clinit>():void");
    }

    private SystemServiceRegistry() {
    }

    public static Object[] createServiceCache() {
        return new Object[sServiceCacheSize];
    }

    public static Object getSystemService(ContextImpl ctx, String name) {
        ServiceFetcher<?> fetcher = (ServiceFetcher) SYSTEM_SERVICE_FETCHERS.get(name);
        if (fetcher != null) {
            return fetcher.getService(ctx);
        }
        return null;
    }

    public static String getSystemServiceName(Class<?> serviceClass) {
        return (String) SYSTEM_SERVICE_NAMES.get(serviceClass);
    }

    private static <T> void registerService(String serviceName, Class<T> serviceClass, ServiceFetcher<T> serviceFetcher) {
        SYSTEM_SERVICE_NAMES.put(serviceClass, serviceName);
        SYSTEM_SERVICE_FETCHERS.put(serviceName, serviceFetcher);
    }
}
