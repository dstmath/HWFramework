package org.bouncycastle.cms.jcajce;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.KeyAgreeRecipientIdentifier;
import org.bouncycastle.asn1.cms.OriginatorPublicKey;
import org.bouncycastle.asn1.cms.RecipientEncryptedKey;
import org.bouncycastle.asn1.cms.RecipientKeyIdentifier;
import org.bouncycastle.asn1.cms.ecc.MQVuserKeyingMaterial;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.Gost2814789EncryptedKey;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.KeyAgreeRecipientInfoGenerator;
import org.bouncycastle.jcajce.spec.GOST28147WrapParameterSpec;
import org.bouncycastle.jcajce.spec.MQVParameterSpec;
import org.bouncycastle.jcajce.spec.UserKeyingMaterialSpec;
import org.bouncycastle.operator.DefaultSecretKeySizeProvider;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.SecretKeySizeProvider;
import org.bouncycastle.util.Arrays;

public class JceKeyAgreeRecipientInfoGenerator extends KeyAgreeRecipientInfoGenerator {
    private static KeyMaterialGenerator ecc_cms_Generator = new RFC5753KeyMaterialGenerator();
    private KeyPair ephemeralKP;
    private EnvelopedDataHelper helper = new EnvelopedDataHelper(new DefaultJcaJceExtHelper());
    private SecretKeySizeProvider keySizeProvider = new DefaultSecretKeySizeProvider();
    private SecureRandom random;
    private List recipientIDs = new ArrayList();
    private List recipientKeys = new ArrayList();
    private PrivateKey senderPrivateKey;
    private PublicKey senderPublicKey;
    private byte[] userKeyingMaterial;

    public JceKeyAgreeRecipientInfoGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier, PrivateKey privateKey, PublicKey publicKey, ASN1ObjectIdentifier aSN1ObjectIdentifier2) {
        super(aSN1ObjectIdentifier, SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()), aSN1ObjectIdentifier2);
        this.senderPublicKey = publicKey;
        this.senderPrivateKey = privateKey;
    }

    private void init(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws CMSException {
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        if (CMSUtils.isMQV(aSN1ObjectIdentifier) && this.ephemeralKP == null) {
            try {
                SubjectPublicKeyInfo instance = SubjectPublicKeyInfo.getInstance(this.senderPublicKey.getEncoded());
                AlgorithmParameters createAlgorithmParameters = this.helper.createAlgorithmParameters(aSN1ObjectIdentifier);
                createAlgorithmParameters.init(instance.getAlgorithm().getParameters().toASN1Primitive().getEncoded());
                KeyPairGenerator createKeyPairGenerator = this.helper.createKeyPairGenerator(aSN1ObjectIdentifier);
                createKeyPairGenerator.initialize(createAlgorithmParameters.getParameterSpec(AlgorithmParameterSpec.class), this.random);
                this.ephemeralKP = createKeyPairGenerator.generateKeyPair();
            } catch (Exception e) {
                throw new CMSException("cannot determine MQV ephemeral key pair parameters from public key: " + e, e);
            }
        }
    }

    public JceKeyAgreeRecipientInfoGenerator addRecipient(X509Certificate x509Certificate) throws CertificateEncodingException {
        this.recipientIDs.add(new KeyAgreeRecipientIdentifier(CMSUtils.getIssuerAndSerialNumber(x509Certificate)));
        this.recipientKeys.add(x509Certificate.getPublicKey());
        return this;
    }

    public JceKeyAgreeRecipientInfoGenerator addRecipient(byte[] bArr, PublicKey publicKey) throws CertificateEncodingException {
        this.recipientIDs.add(new KeyAgreeRecipientIdentifier(new RecipientKeyIdentifier(bArr)));
        this.recipientKeys.add(publicKey);
        return this;
    }

    public ASN1Sequence generateRecipientEncryptedKeys(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, GenericKey genericKey) throws CMSException {
        AlgorithmParameterSpec algorithmParameterSpec;
        DEROctetString dEROctetString;
        if (!this.recipientIDs.isEmpty()) {
            init(algorithmIdentifier.getAlgorithm());
            PrivateKey privateKey = this.senderPrivateKey;
            ASN1ObjectIdentifier algorithm = algorithmIdentifier.getAlgorithm();
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            int i = 0;
            while (i != this.recipientIDs.size()) {
                PublicKey publicKey = (PublicKey) this.recipientKeys.get(i);
                KeyAgreeRecipientIdentifier keyAgreeRecipientIdentifier = (KeyAgreeRecipientIdentifier) this.recipientIDs.get(i);
                try {
                    ASN1ObjectIdentifier algorithm2 = algorithmIdentifier2.getAlgorithm();
                    if (CMSUtils.isMQV(algorithm)) {
                        algorithmParameterSpec = new MQVParameterSpec(this.ephemeralKP, publicKey, this.userKeyingMaterial);
                    } else if (CMSUtils.isEC(algorithm)) {
                        algorithmParameterSpec = new UserKeyingMaterialSpec(ecc_cms_Generator.generateKDFMaterial(algorithmIdentifier2, this.keySizeProvider.getKeySize(algorithm2), this.userKeyingMaterial));
                    } else if (CMSUtils.isRFC2631(algorithm)) {
                        if (this.userKeyingMaterial != null) {
                            algorithmParameterSpec = new UserKeyingMaterialSpec(this.userKeyingMaterial);
                        } else if (!algorithm.equals(PKCSObjectIdentifiers.id_alg_SSDH)) {
                            algorithmParameterSpec = null;
                        } else {
                            throw new CMSException("User keying material must be set for static keys.");
                        }
                    } else if (!CMSUtils.isGOST(algorithm)) {
                        throw new CMSException("Unknown key agreement algorithm: " + algorithm);
                    } else if (this.userKeyingMaterial != null) {
                        algorithmParameterSpec = new UserKeyingMaterialSpec(this.userKeyingMaterial);
                    } else {
                        throw new CMSException("User keying material must be set for static keys.");
                    }
                    KeyAgreement createKeyAgreement = this.helper.createKeyAgreement(algorithm);
                    createKeyAgreement.init(privateKey, algorithmParameterSpec, this.random);
                    createKeyAgreement.doPhase(publicKey, true);
                    SecretKey generateSecret = createKeyAgreement.generateSecret(algorithm2.getId());
                    Cipher createCipher = this.helper.createCipher(algorithm2);
                    if (!algorithm2.equals(CryptoProObjectIdentifiers.id_Gost28147_89_None_KeyWrap)) {
                        if (!algorithm2.equals(CryptoProObjectIdentifiers.id_Gost28147_89_CryptoPro_KeyWrap)) {
                            createCipher.init(3, generateSecret, this.random);
                            dEROctetString = new DEROctetString(createCipher.wrap(this.helper.getJceKey(genericKey)));
                            aSN1EncodableVector.add(new RecipientEncryptedKey(keyAgreeRecipientIdentifier, dEROctetString));
                            i++;
                        }
                    }
                    createCipher.init(3, generateSecret, new GOST28147WrapParameterSpec(CryptoProObjectIdentifiers.id_Gost28147_89_CryptoPro_A_ParamSet, this.userKeyingMaterial));
                    byte[] wrap = createCipher.wrap(this.helper.getJceKey(genericKey));
                    dEROctetString = new DEROctetString(new Gost2814789EncryptedKey(Arrays.copyOfRange(wrap, 0, wrap.length - 4), Arrays.copyOfRange(wrap, wrap.length - 4, wrap.length)).getEncoded(ASN1Encoding.DER));
                    aSN1EncodableVector.add(new RecipientEncryptedKey(keyAgreeRecipientIdentifier, dEROctetString));
                    i++;
                } catch (GeneralSecurityException e) {
                    throw new CMSException("cannot perform agreement step: " + e.getMessage(), e);
                } catch (IOException e2) {
                    throw new CMSException("unable to encode wrapped key: " + e2.getMessage(), e2);
                }
            }
            return new DERSequence(aSN1EncodableVector);
        }
        throw new CMSException("No recipients associated with generator - use addRecipient()");
    }

    /* access modifiers changed from: protected */
    public byte[] getUserKeyingMaterial(AlgorithmIdentifier algorithmIdentifier) throws CMSException {
        init(algorithmIdentifier.getAlgorithm());
        if (this.ephemeralKP == null) {
            return this.userKeyingMaterial;
        }
        OriginatorPublicKey createOriginatorPublicKey = createOriginatorPublicKey(SubjectPublicKeyInfo.getInstance(this.ephemeralKP.getPublic().getEncoded()));
        try {
            return this.userKeyingMaterial != null ? new MQVuserKeyingMaterial(createOriginatorPublicKey, new DEROctetString(this.userKeyingMaterial)).getEncoded() : new MQVuserKeyingMaterial(createOriginatorPublicKey, null).getEncoded();
        } catch (IOException e) {
            throw new CMSException("unable to encode user keying material: " + e.getMessage(), e);
        }
    }

    public JceKeyAgreeRecipientInfoGenerator setProvider(String str) {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceExtHelper(str));
        return this;
    }

    public JceKeyAgreeRecipientInfoGenerator setProvider(Provider provider) {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceExtHelper(provider));
        return this;
    }

    public JceKeyAgreeRecipientInfoGenerator setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }

    public JceKeyAgreeRecipientInfoGenerator setUserKeyingMaterial(byte[] bArr) {
        this.userKeyingMaterial = Arrays.clone(bArr);
        return this;
    }
}
