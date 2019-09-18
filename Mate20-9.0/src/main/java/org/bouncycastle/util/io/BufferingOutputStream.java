package org.bouncycastle.util.io;

import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.util.Arrays;

public class BufferingOutputStream extends OutputStream {
    private final byte[] buf;
    private int bufOff;
    private final OutputStream other;

    public BufferingOutputStream(OutputStream outputStream) {
        this.other = outputStream;
        this.buf = new byte[PKIFailureInfo.certConfirmed];
    }

    public BufferingOutputStream(OutputStream outputStream, int i) {
        this.other = outputStream;
        this.buf = new byte[i];
    }

    public void close() throws IOException {
        flush();
        this.other.close();
    }

    public void flush() throws IOException {
        this.other.write(this.buf, 0, this.bufOff);
        this.bufOff = 0;
        Arrays.fill(this.buf, (byte) 0);
    }

    public void write(int i) throws IOException {
        byte[] bArr = this.buf;
        int i2 = this.bufOff;
        this.bufOff = i2 + 1;
        bArr[i2] = (byte) i;
        if (this.bufOff == this.buf.length) {
            flush();
        }
    }

    public void write(byte[] bArr, int i, int i2) throws IOException {
        if (i2 >= this.buf.length - this.bufOff) {
            int length = this.buf.length - this.bufOff;
            System.arraycopy(bArr, i, this.buf, this.bufOff, length);
            this.bufOff += length;
            flush();
            i += length;
            while (true) {
                i2 -= length;
                if (i2 < this.buf.length) {
                    break;
                }
                this.other.write(bArr, i, this.buf.length);
                i += this.buf.length;
                length = this.buf.length;
            }
            if (i2 <= 0) {
                return;
            }
        }
        System.arraycopy(bArr, i, this.buf, this.bufOff, i2);
        this.bufOff += i2;
    }
}
