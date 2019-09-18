package org.bouncycastle.operator.jcajce;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.Provider;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.SymmetricKeyWrapper;

public class JceSymmetricKeyWrapper extends SymmetricKeyWrapper {
    private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());
    private SecureRandom random;
    private SecretKey wrappingKey;

    public JceSymmetricKeyWrapper(SecretKey secretKey) {
        super(determineKeyEncAlg(secretKey));
        this.wrappingKey = secretKey;
    }

    static AlgorithmIdentifier determineKeyEncAlg(String str, int i) {
        ASN1ObjectIdentifier aSN1ObjectIdentifier;
        ASN1ObjectIdentifier aSN1ObjectIdentifier2;
        if (str.startsWith("DES") || str.startsWith("TripleDES")) {
            return new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_CMS3DESwrap, DERNull.INSTANCE);
        }
        if (str.startsWith("RC2")) {
            return new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.3.7"), new ASN1Integer(58));
        }
        if (str.startsWith("AES")) {
            if (i == 128) {
                aSN1ObjectIdentifier2 = NISTObjectIdentifiers.id_aes128_wrap;
            } else if (i == 192) {
                aSN1ObjectIdentifier2 = NISTObjectIdentifiers.id_aes192_wrap;
            } else if (i == 256) {
                aSN1ObjectIdentifier2 = NISTObjectIdentifiers.id_aes256_wrap;
            } else {
                throw new IllegalArgumentException("illegal keysize in AES");
            }
            return new AlgorithmIdentifier(aSN1ObjectIdentifier2);
        } else if (str.startsWith("SEED")) {
            return new AlgorithmIdentifier(KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap);
        } else {
            if (str.startsWith("Camellia")) {
                if (i == 128) {
                    aSN1ObjectIdentifier = NTTObjectIdentifiers.id_camellia128_wrap;
                } else if (i == 192) {
                    aSN1ObjectIdentifier = NTTObjectIdentifiers.id_camellia192_wrap;
                } else if (i == 256) {
                    aSN1ObjectIdentifier = NTTObjectIdentifiers.id_camellia256_wrap;
                } else {
                    throw new IllegalArgumentException("illegal keysize in Camellia");
                }
                return new AlgorithmIdentifier(aSN1ObjectIdentifier);
            }
            throw new IllegalArgumentException("unknown algorithm");
        }
    }

    private static AlgorithmIdentifier determineKeyEncAlg(SecretKey secretKey) {
        return determineKeyEncAlg(secretKey.getAlgorithm(), secretKey.getEncoded().length * 8);
    }

    public byte[] generateWrappedKey(GenericKey genericKey) throws OperatorException {
        Key jceKey = OperatorUtils.getJceKey(genericKey);
        Cipher createSymmetricWrapper = this.helper.createSymmetricWrapper(getAlgorithmIdentifier().getAlgorithm());
        try {
            createSymmetricWrapper.init(3, this.wrappingKey, this.random);
            return createSymmetricWrapper.wrap(jceKey);
        } catch (GeneralSecurityException e) {
            throw new OperatorException("cannot wrap key: " + e.getMessage(), e);
        }
    }

    public JceSymmetricKeyWrapper setProvider(String str) {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(str));
        return this;
    }

    public JceSymmetricKeyWrapper setProvider(Provider provider) {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));
        return this;
    }

    public JceSymmetricKeyWrapper setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
