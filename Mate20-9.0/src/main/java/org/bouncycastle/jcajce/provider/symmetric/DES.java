package org.bouncycastle.jcajce.provider.symmetric;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.PasswordConverter;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.RFC3211WrapEngine;
import org.bouncycastle.crypto.generators.DESKeyGenerator;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.macs.CFBBlockCipherMac;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.macs.ISO9797Alg3Mac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import org.bouncycastle.crypto.params.DESParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jcajce.PBKDF1Key;
import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseAlgorithmParameterGenerator;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseMac;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseSecretKeyFactory;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher;
import org.bouncycastle.jcajce.provider.symmetric.util.PBE;
import org.bouncycastle.jcajce.provider.util.AlgorithmProvider;

public final class DES {

    public static class AlgParamGen extends BaseAlgorithmParameterGenerator {
        /* access modifiers changed from: protected */
        public AlgorithmParameters engineGenerateParameters() {
            byte[] bArr = new byte[8];
            if (this.random == null) {
                this.random = CryptoServicesRegistrar.getSecureRandom();
            }
            this.random.nextBytes(bArr);
            try {
                AlgorithmParameters createParametersInstance = createParametersInstance("DES");
                createParametersInstance.init(new IvParameterSpec(bArr));
                return createParametersInstance;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        /* access modifiers changed from: protected */
        public void engineInit(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for DES parameter generation.");
        }
    }

    public static class CBC extends BaseBlockCipher {
        public CBC() {
            super((BlockCipher) new CBCBlockCipher(new DESEngine()), 64);
        }
    }

    public static class CBCMAC extends BaseMac {
        public CBCMAC() {
            super(new CBCBlockCipherMac(new DESEngine()));
        }
    }

    public static class CMAC extends BaseMac {
        public CMAC() {
            super(new CMac(new DESEngine()));
        }
    }

    public static class DES64 extends BaseMac {
        public DES64() {
            super(new CBCBlockCipherMac((BlockCipher) new DESEngine(), 64));
        }
    }

    public static class DES64with7816d4 extends BaseMac {
        public DES64with7816d4() {
            super(new CBCBlockCipherMac(new DESEngine(), 64, new ISO7816d4Padding()));
        }
    }

    public static class DES9797Alg3 extends BaseMac {
        public DES9797Alg3() {
            super(new ISO9797Alg3Mac(new DESEngine()));
        }
    }

    public static class DES9797Alg3with7816d4 extends BaseMac {
        public DES9797Alg3with7816d4() {
            super(new ISO9797Alg3Mac((BlockCipher) new DESEngine(), (BlockCipherPadding) new ISO7816d4Padding()));
        }
    }

    public static class DESCFB8 extends BaseMac {
        public DESCFB8() {
            super(new CFBBlockCipherMac(new DESEngine()));
        }
    }

    public static class DESPBEKeyFactory extends BaseSecretKeyFactory {
        private int digest;
        private boolean forCipher;
        private int ivSize;
        private int keySize;
        private int scheme;

        public DESPBEKeyFactory(String str, ASN1ObjectIdentifier aSN1ObjectIdentifier, boolean z, int i, int i2, int i3, int i4) {
            super(str, aSN1ObjectIdentifier);
            this.forCipher = z;
            this.scheme = i;
            this.digest = i2;
            this.keySize = i3;
            this.ivSize = i4;
        }

        /* access modifiers changed from: protected */
        public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
            if (keySpec instanceof PBEKeySpec) {
                PBEKeySpec pBEKeySpec = (PBEKeySpec) keySpec;
                if (pBEKeySpec.getSalt() != null) {
                    CipherParameters makePBEParameters = this.forCipher ? PBE.Util.makePBEParameters(pBEKeySpec, this.scheme, this.digest, this.keySize, this.ivSize) : PBE.Util.makePBEMacParameters(pBEKeySpec, this.scheme, this.digest, this.keySize);
                    DESParameters.setOddParity((makePBEParameters instanceof ParametersWithIV ? (KeyParameter) ((ParametersWithIV) makePBEParameters).getParameters() : (KeyParameter) makePBEParameters).getKey());
                    BCPBEKey bCPBEKey = new BCPBEKey(this.algName, this.algOid, this.scheme, this.digest, this.keySize, this.ivSize, pBEKeySpec, makePBEParameters);
                    return bCPBEKey;
                } else if (this.scheme == 0 || this.scheme == 4) {
                    return new PBKDF1Key(pBEKeySpec.getPassword(), this.scheme == 0 ? PasswordConverter.ASCII : PasswordConverter.UTF8);
                } else {
                    BCPBEKey bCPBEKey2 = new BCPBEKey(this.algName, this.algOid, this.scheme, this.digest, this.keySize, this.ivSize, pBEKeySpec, null);
                    return bCPBEKey2;
                }
            } else {
                throw new InvalidKeySpecException("Invalid KeySpec");
            }
        }
    }

