package com.android.org.bouncycastle.jcajce.provider.asymmetric.ec;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.x9.ECNamedCurveTable;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import com.android.org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import com.android.org.bouncycastle.crypto.params.ECDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.ECPublicKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import com.android.org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;
import com.android.org.bouncycastle.jce.spec.ECNamedCurveSpec;
import com.android.org.bouncycastle.jce.spec.ECParameterSpec;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.util.Integers;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.util.Hashtable;

public abstract class KeyPairGeneratorSpi extends KeyPairGenerator {

    public static class EC extends KeyPairGeneratorSpi {
        private static Hashtable ecParameters = new Hashtable();
        String algorithm;
        int certainty;
        ProviderConfiguration configuration;
        Object ecParams;
        ECKeyPairGenerator engine;
        boolean initialised;
        ECKeyGenerationParameters param;
        SecureRandom random;
        int strength;

        static {
            ecParameters.put(Integers.valueOf(192), new ECGenParameterSpec("prime192v1"));
            ecParameters.put(Integers.valueOf(239), new ECGenParameterSpec("prime239v1"));
            ecParameters.put(Integers.valueOf(256), new ECGenParameterSpec("prime256v1"));
            ecParameters.put(Integers.valueOf(224), new ECGenParameterSpec("P-224"));
            ecParameters.put(Integers.valueOf(384), new ECGenParameterSpec("P-384"));
            ecParameters.put(Integers.valueOf(521), new ECGenParameterSpec("P-521"));
        }

        public EC() {
            super("EC");
            this.engine = new ECKeyPairGenerator();
            this.ecParams = null;
            this.strength = 256;
            this.certainty = 50;
            this.random = new SecureRandom();
            this.initialised = false;
            this.algorithm = "EC";
            this.configuration = BouncyCastleProvider.CONFIGURATION;
        }

        public EC(String algorithm, ProviderConfiguration configuration) {
            super(algorithm);
            this.engine = new ECKeyPairGenerator();
            this.ecParams = null;
            this.strength = 256;
            this.certainty = 50;
            this.random = new SecureRandom();
            this.initialised = false;
            this.algorithm = algorithm;
            this.configuration = configuration;
        }

        public void initialize(int strength, SecureRandom random) {
            this.strength = strength;
            if (random != null) {
                this.random = random;
            }
            AlgorithmParameterSpec ecParams = (ECGenParameterSpec) ecParameters.get(Integers.valueOf(strength));
            if (ecParams == null) {
                throw new InvalidParameterException("unknown key size.");
            }
            try {
                initialize(ecParams, random);
            } catch (InvalidAlgorithmParameterException e) {
                throw new InvalidParameterException("key size not configurable.");
            }
        }

        public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
            if (random == null) {
                random = this.random;
            }
            if (params == null) {
                ECParameterSpec implicitCA = this.configuration.getEcImplicitlyCa();
                if (implicitCA == null) {
                    throw new InvalidAlgorithmParameterException("null parameter passed but no implicitCA set");
                }
                this.ecParams = null;
                this.param = createKeyGenParamsBC(implicitCA, random);
            } else if (params instanceof ECParameterSpec) {
                this.ecParams = params;
                this.param = createKeyGenParamsBC((ECParameterSpec) params, random);
            } else if (params instanceof java.security.spec.ECParameterSpec) {
                this.ecParams = params;
                this.param = createKeyGenParamsJCE((java.security.spec.ECParameterSpec) params, random);
            } else if (params instanceof ECGenParameterSpec) {
                initializeNamedCurve(((ECGenParameterSpec) params).getName(), random);
            } else if (params instanceof ECNamedCurveGenParameterSpec) {
                initializeNamedCurve(((ECNamedCurveGenParameterSpec) params).getName(), random);
            } else {
                throw new InvalidAlgorithmParameterException("parameter object not a ECParameterSpec");
            }
            this.engine.init(this.param);
            this.initialised = true;
        }

        public KeyPair generateKeyPair() {
            if (!this.initialised) {
                initialize(this.strength, new SecureRandom());
            }
            AsymmetricCipherKeyPair pair = this.engine.generateKeyPair();
            ECPublicKeyParameters pub = (ECPublicKeyParameters) pair.getPublic();
            ECPrivateKeyParameters priv = (ECPrivateKeyParameters) pair.getPrivate();
            BCECPublicKey pubKey;
            if (this.ecParams instanceof ECParameterSpec) {
                ECParameterSpec p = this.ecParams;
                pubKey = new BCECPublicKey(this.algorithm, pub, p, this.configuration);
                return new KeyPair(pubKey, new BCECPrivateKey(this.algorithm, priv, pubKey, p, this.configuration));
            } else if (this.ecParams == null) {
                return new KeyPair(new BCECPublicKey(this.algorithm, pub, this.configuration), new BCECPrivateKey(this.algorithm, priv, this.configuration));
            } else {
                java.security.spec.ECParameterSpec p2 = this.ecParams;
                pubKey = new BCECPublicKey(this.algorithm, pub, p2, this.configuration);
                return new KeyPair(pubKey, new BCECPrivateKey(this.algorithm, priv, pubKey, p2, this.configuration));
            }
        }

        protected ECKeyGenerationParameters createKeyGenParamsBC(ECParameterSpec p, SecureRandom r) {
            return new ECKeyGenerationParameters(new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH()), r);
        }

        protected ECKeyGenerationParameters createKeyGenParamsJCE(java.security.spec.ECParameterSpec p, SecureRandom r) {
            ECCurve curve = EC5Util.convertCurve(p.getCurve());
            return new ECKeyGenerationParameters(new ECDomainParameters(curve, EC5Util.convertPoint(curve, p.getGenerator(), false), p.getOrder(), BigInteger.valueOf((long) p.getCofactor())), r);
        }

        protected ECNamedCurveSpec createNamedCurveSpec(String curveName) throws InvalidAlgorithmParameterException {
            X9ECParameters p = ECUtils.getDomainParametersFromName(curveName);
            if (p == null) {
                try {
                    p = ECNamedCurveTable.getByOID(new ASN1ObjectIdentifier(curveName));
                    if (p == null) {
                        p = (X9ECParameters) this.configuration.getAdditionalECParameters().get(new ASN1ObjectIdentifier(curveName));
                        if (p == null) {
                            throw new InvalidAlgorithmParameterException("unknown curve OID: " + curveName);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    throw new InvalidAlgorithmParameterException("unknown curve name: " + curveName);
                }
            }
            return new ECNamedCurveSpec(curveName, p.getCurve(), p.getG(), p.getN(), p.getH(), null);
        }

        protected void initializeNamedCurve(String curveName, SecureRandom random) throws InvalidAlgorithmParameterException {
            ECNamedCurveSpec namedCurve = createNamedCurveSpec(curveName);
            this.ecParams = namedCurve;
            this.param = createKeyGenParamsJCE(namedCurve, random);
        }
    }

    public static class ECDH extends EC {
        public ECDH() {
            super("ECDH", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECDHC extends EC {
        public ECDHC() {
            super("ECDHC", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECDSA extends EC {
        public ECDSA() {
            super("ECDSA", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public static class ECMQV extends EC {
        public ECMQV() {
            super("ECMQV", BouncyCastleProvider.CONFIGURATION);
        }
    }

    public KeyPairGeneratorSpi(String algorithmName) {
        super(algorithmName);
    }
}
