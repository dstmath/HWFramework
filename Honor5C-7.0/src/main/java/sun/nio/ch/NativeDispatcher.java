package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;

abstract class NativeDispatcher {
    abstract void close(FileDescriptor fileDescriptor) throws IOException;

    abstract int read(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    abstract long readv(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    abstract int write(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    abstract long writev(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    NativeDispatcher() {
    }

    boolean needsPositionLock() {
        return false;
    }

    int pread(FileDescriptor fd, long address, int len, long position) throws IOException {
        throw new IOException("Operation Unsupported");
    }

    int pwrite(FileDescriptor fd, long address, int len, long position) throws IOException {
        throw new IOException("Operation Unsupported");
    }

    void preClose(FileDescriptor fd) throws IOException {
    }
}
