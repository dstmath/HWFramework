package org.bouncycastle.operator.bc;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.operator.OperatorCreationException;

public class BcECContentSignerBuilder extends BcContentSignerBuilder {
    public BcECContentSignerBuilder(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) {
        super(algorithmIdentifier, algorithmIdentifier2);
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.operator.bc.BcContentSignerBuilder
    public Signer createSigner(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) throws OperatorCreationException {
        return new DSADigestSigner(new ECDSASigner(), this.digestProvider.get(algorithmIdentifier2));
    }
}
