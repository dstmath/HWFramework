package com.huawei.distributedgw;

import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.distributedgw.IDistributedGatewayService;

public class DistributedGatewayManager {
    private static final String REMOTE_SERVICE_NAME = IDistributedGatewayService.class.getSimpleName();
    private final IDistributedGatewayService mService = IDistributedGatewayService.Stub.asInterface(ServiceManager.getService(REMOTE_SERVICE_NAME));

    public static DistributedGatewayManager getInstance() {
        return new DistributedGatewayManager();
    }

    private void checkParams(InternetSharingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Param error. request null.");
        } else if (request.getDeviceType() == 0) {
            throw new IllegalArgumentException("Param error. Must specify a valid device type.");
        } else if (request.getDeviceType() != 2) {
            throw new IllegalArgumentException("Param error. Unsupported device type.");
        }
    }

    public boolean enableInternetSharing(InternetSharingRequest request) {
        try {
            checkParams(request);
            return this.mService.enableInternetSharing(request);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean disableInternetSharing(InternetSharingRequest request) {
        try {
            checkParams(request);
            return this.mService.disableInternetSharing(request);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isInternetSharing(InternetSharingRequest request) {
        try {
            checkParams(request);
            return this.mService.isInternetSharing(request);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private DistributedGatewayManager() {
        if (this.mService == null) {
            throw new IllegalStateException("Failed to find IDistributedGatewayService by name [" + REMOTE_SERVICE_NAME + "]");
        }
    }
}
