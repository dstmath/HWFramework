package sun.nio.ch;

import dalvik.system.BlockGuard;
import java.io.FileDescriptor;
import java.io.IOException;

class DatagramDispatcher extends NativeDispatcher {
    static native int read0(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    static native long readv0(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    static native int write0(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    static native long writev0(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    DatagramDispatcher() {
    }

    int read(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return read0(fd, address, len);
    }

    long readv(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return readv0(fd, address, len);
    }

    int write(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return write0(fd, address, len);
    }

    long writev(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return writev0(fd, address, len);
    }

    void close(FileDescriptor fd) throws IOException {
        FileDispatcherImpl.close0(fd);
    }

    void preClose(FileDescriptor fd) throws IOException {
        FileDispatcherImpl.preCloseImpl(fd);
    }
}
