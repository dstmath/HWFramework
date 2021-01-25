package org.bouncycastle.crypto.engines;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECMultiplier;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public class SM2Engine {
    private int curveLength;
    private final Digest digest;
    private ECKeyParameters ecKey;
    private ECDomainParameters ecParams;
    private boolean forEncryption;
    private final Mode mode;
    private SecureRandom random;

    /* access modifiers changed from: package-private */
    /* renamed from: org.bouncycastle.crypto.engines.SM2Engine$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$org$bouncycastle$crypto$engines$SM2Engine$Mode = new int[Mode.values().length];

        static {
            try {
                $SwitchMap$org$bouncycastle$crypto$engines$SM2Engine$Mode[Mode.C1C3C2.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    public enum Mode {
        C1C2C3,
        C1C3C2
    }

    public SM2Engine() {
        this(new SM3Digest());
    }

    public SM2Engine(Digest digest2) {
        this(digest2, Mode.C1C2C3);
    }

    public SM2Engine(Digest digest2, Mode mode2) {
        if (mode2 != null) {
            this.digest = digest2;
            this.mode = mode2;
            return;
        }
        throw new IllegalArgumentException("mode cannot be NULL");
    }

    public SM2Engine(Mode mode2) {
        this(new SM3Digest(), mode2);
    }

    private void addFieldElement(Digest digest2, ECFieldElement eCFieldElement) {
        byte[] asUnsignedByteArray = BigIntegers.asUnsignedByteArray(this.curveLength, eCFieldElement.toBigInteger());
        digest2.update(asUnsignedByteArray, 0, asUnsignedByteArray.length);
    }

    private byte[] decrypt(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        int i3;
        byte[] bArr2 = new byte[((this.curveLength * 2) + 1)];
        System.arraycopy(bArr, i, bArr2, 0, bArr2.length);
        ECPoint decodePoint = this.ecParams.getCurve().decodePoint(bArr2);
        if (!decodePoint.multiply(this.ecParams.getH()).isInfinity()) {
            ECPoint normalize = decodePoint.multiply(((ECPrivateKeyParameters) this.ecKey).getD()).normalize();
            int digestSize = this.digest.getDigestSize();
            byte[] bArr3 = new byte[((i2 - bArr2.length) - digestSize)];
            if (this.mode == Mode.C1C3C2) {
                System.arraycopy(bArr, bArr2.length + i + digestSize, bArr3, 0, bArr3.length);
            } else {
                System.arraycopy(bArr, bArr2.length + i, bArr3, 0, bArr3.length);
            }
            kdf(this.digest, normalize, bArr3);
            byte[] bArr4 = new byte[this.digest.getDigestSize()];
            addFieldElement(this.digest, normalize.getAffineXCoord());
            this.digest.update(bArr3, 0, bArr3.length);
            addFieldElement(this.digest, normalize.getAffineYCoord());
            this.digest.doFinal(bArr4, 0);
            if (this.mode == Mode.C1C3C2) {
                i3 = 0;
                for (int i4 = 0; i4 != bArr4.length; i4++) {
                    i3 |= bArr4[i4] ^ bArr[(bArr2.length + i) + i4];
                }
            } else {
                i3 = 0;
                for (int i5 = 0; i5 != bArr4.length; i5++) {
                    i3 |= bArr4[i5] ^ bArr[((bArr2.length + i) + bArr3.length) + i5];
                }
            }
            Arrays.fill(bArr2, (byte) 0);
            Arrays.fill(bArr4, (byte) 0);
            if (i3 == 0) {
                return bArr3;
            }
            Arrays.fill(bArr3, (byte) 0);
            throw new InvalidCipherTextException("invalid cipher text");
        }
        throw new InvalidCipherTextException("[h]C1 at infinity");
    }

    private byte[] encrypt(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        byte[] encoded;
        ECPoint normalize;
        byte[] bArr2 = new byte[i2];
        System.arraycopy(bArr, i, bArr2, 0, bArr2.length);
        ECMultiplier createBasePointMultiplier = createBasePointMultiplier();
        do {
            BigInteger nextK = nextK();
            encoded = createBasePointMultiplier.multiply(this.ecParams.getG(), nextK).normalize().getEncoded(false);
            normalize = ((ECPublicKeyParameters) this.ecKey).getQ().multiply(nextK).normalize();
            kdf(this.digest, normalize, bArr2);
        } while (notEncrypted(bArr2, bArr, i));
        byte[] bArr3 = new byte[this.digest.getDigestSize()];
        addFieldElement(this.digest, normalize.getAffineXCoord());
        this.digest.update(bArr, i, i2);
        addFieldElement(this.digest, normalize.getAffineYCoord());
        this.digest.doFinal(bArr3, 0);
        return AnonymousClass1.$SwitchMap$org$bouncycastle$crypto$engines$SM2Engine$Mode[this.mode.ordinal()] != 1 ? Arrays.concatenate(encoded, bArr2, bArr3) : Arrays.concatenate(encoded, bArr3, bArr2);
    }

    private void kdf(Digest digest2, ECPoint eCPoint, byte[] bArr) {
        Memoable memoable;
        int digestSize = digest2.getDigestSize();
        byte[] bArr2 = new byte[Math.max(4, digestSize)];
        Memoable memoable2 = null;
        if (digest2 instanceof Memoable) {
            addFieldElement(digest2, eCPoint.getAffineXCoord());
            addFieldElement(digest2, eCPoint.getAffineYCoord());
            memoable2 = (Memoable) digest2;
            memoable = memoable2.copy();
        } else {
            memoable = null;
        }
        int i = 0;
        int i2 = 0;
        while (i < bArr.length) {
            if (memoable2 != null) {
                memoable2.reset(memoable);
            } else {
                addFieldElement(digest2, eCPoint.getAffineXCoord());
                addFieldElement(digest2, eCPoint.getAffineYCoord());
            }
            i2++;
            Pack.intToBigEndian(i2, bArr2, 0);
            digest2.update(bArr2, 0, 4);
            digest2.doFinal(bArr2, 0);
            int min = Math.min(digestSize, bArr.length - i);
            xor(bArr, bArr2, i, min);
            i += min;
        }
    }

    private BigInteger nextK() {
        int bitLength = this.ecParams.getN().bitLength();
        while (true) {
            BigInteger createRandomBigInteger = BigIntegers.createRandomBigInteger(bitLength, this.random);
            if (!createRandomBigInteger.equals(BigIntegers.ZERO) && createRandomBigInteger.compareTo(this.ecParams.getN()) < 0) {
                return createRandomBigInteger;
            }
        }
    }

    private boolean notEncrypted(byte[] bArr, byte[] bArr2, int i) {
        for (int i2 = 0; i2 != bArr.length; i2++) {
            if (bArr[i2] != bArr2[i + i2]) {
                return false;
            }
        }
        return true;
    }

    private void xor(byte[] bArr, byte[] bArr2, int i, int i2) {
        for (int i3 = 0; i3 != i2; i3++) {
            int i4 = i + i3;
            bArr[i4] = (byte) (bArr[i4] ^ bArr2[i3]);
        }
    }

    /* access modifiers changed from: protected */
    public ECMultiplier createBasePointMultiplier() {
        return new FixedPointCombMultiplier();
    }

    public int getOutputSize(int i) {
        return (this.curveLength * 2) + 1 + i + this.digest.getDigestSize();
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        this.forEncryption = z;
        if (z) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.ecKey = (ECKeyParameters) parametersWithRandom.getParameters();
            this.ecParams = this.ecKey.getParameters();
            if (!((ECPublicKeyParameters) this.ecKey).getQ().multiply(this.ecParams.getH()).isInfinity()) {
                this.random = parametersWithRandom.getRandom();
            } else {
                throw new IllegalArgumentException("invalid key: [h]Q at infinity");
            }
        } else {
            this.ecKey = (ECKeyParameters) cipherParameters;
            this.ecParams = this.ecKey.getParameters();
        }
        this.curveLength = (this.ecParams.getCurve().getFieldSize() + 7) / 8;
    }

    public byte[] processBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        return this.forEncryption ? encrypt(bArr, i, i2) : decrypt(bArr, i, i2);
    }
}
