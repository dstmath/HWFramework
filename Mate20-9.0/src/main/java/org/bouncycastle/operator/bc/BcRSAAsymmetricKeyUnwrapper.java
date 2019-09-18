package org.bouncycastle.operator.bc;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class BcRSAAsymmetricKeyUnwrapper extends BcAsymmetricKeyUnwrapper {
    public BcRSAAsymmetricKeyUnwrapper(AlgorithmIdentifier algorithmIdentifier, AsymmetricKeyParameter asymmetricKeyParameter) {
        super(algorithmIdentifier, asymmetricKeyParameter);
    }

    /* access modifiers changed from: protected */
    public AsymmetricBlockCipher createAsymmetricUnwrapper(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return new PKCS1Encoding(new RSAEngine());
    }
}
