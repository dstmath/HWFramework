package android.os;

import android.content.Context;

public class DeviceIdleManager {
    private final Context mContext;
    private final IDeviceIdleController mService;

    public DeviceIdleManager(Context context, IDeviceIdleController service) {
        this.mContext = context;
        this.mService = service;
    }

    public String[] getSystemPowerWhitelistExceptIdle() {
        try {
            return this.mService.getSystemPowerWhitelistExceptIdle();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return new String[0];
        }
    }

    public String[] getSystemPowerWhitelist() {
        try {
            return this.mService.getSystemPowerWhitelist();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return new String[0];
        }
    }
}
