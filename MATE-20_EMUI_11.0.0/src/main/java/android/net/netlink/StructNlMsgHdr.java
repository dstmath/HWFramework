package android.net.netlink;

import java.nio.ByteBuffer;

public class StructNlMsgHdr {
    public static final short NLM_F_ACK = 4;
    public static final short NLM_F_APPEND = 2048;
    public static final short NLM_F_CREATE = 1024;
    public static final short NLM_F_DUMP = 768;
    public static final short NLM_F_ECHO = 8;
    public static final short NLM_F_EXCL = 512;
    public static final short NLM_F_MATCH = 512;
    public static final short NLM_F_MULTI = 2;
    public static final short NLM_F_REPLACE = 256;
    public static final short NLM_F_REQUEST = 1;
    public static final short NLM_F_ROOT = 256;
    public static final int STRUCT_SIZE = 16;
    public short nlmsg_flags = 0;
    public int nlmsg_len = 0;
    public int nlmsg_pid = 0;
    public int nlmsg_seq = 0;
    public short nlmsg_type = 0;

    public static String stringForNlMsgFlags(short flags) {
        StringBuilder sb = new StringBuilder();
        if ((flags & 1) != 0) {
            sb.append("NLM_F_REQUEST");
        }
        if ((flags & 2) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("NLM_F_MULTI");
        }
        if ((flags & 4) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("NLM_F_ACK");
        }
        if ((flags & 8) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("NLM_F_ECHO");
        }
        if ((flags & 256) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("NLM_F_ROOT");
        }
        if ((flags & 512) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("NLM_F_MATCH");
        }
        return sb.toString();
    }

    public static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= 16;
    }

    public static StructNlMsgHdr parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) {
            return null;
        }
        StructNlMsgHdr struct = new StructNlMsgHdr();
        struct.nlmsg_len = byteBuffer.getInt();
        struct.nlmsg_type = byteBuffer.getShort();
        struct.nlmsg_flags = byteBuffer.getShort();
        struct.nlmsg_seq = byteBuffer.getInt();
        struct.nlmsg_pid = byteBuffer.getInt();
        if (struct.nlmsg_len < 16) {
            return null;
        }
        return struct;
    }

    public void pack(ByteBuffer byteBuffer) {
        byteBuffer.putInt(this.nlmsg_len);
        byteBuffer.putShort(this.nlmsg_type);
        byteBuffer.putShort(this.nlmsg_flags);
        byteBuffer.putInt(this.nlmsg_seq);
        byteBuffer.putInt(this.nlmsg_pid);
    }

    public String toString() {
        return "StructNlMsgHdr{ nlmsg_len{" + this.nlmsg_len + "}, nlmsg_type{" + ("" + ((int) this.nlmsg_type) + "(" + NetlinkConstants.stringForNlMsgType(this.nlmsg_type) + ")") + "}, nlmsg_flags{" + ("" + ((int) this.nlmsg_flags) + "(" + stringForNlMsgFlags(this.nlmsg_flags) + ")") + ")}, nlmsg_seq{" + this.nlmsg_seq + "}, nlmsg_pid{" + this.nlmsg_pid + "} }";
    }
}
