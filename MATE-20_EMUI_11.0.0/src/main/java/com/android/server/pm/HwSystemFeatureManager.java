package com.android.server.pm;

import android.content.pm.FeatureInfo;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.SystemConfig;

public class HwSystemFeatureManager {
    private static final String TAG = "HwSystemFeatureManager";
    private static volatile HwSystemFeatureManager sInstance;

    private HwSystemFeatureManager() {
    }

    public static HwSystemFeatureManager getInstance() {
        if (sInstance == null) {
            synchronized (HwSystemFeatureManager.class) {
                if (sInstance == null) {
                    sInstance = new HwSystemFeatureManager();
                }
            }
        }
        return sInstance;
    }

    public FeatureInfo[] getHwSystemAvailableFeatures() {
        ArrayMap<String, FeatureInfo> availableHwFeatures = SystemConfig.getInstance().getAvailableHwFeatures();
        if (availableHwFeatures == null || availableHwFeatures.size() == 0) {
            return new FeatureInfo[0];
        }
        return (FeatureInfo[]) availableHwFeatures.values().toArray(new FeatureInfo[0]);
    }

    public boolean hasHwSystemFeature(String featureName, int version) {
        ArrayMap<String, FeatureInfo> availableHwFeatures;
        FeatureInfo feat;
        if (TextUtils.isEmpty(featureName) || (availableHwFeatures = SystemConfig.getInstance().getAvailableHwFeatures()) == null || availableHwFeatures.size() == 0 || (feat = availableHwFeatures.get(featureName)) == null || feat.version < version) {
            return false;
        }
        return true;
    }
}
