package android.security.net.config;

import android.util.Pair;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.net.ssl.X509TrustManager;

public final class ApplicationConfig {
    private static ApplicationConfig sInstance;
    private static Object sLock = new Object();
    private ConfigSource mConfigSource;
    private Set<Pair<Domain, NetworkSecurityConfig>> mConfigs;
    private NetworkSecurityConfig mDefaultConfig;
    private boolean mInitialized;
    private final Object mLock = new Object();
    private X509TrustManager mTrustManager;

    public ApplicationConfig(ConfigSource configSource) {
        this.mConfigSource = configSource;
        this.mInitialized = false;
    }

    public boolean hasPerDomainConfigs() {
        ensureInitialized();
        Set<Pair<Domain, NetworkSecurityConfig>> set = this.mConfigs;
        return set != null && !set.isEmpty();
    }

    public NetworkSecurityConfig getConfigForHostname(String hostname) {
        ensureInitialized();
        if (hostname == null || hostname.isEmpty() || this.mConfigs == null) {
            return this.mDefaultConfig;
        }
        if (hostname.charAt(0) != '.') {
            String hostname2 = hostname.toLowerCase(Locale.US);
            if (hostname2.charAt(hostname2.length() - 1) == '.') {
                hostname2 = hostname2.substring(0, hostname2.length() - 1);
            }
            Pair<Domain, NetworkSecurityConfig> bestMatch = null;
            for (Pair<Domain, NetworkSecurityConfig> entry : this.mConfigs) {
                Domain domain = entry.first;
                NetworkSecurityConfig config = entry.second;
                if (domain.hostname.equals(hostname2)) {
                    return config;
                }
                if (domain.subdomainsIncluded && hostname2.endsWith(domain.hostname) && hostname2.charAt((hostname2.length() - domain.hostname.length()) - 1) == '.') {
                    if (bestMatch == null) {
                        bestMatch = entry;
                    } else if (domain.hostname.length() > bestMatch.first.hostname.length()) {
                        bestMatch = entry;
                    }
                }
            }
            if (bestMatch != null) {
                return bestMatch.second;
            }
            return this.mDefaultConfig;
        }
        throw new IllegalArgumentException("hostname must not begin with a .");
    }

    public X509TrustManager getTrustManager() {
        ensureInitialized();
        return this.mTrustManager;
    }

    public boolean isCleartextTrafficPermitted() {
        ensureInitialized();
        Set<Pair<Domain, NetworkSecurityConfig>> set = this.mConfigs;
        if (set != null) {
            for (Pair<Domain, NetworkSecurityConfig> entry : set) {
                if (!entry.second.isCleartextTrafficPermitted()) {
                    return false;
                }
            }
        }
        return this.mDefaultConfig.isCleartextTrafficPermitted();
    }

    public boolean isCleartextTrafficPermitted(String hostname) {
        return getConfigForHostname(hostname).isCleartextTrafficPermitted();
    }

    public void handleTrustStorageUpdate() {
        synchronized (this.mLock) {
            if (this.mInitialized) {
                this.mDefaultConfig.handleTrustStorageUpdate();
                if (this.mConfigs != null) {
                    Set<NetworkSecurityConfig> updatedConfigs = new HashSet<>(this.mConfigs.size());
                    for (Pair<Domain, NetworkSecurityConfig> entry : this.mConfigs) {
                        if (updatedConfigs.add(entry.second)) {
                            entry.second.handleTrustStorageUpdate();
                        }
                    }
                }
            }
        }
    }

    private void ensureInitialized() {
        synchronized (this.mLock) {
            if (!this.mInitialized) {
                this.mConfigs = this.mConfigSource.getPerDomainConfigs();
                this.mDefaultConfig = this.mConfigSource.getDefaultConfig();
                this.mConfigSource = null;
                this.mTrustManager = new RootTrustManager(this);
                this.mInitialized = true;
            }
        }
    }

    public static void setDefaultInstance(ApplicationConfig config) {
        synchronized (sLock) {
            sInstance = config;
        }
    }

    public static ApplicationConfig getDefaultInstance() {
        ApplicationConfig applicationConfig;
        synchronized (sLock) {
            applicationConfig = sInstance;
        }
        return applicationConfig;
    }
}
