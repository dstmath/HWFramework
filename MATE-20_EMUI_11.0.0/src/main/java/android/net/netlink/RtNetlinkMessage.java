package android.net.netlink;

import android.system.OsConstants;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;

public class RtNetlinkMessage extends NetlinkMessage {
    public static final short RTA_DST = 1;
    public static final short RTA_GATEWAY = 5;
    public static final short RTA_IIF = 3;
    public static final short RTA_MARK = 16;
    public static final short RTA_OIF = 4;
    public static final short RTA_PREFSRC = 7;
    public static final short RTA_SRC = 2;
    public static final short RTA_TABLE = 15;
    public static final short RTA_UNSPEC = 0;
    public static final short RTA_VIA = 18;
    public static final short RTN_BROADCAST = 3;
    public static final short RTN_LOCAL = 2;
    public static final short RTN_UNICAST = 1;
    private static final String TAG = "RtNetlinkMessage";
    private static final int WIFI_IPV4_HOST_LEN = 32;
    private static final int WIFI_IPV6_HOST_LEN = 128;
    private StructNlAttr mDest = null;
    private StructNlAttr mGateway = null;
    private StructNlAttr mOif = null;
    private StructNlAttr mPrefSrc = null;
    private StructRtMsg mRtMsg = null;
    private StructNlAttr mVia = null;

    public static class StructRtMsg {
        public static final int STRUCT_SIZE = 12;
        byte rtm_dst_len;
        byte rtm_family;
        int rtm_flags;
        byte rtm_protocol;
        byte rtm_scope;
        byte rtm_src_len;
        byte rtm_table;
        byte rtm_tos;
        byte rtm_type;

        public static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
            return byteBuffer != null && byteBuffer.remaining() >= 12;
        }

        public static StructRtMsg parse(ByteBuffer byteBuffer) {
            if (!hasAvailableSpace(byteBuffer)) {
                return null;
            }
            StructRtMsg struct = new StructRtMsg();
            struct.rtm_family = byteBuffer.get();
            struct.rtm_dst_len = byteBuffer.get();
            struct.rtm_src_len = byteBuffer.get();
            struct.rtm_tos = byteBuffer.get();
            struct.rtm_table = byteBuffer.get();
            struct.rtm_protocol = byteBuffer.get();
            struct.rtm_scope = byteBuffer.get();
            struct.rtm_type = byteBuffer.get();
            struct.rtm_flags = byteBuffer.getInt();
            return struct;
        }

