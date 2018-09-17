package android.system;

import java.net.UnknownHostException;
import libcore.io.Libcore;

public final class GaiException extends RuntimeException {
    public final int error;
    private final String functionName;

    public GaiException(String functionName, int error) {
        this.functionName = functionName;
        this.error = error;
    }

    public GaiException(String functionName, int error, Throwable cause) {
        super(cause);
        this.functionName = functionName;
        this.error = error;
    }

    public String getMessage() {
        String gaiName = OsConstants.gaiName(this.error);
        if (gaiName == null) {
            gaiName = "GAI_ error " + this.error;
        }
        return this.functionName + " failed: " + gaiName + " (" + Libcore.os.gai_strerror(this.error) + ")";
    }

    public UnknownHostException rethrowAsUnknownHostException(String detailMessage) throws UnknownHostException {
        UnknownHostException newException = new UnknownHostException(detailMessage);
        newException.initCause(this);
        throw newException;
    }

    public UnknownHostException rethrowAsUnknownHostException() throws UnknownHostException {
        throw rethrowAsUnknownHostException(getMessage());
    }
}
