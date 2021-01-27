package android.net.netlink;

import android.net.util.SocketUtils;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NetlinkSocket {
    public static final int DEFAULT_RECV_BUFSIZE = 8192;
    public static final int SOCKET_RECV_BUFSIZE = 65536;
    private static final String TAG = "NetlinkSocket";

    public static void sendOneShotKernelMessage(int nlProto, byte[] msg) throws ErrnoException {
        String errmsg;
        FileDescriptor fd = forProto(nlProto);
        try {
            connectToKernel(fd);
            sendMessage(fd, msg, 0, msg.length, 300);
            ByteBuffer bytes = recvMessage(fd, 8192, 300);
            NetlinkMessage response = NetlinkMessage.parse(bytes);
            if (response == null || !(response instanceof NetlinkErrorMessage) || ((NetlinkErrorMessage) response).getNlMsgError() == null) {
                if (response == null) {
                    bytes.position(0);
                    errmsg = "raw bytes: " + NetlinkConstants.hexify(bytes);
                } else {
                    errmsg = response.toString();
                }
                Log.e(TAG, "Error in NetlinkSocket.sendOneShotKernelMessage, errmsg=" + errmsg);
                throw new ErrnoException(errmsg, OsConstants.EPROTO);
            }
            int errno = ((NetlinkErrorMessage) response).getNlMsgError().error;
            if (errno == 0) {
                try {
                    SocketUtils.closeSocket(fd);
                } catch (IOException e) {
                }
            } else {
                Log.e(TAG, "Error in NetlinkSocket.sendOneShotKernelMessage, errmsg=" + response.toString());
                throw new ErrnoException(response.toString(), Math.abs(errno));
            }
        } catch (InterruptedIOException e2) {
            Log.e(TAG, "Error in NetlinkSocket.sendOneShotKernelMessage", e2);
            throw new ErrnoException("Error in NetlinkSocket.sendOneShotKernelMessage", OsConstants.ETIMEDOUT, e2);
        } catch (SocketException e3) {
            Log.e(TAG, "Error in NetlinkSocket.sendOneShotKernelMessage", e3);
            throw new ErrnoException("Error in NetlinkSocket.sendOneShotKernelMessage", OsConstants.EIO, e3);
        } catch (Throwable th) {
            try {
                SocketUtils.closeSocket(fd);
            } catch (IOException e4) {
            }
            throw th;
        }
    }

    public static FileDescriptor forProto(int nlProto) throws ErrnoException {
        FileDescriptor fd = Os.socket(OsConstants.AF_NETLINK, OsConstants.SOCK_DGRAM, nlProto);
        Os.setsockoptInt(fd, OsConstants.SOL_SOCKET, OsConstants.SO_RCVBUF, 65536);
        return fd;
    }

    public static void connectToKernel(FileDescriptor fd) throws ErrnoException, SocketException {
        Os.connect(fd, SocketUtils.makeNetlinkSocketAddress(0, 0));
    }

    private static void checkTimeout(long timeoutMs) {
        if (timeoutMs < 0) {
            throw new IllegalArgumentException("Negative timeouts not permitted");
        }
    }

    public static ByteBuffer recvMessage(FileDescriptor fd, int bufsize, long timeoutMs) throws ErrnoException, IllegalArgumentException, InterruptedIOException {
        checkTimeout(timeoutMs);
        Os.setsockoptTimeval(fd, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, StructTimeval.fromMillis(timeoutMs));
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufsize);
        int length = Os.read(fd, byteBuffer);
        if (length == bufsize) {
            Log.w(TAG, "maximum read");
        }
        byteBuffer.position(0);
        byteBuffer.limit(length);
        byteBuffer.order(ByteOrder.nativeOrder());
        return byteBuffer;
    }

    public static int sendMessage(FileDescriptor fd, byte[] bytes, int offset, int count, long timeoutMs) throws ErrnoException, IllegalArgumentException, InterruptedIOException {
        checkTimeout(timeoutMs);
        Os.setsockoptTimeval(fd, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(timeoutMs));
        return Os.write(fd, bytes, offset, count);
    }
}
