package ohos.hiviewdfx;

public final class HiTrace {
    public static final int HITRACE_FLAG_DEFAULT = 0;
    public static final int HITRACE_FLAG_DONOT_CREATE_SPAN = 2;
    public static final int HITRACE_FLAG_DONOT_ENABLE_LOG = 16;
    public static final int HITRACE_FLAG_FAILURE_TRIGGER = 32;
    public static final int HITRACE_FLAG_INCLUDE_ASYNC = 1;
    public static final int HITRACE_FLAG_MAX = 64;
    public static final int HITRACE_FLAG_MIN = 0;
    public static final int HITRACE_FLAG_NO_BE_INFO = 8;
    public static final int HITRACE_FLAG_TP_INFO = 4;
    public static final int HITRACE_ID_INVALID = 0;
    public static final int HITRACE_ID_VALID = 1;
    public static final int HITRACE_TP_CR = 1;
    public static final int HITRACE_TP_CS = 0;
    public static final int HITRACE_TP_GENERAL = 4;
    public static final int HITRACE_TP_MIN = 0;
    public static final int HITRACE_TP_SR = 3;
    public static final int HITRACE_TP_SS = 2;

    private HiTrace() {
    }

    public static HiTraceId begin(String str, int i) {
        return HiTraceImpl.begin(str, i);
    }

    public static void end(HiTraceId hiTraceId) {
        HiTraceImpl.end(hiTraceId);
    }

    public static HiTraceId getId() {
        return HiTraceImpl.getId();
    }

    public static void setId(HiTraceId hiTraceId) {
        HiTraceImpl.setId(hiTraceId);
    }

    public static void clearId() {
        HiTraceImpl.clearId();
    }

    public static HiTraceId createSpan() {
        return HiTraceImpl.createSpan();
    }

    public static void tracePoint(int i, HiTraceId hiTraceId, String str, Object... objArr) {
        HiTraceImpl.tracePoint(i, hiTraceId, str, objArr);
    }
}
