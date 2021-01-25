package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.util.Pack;

public class SipHash128 extends SipHash {
    public SipHash128() {
    }

    public SipHash128(int i, int i2) {
        super(i, i2);
    }

    @Override // org.bouncycastle.crypto.macs.SipHash, org.bouncycastle.crypto.Mac
    public int doFinal(byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        this.m >>>= (7 - this.wordPos) << 3;
        this.m >>>= 8;
        this.m |= (((long) ((this.wordCount << 3) + this.wordPos)) & 255) << 56;
        processMessageWord();
        this.v2 ^= 238;
        applySipRounds(this.d);
        long j = ((this.v0 ^ this.v1) ^ this.v2) ^ this.v3;
        this.v1 ^= 221;
        applySipRounds(this.d);
        reset();
        Pack.longToLittleEndian(j, bArr, i);
        Pack.longToLittleEndian(((this.v0 ^ this.v1) ^ this.v2) ^ this.v3, bArr, i + 8);
        return 16;
    }

    @Override // org.bouncycastle.crypto.macs.SipHash
    public long doFinal() throws DataLengthException, IllegalStateException {
        throw new UnsupportedOperationException("doFinal() is not supported");
    }

    @Override // org.bouncycastle.crypto.macs.SipHash, org.bouncycastle.crypto.Mac
    public String getAlgorithmName() {
        return "SipHash128-" + this.c + "-" + this.d;
    }

    @Override // org.bouncycastle.crypto.macs.SipHash, org.bouncycastle.crypto.Mac
    public int getMacSize() {
        return 16;
    }

    @Override // org.bouncycastle.crypto.macs.SipHash, org.bouncycastle.crypto.Mac
    public void reset() {
        super.reset();
        this.v1 ^= 238;
    }
}
