package android.net.netlink;

import android.system.ErrnoException;
import android.system.NetlinkSocketAddress;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import android.util.Log;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import libcore.io.IoUtils;
import libcore.io.Libcore;

public class NetlinkSocket implements Closeable {
    private static final int DEFAULT_RECV_BUFSIZE = 8192;
    private static final int SOCKET_RECV_BUFSIZE = 65536;
    private static final String TAG = "NetlinkSocket";
    private NetlinkSocketAddress mAddr;
    private final FileDescriptor mDescriptor;
    private long mLastRecvTimeoutMs;
    private long mLastSendTimeoutMs;

    public NetlinkSocket(int nlProto) throws ErrnoException {
        this.mDescriptor = Os.socket(OsConstants.AF_NETLINK, OsConstants.SOCK_DGRAM, nlProto);
        Libcore.os.setsockoptInt(this.mDescriptor, OsConstants.SOL_SOCKET, OsConstants.SO_RCVBUF, 65536);
    }

    public NetlinkSocketAddress getLocalAddress() throws ErrnoException {
        return (NetlinkSocketAddress) Os.getsockname(this.mDescriptor);
    }

    public void bind(NetlinkSocketAddress localAddr) throws ErrnoException, SocketException {
        Os.bind(this.mDescriptor, localAddr);
    }

    public void connectTo(NetlinkSocketAddress peerAddr) throws ErrnoException, SocketException {
        Os.connect(this.mDescriptor, peerAddr);
    }

    public void connectToKernel() throws ErrnoException, SocketException {
        connectTo(new NetlinkSocketAddress(0, 0));
    }

    public ByteBuffer recvMessage() throws ErrnoException, InterruptedIOException {
        return recvMessage(8192, 0);
    }

    public ByteBuffer recvMessage(long timeoutMs) throws ErrnoException, InterruptedIOException {
        return recvMessage(8192, timeoutMs);
    }

    private void checkTimeout(long timeoutMs) {
        if (timeoutMs < 0) {
            throw new IllegalArgumentException("Negative timeouts not permitted");
        }
    }

    public ByteBuffer recvMessage(int bufsize, long timeoutMs) throws ErrnoException, IllegalArgumentException, InterruptedIOException {
        checkTimeout(timeoutMs);
        synchronized (this.mDescriptor) {
            if (this.mLastRecvTimeoutMs != timeoutMs) {
                Os.setsockoptTimeval(this.mDescriptor, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, StructTimeval.fromMillis(timeoutMs));
                this.mLastRecvTimeoutMs = timeoutMs;
            }
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufsize);
        int length = Os.read(this.mDescriptor, byteBuffer);
        if (length == bufsize) {
            Log.w(TAG, "maximum read");
        }
        byteBuffer.position(0);
        byteBuffer.limit(length);
        byteBuffer.order(ByteOrder.nativeOrder());
        return byteBuffer;
    }

    public boolean sendMessage(byte[] bytes, int offset, int count) throws ErrnoException, InterruptedIOException {
        return sendMessage(bytes, offset, count, 0);
    }

    public boolean sendMessage(byte[] bytes, int offset, int count, long timeoutMs) throws ErrnoException, IllegalArgumentException, InterruptedIOException {
        checkTimeout(timeoutMs);
        synchronized (this.mDescriptor) {
            if (this.mLastSendTimeoutMs != timeoutMs) {
                Os.setsockoptTimeval(this.mDescriptor, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(timeoutMs));
                this.mLastSendTimeoutMs = timeoutMs;
            }
        }
        if (count == Os.write(this.mDescriptor, bytes, offset, count)) {
            return true;
        }
        return false;
    }

    public void close() {
        IoUtils.closeQuietly(this.mDescriptor);
    }
}
