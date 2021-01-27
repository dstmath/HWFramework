package org.bouncycastle.pkcs.jcajce;

import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.Provider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
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
import org.bouncycastle.jcajce.io.CipherInputStream;
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
    private JcaJceHelper helper = new DefaultJcaJceHelper();
    private SecretKeySizeProvider keySizeProvider = DefaultSecretKeySizeProvider.INSTANCE;
    private boolean wrongPKCS12Zero = false;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCCMorGCM(ASN1Encodable aSN1Encodable) {
        ASN1Encodable parameters = AlgorithmIdentifier.getInstance(aSN1Encodable).getParameters();
        if (!(parameters instanceof ASN1Sequence)) {
            return false;
        }
        ASN1Sequence instance = ASN1Sequence.getInstance(parameters);
        if (instance.size() == 2) {
            return instance.getObjectAt(1) instanceof ASN1Integer;
        }
        return false;
    }

    public InputDecryptorProvider build(final char[] cArr) {
        return new InputDecryptorProvider() {
            /* class org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder.AnonymousClass1 */
            private Cipher cipher;
            private AlgorithmIdentifier encryptionAlg;

            @Override // org.bouncycastle.operator.InputDecryptorProvider
            public InputDecryptor get(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
                SecretKey secretKey;
                ASN1ObjectIdentifier algorithm = algorithmIdentifier.getAlgorithm();
                try {
                    if (algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds)) {
                        PKCS12PBEParams instance = PKCS12PBEParams.getInstance(algorithmIdentifier.getParameters());
                        this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(algorithm.getId());
                        this.cipher.init(2, new PKCS12KeyWithParameters(cArr, JcePKCSPBEInputDecryptorProviderBuilder.this.wrongPKCS12Zero, instance.getIV(), instance.getIterations().intValue()));
                        this.encryptionAlg = algorithmIdentifier;
                    } else if (algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.id_PBES2)) {
                        PBES2Parameters instance2 = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
                        if (MiscObjectIdentifiers.id_scrypt.equals((ASN1Primitive) instance2.getKeyDerivationFunc().getAlgorithm())) {
                            ScryptParams instance3 = ScryptParams.getInstance(instance2.getKeyDerivationFunc().getParameters());
                            secretKey = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createSecretKeyFactory("SCRYPT").generateSecret(new ScryptKeySpec(cArr, instance3.getSalt(), instance3.getCostParameter().intValue(), instance3.getBlockSize().intValue(), instance3.getParallelizationParameter().intValue(), JcePKCSPBEInputDecryptorProviderBuilder.this.keySizeProvider.getKeySize(AlgorithmIdentifier.getInstance(instance2.getEncryptionScheme()))));
                        } else {
                            SecretKeyFactory createSecretKeyFactory = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createSecretKeyFactory(instance2.getKeyDerivationFunc().getAlgorithm().getId());
                            PBKDF2Params instance4 = PBKDF2Params.getInstance(instance2.getKeyDerivationFunc().getParameters());
                            AlgorithmIdentifier instance5 = AlgorithmIdentifier.getInstance(instance2.getEncryptionScheme());
                            secretKey = instance4.isDefaultPrf() ? createSecretKeyFactory.generateSecret(new PBEKeySpec(cArr, instance4.getSalt(), instance4.getIterationCount().intValue(), JcePKCSPBEInputDecryptorProviderBuilder.this.keySizeProvider.getKeySize(instance5))) : createSecretKeyFactory.generateSecret(new PBKDF2KeySpec(cArr, instance4.getSalt(), instance4.getIterationCount().intValue(), JcePKCSPBEInputDecryptorProviderBuilder.this.keySizeProvider.getKeySize(instance5), instance4.getPrf()));
                        }
                        this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(instance2.getEncryptionScheme().getAlgorithm().getId());
                        this.encryptionAlg = AlgorithmIdentifier.getInstance(instance2.getEncryptionScheme());
                        ASN1Encodable parameters = instance2.getEncryptionScheme().getParameters();
                        if (parameters instanceof ASN1OctetString) {
                            this.cipher.init(2, secretKey, new IvParameterSpec(ASN1OctetString.getInstance(parameters).getOctets()));
                        } else if ((parameters instanceof ASN1Sequence) && JcePKCSPBEInputDecryptorProviderBuilder.this.isCCMorGCM(instance2.getEncryptionScheme())) {
                            AlgorithmParameters instance6 = AlgorithmParameters.getInstance(instance2.getEncryptionScheme().getAlgorithm().getId());
                            instance6.init(((ASN1Sequence) parameters).getEncoded());
                            this.cipher.init(2, secretKey, instance6);
                        } else if (parameters == null) {
                            this.cipher.init(2, secretKey);
                        } else {
                            GOST28147Parameters instance7 = GOST28147Parameters.getInstance(parameters);
                            this.cipher.init(2, secretKey, new GOST28147ParameterSpec(instance7.getEncryptionParamSet(), instance7.getIV()));
                        }
                    } else {
                        if (!algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.pbeWithMD5AndDES_CBC)) {
                            if (!algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.pbeWithSHA1AndDES_CBC)) {
                                throw new OperatorCreationException("unable to create InputDecryptor: algorithm " + algorithm + " unknown.");
                            }
                        }
                        PBEParameter instance8 = PBEParameter.getInstance(algorithmIdentifier.getParameters());
                        this.cipher = JcePKCSPBEInputDecryptorProviderBuilder.this.helper.createCipher(algorithm.getId());
                        this.cipher.init(2, new PBKDF1Key(cArr, PasswordConverter.ASCII), new PBEParameterSpec(instance8.getSalt(), instance8.getIterationCount().intValue()));
                    }
                    return new InputDecryptor() {
                        /* class org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder.AnonymousClass1.AnonymousClass1 */

                        @Override // org.bouncycastle.operator.InputDecryptor
                        public AlgorithmIdentifier getAlgorithmIdentifier() {
                            return AnonymousClass1.this.encryptionAlg;
                        }

                        @Override // org.bouncycastle.operator.InputDecryptor
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
