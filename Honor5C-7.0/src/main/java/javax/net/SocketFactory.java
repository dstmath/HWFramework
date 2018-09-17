package javax.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public abstract class SocketFactory {
    private static SocketFactory theFactory;

    public abstract Socket createSocket(String str, int i) throws IOException, UnknownHostException;

    public abstract Socket createSocket(String str, int i, InetAddress inetAddress, int i2) throws IOException, UnknownHostException;

    public abstract Socket createSocket(InetAddress inetAddress, int i) throws IOException;

    public abstract Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) throws IOException;

    protected SocketFactory() {
    }

    public static SocketFactory getDefault() {
        synchronized (SocketFactory.class) {
            if (theFactory == null) {
                theFactory = new DefaultSocketFactory();
            }
        }
        return theFactory;
    }

    public static void setDefault(SocketFactory factory) {
        synchronized (SocketFactory.class) {
            theFactory = factory;
        }
    }

    public Socket createSocket() throws IOException {
        UnsupportedOperationException uop = new UnsupportedOperationException();
        SocketException se = new SocketException("Unconnected sockets not implemented");
        se.initCause(uop);
        throw se;
    }
}
