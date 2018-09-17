package android.net;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.bluetooth.BluetoothInputDevice;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructLinger;
import android.system.StructTimeval;
import android.util.MutableInt;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class LocalSocketImpl {
    private FileDescriptor fd;
    private SocketInputStream fis;
    private SocketOutputStream fos;
    FileDescriptor[] inboundFileDescriptors;
    private boolean mFdCreatedInternally;
    FileDescriptor[] outboundFileDescriptors;
    private Object readMonitor = new Object();
    private Object writeMonitor = new Object();

    class SocketInputStream extends InputStream {
        SocketInputStream() {
        }

        public int available() throws IOException {
            FileDescriptor myFd = LocalSocketImpl.this.fd;
            if (myFd == null) {
                throw new IOException("socket closed");
            }
            MutableInt avail = new MutableInt(0);
            try {
                Os.ioctlInt(myFd, OsConstants.FIONREAD, avail);
                return avail.value;
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        }

        public void close() throws IOException {
            LocalSocketImpl.this.close();
        }

        public int read() throws IOException {
            int ret;
            synchronized (LocalSocketImpl.this.readMonitor) {
                FileDescriptor myFd = LocalSocketImpl.this.fd;
                if (myFd == null) {
                    throw new IOException("socket closed");
                }
                ret = LocalSocketImpl.this.read_native(myFd);
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
                if (myFd == null) {
                    throw new IOException("socket closed");
                }
                LocalSocketImpl.this.write_native(b, myFd);
            }
        }

        public void flush() throws IOException {
            FileDescriptor myFd = LocalSocketImpl.this.fd;
            if (myFd == null) {
                throw new IOException("socket closed");
            }
            MutableInt pending = new MutableInt(0);
            while (true) {
                try {
                    Os.ioctlInt(myFd, OsConstants.TIOCOUTQ, pending);
                    if (pending.value > 0) {
                        int left = pending.value;
                        if (left <= 1000) {
                            try {
                                Thread.sleep(0, 10);
                            } catch (InterruptedException e) {
                                return;
                            }
                        } else if (left <= BluetoothInputDevice.INPUT_DISCONNECT_FAILED_NOT_CONNECTED) {
                            Thread.sleep(0, RunningAppProcessInfo.IMPORTANCE_EMPTY);
                        } else {
                            Thread.sleep(1);
                        }
                    } else {
                        return;
                    }
                } catch (ErrnoException e2) {
                    throw e2.rethrowAsIOException();
                }
            }
        }
    }

    private native void bindLocal(FileDescriptor fileDescriptor, String str, int i) throws IOException;

    private native void connectLocal(FileDescriptor fileDescriptor, String str, int i) throws IOException;

    private native Credentials getPeerCredentials_native(FileDescriptor fileDescriptor) throws IOException;

    private native int read_native(FileDescriptor fileDescriptor) throws IOException;

    private native int readba_native(byte[] bArr, int i, int i2, FileDescriptor fileDescriptor) throws IOException;

    private native void write_native(int i, FileDescriptor fileDescriptor) throws IOException;

    private native void writeba_native(byte[] bArr, int i, int i2, FileDescriptor fileDescriptor) throws IOException;

    LocalSocketImpl() {
    }

    LocalSocketImpl(FileDescriptor fd) {
        this.fd = fd;
    }

    public String toString() {
        return super.toString() + " fd:" + this.fd;
    }

    public void create(int sockType) throws IOException {
        if (this.fd != null) {
            throw new IOException("LocalSocketImpl already has an fd");
        }
        int osType;
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
            return;
        }
    }

    protected void connect(LocalSocketAddress address, int timeout) throws IOException {
        if (this.fd == null) {
            throw new IOException("socket not created");
        }
        connectLocal(this.fd, address.getName(), address.getNamespace().getId());
    }

    public void bind(LocalSocketAddress endpoint) throws IOException {
        if (this.fd == null) {
            throw new IOException("socket not created");
        }
        bindLocal(this.fd, endpoint.getName(), endpoint.getNamespace().getId());
    }

    protected void listen(int backlog) throws IOException {
        if (this.fd == null) {
            throw new IOException("socket not created");
        }
        try {
            Os.listen(this.fd, backlog);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    protected void accept(LocalSocketImpl s) throws IOException {
        if (this.fd == null) {
            throw new IOException("socket not created");
        }
        try {
            s.fd = Os.accept(this.fd, null);
            s.mFdCreatedInternally = true;
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    protected InputStream getInputStream() throws IOException {
        if (this.fd == null) {
            throw new IOException("socket not created");
        }
        InputStream inputStream;
        synchronized (this) {
            if (this.fis == null) {
                this.fis = new SocketInputStream();
            }
            inputStream = this.fis;
        }
        return inputStream;
    }

    protected OutputStream getOutputStream() throws IOException {
        if (this.fd == null) {
            throw new IOException("socket not created");
        }
        OutputStream outputStream;
        synchronized (this) {
            if (this.fos == null) {
                this.fos = new SocketOutputStream();
            }
            outputStream = this.fos;
        }
        return outputStream;
    }

    protected int available() throws IOException {
        return getInputStream().available();
    }

    protected void shutdownInput() throws IOException {
        if (this.fd == null) {
            throw new IOException("socket not created");
        }
        try {
            Os.shutdown(this.fd, OsConstants.SHUT_RD);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    protected void shutdownOutput() throws IOException {
        if (this.fd == null) {
            throw new IOException("socket not created");
        }
        try {
            Os.shutdown(this.fd, OsConstants.SHUT_WR);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    protected FileDescriptor getFileDescriptor() {
        return this.fd;
    }

    protected boolean supportsUrgentData() {
        return false;
    }

    protected void sendUrgentData(int data) throws IOException {
        throw new RuntimeException("not impled");
    }

    public Object getOption(int optID) throws IOException {
        if (this.fd == null) {
            throw new IOException("socket not created");
        }
        switch (optID) {
            case 1:
                return Integer.valueOf(Os.getsockoptInt(this.fd, OsConstants.IPPROTO_TCP, OsConstants.TCP_NODELAY));
            case 4:
            case 4097:
            case 4098:
                return Integer.valueOf(Os.getsockoptInt(this.fd, OsConstants.SOL_SOCKET, javaSoToOsOpt(optID)));
            case 128:
                StructLinger linger = Os.getsockoptLinger(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_LINGER);
                if (linger.isOn()) {
                    return Integer.valueOf(linger.l_linger);
                }
                return Integer.valueOf(-1);
            case 4102:
                return Integer.valueOf((int) Os.getsockoptTimeval(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO).toMillis());
            default:
                try {
                    throw new IOException("Unknown option: " + optID);
                } catch (ErrnoException e) {
                    throw e.rethrowAsIOException();
                }
        }
        throw e.rethrowAsIOException();
    }

    public void setOption(int optID, Object value) throws IOException {
        if (this.fd == null) {
            throw new IOException("socket not created");
        }
        int boolValue = -1;
        int intValue = 0;
        if (value instanceof Integer) {
            intValue = ((Integer) value).intValue();
        } else if (value instanceof Boolean) {
            boolValue = ((Boolean) value).booleanValue() ? 1 : 0;
        } else {
            throw new IOException("bad value: " + value);
        }
        switch (optID) {
            case 1:
                Os.setsockoptInt(this.fd, OsConstants.IPPROTO_TCP, OsConstants.TCP_NODELAY, intValue);
                return;
            case 4:
            case 4097:
            case 4098:
                Os.setsockoptInt(this.fd, OsConstants.SOL_SOCKET, javaSoToOsOpt(optID), intValue);
                return;
            case 128:
                Os.setsockoptLinger(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_LINGER, new StructLinger(boolValue, intValue));
                return;
            case 4102:
                StructTimeval timeval = StructTimeval.fromMillis((long) intValue);
                Os.setsockoptTimeval(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, timeval);
                Os.setsockoptTimeval(this.fd, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, timeval);
                return;
            default:
                try {
                    throw new IOException("Unknown option: " + optID);
                } catch (ErrnoException e) {
                    throw e.rethrowAsIOException();
                }
        }
        throw e.rethrowAsIOException();
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

    protected void finalize() throws IOException {
        close();
    }

    private static int javaSoToOsOpt(int optID) {
        switch (optID) {
            case 4:
                return OsConstants.SO_REUSEADDR;
            case 4097:
                return OsConstants.SO_SNDBUF;
            case 4098:
                return OsConstants.SO_RCVBUF;
            default:
                throw new UnsupportedOperationException("Unknown option: " + optID);
        }
    }
}
