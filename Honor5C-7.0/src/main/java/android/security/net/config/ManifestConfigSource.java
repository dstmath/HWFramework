package android.security.net.config;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.BatteryStats.HistoryItem;
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
    private final Object mLock;
    private final int mTargetSdkVersion;

    private static final class DefaultConfigSource implements ConfigSource {
        private final NetworkSecurityConfig mDefaultConfig;

        public DefaultConfigSource(boolean usesCleartextTraffic, int targetSdkVersion) {
            this.mDefaultConfig = NetworkSecurityConfig.getDefaultBuilder(targetSdkVersion).setCleartextTrafficPermitted(usesCleartextTraffic).build();
        }

        public NetworkSecurityConfig getDefaultConfig() {
            return this.mDefaultConfig;
        }

        public Set<Pair<Domain, NetworkSecurityConfig>> getPerDomainConfigs() {
            return null;
        }
    }

    public ManifestConfigSource(Context context) {
        this.mLock = new Object();
        this.mContext = context;
        ApplicationInfo info = context.getApplicationInfo();
        this.mApplicationInfoFlags = info.flags;
        this.mTargetSdkVersion = info.targetSdkVersion;
        this.mConfigResourceId = info.networkSecurityConfigRes;
    }

    public Set<Pair<Domain, NetworkSecurityConfig>> getPerDomainConfigs() {
        return getConfigSource().getPerDomainConfigs();
    }

    public NetworkSecurityConfig getDefaultConfig() {
        return getConfigSource().getDefaultConfig();
    }

    private ConfigSource getConfigSource() {
        synchronized (this.mLock) {
            if (this.mConfigSource != null) {
                ConfigSource configSource = this.mConfigSource;
                return configSource;
            }
            ConfigSource source;
            if (this.mConfigResourceId != 0) {
                boolean debugBuild = (this.mApplicationInfoFlags & 2) != 0 ? DBG : false;
                Log.d(LOG_TAG, "Using Network Security Config from resource " + this.mContext.getResources().getResourceEntryName(this.mConfigResourceId) + " debugBuild: " + debugBuild);
                source = new XmlConfigSource(this.mContext, this.mConfigResourceId, debugBuild, this.mTargetSdkVersion);
            } else {
                Log.d(LOG_TAG, "No Network Security Config specified, using platform default");
                source = new DefaultConfigSource((this.mApplicationInfoFlags & HistoryItem.STATE_WIFI_SCAN_FLAG) != 0 ? DBG : false, this.mTargetSdkVersion);
            }
            this.mConfigSource = source;
            configSource = this.mConfigSource;
            return configSource;
        }
    }
}
