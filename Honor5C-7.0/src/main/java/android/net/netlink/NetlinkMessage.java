package android.net.netlink;

import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
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
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                return NetlinkErrorMessage.parse(nlmsghdr, byteBuffer);
            case H.REPORT_LOSING_FOCUS /*3*/:
                byteBuffer.position(byteBuffer.position() + payloadLength);
                return new NetlinkMessage(nlmsghdr);
            case H.DO_DISPLAY_REMOVED /*28*/:
            case H.DO_DISPLAY_CHANGED /*29*/:
            case H.CLIENT_FREEZE_TIMEOUT /*30*/:
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
