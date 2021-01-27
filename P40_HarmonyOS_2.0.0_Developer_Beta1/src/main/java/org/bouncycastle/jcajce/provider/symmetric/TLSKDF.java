package org.bouncycastle.jcajce.provider.symmetric;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.symmetric.util.BaseSecretKeyFactory;
import org.bouncycastle.jcajce.provider.util.AlgorithmProvider;
import org.bouncycastle.jcajce.spec.TLSKeyMaterialSpec;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public class TLSKDF {

    public static class Mappings extends AlgorithmProvider {
        private static final String PREFIX = TLSKDF.class.getName();

        @Override // org.bouncycastle.jcajce.provider.util.AlgorithmProvider
        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("SecretKeyFactory.TLS10KDF", PREFIX + "$TLS10");
            configurableProvider.addAlgorithm("SecretKeyFactory.TLS11KDF", PREFIX + "$TLS11");
            configurableProvider.addAlgorithm("SecretKeyFactory.TLS12WITHSHA256KDF", PREFIX + "$TLS12withSHA256");
            configurableProvider.addAlgorithm("SecretKeyFactory.TLS12WITHSHA384KDF", PREFIX + "$TLS12withSHA384");
            configurableProvider.addAlgorithm("SecretKeyFactory.TLS12WITHSHA512KDF", PREFIX + "$TLS12withSHA512");
        }
    }

    public static final class TLS10 extends TLSKeyMaterialFactory {
        public TLS10() {
            super("TLS10KDF");
        }

        /* access modifiers changed from: protected */
        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseSecretKeyFactory, javax.crypto.SecretKeyFactorySpi
        public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
            if (keySpec instanceof TLSKeyMaterialSpec) {
                return new SecretKeySpec(TLSKDF.PRF_legacy((TLSKeyMaterialSpec) keySpec), this.algName);
            }
            throw new InvalidKeySpecException("Invalid KeySpec");
        }
    }

    public static final class TLS11 extends TLSKeyMaterialFactory {
        public TLS11() {
            super("TLS11KDF");
        }

        /* access modifiers changed from: protected */
        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseSecretKeyFactory, javax.crypto.SecretKeyFactorySpi
        public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
            if (keySpec instanceof TLSKeyMaterialSpec) {
                return new SecretKeySpec(TLSKDF.PRF_legacy((TLSKeyMaterialSpec) keySpec), this.algName);
            }
            throw new InvalidKeySpecException("Invalid KeySpec");
        }
    }

    public static class TLS12 extends TLSKeyMaterialFactory {
        private final Mac prf;

        protected TLS12(String str, Mac mac) {
            super(str);
            this.prf = mac;
        }

        private byte[] PRF(TLSKeyMaterialSpec tLSKeyMaterialSpec, Mac mac) {
            byte[] concatenate = Arrays.concatenate(Strings.toByteArray(tLSKeyMaterialSpec.getLabel()), tLSKeyMaterialSpec.getSeed());
            byte[] secret = tLSKeyMaterialSpec.getSecret();
            byte[] bArr = new byte[tLSKeyMaterialSpec.getLength()];
            TLSKDF.hmac_hash(mac, secret, concatenate, bArr);
            return bArr;
        }

        /* access modifiers changed from: protected */
        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseSecretKeyFactory, javax.crypto.SecretKeyFactorySpi
        public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
            if (keySpec instanceof TLSKeyMaterialSpec) {
                return new SecretKeySpec(PRF((TLSKeyMaterialSpec) keySpec, this.prf), this.algName);
            }
            throw new InvalidKeySpecException("Invalid KeySpec");
        }
    }

    public static final class TLS12withSHA256 extends TLS12 {
        public TLS12withSHA256() {
            super("TLS12withSHA256KDF", new HMac(new SHA256Digest()));
        }
    }

    public static final class TLS12withSHA384 extends TLS12 {
        public TLS12withSHA384() {
            super("TLS12withSHA384KDF", new HMac(new SHA384Digest()));
        }
    }

    public static final class TLS12withSHA512 extends TLS12 {
        public TLS12withSHA512() {
            super("TLS12withSHA512KDF", new HMac(new SHA512Digest()));
        }
    }

    public static class TLSKeyMaterialFactory extends BaseSecretKeyFactory {
        protected TLSKeyMaterialFactory(String str) {
            super(str, null);
        }
    }

    /* access modifiers changed from: private */
    public static byte[] PRF_legacy(TLSKeyMaterialSpec tLSKeyMaterialSpec) {
        HMac hMac = new HMac(DigestFactory.createMD5());
        HMac hMac2 = new HMac(DigestFactory.createSHA1());
        byte[] concatenate = Arrays.concatenate(Strings.toByteArray(tLSKeyMaterialSpec.getLabel()), tLSKeyMaterialSpec.getSeed());
        byte[] secret = tLSKeyMaterialSpec.getSecret();
        int length = (secret.length + 1) / 2;
        byte[] bArr = new byte[length];
        byte[] bArr2 = new byte[length];
        System.arraycopy(secret, 0, bArr, 0, length);
        System.arraycopy(secret, secret.length - length, bArr2, 0, length);
        int length2 = tLSKeyMaterialSpec.getLength();
        byte[] bArr3 = new byte[length2];
        byte[] bArr4 = new byte[length2];
        hmac_hash(hMac, bArr, concatenate, bArr3);
        hmac_hash(hMac2, bArr2, concatenate, bArr4);
        for (int i = 0; i < length2; i++) {
            bArr3[i] = (byte) (bArr3[i] ^ bArr4[i]);
        }
        return bArr3;
    }

    /* access modifiers changed from: private */
    public static void hmac_hash(Mac mac, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        mac.init(new KeyParameter(bArr));
        int macSize = mac.getMacSize();
        int length = ((bArr3.length + macSize) - 1) / macSize;
        byte[] bArr4 = new byte[mac.getMacSize()];
        byte[] bArr5 = new byte[mac.getMacSize()];
        byte[] bArr6 = bArr2;
        int i = 0;
        while (i < length) {
            mac.update(bArr6, 0, bArr6.length);
            mac.doFinal(bArr4, 0);
            mac.update(bArr4, 0, bArr4.length);
            mac.update(bArr2, 0, bArr2.length);
            mac.doFinal(bArr5, 0);
            int i2 = macSize * i;
            System.arraycopy(bArr5, 0, bArr3, i2, Math.min(macSize, bArr3.length - i2));
            i++;
            bArr6 = bArr4;
        }
    }
}
