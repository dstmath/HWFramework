package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

public abstract class BaseAlgorithmParameters extends AlgorithmParametersSpi {
    protected abstract AlgorithmParameterSpec localEngineGetParameterSpec(Class cls) throws InvalidParameterSpecException;

    protected boolean isASN1FormatString(String format) {
        return format != null ? format.equals("ASN.1") : true;
    }

    protected AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
        if (paramSpec != null) {
            return localEngineGetParameterSpec(paramSpec);
        }
        throw new NullPointerException("argument to getParameterSpec must not be null");
    }
}
