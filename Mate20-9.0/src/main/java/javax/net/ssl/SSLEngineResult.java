package javax.net.ssl;

public class SSLEngineResult {
    private final int bytesConsumed;
    private final int bytesProduced;
    private final HandshakeStatus handshakeStatus;
    private final Status status;

    public enum HandshakeStatus {
        NOT_HANDSHAKING,
        FINISHED,
        NEED_TASK,
        NEED_WRAP,
        NEED_UNWRAP
    }

    public enum Status {
        BUFFER_UNDERFLOW,
        BUFFER_OVERFLOW,
        OK,
        CLOSED
    }

    public SSLEngineResult(Status status2, HandshakeStatus handshakeStatus2, int bytesConsumed2, int bytesProduced2) {
        if (status2 == null || handshakeStatus2 == null || bytesConsumed2 < 0 || bytesProduced2 < 0) {
            throw new IllegalArgumentException("Invalid Parameter(s)");
        }
        this.status = status2;
        this.handshakeStatus = handshakeStatus2;
        this.bytesConsumed = bytesConsumed2;
        this.bytesProduced = bytesProduced2;
    }

    public final Status getStatus() {
        return this.status;
    }

    public final HandshakeStatus getHandshakeStatus() {
        return this.handshakeStatus;
    }

    public final int bytesConsumed() {
        return this.bytesConsumed;
    }

    public final int bytesProduced() {
        return this.bytesProduced;
    }

    public String toString() {
        return "Status = " + this.status + " HandshakeStatus = " + this.handshakeStatus + "\nbytesConsumed = " + this.bytesConsumed + " bytesProduced = " + this.bytesProduced;
    }
}
