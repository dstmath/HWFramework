package sun.net;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TelnetInputStream extends FilterInputStream {
    public boolean binaryMode = false;
    boolean seenCR = false;
    boolean stickyCRLF = false;

    public TelnetInputStream(InputStream fd, boolean binary) {
        super(fd);
        this.binaryMode = binary;
    }

    public void setStickyCRLF(boolean on) {
        this.stickyCRLF = on;
    }

    public int read() throws IOException {
        if (this.binaryMode) {
            return super.read();
        }
        if (this.seenCR) {
            this.seenCR = false;
            return 10;
        }
        int c = super.read();
        if (c != 13) {
            return c;
        }
        switch (super.read()) {
            case 0:
                return 13;
            case 10:
                if (!this.stickyCRLF) {
                    return 10;
                }
                this.seenCR = true;
                return 13;
            default:
                throw new TelnetProtocolException("misplaced CR in input");
        }
    }

    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    public int read(byte[] bytes, int off, int length) throws IOException {
        int i = -1;
        if (this.binaryMode) {
            return super.read(bytes, off, length);
        }
        int i2;
        int offStart = off;
        while (true) {
            i2 = off;
            length--;
            if (length < 0) {
                break;
            }
            int c = read();
            if (c == -1) {
                break;
            }
            off = i2 + 1;
            bytes[i2] = (byte) c;
        }
        if (i2 > offStart) {
            i = i2 - offStart;
        }
        return i;
    }
}
