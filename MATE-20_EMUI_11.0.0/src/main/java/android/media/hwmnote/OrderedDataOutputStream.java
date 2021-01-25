package android.media.hwmnote;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class OrderedDataOutputStream extends FilterOutputStream {
    private static final int BYTE_BUFFER_ARRAY_LEN = 2;
    private static final int BYTE_BUFFER_LEN = 4;
    private final ByteBuffer mByteBuffer = ByteBuffer.allocate(4);

    public OrderedDataOutputStream(OutputStream out) {
        super(out);
    }

    public OrderedDataOutputStream setByteOrder(ByteOrder order) {
        this.mByteBuffer.order(order);
        return this;
    }

    public OrderedDataOutputStream writeShort(short value) throws IOException {
        this.mByteBuffer.rewind();
        this.mByteBuffer.putShort(value);
        this.out.write(this.mByteBuffer.array(), 0, 2);
        return this;
    }

    public OrderedDataOutputStream writeInt(int value) throws IOException {
        this.mByteBuffer.rewind();
        this.mByteBuffer.putInt(value);
        this.out.write(this.mByteBuffer.array());
        return this;
    }
}
