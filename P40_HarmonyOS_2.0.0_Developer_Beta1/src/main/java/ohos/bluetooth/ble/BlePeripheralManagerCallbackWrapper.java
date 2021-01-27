package ohos.bluetooth.ble;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class BlePeripheralManagerCallbackWrapper extends BlePeripheralManagerCallbackSkeleton {
    private static final int AUTHENTICATION_MITM = 2;
    private static final int AUTHENTICATION_NONE = 0;
    private static final int AUTHENTICATION_NO_MITM = 1;
    private static final int AUTH_RETRY_STATE_IDLE = 0;
    private static final int AUTH_RETRY_STATE_MITM = 2;
    private static final int AUTH_RETRY_STATE_NO_MITM = 1;
    private static final int GATT_INSUFFICIENT_AUTHENTICATION = 5;
    private static final int GATT_INSUFFICIENT_ENCRYPTION = 15;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BlePeripheralManagerCallbackWrapper");
    private BlePeripheralManagerCallback mAppCallBack;
    private BlePeripheralManager mPeripheralManager;

    public BlePeripheralManagerCallbackWrapper(BlePeripheralManagerCallback blePeripheralManagerCallback, BlePeripheralManager blePeripheralManager, String str) {
        super(str);
        this.mAppCallBack = blePeripheralManagerCallback;
        this.mPeripheralManager = blePeripheralManager;
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralManagerCallback
    public void characteristicReadRequestEvent(String str, int i, int i2, boolean z, int i3) {
        BlePeripheralDevice blePeripheralDevice = new BlePeripheralDevice(str);
        BlePeripheralManager blePeripheralManager = this.mPeripheralManager;
        if (blePeripheralManager == null) {
            HiLog.warn(TAG, "characteristicReadRequestEvent: peripheral manager is null", new Object[0]);
            return;
        }
        Optional<GattCharacteristic> characteristicByHandle = getCharacteristicByHandle(i3, blePeripheralManager.getServices());
        if (!characteristicByHandle.isPresent()) {
            HiLog.warn(TAG, "characteristicReadRequestEvent %{public}d", new Object[]{Integer.valueOf(i3)});
        } else {
            this.mAppCallBack.receiveCharacteristicReadEvent(blePeripheralDevice, i, i2, characteristicByHandle.get());
        }
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralManagerCallback
    public void characteristicWriteRequestEvent(String str, int i, int i2, int i3, boolean z, boolean z2, int i4, byte[] bArr) {
        BlePeripheralDevice blePeripheralDevice = new BlePeripheralDevice(str);
        BlePeripheralManager blePeripheralManager = this.mPeripheralManager;
        if (blePeripheralManager == null) {
            HiLog.warn(TAG, "characteristicWriteRequestEvent: peripheral manager is null", new Object[0]);
            return;
        }
        Optional<GattCharacteristic> characteristicByHandle = getCharacteristicByHandle(i4, blePeripheralManager.getServices());
        if (!characteristicByHandle.isPresent()) {
            HiLog.warn(TAG, "characteristicWriteRequestEvent", new Object[0]);
        } else {
            this.mAppCallBack.receiveCharacteristicWriteEvent(blePeripheralDevice, i, characteristicByHandle.get(), z, z2, i2, bArr);
        }
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralManagerCallback
    public void connectionStateUpdateEvent(String str, int i, int i2, int i3, int i4) {
        this.mAppCallBack.connectionStateChangeEvent(new BlePeripheralDevice(str), i, i2, i3, i4);
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralManagerCallback
    public void descriptorReadRequestEvent(String str, int i, int i2, boolean z, int i3) {
        BlePeripheralDevice blePeripheralDevice = new BlePeripheralDevice(str);
        BlePeripheralManager blePeripheralManager = this.mPeripheralManager;
        if (blePeripheralManager == null) {
            HiLog.warn(TAG, "descriptorReadRequestEvent: peripheral manager is null", new Object[0]);
            return;
        }
        Optional<GattDescriptor> gattDescriptorByHandle = getGattDescriptorByHandle(i3, blePeripheralManager.getServices());
        if (!gattDescriptorByHandle.isPresent()) {
            HiLog.warn(TAG, "descriptorReadRequestEvent invalid input", new Object[0]);
        } else {
            this.mAppCallBack.receiveDescriptorReadEvent(blePeripheralDevice, i, i2, gattDescriptorByHandle.get());
        }
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralManagerCallback
    public void descriptorWriteRequestEvent(String str, int i, int i2, int i3, boolean z, boolean z2, int i4, byte[] bArr) {
        BlePeripheralDevice blePeripheralDevice = new BlePeripheralDevice(str);
        BlePeripheralManager blePeripheralManager = this.mPeripheralManager;
        if (blePeripheralManager == null) {
            HiLog.warn(TAG, "descriptorWriteRequestEvent: peripheral manager is null", new Object[0]);
            return;
        }
        Optional<GattDescriptor> gattDescriptorByHandle = getGattDescriptorByHandle(i4, blePeripheralManager.getServices());
        if (!gattDescriptorByHandle.isPresent()) {
            HiLog.warn(TAG, "descriptorWriteRequestEvent invalid input", new Object[0]);
        } else {
            this.mAppCallBack.receiveDescriptorWriteRequestEvent(blePeripheralDevice, i, gattDescriptorByHandle.get(), z, z2, i2, bArr);
        }
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralManagerCallback
    public void executeWriteEvent(String str, int i, boolean z) {
        this.mAppCallBack.executeWriteEvent(new BlePeripheralDevice(str), i, z);
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralManagerCallback
    public void mtuUpdateEvent(String str, int i) {
        this.mAppCallBack.mtuUpdateEvent(new BlePeripheralDevice(str), i);
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralManagerCallback
    public void notificationSentEvent(String str, int i) {
        this.mAppCallBack.notificationSentEvent(new BlePeripheralDevice(str), i);
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralManagerCallback
    public void serviceAddedEvent(int i, GattService gattService) {
        BlePeripheralManager blePeripheralManager = this.mPeripheralManager;
        if (blePeripheralManager == null) {
            HiLog.warn(TAG, "peripheral manager is null", new Object[0]);
        } else if (blePeripheralManager.getPendingService() == null) {
            HiLog.warn(TAG, "pending service is null", new Object[0]);
        } else {
            GattService pendingService = this.mPeripheralManager.getPendingService();
            this.mPeripheralManager.setPendingService(null);
            pendingService.setHandle(gattService.getHandle());
            List<GattCharacteristic> characteristics = pendingService.getCharacteristics();
            List<GattCharacteristic> characteristics2 = gattService.getCharacteristics();
            for (int i2 = 0; i2 < characteristics2.size(); i2++) {
                GattCharacteristic gattCharacteristic = characteristics.get(i2);
                GattCharacteristic gattCharacteristic2 = characteristics2.get(i2);
                gattCharacteristic.setHandle(gattCharacteristic2.getHandle());
                List<GattDescriptor> descriptors = gattCharacteristic.getDescriptors();
                List<GattDescriptor> descriptors2 = gattCharacteristic2.getDescriptors();
                for (int i3 = 0; i3 < descriptors2.size(); i3++) {
                    descriptors.get(i3).setHandle(descriptors2.get(i3).getHandle());
                }
            }
            this.mPeripheralManager.addToServiceList(pendingService);
            this.mAppCallBack.serviceAddedEvent(i, pendingService);
        }
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralManagerCallback
    public void serverRegisteredEvent(int i, int i2) {
        BlePeripheralManager blePeripheralManager = this.mPeripheralManager;
        if (blePeripheralManager == null) {
            HiLog.error(TAG, "the corresponding manager of current wrapper is null %{public}d", new Object[]{Integer.valueOf(i)});
        } else {
            blePeripheralManager.registerServerIf(i2);
        }
    }

    private Optional<GattCharacteristic> getCharacteristicByHandle(int i, List<GattService> list) {
        if (list == null) {
            return Optional.empty();
        }
        for (GattService gattService : list) {
            Iterator<GattCharacteristic> it = gattService.getCharacteristics().iterator();
            while (true) {
                if (it.hasNext()) {
                    GattCharacteristic next = it.next();
                    if (next.getHandle() == i) {
                        return Optional.ofNullable(next);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<GattDescriptor> getGattDescriptorByHandle(int i, List<GattService> list) {
        if (list == null) {
            return Optional.empty();
        }
        for (GattService gattService : list) {
            Iterator<GattCharacteristic> it = gattService.getCharacteristics().iterator();
            while (true) {
                if (it.hasNext()) {
                    Iterator<GattDescriptor> it2 = it.next().getDescriptors().iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            GattDescriptor next = it2.next();
                            if (next.getHandle() == i) {
                                return Optional.ofNullable(next);
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }
}
