package com.huawei.android.feature.install;

import android.content.Context;

public class InstallManagerFactory {
    public static InstallManager create(Context context) {
        if (context != null) {
            return new FeatureInstallManager(context);
        }
        throw new IllegalArgumentException("config must not be null");
    }

    public static InstallManager create(Context context, int i) {
        FeatureInstallManager featureInstallManager = null;
        if (i == 1 || i == 2) {
            featureInstallManager = new FeatureInstallManager(context);
        } else if (i == 3) {
            featureInstallManager = new FeatureInstallManager(context);
        }
        if (featureInstallManager != null) {
            return featureInstallManager;
        }
        throw new IllegalArgumentException("invalid loader type");
    }
}
