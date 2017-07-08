package java.net;

import java.io.FileDescriptor;
import java.io.IOException;

class PlainSocketImpl extends AbstractPlainSocketImpl {
    native void socketAccept(SocketImpl socketImpl) throws IOException;

    native int socketAvailable() throws IOException;

    native void socketBind(InetAddress inetAddress, int i) throws IOException;

    native void socketClose0() throws IOException;

    native void socketConnect(InetAddress inetAddress, int i, int i2) throws IOException;

    native void socketCreate(boolean z) throws IOException;

    native int socketGetOption(int i, Object obj) throws SocketException;

    native void socketListen(int i) throws IOException;

    native void socketSendUrgentData(int i) throws IOException;

    native void socketSetOption(int i, boolean z, Object obj) throws SocketException;

    native void socketShutdown(int i) throws IOException;

    PlainSocketImpl() {
        this(new FileDescriptor());
    }

    PlainSocketImpl(FileDescriptor fd) {
        this.fd = fd;
    }
}
