package ohos.distributedschedule.interwork;

import java.util.Collections;
import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.bundle.AbilityInfo;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class DeviceManager {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218109952, "DeviceManager");

    private DeviceManager() {
    }

    public static boolean registerDeviceStateCallback(IDeviceStateCallback iDeviceStateCallback) {
        if (iDeviceStateCallback == null) {
            return false;
        }
        try {
            return DeviceManagerServiceProxy.getInstance().registerDeviceStateCallback(DeviceStateCallbackImpl.get(iDeviceStateCallback));
        } catch (RemoteException unused) {
            HiLog.error(TAG, "failed to register device state callback", new Object[0]);
            return false;
        }
    }

    public static boolean unregisterDeviceStateCallback(IDeviceStateCallback iDeviceStateCallback) {
        if (iDeviceStateCallback == null) {
            return false;
        }
        try {
            return DeviceManagerServiceProxy.getInstance().unregisterDeviceStateCallback(DeviceStateCallbackImpl.remove(iDeviceStateCallback));
        } catch (RemoteException unused) {
            HiLog.error(TAG, "failed to unregister device state callback", new Object[0]);
            return false;
        }
    }

    public static List<DeviceInfo> getDeviceList(int i) {
        if (i == 0 || i == 1 || i == 2) {
            try {
                return DeviceManagerServiceProxy.getInstance().getDeviceList(i);
            } catch (RemoteException unused) {
                HiLog.error(TAG, "failed to get device list", new Object[0]);
                return Collections.emptyList();
            }
        } else {
            HiLog.error(TAG, "invalid device state flag", new Object[0]);
            return Collections.emptyList();
        }
    }

    public static DeviceInfo getDeviceInfo(String str) {
        if (str == null) {
            HiLog.error(TAG, "invalid device network id", new Object[0]);
            return null;
        }
        try {
            return DeviceManagerServiceProxy.getInstance().getDeviceInfo(str);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "failed to get device list", new Object[0]);
            return null;
        }
    }

    public static List<AbilityInfo> queryRemoteAbilityByIntent(Intent intent) {
        if (intent == null) {
            HiLog.error(TAG, "queryRemoteAbilityByIntent intent is null", new Object[0]);
            return Collections.emptyList();
        }
        try {
            return DeviceManagerServiceProxy.getInstance().queryRemoteAbilityByIntent(intent);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "failed to query remote ability list", new Object[0]);
            return Collections.emptyList();
        }
    }

    public static void initDistributedEnvironment(String str, IInitCallback iInitCallback) throws RemoteException {
        if (str == null || iInitCallback == null) {
            throw new IllegalArgumentException("initDistributedEnvironment parameter is null");
        }
        DeviceManagerServiceProxy.getInstance().initDistributedEnvironment(str, DistributedInitCallbackDelegate.getOrCreate(iInitCallback));
    }

    public static void unInitDistributedEnvironment(String str, IInitCallback iInitCallback) throws RemoteException {
        if (str == null || iInitCallback == null) {
            throw new IllegalArgumentException("unInitDistributedEnvironment parameter is null");
        }
        if (DeviceManagerServiceProxy.getInstance().unInitDistributedEnvironment(str, DistributedInitCallbackDelegate.get(iInitCallback))) {
            DistributedInitCallbackDelegate.remove(iInitCallback);
        }
    }
}
