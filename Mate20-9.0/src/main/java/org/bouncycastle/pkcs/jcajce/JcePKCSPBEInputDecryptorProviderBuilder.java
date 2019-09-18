package org.bouncycastle.pkcs.jcajce;

import java.io.InputStream;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.cryptopro.GOST28147Parameters;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.ScryptParams;
import org.bouncycastle.asn1.pkcs.PBEParameter;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.PasswordConverter;
import org.bouncycastle.jcajce.PBKDF1Key;
import org.bouncycastle.jcajce.PKCS12KeyWithParameters;
import org.bouncycastle.jcajce.spec.GOST28147ParameterSpec;
import org.bouncycastle.jcajce.spec.PBKDF2KeySpec;
import org.bouncycastle.jcajce.spec.ScryptKeySpec;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.operator.DefaultSecretKeySizeProvider;
import org.bouncycastle.operator.InputDecryptor;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.SecretKeySizeProvider;

public class JcePKCSPBEInputDecryptorProviderBuilder {
    /* access modifiers changed from: private */
    public JcaJceHelper helper = new DefaultJcaJceHelper();
    /* access modifiers changed from: private */
    public SecretKeySizeProvider keySizeProvider = DefaultSecretKeySizeProvider.INSTANCE;
    /* access modifiers changed from: private */
    public boolean wrongPKCS12Zero = false;

    public InputDecryptorProvider build(final char[] cArr) {
        return new InputDecryptorProvider() {
            /* access modifiers changed from: private */
            public Cipher cipher;
            /* access modifiers changed from: private */
            public AlgorithmIdentifier encryptionAlg;

            public InputDecryptor get(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
                SecretKey secretKey;
                Cipher cipher2;
                AlgorithmParameterSpec gOST28147ParameterSpec;
                ASN1ObjectIdentifier algorithm = algorithmIdentifier.getAlgorithm();
                try {
                    if (algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
                        PKCS12PBEParams instance = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
                        this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(algorithm.getId());
                        this.cipher.init(2, new PKCS12KeyWithParameters(cArr, JcePKCSPBEInputDecryptorProviderBuilder.this.wrongPKCS12Zero, instance.getIV(), instance.getIterations().intValue()));
                        this.encryptionAlg = algorithmIdentifier;
                    } else if (algorithm.equals(PKCSObjectIdentifiers.id_PBES2)) {
                        PBES2Parameters instance2 = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
                        if (MiscObjectIdentifiers.id_scrypt.equals(instance2.getKeyDerivationFunc().getAlgorithm())) {
                            ScryptParams instance3 = ScryptParams.getInstance(instance2.getKeyDerivationFunc().getParameters());
                            AlgorithmIdentifier instance4 = AlgorithmIdentifier.getInstance(instance2.getEncryptionScheme());
                            SecretKeyFactory createSecretKeyFactory = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createSecretKeyFactory("SCRYPT");
                            ScryptKeySpec scryptKeySpec = new ScryptKeySpec(cArr, instance3.getSalt(), instance3.getCostParameter().intValue(), instance3.getBlockSize().intValue(), instance3.getParallelizationParameter().intValue(), JcePKCSPBEInputDecryptorProviderBuilder.this.keySizeProvider.getKeySize(instance4));
                            secretKey = createSecretKeyFactory.generateSecret(scryptKeySpec);
                        } else {
                            SecretKeyFactory createSecretKeyFactory2 = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createSecretKeyFactory(instance2.getKeyDerivationFunc().getAlgorithm().getId());
                            PBKDF2Params instance5 = PBKDF2Params.getInstance(instance2.getKeyDerivationFunc().getParameters());
                            AlgorithmIdentifier instance6 = AlgorithmIdentifier.getInstance(instance2.getEncryptionScheme());
                            if (instance5.isDefaultPrf()) {
                                secretKey = createSecretKeyFactory2.generateSecret(new PBEKeySpec(cArr, instance5.getSalt(), instance5.getIterationCount().intValue(), JcePKCSPBEInputDecryptorProviderBuilder.this.keySizeProvider.getKeySize(instance6)));
                            } else {
                                PBKDF2KeySpec pBKDF2KeySpec = new PBKDF2KeySpec(cArr, instance5.getSalt(), instance5.getIterationCount().intValue(), JcePKCSPBEInputDecryptorProviderBuilder.this.keySizeProvider.getKeySize(instance6), instance5.getPrf());
                                secretKey = createSecretKeyFactory2.generateSecret(pBKDF2KeySpec);
                            }
                        }
                        this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(instance2.getEncryptionScheme().getAlgorithm().getId());
                        this.encryptionAlg = AlgorithmIdentifier.getInstance(instance2.getEncryptionScheme());
                        ASN1Encodable parameters = instance2.getEncryptionScheme().getParameters();
                        if (parameters instanceof ASN1OctetString) {
                            cipher2 = this.cipher;
                            gOST28147ParameterSpec = new IvParameterSpec(ASN1OctetString.getInstance(parameters).getOctets());
                        } else {
                            GOST28147Parameters instance7 = GOST28147Parameters.getInstance(parameters);
                            cipher2 = this.cipher;
                            gOST28147ParameterSpec = new GOST28147ParameterSpec(instance7.getEncryptionParamSet(), instance7.getIV());
                        }
                        cipher2.init(2, secretKey, gOST28147ParameterSpec);
                    } else {
                        if (!algorithm.equals(PKCSObjectIdentifiers.pbeWithMD5AndDES_CBC)) {
                            if (!algorithm.equals(PKCSObjectIdentifiers.pbeWithSHA1AndDES_CBC)) {
                                throw new OperatorCreationException("unable to create InputDecryptor: algorithm " + algorithm + " unknown.");
                            }
                        }
                        PBEParameter instance8 = PBEParameter.getInstance(algorithmIdentifier.getParameters());
                        this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(algorithm.getId());
                        this.cipher.init(2, new PBKDF1Key(cArr, PasswordConverter.ASCII), new PBEParameterSpec(instance8.getSalt(), instance8.getIterationCount().intValue()));
                    }
                    return new InputDecryptor() {
                        public AlgorithmIdentifier getAlgorithmIdentifier() {
                            return AnonymousClass1.this.encryptionAlg;
                        }

                        public InputStream getInputStream(InputStream inputStream) {
                            return new CipherInputStream(inputStream, AnonymousClass1.this.cipher);
                        }
                    };
                } catch (Exception e) {
                    throw new OperatorCreationException("unable to create InputDecryptor: " + e.getMessage(), e);
                }
            }
        };
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setKeySizeProvider(SecretKeySizeProvider secretKeySizeProvider) {
        this.keySizeProvider = secretKeySizeProvider;
        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }

    public JcePKCSPBEInputDecryptorProviderBuilder setTryWrongPKCS12Zero(boolean z) {
        this.wrongPKCS12Zero = z;
        return this;
    }
}
