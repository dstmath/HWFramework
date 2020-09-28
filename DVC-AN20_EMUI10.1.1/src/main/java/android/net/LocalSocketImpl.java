package android.net;

import android.annotation.UnsupportedAppUsage;
import android.system.ErrnoException;
import android.system.Int32Ref;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructLinger;
import android.system.StructTimeval;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* access modifiers changed from: package-private */
public class LocalSocketImpl {
    private FileDescriptor fd;
    private SocketInputStream fis;
    private SocketOutputStream fos;
    @UnsupportedAppUsage
    FileDescriptor[] inboundFileDescriptors;
    private boolean mFdCreatedInternally;
    @UnsupportedAppUsage
    FileDescriptor[] outboundFileDescriptors;
    private Object readMonitor = new Object();
    private Object writeMonitor = new Object();

    private native void bindLocal(FileDescriptor fileDescriptor, String str, int i) throws IOException;

    private native void connectLocal(FileDescriptor fileDescriptor, String str, int i) throws IOException;

    private native Credentials getPeerCredentials_native(FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native int read_native(FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native int readba_native(byte[] bArr, int i, int i2, FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void write_native(int i, FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void writeba_native(byte[] bArr, int i, int i2, FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: package-private */
    public class SocketInputStream extends InputStream {
        SocketInputStream() {
        }

        @Override // java.io.InputStream
        public int available() throws IOException {
            FileDescriptor myFd = LocalSocketImpl.this.fd;
            if (myFd != null) {
                Int32Ref avail = new Int32Ref(0);
                try {
                    Os.ioctlInt(myFd, OsConstants.FIONREAD, avail);
                    return avail.value;
                } catch (ErrnoException e) {
                    throw e.rethrowAsIOException();
                }
            } else {
                throw new IOException("socket closed");
            }
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
        public void close() throws IOException {
            LocalSocketImpl.this.close();
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            int ret;
            synchronized (LocalSocketImpl.this.readMonitor) {
                FileDescriptor myFd = LocalSocketImpl.this.fd;
                if (myFd != null) {
                    ret = LocalSocketImpl.this.read_native(myFd);
                } else {
                    throw new IOException("socket closed");
                }
            }
            return ret;
        }

        @Override // java.io.InputStream
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override // java.io.InputStream
        public int read(byte[] b, int off, int len) throws IOException {
            int ret;
            synchronized (LocalSocketImpl.this.readMonitor) {
                FileDescriptor myFd = LocalSocketImpl.this.fd;
                if (myFd == null) {
                    throw new IOException("socket closed");
                } else if (off < 0 || len < 0 || off + len > b.length) {
                    throw new ArrayIndexOutOfBoundsException();
                } else {
                    ret = LocalSocketImpl.this.readba_native(b, off, len, myFd);
                }
            }
            return ret;
        }
    }

    /* access modifiers changed from: package-private */
    public class SocketOutputStream extends OutputStream {
        SocketOutputStream() {
        }

        @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            LocalSocketImpl.this.close();
        }

        @Override // java.io.OutputStream
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override // java.io.OutputStream
        public void write(byte[] b, int off, int len) throws IOException {
            synchronized (LocalSocketImpl.this.writeMonitor) {
                FileDescriptor myFd = LocalSocketImpl.this.fd;
                if (myFd == null) {
                    throw new IOException("socket closed");
                } else if (off < 0 || len < 0 || off + len > b.length) {
                    throw new ArrayIndexOutOfBoundsException();
                } else {
                    LocalSocketImpl.this.writeba_native(b, off, len, myFd);
                }
            }
        }

        @Override // java.io.OutputStream
        public void write(int b) throws IOException {
            synchronized (LocalSocketImpl.this.writeMonitor) {
                FileDescriptor myFd = LocalSocketImpl.this.fd;
                if (myFd != null) {
                    LocalSocketImpl.this.write_native(b, myFd);
                } else {
                    throw new IOException("socket closed");
                }
            }
        }

        @Override // java.io.OutputStream, java.io.Flushable
        public void flush() throws IOException {
            FileDescriptor myFd = LocalSocketImpl.this.fd;
            if (myFd != null) {
                Int32Ref pending = new Int32Ref(0);
                while (true) {
                    try {
                        Os.ioctlInt(myFd, OsConstants.TIOCOUTQ, pending);
                        if (pending.value > 0) {
                            int left = pending.value;
                            if (left <= 1000) {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    return;
                                }
                            } else if (left <= 5000) {
                                Thread.sleep(5);
                            } else {
                                Thread.sleep(10);
                            }
                        } else {
                            return;
                        }
                    } catch (ErrnoException e2) {
                        throw e2.rethrowAsIOException();
                    }
                }
            } else {
                throw new IOException("socket closed");
            }
        }
    }

    @UnsupportedAppUsage
    LocalSocketImpl() {
    }

    LocalSocketImpl(FileDescriptor fd2) {
        this.fd = fd2;
    }

    public String toString() {
        return super.toString() + " fd:" + this.fd;
    }

    public void create(int sockType) throws IOException {
        int osType;
        if (this.fd == null) {
            if (sockType == 1) {
                osType = OsConstants.SOCK_DGRAM;
            } else if (sockType == 2) {
                osType = OsConstants.SOCK_STREAM;
            } else if (sockType == 3) {
                osType = OsConstants.SOCK_SEQPACKET;
            } else {
                throw new IllegalStateException("unknown sockType");
            }
            try {
                this.fd = Os.socket(OsConstants.AF_UNIX, osType, 0);
                this.mFdCreatedInternally = true;
            } catch (ErrnoException e) {
                e.rethrowAsIOException();
            }
        } else {
            throw new IOException("LocalSocketImpl already has an fd");
        }
    }

    public void close() throws IOException {
        synchronized (this) {
            if (this.fd == null || !this.mFdCreatedInternally) {
                this.fd = null;
                return;
            }
            try {
                Os.close(this.fd);
            } catch (ErrnoException e) {
                e.rethrowAsIOException();
            }
            this.fd = null;
        }
    }

    /* access modifiers changed from: protected */
    public void connect(LocalSocketAddress address, int timeout) throws IOException {
        FileDescriptor fileDescriptor = this.fd;
        if (fileDescriptor != null) {
            connectLocal(fileDescriptor, address.getName(), address.getNamespace().getId());
            return;
        }
        throw new IOException("socket not created");
    }

    public void bind(LocalSocketAddress endpoint) throws IOException {
        FileDescriptor fileDescriptor = this.fd;
        if (fileDescriptor != null) {
            bindLocal(fileDescriptor, endpoint.getName(), endpoint.getNamespace().getId());
            return;
        }
        throw new IOException("socket not created");
    }

    /* access modifiers changed from: protected */
    public void listen(int backlog) throws IOException {
        FileDescriptor fileDescriptor = this.fd;
        if (fileDescriptor != null) {
            try {
                Os.listen(fileDescriptor, backlog);
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        } else {
            throw new IOException("socket not created");
        }
    }

    /* access modifiers changed from: protected */
    public void accept(LocalSocketImpl s) throws IOException {
        FileDescriptor fileDescriptor = this.fd;
        if (fileDescriptor != null) {
            try {
                s.fd = Os.accept(fileDescriptor, null);
                s.mFdCreatedInternally = true;
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        } else {
            throw new IOException("socket not created");
        }
    }

    /* access modifiers changed from: protected */
    public InputStream getInputStream() throws IOException {
        SocketInputStream socketInputStream;
        if (this.fd != null) {
            synchronized (this) {
                if (this.fis == null) {
                    this.fis = new SocketInputStream();
                }
                socketInputStream = this.fis;
            }
            return socketInputStream;
        }
        throw new IOException("socket not created");
    }

    /* access modifiers changed from: protected */
    public OutputStream getOutputStream() throws IOException {
        SocketOutputStream socketOutputStream;
        if (this.fd != null) {
            synchronized (this) {
                if (this.fos == null) {
                    this.fos = new SocketOutputStream();
                }
                socketOutputStream = this.fos;
            }
            return socketOutputStream;
        }
        throw new IOException("socket not created");
    }

    /* access modifiers changed from: protected */
    public int available() throws IOException {
        return getInputStream().available();
    }

    /* access modifiers changed from: protected */
    public void shutdownInput() throws IOException {
        FileDescriptor fileDescriptor = this.fd;
        if (fileDescriptor != null) {
            try {
                Os.shutdown(fileDescriptor, OsConstants.SHUT_RD);
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        } else {
            throw new IOException("socket not created");
        }
    }

    /* access modifiers changed from: protected */
    public void shutdownOutput() throws IOException {
        FileDescriptor fileDescriptor = this.fd;
        if (fileDescriptor != null) {
            try {
                Os.shutdown(fileDescriptor, OsConstants.SHUT_WR);
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        } else {
            throw new IOException("socket not created");
        }
    }

    /* access modifiers changed from: protected */
    public FileDescriptor getFileDescriptor() {
        return this.fd;
    }

    /* access modifiers changed from: protected */
    public boolean supportsUrgentData() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void sendUrgentData(int data) throws IOException {
        throw new RuntimeException("not impled");
    }

    public Object getOption(int optID) throws IOException {
        FileDescriptor fileDescriptor = this.fd;
        if (fileDescriptor == null) {
            throw new IOException("socket not created");
        } else if (optID == 1) {
            return Integer.valueOf(Os.getsockoptInt(fileDescriptor, OsConstants.IPPROTO_TCP, OsConstants.TCP_NODELAY));
        } else {
            if (optID != 4) {
                if (optID == 128) {
                    StructLinger linger = Os.getsockoptLinger(fileDescriptor, OsConstants.SOL_SOCKET, OsConstants.SO_LINGER);
                    if (!linger.isOn()) {
                        return -1;
                    }
                    return Integer.valueOf(linger.l_linger);
                } else if (optID == 4102) {
                    return Integer.valueOf((int) Os.getsockoptTimeval(fileDescriptor, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO).toMillis());
                } else {
                    if (!(optID == 4097 || optID == 4098)) {
                        try {
                            throw new IOException("Unknown option: " + optID);
                        } catch (ErrnoException e) {
                            throw e.rethrowAsIOException();
                        }
                    }
                }
            }
            return Integer.valueOf(Os.getsockoptInt(this.fd, OsConstants.SOL_SOCKET, javaSoToOsOpt(optID)));
        }
    }

    public void setOption(int optID, Object value) throws IOException {
        if (this.fd != null) {
            int boolValue = -1;
            int intValue = 0;
            if (value instanceof Integer) {
                intValue = ((Integer) value).intValue();
            } else if (value instanceof Boolean) {
                boolValue = ((Boolean) value).booleanValue();
            } else {
                throw new IOException("bad value: " + value);
            }
            if (optID != 1) {
                if (optID != 4) {
                    if (optID == 128) {
                        Os.setsockoptLinger(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_LINGER, new StructLinger(boolValue, intValue));
                        return;
                    } else if (optID == 4102) {
                        StructTimeval timeval = StructTimeval.fromMillis((long) intValue);
                        Os.setsockoptTimeval(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, timeval);
                        Os.setsockoptTimeval(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, timeval);
                        return;
                    } else if (!(optID == 4097 || optID == 4098)) {
                        try {
                            throw new IOException("Unknown option: " + optID);
                        } catch (ErrnoException e) {
                            throw e.rethrowAsIOException();
                        }
                    }
                }
                Os.setsockoptInt(this.fd, OsConstants.SOL_SOCKET, javaSoToOsOpt(optID), intValue);
                return;
            }
            Os.setsockoptInt(this.fd, OsConstants.IPPROTO_TCP, OsConstants.TCP_NODELAY, intValue);
            return;
        }
        throw new IOException("socket not created");
    }

    public void setFileDescriptorsForSend(FileDescriptor[] fds) {
        synchronized (this.writeMonitor) {
            this.outboundFileDescriptors = fds;
        }
    }

    public FileDescriptor[] getAncillaryFileDescriptors() throws IOException {
        FileDescriptor[] result;
        synchronized (this.readMonitor) {
            result = this.inboundFileDescriptors;
            this.inboundFileDescriptors = null;
        }
        return result;
    }

    public Credentials getPeerCredentials() throws IOException {
        return getPeerCredentials_native(this.fd);
    }

    public LocalSocketAddress getSockAddress() throws IOException {
        return null;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws IOException {
        close();
    }

    private static int javaSoToOsOpt(int optID) {
        if (optID == 4) {
            return OsConstants.SO_REUSEADDR;
        }
        if (optID == 4097) {
            return OsConstants.SO_SNDBUF;
        }
        if (optID == 4098) {
            return OsConstants.SO_RCVBUF;
        }
        throw new UnsupportedOperationException("Unknown option: " + optID);
    }
}
