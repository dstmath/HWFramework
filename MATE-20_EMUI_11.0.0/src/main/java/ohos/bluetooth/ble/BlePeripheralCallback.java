package ohos.bluetooth.ble;

public abstract class BlePeripheralCallback {
    public void mtuUpdateEvent(int i, int i2) {
    }

    public void onCharacteristicChanged(GattCharacteristic gattCharacteristic) {
    }

    public void onCharacteristicReadResult(GattCharacteristic gattCharacteristic, int i) {
    }

    public void onCharacteristicWriteResult(GattCharacteristic gattCharacteristic, int i) {
    }

    public void onConnectionStateChanged(int i) {
    }

    public void onDescriptorReadResult(GattDescriptor gattDescriptor, int i) {
    }

    public void onDescriptorWriteResult(GattDescriptor gattDescriptor, int i) {
    }

    public void onServicesDiscovered(int i) {
    }

    public void readRemoteRssiEvent(int i, int i2) {
    }
}
