package ohos.msdp.devicestatus;

public class HwMSDPDeviceStatusConstant {
    private static final String[] DEFAULT_SUPPORTS = {MSDP_DEVICETSTATUS_TYPE_HIGH_STILL, MSDP_DEVICETSTATUS_TYPE_FINE_STILL, MSDP_DEVICETSTATUS_TYPE_COARSE_STILL, MSDP_DEVICETSTATUS_TYPE_CELL_CHANGED};
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    public static final String MSDP_DEVICETSTATUS_TYPE_CELL_CHANGED = "android.msdp.device_status.cell_changed";
    public static final String MSDP_DEVICETSTATUS_TYPE_COARSE_STILL = "android.msdp.device_status.coarse_still";
    public static final String MSDP_DEVICETSTATUS_TYPE_FINE_STILL = "android.msdp.device_status.fine_still";
    public static final String MSDP_DEVICETSTATUS_TYPE_HIGH_STILL = "android.msdp.device_status.high_still";
    private static final String[] SUPPORTS = {MSDP_DEVICETSTATUS_TYPE_HIGH_STILL, MSDP_DEVICETSTATUS_TYPE_FINE_STILL, MSDP_DEVICETSTATUS_TYPE_COARSE_STILL, MSDP_DEVICETSTATUS_TYPE_CELL_CHANGED};
    public static final String TYPE_CAR_BLUETOOTH = "com.huawei.msdp.device_status.car_bluetooth";
    public static final String TYPE_STILL_STATUS = "msdp.devicestatus_type_still_status";
    public static final String TYPE_UNKNOWN = "android.msdp.device_status.type.unknown";

    public static String[] getSUPPORTS() {
        return (String[]) SUPPORTS.clone();
    }

    public static String[] getDefaultSupports() {
        return (String[]) DEFAULT_SUPPORTS.clone();
    }
}
