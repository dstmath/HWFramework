package android.media;

public class MediaCasStateException extends IllegalStateException {
    private final String mDiagnosticInfo;
    private final int mErrorCode;

    private MediaCasStateException(int err, String msg, String diagnosticInfo) {
        super(msg);
        this.mErrorCode = err;
        this.mDiagnosticInfo = diagnosticInfo;
    }

    static void throwExceptionIfNeeded(int err) {
        throwExceptionIfNeeded(err, null);
    }

    static void throwExceptionIfNeeded(int err, String msg) {
        String diagnosticInfo;
        if (err != 0) {
            if (err != 6) {
                switch (err) {
                    case 1:
                        diagnosticInfo = "No license";
                        break;
                    case 2:
                        diagnosticInfo = "License expired";
                        break;
                    case 3:
                        diagnosticInfo = "Session not opened";
                        break;
                    case 4:
                        diagnosticInfo = "Unsupported scheme or data format";
                        break;
                    case 5:
                        diagnosticInfo = "Invalid CAS state";
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 11:
                    default:
                        diagnosticInfo = "Unknown CAS state exception";
                        break;
                    case 9:
                        diagnosticInfo = "Insufficient output protection";
                        break;
                    case 10:
                        diagnosticInfo = "Tamper detected";
                        break;
                    case 12:
                        diagnosticInfo = "Not initialized";
                        break;
                    case 13:
                        diagnosticInfo = "Decrypt error";
                        break;
                    case 14:
                        diagnosticInfo = "General CAS error";
                        break;
                }
                throw new MediaCasStateException(err, msg, String.format("%s (err=%d)", diagnosticInfo, Integer.valueOf(err)));
            }
            throw new IllegalArgumentException();
        }
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public String getDiagnosticInfo() {
        return this.mDiagnosticInfo;
    }
}
