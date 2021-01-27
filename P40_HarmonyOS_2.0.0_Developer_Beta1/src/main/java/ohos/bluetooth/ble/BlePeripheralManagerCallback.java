package ohos.bluetooth.ble;

public abstract class BlePeripheralManagerCallback {
    public void connectionStateChangeEvent(BlePeripheralDevice blePeripheralDevice, int i, int i2, int i3, int i4) {
    }

    public void executeWriteEvent(BlePeripheralDevice blePeripheralDevice, int i, boolean z) {
    }

    public void mtuUpdateEvent(BlePeripheralDevice blePeripheralDevice, int i) {
    }

    public void notificationSentEvent(BlePeripheralDevice blePeripheralDevice, int i) {
    }

    public void receiveCharacteristicReadEvent(BlePeripheralDevice blePeripheralDevice, int i, int i2, GattCharacteristic gattCharacteristic) {
    }

    public void receiveCharacteristicWriteEvent(BlePeripheralDevice blePeripheralDevice, int i, GattCharacteristic gattCharacteristic, boolean z, boolean z2, int i2, byte[] bArr) {
    }

    public void receiveDescriptorReadEvent(BlePeripheralDevice blePeripheralDevice, int i, int i2, GattDescriptor gattDescriptor) {
    }

    public void receiveDescriptorWriteRequestEvent(BlePeripheralDevice blePeripheralDevice, int i, GattDescriptor gattDescriptor, boolean z, boolean z2, int i2, byte[] bArr) {
    }

    public void serviceAddedEvent(int i, GattService gattService) {
    }
}
