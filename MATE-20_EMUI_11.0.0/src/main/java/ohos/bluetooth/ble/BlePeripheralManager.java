package ohos.bluetooth.ble;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ohos.app.Context;
import ohos.bluetooth.BluetoothHostProxy;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.SequenceUuid;

public class BlePeripheralManager {
    private static final int CALLBACK_TIMEOUT = 10000;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BlePeripheralManager");
    private BlePeripheralManagerCallback mBlePeripheralManagerCallback;
    private BlePeripheralManagerProxy mBlePeripheralManagerProxy;
    private BluetoothHostProxy mBluetoothHostProxy;
    private BlePeripheralManagerCallbackWrapper mCallBackWrapper;
    private GattService mPendingService;
    private volatile int mServerIf;
    private Object mServerLock;
    private List<GattService> mServices;
    private int mTransport;

    public List<GattService> getServices() {
        return this.mServices;
    }

    public BlePeripheralManager(Context context, BlePeripheralManagerCallback blePeripheralManagerCallback, int i) {
        this.mBlePeripheralManagerProxy = null;
        this.mBlePeripheralManagerCallback = null;
        this.mCallBackWrapper = null;
        this.mServerLock = new Object();
        this.mBlePeripheralManagerProxy = new BlePeripheralManagerProxy(BluetoothHostProxy.getInstace().getSaProfileProxy(12).orElse(null));
        this.mTransport = i;
        openGattServer(context, blePeripheralManagerCallback);
        this.mServices = new ArrayList();
    }

    public boolean addService(GattService gattService) {
        HiLog.debug(TAG, "adding service: %{public}s", new Object[]{gattService.getUuid().toString()});
        if (this.mBlePeripheralManagerProxy == null || this.mServerIf == 0) {
            HiLog.error(TAG, "peripheral manager was not valid", new Object[0]);
            return false;
        }
        this.mPendingService = gattService;
        this.mBlePeripheralManagerProxy.addService(this.mServerIf, gattService);
        return true;
    }

    public void cancelConnection(BlePeripheralDevice blePeripheralDevice) {
        HiLog.debug(TAG, "cancelConnection: %{public}s", new Object[]{blePeripheralDevice.getAddress()});
        if (this.mBlePeripheralManagerProxy == null || this.mServerIf == 0) {
            HiLog.error(TAG, "peripheral manager was not valid", new Object[0]);
        } else {
            this.mBlePeripheralManagerProxy.disconnectServer(this.mServerIf, blePeripheralDevice.getAddress());
        }
    }

