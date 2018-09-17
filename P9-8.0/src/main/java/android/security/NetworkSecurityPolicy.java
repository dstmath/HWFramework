package android.security;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.security.net.config.ApplicationConfig;
import android.security.net.config.ManifestConfigSource;

public class NetworkSecurityPolicy {
    private static final NetworkSecurityPolicy INSTANCE = new NetworkSecurityPolicy();

    private NetworkSecurityPolicy() {
    }

    public static NetworkSecurityPolicy getInstance() {
        return INSTANCE;
    }

    public boolean isCleartextTrafficPermitted() {
        return libcore.net.NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
    }

    public boolean isCleartextTrafficPermitted(String hostname) {
        return libcore.net.NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(hostname);
    }

    public void setCleartextTrafficPermitted(boolean permitted) {
        libcore.net.NetworkSecurityPolicy.setInstance(new FrameworkNetworkSecurityPolicy(permitted));
    }

    public void handleTrustStorageUpdate() {
        ApplicationConfig config = ApplicationConfig.getDefaultInstance();
        if (config != null) {
            config.handleTrustStorageUpdate();
        }
    }

    public static ApplicationConfig getApplicationConfigForPackage(Context context, String packageName) throws NameNotFoundException {
        return new ApplicationConfig(new ManifestConfigSource(context.createPackageContext(packageName, 0)));
    }
}
