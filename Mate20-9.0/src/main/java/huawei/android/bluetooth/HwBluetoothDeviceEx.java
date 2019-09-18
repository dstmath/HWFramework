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
    private static HwBluetoothDeviceEx mInstance = new HwBluetoothDeviceEx();

    public interface IReadRssiCallback {
        void onReadRssi(int i, int i2, BluetoothDevice bluetoothDevice);
    }

    private static class ReadRssiCallbackBinder extends Binder {
        private IReadRssiCallback mCallback;

        public ReadRssiCallbackBinder(IReadRssiCallback callback) {
            this.mCallback = callback;
        }

        /* access modifiers changed from: protected */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BluetoothDevice device;
            if (code != HwBluetoothDeviceEx.CODE_READ_RSSI_CALLBACK) {
                return super.onTransact(code, data, reply, flags);
            }
            int status = data.readInt();
            int rssi = data.readInt();
            if (data.readInt() != 0) {
                device = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
            } else {
                device = null;
            }
            Log.d(HwBluetoothDeviceEx.TAG, "ReadRssiCallback: status = " + status + " rssi = " + rssi + " device = " + device + " mCallback = " + this.mCallback);
            if (this.mCallback != null) {
                this.mCallback.onReadRssi(status, rssi, device);
            }
            reply.writeNoException();
            reply.writeInt(1);
            return true;
        }
    }

    public static HwBluetoothDeviceEx getDefault() {
        return mInstance;
    }

    public void addNfcPairingWhiteList(BluetoothDevice device, String address) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBluetooth sService = BluetoothDeviceUtils.getService(device);
            if (sService != null) {
                IBinder binder = sService.asBinder();
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(address);
                binder.transact(1001, _data, _reply, 0);
                _reply.readException();
            } else {
                Log.e(TAG, "BT not enabled. Cannot addNfcPairingWhiteList to Remote Device");
            }
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    public void removeNfcPairingWhiteList(BluetoothDevice device, String deviceAddress) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBluetooth sService = BluetoothDeviceUtils.getService(device);
            if (sService != null) {
                IBinder binder = sService.asBinder();
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(deviceAddress);
                binder.transact(1002, _data, _reply, 0);
                _reply.readException();
            } else {
                Log.e(TAG, "BT not enabled. Cannot removeNfcPairingWhiteList to Remote Device");
            }
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    public void clearNfcPairingWhiteList(BluetoothDevice device) {
        Log.d(TAG, "clearNfcPairingWhiteList");
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBluetooth sService = BluetoothDeviceUtils.getService(device);
            if (sService != null) {
                IBinder binder = sService.asBinder();
                _data.writeInterfaceToken(DESCRIPTOR);
                binder.transact(1003, _data, _reply, 0);
                _reply.readException();
            } else {
                Log.e(TAG, "BT not enabled. Cannot clearNfcPairingWhiteList to Remote Device");
            }
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
    }

    public boolean readRssi(BluetoothDevice device, IReadRssiCallback callback) {
        Log.d(TAG, "readRssi");
        boolean result = false;
        if (device == null || callback == null) {
            Log.e(TAG, "Invalid args in readRssi(): device = " + device + "callback: " + callback);
            return false;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean result2 = false;
        try {
            IBluetooth sService = BluetoothDeviceUtils.getService(device);
            if (sService != null) {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(1);
                device.writeToParcel(data, 0);
                data.writeStrongBinder(new ReadRssiCallbackBinder(callback));
                sService.asBinder().transact(2001, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 0) {
                    result = true;
                }
                result2 = result;
            } else {
                Log.e(TAG, "Cannot readRssi!");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return result2;
    }
}
