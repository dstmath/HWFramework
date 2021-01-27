package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.Parcel;

class HandsFreeHfProxy implements IHandsFreeHf {
    private static final int COMMAND_ACCEPT_CALL_HF = 5;
    private static final int COMMAND_CONNECT_AG_DEVICE_HF = 12;
    private static final int COMMAND_CONNECT_AUDIO_HF = 10;
    private static final int COMMAND_DIAL_HF = 4;
    private static final int COMMAND_DISCONNECT_AG_DEVICE_HF = 13;
    private static final int COMMAND_DISCONNECT_AUDIO_HF = 11;
    private static final int COMMAND_GET_DEVICES_BY_STATES_HF = 1;
    private static final int COMMAND_GET_DEVICE_STATE_HF = 2;
    private static final int COMMAND_GET_SCO_STATE_HF = 3;
    private static final int COMMAND_HOLD_CALL_HF = 6;
    private static final int COMMAND_REJECT_CALL_HF = 7;
    private static final int COMMAND_SEND_DTMF_HF = 9;
    private static final int COMMAND_TERMINATE_CALL_HF = 8;
    private static final int DEFAULT_HANDS_FREE_HF_NUM = 5;
    private static final int ERR_OK = 0;
    private static final int MIN_TRANSACTION_ID = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "HandsFreeHfProxy");
    private final Object mRemoteLock = new Object();
    private IRemoteObject mRemoteService;

    HandsFreeHfProxy() {
    }

    public IRemoteObject asObject() {
        synchronized (this.mRemoteLock) {
            if (this.mRemoteService != null) {
                return this.mRemoteService;
            }
            this.mRemoteService = BluetoothHostProxy.getInstace().getSaProfileProxy(2).orElse(null);
            if (this.mRemoteService == null) {
                HiLog.error(TAG, "get HFP_UNIT failed.", new Object[0]);
            } else {
                this.mRemoteService.addDeathRecipient(new HandsFreeHfProxyDeathRecipient(), 0);
            }
            return this.mRemoteService;
        }
    }

    /* access modifiers changed from: private */
    public class HandsFreeHfProxyDeathRecipient implements IRemoteObject.DeathRecipient {
        private HandsFreeHfProxyDeathRecipient() {
        }

        public void onRemoteDied() {
            HiLog.warn(HandsFreeHfProxy.TAG, "HandsFreeHfProxyDeathRecipient::onRemoteDied.", new Object[0]);
            HandsFreeHfProxy.this.setRemoteObject(null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.mRemoteLock) {
            HiLog.info(TAG, "HandsFreeHfProxy::setRemoteObject.", new Object[0]);
            this.mRemoteService = iRemoteObject;
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "HandsFreeHfProxy getDevicesByStates : remote is null", new Object[0]);
                return new ArrayList();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeIntArray(iArr);
            try {
                asObject.sendRequest(1, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "getDevicesByStates : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    ArrayList arrayList = new ArrayList();
                    obtain.reclaim();
                    obtain2.reclaim();
                    HiLog.debug(TAG, "parcel reclaimed after HFP hands-free connecting", new Object[0]);
                    return arrayList;
                } else {
                    ArrayList<BluetoothRemoteDevice> createDeviceList = Utils.createDeviceList(obtain2, 5);
                    obtain.reclaim();
                    obtain2.reclaim();
                    HiLog.debug(TAG, "parcel reclaimed after HFP hands-free connecting", new Object[0]);
                    return createDeviceList;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getDevicesByStates for HFP hands-free unit : call error", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                HiLog.debug(TAG, "parcel reclaimed after HFP hands-free connecting", new Object[0]);
                return new ArrayList();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                HiLog.debug(TAG, "parcel reclaimed after HFP hands-free connecting", new Object[0]);
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
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
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(2, obtain, obtain2, messageOption);
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
    }

    @Override // ohos.bluetooth.IHandsFreeHf
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
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(3, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "getScoState : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
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

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IHandsFreeHf
    public Optional<HandsFreeUnitCall> startDial(BluetoothRemoteDevice bluetoothRemoteDevice, String str) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "dial : remote is null", new Object[0]);
                return Optional.empty();
            }
            MessageParcel obtain = MessageParcel.obtain();
            Parcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            obtain.writeString(str);
            try {
                asObject.sendRequest(4, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "dial : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    Optional<HandsFreeUnitCall> empty = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty;
                } else {
                    HandsFreeUnitCall handsFreeUnitCall = new HandsFreeUnitCall();
                    handsFreeUnitCall.unmarshllingFromSa(obtain2);
                    Optional<HandsFreeUnitCall> ofNullable = Optional.ofNullable(handsFreeUnitCall);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return ofNullable;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "dial : a remote exception occured", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return Optional.empty();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
    public boolean acceptIncomingCall(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "acceptCall : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            obtain.writeInt(i);
            try {
                asObject.sendRequest(5, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "acceptCall : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "acceptCall : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
    public boolean holdActiveCall(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "holdCall : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(6, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "holdCall : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "holdCall : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
    public boolean rejectIncomingCall(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "rejectCall : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(7, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "rejectCall : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "rejectCall : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
    public boolean finishActiveCall(BluetoothRemoteDevice bluetoothRemoteDevice, HandsFreeUnitCall handsFreeUnitCall) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "terminateCall : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            obtain.writeSequenceable(handsFreeUnitCall);
            try {
                asObject.sendRequest(8, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "terminateCall : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "terminateCall : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
    public boolean sendDTMFTone(BluetoothRemoteDevice bluetoothRemoteDevice, byte b) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "sendDTMF : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            obtain.writeByte(b);
            try {
                asObject.sendRequest(9, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "sendDTMF : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "sendDTMF : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
    public boolean connectSco(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "connectAudio : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(10, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "connectAudio : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "connectAudio : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
    public boolean disconnectSco(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "disconnectAudio : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(11, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "disconnectAudio : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "disconnectAudio : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
    public boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "connect hf : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(12, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "connect hf : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "connect hf : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IHandsFreeHf
    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "disconnect hf : remote is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            obtain.writeInterfaceToken("ohos.bluetooth.IBluetoothHost");
            obtain.writeSequenceable(bluetoothRemoteDevice);
            try {
                asObject.sendRequest(13, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt != 0) {
                    HiLog.error(TAG, "disconnect hf : an error received after request sent, error:%{public}d", new Object[]{Integer.valueOf(readInt)});
                    return false;
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "disconnect hf : a remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }
}
