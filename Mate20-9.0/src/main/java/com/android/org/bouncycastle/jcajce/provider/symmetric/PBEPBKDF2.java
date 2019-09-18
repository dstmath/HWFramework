package com.android.org.bouncycastle.jcajce.provider.symmetric;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BCPBEKey;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseSecretKeyFactory;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE;
import com.android.org.bouncycastle.jcajce.provider.util.AlgorithmProvider;
import com.android.org.bouncycastle.jcajce.spec.PBKDF2KeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;

public class PBEPBKDF2 {

    public static class BasePBKDF2 extends BaseSecretKeyFactory {
        private int defaultDigest;
        private int ivSizeInBits;
        private int keySizeInBits;
        private int scheme;

        public BasePBKDF2(String name, int scheme2) {
            this(name, scheme2, 1);
        }

        private BasePBKDF2(String name, int scheme2, int digest, int keySizeInBits2, int ivSizeInBits2) {
            super(name, PKCSObjectIdentifiers.id_PBKDF2);
            this.scheme = scheme2;
            this.keySizeInBits = keySizeInBits2;
            this.ivSizeInBits = ivSizeInBits2;
            this.defaultDigest = digest;
        }

        private BasePBKDF2(String name, int scheme2, int digest) {
            this(name, scheme2, digest, 0, 0);
        }

        /* access modifiers changed from: protected */
        public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
            KeySpec keySpec2 = keySpec;
            if (keySpec2 instanceof PBEKeySpec) {
                PBEKeySpec pbeSpec = (PBEKeySpec) keySpec2;
                if (pbeSpec.getSalt() == null && pbeSpec.getIterationCount() == 0 && pbeSpec.getKeyLength() == 0 && pbeSpec.getPassword().length > 0 && this.keySizeInBits != 0) {
                    BCPBEKey bCPBEKey = new BCPBEKey(this.algName, this.algOid, this.scheme, this.defaultDigest, this.keySizeInBits, this.ivSizeInBits, pbeSpec, null);
                    return bCPBEKey;
                } else if (pbeSpec.getSalt() == null) {
                    throw new InvalidKeySpecException("missing required salt");
                } else if (pbeSpec.getIterationCount() <= 0) {
                    throw new InvalidKeySpecException("positive iteration count required: " + pbeSpec.getIterationCount());
                } else if (pbeSpec.getKeyLength() <= 0) {
                    throw new InvalidKeySpecException("positive key length required: " + pbeSpec.getKeyLength());
                } else if (pbeSpec.getPassword().length == 0) {
                    throw new IllegalArgumentException("password empty");
                } else if (pbeSpec instanceof PBKDF2KeySpec) {
                    int digest = getDigestCode(((PBKDF2KeySpec) pbeSpec).getPrf().getAlgorithm());
                    int keySize = pbeSpec.getKeyLength();
                    BCPBEKey bCPBEKey2 = new BCPBEKey(this.algName, this.algOid, this.scheme, digest, keySize, -1, pbeSpec, PBE.Util.makePBEMacParameters(pbeSpec, this.scheme, digest, keySize));
                    return bCPBEKey2;
                } else {
                    int digest2 = this.defaultDigest;
                    int keySize2 = pbeSpec.getKeyLength();
                    BCPBEKey bCPBEKey3 = new BCPBEKey(this.algName, this.algOid, this.scheme, digest2, keySize2, -1, pbeSpec, PBE.Util.makePBEMacParameters(pbeSpec, this.scheme, digest2, keySize2));
                    return bCPBEKey3;
                }
            } else {
                throw new InvalidKeySpecException("Invalid KeySpec");
            }
        }

