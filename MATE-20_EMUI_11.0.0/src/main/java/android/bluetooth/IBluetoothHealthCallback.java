package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IBluetoothHealthCallback extends IInterface {
    void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration, int i) throws RemoteException;

    void onHealthChannelStateChange(BluetoothHealthAppConfiguration bluetoothHealthAppConfiguration, BluetoothDevice bluetoothDevice, int i, int i2, ParcelFileDescriptor parcelFileDescriptor, int i3) throws RemoteException;

    public static class Default implements IBluetoothHealthCallback {
        @Override // android.bluetooth.IBluetoothHealthCallback
        public void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration config, int status) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothHealthCallback
        public void onHealthChannelStateChange(BluetoothHealthAppConfiguration config, BluetoothDevice device, int prevState, int newState, ParcelFileDescriptor fd, int id) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBluetoothHealthCallback {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothHealthCallback";
        static final int TRANSACTION_onHealthAppConfigurationStatusChange = 1;
        static final int TRANSACTION_onHealthChannelStateChange = 2;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onHealthAppConfigurationStatusChange";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onHealthChannelStateChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BluetoothHealthAppConfiguration _arg0;
            BluetoothHealthAppConfiguration _arg02;
            BluetoothDevice _arg1;
            ParcelFileDescriptor _arg4;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onHealthAppConfigurationStatusChange(_arg0, data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = BluetoothHealthAppConfiguration.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = BluetoothDevice.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                int _arg2 = data.readInt();
                int _arg3 = data.readInt();
                if (data.readInt() != 0) {
                    _arg4 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                } else {
                    _arg4 = null;
                }
                onHealthChannelStateChange(_arg02, _arg1, _arg2, _arg3, _arg4, data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IBluetoothHealthCallback {
            public static IBluetoothHealthCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.bluetooth.IBluetoothHealthCallback
            public void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration config, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(status);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onHealthAppConfigurationStatusChange(config, status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothHealthCallback
            public void onHealthChannelStateChange(BluetoothHealthAppConfiguration config, BluetoothDevice device, int prevState, int newState, ParcelFileDescriptor fd, int id) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeInt(prevState);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(newState);
                        if (fd != null) {
                            _data.writeInt(1);
                            fd.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(id);
                            if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onHealthChannelStateChange(config, device, prevState, newState, fd, id);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
        }

        public static boolean setDefaultImpl(IBluetoothHealthCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBluetoothHealthCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
