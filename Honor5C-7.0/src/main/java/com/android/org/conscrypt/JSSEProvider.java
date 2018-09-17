package com.android.org.conscrypt;

import java.security.Provider;

public final class JSSEProvider extends Provider {
    private static final long serialVersionUID = 3075686092260669675L;

    public JSSEProvider() {
        super("HarmonyJSSE", 1.0d, "Harmony JSSE Provider");
        put("KeyManagerFactory.PKIX", KeyManagerFactoryImpl.class.getName());
        put("Alg.Alias.KeyManagerFactory.X509", "PKIX");
        put("TrustManagerFactory.PKIX", TrustManagerFactoryImpl.class.getName());
        put("Alg.Alias.TrustManagerFactory.X509", "PKIX");
        put("KeyStore.AndroidCAStore", TrustedCertificateKeyStoreSpi.class.getName());
    }
}
