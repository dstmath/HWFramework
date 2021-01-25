package org.bouncycastle.cms.jcajce;

import java.io.OutputStream;
import java.security.Key;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientOperator;
import org.bouncycastle.jcajce.io.MacOutputStream;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.MacCalculator;
import org.bouncycastle.operator.jcajce.JceGenericKey;

public class JceKEKAuthenticatedRecipient extends JceKEKRecipient {
    public JceKEKAuthenticatedRecipient(SecretKey secretKey) {
        super(secretKey);
    }

    @Override // org.bouncycastle.cms.KEKRecipient
    public RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, final AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CMSException {
        final Key extractSecretKey = extractSecretKey(algorithmIdentifier, algorithmIdentifier2, bArr);
        final Mac createContentMac = this.contentHelper.createContentMac(extractSecretKey, algorithmIdentifier2);
        return new RecipientOperator(new MacCalculator() {
            /* class org.bouncycastle.cms.jcajce.JceKEKAuthenticatedRecipient.AnonymousClass1 */

            @Override // org.bouncycastle.operator.MacCalculator
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier2;
            }

            @Override // org.bouncycastle.operator.MacCalculator
            public GenericKey getKey() {
                return new JceGenericKey(algorithmIdentifier2, extractSecretKey);
            }

            @Override // org.bouncycastle.operator.MacCalculator
            public byte[] getMac() {
                return createContentMac.doFinal();
            }

            @Override // org.bouncycastle.operator.MacCalculator
            public OutputStream getOutputStream() {
                return new MacOutputStream(createContentMac);
            }
        });
    }
}
