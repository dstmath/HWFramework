package org.bouncycastle.mime.encoding;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;

public class Base64InputStream extends InputStream {
    private static final byte[] decodingTable = new byte[128];
    int bufPtr = 3;
    InputStream in;
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
            if (read != 9 && read != 32) {
                return read;
            }
        }
    }

    private int readIgnoreSpaceFirst() throws IOException {
        while (true) {
            int read = this.in.read();
            if (read != 9 && read != 10 && read != 13 && read != 32) {
                return read;
            }
        }
    }

    @Override // java.io.InputStream
    public int available() throws IOException {
        return 0;
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.in.close();
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        if (this.bufPtr > 2) {
            int readIgnoreSpaceFirst = readIgnoreSpaceFirst();
            if (readIgnoreSpaceFirst < 0) {
                return -1;
            }
            this.bufPtr = decode(readIgnoreSpaceFirst, readIgnoreSpace(), readIgnoreSpace(), readIgnoreSpace(), this.outBuf);
        }
        int[] iArr = this.outBuf;
        int i = this.bufPtr;
        this.bufPtr = i + 1;
        return iArr[i];
    }
}
