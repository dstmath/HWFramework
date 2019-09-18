package org.bouncycastle.crypto.util;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.Integers;

public class PBKDF2Config extends PBKDFConfig {
    private static final Map PRFS_SALT = new HashMap();
    public static final AlgorithmIdentifier PRF_SHA1 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA1, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_SHA256 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA256, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_SHA3_256 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_hmacWithSHA3_256, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_SHA3_512 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_hmacWithSHA3_512, DERNull.INSTANCE);
    public static final AlgorithmIdentifier PRF_SHA512 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE);
    private final int iterationCount;
    private final AlgorithmIdentifier prf;
    private final int saltLength;

    public static class Builder {
        /* access modifiers changed from: private */
        public int iterationCount = 1024;
        /* access modifiers changed from: private */
        public AlgorithmIdentifier prf = PBKDF2Config.PRF_SHA1;
        /* access modifiers changed from: private */
        public int saltLength = -1;

        public PBKDF2Config build() {
            return new PBKDF2Config(this);
        }

        public Builder withIterationCount(int i) {
            this.iterationCount = i;
            return this;
        }

        public Builder withPRF(AlgorithmIdentifier algorithmIdentifier) {
            this.prf = algorithmIdentifier;
            return this;
        }

        public Builder withSaltLength(int i) {
            this.saltLength = i;
            return this;
        }
    }

    static {
        PRFS_SALT.put(PKCSObjectIdentifiers.id_hmacWithSHA1, Integers.valueOf(20));
        PRFS_SALT.put(PKCSObjectIdentifiers.id_hmacWithSHA256, Integers.valueOf(32));
        PRFS_SALT.put(PKCSObjectIdentifiers.id_hmacWithSHA512, Integers.valueOf(64));
        PRFS_SALT.put(PKCSObjectIdentifiers.id_hmacWithSHA224, Integers.valueOf(28));
        PRFS_SALT.put(PKCSObjectIdentifiers.id_hmacWithSHA384, Integers.valueOf(48));
        PRFS_SALT.put(NISTObjectIdentifiers.id_hmacWithSHA3_224, Integers.valueOf(28));
        PRFS_SALT.put(NISTObjectIdentifiers.id_hmacWithSHA3_256, Integers.valueOf(32));
        PRFS_SALT.put(NISTObjectIdentifiers.id_hmacWithSHA3_384, Integers.valueOf(48));
        PRFS_SALT.put(NISTObjectIdentifiers.id_hmacWithSHA3_512, Integers.valueOf(64));
        PRFS_SALT.put(CryptoProObjectIdentifiers.gostR3411Hmac, Integers.valueOf(32));
        PRFS_SALT.put(RosstandartObjectIdentifiers.id_tc26_hmac_gost_3411_12_256, Integers.valueOf(32));
        PRFS_SALT.put(RosstandartObjectIdentifiers.id_tc26_hmac_gost_3411_12_512, Integers.valueOf(64));
        PRFS_SALT.put(GMObjectIdentifiers.hmac_sm3, Integers.valueOf(32));
    }

    private PBKDF2Config(Builder builder) {
        super(PKCSObjectIdentifiers.id_PBKDF2);
        this.iterationCount = builder.iterationCount;
        this.prf = builder.prf;
        this.saltLength = builder.saltLength < 0 ? getSaltSize(this.prf.getAlgorithm()) : builder.saltLength;
    }

    static int getSaltSize(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        if (PRFS_SALT.containsKey(aSN1ObjectIdentifier)) {
            return ((Integer) PRFS_SALT.get(aSN1ObjectIdentifier)).intValue();
        }
        throw new IllegalStateException("no salt size for algorithm: " + aSN1ObjectIdentifier);
    }

    public int getIterationCount() {
        return this.iterationCount;
    }

    public AlgorithmIdentifier getPRF() {
        return this.prf;
    }

    public int getSaltLength() {
        return this.saltLength;
    }
}
