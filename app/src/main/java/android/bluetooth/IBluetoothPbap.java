package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBluetoothPbap extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothPbap {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothPbap";
        static final int TRANSACTION_connect = 3;
        static final int TRANSACTION_disconnect = 4;
        static final int TRANSACTION_getClient = 2;
        static final int TRANSACTION_getState = 1;
        static final int TRANSACTION_isConnected = 5;

        private static class Proxy implements IBluetoothPbap {
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

            public int getState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public BluetoothDevice getClient() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    BluetoothDevice bluetoothDevice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getClient, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(_reply);
                    } else {
                        bluetoothDevice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bluetoothDevice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean connect(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_connect, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disconnect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isConnected(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isConnected, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothPbap asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothPbap)) {
                return new Proxy(obj);
            }
            return (IBluetoothPbap) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            BluetoothDevice bluetoothDevice;
            boolean _result;
            switch (code) {
                case TRANSACTION_getState /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = getState();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getClient /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    BluetoothDevice _result3 = getClient();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getState);
                        _result3.writeToParcel(reply, TRANSACTION_getState);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_connect /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    _result = connect(bluetoothDevice);
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_getState;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_disconnect /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    disconnect();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isConnected /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    _result = isConnected(bluetoothDevice);
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_getState;
                    }
                    reply.writeInt(i);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean connect(BluetoothDevice bluetoothDevice) throws RemoteException;

    void disconnect() throws RemoteException;

    BluetoothDevice getClient() throws RemoteException;

    int getState() throws RemoteException;

    boolean isConnected(BluetoothDevice bluetoothDevice) throws RemoteException;
}
