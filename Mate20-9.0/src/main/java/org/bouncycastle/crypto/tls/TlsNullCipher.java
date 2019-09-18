package org.bouncycastle.crypto.tls;

import java.io.IOException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.util.Arrays;

public class TlsNullCipher implements TlsCipher {
    protected TlsContext context;
    protected TlsMac readMac;
    protected TlsMac writeMac;

    public TlsNullCipher(TlsContext tlsContext) {
        this.context = tlsContext;
        this.writeMac = null;
        this.readMac = null;
    }

    public TlsNullCipher(TlsContext tlsContext, Digest digest, Digest digest2) throws IOException {
        TlsMac tlsMac;
        boolean z = true;
        if ((digest == null) == (digest2 != null ? false : z)) {
            this.context = tlsContext;
            TlsMac tlsMac2 = null;
            if (digest != null) {
                int digestSize = digest.getDigestSize() + digest2.getDigestSize();
                TlsContext tlsContext2 = tlsContext;
                byte[] calculateKeyBlock = TlsUtils.calculateKeyBlock(tlsContext, digestSize);
                tlsMac = new TlsMac(tlsContext2, digest, calculateKeyBlock, 0, digest.getDigestSize());
                int digestSize2 = digest.getDigestSize() + 0;
                TlsMac tlsMac3 = new TlsMac(tlsContext2, digest2, calculateKeyBlock, digestSize2, digest2.getDigestSize());
                if (digestSize2 + digest2.getDigestSize() == digestSize) {
                    tlsMac2 = tlsMac3;
                } else {
                    throw new TlsFatalAlert(80);
                }
            } else {
                tlsMac = null;
            }
            if (tlsContext.isServer()) {
                this.writeMac = tlsMac2;
                this.readMac = tlsMac;
                return;
            }
            this.writeMac = tlsMac;
            this.readMac = tlsMac2;
            return;
        }
        throw new TlsFatalAlert(80);
    }

    public byte[] decodeCiphertext(long j, short s, byte[] bArr, int i, int i2) throws IOException {
        if (this.readMac == null) {
            return Arrays.copyOfRange(bArr, i, i2 + i);
        }
        int size = this.readMac.getSize();
        if (i2 >= size) {
            int i3 = i2 - size;
            int i4 = i + i3;
            if (Arrays.constantTimeAreEqual(Arrays.copyOfRange(bArr, i4, i2 + i), this.readMac.calculateMac(j, s, bArr, i, i3))) {
                return Arrays.copyOfRange(bArr, i, i4);
            }
            throw new TlsFatalAlert(20);
        }
        throw new TlsFatalAlert(50);
    }

    public byte[] encodePlaintext(long j, short s, byte[] bArr, int i, int i2) throws IOException {
        if (this.writeMac == null) {
            return Arrays.copyOfRange(bArr, i, i2 + i);
        }
        byte[] calculateMac = this.writeMac.calculateMac(j, s, bArr, i, i2);
        byte[] bArr2 = new byte[(calculateMac.length + i2)];
        System.arraycopy(bArr, i, bArr2, 0, i2);
        System.arraycopy(calculateMac, 0, bArr2, i2, calculateMac.length);
        return bArr2;
    }

    public int getPlaintextLimit(int i) {
        return this.writeMac != null ? i - this.writeMac.getSize() : i;
    }
}
