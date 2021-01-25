package android.net.netlink;

import android.hardware.contexthub.V1_0.HostEndPoint;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StructNlAttr {
    public static final int NLA_F_NESTED = 32768;
    public static final int NLA_HEADERLEN = 4;
    private ByteOrder mByteOrder;
    public short nla_len;
    public short nla_type;
    public byte[] nla_value;

    public static short makeNestedType(short type) {
        return (short) (32768 | type);
    }

    /* JADX INFO: finally extract failed */
    public static StructNlAttr peek(ByteBuffer byteBuffer) {
        if (byteBuffer == null || byteBuffer.remaining() < 4) {
            return null;
        }
        int baseOffset = byteBuffer.position();
        StructNlAttr struct = new StructNlAttr(byteBuffer.order());
        ByteOrder originalOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.nativeOrder());
        try {
            struct.nla_len = byteBuffer.getShort();
            struct.nla_type = byteBuffer.getShort();
            byteBuffer.order(originalOrder);
            byteBuffer.position(baseOffset);
            if (struct.nla_len < 4) {
                return null;
            }
            return struct;
        } catch (Throwable th) {
            byteBuffer.order(originalOrder);
            throw th;
        }
    }

    public static StructNlAttr parse(ByteBuffer byteBuffer) {
        StructNlAttr struct = peek(byteBuffer);
        if (struct == null || byteBuffer.remaining() < struct.getAlignedLength()) {
            return null;
        }
        int baseOffset = byteBuffer.position();
        byteBuffer.position(baseOffset + 4);
        int valueLen = (struct.nla_len & HostEndPoint.BROADCAST) - 4;
        if (valueLen > 0) {
            struct.nla_value = new byte[valueLen];
            byteBuffer.get(struct.nla_value, 0, valueLen);
            byteBuffer.position(struct.getAlignedLength() + baseOffset);
        }
        return struct;
    }

    public StructNlAttr() {
        this.nla_len = 4;
        this.mByteOrder = ByteOrder.nativeOrder();
    }

    public StructNlAttr(ByteOrder byteOrder) {
        this.nla_len = 4;
        this.mByteOrder = ByteOrder.nativeOrder();
        this.mByteOrder = byteOrder;
    }

    public StructNlAttr(short type, byte value) {
        this.nla_len = 4;
        this.mByteOrder = ByteOrder.nativeOrder();
        this.nla_type = type;
        setValue(new byte[1]);
        this.nla_value[0] = value;
    }

    public StructNlAttr(short type, short value) {
        this(type, value, ByteOrder.nativeOrder());
    }

    public StructNlAttr(short type, short value, ByteOrder order) {
        this(order);
        this.nla_type = type;
        setValue(new byte[2]);
        getValueAsByteBuffer().putShort(value);
    }

    public StructNlAttr(short type, int value) {
        this(type, value, ByteOrder.nativeOrder());
    }

    public StructNlAttr(short type, int value, ByteOrder order) {
        this(order);
        this.nla_type = type;
        setValue(new byte[4]);
        getValueAsByteBuffer().putInt(value);
    }

    public StructNlAttr(short type, InetAddress ip) {
        this.nla_len = 4;
        this.mByteOrder = ByteOrder.nativeOrder();
        this.nla_type = type;
        setValue(ip.getAddress());
    }

    public StructNlAttr(short type, StructNlAttr... nested) {
        this();
        this.nla_type = makeNestedType(type);
        int payloadLength = 0;
        for (StructNlAttr nla : nested) {
            payloadLength += nla.getAlignedLength();
        }
        setValue(new byte[payloadLength]);
        ByteBuffer buf = getValueAsByteBuffer();
        for (StructNlAttr nla2 : nested) {
            nla2.pack(buf);
        }
    }

    public int getAlignedLength() {
        return NetlinkConstants.alignedLengthOf(this.nla_len);
    }

    public ByteBuffer getValueAsByteBuffer() {
        byte[] bArr = this.nla_value;
        if (bArr == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bArr);
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
        byte[] bArr = this.nla_value;
        if (bArr == null) {
            return null;
        }
        try {
            return InetAddress.getByAddress(bArr);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /* JADX INFO: finally extract failed */
    public void pack(ByteBuffer byteBuffer) {
        ByteOrder originalOrder = byteBuffer.order();
        int originalPosition = byteBuffer.position();
        byteBuffer.order(ByteOrder.nativeOrder());
        try {
            byteBuffer.putShort(this.nla_len);
            byteBuffer.putShort(this.nla_type);
            if (this.nla_value != null) {
                byteBuffer.put(this.nla_value);
            }
            byteBuffer.order(originalOrder);
            byteBuffer.position(getAlignedLength() + originalPosition);
        } catch (Throwable th) {
            byteBuffer.order(originalOrder);
            throw th;
        }
    }

    private void setValue(byte[] value) {
        this.nla_value = value;
        byte[] bArr = this.nla_value;
        this.nla_len = (short) ((bArr != null ? bArr.length : 0) + 4);
    }

    public String toString() {
        return "StructNlAttr{ nla_len{" + ((int) this.nla_len) + "}, nla_type{" + ((int) this.nla_type) + "}, nla_value{" + NetlinkConstants.hexify(this.nla_value) + "}, }";
    }
}
