package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDeviceAdapterEx;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.bluetooth.IBluetoothDeviceInforCallback;

public class BluetoothDeviceInfor {
    private static final int CODE_GET_DEVICE_INFOR_CALLBACK = 1019;
    private static final int CODE_GET_RANGE = 1018;
    private static final int CODE_GET_RSSI = 1017;
    private static final String DESCRIPTOR = "android.bluetooth.IBluetooth";
    private static final String TAG = "BluetoothDeviceInfor";
    private static final int TYPE_DEVICE_BR_READ_RANGE = 22;
    private static final int TYPE_DEVICE_BR_READ_RSSI = 21;
    private static BluetoothDeviceInfor mInstance = new BluetoothDeviceInfor();

    public static BluetoothDeviceInfor getDefault() {
        return mInstance;
    }

    public void getDeviceInfor(BluetoothDevice device, int type, BluetoothDeviceInforCallback callback) {
        Log.d(TAG, "getDeviceInfor");
        if (callback == null) {
            Log.e(TAG, "getDeviceInfor callback is null");
        } else if (device == null) {
            Log.e(TAG, "getDeviceInfor device is null");
        } else {
            new BluetoothDeviceInforCallbackWrapper(callback).getDeviceInforByType(device, type);
        }
    }

    /* access modifiers changed from: private */
    public static class BluetoothDeviceInforCallbackBinder extends Binder {
        private IBluetoothDeviceInforCallback mCallback;

        BluetoothDeviceInforCallbackBinder(IBluetoothDeviceInforCallback callback) {
            this.mCallback = callback;
        }

        /* access modifiers changed from: protected */
        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BluetoothDevice device;
            if (code != BluetoothDeviceInfor.CODE_GET_DEVICE_INFOR_CALLBACK) {
                return super.onTransact(code, data, reply, flags);
            }
            int status = data.readInt();
            int result = data.readInt();
            if (data.readInt() != 0) {
                device = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
            } else {
                device = null;
            }
            Log.d(BluetoothDeviceInfor.TAG, "BluetoothDeviceInforCallback: status = " + status + " result = " + result + " mCallback = " + this.mCallback);
            if (this.mCallback != null) {
                this.mCallback.onDeviceInforResult(device, result, status);
            }
            reply.writeNoException();
            reply.writeInt(1);
            return true;
        }
    }

    private class BluetoothDeviceInforCallbackWrapper extends IBluetoothDeviceInforCallback.Stub {
        private final BluetoothDeviceInforCallback mBluetoothDeviceInforCallback;

        public BluetoothDeviceInforCallbackWrapper(BluetoothDeviceInforCallback callback) {
            this.mBluetoothDeviceInforCallback = callback;
        }

        private int getCodeType(int type) {
            switch (type) {
                case 21:
                    return BluetoothDeviceInfor.CODE_GET_RSSI;
                case 22:
                    return BluetoothDeviceInfor.CODE_GET_RANGE;
                default:
                    return 1;
            }
        }

        public void getDeviceInforByType(BluetoothDevice device, int type) {
            Log.d(BluetoothDeviceInfor.TAG, "getDeviceInforByType");
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(BluetoothDeviceInfor.DESCRIPTOR);
            data.writeInt(1);
            device.writeToParcel(data, 0);
            data.writeStrongBinder(new BluetoothDeviceInforCallbackBinder(this));
            BluetoothDeviceAdapterEx.readRssiAdapter(device, getCodeType(type), data, Parcel.obtain());
        }

        @Override // com.huawei.android.bluetooth.IBluetoothDeviceInforCallback
        public void onDeviceInforResult(BluetoothDevice device, int result, int status) throws RemoteException {
            this.mBluetoothDeviceInforCallback.onDeviceInforResult(device, result, status);
        }
    }
}
