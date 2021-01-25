package ohos.bluetooth.ble;

public abstract class BlePeripheralManagerCallback {
    /* access modifiers changed from: package-private */
    public void onCharacteristicReadRequest(BlePeripheralDevice blePeripheralDevice, int i, int i2, GattCharacteristic gattCharacteristic) {
    }

    /* access modifiers changed from: package-private */
    public void onCharacteristicWriteRequest(BlePeripheralDevice blePeripheralDevice, int i, GattCharacteristic gattCharacteristic, boolean z, boolean z2, int i2, byte[] bArr) {
    }

    /* access modifiers changed from: package-private */
    public void onConnectionUpdated(BlePeripheralDevice blePeripheralDevice, int i, int i2, int i3, int i4) {
    }

    /* access modifiers changed from: package-private */
    public void onDescriptorReadRequest(BlePeripheralDevice blePeripheralDevice, int i, int i2, GattDescriptor gattDescriptor) {
    }

    /* access modifiers changed from: package-private */
    public void onDescriptorWriteRequest(BlePeripheralDevice blePeripheralDevice, int i, GattDescriptor gattDescriptor, boolean z, boolean z2, int i2, byte[] bArr) {
    }

    /* access modifiers changed from: package-private */
    public void onExecuteWrite(BlePeripheralDevice blePeripheralDevice, int i, boolean z) {
    }

    /* access modifiers changed from: package-private */
    public void onMtuChanged(BlePeripheralDevice blePeripheralDevice, int i) {
    }

    /* access modifiers changed from: package-private */
    public void onNotificationSent(BlePeripheralDevice blePeripheralDevice, int i) {
    }

    /* access modifiers changed from: package-private */
    public void onServiceAdded(int i, GattService gattService) {
    }
}
