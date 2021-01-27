package ohos.bluetooth.ble;

public abstract class BleAdvertiseCallback {
    public static final int RESULT_FAIL_ALREADY_STARTED = 3;
    public static final int RESULT_FAIL_DATA_ILLEGAL = 1;
    public static final int RESULT_FAIL_ERROR = 4;
    public static final int RESULT_FAIL_FEATURE_UNSUPPORTED = 5;
    public static final int RESULT_FAIL_TOO_MANY_ADVERTISERS = 2;
    public static final int RESULT_SUCC = 0;

    public void startResultEvent(int i) {
    }
}
