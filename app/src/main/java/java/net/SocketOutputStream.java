package java.net;

import dalvik.system.BlockGuard;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import sun.misc.IoTrace;
import sun.net.ConnectionResetException;

class SocketOutputStream extends FileOutputStream {
    private boolean closing;
    private AbstractPlainSocketImpl impl;
    private Socket socket;
    private byte[] temp;

    private native void socketWrite0(FileDescriptor fileDescriptor, byte[] bArr, int i, int i2) throws IOException;

    SocketOutputStream(AbstractPlainSocketImpl impl) throws IOException {
        super(impl.getFileDescriptor());
        this.impl = null;
        this.temp = new byte[1];
        this.socket = null;
        this.closing = false;
        this.impl = impl;
        this.socket = impl.getSocket();
    }

    public final FileChannel getChannel() {
        return null;
    }

    private void socketWrite(byte[] b, int off, int len) throws IOException {
        if (len > 0 && off >= 0 && off + len <= b.length) {
            Object traceContext = IoTrace.socketWriteBegin();
            FileDescriptor fd = this.impl.acquireFD();
            try {
                BlockGuard.getThreadPolicy().onNetwork();
                socketWrite0(fd, b, off, len);
                int bytesWritten = len;
                IoTrace.socketWriteEnd(traceContext, this.impl.address, this.impl.port, (long) len);
            } catch (SocketException e) {
                SocketException se = e;
                if (se instanceof ConnectionResetException) {
                    this.impl.setConnectionResetPending();
                    se = new SocketException("Connection reset");
                }
                if (this.impl.isClosedOrPending()) {
                    throw new SocketException("Socket closed");
                }
                throw se;
            } catch (Throwable th) {
                IoTrace.socketWriteEnd(traceContext, this.impl.address, this.impl.port, 0);
            }
        } else if (len != 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void write(int b) throws IOException {
        this.temp[0] = (byte) b;
        socketWrite(this.temp, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        socketWrite(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        socketWrite(b, off, len);
    }

    public void close() throws IOException {
        if (!this.closing) {
            this.closing = true;
            if (this.socket == null) {
                this.impl.close();
            } else if (!this.socket.isClosed()) {
                this.socket.close();
            }
            this.closing = false;
        }
    }

    protected void finalize() {
    }
}
