package org.bouncycastle.operator.bc;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.operator.AsymmetricKeyUnwrapper;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.OperatorException;

public abstract class BcAsymmetricKeyUnwrapper extends AsymmetricKeyUnwrapper {
    private AsymmetricKeyParameter privateKey;

    public BcAsymmetricKeyUnwrapper(AlgorithmIdentifier algorithmIdentifier, AsymmetricKeyParameter asymmetricKeyParameter) {
        super(algorithmIdentifier);
        this.privateKey = asymmetricKeyParameter;
    }

    /* access modifiers changed from: protected */
    public abstract AsymmetricBlockCipher createAsymmetricUnwrapper(ASN1ObjectIdentifier aSN1ObjectIdentifier);

    @Override // org.bouncycastle.operator.KeyUnwrapper
    public GenericKey generateUnwrappedKey(AlgorithmIdentifier algorithmIdentifier, byte[] bArr) throws OperatorException {
        AsymmetricBlockCipher createAsymmetricUnwrapper = createAsymmetricUnwrapper(getAlgorithmIdentifier().getAlgorithm());
        createAsymmetricUnwrapper.init(false, this.privateKey);
        try {
            byte[] processBlock = createAsymmetricUnwrapper.processBlock(bArr, 0, bArr.length);
            return algorithmIdentifier.getAlgorithm().equals(PKCSObjectIdentifiers.des_EDE3_CBC) ? new GenericKey(algorithmIdentifier, processBlock) : new GenericKey(algorithmIdentifier, processBlock);
        } catch (InvalidCipherTextException e) {
            throw new OperatorException("unable to recover secret key: " + e.getMessage(), e);
        }
    }
}
