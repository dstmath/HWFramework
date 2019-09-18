package sun.nio.ch;

import dalvik.system.BlockGuard;
import java.io.FileDescriptor;
import java.io.IOException;

class SocketDispatcher extends NativeDispatcher {
    SocketDispatcher() {
    }

    /* access modifiers changed from: package-private */
    public int read(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return FileDispatcherImpl.read0(fd, address, len);
    }

    /* access modifiers changed from: package-private */
    public long readv(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return FileDispatcherImpl.readv0(fd, address, len);
    }

    /* access modifiers changed from: package-private */
    public int write(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return FileDispatcherImpl.write0(fd, address, len);
    }

    /* access modifiers changed from: package-private */
    public long writev(FileDescriptor fd, long address, int len) throws IOException {
        BlockGuard.getThreadPolicy().onNetwork();
        return FileDispatcherImpl.writev0(fd, address, len);
    }

    /* access modifiers changed from: package-private */
    public void close(FileDescriptor fd) throws IOException {
        FileDispatcherImpl.close0(fd);
    }

    /* access modifiers changed from: package-private */
    public void preClose(FileDescriptor fd) throws IOException {
        FileDispatcherImpl.preClose0(fd);
    }
}
