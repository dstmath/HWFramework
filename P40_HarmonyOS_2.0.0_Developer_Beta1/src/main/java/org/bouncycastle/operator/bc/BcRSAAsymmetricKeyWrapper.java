package org.bouncycastle.operator.bc;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PublicKeyFactory;

public class BcRSAAsymmetricKeyWrapper extends BcAsymmetricKeyWrapper {
    public BcRSAAsymmetricKeyWrapper(AlgorithmIdentifier algorithmIdentifier, SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
        super(algorithmIdentifier, PublicKeyFactory.createKey(subjectPublicKeyInfo));
    }

    public BcRSAAsymmetricKeyWrapper(AlgorithmIdentifier algorithmIdentifier, AsymmetricKeyParameter asymmetricKeyParameter) {
        super(algorithmIdentifier, asymmetricKeyParameter);
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.operator.bc.BcAsymmetricKeyWrapper
    public AsymmetricBlockCipher createAsymmetricWrapper(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return new PKCS1Encoding(new RSABlindedEngine());
    }
}
