package sun.net;

import java.net.SocketException;

public class ConnectionResetException extends SocketException {
    private static final long serialVersionUID = -7633185991801851556L;

    public ConnectionResetException(String msg) {
        super(msg);
    }
}
