package android.util;

public final class HwEtrace {
    public static final int ETRACE_ASYNC_CALL = 2;
    public static final int ETRACE_CLIENT_RECV = 8;
    public static final int ETRACE_CLIENT_SEND = 1;
    public static final int ETRACE_LOG_ENABLE = 1;
    public static final int ETRACE_PRINT_HANDLER_INFO = 0;
    public static final int ETRACE_SERVER_RECV = 2;
    public static final int ETRACE_SERVER_SEND = 4;
    public static final int ETRACE_SHOW_SPAN_LOG = 8;
    public static final int ETRACE_SYNC_AND_ASYNC = 4;
    public static final int ETRACE_SYNC_CALL = 1;
    public static final int ETRACE_SYSTRACE_ENABLE = 2;
    private static final String TAG = "HwEtrace";

    private static native void etrace_begin_native(String str, int i);

    private static native void etrace_begin_native_by_id(long j, int i);

    private static native void etrace_clear_tls_id_native();

    private static native void etrace_end_native();

    private static native long etrace_get_tls_id_and_new_span_for_caller_native(int i);

    private static native long etrace_get_tls_id_native();

    private static native void etrace_set_tls_id_and_new_span_for_callee_native(long j);

    private static native void etrace_set_tls_id_native(long j);

    static {
        try {
            System.loadLibrary("hwetrace_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Loading hwetrace_jni libarary failed >>>>>" + e);
        }
    }

    public static void traceBegin(String name, int flags) {
        etrace_begin_native(name, flags);
    }

    public static void traceBegin(long id, int flags) {
        etrace_begin_native_by_id(id, flags);
    }

    public static void traceEnd() {
        etrace_end_native();
    }

    public static long getTlsId() {
        return etrace_get_tls_id_native();
    }

    public static void setTlsId(long id) {
        etrace_set_tls_id_native(id);
    }

    public static void clearTlsId() {
        etrace_clear_tls_id_native();
    }

    public static long getTlsIdAndNewSpanForCaller(int callType) {
        return etrace_get_tls_id_and_new_span_for_caller_native(callType);
    }

    public static void setTlsIdAndNewSpanForCallee(long id) {
        etrace_set_tls_id_and_new_span_for_callee_native(id);
    }

    public static boolean isFlagEnabled(long id, int flag) {
        return (((long) flag) & id) != 0;
    }
}
