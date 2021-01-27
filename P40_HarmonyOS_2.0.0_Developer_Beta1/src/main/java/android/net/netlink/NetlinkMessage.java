package android.net.netlink;

import com.android.server.TrustedUIService;
import com.android.server.hdmi.HdmiCecKeycode;
import java.nio.ByteBuffer;

public class NetlinkMessage {
    private static final String TAG = "NetlinkMessage";
    protected StructNlMsgHdr mHeader;

    public static NetlinkMessage parse(ByteBuffer byteBuffer) {
        if (byteBuffer != null) {
            byteBuffer.position();
        }
        StructNlMsgHdr nlmsghdr = StructNlMsgHdr.parse(byteBuffer);
        if (nlmsghdr == null) {
            return null;
        }
        int payloadLength = NetlinkConstants.alignedLengthOf(nlmsghdr.nlmsg_len) - 16;
        if (payloadLength < 0 || payloadLength > byteBuffer.remaining()) {
            byteBuffer.position(byteBuffer.limit());
            return null;
        }
        short s = nlmsghdr.nlmsg_type;
        if (s == 2) {
            return NetlinkErrorMessage.parse(nlmsghdr, byteBuffer);
        }
        if (s == 3) {
            byteBuffer.position(byteBuffer.position() + payloadLength);
            return new NetlinkMessage(nlmsghdr);
        } else if (s == 20) {
            return InetDiagMessage.parse(nlmsghdr, byteBuffer);
        } else {
            switch (s) {
                case 24:
                case 25:
                case TrustedUIService.TUI_POLL_FOLD /* 26 */:
                    return RtNetlinkMessage.parse(nlmsghdr, byteBuffer);
                default:
                    switch (s) {
                        case 28:
                        case HdmiCecKeycode.CEC_KEYCODE_NUMBER_ENTRY_MODE /* 29 */:
                        case HdmiCecKeycode.CEC_KEYCODE_NUMBER_11 /* 30 */:
                            return RtNetlinkNeighborMessage.parse(nlmsghdr, byteBuffer);
                        default:
                            if (nlmsghdr.nlmsg_type > 15) {
                                return null;
                            }
                            byteBuffer.position(byteBuffer.position() + payloadLength);
                            return new NetlinkMessage(nlmsghdr);
                    }
            }
        }
    }

    public NetlinkMessage(StructNlMsgHdr nlmsghdr) {
        this.mHeader = nlmsghdr;
    }

    public StructNlMsgHdr getHeader() {
        return this.mHeader;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NetlinkMessage{");
        StructNlMsgHdr structNlMsgHdr = this.mHeader;
        sb.append(structNlMsgHdr == null ? "" : structNlMsgHdr.toString());
        sb.append("}");
        return sb.toString();
    }
}
