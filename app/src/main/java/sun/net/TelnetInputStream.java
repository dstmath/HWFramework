package sun.net;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;

public class TelnetInputStream extends FilterInputStream {
    public boolean binaryMode;
    boolean seenCR;
    boolean stickyCRLF;

    public TelnetInputStream(InputStream fd, boolean binary) {
        super(fd);
        this.stickyCRLF = false;
        this.seenCR = false;
        this.binaryMode = false;
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
            case GeneralNameInterface.NAME_MATCH /*0*/:
                return 13;
            case BaseCalendar.OCTOBER /*10*/:
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
        int offStart = off;
        int off2 = off;
        while (true) {
            length--;
            if (length < 0) {
                break;
            }
            int c = read();
            if (c == -1) {
                break;
            }
            off = off2 + 1;
            bytes[off2] = (byte) c;
            off2 = off;
        }
        if (off2 > offStart) {
            i = off2 - offStart;
        }
        return i;
    }
}