    public static class ECB extends BaseBlockCipher {
        public ECB() {
            super((BlockCipher) new DESEngine());
        }
    }

    public static class KeyFactory extends BaseSecretKeyFactory {
        public KeyFactory() {
            super("DES", null);
        }

        /* access modifiers changed from: protected */
        public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
            return keySpec instanceof DESKeySpec ? new SecretKeySpec(((DESKeySpec) keySpec).getKey(), "DES") : super.engineGenerateSecret(keySpec);
        }

        /* access modifiers changed from: protected */
        public KeySpec engineGetKeySpec(SecretKey secretKey, Class cls) throws InvalidKeySpecException {
            if (cls == null) {
                throw new InvalidKeySpecException("keySpec parameter is null");
            } else if (secretKey == null) {
                throw new InvalidKeySpecException("key parameter is null");
            } else if (SecretKeySpec.class.isAssignableFrom(cls)) {
                return new SecretKeySpec(secretKey.getEncoded(), this.algName);
            } else {
                if (DESKeySpec.class.isAssignableFrom(cls)) {
                    try {
                        return new DESKeySpec(secretKey.getEncoded());
                    } catch (Exception e) {
                        throw new InvalidKeySpecException(e.toString());
                    }
                } else {
                    throw new InvalidKeySpecException("Invalid KeySpec");
                }
            }
        }
    }

    public static class KeyGenerator extends BaseKeyGenerator {
        public KeyGenerator() {
            super("DES", 64, new DESKeyGenerator());
        }

        /* access modifiers changed from: protected */
        public SecretKey engineGenerateKey() {
            if (this.uninitialised) {
                this.engine.init(new KeyGenerationParameters(CryptoServicesRegistrar.getSecureRandom(), this.defaultKeySize));
                this.uninitialised = false;
            }
            return new SecretKeySpec(this.engine.generateKey(), this.algName);
        }

        /* access modifiers changed from: protected */
        public void engineInit(int i, SecureRandom secureRandom) {
            super.engineInit(i, secureRandom);
        }
    }

    public static class Mappings extends AlgorithmProvider {
        private static final String PACKAGE = "org.bouncycastle.jcajce.provider.symmetric";
        private static final String PREFIX = DES.class.getName();

