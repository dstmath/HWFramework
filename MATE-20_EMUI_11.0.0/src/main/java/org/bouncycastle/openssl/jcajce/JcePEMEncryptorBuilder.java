package org.bouncycastle.openssl.jcajce;

import java.security.Provider;
import java.security.SecureRandom;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.PEMException;

public class JcePEMEncryptorBuilder {
    private final String algorithm;
    private JcaJceHelper helper = new DefaultJcaJceHelper();
    private SecureRandom random;

    public JcePEMEncryptorBuilder(String str) {
        this.algorithm = str;
    }

    public PEMEncryptor build(final char[] cArr) {
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        final byte[] bArr = new byte[(this.algorithm.startsWith("AES-") ? 16 : 8)];
        this.random.nextBytes(bArr);
        return new PEMEncryptor() {
            /* class org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder.AnonymousClass1 */

            @Override // org.bouncycastle.openssl.PEMEncryptor
            public byte[] encrypt(byte[] bArr) throws PEMException {
                return PEMUtilities.crypt(true, JcePEMEncryptorBuilder.this.helper, bArr, cArr, JcePEMEncryptorBuilder.this.algorithm, bArr);
            }

            @Override // org.bouncycastle.openssl.PEMEncryptor
            public String getAlgorithm() {
                return JcePEMEncryptorBuilder.this.algorithm;
            }

            @Override // org.bouncycastle.openssl.PEMEncryptor
            public byte[] getIV() {
                return bArr;
            }
        };
    }

    public JcePEMEncryptorBuilder setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JcePEMEncryptorBuilder setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }

    public JcePEMEncryptorBuilder setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
