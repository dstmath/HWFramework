package org.bouncycastle.jce.spec;

import java.security.spec.KeySpec;

public class ECKeySpec implements KeySpec {
    private ECParameterSpec spec;

    protected ECKeySpec(ECParameterSpec eCParameterSpec) {
        this.spec = eCParameterSpec;
    }

    public ECParameterSpec getParams() {
        return this.spec;
    }
}
