package javax.sip;

import java.util.EventObject;

public class IOExceptionEvent extends EventObject {
    private String mHost;
    private int mPort;
    private String mTransport;

    public IOExceptionEvent(Object source, String host, int port, String transport) {
        super(source);
        this.mHost = host;
        this.mPort = port;
        this.mTransport = transport;
    }

    public String getHost() {
        return this.mHost;
    }

    public int getPort() {
        return this.mPort;
    }

    public String getTransport() {
        return this.mTransport;
    }
}
