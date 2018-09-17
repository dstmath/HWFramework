package android.net.netlink;

import android.net.util.NetworkConstants;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StructNlAttr {
    public static final int NLA_HEADERLEN = 4;
    public ByteOrder mByteOrder = ByteOrder.nativeOrder();
    public short nla_len;
    public short nla_type;
    public byte[] nla_value;

    public static StructNlAttr peek(ByteBuffer byteBuffer) {
        if (byteBuffer == null || byteBuffer.remaining() < 4) {
            return null;
        }
        int baseOffset = byteBuffer.position();
        StructNlAttr struct = new StructNlAttr();
        struct.nla_len = byteBuffer.getShort();
        struct.nla_type = byteBuffer.getShort();
        struct.mByteOrder = byteBuffer.order();
        byteBuffer.position(baseOffset);
        if (struct.nla_len < (short) 4) {
            return null;
        }
        return struct;
    }

    public static StructNlAttr parse(ByteBuffer byteBuffer) {
        StructNlAttr struct = peek(byteBuffer);
        if (struct == null || byteBuffer.remaining() < struct.getAlignedLength()) {
            return null;
        }
        int baseOffset = byteBuffer.position();
        byteBuffer.position(baseOffset + 4);
        int valueLen = (struct.nla_len & NetworkConstants.ARP_HWTYPE_RESERVED_HI) - 4;
        if (valueLen > 0) {
            struct.nla_value = new byte[valueLen];
            byteBuffer.get(struct.nla_value, 0, valueLen);
            byteBuffer.position(struct.getAlignedLength() + baseOffset);
        }
        return struct;
    }

    public int getAlignedLength() {
        return NetlinkConstants.alignedLengthOf(this.nla_len);
    }

    public ByteBuffer getValueAsByteBuffer() {
        if (this.nla_value == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(this.nla_value);
        byteBuffer.order(this.mByteOrder);
        return byteBuffer;
    }

    public int getValueAsInt(int defaultValue) {
        ByteBuffer byteBuffer = getValueAsByteBuffer();
        if (byteBuffer == null || byteBuffer.remaining() != 4) {
            return defaultValue;
        }
        return getValueAsByteBuffer().getInt();
    }

    public InetAddress getValueAsInetAddress() {
        if (this.nla_value == null) {
            return null;
        }
        try {
            return InetAddress.getByAddress(this.nla_value);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public void pack(ByteBuffer byteBuffer) {
        int originalPosition = byteBuffer.position();
        byteBuffer.putShort(this.nla_len);
        byteBuffer.putShort(this.nla_type);
        byteBuffer.put(this.nla_value);
        byteBuffer.position(getAlignedLength() + originalPosition);
    }

    public String toString() {
        return "StructNlAttr{ nla_len{" + this.nla_len + "}, " + "nla_type{" + this.nla_type + "}, " + "nla_value{" + NetlinkConstants.hexify(this.nla_value) + "}, " + "}";
    }
}
