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

    public FileInputStream(String name) throws FileNotFoundException {
        File file = null;
        if (name != null) {
            file = new File(name);
        }
        this(file);
    }

    public FileInputStream(File file) throws FileNotFoundException {
        this.channel = null;
        this.closeLock = new Object();
        this.closed = false;
        this.guard = CloseGuard.get();
        this.tracker = new IoTracker();
        String name = file != null ? file.getPath() : null;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(name);
        }
        if (name == null) {
            throw new NullPointerException();
        } else if (file.isInvalid()) {
            throw new FileNotFoundException("Invalid file path");
        } else {
            this.fd = new FileDescriptor();
            this.isFdOwner = true;
            this.path = name;
            BlockGuard.getThreadPolicy().onReadFromDisk();
            open(name);
            this.guard.open("close");
        }
    }

    public FileInputStream(FileDescriptor fdObj) {
        this(fdObj, false);
    }

    public FileInputStream(FileDescriptor fdObj, boolean isFdOwner) {
        this.channel = null;
        this.closeLock = new Object();
        this.closed = false;
        this.guard = CloseGuard.get();
        this.tracker = new IoTracker();
        if (fdObj == null) {
            throw new NullPointerException("fdObj == null");
        }
        this.fd = fdObj;
        this.isFdOwner = isFdOwner;
        this.path = null;
    }

    private void open(String name) throws FileNotFoundException {
        open0(name);
    }

    public int read() throws IOException {
        byte[] b = new byte[1];
        if (read(b, 0, 1) != -1) {
            return b[0] & 255;
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
        if (this.closed) {
            throw new IOException("Stream Closed");
        }
        try {
            BlockGuard.getThreadPolicy().onReadFromDisk();
            return skip0(n);
        } catch (UseManualSkipException e) {
            return super.skip(n);
        }
    }

    public int available() throws IOException {
        if (!this.closed) {
            return available0();
        }
        throw new IOException("Stream Closed");
    }

    /* JADX WARNING: Missing block: B:11:0x000d, code:
            r2.guard.close();
     */
    /* JADX WARNING: Missing block: B:12:0x0014, code:
            if (r2.channel == null) goto L_0x001b;
     */
    /* JADX WARNING: Missing block: B:13:0x0016, code:
            r2.channel.close();
     */
    /* JADX WARNING: Missing block: B:15:0x001d, code:
            if (r2.isFdOwner == false) goto L_0x0024;
     */
    /* JADX WARNING: Missing block: B:16:0x001f, code:
            libcore.io.IoBridge.closeAndSignalBlockedThreads(r2.fd);
     */
    /* JADX WARNING: Missing block: B:17:0x0024, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void close() throws IOException {
        synchronized (this.closeLock) {
            if (this.closed) {
                return;
            }
            this.closed = true;
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

    protected void finalize() throws IOException {
        if (this.guard != null) {
            this.guard.warnIfOpen();
        }
        if (this.fd != null && this.fd != FileDescriptor.in) {
            close();
        }
    }
}
