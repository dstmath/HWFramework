package android.bluetooth;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBluetoothProfileServiceConnection extends IInterface {
    void onServiceConnected(ComponentName componentName, IBinder iBinder) throws RemoteException;

    void onServiceDisconnected(ComponentName componentName) throws RemoteException;

    public static class Default implements IBluetoothProfileServiceConnection {
        @Override // android.bluetooth.IBluetoothProfileServiceConnection
        public void onServiceConnected(ComponentName comp, IBinder service) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothProfileServiceConnection
        public void onServiceDisconnected(ComponentName comp) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBluetoothProfileServiceConnection {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothProfileServiceConnection";
        static final int TRANSACTION_onServiceConnected = 1;
        static final int TRANSACTION_onServiceDisconnected = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothProfileServiceConnection asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothProfileServiceConnection)) {
                return new Proxy(obj);
            }
            return (IBluetoothProfileServiceConnection) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onServiceConnected";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onServiceDisconnected";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            ComponentName _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onServiceConnected(_arg0, data.readStrongBinder());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onServiceDisconnected(_arg02);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IBluetoothProfileServiceConnection {
            public static IBluetoothProfileServiceConnection sDefaultImpl;
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

            @Override // android.bluetooth.IBluetoothProfileServiceConnection
            public void onServiceConnected(ComponentName comp, IBinder service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (comp != null) {
                        _data.writeInt(1);
                        comp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(service);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onServiceConnected(comp, service);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothProfileServiceConnection
            public void onServiceDisconnected(ComponentName comp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (comp != null) {
                        _data.writeInt(1);
                        comp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onServiceDisconnected(comp);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBluetoothProfileServiceConnection impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBluetoothProfileServiceConnection getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
