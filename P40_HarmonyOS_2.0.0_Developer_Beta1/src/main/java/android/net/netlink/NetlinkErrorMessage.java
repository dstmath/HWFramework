package android.net.netlink;

import java.nio.ByteBuffer;

public class NetlinkErrorMessage extends NetlinkMessage {
    private StructNlMsgErr mNlMsgErr = null;

    public static NetlinkErrorMessage parse(StructNlMsgHdr header, ByteBuffer byteBuffer) {
        NetlinkErrorMessage errorMsg = new NetlinkErrorMessage(header);
        errorMsg.mNlMsgErr = StructNlMsgErr.parse(byteBuffer);
        if (errorMsg.mNlMsgErr == null) {
            return null;
        }
        return errorMsg;
    }

    NetlinkErrorMessage(StructNlMsgHdr header) {
        super(header);
    }

    public StructNlMsgErr getNlMsgError() {
        return this.mNlMsgErr;
    }

    @Override // android.net.netlink.NetlinkMessage
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NetlinkErrorMessage{ nlmsghdr{");
        String str = "";
        sb.append(this.mHeader == null ? str : this.mHeader.toString());
        sb.append("}, nlmsgerr{");
        StructNlMsgErr structNlMsgErr = this.mNlMsgErr;
        if (structNlMsgErr != null) {
            str = structNlMsgErr.toString();
        }
        sb.append(str);
        sb.append("} }");
        return sb.toString();
    }
}