        private void addAlias(ConfigurableProvider configurableProvider, ASN1ObjectIdentifier aSN1ObjectIdentifier, String str) {
            configurableProvider.addAlgorithm("Alg.Alias.KeyGenerator." + aSN1ObjectIdentifier.getId(), str);
            configurableProvider.addAlgorithm("Alg.Alias.KeyFactory." + aSN1ObjectIdentifier.getId(), str);
        }

        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("Cipher.DES", PREFIX + "$ECB");
            ASN1ObjectIdentifier aSN1ObjectIdentifier = OIWObjectIdentifiers.desCBC;
            configurableProvider.addAlgorithm("Cipher", aSN1ObjectIdentifier, PREFIX + "$CBC");
            addAlias(configurableProvider, OIWObjectIdentifiers.desCBC, "DES");
            configurableProvider.addAlgorithm("Cipher.DESRFC3211WRAP", PREFIX + "$RFC3211");
            configurableProvider.addAlgorithm("KeyGenerator.DES", PREFIX + "$KeyGenerator");
            configurableProvider.addAlgorithm("SecretKeyFactory.DES", PREFIX + "$KeyFactory");
            configurableProvider.addAlgorithm("Mac.DESCMAC", PREFIX + "$CMAC");
            configurableProvider.addAlgorithm("Mac.DESMAC", PREFIX + "$CBCMAC");
            configurableProvider.addAlgorithm("Alg.Alias.Mac.DES", "DESMAC");
            configurableProvider.addAlgorithm("Mac.DESMAC/CFB8", PREFIX + "$DESCFB8");
            configurableProvider.addAlgorithm("Alg.Alias.Mac.DES/CFB8", "DESMAC/CFB8");
            configurableProvider.addAlgorithm("Mac.DESMAC64", PREFIX + "$DES64");
            configurableProvider.addAlgorithm("Alg.Alias.Mac.DES64", "DESMAC64");
            configurableProvider.addAlgorithm("Mac.DESMAC64WITHISO7816-4PADDING", PREFIX + "$DES64with7816d4");
            configurableProvider.addAlgorithm("Alg.Alias.Mac.DES64WITHISO7816-4PADDING", "DESMAC64WITHISO7816-4PADDING");
            configurableProvider.addAlgorithm("Alg.Alias.Mac.DESISO9797ALG1MACWITHISO7816-4PADDING", "DESMAC64WITHISO7816-4PADDING");
            configurableProvider.addAlgorithm("Alg.Alias.Mac.DESISO9797ALG1WITHISO7816-4PADDING", "DESMAC64WITHISO7816-4PADDING");
            configurableProvider.addAlgorithm("Mac.DESWITHISO9797", PREFIX + "$DES9797Alg3");
            configurableProvider.addAlgorithm("Alg.Alias.Mac.DESISO9797MAC", "DESWITHISO9797");
            configurableProvider.addAlgorithm("Mac.ISO9797ALG3MAC", PREFIX + "$DES9797Alg3");
            configurableProvider.addAlgorithm("Alg.Alias.Mac.ISO9797ALG3", "ISO9797ALG3MAC");
            configurableProvider.addAlgorithm("Mac.ISO9797ALG3WITHISO7816-4PADDING", PREFIX + "$DES9797Alg3with7816d4");
            configurableProvider.addAlgorithm("Alg.Alias.Mac.ISO9797ALG3MACWITHISO7816-4PADDING", "ISO9797ALG3WITHISO7816-4PADDING");
            configurableProvider.addAlgorithm("AlgorithmParameters.DES", "org.bouncycastle.jcajce.provider.symmetric.util.IvAlgorithmParameters");
            configurableProvider.addAlgorithm("Alg.Alias.AlgorithmParameters", OIWObjectIdentifiers.desCBC, "DES");
            configurableProvider.addAlgorithm("AlgorithmParameterGenerator.DES", PREFIX + "$AlgParamGen");
            configurableProvider.addAlgorithm("Alg.Alias.AlgorithmParameterGenerator." + OIWObjectIdentifiers.desCBC, "DES");
            configurableProvider.addAlgorithm("Cipher.PBEWITHMD2ANDDES", PREFIX + "$PBEWithMD2");
            configurableProvider.addAlgorithm("Cipher.PBEWITHMD5ANDDES", PREFIX + "$PBEWithMD5");
            configurableProvider.addAlgorithm("Cipher.PBEWITHSHA1ANDDES", PREFIX + "$PBEWithSHA1");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher", PKCSObjectIdentifiers.pbeWithMD2AndDES_CBC, "PBEWITHMD2ANDDES");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher", PKCSObjectIdentifiers.pbeWithMD5AndDES_CBC, "PBEWITHMD5ANDDES");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher", PKCSObjectIdentifiers.pbeWithSHA1AndDES_CBC, "PBEWITHSHA1ANDDES");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher.PBEWITHMD2ANDDES-CBC", "PBEWITHMD2ANDDES");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher.PBEWITHMD5ANDDES-CBC", "PBEWITHMD5ANDDES");
            configurableProvider.addAlgorithm("Alg.Alias.Cipher.PBEWITHSHA1ANDDES-CBC", "PBEWITHSHA1ANDDES");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBEWITHMD2ANDDES", PREFIX + "$PBEWithMD2KeyFactory");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBEWITHMD5ANDDES", PREFIX + "$PBEWithMD5KeyFactory");
            configurableProvider.addAlgorithm("SecretKeyFactory.PBEWITHSHA1ANDDES", PREFIX + "$PBEWithSHA1KeyFactory");
            configurableProvider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBEWITHMD2ANDDES-CBC", "PBEWITHMD2ANDDES");
            configurableProvider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBEWITHMD5ANDDES-CBC", "PBEWITHMD5ANDDES");
            configurableProvider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBEWITHSHA1ANDDES-CBC", "PBEWITHSHA1ANDDES");
            configurableProvider.addAlgorithm("Alg.Alias.SecretKeyFactory." + PKCSObjectIdentifiers.pbeWithMD2AndDES_CBC, "PBEWITHMD2ANDDES");
            configurableProvider.addAlgorithm("Alg.Alias.SecretKeyFactory." + PKCSObjectIdentifiers.pbeWithMD5AndDES_CBC, "PBEWITHMD5ANDDES");
            configurableProvider.addAlgorithm("Alg.Alias.SecretKeyFactory." + PKCSObjectIdentifiers.pbeWithSHA1AndDES_CBC, "PBEWITHSHA1ANDDES");
        }
    }

    public static class PBEWithMD2 extends BaseBlockCipher {
        /* JADX WARNING: Illegal instructions before constructor call */
        public PBEWithMD2() {
            super(r1, 0, 5, 64, 8);
            CBCBlockCipher cBCBlockCipher = new CBCBlockCipher(new DESEngine());
        }
    }

    public static class PBEWithMD2KeyFactory extends DESPBEKeyFactory {
        public PBEWithMD2KeyFactory() {
            super("PBEwithMD2andDES", PKCSObjectIdentifiers.pbeWithMD2AndDES_CBC, true, 0, 5, 64, 64);
        }
    }

    public static class PBEWithMD5 extends BaseBlockCipher {
        /* JADX WARNING: Illegal instructions before constructor call */
        public PBEWithMD5() {
            super(r1, 0, 0, 64, 8);
            CBCBlockCipher cBCBlockCipher = new CBCBlockCipher(new DESEngine());
        }
    }

    public static class PBEWithMD5KeyFactory extends DESPBEKeyFactory {
        public PBEWithMD5KeyFactory() {
            super("PBEwithMD5andDES", PKCSObjectIdentifiers.pbeWithMD5AndDES_CBC, true, 0, 0, 64, 64);
        }
    }

    public static class PBEWithSHA1 extends BaseBlockCipher {
        /* JADX WARNING: Illegal instructions before constructor call */
        public PBEWithSHA1() {
            super(r1, 0, 1, 64, 8);
            CBCBlockCipher cBCBlockCipher = new CBCBlockCipher(new DESEngine());
        }
    }

    public static class PBEWithSHA1KeyFactory extends DESPBEKeyFactory {
        public PBEWithSHA1KeyFactory() {
            super("PBEwithSHA1andDES", PKCSObjectIdentifiers.pbeWithSHA1AndDES_CBC, true, 0, 1, 64, 64);
        }
    }

    public static class RFC3211 extends BaseWrapCipher {
        public RFC3211() {
            super(new RFC3211WrapEngine(new DESEngine()), 8);
        }
    }

    private DES() {
    }
}
