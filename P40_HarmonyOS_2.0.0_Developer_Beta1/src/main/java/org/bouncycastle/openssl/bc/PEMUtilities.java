package org.bouncycastle.openssl.bc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.eac.CertificateHolderAuthorization;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.generators.OpenSSLPBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.RC2Parameters;
import org.bouncycastle.openssl.EncryptionException;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.util.Integers;

class PEMUtilities {
    private static final Map KEYSIZES = new HashMap();
    private static final Set PKCS5_SCHEME_1 = new HashSet();
    private static final Set PKCS5_SCHEME_2 = new HashSet();

    static {
        PKCS5_SCHEME_1.add(PKCSObjectIdentifiers.pbeWithMD2AndDES_CBC);
        PKCS5_SCHEME_1.add(PKCSObjectIdentifiers.pbeWithMD2AndRC2_CBC);
        PKCS5_SCHEME_1.add(PKCSObjectIdentifiers.pbeWithMD5AndDES_CBC);
        PKCS5_SCHEME_1.add(PKCSObjectIdentifiers.pbeWithMD5AndRC2_CBC);
        PKCS5_SCHEME_1.add(PKCSObjectIdentifiers.pbeWithSHA1AndDES_CBC);
        PKCS5_SCHEME_1.add(PKCSObjectIdentifiers.pbeWithSHA1AndRC2_CBC);
        PKCS5_SCHEME_2.add(PKCSObjectIdentifiers.id_PBES2);
        PKCS5_SCHEME_2.add(PKCSObjectIdentifiers.des_EDE3_CBC);
        PKCS5_SCHEME_2.add(NISTObjectIdentifiers.id_aes128_CBC);
        PKCS5_SCHEME_2.add(NISTObjectIdentifiers.id_aes192_CBC);
        PKCS5_SCHEME_2.add(NISTObjectIdentifiers.id_aes256_CBC);
        KEYSIZES.put(PKCSObjectIdentifiers.des_EDE3_CBC.getId(), Integers.valueOf(CertificateHolderAuthorization.CVCA));
        KEYSIZES.put(NISTObjectIdentifiers.id_aes128_CBC.getId(), Integers.valueOf(128));
        KEYSIZES.put(NISTObjectIdentifiers.id_aes192_CBC.getId(), Integers.valueOf(CertificateHolderAuthorization.CVCA));
        KEYSIZES.put(NISTObjectIdentifiers.id_aes256_CBC.getId(), Integers.valueOf(256));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC4.getId(), Integers.valueOf(128));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC4, Integers.valueOf(40));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd2_KeyTripleDES_CBC, Integers.valueOf(128));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, Integers.valueOf(CertificateHolderAuthorization.CVCA));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC2_CBC, Integers.valueOf(128));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC, Integers.valueOf(40));
    }

    PEMUtilities() {
    }

    static byte[] crypt(boolean z, byte[] bArr, char[] cArr, String str, byte[] bArr2) throws PEMException {
        PKCS7Padding pKCS7Padding;
        String str2;
        byte[] bArr3;
        BlockCipher blockCipher;
        KeyParameter keyParameter;
        BufferedBlockCipher bufferedBlockCipher;
        byte[] bArr4 = bArr2;
        PKCS7Padding pKCS7Padding2 = new PKCS7Padding();
        if (str.endsWith("-CFB")) {
            str2 = "CFB";
            pKCS7Padding = null;
        } else {
            pKCS7Padding = pKCS7Padding2;
            str2 = "CBC";
        }
        if (str.endsWith("-ECB") || "DES-EDE".equals(str) || "DES-EDE3".equals(str)) {
            str2 = "ECB";
            bArr3 = null;
        } else {
            bArr3 = bArr4;
        }
        if (str.endsWith("-OFB")) {
            pKCS7Padding = null;
            str2 = "OFB";
        }
        if (str.startsWith("DES-EDE")) {
            keyParameter = getKey(cArr, 24, bArr4, !str.startsWith("DES-EDE3"));
            blockCipher = new DESedeEngine();
        } else if (str.startsWith("DES-")) {
            keyParameter = getKey(cArr, 8, bArr4);
            blockCipher = new DESEngine();
        } else if (str.startsWith("BF-")) {
            keyParameter = getKey(cArr, 16, bArr4);
            blockCipher = new BlowfishEngine();
        } else {
            int i = 128;
            if (str.startsWith("RC2-")) {
                if (str.startsWith("RC2-40-")) {
                    i = 40;
                } else if (str.startsWith("RC2-64-")) {
                    i = 64;
                }
                RC2Parameters rC2Parameters = new RC2Parameters(getKey(cArr, i / 8, bArr4).getKey(), i);
                blockCipher = new RC2Engine();
                keyParameter = rC2Parameters;
            } else if (str.startsWith("AES-")) {
                if (bArr4.length > 8) {
                    byte[] bArr5 = new byte[8];
                    System.arraycopy(bArr4, 0, bArr5, 0, 8);
                    bArr4 = bArr5;
                }
                if (!str.startsWith("AES-128-")) {
                    if (str.startsWith("AES-192-")) {
                        i = CertificateHolderAuthorization.CVCA;
                    } else if (str.startsWith("AES-256-")) {
                        i = 256;
                    } else {
                        throw new EncryptionException("unknown AES encryption with private key: " + str);
                    }
                }
                keyParameter = getKey(cArr, i / 8, bArr4);
                blockCipher = new AESEngine();
            } else {
                throw new EncryptionException("unknown encryption with private key: " + str);
            }
        }
        BlockCipher cBCBlockCipher = str2.equals("CBC") ? new CBCBlockCipher(blockCipher) : str2.equals("CFB") ? new CFBBlockCipher(blockCipher, blockCipher.getBlockSize() * 8) : str2.equals("OFB") ? new OFBBlockCipher(blockCipher, blockCipher.getBlockSize() * 8) : blockCipher;
        if (pKCS7Padding == null) {
            try {
                bufferedBlockCipher = new BufferedBlockCipher(cBCBlockCipher);
            } catch (Exception e) {
                throw new EncryptionException("exception using cipher - please check password and data.", e);
            }
        } else {
            bufferedBlockCipher = new PaddedBufferedBlockCipher(cBCBlockCipher, pKCS7Padding);
        }
        if (bArr3 == null) {
            bufferedBlockCipher.init(z, keyParameter);
        } else {
            bufferedBlockCipher.init(z, new ParametersWithIV(keyParameter, bArr3));
        }
        byte[] bArr6 = new byte[bufferedBlockCipher.getOutputSize(bArr.length)];
        int processBytes = bufferedBlockCipher.processBytes(bArr, 0, bArr.length, bArr6, 0);
        int doFinal = processBytes + bufferedBlockCipher.doFinal(bArr6, processBytes);
        if (doFinal == bArr6.length) {
            return bArr6;
        }
        byte[] bArr7 = new byte[doFinal];
        System.arraycopy(bArr6, 0, bArr7, 0, doFinal);
        return bArr7;
    }

    public static KeyParameter generateSecretKeyForPKCS5Scheme2(String str, char[] cArr, byte[] bArr, int i) {
        PKCS5S2ParametersGenerator pKCS5S2ParametersGenerator = new PKCS5S2ParametersGenerator(new SHA1Digest());
        pKCS5S2ParametersGenerator.init(PBEParametersGenerator.PKCS5PasswordToBytes(cArr), bArr, i);
        return (KeyParameter) pKCS5S2ParametersGenerator.generateDerivedParameters(getKeySize(str));
    }

    private static KeyParameter getKey(char[] cArr, int i, byte[] bArr) throws PEMException {
        return getKey(cArr, i, bArr, false);
    }

    private static KeyParameter getKey(char[] cArr, int i, byte[] bArr, boolean z) throws PEMException {
        OpenSSLPBEParametersGenerator openSSLPBEParametersGenerator = new OpenSSLPBEParametersGenerator();
        openSSLPBEParametersGenerator.init(PBEParametersGenerator.PKCS5PasswordToBytes(cArr), bArr, 1);
        KeyParameter keyParameter = (KeyParameter) openSSLPBEParametersGenerator.generateDerivedParameters(i * 8);
        if (!z || keyParameter.getKey().length != 24) {
            return keyParameter;
        }
        byte[] key = keyParameter.getKey();
        System.arraycopy(key, 0, key, 16, 8);
        return new KeyParameter(key);
    }

    static int getKeySize(String str) {
        if (KEYSIZES.containsKey(str)) {
            return ((Integer) KEYSIZES.get(str)).intValue();
        }
        throw new IllegalStateException("no key size for algorithm: " + str);
    }

    public static boolean isPKCS12(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return aSN1ObjectIdentifier.getId().startsWith(PKCSObjectIdentifiers.pkcs_12PbeIds.getId());
    }

    static boolean isPKCS5Scheme1(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return PKCS5_SCHEME_1.contains(aSN1ObjectIdentifier);
    }

    static boolean isPKCS5Scheme2(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return PKCS5_SCHEME_2.contains(aSN1ObjectIdentifier);
    }
}
