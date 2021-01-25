package ohos.bluetooth;

import java.util.List;
import ohos.rpc.IRemoteBroker;

public interface IHandsFreeAg extends IRemoteBroker {
    boolean closeVoiceRecognition(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean connectSco();

    boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean disconnectSco();

    List<BluetoothRemoteDevice> getConnectedDevices();

    int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice);

    List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr);

    int getScoState(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean openVoiceRecognition(BluetoothRemoteDevice bluetoothRemoteDevice);
}
