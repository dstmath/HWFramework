package org.bouncycastle.cms.jcajce;

import java.io.InputStream;
import java.security.PrivateKey;
import javax.crypto.Cipher;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientOperator;
import org.bouncycastle.jcajce.io.CipherInputStream;
import org.bouncycastle.operator.InputDecryptor;

public class JceKeyAgreeEnvelopedRecipient extends JceKeyAgreeRecipient {
    public JceKeyAgreeEnvelopedRecipient(PrivateKey privateKey) {
        super(privateKey);
    }

    @Override // org.bouncycastle.cms.KeyAgreeRecipient
    public RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, final AlgorithmIdentifier algorithmIdentifier2, SubjectPublicKeyInfo subjectPublicKeyInfo, ASN1OctetString aSN1OctetString, byte[] bArr) throws CMSException {
        final Cipher createContentCipher = this.contentHelper.createContentCipher(extractSecretKey(algorithmIdentifier, algorithmIdentifier2, subjectPublicKeyInfo, aSN1OctetString, bArr), algorithmIdentifier2);
        return new RecipientOperator(new InputDecryptor() {
            /* class org.bouncycastle.cms.jcajce.JceKeyAgreeEnvelopedRecipient.AnonymousClass1 */

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
