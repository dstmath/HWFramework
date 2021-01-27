package ohos.bluetooth.ble;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class BlePeripheralDevice {
    public static final int CONNECTION_PRIORITY_HIGH = 1;
    public static final int CONNECTION_PRIORITY_LOW = 2;
    public static final int CONNECTION_PRIORITY_NORMAL = 0;
    public static final int OPERATION_SUCC = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BlePeripheralDevice");
    private String mAddress;
    private BlePeripheralCallbackWrapper mCallbackWrapper;
    private int mClientHandle;
    private boolean mIsAutoConnect;
    private boolean mIsWriteReadBusy = false;
    private BlePeripheralProxy mProxy;
    private List<GattService> mServices;
    private final Object mWriteReadLock = new Object();

    BlePeripheralDevice(String str) {
        this.mAddress = str;
        this.mProxy = new BlePeripheralProxy();
        this.mServices = new ArrayList();
    }

    /* access modifiers changed from: package-private */
    public void setClientHandle(int i) {
        this.mClientHandle = i;
    }

    /* access modifiers changed from: package-private */
    public int getClientHandle() {
        return this.mClientHandle;
    }

    /* access modifiers changed from: package-private */
    public boolean isAutoConnect() {
        return this.mIsAutoConnect;
    }

    /* access modifiers changed from: package-private */
    public String getAddress() {
        return this.mAddress;
    }

    public boolean connect(boolean z, BlePeripheralCallback blePeripheralCallback) {
        if (blePeripheralCallback == null) {
            return false;
        }
        this.mIsAutoConnect = z;
        this.mCallbackWrapper = new BlePeripheralCallbackWrapper(this, blePeripheralCallback, this.mProxy, "ohos.bluetooth.ble.IBlePeripheralCallback");
        try {
            this.mProxy.registerClient(this.mCallbackWrapper);
            return true;
        } catch (RemoteException unused) {
            return false;
        }
    }

    public boolean discoverServices() {
        if (this.mClientHandle == 0) {
            return false;
        }
        this.mServices.clear();
        try {
            this.mProxy.discoverServices(this.mClientHandle, this.mAddress);
            return true;
        } catch (RemoteException unused) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateServiceAndFixIncluedeRelation(List<GattService> list) {
        this.mServices.addAll(list);
        for (GattService gattService : this.mServices) {
            ArrayList arrayList = new ArrayList(gattService.getIncludedServices());
            gattService.getIncludedServices().clear();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                GattService gattService2 = (GattService) it.next();
                getService(gattService2.getUuid(), gattService2.getHandle()).ifPresent(new Consumer() {
                    /* class ohos.bluetooth.ble.$$Lambda$BlePeripheralDevice$Tmm8IcLCCYsOhdhY_CaHtdMvPI */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        GattService.this.addService((GattService) obj);
                    }
                });
            }
        }
    }

    private Optional<GattService> getService(UUID uuid, int i) {
        for (GattService gattService : this.mServices) {
            if (gattService.getHandle() == i && Objects.equals(gattService.getUuid(), uuid)) {
                return Optional.ofNullable(gattService);
            }
        }
        return Optional.empty();
    }

    public List<GattService> getServices() {
        return this.mServices;
    }

    public Optional<GattService> getService(UUID uuid) {
        for (GattService gattService : this.mServices) {
            if (Objects.equals(gattService.getUuid(), uuid)) {
                return Optional.ofNullable(gattService);
            }
        }
        return Optional.empty();
    }

    public boolean disconnect() {
        int i = this.mClientHandle;
        if (i == 0) {
            return false;
        }
        try {
            this.mProxy.clientDisconnect(i, this.mAddress);
            return true;
        } catch (RemoteException unused) {
            return false;
        }
    }

    public boolean close() {
        int i = this.mClientHandle;
        if (i == 0) {
            return false;
        }
        try {
            this.mProxy.unregisterClient(i);
            this.mCallbackWrapper = null;
            this.mClientHandle = 0;
            return true;
        } catch (RemoteException unused) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void releaseWriteReadLock() {
        synchronized (this.mWriteReadLock) {
            this.mIsWriteReadBusy = false;
        }
    }

    public boolean readCharacteristic(GattCharacteristic gattCharacteristic) {
        if ((gattCharacteristic.getProperties() & 2) == 0) {
            HiLog.error(TAG, "readCharacteristic property illeagal.", new Object[0]);
            return false;
        } else if (this.mClientHandle == 0) {
            HiLog.error(TAG, "readCharacteristic mClientHandle illeagal.", new Object[0]);
            return false;
        } else if (gattCharacteristic.getService() == null) {
            HiLog.error(TAG, "readCharacteristic service illeagal.", new Object[0]);
            return false;
        } else {
            synchronized (this.mWriteReadLock) {
                if (this.mIsWriteReadBusy) {
                    HiLog.error(TAG, "readCharacteristic state illeagal.", new Object[0]);
                    return false;
                }
                this.mIsWriteReadBusy = true;
                try {
                    this.mProxy.readCharacteristic(this.mClientHandle, this.mAddress, gattCharacteristic.getHandle(), 0);
                    return true;
                } catch (RemoteException unused) {
                    releaseWriteReadLock();
                    return false;
                }
            }
        }
    }

    public boolean writeCharacteristic(GattCharacteristic gattCharacteristic) {
        if ((gattCharacteristic.getProperties() & 8) == 0 && (gattCharacteristic.getProperties() & 4) == 0) {
            HiLog.error(TAG, "writeCharacteristic property illeagal.", new Object[0]);
            return false;
        } else if (this.mClientHandle == 0) {
            HiLog.error(TAG, "writeCharacteristic mClientHandle illeagal.", new Object[0]);
            return false;
        } else if (gattCharacteristic.getValue().length == 0) {
            HiLog.error(TAG, "writeCharacteristic value illeagal.", new Object[0]);
            return false;
        } else if (gattCharacteristic.getService() == null) {
            HiLog.error(TAG, "writeCharacteristic service illeagal.", new Object[0]);
            return false;
        } else {
            synchronized (this.mWriteReadLock) {
                if (this.mIsWriteReadBusy) {
                    HiLog.error(TAG, "writeCharacteristic state illeagal.", new Object[0]);
                    return false;
                }
                this.mIsWriteReadBusy = true;
                try {
                    this.mProxy.writeCharacteristic(this.mClientHandle, this.mAddress, 0, gattCharacteristic);
                    return true;
                } catch (RemoteException unused) {
                    releaseWriteReadLock();
                    return false;
                }
            }
        }
    }

    public boolean setNotifyCharacteristic(GattCharacteristic gattCharacteristic, boolean z) {
        if (this.mClientHandle == 0 || gattCharacteristic.getService() == null) {
            return false;
        }
        try {
            this.mProxy.registerForNotification(this.mClientHandle, this.mAddress, gattCharacteristic.getHandle(), z);
            return true;
        } catch (RemoteException unused) {
            return false;
        }
    }

    public boolean readDescriptor(GattDescriptor gattDescriptor) {
        if (this.mClientHandle == 0) {
            HiLog.error(TAG, "readDescriptor mClientHandle illeagal.", new Object[0]);
            return false;
        }
        GattCharacteristic characteristic = gattDescriptor.getCharacteristic();
        if (characteristic == null || characteristic.getService() == null) {
            HiLog.error(TAG, "readDescriptor characteristic or service illeagal.", new Object[0]);
            return false;
        }
        synchronized (this.mWriteReadLock) {
            if (this.mIsWriteReadBusy) {
                HiLog.error(TAG, "the reading operation of descriptor is blocked", new Object[0]);
                return false;
            }
            this.mIsWriteReadBusy = true;
            try {
                this.mProxy.readDescriptor(this.mClientHandle, this.mAddress, gattDescriptor.getHandle(), 0);
                return true;
            } catch (RemoteException unused) {
                releaseWriteReadLock();
                return false;
            }
        }
    }

    public boolean writeDescriptor(GattDescriptor gattDescriptor) {
        if (this.mClientHandle == 0) {
            HiLog.error(TAG, "writeDescriptor mClientHandle illeagal.", new Object[0]);
            return false;
        } else if (gattDescriptor.getValue().length == 0) {
            HiLog.error(TAG, "writeDescriptor value illeagal.", new Object[0]);
            return false;
        } else {
            GattCharacteristic characteristic = gattDescriptor.getCharacteristic();
            if (characteristic == null || characteristic.getService() == null) {
                HiLog.error(TAG, "writeDescriptor characteristic or service illeagal.", new Object[0]);
                return false;
            }
            synchronized (this.mWriteReadLock) {
                if (this.mIsWriteReadBusy) {
                    HiLog.error(TAG, "the writing operation of descriptor is blocked", new Object[0]);
                    return false;
                }
                this.mIsWriteReadBusy = true;
                try {
                    this.mProxy.writeDescriptor(this.mClientHandle, this.mAddress, gattDescriptor.getHandle(), 0, gattDescriptor.getValue());
                    return true;
                } catch (RemoteException unused) {
                    releaseWriteReadLock();
                    return false;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Optional<GattCharacteristic> getCharacteristicByHandle(int i) {
        for (GattService gattService : this.mServices) {
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

    /* access modifiers changed from: package-private */
    public Optional<GattDescriptor> getDescriptorByHandle(int i) {
        for (GattService gattService : this.mServices) {
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

    public boolean readRemoteRssiValue() {
        int i = this.mClientHandle;
        if (i == 0) {
            HiLog.error(TAG, "readRemoteRssiValue mClientHandle illeagal.", new Object[0]);
            return false;
        }
        try {
            this.mProxy.readRemoteRssiValue(i, this.mAddress);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean requestBleConnectionPriority(int i) {
        if (i < 0 || i > 2) {
            HiLog.error(TAG, "readRemoteRssiValue mClientHandle illeagal.", new Object[0]);
            return false;
        }
        int i2 = this.mClientHandle;
        if (i2 == 0) {
            HiLog.error(TAG, "readRemoteRssiValue mClientHandle illeagal.", new Object[0]);
            return false;
        }
        try {
            this.mProxy.requestBleConnectionPriority(i2, this.mAddress, i);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean requestBleMtuSize(int i) {
        int i2 = this.mClientHandle;
        if (i2 == 0) {
            HiLog.error(TAG, "requestBleMtuSize mClientHandle illeagal.", new Object[0]);
            return false;
        }
        try {
            this.mProxy.requestBleMtuSize(i2, this.mAddress, i);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "RemoteException", new Object[0]);
            return false;
        }
    }
}
