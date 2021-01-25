package org.bouncycastle.mime;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.util.Strings;

public class BoundaryLimitedInputStream extends InputStream {
    private final byte[] boundary;
    private final byte[] buf;
    private int bufOff = 0;
    private boolean ended = false;
    private int index = 0;
    private int lastI;
    private final InputStream src;

    public BoundaryLimitedInputStream(InputStream inputStream, String str) {
        this.src = inputStream;
        this.boundary = Strings.toByteArray(str);
        this.buf = new byte[(str.length() + 3)];
        this.bufOff = 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0054  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a8  */
    @Override // java.io.InputStream
    public int read() throws IOException {
        int i;
        int i2;
        int read;
        if (this.ended) {
            return -1;
        }
        int i3 = this.index;
        int i4 = this.bufOff;
        if (i3 < i4) {
            byte[] bArr = this.buf;
            this.index = i3 + 1;
            i = bArr[i3] & 255;
            if (this.index < i4) {
                return i;
            }
            this.bufOff = 0;
            this.index = 0;
        } else {
            i = this.src.read();
        }
        this.lastI = i;
        if (i < 0) {
            return -1;
        }
        if (i == 13 || i == 10) {
            this.index = 0;
            if (i == 13) {
                i2 = this.src.read();
                if (i2 == 10) {
                    byte[] bArr2 = this.buf;
                    int i5 = this.bufOff;
                    this.bufOff = i5 + 1;
                    bArr2[i5] = 10;
                }
                if (i2 == 45) {
                    byte[] bArr3 = this.buf;
                    int i6 = this.bufOff;
                    this.bufOff = i6 + 1;
                    bArr3[i6] = 45;
                    i2 = this.src.read();
                }
                if (i2 != 45) {
                    byte[] bArr4 = this.buf;
                    int i7 = this.bufOff;
                    this.bufOff = i7 + 1;
                    bArr4[i7] = 45;
                    int i8 = this.bufOff;
                    while (true) {
                        if (this.bufOff - i8 == this.boundary.length || (read = this.src.read()) < 0) {
                            break;
                        }
                        byte[] bArr5 = this.buf;
                        int i9 = this.bufOff;
                        bArr5[i9] = (byte) read;
                        if (bArr5[i9] != this.boundary[i9 - i8]) {
                            this.bufOff = i9 + 1;
                            break;
                        }
                        this.bufOff = i9 + 1;
                    }
                    if (this.bufOff - i8 == this.boundary.length) {
                        this.ended = true;
                        return -1;
                    }
                } else if (i2 >= 0) {
                    byte[] bArr6 = this.buf;
                    int i10 = this.bufOff;
                    this.bufOff = i10 + 1;
                    bArr6[i10] = (byte) i2;
                }
            }
            i2 = this.src.read();
            if (i2 == 45) {
            }
            if (i2 != 45) {
            }
        }
        return i;
    }
}
