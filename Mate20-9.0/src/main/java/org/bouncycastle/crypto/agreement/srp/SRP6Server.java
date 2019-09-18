package org.bouncycastle.crypto.agreement.srp;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.params.SRP6GroupParameters;

public class SRP6Server {
    protected BigInteger A;
    protected BigInteger B;
    protected BigInteger Key;
    protected BigInteger M1;
    protected BigInteger M2;
    protected BigInteger N;
    protected BigInteger S;
    protected BigInteger b;
    protected Digest digest;
    protected BigInteger g;
    protected SecureRandom random;
    protected BigInteger u;
    protected BigInteger v;

    private BigInteger calculateS() {
        return this.v.modPow(this.u, this.N).multiply(this.A).mod(this.N).modPow(this.b, this.N);
    }

    public BigInteger calculateSecret(BigInteger bigInteger) throws CryptoException {
        this.A = SRP6Util.validatePublicValue(this.N, bigInteger);
        this.u = SRP6Util.calculateU(this.digest, this.N, this.A, this.B);
        this.S = calculateS();
        return this.S;
    }

    public BigInteger calculateServerEvidenceMessage() throws CryptoException {
        if (this.A == null || this.M1 == null || this.S == null) {
            throw new CryptoException("Impossible to compute M2: some data are missing from the previous operations (A,M1,S)");
        }
        this.M2 = SRP6Util.calculateM2(this.digest, this.N, this.A, this.M1, this.S);
        return this.M2;
    }

    public BigInteger calculateSessionKey() throws CryptoException {
        if (this.S == null || this.M1 == null || this.M2 == null) {
            throw new CryptoException("Impossible to compute Key: some data are missing from the previous operations (S,M1,M2)");
        }
        this.Key = SRP6Util.calculateKey(this.digest, this.N, this.S);
        return this.Key;
    }

    public BigInteger generateServerCredentials() {
        BigInteger calculateK = SRP6Util.calculateK(this.digest, this.N, this.g);
        this.b = selectPrivateValue();
        this.B = calculateK.multiply(this.v).mod(this.N).add(this.g.modPow(this.b, this.N)).mod(this.N);
        return this.B;
    }

    public void init(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, Digest digest2, SecureRandom secureRandom) {
        this.N = bigInteger;
        this.g = bigInteger2;
        this.v = bigInteger3;
        this.random = secureRandom;
        this.digest = digest2;
    }

    public void init(SRP6GroupParameters sRP6GroupParameters, BigInteger bigInteger, Digest digest2, SecureRandom secureRandom) {
        init(sRP6GroupParameters.getN(), sRP6GroupParameters.getG(), bigInteger, digest2, secureRandom);
    }

    /* access modifiers changed from: protected */
    public BigInteger selectPrivateValue() {
        return SRP6Util.generatePrivateValue(this.digest, this.N, this.g, this.random);
    }

    public boolean verifyClientEvidenceMessage(BigInteger bigInteger) throws CryptoException {
        if (this.A == null || this.B == null || this.S == null) {
            throw new CryptoException("Impossible to compute and verify M1: some data are missing from the previous operations (A,B,S)");
        } else if (!SRP6Util.calculateM1(this.digest, this.N, this.A, this.B, this.S).equals(bigInteger)) {
            return false;
        } else {
            this.M1 = bigInteger;
            return true;
        }
    }
}
