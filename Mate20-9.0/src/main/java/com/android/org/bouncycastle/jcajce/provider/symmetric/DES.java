package com.android.org.bouncycastle.jcajce.provider.symmetric;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.KeyGenerationParameters;
import com.android.org.bouncycastle.crypto.engines.DESEngine;
import com.android.org.bouncycastle.crypto.generators.DESKeyGenerator;
import com.android.org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import com.android.org.bouncycastle.crypto.modes.CBCBlockCipher;
import com.android.org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import com.android.org.bouncycastle.crypto.params.DESParameters;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseMac;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseSecretKeyFactory;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE;
import com.android.org.bouncycastle.jcajce.provider.util.AlgorithmProvider;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class DES {

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

    public static class DESPBEKeyFactory extends BaseSecretKeyFactory {
        private int digest;
        private boolean forCipher;
        private int ivSize;
        private int keySize;
        private int scheme;

        public DESPBEKeyFactory(String algorithm, ASN1ObjectIdentifier oid, boolean forCipher2, int scheme2, int digest2, int keySize2, int ivSize2) {
            super(algorithm, oid);
            this.forCipher = forCipher2;
            this.scheme = scheme2;
            this.digest = digest2;
            this.keySize = keySize2;
            this.ivSize = ivSize2;
        }

        /* access modifiers changed from: protected */
        public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
            CipherParameters param;
            KeyParameter kParam;
            if (keySpec instanceof PBEKeySpec) {
                PBEKeySpec pbeSpec = (PBEKeySpec) keySpec;
                if (pbeSpec.getSalt() == null) {
                    BCPBEKey bCPBEKey = new BCPBEKey(this.algName, this.algOid, this.scheme, this.digest, this.keySize, this.ivSize, pbeSpec, null);
                    return bCPBEKey;
                }
                if (this.forCipher) {
                    param = PBE.Util.makePBEParameters(pbeSpec, this.scheme, this.digest, this.keySize, this.ivSize);
                } else {
                    param = PBE.Util.makePBEMacParameters(pbeSpec, this.scheme, this.digest, this.keySize);
                }
                CipherParameters param2 = param;
                if (param2 instanceof ParametersWithIV) {
                    kParam = (KeyParameter) ((ParametersWithIV) param2).getParameters();
                } else {
                    kParam = (KeyParameter) param2;
                }
                DESParameters.setOddParity(kParam.getKey());
                BCPBEKey bCPBEKey2 = new BCPBEKey(this.algName, this.algOid, this.scheme, this.digest, this.keySize, this.ivSize, pbeSpec, param2);
                return bCPBEKey2;
            }
            throw new InvalidKeySpecException("Invalid KeySpec");
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
        public KeySpec engineGetKeySpec(SecretKey key, Class keySpec) throws InvalidKeySpecException {
            if (keySpec == null) {
                throw new InvalidKeySpecException("keySpec parameter is null");
            } else if (key == null) {
                throw new InvalidKeySpecException("key parameter is null");
            } else if (SecretKeySpec.class.isAssignableFrom(keySpec)) {
                return new SecretKeySpec(key.getEncoded(), this.algName);
            } else {
                if (DESKeySpec.class.isAssignableFrom(keySpec)) {
                    try {
                        return new DESKeySpec(key.getEncoded());
                    } catch (Exception e) {
                        throw new InvalidKeySpecException(e.toString());
                    }
                } else {
                    throw new InvalidKeySpecException("Invalid KeySpec");
                }
            }
        }

        /* access modifiers changed from: protected */
        public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
            if (keySpec instanceof DESKeySpec) {
                return new SecretKeySpec(((DESKeySpec) keySpec).getKey(), "DES");
            }
            return super.engineGenerateSecret(keySpec);
        }
    }

    public static class KeyGenerator extends BaseKeyGenerator {
        public KeyGenerator() {
            super("DES", 64, new DESKeyGenerator());
        }

        /* access modifiers changed from: protected */
        public void engineInit(int keySize, SecureRandom random) {
            super.engineInit(keySize, random);
        }

        /* access modifiers changed from: protected */
        public SecretKey engineGenerateKey() {
            if (this.uninitialised) {
                this.engine.init(new KeyGenerationParameters(new SecureRandom(), this.defaultKeySize));
                this.uninitialised = false;
            }
            return new SecretKeySpec(this.engine.generateKey(), this.algName);
        }
    }

    public static class Mappings extends AlgorithmProvider {
        private static final String PACKAGE = "com.android.org.bouncycastle.jcajce.provider.symmetric";
        private static final String PREFIX = DES.class.getName();

        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("Cipher.DES", PREFIX + "$ECB");
            provider.addAlgorithm("KeyGenerator.DES", PREFIX + "$KeyGenerator");
            provider.addAlgorithm("SecretKeyFactory.DES", PREFIX + "$KeyFactory");
            provider.addAlgorithm("AlgorithmParameters.DES", "com.android.org.bouncycastle.jcajce.provider.symmetric.util.IvAlgorithmParameters");
            provider.addAlgorithm("Alg.Alias.AlgorithmParameters", OIWObjectIdentifiers.desCBC, "DES");
            provider.addAlgorithm("Cipher.PBEWITHMD5ANDDES", PREFIX + "$PBEWithMD5");
            provider.addAlgorithm("Cipher.PBEWITHSHA1ANDDES", PREFIX + "$PBEWithSHA1");
            provider.addAlgorithm("Alg.Alias.Cipher", PKCSObjectIdentifiers.pbeWithMD5AndDES_CBC, "PBEWITHMD5ANDDES");
            provider.addAlgorithm("Alg.Alias.Cipher", PKCSObjectIdentifiers.pbeWithSHA1AndDES_CBC, "PBEWITHSHA1ANDDES");
            provider.addAlgorithm("Alg.Alias.Cipher.PBEWITHMD5ANDDES-CBC", "PBEWITHMD5ANDDES");
            provider.addAlgorithm("Alg.Alias.Cipher.PBEWITHSHA1ANDDES-CBC", "PBEWITHSHA1ANDDES");
            provider.addAlgorithm("SecretKeyFactory.PBEWITHMD5ANDDES", PREFIX + "$PBEWithMD5KeyFactory");
            provider.addAlgorithm("SecretKeyFactory.PBEWITHSHA1ANDDES", PREFIX + "$PBEWithSHA1KeyFactory");
            provider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBEWITHMD5ANDDES-CBC", "PBEWITHMD5ANDDES");
            provider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBEWITHSHA1ANDDES-CBC", "PBEWITHSHA1ANDDES");
            provider.addAlgorithm("Alg.Alias.SecretKeyFactory." + PKCSObjectIdentifiers.pbeWithMD5AndDES_CBC, "PBEWITHMD5ANDDES");
            provider.addAlgorithm("Alg.Alias.SecretKeyFactory." + PKCSObjectIdentifiers.pbeWithSHA1AndDES_CBC, "PBEWITHSHA1ANDDES");
        }

        private void addAlias(ConfigurableProvider provider, ASN1ObjectIdentifier oid, String name) {
            provider.addAlgorithm("Alg.Alias.KeyGenerator." + oid.getId(), name);
            provider.addAlgorithm("Alg.Alias.KeyFactory." + oid.getId(), name);
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

    private DES() {
    }
}
