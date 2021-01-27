package ohos.bluetooth.ble;

public abstract class BlePeripheralCallback {
    public void characteristicChangedEvent(GattCharacteristic gattCharacteristic) {
    }

    public void characteristicReadEvent(GattCharacteristic gattCharacteristic, int i) {
    }

    public void characteristicWriteEvent(GattCharacteristic gattCharacteristic, int i) {
    }

    public void connectionStateChangeEvent(int i) {
    }

    public void descriptorReadEvent(GattDescriptor gattDescriptor, int i) {
    }

    public void descriptorWriteEvent(GattDescriptor gattDescriptor, int i) {
    }

    public void mtuUpdateEvent(int i, int i2) {
    }

    public void readRemoteRssiEvent(int i, int i2) {
    }

    public void servicesDiscoveredEvent(int i) {
    }
}
