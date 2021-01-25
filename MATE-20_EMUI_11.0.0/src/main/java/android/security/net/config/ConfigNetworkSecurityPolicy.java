package android.security.net.config;

import libcore.net.NetworkSecurityPolicy;

public class ConfigNetworkSecurityPolicy extends NetworkSecurityPolicy {
    private final ApplicationConfig mConfig;

    public ConfigNetworkSecurityPolicy(ApplicationConfig config) {
        this.mConfig = config;
    }

    public boolean isCleartextTrafficPermitted() {
        return this.mConfig.isCleartextTrafficPermitted();
    }

    public boolean isCleartextTrafficPermitted(String hostname) {
        return this.mConfig.isCleartextTrafficPermitted(hostname);
    }

    public boolean isCertificateTransparencyVerificationRequired(String hostname) {
        return false;
    }
}
