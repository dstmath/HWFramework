package com.android.org.bouncycastle.jcajce.spec;

import com.android.org.bouncycastle.util.Arrays;
import java.security.spec.AlgorithmParameterSpec;

public class UserKeyingMaterialSpec implements AlgorithmParameterSpec {
    private final byte[] userKeyingMaterial;

    public UserKeyingMaterialSpec(byte[] userKeyingMaterial) {
        this.userKeyingMaterial = Arrays.clone(userKeyingMaterial);
    }

    public byte[] getUserKeyingMaterial() {
        return Arrays.clone(this.userKeyingMaterial);
    }
}
