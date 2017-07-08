package android.webkit;

import android.content.Context;

@Deprecated
public final class CookieSyncManager extends WebSyncManager {
    private static boolean sGetInstanceAllowed;
    private static CookieSyncManager sRef;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.webkit.CookieSyncManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.webkit.CookieSyncManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.webkit.CookieSyncManager.<clinit>():void");
    }

    public /* bridge */ /* synthetic */ void run() {
        super.run();
    }

    private CookieSyncManager() {
        super(null, null);
    }

    public static synchronized CookieSyncManager getInstance() {
        CookieSyncManager cookieSyncManager;
        synchronized (CookieSyncManager.class) {
            checkInstanceIsAllowed();
            if (sRef == null) {
                sRef = new CookieSyncManager();
            }
            cookieSyncManager = sRef;
        }
        return cookieSyncManager;
    }

    public static synchronized CookieSyncManager createInstance(Context context) {
        CookieSyncManager instance;
        synchronized (CookieSyncManager.class) {
            if (context == null) {
                throw new IllegalArgumentException("Invalid context argument");
            }
            setGetInstanceIsAllowed();
            instance = getInstance();
        }
        return instance;
    }

    @Deprecated
    public void sync() {
        CookieManager.getInstance().flush();
    }

    @Deprecated
    protected void syncFromRamToFlash() {
        CookieManager.getInstance().flush();
    }

    @Deprecated
    public void resetSync() {
    }

    @Deprecated
    public void startSync() {
    }

    @Deprecated
    public void stopSync() {
    }

    static void setGetInstanceIsAllowed() {
        sGetInstanceAllowed = true;
    }

    private static void checkInstanceIsAllowed() {
        if (!sGetInstanceAllowed) {
            throw new IllegalStateException("CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()");
        }
    }
}
