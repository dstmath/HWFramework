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
public class PbapClientProxy implements IPbapClient {
    private static final int COMMAND_CONNECT_CLIENT = 3;
    private static final int COMMAND_DISCONNECT_CLIENT = 4;
    private static final int COMMAND_GET_CONNECT_STRATEGY_CLIENT = 6;
    private static final int COMMAND_GET_DEVICES_BY_STATES_CLIENT = 1;
    private static final int COMMAND_GET_DEVICE_STATE_CLIENT = 2;
    private static final int COMMAND_SET_CONNECT_STRATEGY_CLIENT = 5;
    private static final int DEFAULT_PBAP_CLIENT_NUM = 5;
    private static final int ERR_OK = 0;
    private static final int MIN_TRANSACTION_ID = 1;
    private static final int STRATEGY_ALLOW = 100;
    private static final int STRATEGY_AUTO = 1000;
    private static final int STRATEGY_DISALLOW = 0;
    private static final int STRATEGY_UNKNOWN = -1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "PbapClientProxy");
    private IRemoteObject remote;

    PbapClientProxy() {
    }

    public IRemoteObject asObject() {
        this.remote = null;
        BluetoothHostProxy.getInstace().getSaProfileProxy(8).ifPresent(new Consumer() {
            /* class ohos.bluetooth.$$Lambda$PbapClientProxy$AdjXoIvlISrtRxk7Vjq6GDqUKFg */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                PbapClientProxy.this.lambda$asObject$0$PbapClientProxy((IRemoteObject) obj);
            }
        });
        return this.remote;
    }

    public /* synthetic */ void lambda$asObject$0$PbapClientProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IPbapClient
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        if (asObject() == null) {
            HiLog.error(TAG, "PbapClientProxy getDevicesByStates : got null remote", new Object[0]);
            return new ArrayList();
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
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
            HiLog.error(TAG, "PbapClientProxy getDevicesByStates : call error", new Object[0]);
            obtain.reclaim();
            obtain2.reclaim();
            return new ArrayList();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.bluetooth.IPbapClient
    public int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice) {
        if (asObject() == null) {
            HiLog.error(TAG, "getDeviceState : got null remote", new Object[0]);
            return 0;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
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

    @Override // ohos.bluetooth.IPbapClient
    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        if (asObject() == null) {
            HiLog.error(TAG, "disconnect client : got null remote", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
        obtain.writeSequenceable(bluetoothRemoteDevice);
        try {
            this.remote.sendRequest(4, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt == 3756) {
                throw new SecurityException("Permission denied");
            } else if (readInt != 0) {
                HiLog.error(TAG, "disconnect client : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                return false;
            } else {
                boolean readBoolean = obtain2.readBoolean();
                obtain.reclaim();
                obtain2.reclaim();
                return readBoolean;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "disconnect client : a remote exception occured", new Object[0]);
            return false;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.bluetooth.IPbapClient
    public boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        if (asObject() == null) {
            HiLog.error(TAG, "connect client : got null remote", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
        obtain.writeSequenceable(bluetoothRemoteDevice);
        try {
            this.remote.sendRequest(3, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt == 3756) {
                throw new SecurityException("Permission denied");
            } else if (readInt != 0) {
                HiLog.error(TAG, "connect client : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                return false;
            } else {
                boolean readBoolean = obtain2.readBoolean();
                obtain.reclaim();
                obtain2.reclaim();
                return readBoolean;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "connect client : a remote exception occured", new Object[0]);
            return false;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.bluetooth.IPbapClient
    public boolean setConnectStrategy(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        int i2;
        if (asObject() == null) {
            HiLog.error(TAG, "setConnectStrategy : got null remote", new Object[0]);
            return false;
        }
        if (i == 1) {
            i2 = 100;
        } else if (i == 0) {
            i2 = 0;
        } else {
            HiLog.error(TAG, "setConnectStrategy : got illegal strategy value", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
        obtain.writeSequenceable(bluetoothRemoteDevice);
        obtain.writeInt(i2);
        try {
            this.remote.sendRequest(5, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt == 3756) {
                throw new SecurityException("Permission denied");
            } else if (readInt != 0) {
                HiLog.error(TAG, "setConnectStrategy : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                return false;
            } else {
                boolean readBoolean = obtain2.readBoolean();
                obtain.reclaim();
                obtain2.reclaim();
                return readBoolean;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "setConnectStrategy : a remote exception occured", new Object[0]);
            return false;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.bluetooth.IPbapClient
    public int getConnectStrategy(BluetoothRemoteDevice bluetoothRemoteDevice) {
        int i = 0;
        if (asObject() == null) {
            HiLog.error(TAG, "getConnectStrategy : got null remote", new Object[0]);
            return -1;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
        obtain.writeSequenceable(bluetoothRemoteDevice);
        try {
            this.remote.sendRequest(6, obtain, obtain2, messageOption);
            int readInt = obtain2.readInt();
            if (readInt == 3756) {
                throw new SecurityException("Permission denied");
            } else if (readInt != 0) {
                HiLog.error(TAG, "getConnectStrategy : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                return -1;
            } else {
                int readInt2 = obtain2.readInt();
                if (readInt2 != -1) {
                    if (readInt2 != 0) {
                        if (readInt2 == 100) {
                            i = 1;
                        } else if (readInt2 == 1000) {
                            i = 2;
                        }
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return i;
                }
                i = -1;
                obtain.reclaim();
                obtain2.reclaim();
                return i;
            }
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getConnectStrategy : call error", new Object[0]);
            return -1;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }
}
