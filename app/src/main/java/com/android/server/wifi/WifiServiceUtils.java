package com.android.server.wifi;

import com.android.internal.util.AsyncChannel;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.annotation.GetField;

public class WifiServiceUtils extends EasyInvokeUtils {
    FieldObject<WifiController> mWifiController;
    FieldObject<AsyncChannel> mWifiStateMachineChannel;

    @GetField(fieldObject = "mWifiStateMachineChannel")
    public AsyncChannel getWifiStateMachineChannel(WifiServiceImpl wifiServiceImpl) {
        return (AsyncChannel) getField(this.mWifiStateMachineChannel, wifiServiceImpl);
    }

    @GetField(fieldObject = "mWifiController")
    public WifiController getWifiController(WifiServiceImpl wifiServiceImpl) {
        return (WifiController) getField(this.mWifiController, wifiServiceImpl);
    }
}
