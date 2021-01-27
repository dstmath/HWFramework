package android.net.netlink;

import android.net.util.SocketUtils;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class InetDiagMessage extends NetlinkMessage {
    private static final int[] FAMILY = {OsConstants.AF_INET6, OsConstants.AF_INET};
    public static final String TAG = "InetDiagMessage";
    private static final int TIMEOUT_MS = 500;
    public StructInetDiagMsg mStructInetDiagMsg = new StructInetDiagMsg();

    public static byte[] InetDiagReqV2(int protocol, InetSocketAddress local, InetSocketAddress remote, int family, short flags) {
        byte[] bytes = new byte[72];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        StructNlMsgHdr nlMsgHdr = new StructNlMsgHdr();
        nlMsgHdr.nlmsg_len = bytes.length;
        nlMsgHdr.nlmsg_type = 20;
        nlMsgHdr.nlmsg_flags = flags;
        nlMsgHdr.pack(byteBuffer);
        new StructInetDiagReqV2(protocol, local, remote, family).pack(byteBuffer);
        return bytes;
    }

    private InetDiagMessage(StructNlMsgHdr header) {
        super(header);
    }

    public static InetDiagMessage parse(StructNlMsgHdr header, ByteBuffer byteBuffer) {
        InetDiagMessage msg = new InetDiagMessage(header);
        msg.mStructInetDiagMsg = StructInetDiagMsg.parse(byteBuffer);
        return msg;
    }

    private static int lookupUidByFamily(int protocol, InetSocketAddress local, InetSocketAddress remote, int family, short flags, FileDescriptor fd) throws ErrnoException, InterruptedIOException {
        byte[] msg = InetDiagReqV2(protocol, local, remote, family, flags);
        NetlinkSocket.sendMessage(fd, msg, 0, msg.length, 500);
        NetlinkMessage nlMsg = NetlinkMessage.parse(NetlinkSocket.recvMessage(fd, 8192, 500));
        if (nlMsg.getHeader().nlmsg_type != 3 && (nlMsg instanceof InetDiagMessage)) {
            return ((InetDiagMessage) nlMsg).mStructInetDiagMsg.idiag_uid;
        }
        return -1;
    }

    private static int lookupUid(int protocol, InetSocketAddress local, InetSocketAddress remote, FileDescriptor fd) throws ErrnoException, InterruptedIOException {
        int uid;
        int[] iArr = FAMILY;
        for (int family : iArr) {
            if (protocol == OsConstants.IPPROTO_UDP) {
                uid = lookupUidByFamily(protocol, remote, local, family, 1, fd);
            } else {
                uid = lookupUidByFamily(protocol, local, remote, family, 1, fd);
            }
            if (uid != -1) {
                return uid;
            }
        }
        if (protocol == OsConstants.IPPROTO_UDP) {
            try {
                int uid2 = lookupUidByFamily(protocol, local, new InetSocketAddress(Inet6Address.getByName("::"), 0), OsConstants.AF_INET6, 769, fd);
                if (uid2 != -1) {
                    return uid2;
                }
                int uid3 = lookupUidByFamily(protocol, local, new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), 0), OsConstants.AF_INET, 769, fd);
                if (uid3 != -1) {
                    return uid3;
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, e.toString());
            }
        }
        return -1;
    }

    public static int getConnectionOwnerUid(int protocol, InetSocketAddress local, InetSocketAddress remote) throws ErrnoException {
        FileDescriptor fd = NetlinkSocket.forProto(OsConstants.NETLINK_INET_DIAG);
        try {
            NetlinkSocket.connectToKernel(fd);
            int lookupUid = lookupUid(protocol, local, remote, fd);
            if (fd != null) {
                try {
                    SocketUtils.closeSocket(fd);
                } catch (IOException e) {
                    Log.e(TAG, "close socket catch IOException.");
                }
            }
            return lookupUid;
        } catch (InterruptedIOException | IllegalArgumentException | SocketException e2) {
            Log.e(TAG, e2.toString());
            if (fd == null) {
                return -1;
            }
            try {
                SocketUtils.closeSocket(fd);
                return -1;
            } catch (IOException e3) {
                Log.e(TAG, "close socket catch IOException.");
                return -1;
            }
        } catch (Throwable th) {
            if (fd != null) {
                try {
                    SocketUtils.closeSocket(fd);
                } catch (IOException e4) {
                    Log.e(TAG, "close socket catch IOException.");
                }
            }
            throw th;
        }
    }

    @Override // android.net.netlink.NetlinkMessage
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InetDiagMessage{ nlmsghdr{");
        String str = "";
        sb.append(this.mHeader == null ? str : this.mHeader.toString());
        sb.append("}, inet_diag_msg{");
        StructInetDiagMsg structInetDiagMsg = this.mStructInetDiagMsg;
        if (structInetDiagMsg != null) {
            str = structInetDiagMsg.toString();
        }
        sb.append(str);
        sb.append("} }");
        return sb.toString();
    }
}
