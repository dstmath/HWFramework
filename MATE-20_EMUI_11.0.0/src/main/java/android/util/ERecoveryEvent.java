package android.util;

public class ERecoveryEvent {
    public static final long EVENT_BEGIN = 0;
    public static final long EVENT_COMMAND = 4;
    public static final long EVENT_END = 1;
    public static final long EVENT_FAIL = 1;
    public static final long EVENT_PENDING = 2;
    public static final long EVENT_REQUEST = 2;
    public static final long EVENT_RESULT = 3;
    public static final long EVENT_SUCCESS = 0;
    private long eRecoveryID;
    private long faultID;
    private String fingerPrint;
    private long pid;
    private String processName;
    private String reason;
    private String reserved;
    private long result;
    private long state;
    private long timeStamp;

    public ERecoveryEvent() {
    }

    public ERecoveryEvent(long erecovery_id, long fault_id, long pid2, String processname, String fingerprint, long time, long state2, long result2, String reason2, String reserved2) {
        this.eRecoveryID = erecovery_id;
        this.faultID = fault_id;
        this.pid = pid2;
        this.processName = processname;
        this.fingerPrint = fingerprint;
        this.timeStamp = time;
        this.state = state2;
        this.result = result2;
        this.reason = reason2;
        this.reserved = reserved2;
    }

    public void setERecoveryID(long id) {
        this.eRecoveryID = id;
    }

    public void setFaultID(long id) {
        this.faultID = id;
    }

    public void setPid(long pidNum) {
        this.pid = pidNum;
    }

    public void setProcessName(String processname) {
        this.processName = processname;
    }

    public void setFingerPrint(String fingerPrint2) {
        this.fingerPrint = fingerPrint2;
    }

    public void setTimeStamp(long timeStamp2) {
        this.timeStamp = timeStamp2;
    }

    public void setState(long state2) {
        this.state = state2;
    }

    public void setResult(long result2) {
        this.result = result2;
    }

    public void setReason(String reason2) {
        this.reason = reason2;
    }

    public void setReserved(String reserved2) {
        this.reserved = reserved2;
    }
}
