package com.android.server.rms.iaware.appmng;

import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.List;
import java.util.Map;

public final class AwareAppPreloadResourceManager {
    private static final String CONFIG_SUB_SWITCH = "PreloadResourcesubSwitch";
    private static final String FEATURE_BITMAP_DECODE_VALUE = "bitmap_decode_cache_value";
    private static final String FEATURE_ITEM = "feature";
    private static final String FEATURE_NAME = "PreloadResourceFeature";
    private static final String LABEL_CACHE_SIZE = "cache_size";
    private static final String LABEL_SWITCH = "switch";
    private static final Object LOCK = new Object();
    private static final String TAG = "AwareAppPreloadResourceManager";
    private static AwareAppPreloadResourceManager sInstance = null;
    private int mSizeOfBitmapDeocodeCache = 0;
    private boolean mSwitchOfBitmapDeocodeCache = false;

    private AwareAppPreloadResourceManager() {
        getPreloadResourceSwitch();
    }

    public static AwareAppPreloadResourceManager getInstance() {
        AwareAppPreloadResourceManager awareAppPreloadResourceManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new AwareAppPreloadResourceManager();
            }
            awareAppPreloadResourceManager = sInstance;
        }
        return awareAppPreloadResourceManager;
    }

    private void parseAllSwitch(String data, String featureName) {
        if (data != null && FEATURE_BITMAP_DECODE_VALUE.equals(featureName)) {
            try {
                if (Integer.parseInt(data) == 1) {
                    this.mSwitchOfBitmapDeocodeCache = true;
                }
            } catch (NumberFormatException e) {
                AwareLog.w(TAG, "parseAllSwitch parseInt FEATURE_BITMAP_DECODE_VALUE failed!");
            }
        }
    }

    private void parseAllCacheSize(String data, String featureName) {
        if (data != null && FEATURE_BITMAP_DECODE_VALUE.equals(featureName)) {
            try {
                this.mSizeOfBitmapDeocodeCache = Integer.parseInt(data);
            } catch (NumberFormatException e) {
                AwareLog.w(TAG, "parseAllCacheSize  parseInt failed!");
            }
        }
    }

    private void parseSwitchValue(AwareConfig.Item item, String featureName) {
        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
        if (subItemList == null) {
            AwareLog.w(TAG, "get sub switch config item failed!");
            return;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if ("switch".equals(itemName)) {
                    parseAllSwitch(itemValue, featureName);
                }
                if (LABEL_CACHE_SIZE.equals(itemName)) {
                    parseAllCacheSize(itemValue, featureName);
                }
            }
        }
    }

    private void getSwitchConfig(AwareConfig awareConfig) {
        Map<String, String> configProperties;
        if (awareConfig == null) {
            AwareLog.w(TAG, "get cust aware config failed!");
            return;
        }
        List<AwareConfig.Item> itemList = awareConfig.getConfigList();
        if (itemList != null) {
            for (AwareConfig.Item item : itemList) {
                if (!(item == null || (configProperties = item.getProperties()) == null)) {
                    String featureName = configProperties.get(FEATURE_ITEM);
                    if (featureName != null) {
                        parseSwitchValue(item, featureName);
                    } else {
                        return;
                    }
                }
            }
        }
    }

    private void getPreloadResourceSwitch() {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                getSwitchConfig(IAwareCMSManager.getCustConfig(awareService, FEATURE_NAME, CONFIG_SUB_SWITCH));
            } else {
                AwareLog.w(TAG, "getAwareConfig can not find service awareService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareConfig RemoteException");
        }
    }

    public void enable() {
        SystemPropertiesEx.set("persist.sys.iaware.switch.BitmapDeocodeCache", Boolean.toString(this.mSwitchOfBitmapDeocodeCache));
        SystemPropertiesEx.set("persist.sys.iaware.size.BitmapDeocodeCache", Integer.toString(this.mSizeOfBitmapDeocodeCache));
    }

    public void disable() {
        SystemPropertiesEx.set("persist.sys.iaware.switch.BitmapDeocodeCache", Boolean.toString(false));
        SystemPropertiesEx.set("persist.sys.iaware.size.BitmapDeocodeCache", Integer.toString(0));
    }
}
