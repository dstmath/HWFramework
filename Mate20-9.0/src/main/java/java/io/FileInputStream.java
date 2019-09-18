package java.io;

import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.nio.channels.FileChannel;
import libcore.io.IoBridge;
import libcore.io.IoTracker;
import sun.nio.ch.FileChannelImpl;

public class FileInputStream extends InputStream {
    private FileChannel channel;
    private final Object closeLock;
    private volatile boolean closed;
    private final FileDescriptor fd;
    private final CloseGuard guard;
    private final boolean isFdOwner;
    private final String path;
    private final IoTracker tracker;

    private static class UseManualSkipException extends Exception {
        private UseManualSkipException() {
        }
    }

    private native int available0() throws IOException;

    private native void open0(String str) throws FileNotFoundException;

    private native long skip0(long j) throws IOException, UseManualSkipException;

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public FileInputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null);
    }

    public FileInputStream(File file) throws FileNotFoundException {
        String name = null;
        this.channel = null;
        this.closeLock = new Object();
        this.closed = false;
        this.guard = CloseGuard.get();
        this.tracker = new IoTracker();
        name = file != null ? file.getPath() : name;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(name);
        }
        if (name == null) {
            throw new NullPointerException();
        } else if (!file.isInvalid()) {
            this.fd = new FileDescriptor();
            this.isFdOwner = true;
            this.path = name;
            BlockGuard.getThreadPolicy().onReadFromDisk();
            open(name);
            this.guard.open("close");
        } else {
            throw new FileNotFoundException("Invalid file path");
        }
    }

    public FileInputStream(FileDescriptor fdObj) {
        this(fdObj, false);
    }

    public FileInputStream(FileDescriptor fdObj, boolean isFdOwner2) {
        this.channel = null;
        this.closeLock = new Object();
        this.closed = false;
        this.guard = CloseGuard.get();
        this.tracker = new IoTracker();
        if (fdObj != null) {
            this.fd = fdObj;
            this.path = null;
            this.isFdOwner = isFdOwner2;
            return;
        }
        throw new NullPointerException("fdObj == null");
    }

    private void open(String name) throws FileNotFoundException {
        open0(name);
    }

    public int read() throws IOException {
        byte[] b = new byte[1];
        if (read(b, 0, 1) != -1) {
            return b[0] & Character.DIRECTIONALITY_UNDEFINED;
        }
        return -1;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (!this.closed || len <= 0) {
            this.tracker.trackIo(len);
            return IoBridge.read(this.fd, b, off, len);
        }
        throw new IOException("Stream Closed");
    }

    public long skip(long n) throws IOException {
        if (!this.closed) {
            try {
                BlockGuard.getThreadPolicy().onReadFromDisk();
                return skip0(n);
            } catch (UseManualSkipException e) {
                return super.skip(n);
            }
        } else {
            throw new IOException("Stream Closed");
        }
    }

    public int available() throws IOException {
        if (!this.closed) {
            return available0();
        }
        throw new IOException("Stream Closed");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        if (r2.channel == null) goto L_0x001b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        r2.channel.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        if (r2.isFdOwner == false) goto L_0x0024;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001f, code lost:
        libcore.io.IoBridge.closeAndSignalBlockedThreads(r2.fd);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0024, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000d, code lost:
        r2.guard.close();
     */
    public void close() throws IOException {
        synchronized (this.closeLock) {
            if (!this.closed) {
                this.closed = true;
            }
        }
    }

    public final FileDescriptor getFD() throws IOException {
        if (this.fd != null) {
            return this.fd;
        }
        throw new IOException();
    }

    public FileChannel getChannel() {
        FileChannel fileChannel;
        synchronized (this) {
            if (this.channel == null) {
                this.channel = FileChannelImpl.open(this.fd, this.path, true, false, this);
            }
            fileChannel = this.channel;
        }
        return fileChannel;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        if (this.fd != null && this.fd != FileDescriptor.in) {
            close();
        }
    }
}
