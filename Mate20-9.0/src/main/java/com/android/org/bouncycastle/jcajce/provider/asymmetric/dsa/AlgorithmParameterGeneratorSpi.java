package com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa;

import com.android.org.bouncycastle.crypto.digests.SHA256Digest;
import com.android.org.bouncycastle.crypto.generators.DSAParametersGenerator;
import com.android.org.bouncycastle.crypto.params.DSAParameterGenerationParameters;
import com.android.org.bouncycastle.crypto.params.DSAParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseAlgorithmParameterGeneratorSpi;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.PrimeCertaintyCalculator;
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

    /* access modifiers changed from: protected */
    public void engineInit(int strength2, SecureRandom random2) {
        if (strength2 < 512 || strength2 > 3072) {
            throw new InvalidParameterException("strength must be from 512 - 3072");
        } else if (strength2 <= 1024 && strength2 % 64 != 0) {
            throw new InvalidParameterException("strength must be a multiple of 64 below 1024 bits.");
        } else if (strength2 <= 1024 || strength2 % 1024 == 0) {
            this.strength = strength2;
            this.random = random2;
        } else {
            throw new InvalidParameterException("strength must be a multiple of 1024 above 1024 bits.");
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec genParamSpec, SecureRandom random2) throws InvalidAlgorithmParameterException {
        throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for DSA parameter generation.");
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameters engineGenerateParameters() {
        DSAParametersGenerator pGen;
        if (this.strength <= 1024) {
            pGen = new DSAParametersGenerator();
        } else {
            pGen = new DSAParametersGenerator(new SHA256Digest());
        }
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        int certainty = PrimeCertaintyCalculator.getDefaultCertainty(this.strength);
        if (this.strength == 1024) {
            this.params = new DSAParameterGenerationParameters(1024, 160, certainty, this.random);
            pGen.init(this.params);
        } else if (this.strength > 1024) {
            this.params = new DSAParameterGenerationParameters(this.strength, 256, certainty, this.random);
            pGen.init(this.params);
        } else {
            pGen.init(this.strength, certainty, this.random);
        }
        DSAParameters p = pGen.generateParameters();
        try {
            AlgorithmParameters params2 = createParametersInstance("DSA");
            params2.init(new DSAParameterSpec(p.getP(), p.getQ(), p.getG()));
            return params2;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
