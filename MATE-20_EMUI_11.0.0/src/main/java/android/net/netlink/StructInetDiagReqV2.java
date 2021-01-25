package android.net.netlink;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class StructInetDiagReqV2 {
    public static final int STRUCT_SIZE = 56;
    private final int INET_DIAG_REQ_V2_ALL_STATES = -1;
    private final StructInetDiagSockId id;
    private final byte sdiag_family;
    private final byte sdiag_protocol;

    public StructInetDiagReqV2(int protocol, InetSocketAddress local, InetSocketAddress remote, int family) {
        this.sdiag_family = (byte) family;
        this.sdiag_protocol = (byte) protocol;
        this.id = new StructInetDiagSockId(local, remote);
    }

    public void pack(ByteBuffer byteBuffer) {
        byteBuffer.put(this.sdiag_family);
        byteBuffer.put(this.sdiag_protocol);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.putInt(-1);
        this.id.pack(byteBuffer);
    }

    public String toString() {
        String familyStr = NetlinkConstants.stringForAddressFamily(this.sdiag_family);
        String protocolStr = NetlinkConstants.stringForAddressFamily(this.sdiag_protocol);
        return "StructInetDiagReqV2{ sdiag_family{" + familyStr + "}, sdiag_protocol{" + protocolStr + "}, idiag_ext{0)}, pad{0}, idiag_states{" + Integer.toHexString(-1) + "}, " + this.id.toString() + "}";
    }
}
