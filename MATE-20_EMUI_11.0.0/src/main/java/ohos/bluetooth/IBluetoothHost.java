package ohos.bluetooth;

import java.util.List;
import java.util.Optional;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.utils.SequenceUuid;

public interface IBluetoothHost extends IRemoteBroker {
    boolean bluetoothFactoryReset();

    boolean cancelBtDiscovery();

    boolean cancelPairing(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean disableBt(String str);

    boolean enableBt(String str);

    int getBleCapabilities();

    int getBleMaxAdvertisingDataLength();

    int getBtConnectionState();

    long getBtDiscoveryEndMillis();

    int getBtScanMode();

    int getBtState();

    int getDeviceBatteryLevel(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getDeviceType(BluetoothRemoteDevice bluetoothRemoteDevice);

    SequenceUuid[] getDeviceUuids(BluetoothRemoteDevice bluetoothRemoteDevice);

    Optional<String> getLocalAddress();

    Optional<String> getLocalName();

    int getMaxNumConnectedAudioDevices();

    int getMessagePermission(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getPairState(BluetoothRemoteDevice bluetoothRemoteDevice);

    List<BluetoothRemoteDevice> getPairedDevices();

    int getPhonebookPermission(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getProfileConnState(int i);

    List<Integer> getProfileList();

    Optional<String> getRemoteAlias(BluetoothRemoteDevice bluetoothRemoteDevice);

    int getRemoteDeviceClass(BluetoothRemoteDevice bluetoothRemoteDevice);

    Optional<String> getRemoteName(BluetoothRemoteDevice bluetoothRemoteDevice);

    Optional<IRemoteObject> getSaProfileProxy(int i);

    SequenceUuid[] getlocalSupportedUuids();

    boolean isAclConnected(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean isAclEncrypted(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean isBondedFromLocal(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean isBtDiscovering();

    boolean removePair(BluetoothRemoteDevice bluetoothRemoteDevice);

    boolean setBtScanMode(int i, int i2);

    boolean setDevicePairingConfirmation(BluetoothRemoteDevice bluetoothRemoteDevice, boolean z);

    boolean setDevicePin(BluetoothRemoteDevice bluetoothRemoteDevice, byte[] bArr);

    boolean setLocalName(String str);

    boolean setMessagePermission(BluetoothRemoteDevice bluetoothRemoteDevice, int i);

    boolean setPhonebookPermission(BluetoothRemoteDevice bluetoothRemoteDevice, int i);

    boolean setRemoteAlias(BluetoothRemoteDevice bluetoothRemoteDevice, String str);

    boolean startBtDiscovery(String str);

    boolean startPair(BluetoothRemoteDevice bluetoothRemoteDevice);
}
