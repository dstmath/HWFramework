package android.app;

import android.os.RemoteException;
import android.service.vr.IVrManager;

public class VrManager {
    private final IVrManager mService;

    public VrManager(IVrManager service) {
        this.mService = service;
    }

    public void setPersistentVrModeEnabled(boolean enabled) {
        try {
            this.mService.setPersistentVrModeEnabled(enabled);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void setVr2dDisplayProperties(Vr2dDisplayProperties vr2dDisplayProp) {
        try {
            this.mService.setVr2dDisplayProperties(vr2dDisplayProp);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }
}
