package android.net.util;

import android.net.MacAddress;
import android.net.dhcp.DhcpPacket;
import android.system.OsConstants;
import com.android.server.UiModeManagerService;
import com.android.server.usb.descriptors.UsbDescriptor;
import com.android.server.voiceinteraction.DatabaseHelper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringJoiner;

public class ConnectivityPacketSummary {
    private static final String TAG = ConnectivityPacketSummary.class.getSimpleName();
    private final byte[] mBytes;
    private final byte[] mHwAddr;
    private final int mLength;
    private final ByteBuffer mPacket = ByteBuffer.wrap(this.mBytes, 0, this.mLength);
    private final String mSummary;

    public static String summarize(MacAddress hwaddr, byte[] buffer) {
        return summarize(hwaddr, buffer, buffer.length);
    }

    public static String summarize(MacAddress macAddr, byte[] buffer, int length) {
        if (macAddr == null || buffer == null) {
            return null;
        }
        return new ConnectivityPacketSummary(macAddr, buffer, Math.min(length, buffer.length)).toString();
    }

    private ConnectivityPacketSummary(MacAddress macAddr, byte[] buffer, int length) {
        this.mHwAddr = macAddr.toByteArray();
        this.mBytes = buffer;
        this.mLength = Math.min(length, this.mBytes.length);
        this.mPacket.order(ByteOrder.BIG_ENDIAN);
        StringJoiner sj = new StringJoiner(" ");
        parseEther(sj);
        this.mSummary = sj.toString();
    }

    public String toString() {
        return this.mSummary;
    }

    private void parseEther(StringJoiner sj) {
        if (this.mPacket.remaining() < 14) {
            sj.add("runt:").add(NetworkConstants.asString(this.mPacket.remaining()));
            return;
        }
        this.mPacket.position(6);
        ByteBuffer srcMac = (ByteBuffer) this.mPacket.slice().limit(6);
        sj.add(ByteBuffer.wrap(this.mHwAddr).equals(srcMac) ? "TX" : "RX");
        sj.add(getMacAddressString(srcMac));
        this.mPacket.position(0);
        sj.add(">").add(getMacAddressString((ByteBuffer) this.mPacket.slice().limit(6)));
        this.mPacket.position(12);
        int etherType = NetworkConstants.asUint(this.mPacket.getShort());
        if (etherType == 2048) {
            sj.add("ipv4");
            parseIPv4(sj);
        } else if (etherType == 2054) {
            sj.add("arp");
            parseARP(sj);
        } else if (etherType != 34525) {
            sj.add("ethtype").add(NetworkConstants.asString(etherType));
        } else {
            sj.add("ipv6");
            parseIPv6(sj);
        }
    }

    private void parseARP(StringJoiner sj) {
        if (this.mPacket.remaining() < 28) {
            sj.add("runt:").add(NetworkConstants.asString(this.mPacket.remaining()));
        } else if (NetworkConstants.asUint(this.mPacket.getShort()) == 1 && NetworkConstants.asUint(this.mPacket.getShort()) == 2048 && NetworkConstants.asUint(this.mPacket.get()) == 6 && NetworkConstants.asUint(this.mPacket.get()) == 4) {
            int opCode = NetworkConstants.asUint(this.mPacket.getShort());
            String senderHwAddr = getMacAddressString(this.mPacket);
            String senderIPv4 = getIPv4AddressString(this.mPacket);
            getMacAddressString(this.mPacket);
            String targetIPv4 = getIPv4AddressString(this.mPacket);
            if (opCode == 1) {
                sj.add("who-has").add(targetIPv4);
            } else if (opCode == 2) {
                sj.add("reply").add(senderIPv4).add(senderHwAddr);
            } else {
                sj.add("unknown opcode").add(NetworkConstants.asString(opCode));
            }
        } else {
            sj.add("unexpected header");
        }
    }

