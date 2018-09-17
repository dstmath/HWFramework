package com.android.server.wifi;

import android.content.Context;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.annotation.GetField;

public class HwWifiConfigStoreUtils extends EasyInvokeUtils {
    FieldObject<ConfigurationMap> mConfiguredNetworks;
    FieldObject<Context> mContext;

    @GetField(fieldObject = "mContext")
    public Context getContext(WifiConfigManager wifiConfigManager) {
        return (Context) getField(this.mContext, wifiConfigManager);
    }

    @GetField(fieldObject = "mConfiguredNetworks")
    public ConfigurationMap getConfiguredNetworks(WifiConfigManager wifiConfigManager) {
        return (ConfigurationMap) getField(this.mConfiguredNetworks, wifiConfigManager);
    }
}
