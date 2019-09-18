package org.bouncycastle.crypto.tls;

import java.math.BigInteger;
import org.bouncycastle.crypto.params.SRP6GroupParameters;

public class TlsSRPLoginParameters {
    protected SRP6GroupParameters group;
    protected byte[] salt;
    protected BigInteger verifier;

    public TlsSRPLoginParameters(SRP6GroupParameters sRP6GroupParameters, BigInteger bigInteger, byte[] bArr) {
        this.group = sRP6GroupParameters;
        this.verifier = bigInteger;
        this.salt = bArr;
    }

    public SRP6GroupParameters getGroup() {
        return this.group;
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public BigInteger getVerifier() {
        return this.verifier;
    }
}
