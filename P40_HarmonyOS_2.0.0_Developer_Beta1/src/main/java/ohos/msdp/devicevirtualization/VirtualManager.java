package ohos.msdp.devicevirtualization;

import ohos.dmsdp.sdk.HwLog;

public class VirtualManager {
    /* access modifiers changed from: package-private */
    public void onConnect(VirtualService virtualService) {
        HwLog.i("VirtualManager", "parent onConnect");
    }

    /* access modifiers changed from: package-private */
    public void onDisConnect() {
        HwLog.i("VirtualManager", "parent onDisConnect");
    }
}
