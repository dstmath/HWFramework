package com.android.server.devicepolicy;

import android.content.Context;
import com.android.server.devicepolicy.HwDevicePolicyFactory.Factory;
import com.android.server.devicepolicy.HwDevicePolicyFactory.IHwDevicePolicyManagerService;

public class HwDevicePolicyFactoryImpl implements Factory {
    private static final String TAG = "HwDevicePolicyFactoryImpl";

    public static class HwDevicePolicyManagerServiceImpl implements IHwDevicePolicyManagerService {
        public DevicePolicyManagerService getInstance(Context context) {
            return new HwDevicePolicyManagerService(context);
        }
    }

    public IHwDevicePolicyManagerService getHuaweiDevicePolicyManagerService() {
        return new HwDevicePolicyManagerServiceImpl();
    }
}
