package android.system;

import java.io.IOException;
import java.net.SocketException;
import libcore.io.Libcore;

public final class ErrnoException extends Exception {
    public final int errno;
    private final String functionName;

    public ErrnoException(String functionName2, int errno2) {
        this.functionName = functionName2;
        this.errno = errno2;
    }

    public ErrnoException(String functionName2, int errno2, Throwable cause) {
        super(cause);
        this.functionName = functionName2;
        this.errno = errno2;
    }

    public String getMessage() {
        String errnoName = OsConstants.errnoName(this.errno);
        if (errnoName == null) {
            errnoName = "errno " + this.errno;
        }
        String description = Libcore.os.strerror(this.errno);
        return this.functionName + " failed: " + errnoName + " (" + description + ")";
    }

    public IOException rethrowAsIOException() throws IOException {
        IOException newException = new IOException(getMessage());
        newException.initCause(this);
        throw newException;
    }

    public SocketException rethrowAsSocketException() throws SocketException {
        throw new SocketException(getMessage(), this);
    }
}
