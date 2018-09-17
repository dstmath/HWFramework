package huawei.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDeviceUtils;
import android.bluetooth.IBluetooth;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class HwBluetoothDeviceEx {
    private static final int CODE_ADD_NFC_PAIRING_WHITE_LIST = 1001;
    private static final int CODE_CLEAR_NFC_PAIRING_WHITE_LIST = 1003;
    private static final int CODE_READ_RSSI = 2001;
    private static final int CODE_READ_RSSI_CALLBACK = 2002;
    private static final int CODE_REMOVE_NFC_PAIRING_WHITE_LIST = 1002;
    private static final String DESCRIPTOR = "android.bluetooth.IBluetooth";
    private static final String TAG = "HwBluetoothDeviceEx";
    private static HwBluetoothDeviceEx mInstance;

    public interface IReadRssiCallback {
        void onReadRssi(int i, int i2, BluetoothDevice bluetoothDevice);
    }

    private static class ReadRssiCallbackBinder extends Binder {
        private IReadRssiCallback mCallback;

        public ReadRssiCallbackBinder(IReadRssiCallback callback) {
            this.mCallback = callback;
        }

        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != HwBluetoothDeviceEx.CODE_READ_RSSI_CALLBACK) {
                return super.onTransact(code, data, reply, flags);
            }
            BluetoothDevice bluetoothDevice;
            int status = data.readInt();
            int rssi = data.readInt();
            if (data.readInt() != 0) {
                bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
            } else {
                bluetoothDevice = null;
            }
            Log.d(HwBluetoothDeviceEx.TAG, "ReadRssiCallback: status = " + status + " rssi = " + rssi + " device = " + bluetoothDevice + " mCallback = " + this.mCallback);
            if (this.mCallback != null) {
                this.mCallback.onReadRssi(status, rssi, bluetoothDevice);
            }
            reply.writeNoException();
            reply.writeInt(1);
            return true;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.bluetooth.HwBluetoothDeviceEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.bluetooth.HwBluetoothDeviceEx.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.bluetooth.HwBluetoothDeviceEx.<clinit>():void");
    }

    public static HwBluetoothDeviceEx getDefault() {
        return mInstance;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addNfcPairingWhiteList(BluetoothDevice device, String address) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBluetooth sService = BluetoothDeviceUtils.getService(device);
            if (sService != null) {
                IBinder binder = sService.asBinder();
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(address);
                binder.transact(CODE_ADD_NFC_PAIRING_WHITE_LIST, _data, _reply, 0);
                _reply.readException();
            } else {
                Log.e(TAG, "BT not enabled. Cannot addNfcPairingWhiteList to Remote Device");
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeNfcPairingWhiteList(BluetoothDevice device, String deviceAddress) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBluetooth sService = BluetoothDeviceUtils.getService(device);
            if (sService != null) {
                IBinder binder = sService.asBinder();
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(deviceAddress);
                binder.transact(CODE_REMOVE_NFC_PAIRING_WHITE_LIST, _data, _reply, 0);
                _reply.readException();
            } else {
                Log.e(TAG, "BT not enabled. Cannot removeNfcPairingWhiteList to Remote Device");
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clearNfcPairingWhiteList(BluetoothDevice device) {
        Log.d(TAG, "clearNfcPairingWhiteList");
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBluetooth sService = BluetoothDeviceUtils.getService(device);
            if (sService != null) {
                IBinder binder = sService.asBinder();
                _data.writeInterfaceToken(DESCRIPTOR);
                binder.transact(CODE_CLEAR_NFC_PAIRING_WHITE_LIST, _data, _reply, 0);
                _reply.readException();
            } else {
                Log.e(TAG, "BT not enabled. Cannot clearNfcPairingWhiteList to Remote Device");
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean readRssi(BluetoothDevice device, IReadRssiCallback callback) {
        Log.d(TAG, "readRssi");
        if (device == null || callback == null) {
            Log.e(TAG, "Invalid args in readRssi(): device = " + device + "callback: " + callback);
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean result = false;
        try {
            IBluetooth sService = BluetoothDeviceUtils.getService(device);
            if (sService != null) {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(1);
                device.writeToParcel(data, 0);
                data.writeStrongBinder(new ReadRssiCallbackBinder(callback));
                sService.asBinder().transact(CODE_READ_RSSI, data, reply, 0);
                reply.readException();
                result = reply.readInt() != 0;
            } else {
                Log.e(TAG, "Cannot readRssi!");
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
        return result;
    }
}
