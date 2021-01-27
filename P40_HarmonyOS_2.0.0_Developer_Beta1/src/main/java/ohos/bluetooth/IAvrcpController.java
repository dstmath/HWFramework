package ohos.bluetooth;

import java.util.List;
import ohos.rpc.IRemoteBroker;

public interface IAvrcpController extends IRemoteBroker {
    int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice);

    List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr);
}
