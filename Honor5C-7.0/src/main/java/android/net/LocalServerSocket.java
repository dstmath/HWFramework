package android.net;

import java.io.FileDescriptor;
import java.io.IOException;

public class LocalServerSocket {
    private static final int LISTEN_BACKLOG = 50;
    private final LocalSocketImpl impl;
    private final LocalSocketAddress localAddress;

    public LocalServerSocket(String name) throws IOException {
        this.impl = new LocalSocketImpl();
        this.impl.create(2);
        this.localAddress = new LocalSocketAddress(name);
        this.impl.bind(this.localAddress);
        this.impl.listen(LISTEN_BACKLOG);
    }

    public LocalServerSocket(FileDescriptor fd) throws IOException {
        this.impl = new LocalSocketImpl(fd);
        this.impl.listen(LISTEN_BACKLOG);
        this.localAddress = this.impl.getSockAddress();
    }

    public LocalSocketAddress getLocalSocketAddress() {
        return this.localAddress;
    }

    public LocalSocket accept() throws IOException {
        LocalSocketImpl acceptedImpl = new LocalSocketImpl();
        this.impl.accept(acceptedImpl);
        return new LocalSocket(acceptedImpl, 0);
    }

    public FileDescriptor getFileDescriptor() {
        return this.impl.getFileDescriptor();
    }

    public void close() throws IOException {
        this.impl.close();
    }
}
