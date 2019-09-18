package org.bouncycastle.jcajce.provider.asymmetric.ecgost12;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;

public class KeyPairGeneratorSpi extends KeyPairGenerator {
    String algorithm = "ECGOST3410-2012";
    Object ecParams = null;
    ECKeyPairGenerator engine = new ECKeyPairGenerator();
    boolean initialised = false;
    ECKeyGenerationParameters param;
    SecureRandom random = null;
    int strength = 239;

    public KeyPairGeneratorSpi() {
        super("ECGOST3410-2012");
    }

    public KeyPair generateKeyPair() {
        if (this.initialised) {
            AsymmetricCipherKeyPair generateKeyPair = this.engine.generateKeyPair();
            ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters) generateKeyPair.getPublic();
            ECPrivateKeyParameters eCPrivateKeyParameters = (ECPrivateKeyParameters) generateKeyPair.getPrivate();
            if (this.ecParams instanceof ECParameterSpec) {
                ECParameterSpec eCParameterSpec = (ECParameterSpec) this.ecParams;
                BCECGOST3410_2012PublicKey bCECGOST3410_2012PublicKey = new BCECGOST3410_2012PublicKey(this.algorithm, eCPublicKeyParameters, eCParameterSpec);
                return new KeyPair(bCECGOST3410_2012PublicKey, new BCECGOST3410_2012PrivateKey(this.algorithm, eCPrivateKeyParameters, bCECGOST3410_2012PublicKey, eCParameterSpec));
            } else if (this.ecParams == null) {
                return new KeyPair(new BCECGOST3410_2012PublicKey(this.algorithm, eCPublicKeyParameters), new BCECGOST3410_2012PrivateKey(this.algorithm, eCPrivateKeyParameters));
            } else {
                java.security.spec.ECParameterSpec eCParameterSpec2 = (java.security.spec.ECParameterSpec) this.ecParams;
                BCECGOST3410_2012PublicKey bCECGOST3410_2012PublicKey2 = new BCECGOST3410_2012PublicKey(this.algorithm, eCPublicKeyParameters, eCParameterSpec2);
                return new KeyPair(bCECGOST3410_2012PublicKey2, new BCECGOST3410_2012PrivateKey(this.algorithm, eCPrivateKeyParameters, bCECGOST3410_2012PublicKey2, eCParameterSpec2));
            }
        } else {
            throw new IllegalStateException("EC Key Pair Generator not initialised");
        }
    }

    public void initialize(int i, SecureRandom secureRandom) {
        this.strength = i;
        this.random = secureRandom;
        if (this.ecParams != null) {
            try {
                initialize((AlgorithmParameterSpec) (ECGenParameterSpec) this.ecParams, secureRandom);
            } catch (InvalidAlgorithmParameterException e) {
                throw new InvalidParameterException("key size not configurable.");
            }
        } else {
            throw new InvalidParameterException("unknown key size.");
        }
    }

    public void initialize(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        ECKeyGenerationParameters eCKeyGenerationParameters;
        ECKeyGenerationParameters eCKeyGenerationParameters2;
        if (algorithmParameterSpec instanceof ECParameterSpec) {
            ECParameterSpec eCParameterSpec = (ECParameterSpec) algorithmParameterSpec;
            this.ecParams = algorithmParameterSpec;
            eCKeyGenerationParameters2 = new ECKeyGenerationParameters(new ECDomainParameters(eCParameterSpec.getCurve(), eCParameterSpec.getG(), eCParameterSpec.getN(), eCParameterSpec.getH()), secureRandom);
        } else {
            if (algorithmParameterSpec instanceof java.security.spec.ECParameterSpec) {
                java.security.spec.ECParameterSpec eCParameterSpec2 = (java.security.spec.ECParameterSpec) algorithmParameterSpec;
                this.ecParams = algorithmParameterSpec;
                ECCurve convertCurve = EC5Util.convertCurve(eCParameterSpec2.getCurve());
                eCKeyGenerationParameters = new ECKeyGenerationParameters(new ECDomainParameters(convertCurve, EC5Util.convertPoint(convertCurve, eCParameterSpec2.getGenerator(), false), eCParameterSpec2.getOrder(), BigInteger.valueOf((long) eCParameterSpec2.getCofactor())), secureRandom);
            } else {
                boolean z = algorithmParameterSpec instanceof ECGenParameterSpec;
                if (z || (algorithmParameterSpec instanceof ECNamedCurveGenParameterSpec)) {
                    String name = z ? ((ECGenParameterSpec) algorithmParameterSpec).getName() : ((ECNamedCurveGenParameterSpec) algorithmParameterSpec).getName();
                    ECDomainParameters byName = ECGOST3410NamedCurves.getByName(name);
                    if (byName != null) {
                        ECNamedCurveSpec eCNamedCurveSpec = new ECNamedCurveSpec(name, byName.getCurve(), byName.getG(), byName.getN(), byName.getH(), byName.getSeed());
                        this.ecParams = eCNamedCurveSpec;
                        java.security.spec.ECParameterSpec eCParameterSpec3 = (java.security.spec.ECParameterSpec) this.ecParams;
                        ECCurve convertCurve2 = EC5Util.convertCurve(eCParameterSpec3.getCurve());
                        eCKeyGenerationParameters = new ECKeyGenerationParameters(new ECDomainParameters(convertCurve2, EC5Util.convertPoint(convertCurve2, eCParameterSpec3.getGenerator(), false), eCParameterSpec3.getOrder(), BigInteger.valueOf((long) eCParameterSpec3.getCofactor())), secureRandom);
                    } else {
                        throw new InvalidAlgorithmParameterException("unknown curve name: " + name);
                    }
                } else if (algorithmParameterSpec == null && BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa() != null) {
                    ECParameterSpec ecImplicitlyCa = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
                    this.ecParams = algorithmParameterSpec;
                    eCKeyGenerationParameters2 = new ECKeyGenerationParameters(new ECDomainParameters(ecImplicitlyCa.getCurve(), ecImplicitlyCa.getG(), ecImplicitlyCa.getN(), ecImplicitlyCa.getH()), secureRandom);
                } else if (algorithmParameterSpec == null && BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa() == null) {
                    throw new InvalidAlgorithmParameterException("null parameter passed but no implicitCA set");
                } else {
                    throw new InvalidAlgorithmParameterException("parameter object not a ECParameterSpec: " + algorithmParameterSpec.getClass().getName());
                }
            }
            this.param = eCKeyGenerationParameters;
            this.engine.init(this.param);
            this.initialised = true;
        }
        this.param = eCKeyGenerationParameters2;
        this.engine.init(this.param);
        this.initialised = true;
    }
}
