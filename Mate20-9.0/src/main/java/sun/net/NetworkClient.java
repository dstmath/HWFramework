package sun.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import sun.security.util.DerValue;

public class NetworkClient {
    public static final int DEFAULT_CONNECT_TIMEOUT = -1;
    public static final int DEFAULT_READ_TIMEOUT = -1;
    protected static int defaultConnectTimeout;
    protected static int defaultSoTimeout;
    protected static String encoding;
    protected int connectTimeout = -1;
    protected Proxy proxy = Proxy.NO_PROXY;
    protected int readTimeout = -1;
    public InputStream serverInput;
    public PrintStream serverOutput;
    protected Socket serverSocket = null;

    static {
        final int[] vals = {0, 0};
        final String[] encs = {null};
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                vals[0] = Integer.getInteger("sun.net.client.defaultReadTimeout", 0).intValue();
                vals[1] = Integer.getInteger("sun.net.client.defaultConnectTimeout", 0).intValue();
                encs[0] = System.getProperty("file.encoding", "ISO8859_1");
                return null;
            }
        });
        if (vals[0] != 0) {
            defaultSoTimeout = vals[0];
        }
        if (vals[1] != 0) {
            defaultConnectTimeout = vals[1];
        }
        encoding = encs[0];
        try {
            if (!isASCIISuperset(encoding)) {
                encoding = "ISO8859_1";
            }
        } catch (Exception e) {
            encoding = "ISO8859_1";
        }
    }

    private static boolean isASCIISuperset(String encoding2) throws Exception {
        return Arrays.equals("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_.!~*'();/?:@&=+$,".getBytes(encoding2), new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, ObjectStreamConstants.TC_REFERENCE, ObjectStreamConstants.TC_CLASSDESC, ObjectStreamConstants.TC_OBJECT, ObjectStreamConstants.TC_STRING, ObjectStreamConstants.TC_ARRAY, ObjectStreamConstants.TC_CLASS, ObjectStreamConstants.TC_BLOCKDATA, ObjectStreamConstants.TC_ENDBLOCKDATA, ObjectStreamConstants.TC_RESET, ObjectStreamConstants.TC_BLOCKDATALONG, 45, 95, 46, 33, 126, 42, 39, 40, 41, 59, 47, 63, 58, DerValue.TAG_APPLICATION, 38, 61, 43, 36, 44});
    }

    public void openServer(String server, int port) throws IOException, UnknownHostException {
        if (this.serverSocket != null) {
            closeServer();
        }
        this.serverSocket = doConnect(server, port);
        try {
            this.serverOutput = new PrintStream((OutputStream) new BufferedOutputStream(this.serverSocket.getOutputStream()), true, encoding);
            this.serverInput = new BufferedInputStream(this.serverSocket.getInputStream());
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + "encoding not found", e);
        }
    }

    /* access modifiers changed from: protected */
    public Socket doConnect(String server, int port) throws IOException, UnknownHostException {
        Socket s;
        if (this.proxy == null) {
            s = createSocket();
        } else if (this.proxy.type() == Proxy.Type.SOCKS) {
            s = (Socket) AccessController.doPrivileged(new PrivilegedAction<Socket>() {
                public Socket run() {
                    return new Socket(NetworkClient.this.proxy);
                }
            });
        } else if (this.proxy.type() == Proxy.Type.DIRECT) {
            s = createSocket();
        } else {
            s = new Socket(Proxy.NO_PROXY);
        }
        if (this.connectTimeout >= 0) {
            s.connect(new InetSocketAddress(server, port), this.connectTimeout);
        } else if (defaultConnectTimeout > 0) {
            s.connect(new InetSocketAddress(server, port), defaultConnectTimeout);
        } else {
            s.connect(new InetSocketAddress(server, port));
        }
        if (this.readTimeout >= 0) {
            s.setSoTimeout(this.readTimeout);
        } else if (defaultSoTimeout > 0) {
            s.setSoTimeout(defaultSoTimeout);
        }
        return s;
    }

    /* access modifiers changed from: protected */
    public Socket createSocket() throws IOException {
        return new Socket();
    }

    /* access modifiers changed from: protected */
    public InetAddress getLocalAddress() throws IOException {
        if (this.serverSocket != null) {
            return (InetAddress) AccessController.doPrivileged(new PrivilegedAction<InetAddress>() {
                public InetAddress run() {
                    return NetworkClient.this.serverSocket.getLocalAddress();
                }
            });
        }
        throw new IOException("not connected");
    }

    public void closeServer() throws IOException {
        if (serverIsOpen()) {
            this.serverSocket.close();
            this.serverSocket = null;
            this.serverInput = null;
            this.serverOutput = null;
        }
    }

    public boolean serverIsOpen() {
        return this.serverSocket != null;
    }

    public NetworkClient(String host, int port) throws IOException {
        openServer(host, port);
    }

    public NetworkClient() {
    }

    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setReadTimeout(int timeout) {
        if (timeout == -1) {
            timeout = defaultSoTimeout;
        }
        if (this.serverSocket != null && timeout >= 0) {
            try {
                this.serverSocket.setSoTimeout(timeout);
            } catch (IOException e) {
            }
        }
        this.readTimeout = timeout;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }
}
