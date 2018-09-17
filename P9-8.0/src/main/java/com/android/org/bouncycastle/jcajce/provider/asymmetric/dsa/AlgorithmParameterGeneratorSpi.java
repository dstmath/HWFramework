package com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa;

import com.android.org.bouncycastle.crypto.digests.SHA256Digest;
import com.android.org.bouncycastle.crypto.generators.DSAParametersGenerator;
import com.android.org.bouncycastle.crypto.params.DSAParameterGenerationParameters;
import com.android.org.bouncycastle.crypto.params.DSAParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseAlgorithmParameterGeneratorSpi;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;

public class AlgorithmParameterGeneratorSpi extends BaseAlgorithmParameterGeneratorSpi {
    protected DSAParameterGenerationParameters params;
    protected SecureRandom random;
    protected int strength = 1024;

    protected void engineInit(int strength, SecureRandom random) {
        if (strength < 512 || strength > 3072) {
            throw new InvalidParameterException("strength must be from 512 - 3072");
        } else if (strength <= 1024 && strength % 64 != 0) {
            throw new InvalidParameterException("strength must be a multiple of 64 below 1024 bits.");
        } else if (strength <= 1024 || strength % 1024 == 0) {
            this.strength = strength;
            this.random = random;
        } else {
            throw new InvalidParameterException("strength must be a multiple of 1024 above 1024 bits.");
        }
    }

    protected void engineInit(AlgorithmParameterSpec genParamSpec, SecureRandom random) throws InvalidAlgorithmParameterException {
        throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for DSA parameter generation.");
    }

    protected AlgorithmParameters engineGenerateParameters() {
        DSAParametersGenerator pGen;
        if (this.strength <= 1024) {
            pGen = new DSAParametersGenerator();
        } else {
            pGen = new DSAParametersGenerator(new SHA256Digest());
        }
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        if (this.strength == 1024) {
            this.params = new DSAParameterGenerationParameters(1024, 160, 80, this.random);
            pGen.init(this.params);
        } else if (this.strength > 1024) {
            this.params = new DSAParameterGenerationParameters(this.strength, 256, 80, this.random);
            pGen.init(this.params);
        } else {
            pGen.init(this.strength, 20, this.random);
        }
        DSAParameters p = pGen.generateParameters();
        try {
            AlgorithmParameters params = createParametersInstance("DSA");
            params.init(new DSAParameterSpec(p.getP(), p.getQ(), p.getG()));
            return params;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
