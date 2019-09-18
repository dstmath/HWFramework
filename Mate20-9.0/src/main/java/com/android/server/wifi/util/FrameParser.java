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
    private static final byte IEEE_80211_FRAME_CTRL_FLAG_ORDER = Byte.MIN_VALUE;
    private static final byte IEEE_80211_FRAME_CTRL_SUBTYPE_ASSOC_REQ = 0;
    private static final byte IEEE_80211_FRAME_CTRL_SUBTYPE_ASSOC_RESP = 1;
    private static final byte IEEE_80211_FRAME_CTRL_SUBTYPE_AUTH = 11;
    private static final byte IEEE_80211_FRAME_CTRL_SUBTYPE_PROBE_REQ = 4;
    private static final byte IEEE_80211_FRAME_CTRL_SUBTYPE_PROBE_RESP = 5;
    private static final byte IEEE_80211_FRAME_CTRL_TYPE_MGMT = 0;
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
        int unsignedShort = getUnsignedShort(data);
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
                if (dhcpOptionTag == 255) {
                    break;
                }
                short dhcpOptionLen = getUnsignedByte(data);
                if (dhcpOptionTag != 53) {
                    data.position(data.position() + dhcpOptionLen);
                } else if (dhcpOptionLen != 1) {
                    Log.e(TAG, "DHCP option len: " + dhcpOptionLen + " (expected |1|)");
                    return;
                } else {
                    this.mTypeString = decodeDhcpMessageType(getUnsignedByte(data));
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
                return "Unknown type " + messageType;
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
            this.mTypeString = "Type " + messageType;
        } else {
            this.mTypeString = "Echo Request";
        }
    }

    private void parseArpPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "ARP";
        data.position(data.position() + 2 + 2 + 1 + 1);
        int opCode = getUnsignedShort(data);
        switch (opCode) {
            case 1:
                this.mTypeString = "Request";
                return;
            case 2:
                this.mTypeString = "Reply";
                return;
            default:
                this.mTypeString = "Operation " + opCode;
                return;
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
            this.mTypeString = "Option/Protocol " + nextHeaderType;
            return;
        }
        parseIcmpV6Packet(data);
    }

    private void parseIcmpV6Packet(ByteBuffer data) {
        this.mMostSpecificProtocolString = "ICMPv6";
        short icmpV6Type = getUnsignedByte(data);
        if (icmpV6Type != 143) {
            switch (icmpV6Type) {
                case 128:
                    this.mTypeString = "Echo Request";
                    return;
                case 129:
                    this.mTypeString = "Echo Reply";
                    return;
                default:
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
                            this.mTypeString = "Type " + icmpV6Type;
                            return;
                    }
            }
        } else {
            this.mTypeString = "MLDv2 report";
        }
    }

    private void parseEapolPacket(ByteBuffer data) {
        this.mMostSpecificProtocolString = "EAPOL";
        short eapolVersion = getUnsignedByte(data);
        if (eapolVersion < 1 || eapolVersion > 2) {
            Log.e(TAG, "Unrecognized EAPOL version " + eapolVersion);
            return;
        }
        if (getUnsignedByte(data) != 3) {
            Log.e(TAG, "Unrecognized EAPOL type " + eapolType);
            return;
        }
        data.position(data.position() + 2);
        if (getUnsignedByte(data) != 2) {
            Log.e(TAG, "Unrecognized key descriptor " + eapolKeyDescriptorType);
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
        return (byte) ((b & IP_V4_VERSION_BYTE_MASK) >> 4);
    }

    private void parseManagementFrame(ByteBuffer data) {
        data.order(ByteOrder.LITTLE_ENDIAN);
        this.mMostSpecificProtocolString = "802.11 Mgmt";
        byte frameControlVersionTypeSubtype = data.get();
        byte ieee80211Version = parseIeee80211FrameCtrlVersion(frameControlVersionTypeSubtype);
        if (ieee80211Version != 0) {
            Log.e(TAG, "Unrecognized 802.11 version " + ieee80211Version);
            return;
        }
        byte ieee80211FrameType = parseIeee80211FrameCtrlType(frameControlVersionTypeSubtype);
        if (ieee80211FrameType != 0) {
            Log.e(TAG, "Unexpected frame type " + ieee80211FrameType);
            return;
        }
        byte frameControlFlags = data.get();
        data.position(data.position() + 2 + 6 + 6 + 6 + 2);
        if ((frameControlFlags & IEEE_80211_FRAME_CTRL_FLAG_ORDER) != 0) {
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
            case 4:
                this.mTypeString = "Probe Request";
                return;
            case 5:
                this.mTypeString = "Probe Response";
                return;
            case 11:
                this.mTypeString = "Authentication";
                parseAuthenticationFrame(data);
                return;
            default:
                this.mTypeString = "Unexpected subtype " + ieee80211FrameSubtype;
                return;
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
            case 0:
            case 1:
                if (sequenceNum == 2) {
                    hasResultCode = true;
                    break;
                }
                break;
            case 2:
                if (sequenceNum == 2 || sequenceNum == 4) {
                    hasResultCode = true;
                    break;
                }
            case 3:
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
            case ISupplicantStaIfaceCallback.ReasonCode.UNSUPPORTED_RSN_IE_VERSION:
                return "Association denied; must support channel agility";
            case 22:
                return "Association rejected; must support spectrum management";
            case 23:
                return "Association rejected; unacceptable power capability";
            case 24:
                return "Association rejected; unacceptable supported channels";
            case 25:
                return "Association denied; must support short slot time";
            case ISupplicantStaIfaceCallback.ReasonCode.TDLS_TEARDOWN_UNSPECIFIED:
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
            case ISupplicantStaIfaceCallback.ReasonCode.STA_LEAVING:
                return "Reserved";
            case 37:
                return "Declined";
            case 38:
                return "Invalid parameters";
            case 39:
                return "TS cannot be honored; changes suggested";
            case ISupplicantStaIfaceCallback.StatusCode.INVALID_IE:
                return "Invalid element";
            case ISupplicantStaIfaceCallback.StatusCode.GROUP_CIPHER_NOT_VALID:
                return "Invalid group cipher";
            case 42:
                return "Invalid pairwise cipher";
            case 43:
                return "Invalid auth/key mgmt proto (AKMP)";
            case ISupplicantStaIfaceCallback.StatusCode.UNSUPPORTED_RSN_IE_VERSION:
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
            case ISupplicantStaIfaceCallback.ReasonCode.MESH_CHANNEL_SWITCH_UNSPECIFIED:
                return "Reserved";
            case ISupplicantStaIfaceCallback.StatusCode.REQ_REFUSED_SSPN:
                return "Rejected for SSP permissions";
            case ISupplicantStaIfaceCallback.StatusCode.REQ_REFUSED_UNAUTH_ACCESS:
                return "Authentication required";
            case 69:
            case 70:
            case 71:
                return "Reserved";
            case ISupplicantStaIfaceCallback.StatusCode.INVALID_RSNIE:
                return "Invalid RSNE contents";
            case ISupplicantStaIfaceCallback.StatusCode.U_APSD_COEX_NOT_SUPPORTED:
                return "U-APSD coexistence unsupported";
            case ISupplicantStaIfaceCallback.StatusCode.U_APSD_COEX_MODE_NOT_SUPPORTED:
                return "Requested U-APSD coex mode unsupported";
            case ISupplicantStaIfaceCallback.StatusCode.BAD_INTERVAL_WITH_U_APSD_COEX:
                return "Requested parameter unsupported with U-APSD coex";
            case ISupplicantStaIfaceCallback.StatusCode.ANTI_CLOGGING_TOKEN_REQ:
                return "Auth rejected; anti-clogging token required";
            case ISupplicantStaIfaceCallback.StatusCode.FINITE_CYCLIC_GROUP_NOT_SUPPORTED:
                return "Auth rejected; offered group is not supported";
            case ISupplicantStaIfaceCallback.StatusCode.CANNOT_FIND_ALT_TBTT:
                return "Cannot find alternative TBTT";
            case ISupplicantStaIfaceCallback.StatusCode.TRANSMISSION_FAILURE:
                return "Transmission failure";
            case ISupplicantStaIfaceCallback.StatusCode.REQ_TCLAS_NOT_SUPPORTED:
                return "Requested TCLAS not supported";
            case ISupplicantStaIfaceCallback.StatusCode.TCLAS_RESOURCES_EXCHAUSTED:
                return "TCLAS resources exhausted";
            case ISupplicantStaIfaceCallback.StatusCode.REJECTED_WITH_SUGGESTED_BSS_TRANSITION:
                return "Rejected with suggested BSS transition";
            case ISupplicantStaIfaceCallback.StatusCode.REJECT_WITH_SCHEDULE:
                return "Reserved";
            case ISupplicantStaIfaceCallback.StatusCode.REJECT_NO_WAKEUP_SPECIFIED:
            case ISupplicantStaIfaceCallback.StatusCode.SUCCESS_POWER_SAVE_MODE:
            case ISupplicantStaIfaceCallback.StatusCode.PENDING_ADMITTING_FST_SESSION:
            case ISupplicantStaIfaceCallback.StatusCode.PERFORMING_FST_NOW:
            case ISupplicantStaIfaceCallback.StatusCode.PENDING_GAP_IN_BA_WINDOW:
            case ISupplicantStaIfaceCallback.StatusCode.REJECT_U_PID_SETTING:
            case 90:
            case 91:
                return "<unspecified>";
            case ISupplicantStaIfaceCallback.StatusCode.REFUSED_EXTERNAL_REASON:
                return "Refused due to external reason";
            case ISupplicantStaIfaceCallback.StatusCode.REFUSED_AP_OUT_OF_MEMORY:
                return "Refused; AP out of memory";
            case ISupplicantStaIfaceCallback.StatusCode.REJECTED_EMERGENCY_SERVICE_NOT_SUPPORTED:
                return "Refused; emergency services not supported";
            case ISupplicantStaIfaceCallback.StatusCode.QUERY_RESP_OUTSTANDING:
                return "GAS query response outstanding";
            case ISupplicantStaIfaceCallback.StatusCode.REJECT_DSE_BAND:
            case ISupplicantStaIfaceCallback.StatusCode.TCLAS_PROCESSING_TERMINATED:
            case ISupplicantStaIfaceCallback.StatusCode.TS_SCHEDULE_CONFLICT:
            case ISupplicantStaIfaceCallback.StatusCode.DENIED_WITH_SUGGESTED_BAND_AND_CHANNEL:
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
