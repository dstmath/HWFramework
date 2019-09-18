package com.huawei.android.feature.install;

import android.content.Context;
import com.huawei.android.feature.install.config.HWAppBundleRemoteConfig;
import com.huawei.android.feature.install.config.HWSDKRemoteConfig;
import com.huawei.android.feature.install.config.RemoteConfig;

public class InstallManagerFactory {
    public static InstallManager create(Context context, int i) {
        FeatureInstallManager featureInstallManager = null;
        if (i == 1 || i == 2) {
            featureInstallManager = new FeatureInstallManager(context, new HWSDKRemoteConfig());
        } else if (i == 3) {
            featureInstallManager = new FeatureInstallManager(context, new HWAppBundleRemoteConfig());
        }
        if (featureInstallManager != null) {
            return featureInstallManager;
        }
        throw new IllegalArgumentException("invalid loader type");
    }

    public static InstallManager create(Context context, RemoteConfig remoteConfig) {
        if (context != null && remoteConfig != null) {
            return new FeatureInstallManager(context, remoteConfig);
        }
        throw new IllegalArgumentException("config must not be null");
    }
}
