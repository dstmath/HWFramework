package com.android.server.emcom;

import java.util.HashMap;

public class FeatureManager {
    static final boolean DEBUG = false;
    static final String TAG = "BaseService";
    private static int mFeatureValue = 0;
    private static FeatureManager mInstance;
    private HashMap<String, Integer> mFeatureMap = new HashMap();

    private FeatureManager() {
    }

    public static synchronized FeatureManager getInstance() {
        synchronized (FeatureManager.class) {
            FeatureManager featureManager;
            if (mInstance == null) {
                mInstance = new FeatureManager();
                featureManager = mInstance;
                return featureManager;
            }
            featureManager = mInstance;
            return featureManager;
        }
    }

    public static synchronized void setFeatureValue(int value) {
        synchronized (FeatureManager.class) {
            mFeatureValue = value;
        }
    }

    public Integer isFeatureEnable(String servicename) {
        return (Integer) this.mFeatureMap.get(servicename);
    }

    public boolean isFeatureEnable(int subFeature) {
        return (mFeatureValue & (1 << subFeature)) != 0;
    }
}
