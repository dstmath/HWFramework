package com.android.org.bouncycastle.jcajce.provider.config;

import com.android.org.bouncycastle.jce.spec.ECParameterSpec;
import java.util.Map;
import java.util.Set;
import javax.crypto.spec.DHParameterSpec;

public interface ProviderConfiguration {
    Set getAcceptableNamedCurves();

    Map getAdditionalECParameters();

    DHParameterSpec getDHDefaultParameters(int i);

    ECParameterSpec getEcImplicitlyCa();
}
