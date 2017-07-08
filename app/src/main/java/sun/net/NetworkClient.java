package sun.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamConstants;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
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
    protected int connectTimeout;
    protected Proxy proxy;
    protected int readTimeout;
    public InputStream serverInput;
    public PrintStream serverOutput;
    protected Socket serverSocket;

    /* renamed from: sun.net.NetworkClient.1 */
    static class AnonymousClass1 implements PrivilegedAction<Void> {
        final /* synthetic */ String[] val$encs;
        final /* synthetic */ int[] val$vals;

        AnonymousClass1(int[] val$vals, String[] val$encs) {
            this.val$vals = val$vals;
            this.val$encs = val$encs;
        }

        public Void run() {
            this.val$vals[0] = Integer.getInteger("sun.net.client.defaultReadTimeout", 0).intValue();
            this.val$vals[1] = Integer.getInteger("sun.net.client.defaultConnectTimeout", 0).intValue();
            this.val$encs[0] = System.getProperty("file.encoding", "ISO8859_1");
            return null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.NetworkClient.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.NetworkClient.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.net.NetworkClient.<clinit>():void");
    }

    private static boolean isASCIISuperset(String encoding) throws Exception {
        return Arrays.equals("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_.!~*'();/?:@&=+$,".getBytes(encoding), new byte[]{DerValue.tag_SequenceOf, DerValue.tag_SetOf, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, ObjectStreamConstants.TC_NULL, ObjectStreamConstants.TC_REFERENCE, ObjectStreamConstants.TC_CLASSDESC, ObjectStreamConstants.TC_OBJECT, ObjectStreamConstants.TC_STRING, ObjectStreamConstants.TC_ARRAY, ObjectStreamConstants.TC_CLASS, ObjectStreamConstants.TC_BLOCKDATA, ObjectStreamConstants.TC_ENDBLOCKDATA, ObjectStreamConstants.TC_RESET, ObjectStreamConstants.TC_BLOCKDATALONG, (byte) 45, (byte) 95, (byte) 46, (byte) 33, ObjectStreamConstants.TC_MAX, (byte) 42, (byte) 39, (byte) 40, (byte) 41, (byte) 59, (byte) 47, (byte) 63, (byte) 58, DerValue.TAG_APPLICATION, (byte) 38, (byte) 61, (byte) 43, (byte) 36, (byte) 44});
    }

    public void openServer(String server, int port) throws IOException, UnknownHostException {
        if (this.serverSocket != null) {
            closeServer();
        }
        this.serverSocket = doConnect(server, port);
        try {
            this.serverOutput = new PrintStream(new BufferedOutputStream(this.serverSocket.getOutputStream()), true, encoding);
            this.serverInput = new BufferedInputStream(this.serverSocket.getInputStream());
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + "encoding not found");
        }
    }

    protected Socket doConnect(String server, int port) throws IOException, UnknownHostException {
        Socket s;
        if (this.proxy == null) {
            s = createSocket();
        } else if (this.proxy.type() == Type.SOCKS) {
            s = (Socket) AccessController.doPrivileged(new PrivilegedAction<Socket>() {
                public Socket run() {
                    return new Socket(NetworkClient.this.proxy);
                }
            });
        } else if (this.proxy.type() == Type.DIRECT) {
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

    protected Socket createSocket() throws IOException {
        return new Socket();
    }

    protected InetAddress getLocalAddress() throws IOException {
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
        this.proxy = Proxy.NO_PROXY;
        this.serverSocket = null;
        this.readTimeout = DEFAULT_READ_TIMEOUT;
        this.connectTimeout = DEFAULT_READ_TIMEOUT;
        openServer(host, port);
    }

    public NetworkClient() {
        this.proxy = Proxy.NO_PROXY;
        this.serverSocket = null;
        this.readTimeout = DEFAULT_READ_TIMEOUT;
        this.connectTimeout = DEFAULT_READ_TIMEOUT;
    }

    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setReadTimeout(int timeout) {
        if (timeout == DEFAULT_READ_TIMEOUT) {
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
