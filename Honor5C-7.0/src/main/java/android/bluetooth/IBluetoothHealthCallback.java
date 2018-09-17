package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IBluetoothHealthCallback extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothHealthCallback {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothHealthCallback";
        static final int TRANSACTION_onHealthAppConfigurationStatusChange = 1;
        static final int TRANSACTION_onHealthChannelStateChange = 2;

        private static class Proxy implements IBluetoothHealthCallback {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration config, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(Stub.TRANSACTION_onHealthAppConfigurationStatusChange);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onHealthAppConfigurationStatusChange, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onHealthChannelStateChange(BluetoothHealthAppConfiguration config, BluetoothDevice device, int prevState, int newState, ParcelFileDescriptor fd, int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(Stub.TRANSACTION_onHealthAppConfigurationStatusChange);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_onHealthAppConfigurationStatusChange);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(prevState);
                    _data.writeInt(newState);
                    if (fd != null) {
                        _data.writeInt(Stub.TRANSACTION_onHealthAppConfigurationStatusChange);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(id);
                    this.mRemote.transact(Stub.TRANSACTION_onHealthChannelStateChange, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothHealthCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothHealthCallback)) {
                return new Proxy(obj);
            }
            return (IBluetoothHealthCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration;
            switch (code) {
                case TRANSACTION_onHealthAppConfigurationStatusChange /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothHealthAppConfiguration = (BluetoothHealthAppConfiguration) BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothHealthAppConfiguration = null;
                    }
                    onHealthAppConfigurationStatusChange(bluetoothHealthAppConfiguration, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onHealthChannelStateChange /*2*/:
                    BluetoothDevice bluetoothDevice;
                    ParcelFileDescriptor parcelFileDescriptor;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothHealthAppConfiguration = (BluetoothHealthAppConfiguration) BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothHealthAppConfiguration = null;
                    }
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    int _arg2 = data.readInt();
                    int _arg3 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    onHealthChannelStateChange(bluetoothHealthAppConfiguration, bluetoothDevice, _arg2, _arg3, parcelFileDescriptor, data.readInt());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration, int i) throws RemoteException;

    void onHealthChannelStateChange(BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration, BluetoothDevice bluetoothDevice, int i, int i2, ParcelFileDescriptor parcelFileDescriptor, int i3) throws RemoteException;
}
