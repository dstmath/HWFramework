package android.system;

import java.net.SocketAddress;

public final class PacketSocketAddress extends SocketAddress {
    public byte[] sll_addr;
    public short sll_hatype;
    public int sll_ifindex;
    public byte sll_pkttype;
    public short sll_protocol;

    public PacketSocketAddress(short sll_protocol, int sll_ifindex, short sll_hatype, byte sll_pkttype, byte[] sll_addr) {
        this.sll_protocol = sll_protocol;
        this.sll_ifindex = sll_ifindex;
        this.sll_hatype = sll_hatype;
        this.sll_pkttype = sll_pkttype;
        this.sll_addr = sll_addr;
    }

    public PacketSocketAddress(short sll_protocol, int sll_ifindex) {
        this(sll_protocol, sll_ifindex, (short) 0, (byte) 0, null);
    }

    public PacketSocketAddress(int sll_ifindex, byte[] sll_addr) {
        this((short) 0, sll_ifindex, (short) 0, (byte) 0, sll_addr);
    }
}
