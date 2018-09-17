package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.rms.iaware.IRDataRegister;
import java.util.List;
import java.util.Map;

public class VsyncFeature extends RFeature {
    private static final int BASE_VERSION = 2;
    private static final String CONFIG_SUBSWITCH = "sub_switch";
    private static final int DEAULT_SWITCH_ON = 1;
    private static final String FEATURE_SUBSWITCH = "VsyncFirst";
    private static final String ITEM_PROP_TYPE = "type";
    private static final String ITEM_TYPE_SWITCH = "switch";
    private static final String SWITCH_NAME = "vsync_first";
    private static final String TAG = "VsyncFeature";

    public VsyncFeature(Context context, FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    public boolean isCustEnable() {
        boolean z = true;
        Item item = getSwitchConfig(getAwareCustConfig(FEATURE_SUBSWITCH, CONFIG_SUBSWITCH));
        if (item == null) {
            return true;
        }
        if (getSwitchValue(item) != 1) {
            z = false;
        }
        return z;
    }

    private AwareConfig getAwareCustConfig(String feature, String config) {
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                return awareservice.getCustConfig(feature, config);
            }
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareCustConfig RemoteException");
            return null;
        }
    }

    private Item getSwitchConfig(AwareConfig config) {
        if (config == null) {
            AwareLog.i(TAG, "the cust config file is null");
            return null;
        }
        List<Item> itemList = config.getConfigList();
        if (itemList == null) {
            return null;
        }
        for (Item item : itemList) {
            Map<String, String> properties = item.getProperties();
            if (properties != null) {
                if (ITEM_TYPE_SWITCH.equals((String) properties.get("type"))) {
                    return item;
                }
            }
        }
        return null;
    }

    private int getSwitchValue(Item item) {
        int result = 1;
        if (item == null) {
            return 1;
        }
        List<SubItem> subItemList = item.getSubItemList();
        if (subItemList == null) {
            return 1;
        }
        for (SubItem subItem : subItemList) {
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

    public boolean reportData(CollectData data) {
        return false;
    }

    public boolean enable() {
        AwareLog.i(TAG, "enable failed! feature based on IAware2.0, enable() method should not be called!");
        return false;
    }

    public boolean disable() {
        AwareLog.i(TAG, "disable iaware vsyncfirst feature!");
        SystemProperties.set("persist.sys.iaware.vsyncfirst", StorageUtils.SDCARD_RWMOUNTED_STATE);
        return true;
    }

    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 2) {
            AwareLog.i(TAG, "enableFeatureEx failed, realVersion: " + realVersion + ", vsyncfirst baseVersion: " + 2);
            return false;
        }
        AwareLog.i(TAG, "enableFeatureEx iaware vsyncfirst feature!");
        boolean custValue = isCustEnable();
        AwareLog.i(TAG, "enableFeatureEx custValue:" + custValue);
        if (custValue) {
            SystemProperties.set("persist.sys.iaware.vsyncfirst", StorageUtils.SDCARD_ROMOUNTED_STATE);
        } else {
            SystemProperties.set("persist.sys.iaware.vsyncfirst", StorageUtils.SDCARD_RWMOUNTED_STATE);
        }
        return true;
    }
}
