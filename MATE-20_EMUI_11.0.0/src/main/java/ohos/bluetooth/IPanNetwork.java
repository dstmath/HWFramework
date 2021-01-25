package ohos.bluetooth;

import java.util.List;
import ohos.rpc.IRemoteBroker;

public interface IPanNetwork extends IRemoteBroker {
    boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice);

    List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr);
}
