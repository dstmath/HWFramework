package android.os;

public final class Trace {
    private static final int MAX_SECTION_NAME_LEN = 127;
    private static final String TAG = "Trace";
    public static final long TRACE_TAG_ACTIVITY_MANAGER = 64;
    public static final long TRACE_TAG_ALWAYS = 1;
    public static final long TRACE_TAG_APP = 4096;
    public static final long TRACE_TAG_AUDIO = 256;
    public static final long TRACE_TAG_BIONIC = 65536;
    public static final long TRACE_TAG_CAMERA = 1024;
    public static final long TRACE_TAG_DALVIK = 16384;
    public static final long TRACE_TAG_DATABASE = 1048576;
    public static final long TRACE_TAG_GRAPHICS = 2;
    public static final long TRACE_TAG_HAL = 2048;
    public static final long TRACE_TAG_INPUT = 4;
    public static final long TRACE_TAG_NEVER = 0;
    private static final long TRACE_TAG_NOT_READY = Long.MIN_VALUE;
    public static final long TRACE_TAG_PACKAGE_MANAGER = 262144;
    public static final long TRACE_TAG_POWER = 131072;
    public static final long TRACE_TAG_RESOURCES = 8192;
    public static final long TRACE_TAG_RS = 32768;
    public static final long TRACE_TAG_SYNC_MANAGER = 128;
    public static final long TRACE_TAG_SYSTEM_SERVER = 524288;
    public static final long TRACE_TAG_VIDEO = 512;
    public static final long TRACE_TAG_VIEW = 8;
    public static final long TRACE_TAG_WEBVIEW = 16;
    public static final long TRACE_TAG_WINDOW_MANAGER = 32;
    private static volatile long sEnabledTags;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.Trace.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.Trace.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.Trace.<clinit>():void");
    }

    private static native void nativeAsyncTraceBegin(long j, String str, int i);

    private static native void nativeAsyncTraceEnd(long j, String str, int i);

    private static native long nativeGetEnabledTags();

    private static native void nativeSetAppTracingAllowed(boolean z);

    private static native void nativeSetTracingEnabled(boolean z);

    private static native void nativeTraceBegin(long j, String str);

    private static native void nativeTraceBeginHwCam(long j, String str);

    private static native void nativeTraceCounter(long j, String str, int i);

    private static native void nativeTraceEnd(long j);

    private Trace() {
    }

    private static long cacheEnabledTags() {
        long tags = nativeGetEnabledTags();
        sEnabledTags = tags;
        return tags;
    }

    public static boolean isTagEnabled(long traceTag) {
        long tags = sEnabledTags;
        if (tags == TRACE_TAG_NOT_READY) {
            tags = cacheEnabledTags();
        }
        return (tags & traceTag) != TRACE_TAG_NEVER;
    }

    public static void traceCounter(long traceTag, String counterName, int counterValue) {
        if (isTagEnabled(traceTag)) {
            nativeTraceCounter(traceTag, counterName, counterValue);
        }
    }

    public static void setAppTracingAllowed(boolean allowed) {
        nativeSetAppTracingAllowed(allowed);
        cacheEnabledTags();
    }

    public static void setTracingEnabled(boolean enabled) {
        nativeSetTracingEnabled(enabled);
        cacheEnabledTags();
    }

    public static void traceBegin(long traceTag, String methodName) {
        if (isTagEnabled(traceTag)) {
            nativeTraceBegin(traceTag, methodName);
        }
    }

    public static void traceBeginHwCam(long traceTag, String methodName) {
        if (isTagEnabled(traceTag)) {
            nativeTraceBeginHwCam(traceTag, methodName);
        }
    }

    public static void traceEnd(long traceTag) {
        if (isTagEnabled(traceTag)) {
            nativeTraceEnd(traceTag);
        }
    }

    public static void asyncTraceBegin(long traceTag, String methodName, int cookie) {
        if (isTagEnabled(traceTag)) {
            nativeAsyncTraceBegin(traceTag, methodName, cookie);
        }
    }

    public static void asyncTraceEnd(long traceTag, String methodName, int cookie) {
        if (isTagEnabled(traceTag)) {
            nativeAsyncTraceEnd(traceTag, methodName, cookie);
        }
    }

    public static void beginSection(String sectionName) {
        if (!isTagEnabled(TRACE_TAG_APP)) {
            return;
        }
        if (sectionName.length() > MAX_SECTION_NAME_LEN) {
            throw new IllegalArgumentException("sectionName is too long");
        }
        nativeTraceBegin(TRACE_TAG_APP, sectionName);
    }

    public static void endSection() {
        if (isTagEnabled(TRACE_TAG_APP)) {
            nativeTraceEnd(TRACE_TAG_APP);
        }
    }
}
