package org.bouncycastle.asn1;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.util.io.Streams;

class DefiniteLengthInputStream extends LimitedInputStream {
    private static final byte[] EMPTY_BYTES = new byte[0];
    private final int _originalLength;
    private int _remaining;

    DefiniteLengthInputStream(InputStream inputStream, int i) {
        super(inputStream, i);
        if (i >= 0) {
            this._originalLength = i;
            this._remaining = i;
            if (i == 0) {
                setParentEofDetect(true);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("negative lengths not allowed");
    }

    /* access modifiers changed from: package-private */
    public int getRemaining() {
        return this._remaining;
    }

    public int read() throws IOException {
        if (this._remaining == 0) {
            return -1;
        }
        int read = this._in.read();
        if (read >= 0) {
            int i = this._remaining - 1;
            this._remaining = i;
            if (i == 0) {
                setParentEofDetect(true);
            }
            return read;
        }
        throw new EOFException("DEF length " + this._originalLength + " object truncated by " + this._remaining);
    }

    public int read(byte[] bArr, int i, int i2) throws IOException {
        if (this._remaining == 0) {
            return -1;
        }
        int read = this._in.read(bArr, i, Math.min(i2, this._remaining));
        if (read >= 0) {
            int i3 = this._remaining - read;
            this._remaining = i3;
            if (i3 == 0) {
                setParentEofDetect(true);
            }
            return read;
        }
        throw new EOFException("DEF length " + this._originalLength + " object truncated by " + this._remaining);
    }

    /* access modifiers changed from: package-private */
    public byte[] toByteArray() throws IOException {
        if (this._remaining == 0) {
            return EMPTY_BYTES;
        }
        byte[] bArr = new byte[this._remaining];
        int readFully = this._remaining - Streams.readFully(this._in, bArr);
        this._remaining = readFully;
        if (readFully == 0) {
            setParentEofDetect(true);
            return bArr;
        }
        throw new EOFException("DEF length " + this._originalLength + " object truncated by " + this._remaining);
    }
}
