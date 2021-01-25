package org.bouncycastle.mime.encoding;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;

public class Base64InputStream extends InputStream {
    private static final byte[] decodingTable = new byte[128];
    int bufPtr = 3;
    InputStream in;
    boolean isEndOfStream;
    int[] outBuf = new int[3];

    static {
        for (int i = 65; i <= 90; i++) {
            decodingTable[i] = (byte) (i - 65);
        }
        for (int i2 = 97; i2 <= 122; i2++) {
            decodingTable[i2] = (byte) ((i2 - 97) + 26);
        }
        for (int i3 = 48; i3 <= 57; i3++) {
            decodingTable[i3] = (byte) ((i3 - 48) + 52);
        }
        byte[] bArr = decodingTable;
        bArr[43] = 62;
        bArr[47] = 63;
    }

    public Base64InputStream(InputStream inputStream) {
        this.in = inputStream;
    }

    private int decode(int i, int i2, int i3, int i4, int[] iArr) throws EOFException {
        if (i4 < 0) {
            throw new EOFException("unexpected end of file in armored stream.");
        } else if (i3 == 61) {
            byte[] bArr = decodingTable;
            iArr[2] = (((bArr[i] & 255) << 2) | ((bArr[i2] & 255) >> 4)) & GF2Field.MASK;
            return 2;
        } else if (i4 == 61) {
            byte[] bArr2 = decodingTable;
            byte b = bArr2[i];
            byte b2 = bArr2[i2];
            byte b3 = bArr2[i3];
            iArr[1] = ((b << 2) | (b2 >> 4)) & GF2Field.MASK;
            iArr[2] = ((b2 << 4) | (b3 >> 2)) & GF2Field.MASK;
            return 1;
        } else {
            byte[] bArr3 = decodingTable;
            byte b4 = bArr3[i];
            byte b5 = bArr3[i2];
            byte b6 = bArr3[i3];
            byte b7 = bArr3[i4];
            iArr[0] = ((b4 << 2) | (b5 >> 4)) & GF2Field.MASK;
            iArr[1] = ((b5 << 4) | (b6 >> 2)) & GF2Field.MASK;
            iArr[2] = ((b6 << 6) | b7) & GF2Field.MASK;
            return 0;
        }
    }

    private int readIgnoreSpace() throws IOException {
        while (true) {
            int read = this.in.read();
            if (read != 32 && read != 9) {
                return read;
            }
        }
    }

    @Override // java.io.InputStream
    public int available() throws IOException {
        return this.in.available();
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.in.close();
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        int decode;
        if (this.bufPtr > 2) {
            int readIgnoreSpace = readIgnoreSpace();
            if (readIgnoreSpace == 13 || readIgnoreSpace == 10) {
                int readIgnoreSpace2 = readIgnoreSpace();
                while (true) {
                    if (readIgnoreSpace2 != 10 && readIgnoreSpace2 != 13) {
                        break;
                    }
                    readIgnoreSpace2 = readIgnoreSpace();
                }
                if (readIgnoreSpace2 < 0) {
                    this.isEndOfStream = true;
                    return -1;
                }
                decode = decode(readIgnoreSpace2, readIgnoreSpace(), readIgnoreSpace(), readIgnoreSpace(), this.outBuf);
            } else if (readIgnoreSpace >= 0) {
                decode = decode(readIgnoreSpace, readIgnoreSpace(), readIgnoreSpace(), readIgnoreSpace(), this.outBuf);
            } else {
                this.isEndOfStream = true;
                return -1;
            }
            this.bufPtr = decode;
        }
        int[] iArr = this.outBuf;
        int i = this.bufPtr;
        this.bufPtr = i + 1;
        return iArr[i];
    }
}