        private int getDigestCode(ASN1ObjectIdentifier algorithm) throws InvalidKeySpecException {
            if (algorithm.equals(PKCSObjectIdentifiers.id_hmacWithSHA1)) {
                return 1;
            }
            if (algorithm.equals(PKCSObjectIdentifiers.id_hmacWithSHA256)) {
                return 4;
            }
            if (algorithm.equals(PKCSObjectIdentifiers.id_hmacWithSHA224)) {
                return 7;
            }
            if (algorithm.equals(PKCSObjectIdentifiers.id_hmacWithSHA384)) {
                return 8;
            }
            if (algorithm.equals(PKCSObjectIdentifiers.id_hmacWithSHA512)) {
                return 9;
            }
            throw new InvalidKeySpecException("Invalid KeySpec: unknown PRF algorithm " + algorithm);
        }
    }

    public static class BasePBKDF2WithHmacSHA1 extends BasePBKDF2 {
        public BasePBKDF2WithHmacSHA1(String name, int scheme) {
            super(name, scheme, 1);
        }
    }

    public static class BasePBKDF2WithHmacSHA224 extends BasePBKDF2 {
        public BasePBKDF2WithHmacSHA224(String name, int scheme) {
            super(name, scheme, 7);
        }
    }

    public static class BasePBKDF2WithHmacSHA256 extends BasePBKDF2 {
        public BasePBKDF2WithHmacSHA256(String name, int scheme) {
            super(name, scheme, 4);
        }
    }

    public static class BasePBKDF2WithHmacSHA384 extends BasePBKDF2 {
        public BasePBKDF2WithHmacSHA384(String name, int scheme) {
            super(name, scheme, 8);
        }
    }

    public static class BasePBKDF2WithHmacSHA512 extends BasePBKDF2 {
        public BasePBKDF2WithHmacSHA512(String name, int scheme) {
            super(name, scheme, 9);
        }
    }

    public static class Mappings extends AlgorithmProvider {
        private static final String PREFIX = PBEPBKDF2.class.getName();

        public void configure(ConfigurableProvider provider) {
            provider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBKDF2WithHmacSHA1AndUTF8", "PBKDF2WithHmacSHA1");
            provider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBKDF2with8BIT", "PBKDF2WithHmacSHA1And8BIT");
            provider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBKDF2withASCII", "PBKDF2WithHmacSHA1And8BIT");
            provider.addAlgorithm("SecretKeyFactory.PBKDF2WithHmacSHA1", PREFIX + "$PBKDF2WithHmacSHA1UTF8");
            provider.addAlgorithm("SecretKeyFactory.PBKDF2WithHmacSHA224", PREFIX + "$PBKDF2WithHmacSHA224UTF8");
            provider.addAlgorithm("SecretKeyFactory.PBKDF2WithHmacSHA256", PREFIX + "$PBKDF2WithHmacSHA256UTF8");
            provider.addAlgorithm("SecretKeyFactory.PBKDF2WithHmacSHA384", PREFIX + "$PBKDF2WithHmacSHA384UTF8");
            provider.addAlgorithm("SecretKeyFactory.PBKDF2WithHmacSHA512", PREFIX + "$PBKDF2WithHmacSHA512UTF8");
            provider.addAlgorithm("SecretKeyFactory.PBEWithHmacSHA1AndAES_128", PREFIX + "$PBEWithHmacSHA1AndAES_128");
            provider.addAlgorithm("SecretKeyFactory.PBEWithHmacSHA224AndAES_128", PREFIX + "$PBEWithHmacSHA224AndAES_128");
            provider.addAlgorithm("SecretKeyFactory.PBEWithHmacSHA256AndAES_128", PREFIX + "$PBEWithHmacSHA256AndAES_128");
            provider.addAlgorithm("SecretKeyFactory.PBEWithHmacSHA384AndAES_128", PREFIX + "$PBEWithHmacSHA384AndAES_128");
            provider.addAlgorithm("SecretKeyFactory.PBEWithHmacSHA512AndAES_128", PREFIX + "$PBEWithHmacSHA512AndAES_128");
            provider.addAlgorithm("SecretKeyFactory.PBEWithHmacSHA1AndAES_256", PREFIX + "$PBEWithHmacSHA1AndAES_256");
            provider.addAlgorithm("SecretKeyFactory.PBEWithHmacSHA224AndAES_256", PREFIX + "$PBEWithHmacSHA224AndAES_256");
            provider.addAlgorithm("SecretKeyFactory.PBEWithHmacSHA256AndAES_256", PREFIX + "$PBEWithHmacSHA256AndAES_256");
            provider.addAlgorithm("SecretKeyFactory.PBEWithHmacSHA384AndAES_256", PREFIX + "$PBEWithHmacSHA384AndAES_256");
            provider.addAlgorithm("SecretKeyFactory.PBEWithHmacSHA512AndAES_256", PREFIX + "$PBEWithHmacSHA512AndAES_256");
            provider.addAlgorithm("SecretKeyFactory.PBKDF2WithHmacSHA1And8BIT", PREFIX + "$PBKDF2WithHmacSHA18BIT");
        }
    }

    public static class PBEWithHmacSHA1AndAES_128 extends BasePBKDF2 {
        public PBEWithHmacSHA1AndAES_128() {
            super("PBEWithHmacSHA1AndAES_128", 5, 1, 128, 128);
        }
    }

    public static class PBEWithHmacSHA1AndAES_256 extends BasePBKDF2 {
        public PBEWithHmacSHA1AndAES_256() {
            super("PBEWithHmacSHA1AndAES_256", 5, 1, 256, 128);
        }
    }

    public static class PBEWithHmacSHA224AndAES_128 extends BasePBKDF2 {
        public PBEWithHmacSHA224AndAES_128() {
            super("PBEWithHmacSHA224AndAES_128", 5, 7, 128, 128);
        }
    }

    public static class PBEWithHmacSHA224AndAES_256 extends BasePBKDF2 {
        public PBEWithHmacSHA224AndAES_256() {
            super("PBEWithHmacSHA224AndAES_256", 5, 7, 256, 128);
        }
    }

    public static class PBEWithHmacSHA256AndAES_128 extends BasePBKDF2 {
        public PBEWithHmacSHA256AndAES_128() {
            super("PBEWithHmacSHA256AndAES_128", 5, 4, 128, 128);
        }
    }

    public static class PBEWithHmacSHA256AndAES_256 extends BasePBKDF2 {
        public PBEWithHmacSHA256AndAES_256() {
            super("PBEWithHmacSHA256AndAES_256", 5, 4, 256, 128);
        }
    }

    public static class PBEWithHmacSHA384AndAES_128 extends BasePBKDF2 {
        public PBEWithHmacSHA384AndAES_128() {
            super("PBEWithHmacSHA384AndAES_128", 5, 8, 128, 128);
        }
    }

    public static class PBEWithHmacSHA384AndAES_256 extends BasePBKDF2 {
        public PBEWithHmacSHA384AndAES_256() {
            super("PBEWithHmacSHA384AndAES_256", 5, 8, 256, 128);
        }
    }

    public static class PBEWithHmacSHA512AndAES_128 extends BasePBKDF2 {
        public PBEWithHmacSHA512AndAES_128() {
            super("PBEWithHmacSHA512AndAES_128", 5, 9, 128, 128);
        }
    }

    public static class PBEWithHmacSHA512AndAES_256 extends BasePBKDF2 {
        public PBEWithHmacSHA512AndAES_256() {
            super("PBEWithHmacSHA512AndAES_256", 5, 9, 256, 128);
        }
    }

    public static class PBKDF2WithHmacSHA18BIT extends BasePBKDF2WithHmacSHA1 {
        public PBKDF2WithHmacSHA18BIT() {
            super("PBKDF2WithHmacSHA1And8bit", 1);
        }
    }

    public static class PBKDF2WithHmacSHA1UTF8 extends BasePBKDF2WithHmacSHA1 {
        public PBKDF2WithHmacSHA1UTF8() {
            super("PBKDF2WithHmacSHA1", 5);
        }
    }

    public static class PBKDF2WithHmacSHA224UTF8 extends BasePBKDF2WithHmacSHA224 {
        public PBKDF2WithHmacSHA224UTF8() {
            super("PBKDF2WithHmacSHA224", 5);
        }
    }

    public static class PBKDF2WithHmacSHA256UTF8 extends BasePBKDF2WithHmacSHA256 {
        public PBKDF2WithHmacSHA256UTF8() {
            super("PBKDF2WithHmacSHA256", 5);
        }
    }

    public static class PBKDF2WithHmacSHA384UTF8 extends BasePBKDF2WithHmacSHA384 {
        public PBKDF2WithHmacSHA384UTF8() {
            super("PBKDF2WithHmacSHA384", 5);
        }
    }

    public static class PBKDF2WithHmacSHA512UTF8 extends BasePBKDF2WithHmacSHA512 {
        public PBKDF2WithHmacSHA512UTF8() {
            super("PBKDF2WithHmacSHA512", 5);
        }
    }

    private PBEPBKDF2() {
    }
}
