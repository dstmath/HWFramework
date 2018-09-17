package android.security.net.config;

import android.security.net.config.NetworkSecurityConfig.Builder;
import android.util.Pair;
import java.security.KeyStore;
import java.util.Set;

class KeyStoreConfigSource implements ConfigSource {
    private final NetworkSecurityConfig mConfig;

    public KeyStoreConfigSource(KeyStore ks) {
        this.mConfig = new Builder().addCertificatesEntryRef(new CertificatesEntryRef(new KeyStoreCertificateSource(ks), false)).build();
    }

    public Set<Pair<Domain, NetworkSecurityConfig>> getPerDomainConfigs() {
        return null;
    }

    public NetworkSecurityConfig getDefaultConfig() {
        return this.mConfig;
    }
}