    private void parseIPv4(StringJoiner sj) {
        if (!this.mPacket.hasRemaining()) {
            sj.add("runt");
            return;
        }
        int startOfIpLayer = this.mPacket.position();
        int ipv4HeaderLength = (this.mPacket.get(startOfIpLayer) & UsbDescriptor.DESCRIPTORTYPE_BOS) * 4;
        if (this.mPacket.remaining() < ipv4HeaderLength || this.mPacket.remaining() < 20) {
            sj.add("runt:").add(NetworkConstants.asString(this.mPacket.remaining()));
            return;
        }
        int startOfTransportLayer = startOfIpLayer + ipv4HeaderLength;
        this.mPacket.position(startOfIpLayer + 6);
        boolean isFragment = (NetworkConstants.asUint(this.mPacket.getShort()) & NetworkConstants.IPV4_FRAGMENT_MASK) != 0;
        this.mPacket.position(startOfIpLayer + 9);
        int protocol = NetworkConstants.asUint(this.mPacket.get());
        this.mPacket.position(startOfIpLayer + 12);
        String srcAddr = getIPv4AddressString(this.mPacket);
        this.mPacket.position(startOfIpLayer + 16);
        sj.add(srcAddr).add(">").add(getIPv4AddressString(this.mPacket));
        this.mPacket.position(startOfTransportLayer);
        if (protocol == OsConstants.IPPROTO_UDP) {
            sj.add("udp");
            if (isFragment) {
                sj.add("fragment");
            } else {
                parseUDP(sj);
            }
        } else {
            sj.add("proto").add(NetworkConstants.asString(protocol));
            if (isFragment) {
                sj.add("fragment");
            }
        }
    }

    private void parseIPv6(StringJoiner sj) {
        if (this.mPacket.remaining() < 40) {
            sj.add("runt:").add(NetworkConstants.asString(this.mPacket.remaining()));
            return;
        }
        int startOfIpLayer = this.mPacket.position();
        this.mPacket.position(startOfIpLayer + 6);
        int protocol = NetworkConstants.asUint(this.mPacket.get());
        this.mPacket.position(startOfIpLayer + 8);
        String srcAddr = getIPv6AddressString(this.mPacket);
        sj.add(srcAddr).add(">").add(getIPv6AddressString(this.mPacket));
        this.mPacket.position(startOfIpLayer + 40);
        if (protocol == OsConstants.IPPROTO_ICMPV6) {
            sj.add("icmp6");
            parseICMPv6(sj);
        } else {
            sj.add("proto").add(NetworkConstants.asString(protocol));
        }
    }

    private void parseICMPv6(StringJoiner sj) {
        if (this.mPacket.remaining() < 4) {
            sj.add("runt:").add(NetworkConstants.asString(this.mPacket.remaining()));
            return;
        }
        int icmp6Type = NetworkConstants.asUint(this.mPacket.get());
        int icmp6Code = NetworkConstants.asUint(this.mPacket.get());
        this.mPacket.getShort();
        switch (icmp6Type) {
            case NetworkConstants.ICMPV6_ROUTER_SOLICITATION:
                sj.add("rs");
                parseICMPv6RouterSolicitation(sj);
                break;
            case NetworkConstants.ICMPV6_ROUTER_ADVERTISEMENT:
                sj.add("ra");
                parseICMPv6RouterAdvertisement(sj);
                break;
            case NetworkConstants.ICMPV6_NEIGHBOR_SOLICITATION:
                sj.add("ns");
                parseICMPv6NeighborMessage(sj);
                break;
            case NetworkConstants.ICMPV6_NEIGHBOR_ADVERTISEMENT:
                sj.add("na");
                parseICMPv6NeighborMessage(sj);
                break;
            default:
                sj.add(DatabaseHelper.SoundModelContract.KEY_TYPE).add(NetworkConstants.asString(icmp6Type));
                sj.add("code").add(NetworkConstants.asString(icmp6Code));
                break;
        }
    }

    private void parseICMPv6RouterSolicitation(StringJoiner sj) {
        if (this.mPacket.remaining() < 4) {
            sj.add("runt:").add(NetworkConstants.asString(this.mPacket.remaining()));
            return;
        }
        this.mPacket.position(this.mPacket.position() + 4);
        parseICMPv6NeighborDiscoveryOptions(sj);
    }

