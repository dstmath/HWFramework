package android.net.netlink;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StructInetDiagSockId {
    public static final int STRUCT_SIZE = 48;
    private final byte[] INET_DIAG_NOCOOKIE = {-1, -1, -1, -1, -1, -1, -1, -1};
    private final byte[] IPV4_PADDING = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private final InetSocketAddress mLocSocketAddress;
    private final InetSocketAddress mRemSocketAddress;

    public StructInetDiagSockId(InetSocketAddress loc, InetSocketAddress rem) {
        this.mLocSocketAddress = loc;
        this.mRemSocketAddress = rem;
    }

    public void pack(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putShort((short) this.mLocSocketAddress.getPort());
        byteBuffer.putShort((short) this.mRemSocketAddress.getPort());
        byteBuffer.put(this.mLocSocketAddress.getAddress().getAddress());
        if (this.mLocSocketAddress.getAddress() instanceof Inet4Address) {
            byteBuffer.put(this.IPV4_PADDING);
        }
        byteBuffer.put(this.mRemSocketAddress.getAddress().getAddress());
        if (this.mRemSocketAddress.getAddress() instanceof Inet4Address) {
            byteBuffer.put(this.IPV4_PADDING);
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.putInt(0);
        byteBuffer.put(this.INET_DIAG_NOCOOKIE);
    }

    public String toString() {
        return "StructInetDiagSockId{ idiag_sport{" + this.mLocSocketAddress.getPort() + "}, idiag_dport{" + this.mRemSocketAddress.getPort() + "}, idiag_src{" + this.mLocSocketAddress.getAddress().getHostAddress() + "}, idiag_dst{" + this.mRemSocketAddress.getAddress().getHostAddress() + "}, idiag_if{0} idiag_cookie{INET_DIAG_NOCOOKIE}}";
    }
}
