package android.bluetooth.le;

public abstract class AdvertisingSetCallback {
    public static final int ADVERTISE_FAILED_ALREADY_STARTED = 3;
    public static final int ADVERTISE_FAILED_DATA_TOO_LARGE = 1;
    public static final int ADVERTISE_FAILED_FEATURE_UNSUPPORTED = 5;
    public static final int ADVERTISE_FAILED_INTERNAL_ERROR = 4;
    public static final int ADVERTISE_FAILED_TOO_MANY_ADVERTISERS = 2;
    public static final int ADVERTISE_SUCCESS = 0;

    public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
    }

    public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
    }

    public void onAdvertisingEnabled(AdvertisingSet advertisingSet, boolean enable, int status) {
    }

    public void onAdvertisingDataSet(AdvertisingSet advertisingSet, int status) {
    }

    public void onScanResponseDataSet(AdvertisingSet advertisingSet, int status) {
    }

    public void onAdvertisingParametersUpdated(AdvertisingSet advertisingSet, int txPower, int status) {
    }

    public void onPeriodicAdvertisingParametersUpdated(AdvertisingSet advertisingSet, int status) {
    }

    public void onPeriodicAdvertisingDataSet(AdvertisingSet advertisingSet, int status) {
    }

    public void onPeriodicAdvertisingEnabled(AdvertisingSet advertisingSet, boolean enable, int status) {
    }

    public void onOwnAddressRead(AdvertisingSet advertisingSet, int addressType, String address) {
    }
}
