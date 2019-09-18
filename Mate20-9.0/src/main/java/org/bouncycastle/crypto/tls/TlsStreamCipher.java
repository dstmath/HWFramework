package org.bouncycastle.crypto.tls;

import java.io.IOException;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class TlsStreamCipher implements TlsCipher {
    protected TlsContext context;
    protected StreamCipher decryptCipher;
    protected StreamCipher encryptCipher;
    protected TlsMac readMac;
    protected boolean usesNonce;
    protected TlsMac writeMac;

    public TlsStreamCipher(TlsContext tlsContext, StreamCipher streamCipher, StreamCipher streamCipher2, Digest digest, Digest digest2, int i, boolean z) throws IOException {
        TlsContext tlsContext2 = tlsContext;
        StreamCipher streamCipher3 = streamCipher;
        StreamCipher streamCipher4 = streamCipher2;
        int i2 = i;
        boolean z2 = z;
        boolean isServer = tlsContext.isServer();
        this.context = tlsContext2;
        this.usesNonce = z2;
        this.encryptCipher = streamCipher3;
        this.decryptCipher = streamCipher4;
        int digestSize = (2 * i2) + digest.getDigestSize() + digest2.getDigestSize();
        byte[] calculateKeyBlock = TlsUtils.calculateKeyBlock(tlsContext2, digestSize);
        TlsContext tlsContext3 = tlsContext2;
        byte[] bArr = calculateKeyBlock;
        TlsMac tlsMac = new TlsMac(tlsContext3, digest, bArr, 0, digest.getDigestSize());
        int digestSize2 = 0 + digest.getDigestSize();
        TlsMac tlsMac2 = r1;
        TlsMac tlsMac3 = new TlsMac(tlsContext3, digest2, bArr, digestSize2, digest2.getDigestSize());
        int digestSize3 = digestSize2 + digest2.getDigestSize();
        CipherParameters keyParameter = new KeyParameter(calculateKeyBlock, digestSize3, i2);
        int i3 = digestSize3 + i2;
        CipherParameters keyParameter2 = new KeyParameter(calculateKeyBlock, i3, i2);
        if (i3 + i2 == digestSize) {
            if (isServer) {
                this.writeMac = tlsMac2;
                this.readMac = tlsMac;
                this.encryptCipher = streamCipher4;
                this.decryptCipher = streamCipher3;
                CipherParameters cipherParameters = keyParameter2;
                keyParameter2 = keyParameter;
                keyParameter = cipherParameters;
            } else {
                this.writeMac = tlsMac;
                this.readMac = tlsMac2;
                this.encryptCipher = streamCipher3;
                this.decryptCipher = streamCipher4;
            }
            if (z2) {
                byte[] bArr2 = new byte[8];
                CipherParameters parametersWithIV = new ParametersWithIV(keyParameter, bArr2);
                keyParameter2 = new ParametersWithIV(keyParameter2, bArr2);
                keyParameter = parametersWithIV;
            }
            this.encryptCipher.init(true, keyParameter);
            this.decryptCipher.init(false, keyParameter2);
            return;
        }
        throw new TlsFatalAlert(80);
    }

    /* access modifiers changed from: protected */
    public void checkMAC(long j, short s, byte[] bArr, int i, int i2, byte[] bArr2, int i3, int i4) throws IOException {
        if (!Arrays.constantTimeAreEqual(Arrays.copyOfRange(bArr, i, i2), this.readMac.calculateMac(j, s, bArr2, i3, i4))) {
            throw new TlsFatalAlert(20);
        }
    }

    public byte[] decodeCiphertext(long j, short s, byte[] bArr, int i, int i2) throws IOException {
        long j2;
        int i3 = i2;
        if (this.usesNonce) {
            j2 = j;
            updateIV(this.decryptCipher, false, j2);
        } else {
            j2 = j;
        }
        int size = this.readMac.getSize();
        if (i3 >= size) {
            int i4 = i3 - size;
            byte[] bArr2 = new byte[i3];
            this.decryptCipher.processBytes(bArr, i, i3, bArr2, 0);
            checkMAC(j2, s, bArr2, i4, i3, bArr2, 0, i4);
            return Arrays.copyOfRange(bArr2, 0, i4);
        }
        throw new TlsFatalAlert(50);
    }

    public byte[] encodePlaintext(long j, short s, byte[] bArr, int i, int i2) {
        long j2;
        if (this.usesNonce) {
            j2 = j;
            updateIV(this.encryptCipher, true, j2);
        } else {
            j2 = j;
        }
        byte[] bArr2 = new byte[(i2 + this.writeMac.getSize())];
        byte[] bArr3 = bArr;
        int i3 = i;
        int i4 = i2;
        this.encryptCipher.processBytes(bArr3, i3, i4, bArr2, 0);
        byte[] calculateMac = this.writeMac.calculateMac(j2, s, bArr3, i3, i4);
        this.encryptCipher.processBytes(calculateMac, 0, calculateMac.length, bArr2, i2);
        return bArr2;
    }

    public int getPlaintextLimit(int i) {
        return i - this.writeMac.getSize();
    }

    /* access modifiers changed from: protected */
    public void updateIV(StreamCipher streamCipher, boolean z, long j) {
        byte[] bArr = new byte[8];
        TlsUtils.writeUint64(j, bArr, 0);
        streamCipher.init(z, new ParametersWithIV(null, bArr));
    }
}
