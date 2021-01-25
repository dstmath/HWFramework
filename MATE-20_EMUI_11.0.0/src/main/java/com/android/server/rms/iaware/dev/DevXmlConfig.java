package com.android.server.rms.iaware.dev;

import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import java.util.Collections;
import java.util.List;

public class DevXmlConfig {
    public static final int DEFAULT_CUST_CONFIG = 0;
    private static final String DEV_FEATURE_NAME = "DevSchedFeature";
    public static final int INVALID_DEVICE_ID = -1;
    public static final int PLATFORM_CONFIG = 1;
    private static final String TAG = "DevXmlConfig";
    private static IBinder sCmsManager;

    public static List<AwareConfig.Item> getItemList(String configName, int isCustConfig) {
        List<AwareConfig.Item> resultList = Collections.emptyList();
        if (configName == null) {
            AwareLog.e(TAG, "configName is null");
            return resultList;
        }
        if (sCmsManager == null) {
            sCmsManager = IAwareCMSManager.getICMSManager();
        }
        AwareConfig awareConfig = null;
        try {
            if (sCmsManager != null) {
                if (isCustConfig == 0) {
                    awareConfig = IAwareCMSManager.getCustConfig(sCmsManager, DEV_FEATURE_NAME, configName);
                    AwareLog.d(TAG, "read custom config: " + configName);
                } else {
                    awareConfig = IAwareCMSManager.getConfig(sCmsManager, DEV_FEATURE_NAME, configName);
                    AwareLog.d(TAG, "read platform config: " + configName);
                }
            }
            if (awareConfig == null) {
                return resultList;
            }
            return awareConfig.getConfigList();
        } catch (RemoteException e) {
            AwareLog.e(TAG, "awareConfig is null");
            return resultList;
        }
    }
}
