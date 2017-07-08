package android.net.dhcp;

import android.net.DhcpResults;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.metrics.DhcpErrorEvent;
import android.os.SystemProperties;
import android.system.OsConstants;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowManagerService.H;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class DhcpPacket {
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
    public static final byte[] ETHER_BROADCAST = null;
    public static final int HWADDR_LEN = 16;
    public static final Inet4Address INADDR_ANY = null;
    public static final Inet4Address INADDR_BROADCAST = null;
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
    static String testOverrideHostname;
    static String testOverrideVendorId;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.dhcp.DhcpPacket.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.dhcp.DhcpPacket.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.dhcp.DhcpPacket.<clinit>():void");
    }

    private int checksum(java.nio.ByteBuffer r1, int r2, int r3, int r4) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.dhcp.DhcpPacket.checksum(java.nio.ByteBuffer, int, int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.dhcp.DhcpPacket.checksum(java.nio.ByteBuffer, int, int, int):int");
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
        byte[] clientId = new byte[(this.mClientMac.length + ENCAP_L3)];
        clientId[ENCAP_L2] = DHCP_SUBNET_MASK;
        System.arraycopy(this.mClientMac, ENCAP_L2, clientId, ENCAP_L3, this.mClientMac.length);
        return clientId;
    }

    protected void fillInPacket(int encap, Inet4Address destIp, Inet4Address srcIp, short destUdp, short srcUdp, ByteBuffer buf, byte requestCode, boolean broadcast) {
        byte[] destIpArray = destIp.getAddress();
        byte[] srcIpArray = srcIp.getAddress();
        int ipHeaderOffset = ENCAP_L2;
        int ipLengthOffset = ENCAP_L2;
        int ipChecksumOffset = ENCAP_L2;
        int endIpHeader = ENCAP_L2;
        int udpHeaderOffset = ENCAP_L2;
        int udpLengthOffset = ENCAP_L2;
        int udpChecksumOffset = ENCAP_L2;
        buf.clear();
        buf.order(ByteOrder.BIG_ENDIAN);
        if (encap == 0) {
            buf.put(ETHER_BROADCAST);
            buf.put(this.mClientMac);
            buf.putShort((short) OsConstants.ETH_P_IP);
        }
        if (encap <= ENCAP_L3) {
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
        buf.put(DHCP_SUBNET_MASK);
        buf.put((byte) this.mClientMac.length);
        buf.put(DHCP_OPTION_PAD);
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
        buf.position(((buf.position() + (16 - this.mClientMac.length)) + 64) + DumpState.DUMP_PACKAGES);
        buf.putInt(DHCP_MAGIC_COOKIE);
        finishPacket(buf);
        if ((buf.position() & ENCAP_L3) == ENCAP_L3) {
            buf.put(DHCP_OPTION_PAD);
        }
        if (encap <= ENCAP_L3) {
            short udpLen = (short) (buf.position() - udpHeaderOffset);
            buf.putShort(udpLengthOffset, udpLen);
            ByteBuffer byteBuffer = buf;
            byteBuffer = buf;
            byteBuffer.putShort(udpChecksumOffset, (short) checksum(byteBuffer, (((((intAbs(buf.getShort(ipChecksumOffset + ENCAP_BOOTP)) + ENCAP_L2) + intAbs(buf.getShort(ipChecksumOffset + 4))) + intAbs(buf.getShort(ipChecksumOffset + 6))) + intAbs(buf.getShort(ipChecksumOffset + 8))) + 17) + udpLen, udpHeaderOffset, buf.position()));
            buf.putShort(ipLengthOffset, (short) (buf.position() - ipHeaderOffset));
            buf.putShort(ipChecksumOffset, (short) checksum(buf, ENCAP_L2, ipHeaderOffset, endIpHeader));
        }
    }

    private static int intAbs(short v) {
        return 65535 & v;
    }

    protected static void addTlv(ByteBuffer buf, byte type, byte value) {
        buf.put(type);
        buf.put(DHCP_SUBNET_MASK);
        buf.put(value);
    }

    protected static void addTlv(ByteBuffer buf, byte type, byte[] payload) {
        if (payload == null) {
            return;
        }
        if (payload.length > MAX_OPTION_LEN) {
            throw new IllegalArgumentException("DHCP option too long: " + payload.length + " vs. " + MAX_OPTION_LEN);
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
            if (optionLen > MAX_OPTION_LEN) {
                throw new IllegalArgumentException("DHCP option too long: " + optionLen + " vs. " + MAX_OPTION_LEN);
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
            buf.put(DHCP_MESSAGE_TYPE_OFFER);
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
        addTlv(buf, (byte) DHCP_HOST_NAME, getHostname());
    }

    public static String macToString(byte[] mac) {
        String macAddr = "";
        for (int i = ENCAP_L2; i < mac.length; i += ENCAP_L3) {
            String hexString = "0" + Integer.toHexString(mac[i]);
            if (i % ENCAP_BOOTP == 0) {
                macAddr = macAddr + hexString.substring(hexString.length() - 2);
            } else {
                macAddr = macAddr + "xx";
            }
            if (i != mac.length + INFINITE_LEASE) {
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
            length = ENCAP_L2;
            while (length < bytes.length && bytes[length] != null) {
                length += ENCAP_L3;
            }
        }
        return new String(bytes, ENCAP_L2, length, StandardCharsets.US_ASCII);
    }

    private static boolean isPacketToOrFromClient(short udpSrcPort, short udpDstPort) {
        return udpSrcPort == DHCP_CLIENT || udpDstPort == DHCP_CLIENT;
    }

    private static boolean isPacketServerToServer(short udpSrcPort, short udpDstPort) {
        return udpSrcPort == DHCP_SERVER && udpDstPort == DHCP_SERVER;
    }

    public static DhcpPacket decodeFullPacket(ByteBuffer packet, int pktType) throws ParseException {
        Object[] objArr;
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
        Inet4Address inet4Address = null;
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
                objArr = new Object[ENCAP_BOOTP];
                objArr[ENCAP_L2] = Integer.valueOf(packet.remaining());
                objArr[ENCAP_L3] = Integer.valueOf(MIN_PACKET_LENGTH_L2);
                throw new ParseException(DhcpErrorEvent.L2_TOO_SHORT, "L2 packet too short, %d < %d", objArr);
            }
            byte[] l2src = new byte[6];
            packet.get(new byte[6]);
            packet.get(l2src);
            short l2type = packet.getShort();
            if (l2type != OsConstants.ETH_P_IP) {
                objArr = new Object[ENCAP_BOOTP];
                objArr[ENCAP_L2] = Short.valueOf(l2type);
                objArr[ENCAP_L3] = Integer.valueOf(OsConstants.ETH_P_IP);
                throw new ParseException(DhcpErrorEvent.L2_WRONG_ETH_TYPE, "Unexpected L2 type 0x%04x, expected 0x%04x", objArr);
            }
        }
        if (pktType <= ENCAP_L3) {
            if (packet.remaining() < MIN_PACKET_LENGTH_L3) {
                objArr = new Object[ENCAP_BOOTP];
                objArr[ENCAP_L2] = Integer.valueOf(packet.remaining());
                objArr[ENCAP_L3] = Integer.valueOf(MIN_PACKET_LENGTH_L3);
                throw new ParseException(DhcpErrorEvent.L3_TOO_SHORT, "L3 packet too short, %d < %d", objArr);
            }
            byte ipTypeAndLength = packet.get();
            int ipVersion = (ipTypeAndLength & 240) >> 4;
            if (ipVersion != 4) {
                objArr = new Object[ENCAP_L3];
                objArr[ENCAP_L2] = Integer.valueOf(ipVersion);
                throw new ParseException(DhcpErrorEvent.L3_NOT_IPV4, "Invalid IP version %d", objArr);
            }
            byte ipDiffServicesField = packet.get();
            short ipTotalLength = packet.getShort();
            short ipIdentification = packet.getShort();
            byte ipFlags = packet.get();
            byte ipFragOffset = packet.get();
            byte ipTTL = packet.get();
            byte ipProto = packet.get();
            short ipChksm = packet.getShort();
            inet4Address = readIpAddress(packet);
            Inet4Address ipDst = readIpAddress(packet);
            if (ipProto != 17) {
                objArr = new Object[ENCAP_L3];
                objArr[ENCAP_L2] = Byte.valueOf(ipProto);
                throw new ParseException(DhcpErrorEvent.L4_NOT_UDP, "Protocol not UDP: %d", objArr);
            }
            int optionWords = (ipTypeAndLength & 15) - 5;
            for (i = ENCAP_L2; i < optionWords; i += ENCAP_L3) {
                packet.getInt();
            }
            short udpSrcPort = packet.getShort();
            short udpDstPort = packet.getShort();
            short udpLen = packet.getShort();
            short udpChkSum = packet.getShort();
            if (!(isPacketToOrFromClient(udpSrcPort, udpDstPort) || isPacketServerToServer(udpSrcPort, udpDstPort))) {
                objArr = new Object[ENCAP_BOOTP];
                objArr[ENCAP_L2] = Short.valueOf(udpSrcPort);
                objArr[ENCAP_L3] = Short.valueOf(udpDstPort);
                throw new ParseException(DhcpErrorEvent.L4_WRONG_PORT, "Unexpected UDP ports %d->%d", objArr);
            }
        }
        if (pktType > ENCAP_BOOTP || packet.remaining() < MIN_PACKET_LENGTH_BOOTP) {
            objArr = new Object[ENCAP_BOOTP];
            objArr[ENCAP_L2] = Integer.valueOf(packet.remaining());
            objArr[ENCAP_L3] = Integer.valueOf(MIN_PACKET_LENGTH_BOOTP);
            throw new ParseException(DhcpErrorEvent.BOOTP_TOO_SHORT, "Invalid type or BOOTP packet too short, %d < %d", objArr);
        }
        byte type = packet.get();
        byte hwType = packet.get();
        int addrLen = packet.get() & MAX_OPTION_LEN;
        byte hops = packet.get();
        int transactionId = packet.getInt();
        short secs = packet.getShort();
        boolean broadcast = (DumpState.DUMP_VERSION & packet.getShort()) != 0;
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
            if (addrLen > HWADDR_LEN) {
                addrLen = ETHER_BROADCAST.length;
            }
            if (addrLen > HWADDR_LEN) {
                addrLen = ETHER_BROADCAST.length;
            }
            byte[] clientMac = new byte[addrLen];
            packet.get(clientMac);
            packet.position(packet.position() + (16 - addrLen));
            String vendorInfo = readAsciiString(packet, 64, false);
            if (!vendorInfo.isEmpty()) {
                vendorInfo = "hostname:" + vendorInfo;
            }
            packet.position(packet.position() + DumpState.DUMP_PACKAGES);
            try {
                int dhcpMagicCookie = packet.getInt();
                if (dhcpMagicCookie != DHCP_MAGIC_COOKIE) {
                    objArr = new Object[ENCAP_BOOTP];
                    objArr[ENCAP_L2] = Integer.valueOf(dhcpMagicCookie);
                    objArr[ENCAP_L3] = Integer.valueOf(DHCP_MAGIC_COOKIE);
                    throw new ParseException(DhcpErrorEvent.DHCP_BAD_MAGIC_COOKIE, "Bad magic cookie 0x%08x, should be 0x%08x", objArr);
                }
                DhcpPacket newPacket;
                boolean notFinishedOptions = true;
                while (packet.position() < packet.limit() && notFinishedOptions) {
                    byte optionType = packet.get();
                    if (optionType == INFINITE_LEASE) {
                        notFinishedOptions = false;
                    } else if (optionType != null) {
                        try {
                            int optionLen = packet.get() & MAX_OPTION_LEN;
                            int expectedLen = ENCAP_L2;
                            switch (optionType) {
                                case ENCAP_L3 /*1*/:
                                    netMask = readIpAddress(packet);
                                    expectedLen = 4;
                                    break;
                                case H.REPORT_LOSING_FOCUS /*3*/:
                                    expectedLen = ENCAP_L2;
                                    while (expectedLen < optionLen) {
                                        gateways.add(readIpAddress(packet));
                                        expectedLen += 4;
                                    }
                                    break;
                                case H.REMOVE_STARTING /*6*/:
                                    expectedLen = ENCAP_L2;
                                    while (expectedLen < optionLen) {
                                        dnsServers.add(readIpAddress(packet));
                                        expectedLen += 4;
                                    }
                                    break;
                                case AppTransition.TRANSIT_WALLPAPER_CLOSE /*12*/:
                                    expectedLen = optionLen;
                                    hostName = readAsciiString(packet, optionLen, false);
                                    break;
                                case H.FORCE_GC /*15*/:
                                    expectedLen = optionLen;
                                    domainName = readAsciiString(packet, optionLen, false);
                                    break;
                                case H.DO_ANIMATION_CALLBACK /*26*/:
                                    expectedLen = ENCAP_BOOTP;
                                    mtu = Short.valueOf(packet.getShort());
                                    break;
                                case H.DO_DISPLAY_REMOVED /*28*/:
                                    bcAddr = readIpAddress(packet);
                                    expectedLen = 4;
                                    break;
                                case H.RESIZE_TASK /*43*/:
                                    expectedLen = optionLen;
                                    vendorInfo = readAsciiString(packet, optionLen, true);
                                    break;
                                case H.NOTIFY_STARTING_WINDOW_DRAWN /*50*/:
                                    requestedIp = readIpAddress(packet);
                                    expectedLen = 4;
                                    break;
                                case H.UPDATE_ANIMATION_SCALE /*51*/:
                                    leaseTime = Integer.valueOf(packet.getInt());
                                    expectedLen = 4;
                                    break;
                                case H.NOTIFY_DOCKED_STACK_MINIMIZED_CHANGED /*53*/:
                                    dhcpType = packet.get();
                                    expectedLen = ENCAP_L3;
                                    break;
                                case HdmiCecKeycode.CEC_KEYCODE_HELP /*54*/:
                                    serverIdentifier = readIpAddress(packet);
                                    expectedLen = 4;
                                    break;
                                case HdmiCecKeycode.CEC_KEYCODE_PAGE_UP /*55*/:
                                    expectedParams = new byte[optionLen];
                                    packet.get(expectedParams);
                                    expectedLen = optionLen;
                                    break;
                                case HdmiCecKeycode.CEC_KEYCODE_PAGE_DOWN /*56*/:
                                    expectedLen = optionLen;
                                    message = readAsciiString(packet, optionLen, false);
                                    break;
                                case (byte) 57:
                                    expectedLen = ENCAP_BOOTP;
                                    maxMessageSize = Short.valueOf(packet.getShort());
                                    break;
                                case (byte) 58:
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
                                    for (i = ENCAP_L2; i < optionLen; i += ENCAP_L3) {
                                        expectedLen += ENCAP_L3;
                                        byte throwaway = packet.get();
                                    }
                                    break;
                            }
                            if (expectedLen != optionLen) {
                                throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.DHCP_INVALID_OPTION_LENGTH, optionType), "Invalid length %d for option %d, expected %d", Integer.valueOf(optionLen), Byte.valueOf(optionType), Integer.valueOf(expectedLen));
                            }
                        } catch (BufferUnderflowException e) {
                            throw new ParseException(DhcpErrorEvent.errorCodeWithOption(DhcpErrorEvent.BUFFER_UNDERFLOW, optionType), "BufferUnderflowException", new Object[ENCAP_L2]);
                        }
                    } else {
                        continue;
                    }
                }
                DhcpPacket dhcpRequestPacket;
                switch (dhcpType) {
                    case INFINITE_LEASE /*-1*/:
                        throw new ParseException(DhcpErrorEvent.DHCP_NO_MSG_TYPE, "No DHCP message type option", new Object[ENCAP_L2]);
                    case ENCAP_L3 /*1*/:
                        newPacket = new DhcpDiscoverPacket(transactionId, secs, clientMac, broadcast);
                        break;
                    case ENCAP_BOOTP /*2*/:
                        newPacket = new DhcpOfferPacket(transactionId, secs, broadcast, inet4Address, clientIp, yourIp, clientMac);
                        break;
                    case H.REPORT_LOSING_FOCUS /*3*/:
                        dhcpRequestPacket = new DhcpRequestPacket(transactionId, secs, clientIp, clientMac, broadcast);
                        break;
                    case H.DO_TRAVERSAL /*4*/:
                        dhcpRequestPacket = new DhcpDeclinePacket(transactionId, secs, clientIp, yourIp, nextIp, relayIp, clientMac);
                        break;
                    case H.ADD_STARTING /*5*/:
                        newPacket = new DhcpAckPacket(transactionId, secs, broadcast, inet4Address, clientIp, yourIp, clientMac);
                        break;
                    case H.REMOVE_STARTING /*6*/:
                        dhcpRequestPacket = new DhcpNakPacket(transactionId, secs, clientIp, yourIp, nextIp, relayIp, clientMac);
                        break;
                    case H.REPORT_APPLICATION_TOKEN_WINDOWS /*8*/:
                        dhcpRequestPacket = new DhcpInformPacket(transactionId, secs, clientIp, yourIp, nextIp, relayIp, clientMac);
                        break;
                    default:
                        objArr = new Object[ENCAP_L3];
                        objArr[ENCAP_L2] = Byte.valueOf(dhcpType);
                        throw new ParseException(DhcpErrorEvent.DHCP_UNKNOWN_MSG_TYPE, "Unimplemented DHCP type %d", objArr);
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
                throw new ParseException(DhcpErrorEvent.BUFFER_UNDERFLOW, "BufferUnderflowException", new Object[ENCAP_L2]);
            }
        } catch (UnknownHostException e3) {
            objArr = new Object[ENCAP_L3];
            objArr[ENCAP_L2] = Arrays.toString(ipv4addr);
            throw new ParseException(DhcpErrorEvent.L3_INVALID_IP, "Invalid IPv4 address: %s", objArr);
        }
    }

    public static DhcpPacket decodeFullPacket(byte[] packet, int length, int pktType) throws ParseException {
        return decodeFullPacket(ByteBuffer.wrap(packet, ENCAP_L2, length).order(ByteOrder.BIG_ENDIAN), pktType);
    }

    public DhcpResults toDhcpResults() {
        Inet4Address ipAddress = this.mYourIp;
        if (ipAddress.equals(Inet4Address.ANY)) {
            ipAddress = this.mClientIp;
            if (ipAddress.equals(Inet4Address.ANY)) {
                return null;
            }
        }
        if (this.mSubnetMask != null) {
            try {
                int prefixLength = NetworkUtils.netmaskToPrefixLength(this.mSubnetMask);
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
                results.gateway = (InetAddress) this.mGateways.get(ENCAP_L2);
            }
            results.dnsServers.addAll(this.mDnsServers);
            results.domains = this.mDomainName;
            results.serverAddress = this.mServerIdentifier;
            results.vendorInfo = this.mVendorInfo;
            results.leaseDuration = this.mLeaseTime != null ? this.mLeaseTime.intValue() : INFINITE_LEASE;
            if (this.mMtu == null || (short) 1280 > this.mMtu.shortValue() || this.mMtu.shortValue() > (short) 1500) {
                i = ENCAP_L2;
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
        if (this.mLeaseTime == null || this.mLeaseTime.intValue() == INFINITE_LEASE) {
            return 0;
        }
        if (this.mLeaseTime.intValue() < 0 || this.mLeaseTime.intValue() >= MINIMUM_LEASE) {
            return (((long) this.mLeaseTime.intValue()) & 4294967295L) * 1000;
        }
        return 60000;
    }

    public static ByteBuffer buildDiscoverPacket(int encap, int transactionId, short secs, byte[] clientMac, boolean broadcast, byte[] expectedParams) {
        DhcpPacket pkt = new DhcpDiscoverPacket(transactionId, secs, clientMac, broadcast);
        pkt.mRequestedParams = expectedParams;
        return pkt.buildPacket(encap, DHCP_SERVER, DHCP_CLIENT);
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
        return pkt.buildPacket(encap, DHCP_CLIENT, DHCP_SERVER);
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
        return pkt.buildPacket(encap, DHCP_CLIENT, DHCP_SERVER);
    }

    public static ByteBuffer buildNakPacket(int encap, int transactionId, Inet4Address serverIpAddr, Inet4Address clientIpAddr, byte[] mac) {
        DhcpPacket pkt = new DhcpNakPacket(transactionId, (short) 0, clientIpAddr, serverIpAddr, serverIpAddr, serverIpAddr, mac);
        pkt.mMessage = "requested address not available";
        pkt.mRequestedIp = clientIpAddr;
        return pkt.buildPacket(encap, DHCP_CLIENT, DHCP_SERVER);
    }

    public static ByteBuffer buildRequestPacket(int encap, int transactionId, short secs, Inet4Address clientIp, boolean broadcast, byte[] clientMac, Inet4Address requestedIpAddress, Inet4Address serverIdentifier, byte[] requestedParams, String hostName) {
        DhcpPacket pkt = new DhcpRequestPacket(transactionId, secs, clientIp, clientMac, broadcast);
        pkt.mRequestedIp = requestedIpAddress;
        pkt.mServerIdentifier = serverIdentifier;
        pkt.mHostName = hostName;
        pkt.mRequestedParams = requestedParams;
        return pkt.buildPacket(encap, DHCP_SERVER, DHCP_CLIENT);
    }
}
