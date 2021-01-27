package org.bouncycastle.openssl.jcajce;

import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.Provider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.pkcs.EncryptionScheme;
import org.bouncycastle.asn1.pkcs.KeyDerivationFunc;
import org.bouncycastle.asn1.pkcs.PBEParameter;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.CharToByteConverter;
import org.bouncycastle.jcajce.PBKDF1KeyWithParameters;
import org.bouncycastle.jcajce.PKCS12KeyWithParameters;
import org.bouncycastle.jcajce.io.CipherInputStream;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.operator.InputDecryptor;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Strings;

public class JceOpenSSLPKCS8DecryptorProviderBuilder {
    private JcaJceHelper helper = new DefaultJcaJceHelper();

    public InputDecryptorProvider build(final char[] cArr) throws OperatorCreationException {
        return new InputDecryptorProvider() {
            /* class org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder.AnonymousClass1 */

            @Override // org.bouncycastle.operator.InputDecryptorProvider
            public InputDecryptor get(final AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
                final Cipher cipher;
                Key pBKDF1KeyWithParameters;
                try {
                    if (PEMUtilities.isPKCS5Scheme2(algorithmIdentifier.getAlgorithm())) {
                        PBES2Parameters instance = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
                        KeyDerivationFunc keyDerivationFunc = instance.getKeyDerivationFunc();
                        EncryptionScheme encryptionScheme = instance.getEncryptionScheme();
                        PBKDF2Params pBKDF2Params = (PBKDF2Params) keyDerivationFunc.getParameters();
                        int intValue = pBKDF2Params.getIterationCount().intValue();
                        byte[] salt = pBKDF2Params.getSalt();
                        String id = encryptionScheme.getAlgorithm().getId();
                        SecretKey generateSecretKeyForPKCS5Scheme2 = PEMUtilities.isHmacSHA1(pBKDF2Params.getPrf()) ? PEMUtilities.generateSecretKeyForPKCS5Scheme2(JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper, id, cArr, salt, intValue) : PEMUtilities.generateSecretKeyForPKCS5Scheme2(JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper, id, cArr, salt, intValue, pBKDF2Params.getPrf());
                        cipher = JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper.createCipher(id);
                        AlgorithmParameters createAlgorithmParameters = JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper.createAlgorithmParameters(id);
                        createAlgorithmParameters.init(encryptionScheme.getParameters().toASN1Primitive().getEncoded());
                        cipher.init(2, generateSecretKeyForPKCS5Scheme2, createAlgorithmParameters);
                    } else {
                        if (PEMUtilities.isPKCS12(algorithmIdentifier.getAlgorithm())) {
                            PKCS12PBEParams instance2 = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
                            cipher = JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper.createCipher(algorithmIdentifier.getAlgorithm().getId());
                            pBKDF1KeyWithParameters = new PKCS12KeyWithParameters(cArr, instance2.getIV(), instance2.getIterations().intValue());
                        } else if (PEMUtilities.isPKCS5Scheme1(algorithmIdentifier.getAlgorithm())) {
                            PBEParameter instance3 = PBEParameter.getInstance(algorithmIdentifier.getParameters());
                            cipher = JceOpenSSLPKCS8DecryptorProviderBuilder.this.helper.createCipher(algorithmIdentifier.getAlgorithm().getId());
                            pBKDF1KeyWithParameters = new PBKDF1KeyWithParameters(cArr, new CharToByteConverter() {
                                /* class org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder.AnonymousClass1.AnonymousClass1 */

                                @Override // org.bouncycastle.crypto.CharToByteConverter
                                public byte[] convert(char[] cArr) {
                                    return Strings.toByteArray(cArr);
                                }

                                @Override // org.bouncycastle.crypto.CharToByteConverter
                                public String getType() {
                                    return "ASCII";
                                }
                            }, instance3.getSalt(), instance3.getIterationCount().intValue());
                        } else {
                            throw new PEMException("Unknown algorithm: " + algorithmIdentifier.getAlgorithm());
                        }
                        cipher.init(2, pBKDF1KeyWithParameters);
                    }
                    return new InputDecryptor() {
                        /* class org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder.AnonymousClass1.AnonymousClass2 */

                        @Override // org.bouncycastle.operator.InputDecryptor
                        public AlgorithmIdentifier getAlgorithmIdentifier() {
                            return algorithmIdentifier;
                        }

                        @Override // org.bouncycastle.operator.InputDecryptor
                        public InputStream getInputStream(InputStream inputStream) {
                            return new CipherInputStream(inputStream, cipher);
                        }
                    };
                } catch (IOException e) {
                    throw new OperatorCreationException(algorithmIdentifier.getAlgorithm() + " not available: " + e.getMessage(), e);
                } catch (GeneralSecurityException e2) {
                    throw new OperatorCreationException(algorithmIdentifier.getAlgorithm() + " not available: " + e2.getMessage(), e2);
                }
            }
        };
    }

    public JceOpenSSLPKCS8DecryptorProviderBuilder setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JceOpenSSLPKCS8DecryptorProviderBuilder setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }
}
