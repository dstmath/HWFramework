package sun.security.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLSocket;

abstract class BaseSSLSocketImpl extends SSLSocket {
    private static final String PROP_NAME = "com.sun.net.ssl.requireCloseNotify";
    static final boolean requireCloseNotify = false;
    final Socket self;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.BaseSSLSocketImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.BaseSSLSocketImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.BaseSSLSocketImpl.<clinit>():void");
    }

    BaseSSLSocketImpl() {
        this.self = this;
    }

    BaseSSLSocketImpl(Socket socket) {
        this.self = socket;
    }

    public final SocketChannel getChannel() {
        if (this.self == this) {
            return super.getChannel();
        }
        return this.self.getChannel();
    }

    public void bind(SocketAddress bindpoint) throws IOException {
        if (this.self == this) {
            super.bind(bindpoint);
            return;
        }
        throw new IOException("Underlying socket should already be connected");
    }

    public SocketAddress getLocalSocketAddress() {
        if (this.self == this) {
            return super.getLocalSocketAddress();
        }
        return this.self.getLocalSocketAddress();
    }

    public SocketAddress getRemoteSocketAddress() {
        if (this.self == this) {
            return super.getRemoteSocketAddress();
        }
        return this.self.getRemoteSocketAddress();
    }

    public final void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    public final boolean isConnected() {
        if (this.self == this) {
            return super.isConnected();
        }
        return this.self.isConnected();
    }

    public final boolean isBound() {
        if (this.self == this) {
            return super.isBound();
        }
        return this.self.isBound();
    }

    public final void shutdownInput() throws IOException {
        throw new UnsupportedOperationException("The method shutdownInput() is not supported in SSLSocket");
    }

    public final void shutdownOutput() throws IOException {
        throw new UnsupportedOperationException("The method shutdownOutput() is not supported in SSLSocket");
    }

    public final boolean isInputShutdown() {
        if (this.self == this) {
            return super.isInputShutdown();
        }
        return this.self.isInputShutdown();
    }

    public final boolean isOutputShutdown() {
        if (this.self == this) {
            return super.isOutputShutdown();
        }
        return this.self.isOutputShutdown();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected final void finalize() throws Throwable {
        try {
            close();
            super.finalize();
        } catch (IOException e) {
            if (this.self == this) {
                super.close();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public final InetAddress getInetAddress() {
        if (this.self == this) {
            return super.getInetAddress();
        }
        return this.self.getInetAddress();
    }

    public final InetAddress getLocalAddress() {
        if (this.self == this) {
            return super.getLocalAddress();
        }
        return this.self.getLocalAddress();
    }

    public final int getPort() {
        if (this.self == this) {
            return super.getPort();
        }
        return this.self.getPort();
    }

    public final int getLocalPort() {
        if (this.self == this) {
            return super.getLocalPort();
        }
        return this.self.getLocalPort();
    }

    public final void setTcpNoDelay(boolean value) throws SocketException {
        if (this.self == this) {
            super.setTcpNoDelay(value);
        } else {
            this.self.setTcpNoDelay(value);
        }
    }

    public final boolean getTcpNoDelay() throws SocketException {
        if (this.self == this) {
            return super.getTcpNoDelay();
        }
        return this.self.getTcpNoDelay();
    }

    public final void setSoLinger(boolean flag, int linger) throws SocketException {
        if (this.self == this) {
            super.setSoLinger(flag, linger);
        } else {
            this.self.setSoLinger(flag, linger);
        }
    }

    public final int getSoLinger() throws SocketException {
        if (this.self == this) {
            return super.getSoLinger();
        }
        return this.self.getSoLinger();
    }

    public final void sendUrgentData(int data) throws SocketException {
        throw new SocketException("This method is not supported by SSLSockets");
    }

    public final void setOOBInline(boolean on) throws SocketException {
        throw new SocketException("This method is ineffective, since sending urgent data is not supported by SSLSockets");
    }

    public final boolean getOOBInline() throws SocketException {
        throw new SocketException("This method is ineffective, since sending urgent data is not supported by SSLSockets");
    }

    public final int getSoTimeout() throws SocketException {
        if (this.self == this) {
            return super.getSoTimeout();
        }
        return this.self.getSoTimeout();
    }

    public final void setSendBufferSize(int size) throws SocketException {
        if (this.self == this) {
            super.setSendBufferSize(size);
        } else {
            this.self.setSendBufferSize(size);
        }
    }

    public final int getSendBufferSize() throws SocketException {
        if (this.self == this) {
            return super.getSendBufferSize();
        }
        return this.self.getSendBufferSize();
    }

    public final void setReceiveBufferSize(int size) throws SocketException {
        if (this.self == this) {
            super.setReceiveBufferSize(size);
        } else {
            this.self.setReceiveBufferSize(size);
        }
    }

    public final int getReceiveBufferSize() throws SocketException {
        if (this.self == this) {
            return super.getReceiveBufferSize();
        }
        return this.self.getReceiveBufferSize();
    }

    public final void setKeepAlive(boolean on) throws SocketException {
        if (this.self == this) {
            super.setKeepAlive(on);
        } else {
            this.self.setKeepAlive(on);
        }
    }

    public final boolean getKeepAlive() throws SocketException {
        if (this.self == this) {
            return super.getKeepAlive();
        }
        return this.self.getKeepAlive();
    }

    public final void setTrafficClass(int tc) throws SocketException {
        if (this.self == this) {
            super.setTrafficClass(tc);
        } else {
            this.self.setTrafficClass(tc);
        }
    }

    public final int getTrafficClass() throws SocketException {
        if (this.self == this) {
            return super.getTrafficClass();
        }
        return this.self.getTrafficClass();
    }

    public final void setReuseAddress(boolean on) throws SocketException {
        if (this.self == this) {
            super.setReuseAddress(on);
        } else {
            this.self.setReuseAddress(on);
        }
    }

    public final boolean getReuseAddress() throws SocketException {
        if (this.self == this) {
            return super.getReuseAddress();
        }
        return this.self.getReuseAddress();
    }

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        if (this.self == this) {
            super.setPerformancePreferences(connectionTime, latency, bandwidth);
        } else {
            this.self.setPerformancePreferences(connectionTime, latency, bandwidth);
        }
    }
}
