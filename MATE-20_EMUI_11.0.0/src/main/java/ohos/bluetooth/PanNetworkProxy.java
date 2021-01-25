package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

/* access modifiers changed from: package-private */
public class PanNetworkProxy implements IPanNetwork {
    private static final int COMMAND_CONNECT_PAN = 3;
    private static final int COMMAND_DISCONNECT_PAN = 4;
    private static final int COMMAND_GET_DEVICES_BY_STATES_PAN = 1;
    private static final int COMMAND_GET_DEVICE_STATE_PAN = 2;
    private static final int DEFAULT_PAN_NUM = 5;
    private static final int ERR_OK = 0;
    private static final int MIN_TRANSACTION_ID = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "PanNetworkProxy");
    private IRemoteObject remote;

    PanNetworkProxy() {
    }

    public IRemoteObject asObject() {
        this.remote = null;
        BluetoothHostProxy.getInstace().getSaProfileProxy(15).ifPresent(new Consumer() {
            /* class ohos.bluetooth.$$Lambda$PanNetworkProxy$w69fhfyMAX49JPz5xpTUJZu4Qg */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                PanNetworkProxy.this.lambda$asObject$0$PanNetworkProxy((IRemoteObject) obj);
            }
        });
        return this.remote;
    }

    public /* synthetic */ void lambda$asObject$0$PanNetworkProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IPanNetwork
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        if (asObject() == null) {
            HiLog.error(TAG, "PanNetworkProxy getDevicesByStates : got null remote", new Object[0]);
            return new ArrayList();
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeIntArray(iArr);
        try {
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt == 3756) {
                throw new SecurityException("Permission denied");
            } else if (readInt != 0) {
                HiLog.error(TAG, "getDevicesByStates : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                ArrayList arrayList = new ArrayList();
                obtain.reclaim();
                obtain2.reclaim();
                return arrayList;
            } else {
                ArrayList<BluetoothRemoteDevice> createDeviceList = Utils.createDeviceList(obtain2, 5);
                obtain.reclaim();
                obtain2.reclaim();
                return createDeviceList;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "PanNetworkProxy getDevicesByStates : call error", new Object[0]);
            obtain.reclaim();
            obtain2.reclaim();
            return new ArrayList();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.bluetooth.IPanNetwork
    public int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice) {
        if (asObject() == null) {
            HiLog.error(TAG, "getDeviceState : got null remote", new Object[0]);
            return 0;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeSequenceable(bluetoothRemoteDevice);
        try {
            this.remote.sendRequest(2, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt == 3756) {
                throw new SecurityException("Permission denied");
            } else if (readInt != 0) {
                HiLog.error(TAG, "getDeviceState : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                return 0;
            } else {
                int readInt2 = obtain2.readInt();
                obtain.reclaim();
                obtain2.reclaim();
                return readInt2;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getDeviceState : call error", new Object[0]);
            return 0;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.bluetooth.IPanNetwork
    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        if (asObject() == null) {
            HiLog.error(TAG, "disconnect pan device : got null remote", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeSequenceable(bluetoothRemoteDevice);
        try {
            this.remote.sendRequest(4, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt == 3756) {
                throw new SecurityException("Permission denied");
            } else if (readInt != 0) {
                HiLog.error(TAG, "disconnect pan device : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                return false;
            } else {
                boolean readBoolean = obtain2.readBoolean();
                obtain.reclaim();
                obtain2.reclaim();
                return readBoolean;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "disconnect pan device : a remote exception occured", new Object[0]);
            return false;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.bluetooth.IPanNetwork
    public boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        if (asObject() == null) {
            HiLog.error(TAG, "connect pan device : got null remote", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeSequenceable(bluetoothRemoteDevice);
        try {
            this.remote.sendRequest(3, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt == 3756) {
                throw new SecurityException("Permission denied");
            } else if (readInt != 0) {
                HiLog.error(TAG, "connect pan device : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                return false;
            } else {
                boolean readBoolean = obtain2.readBoolean();
                obtain.reclaim();
                obtain2.reclaim();
                return readBoolean;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "connect pan device : a remote exception occured", new Object[0]);
            return false;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }
}
