package android.system;

import java.net.SocketAddress;

public final class PacketSocketAddress extends SocketAddress {
    public byte[] sll_addr;
    public short sll_hatype;
    public int sll_ifindex;
    public byte sll_pkttype;
    public short sll_protocol;

    public PacketSocketAddress(short sll_protocol2, int sll_ifindex2, short sll_hatype2, byte sll_pkttype2, byte[] sll_addr2) {
        this.sll_protocol = sll_protocol2;
        this.sll_ifindex = sll_ifindex2;
        this.sll_hatype = sll_hatype2;
        this.sll_pkttype = sll_pkttype2;
        this.sll_addr = sll_addr2;
    }

    public PacketSocketAddress(short sll_protocol2, int sll_ifindex2) {
        this(sll_protocol2, sll_ifindex2, 0, (byte) 0, null);
    }

    public PacketSocketAddress(int sll_ifindex2, byte[] sll_addr2) {
        this(0, sll_ifindex2, 0, (byte) 0, sll_addr2);
    }
}
