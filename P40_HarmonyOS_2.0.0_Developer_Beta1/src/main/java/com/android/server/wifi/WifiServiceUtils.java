package com.android.server.wifi;

import com.android.internal.util.AsyncChannel;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.annotation.GetField;

public class WifiServiceUtils extends EasyInvokeUtils {
    FieldObject<AsyncChannel> mClientModeImplChannel;
    FieldObject<Integer> mSoftApState;
    FieldObject<WifiController> mWifiController;

    @GetField(fieldObject = "mClientModeImplChannel")
    public AsyncChannel getWifiStateMachineChannel(WifiServiceImpl wifiServiceImpl) {
        return (AsyncChannel) getField(this.mClientModeImplChannel, wifiServiceImpl);
    }

    @GetField(fieldObject = "mWifiController")
    public WifiController getWifiController(WifiServiceImpl wifiServiceImpl) {
        return (WifiController) getField(this.mWifiController, wifiServiceImpl);
    }

    @GetField(fieldObject = "mSoftApState")
    public Integer getSoftApState(WifiServiceImpl wifiServiceImpl) {
        return (Integer) getField(this.mSoftApState, wifiServiceImpl);
    }
}
