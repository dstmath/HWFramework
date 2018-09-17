package com.android.server.wifi.util;

import android.util.Log;
import com.android.server.wifi.HwWifiCHRConst;
import com.android.server.wifi.WifiNetworkScoreCache;
import com.android.server.wifi.WifiQualifiedNetworkSelector;
import com.android.server.wifi.anqp.CivicLocationElement;
import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.anqp.eap.EAP;
import com.android.server.wifi.scanner.BackgroundScanScheduler;
import com.google.protobuf.nano.Extension;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

public class FrameParser {
    private static final byte ARP_HWADDR_LEN_LEN = (byte) 1;
    private static final byte ARP_HWTYPE_LEN = (byte) 2;
    private static final byte ARP_OPCODE_REPLY = (byte) 2;
    private static final byte ARP_OPCODE_REQUEST = (byte) 1;
    private static final byte ARP_PROTOADDR_LEN_LEN = (byte) 1;
    private static final byte ARP_PROTOTYPE_LEN = (byte) 2;
    private static final short BOOTP_BOOT_FILENAME_LEN = (short) 128;
    private static final byte BOOTP_CLIENT_HWADDR_LEN = (byte) 16;
    private static final byte BOOTP_ELAPSED_SECONDS_LEN = (byte) 2;
    private static final byte BOOTP_FLAGS_LEN = (byte) 2;
    private static final byte BOOTP_HOPCOUNT_LEN = (byte) 1;
    private static final byte BOOTP_HWADDR_LEN_LEN = (byte) 1;
    private static final byte BOOTP_HWTYPE_LEN = (byte) 1;
    private static final byte BOOTP_MAGIC_COOKIE_LEN = (byte) 4;
    private static final byte BOOTP_OPCODE_LEN = (byte) 1;
    private static final byte BOOTP_SERVER_HOSTNAME_LEN = (byte) 64;
    private static final byte BOOTP_TRANSACTION_ID_LEN = (byte) 4;
    private static final byte BYTES_PER_OCT = (byte) 8;
    private static final byte BYTES_PER_QUAD = (byte) 4;
    private static final byte DHCP_MESSAGE_TYPE_ACK = (byte) 5;
    private static final byte DHCP_MESSAGE_TYPE_DECLINE = (byte) 4;
    private static final byte DHCP_MESSAGE_TYPE_DISCOVER = (byte) 1;
    private static final byte DHCP_MESSAGE_TYPE_INFORM = (byte) 8;
    private static final byte DHCP_MESSAGE_TYPE_NAK = (byte) 6;
    private static final byte DHCP_MESSAGE_TYPE_OFFER = (byte) 2;
    private static final byte DHCP_MESSAGE_TYPE_RELEASE = (byte) 7;
    private static final byte DHCP_MESSAGE_TYPE_REQUEST = (byte) 3;
    private static final short DHCP_OPTION_TAG_END = (short) 255;
    private static final short DHCP_OPTION_TAG_MESSAGE_TYPE = (short) 53;
    private static final short DHCP_OPTION_TAG_PAD = (short) 0;
    private static final byte EAPOL_KEY_DESCRIPTOR_RSN_KEY = (byte) 2;
    private static final byte EAPOL_LENGTH_LEN = (byte) 2;
    private static final byte EAPOL_TYPE_KEY = (byte) 3;
    private static final int ETHERNET_DST_MAC_ADDR_LEN = 6;
    private static final int ETHERNET_SRC_MAC_ADDR_LEN = 6;
    private static final short ETHERTYPE_ARP = (short) 2054;
    private static final short ETHERTYPE_EAPOL = (short) -30578;
    private static final short ETHERTYPE_IP_V4 = (short) 2048;
    private static final short ETHERTYPE_IP_V6 = (short) -31011;
    private static final int HTTPS_PORT = 443;
    private static final Set<Integer> HTTP_PORTS = null;
    private static final byte ICMP_TYPE_DEST_UNREACHABLE = (byte) 3;
    private static final byte ICMP_TYPE_ECHO_REPLY = (byte) 0;
    private static final byte ICMP_TYPE_ECHO_REQUEST = (byte) 8;
    private static final byte ICMP_TYPE_REDIRECT = (byte) 5;
    private static final short ICMP_V6_TYPE_ECHO_REPLY = (short) 129;
    private static final short ICMP_V6_TYPE_ECHO_REQUEST = (short) 128;
    private static final short ICMP_V6_TYPE_MULTICAST_LISTENER_DISCOVERY = (short) 143;
    private static final short ICMP_V6_TYPE_NEIGHBOR_ADVERTISEMENT = (short) 136;
    private static final short ICMP_V6_TYPE_NEIGHBOR_SOLICITATION = (short) 135;
    private static final short ICMP_V6_TYPE_ROUTER_ADVERTISEMENT = (short) 134;
    private static final short ICMP_V6_TYPE_ROUTER_SOLICITATION = (short) 133;
    private static final byte IEEE_80211_ADDR1_LEN = (byte) 6;
    private static final byte IEEE_80211_ADDR2_LEN = (byte) 6;
    private static final byte IEEE_80211_ADDR3_LEN = (byte) 6;
    private static final short IEEE_80211_AUTH_ALG_FAST_BSS_TRANSITION = (short) 2;
    private static final short IEEE_80211_AUTH_ALG_OPEN = (short) 0;
    private static final short IEEE_80211_AUTH_ALG_SHARED_KEY = (short) 1;
    private static final short IEEE_80211_AUTH_ALG_SIMUL_AUTH_OF_EQUALS = (short) 3;
    private static final byte IEEE_80211_CAPABILITY_INFO_LEN = (byte) 2;
    private static final byte IEEE_80211_DURATION_LEN = (byte) 2;
    private static final byte IEEE_80211_FRAME_CTRL_FLAG_ORDER = Byte.MIN_VALUE;
    private static final byte IEEE_80211_FRAME_CTRL_SUBTYPE_ASSOC_REQ = (byte) 0;
    private static final byte IEEE_80211_FRAME_CTRL_SUBTYPE_ASSOC_RESP = (byte) 1;
    private static final byte IEEE_80211_FRAME_CTRL_SUBTYPE_AUTH = (byte) 11;
    private static final byte IEEE_80211_FRAME_CTRL_SUBTYPE_PROBE_REQ = (byte) 4;
    private static final byte IEEE_80211_FRAME_CTRL_SUBTYPE_PROBE_RESP = (byte) 5;
    private static final byte IEEE_80211_FRAME_CTRL_TYPE_MGMT = (byte) 0;
    private static final byte IEEE_80211_HT_CONTROL_LEN = (byte) 4;
    private static final byte IEEE_80211_SEQUENCE_CONTROL_LEN = (byte) 2;
    private static final byte IP_PROTO_ICMP = (byte) 1;
    private static final byte IP_PROTO_TCP = (byte) 6;
    private static final byte IP_PROTO_UDP = (byte) 17;
    private static final byte IP_V4_ADDR_LEN = (byte) 4;
    private static final byte IP_V4_DSCP_AND_ECN_LEN = (byte) 1;
    private static final byte IP_V4_DST_ADDR_LEN = (byte) 4;
    private static final byte IP_V4_FLAGS_AND_FRAG_OFFSET_LEN = (byte) 2;
    private static final byte IP_V4_HEADER_CHECKSUM_LEN = (byte) 2;
    private static final byte IP_V4_ID_LEN = (byte) 2;
    private static final byte IP_V4_IHL_BYTE_MASK = (byte) 15;
    private static final byte IP_V4_SRC_ADDR_LEN = (byte) 4;
    private static final byte IP_V4_TOTAL_LEN_LEN = (byte) 2;
    private static final byte IP_V4_TTL_LEN = (byte) 1;
    private static final byte IP_V4_VERSION_BYTE_MASK = (byte) -16;
    private static final byte IP_V6_ADDR_LEN = (byte) 16;
    private static final byte IP_V6_HEADER_TYPE_HOP_BY_HOP_OPTION = (byte) 0;
    private static final byte IP_V6_HEADER_TYPE_ICMP_V6 = (byte) 58;
    private static final byte IP_V6_HOP_LIMIT_LEN = (byte) 1;
    private static final byte IP_V6_PAYLOAD_LENGTH_LEN = (byte) 2;
    private static final String TAG = "FrameParser";
    private static final byte TCP_SRC_PORT_LEN = (byte) 2;
    private static final byte UDP_CHECKSUM_LEN = (byte) 2;
    private static final byte UDP_PORT_BOOTPC = (byte) 68;
    private static final byte UDP_PORT_BOOTPS = (byte) 67;
    private static final byte UDP_PORT_NTP = (byte) 123;
    private static final byte WPA_KEYLEN_LEN = (byte) 2;
    private static final byte WPA_KEY_IDENTIFIER_LEN = (byte) 8;
    private static final short WPA_KEY_INFO_FLAG_INSTALL = (short) 64;
    private static final short WPA_KEY_INFO_FLAG_MIC = (short) 256;
    private static final short WPA_KEY_INFO_FLAG_PAIRWISE = (short) 8;
    private static final byte WPA_KEY_IV_LEN = (byte) 16;
    private static final byte WPA_KEY_MIC_LEN = (byte) 16;
    private static final byte WPA_KEY_NONCE_LEN = (byte) 32;
    private static final byte WPA_KEY_RECEIVE_SEQUENCE_COUNTER_LEN = (byte) 8;
    private static final byte WPA_REPLAY_COUNTER_LEN = (byte) 8;
    public String mMostSpecificProtocolString;
    public String mResultString;
    public String mTypeString;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.util.FrameParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.util.FrameParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.util.FrameParser.<clinit>():void");
    }

    public FrameParser(byte frameType, byte[] frameBytes) {
        this.mMostSpecificProtocolString = "N/A";
        this.mTypeString = "N/A";
        this.mResultString = "N/A";
        try {
            ByteBuffer frameBuffer = ByteBuffer.wrap(frameBytes);
            frameBuffer.order(ByteOrder.BIG_ENDIAN);
            if (frameType == 1) {
                parseEthernetFrame(frameBuffer);
            } else if (frameType == 2) {
                parseManagementFrame(frameBuffer);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Dissection aborted mid-frame: " + e);
        }
    }

    private static short getUnsignedByte(ByteBuffer data) {
        return (short) (data.get() & Constants.BYTE_MASK);
    }

    private static int getUnsignedShort(ByteBuffer data) {
        return data.getShort() & Constants.SHORT_MASK;
    }

    private void parseEthernetFrame(ByteBuffer data) {
        this.mMostSpecificProtocolString = "Ethernet";
        data.position((data.position() + ETHERNET_SRC_MAC_ADDR_LEN) + ETHERNET_SRC_MAC_ADDR_LEN);
        switch (data.getShort()) {
            case (short) -31011:
                parseIpv6Packet(data);
            case (short) -30578:
                parseEapolPacket(data);
            case (short) 2048:
                parseIpv4Packet(data);
            case (short) 2054:
                parseArpPacket(data);
            default:
        }
    }

    private void parseIpv4Packet(ByteBuffer data) {
        this.mMostSpecificProtocolString = "IPv4";
        data.mark();
        byte versionAndHeaderLen = data.get();
        int version = (versionAndHeaderLen & -16) >> 4;
        if (version != 4) {
            Log.e(TAG, "IPv4 header: Unrecognized protocol version " + version);
            return;
        }
        data.position(((((data.position() + 1) + 2) + 2) + 2) + 1);
        short protocolNumber = getUnsignedByte(data);
        data.position(((data.position() + 2) + 4) + 4);
        int headerLen = (versionAndHeaderLen & 15) * 4;
        data.reset();
        data.position(data.position() + headerLen);
        switch (protocolNumber) {
            case Extension.TYPE_DOUBLE /*1*/:
                parseIcmpPacket(data);
                break;
            case ETHERNET_SRC_MAC_ADDR_LEN /*6*/:
                parseTcpPacket(data);
                break;
            case Extension.TYPE_SINT32 /*17*/:
                parseUdpPacket(data);
                break;
        }
    }

    private void parseTcpPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "TCP";
        data.position(data.position() + 2);
        int dstPort = getUnsignedShort(data);
        if (dstPort == HTTPS_PORT) {
            this.mTypeString = "HTTPS";
        } else if (HTTP_PORTS.contains(Integer.valueOf(dstPort))) {
            this.mTypeString = "HTTP";
        }
    }

    private void parseUdpPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "UDP";
        int srcPort = getUnsignedShort(data);
        int dstPort = getUnsignedShort(data);
        int length = getUnsignedShort(data);
        data.position(data.position() + 2);
        if ((srcPort == 68 && dstPort == 67) || (srcPort == 67 && dstPort == 68)) {
            parseDhcpPacket(data);
        } else if (srcPort == 123 || dstPort == 123) {
            this.mMostSpecificProtocolString = "NTP";
        }
    }

    private void parseDhcpPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "DHCP";
        data.position((((((((((((data.position() + 1) + 1) + 1) + 1) + 4) + 2) + 2) + 16) + 16) + 64) + CivicLocationElement.SCRIPT) + 4);
        while (data.remaining() > 0) {
            short dhcpOptionTag = getUnsignedByte(data);
            if (dhcpOptionTag != (short) 0) {
                if (dhcpOptionTag != DHCP_OPTION_TAG_END) {
                    short dhcpOptionLen = getUnsignedByte(data);
                    switch (dhcpOptionTag) {
                        case EAP.EAP_EKE /*53*/:
                            if (dhcpOptionLen != IEEE_80211_AUTH_ALG_SHARED_KEY) {
                                Log.e(TAG, "DHCP option len: " + dhcpOptionLen + " (expected |1|)");
                                return;
                            } else {
                                this.mTypeString = decodeDhcpMessageType(getUnsignedByte(data));
                                return;
                            }
                        default:
                            data.position(data.position() + dhcpOptionLen);
                            break;
                    }
                }
            }
        }
    }

    private static String decodeDhcpMessageType(short messageType) {
        switch (messageType) {
            case Extension.TYPE_DOUBLE /*1*/:
                return "Discover";
            case Extension.TYPE_FLOAT /*2*/:
                return "Offer";
            case Extension.TYPE_INT64 /*3*/:
                return "Request";
            case Extension.TYPE_UINT64 /*4*/:
                return "Decline";
            case Extension.TYPE_INT32 /*5*/:
                return "Ack";
            case ETHERNET_SRC_MAC_ADDR_LEN /*6*/:
                return "Nak";
            case Extension.TYPE_FIXED32 /*7*/:
                return "Release";
            case Extension.TYPE_BOOL /*8*/:
                return "Inform";
            default:
                return "Unknown type " + messageType;
        }
    }

    private void parseIcmpPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "ICMP";
        short messageType = getUnsignedByte(data);
        switch (messageType) {
            case ApConfigUtil.SUCCESS /*0*/:
                this.mTypeString = "Echo Reply";
            case Extension.TYPE_INT64 /*3*/:
                this.mTypeString = "Destination Unreachable";
            case Extension.TYPE_INT32 /*5*/:
                this.mTypeString = "Redirect";
            case Extension.TYPE_BOOL /*8*/:
                this.mTypeString = "Echo Request";
            default:
                this.mTypeString = "Type " + messageType;
        }
    }

    private void parseArpPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "ARP";
        data.position((((data.position() + 2) + 2) + 1) + 1);
        int opCode = getUnsignedShort(data);
        switch (opCode) {
            case Extension.TYPE_DOUBLE /*1*/:
                this.mTypeString = "Request";
            case Extension.TYPE_FLOAT /*2*/:
                this.mTypeString = "Reply";
            default:
                this.mTypeString = "Operation " + opCode;
        }
    }

    private void parseIpv6Packet(ByteBuffer data) {
        this.mMostSpecificProtocolString = "IPv6";
        int version = (-268435456 & data.getInt()) >> 28;
        if (version != ETHERNET_SRC_MAC_ADDR_LEN) {
            Log.e(TAG, "IPv6 header: invalid IP version " + version);
            return;
        }
        data.position(data.position() + 2);
        short nextHeaderType = getUnsignedByte(data);
        data.position((data.position() + 1) + 32);
        while (nextHeaderType == (short) 0) {
            data.mark();
            nextHeaderType = getUnsignedByte(data);
            int thisHeaderLen = (getUnsignedByte(data) + 1) * 8;
            data.reset();
            data.position(data.position() + thisHeaderLen);
        }
        switch (nextHeaderType) {
            case (short) 58:
                parseIcmpV6Packet(data);
            default:
                this.mTypeString = "Option/Protocol " + nextHeaderType;
        }
    }

    private void parseIcmpV6Packet(ByteBuffer data) {
        this.mMostSpecificProtocolString = "ICMPv6";
        short icmpV6Type = getUnsignedByte(data);
        switch (icmpV6Type) {
            case CivicLocationElement.SCRIPT /*128*/:
                this.mTypeString = "Echo Request";
            case (short) 129:
                this.mTypeString = "Echo Reply";
            case (short) 133:
                this.mTypeString = "Router Solicitation";
            case (short) 134:
                this.mTypeString = "Router Advertisement";
            case (short) 135:
                this.mTypeString = "Neighbor Solicitation";
            case (short) 136:
                this.mTypeString = "Neighbor Advertisement";
            case (short) 143:
                this.mTypeString = "MLDv2 report";
            default:
                this.mTypeString = "Type " + icmpV6Type;
        }
    }

    private void parseEapolPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "EAPOL";
        short eapolVersion = getUnsignedByte(data);
        if (eapolVersion < IEEE_80211_AUTH_ALG_SHARED_KEY || eapolVersion > IEEE_80211_AUTH_ALG_FAST_BSS_TRANSITION) {
            Log.e(TAG, "Unrecognized EAPOL version " + eapolVersion);
            return;
        }
        short eapolType = getUnsignedByte(data);
        if (eapolType != IEEE_80211_AUTH_ALG_SIMUL_AUTH_OF_EQUALS) {
            Log.e(TAG, "Unrecognized EAPOL type " + eapolType);
            return;
        }
        data.position(data.position() + 2);
        short eapolKeyDescriptorType = getUnsignedByte(data);
        if (eapolKeyDescriptorType != IEEE_80211_AUTH_ALG_FAST_BSS_TRANSITION) {
            Log.e(TAG, "Unrecognized key descriptor " + eapolKeyDescriptorType);
            return;
        }
        short wpaKeyInfo = data.getShort();
        if ((wpaKeyInfo & 8) == 0) {
            this.mTypeString = "Group Key";
        } else {
            this.mTypeString = "Pairwise Key";
        }
        if ((wpaKeyInfo & Constants.ANQP_QUERY_LIST) == 0) {
            this.mTypeString += " message 1/4";
        } else if ((wpaKeyInfo & 64) != 0) {
            this.mTypeString += " message 3/4";
        } else {
            data.position(((((((data.position() + 2) + 8) + 32) + 16) + 8) + 8) + 16);
            if (getUnsignedShort(data) > 0) {
                this.mTypeString += " message 2/4";
            } else {
                this.mTypeString += " message 4/4";
            }
        }
    }

    private static byte parseIeee80211FrameCtrlVersion(byte b) {
        return (byte) (b & 3);
    }

    private static byte parseIeee80211FrameCtrlType(byte b) {
        return (byte) ((b & 12) >> 2);
    }

    private static byte parseIeee80211FrameCtrlSubtype(byte b) {
        return (byte) ((b & 240) >> 4);
    }

    private void parseManagementFrame(ByteBuffer data) {
        data.order(ByteOrder.LITTLE_ENDIAN);
        this.mMostSpecificProtocolString = "802.11 Mgmt";
        byte frameControlVersionTypeSubtype = data.get();
        byte ieee80211Version = parseIeee80211FrameCtrlVersion(frameControlVersionTypeSubtype);
        if (ieee80211Version != null) {
            Log.e(TAG, "Unrecognized 802.11 version " + ieee80211Version);
            return;
        }
        byte ieee80211FrameType = parseIeee80211FrameCtrlType(frameControlVersionTypeSubtype);
        if (ieee80211FrameType != null) {
            Log.e(TAG, "Unexpected frame type " + ieee80211FrameType);
            return;
        }
        byte frameControlFlags = data.get();
        data.position(((((data.position() + 2) + ETHERNET_SRC_MAC_ADDR_LEN) + ETHERNET_SRC_MAC_ADDR_LEN) + ETHERNET_SRC_MAC_ADDR_LEN) + 2);
        if ((frameControlFlags & WifiNetworkScoreCache.INVALID_NETWORK_SCORE) != 0) {
            data.position(data.position() + 4);
        }
        byte ieee80211FrameSubtype = parseIeee80211FrameCtrlSubtype(frameControlVersionTypeSubtype);
        switch (ieee80211FrameSubtype) {
            case ApConfigUtil.SUCCESS /*0*/:
                this.mTypeString = "Association Request";
            case Extension.TYPE_DOUBLE /*1*/:
                this.mTypeString = "Association Response";
                parseAssociationResponse(data);
            case Extension.TYPE_UINT64 /*4*/:
                this.mTypeString = "Probe Request";
            case Extension.TYPE_INT32 /*5*/:
                this.mTypeString = "Probe Response";
            case Extension.TYPE_MESSAGE /*11*/:
                this.mTypeString = "Authentication";
                parseAuthenticationFrame(data);
            default:
                this.mTypeString = "Unexpected subtype " + ieee80211FrameSubtype;
        }
    }

    private void parseAssociationResponse(ByteBuffer data) {
        data.position(data.position() + 2);
        short resultCode = data.getShort();
        this.mResultString = String.format("%d: %s", new Object[]{Short.valueOf(resultCode), decodeIeee80211StatusCode(resultCode)});
    }

    private void parseAuthenticationFrame(ByteBuffer data) {
        short algorithm = data.getShort();
        short sequenceNum = data.getShort();
        boolean hasResultCode = false;
        switch (algorithm) {
            case ApConfigUtil.SUCCESS /*0*/:
            case Extension.TYPE_DOUBLE /*1*/:
                if (sequenceNum == IEEE_80211_AUTH_ALG_FAST_BSS_TRANSITION) {
                    hasResultCode = true;
                    break;
                }
                break;
            case Extension.TYPE_FLOAT /*2*/:
                if (sequenceNum == IEEE_80211_AUTH_ALG_FAST_BSS_TRANSITION || sequenceNum == (short) 4) {
                    hasResultCode = true;
                    break;
                }
            case Extension.TYPE_INT64 /*3*/:
                hasResultCode = true;
                break;
        }
        if (hasResultCode) {
            short resultCode = data.getShort();
            this.mResultString = String.format("%d: %s", new Object[]{Short.valueOf(resultCode), decodeIeee80211StatusCode(resultCode)});
        }
    }

    private String decodeIeee80211StatusCode(short statusCode) {
        switch (statusCode) {
            case ApConfigUtil.SUCCESS /*0*/:
                return "Success";
            case Extension.TYPE_DOUBLE /*1*/:
                return "Unspecified failure";
            case Extension.TYPE_FLOAT /*2*/:
                return "TDLS wakeup schedule rejected; alternative provided";
            case Extension.TYPE_INT64 /*3*/:
                return "TDLS wakeup schedule rejected";
            case Extension.TYPE_UINT64 /*4*/:
                return "Reserved";
            case Extension.TYPE_INT32 /*5*/:
                return "Security disabled";
            case ETHERNET_SRC_MAC_ADDR_LEN /*6*/:
                return "Unacceptable lifetime";
            case Extension.TYPE_FIXED32 /*7*/:
                return "Not in same BSS";
            case Extension.TYPE_BOOL /*8*/:
            case Extension.TYPE_STRING /*9*/:
                return "Reserved";
            case Extension.TYPE_GROUP /*10*/:
                return "Capabilities mismatch";
            case Extension.TYPE_MESSAGE /*11*/:
                return "Reassociation denied; could not confirm association exists";
            case Extension.TYPE_BYTES /*12*/:
                return "Association denied for reasons outside standard";
            case Extension.TYPE_UINT32 /*13*/:
                return "Unsupported authentication algorithm";
            case Extension.TYPE_ENUM /*14*/:
                return "Authentication sequence number of of sequence";
            case Extension.TYPE_SFIXED32 /*15*/:
                return "Authentication challenge failure";
            case Extension.TYPE_SFIXED64 /*16*/:
                return "Authentication timeout";
            case Extension.TYPE_SINT32 /*17*/:
                return "Association denied; too many STAs";
            case Extension.TYPE_SINT64 /*18*/:
                return "Association denied; must support BSSBasicRateSet";
            case CivicLocationElement.HOUSE_NUMBER /*19*/:
                return "Association denied; must support short preamble";
            case CivicLocationElement.HOUSE_NUMBER_SUFFIX /*20*/:
                return "Association denied; must support PBCC";
            case EAP.EAP_TTLS /*21*/:
                return "Association denied; must support channel agility";
            case CivicLocationElement.ADDITIONAL_LOCATION /*22*/:
                return "Association rejected; must support spectrum management";
            case EAP.EAP_AKA /*23*/:
                return "Association rejected; unacceptable power capability";
            case EAP.EAP_3Com /*24*/:
                return "Association rejected; unacceptable supported channels";
            case CivicLocationElement.BUILDING /*25*/:
                return "Association denied; must support short slot time";
            case EAP.EAP_MSCHAPv2 /*26*/:
                return "Association denied; must support DSSS-OFDM";
            case CivicLocationElement.FLOOR /*27*/:
                return "Association denied; must support HT";
            case CivicLocationElement.ROOM /*28*/:
                return "R0 keyholder unreachable (802.11r)";
            case EAP.EAP_PEAP /*29*/:
                return "Association denied; must support PCO transition time";
            case CivicLocationElement.POSTAL_COMMUNITY /*30*/:
                return "Refused temporarily";
            case CivicLocationElement.PO_BOX /*31*/:
                return "Robust management frame policy violation";
            case BackgroundScanScheduler.DEFAULT_MAX_AP_PER_SCAN /*32*/:
                return "Unspecified QoS failure";
            case CivicLocationElement.SEAT_DESK /*33*/:
                return "Association denied; insufficient bandwidth for QoS";
            case CivicLocationElement.PRIMARY_ROAD /*34*/:
                return "Association denied; poor channel";
            case EAP.EAP_ActiontecWireless /*35*/:
                return "Association denied; must support QoS";
            case CivicLocationElement.BRANCH_ROAD /*36*/:
                return "Reserved";
            case CivicLocationElement.SUB_BRANCH_ROAD /*37*/:
                return "Declined";
            case EAP.EAP_HTTPDigest /*38*/:
                return "Invalid parameters";
            case CivicLocationElement.STREET_NAME_POST_MOD /*39*/:
                return "TS cannot be honored; changes suggested";
            case WifiQualifiedNetworkSelector.PASSPOINT_SECURITY_AWARD /*40*/:
                return "Invalid element";
            case EAP.EAP_SPEKE /*41*/:
                return "Invalid group cipher";
            case EAP.EAP_MOBAC /*42*/:
                return "Invalid pairwise cipher";
            case EAP.EAP_FAST /*43*/:
                return "Invalid auth/key mgmt proto (AKMP)";
            case EAP.EAP_ZLXEAP /*44*/:
                return "Unsupported RSNE version";
            case EAP.EAP_Link /*45*/:
                return "Invalid RSNE capabilities";
            case EAP.EAP_PAX /*46*/:
                return "Cipher suite rejected by policy";
            case EAP.EAP_PSK /*47*/:
                return "TS cannot be honored now; try again later";
            case EAP.EAP_SAKE /*48*/:
                return "Direct link rejected by policy";
            case EAP.EAP_IKEv2 /*49*/:
                return "Destination STA not in BSS";
            case EAP.EAP_AKAPrim /*50*/:
                return "Destination STA not configured for QoS";
            case EAP.EAP_GPSK /*51*/:
                return "Association denied; listen interval too large";
            case EAP.EAP_PWD /*52*/:
                return "Invalid fast transition action frame count";
            case EAP.EAP_EKE /*53*/:
                return "Invalid PMKID";
            case (short) 54:
                return "Invalid MDE";
            case EAP.EAP_TEAP /*55*/:
                return "Invalid FTE";
            case (short) 56:
                return "Unsupported TCLAS";
            case (short) 57:
                return "Requested TCLAS exceeds resources";
            case (short) 58:
                return "TS cannot be honored; try another BSS";
            case (short) 59:
                return "GAS Advertisement not supported";
            case (short) 60:
                return "No outstanding GAS request";
            case (short) 61:
                return "No query response from GAS server";
            case (short) 62:
                return "GAS query timeout";
            case (short) 63:
                return "GAS response too large";
            case (short) 64:
                return "Home network does not support request";
            case (short) 65:
                return "Advertisement server unreachable";
            case (short) 66:
                return "Reserved";
            case (short) 67:
                return "Rejected for SSP permissions";
            case (short) 68:
                return "Authentication required";
            case (short) 69:
            case (short) 70:
            case (short) 71:
                return "Reserved";
            case (short) 72:
                return "Invalid RSNE contents";
            case (short) 73:
                return "U-APSD coexistence unsupported";
            case (short) 74:
                return "Requested U-APSD coex mode unsupported";
            case (short) 75:
                return "Requested parameter unsupported with U-APSD coex";
            case (short) 76:
                return "Auth rejected; anti-clogging token required";
            case (short) 77:
                return "Auth rejected; offered group is not supported";
            case (short) 78:
                return "Cannot find alternative TBTT";
            case (short) 79:
                return "Transmission failure";
            case WifiQualifiedNetworkSelector.SECURITY_AWARD /*80*/:
                return "Requested TCLAS not supported";
            case HwWifiCHRConst.WIFI_CLOSE_FAILED /*81*/:
                return "TCLAS resources exhausted";
            case HwWifiCHRConst.WIFI_CONNECT_AUTH_FAILED /*82*/:
                return "Rejected with suggested BSS transition";
            case HwWifiCHRConst.WIFI_CONNECT_ASSOC_FAILED /*83*/:
                return "Reserved";
            case HwWifiCHRConst.WIFI_CONNECT_DHCP_FAILED /*84*/:
            case WifiQualifiedNetworkSelector.RSSI_SCORE_OFFSET /*85*/:
            case HwWifiCHRConst.WIFI_SCAN_FAILED /*86*/:
            case HwWifiCHRConst.WIFI_ACCESS_INTERNET_FAILED /*87*/:
            case (short) 88:
            case (short) 89:
            case HwWifiCHRConst.WIFI_OPEN_FAILED_EX /*90*/:
            case HwWifiCHRConst.WIFI_CLOSE_FAILED_EX /*91*/:
                return "<unspecified>";
            case HwWifiCHRConst.WIFI_CONNECT_AUTH_FAILED_EX /*92*/:
                return "Refused due to external reason";
            case HwWifiCHRConst.WIFI_CONNECT_ASSOC_FAILED_EX /*93*/:
                return "Refused; AP out of memory";
            case HwWifiCHRConst.WIFI_CONNECT_DHCP_FAILED_EX /*94*/:
                return "Refused; emergency services not supported";
            case HwWifiCHRConst.WIFI_ABNORMAL_DISCONNECT_EX /*95*/:
                return "GAS query response outstanding";
            case HwWifiCHRConst.WIFI_SCAN_FAILED_EX /*96*/:
            case HwWifiCHRConst.WIFI_ACCESS_INTERNET_FAILED_EX /*97*/:
            case HwWifiCHRConst.WIFI_STATUS_CHANGEDBY_APK /*98*/:
            case (short) 99:
                return "Reserved";
            case (short) 100:
                return "Failed; reservation conflict";
            case HwWifiCHRConst.WIFI_USER_CONNECT /*101*/:
                return "Failed; exceeded MAF limit";
            case HwWifiCHRConst.WIFI_ACCESS_WEB_SLOWLY /*102*/:
                return "Failed; exceeded MCCA track limit";
            default:
                return "Reserved";
        }
    }
}
