package ohos.bluetooth;

import java.util.List;
import ohos.rpc.IRemoteBroker;

public interface IPbapServer extends IRemoteBroker {
    boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice);

    List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr);
}
