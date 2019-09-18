package org.bouncycastle.cms.jcajce;

import java.io.OutputStream;
import java.security.Key;
import java.security.PrivateKey;
import javax.crypto.Mac;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientOperator;
import org.bouncycastle.jcajce.io.MacOutputStream;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.MacCalculator;
import org.bouncycastle.operator.jcajce.JceGenericKey;

public class JceKeyAgreeAuthenticatedRecipient extends JceKeyAgreeRecipient {
    public JceKeyAgreeAuthenticatedRecipient(PrivateKey privateKey) {
        super(privateKey);
    }

    public RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, final AlgorithmIdentifier algorithmIdentifier2, SubjectPublicKeyInfo subjectPublicKeyInfo, ASN1OctetString aSN1OctetString, byte[] bArr) throws CMSException {
        final Key extractSecretKey = extractSecretKey(algorithmIdentifier, algorithmIdentifier2, subjectPublicKeyInfo, aSN1OctetString, bArr);
        final Mac createContentMac = this.contentHelper.createContentMac(extractSecretKey, algorithmIdentifier2);
        return new RecipientOperator((MacCalculator) new MacCalculator() {
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier2;
            }

            public GenericKey getKey() {
                return new JceGenericKey(algorithmIdentifier2, extractSecretKey);
            }

            public byte[] getMac() {
                return createContentMac.doFinal();
            }

            public OutputStream getOutputStream() {
                return new MacOutputStream(createContentMac);
            }
        });
    }
}
