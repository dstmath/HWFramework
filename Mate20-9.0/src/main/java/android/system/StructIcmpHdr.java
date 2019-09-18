package android.system;

public final class StructIcmpHdr {
    private byte[] packet = new byte[8];

    private StructIcmpHdr() {
    }

    public static StructIcmpHdr IcmpEchoHdr(boolean ipv4, int seq) {
        StructIcmpHdr hdr = new StructIcmpHdr();
        hdr.packet[0] = (byte) (ipv4 ? OsConstants.ICMP_ECHO : OsConstants.ICMP6_ECHO_REQUEST);
        hdr.packet[6] = (byte) (seq >> 8);
        hdr.packet[7] = (byte) seq;
        return hdr;
    }

    public byte[] getBytes() {
        return (byte[]) this.packet.clone();
    }
}
