package org.bouncycastle.crypto.util;

import java.io.OutputStream;
import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.cms.GCMParameters;
import org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import org.bouncycastle.asn1.misc.CAST5CBCParameters;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RC2CBCParameter;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.CAST5Engine;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.RC2Parameters;

public class CipherFactory {
    private static final short[] rc2Ekb = {93, 190, 155, 139, 17, 153, 110, 77, 89, 243, 133, 166, 63, 183, 131, 197, 228, 115, 107, 58, 104, 90, 192, 71, 160, 100, 52, 12, 241, 208, 82, 165, 185, 30, 150, 67, 65, 216, 212, 44, 219, 248, 7, 119, 42, 202, 235, 239, 16, 28, 22, 13, 56, 114, 47, 137, 193, 249, 128, 196, 109, 174, 48, 61, 206, 32, 99, 254, 230, 26, 199, 184, 80, 232, 36, 23, 252, 37, 111, 187, 106, 163, 68, 83, 217, 162, 1, 171, 188, 182, 31, 152, 238, 154, 167, 45, 79, 158, 142, 172, 224, 198, 73, 70, 41, 244, 148, 138, 175, 225, 91, 195, 179, 123, 87, 209, 124, 156, 237, 135, 64, 140, 226, 203, 147, 20, 201, 97, 46, 229, 204, 246, 94, 168, 92, 214, 117, 141, 98, 149, 88, 105, 118, 161, 74, 181, 85, 9, 120, 51, 130, 215, 221, 121, 245, 27, 11, 222, 38, 33, 40, 116, 4, 151, 86, 223, 60, 240, 55, 57, 220, 255, 6, 164, 234, 66, 8, 218, 180, 113, 176, 207, 18, 122, 78, 250, 108, 29, 132, 0, 200, 127, 145, 69, 170, 43, 194, 177, 143, 213, 186, 242, 173, 25, 178, 103, 54, 247, 15, 10, 146, 125, 227, 157, 233, 144, 62, 35, 39, 102, 19, 236, 129, 21, 189, 34, 191, 159, 126, 169, 81, 75, 76, 251, 2, 211, 112, 134, 49, 231, 59, 5, 3, 84, 96, 72, 101, 24, 210, 205, 95, 50, 136, 14, 53, 253};

    private static AEADBlockCipher createAEADCipher(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        if (NISTObjectIdentifiers.id_aes128_GCM.equals((ASN1Primitive) aSN1ObjectIdentifier) || NISTObjectIdentifiers.id_aes192_GCM.equals((ASN1Primitive) aSN1ObjectIdentifier) || NISTObjectIdentifiers.id_aes256_GCM.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            return new GCMBlockCipher(new AESEngine());
        }
        throw new IllegalArgumentException("cannot recognise cipher: " + aSN1ObjectIdentifier);
    }

