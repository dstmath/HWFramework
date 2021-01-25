package ohos.tools;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* renamed from: ohos.tools.Bytrace  reason: case insensitive filesystem */
public final class C0000Bytrace {
    public static final int BYTRACE_DOMAIN = 218116865;
    public static final long BYTRACE_TAG_ABILITY_MANAGER = 2147483648L;
    public static final long BYTRACE_TAG_ACE = 549755813888L;
    public static final long BYTRACE_TAG_ALWAYS = 2;
    public static final long BYTRACE_TAG_DISTRIBUTEDDATA = 68719476736L;
    public static final long BYTRACE_TAG_GRAPHIC_AGP = 274877906944L;
    public static final long BYTRACE_TAG_MDFS = 137438953472L;
    public static final long BYTRACE_TAG_NEVER = 0;
    public static final long BYTRACE_TAG_NOTIFICATION = 1099511627776L;
    public static final long BYTRACE_TAG_ZAUDIO = 34359738368L;
    public static final long BYTRACE_TAG_ZCAMERA = 4294967296L;
    public static final long BYTRACE_TAG_ZIDANE = 1073741824;
    public static final long BYTRACE_TAG_ZIMAGE = 17179869184L;
    public static final long BYTRACE_TAG_ZMEDIA = 8589934592L;
    public static final HiLogLabel LABEL = new HiLogLabel(3, BYTRACE_DOMAIN, "Bytrace");
    private static final String LIBRARY_NAME = "bytrace_jni.z";
    private static final int MAX_SECTION_NAME_LEN = 127;
    private static boolean isLibraryLoaded;

    private static native void nativeCountTrace(long j, String str, int i);

    private static native void nativeFinishAsyncTrace(long j, String str, int i);

    private static native void nativeFinishTrace(long j, String str);

    private static native void nativeStartAsyncTrace(long j, String str, int i, float f);

    private static native void nativeStartTrace(long j, String str, float f);

    private static native void nativeUpdateTraceLabel();

    static {
        isLibraryLoaded = false;
        try {
            System.loadLibrary(LIBRARY_NAME);
            isLibraryLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            HiLog.error(LABEL, "%{public}s is not load. %{public}s", LIBRARY_NAME, e.getMessage());
            isLibraryLoaded = false;
        }
    }

    private C0000Bytrace() {
    }

    private static String cleanesString(String str) {
        String replaceAll = str.replaceAll("\n|\r|\\|", " ");
        return replaceAll.length() > MAX_SECTION_NAME_LEN ? replaceAll.substring(0, MAX_SECTION_NAME_LEN) : replaceAll;
    }

    public static void updateTraceLabel() {
        nativeUpdateTraceLabel();
    }

    public static void startTrace(long j, String str, float f) {
        if (isLibraryLoaded && str != null && str.length() > 0) {
            nativeStartTrace(j, cleanesString(str), f);
        }
    }

    public static void startTrace(long j, String str) {
        startTrace(j, str, -1.0f);
    }

    public static void finishTrace(long j, String str) {
        if (isLibraryLoaded && str != null && str.length() > 0) {
            cleanesString(str);
            nativeFinishTrace(j, str);
        }
    }

    public static void startAsyncTrace(long j, String str, int i, float f) {
        if (isLibraryLoaded && str != null && str.length() > 0) {
            nativeStartAsyncTrace(j, cleanesString(str), i, f);
        }
    }

    public static void startAsyncTrace(long j, String str, int i) {
        startAsyncTrace(j, str, i, -1.0f);
    }

    public static void finishAsyncTrace(long j, String str, int i) {
        if (isLibraryLoaded && str != null && str.length() > 0) {
            nativeFinishAsyncTrace(j, cleanesString(str), i);
        }
    }

    public static void countTrace(long j, String str, int i) {
        if (isLibraryLoaded && str != null && str.length() > 0) {
            nativeCountTrace(j, cleanesString(str), i);
        }
    }
}
