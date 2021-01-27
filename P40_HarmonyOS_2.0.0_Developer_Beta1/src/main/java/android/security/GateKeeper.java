package android.security;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.service.gatekeeper.IGateKeeperService;

public abstract class GateKeeper {
    public static final long INVALID_SECURE_USER_ID = 0;

    private GateKeeper() {
    }

    public static IGateKeeperService getService() {
        IGateKeeperService service = IGateKeeperService.Stub.asInterface(ServiceManager.getService(Context.GATEKEEPER_SERVICE));
        if (service != null) {
            return service;
        }
        throw new IllegalStateException("Gatekeeper service not available");
    }

    @UnsupportedAppUsage
    public static long getSecureUserId() throws IllegalStateException {
        try {
            return getService().getSecureUserId(UserHandle.myUserId());
        } catch (RemoteException e) {
            throw new IllegalStateException("Failed to obtain secure user ID from gatekeeper", e);
        }
    }
}
