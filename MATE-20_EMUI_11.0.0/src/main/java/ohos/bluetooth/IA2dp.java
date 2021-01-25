package ohos.bluetooth;

import java.util.List;
import java.util.Optional;
import ohos.rpc.IRemoteBroker;

public interface IA2dp extends IRemoteBroker {
    boolean connectSinkDevice(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean connectSourceDevice(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean disconnectSinkDevice(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean disconnectSourceDevice(BluetoothRemoteDevice bluetoothRemoteDevice);

    Optional<BluetoothRemoteDevice> getActiveDeviceForSource();

    Optional<A2dpCodecStatus> getCodecStatusForSource(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getConnectStrategyForSink(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getConnectStrategyForSource(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getDeviceStateForSink(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getDeviceStateForSource(BluetoothRemoteDevice bluetoothRemoteDevice);

    List<BluetoothRemoteDevice> getDevicesByStatesForSink(int[] iArr);

    List<BluetoothRemoteDevice> getDevicesByStatesForSource(int[] iArr);

    int getOptionalCodecsOptionForSource(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getOptionalCodecsSupportStateForSource(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getPlayingStateForSink(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getPlayingStateForSource(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean setActiveDeviceForSource(BluetoothRemoteDevice bluetoothRemoteDevice);

    void setCodecPreferenceForSource(BluetoothRemoteDevice bluetoothRemoteDevice, A2dpCodecInfo a2dpCodecInfo);

    boolean setConnectStrategyForSink(BluetoothRemoteDevice bluetoothRemoteDevice, int i);

    boolean setConnectStrategyForSource(BluetoothRemoteDevice bluetoothRemoteDevice, int i);

    void setOptionalCodecsOptionForSource(BluetoothRemoteDevice bluetoothRemoteDevice, int i);

    void switchOptionalCodecsForSource(BluetoothRemoteDevice bluetoothRemoteDevice, boolean z);
}
