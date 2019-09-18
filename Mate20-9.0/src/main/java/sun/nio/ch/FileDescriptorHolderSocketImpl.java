package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

class FileDescriptorHolderSocketImpl extends SocketImpl {
    public FileDescriptorHolderSocketImpl(FileDescriptor fd) {
        this.fd = fd;
    }

    public void setOption(int optID, Object value) throws SocketException {
        throw new UnsupportedOperationException();
    }

    public Object getOption(int optID) throws SocketException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void create(boolean stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void connect(String host, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void connect(InetAddress address, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void connect(SocketAddress address, int timeout) throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void bind(InetAddress host, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void listen(int backlog) throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void accept(SocketImpl s) throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public int available() throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void sendUrgentData(int data) throws IOException {
        throw new UnsupportedOperationException();
    }
}
