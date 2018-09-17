package java.net;

import java.io.IOException;

public class UnknownHostException extends IOException {
    private static final long serialVersionUID = -4639126076052875403L;

    public UnknownHostException(String host) {
        super(host);
    }
}
