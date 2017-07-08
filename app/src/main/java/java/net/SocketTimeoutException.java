package java.net;

import java.io.InterruptedIOException;

public class SocketTimeoutException extends InterruptedIOException {
    private static final long serialVersionUID = -8846654841826352300L;

    public SocketTimeoutException(String msg) {
        super(msg);
    }

    public SocketTimeoutException(Throwable cause) {
        super(cause);
    }

    public SocketTimeoutException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
