package com.android.org.bouncycastle.jcajce.provider.asymmetric.ec;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseKeyFactorySpi;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import com.android.org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.jce.spec.ECParameterSpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class KeyFactorySpi extends BaseKeyFactorySpi implements AsymmetricKeyInfoConverter {
    String algorithm;
    ProviderConfiguration configuration;

    public static class EC extends KeyFactorySpi {
        public EC() {
            super("EC", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECDH extends KeyFactorySpi {
        public ECDH() {
            super("ECDH", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECDHC extends KeyFactorySpi {
        public ECDHC() {
            super("ECDHC", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECDSA extends KeyFactorySpi {
        public ECDSA() {
            super("ECDSA", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECMQV extends KeyFactorySpi {
        public ECMQV() {
            super("ECMQV", BouncyCastleProvider.CONFIGURATION);
        }
    }

    KeyFactorySpi(String algorithm, ProviderConfiguration configuration) {
        this.algorithm = algorithm;
        this.configuration = configuration;
    }

    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key instanceof ECPublicKey) {
            return new BCECPublicKey((ECPublicKey) key, this.configuration);
        }
        if (key instanceof ECPrivateKey) {
            return new BCECPrivateKey((ECPrivateKey) key, this.configuration);
        }
        throw new InvalidKeyException("key type unknown");
    }

    protected KeySpec engineGetKeySpec(Key key, Class spec) throws InvalidKeySpecException {
        ECPublicKey k;
        ECParameterSpec implicitSpec;
        ECPrivateKey k2;
        if (spec.isAssignableFrom(ECPublicKeySpec.class) && (key instanceof ECPublicKey)) {
            k = (ECPublicKey) key;
            if (k.getParams() != null) {
                return new ECPublicKeySpec(k.getW(), k.getParams());
            }
            implicitSpec = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
            return new ECPublicKeySpec(k.getW(), EC5Util.convertSpec(EC5Util.convertCurve(implicitSpec.getCurve(), implicitSpec.getSeed()), implicitSpec));
        } else if (spec.isAssignableFrom(ECPrivateKeySpec.class) && (key instanceof ECPrivateKey)) {
            k2 = (ECPrivateKey) key;
            if (k2.getParams() != null) {
                return new ECPrivateKeySpec(k2.getS(), k2.getParams());
            }
            implicitSpec = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
            return new ECPrivateKeySpec(k2.getS(), EC5Util.convertSpec(EC5Util.convertCurve(implicitSpec.getCurve(), implicitSpec.getSeed()), implicitSpec));
        } else if (spec.isAssignableFrom(com.android.org.bouncycastle.jce.spec.ECPublicKeySpec.class) && (key instanceof ECPublicKey)) {
            k = (ECPublicKey) key;
            if (k.getParams() != null) {
                return new com.android.org.bouncycastle.jce.spec.ECPublicKeySpec(EC5Util.convertPoint(k.getParams(), k.getW(), false), EC5Util.convertSpec(k.getParams(), false));
            }
            return new com.android.org.bouncycastle.jce.spec.ECPublicKeySpec(EC5Util.convertPoint(k.getParams(), k.getW(), false), BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa());
        } else if (!spec.isAssignableFrom(com.android.org.bouncycastle.jce.spec.ECPrivateKeySpec.class) || !(key instanceof ECPrivateKey)) {
            return super.engineGetKeySpec(key, spec);
        } else {
            k2 = (ECPrivateKey) key;
            if (k2.getParams() != null) {
                return new com.android.org.bouncycastle.jce.spec.ECPrivateKeySpec(k2.getS(), EC5Util.convertSpec(k2.getParams(), false));
            }
            return new com.android.org.bouncycastle.jce.spec.ECPrivateKeySpec(k2.getS(), BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa());
        }
    }

    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec instanceof com.android.org.bouncycastle.jce.spec.ECPrivateKeySpec) {
            return new BCECPrivateKey(this.algorithm, (com.android.org.bouncycastle.jce.spec.ECPrivateKeySpec) keySpec, this.configuration);
        }
        if (keySpec instanceof ECPrivateKeySpec) {
            return new BCECPrivateKey(this.algorithm, (ECPrivateKeySpec) keySpec, this.configuration);
        }
        return super.engineGeneratePrivate(keySpec);
    }

    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
        try {
            if (keySpec instanceof com.android.org.bouncycastle.jce.spec.ECPublicKeySpec) {
                return new BCECPublicKey(this.algorithm, (com.android.org.bouncycastle.jce.spec.ECPublicKeySpec) keySpec, this.configuration);
            }
            if (keySpec instanceof ECPublicKeySpec) {
                return new BCECPublicKey(this.algorithm, (ECPublicKeySpec) keySpec, this.configuration);
            }
            return super.engineGeneratePublic(keySpec);
        } catch (Exception e) {
            throw new InvalidKeySpecException("invalid KeySpec: " + e.getMessage(), e);
        }
    }

    public PrivateKey generatePrivate(PrivateKeyInfo keyInfo) throws IOException {
        ASN1ObjectIdentifier algOid = keyInfo.getPrivateKeyAlgorithm().getAlgorithm();
        if (algOid.equals(X9ObjectIdentifiers.id_ecPublicKey)) {
            return new BCECPrivateKey(this.algorithm, keyInfo, this.configuration);
        }
        throw new IOException("algorithm identifier " + algOid + " in key not recognised");
    }

    public PublicKey generatePublic(SubjectPublicKeyInfo keyInfo) throws IOException {
        ASN1ObjectIdentifier algOid = keyInfo.getAlgorithm().getAlgorithm();
        if (algOid.equals(X9ObjectIdentifiers.id_ecPublicKey)) {
            return new BCECPublicKey(this.algorithm, keyInfo, this.configuration);
        }
        throw new IOException("algorithm identifier " + algOid + " in key not recognised");
    }
}
