package com.android.org.bouncycastle.util.encoders;

import com.android.org.bouncycastle.asn1.x509.ReasonFlags;
import com.android.org.bouncycastle.math.ec.ECFieldElement.F2m;
import com.android.org.bouncycastle.x509.ExtendedPKIXParameters;
import java.io.IOException;
import java.io.OutputStream;

public class Base64Encoder implements Encoder {
    protected final byte[] decodingTable;
    protected final byte[] encodingTable;
    protected byte padding;

    protected void initialiseDecodingTable() {
        int i;
        for (i = 0; i < this.decodingTable.length; i++) {
            this.decodingTable[i] = (byte) -1;
        }
        for (i = 0; i < this.encodingTable.length; i++) {
            this.decodingTable[this.encodingTable[i]] = (byte) i;
        }
    }

    public Base64Encoder() {
        this.encodingTable = new byte[]{(byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 43, (byte) 47};
        this.padding = (byte) 61;
        this.decodingTable = new byte[ReasonFlags.unused];
        initialiseDecodingTable();
    }

    public int encode(byte[] data, int off, int length, OutputStream out) throws IOException {
        int i;
        int modulus = length % 3;
        int dataLength = length - modulus;
        for (int i2 = off; i2 < off + dataLength; i2 += 3) {
            int a1 = data[i2] & 255;
            int a2 = data[i2 + 1] & 255;
            int a3 = data[i2 + 2] & 255;
            out.write(this.encodingTable[(a1 >>> 2) & 63]);
            out.write(this.encodingTable[((a1 << 4) | (a2 >>> 4)) & 63]);
            out.write(this.encodingTable[((a2 << 2) | (a3 >>> 6)) & 63]);
            out.write(this.encodingTable[a3 & 63]);
        }
        int d1;
        int b2;
        OutputStream outputStream;
        switch (modulus) {
            case ExtendedPKIXParameters.CHAIN_VALIDITY_MODEL /*1*/:
                d1 = data[off + dataLength] & 255;
                b2 = (d1 << 4) & 63;
                outputStream = out;
                outputStream.write(this.encodingTable[(d1 >>> 2) & 63]);
                out.write(this.encodingTable[b2]);
                out.write(this.padding);
                out.write(this.padding);
                break;
            case F2m.TPB /*2*/:
                d1 = data[off + dataLength] & 255;
                int d2 = data[(off + dataLength) + 1] & 255;
                b2 = ((d1 << 4) | (d2 >>> 4)) & 63;
                int b3 = (d2 << 2) & 63;
                outputStream = out;
                outputStream.write(this.encodingTable[(d1 >>> 2) & 63]);
                out.write(this.encodingTable[b2]);
                out.write(this.encodingTable[b3]);
                out.write(this.padding);
                break;
        }
        int i3 = (dataLength / 3) * 4;
        if (modulus == 0) {
            i = 0;
        } else {
            i = 4;
        }
        return i + i3;
    }

    private boolean ignore(char c) {
        return c == '\n' || c == '\r' || c == '\t' || c == ' ';
    }

    public int decode(byte[] data, int off, int length, OutputStream out) throws IOException {
        int outLen = 0;
        int end = off + length;
        while (end > off && ignore((char) data[end - 1])) {
            end--;
        }
        int i = off;
        int finish = end - 4;
        int i2 = nextI(data, off, finish);
        while (i2 < finish) {
            i = i2 + 1;
            byte b1 = this.decodingTable[data[i2]];
            i = nextI(data, i, finish);
            i2 = i + 1;
            byte b2 = this.decodingTable[data[i]];
            i = nextI(data, i2, finish);
            i2 = i + 1;
            byte b3 = this.decodingTable[data[i]];
            i = nextI(data, i2, finish);
            i2 = i + 1;
            byte b4 = this.decodingTable[data[i]];
            if ((((b1 | b2) | b3) | b4) < 0) {
                throw new IOException("invalid characters encountered in base64 data");
            }
            out.write((b1 << 2) | (b2 >> 4));
            out.write((b2 << 4) | (b3 >> 2));
            out.write((b3 << 6) | b4);
            outLen += 3;
            i2 = nextI(data, i2, finish);
        }
        return outLen + decodeLastBlock(out, (char) data[end - 4], (char) data[end - 3], (char) data[end - 2], (char) data[end - 1]);
    }

    private int nextI(byte[] data, int i, int finish) {
        while (i < finish && ignore((char) data[i])) {
            i++;
        }
        return i;
    }

    public int decode(String data, OutputStream out) throws IOException {
        int length = 0;
        int end = data.length();
        while (end > 0) {
            if (!ignore(data.charAt(end - 1))) {
                break;
            }
            end--;
        }
        int finish = end - 4;
        int nextI = nextI(data, 0, finish);
        while (nextI < finish) {
            int i = nextI + 1;
            byte b1 = this.decodingTable[data.charAt(nextI)];
            i = nextI(data, i, finish);
            nextI = i + 1;
            byte b2 = this.decodingTable[data.charAt(i)];
            i = nextI(data, nextI, finish);
            nextI = i + 1;
            byte b3 = this.decodingTable[data.charAt(i)];
            i = nextI(data, nextI, finish);
            nextI = i + 1;
            byte b4 = this.decodingTable[data.charAt(i)];
            if ((((b1 | b2) | b3) | b4) < 0) {
                throw new IOException("invalid characters encountered in base64 data");
            }
            out.write((b1 << 2) | (b2 >> 4));
            out.write((b2 << 4) | (b3 >> 2));
            out.write((b3 << 6) | b4);
            length += 3;
            nextI = nextI(data, nextI, finish);
        }
        return length + decodeLastBlock(out, data.charAt(end - 4), data.charAt(end - 3), data.charAt(end - 2), data.charAt(end - 1));
    }

    private int decodeLastBlock(OutputStream out, char c1, char c2, char c3, char c4) throws IOException {
        byte b1;
        byte b2;
        if (c3 == this.padding) {
            b1 = this.decodingTable[c1];
            b2 = this.decodingTable[c2];
            if ((b1 | b2) < 0) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            out.write((b1 << 2) | (b2 >> 4));
            return 1;
        } else if (c4 == this.padding) {
            b1 = this.decodingTable[c1];
            b2 = this.decodingTable[c2];
            b3 = this.decodingTable[c3];
            if (((b1 | b2) | b3) < 0) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            out.write((b1 << 2) | (b2 >> 4));
            out.write((b2 << 4) | (b3 >> 2));
            return 2;
        } else {
            b1 = this.decodingTable[c1];
            b2 = this.decodingTable[c2];
            b3 = this.decodingTable[c3];
            byte b4 = this.decodingTable[c4];
            if ((((b1 | b2) | b3) | b4) < 0) {
                throw new IOException("invalid characters encountered at end of base64 data");
            }
            out.write((b1 << 2) | (b2 >> 4));
            out.write((b2 << 4) | (b3 >> 2));
            out.write((b3 << 6) | b4);
            return 3;
        }
    }

    private int nextI(String data, int i, int finish) {
        while (i < finish && ignore(data.charAt(i))) {
            i++;
        }
        return i;
    }
}
