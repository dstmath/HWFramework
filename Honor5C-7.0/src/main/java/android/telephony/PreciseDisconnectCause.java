package android.telephony;

public class PreciseDisconnectCause {
    public static final int ACM_LIMIT_EXCEEDED = 68;
    public static final int BEARER_NOT_AVAIL = 58;
    public static final int BUSY = 17;
    public static final int CALL_BARRED = 240;
    public static final int CDMA_ACCESS_BLOCKED = 1009;
    public static final int CDMA_ACCESS_FAILURE = 1006;
    public static final int CDMA_DROP = 1001;
    public static final int CDMA_INTERCEPT = 1002;
    public static final int CDMA_LOCKED_UNTIL_POWER_CYCLE = 1000;
    public static final int CDMA_NOT_EMERGENCY = 1008;
    public static final int CDMA_PREEMPTED = 1007;
    public static final int CDMA_REORDER = 1003;
    public static final int CDMA_RETRY_ORDER = 1005;
    public static final int CDMA_SO_REJECT = 1004;
    public static final int CHANNEL_NOT_AVAIL = 44;
    public static final int ERROR_UNSPECIFIED = 65535;
    public static final int FDN_BLOCKED = 241;
    public static final int IMEI_NOT_ACCEPTED = 243;
    public static final int IMSI_UNKNOWN_IN_VLR = 242;
    public static final int NORMAL = 16;
    public static final int NORMAL_UNSPECIFIED = 31;
    public static final int NOT_VALID = -1;
    public static final int NO_CIRCUIT_AVAIL = 34;
    public static final int NO_DISCONNECT_CAUSE_AVAILABLE = 0;
    public static final int NUMBER_CHANGED = 22;
    public static final int QOS_NOT_AVAIL = 49;
    public static final int STATUS_ENQUIRY = 30;
    public static final int SWITCHING_CONGESTION = 42;
    public static final int TEMPORARY_FAILURE = 41;
    public static final int UNOBTAINABLE_NUMBER = 1;

    private PreciseDisconnectCause() {
    }
}
