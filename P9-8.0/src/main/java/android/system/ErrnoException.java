package android.system;

import java.io.IOException;
import java.net.SocketException;
import libcore.io.Libcore;

public final class ErrnoException extends Exception {
    public final int errno;
    private final String functionName;

    public ErrnoException(String functionName, int errno) {
        this.functionName = functionName;
        this.errno = errno;
    }

    public ErrnoException(String functionName, int errno, Throwable cause) {
        super(cause);
        this.functionName = functionName;
        this.errno = errno;
    }

    public String getMessage() {
        String errnoName = OsConstants.errnoName(this.errno);
        if (errnoName == null) {
            errnoName = "errno " + this.errno;
        }
        return this.functionName + " failed: " + errnoName + " (" + Libcore.os.strerror(this.errno) + ")";
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
