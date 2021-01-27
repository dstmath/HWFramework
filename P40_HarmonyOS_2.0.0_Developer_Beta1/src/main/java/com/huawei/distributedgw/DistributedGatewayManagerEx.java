package com.huawei.distributedgw;

public class DistributedGatewayManagerEx {
    public static final int CONNECTED = 1;
    public static final int DEVICE_HICAR = 6;
    public static final int DEVICE_PAD = 3;
    public static final int DEVICE_PC = 2;
    public static final int DEVICE_PHONE = 1;
    public static final int DEVICE_TVSET = 5;
    public static final int DISCONNECTED = 0;
    private static final DistributedGatewayManager INSTANCE = new DistributedGatewayManager();
    private static final String TAG = DistributedGatewayManagerEx.class.getSimpleName();

    @Deprecated
    public static boolean enableInternetSharing(int deviceType, String entryInterfaceName) {
        return INSTANCE.enableInternetSharing(deviceType, entryInterfaceName);
    }

    public static boolean enableInternetSharing(InternetSharingRequestEx requestEx) {
        return INSTANCE.enableInternetSharing(requestEx);
    }

    @Deprecated
    public static boolean disableInternetSharing(int deviceType, String entryInterfaceName) {
        return INSTANCE.disableInternetSharing(deviceType, entryInterfaceName);
    }

    public static boolean disableInternetSharing(InternetSharingRequestEx requestEx) {
        return INSTANCE.disableInternetSharing(requestEx);
    }

    @Deprecated
    public static boolean isInternetSharing(int deviceType, String entryInterfaceName) {
        return INSTANCE.isInternetSharing(deviceType, entryInterfaceName);
    }

    public static boolean isInternetSharing(InternetSharingRequestEx requestEx) {
        return INSTANCE.isInternetSharing(requestEx);
    }

    public static boolean enableInternetBorrowing(InternetBorrowingRequestEx requestEx) {
        return INSTANCE.enableInternetBorrowing(requestEx);
    }

    public static boolean disableInternetBorrowing(InternetBorrowingRequestEx requestEx) {
        return INSTANCE.disableInternetBorrowing(requestEx);
    }

    public static boolean isInternetBorrowing(InternetBorrowingRequestEx requestEx) {
        return INSTANCE.isInternetBorrowing(requestEx);
    }

    public static boolean regStateCallback(DistributedGatewayStateCallback callback) {
        return INSTANCE.regStateCallback(callback);
    }

    public static boolean unregStateCallback(DistributedGatewayStateCallback callback) {
        return INSTANCE.unregStateCallback(callback);
    }
}
