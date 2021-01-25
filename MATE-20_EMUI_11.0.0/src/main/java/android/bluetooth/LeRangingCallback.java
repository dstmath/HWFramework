package android.bluetooth;

import android.bluetooth.le.ScanResult;

public interface LeRangingCallback {
    public static final String TAG = "LeRangingCallback";

    void onRangeResult(LeRangingResult leRangingResult, ScanResult scanResult);

    void onStartFailure(int i);

    void onStartSucess(LeRangeFeeding leRangeFeeding);

    void onStopFailure(int i);

    void onStopSucess();
}
