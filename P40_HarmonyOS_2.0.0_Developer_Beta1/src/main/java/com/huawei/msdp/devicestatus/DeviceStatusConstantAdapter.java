package com.huawei.msdp.devicestatus;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DeviceStatusConstantAdapter {
    public static final int EVENT_TYPE_ENTER = 1;
    public static final String MSDP_DEVICESTATUS_TYPE_STILL_STATUS = "msdp.devicestatus_type_still_status";
    public static final String MSDP_DEVICESTATUS_TYPE_UNKNOWN = "android.msdp.device_status.type.unknown";
    public static final String MSDP_DEVICETSTATUS_TYPE_COARSE_STILL = "android.msdp.device_status.coarse_still";
    public static final String MSDP_DEVICETSTATUS_TYPE_FINE_STILL = "android.msdp.device_status.fine_still";
    public static final String MSDP_DEVICETSTATUS_TYPE_HIGH_STILL = "android.msdp.device_status.high_still";
    public static final String MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_FAST_WALKING = "android.msdp.movement_of_fast_walking";
    public static final String MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_OTHER = "android.msdp.movement_of_other";
    public static final String MSDP_DEVICETSTATUS_TYPE_MOVEMENT_OF_WALKING = "android.msdp.movement_of_walking";

    private DeviceStatusConstantAdapter() {
    }
}
