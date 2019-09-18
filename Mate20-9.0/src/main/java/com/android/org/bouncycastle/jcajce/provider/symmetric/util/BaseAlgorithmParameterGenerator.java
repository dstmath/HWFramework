package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import com.android.org.bouncycastle.jcajce.util.JcaJceHelper;
import java.security.AlgorithmParameterGeneratorSpi;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public abstract class BaseAlgorithmParameterGenerator extends AlgorithmParameterGeneratorSpi {
    private final JcaJceHelper helper = new DefaultJcaJceHelper();
    protected SecureRandom random;
    protected int strength = 1024;

    /* access modifiers changed from: protected */
    public final AlgorithmParameters createParametersInstance(String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
        return this.helper.createAlgorithmParameters(algorithm);
    }

    /* access modifiers changed from: protected */
    public void engineInit(int strength2, SecureRandom random2) {
        this.strength = strength2;
        this.random = random2;
    }
}
