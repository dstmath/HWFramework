package sun.net;

import java.io.IOException;

public class TelnetProtocolException extends IOException {
    private static final long serialVersionUID = 8509127047257111343L;

    public TelnetProtocolException(String s) {
        super(s);
    }
}
