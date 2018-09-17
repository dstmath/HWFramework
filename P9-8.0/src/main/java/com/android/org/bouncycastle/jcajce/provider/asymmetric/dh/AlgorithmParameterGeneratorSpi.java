package com.android.org.bouncycastle.jcajce.provider.asymmetric.dh;

import com.android.org.bouncycastle.crypto.generators.DHParametersGenerator;
import com.android.org.bouncycastle.crypto.params.DHParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseAlgorithmParameterGeneratorSpi;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.DHGenParameterSpec;
import javax.crypto.spec.DHParameterSpec;

public class AlgorithmParameterGeneratorSpi extends BaseAlgorithmParameterGeneratorSpi {
    private int l = 0;
    protected SecureRandom random;
    protected int strength = 1024;

    protected void engineInit(int strength, SecureRandom random) {
        this.strength = strength;
        this.random = random;
    }

    protected void engineInit(AlgorithmParameterSpec genParamSpec, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (genParamSpec instanceof DHGenParameterSpec) {
            DHGenParameterSpec spec = (DHGenParameterSpec) genParamSpec;
            this.strength = spec.getPrimeSize();
            this.l = spec.getExponentSize();
            this.random = random;
            return;
        }
        throw new InvalidAlgorithmParameterException("DH parameter generator requires a DHGenParameterSpec for initialisation");
    }

    protected AlgorithmParameters engineGenerateParameters() {
        DHParametersGenerator pGen = new DHParametersGenerator();
        if (this.random != null) {
            pGen.init(this.strength, 20, this.random);
        } else {
            pGen.init(this.strength, 20, new SecureRandom());
        }
        DHParameters p = pGen.generateParameters();
        try {
            AlgorithmParameters params = createParametersInstance("DH");
            params.init(new DHParameterSpec(p.getP(), p.getG(), this.l));
            return params;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
