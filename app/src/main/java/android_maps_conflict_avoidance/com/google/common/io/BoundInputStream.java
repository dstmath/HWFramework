package android_maps_conflict_avoidance.com.google.common.io;

import java.io.IOException;
import java.io.InputStream;

public class BoundInputStream extends InputStream {
    private InputStream base;
    private byte[] buf;
    private int bufPos;
    private int bufSize;
    private int remaining;

    public BoundInputStream(InputStream base, int len) {
        this.base = base;
        this.remaining = len;
        this.buf = new byte[Math.min(len, 4096)];
    }

    private boolean checkBuf() throws IOException {
        if (this.remaining <= 0) {
            return false;
        }
        if (this.bufPos >= this.bufSize) {
            this.bufSize = this.base.read(this.buf, 0, Math.min(this.remaining, this.buf.length));
            if (this.bufSize > 0) {
                this.bufPos = 0;
            } else {
                this.remaining = 0;
                return false;
            }
        }
        return true;
    }

    public int available() {
        return this.bufSize - this.bufPos;
    }

    public int read() throws IOException {
        if (!checkBuf()) {
            return -1;
        }
        this.remaining--;
        byte[] bArr = this.buf;
        int i = this.bufPos;
        this.bufPos = i + 1;
        return bArr[i] & 255;
    }

    public int read(byte[] data, int start, int count) throws IOException {
        if (!checkBuf()) {
            return -1;
        }
        count = Math.min(count, this.bufSize - this.bufPos);
        System.arraycopy(this.buf, this.bufPos, data, start, count);
        this.bufPos += count;
        this.remaining -= count;
        return count;
    }
}
