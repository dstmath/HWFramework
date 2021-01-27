package org.bouncycastle.cms.jcajce;

import java.security.Key;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.Gost2814789EncryptedKey;
import org.bouncycastle.asn1.cryptopro.GostR3410KeyTransport;
import org.bouncycastle.asn1.cryptopro.GostR3410TransportParameters;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.KeyTransRecipient;
import org.bouncycastle.jcajce.spec.GOST28147WrapParameterSpec;
import org.bouncycastle.jcajce.spec.UserKeyingMaterialSpec;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JceAsymmetricKeyUnwrapper;
import org.bouncycastle.util.Arrays;

public abstract class JceKeyTransRecipient implements KeyTransRecipient {
    protected EnvelopedDataHelper contentHelper = this.helper;
    protected Map extraMappings = new HashMap();
    protected EnvelopedDataHelper helper = new EnvelopedDataHelper(new DefaultJcaJceExtHelper());
    private PrivateKey recipientKey;
    protected boolean unwrappedKeyMustBeEncodable;
    protected boolean validateKeySize = false;

    public JceKeyTransRecipient(PrivateKey privateKey) {
        this.recipientKey = CMSUtils.cleanPrivateKey(privateKey);
    }

    /* access modifiers changed from: protected */
    public Key extractSecretKey(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CMSException {
        if (CMSUtils.isGOST(algorithmIdentifier.getAlgorithm())) {
            try {
                GostR3410KeyTransport instance = GostR3410KeyTransport.getInstance(bArr);
                GostR3410TransportParameters transportParameters = instance.getTransportParameters();
                PublicKey generatePublic = this.helper.createKeyFactory(algorithmIdentifier.getAlgorithm()).generatePublic(new X509EncodedKeySpec(transportParameters.getEphemeralPublicKey().getEncoded()));
                KeyAgreement createKeyAgreement = this.helper.createKeyAgreement(algorithmIdentifier.getAlgorithm());
                createKeyAgreement.init(this.recipientKey, new UserKeyingMaterialSpec(transportParameters.getUkm()));
                createKeyAgreement.doPhase(generatePublic, true);
                SecretKey generateSecret = createKeyAgreement.generateSecret(CryptoProObjectIdentifiers.id_Gost28147_89_CryptoPro_KeyWrap.getId());
                Cipher createCipher = this.helper.createCipher(CryptoProObjectIdentifiers.id_Gost28147_89_CryptoPro_KeyWrap);
                createCipher.init(4, generateSecret, new GOST28147WrapParameterSpec(transportParameters.getEncryptionParamSet(), transportParameters.getUkm()));
                Gost2814789EncryptedKey sessionEncryptedKey = instance.getSessionEncryptedKey();
                return createCipher.unwrap(Arrays.concatenate(sessionEncryptedKey.getEncryptedKey(), sessionEncryptedKey.getMacKey()), this.helper.getBaseCipherName(algorithmIdentifier2.getAlgorithm()), 3);
            } catch (Exception e) {
                throw new CMSException("exception unwrapping key: " + e.getMessage(), e);
            }
        } else {
            JceAsymmetricKeyUnwrapper mustProduceEncodableUnwrappedKey = this.helper.createAsymmetricUnwrapper(algorithmIdentifier, this.recipientKey).setMustProduceEncodableUnwrappedKey(this.unwrappedKeyMustBeEncodable);
            if (!this.extraMappings.isEmpty()) {
                for (ASN1ObjectIdentifier aSN1ObjectIdentifier : this.extraMappings.keySet()) {
                    mustProduceEncodableUnwrappedKey.setAlgorithmMapping(aSN1ObjectIdentifier, (String) this.extraMappings.get(aSN1ObjectIdentifier));
                }
            }
            try {
                Key jceKey = this.helper.getJceKey(algorithmIdentifier2.getAlgorithm(), mustProduceEncodableUnwrappedKey.generateUnwrappedKey(algorithmIdentifier2, bArr));
                if (this.validateKeySize) {
                    this.helper.keySizeCheck(algorithmIdentifier2, jceKey);
                }
                return jceKey;
            } catch (OperatorException e2) {
                throw new CMSException("exception unwrapping key: " + e2.getMessage(), e2);
            }
        }
    }

    public JceKeyTransRecipient setAlgorithmMapping(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str) {
        this.extraMappings.put(aSN1ObjectIdentifier, str);
        return this;
    }

    public JceKeyTransRecipient setContentProvider(String str) {
        this.contentHelper = CMSUtils.createContentHelper(str);
        return this;
    }

    public JceKeyTransRecipient setContentProvider(Provider provider) {
        this.contentHelper = CMSUtils.createContentHelper(provider);
        return this;
    }

    public JceKeyTransRecipient setKeySizeValidation(boolean z) {
        this.validateKeySize = z;
        return this;
    }

    public JceKeyTransRecipient setMustProduceEncodableUnwrappedKey(boolean z) {
        this.unwrappedKeyMustBeEncodable = z;
        return this;
    }

    public JceKeyTransRecipient setProvider(String str) {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceExtHelper(str));
        this.contentHelper = this.helper;
        return this;
    }

    public JceKeyTransRecipient setProvider(Provider provider) {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceExtHelper(provider));
        this.contentHelper = this.helper;
        return this;
    }
}
