package java.net;

import java.io.IOException;

public class ProtocolException extends IOException {
    private static final long serialVersionUID = -6098449442062388080L;

    public ProtocolException(String host) {
        super(host);
    }

    public ProtocolException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
