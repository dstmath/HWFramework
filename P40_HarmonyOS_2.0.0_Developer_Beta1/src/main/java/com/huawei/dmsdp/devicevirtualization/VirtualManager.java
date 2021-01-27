package com.huawei.dmsdp.devicevirtualization;

import com.huawei.dmsdpsdk2.HwLog;

public class VirtualManager {
    /* access modifiers changed from: package-private */
    public void onConnect(VirtualService service) {
        HwLog.i("VirtualManager", "parent onConnect");
    }

    /* access modifiers changed from: package-private */
    public void onDisConnect() {
        HwLog.i("VirtualManager", "parent onDisConnect");
    }
}
