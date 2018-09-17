package android.net.dhcp;

import android.net.DhcpResults;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.metrics.DhcpErrorEvent;
import android.net.util.NetworkConstants;
import android.os.SystemProperties;
import android.system.OsConstants;
import android.text.TextUtils;
import com.android.server.connectivity.LingerMonitor;
import com.android.server.wm.WindowManagerService.H;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
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
    protected static final byte CLIENT_ID_ETHER = (byte) 1;
    protected static final byte DHCP_BOOTREPLY = (byte) 2;
    protected static final byte DHCP_BOOTREQUEST = (byte) 1;
    protected static final byte DHCP_BROADCAST_ADDRESS = (byte) 28;
    static final short DHCP_CLIENT = (short) 68;
    protected static final byte DHCP_CLIENT_IDENTIFIER = (byte) 61;
    protected static final byte DHCP_DNS_SERVER = (byte) 6;
    protected static final byte DHCP_DOMAIN_NAME = (byte) 15;
    protected static final byte DHCP_HOST_NAME = (byte) 12;
    protected static final byte DHCP_LEASE_TIME = (byte) 51;
    private static final int DHCP_MAGIC_COOKIE = 1669485411;
    protected static final byte DHCP_MAX_MESSAGE_SIZE = (byte) 57;
    protected static final byte DHCP_MESSAGE = (byte) 56;
    protected static final byte DHCP_MESSAGE_TYPE = (byte) 53;
    protected static final byte DHCP_MESSAGE_TYPE_ACK = (byte) 5;
    protected static final byte DHCP_MESSAGE_TYPE_DECLINE = (byte) 4;
    protected static final byte DHCP_MESSAGE_TYPE_DISCOVER = (byte) 1;
    protected static final byte DHCP_MESSAGE_TYPE_INFORM = (byte) 8;
    protected static final byte DHCP_MESSAGE_TYPE_NAK = (byte) 6;
    protected static final byte DHCP_MESSAGE_TYPE_OFFER = (byte) 2;
    protected static final byte DHCP_MESSAGE_TYPE_REQUEST = (byte) 3;
    protected static final byte DHCP_MTU = (byte) 26;
    protected static final byte DHCP_OPTION_END = (byte) -1;
    protected static final byte DHCP_OPTION_PAD = (byte) 0;
    protected static final byte DHCP_PARAMETER_LIST = (byte) 55;
    protected static final byte DHCP_REBINDING_TIME = (byte) 59;
    protected static final byte DHCP_RENEWAL_TIME = (byte) 58;
    protected static final byte DHCP_REQUESTED_IP = (byte) 50;
    protected static final byte DHCP_ROUTER = (byte) 3;
    static final short DHCP_SERVER = (short) 67;
    protected static final byte DHCP_SERVER_IDENTIFIER = (byte) 54;
    protected static final byte DHCP_SUBNET_MASK = (byte) 1;
    protected static final byte DHCP_VENDOR_CLASS_ID = (byte) 60;
    protected static final byte DHCP_VENDOR_INFO = (byte) 43;
    public static final int ENCAP_BOOTP = 2;
    public static final int ENCAP_L2 = 0;
    public static final int ENCAP_L3 = 1;
    public static final byte[] ETHER_BROADCAST = new byte[]{DHCP_OPTION_END, DHCP_OPTION_END, DHCP_OPTION_END, DHCP_OPTION_END, DHCP_OPTION_END, DHCP_OPTION_END};
    public static final int HWADDR_LEN = 16;
    public static final Inet4Address INADDR_ANY = ((Inet4Address) Inet4Address.ANY);
    public static final Inet4Address INADDR_BROADCAST = ((Inet4Address) Inet4Address.ALL);
    public static final int INFINITE_LEASE = -1;
    private static final short IP_FLAGS_OFFSET = (short) 16384;
    private static final byte IP_TOS_LOWDELAY = (byte) 16;
    private static final byte IP_TTL = (byte) 64;
    private static final byte IP_TYPE_UDP = (byte) 17;
    private static final byte IP_VERSION_HEADER_LEN = (byte) 69;
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

        public ParseException(int errorCode, String msg, Object... args) {
            super(String.format(msg, args));
            this.errorCode = errorCode;
        }
    }

    public abstract ByteBuffer buildPacket(int i, short s, short s2);

    abstract void finishPacket(ByteBuffer byteBuffer);

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
        clientId[0] = (byte) 1;
        System.arraycopy(this.mClientMac, 0, clientId, 1, this.mClientMac.length);
        return clientId;
    }

    protected void fillInPacket(int encap, Inet4Address destIp, Inet4Address srcIp, short destUdp, short srcUdp, ByteBuffer buf, byte requestCode, boolean broadcast) {
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
        buf.order(ByteOrder.BIG_ENDIAN);
        if (encap == 0) {
            buf.put(ETHER_BROADCAST);
            buf.put(this.mClientMac);
            buf.putShort((short) OsConstants.ETH_P_IP);
        }
        if (encap <= 1) {
            ipHeaderOffset = buf.position();
            buf.put(IP_VERSION_HEADER_LEN);
            buf.put(IP_TOS_LOWDELAY);
            ipLengthOffset = buf.position();
            buf.putShort((short) 0);
            buf.putShort((short) 0);
            buf.putShort(IP_FLAGS_OFFSET);
            buf.put(IP_TTL);
            buf.put(IP_TYPE_UDP);
            ipChecksumOffset = buf.position();
            buf.putShort((short) 0);
            buf.put(srcIpArray);
            buf.put(destIpArray);
            endIpHeader = buf.position();
            udpHeaderOffset = buf.position();
            buf.putShort(srcUdp);
            buf.putShort(destUdp);
            udpLengthOffset = buf.position();
            buf.putShort((short) 0);
            udpChecksumOffset = buf.position();
            buf.putShort((short) 0);
        }
        buf.put(requestCode);
        buf.put((byte) 1);
        buf.put((byte) this.mClientMac.length);
        buf.put((byte) 0);
        buf.putInt(this.mTransId);
        buf.putShort(this.mSecs);
        if (broadcast) {
            buf.putShort(Short.MIN_VALUE);
        } else {
            buf.putShort((short) 0);
        }
        buf.put(this.mClientIp.getAddress());
        buf.put(this.mYourIp.getAddress());
        buf.put(this.mNextIp.getAddress());
        buf.put(this.mRelayIp.getAddress());
        buf.put(this.mClientMac);
        buf.position(((buf.position() + (16 - this.mClientMac.length)) + 64) + 128);
        buf.putInt(DHCP_MAGIC_COOKIE);
        finishPacket(buf);
        if ((buf.position() & 1) == 1) {
            buf.put((byte) 0);
        }
        if (encap <= 1) {
            short udpLen = (short) (buf.position() - udpHeaderOffset);
            buf.putShort(udpLengthOffset, udpLen);
            ByteBuffer byteBuffer = buf;
            byteBuffer = buf;
            byteBuffer.putShort(udpChecksumOffset, (short) checksum(byteBuffer, (((((intAbs(buf.getShort(ipChecksumOffset + 2)) + 0) + intAbs(buf.getShort(ipChecksumOffset + 4))) + intAbs(buf.getShort(ipChecksumOffset + 6))) + intAbs(buf.getShort(ipChecksumOffset + 8))) + 17) + udpLen, udpHeaderOffset, buf.position()));
            buf.putShort(ipLengthOffset, (short) (buf.position() - ipHeaderOffset));
            buf.putShort(ipChecksumOffset, (short) checksum(buf, 0, ipHeaderOffset, endIpHeader));
        }
    }

    private static int intAbs(short v) {
        return NetworkConstants.ARP_HWTYPE_RESERVED_HI & v;
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
        start += shortArray.length * 2;
        if (end != start) {
            short b = (short) buf.get(start);
            if (b < (short) 0) {
                b = (short) (b + 256);
            }
            sum += b * 256;
        }
        sum = ((sum >> 16) & NetworkConstants.ARP_HWTYPE_RESERVED_HI) + (sum & NetworkConstants.ARP_HWTYPE_RESERVED_HI);
        return intAbs((short) (~((((sum >> 16) & NetworkConstants.ARP_HWTYPE_RESERVED_HI) + sum) & NetworkConstants.ARP_HWTYPE_RESERVED_HI)));
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
        if (payload.length > 255) {
            throw new IllegalArgumentException("DHCP option too long: " + payload.length + " vs. " + 255);
        }
        buf.put(type);
        buf.put((byte) payload.length);
        buf.put(payload);
    }

    protected static void addTlv(ByteBuffer buf, byte type, Inet4Address addr) {
        if (addr != null) {
            addTlv(buf, type, addr.getAddress());
        }
    }

    protected static void addTlv(ByteBuffer buf, byte type, List<Inet4Address> addrs) {
        if (addrs != null && addrs.size() != 0) {
            int optionLen = addrs.size() * 4;
            if (optionLen > 255) {
                throw new IllegalArgumentException("DHCP option too long: " + optionLen + " vs. " + 255);
            }
            buf.put(type);
            buf.put((byte) optionLen);
            for (Inet4Address addr : addrs) {
                buf.put(addr.getAddress());
            }
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
            buf.put(DHCP_MESSAGE_TYPE_DECLINE);
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

    protected void addCommonClientTlvs(ByteBuffer buf) {
        addTlv(buf, (byte) DHCP_MAX_MESSAGE_SIZE, Short.valueOf((short) 1500));
        addTlv(buf, (byte) DHCP_VENDOR_CLASS_ID, getVendorId());
        String hn = getHostname();
        if (!TextUtils.isEmpty(hn)) {
            addTlv(buf, (byte) DHCP_HOST_NAME, hn);
        }
    }

    public static String macToString(byte[] mac) {
        String macAddr = "";
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
            while (length < bytes.length && bytes[length] != (byte) 0) {
                length++;
            }
        }
        return new String(bytes, 0, length, StandardCharsets.US_ASCII);
    }

    private static boolean isPacketToOrFromClient(short udpSrcPort, short udpDstPort) {
        return udpSrcPort == (short) 68 || udpDstPort == (short) 68;
    }

    private static boolean isPacketServerToServer(short udpSrcPort, short udpDstPort) {
        return udpSrcPort == DHCP_SERVER && udpDstPort == DHCP_SERVER;
    }

    static DhcpPacket decodeFullPacket(ByteBuffer packet, int pktType) throws ParseException {
        int i;
        List<Inet4Address> dnsServers = new ArrayList();
        List<Inet4Address> gateways = new ArrayList();
        Inet4Address serverIdentifier = null;
        Inet4Address netMask = null;
        String message = null;
        String vendorId = null;
        byte[] expectedParams = null;
        String hostName = null;
        String domainName = null;
        Inet4Address ipSrc = null;
        Inet4Address bcAddr = null;
        Inet4Address requestedIp = null;
        Short mtu = null;
        Short maxMessageSize = null;
        Integer leaseTime = null;
        Integer T1 = null;
        Integer T2 = null;
        byte dhcpType = DHCP_OPTION_END;
        packet.order(ByteOrder.BIG_ENDIAN);
        if (pktType == 0) {
            if (packet.remaining() < MIN_PACKET_LENGTH_L2) {
                throw new ParseException(DhcpErrorEvent.L2_TOO_SHORT, "L2 packet too short, %d < %d", Integer.valueOf(packet.remaining()), Integer.valueOf(MIN_PACKET_LENGTH_L2));
            }
            byte[] l2src = new byte[6];
            packet.get(new byte[6]);
            packet.get(l2src);
            if (packet.getShort() != OsConstants.ETH_P_IP) {
                throw new ParseException(DhcpErrorEvent.L2_WRONG_ETH_TYPE, "Unexpected L2 type 0x%04x, expected 0x%04x", Short.valueOf(packet.getShort()), Integer.valueOf(OsConstants.ETH_P_IP));
            }
        }
        if (pktType <= 1) {
            if (packet.remaining() < MIN_PACKET_LENGTH_L3) {
                throw new ParseException(DhcpErrorEvent.L3_TOO_SHORT, "L3 packet too short, %d < %d", Integer.valueOf(packet.remaining()), Integer.valueOf(MIN_PACKET_LENGTH_L3));
            }
            byte ipTypeAndLength = packet.get();
            if (((ipTypeAndLength & 240) >> 4) != 4) {
                throw new ParseException(DhcpErrorEvent.L3_NOT_IPV4, "Invalid IP version %d", Integer.valueOf((ipTypeAndLength & 240) >> 4));
            }
            byte ipDiffServicesField = packet.get();
            short ipTotalLength = packet.getShort();
            short ipIdentification = packet.getShort();
            byte ipFlags = packet.get();
            byte ipFragOffset = packet.get();
            byte ipTTL = packet.get();
            byte ipProto = packet.get();
            short ipChksm = packet.getShort();
            ipSrc = readIpAddress(packet);
            Inet4Address ipDst = readIpAddress(packet);
            if (ipProto != (byte) 17) {
                throw new ParseException(DhcpErrorEvent.L4_NOT_UDP, "Protocol not UDP: %d", Byte.valueOf(ipProto));
            }
            int optionWords = (ipTypeAndLength & 15) - 5;
            for (i = 0; i < optionWords; i++) {
                packet.getInt();
            }
            short udpSrcPort = packet.getShort();
            short udpDstPort = packet.getShort();
            short udpLen = packet.getShort();
            short udpChkSum = packet.getShort();
            if (!(isPacketToOrFromClient(udpSrcPort, udpDstPort) || (isPacketServerToServer(udpSrcPort, udpDstPort) ^ 1) == 0)) {
                throw new ParseException(DhcpErrorEvent.L4_WRONG_PORT, "Unexpected UDP ports %d->%d", Short.valueOf(udpSrcPort), Short.valueOf(udpDstPort));
            }
        }
        if (pktType > 2 || packet.remaining() < MIN_PACKET_LENGTH_BOOTP) {
            throw new ParseException(DhcpErrorEvent.BOOTP_TOO_SHORT, "Invalid type or BOOTP packet too short, %d < %d", Integer.valueOf(packet.remaining()), Integer.valueOf(MIN_PACKET_LENGTH_BOOTP));
        }
        byte type = packet.get();
        byte hwType = packet.get();
        int addrLen = packet.get() & 255;
        byte hops = packet.get();
        int transactionId = packet.getInt();
        short secs = packet.getShort();
        boolean broadcast = (32768 & packet.getShort()) != 0;
        byte[] ipv4addr = new byte[4];
        try {
            packet.get(ipv4addr);
            Inet4Address clientIp = (Inet4Address) Inet4Address.getByAddress(ipv4addr);
            packet.get(ipv4addr);
            Inet4Address yourIp = (Inet4Address) Inet4Address.getByAddress(ipv4addr);
            packet.get(ipv4addr);
            Inet4Address nextIp = (Inet4Address) Inet4Address.getByAddress(ipv4addr);
            packet.get(ipv4addr);
            Inet4Address relayIp = (Inet4Address) Inet4Address.getByAddress(ipv4addr);
            if (addrLen > 16) {
                addrLen = ETHER_BROADCAST.length;
            }
            if (addrLen > 16) {
                addrLen = ETHER_BROADCAST.length;
            }
            byte[] clientMac = new byte[addrLen];
            packet.get(clientMac);
            packet.position(packet.position() + (16 - addrLen));
            String vendorInfo = readAsciiString(packet, 64, false);
            if (!vendorInfo.isEmpty()) {
                vendorInfo = "hostname:" + vendorInfo;
            }
            packet.position(packet.position() + 128);
            if (packet.remaining() < 4) {
                throw new ParseException(DhcpErrorEvent.DHCP_NO_COOKIE, "not a DHCP message", new Object[0]);
            }
            try {
                if (packet.getInt() != DHCP_MAGIC_COOKIE) {
                    throw new ParseException(DhcpErrorEvent.DHCP_BAD_MAGIC_COOKIE, "Bad magic cookie 0x%08x, should be 0x%08x", Integer.valueOf(packet.getInt()), Integer.valueOf(DHCP_MAGIC_COOKIE));
                }
                DhcpPacket newPacket;
                boolean notFinishedOptions = true;
                while (packet.position() < packet.limit() && notFinishedOptions) {
                    byte optionType = packet.get();
                    if (optionType == (byte) -1) {
                        notFinishedOptions = false;
                    } else if (optionType != (byte) 0) {
                        try {
                            int optionLen = packet.get() & 255;
                            int expectedLen = 0;
                            switch (optionType) {
                                case (byte) 1:
                                    netMask = readIpAddress(packet);
                                    expectedLen = 4;
                                    break;
                                case (byte) 3:
                                    expectedLen = 0;
                                    while (expectedLen < optionLen) {
                                        gateways.add(readIpAddress(packet));
                                        expectedLen += 4;
                                    }
                                    break;
                                case (byte) 6:
                                    expectedLen = 0;
                                    while (expectedLen < optionLen) {
                                        dnsServers.add(readIpAddress(packet));
                                        expectedLen += 4;
                                    }
                                    break;
                                case (byte) 12:
                                    expectedLen = optionLen;
                                    hostName = readAsciiString(packet, optionLen, false);
                                    break;
                                case (byte) 15:
                                    expectedLen = optionLen;
                                    domainName = readAsciiString(packet, optionLen, false);
                                    break;
                                case H.DO_ANIMATION_CALLBACK /*26*/:
                                    expectedLen = 2;
                                    mtu = Short.valueOf(packet.getShort());
                                    break;
                                case NetworkConstants.ARP_PAYLOAD_LEN /*28*/:
                                    bcAddr = readIpAddress(packet);
                                    expectedLen = 4;
                                    break;
                                case HdmiCecKeycode.CEC_KEYCODE_ENTER /*43*/:
                                    expectedLen = optionLen;
                                    vendorInfo = readAsciiString(packet, optionLen, true);
                                    break;
                                case HdmiCecKeycode.CEC_KEYCODE_PREVIOUS_CHANNEL /*50*/:
                                    requestedIp = readIpAddress(packet);
                                    expectedLen = 4;
                                    break;
                                case (byte) 51:
                                    leaseTime = Integer.valueOf(packet.getInt());
                                    expectedLen = 4;
                                    break;
                                case (byte) 53:
                                    dhcpType = packet.get();
                                    expectedLen = 1;
                                    break;
                                case (byte) 54:
                                    serverIdentifier = readIpAddress(packet);
                                    expectedLen = 4;
                                    break;
                                case (byte) 55:
                                    expectedParams = new byte[optionLen];
                                    packet.get(expectedParams);
                                    expectedLen = optionLen;
                                    break;
                                case (byte) 56:
                                    expectedLen = optionLen;
                                    message = readAsciiString(packet, optionLen, false);
                                    break;
                                case H.NOTIFY_KEYGUARD_TRUSTED_CHANGED /*57*/:
                                    expectedLen = 2;
                                    maxMessageSize = Short.valueOf(packet.getShort());
                                    break;
                                case H.SET_HAS_OVERLAY_UI /*58*/:
                                    expectedLen = 4;
                                    T1 = Integer.valueOf(packet.getInt());
                                    break;
                                case (byte) 59:
                                    expectedLen = 4;
                                    T2 = Integer.valueOf(packet.getInt());
                                    break;
                                case MINIMUM_LEASE /*60*/:
                                    expectedLen = optionLen;
                                    vendorId = readAsciiString(packet, optionLen, true);
                                    break;
                                case (byte) 61:
                                    packet.get(new byte[optionLen]);
                                    expectedLen = optionLen;
                                    break;
                                default:
                                    for (i = 0; i < optionLen; i++) {
                                        expectedLen++;
                                        byte throwaway = packet.get();
                                    }
                                    break;
                            }
                            if (expectedLen != optionLen) {
                                throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.DHCP_INVALID_OPTION_LENGTH, optionType), "Invalid length %d for option %d, expected %d", Integer.valueOf(optionLen), Byte.valueOf(optionType), Integer.valueOf(expectedLen));
                            }
                        } catch (BufferUnderflowException e) {
                            throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.BUFFER_UNDERFLOW, optionType), "BufferUnderflowException", new Object[0]);
                        }
                    } else {
                        continue;
                    }
                }
                DhcpPacket dhcpRequestPacket;
                switch (dhcpType) {
                    case (byte) -1:
                        throw new ParseException(DhcpErrorEvent.DHCP_NO_MSG_TYPE, "No DHCP message type option", new Object[0]);
                    case (byte) 1:
                        newPacket = new DhcpDiscoverPacket(transactionId, secs, clientMac, broadcast);
                        break;
                    case (byte) 2:
                        newPacket = new DhcpOfferPacket(transactionId, secs, broadcast, ipSrc, clientIp, yourIp, clientMac);
                        break;
                    case (byte) 3:
                        dhcpRequestPacket = new DhcpRequestPacket(transactionId, secs, clientIp, clientMac, broadcast);
                        break;
                    case (byte) 4:
                        dhcpRequestPacket = new DhcpDeclinePacket(transactionId, secs, clientIp, yourIp, nextIp, relayIp, clientMac);
                        break;
                    case (byte) 5:
                        newPacket = new DhcpAckPacket(transactionId, secs, broadcast, ipSrc, clientIp, yourIp, clientMac);
                        break;
                    case (byte) 6:
                        dhcpRequestPacket = new DhcpNakPacket(transactionId, secs, clientIp, yourIp, nextIp, relayIp, clientMac);
                        break;
                    case (byte) 8:
                        dhcpRequestPacket = new DhcpInformPacket(transactionId, secs, clientIp, yourIp, nextIp, relayIp, clientMac);
                        break;
                    default:
                        throw new ParseException(DhcpErrorEvent.DHCP_UNKNOWN_MSG_TYPE, "Unimplemented DHCP type %d", Byte.valueOf(dhcpType));
                }
                newPacket.mBroadcastAddress = bcAddr;
                newPacket.mDnsServers = dnsServers;
                newPacket.mDomainName = domainName;
                newPacket.mGateways = gateways;
                newPacket.mHostName = hostName;
                newPacket.mLeaseTime = leaseTime;
                newPacket.mMessage = message;
                newPacket.mMtu = mtu;
                newPacket.mRequestedIp = requestedIp;
                newPacket.mRequestedParams = expectedParams;
                newPacket.mServerIdentifier = serverIdentifier;
                newPacket.mSubnetMask = netMask;
                newPacket.mMaxMessageSize = maxMessageSize;
                newPacket.mT1 = T1;
                newPacket.mT2 = T2;
                newPacket.mVendorId = vendorId;
                newPacket.mVendorInfo = vendorInfo;
                return newPacket;
            } catch (BufferUnderflowException e2) {
                throw new ParseException(DhcpErrorEvent.BUFFER_UNDERFLOW, "BufferUnderflowException", new Object[0]);
            }
        } catch (UnknownHostException e3) {
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
        }
        prefixLength = NetworkUtils.getImplicitNetmask(ipAddress);
        DhcpResults results = new DhcpResults();
        try {
            int i;
            results.ipAddress = new LinkAddress(ipAddress, prefixLength);
            if (this.mGateways.size() > 0) {
                results.gateway = (InetAddress) this.mGateways.get(0);
            }
            results.dnsServers.addAll(this.mDnsServers);
            results.domains = this.mDomainName;
            results.serverAddress = this.mServerIdentifier;
            results.vendorInfo = this.mVendorInfo;
            results.leaseDuration = this.mLeaseTime != null ? this.mLeaseTime.intValue() : -1;
            if (this.mMtu == null || (short) 1280 > this.mMtu.shortValue() || this.mMtu.shortValue() > (short) 1500) {
                i = 0;
            } else {
                i = this.mMtu.shortValue();
            }
            results.mtu = i;
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
        return LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS;
    }

    public static ByteBuffer buildDiscoverPacket(int encap, int transactionId, short secs, byte[] clientMac, boolean broadcast, byte[] expectedParams) {
        DhcpPacket pkt = new DhcpDiscoverPacket(transactionId, secs, clientMac, broadcast);
        pkt.mRequestedParams = expectedParams;
        return pkt.buildPacket(encap, DHCP_SERVER, (short) 68);
    }

    public static ByteBuffer buildOfferPacket(int encap, int transactionId, boolean broadcast, Inet4Address serverIpAddr, Inet4Address clientIpAddr, byte[] mac, Integer timeout, Inet4Address netMask, Inet4Address bcAddr, List<Inet4Address> gateways, List<Inet4Address> dnsServers, Inet4Address dhcpServerIdentifier, String domainName) {
        DhcpPacket pkt = new DhcpOfferPacket(transactionId, (short) 0, broadcast, serverIpAddr, INADDR_ANY, clientIpAddr, mac);
        pkt.mGateways = gateways;
        pkt.mDnsServers = dnsServers;
        pkt.mLeaseTime = timeout;
        pkt.mDomainName = domainName;
        pkt.mServerIdentifier = dhcpServerIdentifier;
        pkt.mSubnetMask = netMask;
        pkt.mBroadcastAddress = bcAddr;
        return pkt.buildPacket(encap, (short) 68, DHCP_SERVER);
    }

    public static ByteBuffer buildAckPacket(int encap, int transactionId, boolean broadcast, Inet4Address serverIpAddr, Inet4Address clientIpAddr, byte[] mac, Integer timeout, Inet4Address netMask, Inet4Address bcAddr, List<Inet4Address> gateways, List<Inet4Address> dnsServers, Inet4Address dhcpServerIdentifier, String domainName) {
        DhcpPacket pkt = new DhcpAckPacket(transactionId, (short) 0, broadcast, serverIpAddr, INADDR_ANY, clientIpAddr, mac);
        pkt.mGateways = gateways;
        pkt.mDnsServers = dnsServers;
        pkt.mLeaseTime = timeout;
        pkt.mDomainName = domainName;
        pkt.mSubnetMask = netMask;
        pkt.mServerIdentifier = dhcpServerIdentifier;
        pkt.mBroadcastAddress = bcAddr;
        return pkt.buildPacket(encap, (short) 68, DHCP_SERVER);
    }

    public static ByteBuffer buildNakPacket(int encap, int transactionId, Inet4Address serverIpAddr, Inet4Address clientIpAddr, byte[] mac) {
        DhcpPacket pkt = new DhcpNakPacket(transactionId, (short) 0, clientIpAddr, serverIpAddr, serverIpAddr, serverIpAddr, mac);
        pkt.mMessage = "requested address not available";
        pkt.mRequestedIp = clientIpAddr;
        return pkt.buildPacket(encap, (short) 68, DHCP_SERVER);
    }

    public static ByteBuffer buildRequestPacket(int encap, int transactionId, short secs, Inet4Address clientIp, boolean broadcast, byte[] clientMac, Inet4Address requestedIpAddress, Inet4Address serverIdentifier, byte[] requestedParams, String hostName) {
        DhcpPacket pkt = new DhcpRequestPacket(transactionId, secs, clientIp, clientMac, broadcast);
        pkt.mRequestedIp = requestedIpAddress;
        pkt.mServerIdentifier = serverIdentifier;
        pkt.mHostName = hostName;
        pkt.mRequestedParams = requestedParams;
        return pkt.buildPacket(encap, DHCP_SERVER, (short) 68);
    }

    public static ByteBuffer buildDeclinePacket(int encap, int transactionId, short secs, Inet4Address clientIp, boolean broadcast, byte[] clientMac, Inet4Address requestedIpAddress, Inet4Address serverIdentifier, byte[] requestedParams, String hostName) {
        DhcpPacket pkt = new DhcpDeclinePacket(transactionId, secs, INADDR_ANY, INADDR_ANY, INADDR_ANY, INADDR_ANY, clientMac);
        pkt.mRequestedIp = requestedIpAddress;
        pkt.mServerIdentifier = serverIdentifier;
        pkt.mHostName = hostName;
        pkt.mRequestedParams = requestedParams;
        return pkt.buildPacket(encap, DHCP_SERVER, (short) 68);
    }
}
