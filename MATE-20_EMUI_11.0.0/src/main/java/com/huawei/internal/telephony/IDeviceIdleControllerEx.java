package com.huawei.internal.telephony;

import android.os.IDeviceIdleController;
import android.os.RemoteException;
import com.android.internal.telephony.TelephonyComponentFactory;

public class IDeviceIdleControllerEx {
    private IDeviceIdleController mDeviceIdleController = TelephonyComponentFactory.getInstance().inject(IDeviceIdleController.class.getName()).getIDeviceIdleController();

    public long addPowerSaveTempWhitelistAppForSms(String name, int userId, String reason) {
        IDeviceIdleController iDeviceIdleController = this.mDeviceIdleController;
        if (iDeviceIdleController == null) {
            return 0;
        }
        try {
            return iDeviceIdleController.addPowerSaveTempWhitelistAppForSms(name, userId, reason);
        } catch (RemoteException e) {
            return 0;
        }
    }
}