    private void openGattServer(Context context, BlePeripheralManagerCallback blePeripheralManagerCallback) {
        if (blePeripheralManagerCallback == null) {
            HiLog.error(TAG, "null input parameters was input", new Object[0]);
            throw new IllegalArgumentException("null parameter: " + context + " " + blePeripheralManagerCallback);
        } else if (this.mBlePeripheralManagerProxy != null) {
            Boolean.valueOf(registerCallback(blePeripheralManagerCallback));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean registerCallback(BlePeripheralManagerCallback blePeripheralManagerCallback) {
        HiLog.debug(TAG, "registerCallback", new Object[0]);
        if (this.mBlePeripheralManagerProxy == null) {
            HiLog.error(TAG, "gatt service is null", new Object[0]);
            return false;
        }
        synchronized (this.mServerLock) {
            if (this.mBlePeripheralManagerCallback != null) {
                HiLog.error(TAG, "App can register callback only once", new Object[0]);
                return false;
            }
            this.mBlePeripheralManagerCallback = blePeripheralManagerCallback;
            this.mCallBackWrapper = new BlePeripheralManagerCallbackWrapper(this.mBlePeripheralManagerCallback, this, "ohos.bluetooth.ble.IBlePeripheralCallback");
            this.mBlePeripheralManagerProxy.registerServer(new SequenceUuid(UUID.randomUUID()), this.mCallBackWrapper);
            try {
                this.mServerLock.wait(10000);
            } catch (InterruptedException unused) {
                this.mBlePeripheralManagerCallback = null;
            }
            if (this.mServerIf != 0) {
                return true;
            }
            this.mBlePeripheralManagerCallback = null;
            return false;
        }
    }

    public void close() {
        unregisterCallback();
    }

    public void clearServices() {
        HiLog.debug(TAG, "clearServices", new Object[0]);
        if (this.mBlePeripheralManagerProxy == null || this.mServerIf == 0) {
            HiLog.error(TAG, "peripheral manager was not valid", new Object[0]);
            return;
        }
        this.mBlePeripheralManagerProxy.clearServices(this.mServerIf);
        this.mServices.clear();
    }

    public List<BlePeripheralDevice> getDevicesByStates(int[] iArr) {
        HiLog.debug(TAG, "getDevicesByStates", new Object[0]);
        if (this.mBlePeripheralManagerProxy != null && this.mServerIf != 0) {
            return this.mBlePeripheralManagerProxy.getDevicesByStates(iArr);
        }
        HiLog.error(TAG, "peripheral manager was not valid", new Object[0]);
        return new ArrayList();
    }

    public boolean notifyCharacteristicChanged(BlePeripheralDevice blePeripheralDevice, GattCharacteristic gattCharacteristic, boolean z) {
        if (this.mBlePeripheralManagerProxy == null || this.mServerIf == 0 || gattCharacteristic.getService() == null) {
            return false;
        }
        if (gattCharacteristic.getValue() != null) {
            this.mBlePeripheralManagerProxy.sendNotification(this.mServerIf, blePeripheralDevice.getAddress(), gattCharacteristic.getHandle(), z, gattCharacteristic.getValue());
            return true;
        }
        throw new IllegalArgumentException("Chracteristic value is empty.");
    }

    public boolean removeGattService(GattService gattService) {
        if (this.mBlePeripheralManagerProxy == null || this.mServerIf == 0) {
            return false;
        }
        Optional<GattService> service = getService(gattService.getUuid(), gattService.getHandle(), gattService.isPrimary());
        if (!service.isPresent()) {
            return false;
        }
        this.mBlePeripheralManagerProxy.removeService(this.mServerIf, gattService.getHandle());
        this.mServices.remove(service);
        return true;
    }

    public boolean sendResponse(BlePeripheralDevice blePeripheralDevice, int i, int i2, int i3, byte[] bArr) {
        if (this.mBlePeripheralManagerProxy == null || this.mServerIf == 0) {
            return false;
        }
        this.mBlePeripheralManagerProxy.sendResponse(this.mServerIf, blePeripheralDevice.getAddress(), i, i2, i3, bArr);
        return true;
    }

    /* access modifiers changed from: package-private */
    public Optional<GattService> getService(UUID uuid, int i, boolean z) {
        for (GattService gattService : this.mServices) {
            if (gattService.isPrimary() == z && gattService.getHandle() == i && gattService.getUuid().equals(uuid)) {
                return Optional.ofNullable(gattService);
            }
        }
        return Optional.empty();
    }

    private void unregisterCallback() {
        synchronized (this.mServerLock) {
            HiLog.debug(TAG, "unregisterCallback", new Object[0]);
            if (this.mBlePeripheralManagerProxy != null) {
                if (this.mServerIf != 0) {
                    this.mBlePeripheralManagerCallback = null;
                    this.mCallBackWrapper = null;
                    this.mBlePeripheralManagerProxy.unregisterServer(this.mServerIf);
                    this.mServerIf = 0;
                    return;
                }
            }
            HiLog.error(TAG, "peripheral manager was not valid", new Object[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerServerIf(int i) {
        synchronized (this.mServerLock) {
            if (this.mBlePeripheralManagerCallback != null) {
                this.mServerIf = i;
                this.mServerLock.notify();
            } else {
                HiLog.error(TAG, "no available callbakc", new Object[0]);
            }
        }
    }
}
