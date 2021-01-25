package com.android.server.wifi.util;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.util.Log;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

public class FrameParser {
    private static final byte ARP_HWADDR_LEN_LEN = 1;
    private static final byte ARP_HWTYPE_LEN = 2;
    private static final byte ARP_OPCODE_REPLY = 2;
    private static final byte ARP_OPCODE_REQUEST = 1;
    private static final byte ARP_PROTOADDR_LEN_LEN = 1;
    private static final byte ARP_PROTOTYPE_LEN = 2;
    private static final short BOOTP_BOOT_FILENAME_LEN = 128;
    private static final byte BOOTP_CLIENT_HWADDR_LEN = 16;
    private static final byte BOOTP_ELAPSED_SECONDS_LEN = 2;
    private static final byte BOOTP_FLAGS_LEN = 2;
    private static final byte BOOTP_HOPCOUNT_LEN = 1;
    private static final byte BOOTP_HWADDR_LEN_LEN = 1;
    private static final byte BOOTP_HWTYPE_LEN = 1;
    private static final byte BOOTP_MAGIC_COOKIE_LEN = 4;
    private static final byte BOOTP_OPCODE_LEN = 1;
    private static final byte BOOTP_SERVER_HOSTNAME_LEN = 64;
    private static final byte BOOTP_TRANSACTION_ID_LEN = 4;
    private static final byte BYTES_PER_OCT = 8;
    private static final byte BYTES_PER_QUAD = 4;
    private static final byte DHCP_MESSAGE_TYPE_ACK = 5;
    private static final byte DHCP_MESSAGE_TYPE_DECLINE = 4;
    private static final byte DHCP_MESSAGE_TYPE_DISCOVER = 1;
    private static final byte DHCP_MESSAGE_TYPE_INFORM = 8;
    private static final byte DHCP_MESSAGE_TYPE_NAK = 6;
    private static final byte DHCP_MESSAGE_TYPE_OFFER = 2;
    private static final byte DHCP_MESSAGE_TYPE_RELEASE = 7;
    private static final byte DHCP_MESSAGE_TYPE_REQUEST = 3;
    private static final short DHCP_OPTION_TAG_END = 255;
    private static final short DHCP_OPTION_TAG_MESSAGE_TYPE = 53;
    private static final short DHCP_OPTION_TAG_PAD = 0;
    private static final byte EAPOL_KEY_DESCRIPTOR_RSN_KEY = 2;
    private static final byte EAPOL_LENGTH_LEN = 2;
    private static final byte EAPOL_TYPE_KEY = 3;
    private static final int ETHERNET_DST_MAC_ADDR_LEN = 6;
    private static final int ETHERNET_SRC_MAC_ADDR_LEN = 6;
    private static final short ETHERTYPE_ARP = 2054;
    private static final short ETHERTYPE_EAPOL = -30578;
    private static final short ETHERTYPE_IP_V4 = 2048;
    private static final short ETHERTYPE_IP_V6 = -31011;
    private static final int HTTPS_PORT = 443;
    private static final Set<Integer> HTTP_PORTS = new HashSet();
    private static final byte ICMP_TYPE_DEST_UNREACHABLE = 3;
    private static final byte ICMP_TYPE_ECHO_REPLY = 0;
    private static final byte ICMP_TYPE_ECHO_REQUEST = 8;
    private static final byte ICMP_TYPE_REDIRECT = 5;
    private static final short ICMP_V6_TYPE_ECHO_REPLY = 129;
    private static final short ICMP_V6_TYPE_ECHO_REQUEST = 128;
    private static final short ICMP_V6_TYPE_MULTICAST_LISTENER_DISCOVERY = 143;
    private static final short ICMP_V6_TYPE_NEIGHBOR_ADVERTISEMENT = 136;
    private static final short ICMP_V6_TYPE_NEIGHBOR_SOLICITATION = 135;
    private static final short ICMP_V6_TYPE_ROUTER_ADVERTISEMENT = 134;
    private static final short ICMP_V6_TYPE_ROUTER_SOLICITATION = 133;
    private static final byte IEEE_80211_ADDR1_LEN = 6;
    private static final byte IEEE_80211_ADDR2_LEN = 6;
    private static final byte IEEE_80211_ADDR3_LEN = 6;
    private static final short IEEE_80211_AUTH_ALG_FAST_BSS_TRANSITION = 2;
    private static final short IEEE_80211_AUTH_ALG_OPEN = 0;
    private static final short IEEE_80211_AUTH_ALG_SHARED_KEY = 1;
    private static final short IEEE_80211_AUTH_ALG_SIMUL_AUTH_OF_EQUALS = 3;
    private static final byte IEEE_80211_CAPABILITY_INFO_LEN = 2;
    private static final byte IEEE_80211_DURATION_LEN = 2;
    private static final byte IEEE_80211_FRAME_FLAG_ORDER = Byte.MIN_VALUE;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT = 0;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_ACTION = 13;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_ACTION_NO_ACK = 14;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_ASSOC_REQ = 0;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_ASSOC_RESP = 1;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_ATIM = 9;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_AUTH = 11;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_BEACON = 8;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_DEAUTH = 12;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_DISASSOC = 10;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_PROBE_REQ = 4;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_PROBE_RESP = 5;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_REASSOC_REQ = 2;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_REASSOC_RESP = 3;
    private static final byte IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_TIMING_AD = 6;
    private static final byte IEEE_80211_HT_CONTROL_LEN = 4;
    private static final byte IEEE_80211_SEQUENCE_CONTROL_LEN = 2;
    private static final byte IP_PROTO_ICMP = 1;
    private static final byte IP_PROTO_TCP = 6;
    private static final byte IP_PROTO_UDP = 17;
    private static final byte IP_V4_ADDR_LEN = 4;
    private static final byte IP_V4_DSCP_AND_ECN_LEN = 1;
    private static final byte IP_V4_DST_ADDR_LEN = 4;
    private static final byte IP_V4_FLAGS_AND_FRAG_OFFSET_LEN = 2;
    private static final byte IP_V4_HEADER_CHECKSUM_LEN = 2;
    private static final byte IP_V4_ID_LEN = 2;
    private static final byte IP_V4_IHL_BYTE_MASK = 15;
    private static final byte IP_V4_SRC_ADDR_LEN = 4;
    private static final byte IP_V4_TOTAL_LEN_LEN = 2;
    private static final byte IP_V4_TTL_LEN = 1;
    private static final byte IP_V4_VERSION_BYTE_MASK = -16;
    private static final byte IP_V6_ADDR_LEN = 16;
    private static final byte IP_V6_HEADER_TYPE_HOP_BY_HOP_OPTION = 0;
    private static final byte IP_V6_HEADER_TYPE_ICMP_V6 = 58;
    private static final byte IP_V6_HOP_LIMIT_LEN = 1;
    private static final byte IP_V6_PAYLOAD_LENGTH_LEN = 2;
    private static final String TAG = "FrameParser";
    private static final byte TCP_SRC_PORT_LEN = 2;
    private static final byte UDP_CHECKSUM_LEN = 2;
    private static final byte UDP_PORT_BOOTPC = 68;
    private static final byte UDP_PORT_BOOTPS = 67;
    private static final byte UDP_PORT_NTP = 123;
    private static final byte WPA_KEYLEN_LEN = 2;
    private static final byte WPA_KEY_IDENTIFIER_LEN = 8;
    private static final short WPA_KEY_INFO_FLAG_INSTALL = 64;
    private static final short WPA_KEY_INFO_FLAG_MIC = 256;
    private static final short WPA_KEY_INFO_FLAG_PAIRWISE = 8;
    private static final byte WPA_KEY_IV_LEN = 16;
    private static final byte WPA_KEY_MIC_LEN = 16;
    private static final byte WPA_KEY_NONCE_LEN = 32;
    private static final byte WPA_KEY_RECEIVE_SEQUENCE_COUNTER_LEN = 8;
    private static final byte WPA_REPLAY_COUNTER_LEN = 8;
    public String mMostSpecificProtocolString = "N/A";
    public String mResultString = "N/A";
    public String mTypeString = "N/A";

