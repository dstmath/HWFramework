package android.system;

import java.net.UnknownHostException;
import libcore.io.Libcore;

public final class GaiException extends RuntimeException {
    public final int error;
    private final String functionName;

    public GaiException(String functionName2, int error2) {
        this.functionName = functionName2;
        this.error = error2;
    }

    public GaiException(String functionName2, int error2, Throwable cause) {
        super(cause);
        this.functionName = functionName2;
        this.error = error2;
    }

    public String getMessage() {
        String gaiName = OsConstants.gaiName(this.error);
        if (gaiName == null) {
            gaiName = "GAI_ error " + this.error;
        }
        String description = Libcore.os.gai_strerror(this.error);
        return this.functionName + " failed: " + gaiName + " (" + description + ")";
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
