package android.os;

import android.annotation.SystemApi;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;

@SystemApi
public final class NativeHandle implements Closeable {
    private FileDescriptor[] mFds;
    private int[] mInts;
    private boolean mOwn;

    public NativeHandle() {
        this(new FileDescriptor[0], new int[0], false);
    }

    public NativeHandle(FileDescriptor descriptor, boolean own) {
        this(new FileDescriptor[]{descriptor}, new int[0], own);
    }

    private static FileDescriptor[] createFileDescriptorArray(int[] fds) {
        FileDescriptor[] list = new FileDescriptor[fds.length];
        for (int i = 0; i < fds.length; i++) {
            FileDescriptor descriptor = new FileDescriptor();
            descriptor.setInt$(fds[i]);
            list[i] = descriptor;
        }
        return list;
    }

    private NativeHandle(int[] fds, int[] ints, boolean own) {
        this(createFileDescriptorArray(fds), ints, own);
    }

    public NativeHandle(FileDescriptor[] fds, int[] ints, boolean own) {
        this.mOwn = false;
        this.mFds = (FileDescriptor[]) fds.clone();
        this.mInts = (int[]) ints.clone();
        this.mOwn = own;
    }

    public boolean hasSingleFileDescriptor() {
        checkOpen();
        return this.mFds.length == 1 && this.mInts.length == 0;
    }

    public NativeHandle dup() throws IOException {
        FileDescriptor[] fds = new FileDescriptor[this.mFds.length];
        for (int i = 0; i < this.mFds.length; i++) {
            try {
                FileDescriptor newFd = new FileDescriptor();
                newFd.setInt$(Os.fcntlInt(this.mFds[i], OsConstants.F_DUPFD_CLOEXEC, 0));
                fds[i] = newFd;
            } catch (ErrnoException e) {
                e.rethrowAsIOException();
            }
        }
        return new NativeHandle(fds, this.mInts, true);
    }

    private void checkOpen() {
        if (this.mFds == null) {
            throw new IllegalStateException("NativeHandle is invalidated after close.");
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        checkOpen();
        if (this.mOwn) {
            try {
                for (FileDescriptor fd : this.mFds) {
                    Os.close(fd);
                }
            } catch (ErrnoException e) {
                e.rethrowAsIOException();
            }
            this.mOwn = false;
        }
        this.mFds = null;
        this.mInts = null;
    }

    public FileDescriptor getFileDescriptor() {
        checkOpen();
        if (hasSingleFileDescriptor()) {
            return this.mFds[0];
        }
        throw new IllegalStateException("NativeHandle is not single file descriptor. Contents must be retreived through getFileDescriptors and getInts.");
    }

    private int[] getFdsAsIntArray() {
        checkOpen();
        int numFds = this.mFds.length;
        int[] fds = new int[numFds];
        for (int i = 0; i < numFds; i++) {
            fds[i] = this.mFds[i].getInt$();
        }
        return fds;
    }

    public FileDescriptor[] getFileDescriptors() {
        checkOpen();
        return this.mFds;
    }

    public int[] getInts() {
        checkOpen();
        return this.mInts;
    }
}
