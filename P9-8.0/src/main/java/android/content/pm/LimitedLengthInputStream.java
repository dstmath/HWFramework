package android.content.pm;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class LimitedLengthInputStream extends FilterInputStream {
    private final long mEnd;
    private long mOffset;

    public LimitedLengthInputStream(InputStream in, long offset, long length) throws IOException {
        super(in);
        if (in == null) {
            throw new IOException("in == null");
        } else if (offset < 0) {
            throw new IOException("offset < 0");
        } else if (length < 0) {
            throw new IOException("length < 0");
        } else if (length > Long.MAX_VALUE - offset) {
            throw new IOException("offset + length > Long.MAX_VALUE");
        } else {
            this.mEnd = offset + length;
            skip(offset);
            this.mOffset = offset;
        }
    }

    public synchronized int read() throws IOException {
        if (this.mOffset >= this.mEnd) {
            return -1;
        }
        this.mOffset++;
        return super.read();
    }

    public int read(byte[] buffer, int offset, int byteCount) throws IOException {
        if (this.mOffset >= this.mEnd) {
            return -1;
        }
        Arrays.checkOffsetAndCount(buffer.length, offset, byteCount);
        if (this.mOffset > Long.MAX_VALUE - ((long) byteCount)) {
            throw new IOException("offset out of bounds: " + this.mOffset + " + " + byteCount);
        }
        if (this.mOffset + ((long) byteCount) > this.mEnd) {
            byteCount = (int) (this.mEnd - this.mOffset);
        }
        int numRead = super.read(buffer, offset, byteCount);
        this.mOffset += (long) numRead;
        return numRead;
    }

    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }
}
