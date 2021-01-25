package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

class HandsFreeAgProxy implements IHandsFreeAg {
    private static final int COMMAND_ALL_CONNECTED = 10;
    private static final int COMMAND_CONNECT_AUDIO = 8;
    private static final int COMMAND_CONNECT_HF_DEVICE = 4;
    private static final int COMMAND_DISCONNECT_AUDIO = 9;
    private static final int COMMAND_DISCONNECT_HF_DEVICE = 5;
    private static final int COMMAND_GET_DEVICES_BY_STATES_AG = 1;
    private static final int COMMAND_GET_DEVICE_STATE_AG = 2;
    private static final int COMMAND_GET_SCO_STATE_AG = 3;
    private static final int COMMAND_START_VOICE_RECOGNITION = 6;
    private static final int COMMAND_STOP_VOICE_RECOGNITION = 7;
    private static final int DEFAULT_HANDS_FREE_AG_NUM = 5;
    private static final int ERR_OK = 0;
    private static final int MIN_TRANSACTION_ID = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "HandsFreeAgProxy");
    private final Object mRemoteLock = new Object();
    private IRemoteObject mRemoteService;

    HandsFreeAgProxy() {
    }

    public IRemoteObject asObject() {
        synchronized (this.mRemoteLock) {
            if (this.mRemoteService != null) {
                return this.mRemoteService;
            }
            this.mRemoteService = BluetoothHostProxy.getInstace().getSaProfileProxy(1).orElse(null);
            if (this.mRemoteService == null) {
                HiLog.error(TAG, "get HFP failed.", new Object[0]);
            } else {
                this.mRemoteService.addDeathRecipient(new HandsFreeAgDeathRecipient(), 0);
            }
            return this.mRemoteService;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.mRemoteLock) {
            HiLog.info(TAG, "HandsFreeAgProxy::setRemoteObject.", new Object[0]);
            this.mRemoteService = iRemoteObject;
        }
    }

    /* access modifiers changed from: private */
    public class HandsFreeAgDeathRecipient implements IRemoteObject.DeathRecipient {
        private HandsFreeAgDeathRecipient() {
        }

        public void onRemoteDied() {
            HiLog.warn(HandsFreeAgProxy.TAG, "HandsFreeAgDeathRecipient::onRemoteDied.", new Object[0]);
            HandsFreeAgProxy.this.setRemoteObject(null);
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IHandsFreeAg
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "HandsFreeAgProxy getDevicesByStates : remote is null", new Object[0]);
                return new ArrayList();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeIntArray(iArr);
            try {
                asObject.sendRequest(1, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "getDevicesByStates : call fail", new Object[0]);
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
                HiLog.error(TAG, "getDevicesByStates for HFP Audio Gateway: call error", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return new ArrayList();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeAg
    public int getScoState(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getScoState : remote is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(3, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "getScoState : call fail", new Object[0]);
                    return 0;
                } else {
                    int readInt2 = obtain2.readInt();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt2;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getScoState : call error", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeAg
    public int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getDeviceState : remote is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(2, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "getDeviceState : call fail", new Object[0]);
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
    }

    @Override // ohos.bluetooth.IHandsFreeAg
    public boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "connect : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(4, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "connect : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    obtain.reclaim();
                    obtain2.reclaim();
                    HiLog.debug(TAG, "parcel reclaimed after HFP ag connecting", new Object[0]);
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    HiLog.debug(TAG, "parcel reclaimed after HFP ag connecting", new Object[0]);
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "connect : a remote exception occured", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                HiLog.debug(TAG, "parcel reclaimed after HFP ag connecting", new Object[0]);
                return false;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                HiLog.debug(TAG, "parcel reclaimed after HFP ag connecting", new Object[0]);
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeAg
    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "disconnect : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(5, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "disconnect : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "disconnect : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeAg
    public boolean openVoiceRecognition(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "openVoiceRecognition : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(6, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "openVoiceRecognition : an error received after request sent:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "openVoiceRecognition : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeAg
    public boolean closeVoiceRecognition(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "closeVoiceRecognition : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(7, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "closeVoiceRecognition : an error received after request sent:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "closeVoiceRecognition : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeAg
    public boolean connectSco() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "connectSco : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                asObject.sendRequest(8, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "connectSco : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "connectSco : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeAg
    public boolean disconnectSco() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "disconnectSco : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                asObject.sendRequest(9, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "disconnectSco : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "disconnectSco : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IHandsFreeAg
    public List<BluetoothRemoteDevice> getConnectedDevices() {
        ArrayList arrayList;
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getConnectedDevices : remote is null", new Object[0]);
                return new ArrayList();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                asObject.sendRequest(10, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "getConnectedDevices : an error received after request sent:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    ArrayList arrayList2 = new ArrayList();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return arrayList2;
                } else {
                    int readInt2 = obtain2.readInt();
                    if (readInt2 < 0) {
                        arrayList = new ArrayList();
                    } else {
                        arrayList = new ArrayList(5);
                    }
                    for (int i = 0; i < readInt2; i++) {
                        if (obtain2.getReadableBytes() <= 0) {
                            HiLog.warn(TAG, "getConnectedDevices: data read failed due to data size mismatch", new Object[0]);
                            obtain.reclaim();
                            obtain2.reclaim();
                            return arrayList;
                        }
                        BluetoothRemoteDevice bluetoothRemoteDevice = new BluetoothRemoteDevice();
                        obtain2.readSequenceable(bluetoothRemoteDevice);
                        arrayList.add(bluetoothRemoteDevice);
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return arrayList;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getConnectedDevices : a remote exception occured", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return new ArrayList();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }
}
