package org.bouncycastle.jcajce.spec;

import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.util.Arrays;

public class SM2ParameterSpec implements AlgorithmParameterSpec {
    private byte[] id;

    public SM2ParameterSpec(byte[] bArr) {
        if (bArr != null) {
            this.id = Arrays.clone(bArr);
            return;
        }
        throw new NullPointerException("id string cannot be null");
    }

    public byte[] getID() {
        return Arrays.clone(this.id);
    }
}
