package sun.nio.ch;

import dalvik.system.BlockGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.SelectableChannel;

class FileDispatcherImpl extends FileDispatcher {
    static native void close0(FileDescriptor fileDescriptor) throws IOException;

    static native void closeIntFD(int i) throws IOException;

    static native int force0(FileDescriptor fileDescriptor, boolean z) throws IOException;

    static native int lock0(FileDescriptor fileDescriptor, boolean z, long j, long j2, boolean z2) throws IOException;

    static native void preClose0(FileDescriptor fileDescriptor) throws IOException;

    static native int pread0(FileDescriptor fileDescriptor, long j, int i, long j2) throws IOException;

    static native int pwrite0(FileDescriptor fileDescriptor, long j, int i, long j2) throws IOException;

    static native int read0(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    static native long readv0(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    static native void release0(FileDescriptor fileDescriptor, long j, long j2) throws IOException;

    static native long size0(FileDescriptor fileDescriptor) throws IOException;

    static native int truncate0(FileDescriptor fileDescriptor, long j) throws IOException;

    static native int write0(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    static native long writev0(FileDescriptor fileDescriptor, long j, int i) throws IOException;

    FileDispatcherImpl(boolean append) {
    }

    FileDispatcherImpl() {
    }

    /* access modifiers changed from: package-private */
    public int read(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return read0(fd, address, len);
    }

    /* access modifiers changed from: package-private */
    public int pread(FileDescriptor fd, long address, int len, long position) throws IOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return pread0(fd, address, len, position);
    }

    /* access modifiers changed from: package-private */
    public long readv(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return readv0(fd, address, len);
    }

    /* access modifiers changed from: package-private */
    public int write(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return write0(fd, address, len);
    }

    /* access modifiers changed from: package-private */
    public int pwrite(FileDescriptor fd, long address, int len, long position) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return pwrite0(fd, address, len, position);
    }

    /* access modifiers changed from: package-private */
    public long writev(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return writev0(fd, address, len);
    }

    /* access modifiers changed from: package-private */
    public int force(FileDescriptor fd, boolean metaData) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return force0(fd, metaData);
    }

    /* access modifiers changed from: package-private */
    public int truncate(FileDescriptor fd, long size) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return truncate0(fd, size);
    }

    /* access modifiers changed from: package-private */
    public long size(FileDescriptor fd) throws IOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return size0(fd);
    }

    /* access modifiers changed from: package-private */
    public int lock(FileDescriptor fd, boolean blocking, long pos, long size, boolean shared) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return lock0(fd, blocking, pos, size, shared);
    }

    /* access modifiers changed from: package-private */
    public void release(FileDescriptor fd, long pos, long size) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        release0(fd, pos, size);
    }

    /* access modifiers changed from: package-private */
    public void close(FileDescriptor fd) throws IOException {
        close0(fd);
    }

    /* access modifiers changed from: package-private */
    public void preClose(FileDescriptor fd) throws IOException {
        preClose0(fd);
    }

    /* access modifiers changed from: package-private */
    public FileDescriptor duplicateForMapping(FileDescriptor fd) {
        return new FileDescriptor();
    }

    /* access modifiers changed from: package-private */
    public boolean canTransferToDirectly(SelectableChannel sc) {
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean transferToDirectlyNeedsPositionLock() {
        return false;
    }
}
