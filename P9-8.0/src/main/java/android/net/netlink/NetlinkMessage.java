package android.net.netlink;

import android.net.util.NetworkConstants;
import com.android.server.wm.WindowManagerService.H;
import java.nio.ByteBuffer;

public class NetlinkMessage {
    private static final String TAG = "NetlinkMessage";
    protected StructNlMsgHdr mHeader;

    public static NetlinkMessage parse(ByteBuffer byteBuffer) {
        if (byteBuffer != null) {
            int startPosition = byteBuffer.position();
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
        switch (nlmsghdr.nlmsg_type) {
            case (short) 2:
                return NetlinkErrorMessage.parse(nlmsghdr, byteBuffer);
            case (short) 3:
                byteBuffer.position(byteBuffer.position() + payloadLength);
                return new NetlinkMessage(nlmsghdr);
            case (short) 24:
            case (short) 25:
            case H.DO_ANIMATION_CALLBACK /*26*/:
                return RtNetlinkMessage.parse(nlmsghdr, byteBuffer);
            case NetworkConstants.ARP_PAYLOAD_LEN /*28*/:
            case HdmiCecKeycode.CEC_KEYCODE_NUMBER_ENTRY_MODE /*29*/:
            case (short) 30:
                return RtNetlinkNeighborMessage.parse(nlmsghdr, byteBuffer);
            default:
                if (nlmsghdr.nlmsg_type > (short) 15) {
                    return null;
                }
                byteBuffer.position(byteBuffer.position() + payloadLength);
                return new NetlinkMessage(nlmsghdr);
        }
    }

    public NetlinkMessage(StructNlMsgHdr nlmsghdr) {
        this.mHeader = nlmsghdr;
    }

    public StructNlMsgHdr getHeader() {
        return this.mHeader;
    }

    public String toString() {
        return "NetlinkMessage{" + (this.mHeader == null ? "" : this.mHeader.toString()) + "}";
    }
}
