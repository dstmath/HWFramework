package org.bouncycastle.openssl.bc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
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
        KEYSIZES.put(PKCSObjectIdentifiers.des_EDE3_CBC.getId(), Integers.valueOf(192));
        KEYSIZES.put(NISTObjectIdentifiers.id_aes128_CBC.getId(), Integers.valueOf(128));
        KEYSIZES.put(NISTObjectIdentifiers.id_aes192_CBC.getId(), Integers.valueOf(192));
        KEYSIZES.put(NISTObjectIdentifiers.id_aes256_CBC.getId(), Integers.valueOf(256));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC4.getId(), Integers.valueOf(128));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC4, Integers.valueOf(40));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd2_KeyTripleDES_CBC, Integers.valueOf(128));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, Integers.valueOf(192));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC2_CBC, Integers.valueOf(128));
        KEYSIZES.put(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC, Integers.valueOf(40));
    }

    PEMUtilities() {
    }

    static byte[] crypt(boolean z, byte[] bArr, char[] cArr, String str, byte[] bArr2) throws PEMException {
        byte[] bArr3;
        BlockCipher blockCipher;
        KeyParameter keyParameter;
        BufferedBlockCipher paddedBufferedBlockCipher;
        boolean z2 = z;
        byte[] bArr4 = bArr;
        char[] cArr2 = cArr;
        String str2 = str;
        byte[] bArr5 = bArr2;
        String str3 = "CBC";
        PKCS7Padding pKCS7Padding = new PKCS7Padding();
        if (str2.endsWith("-CFB")) {
            str3 = "CFB";
            pKCS7Padding = null;
        }
        if (str2.endsWith("-ECB") || "DES-EDE".equals(str2) || "DES-EDE3".equals(str2)) {
            str3 = "ECB";
            bArr3 = null;
        } else {
            bArr3 = bArr5;
        }
        if (str2.endsWith("-OFB")) {
            str3 = "OFB";
            pKCS7Padding = null;
        }
        if (str2.startsWith("DES-EDE")) {
            keyParameter = getKey(cArr2, 24, bArr5, !str2.startsWith("DES-EDE3"));
            blockCipher = new DESedeEngine();
        } else if (str2.startsWith("DES-")) {
            keyParameter = getKey(cArr2, 8, bArr5);
            blockCipher = new DESEngine();
        } else if (str2.startsWith("BF-")) {
            keyParameter = getKey(cArr2, 16, bArr5);
            blockCipher = new BlowfishEngine();
        } else {
            int i = 128;
            if (str2.startsWith("RC2-")) {
                if (str2.startsWith("RC2-40-")) {
                    i = 40;
                } else if (str2.startsWith("RC2-64-")) {
                    i = 64;
                }
                RC2Parameters rC2Parameters = new RC2Parameters(getKey(cArr2, i / 8, bArr5).getKey(), i);
                blockCipher = new RC2Engine();
                keyParameter = rC2Parameters;
            } else if (str2.startsWith("AES-")) {
                if (bArr5.length > 8) {
                    byte[] bArr6 = new byte[8];
                    System.arraycopy(bArr5, 0, bArr6, 0, 8);
                    bArr5 = bArr6;
                }
                if (!str2.startsWith("AES-128-")) {
                    if (str2.startsWith("AES-192-")) {
                        i = 192;
                    } else if (str2.startsWith("AES-256-")) {
                        i = 256;
                    } else {
                        throw new EncryptionException("unknown AES encryption with private key: " + str2);
                    }
                }
                keyParameter = getKey(cArr2, i / 8, bArr5);
                blockCipher = new AESEngine();
            } else {
                throw new EncryptionException("unknown encryption with private key: " + str2);
            }
        }
        BlockCipher cBCBlockCipher = str3.equals("CBC") ? new CBCBlockCipher(blockCipher) : str3.equals("CFB") ? new CFBBlockCipher(blockCipher, blockCipher.getBlockSize() * 8) : str3.equals("OFB") ? new OFBBlockCipher(blockCipher, blockCipher.getBlockSize() * 8) : blockCipher;
        if (pKCS7Padding == null) {
            try {
                paddedBufferedBlockCipher = new BufferedBlockCipher(cBCBlockCipher);
            } catch (Exception e) {
                throw new EncryptionException("exception using cipher - please check password and data.", e);
            }
        } else {
            paddedBufferedBlockCipher = new PaddedBufferedBlockCipher(cBCBlockCipher, pKCS7Padding);
        }
        BufferedBlockCipher bufferedBlockCipher = paddedBufferedBlockCipher;
        if (bArr3 == null) {
            bufferedBlockCipher.init(z2, keyParameter);
        } else {
            bufferedBlockCipher.init(z2, new ParametersWithIV(keyParameter, bArr3));
        }
        byte[] bArr7 = new byte[bufferedBlockCipher.getOutputSize(bArr4.length)];
        int processBytes = bufferedBlockCipher.processBytes(bArr4, 0, bArr4.length, bArr7, 0);
        int doFinal = processBytes + bufferedBlockCipher.doFinal(bArr7, processBytes);
        if (doFinal == bArr7.length) {
            return bArr7;
        }
        byte[] bArr8 = new byte[doFinal];
        System.arraycopy(bArr7, 0, bArr8, 0, doFinal);
        return bArr8;
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
