package ohos.bluetooth;

import java.util.List;
import ohos.rpc.IRemoteBroker;

public interface IPbapClient extends IRemoteBroker {
    boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getConnectStrategy(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice);

    List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr);

    boolean setConnectStrategy(BluetoothRemoteDevice bluetoothRemoteDevice, int i);
}
