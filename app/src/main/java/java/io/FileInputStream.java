package java.io;

import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.nio.channels.FileChannel;
import libcore.io.IoBridge;
import sun.misc.IoTrace;
import sun.nio.ch.FileChannelImpl;

public class FileInputStream extends InputStream {
    private FileChannel channel;
    private final Object closeLock;
    private volatile boolean closed;
    private final FileDescriptor fd;
    private final CloseGuard guard;
    private final boolean isFdOwner;
    private final String path;

    private static class UseManualSkipException extends Exception {
        private UseManualSkipException() {
        }
    }

    private native int available0() throws IOException;

    private native void open(String str) throws FileNotFoundException;

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
        String path = file != null ? file.getPath() : null;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(path);
        }
        if (path == null) {
            throw new NullPointerException();
        } else if (file.isInvalid()) {
            throw new FileNotFoundException("Invalid file path");
        } else {
            this.fd = new FileDescriptor();
            this.isFdOwner = true;
            this.path = path;
            BlockGuard.getThreadPolicy().onReadFromDisk();
            open(path);
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
        if (fdObj == null) {
            throw new NullPointerException("fdObj == null");
        }
        this.fd = fdObj;
        this.isFdOwner = isFdOwner;
        this.path = null;
    }

    public int read() throws IOException {
        Object traceContext = IoTrace.fileReadBegin(this.path);
        byte[] b = new byte[1];
        try {
            int res = read(b, 0, 1);
            IoTrace.fileReadEnd(traceContext, (long) res);
            if (res != -1) {
                return b[0] & 255;
            }
            return -1;
        } catch (Throwable th) {
            IoTrace.fileReadEnd(traceContext, -1);
        }
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int i = 0;
        if (!this.closed || len <= 0) {
            Object traceContext = IoTrace.fileReadBegin(this.path);
            try {
                int bytesRead = IoBridge.read(this.fd, b, off, len);
                if (bytesRead != -1) {
                    i = bytesRead;
                }
                IoTrace.fileReadEnd(traceContext, (long) i);
                return bytesRead;
            } catch (Throwable th) {
                IoTrace.fileReadEnd(traceContext, (long) null);
            }
        } else {
            throw new IOException("Stream Closed");
        }
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
