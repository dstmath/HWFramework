package java.io;

import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.nio.channels.FileChannel;
import libcore.io.IoBridge;
import libcore.io.IoTracker;
import sun.nio.ch.FileChannelImpl;

public class FileOutputStream extends OutputStream {
    private final boolean append;
    private FileChannel channel;
    private final Object closeLock;
    private volatile boolean closed;
    private final FileDescriptor fd;
    private final CloseGuard guard;
    private final boolean isFdOwner;
    private final String path;
    private final IoTracker tracker;

    private native void open0(String str, boolean z) throws FileNotFoundException;

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public FileOutputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null, false);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public FileOutputStream(String name, boolean append2) throws FileNotFoundException {
        this(name != null ? new File(name) : null, append2);
    }

    public FileOutputStream(File file) throws FileNotFoundException {
        this(file, false);
    }

    public FileOutputStream(File file, boolean append2) throws FileNotFoundException {
        this.closeLock = new Object();
        this.closed = false;
        this.guard = CloseGuard.get();
        this.tracker = new IoTracker();
        String name = file != null ? file.getPath() : null;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(name);
        }
        if (name == null) {
            throw new NullPointerException();
        } else if (!file.isInvalid()) {
            this.fd = new FileDescriptor();
            this.isFdOwner = true;
            this.append = append2;
            this.path = name;
            BlockGuard.getThreadPolicy().onWriteToDisk();
            open(name, append2);
            this.guard.open("close");
        } else {
            throw new FileNotFoundException("Invalid file path");
        }
    }

    public FileOutputStream(FileDescriptor fdObj) {
        this(fdObj, false);
    }

    public FileOutputStream(FileDescriptor fdObj, boolean isFdOwner2) {
        this.closeLock = new Object();
        this.closed = false;
        this.guard = CloseGuard.get();
        this.tracker = new IoTracker();
        if (fdObj != null) {
            this.fd = fdObj;
            this.append = false;
            this.path = null;
            this.isFdOwner = isFdOwner2;
            return;
        }
        throw new NullPointerException("fdObj == null");
    }

    private void open(String name, boolean append2) throws FileNotFoundException {
        open0(name, append2);
    }

    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (!this.closed || len <= 0) {
            this.tracker.trackIo(len);
            IoBridge.write(this.fd, b, off, len);
            return;
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
                this.channel = FileChannelImpl.open(this.fd, this.path, false, true, this.append, this);
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
        if (this.fd == null) {
            return;
        }
        if (this.fd == FileDescriptor.out || this.fd == FileDescriptor.err) {
            flush();
        } else {
            close();
        }
    }
}
