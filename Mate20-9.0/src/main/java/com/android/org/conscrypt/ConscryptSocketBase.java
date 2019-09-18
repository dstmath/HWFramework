package com.android.org.conscrypt;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;

abstract class ConscryptSocketBase extends AbstractConscryptSocket {
    private final boolean autoClose;
    private final List<HandshakeCompletedListener> listeners;
    private String peerHostname;
    private final PeerInfoProvider peerInfoProvider;
    private final int peerPort;
    private int readTimeoutMilliseconds;
    final Socket socket;

    /* access modifiers changed from: package-private */
    public abstract SSLSession getActiveSession();

    /* access modifiers changed from: package-private */
    public abstract void setApplicationProtocolSelector(ApplicationProtocolSelectorAdapter applicationProtocolSelectorAdapter);

    ConscryptSocketBase() throws IOException {
        this.peerInfoProvider = new PeerInfoProvider() {
            /* access modifiers changed from: package-private */
            public String getHostname() {
                return ConscryptSocketBase.this.getHostname();
            }

            /* access modifiers changed from: package-private */
            public String getHostnameOrIP() {
                return ConscryptSocketBase.this.getHostnameOrIP();
            }

            /* access modifiers changed from: package-private */
            public int getPort() {
                return ConscryptSocketBase.this.getPort();
            }
        };
        this.listeners = new ArrayList(2);
        this.socket = this;
        this.peerHostname = null;
        this.peerPort = -1;
        this.autoClose = false;
    }

    ConscryptSocketBase(String hostname, int port) throws IOException {
        super(hostname, port);
        this.peerInfoProvider = new PeerInfoProvider() {
            /* access modifiers changed from: package-private */
            public String getHostname() {
                return ConscryptSocketBase.this.getHostname();
            }

            /* access modifiers changed from: package-private */
            public String getHostnameOrIP() {
                return ConscryptSocketBase.this.getHostnameOrIP();
            }

            /* access modifiers changed from: package-private */
            public int getPort() {
                return ConscryptSocketBase.this.getPort();
            }
        };
        this.listeners = new ArrayList(2);
        this.socket = this;
        this.peerHostname = hostname;
        this.peerPort = port;
        this.autoClose = false;
    }

    ConscryptSocketBase(InetAddress address, int port) throws IOException {
        super(address, port);
        this.peerInfoProvider = new PeerInfoProvider() {
            /* access modifiers changed from: package-private */
            public String getHostname() {
                return ConscryptSocketBase.this.getHostname();
            }

            /* access modifiers changed from: package-private */
            public String getHostnameOrIP() {
                return ConscryptSocketBase.this.getHostnameOrIP();
            }

            /* access modifiers changed from: package-private */
            public int getPort() {
                return ConscryptSocketBase.this.getPort();
            }
        };
        this.listeners = new ArrayList(2);
        this.socket = this;
        this.peerHostname = null;
        this.peerPort = -1;
        this.autoClose = false;
    }

    ConscryptSocketBase(String hostname, int port, InetAddress clientAddress, int clientPort) throws IOException {
        super(hostname, port, clientAddress, clientPort);
        this.peerInfoProvider = new PeerInfoProvider() {
            /* access modifiers changed from: package-private */
            public String getHostname() {
                return ConscryptSocketBase.this.getHostname();
            }

            /* access modifiers changed from: package-private */
            public String getHostnameOrIP() {
                return ConscryptSocketBase.this.getHostnameOrIP();
            }

            /* access modifiers changed from: package-private */
            public int getPort() {
                return ConscryptSocketBase.this.getPort();
            }
        };
        this.listeners = new ArrayList(2);
        this.socket = this;
        this.peerHostname = hostname;
        this.peerPort = port;
        this.autoClose = false;
    }

    ConscryptSocketBase(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        super(address, port, clientAddress, clientPort);
        this.peerInfoProvider = new PeerInfoProvider() {
            /* access modifiers changed from: package-private */
            public String getHostname() {
                return ConscryptSocketBase.this.getHostname();
            }

            /* access modifiers changed from: package-private */
            public String getHostnameOrIP() {
                return ConscryptSocketBase.this.getHostnameOrIP();
            }

            /* access modifiers changed from: package-private */
            public int getPort() {
                return ConscryptSocketBase.this.getPort();
            }
        };
        this.listeners = new ArrayList(2);
        this.socket = this;
        this.peerHostname = null;
        this.peerPort = -1;
        this.autoClose = false;
    }

    ConscryptSocketBase(Socket socket2, String hostname, int port, boolean autoClose2) throws IOException {
        this.peerInfoProvider = new PeerInfoProvider() {
            /* access modifiers changed from: package-private */
            public String getHostname() {
                return ConscryptSocketBase.this.getHostname();
            }

            /* access modifiers changed from: package-private */
            public String getHostnameOrIP() {
                return ConscryptSocketBase.this.getHostnameOrIP();
            }

            /* access modifiers changed from: package-private */
            public int getPort() {
                return ConscryptSocketBase.this.getPort();
            }
        };
        this.listeners = new ArrayList(2);
        this.socket = (Socket) Preconditions.checkNotNull(socket2, "socket");
        this.peerHostname = hostname;
        this.peerPort = port;
        this.autoClose = autoClose2;
    }

