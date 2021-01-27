package com.android.server.devicepolicy;

import android.content.Context;

public class HwDevicePolicyFactoryImpl extends HwDevicePolicyFactory {
    public IHwDevicePolicyManagerService getHuaweiDevicePolicyManagerService(Context context, IHwDevicePolicyManagerInner inner) {
        return new HwDevicePolicyManagerService(context, inner);
    }
}
