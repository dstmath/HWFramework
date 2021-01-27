package org.bouncycastle.crypto.agreement;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.SM2KeyExchangePrivateParameters;
import org.bouncycastle.crypto.params.SM2KeyExchangePublicParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public class SM2KeyExchange {
    private final Digest digest;
    private ECDomainParameters ecParams;
    private ECPrivateKeyParameters ephemeralKey;
    private ECPoint ephemeralPubPoint;
    private boolean initiator;
    private ECPrivateKeyParameters staticKey;
    private ECPoint staticPubPoint;
    private byte[] userID;
    private int w;

    public SM2KeyExchange() {
        this(new SM3Digest());
    }

    public SM2KeyExchange(Digest digest2) {
        this.digest = digest2;
    }

    private byte[] S1(Digest digest2, ECPoint eCPoint, byte[] bArr) {
        digest2.update((byte) 2);
        addFieldElement(digest2, eCPoint.getAffineYCoord());
        digest2.update(bArr, 0, bArr.length);
        return digestDoFinal();
    }

    private byte[] S2(Digest digest2, ECPoint eCPoint, byte[] bArr) {
        digest2.update((byte) 3);
        addFieldElement(digest2, eCPoint.getAffineYCoord());
        digest2.update(bArr, 0, bArr.length);
        return digestDoFinal();
    }

    private void addFieldElement(Digest digest2, ECFieldElement eCFieldElement) {
        byte[] encoded = eCFieldElement.getEncoded();
        digest2.update(encoded, 0, encoded.length);
    }

    private void addUserID(Digest digest2, byte[] bArr) {
        int length = bArr.length * 8;
        digest2.update((byte) (length >>> 8));
        digest2.update((byte) length);
        digest2.update(bArr, 0, bArr.length);
    }

    private byte[] calculateInnerHash(Digest digest2, ECPoint eCPoint, byte[] bArr, byte[] bArr2, ECPoint eCPoint2, ECPoint eCPoint3) {
        addFieldElement(digest2, eCPoint.getAffineXCoord());
        digest2.update(bArr, 0, bArr.length);
        digest2.update(bArr2, 0, bArr2.length);
        addFieldElement(digest2, eCPoint2.getAffineXCoord());
        addFieldElement(digest2, eCPoint2.getAffineYCoord());
        addFieldElement(digest2, eCPoint3.getAffineXCoord());
        addFieldElement(digest2, eCPoint3.getAffineYCoord());
        return digestDoFinal();
    }

    private ECPoint calculateU(SM2KeyExchangePublicParameters sM2KeyExchangePublicParameters) {
        ECDomainParameters parameters = this.staticKey.getParameters();
        ECPoint cleanPoint = ECAlgorithms.cleanPoint(parameters.getCurve(), sM2KeyExchangePublicParameters.getStaticPublicKey().getQ());
        ECPoint cleanPoint2 = ECAlgorithms.cleanPoint(parameters.getCurve(), sM2KeyExchangePublicParameters.getEphemeralPublicKey().getQ());
        BigInteger reduce = reduce(this.ephemeralPubPoint.getAffineXCoord().toBigInteger());
        BigInteger reduce2 = reduce(cleanPoint2.getAffineXCoord().toBigInteger());
        BigInteger mod = this.ecParams.getH().multiply(this.staticKey.getD().add(reduce.multiply(this.ephemeralKey.getD()))).mod(this.ecParams.getN());
        return ECAlgorithms.sumOfTwoMultiplies(cleanPoint, mod, cleanPoint2, mod.multiply(reduce2).mod(this.ecParams.getN())).normalize();
    }

    private byte[] digestDoFinal() {
        byte[] bArr = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr, 0);
        return bArr;
    }

    private byte[] getZ(Digest digest2, byte[] bArr, ECPoint eCPoint) {
        addUserID(digest2, bArr);
        addFieldElement(digest2, this.ecParams.getCurve().getA());
        addFieldElement(digest2, this.ecParams.getCurve().getB());
        addFieldElement(digest2, this.ecParams.getG().getAffineXCoord());
        addFieldElement(digest2, this.ecParams.getG().getAffineYCoord());
        addFieldElement(digest2, eCPoint.getAffineXCoord());
        addFieldElement(digest2, eCPoint.getAffineYCoord());
        return digestDoFinal();
    }

    private byte[] kdf(ECPoint eCPoint, byte[] bArr, byte[] bArr2, int i) {
        Memoable memoable;
        int digestSize = this.digest.getDigestSize();
        byte[] bArr3 = new byte[Math.max(4, digestSize)];
        byte[] bArr4 = new byte[((i + 7) / 8)];
        Digest digest2 = this.digest;
        Memoable memoable2 = null;
        if (digest2 instanceof Memoable) {
            addFieldElement(digest2, eCPoint.getAffineXCoord());
            addFieldElement(this.digest, eCPoint.getAffineYCoord());
            this.digest.update(bArr, 0, bArr.length);
            this.digest.update(bArr2, 0, bArr2.length);
            memoable2 = (Memoable) this.digest;
            memoable = memoable2.copy();
        } else {
            memoable = null;
        }
        int i2 = 0;
        int i3 = 0;
        while (i2 < bArr4.length) {
            if (memoable2 != null) {
                memoable2.reset(memoable);
            } else {
                addFieldElement(this.digest, eCPoint.getAffineXCoord());
                addFieldElement(this.digest, eCPoint.getAffineYCoord());
                this.digest.update(bArr, 0, bArr.length);
                this.digest.update(bArr2, 0, bArr2.length);
            }
            i3++;
            Pack.intToBigEndian(i3, bArr3, 0);
            this.digest.update(bArr3, 0, 4);
            this.digest.doFinal(bArr3, 0);
            int min = Math.min(digestSize, bArr4.length - i2);
            System.arraycopy(bArr3, 0, bArr4, i2, min);
            i2 += min;
        }
        return bArr4;
    }

    private BigInteger reduce(BigInteger bigInteger) {
        return bigInteger.and(BigInteger.valueOf(1).shiftLeft(this.w).subtract(BigInteger.valueOf(1))).setBit(this.w);
    }

    public byte[] calculateKey(int i, CipherParameters cipherParameters) {
        byte[] bArr;
        SM2KeyExchangePublicParameters sM2KeyExchangePublicParameters;
        if (cipherParameters instanceof ParametersWithID) {
            ParametersWithID parametersWithID = (ParametersWithID) cipherParameters;
            sM2KeyExchangePublicParameters = (SM2KeyExchangePublicParameters) parametersWithID.getParameters();
            bArr = parametersWithID.getID();
        } else {
            sM2KeyExchangePublicParameters = (SM2KeyExchangePublicParameters) cipherParameters;
            bArr = new byte[0];
        }
        byte[] z = getZ(this.digest, this.userID, this.staticPubPoint);
        byte[] z2 = getZ(this.digest, bArr, sM2KeyExchangePublicParameters.getStaticPublicKey().getQ());
        ECPoint calculateU = calculateU(sM2KeyExchangePublicParameters);
        return this.initiator ? kdf(calculateU, z, z2, i) : kdf(calculateU, z2, z, i);
    }

    public byte[][] calculateKeyWithConfirmation(int i, byte[] bArr, CipherParameters cipherParameters) {
        SM2KeyExchangePublicParameters sM2KeyExchangePublicParameters;
        byte[] bArr2;
        if (cipherParameters instanceof ParametersWithID) {
            ParametersWithID parametersWithID = (ParametersWithID) cipherParameters;
            sM2KeyExchangePublicParameters = (SM2KeyExchangePublicParameters) parametersWithID.getParameters();
            bArr2 = parametersWithID.getID();
        } else {
            sM2KeyExchangePublicParameters = (SM2KeyExchangePublicParameters) cipherParameters;
            bArr2 = new byte[0];
        }
        if (!this.initiator || bArr != null) {
            byte[] z = getZ(this.digest, this.userID, this.staticPubPoint);
            byte[] z2 = getZ(this.digest, bArr2, sM2KeyExchangePublicParameters.getStaticPublicKey().getQ());
            ECPoint calculateU = calculateU(sM2KeyExchangePublicParameters);
            if (this.initiator) {
                byte[] kdf = kdf(calculateU, z, z2, i);
                byte[] calculateInnerHash = calculateInnerHash(this.digest, calculateU, z, z2, this.ephemeralPubPoint, sM2KeyExchangePublicParameters.getEphemeralPublicKey().getQ());
                if (Arrays.constantTimeAreEqual(S1(this.digest, calculateU, calculateInnerHash), bArr)) {
                    return new byte[][]{kdf, S2(this.digest, calculateU, calculateInnerHash)};
                }
                throw new IllegalStateException("confirmation tag mismatch");
            }
            byte[] kdf2 = kdf(calculateU, z2, z, i);
            byte[] calculateInnerHash2 = calculateInnerHash(this.digest, calculateU, z2, z, sM2KeyExchangePublicParameters.getEphemeralPublicKey().getQ(), this.ephemeralPubPoint);
            return new byte[][]{kdf2, S1(this.digest, calculateU, calculateInnerHash2), S2(this.digest, calculateU, calculateInnerHash2)};
        }
        throw new IllegalArgumentException("if initiating, confirmationTag must be set");
    }

    public void init(CipherParameters cipherParameters) {
        byte[] bArr;
        SM2KeyExchangePrivateParameters sM2KeyExchangePrivateParameters;
        if (cipherParameters instanceof ParametersWithID) {
            ParametersWithID parametersWithID = (ParametersWithID) cipherParameters;
            sM2KeyExchangePrivateParameters = (SM2KeyExchangePrivateParameters) parametersWithID.getParameters();
            bArr = parametersWithID.getID();
        } else {
            sM2KeyExchangePrivateParameters = (SM2KeyExchangePrivateParameters) cipherParameters;
            bArr = new byte[0];
        }
        this.userID = bArr;
        this.initiator = sM2KeyExchangePrivateParameters.isInitiator();
        this.staticKey = sM2KeyExchangePrivateParameters.getStaticPrivateKey();
        this.ephemeralKey = sM2KeyExchangePrivateParameters.getEphemeralPrivateKey();
        this.ecParams = this.staticKey.getParameters();
        this.staticPubPoint = sM2KeyExchangePrivateParameters.getStaticPublicPoint();
        this.ephemeralPubPoint = sM2KeyExchangePrivateParameters.getEphemeralPublicPoint();
        this.w = (this.ecParams.getCurve().getFieldSize() / 2) - 1;
    }
}
