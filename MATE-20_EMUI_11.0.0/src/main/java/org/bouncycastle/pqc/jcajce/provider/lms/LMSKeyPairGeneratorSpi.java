package org.bouncycastle.pqc.jcajce.provider.lms;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.HSSKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.HSSKeyPairGenerator;
import org.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.HSSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMOtsParameters;
import org.bouncycastle.pqc.crypto.lms.LMSKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.lms.LMSKeyPairGenerator;
import org.bouncycastle.pqc.crypto.lms.LMSParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSigParameters;
import org.bouncycastle.pqc.jcajce.spec.LMSHSSParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.LMSParameterSpec;

public class LMSKeyPairGeneratorSpi extends KeyPairGenerator {
    private AsymmetricCipherKeyPairGenerator engine = new LMSKeyPairGenerator();
    private boolean initialised = false;
    private KeyGenerationParameters param;
    private SecureRandom random = CryptoServicesRegistrar.getSecureRandom();
    private ASN1ObjectIdentifier treeDigest;

    public LMSKeyPairGeneratorSpi() {
        super("LMS");
    }

    @Override // java.security.KeyPairGenerator, java.security.KeyPairGeneratorSpi
    public KeyPair generateKeyPair() {
        if (!this.initialised) {
            this.param = new LMSKeyGenerationParameters(new LMSParameters(LMSigParameters.lms_sha256_n32_h10, LMOtsParameters.sha256_n32_w2), this.random);
            this.engine.init(this.param);
            this.initialised = true;
        }
        AsymmetricCipherKeyPair generateKeyPair = this.engine.generateKeyPair();
        return this.engine instanceof LMSKeyPairGenerator ? new KeyPair(new BCLMSPublicKey((LMSPublicKeyParameters) generateKeyPair.getPublic()), new BCLMSPrivateKey((LMSPrivateKeyParameters) generateKeyPair.getPrivate())) : new KeyPair(new BCLMSPublicKey((HSSPublicKeyParameters) generateKeyPair.getPublic()), new BCLMSPrivateKey((HSSPrivateKeyParameters) generateKeyPair.getPrivate()));
    }

    @Override // java.security.KeyPairGenerator, java.security.KeyPairGeneratorSpi
    public void initialize(int i, SecureRandom secureRandom) {
        throw new IllegalArgumentException("use AlgorithmParameterSpec");
    }

    @Override // java.security.KeyPairGenerator, java.security.KeyPairGeneratorSpi
    public void initialize(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException {
        AsymmetricCipherKeyPairGenerator hSSKeyPairGenerator;
        if (algorithmParameterSpec instanceof LMSParameterSpec) {
            LMSParameterSpec lMSParameterSpec = (LMSParameterSpec) algorithmParameterSpec;
            this.param = new LMSKeyGenerationParameters(new LMSParameters(lMSParameterSpec.getSigParams(), lMSParameterSpec.getOtsParams()), secureRandom);
            hSSKeyPairGenerator = new LMSKeyPairGenerator();
        } else if (algorithmParameterSpec instanceof LMSHSSParameterSpec) {
            LMSParameterSpec[] lMSSpecs = ((LMSHSSParameterSpec) algorithmParameterSpec).getLMSSpecs();
            LMSParameters[] lMSParametersArr = new LMSParameters[lMSSpecs.length];
            for (int i = 0; i != lMSSpecs.length; i++) {
                lMSParametersArr[i] = new LMSParameters(lMSSpecs[i].getSigParams(), lMSSpecs[i].getOtsParams());
            }
            this.param = new HSSKeyGenerationParameters(lMSParametersArr, secureRandom);
            hSSKeyPairGenerator = new HSSKeyPairGenerator();
        } else {
            throw new InvalidAlgorithmParameterException("parameter object not a LMSParameterSpec/LMSHSSParameterSpec");
        }
        this.engine = hSSKeyPairGenerator;
        this.engine.init(this.param);
        this.initialised = true;
    }
}
