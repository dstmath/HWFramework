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

    public RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, final AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CMSException {
        final Object createContentCipher = EnvelopedDataHelper.createContentCipher(false, extractSecretKey(algorithmIdentifier, algorithmIdentifier2, bArr), algorithmIdentifier2);
        return new RecipientOperator((InputDecryptor) new InputDecryptor() {
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier2;
            }

            public InputStream getInputStream(InputStream inputStream) {
                return createContentCipher instanceof BufferedBlockCipher ? new CipherInputStream(inputStream, (BufferedBlockCipher) createContentCipher) : new CipherInputStream(inputStream, (StreamCipher) createContentCipher);
            }
        });
    }
}
