package android.net.rtp;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;

public class RtpStream {
    private static final int MODE_LAST = 2;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_RECEIVE_ONLY = 2;
    public static final int MODE_SEND_ONLY = 1;
    private final InetAddress mLocalAddress;
    private final int mLocalPort;
    private int mMode = 0;
    private InetAddress mRemoteAddress;
    private int mRemotePort = -1;
    private int mSocket = -1;

    private native void close();

    private native int create(String str) throws SocketException;

    static {
        System.loadLibrary("rtp_jni");
    }

    RtpStream(InetAddress address) throws SocketException {
        this.mLocalPort = create(address.getHostAddress());
        this.mLocalAddress = address;
    }

    public InetAddress getLocalAddress() {
        return this.mLocalAddress;
    }

    public int getLocalPort() {
        return this.mLocalPort;
    }

    public InetAddress getRemoteAddress() {
        return this.mRemoteAddress;
    }

    public int getRemotePort() {
        return this.mRemotePort;
    }

    public boolean isBusy() {
        return false;
    }

    public int getMode() {
        return this.mMode;
    }

    public void setMode(int mode) {
        if (isBusy()) {
            throw new IllegalStateException("Busy");
        } else if (mode < 0 || mode > 2) {
            throw new IllegalArgumentException("Invalid mode");
        } else {
            this.mMode = mode;
        }
    }

    public void associate(InetAddress address, int port) {
        if (isBusy()) {
            throw new IllegalStateException("Busy");
        } else if ((!(address instanceof Inet4Address) || !(this.mLocalAddress instanceof Inet4Address)) && (!(address instanceof Inet6Address) || !(this.mLocalAddress instanceof Inet6Address))) {
            throw new IllegalArgumentException("Unsupported address");
        } else if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        } else {
            this.mRemoteAddress = address;
            this.mRemotePort = port;
        }
    }

    /* access modifiers changed from: package-private */
    public int getSocket() {
        return this.mSocket;
    }

    public void release() {
        synchronized (this) {
            if (!isBusy()) {
                close();
            } else {
                throw new IllegalStateException("Busy");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
