package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;

abstract class NativeDispatcher {
    /* access modifiers changed from: package-private */
    public abstract void close(FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract int read(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract long readv(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract int write(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract long writev(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    NativeDispatcher() {
    }

    /* access modifiers changed from: package-private */
    public boolean needsPositionLock() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public int pread(FileDescriptor fd, long address, int len, long position) throws IOException {
        throw new IOException("Operation Unsupported");
    }

    /* access modifiers changed from: package-private */
    public int pwrite(FileDescriptor fd, long address, int len, long position) throws IOException {
        throw new IOException("Operation Unsupported");
    }

    /* access modifiers changed from: package-private */
    public void preClose(FileDescriptor fd) throws IOException {
    }
}
