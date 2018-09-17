package java.io;

import android.system.Os;
import android.system.OsConstants;
import sun.misc.JavaIOFileDescriptorAccess;
import sun.misc.SharedSecrets;

public final class FileDescriptor {
    public static final FileDescriptor err = dupFd(2);
    public static final FileDescriptor in = dupFd(0);
    public static final FileDescriptor out = dupFd(1);
    private int descriptor;

    private static native boolean isSocket(int i);

    public native void sync() throws SyncFailedException;

    public FileDescriptor() {
        this.descriptor = -1;
    }

    private FileDescriptor(int descriptor) {
        this.descriptor = descriptor;
    }

    static {
        SharedSecrets.setJavaIOFileDescriptorAccess(new JavaIOFileDescriptorAccess() {
            public void set(FileDescriptor obj, int fd) {
                obj.descriptor = fd;
            }

            public int get(FileDescriptor obj) {
                return obj.descriptor;
            }

            public void setHandle(FileDescriptor obj, long handle) {
                throw new UnsupportedOperationException();
            }

            public long getHandle(FileDescriptor obj) {
                throw new UnsupportedOperationException();
            }
        });
    }

    public boolean valid() {
        return this.descriptor != -1;
    }

    public final int getInt$() {
        return this.descriptor;
    }

    public final void setInt$(int fd) {
        this.descriptor = fd;
    }

    public boolean isSocket$() {
        return isSocket(this.descriptor);
    }

    private static FileDescriptor dupFd(int fd) {
        try {
            return new FileDescriptor(Os.fcntlInt(new FileDescriptor(fd), OsConstants.F_DUPFD_CLOEXEC, 0));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
