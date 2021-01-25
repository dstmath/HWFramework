package org.bouncycastle.pkcs.bc;

import java.io.InputStream;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.InputDecryptor;
import org.bouncycastle.operator.InputDecryptorProvider;

public class BcPKCS12PBEInputDecryptorProviderBuilder {
    private ExtendedDigest digest;

    public BcPKCS12PBEInputDecryptorProviderBuilder() {
        this(new SHA1Digest());
    }

    public BcPKCS12PBEInputDecryptorProviderBuilder(ExtendedDigest extendedDigest) {
        this.digest = extendedDigest;
    }

    public InputDecryptorProvider build(final char[] cArr) {
        return new InputDecryptorProvider() {
            /* class org.bouncycastle.pkcs.bc.BcPKCS12PBEInputDecryptorProviderBuilder.AnonymousClass1 */

            @Override // org.bouncycastle.operator.InputDecryptorProvider
            public InputDecryptor get(final AlgorithmIdentifier algorithmIdentifier) {
                final PaddedBufferedBlockCipher engine = PKCS12PBEUtils.getEngine(algorithmIdentifier.getAlgorithm());
                engine.init(false, PKCS12PBEUtils.createCipherParameters(algorithmIdentifier.getAlgorithm(), BcPKCS12PBEInputDecryptorProviderBuilder.this.digest, engine.getBlockSize(), PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters()), cArr));
                return new InputDecryptor() {
                    /* class org.bouncycastle.pkcs.bc.BcPKCS12PBEInputDecryptorProviderBuilder.AnonymousClass1.AnonymousClass1 */

                    @Override // org.bouncycastle.operator.InputDecryptor
                    public AlgorithmIdentifier getAlgorithmIdentifier() {
                        return algorithmIdentifier;
                    }

                    @Override // org.bouncycastle.operator.InputDecryptor
                    public InputStream getInputStream(InputStream inputStream) {
                        return new CipherInputStream(inputStream, engine);
                    }

                    public GenericKey getKey() {
                        return new GenericKey(PKCS12ParametersGenerator.PKCS12PasswordToBytes(cArr));
                    }
                };
            }
        };
    }
}
