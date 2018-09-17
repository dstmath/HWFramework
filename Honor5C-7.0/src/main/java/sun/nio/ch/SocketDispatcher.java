package sun.nio.ch;

import dalvik.system.BlockGuard;
import java.io.FileDescriptor;
import java.io.IOException;

class SocketDispatcher extends NativeDispatcher {
    SocketDispatcher() {
    }

    int read(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return FileDispatcherImpl.read0(fd, address, len);
    }

    long readv(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return FileDispatcherImpl.readv0(fd, address, len);
    }

    int write(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return FileDispatcherImpl.write0(fd, address, len);
    }

    long writev(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return FileDispatcherImpl.writev0(fd, address, len);
    }

    void close(FileDescriptor fd) throws IOException {
        FileDispatcherImpl.close0(fd);
    }

    void preClose(FileDescriptor fd) throws IOException {
        FileDispatcherImpl.preCloseImpl(fd);
    }
}
