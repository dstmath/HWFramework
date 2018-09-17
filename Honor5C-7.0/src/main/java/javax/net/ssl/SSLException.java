package javax.net.ssl;

import java.io.IOException;

public class SSLException extends IOException {
    private static final long serialVersionUID = 4511006460650708967L;

    public SSLException(String reason) {
        super(reason);
    }

    public SSLException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public SSLException(Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.toString();
        }
        super(str);
        initCause(cause);
    }
}
