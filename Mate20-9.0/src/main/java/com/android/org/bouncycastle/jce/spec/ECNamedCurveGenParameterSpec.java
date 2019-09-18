package com.android.org.bouncycastle.jce.spec;

import java.security.spec.AlgorithmParameterSpec;

public class ECNamedCurveGenParameterSpec implements AlgorithmParameterSpec {
    private String name;

    public ECNamedCurveGenParameterSpec(String name2) {
        this.name = name2;
    }

    public String getName() {
        return this.name;
    }
}
