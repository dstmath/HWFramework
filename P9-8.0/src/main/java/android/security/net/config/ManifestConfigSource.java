package android.security.net.config;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.util.Pair;
import java.util.Set;

public class ManifestConfigSource implements ConfigSource {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "NetworkSecurityConfig";
    private final int mApplicationInfoFlags;
    private final int mConfigResourceId;
    private ConfigSource mConfigSource;
    private final Context mContext;
    private final Object mLock = new Object();
    private final int mTargetSandboxVesrsion;
    private final int mTargetSdkVersion;

    private static final class DefaultConfigSource implements ConfigSource {
        private final NetworkSecurityConfig mDefaultConfig;

        public DefaultConfigSource(boolean usesCleartextTraffic, int targetSdkVersion, int targetSandboxVesrsion) {
            this.mDefaultConfig = NetworkSecurityConfig.getDefaultBuilder(targetSdkVersion, targetSandboxVesrsion).setCleartextTrafficPermitted(usesCleartextTraffic).build();
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
        ApplicationInfo info = context.getApplicationInfo();
        this.mApplicationInfoFlags = info.flags;
        this.mTargetSdkVersion = info.targetSdkVersion;
        this.mConfigResourceId = info.networkSecurityConfigRes;
        this.mTargetSandboxVesrsion = info.targetSandboxVersion;
    }

    public Set<Pair<Domain, NetworkSecurityConfig>> getPerDomainConfigs() {
        return getConfigSource().getPerDomainConfigs();
    }

    public NetworkSecurityConfig getDefaultConfig() {
        return getConfigSource().getDefaultConfig();
    }

    private ConfigSource getConfigSource() {
        synchronized (this.mLock) {
            ConfigSource configSource;
            if (this.mConfigSource != null) {
                configSource = this.mConfigSource;
                return configSource;
            }
            ConfigSource source;
            if (this.mConfigResourceId != 0) {
                boolean debugBuild = (this.mApplicationInfoFlags & 2) != 0;
                Log.d(LOG_TAG, "Using Network Security Config from resource " + this.mContext.getResources().getResourceEntryName(this.mConfigResourceId) + " debugBuild: " + debugBuild);
                source = new XmlConfigSource(this.mContext, this.mConfigResourceId, debugBuild, this.mTargetSdkVersion, this.mTargetSandboxVesrsion);
            } else {
                Log.d(LOG_TAG, "No Network Security Config specified, using platform default");
                boolean usesCleartextTraffic = (this.mApplicationInfoFlags & 134217728) != 0 ? this.mTargetSandboxVesrsion < 2 : false;
                source = new DefaultConfigSource(usesCleartextTraffic, this.mTargetSdkVersion, this.mTargetSandboxVesrsion);
            }
            this.mConfigSource = source;
            configSource = this.mConfigSource;
            return configSource;
        }
    }
}
