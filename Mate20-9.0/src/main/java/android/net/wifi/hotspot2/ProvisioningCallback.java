package android.net.wifi.hotspot2;

public abstract class ProvisioningCallback {
    public static final int OSU_FAILURE_AP_CONNECTION = 1;
    public static final int OSU_FAILURE_PROVIDER_VERIFICATION = 5;
    public static final int OSU_FAILURE_PROVISIONING_ABORTED = 6;
    public static final int OSU_FAILURE_PROVISIONING_NOT_AVAILABLE = 7;
    public static final int OSU_FAILURE_SERVER_CONNECTION = 3;
    public static final int OSU_FAILURE_SERVER_URL_INVALID = 2;
    public static final int OSU_FAILURE_SERVER_VALIDATION = 4;
    public static final int OSU_STATUS_AP_CONNECTED = 2;
    public static final int OSU_STATUS_AP_CONNECTING = 1;
    public static final int OSU_STATUS_PROVIDER_VERIFIED = 5;
    public static final int OSU_STATUS_SERVER_CONNECTED = 3;
    public static final int OSU_STATUS_SERVER_VALIDATED = 4;

    public abstract void onProvisioningFailure(int i);

    public abstract void onProvisioningStatus(int i);
}
