package ohos.bluetooth;

import java.util.List;
import java.util.Optional;
import ohos.rpc.IRemoteBroker;

public interface IHandsFreeHf extends IRemoteBroker {
    boolean acceptIncomingCall(BluetoothRemoteDevice bluetoothRemoteDevice, int i);

    boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean connectSco(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean disconnectSco(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean finishActiveCall(BluetoothRemoteDevice bluetoothRemoteDevice, HandsFreeUnitCall handsFreeUnitCall);

    int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice);

    List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr);

    int getScoState(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean holdActiveCall(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean rejectIncomingCall(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean sendDTMFTone(BluetoothRemoteDevice bluetoothRemoteDevice, byte b);

    Optional<HandsFreeUnitCall> startDial(BluetoothRemoteDevice bluetoothRemoteDevice, String str);
}
