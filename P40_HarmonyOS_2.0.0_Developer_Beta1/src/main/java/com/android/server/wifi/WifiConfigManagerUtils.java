package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class WifiConfigManagerUtils extends EasyInvokeUtils {
    MethodObject<WifiConfiguration> getInternalConfiguredNetwork;

    @InvokeMethod(methodObject = "getInternalConfiguredNetwork")
    public WifiConfiguration getInternalConfiguredNetwork(WifiConfigManager mgr, int networkId) {
        return (WifiConfiguration) invokeMethod(this.getInternalConfiguredNetwork, mgr, new Object[]{Integer.valueOf(networkId)});
    }
}
