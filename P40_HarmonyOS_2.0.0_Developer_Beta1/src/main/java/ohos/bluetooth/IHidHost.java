package ohos.bluetooth;

import java.util.List;
import ohos.rpc.IRemoteBroker;

public interface IHidHost extends IRemoteBroker {
    boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice);

    List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr);
}
