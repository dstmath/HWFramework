package com.huawei.internal.telephony;

import android.telephony.INetworkServiceCallback;
import android.telephony.NetworkService;

public class CellularNetworkServiceEx {
    public static final int NETWORK_SERVICE_CREATE_NETWORK_SERVICE_PROVIDER = 1;
    public static final int NETWORK_SERVICE_GET_REGISTRATION_INFO = 4;
    public static final int NETWORK_SERVICE_INDICATION_NETWORK_INFO_CHANGED = 7;
    public static final int NETWORK_SERVICE_REGISTER_FOR_INFO_CHANGE = 5;
    public static final int NETWORK_SERVICE_REMOVE_ALL_NETWORK_SERVICE_PROVIDERS = 3;
    public static final int NETWORK_SERVICE_REMOVE_NETWORK_SERVICE_PROVIDER = 2;
    public static final int NETWORK_SERVICE_UNREGISTER_FOR_INFO_CHANGE = 6;

    private CellularNetworkServiceEx() {
    }

    public static void createNetworkServiceProvider(NetworkService service, int slotIndex) {
        if (service != null) {
            service.getHandler().obtainMessage(1, slotIndex, 0, null).sendToTarget();
        }
    }

    public static void removeNetworkServiceProvider(NetworkService service, int slotIndex) {
        if (service != null) {
            service.getHandler().obtainMessage(2, slotIndex, 0, null).sendToTarget();
        }
    }

    public static void requestNetworkRegistrationInfo(NetworkService service, int slotIndex, int domain, INetworkServiceCallback callback) {
        if (service != null) {
            service.getHandler().obtainMessage(4, slotIndex, domain, callback).sendToTarget();
        }
    }

    public static void registerForNetworkRegistrationInfoChanged(NetworkService service, int slotIndex, INetworkServiceCallback callback) {
        if (service != null) {
            service.getHandler().obtainMessage(5, slotIndex, 0, callback).sendToTarget();
        }
    }

    public static void unregisterForNetworkRegistrationInfoChanged(NetworkService service, int slotIndex, INetworkServiceCallback callback) {
        if (service != null) {
            service.getHandler().obtainMessage(6, slotIndex, 0, callback).sendToTarget();
        }
    }
}
