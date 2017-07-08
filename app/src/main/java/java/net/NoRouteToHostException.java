package java.net;

public class NoRouteToHostException extends SocketException {
    private static final long serialVersionUID = -1897550894873493790L;

    public NoRouteToHostException(String msg) {
        super(msg);
    }
}
