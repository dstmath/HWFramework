package android.net.netlink;

import java.nio.ByteBuffer;

public class StructNlMsgErr {
    public static final int STRUCT_SIZE = 20;
    public int error;
    public StructNlMsgHdr msg;

    public static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= 20;
    }

    public static StructNlMsgErr parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) {
            return null;
        }
        StructNlMsgErr struct = new StructNlMsgErr();
        struct.error = byteBuffer.getInt();
        struct.msg = StructNlMsgHdr.parse(byteBuffer);
        return struct;
    }

    public void pack(ByteBuffer byteBuffer) {
        byteBuffer.putInt(this.error);
        StructNlMsgHdr structNlMsgHdr = this.msg;
        if (structNlMsgHdr != null) {
            structNlMsgHdr.pack(byteBuffer);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StructNlMsgErr{ error{");
        sb.append(this.error);
        sb.append("}, msg{");
        StructNlMsgHdr structNlMsgHdr = this.msg;
        sb.append(structNlMsgHdr == null ? "" : structNlMsgHdr.toString());
        sb.append("} }");
        return sb.toString();
    }
}
