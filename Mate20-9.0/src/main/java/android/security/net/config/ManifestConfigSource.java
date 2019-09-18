package android.security.net.config;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.util.Pair;
import java.util.Set;

public class ManifestConfigSource implements ConfigSource {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "NetworkSecurityConfig";
    private final ApplicationInfo mApplicationInfo;
    private ConfigSource mConfigSource;
    private final Context mContext;
    private final Object mLock = new Object();

    private static final class DefaultConfigSource implements ConfigSource {
        private final NetworkSecurityConfig mDefaultConfig;

        DefaultConfigSource(boolean usesCleartextTraffic, ApplicationInfo info) {
            this.mDefaultConfig = NetworkSecurityConfig.getDefaultBuilder(info).setCleartextTrafficPermitted(usesCleartextTraffic).build();
        }

        public NetworkSecurityConfig getDefaultConfig() {
            return this.mDefaultConfig;
        }

        public Set<Pair<Domain, NetworkSecurityConfig>> getPerDomainConfigs() {
            return null;
        }
    }

    public ManifestConfigSource(Context context) {
        this.mContext = context;
        this.mApplicationInfo = new ApplicationInfo(context.getApplicationInfo());
    }

    public Set<Pair<Domain, NetworkSecurityConfig>> getPerDomainConfigs() {
        return getConfigSource().getPerDomainConfigs();
    }

    public NetworkSecurityConfig getDefaultConfig() {
        return getConfigSource().getDefaultConfig();
    }

    private ConfigSource getConfigSource() {
        XmlConfigSource source;
        synchronized (this.mLock) {
            if (this.mConfigSource != null) {
                ConfigSource configSource = this.mConfigSource;
                return configSource;
            }
            int configResource = this.mApplicationInfo.networkSecurityConfigRes;
            boolean debugBuild = false;
            if (configResource != 0) {
                if ((2 & this.mApplicationInfo.flags) != 0) {
                    debugBuild = true;
                }
                Log.d(LOG_TAG, "Using Network Security Config from resource " + this.mContext.getResources().getResourceEntryName(configResource) + " debugBuild: " + debugBuild);
                source = new XmlConfigSource(this.mContext, configResource, this.mApplicationInfo);
            } else {
                Log.d(LOG_TAG, "No Network Security Config specified, using platform default");
                if ((this.mApplicationInfo.flags & 134217728) != 0 && this.mApplicationInfo.targetSandboxVersion < 2) {
                    debugBuild = true;
                }
                source = new DefaultConfigSource(debugBuild, this.mApplicationInfo);
            }
            this.mConfigSource = source;
            ConfigSource configSource2 = this.mConfigSource;
            return configSource2;
        }
    }
}
