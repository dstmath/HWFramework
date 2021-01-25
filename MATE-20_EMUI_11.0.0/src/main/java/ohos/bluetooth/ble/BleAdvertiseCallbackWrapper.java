package ohos.bluetooth.ble;

public class BleAdvertiseCallbackWrapper extends BleAdvertiseCallbackSkeleton {
    BleAdvertiseCallback mCallback;

    public BleAdvertiseCallbackWrapper(BleAdvertiseCallback bleAdvertiseCallback, String str) {
        super(str);
        this.mCallback = bleAdvertiseCallback;
    }

    @Override // ohos.bluetooth.ble.IBleAdvertiseCallback
    public void onAdvertisingSetStarted(int i) {
        this.mCallback.startResultEvent(i);
    }
}
