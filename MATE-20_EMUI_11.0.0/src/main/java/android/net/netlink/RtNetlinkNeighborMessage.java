package android.net.netlink;

import android.system.OsConstants;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RtNetlinkNeighborMessage extends NetlinkMessage {
    public static final short NDA_CACHEINFO = 3;
    public static final short NDA_DST = 1;
    public static final short NDA_IFINDEX = 8;
    public static final short NDA_LLADDR = 2;
    public static final short NDA_MASTER = 9;
    public static final short NDA_PORT = 6;
    public static final short NDA_PROBES = 4;
    public static final short NDA_UNSPEC = 0;
    public static final short NDA_VLAN = 5;
    public static final short NDA_VNI = 7;
    private StructNdaCacheInfo mCacheInfo = null;
    private InetAddress mDestination = null;
    private byte[] mLinkLayerAddr = null;
    private StructNdMsg mNdmsg = null;
    private int mNumProbes = 0;

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

    public static RtNetlinkNeighborMessage parse(StructNlMsgHdr header, ByteBuffer byteBuffer) {
        RtNetlinkNeighborMessage neighMsg = new RtNetlinkNeighborMessage(header);
        neighMsg.mNdmsg = StructNdMsg.parse(byteBuffer);
        if (neighMsg.mNdmsg == null) {
            return null;
        }
        int baseOffset = byteBuffer.position();
        StructNlAttr nlAttr = findNextAttrOfType(1, byteBuffer);
        if (nlAttr != null) {
            neighMsg.mDestination = nlAttr.getValueAsInetAddress();
        }
        byteBuffer.position(baseOffset);
        StructNlAttr nlAttr2 = findNextAttrOfType(2, byteBuffer);
        if (nlAttr2 != null) {
            neighMsg.mLinkLayerAddr = nlAttr2.nla_value;
        }
        byteBuffer.position(baseOffset);
        StructNlAttr nlAttr3 = findNextAttrOfType(4, byteBuffer);
        if (nlAttr3 != null) {
            neighMsg.mNumProbes = nlAttr3.getValueAsInt(0);
        }
        byteBuffer.position(baseOffset);
        StructNlAttr nlAttr4 = findNextAttrOfType(3, byteBuffer);
        if (nlAttr4 != null) {
            neighMsg.mCacheInfo = StructNdaCacheInfo.parse(nlAttr4.getValueAsByteBuffer());
        }
        int kAdditionalSpace = NetlinkConstants.alignedLengthOf(neighMsg.mHeader.nlmsg_len - 28);
        if (byteBuffer.remaining() < kAdditionalSpace) {
            byteBuffer.position(byteBuffer.limit());
        } else {
            byteBuffer.position(baseOffset + kAdditionalSpace);
        }
        return neighMsg;
    }

    public static byte[] newGetNeighborsRequest(int seqNo) {
        byte[] bytes = new byte[28];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        StructNlMsgHdr nlmsghdr = new StructNlMsgHdr();
        nlmsghdr.nlmsg_len = 28;
        nlmsghdr.nlmsg_type = 30;
        nlmsghdr.nlmsg_flags = 769;
        nlmsghdr.nlmsg_seq = seqNo;
        nlmsghdr.pack(byteBuffer);
        new StructNdMsg().pack(byteBuffer);
        return bytes;
    }

    public static byte[] newNewNeighborMessage(int seqNo, InetAddress ip, short nudState, int ifIndex, byte[] llAddr) {
        StructNlMsgHdr nlmsghdr = new StructNlMsgHdr();
        nlmsghdr.nlmsg_type = 28;
        nlmsghdr.nlmsg_flags = 261;
        nlmsghdr.nlmsg_seq = seqNo;
        RtNetlinkNeighborMessage msg = new RtNetlinkNeighborMessage(nlmsghdr);
        msg.mNdmsg = new StructNdMsg();
        msg.mNdmsg.ndm_family = (byte) (ip instanceof Inet6Address ? OsConstants.AF_INET6 : OsConstants.AF_INET);
        StructNdMsg structNdMsg = msg.mNdmsg;
        structNdMsg.ndm_ifindex = ifIndex;
        structNdMsg.ndm_state = nudState;
        msg.mDestination = ip;
        msg.mLinkLayerAddr = llAddr;
        byte[] bytes = new byte[msg.getRequiredSpace()];
        nlmsghdr.nlmsg_len = bytes.length;
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        msg.pack(byteBuffer);
        return bytes;
    }

    private RtNetlinkNeighborMessage(StructNlMsgHdr header) {
        super(header);
    }

    public StructNdMsg getNdHeader() {
        return this.mNdmsg;
    }

    public InetAddress getDestination() {
        return this.mDestination;
    }

    public byte[] getLinkLayerAddress() {
        return this.mLinkLayerAddr;
    }

    public int getProbes() {
        return this.mNumProbes;
    }

    public StructNdaCacheInfo getCacheInfo() {
        return this.mCacheInfo;
    }

    public int getRequiredSpace() {
        int spaceRequired = 28;
        InetAddress inetAddress = this.mDestination;
        if (inetAddress != null) {
            spaceRequired = 28 + NetlinkConstants.alignedLengthOf(inetAddress.getAddress().length + 4);
        }
        byte[] bArr = this.mLinkLayerAddr;
        if (bArr != null) {
            return spaceRequired + NetlinkConstants.alignedLengthOf(bArr.length + 4);
        }
        return spaceRequired;
    }

    private static void packNlAttr(short nlType, byte[] nlValue, ByteBuffer byteBuffer) {
        StructNlAttr nlAttr = new StructNlAttr();
        nlAttr.nla_type = nlType;
        nlAttr.nla_value = nlValue;
        nlAttr.nla_len = (short) (nlAttr.nla_value.length + 4);
        nlAttr.pack(byteBuffer);
    }

    public void pack(ByteBuffer byteBuffer) {
        getHeader().pack(byteBuffer);
        this.mNdmsg.pack(byteBuffer);
        InetAddress inetAddress = this.mDestination;
        if (inetAddress != null) {
            packNlAttr(1, inetAddress.getAddress(), byteBuffer);
        }
        byte[] bArr = this.mLinkLayerAddr;
        if (bArr != null) {
            packNlAttr(2, bArr, byteBuffer);
        }
    }

    @Override // android.net.netlink.NetlinkMessage
    public String toString() {
        InetAddress inetAddress = this.mDestination;
        String str = "";
        if (inetAddress != null) {
            inetAddress.getHostAddress();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("RtNetlinkNeighborMessage{ nlmsghdr{");
        sb.append(this.mHeader == null ? str : this.mHeader.toString());
        sb.append("}, ndmsg{");
        StructNdMsg structNdMsg = this.mNdmsg;
        sb.append(structNdMsg == null ? str : structNdMsg.toString());
        sb.append("}, destination{ *** } linklayeraddr{");
        sb.append(NetlinkConstants.hexify(this.mLinkLayerAddr));
        sb.append("} probes{");
        sb.append(this.mNumProbes);
        sb.append("} cacheinfo{");
        StructNdaCacheInfo structNdaCacheInfo = this.mCacheInfo;
        if (structNdaCacheInfo != null) {
            str = structNdaCacheInfo.toString();
        }
        sb.append(str);
        sb.append("} }");
        return sb.toString();
    }
}
