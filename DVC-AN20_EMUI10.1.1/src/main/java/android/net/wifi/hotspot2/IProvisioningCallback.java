package android.net.wifi.hotspot2;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IProvisioningCallback extends IInterface {
    void onProvisioningComplete() throws RemoteException;

    void onProvisioningFailure(int i) throws RemoteException;

    void onProvisioningStatus(int i) throws RemoteException;

    public static class Default implements IProvisioningCallback {
        @Override // android.net.wifi.hotspot2.IProvisioningCallback
        public void onProvisioningFailure(int status) throws RemoteException {
        }

        @Override // android.net.wifi.hotspot2.IProvisioningCallback
        public void onProvisioningStatus(int status) throws RemoteException {
        }

        @Override // android.net.wifi.hotspot2.IProvisioningCallback
        public void onProvisioningComplete() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IProvisioningCallback {
        private static final String DESCRIPTOR = "android.net.wifi.hotspot2.IProvisioningCallback";
        static final int TRANSACTION_onProvisioningComplete = 3;
        static final int TRANSACTION_onProvisioningFailure = 1;
        static final int TRANSACTION_onProvisioningStatus = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IProvisioningCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IProvisioningCallback)) {
                return new Proxy(obj);
            }
            return (IProvisioningCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onProvisioningFailure";
            }
            if (transactionCode == 2) {
                return "onProvisioningStatus";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onProvisioningComplete";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onProvisioningFailure(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onProvisioningStatus(data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onProvisioningComplete();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IProvisioningCallback {
            public static IProvisioningCallback sDefaultImpl;
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

            @Override // android.net.wifi.hotspot2.IProvisioningCallback
            public void onProvisioningFailure(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProvisioningFailure(status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.hotspot2.IProvisioningCallback
            public void onProvisioningStatus(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProvisioningStatus(status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.hotspot2.IProvisioningCallback
            public void onProvisioningComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProvisioningComplete();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IProvisioningCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IProvisioningCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