    public final void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    public final void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (this.peerHostname == null && (endpoint instanceof InetSocketAddress)) {
            this.peerHostname = Platform.getHostStringFromInetSocketAddress((InetSocketAddress) endpoint);
        }
        if (isDelegating()) {
            this.socket.connect(endpoint, timeout);
        } else {
            super.connect(endpoint, timeout);
        }
    }

    public void bind(SocketAddress bindpoint) throws IOException {
        if (isDelegating()) {
            this.socket.bind(bindpoint);
        } else {
            super.bind(bindpoint);
        }
    }

    public void close() throws IOException {
        if (isDelegating()) {
            if (this.autoClose && !this.socket.isClosed()) {
                this.socket.close();
            }
        } else if (!super.isClosed()) {
            super.close();
        }
    }

    public InetAddress getInetAddress() {
        if (isDelegating()) {
            return this.socket.getInetAddress();
        }
        return super.getInetAddress();
    }

    public InetAddress getLocalAddress() {
        if (isDelegating()) {
            return this.socket.getLocalAddress();
        }
        return super.getLocalAddress();
    }

    public int getLocalPort() {
        if (isDelegating()) {
            return this.socket.getLocalPort();
        }
        return super.getLocalPort();
    }

    public SocketAddress getRemoteSocketAddress() {
        if (isDelegating()) {
            return this.socket.getRemoteSocketAddress();
        }
        return super.getRemoteSocketAddress();
    }

    public SocketAddress getLocalSocketAddress() {
        if (isDelegating()) {
            return this.socket.getLocalSocketAddress();
        }
        return super.getLocalSocketAddress();
    }

    public final int getPort() {
        if (isDelegating()) {
            return this.socket.getPort();
        }
        if (this.peerPort != -1) {
            return this.peerPort;
        }
        return super.getPort();
    }

    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        Preconditions.checkArgument(listener != null, "Provided listener is null");
        this.listeners.add(listener);
    }

    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        Preconditions.checkArgument(listener != null, "Provided listener is null");
        if (!this.listeners.remove(listener)) {
            throw new IllegalArgumentException("Provided listener is not registered");
        }
    }

    public FileDescriptor getFileDescriptor$() {
        if (isDelegating()) {
            return Platform.getFileDescriptor(this.socket);
        }
        return Platform.getFileDescriptorFromSSLSocket(this);
    }

    public final void setSoTimeout(int readTimeoutMilliseconds2) throws SocketException {
        if (isDelegating()) {
            this.socket.setSoTimeout(readTimeoutMilliseconds2);
            return;
        }
        super.setSoTimeout(readTimeoutMilliseconds2);
        this.readTimeoutMilliseconds = readTimeoutMilliseconds2;
    }

    public final int getSoTimeout() throws SocketException {
        if (isDelegating()) {
            return this.socket.getSoTimeout();
        }
        return this.readTimeoutMilliseconds;
    }

    public final void sendUrgentData(int data) throws IOException {
        throw new SocketException("Method sendUrgentData() is not supported.");
    }

    public final void setOOBInline(boolean on) throws SocketException {
        throw new SocketException("Method setOOBInline() is not supported.");
    }

    public boolean getOOBInline() throws SocketException {
        return false;
    }

    public SocketChannel getChannel() {
        return null;
    }

    public InputStream getInputStream() throws IOException {
        if (isDelegating()) {
            return this.socket.getInputStream();
        }
        return super.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        if (isDelegating()) {
            return this.socket.getOutputStream();
        }
        return super.getOutputStream();
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        if (isDelegating()) {
            this.socket.setTcpNoDelay(on);
        } else {
            super.setTcpNoDelay(on);
        }
    }

    public boolean getTcpNoDelay() throws SocketException {
        if (isDelegating()) {
            return this.socket.getTcpNoDelay();
        }
        return super.getTcpNoDelay();
    }

    public void setSoLinger(boolean on, int linger) throws SocketException {
        if (isDelegating()) {
            this.socket.setSoLinger(on, linger);
        } else {
            super.setSoLinger(on, linger);
        }
    }

    public int getSoLinger() throws SocketException {
        if (isDelegating()) {
            return this.socket.getSoLinger();
        }
        return super.getSoLinger();
    }

    public void setSendBufferSize(int size) throws SocketException {
        if (isDelegating()) {
            this.socket.setSendBufferSize(size);
        } else {
            super.setSendBufferSize(size);
        }
    }

    public int getSendBufferSize() throws SocketException {
        if (isDelegating()) {
            return this.socket.getSendBufferSize();
        }
        return super.getSendBufferSize();
    }

    public void setReceiveBufferSize(int size) throws SocketException {
        if (isDelegating()) {
            this.socket.setReceiveBufferSize(size);
        } else {
            super.setReceiveBufferSize(size);
        }
    }

    public int getReceiveBufferSize() throws SocketException {
        if (isDelegating()) {
            return this.socket.getReceiveBufferSize();
        }
        return super.getReceiveBufferSize();
    }

    public void setKeepAlive(boolean on) throws SocketException {
        if (isDelegating()) {
            this.socket.setKeepAlive(on);
        } else {
            super.setKeepAlive(on);
        }
    }

    public boolean getKeepAlive() throws SocketException {
        if (isDelegating()) {
            return this.socket.getKeepAlive();
        }
        return super.getKeepAlive();
    }

    public void setTrafficClass(int tc) throws SocketException {
        if (isDelegating()) {
            this.socket.setTrafficClass(tc);
        } else {
            super.setTrafficClass(tc);
        }
    }

    public int getTrafficClass() throws SocketException {
        if (isDelegating()) {
            return this.socket.getTrafficClass();
        }
        return super.getTrafficClass();
    }

    public void setReuseAddress(boolean on) throws SocketException {
        if (isDelegating()) {
            this.socket.setReuseAddress(on);
        } else {
            super.setReuseAddress(on);
        }
    }

    public boolean getReuseAddress() throws SocketException {
        if (isDelegating()) {
            return this.socket.getReuseAddress();
        }
        return super.getReuseAddress();
    }

    public void shutdownInput() throws IOException {
        if (isDelegating()) {
            this.socket.shutdownInput();
        } else {
            super.shutdownInput();
        }
    }

    public void shutdownOutput() throws IOException {
        if (isDelegating()) {
            this.socket.shutdownOutput();
        } else {
            super.shutdownOutput();
        }
    }

    public boolean isConnected() {
        if (isDelegating()) {
            return this.socket.isConnected();
        }
        return super.isConnected();
    }

    public boolean isBound() {
        if (isDelegating()) {
            return this.socket.isBound();
        }
        return super.isBound();
    }

    public boolean isClosed() {
        if (isDelegating()) {
            return this.socket.isClosed();
        }
        return super.isClosed();
    }

    public boolean isInputShutdown() {
        if (isDelegating()) {
            return this.socket.isInputShutdown();
        }
        return super.isInputShutdown();
    }

    public boolean isOutputShutdown() {
        if (isDelegating()) {
            return this.socket.isOutputShutdown();
        }
        return super.isOutputShutdown();
    }

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        if (isDelegating()) {
            this.socket.setPerformancePreferences(connectionTime, latency, bandwidth);
        } else {
            super.setPerformancePreferences(connectionTime, latency, bandwidth);
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("SSL socket over ");
        if (isDelegating()) {
            builder.append(this.socket.toString());
        } else {
            builder.append(super.toString());
        }
        return builder.toString();
    }

    /* access modifiers changed from: package-private */
    public String getHostname() {
        return this.peerHostname;
    }

    /* access modifiers changed from: package-private */
    public void setHostname(String hostname) {
        this.peerHostname = hostname;
    }

    /* access modifiers changed from: package-private */
    public String getHostnameOrIP() {
        if (this.peerHostname != null) {
            return this.peerHostname;
        }
        InetAddress peerAddress = getInetAddress();
        if (peerAddress != null) {
            return Platform.getOriginalHostNameFromInetAddress(peerAddress);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setSoWriteTimeout(int writeTimeoutMilliseconds) throws SocketException {
        throw new SocketException("Method setSoWriteTimeout() is not supported.");
    }

    /* access modifiers changed from: package-private */
    public int getSoWriteTimeout() throws SocketException {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void setHandshakeTimeout(int handshakeTimeoutMilliseconds) throws SocketException {
        throw new SocketException("Method setHandshakeTimeout() is not supported.");
    }

    /* access modifiers changed from: package-private */
    public final void checkOpen() throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
    }

    /* access modifiers changed from: package-private */
    public final PeerInfoProvider peerInfoProvider() {
        return this.peerInfoProvider;
    }

    /* access modifiers changed from: package-private */
    public final void notifyHandshakeCompletedListeners() {
        if (this.listeners != null && !this.listeners.isEmpty()) {
            HandshakeCompletedEvent event = new HandshakeCompletedEvent(this, getActiveSession());
            for (HandshakeCompletedListener listener : this.listeners) {
                try {
                    listener.handshakeCompleted(event);
                } catch (RuntimeException e) {
                    Thread thread = Thread.currentThread();
                    thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
                }
            }
        }
    }

    private boolean isDelegating() {
        return (this.socket == null || this.socket == this) ? false : true;
    }
}
