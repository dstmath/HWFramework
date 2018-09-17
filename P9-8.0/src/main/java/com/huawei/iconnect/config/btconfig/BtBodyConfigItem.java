package com.huawei.iconnect.config.btconfig;

import com.huawei.iconnect.config.btconfig.condition.AbsCondition;
import com.huawei.iconnect.hwutil.HwLog;
import com.huawei.iconnect.wearable.config.BluetoothDeviceData;
import com.huawei.iconnect.wearable.config.Info;

public class BtBodyConfigItem {
    private static final String TAG = "BtBodyConfigItem";
    private AbsCondition mCondition;
    private Info mInfo;

    public BtBodyConfigItem(AbsCondition mCondition, Info mInfo) {
        this.mCondition = mCondition;
        this.mInfo = mInfo;
    }

    public void outputString() {
        HwLog.d(TAG, this.mInfo.toString());
    }

    public Info getInfo(BluetoothDeviceData deviceData) {
        if (this.mCondition.isMatch(deviceData)) {
            return this.mInfo;
        }
        return null;
    }
}
