package gov.nist.core.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class DefaultNetworkLayer implements NetworkLayer {
    public static final DefaultNetworkLayer SINGLETON = null;
    private SSLServerSocketFactory sslServerSocketFactory;
    private SSLSocketFactory sslSocketFactory;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.core.net.DefaultNetworkLayer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.core.net.DefaultNetworkLayer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.core.net.DefaultNetworkLayer.<clinit>():void");
    }

    private DefaultNetworkLayer() {
        this.sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddress) throws IOException {
        return new ServerSocket(port, backlog, bindAddress);
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        return new Socket(address, port);
    }

    public DatagramSocket createDatagramSocket() throws SocketException {
        return new DatagramSocket();
    }

    public DatagramSocket createDatagramSocket(int port, InetAddress laddr) throws SocketException {
        if (!laddr.isMulticastAddress()) {
            return new DatagramSocket(port, laddr);
        }
        try {
            MulticastSocket ds = new MulticastSocket(port);
            ds.joinGroup(laddr);
            return ds;
        } catch (IOException e) {
            throw new SocketException(e.getLocalizedMessage());
        }
    }

    public SSLServerSocket createSSLServerSocket(int port, int backlog, InetAddress bindAddress) throws IOException {
        return (SSLServerSocket) this.sslServerSocketFactory.createServerSocket(port, backlog, bindAddress);
    }

    public SSLSocket createSSLSocket(InetAddress address, int port) throws IOException {
        return (SSLSocket) this.sslSocketFactory.createSocket(address, port);
    }

    public SSLSocket createSSLSocket(InetAddress address, int port, InetAddress myAddress) throws IOException {
        return (SSLSocket) this.sslSocketFactory.createSocket(address, port, myAddress, 0);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress myAddress) throws IOException {
        if (myAddress != null) {
            return new Socket(address, port, myAddress, 0);
        }
        return new Socket(address, port);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress myAddress, int myPort) throws IOException {
        if (myAddress != null) {
            return new Socket(address, port, myAddress, myPort);
        }
        if (port == 0) {
            return new Socket(address, port);
        }
        Socket sock = new Socket();
        sock.bind(new InetSocketAddress(port));
        sock.connect(new InetSocketAddress(address, port));
        return sock;
    }
}
