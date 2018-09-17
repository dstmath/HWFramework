package sun.misc;

import java.io.IOException;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.io.PrintStream;

public class HexDumpEncoder extends CharacterEncoder {
    private int currentByte;
    private int offset;
    private byte[] thisLine = new byte[16];
    private int thisLineLength;

    static void hexDigit(PrintStream p, byte x) {
        int c;
        char c2 = (char) ((x >> 4) & 15);
        if (c2 > 9) {
            c = (char) ((c2 - 10) + 65);
        } else {
            c = (char) (c2 + 48);
        }
        p.write(c);
        c2 = (char) (x & 15);
        if (c2 > 9) {
            c = (char) ((c2 - 10) + 65);
        } else {
            c = (char) (c2 + 48);
        }
        p.write(c);
    }

    protected int bytesPerAtom() {
        return 1;
    }

    protected int bytesPerLine() {
        return 16;
    }

    protected void encodeBufferPrefix(OutputStream o) throws IOException {
        this.offset = 0;
        super.encodeBufferPrefix(o);
    }

    protected void encodeLinePrefix(OutputStream o, int len) throws IOException {
        hexDigit(this.pStream, (byte) ((this.offset >>> 8) & 255));
        hexDigit(this.pStream, (byte) (this.offset & 255));
        this.pStream.print(": ");
        this.currentByte = 0;
        this.thisLineLength = len;
    }

    protected void encodeAtom(OutputStream o, byte[] buf, int off, int len) throws IOException {
        this.thisLine[this.currentByte] = buf[off];
        hexDigit(this.pStream, buf[off]);
        this.pStream.print(" ");
        this.currentByte++;
        if (this.currentByte == 8) {
            this.pStream.print("  ");
        }
    }

    protected void encodeLineSuffix(OutputStream o) throws IOException {
        int i;
        if (this.thisLineLength < 16) {
            for (i = this.thisLineLength; i < 16; i++) {
                this.pStream.print("   ");
                if (i == 7) {
                    this.pStream.print("  ");
                }
            }
        }
        this.pStream.print(" ");
        i = 0;
        while (i < this.thisLineLength) {
            if (this.thisLine[i] < (byte) 32 || this.thisLine[i] > ObjectStreamConstants.TC_BLOCKDATALONG) {
                this.pStream.print(".");
            } else {
                this.pStream.write(this.thisLine[i]);
            }
            i++;
        }
        this.pStream.println();
        this.offset += this.thisLineLength;
    }
}
