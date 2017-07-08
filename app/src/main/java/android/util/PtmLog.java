package android.util;

public final class PtmLog {
    public static final String EVENT_KEY = "event";
    public static final int HW_LOG_ID_PTM = 3;
    public static final String KEY_VAL_SEP = "=";
    public static final String PAIRE_DELIMETER = ",";
    private static String PTM_DEVICE_NODE = null;
    private static final String TAG = "PtmLog";
    private static boolean mIsBetaVer;
    private static boolean mIsPtmLogSupport;

    public interface PTM_EVENT {
        public static final int PTM_ANDROID_WAKEUP_ALARM = 10;
        public static final int PTM_PERF_BOOST = 11;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.PtmLog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.PtmLog.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.util.PtmLog.<clinit>():void");
    }

    public static native int print_ptmlog_native(int i, int i2, String str);

    private PtmLog() {
    }

    public static int w(int eventId, String eventStr) {
        if (!mIsPtmLogSupport) {
            return 0;
        }
        if (mIsBetaVer) {
            Log.w(TAG, "write eventId = " + eventId + ", eventStr = " + eventStr);
        }
        return print_ptmlog_native(5, eventId, eventStr);
    }
}
