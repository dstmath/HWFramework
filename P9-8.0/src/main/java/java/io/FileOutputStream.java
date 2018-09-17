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

    public FileOutputStream(String name) throws FileNotFoundException {
        File file = null;
        if (name != null) {
            file = new File(name);
        }
        this(file, false);
    }

    public FileOutputStream(String name, boolean append) throws FileNotFoundException {
        File file = null;
        if (name != null) {
            file = new File(name);
        }
        this(file, append);
    }

    public FileOutputStream(File file) throws FileNotFoundException {
        this(file, false);
    }

    public FileOutputStream(File file, boolean append) throws FileNotFoundException {
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
        } else if (file.isInvalid()) {
            throw new FileNotFoundException("Invalid file path");
        } else {
            this.fd = new FileDescriptor();
            this.append = append;
            this.path = name;
            this.isFdOwner = true;
            BlockGuard.getThreadPolicy().onWriteToDisk();
            open(name, append);
            this.guard.open("close");
        }
    }

    public FileOutputStream(FileDescriptor fdObj) {
        this(fdObj, false);
    }

    public FileOutputStream(FileDescriptor fdObj, boolean isFdOwner) {
        this.closeLock = new Object();
        this.closed = false;
        this.guard = CloseGuard.get();
        this.tracker = new IoTracker();
        if (fdObj == null) {
            throw new NullPointerException("fdObj == null");
        }
        this.fd = fdObj;
        this.path = null;
        this.append = false;
        this.isFdOwner = isFdOwner;
    }

    private void open(String name, boolean append) throws FileNotFoundException {
        open0(name, append);
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
                this.channel = FileChannelImpl.open(this.fd, this.path, false, true, this.append, this);
            }
            fileChannel = this.channel;
        }
        return fileChannel;
    }

    protected void finalize() throws IOException {
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
