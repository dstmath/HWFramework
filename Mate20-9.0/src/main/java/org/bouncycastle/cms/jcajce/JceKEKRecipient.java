package org.bouncycastle.cms.jcajce;

import java.security.Key;
import java.security.Provider;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.KEKRecipient;
import org.bouncycastle.operator.OperatorException;

public abstract class JceKEKRecipient implements KEKRecipient {
    protected EnvelopedDataHelper contentHelper = this.helper;
    protected EnvelopedDataHelper helper = new EnvelopedDataHelper(new DefaultJcaJceExtHelper());
    private SecretKey recipientKey;
    protected boolean validateKeySize = false;

    public JceKEKRecipient(SecretKey secretKey) {
        this.recipientKey = secretKey;
    }

    /* access modifiers changed from: protected */
    public Key extractSecretKey(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CMSException {
        try {
            Key jceKey = this.helper.getJceKey(algorithmIdentifier2.getAlgorithm(), this.helper.createSymmetricUnwrapper(algorithmIdentifier, this.recipientKey).generateUnwrappedKey(algorithmIdentifier2, bArr));
            if (this.validateKeySize) {
                this.helper.keySizeCheck(algorithmIdentifier2, jceKey);
            }
            return jceKey;
        } catch (OperatorException e) {
            throw new CMSException("exception unwrapping key: " + e.getMessage(), e);
        }
    }

    public JceKEKRecipient setContentProvider(String str) {
        this.contentHelper = new EnvelopedDataHelper(new NamedJcaJceExtHelper(str));
        return this;
    }

    public JceKEKRecipient setContentProvider(Provider provider) {
        this.contentHelper = new EnvelopedDataHelper(new ProviderJcaJceExtHelper(provider));
        return this;
    }

    public JceKEKRecipient setKeySizeValidation(boolean z) {
        this.validateKeySize = z;
        return this;
    }

    public JceKEKRecipient setProvider(String str) {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceExtHelper(str));
        this.contentHelper = this.helper;
        return this;
    }

    public JceKEKRecipient setProvider(Provider provider) {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceExtHelper(provider));
        this.contentHelper = this.helper;
        return this;
    }
}
