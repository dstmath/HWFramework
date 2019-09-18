package com.android.org.bouncycastle.jcajce.provider.asymmetric.dh;

import com.android.org.bouncycastle.crypto.generators.DHParametersGenerator;
import com.android.org.bouncycastle.crypto.params.DHParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseAlgorithmParameterGeneratorSpi;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.PrimeCertaintyCalculator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.DHGenParameterSpec;
import javax.crypto.spec.DHParameterSpec;

public class AlgorithmParameterGeneratorSpi extends BaseAlgorithmParameterGeneratorSpi {
    private int l = 0;
    protected SecureRandom random;
    protected int strength = 2048;

    /* access modifiers changed from: protected */
    public void engineInit(int strength2, SecureRandom random2) {
        this.strength = strength2;
        this.random = random2;
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec genParamSpec, SecureRandom random2) throws InvalidAlgorithmParameterException {
        if (genParamSpec instanceof DHGenParameterSpec) {
            DHGenParameterSpec spec = (DHGenParameterSpec) genParamSpec;
            this.strength = spec.getPrimeSize();
            this.l = spec.getExponentSize();
            this.random = random2;
            return;
        }
        throw new InvalidAlgorithmParameterException("DH parameter generator requires a DHGenParameterSpec for initialisation");
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameters engineGenerateParameters() {
        DHParametersGenerator pGen = new DHParametersGenerator();
        int certainty = PrimeCertaintyCalculator.getDefaultCertainty(this.strength);
        if (this.random != null) {
            pGen.init(this.strength, certainty, this.random);
        } else {
            pGen.init(this.strength, certainty, new SecureRandom());
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
