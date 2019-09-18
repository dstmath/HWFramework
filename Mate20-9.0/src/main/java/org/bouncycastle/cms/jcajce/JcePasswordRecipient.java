package org.bouncycastle.cms.jcajce;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.Provider;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.PasswordRecipient;

public abstract class JcePasswordRecipient implements PasswordRecipient {
    protected EnvelopedDataHelper helper = new EnvelopedDataHelper(new DefaultJcaJceExtHelper());
    private char[] password;
    private int schemeID = 1;

    JcePasswordRecipient(char[] cArr) {
        this.password = cArr;
    }

    public byte[] calculateDerivedKey(int i, AlgorithmIdentifier algorithmIdentifier, int i2) throws CMSException {
        return this.helper.calculateDerivedKey(i, this.password, algorithmIdentifier, i2);
    }

    /* access modifiers changed from: protected */
    public Key extractSecretKey(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, byte[] bArr, byte[] bArr2) throws CMSException {
        Cipher createRFC3211Wrapper = this.helper.createRFC3211Wrapper(algorithmIdentifier.getAlgorithm());
        try {
            createRFC3211Wrapper.init(4, new SecretKeySpec(bArr, createRFC3211Wrapper.getAlgorithm()), new IvParameterSpec(ASN1OctetString.getInstance(algorithmIdentifier.getParameters()).getOctets()));
            return createRFC3211Wrapper.unwrap(bArr2, algorithmIdentifier2.getAlgorithm().getId(), 3);
        } catch (GeneralSecurityException e) {
            throw new CMSException("cannot process content encryption key: " + e.getMessage(), e);
        }
    }

    public char[] getPassword() {
        return this.password;
    }

    public int getPasswordConversionScheme() {
        return this.schemeID;
    }

    public JcePasswordRecipient setPasswordConversionScheme(int i) {
        this.schemeID = i;
        return this;
    }

    public JcePasswordRecipient setProvider(String str) {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceExtHelper(str));
        return this;
    }

    public JcePasswordRecipient setProvider(Provider provider) {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceExtHelper(provider));
        return this;
    }
}
