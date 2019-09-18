package java.net;

import android.system.ErrnoException;
import android.system.OsConstants;
import java.io.FileDescriptor;
import java.io.IOException;
import jdk.net.ExtendedSocketOptions;
import jdk.net.SocketFlow;
import libcore.io.AsynchronousCloseMonitor;
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import libcore.io.Libcore;
import sun.net.ExtendedOptionsImpl;

class PlainSocketImpl extends AbstractPlainSocketImpl {
    PlainSocketImpl() {
        this(new FileDescriptor());
    }

    PlainSocketImpl(FileDescriptor fd) {
        this.fd = fd;
    }

    /* access modifiers changed from: protected */
    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (!name.equals(ExtendedSocketOptions.SO_FLOW_SLA)) {
            super.setOption(name, value);
        } else if (!isClosedOrPending()) {
            ExtendedOptionsImpl.checkSetOptionPermission(name);
            ExtendedOptionsImpl.checkValueType(value, SocketFlow.class);
            ExtendedOptionsImpl.setFlowOption(getFileDescriptor(), (SocketFlow) value);
        } else {
            throw new SocketException("Socket closed");
        }
    }

    /* access modifiers changed from: protected */
    public <T> T getOption(SocketOption<T> name) throws IOException {
        if (!name.equals(ExtendedSocketOptions.SO_FLOW_SLA)) {
            return super.getOption(name);
        }
        if (!isClosedOrPending()) {
            ExtendedOptionsImpl.checkGetOptionPermission(name);
            SocketFlow flow = SocketFlow.create();
            ExtendedOptionsImpl.getFlowOption(getFileDescriptor(), flow);
            return flow;
        }
        throw new SocketException("Socket closed");
    }

    /* access modifiers changed from: protected */
    public void socketSetOption(int opt, Object val) throws SocketException {
        try {
            socketSetOption0(opt, val);
        } catch (SocketException se) {
            if (this.socket == null || !this.socket.isConnected()) {
                throw se;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void socketCreate(boolean isStream) throws IOException {
        this.fd.setInt$(IoBridge.socket(OsConstants.AF_INET6, isStream ? OsConstants.SOCK_STREAM : OsConstants.SOCK_DGRAM, 0).getInt$());
        if (this.serverSocket != null) {
            IoUtils.setBlocking(this.fd, false);
            IoBridge.setSocketOption(this.fd, 4, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void socketConnect(InetAddress address, int port, int timeout) throws IOException {
        if (this.fd == null || !this.fd.valid()) {
            throw new SocketException("Socket closed");
        }
        IoBridge.connect(this.fd, address, port, timeout);
        this.address = address;
        this.port = port;
        if (this.localport == 0 && !isClosedOrPending()) {
            this.localport = IoBridge.getLocalInetSocketAddress(this.fd).getPort();
        }
    }

    /* access modifiers changed from: package-private */
    public void socketBind(InetAddress address, int port) throws IOException {
        if (this.fd == null || !this.fd.valid()) {
            throw new SocketException("Socket closed");
        }
        IoBridge.bind(this.fd, address, port);
        this.address = address;
        if (port == 0) {
            this.localport = IoBridge.getLocalInetSocketAddress(this.fd).getPort();
        } else {
            this.localport = port;
        }
    }

    /* access modifiers changed from: package-private */
    public void socketListen(int count) throws IOException {
        if (this.fd == null || !this.fd.valid()) {
            throw new SocketException("Socket closed");
        }
        try {
            Libcore.os.listen(this.fd, count);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }

    /* access modifiers changed from: package-private */
    public void socketAccept(SocketImpl s) throws IOException {
        if (this.fd == null || !this.fd.valid()) {
            throw new SocketException("Socket closed");
        }
        if (this.timeout <= 0) {
            IoBridge.poll(this.fd, OsConstants.POLLIN | OsConstants.POLLERR, -1);
        } else {
            IoBridge.poll(this.fd, OsConstants.POLLIN | OsConstants.POLLERR, this.timeout);
        }
        InetSocketAddress peerAddress = new InetSocketAddress();
        try {
            s.fd.setInt$(Libcore.os.accept(this.fd, peerAddress).getInt$());
            s.address = peerAddress.getAddress();
            s.port = peerAddress.getPort();
        } catch (ErrnoException errnoException) {
            if (errnoException.errno == OsConstants.EAGAIN) {
                throw new SocketTimeoutException((Throwable) errnoException);
            } else if (errnoException.errno == OsConstants.EINVAL || errnoException.errno == OsConstants.EBADF) {
                throw new SocketException("Socket closed");
            } else {
                errnoException.rethrowAsSocketException();
            }
        }
        s.localport = IoBridge.getLocalInetSocketAddress(s.fd).getPort();
    }

    /* access modifiers changed from: package-private */
    public int socketAvailable() throws IOException {
        return IoBridge.available(this.fd);
    }

    /* access modifiers changed from: package-private */
    public void socketClose0(boolean useDeferredClose) throws IOException {
        if (this.fd == null || !this.fd.valid()) {
            throw new SocketException("socket already closed");
        }
        FileDescriptor markerFD = null;
        if (useDeferredClose) {
            markerFD = getMarkerFD();
        }
        if (!useDeferredClose || markerFD == null) {
            IoBridge.closeAndSignalBlockedThreads(this.fd);
            return;
        }
        try {
            Libcore.os.dup2(markerFD, this.fd.getInt$());
            Libcore.os.close(markerFD);
            AsynchronousCloseMonitor.signalBlockedThreads(this.fd);
        } catch (ErrnoException e) {
        }
    }

    private FileDescriptor getMarkerFD() throws SocketException {
        FileDescriptor fd1 = new FileDescriptor();
        FileDescriptor fd2 = new FileDescriptor();
        try {
            Libcore.os.socketpair(OsConstants.AF_UNIX, OsConstants.SOCK_STREAM, 0, fd1, fd2);
            Libcore.os.shutdown(fd1, OsConstants.SHUT_RDWR);
            Libcore.os.close(fd2);
            return fd1;
        } catch (ErrnoException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void socketShutdown(int howto) throws IOException {
        try {
            Libcore.os.shutdown(this.fd, howto);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    /* access modifiers changed from: package-private */
    public void socketSetOption0(int cmd, Object value) throws SocketException {
        if (cmd != 4102) {
            IoBridge.setSocketOption(this.fd, cmd, value);
        }
    }

    /* access modifiers changed from: package-private */
    public Object socketGetOption(int opt) throws SocketException {
        return IoBridge.getSocketOption(this.fd, opt);
    }

    /* access modifiers changed from: package-private */
    public void socketSendUrgentData(int data) throws IOException {
        if (this.fd == null || !this.fd.valid()) {
            throw new SocketException("Socket closed");
        }
        try {
            Libcore.os.sendto(this.fd, new byte[]{(byte) data}, 0, 1, OsConstants.MSG_OOB, null, 0);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }
}
