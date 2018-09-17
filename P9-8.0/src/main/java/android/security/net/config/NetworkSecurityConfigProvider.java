package android.security.net.config;

import android.content.Context;
import java.security.Provider;
import java.security.Security;
import libcore.net.NetworkSecurityPolicy;

public final class NetworkSecurityConfigProvider extends Provider {
    private static final String PREFIX = (NetworkSecurityConfigProvider.class.getPackage().getName() + ".");

    public NetworkSecurityConfigProvider() {
        super("AndroidNSSP", 1.0d, "Android Network Security Policy Provider");
        put("TrustManagerFactory.PKIX", PREFIX + "RootTrustManagerFactorySpi");
        put("Alg.Alias.TrustManagerFactory.X509", "PKIX");
    }

    public static void install(Context context) {
        ApplicationConfig config = new ApplicationConfig(new ManifestConfigSource(context));
        ApplicationConfig.setDefaultInstance(config);
        int pos = Security.insertProviderAt(new NetworkSecurityConfigProvider(), 1);
        if (pos != 1) {
            throw new RuntimeException("Failed to install provider as highest priority provider. Provider was installed at position " + pos);
        }
        NetworkSecurityPolicy.setInstance(new ConfigNetworkSecurityPolicy(config));
    }
}
