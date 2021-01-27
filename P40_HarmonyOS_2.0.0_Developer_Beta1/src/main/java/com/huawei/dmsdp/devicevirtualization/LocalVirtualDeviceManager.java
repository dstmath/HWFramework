package com.huawei.dmsdp.devicevirtualization;

import com.huawei.dmsdpsdk2.HwLog;
import java.util.EnumSet;

public class LocalVirtualDeviceManager extends VirtualManager {
    private static final String TAG = "LocalVirtualDeviceManager";
    private static LocalVirtualDeviceManager sLocalVirtualManager = null;

    private LocalVirtualDeviceManager() {
    }

    static LocalVirtualDeviceManager getInstance() {
        LocalVirtualDeviceManager localVirtualDeviceManager;
        synchronized (LocalVirtualDeviceManager.class) {
            if (sLocalVirtualManager == null) {
                sLocalVirtualManager = new LocalVirtualDeviceManager();
            }
            localVirtualDeviceManager = sLocalVirtualManager;
        }
        return localVirtualDeviceManager;
    }

    public static int subscribe(ILocalVirtualDeviceObserver callback, EnumSet<VirtualDeviceType> typeFilter) {
        HwLog.i("LocalVirtualDeviceManager", "subscribe");
        return LocalVirtualDeviceAdapter.registerCallback(callback, typeFilter);
    }

    public static void unSubscribe(ILocalVirtualDeviceObserver callback) {
        HwLog.i("LocalVirtualDeviceManager", "unsubscribe");
        LocalVirtualDeviceAdapter.unRegisterCallBack(callback);
    }

    public static int switchToRemoteDevice(VirtualDeviceType virtualDeviceType, String virtualDeviceId) {
        HwLog.i("LocalVirtualDeviceManager", "switchToRemoteDevice");
        return LocalVirtualDeviceAdapter.switchToRemote(virtualDeviceType, virtualDeviceId);
    }

    public static int switchToLocalDevice(VirtualDeviceType virtualDeviceType, String virtualDeviceId) {
        HwLog.i("LocalVirtualDeviceManager", "switchToLocalDevice");
        return LocalVirtualDeviceAdapter.switchToLocal(virtualDeviceType, virtualDeviceId);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onConnect(VirtualService service) {
        HwLog.i("LocalVirtualDeviceManager", "onConnect");
        LocalVirtualDeviceAdapter.onConnect(service);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.dmsdp.devicevirtualization.VirtualManager
    public void onDisConnect() {
        HwLog.i("LocalVirtualDeviceManager", "onDisConnect");
        LocalVirtualDeviceAdapter.onDisConnect();
    }
}
