package com.android.server.wifi;

import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.InvokeMethod;

public class WifiNativeUtils extends EasyInvokeUtils {
    MethodObject<Boolean> doBooleanCommand;
    MethodObject<String> doStringCommand;
    MethodObject<Boolean> p2pGroupRemove;

    @InvokeMethod(methodObject = "doStringCommand")
    public String doStringCommand(WifiNative wifiNative, String command) {
        return (String) invokeMethod(this.doStringCommand, wifiNative, new Object[]{command});
    }

    @InvokeMethod(methodObject = "doBooleanCommand")
    public boolean doBooleanCommand(WifiNative wifiNative, String command) {
        return ((Boolean) invokeMethod(this.doBooleanCommand, wifiNative, new Object[]{command})).booleanValue();
    }

    @InvokeMethod(methodObject = "p2pGroupRemove")
    public boolean p2pGroupRemove(WifiNative wifiNative, String iface) {
        return ((Boolean) invokeMethod(this.p2pGroupRemove, wifiNative, new Object[]{iface})).booleanValue();
    }
}
