package com.huawei.distributedgw;

import com.huawei.distributedgw.InternetSharingRequest;

public class DistributedGatewayManagerEx {
    public static final int DEVICE_PC = 2;

    public static boolean enableInternetSharing(int deviceType, String entryIfaceName) {
        return DistributedGatewayManager.getInstance().enableInternetSharing(new InternetSharingRequest.Builder().setDeviceType(deviceType).setEntryIfaceName(entryIfaceName).build());
    }

    public static boolean disableInternetSharing(int deviceType, String entryIfaceName) {
        return DistributedGatewayManager.getInstance().disableInternetSharing(new InternetSharingRequest.Builder().setDeviceType(deviceType).setEntryIfaceName(entryIfaceName).build());
    }

    public static boolean isInternetSharing(int deviceType, String entryIfaceName) {
        return DistributedGatewayManager.getInstance().isInternetSharing(new InternetSharingRequest.Builder().setDeviceType(deviceType).setEntryIfaceName(entryIfaceName).build());
    }
}
