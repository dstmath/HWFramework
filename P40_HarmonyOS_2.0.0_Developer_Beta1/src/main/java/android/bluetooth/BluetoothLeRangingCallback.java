package android.bluetooth;

import android.bluetooth.le.ScanResult;

public class BluetoothLeRangingCallback implements LeRangingCallback {
    public static final String TAG = "BluetoothLeRangingCallback";

    @Override // android.bluetooth.LeRangingCallback
    public void onRangeResult(LeRangingResult rangingResult, ScanResult scanResult) {
    }

    @Override // android.bluetooth.LeRangingCallback
    public void onStartSucess(LeRangeFeeding feeder) {
    }

    @Override // android.bluetooth.LeRangingCallback
    public void onStartFailure(int errCode) {
    }

    @Override // android.bluetooth.LeRangingCallback
    public void onStopSucess() {
    }

    @Override // android.bluetooth.LeRangingCallback
    public void onStopFailure(int errCode) {
    }
}
