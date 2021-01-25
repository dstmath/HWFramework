package ohos.bluetooth.ble;

import ohos.rpc.IRemoteBroker;

public interface IBleAdvertiseCallback extends IRemoteBroker {
    void onAdvertisingSetStarted(int i);
}
