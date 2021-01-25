package org.bouncycastle.mime.encoding;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class QuotedPrintableInputStream extends FilterInputStream {
    public QuotedPrintableInputStream(InputStream inputStream) {
        super(inputStream);
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read() throws IOException {
        int i;
        int i2;
        int read = this.in.read();
        if (read == -1) {
            return -1;
        }
        while (read == 61) {
            int read2 = this.in.read();
            if (read2 != -1) {
                if (read2 == 13) {
                    read = this.in.read();
                    if (read != 10) {
                    }
                } else if (read2 != 10) {
                    if (read2 >= 48 && read2 <= 57) {
                        i = read2 - 48;
                    } else if (read2 < 65 || read2 > 70) {
                        throw new IllegalStateException("Expecting '0123456789ABCDEF after quote that was not immediately followed by LF or CRLF");
                    } else {
                        i = (read2 - 65) + 10;
                    }
                    int i3 = i << 4;
                    int read3 = this.in.read();
                    if (read3 >= 48 && read3 <= 57) {
                        i2 = read3 - 48;
                    } else if (read3 < 65 || read3 > 70) {
                        throw new IllegalStateException("Expecting second '0123456789ABCDEF after quote that was not immediately followed by LF or CRLF");
                    } else {
                        i2 = (read3 - 65) + 10;
                    }
                    return i3 | i2;
                }
                read = this.in.read();
            } else {
                throw new IllegalStateException("Quoted '=' at end of stream");
            }
        }
        return read;
    }

    @Override // java.io.FilterInputStream, java.io.InputStream
    public int read(byte[] bArr, int i, int i2) throws IOException {
        int i3 = 0;
        while (i3 != i2) {
            int read = read();
            if (read < 0) {
                break;
            }
            bArr[i3 + i] = (byte) read;
            i3++;
        }
        if (i3 == 0) {
            return -1;
        }
        return i3;
    }
}
