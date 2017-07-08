package java.io;

import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.nio.channels.FileChannel;
import libcore.io.IoBridge;
import sun.misc.IoTrace;
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

    private native void open(String str, boolean z) throws FileNotFoundException;

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
        String path = file != null ? file.getPath() : null;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(path);
        }
        if (path == null) {
            throw new NullPointerException();
        } else if (file.isInvalid()) {
            throw new FileNotFoundException("Invalid file path");
        } else {
            this.fd = new FileDescriptor();
            this.append = append;
            this.path = path;
            this.isFdOwner = true;
            BlockGuard.getThreadPolicy().onWriteToDisk();
            open(path, append);
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
        if (fdObj == null) {
            throw new NullPointerException("fdObj == null");
        }
        this.fd = fdObj;
        this.path = null;
        this.append = false;
        this.isFdOwner = isFdOwner;
    }

    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (!this.closed || len <= 0) {
            Object traceContext = IoTrace.fileWriteBegin(this.path);
            try {
                IoBridge.write(this.fd, b, off, len);
                int bytesWritten = len;
                IoTrace.fileWriteEnd(traceContext, (long) len);
            } catch (Throwable th) {
                IoTrace.fileWriteEnd(traceContext, 0);
            }
        } else {
            throw new IOException("Stream Closed");
        }
    }

    public void close() throws IOException {
        synchronized (this.closeLock) {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.guard.close();
            if (this.channel != null) {
                this.channel.close();
            }
            if (this.isFdOwner) {
                IoBridge.closeAndSignalBlockedThreads(this.fd);
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