    private void parseICMPv6RouterAdvertisement(StringJoiner sj) {
        if (this.mPacket.remaining() < 12) {
            sj.add("runt:").add(NetworkConstants.asString(this.mPacket.remaining()));
            return;
        }
        this.mPacket.position(this.mPacket.position() + 12);
        parseICMPv6NeighborDiscoveryOptions(sj);
    }

    private void parseICMPv6NeighborMessage(StringJoiner sj) {
        if (this.mPacket.remaining() < 20) {
            sj.add("runt:").add(NetworkConstants.asString(this.mPacket.remaining()));
            return;
        }
        this.mPacket.position(this.mPacket.position() + 4);
        sj.add(getIPv6AddressString(this.mPacket));
        parseICMPv6NeighborDiscoveryOptions(sj);
    }

    private void parseICMPv6NeighborDiscoveryOptions(StringJoiner sj) {
        while (this.mPacket.remaining() >= 8) {
            int ndType = NetworkConstants.asUint(this.mPacket.get());
            int ndBytes = (NetworkConstants.asUint(this.mPacket.get()) * 8) - 2;
            if (ndBytes < 0 || ndBytes > this.mPacket.remaining()) {
                sj.add("<malformed>");
                return;
            }
            int position = this.mPacket.position();
            if (ndType != 5) {
                switch (ndType) {
                    case 1:
                        sj.add("slla");
                        sj.add(getMacAddressString(this.mPacket));
                        break;
                    case 2:
                        sj.add("tlla");
                        sj.add(getMacAddressString(this.mPacket));
                        break;
                }
            } else {
                sj.add("mtu");
                short s = this.mPacket.getShort();
                sj.add(NetworkConstants.asString(this.mPacket.getInt()));
            }
            this.mPacket.position(position + ndBytes);
        }
    }

    private void parseUDP(StringJoiner sj) {
        if (this.mPacket.remaining() < 8) {
            sj.add("runt:").add(NetworkConstants.asString(this.mPacket.remaining()));
            return;
        }
        int previous = this.mPacket.position();
        int srcPort = NetworkConstants.asUint(this.mPacket.getShort());
        int dstPort = NetworkConstants.asUint(this.mPacket.getShort());
        sj.add(NetworkConstants.asString(srcPort)).add(">").add(NetworkConstants.asString(dstPort));
        this.mPacket.position(previous + 8);
        if (srcPort == 68 || dstPort == 68) {
            sj.add("dhcp4");
            parseDHCPv4(sj);
        }
    }

    private void parseDHCPv4(StringJoiner sj) {
        try {
            sj.add(DhcpPacket.decodeFullPacket(this.mBytes, this.mLength, 0).toString());
        } catch (DhcpPacket.ParseException e) {
            sj.add("parse error: " + e);
        }
    }

    private static String getIPv4AddressString(ByteBuffer ipv4) {
        return getIpAddressString(ipv4, 4);
    }

    private static String getIPv6AddressString(ByteBuffer ipv6) {
        return getIpAddressString(ipv6, 16);
    }

    private static String getIpAddressString(ByteBuffer ip, int byteLength) {
        if (ip == null || ip.remaining() < byteLength) {
            return "invalid";
        }
        byte[] bytes = new byte[byteLength];
        ip.get(bytes, 0, byteLength);
        try {
            return InetAddress.getByAddress(bytes).getHostAddress();
        } catch (UnknownHostException e) {
            return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        }
    }

    private static String getMacAddressString(ByteBuffer mac) {
        if (mac == null || mac.remaining() < 6) {
            return "invalid";
        }
        byte[] bytes = new byte[6];
        int i = 0;
        mac.get(bytes, 0, bytes.length);
        Object[] printableBytes = new Object[bytes.length];
        int i2 = 0;
        int length = bytes.length;
        while (i < length) {
            printableBytes[i2] = new Byte(bytes[i]);
            i++;
            i2++;
        }
        return String.format("%02x:%02x:%02x:%02x:%02x:%02x", printableBytes);
    }
}
