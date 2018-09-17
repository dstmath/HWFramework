package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class StringPrepDataReader implements Authenticate {
    private static final int DATA_FORMAT_ID = 1397772880;
    private static final byte[] DATA_FORMAT_VERSION = new byte[]{(byte) 3, (byte) 2, (byte) 5, (byte) 2};
    private static final boolean debug = ICUDebug.enabled("NormalizerDataReader");
    private ByteBuffer byteBuffer;
    private int unicodeVersion;

    public StringPrepDataReader(ByteBuffer bytes) throws IOException {
        if (debug) {
            System.out.println("Bytes in buffer " + bytes.remaining());
        }
        this.byteBuffer = bytes;
        this.unicodeVersion = ICUBinary.readHeader(this.byteBuffer, DATA_FORMAT_ID, this);
        if (debug) {
            System.out.println("Bytes left in byteBuffer " + this.byteBuffer.remaining());
        }
    }

    public char[] read(int length) throws IOException {
        return ICUBinary.getChars(this.byteBuffer, length, 0);
    }

    public boolean isDataVersionAcceptable(byte[] version) {
        if (version[0] == DATA_FORMAT_VERSION[0] && version[2] == DATA_FORMAT_VERSION[2] && version[3] == DATA_FORMAT_VERSION[3]) {
            return true;
        }
        return false;
    }

    public int[] readIndexes(int length) throws IOException {
        int[] indexes = new int[length];
        for (int i = 0; i < length; i++) {
            indexes[i] = this.byteBuffer.getInt();
        }
        return indexes;
    }

    public byte[] getUnicodeVersion() {
        return ICUBinary.getVersionByteArrayFromCompactInt(this.unicodeVersion);
    }
}
