package android.net;

import android.annotation.UnsupportedAppUsage;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalSocket implements Closeable {
    public static final int SOCKET_DGRAM = 1;
    public static final int SOCKET_SEQPACKET = 3;
    public static final int SOCKET_STREAM = 2;
    static final int SOCKET_UNKNOWN = 0;
    @UnsupportedAppUsage
    private final LocalSocketImpl impl;
    private volatile boolean implCreated;
    private boolean isBound;
    private boolean isConnected;
    private LocalSocketAddress localAddress;
    private final int sockType;

    public LocalSocket() {
        this(2);
    }

    public LocalSocket(int sockType2) {
        this(new LocalSocketImpl(), sockType2);
    }

    private LocalSocket(LocalSocketImpl impl2, int sockType2) {
        this.impl = impl2;
        this.sockType = sockType2;
        this.isConnected = false;
        this.isBound = false;
    }

    public static LocalSocket createConnectedLocalSocket(FileDescriptor fd) {
        return createConnectedLocalSocket(new LocalSocketImpl(fd), 0);
    }

    static LocalSocket createLocalSocketForAccept(LocalSocketImpl impl2) {
        return createConnectedLocalSocket(impl2, 0);
    }

    private static LocalSocket createConnectedLocalSocket(LocalSocketImpl impl2, int sockType2) {
        LocalSocket socket = new LocalSocket(impl2, sockType2);
        socket.isConnected = true;
        socket.isBound = true;
        socket.implCreated = true;
        return socket;
    }

    @Override // java.lang.Object
    public String toString() {
        return super.toString() + " impl:" + this.impl;
    }

    private void implCreateIfNeeded() throws IOException {
        if (!this.implCreated) {
            synchronized (this) {
                if (!this.implCreated) {
                    try {
                        this.impl.create(this.sockType);
                    } finally {
                        this.implCreated = true;
                    }
                }
            }
        }
    }

    public void connect(LocalSocketAddress endpoint) throws IOException {
        synchronized (this) {
            if (!this.isConnected) {
                implCreateIfNeeded();
                this.impl.connect(endpoint, 0);
                this.isConnected = true;
                this.isBound = true;
            } else {
                throw new IOException("already connected");
            }
        }
    }

    public void bind(LocalSocketAddress bindpoint) throws IOException {
        implCreateIfNeeded();
        synchronized (this) {
            if (!this.isBound) {
                this.localAddress = bindpoint;
                this.impl.bind(this.localAddress);
                this.isBound = true;
            } else {
                throw new IOException("already bound");
            }
        }
    }

    public LocalSocketAddress getLocalSocketAddress() {
        return this.localAddress;
    }

    public InputStream getInputStream() throws IOException {
        implCreateIfNeeded();
        return this.impl.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        implCreateIfNeeded();
        return this.impl.getOutputStream();
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        implCreateIfNeeded();
        this.impl.close();
    }

    public void shutdownInput() throws IOException {
        implCreateIfNeeded();
        this.impl.shutdownInput();
    }

    public void shutdownOutput() throws IOException {
        implCreateIfNeeded();
        this.impl.shutdownOutput();
    }

    public void setReceiveBufferSize(int size) throws IOException {
        this.impl.setOption(4098, Integer.valueOf(size));
    }

    public int getReceiveBufferSize() throws IOException {
        return ((Integer) this.impl.getOption(4098)).intValue();
    }

    public void setSoTimeout(int n) throws IOException {
        this.impl.setOption(4102, Integer.valueOf(n));
    }

    public int getSoTimeout() throws IOException {
        return ((Integer) this.impl.getOption(4102)).intValue();
    }

    public void setSendBufferSize(int n) throws IOException {
        this.impl.setOption(4097, Integer.valueOf(n));
    }

    public int getSendBufferSize() throws IOException {
        return ((Integer) this.impl.getOption(4097)).intValue();
    }

    public LocalSocketAddress getRemoteSocketAddress() {
        throw new UnsupportedOperationException();
    }

    public synchronized boolean isConnected() {
        return this.isConnected;
    }

    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    public synchronized boolean isBound() {
        return this.isBound;
    }

    public boolean isOutputShutdown() {
        throw new UnsupportedOperationException();
    }

    public boolean isInputShutdown() {
        throw new UnsupportedOperationException();
    }

    public void connect(LocalSocketAddress endpoint, int timeout) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void setFileDescriptorsForSend(FileDescriptor[] fds) {
        this.impl.setFileDescriptorsForSend(fds);
    }

    public FileDescriptor[] getAncillaryFileDescriptors() throws IOException {
        return this.impl.getAncillaryFileDescriptors();
    }

    public Credentials getPeerCredentials() throws IOException {
        return this.impl.getPeerCredentials();
    }

    public FileDescriptor getFileDescriptor() {
        return this.impl.getFileDescriptor();
    }
}
