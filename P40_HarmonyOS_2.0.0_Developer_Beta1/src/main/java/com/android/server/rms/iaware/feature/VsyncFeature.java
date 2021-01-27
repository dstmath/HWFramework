package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import com.android.server.rms.iaware.IRDataRegister;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.List;
import java.util.Map;

public class VsyncFeature extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final String CONFIG_SUB_SWITCH = "sub_switch";
    private static final int DEAULT_SWITCH_ON = 1;
    private static final String FEATURE_SUB_SWITCH = "VsyncFirst";
    private static final String ITEM_PROP_TYPE = "type";
    private static final String ITEM_TYPE_SWITCH = "switch";
    private static final String SWITCH_NAME = "vsync_first";
    private static final String TAG = "VsyncFeature";

    public VsyncFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    public boolean isCustEnable() {
        AwareConfig.Item item = getSwitchConfig(getAwareCustConfig(FEATURE_SUB_SWITCH, CONFIG_SUB_SWITCH));
        if (item == null || getSwitchValue(item) == 1) {
            return true;
        }
        return false;
    }

    private AwareConfig getAwareCustConfig(String feature, String config) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                return IAwareCMSManager.getCustConfig(awareService, feature, config);
            }
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareCustConfig RemoteException");
            return null;
        }
    }

    private AwareConfig.Item getSwitchConfig(AwareConfig config) {
        if (config == null) {
            AwareLog.i(TAG, "the cust config file is null");
            return null;
        }
        List<AwareConfig.Item> itemList = config.getConfigList();
        if (itemList == null) {
            return null;
        }
        for (AwareConfig.Item item : itemList) {
            Map<String, String> properties = item.getProperties();
            if (properties != null && "switch".equals(properties.get(ITEM_PROP_TYPE))) {
                return item;
            }
        }
        return null;
    }

    private int getSwitchValue(AwareConfig.Item item) {
        List<AwareConfig.SubItem> subItemList;
        int result = 1;
        if (item == null || (subItemList = item.getSubItemList()) == null) {
            return 1;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (SWITCH_NAME.equals(itemName)) {
                try {
                    result = Integer.parseInt(itemValue);
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "parse Int failed");
                }
            }
        }
        return result;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        AwareLog.i(TAG, "enable failed! feature based on IAware2.0, enable() method should not be called!");
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        AwareLog.i(TAG, "disable iaware vsyncfirst feature!");
        SystemPropertiesEx.set("persist.sys.iaware.vsyncfirst", "false");
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 2) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", baseVersion: 2");
            return false;
        }
        AwareLog.i(TAG, "enableFeatureEx iaware vsyncfirst feature!");
        boolean custValue = isCustEnable();
        AwareLog.i(TAG, "enableFeatureEx custValue:" + custValue);
        if (custValue) {
            SystemPropertiesEx.set("persist.sys.iaware.vsyncfirst", "true");
            return true;
        }
        SystemPropertiesEx.set("persist.sys.iaware.vsyncfirst", "false");
        return true;
    }
}
