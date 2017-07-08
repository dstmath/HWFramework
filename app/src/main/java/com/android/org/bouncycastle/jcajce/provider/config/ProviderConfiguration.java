package com.android.org.bouncycastle.jcajce.provider.config;

import com.android.org.bouncycastle.jce.spec.ECParameterSpec;
import javax.crypto.spec.DHParameterSpec;

public interface ProviderConfiguration {
    DHParameterSpec getDHDefaultParameters(int i);

    ECParameterSpec getEcImplicitlyCa();
}