    private static BufferedBlockCipher createCipher(ASN1ObjectIdentifier aSN1ObjectIdentifier) throws IllegalArgumentException {
        CBCBlockCipher cBCBlockCipher;
        if (NISTObjectIdentifiers.id_aes128_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier) || NISTObjectIdentifiers.id_aes192_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier) || NISTObjectIdentifiers.id_aes256_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            cBCBlockCipher = new CBCBlockCipher(new AESEngine());
        } else if (PKCSObjectIdentifiers.des_EDE3_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            cBCBlockCipher = new CBCBlockCipher(new DESedeEngine());
        } else if (OIWObjectIdentifiers.desCBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            cBCBlockCipher = new CBCBlockCipher(new DESEngine());
        } else if (PKCSObjectIdentifiers.RC2_CBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            cBCBlockCipher = new CBCBlockCipher(new RC2Engine());
        } else if (MiscObjectIdentifiers.cast5CBC.equals((ASN1Primitive) aSN1ObjectIdentifier)) {
            cBCBlockCipher = new CBCBlockCipher(new CAST5Engine());
        } else {
            throw new IllegalArgumentException("cannot recognise cipher: " + aSN1ObjectIdentifier);
        }
        return new PaddedBufferedBlockCipher(cBCBlockCipher, new PKCS7Padding());
    }

    public static Object createContentCipher(boolean z, CipherParameters cipherParameters, AlgorithmIdentifier algorithmIdentifier) throws IllegalArgumentException {
        ParametersWithIV parametersWithIV;
        ASN1ObjectIdentifier algorithm = algorithmIdentifier.getAlgorithm();
        if (algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.rc4)) {
            RC4Engine rC4Engine = new RC4Engine();
            rC4Engine.init(z, cipherParameters);
            return rC4Engine;
        } else if (algorithm.equals((ASN1Primitive) NISTObjectIdentifiers.id_aes128_GCM) || algorithm.equals((ASN1Primitive) NISTObjectIdentifiers.id_aes192_GCM) || algorithm.equals((ASN1Primitive) NISTObjectIdentifiers.id_aes256_GCM)) {
            AEADBlockCipher createAEADCipher = createAEADCipher(algorithmIdentifier.getAlgorithm());
            GCMParameters instance = GCMParameters.getInstance(algorithmIdentifier.getParameters());
            if (cipherParameters instanceof KeyParameter) {
                createAEADCipher.init(z, new AEADParameters((KeyParameter) cipherParameters, instance.getIcvLen() * 8, instance.getNonce()));
                return createAEADCipher;
            }
            throw new IllegalArgumentException("key data must be accessible for GCM operation");
        } else {
            BufferedBlockCipher createCipher = createCipher(algorithmIdentifier.getAlgorithm());
            ASN1Primitive aSN1Primitive = algorithmIdentifier.getParameters().toASN1Primitive();
            if (aSN1Primitive != null && !(aSN1Primitive instanceof ASN1Null)) {
                if (algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.des_EDE3_CBC) || algorithm.equals((ASN1Primitive) AlgorithmIdentifierFactory.IDEA_CBC) || algorithm.equals((ASN1Primitive) NISTObjectIdentifiers.id_aes128_CBC) || algorithm.equals((ASN1Primitive) NISTObjectIdentifiers.id_aes192_CBC) || algorithm.equals((ASN1Primitive) NISTObjectIdentifiers.id_aes256_CBC) || algorithm.equals((ASN1Primitive) NTTObjectIdentifiers.id_camellia128_cbc) || algorithm.equals((ASN1Primitive) NTTObjectIdentifiers.id_camellia192_cbc) || algorithm.equals((ASN1Primitive) NTTObjectIdentifiers.id_camellia256_cbc) || algorithm.equals((ASN1Primitive) KISAObjectIdentifiers.id_seedCBC) || algorithm.equals((ASN1Primitive) OIWObjectIdentifiers.desCBC)) {
                    parametersWithIV = new ParametersWithIV(cipherParameters, ASN1OctetString.getInstance(aSN1Primitive).getOctets());
                } else if (algorithm.equals((ASN1Primitive) AlgorithmIdentifierFactory.CAST5_CBC)) {
                    parametersWithIV = new ParametersWithIV(cipherParameters, CAST5CBCParameters.getInstance(aSN1Primitive).getIV());
                } else if (algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.RC2_CBC)) {
                    RC2CBCParameter instance2 = RC2CBCParameter.getInstance(aSN1Primitive);
                    parametersWithIV = new ParametersWithIV(new RC2Parameters(((KeyParameter) cipherParameters).getKey(), rc2Ekb[instance2.getRC2ParameterVersion().intValue()]), instance2.getIV());
                } else {
                    throw new IllegalArgumentException("cannot match parameters");
                }
                createCipher.init(z, parametersWithIV);
            } else if (algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.des_EDE3_CBC) || algorithm.equals((ASN1Primitive) AlgorithmIdentifierFactory.IDEA_CBC) || algorithm.equals((ASN1Primitive) AlgorithmIdentifierFactory.CAST5_CBC)) {
                createCipher.init(z, new ParametersWithIV(cipherParameters, new byte[8]));
            } else {
                createCipher.init(z, cipherParameters);
            }
            return createCipher;
        }
    }

    public static CipherOutputStream createOutputStream(OutputStream outputStream, Object obj) {
        if (obj instanceof BufferedBlockCipher) {
            return new CipherOutputStream(outputStream, (BufferedBlockCipher) obj);
        }
        if (obj instanceof StreamCipher) {
            return new CipherOutputStream(outputStream, (StreamCipher) obj);
        }
        if (obj instanceof AEADBlockCipher) {
            return new CipherOutputStream(outputStream, (AEADBlockCipher) obj);
        }
        throw new IllegalArgumentException("unknown cipher object: " + obj);
    }
}
