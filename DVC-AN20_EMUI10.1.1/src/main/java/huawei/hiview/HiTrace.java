package huawei.hiview;

public interface HiTrace {
    public static final int HITRACE_FLAG_DEFAULT = 0;
    public static final int HITRACE_FLAG_DONOT_CREATE_SPAN = 2;
    public static final int HITRACE_FLAG_DONOT_ENABLE_LOG = 16;
    public static final int HITRACE_FLAG_FAILURE_TRIGGER = 32;
    public static final int HITRACE_FLAG_INCLUDE_ASYNC = 1;
    public static final int HITRACE_FLAG_NO_BE_INFO = 8;
    public static final int HITRACE_FLAG_TP_INFO = 4;
    public static final int HITRACE_TP_CR = 1;
    public static final int HITRACE_TP_CS = 0;
    public static final int HITRACE_TP_GENERAL = 4;
    public static final int HITRACE_TP_SR = 3;
    public static final int HITRACE_TP_SS = 2;

    HiTraceId begin(String str, int i);

    void clearId();

    HiTraceId createSpan();

    void end(HiTraceId hiTraceId);

    HiTraceId getId();

    void setId(HiTraceId hiTraceId);

    void tracePoint(int i, HiTraceId hiTraceId, String str, Object... objArr);
}
