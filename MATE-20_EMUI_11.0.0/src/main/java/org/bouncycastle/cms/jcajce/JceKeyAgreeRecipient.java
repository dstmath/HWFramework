package org.bouncycastle.cms.jcajce;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.cms.ecc.ECCCMSSharedInfo;
import org.bouncycastle.asn1.cms.ecc.MQVuserKeyingMaterial;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.Gost2814789EncryptedKey;
import org.bouncycastle.asn1.cryptopro.Gost2814789KeyWrapParameters;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.KeyAgreeRecipient;
import org.bouncycastle.jcajce.spec.GOST28147WrapParameterSpec;
import org.bouncycastle.jcajce.spec.MQVParameterSpec;
import org.bouncycastle.jcajce.spec.UserKeyingMaterialSpec;
import org.bouncycastle.operator.DefaultSecretKeySizeProvider;
import org.bouncycastle.operator.SecretKeySizeProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public abstract class JceKeyAgreeRecipient implements KeyAgreeRecipient {
    private static KeyMaterialGenerator ecc_cms_Generator = new RFC5753KeyMaterialGenerator();
    private static KeyMaterialGenerator old_ecc_cms_Generator = new KeyMaterialGenerator() {
        /* class org.bouncycastle.cms.jcajce.JceKeyAgreeRecipient.AnonymousClass1 */

        @Override // org.bouncycastle.cms.jcajce.KeyMaterialGenerator
        public byte[] generateKDFMaterial(AlgorithmIdentifier algorithmIdentifier, int i, byte[] bArr) {
            try {
                return new ECCCMSSharedInfo(new AlgorithmIdentifier(algorithmIdentifier.getAlgorithm(), DERNull.INSTANCE), bArr, Pack.intToBigEndian(i)).getEncoded(ASN1Encoding.DER);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create KDF material: " + e);
            }
        }
    };
    private static final Set possibleOldMessages = new HashSet();
    protected EnvelopedDataHelper contentHelper = this.helper;
    protected EnvelopedDataHelper helper = new EnvelopedDataHelper(new DefaultJcaJceExtHelper());
    private SecretKeySizeProvider keySizeProvider = new DefaultSecretKeySizeProvider();
    private AlgorithmIdentifier privKeyAlgID = null;
    private PrivateKey recipientKey;

    static {
        possibleOldMessages.add(X9ObjectIdentifiers.dhSinglePass_stdDH_sha1kdf_scheme);
        possibleOldMessages.add(X9ObjectIdentifiers.mqvSinglePass_sha1kdf_scheme);
    }

    public JceKeyAgreeRecipient(PrivateKey privateKey) {
        this.recipientKey = CMSUtils.cleanPrivateKey(privateKey);
    }

    private SecretKey calculateAgreedWrapKey(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, PublicKey publicKey, ASN1OctetString aSN1OctetString, PrivateKey privateKey, KeyMaterialGenerator keyMaterialGenerator) throws CMSException, GeneralSecurityException, IOException {
        PrivateKey cleanPrivateKey = CMSUtils.cleanPrivateKey(privateKey);
        UserKeyingMaterialSpec userKeyingMaterialSpec = null;
        byte[] bArr = null;
        userKeyingMaterialSpec = null;
        if (CMSUtils.isMQV(algorithmIdentifier.getAlgorithm())) {
            MQVuserKeyingMaterial instance = MQVuserKeyingMaterial.getInstance(aSN1OctetString.getOctets());
            PublicKey generatePublic = this.helper.createKeyFactory(algorithmIdentifier.getAlgorithm()).generatePublic(new X509EncodedKeySpec(new SubjectPublicKeyInfo(getPrivateKeyAlgorithmIdentifier(), instance.getEphemeralPublicKey().getPublicKey().getBytes()).getEncoded()));
            KeyAgreement createKeyAgreement = this.helper.createKeyAgreement(algorithmIdentifier.getAlgorithm());
            if (instance.getAddedukm() != null) {
                bArr = instance.getAddedukm().getOctets();
            }
            KeyMaterialGenerator keyMaterialGenerator2 = old_ecc_cms_Generator;
            if (keyMaterialGenerator == keyMaterialGenerator2) {
                bArr = keyMaterialGenerator2.generateKDFMaterial(algorithmIdentifier2, this.keySizeProvider.getKeySize(algorithmIdentifier2), bArr);
            }
            createKeyAgreement.init(cleanPrivateKey, new MQVParameterSpec(cleanPrivateKey, generatePublic, bArr));
            createKeyAgreement.doPhase(publicKey, true);
            return createKeyAgreement.generateSecret(algorithmIdentifier2.getAlgorithm().getId());
        }
        KeyAgreement createKeyAgreement2 = this.helper.createKeyAgreement(algorithmIdentifier.getAlgorithm());
        if (CMSUtils.isEC(algorithmIdentifier.getAlgorithm())) {
            int keySize = this.keySizeProvider.getKeySize(algorithmIdentifier2);
            userKeyingMaterialSpec = aSN1OctetString != null ? new UserKeyingMaterialSpec(keyMaterialGenerator.generateKDFMaterial(algorithmIdentifier2, keySize, aSN1OctetString.getOctets())) : new UserKeyingMaterialSpec(keyMaterialGenerator.generateKDFMaterial(algorithmIdentifier2, keySize, null));
        } else if (CMSUtils.isRFC2631(algorithmIdentifier.getAlgorithm())) {
            if (aSN1OctetString != null) {
                userKeyingMaterialSpec = new UserKeyingMaterialSpec(aSN1OctetString.getOctets());
            }
        } else if (!CMSUtils.isGOST(algorithmIdentifier.getAlgorithm())) {
            throw new CMSException("Unknown key agreement algorithm: " + algorithmIdentifier.getAlgorithm());
        } else if (aSN1OctetString != null) {
            userKeyingMaterialSpec = new UserKeyingMaterialSpec(aSN1OctetString.getOctets());
        }
        createKeyAgreement2.init(cleanPrivateKey, userKeyingMaterialSpec);
        createKeyAgreement2.doPhase(publicKey, true);
        return createKeyAgreement2.generateSecret(algorithmIdentifier2.getAlgorithm().getId());
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00c1, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00c9, code lost:
        throw new org.bouncycastle.cms.CMSException("originator key invalid.", r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00ca, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00d2, code lost:
        throw new org.bouncycastle.cms.CMSException("required padding not supported.", r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00d3, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00db, code lost:
        throw new org.bouncycastle.cms.CMSException("originator key spec invalid.", r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00dc, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00e4, code lost:
        throw new org.bouncycastle.cms.CMSException("key invalid in message.", r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00e5, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00ed, code lost:
        throw new org.bouncycastle.cms.CMSException("can't find algorithm.", r9);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00c1 A[ExcHandler: Exception (r9v5 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:0:0x0000] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00ca A[ExcHandler: NoSuchPaddingException (r9v4 'e' javax.crypto.NoSuchPaddingException A[CUSTOM_DECLARE]), Splitter:B:0:0x0000] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00d3 A[ExcHandler: InvalidKeySpecException (r9v3 'e' java.security.spec.InvalidKeySpecException A[CUSTOM_DECLARE]), Splitter:B:0:0x0000] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00e5 A[ExcHandler: NoSuchAlgorithmException (r9v1 'e' java.security.NoSuchAlgorithmException A[CUSTOM_DECLARE]), Splitter:B:0:0x0000] */
    public Key extractSecretKey(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, SubjectPublicKeyInfo subjectPublicKeyInfo, ASN1OctetString aSN1OctetString, byte[] bArr) throws CMSException {
        AlgorithmIdentifier instance;
        PublicKey generatePublic;
        try {
            instance = AlgorithmIdentifier.getInstance(algorithmIdentifier.getParameters());
            generatePublic = this.helper.createKeyFactory(subjectPublicKeyInfo.getAlgorithm().getAlgorithm()).generatePublic(new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded()));
            SecretKey calculateAgreedWrapKey = calculateAgreedWrapKey(algorithmIdentifier, instance, generatePublic, aSN1OctetString, this.recipientKey, ecc_cms_Generator);
            if (!instance.getAlgorithm().equals((ASN1Primitive) CryptoProObjectIdentifiers.id_Gost28147_89_None_KeyWrap)) {
                if (!instance.getAlgorithm().equals((ASN1Primitive) CryptoProObjectIdentifiers.id_Gost28147_89_CryptoPro_KeyWrap)) {
                    return unwrapSessionKey(instance.getAlgorithm(), calculateAgreedWrapKey, algorithmIdentifier2.getAlgorithm(), bArr);
                }
            }
            Gost2814789EncryptedKey instance2 = Gost2814789EncryptedKey.getInstance(bArr);
            Gost2814789KeyWrapParameters instance3 = Gost2814789KeyWrapParameters.getInstance(instance.getParameters());
            Cipher createCipher = this.helper.createCipher(instance.getAlgorithm());
            createCipher.init(4, calculateAgreedWrapKey, new GOST28147WrapParameterSpec(instance3.getEncryptionParamSet(), aSN1OctetString.getOctets()));
            return createCipher.unwrap(Arrays.concatenate(instance2.getEncryptedKey(), instance2.getMacKey()), this.helper.getBaseCipherName(algorithmIdentifier2.getAlgorithm()), 3);
        } catch (InvalidKeyException e) {
            if (possibleOldMessages.contains(algorithmIdentifier.getAlgorithm())) {
                return unwrapSessionKey(instance.getAlgorithm(), calculateAgreedWrapKey(algorithmIdentifier, instance, generatePublic, aSN1OctetString, this.recipientKey, old_ecc_cms_Generator), algorithmIdentifier2.getAlgorithm(), bArr);
            }
            throw e;
        } catch (NoSuchAlgorithmException e2) {
        } catch (InvalidKeySpecException e3) {
        } catch (NoSuchPaddingException e4) {
        } catch (Exception e5) {
        }
    }

    @Override // org.bouncycastle.cms.KeyAgreeRecipient
    public AlgorithmIdentifier getPrivateKeyAlgorithmIdentifier() {
        if (this.privKeyAlgID == null) {
            this.privKeyAlgID = PrivateKeyInfo.getInstance(this.recipientKey.getEncoded()).getPrivateKeyAlgorithm();
        }
        return this.privKeyAlgID;
    }

    public JceKeyAgreeRecipient setContentProvider(String str) {
        this.contentHelper = CMSUtils.createContentHelper(str);
        return this;
    }

    public JceKeyAgreeRecipient setContentProvider(Provider provider) {
        this.contentHelper = CMSUtils.createContentHelper(provider);
        return this;
    }

    public JceKeyAgreeRecipient setPrivateKeyAlgorithmIdentifier(AlgorithmIdentifier algorithmIdentifier) {
        this.privKeyAlgID = algorithmIdentifier;
        return this;
    }

    public JceKeyAgreeRecipient setProvider(String str) {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceExtHelper(str));
        this.contentHelper = this.helper;
        return this;
    }

    public JceKeyAgreeRecipient setProvider(Provider provider) {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceExtHelper(provider));
        this.contentHelper = this.helper;
        return this;
    }

    /* access modifiers changed from: protected */
    public Key unwrapSessionKey(ASN1ObjectIdentifier aSN1ObjectIdentifier, SecretKey secretKey, ASN1ObjectIdentifier aSN1ObjectIdentifier2, byte[] bArr) throws CMSException, InvalidKeyException, NoSuchAlgorithmException {
        Cipher createCipher = this.helper.createCipher(aSN1ObjectIdentifier);
        createCipher.init(4, secretKey);
        return createCipher.unwrap(bArr, this.helper.getBaseCipherName(aSN1ObjectIdentifier2), 3);
    }
}
