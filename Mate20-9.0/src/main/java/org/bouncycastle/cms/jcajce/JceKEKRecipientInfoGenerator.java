package org.bouncycastle.cms.jcajce;

import java.security.Provider;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.cms.KEKIdentifier;
import org.bouncycastle.cms.KEKRecipientInfoGenerator;
import org.bouncycastle.operator.jcajce.JceSymmetricKeyWrapper;

public class JceKEKRecipientInfoGenerator extends KEKRecipientInfoGenerator {
    public JceKEKRecipientInfoGenerator(KEKIdentifier kEKIdentifier, SecretKey secretKey) {
        super(kEKIdentifier, new JceSymmetricKeyWrapper(secretKey));
    }

    public JceKEKRecipientInfoGenerator(byte[] bArr, SecretKey secretKey) {
        this(new KEKIdentifier(bArr, null, null), secretKey);
    }

    public JceKEKRecipientInfoGenerator setProvider(String str) {
        ((JceSymmetricKeyWrapper) this.wrapper).setProvider(str);
        return this;
    }

    public JceKEKRecipientInfoGenerator setProvider(Provider provider) {
        ((JceSymmetricKeyWrapper) this.wrapper).setProvider(provider);
        return this;
    }

    public JceKEKRecipientInfoGenerator setSecureRandom(SecureRandom secureRandom) {
        ((JceSymmetricKeyWrapper) this.wrapper).setSecureRandom(secureRandom);
        return this;
    }
}
