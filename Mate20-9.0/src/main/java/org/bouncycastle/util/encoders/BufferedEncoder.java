package org.bouncycastle.util.encoders;

public class BufferedEncoder {
    protected byte[] buf;
    protected int bufOff;
    protected Translator translator;

    public BufferedEncoder(Translator translator2, int i) {
        this.translator = translator2;
        if (i % translator2.getEncodedBlockSize() == 0) {
            this.buf = new byte[i];
            this.bufOff = 0;
            return;
        }
        throw new IllegalArgumentException("buffer size not multiple of input block size");
    }

    public int processByte(byte b, byte[] bArr, int i) {
        byte[] bArr2 = this.buf;
        int i2 = this.bufOff;
        this.bufOff = i2 + 1;
        bArr2[i2] = b;
        if (this.bufOff != this.buf.length) {
            return 0;
        }
        int encode = this.translator.encode(this.buf, 0, this.buf.length, bArr, i);
        this.bufOff = 0;
        return encode;
    }

    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        if (i2 >= 0) {
            int length = this.buf.length - this.bufOff;
            int i4 = 0;
            if (i2 > length) {
                System.arraycopy(bArr, i, this.buf, this.bufOff, length);
                int encode = this.translator.encode(this.buf, 0, this.buf.length, bArr2, i3) + 0;
                this.bufOff = 0;
                int i5 = i2 - length;
                int i6 = i + length;
                int i7 = i3 + encode;
                int length2 = i5 - (i5 % this.buf.length);
                i4 = encode + this.translator.encode(bArr, i6, length2, bArr2, i7);
                i2 = i5 - length2;
                i = i6 + length2;
            }
            if (i2 != 0) {
                System.arraycopy(bArr, i, this.buf, this.bufOff, i2);
                this.bufOff += i2;
            }
            return i4;
        }
        throw new IllegalArgumentException("Can't have a negative input length!");
    }
}