        public String toString() {
            return "StructRtMsg{ rtm_family{" + ((int) this.rtm_family) + "}, rtm_dst_len{" + ((int) this.rtm_dst_len) + "}, rtm_src_len{" + ((int) this.rtm_src_len) + "}, rtm_tos{" + ((int) this.rtm_tos) + "}, rtm_table{" + ((int) this.rtm_table) + "}, rtm_protocol{" + ((int) this.rtm_protocol) + "}, rtm_scope{" + ((int) this.rtm_scope) + "}, rtm_flags{" + this.rtm_flags + "}, }";
        }
    }

    private static StructNlAttr findNextAttrOfType(short attrType, ByteBuffer byteBuffer) {
        while (byteBuffer != null && byteBuffer.remaining() > 0) {
            StructNlAttr nlAttr = StructNlAttr.peek(byteBuffer);
            if (nlAttr == null) {
                return null;
            }
            if (nlAttr.nla_type == attrType) {
                return StructNlAttr.parse(byteBuffer);
            }
            if (byteBuffer.remaining() < nlAttr.getAlignedLength()) {
                return null;
            }
            byteBuffer.position(byteBuffer.position() + nlAttr.getAlignedLength());
        }
        return null;
    }

    private static String getInterfaceNameByIndex(int index) {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ifc = en.nextElement();
                if (index == ifc.getIndex()) {
                    return ifc.getName();
                }
            }
            return null;
        } catch (SocketException e) {
            return null;
        }
    }

    public static RtNetlinkMessage parse(StructNlMsgHdr header, ByteBuffer byteBuffer) {
        RtNetlinkMessage neighMsg = new RtNetlinkMessage(header);
        neighMsg.mRtMsg = StructRtMsg.parse(byteBuffer);
        if (neighMsg.mRtMsg == null) {
            return null;
        }
        int baseOffset = byteBuffer.position();
        StructNlAttr nlAttr = findNextAttrOfType(1, byteBuffer);
        if (nlAttr != null) {
            neighMsg.mDest = nlAttr;
        }
        byteBuffer.position(baseOffset);
        StructNlAttr nlAttr2 = findNextAttrOfType(5, byteBuffer);
        if (nlAttr2 != null) {
            neighMsg.mGateway = nlAttr2;
        }
        byteBuffer.position(baseOffset);
        StructNlAttr nlAttr3 = findNextAttrOfType(18, byteBuffer);
        if (nlAttr3 != null) {
            neighMsg.mVia = nlAttr3;
        }
        byteBuffer.position(baseOffset);
        StructNlAttr nlAttr4 = findNextAttrOfType(4, byteBuffer);
        if (nlAttr4 != null) {
            neighMsg.mOif = nlAttr4;
        }
        byteBuffer.position(baseOffset);
        StructNlAttr nlAttr5 = findNextAttrOfType(7, byteBuffer);
        if (nlAttr5 != null) {
            neighMsg.mPrefSrc = nlAttr5;
        }
        byteBuffer.position(baseOffset);
        int kAdditionalSpace = NetlinkConstants.alignedLengthOf(neighMsg.mHeader.nlmsg_len - 28);
        if (byteBuffer.remaining() < kAdditionalSpace) {
            byteBuffer.position(byteBuffer.limit());
        } else {
            byteBuffer.position(baseOffset + kAdditionalSpace);
        }
        return neighMsg;
    }

    public static byte[] newNewGetRouteMessage() {
        byte[] bytes = new byte[28];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        StructNlMsgHdr nlmsghdr = new StructNlMsgHdr();
        nlmsghdr.nlmsg_len = 28;
        nlmsghdr.nlmsg_type = 26;
        nlmsghdr.nlmsg_flags = 769;
        nlmsghdr.pack(byteBuffer);
        new StructNdMsg().pack(byteBuffer);
        return bytes;
    }

    private RtNetlinkMessage(StructNlMsgHdr header) {
        super(header);
    }

    /* access modifiers changed from: package-private */
    public int getHostLen(int af) {
        if (af == OsConstants.AF_INET6) {
            return 128;
        }
        if (af == OsConstants.AF_INET) {
            return 32;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public String getRtnType() {
        if (this.mRtMsg.rtm_type == 1) {
            return "";
        }
        if (this.mRtMsg.rtm_type == 2) {
            return "local ";
        }
        if (this.mRtMsg.rtm_type == 3) {
            return "broadcast ";
        }
        return "";
    }

    /* access modifiers changed from: package-private */
    public String getDest() {
        int hostLen = getHostLen(this.mRtMsg.rtm_family);
        StructNlAttr structNlAttr = this.mDest;
        if (structNlAttr != null) {
            InetAddress address = structNlAttr.getValueAsInetAddress();
            if (this.mRtMsg.rtm_dst_len != hostLen) {
                return String.format("%s/%d ", address.toString(), Byte.valueOf(this.mRtMsg.rtm_dst_len));
            }
            return String.format("%s ", address.toString());
        } else if (this.mRtMsg.rtm_dst_len != 0) {
            return String.format("0/%d ", Byte.valueOf(this.mRtMsg.rtm_dst_len));
        } else {
            return "default ";
        }
    }

    /* access modifiers changed from: package-private */
    public String getVia() {
        StructNlAttr structNlAttr = this.mGateway;
        if (structNlAttr != null) {
            return String.format("via %s ", structNlAttr.getValueAsInetAddress().toString());
        }
        StructNlAttr structNlAttr2 = this.mVia;
        if (structNlAttr2 != null) {
            return String.format("via %s ", structNlAttr2.getValueAsInetAddress().toString());
        }
        return "";
    }

    /* access modifiers changed from: package-private */
    public String getdev() {
        int index;
        StructNlAttr structNlAttr = this.mOif;
        if (structNlAttr == null || (index = structNlAttr.getValueAsInt(-1)) == -1) {
            return "";
        }
        return String.format("dev %s ", getInterfaceNameByIndex(index));
    }

    /* access modifiers changed from: package-private */
    public String getPrefSrc() {
        StructNlAttr structNlAttr = this.mPrefSrc;
        if (structNlAttr != null) {
            return String.format("src %s ", structNlAttr.getValueAsInetAddress().toString());
        }
        return "";
    }

    @Override // android.net.netlink.NetlinkMessage
    public String toString() {
        return getRtnType() + getDest() + getVia() + getdev() + getPrefSrc() + "\n";
    }
}
