package org.bouncycastle.cms.bc;

import java.io.InputStream;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientOperator;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.operator.InputDecryptor;

public class BcRSAKeyTransEnvelopedRecipient extends BcKeyTransRecipient {
    public BcRSAKeyTransEnvelopedRecipient(AsymmetricKeyParameter asymmetricKeyParameter) {
        super(asymmetricKeyParameter);
    }

    @Override // org.bouncycastle.cms.KeyTransRecipient
    public RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, final AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CMSException {
        final Object createContentCipher = EnvelopedDataHelper.createContentCipher(false, extractSecretKey(algorithmIdentifier, algorithmIdentifier2, bArr), algorithmIdentifier2);
        return new RecipientOperator(new InputDecryptor() {
            /* class org.bouncycastle.cms.bc.BcRSAKeyTransEnvelopedRecipient.AnonymousClass1 */

            @Override // org.bouncycastle.operator.InputDecryptor
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier2;
            }

            @Override // org.bouncycastle.operator.InputDecryptor
            public InputStream getInputStream(InputStream inputStream) {
                Object obj = createContentCipher;
                return obj instanceof BufferedBlockCipher ? new CipherInputStream(inputStream, (BufferedBlockCipher) obj) : new CipherInputStream(inputStream, (StreamCipher) obj);
            }
        });
    }
}
