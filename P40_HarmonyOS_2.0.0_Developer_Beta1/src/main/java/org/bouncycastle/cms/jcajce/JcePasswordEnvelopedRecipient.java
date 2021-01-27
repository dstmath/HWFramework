package org.bouncycastle.cms.jcajce;

import java.io.InputStream;
import javax.crypto.Cipher;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientOperator;
import org.bouncycastle.jcajce.io.CipherInputStream;
import org.bouncycastle.operator.InputDecryptor;

public class JcePasswordEnvelopedRecipient extends JcePasswordRecipient {
    public JcePasswordEnvelopedRecipient(char[] cArr) {
        super(cArr);
    }

    @Override // org.bouncycastle.cms.PasswordRecipient
    public RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, final AlgorithmIdentifier algorithmIdentifier2, byte[] bArr, byte[] bArr2) throws CMSException {
        final Cipher createContentCipher = this.helper.createContentCipher(extractSecretKey(algorithmIdentifier, algorithmIdentifier2, bArr, bArr2), algorithmIdentifier2);
        return new RecipientOperator(new InputDecryptor() {
            /* class org.bouncycastle.cms.jcajce.JcePasswordEnvelopedRecipient.AnonymousClass1 */

            @Override // org.bouncycastle.operator.InputDecryptor
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier2;
            }

            @Override // org.bouncycastle.operator.InputDecryptor
            public InputStream getInputStream(InputStream inputStream) {
                return new CipherInputStream(inputStream, createContentCipher);
            }
        });
    }
}
