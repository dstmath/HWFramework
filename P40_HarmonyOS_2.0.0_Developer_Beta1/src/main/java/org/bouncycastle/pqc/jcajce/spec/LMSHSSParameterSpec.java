package org.bouncycastle.pqc.jcajce.spec;

import java.security.spec.AlgorithmParameterSpec;

public class LMSHSSParameterSpec implements AlgorithmParameterSpec {
    private final LMSParameterSpec[] specs;

    public LMSHSSParameterSpec(LMSParameterSpec[] lMSParameterSpecArr) {
        this.specs = (LMSParameterSpec[]) lMSParameterSpecArr.clone();
    }

    public LMSParameterSpec[] getLMSSpecs() {
        return (LMSParameterSpec[]) this.specs.clone();
    }
}
