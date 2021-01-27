package org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;

public class HMacDSAKCalculator implements DSAKCalculator {
    private static final BigInteger ZERO = BigInteger.valueOf(0);
    private final byte[] K = new byte[this.hMac.getMacSize()];
    private final byte[] V = new byte[this.hMac.getMacSize()];
    private final HMac hMac;
    private BigInteger n;

    public HMacDSAKCalculator(Digest digest) {
        this.hMac = new HMac(digest);
    }

    private BigInteger bitsToInt(byte[] bArr) {
        BigInteger bigInteger = new BigInteger(1, bArr);
        return bArr.length * 8 > this.n.bitLength() ? bigInteger.shiftRight((bArr.length * 8) - this.n.bitLength()) : bigInteger;
    }

    @Override // org.bouncycastle.crypto.signers.DSAKCalculator
    public void init(BigInteger bigInteger, BigInteger bigInteger2, byte[] bArr) {
        this.n = bigInteger;
        Arrays.fill(this.V, (byte) 1);
        Arrays.fill(this.K, (byte) 0);
        int unsignedByteLength = BigIntegers.getUnsignedByteLength(bigInteger);
        byte[] bArr2 = new byte[unsignedByteLength];
        byte[] asUnsignedByteArray = BigIntegers.asUnsignedByteArray(bigInteger2);
        System.arraycopy(asUnsignedByteArray, 0, bArr2, bArr2.length - asUnsignedByteArray.length, asUnsignedByteArray.length);
        byte[] bArr3 = new byte[unsignedByteLength];
        BigInteger bitsToInt = bitsToInt(bArr);
        if (bitsToInt.compareTo(bigInteger) >= 0) {
            bitsToInt = bitsToInt.subtract(bigInteger);
        }
        byte[] asUnsignedByteArray2 = BigIntegers.asUnsignedByteArray(bitsToInt);
        System.arraycopy(asUnsignedByteArray2, 0, bArr3, bArr3.length - asUnsignedByteArray2.length, asUnsignedByteArray2.length);
        this.hMac.init(new KeyParameter(this.K));
        HMac hMac2 = this.hMac;
        byte[] bArr4 = this.V;
        hMac2.update(bArr4, 0, bArr4.length);
        this.hMac.update((byte) 0);
        this.hMac.update(bArr2, 0, bArr2.length);
        this.hMac.update(bArr3, 0, bArr3.length);
        this.hMac.doFinal(this.K, 0);
        this.hMac.init(new KeyParameter(this.K));
        HMac hMac3 = this.hMac;
        byte[] bArr5 = this.V;
        hMac3.update(bArr5, 0, bArr5.length);
        this.hMac.doFinal(this.V, 0);
        HMac hMac4 = this.hMac;
        byte[] bArr6 = this.V;
        hMac4.update(bArr6, 0, bArr6.length);
        this.hMac.update((byte) 1);
        this.hMac.update(bArr2, 0, bArr2.length);
        this.hMac.update(bArr3, 0, bArr3.length);
        this.hMac.doFinal(this.K, 0);
        this.hMac.init(new KeyParameter(this.K));
        HMac hMac5 = this.hMac;
        byte[] bArr7 = this.V;
        hMac5.update(bArr7, 0, bArr7.length);
        this.hMac.doFinal(this.V, 0);
    }

    @Override // org.bouncycastle.crypto.signers.DSAKCalculator
    public void init(BigInteger bigInteger, SecureRandom secureRandom) {
        throw new IllegalStateException("Operation not supported");
    }

    @Override // org.bouncycastle.crypto.signers.DSAKCalculator
    public boolean isDeterministic() {
        return true;
    }

    @Override // org.bouncycastle.crypto.signers.DSAKCalculator
    public BigInteger nextK() {
        byte[] bArr = new byte[BigIntegers.getUnsignedByteLength(this.n)];
        while (true) {
            int i = 0;
            while (i < bArr.length) {
                HMac hMac2 = this.hMac;
                byte[] bArr2 = this.V;
                hMac2.update(bArr2, 0, bArr2.length);
                this.hMac.doFinal(this.V, 0);
                int min = Math.min(bArr.length - i, this.V.length);
                System.arraycopy(this.V, 0, bArr, i, min);
                i += min;
            }
            BigInteger bitsToInt = bitsToInt(bArr);
            if (bitsToInt.compareTo(ZERO) > 0 && bitsToInt.compareTo(this.n) < 0) {
                return bitsToInt;
            }
            HMac hMac3 = this.hMac;
            byte[] bArr3 = this.V;
            hMac3.update(bArr3, 0, bArr3.length);
            this.hMac.update((byte) 0);
            this.hMac.doFinal(this.K, 0);
            this.hMac.init(new KeyParameter(this.K));
            HMac hMac4 = this.hMac;
            byte[] bArr4 = this.V;
            hMac4.update(bArr4, 0, bArr4.length);
            this.hMac.doFinal(this.V, 0);
        }
    }
}
