package org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DSA;
import org.bouncycastle.crypto.DSAExt;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;

public class DSADigestSigner implements Signer {
    private final Digest digest;
    private final DSA dsa;
    private final DSAEncoding encoding;
    private boolean forSigning;

    public DSADigestSigner(DSA dsa2, Digest digest2) {
        this.dsa = dsa2;
        this.digest = digest2;
        this.encoding = StandardDSAEncoding.INSTANCE;
    }

    public DSADigestSigner(DSAExt dSAExt, Digest digest2, DSAEncoding dSAEncoding) {
        this.dsa = dSAExt;
        this.digest = digest2;
        this.encoding = dSAEncoding;
    }

    @Override // org.bouncycastle.crypto.Signer
    public byte[] generateSignature() {
        if (this.forSigning) {
            byte[] bArr = new byte[this.digest.getDigestSize()];
            this.digest.doFinal(bArr, 0);
            BigInteger[] generateSignature = this.dsa.generateSignature(bArr);
            try {
                return this.encoding.encode(getOrder(), generateSignature[0], generateSignature[1]);
            } catch (Exception e) {
                throw new IllegalStateException("unable to encode signature");
            }
        } else {
            throw new IllegalStateException("DSADigestSigner not initialised for signature generation.");
        }
    }

    /* access modifiers changed from: protected */
    public BigInteger getOrder() {
        DSA dsa2 = this.dsa;
        if (dsa2 instanceof DSAExt) {
            return ((DSAExt) dsa2).getOrder();
        }
        return null;
    }

    @Override // org.bouncycastle.crypto.Signer
    public void init(boolean z, CipherParameters cipherParameters) {
        this.forSigning = z;
        AsymmetricKeyParameter asymmetricKeyParameter = cipherParameters instanceof ParametersWithRandom ? (AsymmetricKeyParameter) ((ParametersWithRandom) cipherParameters).getParameters() : (AsymmetricKeyParameter) cipherParameters;
        if (z && !asymmetricKeyParameter.isPrivate()) {
            throw new IllegalArgumentException("Signing Requires Private Key.");
        } else if (z || !asymmetricKeyParameter.isPrivate()) {
            reset();
            this.dsa.init(z, cipherParameters);
        } else {
            throw new IllegalArgumentException("Verification Requires Public Key.");
        }
    }

    @Override // org.bouncycastle.crypto.Signer
    public void reset() {
        this.digest.reset();
    }

    @Override // org.bouncycastle.crypto.Signer
    public void update(byte b) {
        this.digest.update(b);
    }

    @Override // org.bouncycastle.crypto.Signer
    public void update(byte[] bArr, int i, int i2) {
        this.digest.update(bArr, i, i2);
    }

    @Override // org.bouncycastle.crypto.Signer
    public boolean verifySignature(byte[] bArr) {
        if (!this.forSigning) {
            byte[] bArr2 = new byte[this.digest.getDigestSize()];
            this.digest.doFinal(bArr2, 0);
            try {
                BigInteger[] decode = this.encoding.decode(getOrder(), bArr);
                return this.dsa.verifySignature(bArr2, decode[0], decode[1]);
            } catch (Exception e) {
                return false;
            }
        } else {
            throw new IllegalStateException("DSADigestSigner not initialised for verification");
        }
    }
}
