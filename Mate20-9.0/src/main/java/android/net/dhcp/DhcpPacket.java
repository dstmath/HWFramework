package android.net.dhcp;

import android.net.DhcpResults;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.metrics.DhcpErrorEvent;
import android.net.util.NetworkConstants;
import android.os.SystemProperties;
import android.system.OsConstants;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wm.WindowManagerService;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class DhcpPacket {
    protected static final byte CLIENT_ID_ETHER = 1;
    protected static final byte DHCP_BOOTREPLY = 2;
    protected static final byte DHCP_BOOTREQUEST = 1;
    protected static final byte DHCP_BROADCAST_ADDRESS = 28;
    static final short DHCP_CLIENT = 68;
    protected static final byte DHCP_CLIENT_IDENTIFIER = 61;
    protected static final byte DHCP_DNS_SERVER = 6;
    protected static final byte DHCP_DOMAIN_NAME = 15;
    protected static final byte DHCP_HOST_NAME = 12;
    protected static final byte DHCP_LEASE_TIME = 51;
    private static final int DHCP_MAGIC_COOKIE = 1669485411;
    protected static final byte DHCP_MAX_MESSAGE_SIZE = 57;
    protected static final byte DHCP_MESSAGE = 56;
    protected static final byte DHCP_MESSAGE_TYPE = 53;
    protected static final byte DHCP_MESSAGE_TYPE_ACK = 5;
    protected static final byte DHCP_MESSAGE_TYPE_DECLINE = 4;
    protected static final byte DHCP_MESSAGE_TYPE_DISCOVER = 1;
    protected static final byte DHCP_MESSAGE_TYPE_INFORM = 8;
    protected static final byte DHCP_MESSAGE_TYPE_NAK = 6;
    protected static final byte DHCP_MESSAGE_TYPE_OFFER = 2;
    protected static final byte DHCP_MESSAGE_TYPE_REQUEST = 3;
    protected static final byte DHCP_MTU = 26;
    protected static final byte DHCP_OPTION_END = -1;
    protected static final byte DHCP_OPTION_PAD = 0;
    protected static final byte DHCP_PARAMETER_LIST = 55;
    protected static final byte DHCP_REBINDING_TIME = 59;
    protected static final byte DHCP_RENEWAL_TIME = 58;
    protected static final byte DHCP_REQUESTED_IP = 50;
    protected static final byte DHCP_ROUTER = 3;
    static final short DHCP_SERVER = 67;
    protected static final byte DHCP_SERVER_IDENTIFIER = 54;
    protected static final byte DHCP_SUBNET_MASK = 1;
    protected static final byte DHCP_VENDOR_CLASS_ID = 60;
    protected static final byte DHCP_VENDOR_INFO = 43;
    public static final int ENCAP_BOOTP = 2;
    public static final int ENCAP_L2 = 0;
    public static final int ENCAP_L3 = 1;
    public static final byte[] ETHER_BROADCAST = {DHCP_OPTION_END, DHCP_OPTION_END, DHCP_OPTION_END, DHCP_OPTION_END, DHCP_OPTION_END, DHCP_OPTION_END};
    public static final int HWADDR_LEN = 16;
    public static final Inet4Address INADDR_ANY = ((Inet4Address) Inet4Address.ANY);
    public static final Inet4Address INADDR_BROADCAST = ((Inet4Address) Inet4Address.ALL);
    public static final int INFINITE_LEASE = -1;
    private static final short IP_FLAGS_OFFSET = 16384;
    private static final byte IP_TOS_LOWDELAY = 16;
    private static final byte IP_TTL = 64;
    private static final byte IP_TYPE_UDP = 17;
    private static final byte IP_VERSION_HEADER_LEN = 69;
    protected static final int MAX_LENGTH = 1500;
    private static final int MAX_MTU = 1500;
    public static final int MAX_OPTION_LEN = 255;
    public static final int MINIMUM_LEASE = 60;
    private static final int MIN_MTU = 1280;
    public static final int MIN_PACKET_LENGTH_BOOTP = 236;
    public static final int MIN_PACKET_LENGTH_L2 = 278;
    public static final int MIN_PACKET_LENGTH_L3 = 264;
    protected static final String TAG = "DhcpPacket";
    static String testOverrideHostname = null;
    static String testOverrideVendorId = null;
    protected boolean mBroadcast;
    protected Inet4Address mBroadcastAddress;
    protected final Inet4Address mClientIp;
    protected final byte[] mClientMac;
    protected List<Inet4Address> mDnsServers;
    protected String mDomainName;
    protected List<Inet4Address> mGateways;
    protected String mHostName;
    protected Integer mLeaseTime;
    protected Short mMaxMessageSize;
    protected String mMessage;
    protected Short mMtu;
    private final Inet4Address mNextIp;
    private final Inet4Address mRelayIp;
    protected Inet4Address mRequestedIp;
    protected byte[] mRequestedParams;
    protected final short mSecs;
    protected Inet4Address mServerIdentifier;
    protected Inet4Address mSubnetMask;
    protected Integer mT1;
    protected Integer mT2;
    protected final int mTransId;
    protected String mVendorId;
    protected String mVendorInfo;
    protected final Inet4Address mYourIp;

    public static class ParseException extends Exception {
        public final int errorCode;

        public ParseException(int errorCode2, String msg, Object... args) {
            super(String.format(msg, args));
            this.errorCode = errorCode2;
        }
    }

    public abstract ByteBuffer buildPacket(int i, short s, short s2);

    /* access modifiers changed from: package-private */
    public abstract void finishPacket(ByteBuffer byteBuffer);

    protected DhcpPacket(int transId, short secs, Inet4Address clientIp, Inet4Address yourIp, Inet4Address nextIp, Inet4Address relayIp, byte[] clientMac, boolean broadcast) {
        this.mTransId = transId;
        this.mSecs = secs;
        this.mClientIp = clientIp;
        this.mYourIp = yourIp;
        this.mNextIp = nextIp;
        this.mRelayIp = relayIp;
        this.mClientMac = clientMac;
        this.mBroadcast = broadcast;
    }

    public int getTransactionId() {
        return this.mTransId;
    }

    public byte[] getClientMac() {
        return this.mClientMac;
    }

    public byte[] getClientId() {
        byte[] clientId = new byte[(this.mClientMac.length + 1)];
        clientId[0] = 1;
        System.arraycopy(this.mClientMac, 0, clientId, 1, this.mClientMac.length);
        return clientId;
    }

    /* access modifiers changed from: protected */
    public void fillInPacket(int encap, Inet4Address destIp, Inet4Address srcIp, short destUdp, short srcUdp, ByteBuffer buf, byte requestCode, boolean broadcast) {
        int i = encap;
        ByteBuffer byteBuffer = buf;
        byte[] destIpArray = destIp.getAddress();
        byte[] srcIpArray = srcIp.getAddress();
        int ipHeaderOffset = 0;
        int ipLengthOffset = 0;
        int ipChecksumOffset = 0;
        int endIpHeader = 0;
        int udpHeaderOffset = 0;
        int udpLengthOffset = 0;
        int udpChecksumOffset = 0;
        buf.clear();
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        if (i == 0) {
            byteBuffer.put(ETHER_BROADCAST);
            byteBuffer.put(this.mClientMac);
            byteBuffer.putShort((short) OsConstants.ETH_P_IP);
        }
        if (i <= 1) {
            ipHeaderOffset = buf.position();
            byteBuffer.put(IP_VERSION_HEADER_LEN);
            byteBuffer.put((byte) 16);
            ipLengthOffset = buf.position();
            byteBuffer.putShort(0);
            byteBuffer.putShort(0);
            byteBuffer.putShort(IP_FLAGS_OFFSET);
            byteBuffer.put(IP_TTL);
            byteBuffer.put(IP_TYPE_UDP);
            ipChecksumOffset = buf.position();
            byteBuffer.putShort(0);
            byteBuffer.put(srcIpArray);
            byteBuffer.put(destIpArray);
            endIpHeader = buf.position();
            udpHeaderOffset = buf.position();
            byteBuffer.putShort(srcUdp);
            byteBuffer.putShort(destUdp);
            udpLengthOffset = buf.position();
            byteBuffer.putShort(0);
            udpChecksumOffset = buf.position();
            byteBuffer.putShort(0);
        } else {
            short s = destUdp;
            short s2 = srcUdp;
        }
        buf.put(requestCode);
        byteBuffer.put((byte) 1);
        byteBuffer.put((byte) this.mClientMac.length);
        byteBuffer.put((byte) 0);
        byteBuffer.putInt(this.mTransId);
        byteBuffer.putShort(this.mSecs);
        if (broadcast) {
            byteBuffer.putShort(Short.MIN_VALUE);
        } else {
            byteBuffer.putShort(0);
        }
        byteBuffer.put(this.mClientIp.getAddress());
        byteBuffer.put(this.mYourIp.getAddress());
        byteBuffer.put(this.mNextIp.getAddress());
        byteBuffer.put(this.mRelayIp.getAddress());
        byteBuffer.put(this.mClientMac);
        byte[] bArr = destIpArray;
        byteBuffer.position(buf.position() + (16 - this.mClientMac.length) + 64 + 128);
        byteBuffer.putInt(DHCP_MAGIC_COOKIE);
        finishPacket(byteBuffer);
        if ((buf.position() & 1) == 1) {
            byteBuffer.put((byte) 0);
        }
        if (i <= 1) {
            short udpLen = (short) (buf.position() - udpHeaderOffset);
            byteBuffer.putShort(udpLengthOffset, udpLen);
            byteBuffer.putShort(udpChecksumOffset, (short) checksum(byteBuffer, 0 + intAbs(byteBuffer.getShort(ipChecksumOffset + 2)) + intAbs(byteBuffer.getShort(ipChecksumOffset + 4)) + intAbs(byteBuffer.getShort(ipChecksumOffset + 6)) + intAbs(byteBuffer.getShort(ipChecksumOffset + 8)) + 17 + udpLen, udpHeaderOffset, buf.position()));
            byteBuffer.putShort(ipLengthOffset, (short) (buf.position() - ipHeaderOffset));
            byteBuffer.putShort(ipChecksumOffset, (short) checksum(byteBuffer, 0, ipHeaderOffset, endIpHeader));
        }
    }

    private static int intAbs(short v) {
        return 65535 & v;
    }

    private int checksum(ByteBuffer buf, int seed, int start, int end) {
        int sum = seed;
        int bufPosition = buf.position();
        buf.position(start);
        ShortBuffer shortBuf = buf.asShortBuffer();
        buf.position(bufPosition);
        short[] shortArray = new short[((end - start) / 2)];
        shortBuf.get(shortArray);
        for (short s : shortArray) {
            sum += intAbs(s);
        }
        int start2 = start + (shortArray.length * 2);
        if (end != start2) {
            short b = (short) buf.get(start2);
            if (b < 0) {
                b = (short) (b + 256);
            }
            sum += b * 256;
        }
        int sum2 = ((sum >> 16) & NetworkConstants.ARP_HWTYPE_RESERVED_HI) + (sum & NetworkConstants.ARP_HWTYPE_RESERVED_HI);
        return intAbs((short) (~((((sum2 >> 16) & NetworkConstants.ARP_HWTYPE_RESERVED_HI) + sum2) & NetworkConstants.ARP_HWTYPE_RESERVED_HI)));
    }

    protected static void addTlv(ByteBuffer buf, byte type, byte value) {
        buf.put(type);
        buf.put((byte) 1);
        buf.put(value);
    }

    protected static void addTlv(ByteBuffer buf, byte type, byte[] payload) {
        if (payload == null) {
            return;
        }
        if (payload.length <= 255) {
            buf.put(type);
            buf.put((byte) payload.length);
            buf.put(payload);
            return;
        }
        throw new IllegalArgumentException("DHCP option too long: " + payload.length + " vs. " + 255);
    }

    protected static void addTlv(ByteBuffer buf, byte type, Inet4Address addr) {
        if (addr != null) {
            addTlv(buf, type, addr.getAddress());
        }
    }

    protected static void addTlv(ByteBuffer buf, byte type, List<Inet4Address> addrs) {
        if (addrs != null && addrs.size() != 0) {
            int optionLen = 4 * addrs.size();
            if (optionLen <= 255) {
                buf.put(type);
                buf.put((byte) optionLen);
                for (Inet4Address addr : addrs) {
                    buf.put(addr.getAddress());
                }
                return;
            }
            throw new IllegalArgumentException("DHCP option too long: " + optionLen + " vs. " + 255);
        }
    }

    protected static void addTlv(ByteBuffer buf, byte type, Short value) {
        if (value != null) {
            buf.put(type);
            buf.put((byte) 2);
            buf.putShort(value.shortValue());
        }
    }

    protected static void addTlv(ByteBuffer buf, byte type, Integer value) {
        if (value != null) {
            buf.put(type);
            buf.put((byte) 4);
            buf.putInt(value.intValue());
        }
    }

    protected static void addTlv(ByteBuffer buf, byte type, String str) {
        try {
            addTlv(buf, type, str.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("String is not US-ASCII: " + str);
        }
    }

    protected static void addTlvEnd(ByteBuffer buf) {
        buf.put(DHCP_OPTION_END);
    }

    private String getVendorId() {
        if (testOverrideVendorId != null) {
            return testOverrideVendorId;
        }
        return "HUAWEI:android:" + SystemProperties.get("ro.product.board");
    }

    private String getHostname() {
        if (testOverrideHostname != null) {
            return testOverrideHostname;
        }
        return SystemProperties.get("net.hostname");
    }

    /* access modifiers changed from: protected */
    public void addCommonClientTlvs(ByteBuffer buf) {
        addTlv(buf, (byte) DHCP_MAX_MESSAGE_SIZE, (Short) 1500);
        addTlv(buf, (byte) DHCP_VENDOR_CLASS_ID, getVendorId());
        String hn = getHostname();
        if (!TextUtils.isEmpty(hn)) {
            addTlv(buf, (byte) 12, hn);
        }
    }

    public static String macToString(byte[] mac) {
        String macAddr = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        for (int i = 0; i < mac.length; i++) {
            String hexString = "0" + Integer.toHexString(mac[i]);
            if (i % 2 == 0) {
                macAddr = macAddr + hexString.substring(hexString.length() - 2);
            } else {
                macAddr = macAddr + "xx";
            }
            if (i != mac.length - 1) {
                macAddr = macAddr + ":";
            }
        }
        return macAddr;
    }

    public String toString() {
        return macToString(this.mClientMac);
    }

    private static Inet4Address readIpAddress(ByteBuffer packet) {
        byte[] ipAddr = new byte[4];
        packet.get(ipAddr);
        try {
            return (Inet4Address) Inet4Address.getByAddress(ipAddr);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private static String readAsciiString(ByteBuffer buf, int byteCount, boolean nullOk) {
        byte[] bytes = new byte[byteCount];
        buf.get(bytes);
        int length = bytes.length;
        if (!nullOk) {
            length = 0;
            while (length < bytes.length && bytes[length] != 0) {
                length++;
            }
        }
        return new String(bytes, 0, length, StandardCharsets.US_ASCII);
    }

    private static boolean isPacketToOrFromClient(short udpSrcPort, short udpDstPort) {
        return udpSrcPort == 68 || udpDstPort == 68;
    }

    private static boolean isPacketServerToServer(short udpSrcPort, short udpDstPort) {
        return udpSrcPort == 67 && udpDstPort == 67;
    }

    /* JADX WARNING: type inference failed for: r0v38, types: [android.net.dhcp.DhcpDiscoverPacket] */
    /* JADX WARNING: type inference failed for: r0v39 */
    /* JADX WARNING: type inference failed for: r33v2, types: [android.net.dhcp.DhcpOfferPacket] */
    /* JADX WARNING: type inference failed for: r34v7, types: [android.net.dhcp.DhcpRequestPacket] */
    /* JADX WARNING: type inference failed for: r34v8, types: [android.net.dhcp.DhcpDeclinePacket] */
    /* JADX WARNING: type inference failed for: r33v3, types: [android.net.dhcp.DhcpAckPacket] */
    /* JADX WARNING: type inference failed for: r44v2, types: [android.net.dhcp.DhcpNakPacket] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x03de  */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x03e7 A[SYNTHETIC] */
    @VisibleForTesting
    static DhcpPacket decodeFullPacket(ByteBuffer packet, int pktType) throws ParseException {
        String vendorId;
        String message;
        Inet4Address netMask;
        int i;
        byte[] expectedParams;
        String vendorInfo;
        byte[] expectedParams2;
        Inet4Address requestedIp;
        DhcpInformPacket dhcpInformPacket;
        ? r0;
        int expectedLen;
        byte dhcpType;
        ByteBuffer byteBuffer = packet;
        int i2 = pktType;
        List<Inet4Address> arrayList = new ArrayList<>();
        ArrayList arrayList2 = new ArrayList();
        Inet4Address ipDst = null;
        Inet4Address ipSrc = null;
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        if (i2 != 0) {
            netMask = null;
            message = null;
            vendorId = null;
            i = 1;
        } else if (packet.remaining() >= 278) {
            byte[] l2dst = new byte[6];
            netMask = null;
            byte[] l2src = new byte[6];
            byteBuffer.get(l2dst);
            byteBuffer.get(l2src);
            short l2type = packet.getShort();
            byte[] bArr = l2dst;
            if (l2type == OsConstants.ETH_P_IP) {
                message = null;
                vendorId = null;
                i = 1;
            } else {
                byte[] bArr2 = l2src;
                short s = l2type;
                throw new ParseException(DhcpErrorEvent.L2_WRONG_ETH_TYPE, "Unexpected L2 type 0x%04x, expected 0x%04x", Short.valueOf(l2type), Integer.valueOf(OsConstants.ETH_P_IP));
            }
        } else {
            throw new ParseException(DhcpErrorEvent.L2_TOO_SHORT, "L2 packet too short, %d < %d", Integer.valueOf(packet.remaining()), Integer.valueOf(MIN_PACKET_LENGTH_L2));
        }
        if (i2 > i) {
            expectedParams = null;
        } else if (packet.remaining() >= 264) {
            int i3 = packet.get();
            int ipVersion = (i3 & 240) >> 4;
            if (ipVersion == 4) {
                byte ipDiffServicesField = packet.get();
                short ipTotalLength = packet.getShort();
                short s2 = packet.getShort();
                byte b = packet.get();
                byte b2 = packet.get();
                byte b3 = packet.get();
                byte ipProto = packet.get();
                short s3 = packet.getShort();
                ipSrc = readIpAddress(packet);
                ipDst = readIpAddress(packet);
                byte b4 = ipDiffServicesField;
                if (ipProto == 17) {
                    int optionWords = (i3 & 15) - 5;
                    int i4 = 0;
                    while (true) {
                        int ipTypeAndLength = i3;
                        int i5 = i4;
                        if (i5 >= optionWords) {
                            break;
                        }
                        packet.getInt();
                        i4 = i5 + 1;
                        i3 = ipTypeAndLength;
                    }
                    short udpSrcPort = packet.getShort();
                    int i6 = optionWords;
                    short udpDstPort = packet.getShort();
                    short s4 = packet.getShort();
                    short s5 = packet.getShort();
                    if (isPacketToOrFromClient(udpSrcPort, udpDstPort)) {
                        expectedParams = null;
                    } else if (isPacketServerToServer(udpSrcPort, udpDstPort)) {
                        expectedParams = null;
                    } else {
                        short s6 = ipTotalLength;
                        short s7 = udpSrcPort;
                        throw new ParseException(DhcpErrorEvent.L4_WRONG_PORT, "Unexpected UDP ports %d->%d", Short.valueOf(udpSrcPort), Short.valueOf(udpDstPort));
                    }
                } else {
                    int i7 = i3;
                    short s8 = ipTotalLength;
                    throw new ParseException(DhcpErrorEvent.L4_NOT_UDP, "Protocol not UDP: %d", Byte.valueOf(ipProto));
                }
            } else {
                int ipTypeAndLength2 = i3;
                throw new ParseException(DhcpErrorEvent.L3_NOT_IPV4, "Invalid IP version %d", Integer.valueOf(ipVersion));
            }
        } else {
            throw new ParseException(DhcpErrorEvent.L3_TOO_SHORT, "L3 packet too short, %d < %d", Integer.valueOf(packet.remaining()), Integer.valueOf(MIN_PACKET_LENGTH_L3));
        }
        if (i2 > 2 || packet.remaining() < 236) {
            List<Inet4Address> dnsServers = arrayList;
            ArrayList arrayList3 = arrayList2;
            Inet4Address inet4Address = ipDst;
            throw new ParseException(DhcpErrorEvent.BOOTP_TOO_SHORT, "Invalid type or BOOTP packet too short, %d < %d", Integer.valueOf(packet.remaining()), Integer.valueOf(MIN_PACKET_LENGTH_BOOTP));
        }
        byte type = packet.get();
        byte hwType = packet.get();
        int addrLen = packet.get() & 255;
        byte hops = packet.get();
        int transactionId = packet.getInt();
        short secs = packet.getShort();
        byte broadcast = (packet.getShort() & 32768) != 0;
        byte[] ipv4addr = new byte[4];
        try {
            byteBuffer.get(ipv4addr);
            Inet4Address clientIp = (Inet4Address) Inet4Address.getByAddress(ipv4addr);
            byteBuffer.get(ipv4addr);
            Inet4Address yourIp = (Inet4Address) Inet4Address.getByAddress(ipv4addr);
            byteBuffer.get(ipv4addr);
            Inet4Address nextIp = (Inet4Address) Inet4Address.getByAddress(ipv4addr);
            byteBuffer.get(ipv4addr);
            Inet4Address relayIp = (Inet4Address) Inet4Address.getByAddress(ipv4addr);
            if (addrLen > 16) {
                addrLen = ETHER_BROADCAST.length;
            }
            if (addrLen > 16) {
                addrLen = ETHER_BROADCAST.length;
            }
            byte b5 = type;
            byte[] clientMac = new byte[addrLen];
            byteBuffer.get(clientMac);
            byteBuffer.position(packet.position() + (16 - addrLen));
            byte b6 = hwType;
            String vendorInfo2 = readAsciiString(byteBuffer, 64, false);
            if (vendorInfo2.isEmpty() == 0) {
                StringBuilder sb = new StringBuilder();
                int i8 = addrLen;
                sb.append("hostname:");
                sb.append(vendorInfo2);
                vendorInfo = sb.toString();
            } else {
                vendorInfo = vendorInfo2;
            }
            byteBuffer.position(packet.position() + 128);
            if (packet.remaining() >= 4) {
                try {
                    int dhcpMagicCookie = packet.getInt();
                    if (dhcpMagicCookie == DHCP_MAGIC_COOKIE) {
                        byte[] bArr3 = ipv4addr;
                        String vendorInfo3 = vendorInfo;
                        byte b7 = hops;
                        String domainName = null;
                        Inet4Address inet4Address2 = ipDst;
                        Short maxMessageSize = null;
                        Integer leaseTime = null;
                        Integer T1 = null;
                        Integer T2 = null;
                        byte dhcpType2 = -1;
                        Inet4Address serverIdentifier = null;
                        Inet4Address netMask2 = netMask;
                        String message2 = message;
                        String vendorId2 = vendorId;
                        byte[] expectedParams3 = expectedParams;
                        boolean notFinishedOptions = true;
                        String hostName = null;
                        Short mtu = null;
                        Inet4Address bcAddr = null;
                        Inet4Address requestedIp2 = null;
                        while (true) {
                            boolean notFinishedOptions2 = notFinishedOptions;
                            expectedParams2 = expectedParams3;
                            requestedIp = requestedIp2;
                            if (packet.position() >= packet.limit() || !notFinishedOptions2) {
                                Short mtu2 = mtu;
                            } else {
                                byte optionType = packet.get();
                                if (optionType == -1) {
                                    notFinishedOptions = false;
                                } else if (optionType == 0) {
                                    notFinishedOptions = notFinishedOptions2;
                                } else {
                                    try {
                                        int optionLen = packet.get() & DHCP_OPTION_END;
                                        int expectedLen2 = 0;
                                        Short mtu3 = mtu;
                                        if (optionType != 1) {
                                            if (optionType == 3) {
                                                int expectedLen3 = 0;
                                                while (expectedLen < optionLen) {
                                                    arrayList2.add(readIpAddress(packet));
                                                    expectedLen3 = expectedLen + 4;
                                                }
                                            } else if (optionType == 6) {
                                                expectedLen = 0;
                                                while (expectedLen < optionLen) {
                                                    arrayList.add(readIpAddress(packet));
                                                    expectedLen += 4;
                                                }
                                            } else if (optionType == 12) {
                                                expectedLen2 = optionLen;
                                                hostName = readAsciiString(byteBuffer, optionLen, false);
                                            } else if (optionType == 15) {
                                                expectedLen2 = optionLen;
                                                domainName = readAsciiString(byteBuffer, optionLen, false);
                                            } else if (optionType == 26) {
                                                expectedLen2 = 2;
                                                mtu3 = Short.valueOf(packet.getShort());
                                            } else if (optionType == 28) {
                                                bcAddr = readIpAddress(packet);
                                                expectedLen2 = 4;
                                            } else if (optionType != 43) {
                                                switch (optionType) {
                                                    case HdmiCecKeycode.CEC_KEYCODE_PREVIOUS_CHANNEL /*50*/:
                                                        expectedLen2 = 4;
                                                        requestedIp2 = readIpAddress(packet);
                                                        break;
                                                    case 51:
                                                        leaseTime = Integer.valueOf(packet.getInt());
                                                        expectedLen2 = 4;
                                                        break;
                                                    default:
                                                        switch (optionType) {
                                                            case 53:
                                                                dhcpType2 = packet.get();
                                                                expectedLen2 = 1;
                                                                break;
                                                            case 54:
                                                                serverIdentifier = readIpAddress(packet);
                                                                expectedLen2 = 4;
                                                                break;
                                                            case 55:
                                                                byte[] expectedParams4 = new byte[optionLen];
                                                                try {
                                                                    byteBuffer.get(expectedParams4);
                                                                    expectedLen2 = optionLen;
                                                                    expectedParams2 = expectedParams4;
                                                                    break;
                                                                } catch (BufferUnderflowException e) {
                                                                    e = e;
                                                                    byte[] bArr4 = expectedParams4;
                                                                    String str = domainName;
                                                                    BufferUnderflowException bufferUnderflowException = e;
                                                                    throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.BUFFER_UNDERFLOW, optionType), "BufferUnderflowException", new Object[0]);
                                                                }
                                                            case 56:
                                                                expectedLen2 = optionLen;
                                                                message2 = readAsciiString(byteBuffer, optionLen, false);
                                                                break;
                                                            case WindowManagerService.H.NOTIFY_KEYGUARD_TRUSTED_CHANGED /*57*/:
                                                                expectedLen2 = 2;
                                                                maxMessageSize = Short.valueOf(packet.getShort());
                                                                break;
                                                            case WindowManagerService.H.SET_HAS_OVERLAY_UI /*58*/:
                                                                expectedLen2 = 4;
                                                                T1 = Integer.valueOf(packet.getInt());
                                                                break;
                                                            case WindowManagerService.H.SET_RUNNING_REMOTE_ANIMATION /*59*/:
                                                                expectedLen2 = 4;
                                                                T2 = Integer.valueOf(packet.getInt());
                                                                break;
                                                            case 60:
                                                                expectedLen2 = optionLen;
                                                                vendorId2 = readAsciiString(byteBuffer, optionLen, true);
                                                                break;
                                                            case WindowManagerService.H.RECOMPUTE_FOCUS /*61*/:
                                                                byteBuffer.get(new byte[optionLen]);
                                                                expectedLen2 = optionLen;
                                                                break;
                                                            default:
                                                                int i9 = 0;
                                                                while (i9 < optionLen) {
                                                                    expectedLen2++;
                                                                    try {
                                                                        packet.get();
                                                                        i9++;
                                                                    } catch (BufferUnderflowException e2) {
                                                                        e = e2;
                                                                        String str2 = domainName;
                                                                        BufferUnderflowException bufferUnderflowException2 = e;
                                                                        throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.BUFFER_UNDERFLOW, optionType), "BufferUnderflowException", new Object[0]);
                                                                    }
                                                                }
                                                                break;
                                                        }
                                                }
                                            } else {
                                                expectedLen2 = optionLen;
                                                vendorInfo3 = readAsciiString(byteBuffer, optionLen, true);
                                            }
                                            requestedIp2 = requestedIp;
                                            if (expectedLen != optionLen) {
                                                notFinishedOptions = notFinishedOptions2;
                                                mtu = mtu3;
                                                expectedParams3 = expectedParams2;
                                            } else {
                                                String domainName2 = domainName;
                                                try {
                                                    dhcpType = dhcpType2;
                                                } catch (BufferUnderflowException e3) {
                                                    e = e3;
                                                    byte b8 = dhcpType2;
                                                    Integer num = leaseTime;
                                                    String str3 = message2;
                                                    Inet4Address inet4Address3 = requestedIp2;
                                                    domainName = domainName2;
                                                    String str22 = domainName;
                                                    BufferUnderflowException bufferUnderflowException22 = e;
                                                    throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.BUFFER_UNDERFLOW, optionType), "BufferUnderflowException", new Object[0]);
                                                }
                                                try {
                                                    try {
                                                        throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.DHCP_INVALID_OPTION_LENGTH, optionType), "Invalid length %d for option %d, expected %d", Integer.valueOf(optionLen), Byte.valueOf(optionType), Integer.valueOf(expectedLen));
                                                    } catch (BufferUnderflowException e4) {
                                                        e = e4;
                                                        Inet4Address inet4Address4 = requestedIp2;
                                                        domainName = domainName2;
                                                        byte b9 = dhcpType;
                                                        Integer num2 = leaseTime;
                                                        String str4 = message2;
                                                        String str222 = domainName;
                                                        BufferUnderflowException bufferUnderflowException222 = e;
                                                        throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.BUFFER_UNDERFLOW, optionType), "BufferUnderflowException", new Object[0]);
                                                    }
                                                } catch (BufferUnderflowException e5) {
                                                    e = e5;
                                                    Integer num3 = leaseTime;
                                                    String str5 = message2;
                                                    Inet4Address inet4Address5 = requestedIp2;
                                                    domainName = domainName2;
                                                    byte b10 = dhcpType;
                                                    String str2222 = domainName;
                                                    BufferUnderflowException bufferUnderflowException2222 = e;
                                                    throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.BUFFER_UNDERFLOW, optionType), "BufferUnderflowException", new Object[0]);
                                                }
                                            }
                                        } else {
                                            netMask2 = readIpAddress(packet);
                                            expectedLen2 = 4;
                                        }
                                        requestedIp2 = requestedIp;
                                        expectedLen = expectedLen2;
                                        if (expectedLen != optionLen) {
                                        }
                                    } catch (BufferUnderflowException e6) {
                                        e = e6;
                                        Short sh = mtu;
                                        String str22222 = domainName;
                                        BufferUnderflowException bufferUnderflowException22222 = e;
                                        throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.BUFFER_UNDERFLOW, optionType), "BufferUnderflowException", new Object[0]);
                                    }
                                }
                                requestedIp2 = requestedIp;
                                expectedParams3 = expectedParams2;
                            }
                        }
                        Short mtu22 = mtu;
                        if (dhcpType2 != -1) {
                            if (dhcpType2 != 8) {
                                switch (dhcpType2) {
                                    case 1:
                                        dhcpInformPacket = new DhcpDiscoverPacket(transactionId, secs, clientMac, broadcast);
                                        break;
                                    case 2:
                                        DhcpOfferPacket dhcpOfferPacket = new DhcpOfferPacket(transactionId, secs, broadcast, ipSrc, clientIp, yourIp, clientMac);
                                        r0 = dhcpOfferPacket;
                                        break;
                                    case 3:
                                        DhcpRequestPacket dhcpRequestPacket = new DhcpRequestPacket(transactionId, secs, clientIp, clientMac, broadcast);
                                        r0 = dhcpRequestPacket;
                                        break;
                                    case 4:
                                        DhcpDeclinePacket dhcpDeclinePacket = new DhcpDeclinePacket(transactionId, secs, clientIp, yourIp, nextIp, relayIp, clientMac);
                                        r0 = dhcpDeclinePacket;
                                        break;
                                    case 5:
                                        DhcpAckPacket dhcpAckPacket = new DhcpAckPacket(transactionId, secs, broadcast, ipSrc, clientIp, yourIp, clientMac);
                                        r0 = dhcpAckPacket;
                                        break;
                                    case 6:
                                        DhcpNakPacket dhcpNakPacket = new DhcpNakPacket(transactionId, secs, clientIp, yourIp, nextIp, relayIp, clientMac);
                                        r0 = dhcpNakPacket;
                                        break;
                                    default:
                                        throw new ParseException(DhcpErrorEvent.DHCP_UNKNOWN_MSG_TYPE, "Unimplemented DHCP type %d", Byte.valueOf(dhcpType2));
                                }
                                dhcpInformPacket = r0;
                            } else {
                                DhcpInformPacket dhcpInformPacket2 = new DhcpInformPacket(transactionId, secs, clientIp, yourIp, nextIp, relayIp, clientMac);
                                dhcpInformPacket = dhcpInformPacket2;
                            }
                            dhcpInformPacket.mBroadcastAddress = bcAddr;
                            dhcpInformPacket.mDnsServers = arrayList;
                            dhcpInformPacket.mDomainName = domainName;
                            dhcpInformPacket.mGateways = arrayList2;
                            dhcpInformPacket.mHostName = hostName;
                            dhcpInformPacket.mLeaseTime = leaseTime;
                            dhcpInformPacket.mMessage = message2;
                            dhcpInformPacket.mMtu = mtu22;
                            dhcpInformPacket.mRequestedIp = requestedIp;
                            String str6 = domainName;
                            byte[] expectedParams5 = expectedParams2;
                            dhcpInformPacket.mRequestedParams = expectedParams5;
                            byte[] bArr5 = expectedParams5;
                            Inet4Address serverIdentifier2 = serverIdentifier;
                            dhcpInformPacket.mServerIdentifier = serverIdentifier2;
                            Inet4Address inet4Address6 = serverIdentifier2;
                            Inet4Address netMask3 = netMask2;
                            dhcpInformPacket.mSubnetMask = netMask3;
                            Inet4Address inet4Address7 = netMask3;
                            Short maxMessageSize2 = maxMessageSize;
                            dhcpInformPacket.mMaxMessageSize = maxMessageSize2;
                            Short sh2 = maxMessageSize2;
                            Integer T12 = T1;
                            dhcpInformPacket.mT1 = T12;
                            Integer num4 = T12;
                            Integer T13 = T2;
                            dhcpInformPacket.mT2 = T13;
                            Integer num5 = T13;
                            String vendorId3 = vendorId2;
                            dhcpInformPacket.mVendorId = vendorId3;
                            String str7 = vendorId3;
                            dhcpInformPacket.mVendorInfo = vendorInfo3;
                            return dhcpInformPacket;
                        }
                        boolean z = broadcast;
                        String str8 = vendorId2;
                        Inet4Address inet4Address8 = netMask2;
                        Short sh3 = maxMessageSize;
                        Inet4Address inet4Address9 = serverIdentifier;
                        Integer num6 = T1;
                        Integer num7 = T2;
                        byte[] bArr6 = expectedParams2;
                        Inet4Address inet4Address10 = requestedIp;
                        Short sh4 = mtu22;
                        String str9 = vendorInfo3;
                        ArrayList arrayList4 = arrayList;
                        ArrayList arrayList5 = arrayList2;
                        throw new ParseException(DhcpErrorEvent.DHCP_NO_MSG_TYPE, "No DHCP message type option", new Object[0]);
                    }
                    ArrayList arrayList6 = arrayList;
                    ArrayList arrayList7 = arrayList2;
                    byte b11 = hops;
                    Inet4Address inet4Address11 = ipDst;
                    byte hops2 = broadcast;
                    try {
                        throw new ParseException(DhcpErrorEvent.DHCP_BAD_MAGIC_COOKIE, "Bad magic cookie 0x%08x, should be 0x%08x", Integer.valueOf(dhcpMagicCookie), Integer.valueOf(DHCP_MAGIC_COOKIE));
                    } catch (BufferUnderflowException e7) {
                        throw new ParseException(DhcpErrorEvent.BUFFER_UNDERFLOW, "BufferUnderflowException", new Object[0]);
                    }
                } catch (BufferUnderflowException e8) {
                    byte[] bArr7 = ipv4addr;
                    ArrayList arrayList8 = arrayList;
                    ArrayList arrayList9 = arrayList2;
                    byte b12 = hops;
                    Inet4Address inet4Address12 = ipDst;
                    byte hops3 = broadcast;
                    throw new ParseException(DhcpErrorEvent.BUFFER_UNDERFLOW, "BufferUnderflowException", new Object[0]);
                }
            } else {
                ArrayList arrayList10 = arrayList;
                ArrayList arrayList11 = arrayList2;
                byte b13 = hops;
                Inet4Address inet4Address13 = ipDst;
                byte hops4 = broadcast;
                throw new ParseException(DhcpErrorEvent.DHCP_NO_COOKIE, "not a DHCP message", new Object[0]);
            }
        } catch (UnknownHostException e9) {
            ArrayList arrayList12 = arrayList;
            ArrayList arrayList13 = arrayList2;
            byte b14 = type;
            byte b15 = hwType;
            byte b16 = hops;
            Inet4Address inet4Address14 = ipDst;
            byte hops5 = broadcast;
            throw new ParseException(DhcpErrorEvent.L3_INVALID_IP, "Invalid IPv4 address: %s", Arrays.toString(ipv4addr));
        }
    }

    public static DhcpPacket decodeFullPacket(byte[] packet, int length, int pktType) throws ParseException {
        try {
            return decodeFullPacket(ByteBuffer.wrap(packet, 0, length).order(ByteOrder.BIG_ENDIAN), pktType);
        } catch (ParseException e) {
            throw e;
        } catch (Exception e2) {
            throw new ParseException(DhcpErrorEvent.PARSING_ERROR, e2.getMessage(), new Object[0]);
        }
    }

    public DhcpResults toDhcpResults() {
        int prefixLength;
        Inet4Address ipAddress = this.mYourIp;
        if (ipAddress.equals(Inet4Address.ANY)) {
            ipAddress = this.mClientIp;
            if (ipAddress.equals(Inet4Address.ANY)) {
                return null;
            }
        }
        if (this.mSubnetMask != null) {
            try {
                prefixLength = NetworkUtils.netmaskToPrefixLength(this.mSubnetMask);
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            prefixLength = NetworkUtils.getImplicitNetmask(ipAddress);
        }
        DhcpResults results = new DhcpResults();
        try {
            results.ipAddress = new LinkAddress(ipAddress, prefixLength);
            short s = 0;
            if (this.mGateways.size() > 0) {
                results.gateway = this.mGateways.get(0);
            }
            results.dnsServers.addAll(this.mDnsServers);
            results.domains = this.mDomainName;
            results.serverAddress = this.mServerIdentifier;
            results.vendorInfo = this.mVendorInfo;
            results.leaseDuration = this.mLeaseTime != null ? this.mLeaseTime.intValue() : -1;
            if (this.mMtu != null && 1280 <= this.mMtu.shortValue() && this.mMtu.shortValue() <= 1500) {
                s = this.mMtu.shortValue();
            }
            results.mtu = s;
            return results;
        } catch (IllegalArgumentException e2) {
            return null;
        }
    }

    public long getLeaseTimeMillis() {
        if (this.mLeaseTime == null || this.mLeaseTime.intValue() == -1) {
            return 0;
        }
        if (this.mLeaseTime.intValue() < 0 || this.mLeaseTime.intValue() >= 60) {
            return (((long) this.mLeaseTime.intValue()) & 4294967295L) * 1000;
        }
        return 60000;
    }

    public static ByteBuffer buildDiscoverPacket(int encap, int transactionId, short secs, byte[] clientMac, boolean broadcast, byte[] expectedParams) {
        DhcpPacket pkt = new DhcpDiscoverPacket(transactionId, secs, clientMac, broadcast);
        pkt.mRequestedParams = expectedParams;
        return pkt.buildPacket(encap, DHCP_SERVER, 68);
    }

    public static ByteBuffer buildOfferPacket(int encap, int transactionId, boolean broadcast, Inet4Address serverIpAddr, Inet4Address clientIpAddr, byte[] mac, Integer timeout, Inet4Address netMask, Inet4Address bcAddr, List<Inet4Address> gateways, List<Inet4Address> dnsServers, Inet4Address dhcpServerIdentifier, String domainName) {
        DhcpOfferPacket dhcpOfferPacket = new DhcpOfferPacket(transactionId, 0, broadcast, serverIpAddr, INADDR_ANY, clientIpAddr, mac);
        dhcpOfferPacket.mGateways = gateways;
        dhcpOfferPacket.mDnsServers = dnsServers;
        dhcpOfferPacket.mLeaseTime = timeout;
        dhcpOfferPacket.mDomainName = domainName;
        dhcpOfferPacket.mServerIdentifier = dhcpServerIdentifier;
        dhcpOfferPacket.mSubnetMask = netMask;
        dhcpOfferPacket.mBroadcastAddress = bcAddr;
        return dhcpOfferPacket.buildPacket(encap, 68, DHCP_SERVER);
    }

    public static ByteBuffer buildAckPacket(int encap, int transactionId, boolean broadcast, Inet4Address serverIpAddr, Inet4Address clientIpAddr, byte[] mac, Integer timeout, Inet4Address netMask, Inet4Address bcAddr, List<Inet4Address> gateways, List<Inet4Address> dnsServers, Inet4Address dhcpServerIdentifier, String domainName) {
        DhcpAckPacket dhcpAckPacket = new DhcpAckPacket(transactionId, 0, broadcast, serverIpAddr, INADDR_ANY, clientIpAddr, mac);
        dhcpAckPacket.mGateways = gateways;
        dhcpAckPacket.mDnsServers = dnsServers;
        dhcpAckPacket.mLeaseTime = timeout;
        dhcpAckPacket.mDomainName = domainName;
        dhcpAckPacket.mSubnetMask = netMask;
        dhcpAckPacket.mServerIdentifier = dhcpServerIdentifier;
        dhcpAckPacket.mBroadcastAddress = bcAddr;
        return dhcpAckPacket.buildPacket(encap, 68, DHCP_SERVER);
    }

    public static ByteBuffer buildNakPacket(int encap, int transactionId, Inet4Address serverIpAddr, Inet4Address clientIpAddr, byte[] mac) {
        DhcpNakPacket dhcpNakPacket = new DhcpNakPacket(transactionId, 0, clientIpAddr, serverIpAddr, serverIpAddr, serverIpAddr, mac);
        dhcpNakPacket.mMessage = "requested address not available";
        dhcpNakPacket.mRequestedIp = clientIpAddr;
        return dhcpNakPacket.buildPacket(encap, 68, DHCP_SERVER);
    }

    public static ByteBuffer buildRequestPacket(int encap, int transactionId, short secs, Inet4Address clientIp, boolean broadcast, byte[] clientMac, Inet4Address requestedIpAddress, Inet4Address serverIdentifier, byte[] requestedParams, String hostName) {
        DhcpRequestPacket dhcpRequestPacket = new DhcpRequestPacket(transactionId, secs, clientIp, clientMac, broadcast);
        dhcpRequestPacket.mRequestedIp = requestedIpAddress;
        dhcpRequestPacket.mServerIdentifier = serverIdentifier;
        dhcpRequestPacket.mHostName = hostName;
        dhcpRequestPacket.mRequestedParams = requestedParams;
        return dhcpRequestPacket.buildPacket(encap, DHCP_SERVER, 68);
    }

    public static ByteBuffer buildDeclinePacket(int encap, int transactionId, short secs, Inet4Address clientIp, boolean broadcast, byte[] clientMac, Inet4Address requestedIpAddress, Inet4Address serverIdentifier, byte[] requestedParams, String hostName) {
        DhcpDeclinePacket dhcpDeclinePacket = new DhcpDeclinePacket(transactionId, secs, INADDR_ANY, INADDR_ANY, INADDR_ANY, INADDR_ANY, clientMac);
        dhcpDeclinePacket.mRequestedIp = requestedIpAddress;
        dhcpDeclinePacket.mServerIdentifier = serverIdentifier;
        dhcpDeclinePacket.mHostName = hostName;
        dhcpDeclinePacket.mRequestedParams = requestedParams;
        return dhcpDeclinePacket.buildPacket(encap, DHCP_SERVER, 68);
    }
}