    public FrameParser(byte frameType, byte[] frameBytes) {
        try {
            ByteBuffer frameBuffer = ByteBuffer.wrap(frameBytes);
            frameBuffer.order(ByteOrder.BIG_ENDIAN);
            if (frameType == 1) {
                parseEthernetFrame(frameBuffer);
            } else if (frameType == 2) {
                parseManagementFrame(frameBuffer);
            }
        } catch (IllegalArgumentException | BufferUnderflowException e) {
            Log.e(TAG, "Dissection aborted mid-frame: " + e);
        }
    }

    private static short getUnsignedByte(ByteBuffer data) {
        return (short) (data.get() & 255);
    }

    private static int getUnsignedShort(ByteBuffer data) {
        return data.getShort() & 65535;
    }

    private void parseEthernetFrame(ByteBuffer data) {
        this.mMostSpecificProtocolString = "Ethernet";
        data.position(data.position() + 6 + 6);
        short etherType = data.getShort();
        if (etherType == -31011) {
            parseIpv6Packet(data);
        } else if (etherType == -30578) {
            parseEapolPacket(data);
        } else if (etherType == 2048) {
            parseIpv4Packet(data);
        } else if (etherType == 2054) {
            parseArpPacket(data);
        }
    }

    private void parseIpv4Packet(ByteBuffer data) {
        this.mMostSpecificProtocolString = "IPv4";
        data.mark();
        byte versionAndHeaderLen = data.get();
        int version = (versionAndHeaderLen & IP_V4_VERSION_BYTE_MASK) >> 4;
        if (version != 4) {
            Log.e(TAG, "IPv4 header: Unrecognized protocol version " + version);
            return;
        }
        data.position(data.position() + 1 + 2 + 2 + 2 + 1);
        short protocolNumber = getUnsignedByte(data);
        data.position(data.position() + 2 + 4 + 4);
        data.reset();
        data.position(data.position() + ((versionAndHeaderLen & IP_V4_IHL_BYTE_MASK) * 4));
        if (protocolNumber == 1) {
            parseIcmpPacket(data);
        } else if (protocolNumber == 6) {
            parseTcpPacket(data);
        } else if (protocolNumber == 17) {
            parseUdpPacket(data);
        }
    }

