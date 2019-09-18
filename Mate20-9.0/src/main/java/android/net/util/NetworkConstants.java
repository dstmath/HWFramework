package android.net.util;

public final class NetworkConstants {
    public static final int ARP_HWTYPE_ETHER = 1;
    public static final int ARP_HWTYPE_RESERVED_HI = 65535;
    public static final int ARP_HWTYPE_RESERVED_LO = 0;
    public static final int ARP_PAYLOAD_LEN = 28;
    public static final int ARP_REPLY = 2;
    public static final int ARP_REQUEST = 1;
    public static final int DHCP4_CLIENT_PORT = 68;
    public static final int DHCP4_SERVER_PORT = 67;
    public static final int DNS_SERVER_PORT = 53;
    public static final byte[] ETHER_ADDR_BROADCAST = {FF, FF, FF, FF, FF, FF};
    public static final int ETHER_ADDR_LEN = 6;
    public static final int ETHER_DST_ADDR_OFFSET = 0;
    public static final int ETHER_HEADER_LEN = 14;
    public static final int ETHER_MTU = 1500;
    public static final int ETHER_SRC_ADDR_OFFSET = 6;
    public static final int ETHER_TYPE_ARP = 2054;
    public static final int ETHER_TYPE_IPV4 = 2048;
    public static final int ETHER_TYPE_IPV6 = 34525;
    public static final int ETHER_TYPE_LENGTH = 2;
    public static final int ETHER_TYPE_OFFSET = 12;
    public static final byte FF = asByte(255);
    public static final int ICMPV4_ECHO_REQUEST_TYPE = 8;
    public static final int ICMPV6_ECHO_REPLY_TYPE = 129;
    public static final int ICMPV6_ECHO_REQUEST_TYPE = 128;
    public static final int ICMPV6_HEADER_MIN_LEN = 4;
    public static final int ICMPV6_ND_OPTION_LENGTH_SCALING_FACTOR = 8;
    public static final int ICMPV6_ND_OPTION_MIN_LENGTH = 8;
    public static final int ICMPV6_ND_OPTION_MTU = 5;
    public static final int ICMPV6_ND_OPTION_SLLA = 1;
    public static final int ICMPV6_ND_OPTION_TLLA = 2;
    public static final int ICMPV6_NEIGHBOR_ADVERTISEMENT = 136;
    public static final int ICMPV6_NEIGHBOR_SOLICITATION = 135;
    public static final int ICMPV6_ROUTER_ADVERTISEMENT = 134;
    public static final int ICMPV6_ROUTER_SOLICITATION = 133;
    public static final int ICMP_ECHO_DATA_OFFSET = 8;
    public static final int ICMP_ECHO_IDENTIFIER_OFFSET = 4;
    public static final int ICMP_ECHO_SEQUENCE_NUMBER_OFFSET = 6;
    public static final int ICMP_HEADER_CHECKSUM_OFFSET = 2;
    public static final int ICMP_HEADER_CODE_OFFSET = 1;
    public static final int ICMP_HEADER_TYPE_OFFSET = 0;
    public static final int IPV4_ADDR_BITS = 32;
    public static final int IPV4_ADDR_LEN = 4;
    public static final int IPV4_DST_ADDR_OFFSET = 16;
    public static final int IPV4_FLAGS_OFFSET = 6;
    public static final int IPV4_FRAGMENT_MASK = 8191;
    public static final int IPV4_HEADER_MIN_LEN = 20;
    public static final int IPV4_IHL_MASK = 15;
    public static final int IPV4_PROTOCOL_OFFSET = 9;
    public static final int IPV4_SRC_ADDR_OFFSET = 12;
    public static final int IPV6_ADDR_BITS = 128;
    public static final int IPV6_ADDR_LEN = 16;
    public static final int IPV6_DST_ADDR_OFFSET = 24;
    public static final int IPV6_HEADER_LEN = 40;
    public static final int IPV6_MIN_MTU = 1280;
    public static final int IPV6_PROTOCOL_OFFSET = 6;
    public static final int IPV6_SRC_ADDR_OFFSET = 8;
    public static final int RFC6177_MIN_PREFIX_LENGTH = 48;
    public static final int RFC7421_PREFIX_LENGTH = 64;
    public static final int UDP_HEADER_LEN = 8;

    private NetworkConstants() {
        throw new RuntimeException("no instance permitted");
    }

    public static byte asByte(int i) {
        return (byte) i;
    }

    public static String asString(int i) {
        return Integer.toString(i);
    }

    public static int asUint(byte b) {
        return b & 255;
    }

    public static int asUint(short s) {
        return 65535 & s;
    }
}
