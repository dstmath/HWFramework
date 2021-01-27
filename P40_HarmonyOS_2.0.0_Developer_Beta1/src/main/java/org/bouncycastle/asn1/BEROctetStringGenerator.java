package org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

public class BEROctetStringGenerator extends BERGenerator {

    /* access modifiers changed from: private */
    public class BufferedBEROctetStream extends OutputStream {
        private byte[] _buf;
        private DEROutputStream _derOut;
        private int _off = 0;

        BufferedBEROctetStream(byte[] bArr) {
            this._buf = bArr;
            this._derOut = new DEROutputStream(BEROctetStringGenerator.this._out);
        }

        @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            int i = this._off;
            if (i != 0) {
                DEROctetString.encode(this._derOut, true, this._buf, 0, i);
            }
            this._derOut.flushInternal();
            BEROctetStringGenerator.this.writeBEREnd();
        }

        @Override // java.io.OutputStream
        public void write(int i) throws IOException {
            byte[] bArr = this._buf;
            int i2 = this._off;
            this._off = i2 + 1;
            bArr[i2] = (byte) i;
            if (this._off == bArr.length) {
                DEROctetString.encode(this._derOut, true, bArr, 0, bArr.length);
                this._off = 0;
            }
        }

        @Override // java.io.OutputStream
        public void write(byte[] bArr, int i, int i2) throws IOException {
            while (i2 > 0) {
                int min = Math.min(i2, this._buf.length - this._off);
                System.arraycopy(bArr, i, this._buf, this._off, min);
                this._off += min;
                int i3 = this._off;
                byte[] bArr2 = this._buf;
                if (i3 >= bArr2.length) {
                    DEROctetString.encode(this._derOut, true, bArr2, 0, bArr2.length);
                    this._off = 0;
                    i += min;
                    i2 -= min;
                } else {
                    return;
                }
            }
        }
    }

    public BEROctetStringGenerator(OutputStream outputStream) throws IOException {
        super(outputStream);
        writeBERHeader(36);
    }

    public BEROctetStringGenerator(OutputStream outputStream, int i, boolean z) throws IOException {
        super(outputStream, i, z);
        writeBERHeader(36);
    }

    public OutputStream getOctetOutputStream() {
        return getOctetOutputStream(new byte[1000]);
    }

    public OutputStream getOctetOutputStream(byte[] bArr) {
        return new BufferedBEROctetStream(bArr);
    }
}