    static {
        HTTP_PORTS.add(80);
        HTTP_PORTS.add(3128);
        HTTP_PORTS.add(3132);
        HTTP_PORTS.add(5985);
        HTTP_PORTS.add(8080);
        HTTP_PORTS.add(8088);
        HTTP_PORTS.add(11371);
        HTTP_PORTS.add(1900);
        HTTP_PORTS.add(2869);
        HTTP_PORTS.add(2710);
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
        getUnsignedShort(data);
        data.position(data.position() + 2);
        if ((srcPort == 68 && dstPort == 67) || (srcPort == 67 && dstPort == 68)) {
            parseDhcpPacket(data);
        } else if (srcPort == 123 || dstPort == 123) {
            this.mMostSpecificProtocolString = "NTP";
        }
    }

    private void parseDhcpPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "DHCP";
        data.position(data.position() + 1 + 1 + 1 + 1 + 4 + 2 + 2 + 16 + 16 + 64 + 128 + 4);
        while (data.remaining() > 0) {
            short dhcpOptionTag = getUnsignedByte(data);
            if (dhcpOptionTag != 0) {
                if (dhcpOptionTag != 255) {
                    short dhcpOptionLen = getUnsignedByte(data);
                    if (dhcpOptionTag != 53) {
                        data.position(data.position() + dhcpOptionLen);
                    } else if (dhcpOptionLen != 1) {
                        Log.e(TAG, "DHCP option len: " + ((int) dhcpOptionLen) + " (expected |1|)");
                        return;
                    } else {
                        this.mTypeString = decodeDhcpMessageType(getUnsignedByte(data));
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    private static String decodeDhcpMessageType(short messageType) {
        switch (messageType) {
            case 1:
                return "Discover";
            case 2:
                return "Offer";
            case 3:
                return "Request";
            case 4:
                return "Decline";
            case 5:
                return "Ack";
            case 6:
                return "Nak";
            case 7:
                return "Release";
            case 8:
                return "Inform";
            default:
                return "Unknown type " + ((int) messageType);
        }
    }

    private void parseIcmpPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "ICMP";
        short messageType = getUnsignedByte(data);
        if (messageType == 0) {
            this.mTypeString = "Echo Reply";
        } else if (messageType == 3) {
            this.mTypeString = "Destination Unreachable";
        } else if (messageType == 5) {
            this.mTypeString = "Redirect";
        } else if (messageType != 8) {
            this.mTypeString = "Type " + ((int) messageType);
        } else {
            this.mTypeString = "Echo Request";
        }
    }

    private void parseArpPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "ARP";
        data.position(data.position() + 2 + 2 + 1 + 1);
        int opCode = getUnsignedShort(data);
        if (opCode == 1) {
            this.mTypeString = "Request";
        } else if (opCode != 2) {
            this.mTypeString = "Operation " + opCode;
        } else {
            this.mTypeString = "Reply";
        }
    }

    private void parseIpv6Packet(ByteBuffer data) {
        this.mMostSpecificProtocolString = "IPv6";
        int version = (-268435456 & data.getInt()) >> 28;
        if (version != 6) {
            Log.e(TAG, "IPv6 header: invalid IP version " + version);
            return;
        }
        data.position(data.position() + 2);
        short nextHeaderType = getUnsignedByte(data);
        data.position(data.position() + 1 + 32);
        while (nextHeaderType == 0) {
            data.mark();
            nextHeaderType = getUnsignedByte(data);
            data.reset();
            data.position(data.position() + ((getUnsignedByte(data) + 1) * 8));
        }
        if (nextHeaderType != 58) {
            this.mTypeString = "Option/Protocol " + ((int) nextHeaderType);
            return;
        }
        parseIcmpV6Packet(data);
    }

    private void parseIcmpV6Packet(ByteBuffer data) {
        this.mMostSpecificProtocolString = "ICMPv6";
        short icmpV6Type = getUnsignedByte(data);
        if (icmpV6Type == 128) {
            this.mTypeString = "Echo Request";
        } else if (icmpV6Type == 129) {
            this.mTypeString = "Echo Reply";
        } else if (icmpV6Type != 143) {
            switch (icmpV6Type) {
                case 133:
                    this.mTypeString = "Router Solicitation";
                    return;
                case 134:
                    this.mTypeString = "Router Advertisement";
                    return;
                case 135:
                    this.mTypeString = "Neighbor Solicitation";
                    return;
                case 136:
                    this.mTypeString = "Neighbor Advertisement";
                    return;
                default:
                    this.mTypeString = "Type " + ((int) icmpV6Type);
                    return;
            }
        } else {
            this.mTypeString = "MLDv2 report";
        }
    }

    private void parseEapolPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "EAPOL";
        short eapolVersion = getUnsignedByte(data);
        if (eapolVersion < 1 || eapolVersion > 2) {
            Log.e(TAG, "Unrecognized EAPOL version " + ((int) eapolVersion));
            return;
        }
        short eapolType = getUnsignedByte(data);
        if (eapolType != 3) {
            Log.e(TAG, "Unrecognized EAPOL type " + ((int) eapolType));
            return;
        }
        data.position(data.position() + 2);
        short eapolKeyDescriptorType = getUnsignedByte(data);
        if (eapolKeyDescriptorType != 2) {
            Log.e(TAG, "Unrecognized key descriptor " + ((int) eapolKeyDescriptorType));
            return;
        }
        short wpaKeyInfo = data.getShort();
        if ((wpaKeyInfo & 8) == 0) {
            this.mTypeString = "Group Key";
        } else {
            this.mTypeString = "Pairwise Key";
        }
        if ((wpaKeyInfo & 256) == 0) {
            this.mTypeString += " message 1/4";
        } else if ((wpaKeyInfo & 64) != 0) {
            this.mTypeString += " message 3/4";
        } else {
            data.position(data.position() + 2 + 8 + 32 + 16 + 8 + 8 + 16);
            if (getUnsignedShort(data) > 0) {
                this.mTypeString += " message 2/4";
                return;
            }
            this.mTypeString += " message 4/4";
        }
    }

    private static byte parseIeee80211FrameCtrlVersion(byte b) {
        return (byte) (b & 3);
    }

    private static byte parseIeee80211FrameCtrlType(byte b) {
        return (byte) ((b & IEEE_80211_FRAME_TYPE_MGMT_SUBTYPE_DEAUTH) >> 2);
    }

    private static byte parseIeee80211FrameCtrlSubtype(byte b) {
        return (byte) ((b & IP_V4_VERSION_BYTE_MASK) >> 4);
    }

    private void parseManagementFrame(ByteBuffer data) {
        data.order(ByteOrder.LITTLE_ENDIAN);
        this.mMostSpecificProtocolString = "802.11 Mgmt";
        byte frameControlVersionTypeSubtype = data.get();
        byte ieee80211Version = parseIeee80211FrameCtrlVersion(frameControlVersionTypeSubtype);
        if (ieee80211Version != 0) {
            Log.e(TAG, "Unrecognized 802.11 version " + ((int) ieee80211Version));
            return;
        }
        byte ieee80211FrameType = parseIeee80211FrameCtrlType(frameControlVersionTypeSubtype);
        if (ieee80211FrameType != 0) {
            Log.e(TAG, "Unexpected frame type " + ((int) ieee80211FrameType));
            return;
        }
        byte frameControlFlags = data.get();
        data.position(data.position() + 2 + 6 + 6 + 6 + 2);
        if ((frameControlFlags & IEEE_80211_FRAME_FLAG_ORDER) != 0) {
            data.position(data.position() + 4);
        }
        byte ieee80211FrameSubtype = parseIeee80211FrameCtrlSubtype(frameControlVersionTypeSubtype);
        switch (ieee80211FrameSubtype) {
            case 0:
                this.mTypeString = "Association Request";
                return;
            case 1:
                this.mTypeString = "Association Response";
                parseAssociationResponse(data);
                return;
            case 2:
                this.mTypeString = "Reassociation Request";
                return;
            case 3:
                this.mTypeString = "Reassociation Response";
                return;
            case 4:
                this.mTypeString = "Probe Request";
                return;
            case 5:
                this.mTypeString = "Probe Response";
                return;
            case 6:
                this.mTypeString = "Timing Advertisement";
                return;
            case 7:
            case 15:
                this.mTypeString = "Reserved";
                return;
            case 8:
                this.mTypeString = "Beacon";
                return;
            case 9:
                this.mTypeString = "ATIM";
                return;
            case 10:
                this.mTypeString = "Disassociation";
                parseDisassociationFrame(data);
                return;
            case 11:
                this.mTypeString = "Authentication";
                parseAuthenticationFrame(data);
                return;
            case 12:
                this.mTypeString = "Deauthentication";
                parseDeauthenticationFrame(data);
                return;
            case 13:
                this.mTypeString = "Action";
                return;
            case 14:
                this.mTypeString = "Action No Ack";
                return;
            default:
                this.mTypeString = "Unexpected subtype " + ((int) ieee80211FrameSubtype);
                return;
        }
    }

    private void parseAssociationResponse(ByteBuffer data) {
        data.position(data.position() + 2);
        short resultCode = data.getShort();
        this.mResultString = String.format("%d: %s", Short.valueOf(resultCode), decodeIeee80211StatusCode(resultCode));
    }

    private void parseDisassociationFrame(ByteBuffer data) {
        short reasonCode = data.getShort();
        this.mResultString = String.format("%d: %s", Short.valueOf(reasonCode), decodeIeee80211ReasonCode(reasonCode));
    }

    private void parseAuthenticationFrame(ByteBuffer data) {
        short algorithm = data.getShort();
        short sequenceNum = data.getShort();
        boolean hasResultCode = false;
        if (algorithm == 0 || algorithm == 1) {
            if (sequenceNum == 2) {
                hasResultCode = true;
            }
        } else if (algorithm != 2) {
            if (algorithm == 3) {
                hasResultCode = true;
            }
        } else if (sequenceNum == 2 || sequenceNum == 4) {
            hasResultCode = true;
        }
        if (hasResultCode) {
            short resultCode = data.getShort();
            this.mResultString = String.format("%d: %s", Short.valueOf(resultCode), decodeIeee80211StatusCode(resultCode));
        }
    }

    private void parseDeauthenticationFrame(ByteBuffer data) {
        short reasonCode = data.getShort();
        this.mResultString = String.format("%d: %s", Short.valueOf(reasonCode), decodeIeee80211ReasonCode(reasonCode));
    }

    private String decodeIeee80211ReasonCode(short reasonCode) {
        switch (reasonCode) {
            case 0:
                return "Reserved";
            case 1:
                return "Unspecified reason";
            case 2:
                return "Previous authentication no longer valid";
            case 3:
                return "Deauthenticated because sending STA is leaving (or has left) IBSS or ESS";
            case 4:
                return "Disassociated due to inactivity";
            case 5:
                return "Disassociated because AP is unable to handle all currently associated STAs";
            case 6:
                return "Class 2 frame received from nonauthenticated STA";
            case 7:
                return "Class 3 frame received from nonassociated STA";
            case 8:
                return "Disassociated because sending STA is leaving (or has left) BSS";
            case 9:
                return "STA requesting (re)association is not authenticated with responding STA";
            case 10:
                return "Disassociated because the information in the Power Capability element is unacceptable";
            case 11:
                return "Disassociated because the information in the Supported Channels element is unacceptable";
            case 12:
                return "Disassociated due to BSS transition management";
            case 13:
                return "Invalid element, i.e., an element defined in this standard for which the content does not meet the specifications in Clause 9";
            case 14:
                return "Message integrity code (MIC) failure";
            case 15:
                return "4-way handshake timeout";
            case 16:
                return "Group key handshake timeout";
            case 17:
                return "Element in 4-way handshake different from (Re)Association Request/Probe Response/Beacon frame";
            case 18:
                return "Invalid group cipher";
            case 19:
                return "Invalid pairwise cipher";
            case 20:
                return "Invalid AKMP";
            case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION /* 21 */:
                return "Unsupported RSNE version";
            case 22:
                return "Invalid RSNE capabilities";
            case 23:
                return "IEEE 802.1X authentication failed";
            case 24:
                return "Cipher suite rejected because of the security policy";
            case 25:
                return "TDLS direct-link teardown due to TDLS peer STA unreachable via the TDLS direct link";
            case ISupplicantStaIfaceCallback.ReasonCode.TDLS_TEARDOWN_UNSPECIFIED /* 26 */:
                return "TDLS direct-link teardown for unspecified reason";
            case 27:
                return "Disassociated because session terminated by SSP request";
            case 28:
                return "Disassociated because of lack of SSP roaming agreement";
            case 29:
                return "Requested service rejected because of SSP cipher suite or AKM requirement";
            case 30:
                return "Requested service not authorized in this location";
            case 31:
                return "TS deleted because QoS AP lacks sufficient bandwidth for this QoS STA due to a change in BSS service characteristics or operational mode (e.g., an HT BSS change from 40 MHz channel to 20 MHz channel)";
            case 32:
                return "Disassociated for unspecified, QoS-related reason";
            case 33:
                return "Disassociated because QoS AP lacks sufficient bandwidth for this QoS STA";
            case 34:
                return "Disassociated because excessive number of frames need to be acknowledged, but are not acknowledged due to AP transmissions and/or poor channel conditions";
            case 35:
                return "Disassociated because STA is transmitting outside the limits of its TXOPs";
            case 36:
                return "Requesting STA is leaving the BSS (or resetting)";
            case 37:
                return "Requesting STA is no longer using the stream or session";
            case 38:
                return "Requesting STA received frames using a mechanism for which a setup has not been completed";
            case 39:
                return "Requested from peer STA due to timeout";
            case 40:
            case ISupplicantStaIfaceCallback.StatusCode.GROUP_CIPHER_NOT_VALID /* 41 */:
            case 42:
            case 43:
            case ISupplicantStaIfaceCallback.StatusCode.UNSUPPORTED_RSN_IE_VERSION /* 44 */:
                return "<unspecified>";
            case 45:
                return "Peer STA does not support the requested cipher suite";
            case 46:
                return "In a DLS Teardown frame: The teardown was initiated by the DLS peer. In a Disassociation frame: Disassociated because authorized access limit reached";
            case 47:
                return "In a DLS Teardown frame: The teardown was initiated by the AP. In a Disassociation frame: Disassociated due to external service requirements";
            case 48:
                return "Invalid FT Action frame count";
            case 49:
                return "Invalid pairwise master key identifier (PMKID)";
            case 50:
                return "Invalid MDE";
            case 51:
                return "Invalid FTE";
            case 52:
                return "Mesh peering canceled for unknown reasons";
            case 53:
                return "The mesh STA has reached the supported maximum number of peer mesh STAs";
            case 54:
                return "The received information violates the Mesh Configuration policy configured in the mesh STA profile";
            case 55:
                return "The mesh STA has received a Mesh Peering Close frame requesting to close the mesh peering.";
            case 56:
                return "The mesh STA has resent dot11MeshMaxRetries Mesh Peering Open frames, without receiving a Mesh Peering Confirm frame.";
            case 57:
                return "The confirmTimer for the mesh peering instance times out.";
            case 58:
                return "The mesh STA fails to unwrap the GTK or the values in the wrapped contents do not match";
            case 59:
                return "The mesh STA receives inconsistent information about the mesh parameters between mesh peering Management frames";
            case 60:
                return "The mesh STA fails the authenticated mesh peering exchange because due to failure in selecting either the pairwise ciphersuite or group ciphersuite";
            case 61:
                return "The mesh STA does not have proxy information for this external destination.";
            case 62:
                return "The mesh STA does not have forwarding information for this destination.";
            case 63:
                return "The mesh STA determines that the link to the next hop of an active path in its forwarding information is no longer usable.";
            case 64:
                return "The Deauthentication frame was sent because the MAC address of the STA already exists in the mesh BSS. See 11.3.6.";
            case 65:
                return "The mesh STA performs channel switch to meet regulatory requirements.";
            case ISupplicantStaIfaceCallback.ReasonCode.MESH_CHANNEL_SWITCH_UNSPECIFIED /* 66 */:
                return "The mesh STA performs channel switching with unspecified reason.";
            default:
                return "Reserved";
        }
    }

    private String decodeIeee80211StatusCode(short statusCode) {
        switch (statusCode) {
            case 0:
                return "Success";
            case 1:
                return "Unspecified failure";
            case 2:
                return "TDLS wakeup schedule rejected; alternative provided";
            case 3:
                return "TDLS wakeup schedule rejected";
            case 4:
                return "Reserved";
            case 5:
                return "Security disabled";
            case 6:
                return "Unacceptable lifetime";
            case 7:
                return "Not in same BSS";
            case 8:
            case 9:
                return "Reserved";
            case 10:
                return "Capabilities mismatch";
            case 11:
                return "Reassociation denied; could not confirm association exists";
            case 12:
                return "Association denied for reasons outside standard";
            case 13:
                return "Unsupported authentication algorithm";
            case 14:
                return "Authentication sequence number of of sequence";
            case 15:
                return "Authentication challenge failure";
            case 16:
                return "Authentication timeout";
            case 17:
                return "Association denied; too many STAs";
            case 18:
                return "Association denied; must support BSSBasicRateSet";
            case 19:
                return "Association denied; must support short preamble";
            case 20:
                return "Association denied; must support PBCC";
            case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION /* 21 */:
                return "Association denied; must support channel agility";
            case 22:
                return "Association rejected; must support spectrum management";
            case 23:
                return "Association rejected; unacceptable power capability";
            case 24:
                return "Association rejected; unacceptable supported channels";
            case 25:
                return "Association denied; must support short slot time";
            case ISupplicantStaIfaceCallback.ReasonCode.TDLS_TEARDOWN_UNSPECIFIED /* 26 */:
                return "Association denied; must support DSSS-OFDM";
            case 27:
                return "Association denied; must support HT";
            case 28:
                return "R0 keyholder unreachable (802.11r)";
            case 29:
                return "Association denied; must support PCO transition time";
            case 30:
                return "Refused temporarily";
            case 31:
                return "Robust management frame policy violation";
            case 32:
                return "Unspecified QoS failure";
            case 33:
                return "Association denied; insufficient bandwidth for QoS";
            case 34:
                return "Association denied; poor channel";
            case 35:
                return "Association denied; must support QoS";
            case 36:
                return "Reserved";
            case 37:
                return "Declined";
            case 38:
                return "Invalid parameters";
            case 39:
                return "TS cannot be honored; changes suggested";
            case 40:
                return "Invalid element";
            case ISupplicantStaIfaceCallback.StatusCode.GROUP_CIPHER_NOT_VALID /* 41 */:
                return "Invalid group cipher";
            case 42:
                return "Invalid pairwise cipher";
            case 43:
                return "Invalid auth/key mgmt proto (AKMP)";
            case ISupplicantStaIfaceCallback.StatusCode.UNSUPPORTED_RSN_IE_VERSION /* 44 */:
                return "Unsupported RSNE version";
            case 45:
                return "Invalid RSNE capabilities";
            case 46:
                return "Cipher suite rejected by policy";
            case 47:
                return "TS cannot be honored now; try again later";
            case 48:
                return "Direct link rejected by policy";
            case 49:
                return "Destination STA not in BSS";
            case 50:
                return "Destination STA not configured for QoS";
            case 51:
                return "Association denied; listen interval too large";
            case 52:
                return "Invalid fast transition action frame count";
            case 53:
                return "Invalid PMKID";
            case 54:
                return "Invalid MDE";
            case 55:
                return "Invalid FTE";
            case 56:
                return "Unsupported TCLAS";
            case 57:
                return "Requested TCLAS exceeds resources";
            case 58:
                return "TS cannot be honored; try another BSS";
            case 59:
                return "GAS Advertisement not supported";
            case 60:
                return "No outstanding GAS request";
            case 61:
                return "No query response from GAS server";
            case 62:
                return "GAS query timeout";
            case 63:
                return "GAS response too large";
            case 64:
                return "Home network does not support request";
            case 65:
                return "Advertisement server unreachable";
            case ISupplicantStaIfaceCallback.ReasonCode.MESH_CHANNEL_SWITCH_UNSPECIFIED /* 66 */:
                return "Reserved";
            case ISupplicantStaIfaceCallback.StatusCode.REQ_REFUSED_SSPN /* 67 */:
                return "Rejected for SSP permissions";
            case ISupplicantStaIfaceCallback.StatusCode.REQ_REFUSED_UNAUTH_ACCESS /* 68 */:
                return "Authentication required";
            case 69:
            case 70:
            case 71:
                return "Reserved";
            case ISupplicantStaIfaceCallback.StatusCode.INVALID_RSNIE /* 72 */:
                return "Invalid RSNE contents";
            case ISupplicantStaIfaceCallback.StatusCode.U_APSD_COEX_NOT_SUPPORTED /* 73 */:
                return "U-APSD coexistence unsupported";
            case ISupplicantStaIfaceCallback.StatusCode.U_APSD_COEX_MODE_NOT_SUPPORTED /* 74 */:
                return "Requested U-APSD coex mode unsupported";
            case ISupplicantStaIfaceCallback.StatusCode.BAD_INTERVAL_WITH_U_APSD_COEX /* 75 */:
                return "Requested parameter unsupported with U-APSD coex";
            case ISupplicantStaIfaceCallback.StatusCode.ANTI_CLOGGING_TOKEN_REQ /* 76 */:
                return "Auth rejected; anti-clogging token required";
            case ISupplicantStaIfaceCallback.StatusCode.FINITE_CYCLIC_GROUP_NOT_SUPPORTED /* 77 */:
                return "Auth rejected; offered group is not supported";
            case ISupplicantStaIfaceCallback.StatusCode.CANNOT_FIND_ALT_TBTT /* 78 */:
                return "Cannot find alternative TBTT";
            case ISupplicantStaIfaceCallback.StatusCode.TRANSMISSION_FAILURE /* 79 */:
                return "Transmission failure";
            case 80:
                return "Requested TCLAS not supported";
            case ISupplicantStaIfaceCallback.StatusCode.TCLAS_RESOURCES_EXCHAUSTED /* 81 */:
                return "TCLAS resources exhausted";
            case ISupplicantStaIfaceCallback.StatusCode.REJECTED_WITH_SUGGESTED_BSS_TRANSITION /* 82 */:
                return "Rejected with suggested BSS transition";
            case ISupplicantStaIfaceCallback.StatusCode.REJECT_WITH_SCHEDULE /* 83 */:
                return "Reserved";
            case ISupplicantStaIfaceCallback.StatusCode.REJECT_NO_WAKEUP_SPECIFIED /* 84 */:
            case 85:
            case ISupplicantStaIfaceCallback.StatusCode.PENDING_ADMITTING_FST_SESSION /* 86 */:
            case ISupplicantStaIfaceCallback.StatusCode.PERFORMING_FST_NOW /* 87 */:
            case ISupplicantStaIfaceCallback.StatusCode.PENDING_GAP_IN_BA_WINDOW /* 88 */:
            case ISupplicantStaIfaceCallback.StatusCode.REJECT_U_PID_SETTING /* 89 */:
            case 90:
            case 91:
                return "<unspecified>";
            case ISupplicantStaIfaceCallback.StatusCode.REFUSED_EXTERNAL_REASON /* 92 */:
                return "Refused due to external reason";
            case ISupplicantStaIfaceCallback.StatusCode.REFUSED_AP_OUT_OF_MEMORY /* 93 */:
                return "Refused; AP out of memory";
            case ISupplicantStaIfaceCallback.StatusCode.REJECTED_EMERGENCY_SERVICE_NOT_SUPPORTED /* 94 */:
                return "Refused; emergency services not supported";
            case ISupplicantStaIfaceCallback.StatusCode.QUERY_RESP_OUTSTANDING /* 95 */:
                return "GAS query response outstanding";
            case ISupplicantStaIfaceCallback.StatusCode.REJECT_DSE_BAND /* 96 */:
            case ISupplicantStaIfaceCallback.StatusCode.TCLAS_PROCESSING_TERMINATED /* 97 */:
            case ISupplicantStaIfaceCallback.StatusCode.TS_SCHEDULE_CONFLICT /* 98 */:
            case ISupplicantStaIfaceCallback.StatusCode.DENIED_WITH_SUGGESTED_BAND_AND_CHANNEL /* 99 */:
                return "Reserved";
            case 100:
                return "Failed; reservation conflict";
            case 101:
                return "Failed; exceeded MAF limit";
            case 102:
                return "Failed; exceeded MCCA track limit";
            default:
                return "Reserved";
        }
    }
}
