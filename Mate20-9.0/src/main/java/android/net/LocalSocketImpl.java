package android.net;

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

class LocalSocketImpl {
    /* access modifiers changed from: private */
    public FileDescriptor fd;
    private SocketInputStream fis;
    private SocketOutputStream fos;
    FileDescriptor[] inboundFileDescriptors;
    private boolean mFdCreatedInternally;
    FileDescriptor[] outboundFileDescriptors;
    /* access modifiers changed from: private */
    public Object readMonitor = new Object();
    /* access modifiers changed from: private */
    public Object writeMonitor = new Object();

    class SocketInputStream extends InputStream {
        SocketInputStream() {
        }

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

        public void close() throws IOException {
            LocalSocketImpl.this.close();
        }

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

        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

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

    class SocketOutputStream extends OutputStream {
        SocketOutputStream() {
        }

        public void close() throws IOException {
            LocalSocketImpl.this.close();
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

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

    private native void bindLocal(FileDescriptor fileDescriptor, String str, int i) throws IOException;

    private native void connectLocal(FileDescriptor fileDescriptor, String str, int i) throws IOException;

    private native Credentials getPeerCredentials_native(FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: private */
    public native int read_native(FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: private */
    public native int readba_native(byte[] bArr, int i, int i2, FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: private */
    public native void write_native(int i, FileDescriptor fileDescriptor) throws IOException;

    /* access modifiers changed from: private */
    public native void writeba_native(byte[] bArr, int i, int i2, FileDescriptor fileDescriptor) throws IOException;

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
            switch (sockType) {
                case 1:
                    osType = OsConstants.SOCK_DGRAM;
                    break;
                case 2:
                    osType = OsConstants.SOCK_STREAM;
                    break;
                case 3:
                    osType = OsConstants.SOCK_SEQPACKET;
                    break;
                default:
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
        if (this.fd != null) {
            connectLocal(this.fd, address.getName(), address.getNamespace().getId());
            return;
        }
        throw new IOException("socket not created");
    }

    public void bind(LocalSocketAddress endpoint) throws IOException {
        if (this.fd != null) {
            bindLocal(this.fd, endpoint.getName(), endpoint.getNamespace().getId());
            return;
        }
        throw new IOException("socket not created");
    }

    /* access modifiers changed from: protected */
    public void listen(int backlog) throws IOException {
        if (this.fd != null) {
            try {
                Os.listen(this.fd, backlog);
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        } else {
            throw new IOException("socket not created");
        }
    }

    /* access modifiers changed from: protected */
    public void accept(LocalSocketImpl s) throws IOException {
        if (this.fd != null) {
            try {
                s.fd = Os.accept(this.fd, null);
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
        if (this.fd != null) {
            try {
                Os.shutdown(this.fd, OsConstants.SHUT_RD);
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        } else {
            throw new IOException("socket not created");
        }
    }

    /* access modifiers changed from: protected */
    public void shutdownOutput() throws IOException {
        if (this.fd != null) {
            try {
                Os.shutdown(this.fd, OsConstants.SHUT_WR);
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
        int toReturn;
        if (this.fd != null) {
            if (optID != 1) {
                if (optID != 4) {
                    if (optID == 128) {
                        StructLinger linger = Os.getsockoptLinger(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_LINGER);
                        if (!linger.isOn()) {
                            toReturn = -1;
                        } else {
                            toReturn = Integer.valueOf(linger.l_linger);
                        }
                    } else if (optID != 4102) {
                        switch (optID) {
                            case 4097:
                            case 4098:
                                break;
                            default:
                                try {
                                    throw new IOException("Unknown option: " + optID);
                                } catch (ErrnoException e) {
                                    throw e.rethrowAsIOException();
                                }
                        }
                    } else {
                        toReturn = Integer.valueOf((int) Os.getsockoptTimeval(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO).toMillis());
                    }
                }
                toReturn = Integer.valueOf(Os.getsockoptInt(this.fd, OsConstants.SOL_SOCKET, javaSoToOsOpt(optID)));
            } else {
                toReturn = Integer.valueOf(Os.getsockoptInt(this.fd, OsConstants.IPPROTO_TCP, OsConstants.TCP_NODELAY));
            }
            return toReturn;
        }
        throw new IOException("socket not created");
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
                    } else if (optID != 4102) {
                        switch (optID) {
                            case 4097:
                            case 4098:
                                break;
                            default:
                                try {
                                    throw new IOException("Unknown option: " + optID);
                                } catch (ErrnoException e) {
                                    throw e.rethrowAsIOException();
                                }
                        }
                    } else {
                        StructTimeval timeval = StructTimeval.fromMillis((long) intValue);
                        Os.setsockoptTimeval(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, timeval);
                        Os.setsockoptTimeval(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, timeval);
                        return;
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
        switch (optID) {
            case 4097:
                return OsConstants.SO_SNDBUF;
            case 4098:
                return OsConstants.SO_RCVBUF;
            default:
                throw new UnsupportedOperationException("Unknown option: " + optID);
        }
    }
}
