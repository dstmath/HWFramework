package android.net.netlink;

import java.nio.ByteBuffer;

public class StructInetDiagMsg {
    private static final int IDIAG_UID_OFFSET = 80;
    public static final int STRUCT_SIZE = 72;
    public int idiag_uid;

    public static StructInetDiagMsg parse(ByteBuffer byteBuffer) {
        StructInetDiagMsg struct = new StructInetDiagMsg();
        struct.idiag_uid = byteBuffer.getInt(80);
        return struct;
    }

    public String toString() {
        return "StructInetDiagMsg{ idiag_uid{" + this.idiag_uid + "}, }";
    }
}
