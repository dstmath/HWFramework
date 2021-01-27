package org.bouncycastle.crypto.util;

import java.security.SecureRandom;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.eac.CertificateHolderAuthorization;
import org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.CipherKeyGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.generators.DESKeyGenerator;
import org.bouncycastle.crypto.generators.DESedeKeyGenerator;

public class CipherKeyGeneratorFactory {
    private CipherKeyGeneratorFactory() {
    }

    private static CipherKeyGenerator createCipherKeyGenerator(SecureRandom secureRandom, int i) {
        CipherKeyGenerator cipherKeyGenerator = new CipherKeyGenerator();
        cipherKeyGenerator.init(new KeyGenerationParameters(secureRandom, i));
        return cipherKeyGenerator;
    }

    public static CipherKeyGenerator createKeyGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier, SecureRandom secureRandom) throws IllegalArgumentException {
        if (NISTObjectIdentifiers.id_aes128_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            return createCipherKeyGenerator(secureRandom, 128);
        }
        if (NISTObjectIdentifiers.id_aes192_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            return createCipherKeyGenerator(secureRandom, CertificateHolderAuthorization.CVCA);
        }
        if (NISTObjectIdentifiers.id_aes256_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            return createCipherKeyGenerator(secureRandom, 256);
        }
        if (NISTObjectIdentifiers.id_aes128_GCM.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            return createCipherKeyGenerator(secureRandom, 128);
        }
        if (NISTObjectIdentifiers.id_aes192_GCM.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            return createCipherKeyGenerator(secureRandom, CertificateHolderAuthorization.CVCA);
        }
        if (NISTObjectIdentifiers.id_aes256_GCM.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            return createCipherKeyGenerator(secureRandom, 256);
        }
        if (PKCSObjectIdentifiers.des_EDE3_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            DESedeKeyGenerator dESedeKeyGenerator = new DESedeKeyGenerator();
            dESedeKeyGenerator.init(new KeyGenerationParameters(secureRandom, CertificateHolderAuthorization.CVCA));
            return dESedeKeyGenerator;
        } else if (NTTObjectIdentifiers.id_camellia128_cbc.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            return createCipherKeyGenerator(secureRandom, 128);
        } else {
            if (NTTObjectIdentifiers.id_camellia192_cbc.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
                return createCipherKeyGenerator(secureRandom, CertificateHolderAuthorization.CVCA);
            }
            if (NTTObjectIdentifiers.id_camellia256_cbc.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
                return createCipherKeyGenerator(secureRandom, 256);
            }
            if (KISAObjectIdentifiers.id_seedCBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
                return createCipherKeyGenerator(secureRandom, 128);
            }
            if (AlgorithmIdentifierFactory.CAST5_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
                return createCipherKeyGenerator(secureRandom, 128);
            }
            if (OIWObjectIdentifiers.desCBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
                DESKeyGenerator dESKeyGenerator = new DESKeyGenerator();
                dESKeyGenerator.init(new KeyGenerationParameters(secureRandom, 64));
                return dESKeyGenerator;
            } else if (PKCSObjectIdentifiers.rc4.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
                return createCipherKeyGenerator(secureRandom, 128);
            } else {
                if (PKCSObjectIdentifiers.RC2_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
                    return createCipherKeyGenerator(secureRandom, 128);
                }
                throw new IllegalArgumentException("cannot recognise cipher: " + aSN1ObjectIdentifier);
            }
        }
    }
}
