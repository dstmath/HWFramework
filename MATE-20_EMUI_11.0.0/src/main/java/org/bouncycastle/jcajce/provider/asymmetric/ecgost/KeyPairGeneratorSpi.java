package org.bouncycastle.jcajce.provider.asymmetric.ecgost;

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
import org.bouncycastle.crypto.params.ECGOST3410Parameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jcajce.spec.GOST3410ParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;

public class KeyPairGeneratorSpi extends KeyPairGenerator {
    String algorithm = "ECGOST3410";
    Object ecParams = null;
    ECKeyPairGenerator engine = new ECKeyPairGenerator();
    boolean initialised = false;
    ECKeyGenerationParameters param;
    SecureRandom random = null;
    int strength = 239;

    public KeyPairGeneratorSpi() {
        super("ECGOST3410");
    }

    private void init(GOST3410ParameterSpec gOST3410ParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        ECDomainParameters byOID = ECGOST3410NamedCurves.getByOID(gOST3410ParameterSpec.getPublicKeyParamSet());
        if (byOID != null) {
            this.ecParams = new ECNamedCurveSpec(ECGOST3410NamedCurves.getName(gOST3410ParameterSpec.getPublicKeyParamSet()), byOID.getCurve(), byOID.getG(), byOID.getN(), byOID.getH(), byOID.getSeed());
            this.param = new ECKeyGenerationParameters(new ECGOST3410Parameters(new ECNamedDomainParameters(gOST3410ParameterSpec.getPublicKeyParamSet(), byOID), gOST3410ParameterSpec.getPublicKeyParamSet(), gOST3410ParameterSpec.getDigestParamSet(), gOST3410ParameterSpec.getEncryptionParamSet()), secureRandom);
            this.engine.init(this.param);
            this.initialised = true;
            return;
        }
        throw new InvalidAlgorithmParameterException("unknown curve: " + gOST3410ParameterSpec.getPublicKeyParamSet());
    }

    @Override // java.security.KeyPairGenerator, java.security.KeyPairGeneratorSpi
    public KeyPair generateKeyPair() {
        if (this.initialised) {
            AsymmetricCipherKeyPair generateKeyPair = this.engine.generateKeyPair();
            ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters) generateKeyPair.getPublic();
            ECPrivateKeyParameters eCPrivateKeyParameters = (ECPrivateKeyParameters) generateKeyPair.getPrivate();
            Object obj = this.ecParams;
            if (obj instanceof ECParameterSpec) {
                ECParameterSpec eCParameterSpec = (ECParameterSpec) obj;
                BCECGOST3410PublicKey bCECGOST3410PublicKey = new BCECGOST3410PublicKey(this.algorithm, eCPublicKeyParameters, eCParameterSpec);
                return new KeyPair(bCECGOST3410PublicKey, new BCECGOST3410PrivateKey(this.algorithm, eCPrivateKeyParameters, bCECGOST3410PublicKey, eCParameterSpec));
            } else if (obj == null) {
                return new KeyPair(new BCECGOST3410PublicKey(this.algorithm, eCPublicKeyParameters), new BCECGOST3410PrivateKey(this.algorithm, eCPrivateKeyParameters));
            } else {
                java.security.spec.ECParameterSpec eCParameterSpec2 = (java.security.spec.ECParameterSpec) obj;
                BCECGOST3410PublicKey bCECGOST3410PublicKey2 = new BCECGOST3410PublicKey(this.algorithm, eCPublicKeyParameters, eCParameterSpec2);
                return new KeyPair(bCECGOST3410PublicKey2, new BCECGOST3410PrivateKey(this.algorithm, eCPrivateKeyParameters, bCECGOST3410PublicKey2, eCParameterSpec2));
            }
        } else {
            throw new IllegalStateException("EC Key Pair Generator not initialised");
        }
    }

    @Override // java.security.KeyPairGenerator, java.security.KeyPairGeneratorSpi
    public void initialize(int i, SecureRandom secureRandom) {
        this.strength = i;
        this.random = secureRandom;
        Object obj = this.ecParams;
        if (obj != null) {
            try {
                initialize((ECGenParameterSpec) obj, secureRandom);
            } catch (InvalidAlgorithmParameterException e) {
                throw new InvalidParameterException("key size not configurable.");
            }
        } else {
            throw new InvalidParameterException("unknown key size.");
        }
    }

    @Override // java.security.KeyPairGenerator, java.security.KeyPairGeneratorSpi
    public void initialize(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        ECKeyGenerationParameters eCKeyGenerationParameters;
        if (algorithmParameterSpec instanceof GOST3410ParameterSpec) {
            init((GOST3410ParameterSpec) algorithmParameterSpec, secureRandom);
            return;
        }
        if (algorithmParameterSpec instanceof ECParameterSpec) {
            ECParameterSpec eCParameterSpec = (ECParameterSpec) algorithmParameterSpec;
            this.ecParams = algorithmParameterSpec;
            eCKeyGenerationParameters = new ECKeyGenerationParameters(new ECDomainParameters(eCParameterSpec.getCurve(), eCParameterSpec.getG(), eCParameterSpec.getN(), eCParameterSpec.getH()), secureRandom);
        } else if (algorithmParameterSpec instanceof java.security.spec.ECParameterSpec) {
            java.security.spec.ECParameterSpec eCParameterSpec2 = (java.security.spec.ECParameterSpec) algorithmParameterSpec;
            this.ecParams = algorithmParameterSpec;
            ECCurve convertCurve = EC5Util.convertCurve(eCParameterSpec2.getCurve());
            this.param = new ECKeyGenerationParameters(new ECDomainParameters(convertCurve, EC5Util.convertPoint(convertCurve, eCParameterSpec2.getGenerator()), eCParameterSpec2.getOrder(), BigInteger.valueOf((long) eCParameterSpec2.getCofactor())), secureRandom);
            this.engine.init(this.param);
            this.initialised = true;
        } else {
            boolean z = algorithmParameterSpec instanceof ECGenParameterSpec;
            if (z || (algorithmParameterSpec instanceof ECNamedCurveGenParameterSpec)) {
                init(new GOST3410ParameterSpec(z ? ((ECGenParameterSpec) algorithmParameterSpec).getName() : ((ECNamedCurveGenParameterSpec) algorithmParameterSpec).getName()), secureRandom);
                return;
            } else if (algorithmParameterSpec == null && BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa() != null) {
                ECParameterSpec ecImplicitlyCa = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
                this.ecParams = algorithmParameterSpec;
                eCKeyGenerationParameters = new ECKeyGenerationParameters(new ECDomainParameters(ecImplicitlyCa.getCurve(), ecImplicitlyCa.getG(), ecImplicitlyCa.getN(), ecImplicitlyCa.getH()), secureRandom);
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
}
