package sun.net;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TelnetOutputStream extends BufferedOutputStream {
    public boolean binaryMode = false;
    boolean seenCR = false;
    boolean stickyCRLF = false;

    public TelnetOutputStream(OutputStream fd, boolean binary) {
        super(fd);
        this.binaryMode = binary;
    }

    public void setStickyCRLF(boolean on) {
        this.stickyCRLF = on;
    }

    public void write(int c) throws IOException {
        if (this.binaryMode) {
            super.write(c);
            return;
        }
        if (this.seenCR) {
            if (c != 10) {
                super.write(0);
            }
            super.write(c);
            if (c != 13) {
                this.seenCR = false;
            }
        } else if (c == 10) {
            super.write(13);
            super.write(10);
        } else {
            if (c == 13) {
                if (this.stickyCRLF) {
                    this.seenCR = true;
                } else {
                    super.write(13);
                    c = 0;
                }
            }
            super.write(c);
        }
    }

    public void write(byte[] bytes, int off, int length) throws IOException {
        if (this.binaryMode) {
            super.write(bytes, off, length);
            return;
        }
        while (true) {
            int i = off;
            length--;
            if (length >= 0) {
                off = i + 1;
                write(bytes[i]);
            } else {
                return;
            }
        }
    }
}
