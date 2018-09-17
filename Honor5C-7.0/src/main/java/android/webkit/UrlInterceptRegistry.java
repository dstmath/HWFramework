package android.webkit;

import android.webkit.CacheManager.CacheResult;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

@Deprecated
public final class UrlInterceptRegistry {
    private static final String LOGTAG = "intercept";
    private static boolean mDisabled;
    private static LinkedList mHandlerList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.webkit.UrlInterceptRegistry.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.webkit.UrlInterceptRegistry.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.webkit.UrlInterceptRegistry.<clinit>():void");
    }

    private static synchronized LinkedList getHandlers() {
        LinkedList linkedList;
        synchronized (UrlInterceptRegistry.class) {
            if (mHandlerList == null) {
                mHandlerList = new LinkedList();
            }
            linkedList = mHandlerList;
        }
        return linkedList;
    }

    @Deprecated
    public static synchronized void setUrlInterceptDisabled(boolean disabled) {
        synchronized (UrlInterceptRegistry.class) {
            mDisabled = disabled;
        }
    }

    @Deprecated
    public static synchronized boolean urlInterceptDisabled() {
        boolean z;
        synchronized (UrlInterceptRegistry.class) {
            z = mDisabled;
        }
        return z;
    }

    @Deprecated
    public static synchronized boolean registerHandler(UrlInterceptHandler handler) {
        synchronized (UrlInterceptRegistry.class) {
            if (getHandlers().contains(handler)) {
                return false;
            }
            getHandlers().addFirst(handler);
            return true;
        }
    }

    @Deprecated
    public static synchronized boolean unregisterHandler(UrlInterceptHandler handler) {
        boolean remove;
        synchronized (UrlInterceptRegistry.class) {
            remove = getHandlers().remove(handler);
        }
        return remove;
    }

    @Deprecated
    public static synchronized CacheResult getSurrogate(String url, Map<String, String> headers) {
        synchronized (UrlInterceptRegistry.class) {
            if (urlInterceptDisabled()) {
                return null;
            }
            Iterator iter = getHandlers().listIterator();
            while (iter.hasNext()) {
                CacheResult result = ((UrlInterceptHandler) iter.next()).service(url, headers);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
    }

    @Deprecated
    public static synchronized PluginData getPluginData(String url, Map<String, String> headers) {
        synchronized (UrlInterceptRegistry.class) {
            if (urlInterceptDisabled()) {
                return null;
            }
            Iterator iter = getHandlers().listIterator();
            while (iter.hasNext()) {
                PluginData data = ((UrlInterceptHandler) iter.next()).getPluginData(url, headers);
                if (data != null) {
                    return data;
                }
            }
            return null;
        }
    }
}
