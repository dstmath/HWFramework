package com.huawei.android.bluetooth;

import android.bluetooth.le.ScanResult;

public interface LeRangingCallback {
    void onRangeResult(LeRangingResult leRangingResult, ScanResult scanResult);

    void onStartFailure(int i);

    void onStartSucess(LeRangeFeeding leRangeFeeding);

    void onStopFailure(int i);

    void onStopSucess();
}
