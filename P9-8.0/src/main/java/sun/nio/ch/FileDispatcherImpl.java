package sun.nio.ch;

import dalvik.system.BlockGuard;
import dalvik.system.SocketTagger;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectableChannel;

class FileDispatcherImpl extends FileDispatcher {
    static native void close0(FileDescriptor fileDescriptor) throws IOException;

    static native void closeIntFD(int i) throws IOException;

    static native int force0(FileDescriptor fileDescriptor, boolean z) throws IOException;

    static native int lock0(FileDescriptor fileDescriptor, boolean z, long j, long j2, boolean z2) throws IOException;

    private static native void preClose0(FileDescriptor fileDescriptor) throws IOException;

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

    int read(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return read0(fd, address, len);
    }

    int pread(FileDescriptor fd, long address, int len, long position) throws IOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return pread0(fd, address, len, position);
    }

    long readv(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return readv0(fd, address, len);
    }

    int write(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return write0(fd, address, len);
    }

    int pwrite(FileDescriptor fd, long address, int len, long position) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return pwrite0(fd, address, len, position);
    }

    long writev(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return writev0(fd, address, len);
    }

    int force(FileDescriptor fd, boolean metaData) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return force0(fd, metaData);
    }

    int truncate(FileDescriptor fd, long size) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return truncate0(fd, size);
    }

    long size(FileDescriptor fd) throws IOException {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        return size0(fd);
    }

    int lock(FileDescriptor fd, boolean blocking, long pos, long size, boolean shared) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        return lock0(fd, blocking, pos, size, shared);
    }

    void release(FileDescriptor fd, long pos, long size) throws IOException {
        BlockGuard.getThreadPolicy().onWriteToDisk();
        release0(fd, pos, size);
    }

    void close(FileDescriptor fd) throws IOException {
        close0(fd);
    }

    void preClose(FileDescriptor fd) throws IOException {
        preCloseImpl(fd);
    }

    static void preCloseImpl(FileDescriptor fd) throws IOException {
        if (fd.isSocket$()) {
            try {
                SocketTagger.get().untag(fd);
            } catch (SocketException e) {
            }
        }
        preClose0(fd);
    }

    FileDescriptor duplicateForMapping(FileDescriptor fd) {
        return new FileDescriptor();
    }

    boolean canTransferToDirectly(SelectableChannel sc) {
        return true;
    }

    boolean transferToDirectlyNeedsPositionLock() {
        return false;
    }
}
