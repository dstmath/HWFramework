package android.system;

import java.net.InetAddress;

public final class StructIfaddrs {
    public final byte[] hwaddr;
    public final InetAddress ifa_addr;
    public final InetAddress ifa_broadaddr;
    public final int ifa_flags;
    public final String ifa_name;
    public final InetAddress ifa_netmask;

    public StructIfaddrs(String ifa_name2, int ifa_flags2, InetAddress ifa_addr2, InetAddress ifa_netmask2, InetAddress ifa_broadaddr2, byte[] hwaddr2) {
        this.ifa_name = ifa_name2;
        this.ifa_flags = ifa_flags2;
        this.ifa_addr = ifa_addr2;
        this.ifa_netmask = ifa_netmask2;
        this.ifa_broadaddr = ifa_broadaddr2;
        this.hwaddr = hwaddr2;
    }
}
