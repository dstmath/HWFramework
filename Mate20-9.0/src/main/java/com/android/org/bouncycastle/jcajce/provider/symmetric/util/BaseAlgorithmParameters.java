package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

public abstract class BaseAlgorithmParameters extends AlgorithmParametersSpi {
    /* access modifiers changed from: protected */
    public abstract AlgorithmParameterSpec localEngineGetParameterSpec(Class cls) throws InvalidParameterSpecException;

    /* access modifiers changed from: protected */
    public boolean isASN1FormatString(String format) {
        return format == null || format.equals("ASN.1");
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
        if (paramSpec != null) {
            return localEngineGetParameterSpec(paramSpec);
        }
        throw new NullPointerException("argument to getParameterSpec must not be null");
    }
}
