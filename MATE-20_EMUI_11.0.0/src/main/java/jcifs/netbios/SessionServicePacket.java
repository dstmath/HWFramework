package jcifs.netbios;

import java.io.IOException;
import java.io.InputStream;

public abstract class SessionServicePacket {
    static final int HEADER_LENGTH = 4;
    static final int MAX_MESSAGE_SIZE = 131071;
    public static final int NEGATIVE_SESSION_RESPONSE = 131;
    public static final int POSITIVE_SESSION_RESPONSE = 130;
    static final int SESSION_KEEP_ALIVE = 133;
    static final int SESSION_MESSAGE = 0;
    static final int SESSION_REQUEST = 129;
    static final int SESSION_RETARGET_RESPONSE = 132;
    int length;
    int type;

    /* access modifiers changed from: package-private */
    public abstract int readTrailerWireFormat(InputStream inputStream, byte[] bArr, int i) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract int writeTrailerWireFormat(byte[] bArr, int i);

    static void writeInt2(int val, byte[] dst, int dstIndex) {
        dst[dstIndex] = (byte) ((val >> 8) & 255);
        dst[dstIndex + 1] = (byte) (val & 255);
    }

    static void writeInt4(int val, byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) ((val >> 24) & 255);
        int dstIndex3 = dstIndex2 + 1;
        dst[dstIndex2] = (byte) ((val >> 16) & 255);
        dst[dstIndex3] = (byte) ((val >> 8) & 255);
        dst[dstIndex3 + 1] = (byte) (val & 255);
    }

    static int readInt2(byte[] src, int srcIndex) {
        return ((src[srcIndex] & 255) << 8) + (src[srcIndex + 1] & 255);
    }

    static int readInt4(byte[] src, int srcIndex) {
        return ((src[srcIndex] & 255) << 24) + ((src[srcIndex + 1] & 255) << 16) + ((src[srcIndex + 2] & 255) << 8) + (src[srcIndex + 3] & 255);
    }

    static int readLength(byte[] src, int srcIndex) {
        int srcIndex2 = srcIndex + 1;
        int srcIndex3 = srcIndex2 + 1;
        int srcIndex4 = srcIndex3 + 1;
        int i = srcIndex4 + 1;
        return ((src[srcIndex2] & 1) << 16) + ((src[srcIndex3] & 255) << 8) + (src[srcIndex4] & 255);
    }

    static int readn(InputStream in, byte[] b, int off, int len) throws IOException {
        int i = 0;
        while (i < len) {
            int n = in.read(b, off + i, len - i);
            if (n <= 0) {
                break;
            }
            i += n;
        }
        return i;
    }

    static int readPacketType(InputStream in, byte[] buffer, int bufferIndex) throws IOException {
        int n = readn(in, buffer, bufferIndex, 4);
        if (n == 4) {
            return buffer[bufferIndex] & 255;
        }
        if (n == -1) {
            return -1;
        }
        throw new IOException("unexpected EOF reading netbios session header");
    }

    public int writeWireFormat(byte[] dst, int dstIndex) {
        this.length = writeTrailerWireFormat(dst, dstIndex + 4);
        writeHeaderWireFormat(dst, dstIndex);
        return this.length + 4;
    }

    /* access modifiers changed from: package-private */
    public int readWireFormat(InputStream in, byte[] buffer, int bufferIndex) throws IOException {
        readHeaderWireFormat(in, buffer, bufferIndex);
        return readTrailerWireFormat(in, buffer, bufferIndex) + 4;
    }

    /* access modifiers changed from: package-private */
    public int writeHeaderWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) this.type;
        if (this.length > 65535) {
            dst[dstIndex2] = 1;
        }
        writeInt2(this.length, dst, dstIndex2 + 1);
        return 4;
    }

    /* access modifiers changed from: package-private */
    public int readHeaderWireFormat(InputStream in, byte[] buffer, int bufferIndex) throws IOException {
        int bufferIndex2 = bufferIndex + 1;
        this.type = buffer[bufferIndex] & 255;
        this.length = ((buffer[bufferIndex2] & 1) << 16) + readInt2(buffer, bufferIndex2 + 1);
        return 4;
    }
}
