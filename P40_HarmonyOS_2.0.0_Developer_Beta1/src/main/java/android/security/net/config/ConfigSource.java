package android.security.net.config;

import android.util.Pair;
import java.util.Set;

public interface ConfigSource {
    NetworkSecurityConfig getDefaultConfig();

    Set<Pair<Domain, NetworkSecurityConfig>> getPerDomainConfigs();
}
