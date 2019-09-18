package org.bouncycastle.jcajce.provider.digest;

import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.crypto.CipherKeyGenerator;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseMac;
import org.bouncycastle.pqc.jcajce.spec.SPHINCS256KeyGenParameterSpec;

public class SHA3 {

    public static class Digest224 extends DigestSHA3 {
        public Digest224() {
            super(224);
        }
    }

    public static class Digest256 extends DigestSHA3 {
        public Digest256() {
            super(256);
        }
    }

    public static class Digest384 extends DigestSHA3 {
        public Digest384() {
            super(384);
        }
    }

    public static class Digest512 extends DigestSHA3 {
        public Digest512() {
            super(512);
        }
    }

    public static class DigestSHA3 extends BCMessageDigest implements Cloneable {
        public DigestSHA3(int i) {
            super(new SHA3Digest(i));
        }

        public Object clone() throws CloneNotSupportedException {
            BCMessageDigest bCMessageDigest = (BCMessageDigest) super.clone();
            bCMessageDigest.digest = new SHA3Digest((SHA3Digest) this.digest);
            return bCMessageDigest;
        }
    }

    public static class HashMac224 extends HashMacSHA3 {
        public HashMac224() {
            super(224);
        }
    }

    public static class HashMac256 extends HashMacSHA3 {
        public HashMac256() {
            super(256);
        }
    }

    public static class HashMac384 extends HashMacSHA3 {
        public HashMac384() {
            super(384);
        }
    }

    public static class HashMac512 extends HashMacSHA3 {
        public HashMac512() {
            super(512);
        }
    }

    public static class HashMacSHA3 extends BaseMac {
        public HashMacSHA3(int i) {
            super(new HMac(new SHA3Digest(i)));
        }
    }

    public static class KeyGenerator224 extends KeyGeneratorSHA3 {
        public KeyGenerator224() {
            super(224);
        }
    }

    public static class KeyGenerator256 extends KeyGeneratorSHA3 {
        public KeyGenerator256() {
            super(256);
        }
    }

    public static class KeyGenerator384 extends KeyGeneratorSHA3 {
        public KeyGenerator384() {
            super(384);
        }
    }

    public static class KeyGenerator512 extends KeyGeneratorSHA3 {
        public KeyGenerator512() {
            super(512);
        }
    }

    public static class KeyGeneratorSHA3 extends BaseKeyGenerator {
        public KeyGeneratorSHA3(int i) {
            super("HMACSHA3-" + i, i, new CipherKeyGenerator());
        }
    }

    public static class Mappings extends DigestAlgorithmProvider {
        private static final String PREFIX = SHA3.class.getName();

        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("MessageDigest.SHA3-224", PREFIX + "$Digest224");
            configurableProvider.addAlgorithm("MessageDigest.SHA3-256", PREFIX + "$Digest256");
            configurableProvider.addAlgorithm("MessageDigest.SHA3-384", PREFIX + "$Digest384");
            configurableProvider.addAlgorithm("MessageDigest.SHA3-512", PREFIX + "$Digest512");
            configurableProvider.addAlgorithm("MessageDigest", NISTObjectIdentifiers.id_sha3_224, PREFIX + "$Digest224");
            configurableProvider.addAlgorithm("MessageDigest", NISTObjectIdentifiers.id_sha3_256, PREFIX + "$Digest256");
            configurableProvider.addAlgorithm("MessageDigest", NISTObjectIdentifiers.id_sha3_384, PREFIX + "$Digest384");
            configurableProvider.addAlgorithm("MessageDigest", NISTObjectIdentifiers.id_sha3_512, PREFIX + "$Digest512");
            addHMACAlgorithm(configurableProvider, "SHA3-224", PREFIX + "$HashMac224", PREFIX + "$KeyGenerator224");
            addHMACAlias(configurableProvider, "SHA3-224", NISTObjectIdentifiers.id_hmacWithSHA3_224);
            addHMACAlgorithm(configurableProvider, SPHINCS256KeyGenParameterSpec.SHA3_256, PREFIX + "$HashMac256", PREFIX + "$KeyGenerator256");
            addHMACAlias(configurableProvider, SPHINCS256KeyGenParameterSpec.SHA3_256, NISTObjectIdentifiers.id_hmacWithSHA3_256);
            addHMACAlgorithm(configurableProvider, "SHA3-384", PREFIX + "$HashMac384", PREFIX + "$KeyGenerator384");
            addHMACAlias(configurableProvider, "SHA3-384", NISTObjectIdentifiers.id_hmacWithSHA3_384);
            addHMACAlgorithm(configurableProvider, "SHA3-512", PREFIX + "$HashMac512", PREFIX + "$KeyGenerator512");
            addHMACAlias(configurableProvider, "SHA3-512", NISTObjectIdentifiers.id_hmacWithSHA3_512);
        }
    }

    private SHA3() {
    }
}
