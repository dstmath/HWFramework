package android.net.netlink;

import java.nio.ByteBuffer;

public class StructNfGenMsg {
    public static final int NFNETLINK_V0 = 0;
    public static final int STRUCT_SIZE = 4;
    public final byte nfgen_family;
    public final short res_id = 0;
    public final byte version = 0;

    public StructNfGenMsg(byte family) {
        this.nfgen_family = family;
    }

    public void pack(ByteBuffer byteBuffer) {
        byteBuffer.put(this.nfgen_family);
        byteBuffer.put(this.version);
        byteBuffer.putShort(this.res_id);
    }
}
