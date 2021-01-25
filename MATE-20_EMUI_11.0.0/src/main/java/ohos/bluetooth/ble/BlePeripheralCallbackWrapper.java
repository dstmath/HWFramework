package ohos.bluetooth.ble;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class BlePeripheralCallbackWrapper extends BlePeripheralCallbackSkeleton {
    private static final int AUTHENTICATION_MITM = 2;
    private static final int AUTHENTICATION_NONE = 0;
    private static final int AUTHENTICATION_NO_MITM = 1;
    private static final int AUTH_RETRY_STATE_IDLE = 0;
    private static final int AUTH_RETRY_STATE_MITM = 2;
    private static final int AUTH_RETRY_STATE_NO_MITM = 1;
    private static final int GATT_INSUFFICIENT_AUTHENTICATION = 5;
    private static final int GATT_INSUFFICIENT_ENCRYPTION = 15;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BlePeripheralCallbackWrapper");
    private int mAuthRetryState;
    private BlePeripheralCallback mCallback;
    private BlePeripheralDevice mDevice;
    private BlePeripheralProxy mProxy;

    public BlePeripheralCallbackWrapper(BlePeripheralDevice blePeripheralDevice, BlePeripheralCallback blePeripheralCallback, BlePeripheralProxy blePeripheralProxy, String str) {
        super(str);
        this.mDevice = blePeripheralDevice;
        this.mCallback = blePeripheralCallback;
        this.mProxy = blePeripheralProxy;
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralCallback
    public void onClientRegistered(int i, int i2) {
        this.mDevice.setClientHandle(i2);
        if (i != 0) {
            this.mCallback.onConnectionStateChanged(0);
        } else {
            this.mProxy.clientConnect(i2, this.mDevice.getAddress(), this.mDevice.isAutoConnect());
        }
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralCallback
    public void onClientConnectionState(int i, int i2, boolean z, String str) {
        if (Objects.equals(this.mDevice.getAddress(), str)) {
            this.mCallback.onConnectionStateChanged(z ? 2 : 0);
            this.mDevice.releaseWriteReadLock();
        }
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralCallback
    public void onSearchComplete(String str, List<GattService> list, int i) {
        if (Objects.equals(this.mDevice.getAddress(), str)) {
            this.mDevice.updateServiceAndFixIncluedeRelation(list);
            this.mCallback.onServicesDiscovered(i);
        }
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralCallback
    public void onCharacteristicRead(String str, int i, int i2, byte[] bArr) {
        if (Objects.equals(str, this.mDevice.getAddress())) {
            this.mDevice.releaseWriteReadLock();
            if (i == 5 || i == 15) {
                int i3 = this.mAuthRetryState;
                int i4 = 2;
                if (i3 != 2) {
                    if (i3 == 0) {
                        i4 = 1;
                    }
                    try {
                        this.mProxy.readCharacteristic(this.mDevice.getClientHandle(), this.mDevice.getAddress(), i2, i4);
                        this.mAuthRetryState++;
                        return;
                    } catch (RemoteException unused) {
                        HiLog.error(TAG, "onCharacteristicRead call fail", new Object[0]);
                    }
                }
            }
            this.mAuthRetryState = 0;
            this.mDevice.getCharacteristicByHandle(i2).ifPresent(new Consumer(i, bArr) {
                /* class ohos.bluetooth.ble.$$Lambda$BlePeripheralCallbackWrapper$Qe9ynn4Euq6k_RUkOnk9z4fSNug */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ byte[] f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    BlePeripheralCallbackWrapper.this.lambda$onCharacteristicRead$0$BlePeripheralCallbackWrapper(this.f$1, this.f$2, (GattCharacteristic) obj);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onCharacteristicRead$0$BlePeripheralCallbackWrapper(int i, byte[] bArr, GattCharacteristic gattCharacteristic) {
        if (i == 0) {
            gattCharacteristic.setValue(bArr);
        }
        this.mCallback.onCharacteristicReadResult(gattCharacteristic, i);
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralCallback
    public void onCharacteristicWrite(String str, int i, int i2) {
        if (Objects.equals(str, this.mDevice.getAddress())) {
            this.mDevice.releaseWriteReadLock();
            this.mDevice.getCharacteristicByHandle(i2).ifPresent(new Consumer(i) {
                /* class ohos.bluetooth.ble.$$Lambda$BlePeripheralCallbackWrapper$BaXKqz_VXd_EqyfC7b50ba3mkqc */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    BlePeripheralCallbackWrapper.this.lambda$onCharacteristicWrite$1$BlePeripheralCallbackWrapper(this.f$1, (GattCharacteristic) obj);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onCharacteristicWrite$1$BlePeripheralCallbackWrapper(int i, GattCharacteristic gattCharacteristic) {
        if (i == 5 || i == 15) {
            int i2 = this.mAuthRetryState;
            int i3 = 2;
            if (i2 != 2) {
                if (i2 == 0) {
                    i3 = 1;
                }
                try {
                    this.mProxy.writeCharacteristic(this.mDevice.getClientHandle(), this.mDevice.getAddress(), i3, gattCharacteristic);
                    this.mAuthRetryState++;
                    return;
                } catch (RemoteException unused) {
                    HiLog.error(TAG, "onCharacteristicWrite call fail", new Object[0]);
                    return;
                }
            }
        }
        this.mAuthRetryState = 0;
        this.mCallback.onCharacteristicWriteResult(gattCharacteristic, i);
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralCallback
    public void onNotify(String str, int i, byte[] bArr) {
        if (Objects.equals(str, this.mDevice.getAddress())) {
            this.mDevice.getCharacteristicByHandle(i).ifPresent(new Consumer(bArr) {
                /* class ohos.bluetooth.ble.$$Lambda$BlePeripheralCallbackWrapper$aIr_vEvZjluIZOYDD4qW3uI2MjE */
                private final /* synthetic */ byte[] f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    BlePeripheralCallbackWrapper.this.lambda$onNotify$2$BlePeripheralCallbackWrapper(this.f$1, (GattCharacteristic) obj);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onNotify$2$BlePeripheralCallbackWrapper(byte[] bArr, GattCharacteristic gattCharacteristic) {
        gattCharacteristic.setValue(bArr);
        this.mCallback.onCharacteristicChanged(gattCharacteristic);
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralCallback
    public void onDescriptorRead(String str, int i, int i2, byte[] bArr) {
        if (Objects.equals(str, this.mDevice.getAddress())) {
            this.mDevice.releaseWriteReadLock();
            if (i == 5 || i == 15) {
                int i3 = this.mAuthRetryState;
                int i4 = 2;
                if (i3 != 2) {
                    if (i3 == 0) {
                        i4 = 1;
                    }
                    try {
                        this.mProxy.readDescriptor(this.mDevice.getClientHandle(), this.mDevice.getAddress(), i2, i4);
                        this.mAuthRetryState++;
                        return;
                    } catch (RemoteException unused) {
                        HiLog.error(TAG, "onDescriptorRead call fail", new Object[0]);
                    }
                }
            }
            this.mAuthRetryState = 0;
            this.mDevice.getDescriptorByHandle(i2).ifPresent(new Consumer(i, bArr) {
                /* class ohos.bluetooth.ble.$$Lambda$BlePeripheralCallbackWrapper$UIRTLStwY6STVH_2D0o3qv1m4EE */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ byte[] f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    BlePeripheralCallbackWrapper.this.lambda$onDescriptorRead$3$BlePeripheralCallbackWrapper(this.f$1, this.f$2, (GattDescriptor) obj);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onDescriptorRead$3$BlePeripheralCallbackWrapper(int i, byte[] bArr, GattDescriptor gattDescriptor) {
        if (i == 0) {
            gattDescriptor.setValue(bArr);
        }
        this.mCallback.onDescriptorReadResult(gattDescriptor, i);
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralCallback
    public void onDescriptorWrite(String str, int i, int i2) {
        if (Objects.equals(str, this.mDevice.getAddress())) {
            this.mDevice.releaseWriteReadLock();
            this.mDevice.getDescriptorByHandle(i2).ifPresent(new Consumer(i, i2) {
                /* class ohos.bluetooth.ble.$$Lambda$BlePeripheralCallbackWrapper$csKgymYyHYogXrNaCuStBuirIak */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    BlePeripheralCallbackWrapper.this.lambda$onDescriptorWrite$4$BlePeripheralCallbackWrapper(this.f$1, this.f$2, (GattDescriptor) obj);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onDescriptorWrite$4$BlePeripheralCallbackWrapper(int i, int i2, GattDescriptor gattDescriptor) {
        int i3;
        if ((i == 5 || i == 15) && (i3 = this.mAuthRetryState) != 2) {
            try {
                this.mProxy.writeDescriptor(this.mDevice.getClientHandle(), this.mDevice.getAddress(), i2, i3 == 0 ? 1 : 2, gattDescriptor.getValue());
                this.mAuthRetryState++;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "onDescriptorWrite call fail", new Object[0]);
            }
        } else {
            this.mAuthRetryState = 0;
            this.mCallback.onDescriptorWriteResult(gattDescriptor, i);
        }
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralCallback
    public void onReadRemoteRssi(String str, int i, int i2) {
        if (Objects.equals(str, this.mDevice.getAddress())) {
            this.mCallback.readRemoteRssiEvent(i, i2);
        }
    }

    @Override // ohos.bluetooth.ble.IBlePeripheralCallback
    public void onConfigureMTU(String str, int i, int i2) {
        if (Objects.equals(str, this.mDevice.getAddress())) {
            this.mCallback.mtuUpdateEvent(i, i2);
        }
    }
}
